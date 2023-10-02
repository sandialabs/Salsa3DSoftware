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
package gov.sandia.gmp.baseobjects.interfaces.impl;

import java.io.Serializable;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.UncertaintyInterface;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * Returns Globals.NA_VALUE no matter what is requested.
 * 
 * @author sballar
 *
 */
public class UncertaintyNAValue implements UncertaintyInterface, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8419904169472984003L;

	/**
	 * 
	 */
	public UncertaintyNAValue() {
	}

	/**
	 * 
	 */
	public UncertaintyNAValue(PropertiesPlus properties, String propertyPrefix) {
	}

	static public String getVersion() {
		return Utils.getVersion("base-objects");
	}

	@Override
	public double getUncertainty(PredictionRequest predictionRequest, GeoAttributes attribute)
			throws Exception {
		return Globals.NA_VALUE;
	}

	@Override
	public String getUncertaintyVersion() {
		return Utils.getVersion("base-objects");
	}

	/**
	 * Obstype must be one of TT, AZ, SH
	 */
	@Override
	public String getUncertaintyModelFile(PredictionRequest request, String obsType) throws Exception {
		return "internal";
	}

	@Override
	public String getUncertaintyType() {
		return "UncertaintyNAValue";
	}

	/**
	 * When uncertainty is requested and libcorr3d uncertainty is available
	 * return the libcorr uncertainty, otherwise return internally computed uncertainty.
	 */
	@Override
	public boolean isHierarchicalTT() {
		return false;
	}

	/**
	 * When uncertainty is requested and libcorr3d uncertainty is available
	 * return the libcorr uncertainty, otherwise return internally computed uncertainty.
	 */
	@Override
	public boolean isHierarchicalAZ() {
		return false;
	}

	/**
	 * When uncertainty is requested and libcorr3d uncertainty is available
	 * return the libcorr uncertainty, otherwise return internally computed uncertainty.
	 */
	@Override
	public boolean isHierarchicalSH() {
		return false;
	}

	@Override
	public GeoAttributes getUncertaintyComponent(GeoAttributes attribute) throws Exception {
	    switch (attribute) {
	    case TRAVEL_TIME :
		return GeoAttributes.TT_MODEL_UNCERTAINTY_NA_VALUE;
	    case AZIMUTH :
		return GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_NA_VALUE;
	    case SLOWNESS :
		return GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_NA_VALUE;
	    default:
		break;
	    }
	    throw new Exception("attribute must be one of TRAVEL_TIME, AZIMUTH, SLOWNESS");
	}

}
