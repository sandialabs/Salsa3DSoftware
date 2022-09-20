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

/**
 * This class defines earth constants for use by EMPS code.
 * 
 * Note: On a GRS80 ellipsoid, earth radius = 6371. km at latitudes where unit
 * vector is [0.8169307371701541, 0.0, +/-0.5767357893061854]. At such points,
 * the geographic latitude is +/- 35.402807205794716 degrees and the geocentric
 * latitude is +/- 35.221281425820400 degrees
 */
public class EarthConst {

	/**
	 * Average Earth radius in km.
	 */
	public static final double EARTH_RAD = 6371.0;

	/**
	 * Earth equatorial radius in km (GRS80 ellipsoid).
	 */
	public static final double EARTH_A = 6378.137;

	/**
	 * Ellipsoidal Earth flattening parameter (GRS80 ellipsoid).
	 */
	public static final double EARTH_F = 1.0 / 298.257222101;

	/**
	 * 1 - Earth axis ratio squared. EARTH_E is the eccentricity squared. EARTH_E =
	 * 0.006694380022900787
	 */
	public static final double EARTH_E = EARTH_F * (2.0 - EARTH_F);

	/**
	 * Ellipsoidal Earth axis ratio (GRS80 semi-minor over semi-major axes).
	 */
	public static final double EARTH_AXIS_RATIO = 1.0 - EARTH_F;

	/**
	 * Ellipsoidal Earth axis ratio squared (also = 1.0 - EARTH_E).
	 * EARTH_AXIS_RATIO_SQUARED = 0.9933056199770992
	 */
	public static final double EARTH_AXIS_RATIO_SQUARED = 1.0 - EARTH_E;

	/**
	 * Converts the input geodetic latitude to a geocentric latitude.
	 * 
	 * @param geodeticLat Geodetic latitude.
	 * @return Geocentric latitude.
	 */
	public static double geodeticToGeocentricLatitude(double geodeticLat) {
		return Math.atan(EARTH_AXIS_RATIO_SQUARED * Math.tan(geodeticLat));
	}

	/**
	 * Converts the input geocentric latitude to a geodetic latitude.
	 * 
	 * @param geocentricLat Geocentric latitude.
	 * @return Geodetic latitude.
	 */
	public static double geocentricToGeodeticLatitude(double geocentricLat) {
		return Math.atan(Math.tan(geocentricLat) / EARTH_AXIS_RATIO_SQUARED);
	}

}
