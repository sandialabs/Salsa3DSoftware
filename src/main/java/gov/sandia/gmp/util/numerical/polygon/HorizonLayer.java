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
 * A Horizon representing a constant fractional position with a specified layer
 * of the model.
 * @author sballar
 *
 */
public class HorizonLayer extends Horizon
{
	/**
	 * The fractional radius within a specified layer.  
	 * 0 will correspond to the bottom of the layer and
	 * 1 will correspond to the top of the layer.
	 */
	private double fraction;

	/**
	 * Constructor specifying a fractional radius within a layer of the model.
	 * 0 will correspond to the bottom of the layer and 1 to the top of the layer.
	 * @param fraction fractional position within a layer
	 * @param layerIndex the layer within which the radius will be constrained
	 */
	public HorizonLayer(double fraction, int layerIndex)
	{
		this.layerIndex = layerIndex;
		this.fraction = fraction < 0. ? 0. : fraction > 1. ? 1. : fraction;
	}

	public HorizonLayer(DataInputStream input) throws IOException
	{
		int format = input.readInt();
		if (format != 1)
			throw new IOException(format+" is not a recognized format.");
		fraction = input.readDouble();
		layerIndex = input.readInt();
	}

	public HorizonLayer(String record) throws Exception {
		String[] tokens = record.trim().replaceAll(",", " ").split("\\s+");
		// expected 5 tokens: TOP or BOTTOM, className, format, value, layer
		int next=2;  // tokens[0] and tokens[1] have already been processed
		int format = 1;
		if (tokens.length > 4)
			format = Integer.parseInt(tokens[next++]);
		if (format != 1)
			throw new Exception(format+" is not a recognized format.");
		fraction = Double.parseDouble(tokens[next++]);
		layerIndex = Integer.parseInt(tokens[next++]);
	}

	@Override
	public double getRadius(double[] position, double[] layerRadii)
	{
		double bottom = layerRadii[layerIndex];
		if (fraction <= 0. )
			return bottom;
		double top = layerRadii[layerIndex+1];
		if (fraction >= 1.)
			return top;
		return bottom + fraction*(top-bottom);
	}

	@Override
	public double getValue() { return fraction; }

}
