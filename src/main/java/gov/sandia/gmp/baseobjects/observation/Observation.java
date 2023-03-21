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
package gov.sandia.gmp.baseobjects.observation;

import static gov.sandia.gmp.util.globals.Globals.TWO_PI;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.SiteInterface;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.gmp.Srcobsassoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;

/**
 * <p>
 * Observation
 * 
 * <p>
 * An Observation represents a set of observation components (travel time,
 * azimuth, horizontal slowness) associated with a single wiggle on a
 * seismogram. It has fields for all the pertinent information in Arrival and
 * Assoc tables in the KB Core Schema. It also has references to the Source
 * object (origin) and Receiver object (site) with which it is associated. 
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
public class Observation extends PredictionRequest implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -6169774768813956516L;

    /**
     * Arrival time (epoch time) in seconds
     */
    private double arrivaltime;

    /**
     * Uncertainty of observed arrival time, in seconds
     */
    private double deltim;

    /**
     * Whether or not travel time is defining.  Can be modified with call to setTimedef()
     */
    private boolean timedef;

    /**
     * Whether or not travel time is defining. One of d, D, n, N.  Set in constructor and cannot change
     */
    private final char timedefChar;

    /**
     * Observed station-source azimuth.  Clockwise from north in radians.  Range is 0 to 2*PI
     */
    private double azimuth;

    /**
     * Uncertainty of observed azimuth in radians. 
     */
    private double delaz;

    /** 
     * Whether or not azimuth is defining.  Can be modified with call to setAzdef()
     */
    private boolean azdef;  

    /**
     * Whether or not azimuth is defining. One of d, D, n, N.  Set in constructor and cannot change
     */
    private final char azdefChar; 

    /**
     * Observed slowness in seconds/radian.  
     */
    private double slow;  

    /**
     * Uncertainty of observed slowness in seconds/radian
     */
    private double delslo;

    /**
     * Whether or not slowness is defining.  Can be modified with call to setSlodef()
     */
    private boolean slodef; 
    
    /**
     * If master event corrections for tt, az, sh are to be applied to this obsercation
     * than masterEventCorrections will be a 3-element array with values for tt, az, sh.
     * Otherwise, masterEventCorrections will be null.
     */
    private double[] masterEventCorrections;

    /**
     * Whether or not slowness is defining. One of d, D, n, N.  Set in constructor and cannot change
     */
    private final char slodefChar;

    /**
     * View into this Observation object focused on travel time
     */
    private ObservationComponent componentTT = new ObservationTT(this);

    /**
     * View into this Observation object focused on azimuth
     */
    private ObservationComponent componentAZ = new ObservationAZ(this);

    /**
     * View into this Observation object focused on horizontal slowness
     */
    private ObservationComponent componentSH = new ObservationSH(this);

    /**
     * Map from TRAVEL_TIME -> componentTT, AZIMUTH -> componentAZ, SLOWNESS -> componentSH
     */
    private final Map<GeoAttributes, ObservationComponent> observationComponents = Map.ofEntries(
	    new LinkedHashMap.SimpleEntry<GeoAttributes, ObservationComponent>(GeoAttributes.TRAVEL_TIME, componentTT),
	    new LinkedHashMap.SimpleEntry<GeoAttributes, ObservationComponent>(GeoAttributes.AZIMUTH, componentAZ),
	    new LinkedHashMap.SimpleEntry<GeoAttributes, ObservationComponent>(GeoAttributes.SLOWNESS, componentSH)
	    );

    /**
     * When a prediction is computed for this observation, modelName is set to the name of the model used, 
     * e.g. ak135, salsa3d, etc.
     */
    private String modelName;

    /**
     * When a prediction is computed for this observation, predictorName is set to the name of the predictor used, 
     * e.g. lookup2d, rstt, bender, etc.
     */
    private String predictorName;

    /**
     * When a prediction is computed, this map is populated with values like
     * travel_time, azimuth, slowness, derivatives, path_corrections, etc.
     */
    private EnumMap<GeoAttributes, Double> predictions;

    /**
     * RayTypes include things like REFRACTION, REFLECTION, DIFFRACTION, UNKNOWN, INVALID and more
     */
    private RayType predictionRayType;

    /**
     * True if the predicted values needed by the locator are currently valid. 
     */
    private boolean predictionValid;

    /**
     * Used to store any error messages generated by the Predictor.
     */
    private String predictionErrorMessage;

    /**
     * True if predictions are currently up to date.  Will be set to false
     * if, say, the source moves during relocation.
     */
    private boolean predictionUpToDate;

    /**
     * Used in database interactions.  Value, getter and setter provided only.
     */
    private long modelId;

    /**
     * Used in database interactions.  Value, getter and setter provided only.
     */
    private long algorithmId;

    /**
     * When lots of SiteInterface objects are converted to Receivers, this map is used
     * to avoid creating many new instances of the same receiver.
     */
    private final static Map<SiteInterface, Receiver> siteReceiverMap = new HashMap<>();
    private static Receiver getReceiver(SiteInterface site) throws Exception { 
	Receiver r = siteReceiverMap.get(site);
	if (r == null) siteReceiverMap.put(site, r = new Receiver(site));
	return r;
    }


    public Observation() {
	super();
	timedefChar = '-';
	azdefChar = '-';
	slodefChar = '-';
    }

    /**
     * Constructor.
     * 
     * @param observationId
     * @param receiver
     * @param source
     * @param phase
     * @param time in seconds
     * @param deltim in seconds
     * @param timedef, one of [ d | D | n | N ]  (defining or non-defining)
     * @param azimuth       in degrees or radians
     * @param delaz         in degrees or radians
     * @param azdef, one of [ d | D | n | N ]  (defining or non-defining)
     * @param slow          in sec/degree or sec/radian
     * @param delslo        in sec/degree or sec/radian
     * @param slodef, one of [ d | D | n | N ]  (defining or non-defining)
     * @param inDegrees     if true input values of azimuth are assumed to be
     *                      degrees and slowness are assumed sec/degree, otherwise
     *                      radians and sec/radian
     * @throws Exception 
     */
    public Observation(long observationId, Receiver receiver, Source source, SeismicPhase phase,
	    double time, double deltim, String timedefString, 
	    double azimuth, double delaz, String azdefString, 
	    double slow, double delslo, String slodefString, 
	    boolean inDegrees) throws Exception {

	super(observationId, receiver, source, phase, EnumSet.noneOf(GeoAttributes.class), 
		(timedefString.equalsIgnoreCase("d") || azdefString.equalsIgnoreCase("d") 
			|| slodefString.equalsIgnoreCase("d")));

	this.timedefChar = timedefString.charAt(0);
	this.azdefChar = azdefString.charAt(0);
	this.slodefChar = slodefString.charAt(0);

	// ensure that na_values are all Globals.NA_VALUE and convert degrees to radians.
	// Set defining false if observed value or uncertainty are invalid
	if (time == Arrival.TIME_NA)
	    time = Globals.NA_VALUE;

	if (deltim == Arrival.DELTIM_NA)
	    deltim = Globals.NA_VALUE;

	timedef = timedefString.equalsIgnoreCase("d") && this.arrivaltime != Globals.NA_VALUE && deltim > 0.;

	setTime(time);
	setDeltim(deltim);

	if (azimuth == Arrival.AZIMUTH_NA)
	    azimuth = Globals.NA_VALUE;
	if (azimuth != Globals.NA_VALUE && inDegrees)
	    azimuth = toRadians(azimuth);

	if (delaz == Arrival.DELAZ_NA)
	    delaz = Globals.NA_VALUE;
	if (delaz != Globals.NA_VALUE && inDegrees)
	    delaz = toRadians(delaz);

	azdef = azdefString.equalsIgnoreCase("d") && azimuth != Globals.NA_VALUE && delaz > 0.;

	setAzimuth(azimuth);
	setDelaz(delaz);

	if (slow == Arrival.SLOW_NA)
	    slow = Globals.NA_VALUE;
	if (slow != Globals.NA_VALUE && inDegrees)
	    slow = toDegrees(slow);

	if (delslo == Arrival.DELSLO_NA)
	    delslo = Globals.NA_VALUE;
	if (delslo != Globals.NA_VALUE && inDegrees)
	    delslo = toDegrees(delslo);

	slodef = slodefString.equalsIgnoreCase("d") && slow != Globals.NA_VALUE && delslo > 0.;

	setSlow(slow);
	setDelslo(delslo);

    }

    /**
     * Constructor.
     * 
     * @param observationId
     * @param receiver
     * @param source
     * @param phase
     * @param time in seconds
     * @param deltim in seconds
     * @param timedef time defining
     * @param azimuth       in degrees or radians
     * @param delaz         in degrees or radians
     * @param azdef azimuth defining
     * @param slow          in sec/degree or sec/radian
     * @param delslo        in sec/degree or sec/radian
     * @param slodef slowness defining
     * @param inDegrees     if true input values of azimuth are assumed to be
     *                      degrees and slowness are assumed sec/degree, otherwise
     *                      radians and sec/radian
     * @throws Exception
     */
    public Observation(long observationId, Receiver receiver, Source source, SeismicPhase phase,
	    double time, double deltim, boolean timedef, 
	    double azimuth, double delaz, boolean azdef, 
	    double slow, double delslo, boolean slodef, boolean inDegrees) throws Exception
    {
	this(observationId, receiver, source, phase, 
		time, deltim, (timedef ? "d" : "n"),
		azimuth, delaz, (azdef ? "d" : "n"),
		slow, delslo, (slodef ? "d" : "n"),
		inDegrees);
    }

    /**
     * Constructor that assumes all azimuth and slowness observations are NA_VALUE.
     * @param observationId
     * @param receiver
     * @param source
     * @param phase
     * @param arrivalTime
     * @param deltim
     * @param defining
     * @param requestedAttributes 
     * @throws Exception
     */
    public Observation(long observationId, Receiver receiver, Source source, SeismicPhase phase, double arrivalTime,
	    double deltim, boolean timedef, EnumSet<GeoAttributes> requestedAttributes) throws Exception 
    {
	this(observationId, receiver, source, phase, arrivalTime, deltim, timedef, 
		Globals.NA_VALUE, Globals.NA_VALUE, false, 
		Globals.NA_VALUE, Globals.NA_VALUE, false,  
		true);
	super.setRequestedAttributes(requestedAttributes);
    }

    public Observation(Source source, AssocExtended assoc) throws Exception {
	this(assoc.getArid(), getReceiver(assoc.getSite()), source, SeismicPhase.valueOf(assoc.getPhase()),
		assoc.getArrival().getTime(), assoc.getArrival().getDeltim(),assoc.getTimedef(),
		assoc.getArrival().getAzimuth(), assoc.getArrival().getDelaz(),assoc.getAzdef(),
		assoc.getArrival().getSlow(), assoc.getArrival().getDelslo(),assoc.getSlodef(),
		true);
    }

    public Observation(Receiver receiver, Source source, AssocExtended assoc, 
	    double[] masterEventCorrections) throws GMPException, Exception {
	this(assoc.getArid(), receiver, source,
		SeismicPhase.valueOf(assoc.getPhase()), 
		assoc.getArrival().getTime(), assoc.getArrival().getDeltim(), assoc.getTimedef(),
		assoc.getArrival().getAzimuth(), assoc.getArrival().getDelaz(), assoc.getAzdef(),
		assoc.getArrival().getSlow(), assoc.getArrival().getDelslo(), assoc.getSlodef(),
		true);

	this.masterEventCorrections = masterEventCorrections;
    }

    public Observation(Receiver receiver, Source source, Srcobsassoc assoc, 
	    double[] masterEventCorrections) throws GMPException, Exception {
	this(assoc.getObservationid(), receiver, source,
		SeismicPhase.valueOf(assoc.getPhase()), 
		assoc.getObservation().getArrivaltime(), assoc.getObservation().getTimeuncertainty(), assoc.getTimedef(),
		assoc.getObservation().getAzimuth(), assoc.getObservation().getAzuncertainty(), assoc.getAzdef(),
		assoc.getObservation().getSlowness(), assoc.getObservation().getSlowuncertainty(), assoc.getSlowdef(),
		true);

	this.masterEventCorrections = masterEventCorrections;
    }

    public Map<GeoAttributes, ObservationComponent> getObservationComponents() {
	return observationComponents;
    }

    /**
     * Returns timedef || azdef || slodef;
     */
    @Override
    public boolean isDefining() {
	return isTimedef() || isAzdef() || isSlodef();
    }

    @Override
    public void setDefining(boolean defining) {
	super.setDefining(defining);
    }

    public EnumMap<GeoAttributes, Double> getPredictions() {
	return predictions;
    }


    public double getPrediction(GeoAttributes attribute) {
	return predictions.containsKey(attribute) ? predictions.get(attribute) : Globals.NA_VALUE;
    }

    public double[] getPredictions(GeoAttributes[] attributes) {
	double[] p = new double[attributes.length];
	for (int i=0; i<attributes.length; ++i) 
	    p[i] = getPrediction(attributes[i]);
	return p;
    }

    public void predictionUpToDate(boolean predictionUpToDate) {
	this.predictionUpToDate = predictionUpToDate; 
    }

    public boolean predictionUpToDate() {
	return predictionUpToDate;
    }

    public boolean predictionValid() {
	return this.predictionValid;
    }

    protected void predictionValid(boolean valid) {
	this.predictionValid = valid; 
    }

    /**
     * Set the Prediction object for this Observation. This method will extract
     * information to update internal values of timeres, azres and slowres. Consider
     * using updatePrediction() instead.
     * 
     * @param prediction
     */
    public Observation setPrediction(Prediction prediction) {
	this.predictions = prediction.getValues();
	this.predictionRayType = prediction.getRayType();
	this.predictionValid = prediction.isValid();
	this.predictionErrorMessage = prediction.getErrorMessage();
	this.predictionUpToDate = true;

	if (masterEventCorrections != null) {
	    this.predictions.put(GeoAttributes.TT_MASTER_EVENT_CORRECTION, masterEventCorrections[0]);
	    this.predictions.put(GeoAttributes.AZIMUTH_MASTER_EVENT_CORRECTION, masterEventCorrections[1]);
	    this.predictions.put(GeoAttributes.SLOWNESS_MASTER_EVENT_CORRECTION, masterEventCorrections[2]);
	}
	
	componentTT.setPrediction();
	componentAZ.setPrediction();
	componentSH.setPrediction();

	return this;
    }

    /**
     * Ensure that timeres, azres and slores are up to date with current Prediction.
     * It may be necessary to call this, even when Prediction has not changed, if
     * origin time has changed, since that will change timeres.
     */
    public void updateResiduals() {
	componentTT.updateResidual();
	componentAZ.updateResidual();
	componentSH.updateResidual();
	this.predictionUpToDate = true;
    }

    /**
     * Return the observed arrival time. This is epoch time, seconds since 1970.
     * 
     * @return observed arrival time
     */
    public double getArrivalTime() {
	return arrivaltime;
    }

    /**
     * Set the arrival time. Specify an epoch time, seconds since 1970.
     * 
     * @param arrivalTime
     */
    public void setArrivalTime(double arrivalTime) {
	arrivaltime = arrivalTime;
	this.predictionUpToDate = false;
    }

    /**
     * Retrieve the travel time in seconds. This is observed arrival time - source
     * origin time, in seconds.
     * 
     * @return travel time in seconds
     */
    public double getTravelTime() {
	if (getArrivalTime() == Globals.NA_VALUE || source.getOriginTime() == Globals.NA_VALUE)
	    return Globals.NA_VALUE;
	return getArrivalTime() - source.getOriginTime();
    }

    /**
     * Given a Receiver object, return true if this Observation has the same sta and
     * the time of this Observation falls within Receiver ondate-offdate.
     * 
     * @param site
     * @return boolean
     */
    public boolean checkSta(Receiver site) {
	if (!site.getSta().equals(receiver.getSta()))
	    return false;

	int jdate = GMTFormat.getJDate(getArrivalTime());
	return jdate >= site.getOndate() && jdate <= site.getOffdate();
    }

    /**
     * Retrieve data needed to make a Source database row.
     * 
     * @return info
     */
    public gov.sandia.gnem.dbtabledefs.gmp.Source getSourceRow() {
	return source.getSourceRow();
    }

    /**
     * Retrieve data needed to make a Receiver database row.
     * 
     * @return receiver row
     */
    public gov.sandia.gnem.dbtabledefs.gmp.Receiver getReceiverRow() {
	return receiver.getReceiverRow();
    }

    /**
     * Retrieve data needed to make an Observation database row.
     * 
     * @return observation row
     */
    public gov.sandia.gnem.dbtabledefs.gmp.Observation getObservationRow() {
	return new gov.sandia.gnem.dbtabledefs.gmp.Observation(observationId, receiver.getReceiverId(),
		phase.toString(), getArrivalTime(), getDeltim(), degrees(getAzimuth(), Globals.NA_VALUE),
		degrees(getDelaz(), Globals.NA_VALUE), radians(getSlow(), Globals.NA_VALUE),
		radians(getDelslo(), Globals.NA_VALUE), GMPGlobals.getAuth());
    }

    /**
     * Retrieve data needed to make a SrcObsAssoc database row.
     * 
     * @return srcobsassoc row
     */
    public Srcobsassoc getSrcobsassocRow() {
	return new Srcobsassoc(source.getSourceId(), observationId, phase.toString(),
		degrees(getDistance(), Globals.NA_VALUE), degrees(getEsaz(), Globals.NA_VALUE),
		degrees(getSeaz(), Globals.NA_VALUE), isTimedef() ? "d" : "n", isAzdef() ? "d" : "n",
			isSlodef() ? "d" : "n", GMPGlobals.getAuth());
    }

    /**
     * Retrieve data needed to make an Origin database row.
     * 
     * @return origin row
     */
    public Origin getOriginRow() {
	return source.getOriginRow();
    }

    /**
     * Retrieve data needed to make a Site database row.
     * 
     * @return site row
     */
    public Site getSiteRow() {
	return receiver.getSiteRow();
    }

    /**
     * Retrieve data needed to make an Arrival database row.
     * 
     * @return arrival row
     */
    public Arrival getArrivalRow() {
	Arrival row = new Arrival();
	row.setSta(receiver.getSta());
	row.setTime(getArrivalTime());
	row.setArid(observationId);
	row.setJdate(GMTFormat.getJDate(getArrivalTime()));
	row.setIphase(phase.toString());
	row.setDeltim(getDeltim() == Globals.NA_VALUE ? Arrival.DELTIM_NA : getDeltim());
	row.setAzimuth(degrees(getAzimuth(), Arrival.AZIMUTH_NA));
	row.setDelaz(degrees(getDelaz(), Arrival.DELAZ_NA));
	row.setSlow(radians(getSlow(), Arrival.SLOW_NA));
	row.setDelslo(radians(getDelslo(), Arrival.DELSLO_NA));
	row.setAuth(GMPGlobals.getAuth());

	return row;
    }

    /**
     * Retrieve data needed to make an Assoc database row.
     * 
     * @return assoc row
     */
    public AssocExtended getAssocRow() {
	try {
	    String vmodel = getModelName();
	    if (vmodel.length() > 15)
		vmodel = vmodel.substring(0, 15);

	    return new AssocExtended(observationId, source.getSourceId(), receiver.getSta(), phase.toString(), -1.,
		    degrees(getDistance(), -1.), degrees(getSeaz(Globals.NA_VALUE), -1.),
		    degrees(getEsaz(Globals.NA_VALUE), -1.),
		    getTimeres() == Globals.NA_VALUE ? Assoc.TIMERES_NA : getTimeres(),
			    isTimedef() ? "d" : "n", degrees(getAzres(), Assoc.AZRES_NA), isAzdef() ? "d" : "n",
				    radians(getSlores(), Assoc.SLORES_NA), isSlodef() ? "d" : "n", Assoc.EMARES_NA, Assoc.WGT_NA, vmodel, -1);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * When writing a row in to the Prediction Table there is a column that
     * indicates whether the source and/or the receiver were inside a Polygon. What
     * is required is a two character String that should equal one of: " ", "s",
     * "r", "sr" where 's' indicates the source was inside the polygon and 'r'
     * indicates the receiver was inside the polygon. Derived classes must supply
     * the required information.
     * 
     * @return in polygon
     */
    public String getInPolygon() { return inPolygon; }
    private String inPolygon = "  ";
    public void setInPolygon(String in) { this.inPolygon = in; }

    /**
     * Retrieve data needed to make a Prediction database row.
     * 
     * @return prediction row
     */
    public gov.sandia.gnem.dbtabledefs.gmp.Prediction getPredictionRow() {
	return new gov.sandia.gnem.dbtabledefs.gmp.Prediction(-1, source.getSourceId(), receiver.getReceiverId(), modelId,
		algorithmId, observationId, getPhase().toString(),
		predictionRayType.toString(), getPrediction(GeoAttributes.ACTIVE_FRACTION),
		getPrediction(GeoAttributes.TRAVEL_TIME), getTimeres(),
		getPrediction(GeoAttributes.AZIMUTH_DEGREES),
		getPrediction(GeoAttributes.SLOWNESS_DEGREES),
		getPrediction(GeoAttributes.BACKAZIMUTH_DEGREES),
		getPrediction(GeoAttributes.TURNING_DEPTH),
		getPrediction(GeoAttributes.OUT_OF_PLANE),
		getPrediction(GeoAttributes.CALCULATION_TIME), getInPolygon(), GMPGlobals.getAuth());
    }

    /**
     * Retrieve data needed to make an Observation database row.
     * 
     * @return Object[]
     */
    public Object[] getObservationForDb() {
	Object values[] = new Object[11];
	values[0] = observationId; // observationid
	values[1] = receiver.getReceiverId(); // receiverid
	values[2] = phase.toString(); // iphase
	values[3] = new java.sql.Timestamp((long) (getArrivalTime() * 1000)); // arrivalTime converted to timestamp
	values[4] = getDeltim(); // timeUncertainty

	// Note that the values for azimuth and slowness are OBSERVED values, not
	// predicted
	values[5] = degrees(getAzimuth(), Globals.NA_VALUE); // azimuth or
	// degrees(rayInfo.getAttribute(GeoAttributes.AZIMUTH))
	values[6] = degrees(getDelaz(), Globals.NA_VALUE); // azUncertainty
	values[7] = radians(getSlow(), Globals.NA_VALUE); // slowness or
	// convertSlow(rayInfo.getAttribute(GeoAttributes.PHSLOWNESS))
	values[8] = radians(getDelslo(), Globals.NA_VALUE); // slowUncertainty

	values[9] = GMPGlobals.getAuth(); // auth
	values[10] = GMPGlobals.getLddate(); // lddate

	return values;
    }

    /**
     * Retrieve data needed to make a SrcObsAssoc database row.
     * 
     * @return Object[]
     */
    public Object[] getSrcObsAssocForDb() {
	Object values[] = new Object[11];
	values[0] = Long.valueOf(source.getSourceId()); // sourceid
	values[1] = Long.valueOf(observationId); // observationid
	values[2] = phase.toString(); // phase
	values[3] = Double.valueOf(getDistanceDegrees()); // delta
	values[4] = Double.valueOf(getEsaz(0.)); // esaz
	values[5] = Double.valueOf(getSeaz(0.)); // seaz
	values[6] = (isTimedef() ? "d" : "n"); // timedef
	values[7] = (isAzdef() ? "d" : "n"); // azdef
	values[8] = (isSlodef() ? "d" : "n"); // slodef
	values[9] = GMPGlobals.getAuth(); // auth
	values[10] = GMPGlobals.getLddate(); // lddate

	return values;
    }

    /**
     * Retrieve data needed to make a PREDICTION database row.
     *
     * @return Object[]
     * @param outputPredictionId Long
     * @param geomodelid         Long
     * @param algorithmid        Long
     */
    public Object[] getPredictionForDb(Long outputPredictionId, Long geomodelid, Long algorithmid) {
	Object values[] = new Object[19];
	values[0] = outputPredictionId; // predictionid
	values[1] = Long.valueOf(source.getSourceId()); // sourceid
	values[2] = Long.valueOf(receiver.getReceiverId()); // receiverid
	values[3] = geomodelid; // geomodelid
	values[4] = algorithmid; // ttalgoid
	values[5] = Long.valueOf(observationId);
	values[6] = phase.toString(); // phase
	values[7] = predictionRayType.toString(); // raytype
	values[8] = Double.valueOf(getPrediction(GeoAttributes.ACTIVE_FRACTION)); // activefraction
	values[9] = Double.valueOf(getPrediction(GeoAttributes.TRAVEL_TIME));
	values[10] = degrees(getPrediction(GeoAttributes.AZIMUTH), Globals.NA_VALUE); // azimuth
	values[11] = radians(getPrediction(GeoAttributes.SLOWNESS), Globals.NA_VALUE); // slowness
	values[12] = degrees(getPrediction(GeoAttributes.BACKAZIMUTH), Globals.NA_VALUE); // backazimuth
	values[13] = Double.valueOf(getPrediction(GeoAttributes.TURNING_DEPTH)); // turndepth
	values[14] = Double.valueOf(getPrediction(GeoAttributes.OUT_OF_PLANE)); // maxoutplane
	values[15] = getPrediction(GeoAttributes.CALCULATION_TIME); // calctime
	values[16] = getInPolygon();
	values[17] = GMPGlobals.getAuth(); // auth
	values[18] = GMPGlobals.getLddate(); // lddate
	return values;
    }

    /**
     * if x is equal to Globals.NA_VALUE, return the specified na_value. Otherwise,
     * return toDegrees(x).
     * 
     * @param x
     * @param na_value
     * @return
     */
    private double degrees(double x, double na_value) {
	if (x == Globals.NA_VALUE)
	    return na_value;
	return toDegrees(x);
    }

    /**
     * if x is equal to Globals.NA_VALUE, return the specified na_value. Otherwise,
     * return toRadians(x).
     * 
     * @param x
     * @param na_value
     * @return
     */
    private double radians(double x, double na_value) {
	if (x == Globals.NA_VALUE)
	    return na_value;
	return toRadians(x);
    }

    /**
     * get arrival time in sec
     * @return
     */
    public double getTime() {
	return arrivaltime;
    }

    /**
     * set arrival time in sec
     * @param time
     */
    public void setTime(double time) {
	this.arrivaltime = time;
    }

    /**
     * get the uncertainty of observed arrival time in sec
     * @return
     */
    public double getDeltim() {
	return deltim;
    }

    /**
     * set the uncertainty of observed arrival time in sec
     * @param deltim
     */
    public void setDeltim(double deltim) {
	this.deltim = deltim;
    }

    public boolean isTimedef() {
	return timedef;
    }

    public void setTimedef(boolean timedef) {
	this.timedef = timedef;
    }

    /**
     * Azimuth in radians. Range [ 0 .. 2*PI ]
     * @return
     */
    public double getAzimuth() {
	return azimuth;
    }

    /**
     * Set azimuth in radians. Range will be adjusted to [ 0 .. 2*PI ] if necessary.
     * @param azimuth
     */
    public void setAzimuth(double azimuth) {
	this.azimuth = (azimuth + TWO_PI) % TWO_PI;
    }

    /**
     * uncertainty of observed azimuth in radians
     * @return
     */
    public double getDelaz() {
	return delaz;
    }

    /**
     * set uncertainty of observed azimuth in radians
     * @param delaz
     */
    public void setDelaz(double delaz) {
	this.delaz = delaz;
    }

    public boolean isAzdef() {
	return azdef;
    }

    public void setAzdef(boolean azdef) {
	this.azdef = azdef;
    }

    /**
     * get slowness in sec/radian
     * @return
     */
    public double getSlow() {
	return slow;
    }

    /**
     * set slowness in sec/radian
     * @param slow
     */
    public void setSlow(double slow) {
	this.slow = slow;
    }

    /**
     * get uncertainty of observed slowness in sec/radian
     * @return
     */
    public double getDelslo() {
	return delslo;
    }

    /**
     * set uncertainty of observed slowness in sec/radian
     * @param delslo
     */
    public void setDelslo(double delslo) {
	this.delslo = delslo;
    }

    public boolean isSlodef() {
	return slodef;
    }

    public void setSlodef(boolean slodef) {
	this.slodef = slodef;
    }

    /**
     * get azimuth residual in radians
     * @return
     */
    public double getAzres() {
	return componentAZ.getResidual();
    }

    /**
     * get slowness residual in sec/radian
     * @return
     */
    public double getSlores() {
	return componentSH.getResidual();
    }

    /**
     * get travel time residual in seconds
     * @return
     */
    public double getTimeres() {
	return componentTT.getResidual();
    }

    public double[] getMasterEventCorrections() {
	return masterEventCorrections;
    }

    public String getModelName() {
	return modelName == null ? Assoc.VMODEL_NA : modelName;
    }

    public void setModelName(String modelName) {
	this.modelName = modelName;
    }

    public String getPredictorName() {
	return predictorName;
    }

    public void setPredictorName(String predictorName) {
	this.predictorName = predictorName;
    }

    /**
     * Weight assigned to travel time residuals during location.
     * Units are 1/seconds.
     * @return
     */
    public double getTtWeight() {
	return componentTT.getWeight();
    }

    /**
     * Weight assigned to azimuth residuals during location.
     * Units are 1/radians
     * @return
     */
    public double getAzWeight() {
	return componentAZ.getWeight();
    }

    /**
     * Weight assigned to slowness residuals during location.
     * Units are radians/second.
     * @return
     */
    public double getShWeight() {
	return componentSH.getWeight();
    }

    /**
     * One of 'd', 'D', 'n','N'
     * @return
     */
    public char getTimedefChar() {
	return timedefChar;
    }

    /**
     * One of 'd', 'D', 'n','N'
     * @return
     */
    public char getAzdefChar() {
	return azdefChar;
    }

    /**
     * One of 'd', 'D', 'n','N'
     * @return
     */
    public char getSlodefChar() {
	return slodefChar;
    }

    public boolean isTimedefOriginal() {
	return timedefChar == 'd' || timedefChar == 'D';
    }

    public boolean isSlodefOriginal() {
	return slodefChar == 'd' || slodefChar == 'D';
    }

    public boolean isAzdefOriginal() {
	return azdefChar == 'd' || azdefChar == 'D';
    }

    /**
     * Clear the set of requested attributes associated with this arrival
     * and reset it with attributes necessary to compute predictions 
     */
    public void setRequestedAttributes(boolean needDerivatives)
    {
	requestedAttributes.clear();
	if (componentTT.isDefining()) componentTT.addRequiredAttributes(requestedAttributes, needDerivatives);
	if (componentAZ.isDefining()) componentAZ.addRequiredAttributes(requestedAttributes, needDerivatives);
	if (componentSH.isDefining()) componentSH.addRequiredAttributes(requestedAttributes, needDerivatives);
    }

    @Override
    public String toString() {
	return getBuff().toString();
    }

    public Buff getBuff() {
	Buff buffer = new Buff(this.getClass().getSimpleName());
	buffer.add("format", 1);
	buffer.add("observationsId", observationId);
	buffer.add("sourceId", getSourceId());
	buffer.add("phase", phase.toString());

	buffer.add("arrivalTime", arrivaltime, 3);
	buffer.add("deltim", deltim);
	buffer.add("timeres", getTimeres(), 3);
	buffer.add("timedef", timedef);
	buffer.add("timedefChar", Character.toString(timedefChar));

	buffer.add("azimuth", degrees(azimuth, Globals.NA_VALUE), 3);
	buffer.add("delaz", degrees(delaz, Globals.NA_VALUE), 3);
	buffer.add("azres", degrees(getAzres(), Globals.NA_VALUE), 3);
	buffer.add("azdef", azdef);
	buffer.add("azdefChar", Character.toString(azdefChar));

	buffer.add("slow", radians(slow, Globals.NA_VALUE), 3);
	buffer.add("delslo", radians(delslo, Globals.NA_VALUE), 3);
	buffer.add("slores", radians(getSlores(), Globals.NA_VALUE), 3);
	buffer.add("slodef", slodef);
	buffer.add("slodefChar", Character.toString(slodefChar));

	buffer.add("mectt", masterEventCorrections == null ? 0. : masterEventCorrections[0], 3);
	buffer.add("mecaz", masterEventCorrections == null ? 0. : masterEventCorrections[1], 3);
	buffer.add("mecsh", masterEventCorrections == null ? 0. : masterEventCorrections[2], 3);

	buffer.add("ttWeight", getTtWeight(), 3 );
	buffer.add("azWeight", getAzWeight(), 3);
	buffer.add("shWeight", getShWeight(), 3);

	buffer.add("modelId", modelId);
	buffer.add("algorithmId", algorithmId);

	buffer.add("predictionRayType", (predictionRayType == null ? "null" : predictionRayType.toString()));

	buffer.add("ttErrorMessage", componentTT.getErrorMessage());
	buffer.add("azErrorMessage", componentAZ.getErrorMessage());
	buffer.add("shErrorMessage", componentSH.getErrorMessage());

	if (predictions == null)
	    buffer.add("nPredictions", 0);
	else {
	    buffer.add("nPredictions", 1);
	    buffer.add(Prediction.getBuff(predictions, "%1.6f"));
	}

	if (getReceiver() == null)
	    buffer.add("nReceivers", 0);
	else {
	    buffer.add("nReceivers", 1);
	    buffer.add(getReceiver().getBuff());
	}

	return buffer;
    }

    static public Buff getBuff(Scanner input) {
	Buff buf = new Buff(input);
	for (int i=0; i<buf.getInt("nPredictions"); ++i)
	    buf.add(new Buff(input)); // should only be zero or one of these
	for (int i=0; i<buf.getInt("nReceivers"); ++i)
	    buf.add(Receiver.getBuff(input));

	return buf;	
    }

    public String getPredictionErrorMessage() {
	return predictionErrorMessage;
    }

    public ScreenWriterOutput getErrorLog() {
	return source.getErrorLog();
    }


}
