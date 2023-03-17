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
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gnem.dbtabledefs.gmp.Observation;
import gov.sandia.gnem.dbtabledefs.gmp.Receiver;
import gov.sandia.gnem.dbtabledefs.gmp.Source;
import gov.sandia.gnem.dbtabledefs.gmp.Srcobsassoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.Schema;

public class GMPDBOutput extends GMPOutput {

    private Schema outputSchema;

    private AtomicLong nextOrid;

    public GMPDBOutput() {
	super();
    }

    public GMPDBOutput(PropertiesPlusGMP properties) throws Exception {
	this(properties, null);
    }

    public GMPDBOutput(PropertiesPlusGMP properties, NativeInput dataInput) throws Exception {
	super(properties, dataInput);

	this.outputSchema = new Schema("dbOutput", properties, true);

	if (outputSchema != null)
	{
	    nextOrid = getNextId();

	    if (logger != null && logger.getVerbosity() > 0)
		logger.writeln(outputSchema.toString());
	}
    }

    @Override
    void writeData() throws Exception {	
	if (outputSchema == null || outputSources.isEmpty())
	    return;

	if (!properties.getBoolean("dbOutputConstantOrid", false))
	    for (Source o : outputSources.values())
		o.setSourceid(nextOrid.getAndIncrement());

	Date now = new Date();

	Source.write(outputSchema.getConnection(), outputSchema.getTableName("source"), outputSources.values(), 
		now, true);

	String obsTable = outputSchema.getTableName("observation");
	String rcvrTable = outputSchema.getTableName("receiver");

	if (outputSchema.getTableName("srcobsassoc") != null) {
	    Set<Srcobsassoc> soa = new LinkedHashSet<>();
	    Map<Long, Observation> observations = new TreeMap<>();
	    Map<Long, Receiver> receivers = new TreeMap<>();

	    for (Source o : outputSources.values()) {
		soa.addAll(o.getSrcobsassocs().values());
		if (obsTable != null)
		    for (Srcobsassoc s : soa) {
			if (s.getObservation() != null) {
			    observations.put(s.getObservationid(), s.getObservation());
			    if (rcvrTable != null)
				if (s.getObservation().getReceiver() != null)
				    receivers.put(s.getObservation().getReceiverid(), s.getObservation().getReceiver());
			}
		    }
	    }

	    Srcobsassoc.write(outputSchema.getConnection(), outputSchema.getTableName("srcobsassoc"), soa, now, true);

	    if (!observations.isEmpty())
		Observation.write(outputSchema.getConnection(), outputSchema.getTableName("observation"), observations.values(), now, true);

	    if (!receivers.isEmpty())
		Receiver.write(outputSchema.getConnection(), outputSchema.getTableName("receiver"), receivers.values(), now, true);
	}
    }

    /**
     * Retrieve the next unused Id from the specified database table.  If the schema does 
     * not have have a table of the specified type, or if the specified table type is not
     * an idowner table, returns null.
     * @param tableType must be one of origin, source, prediction
     * @return next id
     * @throws SQLException 
     */
    public AtomicLong getNextId() throws SQLException 
    {
	String table = outputSchema.getTableName("source");
	if (table == null)
	    return null;

	String sql = String.format("select max(sourceid) from %s", table);

	Statement statement = outputSchema.getConnection().createStatement();
	if (logger.getVerbosity() > 0)
	    logger.write("Executing query "+sql);
	ResultSet result = statement.executeQuery(sql);
	AtomicLong nextId = null;
	if (result.next())
	    nextId = new AtomicLong(result.getLong(1));
	else
	    nextId = new AtomicLong();

	nextId.incrementAndGet();

	result.close();
	statement.close();

	if (logger.getVerbosity() > 0)
	    logger.write(String.format("next sourceid = %d%n%n", nextId.get()));

	return nextId;
    }

    @Override
    public void close() throws Exception {
	outputSchema.close();
    }

}
