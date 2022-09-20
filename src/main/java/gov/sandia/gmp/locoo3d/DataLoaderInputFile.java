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
package gov.sandia.gmp.locoo3d;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.NetworkExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

public class DataLoaderInputFile implements DataLoaderInput
{
    private HashMap<String, File> inputFiles;


    private PropertiesPlusGMP properties;
    private ScreenWriterOutput logger;

    private NetworkExtended network;
    private List<OriginExtended> origins;

    /**
     * Map from sta/phase -> tt,az,sh corrections for master event relocation.
     * Units are tt (sec), az (radians), sh (sec/radian).
     * Default values are Assoc.TIMERES_NA, Assoc.AZRES_NA, Assoc.SLORES_NA.
     */
    private HashMap<String, double[]> masterEventCorrections;

    private HashMap<Long, OriginExtended> originMap;

    public DataLoaderInputFile(PropertiesPlusGMP properties, ScreenWriterOutput logger, 
	    ScreenWriterOutput errorlog) throws Exception {
	{
	    this.properties = properties;
	    this.logger = logger;

	    // set optional input and output db_table_def columns not used by LocOO.

	    setLocOOOptionalTableColumns();

	    // create input file hashmaps

	    inputFiles = new HashMap<String, File>();

	    // set the token delimiter for file based input if it is defined in the
	    // properties file 

	    String tokenDelimiter = properties.getProperty("dataLoaderFileInputTokenDelimiter", "");
	    if (!tokenDelimiter.equals(" "))
		BaseRow.setTokenDelimiter(tokenDelimiter);

	    // make a new network to hold the input data.

	    network = new NetworkExtended();

	    // fill up input files with any defined in the property settings
	    inputFileCheck("dataLoaderFileInputOrigins", "Origin", inputFiles);
	    inputFileCheck("dataLoaderFileInputSites", "Site", inputFiles);
	    inputFileCheck("dataLoaderFileInputArrivals", "Arrival", inputFiles);
	    inputFileCheck("dataLoaderFileInputAssocs", "Assoc", inputFiles);

	    // load data
	    origins = new ArrayList<OriginExtended>(
		    OriginExtended.readOriginExtended(inputFiles, network));

	    {
		Set<Long> orids = new HashSet<Long>();
		for (OriginExtended o : origins)
		    if (orids.contains(o.getOrid()))
			throw new Exception("Found more than one origin with orid = "+o.getOrid()+ " in the input data set.");
		    else
			orids.add(o.getOrid());
	    }

	    OriginExtended masterEvent = null;
	    String masterEventWhereClause = properties.getProperty("masterEventWhereClause");
	    if (masterEventWhereClause != null)
	    {
		// masterEventWhereClause = orid = xxxxx

		String orid = masterEventWhereClause.split("=")[2].trim();
		long masterEventOrid = Long.valueOf(orid);

		for (OriginExtended origin : origins)
		    if (origin.getOrid() == masterEventOrid)
			masterEvent = origin;

		if (masterEvent == null)
		    throw new Exception("Master event origin where orid = "+orid+" does not exist in the input data set.");

		origins.remove(masterEvent);

		// Create the predictors, using the PredictorFactory
		PredictorFactory predictors = new PredictorFactory(properties,"loc_predictor_type");

		masterEventCorrections = DataLoaderInput.getMasterEventCorrections(masterEvent,
			predictors, logger, "masterEvent with orid " + masterEvent.getOrid() + " loaded from file "
				+  inputFiles.get("origin").getAbsolutePath());

	    }

	    String earthShape = properties.getProperty("earthShape",
		    VectorGeo.earthShape.name());
	    VectorGeo.earthShape = EarthShape.valueOf(earthShape);
	    originMap = new HashMap<Long, OriginExtended>(origins.size());

	    // see if the user specified a list of orids to process in the properties file
	    Set<Long> orids = new HashSet<Long>();
	    for (long o : properties.getLongArray("dataLoaderFileOrids", new long[] {}))
		orids.add(o);

	    if (orids.isEmpty())
	    {
		// orids were not specified in properties file.
		// Process all input origins
		for (OriginExtended origin: origins)
		    originMap.put(origin.getOrid(), origin);
	    }
	    else
	    {
		for (OriginExtended origin: origins)
		    if (orids.contains(origin.getOrid()))
			originMap.put(origin.getOrid(), origin);

		origins = new ArrayList<OriginExtended>(originMap.values());

	    }
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
    public ArrayList<ArrayListLong> readTaskSourceIds() throws SQLException,
    GMPException
    {
	int ndefMax = Math.min(1000, properties.getInt("batchSizeNdef", 1000));

	for (OriginExtended origin: origins)
	    origin.setNdef();

	OriginExtended.sortByNdefDescending(origins);

	int count=0;
	ArrayList<ArrayListLong> batches = new ArrayList<ArrayListLong>();

	ArrayListLong batch = new ArrayListLong();
	batches.add(batch);
	long n=0, ndef;
	for (OriginExtended origin : origins)
	{
	    ++count;
	    ndef = origin.getNdef();
	    if (ndef <= 0)
		ndef = 10;
	    if (batch.size() > 0 && n+ndef > ndefMax)
	    {
		batch = new ArrayListLong();
		batches.add(batch);
		n = 0;
	    }
	    batch.add(origin.getOrid());
	    n += ndef;
	}


	if (logger.isOutputOn() && logger.getVerbosity() > 0)
	    logger.write(String.format("%d Sources divided among %d batches with number of time defining phases < %d in each batch%n",
		    count, batches.size(), ndefMax));

	long check = 0;
	for (ArrayListLong b : batches)
	    check += b.size();

	if (check != count)
	    throw new GMPException(String.format("Sum of batch sizes (%d) != data size (%d)",
		    check, count));

	return batches;
    }

    @Override
    public LocOOTask readTaskObservations(ArrayListLong orids) throws Exception
    {		
	HashSet<OriginExtended> taskOriginSet = new HashSet<OriginExtended>();
	for (int i = 0; i < orids.size(); ++i)
	    taskOriginSet.add(originMap.get(orids.get(i)));

	LocOOTask task = new LocOOTask(properties, taskOriginSet);

	masterEventCorrections = new HashMap<String, double[]>();
	task.setMasterEventCorrections(masterEventCorrections);

	return task;
    }

    @Override
    public void close() throws IOException {
    }

}
