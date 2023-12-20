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
package gov.sandia.gmp.baseobjects.hyperellipse_old;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.testingbuffer.Buff;

/**
 * <p>Title: LocOOJava</p>
 *
 * A class to manage a 2D ellipse.
 *
 * The class keeps track of 3 things:
 *   center:  a 4-element vector that contains the position of the center of the
 *            ellipse.  The components are LAT, LON, DEPTH, TIME in the order
 *            specified in LocooDefines.h
 *
 *   coeff:   a 4-element vector that contains the coefficients of the equation
 *            that defines the ellipse.  The equation is
 *            c0*x*x + c1*x*y + c2*y*y = c3  where x is latitude, y is longitude.
 *            For projected ellipses, c3 = 1 defines the ellipse that corresponds
 *            to the contour in location space where delta chi square = 1.  The
 *            ellipse can be scaled to reflect current statistical parameters.
 *            Scaling simply sets the value of the c3 coefficient to a specified
 *            value.
 *
 *   principal axes:  a 3-element vector containing the length of the major semi-
 *            axis of the ellipse, the length of the minor semi-axis and
 *            the orientation of the trend of the major axis relative to the x
 *            axis (in radians).  The units of the semi-axes are arbitrary.
 *
 * The class is initialized by specifying the center and equation coefficients.
 * The principal axes are calculated internally.
 *
 * The class has a public method to calculate the distance from the center of
 * the ellipse to its perimenter in a direction specified by a pair of
 * direction cosines ralative to the x and y axes.
 *
 * Written By:  Sandy Ballard
 *              Sandia National Laboratories
 *              November 2002
 *
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Sandia National Laboratories</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public class Ellipse implements Serializable
{
	private static final long serialVersionUID = 6690093413458761801L;
	
	private double[] coeff;
	private Location center;
	private double[] principal_axes;

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Ellipse Constructor
	//
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN:      NONE
	//
	// *****************************************************************************
	public Ellipse()
	{
	}

	@Override
	public Ellipse clone()
	{
		Ellipse e = new Ellipse();
		if (this.center != null)
			e.center = this.center.clone();
		if (this.coeff != null)
			e.coeff = this.coeff.clone();
		if (this.principal_axes != null)
			e.principal_axes = this.principal_axes.clone();
		return e;
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// set coefficients method
	//
	//
	// INPUT ARGS:  4-element containing the center of the ellipse (lat, lon, depth,
	//	              origin time, in order specified in LocooDefines.h.  Units are
	//	              radians, radians, km and seconds.
	//	              4-elements VectorMod containing coeffiecients of the equation for
	//	                the Ellipse.
	//	              c0*x*x + c1*x*y + c2*y*y = c3 where x is latitude, y is longitude
	//
	// OUTPUT ARGS: NONE
	// RETURN:      NONE
	//
	// *****************************************************************************
	public void initialize(Location center, double[] coeff)
	{
		this.center = center;
		this.coeff = coeff;
		if (coeff != null)
		{
			find_principal_axes();
			if (isValid() && !validityTest()) coeff[3] = -1;
		}
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Set the scale factor for the ellipse.  This just sets the value of the
	// c3 coefficient.  For a projected ellipse, c3=1 corresponds to the contour
	// in location space where delta chi square = 1.
	//
	// *****************************************************************************
	public void setScaleFactor(double kappa_sqr)
	{
		if (coeff != null) coeff[3] = kappa_sqr;
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// is this Ellipse valid?
	//
	// *****************************************************************************
	public boolean isValid()
	{
		return (coeff != null && coeff.length == 4 && coeff[3] > 0.);
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// return the principal axes of a 2D ellipse given 4 coefficients of equation.
	// Principal axes are scaled by the square root of the scale factor.
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN:      NONE
	//
	// *****************************************************************************
	public double[] getPrincipalAxes()
	{
		return principal_axes;
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// return the length of the major axis of the ellipse.
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN:      NONE
	//
	// *****************************************************************************
	public double getMajaxLength()
	{
		return principal_axes[0] * Math.sqrt(coeff[3]);
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// return the length of the minor axis of the ellipse.
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN:      NONE
	//
	// *****************************************************************************
	public double getMinaxLength()
	{
		return principal_axes[1] * Math.sqrt(coeff[3]);
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// return the trend of the major axis of the ellipse in radians, from x axis
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN:      NONE
	//
	// *****************************************************************************
	public double getMajaxTrend()
	{
		return principal_axes[2];
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// return the area of the ellipse.
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN:      NONE
	//
	// *****************************************************************************
	public double getArea()
	{
		if (!isValid())
			return 0.;
		double d = 1. / coeff[3];
		double a = coeff[0] * d;
		double b = coeff[1] * d;
		double c = coeff[2] * d;
		double radical = 4 * a * c - b * b;
		if (radical <= 0.)
			return 0.;
		return 2*PI / sqrt(radical);
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// find the principal axes of a 2D ellipse given 4 coefficients of equation.
	// Returns a 3-element array: [major axis, minor_axis, trend]
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN:      NONE
	//
	// *****************************************************************************
	private void find_principal_axes()
	{
		if (principal_axes == null)
			principal_axes = new double[3];

		//if coeff[1] is 0, it means that x and y are uncorrelated, ie. the
		//ellipse is a circle.    Major and minor axes are the same length and
		//there will be no meaningful trend.
		double trend = 0.;
		if (abs(coeff[1]) > 1e-60 * abs(coeff[0] - coeff[2]))
		{
			//calculate the angle between the major axis of the error ellipse and the
			//X-axis.  See lsq_algorithm.pdf eq. 6.7
			double eps = (coeff[0] - coeff[2]) / coeff[1];
			trend = atan( -eps + sqrt(eps * eps + 1));
		}

		//calculate some trig functions of theta
		double ct = cos(trend);
		double cc = ct * ct;
		double st = sin(trend);
		double ss = st * st;

		//calculate the major and minor semi-axes of the error ellipse
		//See lsq_algorithm eqs. 6.9 and 6.10
		double d1 = coeff[0] * cc + coeff[1] * st * ct + coeff[2] * ss;
		double d2 = coeff[0] * ss - coeff[1] * st * ct + coeff[2] * cc;

		// if either, or both, of the semi-axes have length 0, then
		// this ellipse is invalid.  clear the coefficients.
		if (d1 <= 0. || d2 <= 0.)
			Arrays.fill(coeff, Globals.NA_VALUE);
		else
		{
			principal_axes[0] = sqrt(1. / d1);
			principal_axes[1] = sqrt(1. / d2);
			principal_axes[2] = trend;
			//if the minor axis is bigger than the major one, swap them
			if (principal_axes[1] > principal_axes[0])
			{
				principal_axes[0] = sqrt(1. / d2);
				principal_axes[1] = sqrt(1. / d1);
				principal_axes[2] += PI/2.;
			}

			//ensure that the trend is between 0 and PI
			while (principal_axes[2] < 0.)
				principal_axes[2] += PI;
			while (principal_axes[2] >= PI)
				principal_axes[2] -= PI;
		}
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// compute the distance from the center of the ellipse to its perimeter, in
	// the direction specified by the unit VectorMod v.
	// coord sys is 0:north, 1:east
	//
	//
	// *****************************************************************************
	public double distance_to_perimeter(double[] v, boolean scaled)
	{
		// calculate distance from center of ellipse to perimeter of ellipse, in
		// the direction specified by VectorMod v.
		// v is a 2-component unit VectorMod specifying direction in which to look
		// (0:north, 1:east)
		if (!isValid() || coeff[3] == 0.)
			return 0.;
		if (v.length != 2)
			return -1.;
		double scale = 1.;
		if (scaled)
			scale = coeff[3];
		return sqrt(scale /
				(coeff[0] * v[0] * v[0] +
						coeff[1] * v[0] * v[1] +
						coeff[2] * v[1] * v[1]));

	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// test the coefficients of the ellipse to ensure that they actually define
	// and ellipse.
	//
	// See Zwillinger, D., 2003, CRC Standard Mathematical Tables and Formulae, 31st edition
	// p. 329.

	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN:      boolean: true if ellipse is valid, false if invalid
	//
	// *****************************************************************************
	public boolean validityTest()
	{
		double[][] m = new double[3][3];
		m[0][0] = coeff[0];
		m[0][1] = coeff[1] * 0.5;
		m[0][2] = 0.;
		m[1][0] = coeff[1] * 0.5;
		m[1][1] = coeff[2];
		m[1][2] = 0.;
		m[2][0] = 0.;
		m[2][1] = 0.;
		m[2][2] = -coeff[3];
		double d = determinant(m, 3);
		if (d == 0.)
			return false;
		double J = determinant(m, 2);
		if (J <= 0.)
			return false;
		if (d / (coeff[0] + coeff[2]) > 0.)
			return false;
		return true;
	}

	public double determinant(double[][] x, int size)
	{
		if (size == 2)
			return x[0][0] * x[1][1] - x[0][1] * x[1][0];
		else if (size == 3)
			return x[0][0] * x[1][1] * x[2][2] +
			x[0][1] * x[1][2] * x[2][0] +
			x[0][2] * x[1][0] * x[2][1] -
			x[0][2] * x[1][1] * x[2][0] -
			x[0][1] * x[1][0] * x[2][2] -
			x[0][0] * x[1][2] * x[2][1];
		else
			return 0.;
	}

	public Location getCenter()
	{
		return center;
	}
	
	public Buff getBuff() {

	    Buff buffer = new Buff(getClass().getSimpleName());
	    buffer.add("format", 1);
	    buffer.add("majax", getMajaxLength()); 
	    buffer.add("minax", getMinaxLength()); 
	    buffer.add("trend", Math.toDegrees(getMajaxTrend())); 
	    buffer.add("area", getArea()); 
	    return buffer;
	}

	static public Buff getBuff(Scanner input) {
	    Buff buf = new Buff(input);
	    return buf;

	}

}
