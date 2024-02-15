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

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;

public class Radial2DModel1stDerivs extends Radial2DModel {

	protected double[][] dtdr;

	//protected double[][] dtdaz;

	public Radial2DModel1stDerivs() {
		super();
	}

	public Radial2DModel1stDerivs(Radial2DModel model) {
		// copy references to all the data in the base class
		this.inputFile = super.inputFile;
		this.name = super.name;
		this.season = super.season;
		this.lat = super.lat;
		this.lon = super.lon;
		this.center = super.center;
		this.period = super.period;
		this.azimuth = super.azimuth;
		this.radii = super.radii;
		this.tt = super.tt;
		this.uncertainty = super.uncertainty;
		this.htConvert = super.htConvert;

		// compute dtdr and dtdaz
		computeDerivatives();
	}

	public Radial2DModel1stDerivs(File inputFile) throws IOException {
		loadModel(inputFile);
	}

	@Override
	public void loadModel(File inputFile) throws IOException {
		super.loadModel(inputFile);
		computeDerivatives();
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
	@Override
	public EnumMap<GeoAttributes, Double> interpolate(double lat, double lon, boolean inDegrees, EnumSet<GeoAttributes> requestedAttributes) {
		return interpolate(inDegrees ? VectorGeo.getVectorDegrees(lat, lon) : VectorGeo.getVector(lat, lon), 
				requestedAttributes);
	}

	/**
	 * Return interpolated values of requested attributes using bi-linear interpolation.  
	 * Supported attributes include:
	 * TRAVEL_TIME, TT_MODEL_UNCERTAINTY, SLOWNESS, SLOWNESS_DEGREES, AZIMUTH, AZIMUTH_DEGREES, DISTANCE, DISTANCE_DEGREES.
	 * @param v unit vector of location where interpolation should be calculated.
	 * @param requestedAttributes
	 * @return
	 */
	@Override
	public EnumMap<GeoAttributes, Double> interpolate(double[] v, EnumSet<GeoAttributes> requestedAttributes) {

		EnumMap<GeoAttributes, double[][]> values = new EnumMap<>(GeoAttributes.class);

		for (GeoAttributes a : requestedAttributes)
			values.put(a, null);

		values.put(GeoAttributes.TRAVEL_TIME, tt);
		values.put(GeoAttributes.TT_MODEL_UNCERTAINTY, uncertainty);
		values.put(GeoAttributes.SLOWNESS, dtdr);

		EnumMap<GeoAttributes, Double> results = interpolate(v, values);

		if (requestedAttributes.contains(GeoAttributes.SLOWNESS_DEGREES))
			results.put(GeoAttributes.SLOWNESS_DEGREES, toRadians(results.get(GeoAttributes.SLOWNESS)));

		return results;
	}

	protected void computeDerivatives() {
		dtdr = new double[azimuth.length][];
		//dtdaz = new double[azimuth.length][];

		for (int i=0; i<azimuth.length; ++i) {
			double[] ri = radii[i];
			double[] ti = tt[i];

			double[] dr = dtdr[i] = new double[ri.length];
			//double[] daz = dtdaz[i] = new double[ri.length];

			for (int j=0; j<ri.length; ++j) {

				// compute dtdr(i,j) in seconds per radian
				if (ri.length >= 2) {
					if (j==0)
						dr[j] = (ti[1]-ti[0])/toRadians(ri[1]-ri[0]);
					else if (j==ri.length-1) 
						dr[j] = (ti[j]-ti[j-1])/toRadians(ri[j]-ri[j-1]);
					else
						dr[j] = (ti[j+1]-ti[j-1])/toRadians(ri[j+1]-ri[j-1]);
				}
				else
					dr[j] = Double.NaN;

//				// convert from seconds/km to seconds/radian 
//				dr[j] = dr[j]*6371.;
//
//				// compute dtdaz(i,j)
//				if (j == 0)
//					daz[j] = 0.;
//				else {
//					int p = iaz(i-1);
//					int n = iaz(i+1);
//					int np = radii[p].length;
//					int nn = radii[n].length;
//
//					if (j < np && j < nn)
//						daz[j] =  (tt[n][j]-tt[p][j])/(toRadians(daz(n,p)*ri[j])*6371.);
//					else if (j < nn)
//						daz[j] =  (tt[n][j]-ti[j])/(toRadians(daz(n,i)*ri[j])*6371.);
//					else if (j < np)
//						daz[j] =  (ti[j]-tt[p][j])/(toRadians(daz(i,p)*ri[j])*6371.);
//					else
//						daz[j] = Double.NaN;
//
//					// convert from seconds/km to seconds/radian
//					daz[j] = daz[j]*6371.;
//				}
			}
		}
	}
}
