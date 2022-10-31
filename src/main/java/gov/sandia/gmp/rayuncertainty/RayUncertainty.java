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
package gov.sandia.gmp.rayuncertainty;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.geotess.PointMap;
import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.globals.WaveType;
import gov.sandia.gmp.baseobjects.interfaces.ModelInterface;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.bender.Bender;
import gov.sandia.gmp.geotessgmp.GeoTessModelGMP;
//import gov.sandia.gmp.geotessgmp.LibCorr3DModelGMP;
import gov.sandia.gmp.observationprediction.PredictorObservation;
import gov.sandia.gmp.observationprediction.PredictorParallelTask;
import gov.sandia.gmp.observationprediction.PredictorParallelTaskResult;
import gov.sandia.gmp.parallelutils.ParallelBroker;
import gov.sandia.gmp.parallelutils.ParallelBrokerConcurrent;
import gov.sandia.gmp.parallelutils.ParallelResult;
import gov.sandia.gmp.rayuncertainty.basecontainers.Observation;
import gov.sandia.gmp.rayuncertainty.containers.FinalVariancesPhaseSiteMap;
import gov.sandia.gmp.rayuncertainty.containers.FinalVariancesPhaseSiteSiteMap;
import gov.sandia.gmp.rayuncertainty.containers.FinalVariancesSourceMap;
import gov.sandia.gmp.rayuncertainty.containers.ObservationsPhaseSiteMap;
import gov.sandia.gmp.rayuncertainty.containers.ObservationsSourceMap;
import gov.sandia.gmp.rayuncertainty.containers.RayWeightSet;
import gov.sandia.gmp.rayuncertainty.containers.RayWeightSetList;
import gov.sandia.gmp.rayuncertainty.debugray.DebugObservation;
import gov.sandia.gmp.rayuncertainty.debugray.DebugRayOutput;
import gov.sandia.gmp.rayuncertainty.debugray.DebugRayOutputEntry;
import gov.sandia.gmp.rayuncertainty.debugray.DebugResultsPhaseSiteMap;
import gov.sandia.gmp.rayuncertainty.debugray.DebugResultsPhaseSiteSiteMap;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerInteger;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerKey;
import gov.sandia.gmp.util.containers.hash.maps.HashMapLongKey;
import gov.sandia.gmp.util.containers.hash.sets.HashSetLong;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.FileDirHandler;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.gui.Utility;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.matrixblock.MatrixBlockDefinition;
import gov.sandia.gmp.util.numerical.matrixblock.MatrixBlockFileServer;
import gov.sandia.gmp.util.numerical.polygon.Polygon3D;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.profiler.ProfilerContent;
import gov.sandia.gmp.util.progress.CliProgress;
import gov.sandia.gmp.util.progress.Progress;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;
import gov.sandia.gmp.util.statistics.Statistic;
//import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * Calculates ray uncertainty given a phase specific velocity model
 * (tomographic) stored on a GeoModel, a set of sites and/or site pairs for
 * which site variance or covariance will be calculated, and a set of source
 * positions for which rays will be calculated between site and source using
 * the tomography velocity model.
 * <p>
 * The source definition can be one of 4 types including a Database, a
 * GeoModels active nodes, a GeoTessModels active nodes, or a defined set of
 * sources provided in the properties file ([id] latitude, longitude, depth)
 * The site list can come from a database (must be a database if the source
 * prescription is a database) or a GMP network object (AftacNetwork,
 * GenericNetwork, IMSNetwork are supported).
 * <p>
 * The actual rays are traced from tomographic nodes influencing each requested
 * source to tomographic nodes influencing each requested receiver. The results
 * from each influence ray are interpolated onto the requested receiver from each
 * influence source position, and then each interpolated influence source
 * position to the requested receiver position is interpolated again onto the
 * requested source position to obtain the final result.
 * <p>
 * The ray weight ensemble from all influence rays in the ray calculation are
 * used to pre- and post-multiply the covariance matrix (generated from the
 * tomography output in LSINV) to obtain model covariance estimates for a ray
 * or ray pair emanating from the same source (site A variance =
 * <p>
 * W(A)^T * COV * W(A) ... site AB covariance = W(A)^T * COV * W(B))
 * <p>
 * The calculation proceeds in 4 distinct calculation steps of which the first
 * and last are performed here by the client. The first is initialization where
 * all influence ray observations are constructed in preparation for performing
 * ray prediction in the 2nd step. The last is where all influence ray
 * covariance values, determined in the 3rd step where ray uncertainty is
 * evaluated, are interpolated onto the requested source/receiver positions to
 * give the final result. The majority of the work is performed in the middle
 * two steps. Each step can be run separately or in sequence. The steps
 * include:
 * <p>
 * Ray Prediction: Rays are calculated (by Bender) between the set of valid
 * sources to each site and the ray weights are returned and
 * written to a set of covariance block row segmented files
 * for each site. The files serve to break the problem down
 * into weight definitions that occur only in specific
 * blocks. This organizes the data readily for performing
 * ray uncertainty and minimizes the amount of data that
 * must be read in a RayUncertaintyTask.
 * Ray Uncertainty: Calculates the contribution to total ray variance or (ray
 * A to ray B covariance) from a single block. This is done
 * for many rays from possibly many phase/sites at one time.
 * Both represented and non-represented variance is
 * evaluated.
 * <p>
 * Once we have all influence ray uncertainties determined the final requested
 * rays are interpolated from the influence ray results here in the client in
 * the last step (4) as mentioned above.
 * <p>
 * The results are written to an output file if the input sources came from
 * the database or the input properties file. If the input source definitions
 * were from a GeoModel or GeoTessModel then the results are written back into
 * the defining models as attributes.
 * <p>
 * Debug output for any requested rays are written to a debug file (one for each
 * requested debug ray) and path information is written for each influence ray
 * that influences the requested source/receiver result. Debug rays are
 * specified in the input properties file using semi-colon (;) separated entries
 * for the property "debugRays = ".
 * <p>
 * In the properties file set
 * <p>
 * debugRay = [SourceModel1] [Phase1] SourceId1 SiteA1 SiteB1; \
 * [SourceModel2] [Phase2] SourceId2 SiteA2 SiteB2; ...
 * <p>
 * If sources are extracted from the properties file, the database, or a single
 * source model then [SourceModel] is not required. Otherwise, the base name
 * of the source model is necessary. If only one phase is solved for then the
 * [Phase] designation is not required.
 * <p>
 * All specified debug rays data are written to their own directory underneath
 * the primary debug directory specified by the property "debugPath = ". If not
 * specified then the default relative directory "/debug" is used. Underneath
 * this directory each debug ray is defined under a sub-directory with the name
 * "/debugRay_SiteA_SourceId", for type AA rays and "/debugRay_SiteA_SiteB_SourceId",
 * for type AB rays. Under these directories the debug output file "debugOut.txt"
 * is written as-well-as any ray path files for all influencing rays that effect
 * the requested debug rays outcome. The ray path files are saved under the name
 * "/debugRayPath_Site_SourceId.txt", for ascii, and "/debugRayPath_Site_SourceId"
 * for binary.
 *
 * @author jrhipp
 */
public class RayUncertainty {

	static public String getVersion() {
		return Utils.getVersion("ray-uncertainty");
	}

    //************** Inner Class Definitions ************************************

    /**
     * Contains the 2 possible solution phases for ray uncertainty which include:
     * PREDICTION:      Read observations from database or reads GeoModel
     * source definition and site list to form observations.
     * Then calculates ray weights and outputs ray weight
     * files for each block of the covariance matrix. A map
     * of phase -> site -> source -> observation is
     * maintained. The map contains the list of observations
     * and their associated ray index along with an array of
     * block sub-divided ray weights. This map is used to
     * build the RayUncertaintyTask objects in the next
     * calculation phase.
     * RAY_UNCERTAINTY: Forms tasks for each block with all or some of the
     * phase -> site ray elements defined for that block.
     * These tasks read the partial ray weight files for
     * exactly the block indices (row and column) that are
     * needed to multiply the covariance block. The partial
     * variances produced by multiplying the block row /
     * column weights times the covariance matrix block
     * elements are returned to the client where a global
     * site partial variance map increments each of the task
     * results to form the final solution. The resulting
     * represented and non-represented partial variances are
     * combined to produce the final ray uncertainty which is
     * written to a text file, if the initial observations
     * were read from the database or properties file in
     * phase 1 (prediction), or they are set into the
     * GeoModelSource object, if it was used to define the
     * ray observations, which is subsequently written to
     * disk.
     *
     * @author jrhipp
     */
    public enum SolutionPhase {
        PREDICTION,
        RAY_UNCERTAINTY;
    }

    /**
     * Defines one of the three possible source definition inputs.
     *
     * @author jrhipp
     */
    public enum SourceDefinition {
        DATABASE,
        GEOMODEL,
        PROPERTIESFILE,
        GEOTESSMODEL,
        LIBCORR3D;
    }

    /**
     * A runnable designed to retrieve all returned tasks immediately upon
     * return form the ParallelBroker object so that their time stamp can
     * be set. This ensures there is no bias in the parallel transfer
     * overhead time estimate. This class simply starts running in a new
     * thread and continually updates a list of returned task results
     * (ObservationPrediction or RayUncertaintyTaskResults). The list is updated
     * by polling the ParallelBroker and returning any task results that it may
     * have, time stamping the result, and storing it in the list. This thread
     * continues until shutdown by the client after all tasks have been
     * processed and received.
     *
     * @author jrhipp
     */
    private class GetTaskResults<T extends ParallelResult> implements Runnable {
      /**
       * The list of all returned results from the ParallelBroker.
       */
      private BlockingQueue<T> returnedResults = new LinkedBlockingQueue<>();

      /**
       * The run condition. Defaults to true until stopped by the client using
       * the function stop().
       */
      private volatile boolean runCondition = true;

      /**
       * Default constructor. Creates a new thread and starts it.
       */
      public GetTaskResults() {
          new Thread(this, "GetResults").start();
      }

      /**
       * The run method called by the new thread. It simply polls the
       * ParallelBroker to see if any new task results have been
       * returned. If any are found they are time stamped and added to
       * the internal list (returnedResults).
       */
      @Override
      public void run() {
          // clear the list and enter the perpetual while loop ... this loop
          // exits when the boolean runCondition is set to false by calling
          // function stop() below.

          while (runCondition) {
              // see if a new task result is available

              @SuppressWarnings("unchecked")
              T tskrslt = (T) aParallelBrkr.getResult();

              if (tskrslt != null) {
                  // found task result ... time stamp it and add it to the list

                  tskrslt.setTaskReturnTime(System.currentTimeMillis());

                  returnedResults.add(tskrslt);
              }
          }
      }

      /** Stop this thread. */
      public void stop() { runCondition = false; }

      /**
       * Get the next available returned task result. Returns null if not tasks
       * are present.
       *
       * @return The next available returned task result.
       */
      public T getNextResult() throws InterruptedException{
          return returnedResults.poll(1,TimeUnit.SECONDS);
      }
    }


    /**
     * Used in solveRayUncertainty() and the Smoother() object to track
     * GeoAttribute indexes that are assigned to phase, receiver pairs. These are
     * recovered during the interpolation and smoothing phases for use in those
     * algorithms.
     * <p>
     * This object holds the seismic phase, receiver A, and receiver B objects
     * that define a surface entry and a map of all defined GeoAttributes
     * associated with their attribute index value in an AttributeIndexer object.
     *
     * @author jrhipp
     */
    public class SiteAttributeMap {
        /**
         * The seismic phase associated with this attribute map.
         */
        public SeismicPhase aPhase = null;

        /**
         * The Receiver A associated with this attribute map.
         */
        public Receiver aRcvrA = null;

        /**
         * The Receiver B associated with this attribute map (May be the same as
         * aRcvrA for type AA rays).
         */
        public Receiver aRcvrB = null;

        /**
         * The attribute map that associated each attribute with its index in an
         * AttributeIndexer
         */
        public HashMap<GeoAttributes, Integer> aAttrbMap = null;

        /**
         * Standard constructor.
         *
         * @param sp The associated seismic phase.
         * @param rA The associated A receiver.
         * @param rB The associated B receiver.
         */
        public SiteAttributeMap(SeismicPhase sp, Receiver rA, Receiver rB) {
            aPhase = sp;
            aRcvrA = rA;
            aRcvrB = rB;
            aAttrbMap = new HashMap<GeoAttributes, Integer>();
        }

        /**
         * Returns the attribute index associated with the input GeoAttribute or
         * null if it is not defined.
         *
         * @param ga The GeoAttribute for which the corresponding attribute index
         *           will be returned.
         * @return The attribute index associated with the input GeoAttribute or
         * null if it is not defined.
         */
        public Integer getAttributeIndex(GeoAttributes ga) {
            return aAttrbMap.get(ga);
        }

        /**
         * Adds the input GeoAttribute to the internal GeoAttribute map associated
         * with the input index.
         *
         * @param ga    The GeoAttribute to be associated with the input attribute
         *              index.
         * @param index The index associated with the input GeoAttribute.
         */
        public void putAttributeIndex(GeoAttributes ga, int index) {
            aAttrbMap.put(ga, index);
        }

        /**
         * Adds the GeoAttribute ga into the input attribute indexer and its index
         * in the attribute indexer into the SiteAttributeMap's attribute map.
         *
         * @param phase The phase of the attribute surface.
         * @param rA    The receiver A of the attribute surface.
         * @param rB    The receiver B of the attribute surface.
         * @param ai    The AttributeIndexer into which the GeoAttribute entry is
         *              added.
         * @param ga    The GeoAttribute to be added.
         * @throws IOException
         */
        private void setAttribute(SeismicPhase phase, Receiver rA, Receiver rB,
                                  AttributeIndexerSmart ai, GeoAttributes ga)
                throws IOException {
            putAttributeIndex(ga, ai.size());
            ai.addEntry(rA, rB, phase, ga);
        }

        /**
         * Adds the input SiteAttributeMap into the input attribute index entries
         * attribute map list and sets the source GeoModel attribute indexer with all
         * applicable GeoAttribute index settings.
         *
         * @param phase   The phase associated with the input
         *                SiteAttributeMap.
         * @param rA      The receiver A associated with the input
         *                SiteAttributeMap.
         * @param rB      The receiver B associated with the input
         *                SiteAttributeMap.
         * @param samList The new SiteAttributeMap.
         * @param ai      A models AttributeIndex containers.
         * @throws IOException
         */
        public void addAttribute(SeismicPhase phase, Receiver rA, Receiver rB,
                                 ArrayList<SiteAttributeMap> samList,
                                 AttributeIndexerSmart ai, boolean[] smoothFlag)
                throws IOException {
            // add SiteAttributeMap to map list and update all pertinent GeoAttributes

            samList.add(this);

            // set all pertinent non-smoothed attribute indexes into the
            // AttributeIndexer and the input SiteAttributeMap

            setAttribute(phase, rA, rB, ai,
                    GeoAttributes.TT_MODEL_VARIANCE);
            setAttribute(phase, rA, rB, ai,
                    GeoAttributes.TT_MODEL_VARIANCE_DIAGONAL);
            setAttribute(phase, rA, rB, ai,
                    GeoAttributes.TT_MODEL_VARIANCE_OFFDIAGONAL);
            setAttribute(phase, rA, rB, ai,
                    GeoAttributes.TT_MODEL_VARIANCE_NONREPRESENTED);
            setAttribute(phase, rA, rB, ai,
                    GeoAttributes.TT_MODEL_VARIANCE_REPRESENTED_FRACTION);

            // set all requested smoothing attribute indexes into the AttributeIndexer
            // and the associated SiteAttributeMap

            if (smoothFlag[0] || smoothFlag[1] ||
                    smoothFlag[2])
                setAttribute(phase, rA, rB, ai,
                        GeoAttributes.TT_MODEL_VARIANCE_SMOOTHED);
            if (smoothFlag[0])
                setAttribute(phase, rA, rB, ai,
                        GeoAttributes.TT_MODEL_VARIANCE_DIAGONAL_SMOOTHED);
            if (smoothFlag[1])
                setAttribute(phase, rA, rB, ai,
                        GeoAttributes.TT_MODEL_VARIANCE_OFFDIAGONAL_SMOOTHED);
            if (smoothFlag[2])
                setAttribute(phase, rA, rB, ai,
                        GeoAttributes.TT_MODEL_VARIANCE_NONREPRESENTED_SMOOTHED);
        }
    }
    
    //************** Class Attributes Definition *********************************

    /**
     * A list of all ray weight sets written to disk for each block row. These
     * sets are read and processed by the ray uncertainty tasks to produce the
     * partial variance from each block which are summed on return from the task
     * by the client and output as final result.
     */
    private RayWeightSetList aRayWeightSetList = null;

    /**
     * The maximum number of RayWeightSets written to disk (excluding the pair
     * RayWeightSet if it was written).
     */
    private int aMaxRayWeightSetCount = -1;

    /**
     * The phase with which the current instantiation of RayUncertainty begins.
     * If aStartPhase == SolutionPhase.PREDICTION then a new IO directory
     * (aIODirectory) is created for this calculation. Otherwise, the input
     * IO directory is expected to exist. If ray uncertainty is the start phase
     * the out.txt file is written to directory run_RAY_UNCERTAINTY, all other
     * outputs remain in the main IO directory.
     */
    private SolutionPhase aStartPhase =
            SolutionPhase.PREDICTION;

    /**
     * The phase with which the current instantiation of RayUncertainty ends.
     * Note: aEndPhase.ordinal() must be >= aStartPhase.ordinal(). This allows
     * for three valid combinations of start/end phase including:
     * <p>
     * aStartPhase            aEndPhase
     * <p>
     * PREDICTION             PREDICTION
     * PREDICTION             RAY_UNCERTAINTY
     * RAY_UNCERTAINTY        RAY_UNCERTAINTY
     */
    private SolutionPhase aEndPhase =
            SolutionPhase.RAY_UNCERTAINTY;

    /**
     * The phase / site / source / observation data map that maps every input
     * observation. This object also maintains a block list of all ray weights
     * decomposed into each block within which they reside. These are filled
     * when a ray is predicted and written to disk when the RayWeightSet that
     * holds them is complete.
     */
    private ObservationsPhaseSiteMap aInputObservationMap = null;

    /**
     * A reference to the GUI used by RayUncertainty.
     */
    private RayUncertaintyUI aUI = null;

    /**
     * Output buffered writer used to write uncertainty output when the source
     * definition is DATABASE or PROPERTIESFILE.
     */
    private BufferedWriter aUncOut = null;

    /**
     * The source definition prescription. Valid types are "Database",
     * "GeoModel", or "PropertiesFile".
     */
    private SourceDefinition aSourceDefinition =
            SourceDefinition.GEOMODEL;

    /**
     * The source path from which sources are read. This can be "NA", if accessed
     * from a database, the GeoModelSource path, if sources come from a GeoModel,
     * or the properties file path, if sources were read from the properties file.
     */
    private String aSourcePath = "";

    /**
     * The set of properties from which the input values are read.
     */
    private PropertiesPlusGMP aProps = null;

    /**
     * Bender Properties used to perform travel time predictions.
     */
    private PropertiesPlusGMP aBenderProps = null;

    /**
     * Output object used to write to the screen or a BufferedWriter log file.
     */
    private ScreenWriterOutput aScrnWrtr = null;

    /**
     * The parallel broker used to excecute each task in a sequential, concurrent,
     * or distributed manner.
     */
    private ParallelBroker aParallelBrkr = null;

    /**
     * The directory where all output is written. This directory is created
     * automatically and populated with all results
     */
    private String aIODirectory = "";

    /**
     * The start time of the Ray Uncertainty process.
     */
    private long aStartTime = -1;

    /**
     * Tomography GeoModel read path.
     */
    private String aGeoModelTomoPath = "";

    /**
     * Tomography GeoModel read file name.
     */
    private String aGeoModelTomoFileName = "";

    /**
     * Tomographic GeoModel.
     */
    protected GeoTessModelGMP aGeoModelTomo = null;

    /**
     * The global observation list that contains all observations that are
     * processed.
     */
    private ArrayList<Observation> aObservationList = null;

    /**
     * List of all seismic phases to be processed.
     */
    private ArrayList<SeismicPhase> aPhaseList = null;

    /**
     * Unique list of sources from the input source GeoModel, GeoTessModel,
     * properties file, or database source input. For multiple GeoModel or
     * GeoTessModel input this source list is changed for each consecutive
     * model input.
     */
    private ArrayList<Source> aSourceList = null;

    /**
     * The total number of model input files read.
     */
    private int aModelSourceCount = 0;

    /**
     * Map of all source ids associated with their sources (cleared and modified
     * each time a new source model is input).
     */
    private HashMap<Long, Source> aSourceMap = null;

    /**
     * Unique list of sites from the database or input site prescription property
     * to be processed.
     */
    private ArrayList<Receiver> aReceiverList = null;

    /**
     * Map of receiver ids associated with their receiver.
     */
    private HashMap<Long, Receiver> aReceiverMap = null;

    /**
     * List of all site pairs to be processed for covariance. Each entry must
     * also exist in the site list (aSiteList).
     */
    HashMap<Receiver, Receiver> aSitePair = null;

    /**
     * List of all site influence point pairs to be processed for covariance. Each
     * entry must also exist in the receiver map (aRcvrXMap).
     */
    HashMap<Receiver, HashSet<Receiver>> aSitePairX = null;

    /**
     * Path to the covariance matrix active node map.
     */
    private String aCovMtrxActvNodeMapPath = "";

    /**
     * File name of the covariance matrix active node map.
     */
    private String aCovMtrxActvNodeMapName = "";

    /**
     * The non-represented active node variance file name.
     */
    private String aNonRepActvNodeVarFileName = "";

    /**
     * Map of all ActiveNodes in aGeoModelTomo mapped to the covariance
     * matrix column index. Active nodes not represented in the matrix will have
     * a column index that exceeds the matrix element size.
     */
    //1TODO PS: add aActiveNodeMapP and aActiveNodeMapS and remove the entry below
    private HashMapIntegerInteger aActiveNodeMap = null;

    /**
     * The represented node count in the sparse matrix (excludes any site terms).
     */
    // TODO PS: add aRepNodeCountP and aRepNodeCountS and remove the entry below
    private int aRepNodeCount = -1;

    /**
     * List of file servers containing the covariance matrix.
     */
    private MatrixBlockFileServer aCovMatrixServers = null;

    /**
     * The path to where all ray weight files are written.
     */
    private String aRayWghtPath = "";

    /**
     * The path to where all output source models are written if the input source
     * definition is GEOMODEL or GEOTESSMODEL.
     */
    private String aSourceModelOutputPath = "";

//  /**
//   * The bender Snell's law search method (SIMPLEX or BRENTS or AUTO).
//   */
//  private SearchMethod                         aBenderSearchMethod       =
//  		                                         SearchMethod.AUTO;

    /**
     * Covariance matrix block information object.
     */
    private MatrixBlockDefinition aCovMatrixBlockDefn = null;

    /**
     * Set to true if node task information is to be output.
     */
    private boolean aNodeOutputTaskInfoFlag = false;

    /**
     * The phase/site/src map of final variance values stored in a 3-element
     * array where index 0 is diagonal variance, index 1 is off-diagonal variance,
     * and index 2 is non-represented variance.
     */
    private FinalVariancesPhaseSiteMap aFinalVarMapAA = null;

    /**
     * The phase/siteA/siteB/src map of final site to site covariance stored in
     * a 3-element array where index 0 is diagonal variance, index 1 is off-
     * diagonal variance, and index 2 is non-represented variance.
     */
    private FinalVariancesPhaseSiteSiteMap aFinalVarMapAB = null;

    /**
     * Used to default to the users definition of the default input source file
     * name. The default can be changed to a const string or an elaborated string
     * with additional text before and after the "*". The "*" is replaced with
     * all receiver names declared in the input source file designation.
     */
    private String aDefaultSourceFileName = "*";

    /**
     * Contains the associated source file for every defined receiver (i.e.
     * associates a specific receiver with the specific source file from which
     * rays are constructed). More than one receiver may reference the same
     * source file (Map<receiver id, source file name>).
     */
    private HashMap<Long, String> aRcvrSourceFileMap = null;

    /**
     * Contains a map of each uniquely specified source file associated with the
     * set of all receivers that use it to define rays for which uncertainty will
     * be evaluated (Map<Unique File Name, Set<receiver id>>). This is the inverse
     * map for aRcvrSourceFileMap. This map is built during observation
     * construction and used during influence ray to requested ray uncertainty
     * interpolation to avoid reading each source model more than once. The source
     * model is reread so that the interpolated uncertainties can be set into it
     * as a result before rewriting it back to disk.
     */
    private HashMap<String, HashSet<Long>> aUniqueSourceFileRcvrMap = null;

    /**
     * Contains the map of each unique file name with each of the receivers for
     * which it supports source definition (aUniqueSourceFileRcvrMap) and for
     * that unique file name -> receiver id association defines the set of all
     * source ids that were used to construct observations for which ray
     * uncertainty will be calculated (Map<Unique File Name, Map<receiver id,
     * Set<source id>>>). This map is built during observation construction and
     * used during influence ray to requested ray uncertainty interpolation to
     * avoid rereading the model sources again.
     */
    private HashMap<String, HashMapLongKey<HashSetLong>> aModelRcvrSrcMap = null;

    /**
     * Maps all defined influence receivers with their receiver id. Note that the
     * receiver id is always defined as the tomography grid active node index for
     * which the influence receiver was created. Any tomography grid node that
     * influences (non-zero interpolation weight) a requested receiver position
     * will be added to this map. This map contains all defined influence receivers
     * for all input source files read during initialization.
     */
    private HashMap<Integer, Receiver> aRcvrXMap = null;

    /**
     * Maps all defined influence sources with their source id. Note that the
     * source id is always defined as the tomography grid active node index for
     * which the influence source was created. Any tomography grid node that
     * influences (non-zero interpolation weight) a requested source position
     * will be added to this map. This map contains all defined influence sources
     * for all input source files read during initialization.
     */
    private HashMap<Integer, Source> aSrcXMap = null;
    
    private boolean killBrokerAfterSolve = false;

    /**
     * Used to hold a specific ModelInterface AttributeIndexer and associated
     * SiteAttributeMap entries. The SiteAttributeMap entries are used for
     * final interpolation and post-smoothing operations. The AttributeIndexer is
     * set into the model at the point of variance/covariance interpolation to
     * populate the model before it is written to disk. The indexer and
     * SiteAttributeMap entries are built during model/source discovery.
     *
     * @author jrhipp
     */
    class AttributeIndexEntries {
        public AttributeIndexerSmart aIndexer = null;
        public ArrayList<SiteAttributeMap> aMapList = null;
        public HashMap<SeismicPhase, HashMap<Receiver, HashMap<Receiver,
                SiteAttributeMap>>> aSurfMap = null;

        public AttributeIndexEntries() {
            aIndexer = new AttributeIndexerSmart();
            aMapList = new ArrayList<SiteAttributeMap>();
            aSurfMap = new HashMap<SeismicPhase, HashMap<Receiver,
                    HashMap<Receiver, SiteAttributeMap>>>();
        }
    }

    /**
     * The source model specific AttributeIndexEntries object. One of these is
     * added to the map for each unique source model file read.
     */
    private HashMap<String, AttributeIndexEntries> aModelIndexer = null;

    //************** Calculated Covariance Limits *******************************

    /**
     * The minimum variance for any ray calculated during the ray uncertainty
     * stage.
     */
    private double[] aMinVariance =
            {Double.MAX_VALUE, Double.MAX_VALUE,
                    Double.MAX_VALUE, Double.MAX_VALUE};

    /**
     * The maximum variance for any ray calculated during the ray uncertainty
     * stage.
     */
    private double[] aMaxVariance =
            {-1.0, -1.0, -1.0, -1.0};

    //************** Debug Ray Parameters ***************************************

    /**
     * Map of all influence rays that are members of the debug output set
     * defined in aDebugRayDefns. The detailed calculation results of these
     * rays are collected on the distributed process node side and returned to
     * the client for processing. The map contains all analyzed influence rays
     * stored in the associative map as srcXId -> rcvrXAId -> rcvrXBId. type AA
     * rays have at least the entry where rcvrXBId == rcvrXAId. This map is
     * passed to process nodes during the ray uncertainty solution.
     */
    private HashMap<Long, HashMap<Long, HashSet<Long>>> aDebugRaysX = null;

    /**
     * Used to debug specific rays. The debug output path.
     */
    private String aDebugPath = "";

    /**
     * If true debug output is provided given the debug settings above.
     */
    private boolean aDebug = false;

    //************** Debug Source Limit Parameters ******************************

    /**
     * If true only sources that lie in the spherical cap centered at
     * aDebugSourceLat, aDebugSourceLon with a radius of aDebugSourceRadius
     * are considered in the subsequent ray uncertainty calculation. This
     * property is only set to true if the input property "debugSourceLimit"
     * is defined and the input start phase is RAY_PREDICTION.
     */
    private boolean aDebugSourceLimit = false;

