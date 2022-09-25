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

import static java.lang.Math.ceil;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
public abstract class Polygon2D implements Polygon, Serializable, Callable<Polygon2D> 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3208955559781800666L;

	/**
	 * A point on the surface of the unit sphere that is used as a reference point. The
	 * status of this point relative to the polygon is known, i.e., it is known if this
	 * point is inside or outside the polygon.
	 */
	protected double[] referencePoint;

	/**
	 * true if the referencePoint is inside the polygon.
	 */
	protected boolean referenceIn;

	/**
	 * The area of the polygon, assuming a unit sphere.  Area will range from 
	 * zero to 4*PI.  Lazy evaluation is used.  Area is computed the first time
	 * the area is requested.
	 */
	protected double area = Double.NaN;

	/**
	 * Some unspecified information that applications can attach to this polygon. This
	 * information is not processed in anyway by Polygon.
	 */
	public Object attachment;

	private static final int pointsPerTask=1000;
	protected int taskId;
	protected Map<double[], Boolean> taskPointMap;
	protected List<double[]> taskPoints;
	protected List<Boolean> taskContained;

	/**
	 * Name of this polygon.  If this polygon is loaded from a file, the
	 * name defaults to the name of the file without the extension.
	 */
	protected String name = getClass().getSimpleName();

	protected String polygonFile;

	private Integer hash;

	@Override
	public int hashCode() { if (hash == null) hash = toString().hashCode(); return hash; }

	@Override
	public boolean equals(Object other)	{
		return other != null && other.getClass().equals(this.getClass()) 
				&& other.toString().equals(this.toString());
	}


	/**
	 * Retrieve a reference to the referencePoint.
	 * 
	 * @return a reference to the referencePoint.
	 * @throws Exception 
	 */
	@Override
	public double[] getReferencePoint()	{ return referencePoint; }

	/**
	 * Returns a String like: "referencePoint lat lon in|out"
	 */
	@Override
	public String refPt()
	{
		return String.format("referencePoint %1.6f %1.6f %s",
				VectorGeo.getLatDegrees(referencePoint),
				VectorGeo.getLonDegrees(referencePoint),
				(referenceIn ? "in" : "out"));
	}

	@Override
	public boolean getReferencePointIn() { return referenceIn; }

	/**
	 * Returns the current polygon file setting (null if the polygon was never
	 * set from a standard file.
	 * 
	 * @return The current polygon file setting.
	 */
	@Override
	public String getPolygonFile() { return polygonFile; }

	@Override
	public Polygon setPolygonFile(String f) { this.polygonFile = f; return this; }

	@Override
	public String getName() { return name;	}

	@Override
	public Polygon2D setName(String name) { this.name = name; return this; }

	/**
	 * Invert the current polygon. What used to be in will be out and what used to be out
	 * will be in.
	 */
	@Override
	public void invert() { referenceIn = !referenceIn; hash = null; }

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
		this.referencePoint = referencePoint.clone(); this.referenceIn = referenceIn;
		hash = null; area = Double.NaN;
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
	public void setReferencePoint(double lat, double lon, boolean referenceIn) throws Exception	{
		if (onBoundary(referencePoint))
			throw new Exception("Cannot specify a referencePoint that falls on the boundary of the polygon.");
		this.referencePoint = referencePoint.clone();
		this.referenceIn = referenceIn;
		hash = null; area = Double.NaN;
	}

	@Override
	public Object getAttachment() {
		return attachment;
	}

	@Override
	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

	public abstract boolean onBoundary(double[] referencePoint2);

	/**
	 * return true if point x is located inside the polygon
	 * @param x the point to be evaluated
	 * @return true if point x is located inside the polygon
	 */
	@Override
	public abstract boolean contains(double[] x);

	/**
	 * Returns true if this Polygon contains any of the supplied unit vectors
	 * 
	 * @param points
	 *            array of unit vectors
	 * @return true if this Polygon contains any of the supplied unit vectors
	 * @throws PolygonException
	 */
	@Override
	public boolean containsAny(double[]... points)
	{
		for (double[] point : points)
			if (contains(point))
				return true;
		return false;
	}

	/**
	 * Returns true if this Polygon contains any of the supplied unit vectors
	 * 
	 * @param points Collection of unit vectors
	 * @return true if this Polygon contains any of the supplied unit vectors
	 * @throws PolygonException
	 */
	@Override
	public boolean containsAny(Collection<double[]> points)
	{
		for (double[] point : points)
			if (contains(point))
				return true;
		return false;
	}

	/**
	 * Returns true if this Polygon contains all of the supplied unit vectors
	 * 
	 * @param points
	 *            double[][3], the unit vectors to check
	 * @return true if this Polygon contains all of the supplied unit vectors
	 * @throws PolygonException
	 */
	@Override
	public boolean containsAll(double[]... points)
	{
		for (double[] point : points)
			if (!contains(point))
				return false;
		return true;
	}

	/**
	 * Returns the number of points that are contained within this Polygon
	 * 
	 * @param points Collection of unit vectors to check
	 * @return the number of points that are contained within this Polygon
	 * @throws PolygonException
	 */
	@Override
	public ArrayList<Boolean> contains(Collection<double[]> points)
	{
		ArrayList<Boolean> contained = new ArrayList<Boolean>(points.size());
		for (double[] point : points)
			contained.add(contains(point));
		return contained;
	}

	/**
	 * Returns the number of points that are contained within this Polygon
	 * 
	 * @param points
	 *            double[][3], the unit vectors to check
	 * @return the number of points that are contained within this Polygon
	 * @throws PolygonException
	 */
	@Override
	public boolean[] contains(double[]... points)
	{
		boolean[] contained = new boolean[points.length];
		for (int i=0; i<points.length; ++i)
			contained[i] = contains(points[i]);
		return contained;
	}

	/**
	 * Returns true if this Polygon contains all of the supplied unit vectors
	 * 
	 * @param points Collection of unit vectors to check
	 * @return true if this Polygon contains all of the supplied unit vectors
	 * @throws PolygonException
	 */
	@Override
	public boolean containsAll(Collection<double[]> points)
	{
		for (double[] point : points)
			if (!contains(point))
				return false;
		return true;
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
	public abstract boolean overlaps(Polygon other) throws Exception;


	/**
	 * Split on any number of commas and/or spaces.  
	 * Convert to lower case.  Ignore comment lines that start 
	 * with '#'.
	 * @param record
	 * @return tokenized record
	 */
	public static String[] tokenize(String record)
	{
		record = record.trim();
		if (record.length() == 0 || record.startsWith("#"))
			return new String[0];

		return record.toLowerCase().replaceAll(";", " ").replaceAll(",", " ").split("\\s+");
	}

	/**
	 * Write this polygon to a file.
	 * If the file extension is 'vtk' then the file is written in 
	 * vtk format.  If the file extension is 'kml' or 'kmz' the 
	 * file is written in a format compatible with Google Earth.
	 * Otherwise the file is written in ascii format with boundary
	 * points written in lat-lon order.
	 * 
	 * @param fileName
	 *            name of file to receive the polygon
	 * @throws Exception
	 */
	@Override
	public void write(File fileName) throws Exception
	{
		String name = fileName.getName().toLowerCase();
		if (name.endsWith("vtk"))
			writeVTK(fileName);
		else if (name.endsWith("kml") || name.endsWith("kmz"))
			PolygonKMLZ.writeKMLZ(fileName, this);
		else
		{
			Writer output = new BufferedWriter(new FileWriter(fileName));
			write(output);
			output.close();
			polygonFile = fileName.getCanonicalPath();
		}
	}

	/**
	 * Write the current polygon to a file in binary format.
	 * @param fileName
	 * @throws Exception
	 */
	@Override
	public void writeBinary(File fileName) throws Exception
	{
		DataOutputStream output = new DataOutputStream(new FileOutputStream(fileName));
		output.writeLong(Polygon.magicKey);
		write(output);
		output.close();
	}


	/**
	 * Write the current polygon to a file in ascii format.
	 * 
	 * @param fileName
	 *            name of file to receive the polygon
	 * @param latLon
	 *            if this String starts with 'lon' the polygon boundary points will be
	 *            written in lon-lat order otherwise they will be written in lat-lon
	 *            order.
	 * @throws Exception
	 */
	@Override
	public void write(Writer output) throws Exception {
	    output.write(toString());
	}

	@Override
	public abstract void write(DataOutputStream output) throws Exception;

	@Override
	public void writeVTK(File file) throws Exception	{

		if (this instanceof PolygonGlobal)
			throw new Exception("Cannot write a vtk file for PolygonGlobal because it has no points");

		double[][][] list = getPoints(true);
		String fileName = file.getCanonicalPath();

		// if multiple polygons are available in the main polygon, as can be the case with 
		// PolygonSmallCircle objects, they must be written to separate vtk files.  
		// Generate new filenames that contain '%d' for this purpose.
		if (list.length > 1)
		{
			int i=fileName.lastIndexOf('.');
			String first = fileName.substring(0, i);
			String ext = fileName.substring(i);
			fileName = first+"_%d"+ext;
		}

		for (int p=0; p<list.length; ++p)
		{
			double[][] points = list[p];

			if (points.length == 0)
				throw new Exception("Cannot write a vtk file because method getPoints() returned zero values");

			File f = new File(fileName.contains("_%d") ? String.format(fileName, p) : fileName);
			DataOutputStream output = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(f)));

			output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
			output.writeBytes(String.format("Polygon%n"));
			output.writeBytes(String.format("BINARY%n"));

			output.writeBytes(String.format("DATASET POLYDATA%n"));

			output.writeBytes(String.format("POINTS %d double%n", points.length));

			// iterate over all the polygon vertices and write out their position
			for (int i = 0; i < points.length; ++i)
			{
				output.writeDouble(points[i][0]);
				output.writeDouble(points[i][1]);
				output.writeDouble(points[i][2]);
			}

			// write out node connectivity
			output.writeBytes(String.format("POLYGONS 1 %d%n", points.length + 1));

			output.writeInt(points.length);
			for (int i = 0; i < points.length; ++i)
				output.writeInt(i);

			output.close();
		}
	}

	/**
	 * If this polygon has a collection of points, retrieve a reference to the collection.
	 * If not, generate points and return them.  Some Polygon objects, such as PolygonSmallCircle
	 * objects, are capable of returning multiple polygons, hence the returned array has to be 3D.
	 * The dimensions will be npolygons x npoints x 3 (unit vectors).
	 * 
	 * @param repeatFirstPoint
	 *            if true, last point will be reference to the same point as the first
	 *            point.
	 * @return a deep copy of the points on the polygon.
	 */
	@Override
	public abstract double[][][] getPoints(boolean repeatFirstPoint);

	/**
	 * If this polygon has a collection of points, a deep copy of those points is generated, ensuring that
	 * the maximum spacing between points is no greater than specified value.
	 * If not, generate points and return them.  Some Polygon objects, such as PolygonSmallCircle
	 * objects, are capable of returning multiple polygons, hence the returned array has to be 3D.
	 * The dimensions will be npolygons x npoints x 3 (unit vectors).
	 * 
	 * @param repeatFirstPoint
	 *            if true, last point will be reference to the same point as the first
	 *            point.
	 * @param maxSpacing max distance between points, in radians
	 * @return a deep copy of the points on the polygon.
	 */
	@Override
	public abstract double[][][] getPoints(boolean repeatFirstPoint, double maxSpacing);

	/**
	 * Retrieve the area of this polygon. This is the unitless area (radians squared). 
	 * To convert to km^2, multiply the result by R^2 where R is the radius of the sphere.
	 * The range is zero to 4*PI.
	 * @return
	 */
	@Override
	public double getArea() { return Double.isNaN(area) ? computeArea() : area; }

	protected abstract double computeArea();

	/**
	 * Returns the number of edges that define the polygon. Equals the number of unique
	 * GeoVectors that define the polygon.
	 * 
	 * @return the number of edges that define the polygon. Equals the number of unique
	 *         GeoVectors that define the polygon.
	 */
	@Override
	public abstract int size();

	/**
	 * Determine if a list of unit vectors is contained within this polygon. 
	 * Concurrency is used to process batches of points in parallel.
	 * @param points list of points to evaluate
	 * @param nProcessors  number of processors to use in concurrent mode.  
	 * If nProcessors < 2, points are evaluated in sequential mode.
	 * @return ArrayList<Boolean> of length equal to input points.
	 * @throws Exception
	 */
	@Override
	public ArrayList<Boolean> contains(List<double[]> points, int nProcessors) throws Exception
	{
		ArrayList<Boolean> contained = new ArrayList<Boolean>(points.size());

		int nTasks = (int) ceil(points.size()/(double)pointsPerTask);

		if (nProcessors < 2 || nTasks < 1)
		{
			for (int i=0; i < points.size(); ++i)
				contained.add(contains(points.get(i)));
		}
		else
		{
			try 
			{
				for (int i=0; i<points.size(); ++i) contained.add(null);

				// set up the thread pool.
				ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors
						.newFixedThreadPool(nProcessors);

				CompletionService<Polygon2D> queue = 
						new ExecutorCompletionService<Polygon2D>(threadPool);

				// submit all the tasks at once.
				for (int taskId = 0; taskId < nTasks; ++taskId)
				{
					Polygon2D task = (Polygon2D) this.clone();

					task.taskId = taskId;
					task.taskPoints = points;
					task.taskContained = contained;
					queue.submit(task); 
				}

				// pause until all the tasks are complete.
				for (int taskId=0; taskId<nTasks; ++taskId)  queue.take().get();

				threadPool.shutdown();

			} 
			catch (Exception e) 
			{
				throw new Exception(e);
			}
		}
		return contained;
	}

	/**
	 * Determine if a set of unit vectors is contained within this polygon. 
	 * Concurrency is used to process batches of points in parallel.
	 * @param points set of points to evaluate
	 * @param nProcessors  number of processors to use in concurrent mode.  
	 * If nProcessors < 2, points are evaluated in sequential mode.
	 * @return HashMap<double[], Boolean> of length equal to input points.
	 * @throws Exception
	 */
	@Override
	public HashMap<double[], Boolean> contains(Set<double[]> points, int nProcessors) throws Exception
	{
		HashMap<double[], Boolean> contained = new HashMap<double[], Boolean>(points.size());

		int nTasks = (int) ceil(points.size()/(double)pointsPerTask);

		if (nProcessors < 2 || nTasks < 2)
		{
			for (double[] point : points)
				contained.put(point, contains(point));
		}
		else
		{
			try 
			{
				// set up the thread pool.
				ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors
						.newFixedThreadPool(nProcessors);

				CompletionService<Polygon2D> queue = 
						new ExecutorCompletionService<Polygon2D>(threadPool);

				Iterator<double[]> it = points.iterator();
				for (int taskId = 0; taskId < nTasks; ++taskId)
				{
					Polygon2D task = (Polygon2D) this.clone();

					task.taskPointMap = new HashMap<double[], Boolean>(pointsPerTask);
					for (int i=0; i<pointsPerTask && it.hasNext(); ++i)
						task.taskPointMap.put(it.next(), null);

					queue.submit(task);
				}

				// pause until all the tasks are complete.
				for (int taskId=0; taskId<nTasks; ++taskId)
				{
					Polygon2D task = (Polygon2D) queue.take().get();
					for (Map.Entry<double[], Boolean> entry : task.taskPointMap.entrySet())
						contained.put(entry.getKey(), entry.getValue());
				}

				threadPool.shutdown();

			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				throw new Exception(e);
			}
		}
		return contained;
	}

	/**
	 * Determine if a set of unit vectors is contained within this polygon. 
	 * Concurrency is used to process batches of points in parallel.
	 * @param points map from unit vector to boolean containing points to evaluate.
	 * Only points where getValue() == null are evaluated.  If a point has 
	 * getValue() = true or false, it is assumed that the point has already
	 * been evaluated and it is not evaluated again.
	 * @param nProcessors  number of processors to use in concurrent mode.  
	 * If nProcessors < 2, points are evaluated in sequential mode.
	 * @throws Exception
	 */
	@Override
	public void contains(Map<double[], Boolean> points, int nProcessors) throws Exception
	{
		int nTasks = (int) ceil(points.size()/(double)pointsPerTask);

		if (nProcessors < 2 || nTasks < 2)
		{
			for (Entry<double[], Boolean> point : points.entrySet())
				if (point.getValue() == null)
					point.setValue(contains(point.getKey()));
		}
		else
		{
			try 
			{
				// set up the thread pool.
				ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors
						.newFixedThreadPool(nProcessors);

				CompletionService<Polygon2D> queue = 
						new ExecutorCompletionService<Polygon2D>(threadPool);

				Iterator<Map.Entry<double[], Boolean>> it = points.entrySet().iterator();
				for (int taskId = 0; taskId < nTasks; ++taskId)
				{
					Polygon2D task = (Polygon2D) this.clone();

					task.taskPointMap = new HashMap<double[], Boolean>(pointsPerTask);
					for (int i=0; i<pointsPerTask && it.hasNext(); ++i)
					{
						Map.Entry<double[], Boolean> entry = it.next();
						task.taskPointMap.put(entry.getKey(), entry.getValue());
					}

					queue.submit(task);
				}

				// pause until all the tasks are complete.
				for (int taskId=0; taskId<nTasks; ++taskId)
				{
					Polygon2D task = (Polygon2D) queue.take().get();
					for (Map.Entry<double[], Boolean> entry : task.taskPointMap.entrySet())
						points.put(entry.getKey(), entry.getValue());
				}

				threadPool.shutdown();

			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				throw new Exception(e);
			}
		}
	}

	@Override
	public Polygon2D call()
	{
		if (taskPoints != null)
		{
			for (int i=taskId*pointsPerTask; i<(taskId+1)*pointsPerTask && i < taskPoints.size(); ++i)
				taskContained.set(i, contains(taskPoints.get(i)));
		}
		if (taskPointMap != null)
		{
			for (Map.Entry<double[], Boolean> entry : taskPointMap.entrySet())
				if (entry.getValue() == null)
					entry.setValue(contains(entry.getKey()));

		}
		return this;
	}

	@Override
	public Horizon getTop() {
		throw new UnsupportedOperationException("Cannot invoke a Polygon3D method on a Polygon2D object.");
	}

	@Override
	public Horizon getBottom() {
		throw new UnsupportedOperationException("Cannot invoke a Polygon3D method on a Polygon2D object.");
	}

	@Override
	public boolean contains(double[] x, double radius, int layer, double[] radii) {
		throw new UnsupportedOperationException("Cannot invoke a Polygon3D method on a Polygon2D object.");
	}

	@Override
	public boolean contains(double[] x, int layer) {
		throw new UnsupportedOperationException("Cannot invoke a Polygon3D method on a Polygon2D object.");
	}

	@Override
	public boolean containsAll(ArrayList<double[]> points, ArrayList<Double> radii, ArrayList<Integer> layers,
			ArrayList<double[]> layerRadii) {
		throw new UnsupportedOperationException("Cannot invoke a Polygon3D method on a Polygon2D object.");
	}

	@Override
	public boolean containsAny(ArrayList<double[]> points, ArrayList<Double> radii, ArrayList<Integer> layers,
			ArrayList<double[]> layerRadii) {
		throw new UnsupportedOperationException("Cannot invoke a Polygon3D method on a Polygon2D object.");
	}

	@Override
	public boolean containsAll(double[][] points, double[] radii, int[] layers, double[][] layerRadii) {
		throw new UnsupportedOperationException("Cannot invoke a Polygon3D method on a Polygon2D object.");
	}

	@Override
	public boolean containsAny(double[][] points, double[] radii, int[] layers, double[][] layerRadii) {
		throw new UnsupportedOperationException("Cannot invoke a Polygon3D method on a Polygon2D object.");
	}

}
