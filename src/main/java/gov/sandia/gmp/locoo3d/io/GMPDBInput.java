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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.locoo3d.LocOOTask;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.Schema;

public class GMPDBInput extends GMPInput {

    private Schema inputSchema;

    public GMPDBInput() {
    }

    public GMPDBInput(PropertiesPlusGMP properties) throws Exception {
	super(properties);
	this.inputSchema = new Schema("dbInput", properties, false);
	if (logger.getVerbosity() > 0) logger.writeln("\n"+inputSchema.toString());
    }

    @Override
    public LocOOTask getLocOOTask(ArrayListLong sourceIds) throws Exception {
	return new LocOOTask(taskProperties, 
		readSources(inputSchema, taskProperties.getProperty("dbInputSrcobsassocWhereClause", ""), sourceIds).values());
    }

    @Override
    public ArrayList<ArrayListLong> readTaskSourceIds() throws Exception {
	int ndefMax = Math.min(1000, taskProperties.getInt("batchSizeNdef", 1000));

	long timer = System.currentTimeMillis();

	String whereClause = taskProperties.getProperty("dbInputWhereClause");

	if (whereClause == null)
	    throw new PropertiesPlusException("Property dbInputWhereClause is not specified.");

	whereClause = whereClause.toLowerCase().startsWith("where ") ? whereClause : "where "+whereClause;

	// query will be something like: 
	// select orid, ndef from origin_table where <whereClause> order by ndef descending

	String sql;

	sql = String.format("select sourceid, numassoc from %s %s order by numassoc desc",
		inputSchema.getTableName("source"), whereClause);

	if (logger.isOutputOn() && logger.getVerbosity() > 0)
	    logger.write(String.format("Executing sql:%n%s%n",sql.toString()));

	Statement statement = inputSchema.getConnection().createStatement();
	ResultSet resultSet = statement.executeQuery(sql);

	int count=0;
	ArrayList<ArrayListLong> batches = new ArrayList<ArrayListLong>();
	{
	    ArrayListLong batch = new ArrayListLong();
	    batches.add(batch);
	    long n=0, sourceId, ndef;
	    while (resultSet.next())
	    {
		++count;
		sourceId = resultSet.getLong(1);
		ndef = resultSet.getLong(2);
		if (ndef <= 0)
		    ndef = 10;
		if (batch.size() > 0 && n+ndef > ndefMax)
		{
		    batch = new ArrayListLong();
		    batches.add(batch);
		    n = 0;
		}
		batch.add(sourceId);
		n += ndef;
	    }
	}
	resultSet.close();
	statement.close();

	if (logger.isOutputOn() && logger.getVerbosity() > 0)
	    logger.write(String.format("Query returned %d records in %s%n", 
		    count, Globals.elapsedTime(timer)));


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

    private Map<Long, Source> readSources(Schema inputSchema, String srcobsassocWhereClause, ArrayListLong sourceIds) throws Exception {
	StringBuffer sourceidList = new StringBuffer();
	sourceidList.append(Long.toString(sourceIds.get(0)));
	for (int i=1; i<sourceIds.size(); ++i)
	    sourceidList.append(',').append(sourceIds.get(i));

	// build assocWhereClause that is either empty, or looks like 'and ...'
	if (srcobsassocWhereClause.length() > 0)
	{
	    if (!srcobsassocWhereClause.toLowerCase().startsWith("and "))
		srcobsassocWhereClause = "and "+srcobsassocWhereClause;
	    srcobsassocWhereClause = " "+srcobsassocWhereClause;
	}

	long timer = System.currentTimeMillis();

	// map containing baseobjects.Sources
	Map<Long, Source> sources = new TreeMap<>();

	String sourceQuery = String.format("select sourceid, eventid, lat, lon, depth, "
		+ "gnem_idcstatic.ts2epoch(origintime), "
		+ "gtlevel, numassoc, polygonid, auth "
		+ "from %s where sourceid in (%s)",
		inputSchema.getTableName("source"), sourceidList);

	if (logger.getVerbosity() > 0)
	    logger.write(String.format("Executing sql:%n%s%n",sourceQuery));

	Statement statement = inputSchema.getConnection().createStatement();
	ResultSet resultSet = statement.executeQuery(sourceQuery);

	while (resultSet.next())
	{
	    int columnIndex = 0;
	    // build a baseobjects.Source
	    Source source = new Source(
		    resultSet.getLong(++columnIndex), // sourceid
		    resultSet.getLong(++columnIndex),  // eventid
		    new GeoVector(
			    resultSet.getDouble(++columnIndex), // lat
			    resultSet.getDouble(++columnIndex), // lon
			    resultSet.getDouble(++columnIndex), // depth
			    true), // in degrees
		    resultSet.getDouble(++columnIndex), // origin time  
		    resultSet.getDouble(++columnIndex)); // gt level  

	    long ndef = resultSet.getLong(++columnIndex);
	    long polygonId = resultSet.getLong(++columnIndex);
	    String auth = resultSet.getString(++columnIndex);

	    source.setGTTime(false);
	    source.setAuthor(auth);

	    sources.put(source.getSourceId(), source);

	    if (inputSources != null)
		// build a dbtabledefs.Source
		inputSources.put(source.getSourceId(), new gov.sandia.gnem.dbtabledefs.gmp.Source(
			source.getSourceId(), 
			source.getEvid(), source.getLatDegrees(), source.getLonDegrees(), 
			source.getDepth(), source.getTime(), source.getGTLevel(), ndef, polygonId, auth));
	}
	resultSet.close();

	if (logger.getVerbosity() > 0)
	    logger.write(String.format("Query returned %d records in %s%n", 
		    sources.size(), GMPGlobals.ellapsedTime(timer)));


	// now observations and receivers.

	long receiverId;
	int ondate, offdate;
	String sta, phase, iphase, auth;
	Receiver receiver;
	int nObs = 0, nResults=0;
	Long sourceId, observationId;
	double time, deltim, az, delaz, slow, delslo, rlat, rlon, relev;
	double delta, esaz, seaz;
	String timedef, azdef, slodef;

	StringBuffer obsQuery = new StringBuffer("select ");
	obsQuery.append("srcobsassoc.sourceid, ");
	obsQuery.append("srcobsassoc.observationid, ");	
	obsQuery.append("observation.receiverid, ");

	obsQuery.append("srcobsassoc.phase, ");
	obsQuery.append("observation.iphase, ");	

	obsQuery.append("srcobsassoc.delta, ");
	obsQuery.append("srcobsassoc.esaz, ");
	obsQuery.append("srcobsassoc.seaz, ");

	obsQuery.append("gnem_idcstatic.ts2epoch(observation.arrivaltime), ");
	obsQuery.append("observation.timeuncertainty, ");
	obsQuery.append("srcobsassoc.timedef, ");
	obsQuery.append("observation.azimuth, ");
	obsQuery.append("observation.azuncertainty, ");
	obsQuery.append("srcobsassoc.azdef, ");
	obsQuery.append("observation.slowness, ");
	obsQuery.append("observation.slowuncertainty, ");
	obsQuery.append("srcobsassoc.slowdef, ");
	obsQuery.append("observation.auth, ");

	obsQuery.append("receiver.sta, ");
	obsQuery.append("receiver.lat, ");
	obsQuery.append("receiver.lon, ");
	obsQuery.append("receiver.elevation, ");
	obsQuery.append("gnem_idcstatic.ts2jdate(receiver.starttime), ");
	obsQuery.append("gnem_idcstatic.ts2jdate(receiver.endtime) ");

	obsQuery.append("from ");
	obsQuery.append(inputSchema.getTableName("srcobsassoc")).append(" srcobsassoc, ");
	obsQuery.append(inputSchema.getTableName("observation")).append(" observation, ");
	obsQuery.append(inputSchema.getTableName("receiver")).append(" receiver ");
	obsQuery.append(String.format("where sourceid in (%s) ", sourceidList));
	obsQuery.append("and srcobsassoc.observationid=observation.observationid ");
	obsQuery.append("and observation.receiverid=receiver.receiverid");
	obsQuery.append(srcobsassocWhereClause);

	if (logger.getVerbosity() > 0)
	    logger.write(String.format("Executing sql:%n%s%n",obsQuery.toString()));

	resultSet = statement.executeQuery(obsQuery.toString());

	while (resultSet.next())
	{
	    ++nResults;
	    int col = 0;
	    sourceId = resultSet.getLong(++col);
	    observationId = resultSet.getLong(++col);
	    receiverId = resultSet.getLong(++col);

	    phase = resultSet.getString(++col);
	    iphase = resultSet.getString(++col);

	    delta = resultSet.getDouble(++col);
	    esaz = resultSet.getDouble(++col);
	    seaz = resultSet.getDouble(++col);

	    time = resultSet.getDouble(++col);
	    deltim = resultSet.getDouble(++col);
	    timedef = resultSet.getString(++col);
	    az = resultSet.getDouble(++col);
	    delaz = resultSet.getDouble(++col);
	    azdef =  resultSet.getString(++col);
	    slow = resultSet.getDouble(++col);
	    delslo = resultSet.getDouble(++col);
	    slodef =  resultSet.getString(++col);
	    auth = resultSet.getString(++col);

	    sta = resultSet.getString(++col);
	    rlat = resultSet.getDouble(++col);
	    rlon = resultSet.getDouble(++col);
	    relev = resultSet.getDouble(++col);
	    ondate = resultSet.getInt(++col);
	    offdate = resultSet.getInt(++col);

	    // retrieve a baseobjects.Receiver
	    receiver = receivers.get(receiverId);
	    if (receiver == null)
		receivers.put(receiverId, receiver = 
		new Receiver(receiverId, sta, rlat, rlon, relev, ondate, offdate, true));		

	    // construct a baseobjects.Observation
	    Observation observation = new Observation(observationId, receiver, sources.get(sourceId), 
		    SeismicPhase.valueOf(phase), time, deltim, timedef, 
		    az, delaz, azdef, slow, delslo,slodef, true);

	    // add the baseobjects.Observation to the appropriate baseobjects.Source
	    sources.get(sourceId).getObservations().put(observationId, observation);


	    if (inputSources != null && srcobsassocsRequested) {

		gov.sandia.gnem.dbtabledefs.gmp.Srcobsassoc srcobsassoc = new gov.sandia.gnem.dbtabledefs.gmp.Srcobsassoc(
			sourceId, observationId, phase, delta, esaz, seaz, timedef, azdef, slodef, auth);

		inputSources.get(sourceId).getSrcobsassocs().put(observationId, srcobsassoc);

		if (observationsRequested) {

		    gov.sandia.gnem.dbtabledefs.gmp.Observation obs = new gov.sandia.gnem.dbtabledefs.gmp.Observation(
			    observationId, receiverId, iphase, time, deltim, az, delaz, slow, delslo, auth);

		    srcobsassoc.setObservation(obs);

		    gov.sandia.gnem.dbtabledefs.gmp.Receiver rcvr = new gov.sandia.gnem.dbtabledefs.gmp.Receiver(
			    receiverId, sta, rlat, rlon, relev, GMTFormat.getEpochTime(ondate), GMTFormat.getEpochTime(offdate), 
			    -1, auth); 

		    obs.setReceiver(rcvr);
		}
	    }
	}

	resultSet.close();
	statement.close();

	if (logger.getVerbosity() > 0) {
	    logger.write(String.format("Query returned %d records in %s%n", 
		    nResults, Globals.elapsedTime(timer)));

	    logger.write(String.format("Discovered %d sources, %d receivers and %d observations.%n", 
		    sources.size(), receivers.size(), nObs));
	}

	return sources;
    }

    @Override
    public void close() throws Exception {
	inputSchema.close();
    }

}
