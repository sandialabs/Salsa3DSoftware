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
package gov.sandia.gmp.util.numerical.vector;

public class VectorGeo extends VectorUnit
{
	/**
	 *  The shape of the Earth that is used to convert between geocentric and
	 *  geographic latitude and between depth and radius.  The default is WGS84.
	 * <ul>
	 * <li>SPHERE - Geocentric and geographic latitudes are identical and
	 * conversion between depth and radius assume the Earth is a sphere
	 * with constant radius of 6371 km.
	 * <li>GRS80 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the GRS80 ellipsoid.
	 * <li>GRS80_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the GRS80 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>WGS84 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the WGS84 ellipsoid.
	 * <li>WGS84_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the WGS84 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>IERS2003 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the IERS2003 ellipsoid.
	 * <li>IERS2003_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the IERS2003 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * </ul>
	 */
	public static EarthShape earthShape = EarthShape.WGS84;
	
	public static void setApproximateLatitudes(boolean approximateLatitudes) 
	{ earthShape.approximateLatitudes = approximateLatitudes; }
	
	/**
	 * Retrieve the radius of the Earth in km at the position specified by an
	 * Earth-centered unit vector.
	 * 
	 * @param vector
	 *            Earth-centered unit vector
	 * @return radius of the Earth in km at specified position.
	 */
	public static double getEarthRadius(double[] vector)
	{ return earthShape.getEarthRadius(vector); }

	/**
	 * Convert a 3-component unit vector to geographic latitude, in radians.
	 * 
	 * @param vector
	 *            3-component unit vector
	 * @return geographic latitude in radians.
	 */
	public static double getLat(double[] vector)
	{ return earthShape.getLat(vector); }

	/**
	 * Convert a 3-component unit vector to a longitude, in radians.
	 * 
	 * @param vector
	 *            3 component unit vector
	 * @return longitude in radians.
	 */
	public static double getLon(double[] vector)
	{ return earthShape.getLon(vector); }

	/**
	 * Convert a 3-component unit vector to geographic latitude, in degrees.
	 * 
	 * @param vector
	 *            3-component unit vector
	 * @return geographic latitude in degrees.
	 */
	public static double getLatDegrees(double[] vector)
	{ return earthShape.getLatDegrees(vector); }

	/**
	 * Convert a 3-component unit vector to a longitude, in degrees.
	 * 
	 * @param vector
	 *            3 component unit vector
	 * @return longitude in degrees.
	 */
	public static double getLonDegrees(double[] vector)
	{ return earthShape.getLonDegrees(vector); }

	/**
	 * Convert a unit vector to a String representation of lat, lon formated
	 * with "%9.5f %10.5f"
	 * 
	 * @param vector
	 * @return a String of lat,lon in degrees formatted with "%9.5f %10.5f"
	 */
	public static String getLatLonString(double[] vector)
	{
		return String.format("%9.5f %10.5f", earthShape.getLatDegrees(vector),
				earthShape.getLonDegrees(vector));
	}

	public static String getLatLonString(double[] vector, int precision) 
	{
		return getLatLonString(vector, String.format("%%%d.%df %%%d.%df", 
				(precision+4), precision, (precision+5), precision));
	}

	public static String getLatLonString(double[] vector, String format) 
	{
		return String.format(format, earthShape.getLatDegrees(vector),
				earthShape.getLonDegrees(vector));
	}

	/**
	 * Convert geographic lat, lon into a geocentric unit vector. The
	 * x-component points toward lat,lon = 0, 0. The y-component points toward
	 * lat,lon = 0, 90. The z-component points toward north pole.
	 * 
	 * @param lat
	 *            geographic latitude in degrees.
	 * @param lon
	 *            longitude in degrees.
	 * @return 3 component unit vector.
	 */
	public static double[] getVectorDegrees(double lat, double lon)
	{ return earthShape.getVectorDegrees(lat,  lon); }

	/**
	 * Convert geographic lat, lon into a geocentric unit vector. The
	 * x-component points toward lat,lon = 0, 0. The y-component points toward
	 * lat,lon = 0, 90. The z-component points toward north pole.
	 * 
	 * @param lat
	 *            geographic latitude in degrees.
	 * @param lon
	 *            longitude in degrees.
	 * @param vector
	 *            3 component unit vector.
	 */
	public static void getVectorDegrees(double lat, double lon, double[] vector)
	{ earthShape.getVectorDegrees(lat, lon, vector); }

	/**
	 * Convert geographic lat, lon into a geocentric unit vector. The
	 * x-component points toward lat,lon = 0, 0. The y-component points toward
	 * lat,lon = 0, PI/2. The z-component points toward north pole.
	 * 
	 * @param lat
	 *            geographic latitude in radians.
	 * @param lon
	 *            longitude in radians.
	 * @return 3 component unit vector.
	 */
	public static double[] getVector(double lat, double lon)
	{ return earthShape.getVector(lat, lon); }

	/**
	 * Convert geographic lat, lon into a geocentric unit vector. The
	 * x-component points toward lat,lon = 0, 0. The y-component points toward
	 * lat,lon = 0, PI/2 The z-component points toward north pole.
	 * 
	 * @param lat
	 *            geographic latitude in radians.
	 * @param lon
	 *            longitude in radians.
	 * @param vector
	 *            3 component unit vector.
	 */
	public static void getVector(double lat, double lon, double[] vector)
	{ earthShape.getVector(lat, lon, vector); }
	
	/**
	 * Return geocentric latitude given a geographic latitude
	 * 
	 * @param lat
	 *            geographic latitude in radians
	 * @return geocentric latitude in radians
	 */
	public static double getGeoCentricLatitude(double lat)
	{ return earthShape.getGeocentricLat(lat); }

	/**
	 * Return geocentric co-latitude given a unit vector
	 * 
	 * @param a unit vector
	 * @return geocentric co-latitude in radians
	 */
	public static double getGeoCentricCoLatitude(double[] unitVector)
	{ return earthShape.getGeocentricCoLat(unitVector); }

	/**
	 * Return geographic latitude given a geocentric latitude
	 * 
	 * @param lat
	 *            geocentric latitude in radians
	 * @return geographic latitude in radians
	 */
	public static double getGeoGraphicLatitude(double lat)
	{ return earthShape.getGeographicLat(lat); }

	/**
	 * Compute points that define an ellipse centered at a specified point.
	 * @param latCenter latitude of center of ellipse
	 * @param lonCenter longiitude of center of ellipse
	 * @param majax the length of the major axis of the ellipse, in km.
	 * @param minax the length of the minor axis of the ellipse, in km.
	 * @param trend the orientation relative to north of the major axis of the 
	 * ellipse.
	 * @param npoints the number of points to define the ellipse
	 * @param inDegrees if true, centerLat, centerLon, trend and all return 
	 * values have units of degrees, otherwise, the units are radians.
	 * @return an array with dimensions npoints x 2 containing the latitude and 
	 * longitude of points that define the ellipse.
	 */
	public static double[][] getEllipse(double latCenter, double lonCenter, double majax, double minax, double trend, 
			int npoints, boolean inDegrees)
	{ return earthShape.getEllipse(latCenter, lonCenter, majax, minax, trend, npoints, inDegrees); }

}
