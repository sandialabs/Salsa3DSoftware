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

import static java.lang.Math.min;
import static java.lang.Math.toRadians;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Objects;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.flinnengdahl.FlinnEngdahlCodes;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.io.GlobalInputStreamProvider;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

public class Radial2DModel {

	protected String inputFile;
	protected String name;
	protected String season;
	protected double lat;
	protected double lon;
	protected double[] center; 
	protected String period;

	protected double[] azimuth;
	protected double[][] radii;	
	protected double[][] tt;
	protected double[][] uncertainty;

	protected double htConvert;
	
	public Radial2DModel() {
		
	}
	
	public Radial2DModel(File inputFile) throws IOException {
		loadModel(inputFile);
	}

	public void loadModel(File inputFile) throws IOException {

		this.inputFile = inputFile.getCanonicalPath();
		season = inputFile.getParentFile().getName();
		name = inputFile.getCanonicalFile().getName();
		htConvert = 0.;

		DataInputStream input = new DataInputStream(new BufferedInputStream(
				GlobalInputStreamProvider.forFiles().newStream(inputFile)));

		lat = input.readFloat();
		lon = input.readFloat();

		center = VectorGeo.getVectorDegrees(lat, lon);

		period = readString(input, 1024);

		ArrayListDouble az = new ArrayListDouble(730);
		ArrayList<double[]> t = new ArrayList<>(361);
		ArrayList<double[]> u = new ArrayList<>(361);
		ArrayList<double[]> r = new ArrayList<>(361);

		while (input.available() > 0) {

			az.add(input.readFloat());

			int n = input.readInt();
			double delt = input.readFloat();

			double[] ri = new double[n];
			double[] ti = new double[n];
			double[] ui = new double[n];

			for (int i=0; i<n; ++i) 
				ti[i] = input.readFloat();
			for (int i=0; i<n; ++i) 
				ui[i] = input.readFloat();				
			for (int i=1; i<n; ++i) 
				ri[i] = ri[i-1]+delt;				

			t.add(ti);
			u.add(ui);
			r.add(ri);
		}

		int naz = az.size();
		azimuth = az.toArray();
		tt = t.toArray(new double[naz][]);
		uncertainty = u.toArray(new double[naz][]);
		radii = r.toArray(new double[naz][]);

		input.close();
	}

	/**
	 * Return interpolated values of requested attributes using bi-linear interpolation.  
	 * Supported attributes include:
	 * TRAVEL_TIME, TT_MODEL_UNCERTAINTY, AZIMUTH, AZIMUTH_DEGREES, DISTANCE, DISTANCE_DEGREES.
	 * @param lat
	 * @param lon
	 * @param inDegrees
	 * @param requestedAttributes
	 * @return
	 */
	public EnumMap<GeoAttributes, Double> interpolate(double lat, double lon, boolean inDegrees, EnumSet<GeoAttributes> requestedAttributes) {
		return interpolate(inDegrees ? VectorGeo.getVectorDegrees(lat, lon) : VectorGeo.getVector(lat, lon), 
				requestedAttributes);
	}

	/**
	 * Return interpolated values of requested attributes using bi-linear interpolation.  
	 * Supported attributes include:
	 * TRAVEL_TIME, TT_MODEL_UNCERTAINTY, AZIMUTH, AZIMUTH_DEGREES, DISTANCE, DISTANCE_DEGREES.
	 * @param v unit vector of location where interpolation should be calculated.
	 * @param requestedAttributes
	 * @return
	 */
	public EnumMap<GeoAttributes, Double> interpolate(double[] v, EnumSet<GeoAttributes> requestedAttributes) {

		EnumMap<GeoAttributes, double[][]> values = new EnumMap<>(GeoAttributes.class);
		
		for (GeoAttributes a : requestedAttributes)
			values.put(a, null);
		
		values.put(GeoAttributes.TRAVEL_TIME, tt);
		values.put(GeoAttributes.TT_MODEL_UNCERTAINTY, uncertainty);
		
		return interpolate(v, values);
	}
	
