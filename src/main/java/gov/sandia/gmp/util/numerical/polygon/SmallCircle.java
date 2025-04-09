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
import static java.lang.Math.cos;
import static java.lang.Math.tan;
import static java.lang.Math.toDegrees;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * An ordered list of points on the surface of a unit sphere that define a closed polygon.
 * Polygons have the ability to test whether or not an arbitrary test point on the sphere
 * is inside or outside the polygon.
 * 
 * When a polygon is constructed from a list of points, the side of the polygon that is
 * deemed to be 'inside' is ambiguous.  The user could intend that the smaller of the 
 * polygons be 'inside' or that the smaller polygon be 'outside'.  With one exception,
 * all of the polygon constuctors in this class assume that the smaller polygon is 'inside'.  
 * The exception is the constructor that creates a polygon from a referencePoint point and a radius.
 * In that case, if the radius is > PI/2, then the larger polygon is deemed to be 'inside'.
 * 
 * If the user wants the larger polygon to be 'inside', they can call method invert(), 
 * which will switch the polygons.
 * 
 * The concept of 'inside' and 'outside' is implemented by having a reference point which 
 * is specified to be either inside or outside the polygon.  Users can specify these 
 * values by calling setReferencePoint().
 * 
 * <p>
 * A convenient mechanism for generating polygons is to use Google Earth. It has the
 * ability to interactively generate polygons and store them in kmz or kml files. Polygon
 * has facilities to read and write kmz and kml files. Polygon can also read and write
 * polygons to an ascii file. An advantage of the ascii file format is that the position
 * of the reference point and whether the reference point is inside or outside the polygon
 * are stored in the file and therefore can be manipulated by users. For example, if a
 * user wishes to generate a polygon that is larger than a hemisphere, they could define
 * the polygon boundary points with Google Earth but the resulting polygon would be
 * inverted (the inside would be outside and the outside would be inside). They could use
 * Polygon to write the polygon to an ascii file,and manually edit the file to modify the
 * reference point definition.
 * 
 * <p>
 * A test point that is located very close to a polygon boundary is deemed to be
 * 'inside' the polygon. This means that if two adjacent, non-overlapping polygons share a
 * boundary point or a boundary edge, a test point near that point or edge will be deemed 
 * to be 'inside' both polygons. In this context two points are 'very close' if they are separated by
 * less than 1e-7 radians or 5.7e-6 degrees. For a sphere with the radius of the Earth
 * (6371 km), this corresponds to a linear distance of about 60 cm.
 * 
 * @author sballar
 */
public class SmallCircle extends Polygon2D implements Serializable, Callable<Polygon2D>
{
	private static final long serialVersionUID = -626981197496728344L;

	/**
	 * Tolerance value in radians used when comparing locations of two points.
	 */
	protected static final double TOLERANCE = 1e-7;

	/**
	 * The radii of the small circles in radians.
	 */
	protected double radius;

	/**
	 * Default constructor. Does nothing.
	 */
	public SmallCircle()
	{
		referencePoint = new double[] {0,0,1};
		referenceIn = false;
		name = "small_circles";
		this.radius = Double.NaN;
	}

	public SmallCircle(DataInputStream input) throws Exception
	{
		int format = input.readInt();
		if (format != 1)
			throw new IOException(format+" is not a recognized format.");
		referencePoint = new double[] {input.readDouble(), input.readDouble(), input.readDouble()};
		referenceIn = input.readBoolean();
		radius = input.readDouble(); // radians
	}

	/**
	 * 
	 * @param referencePoint unit vector of the referencePoint of the polygon
	 * @param referenceIn true if the referencePoint is inside the polygon
	 * @param radii radii of the small circles in radians
	 * @throws Exception
	 */
	public SmallCircle(double[] referencePoint, boolean referenceIn, double holeRadius, double radius)
			throws Exception
	{
		name = "small_circles";

		this.referencePoint = referencePoint.clone();
		this.referenceIn = referenceIn;
		this.radius = radius;
		
		SmallCircle hole = new SmallCircle(referencePoint, !referenceIn, holeRadius);
		this.addHole(hole);
	}

