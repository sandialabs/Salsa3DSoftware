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
package gov.sandia.gmp.rayuncertainty.containers;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;

/**
 * An ObservationsPhaseSiteMap container that is used to subdivide prediction
 * results into a series of sets so as not to overload RayUncertaintyTask
 * objects during the ray uncertainty calculation. This object defines the set
 * as locked once the number of ray weight elements added to this set exceeds
 * the static parameter aElementLimit. At that point the set allows no new
 * phase / site pairs to be added to this set; instead they are added to a new
 * set created by RayWeightSetList. This set can still add rays, once it becomes
 * locked however, but the rays must belong to one of the phase / site pairs
 * already contained by this set. Once all rays have been added to the set for
 * those defined phase / site pairs the ray weights are written to disk for
 * each matrix block row.
 * <p>
 * This object contains ObservationsSourceMap objects that are pulled directly
 * from the static ObservationsPhaseSiteMap aPrimeRayWghtMap. This object was
 * set by RayUncertainty before the ray uncertainty calculation is begun.
 * Effectively, the RayWeightSetList object, owned by the RayUncertainty object,
 * subdivides aPrimeRayWghtMap into several RayWeightSet objects that contain
 * a subset of entries from aPrimeRayWghtMap, such that no entry has many more
 * ray weight elements than the other.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class RayWeightSet extends ObservationsPhaseSiteMap {
    /**
     * A runnable designed to write a ray weight file as a separate thread
     * so that the client can continue processing returned results.
     *
     * @author jrhipp
     */
    private class OutputRayWeightFiles implements Runnable {
        /**
         * The runnable thread for this class.
         */
        private Thread runThread = null;

        /**
         * Default constructor. Creates a new thread and starts it.
         */
        public OutputRayWeightFiles() {
            runThread = new Thread(this, "OutputBlockRayWeightFiles");
            runThread.start();
        }

        /**
         * The run method called by the new thread. It simply calls the function
         * write(). This function writes out all information associated with the
         * RayWeightSet as a series of ray weight files for each block row in the
         * covariance matrix. After the function write() completes the thread
         * terminates.
         */
        @Override
        public void run() {
            try {
                write();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * The total number of ray weight block files written to disk.
     */
    private static int aRayWeightBlocksWritten = 0;

    /**
     * The total number of ray weight sets written to disk.
     */
    private static int aRayWeightSetsWritten = 0;

    /**
     * The ray weight element limit which when exceeded locks this RayWeightSet
     * from adding any more phase / site entries.
     */
    private static long aElementLimit = 0;

    /**
     * The path to where ray weight block files are written.
     */
    private static String aRayWeightPath = "";

    /**
     * The primary ObservationsPhaseSiteMap maintained by RayUncertainty from
     * which entries are pulled to be added to this RayWeightSet and others that
     * are created by the RayWeightSetList object.
     */
    private static ObservationsPhaseSiteMap
            aPrimeRayWghtMap = null;

    /**
     * This RayWeightSet id. These are incremented by the RayWeightSetList object
     * every time it creates a new RayWeightSet.
     */
    private int aRayWghtSetId = 0;

    /**
     * The locked flag, which when set to true, prevents any new phase / site
     * entries from being added to this RayWeightSet.
     */
    boolean aLocked = false;

    /**
     * The total number of phase / site entries added to this RayWeightSet.
     */
    private int aPhaseSiteCount = 0;

    /**
     * The total number of ray weight elements stored by phase / site entries
     * within this RayWeightSet object.
     */
    private long aTotalBlockElementCount = 0;

    /**
     * The maximum number of ray weight elements stored by some arbitrary matrix
     * block row contained by a phase / site entry within this RayWeightSet
     * object.
     */
    private long aMaxBlockElementCount = 0;

    /**
     * The total number of rays added to this RayWeightSet.
     */
    private int aRayAddCount = 0;

    /**
     * The total number of rays that can be added to this RayWeightSet. This
     * number if determined from the ObservationsSourceMap objects associated
     * with each phase / site entry when they are added to this RayWeightSet
     * object. When the total number of rays added (aRayAddCount) equals this
     * number the set is complete and can be written to disk.
     */
    private int aRayAddLimit = 0;

    /**
     * Equal to or less than aRayAddCount defining only valid predicted rays
     * (those with a non-null index array) that were added to this RayWeightSet.
     */
    private int aValidRayAddCount = 0;

    /**
     * Standard constructor.
     *
     * @param id The id for this new RayWeightSet.
     */
    public RayWeightSet(int id) {
        aRayWghtSetId = id;
    }

    /**
     * Static function that sets the static components of this class.
     *
     * @param elemLimit The ray weight element limit above which this RayWeightSet
     *                  becomes locked.
     * @param pth       The path to where all RayWeight block files are written.
     * @param procmap   The primary container of ObservationSourceMap objects
     *                  that are referenced into this RayWeightSet if their
     *                  owning phase / site entry is defined in the RayWeightSet.
     */
    public static void initialize(long elemLimit, String pth,
                                  ObservationsPhaseSiteMap procmap) {
        aElementLimit = elemLimit;
        aRayWeightPath = pth;
        aPrimeRayWghtMap = procmap;
    }

    /**
     * Returns the primary observationsPhaseSiteMap owned by the RayUncertainty
     * class.
     *
     * @return The primary observationsPhaseSiteMap owned by the RayUncertainty
     * class.
     */
    public static ObservationsPhaseSiteMap primaryObservationsPhaseSiteMap() {
        return aPrimeRayWghtMap;
    }

    /**
     * Returns the number of RayWeightSet objects written to disk.
     *
     * @return The number of RayWeightSet objects written to disk.
     */
    public static int getRayWeightSetsWritten() {
        return aRayWeightSetsWritten;
    }

    /**
     * Increments the number of RayWeightSet objects written to disk.
     */
    private static synchronized void incrementRayWeightSetsWritten() {
        ++aRayWeightSetsWritten;
    }

    /**
     * Returns the number of RayWeightSet blocks written to disk.
     *
     * @return The number of RayWeightSet blocks written to disk.
     */
    public static int getRayWeightBlocksWritten() {
        return aRayWeightBlocksWritten;
    }

    /**
     * Increments the number of ray weight blocks written to disk.
     */
    private static synchronized void incrementRayWeightBlocksWritten() {
        ++aRayWeightBlocksWritten;
    }

    /**
     * Returns the valid ray count added to this RayWeightSet.
     *
     * @return The valid ray count added to this RayWeightSet.
     */
    public int getValidRayAddCount() {
        return aValidRayAddCount;
    }

    /**
     * Adds a new ObservationsSourceMap object into this RayWeightSet. This
     * object is already contained in the static set (aPrimeRayWghtMap) created
     * during prediction by RayUncertainty. It is moved into this set if it's
     * associated phase / site was added into this RayWeightSet in the add
     * function below.
     *
     * @param pso The ObservationsSourceMap object to be added.
     */
    public void add(ObservationsSourceMap pso) {
        // exit if this PhaseSiteObservations object is already contained

        if (get(pso.getPhase(), pso.getSiteId()) == pso) return;

        // add object to the map

        add(pso.getPhase(), pso.getSiteId(), pso);

        // increment the phase / site count, the ray add limit, the ray add count,
        // the valid ray count, and the block element count ... update the max
        // block element count also

        ++aPhaseSiteCount;
        aRayAddLimit += pso.size();
        aRayAddCount += pso.getPredictedRayCount();
        aValidRayAddCount += pso.getValidRayCount();
        aTotalBlockElementCount += pso.getTotalBlockElementCount();
        if (pso.getMaxBlockElementCount() > aMaxBlockElementCount)
            aMaxBlockElementCount = pso.getMaxBlockElementCount();

        // lock this RayWeightSet if the element count exceeds the limit and
        // increment the block write reference.

        if (aTotalBlockElementCount > aElementLimit)
            aLocked = true;
        pso.incrementBlockWriteReference();
    }

    /**
     * Attempt to add a new predicted ray to this RayWeightSet. If successful
     * return true. If this ray is a new phase / site entry then use the
     * existing ObservationsSourceMap object from the static aPrimeRayWghtMap as
     * the reference for the phase / site association. Only add it if this
     * RayWeightSet is not locked. If contained or successfully added update the
     * ObservationsSourceMap with the new ray entry.
     *
     * @param phase  The phase of the input ray to be updated.
     * @param siteid The receiver id of the input ray to be updated.
     * @param srcid  The source id of the input ray to be updated.
     * @param indx   The matrix column index array of the ray to be updated.
     * @param wght   The matrix column weight array of the ray to be updated.
     * @return True if this RayWeightSet was updated successfully with this input
     * ray.
     */
    public boolean add(SeismicPhase phase, long siteid, long srcid,
                       int[] indx, double[] wght) {
        // get PhaseSiteObservations and see if it is null

        ObservationsSourceMap pso = get(phase, siteid);
        if (pso == null) {
            // not contained ... return false if this set is locked

            if (aLocked) return false;

            // otherwise set the PhaseSiteObservations from the static map and add
            // it to this set

            pso = aPrimeRayWghtMap.get(phase, siteid);
            add(pso);
        }

        // increment the ray count ... if indx is not null increment valid ray
        // count and block element count

        ++aRayAddCount;
        if (indx != null) {
            ++aValidRayAddCount;
            aTotalBlockElementCount += indx.length;
        }

        // add the weights to the PhaseSiteObservations object and adjust the
        // maximum block element count if necessary

        pso.add(srcid, indx, wght);
        if (pso.getMaxBlockElementCount() > aMaxBlockElementCount)
            aMaxBlockElementCount = pso.getMaxBlockElementCount();

        // if the total block element count exceeds the limit lock this set ...
        // return true to indicate ray was added and exit

        if (aTotalBlockElementCount > aElementLimit)
            aLocked = true;
        return true;
    }

    /**
     * Returns true if all rays have been added for the current set of phase/site
     * entries.
     *
     * @return True if all rays have been added for the current set of phase/site
     * entries.
     */
    public boolean isComplete() {
        return (aRayAddCount == aRayAddLimit);
    }

    /**
     * Returns true if this RayWeightSet is locked.
     *
     * @return True if this RayWeightSet is locked.
     */
    public boolean isLocked() {
        return aLocked;
    }

    /**
     * Create a thread to write ray weights for all block row entries. Here's what
     * happens: This function creates a new OutputRayWeightFiles object and
     * returns. The OutputRayWeightFiles object constructor creates a new thread
     * and runs which calls function write() below. Function write() increments
     * the aBlocksToWrite parameter by the number of blocks and then loops over
     * all blocks writing each to disk by calling function writeBlock(i, fp, 10,
     * 5000). That function allows multiple tries at writing the block to
     * accommodate time out errors when the file system is saturated. Otherwise
     * the previous writeBlock function calls function writeBlock(i, fp) which
     * actually performs the write. The last function loops over all phase/site
     * entries in this set and writes each RayWeightsSourceMap defined for each
     * block. On completion of each block the static number of blocks written to
     * disk is incremented. On completion of all blocks written to disk the
     * static number of RayWeightSets written to disk is incremented.
     * <p>
     * A new thread is instantiated for each RayWeightSet written to disk.
     *
     * @throws IOException
     */
    public void writeSet() throws IOException {
        new OutputRayWeightFiles();
    }

    /**
     * Writes this RayWeightSet to disk. Here's what happens:
     * Function write() increments the aBlocksToWrite parameter by the number of
     * blocks and then loops over all blocks writing each to disk by calling
     * function writeBlock(i, fp, 10, 5000). That function allows multiple tries
     * at writing the block to accommodate time out errors when the file system
     * is saturated. Otherwise the previous writeBlock function calls function
     * writeBlock(i, fp) which actually performs the write. The last function
     * loops over all phase/site entries in this set and writes each
     * RayWeightsSourceMap defined for each block. On completion of each block
     * the static number of blocks written to disk is incremented. On completion
     * of all blocks written to disk the static number of RayWeightSets written
     * to disk is incremented.
     *
     * @throws IOException
     */
    public void write() throws IOException {
        // write all phase/site entries 1 block at a time ... get the number of
        // blocks to be written and increment the blocks-to-write parameter ...
        // loop over all blocks

        int n = ObservationsSourceMap.getBlockCount();
        for (int i = 0; i < n; ++i) {
            // assemble the block path / file name and write the block

            String fp = aRayWeightPath + File.separator + "rayweights_Set_" +
                    aRayWghtSetId + "_Blk_" + i;
            String fail = writeBlock(i, fp, 10, 5000);

            // if any failures occurred write them

            if (!fail.equals("")) {
                String s;
                s = "  Recovered Failures Writing Phase/Site/BlockRow Map ...";
                System.out.println(s + NL +
                        Globals.prependLineHeader(fail, "      ERR ") + NL);
            }

            // decrement the blocks-to-write parameter and continue to the next block

            incrementRayWeightBlocksWritten();
        }

        // increment the number of RayWeightSets written and decrement all
        // ObservationSourceMap references contained in this RayWeightSet

        incrementRayWeightSetsWritten();
        //decrementBlockWriteReferenceCount();
    }

    /**
     * Attempts to write the block file for block id, blkid, at path fpBlk. The
     * write can fail up to writeFailLimit times. The thread sleeps for
     * threadSleep milliseconds between failures if any occurs. If the limit
     * (writeFailLimit) is exceeded a real error is thrown back to the caller.
     *
     * @param blkid          The index of the block to be written.
     * @param fpBlk          The path/file name of the block file to be written.
     * @param writeFailLimit The number of times the write can fail before a
     *                       real exception is thrown.
     * @param threadSleep    The length of time for the thread to sleep between
     *                       failures.
     * @return The intermediate failure exceptions, if any occurred, empty
     * otherwise.
     * @throws IOException
     */
    private String writeBlock(int blkid, String fpBlk,
                              int writeFailLimit, long threadSleep)
            throws IOException {
        // try to write the input file name

        String writeFail = "";
        int ecnt = 0;
        while (true) {
            try {
                // call the write function ... exit if it succeeds.

                writeBlock(blkid, fpBlk);
                break;
            } catch (Exception ex) {
                // unsuccessful ... increment count and try again ... after
                // writeFailLimit tries throw error

                ++ecnt;
                writeFail += catchExceptionString(ecnt, ex, blkid, fpBlk);
                if (ecnt == writeFailLimit) throw new IOException(ex);

                // sleep to give the file system a chance to clear

                try {
                    Thread.sleep(threadSleep);
                } catch (InterruptedException e) {
                }
            }
        }

        // return write fail string

        return writeFail;
    }

    /**
     * Writes all BlockArrayPhaseSite entries in this BlockArraySet to disk for
     * just block index i.
     *
     * @param blkid The block index to be written to disk.
     * @param fp    The path/file name of the block to be written.
     * @throws IOException
     */
    private void writeBlock(int blkid, String fp) throws IOException {
        // Open the file and write out the number of BlockArrayPhaseSite objects to
        // be written to disk

        FileOutputBuffer fob = new FileOutputBuffer(fp);
        fob.writeInt(aPhaseSiteCount);

        // loop over all phase/site entries and write them to disk

        ArrayList<ObservationsSourceMap> psoList;
        psoList = getList();
        for (int k = 0; k < psoList.size(); ++k) {
            ObservationsSourceMap pso = psoList.get(k);
            fob.writeString(pso.getPhase().name());
            fob.writeLong(pso.getSiteId());
            pso.writeBlock(blkid, fob);
        }

        // done ... close file and exit

        fob.close();
    }

    /**
     * Assembles a write fail message where ecnt is the number of failures,
     * ex is the the exception that was thrown trying to write the file, and fp
     * is the path/name of the file that was attempting to write itself to disk.
     *
     * @param ecnt  The current failure count trying to write this file.
     * @param ex    The exception that occurred trying to write this file.
     * @param blkid The block id of the block trying to write itself.
     * @param fp    The path to where the file is being written.
     * @return The formatted exception string.
     */
    private String catchExceptionString(int ecnt, Exception ex,
                                        int blkid, String fp) {
        String s = "writePhaseSiteBlockRowMap::catch (Exception ex) " + NL +
                "  Time:               " + Globals.getTimeStamp() + NL +
                "  Exception Count:    " + ecnt + NL +
                "  Block Array Set ID: " + aRayWghtSetId + NL +
                "  Block Index:        " + blkid + NL +
                "  File Path:          " + fp + NL +
                "  Exception:          " + NL +
                ex.toString() + NL;
        return s;
    }
}
