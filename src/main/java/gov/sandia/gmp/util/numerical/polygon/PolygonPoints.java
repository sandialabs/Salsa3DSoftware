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
import static java.lang.Math.ceil;
import static java.lang.Math.cos;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle.GreatCircleException;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;

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
public class PolygonPoints extends Polygon2D implements Serializable, Callable<Polygon2D>
{
	private static final long serialVersionUID = 8532860500914235523L;

	/**
	 * A GreatCircle object for each edge of the polygon.
	 */
	protected ArrayList<GreatCircle> edges;

	/**
	 * Tolerance value in radians used when comparing locations of two points.
	 */
	protected static final double TOLERANCE = 1e-7;

	/**
	 * Default constructor. Does nothing.
	 */
	public PolygonPoints()
	{
	}

	public PolygonPoints(DataInputStream input) throws Exception
	{
		int format = input.readInt();
		if (format != 1)
			throw new IOException(format+" is not a recognized format.");
		referencePoint = new double[] {input.readDouble(), input.readDouble(), input.readDouble()};
		referenceIn = input.readBoolean();
		int n = input.readInt();
		List<double[]> points = new ArrayList<>(n);
		for (int i=0; i<n; ++i)
			points.add(new double[] {input.readDouble(), input.readDouble(), input.readDouble()});
		setup(points);
	}

	public PolygonPoints(BufferedReader input) throws Exception
	{
		ArrayList<double[]> points = new ArrayList<double[]>();

		// default behavior is that lat is first
		boolean lonFirst = false;

		int maxpoints = Integer.MAX_VALUE;
		int count = 0;

		while (true)
		{
			String record = input.readLine();
			if (record == null) break;
			
			record = record.trim();

			if (record.length() > 0 && !record.startsWith("#"))
			{
				String[] tokens = tokenize(record);

				if (record.toLowerCase().startsWith("lat"))
					lonFirst = false;
				else if (record.toLowerCase().startsWith("lon"))
					lonFirst = true;
				else if (record.toLowerCase().startsWith("reference"))
				{
					if (lonFirst)
						referencePoint = VectorGeo.getVectorDegrees(
								Double.valueOf(tokens[2]), Double.valueOf(tokens[1]));
					else
						referencePoint = VectorGeo.getVectorDegrees(
								Double.valueOf(tokens[1]), Double.valueOf(tokens[2]));
					referenceIn = tokens[3].startsWith("in");
				}
				else if (record.toLowerCase().startsWith("npoints"))
					maxpoints = Integer.valueOf(record.replaceAll("=", " ").split("\\s+")[1].trim());
				else if (record.toLowerCase().startsWith("name"))
					name = record.substring(4).replaceAll("=", " ").trim();
				else if (tokens.length == 2)
				{
					++count;
					try
					{
						double t0 =  Double.parseDouble(tokens[0]);
						double t1 =  Double.parseDouble(tokens[1]);

						if (lonFirst)
							points.add(VectorGeo.getVectorDegrees(t1, t0));
						else
							points.add(VectorGeo.getVectorDegrees(t0, t1));
					}
					catch (NumberFormatException ex)
					{ /* ignore errors */
					}
				}
			}
			if (count >= maxpoints) break;
		}
		setup(points);
	}

	/**
	 * Constructor that accepts a list of unit vectors that define the polygon. The
	 * polygon will be closed, i.e., if the first point and last point are not coincident
	 * then an edge will be created between them. No copies made.
	 * 
	 * <p>
	 * The referencePoint will be the anti-pode of the normalized vector sum of the
	 * supplied points and will be deemed to be 'outside' the polygon.
	 * 
	 * @param points
	 *            Collection
	 * @throws PolygonException
	 */
	public PolygonPoints(double[] referencePoint, boolean referenceIn,
			List<double[]> points) throws Exception { 
		this.referencePoint = referencePoint;
		this.referenceIn = referenceIn;
		setup(points); 
	}

