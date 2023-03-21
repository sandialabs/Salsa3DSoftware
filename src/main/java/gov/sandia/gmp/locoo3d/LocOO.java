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
package gov.sandia.gmp.locoo3d;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.locoo3d.io.LocOO_IO;
import gov.sandia.gmp.locoo3d.io.NativeOutput;
import gov.sandia.gmp.parallelutils.ParallelBroker;
import gov.sandia.gmp.parallelutils.ParallelBroker.ParallelMode;
import gov.sandia.gmp.parallelutils.ParallelResult;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.profiler.ProfilerContent;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;
import gov.sandia.gmp.util.time.Time;

public class LocOO {
  static public String getVersion() {
    return Utils.getVersion("locoo3d");
  }

  // 1.12.0 2020-10-29 sballar Added depth constraints imposed by
  // seismicity_depth_model.
  //
  // 1.11.1 2020-07-08 sballar Fixed bug in LibCorr3D which
  // prevented supportedPhases to be applied properly.
  //
  // 1.11.0 2018-12-13
  // Added file based input output. Changed IODB_LocOO to DataLoaderOracle.
  // Added interface DataLoader
  // Added file loader .... DataLoaderFile
  // Added new required property to input file "dataLoaderType" which must
  // be set to either "file" or "oracle". If new DB types are added then
  // appropriate names will be added in the future.
  // Modified LocOO.java the change over to DataLoader prescription.

