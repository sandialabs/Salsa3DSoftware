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
import java.io.IOException;

/**
 * A Horizon representing a constant radial position in the model.
 * @author sballar
 *
 */
public class HorizonRadius extends Horizon 
{

	/**
	 * The radius in the model, in km.
	 */
	private double radius;

	/**
	 * Constructor for a Horizon object that represents a constant 
	 * radius within the Earth.  Units are km.
	 * <p>Since the layerIndex is not specified, the radius is not
	 * constrained to be within any particular layer.
	 * @param radius radius in km.
	 */
	public HorizonRadius(double radius)
	{
		this.layerIndex = -1;
		this.radius = radius;
	}

	/**
	 * Constructor for a Horizon object that represents a constant 
	 * radius in the Earth, in km.
	 * <p>Since the layerIndex is specified, the radius will be
	 * constrained to be within the specified layer.
	 * @param radius radius within the Earth, in km.
	 * @param layerIndex the index of the layer within which 
	 * the radius will be constrained.
	 */
	public HorizonRadius(double radius, int layerIndex)
	{
		this.layerIndex = layerIndex;
		this.radius = radius;
	}

	public HorizonRadius(DataInputStream input) throws IOException
	{
		int format = input.readInt();
		if (format != 1)
			throw new IOException(format+" is not a recognized format.");
		radius = input.readDouble();
		layerIndex = input.readInt();
	}

	public HorizonRadius(String record) throws Exception {
		String[] tokens = record.trim().replaceAll(",", " ").split("\\s+");
		// expected 5 tokens: TOP or BOTTOM, className, format, value, layer
		int next=2;  // tokens[0] and tokens[1] have already been processed
		int format = 1;
		if (tokens.length > 4)
			format = Integer.parseInt(tokens[next++]);
		if (format != 1)
			throw new Exception(format+" is not a recognized format.");
		radius = Double.parseDouble(tokens[next++]);
		layerIndex = Integer.parseInt(tokens[next++]);
	}

	@Override
	public double getRadius(double[] position, double[] layerRadii)
	{
		if (layerIndex < 0)
			return radius;
		double bottom = layerRadii[layerIndex];
		if (radius <= bottom)
			return bottom;
		double top = layerRadii[layerIndex+1];
		if (radius >= top)
			return top;
		return radius;
	}

	@Override
	public double getValue() { return radius; }

}
