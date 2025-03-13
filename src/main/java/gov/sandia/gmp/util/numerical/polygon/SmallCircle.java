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
package gov.sandia.gmp.util.numerical.polygon;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.tan;

import java.util.ArrayList;
import java.util.List;

import gov.sandia.gmp.util.numerical.vector.VectorUnit;

public class SmallCircle implements GeoCircle {

	/**
	 * Unit vector of the center of the small circle.
	 */
	private double[] center;

	/**
	 * Radius of the small circle in radians.
	 */
	private double radius;

	private static final double TWO_PI = 2*PI;

	public SmallCircle(double[] center, double radius) {
		this.center = center;
		this.radius = radius;
	}

	public double[] getCenter() { return center; }

	public double getRadius() { return radius; }

	/**
	 * Find 0, 1 or 2 points where this small circle intersects a GreatCircle.
	 * @param gc the GreatCircle with which to find intersections.
	 * @param constrained if true, then for an intersection to count it must be
	 * located between the first and last points that define the great circle.
	 * @return 0, 1 or 2 unit vectors specifying the locations of any intersections.  
	 * If length is 0, the small and great circles do not intersect.
	 * If length is 1 then they are tangent.  If length is 2, the unit vectors represent the intersections.
	 */
	public List<double[]> getIntersections(GreatCircle gc, boolean constrained)
	{
		List<double[]> intersections = new ArrayList<>(2);

		// if the radius of the small circle is 0 then the only possible point of intersection
		// is c, the center of the circle.
		if (radius < 1e-7 && gc.onCircle(center) && (!constrained || gc.getDistance() >= TWO_PI || gc.getDistance(center) < gc.getDistance())) {
			intersections.add(center.clone());
			return intersections;
		}

		// if the radius of the small circle is PI then the only possible point of intersection
		// is the antipode of c.
		if (Math.abs(radius-PI) < 1e-7 && gc.onCircle(center) && (!constrained || gc.getDistance() >= TWO_PI || gc.getDistance(center) < gc.getDistance())) {
			intersections.add(new double[] {-center[0], -center[1], -center[2]});
			return intersections;
		}

		if (Math.abs(radius-PI/2) < 1e-7) {
			// this small circle is actually a great circle.
			// It's cheaper to find intersections of two great circles.
			intersections.addAll(new GreatCircle(center).getIntersections(gc, constrained));
		}
		else {
			// if angle between gc.normal and c is > PI/2, flip gc.normal
			double[] n = gc.getNormal();
			if (VectorUnit.dot(center, n) < 0)
				n = new double[] {-n[0], -n[1], -n[2]};

			double[] b = VectorUnit.crossNormal(n, center);

			// normalized vector triple product n x c x n.
			// a is the point on the great circle that is closest to c.
			double[] a = VectorUnit.crossNormal(b, n);

			double ca = VectorUnit.angle(center, a);

			// if distance from c to a is > r, then no intersection; do nothing.

			if (abs(ca-radius) < 1e-7)
			{
				// if distance from c to a is == r, then great circle is tangent to small circle
				// at a.  If a is in the distance range of the great circle, return it.
				if (!constrained || gc.getDistance() >= TWO_PI || gc.getDistance(a) < gc.getDistance())
					intersections.add(a);
			}
			else if (ca < radius)
			{
				// b is currently n x c.  rotate c around b by angle r and replace b with the result.
				VectorUnit.rotate_right(center, b, radius, b);

				// b is now the point of intersection of the great circle through c and a and the small circle.

				// use Napier's Rule from spherical trigonometry to find angle beta, which is
				// the angle, measured at c, between great circle from c to a and great circle 
				// from c to one of the intersections.
				double beta = acos(tan(ca)/tan(radius));

				// rotate b around c by angle beta and -beta to find the two points of intersection.
				double[] i1 = new double[3];
				VectorUnit.rotate_right(b, center, -beta, i1);
				if (!constrained || gc.getDistance() >= TWO_PI || gc.getDistance(i1) < gc.getDistance())
					intersections.add(i1);

				double[] i2 = new double[3];
				VectorUnit.rotate_right(b, center, beta, i2);
				if (!constrained || gc.getDistance() >= TWO_PI || gc.getDistance(i2) < gc.getDistance())
					intersections.add(i2);
			}
		}

		return intersections;
	}

}
