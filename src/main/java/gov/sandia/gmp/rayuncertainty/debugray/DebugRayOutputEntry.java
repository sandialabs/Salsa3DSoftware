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
package gov.sandia.gmp.rayuncertainty.debugray;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.util.ArrayList;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerInteger;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.intrinsicsort.IntrinsicSort;

/**
 * Contains the debug ray content summary of a grid point (diagonal) or grid
 * point pair (off-diagonal). The results include the covariance and weight
 * assigned to the point, the point matrix indices, the point layer names,
 * point pair separation distance (km), and the fractional (%) distance of
 * the points from source to receiver. The primary purpose of this object is
 * to collect the partial fragmented results of the debug results returned by
 * each process node during the ray uncertainty calculation and sum and sort
 * the results in preparation for output, which is also performed by this
 * object.
 *
 * @author jrhipp
 */
public class DebugRayOutputEntry {
    /**
     * A static map that is the inverse of the active node map used in the
     * RayUncertainty code. This map maps matrix column indices to tomography
     * grid active node indices. It is assembled in the static function
     * setMatrixColumnMap(...) below where the input is the RayUncertainty
     * active node map. It is used to get grid node indices given matrix column
     * indices for output.
     */
	// TODO PS: Need two static matrix column to active node id maps here. One
	// TODO PS: for P and one for S.
    private static HashMapIntegerInteger aMtrxColMap = null;

    /**
     * The tomography model that was used in ray uncertainty.
     */
    private static GeoTessModel aTomoModel = null;

    /**
     * The layer name array of the layers defined in the tomography model.
     */
    private static String[] aLayerNames = null;

    /**
     * Builds the inverse matrix column index -> active node index from the
     * input active node map that gives active node index -> matrix column index.
     *
     * @param actvNodeMap The input active node index map.
     */
    // TODO PS: Input both P and S active node maps here and build two matrix
    // TODO PS: column to active node index maps here. One for P and one for S.
    public static void setMatrixColumnMap(HashMapIntegerInteger actvNodeMap) {
        // create a new matrix column map and loop over the input active node map

        aMtrxColMap = new HashMapIntegerInteger(2 * actvNodeMap.size());
        HashMapIntegerInteger.Iterator it = actvNodeMap.iterator();
        while (it.hasNext()) {
            // assign the matrix column entry as the inverse of the active node map
            // entry

            HashMapIntegerInteger.Entry e = it.nextEntry();
            aMtrxColMap.put(e.getValue(), e.getKey());
        }
    }

    /**
     * Set the static tomography model.
     *
     * @param tm The tomography model.
     */
    public static void setTomographyModel(GeoTessModel tm) {
        aTomoModel = tm;
    }

    /**
     * Set the static tomography model layer name array.
     *
     * @param lyrNames The tomography model layer name array.
     */
    public static void setLayerNames(String[] lyrNames) {
        aLayerNames = lyrNames;
    }

    /**
     * A sub-class definition that stores all task input entries that are added
     * to this output entry object. Contains the influence receiver ids (A and B
     * if used), the source id, the influence receiver weights and the source
     * influence weight, and the block element row and column weights. The total
     * weight is calculated and assigned to the total weight value, aW.
     *
     * @author jrhipp
     */
    class Entry {
        /**
         * The influence receiver A id.
         */
        public long aIRA = -1;

        /**
         * The influence receiver B id. (may be equal to aIRA for type AA ray input.
         */
        public long aIRB = -1;

        /**
         * The influence source id.
         */
        public long aIS = -1;

        /**
         * The influence weight of receiver A on this entry.
         */
        public double aIWRA = -1.0;

        /**
         * The influence weight of receiver B on this entry.
         */
        public double aIWRB = -1.0;

        /**
         * The influence weight of source on this entry.
         */
        public double aIWS = -1.0;

        /**
         * The block row weight of this entry (a ray weight).
         */
        public double aWI = -1.0;

        /**
         * The block column weight of this entry (a ray weight).
         */
        public double aWJ = -1.0;

        /**
         * The total weight calculated from those above.
         */
        public double aW = -1.0;

