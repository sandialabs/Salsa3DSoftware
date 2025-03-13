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

import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * 
 * <p>
 * An Enumeration of Earth shapes including a spherical earth 
 * and a number of different ellipsoids.
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
public enum EarthShape
{
	/**
	 * The Earth is assumed to be a sphere of radius 6371 km.
	 */
	SPHERE(Double.POSITIVE_INFINITY, 6371.),

	/**
	 * The Earth is assumed to be an ellipsoid whose shape is defined by the
	 * GRS80 ellipsoid specification.
	 */
	GRS80(298.257222101, 6378.137),

	/**
	 * A hybrid coordinate system where latitudes are converted between
	 * geodetic and geocentric values using the GRS80 ellipsoid, but
	 * conversions between depth and radius assume that the Earth has
	 * constant radius of 6371 km.
	 */
	GRS80_RCONST(298.257222101, 6371.),

	/**
	 * The Earth is assumed to be an ellipsoid whose shape is defined by the
	 * WGS84 ellipsoid specification.
	 */
	WGS84(298.257223563, 6378.137),

	/**
	 * A hybrid coordinate system where latitudes are converted between
	 * geodetic and geocentric values using the WGS84 ellipsoid, but
	 * conversions between depth and radius assume that the Earth has
	 * constant radius of 6371 km.
	 */
	WGS84_RCONST(298.257223563, 6371.),

	/**
	 * The Earth is assumed to be an ellipsoid whose shape is defined by the
	 * IERS ellipsoid specification.
	 */
	IERS2003(298.25642, 6378.1366),

	/**
	 * A hybrid coordinate system where latitudes are converted between
	 * geodetic and geocentric values using the IERS ellipsoid, but
	 * conversions between depth and radius assume that the Earth has
	 * constant radius of 6371 km.
	 */
	IERS2003_RCONST(298.25642, 6371.);
	
	/**
	 * True for EarthShapes that assume that the Earth has constant radius
	 * for purposes of converting between radius and depth.
	 */
	public final boolean constantRadius;

	/**
	 * The radius of the earth at the equator.
	 */
	public final double equatorialRadius;

	/**
	 * flattening equals [ 1 - b/a ] where a is equatorial radius and
	 *  b is the polar radius
	 */
	public final double flattening;

	/**
	 * [ 1./flattening ]
	 */
	public final double inverseFlattening;

	/**
	 * Eccentricity squared.
	 * <p>
	 * Equals [ flattening * (2. - flattening) ]
	 * <p>
	 * Also equals [ 1 - sqr(b)/sqr(a) ] where a is the
	 * equatorial radius and b is the polar radius.
	 */
	public final double e2;
	
	/**
	 * constructor
	 * 
	 * @param inverseFlattening double
	 * @param equatorialRadius double
	 */
	private EarthShape(double inverseFlattening, double equatorialRadius) {
		this.equatorialRadius = equatorialRadius;

		this.inverseFlattening = inverseFlattening;
		if (inverseFlattening == Double.POSITIVE_INFINITY)
		{
			this.flattening = 0.;
			e2 = 0.;
			this.constantRadius = true;
		}
		else
		{
			this.flattening = 1./inverseFlattening;
			e2 = this.flattening*(2.-this.flattening);
			this.constantRadius = this.equatorialRadius < 6372.;
		}
	}

	/**
	 * Convert geocentricLat in radians to geographicLat in radians.
	 * @param geocentricLat
	 * @return geographicLat in radians.
	 */
	public double getGeographicLat(double geocentricLat) {
		return e2 == 0. ? geocentricLat : atan(tan(geocentricLat) / (1.-e2));
	}

	/**
	 * Convert geocentricCoLat in radians to geographicCoLat in radians.
	 * @param geocentricCoLat
	 * @return geographicCoLat in radians.
	 */
	public double getGeographicCoLat(double geocentricCoLat) {
		return 0.5*Math.PI - VectorGeo.getGeoGraphicLatitude(0.5*Math.PI - geocentricCoLat);
	}

	/**
	 * Convert geographicLat in radians to geocentricLat in radians.
	 * @param geographicLat
	 * @return geocentricLat in radians.
	 */
	public double getGeocentricLat(double geographicLat) {
		return e2 == 0. ? geographicLat : atan(tan(geographicLat) * (1.-e2));
	}
	
