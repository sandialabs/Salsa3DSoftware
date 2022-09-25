/**
 * Copyright 2009 Sandia Corporation. Under the terms of Contract
 * DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government
 * retains certain rights in this software.
 * 
 * BSD Open Source License.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of Sandia National Laboratories nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package gov.sandia.gmp.observationprediction;

import static gov.sandia.gmp.util.globals.Globals.NL;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.parallelutils.ParallelTask;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.numerical.polygon.Polygon3D;
import gov.sandia.gmp.util.profiler.Profiler;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * A parallel distributed task that runs in a distributed fashion using the JPPF system by calling
 * the run() function on a distributed node.
 *
 * <p>
 * The job of this object is to execute a list of PredictorObservation (aPredObs) using the Bender
 * predictor. The bender GeoModel is stored as a static object and is recreated if the current model
 * file name is different than the instantiated file name which is saved in the static string
 * aModelFilePath.
 *
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class PredictorParallelTask extends ParallelTask {

  /**
   * The current static instantiated tomography GeoModel. The object is initialized to null to force
   * construction (if requested) on the first entry.
   */
  protected static GeoTessModel aTomoModel = null;

  /**
   * The current static instantiated tomography GeoTessModel file name.
   */
  private static String aTomoModelFilePath = "";

  /**
   * Simple static id counter that is auto-incremented with each constructor call and set into the
   * JPPFTask as its task id.
   */
  private static int aId = -1;

  /**
   * Current version string.
   */
  // private static String aVersion = "4.1.0";

  /**
   * The list of predictor observation objects to be processed by the run() method.
   */
  private ArrayList<PredictorObservation> aPredObs = null;

  /**
   * The GeoModel file path from which the GeoModel used by the Bender is created.
   */
  private String predModelFilePath = null;

  /**
   * The GeoModel file path from which the GeoModel used by the Bender is created.
   */
  private String tomoModelFilePath = null;

  /**
   * The Polygon file path from which the active node polygon used by the GeoModel is created.
   */
  private String polygonFilePath = null;

  /**
   * Simple static debug flag.
   */
  private static boolean aDebug = false;

  /**
   * Simple static screen output flag.
   */
  private static boolean aOutput = true;

  /**
   * The initial submission time of this task. This time is carried through the process until
   * returned to the client where it is used to determine the total process and parallel transfer
   * time of the task.
   */
  private long aTaskSubmitTime = 0;

  /**
   * The profiler sample period (milliseconds). If less than 1 it is not used.
   */
  private long aProfilerSamplePeriod = -1;

  /**
   * The profiler verbose flag. Outputs the profile here at the node if true.
   */
  private boolean aProfilerNodeVerbose = false;

  /**
   * Read GeoModel fail limit.
   */
  private transient static int aReadModelFailLimit = 10;

  private PropertiesPlusGMP aProperties = null;

  /**
   * Standard constructor that sets the GeoModel file name, GeoModel active region polygon, and the
   * list of BenderObservation objects to be executed by the run() method.
   *
   * @param modelFilePath GeoModel file name.
   * @param modelFileFormat GeoModel file format specifier. (ignored)
   * @param polygonFilePath GeoModel active region polygon.
   * @param actvNodeAttributes Array of active node GeoAttributes (typically) PSLOWNESS, SSLOWNESS,
   *        or both) to be used to define the active node setting.
   * @param benderObs List of BenderObservation objects to be executed by the run() method.
   */
  public PredictorParallelTask(String predModelFilePath, String tomoModelFilePath,
      String polygonFilePath, ArrayList<PredictorObservation> benderObs) {
    this.aPredObs = new ArrayList<PredictorObservation>(benderObs);
    this.predModelFilePath = predModelFilePath;
    this.tomoModelFilePath = tomoModelFilePath;
    this.polygonFilePath = polygonFilePath;

    // set JPPFTask id
    ++aId;
    setId(Integer.toString(aId));
  }


  /**
   * Returns the current code version.
   * 
   * @return The current code version.
   */
  public static String getVersion() {
    return ObservationPrediction.getVersion();
  }


  /**
   * Sets the profilers sample period (milliseconds).
   * 
   * @param psp The profilers sample period (milliseconds).
   */
  public void setProfiler(long psp, boolean psv) {
    aProfilerSamplePeriod = psp;
    aProfilerNodeVerbose = psv;
  }

  /**
   * Sets a default predictor properties object.
   * 
   * @param properties The default predictor properties object.
   */
  public void setDefaultPredictorProperties(PropertiesPlusGMP predProperties) {
    aProperties = predProperties;
  }

  /**
   * The primary parallel task function that processes all PredictionRequest objects stored in
   * aPredObs list when this task was instantiated. Each PredictionRequest is processed using the
   * Bender ray calculation method to compute a Prediction (RayInfo from Bender) object result that
   * is stored in a PredictorResult for return to the caller.
   */
  @Override
  public void run() {
    // convert file paths from Windows to Linux if necessary

    aTomoModelFilePath = PropertiesPlus.convertWinFilePathToLinux(aTomoModelFilePath);
    predModelFilePath = PropertiesPlus.convertWinFilePathToLinux(predModelFilePath);
    tomoModelFilePath = PropertiesPlus.convertWinFilePathToLinux(tomoModelFilePath);
    polygonFilePath = PropertiesPlus.convertWinFilePathToLinux(polygonFilePath);

    // create and initialize the task result

    PredictorParallelTaskResult results;
    results = new PredictorParallelTaskResult(aPredObs.size(), predModelFilePath, polygonFilePath);
    results.setTaskSubmitTime(getSubmitTime());
    results.setIndex(getIndex());
    setResult(results);

    // create temporary definitions and objects

    PredictorFactory predictorFactory = null;
    Profiler profiler = null;

    // enter error catch code

    try {
      // get and save host name

      String hostname = (InetAddress.getLocalHost()).getHostName();
      results.setHostName(hostname);
      outputTaskInfo(results.getHostName(), "Entry", "");

      // create profiler if requested

      if (aProfilerSamplePeriod > 0) {
        profiler = new Profiler(Thread.currentThread(), aProfilerSamplePeriod,
            "PredictorParallelTask:" + hostname);
        profiler.setTopClass("gov.sandia.gmp.observationprediction.PredictorParallelTask");
        profiler.setTopMethod("run");
        profiler.accumulateOn();
      }

      // Make a default properties file if it does not exist or does not
      // define a predictor.

      long time0 = (new Date()).getTime();
      if (aProperties == null)
        aProperties = new PropertiesPlusGMP();
      if (aProperties.getProperty("predictors") == null)
        aProperties.put("predictors", "Bender");
      if (aProperties.get("predictors").equals("Bender")) {
        aProperties.put("benderModel", predModelFilePath);
        if ((polygonFilePath != null) && !polygonFilePath.equals(""))
          aProperties.put("benderModelActiveNodePolygon", polygonFilePath);
      }

      // create the predictor factory ... add the prediction requests ... get
      // the predictions

      predictorFactory = new PredictorFactory(aProperties, "predictors");
      ArrayList<Prediction> predictions = predictorFactory.computePredictions(aPredObs);

      // done with predictions ... now get the prediction model and create the
      // tomography model for producing node weights if requested.

      
      //TODO: we now support more than Bender ...
      GeoTessModel predModel =
          ((GeoTessModel) predictorFactory.getPredictor(predictions.get(0).getPredictorType())
              .getEarthModel());
      predModelFilePath = predModel.getCurrentModelFileName();
      createTomographyGeoTessModel(predModel);

      // loop over all predictions and populate the prediction result list and
      // build the ray weights for each prediction

      for (int i = 0; i < predictions.size(); i++) {
        // add ray to list and continue
        Prediction pi = predictions.get(i);

        if (pi.getPredictionRequest() != null) {
            PredictorResult pr = new PredictorResult(pi,
                pi.getPredictionRequest().getObservationId(), true);
            pr.setRayWeights(pi.getRayWeights());
            results.addRay(pr);
        }
    }


      // done ... set list of rays into results and the calculation time
      // and host processor name ... exit

      long boid = aPredObs.get(0).getObservationId();
      if (aOutput)
        System.out.println(
            "Finished Compute Ray Calculation (Group Index = " + boid + ") ...");
      results.setCalculationTime(time0);

      if (aDebug)
        results.appendMessage("Execution complete .... Done");

      // turn off profiler if on and set into results

      if (profiler != null) {
        profiler.stop();
        if (aProfilerNodeVerbose)
          profiler.printAccumulationString();
        results.setProfilerContent(profiler.getProfilerContent());
        profiler = null;
      }
      outputTaskInfo(results.getHostName(), "Exit", "");
    } catch (UnsupportedOperationException ex) {
      // likely GeoTessModel or Bender creation error

      if (profiler != null) {
        profiler.stop();
        if (aProfilerNodeVerbose)
          profiler.printAccumulationString();
        profiler = null;
      }

      // set exception in results object and return

      if (aOutput)
        System.out.println("PredictorParallelTask::catch " + "(UnsupportedOperationException ex) ");
      ex.printStackTrace();
      results.setException(ex);
      if (aDebug)
        results.appendMessage("Exception Occurred ...");
      outputTaskInfo(results.getHostName(), "Error", "");
    } catch (Exception ex) {
      // likely GeoTessModel or Bender creation error

      if (profiler != null) {
        profiler.stop();
        if (aProfilerNodeVerbose)
          profiler.printAccumulationString();
        profiler = null;
      }

      // set exception in results object and return

      if (aOutput)
        System.out.println("PredictorParallelTask::catch (Exception ex) ");
      ex.printStackTrace();
      results.setException(ex);
      if (aDebug)
        results.appendMessage("Exception Occurred ...");
      outputTaskInfo(results.getHostName(), "Error", "");
    }
  }

  /**
   * Reads the GeoTessModel at the input file path. The method continues to attempt to read the
   * model should a timeout occur (up to aReadModelFailLimit attempts before throwing an error).
   * 
   * @param modelPath The model to be read.
   * @return The loaded GeoTessModel.
   */
  private GeoTessModel readGeoModel(String modelPath) {
    GeoTessModel mdl;

    int ecnt = 0;
    while (true) {
      try {
        mdl = new GeoTessModel(modelPath);
        return mdl;
      } catch (Exception ex) {
        // unsuccessful ... increment count and try again
        ++ecnt;
        if (ecnt == aReadModelFailLimit) {
          System.exit(-1);
        }

        // Wait for 5 seconds
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
      }
    }
  }

  /**
   * Outputs task information as an event that can be picked up by task listeners. The task id, host
   * name and time are always output. The optional tag and message are added to the string to
   * identify the pertinent information. The information is only sent if aOutput is true.
   * 
   * @param hostname Hostname of the machine executing the task.
   * @param tag Information tag (e.g. "Entry" or "Exit").
   * @param msg Pertinent information to be conveyed by the message.
   */
  private void outputTaskInfo(String hostname, String tag, String msg) {
    if (aOutput) {
      // output a finalization string and send it across to the parent
      // application to notify of task completion

      String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
      String s = "Task (id = " + getId() + ") " + tag + ", Host Name: " + hostname + ", Time: "
          + formatter.format(cal.getTime()) + NL;
      if (!msg.equals(""))
        s += msg + NL;
      System.out.println(s);
      // fireNotification(new JPPFTaskEvent(s));
    }
  }

  /**
   * Sets the Model file path.
   *
   * @param ic The Bender / Model id tag.
   */
  public void setBenderModelFilePath(String predModelFilePath) {
    this.predModelFilePath = predModelFilePath;
  }

  /**
   * Returns the list of PredictorObservation objects.
   *
   * @return The list of PredictorObservation objects.
   */
  public ArrayList<PredictorObservation> getPredictorObservations() {
    return aPredObs;
  }

  /**
   * Set the debug output flag to dbg.
   */
  public void setDebug(boolean dbg) {
    aDebug = dbg;
  }

  /**
   * Set the screen output flag to out.
   */
  public void setOutput(boolean out) {
    aOutput = out;
  }

  /**
   * Sets/Resets a flag to perform a calculation for the change in travel time wrt. slowness for
   * each grid point that influences the ray path. The flag is set if the input flag is true and
   * reset otherwise.
   * 
   * @param aEvalTomoDerivatives
   */
  public void setDerivativeEvaluation(boolean aEvalTomoDerivatives) {
    if (aEvalTomoDerivatives) {
      for (PredictorObservation obs : aPredObs)
        obs.getRequestedAttributes().add(GeoAttributes.DTT_DSLOW);
    } else {
      for (PredictorObservation obs : aPredObs)
        obs.getRequestedAttributes().remove(GeoAttributes.DTT_DSLOW);
    }
  }

  /**
   * Simple utilities to return the stack trace of an exception as a String.
   */
  public static String getStackTraceString(Throwable aThrowable) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }
  
  /**
   * Contains logic for counting and submitting tasks, but delegates the actual submission of tasks
   * to an optional consumer. This allows us to count the number of tasks to be created without
   * actually creating them, if desired. It also allows more complex performance optimizations to
   * be made without making it more difficult to predict the number of tasks that will be created.
   * @param obs
   * @param tomoModelPath
   * @param numPredPerTask
   * @param predictorProps
   * @param saveRayPaths
   * @param predModelPath
   * @param polyFilePath
   * @param cons consumer of tasks
   * @return number of tasks to be streamed
   */
  private static int streamPredictorParallelTasksHelper(ObservationList obs,
      String tomoModelPath, String predModelPath, String polyFilePath, int numPredPerTask,
      PropertiesPlusGMP predictorProps, boolean saveRayPaths, Consumer<PredictorParallelTask> cons){

    // set up predictor observation and predictor parallel task lists
    ArrayList<PredictorObservation> taskPredObsList = new ArrayList<PredictorObservation>();

    // set the last np tasks (approximately) to decrease several observations per
    // task such that the last task has at most nrmin observations (Note: this is
    // a performance tweak so that the processors are not lying around while the
    // last few tasks finish a full prediction set).

    int np = 200;
    int nrmin = 10;
    int nr = 0;
    for (int i = 0; i < np; ++i)
      nr += numPredPerTask - (numPredPerTask - nrmin) * i / (np - 1);
    int Nt = (obs.size() - nr) / numPredPerTask;
    int n = numPredPerTask;

    // loop over all observations and fill the task bundles

    int tskcnt = 0;
    int obscnt = 0;
    for (ObservationTomo ob : obs) {
      // add BenderObservations to current list of PredictorObservations until
      // full then create a new PredictorParallelTask. Note: Only add observations
      // that are valid for tomography and not marked for skipping. If the
      // save ray path flag (saveRayPaths) is set then add them even if they are
      // marked for skipping since ray path information will be required for
      // output at the end of the current iteration.

      if (ob.getStatus().isValidForTomography() && (saveRayPaths || !ob.isSkipRayTrace())) {

        // add a new request (request has bounce point fixed if
        // ob.isSkipBouncePointOptimization() is true

        try {
          taskPredObsList.add(ob.getPredictorObservation(saveRayPaths));
        } catch (Exception ex) {
          ex.printStackTrace();
        }

        // if hit max or end, create a new task and add to list

        if ((taskPredObsList.size() == n) || (obscnt == obs.size() - 1)) {
          PredictorParallelTask ppt = null;
          if(cons != null && tomoModelPath != null && predModelPath != null && 
              polyFilePath != null && predictorProps != null) {
              // create a new task and add it to the list
              ppt = new PredictorParallelTask(predModelPath, tomoModelPath, 
                  polyFilePath, taskPredObsList);
              ppt.setDefaultPredictorProperties(predictorProps);
          }

          // clear list of bender observations for the next task

          taskPredObsList.clear();

          // increment the task count and see if we are at the last np (or so)
          // tasks where the observation count will decrease linearly down to
          // nrmin by the last task

          if (++tskcnt > Nt) {
            n = numPredPerTask - (numPredPerTask - nrmin) * (tskcnt - Nt - 1) / (np - 1);
            if (n < nrmin)
              n = nrmin;
          }
          
          if(ppt != null) cons.accept(ppt);
        }

        // increment the observation count and continue

        ++obscnt;

      } // end if (ob.getStatus().isValidForTomography() ...)
    } // end for (ObservationTomo ob : obs)
    
    return tskcnt;
  }
  
  /**
   * @param obs
   * @param numPredPerTask
   * @return the number of tasks that will be produced by either streamPredictorParallelTasks or
   * buildPredictorParallelTasks for the given number of observations and numbers of predictions
   * per task.
   */
  public static int countPredictorParallelTasks(ObservationList obs, int numPredPerTask) {
    return streamPredictorParallelTasksHelper(obs,null,null,null,numPredPerTask,null,false,null);
  }

  /**
   * Streams a list of parallel tasks from the input list of observations to the specified
   * PredictorParallelTask consumer.
   *
   * @param obs List of all observations from which parallel tasks will be constructed.
   * @param tomoModelPath The path to the tomography model required by each task.
   * @param numPredPerTask Approximate number of predictions per task.
   * @param predictorProps Properties required by the predictor.
   * @param saveRayPaths Boolean, which if true, saves all ray paths requested for tomography
   *        contained in the list obs, regardless of the skip flag setting.
   * @param cons consumer of tasks created by this method, generally a task execution service of
   *        some kind.
   * 
   * @return number of parallel tasks produced and consumed, if the consumer isn't null
   */
  public static int streamPredictorParallelTasks(ObservationList obs,
      String tomoModelPath, int numPredPerTask, PropertiesPlusGMP predictorProps,
      boolean saveRayPaths, Consumer<PredictorParallelTask> cons) {
    // build path information for each bundle
    String predModelPath = "", polyFilePath = "";
    try {
      GeoTessModel geoModel = obs.getGeoModel();
      predModelPath = geoModel.getCurrentModelFileName();
      File polygonFile = geoModel.getActiveRegionPolygonFile();
      if (polygonFile == null)
        polyFilePath = "";
      else
        polyFilePath = polygonFile.getCanonicalPath();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    
    return streamPredictorParallelTasksHelper(obs,tomoModelPath,predModelPath,polyFilePath,
        numPredPerTask,predictorProps,saveRayPaths,cons);
  }
  
  /**
   * Constructs a list of parallel tasks from the input list of observations for processing on
   * concurrent or distributed parallel systems.
   *
   * @param obs List of all observations from which parallel tasks will be constructed.
   * @param tomoModelPath The path to the tomography model required by each task.
   * @param numPredPerTask Approximate number of predictions per task.
   * @param predictorProps Properties required by the predictor.
   * @param saveRayPaths Boolean, which if true, saves all ray paths requested for tomography
   *        contained in the list obs, regardless of the skip flag setting.
   * 
   * @return List of all parallel prediction tasks.
   */
  public static ArrayList<PredictorParallelTask> buildPredictorParallelTasks(ObservationList obs,
      String tomoModelPath, int numPredPerTask, PropertiesPlusGMP predictorProps,
      boolean saveRayPaths) {
    
    ArrayList<PredictorParallelTask> predTaskList = new ArrayList<PredictorParallelTask>();
    streamPredictorParallelTasks(obs,tomoModelPath,numPredPerTask,predictorProps,saveRayPaths,
        predTaskList::add);
    return predTaskList;
  }
  
  /**
   * Sets the task submission time (called only by the client).
   * 
   * @param tsksbmttime The task submission time.
   */
  public void setTaskSubmitTime(long tsksbmttime) {
    aTaskSubmitTime = tsksbmttime;
  }

  /**
   * Sets the task submission time (called only by the client).
   * 
   * @param tsksbmttime The task submission time.
   */
  public long getTaskSubmitTime() {
    return aTaskSubmitTime;
  }
  
//TODO use the waveTypeIndex info here
  /*public boolean getWeights(ArrayList<RaySegmentInfo> rsi,
      InterpolatorType horizontalType,
      InterpolatorType radialType,
      HashMapIntegerDouble weights) throws GeoTessException {
    GeoTessPosition pos = aTomoModel.getGeoTessPosition(horizontalType, radialType);

    for (RaySegmentInfo r : rsi)
    {
      for(int i = 0; i < r.getPoints().size(); i++) {
        pos.set(r.getLayerIndex(), r.getPoints().get(i), r.getRadii().get(i));
        pos.getWeights(weights, GeoTessUtils.getDistance3D(v1,r1,v2,r2));
      }
    }
    return !weights.contains(-1);
  }*/

  /**
   * Formulates a tomography GeoTessModel give one of 3 outcomes. 1) tomoModelFilePath is not
   * defined (i.e. = ""), in which case a tomography model is not produced. This corresponds to
   * standard prediction scenarios where tomography weight calculations are not produced. 2)
   * tomoModelFilePath is defined and is equal to the prediction model (i.e. tomoModelFilePath =
   * predModelFilePath), in which case the input prediction model is used as the tomography model to
   * evaluate tomography weights which are returned to the caller. 3) tomoModelFilePath is defined
   * but does not reference the model file specified by the prediction model. In this case a new
   * tomography model and polygon are constructed and the model file active region is set to the
   * polygon specification. This tomography model is used to calculate tomography weights using ray
   * paths that were calculated from the prediction model.
   * 
   * @param predModel The prediction model used by the predictor factory.
 * @throws Exception 
   */
  private void createTomographyGeoTessModel(GeoTessModel predModel) throws Exception
  {
    synchronized(PredictorParallelTaskResult.class)
    {
      if (!tomoModelFilePath.equals(""))
      {
        if ((aTomoModel == null) || !aTomoModelFilePath.equals(tomoModelFilePath))
        {
        	// check to see if the tomography model file path and the prediction
        	// model file path are the same ... if they are then get the
        	// tomography model from the predictor factory

        	if (predModelFilePath.equals(tomoModelFilePath))
        		aTomoModel = predModel;
        	else
        	{
	          aTomoModel = readGeoModel(tomoModelFilePath);

	          Polygon3D polygon = null;
	          if ((polygonFilePath != null) && (polygonFilePath.length() > 0))
	          {
	          	File f = new File(polygonFilePath);
	            polygon = new Polygon3D(f);
	            aTomoModel.setActiveRegion(polygon);
	          }
	          else
	            aTomoModel.setActiveRegion();

	          if (aOutput)
	          {
	            System.out.println("");
	            System.out.println("Created Tomography GeoTessModel ...");
	            System.out.println(aTomoModel.getMetaData().getInputModelFile().getCanonicalPath());
	            System.out.println("");
	          }
        	}

          // save configuration string and set verbosity

          aTomoModelFilePath = tomoModelFilePath;
        }
      }
    }
  }
}
