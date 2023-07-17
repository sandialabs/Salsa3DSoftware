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

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.log10;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.baseobjects.observation.ObservationComponent;
import gov.sandia.gmp.locoo3d.EventList.CorrelationMethod;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.brents.Brents;
import gov.sandia.gmp.util.numerical.brents.BrentsFunction;
import gov.sandia.gmp.util.numerical.matrix.CholeskyDecomposition;
import gov.sandia.gmp.util.numerical.matrix.LUDecomposition;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origerr;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AzgapExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_custom.Azgap;

/**
 * <p>
 * Title: LocOO
 * </p>
 * 
 * <p>
 * Description: Seismic Event Locator
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Event implements BrentsFunction, Serializable
{
    private EventParameters parameters;

    protected ScreenWriterOutput logger, errorlog;

    /**
     * Used to store info about observations whose defining status
     * is changed at the start of a location calculation so it can 
     * be output right after the observation table.
     */
    private StringBuffer observationStatus = new StringBuffer();

    /**
     * inputLocation is the original location specified by the 
     * calling application.
     */
    private Location inputLocation;

    /**
     * initialLocation is the location that is to be used 
     * as the starting location for the calculation.
     */
    private Location initialLocation;

    private LocatorResults locatorResults;

    /**
     * All the observationComponents of type TT, AZ, SH, including defining, non-defining, invalids, etc.
     * These are references to the same observationComponents that are owned by Observation objects.
     */
    private ArrayList<ObservationComponent> obsComponents = new ArrayList<ObservationComponent>();

    /**
     * the set of all observationComponents that are defining and have valid predictions.
     */
    private ArrayList<ObservationComponent> definingVec = new ArrayList<ObservationComponent>();

    /**
     * Map from arid to weightedResiduals of TT, AZ, SH, in that order.
     * Value is Globals.NA_VALUE for invalid observations/predictions.
     * For this to have been computed, property io_nondefining_residuals must
     * be true, which is the default.
     */
    private HashMap<Long, double[]> weightedResiduals = new HashMap<Long, double[]>();

    private Source source;

    protected double[] dloc = new double[4];

    protected double dkm;

    private StringBuffer iterationTable = null;

    private boolean definingChanged;
    // true if the list of defining observations
    // (definingVec) changed the last time
    // update was called.

    private double sumSqrWeightedResiduals;
    // the sum squared weighted residuals
    // of observations in the working set.

    protected boolean positionUpToDate;
    // gets set to false when the event
    // location upon which predictions
    // are based is moved. Set to
    // true in the update() method,
    // where values that depend on the
    // predictions are recalculated.

    private boolean originTimeUpToDate;
    // gets set to false when the origin
    // time upon which time residuals
    // are based is moved. Set to
    // true in the update() method,
    // where values that depend on the
    // predictions are recalculated.

    protected double applied_damping;
    // The amount of damping actually being applied during each
    // iteration.  This is lambda in lsq_algorith.pdf Section 5.

    protected double lsq_convergence_value;
    // The convergence value in the current iteration.  This is
    // abs(new_sswr/old_sswr - 1).

    // number of times sum squared weighted residuals are computed
    private int nSSWR = 0;

    /**
     * Amount of time in nanoseconds spent computing predictions in 
     * method update
     */
    private long predictionTime;

    private boolean[] fixed;

    /**
     * If user specified property gen_fix_depth = floating point number
     * fixedDepthValue is the number. Otherwise NaN.
     */
    protected double fixedDepthValue;

    /**
     * If user specified property gen_fix_depth = topo, then fixedDepthIndex
     * will be zero.  If during a free depth calculation depth is out of range,
     * then fixedDepthIndex will be set to 0 if event.depth < minDepth constraint,
     * or set to 1 it event.depth > maxDepth constraint.
     */
    protected int fixedDepthIndex;

    private ArrayListDouble tBrent;

    protected int jdate;

    /**
     * Used to process correlated observations
     */
    private double[][] sigma;

    /**
     * Records the the track of the interim positions computed
     * during a location calculation.
     */
    protected ArrayList<Location> locationTrack;

    private Map<String, double[]> masterEventCorrections;

    /**
     * 
     * @param params
     * @param phasePredictorMap 
     * @param source
     *            Origin
     * @param masterEventCorrections 
     * @throws Exception 
     * @throws LocOOException 
     */
    public Event(EventParameters params, HashMap<SeismicPhase, Predictor> phasePredictorMap, Source source,
	    Map<String, double[]> masterEventCorrections) throws Exception
    {
	this.parameters = params;

	this.source = source;
	
	this.masterEventCorrections = masterEventCorrections;
	
	this.source.setLog(params.outputLog());
	this.source.setErrorLog(params.errorLog());
	
	this.logger = params.outputLog();
	this.errorlog = params.errorLog();

	this.inputLocation = this.getLocation();

	if (parameters.initialLocationMethod().startsWith("properties"))
	    initialLocation = parameters.parFileLocation();
	else if (parameters.initialLocationMethod().startsWith("data"))
	    initialLocation = inputLocation.clone();

	fixed = parameters.fixed();
	fixedDepthValue = parameters.fixedDepthValue();
	fixedDepthIndex = parameters.fixedDepthIndex();

	source.setFixed(fixed);

	positionUpToDate = false;
	originTimeUpToDate = false;
	definingChanged = true;

	this.jdate = source.getJDate();

	source.needDerivatives(parameters.needDerivatives());
	source.useTTModelUncertainty(parameters.useTTModelUncertainty());
	source.useAzModelUncertainty(parameters.useAzModelUncertainty());
	source.useShModelUncertainty(parameters.useShModelUncertainty());
	source.useTTPathCorrections(parameters.useTTPathCorrections());
	source.useAzPathCorrections(parameters.useAzPathCorrections());
	source.useShPathCorrections(parameters.useShPathCorrections());

	for (Observation obs : source.getObservations().values())
	{
	    if (obs.getReceiver() == null)
		errorlog.writeln(String.format(
			"%nIgnoring observation that has no receiver associated with it. "
				+"orid %d, evid %d, arid %d, receiverId %d%n",
				source.getSourceId(), source.getEvid(),
				obs.getObservationId(), obs.getReceiver().getReceiverId()));
	    else if (obs.getPhase() == SeismicPhase.NULL)
		errorlog.writeln(String.format(
			"%nIgnoring observation with unrecognized phase %s. "
				+"orid %d, evid %d, arid %d, receiverId %d%n",
				obs.getPhase().toString(),
				source.getSourceId(), source.getEvid(),
				obs.getObservationId(), obs.getReceiver().getReceiverId()));
	    else
	    {
		Predictor predictor = phasePredictorMap.get(obs.getPhase());
		if (predictor == null)
		{
		    predictor = parameters.predictorFactory().getPredictor(obs.getPhase());
		    phasePredictorMap.put(obs.getPhase(), predictor);
		}
		if (predictor != null) {
		    obs.setPredictorName(predictor.getPredictorName());
		    
		    LibCorr3DModel model = predictor.getLibcorr3d().getModel(obs.getReceiver(), obs.getPhase().toString(), "TT");
		    if (model != null)
			obs.setModelName(model.getVmodel());
		    else
			obs.setModelName(predictor.getModelName());
		}
		obsComponents.addAll(obs.getObservationComponents().values());
	    }
	}
    }

    /** 
     * Retrieve jdate of this event
     */
    public int getJDate()
    {
	return jdate;
    }

    public void setJDate(int jdate)
    {
	this.jdate = jdate;
    }

    public Location getInitialLocation()
    {
	return initialLocation;
    }

    public LocatorResults getLocatorResults()
    {
	return locatorResults;
    }

    public void setLocatorResults(LocatorResults lr) throws Exception
    {
	source.setAlgorithm(getEventParameters().getAlgorithm());
	source.setAuthor(getEventParameters().getAuthor());

	if (lr != null) {
	    this.locatorResults = lr;
	    source.setSdobs(lr.getOrigErrSdobs());
	    source.setAzgap(lr.getAzgapRow());
	    source.setHyperEllipse(lr.getHyperEllipse());
	    source.setPredictionTime(predictionTime*1e-9);
	    source.setNIterations(lr.getNIterations());
	    source.setNFunc(lr.getNFunc());
	}
    }

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Checks each defining observation to see if
    // 1) the observation comes from a station that is a member of
    // eDefiningStations
    // 2) The phase of the observation is a member of eDifiningPhases
    // 3) The observation uncertainty is >= 0.
    // 4) the predictor can support predicted values for that particular
    // station/phase.
    //
    // If any of those conditions is false, the observation is made
    // non-defining.
    //
    // INPUT ARGS: NONE
    // OUTPUT ARGS: NONE
    // RETURN: NONE
    //
    // *****************************************************************************
    public void checkStationsAndPhases() throws Exception
    {
	observationStatus.setLength(0);

	// check each ObservationComponent to see if its defining status needs to change
	// due to parameters specified in par file.
	for (ObservationComponent obsComponent : obsComponents)
	{
	    if (!parameters.definingAttributes().contains(obsComponent.getObsType())
		    || !parameters.definingPhases().contains(obsComponent.getPhase())
		    || (!parameters.definingStations().isEmpty() && !parameters.definingStations()
			    .contains(obsComponent.getObservation().getReceiver().getSta()))
		    )
		obsComponent.setDefiningOriginal(false);

	    // if an observation filter wants to change status, do it.
	    if (parameters.observationFilter().apply(obsComponent))
		observationStatus.append(String.format(
			"Observation %s defining status changed to %s by an ObservationFilter.%n",
			obsComponent.toString(),
			(obsComponent.isDefining() ? "defining" : "non-defining")));

	    if (obsComponent.isDefining() && !obsComponent.isSupported())
	    {
		obsComponent.setDefiningOriginal(false);

		String msg = String.format(
			"Observation %s set non-defining because it is not supported by predictor %s.%n",
			obsComponent.toString(), obsComponent.getObservation().getPredictorName());
		observationStatus.append(msg);
		errorlog.writeln(msg);
	    }

	    if (obsComponent.isDefining() && !obsComponent.isValid())
	    {
		obsComponent.setDefiningOriginal(false);
		String msg = String.format(
			"Observation %s set non-defining because uncertainty is negative.%n",
			obsComponent.toString());
		observationStatus.append(msg);
		errorlog.writeln(msg);
	    }

	    if (obsComponent.isDefining() && Math.abs(obsComponent.getReceiver().getDepth()) > 10)
	    {
		obsComponent.setDefiningOriginal(false);
		String msg = String.format(
			"Observation %s set non-defining because receiver elevation is %1.3f km.%n",
			obsComponent.toString(), -obsComponent.getReceiver().getDepth());
		observationStatus.append(msg);
		errorlog.writeln(msg);
	    }
	}

	// sort the observation ArrayList by station, phase, type.
	// type should be in order tt, az, sh.
	Map<String, ObservationComponent> sortedSet = 
		new TreeMap<String, ObservationComponent>();
	for (ObservationComponent obs : obsComponents)
	    sortedSet.put(String.format("%s_%s_%d_%012d", obs.getObservation().getReceiver()
		    .getSta(), obs.getPhase().toString(), obs.getObsType().ordinal(), 
		    obs.getObservationid()), obs);

	// now repopulate the observation ArrayList with the sorted
	// observations.  
	obsComponents.clear();	
	obsComponents.addAll(sortedSet.values());
    }

    public ArrayList<ObservationComponent> getObsComponents() {
	return obsComponents;
    }

    public boolean[] getFixed() {
	return fixed;
    }

    public int nFree()
    {
	return 4 - (fixed[0] || fixed[1] ? 2 : 0) - (fixed[2] ? 1 : 0)
		- (fixed[3] ? 1 : 0);
    }

    boolean isFree(int i)
    {
	return !fixed[i];
    }

    boolean isFixed(int i)
    {
	return fixed[i];
    }

    boolean depthFixed()
    {
	return fixed[GMPGlobals.DEPTH];
    }

    boolean depthFree()
    {
	return !fixed[GMPGlobals.DEPTH];
    }

    /**
     * If depth is a free parameter, and the depth of this event is out of range, 
     * set depth fixed and return true
     * @return true if depth constraints are violated.
     * @throws Exception
     */
    protected boolean checkDepthConstraints() throws Exception
    {
	if (isFree(GMPGlobals.DEPTH))
	{
	    double duScale = parameters.depthConstraintUncertaintyScale();
	    double duOffset = parameters.depthConstraintUncertaintyOffset();
	    double depthUncertainty = locatorResults.getOrigErrSdepth();
	    if (depthUncertainty == Origerr.SDEPTH_NA && duScale > 0.)
		throw new Exception("depthUncertainty == GMPGlobals.ORIGERR_NA_VLAUE is not allowed here.");
	    double[] depthRange = parameters.getSeismicityDepthRange(getUnitVector());
	    String problem = "";
	    if (source.getDepth()+(depthUncertainty*duScale+duOffset) < depthRange[0])
	    {
		fixed[GMPGlobals.DEPTH] = true;
		fixedDepthIndex = 0;
		problem = String.format("%1.3f + (%1.3f * %1.3f + %1.3f) = %1.3f < %1.3f",
			source.getDepth(), depthUncertainty, duScale, duOffset, 
			source.getDepth()+(depthUncertainty*duScale+duOffset), 
			depthRange[0]);						
	    }
	    else if (source.getDepth()-(depthUncertainty*duScale+duOffset) > depthRange[1])
	    {
		fixed[GMPGlobals.DEPTH] = true;
		fixedDepthIndex = 1;
		problem = String.format("%1.3f - (%1.3f * %1.3f + %1.3f) = %1.3f > %1.3f",
			source.getDepth(), depthUncertainty, duScale, duOffset, 
			source.getDepth()-(depthUncertainty*duScale+duOffset), 
			depthRange[1]);						
	    }

	    if (fixed[GMPGlobals.DEPTH])
	    { 
		String e = String.format(
			    "WARNING:  Free depth solution is out of range for orid, evid = %1d, %1d.%n"
				    +"Free depth solution: %1.4f, %1.4f, %1.3f, %1.3f%n"
				    +"Depth of free depth solution: %1.3f +/- %1.3f%n"
				    +"depthConstraintUncertainy scale, offset: %1.3f, %1.3f%n"
				    +"Acceptable depth range: %1.3f to %1.3f km%n"
				    +"%s%n"
				    +"A fixed depth solution will be computed at %1.3f km%n"
				    +"%n",
				    source.getSourceId(), source.getEvid(), 
				    VectorGeo.getLatDegrees(getUnitVector()),
				    VectorGeo.getLonDegrees(getUnitVector()),
				    source.getDepth(), source.getOriginTime(),
				    source.getDepth(), depthUncertainty,
				    parameters.depthConstraintUncertaintyScale(),
				    parameters.depthConstraintUncertaintyOffset(),
				    depthRange[0], depthRange[1],
				    problem,
				    depthRange[fixedDepthIndex]
			    );
		
		if (logger.getVerbosity() >= 3)
		    logger.write(e);
		
		errorlog.write(e);
		
		return true;
	    }

	}
	return false;
    }

    int size()
    {
	return obsComponents.size();
    }

    // Returns the sum of the squared weighted residuals for defining
    // observations.
    double getSumSqrWeightedResiduals() throws Exception
    {
	checkStatus();
	return sumSqrWeightedResiduals;
    }

    // Returns the root-mean-squared weighted residuals for defining
    // observations.
    double rmsWeightedResidual() throws Exception
    {
	checkStatus();
	if (definingVec.size() == 0)
	    return 0.;
	return sqrt(sumSqrWeightedResiduals / definingVec.size());
    }

    /**
     * If upToDate is false, Event.update() is called which will update 
     * all the predictions.  Then upToDate is set to true.
     * @throws Exception
     */
    void checkStatus() throws Exception
    {
	if (!positionUpToDate || !originTimeUpToDate)
	    update();
    }

    public ArrayList<ObservationComponent> getDefiningVec() throws Exception
    {
	checkStatus();
	return definingVec;
    }

    boolean definingListChanged()
    {
	return definingChanged;
    }

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Clears the vector of observations. This frees the memory that was newed
    // by
    // the ObservationFactory to create the set of Observations
    //
    // INPUT ARGS: NONE
    // OUTPUT ARGS: NONE
    // RETURN: NONE
    //
    // *****************************************************************************
    void clear()
    {
	obsComponents.clear();
    } // END clear


    /**
     * Map from arid to weightedResiduals of TT, AZ, SH, in that order.
     * Value is Globals.NA_VALUE for invalid observations/predictions.
     * For this to have been computed, property io_nondefining_residuals must
     * be true, which is the default.
     */
    public HashMap<Long, double[]> getWeightedResiduals() { return weightedResiduals; }

    /**
     * This function gets called after a location has been calculated and we need
     * to compute residuals of all valid observations regardless of whether they
     * are defining or not.
     * @throws Exception
     * @throws LocOOException
     */
    void updateResiduals() throws Exception
    {
	if (!parameters.properties().getBoolean("io_nondefining_residuals", true))
	    return;

	boolean[] saveDefining = new boolean[obsComponents.size()];		
	for (int i=0; i<obsComponents.size(); ++i) 
	    saveDefining[i] = obsComponents.get(i).isDefining();

	for (ObservationComponent obs : obsComponents) 
	    obs.setDefining(obs.isSupported() && obs.getObserved() != Globals.NA_VALUE);

	for (Observation obs : source.getObservations().values())
	    obs.setRequestedAttributes(parameters.needDerivatives());

	// update the observations. This ensures that their residuals
	// reflect the current event position

	ArrayList<Prediction> results = parameters.predictorFactory().computePredictions(
		source.getObservations().values(), parameters.predictionsThreadPool());

	for(Prediction p : results) source.getObservations().get(p.getObservationId()).setPrediction(p);

	for (int i=0; i<obsComponents.size(); ++i) 
	    obsComponents.get(i).setDefining(saveDefining[i]);

	/*
	 * Map from arid to weightedResiduals of TT, AZ, SH, in that order.
	 * Value is Globals.NA_VALUE for invalid observations/predictions.
	 * For this to have been computed, property io_nondefining_residuals must
	 * be true, which is the default.
	 */
	weightedResiduals.clear();
	for (ObservationComponent obs : obsComponents)
	{
	    long arid = obs.getObservation().getObservationId();
	    double[] wr = weightedResiduals.get(arid);
	    if (wr == null)  { wr = new double[3]; weightedResiduals.put(arid, wr); }
	    switch (obs.getObsType())
	    {
	    case TRAVEL_TIME:
		wr[0] = obs.getWeightedResidual();
		break;
	    case AZIMUTH:
		wr[1] = obs.getWeightedResidual();
		break;
	    case SLOWNESS:
		wr[2] = obs.getWeightedResidual();
		break;
	    default:
		break;
	    }
	}
    }

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Update each observation and the sum squared weighted residuals.
    //
    void update() throws Exception
    {
	long timer = System.nanoTime();

	if (!positionUpToDate)
	{
	    // Event is derived from GeoVector.Location.Source.  Event stores a copy of 
	    // jdate because if might be used a lot.  When Event needs updating it gets
	    // a computed copy from Location.getJDate().
	    setJDate(source.getJDate());

	    // if any observations were originally defining but are now non-defining,
	    // and the number of times their status has been changed here from non-defining
	    // to defining is less than 10, change their status to defining.
	    // In other words, if a defining observation gets set to non-defining 10 times
	    // don't change it from non-defining to defining any more.
	    for (ObservationComponent obs : obsComponents) 
		if (obs.isDefiningOriginal() && !obs.isDefining() && obs.incFlipFlop() < parameters.observationFlipFlops())
		    obs.setDefining(true);

	    if (!masterEventCorrections.isEmpty() && parameters.masterEventUseOnlyStationsWithCorrections())
		for (ObservationComponent obs : obsComponents)
		    if (!masterEventCorrections.containsKey(String.format("%s/%s", 
			    obs.getReceiver().getSta(), obs.getPhase())))
			obs.setDefining(false);
			
	    for (Observation obs : source.getObservations().values())
		obs.setRequestedAttributes(getEventParameters().needDerivatives());

	    // update the observations. This ensures that their residuals,
	    // and derivatives properties reflect the current event position,
	    // sets weighted residuals to residual/totalError, and sets
	    // weighted derivatives to derivatives/totalError.
	    //
	    // Note that all observations are being sent to their predictors,
	    // but predictors should only compute stuff for those that are defining.

	    ArrayList<Prediction> predictions = parameters.predictorFactory().computePredictions(
		    source.getObservations().values(),parameters.predictionsThreadPool());

	    // for each supported observation call setPredictions.  That will update residuals.
	    // Then for each ObservationComponent owned by the Observation, the observation will
	    // call ObservationComponent.setPrediction() which will compute weights, weightedResiduals,
	    // weightedDerivatives etc.
	    for(Prediction p : predictions) 
		source.getObservation(p.getObservationId()).setPrediction(p);

	    // check for invalid observations
	    for (ObservationComponent obs : obsComponents) 
		if (obs.isDefining() && !obs.predictionValid())
		{
		    // this observation is supposed to be defining but its prediction is invalid
		    obs.setDefining(false);

		    String emsg = String.format("WARNING: Source:   %9d  %10.5f, %11.5f, %8.3f%n"
		    	+ "Receiver: %-6s  %s%n"
		    	+ "Distance: %1.4f deg%n"
		    	+ "Setting %s non-defining because %s%n%n",
		    	source.getSourceId(), source.getLatDegrees(), source.getLonDegrees(), source.getDepth(),
		    	obs.getReceiver().getSta(), obs.getReceiver().toString("%10.5f, %11.5f, %8.3f"),
		    	VectorGeo.angleDegrees(this.getUnitVector(), obs.getReceiver().getUnitVector()),
		    	obs.getStaPhaseType(), obs.getErrorMessage());
		    
		    errorlog.write(emsg);
		    if (logger.getVerbosity() >= 4)
			logger.write(emsg);

		}

	    definingVec.clear();
	    for (ObservationComponent obs : obsComponents)
		if (obs.isDefining())
		    definingVec.add(obs);
	    
	    definingChanged = false;

	    // now deal with correlated observations, if necessary
	    if (parameters.correlationMethod() != CorrelationMethod.UNCORRELATED
		    && (definingChanged || Math.abs(dkm) > 100))
	    {
		sigma = getCorrelationMatrix(definingVec);

		if (EventList.debugCorrelatedObservations)
		    logger.write(String.format("correlation matrix =%n%s%n", printMatrix(sigma, " %23.16e")));

		for (int i = 0; i < definingVec.size(); i++)
		{
		    sigma[i][i] *= definingVec.get(i).getTotalUncertainty()
			    * definingVec.get(i).getTotalUncertainty();
		    for (int j = i+1; j < definingVec.size(); j++)
			sigma[j][i] = sigma[i][j] *= definingVec.get(i).getModelUncertainty()
			* definingVec.get(j).getModelUncertainty();
		}

		if (EventList.debugCorrelatedObservations)
		    logger.write(String.format("sigma=%n%s%n", printMatrix(sigma, " %23.16e")));

		// find the cholesky decomposition of the inverse of sigma.
		sigma = new LUDecomposition(sigma).inverse();
		if (EventList.debugCorrelatedObservations)
		    logger.write(String.format("sigma inverse=%n%s%n", printMatrix(sigma, " %23.16e")));

		CholeskyDecomposition chol = new CholeskyDecomposition(sigma);

		if (!chol.isSPD())
		    throw new LocOOException(String.format(
			    "ERROR in LocOO3D version %s.  Cholesky decomposition of inverse A failed "
				    + "because matrix inverse A is not positive definite.  "
				    + "%nThis occurred while attempting to compute the observation weighting factors "
				    + "in the case where two or more of the observations are correlated.%n", LocOO.getVersion()));

		sigma = chol.getDecomposedMatrix();

		if (EventList.debugCorrelatedObservations)
		    logger.write(String.format("Cholesky decomposition of sigma inverse =%n%s%n", printMatrix(sigma, " %23.16e")));
	    }

	}
	else
	    // position of this Event did not change.  Only the origin time changed.
	    // No need for new Predictions but must update timeres and weighted residuals
	    // and weighted derivatives.
	    for (Observation obs : source.getObservations().values())
		obs.updateResiduals();


	// now deal with correlated observations, if necessary
	if (parameters.correlationMethod() != CorrelationMethod.UNCORRELATED)
	{
	    double wr;
	    double[] row;
	    for (int i = 0; i < definingVec.size(); i++)
	    {
		wr = 0;
		row = sigma[i];
		for (int j = i; j < definingVec.size(); j++)
		    wr += row[j] * definingVec.get(j).getResidual();

		if (Math.abs(wr) < 1e-30)
		    wr = 1e-30;
		definingVec.get(i).setWeightedResidual(wr);				
	    }

	    if (getEventParameters().needDerivatives())
		for (int k = 0; k < 4; k++)
		    if (isFree(k))
			for (int i = 0; i < definingVec.size(); i++)
			{
			    wr = 0;
			    for (int j = i; j < definingVec.size(); j++)
				wr += sigma[j][i] * definingVec.get(j).getDerivatives()[k];

			    //							if (k == GMPGlobals.LAT || k == GMPGlobals.LON)
			    //								wr /= getRadius();
			    definingVec.get(i).getWeightedDerivatives()[k] = wr;
			}
	}

	// finally, recompute the sum squared weighted residuals.
	sumSqrWeightedResiduals = 0.;
	for (int i = 0; i < definingVec.size(); i++)
	    sumSqrWeightedResiduals += Math.pow(definingVec.get(i).getWeightedResidual(), 2.);

	// increment the counter that keeps track of how many times sswr is computed.
	++nSSWR; 

	positionUpToDate = true;
	originTimeUpToDate = true;

	predictionTime += System.nanoTime()-timer;
    }

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Compute sigma
    //
    double[][] getCorrelationMatrix(ArrayList<ObservationComponent> observations)
    {
	int i, j, n = observations.size();

	double[][] c = new double[n][n];

	for (i = 0; i < n; i++)
	    c[i][i] = 1.;

	if (parameters.correlationMethod() == CorrelationMethod.FUNCTION)
	{
	    double dsta;
	    for (i = 0; i < n-1; i++)
		for (j = i+1; j < n; j++)
		    if (observations.get(i).getObsType() == observations.get(j).getObsType()
		    && observations.get(i).getPhase() == observations.get(j).getPhase())
		    {
			// distance in radians from station j to station i.
			dsta = observations.get(i).getObservation().getReceiver().distanceDegrees(
				observations.get(j).getObservation().getReceiver()) 
				/ parameters.correlationScale();

			c[i][j] = c[j][i] = exp(-dsta*dsta);
		    }
	}
	else if (parameters.correlationMethod() == CorrelationMethod.FILE)
	{
	    for (i=0; i<n; i++)
	    {
		Map<String, Double> corr = parameters.correlations().get(observations.get(i).getStaPhaseType());
		if (corr != null)
		    for (j=i+1; j<n; ++j)
		    {
			Double correlation = corr.get(observations.get(j).getStaPhaseType());
			if (correlation != null)
			    c[i][j] = c[j][i] = correlation;
		    }
	    }
	}
	return c;
    }

    /**
     * Retrieve a String representation of the observation covariance matrix. 
     * @return String
     */
    public String getCorrelationMatrixString(ArrayList<ObservationComponent> observations)
    {
	if (parameters.correlationMethod() == CorrelationMethod.UNCORRELATED)
	    return "Correlated observation option is not active.";

	StringBuffer cout = new StringBuffer();
	cout.append(String.format("Observation Correlation Coefficients:%n"));
	double[][] m = getCorrelationMatrix(observations);
	for (int i=0; i<observations.size(); i++)
	    if (observations.get(i).isDefining())
		for (int j=i+1; j<observations.size(); ++j)
		    if (observations.get(j).isDefining() && m[i][j] > 1e-6)
			cout.append(String.format("%-16s %-16s %8.6f%n",
				observations.get(i).getStaPhaseType(),
				observations.get(j).getStaPhaseType(),
				m[i][j]));
	return cout.toString();
    }

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Checks whether any defining observations have residuals that exceed the
    // sBigResidualThreshold.
    //
    // INPUT ARGS:
    // OUTPUT ARGS: NONE
    // RETURN:
    // boolean true = An Observation Exceeds the Threshold
    // false = Vice Versa
    //
    // *****************************************************************************
    boolean areThereBigResiduals() throws Exception,
    Exception, Exception
    {
	if (parameters.allowBigResiduals())
	    return false;
	checkStatus();
	for (ObservationComponent obs : definingVec)
	    if (abs(obs.getWeightedResidual()) > parameters.bigResidualThreshold())
		return true;
	return false;

    } // END areThereBigResiduals

    /**
     * Check for observations that have large weighted residuals.
     * Large means > gen_big_residual_threshold (default = 3).  
     * The number of large residuals identified will not exceed
     * gen_big_residual_max_fraction*(N-M) where N is the number of 
     * defining observations and M is the number of free parameters in 
     * the location (4 for free depth, 3 for fixed depth, etc).
     * All observations thus identified are set non-defining.
     * 
     * @return true if any observations were set non-defining, false
     * if none were set non-defining.
     */
    protected boolean checkBigResiduals() 
    {
	if (parameters.allowBigResiduals())
	    return false;

	TreeMap<Double, ObservationComponent> bigResiduals = 
		new TreeMap<Double, ObservationComponent>();

	int n = 0;
	// add observations with weighted residuals greater than bigResidualThreshold to 
	// the map.  The map key is -abs(residual) so that entries are ordered from 
	// largest to smallest absolute value.
	for (ObservationComponent obs : obsComponents)
	    if(obs.isDefining())
	    {
		++n;
		if (abs(obs.getWeightedResidual()) > parameters.bigResidualThreshold())
		    bigResiduals.put(-abs(obs.getWeightedResidual()), obs);
	    }

	if (bigResiduals.size() == 0)
	    return false;

	// n is number of defining observations.  Subtract M to make it
	// number that can be set non-defining without making the problem
	// ill posed.
	n -= nFree();

	// can't set any to non-defining without making the problem ill-posed.
	if (n <= 0)
	    return false;

	// determine how many to set non-defining.  minimum number is one.
	// maximum is a percentage of the number of degrees of freedom.
	int nmax = Math.max(1, (int)Math.floor(n*parameters.bigResidualMaxFraction()));

	n = 0;
	for (ObservationComponent obs : bigResiduals.values())
	{
	    if (n++ == nmax)
		break;

	    obs.setDefining(false);
	    if (logger.getVerbosity() >= 4)
		logger.write(String.format(
			"Setting observation %s non-defining because weighted residual = %1.3f is greater than %1.3f%n",
			obs.toString(), obs.getWeightedResidual(), parameters.bigResidualThreshold()));

	    // 10/09/2012, sb mod.  if travel time is set to non-defining then 
	    // set az and sh non-defining as well.
	    if (obs.getObsType() == GeoAttributes.TRAVEL_TIME)
		for (ObservationComponent ob : obs.getSiblings().values())
		    ob.setDefining(false);
	}

	return true;
    }

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Calculate the azimuthal gap given the station locations of defining
    // events
    // and the current event location. Also calculates the secondary gap (azgap
    // with
    // one station left out) and reports the station that was left out in
    // calculagin
    // the secondary azgap. If there are zero or one stations, both azgaps =
    // 2*PI.
    //
    // INPUT ARGS: NONE
    // OUTPUT ARGS: azimuthal gap, secondary azimuthal gap and station left out
    // of
    // of secondary azimuthal gap calculation.
    // RETURN: NONE
    //
    // *****************************************************************************
    Azgap azimuthalGap() throws Exception
    {
	// set of unique Site objects
	Set<Site> sites = new HashSet<>();

	for (int i = 0; i < definingVec.size(); i++)
	    sites.add(definingVec.get(i).getObservation().getReceiver().getSiteRow());

	Azgap azgap = new AzgapExtended(source.getSourceId(), source.getUnitVector(), sites);

	return azgap;
    }

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Returns the root mean squared residuals for defining Travel Time
    // Observations only.
    //
    // INPUT ARGS: NONE
    // OUTPUT ARGS: NONE
    // RETURN: double
    //
    // *****************************************************************************
    double rmsTTResiduals() throws Exception
    {
	double sum = 0;
	int n = 0;

	checkStatus();
	for (int i = 0; i < definingVec.size(); i++)
	    if (definingVec.get(i).getObsType() == GeoAttributes.TRAVEL_TIME)
	    {
		sum += pow(definingVec.get(i).getResidual(), 2);
		n++;
	    }
	if (n > 0)
	    sum = sqrt(sum / n);

	return sum;

    } // END sumSQRTTResidual


    /**
     * Retrieve the standard deviation of the weighted residuals.  
     * Includes all defining tt, az and sh weighted residuals. 
     * @return the standard deviation of the weighted residuals.  
     * @throws Exception
     */
    double sdobs() throws Exception
    {
	checkStatus();

	int n = definingVec.size();
	if (n < 2) return -1.;

	double[] wr = new double[n];
	double sum = 0;
	for (int i = 0; i < n; i++)
	{
	    wr[i] = definingVec.get(i).getWeightedResidual();
	    sum += wr[i];
	}
	double mean = sum/n;
	sum = 0.;
	for (int i = 0; i < n; i++)
	{
	    wr[i] -= mean;
	    sum += wr[i]*wr[i];
	}

	return sqrt(sum/(n-1));
    } 

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Counts the number of observations associated with this origin. This is the
    // number of unique arids. Includes both defining and non-defining
    // observations.
    //
    // INPUT ARGS: NONE
    // OUTPUT ARGS: NONE
    // RETURN:
    // int Count of arids
    //
    // *****************************************************************************
    int countObservations() {
	return source.getObservations().size();
    }

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Returns the count of either DEFINING or NON-DEFINING observations
    //
    // INPUT ARGS:
    // boolean def_val Flag to denote which events to compute
    // true = DEFINING
    // false = NON-DEFINING
    // OUTPUT ARGS: NONE
    // RETURN:
    // int Count of Observations
    //
    // *****************************************************************************
    int countObservations(boolean def_val) throws Exception
    {
	checkStatus();
	if (def_val)
	    return definingVec.size();
	return obsComponents.size() - definingVec.size();
    } // END countObservations

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Returns the count of either DEFINING or NON-DEFINING TT observations
    //
    // INPUT ARGS:
    // boolean def_val Flag to denote which events to compute
    // true = DEFINING
    // false = NON-DEFINING
    // OUTPUT ARGS: NONE
    // RETURN:
    // int Count of TT Observations
    //
    // *****************************************************************************
    int countTTObservations(boolean def_val) throws Exception
    {
	checkStatus();
	int count = 0;
	for (ObservationComponent obs : obsComponents)
	    if (obs.getObsType() == GeoAttributes.TRAVEL_TIME
	    && obs.isDefining() == def_val)
		count++;
	return count;
    } // END countTTObservations

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Returns the count of either DEFINING or NON-DEFINING Az observations
    //
    // INPUT ARGS:
    // boolean def_val Flag to denote which events to compute
    // true = DEFINING
    // false = NON-DEFINING
    // OUTPUT ARGS: NONE
    // RETURN:
    // int Count of Az Observations
    //
    // *****************************************************************************
    int countAzObservations(boolean def_val) throws Exception

    {
	checkStatus();
	int count = 0;
	for (ObservationComponent obs : obsComponents)
	    if (obs.getObsType() == GeoAttributes.AZIMUTH
	    && obs.isDefining() == def_val)
		count++;
	return count;
    } // END countAzObservations

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Returns the count of either DEFINING or NON-DEFINING Sh observations
    //
    // INPUT ARGS:
    // boolean def_val Flag to denote which events to compute
    // true = DEFINING
    // false = NON-DEFINING
    // OUTPUT ARGS: NONE
    // RETURN:
    // int Count of Sh Observations
    //
    // *****************************************************************************
    int countShObservations(boolean def_val) throws Exception

    {
	checkStatus();
	int count = 0;
	for (ObservationComponent obs : obsComponents)
	    if (obs.getObsType() == GeoAttributes.SLOWNESS
	    && obs.isDefining() == def_val)
		count++;
	return count;
    } // END countShObservations

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Counts the number of defining observations that are of the specified type
    // (TT/Az/Sh) and phase.
    //
    // INPUT ARGS: String type = name of type to count (TT/Az/Sh).
    // OUTPUT ARGS: NONE
    // RETURN: int number of defining observations that are of the specified
    // type
    //
    // *****************************************************************************
    int countPhaseAndType(SeismicPhase phase, GeoAttributes type) throws Exception
    {
	checkStatus();
	int count = 0;
	for (int i = 0; i < definingVec.size(); i++)
	    if (obsComponents.get(i).getPhase() == phase
	    && obsComponents.get(i).getObsType() == type)
		++count;
	return count;
    }

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Counts the number of defining observations of crustal phases (Pn, Sn, Lg,
    // Pg).
    //
    // INPUT ARGS: NONE
    // OUTPUT ARGS: NONE
    // RETURN: int number of defining observations that are crustal phases
    //
    // *****************************************************************************
    int countCrustalPhases() throws Exception
    {
	checkStatus();
	int count = 0;
	for (ObservationComponent obs : definingVec)
	    if (obs.getPhase() == SeismicPhase.Pn
	    || obs.getPhase() == SeismicPhase.Sn
	    || obs.getPhase() == SeismicPhase.Pg
	    || obs.getPhase() == SeismicPhase.Sg
	    || obs.getPhase() == SeismicPhase.Lg)
		++count;
	return count;
    }

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Counts the number of stations that are either defining or nondefining and
    // are
    // within a specified distance (in radians) from the event epicenter.
    //
    // INPUT ARGS:
    // boolean def_val Flag to denote which stations to count
    // true = DEFINING
    // false = NON-DEFINING
    // OUTPUT ARGS: NONE
    // RETURN:
    // int Count of Defining/Nondefining Stations
    //
    // *****************************************************************************
    Set<Receiver> countStations(boolean def_val, double dist) throws Exception

    {
	checkStatus();
	Set<Receiver> in = new HashSet<Receiver>();
	for (ObservationComponent ob : obsComponents)
	    if (ob.isDefining() == def_val && source.distance(ob.getObservation().getReceiver()) <= dist)
		in.add(ob.getObservation().getReceiver());

	return in;

    } // END countStations

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Calculate an initial estimate of the event location.
    //
    // INPUT ARGS: NONE
    // OUTPUT ARGS: NONE
    // RETURN:
    // a 4-element ArrayList containing the initial estimate of the event origin
    // latitude, longitude, depth and origin time.
    //
    // *****************************************************************************
    Location calculateInitialLocation() throws Exception
    {
	Location loc = null;

	// find the earliest arrival time. That should be the closest station.
	// set the event location to the station location and the origin time
	// to 100 seconds before the arrival time.
	// If there are no travel time data then don't modify the origin time
	// and the locator should fix the origin time.
	for (ObservationComponent ob : obsComponents)
	    if (ob.getObsType() == GeoAttributes.TRAVEL_TIME && ob.isDefining()
	    && (loc == null || ob.getObserved() < loc.getTime()))
		loc = new Location(ob.getObservation().getReceiver(), ob.getObserved());

	if (loc != null)
	    loc.setTime(loc.getTime() - 100.);

	// make an index to the defining azimuth observations
	ArrayList<ObservationComponent> azObs = new ArrayList<ObservationComponent>();
	for (ObservationComponent ob : definingVec)
	    if (ob.getObsType() == GeoAttributes.AZIMUTH)
		azObs.add(ob);

	if (azObs.size() == 1)
	{
	    // if there is only one azimuth observation, set the initial event
	    // location to 10 degrees away from the station location, in the direction of
	    // the observed azimuth
	    loc = new Location(azObs.get(0).getObservation().getReceiver().move(
		    azObs.get(0).getObserved(), toRadians(10.)),
		    Globals.NA_VALUE);
	} else if (azObs.size() > 1)
	{
	    try
	    {
		// There are two or more azimuth observations, so use the great
		// circle intersections to estimate the event location.
		ArrayList<GreatCircle> gc = new ArrayList<GreatCircle>();
		for (ObservationComponent ob : azObs)
		{
		    gc.add(new GreatCircle(ob.getObservation().getReceiver().getUnitVector(), 
			    ob.getObservation().getReceiver().move(
				    ob.getObserved(), PI / 2).getUnitVector()));
		}

		double[] vector = new double[3];
		double[] cross = new double[3];

		for (GreatCircle gc1 : gc)
		    for (GreatCircle gc2 : gc)
			if (gc1 != gc2)
			{
			    VectorUnit.cross(gc1.getNormal(), gc2.getNormal(), cross);
			    if (VectorUnit.dot(cross, gc1.getLast()) < 0.)
				Vector3D.negate(cross);
			    Vector3D.increment(vector, cross);
			}

		loc = new Location(cross, VectorUnit.normalize(cross), Globals.NA_VALUE);
		loc.setDepth(0);
	    } catch (Exception e)
	    {
		throw new LocOOException(e.getCause());
	    }
	}

	return loc;

    } // END calculateInitialLocation

    // **** _FUNCTION DESCRIPTION_
    // *************************************************
    //
    // Check whether the problem is properly constrained. In the case of a
    // constrained problem, but with improper observations relative to the free
    // parameter set, constain time or depth as appropriate.
    //
    // INPUT ARGS: sFix : 4 boolean values indicating which components of the
    // location
    // are to be held fixed during search for best fit location.
    // Elements are LAT, LON, DEPTH, TIME.
    // OUTPUT ARGS: NONE
    // RETURN:
    // boolean true = Sufficiently Constrained
    // false = Unconstrained
    //
    // *****************************************************************************
    boolean isConstrained() throws Exception
    {
	int M = 0; // Number of free solution parameter (<=4)
	for (int j = 0; j < 4; j++)
	    if (isFree(j))
		M++;
	int nD = countObservations(true);
	int nTT = countTTObservations(true);
	int nAz = countAzObservations(true);
	int nSh = countShObservations(true);
	int nTTSh = nTT + nSh;

	// ------------------------------------------------------------------------
	// Must Have Defining Events
	// ------------------------------------------------------------------------
	if (nD == 0)
	    throw new LocOOException(String.format(
		    "ERROR:  No defining observations for evid=%d, orid=%d",
		    source.getEvid(), source.getSourceId()));

	// ------------------------------------------------------------------------
	// Azimuth Only Check: Cannot Solve for Depth With Only Azimuth
	// Observations
	// ------------------------------------------------------------------------
	if ((nD == nAz) && isFree(GMPGlobals.DEPTH))
	{
	    String emsg = String.format("WARNING:  Cannot Solve for Depth with Only Azimuth "
		    +"Observations. Evid=%d Orid=%d Fixing Depth and Continuing.%n",
		    source.getEvid(), source.getSourceId());

	    errorlog.write(emsg);
	    
	    if (logger.getVerbosity() >= 2)
		logger.write(emsg);
	    fixed[GMPGlobals.DEPTH] = true;
	    M--;
	}

	// ------------------------------------------------------------------------
	// Basic Check: Need at Least as Many Defining Events as Free Parameters
	// ------------------------------------------------------------------------
	if (nD < M)
	    throw new LocOOException(
		    String.format("ERROR:  Fewer defining observations (%d) than free parameters (%d). for evid=%d, orid=%d",
			    nD, M, source.getEvid(), source.getSourceId()));

	int minAz = 0; // Minimum # Required Azimuth Observations
	int minTT = 0; // Minimum # Required Traveltime Observations
	int minTTSh = 0; // Minimum # Required Traveltime/Slowness Observations

	// ------------------------------------------------------------------------
	// If TIME is Free, then we need at least 1 TT Observations
	// ------------------------------------------------------------------------
	if (isFree(GMPGlobals.TIME))
	    minTT = max(minTT, 1);

	// ------------------------------------------------------------------------
	// If there only 1 Observing (Defining) Station, then Establish Other
	// Constraints
	// ------------------------------------------------------------------------
	Set<Receiver> stations = countStations(true, 1e30);
	if (stations.size() == 1)
	{
	    String sta = stations.iterator().next().getSta();
	    String emsg = String.format("WARNING:  Only 1 Station with Defining Observations (%s). "
		    +"Evid=%d Orid=%d. Fixing Depth and Continuing.%n",
		    sta, source.getEvid(), source.getSourceId());

	    errorlog.writeln(emsg);
	    if (logger.getVerbosity() >= 42)
		logger.writeln(emsg);

	    // If LAT / LON are unconstrained then we need at least 1 azimuth
	    // and M-1 TT/Sh observations
	    if (isFree(GMPGlobals.LAT) || isFree(GMPGlobals.LON))
	    {
		minAz = max(minAz, 1);
		minTTSh = max(minTTSh, M - 1);
	    } else
		minTTSh = max(minTTSh, M);

	}

	// ------------------------------------------------------------------------
	// Check Traveltime Observation Constraints
	// ------------------------------------------------------------------------
	if (nTT < minTT)
	    throw new LocOOException(String.format(
		    "ERROR: Cannot compute location with less than %d TT observations.  evid=%d, orid=%d",
		    minTT, source.getEvid(), source.getSourceId()));

	// ------------------------------------------------------------------------
	// Check Azimuth Observation Constraints
	// ------------------------------------------------------------------------
	if (nAz < minAz)
	    throw new LocOOException(String.format(
		    "ERROR: Cannot compute location with less than %d AZ observations.  evid=%d, orid=%d",
		    minAz, source.getEvid(), source.getSourceId()));

	// ------------------------------------------------------------------------
	// Check Traveltime/Slowness Observation Constraints
	// ------------------------------------------------------------------------
	if (nTTSh < minTTSh)
	    throw new LocOOException(String.format(
		    "ERROR: Cannot compute location with less than %d TT+SH observations.  evid=%d, orid=%d",
		    minTTSh, source.getEvid(), source.getSourceId()));

	return true;

    } // END isConstrained

    /**
     * clearGrids
     */
    public void clearGrids()
    {
    }

    public Map<GeoAttributes, ObservationComponent> getObservations(Observation observation)
    {
	return observation.getObservationComponents();
    }

    public Map<GeoAttributes, ObservationComponent> getObservations(Long arid)
    {
	return source.getObservations().get(arid).getObservationComponents();
    }

    public int countRemovedObservations()
    {
	int n = 0;
	for (Observation observation : source.getObservations().values())
	    for (ObservationComponent obs : observation.getObservationComponents().values())
		if (obs.isDefiningOriginal() && !obs.isDefining())
		    ++n;
	return n;
    }

    public String getInputLocationString()
    {
	return String.format(
		"Input location:%n%n      Orid      Evid         Lat         Lon     Depth             Time                Date (GMT)     JDate%n"
			+"%10d %9d %11.6f %11.6f %9.3f %16.4f %25s  %8d%n",
			source.getSourceId(), source.getEvid(),
			inputLocation.getLatDegrees(),
			inputLocation.getLonDegrees(),
			inputLocation.getDepth(),
			inputLocation.getTime(),
			GMTFormat.GMT_MS.format(GMTFormat.getDate(inputLocation.getTime())),
			GMTFormat.getJDate(inputLocation.getTime()));
    }

    public String getSiteTable()
    {
	TreeMap<String, Receiver> siteMap = new TreeMap<String, Receiver>();

	for (Observation observation : source.getObservations().values())
	    siteMap.put(
		    String.format("%s_%d", observation.getReceiver().getSta(), observation.getReceiver().getOndate()), 
		    observation.getReceiver());

	StringBuffer cout = new StringBuffer(String.format(
		"Site Table:%n%nSta      OnDate   OffDate      Lat         Lon       Elev    StaName%n"));

	for (Receiver site : siteMap.values())
	    cout.append(String.format("%-8s %7d  %7d  %10.6f %11.6f %8.4f   %-1s%n", 
		    site.getSta(),
		    site.getOndate(),
		    site.getOffdate(),
		    site.getLatDegrees(),
		    site.getLonDegrees(),
		    -site.getDepth(),
		    site.getStaName()));
	return cout.toString();
    }

    /**
     * Retreive a String representation of the observation table printed at the 
     * start of the loction calculation.
     * @return observation table
     * @throws Exception 
     */
    public String getObservationTable() throws Exception
    {
	StringBuffer cout = new StringBuffer(String.format(
		"Observation Table:%n%n       Arid  Sta    Phase   Typ Def      Dist          Obs      Obs_err    Predictor%n"));

	for (ObservationComponent obs : sortedObservations(false))
	{
	    Predictor p = parameters.predictorFactory().getPredictor(obs.getPhase());
	    if (p == null)
		throw new Exception("No Predictor for phase "+obs.getPhase().toString());
	    p = p.getPredictor(obs.getObservation());
	    cout.append(obs.observationString(p)).append(Globals.NL);
	}

	cout.append(Globals.NL).append(observationStatus.toString());
	return cout.toString();
    }

    /**
     * Get String representation of observation table that is output each 
     * iteration.
     * @return observation table
     */
    public String getObsIterationTable()
    {
	StringBuffer cout = new StringBuffer(
		"     Arid  Sta    Phase   Typ Def  Predictor            Obs      Obs_err         Pred    Total_err       Weight     Residual      W_Resid         Dist      ES_Azim      SE_Azim");
	cout.append(Globals.NL);

	for (ObservationComponent obs : sortedObservations(true))
	    cout.append(obs.obsIterationString()).append(Globals.NL);
	return cout.toString();
    }

    public String getPredictionTable()
    {
	StringBuffer cout = new StringBuffer(
		"     Arid  Sta    Phase   Typ Def  Model        Model_uncert  Base_model    Ellip_corr    Elev_rcvr     Elev_src    Site_corr  Source_corr    Path_corr      ME_corr       d_dLat       d_dLon         d_dZ         d_dT");
	cout.append(Globals.NL);

	for (ObservationComponent obs : sortedObservations(true))
	    cout.append(obs.predictionString()).append(Globals.NL);
	return cout.toString();
    }

    /**
     * Get the current set of ObservationComponents, sorted in a manner proscribed by 
     * property observationSortParameter defined in the properties file.
     * @param definingOnly
     * @return
     */
    private ArrayList<ObservationComponent> sortedObservations(boolean definingOnly)
    {
	ArrayList<ObservationComponent> list = new ArrayList<ObservationComponent>(obsComponents.size());
	for (ObservationComponent obs : obsComponents)
	    if (!definingOnly || obs.isDefining())
		list.add(obs);

	String sortOrder = parameters.properties().getProperty("io_observation_sort_order", "distance");

	if (sortOrder.equals("distance"))
	{
	    Collections.sort(list, new Comparator<ObservationComponent>()
	    {
		@Override
		public int compare(ObservationComponent o1,
			ObservationComponent o2) {
		    int compare = (int) Math.signum(o1.getObservation().getDistance()-o2.getObservation().getDistance());
		    if (compare != 0)
			return compare;
		    if (o1.getSta().equals(o2.getSta()))
			if (o1.getPhase() == o2.getPhase())
			    return o2.getObsTypeShort().compareTo(o1.getObsTypeShort());
			else
			    return o1.getPhase().toString().compareTo(o2.getPhase().toString());
		    return o1.getSta().compareTo(o2.getSta());
		}
	    });
	}			
	else if (sortOrder.equals("weighted_residual"))
	{

	    Collections.sort(list, new Comparator<ObservationComponent>()
	    {

		@Override
		public int compare(ObservationComponent o1,
			ObservationComponent o2) {
		    int compare = (int) Math.signum(Math.abs(o1.getWeightedResidual())-Math.abs(o2.getWeightedResidual()));
		    if (compare != 0)
			return compare;
		    if (o1.getSta().equals(o2.getSta()))
			if (o1.getPhase() == o2.getPhase())
			    return o2.getObsTypeShort().compareTo(o1.getObsTypeShort());
			else
			    return o1.getPhase().toString().compareTo(o2.getPhase().toString());
		    return o1.getSta().compareTo(o2.getSta());
		}

	    });
	}			
	else // if (sortOrder.equals("station_phase"))
	{
	    Collections.sort(list, new Comparator<ObservationComponent>() {

		@Override
		public int compare(ObservationComponent o1,
			ObservationComponent o2) {
		    if (o1.getSta().equals(o2.getSta()))
			if (o1.getPhase() == o2.getPhase())
			    return o2.getObsTypeShort().compareTo(o1.getObsTypeShort());
			else
			    return o1.getPhase().toString().compareTo(o2.getPhase().toString());
		    return o1.getSta().compareTo(o2.getSta());
		}

	    });
	}
	return list;
    }

    public void submitIterationTableEntry(int baseIteration, int iteration, String comment, int N, int M) 
	    throws Exception
    {
	Location newloc = getLocation();
	newloc = newloc.move(dloc);

	if (iterationTable == null)
	    iterationTable = new StringBuffer(String.format(
		    "Iteration Table:%n%n  Itt   It     Comment    N   M     Lat      Lon        Depth      Time  rms_Trsd  rms_Wrsd    "
			    + "dNorth     dEast       dZ        dT       dkm    dxStart   dzStart   dtStart   azStart   nF damp  converge%n"));

	iterationTable.append(String.format(
		"%5d %4d %11s %4d %3d %9.4f %9.4f %9.3f %9.3f %9.4f %9.4f %9.3f %9.3f %9.3f %9.3f %9.4f %9.4f %9.4f %9.4f %9.4f %4d %4d %9.2e%n",
		baseIteration,
		iteration,
		comment,
		N, 
		M,
		source.getLatDegrees(),
		source.getLonDegrees(),
		source.getDepth(),
		source.getTime()-getInitialLocation().getTime(),
		rmsTTResiduals(),
		rmsWeightedResidual(),
		dloc[GMPGlobals.LAT]*source.getRadius(),
		dloc[GMPGlobals.LON]*source.getRadius(),
		dloc[GMPGlobals.DEPTH],
		dloc[GMPGlobals.TIME],
		dkm,
		getInitialLocation().distance(newloc)*getInitialLocation().getEarthRadius(),
		newloc.getDepth()-getInitialLocation().getDepth(),
		newloc.getTime()-getInitialLocation().getTime(),
		getInitialLocation().azimuthDegrees(newloc, Double.NaN),
		nSSWR,
		(int)round(log10(applied_damping)),
		lsq_convergence_value
		));
    }

    public String getIterationTable()
    {
	if (iterationTable == null)
	    return "";
	return iterationTable.toString();
    }

    @Override
    public String toString()
    {
	try
	{
	    return String.format(
		    "Lat=%8.4f  lon=%9.4f  z=%7.3f t0=%7.3f rms_Trsd=%7.4f rms_Wrsd=%7.4f dNorth=%8.3f dEast=%8.3f dZ=%7.3f dT=%8.4f dkm=%8.3f nf=%3d damp=%3d conv=%1.2e",
		    source.getLatDegrees(),
		    source.getLonDegrees(),
		    source.getDepth(),
		    source.getTime()-getInitialLocation().getTime(),
		    rmsTTResiduals(),
		    rmsWeightedResidual(),
		    dloc[GMPGlobals.LAT]*source.getRadius(),
		    dloc[GMPGlobals.LON]*source.getRadius(),
		    dloc[GMPGlobals.DEPTH],
		    dloc[GMPGlobals.TIME],
		    dkm,
		    nSSWR,
		    (int)round(log10(applied_damping)),
		    lsq_convergence_value
		    );
	} catch (Exception e)
	{
	    return e.getStackTrace().toString();
	} 
    }

    /**
     * Returns a deep copy of the Location object that this Event extends.
     * 
     * @return Location
     * @throws Exception 
     */
    public Location getLocation() throws Exception
    {
	return new Location(getUnitVector(), source.getRadius(), source.getTime());
    }

    /**
     * setLocation
     * 
     * @param location new Location
     */
    public void setLocation(Location location)
    {
	source.setLocation(location);
	setPositionUpToDateFalse();
    }

    /**
     * Sets the working location of this Event to a new Location which is offset
     * from some old location by dloc. Returns the distance 
     * in km from old location to new location. This distance is equal to
     * GeoVector.distance3D() plus difference in time * 8 km/sec.
     * 
     * @param dloc
     *            double[] change in lat (radians), lon (radians), depth (km) and time (sec). 
     *            Change in lon is measures in great circle sense, not small circle.
     * @return double distance moved in km.
     */
    public double moveLocation(Location oldLocation, double[] dloc) throws Exception
    {
	double dkm = sqrt(pow(dloc[0]*oldLocation.getRadius(), 2)
		+ pow(dloc[1]*oldLocation.getRadius(), 2)
		+ pow(dloc[2], 2)
		+ pow(dloc[3]*8., 2));

	// compute a new Location 
	setLocation(oldLocation.move(dloc));
	return dkm;
    }

    /**
     * If false, modelUncertainty will always be zero.
     * Default is true.
     * @return useModelUncertainty
     */
    public boolean useTTModelUncertainty()
    {
	return parameters.useTTModelUncertainty();
    }

    /**
     * If false, modelUncertainty will always be zero.
     * Default is true.
     * @return useModelUncertainty
     */
    public boolean useAzModelUncertainty()
    {
	return parameters.useAzModelUncertainty();
    }

    /**
     * If false, modelUncertainty will always be zero.
     * Default is true.
     * @return useModelUncertainty
     */
    public boolean useShModelUncertainty()
    {
	return parameters.useShModelUncertainty();
    }

    public void setPositionUpToDateFalse() {
	if (positionUpToDate)
	{
	    positionUpToDate = false;
	    for (Observation observation : source.getObservations().values())
		observation.predictionUpToDate(false);
	}
    }

    public void setOriginTimeUpToDateFalse()
    {
	originTimeUpToDate = false;
    }

    public void change(double dlat, double dlon, double ddepth, double dtime) 
    {
	source.change(dlat, dlon, ddepth, dtime);
	setPositionUpToDateFalse();
    }

    public void change(double[] dloc) 
    {
	source.change(dloc);
	setPositionUpToDateFalse();
    }

    public void setTime(double time) 
    {
	source.setTime(time);
	setOriginTimeUpToDateFalse();
    }

    public void add(double[] u) 
    {
	source.add(u);
	setPositionUpToDateFalse();
    }

    public void flip() 
    {
	source.flip();
	setPositionUpToDateFalse();
    }

    public double[] getUnitVector() 
    {
	return source.getUnitVector();
    }

    public void interpolate(GeoVector g1, GeoVector g2, double fraction) 
    {
	source.interpolate(g1, g2, fraction);
	setPositionUpToDateFalse();
    }

    public void mean(Collection<GeoVector> geoVectors) 
    {
	source.mean(geoVectors);
	setPositionUpToDateFalse();
    }

    public void mean(Object[] geoVectors) 
    {
	source.mean(geoVectors);
	setPositionUpToDateFalse();
    }

    public void midpoint(GeoVector g1, GeoVector g2) 
    {
	source.midpoint(g1, g2);
	setPositionUpToDateFalse();
    }

    public boolean move_north(double distance) 
    {
	boolean m = source.move_north(distance);
	setPositionUpToDateFalse();
	return m;
    }

    public void rotateThis(double[] eulerRotationMatrix) 
    {
	source.rotateThis(eulerRotationMatrix);
	setPositionUpToDateFalse();
    }

    public void rotateThis(GeoVector pole, double angle) 
    {
	source.rotateThis(pole, angle);
	setPositionUpToDateFalse();
    }

    public GeoVector setDepth(double depth) 
    {
	GeoVector g = source.setDepth(depth);
	setPositionUpToDateFalse();
	return g;
    }

    public void setGeoVector(double lat, double lon, double depth,
	    boolean inDegrees) 
    {
	source.setGeoVector(lat, lon, depth, inDegrees);
	setPositionUpToDateFalse();
    }

    public void setGeoVector(double[] v, double radius) 
    {
	source.setGeoVector(v, radius);
	setPositionUpToDateFalse();
    }

    public void setGeoVector(double[] v) 
    {
	source.setGeoVector(v);
	setPositionUpToDateFalse();
    }

    public GeoVector setRadius(double r) 
    {
	GeoVector g = source.setRadius(r);
	setPositionUpToDateFalse();
	return g;
    }

    public void setUnitVector(double[] u) 
    {
	source.setUnitVector(u);
	setPositionUpToDateFalse();
    }

    @SuppressWarnings("unused")
    private String printMatrix(double[][] x)
    {
	StringBuffer buf = new StringBuffer();
	for (int i=0; i<x.length; ++i)
	{
	    for (int j=0; j<x[i].length; ++j)
		buf.append("  ").append(Double.toString(x[i][j]));
	    buf.append(Globals.NL);
	}
	return buf.toString();
    }

    private String printMatrix(double[][] x, String format)
    {
	StringBuffer buf = new StringBuffer();
	for (int i=0; i<x.length; ++i)
	{
	    for (int j=0; j<x[i].length; ++j)
		buf.append(String.format(format, x[i][j]));
	    buf.append(Globals.NL);
	}
	return buf.toString();
    }

    /**
     * Generates a grid of sum squared weighted residuals.
     * <p>Required parameters:
     * <br>grid_output_file_name = output file
     * <br>grid_output_file_format = tecplot
     * <br>grid_origin_source = epicenter
     * <br>grid_origin_lat
     * <br>grid_origin_lon
     * <br>grid_origin_depth
     * <br>grid_map_nwidth = 21
     * <br>grid_map_nheight = 23
     * <br>grid_map_ndepth = 1
     * <br>grid_map_width = 20
     * <br>grid_map_height = 22
     * <br>grid_map_depth_range = 0
     * <br>grid_units = radians | degrees | km | meters
     * @throws IOException 
     * @throws Exception 
     */
    protected void griddedResiduals() throws Exception
    {
	String outputFileName = parameters.properties().getProperty("grid_output_file_name");

	if (outputFileName == null) return;

	outputFileName = outputFileName.replace("<orid>", Long.toString(source.getSourceId()));
	outputFileName = outputFileName.replace("<sourceid>", Long.toString(source.getSourceId()));

	File outputFile = new File(outputFileName);

	String gridFileFormat = parameters.properties().getProperty("grid_output_file_format", "tecplot").toLowerCase();

	Location center=null;
	String grid_origin_center = parameters.properties().getProperty("grid_origin_source", "epicenter");
	if (grid_origin_center.equals("epicenter"))
	{
	    center = getLocatorResults().getLocation().clone();
	    center.setDepth(0.);
	}
	else if (grid_origin_center.equals("hypocenter"))
	    center = getLocatorResults().getLocation().clone();
	else
	{
	    center = new Location(
		    parameters.properties().getDouble("grid_origin_lat"),
		    parameters.properties().getDouble("grid_origin_lon"),
		    parameters.properties().getDouble("grid_origin_depth", 0.),
		    true, 0.);
	}

	if (logger.getVerbosity() > 0)
	    System.out.printf(String.format("Center of grid = %s%n", center.toString()));

	int nx = parameters.properties().getInt("grid_map_nwidth");
	int ny = parameters.properties().getInt("grid_map_nheight");
	int nz = parameters.properties().getInt("grid_map_ndepth", 1);

	double width = parameters.properties().getDouble("grid_map_width");
	double height = parameters.properties().getDouble("grid_map_height");

	double depth0 = center.getDepth();
	double ddepth = 0.;
	if (nz > 1)
	{
	    double[] depthRange = parameters.properties().getDoubleArray("grid_map_depth_range");
	    // depthRange is 2-element array with depth of top and bottom of block. 
	    if (depthRange.length != 2)
		throw new LocOOException("\nProperty grid_map_depth_range must have two " +
			"elements for minimum and maximum depth in km");

	    depth0 = depthRange[1];
	    ddepth = (depthRange[0]-depthRange[1])/(nz-1);
	}

	String gridUnits = parameters.properties().getProperty("grid_units", "degrees");
	// convert to radians
	double convert = 1.;
	GeoVector pole = new GeoVector(new double[] {0., 0., 1.}, 1.);

	if (gridUnits.equals("degrees"))
	{
	    convert = Math.PI/180.;
	}
	else if (gridUnits.equals("km"))
	{
	    convert = 1./6371.;
	    pole = center.moveNorth(Math.PI/2.);
	}
	else 
	    throw new LocOOException(String.format("Property grid_units = %s but must be one of [degrees | km ]", 
		    gridUnits));

	GeoVector[][] grid = center.getGrid(pole, nx, width*convert/(nx-1), ny, height*convert/(ny-1));

	double r,az,x,y;
	Brents brents = new Brents();
	// estimates of originTimes (arrivalTime - predicted travel time).
	tBrent = new ArrayListDouble(definingVec.size());
	double[] xbrack;

	double[][][] ssqr = new double[ny][nx][nz];

	boolean needDerivatives = getEventParameters().needDerivatives();
	getEventParameters().needDerivatives(false);

	double time0 = source.getTime();

	if (logger.getVerbosity() > 0)
	    System.out.println("Computing gridded residuals");

	long timer = System.currentTimeMillis();
	for (int i=0; i<ny; ++i)
	{
	    if (logger.getVerbosity() > 0)
		System.out.printf(String.format("NY = %3d / %3d  %10.2f%n", i,ny, 
			(System.currentTimeMillis()-timer)*1e-3));

	    for (int j=0; j<nx; ++j)
		for (int k=0; k<nz; ++k)
		{
		    setLocation(new Location(grid[i][j].setDepth(depth0 + k*ddepth), time0));
		    checkStatus();
		    tBrent.clear();
		    for (int n=0; n<definingVec.size(); ++n)
			if (definingVec.get(n).getObsType() == GeoAttributes.TRAVEL_TIME)
			    tBrent.add(definingVec.get(n).getObservation().getArrivalTime()
				    -time0-definingVec.get(n).getPredicted()); 

		    try
		    {
			xbrack = mnbrak(-0.4, 0.4);
			brents.minF(xbrack[0], xbrack[1], this);
		    } 
		    catch (Exception e)
		    {
			throw new LocOOException(e);
		    }

		    setTime(brents.getExtremaAbscissa()+time0);
		    ssqr[i][j][k] = getSumSqrWeightedResiduals();
		}
	}

	timer = System.currentTimeMillis()-timer;

	if (logger.getVerbosity() >= 1)
	    logger.writeln("Time to compute gridded residuals = "+
		    GMPGlobals.ellapsedTime(timer*.001));

	int m = nFree();
	double kappa_sqr = getLocatorResults().getKappa(m)*getLocatorResults().getKappa(m);
	double ssqr0 = getLocatorResults().getSumSQRWeightedResiduals();

	if (gridFileFormat.equals("tecplot"))
	{
	    if (logger.getVerbosity() >= 1)
		logger.write(String.format("Writing gridded residuals to file %s%n",
			outputFile.getCanonicalPath()));
	    BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));

	    output.write(String.format("# Location of center = %1.6f %1.6f %1.3f %1.3f%n",
		    center.getLatDegrees(), center.getLonDegrees(), center.getDepth(), center.getTime()));

	    if (nz == 1 && gridUnits.equals("km"))
	    {
		output.write("variables = \"East (km)\" \"North (km)\" \"RMS Weighted Residuals\" \"Confidence Level\"");
		output.newLine();
		output.write(String.format("zone i=%d j=%d%n", nx, ny));
		for (int i=0; i<ny; ++i)
		    for (int j=0; j<nx; ++j)
		    {
			r = center.distance(grid[i][j])/convert;
			az = center.azimuth(grid[i][j], 0.);
			output.write(String.format("%12.6f %12.6f %12.6f %12.6f%n",
				r*Math.sin(az), 
				r*Math.cos(az),
				Math.sqrt(ssqr[i][j][0]/definingVec.size()),
				(ssqr[i][j][0]-ssqr0)/kappa_sqr
				));
		    }
	    }
	    else if (nz == 1 && gridUnits.equals("degrees"))
	    {
		output.write("variables = \"Longitude (deg)\" \"Latitude (deg)\" \"RMS Weighted Residuals\" \"Confidence Level\"");
		output.newLine();
		output.write(String.format("zone i=%d j=%d%n", nx, ny));
		for (int i=0; i<ny; ++i)
		    for (int j=0; j<nx; ++j)
			output.write(String.format("%12.6f %12.6f %12.6f %12.6f%n",
				grid[i][j].getLonDegrees(),
				grid[i][j].getLatDegrees(),
				Math.sqrt(ssqr[i][j][0]/definingVec.size()),
				(ssqr[i][j][0]-ssqr0)/kappa_sqr
				));
	    }
	    else if (nz > 1 && gridUnits.equals("km"))
	    {
		output.write("variables = \"East (km)\" \"North (km)\" \"Depth (km)\" \"RMS Weighted Residuals\" \"Confidence Level\"");
		output.newLine();
		output.write(String.format("zone i=%d j=%d k=%d%n", nx, ny, nz));
		for (int k=0; k<nz; ++k)
		    for (int i=0; i<ny; ++i)
			for (int j=0; j<nx; ++j)
			{
			    r = center.distance(grid[i][j])/convert;
			    az = center.azimuth(grid[i][j], 0.);
			    output.write(String.format("%12.6f %12.6f %12.6f %12.6f %12.6f%n",
				    r*Math.sin(az), 
				    r*Math.cos(az),
				    depth0 + k*ddepth,
				    Math.sqrt(ssqr[i][j][k]/definingVec.size()),
				    (ssqr[i][j][k]-ssqr0)/kappa_sqr
				    ));
			}
	    }
	    else if (nz > 1 && gridUnits.equals("degrees"))
	    {
		output.write("variables = \"Longitude (deg)\" \"Latitude (deg)\" \"Depth (km)\" \"RMS Weighted Residuals\" \"Confidence Level\"");
		output.newLine();
		output.write(String.format("zone i=%d j=%d k=%d%n", nx, ny, nz));
		for (int k=0; k<nz; ++k)
		    for (int i=0; i<ny; ++i)
			for (int j=0; j<nx; ++j)
			    output.write(String.format("%12.6f %12.6f %12.6f %12.6f %12.6f%n",
				    grid[i][j].getLonDegrees(),
				    grid[i][j].getLatDegrees(),
				    depth0 + k*ddepth,
				    Math.sqrt(ssqr[i][j][k]/definingVec.size()),
				    (ssqr[i][j][k]-ssqr0)/kappa_sqr
				    ));
	    }
	    output.close();
	}
	else if (gridFileFormat.equals("vtk"))
	{
	    if (logger.getVerbosity() >= 1)
		logger.write(String.format("Writing gridded residuals to file %s%n",
			outputFile.getCanonicalPath()));

	    DataOutputStream output = new DataOutputStream(
		    new BufferedOutputStream(new FileOutputStream(outputFile)));

	    output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
	    output.writeBytes(String.format("LocOO3D_Gridded_Residuals%n"));
	    output.writeBytes(String.format("BINARY%n"));

	    output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

	    output.writeBytes(String.format("POINTS %d double%n", nx*ny*nz));

	    if (gridUnits.equals("km"))
		for (int k=0; k<nz; ++k)
		    for (int i=0; i<ny; ++i)
			for (int j=0; j<nx; ++j)
			{
			    r = center.distance(grid[i][j])/convert;
			    az = center.azimuth(grid[i][j], 0.);
			    x = r*Math.sin(az);
			    y = r*Math.cos(az);
			    output.writeDouble(x);
			    output.writeDouble(y);
			    output.writeDouble(depth0 + k*ddepth);
			}
	    else if (gridUnits.equals("degrees"))
		for (int k=0; k<nz; ++k)
		    for (int i=0; i<ny; ++i)
			for (int j=0; j<nx; ++j)
			{
			    x = grid[i][j].getLonDegrees();
			    y = grid[i][j].getLatDegrees();
			    output.writeDouble(x);
			    output.writeDouble(y);
			    output.writeDouble(depth0 + k*ddepth);
			}

	    if (nz == 1)
	    {
		int nCells = (nx-1)*(ny-1);
		output.writeBytes(String.format("CELLS %d %d%n", nCells, nCells*5));

		for (int i=0; i<ny-1; ++i)
		    for (int j=0; j<nx-1; ++j)
		    {
			output.writeInt(4);
			output.writeInt(i*nx+j);
			output.writeInt(i*nx+j+1);
			output.writeInt((i+1)*nx+j+1);
			output.writeInt((i+1)*nx+j);					
		    }

		output.writeBytes(String.format("CELL_TYPES %d%n", nCells));
		for (int t = 0; t < nCells; ++t)
		    output.writeInt(9); // vtk_quad
	    }
	    else
	    {
		int nCells = (nx-1)*(ny-1)*(nz-1);
		output.writeBytes(String.format("CELLS %d %d%n", nCells, nCells*9));

		for (int k=0; k<nz-1; ++k)
		    for (int i=0; i<ny-1; ++i)
			for (int j=0; j<nx-1; ++j)
			{
			    output.writeInt(8);
			    output.writeInt(k*nx*ny+i*nx+j);
			    output.writeInt(k*nx*ny+i*nx+j+1);
			    output.writeInt(k*nx*ny+(i+1)*nx+j+1);
			    output.writeInt(k*nx*ny+(i+1)*nx+j);					
			    output.writeInt((k+1)*nx*ny+i*nx+j);
			    output.writeInt((k+1)*nx*ny+i*nx+j+1);
			    output.writeInt((k+1)*nx*ny+(i+1)*nx+j+1);
			    output.writeInt((k+1)*nx*ny+(i+1)*nx+j);					
			}

		output.writeBytes(String.format("CELL_TYPES %d%n", nCells));
		for (int t = 0; t < nCells; ++t)
		    output.writeInt(12); // vtk_hexahedron
	    }

	    output.writeBytes(String.format("POINT_DATA %d%n", ny*nx*nz));

	    output.writeBytes("SCALARS RMS_Weighted_Residuals float 1\n");
	    output.writeBytes("LOOKUP_TABLE default\n");

	    for (int k=0; k<nz; ++k)
		for (int i=0; i<ny; ++i)
		    for (int j=0; j<nx; ++j)
			output.writeFloat((float) Math.sqrt(ssqr[i][j][k]/definingVec.size()));

	    output.writeBytes("SCALARS Confidence_Level float 1\n");
	    output.writeBytes("LOOKUP_TABLE default\n");

	    for (int k=0; k<nz; ++k)
		for (int i=0; i<ny; ++i)
		    for (int j=0; j<nx; ++j)
			output.writeFloat((float)((ssqr[i][j][k]-ssqr0)/kappa_sqr));

	    output.close();

	    if (locationTrack != null && locationTrack.size() > 0)
	    {
		output = new DataOutputStream(
			new BufferedOutputStream(new FileOutputStream(
				new File(outputFile.getParent(), "location_track.vtk"))));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("LocOO3D_LocationTrack%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

		output.writeBytes(String.format("POINTS %d double%n", locationTrack.size()));

		if (gridUnits.equals("km"))
		    for (int i=0; i<locationTrack.size(); ++i)
		    {
			r = center.distance(locationTrack.get(i))/convert;
			az = center.azimuth(locationTrack.get(i), 0.);
			x = r*Math.sin(az);
			y = r*Math.cos(az);
			output.writeDouble(x);
			output.writeDouble(y);
			output.writeDouble(depth0);
		    }
		else if (gridUnits.equals("degrees"))
		    for (int k=0; k<nz; ++k)
			for (int i=0; i<ny; ++i)
			    for (int j=0; j<nx; ++j)
			    {
				x = locationTrack.get(i).getLonDegrees();
				y = locationTrack.get(i).getLatDegrees();
				output.writeDouble(x);
				output.writeDouble(y);
				output.writeDouble(depth0);
			    }
		// write out node connectivity
		output.writeBytes(String.format("CELLS %d %d%n", 1, locationTrack.size()+1));

		output.writeInt(locationTrack.size());
		for (int i=0; i<locationTrack.size(); ++i)
		    output.writeInt(i);

		output.writeBytes(String.format("CELL_TYPES %d%n", 1));
		output.writeInt(4);

		output.close();





		output = new DataOutputStream(
			new BufferedOutputStream(new FileOutputStream(
				new File(outputFile.getParent(), "location_track_points.vtk"))));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("LocOO3D_LocationTrack%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

		output.writeBytes(String.format("POINTS %d double%n", locationTrack.size()));

		if (gridUnits.equals("km"))
		    for (int i=0; i<locationTrack.size(); ++i)
		    {
			r = center.distance(locationTrack.get(i))/convert;
			az = center.azimuth(locationTrack.get(i), 0.);
			x = r*Math.sin(az);
			y = r*Math.cos(az);
			output.writeDouble(x);
			output.writeDouble(y);
			output.writeDouble(depth0);
		    }
		else if (gridUnits.equals("degrees"))
		    for (int k=0; k<nz; ++k)
			for (int i=0; i<ny; ++i)
			    for (int j=0; j<nx; ++j)
			    {
				x = locationTrack.get(i).getLonDegrees();
				y = locationTrack.get(i).getLatDegrees();
				output.writeDouble(x);
				output.writeDouble(y);
				output.writeDouble(depth0);
			    }
		// write out node connectivity
		output.writeBytes(String.format("CELLS %d %d%n", 1, locationTrack.size()+1));

		output.writeInt(locationTrack.size());
		for (int i=0; i<locationTrack.size(); ++i)
		    output.writeInt(i);

		output.writeBytes(String.format("CELL_TYPES %d%n", 1));
		output.writeInt(2);
		output.close();
	    }
	}

	getEventParameters().needDerivatives(needDerivatives);

	if (logger.getVerbosity() > 0)
	    System.out.println("Gridded residuals written to\n"+
		    outputFile.getCanonicalPath()+"\nin "+gridFileFormat+" format");
    }

    @Override
    public double bFunc(double originTime) throws Exception
    {
	double sum = 0.;
	for (int i=0; i<tBrent.size(); ++i)
	    sum += (tBrent.get(i)-originTime)*(tBrent.get(i)-originTime);
	return sum;
    }

    /**
     * Bracket a minimum of function bFunc
     * @param x1 initial estimate of left abscissa
     * @param x2 initial estimate of right abscissa
     * @return two abscissa values that bracket the minimum.
     * @throws Exception
     */
    private double[] mnbrak(double x1, double x2) throws Exception
    {
	double GOLD=1.618034,GLIMIT=100.0,TINY=1.0e-20;
	double ulim,r,q;

	double[] f = new double[4];
	double[] x = new double[] {x1, x2, 0., 0.};

	f[0]=bFunc(x[0]);
	f[1]=bFunc(x[1]);
	if (f[1] > f[0]) 
	{
	    x[2] = x[0];
	    x[0] = x[1];
	    x[1] = x[2];
	    f[2] = f[0];
	    f[0] = f[1];
	    f[1] = f[2];
	}
	x[2]=x[1]+GOLD*(x[1]-x[0]);
	f[2]=bFunc(x[2]);
	while (f[1] > f[2]) {
	    r=(x[1]-x[0])*(f[1]-f[2]);
	    q=(x[1]-x[2])*(f[1]-f[0]);
	    x[3]=x[1]-((x[1]-x[2])*q-(x[1]-x[0])*r)/
		    (2.0*nr_sign(Math.max(abs(q-r),TINY),q-r));
	    ulim=x[1]+GLIMIT*(x[2]-x[1]);
	    if ((x[1]-x[3])*(x[3]-x[2]) > 0.0) {
		f[3]=bFunc(x[3]);
		if (f[3] < f[2]) {
		    x[0]=x[1];
		    x[1]=x[3];
		    f[0]=f[1];
		    f[1]=f[3];
		    return new double[] {x[0], x[2]};
		} else if (f[3] > f[1]) {
		    x[2]=x[3];
		    f[2]=f[3];
		    return new double[] {x[0], x[2]};
		}
		x[3]=x[2]+GOLD*(x[2]-x[1]);
		f[3]=bFunc(x[3]);
	    } else if ((x[2]-x[3])*(x[3]-ulim) > 0.0) {
		f[3]=bFunc(x[3]);
		if (f[3] < f[2]) 
		{
		    nr_shft3(x,x[3]+GOLD*(x[3]-x[2]));
		    nr_shft3(f,bFunc(x[3]));
		}
	    } else if ((x[3]-ulim)*(ulim-x[2]) >= 0.0) {
		x[3]=ulim;
		f[3]=bFunc(x[3]);
	    } else {
		x[3]=x[2]+GOLD*(x[2]-x[1]);
		f[3]=bFunc(x[3]);
	    }
	    nr_shft3(x);
	    nr_shft3(f);
	}
	return new double[] {x[0], x[2]};
    }

    private double nr_sign(double a, double b)
    {return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);}

    private void nr_shft3(double[] a)	{a[0]=a[1];	a[1]=a[2]; a[2]=a[3];}

    private void nr_shft3(double[] x, double y) {x[1]=x[2];	x[2]=x[3];	x[3]=y;	}

    public void setInitialLocation(Location initialLocation)
    {
	this.initialLocation = initialLocation;
    }

    /**
     * Get the depth of the upper or lower depth constraint at the 
     * current location.
     * @return depth of constraint, in km
     * @throws Exception
     */
    protected double getSeismicityDepthRange() throws Exception
    {
	return parameters.getSeismicityDepthRange(getUnitVector())[fixedDepthIndex];
    }
    /**
     * Number of times sum squared weighted residuals are compute.
     * Value is updated in method update().
     * @return
     */
    public int getnSSWR() {
	return nSSWR;
    }

    public EventParameters getEventParameters() { return parameters; }

    public Source getSource() { return source; }

}
