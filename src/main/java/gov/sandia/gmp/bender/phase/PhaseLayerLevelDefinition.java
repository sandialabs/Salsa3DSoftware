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
package gov.sandia.gmp.bender.phase;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import gov.sandia.gmp.baseobjects.globals.EarthInterface;
import gov.sandia.gmp.bender.level.Layers;

/**
 * <p>Representation of a seismic phase layer set within which initial ray
 * definitions can be constructed.</p>
 * 
 * <p>Converted for use with GeoTessModel/GeoTessPosition Nov, 2014.</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Sandia National Laboratories</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
@SuppressWarnings("serial")
public class PhaseLayerLevelDefinition extends Layers implements Serializable, Cloneable
{
	/**
	 * The EarthInterface type for which this PhaseLayerLevelDefinition was
	 * created.
	 */
	protected EarthInterface           interfaceLayerType = null;
	
  /**
   * Map of source-to-receiver distance associated with the bottom layer index
   * within which initial rays are constructed for this PhaseLayerDefinition.
   */
  protected TreeMap<Double, Integer> bottomLayer =
            new TreeMap<Double, Integer> ();

  /**
   * Map of source-to-receiver distance associated with the top layer index
   * within which initial rays are constructed for this PhaseLayerDefinition.
   */
  protected TreeMap<Double, Integer> topLayer =
            new TreeMap<Double, Integer> ();

  /**
   * Default constructor.
   */
  public PhaseLayerLevelDefinition()
  {
  	super();
  }

  /**
   * Standard protected constructor that creates a new PhaseLayerLevelDefinition
   * for the input EarthInterface type. This constructor can only be called by
   * private methods defined in the PhaseLayerLevelBuilder object.
   * 
   * @param phaseLayerType The EarthInterface type for which this object was
   *                       constructed.
   */
  protected PhaseLayerLevelDefinition(EarthInterface phaseLayerType)
  {
  	super();
  	interfaceLayerType = phaseLayerType;
  }

  /**
   * Returns the interface layer type name (an EarthInterface).
   * 
   * @return The interface layer type name (an EarthInterface).
   */
  public String getInterfaceLayerTypeName()
  {
  	return interfaceLayerType.name();
  }

  /**
   * Adds the input layer name associated with the input source-to-receiver
   * distance (degrees) as the bottom layer within which initial rays will be
   * constructed for this PhaseLayerDefinition.
   *
   * @param distance Source-to-Receiver distance (degrees) associated with the
   *                 input layer name as the bottom layer for an initial ray
   *                 construction.
   * @param interfaceName String[] Set of one or more interface names for which
   *                               the layer index is returned for the first
   *                               discovered in the set of layer names.
   * @throws InvalidParameterException
   */
  protected void addBottomLayer(double distance, String... interfaceName)
            throws InvalidParameterException
  {
    int i = getInterfaceIndex(interfaceName);
    if (i < 0)
        throw new InvalidParameterException(String.format(
        "%s is not a recognized layer name.", Arrays.toString(interfaceName)));
    bottomLayer.put(distance, i);
  }

  /**
   * Get the bottom layer index associated with the input source-to-receiver
   * distance.
   * 
   * @param distance Source-to-Receiver distance (degrees) for which the
   *                 bottom layer index is returned.
   * @return The bottom layer index associated with the input source-to-receiver
   *         distance. 
   */
  public int getBottomLayer(double distance)
  {
    for (Map.Entry<Double, Integer>  x : bottomLayer.entrySet())
      if (distance <= x.getKey())
        return x.getValue().intValue();
    return -1;
  }

  /**
   * Adds the input layer name associated with the input source-to-receiver
   * distance (degrees) as the top layer within which initial rays will be
   * constructed for this PhaseLayerDefinition.
   * 
   * @param distance Source-to-Receiver distance (degrees) associated with the
   *                 input layer name as the top layer for an initial ray
   *                 construction.
   * @param interfaceName String[] Set of one or more interface names for which
   *                               the layer index is returned for the first
   *                               discovered in the set of layer names.
   * @throws InvalidParameterException
   */
  protected void addTopLayer(double distance, String... interfaceName)
            throws InvalidParameterException
  {
    int i = getInterfaceIndex(interfaceName);
    if (i < 0)
        throw new InvalidParameterException(String.format(
        "%s is not a recognized layer name.", Arrays.toString(interfaceName)));
    topLayer.put(distance, i);
  }

  /**
   * Get the top layer index associated with the input source-to-receiver
   * distance.
   * 
   * @param distance Source-to-Receiver distance (degrees) for which the
   *                 top layer index is returned.
   * @return The top layer index associated with the input source-to-receiver
   *         distance. 
   */
  public int getTopLayer(double distance)
  {
    for (Map.Entry<Double, Integer>  x : topLayer.entrySet())
      if (distance <= x.getKey())
        return x.getValue().intValue();
    return -1;
  }
}
