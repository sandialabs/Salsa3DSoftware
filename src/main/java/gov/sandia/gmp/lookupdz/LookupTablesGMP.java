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
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.io.GlobalInputStreamProvider;
import gov.sandia.gmp.util.io.InputStreamProvider.FileInputStreamProvider;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gmp.util.propertiesplus.Property;

/**
 * Implements travel time predictions using the 1D distance dependent travel time tables. 
 * LookupTablesGMP is thread-safe.
 * 
 * @author sballar
 *
 */
public class LookupTablesGMP extends Predictor { 

	/**
	 * map from [ directory or zipfile ] -> phase -> LookupTable
	 */
	private static final Map<File, EnumMap<SeismicPhase, LookupTable>> tableMap = new LinkedHashMap<>();

	/**
	 * map from [ directory or zipfile ] -> EllipticityCorrections object
	 */
	private static final Map<File, EllipticityCorrections> ellipticityCorrectionMap = new LinkedHashMap<>();

	/**
	 * Retrieve a map from SeismicPhase to LookupTable that is stored in the specified File. 
	 * <p>When a map is loaded into memory a reference to it is stored in a static map from 
	 * tableFile -> SeismicPhase -> LookupTable so that it will not have be loaded again.
	 * 
	 * <ol>There are 4 places that tableFile could reside:
	 * <li>if tableFile refers to a directory on the file system, then load lookupTables from the specified directory
	 * <li>if tableFile starts with "jar" and this library is running from a jar file, load the lookupTables from the root directory/tableFile.getName() of the jar.
	 * <li>if tableFile starts with "jar" and this library is running from an IDE like Eclipse load lookupTables from directory src/main/resources/tableFile.getName() on the file system
	 * <li>else, assume tableName refers to a zip file on the file system and load the lookupTables from there.
	 * </ol>
	 * @param tableFile
	 * @param logger if not null and logger.getVerbosity() > 0, some information about the load process is output to the logger.
	 * @return EnumMap<SeismicPhase, LookupTable>
	 * @throws IOException
	 */
	public static EnumMap<SeismicPhase, LookupTable> getLookupTables(File tableFile, ScreenWriterOutput logger) throws IOException {
		synchronized (tableMap) {
			EnumMap<SeismicPhase, LookupTable> lookupTableMap = tableMap.get(tableFile);
			if (lookupTableMap == null) {
				long timer = System.currentTimeMillis();
				lookupTableMap = new EnumMap<>(SeismicPhase.class);

				String modelName = tableFile.getName().replace(".zip", "");
				String phase = "-";

				FileInputStreamProvider fisp = GlobalInputStreamProvider.forFiles();			
				if (fisp.isDirectory(tableFile)) {
					// if tableName refers to a directory, read the lookupTable files from the directory
					for (File f : fisp.listFiles(tableFile)) if (f.isFile()) {
						// some files have names like 'ak135.P while others are simply P
						// To extract the phase from the file name, we have to ignore any path information
						// and keep only the extension if name includes the model name.
						int idx = f.getName().lastIndexOf('.');
						phase = idx >= 0 ? f.getName().substring(idx+1) : f.getName(); 
						try (InputStream inputStream = fisp.newStream(f)) {
							SeismicPhase seismicPhase = SeismicPhase.valueOf(phase);
							lookupTableMap.put(seismicPhase, new LookupTable(inputStream, modelName, seismicPhase));
						} catch (IllegalArgumentException e) {
							// phase is not supported by SeismicPhase.  It is likely that the File f
							// does not contain travel time information but something else.
						} 
					}
				}
				else {
					// boolean jar will be true if the user requested tt models from a jar file or from the 
					// /src/main/resources directory if running from an IDE.
					boolean jar = tableFile.toPath().getName(0).toString().toLowerCase().equals("jar");
					try (ZipInputStream zipInputStream = new ZipInputStream(
							jar ? LookupTablesGMP.class.getClassLoader().getResourceAsStream(tableFile.getName()) 
									: fisp.newStream(tableFile));) {
						ZipEntry zipEntry;
						while ((zipEntry = zipInputStream.getNextEntry()) != null) {
							// some entries have names like 'ak135/ak135.P while others are simply ak135/P
							// To extract the phase from the entry name, we have to ignore any path information
							// and keep only the extension if name includes the model name.
							String entryName = new File(zipEntry.getName()).getName(); // ignore path information
							int idx = entryName.lastIndexOf('.');
							phase = idx >= 0 ? entryName.substring(idx+1) : entryName;	// extract extension if name includes one.
							if (!zipEntry.isDirectory() && !phase.startsWith("_") && !phase.equals(modelName)) {
								try {
									SeismicPhase seismicPhase = SeismicPhase.valueOf(phase);
									lookupTableMap.put(seismicPhase, new LookupTable(zipInputStream, modelName, seismicPhase));
								} catch (IllegalArgumentException e) {
									// phase is not supported by SeismicPhase.  It is likely that the File f
									// does not contain travel time information but something else.
								} 
							}
							zipInputStream.closeEntry();
						}
					}
				}
				if (logger != null && logger.getVerbosity() > 0) 
					logger.writef("Loaded %3d tt models from %s in %s%n", lookupTableMap.size(), tableFile, Globals.elapsedTime(timer));
				tableMap.put(tableFile, lookupTableMap);
			}
			return lookupTableMap;
		}
	}

