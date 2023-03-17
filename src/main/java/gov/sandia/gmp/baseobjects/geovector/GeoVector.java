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
package gov.sandia.gmp.baseobjects.geovector;

import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * <p>
 * GeoVector manages a single point in proximity to the Earth. A point is
 * represented by a 3D vector with its origin at the center of the Earth. The x
 * component of the vector points toward the point on the surface of the Earth
 * with latitude, longitude = 0, 0. The y component of the vector points toward
 * the point on the surface of the Earth with latitude, longitude = 0, 90
 * degrees. The z component points toward the north pole. For efficiency, the
 * vector is represented internally by a unit vector, v, and the length of the
 * vector, radius, in km.
 * 
 * <p>
 * GeoVector requires knowledge of the shape of the Earth in order to convert
 * between geodetic and geocentric latitudes, and to convert between depth and
 * radius. GeoVector can function without this knowledge, but without it method
 * calls which require it will throw a GeoVectorException.
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * <p>
 * Author: Sandy Ballard
 * <p>
 * Version 2.1.0
 */
public class GeoVector implements Cloneable, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3278197063285112748L;

    private EarthShape earthShape;

    /**
     * v is the geocentric unit vector that describes the position on the earth. The
     * origin of the vector is at the center of the earth. The x-component points to
     * lon,lat = 0,0. y-component points to lon,lat = PI/2,0 and the z-component
     * points to lon,lat = 0,PI/2.
     */
    protected double[] v;

    /**
     * The distance from the center of the earth to this GeoVector in km.
     */
    protected double radius;

    /**
     * Default constructor. Sets lat=0, lon=0, radius=1 km.
     */
    public GeoVector() {
	this(EarthShape.WGS84); 
    }

    public GeoVector(EarthShape earthShape) {
	this.earthShape = earthShape;
	v = new double[] { 1., 0., 0. };
	radius = 1.;
    }

    /**
     * Constructor specifying unit vector with origin at center of earth. Makes a
     * deep copy of unitVector. No checks are performed that the input vector is
     * really of unit length.
     * 
     * @param unitVector double[] 3-component unit vector.
     * @param radius     double radius in km.
     */
    public GeoVector(double unitVector[], double radius, EarthShape earthShape) {
	this.earthShape = earthShape;
	if (unitVector[0] == 0. && unitVector[1] == 0. && unitVector[2] == 0.) {
	    v = new double[] { 1., 0., 0. };
	    radius = 0.;
	} else {
	    this.v = unitVector.clone();
	    this.radius = radius;
	}
    }

    /**
     * Constructor specifying 3D vector with origin at center of earth. this.v set
     * to deep copy of vector, radius is set to length of this.v and then this.v is
     * normalized to unit length.
     * 
     * @param vector double[] 3-component vector.
     */
    public GeoVector(double vector[], EarthShape earthShape) {
	this.earthShape = earthShape;
	this.v = vector.clone();
	this.radius = VectorUnit.normalize(this.v);
	if (radius == 0.)
	    v[0] = 1.;
    }

    /**
     * Constructor based on geographic latitude, longitude and depth. If inDegrees
     * is true, latitude and longitude are converted from degrees to radians before
     * calculation.
     * 
     * @param lat       double
     * @param lon       double
     * @param depth     double
     * @param inDegrees boolean
     */
    public GeoVector(double lat, double lon, double depth, boolean inDegrees, EarthShape earthShape) {
	this.earthShape = earthShape;
	v = new double[3];
	setGeoVector(lat, lon, depth, inDegrees);
    }

    /**
     * Constructor specifying unit vector with origin at center of earth. Makes a
     * deep copy of unitVector. No checks are performed that the input vector is
     * really of unit length.
     * 
     * @param unitVector double[] 3-component unit vector.
     * @param radius     double radius in km.
     */
    public GeoVector(double unitVector[], double radius) {
	this.earthShape = EarthShape.WGS84; 
	if (unitVector[0] == 0. && unitVector[1] == 0. && unitVector[2] == 0.) {
	    v = new double[] { 1., 0., 0. };
	    radius = 0.;
	} else {
	    this.v = unitVector.clone();
	    this.radius = radius;
	}
    }

    /**
     * Constructor specifying 3D vector with origin at center of earth. this.v set
     * to deep copy of vector, radius is set to length of this.v and then this.v is
     * normalized to unit length.
     * 
     * @param vector double[] 3-component vector.
     */
    public GeoVector(double vector[]) {
	this.earthShape = EarthShape.WGS84; 
	this.v = vector.clone();
	this.radius = VectorUnit.normalize(this.v);
	if (radius == 0.)
	    v[0] = 1.;
    }

    /**
     * Copy constructor. Returns a deep copy of this GeoVector.
     * 
     * @param g GeoVector
     */
    public GeoVector(GeoVector g) {
	this.earthShape = g.earthShape;
	v = g.v.clone();
	radius = g.radius;
    }

    /**
     * The new GeoVector is located on the straight line from gv1 to gv2, halfway
     * between the two. gv1 and gv2 are converted to 3d vectors and added together.
     * this.v is set equal to the sum and normalized to unit length. this.radius is
     * set equal to 0.5 * the sum.
     * 
     * @param gv1 GeoVector
     * @param gv2 GeoVector
     * @throws Exception 
     */
    public GeoVector(GeoVector gv1, GeoVector gv2) throws Exception {
	if (gv1.earthShape != gv2.earthShape)
	    throw new Exception("gv1.earthShape != gv2.earthShape");
	this.earthShape = gv1.earthShape;
	v = new double[3];
	midpoint(gv1, gv2);
    }

    /**
     * Constructor based on geographic latitude, longitude and depth. If inDegrees
     * is true, latitude and longitude are converted from degrees to radians before
     * calculation.
     * 
     * @param lat       double
     * @param lon       double
     * @param depth     double
     * @param inDegrees boolean
     */
    public GeoVector(double lat, double lon, double depth, boolean inDegrees) {
	this.earthShape = EarthShape.WGS84; 
	v = new double[3];
	setGeoVector(lat, lon, depth, inDegrees);
    }

    /**
     * Set the geographic lat, lon and depth of this GeoVector.
     * 
     * @param lat       the geographic latitude (units vary).
     * @param lon       the geographic longitude (units vary).
     * @param depth     the depth in km.
     * @param inDegrees if true, lat and lon are converted from degrees to radians
     *                  before calculations.
     */
    public void setGeoVector(double lat, double lon, double depth, boolean inDegrees) {
	if (inDegrees)
	    earthShape.getVectorDegrees(lat, lon, v);
	else
	    earthShape.getVector(lat, lon, v);
	setDepth(depth);
    }

    /**
     * Sets this GeoVector's unit vector equal to a reference to the input vector
     * (shallow copy). Input vector is assumed to be a unit vector but the
     * assumption is not checked.
     * 
     * @param v      double[]
     * @param radius double
     */
    public void setGeoVector(double[] v, double radius) {
	this.v[0] = v[0];
	this.v[1] = v[1];
	this.v[2] = v[2];
	this.radius = radius;
	if (this.v[0] == 0. && this.v[1] == 0. && this.v[2] == 0.)
	    this.v[0] = 1.;
    }

    /**
     * Sets this GeoVector's v equal to a copy of the input v. Then this GeoVector's
     * radius is set to the length of vector v and v is normalized to unit length.
     * The input array v is not modified by this routine.
     * 
     * @param v double[]
     * @throws GeoModelException
     */
    public void setGeoVector(double[] v) {
	this.v[0] = v[0];
	this.v[1] = v[1];
	this.v[2] = v[2];
	this.radius = VectorUnit.normalize(this.v);
	if (radius <= 0.)
	    v[0] = 1.;
    }

    /**
     * Returns a deep copy of this GeoVector.
     * 
     * @return GeoVector the returned GeoVector will have the same EarthShape as
     *         this.
     */
    @Override
    public GeoVector clone() {
	return new GeoVector(this);
    }

    /**
     * Set this GeoVector equal to a deep copy of other GeoVector. If this and other
     * have different EarthShapes, then latitudes and depths will be different.
     * 
     * @param other GeoVector
     */
    public void copy(GeoVector other) {
	v[0] = other.v[0];
	v[1] = other.v[1];
	v[2] = other.v[2];
	radius = other.radius;
    }

    /**
     * Returns earth radius at this position divided by equatorial_radius.
     * 
     * @return double
     */
    public double getSquashFactor() {
	return earthShape.getSquashFactor(v);
    }

    /**
     * Retrieve the latitude of this GeoVector.
     * 
     * @return geographic latitude, in radians.
     */
    public double getLat() {
	// X return getEarthShape().getGeodeticLatitude(v);
	return earthShape.getLat(v);
    }

    /**
     * Retrieve the radius of the Earth at the latitude of this GeoVector.
     * 
     * @return the radius of the Earth in km.
     */
    public double getEarthRadius() {
	return earthShape.getEarthRadius(v);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((earthShape == null) ? 0 : earthShape.hashCode());
	long temp;
	temp = Double.doubleToLongBits(radius);
	result = prime * result + (int) (temp ^ (temp >>> 32));
	result = prime * result + Arrays.hashCode(v);
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	GeoVector other = (GeoVector) obj;
	if (earthShape != other.earthShape)
	    return false;
	if (Double.doubleToLongBits(radius) != Double.doubleToLongBits(other.radius))
	    return false;
	if (!Arrays.equals(v, other.v))
	    return false;
	return true;
    }

    public boolean close(GeoVector other, double dkm) {
	return Math.pow(v[0] * radius - other.v[0] * other.radius, 2)
		+ Math.pow(v[1] * radius - other.v[1] * other.radius, 2)
		+ Math.pow(v[2] * radius - other.v[2] * other.radius, 2) < dkm * dkm;
    }

    /**
     * Returns a convenient string representation of this GeoVector. Geodetic
     * latitude in degrees, longitude in degrees, depth in km.
     * 
     * @return String a convenient string representation of this GeoVector.
     */
    @Override
    public String toString() {
	return toString("%10.6f, %11.6f, %8.3f");
    }

    /**
     * Returns a convenient string representation of this GeoVector. Geodetic
     * latitude in degrees, longitude in degrees, depth in km.
     * 
     * @return String a convenient string representation of this GeoVector.
     */
    public String toString(String format) {
	return String.format(format, getLatDegrees(), getLonDegrees(), getDepth());
    }

    /**
     * Same as toString() but if called on a derived class returns
     * GeoVector.toString(), not derived class's toString().
     * 
     * @return String a convenient string representation of this GeoVector.
     */
    public String geovectorToString() {
	return String.format("%10.6f, %11.6f, %8.3f", getLatDegrees(), getLonDegrees(), getDepth());
    }

    /**
     * Retrieve the 3D vector (unit vector times radius) formatted as a String.
     * 
     * @return String
     */
    public String toStringVector() {
	return String.format("%1.9f %1.9f %1.9f", v[0] * radius, v[1] * radius, v[2] * radius);

    }

    /**
     * Convert this GeoVector into its antipode by reversing the sign of all three
     * components of its unit vector.
     */
    public void flip() {
	Vector3D.negate(v);
    }

    /**
     * Set the radius of this GeoVector.
     * 
     * @param r the radius in km.
     */
    public GeoVector setRadius(double r) {
	radius = r;
	return this;
    }

    /**
     * Retrieve the radius of this GeoVector object.
     * 
     * @return the radius in km.
     */
    public double getRadius() {
	return radius;
    }

    /**
     * Retrieve the depth of this GeoVector.
     * 
     * @return the depth of this GeoVector in km.
     */
    public double getDepth() {
	return getEarthRadius() - radius;
    }

    /**
     * Set the depth of this GeoVector.
     * 
     * @param depth the desired depth in km.
     * @return
     */
    public GeoVector setDepth(double depth) {
	radius = getEarthRadius() - depth;
	return this;
    }

    /**
     * Returns the geocentric angular distance, in radians, from this GeoVector to
     * some other GeoVector.
     * 
     * @param other the other GeoVector to which this GeoVector is to be compared.
     * @return double separation of the GeoVectors in radians.
     */
    public double distance(GeoVector other) {
	return VectorUnit.angle(v, other.v);
    }

    /**
     * Return the geocentric angular distance, in degrees, from this GeoVector to
     * some other GeoVector.
     * 
     * @param other the other GeoVector to which this GeoVector is to be compared.
     * @return double separation of the GeoVectors in degrees.
     */
    public double distanceDegrees(GeoVector other) {
	return VectorUnit.angleDegrees(v, other.v);
    }

    /**
     * Returns the straight-line distance in km from the tip of this GeoVector to
     * the tip of other GeoVector.
     * 
     * @param other GeoVector
     * @return double
     */
    public double distance3D(GeoVector other) {
	return sqrt(Math.pow(v[0] * radius - other.v[0] * other.radius, 2)
		+ Math.pow(v[1] * radius - other.v[1] * other.radius, 2)
		+ Math.pow(v[2] * radius - other.v[2] * other.radius, 2));
    }

    /**
     * Returns the distance from this to other, in km, as measured along the surface
     * of the earth.
     * 
     * @param other GeoVector
     * @return double
     * @throws GeoVectorException
     */
    public double getDistanceKm(GeoVector other) {
	return (new GreatCircle(this.v, other.v)).getDistanceKm();
    }

    /**
     * Find the azimuth from this GeoVector to some other GeoVector. Result will be
     * between -PI and PI radians.
     * 
     * @param other      the other GeoVector to which the azimuth is requested.
     * @param errorValue double
     * @return the azimuth from this to other, in radians clockwise from north. If
     *         locations are coincident or anti-parallel, or if this is either the
     *         north or south pole, then returns errorValue.
     */
    public double azimuth(GeoVector other, double errorValue) {
	double az = VectorUnit.azimuth(v, other.v, errorValue);
	return (Double.isNaN(az) ? errorValue : az);
    }

    /**
     * Find the azimuth from this GeoVector to some other GeoVector, in degrees.
     * Result will be between -180 to 180 degrees.
     * 
     * @param other      the other GeoVector to which the azimuth is requested.
     * @param errorValue double
     * @return the azimuth from this to other, in degrees clockwise from north.
     */
    public double azimuthDegrees(GeoVector other, double errorValue) {
	double az = VectorUnit.azimuth(v, other.v, errorValue);
	return (Double.isNaN(az) ? errorValue : toDegrees(az));
    }

    /**
     * returns true if this GeoVector resides at either north or south pole.
     * 
     * @return
     */
    public boolean isPole() {
	return VectorUnit.isPole(v);
    }

    /**
     * Retrieve the geographic latitude of this GeoVector.
     * 
     * @return geographic latitude, in degrees.
     */
    public double getLatDegrees() {
	return toDegrees(getLat());
    }

    /**
     * Retrieve the geocentric colatitude of this GeoVector.
     * 
     * @return geocentric colatitude, in radians.
     */
    public double getGeocentricCoLat() {
	return acos(v[2]);
    }

    /**
     * Retrieve the geocentric latitude of this GeoVector.
     * 
     * @return geocentric latitude, in radians.
     */
    public double getGeocentricLat() {
	return asin(v[2]);
    }

    /**
     * Retrieve the geocentric latitude of this GeoVector, in degrees.
     * 
     * @return geocentric latitude, in degrees.
     */
    public double getGeocentricLatDegrees() {
	return toDegrees(asin(v[2]));
    }

    /**
     * Retrieve the longitude of this GeoVector. Value will be between -PI and PI
     * radians.
     * 
     * @return longitude, in radians.
     */
    public double getLon() {
	return atan2(v[1], v[0]);
    }

    /**
     * Retrieve the longitude of this GeoVector, in degrees. Value will be between
     * -180 and 180 degrees.
     * 
     * @return longitude, in degrees.
     */
    public double getLonDegrees() {
	return toDegrees(getLon());
    }

    /**
     * Retrieve a deep copy of this GeoVector's unit vector.
     * 
     * @param u double[] 3-element array to be populated with a copy of this
     *          GeoVector's unit vector.
     */
    public void getUnitVector(double[] u) {
	u[0] = v[0];
	u[1] = v[1];
	u[2] = v[2];
    }

    /**
     * Retrieve a reference to this GeoVector's unit vector.
     * 
     * @return double[]
     */
    public double[] getUnitVector() {
	return v;
    }

    /**
     * Set this GeoVector's unit vector equal to deep copy of specified vector.
     * 
     * @param u double[] 3-element unit vector. No checking is performed that u is
     *          valid.
     */
    public void setUnitVector(double[] u) {
	v[0] = u[0];
	v[1] = u[1];
	v[2] = u[2];
    }

    /**
     * Retrieve a deep copy of this GeoVector's unit vector multiplied by its
     * radius.
     * 
     * @return double[]
     */
    public double[] getVector() {
	double[] u = new double[3];
	getVector(u);
	return u;
    }

    /**
     * Retrieve a deep copy of this GeoVector's unit vector multiplied by its
     * radius.
     * 
     * @param u double[] 3-element array to be populated with a copy of this
     *          GeoVector's vector.
     */
    public void getVector(double[] u) {
	u[0] = v[0] * radius;
	u[1] = v[1] * radius;
	u[2] = v[2] * radius;
    }

    /**
     * Find the normalized unit vector that is the vector sum of the g1's unit
     * vector and g2's unit vector.
     * 
     * @param g1 GeoVector
     * @param g2 GeoVector
     * @param u  double[]
     * @return boolean true if mean vector has length > 0
     */
    public static boolean mean(GeoVector g1, GeoVector g2, double[] u) {
	double[] c = VectorUnit.center(g1.v, g2.v);
	u[0] = c[0];
	u[1] = c[1];
	u[2] = c[2];
	return (VectorUnit.length(u) > 0.0) ? true : false;
    }

    /**
     * Retrieve the GeoVector that has unit vector equal to vector mean of the unit
     * vectors of two other GeoVectors. Radius is set to mean radius of other two
     * GeoVectors.
     * 
     * @param g1
     * @param g2
     * @return GeoVector
     */
    public static GeoVector mean(GeoVector g1, GeoVector g2) {
	GeoVector gv = new GeoVector();
	gv.v[0] = g1.v[0] + g2.v[0];
	gv.v[1] = g1.v[1] + g2.v[1];
	gv.v[2] = g1.v[2] + g2.v[2];
	VectorUnit.normalize(gv.v);
	gv.radius = 0.5 * (g1.radius + g2.radius);
	return gv;
    }

    /**
     * Set the position of this GeoVector to be vector mean of the supplied
     * collection of GeoVectors (their unit vectors). The radius of the result is
     * the average radius.
     * 
     * @param geoVectors Collection
     */
    public void mean(Object[] geoVectors) {
	v[0] = 0;
	v[1] = 0;
	v[2] = 0;
	radius = 0.;
	for (int i = 0; i < geoVectors.length; ++i) {
	    v[0] += ((GeoVector) geoVectors[i]).v[0];
	    v[1] += ((GeoVector) geoVectors[i]).v[1];
	    v[2] += ((GeoVector) geoVectors[i]).v[2];
	    radius += ((GeoVector) geoVectors[i]).radius;
	}
	VectorUnit.normalize(v);
	radius /= geoVectors.length;
    }

    /**
     * Set the position of this GeoVector to be vector mean of the supplied
     * collection of GeoVectors (their unit vectors). The radius of the result is
     * the average radius.
     * 
     * @param geoVectors Collection
     */
    public void mean(Collection<GeoVector> geoVectors) {
	mean(geoVectors.toArray());
    }

    /**
     * Return a new GeoVector which is the vector mean of the supplied Collection of
     * GeoVectors.
     * 
     * @param geoVectors Collection
     * @return GeoVector
     */
    public static GeoVector getMean(Object[] geoVectors) {
	GeoVector gv = ((GeoVector) geoVectors[0]).clone();
	gv.mean(geoVectors);
	return gv;
    }

    /**
     * Return a new GeoVector which is the vector mean of the supplied Collection of
     * GeoVectors.
     * 
     * @param geoVectors Collection
     * @return GeoVector
     */
    public static GeoVector getMean(Collection<GeoVector> geoVectors) {
	GeoVector gv = new GeoVector();
	gv.mean(geoVectors);
	return gv;
    }

    /**
     * Subtract other vector (unit vector * radius) from this vector and return
     * result in d.
     * 
     * @param other GeoVector
     * @param d     double[]
     */
    public void minus(GeoVector other, double[] d) {
	d[0] = v[0] * radius - other.v[0] * other.radius;
	d[1] = v[1] * radius - other.v[1] * other.radius;
	d[2] = v[2] * radius - other.v[2] * other.radius;
    }

    /**
     * Subtract other vector (unit vector * radius) from this vector and return
     * result in a new double[]
     * 
     * @param other GeoVector
     * @return double[]
     */
    public double[] minus(GeoVector other) {
	double[] d = new double[3];
	minus(other, d);
	return d;
    }

    /**
     * Set the position of this GeoVector to the sum of its current full vector
     * position plus u.
     * 
     * @param u
     */
    public void add(double[] u) {
	double[] w = getVector();
	Vector3D.increment(w, u);
	setRadius(VectorUnit.normalize(w));
	v[0] = w[0];
	v[1] = w[1];
	v[2] = w[2];
    }

    /**
     * Sets this GeoVector equal to a position halfway between g1 and g2 in 3D
     * sense. Equivalent to calling interpolate with fraction = 0.5;
     * 
     * @param g1 GeoVector
     * @param g2 GeoVector
     */
    public void midpoint(GeoVector g1, GeoVector g2) {
	v[0] = g1.v[0] * g1.radius + g2.v[0] * g2.radius;
	v[1] = g1.v[1] * g1.radius + g2.v[1] * g2.radius;
	v[2] = g1.v[2] * g1.radius + g2.v[2] * g2.radius;
	radius = 0.5 * VectorUnit.normalize(v);
    }

    /**
     * Retrieve a new GeoVector that is halfway along the straight line that
     * connects g1 and g2.
     * 
     * @param g1
     * @param g2
     * @return
     */
    static public GeoVector getMidpoint(GeoVector g1, GeoVector g2) {
	GeoVector gv = g1.clone();
	gv.midpoint(g1, g2);
	return gv;
    }

    /**
     * Replaces this GeoVector with one that is interpolated from the two input
     * GeoVectors. Calculates the line from g1 to g2. Finds a point on that line
     * that is fractional distance from g1 to g2. If fraction == 0 returns copy of
     * g1. If fraction == 1, returns copy of g2. For other values, interpolates.
     * 
     * @param g1       GeoVector
     * @param g2       GeoVector
     * @param fraction double
     */
    public void interpolate(GeoVector g1, GeoVector g2, double fraction) {
	v[0] = g1.v[0] * g1.radius + (g2.v[0] * g2.radius - g1.v[0] * g1.radius) * fraction;
	v[1] = g1.v[1] * g1.radius + (g2.v[1] * g2.radius - g1.v[1] * g1.radius) * fraction;
	v[2] = g1.v[2] * g1.radius + (g2.v[2] * g2.radius - g1.v[2] * g1.radius) * fraction;
	radius = VectorUnit.normalize(v);
    }

    /**
     * Return a new GeoVector that is interpolated from the two input GeoVectors.
     * Calculates the line from g1 to g2. Finds a point on that line that is
     * fractional distance from g1 to g2. If fraction == 0 returns copy of g1. If
     * fraction == 1, returns copy of g2. For other values, interpolates.
     * 
     * @param g1       GeoVector
     * @param g2       GeoVector
     * @param fraction double
     * @return GeoVector a new GeoVector interpolated from two input GeoVectors.
     */
    public static GeoVector getNewInterpolate(GeoVector g1, GeoVector g2, double fraction) {
	GeoVector gv = g1.clone();
	gv.interpolate(g1, g2, fraction);
	return gv;
    }

    /**
     * Find the normal to the plane defined by the tips of three GeoVectors. Given
     * two other GeoVectors, n1 and n2, returns (n1-this) cross (n2-this),
     * normalized to unit vector;
     * 
     * @param n1     GeoVector
     * @param n2     GeoVector
     * @param normal double[]
     * @return false if the plane defined by the three GeoVectors contains the
     *         origin, in which case the normal is of zero length. True otherwise.
     */
    public boolean getNormal(GeoVector n1, GeoVector n2, double[] normal) {
	double[][] temp = new double[3][3];
	temp[0][0] = v[0] * radius;
	temp[0][1] = v[1] * radius;
	temp[0][2] = v[2] * radius;

	temp[1][0] = n1.v[0] * n1.radius - temp[0][0];
	temp[1][1] = n1.v[1] * n1.radius - temp[0][1];
	temp[1][2] = n1.v[2] * n1.radius - temp[0][2];

	temp[2][0] = n2.v[0] * n2.radius - temp[0][0];
	temp[2][1] = n2.v[1] * n2.radius - temp[0][1];
	temp[2][2] = n2.v[2] * n2.radius - temp[0][2];

	normal[0] = temp[1][1] * temp[2][2] - temp[1][2] * temp[2][1];
	normal[1] = temp[1][2] * temp[2][0] - temp[1][0] * temp[2][2];
	normal[2] = temp[1][0] * temp[2][1] - temp[1][1] * temp[2][0];

	return VectorUnit.normalize(normal) > 0.;
    }

    /**
     * Find the unit vector which is normal to the plane defined by the tips of
     * three GeoVectors.
     * 
     * @param n GeoVector[] array of 3 GeoVectors
     * @return double[] unit vector normal to plane defined by tips of GeoVectors.
     */
    static public double[] getNormal(GeoVector[] n) {
	double[][] temp = new double[3][3];

	n[0].getVector(temp[0]);

	// set temp1 = v1 - v0
	temp[1][0] = n[1].v[0] * n[1].radius - temp[0][0];
	temp[1][1] = n[1].v[1] * n[1].radius - temp[0][1];
	temp[1][2] = n[1].v[2] * n[1].radius - temp[0][2];

	// set temp2 = v2 - v0
	temp[2][0] = n[2].v[0] * n[2].radius - temp[0][0];
	temp[2][1] = n[2].v[1] * n[2].radius - temp[0][1];
	temp[2][2] = n[2].v[2] * n[2].radius - temp[0][2];

	// set temp0 = temp1 cross temp2
	temp[0][0] = -(temp[1][1] * temp[2][2] - temp[1][2] * temp[2][1]);
	temp[0][1] = -(temp[1][2] * temp[2][0] - temp[1][0] * temp[2][2]);
	temp[0][2] = -(temp[1][0] * temp[2][1] - temp[1][1] * temp[2][0]);

	// normalize temp0 and return it.
	VectorUnit.normalize(temp[0]);
	return temp[0];
    }

    /**
     * Find the unit vector which is normal to the plane defined by the tips of
     * three GeoVectors.
     * 
     * @param n0 GeoVector[] array of 3 GeoVectors
     * @param n1 GeoVector
     * @param n2 GeoVector
     * @return double[] unit vector normal to plane defined by tips of GeoVectors.
     */
    static public double[] getNormal(GeoVector n0, GeoVector n1, GeoVector n2) {
	double[][] temp = new double[3][3];

	n0.getVector(temp[0]);

	// set temp1 = v1 - v0
	temp[1][0] = n1.v[0] * n1.radius - temp[0][0];
	temp[1][1] = n1.v[1] * n1.radius - temp[0][1];
	temp[1][2] = n1.v[2] * n1.radius - temp[0][2];

	// set temp2 = v2 - v0
	temp[2][0] = n2.v[0] * n2.radius - temp[0][0];
	temp[2][1] = n2.v[1] * n2.radius - temp[0][1];
	temp[2][2] = n2.v[2] * n2.radius - temp[0][2];

	// set temp0 = temp1 cross temp2
	temp[0][0] = -(temp[1][1] * temp[2][2] - temp[1][2] * temp[2][1]);
	temp[0][1] = -(temp[1][2] * temp[2][0] - temp[1][0] * temp[2][2]);
	temp[0][2] = -(temp[1][0] * temp[2][1] - temp[1][1] * temp[2][0]);

	// normalize temp0 and return it.
	VectorUnit.normalize(temp[0]);
	return temp[0];
    }

    /**
     * Retrieve a GeoVector that is a specified distance away from this GeoVector,
     * in a specified direction. Radius is unchanged.
     * 
     * @param azimuth  the azimuth from this GeoVector to the desired GeoVector, in
     *                 radians, measured clockwise from north.
     * @param distance the distance from this GeoVector to the desired GeoVector, in
     *                 radians.
     * @param loc      the GeoVector that has been moved relative to this GeoVector.
     * @return boolean false if this GeoVector is located at north or south pole. In
     *         this case, azimuth is ill defined direction and a deep copy of this
     *         GeoVector is returned. True otherwise.
     */
    public boolean move(double azimuth, double distance, GeoVector loc) {
	return VectorUnit.move(this.v, distance, azimuth, loc.v);
    }

    /**
     * Retrieve a new GeoVector with the same EarthShape as this GeoVector, that is
     * located azimuth and distance away from this GeoVector. Radius is unchanged.
     * 
     * @param azimuth  double in radians
     * @param distance double in radians
     * @return GeoVector
     */
    public GeoVector move(double azimuth, double distance) {
	GeoVector geoVector = new GeoVector();
	VectorUnit.move(this.v, distance, azimuth, geoVector.v);
	return geoVector;
    }

    /**
     * Retrieve a new GeoVector with the same EarthShape as this GeoVector, that is
     * located azimuth and distance away from this GeoVector. Radius is unchanged.
     * 
     * @param azimuth  double in degrees
     * @param distance double in degrees
     * @return GeoVector
     */
    public GeoVector moveDegrees(double azimuth, double distance) {
	GeoVector geoVector = new GeoVector();
	VectorUnit.move(this.v, toRadians(distance), toRadians(azimuth), geoVector.v);
	return geoVector;
    }

    /**
     * Retrieve a new GeoVector with the same EarthShape as this GeoVector and which
     * is displaced from this GeoVector in the following manner. First, move
     * distance y in direction toward vtp. Then rotate the resulting vector angle x
     * around pole vtp.
     * 
     * @param vtp GeoVector
     * @param y   double
     * @param x   double
     * @return GeoVector
     */
    public GeoVector move(double[] vtp, double x, double y) {
	GeoVector g = new GeoVector();
	Vector3D.move(this.v, vtp, x, y, g.v);
	return g;
    }

    /**
     * Generate a regular grid of GeoVectors centered on the position of this
     * GeoVector. The shape of the grid depends on the position of pole relative to
     * this GeoVector. If pole is the north pole, then the grid will be a regular,
     * lat-lon grid. If the pole is a point 90 degrees from this GeoVector, then the
     * grid will be as close as possible to evenly spaced in the sense that grid
     * cells will be as nearly equal area as possible. x refers to east-west
     * direction, y refers to north-south direction.
     * 
     * @param pole GeoVector
     * @param nx   int
     * @param dx   double width of grid cells in radians
     * @param ny   int
     * @param dy   double height of grid cells in radians
     * @return GeoVector[ny][nx]
     */
    public GeoVector[][] getGrid(GeoVector pole, int nx, double dx, int ny, double dy) {
	GeoVector[][] grid = new GeoVector[ny][nx];
	double x = -0.5 * (nx - 1.) * dx;
	double y = -0.5 * (ny - 1.) * dy;
	for (int i = 0; i < nx; ++i)
	    for (int j = 0; j < ny; ++j)
		grid[j][i] = move(pole.v, x + i * dx, y + j * dy);
	return grid;
    }

    /**
     * Generate a regular, lat-lon grid of GeoVectors centered on the position of
     * this GeoVector. x refers to east-west direction, y refers to north-south
     * direction.
     * 
     * @param nx int
     * @param dx double width of grid cells in radians
     * @param ny int
     * @param dy double height of grid cells in radians
     * @return GeoVector[ny][nx]
     */
    public GeoVector[][] getGrid(int nx, double dx, int ny, double dy) {
	GeoVector northPole = new GeoVector(new double[] { 0., 0., 1. }, 1.);
	return getGrid(northPole, nx, dx, ny, dy);
    }

    /**
     * Retrieve a new GeoVector with the same EarthShape as this GeoVector and which
     * is displaced from this GeoVector in the following manner. First, move
     * distance y in direction toward GeoVector vtp. Then rotate the resulting
     * vector angle x around pole vtp.
     * 
     * @param vtp GeoVector
     * @param y   double
     * @param x   double
     * @return GeoVector
     */
    public GeoVector move(GeoVector vtp, double y, double x) {
	return move(vtp.v, y, x);
    }

    /**
     * Retrieve a GeoVector which is normal to the plane containing the great circle
     * path from this GeoVector to another GeoVector. Considering the GeoVectors to
     * be unit vectors, loc will be set to this cross x. The radius of the returned
     * GeoVector is not modified by this method.
     * 
     * @param x the other GeoVector which, together with this GeoVector, define the
     *          great circle path.
     */
    public GeoVector cross(GeoVector x) {
	GeoVector loc = new GeoVector();
	VectorUnit.crossNormal(v, x.v, loc.v);
	loc.setRadius(this.radius);
	return loc;
    }

    /**
     * Retrieve a GeoVector which is normal to the plane containing the great circle
     * path from this GeoVector to another GeoVector. Considering the GeoVectors to
     * be unit vectors, loc will be set to this cross x. The radius of the returned
     * GeoVector is not modified by this method.
     * 
     * @param x   the other GeoVector which, together with this GeoVector, define
     *            the great circle path.
     * @param loc the GeoVector normal to the plane containing the great circle
     *            path. Invalid if this and loc are parallel.
     * @return true if loc is valid, false if this and x are parallel.
     */
    public boolean cross(GeoVector x, GeoVector loc) {
	return VectorUnit.crossNormal(v, x.v, loc.v) > 0;
    }

    /**
     * Rotate this GeoVector around GeoVector pole by angle a. When looking in
     * direction of pole's unit vector, clockwise rotation is positive. The radius
     * of the returned GeoVector is not modified by this method.
     * 
     * @param pole  the pole around which this GeoVector is to be rotated.
     * @param angle the angular distance by which this GeoVector is to be rotated
     *              around pole, in radians.
     * @param loc   the GeoVector that results from rotating this GeoVector around
     *              pole.
     */
    public void rotate(GeoVector pole, double angle, GeoVector loc) {
	VectorUnit.rotate(v, pole.v, angle, loc.v);
    }

    /**
     * Retrieve a new GeoVector obtained by rotating this GeoVector around pole by
     * angular distance angle. Rotation is in radians positive clockwise when
     * looking in the direction of pole.
     * 
     * @param pole
     * @param angle
     * @return
     */
    public GeoVector rotate(GeoVector pole, double angle) {
	GeoVector loc = new GeoVector();
	VectorUnit.rotate(v, pole.v, angle, loc.v);
	return loc;
    }

    /**
     * 
     * @param pole
     * @param angle
     * @return
     */
    public void rotateThis(GeoVector pole, double angle) {
	double[] u = new double[3];
	VectorUnit.rotate(v, pole.v, angle, u);
	v[0] = u[0];
	v[1] = u[1];
	v[2] = u[2];
    }

    /**
     * Apply an Euler rotation matrix to this GeoVector. This GeoVector is modified
     * by this call.
     * 
     * @param eulerRotationMatrix
     */
    public void rotateThis(double[] eulerRotationMatrix) {
	VectorUnit.eulerRotation(v, eulerRotationMatrix, v);
    }

    /**
     * Apply an Euler rotation matrix to this GeoVector and return the rotated
     * GeoVector in loc. Radius of loc is set equal to radius of this. this
     * GeoVector is not modified.
     * 
     * @param eulerRotationMatrix
     * @param loc
     */
    public void rotate(double[] eulerRotationMatrix, GeoVector loc) {
	VectorUnit.eulerRotation(v, eulerRotationMatrix, loc.v);
	loc.setRadius(radius);
    }

    /**
     * Apply an Euler rotation matrix to this GeoVector and return the rotated
     * GeoVector in a new GeoVector. this GeoVector is not modified. Radius of loc
     * is set equal to radius of this.
     * 
     * @param eulerRotationMatrix
     * @param loc
     */
    public GeoVector rotate(double[] eulerRotationMatrix) {
	GeoVector loc = new GeoVector();
	VectorUnit.eulerRotation(v, eulerRotationMatrix, loc.v);
	loc.setRadius(radius);
	return loc;
    }

    /**
     * Compute the vector triple product (this x other) x this, normalized to unit
     * length.
     * 
     * @param other the other GeoVector with which this GeoVector is to be crossed.
     * @param vtp   the normalized vector triple product.
     * @return true if valid, false if triple product has zero length, which will
     *         happen when this and other are coincident or PI radians apart.
     */
    public boolean vectorTripleProduct(GeoVector other, double vtp[]) {
	return VectorUnit.vectorTripleProduct(v, other.v, vtp);
    }

    /**
     * Retrieve a 3 component unit vector that is normal to this GeoVector's unit
     * vector, and lies in the plane defined by this GeoVector and other GeoVector.
     * 
     * @param other GeoVector
     * @return double[] normalized vector triple product of this and other. All 3
     *         elements will be zero if this and other are coincident or PI radians
     *         apart.
     */
    public double[] vectorTripleProduct(GeoVector other) {
	double[] vtp = new double[3];
	VectorUnit.vectorTripleProduct(v, other.v, vtp);
	return vtp;
    }

    /**
     * Move this GeoVector object a specified angular distance (radians) in the
     * direction specified by vtp, which is assumed to be a unit vector normal to
     * this GeoVector object's unit vector. vtp values are typically obtained by
     * calling vectorTripleProduct(). The radius of the returned GeoVector is not
     * modified by this method.
     * 
     * @param vtp a 3 component unit vector normal to this GeoVector.
     * @param a   the angular distance from this GeoVector to the desired GeoVector,
     *            in radians.
     * @param loc the GeoVector object which has been relocated as requested.
     */
    public void move(double vtp[], double a, GeoVector loc) {
	VectorUnit.move(v, vtp, a, loc.v);
    }

    /**
     * Retrieve the dot product of this unit vector with other unit vector.
     * 
     * @param other GeoVector
     * @return double
     */
    public double dot(GeoVector other) {
	return VectorUnit.dot(this.v, other.v);
    }

    /**
     * Retrieve the dot product of this unit vector with other vector.
     * 
     * @param other GeoVector
     * @return double
     */
    public double dot(double[] other) {
	return VectorUnit.dot(this.v, other);
    }

    /**
     * Move this GeoVector a specified angular distance toward the north pole. If
     * the colatitude of this is less than distance, then this will move past the
     * north pole and it's longitude will change by 180 degrees. If this is already
     * at the north or south pole, then it is returned unmodified.
     * 
     * @param distance the distance, in radians, that this is to be moved toward the
     *                 north.
     * @return true if operation successful, false if this is already at north or
     *         south pole.
     */
    public boolean move_north(double distance) {
	return VectorUnit.moveNorth(v, distance, v);
    }

    /**
     * Return a unit vector that is distance radians due north of this. If this is
     * already at the north or south pole, then a copy of this is returned. This
     * geoVector is not modified by this operation.
     * 
     * @param distance the distance, in radians, that x is to be moved toward the
     *                 north.
     * @param z        the 3-element unit vector representing the position after
     *                 having moved distance north.
     * @return true if operation successful, false if this is already at north or
     *         south pole.
     */
    public boolean move_north(double distance, double z[]) {
	return VectorUnit.moveNorth(v, distance, z);
    }

    /**
     * Retrieve a new GeoVector object that has the same EarthShape as this
     * GeoVector but which is located angular distance 'distance' due north of this
     * this GeoVector. New position will move past the pole if necessary.
     * 
     * @param distance double in radians
     * @return GeoVector
     */
    public GeoVector moveNorth(double distance) {
	GeoVector x = new GeoVector();
	VectorUnit.moveNorth(this.v, distance, x.v);
	return x;
    }

    /**
     * Return the scalar triple product of u, w, and this GeoVector's unit vector.
     * In other words (u cross w) dot v.
     * 
     * @param u double[]
     * @param w double[]
     * @return double scalar triple product (u cross w) dot v.
     */
    public double scalarTripleProduct(GeoVector u, GeoVector w) {
	// return scalarTripleProduct(u.v, w.v, v);
	return u.v[0] * w.v[1] * v[2] + w.v[0] * v[1] * u.v[2] + v[0] * u.v[1] * w.v[2] - v[0] * w.v[1] * u.v[2]
		- u.v[0] * v[1] * w.v[2] - w.v[0] * u.v[1] * v[2];
    }

    /**
     * Return the scalar triple product of u, w, and this GeoVector's unit vector.
     * In other words (u cross w) dot v. If u and w are unit vectors which lie in
     * the plane normal to this GeoVector, then the result is cos(phi) where phi is
     * the angle between this GeoVector and u cross w.
     * 
     * @param u double[]
     * @param w double[]
     * @return double scalar triple product (u cross w) dot v.
     */
    public double scalarTripleProduct(double u[], double w[]) {
	return VectorUnit.scalarTripleProduct(u, w, v);
    }

    public double getSeaz(GeoVector event) {
	return (azimuthDegrees(event, Double.NaN)+360.) % 360.;
    }

    public double getEsaz(GeoVector receiver) {
	return (azimuthDegrees(receiver, Double.NaN)+360.) % 360.;
    }

    public EarthShape getEarthShape() { return earthShape; }
    
    public void write(FileOutputBuffer fob) throws IOException {
	    fob.writeDouble(v[0]);
	    fob.writeDouble(v[1]);
	    fob.writeDouble(v[2]);
	    fob.writeDouble(radius);
    }
}
