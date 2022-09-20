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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.intrinsicsort.IntrinsicSort;

/**
 * Contains a specific debug ray content summary from all influencing grid
 * points that uniquely define this ray. This object contains the ray
 * specification including, the model name, phase, site A, site B, source, and
 * source to receiver distance. It also, holds the site A, site B (if defined),
 * and source ray influence weight maps. When the ray entries are sorted they
 * are divided into diagonal, off-diagonal, and non-represented sets for output.
 * <p>
 * All entries containing partial results from specific ray uncertainty tasks
 * are accumulated in a DebugRayOutputEntry defined specifically for a unique
 * grid point pair in the ray (the pair may be the same grid point for diagonal
 * entries). Each of those DebugRayOutputEntries are stored in a map associated
 * with the two grid points (aMap).
 * <p>
 * All DebugRayOutput objects have private constructors and may only be created
 * by calling the 4 static function addDebugRay(...) functions below. They
 * support creation of a DebugRayOutput object for both type AA and type AB
 * rays and for cases where the Source is totally defined or for when only the
 * source id is known.
 * <p>
 * All Created DebugRayOutput objects are contained in a static map and a list.
 * The map entries are associated with their model name, phase, receiver A,
 * receiver B, and source id.
 * <p>
 * When all the DebugRayOutputEntry objects contained in a DebugRayOutput object
 * are defined the DebugRayOutput.sort() function is called to order them and
 * then the DebugRayOutput.output() function can be called to write the results
 * to the screen/file.
 *
 * @author jrhipp
 */
public class DebugRayOutput {
    /**
     * The static maximum size of the matrix. Used to determine non-represented
     * from represented grid points. Set statically before any DebugRayOutput
     * objects are created.
     */
    private static int aCovMtrxSize = 0;

    /**
     * The static DebugRayOutput map that contains all instantiated DebugRayOutput
     * objects associated with their model name, phase, receiver A, receiver B,
     * and source id. It is populated by calling one of the 4 addDebugRay(...)
     * functions.
     */
    private static HashMap<String, HashMap<SeismicPhase, HashMap<Receiver,
            HashMap<Receiver, HashMap<Long,
                    DebugRayOutput>>>>> aDebugRayOutputMap =
            new HashMap<String, HashMap<SeismicPhase, HashMap<Receiver,
                    HashMap<Receiver, HashMap<Long, DebugRayOutput>>>>>();

    /**
     * The static DebugrayOutput list that contains all instantiated
     * DebugRayOutput objects. It is populated by calling one of the 4
     * addDebugRay(...) functions.
     */
    private static ArrayList<DebugRayOutput> aDebugRayOutputList =
            new ArrayList<DebugRayOutput>();

    /**
     * Set the static maximum matrix size. This value is used to determine
     * non-represented from represented grid points. Set statically before any
     * DebugRayOutput objects are created.
     *
     * @param cms The maximum represented matrix size.
     */
    public static void setCovarianceMatrixSize(int cms) {
        aCovMtrxSize = cms;
    }

    /**
     * Returns the static list of all instantiated DebugRayOutput objects.
     *
     * @return The static list of all instantiated DebugRayOutput objects.
     */
    public static ArrayList<DebugRayOutput> getList() {
        return aDebugRayOutputList;
    }

    /**
     * Instantiates a new type AB debug ray and adds the ray to the internal
     * static list.
     *
     * @param mdlName The model name for which the debug ray is defined.
     * @param ph      The phase for which the debug ray is defined.
     * @param rcvrA   The receiver A for which the debug ray is defined.
     * @param rcvrB   The receiver B for which the debug ray is defined.
     * @param srcid   The source id for which the debug ray is defined.
     * @throws GMPException
     * @throws IOException
     */
    public static void addDebugRay(String mdlName, SeismicPhase ph,
                                   Receiver rcvrA, Receiver rcvrB, long srcid)
            throws GMPException, IOException {
        DebugRayOutput dro = new DebugRayOutput(mdlName, ph, rcvrA, rcvrB, srcid);
        addDebugRay(dro);
    }

    /**
     * Instantiates a new type AB debug ray and adds the ray to the internal
     * static list.
     *
     * @param mdlName The model name for which the debug ray is defined.
     * @param ph      The phase for which the debug ray is defined.
     * @param rcvrA   The receiver A for which the debug ray is defined.
     * @param rcvrB   The receiver B for which the debug ray is defined.
     * @param src     The source for which the debug ray is defined.
     * @throws GMPException
     * @throws IOException
     */
    public static void addDebugRay(String mdlName, SeismicPhase ph,
                                   Receiver rcvrA, Receiver rcvrB, Source src)
            throws GMPException, IOException {
        DebugRayOutput dro = new DebugRayOutput(mdlName, ph, rcvrA, rcvrB, src);
        addDebugRay(dro);
    }

    /**
     * Instantiates a new type AA debug ray and adds the ray to the internal
     * static list.
     *
     * @param mdlName The model name for which the debug ray is defined.
     * @param ph      The phase for which the debug ray is defined.
     * @param rcvrA   The receiver for which the debug ray is defined.
     * @param srcid   The source id for which the debug ray is defined.
     * @throws GMPException
     * @throws IOException
     */
    public static void addDebugRay(String mdlName, SeismicPhase ph,
                                   Receiver rcvrA, long srcid)
            throws GMPException, IOException {
        DebugRayOutput dro = new DebugRayOutput(mdlName, ph, rcvrA, srcid);
        addDebugRay(dro);
    }

    /**
     * Instantiates a new type AA debug ray and adds the ray to the internal
     * static list.
     *
     * @param mdlName The model name for which the debug ray is defined.
     * @param ph      The phase for which the debug ray is defined.
     * @param rcvrA   The receiver for which the debug ray is defined.
     * @param src     The source for which the debug ray is defined.
     * @throws GMPException
     * @throws IOException
     */
    public static void addDebugRay(String mdlName, SeismicPhase ph,
                                   Receiver rcvrA, Source src)
            throws GMPException, IOException {
        DebugRayOutput dro = new DebugRayOutput(mdlName, ph, rcvrA, src);
        addDebugRay(dro);
    }

