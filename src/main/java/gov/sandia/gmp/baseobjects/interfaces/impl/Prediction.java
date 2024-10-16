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

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.geovector.GeoVectorRay;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayPath;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.globals.WaveType;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintyType;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerDouble;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.testingbuffer.TestBuffer;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;

/*
 * A container class to store predicted values of seismic observables in an EnumMap of GeoAttributes
 * -> Double It doesn't process the information in anyway but simply stores the values and supplies
 * getters and setters. This class, and other classes derived off of it, are appropriate for
 * serialization and transmission across networks.
 */
public class Prediction implements Serializable {
	private static final long serialVersionUID = -6015387032594709889L;

	private PredictionRequest predictionRequest;

	/**
	 * An EnumMap containing GeoAttributes and computed values. The keySet will be a subset of all the
	 * GeoAttributes that are supported by a Predictor (see Predictor.getSupportedGeoAttributes(). The
	 * keySet will contain all the GeoAttributes that were requested of the Predictor object in a
	 * PredictionRequest object. Some of the results may be invalid.
	 */
	private EnumMap<GeoAttributes, Double> values =
			new EnumMap<GeoAttributes, Double>(GeoAttributes.class);

	/**
	 * A list of GeoVectors that define the ray path. The original ray computed by the Predictor is
	 * resampled such that points are approximately evenly spaced along the ray. The spacing is
	 * controlled by private static variable RayPath.nodeSpacing, which can be modified by calling
	 * method static method RayPath.setRayPathNodeSpacing. To clarify, in each RaySegment (model
	 * layer) the points on the rayPath are equal but reduce from specified nodeSpacing so that an
	 * points are evenly spaced.
	 * <p>
	 * The GeoVector objects that make up the rayPath are actually GeoVectorRay objects which add
	 * layerId and waveType (PSLOWNESS or SSLOWNESS to each point.
	 * <p>
	 * rayPath may contain node weights, if they have been calculated. See RayPath.getWeights() for
	 * more info.
	 */
	protected RayPath rayPath;

	/**
	 * Map from TRAVEL_TIME, AZIMUTH, SLOWNESS to UncertaintyType
	 */
	protected EnumMap<GeoAttributes, UncertaintyType> uncertaintyTypes = 
			new EnumMap<>(GeoAttributes.class); 


	/**
	 * Map from WaveType (P or S) to a map of tomography weights. The weights are geotess model point
	 * indices touched by this ray and the corresponding weight. The sum of the weights is equal to
	 * the path length of this ray in km.
	 * <p>
	 * Here is how to iterate over the data in this map:
	 * 
	 * <pre>
	 * {@code 
	 * for (Entry<WaveType, HashMapIntegerDouble> e : prediction.getRayWeights().entrySet()) {
    WaveType waveType = e.getKey();
    HashMapIntegerDouble weights = e.getValue();
    HashMapIntegerDouble.Iterator it = weights.iterator();
    while (it.hasNext()) {
    	HashMapIntegerDouble.Entry ite = it.nextEntry();
    	int pointIndex = ite.getKey();
    	double weight = ite.getValue();
    	// do something with wavetype, pointIndex and weight.
    }
  }
	 * </pre>
	 */
	private Map<WaveType, HashMapIntegerDouble> rayWeights = new LinkedHashMap<>();

	private Map<WaveType, Double> rayWeightsInactive = new LinkedHashMap<>();

	/**
	 * If the ray path is to be resampled, then this is the initial estimate of the node spacing along
	 * the ray in km. The actual node spacing may be less than this value in order that an integer
	 * number of equally spaced nodes define the ray.
	 * <p>
	 * If nodeSpacing gets set to a value <= 0. then ray path is not resampled. The original points
	 * that define the ray will be returned.
	 */
	protected static double nodeSpacing = 10.;

	private String modelName = "-";

	private String predictorName = "-";

	private PredictorType predictorType = null;

	private String predictorVersion = "-";

	/**
	 * values of slowness sampled along the rayPath
	 */
	protected double[] slowPath;

	/**
	 * The index of the layer in which the deepest point on the ray resides.
	 */
	protected int bottomLayer = -1;

	/**
	 * What is this?
	 */
	protected double[] activeNodeDerivs;

	/**
	 * What type of ray this is: INVALID, ERROR, REFRACTION, DIFFRACTION, REFLECTION.
	 */
	protected RayType rayType = RayType.INVALID;

	/**
	 * 
	 */
	protected String rayTypeString = "";

	/**
	 * Error message that can be set by Predictor objects
	 */
	protected String errorMessage;

	/**
	 * Maximum error message length. If an application calls setErrorMessage() with message longer
	 * than this length, then middle of message is replaced with " ... " such that the length of the
	 * message is reduced to maxErrorMessageLength.
	 */
	public final static int maxErrorMessageLength = 10000;

	protected String statusLog;

	public static final int maxStatusLogLength = 10000;

