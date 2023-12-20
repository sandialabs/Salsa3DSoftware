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
package gov.sandia.gmp.ak135rays;

import static gov.sandia.gmp.util.globals.Globals.NL;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.awt.image.LookupTable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.TauP.TauP_Pierce;
import edu.sc.seis.TauP.TimeDist;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.extensions.siteterms.GeoTessModelSiteData;
import gov.sandia.gmp.baseobjects.EllipticityCorrections;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.globals.WaveType;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintyDistanceDependent;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintyNAValue;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.FileDirHandler;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gmp.util.propertiesplus.Property;

/**
 * Implements a wrapper around the TaupToolkit software of Crotwell, et. al.
 * Based on original class taup-toolkit-wrapper by sballar. Note that because
 * this wrapper does not calculate slowness derivatives, Locoo3D cannot use
 * it to determine locations based on slowness.
 * <ul>
 * <li>Libcorr3d corrections applied if ak135raysPathCorrectionsType = libcorr3d </li>
 * <li>Ellipticity corrections are applied if property
 * tauptoolkitEllipticityCorrectionsDirectory is specified and
 * GeoAttributes.TT_ELLIPTICITY_CORRECTION is one of the requested attributes in
 * a PredictionRequest.</li>
 * <li>Applies an elevation correction for both positive and negative receiver
 * elevations.</li>
 * <li>Applies an elevation correction for positive source elevation.</li>
 * </ul>
 * <p>
 * @author acconle
 * 
 */
public class AK135Rays extends Predictor {
	
	//Class AK135Rays extends the Predictor class in baseobjects. It implements UncertaintyInterface
	//from baseobjects in order to apply 1D distance dependence uncertainties to travel-time predictions.
	
	private static final Map<File, EllipticityCorrections> ellip = new LinkedHashMap<>();
	
	public static EllipticityCorrections getEllipticityCorrections(File ellipDir) throws IOException{
		synchronized (ellip) {
			if (ellip.containsKey(ellipDir))
				return ellip.get(ellipDir);
			
			EllipticityCorrections ec = new EllipticityCorrections(ellipDir);
			ellip.put(ellipDir, ec);
			return ec;
		}
	}
	
	private static final Map<File, Map<SeismicPhase, LookupTable>> tableMap = new LinkedHashMap<>();
	
	  public static Map<SeismicPhase, LookupTable> getLookupTable(File tableFile) throws IOException {
		    synchronized (tableMap) {
		      if (tableMap.containsKey(tableFile))
		        return tableMap.get(tableFile);

		      Map<SeismicPhase, LookupTable> m = new LinkedHashMap<>();
		      tableMap.put(tableFile, m);
		      return m;
		    }
		  }
	
	/**
	* The set of properties from which the tomography takes its input values.
	*/
    private PropertiesPlusGMP  aProps = null;
    
    /**
     * The primary directory into which all IO from this GeoTomography object is
     * read and written.
     */
    private String aIODirectory = "";

    
    /**
     * Output object used to write to the screen or a BufferedWriter log file.
     */
    private ScreenWriterOutput aScrnWrtr = new ScreenWriterOutput();
    
    /**
     * Implements a fixed ak135 ray predictor based on TaupToolkit
     * @throws GeoTomoException
     * @throws GeoTessException Default constructor
     * @throws IOException
     * @throws
     */
    
	/**Name of taupToolKitModel. For ak135 raypaths this will be 'ak135.tvel'
	 * By making this a class variable, this Predictor is not thread safe. */
	private String taupToolKitModel;
	
	/**
	 * The TaupToolkit model (this version should include moho, cmb, icb depths based on pierce points.  
	 * For ak135 raypaths this will be 'ak135.tvel'.
	 * By making this a class variable, this Predictor is not thread safe.
	 */
	private TauP_Pierce taupPierce;
	
	/**Relevant depths based on pierce points*/
	private double mohoDepth;
	
	private double cmbDepth;
	
	private double icbDepth;
	
	/**Relevant velocities*/
	private double PgVelocity;
	private double sedimentaryVelocityP; //km/sec, used for elevation correction
	private double sedimentaryVelocityS; //km/sec, used for elevation correction
	
	/**Set whether to use corrections*/
	private final boolean useElevationCorrections;
	private final boolean useEllipticityCorrections;
	
	/**
	 * Path to directory that contains ellipticity corrections
	 */
	private final File ellipticityDirectory;

	/**
	 * The 3D model through which we want to compute travel times using a path integral
	 * along an ak135 raypath.
	 */
	private GeoTessModel model3d;
	
	/**
	 * Path to directory that contains all the lookup tables for the supported model
	*/
	private final File tableDirectory;
	
	private final boolean fileNamesIncludeModelName;
	
	/**
	 * Name of the supported model.
	 */
	private final String modelName;

	/**
	 * Phases that this Predictor can support.  All phases.
	 */
	protected final EnumSet<SeismicPhase> supportedPhases = EnumSet.allOf(SeismicPhase.class);
	
	private static String version = null;
	
	private long algorithmId = -1, modelId = -1;
	
