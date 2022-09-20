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
package gov.sandia.gmp.bender;

import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;

/**
 * 
 * @author sballar
 *
 */
public class BenderPredictionRequest extends PredictionRequest {

    private static final long serialVersionUID = 1L;

    /**
     * Used by predictors that understand the behavior of reflective phases that are
     * defined with under side reflective bounce points. If the under side reflected
     * phase bounce point object is requested but not defined it is first created
     * and then returned.
     */
    private UndersideReflectedPhaseBouncePoint undersideReflectedPhaseBouncePoint = null;

    public BenderPredictionRequest(PredictionRequest request) throws Exception {
	super(request);
    }

    public UndersideReflectedPhaseBouncePoint getUndersideReflectedPhaseBouncePoint() {
	if (undersideReflectedPhaseBouncePoint == null)
	    undersideReflectedPhaseBouncePoint = new UndersideReflectedPhaseBouncePoint();

	return undersideReflectedPhaseBouncePoint;
    }

}