	/**
	 * Convert geocentricLat in degrees to geographicLat in degrees.
	 * @param geocentricLat
	 * @return geographicLat in degrees.
	 */
	public double getGeographicLatDegrees(double geocentricLat)
	{return Math.toDegrees(getGeographicLat(Math.toRadians(geocentricLat)));}

	/**
	 * Convert geographicLat in degrees to geocentricLat in degrees.
	 * @param geographicLat
	 * @return geocentricLat in degrees.
	 */
	public double getGeocentricLatDegrees(double geographicLat)
	{return Math.toDegrees(getGeocentricLat(Math.toRadians(geographicLat)));}

	/**
	 * Retrieve the radius of the Earth at the point defined by unit vector v.
	 * 
	 * @param v unit vector of the geographic position where earth radius is requested
	 * @return earth radius in km
	 */
	public double getEarthRadius(double[] v) {
		return constantRadius ? equatorialRadius 
				: equatorialRadius / sqrt(1. + e2/(1.-e2) *	sqr(v[2]));
	}

	/**
	 * Retrieve the radius of the Earth (km) at specified geographic latitude (radians).
	 * 
	 * @param latitude latitude in radians
	 * @return earth radius in km
	 */
	public double getEarthRadius(double latitude) {
		return constantRadius ? equatorialRadius : 
			equatorialRadius / sqrt(1. + e2/(1.-e2) * sqr(sin(getGeocentricLat(latitude))));
	}

	/**
	 * Retrieve the radius of the Earth (km) at specified geographic latitude (degrees).
	 * 
	 * @param latitude latitude in degrees
	 * @return earth radius in km
	 */
	public double getEarthRadiusDegrees(double latitude) { return getEarthRadius(toRadians(latitude)); }

	/**
	 * Retrieve the longitude of the point defined by unit vector v, in radians
	 * 
	 * @param v double[]
	 * @return double longitude in radians.  Values range from -PI to PI.
	 */
	public double getLon(double[] v) { return atan2(v[1], v[0]); }

	/**
	 * Retrieve the longitude of the point defined by unit vector v, in degrees
	 * 
	 * @param v double[]
	 * @return double longitude in radians.  Values range from -180 to 180.
	 */
	public double getLonDegrees(double[] v) {
		return toDegrees(getLon(v));
	}

	/**
	 * Retrieve the geographic latitude of the point defined by unit vector v, in radians.
	 * 
	 * @param v double[]
	 * @return  the geographic latitude of the point defined by unit vector v, in radians.
	 */
	public double getLat(double[] v) { return getGeographicLat(asin(v[2])); }

	/**
	 * Retrieve the geographic latitude of the point defined by unit vector v.
	 * 
	 * @param v double[]
	 * @return  the geographic latitude of the point defined by unit vector v, in degrees.
	 */
	public double getLatDegrees(double[] v) { return toDegrees(getLat(v)); }

	/**
	 * Retrieve the geocentric latitude of the point defined by unit vector v, in radians.
	 * 
	 * @param v double[]
	 * @return double
	 */
	public double getGeocentricLat(double[] v) { return asin(v[2]); }

	/**
	 * Retrieve the geocentric latitude of the point defined by unit vector v, in degrees.
	 * 
	 * @param v double[]
	 * @return double
	 */
	public double getGeocentricLatDegrees(double[] v) { return toDegrees(asin(v[2])); }

	/**
	 * Retrieve the geocentric co-latitude of the point defined by unit vector v, in radians.
	 * 
	 * @param v double[]
	 * @return double
	 */
	public double getGeocentricCoLat(double[] v) { return Math.acos(v[2]); }

	/**
	 * Retrieve the geocentric co-latitude of the point defined by unit vector v, in degrees.
	 * 
	 * @param v double[]
	 * @return double
	 */
	public double getGeocentricCoLatDegrees(double[] v) { return toDegrees(getGeocentricCoLat(v)); }

