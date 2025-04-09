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
package gov.sandia.gmp.surfacewavepredictor;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import gov.sandia.geotess.GeoTessModelUtils;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.mapprojection.RobinsonProjection;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.polygon.SmallCircle;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

public class SurfaceWaveModel {

	public static final double[] northPole = new double[] {0,0,1};
	public static final double TWO_PI = 2*PI;

	protected final int nlat;
	protected final int nlon;
	protected final int nperiods;

	/**
	 * Grid spacing in radians in both colatitude and longitude dimensions.
	 */
	protected final double spacing;

	/**
	 * Array of period values loaded from the velocity file.
	 */
	protected double[] periods;

	/**
	 * Cell based velocity values in km/sec as a function of geocentric colatitude, longitude and period.
	 */
	protected double[][][] velocities;

	protected final SeismicPhase phase; 

	/**
	 * Great circles that correspond to the meridians in the model grid.  Each great circle 
	 * corresponds to two meridians located 180 degrees apart. There will be nlon/2 of them.
	 * They go all the way around the earth, with distance = TWO_PI.  
	 * Their normals all reside in the equatorial plane.
	 */
	protected GreatCircle[] meridians;

	/**
	 * Nlat small circles centered at the north pole.  They are spaced spacing apart in geocentric coordinates.
	 * The first one has radius zero and is never used.  The last one is at (PI-spacing) from the north pole.
	 */
	protected SmallCircle[] parallels;

	/**
	 * Load a surface wave model.
	 * @param directory the directory where the model files are stored.
	 * @param phase either 'LR' or 'LQ'
	 * @throws Exception
	 */
	public SurfaceWaveModel(File directory, SeismicPhase phase) throws Exception {
		this.phase = phase;

		Scanner input = new Scanner(loadFile(new File(directory, "LP_grid."+phase.name())));
		nlat = input.nextInt();
		nlon = input.nextInt();

		double spacingDegrees = input.nextDouble();
		
		if ((180. % spacingDegrees) > 1e-6)
			throw new Exception("Assumption that 180 is evenly divisible by spacing is violated.  spacing) = "+toDegrees(spacingDegrees));

		spacing = toRadians(spacingDegrees);

		int[][] index = new int[nlat][nlon];
		for (int i=0; i<nlat; ++i) {
			int[] idx = index[i];
			for (int j=0; j<nlon; ++j)
				idx[j] = input.nextInt();
		}
		input.close();

		input = new Scanner(loadFile(new File(directory, "LP_vel."+phase)));

		int n_indexes = input.nextInt();
		nperiods = input.nextInt();

		periods = new double[nperiods];
		for (int i=0; i<nperiods; ++i)
			periods[i] = input.nextDouble();

		double[][] vel = new double[n_indexes][nperiods];
		for (int i=0; i<n_indexes; ++i)
			for (int j=0; j<nperiods; ++j)
				vel[i][j] = input.nextDouble();
		input.close();

		velocities = new double[nlat][nlon][];
		for (int i=0; i<nlat; ++i)
			for (int j=0; j<nlon; ++j) 
				velocities[i][j] = vel[index[i][j]];

		// instantiate a great circle object along half the meridians of the grid. 
		// Each great circle really incorporates two meridians which are 180 degrees apart.
		// These meridians go all the way around the earth and have distance = 2*PI.
		// Their normals reside in the equatorial plane.
		meridians = new GreatCircle[nlon/2];
		for (int j=0; j<nlon/2; ++j) 
			meridians[j] = new GreatCircle(EarthShape.SPHERE.getVector(0., j*spacing-PI/2));

		// instantiate nlat small circles centered on north pole with radius = i*spacing.
		parallels = new SmallCircle[nlat];
		for (int i=0; i<nlat; ++i) 
			parallels[i] = new SmallCircle(northPole, true, i*spacing);

	}

