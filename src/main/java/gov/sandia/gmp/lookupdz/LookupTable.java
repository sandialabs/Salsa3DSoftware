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

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import gov.sandia.gmp.seismicbasedata.SeismicBaseData;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Utils;

public class LookupTable
{

	protected File file;
	
	/**
	 * First line of a lookup table file.
	 */
	protected String header;

	/**
	 * distances, in degrees, at which values are stored in tables
	 */
	protected double[] distances;

	/**
	 * depths, in km, at which values are stored in tables
	 */
	protected double[] depths;

	/**
	 * values at various distances and depths.
	 * double[depths.length][distances.length]
	 */
	protected double[][] values;

	/**
	 * distances, in degrees, at which uncertainties are stored in tables
	 */
	protected double[] uncDistances;

	/**
	 * depths, in km, at which uncertainties are stored in tables
	 */
	protected double[] uncDepths;

	/**
	 * uncertainties at various distances and depths.
	 */
	protected double[][] uncertainties;
	

	static final int MAX_DIST_SAMPLES = 7;
	static final int MAX_DEPTH_SAMPLES = 4;
	static final int MIN_NUM_DIST_SAMPLES = 3;
	static final double BAD_SAMPLE = -1.0;
	static final int WRN_INVALID_VALUE = -1;
	static final int WRN_INSUFFICIENT_DATA = -2;
	static final double DISTANCE_PROXIMITY_TOLERANCE = 0.00001;
	static final double DEPTH_PROXIMITY_TOLERANCE = 0.001;

	//**** Used only by method ratint (Diagonal Rational Function interpolation/
	//**** extrapolation method by Bulirsch and Stoer). Modified the value from
	//**** 1.0e-7 to 1.0e-30 as per Numerical Recipes (from which
	//**** the algorithm is taken).
	//****  (jrh 12-19-2018)
	static final double FLOAT_EPSILON = 1.0e-30;

	//**** Used only by method ratint (Diagonal Rational Function interpolation/
	//**** extrapolation method by Bulirsch and Stoer). Modified the value from
	//**** 1.0e-9 to 1.0e-15 as per Numerical Recipes (from which
	//**** the algorithm is taken).
	//****  (jrh 12-19-2018)
	static final double RATINT_POLE_THRESHOLD = 1.0e-15;
	
	static final double TT_DEL_DEPTH = 1e-7;
	
	static final Map<Integer, String> messages;
	static
	{
		messages = new LinkedHashMap<Integer, String>();
		messages.put( 0, "");
		messages.put(-1, "Single depth sampling exists, but requested depth is not the same as that in the table, or problems are encountered while doing rational function extrapolation (via function, ratint()).");
		messages.put(-2, "Insufficient valid samples exist for a meaningful traveltime calculation.");
		messages.put(11, "Extrapolated point in hole of curve");
		messages.put(12, "Extrapolated point < first distance");
		messages.put(13, "Extrapolated point > last distance");
		messages.put(14, "Extrapolated point < first depth");
		messages.put(15, "Extrapolated point > last depth");
		messages.put(16, "Extrapolated point < first distance and < first depth");
		messages.put(17, "Extrapolated point > last distance and < first depth");
		messages.put(18, "Extrapolated point < first distance and > last depth");
		messages.put(19, "Extrapolated point > last distance and > last depth");

	};

	static public String getVersion()
	{
		return Utils.getVersion("lookup-tables-dz");
	}
	
	public LookupTable(double[] distances, double[] depths, double[][] values,
			double[] uncDistances, double[] uncDepths, double[][] uncertainties) throws IOException
	{
		this.distances = distances;
		this.depths = depths;
		this.values = values;
		this.uncDistances = uncDistances;
		this.uncDepths = uncDepths;
		this.uncertainties = uncertainties;
	}

	public LookupTable(File inputFile) throws IOException
	{
		this.file = inputFile;
		read(this.file);
	}
	
	public void writeToFile(File f) throws IOException
	{
		file = f;
		BufferedWriter output = new BufferedWriter(new FileWriter(f));
		output.write(header);
		output.newLine();
		
		output.write(String.format("%3d   Number of depth samples at the following depths (km):%n", depths.length));
		for (int i=0; i<depths.length; ++i)
		{
			output.write(String.format(" %7.2f", depths[i]));
			if ((i+1) % 10 == 0 || i == depths.length-1)
				output.newLine();
		}
		
		output.write(String.format("%3d   Number of distance samples at the following distances (deg):%n", distances.length));
		for (int i=0; i<distances.length; ++i)
		{
			output.write(String.format(" %7.2f", distances[i]));
			if ((i+1) % 10 == 0 || i == distances.length-1)
				output.newLine();
		}
		
		for (int i=0; i<depths.length; ++i)
		{
			output.write(String.format("# Travel time at depth =%7.2f km.%n", depths[i]));
			for (int j=0; j<distances.length; ++j)
				output.write(String.format("%12.4f%n", values[i][j]));
		}
		
		output.write(String.format("# Distance-dependent modelling error(s)%n"));
		output.write(String.format(" %4d %4d%n", uncDistances.length, uncDepths.length));
		
		for (int i=0; i<uncDistances.length; ++i)
		{
			output.write(String.format(" %7.2f", uncDistances[i]));
			if ((i+1) % 10 == 0 || i == uncDistances.length-1)
				output.newLine();
		}
		
		for (int i=0; i<uncDepths.length; ++i)
		{
			output.write(String.format(" %7.2f", uncDepths[i]));
			if ((i+1) % 10 == 0 || i == uncDepths.length-1)
				output.newLine();
		}
		
		for (int i=0; i<uncDepths.length; ++i)
		{
			output.write(String.format("#%n"));
			for (int j=0; j<uncDistances.length; ++j)
				output.write(String.format("%12.4f%n", uncertainties[i][j]));
		}
		output.close();
	}

	/**
	 * @return the file
	 */
	public File getFile()
	{
		return file;
	}

	public String getHeader() {
	    return header;
	}

	public void setHeader(String header) {
	    this.header = header;
	}

	/**
	 * @return the distances
	 */
	public double[] getDistances()
	{
		return distances;
	}

	/**
	 * @return the depths
	 */
	public double[] getDepths()
	{
		return depths;
	}

	/**
	 * Values is an nDepths x nDistances array.
	 * @return the values
	 */
	public double[][] getValues()
	{
		return values;
	}

	/**
	 * @return the uncDistances
	 */
	public double[] getUncertaintyDistances()
	{
		return uncDistances;
	}

