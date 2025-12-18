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
package gov.sandia.gmp.lookupdz;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;

public class Tau {
	/**
	 * distance, in degrees, at which ellipticity coefficients are stored in tables
	 */
	float[] distance;

	/**
	 * depth, in km, at which ellipticity coefficients are stored in tables
	 */
	float[] depth;

	/**
	 * ellipticity coefficients at various distance and depth.
	 */
	float[][][] coeff;

	private final static double SQRT3_OVER2 = sqrt(3.) / 2.;

	/**
	 * 
	 * ASCII Text File Input Function
	 * 
	 * Given the input \em filename, this function reads in the associated basemodel
	 * ellipticity correction lookup table.
	 * @param tableFile only used in error message
	 * @param phase only used in error message
	 * 
	 * @throws IOException
	 * 
	 */
	public Tau(InputStream inputStream, File tableFile, String phase) throws IOException {
		int ix, iz;
		int ntbd, ntbz;

		Scanner scn = new Scanner(inputStream);

		// read the contents of the file into a single text string, ignoring all
		// comments and line endings 
		StringBuffer buf = new StringBuffer();
		while (scn.hasNextLine()) {
			String line = scn.nextLine().trim();
			int idx = line.indexOf('#');
			if (idx < 0)
				idx = line.indexOf("N");
			if (idx < 0)
				buf.append(line+" ");
			else if (idx > 0) 
				buf.append(line.substring(0, idx)+" ");		
		}

		try (Scanner input = new Scanner(buf.toString())) {

			// get number of depth samples
			ntbz = input.nextInt();

			depth = new float[ntbz];

			// read depths
			for (iz = 0; iz < ntbz; iz++)
				depth[iz] = input.nextFloat();

			// get number of distance samples
			ntbd = input.nextInt();

			distance = new float[ntbd];

			// read depths
			for (int i = 0; i < ntbd; i++)
				distance[i] = input.nextFloat();

			// Resize the tau arrays to reflect the number of depths
			// and distances

			coeff = new float[3][ntbz][ntbd];

			// Input the tau values depth by depth

			for (iz = 0; iz < ntbz; iz++) 
				for (ix = 0; ix < ntbd; ix++) 
					for (int i = 0; i < 3; ++i)
						coeff[i][iz][ix] = input.nextFloat();

			if (input.hasNext()) {
				throw new IOException(String.format("%nERROR reading ellipticity corrections directory %s phase %s%n"
						+ "All expected values have been read but there remains unread information in the buffer.%n"
						+ "It is recommended that the ellipticity corrections file be examined for incorrect array size specifications.%n", 
						tableFile.getPath(), phase));
			}
		}
	}


