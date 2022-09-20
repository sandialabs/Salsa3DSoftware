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
package gov.sandia.gmp.infrasoundpredictor;

import java.io.File;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.UncertaintyInterface;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class InfrasoundPredictor extends Predictor implements UncertaintyInterface
{
	private InfrasoundModel model;

	/*
	From Kyle Jones (krjones@sandia.gov)
	Phase Name				Phase Description
	N							Noise
	I							Direct Infrasound wave
	Iw							Tropospheric ducted wave with a turing height of <15-20 km
	Is							Stratospheric ducted wave with a turning height of < 60 km
	It							Thermospheric ducted wave with a turning height of < 120 km					
	 */
	public static final EnumSet<SeismicPhase> supportedPhases = EnumSet.of(
			SeismicPhase.I, SeismicPhase.LW, SeismicPhase.Iw, SeismicPhase.Is, SeismicPhase.It);

	// TODO: update this list
	public static final EnumSet<GeoAttributes> supportedAttributes = EnumSet
			.of(GeoAttributes.TRAVEL_TIME, GeoAttributes.TT_BASEMODEL,
					GeoAttributes.TT_MODEL_UNCERTAINTY,
					GeoAttributes.TT_PATH_CORRECTION,
					GeoAttributes.TT_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.TT_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.TT_ELLIPTICITY_CORRECTION,
					GeoAttributes.TT_ELEVATION_CORRECTION,
					GeoAttributes.TT_ELEVATION_CORRECTION_SOURCE,
					GeoAttributes.DTT_DLAT, GeoAttributes.DTT_DLON,
					GeoAttributes.DTT_DR, GeoAttributes.DTT_DTIME,
					GeoAttributes.AZIMUTH, GeoAttributes.AZIMUTH_DEGREES,
					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.DAZ_DLAT, GeoAttributes.DAZ_DLON,
					GeoAttributes.DAZ_DR, GeoAttributes.DAZ_DTIME,
					GeoAttributes.SLOWNESS, GeoAttributes.SLOWNESS_DEGREES,
					GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY,
					GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.DSH_DLAT, GeoAttributes.DSH_DLON,
					GeoAttributes.DSH_DR, GeoAttributes.DSH_DTIME,
					GeoAttributes.BACKAZIMUTH, GeoAttributes.OUT_OF_PLANE,
					GeoAttributes.CALCULATION_TIME, GeoAttributes.DISTANCE,
					GeoAttributes.DISTANCE_DEGREES);

	public InfrasoundPredictor(PropertiesPlus properties)
			throws Exception
	{
		this(properties, null);
	}

	public InfrasoundPredictor(PropertiesPlus properties,
			InfrasoundModel model) throws Exception
	{
		super(properties);
		this.properties = properties;
		if (model == null)
			this.model = new InfrasoundModel(properties);
		else
			this.model = model;
		uncertaintyInterface = this;
	}

	@Override
	public boolean isSupported(Receiver receiver, SeismicPhase phase,
			GeoAttributes attribute, double epochTime)
	{
		return supportedPhases.contains(phase)
				&& supportedAttributes.contains(attribute);
	}

	@Override
	public EnumSet<GeoAttributes> getSupportedAttributes()
	{
		return supportedAttributes;
	}

	@Override
	public Prediction getPrediction(PredictionRequest request)
			throws Exception
	{
		Prediction result = new Prediction(request);

		travelTime = slowness = dttdr = dshdx = dshdr = Globals.NA_VALUE;

		if (!request.isDefining())
			return new Prediction(request, this,
					"PredictionRequest was non-defining");

		double[] event = request.getSource().getUnitVector();
		double[] station = request.getReceiver().getUnitVector();
		double azimuth = VectorGeo.azimuth(station, event, Globals.NA_VALUE);

		travelTime = model.getTravelTime(event, station);
		slowness = model.getSlowness(station, event, travelTime);

		result.setRayType(RayType.REFRACTION);

		setGeoAttributes(result, travelTime, azimuth, slowness, dttdr, dshdx, dshdr);

		return result;

	}

	/**
	 * Retrieve a new, invalid TaupResult object whose error message is set to
	 * the supplied string.
	 */
	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, String msg) {
		return new Prediction(predictionRequest, this, msg);
	}

	/**
	 * Retrieve a new, invalid TaupResult object whose error message is set to
	 * the error message and stack trace of the supplied Exception.
	 */
	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, Exception e) {
		return new Prediction(predictionRequest, this, e);
	}

	public String getModelDescription() 
	{
		return model.getDescription();
	}

	@Override
	public String getModelName()
	{
		return "InfrasoundPredictionModel";
	}

	@Override
	public String getPredictorName()
	{
		return getPredictorType().toString();
	}

	@Override
	public PredictorType getPredictorType()
	{
		return PredictorType.INFRASOUND;
	}

	public String getUncertaintyRootDirectory() throws Exception
	{
		throw new UnsupportedOperationException();
	}

	static public String getVersion() 	{ 
		return Utils.getVersion("infrasound-predictor");
	}

	@Override
	public String getPredictorVersion() {
		return getVersion();
	}

	public boolean isUncertaintySupported(Receiver receiver,
			SeismicPhase phase, GeoAttributes attribute)
	{
		return supportedPhases.contains(phase)
				&& supportedAttributes.contains(attribute);
	}

	@Override
	public double getUncertainty(PredictionRequest predictionRequest, GeoAttributes attribute) throws Exception
	{
		switch (attribute)
		{
		case TT_MODEL_UNCERTAINTY:
			return 10.;
		case AZIMUTH_MODEL_UNCERTAINTY:
			return Math.toRadians(10.);
		case AZIMUTH_MODEL_UNCERTAINTY_DEGREES:
			return 10.;
		case SLOWNESS_MODEL_UNCERTAINTY:
			return Math.toDegrees(10.);
		case SLOWNESS_MODEL_UNCERTAINTY_DEGREES:
			return 10.;
		default:
			return Globals.NA_VALUE;
		}
	}

	/**
	 * Obstype must be one of TT, AZ, SH
	 */
	@Override
	public String getUncertaintyModelFile(PredictionRequest request, String obsType) throws Exception {
		return "unspecified";
	}

	public double getUncertainty(int lookupIndex, Source source)
			throws Exception
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public EnumSet<SeismicPhase> getSupportedPhases()
	{
		return supportedPhases;
	}

	private double travelTime;
	private double slowness;
	private double dttdr;
	private double dshdx;
	private double dshdr;


	protected double getTravelTime() { return travelTime; }


	protected double getSlowness() { return slowness; }


	protected double getDttDr() { return dttdr; }



	protected double getDshDx() { return dshdx; }


	protected double getDshDr() { return dshdr; }

	@Override
	public File getModelFile() {
		return null;
	}

	@Override
	public String getUncertaintyVersion() {
		return Utils.getVersion("base-objects");
	}

	@Override
	public Object getEarthModel() {
		return null;
	}

	/**
	 * Returns the type of the UncertaintyInterface object: UncertaintyNAValue,
	 * UncertaintyDistanceDependent, etc.
	 */
	@Override
	public String getUncertaintyType() {
		return "InfrasoundPredictor";
	}

	/**
	 * When uncertainty is requested and libcorr3d uncertainty is available
	 * return the libcorr uncertainty, otherwise return internally computed uncertainty.
	 */
	@Override
	public boolean isHierarchicalTT() {
		return false;
	}

	/**
	 * When uncertainty is requested and libcorr3d uncertainty is available
	 * return the libcorr uncertainty, otherwise return internally computed uncertainty.
	 */
	@Override
	public boolean isHierarchicalAZ() {
		return false;
	}

	/**
	 * When uncertainty is requested and libcorr3d uncertainty is available
	 * return the libcorr uncertainty, otherwise return internally computed uncertainty.
	 */
	@Override
	public boolean isHierarchicalSH() {
		return false;
	}

}