    /**
     * Private static function called by all public addDebugRay function to add
     * the new DebugRayOutput object to the static map and list.
     *
     * @param dro The new DebugRayOutput object to be added to the static map and
     *            list.
     * @throws IOException
     */
    private static void addDebugRay(DebugRayOutput dro) throws IOException {
        HashMap<SeismicPhase, HashMap<Receiver, HashMap<Receiver,
                HashMap<Long, DebugRayOutput>>>> droPhaseMap;
        HashMap<Receiver, HashMap<Receiver, HashMap<Long,
                DebugRayOutput>>> droRcvrAMap;
        HashMap<Receiver, HashMap<Long, DebugRayOutput>> droRcvrBMap;
        HashMap<Long, DebugRayOutput> droSrcIdMap;

        // see if the model name is defined in the map ... if not add it

        droPhaseMap = aDebugRayOutputMap.get(dro.aModelName);
        if (droPhaseMap == null) {
            droPhaseMap = new HashMap<SeismicPhase, HashMap<Receiver,
                    HashMap<Receiver, HashMap<Long,
                            DebugRayOutput>>>>();
            aDebugRayOutputMap.put(dro.aModelName, droPhaseMap);
        }

        // see if the phase is defined in the map ... if not add it

        droRcvrAMap = droPhaseMap.get(dro.aPhase);
        if (droRcvrAMap == null) {
            droRcvrAMap = new HashMap<Receiver, HashMap<Receiver, HashMap<Long,
                    DebugRayOutput>>>();
            droPhaseMap.put(dro.aPhase, droRcvrAMap);
        }

        // see if the receiver A is defined in the map ... if not add it

        droRcvrBMap = droRcvrAMap.get(dro.aRcvrA);
        if (droRcvrBMap == null) {
            droRcvrBMap = new HashMap<Receiver, HashMap<Long,
                    DebugRayOutput>>();
            droRcvrAMap.put(dro.aRcvrA, droRcvrBMap);
        }

        // see if receiver B is in the map ... use rcvrA over as the key if rcvrB
        // is null

        if (dro.aRcvrB == null)
            droSrcIdMap = droRcvrBMap.get(dro.aRcvrA);
        else
            droSrcIdMap = droRcvrBMap.get(dro.aRcvrB);

        // if Receiver B (or A if B is null) is not in the map add it

        if (droSrcIdMap == null) {
            droSrcIdMap = new HashMap<Long, DebugRayOutput>();
            if (dro.aRcvrB == null)
                droRcvrBMap.put(dro.aRcvrA, droSrcIdMap);
            else
                droRcvrBMap.put(dro.aRcvrB, droSrcIdMap);
        }

        // throw error if a DebugRayOutput object is already defined for this
        // entry ... otherwise add it and continue

        if (droSrcIdMap.containsKey(dro.aSrcId)) {
            // error ... doubly defined ray

            String s = "Error: Attempt to insert a previously defined ray:" +
                    NL + "        model name=" + dro.aModelName +
                    ", phase=" + dro.aPhase.name() + ", receiver=" +
                    dro.aRcvrA.getSta();
            if ((dro.aRcvrB != null) && (dro.aRcvrA != dro.aRcvrB))
                s += ", receiver(B)=" + dro.aRcvrB.getSta();
            s += ", source id=" + dro.aSrcId + " ...";
            throw new IOException(s);
        }

        // new definition ... add dro to both the map and the list and exit

        droSrcIdMap.put(dro.aSrcId, dro);
        aDebugRayOutputList.add(dro);
    }

    /**
     * Returns an entry from the static DebugRayOutput map or null if it not
     * contained.
     *
     * @param modelName The model name associated with the requested entry.
     * @param phase     The phase associated with the requested entry.
     * @param rcvrA     The receiver A associated with the requested entry.
     * @param rcvrB     The receiver B associated with the requested entry.
     * @param srcId     The source id associated with the requested entry.
     * @return The requested entry or null if it is not contained.
     */
    public static DebugRayOutput getDebugRay(String modelName, SeismicPhase phase,
                                             Receiver rcvrA, Receiver rcvrB,
                                             long srcId) {
        HashMap<SeismicPhase, HashMap<Receiver, HashMap<Receiver,
                HashMap<Long, DebugRayOutput>>>> droPhaseMap;
        HashMap<Receiver, HashMap<Receiver, HashMap<Long,
                DebugRayOutput>>> droRcvrAMap;
        HashMap<Receiver, HashMap<Long, DebugRayOutput>> droRcvrBMap;
        HashMap<Long, DebugRayOutput> droSrcIdMap;

        // get the model name map and see if it is not null

        droPhaseMap = aDebugRayOutputMap.get(modelName);
        if (droPhaseMap != null) {
            // get the phase name map and see if it is not null

            droRcvrAMap = droPhaseMap.get(phase);
            if (droRcvrAMap != null) {
                // get the receiver A map and see if it is not null

                droRcvrBMap = droRcvrAMap.get(rcvrA);
                if (droRcvrBMap != null) {
                    // get the receiver B map and see if it is not null
                    // if receiver B is null use receiver A as the key

                    if (rcvrB == null)
                        droSrcIdMap = droRcvrBMap.get(rcvrA);
                    else
                        droSrcIdMap = droRcvrBMap.get(rcvrB);
                    if (droSrcIdMap != null) {
                        // have at least a source id map ... return the srcid entry (which
                        // may be null)

                        return droSrcIdMap.get(srcId);
                    }
                }
            }
        }

        // one of the keys was not contained ... return null

        return null;
    }

    // *** Non-Static code ******************************************************

    /**
     * The model name of this debug ray.
     */
    private String aModelName = "";

    /**
     * The seismic phase of this debug ray.
     */
    private SeismicPhase aPhase = null;

    /**
     * The receiver A of this debug ray.
     */
    private Receiver aRcvrA = null;

    /**
     * The receiver B of this debug ray (may be null for type AA rays).
     */
    private Receiver aRcvrB = null;

    /**
     * The source of this debug ray.
     */
    private Source aSrc = null;

    /**
     * The source id of this debug ray.
     */
    private long aSrcId = -1;

    /**
     * The source to receiver distance (3D in km).
     */
    private double aSrcRcvrDist = 0.0;

    /**
     * The map of all tomography grid points associated with the
     * weight (the amount of influence) they have on receiver A's position.
     */
    private HashMap<Integer, Double> aRayWghtsRcvrA = null;

    /**
     * The map of all tomography grid points associated with the
     * weight (the amount of influence) they have on receiver B's position.
     * This map is null if aRcvrB is null.
     */
    private HashMap<Integer, Double> aRayWghtsRcvrB = null;

    /**
     * The map of all tomography grid points associated with the
     * weight (the amount of influence) they have on the source position.
     */
    private HashMap<Integer, Double> aRayWghtsSrc = null;

    /**
     * The total diagonal variance of this ray.
     */
    private double aDiagVar = 0.0;

    /**
     * The total off-diagonal variance of this ray.
     */
    private double aOffDiagVar = 0.0;