	/**
	 * @return the uncDepths
	 */
	public double[] getUncertaintyDepths()
	{
		return uncDepths;
	}

	/**
	 * @return the uncertainties
	 */
	public double[][] getUncertainties()
	{
		return uncertainties;
	}

	@Override
	public String toString()
	{
		StringBuffer out = new StringBuffer();
		out.append(toString(distances, depths, values, false));
		out.append('\n');
		out.append(toString(uncDistances, uncDepths, uncertainties, false));
		out.append('\n');
		return out.toString();
	}

	public String toString(boolean transpose)
	{
		StringBuffer out = new StringBuffer();
		out.append(toString(distances, depths, values, transpose));
		out.append('\n');
		out.append(toString(uncDistances, uncDepths, uncertainties, transpose));
		out.append('\n');
		return out.toString();
	}

	public String toString(double[] x, double[] z, double[][] data, boolean transpose)
	{
		StringBuffer out = new StringBuffer();
		if (transpose)
		{		
			out.append(String.format("%10s", file.getName()));
			out.append(String.format(" %10s", " "));
			for (int i = 0; i < z.length; ++i)
				out.append(String.format(" %10d", i));
			out.append('\n');
			
			out.append(String.format("%10s", " "));
			out.append(String.format(" %10s", " "));
			for (int i = 0; i < z.length; ++i)
				out.append(String.format(" %10.4f", z[i]));
			out.append('\n');

			for (int i = 0; i < x.length; ++i)
			{
				out.append(String.format("%10d", i));
				out.append(String.format(" %10.3f", x[i]));
				for (int j = 0; j < z.length; ++j)
					out.append(String.format(" %10.4f", data[j][i]));

				out.append(String.format("%10d", i));
				out.append('\n');
			}

		}
		else
		{
			out.append(String.format("%10s", file.getName()));
			out.append(String.format(" %10s", " "));
			for (int i = 0; i < x.length; ++i)
				out.append(String.format(" %10d", i));
			out.append('\n');
			
			out.append(String.format("%10s", " "));
			out.append(String.format(" %10s", " "));
			for (int i = 0; i < x.length; ++i)
				out.append(String.format(" %10.4f", x[i]));
			out.append('\n');

			for (int j = 0; j < z.length; ++j)
			{
				out.append(String.format("%10d", j));
				out.append(String.format(" %10.3f", z[j]));
				for (int i = 0; i < x.length; ++i)
					out.append(String.format(" %10.4f", data[j][i]));

				out.append(String.format("%10d", j));
				out.append('\n');
			}
		}
		return out.toString();
	}

	public LookupTable read(File inputFile) throws IOException
	{
		InputStream inputStream = new SeismicBaseData(this.file).getInputStream();
		Scanner input = null;
		try {
			input = new Scanner(inputStream);
		} catch (Exception e) {
			throw new IOException("Could not read file "+inputFile.getPath());
		}

		// skip first line. comment
		header = input.nextLine();

		// get number of depth samples
		int n = input.nextInt();
		depths = new double[n];

		input.nextLine();

		// read depths
		for (int iz = 0; iz < depths.length; iz++)
			depths[iz] = input.nextDouble();

		input.nextLine();

		// get number of distance samples
		distances = new double[input.nextInt()];

		input.nextLine();

		// read distances
		for (int i = 0; i < distances.length; i++)
			distances[i] = input.nextDouble();

		input.nextLine();

		values = new double[depths.length][distances.length];

		// Input the tau values depth by depth

		for (int iz = 0; iz < depths.length; iz++)
		{
			// Skip depth header line.
			input.nextLine();
			// read each line for depth iz
			for (int ix = 0; ix < distances.length; ix++)
				values[iz][ix] = input.nextDouble();
			input.nextLine();
		}
		
		if (!input.hasNext())
		{
			uncDistances = new double[0];
			uncDepths = new double[0];			
		}
		else
		{
			// skip the comment
			input.nextLine();
			
			uncDistances = new double[input.nextInt()];
			uncDepths = new double[input.nextInt()];

			// read distances
			if (uncDistances.length > 1)
				for (int i = 0; i < uncDistances.length; i++)
					uncDistances[i] = input.nextDouble();

			// read depths
			if (uncDepths.length > 1)
				for (int iz = 0; iz < uncDepths.length; iz++)
					uncDepths[iz] = input.nextDouble();

			uncertainties = new double[uncDepths.length][uncDistances.length];

			// Input the uncertainty values depth by depth
			input.nextLine();
			for (int iz = 0; iz < uncDepths.length; iz++)
			{
				// Skip depth header line.
				input.nextLine();
				// read values for depth iz
				for (int ix = 0; ix < uncDistances.length; ix++)
					uncertainties[iz][ix] = input.nextDouble();
				input.nextLine();
			}
		}

		inputStream.close();
		input.close();

		return this;
	}

	public double interpolateUncertainty(double distance, double depth)
	{
		if (uncDepths.length == 1 && uncDistances.length == 1)
			return uncertainties[0][0];

		if (uncDepths.length == 1)
		{
			distance = max(uncDistances[0],
					min(uncDistances[uncDistances.length - 1], distance));
			int x = min(uncDistances.length - 2, hunt(uncDistances, distance));
			double dx = (distance - uncDistances[x])
					/ (uncDistances[x + 1] - uncDistances[x]);
			return uncertainties[0][x] * (1. - dx) + uncertainties[0][x + 1]
					* dx;
		}

		if (uncDistances.length == 1)
		{
			depth = max(uncDepths[0],
					min(uncDepths[uncDepths.length - 1], depth));
			int z = min(uncDepths.length - 2, hunt(uncDepths, depth));
			double dz = (depth - uncDepths[z])
					/ (uncDepths[z + 1] - uncDepths[z]);
			return uncertainties[z][0] * (1. - dz) + uncertainties[z + 1][0]
					* dz;
		}

		distance = max(uncDistances[0],
				min(uncDistances[uncDistances.length - 1], distance));
		int x = min(uncDistances.length - 2, hunt(uncDistances, distance));
		double dx = (distance - uncDistances[x])
				/ (uncDistances[x + 1] - uncDistances[x]);

		depth = max(uncDepths[0], min(uncDepths[uncDepths.length - 1], depth));
		int z = min(uncDepths.length - 2, hunt(uncDepths, depth));
		double dz = (depth - uncDepths[z]) / (uncDepths[z + 1] - uncDepths[z]);

		return uncertainties[z][x] * (1. - dx) * (1. - dz)
				+ uncertainties[z][x + 1] * (1. - dz) * dx
				+ uncertainties[z + 1][x] * dz * (1. - dx)
				+ uncertainties[z + 1][x + 1] * dz * dx;

	}

