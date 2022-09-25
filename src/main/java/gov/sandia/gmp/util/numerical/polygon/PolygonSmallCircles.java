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
import static java.lang.Math.cos;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
 * The exception is the constructor that creates a polygon from a center point and a radius.
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
public class PolygonSmallCircles extends Polygon2D implements Serializable, Callable<Polygon2D>
{
	private static final long serialVersionUID = -626981197496728344L;

	/**
	 * The area of the polygon, assuming a unit sphere.  Area will range from 
	 * zero to 4*PI.  Lazy evaluation is used.  Area is computed the first time
	 * the area is requested.
	 */
	protected double area = Double.NaN;

	/**
	 * Tolerance value in radians used when comparing locations of two points.
	 */
	protected static final double TOLERANCE = 1e-7;

	/**
	 * The radii of the small circles in radians.
	 */
	protected double[] smallCircleRadii;

	/**
	 * Default constructor. Does nothing.
	 */
	public PolygonSmallCircles()
	{
		referencePoint = new double[] {0,0,1};
		referenceIn = false;
		name = "small_circles";
		this.smallCircleRadii = new double[0];
	}

	public PolygonSmallCircles(DataInputStream input) throws Exception
	{
		int format = input.readInt();
		if (format != 1)
			throw new IOException(format+" is not a recognized format.");
		referencePoint = new double[] {input.readDouble(), input.readDouble(), input.readDouble()};
		referenceIn = input.readBoolean();
		smallCircleRadii = new double[input.readInt()];
		for (int i=0; i<smallCircleRadii.length; ++i)
			smallCircleRadii[i] = input.readDouble(); // radians
	}

	/**
	 * 
	 * @param center unit vector of the center of the polygon
	 * @param referenceIn true if the center is inside the polygon
	 * @param radii radii of the small circles in radians
	 * @throws Exception
	 */
	public PolygonSmallCircles(double[] center, boolean referenceIn, double... radii)
			throws Exception
	{
		name = "small_circles";

		this.referencePoint = center.clone();
		this.referenceIn = referenceIn;

		ArrayList<Double> r = new ArrayList<>(radii.length);
		for (double x : radii)
			if (x > 0. && x < PI)
				r.add(x);
		Collections.sort(r);

		this.smallCircleRadii = radii.clone();
	}

	public PolygonSmallCircles(BufferedReader buffer) throws IOException { 
	    this(buffer.readLine()); 
	    }