    /**
     * The total non-represented variance of this ray.
     */
    private double aNonRepVar = 0.0;

    /**
     * The maximum diagonal entry layer name length. Used to build the diagonal
     * table headers of the proper size.
     */
    private int aDiagMaxLyrNmLen = 0;

    /**
     * The maximum off-diagonal entry layer name length. Used to build the
     * off-diagonal table headers of the proper size.
     */
    private int aOffDiagMaxLyrNmLen = 0;

    /**
     * The maximum non-represented entry layer name length. Used to build the
     * non-represented table headers of the proper size.
     */
    private int aNonRepMaxLyrNmLen = 0;

    /**
     * List of only those DebugRayOutputEntry that contribute to the diagonal
     * covariance. This list is constructed during the sort operation.
     */
    ArrayList<DebugRayOutputEntry> aDiagList = null;

    /**
     * List of only those DebugRayOutputEntry that contribute to the off-diagonal
     * covariance. This list is constructed during the sort operation.
     */
    ArrayList<DebugRayOutputEntry> aOffDiagList = null;

    /**
     * List of only those DebugRayOutputEntry that contribute to the
     * non-represented covariance. This list is constructed during the sort
     * operation.
     */
    ArrayList<DebugRayOutputEntry> aNonRepList = null;

    /**
     * A map of all DebugRayOutputEntry associated with the grid point pair
     * indices that they are defined for.
     */
    private HashMap<Integer, HashMap<Integer, DebugRayOutputEntry>> aMap = null;

    /**
     * Default constructor. Not used.
     */
    private DebugRayOutput() {
        // no code
    }

    /**
     * Standard constructor for a type AB debug ray with a defined source.
     *
     * @param mdlName The model name of the ray.
     * @param ph      The phase of the ray.
     * @param rcvrA   The receiver A of the ray.
     * @param rcvrB   The receiver B of the ray.
     * @param src     The source of the ray.
     * @throws GMPException
     */
    private DebugRayOutput(String mdlName, SeismicPhase ph,
                           Receiver rcvrA, Receiver rcvrB, Source src)
            throws GMPException {
        // assign attributes and create map

        aModelName = mdlName;
        aPhase = ph;
        aRcvrA = rcvrA;
        aRcvrB = rcvrB;
        aSrc = src;
        aSrcId = aSrc.getSourceId();

        aMap = new HashMap<Integer, HashMap<Integer, DebugRayOutputEntry>>();

        // get source to receiver distance ... use a position half way between
        // receiver A and receiver B for the receiver position if both A and B
        // are defined

        aSrcRcvrDist = aRcvrA.getDistanceKm(aSrc);
        if (aRcvrB != null) {
            double dB = aRcvrB.getDistanceKm(aSrc);
            aSrcRcvrDist = 0.5 * (aSrcRcvrDist + dB);
        }
    }

    /**
     * Standard constructor for a type AA debug ray with a defined source.
     *
     * @param mdlName The model name of the ray.
     * @param ph      The phase of the ray.
     * @param rcvrA   The receiver of the ray.
     * @param src     The source of the ray.
     * @throws GMPException
     */
    private DebugRayOutput(String mdlName, SeismicPhase ph,
                           Receiver rcvrA, Source src) throws GMPException {
        // assign attributes and create map

        aModelName = mdlName;
        aPhase = ph;
        aRcvrA = rcvrA;
        aSrc = src;
        aSrcId = aSrc.getSourceId();

        aMap = new HashMap<Integer, HashMap<Integer, DebugRayOutputEntry>>();

        // get source to receiver distance

        aSrcRcvrDist = aRcvrA.getDistanceKm(aSrc);
    }

    /**
     * Standard constructor for a type AB debug ray with a known source id.
     *
     * @param mdlName The model name of the ray.
     * @param ph      The phase of the ray.
     * @param rcvrA   The receiver A of the ray.
     * @param rcvrB   The receiver B of the ray.
     * @param srcid   The source id of the ray.
     * @throws GMPException
     */
    private DebugRayOutput(String mdlName, SeismicPhase ph,
                           Receiver rcvrA, Receiver rcvrB, long srcid)
            throws GMPException {
        // assign attributes and create map

        aModelName = mdlName;
        aPhase = ph;
        aRcvrA = rcvrA;
        aRcvrB = rcvrB;
        aSrcId = srcid;

        aMap = new HashMap<Integer, HashMap<Integer, DebugRayOutputEntry>>();
    }

    /**
     * Standard constructor for a type AA debug ray with a known source id.
     *
     * @param mdlName The model name of the ray.
     * @param ph      The phase of the ray.
     * @param rcvrA   The receiver of the ray.
     * @param srcid   The source id of the ray.
     * @throws GMPException
     */
    private DebugRayOutput(String mdlName, SeismicPhase ph,
                           Receiver rcvrA, long srcid) throws GMPException {
        // assign attributes and create map

        aModelName = mdlName;
        aPhase = ph;
        aRcvrA = rcvrA;
        aSrcId = srcid;

        aMap = new HashMap<Integer, HashMap<Integer, DebugRayOutputEntry>>();
    }

    /**
     * Sets the source (when it becomes known). This function is used for debug
     * rays whose source id was known but had know source definition at the point
     * the debug ray was instantiated.
     *
     * @param src The debug rays source.
     * @throws GMPException
     */
    public void setSource(Source src) throws GMPException {
        // set the source and id

        aSrc = src;
        aSrcId = aSrc.getSourceId();

        // get source to receiver distance ... use a position half way between
        // receiver A and receiver B for the receiver position if both A and B
        // are defined

        aSrcRcvrDist = aRcvrA.getDistanceKm(aSrc);
        if (aRcvrB != null) {
            double dB = aRcvrB.getDistanceKm(aSrc);
            aSrcRcvrDist = 0.5 * (aSrcRcvrDist + dB);
        }
    }

    /**
     * Returns true if the source has been set.
     *
     * @return True if the source has been set.
     */
    public boolean isSourceSet() {
        return (aSrc != null);
    }

    /**
     * Sets the receiver A and B and the source influence weight maps. The
     * receiver B influence map may be null if aRcvrB is null.
     *
     * @param rwRcvrA The receiver A influence weight map.
     * @param rwRcvrB The receiver B influence weight map.
     * @param rwSrc   The source influence weight map.
     */
    public void setInfluenceWeights(HashMap<Integer, Double> rwRcvrA,
                                    HashMap<Integer, Double> rwRcvrB,
                                    HashMap<Integer, Double> rwSrc) {
        aRayWghtsRcvrA = new HashMap<Integer, Double>(rwRcvrA);
        if (aRcvrB != null)
            aRayWghtsRcvrB = new HashMap<Integer, Double>(rwRcvrB);
        aRayWghtsSrc = new HashMap<Integer, Double>(rwSrc);
    }

