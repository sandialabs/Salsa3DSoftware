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
package gov.sandia.gmp.rayuncertainty.smoothing;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.ModelInterface;
import gov.sandia.gmp.geotessgmp.GeoTessModelGMP;
import gov.sandia.gmp.geotessgmp.LibCorr3DModelGMP;
import gov.sandia.gmp.rayuncertainty.RayUncertainty;
import gov.sandia.gmp.rayuncertainty.RayUncertainty.SourceDefinition;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerDouble;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerInteger;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerKey;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.statistics.Statistic;

/**
 * This class performs smoothing actions on the three primary components of the
 * ray uncertainty (variance actually) result and stores the results back into
 * the defined source model. The 3 primary components are defined as
 * <p>
 * DIAGONAL         associated with GeoAttribute TT_MODEL_VARIANCE_DIAGONAL
 * OFF_DIAGONAL     associated with GeoAttribute TT_MODEL_VARIANCE_OFFDIAGONAL
 * NON_REPRESENTED  associated with GeoAttribute TT_MODEL_VARIANCE_NONREPRESENTED
 * <p>
 * One or more of these components can be smoothed. If any are smoothed they are
 * stored in the following GeoAttribute associations
 * <p>
 * DIAGONAL        = TT_MODEL_VARIANCE_DIAGONAL_SMOOTHED
 * OFF_DIAGONAL    = TT_MODEL_VARIANCE_OFFDIAGONAL_SMOOTHED
 * NON_REPRESENTED = TT_MODEL_VARIANCE_NONREPRESENTED_SMOOTHED
 * <p>
 * In addition, the total ray variance is stored in the GeoAttribute
 * <p>
 * TT_MODEL_VARIANCE
 * <p>
 * and its equivalent smoothed result in
 * <p>
 * TT_MODEL_VARIANCE_SMOOTHED
 * <p>
 * The total smoothed component is comprised of any smoothed components + the
 * original component value if it were not smoothed.
 * <p>
 * Two types of smoothing are available and any component can use any or none
 * of them. The two types include
 * <p>
 * RING       Ring Neighbor Smoothing (RNS), and
 * DISTANCE   Distance Neighbor Smoothing (DNS)
 * <p>
 * RNS finds the points on the tomography model that influence a point on the
 * source model that is to be smoothed. Next, the ring neighbors of the influence
 * points are discovered on the tomography grid. These points are all interpolated
 * on the source model and the results are then averaged for each requested ring.
 * These averaged results are then weighted by the specified ring weights to
 * arrive at a single value (for each unique surface defined on the source model)
 * for one of the influence points. These are then weighted by the influence
 * weights to give the final smoothed result on the source model.
 * <p>
 * DNS finds the points on the source model that influence a point to be
 * smoothed. These points are then weighted based on their distance from the
 * point to be smoothed using two distances defined on input. The first, D0,
 * defines a weight of 1.0 for all points that have a distance equal or less
 * than D0. The second, D1, defines a weight taken from a normalized unit
 * quartic function that is 1.0 at D0 and 0.0 at D1. D0 must be between 0.0 and
 * D1 (inclusive). All neighbor results are summed with their distance weight
 * and then normalized with the sum of the weights alone to arrive at the
 * smoothed result for each source point.
 * <p>
 * When smoothing completes the input source model is rewritten as a new
 * model with the smoothing parameters written as part of the model description.
 * <p>
 * To smooth a set of input models
 * 1) Create a Smoother() object.
 * 2) Call setSmoothingDefinitions(UncertaintyComponent, SmoothingType, Weights)
 * where Weights are the definition for the requested type and component.
 * Call for each desired component. A component can only be smoothed one
 * way (RING or DISTANCE) calling it for both throws an error.
 * 3) Call smoothModel(tomoModel, srcModelIn, srcModelOut, sourceType) for
 * each consecutive model.
 *
 * @author jrhipp
 */
public class Smoother {
    /**
     * Converts degrees to km and back (doesn't need to be accurate)
     */
    private static final double KM_PER_DEG = 111.0;

    /**
     * Enum for the two types of supported smoothing.
     *
     * @author jrhipp
     */
    public enum SmoothingType {
        RING,
        DISTANCE;
    }

    /**
     * Enum of the three components that can be smoothed.
     *
     * @author jrhipp
     */
    public enum UncertaintyComponent {
        DIAGONAL,
        OFF_DIAGONAL,
        NON_REPRESENTED;
    }

    /**
     * Used in GeoAttribute indexes that are assigned to specific phase/receiver/
     * receiver entries in an AttributeIndexer.
     * <p>
     * This object holds the seismic phase, receiver A, and receiver B objects
     * that define a surface entry and a map of all defined GeoAttributes
     * associated with their attribute index value in an AttributeIndexer object.
     *
     * @author jrhipp
     */
    public class SiteAttributeMap {
        public SeismicPhase aPhase = null;
        public Receiver aRcvrA = null;
        public Receiver aRcvrB = null;
        public HashMap<GeoAttributes, Integer> aAttrbMap = null;

        public SiteAttributeMap(SeismicPhase sp, Receiver rA, Receiver rB) {
            aPhase = sp;
            aRcvrA = rA;
            aRcvrB = rB;
            aAttrbMap = new HashMap<GeoAttributes, Integer>();
        }

        public Integer getAttributeIndex(GeoAttributes ga) {
            return aAttrbMap.get(ga);
        }

        public void putAttributeIndex(GeoAttributes ga, int index) {
            aAttrbMap.put(ga, index);
        }
    }

    /**
     * True if smoothing is performed for the respective component.
     */
    private boolean[] aSmoothingFlag = {false, false, false};

    /**
     * The smoothing description for each component (Contains "" if the
     * component is not smoothed).
     */
    private String[] aSmoothingDescr = {"", "", ""};

    /**
     * The map of ring weight smoothing parameters for each requested component.
     */
    private HashMap<Integer, double[]> aRingWeights = new HashMap<Integer, double[]>();

    /**
     * The map of distance weight smoothing parameters for each requested component.
     */
    private HashMap<Integer, double[]> aDistWeights = new HashMap<Integer, double[]>();

    /**
     * Used to perform interpolations on models that are GeoTessModel objects.
     */
    private GeoTessPosition aGeoTessPosition = null;

    /**
     * The statistic object containing results from the difference of the
     * original point - the smoothed result. One for each commponent can be
     * saved.
     */
    private Statistic[] aSmoothResidual = new Statistic[3];

    /**
     * The statistic object containing results for the total number of
     * neighbors that influenced a smoothing point.
     */
    private Statistic aSmoothNeighbors = new Statistic();

    /**
     * The statistic (one for each component if smoothed) of distanced bins
     * (if DNS the four bins are <D0/2, <D0, <(D0+D1)/2, and <D1. If RNS the
     * bins correspond to the average distance of neighbors in a neighbor
     * ring).
     */
    private Statistic[][] aDistNeighbors = new Statistic[3][];

    /**
     * The statistic (one for each component if smoothed) of neighbor count bins
     * (if DNS the four bins are <D0/2, <D0, <(D0+D1)/2, and <D1. If RNS the
     * bins correspond to the average distance of neighbors in a neighbor
     * ring).
     */
    private Statistic[][] aRingNeighbors = new Statistic[3][];

    /**
     * Used by DNS only to store the DNS distance neighbor bin limits. These are
     * defined as <D0/2, <D0, <(D0+D1)/2, and <D1.
     */
    private double[][] aDistNghbrLimits = new double[3][];

    /**
     * Default constructor.
     */
    public Smoother() {
        // no code
    }

