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
package gov.sandia.gmp.baseobjects.globals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.geovector.GeoVectorRay;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.mapprojection.RobinsonProjection;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.resampleray.ResampleRay;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

/**
 * RayPath represents a single ray path through the Earth.  It includes a list of 
 * GeoVectors that define the geographic locations of points on the ray path.
 * 
 * <p>There are also static methods to produce vtk files for maps and cross sections 
 * of collections of ray paths.
 * @author sballar
 *
 */
public class RayPath extends ArrayList<GeoVector> {

    /**
     * 
     */
    private static final long serialVersionUID = -8762490550657498463L;
    
    /**
     * the length of the path in km.  lazy evaluation is used.  The pathLength
     * is computed when requested.
     */
    private double pathLength;

    /**
     * Default constructor
     */
    public RayPath() {
    }

    /**
     * Default constructor that initializes the dimensions of the points 
     * @param capacity
     */
    public RayPath(int capacity) {
	super(capacity);
    }

    /**
     * 
     * @param geoVectors
     * @param radii
     */
    public RayPath(ArrayList<GeoVector> geoVectors) {
	super(geoVectors);
    }

    /**
     * @param points 3D vectors (not unit vectors)
     * @param earthShape which ellipsoid to use with these vectors.
     */
    public RayPath(List<double[]> vectors, EarthShape earthShape) {
	super(vectors.size());
	for (double[] v : vectors)
	    add(new GeoVector(v, earthShape));
    }

    /**
     * EarthShape.WGS84 is assumed.
     * @param vectors 3D vectors (not unit vectors)
     */
    public RayPath(List<double[]> vectors) {
	super(vectors.size());
	for (double[] v : vectors)
	    add(new GeoVector(v));
    }

    /**
     * EarthShape.WGS84 is assumed.
     * @param points unit vectors
     * @param radii radii in km. 
     */
    public RayPath(ArrayList<double[]> vectors, double[] radii) {
	super(vectors.size());
	for (int i=0; i<vectors.size(); ++i)
	    add(new GeoVector(vectors.get(i), radii[i]));
    }
    
    /**
     * 
     * @param points unit vectors
     * @param radii radii in km. 
     * @param earthShape
     */
    public RayPath(ArrayList<double[]> vectors, double[] radii, EarthShape earthShape) {
	super(vectors.size());
	for (int i=0; i<vectors.size(); ++i)
	    add(new GeoVector(vectors.get(i), radii[i], earthShape));
    }
    
    public RayPath(FileInputBuffer fib) throws IOException {
	this(fib, EarthShape.WGS84);
    }
    
    public RayPath(FileInputBuffer fib, EarthShape earthShape) throws IOException
    {
	super();
	int n = fib.readInt();
	ensureCapacity(n);
	for (int i=0; i<n; ++i)
	    add(new GeoVector(new double[] {fib.readDouble(), fib.readDouble(), fib.readDouble()}, 
		    fib.readDouble(), earthShape));
	
    }

    /**
     * Find the radius of the deepest point on the ray.
     * @return
     */
    public double getTurningRadius() {
	if (isEmpty())
	    return Double.NaN;

	double rmin = get(0).getRadius();
	for (int i=1; i<size(); ++i)
	    rmin = Math.min(rmin, get(i).getRadius());
	return rmin;
    }

    /**
     * Find the depth of the deepest point on the ray.
     * @return
     */
    public double getTurningDepth() {
	if (isEmpty())
	    return Double.NaN;

	double zmax = get(0).getDepth();
	for (int i=1; i<size(); ++i)
	    zmax = Math.max(zmax, get(i).getDepth());
	return zmax;
    }

    /**
     * Add a point and a radius to the end of the lists of points and radii.
     * @param point
     * @param radius
     */
    public void addPoint(double[] point, double radius) {
	add(new GeoVector(point, radius)); 
    }

    /**
     * Add a point and a radius to the end of the lists of points and radii.
     * @param point
     * @param radius
     */
    public void addPoint(double[] point, double radius, EarthShape earthShape) {
	add(new GeoVector(point, radius, earthShape)); 
    }

    /**
     * Replace the current list of points
     * @param points
     */
    public void setPoints(ArrayList<GeoVector> points) {
	clear();
	addAll(points);
    }

    /**
     * Retrieve a list of references to unit vectors.
     * @return
     */
    public ArrayList<double[]> getUnitVectors() {
	ArrayList<double[]> vectors = new ArrayList<>(size());
	for (GeoVector pt : this)
	    vectors.add(pt.getUnitVector());
	return vectors;
    }

    /**
     * Retrieve a list of new 3D vectors (not unit vectors).
     * @return
     */
    public ArrayList<double[]> getVectors() {
	ArrayList<double[]> vectors = new ArrayList<>(size());
	for (GeoVector pt : this)
	    vectors.add(pt.getVector());
	return vectors;
    }

    /**
     * Retrieve an array of all the radii.
     * @return
     */
    public double[] getRadii() {
	double[] radii = new double[size()];
	for (int i=0; i<size(); ++i)
	    radii[i] = get(i).getRadius();
	return radii;
    }

    /**
     * Retrieve an array of all the depths.
     * @return
     */
    public double[] getDepths() {
	double[] depths = new double[size()];
	for (int i=0; i<size(); ++i)
	    depths[i] = get(i).getDepth();
	return depths;
    }


