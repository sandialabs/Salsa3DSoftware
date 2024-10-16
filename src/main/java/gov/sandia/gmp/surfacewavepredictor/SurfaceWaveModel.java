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
import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

public class SurfaceWaveModel {

	/**
	 * evenly spaced geocentric colatitudes, in radians. 0 to (PI - spacing)
	 */
	private double[] colatitudes;

	/**
	 * evenly spaced longitudes in radians. Range is 0 to (2*PI - spacing) 
	 */
	private double[] longitudes;

	/**
	 * Array of period values loaded from the velocity file.
	 */
	private double[] periods;

	/**
	 * Cell based velocity values in km/sec as a function of geocentric colatitude, longitude and period.
	 */
	private double[][][] velocities;

	private SeismicPhase phase; 

	/**
	 * Grid spacing in radians in both colatitude and longitude dimensions.
	 */
	private double spacing;

	private static final double[] northPole = new double[] {0,0,1};
	private static final double[] southPole = new double[] {0,0,-1};


	public static void main(String[] args) {
		try {

			for (SeismicPhase phase : new SeismicPhase[] {SeismicPhase.LR, SeismicPhase.LQ}) {
				
				SurfaceWaveModel model = new SurfaceWaveModel(new File("/Users/sballar/Documents/GMS/earth_specs/TT/LP"), phase);

				System.out.println(Arrays.toString(model.getVelocityRange()));

				model.vtk(new File("target/"+model.getPhase().name()+"_grid.vtk"));
				
				double lat1 = 27;
				double lon1 = -137;
				double lat2 = 63;
				double lon2 = -67;

				model.vtkPath(new File("target/"+model.getPhase().name()+"_path.vtk"), lat1, lon1, lat2, lon2);

				model.vtkIntersections(new File("target/"+model.getPhase().name()+"_intersections.vtk"), lat1, lon1, lat2, lon2);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Load a surface wave model.
	 * @param directory the directory where the model files are stored.
	 * @param phase either 'LR' or 'LQ'
	 * @throws Exception
	 */
	public SurfaceWaveModel(File directory, SeismicPhase phase) throws Exception {
		this.phase = phase;

		Scanner input = new Scanner(loadFile(new File(directory, "LP_grid."+phase.name())));
		int nlat = input.nextInt();
		int nlon = input.nextInt();

		spacing = toRadians(input.nextDouble());

		colatitudes = new double[nlat];
		for (int i=1; i<nlat; ++i)
			colatitudes[i] = colatitudes[i-1]+spacing;

		longitudes = new double[nlon];
		for (int j=1; j<nlon; ++j)
			longitudes[j] = longitudes[j-1]+spacing;

		int[][] index = new int[nlat][nlon];
		for (int i=0; i<nlat; ++i) {
			int[] idx = index[i];
			for (int j=0; j<nlon; ++j)
				idx[j] = input.nextInt();
		}
		input.close();

		input = new Scanner(loadFile(new File(directory, "LP_vel."+phase)));

		int n_indexes = input.nextInt();
		int n_periods = input.nextInt();

		periods = new double[n_periods];
		for (int i=0; i<n_periods; ++i)
			periods[i] = input.nextDouble();

		double[][] vel = new double[n_indexes][n_periods];
		for (int i=0; i<n_indexes; ++i)
			for (int j=0; j<n_periods; ++j)
				vel[i][j] = input.nextDouble();
		input.close();

		velocities = new double[nlat][nlon][];
		for (int i=0; i<nlat; ++i)
			for (int j=0; j<nlon; ++j)
				velocities[i][j] = vel[index[i][j]];
	}

	/**
	 * Compute the path integral (travel time in seconds) from point1 to point2 using phase velocities 
	 * at the specified period.
	 * @param point1 earth-centered unit vector 
	 * @param point2 earth-centered unit vector
	 * @param period
	 * @return travel time of the ray path in seconds
	 * @throws Exception
	 */
	public double pathIntegral(double[] point1, double[] point2, double period) throws Exception {
		return pathIntegral(point1, point2, period, null, null);
	}

	/**
	 * Compute the path integral (travel time in seconds) from point1 to point2 using phase velocities 
	 * at the specified period.
	 * @param point1
	 * @param point2
	 * @param period
	 * @param points (optional) if this array is not null it will be cleared and populated with the points along
	 * the ray path that were used to compute the path integral.  These points will reside 
	 * along either the colatitudes or the longitudes of the grid.
	 * @param pathVelocities (optional) if this array is not null then it will be cleared and populated with the phase
	 * velocities used to compute the path integral.  The phase velocity values are interpolated
	 * at the center of each path interval.  pathVelocities.size() will equal points.size()-1.
	 * @return travel time of the ray path in seconds
	 * @throws Exception
	 */
	public double pathIntegral(double[] point1, double[] point2, double period, 
			ArrayList<double[]> points, ArrayList<Double> pathVelocities) throws Exception {

		GreatCircle greatCircle = new GreatCircle(point1, point2); 

		// create an array to contain the points along the greatcircle where it intersects the 
		// grid longitudes and colatitudes.
		ArrayList<Point> path = new ArrayList<>(200);

		// add the first and last points of the great circle, along with the distance of each
		// from the first point of the great circle.
		path.add(new Point(point1, 0.));
		path.add(new Point(point2, greatCircle.getDistance()));

		for (double colatitude : colatitudes) {
			// find all intersections of this great circle with the small circle that corresponds
			// to the current colatitude. colatitudes are assumed to be geocentric.
			ArrayList<double[]> intersections = greatCircle.getIntersections(northPole, colatitude, true);
			// add all the intersections to the current path.  The intersections will be in random
			// order but they will be sorted later.
			for (double[] pt : intersections) 
				path.add(new Point(pt, greatCircle.getDistance(pt)));	
		}

		for (double longitude : longitudes) {
			// create a great circle that extends from pole to pole along the specified longitude.
			GreatCircle meridian = new GreatCircle(northPole, VectorGeo.getVector(0., longitude), southPole);
			// find all the intersections of the current great circle with the meridian
			for (double[] intersection : greatCircle.getIntersections(meridian, true))
				path.add(new Point(intersection, greatCircle.getDistance(intersection)));

			//			double[] intersection = greatCircle.getIntersection(meridian, true);
			//			// add all the intersections to the current path.  The intersections will be in random
			//			// order but they will be sorted later.
			//			if (intersection != null) {
			//				path.add(new Point(intersection, greatCircle.getDistance(intersection)));
			//			}	
		}

		// sort the points by distance along the great circle.
		Collections.sort(path);

		// compute interpolation coefficients based on period array that can be used to interpolate
		// velocity values.
		VelocityInterpolator velocityInterpolator = new VelocityInterpolator(this, period);

		// if caller provided arrays in which to put path points and velocity values, initialize them.  optional.
		if (points != null) {
			points.clear();
			points.add(greatCircle.getFirst());
		}
		if (pathVelocities != null)
			pathVelocities.clear();

		double pathIntegral = 0;
		for (int k=1; k<path.size(); ++k) {
			// find epicentral distance from previous point to this point.
			double delta = path.get(k).distance - path.get(k-1).distance;
			// proceed only if points are not colocated.
			if (delta > 1e-7) {
				// find a new point midway current point and previous point.
				double[] midPoint = VectorGeo.center(path.get(k-1).point, path.get(k).point);
				// interpolate a value of the phase velocity at the midpoint.
				double velocity = velocityInterpolator.getVelocity(midPoint);
				// add a travel time component to the path integral
				pathIntegral += delta * VectorGeo.getEarthRadius(midPoint) / velocity;

				// if caller provided arrays in which to put path points and velocity values, do it.  optional.
				if (points != null) points.add(path.get(k).point);
				if (pathVelocities != null) pathVelocities.add(velocity);
			}
		}

		return pathIntegral;
	}

	/**
	 * internal class to store a double[] point and the distance of the point
	 * from the start of the great circle.  Used to sort a list of points into 
	 * ascending order from start of great circle.
	 */
	public class Point implements Comparable<Point> {
		double[] point;
		double distance;
		public Point(double[] point, double distance) {
			this.distance = distance;
			this.point = point;
		}

		@Override
		public int compareTo(Point o) {
			return (int)Math.signum(this.distance-o.distance);
		}
	}

	/**
	 * Retrieve the index in the geocentric colatitude array such that the latitude
	 * of point falls in the range.
	 * @param point
	 * @return
	 */
	public int getColatitudeIndex(double[] point) {
		return Globals.hunt(colatitudes, VectorGeo.getGeoCentricCoLatitude(point));
	}

	/**
	 * Retrieve the index in the longitudes array such that the longitude
	 * of point falls in the range.
	 * @param point
	 * @return
	 */
	public int getLongitudeIndex(double[] point) {
		// ensure that longitude of pt is between 0 and 2*PI
		return Globals.hunt(longitudes, (VectorGeo.getLon(point)+2*PI) % (2*PI));
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
		return colatitudes;
	}

	/**
	 * Retrieve the longitudes that define the velocity grid, in radians in range 0 to 2*PI
	 * @return
	 */
	public double[] getLongitudes() {
		return longitudes;
	}

	public double[] getPeriods() {
		return periods;
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

	/**
	 * 
	 * @param outputFile
	 * @param lat in degrees
	 * @param lon in degrees
	 * @throws Exception 
	 */
	public void vtkPath(File outputFile, double lat1Degrees, double lon1Degrees, double lat2Degrees, double lon2Degrees) throws Exception {

		GreatCircle path = new GreatCircle(EarthShape.SPHERE.getVectorDegrees(lat1Degrees, lon1Degrees), 
				EarthShape.SPHERE.getVectorDegrees(lat2Degrees, lon2Degrees));

		ArrayList<double[]> points = path.getPoints((int)Math.ceil(Math.toDegrees(path.getDistance())), false);

		ArrayList<Integer> ids = new ArrayList<Integer>(points.size());
		for (int i=0; i<points.size(); ++i)
			ids.add(i);

		// vtk poly line
		List<VTKCell> cells = new ArrayList<>();
		cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, ids));

		// write the vtk file.
		VTKDataSet.write(outputFile, points, cells);
	}

	/**
	 * 
	 * @param outputFile
	 * @param lat in degrees
	 * @param lon in degrees
	 * @throws Exception 
	 */
	public void vtkIntersections(File outputFile, double lat1Degrees, double lon1Degrees, double lat2Degrees, double lon2Degrees) throws Exception {

		ArrayList<double[]> intersections = new ArrayList<double[]>();

		pathIntegral(EarthShape.SPHERE.getVectorDegrees(lat1Degrees, lon1Degrees), 
				EarthShape.SPHERE.getVectorDegrees(lat2Degrees, lon2Degrees), periods[0], intersections, null);

		ArrayList<Integer> ids2 = new ArrayList<Integer>(intersections.size());
		for (int i=0; i<intersections.size(); ++i)
			ids2.add(i);

		// generate list of cells
		List<VTKCell> cells = new ArrayList<>();
		cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, ids2));

		// write the vtk file.
		VTKDataSet.write(outputFile, intersections, cells);


	}

	public void vtk(File outputFile) throws Exception {

		// make a list of all the point in the model.
		List<double[]> points = new ArrayList<>((colatitudes.length+1)*(longitudes.length+1));
		for (int i=0; i<=colatitudes.length; ++i) {
			double lat = PI/2-i*spacing;
			for (int j=0; j<=longitudes.length; ++j) {
				double lon = j*spacing;
				points.add(EarthShape.SPHERE.getVector(lat, lon));
			}
		}

		// generate list of QUAD cells
		List<VTKCell> cells = new ArrayList<>(colatitudes.length*longitudes.length);
		int nlon = longitudes.length+1;
		int k = 0;
		for (int i=0; i<colatitudes.length; ++i) {
			for (int j=0; j<longitudes.length; ++j) {
				VTKCell cell = new VTKCell(VTKCellType.VTK_QUAD, new int[] {k, k+nlon, k+nlon+1, k+1});
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
		for (int i=0; i<colatitudes.length; ++i) {
			for (int j=0; j<longitudes.length; ++j) {
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

	public void close() {
		velocities = null;
		colatitudes = longitudes = periods = null;
	}

}
