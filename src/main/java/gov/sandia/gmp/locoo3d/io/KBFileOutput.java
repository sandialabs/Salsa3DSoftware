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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origerr;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_custom.Azgap;

public class KBFileOutput extends KBOutput{

    private Map<String, File> outputFiles;
    
    private Map<String, BufferedWriter> writers;

    public KBFileOutput() {
	super();
    }

    public KBFileOutput(PropertiesPlusGMP properties) throws Exception {
	this(properties, null);
    }

    public KBFileOutput(PropertiesPlusGMP properties, NativeInput dataInput) throws Exception {
	
	super(properties, dataInput);
	// set optional input and output db_table_def columns not used by LocOO.

	setLocOOOptionalTableColumns();

	// create input and output file hashmaps

	outputFiles = new HashMap<String, File>();

	outputFileCheck("dataLoaderFileOutputOrigins", "Origin", outputFiles);
	outputFileCheck("dataLoaderFileOutputOrigerrs", "Origerr", outputFiles);
	outputFileCheck("dataLoaderFileOutputAzgaps", "Azgap", outputFiles);
	outputFileCheck("dataLoaderFileOutputAssocs", "Assoc", outputFiles);
	outputFileCheck("dataLoaderFileOutputArrivals", "Arrival", outputFiles);
	outputFileCheck("dataLoaderFileOutputSites", "Site", outputFiles);
	outputFileCheck("dataLoaderFileOutputPredictions", "Prediction", outputFiles);

	// override the value of property outputTableTypes with all the 
	// table types that were specified with file names.
	String outputTableTypes = "";
	for (String s : outputFiles.keySet())
	    outputTableTypes += ","+s.toLowerCase();

	if (outputTableTypes.length() > 0)
	{
	    properties.remove("dbOutputTableTypes");
	    properties.setProperty("outputTableTypes", 
		    outputTableTypes.substring(1));
	}

	String[] columns = properties.getProperty("dataLoaderFileOutputOriginColumns", 
		Arrays.toString(Origin.getColumns().getColumnNames()).replace("[", "").replace("]",""))
		.replaceAll(",", " ").split("\\s+");

	Origin.setNewOutputColumnNames(columns);

	columns = properties.getProperty("dataLoaderFileOutputOrigerrColumns", 
		Arrays.toString(Origerr.getColumns().getColumnNames()).replace("[", "").replace("]",""))
		.replaceAll(",", " ").split("\\s+");

	Origerr.setNewOutputColumnNames(columns);

	columns = properties.getProperty("dataLoaderFileOutputAzgapColumns", 
		Arrays.toString(Azgap.getColumns().getColumnNames()).replace("[", "").replace("]",""))
		.replaceAll(",", " ").split("\\s+");

	Azgap.setNewOutputColumnNames(columns);

	columns = properties.getProperty("dataLoaderFileOutputAssocColumns", 
		Arrays.toString(Assoc.getColumns().getColumnNames()).replace("[", "").replace("]",""))
		.replaceAll(",", " ").split("\\s+");

	Assoc.setNewOutputColumnNames(columns);

	columns = properties.getProperty("dataLoaderFileOutputArrivalColumns", 
		Arrays.toString(Arrival.getColumns().getColumnNames()).replace("[", "").replace("]",""))
		.replaceAll(",", " ").split("\\s+");

	Arrival.setNewOutputColumnNames(columns);

	columns = properties.getProperty("dataLoaderFileOutputSiteColumns", 
		Arrays.toString(Site.getColumns().getColumnNames()).replace("[", "").replace("]",""))
		.replaceAll(",", " ").split("\\s+");

	Site.setNewOutputColumnNames(columns);

	String currentDelimeter = BaseRow.getTokenDelimiter();
	String outputTokenDelimeter = properties.getProperty("dataLoaderFileOutputTokenDelimiter", 
		currentDelimeter);

	BaseRow.setTokenDelimiter(outputTokenDelimeter);

	HashMap<String, String> headers = new HashMap<String, String>();
	headers.put("Origin", Origin.getHeader());
	headers.put("Origerr", Origerr.getHeader());
	headers.put("Assoc", Assoc.getHeader());
	headers.put("Azgap", Azgap.getHeader());
	headers.put("Arrival", Arrival.getHeader());
	headers.put("Site", Site.getHeader());

	BaseRow.setTokenDelimiter(currentDelimeter);

	// create BufferedWriters for every output type and write headers
	writers = new HashMap<String, BufferedWriter>(6);
	for (String type : new String[] {"Origin", "Origerr", "Azgap", "Assoc", "Arrival", "Site"})
	    if (outputFiles.containsKey(type))
	    {
		outputFiles.get(type).getParentFile().mkdirs();
		writers.put(type, new BufferedWriter(new FileWriter(outputFiles.get(type))));
		writers.get(type).write(headers.get(type) + "\n");
	    }
    }

