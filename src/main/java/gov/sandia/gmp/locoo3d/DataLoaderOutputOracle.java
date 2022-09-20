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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicLong;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.DBTableTypes;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.Schema;

/**
 * <p>Title: LocOO</p>
 *
 * <p>Description: Seismic Event Locator</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Sandia National Laboratories</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
/***
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class DataLoaderOutputOracle extends DataLoaderOutput
{

    private PropertiesPlusGMP properties;
    private ScreenWriterOutput logger;
    //private ScreenWriterOutput errorlog;

    protected Schema outputSchema;

    private AtomicLong nextSourceId = null;

    public DataLoaderOutputOracle(PropertiesPlusGMP properties, 
	    ScreenWriterOutput logger, ScreenWriterOutput errorlog) throws Exception
    {
	this.properties = properties;
	this.logger = logger;
	//this.errorlog = errorlog;

	this.outputSchema = new Schema("dbOutput", properties, true);

	if (outputSchema != null)
	{
	    nextSourceId = getNextId(DBTableTypes.SOURCE);
	    if (nextSourceId == null)
		nextSourceId = getNextId(DBTableTypes.ORIGIN);
	    //nextPredictionId = iodb.getNextId(DBTableTypes.PREDICTION);
	    
	    
	    String originTable = outputSchema.getTableName("Origin");
	    String origerrTable = outputSchema.getTableName("Origerr");
	    String azgapTable = outputSchema.getTableName("Azgap");
	    String assocTable = outputSchema.getTableName("Assoc");
	    String arrivalTable = outputSchema.getTableName("Arrival");
	    String siteTable = outputSchema.getTableName("Site");
	    
	    logger.writeln("===== originTable  (Output) = "+originTable);
	    logger.writeln("===== origerrTable (Output) = "+origerrTable);
	    logger.writeln("===== azgapTable   (Output) = "+azgapTable);
	    logger.writeln("===== assocTable   (Output) = "+assocTable);
	    logger.writeln("===== arrivalTable (Output) = "+arrivalTable);
	    logger.writeln("===== siteTable    (Output) = "+siteTable);
	}
    }

    /**
     * 
     * @param schema
     * @param nextSourceId 
     * @param results
     * @throws Exception 
     */
    @Override
    public void writeTaskResult(LocOOTaskResult results) 
	    throws Exception
    {
	if (outputSchema == null || results.getResults().isEmpty())
	    return;

	if (!properties.getBoolean("dbOutputConstantOrid", false))
	    for (LocOOResult result : results.getResults())
		//if (result.isValid())
		result.setSourceId(nextSourceId.getAndIncrement());

	OriginExtended.writeOriginExtendeds(results.getOutputOrigins(), outputSchema, true);
    }

    /**
     * Retrieve the next unused Id from the specified database table.  If the schema does 
     * not have have a table of the specified type, or if the specified table type is not
     * an idowner table, returns null.
     * @param tableType must be one of origin, source, prediction
     * @return next id
     * @throws SQLException 
     */
    public AtomicLong getNextId(DBTableTypes tableType) throws SQLException 
    {
	String table = outputSchema.getTableName(tableType.toString());
	if (table == null)
	    return null;

	String id;
	switch (tableType)
	{
	case ORIGIN:
	    id = "orid";
	    break;
	case PREDICTION:
	    id = "predictionid";
	    break;
	case SOURCE:
	    id = "sourceid";
	    break;
	default:
	    return null;
	}

	Statement statement = outputSchema.getConnection().createStatement();
	if (logger.getVerbosity() > 0)
	    logger.write("Executing query "+String.format("select max(%s) from %s%n", id, table));
	ResultSet result = statement.executeQuery(String.format("select max(%s) from %s", id, table));
	AtomicLong nextId = null;
	if (result.next())
	    nextId = new AtomicLong(result.getLong(1));
	else
	    nextId = new AtomicLong();

	nextId.incrementAndGet();

	result.close();
	statement.close();

	if (logger.getVerbosity() > 0)
	    logger.write(String.format("next %s = %d%n%n", id, nextId.get()));

	return nextId;
    }

    @Override
    public void close() throws Exception {
    }

}
