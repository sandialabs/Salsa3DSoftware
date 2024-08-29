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
package gov.sandia.gmp.slbmwrapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintyInterface;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintyType;
import gov.sandia.gmp.lookupdz.LookupTablesGMP;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gnem.slbmjni.SLBMException;
import gov.sandia.gnem.slbmjni.SlbmInterface;

/**
 * Implements a wrapper around the SLBM_JNI (RSTT).
 * 
 * Like all implementations of PredictorInterface, SLBMWrapper is NOT thread-safe.
 * 
 * @author sballar
 * 
 */
public class SLBMWrapper extends Predictor implements UncertaintyInterface
{
	private static boolean libLoaded = false;
	private static SlbmInterface slbm;
	private static File slbmModel;
	private final double slbmMaxDistance;
	private final double slbmMaxDepth;
	private final double slbmCHMax;

	private Predictor predictor_lookup2d;
	private UncertaintyType uncertaintyType;

	/**
	 * This is the set of GeoAttributes that LookupTablesGMP is capable of
	 * computing. The set of GeoAttributes that is actually computed during any
	 * call to LookupTablesGMP.getPrediction() or getPredictions() will depend
	 * on the set of requestetdAttributes that are submitted as part of the
	 * PredictionRequest object.
	 */
	public static final EnumSet<GeoAttributes> supportedAttributes = EnumSet
			.of(GeoAttributes.TRAVEL_TIME,
					GeoAttributes.TT_BASEMODEL,
					GeoAttributes.TT_MODEL_UNCERTAINTY,
					GeoAttributes.TT_PATH_CORRECTION,
					GeoAttributes.TT_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.TT_PATH_CORR_DERIV_LAT,
					GeoAttributes.TT_PATH_CORR_DERIV_LON,
					GeoAttributes.TT_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.DTT_DLAT,
					GeoAttributes.DTT_DLON,
					GeoAttributes.DTT_DR,
					GeoAttributes.DTT_DTIME,
					GeoAttributes.AZIMUTH,
					GeoAttributes.AZIMUTH_DEGREES,
					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_LAT,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_LON,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.DAZ_DLAT,
					GeoAttributes.DAZ_DLON,
					GeoAttributes.DAZ_DR,
					GeoAttributes.DAZ_DTIME,
					GeoAttributes.SLOWNESS,
					GeoAttributes.SLOWNESS_DEGREES,
					GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY,
					GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_LAT,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_LON,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_RADIAL,
					// GeoAttributes.DSH_DLAT,
					// GeoAttributes.DSH_DLON,
					// GeoAttributes.DSH_DR,
					// GeoAttributes.DSH_DTIME,
					GeoAttributes.BACKAZIMUTH,
					GeoAttributes.BACKAZIMUTH_DEGREES,
					GeoAttributes.OUT_OF_PLANE, GeoAttributes.CALCULATION_TIME,
					GeoAttributes.DISTANCE, GeoAttributes.DISTANCE_DEGREES);

	protected static final EnumSet<SeismicPhase> supportedPhases = EnumSet.of(
			SeismicPhase.Pn, SeismicPhase.Sn, SeismicPhase.Pg, SeismicPhase.Lg);

	/**
	 * Implements a wrapper around the SLBM/RSTT
	 * 
	 * @param properties
	 * @throws Exception
	 * @throws IOException
	 */
	public SLBMWrapper(PropertiesPlus properties) throws Exception
	{
		super(properties);

		// set to huge value because slbm is not thread-safe
		predictionsPerTask = properties.getInt("slbmPredictionsPerTask", Integer.MAX_VALUE);

		loadLibSLBM(properties);

		slbmModel = getSLBMModelFile(properties);
		double val;
		val = properties.getDouble("slbm_max_distance", 15.);
		slbmMaxDistance = (val == Globals.NA_VALUE ? val : Math.toRadians(val));
		slbmMaxDepth = properties.getDouble("slbm_max_depth", 200.);
		slbmCHMax = properties.getDouble("slbm_ch_max", 0.2);

		if (slbmMaxDistance != Globals.NA_VALUE)
			slbm.setMaxDistance(slbmMaxDistance);
		if (slbmMaxDepth != Globals.NA_VALUE)
			slbm.setMaxDepth(slbmMaxDepth);
		if (slbmCHMax != Globals.NA_VALUE)
			slbm.setCHMax(slbmCHMax);

		if (properties.getBoolean("slbm_backstop_lookup2d", true))
			predictor_lookup2d = new LookupTablesGMP(properties);

		String type = properties.getProperty("rsttTTUncertaintyType", 
				properties.getProperty("slbmTTUncertaintyType", 
						properties.getProperty("slbmUncertaintyType")));

		if (type == null)
			throw new Exception("Must specify property slbmTTUncertaintyType equal to either 'distance_dependent' or 'path_dependent'"); 

		type = type.toLowerCase();
		if (type.contains("distance"))
			uncertaintyType = UncertaintyType.DISTANCE_DEPENDENT;
		else if (type.contains("path"))
			uncertaintyType = UncertaintyType.PATH_DEPENDENT;

		super.getUncertaintyInterface().put(GeoAttributes.TRAVEL_TIME, this);
	}

