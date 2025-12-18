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
package gov.sandia.gmp.baseobjects.interfaces.impl;

import static gov.sandia.gmp.util.globals.Globals.NA_VALUE;
import static gov.sandia.gmp.util.globals.Globals.TWO_PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3D;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.sasc.SASC_Library;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintyAzimuth;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintySlowness;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.GeoMath;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * If method getPrediction(PredictionRequest) is called then a Prediction for a single
 * source-receiver pair is computed in sequential mode. Methods getPredictions() compute multiple
 * predictions in concurrent mode.
 * 
 * @author sballar
 * 
 */
abstract public class Predictor implements Callable<Predictor> {
	
	private static int nextIndex = 0;

	protected ArrayList<PredictionRequest> predictionRequest;

	protected ArrayList<Prediction> predictions;

	protected LibCorr3D libcorr3d;

	private int index;

	protected PropertiesPlus properties;

	protected int taskIndex;
	long taskTimer;

	private int predictorVerbosity;

	protected int predictionsPerTask = 1000;

	protected final int maxProcessors;

	/**
	 * Timer gets set to System.currentTimeMillis() at start of constructor.
	 */
	protected long constructorTimer = System.currentTimeMillis();

	/**
	 * Here to facilitate interactions with database. Getter and setter provided but value is never
	 * modified by this class.
	 */
	private long modelId = -1;

	/**
	 * Here to facilitate interactions with database. Getter and setter provided but value is never
	 * modified by this class.
	 */
	private long algorithmId = -1;

	/**
	 * Uncertainty scale and offset.  Default to 1, 0.
	 * Keys are TT_MODEL_UNCERTAINTY, AZIMUTH_MODEL_UNCERTAINTY, SLOWNESS_MODEL_UNCERTAINTY.
	 */
	protected Map<GeoAttributes, double[]> uncertaintyScale;

	protected boolean usePathCorrectionsInDerivativesTT;
	protected boolean usePathCorrectionsInDerivativesAZ;
	protected boolean usePathCorrectionsInDerivativesSH;

	protected transient ExecutorService threads = null;

	protected UncertaintyAzimuth uncertaintyAzimuth;
	protected UncertaintySlowness uncertaintySlowness;
	
	private SASC_Library sascLibrary;

	public Predictor() {
		this.maxProcessors = Runtime.getRuntime().availableProcessors();
	}

	/**
	 * 
	 * @param properties
	 * @throws Exception
	 */
	public Predictor(PropertiesPlus properties) throws Exception {
		this(properties, null);  
	}

