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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.ArrivalExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.Schema;

public class KBDBOutput extends KBOutput {

    private Schema outputSchema;
    
    private AtomicLong nextOrid;

    /**
     * The set of sites that are accumulated during execution and written to output in the close() method.
     */
    private Set<Site> sites;

    /**
     * The set of arrivals that are accumulated during execution and written to output in the close() method.
     */
    private Map<Long, ArrivalExtended> arrivals;

    public KBDBOutput() {
	super();
    }

    public KBDBOutput(PropertiesPlusGMP properties) throws Exception {
	this(properties, null);
    }

    public KBDBOutput(PropertiesPlusGMP properties, NativeInput dataInput) throws Exception {
	super(properties, dataInput);
	
	sites = new TreeSet<>();
	
	arrivals = new HashMap<>();
	
	this.outputSchema = new Schema("dbOutput", properties, true);

	if (outputSchema != null)
	{
	    nextOrid = getNextId();
	    
	    if (logger != null && logger.getVerbosity() > 0)
		logger.writeln(outputSchema.toString());
	}
    }

    @Override
    protected void writeData() throws Exception {
	if (outputSchema == null || outputOrigins.isEmpty())
	    return;

	if (!properties.getBoolean("dbOutputConstantOrid", properties.getBoolean("outputConstantOrid", false)))
	    for (OriginExtended o : outputOrigins.values())
		o.setOrid(nextOrid.getAndIncrement());
	
	// strip the arrivals and sites out of the origins because they can generate primary key violations
	// when this method is called multiple times with results from different LocOOTaskResults.
	// The arrivals and site are accumulated in separate collections and written to db in the 
	// close() method.
	for (OriginExtended origin : outputOrigins.values()) 
	    for (AssocExtended assoc : origin.getAssocs().values()) {
		ArrivalExtended arrival = assoc.getArrival();
		if (arrival != null) {
		    arrivals.put(arrival.getArid(), arrival);
		    if (arrival.getSite() != null)
			sites.add(assoc.getSite());
		    assoc.setArrival(null);
		}
	    }

	// write the origins to db, without any arrivals or sites which could generate 
	// primary key violations.
	OriginExtended.writeOriginExtendeds(outputOrigins.values(), outputSchema, true);
	
	// restore the arrivals and sites if they were stripped.
	for (OriginExtended origin : outputOrigins.values()) 
	    for (AssocExtended assoc : origin.getAssocs().values()) 
		assoc.setArrival(arrivals.get(assoc.getArid()));
	
	// clear the outputOrigins so that they will not be written out again when another
	// LocOOTaskResult is processed.
	outputOrigins.clear();
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
	String table = outputSchema.getTableName("origin");
	if (table == null)
	    return null;
	
	String sql = String.format("select max(orid) from %s", table);

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
	    logger.write(String.format("next orid = %d%n%n", nextId.get()));

	return nextId;
    }

    @Override
    public void close() throws Exception {
	Date lddate = new Date();
	if (outputSchema.getTableName("Arrival") != null && !arrivals.isEmpty()) 
	    Arrival.write(outputSchema.getConnection(), outputSchema.getTableName("Arrival"), arrivals.values(), lddate, true);
	
	if (outputSchema.getTableName("Site") != null && !sites.isEmpty()) 
	    Site.write(outputSchema.getConnection(), outputSchema.getTableName("Site"), sites, lddate, true);
	
	outputSchema.close();
    }
    
}
