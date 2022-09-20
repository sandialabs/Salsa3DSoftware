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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import gov.sandia.gmp.baseobjects.Observation;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;

/**
 * <p>
 * Arrival
 * </p>
 * 
 * <p>
 * An Arrival represents a set of observations associated with a single wiggle
 * on a seismogram. It has attributes for arrival time, observed azimuth,
 * slowness, uncertainties, etc. It also has a reference to the event with which
 * it is associated (just one). It also has a reference to a Predictor
 * object that knows how to generate predictions of the observations it manages.
 * It also has a reference to a Prediction object that stores
 * predictions of the observations that it manages, but it doesn't do anything
 * with those predictions.
 * </p>
 * 
 * <p>
 * ObservationComponent objects are 'views' into an Arrival object. 
 * ObservationTT objects
 * view the travel time information, ObservationAZ objects view the azimuth
 * information, and ObservationSH objects view the slowness information. The
 * Observation objects know how to do things like compute residuals, etc.
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
public class Arrival extends Observation
{
	private char timedefChar, azdefChar, slodefChar;

	final protected String phaseName;

	private HashMap<GeoAttributes, ObservationComponent> observations = 
			new HashMap<GeoAttributes, ObservationComponent>();
	
	private EventParameters eventParameters;

	/**
	 * tt,az,sh corrections for master event relocation.
	 * Units are tt (sec), az (radians), sh (sec/radian)
	 */
	final protected double[] masterEventCorrections;
	
	public static boolean TESTING = false;
	
	public Arrival(Receiver receiver, Source source, EventParameters params, LocOOObservation obs,
			Predictor predictor, double[] masterEventCorrections) throws Exception
	{
		super(obs.observationid, receiver, source, obs.phase, 
				obs.time, obs.deltim, obs.timedef=='d' || obs.timedef=='D',
				obs.azimuth, obs.delaz, obs.azdef=='d' || obs.azdef=='D',
				obs.slow, obs.delslo, obs.slodef=='d' || obs.slodef=='D',
				false, predictor);

		this.timedefChar = obs.timedef;
		this.azdefChar = obs.azdef;
		this.slodefChar = obs.slodef;
		this.eventParameters = params;
		this.phaseName = obs.phaseName;

		this.masterEventCorrections = masterEventCorrections;

		if (masterEventCorrections[0] != Assoc.TIMERES_NA) setTimecorr(masterEventCorrections[0]);
		if (masterEventCorrections[1] != Assoc.AZRES_NA) setAzcorr(masterEventCorrections[1]);
		if (masterEventCorrections[2] != Assoc.SLORES_NA) setSlocorr(masterEventCorrections[2]);
	}

	protected void addObservation(ObservationComponent obs)
	{
		observations.put(obs.getObsType(), obs);
	}

	public HashMap<GeoAttributes, ObservationComponent> getObservations()
	{
		return observations;
	}

	/**
	 * @return the event
	 */
	public EventParameters getEventParameters()
	{
		return eventParameters;
	}

	/**
	 * Clear the set of requested attributes associated with this arrival
	 * and reset it with attributes necessary to compute predictions 
	 */
	protected void setRequestedAttributes(boolean needDerivatives)
	{
		requestedAttributes.clear();
		for (ObservationComponent obs : observations.values())
			if (obs.isDefining())
				obs.addRequiredAttributes(requestedAttributes, needDerivatives);		
	}

	@Override
	public Arrival setPrediction(Prediction prediction)
	{
	    super.setPrediction(prediction);

	    for (ObservationComponent obs : observations.values())
		obs.setPrediction(prediction);

	    return this;
	}

	/**
	 * @return the timedefChar
	 */
	public char getTimedefChar()
	{
		return timedefChar;
	}

	/**
	 * @return the azdefChar
	 */
	public char getAzdefChar()
	{
		return azdefChar;
	}

	/**
	 * @return the slodefChar
	 */
	public char getSlodefChar()
	{
		return slodefChar;
	}

	public boolean isTimedefOriginal()
	{
		return timedefChar == 'd' || timedefChar == 'D';
	}

	public boolean isSlodefOriginal()
	{
		return slodefChar == 'd' || slodefChar == 'D';
	}

	public boolean isAzdefOriginal()
	{
		return azdefChar == 'd' || azdefChar == 'D';
	}

	/**
	 * Retrieve data needed to make an Assoc database row.
	 * 
	 * @return assoc row
	 */
	@Override
	public AssocExtended getAssocRow()
	{
		AssocExtended assocRow = super.getAssocRow();

		if (isTimedef() == isTimedefOriginal())
			assocRow.setTimedef(String.valueOf(timedefChar));

		if (isSlodef() == isSlodefOriginal())
			assocRow.setSlodef(String.valueOf(slodefChar));

		if (isAzdef() == isAzdefOriginal())
			assocRow.setAzdef(String.valueOf(azdefChar));

		if (observations.get(GeoAttributes.TRAVEL_TIME) != null)
			assocRow.setWgt(observations.get(GeoAttributes.TRAVEL_TIME).getWeight());
		
		if (TESTING) {
		    Map<String, Double> pvals = new HashMap<>(prediction.getValues().size());
		    for (Entry<GeoAttributes, Double> entry : prediction.getValues().entrySet())
			pvals.put(entry.getKey().toString(), entry.getValue());
		    assocRow.attachment = pvals;
		}

		return assocRow;
	}

	@Override
	public String getInPolygon()
	{
		return "  ";
	}

}
