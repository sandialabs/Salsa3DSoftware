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
package gov.sandia.gmp.lookupdz;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.sandia.gmp.baseobjects.EllipticityCorrections;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.globals.WaveType;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.seismicbasedata.SeismicBaseData;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gmp.util.propertiesplus.Property;

/**
 * Implements travel time predictions using the 1D distance dependent travel time tables in seismic
 * base data. LookupTablesGMP is thread-safe.
 * 
 * @author sballar
 *
 */
public class LookupTablesGMP extends Predictor { //implements UncertaintyInterface {
	private static final Map<File, Map<SeismicPhase, LookupTable>> tableMap = new LinkedHashMap<>();

	private static final Map<File, EllipticityCorrections> ellip = new LinkedHashMap<>();

	private static final Map<File, Map<String, EnumMap<SeismicPhase, Boolean>>> phaseFileExists =
			new HashMap<>();

	public static Map<SeismicPhase, LookupTable> getLookupTable(File tableFile) throws IOException {
		synchronized (tableMap) {
			if (tableMap.containsKey(tableFile))
				return tableMap.get(tableFile);

			Map<SeismicPhase, LookupTable> m = new LinkedHashMap<>();
			tableMap.put(tableFile, m);
			return m;
		}
	}

	public void  close() throws Exception {
		super.close();
		tableMap.clear();
		ellip.clear();
		phaseFileExists.clear();
	}

	public static EllipticityCorrections getEllipticityCorrections(File ellipDir) throws IOException {
		synchronized (ellip) {
			if (ellip.containsKey(ellipDir))
				return ellip.get(ellipDir);

			EllipticityCorrections ec = new EllipticityCorrections(ellipDir);
			ellip.put(ellipDir, ec);
			return ec;
		}
	}

	/**
	 * Name of the supported model .
	 */
	private String modelName;

	/**
	 * Path to directory that contains all the lookup tables for the supported model
	 */
	private final File tableDirectory;

	/**
	 * Path to directory that contains ellipticity corrections
	 */
	private final File ellipticityDirectory;

	private final boolean useEllipticityCorrections;
	private final boolean useElevationCorrections;
	private final double sedimentaryVelocityP;
	private final double sedimentaryVelocityS;
	private final EnumSet<SeismicPhase> omitEllipticityCorrectionsByPhase = EnumSet.noneOf(SeismicPhase.class);

	private long algorithmId = -1, modelId = -1;

	private final boolean fileNamesIncludeModelName;

	/**
	 * Extrapolation flag. Uses extrapolation if required and this flag is true.
	 */
	private final boolean useExtrapolation;

	/**
	 * This is the set of GeoAttributes that LookupTablesGMP is capable of computing. The set of
	 * GeoAttributes that is actually computed during any call to LookupTablesGMP.getPrediction() or
	 * getPredictions() will depend on the set of requestetdAttributes that are submitted as part of
	 * the PredictionRequest object.
	 */
	public final EnumSet<GeoAttributes> supportedAttributes = EnumSet.of(GeoAttributes.TRAVEL_TIME,
			GeoAttributes.TT_BASEMODEL, GeoAttributes.TT_MODEL_UNCERTAINTY,
			GeoAttributes.TT_PATH_CORRECTION, GeoAttributes.TT_PATH_CORR_DERIV_HORIZONTAL,
			GeoAttributes.TT_PATH_CORR_DERIV_RADIAL, GeoAttributes.TT_ELLIPTICITY_CORRECTION,
			GeoAttributes.TT_ELEVATION_CORRECTION, GeoAttributes.TT_ELEVATION_CORRECTION_SOURCE,
			GeoAttributes.DTT_DLAT, GeoAttributes.DTT_DLON, GeoAttributes.DTT_DR, GeoAttributes.DTT_DTIME,
			GeoAttributes.AZIMUTH, GeoAttributes.AZIMUTH_DEGREES, GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
			GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES,
			GeoAttributes.AZIMUTH_PATH_CORR_DERIV_HORIZONTAL,
			GeoAttributes.AZIMUTH_PATH_CORR_DERIV_RADIAL, GeoAttributes.DAZ_DLAT, GeoAttributes.DAZ_DLON,
			GeoAttributes.DAZ_DR, GeoAttributes.DAZ_DTIME, GeoAttributes.SLOWNESS,
			GeoAttributes.SLOWNESS_DEGREES, GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY,
			GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES,
			GeoAttributes.SLOWNESS_PATH_CORR_DERIV_HORIZONTAL,
			GeoAttributes.SLOWNESS_PATH_CORR_DERIV_RADIAL, GeoAttributes.DSH_DLAT, GeoAttributes.DSH_DLON,
			GeoAttributes.DSH_DR, GeoAttributes.DSH_DTIME, GeoAttributes.BACKAZIMUTH,
			GeoAttributes.OUT_OF_PLANE, GeoAttributes.CALCULATION_TIME, GeoAttributes.DISTANCE,
			GeoAttributes.DISTANCE_DEGREES);