    /**
     * The function called by a client to perform smoothing operations.
     * This function will smooth requested components of the the input
     * source model (fin) and writes the resulting smoothed output model to
     * the file fout.
     *
     * @param finTomo   The input tomographic model file path.
     * @param fin       The input source model file path (the model to be
     *                  smoothed).
     * @param fout      The smoothed model output file path.
     * @param modelType The model type (GEOMODEL or GEOTESSMODEL).
     * @throws IOException
     * @throws GMPException
     * @throws GeoTessException
     */
    public void smoothModel(String finTomo, String fin, String fout,
                            SourceDefinition modelType)
            throws IOException, GMPException, GeoTessException {
        String s;

        // start

        System.out.println("RayUncertainty.smoothModel Start");
        System.out.println("  Start Time = " + Globals.getTimeStamp());
        long strtTime = (new Date()).getTime();

        // output smoothing message

        String smoothingDescr = "";

        // exit if no smoothing was requested

        if (!aSmoothingFlag[0] && !aSmoothingFlag[1] && !aSmoothingFlag[2]) {
            System.out.println("  No Smoothing was Requested ...");
            System.out.println("Exiting ...");
            return;
        } else {
            System.out.println(NL + "  Smoothing Travel Time Variance with ... ");
            if (!aSmoothingDescr[0].equals(""))
                System.out.print(aSmoothingDescr[0]);
            if (!aSmoothingDescr[1].equals(""))
                System.out.print(aSmoothingDescr[1]);
            if (!aSmoothingDescr[2].equals(""))
                System.out.print(aSmoothingDescr[2]);
            System.out.println("");
        }

        // done setting smoothing flag ... now load Models twice. Use mdl0 to
        // find current attributes settings and as a container of the current
        // set of data that will be copied to mdl1. The second model (mdl1) will
        // be redefined with a new attribute indexer set to the new set of
        // smoothing attributes if they are different. If they are then all existing
        // non-smoothed data will be copied from mdl0 to mdl1 before mdl1 is
        // smoothed and then written back out to disk

        ModelInterface mdl = null;
        if (modelType == SourceDefinition.GEOTESSMODEL) {
            System.out.println("  Loading GeoTessModel \"" + fin + "\" ... ");
            LibCorr3DModelGMP gm = new LibCorr3DModelGMP(fin);
            mdl = gm;
        }

        System.out.println("  Loading Tomographic GeoTessModel \"" + finTomo + "\" ... ");
        GeoTessModelGMP tomoModel = new GeoTessModelGMP(finTomo);
        //XXX tomoModel.setActiveNodesGlobal();
        tomoModel.setActiveRegion();

        // get attribute indexer from model 0 and see if it contains the requested
        // smoothing attributes exactly (no more no less)

        System.out.println("  Constructing Smoothing AttributeIndexer ...");

        AttributeIndexerSmart ai = mdl.getAttributeIndexer();
        EnumSet<GeoAttributes> attrSet = ai.getSupportedAttributes();
        boolean useModel = false;
        if (attrSet.contains(GeoAttributes.TT_MODEL_VARIANCE_SMOOTHED)) {
            if ((aSmoothingFlag[0] ==
                    attrSet.contains(GeoAttributes.TT_MODEL_VARIANCE_DIAGONAL_SMOOTHED)) &&
                    (aSmoothingFlag[1] ==
                            attrSet.contains(GeoAttributes.TT_MODEL_VARIANCE_OFFDIAGONAL_SMOOTHED)) &&
                    (aSmoothingFlag[2] ==
                            attrSet.contains(GeoAttributes.TT_MODEL_VARIANCE_NONREPRESENTED_SMOOTHED)))
                useModel = true;
        }

        // now create a new attribute indexer and build the site attribute map
        // for each unique surface (a unique seismic phase/receiver A/receiver B
        // is defined as a unique surface). This is required for smoothing
        // and to copy data from the original model to the new copy if useModel is
        // false

        AttributeIndexerSmart aiSmooth = new AttributeIndexerSmart();
        ArrayList<SiteAttributeMap> samList = new ArrayList<SiteAttributeMap>();
        HashMap<SeismicPhase, HashMap<Receiver, HashMap<Receiver,
                SiteAttributeMap>>> surfMap;
        surfMap = new HashMap<SeismicPhase, HashMap<Receiver, HashMap<Receiver,
                SiteAttributeMap>>>();
        for (int i = 0; i < ai.size(); ++i) {
            Object[] keys = ai.getKeys(i);
            Receiver rcvrA = (Receiver) keys[0];
            Receiver rcvrB = (Receiver) keys[1];
            SeismicPhase phase = (SeismicPhase) keys[2];

            addNewAttributeSurface(phase, rcvrA, rcvrB, aiSmooth,
                    samList, surfMap, aSmoothingFlag);
        }

        // if it we can't use the model directly we must reload the model as a copy
        // and assign the smoothing attribute indexer (aiSmooth) for the model copy
        // and set it into that model (initializing data to NaN) and then copy all of
        // the non-smoothed data from the original model into the copy. Then we can
        // smooth the copy. Otherwise, we can smooth the original model directly.

        if (!useModel) {
            // attribute definition isn't exactly the same as the request ... we
            // first need to reload the model and then set the smoothing attribute
            // indexer into the copied model initializing all values to NaN

            ModelInterface mdlCopy = null;
            if (modelType == SourceDefinition.GEOTESSMODEL) {
                System.out.println("  Loading GeoTessModel Copy \"" + fin + "\" ... ");
                LibCorr3DModelGMP gm = new LibCorr3DModelGMP(fin);
                mdlCopy = gm;
            }

            // set attribute indexer into model copy and initialize to NaN

            mdlCopy.setAttributeIndexer(aiSmooth, Float.NaN);

            // now we must copy all of the unsmoothed values from the original model
            // into their appropriate indices in the copied model. First make an
            // array of the GeoAttributes to be copied.

            System.out.println("  Copying Existing Unsmoothed Data to Model Copy ...");

            GeoAttributes[] attr = {GeoAttributes.TT_MODEL_VARIANCE,
                    GeoAttributes.TT_MODEL_VARIANCE_DIAGONAL,
                    GeoAttributes.TT_MODEL_VARIANCE_OFFDIAGONAL,
                    GeoAttributes.TT_MODEL_VARIANCE_NONREPRESENTED,
                    GeoAttributes.TT_MODEL_VARIANCE_REPRESENTED_FRACTION};

            // loop over each GeoAttribute to be copied

            for (int k = 0; k < attr.length; ++k) {
                // loop over each unique surface

                for (int j = 0; j < samList.size(); ++j) {
                    // get surface attribute map and get the model attribute index and
                    // the model copy attribute index

                    SiteAttributeMap sam = samList.get(j);
                    int mdlAttrIndex = ai.getIndex(sam.aRcvrA, sam.aRcvrB, sam.aPhase,
                            attr[k]);
                    int mdlCopyAttrIndex = sam.aAttrbMap.get(attr[k]);

                    // loop over all points and copy the attribute value from the original
                    // model to the copy

                    for (int i = 0; i < mdl.getPointCount(); ++i) {
                        double value = mdl.getPointValue(i, mdlAttrIndex);
                        mdlCopy.setPointValue(i, mdlCopyAttrIndex, value);
                    }
                }
            }

            // done ... now assign the copy to the original reference and continue

            mdl = mdlCopy;
        }
        setSourceModel(mdl);

        // done ... smooth model

        if (aRingWeights.size() > 0) {
            System.out.println("");
            System.out.println("  Performing Neighbor Ring Based Smoothing ...");
            smoothNeighborRings(mdl, tomoModel, samList, aRingWeights);
        }
        if (aDistWeights.size() > 0) {
            System.out.println("");
            System.out.println("  Performing Neighbor Distance Based Smoothing ...");
            smoothNeighborDistance(mdl, samList, aRingWeights.keySet(),
                    aDistWeights);
        }

        // set GeoModel description

        s = mdl.getModelDescription() + NL +
                "Added Smoothed Travel Time Uncertainty Data (" +
                Globals.getTimeStamp() + ")" + NL + smoothingDescr;
        mdl.setModelDescription(s);

        // write model to disk

        System.out.println("  Writing Smoothed Model ...");
        mdl.writeModel(fout, "*");

        System.out.println(NL + "  Elapsed Time = " + Globals.elapsedTimeString(strtTime));
        System.out.println("RayUncertainty.smoothModel Complete");
    }

    public void clearSmoothingDefinitions() {
        clearSmoothingDefinition(UncertaintyComponent.DIAGONAL);
        clearSmoothingDefinition(UncertaintyComponent.OFF_DIAGONAL);
        clearSmoothingDefinition(UncertaintyComponent.NON_REPRESENTED);
    }

    public void clearSmoothingDefinition(UncertaintyComponent uc) {
        int cmpnt = uc.ordinal();
        aSmoothingDescr[cmpnt] = "";
        aSmoothingFlag[cmpnt] = false;
        aRingWeights.remove(cmpnt);
        aDistWeights.remove(cmpnt);
    }

    /**
     * Sets the smoothing definition for the input component (uc) to the input
     * type (st) with the input weight definition (wghts).
     *
     * @param uc    Uncertainty component type.
     * @param st    Smoothing type.
     * @param wghts Weight definition.
     * @throws IOException
     */
    public void setSmoothingDefinition(UncertaintyComponent uc,
                                       SmoothingType st, double[] wghts)
            throws IOException {
        if (st == SmoothingType.RING)
            setRingComponentWeights(uc, wghts);
        else if (st == SmoothingType.DISTANCE)
            setDistanceComponentWeights(uc, wghts);
    }

    /**
     * Called by smoothModel right before smoothing begins. If the input source
     * model is a LibCorr3DModelGMP object then the GeoTessPosition object is
     * created to perform interpolations.
     *
     * @param sourceModel The input source model for which a GeoTessPosition
     *                    interpolator is created if the model is a
     *                    LibCorr3DModelGMP object.
     * @throws GeoTessException
     */
    private void setSourceModel(ModelInterface sourceModel) throws GeoTessException {
        if (sourceModel instanceof LibCorr3DModelGMP) {
            InterpolatorType itype = InterpolatorType.LINEAR;
            LibCorr3DModelGMP mdl = (LibCorr3DModelGMP) sourceModel;
            aGeoTessPosition = GeoTessPosition.getGeoTessPosition(mdl, itype);
        }
    }

    /**
     * Sets the input distance smoothing weights for the input uncertainty
     * component.
     *
     * @param uc    The uncertainty component for which distance smoothing will
     *              be performed.
     * @param wghts The map of distance weights defined for a specific uncertainty
     *              component (uc) to be smoothed. Contains a 2 element array
     *              defining entries D0, and D1. Note: any component smoothed
     *              with RNS cannot be smoothed with DNS. Otherwise an error is
     *              thrown.
     * @throws IOException
     */
    private void setDistanceComponentWeights(UncertaintyComponent uc,
                                             double[] wghts)
            throws IOException {

        String s;

        // only add description of the requested component (cmpnt) is defined

        boolean use3D = false;

        int cmpnt = uc.ordinal();
        if (wghts == null) {
            clearSmoothingDefinition(uc);
            return;
        } else if (aRingWeights.containsKey(cmpnt)) {
            s = "Distance Weighting and Ring Weighting are both defined for " +
                    "component " + uc.name() + " ... Only one definition is allowed ...";
            throw new IOException(s);
        }

        // set flag to show component was defined and build description header

        aSmoothingFlag[cmpnt] = true;
        aSmoothingDescr[cmpnt] += "    Smoothing " + uc.name() +
                " with Neighbor Distance Weighted Smoothing " +
                NL;
        aSmoothingDescr[cmpnt] += "      ";

        // validate D0 and D1 ... throw errors if invalid

        if (wghts.length != 2) {
            s = "Distance weights must have two and only two entries ..." +
                    " Component " + uc.name() + " has " + wghts.length + " ...";
            throw new IOException(s);
        }
        if (wghts[0] < 0.0) {
            s = "D0 must be >= 0.0 ... Component " + uc.name() + " is Set as " +
                    wghts[0];
            throw new IOException(s);
        }
        if (wghts[0] > wghts[1]) {
            s = "D0 must be <= D1 ... Component " + uc.name() + " is Set as D0 = " +
                    wghts[0] + " and D1 = " + wghts[1];
            throw new IOException(s);
        }

        // valid ... finish appending description and convert input degrees to
        // km

        aSmoothingDescr[cmpnt] += "D0 (deg) = " + wghts[0] + ", D1 (deg) = " +
                wghts[1] + NL;

        // convert deg distances to km and set definition for the input component

        if (use3D) {
            wghts[0] *= KM_PER_DEG;
            wghts[1] *= KM_PER_DEG;
        } else {
            wghts[0] = Math.toRadians(wghts[0]);
            wghts[1] = Math.toRadians(wghts[1]);
        }
        aDistWeights.put(cmpnt, wghts);
    }

