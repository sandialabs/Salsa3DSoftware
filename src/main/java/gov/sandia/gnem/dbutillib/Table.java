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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import gov.sandia.gnem.dbutillib.dao.DAODatabase;
import gov.sandia.gnem.dbutillib.jaxb.ObjectFactory;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * This class handles the encapsulation of Table metadata for a table within the database. A Table object maintains an
 * array of Column objects, one for each column in the corresponding table.
 *
 * @author Sandy Ballard and Jennifer Lewis
 * @version 1.0
 */
public class Table {
    /**
     * Schema this Table belongs to.
     */
    protected Schema schema = null;

    /**
     * Table name.
     */
    protected String name = null;

    /**
     * Table type. Each table has a name, but tables must also be of a certain table type - a table type that is defined
     * in the TableDefinition table associated with the Schema this Table is associated with.
     */
    protected String tableType = null;

    /**
     * The data source for data in this Table. For data base tables, data source is the username of the database account
     * where the table resides. For data from files, the datasource is the absolute pathname of the directory where the
     * file that contains the data resides.
     */
    protected String dataSource;

    /**
     * If this table owns an ID (is an ID owner), then ownedID is the name of the ID that this table owns. Whether or
     * not a table is an ID owner is defined in the TableDefinition table associated with the Schema this Table is
     * associated with.
     */
    protected String ownedID = null;

    /**
     * foreignKeys is a list of the Column indices of all the foreign keys in this Table (all of the columns in this
     * table that point to columns in other tables). These foreign keys are specified in the TableDefinition table
     * associated with the Schema this Table is associated with. Columns are included regardless of whether or not they
     * are involved in relationships. The foreign key name is the name of the ownedID in the ID Owner table that this
     * Table's foreign key points to, which is not necessarily the same name as the name of the column in this table.
     * OwnedID is included in this list if not null (if this Table is an ID owner table).
     */
    protected int[] foreignKeys = null;

    /**
     * The indices of the columns in this table that are foreign keys involved in Relationships. These are columns whose
     * idLink field in the is not null in the TableDefinition table associated with the Schema this Table is associated
     * with. If this table is an ID Owner table, it's ownedID will not be in this list.
     */
    protected int[] idLinks = null;

    /**
     * A key in this map is the name of a column in another table that columns in this table point to and the value is a
     * list of column indexes is this table that point to that column in the other table. For example, in the origin
     * table, the mbid, msid, and mlid columns all refer to the magid column in the netmag table. For a schema that has
     * an origin table related to a netmag table with relationships defined between mbid and magid, msid and magid, and
     * mlid and magid, idLinkMap will have an entry that looks like:
     * <p>
     * magid-> 16, 18, 20 <br>
     * where 16 is mbid's index, 18 is mlid's index, and 20 is msid's index in the origin table
     * <p>
     * If this table is an ID Owner table, it's ownedID will not be an element of the keyset.
     */
    protected HashMap<String, LinkedList<Integer>> idLinkMap = new HashMap<String, LinkedList<Integer>>();

    /**
     * If this table is an ID Owner, then this method will return a HashMap whose keys are the other Tables in the
     * schema that have this Table's ownedID as a foreign key and whose values are a LinkedList of columns in that table
     * that make up the foreign key that point to this Table's ownedID.
     */
    protected HashMap<Table, LinkedList<String>> linkedTables = new HashMap<Table, LinkedList<String>>();

    /**
     * uniqueKeys is the set of column indices in this table for columns that together must be unique in this Table.
     */
    protected int[] uniqueKeys = new int[0];

    /**
     * uniqueKeyString is a comma separated list of the column names that together must be unique in this table.
     * Uppercase. Guaranteed to be consistent with uniqueKeys int[] defined above.
     */
    protected String uniqueKeyString = "";

    /**
     * primaryKeys is the set of column indexes in this table for columns that together constitute the primary key for
     * this table.
     */
    protected int[] primaryKeys = null;

    /**
     * Array of Column objects associated with this Table.
     */
    protected Column[] columns = null;

    /**
     * Map of column names to the index of that Column object in the columns array.
     */
    protected HashMap<String, Integer> columnNames = null;

    /**
     * Names of the columns that make up all of the components of a row name.
     */
    private String[] rowNameComponents = null;

    /**
     * A sql where clause that includes all the columns that make up the primary key for this table. Example: WHERE
     * COL_A=? AND COL_B=? AND COL_C=? The columns that make up the primary key come from the table definition table,
     * column_type column.
     */
    private String primaryKeyWhereClause = null;

    /**
     * An insert SQL statement for this Table where the statement adheres to the reusability conventions followed by
     * java.sql's PreparedStatement class. The insert statement will be of the form: <BR>
     * <code>INSERT INTO tableName (<commma separated list of Table's
     * columns>) VALUES (<values to insert corresponding to and in the same order
     * as the Table's columns as listed after tableName>)</code>
     */
    protected String insertStatement;

    /**
     * An update SQL statement for this Table where the statement adheres to the reusability conventions followed by
     * java.sql's PreparedStatement class. This statement only works for ID Owner tables. The update statement will be
     * of the form: <BR>
     * <code>UPDATE table_name SET col1=?, col2=?, ..., coln=? WHERE
     * ownedID=?</code> <BR>
     * where the ownedID column name is not included in the col1 - coln column list. ownedIDs cannot be modified since
     * there are many other rows in the schema that have ownedID as a foreign key.
     */
    protected String updateStatement;

