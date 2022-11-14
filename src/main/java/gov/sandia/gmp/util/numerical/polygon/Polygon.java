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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.sandia.gmp.util.globals.Globals;

/**
 * This Polygon Interface is implemented by two derived classes: Polygon2D and Polygon3D.
 * This interface provides a number of static methods for reading and writing Polygon2D and 
 * Polygon3D objects.  They can be read/written to ascii files, binary files, kml/kmz files 
 * and vtk files. 
 * @author sballar
 *
 */
public interface Polygon {

	/**
	 * Retrieve a single Polygon from an ascii file.  If the extension is kml or kmz 
	 * (case insensitive) then a single polygon is loaded from a Google Earth file.
	 * @param inputFile
	 * @return
	 * @throws Exception
	 */
	static Polygon getPolygon(File inputFile) throws Exception {
		String ext = getFileExt(inputFile).toLowerCase();
		if (ext.equals("kml") || ext.equals("kmz"))
		{
			List<Polygon> polygons = PolygonKMLZ.readKMLZ(inputFile);
			if (polygons.size() == 1)
				return polygons.get(0);
			else
				throw new Exception("The File contains multiple Polygons.  Call Polygon.getPolygonList(File) to get them all.");
		}
		DataInputStream input = getDataInputStream(inputFile);
		Polygon polygon = null;
		if (input != null)
		{
			polygon = getPolygon(input)
					.setPolygonFile(inputFile.getCanonicalPath())
					.setName(extractName(inputFile));
			input.close();
		}
		else
			polygon = getPolygon(new BufferedReader(new FileReader(inputFile)))
			.setPolygonFile(inputFile.getCanonicalPath())
			.setName(extractName(inputFile));
		return polygon;
	}

	/**
	 * Read multiple Polygons from a file and return them in a List.
	 * If the file has extension 'kml' or 'kmz' then this method will attempt
	 * to read them in Google Earth format.
	 * @param inputFile
	 * @return
	 * @throws Exception
	 */
	static List<Polygon> getPolygonList(File inputFile) throws Exception {
		List<Polygon> list;
		String ext = getFileExt(inputFile).toLowerCase();
		if (ext.equals("kml") || ext.equals("kmz"))
			return PolygonKMLZ.readKMLZ(inputFile);

		DataInputStream input = getDataInputStream(inputFile);
		if (input != null)
		{
			list = getPolygonList(input);
			input.close();
		}
		else
			list = getPolygonList(new BufferedReader(new FileReader( inputFile)));

		for (Polygon p : list) p.setPolygonFile(inputFile.getCanonicalPath()).setName(extractName(inputFile));
		return list;
	}

	static void writeList(File outputFile, List<Polygon> polygons) throws Exception {
		String ext = getFileExt(outputFile).toLowerCase();
		if (ext.equals("kml") || ext.equals("kmz"))
			PolygonKMLZ.writeKMLZ(outputFile, polygons);
		else
		{
			BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
			for (Polygon polygon : polygons)
				polygon.write(output);
			output.close();
		}
	}

	static void writeListBinary(File outputFile, List<Polygon> polygons) throws Exception {
		String ext = getFileExt(outputFile).toLowerCase();
		if (ext.equals("kml") || ext.equals("kmz"))
			PolygonKMLZ.writeKMLZ(outputFile, polygons);
		else
		{
			DataOutputStream output = new DataOutputStream(new FileOutputStream(outputFile));
			output.writeLong(magicKey);
			for (Polygon polygon : polygons)
				polygon.write(output);
			output.close();
		}
	}
	
	static void write(File outputFile, Polygon...polygons) throws Exception {
		writeList(outputFile, Arrays.asList(polygons));
	}

	static void writeBinary(File outputFile, Polygon...polygons) throws Exception {
		writeListBinary(outputFile, Arrays.asList(polygons));
	}