	/**
	 * Phases that this Predictor can support. All phases.
	 */
	protected final EnumSet<SeismicPhase> supportedPhases;

	public LookupTablesGMP(PropertiesPlus properties) throws Exception {
		this(properties, null);
	}

	public LookupTablesGMP(PropertiesPlus properties, ScreenWriterOutput logger) throws Exception {
		super(properties, logger);

		predictionsPerTask = properties.getInt("lookup2dPredictionsPerTask", 500);

		// must determine tableDir, ellipDir and modelName.
		// - if properties 'lookup2dTableDirectory' and 'lookup2dEllipticityCorrectionsDirectory' are specified 
		//   (no defaults) then modelName will default tableDir.getName() but can be overridden with property 
		//   'lookup2dModel'.
		// - Otherwise, modelFile will be set to value of property 'lookup2dModel', defaults to 'ak135'.
		//   File seismicBaseData will be set to value of property 'seismicBaseData'
		//   (defaults to seismic-base-data.jar).  tableDir will become seismicBaseData/tt/modelName
		//   and ellipDir will become seismicBaseData/el/modelName.
		//   If seismicBaseData == seismic-base-data.jar, travel times and ellipticity corrections will
		//   come from the application jar file.

		File tableDir = properties.getFile(PROP_TABLE_DIR, properties.getFile("lookup2dModelDirectory"));

		File ellipDir = properties.getFile(PROP_ELLIPTICITY_CORR_DIR);

		if (tableDir != null && ellipDir != null) {
			modelName = properties.getProperty(PROP_MODEL, tableDir.getName());
		}
		else {
			File seismicBaseData =
					properties.getFile(PROP_SEISMIC_BASE_DATA, new File("seismic-base-data.jar"));

			modelName = properties.getProperty(PROP_MODEL, "ak135");
			tableDir = new File(new File(seismicBaseData, "tt"), modelName);
			ellipDir = new File(new File(seismicBaseData, "el"), "ak135");
		}

		this.tableDirectory = tableDir;
		this.ellipticityDirectory = ellipDir;
		fileNamesIncludeModelName = new File(tableDirectory, modelName + ".P").exists()
				|| new File(tableDirectory, modelName + ".Pn").exists()
				|| new File(tableDirectory, modelName + ".Pg").exists()
				|| new File(tableDirectory, modelName + ".S").exists()
				|| new File(tableDirectory, modelName + ".Sn").exists()
				|| new File(tableDirectory, modelName + ".Lg").exists();

		//Begin phase file caching:
		//If many LookupTableGMP instances are created, then all seismic phases must be checked prior
		//to finishing construction in order to support the getSupportedPhases() method. This can be a
		//very long time, so we cache the existence of these files inside "phaseFileExists" here:
		Map<String, EnumMap<SeismicPhase, Boolean>> m1 = null;
		synchronized (phaseFileExists) {
			m1 = phaseFileExists.computeIfAbsent(tableDirectory, k -> new HashMap<>());
		}

		EnumMap<SeismicPhase, Boolean> m2 = null;
		synchronized (m1) {
			m2 = m1.computeIfAbsent(modelName, k -> new EnumMap<>(SeismicPhase.class));
		}

		supportedPhases = EnumSet.noneOf(SeismicPhase.class);
		for (SeismicPhase phase : EnumSet.allOf(SeismicPhase.class)) {
			Boolean exists = null;

			synchronized (m2) {
				exists = m2.get(phase);

				if (exists == null) {
					exists = new SeismicBaseData(getFile(phase)).exists();
					m2.put(phase, exists);
				}
			}

			if (exists)
				supportedPhases.add(phase);
			//End supported phase caching.
		}

		useElevationCorrections = properties.getBoolean(PROP_USE_ELEV_CORR, true);

		sedimentaryVelocityP = properties.getDouble(PROP_SEDIMENTARY_VELOCITY_P, 5.8);
		sedimentaryVelocityS = properties.getDouble(PROP_SEDIMENTARY_VELOCITY_S, 3.4);

		if (properties.containsKey("lookup2dSedimentaryVelocity"))
			throw new Exception("Property 'lookup2dSedimentaryVelocity' is no longer valid. \n"
					+ "Specify either lookup2dSedimentaryVelocityP (defaults to 5.8 km/s) \n"
					+ "or lookup2dSedimentaryVelocityS (defaults to 3.4 km/s)");

		useEllipticityCorrections = properties.getBoolean(PROP_USE_ELLIPTICITY_CORR, true);

		String property = properties.getProperty("lookup2dOmitEllipticityCorrectionsByPhase");
		if (property != null) {
			String[] list = property.replaceAll(",", " ").split("\\s+");
			for (String p : list)
				omitEllipticityCorrectionsByPhase.add(SeismicPhase.valueOf(p));
		}

		useExtrapolation = properties.getBoolean("lookup2dUseExtrapolation", false);

		if (logger != null && logger.getVerbosity() > 0)
			logger.writef("lookup2d Predictor instantiated in %s%n", Globals.elapsedTime(constructorTimer));

	}

