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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.rayuncertainty.basecontainers.Observation;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerInteger;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.intrinsicsort.IntrinsicSort;

/**
 * Used to organize and output any debug observations. Primarily the ray path
 * is output in both binary and ascii.
 *
 * @author jrhipp
 */
public class DebugObservation {
    /**
     * An internal hash map associating an observation with its DebugObservation.
     * All DebugObservations. This map is populated with the static function
     * addObservation which is the only way to create new DebugObservations
     * (private constructors).
     */
    private static HashMap<Observation, DebugObservation> aDebugObsMap =
            new HashMap<Observation, DebugObservation>();

    /**
     * A map of all created DebugObservations associated with its receiver id and
     * source id.
     */
    private static HashMap<Long, HashMap<Long, DebugObservation>> aDebugRcvrSrcMap =
            new HashMap<Long, HashMap<Long, DebugObservation>>();

    /**
     * The tomography active node map set statically from RayUncertainty if
     * debug information has been requested.
     */
    // TODO PS: Add another field to define both P and S maps
    private static HashMapIntegerInteger aActiveNodeMap = null;

    /**
     * This DebugObservations Observation object.
     */
    private Observation aObs = null;

    /**
     * This DebugObservations Receiver object.
     */
    private Receiver aRcvr = null;

    /**
     * This DebugObservations Source object.
     */
    private Source aSrc = null;

    /**
     * The DebugObservations ray path.
     */
    private ArrayList<GeoVector> aRayPath = null;

    // TODO PS: Add a field to contain the P and S wavetype.
    // TODO PS: This can be run-length-encoded array that
    // TODO PS: saves size at the point of wave type change.
    // TODO PS: (See document discussion on Ray.resample(...))
    /**
     * The array of active node indices influencing this ray.
     */
    private int[] aIndex = null;

    /**
     * The array of active node influence weights for this ray.
     */
    private double[] aWeight = null;

    /**
     * Sets the active node map which maps grid point indexes into matrix column
     * indexes. Must be called before using the function output(...).
     *
     * @param anm The tomographic active node map which maps grid point indexes
     *            into matrix column indexes.
     */
    // TODO PS: Add two maps ... the P and S. Requires another field definition.
    public static void setActiveNodeMap(HashMapIntegerInteger anm) {
        aActiveNodeMap = anm;
    }

    /**
     * Sets the ray path, grid point indices, and grid point weights for this
     * DebugObservation. These are ouput when the output(...) function is called.
     *
     * @param rayPath The list of GeoVectors defining the ray path for this
     *                DebugObservation.
     * @param indx    The array of grid point indices of all grid points that
     *                influence the ray path.
     * @param wght    The array of grid point weights of all grid points that
     *                influence the ray path.
     */
    // TODO PS: input the run-length-encoded wavetype list here.
    public void setRay(ArrayList<GeoVector> rayPath, int[] indx, double[] wght) {
        aRayPath = rayPath;
        aIndex = new int[indx.length];
        aWeight = new double[indx.length];
        for (int i = 0; i < indx.length; ++i) {
            aIndex[i] = indx[i];
            aWeight[i] = wght[i];
        }
    }

    /**
     * Private default constructor.
     */
    private DebugObservation() {
        // no code
    }

    /**
     * Private constructor to create a new DebugObservation.
     *
     * @param obs  The Observation object of this new DebugObservation.
     * @param rcvr The Receiver object of this new DebugObservation.
     * @param src  The Source object of this new DebugObservation.
     */
    private DebugObservation(Observation obs, Receiver rcvr, Source src) {
        aObs = obs;
        aRcvr = rcvr;
        aSrc = src;
    }

    /**
     * Returns the DebugObservatino associated with the input Observation.
     *
     * @param obs The observation for which the associated DebugObservation will
     *            be returned.
     * @return The DebugObservatino associated with the input Observation.
     */
    public static DebugObservation getDebugObservation(Observation obs) {
        return aDebugObsMap.get(obs);
    }

