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
package gov.sandia.gmp.surfacewavepredictor;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

public class SphericalTrig {

	/**
	 * Given angleA, sideB, and angleC between the two angles, return 
	 * the length of sideC.
	 * @param angleA
	 * @param sideB
	 * @param angleC
	 * @return sideC
	 */
	public static double triangle_asa(double angleA, double sideB, double angleC) {
		double angleB = law_of_cosines_asa(angleA, sideB, angleC);
		return law_of_cosines_aaa(angleA, angleB, angleC);
	}

	/**
	 * Given two sides of a triangle, sideB and sideC, and the angleB opposite to 
	 * sideB, find angleA. Spherical SSA.  sideC must be shorter than sideB
	 * @param sideB long side
	 * @param sideC short side
	 * @param angleB
	 * @return
	 */
	public static double triangle_ssa(double sideB, double sideC, double angleB) {
		if (angleB < PI/2) { angleB = PI-angleB; sideC = PI-sideC; sideB = PI-sideB; }		
		double angleC = asin(sin(sideC)*sin(angleB)/sin(sideB));
		return 2*acot(tan((angleB-angleC)/2)*sin((sideB+sideC)/2)/sin((sideB-sideC)/2) );
	}

	/**
	 * given two sides of a triangle and the angle between the two sides,
	 * find the length of the third side. All quantities in radians.
	 * @param sideA
	 * @param angleC
	 * @param sideB
	 * @return sideC
	 */
	public static double law_of_cosines_sas(double sideA, double angleC, double sideB) {
		return acos(cos(sideA)*cos(sideB) + sin(sideA)*sin(sideB)*cos(angleC));
	}
	
	/**
	 * given two angles of a triangle and length of the side in between,
	 * find the third angle. All quantities in radians.
	 * @param angleA
	 * @param sideC
	 * @param angleB
	 * @return angleC
	 */
	public static double law_of_cosines_asa(double angleA, double sideC, double angleB) {
		return acos(-cos(angleA)*cos(angleB) + sin(angleA)*sin(angleB)*cos(sideC));
	}
	
	/**
	 * given three sides of a triangle, find the angle opposite sideC.
	 * All quantities in radians.
	 * @param sideA
	 * @param sideB
	 * @param sideC
	 * @return angleC
	 */
	public static double law_of_cosines_sss(double sideA, double sideB, double sideC) {
		return acos((cos(sideC)-cos(sideA)*cos(sideB))/(sin(sideA)*sin(sideB)));
	}
	
	/**
	 * given three angles of a triangle, find the side opposite angleC.
	 * All quantities in radians.
	 * @param angleA
	 * @param angleB
	 * @param angleC
	 * @return sideC
	 */
	public static double law_of_cosines_aaa(double angleA, double angleB, double angleC) {
		return acos((cos(angleC)+cos(angleA)*cos(angleB))/(sin(angleA)*sin(angleB)));
	}
	
	public static double acot(double x) { return x == 0. ? PI/2. : atan(1 / x); }

}