	/**
	 * Compute the path integral (travel time in seconds) from lat1, lon1 in degrees to lat2, lon2 in degrees using phase velocities 
	 * at the specified period.
	 * @param lat1 in degrees
	 * @param lon1 in degrees
	 * @param lat2 in degrees
	 * @param lon2 in degrees
	 * @param period in seconds
	 * @return travel time of the ray path in seconds
	 * @throws Exception
	 */
	public double pathIntegral(double lat1, double lon1, double lat2, double lon2, double period) throws Exception {
		return pathIntegral(new GreatCircle(VectorGeo.getVectorDegrees(lat1, lon1), 
				VectorGeo.getVectorDegrees(lat2, lon2)), new double[] {period}, null, null)[0];
	}

	/**
	 * Compute the path integral (travel time in seconds) from point1 to point2 using phase velocities 
	 * at the specified period.
	 * @param point1 earth-centered unit vector 
	 * @param point2 earth-centered unit vector
	 * @param period in seconds
	 * @return travel time of the ray path in seconds
	 * @throws Exception
	 */
	public double pathIntegral(double[] point1, double[] point2, double period) throws Exception {
		return pathIntegral(new GreatCircle(point1, point2), new double[] {period}, null, null)[0];
	}

	/**
	 * Compute the path integral (travel time in seconds) for the great circle using phase velocities 
	 * at the specified period.
	 * @param path 
	 * @param period in seconds
	 * @return travel time of the ray path in seconds
	 * @throws Exception
	 */
	public double  pathIntegral(GreatCircle path, double period) throws Exception {
		return pathIntegral(path, new double[] {period}, null, null)[0];
	}

	/**
	 * Compute the path integral (travel time in seconds) for the great circle using phase velocities 
	 * at the specified period.
	 * @param path 
	 * @param period in seconds
	 * @param points (optional) if this array is not null it will be cleared and populated with the points along
	 * the ray path that were used to compute the path integral.  These points will reside 
	 * along either the colatitudes or the longitudes of the grid.
	 * @param pathVelocities (optional) if this array is not null then it will be cleared and populated with the phase
	 * velocities used to compute the path integral.  The phase velocity values are interpolated
	 * at the center of each path interval.  pathVelocities.size() will equal points.size()-1.
	 * @return travel time of the ray path in seconds
	 * @throws Exception
	 */
	public double pathIntegral(GreatCircle path, double period, ArrayList<double[]> points,
			ArrayList<double[]> pathVelocities) throws Exception {
		return pathIntegral(path, new double[] {period}, points, pathVelocities)[0];
	}

	/**
	 * Compute the path integral (travel time in seconds) from lat1, lon1 in degrees to lat2, lon2 in degrees using phase velocities 
	 * at the specified period.
	 * @param lat1 in degrees
	 * @param lon1 in degrees
	 * @param lat2 in degrees
	 * @param lon2 in degrees
	 * @param period in seconds
	 * @return travel time of the ray path in seconds
	 * @throws Exception
	 */
	public double[] pathIntegral(double lat1, double lon1, double lat2, double lon2, double[] periods) throws Exception {
		return pathIntegral(new GreatCircle(VectorGeo.getVectorDegrees(lat1, lon1), 
				VectorGeo.getVectorDegrees(lat2, lon2)), periods, null, null);
	}

	/**
	 * Compute the path integral (travel time in seconds) from point1 to point2 using phase velocities 
	 * at the specified period.
	 * @param point1 earth-centered unit vector 
	 * @param point2 earth-centered unit vector
	 * @param period in seconds
	 * @return travel time of the ray path in seconds
	 * @throws Exception
	 */
	public double[] pathIntegral(double[] point1, double[] point2, double periods[]) throws Exception {
		return pathIntegral(new GreatCircle(point1, point2), periods, null, null);
	}

	/**
	 * Compute the path integral (travel time in seconds) for the great circle using phase velocities 
	 * at the specified period.
	 * @param path 
	 * @param period in seconds
	 * @return travel time of the ray path in seconds
	 * @throws Exception
	 */
	public double[] pathIntegral(GreatCircle path, double[] periods) throws Exception {
		return pathIntegral(path, periods, null, null);
	}

