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
package gov.sandia.gmp.baseobjects.globals;

import java.util.EnumSet;

/**
 * Attributes of geological significance. Future versions may add new attributes
 * to this list but may not remove existing attributes or change their units,
 * without acheiving broad agreement from software developers. Order of
 * attributes is not guaranteed.
 */
public enum GeoAttributes {
	/**
	 * P velocity in km/second
	 */
	PVELOCITY("km/second", 7, 4),

	/**
	 * S velocity in km/second
	 */
	SVELOCITY("km/second", 7, 4),

	/**
	 * P slowness in seconds/km. This is the inverse of PVELOCITY. This typically
	 * refers to a material property at some point within the Earth.
	 */
	PSLOWNESS("seconds/km", 9, 6),

	/**
	 * S slowness in seconds/km. This is the inverse of SVELOCITY This typically
	 * refers to a material property at some point within the Earth.
	 */
	SSLOWNESS("seconds/km", 9, 6),

	/**
	 * Density in g/cm^3.
	 */
	DENSITY("g/cm^3", 7, 4),

	/**
	 * Seismic Q for P waves.
	 */
	QP(""),

	/**
	 * Seismic Q for S waves
	 */
	QS(""),

	/**
	 * Source-receiver travel time, in seconds.
	 */
	TRAVEL_TIME("seconds"),

	/**
	 * Source-receiver travel time from basemodel (no corrections), in seconds.
	 */
	TT_BASEMODEL("seconds"),

	/**
	 * tt observed minus tt predicted, in seconds.
	 */
	TT_RESIDUAL("seconds"),

	/**
	 * tt difference from ak135 predicted, in seconds.
	 */
	TT_DELTA_AK135("seconds"),

	/**
	 * tt difference from a starting model, in seconds.
	 */
	TT_DELTA_STARTING_MODEL("seconds"),

	/**
	 * Uncertainty of the predicted source-receiver travel time, in seconds.
	 */
	TT_MODEL_UNCERTAINTY("seconds"),
	
	TT_MODEL_UNCERTAINTY_PATH_DEPENDENT("seconds"),
	
	TT_MODEL_UNCERTAINTY_DISTANCE_DEPENDENT("seconds"), 
	
	TT_MODEL_UNCERTAINTY_STATION_PHASE_DEPENDENT("seconds"),
	
	TT_MODEL_UNCERTAINTY_CONSTANT("seconds"), 
	
	TT_MODEL_UNCERTAINTY_SOURCE_DEPENDENT("seconds"),

	TT_MODEL_UNCERTAINTY_NA_VALUE(""),

	/**
	 * Variance of the predicted source-receiver travel time, in seconds^2.
	 */
	TT_MODEL_VARIANCE("seconds^2"),

	/**
	 * Covariance of the predicted source-receiver travel time but smoothed from the
	 * direct result, in seconds^2.
	 */
	TT_MODEL_VARIANCE_SMOOTHED("seconds^2"),

	/**
	 * Variance of the predicted source-receiver travel time, in seconds^2 accrued
	 * from the covariance matrix diagonal elements only.
	 */
	TT_MODEL_VARIANCE_DIAGONAL("seconds^2"),

	/**
	 * Variance of the predicted source-receiver travel time, in seconds^2 accrued
	 * from the covariance matrix diagonal elements only. Same as
	 * TT_MODEL_VARIANCE_DIAGONAL except the result is spatially smoothed with its
	 * neighbors
	 */
	TT_MODEL_VARIANCE_DIAGONAL_SMOOTHED("seconds^2"),

	/**
	 * Variance of the predicted source-receiver travel time, in seconds^2 accrued
	 * from the covariance matrix off-diagonal elements only.
	 */
	TT_MODEL_VARIANCE_OFFDIAGONAL("seconds^2"),

	/**
	 * Variance of the predicted source-receiver travel time, in seconds^2 accrued
	 * from the covariance matrix diagonal elements only. Same as
	 * TT_MODEL_VARIANCE_OFFDIAGONAL except the result is spatially smoothed with
	 * its neighbors
	 */
	TT_MODEL_VARIANCE_OFFDIAGONAL_SMOOTHED("seconds^2"),

	/**
	 * Variance of the predicted source-receiver travel time, in seconds^2 accrued
	 * from the covariance matrix off-diagonal elements only.
	 */
	TT_MODEL_VARIANCE_NONREPRESENTED("seconds^2"),