	/**
	 * Static method that reads the class name from the input stream and then
	 * constructs a Polygon of that type and returns it. Recognized types include
	 * PolygonGlobal, PolygonSmallCircles, PolygonPoints and Polygon3D
	 * @param input
	 * @return one of the derived Polygon classes
	 * @throws Exception
	 */
	static Polygon getPolygon(DataInputStream input) throws EOFException, Exception {
		String type = Globals.readString(input);
		if (type.equals("PolygonGlobal"))
			return new PolygonGlobal(input);
		if (type.equals("PolygonSmallCircles"))
			return new PolygonSmallCircles(input);
		if (type.equals("PolygonPoints"))
			return new PolygonPoints(input);
		if (type.equals("Polygon3D"))
			return new Polygon3D(input);
		throw new Exception(type+" is not a recognized Polygon class.  "
				+ "Must be one of PolygonPoints, PolygonSmallCircles, PolygonGlobal, Polygon3D");
	}

	/**
	 * Static method that reads the class name from the input stream and then
	 * constructs a Polygon of that type and returns it. Recognized types include
	 * PolygonGlobal, PolygonSmallCircles, PolygonPoints and Polygon3D
	 * @param input
	 * @return one of the derived Polygon classes
	 * @throws Exception
	 */
	static Polygon getPolygon(BufferedReader buffer) throws EOFException, Exception {

		// read the firstLine of the input buffer and then put it back in the buffer.
		buffer.mark(1000);
		String firstLine = buffer.readLine();

		// if the first line is null, throw enf-of-file exception
		if (firstLine == null)
			throw new EOFException();
		firstLine = firstLine.trim().toUpperCase();

		if (firstLine.startsWith("POLYGON3D"))
			return new Polygon3D(buffer);
		if (firstLine.startsWith("POLYGONPOINTS"))  
			return new PolygonPoints(buffer);
		if (firstLine.startsWith("POLYGONSMALLCIRCLES")) 
			return new PolygonSmallCircles(buffer);
		if (firstLine.startsWith("POLYGONGLOBAL")) 
			return new PolygonGlobal(buffer);
		if (firstLine.startsWith("POLYGON2D")) {
			// have to deal with files written by legacy code.
			if (firstLine.contains("GLOBAL"))
				return new PolygonGlobal(buffer);
			else
				return new PolygonPoints(buffer);
		}
		else {
			// have to deal with files written by legacy code.
			buffer.reset();
			if (firstLine.toLowerCase().contains("global"))
				return new PolygonGlobal(buffer);
			else
				return new PolygonPoints(buffer);
		}		
	}

	/**
	 * Read multiple Polygons from a BufferedReader
	 * @param input
	 * @return
	 * @throws Exception
	 */
	static List<Polygon> getPolygonList(BufferedReader input) throws Exception {
		List<Polygon> polygons = new ArrayList<>();
		while (true) {
			try { 
				polygons.add(getPolygon(input)); 
			} 
			catch (EOFException e) {
				break;
			}	
		}
		return polygons;
	}

	/**
	 * Read multiple Polygons from a DataInputStream
	 * @param input
	 * @return
	 * @throws Exception
	 */
	static List<Polygon> getPolygonList(DataInputStream input) throws Exception {
		List<Polygon> polygons = new ArrayList<>();
		while (true) {
			try { 
				polygons.add(getPolygon(input)); 
			} 
			catch (EOFException e) {
				break;
			}	
		}
		return polygons;
	}

	/**
	 * Parses the input String to extract a Polygon.
	 * @param input
	 * @return
	 * @throws Exception
	 */
	static Polygon getPolygon(String input) throws Exception {
		return getPolygon(new BufferedReader(new StringReader(input)));
	}

	/**
	 * Parses the input String to extract multiple Polygons.
	 * @param input
	 * @return
	 * @throws Exception
	 */
	static List<Polygon> getPolygonList(String input) throws Exception {
		return getPolygonList(new BufferedReader(new StringReader(input)));
	}

