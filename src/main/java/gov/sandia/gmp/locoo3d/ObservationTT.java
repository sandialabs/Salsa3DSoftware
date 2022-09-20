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

import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.util.globals.Globals;

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
public class ObservationTT extends ObservationComponent
{
    private static final long serialVersionUID = 1L;
	/**
	 * True if there is a valid predictor for this observation
	 * and that predictor is capable of computing predicted value,
	 * model uncertainty, and derivatives for this observation
	 * component.  Set in the constructors.
	 */
	final protected boolean supported;
	
	/**
	 * ObservationTT
	 * 
	 * @param arrival
	 *            Arrival
	 * @throws Exception 
	 */
	public ObservationTT(Arrival arrival, Predictor p) throws Exception
	{
		super(arrival);
		
		masterEventCorrection = arrival.masterEventCorrections[0];

//		supported = arrival.getPredictor() != null
//		   && arrival.getPredictor().getSupportedGeoAttributes().contains(getObsType())
//		   && (!useModelUncertainty()
//				   || arrival.getPredictor().getSupportedGeoAttributes().contains(getModelUncertaintyType()))
//		   && arrival.getPredictor().getSupportedGeoAttributes().contains(DERIVS[0])
//		   && arrival.getPredictor().getSupportedGeoAttributes().contains(DERIVS[1])
//		   && arrival.getPredictor().getSupportedGeoAttributes().contains(DERIVS[2])
//		   ;		   

		//Predictor p = arrival.getPredictor();
		supported = p != null 
				&& p.isSupported(getReceiver(), getPhase(), getObsType(), Globals.NA_VALUE)
				&& (!useModelUncertainty()
				   || p.isSupported(getReceiver(), getPhase(), getModelUncertaintyType(), Globals.NA_VALUE))
					&& p.isSupported(getReceiver(), getPhase(), DERIVS[0], Globals.NA_VALUE)
					&& p.isSupported(getReceiver(), getPhase(), DERIVS[1], Globals.NA_VALUE)
					&& p.isSupported(getReceiver(), getPhase(), DERIVS[2], Globals.NA_VALUE);
				
}

	/**
	 * Returns true if the predictor is capable of computing a predicted value
	 * model uncertainty and derivatives.  The boolean value that backs this
	 * call was set in the ObservationComponent constructor and is final.
	 * 
	 * @return boolean
	 * @throws GeoVectorException
	 */
	@Override
	public boolean isSupported()
	{
		return supported;
	}

	@Override
	public GeoAttributes getObsType()
	{
		return GeoAttributes.TRAVEL_TIME;
	}

	@Override
	public GeoAttributes getObsUncertaintyType()
	{
		return GeoAttributes.TT_OBSERVED_UNCERTAINTY;
	}

	@Override
	public GeoAttributes getModelUncertaintyType()
	{
		return GeoAttributes.TT_MODEL_UNCERTAINTY;
	}

	@Override
	public boolean useModelUncertainty()
	{
		return arrival.getEventParameters().useTTModelUncertainty();
	}

	/**
	 * Returns observed travel time (arrival time - origin time).
	 */
	@Override
	public double getObserved()
	{
		return arrival.getTravelTime();
	}

	@Override
	public double getObsUncertainty()
	{
		return arrival.getDeltim();
	}

	@Override
	public boolean isDefining()
	{
		return arrival.isTimedef();
	}

	@Override
	public void setDefining(boolean defining)
	{
		arrival.setTimedef(defining);
	}

	@Override
	public boolean isDefiningOriginal()
	{
		return arrival.isTimedefOriginal();
	}

	private static final GeoAttributes[] DERIVS = new GeoAttributes[] {
		GeoAttributes.DTT_DLAT, GeoAttributes.DTT_DLON,
		GeoAttributes.DTT_DR, GeoAttributes.DTT_DTIME };
	
	@Override
	protected GeoAttributes[] getDerivAttributes()
	{
		return DERIVS;
	}

	@Override
	protected GeoAttributes DObs_DLAT()
	{
		return GeoAttributes.DTT_DLAT;
	}

	@Override
	protected GeoAttributes DObs_DLON()
	{
		return GeoAttributes.DTT_DLON;
	}

	@Override
	protected GeoAttributes DObs_DR()
	{
		return GeoAttributes.DTT_DR;
	}

	@Override
	protected GeoAttributes DObs_DTIME()
	{
		return GeoAttributes.DTT_DTIME;
	}

	@Override
	protected double toOutput(double value)
	{
		return value;
	}

	@Override
	public String getObsTypeShort()
	{
		return "TT";
	}

	@Override
	public double getElevationCorrection()
	{
		if (isPredictionValid())
			return arrival.getPrediction().getAttribute(
					GeoAttributes.TT_ELEVATION_CORRECTION);
		return Globals.NA_VALUE;
	}

	@Override
	public double getElevationCorrectionAtSource()
	{
		if (isPredictionValid())
			return arrival.getPrediction().getAttribute(
					GeoAttributes.TT_ELEVATION_CORRECTION_SOURCE);
		return Globals.NA_VALUE;
	}

	@Override
	public double getEllipticityCorrection()
	{
		if (isPredictionValid())
			return arrival.getPrediction().getAttribute(
					GeoAttributes.TT_ELLIPTICITY_CORRECTION);
		return Globals.NA_VALUE;
	}

	@Override
	public double getPathCorrection()
	{
		if (isPredictionValid())
			return arrival.getPrediction().getAttribute(
					GeoAttributes.TT_PATH_CORRECTION);
		return Globals.NA_VALUE;
	}

	@Override
	public double getSiteCorrection()
	{
		if (isPredictionValid())
			return arrival.getPrediction().getAttribute(
					GeoAttributes.TT_SITE_CORRECTION);
		return Globals.NA_VALUE;
	}

	@Override
	public double getSourceCorrection()
	{
		if (isPredictionValid())
			return arrival.getPrediction().getAttribute(
					GeoAttributes.TT_SOURCE_CORRECTION);
		return Globals.NA_VALUE;
	}

	@Override
	protected void addRequiredAttributes(EnumSet<GeoAttributes> attributes, boolean needDerivatives)
	{
		super.addRequiredAttributes(attributes, needDerivatives);
	}

	@Override
	public char getDefiningChar()
	{
		return arrival.getTimedefChar();
	}

	@Override
	public boolean usePathCorr()
	{
		return arrival.getEventParameters().useTTPathCorrections();
	}

	@Override
	public GeoAttributes getPathCorrType()
	{
		return GeoAttributes.TT_PATH_CORRECTION;
	}

	@Override
	public GeoAttributes getBaseModelType()
	{
		return GeoAttributes.TT_BASEMODEL;
	}
}
