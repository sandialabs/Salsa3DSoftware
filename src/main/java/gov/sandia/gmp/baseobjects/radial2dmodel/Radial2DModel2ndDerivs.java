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

import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;

public class Radial2DModel2ndDerivs extends Radial2DModel {

	protected double[][] slowness;

	protected double[][] dsh_dx;

	public Radial2DModel2ndDerivs() {
		super();
	}

	public Radial2DModel2ndDerivs(Radial2DModel model) {
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

		computeDerivatives();
	}

	public Radial2DModel2ndDerivs(File inputFile) throws IOException {
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

		EnumMap<GeoAttributes, double[][]> data = new EnumMap<>(GeoAttributes.class);

		for (GeoAttributes a : requestedAttributes)
			data.put(a, null);

		data.put(GeoAttributes.TRAVEL_TIME, tt);
		data.put(GeoAttributes.TT_MODEL_UNCERTAINTY, uncertainty);
		data.put(GeoAttributes.SLOWNESS, slowness);
		data.put(GeoAttributes.DSH_DX, dsh_dx);

		EnumMap<GeoAttributes, Double> results = super.interpolate(v, data);

		if (requestedAttributes.contains(GeoAttributes.SLOWNESS_DEGREES))
			results.put(GeoAttributes.SLOWNESS_DEGREES, toRadians(results.get(GeoAttributes.SLOWNESS)));

		return results;
	}

	protected void computeDerivatives() {
		// derivative of tt wrt radius
		slowness = new double[azimuth.length][];
		// derivative of slowness wrt radius
		dsh_dx = new double[azimuth.length][];

		for (int i=0; i<azimuth.length; ++i) {
			double[] ri = radii[i];
			double[] slo_i = slowness[i] = new double[ri.length];
			double[] dsh_dx_i = dsh_dx[i] = new double[ri.length];

			if (ri.length > 0) {
				CubicSpline spline = new CubicSpline(ri, tt[i]);

				for (int j=0; j<ri.length; ++j) {
					double[] values = spline.interpolate(ri[j]);
					slo_i[j] = toDegrees(values[1]);  // convert sec/degree to sec/radian
					dsh_dx_i[j] = toDegrees(toDegrees(values[2])); // convert sec/degree^2 to sec/radian^2
				}
			}
		}
	}
}