	/**
	 * 
	 * @param properties
	 * @throws Exception
	 */
	public Predictor(PropertiesPlus properties, ScreenWriterOutput logger) throws Exception {
		
		index = nextIndex++;

		this.properties = properties;

		try {
			GeoMath.setEarthShape(properties);
		} catch (Exception e) {
			throw new Exception(String.format("%nThe properties object specifies earthShape %s "
					+ "but earthShape has already been set to %s%n"
					+ "Once earthShape is set anywhere it cannot be changed.",
					properties.getProperty("earthShape", EarthShape.WGS84.toString()),
					GeoMath.getEarthShape().toString()));
		}

		String prefix = getPredictorName().toLowerCase();

		// set maxProcessors, the maximum number of processors to use concurrently.
		this.maxProcessors =
				properties.getInt("maxProcessors", Runtime.getRuntime().availableProcessors());

		this.predictorVerbosity = properties.getInt(prefix + "PredictorVerbosity", 0);


		File azSloUncertaintyFile = properties.getFile(prefix+"AzSloUncertaintyFile", 
				properties.getFile("AzSloUncertaintyFile"));

		if (azSloUncertaintyFile == null) {
			uncertaintyAzimuth = new UncertaintyAzimuth();
			uncertaintySlowness = new UncertaintySlowness();
		}
		else {
			if (azSloUncertaintyFile.exists() && azSloUncertaintyFile.isDirectory() &&
					new File(azSloUncertaintyFile, "azimuth_slowness_uncertainty.dat").exists())
				azSloUncertaintyFile = new File(azSloUncertaintyFile, "azimuth_slowness_uncertainty.dat");
			uncertaintyAzimuth = new UncertaintyAzimuth(azSloUncertaintyFile);
			uncertaintySlowness = new UncertaintySlowness(azSloUncertaintyFile);
		}

		uncertaintyScale = new HashMap<GeoAttributes, double[]>();
		
		double[] scale = properties.getDoubleArray(prefix + "TTModelUncertaintyScale", new double[] {1.0, 0.0});
		double offset = properties.getDouble(prefix + "TTModelUncertaintyOffset", 0.0);
		if (scale.length == 1 || offset != 0.0) 
			scale = new double[] {scale[0], offset};
		uncertaintyScale.put(GeoAttributes.TT_MODEL_UNCERTAINTY, scale);

		scale = properties.getDoubleArray(prefix + "AZModelUncertaintyScale", new double[] {1.0, 0.0});
		offset = properties.getDouble(prefix + "AZModelUncertaintyOffset", 0.0);
		if (scale.length == 1 || offset != 0.0) 
			scale = new double[] {scale[0], offset};
		uncertaintyScale.put(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
				new double[] {scale[0], toRadians(scale[1])});  // convert offset to radians

		scale = properties.getDoubleArray(prefix + "SHModelUncertaintyScale", new double[] {1.0, 0.0});
		offset = properties.getDouble(prefix + "SHModelUncertaintyOffset", 0.0);
		if (scale.length == 1 || offset != 0.0) 
			scale = new double[] {scale[0], offset};
		uncertaintyScale.put(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY,
				new double[] {scale[0], toDegrees(scale[1])});  // convert offset to sec/radian


		String type = properties.getProperty(prefix + "PathCorrectionsType", "");
		if (type.toLowerCase().startsWith("libcorr")) {
			
			// if an appropriate libcorr3d object already exists, a reference will be returned.
			this.libcorr3d = LibCorr3D.getLibCorr3D(prefix, properties, logger);

			if (!this.libcorr3d.isEmpty()) {
				boolean usePathCorrectionsInDerivatives =
						properties.getBoolean(prefix + "UsePathCorrectionsInDerivatives", false);

				usePathCorrectionsInDerivativesTT = properties.getBoolean(
						prefix + "UsePathCorrectionsInDerivativesTT", usePathCorrectionsInDerivatives);

				usePathCorrectionsInDerivativesAZ = properties.getBoolean(
						prefix + "UsePathCorrectionsInDerivativesAZ", usePathCorrectionsInDerivatives);

				usePathCorrectionsInDerivativesSH = properties.getBoolean(
						prefix + "UsePathCorrectionsInDerivativesSH", usePathCorrectionsInDerivatives);
				
			}
		} else
			this.libcorr3d = LibCorr3D.getLibCorr3D();
		
		File sascLibraryDirectory = properties.getFile("sascLibraryDirectory");
		if (sascLibraryDirectory != null) 
			sascLibrary = SASC_Library.getLibrary(sascLibraryDirectory);
	}

	/**
	 * Derived classes must supply a method that computes a single Predictor object given a single
	 * PredictionRequest object.
	 * 
	 * @throws Exception
	 */
	abstract public Prediction getPrediction(PredictionRequest request) throws Exception;

	@Override
	public Predictor call() {
		if (predictorVerbosity > 1)
			taskTimer = System.currentTimeMillis();
		predictions = new ArrayList<>(predictionRequest.size());
		for (PredictionRequest request : predictionRequest) {
			Prediction prediction = null;
			try {
				prediction = getPrediction(request);
			} catch (Exception e) {
				prediction = getNewPrediction(request, e);
			}
			predictions.add(prediction);
		}
		if (predictorVerbosity > 1)
			taskTimer = System.currentTimeMillis() - taskTimer;
		return this;
	}

	/**
	 * Derived classed must supply a method that returns a new Predictor object based on a
	 * PredictionRequest and an error message.
	 * 
	 * @param predictionRequest
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	public abstract Prediction getNewPrediction(PredictionRequest predictionRequest, String msg);

	/**
	 * @throws Exception Derived classed must supply a method that returns a new Predictor object
	 *         based on a PredictionRequest and an Exception.
	 * 
	 * @param predictionRequest
	 * @param ex
	 * @return
	 * @throws
	 */
	public abstract Prediction getNewPrediction(PredictionRequest predictionRequest, Exception ex);

	abstract public String getModelDescription() throws Exception;

	abstract public String getModelName();

	abstract public String getPredictorName();

	abstract public PredictorType getPredictorType();

	/**
	 * Return the Predictor that supports the specified PredictionRequest. Most Predictor represent
	 * just one Predictor, independent of the request. But some Predictors, such as BenderLookup2D,
	 * represent multiple Predictors depending on the request. Those predictors will override this
	 * method.
	 * 
	 * @param predicitionRequest
	 * @return
	 * @throws Exception
	 */
	public Predictor getPredictor(PredictionRequest predicitionRequest) throws Exception {
		return this;
	}