	/**
	 * Constructor that accepts a list of unit vectors that define the polygon. The
	 * polygon will be closed, i.e., if the first point and last point are not coincident
	 * then an edge will be created between them. No copies made.
	 * 
	 * @param points
	 *            Collection
	 * @throws PolygonException
	 */
	public PolygonPoints(double[] referencePoint, boolean referenceIn,
			double[]... points) throws Exception { 
		this(referencePoint, referenceIn, Arrays.asList(points)); 
	}
	
	/**
	 * Constructor that accepts a list of unit vectors that define the polygon. The
	 * polygon will be closed, i.e., if the first point and last point are not coincident
	 * then an edge will be created between them. No copies made.
	 * 
	 * <p>
	 * The referencePoint will be the anti-pode of the normalized vector sum of the
	 * supplied points and will be deemed to be 'outside' the polygon.
	 * 
	 * @param points
	 *            Collection
	 * @throws PolygonException
	 */
	public PolygonPoints(List<double[]> points) throws Exception { setup(points); }

	/**
	 * Constructor that accepts a list of unit vectors that define the polygon. The
	 * polygon will be closed, i.e., if the first point and last point are not coincident
	 * then an edge will be created between them. No copies made.
	 * 
	 * @param points
	 *            Collection
	 * @throws PolygonException
	 */
	public PolygonPoints(double[]... points) throws Exception { this(Arrays.asList(points)); }
	
	@Override
	public void write(DataOutputStream output) throws Exception
	{
		Globals.writeString(output, getClass().getSimpleName());
		output.writeInt(1);
		output.writeDouble(referencePoint[0]);
		output.writeDouble(referencePoint[1]);
		output.writeDouble(referencePoint[2]);
		output.writeBoolean(referenceIn);
		output.writeInt(edges.size());
		for (GreatCircle edge : edges)
			for (int i=0; i<3; i++)
				output.writeDouble(edge.getFirst()[i]);
	}

	/**
	 * Invert the current polygon. What used to be in will be out and what used to be out
	 * will be in.
	 */
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
	public void setReferencePoint(double[] referencePoint, boolean referenceIn) throws Exception
	{
		if (onBoundary(referencePoint))
			throw new Exception("Cannot specify a referencePoint that falls on the boundary of the polygon.");

		if (edgeCrossings(referencePoint) % 2 == 0 ^ this.referenceIn == referenceIn)
			area = 4*PI-area;

		this.referencePoint = referencePoint.clone();
		this.referenceIn = referenceIn;
	}

	/**
	 * Specify the reference point for this polygon and whether or not the specified point
	 * is inside or outside the polygon.
	 * 
	 * @param lat
	 *            geographic latitude in degrees
	 * @param lon
	 *            longitude in degrees
	 * @param referenceIn
	 * @throws Exception 
	 */
	@Override
	public void setReferencePoint(double lat, double lon, boolean referenceIn) throws Exception
	{
		setReferencePoint(VectorGeo.getVectorDegrees(lat, lon), referenceIn);
	}

	/**
	 * @param points
	 *            an array of double[3]
	 * @throws Exception
	 */
	private void setup(List<double[]> points) throws Exception
	{
		if (points.size() < 2)
			throw new Exception("Cannot create a polygon with only "
					+ points.size() + " point(s).");

		// there will be a GreatCircle edge for every vertex
		edges = new ArrayList<GreatCircle>(points.size() + 1);

		Iterator<double[]> it = points.iterator();
		double[] next, previous = it.next();
		GreatCircle gc;
		while (it.hasNext())
		{
			// create a GreatCircle from previous to next and add to list of
			// edges.
			next = it.next();
			gc = new GreatCircle(previous, next);
			if (gc.getDistance() >= TOLERANCE)
			{
				edges.add(gc);
				previous = next;
			}
		}

		// if last point != first point, add another edge to close the
		// polygon
		next = points.iterator().next();

		if (VectorGeo.angle(previous, next) > TOLERANCE)
			// create a GreatCircle from previous to next and add to list of
			// edges.
			edges.add(new GreatCircle(previous, next));

		area = computeArea();

		if (area < TOLERANCE)
			throw new Exception("The polygon is invalid because the area of the polygon is zero "
					+ "even though the polygon consists of "+points.size()+" points.  "
					+ "This can happen if all the points that define the polygon lie on a common great circle.");

		if (referencePoint == null)
		{
			// find the location of the vector sum of all the points.
			double[] center = VectorGeo.center(points
					.toArray(new double[points.size()][3]));

			// deal with degenerate case where the vector sum of the points is zero.
			// One way this can happen is if all the points are evenly distributed along
			// a great circle that encircles the globe (pretty unlikely). There may be other ways.
			if (center[0] == 0. && center[1] == 0. && center[2] == 0.)
			{
				for (GreatCircle edge : edges)
				{
					center = edge.getNormal().clone();
					if (!onBoundary(center))
						break;
				}
			}

			// set referencePoint so that it is on the opposite side of the Earth from center
			referencePoint = new double[] {-center[0], -center[1], -center[2]};
			referenceIn = false;
		}
	}

