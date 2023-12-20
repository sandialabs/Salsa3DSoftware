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
package gov.sandia.gmp.baseobjects.hyperellipse;

import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.LAT;
import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.LON;
import static gov.sandia.gmp.util.globals.Globals.NA_VALUE;
import static gov.sandia.gmp.util.globals.Globals.PI_OVR_TWO;
import static gov.sandia.gmp.util.globals.Globals.TWO_PI;
import static gov.sandia.gmp.util.globals.Globals.sqr;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.Arrays;
import java.util.Scanner;

import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.css30.Origerr;

public class Ellipse {

    private HyperEllipse hyperEllipse;
    private double[] coeff;
    private double[] principal_axes;

    Ellipse(HyperEllipse hyperEllipse) throws Exception {
	this.hyperEllipse = hyperEllipse;
	this.coeff = this.hyperEllipse.uncertainty_equation_coefficients(new int[] {LON, LAT});
	if (isValid())
	    find_principal_axes();
    }

    public boolean isValid() { return coeff != null; }

    /**
     * Get the length of the major axis of the ellipse in km
     * @return
     * @throws Exception 
     */
    public double getMajaxLength() 	{
	return isValid() ? principal_axes[0] * hyperEllipse.getKappa(2) : -1.;
    }

    /**
     * Get the length of the minor axis of the ellipse in km
     * @return
     * @throws Exception 
     */
    public double getMinaxLength() {
	return isValid() ? principal_axes[1] * hyperEllipse.getKappa(2) : -1.;
    }

    /**
     * the trend of the major axis of the ellipse in degrees, from x axis
     * @return
     */
    public double getMajaxTrend() {
	return isValid() ? principal_axes[2] : -1.;
    }

    /**
     * get area of the ellipse in radian squared?
     * @return
     * @throws Exception 
     */
    public double getArea() 
    {
	if (isValid()) {
	    double d = 1. / sqr(hyperEllipse.getKappa(2));
	    double a = coeff[0] * d;
	    double b = coeff[1] * d;
	    double c = coeff[2] * d;
	    double radical = 4 * a * c - b * b;
	    if (radical <= 0.)
		return 0.;
	    return TWO_PI / sqrt(radical);
	}
	return -1.;
    }


    /**
     * find the principal axes of a 2D ellipse given 4 coefficients of equation.
     * Returns a 3-element array: [major axis, minor_axis, trend]
     */
    private void find_principal_axes()
    {
	if (principal_axes == null)
	    principal_axes = new double[] {Double.NaN, Double.NaN, Double.NaN};

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
	    Arrays.fill(coeff, NA_VALUE);
	else
	{
	    principal_axes[0] = sqrt(1. / d1);
	    principal_axes[1] = sqrt(1. / d2);
	    //if the minor axis is bigger than the major one, swap them
	    if (principal_axes[1] > principal_axes[0])
	    {
		principal_axes[0] = sqrt(1. / d2);
		principal_axes[1] = sqrt(1. / d1);
		trend += PI_OVR_TWO;
	    }
	    // change trend from being measured from east to being measured from north
	    trend = PI_OVR_TWO - trend;

	    // ensure that trend is in range 0 to PI
	    trend = (trend + PI) % PI;

	    principal_axes[2] = Math.toDegrees(trend);
	}
    }

    /**
     * compute the distance from the center of the ellipse to its perimeter, in
     * the direction specified by the unit vector v.
     * coord sys is 0:north, 1:east
     * @param v
     * @param scaled
     * @return
     * @throws Exception 
     */
    public double distance_to_perimeter(double[] v, boolean scaled) throws Exception
    {
	// calculate distance from center of ellipse to perimeter of ellipse, in
	// the direction specified by VectorMod v.
	// v is a 2-component unit VectorMod specifying direction in which to look
	// (0:north, 1:east)
	if (v.length != 2)
	    return -1.;
	double scale = 1.;
	if (scaled)
	    scale = hyperEllipse.getKappa(2);
	return sqrt(scale /
		(coeff[0] * v[0] * v[0] +
			coeff[1] * v[0] * v[1] +
			coeff[2] * v[1] * v[1]));

    }

    /**
     * test the coefficients of the ellipse to ensure that they actually define
     * and ellipse.
     *
     * See Zwillinger, D., 2003, CRC Standard Mathematical Tables and Formulae, 31st edition
     * p. 329.
     * @return
     * @throws Exception 
     */
    public boolean validityTest() throws Exception {
	double[][] m = new double[3][3];
	m[0][0] = coeff[0];
	m[0][1] = coeff[1] * 0.5;
	m[0][2] = 0.;
	m[1][0] = coeff[1] * 0.5;
	m[1][1] = coeff[2];
	m[1][2] = 0.;
	m[2][0] = 0.;
	m[2][1] = 0.;
	m[2][2] = -hyperEllipse.getKappa(2);;
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

    public double determinant(double[][] x, int size) {
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

    public Buff getBuff()  {

	Buff buffer = new Buff(getClass().getSimpleName());
	buffer.add("format", 1);
	buffer.add("majax", getMajaxLength()); 
	buffer.add("minax", getMinaxLength()); 
	buffer.add("trend", getMajaxTrend()); 
	buffer.add("area", getArea()); 
	return buffer;
    }

    static public Buff getBuff(Scanner input) {
	Buff buf = new Buff(input);
	return buf;

    }

}
