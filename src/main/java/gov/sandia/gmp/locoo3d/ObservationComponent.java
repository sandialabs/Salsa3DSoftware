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
package gov.sandia.gmp.locoo3d;

import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;

/**
 * <p>
 * Title: LocOO
 * </p>
 * 
 * <p>
 * Description: Seismic Event Locator
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
public abstract class ObservationComponent implements Serializable {
  private static final long serialVersionUID = 1L;

  protected Arrival arrival;

  /**
   * At the start of a location calculation, definingNow is equal to
   * Observation.[timedef|azdef|slodef] but can change each iteration if the prediction is invalid
   * for some reason.
   */
  private boolean definingNow;

  /**
   * true if observation was defining at the start of the previous iteration.
   */
  private boolean wasDefining;

  /**
   * True if the predicted values needed by the locator are currently valid. It is set in
   * setPrediction() and nowhere else.
   */
  private boolean predictionValid;

  private String predictionErrorMessage;

  private double[] derivatives;

  private double weight;

  private double weightedResidual;

  private double[] weightedDerivatives;

  /**
   * count the number of times definingNow has flipped from definingNow to non-definingNow.
   */
  private int flipFlop;

  private String staPhaseType = null;

  protected double masterEventCorrection;

  private static final String[] obsComponent =
      new String[] {"latitude", "longitude", "depth", "time"};

  /**
   * 
   * @param arrival Arrival
   */
  protected ObservationComponent(Arrival arrival) {
    this.arrival = arrival;
    predictionValid = false;
    definingNow = false;
    wasDefining = false;
    weight = weightedResidual = Globals.NA_VALUE;
    arrival.addObservation(this);
  }

  /**
   * return TRAVEL_TIME, AZIMUTH or SLOWNESS
   * 
   * @return GeoAttributes
   */
  abstract public GeoAttributes getObsType();

  /**
   * Returns TT, AZ or SH
   * 
   * @return
   */
  abstract public String getObsTypeShort();

  /**
   * GeoAttributes.TT_MODEL_UNCERTAINTY or GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY or
   * GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY
   * 
   * @return GeoAttributes
   */
  abstract public GeoAttributes getObsUncertaintyType();

  /**
   * GeoAttributes.TT_MODEL_UNCERTAINTY or GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY or
   * GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY
   * 
   * @return GeoAttributes
   */
  abstract public GeoAttributes getModelUncertaintyType();

  /**
   * GeoAttributes.TT_BASEMODEL or GeoAttributes.AZ_BASEMODEL or GeoAttributes.SH_BASEMODEL
   * 
   * @return GeoAttributes
   */
  abstract public GeoAttributes getBaseModelType();

  /**
   * 
   * @return
   */
  abstract public boolean useModelUncertainty();

  /**
   * Retrieve the set of attributes needed to support the current location calculation.
   * 
   * @return
   */
  protected void addRequiredAttributes(EnumSet<GeoAttributes> attributes, boolean needDerivatives) {
    attributes.add(getObsType());
    if (useModelUncertainty())
      attributes.add(getModelUncertaintyType());
    if (usePathCorr())
      attributes.add(getPathCorrType());
    if (needDerivatives) {
      if (arrival.getEventParameters().isFree(GMPGlobals.LAT)) {
        attributes.add(DObs_DLAT());
        attributes.add(DObs_DLON());
      }
      if (arrival.getEventParameters().isFree(GMPGlobals.DEPTH))
        attributes.add(DObs_DR());
      if (arrival.getEventParameters().isFree(GMPGlobals.TIME))
        attributes.add(DObs_DTIME());
    }
  }

  /**
   * Retrieved the observed value of travel time (sec), azimuth (radians) or slowness (sec/radian).
   * 
   * @return double
   */
  abstract public double getObserved();

  /**
   * Retrieved the uncertainty of the observed value of travel time (sec), azimuth (radians) or
   * slowness (sec/radian). In the database, these are deltim, delslo and delaz.
   * 
   * @return double
   */
  abstract public double getObsUncertainty();

  /**
   * Original value of Observation.[timedef|azdef|slodef]. This is the value read from the database
   * and cannot be changed (it is final).
   * 
   * @return boolean
   */
  abstract public boolean isDefiningOriginal();

  /**
   * Current value of Observation.[timedef|azdef|slodef]. At the beginning of a location
   * calculation, this value might be modified from original value in order to reflect property file
   * settings, predictor support, etc.
   * 
   * @return boolean
   */
  abstract public boolean isDefining();

  /**
   * At the start of a location calculation, this is equal to isDefining() but can change each
   * iteration if the prediction is invalid for some reason.
   * 
   * @return
   */
  public boolean isDefiningNow() {
    return definingNow;
  }

  abstract public char getDefiningChar();

  /**
   * Returns true if the observed value and its uncertainty are not NA.
   * 
   * @return boolean
   */
  public boolean isValid() {
    return getObserved() != Globals.NA_VALUE && getObsUncertainty() != Globals.NA_VALUE;
  }

  /**
   * Returns true if the predictor is capable of computing a predicted value model uncertainty and
   * derivatives. The boolean value that backs this call was set in the ObservationComponent
   * constructor and is final.
   * 
   * @return boolean
   * @throws GeoVectorException
   */
  abstract public boolean isSupported();

  /**
   * Returns true if all of the predicted values for this observation are currently valid. Set in
   * setPrediciton() and nowhere else.
   * 
   * @return
   */
  public boolean isPredictionValid() {
    return predictionValid;
  }

  public String getPredictionErrorMessage() {
    return predictionErrorMessage;
  }

  abstract public double getPathCorrection();

  abstract public double getEllipticityCorrection();

  abstract public double getElevationCorrection();

  abstract public double getElevationCorrectionAtSource();

  abstract public double getSiteCorrection();

  abstract public double getSourceCorrection();

  /**
   * retrieves the set of geoattributes that need to be specified in order to compute derivatives of
   * the observation component with respect to source position.
   * 
   * @return
   */
  abstract protected GeoAttributes[] getDerivAttributes();

  abstract protected GeoAttributes DObs_DLAT();

  abstract protected GeoAttributes DObs_DLON();

  abstract protected GeoAttributes DObs_DR();

  abstract protected GeoAttributes DObs_DTIME();

  /**
   * Convert value from internal to output units
   * 
   * @param value
   * @return
   */
  abstract protected double toOutput(double value);


  abstract public void setDefining(boolean defining);

  /**
   * Set to defining. This is the local version that does not change Observation.isDefining().
   * Returns true if status changed.
   */
  protected boolean setDefiningNow(boolean defining) {
    boolean flipped = definingNow != defining;

    if (definingNow && !defining)
      ++flipFlop;
    definingNow = defining;
    return flipped;
  }

  public Arrival getArrival() {
    return arrival;
  }

  /**
   * 
   * @return HashSet
   */
  protected HashMap<GeoAttributes, ObservationComponent> getSiblings() {
    return arrival.getObservations();
  }

  /**
   * 
   * @return double
   * @throws GeoVectorException
   */
  public double getPredicted() {
    if (isPredictionValid())
      return arrival.getPrediction().getAttribute(getObsType())
          + (masterEventCorrection == -999. ? 0 : masterEventCorrection);
    return Globals.NA_VALUE;
  }

  /**
   * 
   * @return Long
   */
  public Long getSourceid() {
    return arrival.getSource().getSourceId();
  }

  /**
   * 
   * @return Long
   */
  public Long getEvid() {
    return arrival.getSource().getEvid();
  }

  /**
   * 
   * @return Long
   */
  public Long getObservationid() {
    return arrival.getObservationId();
  }

  /**
   * 
   * @return Site
   */
  public Receiver getReceiver() {
    return arrival.getReceiver();
  }

  /**
   * 
   * @return SeismicPhase
   */
  public SeismicPhase getPhase() {
    return arrival.getPhase();
  }

  /**
   * 
   * @return double
   */
  public double getEsaz() {
    return arrival.getEsaz();
  }

  /**
   * 
   * @return double
   */
  public double getSeaz() {
    return arrival.getSeaz();
  }

  /**
   * Return observed value minus prediction.
   * 
   * @return double
   * @throws GeoVectorException
   */
  public double getResidual() {
    // this method works for tt and sh, but is overriden by az
    // to ensure that azimuth residual is in range [-PI, PI].
    return isPredictionValid() ? getObserved() - getPredicted() : Globals.NA_VALUE;
  }

  /**
   * 
   * @return double
   * @throws GeoVectorException
   */
  public double getWeight() {
    return weight;
  }

  public void setWeightedResidual(double weightedResidual) {
    this.weightedResidual = weightedResidual;
    weight = weightedResidual / getResidual();
  }

  /**
   * 
   * @return double
   * @throws GeoVectorException
   */
  public double getWeightedResidual() {
    return weightedResidual;
  }

  /**
   * getWeightedDerivatives
   * 
   * @return double[]
   */
  public double[] getWeightedDerivatives() {
    return weightedDerivatives;
  }

  /**
   * getTotalError
   * 
   * @return double
   */
  public double getTotalUncertainty() {
    double u = getObsUncertainty();
    if (u == Globals.NA_VALUE || !useModelUncertainty())
      return u;
    double e = getModelUncertainty();
    if (e == Globals.NA_VALUE)
      return e;
    return sqrt(u * u + e * e);
  }

  /**
   * getModelError
   * 
   * @return int
   */
  public double getModelUncertainty() {
    if (arrival.isPredictionUpToDate())
      return arrival.getPrediction().getAttribute(getModelUncertaintyType());
    return Globals.NA_VALUE;
  }

  /**
   * getModelError
   * 
   * @return int
   */
  public double getBaseModel() {
    if (arrival.isPredictionUpToDate())
      return arrival.getPrediction().getAttribute(getBaseModelType());
    return Globals.NA_VALUE;
  }

  /**
   * usePathCorr
   * 
   * @return boolean
   */
  public abstract boolean usePathCorr();


  public abstract GeoAttributes getPathCorrType();

  /**
   * getSPA
   * 
   * @return String[]
   */
  public String getSPA() {
    return String.format("%-8s %-6s %2s", getReceiver().getSta(), getPhase().toString(),
        getObsTypeShort());
  }

  /**
   * 
   * @return
   */
  public String getSta() {
    return getReceiver().getSta();
  }

  /**
   * Derivatives with respect to lat, lon, depth and time. Units are xx/km, xx/km, xx/km and
   * xx/second.
   * 
   * @return double[]
   */
  public double[] getDerivatives() {
    return derivatives;
  }
  
  /**
   * Given a new Prediction object, extract all the information from it that is relevant to this
   * observation, including the predicted value, the model uncertainty, the derivatives, etc.
   * <p>
   * The boolean returned by isPredictionValid() is set in this method. It is the only place in the
   * code where predictionValid is set.
   * 
   * @param prediction
   */
  protected void setPrediction(Prediction prediction) {
    if (prediction.getRayType() == RayType.ERROR)
      arrival.getEventParameters().errorLog().writeln(prediction.getErrorMessage());

    weight = Globals.NA_VALUE;
    weightedResidual = Globals.NA_VALUE;
    derivatives = null;
    weightedDerivatives = null;

    predictionValid = arrival.getPrediction().isValid()
        && arrival.getPrediction().getAttribute(getObsType()) != Globals.NA_VALUE
        && (!useModelUncertainty() || getModelUncertainty() > 0.)
        && getObserved() != Globals.NA_VALUE && getObsUncertainty() > 0.;

    if (!predictionValid) {
      if (!arrival.getPrediction().isValid())
        predictionErrorMessage = prediction.getErrorMessage();
      else if (arrival.getPrediction().getAttribute(getObsType()) == Globals.NA_VALUE)
        predictionErrorMessage = String.format("Predicted %s is == Globals.NA_VALUE", getObsType());
      else if ((!useModelUncertainty() || getModelUncertainty() > 0.) == false)
        predictionErrorMessage = String.format("%s model uncertainty is <= 0.", getObsType());
      else if (getObserved() == Globals.NA_VALUE)
        predictionErrorMessage =
            String.format("%s observed value == Globals.NA_VALUE", getObsType());
      else if (getObsUncertainty() <= Globals.NA_VALUE)
        predictionErrorMessage = String.format("%s observation uncertainty <= 0.", getObsType());
      return;
    } else
      predictionErrorMessage = "";

    if (useModelUncertainty())
      weight = 1. / sqrt(getModelUncertainty() * getModelUncertainty()
          + getObsUncertainty() * getObsUncertainty());
    else
      weight = 1. / getObsUncertainty();

    weightedResidual = getResidual() * weight;

    // get derivatives if needed
    if (arrival.getEventParameters().needDerivatives()) {
      // this returns all 4 derivatives: (wrt lat, lon, depth, time).
      derivatives = arrival.getPrediction().getAttributes(getDerivAttributes());

      weightedDerivatives = new double[4];
      Arrays.fill(weightedDerivatives, Globals.NA_VALUE);

      for (int component = 0; component < 4; ++component) {
        if (arrival.getEventParameters().isFree(component)) {
          if (derivatives[component] == Globals.NA_VALUE
              || Double.isInfinite(derivatives[component])
              || Double.isNaN(derivatives[component])) {
            predictionValid = false;
            weightedDerivatives[component] = Globals.NA_VALUE;
            predictionErrorMessage = String.format("derivative wrt %s is invalid %f",
                obsComponent[component], derivatives[component]);
          } else {
            switch (component) {
              case GMPGlobals.LAT:
                // convert from xx per radian to xx per km
                derivatives[component] /= arrival.getSource().getRadius();
                break;
              case GMPGlobals.LON:
                // convert from xx per radian to xx per km
                derivatives[component] /= arrival.getSource().getRadius();
                break;
              case GMPGlobals.DEPTH:
                // convert from deriv wrt radius to deriv wrt depth
                derivatives[component] = -derivatives[component];
                break;
              case GMPGlobals.TIME:
                // no conversion necessary
                break;
              default:
                // never happens
                break;
            }
            weightedDerivatives[component] = derivatives[component] * weight;
          }
        }
      }
    }
  }

  public String observationString(Predictor predictor) {
    StringBuffer cout = new StringBuffer();
    try {
      cout.append(String.format("%12d %-6s %-6s %4s %2s  %9.4f %12.4f %12.4f  ", getObservationid(),
          getReceiver().getSta(), getPhase().toString(), getObsTypeShort(),
          // (isDefiningNow() ? " *" : " "),
          getDefiningChar(), arrival.getDistanceDegrees(), toOutput(getObserved()),
          toOutput(getObsUncertainty())));

      if (predictor == null)
        cout.append("No Predictor for this Arrival");
      else
        cout.append(String.format("%s.%s %s.%s %s", predictor.getPredictorName(),
            predictor.getPredictorVersion(),
            predictor.getUncertaintyInterface().getUncertaintyType(),
            predictor.getUncertaintyInterface().getUncertaintyVersion(), predictor
                .getUncertaintyInterface().getUncertaintyModelFile(arrival, getObsTypeShort())));
    } catch (Exception e) {
      return e.getMessage() + "\n" + GMPException.getStackTraceAsString(e);
    }
    return cout.toString();
  }

  public String obsIterationString() {
    StringBuffer cout = new StringBuffer();
    try {
      cout.append(String.format(
          "%10d %-6s %-6s %4s %2s  %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f %12.3f %12.2f %12.2f",
          getObservationid(), getReceiver().getSta(), getPhase().toString(), getObsTypeShort(),
          (isDefiningNow() ? " *" : "  "), toOutput(getObserved()), toOutput(getObsUncertainty()),
          toOutput(getPredicted()), toOutput(getTotalUncertainty()), getWeight(),
          toOutput(getResidual()), getWeightedResidual(), arrival.getDistanceDegrees(),
          Math.toDegrees(arrival.getEsaz()), Math.toDegrees(arrival.getSeaz())));
    } catch (Exception e) {
      return GMPException.getStackTraceAsString(e);
    }
    return cout.toString();
  }

  public String predictionString() {
    StringBuffer cout = new StringBuffer();
    double[] derivatives = getDerivatives();

    if (derivatives == null)
      derivatives =
          new double[] {Globals.NA_VALUE, Globals.NA_VALUE, Globals.NA_VALUE, Globals.NA_VALUE};

    // convert derivatives from xxx/km to xxx/degree
    if (derivatives[GMPGlobals.LAT] != Globals.NA_VALUE) {
      derivatives[GMPGlobals.LAT] =
          toRadians(derivatives[GMPGlobals.LAT] * arrival.getSource().getRadius());
      derivatives[GMPGlobals.LON] =
          toRadians(derivatives[GMPGlobals.LON] * arrival.getSource().getRadius());
    }

    cout.append(String.format(
        "%10d %-6s %-6s %4s %2s  %12.4f %12.4f  %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f %12.4f",
        getObservationid(), getReceiver().getSta(), getPhase().toString(), getObsTypeShort(),
        (isDefiningNow() ? " *" : "  "), toOutput(getModelUncertainty()), toOutput(getBaseModel()),
        toOutput(getEllipticityCorrection()), toOutput(getElevationCorrection()),
        toOutput(getElevationCorrectionAtSource()), toOutput(getSiteCorrection()),
        toOutput(getSourceCorrection()), toOutput(getPathCorrection()),
        toOutput(masterEventCorrection), toOutput(derivatives[GMPGlobals.LAT]),
        toOutput(derivatives[GMPGlobals.LON]), toOutput(derivatives[GMPGlobals.DEPTH]),
        toOutput(derivatives[GMPGlobals.TIME])));

    return cout.toString();

  }

  @Override
  public String toString() {
    return String.format("orid=%d, evid=%d, arid=%d, %s/%s/%s", arrival.getSourceId(),
        arrival.getEvid(), arrival.getObservationId(), arrival.getReceiver().getSta(),
        arrival.getPhase(), getObsTypeShort());
  }

  /**
   * FlipFlop is an int that gets incremented every time definingNow is changed from true to false.
   * 
   * @return
   */
  public int getFlipFlop() {
    return flipFlop;
  }

  /**
   * FlipFlop is an int that gets incremented every time definingNow is changed from true to false.
   * 
   * @param flipFlop
   */
  public void setFlipFlop(int flipFlop) {
    this.flipFlop = flipFlop;
  }

  /**
   * @return the "STA/Phase/Type"
   */
  public String getStaPhaseType() {
    if (staPhaseType == null)
      staPhaseType = String.format("%s/%s/%s", getSta(), getPhase().toString(), getObsTypeShort());
    return staPhaseType;
  }

  /**
   * @return the wasDefining
   */
  public boolean wasDefining() {
    return wasDefining;
  }

  /**
   * @param wasDefining the wasDefining to set
   */
  public void setWasDefining(boolean wasDefining) {
    this.wasDefining = wasDefining;
  }

}
