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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.tttables.AttributeTables;
import gov.sandia.gmp.baseobjects.tttables.Table;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * @author sballar
 *
 */
public class UncertaintyDistanceDependent implements UncertaintyInterface {

    protected static final Map<File,AttributeTables> CACHE = new HashMap<>();

    protected AttributeTables uncertaintyTables;

    private File uncertaintyDirectory;

    static public String getVersion() {
	return Utils.getVersion("base-objects");
    }

    @Override
    public String getUncertaintyVersion() {
	return getVersion();
    }

    /**
     * 
     * @param properties
     * @throws Exception if 'prefixUncertaintyDirectory' and
     *                      'prefixUncertaintyModel' properties are not specified.
     */
    public UncertaintyDistanceDependent(PropertiesPlus properties, String prefix) throws Exception {
	uncertaintyDirectory = properties.getFile(prefix + "UncertaintyDirectory",
		new File("seismic-base-data.jar"));

	String modelName;

	if (uncertaintyDirectory.exists() && uncertaintyDirectory.isDirectory() &&
		new File(uncertaintyDirectory, "distance_dependent_uncertainty").exists() &&
		new File(uncertaintyDirectory, "prediction_model.geotess").exists())
	{
	    uncertaintyDirectory = new File(uncertaintyDirectory, "distance_dependent_uncertainty");
	    File ttDir = new File(uncertaintyDirectory, "tt");

	    String[] list = ttDir.list();
	    // must ignore all file names that start with '.'
	    ArrayList<String> modelFiles = new ArrayList<>(list.length);
	    for (String s : list)
		if (new File(ttDir, s).isDirectory())
		    modelFiles.add(s);

	    if (modelFiles.size() != 1)
		throw new Exception(String.format("Expected to find 1 uncertainty model file in directory%n"
			+ "%s%n"
			+ "but found %d:%n"
			+ "%s%n",
			new File(uncertaintyDirectory, "tt").getAbsolutePath(),
			modelFiles.size(), Arrays.toString(modelFiles.toArray())));
	    modelName = modelFiles.get(0);
	}
	else
	{
	    modelName = properties.getProperty(prefix + "UncertaintyModel", "ak135");

	    if (modelName.length() == 0)
		throw new Exception(
			prefix + "UncertaintyModel is not specified in the property file. " + "Suggested value is ak135");
	}

	synchronized(CACHE) {
	    uncertaintyTables = CACHE.get(uncertaintyDirectory);

	    if(uncertaintyTables == null) {
		uncertaintyTables = new AttributeTables(uncertaintyDirectory, modelName);
		CACHE.put(uncertaintyDirectory, uncertaintyTables);
	    }
	}

	uncertaintyDirectory = new File(uncertaintyDirectory, modelName);
    }

    /**
     * Ensures that all necessary files are specified in par file and exist in file
     * system.
     * 
     * @param properties
     * @param prefix
     * @return error messages or empty string if no errors.
     * @throws Exception
     */
    static public String checkFiles(PropertiesPlusGMP properties, String prefix) throws Exception {
	File seismicBaseData = properties.getFile(prefix + "UncertaintyDirectory");

	String errMessage = "";
	if (seismicBaseData == null)
	    errMessage += prefix + "UncertaintyDirectory is not specified in the property file" + Globals.NL;

	String modelName = properties.getProperty(prefix + "UncertaintyModel", "");

	if (modelName.length() == 0)
	    errMessage += prefix + "UncertaintyModel is not specified in the property file." + Globals.NL;

	File f = new File(new File(seismicBaseData, "tt"), modelName);
	if (!f.exists())
	    try {
		errMessage += String.format(prefix + "UncertaintyModel %s not found in file sytem%n",
			f.getCanonicalPath());
	    } catch (Exception e) {
		errMessage += e.getMessage();
	    }
	return errMessage;
    }

    /**
     * Returns true if the files containing the uncertainty information were
     * successfully located.
     * 
     * @return
     */
    public boolean isValid() {
	return uncertaintyTables != null;
    }

    public Table getTable(GeoAttributes attribute, SeismicPhase phase) throws Exception, Exception {
	return uncertaintyTables.getTable(attribute, phase);
    }

    /*
     * Return uncertainty estimate interpolated from the bottom of travel time tables. 
     */
    @Override
    public double getUncertainty(PredictionRequest request) throws Exception {
	if (isValid()) {
	    return uncertaintyTables.getValue(GeoAttributes.TT_MODEL_UNCERTAINTY, request.getPhase(), 
		    request.getDistanceDegrees(), request.getSource().getDepth());
	}
	return Globals.NA_VALUE;
    }

    @Override
    public String getUncertaintyModelFile(PredictionRequest request) throws Exception {
	if (uncertaintyDirectory.toString().startsWith("seismic-base-data.jar"))
	    return uncertaintyDirectory.toString();
	return getTable(GeoAttributes.TT_MODEL_UNCERTAINTY, request.getPhase()).getFile().getCanonicalPath();
    }

    @Override
    public UncertaintyType getUncertaintyType() {
	return UncertaintyType.DISTANCE_DEPENDENT;
    }

}