    /**
     * Sets the input ring smoothing weights for the input uncertainty
     * component.
     *
     * @param uc    The uncertainty component for which smoothing will be
     *              performed.
     * @param wghts The ring weight definition for the input uncertainty
     *              component (uc). The double[] array contains the ring
     *              smoothing weights for the input component. Note:
     *              any component smoothed with RNS cannot be smoothed with
     *              DNS. Otherwise an error is thrown.
     * @throws IOException
     */
    private void setRingComponentWeights(UncertaintyComponent uc,
                                         double[] wghts) throws IOException {
        String s;

        // only add description of the requested component (cmpnt) is defined

        int cmpnt = uc.ordinal();
        if (wghts == null) {
            clearSmoothingDefinition(uc);
            return;
        } else if (aDistWeights.containsKey(cmpnt)) {
            s = "Distance Weighting and Ring Weighting are both defined for " +
                    "component " + uc.name() + " ... Only one definition is allowed ...";
            throw new IOException(s);
        }

        // set flag to show component was defined and build description header

        aSmoothingFlag[cmpnt] = true;
        aSmoothingDescr[cmpnt] += "    Smoothing " + uc.name() +
                " with Neighbor Ring Weighted Smoothing " + NL;
        aSmoothingDescr[cmpnt] += "      ";

        // add each ring description

        for (int i = 0; i < wghts.length; ++i) {
            if (i == 0)
                aSmoothingDescr[cmpnt] += "Ring " + i + " = " + wghts[i];
            else
                aSmoothingDescr[cmpnt] += ", Ring " + i + " = " + wghts[i];
        }
        aSmoothingDescr[cmpnt] += NL;

        double wghtsum = 0.0;
        for (int i = 0; i < wghts.length; ++i) wghtsum += wghts[i];
        for (int i = 0; i < wghts.length; ++i) wghts[i] /= wghtsum;
        aRingWeights.put(cmpnt, wghts);
    }

