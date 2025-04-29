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
package gov.sandia.gmp.baseobjects.radial2dmodel;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.flinnengdahl.FlinnEngdahlCodes;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.io.GlobalInputStreamProvider;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

public class Radial2DModelLegacy implements Radial2DModel {

	private static final double HYDRO_SEC_PER_DEG = 75.;

	/**
	 * The canonical file from which this model was read.
	 */
	protected File inputFile;

	/**
	 * Unit vector of the location at the center of the model.
	 */
	protected final double[] center; 
	protected double centerLat, centerLon;

	/**
	 * Period name.  Corresponds to the name of a month or a season.
	 */
	protected String period;

	/**
	 * Array of azimuths in degrees.
	 */
	protected double[] azimuth;

	/**
	 * Radial spacing of grid nodes in degrees as a function of azimuth.
	 */
	protected double[] delta;
	
	/**
	 * travel time at each azimuth, radius, in seconds.
	 * tt[i][0] is at distance = delta[0], 
	 * i.e., at the end of the first distance interval.
	 */
	protected double[][] tt;

	/**
	 * travel time uncertainty at each azimuth, radius, in seconds
	 * uncertainty[i][0] is at distance = delta[0], 
	 * i.e., at the end of the first distance interval.
	 */
	protected double[][] uncertainty;

	/**
	 * travel time uncertainty that should be added to the
	 * normal values of tt_uncertainty stored in this model.  This constant value
	 * is retrieved from file <code>time_guide</code> in the directory from which
	 * this model was loaded.  It should only be used for hydroacoustic T phases.
	 */
	protected double htConvert;
	
	/**
	 * When VINCENTY is true, the incorrect vincenty method is used to compute
	 * distance, azimuth and backazimuth.  When false, the correct methods in
	 * EarthShape and VectorGeo are used to do that.
	 */
	public static final boolean VINCENTY = false;

	public Radial2DModelLegacy() {
		this.center = null;
	}

	public Radial2DModelLegacy(File inputFile) throws IOException {
		this.inputFile = inputFile.getCanonicalFile();
		htConvert = 0.;

		DataInputStream input = new DataInputStream(new BufferedInputStream(
				GlobalInputStreamProvider.forFiles().newStream(inputFile)));

		// read geoagraphic latitude and longitude in degrees and convert to unit 
		// vector, assuming WGS84 ellipsoid
		centerLat = input.readFloat();
		centerLon = input.readFloat();
		center = EarthShape.WGS84.getVectorDegrees(centerLat, centerLon);

		period = readString(input, 1024);

		ArrayListDouble az = new ArrayListDouble(720);
		ArrayList<double[]> t = new ArrayList<>(400);
		ArrayList<double[]> u = new ArrayList<>(400);

		ArrayListDouble dr = new ArrayListDouble(720);
		ArrayListInt nr = new ArrayListInt(720);

		while (input.available() > 0) {

			az.add(input.readFloat());

			int n = input.readInt();
			nr.add(n);

			dr.add(input.readFloat());

			double[] ti = new double[n];
			double[] ui = new double[n];

			for (int i=0; i<n; ++i) 
				ti[i] = input.readFloat();
			for (int i=0; i<n; ++i) 
				ui[i] = input.readFloat();				

			t.add(ti);
			u.add(ui);
		}

		int naz = az.size();
		azimuth = az.toArray();
		tt = t.toArray(new double[naz][]);
		uncertainty = u.toArray(new double[naz][]);

		delta = dr.toArray();

		input.close();
	}

	/**
	 * Retrieve a map of attribute values.  The map will always contain
	 * distance, distance_degrees, azimuth and azimuth_degrees.
	 * If not blocked, values will also contain travel_time, tt_model_uncertainty,
	 * slowness and slowness_degrees.  Slowness is computed as travel_time/distance.
	 * @param lat geographic latitude in degrees
	 * @param lon longitude in degrees
	 * @return map of attribute values.  If map does not include travel_time then 
	 * ray path was blocked.
	 * @throws Exception
	 */
	@Override
	public EnumMap<GeoAttributes, Double> interpolate(double lat, double lon) throws Exception {
		return interpolate(VectorGeo.getVectorDegrees(lat, lon));
	}