  // 1.10.2g 2017-05-022
  // fixed bug related to observation filters
  //
  // 1.10.2 2017-03-06
  // delivered
  //
  // 1.10.1f 2017-01-12
  // this version uses bender version 4.x.x
  // Also implements the new SeismicBaseData class
  // that has tt and el models stored in the jar file.
  //
  // 1.10.1d 2016-03-02 2016-07-20
  // This version still uses bender that depends on GeoModel
  //
  // 1.10.1 2016-03-01 fixed a few bugs.
  //
  // 1.10.0 2016-01-22 added master event relocation capability
  //
  // 1.9.5 2015-10-17 removed geomodel from dependency list
  // by implementing a topography model with geotess.
  // 2016-01-04 Implemented Flinn_Engdahl region codes
  // (origin.srn and origin.grn).
  //
  // 1.9.4 2015-10-14 First maven release
  //
  // 1.9.3 2014-12-10 Added ability to apply multiplier and offset to
  // tt model uncertainty in Bender.
  // Improved performance of input sql statements.
  //
  // 2014-12-03 Version 1.9.2
  // Updated version number to reflect changes in GeoModel
  // and Bender to suppot models that contain both
  // PSLOWNESS and SSLOWNESS and P and S site terms.
  //
  // 2014-10-23 Version 1.9.1
  // Added dependence on dbtabledefs Schema object.
  // Also fixed issue where GeoTessPosition was rebelling
  // when attempted to change libcorr3d models on the fly
  // but the grid ids were different.
  //
  // 2014-10-01 Version 1.9.0
  // Added dependence on dbtabledefs and removed dependence
  // on dbutillib.
  //
  // 2014-06-07 Version 1.8.10
  // Changed Origerr.sdobs to be the standard deviation of
  // tt, az and sh weighted residuals.
  // Also made all the objects in DBTableDefs serializable.
  //
  // 2014-05-30 Version 1.8.9
  // fixed a bug in LookupTablesGMP that caused NaN
  // values for tt predictions.
  //
  // 2014-01-27 Version 1.8.8
  // fixed a bug that having to do with correlated
  // observations.
  //
  // 2013-12-19 Version 1.8.7
  // fixed a bug that caused azimuth weighted residuals
  // to be incorrect, leading to erroneous locations
  // under rare circumstances. Changes were in
  // ObservationComponent.setPrediction()
  //
  // 2013-12-5 Version 1.8.6
  // Output assoc.wgt to the database. Fixed bug that
  // happened when lat, lon, depth are fixed and only
  // time is free. It was throwing exception because
  // covariance matrix was singular. Fixed that.
  // When lat,lon,depth fixed and only time is free,
  // predictions will be computed in sequential mode
  // because simplex algorithm is not thread-safe.
  //
  // 2013-11-01 Version 1.8.5
  // Added parameter lookup2dTTModelUncertaintyScale
  // to LookupTablesDZ. Also, significant changes
  // to how Predictor handles concurrency. Before,
  // predictor always processed one prediction at a
  // time in concurrent mode. Now processes batches
  // of predictions in concurrent mode.
  //
  // 2013-09012 Version 1.8.4
  // Fixed bugs in LibCorr3DModels that allowed the
  // wrong model to be associated with a station.
  //
  // 2013-07-16 Version 1.8.3
  // Mods to read tt lookup tables and ellipticity
  // correction tables in earth_specs
  // directory formats.
  //
  // 2013-07-01 Version 1.8.2
  // Preparation for testing
  //
  // 2013-03-17 Version 1.8.1
  // Bug fixes
  //
  // 2013-03-08 Version 1.8.0
  // Added library LookupTableDZ which provide support
  // for 2D, distance-depth travel time lookup tables
  // ala saic mini-tables.
  //
  // 2013-01-18 version 1.7.4
  // Fixed bug in tauptoolkit that prevented
  // property tauptoolkitPgVelocity from being applied.
  //
  // 2013-01-17 Fixed bug where azimuth residuals were
  // not being wrapped properly into range -180 to 180.
  //
  // 2013-01-16 Fixed bug where site elevation was not
  // being converted to depth in DataLoaderOracle.
  //
  // 2012-10-19 version 1.7.0 (sballar)
  // - Implemented LibCorr3D
  //
  // 2011-07-08 version 1.6.0 (sballar)
  // - Implemented core phase renaming algorithm.
  // This allows locoo to rename P to PKP and vice versa
  // if the residuals are dramatically improved by doing so.
  // - Also uses version of Bender that precomputes velocity
  // gradient information.
  // - Implemented Simplex algorith.
  //
  // 2011-06-27 version 1.5.0 (sballar)
  // Implements ttt_path_corrections as implemented in
  // TaupToolkitWrapper.
  //
  // 2011-06-20 version 1.4.3 (sballar)
  // No real changes to locoo but changes to dependencies
  //
  // 2011-02-18 version 1.4.2 (sballar)
  // was terminating ungracefully for ill-posed problems
  // (covariance matrix was singular). Improved robustness.
  // Also incorporates some bender improvements.
  //
  // 2011-01-20 version 1.4.1 (sballar)
  // fixed problem that caused incorrect results when
  // allow_big_residuals was set to false.
  //
  // 2010-11-24 version 1.4.0 (sballar)
  // reintroduced ability to use correlated observations
  //
  // 2010-11-12 version 1.3.2 (sballar)
  // fixed bug in TaupToolkitWrapper where PKPbc
  // was not handled properly.
  //
  // 2010-11-05 version 1.3.1 (sballar)
  // when writing set of results to db, and an oracle
  // error happened, locoo was rolling back the entire
  // set of locations instead of just the one that
  // caused the error.
  //
  // 2010-10-26 version 1.3.0 (sballar)
  // topography now obtained from a GeoModel file that is
  // separate from any of the predictors.
  //
  // 2010-10-25 version 1.2.7 (sballar)
  // Fixed bug in IODB that allowed more than 1000 orids in
  // a single batch. This caused a sql failure when oracle
  // tried to execute 'where orid in (...)'
  //
  // 2010-10-03 version 1.2.6 (sballar)
  // Fixed bug that prevented calculation with lat, lon, depth fixed
  // (only origin time free).
  //
  // 2010-07-08 version 1.2.4 (sballar)
  // 1. regurgitate properties at beginning of output text file.
  // 2. sort observation/prediction tables by source-receiver distance
  // 3. fixed bug where origin time date was not in GMT timezone
  //
  // 2010-07-07 version 1.2.3 (sballar)
  // no changes from 1.2.2.
  //
  // 2010-07-06 version 1.2.2 (sballar)
  // fixed all the uncertainty related parameters to be
  // consistent across all the different predictors.
  //
  // 2010_07_02 version 1.2.1 (sballar)
  // fixed slbm uncertainty so that it will work with locoo3d
  //
  // 2010_06_30 (avencar)
  // added functionality for running in distributed mode
  // on the NRM cluster with JPPF
  //
  // 2010_03_04 version 1.1.0
  // ensured that if exception thrown in iodb output
  // it does not bring down the whole run.
  //
  // 2009_11_05 version 1.0.9
  // switched esaz and seaz in assoc table output
  // improved error handling.
  //
  // 2009_11_05 version 1.0.8 improved error handling.
  //
  // 2009-11-03 version 1.0.7 sent to lanl
  // added parameter dbOutputConstantOrid
  //

  private ScreenWriterOutput logger, errorlog;

  //private double maxExecTime = 0;