    /**
     * Debug source spherical cap latitude. Only used if property
     * "debugSourceLimit" is defined.
     */
    private double aDebugSourceLat = 0.0;

    /**
     * Debug source spherical cap longitude. Only used if property
     * "debugSourceLimit" is defined.
     */
    private double aDebugSourceLon = 0.0;

    /**
     * Debug source spherical cap radius. Only used if property "debugSourceLimit"
     * is defined.
     */
    private double aDebugSourceRad = 0.0;

    /**
     * Tomography model layer names for outputting debug information.
     */
    private String[] aLayerNames = null;

    //************** Profiler Parameters ****************************************

    /**
     * The prediction profiler sample period ... defined if > 0.
     */
    private long aPredictionProfilerSamplePeriod = -1;

    /**
     * The prediction profiler node verbose flag ... output profile at nodes if
     * true.
     */
    private boolean aPredictionProfilerNodeVerbose = false;

    //************** Database Parameters ****************************************

    /**
     * Total observations read from the database.
     */
    private long aTotDBObs = 0;

    /**
     * An input property setting on whether or not to display the GUI's.
     */
    private boolean aDisplayGUI = true;

    /**
     * RayUncertaintyTask Garbage Collection frequency. Number of times run() is
     * executed before calling GC.
     */
    private int aGCFrequency = 10;

    /**
     * An input property setting that closes all GUI's when the solution finishes.
     */
    private boolean aExitGUIsOnCompletion = true;

    /**
     * GeoTessModel file extension being used
     */
    private String aGeoTessModelFileExt = "geotess";

    //************** Code *******************************************************

    /**
     * Main function used to run RayUncertainty
     *
     * @param args (property file name).
     * @throws GMPException
     * @throws GeoTomoException
     * @throws FatalDBUtilLibException
     */
    public static void main(String[] args) throws Exception {
        RayUncertainty ru = new RayUncertainty();
        ru.initializeSolution(args[0]);
        ru.solve();
    }
    
    public void initializeSolution(String propfile) throws Exception {
      initializeSolution(propfile, null);
    }

    /**
     * The public solution initialization function that must be called
     * before beginning the solution (the function solve()). This function
     * performs the following separate tasks:
     * <p>
     * a) Calls the initialize function,
     * b) Sets the source Definition, and finally,
     * c) Builds and defines the parallel broker.
     *
     * @param propfile The input property file name.
     * @throws IOException
     * @throws GMPException
     */
    public void initializeSolution(String propfile, ParallelBroker broker) throws Exception {
        String s;

        // set the start time and call initialize()

        aStartTime = (new Date()).getTime();
        aScrnWrtr = new ScreenWriterOutput();
        aScrnWrtr.setIndent("");

        // perform all standard initializations

        initialize(propfile);

        // set the source definition path

        if (aSourceDefinition == SourceDefinition.DATABASE)
            aSourcePath = "NA";
        else if (aSourceDefinition == SourceDefinition.PROPERTIESFILE)
            aSourcePath = aIODirectory + File.separator +
                    aProps.getProperty("propertiesFileName", "NA");

        // Create a new ParallelTaskManager and set its run mode ... set
        // the thread pool size to 512 ... this needs to be as large as the
        // number of processors for maximum efficiency.

        killBrokerAfterSolve = (broker == null);
        String pm = aProps.getProperty("parallelMode", "sequential").trim();
        if(broker != null) {
          aParallelBrkr = broker;
        }
        else {
          aParallelBrkr = ParallelBroker.create(pm);
          aParallelBrkr.setProperties(aProps);
        }
        if (aParallelBrkr.getName().equalsIgnoreCase("Concurrent")) {
            s = aProps.getProperty("concurrentProcessorCount", "-1").trim();
            int pc = Integer.valueOf(s);
            if (pc > 0)
                ((ParallelBrokerConcurrent) aParallelBrkr).setProcessorCount(pc);
        }
        
        //Lazily initialize the broker's task streaming settings, based one whatever the cluster
        //has available at the time of initial connection:
        aParallelBrkr.addConnectionListener(() -> {
          int procs = Math.max(aParallelBrkr.getProcessorCountEstimate(),
              Runtime.getRuntime().availableProcessors());
          
          // Max requested number of tasks-in-flight:
          String x = aProps.getProperty("maxRayUncertaintyTaskSubmissionLimit",""+procs*5).trim();
          int maxTskSubmsnLimit = Integer.valueOf(x);
          
          // Tasks per batched submission:
          x = aProps.getProperty("taskLimitPerSubmission");
          int batchSize = x == null ? Math.max(procs/8,8) : Integer.valueOf(x);
          
          // Simultaneous batch submission limit:
          x = aProps.getProperty("submissionLimit");
          int batchLimit = x == null ? Math.max(2, maxTskSubmsnLimit/batchSize +
              maxTskSubmsnLimit%batchSize > 0 ? 1 : 0) :
                Integer.valueOf(x);
          
          aParallelBrkr.setBatchSize(batchSize);
          aParallelBrkr.setMaxBatches(batchLimit);
        });

        // output solution phases, source definition, and parallel mode

        if (aScrnWrtr.isOutputOn()) {
            s = NL + "  Solution Phases ..." + NL +
                    "    Start Phase            = " + aStartPhase.name() + NL +
                    "    End Phase              = " + aEndPhase.name() + NL;
            aScrnWrtr.write(s);
            s = NL + "  Source Definition        = " + aSourceDefinition.name() + NL +
                    "  Source Path              = " + aSourcePath + NL;
            aScrnWrtr.write(s);
            s = NL + "  Parallel Mode            = " + pm + NL;
            aScrnWrtr.write(s);
        }

        // get the non-blocking parallel submit thread pool size. If this value
        // is > 1, then set the thread pool size into the parallel broker.

        s = aProps.getProperty("nonBlockingParallelSubmitThreadPoolSize", "-1").
                trim();
        int nbtps = Integer.valueOf(s);
        if (nbtps > 1) {
            aParallelBrkr.setClientThreadPoolSize(Integer.valueOf(s));
        }

        // set the covariance matrix block information object and ray weight path
        // statically into the PhaseSiteProcessData object.

        ObservationsSourceMap.setMatrixDefn(aCovMatrixBlockDefn);
    }

    /**
     * Creates a PropertiesPlusGMP reader from the input properties file name
     * string.
     *
     * @param propFileName The file name containing the input properties.
     * @param verbose      Optional parameter that if set to true outputs the
     *                     property file that is being read.
     * @throws IOException
     */
    public static PropertiesPlusGMP setProperties(String propFileName,
                                                  boolean... verbose)
            throws IOException {
        if ((verbose.length == 0) || (verbose[0] == true))
            System.out.println(NL + "Reading properties from file: " + propFileName);

        PropertiesPlusGMP props = new PropertiesPlusGMP();
        props.load(new FileInputStream(propFileName));
        props.setProperty("propertiesFileName", propFileName);
        return props;
    }

    /**
     * Primary initialization function. This function
     * a) Creates the PropertiesPlusGMP object from the input properties file
     * name (propFile),
     * b) Determines the start and end calculation phases,
     * c) Configures the output directory (read from the properties file),
     * d) Checks for the "weights" directory if the phase dictates it should
     * exist,
     * e) Sets the source definition,
     * f) Builds the covariance matrix file server,
     * g) Builds the source and tomographic GeoModels,
     * h) Defines the covariance matrix block information, and Finally,
     * i) Defines any debug properties.
     *
     * @param propFile The input properties file name.
     * @throws IOException
     * @throws GeoTomoException
     * @throws FatalDBUtilLibException
     */
    private void initialize(String propFile) throws Exception {
        String s;

        // set properties (aProps) and get the solution types ... verify at least
        // one solution type request was specified

        aProps = setProperties(propFile);
        aBenderProps = Bender.getBenderProperties(aProps);

        s = aProps.getProperty("startPhase", "PREDICTION").trim();
        aStartPhase = SolutionPhase.valueOf(s);
        s = aProps.getProperty("endPhase", "RAY_UNCERTAINTY").trim();
        aEndPhase = SolutionPhase.valueOf(s);
        if (aEndPhase.ordinal() < aStartPhase.ordinal()) {
            s = "Error: End phase ordinal (" + aEndPhase.name() +
                    ") must be greater than or equal to start phase ordinal (" +
                    aStartPhase.name() + ") ...";
            throw new IOException(s);
        }

        // set up IODirectory

        if (aStartPhase == SolutionPhase.PREDICTION) {
            aIODirectory = Utility.initializeDirectory(aProps, aScrnWrtr);
        } else {
            // make sure aIODirectory exists

            aIODirectory = aProps.getPropertyPath("ioDirectory", "").trim();
            File f = new File(aIODirectory);
            if (!f.exists()) {
                s = NL +
                        "Property path \"ioDirectory\" = \"" + aIODirectory + "\"" + NL +
                        "Does not exist ..." + NL +
                        "Path must exist if start phase is not \"PREDICTION\"" +
                        " ..." + NL + "Exiting ...";
                aScrnWrtr.write(s + NL);
                throw new IOException(s);
            }

            // add "run" path to aIODirectory and create new run_PHASE
            // directory if necessary

            String saveIODir = aIODirectory;
            aIODirectory += File.separator + "run_" + aStartPhase.name();
            aProps.setProperty("ioDirectory", aIODirectory);
            aIODirectory = saveIODir;
            aProps.setProperty("ioDirectory", aIODirectory);
        }
        setOutputWriterMode();

        // check for existence of "weights" directory if start phase is
        // RAY_UNCERTAINTY

        // assign ray weight and block map paths

        aRayWghtPath = aIODirectory + File.separator + "weights";
        if (aStartPhase == SolutionPhase.RAY_UNCERTAINTY) {
            File f = new File(aRayWghtPath);
            if (!f.exists()) {
                s = NL +
                        "Path \"" + aRayWghtPath + "\"" + NL +
                        "Does not exist ..." + NL +
                        "Path must exist if start phase is not \"BLOCK_MAPS\"" +
                        " ..." + NL + "Exiting ...";
                aScrnWrtr.write(s + NL);
                throw new IOException(s);
            }
        }

        // assign output model path

        aSourceModelOutputPath = aIODirectory + File.separator + "sourceModels";

        // set task garbage collection frequency

        aGCFrequency = aProps.getInt("taskGarbageCollectionFrequency", 10);

        // output header

        if (aScrnWrtr.isOutputOn()) {
            s = NL + "Ray Uncertainty version " + getVersion() + NL +
                    "Start Time: " + Globals.getTimeStamp() + NL + NL +
                    "  Created IO Directory: \"" + aIODirectory + "\"" + NL;
            aScrnWrtr.write(s);
        }

        // define where sources are defined

        s = aProps.getProperty("sourceDefinition", "GeoTessModel");
        aSourceDefinition = SourceDefinition.valueOf(s.toUpperCase());

        // build file servers

        buildFileServers();

        //****X Moved this definition in front of buildTomographicGeoModel
        // build covariance matrix block information object and output size

        String f = aCovMatrixServers.getPath("cov", 0);
        f = MatrixBlockDefinition.getDefaultPathFileName(f);
        aCovMatrixBlockDefn = new MatrixBlockDefinition(f);

        // build tomography GeoTessModel. Also, construct
        // active node to matrix column map for use after
        // prediction to convert each rays active node
        // index into a matrix column index or a pseudo
        // matrix column representing a non-referenced
        // node.

        buildTomographicGeoModel();
        buildActiveNodeMap();
        
        if (aScrnWrtr.isOutputOn()) {
            s = NL + "  Covariance Matrix Size ..." + NL +
                    "    Matrix Size            = " + aCovMatrixBlockDefn.size() +
                    " x " + aCovMatrixBlockDefn.size() + NL +
                    "    Matrix Blocks          = " + aCovMatrixBlockDefn.blocks() +
                    " x " + aCovMatrixBlockDefn.blocks() + NL +
                    "    Matrix Block Size      = " + aCovMatrixBlockDefn.blockSize() +
                    " x " + aCovMatrixBlockDefn.blockSize() + NL +
                    "    Total Block Count      = " +
                    aCovMatrixBlockDefn.symmMatrixBlockCount() + " {Nb(Nb+1)/2}" + NL;
            aScrnWrtr.write(s);
        }

        // set output flags and covariance approximation limits

        s = aProps.getProperty("outputNodeTaskInformation", "false").trim();
        aNodeOutputTaskInfoFlag = Boolean.valueOf(s);

        // see if debug source limit spherical cap is defined

        s = aProps.getProperty("debugSourceLimit", "");
        if (!s.equals("")) {
            String[] tokens = Globals.getTokens(s, "\t, ");
            if (tokens.length != 3) {
                s = "Error: Property \"debugSourceLimit\" requires 3 inputs " +
                        "(latitude, longitude, radius) ..." + NL +
                        "       Found \"" + s + "\" ..." + NL;
                throw new IOException(s);
            }

            aDebugSourceLat = Double.valueOf(tokens[0]);
            aDebugSourceLon = Double.valueOf(tokens[1]);
            aDebugSourceRad = Double.valueOf(tokens[2]);

            aDebugSourceLimit = true;

            if (aScrnWrtr.isOutputOn()) {
                s = NL + "  Debug Source Cap Settings ..." + NL +
                        "    Cap Center Latitude (deg)  = " + aDebugSourceLat + NL +
                        "    Cap Center Longitude (deg) = " + aDebugSourceLon + NL +
                        "    Cap Radius (deg)           = " + aDebugSourceRad + NL;
                aScrnWrtr.write(s);
            }
        }

        s = aProps.getProperty("displayGUI", "true").trim();
        aDisplayGUI = Boolean.valueOf(s);

        s = aProps.getProperty("exitGUIsOnCompletion", "false").trim();
        aExitGUIsOnCompletion = Boolean.valueOf(s);

        // set tomography and prediction profiler sample periods and node verbosity
        // if defined

        s = aProps.getProperty("predictionProfileSamplePeriod", "-1").trim();
        aPredictionProfilerSamplePeriod = Long.valueOf(s);

        s = aProps.getProperty("predictionProfileNodeVerbose", "false").trim();
        aPredictionProfilerNodeVerbose = Boolean.valueOf(s);

        aGeoTessModelFileExt = aProps.getProperty("geoTessModelFileExt", "geotess").trim();
    }

    /**
     * Used to build any requested debug ray definitions. Requires that the
     * containers aUniqueSourceFileMap (buildSourceFileMap()), aReceiverList
     * (getInputSites()), aSitePairs (getInputSitePairs()), and aPhaseList
     * (getInputPhases()) be constructed before calling this function.
     * <p>
     * debugRay = [srcModelFile1 :] [phase1 :] sourceId1 ReceiverAName1 [ReceiverBName1];
     * [srcModelFile2 :] [phase2 :] sourceId2 ReceiverAName2 [ReceiverBName2]
     * <p>
     * where the ";" separates distinct debug rays. As many as desired can be
     * added.
     * <p>
     * The "srcModelFile#" and "phase#" entries are optional if only one of each
     * is defined for the current execution. If more than one source model is
     * defined then "srcModelFile#" defaults to the first one entered during
     * sourceFileMap construction. Likewise, if more than one phase is defined
     * then "phase#" defaults to the first one defined in the getInputPhases()
     * function. Source model file, phase, and ray components (src/rcvrA/rcvrB)
     * must be separated by ":" if they are provided. If both source model file
     * and phase are input they are assumed to be in that order.
     * <p>
     * The B receiver ("ReceiverBName#") is only required for type AB ray
     * covariance. It is not defined for type AA rays.
     *
     * @throws IOException
     * @throws GMPException
     */
    private void buildDebugRays() throws IOException, GMPException {
        String s;

        // set the default source model name and phase.

        String defDbgModelName = aUniqueSourceFileRcvrMap.entrySet().iterator().
                next().getKey();
        SeismicPhase defDbgPhase = aPhaseList.get(0);

        // make a map of receiver sta() name with the receiver, and a set of
        // supported phases for this execution

        HashMap<String, Receiver> rcvrNameMap = new HashMap<String, Receiver>();
        for (Receiver rcvr : aReceiverList) rcvrNameMap.put(rcvr.getSta(), rcvr);
        HashSet<SeismicPhase> phaseSet = new HashSet<SeismicPhase>();
        for (SeismicPhase ph : aPhaseList) phaseSet.add(ph);

        // set the debug flag to false and get the "debugRay" property ... see if
        // any are defined

        aDebug = false;
        s = aProps.getProperty("debugRay", "");
        if (!s.equals("")) {
            // at least one is defined get the array of all debug ray definitions
            // and create the list to hold them as they are constructed.

            String[] raydefs = Globals.getTokens(s, ";");

            // loop over all debug ray definitions

            for (int i = 0; i < raydefs.length; ++i) {
                // get the ith definition ... look for primary parts
                // "model : phase : ray" ... the last one (may be the only one) is the
                // ray definition so set it now

                String[] rayparts = Globals.getTokens(raydefs[i], ":");
                String ray = rayparts[rayparts.length - 1];

                // check the model name and phase name to the defaults ... check the
                // validity of the model name and/or phase name if they were defined

                String dbgModelName = defDbgModelName;
                SeismicPhase dbgPhase = defDbgPhase;
                if (rayparts.length == 3) {
                    // model name and phase name were provided ... make sure model name
                    // is valid

                    dbgModelName = rayparts[0];
                    if (!aUniqueSourceFileRcvrMap.containsKey(dbgModelName)) {
                        s = "Debug Ray Model Name: " + dbgModelName + " is not a valid " +
                                "model name ...";
                        throw new IOException(s);
                    }

                    // make sure phase name is valid

                    dbgPhase = SeismicPhase.valueOf(rayparts[1]);
                    if (!phaseSet.contains(dbgPhase)) {
                        s = "SeismicPhase Name: " + dbgPhase.name() + " was not a requested " +
                                "phase name ...";
                        throw new IOException(s);
                    }
                } else if (rayparts.length == 2) {
                    // either model name or phase name was provided ... first test
                    // as a model name

                    if (!aUniqueSourceFileRcvrMap.containsKey(rayparts[0])) {
                        // not a model name ... so it must be a phase name ... check its
                        // validity

                        if (!phaseSet.contains(SeismicPhase.valueOf(rayparts[0]))) {
                            s = "Input model (or phase) Name: " + rayparts[0] +
                                    " is not valid ...";
                            throw new IOException(s);
                        } else // valid phase name ... set it
                            dbgPhase = SeismicPhase.valueOf(rayparts[0]);
                    } else // valid model name ... set it
                        dbgModelName = rayparts[0];
                }

                // valid model and phase ... make sure the ray has valid number of
                // entries

                String[] rayCmpnts = Globals.getTokens(ray, "\t, ");
                if ((rayCmpnts.length < 2) || (rayCmpnts.length > 3)) {
                    s = "Error: Property \"debugRay\" ray definition property requires " +
                            "source id, site A id OR source id, site A id, site B id ..." +
                            NL + "       Found \"" + ray + "\" ..." + NL;
                    throw new IOException(s);
                }

                // set source

                long srcId = Long.valueOf(rayCmpnts[0]);

                //  set receiver (A) and validate

                Receiver rcvrA = rcvrNameMap.get(rayCmpnts[1]);
                if (rcvrA == null) {
                    s = "Input Debug Ray Receiver Name: \"" + rayCmpnts[1] +
                            "\" is not valid ...";
                    throw new IOException(s);
                }

                // set receiver B to null and see if a definition was provided

                Receiver rcvrB = null;
                if (rayCmpnts.length == 3) {
                    // set receiver B and validate

                    rcvrB = rcvrNameMap.get(rayCmpnts[2]);
                    if (rcvrB == null) {
                        s = "Input Debug Ray Receiver B Name: \"" + rayCmpnts[2] +
                                "\" is not valid ...";
                        throw new IOException(s);
                    }

                    // validate site pair

                    if (aSitePair.get(rcvrA) != rcvrB) {
                        s = "Input Debug Ray Receiver B Name: \"" + rayCmpnts[2] +
                                "\" is not a pair with Receiver A Name: \"" +
                                rayCmpnts[1] + "\" ...";
                        throw new IOException(s);
                    }
                }

                // done ... have model name, phase, source id, and receiver A (and maybe
                // B) definitions. Create the DebugRayOutput object and add it to the
                // map

                DebugRayOutput.addDebugRay(dbgModelName, dbgPhase, rcvrA, rcvrB, srcId);
            } // end for (int i = 0; i < raydefs.length; ++i)

            // done ... create debug directory

            if (DebugRayOutput.getList().size() > 0)
                aDebugRaysX = new HashMap<Long, HashMap<Long, HashSet<Long>>>();

            s = aProps.getProperty("debugRayPath", "debug");
            aDebugPath = aIODirectory + File.separator + s;
            File dbp = new File(aDebugPath);
            if (!dbp.exists()) FileDirHandler.createDirectory(aDebugPath);

            // set debug flag to true and exit

            aDebug = true;
        } // end if (!s.equals(""))
    }

    /**
     * Creates and initializes the UI.
     */
    private void createAndInitializeUI() {
        // create and initialize GUI

        aUI = new RayUncertaintyUI();
        SolutionPhase[] stage = null;
        if ((aStartPhase == RayUncertainty.SolutionPhase.PREDICTION) &&
                (aEndPhase == RayUncertainty.SolutionPhase.RAY_UNCERTAINTY)) {
            stage = new SolutionPhase[2];
            stage[0] = RayUncertainty.SolutionPhase.PREDICTION;
            stage[1] = RayUncertainty.SolutionPhase.RAY_UNCERTAINTY;
        } else if (aStartPhase == aEndPhase) {
            stage = new SolutionPhase[1];
            stage[0] = aStartPhase;
        }

        // set stage and initialize status

        aUI.makeGUI(stage);
        aUI.setInitialStatus(aSourceDefinition.name(), aSourcePath,
                aPhaseList.size(), aModelSourceCount,
                aReceiverList.size(), aSitePair.size(),
                aCovMatrixBlockDefn);
    }

    /**
     * The primary solution function called after initialization.
     *
     * @throws GMPException
     * @throws IOException
     * @throws GeoTomoException
     * @throws FatalDBUtilLibException
     */
    public void solve() throws Exception {
        // build phase, site, source, and PhaseSiteProcessData maps and create
        // observations

// Database functionality commented out by sballar 2022-09-27 because there is no requitement 
// for it and it introduces many dependencies that are challenging to manage.
//        if (aSourceDefinition == SourceDefinition.DATABASE)
//            buildDatabaseObservations();
//        else
            buildNonDatabaseObservations();

        // build the non-represented active node variance vector and output the
        // result to disk so that it is available during the block map solution

        setNonRepresentedVariance();

        // set debug statics if requested

        //6TODO PS: The debugging capability will have to be changed to support
        // TODO PS: the P and S wave active node maps. The old versions had only
        // TODO PS: one map. The two methods below that use the single
        // TODO PS: aActiveNodeMap will have to be modified to accept both. 

        if (aDebug) {
            DebugObservation.setActiveNodeMap(aActiveNodeMap);
            DebugRayOutput.setCovarianceMatrixSize(aCovMatrixBlockDefn.size());
            DebugRayOutputEntry.setTomographyModel(aGeoModelTomo);
            DebugRayOutputEntry.setMatrixColumnMap(aActiveNodeMap);
            DebugRayOutputEntry.setLayerNames(aLayerNames);
        }

        // create and initialize GUI

        if (aDisplayGUI) createAndInitializeUI();

        // predict all rays if start phase is prediction

        if (aStartPhase == SolutionPhase.PREDICTION) predictRays();

        // solve for ray uncertainty if the end phase is defined as such

        if (aEndPhase == SolutionPhase.RAY_UNCERTAINTY) {
            if (aInputObservationMap == null) {
                readInputObservationMap();
                dumpInputObservationMap();
            }

            // solve ray uncertainty

            solveRayUncertainty();
        }

        // perform uncertainty interpolation from tomography grid back to source
        // grids

        interpolateUncertainties();

        // output any debug entries

        if (aDebug) {
            // loop over all debug ray requests and populate their entries, sort them,
            // and output them to the screen writer ... set the screen writer buffer
            // to a new file name for each debug ray

            BufferedWriter bw = aScrnWrtr.getWriter();
            ArrayList<DebugRayOutput> droList = DebugRayOutput.getList();
            for (int r = 0; r < droList.size(); ++r) {
                // get the next debug ray and create a file name and subsequent
                // buffered writer and set it into aScrnWrtr

                DebugRayOutput dro = droList.get(r);
                String pth = aDebugPath + File.separator + dro.getFileName();
                FileWriter fw = new FileWriter(pth);
                BufferedWriter droBW = new BufferedWriter(fw);
                aScrnWrtr.setWriter(droBW);

                // sort the debug ray contents and output it to the screen writer ...
                // close the buffered writer and get the next debug ray

                dro.sort();
                dro.output(aScrnWrtr);
                fw.close();
            }

            // done ... reset the original buffered writer back into the screen
            // writer

            aScrnWrtr.setWriter(bw);

            // loop over all debug ray influence rays from the tomography model and
            // output their ray path information to files here

            ArrayList<DebugObservation> doList = DebugObservation.getList();
            for (int o = 0; o < doList.size(); ++o) {
                DebugObservation dbgobs = doList.get(o);
                dbgobs.outputRay(aDebugPath, aGeoModelTomo, aLayerNames,
                        aCovMatrixBlockDefn.size());
            }
        }

        // write out final data and close the broker and exit

        outputFinal();
        if (killBrokerAfterSolve) aParallelBrkr.close();
        if (aUI != null) aUI.updateInitialStatus(null);

        // exit GUIs if flag is set

        if (aExitGUIsOnCompletion) {
            System.out.println("Exiting ...");
            System.exit(0);
        }
    }

    /**
     * Final output message.
     */
    private void outputFinal() {
        // output summary results of final solution

        if (aScrnWrtr.isOutputOn()) {
            String s = NL + "  Ray Uncertainty Solution Complete:" + NL +
                    "    Current Time                            = " +
                    Globals.getTimeStamp() + NL +
                    "    Total Solution Execution Time           = " +
                    Globals.elapsedTimeString(aStartTime) + NL + NL;
            aScrnWrtr.write(s);
        }
        
        try {
			aScrnWrtr.getWriter().close();
		} catch (IOException e) {
			//e.printStackTrace();
		}
    }

    /**
     * Builds the file server object.
     *
     * @throws IOException
     */
    private void buildFileServers() throws IOException {
        String s;
        String[] tokens;

        if (aScrnWrtr.isOutputOn()) {
            s = NL + "  Building Covariance File Servers ..." + NL;
            aScrnWrtr.write(s);
        }

        // setup server directories to have the same settings as aIODirectory
        // this means creating aIODirectory (and aBaseIODirectory if it is
        // assigned) and writing refloc.txt into each. Also the FileServerPath
        // object is created which will contain at a minimum aIODirectory if no
        // others are defined

        // load the input data matrix file definitions ... create the input data
        // matrix FileServerPath object and load any defined server paths.

        aCovMatrixServers = new MatrixBlockFileServer();

        s = aProps.getPropertyPath("covarianceFileServerPaths", "").trim();
        tokens = Globals.getTokens(s, "\t, ");
        for (String fs : tokens) {
            // see if base path exists

            File ffs = new File(fs);
            if (ffs.exists()) {
                aCovMatrixServers.addServerPath(fs);
            } else {
                // base path does not exist ... throw error

                s = NL + "Error: Base Input Matrix Server Path: \"" + fs +
                        "\" does not exist ..." + NL + "Exiting ...";
                aScrnWrtr.write(s + NL);
                throw new IOException(s);
            }
        }

        // have all input matrix file server paths ... see if the block map is
        // specified

        s = aProps.getProperty("covarianceFileServerBlockMap", "").trim();
        tokens = Globals.getTokens(s, "\t, ");
        if (tokens != null) {
            for (String fs : tokens) {
                int ie = Integer.valueOf(fs);
                if ((ie < 0) || (ie >= aCovMatrixServers.getServerCount())) {
                    s = NL + "Error: property \"covarianceFileServerBlockMap\" entry, " +
                            fs + ", is invalid ..." + NL + "Exiting ...";
                    aScrnWrtr.write(s + NL);
                    throw new IOException(s);
                }
                aCovMatrixServers.addBlockServerStencilEntry(ie);
            }
        }

        // have stencil ... see if server storage use fractions are given (these
        // are preferred over the stencil definitions above).

        s = aProps.getProperty("covarianceFileServerStorageUseFractions", "").
                trim();
        tokens = Globals.getTokens(s, "\t, ");
        if (tokens != null) {
            for (String fs : tokens) {
                double e = Double.valueOf(fs);
                if (e <= 0.0) {
                    s = NL + "Error: property " +
                            "\"covarianceFileServerStorageUseFraction\" entry, " + fs +
                            ", is invalid ..." + NL + "Exiting ...";
                    aScrnWrtr.write(s + NL);
                    throw new IOException(s);
                }
                aCovMatrixServers.addServerStorageUsageFraction(e);
            }
        }

        // set default secondary path definition

        aCovMatrixServers.addSecondaryFilePath("cov", "cov");

        // add secondary path definition if it is defined

        s = aProps.getProperty("covarianceFileSecondaryPaths", "").trim();
        String[] subtokens = Globals.getTokens(s, "\t, ");
        if ((subtokens != null) && (subtokens.length == 2)) {
            if (subtokens[0].toLowerCase().equals("cov")) {
                aCovMatrixServers.addSecondaryFilePath("cov", subtokens[1]);
            } else {
                s = "Error: property \"covarianceFileSecondaryPaths\" entry " +
                        subtokens[0] + " is not the same as the input header name (" +
                        "cov) ....";
                aScrnWrtr.write(s + NL);
                throw new IOException(s);
            }
        } else {
            s = "Error: property \"covarianceFileSecondaryPaths\" = " +
                    s + " ... must be a pairwise entry (headerName pathName) ....";
            aScrnWrtr.write(s + NL);
            throw new IOException(s);
        }

        // verify full path exists

        for (int i = 0; i < aCovMatrixServers.getServerCount(); ++i) {
            String fs = aCovMatrixServers.getPath("cov", i);
            s = "    File Server " + i + ": \"" + fs + "\"" + NL;
            aScrnWrtr.write(s);
            File ffs = new File(fs);
            if (!ffs.exists()) {
                s = "Error: Input path to \"cov\" block files does not exist ..." +
                        NL + "       Path: \"" + fs + "\"" + NL;
                aScrnWrtr.write(s + NL);
                throw new IOException(s);
            }
        }
    }

