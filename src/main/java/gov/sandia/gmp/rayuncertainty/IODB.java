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
package gov.sandia.gmp.rayuncertainty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Row;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.Table;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * <p>
 * IODB
 * </p>
 *
 * <p>
 * Class to perform IO operations to/from a database.
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 *
 * <p>
 * Company:
 * </p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public class IODB {
    /**
     * Default values extracted from user's environment.
     */
    protected String defaultUserName = System.getenv("DBTOOLS_USERNAME");
    protected String defaultPassword = System.getenv("DBTOOLS_PASSWORD");
    protected String defaultDriver = System.getenv("DBTOOLS_DRIVER");
    protected String defaultInstance = System.getenv("DBTOOLS_INSTANCE");
    protected String defaultTableDef = System.getenv("DBTOOLS_TABLEDEF");


    public enum DBTableTypes {
        ALGORITHM("Algorithm"),
        ARRIVAL("Arrival"),
        ASSOC("Assoc"),
        AZGAP("Azgap"),
        CELL("Cell"),
        DETECTION("Detection"),
        EVENTTERM("EventTerm"),
        GEOMODEL("GeoModel"),
        GT_TIME("GT_Time"),
        GT_EPI("GT_Epi"),
        NODE("Node"),
        OBSERVATION("Observation"),
        OBSSEQ("ObsSeq"),
        ORIGIN("Origin"),
        ORIGERR("Origerr"),
        POLYGON("Polygon"),
        PREDICTION("Prediction"),
        PREDICTSIM("PredictSim"),
        RECEIVER("Receiver"),
        RECEIVERCELLASSOC("ReceiverCellAssoc"),
        REPPREDICT("RepPredict"),
        SITE("Site"),
        SITETERM("SiteTerm"),
        SOURCE("Source"),
        SOURCECELLASSOC("SouceCellAssoc"),
        SRCOBSASSOC("SrcObsAssoc"),
        TOMOMODASSOC("TomoModAssoc"),
        TOMOMODOBSASSOC("TomoModObsAssoc"),
        TOMORUN("TomoRun"),
        TOMOSTATS("TomoStats");

        private String mixedCase;

        DBTableTypes(String mixedCase) {
            this.mixedCase = mixedCase;
        }

        /**
         * Retrieve the String for the associated TableTypes
         *
         * @return String
         */
        public String toMixedCase() {
            return mixedCase;
        }

    }

    protected PropertiesPlusGMP properties;

    protected ScreenWriterOutput logger, errorlog;

    /**
     * @param logger
     * @param errorlog
     */
    public IODB(PropertiesPlusGMP properties, ScreenWriterOutput logger, ScreenWriterOutput errorlog) {
        this.properties = properties;
        this.logger = logger;
        this.errorlog = errorlog;
    }

    /**
     * Set the logger and errorlog.
     *
     * @param scrnWrtr
     * @param scrnWrtr2
     */
    protected void setLoggers(ScreenWriterOutput logger,
                              ScreenWriterOutput errorlog) {
        this.logger = logger;
        this.errorlog = errorlog;
    }

    /**
     * Retrieve a DBUtilLib.Schema object.  The information needed to
     * construct the Schema is extracted from the specified PropertiesPlus
     * object using parameters that have the specified prefix.  For example,
     * if prefix = 'dbInput' then parameters 'dbInputUserName', 'dbInputPassword',
     * etc. will be used to build the schema.
     * <p>Relevant parameters, with default values following = sign, comments in ():
     * <BR>prefixUserName = (specify database account, ie. 'jblow')
     * <BR>prefixPassword = UserName (can omit this if password == username)
     * <BR>prefixTableDefinitionTable = <can try GMP2009.NNSA_TABLE_DEFS_VIEW>
     * <BR>
     * <BR>prefixTablePrefix = (eg. locdb_)
     * <BR>prefixTableTypes =  (eg. source|origin, observation|arrival, etc.)
     * <BR>
     * <BR>prefixInstance = jdbc:oracle:thin:@bikinifire:1522:kbdb (rarely need to change this)
     * <BR>prefixDriver = oracle.jdbc.driver.OracleDriver (rarely need to change this)
     * <p>if isOutput is true then the following parameters are checked.
     * <BR>prefixAutoTableCreation = false (false is safe, but true is convenient)
     * <BR>prefixTruncateTables = false (careful!  if true, all the specified tables will be truncated!)
     * <BR>prefixPromptBeforeTruncate = true  (true is safe, but false is convenient)
     *
     * @param prefix
     * @return new Schema object
     * @throws FatalDBUtilLibException
     * @throws GMPException
     * @throws IOException
     */
    public Schema getSchema(String prefix, boolean isOutput)
            throws FatalDBUtilLibException, GMPException, IOException {
        ParInfo parInfo = new ParInfo();

        String dbUserName = properties.getProperty(prefix + "UserName", defaultUserName);

        if (dbUserName == null)
            throw new GMPException(String.format(
                    "Cannot create %s schema because property %sUserName is not specified in the property file.",
                    isOutput ? "output" : "input", prefix));

        String dbPassword = properties.getProperty(prefix + "Password",
                defaultPassword != null ? defaultPassword : dbUserName);

        String dbTableDefinitionTable = properties.getProperty(prefix + "TableDefinitionTable",
                defaultTableDef);

        String dbInstance = properties.getProperty(prefix + "Instance",
                defaultInstance != null ? defaultInstance
                        : "jdbc:oracle:thin:@fignewton:1521:dbgmp");

        String dbDriver = properties.getProperty(prefix + "Driver",
                defaultDriver != null ? defaultDriver
                        : "oracle.jdbc.driver.OracleDriver");

//		if (logger.isOutputOn() && logger.getVerbosity() > 0)
//		{
//			logger.write(String.format("%nConstructing %s database schema%n", isOutput ? "output" : "input"));
//			logger.write(String.format(prefix+"UserName = %s (source = %s)%n", dbUserName,
//					dbUserName == defaultUserName ? "environment" : "property file"));
//			logger.write(String.format(prefix+"Password = * (source = %s)%n",
//					dbPassword == defaultPassword ? "environment" : "property file"));
//			logger.write(String.format(prefix+"TableDefinitionTable = %s (source = %s)%n", dbTableDefinitionTable,
//					dbTableDefinitionTable == defaultTableDef ? "environment" : "property file"));
//			logger.write(String.format(prefix+"Instance = %s (source = %s)%n", dbInstance,
//					dbInstance == defaultInstance ? "environment" : "property file"));
//			logger.write(String.format(prefix+"Driver = %s (source = %s)%n%n", dbDriver,
//					dbDriver == defaultDriver ? "environment" : "property file"));
//		}

        ArrayList<String> tabletypesToTruncate = new ArrayList<String>();

        parInfo.addParameter("Username", dbUserName);
        parInfo.addParameter("Password", dbPassword);
        parInfo.addParameter("TableDefinitionTable", dbTableDefinitionTable);
        parInfo.addParameter("Instance", dbInstance);
        parInfo.addParameter("Driver", dbDriver);

        // username for table definition tables defaults to the same account as data account,
        // but can be overridden with either prefixTableDefinitionTableUserName OR
        // prefixTableDefinitionTableUsername  (upper or lower case N).
        String prop = properties.getProperty(prefix + "TableDefinitionTableUserName", dbUserName);
        prop = properties.getProperty(prefix + "TableDefinitionTableUsername", prop);
        parInfo.addParameter("TableDefinitionTableUsername", prop);

        parInfo.addParameter("TableDefinitionTablePassword",
                properties.getProperty(prefix + "TableDefinitionTablePassword", dbPassword));
        parInfo.addParameter("TableDefinitionTableInstance",
                properties.getProperty(prefix + "TableDefinitionTableInstance", dbInstance));
        parInfo.addParameter("TableDefinitionTableDriver",
                properties.getProperty(prefix + "TableDefinitionTableDriver", dbDriver));


        if (isOutput) {
            parInfo.addParameter("AutoTableCreation", properties.getProperty(
                    prefix + "AutoTableCreation", "false"));
            parInfo.addParameter("PromptBeforeTruncate", properties.getProperty(
                    prefix + "PromptBeforeTruncate", "true"));

            String dbTruncateTables = properties.getProperty(
                    prefix + "TruncateTables", "false");

            if (dbTruncateTables.trim().equalsIgnoreCase("true")
                    || dbTruncateTables.trim().equalsIgnoreCase("false"))
                parInfo.addParameter("TruncateTables", dbTruncateTables);
            else {
                // don't truncate all the tables, parse the list into
                // a list of the table types that should be truncated.
                parInfo.addParameter("TruncateTables", "false");
                for (String tabletype : dbTruncateTables.replaceAll(",", " ").split(" "))
                    if (tabletype.trim().length() > 0)
                        tabletypesToTruncate.add(tabletype.trim());
            }
        }

        Schema schema = new Schema(parInfo);

        String dbTablePrefix = properties.getProperty(prefix + "TablePrefix", "");
        HashMap<DBTableTypes, String> tableNames = new HashMap<DBTableTypes, String>();

        for (String tableType : properties.getProperty(prefix + "TableTypes", "")
                .replaceAll(",", " ").split(" "))
            if (tableType.trim().length() > 0) {
                try {
                    DBTableTypes type = DBTableTypes.valueOf(tableType.trim().toUpperCase());
                    if (type == null)
                        throw new GMPException(
                                tableType.toUpperCase()
                                        + " is not a recognized table type, i.e., is not an element of enum DBTableTypes");

                    tableNames.put(type, dbTablePrefix + tableType);
                } catch (java.lang.IllegalArgumentException ex) {
                    throw new GMPException(tableType + " is not a recognized table type.");
                }
            }

        for (DBTableTypes type : DBTableTypes.values()) {
            String tableName = properties.getProperty(prefix + type.toMixedCase()
                    + "Table");
            if (tableName != null && tableName.length() > 0)
                tableNames.put(type, tableName);
        }

        for (Map.Entry<DBTableTypes, String> entry : tableNames.entrySet())
            schema.addTable(entry.getValue(), entry.getKey().toString(), "on");

        schema.completeSetup();

        for (String tabletype : tabletypesToTruncate) {
            Table table = schema.getTableOfType(tabletype);
            if (table == null)
                continue;
            char response = 'y';
            if (properties.getBoolean(prefix + "PromptBeforeTruncate", true)) {
                System.out.printf("OK to truncate table %s ? (y/n) ", table.getName());
                response = (char) System.in.read();
            }
            if (response == 'y')
                schema.getDAO().truncateTable(table);
        }

        return schema;
    }

    /**
     * Query the database to retrieve batches of orid|sourceid such that each batch has
     * either one orid|sourceid;  or n orid|sourceid where the sum of the time defining
     * observations in the batch is less than some specified number.
     * Returns a 2D ragged array of longs.  The first index spans the batches and the
     * second spans the orids|sourceids in each batch.
     * <p>The maximum number of time defining phases in a batch is retrieved from the
     * input property file with property batchSizeNdef, which defaults to 100.  Then
     * the following sql statement is executed
     * <p>select orid, count(*) as ndef from assoc where (user-defined where clause)
     * and timedef='d' group by orid order by -ndef
     * <p>The returned information is used to split the orids up into batches, as described above.
     *
     * @param schema
     * @param prefix
     * @return 2D ragged array of orids
     * @throws GMPException
     * @throws FatalDBUtilLibException
     * @throws IOException
     */
    public ArrayList<ArrayListLong> readBatches(Schema schema, String prefix)
            throws GMPException, FatalDBUtilLibException, IOException {
        return readBatches(schema, Math.min(1000, properties.getInt("batchSizeNdef", 1000)),
                properties.getProperty(prefix + "WhereClause", ""));
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
     *
     * @param schema
     * @param ndefMax     desired maximum number of defining phases per batch of orids.
     * @param whereClause
     * @return 2D ragged array of orids
     * @throws GMPException
     * @throws FatalDBUtilLibException
     * @throws IOException
     */
    public ArrayList<ArrayListLong> readBatches(Schema schema, int ndefMax, String whereClause)
            throws GMPException, FatalDBUtilLibException, IOException {
        ArrayList<ArrayListLong> batches = new ArrayList<ArrayListLong>();

        boolean gmp = schema.getTableOfType("SOURCE") != null;

        Table originTable = schema.getTableOfType(gmp ? "SOURCE" : "ORIGIN");
        if (originTable == null)
            throw new GMPException("Schema does not contain a table of type " + (gmp ? "SOURCE" : "ORIGIN"));

        // query will be something like:
        // select orid, ndef from origin_table where <whereClause> order by ndef descending

        String query = String.format("select %s, %s from %s %s order by %s desc",
                gmp ? "sourceid" : "orid",
                gmp ? "numassoc" : "ndef",
                originTable.getName(),
                whereClause.toLowerCase().startsWith("where ") ? whereClause : "where " + whereClause,
                gmp ? "numassoc" : "ndef"
        );

        if (logger.isOutputOn() && logger.getVerbosity() > 0)
            logger.write(String.format("Executing sql:%n%s%n", query.toString()));

        long timer = System.nanoTime();

        // execute the select statement to retrieve data.
        ArrayList<Object[]> data = schema.getDAO().executeSelect(query, new Class[]{Long.class, Long.class});

        timer = System.nanoTime() - timer;

        if (logger.isOutputOn() && logger.getVerbosity() > 0)
            logger.write(String.format("Query returned %d records in %s%n",
                    data.size(), GMPGlobals.ellapsedTime(timer * 1e-9)));

        if (data.size() > 0) {
            long n = 0, orid, ndef;
            ArrayListLong batch = new ArrayListLong();
            batches.add(batch);
            int i = 0;
            while (i < data.size()) {
                orid = ((Long) data.get(i)[0]).longValue();
                ndef = ((Long) data.get(i)[1]).longValue();
                if (ndef <= 0)
                    ndef = 10;
                if (batch.size() > 0 && n + ndef > ndefMax) {
                    batch = new ArrayListLong();
                    batches.add(batch);
                    n = 0;
                }
                batch.add(orid);
                n += ndef;
                ++i;
            }
        }

        if (logger.isOutputOn() && logger.getVerbosity() > 0)
            logger.write(String.format("%d Sources divided among %d batches with number of time defining phases < %d in each batch%n",
                    data.size(), batches.size(), ndefMax));

        long check = 0;
        for (ArrayListLong batch : batches)
            check += batch.size();

        if (check != data.size())
            throw new GMPException(String.format("Sum of batch sizes (%d) != data size (%d)",
                    check, data.size()));

        return batches;
    }

    /**
     * Query the database to retrieve batches of orid|sourceid such that each batch has
     * less than some number of time defining phases.  Returns a 2D ragged array of longs.
     * The first index spans the batches and the second spans the orids|sourceids in each
     * batch.
     * <p>The maximum number of time defining phases in a batch is retrieve from the
     * input property file with property batchSizeNdef, which defaults to 100.  Then
     * the following sql statement is executed
     * <p>select orid, count(*) as ndef from assoc where (ser-defined where clause)
     * and timedef='d' group by orid order by -ndef
     * <p>The returned information is used to split the orids up into batches, as described above.
     *
     * @param schema
     * @return array of orids
     * @throws GMPException
     * @throws FatalDBUtilLibException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public ArrayListLong readUniqueSourceIds(Schema schema, String inputPrefix)
            throws GMPException, FatalDBUtilLibException, IOException {
        String whereClause = properties.getProperty(inputPrefix + "WhereClause", "");

        Table assocTable = schema.getTableOfType("ASSOC");
        if (assocTable == null)
            assocTable = schema.getTableOfType("SRCOBSASSOC");
        if (assocTable == null)
            throw new GMPException("Schema does not contain a table of type ASSOC or SRCOBSASSOC");

        boolean gmp = assocTable.getTableType().equals("ASSOC");

        // select unique sourceids based on input where clause
        String query = String.format("select unique( %s ) from %s where %s ",
                gmp ? "orid" : "sourceid",
                assocTable.getName(),
                whereClause
        );

        if (logger.isOutputOn() && logger.getVerbosity() > 0)
            logger.write(String.format("Executing sql:%n%s%n", query.toString()));

        // create an array of Class types. this will be passed in to the
        // DataAccessObject's select statement to inform it that we want one long back.
        Class[] types = new Class[]{Long.class};

        long timer = System.nanoTime();

        // execute the select statement to retreive data.
        ArrayList<Object[]> data = schema.getDAO().executeSelect(
                query, types);

        timer = System.nanoTime() - timer;

        if (logger.isOutputOn() && logger.getVerbosity() > 0)
            logger.write(String.format("Query returned %d records in %s%n",
                    data.size(), GMPGlobals.ellapsedTime(timer * 1e-9)));

        ArrayListLong uniqueSourceIds = new ArrayListLong();
        if (data.size() > 0) {
            for (Object[] record : data) {
                uniqueSourceIds.add(((Long) record[0]).longValue());
            }
        }

        return uniqueSourceIds;
    }

    /**
     * Given a Colleciton of Predictors, see if they already have entries in the data base
     * ALGORITHM table.  If they do not, then create entries in the database.  Then
     * update the predictionId value in the Predictor object with the correct value
     * from the database.
     * <p>Then check all the models that the Predictors are currently configured to use
     * to see if they have entries in the database GEOMODEL table.  If they do not then
     * create new database table entries.  Then update the modelId value in the
     * Predictor object with the correct value from the database.
     * <p>If the supplied schema object does not include ALGORITHM and/or GEOMODEL tables
     * then the corresponding step is skipped.
     *
     * @param schema
     * @param predictors
     * @throws Exception 
     */
    public void updatePredictorIds(Schema schema, Collection<Predictor> predictors)
            throws Exception {
        try {
            Table algorithmTable = schema.getTableOfType(DBTableTypes.ALGORITHM.toString());
            if (algorithmTable != null) {
                if (logger.isOutputOn() && logger.getVerbosity() > 0)
                    logger.write(String.format("Querying database table %s%n", algorithmTable.getName()));

                for (Predictor predictor : predictors) {
                    long algorithmId;

                    String select = String.format(
                            "select algorithmid from %s where name='%s' and version='%s'",
                            algorithmTable.getName(), predictor.getPredictorName(),
                            predictor.getPredictorVersion());

                    ArrayList<Object[]> ids;
                    ids = schema.getDAO().executeSelect(select, new Class[]
                            {Long.class});

                    if (ids.size() == 0) {
                        algorithmId = schema.getDAO().getMaxID("algorithmid", algorithmTable.getName()) + 1;
                        Object[] values = new Object[5];
                        values[0] = new Long(algorithmId); // ttalgoid
                        values[1] = predictor.getPredictorName(); // name
                        values[2] = predictor.getPredictorVersion(); // version
                        values[3] = GMPGlobals.getAuth();   // auth
                        values[4] = GMPGlobals.getLddate(); // lddate

                        if (logger.isOutputOn() && logger.getVerbosity() > 0)
                            logger.write(String.format("Inserting row %d %s.%s%n",
                                    algorithmId, predictor.getPredictorName(), predictor.getPredictorVersion()));

                        (new Row(algorithmTable, values, false)).insertIntoDB();
                    } else {
                        algorithmId = ((Long) ids.get(0)[0]).longValue();

                        if (logger.isOutputOn() && logger.getVerbosity() > 0)
                            logger.write(String.format("Retrieved row %d %s.%s%n",
                                    algorithmId, predictor.getPredictorName(), predictor.getPredictorVersion()));

                    }
                    predictor.setAlgorithmId(algorithmId);
                }
                schema.getDAO().commit();

                if (logger.isOutputOn() && logger.getVerbosity() > 0)
                    logger.write(String.format("Database commit in IODB.updatePredictionIds()%n"));

            }

            Table modelTable = schema.getTableOfType(DBTableTypes.GEOMODEL.toString());
            if (modelTable != null) {
                if (logger.isOutputOn() && logger.getVerbosity() > 0)
                    logger.write(String.format("Querying database table %s%n", algorithmTable.getName()));

                for (Predictor predictor : predictors) {
                    File f = predictor.getModelFile();
                    Calendar calendar = new GregorianCalendar();

                    // set time to 2 seconds before file's last modified date to use as low range
                    calendar.setTime(new Date(f.lastModified() - 2000));
                    String sdatelow = String.format("%4d.%02d.%02d.%02d.%02d.%02d",
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            calendar.get(Calendar.SECOND));

                    // now set time to 2 seconds after file's last modified date to use as high range
                    calendar.setTime(new Date(f.lastModified() + 2000));
                    String sdatehigh = String.format("%4d.%02d.%02d.%02d.%02d.%02d",
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            calendar.get(Calendar.SECOND));

                    Table geomodelTable = schema.getTableOfType(DBTableTypes.GEOMODEL.toString());

                    String select = String.format(
                            "select geomodelid from %s where dfile='%s' and to_char(filedate, 'YYYY.MM.DD.HH24.MI.SS')>'%s' "
                                    + " and to_char(filedate, 'YYYY.MM.DD.HH24.MI.SS')<'%s'",  // use filedate timestamp with a 4 second tolerance
                            geomodelTable.getName(), f.getName(), sdatelow, sdatehigh);

                    ArrayList<Object[]> ids = schema.getDAO().executeSelect(select, new Class[]{Long.class});

                    long modelId;
                    if (ids.size() == 0) {
                        modelId = schema.getDAO().getMaxID("geomodelid", geomodelTable.getName()) + 1;
                        Object values[] = new Object[8];
                        values[0] = new Long(modelId); // geomodelid
                        values[1] = VectorGeo.earthShape.toString(); // earthshape
                        try {
                            values[2] = f.getParentFile().getCanonicalPath(); // dir
                        } catch (IOException e) {
                            values[2] = "error in IODB.updatePredictionIds()";
                        }
                        values[3] = f.getName(); // dfile
                        values[4] = new Date(f.lastModified()); // filedate
                        values[5] = predictor.getModelDescription(); // description
                        values[6] = GMPGlobals.getAuth(); // auth
                        values[7] = GMPGlobals.getLddate(); // lddate

                        (new Row(geomodelTable, values, false)).insertIntoDB();

                        if (logger.isOutputOn())
                            logger.write(String.format("Inserting row %d %s%n",
                                    modelId, f.getCanonicalPath()));

                    } else {
                        modelId = ((Long) ids.get(0)[0]).longValue();

                        if (logger.isOutputOn())
                            logger.write(String.format("Retrieved row %d %s%n",
                                    modelId, f.getCanonicalPath()));
                    }
                    predictor.setModelId(modelId);
                }
                schema.getDAO().commit();

                if (logger.isOutputOn())
                    logger.write(String.format("Database commit in IODB.updatePredictionIds()%n"));

            }

        } catch (FatalDBUtilLibException e) {
            schema.getDAO().rollback();

            if (logger.isOutputOn())
                logger.write(String.format("Database rollback in IODB.updatePredictionIds()%n"));

        }
    }

    /**
     * Retrieve the next unused Id from the specified database table.  If the schema does
     * not have have a table of the specified type, or if the specified table type is not
     * an idowner table, returns null.
     *
     * @param schema
     * @param tableType
     * @return next id
     * @throws FatalDBUtilLibException
     */
    public AtomicLong getNextId(Schema schema, DBTableTypes tableType) throws FatalDBUtilLibException {
        Table table = schema.getTableOfType(tableType.toString());
        if (table == null)
            return null;
        String ownedId = table.getOwnedID();
        if (ownedId == null)
            return null;
        return new AtomicLong(schema.getDAO().getMaxID(ownedId, table.getName()) + 1);
    }

    /**
     * For every idowner table in the specified schema, find the next unused owned id value
     * and return a hashmap from TomoTableType to nextID.
     *
     * @param schema
     * @return lots of next ids
     * @throws FatalDBUtilLibException
     */
    public HashMap<DBTableTypes, AtomicLong> getNextIds(Schema schema) throws FatalDBUtilLibException {
        HashMap<DBTableTypes, AtomicLong> nextIds = new HashMap<DBTableTypes, AtomicLong>();
        for (Table table : schema.getTables()) {
            DBTableTypes type = DBTableTypes.valueOf(table.getTableType().toUpperCase());
            AtomicLong nextId = getNextId(schema, type);
            if (nextId != null)
                nextIds.put(type, nextId);
        }
        return nextIds;
    }

}