    /**
     * Performs ring weighted smoothing of all uncertainty components defined
     * as a key in the input ringWeights parameter. Each entry in the
     * ringWeights parameter has a key value of either 0, 1, or 2, which
     * corresponds to 0=DIAGONAL, 1=OFF_DIAGONAL, and 2=NON-REPRESENTED uncertainty
     * components. Associated with each key is a n-element array containing the
     * ring weights used to weight contributions from points in those rings to
     * the total smoothed result. The points that influence the source points
     * smoothed result are taken from the tomography model (modelTomo). Since the
     * source point is interpolated off of the tomography grid all influencing
     * points on the tomography grid are accessed for ring neighbors. The values
     * stored on the ring neighbors are averaged in each ring and then weighted
     * (by the ring weights) to obtain a single result for one of the tomography
     * model influence nodes. These influence node results are then weighted by
     * the influence weights to finally arrive at the smoothed result on the
     * source point.
     *
     * @param modelSource The source model from which all valid points for which
     *                    smoothing was requested are evaluated.
     * @param modelTomo   The tomography model for which points influencing the
     *                    source points, and their ring neighbors, are accessed
     *                    and used as interpolation points on the source grid
     *                    to acquire results which are first weighted by ring and
     *                    then by influence weight to arrive at a smoothed result.
     * @param attrMapList The map of all unique surfaces defined on the source
     *                    model. The requested smoothing is performed on each
     *                    unique surface stored on the source model.
     * @param ringWeights The map of smoothing component index associated with
     *                    the array of ring weights used to weight neighbors that
     *                    contribute to the source point smoothed result.
     * @throws IOException
     * @throws GMPException
     * @throws GeoTessException
     */
    private void smoothNeighborRings(ModelInterface modelSource,
                                     GeoTessModelGMP modelTomo,
                                     ArrayList<SiteAttributeMap> attrMapList,
                                     HashMap<Integer, double[]> ringWeights)
            throws IOException, GMPException, GeoTessException {
        double r, origVal;
        int gn, layer;
        int currTomoLayer = -1;

        // exit if no smoothing has been requested

        if ((ringWeights == null) || (ringWeights.size() == 0)) return;

        // use the first surface phase as the influence phase for finding influence
        // point weights on the tomography model

        SeismicPhase inflPhase = attrMapList.get(0).aPhase;
        //XXX modelTomo.setActiveNodesGlobal(inflPhase.getWaveType());
        modelTomo.setActiveRegion();

        // set up temporary arrays

        GeoAttributes[] componentAttr =
                {GeoAttributes.TT_MODEL_VARIANCE_DIAGONAL,
                        GeoAttributes.TT_MODEL_VARIANCE_OFFDIAGONAL,
                        GeoAttributes.TT_MODEL_VARIANCE_NONREPRESENTED};

        GeoAttributes smoothTotalAttr = GeoAttributes.TT_MODEL_VARIANCE_SMOOTHED;
        GeoAttributes[] smoothComponentAttr =
                {GeoAttributes.TT_MODEL_VARIANCE_DIAGONAL_SMOOTHED,
                        GeoAttributes.TT_MODEL_VARIANCE_OFFDIAGONAL_SMOOTHED,
                        GeoAttributes.TT_MODEL_VARIANCE_NONREPRESENTED_SMOOTHED};

        // build attribute indexes of the non-smoothed and smoothed attributes for
        // use below
        // attrIndex[p][k]    has the non-smoothed attribute indexes for each
        //                    component (p) and each surface (k).
        // nonSmthAttrIndex   has the non-smoothed attribute indexes of just those
        //                    components (p) to be smoothed for all surfaces (k).
        // smthAttrIndex      has the smoothed attribute indexes of just those
        //                    components (p) to be smoothed for all surfaces (k).
        // smthTotalAttrIndex has the total smoothed attribute index for all
        //                    surfaces [k].

        int[][] attrIndex = new int[3][attrMapList.size()];
        HashMap<Integer, int[]> nonSmthAttrIndex = new HashMap<Integer, int[]>();
        HashMap<Integer, int[]> smthAttrIndex = new HashMap<Integer, int[]>();
        int[] smthTotalAttrIndex = new int[attrMapList.size()];
        HashMap<Integer, int[]> ringNeighbors = new HashMap<Integer, int[]>();
        HashMap<Integer, double[]> distNeighbors = new HashMap<Integer, double[]>();

        // loop over each component and create entries

        aSmoothNeighbors = new Statistic();
        for (int p = 0; p < 3; ++p) {
            // get the pth component non-smoothed attribute index and loop over all
            // surfaces setting the index

            int[] attrindx = attrIndex[p];
            for (int k = 0; k < attrMapList.size(); ++k)
                attrindx[k] = attrMapList.get(k).getAttributeIndex(componentAttr[p]);

            // if the pth component is to be smoothed then add it to the non-smoothed
            // and smoothed maps

            if (ringWeights.containsKey(p)) {
                // add the pth non-smoothed attribute index array for all surfaces to
                // the non-smoothed map and create a new one for the smoothed map and
                // add it to the map ... loop over all surfaces and fill the smoothed
                // attribute index

                aSmoothResidual[p] = new Statistic();
                int wghtlen = ringWeights.get(p).length;
                aRingNeighbors[p] = new Statistic[wghtlen];
                aDistNeighbors[p] = new Statistic[wghtlen];
                for (int rr = 0; rr < wghtlen; ++rr) {
                    aRingNeighbors[p][rr] = new Statistic();
                    aDistNeighbors[p][rr] = new Statistic();
                }
                distNeighbors.put(p, new double[wghtlen]);
                ringNeighbors.put(p, new int[wghtlen]);
                nonSmthAttrIndex.put(p, attrindx);
                attrindx = new int[attrMapList.size()];
                smthAttrIndex.put(p, attrindx);
                for (int k = 0; k < attrMapList.size(); ++k)
                    attrindx[k] = attrMapList.get(k).getAttributeIndex(smoothComponentAttr[p]);
            }
        }

        // loop over all surfaces and set the total smoothed attribute
        // index array

        for (int k = 0; k < attrMapList.size(); ++k)
            smthTotalAttrIndex[k] = attrMapList.get(k).getAttributeIndex(smoothTotalAttr);

        // finally, build the map of interpolation results for each smoothed
        // component (p) and each surface (k). This is used in function
        // getPointRingMap to hold temporary interpolation results.

        HashMap<Integer, double[]> intrpArray = new HashMap<Integer, double[]>();
        HashMap<Integer, double[]> inflRslts = new HashMap<Integer, double[]>();
        for (Map.Entry<Integer, int[]> e : smthAttrIndex.entrySet()) {
            double[] ia = new double[attrMapList.size()];
            intrpArray.put(e.getKey(), ia);
            ia = new double[attrMapList.size()];
            inflRslts.put(e.getKey(), ia);
        }

        // build various containers

        GeoVector gv = new GeoVector();
        HashMap<Integer, Double> inflWeights = new HashMap<Integer, Double>();
        HashMapIntegerKey<HashMapIntegerInteger> pointRingNghbrMap;
        pointRingNghbrMap = new HashMapIntegerKey<HashMapIntegerInteger>();
        ModelInterface tomoModel = modelTomo;

        // build ring array and ring array count maps for use in getPointRingMap.
        // map<smooth component, double[surface][ring]>

        HashMap<Integer, double[][]> ringArray = new HashMap<Integer, double[][]>();
        HashMap<Integer, int[][]> ringArrayCnt = new HashMap<Integer, int[][]>();
        int nringsmax = 0;
        for (Map.Entry<Integer, double[]> eRing : ringWeights.entrySet()) {
            int p = eRing.getKey();
            int nrings = eRing.getValue().length;
            if (nringsmax < nrings) nringsmax = nrings;
            double[][] ra = new double[nrings][attrMapList.size()];
            ringArray.put(p, ra);
            int[][] rc = new int[nrings][attrMapList.size()];
            ringArrayCnt.put(p, rc);
        }
        --nringsmax;

        // build a sorted list of points on layer index and store them in a map

        int pointCount = modelSource.getPointCount();
        HashMap<Integer, ArrayListInt> layerSortedPoints = new HashMap<Integer, ArrayListInt>();
        for (int i = 0; i < pointCount; ++i) {
            layer = modelSource.getPointMap(i)[1];
            ArrayListInt pntList = layerSortedPoints.get(layer);
            if (pntList == null) {
                pntList = new ArrayListInt();
                layerSortedPoints.put(layer, pntList);
            }
            pntList.add(i);
        }

        //*************** Smoothing Operation *************************************
        // loop over all source points sorted on layer index to minimize the number
        // of times that modelSource.setGridNodeElementNeighbors(layer) is called

        for (Map.Entry<Integer, ArrayListInt> layerPnts : layerSortedPoints.entrySet()) {
            // get layer index and corresponding list of point indices

            layer = layerPnts.getKey();
            ArrayListInt pntList = layerPnts.getValue();
            modelSource.setGridNodeElementNeighbors(layer);

            // first build a map of tomography model layer ordered source points

            HashMap<Integer, ArrayListInt> tomoLayerPoints;
            tomoLayerPoints = new HashMap<Integer, ArrayListInt>();
            modelTomo.setActiveRegion();
            //XXX modelTomo.setActiveNodesGlobal(attrMapList.get(0).aPhase.getWaveType());
            for (int pnt = 0; pnt < pntList.size(); ++pnt) {
                // get the point index (gn) and the radius of the point (r)

                gn = pntList.get(pnt);
                r = modelSource.getPointRadius(gn);
                gv.setGeoVector(modelSource.getPointUnitVector(gn), r);

                // find the tomography grid points that influence this source point and
                // return the influence point indexes and weights in inflWeights

                RayUncertainty.getPositionInfluencePoints(modelTomo, gv,
                        attrMapList.get(0).aPhase,
                        inflWeights);

                // get the first influence point and see what layer it is (all influence
                // points will be in the same tomography model layer

                int tomoLayer = -1;
                int inflPntIndx = inflWeights.entrySet().iterator().next().getKey();
                //XXX tomoLayer = modelTomo.getMajorLayerIndex(inflPntIndx);
                tomoLayer = modelTomo.getPointMap().getLayerIndex(inflPntIndx);

                // if this layer is not yet represented in the map then create it

                ArrayListInt tomoLyrPnts = tomoLayerPoints.get(tomoLayer);
                if (tomoLyrPnts == null) {
                    tomoLyrPnts = new ArrayListInt();
                    tomoLayerPoints.put(tomoLayer, tomoLyrPnts);
                }

                // add the source point index to the map and continue

                tomoLyrPnts.add(gn);
            }

            // loop over all points and smooth them. The map tomoLayerPoints are a
            // set of source model points to be smoothed that reside within a specific
            // layer of the tomography model. IOW, the tomography points that influence
            // the source points in the map are all from a single layer (epnts.getKey()
            // below)

            for (Map.Entry<Integer, ArrayListInt> ePnts : tomoLayerPoints.entrySet()) {
                // set the topology of the tomography model for the current layer

                int tomoLayer = ePnts.getKey();
                if (currTomoLayer != -1)
                    modelTomo.clearGridNodeElementNeighbors(currTomoLayer);
                currTomoLayer = tomoLayer;
                modelTomo.setGridNodeElementNeighbors(currTomoLayer);

                // loop over all source model points that are influenced by tomography
                // points that lay exclusively withing tomogLayer

                ArrayListInt srcLayerPnts = ePnts.getValue();
                for (int pnt = 0; pnt < srcLayerPnts.size(); ++pnt) {
                    // get the next source point and set its position vector.

                    gn = srcLayerPnts.get(pnt);
                    r = modelSource.getPointRadius(gn);
                    gv.setGeoVector(modelSource.getPointUnitVector(gn), r);

                    // get the tomography influence weights for the current source
                    // point position gv

                    RayUncertainty.getPositionInfluencePoints(modelTomo, gv, inflPhase,
                            inflWeights);

                    // initialize to zero the surface result array of each component to be
                    // smoothed

                    for (Map.Entry<Integer, double[]> eInfl : inflRslts.entrySet()) {
                        double[] rslts = eInfl.getValue();
                        for (int k = 0; k < rslts.length; ++k) rslts[k] = 0.0;
                        int[] ringCnt = ringNeighbors.get(eInfl.getKey());
                        double[] ringDst = distNeighbors.get(eInfl.getKey());
                        for (int i = 0; i < ringCnt.length; ++i) {
                            ringCnt[i] = 0;
                            ringDst[i] = 0.0;
                        }
                    }

                    // loop over all influence points and sum contributions to result
                    // array

                    double weightSum = 0.0;
                    double totlNghbrCnt = 0.0;
                    for (Map.Entry<Integer, Double> eIP : inflWeights.entrySet()) {
                        // get the influence point index and weight and retrieve the
                        // points ring weighted interpolation array

                        int inflPntIndx = eIP.getKey();
                        double inflWght = eIP.getValue();
                        totlNghbrCnt += inflWght * getPointRingWeightedInterpolation(
                                inflPntIndx, nringsmax,
                                pointRingNghbrMap, modelSource,
                                tomoModel, nonSmthAttrIndex,
                                intrpArray, ringWeights,
                                ringArray, ringArrayCnt,
                                ringNeighbors, distNeighbors,
                                gv, inflWght);

                        // get list of surfaces utilizing this phase ... loop over all
                        // surfaces and sum ring weighted interpolation for this influence
                        // point

                        for (Map.Entry<Integer, double[]> eInfl : intrpArray.entrySet()) {
                            // get interpolation result and influence result arrays ... loop
                            // over all surfaces and sum weighted results

                            double[] intrps = eInfl.getValue();
                            double[] rslts = inflRslts.get(eInfl.getKey());
                            for (int k = 0; k < intrps.length; ++k) {
                                rslts[k] += inflWght * intrps[k];
                                weightSum += inflWght;
                            }
                        }
                    } // end for (Map.Entry<Integer, Double> eIP: inflWeights.entrySet())

                    // done with influence contributions ... loop over all surfaces

                    aSmoothNeighbors.add(totlNghbrCnt);
                    for (int k = 0; k < attrMapList.size(); ++k) {
                        // set total smoothed result for surface k to zero and loop over
                        // all three uncertainty components

                        double smthTotl = 0.0;
                        for (int p = 0; p < 3; ++p) {
                            // get influence point result array and see if it is null for
                            // component p

                            double[] rslts = inflRslts.get(p);
                            if (rslts != null) {
                                // not null ... this component was smoothed ... get the kth
                                // surface attribute index and normalize the result with the
                                // weight sum

                                if (weightSum != 1.0) rslts[k] /= weightSum;

                                // set the result into the surface index for the current point
                                // (gn) and sum the value to the total smoothed result

                                int sindx = smthAttrIndex.get(p)[k];
                                modelSource.setPointValue(gn, sindx, rslts[k]);
                                smthTotl += rslts[k];
                                origVal = modelSource.getPointValue(gn, attrIndex[p][k]);
                                aSmoothResidual[p].add(rslts[k] - origVal);
                                for (int rr = 0; rr < ringNeighbors.get(p).length; ++rr) {
                                    aRingNeighbors[p][rr].add(ringNeighbors.get(p)[rr]);
                                    aDistNeighbors[p][rr].add(distNeighbors.get(p)[rr]);
                                }
                            } else {
                                // null ... this component was NOT smoothed ... get the
                                // unsmoothed result for this component and add it to the
                                // total smoothed result

                                int ai = attrIndex[p][k];
                                smthTotl += modelSource.getPointValue(gn, ai);
                            }
                        } // end for (int p = 0; p < 3; ++p)

                        // done with all components ... set the total smoothed result for
                        // this surface at the current point (gn) and continue to the next
                        // surface

                        modelSource.setPointValue(gn, smthTotalAttrIndex[k], smthTotl);
                    } // end for (int k = 0; k < attrMapList.size(); ++k)
                } // end for (int pnt = 0; pnt < srcLayerPnts.size(); ++pnt)
            } // end for (Map.Entry<Integer, ArrayListInt> ePnts:
            //          tomoLayerPoints.entrySet())

            // clear the layer grid node element neighbors and loop for the next layer

            modelSource.clearGridNodeElementNeighbors(layer);
        } // end for (Map.Entry<Integer, ArrayListInt> layerPnts:
        //          layerSortedPoints.entrySet())

        // done ... now output statistics

        System.out.println("");
        System.out.println("      " + Globals.repeat("*", 65));
        System.out.println("      Neighbor Count Results:");
        String fmt = "%8d   %8.4f   %8.4f   %8.4f   %8.4f   %8.4f";
        System.out.println("         Points       Min        Max       " +
                "Mean     Std.Dev.      RMS");
        System.out.println("        " + String.format(fmt,
                aSmoothNeighbors.getCount(),
                aSmoothNeighbors.getMinimum(),
                aSmoothNeighbors.getMaximum(),
                aSmoothNeighbors.getMean(),
                aSmoothNeighbors.getStdDev(),
                aSmoothNeighbors.getRMS()));
        System.out.println("      " + Globals.repeat("*", 65));

        for (Map.Entry<Integer, double[]> eSmth : ringWeights.entrySet()) {
            int p = eSmth.getKey();
            Statistic rsdStat = aSmoothResidual[p];
            Statistic[] dstNghbrStat = aDistNeighbors[p];
            Statistic[] rngNghbrStat = aRingNeighbors[p];

            System.out.println(NL + "      Smoothed Component: " +
                    UncertaintyComponent.values()[p]);
            System.out.println("      " + Globals.repeat("*", 65));
            System.out.println("      Difference (Smooth - Original) Results:");
            fmt = "%8d   %8.4f   %8.4f   %8.4f   %8.4f   %8.4f";
            System.out.println("         Points       Min        Max       " +
                    "Mean     Std.Dev.      RMS");
            System.out.println("        " + String.format(fmt, rsdStat.getCount(),
                    rsdStat.getMinimum(), rsdStat.getMaximum(),
                    rsdStat.getMean(), rsdStat.getStdDev(),
                    rsdStat.getRMS()));

            System.out.println("");
            System.out.println("      Neighbor Count Results By Ring:");
            System.out.println("        Ring      Min        Max       " +
                    "Mean     Std.Dev.      RMS");
            fmt = " %4d    %8.4f   %8.4f   %8.4f   %8.4f   %8.4f";
            for (int i = 0; i < rngNghbrStat.length; ++i)
                System.out.println("        " + String.format(fmt, i,
                        rngNghbrStat[i].getMinimum(),
                        rngNghbrStat[i].getMaximum(),
                        rngNghbrStat[i].getMean(),
                        rngNghbrStat[i].getStdDev(),
                        rngNghbrStat[i].getRMS()));

            System.out.println("");
            System.out.println("      Neighbor Distance Results By Ring (Deg):");
            System.out.println("        Ring      Min        Max       " +
                    "Mean     Std.Dev.      RMS");
            fmt = " %4d    %8.4f   %8.4f   %8.4f   %8.4f   %8.4f";
            for (int i = 0; i < dstNghbrStat.length; ++i)
                System.out.println("        " + String.format(fmt, i,
                        dstNghbrStat[i].getMinimum(),
                        dstNghbrStat[i].getMaximum(),
                        dstNghbrStat[i].getMean(),
                        dstNghbrStat[i].getStdDev(),
                        dstNghbrStat[i].getRMS()));
            System.out.println("");
        }
    }