	/**
	 * Get a unit vector corresponding to a point on the Earth
	 * with the specified latitude and longitude.
	 * 
	 * @param lat the geographic latitude, in radians.
	 * @param lon the geographic longitude, in radians.
	 * @return The returned unit vector.
	 */
	public double[] getVector(double lat, double lon) {
		double[] v = new double[3];
		getVector(lat, lon, v);
		return v;
	}

	/**
	 * Get a unit vector corresponding to a point on the Earth
	 * with the specified latitude and longitude.
	 * 
	 * @param lat the geographic latitude, in degrees.
	 * @param lon the geographic longitude, in degrees.
	 * @return The returned unit vector.
	 */
	public double[] getVectorDegrees(double lat, double lon) {
		double[] v = new double[3];
		getVector(toRadians(lat), toRadians(lon), v);
		return v;
	}

	/**
	 * Get a unit vector corresponding to a point on the Earth
	 * with the specified latitude and longitude.
	 * 
	 * @param lat the geographic latitude, in degrees.
	 * @param lon the geographic longitude, in degrees.
	 * @param v the unit vector into which results will be copied.
	 */
	public void getVectorDegrees(double lat, double lon, double[] v) {
		getVector(toRadians(lat), toRadians(lon), v);
	}

	/**
	 * Get a unit vector corresponding to a point on the Earth
	 * with the specified geographic latitude and longitude.
	 * 
	 * @param lat the geographic latitude, in radians.
	 * @param lon the geographic longitude, in radians.
	 * @param v the unit vector into which results will be copied.
	 */
	public void getVector(double lat, double lon, double[] v) {
		lat = getGeocentricLat(lat);
		v[2] = sin(lat);
		lat = cos(lat);
		v[0] = lat * cos(lon);
		v[1] = lat * sin(lon);
	}
	
	/**
	 * Convert a unit vector to a String representation of lat, lon formated
	 * with "%10.6f %11.6f"
	 * 
	 * @param vector
	 * @return a String of lat,lon in degrees formatted with "%10.6f %11.6f"
	 */
	public String getLatLonString(double[] vector) {
		return String.format("%10.6f %11.6f", getLatDegrees(vector),
				             getLonDegrees(vector));
	}

	public String getLatLonString(double[] vector, int precision)  {
		return getLatLonString(vector, String.format("%%%d.%df %%%d.%df", 
				(precision+4), precision, (precision+5), precision));
	}

	public String getLatLonString(double[] vector, String format)  {
		return String.format(format, getLatDegrees(vector),
				             getLonDegrees(vector));
	}

	public String getLonLatString(double[] vector) {
		return String.format("%10.6f %11.6f", getLonDegrees(vector),
	             getLatDegrees(vector));
	}

	public String getLonLatString(double[] vector, String format)  {
		return String.format(format, getLonDegrees(vector),
				             getLatDegrees(vector));
	}

	/**
	 * If you have some radius value that is appropriate at the equator,
	 * multiply it by squash value in order to get the radius value
	 * appropriate at the latitude of the supplied unit vector.
	 * 
	 * @param v
	 *            double[]
	 * @return double
	 */
	public double getSquashFactor(double[] v)  {
		return (constantRadius ? 1. : 1. / sqrt(1. + e2/(1.-e2) * sqr(v[2])));
	}

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
	public double[][] getEllipse(double latCenter, double lonCenter, double majax, double minax, double trend, 
			int npoints, boolean inDegrees) {
		if (inDegrees)
		{
			double[] u = getVectorDegrees(latCenter, lonCenter);
			double r = getEarthRadius(u);
			double[][] points = VectorUnit.getEllipse(u, majax/r, minax/r, Math.toRadians(trend), npoints);
			for (int i=0; i<npoints; ++i)
				points[i] = new double[] { getLatDegrees(points[i]), getLonDegrees(points[i]) };
			return points;
		}
		else
		{
			double[] u = getVector(latCenter, lonCenter);
			double r = getEarthRadius(u);
			double[][] points = VectorUnit.getEllipse(u, majax/r, minax/r, trend, npoints);
			for (int i=0; i<npoints; ++i)
				points[i] = new double[] { getLat(points[i]), getLon(points[i]) };
			return points;
		}
	}
	
	private double sqr(double x) {return x*x;}

} // end of definition of enum EarthShape
