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

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauP_Time;
import edu.sc.seis.TauP.TimeDist;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class AK135Rays extends Predictor {

	public static void main(String[] args) {
		try {
			
			// Instantiates a properties object. In normal operations properties are read from a file
			// but here we will populate them on the fly.
			PropertiesPlusGMP properties = new PropertiesPlusGMP();

			// don't have to specify this because this is the default in the AK135Rays constructor
			// properties.setProperty("tauptoolkitModel = ak135.tvel");

			// must specify the 3D model (geotess) that we want to compute travel times with
			properties.setProperty("ak135raysModel", "/Users/sballar/Documents/salsa3d/salsa3d_v2.1");

			// instantiate an instance of AK135Rays, based on properties.
			AK135Rays ak135RaysPredictor = new AK135Rays(properties);

			// instantiate a Receiver object
			// sta, ondate, offdate, lat, lon, elevation, staname, statype, refsta, dnorth, deast
			Receiver receiver = new Receiver("XYZ", -1, 2286324, 0., 0., 0., "Fictitious station", "ss", "XYZ", 0., 0.);

			// instantiate a Source object
			// lat, lon, depth, inDegrees
			Source source = new Source(0., 30., 0., true);

			// Specify a phase
			SeismicPhase phase = SeismicPhase.valueOf("P");

			// specify the attributes that need to be computed.
			EnumSet<GeoAttributes> requestedAttributes = EnumSet.of(GeoAttributes.TRAVEL_TIME);

			// create a PreditionRequest
			PredictionRequest request = new PredictionRequest(receiver, source, phase, requestedAttributes, true);

			// compute the predictions
			Prediction prediction = ak135RaysPredictor.getPrediction(request);
			
			// Retrieve the computed travel time
			double tt_3dmodel = prediction.getAttribute(GeoAttributes.TRAVEL_TIME);
			
			// calculate ak135 travel time using taup toolkit
			double tt_ak135 = ak135RaysPredictor.getTaupTravelTime(
					request.getPhase().toString(), request.getDistance(), request.getSource().getDepth());
			
			// print out results
			System.out.printf("tt3d = %1.3f, ttak135 = %1.3f, diff = %1.3f%n", tt_3dmodel, tt_ak135, tt_3dmodel-tt_ak135);
			
			// specify the attributes that need to be computed.
			requestedAttributes = EnumSet.of(GeoAttributes.TRAVEL_TIME,
					GeoAttributes.TURNING_DEPTH, GeoAttributes.DISTANCE_DEGREES);

			for (int longitude = 1; longitude <= 25; ++longitude)
			{
				source = new Source (0, longitude, 0, true);
				request = new PredictionRequest(receiver, source, phase, requestedAttributes, true);
				prediction = ak135RaysPredictor.getPrediction(request);
				System.out.printf("%10.3f %10.3f %10.3f%n", 
						prediction.getAttribute(GeoAttributes.DISTANCE_DEGREES),
						prediction.getAttribute(GeoAttributes.TURNING_DEPTH),
						prediction.getAttribute(GeoAttributes.TRAVEL_TIME));
			}
			
			System.out.println("Done.");
			
			
			// example code derived from the Taup Toolkit Manual (http://www.seis.sc.edu/downloads/TauP/taup.pdf)
			//			TauP_Time timeTool = new TauP_Time("ak135.tvel");
			//			timeTool.parsePhaseList("P");
			//			timeTool.setSourceDepth(15.0);
			//
			//			timeTool.calculate(40);
			//			
			//			List<Arrival> arrivals = timeTool.getArrivals();
			//			for (int i=0; i<arrivals.size(); i++) {
			//				System.out.println(arrivals.get(i)+" arrives at "+
			//						(arrivals.get(i).getDist()*180.0/Math.PI)+" degrees after "+
			//						arrivals.get(i).getTime()+" seconds.");
			//			}
			//			System.out.println();
			//
			//			TimeDist[] path = arrivals.get(0).getPath();
			//			for (TimeDist td : path)
			//				//System.out.printf("%8.3f %8.3f%n", td.getDistDeg(), td.getDepth());
			//				System.out.println(td);


		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * The TaupToolkit model.  For ak135 raypaths this will be 'ak135.tvel'.
	 * By making this a class variable, this Predictor is not thread safe.
	 */
	private TauP_Time taup;

	/**
	 * The 3D model through which we want to compute travel times using a path integral
	 * along an ak135 raypath.
	 */
	private GeoTessModel model3d;

	/**
	 * This is the set of GeoAttributes that AK135Rays is currently capable of
	 * computing. The set of GeoAttributes that is actually computed during any
	 * call to getPredictions() will depend
	 * on the set of requestetdAttributes that are submitted as part of the
	 * PredictionRequest object.
	 */
	public final EnumSet<GeoAttributes> supportedAttributes = EnumSet.of(GeoAttributes.TRAVEL_TIME);

	//	/**
	//	 * This is the set of GeoAttributes that LookupTablesGMP is capable of
	//	 * computing. The set of GeoAttributes that is actually computed during any
	//	 * call to LookupTablesGMP.getPrediction() or getPredictions() will depend
	//	 * on the set of requestetdAttributes that are submitted as part of the
	//	 * PredictionRequest object.
	//	 */
	//	public final EnumSet<GeoAttributes> supportedAttributes = EnumSet
	//			.of(GeoAttributes.TRAVEL_TIME, 
	//					GeoAttributes.TT_BASEMODEL,
	//					GeoAttributes.TT_MODEL_UNCERTAINTY,
	//					GeoAttributes.TT_PATH_CORRECTION,
	//					GeoAttributes.TT_PATH_CORR_DERIV_HORIZONTAL,
	//					GeoAttributes.TT_PATH_CORR_DERIV_RADIAL,
	//					GeoAttributes.TT_ELLIPTICITY_CORRECTION,
	//					GeoAttributes.TT_ELEVATION_CORRECTION,
	//					GeoAttributes.TT_ELEVATION_CORRECTION_SOURCE,
	//					GeoAttributes.DTT_DLAT, 
	//					GeoAttributes.DTT_DLON,
	//					GeoAttributes.DTT_DR, 
	//					GeoAttributes.DTT_DTIME,
	//					GeoAttributes.AZIMUTH, 
	//					GeoAttributes.AZIMUTH_DEGREES,
	//					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
	//					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES,
	//					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_HORIZONTAL,
	//					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_RADIAL,
	//					GeoAttributes.DAZ_DLAT, 
	//					GeoAttributes.DAZ_DLON,
	//					GeoAttributes.DAZ_DR, 
	//					GeoAttributes.DAZ_DTIME,
	//					GeoAttributes.SLOWNESS, 
	//					GeoAttributes.SLOWNESS_DEGREES,
	//					GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY,
	//					GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES,
	//					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_HORIZONTAL,
	//					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_RADIAL,
	//					GeoAttributes.DSH_DLAT, 
	//					GeoAttributes.DSH_DLON,
	//					GeoAttributes.DSH_DR, 
	//					GeoAttributes.DSH_DTIME,
	//					GeoAttributes.BACKAZIMUTH, 
	//					GeoAttributes.OUT_OF_PLANE,
	//					GeoAttributes.CALCULATION_TIME, 
	//					GeoAttributes.DISTANCE,
	//					GeoAttributes.DISTANCE_DEGREES);

	/**
	 * Phases that this Predictor can support.  All phases.
	 */
	protected final EnumSet<SeismicPhase> supportedPhases = EnumSet.allOf(SeismicPhase.class);

	static public String getVersion() { return Utils.getVersion("ak135-rays"); }	
	
	/**
	 * Default constructor doesn't do much
	 */
	public AK135Rays() {
		super();
	}

	/**
	 * Parameterized constructor loads the tauptoolkit model and the 3D model into memory
	 * @param properties
	 * @throws Exception
	 */
	public AK135Rays(PropertiesPlus properties) throws Exception {
		super(properties);

		// not necessary to specify this property in the properties object since ak135.tvel is the default
		taup = new TauP_Time(properties.getProperty("tauptoolkitModel", "ak135.tvel"));

		File modelFile = properties.getFile("ak135raysModel");
		
		// if user specified a model directory that contains a file called prediction_model.geotess
		// load the prediction model.  Otherwise load the model from the user specified file.
		if (modelFile.isDirectory() && new File(modelFile, "prediction_model.geotess").exists())
			model3d = new GeoTessModel(new File(modelFile, "prediction_model.geotess"));
		else
			model3d = new GeoTessModel(modelFile);
	}

	/**
	 * Compute a single prediction based on the PredictionRequest provided
	 * @param request
	 * @return prediction
	 */
	@Override
	public Prediction getPrediction(PredictionRequest request) throws Exception {

		// instantiate a Prediction object to receive the results of the prediction calculations.
		Prediction prediction = new Prediction(request, PredictorType.AK135RAYS);

		// get the taup path (distance, depth)
		TimeDist[] taupPath = getTaupRayPath(request.getPhase().toString(), request.getDistance(), request.getSource().getDepth());

		// instantiate a list of unit vectors between source and receiver which will be populated with values
		// based on the taup path
		ArrayList<double[]> geographicPath = new ArrayList<>(taupPath.length);
		
		// radii along the path.  The depths retrieved from taupPath will be converted to radii based 
		// on the geographic locations along the geographicPath.
		double[] radii = new double[taupPath.length];

		// Get a great circle path from source to receiver
		GreatCircle gc = new GreatCircle(request.getSource().getUnitVector(), request.getReceiver().getUnitVector());
		
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
			
			turnDepth = Math.max(turnDepth, depth);
		}

		// Find the index of the attribute in the 3D model that we want to integrate
		int attribute = model3d.getMetaData().getAttributeIndex("PSLOWNESS");

		// do not constrain the calculation to any particular layers in the 3d model.
		int[] layers = null;

		// perform a path integral of the pslowness in the 3D model along the ray path retrieved from taup.
		double travel_time = model3d.getPathIntegral(attribute, geographicPath, radii, layers, InterpolatorType.LINEAR, InterpolatorType.LINEAR);

		// set the value of the travel time in the Prediction object. 
		prediction.setAttribute(GeoAttributes.TRAVEL_TIME, travel_time);
		
		prediction.setAttribute(GeoAttributes.DISTANCE_DEGREES, request.getDistanceDegrees()); 
		prediction.setAttribute(GeoAttributes.TURNING_DEPTH, turnDepth); 
		
		return prediction;
	}

	/**
	 * 
	 * @param distance source-receiver distance in radians
	 * @param depth depth in km.
	 * @return array with dimension 2 x n were first array refers to distances
	 * in radians and second array refers to depths.
	 * @throws Exception 
	 */
	private TimeDist[] getTaupRayPath(String phase, double distanceDegrees, double sourceDepth) throws Exception {

		taup.parsePhaseList("p,P");
		taup.setSourceDepth(sourceDepth);
		taup.calculate(distanceDegrees);
		List<Arrival> arrivals = taup.getArrivals();
		if (arrivals.size() > 1)
			System.out.printf("WARNING: Taup computed multiple Arrivals at distance, depth, phase = %1.3f, %1.3f, %s%n",
					distanceDegrees, sourceDepth, phase);
		TimeDist[] path = arrivals.get(0).getPath();
		return path;
	}

	private double getTaupTravelTime(String phase, double distanceDegrees, double sourceDepth) throws Exception {
		taup.parsePhaseList(phase);
		taup.setSourceDepth(sourceDepth);
		taup.calculate(distanceDegrees);
		List<Arrival> arrivals = taup.getArrivals();
		if (arrivals.size() > 1)
			System.out.printf("WARNING: Taup computed multiple Arrivals at distance, depth, phase = %1.3f, %1.3f, %s%n",
					distanceDegrees, sourceDepth, phase);
		
		return arrivals.get(0).getTime();

	}
//	@Override
//	protected Predictor getCopy() throws Exception {
//		// if this class was thread safe, we could simply 'return this;' but it is not thread safe
//		// so we must instantiate a new instance are return that.
//		return new AK135Rays(properties);
//	}

	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, String msg) {
		return new Prediction(predictionRequest, this, msg);
	}

	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, Exception ex) {
		return new Prediction(predictionRequest, this, ex);
	}

	@Override
	public String getModelDescription() throws Exception {
		return model3d.getMetaData().getDescription();
	}

	@Override
	public String getModelName() {
		return model3d.getMetaData().getInputModelFile().getName();
	}

	@Override
	public String getPredictorName() {
		// this will return 'ak135rays'
		return getClass().getSimpleName().toLowerCase();
	}

	@Override
	public PredictorType getPredictorType() {
		return PredictorType.AK135RAYS;
	}

	@Override
	public File getModelFile() {
		return model3d.getMetaData().getInputModelFile();
	}

	@Override
	public boolean isSupported(Receiver receiver, SeismicPhase phase, GeoAttributes attribute, double originTime) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getPredictorVersion() {
		// TODO Auto-generated method stub
		return null;
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
		return model3d;
	}

}