	public Prediction(PredictionRequest request, PredictorType type) {
		this.rayPath = new RayPath();

		this.predictionRequest = request;
		this.predictorType = type;

		for (GeoAttributes attribute : request.getRequestedAttributes())
			values.put(attribute, Globals.NA_VALUE);
	}

	/**
	 * Constructor to be used in the case where a Prediction calculation failed for some reason.
	 * getRayType() will return RayType.INVALID;
	 * 
	 * @param request a Prediction will keep a reference to this predictionRequest
	 * @param predictor basic info is copied from this Predictor but no reference is maintained. Can be null.
	 * @param message
	 */
	public Prediction(PredictionRequest request, Predictor predictor, String message) {
		this(request, predictor == null ? null : predictor.getPredictorType());

		if (predictor != null) {
			setModelName(predictor.getModelName());
			predictorName = predictor.getPredictorName();
			predictorVersion = predictor.getPredictorVersion();
		}

		setErrorMessage(message);
		rayType = RayType.INVALID;
		this.rayPath = new RayPath();
	}

	/**
	 * Constructor to be used in the case where a Prediction calculation failed for some reason.
	 * getRayType() will return RayType.INVALID;
	 * 
	 * @param request a Prediction will keep a reference to this predictionRequest
	 * @param predictor basic info is copied from this Predictor but no reference is maintained. Can be null.
	 * @param exception
	 */
	public Prediction(PredictionRequest request, Predictor predictor, Exception ex) {
		this(request, predictor == null ? null : predictor.getPredictorType());

		if (predictor != null) {
			setModelName(predictor.getModelName());
			predictorName = predictor.getPredictorName();
			predictorVersion = predictor.getPredictorVersion();
		}

		StringBuffer buf = new StringBuffer();
		buf.append(String.format("%s%n", ex.getClass().getName()));
		if (ex.getMessage() != null)
			buf.append(String.format("    %s%n", ex.getMessage()));

		//    buf.append(String.format("Rcvr %s,  Src %s; Phase = %s, Delta = %1.4f%n",
		//        ((GeoVector) getReceiver()).geovectorToString(),
		//        ((GeoVector) getSource()).geovectorToString(), request.getPhase().toString(),
		//        request.getDistanceDegrees()));
		for (StackTraceElement trace : ex.getStackTrace())
			buf.append(String.format("        at %s%n", trace));
		setErrorMessage(buf.toString());
		rayType = RayType.ERROR;
	}

	public void readPrediction(FileInputBuffer fib) throws Exception {
		bottomLayer = fib.readInt();
		rayType = RayType.valueOf(fib.readString());
		rayTypeString = fib.readString();

		// read GeoAttribute values
		values.clear();
		int ct = fib.readInt();
		for (int i = 0; i < ct; ++i) {
			String attrStrng = fib.readString();
			double attrValue = fib.readDouble();
			values.put(GeoAttributes.valueOf(attrStrng), attrValue);
		}

		rayPath = null;
		if (fib.readBoolean())
			rayPath = new RayPath(fib);
	}

	public static void writeRayWeights(Map<WaveType, HashMapIntegerDouble> rayWeights,
			FileOutputBuffer fob) throws IOException {
		if (rayWeights == null)
			fob.writeInt(0);
		else {
			fob.writeInt(rayWeights.size());
			for (WaveType waveType : rayWeights.keySet()) {
				//fob.writeString(waveType.toString());
				fob.writeInt(waveType.ordinal());

				HashMapIntegerDouble weights = rayWeights.get(waveType);

				fob.writeInt(weights.size());
				IOException[] ioe = new IOException[1];
				weights.forEach((pt, w) -> {
					try {
						fob.writeInt(pt);
						fob.writeDouble(w);
					} catch (IOException e) {
						ioe[0] = e;
					}
				});
				if (ioe[0] != null)
					throw ioe[0];
			}
		}
	}

	public static Map<WaveType, HashMapIntegerDouble> readRayWeights(FileInputBuffer fib)
			throws IOException {
		Map<WaveType, HashMapIntegerDouble> rw = new LinkedHashMap<>(2);
		int types = fib.readInt();
		for (int i = 0; i < types; i++) {
			//WaveType waveType = WaveType.valueOf(fib.readString());
			WaveType waveType = WaveType.values()[fib.readInt()];

			int size = fib.readInt();
			HashMapIntegerDouble map = new HashMapIntegerDouble(Math.max(1,size));
			rw.put(waveType, map);
			for (int j = 0; j < size; ++j)
				map.put(fib.readInt(), fib.readDouble());
		}

		return rw;
	}

	/**
	 * Writes the data for this prediction to the input FileOutputBuffer. The buffer is assumed to be
	 * open on entry and is not closed on exit.
	 * 
	 * @param fob The FileOutputBuffer into which this predictions data is written.
	 * @throws IOException
	 */
	public void writePrediction(FileOutputBuffer fob) throws IOException {
		fob.writeInt(bottomLayer);
		fob.writeString(getRayType().toString());
		fob.writeString(rayTypeString);

		fob.writeInt(getAttributes().size());
		for (Map.Entry<GeoAttributes, Double> e : getAttributes().entrySet()) {
			fob.writeString(e.getKey().toString());
			fob.writeDouble(e.getValue());
		}

		fob.writeBoolean(rayPath != null);
		if (rayPath != null)
			rayPath.write(fob);
	}

