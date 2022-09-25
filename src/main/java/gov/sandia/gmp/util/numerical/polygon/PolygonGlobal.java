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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;

import gov.sandia.gmp.util.globals.Globals;

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
 * ability to interactively generate polygons and store them in kmz or kml files. Polygon2D
 * has facilities to read and write kmz and kml files. Polygon2D can also read and write
 * polygons to an ascii file. An advantage of the ascii file format is that the position
 * of the reference point and whether the reference point is inside or outside the polygon
 * are stored in the file and therefore can be manipulated by users. For example, if a
 * user wishes to generate a polygon that is larger than a hemisphere, they could define
 * the polygon boundary points with Google Earth but the resulting polygon would be
 * inverted (the inside would be outside and the outside would be inside). They could use
 * Polygon2D to write the polygon to an ascii file,and manually edit the file to modify the
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
public class PolygonGlobal extends Polygon2D implements Serializable, Callable<Polygon2D>
{
	private static final long serialVersionUID = -5569895381213292444L;

	/**
	 * Default constructor. 
	 */
	public PolygonGlobal()
	{
		referencePoint = new double[] {1,0,0};
		referenceIn = true;
		name = "global in";
	}

	public PolygonGlobal(DataInputStream input) throws Exception
	{
		this();
		int format = input.readInt();
		if (format != 1)
			throw new IOException(format+" is not a recognized format.");
		referenceIn = input.readBoolean();
	}

	/**
	 * Create a global polygon. Method constains(x) will always return the value of global
	 * that is specified in this constructor.
	 * 
	 * @param global
	 *            boolean
	 */
	public PolygonGlobal(boolean global)
	{
		referencePoint = new double[] {1,0,0};
		referenceIn = global;
		name = "global "+(referenceIn ? "in" : "out");
	}

	public PolygonGlobal(BufferedReader buffer) throws IOException {
		// expecting a single record 'global' or 'global in'
		this(true);
		String[] tokens = tokenize(buffer.readLine());
		if (tokens.length > 1)
			referenceIn = tokens[1].startsWith("in");
	}

	public PolygonGlobal(String string) {
		this(string.toLowerCase().contains("in"));
	}

	/**
	 * Returns true if this Polygon2D contains any of the supplied unit vectors
	 * 
	 * @param points
	 *            array of unit vectors
	 * @return true if this Polygon2D contains any of the supplied unit vectors
	 * @throws PolygonException
	 */
	@Override
	public boolean containsAny(double[]... points) { return referenceIn; }

	/**
	 * Returns true if this Polygon2D contains any of the supplied unit vectors
	 * 
	 * @param points Collection of unit vectors
	 * @return true if this Polygon2D contains any of the supplied unit vectors
	 * @throws PolygonException
	 */
	@Override
	public boolean containsAny(Collection<double[]> points) { return referenceIn; }

	/**
	 * Returns true if this Polygon2D contains all of the supplied unit vectors
	 * 
	 * @param points
	 *            double[][3], the unit vectors to check
	 * @return true if this Polygon2D contains all of the supplied unit vectors
	 * @throws PolygonException
	 */
	@Override
	public boolean containsAll(double[]... points) { return referenceIn; }

	/**
	 * Returns true if this Polygon2D contains all of the supplied unit vectors
	 * 
	 * @param points Collection of unit vectors to check
	 * @return true if this Polygon2D contains all of the supplied unit vectors
	 * @throws PolygonException
	 */
	@Override
	public boolean containsAll(Collection<double[]> points) { return referenceIn; }

	/**
	 * return true if point x is located inside the polygon
	 * @param x the point to be evaluated
	 * @return true if point x is located inside the polygon
	 */
	@Override
	public boolean contains(double[] x) { return referenceIn; }

	/**
	 * Checks a List of points to see if they are contained in this polygon
	 * 
	 * @param points Collection of unit vectors to check
	 * @return the number of points that are contained within this Polygon2D
	 * @throws PolygonException
	 */
	@Override
	public ArrayList<Boolean> contains(Collection<double[]> points) { 
		ArrayList<Boolean> in = new ArrayList<>(points.size());
		for (int i=0; i<points.size(); ++i)
			in.add(referenceIn);
		return in; 
	}

	/**
	 * Checks a List of points to see if they are contained in this polygon
	 * 
	 * @param points
	 *            double[][3], the unit vectors to check
	 * @return the number of points that are contained within this Polygon2D
	 * @throws PolygonException
	 */
	@Override
	public boolean[] contains(double[]... points) { 
		boolean[] in = new boolean[points.length];
		Arrays.fill(in, referenceIn);
		return in; 
	}

	/**
	 * Retrieve the area of this polygon. This is the unitless area (radians squared). 
	 * To convert to km^2, multiply the result by R^2 where R is the radius of the sphere.
	 * The range is zero to 4*PI.
	 * @return
	 */
	@Override
	public double getArea() { return referenceIn ? 4*PI : 0.; }

	/**
	 * Returns "global <in or out>
	 * 
	 * @return String
	 */
	@Override
	public String toString() { return getClass().getSimpleName()+"\nglobal " + (referenceIn ? "in" : "out")+"\n"; }

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
	 * Returns true if this Polygon2D and some other Polygon2D overlap.
	 * Note that it is possible for two polygons to overlap even when
	 * neither Polygon2D contains any of the points that define the other
	 * Polygon2D.
	 * @param other
	 * @return
	 */
	@Override
	public boolean overlaps(Polygon other)
	{
		if (other instanceof PolygonGlobal)
			return this.referenceIn && ((Polygon2D)other).referenceIn;
		return this.referenceIn;
	}

//	@Override
//	public void write(Writer output) throws Exception {
//		// write 2 lines: "PolygonGlobal" and either "global in" or "global out"
//		output.append(String.format("%s%nglobal %s%n", getClass().getSimpleName(),
//				(referenceIn ? "in" : "out")));
//	}

	@Override
	public void write(DataOutputStream output) throws Exception
	{
		Globals.writeString(output, getClass().getSimpleName());
		output.writeInt(1);
		output.writeBoolean(referenceIn);
	}

	@Override
	public double[][][] getPoints(boolean repeatFirstPoint) {
		return new double[1][0][];
	}

	@Override
	public double[][][] getPoints(boolean repeatFirstPoint, double maxSpacing) {
		return new double[1][0][];
	}

	@Override
	public boolean onBoundary(double[] referencePoint2) { return false; }

	@Override
	protected double computeArea() { area = referenceIn ? 4*PI : 0.; return area; }

}
