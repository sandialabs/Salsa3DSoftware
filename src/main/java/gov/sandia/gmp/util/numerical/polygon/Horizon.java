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

import java.io.DataInputStream;
import java.io.DataOutputStream;

import gov.sandia.gmp.util.globals.Globals;

/**
 * Horizon is an abstract class that represents a single "surface" within a
 * model. This might be a surface of constant radius, constant depth, the top or
 * bottom of a layer, etc. The surface can be constrained to a specified layer,
 * or can cross layer boundaries.  There are derived classes for each of these, 
 * HorizonRadius, HorizonDepth, and HorizonLayer.  A Horizon class implements
 * a single basic function, getRadius().  That method can take either a 
 * GeoTessPosition object or a vertex position and the 1D array of 
 * Profiles associated with that vertex.
 * 
 * @author sballar
 * 
 */
public abstract class Horizon
{
	/**
	 * If layerIndex is >= 0 and < the number of layers represented in a model,
	 * then the returned radius will be constrained to be between the top and
	 * bottom of the specified layer.  Otherwise, the radius will not be 
	 * so constrained. 
	 */
	protected int layerIndex;

	public static Horizon getHorizon(DataInputStream input) throws Exception
	{
		String type = Globals.readString(input);
		if (type.equals("HorizonDepth"))
			return new HorizonDepth(input);
		if (type.equals("HorizonRadius"))
			return new HorizonRadius(input);
		if (type.equals("HorizonLayer"))
			return new HorizonLayer(input);
		throw new Exception(type+" is not a recognized Horizon class.");
	}

	public static Horizon getHorizon(String record) throws Exception {
		if (record.toLowerCase().contains("depth"))
			return new HorizonDepth(record);
		if (record.toLowerCase().contains("radius"))
			return new HorizonRadius(record);
		if (record.toLowerCase().contains("layer"))
			return new HorizonLayer(record);
		throw new Exception("Could not extract a valid Horizon className from "+record);
		}
	
	/**
	 * Return the radius of the Horizon at the specified geographic position
	 * and constrained by the specified array of radii, all of which are
	 * assumed to reside at the specified position.
	 * @param position the unit vector representing the position where the 
	 * radius is to be determined.  This should correspond to the position
	 * of the supplied array of Profiles.  Used only by HorizonDepth objects
	 * to determine the radius of the Earth at the position of the Profiles.
	 * Not used by HorizonLayer or HorizonRadius objects.
	 * @param layerRadii a 1D array of radius values, in km, that specify the radii
	 * of the interfaces between layers, determined at the specified position.
	 * The number of elements must be equal to one plus the number of layers in the 
	 * model.  The first value is the radius of the bottom of the deepest layer
	 * (closest to the center of the Earth) and the last value is the radius of the 
	 * top of the last layer (farthest from the center of the Earth).
	 * @return the radius of the Horizon at the specified position and 
	 * perhaps constrained to reside in the specified layer.  Units are km.
	 */
	public abstract double getRadius(double[] position, double[] layerRadii);
	
	public abstract double getValue();
	
	/**
	 * Retrieve the index of the layer that was specified at construction.  
	 * If >= 0 and < the number of layers in the model then the
	 * radius of this Horizon object will be constrained to be within the radii of 
	 * the top and bottom of this layer.  
	 * @return layer index, or -1.
	 */
	public int getLayerIndex() { return layerIndex; }

	public String toString() {
		return String.format("%s 1 %8.3f %3d", getClass().getSimpleName(), getValue(), layerIndex);
	}

	public void write(DataOutputStream output) throws Exception
	{
		Globals.writeString(output, getClass().getSimpleName());
		output.writeInt(1);
		output.writeDouble(getValue());
		output.writeInt(layerIndex);
	}

}
