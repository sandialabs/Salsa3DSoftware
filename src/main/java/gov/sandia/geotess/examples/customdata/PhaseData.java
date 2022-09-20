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
package gov.sandia.geotess.examples.customdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Defines travel time information for an event hypothesis 
 * at a single grid point and single station - phase.
 * @author sandy
 *
 */
public class PhaseData extends HashMap<GeoAttributes, Double>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7339325174314271494L;

	/**
	 * Whether or not this is a primary phase.  Need more information about this.
	 */
	boolean primary;
	
	public PhaseData() {
	}

	/**
	 * 
	 * @param input
	 * @param attributes A list of all the attributes supported by this model.
	 * All of these attributes will have values in each PhaseData map and will
	 * be read from, and written to the output files.
	 * @throws IOException
	 */
	public PhaseData(DataInputStream input, GeoAttributes[] attributes) throws IOException
	{
		primary = input.readBoolean();
		for (GeoAttributes a : attributes)
			put(a, input.readDouble());
	}
	
	/**
	 * 
	 * @param output
	 * @param attributes A list of all the attributes supported by this model.
	 * All of these attributes will have values in each PhaseData map and will
	 * be read from, and written to the output files.
	 * @throws IOException
	 */
	public void write(DataOutputStream output, GeoAttributes[] attributes) throws IOException 
	{
		output.writeBoolean(primary);
		for (GeoAttributes a : attributes)
			output.writeDouble(get(a));
	}
	
	/**
	 * 
	 * @param input
	 * @param attributes A list of all the attributes supported by this model.
	 * All of these attributes will have values in each PhaseData map and will
	 * be read from, and written to the output files.
	 */
	public PhaseData(Scanner input, GeoAttributes[] attributes) 
	{
		primary = input.nextBoolean();
		for (GeoAttributes a : attributes)
			put(a, input.nextDouble());
	}
	
	/**
	 * 
	 * @param output A list of all the attributes supported by this model.
	 * All of these attributes will have values in each PhaseData map and will
	 * be read from, and written to the output files.
	 * @param attributes
	 */
	public void write(StringBuffer output, GeoAttributes[] attributes) {
		output.append(primary);
		for (GeoAttributes a : attributes)
		{
			Double value = get(a);
			output.append(' ');
			output.append(value == null ? "NaN" 
					: Double.toString(value));
		}
	}
	
}