	/**
	 * Traveltime Interpolation Function
	 *
	 * <p>TTBMCubicSpline interpolate member function. Computes a traveltime from the
	 * associated lookup table. If the point (distance-depth) falls within the
	 * range of the table then interpolation is performed via the 1-d
	 * (distance-traveltime) cubic splines representing the table. Otherwise, rational
	 * polynomial interpolation is used to extrapolate a value.
	 *
	 * <p>Method is as follows: Using bi-section, bracket a two-dimensional array
	 * by performing successive searches on two single arrays which are ordered
	 * tables possessing monotonically increasing or decreasing values.
	 * Then using bi-cubic interpolation determine a value, and 1st and 2nd
	 * derivatives for a requested distance and depth.
	 *
	 * <p>We can think of nx_req and nz_req being indexed (ordered) as follows:
	 *
	 * <p><code>
	 * <br>o = Table node (an actual distance/depth in table)
	 * <br>x = Point of Interest (distance or depth)
	 * <br>l = Nearest left element (xleft in code)
	 *
	 * <p>o o o lxo o o
	 * <br>7 5 3 1 2 4 6
	 * </code>
	 *
	 * <p>Based On the Function "interpolate_table_value"
	 *<br> Walter Nagy, March 1993.
	 * <br>Copyright (c) 1993-1996 Science Applications International Corporation.
	 *
	 * @param distance - Distance value for which travel-time will be evaluated, in degrees.
	 * @param depth - Depth value for which travel-time will be evaluated, in km.
	 * @param ttZDerivatives whether or not dtdz and d2tdz2 should be computed
	 * @param shZDerivatives whether or not dshdz should be computed
	 * @param ttExtrapolate whether or not extrapolation is allowed.
	 * @param results (caller must supply this array with at least 6 elements)
	 * <ol start=0>
	 * <li>interpolated travel time, in seconds.
	 * <li>dtdx - derivative of travel time wrt distance, in sec/degree
	 * <li>d2tdx2 - second derivative of travel time wrt distance, in sec/degree^2
	 * <li>dtdz - derivative of travel time wrt depth, in sec/km
	 * <li>d2tdz2 - second derivative of travel time wrt depth, in sec/km^2
	 * <li>d2tdxdz - derivative of horizontal slowness wrt depth, in sec/(deg.km)
	 * </ol>
	 * If anything goes wrong, results is filled with NaN.
	 * @return Flag indicating where the distance-depth point fell
	 * with respect to the travel time table.
	 * <ul>
	 * <li> 0: No errors, no extrapolation was necessary
	 * <li> -1 = Single depth sampling exists, but requested depth is not the
	 * same as that in the table, or Problems are encountered while
	 * doing rational function extrapolation (via function,\ ratint()). 
	 * <li> -2 = Insufficient valid samples exist for a meaningful traveltime\ calculation.
	 * <li> 11: Extrapolated point in hole of curve </li>
	 * <li> 12: Extrapolated point < first distance </li>
	 * <li> 13: Extrapolated point > last distance </li>
	 * <li> 14: Extrapolated point < first depth </li>
	 * <li> 15: Extrapolated point > last depth </li>
	 * <li> 16: Extrapolated point < first distance and < first depth </li>
	 * <li> 17: Extrapolated point > last distance and < first depth </li>
	 * <li> 18: Extrapolated point < first distance and > last depth </li>
	 * <li> 19: Extrapolated point > last distance and > last depth </li>
	 * </ul>
	 */
	public int interpolate(double distance, double depth, 
			boolean ttZDerivatives, boolean shZDerivatives, boolean ttExtrapolate, 
			double[] results)
	{
		Arrays.fill(results, Double.NaN);
		// 0: interpolated value
		// 1: dvdx
		// 2: d2vdx2
		// 3: dvdz
		// 4: d2vdz2
		// 5: d2vdxdz

		int i, j, k, kk, m, n;
		int xleft, zleft, nx_req, nz_req;
		int xlow, xhigh, ztop, zbottom;
		int num_extrap = 0, num_samp = 0;
		int idist = 0, idepth = 0;

		double xshift, zshift, diff;

		// Required # of samples in x-direction
		nx_req = min(MAX_DIST_SAMPLES, distances.length);

		// Required # of samples in z-direction
		nz_req = min(MAX_DEPTH_SAMPLES, depths.length);

		double[] ttcsInHoleDist = new double[] { 181., -1., 0. };

		boolean ok_so_far = true;
		for (i = 1; i < distances.length; i++)
		{
			if (values[0][i - 1] != -1. && values[0][i] == -1)
			{
				ttcsInHoleDist[0] = distances[i - 1];
				ok_so_far = false;
			}
			else if (!ok_so_far && values[0][i] != -1.)
			{
				ttcsInHoleDist[1] = distances[i];
				break;
			}
		}
		boolean in_hole = distance > ttcsInHoleDist[0]
				&& distance < ttcsInHoleDist[1];

		// ====================================================================
		// Set Depth Range
		// ====================================================================
		if (depths.length == 1)
		{
			// ------------------------------------------------------------------
			// CASE A: Only 1 depth sample available
			// ------------------------------------------------------------------
			ztop = 0;
			zbottom = 0;
			if (depth != depths[0])
				return WRN_INVALID_VALUE;
		}
		else
		{

			// ------------------------------------------------------------------
			// CASE B: Table contains at least 2 depth samples
			// ------------------------------------------------------------------
			zleft = hunt(depths, depth);

			if (zleft < 0) // depth < min. table depth
			{
				// Check if exactly equal
				if (abs(depth - depths[0]) < DEPTH_PROXIMITY_TOLERANCE)
					zleft = 0;
				else
					idepth--;
				ztop = 0;
				zbottom = nz_req - 1;
			}
			else if (zleft >= depths.length - 1) // depth > max. table depth
			{
				idepth++;
				ztop = depths.length - nz_req;
				zbottom = depths.length - 1;
			}
			else
				// requested depth within valid range
			{
				zbottom = min(zleft + (nz_req / 2), depths.length - 1);
				ztop = max(zbottom - nz_req + 1, 0);
				nz_req = zbottom - ztop + 1;
			}
		}

		// ====================================================================
		// Set Distance Range
		// ====================================================================
		// --------------------------------------------------------------------
		// Preliminary Bracketing
		// --------------------------------------------------------------------
		xleft = hunt(distances, distance);

		if (xleft < 0)
		{
			// Case 1: distance < minimum table distance
			// Check if exactly equal
			if (abs(distance - distances[0]) < DISTANCE_PROXIMITY_TOLERANCE)
				xleft = 0;
			else
				idist--;
			xlow = 0;
			xhigh = nx_req - 1;
		}

		else if (xleft >= distances.length - 1)
		{
			// Case 2: distance > maximum table distance
			idist++;
			xlow = distances.length - nx_req;
			xhigh = distances.length - 1;
		}

		else
		{
			// Case 3: distance within valid table region

			// Distance is within a valid table region, but may not have a
			// valid value. Interogate table in order to obtain as many
			// valid values as possible for either direct interpolation or
			// eventual extrapolation. This is determined by the xlow and
			// xhigh settings.

			// Make sure that high and low end requested does not run us
			// off one side of the distance curve or the other. We need
			// to do this even before we check the actual values contained
			// in the 2-D (x-z) array.

			xhigh = min(xleft + (nx_req / 2), distances.length - 1);
//			if (xhigh == distances.length - 1)
//				xlow = distances.length - nx_req;
			xlow = max(xhigh - nx_req + 1, 0);
			if (xlow == 0)
				xhigh = nx_req - 1;
		}

		// --------------------------------------------------------------------
		// If we are not in a valid region of the table and if no
		// extrapolation has been requested, then we are done.
		// --------------------------------------------------------------------
		if ((idist != 0 || idepth != 0 || in_hole) && !ttExtrapolate)
		{
			if (in_hole)
				return 11;
			else if (idist < 0 && idepth == 0)
				return 12;
			else if (idist > 0 && idepth == 0)
				return 13;
			else if (idist == 0 && idepth < 0)
				return 14;
			else if (idist == 0 && idepth > 0)
				return 15;
			else if (idist < 0 && idepth < 0)
				return 16;
			else if (idist > 0 && idepth < 0)
				return 17;
			else if (idist < 0 && idepth > 0)
				return 18;
			else if (idist > 0 && idepth > 0)
				return 19;
			else
				return 0;
		}

		// --------------------------------------------------------------------
		// Final Adjustment of Distance Range Bounds
		//
		// If requested distance sample is within table bounds, then we
		// need to find as many valid samples as possible. If none exists
		// shift xlow and xhigh closest to a valid curve. On the other
		// hand, if the requested distance sample is located clearly
		// outside the valid table region, create an artificial mini-table
		// surrounding the requested sample distance value.
		// --------------------------------------------------------------------
		if (in_hole)
		{
			// ------------------------------------------------------------------
			// Case 1: Requested Distance is in a Hole
			// ------------------------------------------------------------------

			// Check outer distance value
			if (invalid(values[ztop][xhigh]))
			{
				// Case A: Outer distance sample is also in the hole

				for (i = 0; i < (nx_req - 1) / 2; i++)
				{
					// Shift upper distance bound downward until a valid table
					// entry is found
					--xhigh;
					if (valid(values[ztop][xhigh]))
						break;
				}
				// Reset lower distance bound to reflect the new upper bound
				xlow = xhigh - nx_req + 1;
			}
			else
			{
				// Case B: Outer distance sample is valid

				//**** added distance shift up, similar to distance shift down above.
				//**** Not sure why this was'nt done properly here.
				//****  (jrh 12-19-2018)
				for (i = 0; i < (nx_req - 1) / 2; i++)
				{
					// Shift upper distance bound downward until a valid table
					// entry is found
					++xlow;
					if (valid(values[ztop][xlow]))
						break;
				}
				// Reset higher distance bound to reflect the new lower bound
				xhigh = xlow + nx_req - 1;

				// Use a "safe" value from the distances before the hole
				//xhigh = 109;
				//xlow = xhigh - nx_req + 1;
			}
		}
		else if (idist == 0)
		{
			// ------------------------------------------------------------------
			// Case 2: Distance w/in Table, but not in a hole
			// ------------------------------------------------------------------

			// Check to see if the upper distance bound is in a bad sample region
			// Check outer distance value
			if (valid(values[ztop][0])
					&& invalid(values[ztop][xhigh]))
			{
				idist = 1;
				for (i = 0; i < (nx_req - 1) / 2; i++)
				{
					// Shift upper distance bound downward until a valid table
					// entry is found
					--xhigh;
					if (valid(values[ztop][xhigh]))
					{
						idist = 0;
						break;
					}
				}
				// Reset lower distance bound to reflect the new upper bound
				xlow = xhigh - nx_req + 1;
			}
			// Check to see if the lower distance bound is in a bad sample
			// region
			else if (invalid(values[ztop][xlow]))
			{
				idist = -1;
				for (i = 0; i < (nx_req - 1) / 2; i++)
				{
					// Shift lower distance bound upward until a valid table
					// entry is found
					++xlow;
					if (valid(values[ztop][xlow]))
					{
						idist = 0;
						break;
					}
				}
				// Reset upper distance bound to reflect the new upper bound
				xhigh = xlow + nx_req - 1;
			}
		}

		// ====================================================================
		// Construct Mini Table
		//
		// Up to now we have only inspected the 1st depth component on the
		// distance vector. Now we will build a complete mini-table which
		// will be used for actual inter/extrapolation using rational
		// function and bi-cubic spline interpolation routines.
		// ====================================================================

		double[][] mini_table = new double[nz_req][];
		double[][] deriv_2nd = new double[nz_req][nx_req];

		double[] mini_dist = extract(distances, xlow, nx_req);
		double[] mini_depth = extract(depths, ztop, nz_req);

		// --------------------------------------------------------------------
		// First, construct mini-table assuming no depth extrapolation is
		// needed. All distance extrapolation will be handled in this master
		// "for loop".
		// --------------------------------------------------------------------
		for (k = 0, kk = ztop; k < nz_req; k++, kk++)
		{
			// First fill mini_table assuming all values[][] values are valid
			mini_table[k] = extract(values[kk], xlow, nx_req);
			//double[] min_table_row = mini_table[k].clone();

			// ------------------------------------------------------------------
			// Check the distance value with respect to range of distances
			// ------------------------------------------------------------------
			if (in_hole || idist > 0)
			{
				// ----------------------------------------------------------------
				// Case 1: Off high end of distance curve -OR-
				// in a hole
				// ----------------------------------------------------------------
				diff = distance - distances[xhigh];
				if (idist > 0 && diff > 1e-9)
				{
					// Case A: Off the high end of the distance curve
					//
					// Shift the distances associated with the mini table out to
					// a region centered about the requested distance
					xshift = distance - distances[xhigh - ((nx_req - 1) / 2)];
					for (j = 0; j < nx_req; j++)
					{
						if (k < 1)
							mini_dist[j] = mini_dist[j] + xshift;
						mini_table[k][j] = BAD_SAMPLE;
					}
					i = xlow;
				}

				else
				{
					// Case B: In a hole in the distance curve

					//**** Added check for xhigh or xlow having an invalid value. It is
					//**** possible when the interpolation point is in a hole, but not
					//**** shifted left or right, to have either end with invalid points.
					//**** (jrh 12-18-2018)
					if (valid(values[kk][xhigh]))
					{
						for (i = xlow; i < distances.length; i++)
						{
							// Look for the first good value scanning upward from
							// the
							// lower distance bound
							if (valid(values[kk][i]))
								break;
						}
					}
					else
					{
						// Scanning downward in distance, look for valid values in
						// the table to use for extrapolation fill-in of the mini table
						for (i = xhigh; i >= 0; i--)
						{
							// Look for the first good value scanning downward from
							// the upper distance bound
							if (valid(values[kk][i]))
								break;
						}
						i = i - nx_req + 1;
					}
				}

				//**** added two rows below to get valid rational spline interpolant
				//****  (jrh 12-18-2018)
				double[] min_table_row_ex = extract(values[kk], i, nx_req);
				double[] mini_dist_ex = extract(distances, i, nx_req);
				
				// At this depth (k) in the mini-table, extrapolate any missing
				// values along the distance direction
				for (j = 0; j < nx_req; j++)
					if (invalid(mini_table[k][j]))
						//**** replaced arguments mini_dist and min_table_row with their
						//**** properly shifted versions mini_dist_ex and min_table_row_ex
						//**** (jrh 12-18-2018)
						mini_table[k][j] = ratint(mini_dist_ex, min_table_row_ex, mini_dist[j]);
				
			} // End if (in_hole || idist > 0)

			else if (idist < 0)
			{
				// ----------------------------------------------------------------
				// Case 2: Off low end of distance curve
				// ----------------------------------------------------------------
				if (distance < distances[xlow])
				{
					// Shift the distances associated with the mini table down
					// to
					// a region centered about the requested distance
					xshift = distance - distances[xlow + ((nx_req - 1) / 2)];
					for (j = 0; j < nx_req; j++)
					{
						if (k < 1)
							mini_dist[j] = mini_dist[j] + xshift;
						mini_table[k][j] = BAD_SAMPLE;
					}
					i = xlow;
				}
				else
				{
					// Scanning upward in distance, look for valid values in the
					// table to use for extrapolation fill-in of the mini table
					for (i = xlow; i < distances.length; i++)
					{
						// Look for the first good value scanning upward from
						// the
						// lower distance bound
						if (valid(values[kk][i]))
							break;
					}
				}

				//**** added two rows below to get valid rational spline interpolant
				//****  (jrh 12-18-2018)
				double[] min_table_row_ex = extract(values[kk], i, nx_req);
				double[] mini_dist_ex = extract(distances, i, nx_req);

				// At this depth (k) in the mini-table, interpolate any missing
				// values along the distance direction
				for (j = 0; j < nx_req; j++)
					if (invalid(mini_table[k][j]))
						//**** replaced arguments mini_dist and min_table_row with their
						//**** properly shifted versions mini_dist_ex and min_table_row_ex
						//**** (jrh 12-18-2018)
						mini_table[k][j] = ratint(mini_dist_ex, min_table_row_ex, mini_dist[j]);
			}

			else
			{
				// ----------------------------------------------------------------
				// Case 3: Distance is at a valid range in the distance vector
				//
				// Make sure there are no single BAD_SAMPLE entries. If so,
				// extrapolate as necessary.
				// ----------------------------------------------------------------
				for (j = 0; j < nx_req; j++) // Scan distances (j) in the mini
				{ // table at this depth (k)
					if (invalid(mini_table[k][j]))
					{
						if (j > 0)
						{
							// Go back and get as many valid samples for this
							// depth as is possible for a good sample space.

							num_extrap = nx_req - j;
							i = xlow - num_extrap;
							num_samp = nx_req;
							while (i < 0 || invalid(values[kk][i]))
							{
								++i;
								--num_samp;

								// check for minimum sample number

								if (num_samp < MIN_NUM_DIST_SAMPLES)
									return WRN_INSUFFICIENT_DATA;
							}

							// Extrapolate a valid traveltime for the mini table
							for (n = 0; n < num_extrap; n++)
							{
								m = j + n;
								if (invalid(mini_table[k][m]))
								{
									mini_table[k][m] = ratint(
											extract(distances, i, num_samp),
											extract(values[kk], i, num_samp),
											mini_dist[m]);
								}
							}
						}
						else
						{
							// Advance in distance and get as many valid samples
							// for
							// this depth as is possible for a good sample
							// space.

							for (n = 0, i = xlow; i < xhigh; i++, n++)
							{
								if (valid(values[kk][i]))
								{
									xlow = i;
									num_extrap = n;
									for (num_samp = 0, n = 0; n < nx_req; n++)
										if (valid(values[kk][xlow + n]))
											++num_samp;

									// check for minimum sample number

									if (num_samp < MIN_NUM_DIST_SAMPLES)
										return WRN_INSUFFICIENT_DATA;
									break;
								}
							}

							// Check for at least 1 sample

							if (i == xhigh)
								return WRN_INSUFFICIENT_DATA;

							// create mini table

							for (n = 0; n < num_extrap; n++)
							{
								if (invalid(mini_table[k][n]))
								{
									mini_table[k][n] = ratint(
											extract(distances, i, num_samp),
											extract(values[kk], i, num_samp),
											mini_dist[n]);
								}
							}
						}
						break;
					}
				}
			}
		}

		// If only one depth component exists wrap it up here.

		if (depths.length == 1)
		{
			// Perform a one-dimensional cubic spline interpolation on a
			// single row of mini_table[][] to get the desired value for
			// special case of only one depth available in the table.

			try
			{
				spline(mini_dist, mini_table[0], nx_req, 1.0e30, 1.0e30,
						deriv_2nd[0]);
				splint_deriv(mini_dist, mini_table[0], deriv_2nd[0], nx_req,
						distance, results);
			}
			catch (GMPException e)
			{
				Arrays.fill(results, Double.NaN);
				return WRN_INVALID_VALUE;
			}

			if (in_hole)
				return 11;
			else if (idist < 0 && idepth == 0)
				return 12;
			else if (idist > 0 && idepth == 0)
				return 13;
			else if (idist == 0 && idepth < 0)
				return 14;
			else if (idist == 0 && idepth > 0)
				return 15;
			else if (idist < 0 && idepth < 0)
				return 16;
			else if (idist > 0 && idepth < 0)
				return 17;
			else if (idist < 0 && idepth > 0)
				return 18;
			else if (idist > 0 && idepth > 0)
				return 19;

			return 0;
		}

		// Now that the distance component of the mini-table is secure,
		// perform any necessary extrapolation for the depth component by
		// re-constructing the mini-table. Also, build transposed mini-
		// table, mini_table_trans[][], to obtain distance derivatives
		// from spline routines below.

		double[][] mini_table_trans = new double[nx_req][nz_req];
		double[][] deriv_2nd_trans = new double[nx_req][nz_req];

		for (j = 0; j < nx_req; j++)
		{
			// Fill mini_table_trans[][] assuming all values from array,
			// mini_table[][], are valid

			for (i = 0; i < nz_req; i++)
				mini_table_trans[j][i] = mini_table[i][j];

			// Are we below the lowest depth component in the curve

			if (idepth > 0)
			{
				// Case 1: Off the deep end of the depth range
				zshift = depth - depths[zbottom - ((nz_req - 1) / 2)];
				if (j < 1)
				{
					for (i = 0; i < nz_req; i++)
						mini_depth[i] = mini_depth[i] + zshift;
				}
				// Extrapolate a new set of depths bracketing the requested
				// depth
				for (i = 0; i < nz_req; i++)
				{
					mini_table[i][j] = ratint(extract(depths, ztop, nz_req),
							extract(mini_table_trans[j], 0, nz_req),
							mini_depth[i]);
				}
				// Create Transpose mini table
				for (i = 0; i < nz_req; i++)
					mini_table_trans[j][i] = mini_table[i][j];
			}
			else if (idepth < 0)
			{
				// Case 2: Off the shallow end of the depth range
				zshift = depth - depths[ztop + ((nz_req - 1) / 2)];
				if (j < 1)
				{
					for (i = 0; i < nz_req; i++)
						mini_depth[i] = mini_depth[i] + zshift;
				}
				// Extrapolate a new set of depths bracketing the requested
				// depth
				for (i = 0; i < nz_req; i++)
				{
					mini_table[i][j] = ratint(extract(depths, ztop, nz_req),
							extract(mini_table_trans[j], 0, nz_req),
							mini_depth[i]);
				}
				// Create Transpose mini table
				for (i = 0; i < nz_req; i++)
					mini_table_trans[j][i] = mini_table[i][j];
			}
		}
		
		if (!ttExtrapolate)
		{
			if (in_hole)
				return 11;
			else if (idist < 0 && idepth == 0)
				return 12;
			else if (idist > 0 && idepth == 0)
				return 13;
			else if (idist == 0 && idepth < 0)
				return 14;
			else if (idist == 0 && idepth > 0)
				return 15;
			else if (idist < 0 && idepth < 0)
				return 16;
			else if (idist > 0 && idepth < 0)
				return 17;
			else if (idist < 0 && idepth > 0)
				return 18;
			else if (idist > 0 && idepth > 0)
				return 19;
		}



		// Now we have both mini-tables and can perform 2-D bi-cubic
		// spline interpolations on our mini-tables to obtain our value
		// of interest and 1st and 2nd derivatives in both the distance
		// and depth directions. Note that the bi-cubic splines routines
		// need to be called twice in order to obtain the derivatives in
		// first the distance direction and then the depth direction.
		// If do_we_need_z_derivs is set false, then depth derivatives
		// do not need to be computed.

		try
		{
			splie2(mini_dist, mini_depth, mini_table_trans, nx_req, nz_req, deriv_2nd_trans);

			splin2(mini_dist, mini_depth, mini_table_trans, deriv_2nd_trans,
					nx_req, nz_req, distance, depth, results);

			if (ttZDerivatives)
			{
				// interpolated value, first and second derivatives in z direction.
				double[] z = new double[3];
				splie2(mini_depth, mini_dist, mini_table, nz_req, nx_req, deriv_2nd);
				splin2(mini_depth, mini_dist, mini_table, deriv_2nd, nz_req,
						nx_req, depth, distance, z);
				results[0] = z[0];
				results[3] = z[1];
				results[4] = z[2];
			}

			if (shZDerivatives)
			{
				double[] results2 = new double[6];
				interpolate(distance, depth-TT_DEL_DEPTH, false, false, ttExtrapolate, results2);
				double slowness = results2[1];
				interpolate(distance, depth+TT_DEL_DEPTH, false, false, ttExtrapolate, results2);
				results[5] = (results2[1]-slowness)/(2. * TT_DEL_DEPTH);
			}
		}
		catch (GMPException e)
		{
			Arrays.fill(results, Double.NaN);
			return WRN_INVALID_VALUE;
		}

		if (in_hole)
			return 11;
		else if (idist < 0 && idepth == 0)
			return 12;
		else if (idist > 0 && idepth == 0)
			return 13;
		else if (idist == 0 && idepth < 0)
			return 14;
		else if (idist == 0 && idepth > 0)
			return 15;
		else if (idist < 0 && idepth < 0)
			return 16;
		else if (idist > 0 && idepth < 0)
			return 17;
		else if (idist < 0 && idepth > 0)
			return 18;
		else if (idist > 0 && idepth > 0)
			return 19;

		return 0;
	}