	/**
	 * Variance of the predicted source-receiver travel time, in seconds^2 accrued
	 * from the covariance matrix off-diagonal elements only. Same as
	 * TT_MODEL_VARIANCE_NONREPRESENTED except the result is spatially smoothed with
	 * its neighbors
	 */
	TT_MODEL_VARIANCE_NONREPRESENTED_SMOOTHED("seconds^2"),

	/**
	 * Fraction of covariance contributed by represented tomography nodes. One
	 * implies all contributing nodes are represented. Zero implies all nodes
	 * contributing to uncertainty are non-represented.
	 */
	TT_MODEL_VARIANCE_REPRESENTED_FRACTION(""),

	/**
	 * Uncertainty of the observed source-receiver travel time, in seconds.
	 */
	TT_OBSERVED_UNCERTAINTY("seconds"),

	/**
	 * Travel time tolerance value, in seconds.
	 */
	TT_TOLERANCE("seconds"),
	
	TT_MASTER_EVENT_CORRECTION("seconds"),

	/**
	 * Travel time path correction, in seconds.
	 */
	TT_PATH_CORRECTION("seconds"),

	/**
	 * Derivative of the travel time path correction with respect to source-receiver
	 * separation, in seconds/radian.
	 */
	TT_PATH_CORR_DERIV_HORIZONTAL("seconds/radian"),

	/**
	 * Derivative of the travel time path correction with respect to source radius,
	 * in seconds.
	 */
	TT_PATH_CORR_DERIV_RADIAL("seconds/km"),

	/**
	 * Derivative of the travel time path correction with respect to source
	 * latitude, in seconds/radian.
	 */
	TT_PATH_CORR_DERIV_LAT("seconds/radian"),

	/**
	 * Derivative of the travel time path correction with respect to source
	 * longitude, in seconds/radian.
	 */
	TT_PATH_CORR_DERIV_LON("seconds/radian"),

	/**
	 * Travel time ellipticity correction, in seconds.
	 */
	TT_ELLIPTICITY_CORRECTION("seconds"),

	/**
	 * Travel time receiver elevation correction, in seconds.
	 */
	TT_ELEVATION_CORRECTION("seconds"),

	/**
	 * Travel time source elevation correction, in seconds. Zero for positive source
	 * depth, positive number for source at negative source depth.
	 */
	TT_ELEVATION_CORRECTION_SOURCE("seconds"),

	/**
	 * Travel time site correction, in seconds.
	 */
	TT_SITE_CORRECTION("seconds"),

	/**
	 * Travel time site correction for P phases, in seconds.
	 */
	TT_SITE_CORRECTION_P("seconds"),

	/**
	 * Travel time site correction for S phases, in seconds.
	 */
	TT_SITE_CORRECTION_S("seconds"),

	/**
	 * Travel time source correction, in seconds.
	 */
	TT_SOURCE_CORRECTION("seconds"),

	/**
	 * Derivative of travel time wrt source latitude, in seconds/radian
	 */
	DTT_DLAT("seconds/radian"),

	/**
	 * Derivative of travel time wrt source longitude, in seconds/radian
	 */
	DTT_DLON("seconds/radian"),

	/**
	 * Derivative of travel time wrt source radius, in seconds/km
	 */
	DTT_DR("seconds/km"),

	/**
	 * Derivative of travel time wrt origin time (always = 1.)
	 */
	DTT_DTIME(""),

	/**
	 * Horizontal component of slowness, in seconds/radian. Synonymous with ray
	 * parameter. This is generally an observed or predicted quantity, not a
	 * material property.
	 */
	SLOWNESS("seconds/radian"),

	/**
	 * Horizontal component of slowness, in seconds/degree. Synonymous with ray
	 * parameter. This is generally an observed or predicted quantity, not a
	 * material property.
	 */
	SLOWNESS_DEGREES("seconds/degree"),

	SLOWNESS_DELTA_AK135("seconds/radian"),

	SLOWNESS_BASEMODEL("seconds/radian"),
	
	SLOWNESS_MASTER_EVENT_CORRECTION("seconds/radian"),

	SLOWNESS_PATH_CORRECTION("seconds/radian"),

	SLOWNESS_PATH_CORR_DERIV_HORIZONTAL("seconds/radian^2"),

	SLOWNESS_PATH_CORR_DERIV_LAT("seconds/radian^2"),

	SLOWNESS_PATH_CORR_DERIV_LON("seconds/radian^2"),

	SLOWNESS_PATH_CORR_DERIV_RADIAL("seconds/radian/km"),

	/**
	 * Uncertainty of predicted horizontal component of slowness, in seconds/radian.
	 */
	SLOWNESS_MODEL_UNCERTAINTY("seconds/radian"),