	/**
	 * Retrieve a map of attribute values.  The map will always contain
	 * distance, distance_degrees, azimuth and azimuth_degrees.
	 * If not blocked, values will also contain travel_time, tt_model_uncertainty,
	 * slowness and slowness_degrees.  Slowness is computed as travel_time/distance.
	 * @param v unit vector of location where interpolation should be calculated.
	 * @return map of attribute values.  If map does not include travel_time then 
	 * ray path was blocked.
	 * @throws Exception 
	 */
	@Override
	public EnumMap<GeoAttributes, Double> interpolate(double[] v) throws Exception {

		double r, seaz;
		if (VINCENTY) {
			// compute distance, azimuth and backazimuth from center to v, all in degrees
			// using the erroneous Vincenty method.
			double[] ellip_dist = ellip_dist(centerLat, centerLon,
					EarthShape.WGS84.getLatDegrees(v), EarthShape.WGS84.getLonDegrees(v), false);

			r = ellip_dist[0];
			seaz = ellip_dist[1];
		}
		else {
			r = VectorUnit.angleDegrees(center, v);
			seaz = VectorUnit.azimuthDegrees(center, v, Double.NaN);
		}

		if (Double.isNaN(seaz))
			seaz = 0.;
		else
			seaz = (seaz+360.) % 360.;

		EnumMap<GeoAttributes, Double> values = interp(r, seaz);

		// if raypath was blocked, try going the other way around the world.
		if (Math.round(values.get(GeoAttributes.HYDRO_BLOCKED)) == 4L) {
			EnumMap<GeoAttributes, Double> values2 = interp(360.-r, (seaz+180.) % 360.);
			if (Math.round(values2.get(GeoAttributes.HYDRO_BLOCKED)) < 4L)
				return values2;
		}

		return values;
	}

