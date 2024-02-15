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
package gov.sandia.gmp.baseobjects.globals;

/**
 * Specification of how energy travels through the Earth: P as a compressional wave, S as a shear wave
 * @author sballar
 *
 */
public enum WaveType { 
    //Lots of serialization throughout SALSA3D depends on the order being correct here and we can't
    //predict how long serialized things in SALSA3D may end up being retained and relied upon by our
    //customers, so don't change the order (appending new types as needed is fine).
    //
    //bjlawry, 2023/12/15
  
    /**
     * Energy travels through the Earth as a compressional wave
     */
    P (GeoAttributes.PSLOWNESS), 

    /**
     * Energy travels through the Earth as a shear wave
     */
    S (GeoAttributes.SSLOWNESS), 
    
    /**
     * Energy travels as an acoustic wave through the atmosphere.
     */
    I (null), 
    
    /**
     * Energy travels as an acoustic wave in the ocean.
     */
    H (null);
    
    /**
     * The GeoAttribute that supports transmission of a wave of this WaveType through
     * a physical medium.
     */
    private GeoAttributes attribute;
    
    private WaveType (GeoAttributes attribute) { this.attribute = attribute; }
    
    /**
     * The GeoAttribute that supports transmission of a wave of this WaveType through
     * a physical medium. Either GeoAttributes.PSLOWNESS or GeoAttributes.SSLOWNESS
     * 
     * @return the GeoAttribute that supports transmission of a wave of this WaveType through
     * a physical medium.
     */
    public GeoAttributes getAttribute() { return attribute; }
}