    /**
     * Returns the DebugObservation associated with the input receiver/source ids,
     * or null if there isn't one.
     *
     * @param rcvrId The receiver id for which an associated DebugObservation will
     *               be returned.
     * @param srcId  The source id for which an associated DebugObservation will
     *               be returned.
     * @return The DebugObservation associated with the input receiver/
     * source id's, or null if there isn't one.
     */
    public static DebugObservation getDebugObservation(long rcvrId, long srcId) {
        HashMap<Long, DebugObservation> srcMap;

        srcMap = aDebugRcvrSrcMap.get(rcvrId);
        if (srcMap != null)
            return srcMap.get(srcId);

        return null;
    }

    /**
     * Returns true if the input observation is associated with a DebugObservation.
     *
     * @param obs The observation of a debug observation.
     * @return True if the input observation is associated with a DebugObservation.
     */
    public static boolean containsObservation(Observation obs) {
        return aDebugObsMap.containsKey(obs);
    }

    /**
     * Returns true if a DebugObservation is associated with the input receiver/
     * source id.
     *
     * @param rcvrId The receiver id of a debug observation.
     * @param srcId  The source id of a debug observation.
     * @return True if a DebugObservation is associated with the input receiver/
     * source id.
     */
    public static boolean containsReceiverSource(long rcvrId, long srcId) {
        HashMap<Long, DebugObservation> srcMap;

        srcMap = aDebugRcvrSrcMap.get(rcvrId);
        if (srcMap != null)
            return srcMap.containsKey(srcId);

        return false;
    }

    /**
     * Add a new DebugObservation into the static maps. This is the only way to
     * create a new DebugObservation.
     *
     * @param obs  The observation associated with the DebugObservation
     * @param rcvr The Receiver defined for the input Observation.
     * @param src  The Source defined for the input Observation.
     */
    public static void addObservation(Observation obs, Receiver rcvr, Source src) {
        HashMap<Long, DebugObservation> srcMap;

        // see if the observation is already contained

        if (!aDebugObsMap.containsKey(obs)) {
            // not contained ... create a new DebugObservation and add it to the
            // observation map associated with the input observation

            DebugObservation dbgobs = new DebugObservation(obs, rcvr, src);
            aDebugObsMap.put(obs, dbgobs);

            // add it to the source receiver/source map also

            srcMap = aDebugRcvrSrcMap.get(rcvr.getReceiverId());
            if (srcMap == null) {
                // receiver id is not yet defined create a new source map and insert
                // it into the receiver/source map associated with the receiver id

                srcMap = new HashMap<Long, DebugObservation>();
                aDebugRcvrSrcMap.put(rcvr.getReceiverId(), srcMap);
            }

            // add the new debug observation into the source map associated with the
            // source id

            srcMap.put(src.getSourceId(), dbgobs);
        }
    }

    /**
     * Returns a list of all defined DebugObservations.
     *
     * @return A list of all defined DebugObservations.
     */
    public static ArrayList<DebugObservation> getList() {
        ArrayList<DebugObservation> lst;
        lst = new ArrayList<DebugObservation>(aDebugObsMap.size());
        for (Map.Entry<Observation, DebugObservation> e :
                aDebugObsMap.entrySet())
            lst.add(e.getValue());

        return lst;
    }

