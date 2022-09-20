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
 * Stores the weight assigned to each grid node that was touched by the
 * <A href="../../../../../../../../SLBM/doc/html/class_great_circle.html">GreatCircle</A>
 * that was in memory the last time
 * {@link gov.sandia.gnem.slbmjni.SlbmInterface#getWeights()} was called.
 *
 * <p>When {@link SlbmInterface#getWeights()}
 * is called, a map which associates an instance of a
 * <A href="../../../../../../../../SLBM/doc/html/class_great_circle.html">GreatCircle</A>
 * object with a double <I>weight</I> is initialized.  Then every
 * <A href="../../../../../../../../SLBM/doc/html/class_layer_profile.html">LayerProfile</A>
 * on the head wave interface between the source and
 * receiver is visited and the angular distance, <I>d</I>, that the ray
 * traveled in the horizontal segment is retreived.  If <I>d</I> &gt; 0,
 * then the neighboring
 * <A href="../../../../../../../../SLBM/doc/html/class_grid_profile.html">GridProfile</A>
 * objects that contributed to the interpolated value of the
 * <A href="../../../../../../../../SLBM/doc/html/class_layer_profile.html">LayerProfile</A>
 * are visited.  The product of <I>d * R * C</I> is added to the weight associated with that
 * <A href="../../../../../../../../SLBM/doc/html/class_grid_profile.html">GridProfile</A>
 * object, where <I>R</I> is the radius of the head wave interface for the
 * <A href="../../../../../../../../SLBM/doc/html/class_layer_profile.html">LayerProfile</A>
 * object being evaluated, and <I>C</I> is the interpolation coefficient for the
 * GridProfile - LayerProfile pair under consideration.  Then, all the
 * <A href="../../../../../../../../SLBM/doc/html/class_grid_profile.html">GridProfile</A>
 * objects in the map are visited, the grid node IDs extracted into int array <I>node</I>,
 * and the <I>weight</I> extracted into double array <I>weight</I>.
 *
 * <p>Note: Only grid nodes touched by the
 * <A href="../../../../../../../../SLBM/doc/html/class_great_circle.html">GreatCircle</A>
 * currently in memory are included in the output.  Each grid node is included only
 * once, even though more than one
 * <A href="../../../../../../../../SLBM/doc/html/class_layer_profile.html">LayerProfile</A>
 * object may have contributed some weight to it.  The sum of all the weights will equal
 * the horizontal distance traveled by the ray along the head wave interface, from the
 * source pierce point to the receiver pierce point, in km.
  */
public class GridWeight
{
    public GridWeight()
    {
    }

    /**
     * The IDs of the grid nodes that were 'touched' by
     * the current GreatCircle.
     */
    public int[] node = new int[0];

    /**
     * The weight accumulated by each node.
     */
    public double[] weight = new double[0];

    /**
     * Retrieve the sum of the weights.  This should be
     * equal to getHeadwaveDistanceKm();
     * @return the sum of the weights
     */
    public double getSum()
    {
        double sum = 0;
        for (double w : weight)
            sum += w;
        return sum;
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
        StringBuffer s = new StringBuffer("Grid Node Weights: \n\n");
        s.append(String.format("  Node     Weight%n"));
        for (int i=0; i<node.length; i++)
            s.append(String.format("%6d %10.4f%n", node[i], weight[i]));
        return s.toString();
    }

}