  static private Map<Long, List<Statistics>> statistics;

  /**
   * @param args
   */
  static public void main(String[] args) {
      try {
	  if (args.length == 0) {
	      StringBuffer buf = new StringBuffer();
	      System.out.print(String.format("LocOO version %s   %s%n%n", LocOO.getVersion(),
		      GMTFormat.localTime.format(new Date())));

	      buf.append("Must specify a property file as a command line argument.\n\n");
	      for (String arg : args)
		  buf.append(String.format("%s%n", arg));
	      throw new LocOOException(buf.toString());
	  }

	  for (String arg : args) {

	      if (!arg.toLowerCase().endsWith(".properties"))
		  arg += ".properties";

	      File propertyFile = new File(arg);
	      if (!propertyFile.exists())
		  throw new LocOOException(
			  "Property file " + propertyFile.getCanonicalPath() + " does not exist");

	      PropertiesPlusGMP properties = null;
	      properties = new PropertiesPlusGMP(propertyFile);

	      if (properties.containsKey("printStatistics")
		      && !properties.getProperty("printStatistics").equalsIgnoreCase("false"))
		  statistics = new HashMap<Long, List<Statistics>>();

	      LocOO_IO dio = new LocOO_IO(properties);
	      
	      (new LocOO()).run(properties, dio);
	      
	      dio.close();

	      if (statistics != null) {
		  String header = "orid\tnass\tndef\tnIter\tnFunc\tpredictionTime\tcalculationTime";
		  String p = properties.getProperty("printStatistics");
		  if (p.equalsIgnoreCase("true")) {
		      System.out.println(header);
		      for (List<Statistics> list : statistics.values())
			  for (Statistics s : list)
			      System.out.println(s);
		  } else {
		      FileWriter output = new FileWriter(p);
		      output.write(header + "\n");
		      for (List<Statistics> list : statistics.values())
			  for (Statistics s : list)
			      output.write(s.toString() + "\n");
		      output.close();

		  }
	      }
	  }


	  System.exit(0);
      } catch (Exception e) {
	  e.printStackTrace();
      }

  }

  private boolean slept(ParallelResult r, long millis) {
    if (r != null)
      return false;
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return true;
  }

  private synchronized void handleResult(LocOOTaskResult result, NativeOutput output,
	  int resultsCount, int tasks, ProfilerContent fppc) {

      // Output the log for each result
      try {
	  errorlog.write(result.getTaskErrorLog().getStringBuffer().toString());
	  if (result != null && result.getTaskLog() != null)
	      logger.write(result.getTaskLog().getStringBuffer().toString());
      } catch (Exception e1) {
	  e1.printStackTrace();
      }

      if(logger.getVerbosity() > 0)
	  for (Source source : result.getSources().values()) {
	   logger.writef("Finished processing orid= %d in %s with ndef= %d%n",
		  source.getSourceId(), Time.elapsedTime((long) (source.getCalculationTime() * 1000)), source.getNdef());
      }

      if (statistics != null) {
	  for (Source source : result.getSources().values()) {
	      if (source != null) {
		  Statistics s = new Statistics(source);
		  List<Statistics> list = statistics.get(s.orid);
		  if (list == null)
		      statistics.put(s.orid, list = new ArrayList<>());
		  list.add(s);
	      }
	  }
      }

      try {
	  // write the result to output db or files.
	  if (output != null)
	      synchronized (output) {
		  output.writeTaskResult(result);
	      }
	  if(logger.getVerbosity() > 0) 
	      logger.writeln("Wrote task result " + resultsCount + " of " + tasks + " to database.");
      } catch (Exception e) {
	  errorlog.writeln(e);
	  e.printStackTrace();
      }

      // add profile results if defined
      ProfilerContent pc = result.getProfilerContent();
      if (pc != null)
	  fppc.addProfilerContent(pc);
  }