    /**
     * Outputs this Debug observation to disk. Both binary and ascii outputs are
     * provided
     *
     * @param debugPath     The path to where the output files are written.
     * @param tomoModel     The Tomography model.
     * @param layerNames    The array of layer names in the tomography model.
     * @param maxRepMtrxCol The Maximum matrix column index that is represented
     *                      in the covariance matrix.
     * @throws IOException
     */
    public void outputRay(String debugPath, GeoTessModel tomoModel,
                          String[] layerNames, int maxRepMtrxCol)
            throws IOException {
        // Define an inner class to aid in sorting the weight information.

        class DebugRayWeightOutput {
            public int ani = -1;
            public int mc = -1;
            public String repStr = "";
            public String lyrNam = "";
            public double depth = 0.0;
            public double wght = 0.0;

            public DebugRayWeightOutput(int ani, int mc, String repStr,
                                        String lyrNam, double depth,
                                        double wght) {
                this.ani = ani;
                this.mc = mc;
                this.repStr = repStr;
                this.lyrNam = lyrNam;
                this.depth = depth;
                this.wght = wght;
            }
        }

        // get the source and receive ids and the source and receiver

        long rcvrid = aObs.getReceiverId();
        long srcid = aObs.getSourceId();

        // get the active node map and maximum column index
        // of represented nodes in the covariance matrix (n).

        // open the debug file and output header information

        String fn = debugPath + File.separator + "rayWeights_Site_" +
                rcvrid + "_Src_" + srcid;
        ;
        FileWriter fw = new FileWriter(fn);
        fw.write(Globals.getTimeStamp() + NL);
        fw.write("  Influence Receiver Id, Name, <position> = " + rcvrid +
                ", " + aRcvr.getSta() + ", <" +
                aRcvr.toString() + ">" + NL +
                "  Influence Source Id, <position> = " + srcid +
                ", <" + aSrc.toString() + ">" + NL +
                "  Ray Index = " + aObs.getRayIndex() + NL);
        //"  Entry  Active   Matrix    Node         Layer             Weight
        //"          Node    Column    Type
        //"   ddd   dddddd   dddddd   ssssss   ssssssssssssssss   gggggggggggggg
        //"    Depth           Distance"
        //"     (km)             (deg)"
        //"gggggggggggggg   gggggggggggggg
        fw.write("  Entry  Active   Matrix    Node         Layer" +
                "             Weight           Depth           Distance" + NL);
        fw.write("          Node    Column    Type              " +
                "                               (km)             (deg)" + NL);

        // get the ray weights and build a sort-able structure to sort indices on
        // distance from receiver

        double[] distance = new double[aWeight.length];
        DebugRayWeightOutput[] drwo = new DebugRayWeightOutput[aWeight.length];
        GeoVector gv = new GeoVector();
        for (int ix = 0; ix < aIndex.length; ++ix) {
            double[] uv = tomoModel.getPointMap().getPointUnitVector(aIndex[ix]);
            double r = tomoModel.getPointMap().getPointRadius(aIndex[ix]);
            gv.setGeoVector(uv, r);
            distance[ix] = aSrc.distance3D(gv);
            double depth = gv.getDepth();
            int li = tomoModel.getPointMap().getLayerIndex(aIndex[ix]);
            // TODO PS: Use the run-length-encoded list to determine if entry ix is
            // TODO PS: P or S here. Use the appropriate active node map to retrieve
            // TODO PS: the matrix column (mc).
            int mc = aActiveNodeMap.get(aIndex[ix]);
            String a = "   Rep";
            if (mc >= maxRepMtrxCol) a = "NonRep";
            drwo[ix] = new DebugRayWeightOutput(aIndex[ix], mc, a, layerNames[li],
                    depth, aWeight[ix]);
        }

        // sort structure and output ray weights in a distance sorted manner

        IntrinsicSort.sort(distance, drwo);
        String fmt = "   %3d   %6d   %6d   %6s   %16s   %14g   %14g   %14g";
        for (int ix = 0; ix < aIndex.length; ++ix) {
            fw.write(String.format(fmt, ix,
                    drwo[ix].ani, drwo[ix].mc, drwo[ix].repStr, drwo[ix].lyrNam,
                    drwo[ix].wght, drwo[ix].depth, distance[ix]) + NL);
        }
        fw.close();

        // now get ray path and write ray path file

        fn = debugPath + File.separator + "rayPath_Site_" +
                rcvrid + "_Src_" + srcid;
        FileOutputBuffer fob = new FileOutputBuffer(fn);

        // write number of rayPath entries and ray path positions

        fob.writeInt(aRayPath.size());
        for (int ip = 0; ip < aRayPath.size(); ++ip) {
            gv = aRayPath.get(ip);
            double[] pos = gv.getUnitVector();
            double rad = gv.getRadius();
            fob.writeDouble(rad * pos[0]);
            fob.writeDouble(rad * pos[1]);
            fob.writeDouble(rad * pos[2]);
        }

        // now write number of weights and each weights active node index,
        // grid node index, major layer index, sub layer index, and weight

        fob.writeInt(aWeight.length);
        for (int ip = 0; ip < aWeight.length; ++ip) {
            fob.writeInt(aIndex[ip]);
            int[] ids = tomoModel.getPointMap().getPointIndices(aIndex[ip]);
            fob.writeInt(ids[0]); // vertex id
            fob.writeInt(ids[1]);  // layer
            fob.writeInt(ids[2]);   // node id
            fob.writeDouble(aWeight[ip]);
        }

        // done ... close file and continue

        fob.close();
    }
}
