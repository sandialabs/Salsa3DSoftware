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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.rayuncertainty.basecontainers.Observation;
import gov.sandia.gmp.rayuncertainty.basecontainers.PhaseSiteBase;
import gov.sandia.gmp.rayuncertainty.basecontainers.SourceMap;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.matrixblock.MatrixBlockDefinition;

/**
 * This object is used to store observations associated with a specific source
 * id as a group, that taken together, are associated with a unique phase -->
 * receiver pair. These are created during RayUncertainty initialization from
 * the database, a GeoModel used as a source definition object, or from the
 * input properties file which defines sources and receivers. When created an
 * observation object is created to hold the phase/site/source definition and
 * added to the extended source map (SourceMap<Observation>) from which this
 * class is derived.
 * <p>
 * During prediction an internal array of RayWeightSourceMap objects are
 * created for each covariance block row index and stored in this object. With
 * this definition a ray weight definition (index array and weight array),
 * which is defined for a phase --> receiver id, finds it's way to this
 * ObservationSourceMap. Each entry of the ray weight arrays are parsed and
 * added to the owning RayWeightSourceMap for the block that owns it. In this
 * way a ray is decomposed into it's various covariance block components which
 * are read during the ray uncertainty calculation.
 * <p>
 * Functions to add new entries, read and write block ray weights, and getters
 * to return properties are provided.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class ObservationsSourceMap extends SourceMap<Observation>
        implements Serializable {
    /**
     * The matrix block information object that contains the matrix structure
     * definition.
     */
    private static MatrixBlockDefinition aMtrxBlkDefn = null;

    /**
     * Block row partitioned ray weight maps. Each block row contains a
     * RayWeightsSourceMap that maps observation source id to a
     * SparseMatrixVector containing that rays index and weights for the owning
     * block row. These are populated during prediction and subsequently written
     * to disk.
     */
    private ArrayList<RayWeightsSourceMap>
            aBlockRowRayWeights = null;

    /**
     * The total number of elements contained in all observation ray weight
     * entries.
     */
    private long aTotalElementCount = 0;

    /**
     * The total number of predicted rays (equals the observation count (size())
     * when all predictions have completed.
     */
    private int aPredictedRayCount = 0;

    /**
     * The total number of valid entries (index and weight arrays in the add
     * function were not null).
     */
    private int aValidRayCount = 0;

    /**
     * The number of ray weight elements from the block with the most ray weight
     * elements.
     */
    private long aMaxBlockElementCount = 0;

    /**
     * The number of rays from the block with the most rays.
     */
    private int aMaxBlockRayCount = 0;

    /**
     * The block write reference count (0, 1, or 2). The number of RayWeightSets
     * awaiting to be written to disk that contain this ObservationsSourceMap.
     */
    private int aBlockWriteReference = 0;

    /**
     * Standard constructor.
     */
    public ObservationsSourceMap() {
        super(null, -1);
        initialize();
    }

    /**
     * Standard constructor.
     *
     * @param ph     The input Seismic Phase.
     * @param siteID The input Receiver ID.
     */
    public ObservationsSourceMap(SeismicPhase ph, long siteID) {
        super(ph, siteID);
        initialize();
    }

    /**
     * Standard constructor.
     *
     * @param fib The FileInputBuffer from which this object will be read.
     * @throws IOException
     */
    public ObservationsSourceMap(FileInputBuffer fib) throws IOException {
        super(null, -1);
        initialize();
        read(fib);
    }

    /**
     * Creates and initializes the array list of RayWeightsSourceMap objects for
     * each covariance matrix block row.
     */
    private void initialize() {
        aBlockRowRayWeights = new ArrayList<RayWeightsSourceMap>
                (aMtrxBlkDefn.blocks() + 1);
        for (int i = 0; i < aMtrxBlkDefn.blocks() + 1; ++i)
            aBlockRowRayWeights.add(null);
    }

    /**
     * Sets the static MatrixBlockInfo object and the block ray weight path.
     *
     * @param mbd MatrixBlockInfo object.
     */
    public static void setMatrixDefn(MatrixBlockDefinition mbd) {
        aMtrxBlkDefn = mbd;
    }

    /**
     * Returns the number of block rows (1 extra for the non-represented block).
     *
     * @return The number of block rows (1 extra for the non-represented block).
     */
    public static int getBlockCount() {
        return aMtrxBlkDefn.blocks() + 1;
    }

    /**
     * Returns the valid predicted ray count (The number of rays with a non-null
     * index and weight array after prediction).
     *
     * @return The valid predicted ray count (The number of rays with a non-null
     * index and weight array after prediction).
     */
    public int getValidRayCount() {
        return aValidRayCount;
    }

    /**
     * Returns the number of predicted rays. Incremented each time add(...) is
     * called. Equals size() after all rays have been predicted.
     *
     * @return The number of predicted rays. Incremented each time add(...) is
     * called. Equals size() after all rays have been predicted.
     */
    public int getPredictedRayCount() {
        return aPredictedRayCount;
    }

    /**
     * Returns the number of rays contained by the block entry with the most rays.
     *
     * @return The number of rays contained by the block entry with the most rays.
     */
    public int getMaxBlockRayCount() {
        return aMaxBlockRayCount;
    }

    /**
     * Returns the phase name.
     *
     * @return The phase name.
     */
    public String getPhaseName() {
        return aPhase.name();
    }

    /**
     * Returns the total ray count (Same as source count) processed by this
     * phase/site pair.
     *
     * @return The total ray count (Same as source count) processed by this
     * phase/site pair.
     */
    public int getRayCount() {
        return size();
    }

    /**
     * Adds a new Observation to this PhaseSiteProcessData object. Called during
     * RayUncertainty initialization as new observations are created from the
     * database, a GeoModel, or a properties file input.
     *
     * @param obs The new Observation to be added to this ObservationsSourceMap
     *            object.
     */
    public void addObservation(Observation obs) {
        getSet(obs.getSourceId(), obs);
    }

    /**
     * Returns the ray index of the input Source ID. If the input Source ID is not
     * contained -1 is returned.
     *
     * @param srcID The input Source ID for which the ray index will be returned.
     * @return
     */
    public int getRayIndex(long srcID) {
        Observation obs = get(srcID);
        if (obs == null)
            return -1;
        else
            return obs.getRayIndex();
    }

    /**
     * Returns the Source ID associated with the input ray index.
     *
     * @param rayIndex The ray index for which the associated Source ID will be
     *                 returned.
     * @return The Source ID associated with the input ray index.
     */
    public long getSourceID(int rayIndex) {
        return getList().get(rayIndex).getSourceId();
    }

    /**
     * Returns the Receiver ID associated with the input ray index.
     *
     * @param rayIndex The ray index for which the associated Receiver ID will be
     *                 returned.
     * @return The Receiver ID associated with the input ray index.
     */
    public long getReceiverID(int rayIndex) {
        return getList().get(rayIndex).getReceiverId();
    }

    /**
     * Increments the block write reference (0, 1, or 2). This is the number of
     * RayWeightSet objects that reference this ObservationSourceMap.
     */
    public synchronized void incrementBlockWriteReference() {
        ++aBlockWriteReference;
    }

    /**
     * Decrements the block write reference (0, 1, or 2). This is the number of
     * RayWeightSet objects that reference this ObservationSourceMap.
     */
    public synchronized void decrementBlockWriteReference() {
        --aBlockWriteReference;
    }

    /**
     * Adds the ray indices and weights to their appropriate block
     * RayWeightSourceMap object. This is the primary add function during
     * prediction. It is called by the RayWeightSet to add a new predicted ray.
     *
     * @param srcid The source id of the input ray.
     * @param indx  The ray indices.
     * @param wght  The ray weights.
     */
    public void add(long srcid, int[] indx, double[] wght) {
        // increment the prediction count and see if the indx array is valid

        ++aPredictedRayCount;
        if (indx != null) {
            // valid ray ... increment the valid ray count and the total element
            // count ... loop over all entries (of indx and wght) to place them in
            // their respective block map.

            ++aValidRayCount;
            aTotalElementCount += indx.length;
            for (int i = 0; i < indx.length; ++i) {
                // get the block that hold element i and get that RayWeightsSourceMap

                int blkid = aMtrxBlkDefn.blockRowPlus1(indx[i]);
                RayWeightsSourceMap brw = aBlockRowRayWeights.get(blkid);

                // see if the RayWeightSourceMap is defined

                if (brw == null) {
                    // not defined ... create a new one and set it into the list for the
                    // requested block id

                    brw = new RayWeightsSourceMap(aPhase, aSiteId);
                    aBlockRowRayWeights.set(blkid, brw);
                }

                // add the element into the RayWeightsSourceMap ... increment the
                // maximum block ray count and element count if necessary

                brw.add(srcid, indx[i], wght[i]);
                if (aMaxBlockRayCount < brw.getTotalRayCount())
                    aMaxBlockRayCount = brw.getTotalRayCount();
                if (aMaxBlockElementCount < brw.getTotalRayWeightCount())
                    aMaxBlockElementCount = brw.getTotalRayWeightCount();
            }
        }
    }

    /**
     * Writes a specific block RayWeightsSourceMap to the input file output
     * buffer.
     *
     * @param blkid The block id to be written to disk.
     * @param fob   The file output buffer into which the block will be written.
     * @throws IOException
     */
    public void writeBlock(int blkid, FileOutputBuffer fob) throws IOException {
        // get RayWeightsSourceMap for the block if it exists ... if not write size
        // as 0 and phase and site id

        RayWeightsSourceMap brw = aBlockRowRayWeights.get(blkid);
        if (brw == null) {
            fob.writeInt(0);
            fob.writeString(aPhase.name());
            fob.writeLong(aSiteId);
        } else {
            // valid pointer ... write brw

            brw.write(fob);

            // clear the blocks SourceMap once once it has been written to disk. This
            // will help clear up memory for reuse

            if (aBlockWriteReference == 1) brw.clear();
        }
    }

    /**
     * Reads in a specific blocks RayWeightsSourceMap using the format
     * prescription defined by the writeBlock(...) function above. This function
     * is static and can be used to return a RayWeightsSourceMap assuming the
     * input file buffer is pointing to one that was written previously.
     *
     * @param fib The input file buffer from which the block will be read.
     * @return The RayMap containing the block read.
     * @throws IOException
     */
    public static RayWeightsSourceMap readBlock(FileInputBuffer fib)
            throws IOException {
        RayWeightsSourceMap psrw = new RayWeightsSourceMap();
        psrw.read(fib);
        return psrw;
    }

    /**
     * Returns the total element count summed over all blocks.
     *
     * @return The total element count summed over all blocks.
     */
    public long getTotalBlockElementCount() {
        return aTotalElementCount;
    }

    /**
     * Returns the mean element count averaged over all blocks.
     *
     * @return The mean element count averaged over all blocks.
     */
    public double getMeanBlockElementCount() {
        return (double) aTotalElementCount / aBlockRowRayWeights.size();
    }

    /**
     * Returns the block element count from the block that contains the most
     * elements.
     *
     * @return The block element count from the block that contains the most
     * elements.
     */
    public long getMaxBlockElementCount() {
        return aMaxBlockElementCount;
    }

    /**
     * Writes this ObservationsSourceMap object to the input FileOutputBuffer fob.
     * Note: this function does not output the array of block
     * RayWeightsSourceMap objects.
     *
     * @param fob The FileOutputBuffer into which this ObservationsSourceMap
     *            object will be written.
     * @throws IOException
     */
    @Override
    public void write(FileOutputBuffer fob) throws IOException {
        // output the phase name and site id

        fob.writeString(aPhase.name());
        fob.writeLong(aSiteId);

        // output the number of sources (rays) and each observation

        ArrayList<Observation> obsList = getList();
        fob.writeInt(obsList.size());
        for (int i = 0; i < obsList.size(); ++i)
            obsList.get(i).write(fob);

        // write out summary counts

        fob.writeLong(aTotalElementCount);
        fob.writeLong(aMaxBlockElementCount);
        fob.writeInt(aPredictedRayCount);
        fob.writeInt(aValidRayCount);
        fob.writeInt(aMaxBlockRayCount);
    }

    /**
     * Reads this ObservationsSourceMap object from the input FileInputBuffer fib.
     * Note: this function does not populate the array of block
     * RayWeightsSourceMap objects.
     *
     * @param fib The FileInputBuffer into which this ObservationsSourceMap object
     *            will be written.
     * @throws IOException
     */
    @Override
    public void read(FileInputBuffer fib) throws IOException {
        // read phase name and site id

        aPhase = SeismicPhase.valueOf(fib.readString());
        aSiteId = fib.readLong();

        // read source id list size and create source list and source map ...
        // read in each source and populate list and map

        int n = fib.readInt();
        newMap(n);
        for (int i = 0; i < n; ++i) {
            Observation obs = new Observation(fib);
            getSet(obs.getSourceId(), obs);
        }

        // read in summary counts

        aTotalElementCount = fib.readLong();
        aMaxBlockElementCount = fib.readLong();
        aPredictedRayCount = fib.readInt();
        aValidRayCount = fib.readInt();
        aMaxBlockRayCount = fib.readInt();
    }

    /**
     * Dumps this ObservationsSourceMap object to the input screen writer
     * (scrnWrtr). Each line prepends the header hdr to the line before output.
     *
     * @param hdr      The line header.
     * @param scrnWrtr The output screen writer.
     */
    public void dump(String hdr, String rcvrName, ScreenWriterOutput scrnWrtr) {
        String s;

        // make sure output is on

        if (scrnWrtr.isOutputOn()) {
            // output phase name, site id, ray count and source id header

            s = hdr + "Phase:                         " + aPhase.name() + NL +
                    hdr + "  Site ID:                     " + aSiteId +
                    " (" + rcvrName + ")" + NL +
                    hdr + "    Total Ray (Source) Count:  " + size() + NL +
                    hdr + "    Total Valid Ray Count:     " + aValidRayCount + NL +
                    hdr + "    Max. Block Ray Count:      " + aMaxBlockRayCount + NL +
                    hdr + "    Source ID [Ray Index]:    ";
            scrnWrtr.write(s);

            // loop over all sources and output their id and ray index

            s = "";
            ArrayList<Observation> obsList = getList();
            for (int i = 0; i < obsList.size(); ++i) {
                // after the first entry test for ", " insertion or line print

                if (i > 0) {
                    // print new line and reset header on every 8th entry ... otherwise
                    // add a comma

                    if (i % 8 == 0) {
                        scrnWrtr.write(s + NL);
                        s = hdr + "                              ";
                    } else
                        s += ", ";
                }

                // add ith entry and continue

                s += String.format("%8d [%5d]", obsList.get(i).getSourceId(), i);
            }

            // done ... dump last line

            scrnWrtr.write(s + NL + NL);

            // output total ray element count and block array size, and
            // RayElementCount header

            s = hdr + "    Total Ray Element Count:  " + aTotalElementCount + NL +
                    hdr + "    Max. Block Element Count: " + aMaxBlockElementCount + NL +
                    hdr + "    Total Cov. Block Rows:    " +
                    (aMtrxBlkDefn.blocks() + 1) + NL +
                    hdr + "    [Block] (Rays, Elements): ";
            scrnWrtr.write(s);

            // loop over all blocks and output the # of rays and elements for each
            // block

            s = "";
            for (int i = 0; i < aBlockRowRayWeights.size(); ++i) {
                // get the ith block ray weights and set ray and element counts

                int nrays = 0;
                long nelems = 0;
                RayWeightsSourceMap psrw = aBlockRowRayWeights.get(i);
                if (psrw != null) {
                    nrays = psrw.getTotalRayCount();
                    nelems = psrw.getTotalRayWeightCount();
                }

                // after the first entry test for ", " insertion or line print

                if (i > 0) {
                    // print new line and reset header on every 4th entry ... otherwise
                    // add a comma

                    if (i % 4 == 0) {
                        scrnWrtr.write(s + NL);
                        s = hdr + "                              ";
                    } else
                        s += ", ";
                }

                // add ith entry and continue

                s += String.format("[%4d] (%5d, %8d)", i, nrays, nelems);
            }

            // done ... dump last line and exit

            scrnWrtr.write(s + NL + NL);
        } // end if (scrnWrtr.isOutputOn())
    }

    /**
     * Returns a new object constructed from the input FileInputBuffer.
     */
    @Override
    public PhaseSiteBase readNew(FileInputBuffer fib) throws IOException {
        ObservationsSourceMap pso = new ObservationsSourceMap();
        pso.read(fib);
        return pso;
    }
}