	private File getFile(SeismicPhase phase) throws FileNotFoundException {
		if (fileNamesIncludeModelName)
			return new File(tableDirectory, modelName + "." + phase.getFileName());

		return new File(tableDirectory, phase.getFileName());
	}

	public LookupTable getTable(SeismicPhase phase) throws Exception {
		synchronized (tableMap) {
			Map<SeismicPhase, LookupTable> tables = getLookupTable(tableDirectory);

			if (tables.containsKey(phase))
				return tables.get(phase);

			LookupTable t = new LookupTable(getFile(phase), modelName);
			tables.put(phase, t);
			return t;
		}
	}

	@Override
	public String getPredictorName() {
		return "lookup2d";
	}

	public Object getEarthModel() {
		return tableMap;
	}

	@Override
	public String getModelName() {
		return modelName;
	}

	@Override
	public String getModelName(int maxLength) {
		return modelName.length() <= maxLength ? modelName : modelName.substring(0, maxLength);
	}

	@Override
	public String getModelDescription() throws GMPException {
		return modelName;
	}

	public boolean isSupported(Receiver receiver, SeismicPhase phase, GeoAttributes attribute,
			double epochTime) {
		return supportedAttributes.contains(attribute) && getSupportedPhases().contains(phase);
	}

	@Override
	public void setAlgorithmId(long algorithmId) {
		this.algorithmId = algorithmId;
	}

	@Override
	public long getAlgorithmId() {
		return algorithmId;
	}

	@Override
	public void setModelId(long modelId) {
		this.modelId = modelId;
	}

	@Override
	public long getModelId() {
		return modelId;
	}

	@Override
	public File getModelFile() {
		return tableDirectory;
	}

	public File getModelFile(SeismicPhase phase) throws Exception {
		LookupTable tbl = getTable(phase);
		return tbl == null ? null : tbl.getFile();
	}

	public double getSurfaceRadius(GeoVector position) throws GMPException {
		return 6371.;
	}

