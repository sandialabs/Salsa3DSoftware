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
import java.util.LinkedHashMap;
import java.util.Map;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class UncertaintySourceDependent implements UncertaintyInterface{
    
    private GeoTessPosition uncertaintyModel;
    
    private File modelFile;
    
    public UncertaintySourceDependent(PropertiesPlus properties, String prefix) throws Exception {
	
	modelFile = properties.getFile(prefix+"UncertaintyModel", properties.getFile(prefix+"Model"));
		
	uncertaintyModel = getGeoTessModel(modelFile).getGeoTessPosition();
    }

    @Override
    public double getUncertainty(PredictionRequest predictionRequest) throws Exception {
	return uncertaintyModel.set(predictionRequest.getSource().getUnitVector(),
		predictionRequest.getSource().getRadius()).getValue(0);
    }

    @Override
    public String getUncertaintyVersion() {
	return getVersion();
    }

    static public String getVersion() {
	return Utils.getVersion("base-objects");
    }

    @Override
    public String getUncertaintyModelFile(PredictionRequest request) throws Exception {
	return modelFile.getCanonicalPath();
    }

    @Override
    public UncertaintyType getUncertaintyType() {
	return UncertaintyType.SOURCE_DEPENDENT;
    }

    /**
     * A map of File name to GeoTessModel. 
     */
    static private Map<String, GeoTessModel> geotessModels = new LinkedHashMap<>();

    static private GeoTessModel getGeoTessModel(File modelFile) throws Exception
    {
	if (modelFile == null)
	    throw new GMPException("modelFile is null");

	if (!modelFile.exists())
	    throw new GMPException("modelFile "+modelFile.toString()+" does not exist.");

	if (modelFile.isDirectory() && new File(modelFile, "source_dependent_uncertainty.geotess").exists())
	    modelFile = (new File(modelFile, "source_dependent_uncertainty.geotess"));

	String modelFileName = modelFile.getCanonicalPath();

	GeoTessModel model;
	synchronized(geotessModels) {

	    model = geotessModels.get(modelFileName);

	    if(model != null) return model;

	    for (int ecnt = 0; ecnt < 10; ++ecnt) {
		try {
		    model = GeoTessModel.getGeoTessModel(modelFile);

		    if(model != null) {
			geotessModels.put(modelFileName, model);
			return model;
		    }
		} catch (Exception ex) {
		    // unsuccessful ... wait 5 seconds and try again
		    ex.printStackTrace();
		    try {
			Thread.sleep(5000);
		    } catch (InterruptedException e) {
		    }
		}
	    }

	    throw new Exception("Failed to read in geotess model at " + modelFileName + "\n");
	}
    }

}
