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

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.mapprojection.RobinsonProjection;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

public class Ellipse extends Polygon2D {

	private static final long serialVersionUID = 6448266829401552161L;

	double majax;
	double minax;
	double trend;

	/**
	 * 
	 * @param referencePoint unit vector at center of ellipse
	 * @param referenceIn is the center of the ellipse 'inside' or 'outside' the polygon
	 * @param majorAxis 
	 * @param minorAxis
	 * @param trend
	 * @param inDegrees
	 * @throws Exception
	 */
	public Ellipse(double[] referencePoint, boolean referenceIn, double majorAxis, double minorAxis, double trend, boolean inDegrees) throws Exception {
		if (Math.abs(VectorUnit.dot(referencePoint, new double[] {0,0,1})) > 0.9999999)
			throw new Exception("Center of ellipse cannot be located on a pole.");
		this.referencePoint = referencePoint;
		this.referenceIn = referenceIn;
		this.majax = inDegrees ? toRadians(majorAxis) : majorAxis;
		this.minax = inDegrees ? toRadians(minorAxis) : minorAxis;
		this.trend = inDegrees ? toRadians(trend) : trend;
	}

	public Ellipse(BufferedReader buffer) throws IOException { 
		this(buffer.readLine()); 
	}

	/**
	 * Expecting a record containing 'referencePoint <lat> <lon> <in or out> <radius1> ...'
	 * Lat, lon, and radii all in degrees 
	 * @param record
	 * @throws IOException
	 */
	public Ellipse(String record) throws IOException {
		// expecting a single line like: "referencePoint <lat> <lon> <in or out> majax minax trend"
		String[] tokens = record.trim().split("\\s+");
		referencePoint = VectorGeo.getVectorDegrees(
				Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]));
		referenceIn = tokens[3].toLowerCase().startsWith("in");
		majax = toRadians(Double.parseDouble(tokens[4]));
		minax = toRadians(Double.parseDouble(tokens[5]));
		trend = toRadians(Double.parseDouble(tokens[6]));
	}

	public Ellipse(DataInputStream input) throws Exception
	{
		int format = input.readInt();
		if (format != 1)
			throw new IOException(format+" is not a recognized format.");
		referencePoint = new double[] {input.readDouble(), input.readDouble(), input.readDouble()};
		referenceIn = input.readBoolean();
		majax = input.readDouble(); // radians
		minax = input.readDouble(); // radians
		trend = input.readDouble(); // radians
	}

	/**
	 * compute the distance from the center of the ellipse to its perimeter, in
	 * the direction of point
	 * @param point some other point
	 * @return distance to perimeter in radians
	 * @throws Exception if center is at north or south pole, or if distance from center
	 * to point is 0 or PI.
	 */
	public double distanceToPerimeter(double[] point) throws Exception {
		return distanceToPerimeter(VectorUnit.azimuth(referencePoint, point, Double.NaN));
	}

	/**
	 * compute the distance from the center of the ellipse to its perimeter, in
	 * the direction of point
	 * @param point some other point
	 * @return distance to perimeter in degrees
	 * @throws Exception 
	 */
	public double distanceToPerimeterDegrees(double[] point) throws Exception {
		return toDegrees(distanceToPerimeter(point));
	}

	/**
	 * compute the distance from the center of the ellipse to its perimeter, in
	 * the direction specified by azimuth.
	 * @param azimuth in degrees
	 * @return distance to perimeter in degrees
	 * @throws Exception 
	 */
	public double distanceToPerimeterDegrees(double azimuth) throws Exception {
		return toDegrees(distanceToPerimeter(toRadians(azimuth)));
	}

	/**
	 * compute the distance from the center of the ellipse to its perimeter, in
	 * the direction specified by azimuth.
	 * @param azimuth in radians
	 * @return distance to perimeter in radians
	 * @throws Exception if center is at north or south pole
	 */
	public double distanceToPerimeter(double azimuth) throws Exception {
		if (Double.isNaN(azimuth))
			throw new Exception("Cannot compute distance to perimeter because azimuth is NaN");
		// calculate distance from center of ellipse to perimeter of ellipse, in
		// the direction specified by VectorMod v.
		double theta = trend - azimuth;
		double a = majax*sin(theta);
		double b = minax*cos(theta);
		return majax*minax/sqrt(a*a+b*b);
	}

	@Override
	public boolean onBoundary(double[] x) throws Exception {
		double d1 = VectorUnit.angle(referencePoint, x);
		double d2 = distanceToPerimeter(x);
		return Math.abs(d1-d2) < 1e-7;
	}

	@Override
	public boolean contains(double[] x) throws Exception {
		double distance = VectorUnit.angle(referencePoint, x);
		// note, distanceToPerimeter(x) will throw an exception if distance == 0 or PI.
		boolean in;
		if (distance <= minax) 
			in = referenceIn;
		else if (distance > majax)
			in = !referenceIn;	
		else 
			in = referenceIn == distance <= distanceToPerimeter(x);
			
		if (in)
			for (Polygon2D hole : getHoles())
				if (!hole.contains(x)) {
					in = false;
					break;
				}
		return in;
	}

	@Override
	public boolean overlaps(Polygon other) throws Exception {
		if (other instanceof PolygonGlobal)
			return ((PolygonGlobal)other).referenceIn;

		if (other instanceof SmallCircle) {
			SmallCircle sc = (SmallCircle)other;
			double separation = VectorUnit.angle(this.referencePoint, sc.referencePoint);
			if (separation > this.majax+sc.radius)
				return false;
			if (separation <= this.minax+sc.radius)
				return true;

			// TODO: I don't think this is right
			double rThis = distanceToPerimeter(sc.referencePoint);
			return separation <= rThis+sc.radius;
		}

		if (other instanceof Ellipse) {
			Ellipse e = (Ellipse)other;
			double separation = VectorUnit.angle(this.referencePoint, e.referencePoint);
			if (separation > this.majax+e.majax)
				return false;
			if (separation <= this.minax+e.minax)
				return true;

			// TODO: I don't think this is right
			double rThis = distanceToPerimeter(e.referencePoint);
			double rOther = e.distanceToPerimeter(referencePoint);
			return separation <= rThis+rOther;
		}

		return other.overlaps(this);
	}

	@Override
	public void write(DataOutputStream output) throws Exception {
		Globals.writeString(output, getClass().getSimpleName());
		output.writeInt(1);
		output.writeDouble(referencePoint[0]);
		output.writeDouble(referencePoint[1]);
		output.writeDouble(referencePoint[2]);
		output.writeBoolean(referenceIn);
		output.writeDouble(majax);
		output.writeDouble(minax);
		output.writeDouble(trend);
	}

	@Override
	public double[][] getPoints(boolean repeatFirstPoint) throws Exception {
		return getPoints(repeatFirstPoint, TWO_PI/100.);
	}

	/**
	 * Retrieve a deep copy of the points on the polygon.
	 * 
	 * @param repeatFirstPoint
	 *            if true, last point will be reference to the same point as the first
	 *            point.
	 * @param maxSpacing max distance between points, in radians
	 * @return a deep copy of the points on the polygon.
	 * @throws Exception 
	 */
	@Override
	public double[][] getPoints(boolean repeatFirstPoint, double spacing) throws Exception
	{
		int nIntervals =  Math.max(12, (int)Math.ceil(Math.sin(majax)*TWO_PI/spacing));
		double dx = TWO_PI/nIntervals;
		int n = repeatFirstPoint ? nIntervals+1 : nIntervals;
		double[][] points = new double[n][];
		dx = -dx; // compute azimuths in counter clockwise order (right hand rule)
		for (int j=0; j<n; ++j) {
			points[j] = VectorUnit.move(referencePoint, distanceToPerimeter(j*dx), j*dx);
		}

		return points;
	}

	/**
	 * Retrieve a deep copy of the points on the polygon.
	 * 
	 * @param repeatFirstPoint
	 *            if true, last point will be reference to the same point as the first
	 *            point.
	 * @param nPoints number of points
	 * @return a deep copy of the points on the polygon.
	 * @throws Exception 
	 */
	public double[][] getPoints(boolean repeatFirstPoint, int nPoints) throws Exception
	{
		int nIntervals = repeatFirstPoint ? nPoints+1 : nPoints;
		double dx = TWO_PI/nIntervals;
		int n = repeatFirstPoint ? nIntervals+1 : nIntervals;
		double[][] points = new double[n][];
		dx = -dx; // compute azimuths in counter clockwise order (right hand rule)
		for (int j=0; j<n; ++j) {
			points[j] = VectorUnit.move(referencePoint, distanceToPerimeter(j*dx), j*dx);
		}

		return points;
	}

	/**
	 * Retrieve the area of this polygon. This is the unitless area (radians squared). 
	 * To convert to km^2, multiply the result by R^2 where R is the radius of the sphere.
	 * <p>
	 * This method assumes that the polygon is the smaller than the area of a hemisphere.
	 * 
	 * @return the area of this polygon.
	 * @throws Exception 
	 */
	@Override
	protected double computeArea() throws Exception
	{
		area = new PolygonPoints(getPoints(true)).getArea();
		return area;
	}


	@Override
	public int size() {
		return 0;
	}

	/**
	 * Returns a String containing className, referencePoint, in/out, majoraxix, minoraxis, trend,
	 * all in degrees.
	 * 
	 * @return String
	 */
	@Override
	public String toString()
	{
		return String.format("%s%nreferencePoint %s %s %1.6f %1.6f %1.6f", 
				getClass().getSimpleName(), VectorGeo.getLatLonString(referencePoint, "%1.6f %1.6f"),
				(referenceIn ? "in" : "out"),
				toDegrees(majax), toDegrees(minax), toDegrees(trend));
	}
	
	static public void vtkEllipses(File vtkFile, Ellipse...ellipses) throws Exception {
		int n=0;
		int nPoints = 150;
		ArrayList<double[]> points = new ArrayList<double[]>(ellipses.length * (nPoints+1));
		ArrayList<VTKCell> cells = new ArrayList<>(2*ellipses.length);
		
		n=0;
		for (Ellipse e : ellipses) {
			double[][] pts = e.getPoints(true, nPoints);
			points.addAll(Arrays.asList(pts));
			cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, n, pts.length));
			n += pts.length;

			points.add(e.getReferencePoint());
			cells.add(new VTKCell(VTKCellType.VTK_VERTEX, n, 1));
			++n;
		}
		
		// find the average location of all the ellipse centers
		n=0;
		double[][] centers = new double[ellipses.length][];
		for (Ellipse e : ellipses)
			centers[n++] = e.getReferencePoint();
		double[] center = VectorUnit.center(centers);
		double earthRadius = VectorGeo.getEarthRadius(center);
		
		for (n=0; n<points.size(); ++n) {
			double az = VectorUnit.azimuth(center, points.get(n), Double.NaN);
			double dist = VectorUnit.angle(center, points.get(n))*earthRadius;
			points.set(n, new double[] {dist*sin(az), dist*cos(az), 0.});
		}

//		// get euler rotation matrix that will rotate coordinates so that center is located at lat, lon = 0, 0
//		double[] eulerMatrix = VectorUnit.getEulerMatrix(EarthShape.SPHERE.getLon(center)+Math.PI/2, 
//				EarthShape.SPHERE.getLat(center), -Math.PI/2);
//		// apply the rotation to all points.
//		for (n=0; n<points.size(); ++n)
//			points.set(n, VectorUnit.eulerRotation(points.get(n), eulerMatrix));
		
		VTKDataSet.write(vtkFile, points, cells); 
	}

}