    /**
     * Returns the source model (GeoTessModel) read from the input
     * file location back to the caller as a ModelInterface object.
     *
     * @param fn     The path from where the source model is read.
     * @param spcHdr Space header for output load message purposes.
     * @param silent If true the model toString() function is not output after
     *               loading.
     * @return The loaded model.
     * @throws GMPException
     * @throws IOException
     */
    private ModelInterface loadModel(String fn, String spcHdr, boolean silent)
            throws GMPException, IOException {
        String s;

        // output load message

        if (aScrnWrtr.isOutputOn()) {
            s = NL + spcHdr + "  Reading Source GeoModel = \"" + fn + "\"" + NL;
            aScrnWrtr.write(s);
        }

        // load based on source type

        if (aSourceDefinition == SourceDefinition.GEOTESSMODEL) {
            // read GeoModel and set active nodes ... output content if requested

            GeoTessModelGMP gm = new GeoTessModelGMP(fn);
            gm.setActiveRegion();
            //gm.setActiveNodesGlobal();
            if (!silent) {
                s = Globals.prependLineHeader(gm.toString(), spcHdr + "    ");
                aScrnWrtr.write(NL + s + NL);
            }
            return gm;
        } 
//        else if (aSourceDefinition == SourceDefinition.LIBCORR3D) {
//            // Load GeoTessModel ... output content if requested
//
//            LibCorr3DModelGMP gm = new LibCorr3DModelGMP(fn);
//            if (!silent) {
//                s = Globals.prependLineHeader(gm.toString(), spcHdr + "    ");
//                aScrnWrtr.write(NL + s + NL);
//            }
//            return gm;
//        }

        // not a valid source definition ... return null

        return null;
    }

    //3TODO PS: This is new code that replaces the old single wave type active node
    // TODO PS: map build. Remove the old method buildActiveNodeMap() and replace
    // TODO PS: it with this one (Rename by removing the X at the end of the method
    // TODO PS: name). The new code expects two active node maps called
    // TODO PS: aActiveNodeMapP and aActiveNodeMapS.
    /**
     * Builds the wave type specific active node to matrix column maps given
     * in aActiveNodeMapP and aActiveNodeMapS. These maps relate a global
     * active node index to a covariance matrix column index. For example,
     * 
     * 		int covMatrixColIndex = aActiveNodeMapP.get(globalActiveNodeIndex);
     * 
     * If the globalActiveNodeIndex is a node that was utilized in the P wave
     * section of the tomography solution, then the returned covMatrixColIndex
     * is a valid index that is less than the total number of columns in the
     * covariance matrix, and for P wave, less than the total number of P columns
     * in the matrix. If the returned covMatrixColIndex is >= to the total number
     * of columns in the covariance matrix, then this represents a pseudo index
     * for a non-represented node that was not part of the tomography solution.
     * Non-represented nodes are global active node indices that were not
     * active during the tomography solution, or were active, but never touched
     * by any ray used to create to the tomography solution.
     * 
     * Non-represented nodes will be used to save non-represented variances, which
     * are used to calculate ray uncertainty during the solution phase. P and S
     * non-represented pseudo matrix column indexes are unique in each of the two
     * active node maps. This will enable P wave non-represented variances to be
     * associated with P wave weights, and S wave non-represented variances to be
     * associated with S wave weights.
     * 
     * @throws IOException
     */
    private void buildActiveNodeMapX() throws IOException {
        String s, f;

		// get the sparse matrix column node map and load it. This was written
		// by tomography and contains the list of active node indexes for the
		// tomography solution. These are not likely global indexes, as
		// tomography solution typically utilizes a polygon to restrict the
        // active region.
        // 
        // In addition to the to the tomography active node indexes, this file
        // also contains the associated global point indices, which are a
        // GeoTessModel 3 element int array that holds the grid node index,
        // major layer index, and sub-layer index of each node in the
        // GeoTessModel. These can be used to find the associated global node
        // active node indices in the method buildActiveNodeMapWaveType(...)
        // below.

        aCovMtrxActvNodeMapPath = aProps.getPropertyPath("covarianceMatrixActiveNodeMapPath", "").trim();
        aCovMtrxActvNodeMapName = aProps.getProperty(
                "covarianceMatrixActiveNodeMapFileName", "").trim();

        if (aScrnWrtr.isOutputOn()) {
            s = "  Reading Covariance Matrix Node Map = \"" +
                    aCovMtrxActvNodeMapPath + File.separator + aCovMtrxActvNodeMapName +
                    "\"" + NL;
            aScrnWrtr.write(s);
        }

        // open active node map and create input file buffer ... read the number of
        // P wave entries and total entries in the active node map file. Note: The
        // total number of entries is the size of the number of matrix columns in
        // the covariance matrix.

        f = aCovMtrxActvNodeMapPath + File.separator + aCovMtrxActvNodeMapName;
        FileInputBuffer fib = new FileInputBuffer(f);
        
        // TODO PS: make sure the code in the tomography method
        // TODO PS: GeoTomogrpahy.writeActiveNodePointIndexMap(String fpth) is 
        // TODO PS: modified to output the number of P wave entries
        // TODO PS: (aRepNodeCountP) in the sparse matrix. It currently does not
        // TODO PS: do this.

        // TODO PS: remove the int type from the declarations for aRepNodeCountP
        // TODO PS: and aRepNodeCountS below. These are now declared fields of
        // TODO PS: RayUncertainty, once you make changes at the top of this file.
        int aRepNodeCountP = fib.readInt();
        int covMtrxSize    = fib.readInt();
        
        int aRepNodeCountS = covMtrxSize - aRepNodeCountP;
        
        // make sure the input active node file from tomography agrees with the
        // input covariance matrix in terms of the number of entries.
        
        if (covMtrxSize != aCovMatrixBlockDefn.size()) {
            s = "\nError: Tomography active node map has " + covMtrxSize +
                    " active nodes ...\n" + 
                    "       The Sparse Matrix size is " + aCovMatrixBlockDefn.size() +
                    " ... \n" +
                    "       The covariance matrix size and the inut active node" +
                    " index map size do not match ...\n\n";
            throw new IOException(s);
        }

        // Build the P and S wave active node maps. On return the following size
        // information should be true for each of the active node maps:
        
        // TODO PS: In the two methods below change the name of the aActiveNodeMap
        // TODO PS: to aActiveNodeMapP in the first call and aActiveNodeMapS in the
        // TODO PS: second call once they have been added to this objects fields.
        // TODO PS: The old usage, aActiveNodeMap, should be removed from the code.

        int nonRepP = buildActiveNodeMapWaveType(fib, 0, aRepNodeCountP, covMtrxSize,
        		aActiveNodeMap);
        int nonRepS = buildActiveNodeMapWaveType(fib, aRepNodeCountP, covMtrxSize,
        		nonRepP, aActiveNodeMap);

        // close the input active node index file and output results
        
        fib.close();
        
        // The following information can be determined for nonRepP and nonRepS
        // 
        // 		let nonRepNodeCountP be the number of non-represented nodes added
        //      to the map aActiveNodeMapP. The non-represented node pseudo index
        //		for P waves started at covMtrxSize and ended at nonRepP-1, so we
        //      can write		nonRepNodeCountP = nonRepP - covMtrxSize
        //
        //      Similarly, let nonRepNodeCountS be the number of non-represented
        //		nodes added to the map aActiveNodeMapS. The non-represented node
        //      pseudo index for S waves started at nonRepP and ended at nonRepS-1, so we
        //      can write		nonRepNodeCountS = nonRepS - nonRepP
        //
        // 		Each map has glblActiveNodeCount = aGeoModelTomo.getPointMap().size();
        //		entries.
        //
        //		So, nonRepNodeCountP + aRepNodeCountP = glblActiveNodeCount, and
        //		nonRepNodeCountS + aRepNodeCountS = glblActiveNodeCount
        //
        //		Solving for nonRepP and nonRepS from the relations above gives
        //
        //			nonRepP = glblActiveNodeCount + aRepNodeCountS, and
        //			nonRepS = 2 * glblActiveNodeCount
        //		

        if (aScrnWrtr.isOutputOn()) {
            s = "    Active Node Map Construction Information:\n" +
                "    	Tomography GeoTessModel Global Active Node Count    = " +
                	aGeoModelTomo.getPointMap().size() + "\n" +
                "    	Covariance Matrix Represented Node Count            = " +
            		covMtrxSize  + "\n" +
                "    	P Wave Represented Node Count                       = " +
            		aRepNodeCountP  + "\n" +
                "    	S Wave Represented Node Count                       = " +
            		aRepNodeCountS  + "\n" +
                "    	P Wave Non-Represented Node Count                   = " +
            		(aGeoModelTomo.getPointMap().size() - aRepNodeCountP)  + "\n" +
                "    	S Wave Non-Represented Node Count                   = " +
            		(aGeoModelTomo.getPointMap().size() - aRepNodeCountP)  + "\n" +
                "    	Total P Wave entries                                = " +
            		(aGeoModelTomo.getPointMap().size())  + "\n" +
                "    	Total S Wave entries                                = " +
            		(aGeoModelTomo.getPointMap().size())  + "\n" +
                "    	Total P and S Wave entries                          = " +
            		nonRepS  + "\n\n";
            aScrnWrtr.write(s);
        }
    }

    /**
     * Builds a specific active node to matrix column map (activeNodeMap)
     * from wave type specific row entries in the input FileInputBuffer fib.
     * The start and end index of the lines to be read from the file are
     * given by fibStrt and fibEnd-1. These represent covariance matrix column
     * indexes. All non-represented pseudo matrix column indexes added to
     * the map begin with index nonRepStrt.
     * 
     * The resulting map relates a global active node index from the tomography
     * model to a covariance matrix column index. For example,
     * 
     * 		int covMatrixColIndex = aActiveNodeMap.get(globalActiveNodeIndex);
     * 
     * If the globalActiveNodeIndex is a node that was utilized in the wave
     * type section of the tomography solution for which this map is being
     * constructed, then the returned covMatrixColIndex is a valid index that
     * is less than the total number of columns in the covariance matrix. If
     * the returned covMatrixColIndex is >= to the total number of columns in
     * the covariance matrix, then the index represents a pseudo index
     * for a non-represented node that was not part of the tomography solution.
     * Non-represented nodes are global active node indices that were not
     * active during the tomography solution, or were active, but never touched
     * by any ray used to create to the tomography solution.
     * 
     * Non-represented nodes will be used to save non-represented variances, which
     * are used to calculate ray uncertainty during the solution phase. P and S
     * non-represented pseudo indexes are unique in each of the two active node
     * maps. This will enable P wave non-represented variances to be associated
     * with P wave weights, and S wave non-represented variances to be associated
     * with S wave weights. The input nonRepStrt will be different depending on
     * if this is a P wave or S wave call to this method.
     * 
     * @param fib			The FileInputBuffer from which matrix column active
     * 						node descriptions are input.
     * @param fibStrt		The first matrix column to be read from the input
     * 						FileInputBuffer fib.
     * @param fibEnd		The last matrix column to be read from the input
     * 						FileInputBuffer fib.
     * @param nonRepStrt	The starting pseudo matrix column index for non-
     * 						represented nodes.
     * @param activeNodeMap The active node map to be constructed (P or S wave).
     * 
     * @return The total number of non-represented nodes added to the map
     * 		   not counting the start value of nonRepStrt.
     * 
     * @throws IOException
     */
    private int buildActiveNodeMapWaveType(FileInputBuffer fib, int fibStrt,
    		int fibEnd, int nonRepStrt, HashMapIntegerInteger activeNodeMap)
    				throws IOException {
    	
        String s;

        // Get the tomography model point map and then loop over all entries of
        // the input file buffer, fib, reading matrix columns from fibStrt to
        // fibEnd-1. The entries in the file include the active node index, grid
        // node index, major layer index, and sub-layer index. The active node
        // index is ignored as it was unlikely global. The three point indexes
        // can be used to find the global index, which is used to populate the
        // map.

        PointMap pmap = aGeoModelTomo.getPointMap();
        for (int mc = fibStrt; mc < fibEnd; ++mc) {
        	
            // read the active node index, grid node index, major layer index, and
            // sub layer index for matrix column mc. For ray uncertainty the input
        	// active node index is not used and will be neglected.

            fib.readInt(); // read active node index ... not used
            int gni = fib.readInt();
            int mli = fib.readInt();
            int sli = fib.readInt();
            
            // retrieve the global active node index from the tomography model and
            // ensure it is valid
            
            int gani = pmap.getPointIndex(gni, mli, sli);
            if (gani == -1) {
            	s = "\nError: Input Active node Entry: Matric Column Index " + mc +
            		", Grid Node Index " + gni + ", Major Layer Index " + mli +
            		", Sub-Layer Index " + sli + "\n" +
            		"       Was not found in the input tomography model ...\n\n";
            	throw new IOException(s);
            }
            
            // save the global active node index into the map associated with the
            // matrix column index mc.
            
            activeNodeMap.put(gani, mc);
        }
        
        // loop over all global active nodes in the tomography model to add
        // non-represented nodes to the global active node map
        
        for (int gani = 0; gani < pmap.size(); ++gani) {
        	
        	// If the global active node index is already present then
        	// gani is a represented node within the covariance matrix.
        	// If it is not contained in the map then gani is a non-
        	// represented node in the tomography model. Associate the
        	// next pseudo index, nonRepStrt, to the global active node
        	// index and increment the nonRepStrt column counter in
        	// preparation for the next non-represented node entry.
        	
        	if (!activeNodeMap.contains(gani))
        		activeNodeMap.put(gani, nonRepStrt++);
        }
        
        // done ... return the final non-represented column entry in the map

        return nonRepStrt;
    }
    
    // TODO PS: remove this routine after aActiveNodeMapP and aActiveNodeMapS
    // TODO PS: fields have been created. Also rename the "X" version of this
    // TODO PS: method above.
    /**
     * Builds the Active node to matrix column map, aActiveNodeMap. This map
     * relates a global active node index to a covariance matrix column index
     * For example,
     * 
     * 		int covMatrixColIndex = aActiveNodeMap.get(globalActiveNodeIndex);
     * 
     * If the globalActiveNodeIndex is a node that was utilized in the tomography
     * solution, then the returned covMatrixColIndex is a valid index that is
     * less than the total number of columns in the covariance matrix. If the
     * returned covMatrixColIndex is >= to the total number of columns in the
     * covariance matrix, then this represents a pseudo index for a
     * non-represented node that was not part of the tomography solution.
     * Non-represented nodes are global active node indices that were not
     * active during the tomography solution, or were active, but never touched
     * by any ray used to create to the tomography solution.
     * 
     * Non-represented nodes will be used to save non-represented variances, which
     * are used to calculate ray uncertainty during the solution phase.
     * 
     * @throws IOException
     */
    private void buildActiveNodeMap() throws IOException {
    	
        HashMapIntegerKey.Entry<HashMapIntegerKey<HashMapIntegerInteger>> e;
        HashMapIntegerKey.Entry<HashMapIntegerInteger> ee;
        HashMapIntegerInteger.Entry eee;
        String s, f;

        // get sparse matrix column node map and load it. This was written
        // by tomography and contains the list of active node indexes. These
        // are not likely global indexes as tomography utilizes a polygon to
        // restrict the active region. In addition to the to the tomography
        // active node indexes, this file also has the associated global
        // point indices, which is a GeoTessModel 3 element int array that
        // holds the grid node index, major layer index, and sub-layer index,
        // of each node in the GeoTessModel. These can be used to find the
        // associated global node active node indices.

        aCovMtrxActvNodeMapPath = aProps.getPropertyPath("covarianceMatrixActiveNodeMapPath", "").trim();
        aCovMtrxActvNodeMapName = aProps.getProperty(
                "covarianceMatrixActiveNodeMapFileName", "").trim();

        if (aScrnWrtr.isOutputOn()) {
            s = "  Reading Covariance Matrix Node Map = \"" +
                    aCovMtrxActvNodeMapPath + File.separator + aCovMtrxActvNodeMapName +
                    "\"" + NL;
            aScrnWrtr.write(s);
        }

        // open active node map and create input file buffer ... read number of
        // entries. This number is the size of the number of matrix columns in the
        // covariance matrix.

        f = aCovMtrxActvNodeMapPath + File.separator + aCovMtrxActvNodeMapName;
        FileInputBuffer fib = new FileInputBuffer(f);
        int sze = fib.readInt();

//        if (sze != aCovMatrixBlockDefn.size()) {
//            s = "Error: Tomography active node map has " + sze +
//                    " active nodes ...\n" + 
//                    "       The Sparse Matrix size is " + aCovMatrixBlockDefn.size() +
//                    " ... \n" +
//                    "       The covariance matrix size and the inut active node" +
//                    " index map size do not match ...";
//            throw new IOException(s);
//        }

        // create GeoTesModel global node to matrix column map utilizing the
        // 3 element point indexes. The map is defined as
        // 
        // model grid node index -> major layer index -> sub-layer index -> matrix column.
        //
        // This will provide fast access to the matrix column index later when we
        // loop over all global active node indexes to build the active node map
        // (aActiveNodeMap).
        
        // size the grid node index outer map and begin loop reading in each matrix
        // columns active node description

        int gnCount = aGeoModelTomo.getNVertices();
        HashMapIntegerKey<HashMapIntegerKey<HashMapIntegerInteger>>
                nodeMtrxColumnMap =
                new HashMapIntegerKey<HashMapIntegerKey<HashMapIntegerInteger>>(gnCount);
        for (int i = 0; i < sze; ++i) {
        	
            // read the active node index, grid node index, major layer index, and
            // sub layer index for the ith matrix column (i). For ray uncertainty the
        	// active node index is not used and will be neglected.

            fib.readInt(); // read active node index ... not used
            int gni = fib.readInt();
            int mli = fib.readInt();
            int sli = fib.readInt();

            // see if the grid node index (gni) is in the map ... if not add it
            // set the entry assigned to gni to majorLayerIndexMap

            HashMapIntegerKey<HashMapIntegerInteger> majorLayerIndexMap = null;
            e = nodeMtrxColumnMap.getEntry(gni);
            if (e == null) {
                majorLayerIndexMap = new HashMapIntegerKey<HashMapIntegerInteger>();
                nodeMtrxColumnMap.put(gni, majorLayerIndexMap);
            } else
                majorLayerIndexMap = e.getValue();

            // see if the major layer index (mli) is in the major layer index map
            // (majorLayerIndexMap) ... if not add it ... set the entry assigned
            // to mli to sublayerIndexMap

            HashMapIntegerInteger sublayerIndexMap = null;
            ee = majorLayerIndexMap.getEntry(mli);
            if (ee == null) {
                sublayerIndexMap = new HashMapIntegerInteger();
                majorLayerIndexMap.put(mli, sublayerIndexMap);
            } else
                sublayerIndexMap = ee.getValue();

            // see if the the sub-layer index (sli) is in the sub-layer index map
            // (sublayerIndexMap) ... if not add it associated with the covariance
            // matrix column index i. If it already existed then throw an error.

            eee = sublayerIndexMap.getEntry(sli);
            if (eee == null) {
                sublayerIndexMap.put(sli, i);
            } else {
                s = "Error: Grid Node, Major Layer, Sub-Layer indices found for" +
                        " two matrix columns: (" + i + ", " + eee.getValue() + ") ...";
                throw new IOException(s);
            }
        }
        fib.close();

        // now make a map of all GeoModelTomo active nodes to matrix column nodes
        // which will be used during ray prediction to convert the ray node index
        // arrays to matrix column indices. This map will also include pseudo-matrix
        // column indices (above the actual covariance matrix size) which define
        // mappings of non-represented active nodes to fictitious matrix column
        // entries. See function setNonRepresentedVariance() for a description of
        // the non-represented active nodes and their use.

        if (aScrnWrtr.isOutputOn()) {
            s = "  Building Active Node Index To Covariance Matrix Index Map ..." +
                    NL;
            aScrnWrtr.write(s);
        }

        // save the represented node count (same as the covariance matrix size)
        // and the global active node count and non-represented node count.
        // Allocate the active node map and loop over all global active nodes
        // in the model.
        
        aRepNodeCount = sze;
        int glblActiveNodeCount = aGeoModelTomo.getPointMap().size();
        int glblNonRepNodeCount = glblActiveNodeCount - aRepNodeCount;
        aActiveNodeMap = new HashMapIntegerInteger(2 * glblActiveNodeCount);
        for (int i = 0; i < glblActiveNodeCount; ++i) {
        	
        	// get the global active node point index array and look for the
        	// grid node index (nmap[0]) in the temporary point index to
        	// matrix column map.
        	
            int[] nmap = aGeoModelTomo.getPointMap().getPointIndices(i);
            e = nodeMtrxColumnMap.getEntry(nmap[0]);
            
            // if the grid node index is not in the temporary map then this
            // is a non-represented node. Associate the global active node
            // index (i) with the next non-represented node index (sze) and
            // increment sze to be ready for the next entry
            
            if (e == null)
                aActiveNodeMap.put(i, sze++);
            else {
            	
            	// the grid node index was found in the temporary map. Now
            	// look for the major layer index (nmap[1]) in the temporary
            	// map.
            	
                ee = e.getValue().getEntry(nmap[1]);
                
                // if the major layer index is not in the temporary map then
                // this is a non-represented node. Associate the global active
                // node index (i) with the next non-represented node index (sze) and
                // increment sze to be ready for the next entry
                
                if (ee == null)
                    aActiveNodeMap.put(i, sze++);
                else {
                	
                	// the major layer index was found in the temporary map. Now
                	// look for the sub-layer index (nmap[2]) in the temporary
                	// map.
                	
                    eee = ee.getValue().getEntry(nmap[2]);
                    
                    // if the sub-layer index is not in the temporary map then
                    // this is a non-represented node. Associate the global active
                    // node index (i) with the next non-represented node index (sze) and
                    // increment sze to be ready for the next entry
                    
                    if (eee == null)
                        aActiveNodeMap.put(i, sze++);
                    else {
                    	
                    	// the sub-layer index was found in the temporary map.
                    	// This global active node index (i) is true entry in
                    	// the covariance matrix, and the covariance matrix
                    	// column is stored in eee.getValue(). Associate the
                    	// global active node index, i, with the covariance
                    	// matrix column index, eee.getValue().
                    	
                        aActiveNodeMap.put(i, eee.getValue());
                    }
                }
            }
        }

        // output completion and exit

        if (aScrnWrtr.isOutputOn()) {
            s = "    Tomography GeoModel Active Node Count        = " + glblActiveNodeCount + NL +
                    "    Covariance Matrix Represented Node Count     = " + aRepNodeCount + NL +
                    "    Covariance Matrix Non-Represented Node Count = " + glblNonRepNodeCount +
                    NL + NL;
            aScrnWrtr.write(s);
        }
    }
    
    /**
     * Builds the tomographic GeoTessModel. The model is constructed with a
     * global active node region, which is required to manage non-represented
     * nodes properly.
     *
     * @throws IOException
     * @throws GMPException
     */
    private void buildTomographicGeoModel() throws IOException {
        String s, f;

        // get GeoModel tomography strings and load tomography GeoModel

        aGeoModelTomoPath = aProps.getPropertyPath("geoModelTomographyPath", "").trim();
        aGeoModelTomoFileName = aProps.getProperty("geoModelTomographyFileName",
                "").trim();

        if (aScrnWrtr.isOutputOn()) {
            s = NL + "  Reading Tomography GeoModel = \"" + aGeoModelTomoPath +
                    File.separator + aGeoModelTomoFileName + "\"" + NL;
            aScrnWrtr.write(s);
        }

        f = aGeoModelTomoPath + File.separator + aGeoModelTomoFileName;
        aGeoModelTomo = new GeoTessModelGMP(f);
        aGeoModelTomo.setActiveRegion();
        s = Globals.prependLineHeader(aGeoModelTomo.toString(), "    ");
        aScrnWrtr.write(NL + s + NL);
    }

    /**
     * Builds a unique source list from the source GeoTessModel and returns the
     * list to the caller.
     */
    private ArrayList<Source> getModelSources(ModelInterface mdl) throws GMPException {
        // get number of entries/sources and create the list

        int n = mdl.getPointCount();
        ArrayList<Source> sourceList = new ArrayList<Source>(n);

        // loop over all entries in the list
        for (int i = 0; i < n; i++) {
            double[] vertex = mdl.getPointUnitVector(i);
            double radius = mdl.getPointRadius(i);

            Source src = new Source(i, -1, new GeoVector(vertex, radius), 0.0, 0.0);
            sourceList.add(src);
        }

        // done ... return list
        return sourceList;
    }

    /**
     * Defines all receivers read from the input properties file.
     *
     * @return The list of all receivers/sites
     * @throws IOException
     * @throws GMPException
     */
    private ArrayList<Receiver> getPropertiesFileReceivers() throws IOException,
            GMPException {
        // throw error if property "receiverDefinitionList" is empty or not defined
        String s = aProps.getProperty("receiverDefinitionList", "");
        if (s.equals("")) {
            s = "Error: property \"receiverDefinitionList\" is empty ..." + NL;
            throw new IOException(s);
        }

        // get receiver definitions and create list to contain them
        String[] receivers = Globals.getTokens(s, ";");
        ArrayList<Receiver> receiverList = new ArrayList<Receiver>(receivers.length);

        // loop over all entries in the list
        //aScrnWrtr.write("    Property File Receiver Definitions " +
        //                "{id, lat(deg), lon(deg), depth(km)} ..." + NL);
        //String frmt = "      %6d  position <lat, lon, depth> = <%8.3f, %8.3f, %8.3f>";
        for (int i = 0; i < receivers.length; ++i) {
            String[] r = Globals.getTokens(receivers[i], "\t, ");
            if (r.length != 10) {
                s = "Error: properties file receiver position \"" + receivers[i] +
                        "\" requires 10 entries ...";
                throw new IOException(s);
            }

            // create receiver and add to list
            Receiver rec = new Receiver(r[0], Integer.valueOf(r[1]), Integer.valueOf(r[2]),
                    Double.valueOf(r[3]), Double.valueOf(r[4]), Double.valueOf(r[5]),
                    r[0], r[6], r[7], Double.valueOf(r[8]), Double.valueOf(r[9]));
            //aScrnWrtr.write(String.format(frmt, i, src.getGeocentricLatDegrees(),
            //                              src.getLonDegrees(), src.getDepth()) + NL);
            receiverList.add(rec);
        }

        // done ... return list

        return receiverList;
    }