	static public List<String> getRecognizedProperties()
	{
		return Arrays.asList(new String[] { "maxProcessors", "slbmModel",
				"slbm_max_distance", "slbm_max_depth", "slbm_ch_max", "slbmUncertaintyType" });
	}

	/*
	 * Retrieve a Prediction for the supplied source, receiver, phase
	 * combination specified in the PredictionRequest object. Computed
	 * travel times include ellipticity and elevation corrections, if they are
	 * required. If the request in non-defining then an invalid Prediction is
	 * returned with errormessage "PredictionRequest was non-defining".
	 * Derivatives of travel with respect to radius and slowness with respect to
	 * lat, lon and radius are computed only if the appropriate GeoAttributes
	 * are specified in the Set of RequestedAttributes supplied in the
	 * PredictionRequest.
	 * 
	 * @see
	 * gov.sandia.gmp.util.interfaces.PredictorInterface#getPrediction(gov.sandia
	 * .gmp.util.interfaces.PredictionRequest)
	 */
	@Override
	public Prediction getPrediction(PredictionRequest request)
			throws Exception
	{
		travelTime = slowness = dttdlat = dttdlon = dttdr = Globals.NA_VALUE;

		if (!request.isDefining())
			return new SLBMResult(request, this,
					"PredictionRequest submitted to SLBMWrapper was non-defining");

		Prediction result = null;
		long timer = System.nanoTime();
		try
		{
			slbm.createGreatCircle(
					request.getPhase() == SeismicPhase.P ? "Pn" : request.getPhase().toString(), 
							request.getSource().getLat(), 
							request.getSource().getLon(),
							request.getSource().getDepth(), 
							request.getReceiver().getLat(), 
							request.getReceiver().getLon(),
							request.getReceiver().getDepth());

			result = new SLBMResult(request);

			travelTime = slbm.getTravelTime();
			slowness = slbm.getSlowness();
			dttdr = -slbm.get_dtt_ddepth();

			result.setAttribute(GeoAttributes.TT_BASEMODEL, travelTime);
			result.setAttribute(GeoAttributes.SLOWNESS_BASEMODEL, slowness);

			// dttdlat and dttdlon can be computed by slbm instead of the 
			// values computed by PredictionRequest
			result.setAttribute(GeoAttributes.DTT_DLAT, slbm.get_dtt_dlat());
			result.setAttribute(GeoAttributes.DTT_DLON, slbm.get_dtt_dlon());

			// set a whole bunch of other attributes based on the values of
			// the specified attributes.
			setGeoAttributes(result, travelTime, request.getSeaz(), slowness, dttdr, 
					Globals.NA_VALUE, Globals.NA_VALUE);

			result.setRayType(RayType.REFRACTION);

		}
		catch (Exception e)
		{
			if (predictor_lookup2d != null)
				result = predictor_lookup2d.getPrediction(request);  
			else if (e.getMessage().contains("c*H is greater than ch_max"))
				result = new SLBMResult(request, this, "c*H is greater than ch_max");
			else if (e.getMessage().contains("Source-receiver separation exceeds maximum value"))
				result = new SLBMResult(request, this, "Source-receiver separation exceeds maximum value");
			else if (e.getMessage().contains("Source depth exceeds maximum value"))
				result = new SLBMResult(request, this, "Source depth exceeds maximum value");
			else
				result = new SLBMResult(request, this, e);
		}

		if (request.getRequestedAttributes().contains(GeoAttributes.CALCULATION_TIME))
			result.setAttribute(GeoAttributes.CALCULATION_TIME, (System.nanoTime() - timer) * 1e-9);

		return result;
	}