	/**
	 * Retrieve a map of attribute values.  The map will always contain
	 * distance, distance_degrees, azimuth and azimuth_degrees.
	 * If not blocked, values will also contain travel_time, tt_model_uncertainty,
	 * slowness and slowness_degrees.  Slowness is computed as travel_time/distance.
	 *
	 * @param distance receiver-source distance in degrees.
	 * @param seaz receiver-source azimuth in degrees.
	 * @return map of attribute values.  If map does not include travel_time then 
	 * ray path was blocked.
	 */
	private EnumMap<GeoAttributes, Double> interp(double distance, double seaz) {

		EnumMap<GeoAttributes, Double> values = new EnumMap<GeoAttributes, Double>(GeoAttributes.class);

		values.put(GeoAttributes.DISTANCE, toRadians(distance));
		values.put(GeoAttributes.DISTANCE_DEGREES, distance);

		values.put(GeoAttributes.AZIMUTH, toRadians(seaz));
		values.put(GeoAttributes.AZIMUTH_DEGREES, seaz);

		int iaz = Globals.hunt(azimuth, seaz);

		double[][] ttime = new double[2][2];
		double[][] error = new double[2][2];
		double[] dist_interp = new double[2];

		int blocked = 0;

		for (int k=0; k<2; ++k) {
			int kaz = iaz(iaz+k);
			int nrad = tt[kaz].length;
			double delta_rad = delta[iaz(kaz)];
			double dist_deltarad = distance / delta_rad;
			int d = (int)(dist_deltarad); // first index in tt table where tt[i] is >= r
			for (int l=0; l<2; ++l) {
				int steps_beyond_table = d+l - nrad;
				if (steps_beyond_table > 0) {
					++blocked;
					// This distance is beyond our table.  Extrapolate beyond
					// the end of the table using a constant velocity 
					ttime[k][l] = HYDRO_SEC_PER_DEG * delta_rad * steps_beyond_table;
					if(nrad > 0)
						ttime[k][l] += tt[kaz][nrad-1];

					// If this table didn't go out 5 deg for this azimuth,
					// return a modeling error of 
					// 5 seconds if distance < 5 deg
					// 1 sec/deg if distance > 5 deg
					if(((d+l)*delta_rad)>5.0) 
						error[k][l] = 5.0;
					else
						error[k][l] = (d+l)*delta_rad;	
				}
				else {
					if (d+l > 0) // The event is within the grid.  Interpolate
					{
						ttime[k][l] = tt[kaz][d+l-1];
						error[k][l] = uncertainty[kaz][d+l-1];
					}
					else  // The event is closer than the first distance sample.
						ttime[k][l] = error[k][l] = 0.0;
				}
			}
			dist_interp[k] = (distance - d*delta_rad)/(delta_rad);
		}

		double azi_interp = daz(seaz, azimuth[iaz])/daz(iaz+1, iaz);

		// Interpolate between 4 points, which are either true grid points
		// or extrapolated values.
		double travel_time = (ttime[0][0]*(1.0-dist_interp[0]) + 
				ttime[0][1]*dist_interp[0]) * (1.0-azi_interp);
		travel_time += (ttime[1][0]*(1.0-dist_interp[1]) + 
				ttime[1][1]*dist_interp[1]) * azi_interp;

		values.put(GeoAttributes.TRAVEL_TIME, travel_time);

		double model_error = (error[0][0]*(1.0-dist_interp[0]) + 
				error[0][1]*dist_interp[0]) * (1.0-azi_interp);
		model_error += (error[1][0]*(1.0-dist_interp[1]) + 
				error[1][1]*dist_interp[1]) * azi_interp;

		values.put(GeoAttributes.TT_MODEL_UNCERTAINTY, model_error);

		values.put(GeoAttributes.SLOWNESS, travel_time/toRadians(distance)); // sec/radian
		values.put(GeoAttributes.SLOWNESS_DEGREES, travel_time/distance); // sec/degree
		
		values.put(GeoAttributes.HYDRO_BLOCKED, (double) blocked);

		return values;
	}

	/**
	 * Return the maximum distance in degrees from the center of the model moving in the direction 
	 * given by azimuth in degrees.  For hydroacoustics, this is blockage.
	 * @param az in degrees
	 * @return max distance in degrees
	 */
	@Override
	public double getMaxDistance(double az) {
		int i1 = Globals.hunt(azimuth, ((az + 360.) % 360.)); 
		int i2 = iaz(i1+1);
		return Math.max(delta[i1]*tt[i1].length, delta[i2]*tt[i2].length);
	}

	/**
	 * Retrieve the canonical File from which this model was loaded
	 */
	@Override
	public File inputFile() {
		return inputFile;
	}

	/**
	 * Station name. Same as the canonical file name.
	 * @return
	 */
	@Override
	public String name() {
		return inputFile.getName();
	}

	/**
	 * The season.  Returns the name of the directory from which this model was read.
	 * Will be the name of a month, or of a season.
	 * @return
	 */
	@Override
	public String season() {
		return inputFile.getParentFile().getName();
	}

	/**
	 * Latitude of the center of the model in degrees.
	 * @return
	 */
	@Override
	public double lat() {
		return VectorGeo.getLatDegrees(center);
	}

	/**
	 * Longitude of the center of the model in degrees.
	 * @return
	 */
	@Override
	public double lon() {
		return VectorGeo.getLonDegrees(center);
	}

	/**
	 * Retrieve the earth-centered unit vector of the center of the model.
	 * @return
	 */
	@Override
	public double[] center() {
		return center;
	}

	/**
	 * Value of <i>period</i> read from the input file.  This is 
	 * supposed to be the <i>season</i>, but is not (at least in some models).
	 * @return
	 */
	@Override
	public String period() {
		return period;
	}

	/**
	 * The travel time at each grid point, in seconds.
	 * Dimensions are nAzimuths x nRadii. 
	 * tt()[i][0] is at radius 0 degrees and is equal to 0 seconds
	 * @return
	 */
	@Override
	public double[][] tt() {
		return tt;
	}

