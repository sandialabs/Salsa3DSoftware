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
package gov.sandia.gmp.baseobjects.uncertainty;

import java.io.File;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;

public class UncertaintyAzimuth extends UncertaintyAzimuthSlowness implements UncertaintyInterface{

	public UncertaintyAzimuth() throws Exception {
		super();
	}

	public UncertaintyAzimuth(PropertiesPlusGMP properties, String prefix) throws Exception {
		super(properties, prefix);
	}

	public UncertaintyAzimuth(File f) throws Exception {
		super(f);
	}

	public UncertaintyAzimuth(String records) throws Exception {
		super(records);
	}

	@Override
	public GeoAttributes getUncertaintyType() {
		return GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_STATION_PHASE_DEPENDENT;
	}

	@Override
	public double getUncertainty(PredictionRequest predictionRequest) throws Exception {
		return super.getAzUncertainty(predictionRequest.getReceiver().getSta(), 
				predictionRequest.getPhase().toString());
	}

	@Override
	public String getUncertaintyModelFile(PredictionRequest request) throws Exception {
		return super.getUncertaintyModelFile();
	}

	@Override
	public String getUncertaintyVersion() {
		return getVersion();
	}

}
