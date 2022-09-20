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

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.rayuncertainty.basecontainers.Observation;
import gov.sandia.gmp.rayuncertainty.containers.ObservationsPhaseSiteMap;
import gov.sandia.gmp.rayuncertainty.containers.ObservationsSourceMap;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * An IODB extension specifically for the RayUncertainty application. Contains
 * functions to retrieve source, receiver, phase, and observations from an
 * input query contained in the input properties file.
 *
 * @author jrhipp
 */
public class IODBRayUnc extends IODB {
    /**
     * Used to provide Screen or BufferedWriter Output.
     */
    private ScreenWriterOutput aScrnWrtr = new ScreenWriterOutput();

    /**
     * Input schema used for all input functions.  Note that this may include
     * table types that do not physically exist in the database.
     */
    private Schema inputSchema;

    /**
     * Input database tables prefix.
     */
    private String dbInputTablePrefix;

    /**
     * Standard Constructor.
     *
     * @param inputProperties The properties file containing the database
     *                        connection properties.
     * @param inputEarthShape The EarthShape object defining the GeoVectors of
     *                        all read source and receiver objects.
     * @param inputaScrnWrtr  The ScreenWriterOutput object that outputs to the
     *                        screen and an ouput text file.
     * @throws IOException
     */
    public IODBRayUnc(PropertiesPlusGMP inputProperties,
                      ScreenWriterOutput inputaScrnWrtr)
            throws IOException {
        // call the base object to make the connection and set the screen writer

        super(inputProperties, inputaScrnWrtr, inputaScrnWrtr);

        if (inputaScrnWrtr != null)
            this.aScrnWrtr = inputaScrnWrtr;
        else {
            // else reset superclass loggers back to original since input was null
            setLoggers(this.aScrnWrtr, this.aScrnWrtr);
        }

        // create input and output schema

        createInputSchema();
        //createOutputSchema();
    }

    /**
     * Define the input schema.
     */
    private void createInputSchema() {
        String s;

        // create the input schema

        if (aScrnWrtr.isOutputOn())
            aScrnWrtr.write(NL + "  Creating input schema ..." + NL);

        dbInputTablePrefix = properties.getProperty("dbInputTablePrefix", "").trim();
        String dbInputUserName = properties.getProperty("dbInputUserName", "").trim();
        String dbInputPassword = properties.getProperty("dbInputPassword", "").trim();
        String dbInputTableDefinitionTable = properties.getProperty(
                "dbInputTableDefinitionTable", "").trim();

        // get input instance/driver

        String dbInstance = properties.getProperty("dbInputInstance",
                "jdbc:oracle:thin:@bikinifire:1522:kbdb").trim();
        String dbDriver = properties.getProperty("dbInputDriver",
                "oracle.jdbc.driver.OracleDriver").trim();

        // but the connection properties into a parameter information object

        ParInfo parInfo = new ParInfo();
        parInfo.addParameter(ParInfo.USERNAME, dbInputUserName);
        parInfo.addParameter(ParInfo.PASSWORD, dbInputPassword);
        parInfo.addParameter(ParInfo.TABLE_DEFINITION_TABLE,
                dbInputTableDefinitionTable);
        parInfo.addParameter(ParInfo.INSTANCE, dbInstance);
        parInfo.addParameter(ParInfo.DRIVER, dbDriver);

        // output connection properties

        if (aScrnWrtr.isOutputOn()) {
            s = "    Username             = " + dbInputUserName + NL +
                    "    Password             = " + dbInputPassword + NL +
                    "    Driver               = " + dbDriver + NL +
                    "    Instance             = " + dbInstance + NL +
                    "    TableDefinitionTable = " + dbInputTableDefinitionTable +
                    NL + NL;
            aScrnWrtr.write(s);
        }

        // create the schema

        try {
            inputSchema = new Schema(parInfo);
        } catch (FatalDBUtilLibException e) {
            if (aScrnWrtr.isOutputOn())
                aScrnWrtr.write("Error while creating input schema ..." + NL);
            e.printStackTrace();
        }
    }

    /**
     * Returns the ScreenWriterOutput object with which a BufferedWriter can
     * be set and screen / writer output options can be changed.
     *
     * @return The owned ScreenWriterOutput object.
     */
    public ScreenWriterOutput getScreenWriterOutput() {
        return aScrnWrtr;
    }