	/**
	 * Return interpolated values of requested attributes using bi-linear interpolation.  
	 * @param v unit vector of location where interpolation should be calculated.
	 * @param requestedAttributes
	 * @return
	 */
	protected EnumMap<GeoAttributes, Double> interpolate(double[] v, EnumMap<GeoAttributes, double[][]> requestedAttributes) {

		EnumMap<GeoAttributes, Double> values =
				new EnumMap<GeoAttributes, Double>(GeoAttributes.class);

		// populate the return map with Nans.
		for (Entry<GeoAttributes, double[][]> entry : requestedAttributes.entrySet())
			values.put(entry.getKey(), Double.NaN);
		
		// get distance and azimuth from center to interpolation point, both in degrees.
		double r = VectorGeo.angleDegrees(center, v);
		double az = (VectorGeo.azimuthDegrees(center, v, Double.NaN) + 360.) % 360.;

		if (requestedAttributes.containsKey(GeoAttributes.AZIMUTH))
			values.put(GeoAttributes.AZIMUTH, toRadians(az));
		if (requestedAttributes.containsKey(GeoAttributes.AZIMUTH_DEGREES))
			values.put(GeoAttributes.AZIMUTH_DEGREES, az);

		if (requestedAttributes.containsKey(GeoAttributes.DISTANCE))
			values.put(GeoAttributes.DISTANCE, toRadians(r));
		if (requestedAttributes.containsKey(GeoAttributes.DISTANCE_DEGREES))
			values.put(GeoAttributes.DISTANCE_DEGREES, r);

		if (r < toRadians(1e-7)) {
			for (Entry<GeoAttributes, double[][]> entry : requestedAttributes.entrySet())
				if (entry.getValue() != null)
					values.put(entry.getKey(), entry.getValue()[0][0]);
		}
		else {
			int i = Globals.hunt(azimuth, az); 

			if (radii[i].length == 0) return values; 

			int j = Globals.hunt(radii[i], r);

			if (j < radii[i].length-1 && j < radii[iaz(i+1)].length-1) {
				double dx = (azimuth[iaz(i+1)]-az)/(azimuth[iaz(i+1)]-azimuth[i]);
				double dy = (radii[i][j+1]-r)/(radii[i][j+1]-radii[i][j]);
				double c0 = dx*dy;
				double c1 = (1.-dx)*dy;
				double c2 = (1.-dx)*(1.-dy);
				double c3 = dx*(1.-dy);
				for (Entry<GeoAttributes, double[][]> entry : requestedAttributes.entrySet()) {
					GeoAttributes attribute = entry.getKey();
					double[][] dataValues = entry.getValue();
					if (dataValues != null)
						values.put(attribute,  dataValues[i][j] * c0 + dataValues[iaz(i+1)][j]*c1 
							+ dataValues[iaz(i+1)][j+1]*c2 + dataValues[i][j+1]*c3);
				}
			}
		}
		return values;
	}
	
	public boolean testLocation(double[] v) {
		double az = (VectorGeo.azimuthDegrees(center, v, Double.NaN) + 360.) % 360.;
		int i = Globals.hunt(azimuth, az); 
		if (radii[i].length == 0) return false; 
		double r = VectorGeo.angleDegrees(center, v);
		if (r < toRadians(1e-7)) return true;
		int j = Globals.hunt(radii[i], r);
		if (j < radii[i].length-1 && j < radii[iaz(i+1)].length-1) return true;
		return false;
	}
	
	public String inputFile() {
		return inputFile;
	}

	/**
	 * Station name. Same as the connonical file name.
	 * @return
	 */
	public String name() {
		return name;
	}

	/**
	 * The season.  Returns the name of the directory from which this model was read.
	 * @return
	 */
	public String season() {
		return season;
	}

	/**
	 * Latitude of the center of the model in degrees.
	 * @return
	 */
	public double lat() {
		return lat;
	}

	/**
	 * Longitude of the center of the model in degrees.
	 * @return
	 */
	public double lon() {
		return lon;
	}

	/**
	 * Retrieve the earth-centered unit vector of the center of the model.
	 * @return
	 */
	public double[] center() {
		return center;
	}

	/**
	 * Value of <i>period</i> read from the input file.  This is 
	 * supposed to be the <i>season</i>, but is not (at least in some models).
	 * @return
	 */
	public String period() {
		return period;
	}

