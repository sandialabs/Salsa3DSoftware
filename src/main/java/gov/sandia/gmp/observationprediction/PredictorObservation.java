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
package gov.sandia.gmp.observationprediction;

import java.io.Serializable;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.bender.UndersideReflectedPhaseBouncePoint;

/**
 * A PredictorObservation is used as the input source for a single ray-path
 * calculation using the Bender predictor. The input includes a source,
 * receiver, phase, and various database ids defining the data origins.
 *
 * <p> A defining boolean is used to omit the entry from being evaluated
 * in the PredictorParallelTask object. An observation id is used to
 * uniquely identify this observation within the application that will
 * use it.
 *
 * @author gtbarke
 *
 */
@SuppressWarnings("serial")
public class PredictorObservation extends PredictionRequest
       implements Serializable
{
  /**
   * Returns the ray path if true.
   */
  private boolean aReturnRayPath = false;

	/**
	 * Used by predictors that understand the behavior of reflective phases that
	 * are defined with under side reflective bounce points. If the under side
	 * reflected phase bounce point object is requested but not defined it is first created and
	 * then returned.
	 */
	private UndersideReflectedPhaseBouncePoint undersideReflectedPhaseBouncePoint = null;

  /**
   * Standard constructor that sets the source, receiver, phase, defining flag,
   * travel time model table row id, origin id, arrival id, and the application
   * specific observation index.
   *
   * @param src The observation source position.
   * @param rcv The observation receiver position.
   * @param ph The observation phase description.
   * @param defining The observation "defining" flag.
   * @param observationId The application defined observation index.
 * @throws Exception 
   */
  public PredictorObservation(Source src, Receiver rcv, SeismicPhase ph,
                              EnumSet<GeoAttributes> requestedAttributes,
                              boolean defining, int observationId) throws Exception
  {
	  super(observationId, rcv, src, ph, requestedAttributes, defining);
  }

  /**
   * Sets the observation to return the ray path positions as part of the
   * the predicted RayInfo object if the input receiver and source id match this
   * observations receiver and source id. If the attribute is already added it
   * is not added again.
   * 
   * @param rcvrID The input receiver id which is checked against this
   *               observations receiver id (rcv.getReceiverId()).
   * @param srcID The input source id which is checked against this
   *              observations source id (src.getSourceId()).
   */
  public void setRayPathReturn(long rcvrID, long srcID)
  {
    if ((receiver.getReceiverId() == rcvrID) && (source.getSourceId() == srcID))
    {
      aReturnRayPath = true;
      if (!requestedAttributes.contains(GeoAttributes.RAY_PATH))
        requestedAttributes.add(GeoAttributes.RAY_PATH);
    }
  }

  /**
   * Returns the ray path return flag.
   * 
   * @return The ray path return flag.
   */
  public boolean returnRayPath()
  {
    return aReturnRayPath;
  }

	/**
	 * Used by predictors that understand the behavior of reflective phases that
	 * are defined with under side reflective bounce points. If the under side
	 * reflected phase bounce point object is not defined it is first created and
	 * then returned.
	 * 
	 * @return an UndersideReflectedPhaseBouncePoint object.
	 */
	public UndersideReflectedPhaseBouncePoint getUndersideReflectedPhaseBouncePoint()
	{
		if (undersideReflectedPhaseBouncePoint == null)
			undersideReflectedPhaseBouncePoint = new UndersideReflectedPhaseBouncePoint();
		
		return undersideReflectedPhaseBouncePoint;
	}
}
