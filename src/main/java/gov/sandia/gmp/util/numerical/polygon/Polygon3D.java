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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.sandia.gmp.util.globals.Globals;

/**
 * An extension of Polygon.java that includes the ability to limit the radial extent of a
 * polygon.
 * 
 * @author sballar
 * 
 */
public class Polygon3D implements Polygon
{
	private Polygon2D polygon2d;

	private Horizon top;

	private Horizon bottom;

	private String polygonFile;

	private String name;
	
	private Object attachment;

	public Polygon3D()
	{
	}

	public Polygon3D(Polygon2D polygon2d, Horizon bottom, Horizon top)
			throws Exception
	{
		this.polygon2d = polygon2d;
		this.bottom = bottom;
		this.top = top;

		if (bottom.getLayerIndex() > top.getLayerIndex())
			throw new Exception(
					"Layer index of bottom horizon is greater than layer index of top horizon");
	}
	
	public Polygon3D(DataInputStream input) throws Exception
	{
		int format = input.readInt();
		if (format != 1)
			throw new IOException(format+" is not a recognized format.");
		
		String x = Globals.readString(input);
		if (x.equals("TOP"))
			top = Horizon.getHorizon(input);
		else if (x.equals("BOTTOM"))
			bottom = Horizon.getHorizon(input);
		
		x = Globals.readString(input);
		if (x.equals("TOP"))
			top = Horizon.getHorizon(input);
		else if (x.equals("BOTTOM"))
			bottom = Horizon.getHorizon(input);
		
		polygon2d = (Polygon2D) Polygon.getPolygon(input);
	}

	public Polygon3D(BufferedReader input) throws Exception 
	{
		input.mark(256);
		int format = -1;
		try {
			format = Integer.parseInt(input.readLine());
		} catch (NumberFormatException e) {
			format = 1;
			input.reset();
		}
		if (format != 1)
			throw new Exception(format+" is not a recognized format.");

		while (true)
		{
		    input.mark(256);
		    // read two lines and parse top and bottom boundaries which might be either order
		    String record = input.readLine().trim().toUpperCase();
		    if (record.startsWith("TOP"))
			top = Horizon.getHorizon(record);
		    else if (record.startsWith("BOTTOM"))
			bottom = Horizon.getHorizon(record);
		    else if (!record.startsWith("POLYGON3D"))
			break;
		}

		input.reset();
		polygon2d = (Polygon2D) Polygon.getPolygon(input);

	}

	public Polygon3D(File f) throws FileNotFoundException, Exception {
		this(new BufferedReader(new FileReader(f)));
	}

	/**
	 * Returns the Horizon object that defines the 'top' of the Polygon
	 * @return
	 */
	@Override
	public Horizon getTop() { return top; }

	/**
	 * Returns the Horizon object that defines the 'bottom' of the Polygon
	 * @return
	 */
	@Override
	public Horizon getBottom() { return bottom; }

	/**
	 * 
	 */
	public boolean contains(double[] x, double radius, int layer, double[] radii) {
		return (bottom.getLayerIndex() < 0 || layer >= bottom.getLayerIndex())
				&& (top.getLayerIndex() < 0 || layer <= top.getLayerIndex())
				&& radius > bottom.getRadius(x, radii) - 1e-4
				&& radius < top.getRadius(x, radii) + 1e-4 
				&& polygon2d.contains(x);
	}