	public long getAlgorithmId() {
		return algorithmId;
	}

	public long getModelId() {
		return modelId;
	}

	public synchronized void setAlgorithmId(long algorithmId) {
		this.algorithmId = algorithmId;
	}

	public synchronized void setModelId(long modelId) {
		this.modelId = modelId;
	}

	abstract public File getModelFile();

	/**
	 * @return the maxProcessors
	 */
	public int getMaxProcessors() {
		return maxProcessors;
	}

	/**
	 * @return the number of predictions per task
	 */
	public int getPredictionsPerTask() {
		return predictionsPerTask;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Retrieve a brief name for the model loaded into Bender. If the name is longer than maxLength it
	 * will be truncated.
	 */
	public String getModelName(int maxLength) {
		String name = getModelName();
		if (name.length() <= maxLength)
			return name;
		return name.substring(0, maxLength);
	}

	public int getPredictorVerbosity() {
		return predictorVerbosity;
	}

	public void setPredictorVerbosity(int predictorVerbosity) {
		this.predictorVerbosity = predictorVerbosity;
	}

	public abstract boolean isSupported(Receiver receiver, SeismicPhase phase,
			GeoAttributes attribute, double originTime);

	abstract public String getPredictorVersion();

	abstract public EnumSet<GeoAttributes> getSupportedAttributes();

	abstract public EnumSet<SeismicPhase> getSupportedPhases();

	public abstract Object getEarthModel();

	public LibCorr3D getLibcorr3d() {
		return libcorr3d;
	}

	private GeoTessPosition getLibcorrPosition(PredictionRequest request, String attribute)
			throws Exception {
		GeoTessPosition pos = libcorr3d.getGeoTessPosition(request.getReceiver(),
				request.getPhase().toString(), attribute);
		if (pos != null) {
			//			pos.set(request.getSource().getUnitVector(), request.getSource().getRadius());
			//			if (VectorGeo.getEarthShape() != pos.getEarthShape())
			//				pos.setRadius(pos.getEarthRadius()-request.getSource().getDepth());
			pos.set(request.getSource().getLatDegrees(), request.getSource().getLonDegrees(), 
					request.getSource().getDepth());
		}
		return pos;
	}

	/**
	 * Close the libcorr3d object if it exists.
	 * Closing libcorr3d will call close() on all the models it currently has in memory.
	 * It is important to do this so that the GeoTessGrids stored in GeoTessModel.reuseGridMap
	 * will be released for garbage collection.
	 * @throws Exception
	 */
	public void close() throws Exception { libcorr3d.close(); }

	/**
	 * Retrieve the derivative of travel time wrt latitude in seconds/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	private double getDttDlat(double slowness, double backAzimuth) throws Exception {
		if (slowness == NA_VALUE || backAzimuth == NA_VALUE)
			return NA_VALUE;
		return -slowness * cos(backAzimuth);
	}

	/**
	 * Retrieve the derivative of travel time wrt longitude in seconds/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	private double getDttDlon(double slowness, double backAzimuth) throws Exception {
		if (slowness == NA_VALUE || backAzimuth == NA_VALUE)
			return NA_VALUE;
		return -slowness * sin(backAzimuth);
	}

	/**
	 * Retrieve the derivative of slowness wrt latitude in seconds/radian/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	private double getDshDlat(double dshdx, double backAzimuth) throws Exception {
		if (dshdx == NA_VALUE || backAzimuth == NA_VALUE)
			return NA_VALUE;
		return -dshdx * cos(backAzimuth);
	}

	/**
	 * Retrieve the derivative of slowness wrt longitude in seconds/radian/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	private double getDshDlon(double dshdx, double backAzimuth) throws Exception {
		if (dshdx == NA_VALUE || backAzimuth == NA_VALUE)
			return NA_VALUE;
		return -dshdx * sin(backAzimuth);
	}

	/**
	 * Retrieve the derivative of receiver-source azimuth wrt latitude, in radians/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	private double getDazDlat(double distance, double backAzimuth) throws Exception {
		if (backAzimuth == NA_VALUE || distance < 1e-7)
			return NA_VALUE;
		return sin(backAzimuth) / sin(distance);
	}

	/**
	 * Retrieve the derivative of receiver-source azimuth wrt longitude, in radians/radian.
	 * 
	 * @return
	 * @throws Exception
	 */
	private double getDazDlon(double distance, double backAzimuth) throws Exception {
		if (backAzimuth == NA_VALUE || distance < 1e-7)
			return NA_VALUE;
		return -cos(backAzimuth) / sin(distance);
	}

	/**
	 * Retrieve the derivative of azimuth wrt to source radius, in radians/km. Usually zero.
	 * 
	 * @return
	 * @throws Exception
	 */
	private double getDazDr() throws Exception {
		return 0.;
	}

	/**
	 * Retrieve the derivative of travel time wrt to origin time, in seconds/second Hard to imagine a
	 * scenario where this is not 1..
	 * 
	 * @return
	 * @throws Exception
	 */
	private double getDttDt() throws Exception {
		return 1.;
	}

	/**
	 * Retrieve the derivative of azimuth wrt to origin time, in radians/second
	 * 
	 * @return
	 * @throws Exception
	 */
	private double getDazDt() throws Exception {
		return 0.;
	}

	/**
	 * Retrieve the derivative of slowness wrt to origin time, in seconds/radians/second
	 * 
	 * @return
	 * @throws Exception
	 */
	private double getDshDt() throws Exception {
		return 0.;
	}

	/**
	 * This method will set many GeoAttributes in the Prediction based on the supplied parameters,
	 * including path corrections and model uncertainties if available. Values can be NaN or NA_VALUE
	 * if not available
	 * 
	 * @param prediction the Prediction object that is to be partially populated by this method.
	 * @param tt travel time in seconds
	 * @param azimuth receiver-source azimuth in radians
	 * @param slowness horizontal slowness in sec/radian
	 * @param dttdr derivative of travel time wrt radius in sec/km
	 * @param dshdx derivative of slowness wrt distance in sec/radian^2
	 * @param dshdr derivative of slowness wrt radius in sec/(radian.km)
	 * @throws Exception
	 */
	public void setGeoAttributes(Prediction prediction, double tt, double azimuth, double slowness,
			double dttdr, double dshdx, double dshdr) throws Exception {

		PredictionRequest request = prediction.getPredictionRequest();

		EnumSet<GeoAttributes> requestedAttributes = request.getRequestedAttributes();

		prediction.setModelName(getModelName());
		prediction.setPredictorName(getPredictorName());
		prediction.setPredictorVersion(getPredictorVersion());
		
		prediction.setSascLibrary(sascLibrary);

		if (Double.isNaN(tt))
			tt = NA_VALUE;

		if (Double.isNaN(dttdr))
			dttdr = NA_VALUE;

		if (Double.isNaN(slowness))
			slowness = NA_VALUE;

		if (Double.isNaN(dshdx))
			dshdx = NA_VALUE;

		if (Double.isNaN(dshdr))
			dshdr = NA_VALUE;

		// get greatcircle azimuth if azimuth not already computed in derived class
		if (Double.isNaN(azimuth) || azimuth == NA_VALUE)
			azimuth = request.getSeaz(NA_VALUE);

		// get greatcircle backazimuth (source - receiver azimuth) if not already computed in derived class
		double backAzimuth = prediction.getAttribute(GeoAttributes.BACKAZIMUTH);
		if (Double.isNaN(backAzimuth) || backAzimuth == NA_VALUE)
			backAzimuth = request.getEsaz(0.);

		// source-receiver great circle distance in radians.
		double distance = request.getDistance(); 

		// Now start setting requestedAttributes

		if (requestedAttributes.contains(GeoAttributes.BACKAZIMUTH))
			prediction.setAttribute(GeoAttributes.BACKAZIMUTH, backAzimuth);

		if (requestedAttributes.contains(GeoAttributes.BACKAZIMUTH_DEGREES))
			prediction.setAttribute(GeoAttributes.BACKAZIMUTH_DEGREES,
					backAzimuth == NA_VALUE ? NA_VALUE : toDegrees(backAzimuth));

		if (requestedAttributes.contains(GeoAttributes.DISTANCE))
			prediction.setAttribute(GeoAttributes.DISTANCE, distance);

		if (requestedAttributes.contains(GeoAttributes.DISTANCE_DEGREES))
			prediction.setAttribute(GeoAttributes.DISTANCE_DEGREES, toDegrees(distance));

		if (requestedAttributes.contains(GeoAttributes.OUT_OF_PLANE)
				&& prediction.getAttribute(GeoAttributes.OUT_OF_PLANE) == NA_VALUE)
			prediction.setAttribute(GeoAttributes.OUT_OF_PLANE, 0.);

		if (requestedAttributes.contains(GeoAttributes.TRAVEL_TIME))
			prediction.setAttribute(GeoAttributes.TRAVEL_TIME, tt);

		if (requestedAttributes.contains(GeoAttributes.DTT_DLAT)
				&& prediction.getAttribute(GeoAttributes.DTT_DLAT) == NA_VALUE)
			prediction.setAttribute(GeoAttributes.DTT_DLAT, getDttDlat(slowness, backAzimuth));

		if (requestedAttributes.contains(GeoAttributes.DTT_DLON)
				&& prediction.getAttribute(GeoAttributes.DTT_DLON) == NA_VALUE)
			prediction.setAttribute(GeoAttributes.DTT_DLON, getDttDlon(slowness, backAzimuth));

		if (requestedAttributes.contains(GeoAttributes.DTT_DR))
			prediction.setAttribute(GeoAttributes.DTT_DR, dttdr);

		if (requestedAttributes.contains(GeoAttributes.DTT_DTIME)
				&& prediction.getAttribute(GeoAttributes.DTT_DTIME) == NA_VALUE)
			prediction.setAttribute(GeoAttributes.DTT_DTIME, getDttDt());

		GeoTessPosition libcorrPosTT = getLibcorrPosition(request, "TT");

		double correction = NA_VALUE;
		if (libcorrPosTT != null) {

			prediction.setModelName(((LibCorr3DModel) libcorrPosTT.getModel()).getVmodel());
			prediction.setPredictorName("libcorr3d");
			prediction.setPredictorVersion(LibCorr3D.getVersion());

			correction = libcorrPosTT.getValue(0);
			if (Double.isNaN(correction))
				correction = NA_VALUE;

			if (tt != NA_VALUE && correction != NA_VALUE) {
				tt += correction;
				prediction.setAttribute(GeoAttributes.TRAVEL_TIME, tt);
				prediction.setAttribute(GeoAttributes.TT_PATH_CORRECTION, correction);
			}

			if (libcorrPosTT.getModel().getNAttributes() > 1) {
				double uncertainty = libcorrPosTT.getValue(1);
				if (Double.isNaN(uncertainty)) 
					uncertainty = NA_VALUE;
				if (uncertainty != NA_VALUE) {
					prediction.setAttribute(GeoAttributes.TT_MODEL_UNCERTAINTY, uncertainty);
					prediction.putUncertaintyType(GeoAttributes.TT_MODEL_UNCERTAINTY, GeoAttributes.TT_MODEL_UNCERTAINTY_PATH_DEPENDENT);
				}

				if (usePathCorrectionsInDerivativesTT) {
					if (requestedAttributes.contains(GeoAttributes.DTT_DLAT)) {
						double value = prediction.getAttribute(GeoAttributes.DTT_DLAT);
						correction = libcorrPosTT.getDerivLat()[0];
						if (Double.isNaN(correction))
							correction = NA_VALUE;
						if (value != NA_VALUE && correction != NA_VALUE) {
							prediction.setAttribute(GeoAttributes.DTT_DLAT, value + correction);
							prediction.setAttribute(GeoAttributes.TT_PATH_CORR_DERIV_LAT, correction);
						}
					}

					if (requestedAttributes.contains(GeoAttributes.DTT_DLON)) {
						double value = prediction.getAttribute(GeoAttributes.DTT_DLON);
						correction = libcorrPosTT.getDerivLon()[0];
						if (Double.isNaN(correction))
							correction = NA_VALUE;
						if (value != NA_VALUE && correction != NA_VALUE) {
							prediction.setAttribute(GeoAttributes.DTT_DLON, value + correction);
							prediction.setAttribute(GeoAttributes.TT_PATH_CORR_DERIV_LON, correction);
						}
					}

					if (requestedAttributes.contains(GeoAttributes.DTT_DR)) {
						double value = prediction.getAttribute(GeoAttributes.DTT_DR);
						correction = libcorrPosTT.getDerivRadial()[0];
						if (Double.isNaN(correction))
							correction = NA_VALUE;
						if (value != NA_VALUE && correction != NA_VALUE) {
							prediction.setAttribute(GeoAttributes.DTT_DR, value + correction);
							prediction.setAttribute(GeoAttributes.TT_PATH_CORR_DERIV_RADIAL, correction);
						}
					}
				}
			}
		}

		if (requestedAttributes.contains(GeoAttributes.SLOWNESS)
				|| requestedAttributes.contains(GeoAttributes.SLOWNESS_DEGREES)) {

			if (requestedAttributes.contains(GeoAttributes.SLOWNESS))
				prediction.setAttribute(GeoAttributes.SLOWNESS, slowness);

			if (requestedAttributes.contains(GeoAttributes.SLOWNESS_DEGREES))
				prediction.setAttribute(GeoAttributes.SLOWNESS_DEGREES,
						slowness == NA_VALUE ? NA_VALUE : toRadians(slowness));

			if (requestedAttributes.contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY)
					|| requestedAttributes.contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES)) {
				double uncertainty = uncertaintySlowness.getSloUncertainty(request.getReceiver().getSta(), 
						request.getPhase().toString());
				prediction.setAttribute(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY, uncertainty);
				prediction.putUncertaintyType(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY, 
						GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_STATION_PHASE_DEPENDENT);
			}			

			if (requestedAttributes.contains(GeoAttributes.SLOWNESS_BASEMODEL))
				prediction.setAttribute(GeoAttributes.SLOWNESS_BASEMODEL, slowness);

			if (requestedAttributes.contains(GeoAttributes.DSH_DX))
				prediction.setAttribute(GeoAttributes.DSH_DX, dshdx);

			if (requestedAttributes.contains(GeoAttributes.DSH_DX_DEGREES))
				prediction.setAttribute(GeoAttributes.DSH_DX_DEGREES,
						dshdx == NA_VALUE ? NA_VALUE : toRadians(toRadians(dshdx)));

			if (requestedAttributes.contains(GeoAttributes.DSH_DLAT))
				prediction.setAttribute(GeoAttributes.DSH_DLAT, getDshDlat(dshdx, backAzimuth));

			if (requestedAttributes.contains(GeoAttributes.DSH_DLON))
				prediction.setAttribute(GeoAttributes.DSH_DLON, getDshDlon(dshdx, backAzimuth));

			if (requestedAttributes.contains(GeoAttributes.DSH_DR))
				prediction.setAttribute(GeoAttributes.DSH_DR, dshdr);

			if (requestedAttributes.contains(GeoAttributes.DSH_DTIME))
				prediction.setAttribute(GeoAttributes.DSH_DTIME, getDshDt());

			GeoTessPosition libcorrPosSH = getLibcorrPosition(request, "SH");
			if (libcorrPosSH != null) {

				boolean degrees =
						libcorrPosSH.getModel().getMetaData().getAttributeUnit(0).toLowerCase().contains("deg");

				// conversion factor from sec/degree to sec/radian
				double convert = degrees ? toDegrees(1.) : 1.;

				correction = libcorrPosSH.getValue(0) * convert;
				if (Double.isNaN(correction))
					correction = NA_VALUE;

				if (slowness != NA_VALUE && correction != NA_VALUE) {
					slowness += correction;

					prediction.setAttribute(GeoAttributes.SLOWNESS_PATH_CORRECTION, correction);

					if (requestedAttributes.contains(GeoAttributes.SLOWNESS))
						prediction.setAttribute(GeoAttributes.SLOWNESS, slowness);

					if (requestedAttributes.contains(GeoAttributes.SLOWNESS_DEGREES))
						prediction.setAttribute(GeoAttributes.SLOWNESS_DEGREES,
								slowness == NA_VALUE ? NA_VALUE : toRadians(slowness));
				}

				if (libcorrPosSH.getModel().getNAttributes() > 1) {
					double uncertainty = libcorrPosSH.getValue(1);
					if (libcorrPosSH.getModel().getMetaData().getAttributeUnit(1).toLowerCase().contains("deg"))
						uncertainty = toDegrees(uncertainty);  // toDegrees() changes sec/deg to sec/radian
					if (Double.isNaN(uncertainty))
						uncertainty = NA_VALUE;
					prediction.setAttribute(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY, uncertainty); // units are sec/radian
					prediction.putUncertaintyType(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY, GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_PATH_DEPENDENT);
				}

				if (usePathCorrectionsInDerivativesSH) {

					convert = degrees ? toDegrees(toDegrees(1.)) : 1.;

					if (requestedAttributes.contains(GeoAttributes.DSH_DX)) {
						correction =
								libcorrPosSH.getDerivHorizontal(request.getReceiver().getUnitVector())[0] * convert;
						if (Double.isNaN(correction))
							correction = NA_VALUE;
						prediction.setAttribute(GeoAttributes.SLOWNESS_PATH_CORR_DERIV_HORIZONTAL, correction);
						if (dshdx != NA_VALUE && correction != NA_VALUE)
							prediction.setAttribute(GeoAttributes.DSH_DX, dshdx + correction);
					}

					if (requestedAttributes.contains(GeoAttributes.DSH_DLAT)) {
						double dshdlat = prediction.getAttribute(GeoAttributes.DSH_DLAT);
						correction = libcorrPosSH.getDerivLat()[0] * convert;
						if (Double.isNaN(correction))
							correction = NA_VALUE;
						prediction.setAttribute(GeoAttributes.SLOWNESS_PATH_CORR_DERIV_LAT, correction);
						if (dshdlat != NA_VALUE && correction != NA_VALUE)
							prediction.setAttribute(GeoAttributes.DSH_DLAT, dshdlat + correction);
					}

					if (requestedAttributes.contains(GeoAttributes.DSH_DLON)) {
						double dshdlon = prediction.getAttribute(GeoAttributes.DSH_DLON);
						correction = libcorrPosSH.getDerivLon()[0] * convert;
						if (Double.isNaN(correction))
							correction = NA_VALUE;
						prediction.setAttribute(GeoAttributes.SLOWNESS_PATH_CORR_DERIV_LON, correction);
						if (dshdlon != NA_VALUE && correction != NA_VALUE)
							prediction.setAttribute(GeoAttributes.DSH_DLON, dshdlon + correction);
					}

					if (requestedAttributes.contains(GeoAttributes.DSH_DR)) {
						convert = degrees ? toDegrees(toDegrees(1.)) : 1.;
						correction = libcorrPosSH.getDerivRadial()[0] * convert;
						// if necessary, convert from sec/degree/km to sec/radian/km
						if (degrees)
							correction = toDegrees(correction);
						if (Double.isNaN(correction))
							correction = NA_VALUE;
						prediction.setAttribute(GeoAttributes.SLOWNESS_PATH_CORR_DERIV_RADIAL, correction);
						if (dshdr != NA_VALUE && correction != NA_VALUE)
							prediction.setAttribute(GeoAttributes.DSH_DR, dshdr + correction);
					}
				}
			}
		}