        /**
         * Ray type AB constructor. A single entry for a point pair (aP1/aP2)
         * where aP1 may equal aP2 (Diagonal entry).
         *
         * @param rai  The influence receiver A id.
         * @param rbi  The influence receiver B id.
         * @param si   The influence source id.
         * @param iwra The interpolation weight for influence receiver A.
         * @param iwrb The interpolation weight for influence receiver B.
         * @param iws  The interpolation weight for the influence source.
         * @param wi   The aP1 point weight.
         * @param wj   The aP2 point weight.
         */
        public Entry(long rai, long rbi, long si,
                     double iwra, double iwrb, double iws,
                     double wi, double wj) {
            aIRA = rai;
            aIRB = rbi;
            aIS = si;
            aIWRA = iwra;
            aIWRB = iwrb;
            aIWS = iws;
            aWI = wi;
            aWJ = wj;

            // calculate the total weight. If input receiver rays are equal
            // (input ray type is AA) and aP1 != aP2 (off-diagonal) then multiply
            // weight by 2.0 to account for symmetric component.

            aW = aIWRA * aIWRB * aIWS * aWI * aWJ;
            if ((aIRA == aIRB) && (aP1 != aP2)) aW *= 2.0;
        }

        /**
         * Ray type AA constructor. A single entry for a point pair (aP1/aP2)
         * where aP1 may equal aP2 (Diagonal entry).
         *
         * @param rai  The influence receiver A id.
         * @param si   The influence source id.
         * @param iwra The interpolation weight for influence receiver A.
         * @param iws  The interpolation weight for the influence source.
         * @param wi   The aP1 point weight.
         * @param wj   The aP2 point weight.
         */
        public Entry(long rai, long si,
                     double iwra, double iws,
                     double wi, double wj) {
            aIRA = rai;
            aIRB = rai;
            aIS = si;
            aIWRA = iwra;
            aIWRB = 1.0;
            aIWS = iws;
            aWI = wi;
            aWJ = wj;

            // calculate the total weight. if aP1 != aP2 (off-diagonal) then
            // multiply weight by 2.0 to account for symmetric component.

            aW = aIWRA * aIWS * aWI * aWJ;
            if (aP1 != aP2) aW *= 2.0;
        }
    }

    /**
     * The first point matrix column index of the two points whose covariance
     * contribution is to be calculated. Note that aP1 == aP2 for diagonal
     * entries.
     */
    private int aP1 = -1;

    /**
     * The second point matrix column index of the two points whose covariance
     * contribution is to be calculated. Note that aP1 == aP2 for diagonal
     * entries.
     */
    private int aP2 = -1;

    /**
     * The layer name of the first point.
     */
    private String aP1Layer = "";

    /**
     * The layer name of the second point.
     */
    private String aP2Layer = "";

    /**
     * The fractional separation distance of the two points. They are normalized
     * with the total distance between the source and receiver for which these
     * entries are accumulated.
     */
    private double aSepDist = 0.0;

    /**
     * The first point distance fraction relative to the source. It is normalized
     * with the total distance between the source and receive.
     */
    private double aP1Dist = 0.0;

    /**
     * The second point distance fraction relative to the source. It is normalized
     * with the total distance between the source and receive.
     */
    private double aP2Dist = 0.0;

    /**
     * The covariance between the two points aP1 and aP2.
     */
    private double aCov = Double.NaN;

    /**
     * The total weight summed from the final weight of all entries in the entry
     * list.
     */
    private double aWght = 0.0;

    /**
     * The entry list of all entries added to this output entry.
     */
    private ArrayList<Entry> aEntryList = null;

    /**
     * Standard constructor.
     *
     * @param p1          The first matrix column index for which a covariance
     *                    contribution will be calculated.
     * @param p2          The second matrix column index for which a covariance
     *                    contribution will be calculated.
     * @param cov         The covariance of the two matrix column entries p1 and
     *                    p2.
     * @param src         The Source associated with all entries contained by
     *                    this entry object.
     * @param srcRcvrDist The source to receiver 3D distance in kilometers. This
     *                    value is calculated in the DebugRayOutput object that
     *                    owns this entry object.
     */
    public DebugRayOutputEntry(int p1, int p2, double cov,
                               Source src, double srcRcvrDist) {
        aP1 = p1;
        aP2 = p2;
        aCov = cov;

        // create a new entry list, calculate the distance fractions, and set the
        // layer names.

        aEntryList = new ArrayList<Entry>();
        setDistanceFractions(src, srcRcvrDist);
        // TODO PS: Only one of the two aMtrxColMaps (P or S) will hold aP1 and aP2.
        // TODO PS: Use whichever contains the entry. 
        // TODO PS:   private int getGlobalActiveNodeIndexFromMatrixColumn(int mc) {
        // TODO PS:        if (aMtrxColMapP.contains(mc))
        // TODO PS:          return aMtrxColMapP.get(mc);
        // TODO PS:        else
        // TODO PS:          return aMtrxColMapS.get(mc);
        // TODO PS:   }
        // TODO PS:
        // TODO PS: Wherever aMtrxColMap is used below replace it usage with a call
        // TODO PS: to getGlobalActiveNodeIndexFromMatrixColumn(int mc).
        aP1Layer = aLayerNames[aTomoModel.getPointMap().getLayerIndex(aMtrxColMap.get(aP1))];
        aP2Layer = aLayerNames[aTomoModel.getPointMap().getLayerIndex(aMtrxColMap.get(aP2))];
    }

