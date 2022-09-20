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
package gov.sandia.gmp.bender.ray;

import static java.lang.Math.pow;
import static java.lang.Math.round;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.globals.Globals;

/**
 * <p>
 * Stores information about a computed seismic ray. Bender computes Ray objects which retain
 * references back to the GeoModel object from which the Ray was constructed. A RayInfo object
 * copies important information from a Ray but retains no references back to the Ray or the
 * GeoModel.
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
@SuppressWarnings("serial")
public class RayInfo extends Prediction {
  public RayInfo(PredictionRequest request) throws Exception {
    super(request);
  }

  public RayInfo(PredictionRequest request, Predictor predictor, String string) {
    super(request, predictor, string);
  }

  public RayInfo(PredictionRequest request, Predictor predictor, Exception ex) {
    super(request, predictor, ex);
  }

  /**
   * good idea to supply reference to PredictorInterface object that produce the Prediction
   * 
   * @param request
   * @param string
   * @throws Exception
   */
  @Deprecated
  public RayInfo(PredictionRequest request, String string) throws Exception {
    this(request, null, string);
  }

  /**
   * Initializes a RayInfo object from the input FileInputBuffer (fib) assuming it was written
   * previously using the the method:
   * 
   * public void writePrediction(FileOutputBuffer fob) throws IOException
   *
   * @param request The prediction request.
   * @param fib The buffer from which to read the prediction.
   * @throws Exception
   */
  public RayInfo(PredictionRequest request, FileInputBuffer fib) throws Exception {
    super(request);
    readPrediction(fib);
  }

  /**
   * RayInfo constructor that copies information from a Ray object and frees up resources held by
   * the Ray.
   * 
   * @param request
   * @param predictor
   * @param ray
   * @throws Exception
   */
  public RayInfo(PredictionRequest request, Ray ray) throws Exception {
    super(request);

    this.bottomLayer = ray.getBottomLayer();

    setRayType(ray.getRayType());
    rayTypeString = ray.getRayTypeString();

    if (request.getRequestedAttributes().contains(GeoAttributes.TRAVEL_TIME))
      setAttribute(GeoAttributes.TRAVEL_TIME, setPrecision(ray.getTravelTime(), 3));

    if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH)
        || request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_DEGREES)) {
      setAttribute(GeoAttributes.AZIMUTH, Globals.conditionAz(ray.getAzimuth(), Globals.NA_VALUE));
      setAttribute(GeoAttributes.AZIMUTH_DEGREES,
          Globals.conditionAzDegrees(Math.toDegrees(ray.getAzimuth()), Globals.NA_VALUE, 3));
    }

    if (request.getRequestedAttributes().contains(GeoAttributes.BACKAZIMUTH)
        || request.getRequestedAttributes().contains(GeoAttributes.BACKAZIMUTH_DEGREES)) {
      setAttribute(GeoAttributes.BACKAZIMUTH,
          Globals.conditionAz(ray.getBackAzimuth(), Globals.NA_VALUE));
      setAttribute(GeoAttributes.BACKAZIMUTH_DEGREES,
          Globals.conditionAzDegrees(Math.toDegrees(ray.getBackAzimuth()), Globals.NA_VALUE, 3));
    }

    if (request.getRequestedAttributes().contains(GeoAttributes.TURNING_DEPTH))
      setAttribute(GeoAttributes.TURNING_DEPTH, setPrecision(ray.getTurningPoint().getDepth(), 3));

    if (request.getRequestedAttributes().contains(GeoAttributes.OUT_OF_PLANE))
      setAttribute(GeoAttributes.OUT_OF_PLANE, setPrecision(ray.getOutOfPlane(), 3));

    if (request.getRequestedAttributes().contains(GeoAttributes.AVERAGE_RAY_VELOCITY))
      setAttribute(GeoAttributes.AVERAGE_RAY_VELOCITY, ray.getPathLength() / ray.getTravelTime());

    if (request.getRequestedAttributes().contains(GeoAttributes.RAY_PATH)
        || request.getRequestedAttributes().contains(GeoAttributes.DTT_DSLOW)
        || request.getRequestedAttributes().contains(GeoAttributes.TOMO_WEIGHTS)
        || request.getRequestedAttributes().contains(GeoAttributes.ACTIVE_FRACTION)) {
      rayPath.clear();

      // TODO: the last argument in call to ray.resample() is testSamples,
      // which is pretty expensive. Only set it to true during development of a new
      // algorithm that uses ResampleRay.resample() to gain confidence that it
      // is working properly. The test ensures that samples are evenly spaced
      // and that every sample lies on an interval between two points in a colinear
      // manner.

      ray.resample(nodeSpacing, rayPath, true);
      processRayPath(ray.getGeoTessModel(), request.getRequestedAttributes());
    }
  }

  public void setZeroLengthRay() {
    setAttribute(GeoAttributes.TRAVEL_TIME, 0.);
    getSupportedAttributes().remove(GeoAttributes.AZIMUTH);
    getSupportedAttributes().remove(GeoAttributes.AZIMUTH_DEGREES);
    getSupportedAttributes().remove(GeoAttributes.BACKAZIMUTH);
    getSupportedAttributes().remove(GeoAttributes.BACKAZIMUTH_DEGREES);
  }

  /**
   * @return travel time in seconds.
   */
  public double getTravelTime() {
    return getAttribute(GeoAttributes.TRAVEL_TIME);
  }

  /**
   * @return receiver-source azimuth in radians clockwise from north (0 - 2*PI).
   */
  public double getAzimuth() {
    return getAttribute(GeoAttributes.AZIMUTH);
  }

  /**
   * @return receiver-source azimuth in degrees clockwise from north (0 - 360).
   */
  public double getAzimuthDegrees() {
    return getAttribute(GeoAttributes.AZIMUTH_DEGREES);
  }

  /**
   * @return source-receiver azimuth in radians clockwise from north (0 - 2*PI).
   */
  public double getBackAzimuth() {
    return getAttribute(GeoAttributes.BACKAZIMUTH);
  }

  /**
   * @return source-receive azimuth in degrees clockwise from north (0 - 360).
   */
  public double getBackAzimuthDegrees() {
    return getAttribute(GeoAttributes.BACKAZIMUTH_DEGREES);
  }

  /**
   * toString returns the information content of this RayInfo object formatted in a String.
   * 
   * @return String
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();

    try {
      if (isValid()) {
        buf.append(String.format("obsId=%d layer=%3d dist=%6.2f tt=%8.3f turn=%6.2f op=%6.2f %11s",
            getObservationId(), bottomLayer, getDistanceDegrees(),
            getAttribute(GeoAttributes.TRAVEL_TIME), getAttribute(GeoAttributes.TURNING_DEPTH),
            getAttribute(GeoAttributes.OUT_OF_PLANE), "  ray type = " + getRayTypeString()));
      } else {
        buf.append(String.format("%9.4f %9.4f  d=%1.4f  %1s", getSource().getLatDegrees(),
            getSource().getLonDegrees(), getDistanceDegrees(),
            "  ray type = " + getRayTypeString()));
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      buf.append("/n/n").append(ex.getMessage()).append("\n\n");
    }
    return buf.toString();
  }

  private double setPrecision(double x, int digits) {
    double precision = pow(10., digits);
    return round(precision * x) / precision;
  }


}
