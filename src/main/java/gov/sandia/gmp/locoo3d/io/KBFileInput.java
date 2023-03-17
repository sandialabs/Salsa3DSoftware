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
package gov.sandia.gmp.locoo3d.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

public class KBFileInput extends KBInput {

    public KBFileInput() {
    }

    public KBFileInput(PropertiesPlusGMP properties) throws Exception {
	super(properties);

	// set optional input and output db_table_def columns not used by LocOO.
	setLocOOOptionalTableColumns();

	// set the token delimiter for file based input if it is defined in the
	// properties file 

	String tokenDelimiter = properties.getProperty("dataLoaderFileInputTokenDelimiter", " ");
	if (!tokenDelimiter.equals(" "))
	    BaseRow.setTokenDelimiter(tokenDelimiter);

	// create input file hashmaps
	HashMap<String, File> inputFiles = new HashMap<String, File>();
	// fill up input files with any defined in the property settings
	inputFileCheck("dataLoaderFileInputOrigins", "Origin", inputFiles);
	inputFileCheck("dataLoaderFileInputSites", "Site", inputFiles);
	inputFileCheck("dataLoaderFileInputArrivals", "Arrival", inputFiles);
	inputFileCheck("dataLoaderFileInputAssocs", "Assoc", inputFiles);

	// load data
	ArrayList<OriginExtended> origins = new ArrayList<OriginExtended>(
		OriginExtended.readOriginExtended(inputFiles));

	Set<Long> orids = new HashSet<Long>();
	for (OriginExtended o : origins)
	    if (orids.contains(o.getOrid()))
		throw new Exception("Found more than one origin with orid = "+o.getOrid()+ " in the input data set.");
	    else
		orids.add(o.getOrid());

	OriginExtended masterEvent = null;
	
	String masterEventWhereClause = properties.getProperty("masterEventWhereClause");

	if (masterEventWhereClause != null)
	{
	    String orid = masterEventWhereClause.split("=")[1].trim();
	    long masterEventOrid = Long.valueOf(orid);

	    for (OriginExtended origin : origins)
		if (origin.getOrid() == masterEventOrid)
		    masterEvent = origin;

	    if (masterEvent == null)
		throw new Exception("Master event origin where orid = "+orid+" does not exist in the input data set.");

	    //origins.remove(masterEvent);

	    // Create the predictors, using the PredictorFactory
	    PredictorFactory predictors = new PredictorFactory(properties,"loc_predictor_type", logger);

	    masterEventCorrections = getMasterEventCorrections(new Source(masterEvent),
		    predictors, "masterEvent with orid " + masterEvent.getOrid() + " loaded from file "
			    +  inputFiles.get("Origin").getAbsolutePath());

	}

	VectorGeo.earthShape = EarthShape.valueOf(properties.getProperty("earthShape",
		VectorGeo.earthShape.name()).toUpperCase());

	inputOrigins = new LinkedHashMap<Long, OriginExtended>(origins.size());
	
	// see if the user specified a list of orids to process in the properties file
	orids.clear();
	for (long o : properties.getLongArray("dataLoaderFileOrids", new long[] {}))
	    orids.add(o);

	if (orids.isEmpty())
	{
	    // orids were not specified in properties file.
	    // Process all input origins
	    for (OriginExtended origin: origins)
		inputOrigins.put(origin.getOrid(), origin);
	}
	else
	{
	    for (OriginExtended origin: origins)
		if (orids.contains(origin.getOrid()))
		    inputOrigins.put(origin.getOrid(), origin);
	}
    }



    /**
     * Sets db_table_def optional columns that are not required by LocOO. This allows
     * users to omit these columns from input and output tables if desired.
     * 
     * @throws IOException
     */
    private void setLocOOOptionalTableColumns() throws IOException
    {
	gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin.getColumns().setColumnRequiredStatus(
		gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin.getColumns().getColumnNames(), false);

	gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc.getColumns().setColumnRequiredStatus(
		gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc.getColumns().getColumnNames(), false);

	gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival.getColumns().setColumnRequiredStatus(
		gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival.getColumns().getColumnNames(), false);

	gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site.getColumns().setColumnRequiredStatus(
		gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site.getColumns().getColumnNames(), false);

	gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origerr.getColumns().setColumnRequiredStatus(
		gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origerr.getColumns().getColumnNames(), false);

	gov.sandia.gnem.dbtabledefs.nnsa_kb_custom.Azgap.getColumns().setColumnRequiredStatus(
		gov.sandia.gnem.dbtabledefs.nnsa_kb_custom.Azgap.getColumns().getColumnNames(), false);

	gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Network.getColumns().setColumnRequiredStatus(
		gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Network.getColumns().getColumnNames(), false);

	gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Event.getColumns().setColumnRequiredStatus(
		gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Event.getColumns().getColumnNames(), false);

    }

    private void inputFileCheck(String fileProp, String fileType, 
	    HashMap<String, File> inputFiles) throws IOException, Exception
    {
	String prop = properties.getProperty(fileProp, "");
	if (prop.equals(""))
	    throw new IOException("Error: DataLoaderFile input property \"" +
		    fileProp + "\" is required to load "+ fileType +
		    "s but was not defined ...");

	File f = new File(prop);
	if (((f == null) || !f.isFile()))
	    throw new IOException("Error: DataLoaderFile input file \"" +
		    fileProp + " = " + prop + "\" is required to load "+ fileType +
		    "s but was not found ...");
	else if ((f != null) && f.isFile())
	    inputFiles.put(fileType, f);
    }

    @Override
    public void close() throws Exception {
	
    }

}
