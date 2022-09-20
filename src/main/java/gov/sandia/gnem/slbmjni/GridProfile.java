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
package gov.sandia.gnem.slbmjni;

import static java.lang.Math.*;

/**
 * Stores information related to a single node in the Earth model.  The information
 * is retrieved from a C++
 * <A href="../../../../../../../../SLBM/doc/html/class_grid_profile.html">GridProfile</A>
 * object via a call to {@link SlbmInterface#getGridData(int)}
 * and includes the node ID of the grid node, the geographic latitude and longitude
 * of the grid node, the depths of all model interfaces beneath location of the
 * grid node, the P and S velocities of each model interval, and the P and S velocity
 * gradients in the mantle.
 */
public class GridProfile
{
    /**
     * The node ID of this node in the grid.  
     */
    public int nodeId = -1;

    /**
     * The geographic latitude of the grid node, in radians.
     */
    public double lat;

    /**
     * The geographic longitude of the grid node, in radians.
     */
    public double lon;

    /**
     * The depth of each interface in the model, in km.
     */
    public double[] depth = new double[0];

    /**
     * A 2 x nIntervals array containing the P and S velocities
     * of each interval of the earth model encountered at this
     * horizontal position, in km/sec.  For the first
     * index, element 0 refers to P velocities and element
     * 1 refers to S velocities.
     */
    public double[][] velocity = new double[2][0];

    /**
     * A 2 element array containing the P and S mantle
     * velocity gradients in 1/sec.  Element 0 refers to
     * the P gradient and element 1 refers to the S gradient.
     */
    public double[] gradient = new double[0];

    private Point point;

    public final static String[] layers = new String[] {
        "WATER", "SEDIMENT1", "SEDIMENT2", "SEDIMENT3", "UPPER_CRUST", "MIDDLE_CRUST_N",
        "MIDDLE_CRUST_G", "LOWER_CRUST", "MANTLE"
    };

    public GridProfile()
    {
    }

    public String getLatLon()
    {
        return String.format("%8.4f %9.4f",
                             Math.toDegrees(lat), Math.toDegrees(lon));
    }

    /**
     * Retrieve the number of intervals in this GridProfile, including zero
     * thickness intervals.
     * This is equivalent to the number of layers in the earth model.
     * @return int number of intervals in this GridProfile.
     */
    public int getNIntervals()
    {
        return depth.length;
    }

    public Point getPoint()
    {
        if (point == null)
            point = new Point(this);
        return point;
    }

    /**
     * Retrieve a formatted String representation of the
     * information in this GridProfile object.
     * @return String a formatted String representation of the
     * information in this GridProfile object.
     */
    @Override
    public String toString()
    {
        StringBuffer s = new StringBuffer("GridProfile:\n\n");
        s.append(String.format("  Node ID  : %10d%n  Latitude : %10.4f%n  Longitude: %10.4f%n%n",
                               nodeId, Math.toDegrees(lat), Math.toDegrees(lon)));
        s.append("Layer               Depth        Thick      P Vel      S Vel\n");
        for (int i=0; i<getNIntervals()-1; i++)
            s.append(String.format("%-14s %12.6f %10.4f %10.4f %10.4f%n",
                    layers[i], depth[i], depth[i+1]-depth[i],
                    velocity[0][i], velocity[1][i]));

        int i=getNIntervals()-1;
        s.append(String.format("%-14s %12.6f %10s %10.4f %10.4f%n",
                layers[i], depth[i], " ",
                velocity[0][i], velocity[1][i]));

        s.append("               P Grad     S Grad\n");
        s.append(String.format("%10s %10.6f %10.6f%n%n",
                               "", gradient[0], gradient[1]));

        return s.toString();
    }

    @Override
    public boolean equals(Object other)
    {
        if ( !(other instanceof GridProfile))
            return false;

        GridProfile gp = (GridProfile) other;

        if (!gp.getPoint().equals(getPoint())) return false;

        if (abs(gp.gradient[0] - gradient[0]) > 1e-8 ||
                abs(gp.gradient[1] - gradient[1]) > 1e-8) return false;

        for (int i=0; i<depth.length; ++i)
            if (abs(gp.depth[i]-depth[i]) > 1e3
                    || abs(gp.velocity[0][i]-velocity[0][i]) > 1e-6
                            || abs(gp.velocity[1][i]-velocity[1][i]) > 1e-6 )
                return false;

        return true;
    }

}