	/**
	 * The travel time uncertainty at each grid point, in seconds.
	 * Dimensions are nAzimuths x nRadii. 
	 * uncertainty()[i][0] is at radius 0 degrees and is equal to 0 seconds
	 * @return
	 */
	@Override
	public double[][] uncertainty() {
		return uncertainty;
	}

	/**
	 * returns the value of travel time uncertainty that should be added to the
	 * normal values of tt_uncertainty stored in this model.  This constant value
	 * is retrieved from file <code>time_guide</code> in the directory from which
	 * this model was loaded.  It should only be used for hydroacoustic T phases.
	 * @return
	 */
	@Override
	public double htConvert() {
		return htConvert;
	}

	/**
	 * set the value of travel time uncertainty that should be added to the
	 * normal values of tt_uncertainty stored in this model.  This constant value
	 * is retrieved from file <code>time_guide</code> in the directory from which
	 * this model was loaded.  It should only be used for hydroacoustic T phases.
	 * @param htConvert
	 */
	@Override
	public void htConvert(double htConvert) {
		this.htConvert = htConvert;
	}

	/** 
	 * if i is < 0 or >= azimuth.length, wrap it around to be in range.
	 * @param i
	 * @return (i+azimuth.length) % azimuth.length;
	 */
	protected int iaz(int i) { return (i+azimuth.length) % azimuth.length; }

	/**
	 * Retrieve azimuth[i2] - azimuth[i1] ensuring that proper rotations occur.  
	 * Result will always be >= 0 and < 360
	 * @param i2
	 * @param i1
	 * @return
	 */
	protected double daz(int i2, int i1) {
		return daz(azimuth[iaz(i2)], azimuth[iaz(i1)]);
	}

	/**
	 * Return az2 - az1 ensuring that the proper rotations occur.
	 * Result will always be >= 0 and < 360
	 * @param az2
	 * @param az1
	 * @return
	 */
	protected double daz(double az2, double az1) {
		return (az2-az1+360.) % 360.;
	}

	/**
	 * Read a String from a binary file. First, read the length of the String
	 * (number of characters), then read that number of characters into the
	 * String.  Fails if the number of characters is &gt; maxLength
	 * 
	 * @param input
	 *            DataInputStream
	 * @return String
	 * @throws IOException
	 */
	public static String readString(DataInputStream input, int maxLength) throws IOException
	{
		int n=input.readInt();
		if (n == 0) return "";
		if (n > maxLength)
			throw new IOException(String.format("readString failed because nChar=%d is > than maxLength=%d",
					n, maxLength));
		byte[] bytes = new byte[n];
		input.readFully(bytes);
		return new String(bytes);
	}

	@Override
	public void vtk(File outputFile) throws Exception {
		int nr = (int) Math.ceil(180./delta[0])+1;
		int naz = azimuth.length;

		double dazimuth = azimuth[1]-azimuth[0];

		ArrayList<double[]> points = new ArrayList<>((naz+1)*(nr+1));
		ArrayList<float[]> data = new ArrayList<float[]>((naz+1)*(nr+1));
		for (int j=0; j<nr; ++j) {
			for (int i=0; i<=naz; ++i) {
				double[] u = VectorGeo.move(center, Math.toRadians(Math.max(180., j*delta[0])), Math.toRadians(i*dazimuth));
				points.add(u);
				EnumMap<GeoAttributes, Double> attributes = interpolate(u);
				if (attributes.containsKey(GeoAttributes.TRAVEL_TIME))
					data.add(new float[] {
							attributes.get(GeoAttributes.TRAVEL_TIME).floatValue(),
							attributes.get(GeoAttributes.TT_MODEL_UNCERTAINTY).floatValue(),
							attributes.get(GeoAttributes.SLOWNESS_DEGREES).floatValue()		
					});
				else
					data.add(new float[] {Float.NaN, Float.NaN, Float.NaN});
			}
		}

		ArrayList<VTKCell> cells = new ArrayList<>(naz*nr);
		for (int r=0; r<nr-1; ++r) {
			for (int a=0; a<naz; ++a) {
				int[] i = new int[] {
						r*(naz+1)+a,
						r*(naz+1)+a+1,
						(r+1)*(naz+1)+a+1,
						(r+1)*(naz+1)+a
				};				
				VTKCell cell = new VTKCell(VTKCellType.VTK_POLYGON, i);
				cells.add(cell);
			}
		}

		outputFile.getParentFile().mkdir();
		VTKDataSet.write(outputFile, points, cells, Arrays.asList(new String[] {"travel_time", "uncertainty", "slowness"}),  data); 
	}

