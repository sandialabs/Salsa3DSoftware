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
package gov.sandia.gmp.util.numerical.polygon;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.IOException;
import java.util.HashSet;

import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

// for containment
//   come in with a radius, depth, layer, radius and layer, depth and layer
// 
// if radius compare between top and bottom radii
// if depth convert to radius and compare between top and bottom radii
// if layer compare between top and bottom layers
// if radius and layer both must match. if either is undefined it is automatic
// containment
// if depth convert to radius. radius and layer both must match. if either is
// undefined it is automatic containment
// 
// 
/**
 * Strictly vertical containment regions that can use depth or layer ids to
 * define point containment. If neither are defined points are contained. This
 * object is used in conjunction with SurfaceRegion objects to form the basis
 * definition of Region objects that provide for 3D combinatorial containment
 * regions.
 * 
 * @author jrhipp
 *
 */
public class DepthLayerInterval
{
  /**
   * The set of layers for which this density definition is defined. This map
   * begins with an empty set that is interpreted as "ALL". Otherwise, a point
   * is only contained if its layer is one of the layers in this set.
   */
  private HashSet<Integer>        aLayers     = null;

  /**
   * The depth top (0) and bottom (1) within which a point is deemed to be
   * contained by this DensityDefinition. Typically either layers or depth, but
   * not both, are used to constrain a point relative to containment by the
   * DensityDefinition.
   */
  private double[]                aDepths     = null;

  /**
   * Default constructor.
   */
  public DepthLayerInterval()
  {
  	// no code
  }

  /**
   * Standard constructor. Reads this DepthLayerRegion from the provided file
   * input buffer.
   */
  public DepthLayerInterval(FileInputBuffer fib) throws IOException
  {
  	read(fib);
  }

  /**
   * Adds a new layer index to this DepthLayerRegion.
   * 
   * @param layer The new layer index to be added.
   */
  public void addLayer(int layer)
  {
  	aLayers.add(layer);
  }

  /**
   * Sets the depth range limits within which a point is deemed to be contained
   * by this DepthLayerRegion.
   * 
   * @param d0 The top depth of the valid range.
   * @param d1 The bottom depth of the valid range.
   * @throws IOException
   */
  public void setDepthRange(double d0, double d1) throws IOException
  {
  	if (d0 >= d1)
  		throw new IOException("Error: Lower Depth Limit (" + d0 +
  				                  ") Exceeds the Upper Depth Limit (" + d1 + ") ...");

  	aDepths = new double [2];
  	aDepths[0] = d0;
  	aDepths[1] = d1;
  }

  /**
   * Returns true if the depth range has been assigned.
   * 
   * @return True if the depth range has been assigned.
   */
  public boolean isDepthRangeAssigned()
  {
  	return (aDepths != null);
  }

  /**
   * Returns true if the input layer index are contained by the layer set.
   * 
   * @param layer The points layer index.
   * @return True if the layer is specified or no layers were defined.
   * @throws IOException
   */
  public boolean isPointContained(int layer) throws IOException
  {
  	// See if the layer index is contained. If no layers are in the set then
  	// "ALL" layers are assumed to be contained.

  	return ((aLayers.size() == 0) || aLayers.contains(layer)) ? true : false;
  }

  /**
   * Returns true if the input point depth is contained within depth interval.
   * 
   * @param depth The points depth (km).
   * @return True if the point is contained within the depth interval or no
   *         interval was defined.
   * @throws IOException
   */
  public boolean isPointContained(double depth) throws IOException
  {
  	// See if the depth is within the depth range. if aDepths = null then 
  	// "ALL" depths are assumed to be contained.

  	return ((aDepths == null) ||
  			    ((depth >= aDepths[0]) && (depth <= aDepths[1]))) ?
  			   true : false;
  }

  /**
   * Standard toString.
   */
  @Override
  public String toString()
  {
  	return toString("");
  }

  /**
   * Returns the contents of this DepthLayerRegion as a string.
   * 
   * @param hdr A header pre-pended to the beginning of every line.
   * @return The contents of this DepthLayerRegion as a string.
   */
  public String toString(String hdr)
  {
  	String s = "";

    // output containment data

    s += hdr + "Depth/Layer Region Containment:" + NL;

  	// add name

  	//s += hdr + "Name                                 = " +
  	//     aName + NL;

    // output depth containment range

    s += hdr + "  Depth Range (km)                   = ";
    if (aDepths == null)
    	s += "ANY" + NL;
    else
    	s += aDepths[0] + " To " + aDepths[1] + NL;

    // output layer containment set

    s += hdr + "  Layer Index Set                    = ";
    if (aLayers.size() == 0)
    	s += "ALL" + NL;
    else
    {
    	for (Integer layer: aLayers) s += " " + layer;
    	s += NL;
    }

  	return s;
  }

  /**
   * Read this DepthLayerRegion object from the provided file input buffer.
   * 
   * @param fib The file input buffer from which this object is read.
   * @throws IOException
   */
	public void read(FileInputBuffer fib) throws IOException
	{
		// read depths ... if zero set to null

		int n = fib.readInt();
		if (n == 0)
			aDepths = null;
		else
		{
			aDepths = new double [2];
			aDepths[0] = fib.readDouble();
			aDepths[1] = fib.readDouble();
		}

		// create layer set and read layers

		aLayers = new HashSet<Integer>();
		n = fib.readInt();
		for (int i = 0; i < n; ++i) aLayers.add(fib.readInt());
	}

	/**
	 * Write this DepthLayerRegion to the provided file output buffer.
	 * 
	 * @param fob The file output buffer into which this DepthLayerRegion object
	 *            is written.
	 * @throws IOException
	 */
	public void write(FileOutputBuffer fob) throws IOException
	{
		// write the depth array size, and if defined, the two depth limits

		if (aDepths == null)
			fob.writeInt(0);
		else
		{
			fob.writeInt(2);
			fob.writeDouble(aDepths[0]);
			fob.writeDouble(aDepths[1]);
		}

		// write the layer set size and the layer ids

		fob.writeInt(aLayers.size());
		for (Integer i: aLayers) fob.writeInt(i);
	}
}