    /**
     * Read observations count from the database
     * using the dbInputModelQuery plus any user specified
     * additional query
     *
     * @param additionalQuery SQL where clause to add to the default query, or
     *                        null. The query should NOT include "where".
     * @return Number of observations that satisfy the query
     * @throws FatalDBUtilLibException
     */
    public long getObservationsCountFromDatabase(String additionalQuery)
            throws FatalDBUtilLibException {
        String s;

        try {
            // output message

            if (aScrnWrtr.isOutputOn())
                aScrnWrtr.write("  Retrieving observations count from database ..." +
                        NL);

            // read properties file to assemble all tables

            HashSet<String> dbInputTableTypes = new HashSet<String>();
            for (String tableType :
                    properties.getProperty("dbInputTableTypes", "").trim().split(" "))
                dbInputTableTypes.add(tableType.trim().toLowerCase());

            String predictionTable = properties.getProperty(
                    "dbInputPredictionTable", dbInputTablePrefix +
                            DBTableTypes.PREDICTION.toString());
            String sourceTable = properties.getProperty(
                    "dbInputSourceTable", dbInputTablePrefix +
                            DBTableTypes.SOURCE.toString());
            String srcObsAssocTable = properties.getProperty(
                    "dbInputSrcObsAssocTable", dbInputTablePrefix +
                            DBTableTypes.SRCOBSASSOC.toString());
            String observationTable = properties.getProperty(
                    "dbInputObservationTable", dbInputTablePrefix +
                            DBTableTypes.OBSERVATION.toString());
            String receiverTable = properties.getProperty(
                    "dbInputReceiverTable", dbInputTablePrefix +
                            DBTableTypes.RECEIVER.toString());

            String dbInputModelQuery = properties.getProperty("dbInputModelQuery",
                    "").trim();

            // add additional query if specified

            if (additionalQuery != null && additionalQuery.trim().length() > 0)
                dbInputModelQuery += " and " + additionalQuery.trim() + " ";

            if (aScrnWrtr.isOutputOn())
                aScrnWrtr.write("    Observation count query = " +
                        dbInputModelQuery + NL);

            // only include prediction table if dbinputtabletypes includes a
            // prediction table

            boolean includePrediction = false;
            for (String type : dbInputTableTypes) {
                if (type.equalsIgnoreCase("prediction")) {
                    includePrediction = true;
                    break;
                }
            }

            // assemble query

            StringBuffer query = new StringBuffer();
            query.append("select count(*) from ");

            query.append(sourceTable).append(" source, ");
            query.append(srcObsAssocTable).append(" srcobsassoc, ");
            query.append(observationTable).append(" observation, ");
            query.append(receiverTable).append(" receiver");

            // optionally include prediction table in query

            if (includePrediction) {
                query.append(", ");
                query.append(predictionTable).append(" prediction ");
            }

            // where clause

            query.append(" ").append(dbInputModelQuery).append(" ");

            // optionally include prediction part of query

            if (includePrediction) {
                query.append("and prediction.sourceid=source.sourceid ");
                query.append("and prediction.receiverid=receiver.receiverid ");
                query.append("and srcobsassoc.phase=prediction.phase ");
            }

            // append to query

            query.append("and srcobsassoc.sourceid=source.sourceid ");
            query.append("and srcobsassoc.observationid=observation.observationid ");
            query.append("and observation.receiverid=receiver.receiverid ");

            // execute the select statement to retreive data.

            if (aScrnWrtr.isOutputOn())
                aScrnWrtr.write("    Executing query         = " +
                        query.toString() + NL);

            ArrayList<Object[]>
                    data = inputSchema.getDAO().executeSelect(query.toString(),
                    new Class[]{Long.class});

            // see if valid result was returned

            if (data == null || data.size() != 1) {
                s = "Error: Function getObservationsCountFromDatabase(...) did not " +
                        "       return any rows.";
                throw new FatalDBUtilLibException(s);
            }

            // return result

            Object[] obj = data.get(0);
            if (obj.length == 1 && obj[0] instanceof Long) {
                if (aScrnWrtr.isOutputOn())
                    aScrnWrtr.write("    Found " + ((Long) obj[0]) +
                            " observations." + NL);
                return (Long) (obj[0]);
            } else {
                s = "Error: Function getObservationsCountFromDatabase(...) returned " +
                        "invalid results.";
                throw new FatalDBUtilLibException(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FatalDBUtilLibException(ex.getMessage());
        }
    }

    /**
     * Returns a long array of unique receiver id's from the input schema's
     * receiver table.
     *
     * @return Long array of unique receiver id's.
     * @throws FatalDBUtilLibException
     */
    public long[] getUniqueReceiverIds() throws FatalDBUtilLibException {
        // get receiver table and assemble query

        String receiver = properties.getProperty(
                "dbInputReceiverTable", dbInputTablePrefix +
                        DBTableTypes.RECEIVER.toString());

        String query = "select unique(receiverid) from " + receiver +
                " order by receiverid";

        // execute select statement

        ArrayList<Object[]> rows = inputSchema.getDAO().executeSelect(query,
                new Class[]{Long.class});
        if (rows == null || rows.size() == 0)
            throw new FatalDBUtilLibException("The input receiver table does not " +
                    "contain any rows.");

        // fill receiver id array

        long[] receiverIds = new long[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            Object[] obj = rows.get(i);
            if (obj.length == 1 && obj[0] instanceof Long)
                receiverIds[i] = (Long) (obj[0]);
            else
                throw new FatalDBUtilLibException("The input receiver table does " +
                        "not have a valid receiverid field.");
        }

        // return result

        return receiverIds;
    }

    /**
     * Returns a long array of unique source id's from the input schema's source
     * table.
     *
     * @return Long array of unique source id's.
     * @throws FatalDBUtilLibException
     */
    public long[] getUniqueSourceIds() throws FatalDBUtilLibException {
        // get source table and assemble query

        String source = properties.getProperty(
                "dbInputSourceTable", dbInputTablePrefix +
                        DBTableTypes.SOURCE.toString());

        String query = "select unique(sourceid) from " + source +
                " order by sourceid";

        // execute select statement

        ArrayList<Object[]> rows = inputSchema.getDAO().executeSelect(query,
                new Class[]{Long.class});
        if (rows == null || rows.size() == 0)
            throw new FatalDBUtilLibException("The input source table does not " +
                    "contain any rows.");

        // fill source id array

        long[] sourceIds = new long[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            Object[] obj = rows.get(i);
            if (obj.length == 1 && obj[0] instanceof Long)
                sourceIds[i] = (Long) (obj[0]);
            else
                throw new FatalDBUtilLibException("The input source table does "
                        + "not have a valid sourceid field.");
        }

        // return result

        return sourceIds;
    }

    /**
     * Returns a string array of unique phases from the input schema's
     * srcobsassoc table.
     *
     * @return String array of unique phases.
     * @throws FatalDBUtilLibException
     */
    public String[] getUniquePhases() throws FatalDBUtilLibException {
        // get source observation assoc table and assemble query

        String srcObsAssoc = properties.getProperty(
                "dbInputSrcObsAssocTable", dbInputTablePrefix +
                        DBTableTypes.SRCOBSASSOC.toString());

        String query = "select unique(phase) from " + srcObsAssoc +
                " order by phase";

        // execute select statement

        ArrayList<Object[]> rows = inputSchema.getDAO().executeSelect(query,
                new Class[]{String.class});
        if (rows == null || rows.size() == 0)
            throw new FatalDBUtilLibException("The input srcobsassoc table does " +
                    "not contain any rows.");

        // fill phase name array

        String[] phases = new String[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            Object[] obj = rows.get(i);
            if (obj.length == 1 && obj[0] instanceof String)
                phases[i] = (String) (obj[0]);
            else
                throw new FatalDBUtilLibException("The input srcobsassoc table "
                        + "does not have a valid phase field.");
        }

        // return result

        return phases;
    }

    /**
     * Read observations from the database with an optional clustering query.
     *
     * @param selectPhases    If defined only phases contained in this input list
     *                        will be included in the output observation map and
     *                        list (phRcvrObsMap and glbObsList). If null it is
     *                        ignored and all discovered phases will be included.
     * @param selectReceivers If defined only receivers contained in this input
     *                        list will be included in the output observation map
     *                        and list (phRcvrObsMap and glbObsList). If null it is
     *                        ignored and all discovered receivers will be included.
     * @param rcvrs           The output map of all receivers (associated with their id)
     *                        added to the output observation map and global list.
     * @param srcs            The output map of all sources (associated with their id)
     *                        added to the output observation map and global list.
     * @param observationMap  The output map of all returned observations
     *                        associated by phase, site id, and source id.
     * @param observationList The output list of all observations in the
     *                        observation map.
     * @param addionalQuery   Additional query to add to the default query which is
     *                        based on dbInputModelQuery, or null. The query should
     *                        NOT include "where".
     * @throws GeoTomoException
     */
    public void readObservationsFromDatabase(HashSet<SeismicPhase> selectPhases,
                                             HashSet<String> selectReceivers,
                                             HashMap<Long, Receiver> rcvrs,
                                             HashMap<Long, Source> srcs,
                                             ObservationsPhaseSiteMap
                                                     observationMap,
                                             ArrayList<Observation>
                                                     observationList,
                                             String additionalQuery)
            throws Exception {
        // output message

        if (aScrnWrtr.isOutputOn())
            aScrnWrtr.write("  Reading observations from the database ..." + NL);

        // first get observation related database input settings

        HashSet<String> dbInputTableTypes = new HashSet<String>();
        for (String tableType :
                properties.getProperty("dbInputTableTypes", "").trim().split(" "))
            dbInputTableTypes.add(tableType.trim().toLowerCase());

        String predictionTable = properties.getProperty(
                "dbInputPredictionTable", dbInputTablePrefix +
                        DBTableTypes.PREDICTION.toString());
        String sourceTable = properties.getProperty(
                "dbInputSourceTable", dbInputTablePrefix +
                        DBTableTypes.SOURCE.toString());
        String srcObsAssocTable = properties.getProperty(
                "dbInputSrcObsAssocTable", dbInputTablePrefix +
                        DBTableTypes.SRCOBSASSOC.toString());
        String observationTable = properties.getProperty(
                "dbInputObservationTable", dbInputTablePrefix +
                        DBTableTypes.OBSERVATION.toString());
        String receiverTable = properties.getProperty(
                "dbInputReceiverTable", dbInputTablePrefix +
                        DBTableTypes.RECEIVER.toString());

//        try {
            // get input query

            String dbInputModelQuery = properties.getProperty("dbInputModelQuery",
                    "").trim();
            if (aScrnWrtr.isOutputOn())
                aScrnWrtr.write("    Input model query       = " +
                        dbInputModelQuery + NL);

            // only include prediction table if dbinputtabletypes includes a
            // prediction table

            boolean includePrediction = false;
            for (String type : dbInputTableTypes) {
                if (type.equalsIgnoreCase("prediction")) {
                    includePrediction = true;
                    break;
                }
            }

            // create an array of Class types.  this will be passed in to the
            // DataAccessObject's select statement to inform it what types of
            // data are expected to be returned by the select statement.

            Class[] types = new Class[]
                    {
                            Long.class,   //  0 source.sourceid
                            Long.class,   //  1 observation.observationid
                            Long.class,   //  2 prediction.predictionid OR dummy value if no prediction table
                            Long.class,   //  3 receiver.receiverid
                            String.class, //  4 receiver.sta
                            String.class, //  5 observation.phase,
                            Double.class, //  6 source.lat
                            Double.class, //  7 source.lon
                            Double.class, //  8 source.depth
                            oracle.sql.TIMESTAMP.class,   //  9 source.origintime
                            Double.class, // 10 receiver.lat
                            Double.class, // 11 receiver.lon
                            Double.class, // 12 receiver.elevation
                            oracle.sql.TIMESTAMP.class,   // 13 receiver.starttime
                            oracle.sql.TIMESTAMP.class,   // 14 receiver.endtime
                            oracle.sql.TIMESTAMP.class,   // 15 observation.arrivaltime
                            Double.class, // 16 observation.timeUncertainty
                            Double.class, // 17 source.gtlevel,
                            Long.class    // 18 source.eventid
                    };

            // build query

            StringBuffer query = new StringBuffer();
            query.append("select ");
            query.append("source.sourceid, ");
            query.append("observation.observationid, ");

            // either get predictionid or dummy value if no prediction table

            if (includePrediction)
                query.append("prediction.predictionid, ");
            else
                query.append("-1, ");

            query.append("receiver.receiverid, ");
            query.append("receiver.sta, ");
            query.append("srcobsassoc.phase, ");
            query.append("source.lat, ");
            query.append("source.lon, ");
            query.append("source.depth, ");
            query.append("source.origintime, ");
            query.append("receiver.lat, ");
            query.append("receiver.lon, ");
            query.append("receiver.elevation, ");
            query.append("receiver.starttime, ");
            query.append("receiver.endtime, ");
            query.append("observation.arrivaltime, ");
            query.append("observation.timeuncertainty, ");
            query.append("source.gtlevel, ");
            query.append("source.eventid ");
            query.append("from ");
            query.append(sourceTable).append(" source, ");
            query.append(srcObsAssocTable).append(" srcobsassoc, ");
            query.append(observationTable).append(" observation, ");
            query.append(receiverTable).append(" receiver");

            // optionally include prediction table in query

            if (includePrediction) {
                query.append(", ");
                query.append(predictionTable).append(" prediction ");
            }

            // where clause

            query.append(" ").append(dbInputModelQuery).append(" ");

            // optionally include prediction part of query

            if (includePrediction) {
                query.append("and prediction.sourceid=source.sourceid ");
                query.append("and prediction.receiverid=receiver.receiverid ");
                query.append("and srcobsassoc.phase=prediction.phase ");
            }

            // optionally include additionalQuery

            if (additionalQuery != null) {
                query.append("and " + additionalQuery + " ");
            }

            // append to query

            query.append("and srcobsassoc.sourceid=source.sourceid ");
            query.append("and srcobsassoc.observationid=observation.observationid ");
            query.append("and observation.receiverid=receiver.receiverid ");

            // execute the select statement to retrieve data.

            if (aScrnWrtr.isOutputOn()) {
                aScrnWrtr.write("    Executing query         = " +
                        query.toString() + NL);
            }

            // execute

            long startTime = (new Date()).getTime();
            ArrayList<Object[]>
                    data = inputSchema.getDAO().executeSelect(query.toString(), types);
            long stopTime = (new Date()).getTime();
            String ts = Globals.timeString(stopTime - startTime);

            // output results

            if (aScrnWrtr.isOutputOn()) {
                aScrnWrtr.write("    Retrieved " + data.size() +
                        " rows of observation data." + NL);
                aScrnWrtr.write("    Database read time      = " + ts + NL);
                aScrnWrtr.write("    Creating observations ..." + NL);
            }

            // clear input lists

            observationList.clear();
            observationMap.clear();
            srcs.clear();
            rcvrs.clear();

            Source source;
            Receiver receiver;
            Long sourceid, receiverid;

            // read gt time event id's from the database

            ArrayList<Long> gtTimeEventIds = null;
            try {
                if (aScrnWrtr.isOutputOn()) {
                    aScrnWrtr.write("    Reading GT Time event id's from database..." +
                            NL);
                }
                gtTimeEventIds = this.getGtTimeEvids();
            } catch (FatalDBUtilLibException e) {
                if (aScrnWrtr.isOutputOn()) {
                    aScrnWrtr.write("  Error reading from gttime table. " +
                            "Ignoring GT Time Event flags..." + NL);
                }
                e.printStackTrace();

                // make empty list so that it will not keep trying to
                // read from the database if it failed

                gtTimeEventIds = new ArrayList<Long>();
            }

            // parse the data returned from the select statement into Observation
            // objects and store the observation objects in an ObservationList.

            HashMap<Long, Observation> obsMap = new HashMap<Long, Observation>();
            for (Object[] ob : data) {
                // see if observation phase and receiver names are contained in the
                // input selection sets (if they are defined). If they are defined but
                // the observations phase or receiver name is not contained then do not
                // include the observation in the result.

                SeismicPhase ph = SeismicPhase.valueOf((String) ob[5]);
                boolean addObservation = true;
                if ((selectPhases != null) &&
                        !selectPhases.contains(ph)) addObservation = false;
                if ((selectReceivers != null) &&
                        !selectReceivers.contains((String) ob[4])) addObservation = false;

                // include observation if still requested

                if (addObservation) {
                    // need to convert from oracle timestamp to epoch time

                    java.sql.Timestamp t = ((oracle.sql.TIMESTAMP) ob[9]).timestampValue();
                    double originTime = GMTFormat.toGMT(t.getTime()) / 1000.0;
                    t = ((oracle.sql.TIMESTAMP) ob[15]).timestampValue();
                    //double arrivalTime = GMTFormat.toGMT(t.getTime())/1000.0;

                    // add source if not yet included

                    sourceid = ((Long) ob[0]).longValue();
                    source = srcs.get(sourceid);
                    if (source == null) {
                        // get source from origin table entries

                        GeoVector sourcePosition = new GeoVector(
                                ((Double) ob[6]).doubleValue(),  // source.lat in degrees
                                ((Double) ob[7]).doubleValue(),  // source.lon in degrees
                                ((Double) ob[8]).doubleValue(),  // source.depth
                                true);    // in degrees

                        // create source with all fields set

                        long eventId = ((Long) ob[18]).longValue();
                        source = new Source(sourceid, eventId, sourcePosition,
                                originTime, ((Double) ob[17]).doubleValue(),
                                gtTimeEventIds.contains(eventId));
                        srcs.put(sourceid, source);
                    }

                    // add receiver if not yet included

                    receiverid = ((Long) ob[3]).longValue();
                    receiver = rcvrs.get(receiverid);
                    if (receiver == null) {
                        GeoVector receiverPosition = new GeoVector(
                                ((Double) ob[10]).doubleValue(), // receiver.lat in degrees
                                ((Double) ob[11]).doubleValue(), // receiver.lon in degrees
                                -((Double) ob[12]).doubleValue(), // receiver.elevation converted to depth
                                true);

                        // convert receiver starttime and endtime from Oracle timestamp to
                        // epoch time

                        java.sql.Timestamp tempTime = ((oracle.sql.TIMESTAMP) ob[13]).
                                timestampValue(); // starttime
                        double receiverStart = GMTFormat.toGMT(tempTime.getTime()) / 1000.0;
                        tempTime = ((oracle.sql.TIMESTAMP) ob[14]).
                                timestampValue();  // endtime
                        double receiverEnd = GMTFormat.toGMT(tempTime.getTime()) / 1000.0;

                        // create receiver with some fields set (fake ondate and offdate)

                        receiver = new Receiver(receiverid, (String) ob[4], receiverStart,
                                receiverEnd, receiverPosition);
                        rcvrs.put(receiverid, receiver);
                    }

                    // get phase and phase/receiver observation set (obsSet)

                    ObservationsSourceMap procData = observationMap.getSet(ph, receiverid);

                    // create a new observation

                    Observation observation;
                    observation = new Observation(((Long) ob[1]).longValue(),
                            observationList.size(),
                            obsMap.size(), ph, receiverid,
                            sourceid);

                    // throw an error if the observation already exists.

                    if (obsMap.get(observation.getId()) != null) {
                        if (aScrnWrtr.isOutputOn()) {
                            aScrnWrtr.write("  Unique constraint violated for observation " +
                                    "(obs id = " + observation.getId() +
                                    ", source id = " + observation.getSourceId() +
                                    ", receiver id = " + observation.getReceiverId() +
                                    ")" + NL);
                            Observation oldOb = obsMap.get(observation.getId());
                            aScrnWrtr.write("    Ignoring new observation and using old " +
                                    "observation (obs id = " + oldOb.getId() +
                                    ", source id = " + oldOb.getSourceId() +
                                    ", receiver id = " + oldOb.getReceiverId() +
                                    ")" + NL);
                        }
                    } else {
                        // good observation ... add to map and to phase/Receiver set

                        obsMap.put(observation.getId(), observation);
                        procData.addObservation(observation);
                        observationList.add(observation);
                    }
                }
            }

            // done ... output message and exit

            if (aScrnWrtr.isOutputOn()) {
                String s = "    Constructed " + obsMap.size() +
                        " Observation objects from the database input." + NL;
                aScrnWrtr.write(s);
            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw new GeoTomoException(ex.getMessage());
//        }
    }

    /**
     * Read the Event Id's from a GT Time table in the input schema.
     *
     * @return List of eventid's for the gt time events.
     * @throws FatalDBUtilLibException
     */
    public ArrayList<Long> getGtTimeEvids() throws FatalDBUtilLibException {
        // get gt time table and assemble query

        String gtTimeTable = properties.getProperty(
                "dbInputGtTimeTable", dbInputTablePrefix +
                        DBTableTypes.GT_TIME.toString());

        String query = "select unique(evid) from " + gtTimeTable +
                " order by evid";

        // execute select statement

        ArrayList<Object[]> rows = inputSchema.getDAO().executeSelect(query,
                new Class[]{Long.class});
        if (rows == null || rows.size() == 0)
            throw new FatalDBUtilLibException("The input gt time table does not " +
                    "contain any rows.");

        // fill list of event ids

        ArrayList<Long> eventIds = new ArrayList<Long>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            Object[] obj = rows.get(i);
            if (obj.length == 1 && obj[0] instanceof Long)
                eventIds.add(new Long((Long) (obj[0])));
            else
                throw new FatalDBUtilLibException("The input gt time table does "
                        + "not have a valid evid field.");
        }

        // return result

        return eventIds;
    }
}