	/**
	 * This is the set of GeoAttributes that AK135Rays is currently capable of
	 * computing. The set of GeoAttributes that is actually computed during any
	 * call to getPredictions() will depend
	 * on the set of requestetdAttributes that are submitted as part of the
	 * PredictionRequest object. Currently, only TRAVEL_TIME is supported.
	 */
	public final EnumSet<GeoAttributes> supportedAttributes = EnumSet.of(GeoAttributes.TRAVEL_TIME);

	
    //TODO: Ask Sandy about throwing a PropertiesPlusException, whether a default constructor is needed
    public AK135Rays(String propertiesFileName) throws Exception {
        // load the properties file

        System.out.println(NL + "Starting AK135Rays ..." + NL +
                "Reading AK135Rays properties from file: " +
                propertiesFileName);
        aProps = new PropertiesPlusGMP();
        aProps.load(new FileInputStream(propertiesFileName));
		
		modelName = properties.getProperty(PROP_MODEL, "ak135");

		File tableDir = properties.getFile(PROP_TABLE_DIR);
		
		this.tableDirectory = tableDir;
		fileNamesIncludeModelName = new File(tableDirectory, modelName + ".P").exists();
        
        useEllipticityCorrections = aProps.getBoolean(PROP_USE_ELLIPTICITY_CORR, true);
        useElevationCorrections = aProps.getBoolean(PROP_USE_ELEV_CORR, true);
        
		File ellipDir = aProps.getFile(PROP_ELLIPTICITY_CORR_DIR);
		
		if (ellipDir == null) {
			File seismicBaseData = properties.getFile(PROP_SEISMIC_BASE_DATA, new File("seismic-base-data.jar"));
			
			ellipDir = new File(new File(seismicBaseData, "el"),"ak135");
		}
        
        this.ellipticityDirectory = ellipDir;
                
        // make the io directory;
        createIODirectory(propertiesFileName);

	String type = properties.getProperty(PROP_UNCERTAINTY_TYPE, "DistanceDependent").replaceAll("_", "");
	if (type.equalsIgnoreCase("DistanceDependent"))
		super.getUncertaintyInterface().put(GeoAttributes.TRAVEL_TIME,  
			new UncertaintyDistanceDependent(properties, "ak135-rays"));
	else
	    super.getUncertaintyInterface().put(GeoAttributes.TRAVEL_TIME, new UncertaintyNAValue());
	
}