    /**
     * Adds a new ray type AA entry to the entry list.
     *
     * @param RI  Influence receiver (A) id.
     * @param SI  Influence source id.
     * @param IWR Influence receiver (A) weight.
     * @param IWS Influence source weight.
     * @param wI  block row weight.
     * @param wJ  block column weight.
     */
    public void addEntry(long RI, long SI, double IWR, double IWS,
                         double wI, double wJ) {
        aEntryList.add(new Entry(RI, SI, IWR, IWS, wI, wJ));
    }

    /**
     * Adds a new ray type AB entry to the entry list. Note that the input may
     * have RAI == RBI which means it came from a debug type AA ray.
     *
     * @param RAI  Influence receiver (A) id.
     * @param RBI  Influence receiver (B) id.
     * @param SI   Influence source id.
     * @param IWRA Influence receiver (A) weight.
     * @param IWRB Influence receiver (B) weight.
     * @param IWS  Influence source weight.
     * @param wI   block row weight.
     * @param wJ   block column weight.
     */
    public void addEntry(long RAI, long RBI, long SI,
                         double IWRA, double IWRB, double IWS,
                         double wI, double wJ) {
        aEntryList.add(new Entry(RAI, RBI, SI, IWRA, IWRB, IWS, wI, wJ));
    }

    /**
     * Sort is called one time to accumulate the total weight and to place the
     * entries in sorted order. Calling addEntry after this call invalidates the
     * sort.
     */
    public void sort() {
        // create storage for the index and weight arrays

        int[] ai = new int[aEntryList.size()];
        double[] bi = new double[aEntryList.size()];

        // build the index array ai and the weight array bi to be sorted ...
        // sum the total weight into aWght

        aWght = 0.0;
        for (int i = 0; i < aEntryList.size(); ++i) {
            Entry e = aEntryList.get(i);
            ai[i] = i;
            bi[i] = Math.abs(e.aW);
            aWght += e.aW;
        }

        // sort on the weights (bi) and the accompanying index array ai and invert
        // the order of ai to descending

        IntrinsicSort.sort(bi, ai);
        IntrinsicSort.reverseOrder(ai);

        // create a new list and copy entries over to the new list in sorted
        // order ... assign the new list to aEntryList and exit

        ArrayList<Entry> newList = new ArrayList<Entry>(aEntryList.size());
        for (int i = 0; i < aEntryList.size(); ++i)
            newList.add(aEntryList.get(ai[i]));
        aEntryList = newList;
    }