    /**
     * Defines all sources read from the input properties file.
     *
     * @return The list of all sources.
     * @throws IOException
     * @throws GMPException
     */
    private ArrayList<Source> getPropertiesFileSources() throws IOException,
            GMPException {
        // throw error if property "sourceDefinitionList" is empty or not defined

        String s = aProps.getProperty("sourceDefinitionList", "");
        if (s.equals("")) {
            s = "Error: property \"sourceDefinitionList\" is empty ..." + NL;
            throw new IOException(s);
        }

        // get source definitions and create list to contain them

        String[] sources = Globals.getTokens(s, ";");
        ArrayList<Source> sourceList = new ArrayList<Source>(sources.length);

        // loop over all entries in the list

        aScrnWrtr.write("    Property File Source Definitions " +
                "{id, lat(deg), lon(deg), depth(km)} ..." + NL);
        for (int i = 0; i < sources.length; ++i) {
            String[] pstn = Globals.getTokens(sources[i], "\t, ");
            if (pstn.length != 3) {
                s = "Error: properties file source position \"" + sources[i] +
                        "\" requires 3 entries ..." + NL +
                        "       (lat (deg), lon (deg), depth (km)) ..." + NL;
                throw new IOException(s);
            }

            // create source position and add to list

            GeoVector gv = new GeoVector(Double.valueOf(pstn[0]),
                    Double.valueOf(pstn[1]),
                    Double.valueOf(pstn[2]), true);
            Source src = new Source(i, -1, gv, 0.0, 0.0);
//            aScrnWrtr.write(String.format("      %6d  position <lat, lon, depth> = <%8.3f, %8.3f, %8.3f>%n", 
//            		i, src.getGeocentricLatDegrees(), src.getLonDegrees(), src.getDepth()));
            sourceList.add(src);
        }

        // done ... return list

        return sourceList;
    }

// Database functionality commented out by sballar 2022-09-27 because there is no requitement 
// for it and it introduces many dependencies that are challenging to manage.
//    /**
//     * Primary function to build observations read from the database. This
//     * function also defines the phase -> receiver -> PhaseSiteProcessData map,
//     * the global observation list, the receiver list and map, the source list
//     * and map, the phase list, and the site pair list. This function is called
//     * once at the beginning of function solve() if the source definition is
//     * DATABASE.
//     * @throws Exception 
//     *
//     * @throws GeoTomoException
//     */
//    private void buildDatabaseObservations() throws Exception {
//        String s;
//        AttributeIndexEntries attrIndexEntries;
//        HashMapLongKey<HashSetLong> rcvrSrcMap;
//
//        // create source and receiver point index -> weight maps to find influence
//        // point indexes (weights are ignored here) about each considered receiver
//        // and source point
//
//        HashMap<Integer, Double> weightsRB = new HashMap<Integer, Double>();
//        HashMap<Integer, Double> weightsR = new HashMap<Integer, Double>();
//        HashMap<Integer, Double> weightsS = new HashMap<Integer, Double>();
//
//        // create source limit spherical cap center if source limit is defined
//
//        GeoVector gvc = null;
//        if (aDebugSourceLimit) gvc = new GeoVector(aDebugSourceLat, aDebugSourceLon,
//                0.0, true);
//
//        String fn = "DATABASE";
//
//        // build global observation list, phase/receiver/observation list, the
//        // influence point receiver and source maps (aRcvrXMap and aSrcXMap), the
//        // model -> rcvr id -> src id map and model indexer map which will be used
//        // during the interpolation phase, and the influence point site pair map
//        // which will be used to build the ray uncertainty tasks.
//
//        aObservationList = new ArrayList<Observation>(65536);
//        aInputObservationMap = new ObservationsPhaseSiteMap();
//        aRcvrXMap = new HashMap<Integer, Receiver>();
//        aSrcXMap = new HashMap<Integer, Source>();
//        aModelRcvrSrcMap = new HashMap<String, HashMapLongKey<HashSetLong>>();
//        aModelIndexer = new HashMap<String, AttributeIndexEntries>();
//        aSitePairX = new HashMap<Receiver, HashSet<Receiver>>();
//
//        // create a new IODBRayUnc object to read observations from the database
//
//        VectorGeo.earthShape = EarthShape.valueOf(
//                aProps.getProperty("earthShape", "WGS84"));
//
//        IODBRayUnc iodbRayUnc = new IODBRayUnc(aProps, aScrnWrtr);
//        iodbRayUnc.getScreenWriterOutput().setWriter(aScrnWrtr.getWriter());
//        iodbRayUnc.getScreenWriterOutput().setScreenAndWriterOutputOn();
//
//        // output start message
//
//        if (aScrnWrtr.isOutputOn()) {
//            s = "  Defining Database Sources ..." + NL + NL;
//            aScrnWrtr.write(s);
//        }
//        aScrnWrtr.setIndent("  ");
//
//        // Create phase set and receiver name sets to limit the data the input
//        // observations read from the database (if they are defined).
//
//        ArrayList<SeismicPhase> phList = getInputPhases();
//        HashSet<SeismicPhase> phSet = null;
//        if ((phList != null) && (phList.size() > 0))
//            phSet = new HashSet<SeismicPhase>(phList);
//        ArrayList<Receiver> rcvrList = getInputSites();
//        HashSet<String> rcvrNameSet = null;
//        if ((rcvrList != null) && (rcvrList.size() > 0)) {
//            rcvrNameSet = new HashSet<String>(2 * rcvrList.size());
//            for (Receiver r : rcvrList) rcvrNameSet.add(r.getSta());
//        }
//
//        // create the observation map (observation id associated with observation)
//        // create source map (source id associated with source)
//        // create receiver map (receiver id associated with receiver)
//        // create phase/Receiver/Observation map (phase associated with receiver
//        // associated with an observation set)
//
//        // get unique receiver ids and gt levels in the database
//
//        aTotDBObs = iodbRayUnc.getObservationsCountFromDatabase(null);
//        iodbRayUnc.getUniqueReceiverIds();
//        int mapSize = (int) (4 * aTotDBObs / 3);
//        ArrayList<Observation> observationList = new ArrayList<Observation>((int) aTotDBObs);
//        aSourceMap = new HashMap<Long, Source>(mapSize / 10);
//        aReceiverMap = new HashMap<Long, Receiver>(mapSize / 10);
//        ObservationsPhaseSiteMap inputObservationMap = new ObservationsPhaseSiteMap();
//
//        // get observations
//
//        iodbRayUnc.readObservationsFromDatabase(phSet, rcvrNameSet,
//                aReceiverMap, aSourceMap,
//                inputObservationMap,
//                observationList, null);
//
//        // build phase list
//        SeismicPhase[] phArray = inputObservationMap.getPhases();
//        aPhaseList = new ArrayList<SeismicPhase>(phArray.length);
//        for (int i = 0; i < phArray.length; ++i) aPhaseList.add(phArray[i]);
//
//        // build receiver list
//
//        aReceiverList = new ArrayList<Receiver>(aReceiverMap.size());
//        for (Map.Entry<Long, Receiver> e : aReceiverMap.entrySet())
//            aReceiverList.add(e.getValue());
//
//        // build source list
//
//        aSourceList = new ArrayList<Source>(aSourceMap.size());
//        for (Map.Entry<Long, Source> e : aSourceMap.entrySet())
//            aSourceList.add(e.getValue());
//
//        // get site pairs
//
//        aSitePair = getInputSitePairs();
//
//        // build the source file map (all receivers assigned to fn) and debug rays
//        // if any were requested
//
//        buildSourceFileMap();
//        buildDebugRays();
//
//        attrIndexEntries = new AttributeIndexEntries();
//        aModelIndexer.put(fn, attrIndexEntries);
//
//        // get the list of receivers associated with the source file and create
//        // the models rcvr id -> src id association map
//
//        rcvrSrcMap = new HashMapLongKey<HashSetLong>();
//        aModelRcvrSrcMap.put(fn, rcvrSrcMap);
//
//        HashMap<Long, ObservationsSourceMap> siteIdMap;
//        for (int i = 0; i < aPhaseList.size(); ++i) {
//            SeismicPhase phase = aPhaseList.get(i);
//            siteIdMap = inputObservationMap.getSiteIdMap(phase);
//            for (Map.Entry<Long, ObservationsSourceMap> e : siteIdMap.entrySet()) {
//                long rcvrId = e.getKey();
//                ObservationsSourceMap osm = e.getValue();
//                ArrayList<Source> srcList = new ArrayList<Source>(osm.size());
//                for (int j = 0; j < osm.size(); ++j)
//                    srcList.add(aSourceMap.get(osm.get(j).getSourceId()));
//
//                Receiver rcvrA = aReceiverMap.get(rcvrId);
//                buildInfluenceObservationList(phase, rcvrA, rcvrSrcMap, srcList,
//                        null, weightsRB, weightsR,
//                        weightsS, gvc, fn);
//            }
//        }
//
//        // done ... output message and exit
//
//        aScrnWrtr.setIndent("");
//        if (aScrnWrtr.isOutputOn()) {
//            s = "  Loaded Database Observations:" + NL +
//                    "    Total Phases                                  = " +
//                    aPhaseList.size() + NL +
//                    "    Total Receivers                               = " +
//                    aReceiverMap.size() + NL +
//                    "    Total Receivers Pairs                         = " +
//                    aSitePair.size() + NL +
//                    "    Total Sources                                 = " +
//                    aSourceMap.size() + NL +
//                    "    Total Observations                            = " +
//                    observationList.size() + NL +
//                    "    Total Tomography Influence Receiver Positions = " +
//                    aRcvrXMap.size() + NL +
//                    "    Total Tomography Influence Source Positions   = " +
//                    aSrcXMap.size() + NL +
//                    "    Total Tomography Influence Site Pairs         = " +
//                    aSitePairX.size() + NL +
//                    "    Total Tomo. Influence Observations Created    = " +
//                    aObservationList.size() + NL;
//            aScrnWrtr.write(s);
//        }
//    }

    /**
     * Builds the source file map (aSourceFileMap) from the properties
     * "sourceModelPath" and "sourceModelFile". On exit the source file map
     * contains a map of receiver A id associated with a map of receiver B id
     * associated with a valid file name within which the uncertainty results
     * are to be written for that receiver pair (note receiverA id may = receiver
     * B id). If a file is not found a FileNotFoundError is thrown.
     * <p>
     * The source file map is used to construct the set of tomography model
     * observations to be predicted and subsequently evaluated for uncertainty,
     * and again when those uncertainties are interpolated back onto the original
     * source/receiver positions within the source files. After the variances are
     * assigned to the source model files they are written back to disk.
     *
     * @throws IOException
     */
    private void buildSourceFileMap() throws IOException {
        String s;

        // make source file map

        aRcvrSourceFileMap = new HashMap<Long, String>();

        if ((aSourceDefinition == SourceDefinition.PROPERTIESFILE) ||
                (aSourceDefinition == SourceDefinition.DATABASE)) {
            // sources are read from a properties file. build source file map for
            // all receivers from receiver list associated with "PROPERTIESFILE" as
            // the file name

            for (int i = 0; i < aReceiverList.size(); ++i) {
                Receiver rcvr = aReceiverList.get(i);
                long rcvrId = rcvr.getReceiverId();

                String name = aRcvrSourceFileMap.get(rcvrId);
                if (name == null)
                    aRcvrSourceFileMap.put(rcvrId, aSourceDefinition.name());
            }
        } else {
            // read "sourceModelPath" and "sourceModelFile" properties to find
            // source file -> receiver associations.

            String singleFile = aDefaultSourceFileName;
            String[] tokens = null;
            String filePath = aProps.getPropertyPath("sourceModelPath", "").trim();
            aSourcePath = filePath;
            if (!filePath.equals("")) filePath += File.separator;

            // read "sourceModelFile" property and see if it is empty or composed of
            // a single file name or one or more filename -> reciver list associations.

            s = aProps.getProperty("sourceModelFile", "").trim();
            if (!s.equals("")) {
                // not empty see if more than one filename -> receiver list associations
                // are defined

                tokens = Globals.getTokens(s, ";");
                if (tokens.length == 1) {
                    // one entry ... see if it a single filename or a single filename ->
                    // receiver list association

                    String[] entries = Globals.getTokens(tokens[0], "\t ,");
                    if (entries.length == 1) {
                        // single file name entry ... set entry and nullify tokens

                        singleFile = entries[0];
                        tokens = null;
                    }
                }
            }

            // build a map of valid receiver names associated with their receiver
            // objects

            HashMap<String, Receiver> validRcvrNames;
            validRcvrNames = new HashMap<String, Receiver>();
            for (int i = 0; i < aReceiverList.size(); ++i) {
                Receiver rcvr = aReceiverList.get(i);
                validRcvrNames.put(rcvr.getSta(), rcvr);
            }

            // build aSourceFileMap depending on input type for property "sourceModelFile"

            if (tokens != null) {
                // arbitrary file -> receiver list associations given in tokens ...
                // build aSourceFileMap from input file -> receiver associations
                // loop over each subsequent entry

                for (int i = 0; i < tokens.length; ++i) {
                    // get filename and associated receiver list ... append filePath and
                    // extension to file name and test for validity ... loop over all
                    // associated receiver names (or pairs).

                    String[] entries = Globals.getTokens(tokens[i], " ," + '\t');
                    String name = getPatternedName(filePath, "", entries[0], "." + aGeoTessModelFileExt, -1);
                    for (int j = 1; j < entries.length; ++j) {
                        // get receiver pair associated with entry j and test for validity

                        Receiver rcvr = validRcvrNames.get(entries[j]);
                        if (rcvr != null) {
                            // found valid name ... remove it from valid map and add to the
                            // aSourceFileMap definition

                            validRcvrNames.remove(entries[j]);
                            aRcvrSourceFileMap.put(rcvr.getReceiverId(), name);
                        } else {
                            // error ... entries[j] is not a valid name

                            s = "Unknown receiver \"" + entries[j] + "\" or receiver has" +
                                    " already been assigned to another source file ...";
                            throw new IOException(s);
                        }
                    }
                }
            }

            // done see if any are remaining and add associated with a single file
            // each

            if (validRcvrNames.size() > 0) {
                String name0 = "", name = "";
                HashSet<Receiver> processedRcvrSet = new HashSet<Receiver>();

                // some (or all) receiver names have not been assigned ... use the
                // singleFile definitions single file or patterned "*" ... see if
                // patterned ... if not then assign name0 ... loop over all remaining
                // entries in the valid receiver names map

                int strt = singleFile.indexOf("*");
                if (strt == -1)
                    name0 = getPatternedName(filePath, name0, singleFile, "." + aGeoTessModelFileExt, strt);
                for (Map.Entry<String, Receiver> e : validRcvrNames.entrySet()) {
                    // get A and B receivers and ids

                    Receiver rcvrA = e.getValue();
                    if (!processedRcvrSet.contains(rcvrA)) {
                        Receiver rcvrB = aSitePair.get(rcvrA);
                        long rcvrAId = rcvrA.getReceiverId();

                        // assign name to name0 if not patterned ... otherwise build name from
                        // receiver name, if both are identical, or a joined hyphen of both
                        // receiver names, if they are different

                        if (strt == -1)
                            name = name0;
                        else {
                            name = rcvrA.getSta();
                            if (rcvrB != null) name += "_" + rcvrB.getSta();
                            name = getPatternedName(filePath, name, singleFile, "." + aGeoTessModelFileExt, strt);
                        }

                        // add rcvrA id to map associated with file name ... do the same
                        // for rcvrB if not null

                        processedRcvrSet.add(rcvrA);
                        aRcvrSourceFileMap.put(rcvrAId, name);
                        if (rcvrB != null) {
                            processedRcvrSet.add(rcvrB);
                            aRcvrSourceFileMap.put(rcvrB.getReceiverId(), name);
                        }
                    }
                }
            }
        }

        // done ... now build inverse unique source file name to the set of
        // receiver ids that it supports

        aUniqueSourceFileRcvrMap = new HashMap<String, HashSet<Long>>();
        for (Map.Entry<Long, String> e : aRcvrSourceFileMap.entrySet()) {
            long rcvrId = e.getKey();
            String fileName = e.getValue();

            HashSet<Long> hs = aUniqueSourceFileRcvrMap.get(fileName);
            if (hs == null) {
                hs = new HashSet<Long>();
                aUniqueSourceFileRcvrMap.put(fileName, hs);
            }
            hs.add(rcvrId);
        }
    }

    /**
     * Called only by function buildSourceFileMap(...) to construct a patterned
     * source model name. If a substitution pattern ("*") is in the file name it
     * will be replaced by "name". So the final file name, given a "*" in sfil,
     * will be
     * <p>
     * pth + sfilFront + name + sfilBack + ext
     * <p>
     * where sfilFront is the text in front of "*" and sfilBack is the text behind
     * "*". If there is no "*" in sfil (ptrnStrt == -1) then the final name is
     * defined as
     * <p>
     * pth + sfil + ext
     * <p>
     * where "name" is not used.
     * <p>
     * Regardless of the final name it is tested for existence and an error is
     * thrown (IOException) if the file does not exist. If successful the final
     * assembled file name is returned to the caller.
     *
     * @param pth      The path to the file name.
     * @param name     The name substituted for "*" if "*" is found in sfil.
     * @param sfil     The primary file name with a possible pattern insertion
     *                 character ("*") within.
     * @param ext      The file extension of the form ".ext".
     * @param ptrnStrt The position of "*" in sfil. Equals -1 if no "*" is
     *                 contained in sfil.
     * @return The final assembled file name.
     * @throws IOException
     */
    private String getPatternedName(String pth, String name, String sfil,
                                    String ext, int ptrnStrt) throws IOException {
        String s;

        // assemble the substituted file name if ptrnStr > 1 or just sfil + ext
        // otherwise

        if (ptrnStrt == -1)
            s = sfil + ext;
        else
            s = sfil.substring(0, ptrnStrt) + name +
                    sfil.substring(ptrnStrt + 1) + ext;

        // prepend the path to the file name and test for existence. Throw an error
        // if the file does not exist

        s = pth + s;
        File f = new File(s);
        if (!f.exists()) {
            s = "File: " + s + " Does not exist ...";
            throw new FileNotFoundException(s);
        }

        // return the assembled file name

        return s;
    }

    /**
     * The primary function used to construct observations from input GeoModel,
     * GeoTessModel source files, or the properties file. This function also
     * defines the phase -> receiver -> PhaseSiteProcessData map, the global
     * observation list, the receiver list and map, the source list and map, the
     * phase list, and the site pair list. The source list is cleared and rebuilt
     * for each consecutive source model read from disk. This function is called
     * once at the beginning of function solve() if the source definition is
     * anything but DATABASE.
     *
     * @throws IOException
     * @throws GMPException
     * @throws GeoTessException
     */
    private void buildNonDatabaseObservations() throws IOException, GMPException, GeoTessException {
        String s;
        HashMapLongKey<HashSetLong> rcvrSrcMap;
        AttributeIndexEntries attrIndexEntries;

        // create source and receiver point index -> weight maps to find influence
        // point indexes (weights are ignored here) about each considered receiver
        // and source point

        HashMap<Integer, Double> weightsRB = new HashMap<Integer, Double>();
        HashMap<Integer, Double> weightsR = new HashMap<Integer, Double>();
        HashMap<Integer, Double> weightsS = new HashMap<Integer, Double>();

        // output starting message

        if (aScrnWrtr.isOutputOn()) {
            if (aSourceDefinition == SourceDefinition.GEOMODEL)
                s = NL + "  Start GeoModel Source Definition " +
                        Globals.repeat("#", 45) + NL;
            else if (aSourceDefinition == SourceDefinition.GEOTESSMODEL)
                s = NL + "  Start GeoTessModel Source Definition " +
                        Globals.repeat("#", 41) + NL;
            else
                s = NL + "  Start Property File Source Definition " +
                        Globals.repeat("#", 40) + NL;
            aScrnWrtr.write(s);
        }

        // read phase, site, and site pair lists

        aPhaseList = getInputPhases();
        aReceiverList = getInputSites();
        aReceiverMap = new HashMap<Long, Receiver>();
        for (int i = 0; i < aReceiverList.size(); ++i) {
            Receiver rcvr = aReceiverList.get(i);
            aReceiverMap.put(rcvr.getReceiverId(), rcvr);
        }
        aSitePair = getInputSitePairs();

        // create source limit spherical cap center if source limit is defined

        GeoVector gvc = null;
        if (aDebugSourceLimit) gvc = new GeoVector(aDebugSourceLat, aDebugSourceLon,
                0.0, true);

        // get sources and build source map and debug rays if any were requested

        buildSourceFileMap();
        buildDebugRays();

        // build global observation list, phase/receiver/observation list, the
        // influence point receiver and source maps (aRcvrXMap and aSrcXMap), the
        // model -> rcvr id -> src id map and model indexer map which will be used
        // during the interpolation phase, and the influence point site pair map
        // which will be used to build the ray uncertainty tasks.

        aObservationList = new ArrayList<Observation>(65536);
        aInputObservationMap = new ObservationsPhaseSiteMap();
        aRcvrXMap = new HashMap<Integer, Receiver>();
        aSrcXMap = new HashMap<Integer, Source>();
        aModelRcvrSrcMap = new HashMap<String, HashMapLongKey<HashSetLong>>();
        aModelIndexer = new HashMap<String, AttributeIndexEntries>();
        aSitePairX = new HashMap<Receiver, HashSet<Receiver>>();

        // create model receiver -> source map and loop over and process all models
        // this is the outer most loop used to create the observations.

        aModelSourceCount = 0;
        for (Map.Entry<String, HashSet<Long>> e : aUniqueSourceFileRcvrMap.entrySet()) {
            // load next model and get its source list array. If the sources come
            // from the properties file their id is the same as their entry index in
            // the properties file. The first entry is 0, the second 1, and so on. If
            // the model sources come from a GeoModel or GeoTessModel file then the
            // id of the returned source is the same as the point index on the source
            // models grid for which the source was created (i.e source grid point
            // index = source id).

            ArrayList<Source> mdlSources;
            String fn = e.getKey();

            // get the list of receivers associated with the source file and create
            // the models rcvr id -> src id association map

            HashSet<Long> modelRcvrIds = e.getValue();
            rcvrSrcMap = new HashMapLongKey<HashSetLong>();
            aModelRcvrSrcMap.put(fn, rcvrSrcMap);

            attrIndexEntries = new AttributeIndexEntries();
            aModelIndexer.put(fn, attrIndexEntries);

            if (aSourceDefinition == SourceDefinition.PROPERTIESFILE) {
                // source comes from properties file ... attribute indexer is not
                // needed (i.e. set to null).

                mdlSources = getPropertiesFileSources();
                aSourceList = mdlSources;
                aSourceMap = new HashMap<Long, Source>(2 * aSourceList.size());
                for (int i = 0; i < aSourceList.size(); ++i) {
                    Source src = aSourceList.get(i);
                    aSourceMap.put(src.getSourceId(), src);
                }
            } else {
                // source comes from GeoModel or GeoTessModel source file ... create a
                // new attribute indexer and add to map ... load the model and retrieve
                // its source list.

                ModelInterface mdl = loadModel(fn, "  ", false);
                mdlSources = getModelSources(mdl);
            }
            aModelSourceCount += mdlSources.size();

            // now loop over all requested phases

            for (int k = 0; k < aPhaseList.size(); ++k) {
                // get the next phase and the associated site process data map

                SeismicPhase phase = aPhaseList.get(k);

                // set active node attributes in global tomography model based on phase

                WaveType waveType = phase.getWaveType();
                if (waveType != null)
                    aGeoModelTomo.setActiveRegion();
                else {
                    s = "Error: Unsupported phase \"" + phase + "\"" + NL;
                    throw new IOException(s);
                }

                // loop over all receivers defined by the current model

                for (long rcvrId : modelRcvrIds) {
                    // get receiver A and build influence observation map and list
                    // from the input model sources (mdlSources) for this phase/receiver

                    Receiver rcvrA = aReceiverMap.get(rcvrId);
                    buildInfluenceObservationList(phase, rcvrA, rcvrSrcMap, mdlSources,
                            attrIndexEntries, weightsRB, weightsR,
                            weightsS, gvc, fn);
                } // end for (long rcvrId: modelRcvrIds)
            } // end for (int k = 0; k < aPhaseList.size(); ++k)
        } // end for (Map.Entry<String, HashSet<Long>> e: aUniqueSourceFileMap.entrySet())

        // Note: aInputObservationMap is now map of phase->rcvrX->srcX

        aScrnWrtr.write("    Source Definition Summary: " + NL);
        aScrnWrtr.write("      Source Model Files Read                         = " +
                aUniqueSourceFileRcvrMap.size() + NL);
        aScrnWrtr.write("      Sources Tested for Validity                     = " +
                aModelSourceCount + NL);
        aScrnWrtr.write("      Phases Defined                                  = " +
                aPhaseList.size() + NL);
        aScrnWrtr.write("      Receivers Defined                               = " +
                aReceiverMap.size() + NL);
        aScrnWrtr.write("      Receivers Pairs Defined                         = " +
                aSitePair.size() + NL);
        aScrnWrtr.write("      Tomography Influence Receiver Positions Defined = " +
                aRcvrXMap.size() + NL);
        aScrnWrtr.write("      Tomography Influence Source Positions Defined   = " +
                aSrcXMap.size() + NL);
        aScrnWrtr.write("      Tomography Influence Site Pairs Defined         = " +
                aSitePairX.size() + NL);
        aScrnWrtr.write("      Observations Created                            = " +
                aObservationList.size() + NL);
        s = NL + "  End Source Definition " +
                Globals.repeat("#", 56) + NL;
        aScrnWrtr.write(s);
    }