	/**
	 * return true if point x is located inside the polygon
	 * @param x the point to be evaluated
	 * @return true if point x is located inside the polygon
	 */
	@Override
	public boolean contains(double[] x)
	{
		// if x is colocated with the reference point return referenceIn
		if (VectorGeo.dot(referencePoint, x) > cos(TOLERANCE))
			return referenceIn;

		// if x is on the edge or very close to the edge, return true
		if (onBoundary(x))
			return true;

		// count the number of times that a great circle from reference
		// point to x crosses the boundary of the polygon.  If the number
		// of crossings is even, then return referenceIn.
		// If number of crossings is odd, then return !referenceIn 
		return (edgeCrossings(x) % 2 == 0) == referenceIn;
	}

	/**
	 * When a point and a great circle are compared, the response
	 * might be that the point is on the great circle, to the left
	 * of the great circle (on the same side of the great circle as
	 * the great circle's normal), of to the right of the great circle.
	 * This enum provides a mechanism to describe the result.
	 * @author sandy
	 *
	 */
	private enum ON_GREAT_CIRCLE { ON, LEFT, RIGHT, BEYOND }

	/**
	 * Determine if point is to the left of, on, or to the right of
	 * the specified great circle.
	 * The point is not required to be between gc.getFirst() and gc.getLast()
	 * <p>
	 * Returns LEFT, ON, or RIGHT
	 * @param gc
	 * @param point
	 * @return
	 */
	private ON_GREAT_CIRCLE isOnGreatCicle(GreatCircle gc, double[] point)
	{ 
		double dot = VectorGeo.dot(point, gc.getNormal());
		if (dot > TOLERANCE) return ON_GREAT_CIRCLE.LEFT;
		if (dot < -TOLERANCE) return ON_GREAT_CIRCLE.RIGHT;
		return ON_GREAT_CIRCLE.ON;
	}

