package gov.sandia.gnem.slbmjni;

import static java.lang.Math.*;

public class Point
{
    public double[] u;

    /**
     * Point constructor
     * @param lat in degrees or radians
     * @param lon in degrees or radians
     * @param inDegrees if true, then lat,lon are assumed to be in degrees
     * otherwise radians
     */
    public Point(double lat, double lon, boolean inDegrees)
    {
        u = inDegrees ? getVector(toRadians(lat), toRadians(lon)) : getVector(lat, lon);
    }

    public Point(GridProfile profile)
    {
        this(profile.lat, profile.lon, false);
    }

    @Override
    public boolean equals(Object other)
    {
        return dot(u, ((Point)other).u) > cos(1e-7);
    }

    @Override
    public int hashCode() { return (int)(u[2]*1e4); }

    public static double dot(double[] v0, double[] v1)
    { return v0[0] * v1[0] + v0[1] * v1[1] + v0[2] * v1[2]; }

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
    {
        double[] vector = new double[3];
        getVector(lat, lon, vector);
        return vector;
    }

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
    {
        lat = atan(0.9933056199770992 * tan(lat));

        // z component of v is sin of geocentric latitude.
        vector[2] = sin(lat);

        // set lat = to cos of geocentric latitude
        lat = cos(lat);

        // compute x and y components of v
        vector[0] = lat * cos(lon);
        vector[1] = lat * sin(lon);
    }

}

