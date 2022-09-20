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
 * Stores data required for input to the travel time calculation, for the C++
 * <A href="../../../../../../../../SLBM/doc/html/class_great_circle.html">GreatCircle</A>
 * object in memory the last time {@link SlbmInterface#getGreatCircleData()}
 * was called.
 */
public class GreatCircleData
{
    /**
     * The phase that this GreatCircle supports.  Supported phases include
     *  Pn, Sn, Pg and Lg.
     */
    public String phase;

    /**
     * The horizontal separation of the LayerProfile
     * objects along the head wave interface, in radians.  This is the actual
     * separation of the LayerProfile object which may be reduced from the value
     * requested in the call to createGreatCircle() in order that some number of
     * equal sized increments will exactly fit between the source and receiver.
     */
    public double actualPathIncrement;

    /**
     * The depth of each interface in the earth model below the
     * source, in km.
     */
    public double[] sourceDepths = new double[0];

    /**
     * The P or S velocity of each interval below the source,
     * in km/sec.
     */
    public double[] sourceVelocitites = new double[0];

    /**
     * The depth of each interface in the earth model below the
     * receiver, in km.
     */
    public double[] receiverDepths = new double[0];

    /**
     * The P or S velocity of each interval below the receiver,
     * in km/sec.
     */
    public double[] receiverVelocities = new double[0];

    /**
     * The P or S velocity at the center of each
     * horizontal segment between the source and the receiver, in km/sec.
     * The first horizontal segment starts at the source, the last horizontal
     * segment ends at the receiver, and each one is of size actualPathIncrement.  The head
     * wave velocities are interpolated at the center of each of these horizontal
     * segments, just below the head wave interface.
     */
    public double[] headWaveVelocities = new double[0];

    /**
     * The P or S velocity gradient in the mantle at the center
     * of each horizontal segment of the head wave, in 1/sec.  For Pg and Lg,
     * the values will be -999999.
     */
    public double[] mantleGradients = new double[0];

    /**
     * The nodeIds of the neighboring
     * grid nodes used to derive the interpolated data at each head wave
     * profile.
    */
    public int[][] neighbors = new int[0][0];

    /**
     * The interpolation coefficients applied to each element of neighbors.
     */
    public double[][] coefficients = new double[0][0];

    public GreatCircleData()
    {
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();

        buf.append("Source/Receiver Profiles:\n");
        for (int i=0; i<sourceDepths.length; ++i)
            buf.append(String.format("%8.4f %8.4f    %8.4f %8.4f%n",
                    sourceDepths[i], sourceVelocitites[i], receiverDepths[i], receiverVelocities[i]));

        buf.append('\n');

        buf.append("Headwave velocities, gradients,  nodeIds and coefficients\n");

        for (int i=0; i<headWaveVelocities.length; ++i)
        {
            buf.append(String.format("%8.4f %9.6f", headWaveVelocities[i], mantleGradients[i]));
            for (int j=0; j<neighbors[i].length; ++j)
                buf.append(String.format(" %9d %9.6f", neighbors[i][j], coefficients[i][j]));
            buf.append('\n');
        }
        buf.append('\n');

        return buf.toString();
    }
}
