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

public class ZhaoParameters
{
    /**
     * the velocity at the top of the mantle averaged along the Moho
     * between the source and receiver pierce points.
     */
    public double Vm;

    /**
     * the velocity gradient at the top of the mantle averaged along the Moho
     * between the source and receiver pierce points.
     */
    public double Gm;

    /**
     * the turning depth of the ray relative to the Moho
     */
    public double H;

    /**
     * a constant whose product with V0 gives the mantle velocity gradient
     * for a flat Earth; V0 is the velocity of the top of the mantle averaged over
     * the whole model.
     */
    public double C;

    /**
     * a constant whose product with Vm gives the mantle velocity gradient
     * for a flat Earth.
     */
    public double Cm;

    /**
     * a value of 0 indicates the source is in the crust;
     * +1 indicates the ray leaves a mantle source in the downgoing
     * direction;  -1 indicates the ray leaves a mantle source in an upgoing direction.

     */
    public int udSign;

    public ZhaoParameters()
    {
    }

    public String toString()
    {
        return String.format("Vm=%1.4f  Gm=%1.6f  H=%1.3f  C=%1.9f  Cm=%1.9f  udSign=%d",
                Vm, Gm, H, C, Cm, udSign);
    }
}