		if (requestedAttributes.contains(GeoAttributes.AZIMUTH)
				|| requestedAttributes.contains(GeoAttributes.AZIMUTH_DEGREES)) {

			prediction.setAttribute(GeoAttributes.AZIMUTH_BASEMODEL, azimuth);

			if (requestedAttributes.contains(GeoAttributes.AZIMUTH))
				prediction.setAttribute(GeoAttributes.AZIMUTH, azimuth);
			
			if (requestedAttributes.contains(GeoAttributes.AZIMUTH_DEGREES))
				prediction.setAttribute(GeoAttributes.AZIMUTH_DEGREES, 
						azimuth == NA_VALUE ? NA_VALUE : toDegrees(azimuth));

			if (requestedAttributes.contains(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY)
					|| requestedAttributes.contains(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES)) {
				double az = uncertaintyAzimuth.getAzUncertainty(request.getReceiver().getSta(), 
						request.getPhase().toString());
				prediction.setAttribute(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY, az);
				prediction.putUncertaintyType(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY, 
						GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_STATION_PHASE_DEPENDENT);
			}			

			if (requestedAttributes.contains(GeoAttributes.DAZ_DLAT)
					&& prediction.getAttribute(GeoAttributes.DAZ_DLAT) == NA_VALUE)
				prediction.setAttribute(GeoAttributes.DAZ_DLAT, getDazDlat(distance, backAzimuth));

			if (requestedAttributes.contains(GeoAttributes.DAZ_DLON)
					&& prediction.getAttribute(GeoAttributes.DAZ_DLON) == NA_VALUE)
				prediction.setAttribute(GeoAttributes.DAZ_DLON, getDazDlon(distance, backAzimuth));

			if (requestedAttributes.contains(GeoAttributes.DAZ_DR)
					&& prediction.getAttribute(GeoAttributes.DAZ_DR) == NA_VALUE)
				prediction.setAttribute(GeoAttributes.DAZ_DR, getDazDr());

			if (requestedAttributes.contains(GeoAttributes.DAZ_DTIME)
					&& prediction.getAttribute(GeoAttributes.DAZ_DTIME) == NA_VALUE)
				prediction.setAttribute(GeoAttributes.DAZ_DTIME, getDazDt());

			GeoTessPosition libcorrPosAZ = getLibcorrPosition(request, "AZ");
			if (libcorrPosAZ != null) {

				boolean degrees =
						libcorrPosAZ.getModel().getMetaData().getAttributeUnit(0).toLowerCase().contains("deg");

				// conversion factor from degrees to radians
				double convert = degrees ? toRadians(1.) : 1.;

				correction = libcorrPosAZ.getValue(0) * convert;
				if (Double.isNaN(correction))
					correction = NA_VALUE;

				if (azimuth != NA_VALUE && correction != NA_VALUE) {
					prediction.setAttribute(GeoAttributes.AZIMUTH_PATH_CORRECTION, correction);
					if (requestedAttributes.contains(GeoAttributes.AZIMUTH))
						prediction.setAttribute(GeoAttributes.AZIMUTH,
								(azimuth + correction + TWO_PI) % TWO_PI);

					if (requestedAttributes.contains(GeoAttributes.AZIMUTH_DEGREES))
						prediction.setAttribute(GeoAttributes.AZIMUTH_DEGREES,
								toDegrees((azimuth + correction + TWO_PI) % TWO_PI));
				}

				if (libcorrPosAZ.getModel().getNAttributes() > 1) {
					double uncertainty = libcorrPosAZ.getValue(1);
					if (libcorrPosAZ.getModel().getMetaData().getAttributeUnit(1).toLowerCase().contains("deg"))
						uncertainty = toRadians(uncertainty);  // convert deg to radians
					if (Double.isNaN(uncertainty))
						uncertainty = NA_VALUE;
					prediction.setAttribute(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY, uncertainty); // units are radian
					prediction.putUncertaintyType(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY, GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_PATH_DEPENDENT);
				}

				if (usePathCorrectionsInDerivativesAZ) {

					if (requestedAttributes.contains(GeoAttributes.DAZ_DLAT)) {
						double dazdlat = prediction.getAttribute(GeoAttributes.DAZ_DLAT);
						correction = libcorrPosAZ.getDerivLat()[0];
						if (Double.isNaN(correction))
							correction = NA_VALUE;
						prediction.setAttribute(GeoAttributes.AZIMUTH_PATH_CORR_DERIV_LAT, correction);
						if (dazdlat != NA_VALUE && correction != NA_VALUE)
							prediction.setAttribute(GeoAttributes.DAZ_DLAT, dazdlat + correction);
					}

					if (requestedAttributes.contains(GeoAttributes.DAZ_DLON)) {
						double dazdlon = prediction.getAttribute(GeoAttributes.DAZ_DLON);
						correction = libcorrPosAZ.getDerivLon()[0];
						if (Double.isNaN(correction))
							correction = NA_VALUE;
						prediction.setAttribute(GeoAttributes.AZIMUTH_PATH_CORR_DERIV_LON, correction);
						if (dazdlon != NA_VALUE && correction != NA_VALUE)
							prediction.setAttribute(GeoAttributes.DAZ_DLON, dazdlon + correction);
					}
				}
			}
		}

