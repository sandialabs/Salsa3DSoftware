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

import static gov.sandia.gmp.util.globals.Globals.TWO_PI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;

/**
 * Concrete implementation of a PredictionRequest object. This is
 * basically a container class to group together all the information needed to
 * compute a Prediction including:
 * <ul>
 * <li>a Source,
 * <li>a Receiver,
 * <li>a SeismicPhase
 * <li>a set of requested attributes
 * </ul>
 * It also has reference to a PredictionInterface object that will contain the
 * Prediction if/when it is computed.
 * <p>
 * Note that there are no data stored here. Therefore residuals cannot be
 * computed. Class Observation extends PredictionRequest and stores typical
 * seismic data and residuals.
 * 
 * @author sballar
 * 
 */
public class PredictionRequest implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4185610227981371455L;

	protected Source source;

	protected Receiver receiver;

	protected SeismicPhase phase;

	protected EnumSet<GeoAttributes> requestedAttributes;

	protected EnumMap<GeoAttributes, Double> auxiliaryInformation = 
			new EnumMap<GeoAttributes, Double>(GeoAttributes.class);

	/**
	 * The period of an observed signal in seconds.  Used in calculation of surface wave travel time.
	 */
	private double period = Double.NaN;

	/**
	 * If a PredictionisDefining() returns false then the Predictor should
	 * refrain from computing a prediction and should instead return a
	 * PredictionInterface object with isValid() false, all data set to
	 * BaseConst.NA_VALUE, and errorMessage = ""PredictionRequest was non-defining".
	 * 
	 * @return
	 */
	protected boolean isDefining;
	
	/**
	 * Optional parameter. If this PredictionRequest is supposed to predict values
	 * that correspond to an Observation, then this is the observation id. Can be
	 * set to -1L.
	 */
	protected long observationId;

	public PredictionRequest() {
	}

	public PredictionRequest(PredictionRequest request) {
	    this.isDefining = request.isDefining;
	    this.observationId = request.observationId;
	    this.phase = request.phase;
	    this.receiver = request.receiver;
	    this.requestedAttributes = request.requestedAttributes.clone();
	    this.source = request.source;
	}

	public PredictionRequest(long observationId, Receiver receiver, Source source, SeismicPhase phase,
			EnumSet<GeoAttributes> requestedAttributes, boolean isDefining) throws Exception {
		this.observationId = observationId;
		this.source = source;
		this.receiver = receiver;
		this.phase = phase;
		this.requestedAttributes = requestedAttributes.clone();
		this.isDefining = isDefining;
	}


	public PredictionRequest(Receiver receiver, Source source, SeismicPhase phase,
			EnumSet<GeoAttributes> requestedAttributes, boolean isDefining) throws Exception {
		this(-1L, receiver, source, phase, requestedAttributes, isDefining);
	}

	public PredictionRequest(Receiver receiver, Source source, SeismicPhase phase) throws Exception {
		this(-1L, receiver, source, phase, EnumSet.of(GeoAttributes.TRAVEL_TIME), true);
	}

	public PredictionRequest(AssocExtended assoc, EnumSet<GeoAttributes> requestedAttributes, boolean isDefining)
			throws Exception {
		this(assoc.getArid(), new Receiver(assoc.getSite()), new Source(assoc.getOrigin()),
				SeismicPhase.valueOf(assoc.getPhase()), requestedAttributes, isDefining);
	}

	/**
	 * @return the phase
	 */
	public SeismicPhase getPhase() {
		return phase;
	}

	/**
	 * @param phase the phase to set
	 * @throws Exception 
	 */
	public void setPhase(SeismicPhase phase) throws Exception {
		this.phase = phase;
	}

	/**
	 * @return the source
	 */
	public Source getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 * @throws Exception 
	 */
	public void setSource(Source source) throws Exception {
		this.source = source;
	}

	/**
	 * @return the receiver
	 */
	public Receiver getReceiver() {
		return receiver;
	}

	/**
	 * @param receiver the receiver to set
	 * @throws Exception 
	 */
	public void setReceiver(Receiver receiver) throws Exception {
		this.receiver = receiver;
	}

	/**
	 * If a PredictionRequest is not defining, then the Predictor should refrain
	 * from computing a prediction and should instead return a PredictionInterface
	 * object with isValid() false and all data set to BaseConst.NA_VALUE
	 * 
	 * @return defining
	 */
	public boolean isDefining() {
		return isDefining;
	}

	/**
	 * If a PredictionRequest is not defining, then the Predictor should refrain
	 * from computing a prediction and should instead return a PredictionInterface
	 * object with isValid() false and all data set to BaseConst.NA_VALUE
	 * 
	 * @param isDefining the isDefining to set
	 */
	public void setDefining(boolean isDefining) {
		this.isDefining = isDefining;
	}

	/**
	 * @return the requestedAttributes
	 */
	public EnumSet<GeoAttributes> getRequestedAttributes() {
		return requestedAttributes;
	}

	/**
	 * Specify the set of GeoAttributes owned by this PredictionRequest.
	 * This PredictionRequests makes a cloned copy of the specified 
	 * requestedAttributes.
	 * @param requestedAttributes the requestedAttributes to set
	 */
	public void setRequestedAttributes(EnumSet<GeoAttributes> requestedAttributes) {
		this.requestedAttributes = requestedAttributes.clone();
	}

	/**
	 * Add the specified GeoAttribute to the set of requested attributes
	 * 
	 * @param attribute
	 */
	public void addRequestedAttribute(GeoAttributes attribute) {
		requestedAttributes.add(attribute);
	}

	public long getObservationId() {
		return observationId;
	}

	/**
	 * 
	 * @return sourceId
	 */
	public long getSourceId() {
		return source == null ? -1L : source.getSourceId();
	}

	/**
	 * 
	 * @return evid
	 */
	public long getEvid() {
		return source.getEvid();
	}

	@Override
	public String toString() {
		return String.format("%8d %8d %s %s %s", -1, getObservationId(), getReceiver(), getSource(),
				getPhase());
	}
	
	public String getString() {
	    return String.format("source: eventid= %d, sourceid= %d, lat,lon,depth = %s, %1.3f%n"
	    	+ "receiver: %s, lat,lon,elev= %s, %1.3f, ondate,offdate= %d, %d%n"
	    	+ "obsid= %d, phase= %s, distance= %1.3f, seaz= %1.3f, esaz= %1.3f%n",
	    	source.getEvid(), source.getSourceId(),
		    VectorGeo.getLatLonString(source.getUnitVector(), "%9.5f, %10.5f"), source.getDepth(),
		    receiver.getSta(),
		    VectorGeo.getLatLonString(receiver.getUnitVector(), "%9.5f, %10.5f"), receiver.getElev(),
		    receiver.getOndate(), receiver.getOffdate(),
		    observationId, phase.toString(),
		    getDistanceDegrees(), Math.toDegrees(getSeaz()), Math.toDegrees(getEsaz()));
		    

	}
	
	public String toStringOneLiner() {
	  return new StringBuilder(getClass().getCanonicalName())
	      .append("[requestId=").append(-1)
	      .append(",observationId=").append(observationId)
	      .append(",isDefining=").append(isDefining)
	      .append(",phase=").append(phase)
	      .append(",sourceId=").append(source.getSourceId())
	      .append(",receiverId=").append(receiver.getReceiverId())
	      .append(",requestedAttributes=").append(requestedAttributes)
	      .append("]").toString();
	}

	public void setObservationId(long observationId) {
		this.observationId = observationId;
	}

	/**
	 * Source-receiver distance in radians
	 * @return
	 */
	public double getDistance() {
		return source.distance(receiver);
	}

	public double getDistanceDegrees() {
		return Math.toDegrees(getDistance());
	}

	/**
	 * Source-to-receiver azimuth, in radians. Range is 0 to 2*PI but will return
	 * NaN if source and receiver are colocated or if source is at north or south
	 * pole.
	 * 
	 * @return esaz in radians
	 */
	public double getEsaz() {
		return (source.azimuth(receiver, Double.NaN) + TWO_PI) % TWO_PI;
	}

	/**
	 * Receiver-to-source azimuth, in radians. Range is 0 to 2*PI but will return
	 * NaN if source and receiver are colocated or if receiver is at north or south
	 * pole.
	 * 
	 * @return seaz in radians
	 */
	public double getSeaz() {
		return (receiver.azimuth(source, Double.NaN) + TWO_PI) % TWO_PI;
	}

	/**
	 * Source-to-receiver azimuth, in radians. Range is 0 to 2*PI. If answer is NaN,
	 * returns errorValue
	 * 
	 * @return azimuth in radians
	 */
	public double getEsaz(double errorValue) {
		double a = getEsaz();
		return Double.isNaN(a) ? errorValue : a;
	}

	/**
	 * Receiver-to-source azimuth, in radians. Range is 0 to 2*PI If answer is NaN,
	 * returns errorValue
	 * 
	 * @return seaz
	 */
	public double getSeaz(double errorValue) {
		double a = getSeaz();
		return Double.isNaN(a) ? errorValue : a;
	}


	public static ArrayList<PredictionRequest> getRequests(Collection<? extends Receiver> receivers,
			Collection<? extends Source> sources, Collection<SeismicPhase> phases,
			EnumSet<GeoAttributes> requestedAttributes) throws Exception {
		ArrayList<PredictionRequest> requests = new ArrayList<PredictionRequest>(sources.size());
		Iterator<? extends Receiver> itReceiver = receivers.iterator();
		Iterator<? extends Source> itSource = sources.iterator();
		Iterator<SeismicPhase> itPhase = phases.iterator();
		while (itSource.hasNext())
			requests.add(new PredictionRequest(itReceiver.next(), itSource.next(), itPhase.next(), requestedAttributes,
					true));
		return requests;
	}

	public static ArrayList<PredictionRequest> getRequests(ArrayList<GeoVector> receivers,
			ArrayList<GeoVector> sources, ArrayList<SeismicPhase> phases, EnumSet<GeoAttributes> requestedAttributes)
					throws Exception {
		ArrayList<PredictionRequest> requests = new ArrayList<PredictionRequest>(sources.size());
		Iterator<GeoVector> itReceiver = receivers.iterator();
		Iterator<GeoVector> itSource = sources.iterator();
		Iterator<SeismicPhase> itPhase = phases.iterator();
		while (itSource.hasNext())
			requests.add(new PredictionRequest(new Receiver(itReceiver.next()), new Source(itSource.next()),
					itPhase.next(), requestedAttributes, true));
		return requests;
	}

	/**
	 * The period of an observed signal in seconds.  Used in calculation of surface wave travel time.
	 * @return
	 */
	public double getPeriod() {
		return period;
	}

	/**
	 * The period of an observed signal in seconds.  Used in calculation of surface wave travel time.

	 * @param period
	 * @return 
	 */
	public PredictionRequest setPeriod(double period) {
		this.period = period;
		return this;
	}