    /**
     * Resample the ray so that the separation of points is equal to dkm.
     * The 
     * separation of points is measured in 3D, not along the surface of the earth.
     * @param dkm maximum separation of adjacent points, in km.
     * @throws Exception 
     */
    public void resample(double dkm) throws Exception
    {
	EarthShape earthShape = isEmpty() ? EarthShape.WGS84 : get(0).getEarthShape();
	
	ArrayList<double[]> pts = new ArrayList<double[]>(size());
	for (GeoVector point : this)
	    pts.add(point.getVector());

	pts = ResampleRay.resample(pts, dkm, false);
	clear();
	for (double[] pt : pts)
	    add(new GeoVector(pt, earthShape));
    }

    public void write(FileOutputBuffer fob) throws IOException
    {
	fob.writeInt(size());
	for (GeoVector v : this)
	    v.write(fob);
    }
    
    @Override
    public String toString() {
	StringBuffer out = new StringBuffer();
	for (GeoVector v : this)
	    out.append(v.toString()+"\n");
	return out.toString();
    }

    public static void toVTKSphere(Collection<RayPath> paths, File outputFile) throws Exception
    {
	List<double[]> points = new ArrayList<double[]>();
	List<VTKCell> cells = new ArrayList<>(paths.size());
	int n = 0;
	for (RayPath path : paths)
	{	
	    for (GeoVector point : path)
		points.add(point.getUnitVector());
	    int[] indices = new int[path.size()];
	    for (int i=0; i<indices.length; ++i)
		indices[i] = n++;
	    cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, indices));
	}
	VTKDataSet.write(outputFile, points, cells);
    }

    public static void toVTKRobinson(Collection<RayPath> paths, double centerLon, File outputFile) throws Exception
    {
	RobinsonProjection rob = new RobinsonProjection(centerLon);

	List<double[]> points = new ArrayList<double[]>();
	List<VTKCell> cells = new ArrayList<>(paths.size());
	int n = 0;
	for (RayPath path : paths)
	    for (ArrayList<double[]> p : rob.project(path.getUnitVectors()))
	    {
		for (double[] x : p)
		    points.add(new double[] { x[0], x[1], 0.});
		int[] indices = new int[p.size()];
		for (int i=0; i<p.size(); ++i)
		    indices[i] = n++;
		cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, indices));
	    }
	VTKDataSet.write(outputFile, points, cells);

    }

    public static void toVTKSlice(Collection<RayPath> paths, GreatCircle gc, File outputFile) throws Exception
    {
	int count = 0;
	for (RayPath path : paths)
	    count += path.size();

	List<double[]> points = new ArrayList<double[]>(count);
	List<VTKCell> cells = new ArrayList<>(paths.size());
	int n = 0;
	for (RayPath path : paths)
	{	
	    int[] indices = new int[path.size()];
	    for (int i=0; i < path.size(); ++i)
	    {
		points.add(gc.transform(path.get(i).getVector()));
		indices[i] = n++;
	    }
	    cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, indices));
	}
	VTKDataSet.write(outputFile, points, cells);
    }

    public static void toVTKSlice(Collection<RayPath> paths, File outputFile) throws Exception
    {
	int count = 0;
	for (RayPath path : paths)
	    count += path.size();

	List<double[]> points = new ArrayList<double[]>(count);
	List<VTKCell> cells = new ArrayList<>(paths.size());
	int n = 0;
	for (RayPath path : paths)
	{	
	    int[] indices = new int[path.size()];
	    for (int i=0; i < path.size(); ++i)
	    {
		points.add(new double[] {path.get(0).getDistanceKm(path.get(i)), -path.get(i).getDepth(), 0.});
		indices[i] = n++;
	    }
	    cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, indices));
	}
	VTKDataSet.write(outputFile, points, cells);
    }

    public void toVTKSphere(File outputFile) throws Exception {
	ArrayList<RayPath> a = new ArrayList<RayPath>();
	a.add(this);
	toVTKSphere(a, outputFile);
    }

    public void toVTKRobinson(double centerLon, File outputFile) throws Exception {
	ArrayList<RayPath> a = new ArrayList<RayPath>();
	a.add(this);
	toVTKRobinson(a, centerLon, outputFile);
    }

    public void toVTKSlice(GreatCircle gc, File outputFile) throws Exception {
	ArrayList<RayPath> a = new ArrayList<RayPath>();
	a.add(this);
	toVTKSlice(a, gc, outputFile);
    }

    public void toVTKSlice(File file) throws Exception {
	ArrayList<RayPath> a = new ArrayList<RayPath>();
	a.add(this);
	toVTKSlice(file);
    }

    public double getPathLength() {
	if (pathLength == 0.)
	    for (int i=1; i<size(); ++i)
		pathLength += get(i-1).distance3D(get(i));
	return pathLength;
	
    }

    /**
     * 
     * @param waveType either WaveType.P or WaveType.S
     * @return
     */
    public double getPathLength(WaveType waveType) {
	double pl = 0;
	WaveType ntype, ptype = ((GeoVectorRay) get(0)).getWaveType();
	for (int i=1; i<size(); ++i)
	{
	    ntype = ((GeoVectorRay) get(i)).getWaveType();
	    if (ntype == ptype && ntype == waveType)
		pl += get(i-1).distance3D(get(i));
	    ptype = ntype;
	}
	return pl;

    }

}
