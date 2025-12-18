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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.GeoMath;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.testingbuffer.TestBuffer;

public class Ellipse implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private HyperEllipse hyperEllipse;
	
	private double[] coeff;
	
	private double[] principal_axes;
	
	private double[] center;
	
	/**
	 * Length of major axis in km;
	 */
	private double majax;
	
	/**
	 * Length of minor axis in km;
	 */
	private double minax;
	
	/**
	 * Orientation of the major axis of the ellipse in degrees from north
	 */
	private double trend;

	Ellipse(HyperEllipse hyperEllipse) throws Exception {
		this.hyperEllipse = hyperEllipse;
		this.coeff = this.hyperEllipse.uncertainty_equation_coefficients(new int[] {LON, LAT});
		
		this.center = hyperEllipse.getCenter().getUnitVector();
		
		if (isValid()) {
			find_principal_axes();
			majax = principal_axes[0] * hyperEllipse.getKappa(2);
			minax = principal_axes[1] * hyperEllipse.getKappa(2);
			trend = principal_axes[2];
		}
		else {
			majax = minax = trend = -1.;
		}
	}
	
	/**
	 * 
	 * @param center
	 * @param majax length of semi-major axis in km
	 * @param minax length of semi-minor axis in km
	 * @param trend orientation of major axis relative to north in degrees
	 */
	public Ellipse (double[] center, double majax, double minax, double trend) {
		this.center = center;
		this.majax = majax;
		this.minax = minax;
		this.trend = (trend + 180.) % 180.; // ensure range = 0 to 180.
	}

	public boolean isValid() { return coeff != null; }

	/**
	 * Get the length of the major axis of the ellipse in km
	 * @return
	 * @throws Exception 
	 */
	public double getMajaxLength() 	{
		return majax;
	}

	/**
	 * Get the length of the minor axis of the ellipse in km
	 * @return
	 * @throws Exception 
	 */
	public double getMinaxLength() {
		return minax;
	}

	/**
	 * the trend of the major axis of the ellipse in degrees from north
	 * @return
	 */
	public double getMajaxTrend() {
		return trend;
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

	public TestBuffer getTestBuffer() {
		TestBuffer buffer = new TestBuffer(this.getClass().getSimpleName());
		buffer.add("ellipse.majax", getMajaxLength()); 
		buffer.add("ellipse.minax", getMinaxLength()); 
		buffer.add("ellipse.trend", getMajaxTrend()); 
		buffer.add();
		return buffer;
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
	 * compute the distance in km from the center of the ellipse to its perimeter, in
	 * the direction specified by azimuth.
	 * @param azimuth in radians
	 * @return distance to perimeter in km
	 * @throws Exception azimuth is NaN
	 */
	public double distanceToPerimeter(double azimuth) throws Exception {
		if (Double.isNaN(azimuth))
			throw new Exception("Cannot compute distance to perimeter because azimuth is NaN");
		double theta = Math.toRadians(trend) - azimuth;
		double a = majax*sin(theta);
		double b = minax*cos(theta);
		return majax*minax/sqrt(a*a+b*b);
	}

	/**
	 * compute the distance in km from the center of the ellipse to its perimeter, in
	 * the direction of point
	 * @param point some other point
	 * @return distance to perimeter in km
	 * @throws Exception if center is at north or south pole, or if distance from center
	 * to point is 0 or PI.
	 */
	public double distanceToPerimeter(double[] point) throws Exception {
		return distanceToPerimeter(VectorUnit.azimuth(center, point, 0.));
	}
	
	public double scaledSeparation(Ellipse other) throws Exception {

		// distance from center of this ellipse to center of other ellipse in km
		double dkm = GeoMath.angle(center, other.center)* GeoMath.getEarthRadius(center);
		
		// distance in km from center of this ellipse to perimeter of this ellipse, 
		// moving in direction from center of this ellipse to center of other ellipse
		double dkm0 = distanceToPerimeter(other.center);
		
		// distance in km from center of other ellipse to perimeter of other ellipse, 
		// moving in direction from center of other ellipse to center of this ellipse
		double dkm1 = other.distanceToPerimeter(center);
		
		return dkm / (dkm0 + dkm1);
	}

	public double[] center() {
		return center;
	}

	/**
	 * Retrieve a list of points (unit vectors) that define the ellipse
	 * 
	 * @param nPoints number of points
	 * @return a list of points (unit vectors) that define the ellipse
	 * @throws Exception 
	 */
	public ArrayList<double[]> getPointsUnitVectors(int nPoints) throws Exception
	{
		double re = GeoMath.getEarthRadius(center);
		ArrayList<double[]> points = new ArrayList<>(nPoints);
		double dx = TWO_PI/(nPoints-1);
		for (int j=0; j<nPoints; ++j) 
			points.add(VectorUnit.move(center, distanceToPerimeter(j*dx)/re, j*dx));

		return points;
	}

	/**
	 * Retrieve a list of points {x (km), y (km), 0} that define the ellipse
	 * @param origin unit vector of origin of the Cartesian coordinate system
	 * @param nPoints number of points
	 * @return a list of points {x (km), y (km), 0} that define the ellipse
	 * @throws Exception 
	 */
	public ArrayList<double[]> getPointsKm(double[] origin, int nPoints) throws Exception
	{
		ArrayList<double[]> points = new ArrayList<>(nPoints);
		for (double[] u : getPointsUnitVectors(nPoints)) {
			double dkm = new GreatCircle(origin, u).getDistanceKm();
			double az = GeoMath.azimuth(origin, u, 0.);
			points.add(new double[] {dkm*sin(az), dkm*cos(az), 0.});
		}

		return points;
	}

}