    /**
     * A delete SQL statement for this Table where the statement adheres to the reusability conventions followed by
     * java.sql's PreparedStatement class. If this Table is an ID owner table, then the statement will be of the form: <BR>
     * <code>DELETE FROM tableName WHERE ownedID = ?</code> <BR>
     * If this Table is not an ID owner table, but has unique keys, the statement will be of the form: <BR>
     * <code>DELETE FROM tableName WHERE uniqueKey1 = ? AND uniqueKey2 = ?
     * </code> <BR>
     * for as many unique keys as this table has. <BR>
     * If this table is not an ID owner table, and it does not have any unique keys, then the delete statement will be
     * of the form: <BR>
     * <code>DELETE FROM tableName WHERE columnName1 = ? AND columnName2 = ?
     * </code> <BR>
     * for all of the columns in this Table.
     */
    protected String deleteStatement;

    /**
     * Index into the Column array that for the LDDATE column.
     */
    protected int indexOfLDDATE = -1;

    /*
     * Generic alert! Sandy, why do we have an AUTH index? I am sure there is a good reason, but I just can't remember
     * it off hand.
     */
    /**
     * Index into the Column array that for the LDDATE column.
     */
    protected int indexOfAUTH = -1;

    /**
     * Consistent way to begin a log message for this class.
     */
    private final static String log = "TABLE";

    private String uri = null;

    /**
     * Constructor.
     *
     * @param name      name of the Table to be created
     * @param tableType type of the Table to be created
     * @param uKeys     if this string is empty, equal to "false" or equal to "0", then no unique keys are set. If the
     *                  string is anything else then unique keys are set to the unique keys specified in the table definition table.
     * @param schema    Schema this table belongs to
     * @throws FatalDBUtilLibException if an error occurs
     */
    protected Table(String name, String tableType, String uKeys, Schema schema) throws FatalDBUtilLibException {
        // Call the Table constructor with no Column information.
        this(name, tableType, uKeys, schema, null);
    }

    /**
     * Constructor.
     *
     * @param name      name of the Table to be created
     * @param tableType type of the Table to be created
     * @param uKeys     if this string is empty, equal to "false" or equal to "0", then no unique keys are set. If the
     *                  string is anything else then unique keys are set to the unique keys specified in the table definition table.
     * @param schema    Schema this table belongs to
     * @param columns   array of Columns that belong to this table; if columns is null, then the Column information will
     *                  be gotten out of the schema's table definition table
     * @throws FatalDBUtilLibException if an error occurs
     */
    protected Table(String name, String tableType, String uKeys, Schema schema, Column[] columns)
            throws FatalDBUtilLibException {
        // Make sure all of the parameters are more or less what they are
        // expected to be.
        if ((name == null) || (tableType == null) || (schema == null) || (name.length() == 0)
                || (tableType.length() == 0)) {
            DBDefines.ERROR_LOG.add(log + " Error creating table with the" + " following parameters - name: " + name
                    + " tableType: " + tableType + " uKeys: " + uKeys + " schema: " + schema + ". Table not created.");
            return;
        }

        this.schema = schema;
        this.tableType = tableType;
        this.name = name;

        // If no Column information was in columns, get the Column information
        // out of schema's table definition table.
        if (columns == null || columns.length == 0)
            columns = schema.tableDefinitionTable.getColumns(this.tableType);

        this.columns = columns;

        /***********************************************************************
         * Sandy, above we just generate an error and return if we cannot create a table if the parameters are
         * "not good", while here we throw an exception that could potentially halt the entire program. I think we might
         * need to throw an exception above as well instead of just returning since we are not actually creating a table
         * before returning in the case above.
         ***********************************************************************/
        // No columns found for a table is odd - generate a warning.
        if (this.columns == null || this.columns.length == 0) {
            throw new FatalDBUtilLibException("FATAL ERROR in Table " + "constructor. Unable to create table " + name
                    + " of type " + tableType + ".  No table definition information found for this table.\n");
        }

        // Create the columnNames HashMap. (Map of column names to the index
        // of that Column object in the columns array.) If this table is an
        // ID owner table, set up ownedID.
        columnNames = new HashMap<String, Integer>(columns.length);
        for (int i = 0; i < columns.length; i++) {
            columnNames.put(columns[i].name, new Integer(i));
            // Set up ownedID
            if (columns[i].ownedID)
                ownedID = columns[i].name;
        }

        // set the dataSource for this table. either the database account
        // (username) or the name of the directory where the file resides.
        if (schema.dao.getType().equals(DBDefines.DATABASE_DAO)) {
            int i = name.indexOf('.');
            if (i > 0)
                dataSource = name.substring(0, i).toUpperCase();
            else
                dataSource = ((DAODatabase) schema.dao).getUserName().toUpperCase();
        } else if (schema.dao.getType().equals(DBDefines.XML_DAO) || schema.dao.getType().equals(DBDefines.FF_DAO))
            dataSource = (new File(name)).getAbsolutePath();
        else
            dataSource = "?";

        // create an int[] of the indexes of the columns that are part of the primary key
        int npk = 0;
        for (int i = 0; i < columns.length; i++)
            if (columns[i].columnType.equalsIgnoreCase("primary key"))
                ++npk;

        primaryKeys = new int[npk];
        npk = 0;
        for (int i = 0; i < columns.length; i++)
            if (columns[i].columnType.equalsIgnoreCase("primary key"))
                primaryKeys[npk++] = i;

        // create the primaryKeyWhereClause
        primaryKeyWhereClause = "";
        String delim = "WHERE ";
        for (int i = 0; i < primaryKeys.length; i++) {
            primaryKeyWhereClause += delim + columns[primaryKeys[i]].name + "=?";
            delim = " AND ";
        }

        // uKeys = uKeys.trim().toLowerCase().replaceAll(" ", "");
        uKeys = DBDefines.removeExtraSpaces(uKeys.trim().toLowerCase());
        if (!uKeys.equals("false") && !uKeys.equals("0") && !uKeys.equals("off")) {
            int n = 0;
            for (Column column : columns)
                if (column.columnType.equals("unique key"))
                    ++n;
            uniqueKeys = new int[n];
            if (n > 0) {
                n = 0;
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < columns.length; i++)
                    if (columns[i].columnType.equals("unique key")) {
                        uniqueKeys[n++] = i;
                        s.append(',').append(columns[i].name);
                    }
                uniqueKeyString = s.deleteCharAt(0).toString();
            }
        }