	public PredictionRequest getPredictionRequest() {
		return predictionRequest;
	}

	public String getModelName() {
		return modelName;
	}

	public String getPredictorName() {
		return predictorName;
	}

	public PredictorType getPredictorType() {
		return predictorType;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public void setPredictorName(String predictorName) {
		this.predictorName = predictorName;
	}

	public String getErrorMessage() {
		return errorMessage == null ? "" : errorMessage;
	}

	public long getObservationId() {
		return predictionRequest.getObservationId();
	}

	public SeismicPhase getPhase() {
		return predictionRequest.getPhase();
	}

	/**
	 * getWaveType will return one of P, S
	 *
	 * @return WaveType
	 */
	public WaveType getWaveType() {
		return predictionRequest.getPhase().getWaveType();
	}

	public void setRayType(RayType rayType) {
		this.rayType = rayType;
	}

	public RayType getRayType() {
		return rayType;
	}


	public Receiver getReceiver() {
		return predictionRequest.getReceiver();
	}


	public Source getSource() {
		return predictionRequest.getSource();
	}

	/**
	 * An EnumMap containing GeoAttributes and computed values. The keySet will be a subset of all the
	 * GeoAttributes that are supported by a Predictor (see Predictor.getSupportedGeoAttributes(). The
	 * keySet will contain all the GeoAttributes that were requested of the Predictor object in a
	 * PredictionRequest object. Some of the results may be invalid. It will not contain any
	 * GeoAttributes that were not requested.
	 * 
	 * @return EnumMap<GeoAttributes, Double>
	 */

	public EnumMap<GeoAttributes, Double> getSupportedAttributes() {
		return values;
	}

	/**
	 * A String representation of all the GeoAttributes that were requested when this Prediction was
	 * computed.
	 */
	public String getSupportedAttributesString() {
		StringBuffer s = new StringBuffer();
		for (GeoAttributes a : values.keySet())
			s.append(s.length() == 0 ? "" : ", ").append(a.toString());
		return s.toString();
	}

	public boolean isValid() {
		return getRayType() != RayType.ERROR && getRayType() != RayType.INVALID;
	}

	/**
	 * Set the error message string for this ray.
	 * 
	 * @param errorMessage String
	 */
	public void setErrorMessage(String errorMessage) {
		//		if (errorMessage == null)
		//			this.errorMessage = "";
		//		else 
		{
			if (errorMessage.length() < maxErrorMessageLength)
				this.errorMessage = errorMessage;
			else
				this.errorMessage = String.format("%s%n%n..<original message size = %d>..%n%n%s",
						errorMessage.substring(0, maxErrorMessageLength / 2), errorMessage.length(),
						errorMessage.substring(errorMessage.length() - maxErrorMessageLength / 2));
		}
	}

	/**
	 * Set the error message for this ray.
	 * 
	 * @param errorMessage String
	 */
	public void setErrorMessage(Exception ex) {
		this.errorMessage = Globals.getExceptionAsString(ex);
	}

	/**
	 * Store a GeoAttribute-value pair.
	 * 
	 * @param attribute
	 * @param value
	 */

	public void setAttribute(GeoAttributes attribute, double value) {
		values.put(attribute, Double.valueOf(value));
	}

	/**
	 * Retrieve a new double[] containing the values of the requested attributes.
	 */

	public double[] getAttributes(GeoAttributes[] attributes) {
		double[] val = new double[attributes.length];
		for (int i = 0; i < attributes.length; ++i)
			val[i] = getAttribute(attributes[i]);
		return val;
	}


	public EnumMap<GeoAttributes, Double> getAttributes(EnumSet<GeoAttributes> attributes) {
		EnumMap<GeoAttributes, Double> val = new EnumMap<GeoAttributes, Double>(GeoAttributes.class);
		getAttributes(attributes, val);
		return val;
	}


	public void getAttributes(EnumSet<GeoAttributes> attributes,
			EnumMap<GeoAttributes, Double> values) {
		for (GeoAttributes attribute : attributes)
			values.put(attribute, getAttribute(attribute));
	}

	/**
	 * Retrieve the value of the specified GeoAttribute. Returns BaseConst.NA_VALUE if an unsupported
	 * GeoAttribute is requested.
	 * 
	 * @param attribute GeoAttributes
	 * @return double
	 */
	public double getAttribute(String attribute) {
		return getAttribute(GeoAttributes.valueOf(attribute.toUpperCase()));
	}

	/**
	 * Retrieve the value of the specified GeoAttribute. Returns Globals.NA_VALUE (-999999.0) if an unsupported
	 * GeoAttribute is requested.
	 * 
	 * @param attribute GeoAttributes
	 * @return double
	 */
	public double getAttribute(GeoAttributes attribute) {
		return getAttribute(attribute,Globals.NA_VALUE);
	}
	
	public double getAttribute(GeoAttributes attribute, double defValue) {
	  Double value = values.get(attribute);
	  return value == null || value.doubleValue() == Globals.NA_VALUE ? defValue : value.doubleValue();
	}

	public EnumMap<GeoAttributes, Double> getAttributes() {
		return values;
	}


	/**
	 * Get the tomography weights for this observation, if they were computed. RayWeights is a map
	 * from WaveType (either WaveType.P of WaveType.S) to a Tuple<int[], double[]>. The ints are point
	 * indexes in a GeoTessModel that includes all the points in the model that were touched by the
	 * ray. The doubles are the weights (sum of interpolation coefficients * interval length) that
	 * correspond to the point indexes. The int[] and the double[] within a single Tuple are
	 * guaranteed to have the same length.
	 * <p>
	 * For phases that comprise of only one wavetype (P, pP, PKP, S,sS, ScS, etc), the map will only
	 * have one key (WaveType.P or WaveType.S) and the entries of the double[] of the corresponding
	 * Tuple will sum to the length of the raypath.
	 * <p>
	 * For phases comprised of segments of two different wavetypes (PS, sP, PcS, SKS, etc), there will
	 * be two keys in the map (WaveType.P and WaveType.S) and the doubles of the corresponding Tuples
	 * will sum to the length of the segments of the ray that traveled through the different parts of
	 * the ray. The sum of all the doubles[] in both map.values() will still sum to the length of the
	 * overall ray.
	 * 
	 * @return rayWeights, which will be empty if the tomography weights were not computed. Will never
	 *         be null.
	 */
	public Map<WaveType, HashMapIntegerDouble> getRayWeights() {
		return rayWeights;
	}

	public String toString() {
		EnumSet<RayType> validTypes = EnumSet.of(RayType.REFLECTION, RayType.REFRACTION,
				RayType.TOP_SIDE_DIFFRACTION, RayType.BOTTOM_SIDE_DIFFRACTION,
				RayType.INFRASOUND_WAVE, RayType.HYDROACOUSTIC_WAVE);

		StringBuffer buf = new StringBuffer();
		try {
			buf.append(String.format("Predictor: %s, %s%n", predictorName, modelName));
			buf.append(String.format("Receiver: %s%n", getReceiver().toString()));
			buf.append(String.format("Source: %s%n", getSource().toString()));
			buf.append(String.format("Phase: %s%n", getPhase().toString()));
			buf.append(String.format("RayType: %s%n", getRayType()));
			if (validTypes.contains(getRayType()))
				for (GeoAttributes attribute : values.keySet())
					buf.append(String.format("%-20s : %1.6f%n", attribute.toString(), values.get(attribute)));
			else
				buf.append(errorMessage).append(NL);
		} catch (Exception e) {
			buf.append(Globals.exceptionToString(e));
		}

		return buf.toString();
	}

	/**
	 * Retrieve an arrival row populated with predicted values
	 * 
	 * @return
	 */
	public Arrival getArrivalRow() {
		Arrival arrivalRow = new Arrival();

		arrivalRow.setArid(getObservationId());
		arrivalRow.setSta(getReceiver().getSta());
		arrivalRow.setTime(getSource().getOriginTime() + getAttribute(GeoAttributes.TRAVEL_TIME));
		arrivalRow.setJdate(GMTFormat.getJDate(arrivalRow.getTime()));
		arrivalRow.setDeltim(1.);
		arrivalRow.setAzimuth(getAttribute(GeoAttributes.AZIMUTH_DEGREES));
		arrivalRow.setDelaz(15.);
		arrivalRow.setSlow(getAttribute(GeoAttributes.SLOWNESS_DEGREES));
		arrivalRow.setDelaz(1.5);
		arrivalRow.setAuth(System.getenv("user.name"));
		arrivalRow.setIphase(getPhase().toString());

		return arrivalRow;
	}

	/**
	 * Retrieve an assoc row populated with predicted values
	 * 
	 * @return
	 */
	public Assoc getAssocRow() {
		AssocExtended assocRow = new AssocExtended();

		assocRow.setArid(getObservationId());
		assocRow.setSta(getReceiver().getSta());
		assocRow.setOrid(getSource().getSourceId());
		assocRow.setPhase(getPhase().toString());
		assocRow.setDelta(getSource().distanceDegrees(getReceiver()));
		assocRow.setSeaz(getSeaz());
		assocRow.setEsaz(getEsaz());

		assocRow.setTimeres(0.);
		assocRow.setAzres(0.);
		assocRow.setSlores(0.);

		assocRow.setTimedef("d");
		assocRow.setAzdef("d");
		assocRow.setSlodef("d");

		String vmodel = modelName;
		int index = vmodel.lastIndexOf(File.separatorChar);
		if (index > 0) {
			File f = new File(vmodel);
			if (f.getName().equals("prediction_model.geotess"))
				f = f.getParentFile();
			vmodel = f.getName();
		}
		if (vmodel.length() > 15)
			vmodel = vmodel.substring(0, 15);

		assocRow.setVmodel(vmodel);

		return assocRow;
	}

	/**
	 * Get event to station azimuth in degrees. Range 0 to 360. Error value is Assoc.ESAZ = -1
	 * 
	 * @return
	 */
	public double getEsaz() {
		double az = (VectorUnit.azimuthDegrees(getSource().getUnitVector(),
				getReceiver().getUnitVector(), Double.NaN) + 360.) % 360.;
		return Double.isNaN(az) ? Assoc.ESAZ_NA : az;
	}

	/**
	 * Get station to event azimuth in degrees. Range 0 to 360. Error value is Assoc.SEAZ = -1
	 * 
	 * @return
	 */
	public double getSeaz() {
		double az = (VectorUnit.azimuthDegrees(getReceiver().getUnitVector(),
				getSource().getUnitVector(), Double.NaN) + 360.) % 360.;
		return Double.isNaN(az) ? Assoc.SEAZ_NA : az;
	}

	public Origin getOriginRow() {
		return ((Source) getSource()).getOriginRow();
	}

	public Site getSiteRow() {
		return ((Receiver) getReceiver()).getSiteRow();
	}


	public double getDistance() {
		if (getSource() == null || getReceiver() == null)
			return Globals.NA_VALUE;

		return getSource().distance(getReceiver());
	}

	public double getDistanceDegrees() {
		if (getSource() == null || getReceiver() == null)
			return Globals.NA_VALUE;

		return getSource().distanceDegrees(getReceiver());
	}

	/**
	 * Retrieve points along the ray path. If requestedAttributes did not contain
	 * GeoAttributes.RAY_PATH, then this method returns null.
	 * 
	 * @return RayPath (a class that extends ArrayList<GeoVector>)
	 */
	public RayPath getRayPath() {
		return rayPath;
	}


	public void setRayPath(RayPath rayPath) {
		this.rayPath = rayPath;
	}

	/**
	 * Free the memory used to store the points along the rayPath and the slowness at those points. If
	 * that information is already null nothing happens.
	 */
	public void clearRayPath() {
		rayPath.clear();;
		slowPath = null;
	}

	/**
	 * Retrieve the slowness at all points along rayPath, seconds/km.
	 * 
	 * @return double[]
	 */
	public double[] getPathSlowness() {
		return slowPath;
	}

	/**
	 * A reference to the EnumMap containing GeoAttributes and computed values. The keySet will be a
	 * subset of all the GeoAttributes that are supported by a Predictor (see
	 * Predictor.getSupportedGeoAttributes(). The keySet will contain all the GeoAttributes that were
	 * requested of the Predictor object in a PredictionRequest object. Some of the results may be
	 * invalid. It will not contain any GeoAttributes that were not requested.
	 */
	public EnumMap<GeoAttributes, Double> getValues() {
		return values;
	}

	/**
	 * Return a bunch of junit assert statements, one for each requestedAttribute. If this prediction
	 * is invalid, returns "assertFalse(predition.isValid();"
	 * 
	 * @param typically this would be 'prediction';
	 * @return
	 */
	public String getAssertStatements(String variableName) {
		StringBuffer b = new StringBuffer();
		if (!isValid())
			b.append(String.format("assertFalse(%s.isValid());%n", variableName));
		else {
			b.append(String.format("assertTrue(%s.isValid());%n", variableName));
			b.append(String.format("assertEquals(\"%s\", %s.getRayType().toString());%n",
					getRayType().toString(), variableName));
			for (Entry<GeoAttributes, Double> e : values.entrySet())
				b.append(String.format("assertEquals(%1.6f, %s.getAttribute(GeoAttributes.%s), 1e-3);%n",
						e.getValue(), variableName, e.getKey().toString()));
		}
		return b.toString();

	}

	/**
	 * Retrieve any statusLog associated with this ray.
	 * 
	 * @return String
	 */
	public String getStatusLog() {
		return statusLog;
	}

	/**
	 * Set the statusLog string for this ray.
	 * 
	 * @param statusLog String
	 */
	public void setStatusLog(String statusLog) {
		if (statusLog.length() < maxStatusLogLength)
			this.statusLog = statusLog;
		else
			this.statusLog = String.format("%s%n%n..<original message size = %d>..%n%n%s",
					statusLog.substring(0, maxStatusLogLength / 2), statusLog.length(),
					statusLog.substring(statusLog.length() - maxStatusLogLength / 2));
	}

	/**
	 * Set the derivatives of travel time with respect to active node slowness.
	 * 
	 * @param derivs double[]
	 */
	protected void setActiveNodeDerivs(double[] derivs) {
		this.activeNodeDerivs = derivs;
	}

	/**
	 * Retrieve the derivatives of travel time with respect to active node slowness. Returns null if
	 * derivatives were never calculated.
	 * 
	 * @return double[]
	 */
	public double[] getActiveNodeDerivs() {
		return activeNodeDerivs;
	}

	/**
	 * Change the default spacing between GeoVectors returned by call to getRayPath(), in km. Default
	 * value is 10 km. If set to a value <= 0., then ray is not resampled; original points that define
	 * the ray will returned by getRayPath().
	 * 
	 * @param spacing double nodeSpacing in km
	 */
	static public synchronized void setRayPathNodeSpacing(double spacing) {
		nodeSpacing = spacing;
	}

	/**
	 * Integrates the input attribute over the path specified by this RayInfo object using the
	 * EarthShape and model attributes given in the input GeoTessModel.
	 * 
	 * @param model The model containing the attribute data and the EarthShape.
	 * @param attribute The attribute to be integrated.
	 * @return The path integral.
	 * @throws GeoTessException
	 */
	public double pathIntegral(GeoTessModel model, GeoAttributes attribute) throws GeoTessException {
		List<? extends GeoVector> geoVectors = getRayPath();
		double value = 0;
		double oldValue, nextValue;
		int attributeIndex = model.getMetaData().getAttributeIndex(attribute.name());
		GeoTessPosition node = GeoTessPosition.getGeoTessPosition(model);
		node.set(geoVectors.get(0).getUnitVector(), geoVectors.get(0).getRadius());
		oldValue = node.getValue(attributeIndex);
		for (int i = 1; i < geoVectors.size() - 1; ++i) {
			node.set(geoVectors.get(i).getUnitVector(), geoVectors.get(i).getRadius());
			nextValue = node.getValue(attributeIndex);
			value += geoVectors.get(i).distance3D(geoVectors.get(i - 1)) * (oldValue + nextValue) / 2;
			oldValue = nextValue;
		}
		return value;
	}

	public String getRayTypeString() {
		return rayTypeString;
	}

	public void setRayTypeString(String rayTypeString) {
		this.rayTypeString = rayTypeString;
	}

	/**
	 * Retrieve an array with all the active point indexes.
	 * 
	 * @param weights
	 * @return
	 */
	public static int[] getArrayIndexes(HashMapIntegerDouble weights) {
		int[] idx = new int[weights.size()];
		int[] i = new int[1];
		weights.forEach((pt, w) -> {
			idx[i[0]++] = pt;
		});
		return idx;
	}

	/**
	 * Retrieve an array with all the active point weights.
	 * 
	 * @param weights
	 * @return
	 */
	public static double[] getArrayWeights(HashMapIntegerDouble weights) {
		double[] wgt = new double[weights.size()];
		int[] i = new int[1];
		weights.forEach((pt, w) -> {
			wgt[i[0]++] = w;
		});
		return wgt;
	}

	/**
	 * Retrieve an array with all the active point indexes, spanning all waveTypes.
	 * 
	 * @param rayWeights
	 * @return
	 */
	public static int[] getArrayIndexes(Map<WaveType, HashMapIntegerDouble> rayWeights) {
		return getArrayIndexes(getCombinedRayWeights(rayWeights));
	}

	/**
	 * Retrieve an array with all the active point weights, spanning all waveTypes.
	 * 
	 * @param rayWeights
	 * @return
	 */
	public static double[] getArrayWeights(Map<WaveType, HashMapIntegerDouble> rayWeights) {
		return getArrayWeights(getCombinedRayWeights(rayWeights));
	}

	/**
	 * If tomography weights have been computed for the ray represented by rayWeights, return a map
	 * with the indexes and weights of all the geotess model points that were touched by the ray,
	 * including all wavetypes (P and S). If this ray path included segments of both wavetypes, a new
	 * HashMapInterDouble will be constructed. If only one wavetype was involved, a reference to the
	 * existing HashMapInterDouble will be returned. If rayWeights is empty, a new, empty
	 * HashMapInterDouble is returned.
	 * 
	 * @return
	 */
	public static HashMapIntegerDouble getCombinedRayWeights(
			Map<WaveType, HashMapIntegerDouble> tomoWeights) {
		if (tomoWeights == null || tomoWeights.isEmpty())
			return new HashMapIntegerDouble();

		if (tomoWeights.size() == 1)
			return tomoWeights.values().iterator().next();

		int size = 0;
		for (WaveType waveType : WaveType.values())
			size += tomoWeights.get(waveType).size();

		HashMapIntegerDouble newWeights = new HashMapIntegerDouble(size);

		for (WaveType waveType : WaveType.values())
			tomoWeights.get(waveType).forEach((pointIndex, weight) -> {
				newWeights.put(pointIndex, newWeights.get(pointIndex) + weight);
			});
		return newWeights;
	}

	/**
	 * Retrieve the weight assigned to inactive points in the model, distinguished by wavetype.
	 * 
	 * @return map may be empty but will not be null.
	 */
	public Map<WaveType, Double> getRayWeightsInactive() {
		return rayWeightsInactive;
	}

	/**
	 * Retrieve the weight assigned to inactive points in the model, distinguished by wavetype.
	 * 
	 * @return map may be empty but will not be null.
	 */
	public double getRayWeightsInactiveAll() {
		double sum = 0;
		for (Double v : rayWeightsInactive.values())
			sum += v;
		return sum;
	}

	/**
	 * Retrieve the weight assigned to inactive points in the model
	 * 
	 * @return map may be empty but will not be null.
	 */
	public double getRayWeightsInactive(WaveType wavetype) {
		return rayWeightsInactive.get(wavetype) == null ? 0 : rayWeightsInactive.get(wavetype);
	}

	/**
	 * This method is called by Predictors that have populated this Prediction's rayPath with
	 * GeoVectors. Given a model, it will compute rayWeights, which will include the indexes and
	 * weights of all the active points in the model touched by the ray, distinguished by WaveType.
	 * Weights assigned to inactive points will be summed and added to rayWeightInactive.
	 * <p>
	 * To iterate over tomo_weights, use this sample code:
	 * 
	 * <pre>
	 * {@code 
	 * double travel_time = 0.;
	 * for (Entry<WaveType, HashMapIntegerDouble> e : prediction.getRayWeights().entrySet()) {
    WaveType waveType = e.getKey();
    GeoAttributes attribute = waveType.getAttribute();
    int attributeIndex = model3d.getMetaData().getAttributeIndex(attribute.toString());

    if (attributeIndex < 0)
    	throw new Exception("3D model does not support wavetype "+waveType.toString());

    HashMapIntegerDouble weights = e.getValue();
    HashMapIntegerDouble.Iterator it = weights.iterator();
    while (it.hasNext()) {
    	HashMapIntegerDouble.Entry ite = it.nextEntry();
    	int pointIndex = ite.getKey();
    	double weight = ite.getValue();
    	travel_time += model3d.getPointMap().getPointValueDouble(pointIndex, attributeIndex) * weight;
    }
	 * </pre>
	 * 
	 * @param model
	 * 
	 * @param requestedAttributes
	 * @throws Exception
	 */
	public void processRayPath(GeoTessModel model, EnumSet<GeoAttributes> requestedAttributes)
			throws Exception {
		// if derived class did not populate the rayPath, this method can't do anything.
		if (rayPath == null)
			return;

		GeoTessPosition pos = model.getGeoTessPosition();

		if (requestedAttributes.contains(GeoAttributes.TOMO_WEIGHTS)
				|| requestedAttributes.contains(GeoAttributes.ACTIVE_FRACTION)) {
			rayWeights.clear();
			rayWeightsInactive.clear();

			for (WaveType waveType : WaveType.values()) {
				HashMapIntegerDouble weights = getWeights(pos, rayPath, waveType);
				if (weights.size() > 0) {
					// remove the entry for inactive nodes and add weight of inactive nodes to
					// rayWeightsInactive.
					HashMapIntegerDouble.Entry e = weights.remove(-1);
					rayWeightsInactive.put(waveType, e == null ? 0. : e.getValue());
					rayWeights.put(waveType, weights);
				}
			}

			if (requestedAttributes.contains(GeoAttributes.ACTIVE_FRACTION)) {
				double[] active = new double[] {0.};
				double inactive = 0.;

				for (WaveType wavetype : rayWeights.keySet()) {
					rayWeights.get(wavetype).forEach((pointIndex, weight) -> {
						active[0] += weight;
					});
					inactive += rayWeightsInactive.get(wavetype);

					if (active[0] + inactive > 0)
						setAttribute(GeoAttributes.ACTIVE_FRACTION, active[0] / (active[0] + inactive));
					else
						setAttribute(GeoAttributes.ACTIVE_FRACTION, Globals.NA_VALUE);
				}

				// if activefraction was requested but not tomoweights, the discard tomoweights
				if (!requestedAttributes.contains(GeoAttributes.TOMO_WEIGHTS)) {
					rayWeights.clear();
					rayWeightsInactive.clear();
				}
			}
		}

		if (requestedAttributes.contains(GeoAttributes.DTT_DSLOW)) {
			slowPath = new double[rayPath.size()];

			for (int i = 0; i < rayPath.size(); ++i) {
				GeoVectorRay v = (GeoVectorRay) rayPath.get(i);
				GeoAttributes attr =
						(WaveType.P == getWaveType() ? GeoAttributes.PSLOWNESS : GeoAttributes.SSLOWNESS);
				int attrIndx = model.getMetaData().getAttributeIndex(attr.name());
				pos.set(v.getLayerIndex(), v.getUnitVector(), v.getRadius());
				slowPath[i] = pos.getValue(attrIndx);
			}
		}

		// if GeoAttributes.RAY_PATH was not requested, clear it.
		if (!requestedAttributes.contains(GeoAttributes.RAY_PATH))
			rayPath.clear();
	}

	/**
	 * Compute the weights on each model point that results from interpolating positions along the
	 * specified ray path.
	 * 
	 * @param pos a GeoTessPosition object that will be used to obtain interpolation coefficients from
	 *        the model.
	 * @param vectors an ordered list GeoVectorRay objects that define a ray path.
	 * @param waveType only segments of the raypath that have waveType equal to this value will be
	 *        considered. waveType must be one of PSLOWNESS or SSLOWNESS.
	 * @return weights a map from the pointIndex of a point in the model to the weight that accrued to
	 *         that point from the ray path. The sum of all the weights in the map will equal the
	 *         length in km of the portion of the ray where wavetype is equal to specified value.
	 *         Might be empty but will not be null.
	 * @throws GeoTessException
	 */
	private HashMapIntegerDouble getWeights(GeoTessPosition pos, ArrayList<GeoVector> vectors,
			WaveType waveType) throws GeoTessException {
		HashMapIntegerDouble weights = new HashMapIntegerDouble(100);

		GeoVectorRay vn, v = (GeoVectorRay) vectors.get(0);
		double[] pn, p = v.getVector();
		double dn, dp = 0;

		for (int i = 0; i < vectors.size() - 1; ++i) {
			vn = (GeoVectorRay) vectors.get(i + 1);
			pn = vn.getVector();
			dn = Vector3D.distance3D(p, pn) / 2;

			if (v.getWaveType() == waveType)
				pos.set(v.getLayerIndex(), v.getUnitVector(), v.getRadius()).getWeights(weights, dp + dn);

			v = vn;
			p = pn;
			dp = dn;
		}

		if (v.getWaveType() == waveType)
			pos.set(v.getLayerIndex(), v.getUnitVector(), v.getRadius()).getWeights(weights, dp);

		return weights;
	}

	public String getPredictorVersion() {
		return predictorVersion;
	}

	public void setPredictorVersion(String predictorVersion) {
		this.predictorVersion = predictorVersion;
	}


	/**
	 * Map from TRAVEL_TIME, AZIMUTH, SLOWNESS to UncertaintyType
	 */
	public EnumMap<GeoAttributes, UncertaintyType> getUncertaintyTypes() {
		return uncertaintyTypes;
	}

	/**
	 * 
	 * @param attribute one of GeoAttributes.TRAVEL_TIME, GeoAttributes.AZIMUTH, GeoAttributes.SLOWNESS
	 * @return one of DISTANCE_DEPENDENT, PATH_DEPENDENT, STATION_PHASE_DEPENDENT, SOURCE_DEPENDENT, LIBCORR3D
	 */
	public UncertaintyType getUncertaintyType(GeoAttributes attribute) {
		return uncertaintyTypes.get(attribute);
	}

	/**
	 * 
	 * @return one of DISTANCE_DEPENDENT, PATH_DEPENDENT, STATION_PHASE_DEPENDENT, SOURCE_DEPENDENT, LIBCORR3D
	 */
	public UncertaintyType getUncertaintyTypeTT() {
		return uncertaintyTypes.get(GeoAttributes.TRAVEL_TIME);
	}
	/**
	 * 
	 * @return one of DISTANCE_DEPENDENT, PATH_DEPENDENT, STATION_PHASE_DEPENDENT, SOURCE_DEPENDENT, LIBCORR3D
	 */
	public UncertaintyType getUncertaintyTypeAZ() {
		return uncertaintyTypes.get(GeoAttributes.AZIMUTH);
	}

	/**
	 * 
	 * @return one of DISTANCE_DEPENDENT, PATH_DEPENDENT, STATION_PHASE_DEPENDENT, SOURCE_DEPENDENT, LIBCORR3D
	 */
	public UncertaintyType getUncertaintyTypeSH() {
		return uncertaintyTypes.get(GeoAttributes.SLOWNESS);
	}

	public TestBuffer getTestBuffer() {
		TestBuffer buffer = new TestBuffer(this.getClass().getSimpleName());
		buffer.add("predictorType", predictorType.name());
		buffer.add("predictorName", predictorName);
		buffer.add("modelName", modelName);
		buffer.add("rayType", rayType.name());
		
		if (values != null) {
			TreeMap<String, Double> pd = new TreeMap<>();
			if (values != null)
				for (Entry<GeoAttributes, Double> e : values.entrySet())
					if (e.getValue() != Globals.NA_VALUE)
						pd.put(e.getKey().name(), e.getValue());

			for (Entry<String, Double> e : pd.entrySet())
				buffer.add(e.getKey(), e.getValue());
		}
		buffer.add();

		return buffer;
	}
	
	static public TestBuffer getTestBuffer(EnumMap<GeoAttributes, Double> values) {
		TestBuffer buffer = new TestBuffer("predictions");
		if (values != null) {
			TreeMap<String, Double> pd = new TreeMap<>();
			if (values != null)
				for (Entry<GeoAttributes, Double> e : values.entrySet())
					if (e.getKey() != GeoAttributes.CALCULATION_TIME && e.getValue() != Globals.NA_VALUE)
						pd.put(e.getKey().name(), e.getValue());

			for (Entry<String, Double> e : pd.entrySet())
				buffer.add(e.getKey(), e.getValue());
			buffer.add();
		}
		return buffer;
	}

}