	/**
	 * Parameterized constructor loads the tauptoolkit model and the 3D model into memory
	 * @param properties
	 * @throws Exception
	 */
	public AK135Rays(PropertiesPlus properties) throws Exception {
		
		// Goes up to the super class, Predictor. Predictor checks if there are empirical corrections
		// requested in the properties file.
		super(properties);
		
		//Initialize whether to use ellipticity and/or elevation corrections and sedimentary velocity values
		
		useElevationCorrections = properties.getBoolean(PROP_USE_ELEV_CORR, true);
		
		sedimentaryVelocityP = properties.getDouble(PROP_SEDIMENTARY_VELOCITY, properties.getDouble(PROP_SEDIMENTARY_VELOCITY, 5.8));
		
		sedimentaryVelocityS = properties.getDouble(PROP_SEDIMENTARY_VELOCITY, properties.getDouble(PROP_SEDIMENTARY_VELOCITY, 3.4));
		
		useEllipticityCorrections = properties.getBoolean(PROP_USE_ELLIPTICITY_CORR, true);
		
		File ellipDir = properties.getFile(PROP_ELLIPTICITY_CORR_DIR);
		
		//If no specific ellipticity directory is defined in the properties file, use ellipticity corrections in seismicBaseData
		if (ellipDir == null) {
			File seismicBaseData = properties.getFile(PROP_SEISMIC_BASE_DATA, new File("seismic-base-data.jar"));
			
			ellipDir = new File(new File(seismicBaseData, "el"),"ak135");
		}
		
		this.ellipticityDirectory = ellipDir;
		
		modelName = properties.getProperty(PROP_MODEL, "ak135");
		
		File tableDir = properties.getFile(PROP_TABLE_DIR);
		this.tableDirectory = tableDir;
		
      	if (tableDir == null) {
    	  File seismicBaseData = properties.getFile(PROP_SEISMIC_BASE_DATA, new File("seismic-base-data.jar"));
    	  
    	  tableDir = new File(new File(seismicBaseData, "tt"), modelName);
      	}
      	
      	fileNamesIncludeModelName = new File(tableDirectory, modelName + ".P").exists();
      	
		
		// not necessary to specify this property in the properties object since ak135.tvel is the default
		
		taupToolKitModel = properties.getProperty("taupToolKitModel","ak135.tvel");

		File modelFile = properties.getFile("ak135raysModel");
		
		// if user specified a model directory that contains a file called prediction_model.geotess
		// load the prediction model.  Otherwise load the model from the user specified file.
		// Needs to be made a GeoTessModelSiteData type, otherwise will not work in predictor factory
		if (modelFile.isDirectory() && new File(modelFile, "prediction_model.geotess").exists())
			model3d = new GeoTessModelSiteData(new File(modelFile, "prediction_model.geotess"));
		else
			model3d = new GeoTessModelSiteData(modelFile);
		
		model3d.setEarthShape(VectorGeo.getEarthShape());
				
		try
		{
			taupPierce = new TauP_Pierce(taupToolKitModel);

			// find depth to moho (via PvmP phase pierce point) 
			// depthCorrect method is deprecated, but still functional
			taupPierce.parsePhaseList("PvmP");
			taupPierce.depthCorrect(0.);
			taupPierce.calculate(2.);
			double z = 0.;
			for (TimeDist piercePoint : taupPierce.getArrival(0).getPierce())
				if (piercePoint.getDepth() > z)
					z = piercePoint.getDepth();
			mohoDepth = z;
			//System.out.println("TaupToolkitWrapper mohoDepth = "+mohoDepth);

			// find depth to cmb (via PcP phase pierce point)
			// depthCorrect method is deprecated, but still functional
			taupPierce.clearArrivals();
			taupPierce.clearPhaseNames();
			taupPierce.parsePhaseList("PcP");
			taupPierce.depthCorrect(0.);
			taupPierce.calculate(20.);
			z = 0.;
			for (TimeDist piercePoint : taupPierce.getArrival(0).getPierce())
				if (piercePoint.getDepth() > z)
					z = piercePoint.getDepth();
			cmbDepth = z;
			//System.out.println("TaupToolkitWrapper cmbDepth = "+cmbDepth);

			// find depth to icb (via PKiKP phase pierce point)
			// depthCorrect method is deprecated, but still functional
			taupPierce.clearArrivals();
			taupPierce.clearPhaseNames();
			taupPierce.parsePhaseList("PKiKP");
			taupPierce.depthCorrect(0.);
			taupPierce.calculate(60.);
			z = 0.;
			for (TimeDist piercePoint : taupPierce.getArrival(0).getPierce())
				if (piercePoint.getDepth() > z)
					z = piercePoint.getDepth();
			icbDepth = z;
			//System.out.println("TaupToolkitWrapper icbDepth = "+icbDepth);

		}
		catch (TauModelException e)
		{
			throw new GMPException(e);
		}

		String type = properties.getProperty(PROP_UNCERTAINTY_TYPE, "DistanceDependent").replaceAll("_", "");
		if (type.equalsIgnoreCase("DistanceDependent"))
			super.getUncertaintyInterface().put(GeoAttributes.TRAVEL_TIME,  
				new UncertaintyDistanceDependent(properties, "ak135-rays"));
		else
		    super.getUncertaintyInterface().put(GeoAttributes.TRAVEL_TIME, new UncertaintyNAValue());
		
		// these are phases that are currently known to be unsupported. More
		// can be
		// added to this set with property tauptoolkitUnsupportedPhases
		// EnumSet<SeismicPhase> unsupportedPhases = EnumSet.of(
		// SeismicPhase.T,
		// SeismicPhase.LR,
		// SeismicPhase.H,
		// SeismicPhase.PKP2
		// );
		//
		// // add all phases that end with 'ab', 'bc' or 'df' to
		// unsupportedPhases,
		// // except keep PKPab, PKPbc and PKPdf
		// for (SeismicPhase phase : uncertaintyInterface.getSupportedPhases())
		// {
		// if (phase == SeismicPhase.PKPab || phase == SeismicPhase.PKPbc ||
		// phase == SeismicPhase.PKPdf)
		// continue;
		// String p = phase.toString();
		// if (p.endsWith("ab") || p.endsWith("bc") || p.endsWith("df"))
		// unsupportedPhases.add(phase);
		// }
		//
		// // add any user-specified phases to unsupportedPhases
		// unsupportedPhases.addAll(properties.getSeismicPhases("tauptoolkitUnsupportedPhases"));
		//
		// // supportedPhases will include all phases specified in both the
		// uncertainty directory
		// // and the ellipticity corrections directory (intersection), and
		// which are not
		// // explicitly excluded by unsupportedPhases.
		// supportedPhases.clear();
		// for (SeismicPhase phase : uncertaintyInterface.getSupportedPhases())
		// if ((ellip == null || ellip.getSupportedPhases().contains(phase)) &&
		// !unsupportedPhases.contains(phase))
		// supportedPhases.add(phase);

	}
	