	//	@Override
	//	public void vtk(File outputFile) throws Exception {
	//		// ASSUMPTION: all the delta[i] are equal for all azimuths
	//		int nr = (int) Math.ceil(180./delta[0]);
	//		int naz = azimuth.length+1;
	//		double[][][] vectors = new double[naz][nr][];
	//		float[][] t = new float[naz][nr];
	//		float[][] u = new float[naz][nr];
	//		for (int a=0; a<naz; ++a) {
	//			double az = toRadians(azimuth[iaz(a)]);
	//			vectors[a][0] = center;
	//			for (int r=1; r<nr; ++r) {
	//				vectors[a][r] = VectorUnit.move(center, toRadians(r*delta[0]), az);
	//				if (r < nRadii(a)) {
	//					t[a][r] = (float)tt[iaz(a)][r-1];
	//					u[a][r] = (float)uncertainty[iaz(a)][r-1];
	//				}
	//				else {
	//					t[a][r] = Float.NaN;
	//					u[a][r] = Float.NaN;
	//				}
	//			}
	//		}
	//
	//		ArrayList<double[]> points = new ArrayList<>(naz*nr);
	//		ArrayList<float[]> data = new ArrayList<float[]>(naz*nr);
	//		for (int r=0; r<nr; ++r) {
	//			for (int a=0; a<naz; ++a) {
	//				points.add(vectors[a][r]);
	//				EnumMap<GeoAttributes, Double> values = interpolate(vectors[a][r]);
	//				if (values.containsKey(GeoAttributes.TRAVEL_TIME)) 
	//					data.add(new float[] {values.get(GeoAttributes.TRAVEL_TIME).floatValue(), 
	//							values.get(GeoAttributes.TT_MODEL_UNCERTAINTY).floatValue(), 
	//							values.get(GeoAttributes.SLOWNESS_DEGREES).floatValue()});
	//				else 
	//					data.add(new float[] {Float.NaN, Float.NaN, Float.NaN});
	//
	//			}
	//		}
	//
	//		ArrayList<VTKCell> cells = new ArrayList<>(naz*nr);
	//		for (int a=0; a<naz-1; ++a) {
	//			for (int r=0; r<nr-1; ++r) {
	//				int[] i = new int[] {
	//						r*naz+a,
	//						r*naz+a+1,
	//						(r+1)*naz+a+1,
	//						(r+1)*naz+a
	//				};				
	//				VTKCell cell = new VTKCell(VTKCellType.VTK_POLYGON, i);
	//				cells.add(cell);
	//			}
	//		}
	//
	//		outputFile.getParentFile().mkdir();
	//		VTKDataSet.write(outputFile, points, cells, Arrays.asList(new String[] {"travel_time", "uncertainty"}),  data); 
	//	}


	@Override
	public String toString() {
		double lat = VectorGeo.getLatDegrees(center);
		double lon = VectorGeo.getLonDegrees(center);
		return String.format("%-6s %-6s %10.5f, %11.5f %s", 
				name(), season(), lat, lon,FlinnEngdahlCodes.getGeoRegionName(lat, lon));
	}