    /**
     * Performs distance weighted smoothing of all uncertainty components defined
     * as a key in the input distanceWeights parameter. Each entry in the
     * distanceWeights parameter has a key value of either 0, 1, or 2, which
     * corresponds to 0=DIAGONAL, 1=OFF_DIAGONAL, and 2=NON-REPRESENTED uncertainty
     * components. Associated with each key is a 2-element array containing the
     * distance elements [0]=D0 and [1]=D1 where D0 represents the distance,
     * within which, all neighbors have an equivalent weight of 1.0; and D1
     * represents the distance, within which, all neighbors have a weight that
     * is defined between 0.0 and 1.0 and falls from 1.0 at D0 to 0.0 at D1. The
     * entry D0 must be between 0.0 and D1 and D1 must be > 0.0
     * <p>
     * This function takes any distance weighted components and adds in any
     * previously ring neighbor smoothed components along with any non-smoothed
     * components to set the total smoothing result for each surface.
     *
     * @param modelSource     The source model containing attributes to be smoothed.
     * @param attrMapList     The list of all surfaces containing the phase and
     *                        receiver A and B objects and the map of GeoAttributes
     *                        defines for the surface associated with their model
     *                        attribute indices.
     * @param ringWeights     A set of smoothing component indices that were used
     *                        to perform neighbor ring based smoothing.
     * @param distanceWeights The map of smoothing component index associated with
     *                        a 2-element array containing the distance limits
     *                        [0]=D0 and [1]=D1.
     * @throws IOException
     * @throws GMPException
     * @throws GeoTessException
     */
    private void smoothNeighborDistance(ModelInterface modelSource,
                                        ArrayList<SiteAttributeMap> attrMapList,
                                        Set<Integer> ringWeights,
                                        HashMap<Integer, double[]> distanceWeights)
            throws IOException, GMPException, GeoTessException {
        int indx, sindx;
        double w, val, origVal, dst, gnNghbrRadius = 0.0;
        int gn, gnNghbr, layer, gnNghbrLayer = 0;
        double[][] nghbrDstLmtCounts = new double[3][4];
        boolean use3D = false;

        // exit if no smoothing has been requested

        if ((distanceWeights == null) || (distanceWeights.size() == 0)) return;

        // set up temporary arrays

        GeoAttributes totalAttr = GeoAttributes.TT_MODEL_VARIANCE;
        GeoAttributes[] componentAttr =
                {GeoAttributes.TT_MODEL_VARIANCE_DIAGONAL,
                        GeoAttributes.TT_MODEL_VARIANCE_OFFDIAGONAL,
                        GeoAttributes.TT_MODEL_VARIANCE_NONREPRESENTED};

        GeoAttributes smoothTotalAttr = GeoAttributes.TT_MODEL_VARIANCE_SMOOTHED;
        GeoAttributes[] smoothComponentAttr =
                {GeoAttributes.TT_MODEL_VARIANCE_DIAGONAL_SMOOTHED,
                        GeoAttributes.TT_MODEL_VARIANCE_OFFDIAGONAL_SMOOTHED,
                        GeoAttributes.TT_MODEL_VARIANCE_NONREPRESENTED_SMOOTHED};

        // build attribute indexes of the non-smoothed and smoothed attributes for
        // use below
        // attrIndex[p][k]    has the non-smoothed attribute indexes for each
        //                    component (p) and each surface (k).
        // nonSmthAttrIndex   has the non-smoothed attribute indexes of just those
        //                    components (p) to be smoothed for all surfaces (k).
        // smthAttrIndex      has the smoothed attribute indexes of just those
        //                    components (p) to be smoothed for all surfaces (k).
        // smthTotalAttrIndex has the total smoothed attribute index for all
        //                    surfaces [k].

        int[][] attrIndex = new int[3][attrMapList.size()];
        HashMap<Integer, int[]> nonSmthAttrIndex = new HashMap<Integer, int[]>();
        HashMap<Integer, int[]> smthAttrIndex = new HashMap<Integer, int[]>();
        int[] smthTotalAttrIndex = new int[attrMapList.size()];

        // loop over each component and surface and create attribute index
        // arrays

        aSmoothNeighbors = new Statistic();
        for (int p = 0; p < 3; ++p) {
            // get the pth component non-smoothed attribute index and loop over all
            // surfaces setting the index

            int[] attrindx = attrIndex[p];
            for (int k = 0; k < attrMapList.size(); ++k)
                attrindx[k] = attrMapList.get(k).getAttributeIndex(componentAttr[p]);

            // if the pth component is to be smoothed then add it to the non-smoothed
            // and smoothed maps

            if (distanceWeights.containsKey(p)) {
                // add the pth non-smoothed attribute index array for all surfaces to
                // the non-smoothed map and create a new one for the smoothed map and
                // add it to the map ... loop over all surfaces and fill the smoothed
                // attribute index

                aSmoothResidual[p] = new Statistic();

                aDistNeighbors[p] = new Statistic[4];
                aDistNeighbors[p][0] = new Statistic();
                aDistNeighbors[p][1] = new Statistic();
                aDistNeighbors[p][2] = new Statistic();
                aDistNeighbors[p][3] = new Statistic();
                double[] lmts = new double[4];
                aDistNghbrLimits[p] = lmts;

                double[] D = distanceWeights.get(p);
                if ((D[0] > 0.0) && (D[0] != D[1])) {
                    lmts[0] = 0.5 * D[0];
                    lmts[1] = D[0];
                    lmts[2] = 0.5 * (D[0] + D[1]);
                    lmts[3] = D[1];
                } else {
                    lmts[0] = 0.25 * D[1];
                    lmts[1] = 0.5 * D[1];
                    lmts[2] = 0.75 * D[1];
                    lmts[3] = D[1];
                }
                nonSmthAttrIndex.put(p, attrindx);
                attrindx = new int[attrMapList.size()];
                smthAttrIndex.put(p, attrindx);
                for (int k = 0; k < attrMapList.size(); ++k)
                    attrindx[k] = attrMapList.get(k).getAttributeIndex(smoothComponentAttr[p]);
            }
        }

        // loop over all surfaces and set the total smoothed attribute
        // index array

        for (int k = 0; k < attrMapList.size(); ++k)
            smthTotalAttrIndex[k] = attrMapList.get(k).getAttributeIndex(smoothTotalAttr);

        // finally, build the map of interpolation results for each smoothed
        // component (p) and each surface (k). This is used in function
        // getPointRingMap to hold temporary interpolation results.

        HashMap<Integer, double[]> intrpArray = new HashMap<Integer, double[]>();
        HashMap<Integer, double[]> intrpWeight = new HashMap<Integer, double[]>();
        for (Map.Entry<Integer, int[]> e : smthAttrIndex.entrySet()) {
            double[] ia = new double[attrMapList.size()];
            intrpArray.put(e.getKey(), ia);
            ia = new double[attrMapList.size()];
            intrpWeight.put(e.getKey(), ia);
        }

        // find maximum search distance for finding neighbors

        double maxDist = 0.0;
        for (Map.Entry<Integer, double[]> eDist : distanceWeights.entrySet()) {
            double dist = eDist.getValue()[1];
            if (maxDist < dist) maxDist = dist;
        }

        // build a sorted list of points on layer index and store them in a map

        int pointCount = modelSource.getPointCount();
        HashMap<Integer, ArrayListInt> layerSortedPoints = new HashMap<Integer, ArrayListInt>();
        for (int i = 0; i < pointCount; ++i) {
            layer = modelSource.getPointMap(i)[1];
            ArrayListInt pntList = layerSortedPoints.get(layer);
            if (pntList == null) {
                pntList = new ArrayListInt();
                layerSortedPoints.put(layer, pntList);
            }
            pntList.add(i);
        }

        //*************** Smoothing Operation *************************************
        // loop over all source points sorted on layer index to minimize the number
        // of times that modelSource.setGridNodeElementNeighbors(layer) is called

        HashMapIntegerDouble neighborMap = new HashMapIntegerDouble();
        HashMapIntegerDouble.Iterator nghbrIt;
        HashMapIntegerDouble.Entry nghbrEntry;
        for (Map.Entry<Integer, ArrayListInt> layerPnts : layerSortedPoints.entrySet()) {
            // get layer index and corresponding list of point indices and set the
            // grid elements for this layer and loop over all points

            layer = layerPnts.getKey();
            ArrayListInt pntList = layerPnts.getValue();
            modelSource.setGridNodeElementNeighbors(layer);

            // loop over all points of the current layer

            for (int pnt = 0; pnt < pntList.size(); ++pnt) {
                // get the point index (gn) ... if surface 0 has a NaN here they all
                // do so don't smooth

                gn = pntList.get(pnt);
                indx = attrMapList.get(0).getAttributeIndex(totalAttr);
                if (!modelSource.isNaN(gn, indx)) {
                    // gn is valid ... initialize distance limit counts to 0.0

                    for (Map.Entry<Integer, double[]> eSmth : distanceWeights.entrySet()) {
                        int p = eSmth.getKey();
                        double[] lmtCounts = nghbrDstLmtCounts[p];
                        lmtCounts[0] = lmtCounts[1] = lmtCounts[2] = lmtCounts[3] = 0.0;
                        double[] intrps = intrpArray.get(p);
                        double[] weights = intrpWeight.get(p);
                        for (int i = 0; i < intrps.length; ++i)
                            intrps[i] = weights[i] = 0.0;
                    }

                    if (!use3D) {
                        gnNghbrLayer = modelSource.getPointLayerId(gn);
                        gnNghbrRadius = modelSource.getPointRadius(gn);
                    }

                    // get its neighbors and loop over each

                    getNeighborVertices(gn, maxDist, modelSource, neighborMap, use3D);
                    aSmoothNeighbors.add(neighborMap.size());
                    nghbrIt = neighborMap.iterator();
                    while (nghbrIt.hasNext()) {
                        // get the neighbor and associated distance to gn

                        nghbrEntry = nghbrIt.nextEntry();
                        gnNghbr = nghbrEntry.getKey();
                        dst = nghbrEntry.getValue();

                        // loop over each smoothing component

                        for (Map.Entry<Integer, double[]> eSmth : distanceWeights.entrySet()) {
                            // get smoothing component index and distance limits (D0 and D1)

                            int p = eSmth.getKey();
                            double[] dlmts = eSmth.getValue();

                            // get the non-smoothed attribute indices for all surfaces, the
                            // interpolation result and weight storage arrays, and the
                            // statistical neighbor limits and statistic objects.

                            int[] surfs = nonSmthAttrIndex.get(p);
                            double[] intrps = intrpArray.get(p);
                            double[] weights = intrpWeight.get(p);
                            double[] lmtCounts = nghbrDstLmtCounts[p];
                            double[] lmts = aDistNghbrLimits[p];

                            // increment limit counts

                            if (dst <= lmts[0])
                                ++lmtCounts[0];
                            else if (dst <= lmts[1])
                                ++lmtCounts[1];
                            else if (dst <= lmts[2])
                                ++lmtCounts[2];
                            else if (dst < lmts[3])
                                ++lmtCounts[3];

                            // loop over each surface

                            for (int k = 0; k < surfs.length; ++k) {
                                // get the neighbors point value ... only sum if it is not
                                // NaN

                                if (use3D)
                                    val = modelSource.getPointValue(gnNghbr, surfs[k]);
                                else
                                    val = modelSource.getValue(gnNghbr, gnNghbrLayer, surfs[k],
                                            gnNghbrRadius, InterpolatorType.LINEAR);

                                if (!Double.isNaN(val)) {
                                    // get the weight using the smoothing settings for the
                                    // pth parameter ... if the weight is larger than zero
                                    // sum the contribution to the interpolation and result
                                    // arrays for the kth surface

                                    w = smoothWeight(dst, dlmts[0], dlmts[1]);
                                    if (w > 0.0) {
                                        weights[k] += w;
                                        intrps[k] += val * w;
                                    }
                                }
                            } // end for (int k = 0; k < surfs.length; ++k)
                        } // end for (Map.Entry<Integer, int[]> eSmth:
                        //          nonSmthAttrIndex.entrySet())
                    } // end while (nghbrIt.hasNext())

                    // finished interpolation for point p ... store results ... loop over
                    // all attribute surfaces

                    for (int k = 0; k < attrMapList.size(); ++k) {
                        // set the total smoothed result to 0 and loop over all three
                        // uncertainty components

                        double smthTotl = 0.0;
                        for (int p = 0; p < 3; ++p) {
                            // see if the component p was smoothed or not

                            int[] surfs = smthAttrIndex.get(p);
                            if (surfs != null) {
                                // component p was smoothed ... get the interpolation and
                                // weight sums ... normalize the interpolated result with the
                                // wed and set into the model for this surface ... also sum the
                                // result to the total smoothed value (smthTotl)

                                double[] intrps = intrpArray.get(p);
                                double[] weights = intrpWeight.get(p);
                                if (weights[k] > 0.0) intrps[k] /= weights[k];
                                modelSource.setPointValue(gn, surfs[k], intrps[k]);
                                origVal = modelSource.getPointValue(gn, attrIndex[p][k]);
                                aSmoothResidual[p].add(intrps[k] - origVal);
                                smthTotl += intrps[k];

                                // update the statistics for distance neighbor counts

                                double[] lmtCounts = nghbrDstLmtCounts[p];
                                Statistic[] stats = aDistNeighbors[p];
                                stats[0].add(lmtCounts[0]);
                                stats[1].add(lmtCounts[1]);
                                stats[2].add(lmtCounts[2]);
                                stats[3].add(lmtCounts[3]);
                            } else if (ringWeights.contains(p)) {
                                // component p was not distance smoothed but it was ring
                                // smoothed ... get ring smoothed component value and increment
                                // the total smoothed result with it.

                                sindx = attrMapList.get(k).getAttributeIndex(smoothComponentAttr[p]);
                                smthTotl += modelSource.getPointValue(gn, sindx);
                            } else
                                // component p was not distance or ring smoothed ... get the
                                // non smoothed result and add to the total smoothing value.

                                smthTotl += modelSource.getPointValue(gn, attrIndex[p][k]);
                        } // end for (int p = 0; p < 3; ++p)

                        // done with surface k ... set the total smoothing result into the
                        // model

                        modelSource.setPointValue(gn, smthTotalAttrIndex[k], smthTotl);
                    } // end for (int k = 0; k < attrMapList.size(); ++k)
                } // end if (!modelSource.isNaN(gn, indx))
            } // end for (int pnt = 0; pnt < pntList.size(); ++pnt)

            // clear the layer grid node element neighbors and loop for the next layer

            modelSource.clearGridNodeElementNeighbors(layer);
        } // end for (Map.Entry<Integer, ArrayListInt> layerPnts:
        //          layerSortedPoints.entrySet())

        // done ... now output statistics

        for (Map.Entry<Integer, double[]> eSmth : distanceWeights.entrySet()) {
            int p = eSmth.getKey();
            Statistic rsdStat = aSmoothResidual[p];
            Statistic[] dstNghbrStat = aDistNeighbors[p];
            double[] lmts = aDistNghbrLimits[p];

            System.out.println(NL + "      Smoothed Component: " +
                    UncertaintyComponent.values()[p]);
            System.out.println("      " + Globals.repeat("*", 65));
            System.out.println("      Difference (Smooth - Original) Results:");
            String fmt = "%8d   %8.4f   %8.4f   %8.4f   %8.4f   %8.4f";
            System.out.println("         Points       Min        Max       " +
                    "Mean     Std.Dev.      RMS");
            System.out.println("        " + String.format(fmt, rsdStat.getCount(),
                    rsdStat.getMinimum(), rsdStat.getMaximum(),
                    rsdStat.getMean(), rsdStat.getStdDev(),
                    rsdStat.getRMS()));

            System.out.println("");
            System.out.println("      Neighbor Count Results:");
            fmt = "%8d   %8.4f   %8.4f   %8.4f   %8.4f   %8.4f";
            System.out.println("         Points       Min        Max       " +
                    "Mean     Std.Dev.      RMS");
            System.out.println("        " + String.format(fmt,
                    aSmoothNeighbors.getCount(),
                    aSmoothNeighbors.getMinimum(),
                    aSmoothNeighbors.getMaximum(),
                    aSmoothNeighbors.getMean(),
                    aSmoothNeighbors.getStdDev(),
                    aSmoothNeighbors.getRMS()));

            double c = KM_PER_DEG;
            if (!use3D) c = Math.PI / 180.0;
            System.out.println("");
            System.out.println("      Neighbor Count Results Binned (Limit in Degrees):");
            System.out.println("        Limit (<=)   Min        Max       " +
                    "Mean     Std.Dev.      RMS");
            fmt = "%6.2f   %8.4f   %8.4f   %8.4f   %8.4f   %8.4f";
            for (int i = 0; i < 4; ++i)
                System.out.println("        " + String.format(fmt, lmts[i] / c,
                        dstNghbrStat[i].getMinimum(),
                        dstNghbrStat[i].getMaximum(),
                        dstNghbrStat[i].getMean(),
                        dstNghbrStat[i].getStdDev(),
                        dstNghbrStat[i].getRMS()));
            System.out.println("      " + Globals.repeat("*", 65));
            System.out.println("");
        }
    }

