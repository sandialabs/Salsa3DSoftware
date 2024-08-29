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
package gov.sandia.gmp.hydroradial2d;

import java.io.File;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map.Entry;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.baseobjects.radial2dmodel.Radial2DLibrary;
import gov.sandia.gmp.baseobjects.radial2dmodel.Radial2DModel;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintyInterface;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintyType;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class HydroRadial2D extends Predictor implements UncertaintyInterface {

	private String modelName;

	private File modelDirectory;
	
	private String radial2dModelClassName;

	/**
	 * map from day-of-year -> stationName -> Radial2DModel
	 */
	private Radial2DLibrary  library;

	private UncertaintyType uncertaintyType;

	/**
	 * This is the set of GeoAttributes supported by HydroRadial2D
	 */
	public static final EnumSet<GeoAttributes> supportedAttributes = EnumSet.of(GeoAttributes.TRAVEL_TIME,
			GeoAttributes.TT_BASEMODEL, GeoAttributes.TT_MODEL_UNCERTAINTY,
			GeoAttributes.TT_PATH_CORRECTION, GeoAttributes.TT_PATH_CORR_DERIV_HORIZONTAL,
			GeoAttributes.TT_PATH_CORR_DERIV_RADIAL, 
			GeoAttributes.DTT_DLAT, GeoAttributes.DTT_DLON, GeoAttributes.DTT_DTIME,
			GeoAttributes.AZIMUTH, GeoAttributes.AZIMUTH_DEGREES, GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
			GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES,
			GeoAttributes.AZIMUTH_PATH_CORR_DERIV_HORIZONTAL,
			GeoAttributes.AZIMUTH_PATH_CORR_DERIV_RADIAL, GeoAttributes.DAZ_DLAT, GeoAttributes.DAZ_DLON,
			GeoAttributes.DAZ_DTIME, GeoAttributes.SLOWNESS,
			GeoAttributes.SLOWNESS_DEGREES, GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY,
			GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES,
			GeoAttributes.SLOWNESS_PATH_CORR_DERIV_HORIZONTAL,
			GeoAttributes.SLOWNESS_PATH_CORR_DERIV_RADIAL, 
			GeoAttributes.DSH_DTIME, GeoAttributes.BACKAZIMUTH,
			GeoAttributes.OUT_OF_PLANE, GeoAttributes.CALCULATION_TIME, GeoAttributes.DISTANCE,
			GeoAttributes.DISTANCE_DEGREES);

	/**
	 * Phases that this Predictor can support. All phases.
	 */
	protected final EnumSet<SeismicPhase> supportedPhases = EnumSet.of(SeismicPhase.T, SeismicPhase.H);

	public HydroRadial2D(PropertiesPlus properties) throws Exception {
		this(properties, null);
	}

	public HydroRadial2D(PropertiesPlus properties, ScreenWriterOutput logger) throws Exception {
		super(properties, logger);

		predictionsPerTask = properties.getInt(getPredictorName()+"PredictionsPerTask", 500);

		modelDirectory = properties.getFile(getPredictorName()+"ModelDirectory");

		if (modelDirectory == null)
			throw new Exception("Must specify property "+(getPredictorName()+"ModelDirectory")+" in properties file.");

		if (!modelDirectory.exists())
			throw new Exception(getPredictorName()+"ModelDirectory specified in properties file does not exist. "+modelDirectory.getPath());

		library = Radial2DLibrary.getLibrary(modelDirectory);
		
		// this will be either Radial2DModelLegacy or Radial2DModelImproved
		radial2dModelClassName = library.models .values().iterator().next().values().iterator().next().getClass().getSimpleName();

		modelName = modelDirectory.getName();

		uncertaintyType = UncertaintyType.PATH_DEPENDENT;
		super.getUncertaintyInterface().put(GeoAttributes.TRAVEL_TIME, this);

	}

	@Override
	public Prediction getPrediction(PredictionRequest request) throws Exception {
		
		if (!request.isDefining())
			return new Prediction(request, this,
					"PredictionRequest submitted to HydroRadial2D was non-defining");

		Radial2DModel model = library.getModel(request);
		if (model == null)
			return new Prediction(request, this, String.format("Station %s is not supported by Predictor %s model %s", 
					request.getReceiver().getSta(), getPredictorName(), getModelName()));
		
		EnumMap<GeoAttributes, Double> modelValues = model.interpolate(request.getSource().getUnitVector());

		Prediction prediction;
		if (modelValues == null) {
			prediction = new Prediction(request, this, String.format("Unsupported ray path%n%s", request.getString()));
			prediction.setRayType(RayType.ERROR);
		}
		else if (!modelValues.containsKey(GeoAttributes.TRAVEL_TIME)) {
			prediction = new Prediction(request, this, String.format("Ray path is blocked.h%n%s", request.getString()));
			prediction.setRayType(RayType.INVALID);
		}
		else {
			prediction = new Prediction(request, getPredictorType());
			
			for (Entry<GeoAttributes, Double> entry : modelValues.entrySet())
				prediction.setAttribute(entry.getKey(), entry.getValue());

			prediction.setRayType(RayType.HYDROACOUSTIC_WAVE);

			prediction.setAttribute(GeoAttributes.TT_BASEMODEL, prediction.getAttribute(GeoAttributes.TRAVEL_TIME));

			// tt, az, slowness, dtt_dr, dslo_dx, dslo_dr (radians, not degrees)
			setGeoAttributes(prediction, 
					prediction.getAttribute(GeoAttributes.TRAVEL_TIME), 
					prediction.getAttribute(GeoAttributes.AZIMUTH), 
					prediction.getAttribute(GeoAttributes.SLOWNESS), 
					Globals.NA_VALUE, // deriv tt wrt radius
					Globals.NA_VALUE,  // deriv slow wrt x
					Globals.NA_VALUE); // deriv slow wrt radius
			
		}
		return prediction;
	}

	/**
	 * Retrieve a new, invalid Prediction object whose error message is set to the supplied string.
	 */
	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, String msg) {
		return new Prediction(predictionRequest, this, msg);
	}

	/**
	 * Retrieve a new, invalid Prediction object whose error message is set to the error message and
	 * stack trace of the supplied Exception.
	 */
	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, Exception e) {
		return new Prediction(predictionRequest, this, e);
	}

	@Override
	public String getModelDescription() throws Exception {
		return modelName;
	}

	@Override
	public String getModelName() {
		return modelName;
	}

	@Override
	public String getPredictorName() {
		return PredictorType.HYDRO_RADIAL2D.name().toLowerCase();
	}

	@Override
	public PredictorType getPredictorType() {
		return PredictorType.HYDRO_RADIAL2D;
	}

	@Override
	public File getModelFile() {
		return modelDirectory;
	}

	@Override
	public boolean isSupported(Receiver receiver, SeismicPhase phase, GeoAttributes attribute, double originTime) {
		return phase == SeismicPhase.H && library.getModel(GMTFormat.getJDate(originTime), receiver.getSta()) != null;
	}

	@Override
	public String getPredictorVersion() {
		return Utils.getVersion("hydro-radial2d");
	}

	@Override
	public EnumSet<GeoAttributes> getSupportedAttributes() {
		return supportedAttributes;
	}

	@Override
	public EnumSet<SeismicPhase> getSupportedPhases() {
		return supportedPhases;
	}

	@Override
	public Object getEarthModel() {
		return library;
	}

	/**
	 * Retrieve a map from day-of-year -> station name -> Radial2DModel
	 * @return
	 */
	public Radial2DLibrary getModels() {
		return library;
	}

	@Override
	public double getUncertainty(PredictionRequest predictionRequest) throws Exception {
		Radial2DModel model = library.getModel(predictionRequest);
		if (model == null)
			return Double.NaN;

		EnumMap<GeoAttributes, Double> values = model.interpolate(predictionRequest.getSource().getUnitVector());
		
		Double ttuncertainty = values.get(GeoAttributes.TT_MODEL_UNCERTAINTY);
		if (ttuncertainty == null)
			return Double.NaN;
		
		if (predictionRequest.getPhase() == SeismicPhase.T)
			ttuncertainty += model.htConvert();
		
		return ttuncertainty;
	}

	@Override
	public String getUncertaintyVersion() {
		return getPredictorVersion();
	}

	@Override
	public String getUncertaintyModelFile(PredictionRequest request) throws Exception {
		return modelDirectory.getCanonicalPath();
	}

	@Override
	public UncertaintyType getUncertaintyType() {
		return uncertaintyType;
	}

	public static String getVersion() {
		return Utils.getVersion("hydro-radial2d");
	}

	public String getRadial2dModelClassName() {
		return radial2dModelClassName;
	}

}