	/**
	 * 
	 * @param referencePoint unit vector of the referencePoint of the polygon
	 * @param referenceIn true if the referencePoint is inside the polygon
	 * @param radii 1 or 2 radii in radians. If 2 radii are specified
	 * the second one defines the outer radius of the polygon and the first defines a single hole.
	 * @throws Exception
	 */
	public SmallCircle(double[] referencePoint, boolean referenceIn, double...radii)
			throws Exception
	{
		name = "small_circles";

		this.referencePoint = referencePoint.clone();
		this.referenceIn = referenceIn;
		
		if (radii.length == 1)	
			this.radius = radii[0];
		else if (radii.length == 2) {
			this.radius = radii[1];
			addHole(new SmallCircle(referencePoint, referenceIn, radii[0]));
		}
		else
			throw new Exception("Can only construct a SmallCircle polygon with 1 or 2 radii, not "+radii.length);
	}

	public SmallCircle(BufferedReader buffer) throws Exception { 
		this(buffer.readLine()); 
	}

	/**
	 * Expecting a record containing 'referencePoint <lat> <lon> <in or out> <radius1> ...'
	 * Lat, lon, and radii all in degrees 
	 * @param record
	 * @throws Exception 
	 */
	public SmallCircle(String record) throws Exception {
		// expecting a single line like: "referencePoint <lat> <lon> <in or out> radius"
		String[] tokens = record.trim().split("\\s+");
		referencePoint = VectorGeo.getVectorDegrees(
				Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
		referenceIn = tokens[3].toLowerCase().startsWith("in");
		double r1 = Math.toRadians(Double.parseDouble(tokens[4]));
		if (tokens.length > 5) {
			radius = Math.toRadians(Double.parseDouble(tokens[5]));
			addHole(new SmallCircle(referencePoint, !referenceIn, r1));
		}
		else
			radius = r1;
	}

	/**
	 * Write this SmallCircle to a DataOutputStream.  Writes
	 * the className, fileformat id, referencePoint point as unit vector, boolean in/out,
	 * number of radii and then the radii in radians.
	 */
	@Override
	public void write(DataOutputStream output) throws Exception
	{
		Globals.writeString(output, getClass().getSimpleName());
		output.writeInt(1);
		output.writeDouble(referencePoint[0]);
		output.writeDouble(referencePoint[1]);
		output.writeDouble(referencePoint[2]);
		output.writeBoolean(referenceIn);
		output.writeDouble(radius);
	}

	/**
	 * Invert the current polygon. What used to be in will be out and what used to be out
	 * will be in.
	 */
	@Override
	public void invert()
	{
		referenceIn = !referenceIn;
		area = 4*PI - area;
	}

	/**
	 * Specify the reference point for this polygon and whether or not the specified point
	 * is inside or outside the polygon.
	 * 
	 * @param referencePoint
	 * @param referenceIn
	 * @throws Exception 
	 */
	@Override
	public void setReferencePoint(double[] referencePoint, boolean referenceIn) throws Exception {
		throw new Exception("Cannnot change the referencePoint of a PolygonSmallCircle");
	}

	/**
	 * return true if point x is located inside the polygon
	 * @param x the point to be evaluated
	 * @return true if point x is located inside the polygon
	 * @throws Exception 
	 */
	@Override
	public boolean contains(double[] x) throws Exception {
		boolean in = referenceIn == VectorUnit.angle(x, referencePoint) <= radius;
		if (in)
			for (Polygon2D hole : getHoles())
				if (!hole.contains(x)) {
					in = false;
					break;
				}
		return in;
	}

	/**
	 * Retrieve a deep copy of the points on the polygon.
	 * 
	 * @param repeatFirstPoint
	 *            if true, last point will be reference to the same point as the first
	 *            point.
	 * @return a deep copy of the points on the polygon.
	 */
	@Override
	public double[][] getPoints(boolean repeatFirstPoint) {
		return getPoints(repeatFirstPoint, TWO_PI/128);
	}

	/**
	 * Retrieve a deep copy of the points on the polygon.
	 * 
	 * @param repeatFirstPoint
	 *            if true, last point will be reference to the same point as the first
	 *            point.
	 * @param maxSpacing max distance between points, in radians
	 * @return a deep copy of the points on the polygon.
	 */
	@Override
	public double[][] getPoints(boolean repeatFirstPoint, double spacing)
	{
			int nIntervals =  Math.max(12, (int)Math.ceil(Globals.TWO_PI/spacing));
			double dx = Globals.TWO_PI/nIntervals;
			int n = repeatFirstPoint ? nIntervals+1 : nIntervals;
			double[][] points = new double[n][3];
			double[] start = VectorUnit.move(referencePoint, radius, 0.);
			for (int j=0; j<n; ++j)
				VectorUnit.rotate_right(start, referencePoint, j*dx, points[j]);
			return points;
	}

	protected double computeArea()
	{
		area = 1-cos(radius);
		area *= 2*PI;
		if (!referenceIn)
			area = 4*PI - area;
		return area;
	}

	/**
	 * Returns a String containing className, referencePoint lat lon, in/out, radius,
	 * all in degrees.
	 * 
	 * @return String
	 */
	@Override
	public String toString() {		
		return String.format("%s%nreferencePoint %s %s %1.6f", 
				getClass().getSimpleName(), VectorGeo.getLatLonString(referencePoint, "%1.6f %1.6f"),
				(referenceIn ? "in" : "out"),
				toDegrees(radius));
	}

	/**
	 * Returns the number of edges that define the polygon. Equals the number of unique
	 * GeoVectors that define the polygon.
	 * 
	 * @return the number of edges that define the polygon. Equals the number of unique
	 *         GeoVectors that define the polygon.
	 */
	@Override
	public int size() { return 0; }

	@Override
	public boolean getReferencePointIn() { return referenceIn; }

	/**
	 * Returns true if this Polygon and some other Polygon overlap.
	 * Note that it is possible for two polygons to overlap even when
	 * neither Polygon contains any of the points that define the other
	 * Polygon.
	 * @param other
	 * @return
	 * @throws Exception 
	 */
	@Override
	public boolean overlaps(Polygon other) throws Exception
	{
		if (other instanceof PolygonGlobal)
			return ((PolygonGlobal)other).referenceIn;

		if (other instanceof SmallCircle) {
			SmallCircle circle = (SmallCircle)other;
			return VectorUnit.angle(this.referencePoint, circle.referencePoint) <= this.radius+circle.radius;
		}

		return other.overlaps(this);
	}

	@Override
	public boolean onBoundary(double[] x) {
		if (Math.abs(VectorUnit.angle(referencePoint, x)-radius) < 1e-7)
			return true;
		return false;
	}

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
		// is c, the referencePoint of the circle.
		if (radius < 1e-7 && gc.onCircle(referencePoint) && (!constrained || gc.getDistance() >= TWO_PI 
				|| gc.getDistance(referencePoint) < gc.getDistance())) {
			intersections.add(referencePoint.clone());
			return intersections;
		}

		// if the radius of the small circle is PI then the only possible point of intersection
		// is the antipode of c.
		if (Math.abs(radius-PI) < 1e-7 && gc.onCircle(referencePoint) && (!constrained || gc.getDistance() >= TWO_PI || gc.getDistance(referencePoint) < gc.getDistance())) {
			intersections.add(new double[] {-referencePoint[0], -referencePoint[1], -referencePoint[2]});
			return intersections;
		}

		if (Math.abs(radius-PI/2) < 1e-7) {
			// this small circle is actually a great circle.
			// It's cheaper to find intersections of two great circles.
			intersections.addAll(new GreatCircle(referencePoint).getIntersections(gc, constrained));
		}
		else {
			// if angle between gc.normal and c is > PI/2, flip gc.normal
			double[] n = gc.getNormal();
			if (VectorUnit.dot(referencePoint, n) < 0)
				n = new double[] {-n[0], -n[1], -n[2]};

			double[] b = VectorUnit.crossNormal(n, referencePoint);

			// normalized vector triple product n x c x n.
			// a is the point on the great circle that is closest to c.
			double[] a = VectorUnit.crossNormal(b, n);

			double ca = VectorUnit.angle(referencePoint, a);

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
				VectorUnit.rotate_right(referencePoint, b, radius, b);

				// b is now the point of intersection of the great circle through c and a and the small circle.

				// use Napier's Rule from spherical trigonometry to find angle beta, which is
				// the angle, measured at c, between great circle from c to a and great circle 
				// from c to one of the intersections.
				double beta = acos(tan(ca)/tan(radius));

				// rotate b around c by angle beta and -beta to find the two points of intersection.
				double[] i1 = new double[3];
				VectorUnit.rotate_right(b, referencePoint, -beta, i1);
				if (!constrained || gc.getDistance() >= TWO_PI || gc.getDistance(i1) < gc.getDistance())
					intersections.add(i1);

				double[] i2 = new double[3];
				VectorUnit.rotate_right(b, referencePoint, beta, i2);
				if (!constrained || gc.getDistance() >= TWO_PI || gc.getDistance(i2) < gc.getDistance())
					intersections.add(i2);
			}
		}

		return intersections;
	}
}
