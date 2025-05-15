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
package gov.sandia.gmp.surfacewavepredictor;

import static gov.sandia.gmp.util.globals.Globals.NA_VALUE;

import java.io.File;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class SurfaceWavePredictor extends Predictor {

	private String modelName;

	private File modelDirectory;

	private EnumMap<SeismicPhase, SurfaceWaveModel> surfaceWaveModels;
	
	/**
	 * Constant value of travel time model uncertainty used for all surface wave 
	 * travel time predictions. Independent of source, receiver, distance, depth, path, etc.
	 */
	private double ttUncertainty;
	
	private double defaultPeriod;

	/**
	 * Map from a canonical directory name to a EnumMap<SeismicPhase, SurfaceWaveModel>
	 */
	static private Map<File, EnumMap<SeismicPhase, SurfaceWaveModel>> libraryMap = 
			new ConcurrentHashMap<File, EnumMap<SeismicPhase, SurfaceWaveModel>>();
	
	synchronized static public Map<File, EnumMap<SeismicPhase, SurfaceWaveModel>> getLibraryMap() { return libraryMap; }

	/**
	 * Retrieve a Radial2DLibrary object that contains all the models in the specified modelDirectory.
	 * A Radial2DLibrary that corresponds to a particular modelDirectory is only loaded into memory
	 * the first time this method is called with a particular modelDirectory.  Subsequent calls with 
	 * the same modelDirectory return a reference to the previously loaded library.
	 * 
	 * @param modelDirectory
	 * @return a Radial2DLibrary object.
	 * @throws Exception 
	 */
	synchronized static public EnumMap<SeismicPhase, SurfaceWaveModel> getLibrary(File modelDirectory) throws Exception {
		EnumMap<SeismicPhase, SurfaceWaveModel> library = libraryMap.get(modelDirectory.getCanonicalFile());
		if (library == null) {
			library = new EnumMap<SeismicPhase, SurfaceWaveModel>(SeismicPhase.class);
			for (SeismicPhase phase : new SeismicPhase[] {SeismicPhase.LR, SeismicPhase.LQ}) {
				SurfaceWaveModel model = new SurfaceWaveModel(modelDirectory, phase);
				if (model != null)
					library.put(phase, model);
			}
			libraryMap.put(modelDirectory.getCanonicalFile(), library);
		}
		return library;
	}
	
	/**
	 * This is the set of GeoAttributes supported by SurfaceWavePredictor
	 */
	public static final EnumSet<GeoAttributes> supportedAttributes = EnumSet.of(
			GeoAttributes.TRAVEL_TIME,
			GeoAttributes.TT_BASEMODEL, 
			GeoAttributes.TT_MODEL_UNCERTAINTY,
			GeoAttributes.TT_PATH_CORRECTION, 
			GeoAttributes.TT_PATH_CORR_DERIV_HORIZONTAL,
			GeoAttributes.TT_PATH_CORR_DERIV_RADIAL, 
			GeoAttributes.DTT_DLAT, 
			GeoAttributes.DTT_DLON, 
			GeoAttributes.DTT_DTIME,
			GeoAttributes.AZIMUTH, 
			GeoAttributes.AZIMUTH_DEGREES, 
			GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
			GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES,
			GeoAttributes.AZIMUTH_PATH_CORR_DERIV_HORIZONTAL,
			GeoAttributes.AZIMUTH_PATH_CORR_DERIV_RADIAL, 
			GeoAttributes.DAZ_DLAT, 
			GeoAttributes.DAZ_DLON,
			GeoAttributes.DAZ_DTIME, 
			GeoAttributes.SLOWNESS,
			GeoAttributes.SLOWNESS_DEGREES, 
			GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY,
			GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES,
			GeoAttributes.SLOWNESS_PATH_CORR_DERIV_HORIZONTAL,
			GeoAttributes.SLOWNESS_PATH_CORR_DERIV_RADIAL, 
			GeoAttributes.DSH_DTIME, 
			GeoAttributes.BACKAZIMUTH,
			GeoAttributes.OUT_OF_PLANE, 
			GeoAttributes.CALCULATION_TIME, 
			GeoAttributes.DISTANCE,
			GeoAttributes.DISTANCE_DEGREES);
	
	public SurfaceWavePredictor(PropertiesPlus properties) throws Exception {
		this(properties, null);
	}

	public SurfaceWavePredictor(PropertiesPlus properties, ScreenWriterOutput logger) throws Exception {
		super(properties, logger);
		
		// predictor name is 'surface_wave_predictor'

		predictionsPerTask = properties.getInt(getPredictorName()+"PredictionsPerTask", 500);

		modelDirectory = properties.getFile(getPredictorName()+"ModelDirectory");
		
		if (modelDirectory == null)
			throw new Exception("Must specify property "+(getPredictorName()+"ModelDirectory")+" in properties file.");
		
		if (!modelDirectory.exists())
			throw new Exception((getPredictorName()+"ModelDirectory")+" specified in properties file does not exist. "+modelDirectory.getPath());
		
		modelDirectory = modelDirectory.getCanonicalFile();
		
		modelName = modelDirectory.getName();

		surfaceWaveModels = getLibrary(modelDirectory);

		// there is a single, constant value of tt uncertainty associated with all surface wave predictions.
		// Default value is 30 seconds.
		ttUncertainty = properties.getDouble(getPredictorName()+"_tt_model_uncertainty", 30.);
		
		defaultPeriod = properties.getDouble(getPredictorName()+"_default_period", 20.);
		
		if (logger != null && logger.getVerbosity() > 0)
			logger.writef(getPredictorName()+" Predictor instantiated in %s%n", Globals.elapsedTime(constructorTimer));
	}

	@Override
	public Prediction getPrediction(PredictionRequest request) throws Exception {
		if (!request.isDefining())
			return new Prediction(request, this, "PredictionRequest submitted to LookuTablesGMP was non-defining");
		
		long timer = System.currentTimeMillis();

		Prediction prediction = new Prediction(request, PredictorType.SURFACE_WAVE_PREDICTOR);
		
		try {
			SurfaceWaveModel surfaceWaveModel = surfaceWaveModels.get(request.getPhase());
			
			double[] source = request.getSource().getUnitVector();
			double[] receiver = request.getReceiver().getUnitVector();

			if (surfaceWaveModel == null)
				return new Prediction(request, this,
						String.format("Phase %s is not supported by this instance of SurfaceWavePredictor.", request.getPhase().name()));

			// a default period can be retrieved from the properties file in the constructor.  The default value of the default 
			// period is 20 seconds.  If a period 
			double period = request.getPeriod();
			if (Double.isNaN(period) || period <= 0.)
				period = defaultPeriod;
			
			prediction.setAttribute(GeoAttributes.PERIOD, period);

			double travelTime = surfaceWaveModels.get(request.getPhase()).pathIntegral(source, receiver, period);
			
			prediction.setAttribute(GeoAttributes.TT_BASEMODEL, travelTime);

			if (request.getRequestedAttributes().contains(GeoAttributes.TT_MODEL_UNCERTAINTY)) {
				prediction.setAttribute(GeoAttributes.TT_MODEL_UNCERTAINTY, ttUncertainty);
				prediction.putUncertaintyType(GeoAttributes.TT_MODEL_UNCERTAINTY, 
						GeoAttributes.TT_MODEL_UNCERTAINTY_CONSTANT);
			}

			// slowness in sec/radian is earthRadius at source in km / velocity at source in km/sec.
			double slowness = VectorGeo.getEarthRadius(source)/new VelocityInterpolator(surfaceWaveModel, period).getVelocity(source);

			prediction.setAttribute(GeoAttributes.SLOWNESS_BASEMODEL, slowness);

			prediction.setRayType(RayType.SURFACE_WAVE);

			// specify travel time, azimuth, slowness, dttdr, dshdx, dshdr)
			setGeoAttributes(prediction, travelTime, request.getSeaz(), slowness, NA_VALUE, NA_VALUE, NA_VALUE);
			
		} catch (Exception e) {
			prediction = new Prediction(request, this, e);
		}

		if (request.getRequestedAttributes().contains(GeoAttributes.CALCULATION_TIME))
			prediction.setAttribute(GeoAttributes.CALCULATION_TIME,
					(System.currentTimeMillis() - timer) * 1e-3);

		return prediction;
	}

	/**
	 * Retrieve a new, invalid TaupResult object whose error message is set to the supplied string.
	 */
	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, String msg) {
		return new Prediction(predictionRequest, this, msg);
	}

	/**
	 * Retrieve a new, invalid TaupResult object whose error message is set to the error message and
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
		return getPredictorType().name().toLowerCase();
	}

	@Override
	public PredictorType getPredictorType() {
		return PredictorType.SURFACE_WAVE_PREDICTOR;
	}

	@Override
	public File getModelFile() {
		return modelDirectory;
	}

	@Override
	public boolean isSupported(Receiver receiver, SeismicPhase phase, GeoAttributes attribute, double originTime) {
		return surfaceWaveModels.containsKey(phase);
	}

	@Override
	public String getPredictorVersion() {
		return Utils.getVersion("surface-wave-predictor");
	}

	@Override
	public EnumSet<GeoAttributes> getSupportedAttributes() {
		return supportedAttributes;
	}

	@Override
	public EnumSet<SeismicPhase> getSupportedPhases() {
		EnumSet<SeismicPhase> set = EnumSet.noneOf(SeismicPhase.class);
		set.addAll(surfaceWaveModels.keySet());
		return set;
	}

	@Override
	public Object getEarthModel() {
		return surfaceWaveModels;
	}

	public static String getVersion() {
		return  Utils.getVersion("surface-wave-predictor");
	}

	public void  close() throws Exception {
		super.close();
		for (Entry<File, EnumMap<SeismicPhase, SurfaceWaveModel>> entry1 : libraryMap.entrySet())
			for (Entry<SeismicPhase, SurfaceWaveModel> entry2 : entry1.getValue().entrySet())
				entry2.getValue().close();
		libraryMap.clear();
	}


}