	/*
	 * Retrieve the name of this Predictor: "SLBM"
	 */
	@Override
	public String getPredictorName()
	{
		return "slbm";
	}

	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, String msg) {
		return new SLBMResult(predictionRequest, this, msg);
	}

	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, Exception ex) {
		return new SLBMResult(predictionRequest, this, ex);
	}

	/*
	 * Returns the name of slbm model
	 */
	@Override
	public String getModelName()
	{
//		String name = slbmModel.getName();
//		int idx = name.lastIndexOf('.');
//		if (idx > 1)
//			name = name.substring(0, idx);
//		return name;
		return slbmModel.getName();
	}

	/*
	 * Returns the full path of file from which slmb model was loaded
	 */
	@Override
	public File getModelFile()
	{
		return slbmModel;
	}

	/*
	 * Returns the name of the directory from which the slbm model was read.
	 */
	@Override
	public String getModelDescription() throws Exception
	{
		return new GeoTessMetaData(slbmModel).getDescription();
	}

	public SlbmInterface getSlbmInterface()
	{
		return slbm;
	}

	@Override
	public boolean isSupported(Receiver receiver, SeismicPhase phase,
			GeoAttributes attribute, double epochTime)
	{
		return supportedPhases.contains(phase)
				&& supportedAttributes.contains(attribute);
	}

	public boolean isUncertaintySupported(Receiver receiver,
			SeismicPhase phase, GeoAttributes attribute)
	{
		return supportedPhases.contains(phase)
				&& supportedAttributes.contains(attribute);
	}

	/*
	 * Retrieve the value returned by calling SlbmInterface.getVersion().
	 */
	public String getSlbmVersion()
	{
		return slbm == null ? "null" : slbm.getVersion();
	}

	static public String getVersion() 	{ 
		return Utils.getVersion("slbm-wrapper");
	}

	@Override
	public String getPredictorVersion() {
		return getSlbmVersion();
	}

	@Override
	public double getUncertainty(PredictionRequest request) throws Exception
	{
		switch (uncertaintyType)
		{
		case DISTANCE_DEPENDENT:
			return slbm.getTravelTimeUncertainty(request.getPhase().toString(), request.getDistance());
		case PATH_DEPENDENT:
			return slbm.getTravelTimeUncertainty(true);
		default:
			return Double.NaN;
		}
	}

	@Override
	public PredictorType getPredictorType() { return PredictorType.SLBM; }

	@Override public EnumSet<GeoAttributes> getSupportedAttributes() { return supportedAttributes; }

	@Override
	public EnumSet<SeismicPhase> getSupportedPhases() { return supportedPhases; }

	private double travelTime = Globals.NA_VALUE;
	private double slowness = Globals.NA_VALUE;
	private double dttdlat = Globals.NA_VALUE;
	private double dttdlon = Globals.NA_VALUE;
	private double dttdr = Globals.NA_VALUE;

	protected double getTravelTime() throws Exception 
	{ 
		if (travelTime == Globals.NA_VALUE)
			travelTime = slbm.getTravelTime();
		return travelTime;
	}

	protected double getSlowness()  throws Exception 
	{ 
		if (slowness == Globals.NA_VALUE)
			slowness = slbm.getSlowness();
		return slowness;
	}

	protected double getDttDlat()  throws Exception 
	{ 
		if (dttdlat == Globals.NA_VALUE)
			dttdlat = slbm.get_dtt_dlat();
		return dttdlat;
	}

	protected double getDttDlon()  throws Exception 
	{ 
		if (dttdlon == Globals.NA_VALUE)
			dttdlon = slbm.get_dtt_dlon();
		return dttdlon;
	}

	protected double getDttDr()  throws Exception 
	{ 
		if (dttdr == Globals.NA_VALUE)
			dttdr = -slbm.get_dtt_ddepth();
		return dttdr;
	}

	protected double getDshDx()  throws Exception { return Globals.NA_VALUE; }

	protected double getDshDr()  throws Exception { return Globals.NA_VALUE; }

	@Override
	public Object getEarthModel() {
		return slbmModel;
	}

	@Override
	public String getUncertaintyVersion() {
		return slbm.getVersion();
	}

	/**
	 * 
	 */
	@Override
	public String getUncertaintyModelFile(PredictionRequest request) throws Exception {
		return slbmModel.getAbsolutePath();
	}

	/**
	 * Returns the type of the UncertaintyInterface object: UncertaintyNAValue,
	 * UncertaintyDistanceDependent, etc.
	 */
	@Override
	public UncertaintyType getUncertaintyType() { return uncertaintyType; }

	private synchronized static void loadLibSLBM(PropertiesPlus properties) 
			throws Exception{
		if(libLoaded) return;
		// first we'll just try and load the library
		try
		{
			System.loadLibrary("slbmjni");
		}

		// if that didn't work, we'll start checking environmental variables
		catch (java.lang.UnsatisfiedLinkError e)
		{
			// get the filename of the library we're looking for
			String libName = System.mapLibraryName("slbmjni");  // e.g., "libslbmjni.so"
			String libBase = libName.split("\\.")[0];  // file basename
			String libExt  = libName.split("\\.")[1];  // file extension

			// make our list of env vars to search, in preferred order
			String envVars[] = {"RSTT_ROOT", "RSTT_HOME", "SLBM_ROOT", "SLBM_HOME"};

			// initialize a boolean for when the library has loaded. if we have
			// successfully loaded it, we'll end the whole method
			boolean jniLoaded = false;

			// loop through each environment variable and look for slbmjni
			for (String env : envVars)
			{
				// try and get the environment variable
				String rootDir = System.getenv(env);

				// move on if it wasn't set
				if (rootDir == null)
					continue;

				// first check if libName exists
				if (new File(rootDir + "/lib/" + libBase + "." + libExt).exists())
					System.load(rootDir + "/lib/" + libBase + "." + libExt);  // load it

				// if that file doesn't exist, look for libslbmjni.jnilib
				else if (new File(rootDir + "/lib/" + libBase + ".jnilib").exists())
					System.load(rootDir + "/lib/" + libBase + ".jnilib");  // load it

				// if that doesn't exist, I we'll move onto the next variable
				else
					continue;

				// we made it this far, so we must have loaded the library!
				jniLoaded = true;  // set our boolean to true
				break;             // break out of the loop

			}

			// if, we still haven't loaded slbmjni, throw a helpful error message
			if (!jniLoaded)
			{
				// append some helpful info to the error message
				String errMsg = e.getMessage() + " or [$RSTT_ROOT/lib, $RSTT_HOME/lib, $SLBM_ROOT/lib, $SLBM_HOME/lib]";

				// make a new UnsatisfiedLinkError with our updated message
				UnsatisfiedLinkError ex = new UnsatisfiedLinkError(errMsg);

				// print out the stacktrace, some helpful info, and exit
				ex.printStackTrace();
				System.out.println("Did you try adding '-Djava.library.path=\"/path/to/rstt/lib\"' to your 'java' command?");
				System.out.println("Alternatively, set $RSTT_ROOT, $RSTT_HOME, $SLBM_ROOT, or $SLBM_HOME environment variables.");
				System.exit(1);
			}
		}


		File slbmModel = getSLBMModelFile(properties);

		if (!slbmModel.exists())
			throw new Exception(
					String.format("slbmModel = %s does not exist.", slbmModel.getCanonicalPath()));

		slbm = new SlbmInterface();
		try {
			slbm.loadVelocityModel(slbmModel.getCanonicalPath());
		} catch (SLBMException e) {
			throw new Exception(e);
		}

		libLoaded = true;
	}

	private static File getSLBMModelFile(PropertiesPlus properties) throws Exception {
		File slbmModel = null;
		if (properties.containsKey("slbmModel"))
			slbmModel = properties.getFile("slbmModel");
		else if (properties.containsKey("rsttModel"))
			slbmModel = properties.getFile("rsttModel");
		else
			throw new Exception("Must specify one of slbmModel or rsttModel in properties file");
		return slbmModel;
	}

}
