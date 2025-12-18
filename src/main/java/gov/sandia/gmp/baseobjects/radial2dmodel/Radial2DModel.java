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

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.flinnengdahl.FlinnEngdahlCodes;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.io.GlobalInputStreamProvider;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.GeoMath;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

public class Radial2DModel {

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
	 * There are two ways to compute distance, azimuth and backazimuth: VINCENTY and SNYDER.
	 * VINCENTY is incorrect and SNYDER is correct.  SNYDER is the method used by EarthShape
	 * and VectorGeo classes.  References are 
	 * <p>Snyder, J. P., Map Projections – A Working Manual, USGS Prof. Paper 1395, 1987.
	 * <p>Vincenty, T., Survey Review, 23, No 176, p 88-93, 1975
	 * <p>See also: Ballard, S., Manipulation of Geographic Information in Global Seismology
	 */
	//public enum DISTANCE_AZIMUTH_METHOD { VINCENTY, SNYDER };

	public Radial2DModel() {
		this.center = null;
	}

	public Radial2DModel(File inputFile) throws IOException {
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
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Prediction getPrediction(PredictionRequest request) throws Exception {

		// The receiver stored in the request is not used for calculations.
		// The receiver location stored in the model is used.
		
		// ASSUMPTION: the radial2d models were constructed assuming WGS84 ellipsoid.  Unverified!
		double[] event = EarthShape.WGS84.getVector(request.getSource().getLat(), request.getSource().getLon());
		double distance = GeoMath.angleDegrees(center, event);
		double seaz = (GeoMath.azimuthDegrees(center, event, 0.) +360.) % 360.;
		
		Prediction prediction = getPrediction(request, distance, seaz);
		
		if (prediction.getAttributeBoolean(GeoAttributes.BLOCKED)) {
			Prediction prediction2 = getPrediction(request, 360.-distance, (seaz+180.) % 360.); 
			if (!prediction2.getAttributeBoolean(GeoAttributes.BLOCKED))
				prediction = prediction2;
		}
		return prediction;
	}

	/**
	 * Retrieve a map of attribute values.  The map will always contain
	 * distance, distance_degrees, azimuth and azimuth_degrees.
	 * If not blocked, values will also contain travel_time, tt_model_uncertainty,
	 * slowness and slowness_degrees.  Slowness is computed as travel_time/distance.
	 *
	 * @param distance receiver-source distance in degrees.
	 * @param seaz receiver-source azimuth in degrees.
	 * @return map of attribute values.  
	 */
	private Prediction getPrediction(PredictionRequest request, double distance, double seaz) {

		Prediction prediction = new Prediction(request, 
				request.getPhase() == SeismicPhase.H || request.getPhase() == SeismicPhase.T
				? PredictorType.HYDRO_RADIAL2D : PredictorType.INFRASOUND_RADIAL2D);
		
		prediction.setAttribute(GeoAttributes.DISTANCE, toRadians(distance));
		prediction.setAttribute(GeoAttributes.DISTANCE_DEGREES, distance);

		prediction.setAttribute(GeoAttributes.AZIMUTH, toRadians(seaz));
		prediction.setAttribute(GeoAttributes.AZIMUTH_DEGREES, seaz);

		int iaz = Globals.hunt(azimuth, seaz);

		double[][] ttime = new double[2][2];
		double[][] error = new double[2][2];
		double[] dist_interp = new double[2];

		int nblocked = 0;

		for (int k=0; k<2; ++k) {
			int kaz = iaz(iaz+k);
			int nrad = tt[kaz].length;
			double delta_rad = delta[iaz(kaz)];
			double dist_deltarad = distance / delta_rad;
			int d = (int)(dist_deltarad); // first index in tt table where tt[i] is >= r
			for (int l=0; l<2; ++l) {
				int steps_beyond_table = d+l - nrad;
				if (steps_beyond_table > 0) {
					++nblocked;
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

		prediction.setAttribute(GeoAttributes.TT_BASEMODEL, travel_time);
		prediction.setAttribute(GeoAttributes.TRAVEL_TIME, travel_time);

		prediction.putUncertaintyType(GeoAttributes.TT_MODEL_UNCERTAINTY, 
				GeoAttributes.TT_MODEL_UNCERTAINTY_PATH_DEPENDENT);

		prediction.setRayType(request.getPhase() == SeismicPhase.H || request.getPhase() == SeismicPhase.T
				? RayType.HYDROACOUSTIC_WAVE : RayType.INFRASOUND_WAVE);

		double model_error = (error[0][0]*(1.0-dist_interp[0]) + 
				error[0][1]*dist_interp[0]) * (1.0-azi_interp);
		model_error += (error[1][0]*(1.0-dist_interp[1]) + 
				error[1][1]*dist_interp[1]) * azi_interp;
		
		if (request.getPhase() == SeismicPhase.T)
			model_error += htConvert;

		prediction.setAttribute(GeoAttributes.TT_MODEL_UNCERTAINTY, model_error);

		prediction.setAttribute(GeoAttributes.SLOWNESS, travel_time/toRadians(distance)); // sec/radian
		prediction.setAttribute(GeoAttributes.SLOWNESS_DEGREES, travel_time/distance); // sec/degree

		prediction.setAttributeBoolean(GeoAttributes.BLOCKED, nblocked == 4);
		
		return prediction;
	}

	/**
	 * Return the maximum distance in degrees from the center of the model moving in the direction 
	 * given by azimuth in degrees.  For hydroacoustics, this is blockage.
	 * @param az in degrees
	 * @return max distance in degrees
	 */
	public double getMaxDistance(double az) {
		int i1 = Globals.hunt(azimuth, ((az + 360.) % 360.)); 
		int i2 = iaz(i1+1);
		return Math.max(delta[i1]*tt[i1].length, delta[i2]*tt[i2].length);
	}

	/**
	 * Retrieve the canonical File from which this model was loaded
	 */
	public File inputFile() {
		return inputFile;
	}

	/**
	 * Station name. Same as the canonical file name.
	 * @return
	 */
	public String name() {
		return inputFile.getName();
	}

	/**
	 * The season.  Returns the name of the directory from which this model was read.
	 * Will be the name of a month, or of a season.
	 * @return
	 */
	public String season() {
		return inputFile.getParentFile().getName();
	}

	/**
	 * Latitude of the center of the model in degrees.
	 * @return
	 */
	public double lat() {
		return GeoMath.getLatDegrees(center);
	}

	/**
	 * Longitude of the center of the model in degrees.
	 * @return
	 */
	public double lon() {
		return GeoMath.getLonDegrees(center);
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
	 * The travel time at each grid point, in seconds.
	 * Dimensions are nAzimuths x nRadii. 
	 * tt()[i][0] is at radius 0 degrees and is equal to 0 seconds
	 * @return
	 */
	public double[][] tt() {
		return tt;
	}

	/**
	 * The travel time uncertainty at each grid point, in seconds.
	 * Dimensions are nAzimuths x nRadii. 
	 * uncertainty()[i][0] is at radius 0 degrees and is equal to 0 seconds
	 * @return
	 */
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

	public void vtk(File outputFile) throws Exception {
		double dr = delta[0]; // radial spacing in degrees; assumed constant!
		int nr = (int) Math.round(180./dr);
		int naz = azimuth.length;

		double dazimuth = azimuth[1]-azimuth[0];

		float t, u;

		ArrayList<double[]> points = new ArrayList<>((naz+1)*(nr+1));
		ArrayList<float[]> data = new ArrayList<float[]>((naz+1)*(nr+1));

		for (int j=1; j<=nr; ++j) {
			for (int i=0; i<=naz; ++i) {

				double[] v = GeoMath.move(center, Math.toRadians(Math.min(180., j*dr)), Math.toRadians(i*dazimuth));
				points.add(v);

				if (j < tt[i%naz].length) {
					t = (float) tt[i%naz][j];
					u = (float) uncertainty[i%naz][j];
				}
				else
					t = u = Float.NaN;

				data.add(new float[] {t,u});
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
		VTKDataSet.write(outputFile, points, cells, Arrays.asList(new String[] {"travel_time", "uncertainty"}),  data); 
	}


	@Override
	public String toString() {
		double lat = GeoMath.getLatDegrees(center);
		double lon = GeoMath.getLonDegrees(center);
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
		double lat = GeoMath.getLatDegrees(center);
		double lon = GeoMath.getLonDegrees(center);
		Receiver receiver = new Receiver(name(), new GeoVector(lat, lon, 0, true));
		receiver.setStaName((FlinnEngdahlCodes.getGeoRegionName(lat, lon)+" ("+season()+")").toLowerCase());
		return receiver;
	}

	public double delta() {
		// ASSUMPTION: delta is constant for all azimuths!
		return delta[0];
	}

	public int nRadii(int a) { return tt[iaz(a)].length; }

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

}
