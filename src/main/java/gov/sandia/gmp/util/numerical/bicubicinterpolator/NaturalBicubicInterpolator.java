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
package gov.sandia.gmp.util.numerical.bicubicinterpolator;

import java.util.Arrays;

public class NaturalBicubicInterpolator
{
	private class Bracket
	{
		public int		khi	= 0;
		public int		klo	= 0;
		public double	a		= 0.0;
		public double	b		= 0.0;
		public double	h		= 0.0;

		public Bracket()
		{
			
		}

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
		private void bracket(double x, double[] xGrid)
		{
			if (!isBracketed(x, xGrid))
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
		
		public boolean isBracketed(double x, double[] xGrid)
		{
			return (x >= xGrid[klo]) ? ((x < xGrid[khi]) ? true : false) : false;
		}
		
		public void reset()
		{
			khi	= 0;
			klo	= 0;
			a		= 0.0;
			b		= 0.0;
			h		= 0.0;
		}
	}

	/**
	 * The x grid interpolation location.
	 */
	private double			xInterpolate				= 0.0;

	/**
	 * The y grid interpolation location.
	 */
	private double			yInterpolate				= 0.0;

	/**
	 * The x grid locations. A monotonically increasing array. The x interpolation
	 * location must lie within the limits of this vector.
	 */
	private double[]		xGrid								= null;

	/**
	 * The y grid locations. A monotonically increasing array. The y interpolation
	 * location must lie within the limits of this vector.
	 */
	private double[]		yGrid								= null;

	/**
	 * The 2d array of values to be interpolated. These are stored as [x][y].
	 */
	private double[][]	values							= null;

	/**
	 * A Bracket object that contains the interpolation spacing information for
	 * the x grid.
	 */
	private Bracket			xBracket						= new Bracket();

	/**
	 * A Bracket object that contains the interpolation spacing information for
	 * the y grid.
	 */
	private Bracket			yBracket						= new Bracket();

	/**
	 * Second derivatives of the input values along splines in the y direction for
	 * each x grid point.
	 */
	private double[][]	d2v_dy2							= null;

	/**
	 * Second derivatives of the interpolated y splines at the y interpolation
	 * point.
	 */
	private double[]		d2v_dx2							= null;

	/**
	 * interpolated values (at yInterpolate) along y splines for each x grid
	 * point.
	 */
	private double[]		valuesOnXAtYinterp	= null;
	
	public NaturalBicubicInterpolator()
	{

	}

	/**
	 * Default constructor that sets the input interpolation data arrays by
	 * reference.
	 * 
	 * @param x
	 *          The monotonically increasing array of x positions.
	 * @param y
	 *          The monotonically increasing array of y positions.
	 * @param data
	 *          The 2D values for each grid position stored as [x][y].
	 */
	public NaturalBicubicInterpolator(double[] x, double[] y, double[][] data)
	{
		setArrayReferences(x, y, data);
	}

	/**
	 * Sets copies of the input arrays.
	 * 
	 * @param x
	 *          The monotonically increasing array of x positions.
	 * @param y
	 *          The monotonically increasing array of y positions.
	 * @param data
	 *          The 2D values for each grid position stored as [x][y].
	 */
	public void setArrayCopies(double[] x, double[] y, double[][] data)
	{
		reset();
		
		xGrid = Arrays.copyOf(x, x.length);
		yGrid = Arrays.copyOf(y, y.length);
		values = new double[data.length][];
		for (int i = 0; i < values.length; ++i)
			values[i] = Arrays.copyOf(data[i], data[i].length);
	}

	/**
	 * Sets references to the input arrays.
	 * 
	 * @param x
	 *          The monotonically increasing array of x positions.
	 * @param y
	 *          The monotonically increasing array of y positions.
	 * @param data
	 *          The 2D values for each grid position stored as [x][y].
	 */
	public void setArrayReferences(double[] x, double[] y, double[][] data)
	{
		reset();
		
		xGrid = x;
		yGrid = y;
		values = data;
	}