	/**
	 * Retrieve a reference to the referencePoint.
	 * 
	 * @return a reference to the referencePoint. 
	 */
	double[] getReferencePoint();

	boolean getReferencePointIn();

	/**
	 * Returns a String similar to: "referencePoint lat lon in|out"
	 */
	String refPt();

	/**
	 * If the polygon was read from, or written to, a file, return 
	 * the cannonical name of the file.
	 * 
	 * @return Name of polygon File
	 */
	String getPolygonFile();

	/**
	 * Set polygonFile name and return a reference to 'this'
	 * @param polygonFile
	 * @return
	 */
	Polygon setPolygonFile(String polygonFile);

	/**
	 * Retrieve the name of this Polygon
	 * @return
	 */
	String getName();

	/**
	 * Set the name of this polygon and return a reference to 'this'
	 * @param name
	 * @return
	 */
	Polygon setName(String name);

	/**
	 * Invert the current polygon. What used to be in will be out and what used to be out
	 * will be in.
	 */
	void invert();

	/**
	 * Specify the reference point for this polygon and whether or not the specified point
	 * is inside or outside the polygon.
	 * 
	 * @param referencePoint
	 * @param referenceIn
	 * @throws Exception 
	 */
	void setReferencePoint(double[] referencePoint, boolean referenceIn) throws Exception;

	/**
	 * Specify the reference point for this polygon and whether or not the specified point
	 * is inside or outside the polygon.
	 * 
	 * @param lat geographic latitude in degrees
	 * @param lon longitude in degrees
	 * @param referenceIn
	 * @throws Exception 
	 */
	void setReferencePoint(double lat, double lon, boolean referenceIn) throws Exception;

	/**
	 * return true if point x is located inside the polygon
	 * @param x the point to be evaluated (unit vector)
	 * @return true if point x is located inside the polygon
	 */
	boolean contains(double[] x);

	/**
	 * Returns true if this Polygon contains any of the supplied unit vectors
	 * 
	 * @param points
	 *            array of unit vectors
	 * @return true if this Polygon contains any of the supplied unit vectors
	 * @throws Exception
	 */
	boolean containsAny(double[]... points);

	/**
	 * Returns true if this Polygon contains any of the supplied unit vectors
	 * 
	 * @param points Collection of unit vectors
	 * @return true if this Polygon contains any of the supplied unit vectors
	 * @throws Exception
	 */
	boolean containsAny(Collection<double[]> points);

	/**
	 * Returns true if this Polygon contains all of the supplied unit vectors
	 * 
	 * @param points
	 *            double[][3], the unit vectors to check
	 * @return true if this Polygon contains all of the supplied unit vectors
	 * @throws Exception
	 */
	boolean containsAll(double[]... points);

	/**
	 * Returns boolean for each point contained within this Polygon
	 * 
	 * @param points Collection of unit vectors to check
	 * @return boolean for each point contained within this Polygon
	 * @throws Exception
	 */
	ArrayList<Boolean> contains(Collection<double[]> points);

	/**
	 * Returns boolean for each point contained within this Polygon
	 * 
	 * @param points Collection of unit vectors to check
	 * @return boolean for each point contained within this Polygon
	 * @throws Exception
	 */
	boolean[] contains(double[]... points);

	/**
	 * Returns true if this Polygon contains all of the supplied unit vectors
	 * 
	 * @param points Collection of unit vectors to check
	 * @return true if this Polygon contains all of the supplied unit vectors
	 * @throws Exception
	 */
	boolean containsAll(Collection<double[]> points);

	/**
	 * Returns true if this Polygon and some other Polygon overlap.
	 * Note that it is possible for two polygons to overlap even when
	 * neither Polygon contains any of the points that define the other
	 * Polygon.
	 * @param other
	 * @return
	 * @throws Exception 
	 */
	boolean overlaps(Polygon other) throws Exception;