	/**
	 * The azimuth of each radial spoke, in degrees.
	 * @return
	 */
	public double[] azimuth() {
		return azimuth;
	}

	/**
	 * The travel time at each grid point, in seconds.
	 * @return
	 */
	public double[][] tt() {
		return tt;
	}

	/**
	 * The travel time uncertainty at each grid point, in seconds.
	 * @return
	 */
	public double[][] uncertainty() {
		return uncertainty;
	}

	/**
	 * The distance from the center of the model to each grid point, in degrees.
	 * @return
	 */
	public double[][] radii() {
		return radii;
	}

	/**
	 * returns the value of travel time uncertainty that should be added to the
	 * normal values of tt_uncertainty stored in this model.  This constant value
	 * is retrieved from file <code>time_guide</code> in the directory from which
	 * this model was loaded.  It should only be used for hydroacoustic T phases.
	 * @return
	 */
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
	 * Retrieve azimuth[i] - azimuth[j] ensuring that proper rotations occur.  
	 * Result will always be positive between 0 and 360;
	 * @param i
	 * @param j
	 * @return
	 */
	protected double daz(int i, int j) {
		double daz = azimuth[iaz(i)]-azimuth[iaz(j)];
		return (daz+360.) % 360.;
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

	public double[][][] getVectors() throws Exception {
		double[][][] vectors = new double[azimuth.length][][];
		for (int a=0; a<azimuth.length; ++a) {
			int n = radii[a].length;
			vectors[a] = new double[n][];
			double az = toRadians(azimuth[a]);
			for (int r=0; r<n; ++r)
				vectors[a][r] = VectorGeo.move(center, toRadians(radii[a][r]), az);	
		}
		return vectors;
	}

	public void vtk(File outputFile) throws Exception {

		ArrayList<VTKCell> cells = new ArrayList<>(36100);
		ArrayList<double[]> points = new ArrayList<double[]>(36100);
		
		vtkStructure(cells, points);

		ArrayList<float[]> data = new ArrayList<float[]>(36100);
		for (int i=0; i<=azimuth.length; ++i) {
			for (int j=0; j<radii[i].length; ++j) {
				data.add(new float[] { (float)tt[i][j],  (float)uncertainty[i][j]});
			}
		}

		outputFile.getParentFile().mkdir();
		VTKDataSet.write(outputFile, points, cells, Arrays.asList(new String[] {"travel_time", "uncertainty"}),  data); 
	}
	
	public void vtkStructure(ArrayList<VTKCell> cells, ArrayList<double[]> points) throws Exception {
		double[][][] vectors = getVectors();
		for (int a=0; a<=vectors.length; ++a) {
			int i = iaz(a);
			for (int j=0; j<vectors[i].length; ++j) {
				points.add(vectors[i][j]);
			}
		}

		int rbase=0;
		for (int a=0; a<azimuth.length; ++a) {
			int ll = rbase;
			int lr = rbase+radii[iaz(a)].length;
			int nr = min(radii[iaz(a)].length, radii[iaz(a+1)].length);
			for (int r=0; r<nr-1; ++r) 
				cells.add(new VTKCell(VTKCellType.VTK_QUAD, new int[] {ll+r, lr+r, lr+r+1, ll+r+1}));
			rbase += radii[a].length;
		}
	}


	@Override
	public String toString() {
		return String.format("%-6s %-6s %10.5f, %11.5f %s", 
				name, season, lat, lon,FlinnEngdahlCodes.getGeoRegionName(lat, lon));
	}

	/**
	 * Retrieve a reference to a Receiver object constructed using only information 
	 * in this Radial2D object;
	 * @return
	 * @throws Exception
	 */
	public Receiver getReceiver() throws Exception {
		Receiver receiver = new Receiver(name, new GeoVector(lat, lon, 0, true));
		receiver.setStaName((FlinnEngdahlCodes.getGeoRegionName(lat, lon)+" ("+season+")").toLowerCase());
		return receiver;
	}

	@Override
	public int hashCode() {
		return Objects.hash(inputFile);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return Objects.equals(inputFile, ((Radial2DModel) obj).inputFile);
	}

}