	/**
	 * \brief Function to Perform Cubic Spline Interpolation at a Point Bracketed
	 * by 1-D Cubic Splines. On Return the interpolated value, derivative, and 2nd
	 * derivative are available for return using the appropriate getters (i.e.
	 * getInterpolatedValue(), getInterpolatedDerivative(), and/or
	 * getInterpolated2ndDerivative()).
	 *
	 * Based On the function "splin2": Press, W.H. et al., 1988,
	 * "Numerical Recipes", 94-110.
	 * 
	 * @param xintrp
	 *          The x interpolation location.
	 * @param yintrp
	 *          The y interpolation location.
	 */
	public void interpolate(double xintrp, double yintrp)
	{
		// Note: should throw an error here if values is null
		//       i.e. setArrays was not called

		xInterpolate = xintrp;
		yInterpolate = yintrp;
		xBracket.bracket(xintrp, xGrid);
		yBracket.bracket(yintrp, yGrid);

		evaluateYSplinesAtX();
		evaluateXSplineAtY();
	}

	/**
	 * Calculates the interpolation spline at a new y interpolation location using
	 * a previous x interpolation location. This is significantly faster than
	 * calling interpolate(xintrp, yintrp) again since the xGrid.length splines
	 * evaluated along the y direction for each xGrid point is avoided. The x,y
	 * interpolation call must be made at least once before calling this method.
	 * 
	 * On Return the interpolated value, derivative, and 2nd derivative are
	 * available for return using the appropriate getters (i.e.
	 * getInterpolatedValue(), getInterpolatedDerivative(), and/or
	 * getInterpolated2ndDerivative()).
	 * 
	 * @param yintrp
	 *          The y interpolation location.
	 */
	public void interpolateNewY(double yintrp)
	{
		// Note: should throw an error here if d2v_dy2 is null
    //       .i.e. interpolate(xintrp, yintrp) was not called

		yInterpolate = yintrp;
		yBracket.bracket(yintrp, yGrid);
		evaluateXSplineAtY();
	}

	/**
	 * Calculates the interpolation spline at a new x interpolation location using
	 * a previous y interpolation location. This is significantly faster than
	 * calling interpolate(xintrp, yintrp) again since the xGrid.length splines
	 * evaluated along the y direction for each xGrid point is avoided as is
	 * calling the single x interpolation spline at a new y interpolation location.
	 * The interpolation(x,y) call must be made at least once before calling this
	 * method.
	 * 
	 * On Return the interpolated value, derivative, and 2nd derivative are
	 * available for return using the appropriate getters (i.e.
	 * getInterpolatedValue(), getInterpolatedDerivative(), and/or
	 * getInterpolated2ndDerivative()).
	 * 
	 * @param xintrp
	 *          The x interpolation location.
	 */
	public void interpolateNewX(double xintrp)
	{
		// Note: should throw an error here if d2v_dy2 is null
    //       .i.e. interpolate(xintrp, yintrp) was not called

		xInterpolate = xintrp;
		xBracket.bracket(xintrp, xGrid);
	}

	public double getXInterpolationPoint()
	{
		return xInterpolate;
	}

	public double getYInterpolationPoint()
	{
		return yInterpolate;
	}

	public double getInterpolatedValue()
	{
		return interpolatedValue(xBracket, valuesOnXAtYinterp, d2v_dx2);
	}

	public double getInterpolatedDerivative()
	{
		return interpolatedDerivative(xBracket, valuesOnXAtYinterp, d2v_dx2);
	}

	public double getInterpolated2ndDerivative()
	{
		return interpolated2ndDerivative(xBracket, d2v_dx2);
	}

	public boolean isYBracketed(double y)
	{
		return yBracket.isBracketed(y, yGrid);
	}
	
	public boolean isXBracketed(double x)
	{
		return xBracket.isBracketed(x, xGrid);
	}

	/**
	 * Calculates the second derivatives of the input values array at each xGrid
	 * location along the direction of the yGrid points.
	 */
	private void evaluateYSplinesAtX()
	{
		// calculate the 2nd derivatives at each of the tables values for splines
		// running in the y directions (d2v_dy2)

		d2v_dy2 = new double[xGrid.length][yGrid.length];
		for (int j = 0; j < xGrid.length; j++)
			naturalSpline(yGrid, values[j], d2v_dy2[j]);
	}

