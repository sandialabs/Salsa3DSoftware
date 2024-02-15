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

public class Bracket
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

	public boolean isBracketed(double x, double[] xGrid) {
		return (x >= xGrid[klo]) ? ((x < xGrid[khi]) ? true : false) : false;
	}
	
}