    /**
     * Sets db_table_def optional columns that are not required by LocOO. This allows
     * users to omit these columns from input and output tables if desired.
     * 
     * @throws IOException
     */
    private static void setLocOOOptionalTableColumns() throws IOException
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

    /**
     * Checks to see if <i>fileProp</i> is specified.  If it is then then entry is added to <i>outputFiles</i>
     * with key = <i>fileType</i> and value = <i>properties.getProperty(fileProp)</i>.
     * @param fileProp
     * @param fileType
     * @param outputFiles
     */
    private void outputFileCheck(String fileProp, String fileType,
	    Map<String, File> outputFiles)
    {
	String prop = properties.getProperty(fileProp, "");
	if (prop.equals("")) return;

	File f = new File(prop);
	outputFiles.put(fileType, f);
    }

    @Override
    void writeData() throws Exception {

	if (outputOrigins.isEmpty())
	    return;

	String currentDelimeter = BaseRow.getTokenDelimiter();
	String outputTokenDelimeter = properties.getProperty("dataLoaderFileOutputTokenDelimiter", 
		currentDelimeter);

	BaseRow.setTokenDelimiter(outputTokenDelimeter);

	BufferedWriter originWriter = writers.get("Origin");
	BufferedWriter origerrWriter = writers.get("Origerr");
	BufferedWriter assocWriter = writers.get("Assoc");
	BufferedWriter azgapWriter = writers.get("Azgap");
	BufferedWriter arrivalWriter = writers.get("Arrival");
	BufferedWriter siteWriter = writers.get("Site");

	for (OriginExtended origin : outputOrigins.values()) 
	{
	    if (origin != null)
	    {
		if (originWriter != null)
		    origin.writeln(originWriter);
		if (origin.getOrigerr() != null && origerrWriter != null)
		    origin.getOrigerr().writeln(origerrWriter);
		if (origin.getAzgap() != null && azgapWriter != null)
		    origin.getAzgap().writeln(azgapWriter);
		if (assocWriter != null) 
		    for (AssocExtended assoc : origin.getAssocs().values()) 
			assoc.writeln(assocWriter);
		if (arrivalWriter != null) 
		    for (AssocExtended assoc : origin.getAssocs().values()) 
			assoc.getArrival().writeln(arrivalWriter);
		if (siteWriter != null) 
		    for (AssocExtended assoc : origin.getAssocs().values()) 
			assoc.getSite().writeln(siteWriter);
	    }
	}
	
	if (outputFiles.get("Predictions") != null) 
	    NativeOutput.writePredictions(outputFiles.get("Predictions"), predictions);
	    


	for (BufferedWriter w : writers.values())
	    w.flush();

	BaseRow.setTokenDelimiter(currentDelimeter);
	
    }

    @Override
    public void close() throws Exception {
	for (BufferedWriter writer : writers.values())
	    writer.close();
	writers.clear();
    }

}