        // a limitation of current set up is that a Table with a compound primary key
        // may not have unique keys. The primary key will be unique, but additional
        // unique keys may not be specified.
        if (ownedID != null && primaryKeys.length > 1 && uniqueKeys.length > 0) {
            boolean same = true;
            if (primaryKeys.length == uniqueKeys.length) {
                HashSet<Integer> pk = new HashSet<Integer>();
                for (int i = 0; i < primaryKeys.length; i++)
                    pk.add(new Integer(primaryKeys[i]));
                for (int i = 0; i < uniqueKeys.length; i++)
                    if (!pk.contains(new Integer(uniqueKeys[i])))
                        same = false;
            }
            if (same) {
                uniqueKeys = new int[0];
            } else {
                StringBuilder msg = new StringBuilder("FATAL ERROR in Table constructor.  Table name = " + name
                        + ", table type = " + tableType + "\n" + "has compound primary key [");
                for (int i = 0; i < primaryKeys.length; i++) {
                    if (i > 0)
                        msg.append(",");
                    msg.append(columns[primaryKeys[i]].name);
                }
                msg.append("] and unique keys " + uKeys);
                msg.append(" were specified.  Unique keys may not be specified for a Table ");
                msg.append("with compound primary key.");
                throw new FatalDBUtilLibException(msg.toString());
            }
        }

        indexOfLDDATE = getColumnIndex("LDDATE");

        indexOfAUTH = getColumnIndex("AUTH");