	/**
	 * Calculates the xGrid interpolated values at the y interpolation location
	 * for each of y splines defined at each xGrid location. Then it formulates a
	 * single spline in the x direction calculating the second derivatives along
	 * that spline. These derivatives and interpolated values are used to evaluate
	 * the final interpolation result at the x interpolation location.
	 */
	private void evaluateXSplineAtY()
	{
		// calculate xGrid interpolated values interpolated at the y interpolation
		// location (valuesOnXAtYinterp)

		valuesOnXAtYinterp = new double[xGrid.length];
		for (int j = 0; j < xGrid.length; j++)
			valuesOnXAtYinterp[j] = interpolatedValue(yBracket, values[j], d2v_dy2[j]);

		// calculate xgrid second derivative entries along the y interpolation
		// location and save in d2v_dx2

		d2v_dx2 = new double[xGrid.length];
		naturalSpline(xGrid, valuesOnXAtYinterp, d2v_dx2);
		// spline(xGrid, valuesOnXAtYinterp, 1.0e30, 1.0e30, d2v_dx2);
	}

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
	 * @param yp1
	 *          - Value of dy/dx evaluated at x[0].
	 * @param ypn
	 *          - Value of dy/dx evaluated at x[x.length-1].
	 * @param y2
	 *          - A Vector of second derivatives defined on xa.
	 */
	private void spline(double[] x, double[] y, double yp1, double ypn,
			double[] y2)
	{
		int i, k;
		double p, qn, sig, un;
		double[] u = new double[x.length];

		// calculate temporary u vector
		if (yp1 > 0.99e30)
			y2[0] = u[0] = 0.0;
		else
		{
			y2[0] = -0.5;
			u[0] = ((3.0 / (x[1] - x[0])) * ((y[1] - y[0]) / (x[1] - x[0]) - yp1));
		}

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
		if (ypn > 0.99e30)
			qn = un = 0.0;
		else
		{
			qn = 0.5;
			un = (3.0 / (x[xlm1] - x[xlm2]))
					* (ypn - (y[xlm1] - y[xlm2]) / (x[xlm1] - x[xlm2]));
		}

		// Back substitution loop of tri-diagonal algorithm

		y2[xlm1] = (un - qn * u[xlm2]) / (qn * y2[xlm2] + 1.0);
		for (k = xlm2; k >= 0; k--)
			y2[k] = y2[k] * y2[k + 1] + u[k];
	}

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
	private void naturalSpline(double[] x, double[] y, double[] y2)
	{
		int i, k;
		double p, sig;
		double[] u = new double[x.length];

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
	 * Returns the natural cubic spline interpolated value at the requested
	 * interpolation location.
	 * 
	 * @param bracket
	 *          The bracket object providing the x or y grid interpolation spacing
	 *          information.
	 * @param values
	 *          The set of values to be interpolated.
	 * @param d2v
	 *          The second derivative in the direction of the monotonically
	 *          increasing grid (for which bracket was defined).
	 * @return The interpolated value.
	 */
	private double interpolatedValue(Bracket bracket, double[] values,
			double[] d2v)
	{
		return bracket.a * values[bracket.klo] + bracket.b * values[bracket.khi]
				+ (bracket.a * (bracket.a * bracket.a - 1.0) * d2v[bracket.klo]
				+	bracket.b * (bracket.b * bracket.b - 1.0)	* d2v[bracket.khi])
				* (bracket.h * bracket.h) / 6.0;
	}

	/**
	 * Returns the natural cubic spline interpolated 1st derivative at the
	 * requested interpolation location.
	 * 
	 * @param bracket
	 *          The bracket object providing the x or y grid interpolation spacing
	 *          information.
	 * @param values
	 *          The set of values to be interpolated for the 1st derivative.
	 * @param d2v
	 *          The second derivative in the direction of the monotonically
	 *          increasing grid (for which bracket was defined).
	 * @return The interpolated first derivative.
	 */
	private double interpolatedDerivative(Bracket bracket, double[] values,
			double[] d2v)
	{
		return ((values[bracket.khi] - values[bracket.klo]) / bracket.h)
				+ (((3.0 * bracket.b * bracket.b - 1.0) * d2v[bracket.khi])
				- ((3.0 * bracket.a * bracket.a - 1.0) * d2v[bracket.klo]))
				* bracket.h / 6.0;
	}

	/**
	 * Returns the natural cubic spline interpolated 2nd derivative at the
	 * requested interpolation location.
	 * 
	 * @param bracket
	 *          The bracket object providing the x or y grid interpolation spacing
	 *          information.
	 * @param d2v
	 *          The second derivative in the direction of the monotonically
	 *          increasing grid (for which bracket was defined).
	 * @return The interpolated second derivative.
	 */
	private double interpolated2ndDerivative(Bracket bracket, double[] d2v)
	{
		return bracket.a * d2v[bracket.klo] + bracket.b * d2v[bracket.khi];
	}

	private void reset()
	{
		xBracket.reset();
		yBracket.reset();
		xInterpolate = yInterpolate = 0.0;
		valuesOnXAtYinterp = d2v_dx2 = null;
		d2v_dy2 = null;
	}
}