    /**
     * Accumulates influence observation entries in the observation map
     * (aInputObservationMap) and list (aObservationList) for the specific phase
     * and receiver A input into this function. This function also accumulates
     * entries for the input receiver source map (rcvrSrcMap) for the input
     * receiver A with all discovered valid source ids contained in mdlSources.
     * <p>
     * The construction of influence observations begins by finding all tomography
     * grid influence points for the input receiver. These are looped over in a
     * consecutive fashion. For each, if the receiver is associated with a site
     * pair (aSitePair) B receiver then those influence points on the tomography
     * grid are also found and added to the influence site pair list (aSitePairX)
     * associated with the current site A influence point in the loop.
     * <p>
     * Then, each model source is processed in a loop for the current receiver
     * influence point. The source is processed to obtain its tomography grid
     * influenced points. Each of those, in turn, are processed in a loop to
     * determine if they are valid (influence source and influence receiver are
     * within 95 degrees of one another). If they are and that influence source/
     * influence receiver have not yet been added as an observation by some other
     * source/receiver loop entry, then they are added now. If the input
     * attribute indexer is not null then it is also updated at this time with
     * the new observation.
     * <p>
     * This looping continues until all influence sources are processed for a
     * specific model source. Then the next model source in the loop is obtained
     * and the process is repeated. Once all model sources have been processed
     * the next influence receiver A point is processed again with all input
     * model sources. Finally, once all influence receiver A points have been
     * processed the function exits.
     *
     * @param phase            The input phase for which all observations will be
     *                         constructed.
     * @param rcvrA            The input receiver for which all observations will
     *                         be constructed.
     * @param rcvrSrcMap       Receiver source map that associates all valid
     *                         sources with the input receiver. Used in the
     *                         interpolation phase following the ray uncertainty
     *                         calculation.
     * @param mdlSources       Model source list for this receiver and phase.
     * @param attrIndexEntries AttributeIndexer and map and list. This is null
     *                         when sources are extracted from the database or the
     *                         properties file.
     * @param weightsRB        Temporary storage for all returned Receiver B
     *                         influence points and weights.
     * @param weightsR         Temporary storage for all returned Receiver A
     *                         influence points and weights.
     * @param weightsS         Temporary storage for all source influence points
     *                         and weights.
     * @param gvc              Spherical cap debug vector ... generally null.
     * @param fn               Model file name or "PROPERTIESFILE", or
     *                         "DATABASE".
     * @throws IOException
     * @throws GMPException
     * @throws GeoTessException
     */
    private void buildInfluenceObservationList(SeismicPhase phase, Receiver rcvrA,
                                               HashMapLongKey<HashSetLong> rcvrSrcMap,
                                               ArrayList<Source> mdlSources,
                                               AttributeIndexEntries attrIndexEntries,
                                               HashMap<Integer, Double> weightsRB,
                                               HashMap<Integer, Double> weightsR,
                                               HashMap<Integer, Double> weightsS,
                                               GeoVector gvc, String fn)
            throws IOException, GMPException, GeoTessException {
        ObservationsSourceMap procData;

        // set dummy no smoothing flag

        boolean[] noSmoothing = {false, false, false};

        // get receiver A id and rcvrB if defined as a site pair

        long rcvrId = rcvrA.getReceiverId();
        Receiver rcvrB = aSitePair.get(rcvrA);
        HashSetLong srcIds = null;

        // get map of points from tomography model that influence this
        // receivers (rcvrA) position ... loop over each and create pseudo
        // receivers

        getPositionInfluencePoints(aGeoModelTomo, rcvrA, phase, weightsR);
        int ixa = 0;
        for (Map.Entry<Integer, Double> eRA : weightsR.entrySet()) {
            // get the influence Receiver from the influence point index
            // (eRA.getKey()) and set its receiver id into rcvrXId

            Receiver rcvrXA = getInfluenceReceiver(eRA.getKey(), ixa, rcvrA);
            long rcvrXId = rcvrXA.getReceiverId();
            ++ixa;

            // associate rcvrX (site A) with all rcvrB influence points to aSitePairX
            // if rcvrB is not null

            if (rcvrB != null) {
                // get rcvrB influence points and loop over each

                getPositionInfluencePoints(aGeoModelTomo, rcvrB, phase, weightsRB);
                int ixb = 0;
                for (Map.Entry<Integer, Double> eRB : weightsRB.entrySet()) {
                    // get the influence Receiver from the influence point index
                    // (eRB.getKey()) and associate the site A influence point
                    // (rcvrX) with the site B influence point (rcvrXB) and continue
                    // to next point

                    Receiver rcvrXB = getInfluenceReceiver(eRB.getKey(), ixb, rcvrB);
                    if (rcvrXB != rcvrXA) {
                        HashSet<Receiver> bRcvrSet = aSitePairX.get(rcvrXA);
                        if (bRcvrSet == null) {
                            bRcvrSet = new HashSet<Receiver>();
                            aSitePairX.put(rcvrXA, bRcvrSet);
                        }
                        bRcvrSet.add(rcvrXB);
                    }
                    ++ixb;
                }
            }

            // have influence receiver now get observation map associated with
            // the current phase and influence point receiver id (rcvrXA) and
            // loop over all source points

            procData = aInputObservationMap.getSet(phase, rcvrXId);
            for (int j = 0; j < mdlSources.size(); ++j) {
                // get the next source and its id (same as the source grid point
                // index. Find all influence points associated with the source and
                // loop over each

                Source src = mdlSources.get(j);
                long srcId = src.getSourceId();
                getPositionInfluencePoints(aGeoModelTomo, src, phase, weightsS);
                for (Map.Entry<Integer, Double> eS : weightsS.entrySet()) {
                    // get the influence Source from the input influence point index
                    // (eS.getKey()) and set its source id into srcXId

                    Source srcX = getInfluenceSource(eS.getKey());
                    long srcXId = srcX.getSourceId();

                    // have influence source and receiver ... make observation ...
                    // first get distance between the two and see if they are less
                    // than the 95.0 deg limit

                    double d = VectorUnit.angleDegrees(rcvrXA.getUnitVector(),
                            srcX.getUnitVector());

                    if ((d > phase.approximateMinimumDistanceLimitDegrees()) &&
                            (d < phase.approximateMaximumDistanceLimitDegrees())) {
                        // still ok ... now see if debug source liasdfmit is defined. Test
                        // source to see if it lies in the debug spherical cap
                        // definition if limit is defined

                        boolean addSrc = true;
                        if (aDebugSourceLimit) {
                            d = VectorUnit.angleDegrees(gvc.getUnitVector(),
                                    srcX.getUnitVector());
                            if (d > aDebugSourceRad) addSrc = false;
                        }

                        // If source was within debug source limit then add observation

                        if (addSrc) {
                            // if srcIds is null make a new one

                            if (srcIds == null) {
                                srcIds = rcvrSrcMap.get(rcvrId);
                                if (srcIds == null) {
                                    srcIds = new HashSetLong();
                                    rcvrSrcMap.put(rcvrId, srcIds);
                                }
                            }

                            // only add observation if it is unique

                            Observation obs = procData.get(srcXId);
                            if (obs == null) {
                                // good ... create observation and add to list and map ...
                                // update debug information and add srcId to srcIds set so
                                // that it is associated as evaluated source for the
                                // current receiver (rcvr) of the current model (mdl).

                                obs = new Observation(aObservationList.size(),
                                        aObservationList.size(),
                                        procData.size(), phase, rcvrXId, srcXId);
                                aObservationList.add(obs);
                                procData.addObservation(obs);
                            }

                            // if debug check to see if any rays should be added to the
                            // aDebugRaysX container for shipment to the ray uncertainty
                            // tasks

                            if (aDebug)
                                checkDebugRay(fn, phase, src, rcvrA, rcvrB,
                                        srcXId, rcvrXId, weightsS,
                                        weightsR, weightsRB, obs);

                            // create new attribute surface for type AA and type AB
                            // rays these return without modification if the input
                            // phase/receivers have already been added.

                            if (attrIndexEntries != null) {
                                addNewAttributeSurface(phase, rcvrA, rcvrA,
                                        attrIndexEntries.aIndexer,
                                        attrIndexEntries.aMapList,
                                        attrIndexEntries.aSurfMap,
                                        noSmoothing, this);
                                if (rcvrB != null)
                                    addNewAttributeSurface(phase, rcvrA, rcvrB,
                                            attrIndexEntries.aIndexer,
                                            attrIndexEntries.aMapList,
                                            attrIndexEntries.aSurfMap,
                                            noSmoothing, this);
                            }

                            // add srcId to valid sources requiring interpolation

                            srcIds.add(srcId);
                        }
                    } // end if (d < 95.0)
                } // end for (Map.Entry<Integer, Double> eS: weightsS.entrySet())
            } // end for (int j = 0; j < mdlSources.size(); ++j)
        } // end for (Map.Entry<Integer, Double> eR: weightsR.entrySet())
    }

    /**
     * Returns the tomographic influence Source associated with the input
     * tomographic grid point index (active node index). If the source already
     * exists in the influence source map (aSrcXMap) it is simply returned.
     * Otherwise a new source is created, added to the map, and returned. If
     * a new source is created it uses the grid point index position vector as
     * the position of the source and the grid point index as the source id.
     *
     * @param pointIndex The tomographic grid point index for which a corresponding
     *                   influence source is returned.
     * @return The influence source corresponding to the input tomographic grid
     * point index.
     * @throws GMPException
     */
    private Source getInfluenceSource(int pointIndex) throws GMPException {
        // get the source from the influence source map and see if it is defined

        Source srcX = aSrcXMap.get(pointIndex);
        if (srcX == null) {
            // not yet added ... create a new source and add it to the
            // influence point source map

            GeoVector pstn = getGeoVector(aGeoModelTomo.getPointMap(), pointIndex);
            srcX = new Source(pointIndex, -2, pstn, 0.0, 0.0);
            aSrcXMap.put(pointIndex, srcX);
        }

        // return the influence source

        return srcX;
    }

    private GeoVector getGeoVector(PointMap pmap, int pointIndex) {
        GeoVector gv = new GeoVector();
        gv.setGeoVector(pmap.getPointUnitVector(pointIndex), pmap.getPointRadius(pointIndex));
        return gv;
    }

    /**
     * Returns the tomographic influence Receiver associated with the input
     * tomographic grid point index (active node index). If the receiver already
     * exists in the influence receiver map (aRcvrXMap) it is simply returned.
     * Otherwise a new receiver is created, added to the map, and returned. If
     * a new receiver is created it uses the grid point index position vector as
     * the position of the receiver and the grid point index as the receiver id,
     * and the input requested receiver (rcvr) sta() as the name with an increment
     * (ix) added in parentheses for the name.
     *
     * @param pointIndexR The tomographic grid point index for which a corresponding
     *                    influence Receiver is returned.
     * @param ix          The incremental offset added to the input requested
     *                    receiver name (rcvr.sta()) in parentheses.
     * @param rcvr        The requested receiver whose name will be used to
     *                    construct the name of this influence receiver if it has
     *                    not yet been created.
     * @return The influence receiver corresponding to the input tomographic grid
     * point index.
     * @throws GMPException
     */
    private Receiver getInfluenceReceiver(int pointIndexR, int ix,
                                          Receiver rcvr) throws GMPException {
        // get the receiver from the influence receiver map and see if it was
        // defined.

        Receiver rcvrX = aRcvrXMap.get(pointIndexR);
        if (rcvrX == null) {
            // not yet added ... create a new receiver and add it to the
            // aRcvrXMap

            GeoVector pstn = getGeoVector(aGeoModelTomo.getPointMap(), pointIndexR);
            double startTime = (new Date()).getTime() / 1000;
            double endTime = startTime + 1.0;
            String rcvrName = rcvr.getSta() + "(" + ix + ")";
            rcvrX = new Receiver(pointIndexR, rcvrName,
                    startTime, endTime, pstn);
            aRcvrXMap.put(pointIndexR, rcvrX);
        }

        // return the influence receiver

        return rcvrX;
    }