	public static EllipticityCorrections getEllipticityCorrections(File ellipFile, ScreenWriterOutput logger) throws Exception {
		synchronized (ellipticityCorrectionMap) {
			EllipticityCorrections ellipticityCorrections = ellipticityCorrectionMap.get(ellipFile);
			if (ellipticityCorrections == null)
				ellipticityCorrectionMap.put(ellipFile, ellipticityCorrections = new EllipticityCorrections(ellipFile, logger));
			return ellipticityCorrections;
		}
	}

	public void  close() throws Exception {
		super.close();
		tableMap.clear();
		ellipticityCorrectionMap.clear();
	}

	private EnumMap<SeismicPhase, LookupTable> lookupTables = null;

	private EnumSet<SeismicPhase> supportedPhases = null;

	private EllipticityCorrections ellipticityCorrections;

	/**
	 * Name of the supported model .
	 */
	private String modelName;

	/**
	 * Path to directory that contains all the lookup tables for the supported model
	 */
	private File tableDirectory;

	/**
	 * Path to directory that contains ellipticity corrections
	 */
	private File ellipticityDirectory;

	private boolean useEllipticityCorrections;
	private boolean useElevationCorrections;
	private double sedimentaryVelocityP;
	private double sedimentaryVelocityS;

	private long algorithmId = -1, modelId = -1;

	/**
	 * Extrapolation flag. Uses extrapolation if required and this flag is true.
	 */
	private boolean useExtrapolation;

	/**
	 * This is the set of GeoAttributes that LookupTablesGMP is capable of computing. The set of
	 * GeoAttributes that is actually computed during any call to LookupTablesGMP.getPrediction() or
	 * getPredictions() will depend on the set of requestetdAttributes that are submitted as part of
	 * the PredictionRequest object.
	 */
	public static final EnumSet<GeoAttributes> supportedAttributes = EnumSet.of(GeoAttributes.TRAVEL_TIME,
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
			GeoAttributes.DISTANCE_DEGREES,
			GeoAttributes.EXTRAPOLATED);

	public LookupTablesGMP(PropertiesPlus properties) throws Exception {
		this(properties, null);
	}

	public LookupTablesGMP(File tableDirectory, File ellipticityDirectory) throws Exception {
		lookupTables = getLookupTables(tableDirectory, null);
		
		if (ellipticityDirectory != null)
			ellipticityCorrections = getEllipticityCorrections(ellipticityDirectory, null);		
	}