  public void run(PropertiesPlusGMP properties, LocOO_IO dio) throws Exception {
    long startTime = System.currentTimeMillis();

    AtomicLong executionTime = new AtomicLong(0);
    
    this.logger = dio.getLogger();
    this.errorlog = dio.getErrorlog();
    
    int nSources = 0;
    
    VectorGeo.earthShape = EarthShape.valueOf(properties.getProperty("earthShape", "WGS84"));
    ParallelMode parallelMode = null;
    ParallelBroker parallelBroker = null;
    ExecutorService es = null;
    int concurrentParallelPredictorTasksMax = 8;
    try {
      concurrentParallelPredictorTasksMax = properties.getInt("parallelPredictorTaskThreads",8);
    } catch (PropertiesPlusException e3) {
      e3.printStackTrace();
    }
    if(parallelMode == ParallelMode.SEQUENTIAL) concurrentParallelPredictorTasksMax = 1;

    try {
	
      try {
        // see if the requested mode is supported by the current version of ParallelUtils.
        parallelMode = ParallelMode
            .valueOf(properties.getProperty("parallelMode", "sequential").toUpperCase());
      } catch (Exception e2) {
        throw new Exception("The version of ParallelUtils implemented in this version of LocOO3D "
            + "does not support parallelMode = "
            + properties.getProperty("parallelMode", "sequential"));
      }

      parallelBroker = ParallelBroker.create(parallelMode);
      parallelBroker.setProperties(properties);

      final ParallelBroker fpb = parallelBroker;

      if (logger.getVerbosity() > 0) {
        //TODO this whole parallelMode logic section probably needs to get refactored in terms of
        //whether splitSizeNdef is to be used:
        if (parallelMode == ParallelMode.SEQUENTIAL) {
          int procs = Runtime.getRuntime().availableProcessors();
          if(properties != null) {
            try{
              procs = properties.getInt("maxProcessors", procs);
            } catch (GMPException e) {
              e.printStackTrace();
            }
          }
          
          AtomicInteger i = new AtomicInteger(0);
          es = Executors.newFixedThreadPool(procs, r -> {
            Thread t = new Thread(r);
            t.setName(PredictorFactory.class.getSimpleName()+"-"+i.getAndIncrement());
            return t;
          });

          logger.writeln(
              "ParallelMode = sequential (locations computed in sequential mode, predictions in concurrent mode)");
        } else if (parallelMode == ParallelMode.DISTRIBUTED
            || parallelMode == ParallelMode.DISTRIBUTED_FABRIC) {
          es = parallelBroker.getExecutorService();
          logger.writeln(
              "ParallelMode = distributed (locations computed in distributed mode, predictions in mixed concurrent/sequential modes)");
        } else if (parallelMode == ParallelMode.CONCURRENT) {
          es = parallelBroker.getExecutorService();
          logger.writeln(
              "ParallelMode = concurrent (locations computed in conccurent mode, predictions in mixed concurrent/sequential mode)");
        } else
          throw new LocOOException(String.format(
              "parallelMode = %s but must be one of sequential, concurrent, distributed, or distributed_fabric.",
              parallelMode));

        if(logger.getVerbosity() > 0) logger.writeln("Using "
            + properties.getInt("maxProcessors", Runtime.getRuntime().availableProcessors())
            + " of " + Runtime.getRuntime().availableProcessors() + " available processors.");
      }

      // a 2D ragged array of longs. The first index spans the batches and the
      // second spans the orids|sourceids in each batch. Each batch represents one or more
      // sources to be located such that each batch has approximately the same number
      // of defining observations in it. Earlier batches will have fewer,larger, sources
      // per batch and later batches will have more, smaller, sources per batch.
      ArrayList<ArrayListLong> srcIdLists = dio.getDataInput().readTaskSourceIds();

      if (logger.getVerbosity() > 0)
        logger.writeln("Number of batches to submit: " + srcIdLists.size());

      nSources = 0;
      for (ArrayListLong batch : srcIdLists)
        nSources += batch.size();

      if (logger.getVerbosity() > 0)
        logger.writeln("Total number of events to process: " + nSources);

      if ((nSources > 1000 && logger.getVerbosity() > 1)
          || (nSources > 50 && logger.getVerbosity() > 2))
        logger.writeln("\n\n\n!!! WARNING !!!\n\n"
            + "Running LocOO with a large number of sources and high verbosity is prone to\n"
            + "OutOfMemoryErrors! Recommend setting io_verbosity to less than 2.");

      // index over all the batches
      int index = 0;

      // in the while loop that follows, this is the number of batches that will be submitted to
      // the broker before any are retrieved from the broker.
      // TODO update documentation in LocOO user manual
      int procs = Math.max(Runtime.getRuntime().availableProcessors(),
          parallelBroker.getProcessorCountEstimate());
      int queueSizeMax = properties.getInt("queueSizeMax", 5 * procs);

      // This number determines how many tasks will be queued at the client before submission.
      // TODO update documentation in LocOO user manual
      int tasksPerBatch =
          properties.getInt("tasksPerBatch", Math.max(1, parallelBroker.getHostCount() / 2));
      int maxBatches = queueSizeMax / tasksPerBatch + (queueSizeMax % tasksPerBatch > 0 ? 1 : 0);

      // If tasks have larger NDEF than this number, perform their location computations in
      // sequential mode but handle the predictions in parallel mode (concurrent or distributed):
      // TODO update documentation in LocOO user manual
      int splitSizeNdef = properties.getInt("splitSizeNdef", 5000);

      parallelBroker.setBatchSize(tasksPerBatch);
      parallelBroker.setMaxBatches(maxBatches);

      if (logger.getVerbosity() > 0)
        logger.writeln("Parallel broker preferred max queue size: " + queueSizeMax
            + " maxBatches = " + maxBatches + ", tasksPerBatch = " + tasksPerBatch);

      ProfilerContent predProfilerContent = null;
      long profilerSamplePeriod = properties.getInt("profilerSamplePeriod", -1);
      if (profilerSamplePeriod > 0)
        predProfilerContent = new ProfilerContent();


      // Start the ParallelBroker results-gathering thread first:
      final ProfilerContent fppc = predProfilerContent;
      final AtomicInteger resultsCount = new AtomicInteger(0);
      final NativeOutput fdlo = dio.getDataOutput();

      // This thread handles all results that were submitted directly to the ParallelBroker:
      String name = "Sequential-Predictor-Results-Thread";
      Thread resThread = new Thread(() -> {
        while (resultsCount.get() < srcIdLists.size()) {
          long t0 = System.currentTimeMillis();

          ParallelResult r = null;
          do {
            r = fpb.getResultWait();
          } while (slept(r, 100));

          if (r.getException() != null) {
            errorlog.writeln(r.getException());
            errorlog.writeln("Error occurred at Task on host \"" + r.getHostName() + "\", "
                + "LocOO3D shutting down.");
            System.exit(0);
          }

          executionTime.addAndGet(System.currentTimeMillis() - t0);
          LocOOTaskResult result = (LocOOTaskResult) r;


          // add profile results if defined
          ProfilerContent pc = result.getProfilerContent();
          if (pc != null)
            fppc.addProfilerContent(pc);
          resultsCount.incrementAndGet();

          executionTime.addAndGet(System.currentTimeMillis() - t0);
          handleResult(result, fdlo, resultsCount.get(), srcIdLists.size(), fppc);
        }
      }, name);

      //Ensures the biggest tasks are preferentially executed in descending NDEF order:
      Comparator<LocOOTask> c = Comparator.comparingInt(LocOOTask::getTotalNDef).reversed();
      AtomicBoolean doneSubmittingHugeTasks = new AtomicBoolean(false);
      BlockingQueue<LocOOTask> hugeTasks = new PriorityBlockingQueue<LocOOTask>(16,c);
      List<Thread> hugeTaskThreads = new LinkedList<>();

      // Queue up tasks for submission, then wait on the results thread for completion:
      int submitted = 0;
      for (int i = 0; i < srcIdLists.size(); i++) {
        LocOOTask task = dio.getDataInput().readTaskObservations(srcIdLists.get(index++));

        if (task.getOriginCount() == 1 && task.getTotalNDef() >= splitSizeNdef) {
          if(logger.getVerbosity() > 0) 
            logger.writeln("Found large task, ndef = " + task.getTotalNDef());
          hugeTasks.add(task);
          submitted++;

          // This thread calls [LocOOTask instance].run(), which defers to PredictorFactory for
          // parallel execution of that task's predictions, either in concurrent or distributed
          // modes (recall how we configured PredictorFactory's global executor service instance
          // in the parallelMode if/else statements near the top of main()):
          if (hugeTaskThreads.size() < concurrentParallelPredictorTasksMax) {
            final ExecutorService s = es;
            String hname = "Parallel-Predictor-Results-Thread-"+(hugeTaskThreads.size()+1);
            Thread hugeTaskThread = new Thread(() -> {
              while (!doneSubmittingHugeTasks.get() || !hugeTasks.isEmpty()) {
                try {
                  LocOOTask t = hugeTasks.poll(1, TimeUnit.SECONDS);
                  if (t != null) {
                    int rId = resultsCount.incrementAndGet();
                    long orid = t.getSources().iterator().next().getSourceId();
                    t.setPredictionsThreadPool(s);
                    long time = System.currentTimeMillis();
                    t.run(); // Performs predictions in parallel if s != null
                    time = System.currentTimeMillis()-time;
                    if(logger.getVerbosity() > 0) logger.writeln(
                        "Completed large ndef task, orid = " + orid+", time = "+time/1000.+", "+
                            hugeTasks.size()+" remain.");
                    handleResult(t.getResultObject(), fdlo, rId, srcIdLists.size(),fppc);
                  }
                } catch (InterruptedException e) {
                  logger.write(e);
                }
              }
              if(logger.getVerbosity() > 0) logger.writeln(hname + " returning.");
            }, hname);
            if(logger.getVerbosity() > 0) logger.writeln("Starting " + hname + " ...");
            hugeTaskThreads.add(hugeTaskThread);
            hugeTaskThread.start();
          }
        } else {
          if (!resThread.isAlive()) {
            if(logger.getVerbosity() > 0) logger.writeln("Starting " + name + " ...");
            resThread.start();
          }

          parallelBroker.submitBatched(task); // Computes predictions sequentially on different
                                              // thread
          submitted++;

          if ((i + 1) % 100 == 0 && logger.getVerbosity() > 0)
            logger.writeln("Tasks submitted: " + (i + 1) + " of " + srcIdLists.size());
        }
      }
      doneSubmittingHugeTasks.set(true);
      parallelBroker.purgeBatch();
      
      if(logger.getVerbosity() > 0) logger.writeln(
          "All tasks submitted (" + submitted + "), waiting for results thread to complete.");

      // Wait for both results threads to complete:
      List<Thread> allThreads = new LinkedList<>(hugeTaskThreads);
      allThreads.add(resThread);
      for (Thread t : allThreads)
        if (t != null && t.isAlive())
          t.join();

      if (logger.getVerbosity() > 0)
        logger.writeln("All results received.");

      if (logger.getVerbosity() > 0)
        logger.writeln("Data loader closed.");
    } catch (Exception ex) {
      ex.printStackTrace();
      if (errorlog != null)
        errorlog.writeln(ex);
      else
        ex.printStackTrace();
    } finally {
      if (parallelBroker != null) {
        try {
          parallelBroker.close();
          parallelBroker = null;
        } catch (Exception e) {
          logger.write(e);
        }
      }

      // if(ParallelMode.SEQUENTIAL == parallelMode) {
      try {
        if (es != null) {
          es.shutdown();
        }
      } catch (Exception e) {
        logger.write(e);
      }

    }

    if (logger.getVerbosity() > 0) {

      logger.writeln();
      logger.writeln();
      logger.writeln(properties.getRequestedPropertiesString(true));

      logger.write(String.format("Time = %s%nElapsed time = %s%n",
          GMTFormat.localTime.format(new Date()), Globals.elapsedTime(startTime)));
      if (nSources > 0)
        logger.write(
            String.format("Execution time (sec) = %1.3f%nExecution time (sec/source) = %1.3f%n",
                executionTime.get() * 1e-3, executionTime.get() * 1e-3 / nSources));
      else
        logger.write(String.format("Execution time (sec) = %1.3f%nnSources = 0%n",
            executionTime.get() * 1e-3));

      logger.write(String.format("Done.%n"));
      
    }
  }

