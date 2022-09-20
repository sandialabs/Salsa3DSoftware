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

/**
 * Stores information related to an interpolated profile through the Earth model.
 * The information is retrieved from a C++
 * <A href="../../../../../../../../SLBM/doc/html/class_query_profile.html">QueryProfile</A>
 * object via a call to {@link SlbmInterface#getInterpolatedPoint(double, double)}.
 * Information includes: the geographic latitude and
 * longitude of the point in the Earth model where the information was
 * interpolated, the depths of all model interfaces beneath the point
 * of interpolation, the P and S velocities of each model interval,
 * and the P and S velocity gradients in the mantle.  Also includes the
 * node IDs and interpolation coefficients used to interpolate the
 * information from the grid nodes.
 */
public class QueryProfile
{
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
     * A 2 x nLayers array containing the P and S velocities
     * of each layer in the model, in km/sec.  For the first
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

    /**
     * The IDs of the grid nodes that were used to interpolate
     * the depth, velocity and gradient values of this
     * QueryProfile.
     */
    public int[] nodeId = new int[0];

    /**
     * The interpolation coefficients that were applied to the
     * the depth, velocity and gradient values of the grid nodes
     * identified by nodeId to compute the corresponding data values
     * of this QueryProfile.
     */
    public double[] coefficient = new double[0];

    public QueryProfile()
    {
    }

    /**
     * Retrieve the number of model intervals intersected by this QueryProfile,
     * including zero thickness intervals.  This is the same as the number of
     * layers in the Earth model.
     * @return int the number of model intervals intersected by this QueryProfile
     */
    public int getNIntervals()
    {
        return depth.length;
    }

    /**
     * Retrieve the number of Grid nodes involved in the interpolation of the
     * information stored by this QueryProfile object.
     * @return int the number of Grid nodes involved in the interpolation of the
     * information stored by this QueryProfile object.
     */
    public int getNCoefficients()
    {
        return nodeId.length;
    }

    /**
     * Retrieve a formatted String representation of the
     * information in this QueryProfile object.
     * @return String a formatted String representation of the
     * information in this QueryProfile object.
     */
    @Override
    public String toString()
    {
        int i;
        StringBuffer s = new StringBuffer("QueryProfile: \n");
        s.append(String.format("   Latitude : %10.4f%n", Math.toDegrees(lat)));
        s.append(String.format("   Longitude: %10.4f%n%n", Math.toDegrees(lon)));
        s.append("     Depth        Thick      P Vel      S Vel\n");
        for (i=0; i<getNIntervals()-1; i++)
            s.append(String.format("%12.6f %10.4f %10.4f %10.4f%n",
                    depth[i], depth[i+1]-depth[i],
                    velocity[0][i], velocity[1][i]));

        i=getNIntervals()-1;
        s.append(String.format("%12.6f %10s %10.4f %10.4f%n",
                depth[i], " ",
                velocity[0][i], velocity[1][i]));

        s.append("               P Grad     S Grad\n");
        s.append(String.format("%12s %10.6f %10.6f%n%n",
                               "", gradient[0], gradient[1]));

        s.append(String.format("    NodeId      Coeff%n"));
        for (i=0; i<nodeId.length; i++)
            s.append(String.format("%10d %10.4f%n",
                                   nodeId[i], coefficient[i]));
        s.append('\n');
        return s.toString();

    }

}
