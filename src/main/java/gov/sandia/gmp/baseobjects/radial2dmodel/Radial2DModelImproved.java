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

import static java.lang.Math.toRadians;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.flinnengdahl.FlinnEngdahlCodes;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.io.GlobalInputStreamProvider;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

public class Radial2DModelImproved implements Radial2DModel {

	/**
	 * The canonical file from which this model was read.
	 */
	protected File inputFile;

	/**
	 * Unit vector of the location at the center of the model.
	 */
	protected final double[] center; 

	/**
	 * Period name.  Corresponds to the name of a month or a season.
	 */
	protected String period;

	/**
	 * The number of azimuths in the model (number of spokes).
	 */
	int naz;

	/**
	 * Azimuthal spacing of grid nodes in degrees (assumed constant; if not throw exception).
	 */
	protected double dazimuth;

	/**
	 * Radial spacing of grid nodes in degrees (assumed constant; if not throw exception).
	 */
	protected double delta;

	/**
	 * travel time at each azimuth, radius, in seconds.
	 * Starts at radius=0 where tt[0] = 0;
	 */
	protected double[][] tt;

	/**
	 * travel time uncertainty at each azimuth, radius, in seconds
	 * Starts at radius=0 where incertainty[0] = 0;
	 */
	protected double[][] uncertainty;

	/**
	 * travel time uncertainty that should be added to the
	 * normal values of tt_uncertainty stored in this model.  This constant value
	 * is retrieved from file <code>time_guide</code> in the directory from which
	 * this model was loaded.  It should only be used for hydroacoustic T phases.
	 */
	protected double htConvert;

	private int[] nrBuffered;

	private int[] nrUnbuffered;

	public Radial2DModelImproved() {
		this.center = null;
	}