	/**
	 * Compute the path integral (travel time in seconds) for the great circle using phase velocities 
	 * at the specified period.
	 * @param path 
	 * @param periods in seconds
	 * @param points (optional) if this array is not null it will be cleared and populated with the points along
	 * the ray path that were used to compute the path integral.  These points will reside 
	 * along either the colatitudes or the longitudes of the grid.
	 * @param pathVelocities (optional) if this array is not null then it will be cleared and populated with the phase
	 * velocities used to compute the path integral.  The phase velocity values are interpolated
	 * at the center of each path interval.  pathVelocities.size() will equal points.size()-1.
	 * @return travel time of the ray path in seconds
	 * @throws Exception
	 */
	public double[] pathIntegral(GreatCircle path, double[] periods, ArrayList<double[]> points,
			ArrayList<double[]> pathVelocities) throws Exception {

		Set<Point> intersections = new TreeSet<>();
		for (double[] point : getIntersections(path))
			intersections.add(new Point(point, path.getDistance(point)));

		// compute interpolation coefficients based on period array that can be used to interpolate
		// velocity values.
		VelocityInterpolator[] velocityInterpolators = new VelocityInterpolator[periods.length];
		for (int i=0; i<velocityInterpolators.length; ++i)
			velocityInterpolators[i] = new VelocityInterpolator(this, periods[i]);

		// if caller provided arrays in which to put path points and velocity values, initialize them.  optional.
		if (points != null) {
			points.clear();
			points.ensureCapacity(intersections.size());
			points.add(path.getFirst());
		}
		if (pathVelocities != null) {
			pathVelocities.clear();
			pathVelocities.ensureCapacity(intersections.size());
		}

		double[] travelTimes = new double[periods.length];

		Iterator<Point> it = intersections.iterator();
		Point next, previous = it.next();
		double[] midpoint = new double[3];

		// iterate over all the intersection Points, which are now in order 
		// (increasing with distance from path.getFirst()
		while (it.hasNext()) {

			next = it.next();

			midpoint = VectorUnit.center(previous.point, next.point);

			// get the angular separation of previous and next in radians, multiplied by the radius of the Earth
			// at the latitude of the midpoint, in km.
			double dx = (next.distance-previous.distance) *  VectorGeo.getEarthRadius(midpoint);

			double[] v = new double[periods.length];

			// iterate over all the periods requested by caller.
			for (int i=0; i<periods.length; ++i) {
				// interpolate a value of the phase velocity at the midpoint.
				v[i] = velocityInterpolators[i].getVelocity(midpoint);
				// add a travel time component to the path integrals
				travelTimes[i] += dx / v[i];
			}

			// if caller provided arrays in which to put path points and velocity values, do it.  optional.
			if (points != null) points.add(next.point);
			if (pathVelocities != null) pathVelocities.add(v);

			previous = next;

		}

		return travelTimes;
	}

	protected Collection<double[]> getIntersectionsSlow(GreatCircle path) throws Exception {
		// create a Collection to contain the points along the greatcircle where it intersects the 
		// grid longitudes and colatitudes.  
		Collection<double[]> intersections = new ArrayList<>();
		// add the first and last points of the great circle
		intersections.add(path.getFirst());
		intersections.add(path.getLast());

		for (GreatCircle circle : meridians)
			intersections.addAll(circle.getIntersections(path, true));

		for (SmallCircle circle : parallels)
			intersections.addAll(circle.getIntersections(path, true));

		return intersections;
	}

	protected Collection<double[]> getIntersections(GreatCircle path) throws Exception {

		if (Double.isNaN(path.getAzimuth()) && Double.isNaN(path.getBackAzimuth()))
			throw new Exception("GreatCircle path is invalid because distance between them is equal to PI");

		// create a Collection to contain the points along the greatcircle where it intersects the 
		// grid longitudes and colatitudes.  
		Collection<double[]> intersections = new ArrayList<>();

		// add the first and last points of the great circle
		intersections.add(path.getFirst());
		intersections.add(path.getLast());

		processMeridians(path, intersections);

		processParallels(path, intersections);

		return intersections;
	}

