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
package gov.sandia.gnem.dbutillib;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import gov.sandia.gnem.dbutillib.dao.DAO;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * This class handles all functionality that interacts with the IDGaps table in any way. The concept behind the IDGaps
 * table is that whenever you need to add a new row to a table, you are going to need a new, unique id number for the
 * owned id/primary key for that row. The IDGaps table keeps track of what id values are available for owned ids/primary
 * keys. Usually, the next available id value is just the current highest id value + 1. However, when dealing with large
 * tables where data has been added and deleted repeatedly over time, it is possible to run out of id values to give to
 * new rows being added if you use the highest value + 1 method. So, the IDGaps table keeps track of all available ids -
 * either from gaps left by deletions or the max id + 1.
 * <p>
 * Each Schema is expected to have its own IDGaps table. It is expected that all of the ids within one Schema are
 * internally consistent so that the foreign keys in rows refer to the row they are expected to refer to.
 * <p>
 * The IDGaps table is expected to exist with the names and types of the columns identified below. The types are listed
 * as Oracle SQL types, but an equivalent type in something like an XML database would also be fine. <BR>
 * <B>ID</B>: unique identifier - NUMBER(12) <BR>
 * <B>ID_NAME</B>: name of the id that the IDGaps table has available values for - VARCHAR2(12) <BR>
 * <B>GAP_START</B>: value where a "gap" in the used id values for the id in ID_NAME starts; for an id in a table that
 * does not wish to have its gaps used up, this can be just the highest value + 1 - NUMBER(12) <BR>
 * <B>GAP_END</B>: value where a "gap" in the used id values for the id in ID_NAME ends or the highest available id -
 * NUMBER(12)
 * <p>
 * When new information is updated in the table, that information will be updated in an in memory structure that keeps
 * track of what's been changed since the IDGapsTable object was instantiated. To actually commit changes that have been
 * made to the IDGaps table, call the {@link #write write()} function.
 * <p>
 * <B><I>IMPORTANT NOTE!</B> While the above is an accurate description of the desired end result of this class, nothing
 * has been implemented as of yet that actually handles the above mentioned gap functionality. Right now, the IDGaps
 * table just keeps track of the highest value id + 1.</I>
 * <p>
 * Here is an example of how to create and use an IDGapsTable object
 * <p>
 * Creating:<BR>
 * <CODE>IDGapsTable idgapsTable = new IDGapsTable(mySchema, "MY_IDGAPS");</CODE>
 * <p>
 * Getting the next available id for a primary key named "myKey":<BR>
 * <CODE>Long id = idGapsTable.getNextId("myKey");</CODE>
 *
 * @author Jennifer Lewis
 * @version 1.0
 */
public class IDGapsTable {
    /**
     * Schema object this IDGapsTable is associated with.
     */
    protected Schema schema = null;

    /*
     * Name of the IDGapsTable.
     */
    protected String name = null;

    /*
     * In memory representation of the IDGaps table. This is just a map from idNames to Gaps objects. The Gaps class is
     * defined at the end of this file.
     */
    private HashMap<String, Gaps> idGaps = null;

    private HashMap<String, Gaps> savedState = new HashMap<String, Gaps>();

    private HashMap<String, String> nextIdSequences = null;

    /**
     * ID column in the ID_GAPS table. This column is just a unique identifier.
     */
    private final static String ID_GAPS_ID = "GAPID";

    /**
     * ID_NAME column in the ID_GAPS table. This column has the name of the id that values are available for.
     */
    private final static String ID_GAPS_ID_NAME = "ID_NAME";

    /**
     * GAP_START column in the ID_GAPS table. This column has the next available value for the id in ID_NAME.
     */
    private final static String ID_GAPS_GAP_START = "GAP_START";

    /**
     * GAP_END column in the ID_GAPS table. This column has the last available value for the id in ID_NAME.
     */
    private final static String ID_GAPS_GAP_END = "GAP_END";

    /**
     * The String used to create the PreparedStatement that can write a row of IDGaps information. For DAOs that are for
     * databases that have JDBC classes implemented for them, this key will be used to initialize the PreparedStatement
     * object. For other DAOs, this key can be parsed in order to form a query that will work for non JDBC DAOs.
     */
    protected String preparedStatementKey = null;

    /*
     * Whether or not to "use up" the id gaps or to preserve them. true indicates that they should be used, false
     * indicates that they should be preserved. Defaults to false.
     */
    private boolean useGaps = false;

    /**
     * Keeps a running log of SQL statements that can be executed to undo anything that was done to the database by this
     * class.
     */
    protected LinkedList<String> undoLog = null;

    /**
     * Constructor for an IDGaps table that exists externally.
     *
     * @param schema Schema object this IDGapsTable is associated with
     * @param name   name of the IDGaps table (db table or external file).
     * @throws FatalDBUtilLibException if an error occurs
     */
    protected IDGapsTable(Schema schema, String name) throws FatalDBUtilLibException {
        synchronized (this) {
            this.schema = schema;
            this.nextIdSequences = null;
            if (name != null && name.length() > 0) {
                this.name = name;
                this.preparedStatementKey = prepStmtKey();
                // Since IDGaps tables are usually pretty small, just read the whole
                // thing into memory right off the bat.
                try {
                    readInTable();
                } catch (FatalDBUtilLibException e) {
                    String msg = "Error in IDGapsTable(Schema,name) constructor when"
                            + " reading in table.\nException message: " + e.getMessage();
                    throw new FatalDBUtilLibException(msg);
                }
            } else
                this.name = "";
        }
    }

    /**
     * An IDGaps table that will be constructed internally based on max ID + 1 as discerned from the id owner tables
     * defined in schema.
     *
     * @param schema Schema
     */
    public IDGapsTable(Schema schema) {
        synchronized (this) {
            this.schema = schema;
            this.name = "";
            this.nextIdSequences = null;
        }
    }

    /**
     * An IDGaps Constructor that sets up an IDGaps "table" based on a set of oracle sequences (one for each owned id in
     * the schema). There is no underlying table, but calls to IDGapsTable.nextId(idName) return unique id values from
     * the underlying database sequence. This approach is invalid for schemas with flatfile or xml dao's. It only works
     * with database dao's.
     *
     * @param schema    Schema
     * @param sequences HashMap
     * @throws FatalDBUtilLibException if an error occurs
     */
    public IDGapsTable(Schema schema, HashMap<String, String> sequences) throws FatalDBUtilLibException {
        synchronized (this) {
            this.schema = schema;
            this.name = "";

            if (!schema.dao.getType().equals(DBDefines.DATABASE_DAO)) {
                throw new FatalDBUtilLibException("ERROR in IDGapsTable constructor.  "
                        + "Cannot have an IDGapsTable based on database sequences unless the "
                        + "associated schema has a DB Data Access Object.");
            }

            if (sequences == null) {
                throw new FatalDBUtilLibException("ERROR in IDGapsTable constructor.  "
                        + "Called with HashMap<idName, dbSequence> == null");
            }

            boolean error = false;

            this.nextIdSequences = new HashMap<String, String>();
            for (String idName : sequences.keySet())
                this.nextIdSequences.put(idName.toUpperCase(), sequences.get(idName).toUpperCase());

            StringBuilder missingIDs = new StringBuilder();
            for (String ownedID : schema.ownedIDToTable.keySet())
                if (!this.nextIdSequences.containsKey(ownedID))
                    missingIDs.append(ownedID).append(' ');
            if (missingIDs.length() > 0) {
                DBDefines.ERROR_LOG
                        .add("ERROR in IDGapsTable constructor. NextID Sequences specified in the par file with parameter "
                                + schema.getName()
                                + "NextIdSequences"
                                + " does not include entries for ID(s) "
                                + missingIDs.toString() + '\n');
                error = true;
            }

            for (String ownedID : schema.ownedIDToTable.keySet())
                if (this.nextIdSequences.containsKey(ownedID)
                        && !schema.dao.sequenceExists(this.nextIdSequences.get(ownedID))) {
                    DBDefines.ERROR_LOG.add("ERROR in IDGapsTable constructor.  " + "Sequence "
                            + this.nextIdSequences.get(ownedID) + " does not exist in the database.  "
                            + "It can be created with a sql statement similar to:\n" + "CREATE SEQUENCE "
                            + this.nextIdSequences.get(ownedID) + " START WITH n;\n");
                    error = true;
                }

            if (error) {
                DBDefines.outputLogs();
            }
        }
    }

    public synchronized void close() {
        for (ArrayList<PreparedStatement> preparedStatements : this.preparedStatementPool.values()) {
            for (PreparedStatement preparedStatement : preparedStatements) {
                try {
                    preparedStatement.close();
                } catch (SQLException ex) {
                    // Ignore closing errors
                }
            }
        }
    }

    /**
     * Maintain some PreparedStatements usable by this object for use in multi-threaded environments.
     */
    private HashMap<Connection, ArrayList<PreparedStatement>> preparedStatementPool = new HashMap<Connection, ArrayList<PreparedStatement>>();

    /**
     * Retrieve a PreparedStatement for this object.
     *
     * @param connection Connection object used to create PreparedStatement objects
     * @return a PreparedStatement for this object
     */
    public synchronized PreparedStatement getPreparedStatement(Connection connection) throws SQLException {
        if (this.preparedStatementPool.get(connection) == null)
            this.preparedStatementPool.put(connection, new ArrayList<PreparedStatement>());
        if (this.preparedStatementPool.get(connection).size() == 0)
            this.preparedStatementPool.get(connection).add(connection.prepareStatement(this.preparedStatementKey));
        return this.preparedStatementPool.get(connection).remove(0);
    }

    /**
     * Release a PreparedStatement for future use by this object.
     *
     * @param connection        Connection preparedStatement is associated with
     * @param preparedStatement PreparedStatement to release for future use by this object.
     */
    public synchronized void releasePreparedStatement(Connection connection, PreparedStatement preparedStatement) {
        if (preparedStatement != null)
            this.preparedStatementPool.get(connection).add(preparedStatement);
    }

    /**
     * Set whether or not to use up the id gaps or to preserve them.
     *
     * @param useGaps boolean - use gaps: true, preserve gaps: false
     */
    public synchronized void setUseGaps(boolean useGaps) {
        this.useGaps = useGaps;
    }

    private synchronized void generateTable() throws FatalDBUtilLibException {
        if (this.nextIdSequences != null)
            return;

        long n = 1;
        Long big = new Long(Long.MAX_VALUE);
        this.idGaps = new HashMap<String, Gaps>();
        try {
            for (Map.Entry<String, Table> entry : this.schema.ownedIDToTable.entrySet()) {
                String idName = entry.getKey();
                Table table = entry.getValue();
                long max = Math.max(0, this.schema.dao.getMaxID(idName, table.name));

                Gaps gaps = new Gaps(new Long(n++), new Long(max + 1), big);

                // Put into HashMap.
                this.idGaps.put(idName, gaps);
            }
        } catch (FatalDBUtilLibException e) {
            String msg = "Error in IDGapsTable.generateTable().\nException " + "message: " + e.getMessage();
            throw new FatalDBUtilLibException(msg);
        }
    }

    /*
     * Read the IDGaps table into memory.
     * @throws FatalDBUtilLibException if an error occurs
     */
    private synchronized void readInTable() throws FatalDBUtilLibException {
        if (this.nextIdSequences != null)
            return;

        this.idGaps = new HashMap<String, Gaps>();
        this.undoLog = new LinkedList<String>();

        // Add a "delete all" type statement to the undoLog since if we needed
        // to undo anything done to the IDGaps table, it would be easiest to just
        // delete everything that was in it, and add back what had been in it
        // to start.
        this.undoLog.add("DELETE FROM " + this.name + ";");

        LinkedList<DAO.RowWithoutATable> rows = new LinkedList<DAO.RowWithoutATable>();
        try {
            // Read in the table and put the information into the idGaps HashMap.
            rows = this.schema.dao.executeSelectStatement("SELECT * FROM " + this.name);
        } catch (FatalDBUtilLibException e) {
            String msg = "ERROR in IDGapsTable.readInTable.\nException message: " + e.getMessage();
            throw new FatalDBUtilLibException(msg);
        }

        for (DAO.RowWithoutATable row : rows) {
            String idName = (String) row.getFromDataMap(ID_GAPS_ID_NAME);

            Gaps gaps = new Gaps(new Long(((BigDecimal) row.getFromDataMap(ID_GAPS_ID)).longValue()), new Long(
                    ((BigDecimal) row.getFromDataMap(ID_GAPS_GAP_START)).longValue()), new Long(((BigDecimal) row
                    .getFromDataMap(ID_GAPS_GAP_END)).longValue()));

            if (gaps.gapEnd.longValue() < gaps.gapStart.longValue()) {
                String error = "Error in IDGapsTable.readInTable.  " + "GAP_END (" + gaps.gapEnd
                        + ") is less than GAP_START (" + gaps.gapStart + ").  This is not allowed.";
                DBDefines.ERROR_LOG.add(error);
                throw new FatalDBUtilLibException(error);
            }

            // Put into HashMap.
            this.idGaps.put(idName, gaps);

            // Add an entry to the undoLog that would recreate the row
            // just read in.
            this.undoLog.add("INSERT INTO " + this.name + " (" + ID_GAPS_ID + ", " + ID_GAPS_ID_NAME + ", "
                    + ID_GAPS_GAP_START + ", " + ID_GAPS_GAP_END + ") VALUES (" + gaps.gapId + ", '" + idName + "', "
                    + gaps.gapStart + ", " + gaps.gapEnd + ");");
        }
    }

    /**
     * Returns the undo log (as a LinkedList of SQL statements) that when executed will restore the IDGaps table to the
     * state it was in before the instantiation of this IDGapsTable object.
     *
     * @return undo log (as SQL) that when executed will restore the IDGaps table to the state it was in before the
     * instantiation of this IDGapsTable object
     */
    public synchronized LinkedList<String> getUndoLog() {
        if (this.nextIdSequences != null || this.undoLog == null)
            return new LinkedList<String>();
        return this.undoLog;
    }

    /**
     * Create an internal copy of the current contents of the IDGapsTable object which can be restored later with the
     * restoreState() method. This method can be called after every database commit, then, anytime a database rollback
     * is executed, the method restoreState() can be called to restore the idgaps table to it's state at the time of the
     * previous successful commit.
     *
     * @throws FatalDBUtilLibException if an error occurs
     */
    public synchronized void saveState() throws FatalDBUtilLibException {
        if (this.nextIdSequences != null)
            return;

        if (this.idGaps == null) {
            try {
                generateTable();
            } catch (FatalDBUtilLibException e) {
                String msg = "Error in IDGapsTable.saveState when generating table." + "\nException message: "
                        + e.getMessage();
                throw new FatalDBUtilLibException(msg);
            }
        }
        this.savedState = new HashMap<String, Gaps>();
        for (String idname : this.idGaps.keySet())
            this.savedState.put(idname, this.idGaps.get(idname).clone());
    }

    /**
     * Restore the state of this IDGapsTable object to a state that was previously saved with the saveState() method.
     */
    public synchronized void restoreState() {
        this.idGaps = new HashMap<String, Gaps>();
        for (String idname : this.savedState.keySet())
            this.idGaps.put(idname, this.savedState.get(idname).clone());
    }

    /*
     * Get the next available id for idName.
     * @param idName id name to get the next available id for
     * @return next available id for idName; null if none available
     */
    public synchronized Long getNextId(String idName) {
        if ((idName == null) || (idName.length() == 0)) {
            DBDefines.ERROR_LOG.add("Error in IdGapsTable.getNextId: called with a null or"
                    + " length 0 idName. Returning null.");
            return null;
        }

        if (this.nextIdSequences != null) {
            String sequence = this.nextIdSequences.get(idName);
            if (sequence == null) {
                DBDefines.ERROR_LOG.add("ERROR: IDGapsTable.getNextId() called with " + " idName " + idName
                        + " which is not represented in the sequence HashMap.");
                return null;
            }
            try {
                return new Long(this.schema.dao.executeSelectFromSequence(sequence));
            } catch (FatalDBUtilLibException e) {
                String msg = "Error in IDGapsTable.getNextId.\nException" + " message: " + e.getMessage();
                DBDefines.ERROR_LOG.add(msg);
                return null;
            }
        }

        try {
            if (this.idGaps == null)
                generateTable();
        } catch (FatalDBUtilLibException e) {
            String msg = "Error in IdGapsTable.getNextId()\nException message: " + e.getMessage();
            DBDefines.ERROR_LOG.add(msg);
            return null;
        }

        Long nextId = null;
        String trimmedIdName = idName.trim().toUpperCase();

        // Different functions are called based on whether gaps are to be
        // used or not.
        if (this.useGaps)
            nextId = getNextIdUseGaps(trimmedIdName);
        else
            nextId = getNextIdPreserveGaps(trimmedIdName);

        if (nextId == null)
            DBDefines.ERROR_LOG.add("Error in IdGapsTable.getNextId(" + trimmedIdName + ") - no more"
                    + " id values available for " + trimmedIdName + ". Returning null.");

        return nextId;
    }

    /**
     * Get the next available id for idName using ids in "gaps". THIS IS NOT ACTUALLY FUNCTIONAL YET!
     *
     * @param idName id name to get the next available id for
     * @return next available id for idName; null if none available
     */
    private synchronized Long getNextIdUseGaps(String idName) {
        throw new java.lang.UnsupportedOperationException(
                "Method IDGapsTable.getNextIdUseGaps(String idName) not implemented.");
    }

    /**
     * Get the next available id for idName not using "gaps".
     *
     * @param idName id name to get the next available id for
     * @return next available id for idName; null if none available
     */
    private synchronized Long getNextIdPreserveGaps(String idName) {
        Long nextId = null;

        // Check if this value has been returned for future use and return it if it has
        if (this.returnedValues.get(idName) != null) {
            Long idValue = this.returnedValues.get(idName).remove(0);
            if (this.returnedValues.get(idName).size() == 0)
                this.returnedValues.remove(idName);
            return idValue;
        }

        // Check the in memory structure (idGaps HashMap) to see if an entry
        // exists for idName.
        Gaps gaps = this.idGaps.get(idName);

        // Not in memory
        if (gaps == null)
            return null;

        // If gaps.gapStart is greater than gapEnd, then we are out of ids.
        if (gaps.gapStart.longValue() > gaps.gapEnd.longValue())
            DBDefines.ERROR_LOG.add("Error in IdGapsTable.getNextIdPreserveGaps: out of ids!");
        else {
            nextId = gaps.gapStart;

            // Increment the next available id value by 1 since we just
            // used the one that was there - thus it is no longer available.
            gaps.gapStart = new Long(gaps.gapStart.intValue() + 1);
        }
        return nextId;
    }

    /**
     * Return this idgaps table's name.
     *
     * @return this idgaps table's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * This HashMap from remap source -> idname -> original id -> current id stores idgaps table values that were
     * retrieved and then not used by the calling program. These can be reused by future calls.
     */
    private HashMap<String, ArrayList<Long>> returnedValues = new HashMap<String, ArrayList<Long>>();

    /**
     * Make available values that were retrieved from the idgaps table but then could not be used for one reason or
     * another.
     *
     * @param unusedValues HashMap from idname -> [list of ids retrieved for that idname]
     */
    public void returnUnusedValues(HashMap<String, ArrayList<Long>> unusedValues) {
        synchronized (this.returnedValues) {
            for (String idName : unusedValues.keySet()) {
                if (this.returnedValues.get(idName) == null)
                    this.returnedValues.put(idName, new ArrayList<Long>());
                this.returnedValues.get(idName).addAll(unusedValues.get(idName));
            }
        }
    }

    /*
     * Writes the in memory IDGaps table out to whatever DAO this IDGapsTable's Schema has. This just does the initial
     * write - if you are using a database, you would still need to commit it or if you are writing to a file you would
     * still need to flush the file.
     * @throws FatalDBUtilLibException if an error occurs
     */
    public synchronized void write() throws FatalDBUtilLibException {
        // Walk through the idNames in the idGaps HashMap add update the
        // gap information in the database to reflect the new gap information
        // that is in memory for each of those id names.
        if (this.name != null && this.name.length() > 0) {
            try {
                for (String idName : this.idGaps.keySet()) {
                    Gaps gaps = this.idGaps.get(idName);

                    this.schema.dao.writeIDGapsRow(this, new Object[]{gaps.gapStart, gaps.gapEnd, gaps.gapId});
                }
            } catch (FatalDBUtilLibException e) {
                String errorMessage = e.getMessage();

                // If updating the IDGaps table generates an error, see if the
                // error is a unique constraint violation. If it is, ignore it.
                // Otherwise, report it.
                errorMessage = errorMessage.toUpperCase();
                // Ignore unique constraint violation messages.
                if (!((errorMessage.indexOf("UNIQUE") > 0) && (errorMessage.indexOf("CONSTRAINT") > 0) && (errorMessage
                        .indexOf("VIOLATED") > 0))) {
                    errorMessage += "\nCalled from IDGapsTable.writeToDB";
                    DBDefines.ERROR_LOG.add(errorMessage);
                } else {
                    String msg = "Error in IDGapsTable.write.\nException" + " message: " + e.getMessage();
                    throw new FatalDBUtilLibException(msg);
                }
            }
        }
    }

    /**
     * Returns a String that can be used either to create a PreparedStatement object for JDBC enabled DAOs or that can
     * be parsed by non JDBC enabled DAOs to write out a Row of IDGaps information.
     *
     * @return a String that can be used to write out a Row of IDGaps information
     */
    private String prepStmtKey() {
        return "UPDATE " + this.name + " SET " + ID_GAPS_GAP_START + " = ?, " + ID_GAPS_GAP_END + " = ? WHERE "
                + ID_GAPS_ID + " = ?";
    }

    public String getPreparedStatementKey() {
        return this.preparedStatementKey;
    }

    /**
     * Class to encapsulate information associated with id names. This class mostly exists to facilitate accessing this
     * information in a readable fashion instead of just storing everyting in an array.
     */
    class Gaps implements Cloneable {
        /**
         * The id for this "gap" as found in the IDGaps table.
         */
        protected Long gapId = null;

        /**
         * Where the "gap" starts. This is the id that can be used next, not the last one used.
         */
        protected Long gapStart = null;

        /**
         * Where the "gap" ends.
         */
        protected Long gapEnd = null;

        /**
         * Constructor.
         *
         * @param gapId    id for this "gap" as found in the IDGaps table
         * @param gapStart where this "gap" starts
         * @param gapEnd   where this "gap" ends
         */
        protected Gaps(Long gapId, Long gapStart, Long gapEnd) {
            this.gapId = gapId;
            this.gapStart = gapStart;
            this.gapEnd = gapEnd;
        }

        @Override
        public Gaps clone() {
            return new Gaps(this.gapId, this.gapStart, this.gapEnd);
        }
    }
}
