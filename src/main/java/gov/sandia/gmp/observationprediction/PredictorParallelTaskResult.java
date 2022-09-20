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

import java.util.ArrayList;
import java.util.Map;

import gov.sandia.gmp.baseobjects.globals.WaveType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.parallelutils.ParallelResult;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerDouble;
import gov.sandia.gmp.util.profiler.ProfilerContent;

/**
 * Used as a container to hold the results from a specific
 * PredictorParallelTask. This object is returned from a submit call of a
 * JPPFClient object using the JPPF distributed parallel system.
 *
 * <p> The primary content of this object is a list of PredictorResult
 * objects containing the RayInfo results of a set of PredictorObservations.
 * The unique GeoModel / Bender identification string, the computing host
 * node name, and the process calculation time are also returned.
 *
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class PredictorParallelTaskResult extends ParallelResult
{
  /**
   * Static line separator for output.
   */
  private static final String NL = System.getProperty("line.separator");

  /**
   * The list of PredictorResult objects corresponding to a set of
   * PredictorObservation inputs.
   */
  private ArrayList<PredictorResult> aRayList = null;

  /**
   * The unique GeoModel path string
   * (see BenderObservationBundle).
   */
  private String aModelPath       = "";

  /**
   * The unique Polygon3D path string
   * (see BenderObservationBundle).
   */
  private String aPolygonPath     = "";

  /**
   * Used largely for debugging purposes
   */
  private String aMessage         = "";

  /**
   * Used to retrieve profiler information if it was turned on in
   * the task.
   */
  private ProfilerContent aProfilerContent = null;

  /**
   * Standard constructor that sets the size of the BenderResult list
   * and saves the unique GeoModel / Bender identification string.
   *
   * @param sze The number of BenderResult objects contained by this
   *            BenderResultBundle.
   * @param id The unique GeoModel / Bender identification string.
   */
  public PredictorParallelTaskResult(int sze, String modelPath,
                                     String polygonPath)
  {
    aRayList = new ArrayList<PredictorResult>(sze);
    aModelPath   = modelPath;
    aPolygonPath = polygonPath;
  }

  /**
   * Set profiler content.
   * 
   * @param pc The profiler content to set.
   */
  public void setProfilerContent(ProfilerContent pc)
  {
    aProfilerContent = pc;
  }

  /**
   * Returns the profiler content.
   * 
   * @return The profiler content.
   */
  public ProfilerContent getProfilerContent()
  {
    return aProfilerContent;
  }

  /**
   * Returns the unique GeoModel path string.
   *
   * @return The unique GeoModel path string.
   */
  public String getModelPath()
  {
    return aModelPath;
  }

  /**
   * Returns the unique Polygon3D path string.
   *
   * @return The unique Polygon3D path string.
   */
  public String getPolygonPath()
  {
    return aPolygonPath;
  }

  /**
   * Adds a new PredictorResult to the internal list.
   *
   * @param br A new PredictorResult that is added to the internal list.
   */
  public void addRay(PredictorResult br)
  {
    aRayList.add(br);
  }

  /**
   * Returns the ith RayInfo result.
   *
   * @param i The index of the PredictionInterface object to be returned.
   *
   * @return The ith RayInfo result.
   */
  public Prediction getRay(int i)
  {
    return aRayList.get(i).getPrediction();
  }

  /**
   * Returns the ith ray weight array.
   * 
   * @param i The index of the PredictionInterface object whose ray weight array
   *          is to be returned.
   * @return The ith ray weight array.
   */
  public Map<WaveType, HashMapIntegerDouble> getRayWeights(int i)
  {
  	return aRayList.get(i).getRayWeights();
  }

  /**
   * Returns the corresponding PredictorObservation index for this
   * result.
   *
   * @param i The index of the PredictorObservation used to compute
   *          this RayInfo object.
   * @return
   */
  public long getRayIndex(int i)
  {
    return aRayList.get(i).getObservationId();
  }

  /**
   * Returns the entire list of PredictorResult objects.
   *
   * @return The list of PredictorResult objects.
   */
  public ArrayList<PredictorResult> getRays()
  {
    return aRayList;
  }

  /**
   * Add another line to the debug message.
   *
   * @param s New line added to the debug message.
   */
  public void appendMessage(String s)
  {
    aMessage += s + NL;
  }

  /**
   * Clears the debug message.
   */
  public void clearMessage()
  {
    aMessage = "";
  }

  /**
   * Returns debug message string.
   *
   * @return Debug message string.
   */
  public String getMessage()
  {
    return aMessage;
  }
}