//	/**
//	 * A place to store auxiliary information needed by some Predictors in order to compute Predictions.
//	 * For example, the SurfaceWavePredictor needs to know a period; LookupTableGMP needs a slowness value
//	 * to compute FK_DISTANCE, etc.
//	 * @return
//	 */
//	public EnumMap<GeoAttributes, Double> getAuxiliaryInformation() {
//		return auxiliaryInformation;
//	}
//	
//	/** 
//	 * Add a piece of auxiliary information to this request.  Some Predictors require extra information
//	 * to compute predictions and this is a way to provide it.
//	 * @param attribute
//	 * @param value
//	 * @return a reference to this.
//	 */
//	public PredictionRequest addAuxiliaryInformation(GeoAttributes attribute, Double value) {
//		auxiliaryInformation.put(attribute, value);
//		return this;
//	}
//
//	/**
//	 * Retrieve a piece of auxiliary information from this PredictionRequest.
//	 * @param attribute
//	 * @return Double value associated with the specified attribute.  Will return null
//	 * if the information is not available.
//	 */
//	public Double getAuxiliaryInformation(GeoAttributes attribute) {
//		return auxiliaryInformation.get(attribute);
//	}

  /*@Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(source);
    out.writeObject(receiver);
    out.writeObject(phase);
    out.writeObject(requestedAttributes);
    out.writeBoolean(isDefining);
    out.writeLong(observationId);
    out.writeInt(requestId);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    source = (Source)in.readObject();
    receiver = (Receiver)in.readObject();
    phase = (SeismicPhase)in.readObject();
    requestedAttributes = (EnumSet<GeoAttributes>)in.readObject();
    isDefining = in.readBoolean();
    observationId = in.readLong();
    requestId = in.readInt();
  }*/

}
