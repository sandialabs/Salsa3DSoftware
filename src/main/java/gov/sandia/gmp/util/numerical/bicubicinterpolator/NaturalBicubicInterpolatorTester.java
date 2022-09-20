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

public class NaturalBicubicInterpolatorTester
{
	static double[] xgrid1 = {39.0, 39.5, 40.0, 40.5, 41.0, 41.5, 42.0};
	static double[] ygrid1 = {35.0, 50.0, 75.0, 100.0};
	static double[][] values1 = {{442.9671, 441.4871, 439.0246, 436.5673},
			 {447.1415, 445.658, 443.1895, 440.7262},
			 {451.2988, 449.8117, 447.3374, 444.8681},
			 {455.4391, 453.9485, 451.4683, 448.9932},
			 {459.5623, 458.0683, 455.5823, 453.1014},
			 {463.6686, 462.1711, 459.6794, 457.1926},
			 {467.7578, 466.2568, 463.7593, 461.2666}};
	static double xintrp1 = 40.84073276581503;
	static double yintrp1 = 70.0;
	static double[] results1 = {454.769981628745, 8.224224109065798, -0.0646680840265265};
	
  public static void main(String[] args)
  {
  	NaturalBicubicInterpolator nbci = new NaturalBicubicInterpolator();
  	nbci.setArrayReferences(xgrid1, ygrid1, values1);
  	nbci.interpolate(xintrp1, yintrp1);
  	System.out.println("Value residual = " + Math.abs(results1[0] - nbci.getInterpolatedValue()));
  	System.out.println("Derivative residual = " + Math.abs(results1[1] - nbci.getInterpolatedDerivative()));
  	System.out.println("2nd Derivative residual = " + Math.abs(results1[2] - nbci.getInterpolated2ndDerivative()));

  	nbci.interpolateNewY(yintrp1);
  	System.out.println("Value residual = " + Math.abs(results1[0] - nbci.getInterpolatedValue()));
  	System.out.println("Derivative residual = " + Math.abs(results1[1] - nbci.getInterpolatedDerivative()));
  	System.out.println("2nd Derivative residual = " + Math.abs(results1[2] - nbci.getInterpolated2ndDerivative()));
  	
  	System.out.println("");
  	System.out.println("isYBracketed(50) = " + nbci.isYBracketed(50.0));
  	System.out.println("isYBracketed(50 - del) = " + nbci.isYBracketed(50.0-1.0e-7));
  	System.out.println("isYBracketed(50 + del) = " + nbci.isYBracketed(50.0+1.0e-7));
  	System.out.println("isYBracketed(75.0) = " + nbci.isYBracketed(75.0));
  	System.out.println("isYBracketed(75.0 - del) = " + nbci.isYBracketed(75.0-1.0e-7));
  	System.out.println("isYBracketed(75.0 + del) = " + nbci.isYBracketed(75.0+1.0e-7));
  }
}