	private void processMeridians(GreatCircle path, Collection<double[]> intersections) {
		// There is considerable complexity here designed to improve computational performance
		// by avoiding processing meridians that have no chance of contributing intersections.
		// If you are skeptical of this, you can replace the rest of this method with this:
		//		for (GreatCircle circle : meridians)
		//			intersections.addAll(circle.getIntersections(path, true));
		// which will produce identical results in twice the time.

		if (Double.isNaN(path.getAzimuth()) || Double.isNaN(path.getBackAzimuth())) {
			// one, and only one, of the ends of the path coincides with one of the poles.
			// (already tested for distance == PI)
			// No intersections with a meridian are possible.  No need to check any meridians.
		}
		else {
			// find projections of getFirst() onto equatorial plane
			double[] c1 = new double[3];
			VectorUnit.crossNorth(path.getFirst(), c1);

			// find projections of getLast() onto equatorial plane
			double[] c2 = new double[3];
			VectorUnit.crossNorth(path.getLast(), c2);

			// find the midpoint of projections onto equatorial plane
			double[] c = VectorUnit.center(c1, c2);

			// find cosine of angle between c1 and c
			double delta = VectorUnit.dot(c1, c);

			for (int i=0; i<meridians.length; ++i) 
				// if either of the two normals to the meridian is between c1 and c2 then
				// the absolute value the dot product with c will be >= the dot product of c and c1.
				if (abs(VectorUnit.dot(meridians[i].getNormal(), c)) >= delta) 
				{
					//System.out.println(EarthShape.SPHERE.getLonDegrees(meridians[i].getNormal())+90.);
					intersections.addAll(meridians[i].getIntersections(path, true));
				}
		}
	}

	private void processParallels(GreatCircle path, Collection<double[]> intersections) {
		// There is considerable complexity here designed to improve computational performance
		// by avoiding processing parallels that have no chance of contributing intersections.
		// If you are skeptical of this, you can replace the rest of this method with this:
		//		for (SmallCircle parallel : parallels)
		//			intersections.addAll(parallel.getIntersections(path, true));
		// which will produce identical results in twice the time.

		// see if ray leaves the first point heading north or south. 
		double d1 = cos(VectorUnit.azimuth(path.getFirst(), path.getLast(), Double.NaN));

		// see if ray leaves the last point heading north or south.
		double d2 = cos(VectorUnit.azimuth(path.getLast(), path.getFirst(), Double.NaN));

		// set south equal to either first or last, whichever is further south
		// and north equal to either first or last, whichever is further north
		double[] south = null, north = null;
		if (path.getFirst()[2] >= path.getLast()[2]) {
			north = path.getFirst();
			south = path.getLast();
		}
		else {
			north = path.getLast();
			south = path.getFirst();
		}

		ArrayList<SmallCircle> check = new ArrayList<>(parallels.length);
		if (abs(d1) < 1e-7 && abs(d2) < 1e-7) {
			// ray path coincides with the equator along its entire length.  There can be no intersections.
			//do nothing;			
		}
		else if (d1 >= 0. && d2 >= 0) {
			// both ends of the path take off going north.
			// Only check parallels that are north of southern point
			for (int i = getColatitudeIndex(south); i>0; --i)
				check.add(parallels[i]);
		} else if (d1 <= 0. && d2 <= 0) {
			// both ends of the path take off going south.
			// Only check parallels that are south of northern point
			for (int i = getColatitudeIndex(north)+1; i < nlat; ++i)
				check.add(parallels[i]);
		} else  {
			// one end takes off going north, the other south.
			// Only check parallels that are between the two ends of path.
			for (int i=getColatitudeIndex(north)+1; i<= getColatitudeIndex(south); ++i)
				check.add(parallels[i]);
		}

		for (SmallCircle parallel : check) {
			List<double[]> i = parallel.getIntersections(path, true);
			if (i.isEmpty())
				break;
			intersections.addAll(i);
		}
	}