	/**
	 * Write the current polygon to a file.
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
	void write(File fileName) throws Exception;

	/**
	 * Write the current polygon to a file in binary format.
	 * @param fileName
	 * @throws Exception
	 */
	void writeBinary(File fileName) throws Exception;

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
	void write(Writer output) throws Exception;

	void write(DataOutputStream output) throws Exception;

	void writeVTK(File fileName) throws Exception;

	/**
	 * Retrieve a deep copy of the points on the polygon.
	 * 
	 * @param repeatFirstPoint
	 *            if true, last point will be reference to the same point as the first
	 *            point.
	 * @return a deep copy of the points on the polygon.
	 */
	double[][][] getPoints(boolean repeatFirstPoint);

	/**
	 * Retrieve a deep copy of the points on the polygon.
	 * 
	 * @param repeatFirstPoint
	 *            if true, last point will be reference to the same point as the first
	 *            point.
	 * @param maxSpacing max distance between points, in radians
	 * @return a deep copy of the points on the polygon.
	 */
	double[][][] getPoints(boolean repeatFirstPoint, double maxSpacing);

	/**
	 * Retrieve the area of this polygon. This is the unitless area (radians squared). 
	 * To convert to km^2, multiply the result by R^2 where R is the radius of the sphere.
	 * The range is zero to 4*PI.
	 * @return
	 */
	double getArea();

	/**
	 * Returns the number of edges that define the polygon. Equals the number of unique
	 * GeoVectors that define the polygon.
	 * 
	 * @return the number of edges that define the polygon. Equals the number of unique
	 *         GeoVectors that define the polygon.
	 */
	int size();

	void setAttachment(Object attachment);

	Object getAttachment();

	/**
	 * Determine if a list of unit vectors is contained within this polygon. 
	 * Concurrency is used to process batches of points in parallel.
	 * @param points list of points to evaluate
	 * @param nProcessors  number of processors to use in concurrent mode.  
	 * If nProcessors < 2, points are evaluated in sequential mode.
	 * @return ArrayList<Boolean> of length equal to input points.
	 * @throws Exception
	 */
	ArrayList<Boolean> contains(List<double[]> points, int nProcessors) throws Exception;

	/**
	 * Determine if a set of unit vectors is contained within this polygon. 
	 * Concurrency is used to process batches of points in parallel.
	 * @param points set of points to evaluate
	 * @param nProcessors  number of processors to use in concurrent mode.  
	 * If nProcessors < 2, points are evaluated in sequential mode.
	 * @return HashMap<double[], Boolean> of length equal to input points.
	 * @throws Exception
	 */
	HashMap<double[], Boolean> contains(Set<double[]> points, int nProcessors) throws Exception;

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
	void contains(Map<double[], Boolean> points, int nProcessors) throws Exception;

	/**
	 * If this is a Polygon3D, returns the Horizon object that defines the 'top' 
	 * of the Polygon
	 * @return
	 * @throws UnsupportedOperationException if this is not a Polygon3D
	 */
	Horizon getTop();

	/**
	 * If this is a Polygon3D, returns the Horizon object that defines the 'top' 
	 * of the Polygon
	 * @return
	 * @throws UnsupportedOperationException if this is not a Polygon3D
	 */
	Horizon getBottom();

	/**
	 * 
	 * @param x
	 * @param radius
	 * @param layer
	 * @param radii
	 * @throws UnsupportedOperationException if this is not a Polygon3D
	 * @return
	 */
	boolean contains(double[] x, double radius, int layer, double[] radii);

	/**
	 * 
	 * @param x
	 * @param layer
	 * @return
	 * @throws UnsupportedOperationException if this is not a Polygon3D
	 */
	boolean contains(double[] x, int layer);