    /**
     * Returns a 2D array of the average attribute values at each ring index for
     * the input point index (double[attribute][ring]). The attribute results are
     * interpolated for each neighbor point of the input point using the input
     * ModelInterface sourceModel. The interpolations are stored in a point
     * interpolation map (pointIntrpMap) so that they can be reused later. All
     * valid (non NaN) results are summed and averaged for each ring index (0, 1,
     * ... nrings). A point ring map (pointRingMap) is updated with the result
     * before exiting so that it can be found without rebuilding it at the next
     * request.
     * <p>
     * The result returned by this function is used to interpolate a smoothed
     * value on each source point of the source model (sourceModel).
     *
     * @param pointIndex        The point index whose ring map will be returned.
     * @param nRingsMax         The maximum number of rings away from the input
     *                          point to be used for smoothing. Not all components
     *                          to be smoothed will use this many but none will
     *                          exceed it. Note: The input point is ring 0.
     * @param pointRingNghbrMap The result is stored in this map associated with the
     *                          input  point index (pointIndex) so that it can be
     *                          resused later.
     * @param sourceModel       The model with values used to interpolate results
     *                          into the point interpolation map (pointIntrpMap).
     * @param tomoModel         The model for which pointIndex is a member and for
     *                          which neighbors are discovered to assemble the output
     *                          ring map.
     * @param cmpntAttrIndex    A map of non-smoothed component indices (values)
     *                          defined for each component to be smoothed (key).
     *                          The keys can be one or more of 0, 1, or 2 where
     *                          0) TT_VARIANCE_DIAGONAL,
     *                          1) TT_VARIANCE_OFF_DIAGONAL, and
     *                          2) TT_VARIANCE_NON_REPRESENTED.
     *                          The values for each key is an array of surface
     *                          attribute indexes that are to be smoothed.
     *                          HashMap<smooth attribute index, int[surface]>
     * @param intrpArray        A map of surface interpolation values defined for
     *                          each component to be smoothed. The interpolation
     *                          values are defined for each surface to be smoothed.
     *                          They are interpolated and set consecutively for each
     *                          ring neighbor discovered for the input point. The
     *                          results are used locally only.
     *                          HashMap<smooth attribute index, double[surface]>
     * @param ringWeights       A map of all ring weights defined independently for
     *                          each surface to be smoothed. This array contains the
     *                          relative smoothing weight assigned for a specific
     *                          ring.
     *                          HashMap<smooth attribute index, double[ring]>
     * @param ringSum           A map of all surface/ring average interpolation
     *                          results for each component to be smoothed. The value
     *                          of each entry in this map is a double array (indexed
     *                          as [ring][surface]). It represents the average
     *                          interpolated value from all nodes belonging to a
     *                          specific ring index for a specific surface index
     *                          relative to the input point index (pointIndex).
     *                          HashMap<smooth attribute index, double[ring][surface]>
     * @param ringCnt           A map of all surface/ring interpolation contribution
     *                          counts for each component to be smoothed. The value
     *                          of each entry in this map is an int array (indexed
     *                          as [ring][surface]). It represents the number of
     *                          neighbors contributing to the interpolation for a
     *                          specific ring index for a specific surface index
     *                          relative to the input point index (pointIndex).
     *                          HashMap<smooth attribute index, int[ring][surface]>
     * @throws GMPException
     * @throws IOException
     * @throws GeoTessException
     */
    private int getPointRingWeightedInterpolation(int pointIndex, int nRingsMax,
                                                  HashMapIntegerKey<HashMapIntegerInteger> pointRingNghbrMap,
                                                  ModelInterface sourceModel,
                                                  ModelInterface tomoModel,
                                                  HashMap<Integer, int[]> cmpntAttrIndex,
                                                  HashMap<Integer, double[]> intrpArray,
                                                  HashMap<Integer, double[]> ringWeights,
                                                  HashMap<Integer, double[][]> ringSum,
                                                  HashMap<Integer, int[][]> ringCnt,
                                                  HashMap<Integer, int[]> ringNghbrCnt,
                                                  HashMap<Integer, double[]> ringNghbrDst,
                                                  GeoVector inflPosition, double inflWght)
            throws GMPException, GeoTessException, IOException {
        GeoVector nghbrPosition = new GeoVector();

        // zero the ring sum and ring count

        for (Map.Entry<Integer, double[][]> e : ringSum.entrySet()) {
            double[][] rsp = e.getValue();
            int[][] rcp = ringCnt.get(e.getKey());
            for (int r = 0; r < rsp.length; ++r) {
                double[] rspr = rsp[r];
                int[] rcpr = rcp[r];
                for (int k = 0; k < rspr.length; ++k) {
                    rspr[k] = 0.0;
                    rcpr[k] = 0;
                }
            }
        }

        // get the ringMap at the input point index and loop over each neighbor

        HashMapIntegerInteger ringMap = pointRingNghbrMap.get(pointIndex);
        if (ringMap == null)
            ringMap = getRingNeighbors(pointIndex, nRingsMax,
                    tomoModel, pointRingNghbrMap);

        // create temporary arrays to hold ring counts and distances ... loop over
        // each ring neighbor

        HashMapIntegerInteger.Iterator it = ringMap.iterator();
        int[] count = new int[nRingsMax + 1];
        double[] dist = new double[nRingsMax + 1];
        while (it.hasNext()) {
            // get point index -> ring number association of the next neighbor to
            // pointIndex

            HashMapIntegerInteger.Entry e = it.nextEntry();
            int nghbrPntIndx = e.getKey();
            int ring = e.getValue();

            // update ring count and distance to influence point

            ++count[ring];
            nghbrPosition.setGeoVector(tomoModel.getPointUnitVector(nghbrPntIndx),
                    tomoModel.getPointRadius(nghbrPntIndx));
            dist[ring] += inflPosition.distance3D(nghbrPosition);

            // get interpolation result at neighbor point index. The neighbor point
            // index represents a point on the tomography model. We want to
            // interpolate that position on the sourceModel.

            getPointInterpolationMap(nghbrPntIndx, tomoModel, sourceModel,
                    cmpntAttrIndex, intrpArray);

            // loop over each attribute to be smoothed

            for (Map.Entry<Integer, double[]> ec : intrpArray.entrySet()) {
                // get associated interpolation array, ring array, and ring count arrays
                int p = ec.getKey();
                double[] intrps = ec.getValue();
                double[][] rsp = ringSum.get(p);
                int[][] rcp = ringCnt.get(p);

                // make sure this ring array has more entries than the current ring
                // index.

                if (rsp.length > ring) {
                    // ok ... get the surface array and count array for this ring
                    // and loop over all surfaces

                    double[] rspr = rsp[ring];
                    int[] rcpr = rcp[ring];
                    for (int k = 0; k < intrps.length; ++k) {
                        // if the interpolation is not a NaN then sum the value to the
                        // surface ring and increment the count

                        if (!Double.isNaN(intrps[k])) {
                            rspr[k] += intrps[k];
                            ++rcpr[k];
                        }
                    }
                }
            }
        } // end while (it.hasNext())

        // reduce ringSum by ringCnt to get average result for each surface of
        // each attribute to be smoothed and then sum the ring/surface result times
        // the ray weight to obtain a final value for each surface at the point
        // use the input intrpArray to store the results for return to the caller.

        for (Map.Entry<Integer, double[]> ec : intrpArray.entrySet()) {
            // get the interpolation result array, ring weight array, ring neighbor
            // sum array and ring neighbor contribution count arrays ... initialize
            // the interpolation result array to zero for preparation to store the
            // returned results

            int p = ec.getKey();
            double[] intrp = ec.getValue();
            double[] rngW = ringWeights.get(p);
            double[][] rsp = ringSum.get(p);
            int[][] rcp = ringCnt.get(p);
            int[] rncp = ringNghbrCnt.get(p);
            double[] rndp = ringNghbrDst.get(p);
            for (int k = 0; k < intrp.length; ++k) intrp[k] = 0.0;

            // loop over each ring entry for this smoothing parameter

            for (int r = 0; r < rsp.length; ++r) {
                // get the ring surface array and ring count surface array and the
                // ray weight for this ring and loop over all surfaces

                double[] rspr = rsp[r];
                int[] rcpr = rcp[r];
                double rw = rngW[r];
                rncp[r] += inflWght * count[r];
                rndp[r] += inflWght * dist[r] / count[r] / KM_PER_DEG;
                for (int k = 0; k < rspr.length; ++k) {
                    // if more than one entry defines this surface for this ring then
                    // normalize it to get the average ... sum that average times the
                    // ring weight to the interpolation result array

                    if (rcpr[k] > 1) rspr[k] /= rcpr[k];
                    intrp[k] += rw * rspr[k];
                }
            }
        } // end for (Map.Entry<Integer, double[]> ec: intrpArray.entrySet())

        return ringMap.size();
    }