	public LookupTablesGMP(PropertiesPlus properties, ScreenWriterOutput logger) throws Exception {
		super(properties, logger);

		predictionsPerTask = properties.getInt("lookup2dPredictionsPerTask", 500);

		tableDirectory = properties.getFile(PROP_TABLE_DIR, properties.getFile("lookup2dModelDirectory"));

		if (tableDirectory == null) {
			tableDirectory = new File("/jar/ak135");
		}

		tableDirectory = tableDirectory.getCanonicalFile();
		modelName = properties.getProperty(PROP_MODEL, tableDirectory.getName().replace(".zip", ""));

		lookupTables = getLookupTables(tableDirectory, logger);

		if (lookupTables.isEmpty())
			throw new Exception("Failed to read any travel time lookup tables from "+tableDirectory.getPath());
		supportedPhases = EnumSet.copyOf(lookupTables.keySet());

		useEllipticityCorrections = properties.getBoolean(PROP_USE_ELLIPTICITY_CORR, true);

		if (useEllipticityCorrections) {
			ellipticityDirectory = properties.getFile(PROP_ELLIPTICITY_CORR_DIR, new File("/jar/ellipticity_corrections"));

			if (ellipticityDirectory == null)
				throw new Exception("Must specify property "+PROP_ELLIPTICITY_CORR_DIR+" specifying directory or zip file containing ellipticity corrections");

			ellipticityDirectory = ellipticityDirectory.getCanonicalFile();
			ellipticityCorrections = getEllipticityCorrections(ellipticityDirectory, logger);
		}

		useElevationCorrections = properties.getBoolean(PROP_USE_ELEV_CORR, true);

		sedimentaryVelocityP = properties.getDouble(PROP_SEDIMENTARY_VELOCITY_P, 5.8);
		sedimentaryVelocityS = properties.getDouble(PROP_SEDIMENTARY_VELOCITY_S, 3.4);

		if (properties.containsKey("lookup2dSedimentaryVelocity"))
			throw new Exception("Property 'lookup2dSedimentaryVelocity' is no longer valid. \n"
					+ "Specify either lookup2dSedimentaryVelocityP (defaults to 5.8 km/s) \n"
					+ "or lookup2dSedimentaryVelocityS (defaults to 3.4 km/s)");

		//		String property = properties.getProperty("lookup2dOmitEllipticityCorrectionsByPhase");
		//		if (property != null) {
		//			String[] list = property.replaceAll(",", " ").split("\\s+");
		//			for (String p : list)
		//				omitEllipticityCorrectionsByPhase.add(SeismicPhase.valueOf(p));
		//		}

		useExtrapolation = properties.getBoolean("lookup2dUseExtrapolation", false);

		if (logger != null && logger.getVerbosity() > 0)
			logger.writef("lookup2d Predictor instantiated in %s%n", Globals.elapsedTime(constructorTimer));

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

			LookupTable table = lookupTables.get(request.getPhase());

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
			
//			messages.put( 0, "");
//			messages.put(-1, "Single depth sampling exists, but requested depth is not the same as that in the table, or problems are encountered while doing rational function extrapolation (via function, ratint()).");
//			messages.put(-2, "Insufficient valid samples exist for a meaningful traveltime calculation.");
//			messages.put(11, "Extrapolated point in hole of curve");
//			messages.put(12, "Extrapolated point < first distance");
//			messages.put(13, "Extrapolated point > last distance");
//			messages.put(14, "Extrapolated point < first depth");
//			messages.put(15, "Extrapolated point > last depth");
//			messages.put(16, "Extrapolated point < first distance and < first depth");
//			messages.put(17, "Extrapolated point > last distance and < first depth");
//			messages.put(18, "Extrapolated point < first distance and > last depth");
//			messages.put(19, "Extrapolated point > last distance and > last depth");

			if (code < 0 || (code > 0 && !useExtrapolation) || Double.isNaN(predictions[0]))
				return new Prediction(request, this, table.getErrorMessage(code, xDeg, depth));
			
			prediction.setAttributeBoolean(GeoAttributes.EXTRAPOLATED, code > 0);

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

			if (useEllipticityCorrections && ellipticityCorrections.isSupported(request.getPhase())) {
				double ellipCorr =	ellipticityCorrections.getEllipCorr(request.getPhase(), request.getReceiver(), request.getSource());
				prediction.setAttribute(GeoAttributes.TT_ELLIPTICITY_CORRECTION, ellipCorr);
				travelTime += ellipCorr;
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
	public double getElevationCorrection(double elevation, double slowness, double sedimentaryVelocity) {
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
	 * Retrieve a new, invalid Prediction object whose error message is set to the error message and
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

	public EllipticityCorrections getEllipticityCorrections() { 
		return ellipticityCorrections; }
	
	public EnumMap<SeismicPhase, LookupTable> getLookupTables() {
		return lookupTables;
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
