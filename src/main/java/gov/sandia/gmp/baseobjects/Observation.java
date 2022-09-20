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
package gov.sandia.gmp.baseobjects;

import static java.lang.Math.PI;
import java.io.File;
import java.util.EnumSet;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
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
 * object (origin) and Receiver object (site) with which it is associated. It
 * also has a reference to a PredictorInterface object that knows how to
 * generate predictions of the observations it manages. It also has a reference
 * to a Prediction object that stores predictions of the observations
 * that it manages.
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
public class Observation extends PredictionRequest
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6169774768813956516L;

	private double arrivaltime;
	private double deltim;
	private boolean timedef;
	private double timeres;
	private double timecorr;

	private double azimuth;
	private double delaz;
	private boolean azdef;
	private double azres;
	private double azcorr;

	private double slow;
	private double delslo;
	private boolean slodef;
	private double slores;
	private double slocorr;
	
	long modelId, algorithmId;

	private static final double TWO_PI = 2*PI;

	protected Prediction prediction;
	
	private boolean predictionUpToDate;
	
	/**
	 * Constructor.
	 * 
	 * @param observationId
	 * @param receiver
	 * @param source
	 * @param phase
	 * @param time
	 * @param deltim
	 * @param timedef
	 * @param azimuth       in degrees or radians
	 * @param delaz         in degrees or radians
	 * @param azdef
	 * @param slow          in sec/degree or sec/radian
	 * @param delslo        in sec/degree or sec/radian
	 * @param slodef
	 * @param inDegrees     if true input values of azimuth are assumed to be
	 *                      degrees and slowness are assumed sec/degree, otherwise
	 *                      radians and sec/radian
	 * @throws Exception 
	 */
	public Observation(long observationId, Receiver receiver, Source source, SeismicPhase phase,
			double time, double deltim, boolean timedef, double azimuth, double delaz, boolean azdef, double slow,
			double delslo, boolean slodef, boolean inDegrees, Predictor predictor) throws Exception {
		super(observationId, receiver, source, phase, EnumSet.noneOf(GeoAttributes.class), true);

		timedef = timedef && predictor != null
				&& predictor.isSupported(receiver, phase, GeoAttributes.TRAVEL_TIME, source.getOriginTime());

		azdef = azdef && predictor != null
				&& predictor.isSupported(receiver, phase, GeoAttributes.AZIMUTH, source.getOriginTime());

		slodef = slodef && predictor != null
				&& predictor.isSupported(receiver, phase, GeoAttributes.SLOWNESS, source.getOriginTime());

		// ensure that na_values are all Globals.NA_VALUE
		// and convert degrees to radians.
		// Set defining false if observed value or uncertainty are invalid
		if (time == Arrival.TIME_NA)
			time = Globals.NA_VALUE;

		if (deltim == Arrival.DELTIM_NA)
			deltim = Globals.NA_VALUE;

		timedef = timedef && this.arrivaltime != Globals.NA_VALUE && deltim > 0.;

		if (azimuth == Arrival.AZIMUTH_NA)
			azimuth = Globals.NA_VALUE;
		if (azimuth != Globals.NA_VALUE && inDegrees)
			azimuth = Math.toRadians(azimuth);

		if (delaz == Arrival.DELAZ_NA)
			delaz = Globals.NA_VALUE;
		if (delaz != Globals.NA_VALUE && inDegrees)
			delaz = Math.toRadians(delaz);

		azdef = azdef && azimuth != Globals.NA_VALUE && delaz > 0.;

		if (slow == Arrival.SLOW_NA)
			slow = Globals.NA_VALUE;
		if (slow != Globals.NA_VALUE && inDegrees)
			slow = Math.toDegrees(slow);

		if (delslo == Arrival.DELSLO_NA)
			delslo = Globals.NA_VALUE;
		if (delslo != Globals.NA_VALUE && inDegrees)
			delslo = Math.toDegrees(delslo);

		slodef = slodef && slow != Globals.NA_VALUE && delslo > 0.;

		setTime(time);
		setDeltim(deltim);
		setTimedef(timedef);

		setAzimuth(azimuth);
		setDelaz(delaz);
		setAzdef(azdef);

		setSlow(slow);
		setDelslo(delslo);
		setSlodef(slodef);
		
		this.modelId = predictor.getModelId();
		this.algorithmId = predictor.getAlgorithmId();

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
			double deltim, boolean defining, EnumSet<GeoAttributes> requestedAttributes) throws Exception 
	{
		this(observationId, receiver, source, phase, arrivalTime, deltim, defining, 
				Globals.NA_VALUE, Globals.NA_VALUE, false,
				Globals.NA_VALUE, Globals.NA_VALUE, false,  false, null);
		super.setRequestedAttributes(requestedAttributes);
	}

	public Observation(gov.sandia.gnem.dbtabledefs.gmp.Observation obs, Receiver receiver, Source source) throws Exception {
		this(obs.getObservationid(), receiver, source, SeismicPhase.valueOf(obs.getIphase()),
				obs.getArrivaltime(), obs.getTimeuncertainty(), false, obs.getAzimuth(), obs.getAzuncertainty(),
				false, obs.getSlowness(), obs.getSlowuncertainty(), false, true, null);
	}

	/**
	 * Returns timedef || azdef || slodef;
	 */
	@Override
	public boolean isDefining() {
		return isTimedef() || isAzdef() || isSlodef();
	}

	/**
	 * Set the Prediction object for this Observation. This method will extract
	 * information to update internal values of timeres, azres and slowres. Consider
	 * using updatePrediction() instead.
	 * 
	 * @param prediction
	 */
	public Observation setPrediction(Prediction prediction) {
	    this.prediction = prediction;
	    updateResiduals();
	    return this;
	}

	public Prediction getPrediction() { return prediction; }
	
	public void setPredictionUpToDate(boolean predictionUpToDate) {
	    this.predictionUpToDate = predictionUpToDate; 
	}

	public boolean isPredictionUpToDate() {
	    return predictionUpToDate;
	}

	/**
	 * Ensure that timeres, azres and slores are up to date with current Prediction.
	 * It may be necessary to call this, even when Prediction has not changed, if
	 * origin time has changed, since that will change timeres.
	 */
	public void updateResiduals() {
		if (prediction == null) {
			setTimeres(Globals.NA_VALUE);
			setAzres(Globals.NA_VALUE);
			setSlores(Globals.NA_VALUE);
		} else {
			setTimeres(Globals.NA_VALUE);
			double predictedValue = prediction.getAttribute(GeoAttributes.TRAVEL_TIME);
			if (predictedValue != Globals.NA_VALUE) {
				double observedValue = getTravelTime();
				if (observedValue != Globals.NA_VALUE)
					setTimeres(observedValue - predictedValue - timecorr);
			}

			predictedValue = prediction.getAttribute(GeoAttributes.SLOWNESS);
			if (predictedValue == Globals.NA_VALUE || getSlow() == Globals.NA_VALUE)
				setSlores(Globals.NA_VALUE);
			else
				setSlores(getSlow() - predictedValue - slocorr);

			predictedValue = prediction.getAttribute(GeoAttributes.AZIMUTH);
			if (predictedValue == Globals.NA_VALUE || getAzimuth() == Globals.NA_VALUE)
				setAzres(Globals.NA_VALUE);
			else {
				double azres = getAzimuth() - predictedValue - azcorr;
				if (azres > Math.PI)
					azres -= TWO_PI;
				if (azres < -Math.PI)
					azres += TWO_PI;
				setAzres(azres);
			}
		}
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
			String vmodel = prediction == null ? "-" : prediction.getModelName();
			int index = vmodel.lastIndexOf(File.separatorChar);
			if (index > 0)
			{
				File f = new File(vmodel);
				if (f.getName().equals("prediction_model.geotess"))
					f = f.getParentFile();
				vmodel = f.getName();
			}
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
				algorithmId, observationId, prediction.getPhase().toString(),
				prediction.getRayType().toString(), prediction.getAttribute(GeoAttributes.ACTIVE_FRACTION),
				prediction.getAttribute(GeoAttributes.TRAVEL_TIME), getTimeres(),
				prediction.getAttribute(GeoAttributes.AZIMUTH_DEGREES),
				prediction.getAttribute(GeoAttributes.SLOWNESS_DEGREES),
				prediction.getAttribute(GeoAttributes.BACKAZIMUTH_DEGREES),
				prediction.getAttribute(GeoAttributes.TURNING_DEPTH),
				prediction.getAttribute(GeoAttributes.OUT_OF_PLANE),
				prediction.getAttribute(GeoAttributes.CALCULATION_TIME), getInPolygon(), GMPGlobals.getAuth());
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
		values[7] = prediction.getRayType().toString(); // raytype
		values[8] = Double.valueOf(prediction.getAttribute(GeoAttributes.ACTIVE_FRACTION)); // activefraction
		values[9] = Double.valueOf(prediction.getAttribute(GeoAttributes.TRAVEL_TIME));
		values[10] = degrees(prediction.getAttribute(GeoAttributes.AZIMUTH), Globals.NA_VALUE); // azimuth
		values[11] = radians(prediction.getAttribute(GeoAttributes.SLOWNESS), Globals.NA_VALUE); // slowness
		values[12] = degrees(prediction.getAttribute(GeoAttributes.BACKAZIMUTH), Globals.NA_VALUE); // backazimuth
		values[13] = Double.valueOf(prediction.getAttribute(GeoAttributes.TURNING_DEPTH)); // turndepth
		values[14] = Double.valueOf(prediction.getAttribute(GeoAttributes.OUT_OF_PLANE)); // maxoutplane
		values[15] = prediction.getAttribute(GeoAttributes.CALCULATION_TIME); // calctime
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
		return Math.toDegrees(x);
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
		return Math.toRadians(x);
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
		this.azimuth = azimuth < 0. && azimuth >= -TWO_PI ? (azimuth + TWO_PI) % TWO_PI : azimuth;
	}

	/**
	 * get delaz in radians
	 * @return
	 */
	public double getDelaz() {
		return delaz;
	}

	/**
	 * set delaz in radians
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
	 * get delslo in sec/radian
	 * @return
	 */
	public double getDelslo() {
		return delslo;
	}

	/**
	 * set delslo in sec/radian
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
		return azres;
	}

	/**
	 * set azimuth residual in radians
	 * @param azres
	 */
	public void setAzres(double azres) {
		this.azres = azres;
	}

	/**
	 * get slowness residual in sec/radian
	 * @return
	 */
	public double getSlores() {
		return slores;
	}

	/**
	 * set slowness residual in sec/radian
	 * @param slores
	 */
	public void setSlores(double slores) {
		this.slores = slores;
	}

	public double getTimeres() {
		return timeres;
	}

	public void setTimeres(double timeres) {
		this.timeres = timeres;
	}

	public double getTimecorr() {
		return timecorr;
	}

	public void setTimecorr(double timecorr) {
		this.timecorr = timecorr;
	}

	/**
	 * get azimuth correction in radians
	 * @return
	 */
	public double getAzcorr() {
		return azcorr;
	}

	/**
	 * set azimuth correction in radians
	 * @param azcorr
	 */
	public void setAzcorr(double azcorr) {
		this.azcorr = azcorr;
	}

	/**
	 * get slowness correction in sec/radian
	 * @return
	 */
	public double getSlocorr() {
		return slocorr;
	}

	/**
	 * set slowness correction in sec/radian
	 * @param slocorr
	 */
	public void setSlocorr(double slocorr) {
		this.slocorr = slocorr;
	}

}
