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
package gov.sandia.gnem.dbutillib.util;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toDegrees;

/**
 * <p>Title: Location</p>
 *
 * <p>Description: </p>
 * The Location Class manages a single point in/on the Earth.  Both spherical and
 * ellipsoidal earth can be supported, depending on the values assigned to
 * static constant values EARTH_A and EARTH_F.
 *
 * <p>Points in the earth are represented internally by a geocentric,
 * 3 component unit vector that has its origin at the center of the earth. The
 * x-component points to lon,lat = 0,0.  y-component points to lon,lat =
 * PI/2,0 and the z-component points to lon,lat = 0,PI/2.  The radius of point
 * (the distance in km from the center of the earth to the point) is also stored.
 *
 * <p>There are constructors that take geographic latitudes and longitudes as parameters
 * and convert them to geocentric unit vectors.  There are many methods for
 * computing the distance between 2 Location objects, the azimuth from one Location
 * to another, the Location that is normal to the plane containing the great circle
 * path from one Location to another, the radius of the earth at a given location, etc.
 * There is a method for rotating a Location some angular distance around another
 * Location.
 *
 * <p>If the value of EARTH_F is > 0 then geographic latitudes are converted to geocentric
 * latitudes before computing geocentric unit vectors (and vice versa).
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Sandia National Laboratories</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public class Location implements Cloneable {

    /**
     * the radius, in km, of a sphere with same surface area as the earth.
     * reference: http://www.gfy.ku.dk/~iag/HB2000/part4/grs80_corr.htm
     */
    public static final double AUTHALIC_RADIUS = 6371.0071810;

    // Spherical earth.
//    private static final double EARTH_A   = AUTHALIC_RADIUS;
//    private static final double EARTH_F   = 0.0; // flattening parameter

    // GRS80 Ellipsoid
    // reference: http://www.gfy.ku.dk/~iag/HB2000/part4/grs80_corr.htm
    public static final double EARTH_A = 6378.137; // equatorial radius in km
    public static final double EARTH_F = 1 / 298.257222101; // flattening parameter

    // Clarke 1866 Ellipsoid
    // Reference: Snyder, J. P., 1987, "Map Projections - A Working Manual",
    // USGS Professional Paper 1395.
    //private static final double EARTH_A   = 6378.2064;  // equatorial radius in km
    //private static final double EARTH_F   = 1 / 294.98; // flattening parameter

    /**
     * The position of the point relative to the center of the earth can be
     * expressed in one of two ways, either as the depth in km below the surface
     * of the earth, or as radius in km from the center of the earth.  Regardless
     * of how it is stored, getDetph() and getRadius() return the correct values.
     * The significance is what happens when move() or rotate() are called.  The
     * Location object that is returned will have the same z value as the starting
     * Location.  Whether that is a radius or a depth depends on how Z_IS_DEPTH
     * is set.
     */
    public static final boolean Z_IS_DEPTH = true;
    public static final boolean Z_IS_RADIUS = !Z_IS_DEPTH;

    public static final double EARTH_E = 2 * EARTH_F - EARTH_F * EARTH_F;

    /**
     * 3-component unit vector representing the north pole.  Used often enough
     * in the code that it is worth having a permanent copy.
     */
    private static final double[] north_pole = new double[]{0., 0., 1.};

    /**
     * 3-component unit vector representation of a point in the earth.
     */
    private double[] x = new double[3];

    /**
     * Either the depth or the radius of the location in the earth, depending
     * on whether Z_IS_DEPTH is true of false.  In km.
     */
    private double z;

    /**
     * Default constructor.  Location is initialized to lat,lon,depth = 0., 0., 0.
     */
    public Location() {
        x[0] = 1.;
        x[1] = 0.;
        x[2] = 0.;
        if (Z_IS_DEPTH) z = 0.;
        else z = EARTH_A;
    }

    /**
     * Constructor that takes geographic latitude and longitude.  Depth is
     * assumed to be zero.
     *
     * @param latitude  in radians
     * @param longitude in radians
     */
    public Location(double latitude, double longitude)
//            throws LocationException
    {
        this(latitude, longitude, 0.);
    }

    /**
     * Constructor that takes geographic latitude and longitude in radians,
     * and depth in km.
     *
     * @param latitude  double
     * @param longitude double
     * @param depth     double
     */
    public Location(double latitude, double longitude, double depth)