	public static void main(String[] args) {
		try {
			
			AK135Rays ak135RaysProperties = null;
			
			if (args.length == 0)
	            throw new IOException("Error: No AK135Rays properties file name was provided ...");
			
			try {
	            // create AK135Rays object
				try {
					// when running from an executable jar, this will print out all the
					// dependencies with version numbers. Fails when run from an IDE.
					// Dependencies and version numbers are retrieved from <project>.version
					// files stored in the jar file.
					System.out.printf("AK135Rays dependencies:%n%s%n%n",
							Utils.getDependencyVersions());
				} catch (IOException e) {
				}

	            ak135RaysProperties = new AK135Rays(args[0]);
	            
	        } catch (Exception ex) {
	            ex.printStackTrace();

	        }
			
			
			// Instantiates a properties object and reads them in from a properties file
			//PropertiesPlusGMP properties = new PropertiesPlusGMP();
			PropertiesPlusGMP properties = ak135RaysProperties.aProps;

			// instantiate an instance of AK135Rays, based on properties.
			AK135Rays ak135RaysPredictor = new AK135Rays(properties);

			// instantiate a Receiver object
			// sta, ondate, offdate, lat, lon, elevation, staname, statype, refsta, dnorth, deast
			Receiver receiver  = new Receiver(properties.getProperty("receiver"));

			// Retrieve source information from properties file
			String sourceProperties = properties.getProperty("source");
			
			//The replaceAll and split is replacing all commas with a space, all white space (including tabs, new lines, etc.)
			//with a single space, and then split on the space
			String[] sourcePropArray = sourceProperties.replaceAll(",", " ").replaceAll("\\s+", " ").split(" ");
			
			//instantiate a Source object
			// lat, lon, depth, inDegrees
			//NOTE: Seems to specifically have trouble predicting correct value for P at 1 degree and 0 km depth in SALSA3D
			Source source = new Source(Double.parseDouble(sourcePropArray[0]), Double.parseDouble(sourcePropArray[1]), 
					Double.parseDouble(sourcePropArray[2]), Boolean.parseBoolean(sourcePropArray[3]));

			// Read in phase from properties file
			SeismicPhase phase = SeismicPhase.valueOf(properties.getProperty("phase"));

			// specify the attributes that need to be computed.
			//Set up empty ENUM with characteristics from the GeoAttributes class
			EnumSet<GeoAttributes> requestedAttributes = EnumSet.noneOf(GeoAttributes.class);
			
			String requestedAttributesProperties = properties.getProperty("requestedAttributes");
			String[] requestedAttributesPropArray = requestedAttributesProperties.replaceAll(",", " ").replaceAll("\\s+", " ").split(" ");
			
			for (String a: requestedAttributesPropArray)
				requestedAttributes.add(GeoAttributes.valueOf(a.toUpperCase()));

			// create a PredictionRequest
			PredictionRequest request = new PredictionRequest(receiver, source, phase, requestedAttributes, true);
			
			// convert phase in prediction request to taup phases
			
			TaupArrivalList taupArrivalList = new TaupArrivalList(request, ak135RaysPredictor);			
			String[] taupPhaseList = taupArrivalList.getPhaseList();
			
			String taupPhaseListString = Arrays.toString(taupPhaseList);
			//System.out.println("Input phase " + phase + " => " + taupPhaseListString);
			
			// compute the predictions based on the new input phase
			
			Prediction salsa3dPrediction = ak135RaysPredictor.getPrediction(request);
			//TODO: Need to update so that taupPhase is the taupPhase output by the getTaupRayPath method, not
			//the phase read in from the properties file. Commenting out for now.
			//String taupPhase = salsa3dPrediction.getPhase().toString();

			// Retrieve the computed travel time with taup phases
			
			//double tt_3dmodel_TaupPhase = taupPrediction.getAttribute(GeoAttributes.TRAVEL_TIME);
			double tt_3dmodel_TaupPhase = salsa3dPrediction.getAttribute(GeoAttributes.TRAVEL_TIME);
			
			// calculate ak135 travel time using taup toolkit.
			//double tt_ak135 = ak135RaysPredictor.getTaupTravelTime(
			//		taupPhase, request.getDistanceDegrees(), request.getSource().getDepth());
			
			// print out results
			//System.out.printf("tt3d_salsa3d = %1.3f, ttak135 = %1.3f, diff3d_salsa3d = %1.3f%n", tt_3dmodel_TaupPhase, tt_ak135, tt_3dmodel_TaupPhase-tt_ak135);
			
			System.out.printf("tt3d_salsa3d = %1.3f%n", tt_3dmodel_TaupPhase);
			
			// specify the attributes that need to be computed.
			requestedAttributes = EnumSet.of(GeoAttributes.TRAVEL_TIME,
					GeoAttributes.TURNING_DEPTH, GeoAttributes.DISTANCE_DEGREES);

			System.out.println("Done.");


		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private File getFile(SeismicPhase phase) throws FileNotFoundException {
		if (fileNamesIncludeModelName)
			return new File(tableDirectory, modelName + "." + phase.getFileName());

		return new File(tableDirectory, phase.getFileName());
	}
	
//	public LookupTable getTable(SeismicPhase phase) throws Exception {
//		synchronized (tableMap) {
//			Map<SeismicPhase, LookupTable> tables = getLookupTable(tableDirectory);
//			
//			if (tables.containsKey(phase))
//				return tables.get(phase);
//			
//			LookupTable t = new LookupTable(getFile(phase));
//			tables.put(phase, t);
//			return t;
//		}
//	}
	
	/**
	 * Compute a single prediction based on the PredictionRequest provided
	 * @param request
	 * @return prediction
	 */
	public Prediction getPrediction(PredictionRequest request) throws Exception {
		
		long timer = System.currentTimeMillis();
		
		//If the prediction request was non-defining
		if (!request.isDefining())
			return new Prediction(request, this, "PredictionRequest was non-defining");
		
		// instantiate a Prediction object to receive the results of the prediction calculations.
		Prediction prediction = new Prediction(request, PredictorType.AK135RAYS);
		
		try {
		
			// instantiate an instance of AK135Rays, based on properties.
			AK135Rays ak135RaysPredictor = new AK135Rays(properties);
	
			// convert phase in prediction request to taup phases
			
			TaupArrivalList taupArrivalList = new TaupArrivalList(request, ak135RaysPredictor);			
			String[] taupPhaseList = taupArrivalList.getPhaseList();
			
			String taupPhaseListString = Arrays.toString(taupPhaseList).replace("[", "").replace("]", "");
			//System.out.println("Input phase " + prediction.getPhase() + " => " + taupPhaseListString);
			
			// get the taup path (distance, depth)
			TimeDist[] taupPath = getTaupRayPath(taupPhaseListString, request.getDistanceDegrees(), request.getSource().getDepth(), request, prediction);
	
			// instantiate a list of unit vectors between source and receiver which will be populated with values
			// based on the taup path
			ArrayList<double[]> geographicPath = new ArrayList<>(taupPath.length);
			
			// radii along the path. The depths retrieved from taupPath will be converted to radii based 
			// on the geographic locations along the geographicPath.
			double[] radii = new double[taupPath.length];
	
			// Get a great circle path from source to receiver
			GreatCircle gc = new GreatCircle(request.getSource().getUnitVector(), request.getReceiver().getUnitVector());
			
			// Instantiate turnDepth variable
			double turnDepth = Double.NEGATIVE_INFINITY;
	
			// for every distance, depth in the taupPath
			for (int i=0; i<taupPath.length; ++i)
			{
				// get taup distance in radians
				double distance = taupPath[i].getDistRadian();
				// get taup depth in km 
				double depth = taupPath[i].getDepth();
	
				// get a geographic point along the great circle path between source and receiver
				double[] point = gc.getPoint(distance);
				// convert the taup depth to radius.  This will distort the path because geographic position and radius 
				// are based on the WGS84 ellipsoid.
				double radius = VectorGeo.getEarthRadius(point)-depth;
	
				// add the geographic point to the geographicPath
				geographicPath.add(point);
				// and the radius to the array of radii.
				radii[i] = radius;
				
				// add the turn depth
				turnDepth = Math.max(turnDepth, depth);
			}
	
			// Find the index of the attribute in the 3D model that we want to integrate
			String phaseName = request.getPhase().toString();
			
			int attribute = 0;
			
			//NOTE: Below wouldn't handle mixed phases. May need to modify in future
			if (phaseName.contains("p") || phaseName.contains("P")) {
				attribute = model3d.getMetaData().getAttributeIndex("PSLOWNESS");
			}
			else if (phaseName.contains("s") || phaseName.contains("S")) {
				attribute = model3d.getMetaData().getAttributeIndex("SSLOWNESS");
			}
	
			// do not constrain the calculation to any particular layers in the 3d model.
			int[] layers = null;
	
			// perform a path integral of the pslowness (or sslowness) in the 3D model along the ray path retrieved from taup.
			double travel_time = model3d.getPathIntegral(attribute, geographicPath, radii, layers, InterpolatorType.LINEAR, InterpolatorType.LINEAR);
	
			// set the value of the travel time, distance in degrees, and turning depth in the Prediction object. 
			prediction.setAttribute(GeoAttributes.TT_BASEMODEL, travel_time);
			
			// System.out.println(phaseName);
			// System.out.println(uncertaintyValue);
			
			if (useEllipticityCorrections) {
				EllipticityCorrections ellip = getEllipticityCorrections(ellipticityDirectory);
				double ellipCorr = ellip.getEllipCorr(request.getPhase(), request.getReceiver(), request.getSource());
				prediction.setAttribute(GeoAttributes.TT_ELLIPTICITY_CORRECTION, ellipCorr);
				
				travel_time += ellipCorr;
			}
			
			if (useElevationCorrections) {
				double sedVel;
				if (request.getPhase().getWaveTypeReceiver() == WaveType.P)
					sedVel = sedimentaryVelocityP;
				else if (request.getPhase().getWaveTypeReceiver() == WaveType.S)
					sedVel = sedimentaryVelocityS;
				else
					sedVel = Double.NaN;
			
			//find the elevation correction for the receiver
			double elevCorr = getElevationCorrection(-request.getReceiver().getDepth(),
					prediction.getAttribute(GeoAttributes.SLOWNESS_BASEMODEL), sedVel);
			
			prediction.setAttribute(GeoAttributes.TT_ELEVATION_CORRECTION, elevCorr);
			prediction.setAttribute(GeoAttributes.SEDIMENTARY_VELOCITY_RECEIVER, Double.isNaN(sedVel) ? Globals.NA_VALUE : sedVel);
			
			// if the source is above the surface of the earth, then find an elevation correction for the source.
			
			if (request.getPhase().getWaveTypeSource() == WaveType.P)
				sedVel = sedimentaryVelocityP;
			else if (request.getPhase().getWaveTypeSource() == WaveType.S)
				sedVel = sedimentaryVelocityS;
			else
				sedVel = Double.NaN;
			
			double srcElev = -request.getSource().getDepth();
			double srcElevCorr = srcElev <= 0. ? 0 : getElevationCorrection(srcElev, 
					prediction.getAttribute(GeoAttributes.SLOWNESS_BASEMODEL), sedVel);
			
			prediction.setAttribute(GeoAttributes.TT_ELEVATION_CORRECTION_SOURCE, srcElevCorr);
			prediction.setAttribute(GeoAttributes.SEDIMENTARY_VELOCITY_SOURCE, Double.isNaN(sedVel) ? Globals.NA_VALUE : sedVel);
			
			travel_time += elevCorr + srcElevCorr;
		
		}
			
			
			// TODO: (sballar; 2023-11-22) it is recommended to call method super.setGeoAttributes in the Predictor class
			// which will set a bunch of stuff in the Prediction object including any derived derivative values
			// and requested model uncertainty values.  It will also apply libcorr3d corrections if they were
			// requested.
			
			// (sballar) i think slowness got set in the prediction object in getTaupRayPath().  units are sec/radian
			
			// recall that to convert slowness from sec/deg to sec/radian, call toDegrees()
			setGeoAttributes(prediction, travel_time, request.getSeaz(), 
				prediction.getAttribute(GeoAttributes.SLOWNESS), 
				Double.NaN, Double.NaN, Double.NaN);
			
			
			
			
			
			prediction.setAttribute(GeoAttributes.DISTANCE_DEGREES, request.getDistanceDegrees()); 
			prediction.setAttribute(GeoAttributes.TURNING_DEPTH, turnDepth);
			
		} catch (Exception e) {
			e.printStackTrace();
			prediction = new Prediction(request, this, e);
		}

		if (request.getRequestedAttributes().contains(GeoAttributes.CALCULATION_TIME))
			prediction.setAttribute(GeoAttributes.CALCULATION_TIME, (System.currentTimeMillis() - timer) * 1e-3);
		
		/*System.out.println(request.getPhase().toString());
		System.out.println(prediction.getAttribute(GeoAttributes.TT_BASEMODEL));
		System.out.println(prediction.getAttribute(GeoAttributes.TT_ELLIPTICITY_CORRECTION));
		System.out.println(prediction.getAttribute(GeoAttributes.TT_ELEVATION_CORRECTION));
		System.out.println(prediction.getAttribute(GeoAttributes.TT_ELEVATION_CORRECTION_SOURCE));
		System.out.println(prediction.getAttribute(GeoAttributes.TT_MODEL_UNCERTAINTY));
		System.out.println(prediction.getAttribute(GeoAttributes.TRAVEL_TIME));
		System.out.println("");*/
		
		return prediction;
	}
	
	/**
	 * Returns the taup ray path based on phase, source-receiver distance in degrees, source depth,
	 * PredictionRequest object, source, and Prediction object
	 * @param phase
	 * @param distance source-receiver distance in degrees
	 * @param source depth in km.
	 * @param request
	 * @param prediction
	 * @return array with dimension 2 x n where first array refers to distances
	 * in radians and second array refers to depths.
	 * @throws Exception 
	 */
	private TimeDist[] getTaupRayPath(String phase, double distanceDegrees, double sourceDepth, PredictionRequest request, Prediction prediction) throws Exception {
		
		//Clear any previous phase names
		taupPierce.clearPhaseNames();
		
		//Set source depth and calculate distance in degrees based on input phase
		taupPierce.parsePhaseList(phase);
		
		// if sourceDepth is a small negative number, set it to zero
		if (sourceDepth < 0. && sourceDepth > -1e-3)
		    sourceDepth = 0.;
		taupPierce.setSourceDepth(sourceDepth);
		
		taupPierce.calculate(distanceDegrees);
		
		//A printed out arrival shows: 1) modulo distance in degrees 2) source depth in km 3) phase name 4) travel time in sec 5) ray parameter in sec/deg 6) takeoff angle in degrees
		//7) incident angle in degrees 8) distance in degrees 9) ray parameter index and 10) purist phase name
		//If the purist phase name is equal to the input phase name, purist phase name will be written with an = sign, e.g. = P; otherwise it will be written with an *
		//e.g., * P. See Arrival.java class in TauP
		List<Arrival> arrivals = taupPierce.getArrivals();

		TimeDist[] path = null;
		int index = 0;
		
		double slowness = 0;
		
		//Retrieve and set slowness parameter (in sec/radian)
		slowness = arrivals.get(index).getRayParam(); 
		prediction.setAttribute(GeoAttributes.SLOWNESS_BASEMODEL, slowness);
		prediction.setRayType(RayType.VALID);
		
		if (request.getPhase() == SeismicPhase.Pg && getPgVelocity() > 0)
		{
			// Override taup and assume the crust is single-layer, constant velocity
			
			double xkm = request.getReceiver().distance(request.getSource())*6371.;
			double zkm = request.getSource().getDepth();
			double incidenceAngle = Math.atan(xkm/zkm); //approximate!
			
			double rayLength = Math.sqrt(xkm*xkm + zkm*zkm);
			
			slowness = request.getSource().getRadius()*Math.sin(incidenceAngle)/getPgVelocity();
			prediction.setRayType(RayType.VALID);
		}
		
		else {
		
			// Determine which taup arrival corresponds with the phase requested by the user based on
			// depth and/or speed and/or distance. Set the RayType to REFLECTION, REFRACTION, TOP_SIDE_DIFFRACTION, or VALID depending on
			// what arrivals have been determined.
			if (arrivals.size() > 1) {
				
				System.out.printf("WARNING: Taup computed multiple Arrivals at distance, depth, phase = %1.3f, %1.3f, %s%n",
						distanceDegrees, sourceDepth, phase);
				ArrivalArray multiArrivals = null;
				
				if (request.getPhase() == SeismicPhase.Pn || request.getPhase() == SeismicPhase.Sn) {
					multiArrivals = new ArrivalArray(taupPierce.getArrivals(),0.,cmbDepth);
					
					if  (distanceDegrees < 2)
						index = multiArrivals.getDeepest();
					else
						index = multiArrivals.getFastest();
					prediction.setRayType(RayType.REFRACTION);
						
				}
				
			
				else if (request.getPhase() == SeismicPhase.Pg
						|| request.getPhase() == SeismicPhase.Sg
						|| request.getPhase() == SeismicPhase.Lg)
				{
					multiArrivals = new ArrivalArray(taupPierce.getArrivals(), 0., mohoDepth);
					
					if (distanceDegrees > 8)
						index = multiArrivals.getDeepest();
					else
						index = multiArrivals.getFastest();
					prediction.setRayType(RayType.VALID);
				}
				else if (request.getPhase() == SeismicPhase.Pmantle)
				{
					multiArrivals = new ArrivalArray(taupPierce.getArrivals(), mohoDepth, cmbDepth);

					index = multiArrivals.getFastest();
					
					if (distanceDegrees <= 1.5) {
						prediction.setRayType(RayType.REFLECTION);
					}
					
					else if (distanceDegrees > 1.5 && distanceDegrees < 115) {
						prediction.setRayType(RayType.REFRACTION);
					}
					
					else {
						prediction.setRayType(RayType.TOP_SIDE_DIFFRACTION);
					}
					
				}
				else if (request.getPhase() == SeismicPhase.PKPab)
				{
					multiArrivals = new ArrivalArray(taupPierce.getArrivals(), cmbDepth, icbDepth);
					
					index = multiArrivals.getSlowest();
					prediction.setRayType(RayType.REFRACTION);
				}
				else if (request.getPhase() == SeismicPhase.PKPbc)
				{
					if (taupPierce.getNumArrivals() == 2)
					{
						multiArrivals = new ArrivalArray(taupPierce.getArrivals(),cmbDepth, icbDepth);

						index = multiArrivals.getFastest();
					}
					else
						index = -1;
					prediction.setRayType(RayType.REFRACTION);
				}
				else if (request.getPhase() == SeismicPhase.PKPdf)
				{
					multiArrivals = new ArrivalArray(taupPierce.getArrivals(),icbDepth, 6371.);
					
					index = multiArrivals.getFastest();
					prediction.setRayType(RayType.REFRACTION);
				}
				
				else
				{
					multiArrivals = new ArrivalArray(taupPierce.getArrivals(), 0, 1e6);

					index = multiArrivals.getFastest();
					Arrival arrivalNum = taupPierce.getArrival(index);
					
					//Set ray type based on returned tauP arrival phase
					//TODO: add more phases to list
					if (arrivalNum.getPhase().getName().equals("PKIKP"))

					{
						prediction.setRayType(RayType.REFRACTION);
					}
					
					else if (arrivalNum.getPhase().getName().equals("Pdiff"))
					{
						prediction.setRayType(RayType.TOP_SIDE_DIFFRACTION);
					}
					
					else 
					{
						prediction.setRayType(RayType.VALID);
					}
				}
				
				//System.out.println(multiArrivals);
				//System.out.println(index);
				
				slowness = multiArrivals.get(index).getRayParam(); // sec/radian
				prediction.setAttribute(GeoAttributes.SLOWNESS, slowness);
				
			}
		}
		
		if (index < 0)

		    prediction = new Prediction(request,this,"All of the Arrivals returned by TaupToolkit "
					+ "had turning depths that were out of range");

		path = arrivals.get(index).getPath();
		
		return path;
	}
	
	protected Predictor getCopy() throws Exception {
		// if this class was thread safe, we could simply 'return this;' but it is not thread safe
		// so we must instantiate a new instance are return that.
		return new AK135Rays(properties);
	}
	

    /**
	 * Creates a new tomography output directory, or determines that a restart is to
	 * be run from an existing directory.
	 * 
	 * @param propertiesFileName The name of the properties file that will be copied
	 *                           into the new output directory if one is created.
	 */
	protected void createIODirectory(String propertiesFileName) throws IOException {
		
		// get io directory, check for linux conversion, and set back into
		// property name.

		aIODirectory = aProps.getProperty("ioDirectory", "").trim();
		aIODirectory = PropertiesPlusGMP.convertWinFilePathToLinux(aIODirectory);
		aProps.setProperty("ioDirectory", aIODirectory);
		
		// look for "(DATE)" sub string and replace with formatted date if found.
		// If sub string is found then this is a new execution and not a restart
		// set restart flag appropriately.
		
		boolean restart = true;
		int k = aIODirectory.toUpperCase().lastIndexOf("(DATE)");
		if (k > 0) {
			
			// found "(DATE)". Replace with formatted date string
			
			String DATE_FORMAT = "yyyy_MM_dd";
			aIODirectory = aIODirectory.substring(0, k) + Globals.getTimeStamp(DATE_FORMAT)
					+ aIODirectory.substring(k + 6);

			// set ioDirectory property with date substituted and set restart to false

			aProps.setProperty("ioDirectory", aIODirectory);
			restart = false;
		}

		// if output directory exists, append a number to it until it finds a directory
		// that does not exist

		if (!restart) {
			File outDir = new File(aIODirectory);
			if (outDir.exists()) {
				
				// found directory. remove trailing / or \ from path if exists
				
				if (aIODirectory.endsWith("\\") || aIODirectory.endsWith("/"))
					aIODirectory = aIODirectory.substring(0, aIODirectory.length() - 2);

				// find first unused run #
				
				String newIODir = aIODirectory;
				for (int i = 1; i < 1000; i++) {

					// create new directory name and see if it exists. continue to next
					// run # if the current one exists
					
					newIODir = aIODirectory + "_" + i;
					outDir = new File(newIODir);
					if (outDir.exists())
						continue;

					// found new run #. Create the new directory.
					
					System.out.println("Changing ioDirectory because the ioDirectory already exists: " + aIODirectory);
					System.out.println("Setting new ioDirectory to: " + newIODir);

					aIODirectory = newIODir;

					// set new ioDirectory into properties and exit loop

					aProps.setProperty("ioDirectory", newIODir);
					break;
				}
			}

			// create the new output directory(ies) and place a copy of the properties
			// file within it
			
			FileDirHandler.createOutDir(aIODirectory, propertiesFileName);

			// create output log file writer
			
			File outFile = new File(aIODirectory + File.separator + "out.txt"); // gtb added separator and out.txt

			// delete existing file contents (can't really exist because we just created the
			// directory)
			
			if (outFile.exists())
				outFile.delete();

			// create new output file (out.txt).
			
			outFile.createNewFile();

			// set output file into the screenwriter
			
			BufferedWriter outFileWriter = new BufferedWriter(new FileWriter(outFile));
			aScrnWrtr.setWriter(outFileWriter);
		}

	} 
	
	/**Retrieve the elevation correction for a given elevation above sea level (in km) and horizontal slowness (in sec/radian).
	 * The correction is elevation/sedimentaryVelocity * cos (incidence_angle)l
	 * 
	 */
	public double getElevationCorrection(double elevation, double slowness, double sedimentaryVelocity) {
		double el = slowness * sedimentaryVelocity/ 6371.;
		if (el > 1.0)
			el = 1.0/el;
		return elevation / sedimentaryVelocity * sqrt(1. - min(1.0, pow(el,2)));
	}
	
	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, String msg) {
		return new Prediction(predictionRequest, this, msg);
	}

	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, Exception ex) {
		return new Prediction(predictionRequest, this, ex);
	}
	