	/**
	 * Returns true if this Polygon contains all of the supplied points
	 * 
	 * @param points array of unit vectors
	 * @param radii the radii of the positions of the points.  Array must have
	 * same number of elements as points.
	 * @param layers the layer index of each point.
	 * @param layerRadii the radii of the interfaces that define the layers 
	 * at each point.  This is an nPoints by nLayers+1 array, where nPoints
	 * is the number of points in the points array, and nLayers is the number
	 * of layers in the model.
	 * @return true if this Polygon contains all of the supplied points
	 * @throws UnsupportedOperationException if this is not a Polygon3D
	 */
	boolean containsAll(ArrayList<double[]> points,
			ArrayList<Double> radii, ArrayList<Integer> layers,
			ArrayList<double[]> layerRadii);

	/**
	 * Returns true if this Polygon contains any of the supplied points
	 * 
	 * @param points array of unit vectors
	 * @param radii the radii of the positions of the points.  Array must have
	 * same number of elements as points.
	 * @param layers the layer index of each point.
	 * @param layerRadii the radii of the interfaces that define the layers 
	 * at each point.  This is an nPoints by nLayers+1 array, where nPoints
	 * is the number of points in the points array, and nLayers is the number
	 * of layers in the model.
	 * @return true if this Polygon contains any of the supplied points
	 * @throws UnsupportedOperationException if this is not a Polygon3D
	 */
	boolean containsAny(ArrayList<double[]> points,
			ArrayList<Double> radii, ArrayList<Integer> layers,
			ArrayList<double[]> layerRadii);

	/**
	 * Returns true if this Polygon contains all of the supplied points
	 * 
	 * @param points array of unit vectors
	 * @param radii the radii of the positions of the points.  Array must have
	 * same number of elements as points.
	 * @param layers the layer index of each point.
	 * @param layerRadii the radii of the interfaces that define the layers 
	 * at each point.  This is an nPoints by nLayers+1 array, where nPoints
	 * is the number of points in the points array, and nLayers is the number
	 * of layers in the model.
	 * @return true if this Polygon contains all of the supplied points
	 * @throws UnsupportedOperationException if this is not a Polygon3D
	 */
	boolean containsAll(double[][] points, double[] radii, int[] layers,
			double[][] layerRadii);

	/**
	 * Returns true if this Polygon3D contains any of the supplied points
	 * 
	 * @param points array of unit vectors
	 * @param radii the radii of the positions of the points.  Array must have
	 * same number of elements as points.
	 * @param layers the layer index of each point.
	 * @param layerRadii the radii of the interfaces that define the layers 
	 * at each point.  This is an nPoints by nLayers+1 array, where nPoints
	 * is the number of points in the points array, and nLayers is the number
	 * of layers in the model.
	 * @return true if this Polygon contains any of the supplied points
	 * @throws UnsupportedOperationException if this is not a Polygon3D
	 */
	boolean containsAny(double[][] points, double[] radii, int[] layers,
			double[][] layerRadii);

	boolean onBoundary(double[] u);

	/**
	 * This long value is the first thing written in a binary file when Polygon(s)
	 * are written to a binary file.
	 */
	final static long magicKey = 9090056403021603882L;

	/**
	 * Open a DataInputStream for the specified file and read a long value from the stream.
	 * If the long is equal to magicKey, return the DataInputStream, otherwise null.
	 * @param f a File
	 * @return
	 * @throws IOException
	 */
	private static DataInputStream getDataInputStream(File f) throws IOException
	{
		DataInputStream dis = new DataInputStream(new FileInputStream(f));
		long key = dis.readLong();
		if (key == magicKey)
			return dis;
		dis.close();
		return null;
	}

	/**
	 * Retrieve the name of the file without the extension, if any.
	 * @param file
	 * @return
	 */
	private static String extractName(File file) {
		int idx = file.getName().lastIndexOf('.');
		return idx > 0 ? file.getName().substring(0, idx) : file.getName();
	}

	private static String getFileExt(File f) {
		int idx = f.getName().lastIndexOf('.');
		return idx > 0 ? f.getName().substring(idx+1, f.getName().length()) : "";
	}

}
