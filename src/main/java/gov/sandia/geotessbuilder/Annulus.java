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
package gov.sandia.geotessbuilder;

import gov.sandia.gmp.util.numerical.vector.VectorUnit;

public class Annulus {

	/**
	 * unit vector of center of the annulus
	 */
	private double[] center;

	/**
	 * Radii of the annulus in radians
	 */
	private double r1, r2;

	/**
	 * resolution inside the annulus
	 */
	private int tessLevel;

	Annulus(double[] center, double r1, double r2, int tessLevel)
	{
		this.center=center;
		if (r1 <= r2)
		{
			this.r1 = r1;
			this.r2 = r2;
		}
		else
		{
			this.r1 = r2;
			this.r2 = r1;
		}
		this.tessLevel = tessLevel;
	}

	/**
	 * returns true if this annulus and the specified triangle overlap.
	 * Overlap happens when any corner of the triangle is within
	 * the annulus or if any edge of the triangle spans both radii of the annulus.
	 * @param t a Triangle
	 * @return
	 */
	public boolean overlap(Triangle t) {
		// find distance from center to corner[2] (previous corner)
		double n, p = VectorUnit.angle(center, t.get(2).getArray());

		// if within the annulus, return true
		if (p >= r1 && p <= r2) return true;

		for (int i=0; i<3; ++i)
		{
			// find distance from center to next corner
			n = VectorUnit.angle(center, t.get(i).getArray());

			// if within the annulus, return true
			// or if one corner is < r1 and other corner is > r2, return true
			if (n >= r1 && n <= r2 || p <= r1 && n >= r2  || n <= r1 && p >= r2) 
				return true;
			// set previous = next;
			p=n;
		}
		return false;
	}

	/**
	 * Get the desired resolution within annulus
	 * @return
	 */
	public int getTessLevel() {
		return tessLevel;
	}
}