    /**
     * Sets the fractional distance from the source to the points aP1 and aP2
     * relative to the input source-to-receiver distance. Also sets the point
     * separation distance as a fraction of the input source-to-receiver distance.
     * So a point with a near zero fraction is close to the source while a point
     * with a fraction near 1 is close to the receiver. Point separation fractions
     * close to zero infer that the points are near in proximity and point
     * separation fractions close to 1 mean the points are at opposite ends of
     * the ray. All measurements are done using the 3D distance calculation
     * between points.
     *
     * @param src         The source used to calculate the point distance
     *                    fractions.
     * @param srcRcvrDist THe source-to-receiver distance (3D) (km).
     */
    private void setDistanceFractions(Source src, double srcRcvrDist) {
        // get the first points position and calculate the distance fraction

    	// TODO PS: Use getGlobalActiveNodeIndexFromMatrixColumn(int mc).
        double[] v1 = aTomoModel.getPointMap().getPointUnitVector(aMtrxColMap.get(aP1));
        double r1 = aTomoModel.getPointMap().getPointRadius(aMtrxColMap.get(aP1));
        GeoVector p1 = new GeoVector(v1, r1);
        aP1Dist = p1.distance3D(src) / srcRcvrDist;

        // see if point 2 is different than point 1

        if (aP1 != aP2) {
            // get the second points position and calculate the distance fraction

        	// TODO PS: Use getGlobalActiveNodeIndexFromMatrixColumn(int mc).
            double[] v2 = aTomoModel.getPointMap().getPointUnitVector(aMtrxColMap.get(aP2));
            double r2 = aTomoModel.getPointMap().getPointRadius(aMtrxColMap.get(aP2));
            GeoVector p2 = new GeoVector(v2, r2);
            aP2Dist = p2.distance3D(src) / srcRcvrDist;

            // calculate the separation fraction.

            aSepDist = p1.distance3D(p2) / srcRcvrDist;
        } else {
            // second point is the same as the first ... set separation fraction to
            // zero and point distance to the first points result

            aSepDist = 0.0;
            aP2Dist = aP1Dist;
        }
    }

    /**
     * Returns a string of formatted output representing the output entry. The
     * output is different for type AB rays (typeAB is true) versus type AA rays
     * (typeAB is not true). Also output differs if this is a diagonal (aP1 == aP2)
     * versus off diagonal entry.
     *
     * @param typeAB     The ray type. Type AB if true.
     * @param entryIndex The entry index from the DebugRayOutput owner. Used
     *                   strictly in the header information.
     * @return A string of formatted output representing the output entry.
     */
    public String outputRayWghtCmpnts(boolean typeAB, int entryIndex) {
        String s;

        // see if this entry is diagonal or not

        if (!isDiagonal()) {
            // diagonal entry ... make header

            String hdr = Globals.repeat(" ", 38);
        	// TODO PS: Use getGlobalActiveNodeIndexFromMatrixColumn(int mc).
            s = "          " + String.format("%5d  %8d  %8d   ", entryIndex,
                    aMtrxColMap.get(aP1),
                    aMtrxColMap.get(aP2));

            // see if this is a type AB ray

            String fmt;
            double wsum = 0.0;
            if (typeAB) {
                // type AB ray ... loop over all entries in the list and build the
                // output string

                fmt = "%8d    %8d   %8d  %9.3e  %9.3e  %9.3e   %6.2f    %9.3e   %6.2f";
                for (int i = 0; i < aEntryList.size(); ++i) {
                    // get the next entry, increment the weight sum and output a formatted
                    // line

                    Entry e = aEntryList.get(i);
                    if (i > 0) s += hdr;
                    wsum += e.aW;
                    s += String.format(fmt, e.aIRA, e.aIRB, e.aIS, e.aWI, e.aWJ, e.aW,
                            100.0 * e.aW / aWght, wsum,
                            100.0 * wsum / aWght) + NL;
                }
            } else {
                // type AA ray ... loop over all entries in the list and build the
                // output string

                fmt = "%8d    %8d  %9.3e  %9.3e  %9.3e   %6.2f    %9.3e   %6.2f";
                for (int i = 0; i < aEntryList.size(); ++i) {
                    // get the next entry, increment the weight sum and output a formatted
                    // line

                    Entry e = aEntryList.get(i);
                    if (i > 0) s += hdr;
                    wsum += e.aW;
                    s += String.format(fmt, e.aIRA, e.aIS, e.aWI, e.aWJ, e.aW,
                            100.0 * e.aW / aWght, wsum,
                            100.0 * wsum / aWght) + NL;
                }
            }
        } else {
            // off-diagonal entry ... make header

        	// TODO PS: Use getGlobalActiveNodeIndexFromMatrixColumn(int mc).
            String hdr = Globals.repeat(" ", 28);
            s = "           " + String.format("%4d  %8d   ", entryIndex,
                    aMtrxColMap.get(aP1));

            // see if this is a type AB ray

            String fmt;
            double wsum = 0.0;
            if (typeAB) {
                // type AB ray ... loop over all entries in the list and build the
                // output string

                fmt = "%8d    %8d   %8d  %9.3e  %9.3e   %6.2f    %9.3e   %6.2f";
                for (int i = 0; i < aEntryList.size(); ++i) {
                    // get the next entry, increment the weight sum and output a formatted
                    // line

                    Entry e = aEntryList.get(i);
                    if (i > 0) s += hdr;
                    wsum += e.aW;
                    s += String.format(fmt, e.aIRA, e.aIRB, e.aIS, e.aWI, e.aW,
                            100.0 * e.aW / aWght, wsum,
                            100.0 * wsum / aWght) + NL;
                }
            } else {
                // type AA ray ... loop over all entries in the list and build the
                // output string

                fmt = "%8d   %8d  %9.3e  %9.3e   %6.2f    %9.3e   %6.2f";
                for (int i = 0; i < aEntryList.size(); ++i) {
                    // get the next entry, increment the weight sum and output a formatted
                    // line

                    Entry e = aEntryList.get(i);
                    if (i > 0) s += hdr;
                    wsum += e.aW;
                    s += String.format(fmt, e.aIRA, e.aIS, e.aWI, e.aW,
                            100.0 * e.aW / aWght, wsum,
                            100.0 * wsum / aWght) + NL;
                }
            }
        }

        // return the assembled string to the caller

        return s;
    }