	/**
	 * Create a great circle from the reference point to the 
	 * point being evaluated and then count the number of times 
	 * that the great circle intersects the boundary of the polygon.
	 * @param x double[3] unit vector
	 * @return int number of times great circle intersects the polygon
	 * @throws Exception 
	 * @throws PolygonException
	 * @throws GreatCircleException
	 */
	private int edgeCrossings(double[] x)
	{
		// We will create a great circle from the reference point to the 
		// point being evaluated, x, and then count the number of times 
		// that the great circle intersects the boundary of the polygon.
		// Beware that it is possible that some of the points that define
		// the polygon might lie on, or very close to on, the great circle.
		// We have to make sure that those points get counted once, and 
		// only once, as appropriate.

		// create a great circle from the reference point to the 
		// evaluation point
		GreatCircle gc = new GreatCircle(referencePoint, x);

		int ncrossings = 0;
		int first = -1;
		ON_GREAT_CIRCLE on1 = null, on2 = null, on3 = null;
		GreatCircle edge;

		// find the first edge such that the first point of the edge
		// is not on the great circle.  Remember whether the 
		// first point of the edge is to the right or to the left of 
		// the great circle.  
		// There must be at least one polygon point
		// that lies off the great circle because the condition where
		// all polygon points lie on a great circle was checked in the
		// polygon constructor (area = 0).
		for (int i=0; i<edges.size(); ++i)
		{
			on2 = isOnGreatCicle(gc, edges.get(i).getFirst());
			if (on2 != ON_GREAT_CIRCLE.ON)
			{
				first = i;
				break;
			}
		}

		// loop over all the edges, starting from the first one where
		// edge.getFirst() is not ON the great circle from reference point
		// to evaluation point.
		for (int i=0; i< edges.size(); ++i)
		{
			edge = edges.get((first+i) % edges.size());

			// find out if the last point of this edge is 
			// ON, LEFT or RIGHT of the great circle
			on3 = isOnGreatCicle(gc, edge.getLast());

			if (on3 == ON_GREAT_CIRCLE.ON && on2 != ON_GREAT_CIRCLE.ON)
			{
				// the edge.getFirst() is not ON the great
				// circle but edge.getLast() is ON it.
				// So we are stepping onto the great circle.
				// Save the value of on2 as on1 (RIGHT or LEFT).
				on1 = on2;
			}
			else if (on3 == ON_GREAT_CIRCLE.ON && on2 == ON_GREAT_CIRCLE.ON)
			{
				// both ends of this edge are on gc.
				// Do nothing.  We cannot evaluate whether
				// a crossing has occurred until we 
				// get to an edge that has getLast() that 
				// is off the great circle.
			}
			else if (on3 != ON_GREAT_CIRCLE.ON && on2 == ON_GREAT_CIRCLE.ON)
			{
				// We have been on the great circle, but now we
				// are stepping off of it.  If previous edge that
				// was not ON the great circle was on one side of 
				// the great circle and this edge is on the other
				// side of the great circle, then it counts as 
				// a crossing, so long as the distance from the 
				// first point of the great circle to edge.getFirst()
				// is less than the length of the great circle.
				if (on3 != on1 && gc.getDistance(edge.getFirst()) < gc.getDistance())
					++ncrossings;
			}			
			else if (on3 != ON_GREAT_CIRCLE.ON && on2 != ON_GREAT_CIRCLE.ON)
			{
				// neither end of this edge is on the great circle
				// so we just have to figure out if the great circle
				// and the edge intersect.
				if (gc.getIntersection(edge, true) != null)
					++ncrossings;
			}
			on2 = on3;
		}
		return ncrossings;
	}

