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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.locoo3d.LocOOTask;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.NetworkExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.Schema;

public class KBDBInput extends KBInput {

    private Schema inputSchema;
    private NetworkExtended network;

    public KBDBInput() {
    }

    public KBDBInput(PropertiesPlusGMP properties) throws Exception {
	super(properties);

	inputSchema = new Schema("dbInput", properties, false);

	if (logger.getVerbosity() > 0) logger.writeln("\n"+inputSchema.toString());

	String masterEventWhereClause = properties.getProperty("masterEventWhereClause");

	if (masterEventWhereClause != null)
	{
	    String masterSchemaPrefix = properties.getProperty("masterEventSchema", "dbInput");

	    Schema masterSchema = new Schema(masterSchemaPrefix, properties, false);
	    ArrayList<String> executedSql = new ArrayList<String>();

	    Map<Long, OriginExtended> masterEvents = OriginExtended.readOriginExtended(masterSchema, null, 
		    masterEventWhereClause, properties.getProperty("masterAssocWhereClause", ""), executedSql);

	    if (masterEvents.size() == 1)
	    {
		OriginExtended masterEvent = masterEvents.values().iterator().next();

		// Create the predictors, using the PredictorFactory
		PredictorFactory predictors = new PredictorFactory(properties,"loc_predictor_type", logger);

		masterEventCorrections = getMasterEventCorrections(new Source(masterEvent),
			predictors, "masterEventSchema:\n"+masterSchema.toString());

	    }
	    else
	    {
		StringBuffer error = new StringBuffer();
		error.append(String.format("The following sql failed to properly load the master origin "
			+ "(%d origins loaded but 1 is allowed):\n", masterEvents.size()));
		for (String s : executedSql)
		    error.append(s+"\n");
		throw new Exception(error.toString());
	    }			
	    masterSchema.close();
	}

	try
	{
	    String originWhereClause = properties.getProperty("dbInputWhereClause");
	    String originSql = originWhereClause.startsWith("where ") 
		    ? String.format("select orid from %s %s", inputSchema.getTableName("origin"), originWhereClause)
			    : String.format("select orid from %s where %s", inputSchema.getTableName("origin"), originWhereClause);

	    String assocWhereClause = properties.getProperty("dbInputAssocClause", "");
	    if (assocWhereClause.length() > 0
		    && !assocWhereClause.toLowerCase().startsWith("and "))
		assocWhereClause = "and " + assocWhereClause;

	    String assocSql = String.format("select sta from %s where orid in (%s) %s",
		    inputSchema.getTableName("assoc"), originSql, assocWhereClause);

	    String siteSql = String.format("select * from %s where sta in (%s)", inputSchema.getTableName("site"), assocSql);

	    if (logger.getVerbosity() > 0)
		logger.writeln("Loading site information using "+siteSql);

	    long timer = System.currentTimeMillis();

	    network = new NetworkExtended(inputSchema.getConnection(), siteSql);

	    timer = System.currentTimeMillis()-timer;

	    if (logger.getVerbosity() > 0)
		logger.writeln(String.format("Loaded %d sites in %d msec", network.getSites().size(), timer));
	}
	catch(Exception ex)
	{
	    System.err.println("Exception thrown while trying to load site information from the inputSchema.");
	    ex.printStackTrace();
	}

    }

    /**
     * Query the database to retrieve batches of orid|sourceid such that each batch has
     * less than some number of time defining phases.  Returns a 2D ragged array of longs.
     * The first index spans the batches and the second spans the orids|sourceids in each
     * batch.
     * <p>The maximum number of time defining phases in a batch is retrieve from the 
     * input property file with property batchSizeNdef, which defaults to 100.  Then
     * the following sql statement is executed 
     * <p>select orid, ndef from origin_table where <whereClause> order by ndef descending
     * <p>The returned information is used to split the orids up into batches, as described above.
     */
    @Override
    public ArrayList<ArrayListLong> readTaskSourceIds() throws SQLException, GMPException

