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
public class QueryNeighborInfo
{
    /**
     * Node ID of interest.
     */
    public int nid;


    /**
     * The node IDs of neighbors of nid.
     */
    public int[] neighbors = new int[0];

    /**
     * An array of distances between nid and
     * neighbors of nid
     */
    public double[] distance = new double[0];

    /**
     * An array of azimuths between nid and
     * neighbors of nid
     */
    public double[] azimuth = new double[0];

    /**
     * The number of neighbors nid has
     */
    public int nNeighbors = -1;

    public QueryNeighborInfo()
    {
    }

    /**
     * R
     * @return int the number of neighbors
     */
    public int getNNeighbors()
    {
        return nNeighbors;
    }

     /**
     * Retrieve a formatted String representation of the
     * information in this QueryNeighborInfo object.
     * @return String a formatted String representation of the
     * information in this QueryNeighborInfo object.
     */
    @Override
    public String toString()
    {
        StringBuffer s = new StringBuffer("QueryNeighborInfo: \n");
        s.append(String.format("  Node : %7d%n", nid ));
        s.append("  Neighbor    Distance    Azimuth\n");
        for (int i=0; i<getNNeighbors(); i++)
            s.append(String.format("%7d %10.4f %10.4f%n",
                     neighbors[i], Math.toDegrees( distance[i] ), Math.toDegrees( azimuth[i]) ) );
        s.append('\n');
        return s.toString();

    }

}
