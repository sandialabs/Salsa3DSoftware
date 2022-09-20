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
package gov.sandia.gmp.util.numerical.loglinearinterpolator;

import java.io.IOException;
import java.io.Serializable;

/**
 * Performs min and max clamped linear-linear, log-linear, linear-log, and
 * log-log interpolation. The constructor takes the independent axis (X)
 * minimum and maximum values and a flag indicating if the axis is log scale,
 * and the dependent (Y) minimum and maximum values and a flag indicating if
 * the axis is log scale.
 * 
 * The interpolation function takes an input independent (X) value and
 * returns the corresponding interpolated dependent (Y) result.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class ClampedLogLinearInterpolator implements Serializable
{
	/**
	 * Minimum X axis limit below which aY0 is returned.
	 */
	private double          aX0 = 1.0;
	
	/**
	 * Maximum X axis limit above which aY1 is returned.
	 */
	private double          aX1 = 2.0;
	
	/**
	 * X axis log flag.
	 */
	private boolean         aLogX = false;

	/**
	 * Minimum Y axis limit returned if input X axis value is <= aX0.
	 */
	private double          aY0 = 1.0;

	/**
	 * Maximum Y axis limit returned if input X axis value is >= aX1.
	 */
	private double          aY1 = 2.0;

	/**
	 * Y axis log flag.
	 */
	private boolean         aLogY = false;

	/**
	 * X axis minimum value or log of minimum value.
	 */
	private double          afX0  = 1.0;

	/**
	 * Y axis minimum value or log of minimum value.
	 */
	private double          afY0  = 1.0;
	
	/**
	 * Interpolation slope.
	 */
	private double          aA    = 1.0;

	/**
	 * Standard constructor.
	 * 
	 * @param x0   Minimum X value.
	 * @param x1   Maximum X value.
	 * @param lgx  X axis log scale flag (if true). 
	 * @param y0   Minimum Y value.
	 * @param y1   Maximum Y value.
	 * @param lgy  Y axis log scale flag (if true).
	 */
	public ClampedLogLinearInterpolator(double x0, double x1, boolean lgx,
			                                double y0, double y1, boolean lgy)
	       throws IOException
	{
		// validate
		
		if (aX1 <= aX0)
			throw new IOException("   Invalid Inputs: Input x0 must be > x1 ...");
		else if (aLogX && aX0 <= 0.0)
			throw new IOException("   Invalid Inputs: Log X axis requires x0 > 0.0 ...");
		else if (aLogY && aY0 <= 0.0)
			throw new IOException("   Invalid Inputs: Log Y axis requires y0 > 0.0 ...");
		else if (aLogY && aY1 <= 0.0)
			throw new IOException("   Invalid Inputs: Log Y axis requires y1 > 0.0 ...");

		// initialize inputs

		aX0   = x0;
		aX1   = x1;
		aLogX = lgx;
		aY0   = y0;
		aY1   = y1;
		aLogY = lgy;

		// set functional X axis limits

		double fx1;
		if (aLogX)
		{
			afX0 = Math.log10(aX0);
			fx1  = Math.log10(aX1);
		}
		else
		{
			afX0 = aX0;
			fx1  = aX1;
		}

		// set functional Y axis limits

		double fy1;
		if (aLogY)
		{
			afY0 = Math.log10(aY0);
			fy1  = Math.log10(aY1);
		}
		else
		{
			afY0 = aY0;
			fy1  = aY1;
		}

		// calculate slope

		aA = (fy1 - afY0) / (fx1 - afX0);
	}

	/**
	 * Interpolate the input x value. If x <= aX0 then aY0 is returned, else if
	 * x >= aX1 then aY1 is returned. Otherwise, the interpolated result is
	 * returned.
	 * 
	 * @param x  Independent x axis input.
	 * @return   The interpolated y axis result.
	 */
	public double interpolate(double x)
	{
		// if x exceeds limits return appropriate Y axis limit ... otherwise
		// interpolate

		if (x <= aX0)
			return aY0;
		else if (x >= aX1)
			return aY1;
		else
		{
			// interpolate ... take log of x if requested

  		if (aLogX) x = Math.log10(x);
  		double fy = aA * (x - afX0) + afY0;
  		
  		// return result or 10^fy if Y axis is log

  		if (aLogY)
  			return Math.pow(10.0, fy);
  		else
  			return fy;
		}
	}
}