	/**
	 * Uncertainty of predicted horizontal component of slowness, in seconds/degree.
	 */
	SLOWNESS_MODEL_UNCERTAINTY_DEGREES("seconds/degree"),

	SLOWNESS_MODEL_UNCERTAINTY_PATH_DEPENDENT("seconds/radians"),
	
	SLOWNESS_MODEL_UNCERTAINTY_PATH_DEPENDENT_LIBCORR3D("seconds/radian"),
	
	SLOWNESS_MODEL_UNCERTAINTY_DISTANCE_DEPENDENT("seconds/radians"),

	SLOWNESS_MODEL_UNCERTAINTY_STATION_PHASE_DEPENDENT("seconds/radians"),
	
	SLOWNESS_MODEL_UNCERTAINTY_NA_VALUE("seconds/radians"),

	/**
	 * Uncertainty of observed horizontal component of slowness, in seconds/radian.
	 */
	SLOWNESS_OBSERVED_UNCERTAINTY("seconds/radian"),

	/**
	 * Uncertainty of observed horizontal component of slowness, in seconds/degree.
	 */
	SLOWNESS_OBSERVED_UNCERTAINTY_DEGREES("seconds/degree"),

	SLOWNESS_TOLERANCE_DEGREES("seconds/degree"),

	/**
	 * Derivative of horizontal component of slowness wrt source position, in
	 * seconds/radian^2
	 */
	DSH_DX("seconds/radian^2"),

	/**
	 * Derivative of horizontal component of slowness wrt source position, in
	 * seconds/degrees^2
	 */
	DSH_DX_DEGREES("seconds/degrees^2"),

	/**
	 * Derivative of horizontal component of slowness wrt source latitude, in
	 * seconds/radian^2
	 */
	DSH_DLAT("seconds/radian^2"),

	/**
	 * Derivative of horizontal component of slowness wrt source longitude, in
	 * seconds/radian^2
	 */
	DSH_DLON("seconds/radian^2"),

	/**
	 * Derivative of horizontal component of slowness wrt source radius, in
	 * seconds/(radian-km)
	 */
	DSH_DR("seconds/(radian-km)"),

	/**
	 * Derivative of horizontal component of slowness wrt origin time (always = 0.)
	 */
	DSH_DTIME("radian^-1"),

	/**
	 * Geocentric angular distance between two points, in radians
	 */
	DISTANCE("radians"),

	/**
	 * Geocentric angular distance between two points, in degrees
	 */
	DISTANCE_DEGREES("degrees"),

	/**
	 * Azimuth from receiver to source, in radians clockwise from north.
	 */
	AZIMUTH("radians"),

	/**
	 * Azimuth from receiver to source, in degrees clockwise from north.
	 */
	AZIMUTH_DEGREES("degrees"),

	AZIMUTH_MASTER_EVENT_CORRECTION("radians"),

	/**
	 * Azimuth path correction, in radians.
	 */
	AZIMUTH_PATH_CORRECTION("radians"),

	AZIMUTH_PATH_CORR_DERIV_HORIZONTAL("unitless"), AZIMUTH_PATH_CORR_DERIV_LAT("unitless"),
	AZIMUTH_PATH_CORR_DERIV_LON("unitless"), AZIMUTH_PATH_CORR_DERIV_RADIAL("radians/km"),

	/**
	 * Uncertainty of predicted receiver-to-source azimuth, in radians.
	 */
	AZIMUTH_BASEMODEL("radians"),

	AZIMUTH_MODEL_UNCERTAINTY("radians"),

	/**
	 * Uncertainty of predicted receiver-to-source azimuth, in degrees.
	 */
	AZIMUTH_MODEL_UNCERTAINTY_DEGREES("degrees"),

	AZIMUTH_MODEL_UNCERTAINTY_PATH_DEPENDENT("radians"),
	
	AZIMUTH_MODEL_UNCERTAINTY_PATH_DEPENDENT_LIBCORR3D("radians"),

	AZIMUTH_MODEL_UNCERTAINTY_STATION_PHASE_DEPENDENT("radians"),
	
	AZIMUTH_MODEL_UNCERTAINTY_NA_VALUE("radians"),
	
	AZIMUTH_MODEL_UNCERTAINTY_DISTANCE_DEPENDENT("radians"),

	/**
	 * Uncertainty of observed receiver-to-source azimuth, in radians.
	 */
	AZIMUTH_OBSERVED_UNCERTAINTY("radians"),

	/**
	 * Uncertainty of observed receiver-to-source azimuth, in degrees.
	 */
	AZIMUTH_OBSERVED_UNCERTAINTY_DEGREES("degrees"),