//            throws LocationException
    {
//        if (latitude < -PI / 2 || latitude > PI / 2)
//            throw new LocationException(
//                    "ERROR in Location constructor.  Latitude "
//                    + String.format("%1.6f", latitude)
//                    + " is out of range -PI/2 to PI/2");
//        if (longitude < -PI || longitude > 2*PI)
//            throw new LocationException(
//                    "ERROR in Location constructor.  Longitude "
//                    + String.format("%1.6f", longitude)
//                    + " is out of range -PI to 2*PI");
        // first convert geographic latitude to geocentric latitude using the GRS80 ellipsoid.
        double lat;
        if (EARTH_F == 0)
            lat = latitude;
        else
            lat = atan((1 - EARTH_E) * tan(latitude));
        x[0] = cos(lat) * cos(longitude);
        x[1] = cos(lat) * sin(longitude);
        x[2] = sin(lat);
        if (Z_IS_DEPTH)
            z = depth;
        else
            z = getEarthRadius() - depth;
    }

    /**
     * Constructor that takes a unit vector and a z.
     *
     * @param x double[] unit vector.
     * @param z double z.
     */
    public Location(double[] x, double z)
            throws LocationException {
        // if this method is made public, uncomment the following:
        if (x.length != 3)
            throw new LocationException(
                    "ERROR in Location(double[] x, double z) constructor.  Input vector x does not have 3 elements.");
        double len = sqrt(dot(x, x));
        if (abs(1. - len) > 1e-12)
            throw new LocationException(
                    "ERROR in Location constructor.  Input vector x is not a unit vector.  Length="
                            + String.format("%1.16e", len));
        this.x[0] = x[0];
        this.x[1] = x[1];
        this.x[2] = x[2];
        this.z = z;
    }

    /**
     * Return a deep copy of this Location.
     *
     * @return Location a deep copy of this Location.
     */
    @Override
    public Location clone() {
        Location loc = new Location();
        loc.x[0] = x[0];
        loc.x[1] = x[1];
        loc.x[2] = x[2];
        loc.z = z;
        return loc;
    }

    /**
     * Retrieve a String representation of this Location.  Format is
     * latitude in degrees, longitude in degrees, depth in km.
     *
     * @return String 33 character string representation of lat, lon, depth
     */
    @Override
    public String toString() {
        return String.format("%10.6f %11.6f %10.4f",
                toDegrees(getLat()), toDegrees(getLon()), getDepth());
    }

    /**
     * Retrieve a String representation of the distance and azimuth from
     * this Location to some other Location.  Both in degrees.
     *
     * @param other Location  the other Location to which the distance and
     *              azimuth are to be computed.
     * @return String 21 character string.
     */
    public String distAzToString(Location other) {
        double az, dist = distance(other);
        try {
            az = azimuth(other);
        } catch (Exception ex) {
            // azimuth() will fail if dist = 0 or dist = PI or if this
            // Location is on one of the poles.
            az = 0.;
        }
        return String.format("%10.6f %10.6f", toDegrees(dist), toDegrees(az));
    }

    /**
     * Retrieve the geographic latitude of this Location, in radians.
     *
     * @return double geographic latitude in radians.
     */
    public double getLat() {
        if (EARTH_F == 0)
            return asin(x[2]);
        return atan(tan(asin(x[2])) / (1 - EARTH_E));
    }

    public double getLatDegrees() {
        return toDegrees(getLat());
    }

    /**
     * Retrieve the geocentric latitude of this Location, in radians.
     *
     * @return double geocentric latitude of this Location, in radians.
     */
    public double getGeocentricLat() {
        return asin(x[2]);
    }

    /**
     * Retrieve the longitude of this Location, in radians
     *
     * @return double longitude in radians.
     */
    public double getLon() {
        return atan2(x[1], x[0]);
    }

    public double getLonDegrees() {
        return toDegrees(getLon());
    }

    /**
     * Retreive a copy of the unit vector.
     *
     * @param v double[] the 3-element array into which the unit vector will
     *          be inserted.
     * @throws LocationException if supplied array has length < 3.
     */
    public void getVector(double[] v)
            throws LocationException {
        if (v.length < 3)
            throw new LocationException(
                    "ERROR in Location.getVector(double[] v).  v.length < 3.");
        for (int i = 0; i < 3; i++) v[i] = x[i];
    }

    /**
     * Retrieve the radius of this Location, in km.  This is the distance
     * from the center of the earth to this Location.
     *
     * @return double radius of this Location in km.
     */
    public double getRadius() {
        if (Z_IS_DEPTH)
            return getEarthRadius() - this.z;
        else return z;
    }

    /**
     * Retrieve the depth of this Location below the surface of the earth.
     *
     * @return double depth of this location in km.
     */
    public double getDepth() {
        if (Z_IS_DEPTH)
            return z;
        else
            return getEarthRadius() - z;
    }

    /**
     * Set the depth of this Location
     *
     * @param depth double  the depth of this Location.
     */
    public void setDepth(double depth) {
        if (Z_IS_DEPTH)
            this.z = depth;
        else
            this.z = getEarthRadius() - depth;
    }

    /**
     * Retrieve the radius of the earth at the latitude of this Location, in km.
     * For elliptical earth (EARTH_F > 0), the radius returned will be a function
     * of latitude.
     *
     * @return double radius of the earth at the latitude of this Location, in km.
     */
    public double getEarthRadius() {
        if (EARTH_F == 0)
            return EARTH_A;
        return EARTH_A * sqrt(1 - x[2] * x[2] * EARTH_E);
    }

    /**
     * Retrieve the angular distance from this Location to some other Location.
     *
     * @param other Location the other Location to which the distance is to be computed.
     * @return double the angular distance in radians from this Location to other Location.
     */
    public double distance(Location other) {
        return angle(this.x, other.x);
    }

    /**
     * Retrieve the angular distance from this Location to some other Location.
     *
     * @param other Location the other Location to which the distance is to be computed.
     * @return double the angular distance in degrees from this Location to other Location.
     */
    public double distanceDegrees(Location other) {
        return toDegrees(distance(other));
    }

    /* *
     * Returns the distance, in km, from this Location to some other Location,
     * measured along the surface of the earth. If EARTH_F == 0., i.e., the
     * earth is a sphere, then returns angular distance * EARTH_A.
     * Otherwise, the distance from this to other is divided into approximately
     * 1 degree intervals and angular distance * local earthRadius() is summed
     * along the great circle path from this to other.
     * @param other Location the other location to which this Location is to
     * be compared.
     * @return double separation of the locations in km.
     */
    public double distanceKm(Location other)
    //throws LocationException
    {
        double distance = angle(x, other.x);
        if (distance < 1e-12) return 0.;

        // for spherical earth, return R * theta.
        if (EARTH_F == 0.)
            return distance * EARTH_A;

        // find the Location that is other cross this.
        double[] p = new double[3];
        if (!cross(other.x, this.x, p)) {
            // separation is PI.
            // choose great circle integration path over the north pole.
            if (!cross(north_pole, this.x, p)) {
                // path goes from north pole to south pole or vice versa.
                // Choose arbitrary great circle path.
                p[0] = 1.;
                p[1] = 0.;
                p[2] = 0.;
            }
        }

        // make a Location object out of p.  This is the pole or rotation that
        // will move this Location to other Location.
        Location pole = null;
        try {
            pole = new Location(p, z);
        } catch (Exception ex1) {
            // can't fail (famous last words).
        }

        // n = aproximate number of 1 degree increments in distance, minimum of 1.
        // (should do Romberg integration here.)
        double dx = PI / 180.;
        long n = 1 + (long) (distance / dx);
        dx = distance / n;

        distance = 0.;
        Location loc = new Location();
        try {
            for (int i = 0; i < n; i++) {
                rotate(pole, dx * (i + 0.5), loc);
                distance += dx * loc.getEarthRadius();
            }
        } catch (LocationException ex) {
            // code should ensure that this never happens!
            distance = -999999999.;

            String msg =
                    "ERROR in Location.distanceKm().  Cannot rotate this around pole."
                            + "\nthis = " + toString()
                            + "\npole = " + pole.toString()
                            + "\nreturning distanceKm = -999999999.";
            System.out.println(msg);
//            throw new LocationException(msg);
        }
        return distance;
    }

    /**
     * Azimuthal direction from this Location to some other Location, in
     * radians clockwise from north.
     *
     * @param other Location the Location to which the bearing is desired.
     * @return double azimuth from this Location to other Location, in radians.
     * @throws LocationException of the two location are coincident, or
     *                           PI radians apart.
     */
    public double azimuth(Location other)
            throws LocationException {
        double azim;
        // find their cross product.
        double[] c2 = new double[3];
        if (!cross(x, other.x, c2))
            throw new LocationException(
                    "ERROR in Location.azimuth().  Cross product of two Locations is zero.");
        // find the cross product of this with north_pole.
        double[] c = new double[3];
        if (!cross(this.x, north_pole, c))
            throw new LocationException(
                    "ERROR in Location.azimuth().  Starting Location is either north or south pole.");
        azim = angle(c, c2);
        if (dot(c2, north_pole) < 0.)
            azim = 2 * PI - azim;

        return azim;
    }

    /**
     * Move this location some distance along a great circle path specified by an
     * azimuthal direction.  If many steps are to be taken in the same direction,
     * it is more efficient to find the pole normal to the great circle path that
     * contains the direction of motion (using cross()) and then repeatedly rotate
     * this Location around the pole using rotate().
     *
     * @param azimuth  double the azimuthal direction in which the position is to
     *                 move, in radians clockwise from north.
     * @param distance double the distance, in radians, that the position is to be
     *                 moved.
     * @param loc      Location the new Location that results from the move.
     * @throws LocationException if this Location is either the north or south pole.
     */
    public void move(double azimuth, double distance, Location loc)
            throws LocationException {
        loc.z = this.z;
        if (azimuth == 0.) {
            if (!move_north(this.x, distance, loc.x))
                throw new LocationException(
                        "ERROR in Location move().  Starting Location is either north or south pole.");
            return;
        }

        double[] work = new double[3];
        if (!move_north(this.x, distance, work))
            throw new LocationException(
                    "ERROR in Location move().  Starting Location is either north or south pole.");
        if (!rotate(work, this.x, -azimuth, loc.x))
            throw new LocationException(
                    "ERROR in Location move().  Cannot rotate location around this.");
    }

    /**
     * Rotate this Location around pole by angular distance.
     *
     * @param pole  Location the pole about which rotation is to occur.
     * @param angle double amount of rotation in radians.  Positive
     *              rotation is clockwise around pole when looking down on pole.
     * @param loc   Location the Location that results from rotating this
     *              Location around pole.
     * @throws LocationException if this Location and pole are coincident
     *                           or PI radians apart.
     */
    public void rotate(Location pole, double angle, Location loc)
            throws LocationException {
        loc.z = this.z;
        if (!rotate(this.x, pole.x, angle, loc.x))
            throw new LocationException(
                    "ERROR in Location.rotate().  Cannot rotate this around pole.");
    }

    /**
     * Find the Location that corresponds to the cross product of this Location
     * with some other Location.  Result is returned in loc2.
     *
     * @param loc1 Location the other Location that this Location is to be crossed with.
     * @param loc2 Location the result of crossing this Location with other Location.
     * @throws LocationException if this Location and other Location are coincident or
     *                           PI radians apart.
     */
    public void cross(Location loc1, Location loc2)
            throws LocationException {
        if (!cross(this.x, loc1.x, loc2.x))
            throw new LocationException(
                    "ERROR in Location.cross().  Cross product haz zero length.");
    }

    /**
     * Return a location on the earth that is some specified distance due north of
     * positon u.  If distance is greater than separation of u and north pole,
     * rotation is continued past the pole.
     * If u is north or south pole, method returns false and w is set to all zeros.
     * u is assumed to be of unit length on input.
     *
     * @param u        double[] original 3-element unit vector that is to be moved north.
     * @param distance double distance that u is to be moved, in radians.
     * @param w        double[] the result of moving u in northerly direction.
     * @return boolean
     */
    private boolean move_north(double[] u, double distance, double[] w) {
        double[] pole = new double[3];
        if (cross(north_pole, u, pole))
            return rotate(x, pole, distance, w);
        w[0] = 0.;
        w[1] = 0.;
        w[2] = 0.;
        return false;
    }

    /**
     * Rotate u clockwise around p, by angle a and return result in w.
     * Rotation is clockwise around p when looking down vector p (from
     * tip toward tail).  Input vectors are assumed to be of unit length
     * on input.  Rotated vector is normalized to unit length.
     * If u and p are coincident or PI radians apart, w will be zero length
     * and rotate() will return false.
     *
     * @param u double[] 3 component unit vector to be rotated.
     * @param p double[] pole about which rotation is to occur.
     * @param a double amount of rotation in radians.  Positive rotation
     *          is clockwise around p when looking in -p direction(looking down on
     *          surface of the earth).
     * @param w double[] result of rotation.  3-element vector normalized
     *          to unit length.  If u and p are coincident or PI radians apart, then
     *          all 3 elements of w will be zero.
     * @return boolean true if rotation successful, false if u and p are
     * coincident or PI radians apart.
     */
    private boolean rotate(double[] u, double[] p, double a, double[] w) {
        double cosa = cos(-a);
        double sina = sin(-a);
        double d = (u[0] * p[0] + u[1] * p[1] + u[2] * p[2]) * (1 - cosa);
        w[0] = cosa * u[0] + d * p[0] + sina * (p[1] * u[2] - p[2] * u[1]);
        w[1] = cosa * u[1] + d * p[1] + sina * (p[2] * u[0] - p[0] * u[2]);
        w[2] = cosa * u[2] + d * p[2] + sina * (p[0] * u[1] - p[1] * u[0]);
        d = max(0., w[0] * w[0] + w[1] * w[1] + w[2] * w[2]);
        if (d > 1e-12)
            d = 1.0 / sqrt(d);
        else
            d = 0.;
        w[0] *= d;
        w[1] *= d;
        w[2] *= d;
        return d > 1e-12;
    }

    /**
     * Angular distance between two 3-component unit vectors, in radians.
     * Vectors are assumed to be unit length on input.
     *
     * @param u double[] unit vector one.
     * @param v double[] unit vector two.
     * @return double angular separation in radians.
     */
    private double angle(double[] u, double[] v) {
        return acos(max(min(u[0] * v[0] + u[1] * v[1] + u[2] * v[2], 1.0), -1.0));
    }

    /**
     * Dot product of two 3-component unit vectors.
     *
     * @param u double[] unit vector one.
     * @param w double[] unit vector two.
     * @return double dot product
     */
    private double dot(double[] u, double[] w) {
        return u[0] * w[0] + u[1] * w[1] + u[2] * w[2];
    }

    /**
     * Cross product of two 3-component unit vectors, normalized to
     * unit length.  Vectors are assumed to be unit length on input.
     * If the input vectors are coincident or PI radians apart, the
     * cross product is zero length and method returns false.
     *
     * @param u double[3] unit vector one.
     * @param v double[3] unit vector two.
     * @param w double[3] cross product normalized to unit length.
     * @return boolean true if cross product is valid, false if
     * length of cross product is zero.
     */
    private boolean cross(double[] u, double[] v, double[] w) {
        w[0] = u[1] * v[2] - u[2] * v[1];
        w[1] = u[2] * v[0] - u[0] * v[2];
        w[2] = u[0] * v[1] - u[1] * v[0];
        double l = max(w[0] * w[0] + w[1] * w[1] + w[2] * w[2], 0.);
        if (l > 1e-12)
            l = 1.0 / sqrt(l);
        else
            l = 0.;
        w[0] *= l;
        w[1] *= l;
        w[2] *= l;
        return l > 1e-12;
    }

    /**
     * This class just extends the Exception interface to create a customized
     * exception that Location can throw when it encounters fatal errors.
     */
    @SuppressWarnings("serial")
    public static class LocationException extends java.lang.Exception {
        /**
         * Creates a new instance of <code>LocationException</code> without
         * detail message.
         */
        public LocationException() {
        }

        /**
         * Constructs an instance of <code>LocationException</code> with
         * the specified detail message.
         *
         * @param msg the detail message.
         */
        public LocationException(String msg) {
            super(msg);
        }
    }

}