    /**
     * Returns the map of point indexes associated with their interpolation
     * weights of the set of points that influence the input position (gv).
     * This function removes any zero weight entries and only returns influence
     * weights that are non-zero.
     *
     * @param tomoModel The tomographic model used to retrieve the interpolation
     *                  at the input GeoVector (gv).
     * @param gv        The input position for which the influence weights will be
     *                  returned.
     * @param phase     The seismic phase from which the wave type (slowness) is
     *                  retrieved for purposes of performing the weight calculation.
     * @param weights   The map of weights that will contain all influencing
     *                  tomography grid points and their associated influence
     *                  weights on return (cleared before fill).
     * @throws GeoTessException
     */
    public static void getPositionInfluencePoints(GeoTessModel tomoModel,
                                                  GeoVector gv,
                                                  SeismicPhase phase,
                                                  HashMap<Integer, Double> weights) throws GeoTessException {
        weights.clear();
        GeoTessPosition pos = tomoModel.getGeoTessPosition();
        pos.set(gv.getUnitVector(), gv.getRadius());
        //pos.getWeights(weights, 1.0, gv.getRadius(), layer, InterpolatorType.LINEAR);
        pos.getWeights(weights, 1.0);

        Iterator<Map.Entry<Integer, Double>> it = weights.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Double> e = it.next();
            if (e.getValue() == 0.0) it.remove();
        }
    }

    /**
     * Performs ray prediction given an input source list.
     * @throws Exception 
     */
    private void predictRays() throws Exception {
        String s;

        // write start message

        if (aScrnWrtr.isOutputOn()) {
            s = NL + "  Start Ray Prediction " + Globals.repeat("#", 57) + NL +
                    "    Source Type: " + aSourceDefinition.name() + NL;
            aScrnWrtr.write(s);
        }

        // set start time and status of UI

        long predictionStrtTime = (new Date()).getTime();
        if (aUI != null) aUI.updateInitialStatus(RayUncertainty.SolutionPhase.PREDICTION);

        // observations per site is constant ( = aUniqueSourceList.size())

        EnumSet<GeoAttributes> reqAttr = EnumSet.noneOf(GeoAttributes.class);
        reqAttr.add(GeoAttributes.TRAVEL_TIME);
        reqAttr.add(GeoAttributes.TOMO_WEIGHTS);
        reqAttr.add(GeoAttributes.AVERAGE_RAY_VELOCITY);

        // read in max number of rays per prediction task and output prediction
        // sizes

        int numRaysPerPredTask = aProps.getInt(
                "numberOfRaysPerParallelPredictionTask", 100);
        double rayWeightSetElementLimitMult =
                aProps.getDouble("rayElementReadLimitBlockSizeMultiplier", 2.0);
        long blockRayWeightSetElementLimit = 2 * (aCovMatrixBlockDefn.blocks() + 1) *
                aCovMatrixBlockDefn.blockSize() *
                aCovMatrixBlockDefn.blockSize() / 3;
        long rayWeightSetElementLimit = (long) (rayWeightSetElementLimitMult *
                blockRayWeightSetElementLimit);

        if (aScrnWrtr.isOutputOn()) {
            s = "    Rays per Parallel Prediction Task             = " +
                    numRaysPerPredTask + NL +
                    "    Ray Weight Set Element Count Limit Multiplier = " +
                    rayWeightSetElementLimitMult + NL +
                    "    Block Ray Weight Set Element Count Limit      = " +
                    blockRayWeightSetElementLimit + NL +
                    "    Ray Weight Set Element Count Limit            = " +
                    rayWeightSetElementLimit + NL;
            aScrnWrtr.write(s);
        }

        // create phase / site / process data map and task results retreival
        // thread (GetTaskResults<PredictorParallelTaskResult>)

        PredictorParallelTaskResult tskRslt = null;

        // create ray weight directory

        File rwf = new File(aRayWghtPath);
        if (!rwf.exists()) FileDirHandler.createDirectory(aRayWghtPath);

        // loop over all phases and perform ray prediction and ray weight
        // determination for all sites defined by the aUniqueSourceList

        int minRayElemCount = Integer.MAX_VALUE;
        int maxRayElemCount = 0;
        long clientPredTime = 0;
        long minRayCalcTime = Long.MAX_VALUE;
        long maxRayCalcTime = 0;
        long prcNodePredTime = 0;
        int validRayCount = 0;
        int processRayCount = 0;
        int totalRayCount = 0;
        long totalRayElemCount = 0;
        int proccesorCountSum = 0;
        int totalTaskCount = 0;
        long rayWeightElementCount = 0;

        // set profiler if defined

        ProfilerContent predProfilerContent = null;
        if (aPredictionProfilerSamplePeriod > 0)
            predProfilerContent = new ProfilerContent();

        // create and initialize BlockArraySetList and initialize ... this object
        // is responsible for handling all ray weight file creation and output.
        // Ray Weights are written out to disk for each block. One or more sets of
        // block files will be written depending on whether the element count limit
        // is reached (1st argument in initialize(...) below). Also, if any ray
        // pairs are provided they are written to a single block set with set id
        // -1 (all other sets start at 0 and increment). Also, to detect if the
        // Ray pair set should be written the ray pairs must be added to the
        // BlockArraySetList before commencing ray prediction

        aRayWeightSetList = new RayWeightSetList();
        RayWeightSetList.initialize(rayWeightSetElementLimit, aRayWghtPath,
                aCovMatrixBlockDefn, aInputObservationMap);

        if (aSitePairX != null) {
            for (Map.Entry<Receiver, HashSet<Receiver>> e : aSitePairX.entrySet()) {
                HashSet<Receiver> bRcvrSet = e.getValue();
                for (Receiver bRcvr : bRcvrSet)
                    aRayWeightSetList.addSitePair(e.getKey().getReceiverId(),
                            bRcvr.getReceiverId());
            }
        }

        // build all prediction tasks

        ArrayList<PredictorParallelTask> predTasks;
        predTasks = buildPredictorParallelTasks(reqAttr, numRaysPerPredTask,
                aGeoModelTomo);
        totalTaskCount = predTasks.size();
        if (aUI != null) aUI.setTotalRayPredictionTasks(totalTaskCount);

        // Output submission message

        if (aScrnWrtr.isOutputOn()) {
            s = "        Submitting " + predTasks.size() +
                    " prediction tasks ..." + NL;
            aScrnWrtr.write(s);
        }

        // set each tasks submit time and submit

        Long submitStartTime = (new Date()).getTime();
        for (int m = 0; m < predTasks.size(); ++m)
            predTasks.get(m).setSubmitTime(submitStartTime);
        long submitClientTime = 0;
        aParallelBrkr.submit(predTasks);

        GetTaskResults<PredictorParallelTaskResult> taskResults =
                new GetTaskResults<PredictorParallelTaskResult>();

        // enter process loop and wait for returned results

        int taskProcessCount = predTasks.size();
        while (taskProcessCount > 0) {
            // while tasks remain check for a returned task

            //TODO for some reason this line is throwing a ClassCastException (can't cast from a RayUncertaintyTask to this kind) 
            while ((tskRslt = taskResults.getNextResult()) != null) {
                // throw error if task failed to complete

                if (tskRslt.getException() != null) {
                    // throw error

                    throw new IOException(tskRslt.getException());
                }

                // increment total prediction time

                long tottsktim = tskRslt.getTaskOutForProcessTime();
                if (submitClientTime < tottsktim) submitClientTime = tottsktim;

                // get ray count, calculation time, and set the min, max ray process
                // times.

                int pRayCount = tskRslt.getRays().size();
                long rtim = tskRslt.getCalculationTimeMSec();
                if (minRayCalcTime > rtim / pRayCount)
                    minRayCalcTime = rtim / pRayCount;
                if (maxRayCalcTime < rtim / pRayCount)
                    maxRayCalcTime = rtim / pRayCount;
                prcNodePredTime += rtim;

                // loop over all rays in result

                validRayCount = 0;
                rayWeightElementCount = 0;
                processRayCount += pRayCount;
                proccesorCountSum += aParallelBrkr.getProcessorCount();
                for (int m = 0; m < pRayCount; ++m) {
                    // get next RayInfo object (m) and associated observation from the
                    // result. Also assign receiver id and process data object.

                    int obsIndx = (int) tskRslt.getRayIndex(m);
                    Observation obs = aObservationList.get(obsIndx);
                    long rcvrid = obs.getReceiverId();
                    Prediction pi = tskRslt.getRay(m);
                    
                    //5TODO PS: Once changes have been made to Bender and to the
                    // TODO PS: observation prediction project, the index and wghts
                    // TODO PS: arrays below will have either P and S equivalents,
                    // TODO PS: or it will come with a waveType return method that
                    // TODO PS: defines when entries are either P or either S using
                    // TODO PS: a run length encode array of tuples that identifies
                    // TODO PS: segment entries and their waveType (See description
                    // TODO PS: provided in word document for changes made to
                    // TODO PS: Bender class Ray.java at method resample(...).
                    
//                    int[] indx = tskRslt.getRayWeightIndexes(m);
//                    double[] wghts = tskRslt.getRayWeights(m);
                    
                    int[] indx = Prediction.getArrayIndexes(tskRslt.getRayWeights(m));
                    double[] wghts = Prediction.getArrayWeights(tskRslt.getRayWeights(m));
                    
                    if ((indx != null) && pi.isValid()) {
                        // this is a valid ray ... increment count and output debug
                        // information (if requested)

                        ++validRayCount;
                        DebugObservation dbgobs = DebugObservation.getDebugObservation(obs);
                        if (dbgobs != null)
                            dbgobs.setRay(pi.getRayPath(), indx, wghts);

                        // convert the ray active node indices to covariance matrix column
                        // indices and update min and max ray element count

                        // TODO PS: Given an array of P and S wave type changes for the weights
                        // TODO PS: at line 3607 above one can check each index in the loop
                        // TODO PS: to see if the weight assigned is either P or S. Replace
                        // TODO PS: the line of the loop with something like
                        // TODO PS: 
                        // TODO PS: If (weight is P)
                        // TODO PS:    indx[ni] = aActiveNodeMapP.get(indx[ni]);
                        // TODO PS: Else If (weight is S)
                        // TODO PS:    indx[ni] = aActiveNodeMapS.get(indx[ni]);
                        // TODO PS:
                        // TODO PS: This will ensure that the global active node index is
                        // TODO PS: changed to either a P or S matrix column index.
                        for (int ni = 0; ni < indx.length; ++ni)
                            indx[ni] = aActiveNodeMap.get(indx[ni]);

                        if (indx.length > maxRayElemCount) maxRayElemCount = indx.length;
                        if (indx.length < minRayElemCount) minRayElemCount = indx.length;

                        // update ray weight element count

                        rayWeightElementCount += indx.length;
                    } // end if (indx != null)
                    else
                        indx = null;

                    // set ray weights into ray weight set

                    aRayWeightSetList.add(obs.getPhase(), rcvrid, obs.getSourceId(),
                            indx, wghts);
                } // end for (int m = 0; m < tskRslt.getRays().size(); ++m)

                // update ray and element counts and update UI

                totalRayCount += validRayCount;
                totalRayElemCount += rayWeightElementCount;
                if (aUI != null) aUI.updateInitialStatus(RayUncertainty.SolutionPhase.PREDICTION);
                if (aUI != null) aUI.updateRayPrediction(rtim, aParallelBrkr.getProcessorCount(),
                        processRayCount,
                        aRayWeightSetList.getPhaseSiteCount(),
                        totalRayCount, totalRayElemCount,
                        minRayElemCount, maxRayElemCount);

                // update ray weight information and update UI

                int rwsc = RayWeightSetList.getRayWeightSetsCreated();
                int rwbc = rwsc * ObservationsSourceMap.getBlockCount();
                int rwsw = RayWeightSet.getRayWeightSetsWritten();
                int rwbw = RayWeightSet.getRayWeightBlocksWritten();
                if (aUI != null) aUI.updateRayWeightFilesWritten(rwsc, rwbc, rwsw, rwbw);

                // decrement task process count and attempt to retrieve the next
                // returned task

                --taskProcessCount;

                // add profile results if defined

                ProfilerContent pc = tskRslt.getProfilerContent();
                if (pc != null) predProfilerContent.addProfilerContent(pc);
            } // end while ((tskRslt = taskResults.getNextResult()) != null)
        } // end while (taskProcessCount > 0)

        // done ... stop task results thread and set client run time.

        taskResults.stop();
        clientPredTime += submitClientTime;

        // write any remaining RayWeightSets to disk

        aRayWeightSetList.writeLast();
        aMaxRayWeightSetCount = aRayWeightSetList.getSetCount();

        // output process data map for each phase / site and dump to
        // screen if requested wait for ray weight sets to complete writing to disk
        // before continuing on

        writeInputObservationMap();
        dumpInputObservationMap();
        waitForBlockArraySetWriteCompletion();

        // output profiler content if defined

        if (predProfilerContent != null) {
            s = NL + "    Prediction Profile Output ..." + NL;
            aScrnWrtr.write(s);
            aScrnWrtr.write(predProfilerContent.getAccumulationString("      "));
            aScrnWrtr.write(NL);
        }

        // output final task information

        if (aScrnWrtr.isOutputOn()) {
            s = "    Ray Prediction Summary Results:" + NL +
                    "      Current Time                                              = " +
                    Globals.getTimeStamp() + NL +
                    "      Total Prediction Process Time                             = " +
                    Globals.elapsedTimeString(predictionStrtTime) + NL +
                    "      Client Prediction Time                                    = " +
                    Globals.elapsedTimeString(0, clientPredTime) + NL +
                    "      Cumulative  Node Prediction Time                          = " +
                    Globals.elapsedTimeString(0, prcNodePredTime) + NL +
                    "      Total Tasks Processed                                     = " +
                    totalTaskCount + NL +
                    "      Mean Processor Count                                      = " +
                    ((double) proccesorCountSum / totalTaskCount) + NL +
                    "      Parallel Acceleration Ratio                               = " +
                    ((double) prcNodePredTime / clientPredTime) + NL +
                    "      Parallel Efficiency (%)                                   = " +
                    (100.0 * prcNodePredTime / clientPredTime /
                            ((double) proccesorCountSum / totalTaskCount)) + NL +
                    "      Total Rays Predicted                                      = " +
                    processRayCount + NL +
                    "      Total Valid Rays Returned                                 = " +
                    totalRayCount + NL +
                    "      Total Ray Elements Returned                               = " +
                    totalRayElemCount + NL +
                    "      Mean Ray Calculation Time (msec)                          = " +
                    ((double) prcNodePredTime / processRayCount) + NL +
                    "      Minimum Ray Calculation Time (msec)                       = " +
                    minRayCalcTime + NL +
                    "      Maximum Ray Calculation Time (msec)                       = " +
                    maxRayCalcTime + NL +
                    "      Total Phases Processed                                    = " +
                    aPhaseList.size() + NL +
                    "      Total Receiver Count                                      = " +
                    aReceiverList.size() + NL +
                    "      Total Influence Receiver Count                            = " +
                    aRcvrXMap.size() + NL +
                    "      Total Model Source Count                                  = " +
                    aModelSourceCount + NL +
                    "      Total Influence Source Count                              = " +
                    aSrcXMap.size() + NL +
                    "      Mean Ray Elements per Ray                                 = " +
                    ((double) totalRayElemCount / totalRayCount) + NL +
                    "      Mean Ray Elements per Receiver                            = " +
                    ((double) totalRayElemCount / aReceiverList.size()) + NL +
                    "      Mean Rays per Receiver                                    = " +
                    ((double) totalRayCount / aReceiverList.size()) + NL + NL;
            aScrnWrtr.write(s);
            aScrnWrtr.write("  End Ray Prediction " + Globals.repeat("#", 59) +
                    NL + NL);
        }
    }

    /**
     * Writes the input observation map to disk.
     *
     * @throws IOException
     */
    private void writeInputObservationMap() throws IOException {
        // output message

        if (aScrnWrtr.isOutputOn()) {
            String s = NL + "    Writing Phase/Site/ProcessData map to disk ..." +
                    NL + NL;
            aScrnWrtr.write(s);
        }

        // write out maximum ray weight set count

        String fn = aIODirectory + File.separator + "maxRayWeightSetCount";
        FileOutputBuffer fob = new FileOutputBuffer(fn);
        fob.writeInt(aMaxRayWeightSetCount);
        fob.close();

        // create file name and file output buffer

        fn = aIODirectory + File.separator + "inputObservationMap";
        aInputObservationMap.write(fn);
    }

    /**
     * A waiting functions while all of the threads writing BlockArraySet objects
     * complete. These must be completed before the next step (solveRayUncertainty)
     * continues.
     */
    private void waitForBlockArraySetWriteCompletion() {
        String s;

        // get counts and see if the number of ray weight sets written equals the
        // number created

        int rwsc = RayWeightSetList.getRayWeightSetsCreated();
        int rwsw = RayWeightSet.getRayWeightSetsWritten();
        int rwbc = rwsc * ObservationsSourceMap.getBlockCount();
        int rwbw = RayWeightSet.getRayWeightBlocksWritten();

        if (rwsw < rwsc) {
            // still waiting for to finish writing some ray weight sets ... output
            // message and wait

            s = NL +
                    "    **WAIT** Waiting for Ray Weight files to Complete Output " +
                    "Processing ..." + NL +
                    "    **WAIT**   Remaining BlockSets: " +
                    (rwsc - rwsw) + NL +
                    "    **WAIT**   Remaining Blocks:    " +
                    (rwbc - rwbw) + NL;
            aScrnWrtr.write(s);

            // loop while not complete

            while (rwsw < rwsc) {
                try {
                    // update UI and wait until all sets have been written

                    rwsw = RayWeightSet.getRayWeightSetsWritten();
                    rwbc = rwsc * ObservationsSourceMap.getBlockCount();
                    rwbw = RayWeightSet.getRayWeightBlocksWritten();
                    if (aUI != null) aUI.updateRayWeightFilesWritten(rwsc, rwbc, rwsw, rwbw);
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                }
            }

            // done ... continue

            aScrnWrtr.write("    **WAIT** Finished Ray Weight File Writes " +
                    "... Proceeding ..." + NL + NL);
        }

        // make final update and exit

        if (aUI != null) aUI.updateRayWeightFilesWritten(rwsc, rwbc, rwsw, rwbw);
    }

    /**
     * Reads the previously stored input observation map from disk.
     *
     * @return The input observation map generated during ray prediction.
     * @throws IOException
     */
    private void readInputObservationMap() throws IOException {
        // output message

        if (aScrnWrtr.isOutputOn()) {
            String s = "  Reading Phase/Site process map from disk ..." + NL + NL;
            aScrnWrtr.write(s);
        }

        // read in maximum ray weight set count

        String fn = aIODirectory + File.separator + "maxRayWeightSetCount";
        FileInputBuffer fib = new FileInputBuffer(fn);
        aMaxRayWeightSetCount = fib.readInt();
        fib.close();

        // create the filename and a new file input buffer

        fn = aIODirectory + File.separator + "inputObservationMap";
        aInputObservationMap = new ObservationsPhaseSiteMap(fn);
    }

    /**
     * Debug function used to dump the phase/site/source/observation map
     * to the screen.
     *
     * @throws PropertiesPlusException
     */
    private void dumpInputObservationMap() throws PropertiesPlusException {
        String s;

        if (aProps.getBoolean("dumpObservationsPhaseSiteMap", false)) {
            if (aScrnWrtr.isOutputOn()) {
                // output message and loop over all phases in the map

                s = NL + "    Phase/Site/ProcessData Map ..." + NL +
                        "      Total Ray Count:                 " +
                        aInputObservationMap.getTotalRayCount() + NL +
                        "      Max. Block Ray Count:            " +
                        aInputObservationMap.getMaxBlockRayCount() + NL +
                        "      Total Ray Weight Entries:        " +
                        aInputObservationMap.getTotalElementCount() + NL +
                        "      Max. Block Ray Weight Entries:   " +
                        aInputObservationMap.getMaxBlockElementCount() + NL + NL;
                aScrnWrtr.write(s);
                ArrayList<ObservationsSourceMap> psoList;
                psoList = aInputObservationMap.getList();
                for (int i = 0; i < psoList.size(); ++i) {
                    ObservationsSourceMap pso = psoList.get(i);
                    pso.dump("        ", aRcvrXMap.get((int) pso.getSiteId()).getSta(),
                            aScrnWrtr);
                }
                aScrnWrtr.write(NL);
            }
        }
    }

    /**
     * Adds the input phase / rcvrA / rcvrB attribute surface to the input
     * attribute index entries surface map, if it is not yet contained, and
     * associates a new SiteAttributeMap with it. If it is already defined the
     * function simply returns without any changes. This surface map is used to
     * perform smoothing on the requested uncertainty components (diagonal,
     * off-diagonal, and non-represented) following the uncertainty calculation.
     * for the specific source model that owns the input attribute index entry
     * (attrIndxEntries).
     *
     * @param phase           The phase for the new attribute surface.
     * @param rcvrA           The receiver A for the new attribute surface.
     * @param rcvrB           The receiver B for the new attribute surface.
     * @param attrIndxEntries The attribute index entry owned by some source
     *                        model.
     * @throws IOException
     */
    public static void addNewAttributeSurface(SeismicPhase phase,
                                              Receiver rcvrA, Receiver rcvrB,
                                              AttributeIndexerSmart ai,
                                              ArrayList<SiteAttributeMap> samList,
                                              HashMap<SeismicPhase, HashMap<Receiver,
                                                      HashMap<Receiver,
                                                              SiteAttributeMap>>> surfMap,
                                              boolean[] smoothFlag,
                                              RayUncertainty ru)
            throws IOException {
        SiteAttributeMap sam;
        HashMap<Receiver, SiteAttributeMap> phaseRcvrAMap;
        HashMap<Receiver, HashMap<Receiver, SiteAttributeMap>> phaseMap;

        // see if the phase is defined in the map

        phaseMap = surfMap.get(phase);
        if (phaseMap != null) {
            // phase is defined ... see if receiver A is associated with phase

            phaseRcvrAMap = phaseMap.get(rcvrA);
            if (phaseRcvrAMap != null) {
                // receiver A is defined ... see if receiver B is associated with
                // phase / receiver A entry ... if it is return

                sam = phaseRcvrAMap.get(rcvrB);
                if (sam != null)
                    return;
                else {
                    // receiver B was not found in the phase / receiver A map ...
                    // add the first entry

                    sam = ru.new SiteAttributeMap(phase, rcvrA, rcvrB);
                    phaseRcvrAMap.put(rcvrB, sam);
                }
            } else {
                // receiver A was not found for phase map ... create and add
                // first entry for phase

                sam = ru.new SiteAttributeMap(phase, rcvrA, rcvrB);
                phaseRcvrAMap = new HashMap<Receiver, SiteAttributeMap>();
                phaseRcvrAMap.put(rcvrB, sam);
                phaseMap.put(rcvrA, phaseRcvrAMap);
            }
        } else {
            // phase was not found ... add first entry for phase

            sam = ru.new SiteAttributeMap(phase, rcvrA, rcvrB);
            phaseRcvrAMap = new HashMap<Receiver, SiteAttributeMap>();
            phaseRcvrAMap.put(rcvrB, sam);
            phaseMap = new HashMap<Receiver, HashMap<Receiver, SiteAttributeMap>>();
            phaseMap.put(rcvrA, phaseRcvrAMap);
            surfMap.put(phase, phaseMap);
        }

        // update attribute indexer with the new SiteAttributeMap

        sam.addAttribute(phase, rcvrA, rcvrB, samList, ai, smoothFlag);
    }

    /**
     * Solves all site / source ray partial sum ray uncertainty at each block.
     * All results are returned from parallel nodes to the client where they
     * are accumulated for each ray.
     *
     * @throws Exception
     */
    private void solveRayUncertainty() throws Exception {
      String s;

      FinalVariancesPhaseSiteMap vMapAA = null;
      FinalVariancesPhaseSiteSiteMap vMapAB = null;

      DebugResultsPhaseSiteMap drMapAA = null;
      DebugResultsPhaseSiteSiteMap drMapAB = null;

      // Output ray uncertainty solution begin message

      if (aScrnWrtr.isOutputOn()) {
        s = NL + "  Start Ray Uncertainty " + Globals.repeat("#", 56) + NL;
        aScrnWrtr.write(s);
      }
      if (aUI != null)
        aUI.updateInitialStatus(RayUncertainty.SolutionPhase.RAY_UNCERTAINTY);

      // initialize times and create a new PartialVariance object (for new map
      // element creation)

      long rayUncStrtTime = System.currentTimeMillis();
      long clientRayUncTimeTotal = 0;
      // int nrBlkIndx = aCovMatrixBlockInfo.blocks();

      // start task return thread

      int totalTasksProcessed = 0;
      int proccesorCountSum = 0;
      long prcNodeCovBlkReadTime = 0;
      long prcNodeBlockMapReadTime = 0;
      int prcNodeBlockMapReadCount = 0;
      long prcNodeTotalTime = 0;
      long prcNodeProcessTime = 0;
      long prcNodeGCProcTime = 0;
      long prcNodeOverheadTime = 0;
      long prcNodeParallelOvrhdTime = 0;
      long prcNodeRaysLoaded = 0;
      long prcNodeRayElementsLoaded = 0;
      int totalSiteIdAAEntries = 0;
      int totalSourceIdAAEntries = 0;
      int totalASiteIdABEntries = 0;
      int totalBSiteIdABEntries = 0;
      int totalSourceIdABEntries = 0;

      // create variance storage maps

      aFinalVarMapAA = new FinalVariancesPhaseSiteMap();
      if (aSitePair.size() > 0)
        aFinalVarMapAB = new FinalVariancesPhaseSiteSiteMap();

      // ==================================================
      // Submit ray uncertainty tasks in separate thread:
      // ==================================================
      final AtomicInteger taskCt = new AtomicInteger(0);
      final Semaphore submitted = new Semaphore(0);
      final Semaphore init = new Semaphore(0);
      //final Set<Integer> c = Collections.synchronizedSet(new TreeSet<>());
      new Thread(() -> {
        // Creates all tasks and hands them one-by-one to the broker:
        try {
          streamRayUncertaintyTasks(((Consumer<RayUncertaintyTask>) (aParallelBrkr::submitBatched))
              .andThen(t -> t.setSubmitTime(System.currentTimeMillis())),
              //.andThen(t -> c.add(t.getTaskId())),
              (done, total, msg) -> {
                if (taskCt.get() == 0) {
                  taskCt.set(total);
                  init.release(1);
                  if (aScrnWrtr.isOutputOn())
                    aScrnWrtr.write("    Submitting " + total + " tasks in up to "
                        + aParallelBrkr.getMaxBatches() + " batches, "
                        + aParallelBrkr.getBatchSize() + " tasks at a time");
                }
                submitted.release(1);
              });
        } catch (IOException e) {
          aScrnWrtr.write(e);
        }

        // Ensures any queued tasks not yet submitted for execution are flushed:
        aParallelBrkr.purgeBatch();
      }, "RayUncertaintyTask-Submission-Thread").start();

      // =====================================================
      // Handle ray uncertainty results in the calling thread:
      // =====================================================
      
      Progress tasksReturned = new CliProgress(aProps)
          .withMessageGenerator((d,t) -> d+"/"+t+" tasks returned on "+GMTFormat.getNow());
      
      init.acquire(1);
      for (int i = 0; i < taskCt.get(); i++) {
        submitted.acquire(1);
        
        RayUncertaintyTaskResult tskRslt = (RayUncertaintyTaskResult)aParallelBrkr.getResultWait();
        
        tasksReturned.update(i+1, taskCt.get());
        
        //c.remove(tskRslt.getIndex());
        
        //if(c.size() < 100) {
        //  System.out.println("task ids still running: " + c);
        //}
        // output any read/write failures, before throwing any exceptions

        String rwFail = tskRslt.getReadWriteFailErrors();
        if ((rwFail != null) && !rwFail.equals("")) {
          if (aScrnWrtr.isOutputOn()) {
            aScrnWrtr.write(rwFail);
          }
        }

        // throw error if task failed to complete
        if (tskRslt.getException() != null) throw tskRslt.getException();

        // increment returned counts and read times

        prcNodeTotalTime += tskRslt.getTotalTime();
        prcNodeProcessTime += tskRslt.getProcessTime();
        prcNodeGCProcTime += tskRslt.getGCProcessTime();
        prcNodeOverheadTime += tskRslt.getOverheadTime();
        prcNodeParallelOvrhdTime += tskRslt.getTaskManagerOverhead();
        prcNodeCovBlkReadTime += tskRslt.getCovBlockReadTime();
        prcNodeBlockMapReadTime += tskRslt.getBlockMapReadTime();
        prcNodeBlockMapReadCount += tskRslt.getBlockMapReadCount();
        prcNodeRaysLoaded += tskRslt.getRaysLoadedCount();
        prcNodeRayElementsLoaded += tskRslt.getRayElementsLoadedCount();

        // see if tskRslt is a type AA or type BB and process

        vMapAA = tskRslt.getVarianceMapAA();
        if (vMapAA != null) {
          // process type AA task ... update counts and add partial
          // to totals

          totalSiteIdAAEntries += vMapAA.getUniqueSiteCount();
          totalSourceIdAAEntries += vMapAA.getTotalSourceEntryCount();
          aFinalVarMapAA.add(vMapAA);
        }

        vMapAB = tskRslt.getVarianceMapAB();
        if (vMapAB != null) {
          // process type AB task ... loop over all sites and
          // site rays in task and update global site variance

          totalASiteIdABEntries += vMapAB.getUniqueASiteCount();
          totalBSiteIdABEntries += vMapAB.getUniqueBSiteCount();
          totalSourceIdABEntries += vMapAB.getTotalSourceEntryCount();
          aFinalVarMapAB.add(vMapAB);
        } // end else if (pvMapAB != null)

        if ((vMapAA == null) && (vMapAB == null))
          throw new IOException("Task Result Has no Result ...");

        // update debug results if requested

        if (aDebug) {
          drMapAA = tskRslt.getDebugResultsMapAA();
          drMapAB = tskRslt.getDebugResultsMapAB();
          addDebugRayTaskResults(drMapAA, drMapAB);
        }

        totalTasksProcessed++;
        // update UI

        if (aUI != null)
          aUI.updateInitialStatus(RayUncertainty.SolutionPhase.RAY_UNCERTAINTY);
        if (aUI != null)
          aUI.updateRayUncertainty(prcNodeTotalTime, aParallelBrkr.getProcessorCount(),
              prcNodeProcessTime, prcNodeGCProcTime, prcNodeBlockMapReadTime,
              prcNodeBlockMapReadCount, prcNodeCovBlkReadTime, prcNodeOverheadTime);
      }
      clientRayUncTimeTotal = System.currentTimeMillis() - rayUncStrtTime;
      
      // =====================================================
      // End RayUncertaintyTaskResult handling section.
      // =====================================================

      // done ... clear list and loop to fill again

      aScrnWrtr.write(NL);

      // output results

      if (aScrnWrtr.isOutputOn()) {
        s = "    Ray Uncertainty Summary Results:" + NL
            + "      Current Time                                              = "
            + Globals.getTimeStamp() + NL
            + "      Total Ray Uncertainty Process Time                        = "
            + Globals.elapsedTimeString(rayUncStrtTime) + NL
            + "      Client Ray Uncertainty Map Time                           = "
            + Globals.elapsedTimeString(0, clientRayUncTimeTotal) + NL
            + "      Cumulative Node Total Time                                = "
            + Globals.elapsedTimeString(0, prcNodeTotalTime) + NL
            + "      Total Tasks Processed                                     = "
            + totalTasksProcessed + NL
            + "      Mean Processor Count                                      = "
            + ((double) proccesorCountSum / totalTasksProcessed) + NL
            + "      Parallel Acceleration Ratio                               = "
            + ((double) prcNodeTotalTime / clientRayUncTimeTotal) + NL
            + "      Parallel Efficiency (%)                                   = "
            + (100.0 * prcNodeTotalTime / clientRayUncTimeTotal)
                / ((double) proccesorCountSum / totalTasksProcessed)
            + NL + NL + "      Mean Task Process Time                                    = "
            + Globals.timeStringAbbrvUnits(prcNodeTotalTime / totalTasksProcessed) + NL
            + "      Mean Per Task Block Ray Weight Read Time                  = "
            + Globals.timeStringAbbrvUnits(prcNodeBlockMapReadTime / totalTasksProcessed) + NL
            + "      Mean Per Task Block Ray Weight Read Count                 = "
            + ((double) prcNodeBlockMapReadCount / totalTasksProcessed) + NL
            + "      Mean Per Read Block Ray Weight Read Time                  = "
            + Globals.timeStringAbbrvUnits(prcNodeBlockMapReadTime / prcNodeBlockMapReadCount) + NL
            + "      Mean Per Task Covariance Block Read Time                  = "
            + Globals.timeStringAbbrvUnits(prcNodeCovBlkReadTime / totalTasksProcessed) + NL
            + "      Mean Per Task Process Time                                = "
            + Globals.timeStringAbbrvUnits(prcNodeProcessTime / totalTasksProcessed) + NL
            + "      Mean Per Task Garbage Collection Process Time             = "
            + Globals.timeStringAbbrvUnits(prcNodeGCProcTime / totalTasksProcessed) + NL
            + "      Mean Per Task Overhead Time                               = "
            + Globals.timeStringAbbrvUnits(prcNodeOverheadTime / totalTasksProcessed) + NL
            + "      Mean Per Task Parallel Overhead Time                      = "
            + Globals.timeStringAbbrvUnits(prcNodeParallelOvrhdTime / totalTasksProcessed) + NL + NL
            + "      Mean Per Task Rays Loaded                                 = "
            + ((double) prcNodeRaysLoaded / totalTasksProcessed) + NL
            + "      Mean Per Task Ray Elements Loaded                         = "
            + ((double) prcNodeRayElementsLoaded / totalTasksProcessed) + NL
            + "      Mean Per Task AA Site entries Returned                    = "
            + ((double) totalSiteIdAAEntries / totalTasksProcessed) + NL
            + "      Mean Per Task AA Source entries Returned                  = "
            + ((double) totalSourceIdAAEntries / totalTasksProcessed) + NL
            + "      Mean Per Task AB Site A entries Returned                  = "
            + ((double) totalASiteIdABEntries / totalTasksProcessed) + NL
            + "      Mean Per Task AB Site B entries Returned                  = "
            + ((double) totalBSiteIdABEntries / totalTasksProcessed) + NL
            + "      Mean Per Task AB Source entries Returned                  = "
            + ((double) totalSourceIdABEntries / totalTasksProcessed) + NL + NL;
        aScrnWrtr.write(s);
        aScrnWrtr.write("  End Ray Uncertainty " + Globals.repeat("#", 58) + NL + NL);
      } // end if (aScrnWrtr.isOutputOn())
    }

    /**
     * Sets the variance output writer when the source definition is DATABASE
     * or PROPERTIESFILE.
     *
     * @throws IOException
     */
    private void setUncertaintyOutputWriter() throws IOException {
        String s;

        // make sure source definition is NOT GEOMODEL and that the writer has not
        // yet been created

        if ((aUncOut == null) && (aSourceDefinition != SourceDefinition.GEOMODEL &&
                aSourceDefinition != SourceDefinition.GEOTESSMODEL)) {
            // create writer and output header

            s = aIODirectory + File.separator + "variance.txt";
            aUncOut = new BufferedWriter(new FileWriter(s));
            aUncOut.write("Uncertainty Output File" + NL);
            aUncOut.write(Globals.getTimeStamp() + NL + NL);

            // output receiver information

            aUncOut.write("Receiver Count " + aReceiverList.size() + NL + NL);
            aUncOut.write("               Receivers" + NL);
            aUncOut.write("      Id        Name   " +
                    "Lat(deg) Lon(deg)   Depth(km)" + NL);
            String fmt = "  %8d  %8s  %8.3f  %8.3f  %8.3f";
            for (int x = 0; x < aReceiverList.size(); ++x) {
                Receiver rcvr = aReceiverList.get(x);
                aUncOut.write(String.format(fmt, rcvr.getReceiverId(), rcvr.getSta(),
                        rcvr.getLatDegrees(),
                        rcvr.getLonDegrees(),
                        rcvr.getDepth()) + NL);
            }
            aUncOut.write(NL);

            // output source information

            aUncOut.write("Source Count " + aSourceList.size() + NL + NL);
            aUncOut.write("               Sources" + NL);
            aUncOut.write("   Id       " +
                    "Lat(deg)   Lon(deg)    Depth(km)" + NL);
            fmt = "  %8d  %8.3f  %8.3f  %8.3f";
            for (int x = 0; x < aSourceList.size(); ++x) {
                Source src = aSourceList.get(x);
                aUncOut.write(String.format(fmt, src.getSourceId(),
                        src.getLatDegrees(),
                        src.getLonDegrees(),
                        src.getDepth()) + NL);
            }
            aUncOut.write(NL);

            // output ray partial variance header

            //  StaA  (StaB)    Src Id    Total      Diag.   Off-Diag. Non-Rep.  RepFrctn
            //                           (sec^2)    (sec^2)   (sec^2)  (sec^2)     (#)
            // SSSSSS(SSSSSS)  ********  ###.####  ###.####  ###.####  ###.####  ###.####
            // SSSSSS          ********  ###.####  ###.####  ###.####  ###.####  ###.####
            aUncOut.write("Observation Variance Table" + NL + NL);
            aUncOut.write("Observation Count " + aObservationList.size() + NL + NL);
            aUncOut.write(" StaA  (StaB)     Src Id     ");
            aUncOut.write("Total      Diag.   Off-Diag. Non-Rep.  RepFrctn" + NL);
            aUncOut.write("                            ");
            aUncOut.write("(sec^2)    (sec^2)   (sec^2)  (sec^2)     (#)" + NL);
        }
    }

    /**
     * Interpolates all variance/covariance results for every source/receiverA and
     * source/receiverA/receiverB for all models and phases and stores the result
     * in each model and writes the model to disk.
     *
     * @throws GMPException
     * @throws IOException
     * @throws GeoTessException
     */
    private void interpolateUncertainties()
            throws GMPException, IOException, GeoTessException {
        String s;

        HashMapLongKey<HashSetLong> mdlRcvrSrcMap;
        AttributeIndexEntries attrIndexEntries = null;
        HashMap<Receiver, SiteAttributeMap> rcvrSiteMap;

        long strtTime = (new Date()).getTime();
        if (aScrnWrtr.isOutputOn()) {
            s = "  Start Uncertainty Interpolation " + Globals.repeat("#", 46) + NL;
            aScrnWrtr.write(s);
        }

        // turn on the uncertainty output writer if the source type is a "DATABASE"
        // or "PROPERTIESFILE"

        setUncertaintyOutputWriter();

        // create source model output directory

        File smop = new File(aSourceModelOutputPath);
        if (!smop.exists()) FileDirHandler.createDirectory(aSourceModelOutputPath);

        // create temporary weight maps

        HashMap<Integer, Double> weightsS = new HashMap<Integer, Double>();
        HashMap<Integer, Double> weightsRA = new HashMap<Integer, Double>();
        HashMap<Integer, Double> weightsRB = new HashMap<Integer, Double>();

        // create src variance result

        double[] srcUnc = {0.0, 0.0, 0.0, 0.0, 0.0};
        int[] countSAB = {0, 0, 0};

        // create attribute arrays

        GeoAttributes[] attr = {GeoAttributes.TT_MODEL_VARIANCE,
                GeoAttributes.TT_MODEL_VARIANCE_DIAGONAL,
                GeoAttributes.TT_MODEL_VARIANCE_OFFDIAGONAL,
                GeoAttributes.TT_MODEL_VARIANCE_NONREPRESENTED,
                GeoAttributes.TT_MODEL_VARIANCE_REPRESENTED_FRACTION};

        // get storage precision

        String storePrecision = aProps.getProperty("storagePrecision", "float").trim();

        // create model receiver -> source map and loop over and process all models

        GeoVector srcGV = new GeoVector();
        int totlMdlRcvrCount = 0;
        int totlMdlRcvrSrcCount = 0;
        int totlMdlRcvrSrcPhsCount = 0;
        for (Map.Entry<String, HashSet<Long>> e : aUniqueSourceFileRcvrMap.entrySet()) {
            // load next model and get its receiver set, model receiver->source id map,
            // and attribute indexer. Set attribute indexer into model and loop over
            // all requested receivers.

            String fn = e.getKey();
            String fno = getNewFilePath(aSourceModelOutputPath, fn);
            attrIndexEntries = aModelIndexer.get(fn);
            HashSet<Long> modelRcvrIds = e.getValue();
            mdlRcvrSrcMap = aModelRcvrSrcMap.get(fn);

            ModelInterface mdl = null;
            if ((aSourceDefinition == SourceDefinition.GEOMODEL) ||
                    (aSourceDefinition == SourceDefinition.GEOTESSMODEL)) {
                mdl = loadModel(fn, "  ", true);

                // set attribute indexer and initialize

                if (storePrecision.toLowerCase().equals("double"))
                    mdl.setAttributeIndexer(attrIndexEntries.aIndexer, Double.NaN);
                else if (storePrecision.toLowerCase().equals("float"))
                    mdl.setAttributeIndexer(attrIndexEntries.aIndexer, Float.NaN);
            }

            if (aScrnWrtr.isOutputOn()) {
                s = "    Interpolating Source/Receivers ... " + NL;
                aScrnWrtr.write(s);
            }

            int mdlSetCount = 0;
            int mdlRcvrSrcPointCount = 0;
            totlMdlRcvrCount += modelRcvrIds.size();
            for (long rcvrAId : modelRcvrIds) {
                // get reciever A from id ... get receiver B from site pair map
                // (may be null)

                Receiver rcvrA = aReceiverMap.get(rcvrAId);
                Receiver rcvrB = aSitePair.get(rcvrA);

                // get map of points from tomography model that influence this
                // receivers position ... loop over each

                HashSetLong srcIds = mdlRcvrSrcMap.get(rcvrAId);
                if (srcIds == null)
                    throw new IOException("model receiver source map has no entry for " + rcvrA.getSta());

                mdlRcvrSrcPointCount += srcIds.size();
                totlMdlRcvrSrcCount += srcIds.size();
                HashSetLong.Iterator it = srcIds.iterator();
                while (it.hasNext()) {
                    // cast id to point index and set source position

                    long srcId = it.next();
                    int srcPntIndx = (int) srcId;
                    if (mdl == null) {
                        Source src = aSourceMap.get(srcId);
                        srcGV.setGeoVector(src.getUnitVector(),
                                src.getRadius());
                    } else
                        srcGV.setGeoVector(mdl.getPointUnitVector(srcPntIndx),
                                mdl.getPointRadius(srcPntIndx));

                    // loop over all phases

                    totlMdlRcvrSrcPhsCount += aPhaseList.size();
                    for (int k = 0; k < aPhaseList.size(); ++k) {
                        // get the next phase and the associated attribute indexes

                        SeismicPhase phase = aPhaseList.get(k);

                        // get receiver A and source influence weights and site attribute
                        // index map

                        getPositionInfluencePoints(aGeoModelTomo, srcGV, phase, weightsS);
                        getPositionInfluencePoints(aGeoModelTomo, rcvrA, phase, weightsRA);
                        rcvrSiteMap = attrIndexEntries.aSurfMap.get(phase).get(rcvrA);

                        // interpolate requested receiver A to current requested source
                        // point for this phase and save result into model ... if rcvr B
                        // is not null perform same interpolation for site A/B covariance
                        // and save results into model

                        if (interpolateSource(mdl, phase, srcUnc, attr, srcPntIndx,
                                rcvrA, null, rcvrSiteMap, countSAB,
                                weightsS, weightsRA, weightsRB))
                            ++mdlSetCount;
//                        else {
//                            System.out.println(srcPntIndx + ", " + srcUnc[2]);
//                        }
                        if (rcvrB != null)
                            if (interpolateSource(mdl, phase, srcUnc, attr, srcPntIndx,
                                    rcvrA, rcvrB, rcvrSiteMap, countSAB,
                                    weightsS, weightsRA, weightsRB))
                                ++mdlSetCount;
                    } // end for (int k = 0; k < aPhaseList.size(); ++k)
                } // end for (Map.Entry<Integer, Source> esrc: rcvrSrcMap.entrySet())

                if (aScrnWrtr.isOutputOn()) {
                    s = "      Set " + mdlSetCount + " model source points of " +
                            mdlRcvrSrcPointCount + " evaluated ..." + NL;
                    aScrnWrtr.write(s);
                }

                // write model if it is not null

                if (mdl != null) {
                    //set GeoModel description

                    s = mdl.getModelDescription() + NL +
                            "Added Travel Time Uncertainty Data (" +
                            Globals.getTimeStamp() + ")" + NL;
                    mdl.setModelDescription(s);

                    // write model to disk

                    if (aScrnWrtr.isOutputOn()) {
                        s = "    Writing Source Model to Disk at \"" + fno + "\"" + NL;
                        aScrnWrtr.write(s);
                    }

                    mdl.writeModel(fno, "*");
                }
            } // end for (int i = 0; i < aReceiverList.size(); ++i)
        }

        // done ... close output uncertainty.txt if it was constructed.

        if (aUncOut != null) aUncOut.close();

        // output results

        if (aScrnWrtr.isOutputOn()) {
            s = NL;
            s += "    Uncertainty Interpolation Summary Results:" + NL +
                    "      Current Time                                              = " +
                    Globals.getTimeStamp() + NL +
                    "      Total Uncertainty Interpolation Process Time              = " +
                    Globals.elapsedTimeString(strtTime) + NL;
            if ((aSourceDefinition == SourceDefinition.GEOMODEL) ||
                    (aSourceDefinition == SourceDefinition.GEOTESSMODEL))
                s += "      Source Models Processed                                   = " +
                        aUniqueSourceFileRcvrMap.size() + NL;
            s += "      Total Receivers Processed                                 = " +
                    totlMdlRcvrCount + NL +
                    "      Total Receiver/Source Rays Processed                      = " +
                    totlMdlRcvrSrcCount + NL +
                    "      Total Receiver/Source/Phase Rays Processed                = " +
                    totlMdlRcvrSrcPhsCount + NL +
                    "      Total Neighbor Source Interpolations                      = " +
                    countSAB[0] + NL +
                    "      Total Neighbor Source/Recevier Interpolations             = " +
                    countSAB[1] + NL;
            if (countSAB[2] > 0)
                s += "      Total Neighbor Source/Recevier/Receiver B Interpolations  = " +
                        countSAB[2] + NL;
            s += "      Minimum Total Variance                                    = " +
                    aMinVariance[3] + NL +
                    "      Minimum Diagonal Variance                                 = " +
                    aMinVariance[0] + NL +
                    "      Minimum Off-Diagonal Variance                             = " +
                    aMinVariance[1] + NL +
                    "      Minimum Non-Represented Variance                          = " +
                    aMinVariance[2] + NL +
                    "      Maximum Total Variance                                    = " +
                    aMaxVariance[3] + NL +
                    "      Maximum Diagonal Variance                                 = " +
                    aMaxVariance[0] + NL +
                    "      Maximum Off-Diagonal Variance                             = " +
                    aMaxVariance[1] + NL +
                    "      Maximum Non-Represented Variance                          = " +
                    aMaxVariance[2] + NL;
            s += NL;
            aScrnWrtr.write(s);
            aScrnWrtr.write("  End Uncertainty Interpolation " +
                    Globals.repeat("#", 48) + NL + NL);
        } // end if (aScrnWrtr.isOutputOn())
    }

    /**
     * Interpolate uncertainty for a specific phase of a model at a requested
     * source point for a requested receiver A position. If rcvrB is not null
     * then calculate covariance of source point for requested receiver B and
     * requested receiver A.
     *
     * @param mdl
     * @param phase
     * @param srcUnc
     * @param attr
     * @param srcPntIndx
     * @param rcvrA
     * @param rcvrB
     * @param rcvrSiteAttrMap
     * @param countSAB
     * @param weightsS
     * @param weightsRA
     * @param weightsRB
     * @return true if point was set into model
     * @throws GeoModelException
     * @throws IOException
     * @throws GeoTessException
     */
    private boolean interpolateSource(ModelInterface mdl, SeismicPhase phase,
                                      double[] srcUnc, GeoAttributes[] attr,
                                      int srcPntIndx, Receiver rcvrA, Receiver rcvrB,
                                      HashMap<Receiver, SiteAttributeMap> rcvrSiteAttrMap,
                                      int[] countSAB,
                                      HashMap<Integer, Double> weightsS,
                                      HashMap<Integer, Double> weightsRA,
                                      HashMap<Integer, Double> weightsRB)
            throws IOException, GeoTessException {
        SiteAttributeMap sam;

        // see if this is a type AA (rcvrB == null) or type AB calculation

        if (rcvrB == null) {
            // type AA get site attribute map and interpolate variance from a
            // requested source point to a requested receiver point.

            sam = rcvrSiteAttrMap.get(rcvrA);
            interpolateSource(phase, srcUnc, weightsS, weightsRA, null,
                    countSAB);
        } else {
            // type AB get site attribute map, calculate requested receiver B
            // influence weights and interpolate covariance from a requested source
            // point to a requested pair of receiver points.

            sam = rcvrSiteAttrMap.get(rcvrB);
            getPositionInfluencePoints(aGeoModelTomo, rcvrB, phase, weightsRB);
            interpolateSource(phase, srcUnc, weightsS, weightsRA, weightsRB,
                    countSAB);
        }

        // Check for a bad variance just in case

        if ((srcUnc[3] < 0.0) && ((rcvrB == null) || (rcvrA == rcvrB))) {
            String s = "    Negative Variance: " + rcvrA.getSta() +
                    ", Source[" + srcPntIndx + "]: Total Var=" + srcUnc[3] +
                    ", Diag Var=" + srcUnc[0] + ", OffDiag Var=" + srcUnc[1] +
                    ", NonRep Var=" + srcUnc[2] + NL;
            aScrnWrtr.write(s);
        }

        // see if source result was defined (total or diagonal) for this receiver

        if ((srcUnc[0] > 0) || (srcUnc[3] > 0)) {
            // save min and max results

            for (int i = 0; i < aMaxVariance.length; ++i) {
                if (srcUnc[i] < aMinVariance[i]) aMinVariance[i] = srcUnc[i];
                if (srcUnc[i] > aMaxVariance[i]) aMaxVariance[i] = srcUnc[i];
            }

            // set receiver/source/phase uncertainty into source model or output
            // results

            if (mdl == null) {
                // output "DATABASE" or "PROPERTIESFILE" results

                writeUncertaintyResult(rcvrA, rcvrB, srcPntIndx, srcUnc);
            } else {
                // set the model with the uncertainty results

                mdl.setPointValue(srcPntIndx, sam.getAttributeIndex(attr[0]),
                        srcUnc[3]); // total
                mdl.setPointValue(srcPntIndx, sam.getAttributeIndex(attr[1]),
                        srcUnc[0]); // diagonal
                mdl.setPointValue(srcPntIndx, sam.getAttributeIndex(attr[2]),
                        srcUnc[1]); // off-diagonal
                mdl.setPointValue(srcPntIndx, sam.getAttributeIndex(attr[3]),
                        srcUnc[2]); // non-represented
                mdl.setPointValue(srcPntIndx, sam.getAttributeIndex(attr[4]),
                        srcUnc[4]); // represented fraction
            }
            return true;
        } else
            return false;
    }

    /**
     * Performs a requested source to receiver interpolation using influencing
     * source and receiver points from the tomography grid for which ray
     * uncertainty was evaluated. This result is for a specific phase and may be
     * a type AA ray or type AB ray (weightsRB is not null for type AB).
     *
     * @param phase     The phase for which the uncertainty (variance) will be
     *                  evaluated.
     * @param srcUnc    The uncertainty result. Has the following components
     *                  [0] = diagonal
     *                  [1] = off-diagonal
     *                  [2] = non-represented
     *                  [3] = total (sum of [0], [1], and [2])
     *                  [4] = represented fraction (([0] + [1]) / total)
     * @param weightsS  The map of all tomography model source point indexes
     *                  associated with their influence weight defined to
     *                  interpolate at the location of the requested source
     *                  position.
     * @param weightsRA The map of all tomography model receiver A point indexes
     *                  associated with their influence weight defined to
     *                  interpolate at the location of the requested source
     *                  position.
     * @param weightsRB The map of all tomography model receiver B point indexes
     *                  associated with their influence weight defined to
     *                  interpolate at the location of the requested source
     *                  position. This null for type AA rays
     * @param countsSAB The number of neighbor interpolation calculations
     *                  [0] = source interpolations
     *                  [1] = receiver A interpolations
     *                  [2] = receiver B interpolations
     */
    private void interpolateSource(SeismicPhase phase, double[] srcUnc,
                                   HashMap<Integer, Double> weightsS,
                                   HashMap<Integer, Double> weightsRA,
                                   HashMap<Integer, Double> weightsRB,
                                   int[] countsSAB) {
        // create receiver A and B uncertainty storage

        FinalVariancesSourceMap fvsm;
        double[] rcvrAUnc = {0.0, 0.0, 0.0, 0.0, 0.0};
        double[] rcvrBUnc = {0.0, 0.0, 0.0, 0.0, 0.0};

        // initialize the source interpolation array and loop over all
        // source influence points

        srcUnc[0] = srcUnc[1] = srcUnc[2] = srcUnc[3] = srcUnc[4] = 0.0;
        for (Map.Entry<Integer, Double> eS : weightsS.entrySet()) {
            // get source point index, weight, and source id

            int pointIndexS = eS.getKey();
            double wghtS = eS.getValue();
            long srcXId = aSrcXMap.get(pointIndexS).getSourceId();

            // initialize the reveiver A interpolation array and loop over all
            // receiver A influence points

            rcvrAUnc[0] = rcvrAUnc[1] = rcvrAUnc[2] =
                    rcvrAUnc[3] = rcvrAUnc[4] = 0.0;
            for (Map.Entry<Integer, Double> eRA : weightsRA.entrySet()) {
                // get receiver A point index, weight, and receiver id

                int pointIndexRA = eRA.getKey();
                double wghtRA = eRA.getValue();
                long rcvrAXId = aRcvrXMap.get(pointIndexRA).getReceiverId();

                // see if this is a type AB interpolation

                if (weightsRB == null) {
                    // type AA ray ... get source map for phase/receiver A and interpolate
                    // requested receiver location using surrounding influence neighbors.

                    fvsm = aFinalVarMapAA.get(phase, rcvrAXId);
                    interpolateRcvrSrcComponent(rcvrAUnc, wghtRA, srcXId, fvsm);
                } else {
                    // type AB ray ... initialize the reveiver B interpolation array and
                    // loop over all receiver B influence points

                    rcvrBUnc[0] = rcvrBUnc[1] = rcvrBUnc[2] =
                            rcvrBUnc[3] = rcvrBUnc[4] = 0.0;
                    for (Map.Entry<Integer, Double> eRB : weightsRB.entrySet()) {
                        // get receiver B point index, weight, and receiver id

                        int pointIndexRB = eRB.getKey();
                        double wghtRB = eRB.getValue();
                        long rcvrBXId = aRcvrXMap.get(pointIndexRB).getReceiverId();

                        // if receiver A and B share a common neighbor then get variance
                        // from type AA source map. Otherwise, get variance from type AB
                        // source

                        if (rcvrAXId == rcvrBXId)
                            fvsm = aFinalVarMapAA.get(phase, rcvrAXId);
                        else
                            fvsm = aFinalVarMapAB.get(phase, rcvrAXId, rcvrBXId);

                        // perform basic interpolation for requested receiver B covariance
                        // between at influence receiver A point rcvrAXId and influence
                        // source point srcXId

                        interpolateRcvrSrcComponent(rcvrBUnc, wghtRB, srcXId, fvsm);
                    } // end for (Map.Entry<Integer, Double> eRB: weightsRB.entrySet())

                    // perform basic interpolation for requested receiver A covariance
                    // at influence source point srcXId

                    interpolateRcvrSrcComponent(rcvrAUnc, wghtRA, rcvrBUnc);
                    countsSAB[2] += (int) rcvrBUnc[4];
                }
            } // end for (Map.Entry<Integer, Double> eRA: weightsRA.entrySet())

            // perform basic interpolation for requested source variance

            interpolateRcvrSrcComponent(srcUnc, wghtS, rcvrAUnc);
            countsSAB[1] += (int) rcvrAUnc[4];
        } // end for (Map.Entry<Integer, Double> eS: weightsS.entrySet())
        countsSAB[0] += (int) srcUnc[4];

        // done ... if defined normalize sources to weight sum and set elements
        // 3 to total and 4 to represented fraction and exit

        if (srcUnc[4] > 0) {
            srcUnc[0] /= srcUnc[3];
            srcUnc[1] /= srcUnc[3];
            srcUnc[2] /= srcUnc[3];

            srcUnc[3] = srcUnc[0] + srcUnc[1] + srcUnc[2];
            srcUnc[4] = (srcUnc[0] + srcUnc[1]) / srcUnc[3];
        }
    }

    /**
     * Interpolate an influence ray covariance from tomographic grid points onto
     * a receiver position using a constant source influence position. The
     * interpolation sums a single influence ray in a neighborhood about a
     * requested receiver location. This is not a complete interpolation. After
     * this function has been called for all neighbors of a requested receiver
     * position the interpolation will be complete. At that point the result
     * stored in the rcvrUnc array (actually variance) will be the variance
     * associated with a ray from the requested receiver to a source influence
     * point. A source influence point is not the requested source but rather one
     * of the points in the neighborhood of the requested source that influences
     * the source result interpolation.
     *
     * @param rcvrUnc The interpolation result array for diagonal ([0]), off-
     *                diagonal ([1]), and non-represented ([2]) components. This
     *                array also stores the weight sum ([3]) to normalize the
     *                interpolation result if one or more neighbors do not
     *                contribute to the solution (this can happens when ray
     *                influence source points lie more than 95 degrees away from
     *                influence receiver points). Finally, the number of
     *                interpolations neighbor contributions (a count ([4])) is
     *                saved for informational purposes.
     * @param wghtR   The influence weight of some neighbor of the requested
     *                receiver location (an influence receiver point).
     * @param srcXId  The id of the influence source point (a point that
     *                influences the requested source point).
     * @param fvsm    A map associating the 3 element variance (diagonal, off-
     *                diagonal, and non-represented) calculation of a ray
     *                between all determined influence source points and the
     *                specific influence receiver point for which the weight
     *                (wghtR) was pre-determined. The influence source point is
     *                exctracted from this map given the id srcXId.
     */
    private void interpolateRcvrSrcComponent(double[] rcvrUnc, double wghtR,
                                             long srcXId,
                                             FinalVariancesSourceMap fvsm) {
        // if the source map is null exit. This can only happen if no sources were
        // evaluated for a specific phase/receiver or phase/receiver/receiver ray

        if (fvsm != null) {
            // get the source variance components for source srcXId and see if it was
            // defined ... exit if not

            double[] unc = fvsm.get(srcXId);
            if (unc != null) {
                // have results interpolate result onto receiver position ... also
                // sum the weight and increment the count

                rcvrUnc[0] += wghtR * unc[0];
                rcvrUnc[1] += wghtR * unc[1];
                rcvrUnc[2] += wghtR * unc[2];
                rcvrUnc[3] += wghtR;
                ++rcvrUnc[4];
            }
        }
    }

    /**
     * Interpolate an influence ray covariance from tomographic grid points onto
     * a receiver or source position using a previously interpolated receiver.
     * If this is a Type AA ray calculation then the input results array prvUnc
     * was calculated by interpolating a requested receiver from a set of
     * influencing neighbor receiver points. In that case this interpolation
     * is summing one of the influence source point rays (averaged to the
     * requested receiver position) to the requested source position. When this
     * interpolation function has been called for all influence source neighbors
     * it will contain the requested source to requested receiver result.
     * <p>
     * If this is a type AB ray calculation then the input results array can be
     * either an interpolation for requested receiver B covariance between an\
     * influence receiver A point and an influence source point, or a source
     * interpolation as described in the previous paragraph.
     * <p>
     * After this function has been called for all neighbors of a requested
     * receiver position the interpolation will be complete. At that point the
     * result stored in the unc array (actually variance) will be the variance
     * associated with a ray from the requested receiver to a source influence
     * point, or a covariance associated with ray between the requested receiver B
     * positon an influence source point and an influence receiver point for
     * receiver A.
     *
     * @param unc    The interpolation result array for diagonal ([0]), off-
     *               diagonal ([1]), and non-represented ([2]) components. This
     *               array also stores the weight sum ([3]) to normalize the
     *               interpolation result if one or more neighbors do not
     *               contribute to the solution (this can happens when ray
     *               influence source points lie more than 95 degrees away from
     *               influence receiver points). Finally, the number of
     *               interpolations neighbor contributions (a count ([4])) is
     *               saved for informational purposes. This array is either an
     *               intermediate receiver A interpolation for type AB rays or
     *               the final source interpolation.
     * @param wght   The influence weight of some neighbor of the requested
     *               receiver/source location (an influence receiver/source
     *               point).
     * @param prvUnc The previous receiver interpolation result. This array is
     *               either a receiver B interpolation for type AB rays or a
     *               receiver A interpolation.
     */
    private void interpolateRcvrSrcComponent(double[] unc, double wght,
                                             double[] prvUnc) {
        // sum the previous contribution of a receiver interpolation into unc
        // if the previous results (prvUnc) are not empty

        if (prvUnc[4] > 0) // count A or B > 0
        {
            // receiver components were defined ... interpolate receiver
            // result onto receiver/source position ... normalize previous
            // results with the weight sum (prvUnc[3]).

            double w = wght / prvUnc[3];

            unc[0] += w * prvUnc[0];
            unc[1] += w * prvUnc[1];
            unc[2] += w * prvUnc[2];
            unc[3] += wght;
            ++unc[4];
        }
    }

    /**
     * Strips off the path of the filename in the input string oldPathName and
     * prepends the new path to that file and returns the result.
     *
     * @param newPath     The new path for the file.
     * @param oldPathName The old path/filename for which "path" will be replaced
     *                    with the new path.
     * @return newPath/filename.
     */
    private String getNewFilePath(String newPath, String oldPathName) {
        // add file separator to new path and get last occurance of file separator
        // from old path/file.

        String newPathName = newPath + File.separator;
        int i = oldPathName.lastIndexOf(File.separator);

        // if this is just a file name then append to new path ... otherwise
        // retrieve file name from old path/name and append to new path

        if (i == -1)
            newPathName += oldPathName;
        else
            newPathName += oldPathName.substring(i + 1);

        // done ... return new path

        return newPathName;
    }

    //4TODO PS: remove this routine and replace it with the "X" version
    // TODO PS: of this method.
    /**
     * Builds the non-represented slowness variance for all non-represented nodes
     * using layer dependent slowness variance calculated from the spread of
     * slowness in each layer or assigned from values set in the input properties
     * "slownessLayerVariance". Note: values read from the properties file take
     * precedence over calculated values. The final non-represented active node
     * variance vector is written to disk so that it is accessible to processing
     * nodes during the block map solution (solveBlockMaps()).
     *
     * @throws IOException
     */
    private void setNonRepresentedVariance() throws IOException {
        String s;

        // get layer names from tomography GeoModel and make a map of names to
        // index

        aNonRepActvNodeVarFileName = "nonRepresentedActiveNodeVariance";
        aLayerNames = aGeoModelTomo.getMetaData().getLayerNames();
        HashMap<String, Integer> layerNameMap = new HashMap<String, Integer>
                (2 * aLayerNames.length);
        for (int i = 0; i < aLayerNames.length; ++i)
            layerNameMap.put(aLayerNames[i].toUpperCase(), i);

        // loop over all phases (P and / or S)

        for (int k = 0; k < aPhaseList.size(); ++k) {
            // get input phase ... throw error if not P or S

            SeismicPhase p = aPhaseList.get(k);
            String waveTypeName = "";
            if (p.getWaveType() == WaveType.P)
            	waveTypeName = "P";
            else if (p.getWaveType() == WaveType.S)
            	waveTypeName = "S";
            else if (p.getWaveType() == null) {
                s = "Error: Phase not supported: " + p.name();
                throw new IOException(s);
            }

            // create storage for slowness layer variance and initialize to -1

            double[] nrSlownessLayerVar = new double[aLayerNames.length];
            for (int i = 0; i < nrSlownessLayerVar.length; ++i)
                nrSlownessLayerVar[i] = -1.0;

            // see if any input layer slowness standard deviations are provided

            String pname = p.name();
            s = aProps.getProperty("slownessLayerStandardDeviation_" + waveTypeName, "").
                    trim();
            if (!s.equals("")) {
                // inputs provided ... update sigma slowness variance (square the
                // standard deviation) for all provided entries (others remain at -1)

                String[] tokens = Globals.getTokens(s, ";");
                for (int i = 0; i < tokens.length; ++i) {
                    String[] lyr = Globals.getTokens(tokens[i], "\t, ");
                    Integer indx = layerNameMap.get(lyr[0].toUpperCase());
                    if (indx == null) {
                        s = "Unknown layer name: \"" + lyr[0] + "\"" + NL;
                        throw new IOException(s);
                    }

                    double var = Double.valueOf(lyr[1]);
                    var *= var;
                    nrSlownessLayerVar[indx] = var;
                }
            } else {
                s = "Error: No property \"slownessLayerStandardDeviation_" + waveTypeName +
                        "\" was found for phase " + pname;
                throw new IOException(s);
            }

            // set active node indices

            aGeoModelTomo.setActiveRegion();

            // loop over all active nodes and add the slowness of any node whose
            // slowness layer variance is -1 (not assigned) to a statistic
            // (slowstat[i]) ... first create slowness layer statistic containers
            // and get the tomography GeoModel active node index map

            Statistic[] slowstat = new Statistic[aLayerNames.length];
            PointMap pmap = aGeoModelTomo.getPointMap();

            // loop over all active nodes

            for (int i = 0; i < aGeoModelTomo.getPointMap().size(); ++i) {
                // get ith active node index map and see of the layer of the map has
                // been assigned ... if not then add that active nodes slowness to
                // that layers slowness statistic object

                int li = pmap.getPointIndices(i)[1];
                if (nrSlownessLayerVar[li] == -1.0) {
                    if (slowstat[li] == null) slowstat[li] = new Statistic();
                    slowstat[li].add(aGeoModelTomo.getValueDouble(i, 0));
                }
            }

            // done building unassigned statistic objects ... now loop over each
            // slowness layer variance ... for each not assigned (equal to -1) assign
            // the calculated layer slowness variance

            for (int i = 0; i < nrSlownessLayerVar.length; ++i) {
                double var = 0.0;
                if (nrSlownessLayerVar[i] == -1.0) {
                    var = slowstat[i].getStdDev();
                    var *= var;
                    nrSlownessLayerVar[i] = var;
                }
            }

            // output results to out.txt

            if (aScrnWrtr.isOutputOn()) {
                s = NL + "  " + p.name() + " Layer Slowness Standard Deviations:" + NL;
                for (int i = 0; i < aLayerNames.length; ++i) {
                    String nm = aLayerNames[i];
                    s += Globals.repeat(" ", 20 - nm.length()) + nm + " = ";
                    s += Math.sqrt(nrSlownessLayerVar[i]) + NL;
                }
                aScrnWrtr.write(s + NL);
            }

            // now we must create the non-represented grid node variance for all nodes
            // in the tomography geomodel that were not represented in the covariance
            // matrix. When ray prediction is calculted in solveRayPrediction() the
            // resulting ray weight active node indices are converted to covariance
            // matrix columns. Those discovered as non-represented nodes are also
            // converted to a psuedo-matrix column above the last valid represented
            // index. If represented matrix size is n then all non-represented node
            // variances can be stored in a vector nrNodeVar[i] such that the
            // variance for the corresponding psuedo-matrix column index, j (larger
            // than n), can be found in the vector at nrNodeVar[j-n].
            //
            // The previously calculated map, aActiveNodeMap, maps all active node
            // indices in the tomography GeoModel to a corresponding matrix column
            // (and psuedo-matrix column) index. So by looping over all entries in
            // aActiveNodeMap we can build nrNodeVar from each entry in aActiveNodeMap
            // whose matrix column index exceeds the represented matrix column index
            // count.

            //first get matrix size, n, and create storage for aNRNodeVar

            int n = aCovMatrixBlockDefn.size();
            double[] nrNodeVar = new double[aGeoModelTomo.getPointMap().size() - aRepNodeCount];

            // loop over all entries in aActiveNodeMap

            HashMapIntegerInteger.Iterator it = aActiveNodeMap.iterator();
            while (it.hasNext()) {
                // get entry active node index and matrix column index

                HashMapIntegerInteger.Entry e = it.nextEntry();
                int actvNodeIndx = e.getKey();
                int mcIndx = e.getValue();

                
                // if the matrix column index exceeds the matrix size then the index is
                // a pseudo index ... get the active node index map and assign position
                // mcIndx-n of the non-represented node variance vector to the non-
                // represented layer slowness variance for the layer of the current
                // active node index

                if (mcIndx >= n) {
                    int li = pmap.getPointIndices(actvNodeIndx)[1];
                    nrNodeVar[mcIndx - n] = nrSlownessLayerVar[li];
                }
            }

            // now write out aNRNodeVar so that it is available at each of processing
            // nodes during the block map solution (solveBlockMaps()).

            String fn = aIODirectory + File.separator + aNonRepActvNodeVarFileName +
                    "_" + pname;
            FileOutputBuffer fob = new FileOutputBuffer(fn);
            fob.writeDoubles(nrNodeVar);
            fob.close();
        } // end for (int k = 0; k < aPhaseList.size(); ++k)
    }

    // TODO PS: This is new code that replaces the old single wave type non-
    // TODO PS: referenced variance array build method. Remove the old method
    // TODO PS: setNonRepresentedVariance() and replace it with this one
    // TODO PS: (Rename by removing the X at the end of the method name).
    /**
     * Builds the non-represented slowness variance for all non-represented nodes
     * using layer dependent slowness variance input given by the wave type specific
     * properties slownessLayerStandardDeviation_P and slownessLayerStandardDeviation_S.
     * 
     * Only the P wave property needs to be defined if the input covariance
     * matrix is P only. Likewise, only the S wave property needs to be
     * defined if the input covariance matrix is S only. If the input
     * covariance matrix is composed of both P and S entries then both
     * properties are required. An exception will be thrown if this is not the
     * case.
     * 
     * Following completion of this method an array of variances for each
     * non-referenced grid node in the model will be written to disk. This
     * array will contain both P and S wave non-referenced variances, if both are
     * defined. The array is written to the file name 
     * "nonRepresentedActiveNodeVariance", which is read by all ray uncertainty
     * parallel tasks (RayUncertaintyTask) during the solution phase.
     * 
     * @throws IOException
     */
    private void setNonRepresentedVarianceX() throws IOException {

        // get layer names from tomography GeoTessModel and make a map of names to
        // index

        aNonRepActvNodeVarFileName = "nonRepresentedActiveNodeVariance";
        aLayerNames = aGeoModelTomo.getMetaData().getLayerNames();
        HashMap<String, Integer> layerNameMap = new HashMap<String, Integer>
                (2 * aLayerNames.length);
        for (int i = 0; i < aLayerNames.length; ++i)
            layerNameMap.put(aLayerNames[i].toUpperCase(), i);

        // build an array of non-referenced node variances. These will be supplied
        // to the ray uncertainty solution tasks to set P and S wave type non-
        // referenced node variances.
        
        // TODO PS: replace first occurrence of aActiveNodeMap with aActiveNodeMapP
        // TODO PS: and the second occurrence of aActiveNodeMap with aActiveNodeMapS
        double[] nonRefNodeVar = new double[aActiveNodeMap.size() +
                                            aActiveNodeMap.size() -
                                            aCovMatrixBlockDefn.size()];

        // Build the P and S contributions to the non-referenced node variance
        // array utilized by the ray uncertainty solve tasks. Only the wave types
        // requested are built (P only, S only, or P and S). 
        
        double[] slownessLayerVarianceP = null;
        double[] slownessLayerVarianceS = null;
        // TODO PS: replace aRepNodeCount with aRepNodeCountP and aActiveNodeMap
        // TODO PS: with aActiveNodeMapP
        if (aRepNodeCount > 0) {
        	slownessLayerVarianceP = inputWaveTypeNonRepresentedLayerUncertainty(
        			"P", layerNameMap);
        	buildNonReferencedNodeVarianceArray(slownessLayerVarianceP,
        			aActiveNodeMap, nonRefNodeVar);
        }
        // TODO PS: replace aRepNodeCount with aRepNodeCountS and aActiveNodeMap
        // TODO PS: with aActiveNodeMapS
        if (aRepNodeCount > 0) {
        	slownessLayerVarianceS = inputWaveTypeNonRepresentedLayerUncertainty(
        			"S", layerNameMap);
        	buildNonReferencedNodeVarianceArray(slownessLayerVarianceS,
        			aActiveNodeMap, nonRefNodeVar);
        }

        // now write out the nonRefNodeVar array so that it is available 
        // during the ray uncertainty covariance matrix block solution
        // (solveBlockMaps()).

        String fn = aIODirectory + File.separator + aNonRepActvNodeVarFileName;
        FileOutputBuffer fob = new FileOutputBuffer(fn);
        fob.writeDoubles(nonRefNodeVar);
        fob.close();
    }
    
    /**
     * Evaluates and returns the P wave or S wave non-represented slowness layer
     * variance array. These are read from the properties 
     * slownessLayerStandardDeviation_P and slownessLayerStandardDeviation_S.
     * 
     * If called for a wave type where the property is not defined this method
     * will throw an exception
     * 
     * @param waveTypeName The wave type string. Either "P" or "S".
     * @param layerNameMap The map of model layer names associated with their
     * 					   index.
     * 
     * @return The wave type non-represented slowness layer variance array for
     * 		   the input wave type (waveTypeName).
     * 
     * @throws IOException
     */
    private double[] inputWaveTypeNonRepresentedLayerUncertainty(String waveTypeName,
    		HashMap<String, Integer> layerNameMap) throws IOException {

		String s;

		// create storage for slowness layer variance and initialize to -1

		double[] nrSlownessLayerVar = new double[aLayerNames.length];
		for (int i = 0; i < nrSlownessLayerVar.length; ++i)
			nrSlownessLayerVar[i] = -1.0;

		// see if any input layer slowness standard deviations are provided

		s = aProps.getProperty("slownessLayerStandardDeviation_"
				+ waveTypeName, "").trim();
		if (!s.equals("")) {
			
			// inputs provided ... update layer slowness variance (square the
			// standard deviation) for all provided entries. If layers are not
			// defined their values remain at -1.0, which will throw an error
			// at the end of this method.

			String[] tokens = Globals.getTokens(s, ";");
			for (int i = 0; i < tokens.length; ++i) {

				// read the next layer and associated uncertainty. Get the
				// layer names index from the layer name map and throw an
				// exception if the layer name is not defined.

				String[] lyr = Globals.getTokens(tokens[i], "\t, ");
				Integer indx = layerNameMap.get(lyr[0].toUpperCase());
				if (indx == null) {
					s = "\n Error: Unknown layer name: \"" + lyr[0]
							+ "\" read for property \"slownessLayerStandardDeviation_"
							+ waveTypeName + "\" ...\n\n";
					throw new IOException(s);
				}

				// the layer name is valid. Get the uncertainty, square it, and
				// save it to the slowness variance array at the appropriate
				// index.

				double var = Double.valueOf(lyr[1]);
				var *= var;
				nrSlownessLayerVar[indx] = var;
			}
		} else {

			// throw an exception if the wave type property was not found.

			s = "\nError: No property \"slownessLayerStandardDeviation_"
					+ waveTypeName + "\" was found for wave type "
					+ waveTypeName + " ...\n\n";
			throw new IOException(s);
		}

		// make sure all layers were found and added to the slowness layer variance
		// array. Throw an exception if not

		for (int i = 0; i < nrSlownessLayerVar.length; ++i) {
			if (nrSlownessLayerVar[i] == -1) {
				throw new IOException("\nError: Non-Represented " + waveTypeName 
						+ " wave variance for model layer \"" + aLayerNames[i]
						+ "\" was not defined.\n" 
						+ "       All model layers defined in the input "
						+ "GeoTessModel must be defined for property \""
						+ "slownessLayerStandardDeviation_"
						+ waveTypeName + "\" ...\n\n");
			}
		}

		// done ... return wave type specific slowness layer variance

		return nrSlownessLayerVar;
    }
    
    /**
     * Fills P or S wave components of the non-referenced node variance array,
     * which will be used during the ray uncertainty calculation to account
     * for nodes not defined in the covariance matrix.
     * 
     * The slownessLayerVar input array contains the P or S wave non-
     * referenced node variance for each layer in the model. All non-
     * referenced nodes in a specific layer of the model are given the
     * same variance.
     * 
     * The activeNodeMap contains the P or S wave map of global active node
     * ids associated with a specific covariance matrix column index.
     * Additionally, the map contains global active node ids associated with
     * pseudo matrix column indexes that exceed the total number of columns
     * in the covariance matrix. These are non-referenced nodes in the model.
     * 
     * The nonRefNodeVar input array will be filled with the layer
     * variance, which is assigned in slownessLayerVar array. The layer
     * index of the node whose global active node is associated with the
     * pseudo matrix column index is used to extract the variance from
     * slownessLayerVar and then set the variance into nonRefNodeVar array.
     * The variance is put into the position of the the pseudo matrix column
     * index, but offset by the total number of covariance matrix columns.
     * For example,
     * 
     *  	nonRefNodeVar[mcPseudoIndx - nonRefStartIndx] = slownessLayerVar[li];
     * 
     * Where mcPseudoIndx is a non-referenced pseudo matrix column index
     * associated with global active node id, and li is the layer index of
     * the same global active node id.
     * 
     * The pseudo matrix column indexes begin at the size of the covariance
     * matrix size and increase with each new entry added thereafter. The
     * nonRefNodeVar does not store matrix column indices less than the
     * covariance matrix column count as these are NOT non-referenced nodes.
     * So the non-reference node variance array above subtracts the total
     * covariance matrix size from the index so that it is zero based.
     * This also occurs in the ray uncertainty calculation later when non-
     * referenced node variances are retrieved for use. 
     * 
     * @param slownessLayerVar The non-represented slowness layer variance
     * 						   array for wave type P or S.
     * @param activeNodeMap	   The wave type specific global active node map
     * 						   containing active node indexes associated with
     * 						   matrix column indexes, or pseudo matrix column
     * 						   indexes representing non-represented node
     * 						   entries. 
     * @param nonRefNodeVar	   The non-referenced node variance array to be
     * 						   filled with P or S wave type variances.
     */
    private void buildNonReferencedNodeVarianceArray(double[] slownessLayerVar,
    		HashMapIntegerInteger activeNodeMap, double[] nonRefNodeVar) {

    	// Get the tomography model point map and set the start of the non-
    	// referenced pseudo matrix column node ids.
    	
        PointMap pmap = aGeoModelTomo.getPointMap();
        int nonRefStartIndx = aCovMatrixBlockDefn.size(); 

        // loop over all entries in activeNodeMap

        HashMapIntegerInteger.Iterator it = activeNodeMap.iterator();
        while (it.hasNext()) {
        	
            // get entry global active node index and matrix column index

            HashMapIntegerInteger.Entry e = it.nextEntry();
            int glblActvNodeIndx = e.getKey();
            int mcIndx = e.getValue();

            // if the matrix column index exceeds the matrix size then the index is
            // a pseudo index.

            if (mcIndx >= nonRefStartIndx) {
            	
            	// Set the non-referenced node variance at position 
                // mcIndx - nonRefStartIndx with the layer slowness variance
            	// for the layer (li) of the current global active node index
            	
                int li = pmap.getLayerIndex(glblActvNodeIndx);
                nonRefNodeVar[mcIndx - nonRefStartIndx] = slownessLayerVar[li];
            }
        }
    }
    
    /**
     * Creates RayUncertaintyTasks for based on how this RayUncertainty object is configured and
     * hands them to the specified taskConsumer one-by-one. If the optional progress consumer is
     * non-null, progress is handed to it in the form of <code>accept(currentStep,totalSteps)</code>
     * @param taskConsumer consumes tasks as they are created
     * @param progressConsumer consumes progress updates (if not null)
     * @throws IOException may be thrown by created tasks during calls to task.setPhaseSiteSets()
     */
    private void streamRayUncertaintyTasks(Consumer<RayUncertaintyTask> taskConsumer,
        Progress progressConsumer) throws IOException{
      
      // set block limits and create task list

      int blkcnt = aCovMatrixBlockDefn.blocks();
      int blklmt = blkcnt + 1;
      int tCt = 0;
      
      // Compute total task count:
      int totTasks = 0;
      for (int blkrow = 0; blkrow < blklmt; ++blkrow) {
        for (int blkcol = 0; blkcol <= blkrow; ++blkcol) {
          if ((blkrow < blkcnt) || (blkcol == blkcnt)) {
            for (int k = 0; k < aMaxRayWeightSetCount; ++k)
              totTasks++;
            if (aSitePairX.size() > 0)
              totTasks++;
          }
        }
      }

      // Create tasks and update the progress listener
      int taskId = 0;
      for (int blkrow = 0; blkrow < blklmt; ++blkrow) {
        // loop over all block columns

        for (int blkcol = 0; blkcol <= blkrow; ++blkcol) {
          // don't include the non-represented row except when the block column
          // also is non-represented

          if ((blkrow < blkcnt) || (blkcol == blkcnt)) {
            // build tasks for every ray weight set output during prediction

            for (int k = 0; k < aMaxRayWeightSetCount; ++k) {
              // build the task, set the ray weight set limits, debug information,
              // and non-represented path information

              RayUncertaintyTask tsk = new RayUncertaintyTask(taskId++, blkrow, blkcol,
                  aRayWghtPath, aCovMatrixBlockDefn, aCovMatrixServers, aGCFrequency);
              tsk.setPhaseSiteSets(k, k);
              tsk.setDebug(aDebugRaysX);
              if (blkrow == aCovMatrixBlockDefn.blocks())
                tsk.setNRVariancePath(aIODirectory, aNonRepActvNodeVarFileName + "_");

              // add the task to the list and continue

              taskConsumer.accept(tsk);
              if (progressConsumer != null)
                progressConsumer.update(tCt++, totTasks);
            }

            // now see if type AB ray covariance is to be calculated

            if (aSitePairX.size() > 0) {
              // make a hash map of site pairs

              HashMap<Long, HashSet<Long>> pairMap;
              pairMap = new HashMap<Long, HashSet<Long>>();
              for (Map.Entry<Receiver, HashSet<Receiver>> e : aSitePairX.entrySet()) {
                HashSet<Receiver> bRcvrSet = e.getValue();
                HashSet<Long> bRcvrIdSet = new HashSet<Long>();
                pairMap.put(e.getKey().getReceiverId(), bRcvrIdSet);
                for (Receiver bRcvr : bRcvrSet)
                  bRcvrIdSet.add(bRcvr.getReceiverId());
              }

              // create a task and set site pairs, debug information, and non-
              // represented variance paths

              RayUncertaintyTask tsk = new RayUncertaintyTask(taskId++, blkrow, blkcol,
                  aRayWghtPath, aCovMatrixBlockDefn, aCovMatrixServers, aGCFrequency);
              tsk.setSitePairs(pairMap);
              tsk.setDebug(aDebugRaysX);
              if (blkrow == aCovMatrixBlockDefn.blocks())
                tsk.setNRVariancePath(aIODirectory, aNonRepActvNodeVarFileName + "_");

              // add the task to the list and continue

              taskConsumer.accept(tsk);
              if (progressConsumer != null)
                progressConsumer.update(tCt++, totTasks);
            }
          }
        }
      }
    }
    
    private void streamRayUncertaintyTasks(Consumer<RayUncertaintyTask> consumer)
        throws IOException {
      streamRayUncertaintyTasks(consumer,null);
    }
    
    /**
     * Builds all ray uncertainty tasks for subsequent processing. Each block and
     * each RayWeightSet gets a task so given n blocks and m RayWeightSets there
     * will n*m tasks. In addition there will be 1 RayWeightSet extra if site
     * pair covariance is requested so the total number of tasks will be n*(m+1).
     *
     * @return The list of ray uncertainty tasks to be processed.
     * @throws IOException
     */
    private ArrayList<RayUncertaintyTask> buildRayUncertaintyTasks()
            throws IOException {
        ArrayList<RayUncertaintyTask> tskList = new ArrayList<>(
            aCovMatrixBlockDefn.symmMatrixBlockCount()+1);
        
        streamRayUncertaintyTasks(tskList::add);

        if (aScrnWrtr.isOutputOn()) {
            String s = "      Total Tasks Created                     = " +
                    tskList.size() + NL + NL;
            aScrnWrtr.write(s);
        }

        return tskList;
    }

    /**
     * Reads and loads all input sites. If the source definition is DATABASE and
     * input sites are undefined or equals "all" then null is returned. Otherwise,
     * Sites are read from the defined Network. If the source definition is
     * DATABASE and the input sites are valid site names the IMS network is used
     * to construct temporary receivers. These are used to match names from
     * receivers read from the database. The database receivers will replace the
     * temporary IMS receivers.
     *
     * @return The list of input sites.
     * @throws GMPException
     * @throws IOException
     */
    private ArrayList<Receiver> getInputSites() throws GMPException, IOException {
        String s;

        // output header

        if (aScrnWrtr.isOutputOn()) {
            s = "    Input Receivers ..." + NL;
            aScrnWrtr.write(s);
        }

        // read receiver definitions from properties file ... if receiver definition
        // is not defined or the definition equals "all", and the source definition
        // is the DATABASE then assume all receivers are valid and return null

        s = aProps.getProperty("receiverDefinition", "").trim();
        if ((s.equals("") || s.equalsIgnoreCase("all")) &&
                (aSourceDefinition == SourceDefinition.DATABASE))
            return null;

        // if we got here and the receiver definition equals "all" then throw an
        // error since the source definition is NOT DATABASE

        if (s.equalsIgnoreCase("all")) {
            s = "  Receiver Definition Error: \"All\" is not valid unless the " +
                    "source definition is DATABASE ..." + NL;
            throw new IOException(s);
        } else if (s.equals("")) {
            return getPropertiesFileReceivers();
        }

        // create receiver list and get receivers from properties input ... loop
        // over all receivers in properties file and add to list

        ArrayList<Receiver> siteList = new ArrayList<Receiver>();
        String[] siteSources = Globals.getTokens(s, ";");
        for (String str : siteSources) {
            // get type (IMS or Database (DB))

            String[] siteType = Globals.getTokens(str, ":");

            // Manually specified receiver
            if (siteType.length == 1) // no site type specified
            {
                String[] data = Globals.getTokens(str, " ");
                String sta = data[0];
                double lat = Double.valueOf(data[1]);
                double lon = Double.valueOf(data[2]);
                double elev = Double.valueOf(data[3]);
                int ondate = Integer.valueOf(data[4]);
                int offdate = Integer.valueOf(data[5]);
                Receiver r = new Receiver(-1, sta, lat, lon, elev, ondate, offdate, true);
                siteList.add(r);
                aScrnWrtr.write("      " + r.getSta() + " (" + r.getLatDegrees() + "," +
                        r.getLonDegrees() + ")" + NL);
            } 
        }

        return siteList;
    }

    /**
     * Read and return input site pairs from the property file.
     *
     * @return Input site pairs from the property file.
     * @throws IOException
     */
    private HashMap<Receiver, Receiver> getInputSitePairs() throws IOException {
        String s;

        // output header

        if (aScrnWrtr.isOutputOn()) {
            s = "    Input Site Pairs ..." + NL;
            aScrnWrtr.write(s);
        }

        // now read in site covariance pairs ... first build a map of
        // site name --> site

        HashMap<String, Receiver> siteMap;
        siteMap = new HashMap<String, Receiver>(2 * aReceiverList.size());
        for (int i = 0; i < aReceiverList.size(); ++i) {
            Receiver r = aReceiverList.get(i);
            siteMap.put(r.getSta(), r);
        }

        // read in pairs and parse them into the storage map aSitePair

        HashMap<Receiver, Receiver> sitePairMap = new HashMap<Receiver, Receiver>();
        s = aProps.getProperty("receiverCovariancePairs", "").trim();
        String[] sitePairs = Globals.getTokens(s, ";");
        if (sitePairs != null) {
            for (String sitePair : sitePairs) {
                // get next pair and check for errors

                String[] sites = Globals.getTokens(sitePair, "\t, ");
                if ((sites == null) || (sites.length != 2)) {
                    // throw error sites requires a pair of valid site names

                    s = "Site Pair Formation Error: Site pair input require two " +
                            "valid site names ..." + NL;
                    if (sites != null)
                        s += "                           \"" + sitePair + "\"" + NL;
                    throw new IOException(s);
                }

                Receiver r0 = siteMap.get(sites[0]);
                if (r0 == null) {
                    // throw error: sites[0] is not a member of the input site list

                    s = "Invalid Site Name (site pair): \"" + sites[0] + "\"" + NL;
                    throw new IOException(s);
                }

                Receiver r1 = siteMap.get(sites[1]);
                if (r1 == null) {
                    // throw error: sites[1] is not a member of the input site list

                    s = "Invalid Site Name (site pair): \"" + sites[1] + "\"" + NL;
                    throw new IOException(s);
                }

                // good pair ... add to map

                aScrnWrtr.write("      " + r0.getSta() + " --> " + r1.getSta() + NL);
                sitePairMap.put(r0, r1);
            }
        }

        if (sitePairMap.size() == 0)
            aScrnWrtr.write("      No Pairs" + NL);
        return sitePairMap;
    }

    /**
     * Adds all network receivers in the receiver list to the input site list.
     *
     * @param rcvrs    Input receivers to be added to the input site list.
     * @param siteList Input site list into which the input receivers will be
     *                 added.
     */
    private void addNetworkReceivers(ArrayList<Receiver> rcvrs,
                                     ArrayList<Receiver> siteList) {
        for (int i = 0; i < rcvrs.size(); ++i) {
            Receiver r = rcvrs.get(i);
            siteList.add(r);
            aScrnWrtr.write("    " + r.getSta() + " (" +
                    r.getReceiverId() + ")" + NL);
        }
    }

    /**
     * Adds the input receiver, r, into the input site list (siteList).
     *
     * @param site     Site name.
     * @param sitetype Site network.
     * @param r        Receiver to be add to site list.
     * @param siteList Site list into which the receiver, r, is to be added.
     * @throws IOException
     */
    private void addNetworkReceiver(String site, String sitetype, Receiver r,
                                    ArrayList<Receiver> siteList)
            throws IOException {
        if (r == null) {
            String s = "Error: Site \"" + site + "\" was not found in \"" +
                    sitetype + "\" Network ..." + NL;
            throw new IOException(s);
        }
        siteList.add(r);
        aScrnWrtr.write("      " + r.getSta() + " (" + r.getReceiverId() +
                ")" + NL);
    }

    /**
     * Reads all input phase definitions to be processed and returns a phase list
     * containing the phases. If the source definition is DATABASE and the phases
     * are undefined or equals "all" then null is returned.
     *
     * @return A list of all phase definitions to be processed or null.
     */
    private ArrayList<SeismicPhase> getInputPhases() throws IOException {
        String s;

        // output header

        if (aScrnWrtr.isOutputOn()) {
            s = "    Input Seismic Phases ..." + NL;
            aScrnWrtr.write(s);
        }

        // read phase definition from properties file ... if phases are not defined
        // or definition equals "all", and the source definition is the DATABASE
        // then assume all phases are valid and return null

        s = aProps.getProperty("phaseDefinition", "").trim();
        if ((s.equals("") || s.equalsIgnoreCase("all")) &&
                (aSourceDefinition == SourceDefinition.DATABASE))
            return null;

        // if we got here and the phase definition equals "all" then throw an
        // error since the source definition is NOT DATABASE

        if (s.equalsIgnoreCase("all")) {
            s = "  Phase Definition Error: \"All\" is not valid unless the " +
                    "source definition is DATABASE ..." + NL;
            throw new IOException(s);
        } else if (s.equals("")) {
            s = "  Phase Definition Error: EMPTY list is not valid when the " +
                    "source definition is GEOMODEL or PROPERTIES ..." + NL;
            throw new IOException(s);
        }

        // create phase list and get phases from properties input ... loop over all
        // phases in properties file and add to list

        ArrayList<SeismicPhase> phaseList = new ArrayList<SeismicPhase>();
        String[] phases = Globals.getTokens(s, "\t, ");
        for (String phase : phases) {
            SeismicPhase sp = SeismicPhase.valueOf(phase);
            //SeismicPhase sp = SeismicPhase.P;
            aScrnWrtr.write("      " + sp + NL);
            phaseList.add(sp);
        }

        // return list

        return phaseList;
    }

    /**
     * Builds the observation prediction tasks to be submitted to the predictor.
     *
     * @param obsList        List of observations to be packaged into prediction tasks.
     * @param reqAttr        The attributes to be returned by the predictor.
     * @param numPredPerTask The maximum number of observations in a task.
     * @param geoModel       The Geomodel to be used in the prediction.
     * @return The list of prediction tasks to be submitted to the parallel
     * predictor.
     * @throws Exception 
     */
    private ArrayList<PredictorParallelTask>
    buildPredictorParallelTasks(EnumSet<GeoAttributes> reqAttr,
                                int numPredPerTask,
                                GeoTessModel geoModel) throws Exception {
        PredictorObservation bobs;
        PredictorParallelTask bob;

        // set up predictor observation and predictor parallel task lists

        ArrayList<PredictorObservation> predObs =
                new ArrayList<PredictorObservation>();
        ArrayList<PredictorParallelTask> predPTask =
                new ArrayList<PredictorParallelTask>();

        // build path information for each bundle

        String modelFilePath = "", polyFilePath = "";
//    String modelFileFormat = "";
//    GeoAttributes[] geoAttr = null;
        try {
            //modelFilePath = geoModel.getMetaData().getModelFileName();
            modelFilePath = geoModel.getMetaData().getInputModelFile().getCanonicalPath();
            // modelFileFormat = geoModel.getModelFileFormat();
            //geoAttr = geoModel.getActiveNodeAttributes();
            //geoAttr = geoModel.getMetaData().
            //Polygon3D activePolygon = geoModel.getActiveNodePolygon();
            Polygon3D activePolygon = (Polygon3D) geoModel.getPointMap().getPolygon();
            if (activePolygon == null)
                polyFilePath = "";
            else
                polyFilePath = activePolygon.getPolygonFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (polyFilePath == null) polyFilePath = "";

        // set the last np tasks (approximately) to decrease several observation per
        // task such that the last task has at most nrmin observations (Note: this
        // is a performance tweak so that the processors are not lying around while
        // the last tasks finish).

        int np = 200;
        int nrmin = 10;
        int nr = 0;
        for (int i = 0; i < np; ++i)
            nr += numPredPerTask - (numPredPerTask - nrmin) * i / (np - 1);
        int Nt = (aObservationList.size() - nr) / numPredPerTask;
        int n = numPredPerTask;

        // loop over all contained ObservationsSourceMap in the input observation
        // map

        int tskcnt = 0;
        int obscnt = 0;
        ArrayList<ObservationsSourceMap> posList;
        posList = aInputObservationMap.getList();
        for (int k = 0; k < posList.size(); ++k) {
            // get observation source map and associated phase and receiver id, and
            // list of observation objects ... loop over each observation

            ObservationsSourceMap pspd = posList.get(k);
            SeismicPhase phase = pspd.getPhase();
            long rcvrid = pspd.getSiteId();
            ArrayList<Observation> obsList = pspd.getList();
            for (int i = 0; i < obsList.size(); ++i) {
                // get observation

                Observation obs = obsList.get(i);

                // add BenderObservation to current list until hit max

                SeismicPhase usePhase = phase;
                long srcid = obs.getSourceId();
                Receiver rcvr = aRcvrXMap.get((int) rcvrid);
                Source src = aSrcXMap.get((int) srcid);
                double d = VectorUnit.angleDegrees(rcvr.getUnitVector(),
                        src.getUnitVector());
                if (d < 25.0) {
                    if (phase == SeismicPhase.P)
                        usePhase = SeismicPhase.Pn;
                    else if (phase == SeismicPhase.S)
                        usePhase = SeismicPhase.Sn;
                }

                // make new PredictorObservation

                bobs = new PredictorObservation(src, rcvr, usePhase, reqAttr,
                        true, obs.getIndex());
                if (DebugObservation.containsObservation(obs))
                    bobs.setRayPathReturn(rcvr.getReceiverId(), src.getSourceId());
                predObs.add(bobs);

                // if hit max or end, create a new bundle and add to list

                if ((predObs.size() == n) || (obscnt == aObservationList.size() - 1)) {
                    // create a new bundle and add it to the list

                    bob = new PredictorParallelTask(modelFilePath, "", polyFilePath, predObs);
//          if (aBenderSearchMethod != null)
//            bob.setBenderSearchMethod(aBenderSearchMethod);
                    bob.setDefaultPredictorProperties(aBenderProps);

//          bob.setBenderAllowCMBDiffraction(true);
                    bob.setOutput(aNodeOutputTaskInfoFlag);
                    bob.setProfiler(aPredictionProfilerSamplePeriod,
                            aPredictionProfilerNodeVerbose);
                    predPTask.add(bob);

                    // clear list of bender observations for the next bundle

                    predObs.clear();

                    // increment the task count and see if we are at the last np (or so)
                    // tasks where the observation count will decrease linearly down to
                    // nrmin by the last task

                    ++tskcnt;
                    if (tskcnt > Nt) {
                        n = numPredPerTask -
                                (numPredPerTask - nrmin) * (tskcnt - Nt - 1) / (np - 1);
                        if (n < nrmin) n = nrmin;
                    }
                }

                // increment the observation count and continue

                ++obscnt;
            } // end for (ObservationTomo ob : obs)
        } // end for (int k = 0; k < posList.size(); ++k)

        // done ... return prediction task list

        return predPTask;
    }

    /**
     * Sets the screen/file output modes for the GeoTomography, IODB, and LSQR
     * writers based on the output model property
     *
     * @throws IOException
     */
    private void setOutputWriterMode() throws IOException {
        String outputMode = aProps.getProperty("outputMode", "none").trim()
                .toLowerCase();

        if (outputMode.equalsIgnoreCase("screen")) // turn on screen output only
        {
            aScrnWrtr.setScreenOutputOn();
            aScrnWrtr.setWriterOutputOff();
        } else if (outputMode.equalsIgnoreCase("file"))   // turn on file output only
        {
            aScrnWrtr.setWriterOutputOn();
            aScrnWrtr.setScreenOutputOff();
        } else if (outputMode.equalsIgnoreCase("both"))   // default to both on
        {
            aScrnWrtr.setScreenAndWriterOutputOn();
        } else
            aScrnWrtr.setOutputOff();                     // turn off all output
    }

    /**
     * Returns a handle to the GUI JFrame
     *
     * @return
     */
    public RayUncertaintyUI getGUI() {
        return aUI;
    }

    /**
     * Called by interpolateUncertainties() to set the interpolated uncertainty
     * results for the type AA ray rcvrA/srcid, or the type AB ray
     * rcvrA/rcvrB/srcid into the aUncOut output file. This function is only
     * called if the source type is DATABASE or PROPERTIESFILE.
     *
     * @param rcvrA The receiver A associated with this ray.
     * @param rcvrB The receiver B associated with this ray (may be equal to
     *              rcvrA or null for type AA rays.
     * @param srcid The source id associated with this ray.
     * @param unc   The uncertainty results vector to be output for this ray.
     * @throws IOException
     */
    private void writeUncertaintyResult(Receiver rcvrA, Receiver rcvrB,
                                        long srcid, double[] unc)
            throws IOException {
        // only output if the output file is defined

        if (aUncOut != null) {
            // write out type AA or type AB ray header

            if ((rcvrB == null) || (rcvrA == rcvrB))
                aUncOut.write(String.format("%6s          %10d",
                        rcvrA.getSta(), srcid));
            else
                aUncOut.write(String.format("%6s(%6s)  %10d", rcvrA.getSta(),
                        rcvrB.getSta(), srcid));

            // output uncertainty information

            aUncOut.write(String.format("  %8.4f  %8.4f  %8.4f  %8.4f  %8.4f",
                    unc[3], unc[0], unc[1], unc[2], unc[4]) +
                    NL);
        }
    }

    /**
     * Called only by function buildInfluenceObservationList(...) to see if the
     * input source file name (fn), phase, receiver A, and receiver B inputs
     * define a ray contained in the static debug ray list contained in the
     * DebugRayOutput object. If not the function returns. Otherwise, the input
     * source (src) is set as the requesting source, the input influence source
     * and receiver ids (srcXId and rcvrXId) are set as influencing the debug ray
     * and the input source and receiver weights (weightS, weightsR, and
     * weightsRB) are added to the DebugRayOutput definition.
     *
     * @param fn        The source model name used to check if this is a debug
     *                  ray (may be DATABASE or PROPERTIESFILE).
     * @param phase     The phase used to check if this is a debug ray.
     * @param src       The source used to check if this is a debug ray.
     * @param rcvrA     The receiver A used to check if this is a debug ray.
     * @param rcvrB     The receiver B used to check if this is a debug ray.
     * @param srcXId    The influence (tomographic) source id that influences
     *                  the input requested ray.
     * @param rcvrXId   The influence (tomographic) receiver A id that influences
     *                  the input requested ray.
     * @param weightsS  The influence source weights.
     * @param weightsR  The influence receiver A weights.
     * @param weightsRB The influence receiver B weights.
     * @param obs       The Observation associated with srcXId and rcvrXId.
     * @throws GMPException
     */
    private void checkDebugRay(String fn, SeismicPhase phase, Source src,
                               Receiver rcvrA, Receiver rcvrB,
                               long srcXId, long rcvrXId,
                               HashMap<Integer, Double> weightsS,
                               HashMap<Integer, Double> weightsR,
                               HashMap<Integer, Double> weightsRB,
                               Observation obs) throws GMPException {
        DebugRayOutput dro;

        // see if rcvrA type AA is a debug ray

        dro = DebugRayOutput.getDebugRay(fn, phase, rcvrA, null,
                src.getSourceId());
        if (dro != null) {
            // this is a debug ray ... set the source and weights if
            // they haven't been

            if (!dro.isSourceSet()) {
                dro.setSource(src);
                dro.setInfluenceWeights(weightsR, weightsRB, weightsS);
            }

            // add this influence site/src as a debug site/src

            DebugObservation.addObservation(obs, aRcvrXMap.get((int) rcvrXId),
                    aSrcXMap.get((int) srcXId));
            HashSet<Long> dbgSitesBX = getDebugSiteBXIdSet(srcXId, rcvrXId);
            dbgSitesBX.add(rcvrXId);
        }

        // if this is a type AB ray check for debug

        if ((rcvrB != null) && (rcvrB != rcvrA)) {
            // type AB get debug ray if defined.

            dro = DebugRayOutput.getDebugRay(fn, phase, rcvrA, rcvrB,
                    src.getSourceId());
            if (dro != null) {
                // this is a debug ray ... set the source and weights if
                // they haven't been

                if (!dro.isSourceSet()) {
                    dro.setSource(src);
                    dro.setInfluenceWeights(weightsR, weightsRB, weightsS);
                }

                // add all B influence site/src rays as debug site/src rays with site A
                // ray

                DebugObservation.addObservation(obs, aRcvrXMap.get((int) rcvrXId),
                        aSrcXMap.get((int) srcXId));
                HashSet<Long> dbgSitesBX = getDebugSiteBXIdSet(srcXId, rcvrXId);
                for (Map.Entry<Integer, Double> eRB : weightsRB.entrySet()) {
                    long rcvrBXId = aRcvrXMap.get(eRB.getKey()).getReceiverId();
                    dbgSitesBX.add(rcvrBXId);
                    HashSet<Long> dbgSitesBXT = getDebugSiteBXIdSet(srcXId, rcvrBXId);
                    dbgSitesBXT.add(rcvrBXId);
                }
            }
        }
    }

    /**
     * Called only by function checkDebugRay(...) above to return the set of
     * all site B receiver ids associated with the input source id and type A
     * receiver id. If no entry is found then one is added to the aDebugRaysX
     * container and an empty set is returned to the caller to add in type B site
     * ids. If one is found then it is returned to the caller so that more type
     * B site id may be added.
     *
     * @param srcXId  Influence (tomographic) source id for which the set of
     *                corresponding receiver B entries will be returned.
     * @param rcvrXId Influence (tomographic) receiver A id for which the set of
     *                corresponding receiver B entries will be returned.
     * @return The set of receiver B ids associated with the input influence
     * source and receiver A id.
     */
    private HashSet<Long> getDebugSiteBXIdSet(long srcXId, long rcvrXId) {
        // Get the map of all sites associated with the input tomographic influence
        // source id ... if one is not found then add a new map

        HashMap<Long, HashSet<Long>> dbgSiteMapX = aDebugRaysX.get(srcXId);
        if (dbgSiteMapX == null) {
            dbgSiteMapX = new HashMap<Long, HashSet<Long>>();
            aDebugRaysX.put(srcXId, dbgSiteMapX);
        }

        // find the set of all site B ids associated with the input site A id.
        // If one is not found then add a new set

        HashSet<Long> dbgSitesBX = dbgSiteMapX.get(rcvrXId);
        if (dbgSitesBX == null) {
            dbgSitesBX = new HashSet<Long>();
            dbgSiteMapX.put(rcvrXId, dbgSitesBX);
        }

        // return the set

        return dbgSitesBX;
    }

    /**
     * Called by each returning parallel task result (RayUncertaintyTaskResult)
     * in solveRayUncertainty() to add any debug results to the appropriate entry
     * in the DebugRayOutput static list of requested debug rays. This function
     * loops over all defined debug rays and, if the input debug result phase
     * site or debug resut phase site/site map is not null calls the add function
     * for each defined DebugRayOutput object. Note that only entries in the
     * input map that match the appropriate DebugRayOutput specification are
     * actually added. It is possible that for any specific DebugRayOutput in
     * the list that no actual DebugRayOutputEntries will be added even if the
     * input map is non-null.
     *
     * @param mapAA The type AA debug result phase site map.
     * @param mapAB The type AB debug result phase site/site map.
     * @throws IOException
     */
    private void addDebugRayTaskResults(DebugResultsPhaseSiteMap mapAA,
                                        DebugResultsPhaseSiteSiteMap mapAB)
            throws IOException {
        // get the static DebugRayOutput list from the DebugRayOutput object and
        // loop over each entry

        ArrayList<DebugRayOutput> droList = DebugRayOutput.getList();
        for (int r = 0; r < droList.size(); ++r) {
            // get the next DebugRayOutput object and add the input maps if they are
            // not null.

            DebugRayOutput dro = droList.get(r);
            if (mapAA != null) dro.add(mapAA);
            if (mapAB != null) dro.add(mapAB);
        }
    }

}
