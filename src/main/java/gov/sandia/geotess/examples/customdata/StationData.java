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

import gov.sandia.geotess.GeoTessUtils;

/**
 * Defines information for an event hypothesis at a single grid 
 * point and a single station.
 * @author sandy
 * 
 */
public class StationData extends HashMap<SeismicPhase, PhaseData>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8612644644926375628L;

	/**
	 * Distance from station to grid point, in degrees. 
	 * (Easily computed from grid point and station locations.)
	 */
	double delta;

	/**
	 * Azimuth from station to grid point, in degrees.
	 * (In GMS system is azimuth station-to-event or other way around?)
	 */
	double azimuth;

	/**
	 * Backazimuth from grid point to station, in degrees.
	 * (In GMS system is backAzimuth station-to-event or other way around?)
	 */
	double backAzimuth;

	public StationData() {
	}

	/**
	 * 
	 * @param input
	 * @param attributes A list of all the attributes supported by this model.
	 * All of these attributes will have values in each PhaseData map and will
	 * be read from, and written to the output files.
	 * @throws IOException
	 */
	public StationData(DataInputStream input, GeoAttributes[] attributes) throws IOException
	{
		delta = input.readDouble();
		azimuth = input.readDouble();
		backAzimuth = input.readDouble();

		int nPhases = input.readInt();

		for (int i=0; i<nPhases; ++i)
		{
			SeismicPhase phase = SeismicPhase.valueOf(GeoTessUtils.readString(input));
			put(phase, new PhaseData(input, attributes));
		}
	}

	/**
	 * 
	 * @param output
	 * @param attributes A list of all the attributes supported by this model.
	 * All of these attributes will have values in each PhaseData map and will
	 * be read from, and written to the output files.
	 * @throws IOException
	 */
	public void write(DataOutputStream output, 
			GeoAttributes[] attributes) throws IOException {
		output.writeDouble(delta);
		output.writeDouble(azimuth);
		output.writeDouble(backAzimuth);

		output.writeInt(size()); // number phases
		for (Entry<SeismicPhase, PhaseData> entry : entrySet())
		{
			GeoTessUtils.writeString(output, entry.getKey().toString()); // phase name
			entry.getValue().write(output, attributes); // PhaseData
		}
	}

	/**
	 * 
	 * @param input
	 * @param attributes A list of all the attributes supported by this model.
	 * All of these attributes will have values in each PhaseData map and will
	 * be read from, and written to the output files.
	 */
	public StationData(Scanner input, GeoAttributes[] attributes) 
	{
		delta = input.nextDouble();
		azimuth = input.nextDouble();
		backAzimuth = input.nextDouble();

		int nPhases = input.nextInt();
		for (int i=0; i<nPhases; ++i)
		{
			SeismicPhase phase = SeismicPhase.valueOf(input.next());
			put(phase, new PhaseData(input, attributes));
		}
	}

	/**
	 * 
	 * @param output
	 * @param attributes A list of all the attributes supported by this model.
	 * All of these attributes will have values in each PhaseData map and will
	 * be read from, and written to the output files.
	 */
	public void write(StringBuffer output, 
			GeoAttributes[] attributes)  {
		output.append(Double.toString(delta)).append(" ");
		output.append(Double.toString(azimuth)).append(" ");
		output.append(Double.toString(backAzimuth)).append('\n');

		for (Entry<SeismicPhase, PhaseData> entry : entrySet())
		{
			output.append(entry.getKey().toString()).append('\n'); // phase name
			entry.getValue().write(output, attributes); // PhaseData
		}
	}

}