	/**
	 * Return true if evaluation point is very close to being on the boundary of the
	 * polygon.
	 * 
	 * @param x
	 *            the evaluation point.
	 * @return true if x is very close to being on the boundary of the polygon.
	 * @throws PolygonException
	 */
	public boolean onBoundary(double[] x)
	{
		// if point is very close to any of the polygon points, return true
		for (GreatCircle edge : edges)
			if (VectorGeo.dot(x, edge.getFirst()) >= cos(TOLERANCE))
				return true;

		// if point is very close to one of the edges, return true.
		// Close means that the dot product of the point and the normal
		// to the edge is very close to 1 or -1.  Must also be true that
		// the distance from the first point of the edge to the point is
		// less than the length of the edge.
		for (GreatCircle edge : edges)
			if (edge.getDistance(x) < edge.getDistance() 
					&& abs(VectorGeo.dot(x, edge.getNormal())) < TOLERANCE)
				return true;

		return false;
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
	public double[][][] getPoints(boolean repeatFirstPoint)
	{
		double[][] points = new double[edges.size()
		                               + (repeatFirstPoint ? 1 : 0)][];
		for (int i = 0; i < edges.size(); ++i)
			points[i] = edges.get(i).getFirst().clone();
		if (repeatFirstPoint)
			points[points.length - 1] = points[0];
		return new double[][][] {points};
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
	public double[][][] getPoints(boolean repeatFirstPoint, double maxSpacing)
	{
		ArrayList<double[]> points = new ArrayList<double[]>(edges.size());
		for (int i = 0; i < edges.size(); ++i)
		{
			int n = (int) ceil(edges.get(i).getDistance() / maxSpacing);
			double dx = edges.get(i).getDistance() / n;
			for (int j = 0; j < n; ++j)
				points.add(edges.get(i).getPoint(j * dx));
		}
		if (repeatFirstPoint)
			points.add(edges.get(0).getFirst());

		return new double[][][] {points.toArray(new double[points.size()][])};
	}

	/**
	 * Retrieve a reference to one point on the polygon boundary
	 * 
	 * @return a reference to the first point on the polygon
	 */
	public double[] getPoint(int index)
	{
		return edges.get(index).getFirst();
	}

	/**
	 * Retrieve the area of this polygon. This is the unitless area (radians squared). 
	 * To convert to km^2, multiply the result by R^2 where R is the radius of the sphere.
	 * The range is zero to 4*PI.
	 * @return
	 */
	@Override
	public double getArea() { return Double.isNaN(area) ? computeArea() : area; }

	/**
	 * Retrieve the area of this polygon. This is the unitless area (radians squared). 
	 * To convert to km^2, multiply the result by R^2 where R is the radius of the sphere.
	 * <p>
	 * This method assumes that the polygon is the smaller than the area of a hemisphere.
	 * 
	 * @return the area of this polygon.
	 */
	@Override
	protected double computeArea()
	{
		area = 0;
		double a;
		GreatCircle edge, previous = edges.get(edges.size() - 1);
		for (int i = 0; i < edges.size(); ++i)
		{
			edge = edges.get(i);
			a = PI - VectorGeo.angle(previous.getNormal(), edge.getNormal());

			if (VectorGeo.scalarTripleProduct(previous.getNormal(),
					edge.getNormal(), edge.getFirst()) < 0)
				area += a;
			else
				area += 2 * PI - a;

			previous = edge;
		}
		area -= (edges.size() - 2) * PI;
		if (area > 2*PI) area = 4*PI-area;
		return area;
	}

	/**
	 * Returns a String containing all the points that define the polygon with one 
	 * lat,lon pair per record. lats and lons are in degrees. Order is always lat-lon.
	 * <p>
	 * Longitudes will be adjusted so that they fall in the range minLongitude to
	 * (minLongitude+360).
	 * 
	 * @param repeatFirstPoint boolean
	 * @param minLongitude double in degrees
	 * @return String
	 */
	public String toString(boolean repeatFirstPoint, double minLongitude)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(getClass().getSimpleName()+"\n");
		buf.append(refPt() + "\n");
		double[][] points = getPoints(repeatFirstPoint)[0];
		buf.append(String.format("npoints=%d%n", points.length));
		for (double[] point : points)
		{
		    double lat = VectorGeo.getLatDegrees(point);
			double lon = VectorGeo.getLonDegrees(point);
			while (lon < minLongitude)
				lon += 360.;
			while (lon >= minLongitude + 360)
				lon -= 360.;
			buf.append(String.format("%10.6f %11.6f%n", lat, lon));
		}
		return buf.toString();
	}

	/**
	 * Returns a String containing all the points that define the polygon with one lon,
	 * lat pair per record. lats and lons are in degrees. Longitudes range from -180 to
	 * 180 degrees.  First point is not repeated.
	 * 
	 * @return String
	 */
	@Override
	public String toString()
	{
		return toString(false, -180.);
	}

	/**
	 * Returns the number of edges that define the polygon. Equals the number of unique
	 * GeoVectors that define the polygon.
	 * 
	 * @return the number of edges that define the polygon. Equals the number of unique
	 *         GeoVectors that define the polygon.
	 */
	@Override
	public int size()
	{
		if (edges == null)
			return 0;
		else
			return edges.size();
	}
	
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
			for (double r : op.smallCircleRadii)
				for (GreatCircle edge : this.edges)
					if (edge.getIntersections(((Polygon2D)other).referencePoint, r, true).size() > 0)
						return true;
			return false;
		}

		if (other instanceof PolygonPoints)
		{
			PolygonPoints op = (PolygonPoints) other;
			for (GreatCircle e1 : edges)
				for (GreatCircle e2 : op.edges)
					if (e1.getIntersection(e2, true) != null)
						return true;
			return false;
		}

		throw new Exception("Unrecognized Polygon class");
	}

}