		Double uncertainty;
		
		EnumSet<GeoAttributes> keys = EnumSet.of(GeoAttributes.TT_MODEL_UNCERTAINTY, 
				GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
				GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY);

		// Iterate over keys  TT_MODEL_UNCERTAINTY, AZIMUTH_MODEL_UNCERTAINTY, SLOWNESS_MODEL_UNCERTAINTY.
		// If the model uncertainty is set, then apply the scale and offset provided by user, if any.
		// Also add another model uncertainty attributes specifying the type of model uncertainty applied.
		for (GeoAttributes key : keys) {
			uncertainty = prediction.getAttribute(key);
			if (uncertainty != null && uncertainty != Globals.NA_VALUE) {
				double[] scale = uncertaintyScale.get(key);
				uncertainty = uncertainty*scale[0]+scale[1];
				prediction.setAttribute(key, uncertainty);
				prediction.setAttribute(prediction.getUncertaintyType(key), uncertainty);
			}
		}

		// if azimuth in radians was computed, add azimuth in degrees as well
		uncertainty = prediction.getAttribute(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY);
		if (uncertainty != null && uncertainty != Globals.NA_VALUE) 
			prediction.setAttribute(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES, 
					toDegrees(uncertainty));

		// if slowness in sec/radian was computed, add slowness in sec/degree as well
		uncertainty = prediction.getAttribute(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY);
		if (uncertainty != null && uncertainty != Globals.NA_VALUE) 
			prediction.setAttribute(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES, 
					toRadians(uncertainty));
	}
}