	/**
	 * Extract a new array from x that has size elements starting at index
	 * first. No range checking is performed!
	 * 
	 * @param x
	 *            original array
	 * @param first
	 *            index of first element
	 * @param size
	 *            number of elements to extract
	 * @return
	 */
	private double[] extract(double[] x, int first, int size)
	{
		double[] xx = new double[size];
		for (int i = 0; i < size; ++i)
			xx[i] = x[first + i];
		return xx;
	}
	
	private boolean valid(double value)
	{
		return !invalid(value);
	}

	private boolean invalid(double value)
	{
		//return abs(value-BAD_SAMPLE) < 1e-3;
		return value == BAD_SAMPLE;
	}

	/**
	 * Rational Polynomial Interpolation / Extrapolation Function
	 *
	 * Given arrays xa[0..n-1] and ya[0..n-1], and given a value, x, this
	 * routine returns a value of y and an accuracy estimate, dy. The value
	 * returned is that of the diagonal rational function, evaluated at x,
	 * which passes through the n points (xa[i], ya[i]), i = 0..n-1.)
	 *
	 * @param xa - Pointer to a vector of x values.
	 * @param ya - Pointer to a vector of y values.
	 * @param n - Length of vectors.
	 * @param x - Value at which to interpolate / extrapolate.
	 * @param y - Interpolated value.
	 * @param dy - Error estimate of computed value.
	 *
	 * @return - Error flag indicating that the interpolating function has a
	 * pole at the requested \em x value ... (0 = no error / pole; 1 = error
	 * occurred due to pole).
	 */
	private double ratint(double[] xa, double[] ya, double x)
	{
		double dd, h, hh, t, w;
		int ns = 0, n = xa.length;
		double[] c = new double[n];
		double[] d = new double[n];

		double y;

		hh = abs(x - xa[0]);

		for (int i = 0; i < n; i++)
		{
			h = abs(x - xa[i]);
			if (h == 0.0)
				return ya[i];
			else if (h < hh)
			{
				ns = i;
				hh = h;
			}
			c[i] = ya[i];
			d[i] = ya[i] + FLOAT_EPSILON; /* Needed to prevent a rare */
		}                                 /* zero-over-zero condition */

		y = ya[ns--];

		for (int m = 0; m < n - 1; m++)
		{
			for (int i = 0; i < n - 1 - m; i++)
			{
				w = c[i + 1] - d[i];
				h = xa[i + m + 1] - x;
				t = (xa[i] - x) * d[i] / h;
				dd = t - c[i + 1];

				// Interpolating function has a pole at the requested value of x.
				// Return error
				//if (dd == 0.0)
				if (abs(dd) < RATINT_POLE_THRESHOLD)
					return Double.NaN;

				dd = w / dd;
				d[i] = c[i + 1] * dd;
				c[i] = t * dd;
			}
			y += 2 * (ns + 1) < (n - m - 1) ? c[ns + 1] : d[ns--];
		}
		return y;
	}