    /**
     * Adds a DebugTaskResults object containing the partial type AB ray
     * definitions for all debug rays accumulated from a single covariance matrix
     * block during the ray uncertainty calculation. Only those entries in the
     * DebugTaskResults object that define a ray that influences this debug rays
     * receivers or source are added. If none are found no addition occurs.
     *
     * @param mapAB The map of all type AB ray DebugTaskResults associated with
     *              the influence receivers and sources for which they were
     *              accumulated.
     * @throws IOException
     */
    public void add(DebugResultsPhaseSiteSiteMap mapAB) throws IOException {
        DebugTaskResults dtr;

        // exit if this debug ray is not a type AB

        if (!isTypeAB()) return;

        // loop over all receiver A influence receivers

        for (Map.Entry<Integer, Double> eRA : aRayWghtsRcvrA.entrySet()) {
            // get the influence receiver id (the tomography grid point) and loop
            // over all receiver B influence receivers

            long rcvrAXId = (long) eRA.getKey();
            for (Map.Entry<Integer, Double> eRB : aRayWghtsRcvrB.entrySet()) {
                // get the influence receiver id (the tomography grid point) and loop
                // over all source influence sources
                long rcvrBXId = (long) eRB.getKey();
                for (Map.Entry<Integer, Double> eS : aRayWghtsSrc.entrySet()) {
                    // get the source id and check the input AB map to see if it contains
                    // a source map for this rays phase the current influence receiver A
                    // and B ids

                    long srcXId = (long) eS.getKey();
                    DebugResultsSourceMap drsm = mapAB.get(aPhase, rcvrAXId, rcvrBXId);

                    if (drsm != null) {
                        // found a source map ... see if a specific entry for the current
                        // influence source id exists ... if it does then add the
                        // DebugTaskResults object to this DebugRayOutput object.

                        dtr = drsm.get(srcXId);
                        if (dtr != null)
                            add(true, eRA.getValue(), eRB.getValue(), eS.getValue(), dtr);
                    }
                }
            }
        }
    }