    /**
     * This returns ring neighbors (out to nrings) for the input pointIndex of
     * the input ModelInterface mdl. The neighbors are returned in a hashmap of
     * of pointIndex associated with ring number. The input point index is set
     * to ring 0 and all other neighbors are associated with their ring index
     * (1 to nrings).
     *
     * @param pi0    The index whose neighbor ring map will be returned.
     * @param nrings The number of rings to return in the map (must be 1 or larger).
     * @param mdl    The model from which pi0 is defined for which neighbors will
     *               be returned.
     * @return The map of point index in mdl associated with their ring
     * value about pi0.
     * @throws GMPException
     */
    private HashMapIntegerInteger getRingNeighbors(int pi0, int nrings,
                                                   ModelInterface mdl,
                                                   HashMapIntegerKey<HashMapIntegerInteger> ringNghbrMap)
            throws GMPException {
        // make ring map to hold neighbors and add pi0 associated with ring 0

        HashMapIntegerInteger ringMap = new HashMapIntegerInteger();
        ringMap.put(pi0, 0);

        // create two neighbor lists to contain the current ring and the next ring
        // the current ring removes points (assigned from the previous ring) and
        // retrieves their neighgbors to be placed in the next ring container ...
        // add pi0 to the current ring and loop over all rings.

        ArrayListInt currNghbrList = new ArrayListInt();
        ArrayListInt nextNghbrList = new ArrayListInt();
        currNghbrList.add(pi0);
        for (int i = 1; i <= nrings; ++i) {
            // retrieve each neighbor from the previous ring association and find
            // their neighbors

            for (int j = 0; j < currNghbrList.size(); ++j) {
                // get next node and find neighbors ... loop over each

                int currNode = currNghbrList.get(j);
                HashSet<Integer> nghbrSet = null;
                try {
                    nghbrSet = mdl.getPointNeighbors(currNode);
                } catch (GMPException ex) {
                    ex.printStackTrace();
                }
                for (Integer pi : nghbrSet) {
                    // get next neighbor and test to see if it is not already contained
                    // in the map

                    if (!ringMap.contains(pi)) {
                        // not in map ... add it to the map and to the next neighbor list

                        ringMap.put(pi, i);
                        nextNghbrList.add(pi);
                    }
                }
            }

            // done ... clear the current list and swap lists. Now continue processing
            // the next ring

            currNghbrList.clear();
            ArrayListInt tmp = nextNghbrList;
            nextNghbrList = currNghbrList;
            currNghbrList = tmp;
        }

        // done ... return ring map

        ringNghbrMap.put(pi0, ringMap);
        return ringMap;
    }

    // need component indexes for each smoothed phase (p) and all surfaces
    // HashMap<p, surfaces> = HashMap<Integer, int[]>