	/**
	 * Retrieve a reference to a Receiver object constructed using only information 
	 * in this Radial2D object;
	 * @return
	 * @throws Exception
	 */
	@Override
	public Receiver getReceiver() throws Exception {
		double lat = VectorGeo.getLatDegrees(center);
		double lon = VectorGeo.getLonDegrees(center);
		Receiver receiver = new Receiver(name(), new GeoVector(lat, lon, 0, true));
		receiver.setStaName((FlinnEngdahlCodes.getGeoRegionName(lat, lon)+" ("+season()+")").toLowerCase());
		return receiver;
	}

	@Override
	public double delta() {
		// ASSUMPTION: delta is constant for all azimuths!
		return delta[0];
	}

	public int nRadii(int a) { return tt[iaz(a)].length; }

	@Override
	public double dazimuth() {
		// ASSUMPTION: azimuth intervals are all equal size!
		return azimuth[1]-azimuth[0];
	} 

	/**
	 * hashCode is based on the hashCode of the canonical input File.
	 * @return
	 */
	@Override
	public int hashCode() {
		return inputFile.hashCode();
	}

	/**
	 * Equality is based on the equality of the canonical input File.
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		return inputFile.equals(obj);
	}

	/**
	 * Copyright (c) 1994-1997 Science Applications International Corporation.
	 *

	 * NAME
	 *	ellip_dist -- Find distance and azimuth between 2 points on an ellipsoid.

	 * FILE
	 *	ellip_dist.c

	 * SYNOPSIS
	 *	int
	 *	ellip_dist (lat1, lon1, lat2, lon2, dist, faz, baz, flag)
	 *	double	lat1;		(i) Geographic latitude of point 1 (deg)
	 *	double	lon1;		(i) Geographic longitude of point 1 (deg)
	 *	double	lat2;		(i) Geographic latitude of point 2 (deg)
	 *	double	lon2;		(i) Geographic longitude of point 2 (deg)
	 *	double	*dist;		(o) distance in between 1 & 2 (deg/km)
	 *	double	*faz;		(o) azimuth from north of 2 w.r.t. 1 
	 *				    (clockwise; deg)
	 *	double	*baz;		(o) Geocentric back-azimuth from north (deg)
	 *	int	flag		(i) Flag specifying if dist should be in
	 *				    psuedo-degrees (0) or km (1)

	 * DESCRIPTION
	 *   Function ellip_dist. 
	 *
	 *	Finds the distance, azimuth and backazimuth between two lat/lon points
	 *	along the surface of a reference ellipsoid.
	 *
	 *	If flag is false than the distance is returned in degrees
	 *	by setting 1 deg = 111.12 km (60 nautical miles * 1.852 km/nm)
	 *	This means the distance between the equator and the north pole
	 *	is not 90 degrees, but instead 90.01039 degrees
	 *
	 *	uses GRS80 / WGS84 (NAD83) earth model (ellipsoid)
	 *
	 *	The meat of the algorithm was taken from the code "inverse.for"
	 *	written by L.Pfeifer and John G Gergen of the National Geodetic
	 *	Survey (NGS), Rockville MD
	 *
	 *	The algorithm follows T. Vincenty's modified Rainsford's method 
	 *	with Helmert's elliptical terms.
	 *
	 * DIAGNOSTICS
	 *	
	 *	Returns  1 on successful completion
	 *	Returns  0 if invalid latitudes are entered (i.e. fabs(lat) > 90)
	 *	Returns -1 if the two points are identical (sets dist=0,faz=0,baz=180)
	 *	Returns -2 if fails to converge (computes dist,faz and baz using dist_azimuth)
	 *
	 * NOTES
	 *
	 * SEE ALSO
	 *	dist_azimuth.c
	 *
	 * AUTHOR
	 *	Jeffrey A. Hanson, August 1999
	 * 
	 * @param lat1 latitude of first point in degrees
	 * @param lon1 longitude of first point in degrees
	 * @param lat2 latitude of second point in degrees
	 * @param lon2 longitude of second point in degrees
	 * @param flag if true, distance reported in km, if false, in degrees
	 * @return 3-element array containing distance, azimuth from first to second point in degrees
	 * and azimuth from second point to fist point in degrees.ß
	 * @throws Exception
	 */
	private double[] ellip_dist(double lat1, double lon1, double lat2, double lon2, boolean flag) throws Exception {

		/* we are defining the number of km in a degree such that 
		 * 1 degree = 60 nautical miles which is somewhat different 
		 * than using the earth's average radius (i.e. 111.195) */

		double KM_PER_DEG = 111.12; 
		double DEG_PER_RAD = 57.295779513082321004;

		double CONVERGE = 0.5e-13;
		int	INF_LOOP = 100;

		/* GRS80 or WGS84 (NAD83) earth model (ellipsoid) */
		/* equatorial radius (meters) */
		double EQ_RADIUS= 6378137.0;
		/* earth flattening = (EQ_RADIUS-POLAR_RADIUS)/EQ_RADIUS */
		double FLATTENING = 1.0/298.25722210088;

		double	dist;
		//		double	faz;
		//		double	baz;

		int	cnt		= 0;

		double	a		= EQ_RADIUS;
		double	f		= FLATTENING;

		double	r, tu1, tu2, cu1, su1, cu2, s;
		double	d, x, sx, cx, sy, cy, y, sa, c2a, cz, e, c ;
		double	fazi, bazi;

		/* can't be at earth's pole of rotation */
		if (lat1 > 90.0 || lat1 < -90.0) {
			//			fprintf(stderr,"%s: invalid latitude\n",fname);
			//			goto RETURN;
			throw new Exception(String.format("invalid latitude"));
		} else if (lat1 > 89.9999) {
			lat1 = 89.9999;
		} else if (lat1 < -89.9999) {
			lat1 = -89.9999;
		}
		if (lat2 > 90.0 || lat2 < -90.0) {
			//			fprintf(stderr,"%s: invalid latitude\n",fname);
			//			goto RETURN;
			throw new Exception(String.format("invalid latitude"));
		} else if (lat2 > 89.9999) {
			lat2 = 89.9999;
		} else if (lat2 < -89.9999) {
			lat2 = -89.9999;
		}

		/* make longitudes run between -180 and 180 (not really necessary but makes
		   it easier to test if the points are at the same location)
		 */
		//		if (lon1 < -180.0) lon1 += 360.0;
		//		if (lon1 >  180.0) lon1 -= 360.0;
		//		if (lon2 < -180.0) lon1 += 360.0;
		//		if (lon2 >  180.0) lon1 -= 360.0;
		lon1 = (lon1 + 360.) % 360.;
		lon2 = (lon2 + 360.) % 360.;

		/* check if points are equal */
		if (lat1==lat2 && lon1==lon2) {
			return new double[] { 0., Double.NaN, Double.NaN };
		}

		/* convert lat and lon from degrees into radians */
		lat1 /= DEG_PER_RAD;
		lon1 /= DEG_PER_RAD;
		lat2 /= DEG_PER_RAD;
		lon2 /= DEG_PER_RAD;

		/*
		C *** SOLUTION OF THE GEODETIC INVERSE PROBLEM AFTER T.VINCENTY
		C *** MODIFIED RAINSFORD'S METHOD WITH HELMERT'S ELLIPTICAL TERMS
		C *** EFFECTIVE IN ANY AZIMUTH AND AT ANY DISTANCE SHORT OF ANTIPODAL
		C *** STANDPOINT/FOREPOINT MUST NOT BE THE GEOGRAPHIC POLE
		C
		C *** A IS THE SEMI-MAJOR AXIS OF THE REFERENCE (EARTH) ELLIPSOID
		C *** F IS THE FLATTENING (NOT RECIPROCAL) OF THE REFERNECE ELLIPSOID
		C *** LATITUDES AND LONGITUDES IN RADIANS POSITIVE NORTH AND EAST
		C *** FORWARD AZIMUTHS AT BOTH POINTS RETURNED IN RADIANS FROM NORTH
		C
		C *** PROGRAMMED FOR CDC-6600 BY LCDR L.PFEIFER NGS ROCKVILLE MD 18FEB75
		C *** MODIFIED FOR IBM SYSTEM 360 BY JOHN G GERGEN NGS ROCKVILLE MD 7507
		C
		 */
		/* The following code is ugly because it's converted from fortran code 
		   written in 1975. */

		r 	= 1.0 - f;
		tu1 	= r*sin(lat1)/cos(lat1);
		tu2 	= r*sin(lat2)/cos(lat2);
		cu1 	= 1.0/sqrt(tu1*tu1+1.0);
		su1 	= cu1*tu1;
		cu2 	= 1.0/sqrt(tu2*tu2+1.0);
		s 	= cu1*cu2;
		bazi 	= s*tu2;
		fazi	= bazi*tu1;

		x	= lon2-lon1;

		do {

			sx	= sin(x);
			cx	= cos(x);
			tu1	= cu2 * sx;
			tu2	= bazi-su1*cu2*cx;
			sy	= sqrt(tu1*tu1+tu2*tu2);
			cy	= s*cx+fazi;
			y	= atan2(sy,cy);
			sa	= s*sx/sy;
			c2a	= 1.0-sa*sa;
			cz	= fazi+fazi;
			if (c2a > 0.0) cz = cy-cz/c2a;
			e	= 2.0*cz*cz-1.0;
			c	= ((-3.0*c2a+4.0)*f+4.0)*c2a*f/16.0;
			d	= x;
			x	= ((e*cy*c+cz)*sy*c+y)*sa;
			x	= (1.0-c)*x*f+lon2-lon1;

			/* if points are near anti-podes this may fail to converge 
			 * We'll stop the loop and use the more robust but less
			 * accurate dist_azimuth routine 
			 */
			if (++cnt > INF_LOOP) {
				//				fprintf(stderr,"%s: Failed to converge, using 'dist_azimuth' instead\n",fname);
				//				dist_azimuth(lat1*DEG_PER_RAD,lon1*DEG_PER_RAD,
				//					     lat2*DEG_PER_RAD,lon2*DEG_PER_RAD,
				//					     &s,&fazi,&bazi,0);
				//
				//				if (flag) {
				//					*dist = s*KM_PER_DEG;
				//				} else {
				//					*dist = s;
				//				}
				//				*faz  = fazi;
				//				*baz  = bazi;
				//				success = -2;
				//
				//				goto RETURN;
				double[] v1 = VectorGeo.getVector(lat1, lon1);
				double[] v2 = VectorGeo.getVector(lat2, lon2);
				return new double[] { VectorGeo.angleDegrees(v1,v2), VectorGeo.azimuthDegrees(v1, v2, Double.NaN), 
						VectorGeo.azimuthDegrees(v2, v1, Double.NaN) };
			}
		} while (abs(d-x) > CONVERGE);

		fazi	= atan2(tu1,tu2);
		bazi	= atan2(cu1*sx,bazi*cx-su1*cu2)+PI;
		x	= sqrt((1.0/r/r-1.0)*c2a+1.0)+1.0;
		x	= (x-2.0)/x;
		c	= 1.0-x;
		c	= (x*x/4.0+1.0)/c;
		d	= (0.375*x*x-1.0)*x;
		x	= e*cy;
		s	= 1.0-e*e;
		s	= ((((sy*sy*4.0-3.0)*s*cz*d/6.0-x)*d/4.0+cz)*sy*d+y)*c*a*r;

		/* convert fazi and bazi into degrees */
		fazi *= DEG_PER_RAD;
		bazi *= DEG_PER_RAD;

		fazi = (fazi + 360.) % 360.; // ensure fazi and bazi are in range 0 to 360.
		bazi = (bazi + 360.) % 360.; // ensure fazi and bazi are in range 0 to 360.

		/* put answers into return parameters */
		dist	= s/1000.0;	/* convert distance from meters to km */

		if (!flag)
			// convert distance from km to degrees using constant earth radius (bad idea!)
			dist /= KM_PER_DEG;

		return(new double[] {dist, fazi, bazi});
	}

}