	/**
	 * \brief Function to Construct Natural Cubic Splines Along The Rows of a
	 * Rectangular 2-D Grid.
	 *
	 * Given a tabulated function ysf[0..nslow-1][0..nfast-1], and tabulated
	 * independent variables xs[0..nslow-1] and xf[0..nfast-1], this routine
	 * constructs one-dimensional natural cubic splines of the rows of ysf and
	 * returns the second derivatives in the array y2sf[0..nslow-1][0..nfast-1].
	 * This routine only needs to be called once, and then, any number of
	 * bi-cubic spline interpolations can be performed by successive calls to
	 * splin2.
	 *
	 * Based On the function "splie2":
	 * Press, W.H. et al., 1988, "Numerical Recipes", 94-110.
	 *
	 * @param xs - Independent variable for slow index into matrix.
	 * @param xf - Independent variable for fast index into matrix.
	 * @param ysf - Matrix of tablulated y-values ysf[xs][xf].
	 * @param nslow - Size of slow dimension of matrix.
	 * @param nfast - Size of fast dimension of matrix.
	 * @param y2sf - Matrix of computed second derivatives returned to the
	 * caller.
	 */
	private void splie2(double[] xs, double[] xf, double[][] ysf, int nslow, int nfast,
			double[][] y2sf)
	{ for (int j = 0; j < nslow; j++) spline(xf, ysf[j], nfast, 1.0e30, 1.0e30, y2sf[j]); } 