	public Radial2DModelImproved(File f) throws IOException {

		this.inputFile = f.getCanonicalFile();
		htConvert = 0.;

		DataInputStream input = new DataInputStream(new BufferedInputStream(
				GlobalInputStreamProvider.forFiles().newStream(this.inputFile)));

		//		Here is the format for the radial_2D tables.
		//		"Period" refers to the name of the season for 
		//		the table.  These should be all caps (SUMMER, 
		//		SPRING, AUTUMN, WINTER)
		//
		//
		//
		//		float           station latitude
		//		float           longitude
		//		int             period name length
		//		char            period[period name length]
		//
		//
		//		 
		//		[Repeated for each azimuth:]
		//		    float           azimuth
		//		    int             nrad (number of data points along this azimuth)
		//		    float           delta_rad (distance between data points in deg)
		//		    float	    travel_time[nrad]
		//		    float	    modeling_error[nrad] 

		center = EarthShape.WGS84.getVectorDegrees(input.readFloat(), input.readFloat());

		// period will be the name of a month, or a season.
		period = readString(input, 1024);

		ArrayList<ArrayListDouble> t = new ArrayList<>(720);
		ArrayList<ArrayListDouble> u = new ArrayList<>(720);

		// the maximum number nodes in the radial direction.
		int nr_max = 0;

		// the number of azimuthal spokes.  The first will be at azimuth = 0
		// and the last will be at azimuth 360-dazimuth.
		naz = 0;

		// dazimuth is the azimuthal spacing in degrees.  Must be contant.
		dazimuth = Double.NaN;

		// the previous and current azimuth values.
		double az1=0, az2;

		// delta is the spacing in the radial dimension, in degrees.  Must be constant.
		delta = Double.NaN;

		// while there are more azimuthal spokes available
		while (input.available() > 0) {

			++naz;
			az2 = input.readFloat();

			if (naz == 2)  // if two spokes have been read
				dazimuth = az2-az1;
			else if (naz > 2 && az2-az1 != dazimuth) // if more than two spokes have been read
				// if the azimuth spacing is not constant, throw exception
				throw new IOException("dazimuth is not contant!");

			az1 = az2;

			// read the number of tt values along this spoke, not counting the first
			// tt value which is zero
			int n = input.readInt();

			// make sure that the radial spacing is constant.  if not, throw exception
			double dr = input.readFloat();
			if (Double.isNaN(delta))
				delta = dr;
			else if (dr != delta)
				throw new IOException("delta is not contant!");

			// allocate 1D arrays for travel time and uncertainty along this spoke
			ArrayListDouble ti = new ArrayListDouble(n+1);
			ArrayListDouble ui = new ArrayListDouble(n+1);

			// add zero travel time and zero uncertainty at the beginning.
			ti.add(0F);
			ui.add(0F);

			// read in tt and uncertainty values along this spoke, length of the arrays will be n+1
			for (int i=0; i<n; ++i) 
				ti.add(input.readFloat());
			for (int i=0; i<n; ++i) 
				ui.add(input.readFloat());

			// add the 1D arrays to the 2D arrays.
			t.add(ti);
			u.add(ui);

			// if the number of nodes along this spoke exceeds max value, 
			// update max value.
			if (ti.size() > nr_max)
				nr_max = ti.size();

		}

		// done reading data.
		input.close();

		// Now we will populate extra nodes along each spoke if the neighboring 
		// nodes are populated.  The idea is that if a neighboring node (at same
		// radial distance but neighboring azimuth) is populated, then populate 
		// the node with average of values copied from neighbors.

		// we will use 75 sec/degree to extrapolate hydro traveltimes one grid interval
		// in the radial direction.
		double tt_delta_hydro = delta * 75.;

		// first, save the length of each radial profile prior to addition of buffered nodes.
		// This will be used to make vtk plots that exclude the buffered nodes.
		nrUnbuffered = new int[naz];
		for (int i=0; i<naz; ++i)
			nrUnbuffered[i] = t.get(i).size();

		// Container in which to store node value changes.  The key is the azimuth
		// index, and the values are the changes to tt and uncertainty along that 
		// spoke in order of increasing radial distance.
		LinkedHashMap<Integer, ArrayList<double[]>> changes = new LinkedHashMap<>();

		// iterate over all the spokes
		for (int i = 0; i < naz; ++i) {

			// determine the indexes of the previous and next spokes.
			int iminus1 = iaz(i-1);
			int iplus1 = iaz(i+1);

			ArrayList<double[]> chngs = new ArrayList<double[]>();
			changes.put(i, chngs);

			// iterate from the first node past the end of the spoke to the 
			// maximum number nodes in all the spokes plus 1
			for (int j=t.get(i).size(); j<=nr_max; ++j) {

				// instantiate some small arrays to hold neighboring values
				ArrayListDouble tvalues = new ArrayListDouble(3);
				ArrayListDouble uvalues = new ArrayListDouble(3);

				// if the previous spoke has values at this radial distance add its values
				if (t.get(iminus1).size() > j) {
					tvalues.add(t.get(iminus1).get(j));
					uvalues.add(u.get(iminus1).get(j));
				}

				// if the next spoke has values at this radial distance, add, its values
				if (t.get(iplus1).size() > j) {
					tvalues.add(t.get(iplus1).get(j));
					uvalues.add(u.get(iplus1).get(j));
				}

				// if this spoke has a value at the previous radial distance
				// extrapolate its value and add to list
				if (j == t.get(i).size() && j > 0) {
					tvalues.add(t.get(i).get(j-1) + tt_delta_hydro);
					uvalues.add(u.get(i).get(j-1));
				}

				// if we have not found a populated node yet, search a little further afield.
				if (tvalues.size() ==  0) {
					// if this spoke has a value at the previous azimuth and previous radial distance
					// extrapolate its value and add to list
					if (j <= t.get(iminus1).size() && j > 0) {
						tvalues.add(t.get(iminus1).get(j-1) + tt_delta_hydro);
						uvalues.add(u.get(iminus1).get(j-1));
					}
					// if this spoke has a value at the next azimuth and previous radial distance
					// extrapolate its value and add to list
					if (j <= t.get(iplus1).size() && j > 0) {
						tvalues.add(t.get(iplus1).get(j-1) + tt_delta_hydro);
						uvalues.add(u.get(iplus1).get(j-1));
					}
				}

				// if no neighboring values are available then this node and all subsequent
				// nodes along the current spoke cannot be populated so break and move on to next spoke.
				if (tvalues.size() ==  0) 
					break;
				
				// find the average of the traveltime at neighboring nodes and the maximum
				// uncertainty at the neighboring nodes and add the new 
				// values to list of changes along the current spoke.
				double[] vals = new double[2];
				for (int k=0; k<tvalues.size(); ++k) {
					vals[0] += tvalues.get(k);
					vals[1] += uvalues.get(k);
				}
				vals[0] /= tvalues.size();
				vals[1] /= uvalues.size();
				
				chngs.add(vals);
			}
		}

		// after changes have been determined along all spokes, apply the changes to the model.
		// Note that uncertainty of extrapolated nodes is being increased significantly!
		for (Entry<Integer, ArrayList<double[]>> entry : changes.entrySet()) {
			for (double[] x : entry.getValue()) {
				t.get(entry.getKey()).add(x[0]);
				u.get(entry.getKey()).add(x[1]+30F);	
			}
		}

		nrBuffered = new int[naz];
		for (int i=0; i<naz; ++i)
			nrBuffered[i] = t.get(i).size();

		// copy the values from ArrayListDoubles into standard 2D arrays.
		tt = new double[naz][];
		uncertainty = new double[naz][];

		for (int i=0; i<naz; ++i) {
			tt[i] = t.get(i).toArray();
			t.set(i, null);
			uncertainty[i] = u.get(i).toArray();
			u.set(i, null);
		}


		// done with constructor
	}