	public double getSurfaceDepth(GeoVector position) throws GMPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Prediction getPrediction(PredictionRequest request) throws Exception {

		if (!request.isDefining())
			return new Prediction(request, this, "PredictionRequest submitted to LookuTablesGMP was non-defining");

		long timer = System.currentTimeMillis();

		Prediction prediction = new Prediction(request, PredictorType.LOOKUP2D);

		try {

			LookupTable table = getTable(request.getPhase());

			if (table == null)
				return new Prediction(request, this,
						String.format("Phase %s is not supported.", request.getPhase().toString()));

			double xDeg = request.getDistanceDegrees();
			double depth = Math.max(request.getSource().getDepth(), 0.);

			// deal with roundoff errors that prevent valid depths from being processed.
			if (depth > 700. && depth < 700.01)
				depth = 700.;

			int code;
			double[] predictions = new double[6];

			if ((request.getPhase() == SeismicPhase.Pg || request.getPhase() == SeismicPhase.Lg)
					&& xDeg > table.getMaxDistance() && useExtrapolation) {
				// Special case; Pg or Lg is being extrapolated past the end of the table in distance
				// direction.  This sometimes fails because the bi-cubic spline calculation produces
				// NaNs.  Instead, compute travel time and slowness at the maxdistance and depth=0.
				// Set travel time = travel time + (distance-maxDistance)*slowness.
				// Assume dtt_dz and all second derivatives are zero.
				table.interpolate(table.getMaxDistance(), 0., false, false, false, predictions);
				predictions[0] += (xDeg - table.getMaxDistance()) * predictions[1];
				predictions[2] = predictions[3] = predictions[4] = predictions[5] = 0.;
				code = depth <= table.getMaxDepth() ? 13 : 15;
			}
			else 
			{
				code = table.interpolate(xDeg, depth,
						request.getRequestedAttributes().contains(GeoAttributes.DTT_DR),
						request.getRequestedAttributes().contains(GeoAttributes.DSH_DR), useExtrapolation,
						predictions);
			}

			if (code < 0 || (code > 0 && !useExtrapolation) || Double.isNaN(predictions[0]))
				return new Prediction(request, this, table.getErrorMessage(code, xDeg, depth));

			// elements of predictions array:
			// 0: tt (sec)
			// 1: dtdx (sec/degree)
			// 2: d2tdx2 (sec/degree^2)
			// 3: dtdz (sec/km)
			// 4: d2tdz2 (sec/km^2)
			// 5: d2tdxdz (sec/(km.degree))
			// all might be NaN.

			double travelTime = predictions[0];

			prediction.setAttribute(GeoAttributes.TT_BASEMODEL, travelTime);

			double slowness = toDegrees(predictions[1]); // sec/radian; might be NaN?

			prediction.setAttribute(GeoAttributes.SLOWNESS_BASEMODEL, slowness);


			if (useEllipticityCorrections && !omitEllipticityCorrectionsByPhase.contains(request.getPhase())) {
				EllipticityCorrections ellip = getEllipticityCorrections(ellipticityDirectory);

				if (request.getPhase().getWaveType() == WaveType.I) {
					try {
						double ellipCorr =	ellip.getEllipCorr(request.getPhase(), request.getReceiver(), request.getSource());
						prediction.setAttribute(GeoAttributes.TT_ELLIPTICITY_CORRECTION, ellipCorr);
						travelTime += ellipCorr;
					} catch (Exception e) {
						// if no ellipticity correction available for phase I, ignore the error and move on.
					}
				}
				else {
					// for phases other than I, an exception will be thrown if ellipticity correction not available.
					double ellipCorr =	ellip.getEllipCorr(request.getPhase(), request.getReceiver(), request.getSource());
					prediction.setAttribute(GeoAttributes.TT_ELLIPTICITY_CORRECTION, ellipCorr);
					travelTime += ellipCorr;
				}
			}

			// no elevation corrections for infrasound phases
			if (useElevationCorrections && request.getPhase().getWaveType() != WaveType.I) {
				double sedVel;
				if (request.getPhase().getWaveTypeReceiver() == WaveType.P)
					sedVel = sedimentaryVelocityP;
				else if (request.getPhase().getWaveTypeReceiver() == WaveType.S)
					sedVel = sedimentaryVelocityS;
				else
					throw new  Exception (String.format(
							"Unable to compute elevation correction because wavetype is neither P nor S.%n"
									+ "%s", request.getString()));

				// find the elevation correction for the receiver
				double elevCorr =
						getElevationCorrection(-request.getReceiver().getDepth(), slowness, sedVel);
				prediction.setAttribute(GeoAttributes.TT_ELEVATION_CORRECTION, elevCorr);
				prediction.setAttribute(GeoAttributes.SEDIMENTARY_VELOCITY_RECEIVER,
						Double.isNaN(sedVel) ? Globals.NA_VALUE : sedVel);

				// if the source is above the surface of the earth, then
				// find an elevation correction for the source.
				if (request.getPhase().getWaveTypeSource() == WaveType.P)
					sedVel = sedimentaryVelocityP;
				else if (request.getPhase().getWaveTypeSource() == WaveType.S)
					sedVel = sedimentaryVelocityS;
				else
					throw new  Exception (String.format(
							"Unable to compute source elevation correction because wavetype is neither P nor S.%n"
									+ "%s", request.getString()));

				double srcElev = -request.getSource().getDepth();
				double srcElevCorr = srcElev <= 0. ? 0 : getElevationCorrection(srcElev, slowness, sedVel);
				prediction.setAttribute(GeoAttributes.TT_ELEVATION_CORRECTION_SOURCE, srcElevCorr);
				prediction.setAttribute(GeoAttributes.SEDIMENTARY_VELOCITY_SOURCE,
						Double.isNaN(sedVel) ? Globals.NA_VALUE : sedVel);

				travelTime += elevCorr + srcElevCorr;
			}

			if (request.getRequestedAttributes().contains(GeoAttributes.TT_MODEL_UNCERTAINTY)) {
				prediction.setAttribute(GeoAttributes.TT_MODEL_UNCERTAINTY, table.interpolateUncertainty(xDeg, depth));
				prediction.putUncertaintyType(GeoAttributes.TT_MODEL_UNCERTAINTY, 
						GeoAttributes.TT_MODEL_UNCERTAINTY_DISTANCE_DEPENDENT);
			}

			prediction.setRayType(RayType.REFRACTION);

			// 2: d2tdx2 (sec/degree^2)
			// 3: dtdz (sec/km)
			// 4: d2tdz2 (sec/km^2)
			// 5: d2tdxdz (sec/(km.degree))

			// recall that to convert slowness from sec/deg to sec/radian, call toDegrees()
			setGeoAttributes(prediction, travelTime, request.getSeaz(), slowness, -predictions[3],
					toDegrees(toDegrees(predictions[2])), -toDegrees(predictions[5]));

		} catch (Exception e) {
			prediction = new Prediction(request, this, (e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
		}

		if (request.getRequestedAttributes().contains(GeoAttributes.CALCULATION_TIME))
			prediction.setAttribute(GeoAttributes.CALCULATION_TIME,
					(System.currentTimeMillis() - timer) * 1e-3);

		return prediction;
	}

	/**
	 * Retrieve the elevation correction for a given elevation above sea level (in km) and horizontal
	 * slowness (in sec/radian). The correction is elevation/sedimentaryVelocity *
	 * cos(incidence_angle).
	 * 
	 * @param elevation above sea level, in km
	 * @param slowness horizontal slowness in sec/radian
	 * @return elevation correction in sec.
	 */
	public double getElevationCorrection(double elevation, double slowness,
			double sedimentaryVelocity) {
		double el = slowness * sedimentaryVelocity / 6371.;
		if (el > 1.0)
			el = 1.0 / el;
		return elevation / sedimentaryVelocity * sqrt(1. - min(1.0, pow(el, 2)));
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

	/**
	 * Retrieve the code version and timestamp
	 * 
	 * @return code version
	 */
	static public String getVersion() {
		return Utils.getVersion("lookup-tables-dz");
	}

	@Override
	public String getPredictorVersion() {
		return getVersion();
	}

	@Override
	public PredictorType getPredictorType() {
		return PredictorType.LOOKUP2D;
	}

	@Override
	public EnumSet<GeoAttributes> getSupportedAttributes() {
		return supportedAttributes;
	}

	@Override
	public EnumSet<SeismicPhase> getSupportedPhases() {
		return supportedPhases;
	}

	@Property(type = File.class)
	public static final String PROP_MODEL = "lookup2dModel";
	@Property(type = File.class)
	public static final String PROP_TABLE_DIR = "lookup2dTableDirectory";
	@Property(type = File.class)
	public static final String PROP_ELLIPTICITY_CORR_DIR = "lookup2dEllipticityCorrectionsDirectory";
	@Property(type = File.class)
	public static final String PROP_SEISMIC_BASE_DATA = "seismicBaseData";
	@Property(type = Boolean.class)
	public static final String PROP_USE_ELEV_CORR = "lookup2dUseElevationCorrections";
	@Property(type = Double.class)
	public static final String PROP_SEDIMENTARY_VELOCITY_P = "lookup2dSedimentaryVelocityP";
	@Property(type = Double.class)
	public static final String PROP_SEDIMENTARY_VELOCITY_S = "lookup2dSedimentaryVelocityS";
	@Property(type = Boolean.class)
	public static final String PROP_USE_ELLIPTICITY_CORR = "lookup2dUseEllipticityCorrections";
	@Property
	public static final String PROP_UNCERTAINTY_TYPE = "lookup2dTTUncertaintyType";




}