	AZIMUTH_TOLERANCE_DEGREES("degrees"),

	/**
	 * Derivative of receiver-to-source azimuth wrt source latitude, unitless
	 */
	DAZ_DLAT(""),

	/**
	 * Derivative of receiver-to-source azimuth wrt source longitude, unitless
	 */
	DAZ_DLON(""),

	/**
	 * Derivative of receiver-to-source azimuth wrt source radius, radians/km. Hard
	 * to imagine this would ever not equal zero.
	 */
	DAZ_DR("radians/km"),

	/**
	 * Derivative of receiver-to-source azimuth wrt origin time (always = 0.)
	 */
	DAZ_DTIME("radians/sec"),

	/**
	 * Azimuth from source to receiver, in radians clockwise from north.
	 */
	BACKAZIMUTH("radians"),

	/**
	 * Azimuth from source to receiver, in degrees clockwise from north.
	 */
	BACKAZIMUTH_DEGREES("degrees"),

	/**
	 * Uncertainty of source-to-receiver azimuth, in radians.
	 */
	BACKAZIMUTH_UNCERTAINTY("radians"),

	AMPLITUDE(""), AMPLITUDE_TOLERANCE(""),

	/**
	 * Depth of deepest point on a seismic ray measured relative to sea level (km).
	 */
	TURNING_DEPTH("km"),

	/**
	 * The maximum amount by which a seismic ray deviates from the great circle
	 * plane containing the source and the receiver, in km. Considering source and
	 * receiver to be 3 component vectors in Earth centered coordinate system, the
	 * sign of out_of_plane is the same as the sign of source cross receiver.
	 */
	OUT_OF_PLANE("km"),

	/**
	 * If some 3 dimensional portion of the Earth is defined as the 'active region',
	 * then ACTIVE_FRACTION refers to the fraction of a seismic ray that travels
	 * through that active region. This is determined in a 3D sense, i.e., the
	 * vertical component of travel is considered, not simply the great circle path.
	 * Unitless.
	 */
	ACTIVE_FRACTION(""),

	/**
	 * Total travel time for a ray divided by the path length, in km/second.
	 */
	AVERAGE_RAY_VELOCITY("km/second"),

	/**
	 * Time required to compute something, in seconds.
	 */
	CALCULATION_TIME("seconds"),

	/**
	 * Percent deviation from reference P velocity. Used in the MITP08 model where
	 * it represents the percent deviation from AK135 P velocity.
	 */
	DPVELOCITY(""),

	/**
	 * An array of GeoVectors that define points on a ray path.
	 */
	RAY_PATH(""),

	/**
	 * Weights assigned to active nodes in a tomography model.
	 */
	TOMO_WEIGHTS(""),

	/**
	 * Derivative of travel time with respect to slowness. Used in tomography. What
	 * is the change in travel time if the slowness of a model node is changed.
	 */
	DTT_DSLOW(""),

	/**
	 * Topography (positive) / bathymetry (negative), in km relative to sea level.
	 * Includes ice.
	 */
	TOPOGRAPHY("km"),

	/**
	 * Fitness value. Used by PEDAL to define probability that an event occurred at
	 * some node in a model.
	 */
	FITNESS(""),

	/**
	 * Probability of a station detecting an event at a node in a model.
	 */
	PDET(" "),

	X("km"),

	Y("km"),

	Z("km"),

	LATITUDE("radians"),

	LONGITUDE("radians"),

	LATITUDE_DEGREES("degrees"),

	LONGITUDE_DEGREES("degrees"),

	BOUNCE_POINT_LATITUDE_DEGREES("degrees"),

	BOUNCE_POINT_LONGITUDE_DEGREES("degrees"),

	BOUNCE_POINT_SNELLS_LAW(""),

	BOUNCE_POINT_OUTOFPLANE_DEGREES("degrees"),

	/**
	 * Depth relative to surface of ellipsoid, in km.
	 */
	DEPTH("km"),

	/**
	 * Radius in km.
	 */
	RADIUS("km"),

	/**
	 * Hit count. Unitless.
	 */
	HITCOUNT(""),

	/**
	 * Diagonal element of a model resolution matrix. Unitless, range 0. to 1.
	 */
	RESOLUTION(""),

	/**
	 * Diagonal element of a model covariance matrix.
	 */
	VARIANCE(""),

	/**
	 * Element of a row or column of a model covariance matrix.
	 */
	COVARIANCE(""),