	/**
	 * 
	 */
	public boolean contains(double[] x, int layer) {
		return (bottom.getLayerIndex() < 0 || layer >= bottom.getLayerIndex())
				&& (top.getLayerIndex() < 0 || layer <= top.getLayerIndex())
				&& polygon2d.contains(x);
	}

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
	 * @throws PolygonException
	 */
	public boolean containsAll(ArrayList<double[]> points,
			ArrayList<Double> radii, ArrayList<Integer> layers,
			ArrayList<double[]> layerRadii)
	{
		for (int i = 0; i < points.size(); ++i)
			if (!contains(points.get(i), radii.get(i), layers.get(i),
					layerRadii.get(i)))
				return false;
		return true;
	}

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
	 * @throws PolygonException
	 */
	public boolean containsAny(ArrayList<double[]> points,
			ArrayList<Double> radii, ArrayList<Integer> layers,
			ArrayList<double[]> layerRadii)
	{
		for (int i = 0; i < points.size(); ++i)
			if (contains(points.get(i), radii.get(i), layers.get(i),
					layerRadii.get(i)))
				return true;
		return false;
	}

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
	 * @throws PolygonException
	 */
	public boolean containsAll(double[][] points, double[] radii, int[] layers,
			double[][] layerRadii)
	{
		for (int i = 0; i < points.length; ++i)
			if (!contains(points[i], radii[i], layers[i], layerRadii[i]))
				return false;
		return true;
	}

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
	 * @throws PolygonException
	 */
	public boolean containsAny(double[][] points, double[] radii, int[] layers,
			double[][] layerRadii)
	{
		for (int i = 0; i < points.length; ++i)
			if (contains(points[i], radii[i], layers[i], layerRadii[i]))
				return true;
		return false;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer("Polygon3D\n");
		buf.append("TOP ").append(top.toString()).append("\n");
		buf.append("BOTTOM ").append(bottom.toString()).append("\n");
		buf.append(polygon2d.toString());
		return buf.toString();
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

	public void write(Writer output) throws Exception
	{
//		output.write("POLYGON3D\n");
//		output.write("TOP " + top.toString()+"\n");
//		output.write("BOTTOM " + bottom.toString()+"\n");
//		polygon2d.write(output);
	    output.write(toString());
	}

	public void write(DataOutputStream output) throws Exception
	{
		Globals.writeString(output, getClass().getSimpleName());
		output.writeInt(1);
		Globals.writeString(output, "TOP");
		top.write(output);
		Globals.writeString(output, "BOTTOM");
		bottom.write(output);
		polygon2d.write(output);
	}

	@Override
	public String getPolygonFile() {
		return polygonFile;
	}

	@Override
	public Polygon setPolygonFile(String f) { this.polygonFile = f; return this; }

	@Override
	public double[] getReferencePoint() {
		return polygon2d.referencePoint;
	}

	@Override
	public String refPt() {
		return polygon2d.refPt();
	}

	@Override
	public boolean getReferencePointIn() {
		return polygon2d.referenceIn;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Polygon3D setName(String name) { this.name = name; return this; }

	@Override
	public void invert() {
		polygon2d.invert();
	}

	@Override
	public void setReferencePoint(double[] referencePoint, boolean referenceIn) throws Exception {
		polygon2d.setReferencePoint(referencePoint, referenceIn);
	}

	@Override
	public void setReferencePoint(double lat, double lon, boolean referenceIn) throws Exception {
		polygon2d.setReferencePoint(lat, lon, referenceIn);
	}

	@Override
	public boolean contains(double[] x) {
		return polygon2d.contains(x);
	}

	@Override
	public boolean containsAny(double[]... points) {
		return polygon2d.containsAny(points);
	}

	@Override
	public boolean containsAny(Collection<double[]> points) {
		return polygon2d.containsAny(points);
	}

	@Override
	public boolean containsAll(double[]... points) {
		return polygon2d.containsAll(points);
	}

	@Override
	public ArrayList<Boolean> contains(Collection<double[]> points) {
		return polygon2d.contains(points);
	}

	@Override
	public boolean[] contains(double[]... points) {
		return polygon2d.contains(points);
	}

	@Override
	public boolean containsAll(Collection<double[]> points) {
		return polygon2d.containsAll(points);
	}

	@Override
	public boolean overlaps(Polygon other) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void writeBinary(File fileName) throws Exception {
		DataOutputStream output = new DataOutputStream(new FileOutputStream(fileName));
		output.writeLong(Polygon.magicKey);
		write(output);
		output.close();
	}

	@Override
	public void writeVTK(File fileName) throws Exception {
		polygon2d.writeVTK(fileName);
	}

	@Override
	public double[][][] getPoints(boolean repeatFirstPoint) {
		return polygon2d.getPoints(repeatFirstPoint);
	}

	@Override
	public double[][][] getPoints(boolean repeatFirstPoint, double maxSpacing) {
		return polygon2d.getPoints(repeatFirstPoint, maxSpacing);
	}

	@Override
	public double getArea() {
		return polygon2d.getArea();
	}

	@Override
	public int size() {
		return polygon2d.size();
	}

	@Override
	public ArrayList<Boolean> contains(List<double[]> points, int nProcessors) throws Exception {
		return polygon2d.contains(points, nProcessors);
	}

	@Override
	public HashMap<double[], Boolean> contains(Set<double[]> points, int nProcessors) throws Exception {
		return polygon2d.contains(points, nProcessors);
	}

	@Override
	public void contains(Map<double[], Boolean> points, int nProcessors) throws Exception {
		polygon2d.contains(points, nProcessors);
	}

	@Override
	public boolean onBoundary(double[] x) {
		return polygon2d.onBoundary(x);
	}

	public Polygon getPolygon2D() {
		return polygon2d;
	}

	private Integer hash;
	
	@Override
	public int hashCode() { if (hash == null) hash = toString().hashCode(); return hash; }
	
	@Override
	public boolean equals(Object other)	{
		return other != null && other.getClass().equals(this.getClass()) 
				&& other.toString().equals(this.toString());
	}

	@Override
	public Object getAttachment() {
		return attachment;
	}

	@Override
	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

}