	/**
	 * Retrieve a map of attribute values.  The map will always contain
	 * distance, distance_degrees, azimuth and azimuth_degrees.
	 * If not blocked, values will also contain travel_time, tt_model_uncertainty,
	 * slowness and slowness_degrees.  
	 * Slowness is computed as travel time divided by distance.
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
	 * If not blocked, map will also contain travel_time, tt_model_uncertainty,
	 * slowness and slowness_degrees.  
	 * Slowness is computed as travel time divided by distance.
	 * @param v unit vector of location where interpolation should be calculated.
	 * @return map of attribute values.  If map does not include travel_time then 
	 * ray path was blocked.
	 * @throws Exception 
	 */
	@Override
	public EnumMap<GeoAttributes, Double> interpolate(double[] v) {

		// get distance and azimuth from center to interpolation point, both in degrees.
		double r = VectorUnit.angleDegrees(center, v);
		double seaz = VectorUnit.azimuthDegrees(center, v, 0.);

		EnumMap<GeoAttributes, Double> values = interp(r, seaz);

		// if raypath was blocked, try going the other way around the world.
		if (!values.containsKey(GeoAttributes.TRAVEL_TIME)) {
			EnumMap<GeoAttributes, Double> values2 = interp(360.-r, seaz+180.);
			if (values2.containsKey(GeoAttributes.TRAVEL_TIME))
				return values2;
		}
		return values;
	}