    private void getPointInterpolationMap(int pointIndex,
                                          ModelInterface tomoModel,
                                          ModelInterface sourceModel,
                                          HashMap<Integer, int[]> cmpntAttrIndex,
                                          HashMap<Integer, double[]> intrpValues)
            throws GMPException, GeoTessException, IOException {
        double[] u = tomoModel.getPointUnitVector(pointIndex);
        double r = tomoModel.getPointRadius(pointIndex);

        // ... perform interpolations here
//    if (sourceModel instanceof GeoModelUUL)
//    {
//      GeoVector gv = new GeoVector(u, r);
//      InterpolatedNodeLayered inl;
//      inl = ((GeoModelUUL) sourceModel).getInterpolatedNodeLayered(gv);
//      for (Map.Entry<Integer, int[]> e: cmpntAttrIndex.entrySet())
//      {
//        int p = e.getKey();
//        int[] attrIndex = e.getValue();
//        double[] intrps = intrpValues.get(p);
//        for (int i = 0; i < attrIndex.length; ++i)
//          intrps[i] = inl.getValue(attrIndex[i]);
//      }
//    }
        if (sourceModel instanceof GeoTessModel) {
            aGeoTessPosition.set(u, r);
            for (Map.Entry<Integer, int[]> e : cmpntAttrIndex.entrySet()) {
                int p = e.getKey();
                int[] attrIndex = e.getValue();
                double[] intrps = intrpValues.get(p);
                for (int i = 0; i < attrIndex.length; ++i)
                    intrps[i] = aGeoTessPosition.getValue(attrIndex[i]);
            }
        } else {
            String s = "Input ModelInterface is not a GeoModel or a GeoTessModel ...";
            throw new IOException(s);
        }
    }

    /**
     * Finds all neighbors of point index pi0 in the input model (modelSource)
     * that are within distLimit (3D) of pi0. All discovered neighbors are
     * returned in a map of neighbor index associated with the distance.
     *
     * @param pi0         The input point index for which neighbors will be
     *                    discovered.
     * @param dstLimit    The distance limit, after which, neighbors are not
     *                    included in the returned map.
     * @param modelSource The source model from which neighbors of pi0 are
     *                    discovered.
     * @param nghbrMap    The map of all neighbors within distance distLimit of
     *                    pi0 associated with their evaluated distance.
     * @throws GMPException
     */
    private static void getNeighborVertices(int pi0, double dstLimit,
                                            ModelInterface modelSource,
                                            HashMapIntegerDouble nghbrMap,
                                            boolean use3D)
            throws GMPException {
        int pi0v = 0;
        HashSet<Integer> nghbrSet;
        double d;

        // create neighbor map that will hold all nodes found within (dstLimit) of
        // node gn0 ... add gn0 (associated with distance 0.0) to map

        //HashMap<Integer, Double> nghbrMap = new HashMap<Integer, Double>();
        nghbrMap.clear();
        if (use3D)
            nghbrMap.put(pi0, 0.0);
        else {
            pi0v = modelSource.getPointVertexId(pi0);
            nghbrMap.put(pi0v, 0.0);
        }

        // create a temporary stack of discovered neighbors whose neighbors will be
        // checked for inclusion in the final map to be returned ... add gn0 to list
        // and loop until list is empty

        ArrayListInt nghbrList = new ArrayListInt();
        nghbrList.add(pi0);
        while (nghbrList.size() > 0) {
            // remove next node (last node on list) from list and get its neighbor
            // node set ... loop over each neighbor and check for inclusion in the
            // final set

            int currNode = nghbrList.remove(nghbrList.size() - 1);
            if (use3D) {
                nghbrSet = modelSource.getPointNeighbors(currNode);
            } else {
                int vertexIndex = modelSource.getPointVertexId(currNode);
                int layerIndex = modelSource.getPointLayerId(currNode);
                nghbrSet = modelSource.getVertexNeighbors(vertexIndex, layerIndex);
            }

            for (Integer pi : nghbrSet) {
                // make sure neighbor is not already in the final map
                if (!nghbrMap.contains(pi)) {
                    // get distance of neighbor to gn0 and see if distance is less than
                    // limit (dstLimit).

                    // get distance between pi0 and pi

                    if (use3D)
                        d = modelSource.getDistance3D(pi0, pi);
                    else
                        d = modelSource.getDistance(pi0v, pi);

                    if (d < dstLimit) {
                        // neighbor not yet in map but within limit distance of gn0 ... add
                        // neighbor to list and to final map

                        nghbrList.add(pi);
                        nghbrMap.put(pi, d);
                    }
                }
            }
        }
    }

    /**
     * Simple cubic spline smoothing weight. For input distances (dst) < d0 the
     * weight is 1.0. For values > d1 the weight is 0.0. Otherwise, a cubic
     * interpolation is performed to obtain the weight where the cubic splines
     * slope is 0.0 at d0 and d1, and its value is 1.0 at d0 and 0.0 at d1.
     *
     * @param dst The input distance for which the weight will be returned.
     * @param d0  The distance within which the weight is 1.0.
     * @param d1  The distance beyond which the weight is 0.0.
     * @return The weight evaluated at the input distance (dst).
     */
    private static double smoothWeight(double dst, double d0, double d1) {
        double w = 1.0;
        if (dst > d1)
            w = 0.0;
        else if (dst > d0) {
            double dw = 1.0 - (dst - d0) / (d1 - d0);
            w = (1.0 + 3.0 * (1.0 - dw)) * dw * dw * dw; // quartic
            //w = (1.0 + 2.0 * (1.0 - dw)) * dw * dw;    // cubic
        }
        return w;
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
     * @param phase The phase for the new attribute surface.
     * @param rcvrA The receiver A for the new attribute surface.
     * @param rcvrB The receiver B for the new attribute surface.
     * @param ai    The attribute index entry owned by some source
     *              model.
     * @throws IOException
     */
    public void addNewAttributeSurface(SeismicPhase phase,
                                       Receiver rcvrA, Receiver rcvrB,
                                       AttributeIndexerSmart ai,
                                       ArrayList<SiteAttributeMap> samList,
                                       HashMap<SeismicPhase, HashMap<Receiver,
                                               HashMap<Receiver,
                                                       SiteAttributeMap>>> surfMap,
                                       boolean[] smoothFlag)
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

                    sam = new SiteAttributeMap(phase, rcvrA, rcvrB);
                    phaseRcvrAMap.put(rcvrB, sam);
                }
            } else {
                // receiver A was not found for phase map ... create and add
                // first entry for phase

                sam = new SiteAttributeMap(phase, rcvrA, rcvrB);
                phaseRcvrAMap = new HashMap<Receiver, SiteAttributeMap>();
                phaseRcvrAMap.put(rcvrB, sam);
                phaseMap.put(rcvrA, phaseRcvrAMap);
            }
        } else {
            // phase was not found ... add first entry for phase

            sam = new SiteAttributeMap(phase, rcvrA, rcvrB);
            phaseRcvrAMap = new HashMap<Receiver, SiteAttributeMap>();
            phaseRcvrAMap.put(rcvrB, sam);
            phaseMap = new HashMap<Receiver, HashMap<Receiver, SiteAttributeMap>>();
            phaseMap.put(rcvrA, phaseRcvrAMap);
            surfMap.put(phase, phaseMap);
        }

        // update attribute indexer with the new SiteAttributeMap

        addAttribute(phase, rcvrA, rcvrB, sam, samList, ai, smoothFlag);
    }

    /**
     * Adds the input SiteAttributeMap into the input attribute index entries
     * attribute map list and sets the source GeoModel attribute indexer with all
     * applicable GeoAttribute index settings.
     *
     * @param phase The phase associated with the input
     *              SiteAttributeMap.
     * @param rA    The receiver A associated with the input
     *              SiteAttributeMap.
     * @param rB    The receiver B associated with the input
     *              SiteAttributeMap.
     * @param sam   The new SiteAttributeMap.
     * @param ai    A models AttributeIndex containers.
     * @throws IOException
     */
    private static void addAttribute(SeismicPhase phase, Receiver rA, Receiver rB,
                                     SiteAttributeMap sam,
                                     ArrayList<SiteAttributeMap> samList,
                                     AttributeIndexerSmart ai,
                                     boolean[] smoothFlag)
            throws IOException {
        // add SiteAttributeMap to map list and update all pertinent GeoAttributes

        samList.add(sam);

        // set all pertinent non-smoothed attribute indexes into the
        // AttributeIndexer and the input SiteAttributeMap

        setAttribute(phase, rA, rB, sam, ai,
                GeoAttributes.TT_MODEL_VARIANCE);
        setAttribute(phase, rA, rB, sam, ai,
                GeoAttributes.TT_MODEL_VARIANCE_DIAGONAL);
        setAttribute(phase, rA, rB, sam, ai,
                GeoAttributes.TT_MODEL_VARIANCE_OFFDIAGONAL);
        setAttribute(phase, rA, rB, sam, ai,
                GeoAttributes.TT_MODEL_VARIANCE_NONREPRESENTED);
        setAttribute(phase, rA, rB, sam, ai,
                GeoAttributes.TT_MODEL_VARIANCE_REPRESENTED_FRACTION);

        // set all requested smoothing attribute indexes into the AttributeIndexer
        // and the associated SiteAttributeMap

        if (smoothFlag[0] || smoothFlag[1] ||
                smoothFlag[2])
            setAttribute(phase, rA, rB, sam, ai,
                    GeoAttributes.TT_MODEL_VARIANCE_SMOOTHED);
        if (smoothFlag[0])
            setAttribute(phase, rA, rB, sam, ai,
                    GeoAttributes.TT_MODEL_VARIANCE_DIAGONAL_SMOOTHED);
        if (smoothFlag[1])
            setAttribute(phase, rA, rB, sam, ai,
                    GeoAttributes.TT_MODEL_VARIANCE_OFFDIAGONAL_SMOOTHED);
        if (smoothFlag[2])
            setAttribute(phase, rA, rB, sam, ai,
                    GeoAttributes.TT_MODEL_VARIANCE_NONREPRESENTED_SMOOTHED);
    }

    /**
     * Adds the GeoAttribute ga into the input attribute indexer and its index
     * in the attribute indexer into the SiteAttributeMap's attribute map.
     *
     * @param phase The phase of the attribute surface.
     * @param rA    The receiver A of the attribute surface.
     * @param rB    The receiver B of the attribute surface.
     * @param sam   The SiteAttributeMap associated with the surface.
     * @param ai    The AttributeIndexer into which the GeoAttribute entry is
     *              added.
     * @param ga    The GeoAttribute to be added.
     * @throws IOException
     */
    private static void setAttribute(SeismicPhase phase, Receiver rA, Receiver rB,
                                     SiteAttributeMap sam, AttributeIndexerSmart ai,
                                     GeoAttributes ga)
            throws IOException {
        sam.putAttributeIndex(ga, ai.size());
        ai.addEntry(rA, rB, phase, ga);
    }
}
