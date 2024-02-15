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
package gov.sandia.gmp.baseobjects.radial2dmodel;

public class CubicSpline {

	private double[] x;

	private double[] y;

	private double[] y2;

	/**
	 * Cubic Spline Construction Function.
	 *
	 * Function. Given arrays x[0..n-1] and y[0..n-1] containing a tabulated
	 * function, i.e., y[i] = f(x[i]), with x[0] < x[1] < x[n-1], and given values
	 * yp1 and ypn for the first derivative of the interpolating function at
	 * points 0 and n-1, respectively, this routine returns an array y2[0..n-1]
	 * that contains the 2nd i derivatives of the interpolating function at the
	 * tabulated points x[i].
	 *
	 * If yp1 and/or ypn are equal to 1.0e30 or larger, the routine is signaled to
	 * set the corresponding boundary condition for a natural spline, with zero
	 * second derivative on that boundary.
	 *
	 * NOTE: This routine only needs to be called once to process the entire
	 * tabulated function in x and y arrays.
	 *
	 * Based On the function "spline": Press, W.H. et al., 1988,
	 * "Numerical Recipes", 94-110.
	 *
	 *
	 * @param x
	 *          - An input vector of independent values of a cubic spline.
	 * @param y
	 *          - An input vector of dependent values of the cubic spline defined
	 *          on the values xa.
	 * @param y2
	 *          - A Vector of second derivatives defined on xa.
	 */
	public CubicSpline (double[] x, double[] y)
	{
		this.x = x;
		this.y = y;

		int i, k;
		double p, sig;
		double[] u = new double[x.length];

		y2 = new double[x.length];

		// calculate temporary u vector
		y2[0] = u[0] = 0.0;

		// Decomposition loop for tri-diagonal algorithm

		int xlm1 = x.length - 1;
		int xlm2 = x.length - 2;
		for (i = 1; i < xlm1; i++)
		{
			sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
			p = sig * y2[i - 1] + 2.0;
			y2[i] = (sig - 1.0) / p;
			u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1])
					/ (x[i] - x[i - 1]);
			u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
		}

		// Back substitution loop of tri-diagonal algorithm

		y2[xlm1] = 0.0;
		for (k = xlm2; k >= 0; k--)
			y2[k] = y2[k] * y2[k + 1] + u[k];
	}

	/**
	 * Returns interpolated y-value, first derivative wrt x and second derivative wrt x
	 * @param x
	 * @return
	 */
	public double[] interpolate(double x)
	{
		Bracket bracket = new Bracket(x, this.x);
		
		return new double[] {

				// interpolated y value
				bracket.a * y[bracket.klo] + bracket.b * y[bracket.khi]
						+ (bracket.a * (bracket.a * bracket.a - 1.0) * y2[bracket.klo]
								+	bracket.b * (bracket.b * bracket.b - 1.0)	* y2[bracket.khi])
						* (bracket.h * bracket.h) / 6.0,

						// interpolated first derivative
						((y[bracket.khi] - y[bracket.klo]) / bracket.h)
						+ (((3.0 * bracket.b * bracket.b - 1.0) * y2[bracket.khi])
								- ((3.0 * bracket.a * bracket.a - 1.0) * y2[bracket.klo]))
						* bracket.h / 6.0,

						// interpolated second derivative
						bracket.a * y2[bracket.klo] + bracket.b * y2[bracket.khi]

		};
	}


	class Bracket
	{
		public int		khi	= 0;
		public int		klo	= 0;
		public double	a		= 0.0;
		public double	b		= 0.0;
		public double	h		= 0.0;

		/**
		 * Standard bisection method to bracket the input interpolation location
		 * between 2 entries of a monotonically increasing vector (xGrid). On exit
		 * the values a, b, and h are assigned which are used to perform the actual
		 * value, derivative, and 2nd derivative interpolation.
		 * 
		 * @param x
		 *          Interpolation location.
		 * @param xGrid
		 *          Monotonically increasing grid vector.
		 */
		public Bracket(double x, double[] xGrid) {
			//if (!isBracketed(x, xGrid))
			{
				int k = 0;
				klo = 0;
				khi = xGrid.length - 1;
				while (khi - klo > 1)
				{
					k = (khi + klo) >> 1;
					if (xGrid[k] > x)
						khi = k;
					else
						klo = k;
				}

				// klo and khi now bracket the input value of x in xGrid
				// get h, a, and b
				h = xGrid[khi] - xGrid[klo];
			}
			
			a = (xGrid[khi] - x) / h;
			b = (x - xGrid[klo]) / h;
			
		}

//		public boolean isBracketed(double x, double[] xGrid) {
//			return (x >= xGrid[klo]) ? ((x < xGrid[khi]) ? true : false) : false;
//		}
		
	}

}