    /**
     * Adds a DebugTaskResults object containing the partial type AA ray
     * definitions for all debug rays accumulated from a single covariance matrix
     * block during the ray uncertainty calculation. Only those entries in the
     * DebugTaskResults object that define a ray that influences this debug rays
     * receivers or source are added. If none are found no addition occurs.
     *
     * @param mapAA The map of all type AA ray DebugTaskResults associated with
     *              the influence receivers and sources for which they were
     *              accumulated.
     * @throws IOException
     */
    public void add(DebugResultsPhaseSiteMap mapAA) throws IOException {
        DebugTaskResults dtr;

        // loop over all receiver A influence receivers

        for (Map.Entry<Integer, Double> eRA : aRayWghtsRcvrA.entrySet()) {
            // get the receiver id and check the input AA map to see if it contains
            // a source map for this rays phase the current influence receiver id

            long rcvrAXId = (long) eRA.getKey();
            DebugResultsSourceMap drsm = mapAA.get(aPhase, rcvrAXId);
            if (drsm != null) {
                // found a source map ... see if this debug ray is a type AB or type AA
                // ray

                if (isTypeAB()) {
                    // this is a type AB debug ray ... get the receiver B weight
                    // associated with the current receiver A id and see if it exists

                    Double RBW = aRayWghtsRcvrB.get((int) rcvrAXId);
                    if (RBW != null) {
                        // receiver B also contains the receiver A influence id (rcvrAXId)
                        // loop over all influence sources

                        for (Map.Entry<Integer, Double> eS : aRayWghtsSrc.entrySet()) {
                            // get the next influence source id and see if the source map
                            // contains an associated DebugTaskResults object. If it does then
                            // add the DebugTaskResults object to this DebugRayOutput object.

                            long srcXId = (long) eS.getKey();
                            dtr = drsm.get(srcXId);
                            if (dtr != null)
                                add(true, eRA.getValue(), RBW.doubleValue(), eS.getValue(), dtr);
                        }
                    }
                } else {
                    // this is a type AA debug ray ... loop over all influence sources

                    for (Map.Entry<Integer, Double> eS : aRayWghtsSrc.entrySet()) {
                        // get the next influence source id and see if the source map
                        // contains an associated DebugTaskResults object. If it does then
                        // add the DebugTaskResults object to this DebugRayOutput object.

                        long srcXId = (long) eS.getKey();
                        dtr = drsm.get(srcXId);
                        if (dtr != null) {
                            add(false, eRA.getValue(), 0.0, eS.getValue(), dtr);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds the contents of the input DebugTaskResults object to this
     * DebugRayOutput object.
     *
     * @param typeAB Boolean that is true if this is a type AB ray
     * @param iwra   The weight contribution of the influence receiver A.
     * @param iwrb   The weight contribution of the influence receiver B.
     * @param iws    The weight contribution of the influence source.
     * @param dtr    The input DebugTaskResults object.
     * @throws IOException
     */
    private void add(boolean typeAB, double iwra, double iwrb, double iws,
                     DebugTaskResults dtr)
            throws IOException {
        HashMap<Integer, DebugRayOutputEntry> droeMap;
        DebugRayOutputEntry droe;

        // Throw an error if the source was never defined.

        if (aSrc == null) {
            throw new IOException("DebugRayOutput Source Not Set ...");
        }

        // loop over all entries in the input DebugTaskResults object

        for (int i = 0; i < dtr.size(); ++i) {
            // get the next DebugTaskResultsEntry and retrieve its point pair indices
            // and the matrix covariance value

            DebugTaskResultsEntry dtre = dtr.get(i);
            int p1 = dtre.getRowMatrixIndex();
            int p2 = dtre.getColMatrixIndex();
            double cov = dtre.getCovariance();

            // see if the map contains point p2

            droeMap = aMap.get(p2);
            if (droeMap == null) {
                // map does not contain p2 ... see if it contains p1 ... if not create
                // a new sub-map and add it to the map associated with p1

                droeMap = aMap.get(p1);
                if (droeMap == null) {

                    droeMap = new HashMap<Integer, DebugRayOutputEntry>();
                    aMap.put(p1, droeMap);
                }

                // see if the sub-map contains a DebugRayOutputEntry object associated
                // with p2 ... if not create a new one and add it into the sub-map

                droe = droeMap.get(p2);
                if (droe == null) {
                    droe = new DebugRayOutputEntry(p1, p2, cov, aSrc, aSrcRcvrDist);
                    droeMap.put(p2, droe);
                }
            } else {
                // map contains an entry for p2 ... see if that sub-map contains a
                // DebugRayOutputEntry object associated with p1 .. if not create a
                // new one and add into the sub-map

                droe = droeMap.get(p1);
                if (droe == null) {
                    droe = new DebugRayOutputEntry(p1, p2, cov, aSrc, aSrcRcvrDist);
                    droeMap.put(p1, droe);
                }
            }

            // have the DebugRayOutputEntry object associated with p1 and p2 ...
            // add a new entry depending on if it is a type AB or type AA ray entry

            if (!typeAB) {
                // add a type AA entry

                droe.addEntry(dtr.getReceiverAId(), dtr.getSourceId(), iwra, iws,
                        dtre.getRowWeight(), dtre.getColWeight());
            } else {
                // add a type AA entry ... use the A receiver id for the B receiver id
                // if the B receiver is not defined.

                long bid = dtr.getReceiverBId();
                if (bid == -1) bid = dtr.getReceiverAId();
                droe.addEntry(dtr.getReceiverAId(), bid,
                        dtr.getSourceId(), iwra, iwrb, iws,
                        dtre.getRowWeight(), dtre.getColWeight());
            }
        }
    }

    /**
     * Returns true if this is a typeAB debug ray.
     *
     * @return True if this is a typeAB debug ray.
     */
    public boolean isTypeAB() {
        return ((aRcvrB != null) && (aRcvrB != aRcvrA));
    }

    /**
     * Returns the assembled file name of this debug ray output object.
     *
     * @return The assembled file name of this debug ray output object.
     */
    public String getFileName() {
        String fn = "debugRay_" + aPhase.getFileName() + "_" +
                aRcvrA.getSta() + "_";
        if ((aRcvrB != null) && (aRcvrA != aRcvrB))
            fn += aRcvrB.getSta() + "_";
        fn += aSrc.getSourceId() + ".txt";

        return fn;
    }

    /**
     * Called when this DebugRayOutput object has been completely populated with
     * all DebugTaskResults entries. This function splits the entries between the
     * three components (diagonal, off-diagonal, and non-represented) and sorts
     * each set in descending order for output. Each entry is then added to its
     * specific list in the sorted order for ease of output.
     */
    public void sort() {
        ArrayList<DebugRayOutputEntry> droeList;

        // get total number of entries

        int cnt = 0;
        for (Map.Entry<Integer, HashMap<Integer, DebugRayOutputEntry>> e :
                aMap.entrySet())
            cnt += e.getValue().size();

        // add all entries to a list

        droeList = new ArrayList<DebugRayOutputEntry>(cnt);
        for (Map.Entry<Integer, HashMap<Integer, DebugRayOutputEntry>> e :
                aMap.entrySet()) {
            HashMap<Integer, DebugRayOutputEntry> p1Map = e.getValue();
            for (Map.Entry<Integer, DebugRayOutputEntry> ep1 : p1Map.entrySet())
                droeList.add(ep1.getValue());
        }

        // determine number of non-represented, diagonal, and off-diagonal entries

        int nonrepcnt, diagcnt, offdiagcnt;
        nonrepcnt = diagcnt = offdiagcnt = 0;
        for (DebugRayOutputEntry d : droeList) {
            if (d.isNonRepresented(aCovMtrxSize))
                ++nonrepcnt;
            else if (d.isDiagonal())
                ++diagcnt;
            else
                ++offdiagcnt;
        }

        // create index and weighted covariance arrays for each component

        int[] aiNR = new int[nonrepcnt];
        double[] biNR = new double[nonrepcnt];

        int[] aiD = new int[diagcnt];
        double[] biD = new double[diagcnt];

        int[] aiOD = new int[offdiagcnt];
        double[] biOD = new double[offdiagcnt];

        // loop over all entries in the list again and fill the index and
        // weighted covariance arrays

        aNonRepVar = aDiagVar = aOffDiagVar = 0.0;
        aDiagMaxLyrNmLen = aOffDiagMaxLyrNmLen = aNonRepMaxLyrNmLen = 0;
        nonrepcnt = diagcnt = offdiagcnt = 0;
        for (int i = 0; i < droeList.size(); ++i) {
            DebugRayOutputEntry d = droeList.get(i);
            d.sort();
            if (d.isNonRepresented(aCovMtrxSize)) {
                aiNR[nonrepcnt] = i;
                biNR[nonrepcnt++] = d.getWeightedCovariance();
                aNonRepVar += d.getWeightedCovariance();
                if (aNonRepMaxLyrNmLen < d.getLayerName(1).length())
                    aNonRepMaxLyrNmLen = d.getLayerName(1).length();
            } else if (d.isDiagonal()) {
                aiD[diagcnt] = i;
                biD[diagcnt++] = d.getWeightedCovariance();
                aDiagVar += d.getWeightedCovariance();
                if (aDiagMaxLyrNmLen < d.getLayerName(1).length())
                    aDiagMaxLyrNmLen = d.getLayerName(1).length();
            } else {
                aiOD[offdiagcnt] = i;
                biOD[offdiagcnt++] = Math.abs(d.getWeightedCovariance());
                aOffDiagVar += d.getWeightedCovariance();
                if (aOffDiagMaxLyrNmLen < d.getLayerName(1).length())
                    aOffDiagMaxLyrNmLen = d.getLayerName(1).length();
            }
        }

        // sort each component and flip index array to obtain decending order

        IntrinsicSort.sort(biNR, aiNR);
        IntrinsicSort.sort(biD, aiD);
        IntrinsicSort.sort(biOD, aiOD);
        IntrinsicSort.reverseOrder(aiNR);
        IntrinsicSort.reverseOrder(aiD);
        IntrinsicSort.reverseOrder(aiOD);

        // make lists for each component type and exit

        aNonRepList = new ArrayList<DebugRayOutputEntry>(nonrepcnt);
        for (int i = 0; i < nonrepcnt; ++i) aNonRepList.add(droeList.get(aiNR[i]));
        aDiagList = new ArrayList<DebugRayOutputEntry>(diagcnt);
        for (int i = 0; i < diagcnt; ++i) aDiagList.add(droeList.get(aiD[i]));
        aOffDiagList = new ArrayList<DebugRayOutputEntry>(offdiagcnt);
        for (int i = 0; i < offdiagcnt; ++i) aOffDiagList.add(droeList.get(aiOD[i]));
    }

    /**
     * Primary function to dump this debug ray. The function sort() must be
     * called after all entries have been added but before calling this function.
     *
     * @param scrnWriter The screen writer into which this output is dumped.
     */
    public void output(ScreenWriterOutput scrnWriter) {
        String s;

        // write header start and model name and phase

        scrnWriter.write(NL);
        s = "    " + Globals.repeat("*", 136) + NL;
        s += "    Debug Ray" + NL;
        s += "      Model Name               = " + aModelName + NL;
        s += "      Phase                    = " + aPhase.name() + NL;

        // write receiver info

        if ((aRcvrB == null) || (aRcvrA == aRcvrB)) {
            s += "      Receiver                 = " + aRcvrA.getSta() +
                    " <" + aRcvrA.geovectorToString() + ">" + NL;
        } else {
            s += "      Receiver A               = " + aRcvrA.getSta() +
                    " <" + aRcvrA.geovectorToString() + ">" + NL;
            s += "      Receiver B               = " + aRcvrB.getSta() +
                    " <" + aRcvrB.geovectorToString() + ">" + NL;
        }

        // write source info

        s += "      Source                   = Id: " + aSrc.getSourceId() +
                ", Position: <" + aSrc.toString() + ">";
        if (aSrc.getEvid() != -1)
            s += ", EVID: " + aSrc.getEvid();
        s += NL + NL;

        // write source to receiver distance, the total variance, component
        // variances, and the represented variance fraction

        s += "      S->R Distance (3D) (km)  = " +
                aSrcRcvrDist + NL;
        double tvar = aDiagVar + aOffDiagVar + aNonRepVar;
        s += "      Total Variance           = " + tvar + NL;
        s += "      Diagonal Variance        = " + aDiagVar + NL;
        s += "      Off-Diagonal Variance    = " + aOffDiagVar + NL;
        s += "      Non-Represented Variance = " + aNonRepVar + NL;
        double rfrc = (aDiagVar + aOffDiagVar) / tvar;
        s += "      Represented Fraction     = " + rfrc + NL;

        scrnWriter.write(s);

        // write out the receiver and source influence weight maps

        boolean typeAB = ((aRcvrB != null) && (aRcvrA != aRcvrB));
        if (typeAB) {
            outputInfluenceWeights(scrnWriter, "Receiver", " A", aRayWghtsRcvrA);
            outputInfluenceWeights(scrnWriter, "Receiver", " B", aRayWghtsRcvrB);
        } else
            outputInfluenceWeights(scrnWriter, "Receiver", "", aRayWghtsRcvrA);
        outputInfluenceWeights(scrnWriter, "Source", "", aRayWghtsSrc);

        // write out the diagonal variance results an their associated ray weight
        // components

        outputDiagVarPnts(scrnWriter, "Diagonal", aDiagList, aDiagMaxLyrNmLen,
                aDiagVar);
        outputDiagRayWghtCmpnts(scrnWriter, "Diagonal", typeAB, aDiagList);

        // write out the off-diagonal covariance results an their associated ray
        // weight components

        outputOffDiagVarPnts(scrnWriter, "Off-Diagonal", aOffDiagList,
                aOffDiagMaxLyrNmLen);
        outputOffDiagRayWghtCmpnts(scrnWriter, "Off-Diagonal", typeAB, aOffDiagList);

        // write out the non-represented variance results an their associated ray
        // weight components

        outputDiagVarPnts(scrnWriter, "Non-Represented", aNonRepList,
                aNonRepMaxLyrNmLen, aNonRepVar);
        outputDiagRayWghtCmpnts(scrnWriter, "Non-Represented", typeAB, aNonRepList);

        // done finish and exit

        s = "    " + Globals.repeat("*", 136) + NL + NL;
        scrnWriter.write(s);
    }

    /**
     * Outputs all influence ray weights in a tabular fashion
     *
     * @param scrnWriter The screen writer into which the table is written.
     * @param hdrName    The header name.
     * @param hdrExtra   Extra header information.
     * @param inflMap    The influence weight map to be output.
     */
    private void outputInfluenceWeights(ScreenWriterOutput scrnWriter,
                                        String hdrName, String hdrExtra,
                                        HashMap<Integer, Double> inflMap) {
        String s;
        String hdr = "        ";

        // assemble the header and output

        scrnWriter.write(NL);
        scrnWriter.write("      " + hdrName + hdrExtra + " Influence Weights" + NL);
        s = Globals.repeat("_", 39);
        scrnWriter.write("      " + s + NL + NL);

        s = hdr + " " + hdrName + NL;
        s += hdr + "  Point     Influence" + NL;
        s += hdr + "  Index      Weight" + NL;
        s += hdr + Globals.repeat(".", 37) + NL;
        scrnWriter.write(s);

        // make the format and loop over all table entries and write them to the
        // screen writer

        s = "";
        String fmt = "%8d    %9.3e";
        for (Map.Entry<Integer, Double> e : inflMap.entrySet())
            s += "        " + String.format(fmt, e.getKey(), e.getValue()) + NL;

        s += "      " + Globals.repeat("_", 39);
        scrnWriter.write(s + NL + NL);
    }

    /**
     * Outputs diagonal variance list entries.
     *
     * @param scrnWriter    The screen writer into which the table is written.
     * @param hdrName       The header name.
     * @param droeList      The list of diagonal DebugRayOutputEntry objects to be
     *                      output in descending sorted order.
     * @param maxNameLength The maximum layer name length contained in the list.
     * @param diagVar       The total diagonal variance.
     */
    private void outputDiagVarPnts(ScreenWriterOutput scrnWriter,
                                   String hdrName,
                                   ArrayList<DebugRayOutputEntry> droeList,
                                   int maxNameLength, double diagVar) {
        // exit if no entries

        String s;
        String hdr = "        ";
        if (droeList.size() == 0) return;

        // build the layer name header extensions and format

        int ln = maxNameLength - 5;
        String[] lyrNam = getLayerName(maxNameLength);

        // output the header

        scrnWriter.write(NL + "      " + hdrName + " Variance Points" + NL);
        s = Globals.repeat("_", 116 + ln);
        scrnWriter.write("      " + s + NL + NL);

        s = hdr + "          Matrix     Grid    " + lyrNam[0] + "   " +
                "Src->Pnt                                                            " +
                "W*Cov" + NL;
        s += hdr + "  Entry   Column     Point   " + lyrNam[1] + "   " +
                "Distance                                       W*Cov      W*Cov    " +
                "Frctn (%)" + NL;
        s += hdr + "  Index   Index      Index   " + lyrNam[2] + "   Frctn (%)    " +
                "Weight   Covariance    W*Cov    Frctn (%)     Sum       Sum" + NL;
        s += hdr + Globals.repeat(".", 114 + ln) + NL;
        scrnWriter.write(s);

        // make the output format

        String fmt = "           %4d  %8d  %8d  " + lyrNam[3] + "   %6.2f     " +
                "%9.3e  %9.3e   %9.3e   %6.2f    %9.3e   %6.2f";

        // loop over all entries in the list and output each entry

        double wsum = 0.0;
        for (int i = 0; i < droeList.size(); ++i) {
            // get the next entry and sum its weighted covariance ... output the entry

            DebugRayOutputEntry droe = droeList.get(i);
            wsum += droe.getWeightedCovariance();
            s = String.format(fmt, i, droe.getMatrixColumnIndex(1),
                    droe.getGridPointIndex(1), droe.getLayerName(1),
                    droe.getSourceToPointDistFraction(1),
                    droe.getWeight(), droe.getCovariance(),
                    droe.getWeightedCovariance(),
                    100.0 * droe.getWeightedCovariance() / diagVar,
                    wsum, 100.0 * wsum / diagVar) + NL;
            scrnWriter.write(s);
        }

        // done ... finish the output and exit

        s = Globals.repeat("_", 116 + ln);
        scrnWriter.write("      " + s + NL + NL);
    }

    /**
     * Outputs off-diagonal variance list entries.
     *
     * @param scrnWriter    The screen writer into which the table is written.
     * @param hdrName       The header name.
     * @param droeList      The list of off-diagonal DebugRayOutputEntry objects to
     *                      be output in descending sorted order.
     * @param maxNameLength The maximum layer name length contained in the list.
     */
    private void outputOffDiagVarPnts(ScreenWriterOutput scrnWriter,
                                      String hdrName,
                                      ArrayList<DebugRayOutputEntry> droeList,
                                      int maxNameLength) {
        // exit if no entries

        String s;
        String hdr = "        ";
        if (droeList.size() == 0) return;

        // build the layer name header extensions and format

        int ln = maxNameLength - 5;
        String[] lyrNam = getLayerName(maxNameLength);

        // output the header

        scrnWriter.write(NL);
        scrnWriter.write("      " + hdrName + " Covariance Point Pairs" + NL);
        s = Globals.repeat("_", 135 + ln);
        scrnWriter.write("      " + s + NL + NL);
        s = hdr + "                Matrix     Grid    " + lyrNam[0] + "   Src->Pnt   " +
                "Pnt Sep.                                                              " +
                "W*Cov" + NL;
        s += hdr + "  Entry         Column     Point   " + lyrNam[1] + "   Distance   " +
                "Distance                                        W*Cov       " +
                "W*Cov    Frctn (%)" + NL;
        s += hdr + "  Index  Point  Index      Index   " + lyrNam[2] + "   Frctn (%)  " +
                "Frctn (%)   Weight   Covariance      W*Cov    Frctn (%)      " +
                "Sum       Sum" + NL;
        s += hdr + Globals.repeat(".", 133 + ln) + NL;
        scrnWriter.write(s);

        // make the output format

        String fmt1 = "          %5d    1   %8d  %8d  " + lyrNam[3] +
                "   %6.2f     %6.2f    %9.3e  %10.3e   %10.3e   %6.2f    " +
                "%10.3e   %6.2f";
        String fmt2 = "                   2   %8d  %8d  " + lyrNam[3] + "   %6.2f";

        // loop over all entries in the list and output each entry

        double wsum = 0.0;
        for (int i = 0; i < droeList.size(); ++i) {
            // get the next entry and sum its weighted covariance ... output the entry

            DebugRayOutputEntry droe = droeList.get(i);
            wsum += droe.getWeightedCovariance();
            s = String.format(fmt1, i, droe.getMatrixColumnIndex(1),
                    droe.getGridPointIndex(1), droe.getLayerName(1),
                    droe.getSourceToPointDistFraction(1),
                    droe.getPointSeparationDistFraction(),
                    droe.getWeight(), droe.getCovariance(),
                    droe.getWeightedCovariance(),
                    100.0 * Math.abs(droe.getWeightedCovariance() / aOffDiagVar),
                    wsum, 100.0 * Math.abs(wsum / aOffDiagVar)) + NL;
            s += String.format(fmt2, droe.getMatrixColumnIndex(2),
                    droe.getGridPointIndex(2), droe.getLayerName(2),
                    droe.getSourceToPointDistFraction(2)) + NL;
            scrnWriter.write(s);
        }

        // done ... finish the output and exit

        s = Globals.repeat("_", 135 + ln);
        scrnWriter.write("      " + s + NL + NL);
    }

    /**
     * Output the diagonal ray weight components.
     *
     * @param scrnWriter The screen writer into which the table is written.
     * @param hdrName    The header name.
     * @param typeAB     True if ray type is AB
     * @param droeList   The list of diagonal DebugRayOutputEntry objects to
     *                   be output in descending sorted order.
     */
    private void outputDiagRayWghtCmpnts(ScreenWriterOutput scrnWriter,
                                         String hdrName, boolean typeAB,
                                         ArrayList<DebugRayOutputEntry> droeList) {
        // exit if no entries

        String s;
        String hdr = "        ";
        String hdrsep;
        if (droeList.size() == 0) return;

        // output the header depending on ray type

        scrnWriter.write(NL + "      " + hdrName + " Ray Weight Components" + NL);
        if (typeAB) {
            // output type AB ray header

            s = Globals.repeat("_", 108);
            scrnWriter.write("      " + s + NL);
            s = hdr + "(Note: Total Weight = (Influence Receiver A Weight) * " +
                    "(Influence Receiver B Weight) *" + NL + hdr +
                    "                      (Influence Source Weight) * " +
                    "(Point Ray Weight)^2" + NL + NL;
            scrnWriter.write(s + NL);

            s = hdr + "                   Influence   Influence   Influence" + NL;
            s += hdr + "          Grid     Receiver A  Receiver B   Source     " +
                    "Point                Total       Total    Totl Wght" + NL;
            s += hdr + "  Entry   Point      Point       Point       Point      " +
                    "Ray       Total     Weight      Weight   Frctn (%)" + NL;
            s += hdr + "  Index   Index      Index       Index       Index     " +
                    "Weight     Weight   Frctn (%)     Sum        Sum" + NL;
            s += hdr + Globals.repeat(".", 106) + NL;
            hdrsep = Globals.repeat(" ", 28) + Globals.repeat("=", 87) + NL;
        } else {
            // output type AA ray header

            s = Globals.repeat("_", 96);
            scrnWriter.write("      " + s + NL);
            s = hdr + "(Note: Total Weight = (Influence Receiver Weight) * " +
                    "(Influence Source Weight) ) *" + NL + hdr +
                    "                      (Point Ray Weight) )^2" + NL + NL;
            scrnWriter.write(s + NL);

            s = hdr + "                   Influence   Influence" + NL;
            s += hdr + "          Grid     Receiver     Source     " +
                    "Point                Total       Total    Totl Wght" + NL;
            s += hdr + "  Entry   Point      Point       Point      " +
                    "Ray       Total     Weight      Weight   Frctn (%)" + NL;
            s += hdr + "  Index   Index      Index       Index     " +
                    "Weight     Weight   Frctn (%)     Sum        Sum" + NL;
            s += hdr + Globals.repeat(".", 94) + NL;
            hdrsep = Globals.repeat(" ", 28) + Globals.repeat("=", 75) + NL;
        }
        scrnWriter.write(s);

        // loop over all entries in the list and output each

        for (int i = 0; i < droeList.size(); ++i) {
            if (i > 0) scrnWriter.write(hdrsep);

            DebugRayOutputEntry droe = droeList.get(i);
            s = droe.outputRayWghtCmpnts(typeAB, i);
            scrnWriter.write(s);
        }

        // finish output and exit

        if (typeAB)
            s = Globals.repeat("_", 108);
        else
            s = Globals.repeat("_", 96);

        scrnWriter.write("      " + s + NL + NL);
    }

    /**
     * Output the off-diagonal ray weight components.
     *
     * @param scrnWriter The screen writer into which the table is written.
     * @param hdrName    The header name.
     * @param typeAB     True if ray type is AB
     * @param droeList   The list of off-diagonal DebugRayOutputEntry objects to
     *                   be output in descending sorted order.
     */
    private void outputOffDiagRayWghtCmpnts(ScreenWriterOutput scrnWriter,
                                            String hdrName, boolean typeAB,
                                            ArrayList<DebugRayOutputEntry> droeList) {
        // exit if no entries

        String s;
        String hdr = "        ";
        String hdrsep;
        if (droeList.size() == 0) return;

        // output the header depending on ray type

        scrnWriter.write(NL + "      " + hdrName + " Ray Weight Components" + NL);
        if (typeAB) {
            // output type AB ray header

            s = Globals.repeat("_", 130);
            scrnWriter.write("      " + s + NL);
            s = hdr + "(Note: Total Weight = (Influence Receiver A Weight) * " +
                    "(Influence Receiver B Weight) *" + NL + hdr +
                    "                      (Influence Source Weight) * " +
                    "(Point 1 Ray Weight) * (Point 2 Ray Weight)" + NL + NL;

            s += hdr +
                    "                             Influence   Influence   Influence" + NL;
            s += hdr + "           Grid      Grid    Receiver A  Receiver B   " +
                    "Source     Point 1    Point 2              Total       Total    " +
                    "Totl Wght" + NL;
            s += hdr + "  Entry   Point 1   Point 2    Point       Point       " +
                    "Point       Ray        Ray      Total     Weight      Weight   " +
                    "Frctn (%)" + NL;
            s += hdr + "  Index    Index     Index     Index       Index       " +
                    "Index     Weight     Weight     Weight   Frctn (%)     " +
                    "Sum        Sum" + NL;
            s += hdr + Globals.repeat(".", 128) + NL;
            hdrsep = Globals.repeat(" ", 38) + Globals.repeat("=", 97) + NL;
        } else {
            // output type AA ray header

            s = Globals.repeat("_", 118);
            scrnWriter.write("      " + s + NL);
            s = hdr + "(Note: Total Weight = 2 * (Influence Receiver Weight) * " +
                    "(Influence Source Weight) *" + NL + hdr +
                    "                      (Point 1 Ray Weight) * " +
                    "(Point 2 Ray Weight)" + NL + NL;

            s += hdr +
                    "                             Influence   Influence" + NL;
            s += hdr + "           Grid      Grid    Receiver     " +
                    "Source     Point 1    Point 2              Total       Total    " +
                    "Totl Wght" + NL;
            s += hdr + "  Entry   Point 1   Point 2    Point       " +
                    "Point       Ray        Ray      Total     Weight      Weight   " +
                    "Frctn (%)" + NL;
            s += hdr + "  Index    Index     Index     Index       " +
                    "Index     Weight     Weight     Weight   Frctn (%)     " +
                    "Sum        Sum" + NL;
            s += hdr + Globals.repeat(".", 116) + NL;
            hdrsep = Globals.repeat(" ", 38) + Globals.repeat("=", 85) + NL;
        }
        scrnWriter.write(s);

        // loop over all entries in the list and output each

        for (int i = 0; i < droeList.size(); ++i) {
            if (i > 0) scrnWriter.write(hdrsep);

            DebugRayOutputEntry droe = droeList.get(i);
            s = droe.outputRayWghtCmpnts(typeAB, i);
            scrnWriter.write(s);
        }

        // finish output and exit

        if (typeAB)
            s = Globals.repeat("_", 130);
        else
            s = Globals.repeat("_", 118);

        scrnWriter.write("      " + s + NL + NL);
    }

    /**
     * Builds and returns a variable length header given the maximum layer name
     * length.
     *
     * @param maxLayerNameLength The maximum length of any layer name used in the
     *                           layer name column.
     * @return A variable length header given the maximum layer name
     * length.
     */
    private String[] getLayerName(int maxLayerNameLength) {
        // build the header row entries for the layer name for the case where the
        // name length is 5 or fewer characters (Note element [3] is the format).

        String[] lyrNames = new String[4];
        lyrNames[0] = "     ";
        lyrNames[1] = "Layer";
        lyrNames[2] = "Name ";
        lyrNames[3] = "%5s";

        // adjust the baseline entries if the input maximum layer name length
        // exceeds 5

        if (maxLayerNameLength > 5) {
            // substract off the initial length (5) - 1 (i.e. 4) and make before and
            // after "space" strings.

            int i = maxLayerNameLength - 4;
            String before = Globals.repeat(" ", i / 2);
            String after = Globals.repeat(" ", i - 1 - (i / 2));

            // add before and after strings and change format number to the input
            // value

            lyrNames[0] = before + lyrNames[0] + after;
            lyrNames[1] = before + lyrNames[1] + after;
            lyrNames[2] = before + lyrNames[2] + after;
            lyrNames[3] = "%" + maxLayerNameLength + "s";
        }

        // return layer names header array

        return lyrNames;
    }
}
