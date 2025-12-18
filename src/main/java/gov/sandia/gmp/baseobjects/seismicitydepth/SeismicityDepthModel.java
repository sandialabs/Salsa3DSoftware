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
package gov.sandia.gmp.baseobjects.seismicitydepth;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.numerical.vector.GeoMath;

/**
 * Simple class with static methods to load the Seismicity Depth Model from either
 * an internal resources directory or from an external file.
 * @author sballar
 *
 */
public class SeismicityDepthModel {

    /**
     * A map of File name to GeoTessModel. 
     */
    private static Map<String, GeoTessModel> seismicityDepthModels = new LinkedHashMap<>();

    /**
     * If fileName is null, return null.  If fileName is 'default' or 'internal' then 
     * load the seismicity depth model from the internal resources directory.  
     * Otherwise load it from the specified external file.
     * @param fileName
     * @return
     * @throws Exception
     */
    static synchronized public GeoTessModel getModel(String fileName) throws Exception
    {
	if (fileName == null)
	    return null;

	if (fileName.equalsIgnoreCase("default") || fileName.equalsIgnoreCase("internal")) 
	    fileName = "internal";
	else
	    fileName = new File(fileName).getCanonicalPath();

	GeoTessModel model = seismicityDepthModels.get(fileName);

	if (model != null) return model;

	if (fileName.equals("internal")) {
	    model = getModel();
	    if (model != null) {
		testModel(model);
		seismicityDepthModels.put("internal", model);
		return model;
	    }
	}

	File f = new File(fileName);
	if (!f.exists())
	    throw new Exception("Cannot load seismicity depth model because file "+fileName+" does not exist.");
	model = new GeoTessModel(fileName);
	testModel(model);
	seismicityDepthModels.put(fileName, model);
	return model;
    }


    /**
     * load the seismicity depth model from the internal resources directory
     * @return
     * @throws Exception
     */
    static public GeoTessModel getModel() throws Exception
    {
	InputStream is =Utils.getResourceAsStream("seismicity_depth_v2.geotess");
	
	if (is == null) {
		File f = new File("../base-objects/src/main/resources/seismicity_depth_v2.geotess");
		try {
			is = new BufferedInputStream(new FileInputStream(f));
		} catch (FileNotFoundException e) {
		}
	}
		
	if (is == null)
	    throw new IOException("Resource seismicity_depth_v2.geotess not found");
	return testModel(new GeoTessModel(new DataInputStream(is)));
    }

    /**
     * If fileName is null, return null.  If fileName is 'default' or 'internal' then 
     * load the seismicity depth model from the internal resources directory.  
     * Otherwise load it from the specified external file.
     * @param fileName
     * @return
     * @throws Exception
     */
    static public GeoTessPosition getGeoTessPosition(String fileName) throws Exception
    {
	if (fileName == null) return null;
	GeoTessModel m = getModel(fileName);
	if (m == null)
	    return null;
	return getModel(fileName).getGeoTessPosition();
    }

    /**
     * If fileName is null, return null.  If fileName is 'default' or 'internal' then 
     * load the seismicity depth model from the internal resources directory.  
     * Otherwise load it from the specified external file.
     * @param fileName
     * @return
     * @throws Exception
     */
    static public GeoTessPosition getGeoTessPosition(String fileName, InterpolatorType horizontalInterpolatorType) throws Exception
    {
	if (fileName == null) return null;
	GeoTessModel m = getModel(fileName);
	if (m == null)
	    return null;
	return getModel(fileName).getGeoTessPosition(horizontalInterpolatorType);
    }

    /**
     * load the seismicity depth model from the internal resources directory
     * @return
     * @throws Exception
     */
    static public GeoTessPosition getGeoTessPosition() throws Exception
    {
	GeoTessModel m = getModel();
	if (m == null)
	    return null;
	return m.getGeoTessPosition();
    }

    /**
     * load the seismicity depth model from the internal resources directory
     * @return
     * @throws Exception
     */
    static public GeoTessPosition getGeoTessPosition(InterpolatorType horizontalInterpolatorType) throws Exception
    {
	GeoTessModel m = getModel();
	if (m == null)
	    return null;
	return m.getGeoTessPosition(horizontalInterpolatorType);
    }

    /**
     * ensure the loaded model has attributes SEISMICITY_DEPTH_MIN and SEISMICITY_DEPTH_MAX
     * @param model
     * @return
     * @throws Exception
     */
    static private GeoTessModel testModel(GeoTessModel model) throws Exception
    {
	int seismicityDepthMinIndex = model.getMetaData().getAttributeIndex("SEISMICITY_DEPTH_MIN");
	if (seismicityDepthMinIndex < 0)
	    throw new Exception("Seismicity depth model does not contain attribute SEISMICITY_DEPTH_MIN\n"
		    +"Model contains attributes "+model.getMetaData().getAttributeNamesString());

	if (seismicityDepthMinIndex != 0)
	    throw new Exception(String.format("SEISMICITY_DEPTH_MIN index = %d but must = 0%n"
		    +"Model contains attributes %s %n", seismicityDepthMinIndex,
		    model.getMetaData().getAttributeNamesString()));

	int seismicityDepthMaxIndex = model.getMetaData().getAttributeIndex("SEISMICITY_DEPTH_MAX");

	if (seismicityDepthMaxIndex < 0)
	    throw new Exception("Seismicity depth model does not contain attribute SEISMICITY_DEPTH_MAX\n"
		    +"Model contains attributes "+model.getMetaData().getAttributeNamesString());

	if (seismicityDepthMaxIndex != 1)
	    throw new Exception(String.format("SEISMICITY_DEPTH_MAX index = %d but must = 1%n"
		    +"Model contains attributes %s %n", seismicityDepthMaxIndex,
		    model.getMetaData().getAttributeNamesString()));

	model.setEarthShape(GeoMath.getEarthShape());

	return model;
    }

}