	/**
	 * The type of ray: REFRACTION, DIFFRACTION, etc. See ENUM
	 * gov.sandia.gmp.baseobjects.globals.RayType;
	 */
	RAY_TYPE(""),

	/**
	 * Calling applications sometimes need a placeholder.
	 */
	NA_VALUE(""), 
	
	SEDIMENTARY_VELOCITY_RECEIVER("km/sec"), 
	
	SEDIMENTARY_VELOCITY_SOURCE("km/sec"), 
	
	/**
	 * Period of a seismic wave in seconds
	 */
	PERIOD("sec"), 
	
	/**
	 * Boolean indicating whether or not a predicted hydroacoustic travel time observation is blocked
	 */
	TT_BLOCKED(""),
	
	/**
	 * Boolean indicating whether or not a predicted travel time observation is extrapolated
	 */
	TT_EXTRAPOLATED(""), 
	
	/**
	 * String with details about an extrapolated travel time prediction.
	 * <p>Possible messages:
	 * <ul>
	 * <li>No extrapolation
	 * <li>Extrapolated point < first distance
	 * <li>Extrapolated point > last distance
	 * <li>Extrapolated point < first depth
	 * <li>Extrapolated point > last depth
	 * <li>Extrapolated point < first distance and < first depth
	 * <li>Extrapolated point > last distance and < first depth
	 * <li>Extrapolated point < first distance and > last depth
	 * <li>Extrapolated point > last distance and > last depth
	 * </ul>
	 */
	TT_EXTRAPOLATION_MESSAGE(""),
	
	/**
	 * Boolean indicating whether or not a predicted hydroacoustic azimuth observation is blocked
	 */
	AZIMUTH_BLOCKED(""),
	
	/**
	 * Boolean indicating whether or not a predicted azimuth observation is extrapolated
	 */
	AZIMUTH_EXTRAPOLATED(""), 
	
	/**
	 * String with details about an extrapolated azimuth prediction.
	 */
	AZIMUTH_EXTRAPOLATION_MESSAGE(""),
	
	/**
	 * Boolean indicating whether or not a predicted hydroacoustic slowness observation is blocked
	 */
	SLOWNESS_BLOCKED(""),
	
	/**
	 * Boolean indicating whether or not a predicted slowness observation is extrapolated
	 */
	SLOWNESS_EXTRAPOLATED(""), 
	
	/**
	 * String with details about an extrapolated slowness prediction.
	 * <p>Possible messages:
	 * <ul>
	 * <li>No extrapolation
	 * <li>Extrapolated point < first distance
	 * <li>Extrapolated point > last distance
	 * <li>Extrapolated point < first depth
	 * <li>Extrapolated point > last depth
	 * <li>Extrapolated point < first distance and < first depth
	 * <li>Extrapolated point > last distance and < first depth
	 * <li>Extrapolated point < first distance and > last depth
	 * <li>Extrapolated point > last distance and > last depth
	 * </ul>
	 */
	SLOWNESS_EXTRAPOLATION_MESSAGE(""),
	
	;
	
	private String units;

	private int formatWidth;

	private int formatPrecision;

	private String format;

	private String shortFormat;

	GeoAttributes(String units, int width, int precision) {
		this.units = units;
		formatWidth = width;
		formatPrecision = precision;
		format = "%" + formatWidth + "." + formatPrecision + "f";
		shortFormat = "%1." + formatPrecision + "f";
	}

	GeoAttributes(String units) {
		this(units, 11, 3);
	}

	/**
	 * Retrieve the units for the associated GeoAttribute.
	 * 
	 * @return String
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * Returns a format specifier such as "%8.3f" that specifies a reasonable format
	 * for printing values to an output table.
	 * 
	 * @return String
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Returns a format specifier such as "%1.3f" that specifies a reasonable
	 * precision for printing values to an output table.
	 * 
	 * @return String
	 */
	public String getShortFormat() {
		return shortFormat;
	}

	public int getFormatWidth() {
		return formatWidth;
	}

	public void setFormatWidth(int formatWidth) {
		this.formatWidth = formatWidth;
	}

	public int getFormatPrecision() {
		return formatPrecision;
	}

	public void setFormatPrecision(int formatPrecision) {
		this.formatPrecision = formatPrecision;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setShortFormat(String shortFormat) {
		this.shortFormat = shortFormat;
	}

	static public String enumSetToString(EnumSet<GeoAttributes> attributes) {
		StringBuffer buf = new StringBuffer("[ ");
		for (GeoAttributes a : attributes)
			buf.append(a.toString()).append(' ');
		buf.append("]");
		return buf.toString();
	}
};