	/**
	 * @return the model description
	 */
	@Override
	public String getModelDescription() {
		return model3d.getMetaData().getDescription();
	}

	/**
	 * @return the model name
	 */
	@Override
	public String getModelName() {
		return model3d.getMetaData().getInputModelFile().getName();
	}

	/**
	 * @return the predictor name
	 */
	@Override
	public String getPredictorName() {
		// this will return 'ak135rays'
		return getClass().getSimpleName().toLowerCase();
	}

	/**
	 * @return the predictor type
	 */
	@Override
	public PredictorType getPredictorType() {
		return PredictorType.AK135RAYS;
	}

	/**
	 * @return the input model file
	 */
	@Override
	public File getModelFile() {
		return model3d.getMetaData().getInputModelFile();
	}
	
	/*public File getModelFile(SeismicPhase phase) throws Exception {
		LookupTable tbl = getTable(phase);
		return tbl == null ? null : tbl.getFile();
	}*/
	
	public double getSurfaceRadius(GeoVector position) throws GMPException {
		return 6371.;
	}
	
	public double getSurfaceDepth(GeoVector position) throws GMPException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSupported(Receiver receiver, SeismicPhase phase, GeoAttributes attribute, double originTime) {
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

	/**
	 * @return the supported attributes
	 */
	@Override
	public EnumSet<GeoAttributes> getSupportedAttributes() {
		return supportedAttributes;
	}

	/**
	 * @return the supported phase
	 */
	@Override
	public EnumSet<SeismicPhase> getSupportedPhases() {
		return supportedPhases;
	}

	/**
	 * @return the Earth Model
	 */
	@Override
	public Object getEarthModel() {
		return model3d;
	}

	/**
	 * @return the pgVelocity
	 */
	public double getPgVelocity()
	{
		return PgVelocity;
	}

	/**
	 * @return the moho depth
	 */
	public double getMohoDepth() {
		return mohoDepth;
		
	}
	
	/**
	 * @return the cmb depth
	 */
	public double getCmbDepth() {
		return cmbDepth;
		
	}

	/**
	 * @return the icb depth
	 */
	public double getIcbDepth() {
		return icbDepth;
		
	}
	
	/**
	 * Gets version number of predictor ak135-rays
	 * @return
	 */
	static public String getVersion() 	{ 
		if (version != null)
			return version;
		return (version = Utils.getVersion("ak135-rays"));
	    }

	/**
	/**
	 * @return predictor version number
	 */
	@Override
	public String getPredictorVersion() {
		return getVersion();
	}


	@Property(type = Boolean.class)
	public static final String PROP_USE_ELLIPTICITY_CORR = "ak135raysUseEllipticityCorrections";
	@Property(type = Boolean.class)
	public static final String PROP_USE_ELEV_CORR = "ak135raysUseElevationCorrections";
	@Property(type = File.class)
	public static final String PROP_ELLIPTICITY_CORR_DIR = "ak135raysEllipticityCorrectionsDirectory";
	@Property(type = File.class)
	public static final String PROP_SEISMIC_BASE_DATA = "seismicBaseData";
	@Property(type = Double.class)
	public static final String PROP_SEDIMENTARY_VELOCITY = "ak135raysSedimentaryVelocity";
	@Property
	public static final String PROP_UNCERTAINTY_TYPE = "ak135raysUncertaintyType";
	@Property(type = File.class)
	public static final String PROP_TABLE_DIR = "ak135raysTableDirectory";
	@Property(type = File.class)
	public static final String PROP_MODEL = "ak135raysModel";
	@Property (type = File.class)
	public static final String PROP_UNCERTAINTY_DIR = "ak135raysUncertaintyDirectory";

}