	/**
	 * EllipTable interpolate member function. Interpolates an ellipticity
	 * correction from the associated lookup table. Based On the Function
	 * "get_ec_from_table" Walter Nagy, March 1993. Copyright (c) 1993-1996 Science
	 * Applications International Corporation.
	 * 
	 * @param phase
	 * @param event
	 * @param receiver
	 * @return double the ellipticity correction in seconds.  NaN if phase not supported.
	 */
	public double get_correction(SeismicPhase phase, GeoVector receiver, GeoVector event) {
		double ev_sta_dist = event.distanceDegrees(receiver);
		double ev_sta_azim = event.azimuth(receiver, Double.NaN);
		double ev_geoc_co_lat = event.getGeocentricCoLat();
		double ev_depth = max(0., event.getDepth());

		int ix, iz, n;
		int ix1, ix2, iz1, iz2;
		double ellip_corr = 0.0;
		double dist_fac, depth_fac;
		double a, b, c, d;
		double tau0, tau1, tau2;

		// --------------------------------------------------------------------
		// Reset reference constants, if event co-latitude has changed.
		// --------------------------------------------------------------------
		double etSc0 = 0.25 * (1.0 + 3.0 * cos(2.0 * ev_geoc_co_lat));
		double etSc1 = SQRT3_OVER2 * sin(2.0 * ev_geoc_co_lat);
		double etSc2 = SQRT3_OVER2 * sin(ev_geoc_co_lat) * sin(ev_geoc_co_lat);

		// --------------------------------------------------------------------
		// Find high-ends of both distance (ix2) and depth (iz2) indexes.
		// --------------------------------------------------------------------
		n = distance.length;
		for (ix2 = n - 1, ix = 1; ix < n; ix++) {
			if (ev_sta_dist < distance[ix]) {
				ix2 = ix;
				break;
			}
		}
		ix1 = ix2 - 1; // Low-end distance index

		n = depth.length;
		for (iz2 = n - 1, iz = 1; iz < n; iz++) {
			if (ev_depth < depth[iz]) {
				iz2 = iz;
				break;
			}
		}
		iz1 = iz2 - 1; // Low-end depth index

		dist_fac = (ev_sta_dist - distance[ix1]) / (distance[ix2] - distance[ix1]);
		depth_fac = (ev_depth - depth[iz1]) / (depth[iz2] - depth[iz1]);

		// --------------------------------------------------------------------
		// Compute tau coefficients of Dziewonski and Gilbert (1976).
		// --------------------------------------------------------------------
		// tau0
		a = coeff[0][iz1][ix1];
		b = coeff[0][iz2][ix1];
		c = a + (coeff[0][iz1][ix2] - a) * dist_fac;
		d = b + (coeff[0][iz2][ix2] - b) * dist_fac;
		tau0 = c + (d - c) * depth_fac;

		// tau1
		a = coeff[1][iz1][ix1];
		b = coeff[1][iz2][ix1];
		c = a + (coeff[1][iz1][ix2] - a) * dist_fac;
		d = b + (coeff[1][iz2][ix2] - b) * dist_fac;
		tau1 = c + (d - c) * depth_fac;

		// tau2
		a = coeff[2][iz1][ix1];
		b = coeff[2][iz2][ix1];
		c = a + (coeff[2][iz1][ix2] - a) * dist_fac;
		d = b + (coeff[2][iz2][ix2] - b) * dist_fac;
		tau2 = c + (d - c) * depth_fac;

		// --------------------------------------------------------------------
		// Compute ellipticity correction via equations (22) and (26)
		// of Dziewonski and Gilbert (1976).
		// --------------------------------------------------------------------
		ellip_corr = etSc0 * tau0 + etSc1 * cos(ev_sta_azim) * tau1 + etSc2 * cos(2.0 * ev_sta_azim) * tau2;

		return Double.isNaN(ellip_corr) ? 0. : ellip_corr;

	} // END table_correction

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(coeff);
		result = prime * result + Arrays.hashCode(depth);
		result = prime * result + Arrays.hashCode(distance);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tau other = (Tau) obj;
		return Arrays.deepEquals(coeff, other.coeff) && Arrays.equals(depth, other.depth)
				&& Arrays.equals(distance, other.distance);
	}


	public void writeToFile(File fileName, SeismicPhase phase) throws IOException 
	{
		try (BufferedWriter output = new BufferedWriter(new FileWriter(fileName)); ) {
			output.write("# ellipticity correction coefficients for phase "+phase.name());
			output.newLine();

			output.write(String.format("%3d   # Number of depth samples at the following depth (km):%n", depth.length));
			for (int i=0; i<depth.length; ++i)
			{
				output.write(String.format(" %8.2f", depth[i]));
				if ((i+1) % 10 == 0 || i == depth.length-1)
					output.newLine();
			}

			output.write(String.format("%3d   # Number of distance samples at the following distance (deg):%n", distance.length));
			for (int i=0; i<distance.length; ++i)
			{
				output.write(String.format(" %8.2f", distance[i]));
				if ((i+1) % 10 == 0 || i == distance.length-1)
					output.newLine();
			}

			for (int i=0; i<depth.length; ++i)
			{
				output.write(String.format("# Coefficients at depth =%7.2f km.%n", depth[i]));
				for (int j=0; j<distance.length; ++j) {
					for (int k=0; k<3; ++k)
						output.write(String.format("%10.4f", coeff[k][i][j]));
					output.newLine();
				}
			}
		}
	}

	public float[] getDistance() {
		return distance;
	}

	public float[] getDepth() {
		return depth;
	}

	public float[][][] getCoeff() {
		return coeff;
	}
}
