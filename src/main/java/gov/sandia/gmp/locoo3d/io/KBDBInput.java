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

				setMasterEventCorrections(new Source(masterEvent), "masterEventSchema:\n"+masterSchema.toString());

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

			String assocSql = String.format("select unique sta from %s where orid in (%s) %s",
					inputSchema.getTableName("assoc"), originSql, assocWhereClause);

			String siteSql = String.format("select * from %s where sta in (%s)", inputSchema.getTableName("site"), assocSql);

			if (logger.getVerbosity() > 0)
				logger.writeln("Loading site information using "+siteSql);

			long timer = System.currentTimeMillis();

			network = new NetworkExtended();

			timer = System.currentTimeMillis()-timer;
		}
		catch(Exception ex)
		{
			System.err.println("Exception thrown while trying to load site information from the inputSchema.");
			ex.printStackTrace();
		}

	}

	@Override
    public Map<Long, OriginExtended> getInputOrigins() throws Exception {
		String originWhereClause = getProperties().getProperty("dbInputWhereClause");
		String assocWhereClause = getProperties().getProperty("dbInputAssocClause", "");
		if (assocWhereClause.length() > 0
				&& !assocWhereClause.toLowerCase().startsWith("and "))
			assocWhereClause = "and " + assocWhereClause;

		ArrayList<String> executedSql = new ArrayList<String>();

		NetworkExtended net = new NetworkExtended();
		inputOrigins = OriginExtended.readOriginExtended(inputSchema, net,
				originWhereClause, assocWhereClause, executedSql);
		
		return inputOrigins;
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
		String whereClause = taskProperties.getProperty("dbInputWhereClause");

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

		ArrayList<long[]> results = new ArrayList<>();
		long orid, ndef, totalndef=0;
		while (resultSet.next()) {
			orid = resultSet.getLong(1);
			ndef = resultSet.getLong(2);
			if (ndef <= 0) ndef = 10L;
			long[] l = new long[] {orid, ndef};
			results.add(l);
			totalndef += ndef;
		}
		resultSet.close();
		statement.close();

		if (logger.isOutputOn() && logger.getVerbosity() > 0)
			logger.write(String.format("Query returned %d records in %s%n", 
					results.size(), GMPGlobals.ellapsedTime(timer*1e-9)));

		// determine max number of defining observations in each batch.
		// The default is total number of defining observations in all
		// origins, divided by the number of available processors, divided by 2.
		// But no more than 1000
		long batchSizeNdef = Math.min(1000L, totalndef / (2*taskProperties.getInt("maxProcessors", Runtime.getRuntime().availableProcessors()))+1);

		// if value is specified in properties object, use that value instead.
		batchSizeNdef = taskProperties.getInt("batchSizeNdef", batchSizeNdef);

		ArrayList<ArrayListLong> batches = new ArrayList<ArrayListLong>();
		ArrayListLong batch = new ArrayListLong();
		batches.add(batch);
		long n=0;
		for (long[] result : results)
		{
			orid = result[0];
			ndef = result[1];
			if (batch.size() > 0 && n+ndef > batchSizeNdef)
			{
				batch = new ArrayListLong();
				batches.add(batch);
				n = 0;
			}
			batch.add(orid);
			n += ndef;
		}

		if (logger.isOutputOn() && logger.getVerbosity() > 0)
			logger.write(String.format("Total number of time defining arrivals in all origins is %d.%n"
					+ "%d origins divided among %d batches with number of time defining arrivals < %d in each batch%n",
					totalndef, results.size(), batches.size(), batchSizeNdef));

		long check = 0;
		for (ArrayListLong bat : batches)
			check += bat.size();

		if (check != results.size())
			throw new GMPException(String.format("Sum of batch sizes (%d) != data size (%d)",
					check, results.size()));

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
	public LocOOTask getLocOOTask(ArrayListLong orids) 
			throws Exception 
	{
		StringBuffer oridList = new StringBuffer();
		oridList.append(Long.toString(orids.get(0)));
		for (int i=1; i<orids.size(); ++i)
			oridList.append(',').append(orids.get(i));

		// build assocWhereClause that is either empty, or looks like 'and ...'
		String assocWhereClause = taskProperties.getProperty("dbInputAssocClause", "");
		if (assocWhereClause.length() > 0)
		{
			if (!assocWhereClause.toLowerCase().startsWith("and "))
				assocWhereClause = "and "+assocWhereClause;
			assocWhereClause = " "+assocWhereClause;
		}

		ArrayList<String> executedSql = new ArrayList<String>();

		NetworkExtended net = new NetworkExtended();
		inputOrigins = OriginExtended.readOriginExtended(inputSchema, net,
				String.format("orid in (%s)", oridList), assocWhereClause, executedSql);
		network.addAll(net.getSites());

		if (logger.isOutputOn() && logger.getVerbosity() > 0)
			for (String sql : executedSql) logger.writeln(sql);

		return new LocOOTask(taskProperties, convertOriginsToSources(
				taskProperties, inputOrigins.values(), masterEventCorrections), masterEventCorrections);
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