        // Initialize the insert, update, and delete statement strings.
        initInsertStatement();
        initUpdateStatement();
        initDeleteStatement();
    }

    /**
     * Close method that closes all the sql PreparedStatements owned by this class.
     */
    public void close() {
        ArrayList<PreparedStatement> allPreparedStatements = new ArrayList<PreparedStatement>();
        for (ArrayList<PreparedStatement> preparedStatements : this.insertPreparedStatementPool.values())
            allPreparedStatements.addAll(preparedStatements);
        for (ArrayList<PreparedStatement> preparedStatements : this.updatePreparedStatementPool.values())
            allPreparedStatements.addAll(preparedStatements);
        for (ArrayList<PreparedStatement> preparedStatements : this.deletePreparedStatementPool.values())
            allPreparedStatements.addAll(preparedStatements);
        for (PreparedStatement preparedStament : allPreparedStatements) {
            try {
                preparedStament.close();
            } catch (SQLException ex) {
                // Ignore closing errors
            }
        }
    }

    /**
     * Generate an insert statement string that will insert a Row into this Table where the statement adheres to the
     * reusability conventions followed by java.sql's PreparedStatement class. The insert statement will be of the form: <BR>
     * <code>INSERT INTO tableName (<commma separated list of Table's
     * columns>) VALUES (<values to insert corresponding to and in the same order
     * as the Table's columns as listed after tableName>)</code>
     */
    private void initInsertStatement() {
        StringBuilder key = new StringBuilder("INSERT INTO ");
        key.append(name + " (");

        // Get the column names.
        for (int i = 0; i < columns.length; i++)
            key.append(columns[i].name + ", ");

        // Remove the last comma and space.
        key.setLength(key.length() - 2);

        key.append(") VALUES (");

        // Add ? for the values so they can be filled in later.
        for (int i = 0; i < columns.length; i++)
            key.append("?, ");

        // Remove the last comma and space.
        key.setLength(key.length() - 2);
        key.append(")");

        insertStatement = key.toString();
    }

    /**
     * Return the key used to create insert prepared statements for this Table.
     *
     * @return the key used to create insert prepared statements for this Table.
     */
    public String getInsertPreparedStatementKey() {
        return this.insertStatement;
    }

    /**
     * Populate a PreparedStatement that can be used to insert the specified row in the database.
     *
     * @param row               row to use to populate the PreparedStatement
     * @param preparedStatement PreparedStatement to be populated with information to insert the specified row in the
     *                          database
     * @throws FatalDBUtilLibException if an error occurs
     */
    public void populateInsertPreparedStatement(Row row, PreparedStatement preparedStatement)
            throws FatalDBUtilLibException {
        Object[] rowValues = row.getValues();
        for (int i = 0; i < this.columns.length; i++)
            this.columns[i].insertValue(preparedStatement, i + 1, rowValues[i]);
    }

    /**
     * Maintain some insert PreparedStatements usable by this object for use in multi-threaded environments.
     */
    private HashMap<Connection, ArrayList<PreparedStatement>> insertPreparedStatementPool = new HashMap<Connection, ArrayList<PreparedStatement>>();

    /**
     * Retrieve an insert PreparedStatement for this object.
     *
     * @param connection Connection object used to create PreparedStatement objects
     * @return an insert PreparedStatement for this object
     */
    public PreparedStatement getInsertPreparedStatement(Connection connection) throws SQLException {
        return getPreparedStatement(connection, this.insertPreparedStatementPool, this.insertStatement);
    }

    /**
     * Release an insert PreparedStatement for future use by this object.
     *
     * @param connection        Connection preparedStatement is associated with
     * @param preparedStatement PreparedStatement to release for future use by this object.
     */
    public void releaseInsertPreparedStatement(Connection connection, PreparedStatement preparedStatement) {
        releasePreparedStatement(connection, preparedStatement, this.insertPreparedStatementPool);
    }

    /*
     * Note: The comments say the update is based on the ownedid, but it appears to be on the primary key - the primary
     * key is what it should be based on, so, update comments.
     */

    /**
     * Generate an update statement string that will update a row in this Table where the statement adheres to the
     * reusability conventions followed by java.sql's PreparedStatement class. This statement only works for ID Owner
     * tables. The update statement will be of the form: <BR>
     * <code>UPDATE table_name SET col1=?, col2=?, ..., coln=? WHERE
     * ownedID=?</code> <BR>
     * where the ownedID column name is not included in the col1 - coln column list. ownedIDs cannot be modified since
     * there are many other rows in the schema that have ownedID as a foreign key.
     */
    private void initUpdateStatement() {
        StringBuilder key = new StringBuilder("UPDATE ");
        key.append(name);
        key.append(" SET ");

        // Get the names of all columns except primary key columns
        for (int i = 0; i < columns.length; i++)
            if (!columns[i].columnType.equalsIgnoreCase("primary key"))
                key.append(columns[i].name + "=?, ");

        // Remove the last comma and space.
        key.setLength(key.length() - 2);

        key.append(" " + primaryKeyWhereClause);
        updateStatement = key.toString();
    }

    /**
     * Return the key used to create update prepared statements for this Table.
     *
     * @return the key used to create update prepared statements for this Table.
     */
    public String getUpdatePreparedStatementKey() {
        return this.updateStatement;
    }

    /**
     * Populate a PreparedStatement that can be used to update the specified row in the database.
     *
     * @param row               row to use to populate the PreparedStatement
     * @param preparedStatement PreparedStatement to be populated with information to update the specified row in the
     *                          database
     * @throws FatalDBUtilLibException if an error occurs
     */
    public void populateUpdatePreparedStatement(Row row, PreparedStatement preparedStatement)
            throws FatalDBUtilLibException {
        int p = 1;
        Object[] rowValues = row.getValues();
        for (int i = 0; i < this.columns.length; i++)
            if (!this.columns[i].getType().equalsIgnoreCase("primary key"))
                this.columns[i].insertValue(preparedStatement, p++, rowValues[i]);

        for (int i = 0; i < this.columns.length; i++)
            if (this.columns[i].getType().equalsIgnoreCase("primary key"))
                this.columns[i].insertValue(preparedStatement, p++, rowValues[i]);
    }

    /**
     * Maintain some update PreparedStatements usable by this object for use in multi-threaded environments.
     */
    private HashMap<Connection, ArrayList<PreparedStatement>> updatePreparedStatementPool = new HashMap<Connection, ArrayList<PreparedStatement>>();

    /**
     * Retrieve an update PreparedStatement for this object.
     *
     * @param connection Connection object used to create PreparedStatement objects
     * @return an update PreparedStatement for this object
     */
    public PreparedStatement getUpdatePreparedStatement(Connection connection) throws SQLException {
        return getPreparedStatement(connection, this.updatePreparedStatementPool, this.updateStatement);
    }

    /**
     * Release an insert PreparedStatement for future use by this object.
     *
     * @param connection        Connection preparedStatement is associated with
     * @param preparedStatement PreparedStatement to release for future use by this object.
     */
    public void releaseUpdatePreparedStatement(Connection connection, PreparedStatement preparedStatement) {
        releasePreparedStatement(connection, preparedStatement, this.updatePreparedStatementPool);
    }

    /**
     * Generate a delete statement string that will delete a Row from this Table where the statement adheres to the
     * reusability conventions followed by java.sql's PreparedStatement class. If this Table is an ID owner table, then
     * the statement will be of the form: <BR>
     * <code>DELETE FROM tableName WHERE ownedID = ?</code> <BR>
     * If this Table is not an ID owner table, but has unique keys, the statement will be of the form: <BR>
     * <code>DELETE FROM tableName WHERE uniqueKey1 = ? AND uniqueKey2 = ?
     * </code> <BR>
     * for as many unique keys as this table has. <BR>
     * If this table is not an ID owner table, and it does not have any unique keys, then the delete statement will be
     * of the form: <BR>
     * <code>DELETE FROM tableName WHERE columnName1 = ? AND columnName2 = ?
     * </code> <BR>
     * for all of the columns in this Table.
     */
    private void initDeleteStatement() {
        deleteStatement = "DELETE FROM " + name + " " + primaryKeyWhereClause;
    }

    /**
     * Return the key used to create delete prepared statements for this Table.
     *
     * @return the key used to create delete prepared statements for this Table.
     */
    public String getDeletePreparedStatementKey() {
        return this.deleteStatement;
    }

    /**
     * Populate a PreparedStatement that can be used to delete the specified row from the database.
     *
     * @param row               row to use to populate the PreparedStatement
     * @param preparedStatement PreparedStatement to be populated with information to delete the specified row from the
     *                          database
     * @throws FatalDBUtilLibException if an error occurs
     */
    public void populateDeletePreparedStatement(Row row, PreparedStatement preparedStatement)
            throws FatalDBUtilLibException {
        int p = 1;
        Object[] rowValues = row.getValues();
        for (int i = 0; i < this.columns.length; i++)
            if (this.columns[i].getType().equalsIgnoreCase("primary key"))
                this.columns[i].insertValue(preparedStatement, p++, rowValues[i]);
    }

    /**
     * Maintain some delete PreparedStatements usable by this object for use in multi-threaded environments.
     */
    private HashMap<Connection, ArrayList<PreparedStatement>> deletePreparedStatementPool = new HashMap<Connection, ArrayList<PreparedStatement>>();

    /**
     * Retrieve an delete PreparedStatement for this object.
     *
     * @param connection Connection object used to create PreparedStatement objects
     * @return an delete PreparedStatement for this object
     */
    public PreparedStatement getDeletePreparedStatement(Connection connection) throws SQLException {
        return getPreparedStatement(connection, this.deletePreparedStatementPool, this.deleteStatement);
    }

    /**
     * Release an delete PreparedStatement for future use by this object.
     *
     * @param connection        Connection preparedStatement is associated with
     * @param preparedStatement PreparedStatement to release for future use by this object.
     */
    public void releaseDeletePreparedStatement(Connection connection, PreparedStatement preparedStatement) {
        releasePreparedStatement(connection, preparedStatement, this.deletePreparedStatementPool);
    }

    /**
     * Generic preparedStatement retrieval (or creation if there are none to retrieve) that can be used for insert,
     * update, and delete prepared statement pools
     *
     * @param connection   key into the statementPool hashmap
     * @param statmentPool map from connection to list of PreparedStatements associated with that connection
     * @param statementKey sql used to create the prepared statement if a new one is needed
     * @return retrieved (or created) prepared statement
     * @throws SQLException
     */
    private PreparedStatement getPreparedStatement(Connection connection,
                                                   HashMap<Connection, ArrayList<PreparedStatement>> statmentPool, String statementKey) throws SQLException {
        synchronized (statmentPool) {
            if (statmentPool.get(connection) == null)
                statmentPool.put(connection, new ArrayList<PreparedStatement>());
            if (statmentPool.get(connection).size() == 0)
                statmentPool.get(connection).add(connection.prepareStatement(statementKey));
            return statmentPool.get(connection).remove(0);
        }

    }

    /**
     * Generic PreparedStatement release method
     *
     * @param connection        Connection preparedStatement is associated with
     * @param preparedStatement PreparedStatement to release for future use by this object.
     * @param statementPool     map from connection to list of PreparedStatements associated with that connect
     */
    private void releasePreparedStatement(Connection connection, PreparedStatement preparedStatement,
                                          HashMap<Connection, ArrayList<PreparedStatement>> statementPool) {
        if (preparedStatement != null)
            synchronized (statementPool) {
                statementPool.get(connection).add(preparedStatement);
            }
    }

    /**
     * Specify the format of the LDDATE field in this table.
     *
     * @param dateFormat the date format string. See java.text.SimpleDateFormat API for valid format strings.
     */
    public void setLDDATEFormat(String dateFormat) {
        if (indexOfLDDATE >= 0 && dateFormat != null && dateFormat.length() > 0)
            columns[indexOfLDDATE].setDateFormat(dateFormat);
    }

    /**
     * Specify the format of all fields of type Date in this table.
     *
     * @param dateFormat the date format string. See java.text.SimpleDateFormat API for valid format strings.
     */
    public void setDateFormat(String dateFormat) {
        if (dateFormat != null && dateFormat.length() > 0)
            for (int i = 0; i < columns.length; i++)
                if (columns[i].javaType == DBDefines.DATE)
                    columns[i].setDateFormat(dateFormat);
    }

    /**
     * Return a specific Column based on the column's name.
     *
     * @param columnName the name of the column to be returned
     * @return a Column object that has the name in columName; null if that Column does not exist
     */
    public Column getColumn(String columnName) {
        // Make sure that columnName is a String that could be a valid
        // column name.
        if ((columnName == null) || (columnName.length() == 0)) {
            DBDefines.WARNING_LOG.add(log + " In getColumn(), columnName: " + columnName
                    + " is not a valid column name.");
            return null;
        }

        if (DBDefines.convertToUpperCase) {
            columnName = columnName.toUpperCase();
        }

        Integer index = this.columnNames.get(columnName);
        // Column could not be found.
        if (index == null)
            return null;

        return this.columns[index.intValue()];
    }

    /**
     * Return the index into the Column array to a specific Column based on the Column's name.
     *
     * @param columnName the name of the Column who's index is to be returned
     * @return an integer that is an index into the Column array to a specific Column based on the name in columnName;
     * -1 if that column does not exist
     */
    public int getColumnIndex(String columnName) {
        // Make sure that columnName is a String that could be a valid
        // column name.
        if ((columnName == null) || (columnName.length() == 0)) {
            DBDefines.WARNING_LOG.add(log + " In getColumnIndex(), columName: " + columnName
                    + " is not a valid column name.");
            return -1;
        }

        if (DBDefines.convertToUpperCase) {
            columnName = columnName.toUpperCase();
        }
        Integer col = this.columnNames.get(columnName);

        // Column not found.
        if (col == null)
            return -1;

        return col.intValue();
    }

    /**
     * Given a List of (String)columnNames, this method returns an int[] array which contains the indexes of the
     * corresponding Column objects in this table.
     *
     * @param columnNames the (String) column names whose indexes are being requested
     * @return an int[] containing the indexes of the columns in this table that correspond to the names in columnNames
     */
    public int[] getColumnIndexes(List<String> columnNames) {
        int[] indexes = new int[columnNames.size()];
        int i = 0;
        for (String columnName : columnNames)
            indexes[i++] = getColumnIndex(columnName);
        return indexes;
    }

    /**
     * Return the index of the column in this table that corresponds to the AUTH.
     *
     * @return the index of the column in this table that corresponds to the AUTH. Returns -1 if this table does not
     * have column with the name AUTH.
     */
    public int getIndexOfAUTH() {
        return this.indexOfAUTH;
    }

    /**
     * Return the index of the column in this table that corresponds to the LDDATE.
     *
     * @return the index of the column in this table that corresponds to the LDDATE. Returns -1 if this table does not
     * have column with the name LDDATE.
     */
    public int getIndexOfLDDATE() {
        return indexOfLDDATE;
    }

    /**
     * Return a Format object that will appropriately format the LDDATE field for this table.
     *
     * @return a Format object that will appropriately format the LDDATE field for this table. Returns null if this
     * table does not have LDDATE column.
     */
    public SimpleDateFormat getFormatOfLDDATE() {
        if (indexOfLDDATE < 0)
            return null;
        return (SimpleDateFormat) columns[indexOfLDDATE].numberFormatter;
    }

    /**
     * Return this Table's name.
     *
     * @return this Table's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return a reference to the schema this table belongs to.
     *
     * @return a reference to the schema this table belongs to
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * Get all the Relationship objects that start from this table.
     *
     * @return a Set of Relationship objects, all of which originate from this Table object
     */
    public Set<Relationship> getRelationships() {
        LinkedHashMap<Table, Relationship> map = schema.tableGraph.get(this);
        return new LinkedHashSet<Relationship>(map.values());
    }

    /**
     * Return this Table's type.
     *
     * @return this Table's type
     */
    public String getTableType() {
        return this.tableType;
    }

    /**
     * Return the data source for data in this Table. For data base tables, data source is the username of the database
     * account where the table resides. For data from files, the datasource is the absolute pathname of the directory
     * where the file that contains the data resides.
     *
     * @return data source
     */
    public String getDataSource() {
        return dataSource;
    }

    /**
     * Return the URI of this table. If this Table comes from a schema with a DAO type of FF or XML, the URI will look
     * like 'file:/dir/file'. If the Table comes from a schema with DAO type of DB, the URI will look like
     * 'db:instance.username.table_name'. All database synonyms are resolved.
     *
     * @return String URI for this Table
     * @throws FatalDBUtilLibException if an error occurs
     */
    public String getURI() throws FatalDBUtilLibException {
        if (uri == null || uri.length() == 0)
            uri = schema.dao.getURI(this);
        return uri;
    }

    /**
     * if this table is an ID owner table, return the name of the ID that it owns. Otherwise, return null.
     *
     * @return the name of the ID owned by this table or null if this table does not own and ID.
     */
    public String getOwnedID() {
        return this.ownedID;
    }

    /**
     * Return whether or not this table is an idowner table.
     *
     * @return whether or not this table is an idowner table; if false is returned, this table is a non-idowner table
     */
    public boolean isIdOwner() {
        return (this.ownedID != null);
    }

    /**
     * Return this Table's Column objects.
     *
     * @return this Table's Column objects
     */
    public Column[] getColumns() {
        return this.columns;
    }

    /**
     * Retrieve the uniqueKeys in this table.
     *
     * @return a comma separated list of the unique keys in this table. Uppercase.
     */
    public String getUniqueKeys() {
        return uniqueKeyString;
    }

    /**
     * Retrieve column names for the unique keys in this table
     *
     * @return a String array of column names for the unique keys in this table
     */
    public String[] getUniqueKeyColumnNames() {
        String[] colNames = new String[uniqueKeys.length];
        for (int i = 0; i < colNames.length; i++)
            colNames[i] = columns[uniqueKeys[i]].getName();
        return colNames;
    }

    public int[] getUniqueKeyColumnIndexes() {
        return this.uniqueKeys;
    }

    /**
     * Retrieve column names for the primary keys in this table
     *
     * @return a String array of column names for the primary keys in this table
     */
    public String[] getPrimaryKeyColumnNames() {
        String[] colNames = new String[primaryKeys.length];
        for (int i = 0; i < colNames.length; i++)
            colNames[i] = columns[primaryKeys[i]].getName();
        return colNames;
    }

    /**
     * Retrieve the primary key in this table.
     *
     * @return a comma separated list of the primary keys in this table
     */
    public String getPrimaryKey() {
        StringBuilder k = new StringBuilder("");
        String comma = "";
        for (int i = 0; i < primaryKeys.length; i++) {
            k.append(comma + columns[primaryKeys[i]].name);
            comma = ",";
        }
        return k.toString();
    }

    /**
     * Return whether or not this table has a compound primary key (example: remark table has primary keys [commid,
     * lineno])
     *
     * @return whether or not this table has a compound primary key
     */
    public boolean hasCompoundPrimaryKey() {
        return (getPrimaryKeyColumnIndexes().length > 1);
    }

    /**
     * If this table is an ID Owner, then this method will return a HashMap whose keys are the other Tables in the
     * schema that have this Table's ownedID as a foreign key as keys into the HashMap and whose values are a LinkedList
     * of columns in that table that make up the foreign key that point to this Table's ownedID. If this table is a
     * non-ID Owner table, then returns null.
     *
     * @return HashMap
     */
    public HashMap<Table, LinkedList<String>> getLinkedTables() {
        return linkedTables;
    }

    /**
     * Set this table's linkedTables. If this table is an ID Owner, then linkedTables contains the other Tables in the
     * schema that have this Table's ownedID as a foreign key as keys into the HashMap whose values are a LinkedList of
     * columns in that table that make up the foreign key that point to this Table's ownedID.
     *
     * @param linkedTables tables that refer to this table's idowner
     */
    protected void setLinkedTables(HashMap<Table, LinkedList<String>> linkedTables) {
        this.linkedTables = linkedTables;
    }

    /**
     * Compare the unique keys that are set in this Table object with the unique keys set in the actual database table.
     * If the set of unique keys in the database table includes any columns that are not included in the unique keys
     * defined in this Table object, then throw an exception.
     *
     * @throws FatalDBUtilLibException if an error occurs
     */
    public void verifyUniqueKeyConstraint() throws FatalDBUtilLibException {
        // if (schema.dao.getType().equals("DB"))
        // {
        // if (databaseTable == null)
        // databaseTable = new DatabaseTable(schema, name.toUpperCase());
        //
        // if (databaseTable != null && databaseTable.uniqueKeys.length() > 0)
        // {
        // HashSet<String> cols = new HashSet<String>();
        // for (String uk : uniqueKeyString.split(",")) cols.add(uk);
        // for (String uk : databaseTable.uniqueKeys.split(","))
        // if (!cols.contains(uk))
        // throw new FatalDBUtilLibException(
        // "ERROR in Table.verifyUniqueKeyConstraint().\nTable = "
        // +name+"\n"
        // +"DBUtilLib Table object has unique keys "+uniqueKeyString+"\n"
        // +"but database table has unique keys     "
        // +databaseTable.uniqueKeys);
        // }
        // }
    }

    /**
     * Return this Table's row name components (array of Strings that are the names of the Columns that make up all of
     * the components of a row name).
     *
     * @return this Table's row name components
     */
    public String[] getRowNameComponents() {
        if (this.rowNameComponents == null)
            setRowNameComponents("");
        return this.rowNameComponents;
    }

    // /**
    // * Determine if two tables are equal. Equality is true when the Strings
    // * returned by the toString() method are equal. Note that this method
    // * compares MD5 hashes constructed using the toString() methods immediately
    // * following Table construction.
    // * @param otherTable the other table with which equality is to be evaluated.
    // * @return true if the two tables are equal
    // */
    // public boolean equals(Table otherTable)
    // {
    // return md5digest.isEqual(getId(), otherTable.getId());
    // }
    /*
     * Sandy, could you please fill in the part of the comments that explains what format the components variable is
     * supposed to be in?
     */

    /**
     * Initialize row name components - an array of Strings that are the names of the Columns that make up all of the
     * components of a row name - for this Table
     *
     * @param components INSERT FORMAT EXPECTED HERE; if null or "", the rowNameComponents array will be initialized
     *                   based on this Table's owned ID if this Table is an ID owner table, or based on the this Table's idLinks (foreign
     *                   keys involved in Relationships) if this Table is not an ID owner table
     */
    protected void setRowNameComponents(String components) {
        // Create "default" rowNameComponents if components is null or "".
        if (components == null || components.length() == 0) {
            // Base row name components on primaryKeys
            this.rowNameComponents = new String[primaryKeys.length];
            for (int i = 0; i < primaryKeys.length; i++)
                this.rowNameComponents[i] = columns[primaryKeys[i]].name;

            // Sort them so that if a user is ever looking at a primary key with
            // values that correspond to these column names and needs to know which
            // one is at what part of the String, they can just pick the String
            // apart alphabetically.
            Arrays.sort(this.rowNameComponents);
        } else {
            if (DBDefines.convertToUpperCase) {
                components = components.toUpperCase();
            }
            if (components.indexOf(",") >= 0)
                this.rowNameComponents = components.trim().split(",");
            else
                this.rowNameComponents = components.trim().split("_");
            for (int i = 0; i < this.rowNameComponents.length; i++) {
                this.rowNameComponents[i] = this.rowNameComponents[i].trim();
                if (DBDefines.convertToUpperCase) {
                    this.rowNameComponents[i] = this.rowNameComponents[i].toUpperCase();
                }
            }
        }
    }

    /**
     * Turn the constraints on this table on (contraintsOn = true) or off (constraintsOn = false).
     *
     * @param constraintsOn if true, turn the constraints on this table on; if false, turn the constraints on this table
     *                      off
     * @return whether or not the changes to this table's constraints was successful
     */
    public boolean setConstraints(boolean constraintsOn) {
        return this.getSchema().getDAO().setConstraints(this, constraintsOn);
    }

    /**********************************************************************
     * Format conversion functions
     **********************************************************************/
    /**
     * Return a String representation of this Table.
     *
     * @return a String representation of this Table
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (schema.name.length() > 0)
            s.append(schema.name + ".");

        s.append(name + "  Type=" + tableType);

        // Owned ID if there is one.
        if (ownedID == null)
            s.append("    OwnedID: NULL" + DBDefines.EOLN);
        else
            s.append("    OwnedID: " + ownedID + DBDefines.EOLN);

        // URL.
        try {
            s.append("URL: " + getURI() + DBDefines.EOLN);
        } catch (Exception ex) {
            s.append("ERROR in Table.toString().  Unable to retrieve URL.\n");
        }

        // Primary key.
        s.append("Primary key:  " + getPrimaryKey() + DBDefines.EOLN);

        // Unique keys.
        s.append("Unique keys:  " + getUniqueKeys() + DBDefines.EOLN);

        // Columns
        String[] list = new String[columns.length + 2];

        // Column name
        list[0] = "NAME";
        list[1] = "----";
        for (int i = 0; i < columns.length; i++)
            list[i + 2] = columns[i].name;
        DBDefines.evenLength(list);

        // Column foreign key
        list[0] += "   FKEY";
        list[1] += "   ---";
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].foreignKey == null || columns[i].foreignKey.equals(ownedID))
                list[i + 2] += "   -";
            else
                list[i + 2] += "   " + columns[i].foreignKey;
            if (columns[i].idLink != null)
                list[i + 2] += "*";
        }
        DBDefines.evenLength(list);

        // Column NA Value
        list[0] += "   NA_VALUE";
        list[1] += "   --------";
        for (int i = 0; i < columns.length; i++) {
            // if (columns[i].NAValue == null) list[i+2] += "   NOT_ALLOWED";
            if (!columns[i].NAValueAllowed())
                list[i + 2] += "   NOT_ALLOWED";
            else
                list[i + 2] += "   " + columns[i].NAValue;
        }
        DBDefines.evenLength(list);

        // Column Java Type
        list[0] += "   JAVA_TYPE";
        list[1] += "   ---------";
        for (int i = 0; i < columns.length; i++)
            list[i + 2] += "  " + DBDefines.javaTypes[columns[i].javaType];
        DBDefines.evenLength(list);

        // Column External Format
        list[0] += "   EXT_FORMAT";
        list[1] += "   ----------";
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].externalFormat.length() == 0)
                list[i + 2] += "   ?";
            else
                list[i + 2] += "   " + columns[i].externalFormat;
        }

        for (int i = 0; i < list.length; i++)
            s.append(list[i] + DBDefines.EOLN);

        return s.toString();
    }

    /**
     * Return a JAXB representation of this Table.
     *
     * @return JAXB representation of this Table
     * @throws JAXBException if a JAXB error is encountered
     */
    public gov.sandia.gnem.dbutillib.jaxb.Table toJaxb() throws JAXBException {
        return toJaxb(true);
    }

    /**
     * Return a JAXB representation of this Table.
     *
     * @param includeColumnInfo whether or not to include this Table's column information in the Jaxb Table object that
     *                          is returned
     * @return JAXB representation of this Table
     * @throws JAXBException if a JAXB error is encountered
     */
    public gov.sandia.gnem.dbutillib.jaxb.Table toJaxb(boolean includeColumnInfo) throws JAXBException {
        gov.sandia.gnem.dbutillib.jaxb.Table jaxbTable = new ObjectFactory().createTable();
        jaxbTable.setName(this.name);
        jaxbTable.setOwnedId(this.ownedID);
        jaxbTable.setType(this.tableType);
        jaxbTable.setUniqueKeys(this.getUniqueKeys());

        // Only include Column information if includeColumnInfo is true.
        if (includeColumnInfo) {
            List<gov.sandia.gnem.dbutillib.jaxb.Column> cols = jaxbTable.getColumn();
            for (int i = 0; i < this.columns.length; i++)
                cols.add(this.columns[i].toJaxb());
        }
        return jaxbTable;
    }

    /**
     * Create a Table object based on a JAXB Table
     *
     * @param jaxbTable JAXB representation of a Table to generate a new Table object from
     * @param schema    Schema the newly created Table will be associated with
     * @return a Table created from a JAXB representation of a Table (jaxbTable)
     * @throws FatalDBUtilLibException if a DBUtilLib Exception is encountered
     */
    public static Table fromJaxb(gov.sandia.gnem.dbutillib.jaxb.Table jaxbTable, Schema schema)
            throws FatalDBUtilLibException {
        Table table = null;

        // Get the Jaxb Table's Jaxb Columns.
        List<gov.sandia.gnem.dbutillib.jaxb.Column> jaxbColumns = jaxbTable.getColumn();

        // If there is no Column information, call the Table constructor without
        // Column information.
        if (jaxbColumns.size() == 0)
            table = new Table(jaxbTable.getName(), jaxbTable.getType(), jaxbTable.getUniqueKeys(), schema, null);
            // If there is Column information, extract it and call the Table
            // constructor with the Column information.
        else {
            Column[] columns = new Column[jaxbColumns.size()];
            for (int j = 0; j < columns.length; j++)
                columns[j] = Column.fromJaxb(jaxbColumns.get(j));

            table = new Table(jaxbTable.getName(), jaxbTable.getType(), jaxbTable.getUniqueKeys(), schema, columns);
        }

        // Set the ownedID.
        table.ownedID = jaxbTable.getOwnedId();

        return table;
    }

    public int[] getPrimaryKeyColumnIndexes() {
        return primaryKeys;
    }

    public int[] getForeignKeyColumnIndexes() {
        return foreignKeys;
    }

    public int[] getIdLinks() {
        return idLinks;
    }

    protected void setIdLinks() {
        LinkedList<Integer> links = new LinkedList<Integer>();
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].idLink != null) {
                links.add(i);
                if (idLinkMap.get(columns[i].idLink) == null)
                    idLinkMap.put(columns[i].idLink, new LinkedList<Integer>());
                idLinkMap.get(columns[i].idLink).add(new Integer(i));
            }
        }
        idLinks = new int[links.size()];
        int i = 0;
        for (Integer link : links)
            idLinks[i++] = link;
    }

    /**
     * Returns a map where the key is the name of a column in another table that columns in this table point to and the
     * value is a list of column indexes is this table that point to that column in the other table. For example, in the
     * origin table, the mbid, msid, and mlid columns all refer to the magid column in the netmag table. For a schema
     * that has an origin table related to a netmag table with relationships defined between mbid and magid, msid and
     * magid, and mlid and magid, this map will have an entry that looks like:
     * <p>
     * magid-> 16, 18, 20 <br>
     * where 16 is mbid's index, 18 is mlid's index, and 20 is msid's index in the origin table
     * <p>
     * If this table is an ID Owner table, it's ownedID will not be an element of the keyset. return a map where the key
     * is the name of a column in another table that columns in this table point to and the value is a list of column
     * indexes is this table that point to that column in the other table. For example, in the
     */
    protected HashMap<String, LinkedList<Integer>> getFKNameToIndexMap() {
        return this.idLinkMap;
    }
}