	/**
	 * Retrieve the index in the geocentric colatitude array such that the latitude
	 * of point falls in the range.
	 * @param colatitude in radians
	 * @return
	 */
	public int getColatitudeIndex(double colat) {
		return Math.min(nlat-1, (int)floor(colat / spacing));
	}

	/**
	 * Retrieve the index in the geocentric colatitude array such that the latitude
	 * of point falls in the range.
	 * @param point
	 * @return
	 */
	public int getColatitudeIndex(double[] point) {
		return getColatitudeIndex(EarthShape.SPHERE.getGeocentricCoLat(point));
	}

	/**
	 * Retrieve the index in the longitudes array such that the longitude
	 * of point falls in the range.
	 * @param point
	 * @return
	 */
	public int getLongitudeIndex(double[] point) {
		// ensure that longitude of pt is between 0 and 2*PI
		return Math.min(nlon-1, (int)floor(((EarthShape.SPHERE.getLon(point)+TWO_PI) % TWO_PI) / spacing));
	}

	/**
	 * Return the 1D array of velocities for the grid cell that contains the 
	 * specified point.
	 * @param pt
	 * @return
	 */
	public double[] getVelocityArray(double[] pt) {
		return velocities[getColatitudeIndex(pt)][getLongitudeIndex(pt)];
	}

	/**
	 * Read the contents of the given file but skip empty lines,
	 * comment lines, and parts of lines that are comments.
	 * Basically returns just the numbers in the file in one giant string.
	 * @param f
	 * @return
	 * @throws Exception
	 */
	private String loadFile(File f) throws Exception {
		StringBuffer buf = new StringBuffer();
		Scanner input = new Scanner(f);
		while (input.hasNextLine()) {
			String line = input.nextLine().trim();
			if (!line.startsWith("#") && line.length() > 0) {
				int i = line.indexOf("#");
				if (i > 0)
					line = line.substring(0, i).trim();
				buf.append(line+" ");
			}
		}
		input.close();
		return buf.toString();
	}

	public SeismicPhase getPhase() {
		return phase;
	}

	/**
	 * Retrieve the geocentric colatitudes that define the velocity grid, in radians
	 * @return
	 */
	public double[] getColatitudes() {
		double[] colatitudes = new double[nlat];
		for (int i=0; i<nlat; ++i) colatitudes[i] = i*spacing;
		return colatitudes;
	}

	/**
	 * Retrieve the longitudes that define the velocity grid, in radians in range 0 to 2*PI
	 * @return
	 */
	public double[] getLongitudes() {
		double[] x = new double[nlon];
		for (int i=0; i<nlat; ++i) x[i] = i*spacing;
		return x;
	}

	public double[] getPeriods() {
		return periods;
	}
	
	public int getNPeriods() {
		return periods.length;
	}

	public double getPeriod(int i) {
		return periods[i];
	}

	/**
	 * Retrieve the grid velocities.  Dimensions of the array are colatitude, longitude, period.
	 * @return
	 */
	public double[][][] getVelocities() {
		return velocities;
	}

	public double[] getVelocityRange() {
		double[] range = new double[] {Double.MAX_VALUE, Double.MIN_VALUE};
		for (int i=0; i<velocities.length; ++i)
			for (int j=0; j<velocities[i].length; ++j)
				for (int k=0; k<velocities[i][j].length; ++k) {
					double v = velocities[i][j][k];
					if (v < range[0])
						range[0] = v;
					if (v > range[1])
						range[1] = v;
				}
		return range;
	}

	public void close() {
		velocities = null;
		meridians = null;
		parallels = null;
		periods = null;
	}

	public int getNlat() {
		return nlat;
	}