	/**
	 * \brief Function to Perform Cubic Spline Interpolation at a Point
	 * Bracketed by 1-D Cubic Splines.
	 *
	 * Given xs, xf, ysf, nslow, nfast as described in function
	 * splie2() and y2sf as produced by that routine; and given a desired
	 * interpolating point rxs, rxf; this routine returns an interpolated
	 * function value rysf by bi-cubic spline interpolation. The
	 * complemetary routine, splie2(), needs to be called once prior to
	 * accessing this function to initializize natural splines and 2nd
	 * derivatives.
	 *
	 * This is accomplished by constructing row, then column splines,
	 * one-dimension at a time.
	 *
	 * Based On the function "splin2":
	 * Press, W.H. et al., 1988, "Numerical Recipes", 94-110.
	 *
	 * @param xs - Independent variable for slow index into matrix.
	 * @param xf - Independent variable for fast index into matrix.
	 * @param ysf - Matrix of tablulated y-values ysf[xs][xf].
	 * @param nslow - Size of slow dimension of matrix.
	 * @param nfast - Size of fast dimension of matrix.
	 * @param y2sf - Matrix of computed second derivatives.
	 * @param rxs - Value Along slow index at which to interpolate.
	 * @param rxf - Value Along fast index at which to interpolate.
	 * @param rysf - Returned interpolated value.
	 * @param drysf - First derivative of interpolated value in the slow
	 * direction.
	 * @param d2rysf - Second derivative of interpolated value in the slow
	 * direction.
	 */
	private void splin2(double[] xs, double[] xf, double[][] ysf, double[][] y2sf,
			int nslow, int nfast, double rxs, double rxf, double rysf[])
					throws GMPException
					{
		double[] ytmp = new double[nslow];
		double[] y2tmp = new double[nslow];

		// calculate nslow y2tmp entries from splint
		for (int j = 0; j < nslow; j++)
			y2tmp[j] = splint(xf, ysf[j], y2sf[j], nfast, rxf);

		// calculate nslow ytmp entries in spline and interpolate for rysf,
		// drysf, and d2rysf

		spline(xs, y2tmp, nslow, 1.0e30, 1.0e30, ytmp);
		splint_deriv(xs, y2tmp, ytmp, nslow, rxs, rysf);

					} // END splin2