	/**
	 * Expecting a record containing 'referencePoint <lat> <lon> <in or out> <radius1> ...'
	 * Lat, lon, and radii all in degrees 
	 * @param record
	 * @throws IOException
	 */
	public PolygonSmallCircles(String record) throws IOException {
		// expecting a single line like: "referencePoint <lat> <lon> <in or out> <radius1> ..."
		String[] tokens = record.trim().split("\\s+");
		referencePoint = VectorGeo.getVectorDegrees(
				Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
		referenceIn = tokens[3].toLowerCase().startsWith("in");
		int nradii = tokens.length-4;
		smallCircleRadii = new double[nradii];
		for (int i=0; i<nradii; ++i)
			smallCircleRadii[i] = Math.toRadians(Double.parseDouble(tokens[i+4]));
	}

	/**
	 * Write this PolygonSmallCircles to a DataOutputStream.  Writes
	 * the className, fileformat id, center point as unit vector, boolean in/out,
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
		output.writeInt(smallCircleRadii.length);
		for (double r : smallCircleRadii)
			output.writeDouble(r);
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
	 */
	@Override
	public boolean contains(double[] x)
	{
		double r = VectorUnit.angle(x, referencePoint);
		int band=0; 
		for (int i=0; i<smallCircleRadii.length; ++i)
			if (r >= smallCircleRadii[i])	
				++band;
			else 
				break;

		return referenceIn ^ (band % 2) == 1;
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
	public double[][][] getPoints(boolean repeatFirstPoint) {
		return getPoints(repeatFirstPoint, Math.toRadians(2));
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
	public double[][][] getPoints(boolean repeatFirstPoint, double spacing)
	{
		// search the cardinal directions for the one that is closest to 90 degrees away from referencePoint.
		double[][] cardinal = new double[][] {new double[] {1,0,0}, new double[] {0,1,0}, new double[] {0,0,1},
			new double[] {-1,0,0}, new double[] {0,-1,0}, new double[] {0,0,-1}};
			double[] best = null;
			double smallestDot = Double.POSITIVE_INFINITY;
			for (double[] x : cardinal)
			{
				double dot = Math.abs(VectorUnit.dot(referencePoint, x));
				if (dot < smallestDot) { best = x; smallestDot = dot; }
			}

			// gc is a great circle that starts at referencePoint and heads off in some 
			// arbitrary direction.  Will be used to find a starting point a specified distance
			// from referencePoint.
			GreatCircle gc = new GreatCircle(referencePoint, best);

			double[][][] points = new double[smallCircleRadii.length][][];
			int pi=0;
			for (int i=smallCircleRadii.length-1; i>=0; --i)
//			for (int i=0; i<smallCircleRadii.length; ++i)
			{
				double[] start = gc.getPoint(smallCircleRadii[i]);

				int nIntervals =  Math.max(12, (int)Math.ceil(Math.sin(smallCircleRadii[i])*Globals.TWO_PI/spacing));
				double dx = Globals.TWO_PI/nIntervals;
				int n = repeatFirstPoint ? nIntervals+1 : nIntervals;
				double[][] pts = new double[n][3];
				points[pi++] = pts;
				//int sign = (i%2)*2-1;
				for (int j=0; j<n; ++j)
					VectorUnit.rotate(start, referencePoint, -j*dx, pts[j]);
			}
			return points;
	}

	/**
	 * Retrieve the area of this polygon. This is the unitless area (radians squared). 
	 * To convert to km^2, multiply the result by R^2 where R is the radius of the sphere.
	 * The range is zero to 4*PI.
	 * @return
	 */
	@Override
	public double getArea() { return Double.isNaN(area) ? computeArea() : area; }

	protected double computeArea()
	{
		area = 1-cos(smallCircleRadii[0]);
		for (int i=1; i<smallCircleRadii.length; i+=2)
			area += cos(smallCircleRadii[i-1]) - cos(smallCircleRadii[i]);
		area *= 2*PI;
		if (!referenceIn)
			area = 4*PI - area;
		return area;
	}

	/**
	 * Returns a String containing all the points that define the polygon with one lon,
	 * lat pair per record. lats and lons are in degrees. Longitudes range from -180 to
	 * 180 degrees.
	 * 
	 * @return String
	 */
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(getClass().getSimpleName()+"\n");
		buf.append(String.format("referencePoint %1.6f %1.6f %s",
				VectorGeo.getLatDegrees(referencePoint), VectorGeo.getLonDegrees(referencePoint),
				(referenceIn ? "in" : "out")));
		for (double r : smallCircleRadii)
			buf.append(String.format(" %1.4f", Math.toDegrees(r)));
		buf.append("\n");
		return buf.toString();
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

	/**
	 * Retrieve a reference to the referencePoint.
	 * 
	 * @return a reference to the referencePoint.
	 */
	@Override
	public double[] getReferencePoint()
	{
		return referencePoint;
	}

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
			return ((Polygon2D)other).referenceIn;

		if (other instanceof PolygonSmallCircles)
		{
			PolygonSmallCircles op = (PolygonSmallCircles) other;
			// search for small circle intersections which happens when 
			// delta > r1 and delta > r2 and delta <= r1+r2
			double delta = VectorUnit.angle(this.referencePoint, ((Polygon2D)other).referencePoint);
			for (int i=0; i<this.smallCircleRadii.length; ++i)
			{
				if (delta > this.smallCircleRadii[i])
					for (int j=0; j<op.smallCircleRadii.length; ++j)
						if (delta > op.smallCircleRadii[j] && delta < this.smallCircleRadii[i]+op.smallCircleRadii[j])
							return true;
			}
			return false;
		}

		if (other instanceof PolygonPoints)
		{
			PolygonPoints op = (PolygonPoints) other;
			for (double r : this.smallCircleRadii)
				for (GreatCircle edge : op.edges)
					if (edge.getIntersections(this.referencePoint, r, true).size() > 0)
						return true;
			return false;
		}

		throw new Exception("Unrecognized Polygon class");
	}

	@Override
	public boolean onBoundary(double[] x) {
		double delta = VectorUnit.angle(referencePoint, x);
		for (double r : smallCircleRadii)
			if (Math.abs(delta-r) < 1e-7)
				return true;
		return false;
	}

}
