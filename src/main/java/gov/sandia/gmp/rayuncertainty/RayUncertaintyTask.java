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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.parallelutils.CatchRWException;
import gov.sandia.gmp.parallelutils.ParallelTask;
import gov.sandia.gmp.parallelutils.ReadWriteCatch;
import gov.sandia.gmp.rayuncertainty.containers.FinalVariancesPhaseSiteMap;
import gov.sandia.gmp.rayuncertainty.containers.FinalVariancesPhaseSiteSiteMap;
import gov.sandia.gmp.rayuncertainty.containers.FinalVariancesSourceMap;
import gov.sandia.gmp.rayuncertainty.containers.RayWeightsPhaseSiteMap;
import gov.sandia.gmp.rayuncertainty.containers.RayWeightsSourceMap;
import gov.sandia.gmp.rayuncertainty.debugray.DebugResultsPhaseSiteMap;
import gov.sandia.gmp.rayuncertainty.debugray.DebugResultsPhaseSiteSiteMap;
import gov.sandia.gmp.rayuncertainty.debugray.DebugTaskResults;
import gov.sandia.gmp.rayuncertainty.debugray.DebugTaskResultsEntry;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.matrix.SparseMatrixVector;
import gov.sandia.gmp.util.numerical.matrixblock.MatrixBlock;
import gov.sandia.gmp.util.numerical.matrixblock.MatrixBlockDefinition;
import gov.sandia.gmp.util.numerical.matrixblock.MatrixBlockFileServer;
import gov.sandia.gmp.util.profiler.Profiler;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * Calculates all partial variances for rays defined in this blocks phase/site
 * input list of sets. That list contains a starting set and ending set id
 * which identifies one or more files containing phase/site ray information for
 * this block. These are processed against the blocks covariance entries (also
 * read from a file) to calculate the partial variance for each observation
 * defined in the sets. They are partial in the sense that only contributions
 * from this one covariance block are returned. The client then assembles the
 * results from all blocks to complete the total contribution for all defined
 * rays across all ray weight sets.
 * <p>
 * If the start and end set id is -1 then a set of site pairs is input that is
 * used to calculate type AB (site A with site B) covariance. In this case the
 * ray weights are read from a single set (written with id -1). Currently this
 * object processes type AA (site A with site A) variance or type AB only but
 * not both. The results are returned in a PartialVariancesPhaseSiteMap object
 * for type AA solutions and a PartialVariancesPhaseSiteSiteMap object for type
 * AB solutions.
 * <p>
 * So, if type AA rays are processed two ray weight block row files are read
 * for each defined set. The results from each subsequent set are summed to the
 * total solution for this block. The two ray weight block row files that are
 * read are for the input block row (aBlockRow) and the input block column
 * (aBlockCol). If the block row = the block column then only one file is read.
 * If, however, type AB rays are processed then up to 4 ray weight block row
 * files are read (always for a single set with id = -1) ... 1 for site A using
 * the block row, 1 for site A using the block column, and a similar pair for
 * site B. If block row = block column then only two files are read ... 1 for
 * site a A and 1 for site B.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class RayUncertaintyTask extends ParallelTask {
    /**
     * Used to call the site ray weight read functionality wrapped with
     * CatchRWException functionality designed to give a read/write multiple
     * chances for success. The write function is never used for this class.
     *
     * @author jrhipp
     */
    private class ReadBlockRowRayWeights implements ReadWriteCatch {
        private RayWeightsPhaseSiteMap aRayWeights = null;

        public ReadBlockRowRayWeights(RayWeightsPhaseSiteMap brw) {
            aRayWeights = brw;
        }

        public String catchExceptionString(int ecnt, Exception ex, String fp) {
            return exceptionString(ecnt, ex, fp);
        }

        public void readCatch(String fp) throws IOException {
            aRayWeights.read(fp);
            results.addRayWeightBlockRead(new File(fp));
        }

        public void writeCatch(String fp) throws IOException {
            // never performed
        }
    }

    /**
     * Used to call the covariance matrix block read functionality wrapped with
     * CatchRWException functionality designed to give a read/write multiple
     * chances for success. The write function is never used for this class.
     *
     * @author jrhipp
     *
     */
//  private class ReadBlock implements ReadWriteCatch
//  {
//    public String catchExceptionString(int ecnt, Exception ex, String fp)
//    {
//      return exceptionString(ecnt, ex, fp);
//    }
//
//    public void readCatch(String fp) throws IOException
//    {
//      aBlock = MatrixBlockInfo.readBlock(aBlockRow, aBlockCol, aMtrxBlkInfo,
//                                         fp, true);
//    }
//
//    public void writeCatch(String fp) throws IOException
//    {
//      // never performed
//    }    
//  }

    /**
     * Used to call the NRVariance read functionality wrapped with
     * CatchRWException functionality designed to give a read/write multiple
     * chances for success. The write function is never used for this class.
     *
     * @author jrhipp
     */
    private class ReadNRVariance implements ReadWriteCatch {
    	// TODO PS: Remove the phase field. It is no longer required.
        private SeismicPhase aPhase = null;

    	// TODO PS: Remove the phase field. This just becomes a default
        // TODO PS: constructor.
        public ReadNRVariance(SeismicPhase phase) {
            aPhase = phase;
        }

        public String catchExceptionString(int ecnt, Exception ex, String fp) {
            return exceptionString(ecnt, ex, fp);
        }

        public void readCatch(String fp) throws IOException {
            FileInputBuffer fib = new FileInputBuffer(fp);

            // TODO PS: Replace the line below with
            // TODO PS:
            // TODO PS: 	aNonRepresentedVar = fib.readDoubles();
            double[] nrvar = fib.readDoubles();
            fib.close();
            // TODO PS: Remove the line below. No longer have a map.
            aNonRepresentedVarMap.put(aPhase, nrvar);
        }

        public void writeCatch(String fp) throws IOException {
            // never performed
        }
    }

    /**
     * Read/Write exception object to avoid timeout errors during read/write
     * to the file system.
     */
    private CatchRWException aReadWriteException = null;

    /**
     * The task id ... Assigned to the task result for return.
     */
    private int aTaskId = -1;

    /**
     * The covariance matrix block row index.
     */
    private int aBlockRow = -1;

    /**
     * The covariance matrix block column index.
     */
    private int aBlockCol = -1;

    /**
     * The path to where all block row maps are stored.
     */
    private String aBlockRowMapPath = "";

    /**
     * Path from where the phase specific non-represented variance files are to
     * be read.
     */
    private String aNRVariancePath = "";

    /**
     * The non-represented variance file name (without the terminating phase
     * name which is added based on the phase setting at the point of reading
     * the file).
     */
    private String aNRVarianceFileName = "";

    /**
     * The start ray weight set id for type AA solutions. Set to -1 if type AB
     * solution is requested.
     */
    private int aSetMin = -1;

    /**
     * The end ray weight set id for type AA solutions. Set to -1 if type AB
     * solution is requested.
     */
    private int aSetMax = -1;

    /**
     * The site pair map used for type AB solutions. Set to null if type AA
     * solution is requested.
     */
    private HashMap<Long, HashSet<Long>> aSitePairs = null;

    /**
     * The covariance matrix block server paths
     */
    private MatrixBlockFileServer aBlockFileServers = null;

    /**
     * The block matrix layout description.
     */
    private MatrixBlockDefinition aMtrxBlkDefn = null;

    /**
     * The task information output flag. If true task start and stop times
     * and the task index is output on entry and exit.
     */
    private boolean aOutputTaskInfo = false;

    /**
     * The profiler sample period (milliseconds). If less than 1 it is not
     * used.
     */
    private long aProfilerSamplePeriod = -1;

    /**
     * Returned set of phase/site/source partial variances from this block for
     * the defined start and end ray weight set id's. Set to null if type AB
     * solution is requested.
     */
    private transient FinalVariancesPhaseSiteMap aSolutionAA = null;

    /**
     * Returned set of phase/site/site/source partial variances from this block
     * for the ray weight set id = -1. Set to null if type AB solution is
     * requested.
     */
    private transient FinalVariancesPhaseSiteSiteMap aSolutionAB = null;

    /**
     * The non-represented matrix column variance array. This must be read from
     * disk and contains all variances for matrix columns that were not hit by
     * any rays during the tomography calculation. Each phase has a unique array
     * stored in this maps association. This array is only read if
     * aBlockRow = aBlockCol = aMtrxBlkInfo.blocks().
     */
    // TODO PS: Remove aNonRepresentedVarMap and replace it with
    // TODO PS: private transient double[] aNonRepresentedVar = null;
    // TODO PS:
    // TODO PS: Also, consider making the new variable static. It only
    // TODO PS: needs to be loaded once and is the same for all tasks
    // TODO PS: that encounter this host as a thread.
    private transient HashMap<SeismicPhase, double[]>
            aNonRepresentedVarMap = null;

    /**
     * The matrix block defined from the MatrixBlockDefinition.
     */
    private transient MatrixBlock aMB = null;

    /**
     * The in-core matrix block read from disk.
     */
    private transient double[][] aBlock = null;

    /**
     * Map of source id -> site A id -> site B id debug rays. If site A id =
     * site B id then the debug ray is a type AA debug ray. Type AA debug ray
     * results are returned to the client using the DebugResultsPhaseSiteMap
     * aDebugAA, while type AB debug ray results are returned to the client using
     * the DebugResultsPhaseSiteSiteMap aDebugAB.
     */
    private HashMap<Long, HashMap<Long, HashSet<Long>>> aDebugRays = null;

    /**
     * Contains the debug results for all type AA rays defined in the container
     * aDebugRays. These are returned to the client for output. If none were
     * defined this container is null.
     */
    private transient DebugResultsPhaseSiteMap aDebugAA = null;

    /**
     * Contains the debug results for all type AB rays defined in the container
     * aDebugRays. These are returned to the client for output. If none were
     * defined this container is null.
     */
    private transient DebugResultsPhaseSiteSiteMap aDebugAB = null;

    /**
     * The node host name.
     */
    private transient String aHostName = "";

    /**
     * The total number of rays loaded whose elements were used to calculate
     * partial variances.
     */
    private transient long aRaysLoadedCount = 0;

    /**
     * The total number of ray elements loaded which were used to calculate
     * partial variances.
     */
    private transient long aRayElementsLoadedCount = 0;

    /**
     * The time taken to read all site/source ray weight files from disk.
     */
    private transient long aBlockRowMapReadTime = 0;

    /**
     * The total number of site/source ray weight files read from disk.
     */
    private transient int aBlockRowMapReadCount = 0;

    /**
     * The time taken to read the covariance matrix block from disk.
     */
    private transient long aCovBlockReadTime = 0;

    /**
     * The time spent reading the non-represented variance vector.
     */
    private transient long aNRVarReadTime = 0;

    /**
     * The total number of phase-specific non-represented variance vector read.
     */
    private transient int aNRVarReadCount = 0;

    /**
     * The time taken to process partial variances from all site/source
     * ray weight files.
     */
    private transient long aProcessTime = 0;

    /**
     * Returns the garbage collector process time, or zero if it was not called.
     * Note this time does not include any GC calls ordered by the system.
     */
    private transient long aGCProcTime = 0;

    /**
     * The total time spent in the run method.
     */
    private transient long aTotalTime = 0;

    /**
     * Used to debug read/write/process errors.
     */
    private transient String aStateMessg = "";

    /**
     * Saves and returns any read/write fail errors. If the read/write attempts
     * exceed aReadWriteFailLimit then a real error is thrown and returned.
     */
    private transient String aReadWriteFail = "";
    
    private transient RayUncertaintyTaskResult results = null;

    /**
     * The number of times the run method is entered before calling the garbage
     * collector.
     */
    private static int aGCCallFreq = 10;

    /**
     * A static counter tabulating the number of times the run function is called.
     * When it equals aGCCallFreq the garbage collector is called and this value
     * is zeroed.
     */
    private static int aGCCallCount = 0;
    
    /**
     * Standard constructor. Defines all necessary inputs except.
     * 1)  aSetMin and aSetMax. Set with function setPhaseSiteSets(...) for a
     * type AA ray solution.
     * 2)  aSitePairs. Set with function setSitePairs(...) for a type AB ray
     * solution.
     * 3)  aDebugSiteAId, aDebugSiteBId, and aDebugSourceId (not required). Only
     * used to output debug rays. Set by calling function setDebug(...).
     * 4)  aNonRepresentedVariance path (only required if aBlockRow = aBlockCol =
     * aMtrxBlkInfo.blocks()). Set with function setNRVariancePath(...).
     * 5)  aOutputTaskInfo (not required). call setOutputTaskFlag(boolean otf)
     * to set.
     *
     * @param taskid        The task id of this task.
     * @param blkrow        The covariance matrix block row to be loaded.
     * @param blkcol        The covariance matrix block column to be loaded.
     * @param blkrowmappath The path to where all block row maps are stored for
     *                      all site/ray weight files.
     * @param mtrxBlkDefn   The matrix block definition object defining the
     *                      structure of all input covariance matrix blocks.
     * @param fileServers   The list of all covariance matrix block file server
     *                      paths.
     */
    public RayUncertaintyTask(int taskid, int blkrow, int blkcol,
                              String blkrowmappath,
                              MatrixBlockDefinition mtrxBlkDefn,
                              MatrixBlockFileServer fileServers, int gcFreq) {
        aTaskId = taskid;
        aBlockRow = blkrow;
        aBlockCol = blkcol;
        aBlockRowMapPath = blkrowmappath;
        aMtrxBlkDefn = mtrxBlkDefn;
        aBlockFileServers = fileServers;
        if (gcFreq != aGCCallFreq) setGCFrequency(gcFreq);
        aMtrxBlkDefn.setBlockObserversOff();
        super.setIndex(aTaskId);
    }
    
    public int getTaskId() { return aTaskId; }

    /**
     * Sets the non-represented variance path and file name.
     *
     * @param pth The non-represented variance path.
     * @param fn  The non-represented variance file name (does not include the
     *            final phase name which is added using the current phase setting
     *            before the file is read).
     */
    public void setNRVariancePath(String pth, String fn) {
        aNRVariancePath = pth;
        aNRVarianceFileName = fn;
    }

    public static synchronized void setGCFrequency(int gcFreq) {
        aGCCallFreq = gcFreq;
    }

    /**
     * Calls increment the GC call count and calls the garbarge collector when
     * the count == the preset frequency
     */
    public static synchronized long checkGC() {
        // increment count ... see if limit has been attained

        ++aGCCallCount;
        if (aGCCallCount == aGCCallFreq) {
            // call garbarge collector and reset count

            long strtWriteTime = (new Date()).getTime();
            Runtime.getRuntime().gc();
            aGCCallCount = 0;
            return (new Date()).getTime() - strtWriteTime;
        }
        return 0;
    }

    /**
     * Sets the minimum and maximum ray weight set id's to be processed for type
     * AA ray solutions. The input set id's are checked for validity and an error
     * is thrown if they are not positive and setmax >= setmin.
     *
     * @param setmin The minimum ray weight set id to be processed.
     * @param setmax The maximum ray weight set id to be processed.
     * @throws IOException
     */
    public void setPhaseSiteSets(int setmin, int setmax) throws IOException {
        // check for proper input

        if (setmin > setmax) {
            throw new IOException("Error: Minimum phase/site set number (" +
                    setmin + ") cannot exceed the maximum number (" +
                    setmax + ") ...");
        }
        if (setmin < 0) {
            throw new IOException("Error: Minimum phase/site set number (" +
                    setmin + ") cannot be less than 0 " +
                    "(non-represented) ...");
        }

        // set limits

        aSetMin = setmin;
        aSetMax = setmax;
    }

    /**
     * Sets the site pair map used to process type AB ray soltuions.
     *
     * @param sitepairs The input site pair map.
     */
    public void setSitePairs(HashMap<Long, HashSet<Long>> sitepairs) {
        aSitePairs = sitepairs;
    }

    /**
     * Sets the task output flag setting. If true the task outputs
     * startup and completion information.
     *
     * @param otf The task output flag setting.
     */
    public void setOutputTaskFlag(boolean otf) {
        aOutputTaskInfo = otf;
    }

    /**
     * Sets the profilers sample period (milliseconds).
     *
     * @param psp The profilers sample period (milliseconds).
     */
    public void setProfilerSamplePeriod(long psp) {
        aProfilerSamplePeriod = psp;
    }

    /**
     * Standard run method called by the ParallelUtils base class to execute a
     * task on a processing node.
     */
    @Override
    public void run() {
        // set start time

        long strtTotalTime = (new Date()).getTime();
        aGCProcTime += checkGC();

        // create and initialize task result object

        results = new RayUncertaintyTaskResult(aTaskId, aBlockRow, aBlockCol);
        results.setTaskSubmitTime(getSubmitTime());
        results.setIndex(getIndex());
        setResult(results);

        // convert file paths

        convertFilePaths();

        aMtrxBlkDefn.setSubBlockSizeToBlockSize();
        aMB = new MatrixBlock(aBlockRow, aBlockCol, aMtrxBlkDefn);

        Profiler profiler = null;
        try {
            // get host name and assign

            aReadWriteException = new CatchRWException(5000, 10);
            aHostName = (InetAddress.getLocalHost()).getHostName();
            results.setHostName(aHostName);
            outputTaskInfo(results.getHostName(), "Entry", "");

            // create profiler if requested

            if (aProfilerSamplePeriod > 0) {
                profiler = new Profiler(Thread.currentThread(), aProfilerSamplePeriod,
                        "RayUncertaintyTask:" + aHostName);
                profiler.setTopClass("gov.sandia.gmp.observationprediction.PredictorParallelTask");
                profiler.setTopMethod("run");
                profiler.accumulateOn();
            }

            // Process site A -> site A variance and/or site A -> B covariance ray
            // partial sums. Throw error if neither are defined.

            // TODO PS: Remove the line below. No longer required
            aNonRepresentedVarMap = new HashMap<SeismicPhase, double[]>();
            if (aSetMin >= 0) {
                aSolutionAA = new FinalVariancesPhaseSiteMap();
                solveSiteRaySetAA(results);
            } else if (aSitePairs != null) {
                aSolutionAB = new FinalVariancesPhaseSiteSiteMap();
                solveSiteRaySetAB(results);
            } else {
                throw new IOException("Error: Type AA nor type AB ray weight " +
                        "solutions were set (function " +
                        "setPhaseSiteSets(...) or setSitePairs(...) " + "" +
                        "must be called ...");
            }

            // done ... reset loaded variables

            aBlock = null;
            aMB.unLoad();
            aMB = null;

            // TODO PS: Remove the line below. No longer required
            aNonRepresentedVarMap = null;

            // set times and counts and variance maps

            results.setTimes(aBlockRowMapReadTime, aCovBlockReadTime, aNRVarReadTime,
                    aProcessTime, aGCProcTime);
            results.setCounts(aBlockRowMapReadCount, aRaysLoadedCount,
                    aRayElementsLoadedCount, aNRVarReadCount);
            results.setVarianceMaps(aSolutionAA, aSolutionAB);
            results.setDebugResultsMaps(aDebugAA, aDebugAB);

            // set total time and exit

            aTotalTime = (new Date()).getTime() - strtTotalTime;
            results.setTotalTime(aTotalTime);

            // turn off profiler if on and set into results

            if (profiler != null) {
                profiler.stop();
                profiler.printAccumulationString();
                results.setProfilerContent(profiler.getProfilerContent());
                profiler = null;
            }
            outputTaskInfo(results.getHostName(), "Exit", "");
        } catch (Exception ex) {
            // turn off profiler if on

            if (profiler != null) {
                profiler.stop();
                profiler.printAccumulationString();
                profiler = null;
            }

            // error ... assign to results, output, and return

            String s = exceptionString(0, ex, "");
            System.out.println(s);
            results.setException(new Exception(s));
            results.setReadWriteFailErrors(aReadWriteFail);

            outputTaskInfo(results.getHostName(), "Error", "");
        }
    }

    /**
     * Reads the covariance matrix block required by this task.
     *
     * @throws IOException
     */
    private void loadBlock() throws IOException {
//    // create the ReadWriteCatch object that will allow for rereads if
//    // file timeout errors occur.
//
//    ReadBlock rsrrw = new ReadBlock();

        // get block path

        String fh = "ginv";
        String fp = aBlockFileServers.getPath(fh, aBlockRow, aBlockCol) +
                File.separator + fh;

        aMB.setSourcePathFileHeader(fp, fh);
        aStateMessg = "Reading: " + fp;
        aMB.readBlockCatch();
        aStateMessg = "Processing";
        aCovBlockReadTime += aMB.getReadTime();
//
//    // read block and save read time
//
//    long strtWriteTime   = (new Date()).getTime();
//    aStateMessg          = "Reading: " + fp;
//    aReadWriteFail      += aReadWriteException.read(fp, rsrrw);
//    aStateMessg          = "Processing";
//    aCovBlockReadTime   += (new Date()).getTime() - strtWriteTime;
//
//    // transpose matrix to block to proper form and exit
//
//    Matrix.transposeSymmetric(aBlock);
    }

    //***************** Site A -> Site A read and process code ******************

    /**
     * Accumulates site A -> site A partial variances into aSolutionAA using
     * ray weight set files read for this block (aBlockRow and aBlockCol).
     *
     * @throws IOException
     */
    private void solveSiteRaySetAA(RayUncertaintyTaskResult out) throws IOException {
        FinalVariancesSourceMap pspv;
        RayWeightsSourceMap psrwRow, psrwCol;
        RayWeightsPhaseSiteMap blkRayWghtsRow, blkRayWghtsCol;

        // output process header

        if (aOutputTaskInfo) {
            System.out.println("  Processing AA Task: " + aTaskId + ", Block " +
                    aBlockRow + "," + aBlockCol);
            System.out.println("    Total Phase/Site Sets = " +
                    (aSetMax - aSetMin + 1));
        }

        // loop over all defined ray weight sets for this block

        for (int i = aSetMin; i <= aSetMax; ++i) {
            // read in the phase site map of ray weights for the block row index and
            // sum the ray and element counts to the totals read

            blkRayWghtsRow = new RayWeightsPhaseSiteMap();
            readBlockSiteRayElementFile(i, aBlockRow, blkRayWghtsRow);
            sumRayWeightCount(blkRayWghtsRow);

            // assign the column phase site map ray weights from the row if the block
            // row and column are the same ... otherwise read them in for the block
            // column

            if (aBlockRow == aBlockCol)
                blkRayWghtsCol = blkRayWghtsRow;
            else {
                // read in the phase site map of ray weights for the block column index
                // and sum the ray and element counts to the totals read

                blkRayWghtsCol = new RayWeightsPhaseSiteMap();
                readBlockSiteRayElementFile(i, aBlockCol, blkRayWghtsCol);
                sumRayWeightCount(blkRayWghtsCol);
            }

            // loop over all phase / site pairs in aBlkRayWghtsRow

            ArrayList<RayWeightsSourceMap> psrwList = blkRayWghtsRow.getList();
            for (int j = 0; j < psrwList.size(); ++j) {
                // get the jth entry and set its phase and site id

                psrwRow = psrwList.get(j);
                SeismicPhase ph = psrwRow.getPhase();
                long siteid = psrwRow.getSiteId();

                // find the matching phase and site entry in the column map and make
                // sure both the row and column maps have non-zero entries

                psrwCol = blkRayWghtsCol.get(ph, siteid);
                if ((psrwRow.getTotalRayCount() > 0) &&
                        (psrwCol.getTotalRayCount() > 0)) {
                    // have non-zero entries ... get/create the phase/site partial
                    // variances container for the current phase / site ... make a new
                    // entry if it was added to the map

                    pspv = aSolutionAA.getSet(ph, siteid);

                    // read in phase specific non-represented variance array if it is
                    // required and not yet loaded

                    // TODO PS: Replace the second line below with
                    // TODO PS:
                    // TODO PS: (aNonRepresentedVar == null)) readNRVariance();
                    if ((aBlockRow == aMtrxBlkDefn.blocks()) &&
                            (aNonRepresentedVarMap.get(ph) == null)) readNRVariance(ph);

                    // build all ray uncertainty information from the row and column
                    // ray weights and store in the partial variances object

                    buildRayUncertaintyAA(pspv, psrwRow, psrwCol, out);
                }
            }
        }
    }

    /**
     * Performs the partial variance sum for all rays whose ray weights are
     * contained in the block row and column ray weight source maps input into
     * this function. The results are summed to the partial variance source
     * map (pspv) also input into this function. These source maps are defined
     * for a specific phase/site pair a partial variance is only summed for a
     * specific source if it is defined in both of the input ray weight source
     * maps.
     *
     * @param pspv A specific phase / site partial variance source map that will
     *             contain the results of the ray uncertainty calculation on
     *             exit.
     *             blkRow A specific phase / site ray weights source map defined for
     *             the block row (aBlockRow) whose entries will be combined with
     *             the block column ray weights source map (blkCol) and the
     *             the input block covariance matrix (aBlock), if this is not
     *             a non-represented block, to form ray uncertainty components
     *             in the output partial variance source map (pspv).
     *             blkCol A specific phase / site ray weights source map defined for
     *             the block column (aBlockCol) whose entries will be combined
     *             with the block row ray weights source map (blkCol) and the
     *             the input block covariance matrix (aBlock), if this is not
     *             a non-represented block, to form ray uncertainty components
     *             in the output partial variance source map (pspv).
     * @throws IOException
     */
    private void buildRayUncertaintyAA(FinalVariancesSourceMap pspv,
                                       RayWeightsSourceMap brwRow,
                                       RayWeightsSourceMap brwCol,
                                       RayUncertaintyTaskResult out)
            throws IOException {
        DebugTaskResults dtr = null;
        double[] pv;
        int pvcnt = 0;
        SparseMatrixVector smvRow;
        SparseMatrixVector smvCol;

        // read in requested matrix block if this is not a non-represented block and
        // it has not yet been loaded

        //if ((aBlock == null) && (aBlockRow < aMtrxBlkDefn.blocks()))
        if (aBlockRow < aMtrxBlkDefn.blocks()) {
            if (!aMB.isBlockLoaded()) loadBlock();
            aBlock = aMB.getLockedBlock(true);
        } else
            aBlock = null;

        // set the process start time and get the phase and site id a

        long startTime = (new Date()).getTime();
        SeismicPhase phase = pspv.getPhase();
        long siteId = pspv.getSiteId();

        // get the block size and the row and column start indices of the
        // block defined for this task and set the diagonal block flag

        int blksze = aMtrxBlkDefn.blockSize();
        int colStrt = aBlockCol * blksze;
        int rowStrt = aBlockRow * blksze;

        // TODO PS: Remove the two comment lines and the 3 code lines below. No
        // TODO PS: longer required
        // if this is a non-represented block load the non-represented variance
        // array

        double[] nrVarArray = null;
        if (aBlockRow == aMtrxBlkDefn.blocks())
            nrVarArray = aNonRepresentedVarMap.get(pspv.getPhase());

        // loop over all entries in the input block row ray map and build partial
        // variance for rays from the contained ray weight set.

        brwRow.resetIterator();
        while ((smvRow = brwRow.getNext()) != null) {
            // get the current ray index and get (or set) the column ray element
            // sparse matrix vector

            long srcid = brwRow.getCurrentSourceId();
            if (aBlockRow == aBlockCol)
                smvCol = smvRow;
            else
                smvCol = brwCol.get(srcid);

            // if the column sparse matrix vector does not contain the current ray
            // then their is no contribution

            if (smvCol != null) {
                // found the column sparse matrix vector for the current ray index ...
                // set the debug flag and get the partial variance for this ray index
                // if it does not exist then it is created new

                boolean debug = isDebug(siteId, siteId, srcid);
                pv = pspv.getSet(srcid);

                // if debug then output indices and weights for site A and site B

                int[] indxRow = smvRow.getIndexArray();
                double[] wghtRow = smvRow.getValueArray();
                if (debug) {
                    if (aDebugAA == null) aDebugAA = new DebugResultsPhaseSiteMap();
                    dtr = aDebugAA.getSet(phase, siteId).getSet(srcid);
                    dtr.set(aTaskId, aHostName, aBlockRow, aBlockCol,
                            phase, siteId, srcid);
                }

                // sum all partial variance from entries in smvRow to pv ... get
                // the index and weight arrays and loop over all entries

                for (int i = 0; i < smvRow.size(); ++i) {
                    // get row ith index and weight ... retrieve column index and weight
                    // arrays and loop over all column entries

                    int ii = indxRow[i];
                    double wi = wghtRow[i];
                    int[] indxCol = smvCol.getIndexArray();
                    double[] wghtCol = smvCol.getValueArray();
                    for (int j = 0; j < smvCol.size(); ++j) {
                        // get column jth index and weight ... make sure row index equals or
                        // exceeds column index ... this will always be true for off-diagonal
                        // blocks because the aBlkRayMapWghtsRow will be read as the block
                        // row which is always input >= to the block column map
                        // (aBlkRayMapWghtsCol). For diagonal blocks it may not be true but
                        // the upper triangular contribution is handled for the swap case
                        // by multiplying the contribution by 2.0 when ii != ij (see below).

                        int ij = indxCol[j];
                        double wj = wghtCol[j];
                        if (ii >= ij) {
                            // set covariance to zero and calculate weight pair ... see if
                            // this is a non-represented block

                            double cij = 0.0;
                            double u = wi * wj;
                            ++pvcnt;
                            if (aBlockRow == aMtrxBlkDefn.blocks()) {
                                // this is a non-represented block ... get the non-represented
                                // variance if this is a diagonal element ... otherwise the
                                // covariance remains zero (no off-diagonal non-represented
                                // covariance).

                                if (ii == ij) {
                                    int inr = ii - aMtrxBlkDefn.size();
                                    //TODO PS: replace the line below with
                                    //TODO PS: 
                                    //TODO PS:    cij = aNonRepresentedVar[inr];
                                    cij = nrVarArray[inr];
                                    pv[2] += u * cij;
                                }
                            } else {
                                // this is a represented block ... get the represented covariance

                                cij = aBlock[ii - rowStrt][ij - colStrt];
                                if (ii == ij)
                                    pv[0] += u * cij;
                                else
                                    pv[1] += 2.0 * u * cij;
                            }

                            // see if a valid value was added (cij was set)

                            if (cij != 0.0) {
                                // increment count

                                ++pvcnt;

                                // save debug results if debug is true

                                if (debug) {
                                    DebugTaskResultsEntry dtre = new DebugTaskResultsEntry();
                                    dtre.set(i, j, ii, ij, wi, wj, ii - rowStrt, ij - colStrt, cij,
                                            pvcnt);
                                    dtr.add(dtre);
                                }
                            }
                        } // end if (ii >= ij)
                    } // end for (int j = 0; j < indxCol.length; ++j)
                } // end for (int i = 0; i < indx.length; ++i)

            } // end if (smvCol != null)
        } // end while ((smvRow = aBlkRayMapWghtsRow.getNext()) != null)
        if (aBlock != null) aMB.releaseLock(true);

        aProcessTime += (new Date()).getTime() - startTime;
    }

    //***************** Site A -> Site B read and process code ******************

    /**
     * Accumulates site A -> site B partial variances into aSolutionAB using
     * ray weight set files read for this block (aBlockRow and aBlockCol).
     *
     * @throws IOException
     */
    private void solveSiteRaySetAB(RayUncertaintyTaskResult out) throws IOException {
        FinalVariancesSourceMap pspv;
        RayWeightsSourceMap psrwRow, psrwCol;
        RayWeightsPhaseSiteMap blkRayWghtsRow, blkRayWghtsCol;

        // output process header

        if (aOutputTaskInfo) {
            System.out.println("  Processing AB Task: " + aTaskId + ", Block " +
                    aBlockRow + "," + aBlockCol);
            System.out.println("    Total A Sites = " + aSitePairs.size());
        }

        // read in the phase site map of ray weights for the block row index and
        // sum the ray and element counts to the totals read

        blkRayWghtsRow = new RayWeightsPhaseSiteMap();
        readBlockSiteRayElementFile(-1, aBlockRow, blkRayWghtsRow);
        sumRayWeightCount(blkRayWghtsRow);

        // assign the column phase site map ray weights from the row if the block
        // row and column are the same ... otherwise read them in for the block
        // column

        if (aBlockRow == aBlockCol)
            blkRayWghtsCol = blkRayWghtsRow;
        else {
            // read in the phase site map of ray weights for the block column index
            // and sum the ray and element counts to the totals read

            blkRayWghtsCol = new RayWeightsPhaseSiteMap();
            readBlockSiteRayElementFile(-1, aBlockCol, blkRayWghtsCol);
            sumRayWeightCount(blkRayWghtsCol);
        }

        // get unique phase list and loop over all site pairs

        SeismicPhase[] phases = blkRayWghtsRow.getPhases();
        for (Map.Entry<Long, HashSet<Long>> e : aSitePairs.entrySet()) {
            // get site A and B id's and loop over all unique phases

            long siteAid = e.getKey();
            for (Long lng : e.getValue()) {
                long siteBid = lng;
                for (int k = 0; k < phases.length; ++k) {
                    // get next phase and see if the phase / site A id exists in the block
                    // ray weights row container (if it exists but has no rays ignore also)

                    SeismicPhase ph = phases[k];
                    psrwRow = blkRayWghtsRow.get(ph, siteAid);
                    if ((psrwRow != null) && (psrwRow.getTotalRayCount() > 0)) {
                        // we have rays in the row container. Now see if the column container
                        // exists and has rays from site B

                        psrwCol = blkRayWghtsCol.get(ph, siteBid);
                        if ((psrwCol != null) && (psrwCol.getTotalRayCount() > 0)) {
                            // both have rays get the partial variances associated with the
                            // phase / site A / site B

                            pspv = aSolutionAB.getSet(ph, siteAid, siteBid);

                            // read in phase specific non-represented variance array if it is
                            // required and not yet loaded

                            // TODO PS: Replace the second line below with
                            // TODO PS:
                            // TODO PS: (aNonRepresentedVar == null)) readNRVariance();
                            if ((aBlockRow == aMtrxBlkDefn.blocks()) &&
                                    (aNonRepresentedVarMap.get(ph) == null)) readNRVariance(ph);

                            // build the uncertainty for this phase / site A / site B

                            buildRayUncertaintyAB(pspv, psrwRow, psrwCol, out);
                        }
                    }

                    // now perform the same operation on the transpose if this is not a
                    // diagonal block

                    if (aBlockRow != aBlockCol) {
                        // not diagonal ... see if the phase / site B id exists in the block
                        // ray weights row container (if it exists but has no rays ignore
                        // also)

                        psrwRow = blkRayWghtsRow.get(ph, siteBid);
                        if ((psrwRow != null) && (psrwRow.getTotalRayCount() > 0)) {
                            // we have rays in the row container. Now see if the column
                            // container exists and has rays from site A

                            psrwCol = blkRayWghtsCol.get(ph, siteAid);
                            if ((psrwCol != null) && (psrwCol.getTotalRayCount() > 0)) {
                                // both have rays get the partial variances associated with the
                                // phase / site A / site B

                                pspv = aSolutionAB.getSet(ph, siteAid, siteBid);

                                // read in phase specific non-represented variance array if it is
                                // required and not yet loaded

                                // TODO PS: Replace the second line below with
                                // TODO PS:
                                // TODO PS: (aNonRepresentedVar == null)) readNRVariance();
                                if ((aBlockRow == aMtrxBlkDefn.blocks()) &&
                                        (aNonRepresentedVarMap.get(ph) == null)) readNRVariance(ph);

                                // build the uncertainty for phase / site A / site B from the
                                // transpose (transpose row and column)

                                buildRayUncertaintyAB(pspv, psrwCol, psrwRow, out);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Performs the type AB partial variance sum for all rays whose ray weights
     * are contained in the block row and column ray weight source maps input
     * into this function. The results are summed to the partial variance source
     * map (pspv) also input into this function. These source maps are defined
     * for a specific phase/site pair a partial variance is only summed for a
     * specific source if it is defined in both of the input ray weight source
     * maps.
     *
     * @param pspv A specific phase / site A / site B partial variance source map
     *             that will contain the results of the type AB ray uncertainty
     *             calculation on exit.
     *             blkRow A specific phase / site ray weights source map defined for
     *             the block row (aBlockRow) whose entries will be combined with
     *             the block column ray weights source map (blkCol) and the
     *             the input block covariance matrix (aBlock), if this is not
     *             a non-represented block, to form ray uncertainty components
     *             in the output partial variance source map (pspv).
     *             blkCol A specific phase / site ray weights source map defined for
     *             the block column (aBlockCol) whose entries will be combined
     *             with the block row ray weights source map (blkCol) and the
     *             the input block covariance matrix (aBlock), if this is not
     *             a non-represented block, to form ray uncertainty components
     *             in the output partial variance source map (pspv).
     * @throws IOException
     */
    private void buildRayUncertaintyAB(FinalVariancesSourceMap pspv,
                                       RayWeightsSourceMap brwRow,
                                       RayWeightsSourceMap brwCol,
                                       RayUncertaintyTaskResult out)
            throws IOException {
        DebugTaskResults dtr = null;
        double[] pv;
        int pvcnt = 0;
        SparseMatrixVector smvRow, smvCol;

        // read in requested matrix block if this is not a non-represented block and
        // it has not yet been loaded

        //if ((aBlock == null) && (aBlockRow < aMtrxBlkDefn.blocks()))
        if (aBlockRow < aMtrxBlkDefn.blocks()) {
            if (!aMB.isBlockLoaded()) loadBlock();
            aBlock = aMB.getLockedBlock(true);
        } else
            aBlock = null;

        // set the process start time and get the phase, site A id, and site B id

        long startTime = (new Date()).getTime();
        SeismicPhase ph = brwRow.getPhase();
        long siteAID = brwRow.getSiteId();
        long siteBID = brwCol.getSiteId();

        // TODO PS: Remove the two comment lines and the 3 code lines below. No
        // TODO PS: longer required
        // if this is a non-represented block load the non-represented variance
        // array

        double[] nrVarArray = null;
        if (aBlockRow == aMtrxBlkDefn.blocks())
            nrVarArray = aNonRepresentedVarMap.get(pspv.getPhase());

        // get the block size and the row and column start indices of the
        // block defined for this task

        int blksze = aMtrxBlkDefn.blockSize();
        int colStrt = aBlockCol * blksze;
        int rowStrt = aBlockRow * blksze;

        // loop over all entries in the input ray map and build partial variance
        // for rays from the contained ray weight set.

        brwRow.resetIterator();
        while ((smvRow = brwRow.getNext()) != null) {
            // get the current source id and retrieve the ray weights from the column
            // ray weight file.

            long srcid = brwRow.getCurrentSourceId();
            smvCol = brwCol.get(srcid);

            // if the column sparse matrix vector does not contain the current ray
            // then their is no contribution

            if (smvCol != null) {
                // set the debug flag

                boolean debug = isDebug(siteAID, siteBID, srcid);

                // get the partial variance for the current source id

                pv = pspv.getSet(srcid);

                // get the row and column index and weight arrays for the current ray

                int[] indxA = smvRow.getIndexArray();
                double[] wghtA = smvRow.getValueArray();
                int[] indxB = smvCol.getIndexArray();
                double[] wghtB = smvCol.getValueArray();

                // if debug then output indices and weights for site A and site B

                if (debug) {
                    if (aDebugAB == null) aDebugAB = new DebugResultsPhaseSiteSiteMap();
                    dtr = aDebugAB.getSet(ph, siteAID, siteBID).getSet(srcid);
                    dtr.set(aTaskId, aHostName, aBlockRow, aBlockCol,
                            ph, siteAID, siteBID, srcid);
                }

                // loop over all weight entries of the row block

                for (int i = 0; i < smvRow.size(); ++i) {
                    // loop over all weight entries in the column block

                    for (int j = 0; j < smvCol.size(); ++j) {
                        // get jth index and weight. If the jth index is larger than
                        // the ith index then swap indices and weights (we want the
                        // largest index first since the blocks are lower triangular
                        // ... i.e. row indices are bigger than column indices). The only
                        // time that ij > ii can occur is when this is a diagonal block
                        // (aBlockRow == aBlockCol). In that case the indices and weights
                        // are swapped (transposed) so that symmetric lower triangular
                        // covariance block can be used.

                        int ii = indxA[i];
                        double wi = wghtA[i];
                        int ij = indxB[j];
                        double wj = wghtB[j];
                        if (ij > ii) {
                            int itmp = ij;
                            ij = ii;
                            ii = itmp;
                            double wtmp = wj;
                            wj = wi;
                            wi = wtmp;
                        }

                        // set covariance to zero and calculate weight pair ... see if
                        // this is a non-represented block

                        double cij = 0.0;
                        double u = wi * wj;

                        if (aBlockRow == aMtrxBlkDefn.blocks()) {
                            // this is a non-represented block ... get the non-represented
                            // variance if this is a diagonal element ... otherwise the
                            // covariance remains zero (no off-diagonal non-represented
                            // covariance).

                            if (ii == ij) {
                                int inr = ii - aMtrxBlkDefn.size();
                                //TODO PS: replace the line below with
                                //TODO PS: 
                                //TODO PS:    cij = aNonRepresentedVar[inr];
                                cij = nrVarArray[inr];
                                pv[2] += u * cij;
                            }
                        } else {
                            // this is a represented block ... get the represented covariance

                            //cij = aBlock[ii - rowStrt][ij - colStrt];
                            cij = aMB.getBlockElement(ii - rowStrt, ij - colStrt);
                            if (ii == ij)
                                pv[0] += u * cij;
                            else
                                pv[1] += u * cij;
                        }

                        // see if a valid value was added (cij was set)

                        if (cij != 0.0) {
                            // increment count

                            ++pvcnt;

                            // output debug results if debug is true

                            if (debug) {
                                DebugTaskResultsEntry dtre = new DebugTaskResultsEntry();
                                dtre.set(i, j, ii, ij, wi, wj, ii - rowStrt, ij - colStrt, cij,
                                        pvcnt);
                                dtr.add(dtre);
                            }
                        }
                    } // end for (int j = 0; j < indxB.length; ++j)
                } //  end for (int i = 0; i < indxA.length; ++i)
            } // end if (smvCol != null)
        } // while ((smvRow = aBlkRayMapWghtsRow.getNext()) != null)
        if (aBlock != null) aMB.releaseLock(true);

        aProcessTime += (new Date()).getTime() - startTime;
    }

    //***************** Other process code **************************************

    /**
     * Sums the number of rays and elements loaded into the input
     * RayWeightsPhaseSiteMap.
     *
     * @param psmpsrw The input ray weights map whose ray and element count will
     *                be added into the global variables.
     */
    private void sumRayWeightCount(RayWeightsPhaseSiteMap psmpsrw) {
        // get the list of RayWeightsSourceMap object and loop over each

        ArrayList<RayWeightsSourceMap> psrwList = psmpsrw.getList();
        for (int i = 0; i < psrwList.size(); ++i) {
            // get the next entry and sum the ray count and ray element count

            RayWeightsSourceMap psrw = psrwList.get(i);
            aRaysLoadedCount += psrw.getTotalRayCount();
            aRayElementsLoadedCount += psrw.getTotalRayWeightCount();
        }
    }

    /**
     * Reads a site A/ray weight file which will be subsequently processed by the
     * function buildRayUncertaintyAA(long siteID).
     *
     * @param siteID The site A id.
     * @param blkRow The block row of the file to be read.
     * @param rw     The ray map of all source (id) specific SparseMatrixVectors
     *               defined for this site and this block.
     * @throws IOException
     */
    private void readBlockSiteRayElementFile(long setID, int blkRow,
                                             RayWeightsPhaseSiteMap psmbrw)
            throws IOException {
        // create the ReadWriteCatch object that will allow for rereads if
        // file timeout errors occur.

        ReadBlockRowRayWeights rbrsre = new ReadBlockRowRayWeights(psmbrw);

        // build the block map file path/name

        String fp = aBlockRowMapPath + File.separator + "rayweights_Set_" +
                setID + "_Blk_" + blkRow;

        // write the current block map file

        long strtWriteTime = (new Date()).getTime();
        aStateMessg = "Reading: " + fp;
        aReadWriteFail += aReadWriteException.read(fp, rbrsre);
        aStateMessg = "Processing";
        aBlockRowMapReadTime += (new Date()).getTime() - strtWriteTime;
        ++aBlockRowMapReadCount;
    }

    /**
     * Synchronized method to read the non-represented variance data if it has
     * not yet been loaded.
     *
     * @throws IOException
     */
    // TODO PS: Remove the input SeismicPhase argument. No longer required.
    private void readNRVariance(SeismicPhase phase) throws IOException {
        // see if it has been loaded

    	// TODO PS: Replace the if statement line with
    	// TODO PS:
    	// TODO PS: if (aNonRepresentedVar == null) {
    	// TODO PS: if aNonRepresentedVar is static then synchronize and block
    	// TODO PS: here because more than one thread will see the array as
    	// TODO PS: null and subsequently try to fill the array simultaneously.
        if (aNonRepresentedVarMap.get(phase) == null) {
            // convert files to linux if necessary

            aNRVariancePath = PropertiesPlus.convertWinFilePathToLinux(aNRVariancePath);
            aNRVarianceFileName = PropertiesPlus.convertWinFilePathToLinux(aNRVarianceFileName);

            // read and assign non-represented variance data do this in a while
            // loop that keeps trying several times if it fails. This can happen
            // because many processing nodes may be trying to read the file
            // simultaneously

            //TODO PS: delete the phase name from the string build
            String fp = aNRVariancePath + File.separator + aNRVarianceFileName +
                    phase.name();

            long strtReadTime = (new Date()).getTime();
            aStateMessg = "Reading: " + fp;
            ReadWriteCatch rwc = new ReadNRVariance(phase);
            aReadWriteFail += aReadWriteException.read(fp, rwc);
            aStateMessg = "Processing";
            aNRVarReadTime = (new Date()).getTime() - strtReadTime;
            ++aNRVarReadCount;
        }
    }

    /**
     * Function to convert all path information in the server object (aStaticServers).
     */
    private void convertFilePaths() {
        aBlockRowMapPath = PropertiesPlus.convertWinFilePathToLinux(aBlockRowMapPath);

        // convert process paths

        ArrayList<String> fpths = aBlockFileServers.getServerPaths();
        for (int i = 0; i < fpths.size(); ++i)
            fpths.set(i, PropertiesPlus.convertWinFilePathToLinux(fpths.get(i)));

        // convert process secondary paths

        HashMap<String, String> fspths = aBlockFileServers.getSecondaryFilePaths();
        Set<String> keys = fspths.keySet();
        for (String tt : keys) {
            String f = fspths.get(tt);
            f = PropertiesPlus.convertWinFilePathToLinux(f);
            fspths.put(tt, f);
        }
    }

    /**
     * Outputs task information as an event that can be picked up by task
     * listeners. The task id, host name and time are always output. The
     * optional tag and message are added to the string to identify the
     * pertinent information. The information is only sent if aOutputTaskInfo
     * is true.
     *
     * @param hostname Hostname of the machine executing the task.
     * @param tag      Information tag (e.g. "Entry" or "Exit").
     * @param msg      Pertinent information to be conveyed by the message.
     */
    private void outputTaskInfo(String hostname, String tag, String msg) {
        if (aOutputTaskInfo) {
            // output a finalization string and send it across to the parent
            // application to notify of task completion

            String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            String s = "Task (id = " + getId() + ") " + tag +
                    ", Host Name: " + hostname +
                    ", Time: " + formatter.format(cal.getTime()) + NL;
            if (!msg.equals("")) s += msg + NL;
            System.out.println(s);
            //fireNotification(new JPPFTaskEvent(s));
        }
    }

    /**
     * Formats a string containing the input exception. If the error
     * was thrown as an attempt to read write that failed then the
     * input error count (ecnt) will be set to a number larger than
     * zero indicating the attempt trial. If the error was actually
     * thrown and the task terminated the exception count will be
     * zero.
     *
     * @param ecnt The read/write exception count.
     * @param ex   The exception.
     * @param pth  The IO path (empty if not an IO error).
     * @return The formated exception string.
     */
    private String exceptionString(int ecnt, Exception ex, String pth) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));

        // build io path if input (not empty)

        String iopth = "";
        if (!pth.equals("")) {
            iopth = "  IO Path: " + pth + NL;
        }

        // build error string and return

        String s = "BuildBlockMapTask::catch (Exception ex) " + NL +
                "  Host local time: " + Globals.getTimeStamp() + NL +
                "  Exception Count: " + ecnt + NL +
                "  Host: " + aHostName + NL +
                "  Task ID: " + aTaskId + NL +
                "  State: " + aStateMessg + NL +
                "  BlockRowMapPath: " + aBlockRowMapPath + NL +
                iopth +
                "  Block Row/Col: " + aBlockRow + "/" + aBlockCol + NL +
                "  Block Definition: " + NL + aMtrxBlkDefn.showCurrentDefinition("  ") + NL +
                "  Exception: " + NL +
                ex.toString() + NL + sw.toString() + NL;
        return s;
    }

    /**
     * Debug function that returns true if the input site A id, site B id,
     * and source id are defined as a debug ray
     *
     * @param siteAid The site A id.
     * @param siteBid The site B id.
     * @param srcid   The source id.
     * @return True if the inputs define a debug ray ... else false is returned.
     */
    private boolean isDebug(long siteAid, long siteBid,
                            long srcid) {
        //return false if debug rays are not defined

        if (aDebugRays != null) {
            // get site A map and see if srcid was defined

            HashMap<Long, HashSet<Long>> siteAMap = aDebugRays.get(srcid);
            if (siteAMap != null) {
                // srcid was defined as a debug ray ... see if site A and B ids
                // match ... return true if they do

                HashSet<Long> siteBSet = siteAMap.get(siteAid);
                if (siteBSet != null)
                    if (siteBSet.contains(siteBid)) return true;
            }
        }
        return false;
    }

    /**
     * Turns on debug output information for the specified site A and B, and
     * source ids. The output is written to files at path debugPath.
     *
     * @param siteAid The debug site A id.
     * @param siteBid The debug site B id.
     * @param srcid   The debug source id.
     */
    public void setDebug(HashMap<Long, HashMap<Long, HashSet<Long>>> dbgRays) {
        aDebugRays = dbgRays;
    }
}