	/**
	 * Grid spacing in radians
	 * @return
	 */
	public double getSpacing() {
		return spacing;
	}

	public int getNlon() {
		return nlon;
	}

	/**
	 * internal class to store a double[] point and the distance of the point
	 * from the start of the great circle.  Used to sort a list of points into 
	 * ascending order from start of great circle.
	 */
	class Point implements Comparable<Point> 
	{
		double[] point;
		double distance;

		public Point(double[] point, double distance) {
			this.distance = distance;
			this.point = point;
		}

		@Override
		public String toString() {
			return String.format("%s latidx = %d lonidx = %d distance = %1.3f", 
					VectorGeo.getLatLonString(point), 
					getColatitudeIndex(point),
					getLongitudeIndex(point),
					toDegrees(distance));
		}

		@Override
		public int compareTo(Point o) {
			double diff = this.distance-o.distance;
			return diff > 1e-7 ? 1 : diff < -1e-7 ? -1 : 0;
		}
	}

	public void vtk(File dir, String baseName, GreatCircle path) throws Exception {
		dir.mkdir();
		vtkGrid(new File(dir, baseName+"_grid.vtk"));
		vtkPath(new File(dir, baseName+"_path.vtk"), path);
		vtkIntersections(new File(dir, baseName+"_intersections.vtk"), path);
	}

	/**
	 * 
	 * @param outputFile
	 * @param lat in degrees
	 * @param lon in degrees
	 * @throws Exception 
	 */
	public void vtkPath(File outputFile, double lat1Degrees, double lon1Degrees, double lat2Degrees, double lon2Degrees) throws Exception {
		vtkPath(outputFile, new GreatCircle(VectorGeo.getVectorDegrees(lat1Degrees, lon1Degrees), 
				VectorGeo.getVectorDegrees(lat2Degrees, lon2Degrees)));
	}