  static public LocOOTaskResult locate(LocOOTask locooTask) {
    ParallelBroker parallelBroker = null;
    LocOOTaskResult result = null;

    parallelBroker =
        ParallelBroker.create(locooTask.getProperties().getProperty("parallelMode", "sequential"));

    parallelBroker.submit(locooTask);

    // wait for a result to be ready from the broker.
    result = (LocOOTaskResult) parallelBroker.getResultWait();
    parallelBroker.close();
    return result;
  }

  public LocOOTaskResult locateNonStatic(LocOOTask locooTask) {
    locooTask.run();
    return locooTask.getResultObject();
  }

  class Statistics {
    long orid;
    long nass;
    long ndef;
    int nIterations;
    int nFunc;
    double calcTime;
    double predictionTime;

    public Statistics(Source r) {
      this.orid = r.getSourceId();
      this.nass = r.getNass();
      this.ndef = r.getNdef();
      this.nIterations = r.getNIterations();
      this.nFunc = r.getNFunc();
      this.calcTime = r.getCalculationTime();
      this.predictionTime = r.getPredictionTime();
    }

    @Override
    public String toString() {
      return String.format("%d\t%d\t%d\t%d\t%d\t%1.6f\t%1.6f", orid, nass, ndef, nIterations, nFunc,
          predictionTime, calcTime);
    }
  }

}