	/**
	 * Retrieve a map of attribute values.  The map will always contain
	 * distance, distance_degrees, azimuth and azimuth_degrees.
	 * If not blocked, values will also contain travel_time, tt_model_uncertainty,
	 * slowness and slowness_degrees. 
	 * Slowness is computed as travel time divided by distance.

	 *
	 * @param r receiver-source distance in degrees.
	 * @param seaz receiver-source azimuth in degrees. 
	 * @return map of attribute values.  If map does not include travel_time then 
	 * ray path was blocked.
	 */
	private EnumMap<GeoAttributes, Double> interp(double r, double seaz) {

		EnumMap<GeoAttributes, Double> values = new EnumMap<GeoAttributes, Double>(GeoAttributes.class);

		seaz = (seaz+360.) % 360.;

		values.put(GeoAttributes.AZIMUTH, toRadians(seaz));
		values.put(GeoAttributes.AZIMUTH_DEGREES, seaz);

		values.put(GeoAttributes.DISTANCE, toRadians(r));
		values.put(GeoAttributes.DISTANCE_DEGREES, r);

		int iaz1 = (int)(seaz/dazimuth);
		int iaz2 = iaz(iaz1+1);
		int ir1 = (int)(r/delta);
		int ir2 = ir1+1;

		if (ir2 < tt[iaz1].length && ir2 < tt[iaz2].length) {
			double cr = r/delta - ir1;
			double ca = seaz/dazimuth - iaz1;

			double t = tt[iaz1][ir1]*(1.-ca)*(1.-cr)
					+ tt[iaz2][ir1]*ca*(1.-cr)
					+ tt[iaz1][ir2]*(1.-ca)*cr
					+ tt[iaz2][ir2]*ca*cr;

			double u = uncertainty[iaz1][ir1]*(1-ca)*(1-cr)
					+ uncertainty[iaz2][ir1]*ca*(1-cr)
					+ uncertainty[iaz1][ir2]*(1-ca)*cr
					+ uncertainty[iaz2][ir2]*ca*cr;

			values.put(GeoAttributes.TRAVEL_TIME, t);
			values.put(GeoAttributes.TT_MODEL_UNCERTAINTY, u);

			if (r > 0.) {
				values.put(GeoAttributes.SLOWNESS_DEGREES, t/r); // sec/degree
				values.put(GeoAttributes.SLOWNESS, t/Math.toRadians(r)); // sec/radian
			}
			else {
				double t1 = tt[iaz1][ir1]*(1-ca) + tt[iaz2][ir1]*ca;
				double t2 = tt[iaz1][ir2]*(1-ca) + tt[iaz2][ir2]*ca;

				values.put(GeoAttributes.SLOWNESS_DEGREES, (t2-t1)/delta); // sec/degree
				values.put(GeoAttributes.SLOWNESS, (t2-t1)/Math.toRadians(delta)); // sec/radian
			}



		}

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
		az = (az + 360.) % 360.;
		int a1 = (int)(az/dazimuth);
		int a2 = iaz(a1+1);
		return delta * Math.min(tt[a1].length, tt[a2].length);
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
	 * Retrieve a reference to the earth-centered unit vector of the center of the model.
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
	protected int iaz(int i) { return (i+naz) % naz; }

	/**
	 * Retrieve azimuth[i2] - azimuth[i1] ensuring that proper rotations occur.  
	 * Result will always be >= 0 and < 360
	 * @param i2
	 * @param i1
	 * @return
	 */
	protected double daz(int i2, int i1) {
		return daz(iaz(i2)*dazimuth, iaz(i1)*dazimuth);
	}

	/**
	 * Return az2 - az1 ensuring that the proper rotations occur.
	 * Result will always be >= 0 and < 360
	 * @param az2
	 * @param az1
	 * @return
	 */
	protected double daz(double az2, double az1) {
		return (az2-az1+3600.) % 360.;
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
		int nr = (int)Math.ceil(180./delta);
		ArrayList<double[]> points = new ArrayList<>((naz+1)*(nr+1));
		ArrayList<float[]> data = new ArrayList<>((naz+1)*(nr+1));
		for (int j=0; j<nr; ++j) {
			for (int i=0; i<=naz; ++i) {
				double[] u = VectorGeo.move(center, Math.toRadians(j*delta), Math.toRadians(i*dazimuth));
				points.add(u);
				EnumMap<GeoAttributes, Double> attributes = interp(j*delta, i*dazimuth);
				Double slowness = attributes.get(GeoAttributes.SLOWNESS_DEGREES);
				if (j < nrBuffered[iaz(i)])
					data.add(new float[] {
							(float) tt[iaz(i)][j],
							(float) uncertainty[iaz(i)][j],
							slowness == null ? Float.NaN : slowness.floatValue()
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
				VTKCell cell = new VTKCell(VTKCellType.VTK_QUAD, i);
				cells.add(cell);
			}
		}

		outputFile.getParentFile().mkdir();
		VTKDataSet.write(outputFile, points, cells, Arrays.asList(new String[] {"travel_time", "uncertainty", "slowness"}),  data); 
	}


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
	public Receiver getReceiver() throws Exception {
		double lat = VectorGeo.getLatDegrees(center);
		double lon = VectorGeo.getLonDegrees(center);
		Receiver receiver = new Receiver(name(), new GeoVector(lat, lon, 0, true));
		receiver.setStaName((FlinnEngdahlCodes.getGeoRegionName(lat, lon)+" ("+season()+")").toLowerCase());
		return receiver;
	}


	@Override
	public double delta() {
		return delta;
	}

	@Override
	public double dazimuth() {
		return dazimuth;
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

}