    /**
     * Returns the total weight of this entry.
     *
     * @return The total weight of this entry.
     */
    public double getWeight() {
        return aWght;
    }

    /**
     * Returns the covariance of this entry.
     *
     * @return The covariance of this entry.
     */
    public double getCovariance() {
        return aCov;
    }

    /**
     * Returns the weighted covariance of this entry.
     *
     * @return The weighted covariance of this entry.
     */
    public double getWeightedCovariance() {
        return aWght * aCov;
    }

    /**
     * Returns true if this entry is diagonal (aP1 == aP2).
     *
     * @return True if this entry is diagonal (aP1 == aP2).
     */
    public boolean isDiagonal() {
        return (aP1 == aP2);
    }

    /**
     * Returns true if aP1 (matrix column) exceeds the maximum matrix input size.
     * If so then aP1 is a non-represented point
     *
     * @param maxMtrxSize
     * @return True if aP1 (matrix column) exceeds the maximum matrix input size.
     */
    public boolean isNonRepresented(int maxMtrxSize) {
        return (aP1 >= maxMtrxSize);
    }

    /**
     * Returns the matrix column index of point i (== 1 or 2).
     *
     * @param i The matrix column index of point 1 or 2 (any other input returns
     *          -1).
     * @return The matrix column index of point i (== 1 or 2).
     */
    public int getMatrixColumnIndex(int i) {
        if (i == 1)
            return aP1;
        else if (i == 2)
            return aP2;
        else
            return -1;
    }

    /**
     * Returns the grid point index of point i (== 1 or 2).
     *
     * @param i The grid point index of point 1 or 2 (any other input returns -1).
     * @return The grid point index of point i (== 1 or 2).
     */
	// TODO PS: Use getGlobalActiveNodeIndexFromMatrixColumn(int mc).
    public int getGridPointIndex(int i) {
        if (i == 1)
            return aMtrxColMap.get(aP1);
        else if (i == 2)
            return aMtrxColMap.get(aP2);
        else
            return -1;
    }

    /**
     * Return the layer name of point i (== 1 or 2).
     *
     * @param i The point index 1 or 2 (any other input returns "").
     * @return The layer name of point i (== 1 or 2).
     */
    public String getLayerName(int i) {
        if (i == 1)
            return aP1Layer;
        else if (i == 2)
            return aP2Layer;
        else
            return "";
    }

    /**
     * Return the distance fraction of point i (== 1 or 2) from the source
     * relative to the source to receiver distance.
     *
     * @param i The point index 1 or 2 (any other input returns -1.0).
     * @return The distance fraction of point i (== 1 or 2) from the source
     * relative to the source to receiver distance.
     */
    public double getSourceToPointDistFraction(int i) {
        if (i == 1)
            return aP1Dist;
        else if (i == 2)
            return aP2Dist;
        else
            return -1.0;
    }

    /**
     * Returns the fractional distance between aP1 and aP2 relative to the total
     * distance between the source and receiver, where aP1 and aP2 are two grid
     * points that lie on the ray. This fraction can be between zero, if aP1 ==
     * aP2, to one, if aP1 and aP2 lie at the extremes of the ray (one at the
     * source and one at the receiver.
     *
     * @return The fractional distance between aP1 and aP2 relative to the total
     * distance between the source and receiver
     */
    public double getPointSeparationDistFraction() {
        return aSepDist;
    }
}