	/**
	 * 
	 * @param outputFile
	 * @param path
	 * @throws Exception
	 */
	public void vtkPath(File outputFile, GreatCircle path) throws Exception {

//		ArrayList<double[]> intersections = new ArrayList<double[]>();
//		pathIntegral(path, new double[] {20.}, intersections, null);
		
		ArrayList<double[]> intersections = path.getPoints((int)Math.ceil(Math.toDegrees(path.getDistance())), false);


		ArrayList<double[]> points = new ArrayList<double[]>(intersections.size());		
		ArrayList<Integer> ids = new ArrayList<Integer>(intersections.size());

		int count = 0;
		for (int i=1; i<intersections.size(); ++i) {
			GreatCircle gc = new GreatCircle(intersections.get(i-1), intersections.get(i));
			ArrayList<double[]> pts = gc.getPoints((int)Math.ceil(Math.toDegrees(path.getDistance())), false);	


			for (double[] pt : pts) {
				ids.add(count++);
				points.add(pt);
			}
		}

		// generate list of cells
		List<VTKCell> cells = new ArrayList<>();
		cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, ids));

		// write the vtk file.
		VTKDataSet.write(outputFile, points, cells);
	}


	/**
	 * 
	 * @param outputFile
	 * @param path
	 * @throws Exception
	 */
	public void vtkPath(File outputFile, Collection<GreatCircle> paths) throws Exception {
		ArrayList<double[]> points = new ArrayList<>();
		List<VTKCell> cells = new ArrayList<>();

		int id=0;
		for (GreatCircle path : paths) {
			ArrayList<double[]> p = path.getPoints((int)Math.ceil(Math.toDegrees(path.getDistance())), false);
			points.addAll(p);
			ArrayList<Integer> ids = new ArrayList<Integer>();
			for (int i=0; i<p.size(); ++i) 
				ids.add(id++); 

			// vtk poly line
			cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, ids));

		}

		// write the vtk file.
		VTKDataSet.write(outputFile, points, cells);
	}

	/**
	 * 
	 * @param outputFile
	 * @param geographic lat in degrees
	 * @param geographic lon in degrees
	 * @throws Exception 
	 */
	public void vtkIntersections(File outputFile, double lat1Degrees, double lon1Degrees, double lat2Degrees, double lon2Degrees) throws Exception {
		vtkIntersections(outputFile, new GreatCircle(VectorGeo.getVectorDegrees(lat1Degrees, lon1Degrees), 
				VectorGeo.getVectorDegrees(lat2Degrees, lon2Degrees)));
	}

	public void vtkIntersections(File outputFile, GreatCircle path) throws Exception {
		ArrayList<double[]> intersections = new ArrayList<double[]>();

		try {
			pathIntegral(path, new double[] {20.}, intersections, null);
		} catch (Exception e) {
		}

		vtkIntersections(outputFile, intersections);
	}

	public void vtkIntersections(File outputFile, ArrayList<double[]> intersections) throws Exception {
		ArrayList<Integer> ids2 = new ArrayList<Integer>(intersections.size());
		for (int i=0; i<intersections.size(); ++i)
			ids2.add(i);

		// generate list of cells
		List<VTKCell> cells = new ArrayList<>();
		cells.add(new VTKCell(VTKCellType.VTK_POLY_VERTEX, ids2));

		// write the vtk file.
		VTKDataSet.write(outputFile, intersections, cells);
	}

	public void vtkGrid(File outputFile) throws Exception {

		// make a list of all the point in the model.
		List<double[]> points = new ArrayList<>((nlat+1)*(nlon+1));
		for (int i=0; i<=nlat; ++i) {
			for (int j=0; j<=nlon; ++j) {
				points.add(EarthShape.SPHERE.getVector(PI/2-i*spacing, j*spacing));
			}
		}

		// generate list of QUAD cells
		List<VTKCell> cells = new ArrayList<>(nlat*nlon);
		int nl = nlon+1;
		int k = 0;
		for (int i=0; i<nlat; ++i) {
			for (int j=0; j<nlon; ++j) {
				VTKCell cell = new VTKCell(VTKCellType.VTK_QUAD, new int[] {k, k+nl, k+nl+1, k+1});
				cells.add(cell);
				++k;
			}
			++k;
		}

		// generate list of attribute names (periods)
		List<String> attributeNames = new ArrayList<String>();
		for (double period : periods)
			attributeNames.add(String.format("%s velocity (km/sec) at period %1.0f sec", phase.name(), period));

		// generate a 2D array of attribute values at each grid cell
		List<float[]> attributes = new ArrayList<float[]>();
		for (int i=0; i<nlat; ++i) {
			for (int j=0; j<nlon; ++j) {
				double[] vel = velocities[i][j];
				float[] v = new float[vel.length];
				for (int n=0; n<vel.length; ++n)
					v[n] = (float) vel[n];
				attributes.add(v);
			}
		}

		// write the vtk file.
		VTKDataSet.write(outputFile, points, cells, attributeNames, attributes);
	}

	public void vtkRobinsonGrid(File outputFile, double centerLon) throws Exception {
		outputFile.getParentFile().mkdir();

		double spacingDegrees = toDegrees(spacing);

		if (centerLon % spacingDegrees > 1e-6)
			throw new Exception("centerLon is not evenly divisible by spacing.");

		RobinsonProjection map = new RobinsonProjection(centerLon);

		double lonRight = (centerLon+180.)%360.;

		int jright = (int) Math.round(lonRight/spacingDegrees);

		// make a list of all the point in the model.
		List<double[]> points = new ArrayList<>((nlat+1)*(nlon+2));
		for (int i=0; i<=nlat; ++i) {
			for (int j=0; j<=nlon; ++j) {
				double[] point = map.project(90.-i*spacingDegrees, j*spacingDegrees, true);
				if (j == jright) {
					// if this is the right edge of the map then add two points, 
					// one will reside on the right edge of the map, the other on left edge.
					points.add(new double[] {abs(point[0]), point[1]});
					points.add(new double[] {-abs(point[0]), point[1]});
				}
				else 
					points.add(point);
			}
		}

		// generate list of QUAD cells
		List<VTKCell> cells = new ArrayList<>(nlat*nlon);
		{
			int k = 0;
			for (int i=0; i<nlat; ++i) {
				for (int j=0; j<=nlon; ++j) {
					// do not add a cell that includes points on the right and left edges of the map
					if (j != jright) {
						VTKCell cell = new VTKCell(VTKCellType.VTK_QUAD, new int[] {k, k+nlon+2, k+nlon+3, k+1});
						cells.add(cell);
					}
					++k;
				}
				++k;
			}
		}

		// generate list of attribute names (periods)
		List<String> attributeNames = new ArrayList<String>();
		for (double period : periods)
			attributeNames.add(String.format("%s velocity (km/sec) at period %1.0f sec", phase.name(), period));

		// generate a 2D array of attribute values at each grid cell
		List<float[]> attributes = new ArrayList<float[]>();
		for (int i=0; i<nlat; ++i) {
			for (int j=0; j<=nlon; ++j) {
				// do not add an attribute array for cell at right edge of map
				if (j < jright){
					double[] vel = velocities[i][j];
					float[] v = new float[vel.length];
					for (int k=0; k<vel.length; ++k)
						v[k] = (float) vel[k];
					attributes.add(v);
				}
				else if (j > jright) {
					double[] vel = velocities[i][j-1];
					float[] v = new float[vel.length];
					for (int k=0; k<vel.length; ++k)
						v[k] = (float) vel[k];
					attributes.add(v);
				}
			}
		}

		// write the vtk file.
		VTKDataSet.write(outputFile, points, cells, attributeNames, attributes);

		// write the coastlines and map edge to files in the output directory
		GeoTessModelUtils.vtkRobinsonCoastlines(outputFile.getParentFile(), map);
		GeoTessModelUtils.vtkRobinsonMapEdge(outputFile.getParentFile());
	}

	public void vtkRobinsonPath(File outputFile, GreatCircle path, double centerLon) throws Exception {
		RobinsonProjection map = new RobinsonProjection(centerLon);

		ArrayList<double[]> points = new ArrayList<>();
		List<VTKCell> cells = new ArrayList<>();

		int id=0;
		ArrayList<double[]> p = path.getPoints((int)Math.ceil(Math.toDegrees(path.getDistance())), false);

		ArrayList<ArrayList<double[]>> more = map.project(p);

		for (ArrayList<double[]> x : more) {
			points.addAll(x);
			ArrayList<Integer> ids = new ArrayList<Integer>();
			for (int i=0; i<x.size(); ++i) 
				ids.add(id++); 

			// vtk poly line
			cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, ids));
		}

		// write the vtk file.
		VTKDataSet.write(outputFile, points, cells);
	}

	public void vtkRobinsonIntersections(File outputFile, GreatCircle path, double centerLon) throws Exception {
		ArrayList<double[]> intersections = new ArrayList<double[]>();

		pathIntegral(path, new double[] {20.}, intersections, null);

		vtkRobinsonIntersections(outputFile, intersections, centerLon);
	}

	public void vtkRobinsonIntersections(File outputFile, ArrayList<double[]> intersections, double centerLon) throws Exception {
		RobinsonProjection map = new RobinsonProjection(centerLon);
		ArrayList<Integer> ids2 = new ArrayList<Integer>(intersections.size());
		ArrayList<double[]> points = new ArrayList<>(intersections.size());
		for (int i=0; i<intersections.size(); ++i) {
			ids2.add(i);
			points.add(map.project(intersections.get(i)));
		}
		
		// generate list of cells
		List<VTKCell> cells = new ArrayList<>();
		cells.add(new VTKCell(VTKCellType.VTK_POLY_VERTEX, ids2));

		// write the vtk file.
		VTKDataSet.write(outputFile, points, cells);
	}

}