    {
	int ndefMax = Math.min(1000, properties.getInt("batchSizeNdef", 1000));

	String whereClause = properties.getProperty("dbInputWhereClause");

	if (whereClause == null)
	    throw new PropertiesPlusException("Property dbInputWhereClause is not specified.");

	whereClause = whereClause.toLowerCase().startsWith("where ") ? whereClause : "where "+whereClause;

	// query will be something like: 
	// select orid, ndef from origin_table where <whereClause> order by ndef descending

	String sql;

	if (inputSchema.getTableName("source") != null) 
	    sql = String.format("select source, numassoc from %s %s order by numassoc desc",
		    inputSchema.getTableName("source"), whereClause);

	else sql = String.format("select orid, ndef from %s %s order by ndef desc",
		inputSchema.getTableName("origin"), whereClause);

	if (logger.isOutputOn() && logger.getVerbosity() > 0)
	    logger.write(String.format("Executing sql:%n%s%n",sql.toString()));

	long timer = System.nanoTime();

	Statement statement = inputSchema.getConnection().createStatement();
	ResultSet resultSet = statement.executeQuery(sql);

	timer = System.nanoTime()-timer;

	int count=0;
	ArrayList<ArrayListLong> batches = new ArrayList<ArrayListLong>();
	{
	    ArrayListLong batch = new ArrayListLong();
	    batches.add(batch);
	    long n=0, orid, ndef;
	    while (resultSet.next())
	    {
		++count;
		orid = resultSet.getLong(1);
		ndef = resultSet.getLong(2);
		if (ndef <= 0)
		    ndef = 10;
		if (batch.size() > 0 && n+ndef > ndefMax)
		{
		    batch = new ArrayListLong();
		    batches.add(batch);
		    n = 0;
		}
		batch.add(orid);
		n += ndef;
	    }
	}
	resultSet.close();
	statement.close();

	if (logger.isOutputOn() && logger.getVerbosity() > 0)
	    logger.write(String.format("Query returned %d records in %s%n", 
		    count, GMPGlobals.ellapsedTime(timer*1e-9)));


	if (logger.isOutputOn() && logger.getVerbosity() > 0)
	    logger.write(String.format("%d Sources divided among %d batches with number of time defining phases < %d in each batch%n",
		    count, batches.size(), ndefMax));

	long check = 0;
	for (ArrayListLong batch : batches)
	    check += batch.size();

	if (check != count)
	    throw new GMPException(String.format("Sum of batch sizes (%d) != data size (%d)",
		    check, count));

	return batches;
    }

    /**
     * Given a list of orids, retrieve a LocOOTask that includes those 
     * sources.  The data includes the sources as well as the associated observations
     * and receivers.
     * @param orids
     * @return
     * @throws Exception
     */
    @Override
    public LocOOTask readTaskObservations(ArrayListLong orids) 
	    throws Exception 
    {
	StringBuffer oridList = new StringBuffer();
	oridList.append(Long.toString(orids.get(0)));
	for (int i=1; i<orids.size(); ++i)
	    oridList.append(',').append(orids.get(i));

	// build assocWhereClause that is either empty, or looks like 'and ...'
	String assocWhereClause = properties.getProperty("dbInputAssocClause", "");
	if (assocWhereClause.length() > 0)
	{
	    if (!assocWhereClause.toLowerCase().startsWith("and "))
		assocWhereClause = "and "+assocWhereClause;
	    assocWhereClause = " "+assocWhereClause;
	}

	ArrayList<String> executedSql = new ArrayList<String>();

	inputOrigins = OriginExtended.readOriginExtended(inputSchema, network,
		String.format("orid in (%s)", oridList), assocWhereClause, executedSql);
	
	if (logger.isOutputOn() && logger.getVerbosity() > 0)
	    for (String sql : executedSql) logger.writeln(sql);

	return new LocOOTask(properties, convertOriginsToSources(
		properties, inputOrigins.values(), masterEventCorrections), masterEventCorrections);
    }

    /**
     * Close the database connection.
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
	inputSchema.close();
    }

}
