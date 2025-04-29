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
package gov.sandia.gmp.util.testingbuffer;

import java.util.HashMap;
import java.util.Map;

public class HighPrecision {

	static final public Map<String, Double> precision;
	static {
		precision = new HashMap<String, Double>();
		precision.put("origin.lat", 0.000001);
		precision.put("origin.lon", 0.000001);
		precision.put("origin.depth", 0.0001);
		precision.put("origin.time", 0.00001);
		precision.put("origin.depdp", 0.0001);
		precision.put("origin.mb", 0.01);
		precision.put("origin.ms", 0.01);
		precision.put("origin.ml", 0.01);

		precision.put("origerr.sxx", 0.01);
		precision.put("origerr.syy", 0.01);
		precision.put("origerr.szz", 0.01);
		precision.put("origerr.stt", 0.01);
		precision.put("origerr.sxy", 0.01);
		precision.put("origerr.sxz", 0.01);
		precision.put("origerr.syz", 0.01);
		precision.put("origerr.stx", 0.01);
		precision.put("origerr.sty", 0.01);
		precision.put("origerr.stz", 0.01);
		precision.put("origerr.sdobs", 0.0001);
		precision.put("origerr.smajax", 0.0001);
		precision.put("origerr.sminax", 0.0001);
		precision.put("origerr.strike", 0.01);
		precision.put("origerr.sdepth", 0.0001);
		precision.put("origerr.stime", 0.001);
		precision.put("origerr.conf", 0.001);

		precision.put("azgap.azgap1", 0.01);
		precision.put("azgap.azgap2", 0.01);

		precision.put("assoc.belief", 0.01);
		precision.put("assoc.delta", 0.001);
		precision.put("assoc.seaz", 0.01);
		precision.put("assoc.esaz", 0.01);
		precision.put("assoc.timeres", 0.001);
		precision.put("assoc.azres", 0.1);
		precision.put("assoc.slores", 0.01);
		precision.put("assoc.emares", 0.1);
		precision.put("assoc.wgt", 0.001);

		precision.put("arrival.time", 0.00001);
		precision.put("arrival.deltim", 0.001);
		precision.put("arrival.azimuth", 0.01);
		precision.put("arrival.delaz", 0.01);
		precision.put("arrival.slow", 0.01);
		precision.put("arrival.delslo", 0.01);
		precision.put("arrival.ema", 0.01);
		precision.put("arrival.rect", 0.001);
		precision.put("arrival.amp", 0.01);
		precision.put("arrival.per", 0.01);
		precision.put("arrival.logat", 0.01);
		precision.put("arrival.snr", 0.01);

		precision.put("site.lat", 0.000001);
		precision.put("site.lon", 0.000001);
		precision.put("site.elev", 0.0001);
		precision.put("site.dnorth", 0.0001);
		precision.put("site.deast", 0.0001);

		precision.put("gmp.source.lat", 0.000001);
		precision.put("gmp.source.lon", 0.000001);
		precision.put("gmp.source.depth", 0.001);
		precision.put("gmp.source.origintime", 0.1);
		precision.put("gmp.source.gtlevel", 0.01);

		precision.put("gmp.srcobsassoc.delta", 0.000001);
		precision.put("gmp.srcobsassoc.esaz", 0.001);
		precision.put("gmp.srcobsassoc.seaz", 0.001);

		precision.put("gmp.observation.arrivaltime", 0.001);
		precision.put("gmp.observation.timeuncertainty", 0.001);
		precision.put("gmp.observation.azimuth", 0.001);
		precision.put("gmp.observation.azuncertainty", 0.001);
		precision.put("gmp.observation.slowness", 0.001);
		precision.put("gmp.observation.slowuncertainty", 0.001);

		precision.put("gmp.receiver.lat", 0.000001);
		precision.put("gmp.receiver.lon", 0.000001);
		precision.put("gmp.receiver.elevation", 0.001);
		precision.put("gmp.receiver.starttime", 0.001);
		precision.put("gmp.receiver.endtime", 0.001);

		precision.put("source.lat", 0.0001);
		precision.put("source.lon", 0.0001);
		precision.put("source.depth", 0.0001);
		precision.put("source.time", 0.001);
		precision.put("source.gtLevel", 0.0001);
		precision.put("source.sdobs", 0.0001);
		precision.put("observation.arrivalTime", 0.0001);
		precision.put("observation.deltim", 0.0001);
		precision.put("observation.timeres", 0.001);
		precision.put("observation.azimuth", 0.0001);
		precision.put("observation.delaz", 0.0001);
		precision.put("observation.azres", 0.0001);
		precision.put("observation.slow", 0.0001);
		precision.put("observation.delslo", 0.0001);
		precision.put("observation.slores", 0.0001);
		precision.put("observation.mectt", 0.0001);
		precision.put("observation.mecaz", 0.0001);
		precision.put("observation.mecsh", 0.0001);
		precision.put("observation.ttWeight", 0.0001);
		precision.put("observation.azWeight", 0.0001);
		precision.put("observation.shWeight", 0.0001);
		precision.put("receiver.lat", 0.0001);
		precision.put("receiver.lon", 0.0001);
		precision.put("receiver.elev", 0.0001);
		precision.put("receiver.dnorth", 0.0001);
		precision.put("receiver.deast", 0.0001);
		precision.put("hyperellipse.sumSQRWeightedResiduals", 0.0001);
		precision.put("hyperellipse.apriori_variance", 0.0001);
		precision.put("hyperellipse.conf", 0.0001);
		precision.put("hyperellipse.cov_xx", 0.001);
		precision.put("hyperellipse.cov_yy", 0.001);
		precision.put("hyperellipse.cov_zz", 0.001);
		precision.put("hyperellipse.cov_tt", 0.001);
		precision.put("hyperellipse.cov_xy", 0.001);
		precision.put("hyperellipse.cov_xz", 0.001);
		precision.put("hyperellipse.cov_yz", 0.001);
		precision.put("hyperellipse.cov_tx", 0.001);
		precision.put("hyperellipse.cov_ty", 0.001);
		precision.put("hyperellipse.cov_tz", 0.001);
		precision.put("hyperellipse.sigma", 0.0001);
		precision.put("hyperellipse.kappa(1)", 0.0001);
		precision.put("hyperellipse.kappa(2)", 0.0001);
		precision.put("hyperellipse.kappa(3)", 0.0001);
		precision.put("hyperellipse.kappa(4)", 0.0001);
		precision.put("hyperellipse.sdepth", 0.0001);
		precision.put("hyperellipse.stime", 0.0001);
		precision.put("ellipse.majax", 0.0001);
		precision.put("ellipse.minax", 0.0001);
		precision.put("ellipse.trend", 0.001);
		precision.put("ellipsoid.majax_length", 0.0001);
		precision.put("ellipsoid.majax_trend", 0.0001);
		precision.put("ellipsoid.majax_plunge", 0.0001);
		precision.put("ellipsoid.intax_length", 0.0001);
		precision.put("ellipsoid.intax_trend", 0.0001);
		precision.put("ellipsoid.intax_plunge", 0.0001);
		precision.put("ellipsoid.minax_length", 0.0001);
		precision.put("ellipsoid.minax_trend", 0.0001);
		precision.put("ellipsoid.minax_plunge", 0.0001);
		precision.put("TRAVEL_TIME", 0.001);
		precision.put("TT_BASEMODEL", 0.001);
		precision.put("TT_MODEL_UNCERTAINTY", 0.001);
		precision.put("TT_MODEL_UNCERTAINTY_DISTANCE_DEPENDENT", 0.001);
		precision.put("TT_MODEL_UNCERTAINTY_PATH_DEPENDENT", 0.001);
		precision.put("TT_MASTER_EVENT_CORRECTION", 0.001);
		precision.put("TT_PATH_CORRECTION", 0.001);
		precision.put("TT_PATH_CORR_DERIV_HORIZONTAL", 0.001);
		precision.put("TT_PATH_CORR_DERIV_RADIAL", 0.001);
		precision.put("TT_PATH_CORR_DERIV_LAT", 0.001);
		precision.put("TT_PATH_CORR_DERIV_LON", 0.001);
		precision.put("TT_ELLIPTICITY_CORRECTION", 0.001);
		precision.put("TT_ELEVATION_CORRECTION", 0.001);
		precision.put("TT_ELEVATION_CORRECTION_SOURCE", 0.001);
		precision.put("TT_SITE_CORRECTION", 0.001);
		precision.put("DTT_DLAT", 0.01);
		precision.put("DTT_DLON", 0.01);
		precision.put("DTT_DR", 0.001);
		precision.put("DTT_DTIME", 0.001);
		precision.put("SLOWNESS", 0.001);
		precision.put("SLOWNESS_DEGREES", 0.001);
		precision.put("SLOWNESS_BASEMODEL", 0.001);
		precision.put("SLOWNESS_MASTER_EVENT_CORRECTION", 0.001);
		precision.put("SLOWNESS_PATH_CORRECTION", 0.001);
		precision.put("SLOWNESS_PATH_CORR_DERIV_HORIZONTAL", 0.001);
		precision.put("SLOWNESS_PATH_CORR_DERIV_LAT", 0.001);
		precision.put("SLOWNESS_PATH_CORR_DERIV_LON", 0.001);
		precision.put("SLOWNESS_PATH_CORR_DERIV_RADIAL", 0.001);
		precision.put("SLOWNESS_MODEL_UNCERTAINTY", 0.001);
		precision.put("SLOWNESS_MODEL_UNCERTAINTY_DEGREES", 0.001);
		precision.put("SLOWNESS_MODEL_UNCERTAINTY_PATH_DEPENDENT", 0.001);
		precision.put("SLOWNESS_MODEL_UNCERTAINTY_STATION_PHASE_DEPENDENT", 0.001);
		precision.put("DSH_DX", 0.001);
		precision.put("DSH_DX_DEGREES", 0.001);
		precision.put("DSH_DLAT", 0.01);
		precision.put("DSH_DLON", 0.01);
		precision.put("DSH_DR", 0.001);
		precision.put("DSH_DTIME", 0.001);
		precision.put("DISTANCE", 0.001);
		precision.put("DISTANCE_DEGREES", 0.001);
		precision.put("AZIMUTH", 0.001);
		precision.put("AZIMUTH_DEGREES", 0.001);
		precision.put("AZIMUTH_MASTER_EVENT_CORRECTION", 0.001);
		precision.put("AZIMUTH_PATH_CORRECTION", 0.001);
		precision.put("AZIMUTH_PATH_CORR_DERIV_HORIZONTAL", 0.001);
		precision.put("AZIMUTH_PATH_CORR_DERIV_LAT", 0.001);
		precision.put("AZIMUTH_PATH_CORR_DERIV_LON", 0.001);
		precision.put("AZIMUTH_PATH_CORR_DERIV_RADIAL", 0.001);
		precision.put("AZIMUTH_BASEMODEL", 0.001);
		precision.put("AZIMUTH_MODEL_UNCERTAINTY", 0.001);
		precision.put("AZIMUTH_MODEL_UNCERTAINTY_DEGREES", 0.001);
		precision.put("AZIMUTH_MODEL_UNCERTAINTY_PATH_DEPENDENT", 0.001);
		precision.put("AZIMUTH_MODEL_UNCERTAINTY_STATION_PHASE_DEPENDENT", 0.001);
		precision.put("DAZ_DLAT", 0.001);
		precision.put("DAZ_DLON", 0.001);
		precision.put("DAZ_DR", 0.001);
		precision.put("DAZ_DTIME", 0.001);
		precision.put("BACKAZIMUTH", 0.001);
		precision.put("BACKAZIMUTH_DEGREES", 0.001);
		precision.put("OUT_OF_PLANE", 0.001);
		precision.put("SEDIMENTARY_VELOCITY_RECEIVER", 0.001);
		precision.put("SEDIMENTARY_VELOCITY_SOURCE", 0.001);
		precision.put("HYDRO_BLOCKED", 0.1);
	}
}