	/**
	 * Cubic Spline Construction Function.
	 *
	 * Function. Given arrays x[0..n-1] and y[0..n-1] containing a
	 * tabulated function, i.e., y[i] = f(x[i]), with x[0] < x[1] < x[n-1],
	 * and given values yp1 and ypn for the first derivative of the
	 * interpolating function at points 0 and n-1, respectively, this
	 * routine returns an array y2[0..n-1] that contains the 2nd i
	 * derivatives of the interpolating function at the tabulated points
	 * x[i].
	 *
	 * If yp1 and/or ypn are equal to 1.0e30 or larger, the routine is
	 * signalled to set the corresponding boundary condition for a natural
	 * spline, with zero second derivative on that boundary.
	 *
	 * NOTE: This routine only needs to be called once to process the
	 * entire tabulated function in x and y arrays.
	 *
	 * Based On the function "spline":
	 * Press, W.H. et al., 1988, "Numerical Recipes", 94-110.
	 *
	 *
	 * @param x - An input vector of independent values of a cubic spline.
	 * @param y - An input vector of dependent values of the cubic spline
	 * defined on the values xa.
	 * @param n - Number of elements in the vectors.
	 * @param yp1 - Value of dy/dx evaluated at x[0].
	 * @param ypn - Value of dy/dx evaluated at x[n-1].
	 * @param y2 - A Vector of second derivatives defined on xa.
	 */
	private void spline(double[] x, double[] y, int n, double yp1, double ypn,
			double[] y2)
	{
		int i, k;
		double p, qn, sig, un;
		double[] u = new double[n];

		// calculate temporary u vector
		if (yp1 > 0.99e30)
			y2[0] = 0.0;
		else
		{
			y2[0] = -0.5;
			u[0] = ((3.0 / (x[1] - x[0])) * ((y[1] - y[0]) / (x[1] - x[0]) - yp1));
		}

		// Decomposition loop for tridiagonal algorithm

		for (i = 1; i < n - 1; i++)
		{
			sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
			p = sig * y2[i - 1] + 2.0;
			y2[i] = (sig - 1.0) / p;
			u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1])
					/ (x[i] - x[i - 1]);
			u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
		}
		if (ypn > 0.99e30)
			qn = un = 0.0;
		else
		{
			qn = 0.5;
			un = (3.0 / (x[n - 1] - x[n - 2]))
					* (ypn - (y[n - 1] - y[n - 2]) / (x[n - 1] - x[n - 2]));
		}

		// Back substituition loop of tridiagonal algorithm

		y2[n - 1] = (un - qn * u[n - 2]) / (qn * y2[n - 2] + 1.0);
		for (k = n - 2; k >= 0; k--)
			y2[k] = y2[k] * y2[k + 1] + u[k];

	} // END spline

	/**
	 * Cubic Spline Interpolation Function
	 *
	 * Returns an interpolated value of of a cubic spline given its defining
	 * values and second derivatives.
	 *
	 * Based On the function "splint":
	 * Press, W.H. et al., 1988, "Numerical Recipes", 94-110.
	 *
	 * @param xa - An input vector of independent values of a cubic spline.
	 * @param ya - An input vector of dependent values of the cubic spline
	 * defined on the values xa.
	 * @param y2a - A Vector of second derivatives defined on xa.
	 * @param n - Number of elements in the vectors.
	 * @param x - Location to interpolate.
	 * @param y - Interpolated value at x.
	 */
	private double splint(double[] xa, double[] ya, double[] y2a, int n, double x)
			throws GMPException
			{
		int klo, khi, k;
		double h, b, a;

		klo = 0;
		khi = n - 1;
		while (khi - klo > 1)
		{
			k = (khi + klo) >> 1;
			if (xa[k] > x)
				khi = k;
			else
				klo = k;
		} /* klo and khi now bracket the input value of x */

		h = xa[khi] - xa[klo];
		if (h == 0)
			throw new GMPException("WRN_INVALID_VALUE in splint()");

		a = (xa[khi] - x) / h;
		b = (x - xa[klo]) / h;
		return a * ya[klo] + b * ya[khi]
				+ ((a * a * a - a) * y2a[klo] + (b * b * b - b) * y2a[khi])
				* (h * h) / 6.0;

			} // END splint

	/**
	 * Cubic Spline Interpolation Function w/ Derivatives.
	 *
	 * Returns an interpolated value of of a cubic spline given its defining
	 * values and second derivatives. The first and second derivatives are
	 * also computed and returned.
	 *
	 * Based On the function "splint":
	 * Press, W.H. et al., 1988, "Numerical Recipes", 94-110.
	 *
	 * @param xa - An input vector of independent values of a cubic spline.
	 * @param ya - An input vector of dependent values of the cubic spline
	 * defined on the values xa.
	 * @param y2a - A Vector of second derivatives defined on xa.
	 * @param n - Number of elements in the vectors.
	 * @param x - Location to interpolate.
	 * @param y - Interpolated value at x.
	 * @param dy - Interpolated first derivative at x.
	 * @param d2y - Interpolated 2nd derivative at x.
	 */
	private void splint_deriv(double[] xa, double[] ya, double[] y2a, int n, double x, double[] y) throws GMPException
	{
		int klo, khi, k;
		double h, b, a;

		klo = 0;
		khi = n - 1;
		while (khi - klo > 1)
		{
			k = (khi + klo) >> 1;
		if (xa[k] > x)
			khi = k;
		else
			klo = k;
		} /* klo and khi now bracket the input value of x */

		h = xa[khi] - xa[klo];
		if (h == 0)
			throw new GMPException("WRN_INVALID_VALUE in splintderiv");

		a = (xa[khi] - x) / h;
		b = (x - xa[klo]) / h;
		y[0] = a * ya[klo] + b * ya[khi]
				+ ((a * a * a - a) * y2a[klo] + (b * b * b - b) * y2a[khi])
				* (h * h) / 6.0;
		y[1] = ((ya[khi] - ya[klo]) / h)
				- (((3.0 * a * a - 1.0) * h * y2a[klo]) / 6.0)
				+ (((3.0 * b * b - 1.0) * h * y2a[khi]) / 6.0);
		y[2] = a * y2a[klo] + b * y2a[khi];

	} // END splint_deriv

	private int hunt(double[] values, double x)
	{
		if (x == values[values.length - 1])
			return values.length - 2;

		int i;
		int bot = -1;
		int top = values.length;
		while (top - bot > 1)
		{
			i = (top + bot) / 2;
			if (x >= values[i])
				bot = i;
			else
				top = i;
		}
		return bot;
	}
	
	static public String getErrorMessage(int code)
	{
		String message = messages.get(code);
		return message == null ? "invalid code "+Integer.toString(code) : message;
	}

}
