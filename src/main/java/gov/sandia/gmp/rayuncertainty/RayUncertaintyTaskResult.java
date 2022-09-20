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

import gov.sandia.gmp.parallelutils.ParallelResult;
import gov.sandia.gmp.rayuncertainty.containers.FinalVariancesPhaseSiteMap;
import gov.sandia.gmp.rayuncertainty.containers.FinalVariancesPhaseSiteSiteMap;
import gov.sandia.gmp.rayuncertainty.debugray.DebugResultsPhaseSiteMap;
import gov.sandia.gmp.rayuncertainty.debugray.DebugResultsPhaseSiteSiteMap;
import gov.sandia.gmp.util.profiler.ProfilerContent;

/**
 * The ray uncertainty task result that carries performance information
 * (read times and counts) and either the site A -> site A partial variance
 * map for all rays processed, or the site A -> site B partial variance map
 * for all rays processed.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class RayUncertaintyTaskResult extends ParallelResult {
    /**
     * Task id.
     */
    private int aTaskId = -1;

    /**
     * Covariance matrix block row index.
     */
    private int aBlockRow = -1;

    /**
     * Covariance matrix block column index.
     */
    private int aBlockCol = -1;

    /**
     * Total task time.
     */
    private long aTotalTime = -1;

    /**
     * Total partial variance accumulation process time.
     */
    private long aProcessTime = -1;

    /**
     * Time spent in the garbage collector.
     */
    private long aGCProcTime = -1;

    /**
     * Total site/ray file read time.
     */
    private long aBlockRowMapReadTime = -1;

    /**
     * Total covariance matrix block read time.
     */
    private long aCovBlockReadTime = -1;

    /**
     * Total non-represented covariance matrix read time.
     */
    private long aNRVarReadTime = -1;

    /**
     * The number of phase specific non-represented covariance matrix files read.
     */
    private int aNRVarReadCount = 0;

    /**
     * Total number of site/ray files read.
     */
    private int aBlockRowMapReadCount = 0;

    /**
     * Total number rays loaded from all site/ray files read.
     */
    private long aRaysLoadedCount = 0;

    /**
     * Total number ray elements loaded from all site/ray files read.
     */
    private long aRayElementsLoadedCount = 0;

    /**
     * The solution set of all phase/site AA partial variances solved by
     * a RayUncertaintyTask.
     */
    private FinalVariancesPhaseSiteMap aSolutionAA = null;

    /**
     * The solution set of all phase/site/site AB partial variances solved by
     * a RayUncertaintyTask.
     */
    private FinalVariancesPhaseSiteSiteMap aSolutionAB = null;

    /**
     * The set of all phase/site AA debug results requested by
     * a RayUncertaintyTask. Usually null unless debug requests are made.
     */
    private DebugResultsPhaseSiteMap aDebugAA = null;

    /**
     * The set of all phase/site/site AB debug results requested by
     * a RayUncertaintyTask. Usually null unless debug requests are made.
     */
    private DebugResultsPhaseSiteSiteMap aDebugAB = null;

    /**
     * Saves and returns any read/write fail errors. If the read/write attempts
     * exceed aReadWriteFailLimit then a real error is thrown and returned.
     */
    private String aReadWriteFail = "";

    /**
     * Used to retrieve profiler information if it was turned on in
     * the task.
     */
    private ProfilerContent aProfilerContent = null;

    /**
     * Standard constructor.
     *
     * @param taskId The task id.
     * @param blkRow The covariance matrix block row index.
     * @param blkCol The covariance matrix block column index.
     */
    public RayUncertaintyTaskResult(int taskId, int blkRow, int blkCol) {
        aTaskId = taskId;
        aBlockRow = blkRow;
        aBlockCol = blkCol;
    }

    /**
     * Returns the task id.
     *
     * @return The task id.
     */
    public int getTaskId() {
        return aTaskId;
    }

    /**
     * Returns the covariance matrix block row index.
     *
     * @return The covariance matrix block row index.
     */
    public int getBlockRow() {
        return aBlockRow;
    }

    /**
     * Returns the covariance matrix block column index.
     *
     * @return The covariance matrix block column index.
     */
    public int getBlockColumn() {
        return aBlockCol;
    }

    /**
     * Sets the solution set of all phase / site pair partial variances solved
     * for by the owning RayUncertaintyTask object.
     *
     * @param pvmapAA The phase/site partial variances for type AA rays.
     * @param pvmapAB The phase/site/site partial variances for type AB rays.
     */
    public void setVarianceMaps(FinalVariancesPhaseSiteMap pvmapAA,
                                FinalVariancesPhaseSiteSiteMap pvmapAB) {
        aSolutionAA = pvmapAA;
        aSolutionAB = pvmapAB;
    }

    /**
     * Sets the solution set of all phase / site debug results solved
     * for by the owning RayUncertaintyTask object. These are usually null unless
     * debugging results have been explicitly requested in the RayUncertainty
     * properties file.
     *
     * @param drmapAA The phase/site debug results for requested type AA rays.
     * @param drmapAB The phase/site/site debug results for requested type AB
     *                rays.
     */
    public void setDebugResultsMaps(DebugResultsPhaseSiteMap drmapAA,
                                    DebugResultsPhaseSiteSiteMap drmapAB) {
        aDebugAA = drmapAA;
        aDebugAB = drmapAB;
    }

    /**
     * Returns the set of all phase/site specific AA partial variances solved
     * for by the owning RayUncertaintyTask.
     *
     * @return The set of all phase/site specific AA partial variances solved
     * for by the owning RayUncertaintyTask.
     */
    public FinalVariancesPhaseSiteMap getVarianceMapAA() {
        return aSolutionAA;
    }

    /**
     * Returns the set of all phase/site/site specific AB partial variances solved
     * for by the owning RayUncertaintyTask.
     *
     * @return The set of all phase/site/site specific AB partial variances solved
     * for by the owning RayUncertaintyTask.
     */
    public FinalVariancesPhaseSiteSiteMap getVarianceMapAB() {
        return aSolutionAB;
    }

    /**
     * Returns the set of all phase/site specific AA debug results solved
     * for by the owning RayUncertaintyTask. The returned result is usually null
     * unless debugging results were explicitly requested in the RayUncertainty
     * properties file.
     *
     * @return The set of all phase/site specific AA debug results requested
     * by the owning RayUncertaintyTask.
     */
    public DebugResultsPhaseSiteMap getDebugResultsMapAA() {
        return aDebugAA;
    }

    /**
     * Returns the set of all phase/site/site specific AB debug results solved
     * for by the owning RayUncertaintyTask. The returned result is usually null
     * unless debugging results were explicitly requested in the RayUncertainty
     * properties file.
     *
     * @return The set of all phase/site/site specific AB debug results requested
     * by the owning RayUncertaintyTask.
     */
    public DebugResultsPhaseSiteSiteMap getDebugResultsMapAB() {
        return aDebugAB;
    }

    /**
     * Sets read and process counts.
     *
     * @param blockMapReadCount      The total number of site/ray files read.
     * @param raysLoadedCount        The total number of rays loaded from all site/ray
     *                               files read.
     * @param rayElementsLoadedCount The total number of ray elements loaded from
     *                               all site/ray files read.
     * @param nrVarReadCount         The number of phase specific non-represented
     *                               variance files read.
     */
    public void setCounts(int blockMapReadCount, long raysLoadedCount,
                          long rayElementsLoadedCount, int nrVarReadCount) {
        aBlockRowMapReadCount = blockMapReadCount;
        aRaysLoadedCount = raysLoadedCount;
        aRayElementsLoadedCount = rayElementsLoadedCount;
        aNRVarReadCount = nrVarReadCount;
    }

    /**
     * Returns the total number of site/ray files read.
     *
     * @return The total number of site/ray files read.
     */
    public int getBlockMapReadCount() {
        return aBlockRowMapReadCount;
    }

    /**
     * Returns the number of phase specific non-represented variance files read.
     *
     * @return The number of phase specific non-represented variance files read.
     */
    public int getNRVarianceFileReadCount() {
        return aNRVarReadCount;
    }

    /**
     * Returns the total number of rays loaded from all site/ray
     * file reads.
     *
     * @return The total number of rays loaded from all site/ray
     * file reads.
     */
    public long getRaysLoadedCount() {
        return aRaysLoadedCount;
    }

    /**
     * Returns the total number of ray elements loaded from all site/ray
     * file reads.
     *
     * @return The total number of ray elements loaded from all site/ray
     * file reads.
     */
    public long getRayElementsLoadedCount() {
        return aRayElementsLoadedCount;
    }

    /**
     * Sets all timing information.
     *
     * @param readBlockMapTime The total Block Map file read time.
     * @param readCovBlkTime   The total covariance matrix block read time.
     * @param readNRVarTime    The total non-represented variance array read time.
     * @param processTime      The total process time (partial variance accumulation).
     * @param gcProcTime       The time spent in the garbage collector
     */
    public void setTimes(long readBlockMapTime, long readCovBlkTime,
                         long readNRVarTime, long processTime, long gcProcTime) {
        aBlockRowMapReadTime = readBlockMapTime;
        aCovBlockReadTime = readCovBlkTime;
        aNRVarReadTime = readNRVarTime;
        aProcessTime = processTime;
        aGCProcTime = gcProcTime;
    }

    /**
     * Returns the total covariance matrix block read time.
     *
     * @return The total covariance matrix block read time.
     */
    public long getCovBlockReadTime() {
        return aCovBlockReadTime;
    }

    /**
     * Returns the total non-represented variance array read time.
     *
     * @return The total non-represented variance array read time.
     */
    public long getNRVarianceReadTime() {
        return aNRVarReadTime;
    }

    /**
     * Returns the total site/ray file read time.
     *
     * @return The total site/ray file read time.
     */
    public long getBlockMapReadTime() {
        return aBlockRowMapReadTime;
    }

    /**
     * Returns the total process time.
     *
     * @return The total process time.
     */
    public long getProcessTime() {
        return aProcessTime;
    }

    /**
     * Returns the time spent in the garbage collector.
     *
     * @return The time spent in the garbage collector.
     */
    public long getGCProcessTime() {
        return aGCProcTime;
    }

    /**
     * Sets the total task time.
     *
     * @param totalTime The total task time.
     */
    public void setTotalTime(long totalTime) {
        aTotalTime = totalTime;
    }

    /**
     * Returns the total task time.
     *
     * @return The total task time.
     */
    public long getTotalTime() {
        return aTotalTime;
    }

    /**
     * Returns the process overhead time ... The total time minus the process
     * time, the block read time, all site/ray file read time, and all non-
     * represented file read time.
     *
     * @return The process overhead time ... The total time minus the process
     * time, the block read time, all site/ray file read time, and all
     * non-represented file read time.
     */
    public long getOverheadTime() {
        return aTotalTime - aProcessTime - aCovBlockReadTime -
                aBlockRowMapReadTime - aNRVarReadTime - aGCProcTime;
    }

    /**
     * Sets the read/write fail errors string to rwFail.
     *
     * @param rwFail The read/write fail errors string.
     */
    public void setReadWriteFailErrors(String rwFail) {
        aReadWriteFail = rwFail;
    }

    /**
     * Returns the read/write fail errors string.
     *
     * @return The read/write fail errors string.
     */
    public String getReadWriteFailErrors() {
        return aReadWriteFail;
    }

    /**
     * Set profiler content.
     *
     * @param pc The profiler content to set.
     */
    public void setProfilerContent(ProfilerContent pc) {
        aProfilerContent = pc;
    }

    /**
     * Returns the profiler content.
     *
     * @return The profiler content.
     */
    public ProfilerContent getProfilerContent() {
        return aProfilerContent;
    }
}
