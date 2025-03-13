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

import gov.sandia.gmp.util.globals.Globals;

public class VelocityInterpolator {

	private int[] indices;
	private double[] coefficients;
	private SurfaceWaveModel model;

	/**
	 * Compute interpolation coefficients for the specified period in the model.
	 * @param model
	 * @param period
	 * @throws Exception
	 */
	public VelocityInterpolator (SurfaceWaveModel model, double period) throws Exception {
		this.model = model;
		int idx = Globals.hunt(model.getPeriods(), period, false, false);
		if (idx < 0 || idx == model.getPeriods().length-1)
			throw new Exception(String.format("period %1.2f is out of range [%1.2f, %1.2f]", 
					period, model.getPeriod(0), model.getPeriod(model.getNPeriods()-1)));

		double c = (period-model.getPeriod(idx))/(model.getPeriod(idx+1)-model.getPeriod(idx));
		if (c < 1e-7) {
			indices = new int[] {idx};
			coefficients = new double[] {1.};	
		}
		else if (c > 0.9999999) {
			indices = new int[] {idx+1};
			coefficients = new double[] {1.};	
		}
		else {
			indices = new int[] {idx, idx+1};
			coefficients = new double[] {1.-c, c};	
		}
	}

	public double getVelocity(double[] point) throws Exception {
		return getVelocity(model.getColatitudeIndex(point),
				model.getLongitudeIndex(point));
	}
	
	public double getVelocity(int ilat, int ilon) throws Exception {
		if (ilon < 0 || ilon >= model.nlon)
			throw new Exception("ilon out of range "+ilon);
		double[] varray = model.getVelocities()[ilat][ilon];
		double v = 0;
		for (int i =0; i<indices.length; ++i)
			v += varray[indices[i]]*coefficients[i];
		return v;
		
	}
}
