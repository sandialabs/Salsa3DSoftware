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
package gov.sandia.gnem.dbutillib.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;

import gov.sandia.gnem.dbutillib.IDGapsTable;
import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Relationship;
import gov.sandia.gnem.dbutillib.Row;
import gov.sandia.gnem.dbutillib.RowGraph;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.Table;
import gov.sandia.gnem.dbutillib.TableDefinition;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * A DAOPool is a means of maintaining multiple DAO objects with a notion of the "currently selected" DAO object from
 * the pool. Implementations of abstract DAO methods simply call the implemented method of the currently selected DAO
 * object.
 */
public class DAOPool extends DAO {
    /**
     * This is the currently selected DAO
     */
    private DAO currentDAO = null;
    /**
     * All DAOs in the pool
     */
    private HashMap<String, DAO> allDAOs;

    // Constants that help make code easier to read.

    /**
     * Database DAO
     */
    private final static String DB = DBDefines.DATABASE_DAO;
    /**
     * Flat file DAO
     */
    private final static String FF = DBDefines.FF_DAO;
    /**
     * XML DAO
     */
    private final static String XML = DBDefines.XML_DAO;

    /**
     * Constructor. This creates a DAO object for each DAO type (currently Database, FlatFile, and XML).
     *
     * @param schema     schema these DAO objects belong to
     * @param configInfo ParInfo with DAO creation information
     * @param prefix     prefix to be used when accessing parameters from the configInfo object
     * @throws FatalDBUtilLibException if an error occurs while creating the DAO objects for this pool
     */
    protected DAOPool(Schema schema, ParInfo configInfo, String prefix) throws FatalDBUtilLibException {
        this.allDAOs = new HashMap<String, DAO>();

        // Database
        this.allDAOs.put(DB, new DAODatabase(schema, configInfo, prefix));

        // Flat file
        this.allDAOs.put(FF, new DAOFlatFile(schema, configInfo, prefix));

        // XML
        this.allDAOs.put(XML, new DAOFlatFile(schema, configInfo, prefix));

        // Want this to not be null
        this.currentDAO = this.allDAOs.get(DB);
    }

    /**
     * Set which dao is the currently selected DAO
     *
     * @param currentDAOType DAO type for the DAO that should become the currently selected DAO
     */
    public void setCurrentDAO(String currentDAOType) {
        if (!currentDAOType.equals(DB) && !currentDAOType.equals(FF) && !currentDAOType.equals(XML)) {
            DBDefines.ERROR_LOG.add("Error in DAOPool.setCurrentDAO(" + currentDAOType + "). currentDAOType parameter "
                    + "must be one of the following: " + DB + ", " + FF + ", " + XML + ". Unable to set current dao.");
            return;
        }
        this.currentDAO = this.allDAOs.get(currentDAOType);
    }

    /**
     * Return the DAO type of the currently selected DAO
     *
     * @return the DAO type of the currently selected DAO
     */
    public String getCurrentDAOType() {
        return this.currentDAO.getType();
    }

    /**
     * Return the currently selected DAO
     *
     * @return the currently selected DAO
     */
    public DAO getCurrentDAO() {
        return this.currentDAO;
    }

    /**
     * Return this DAOPool's Database DAO
     *
     * @return this DAOPool's Database DAO
     */
    public DAODatabase getDatabaseDAO() {
        return (DAODatabase) this.allDAOs.get(DB);
    }

    /**
     * Return this DAOPool's Flat File DAO
     *
     * @return this DAOPool's Flat File DAO
     */
    public DAOFlatFile getFlatFileDAO() {
        return (DAOFlatFile) this.allDAOs.get(FF);
    }

    /**
     * Return this DAOPool's XML DAO
     *
     * @return this DAOPool's XML DAO
     */
    public DAOFlatFile getXMLDAO() {
        return (DAOFlatFile) this.allDAOs.get(XML);
    }

    /** ~*~*~*~*~*~*~*~ START DAO Abstract Method Implementation ~*~*~*~*~*~*~*~ */

    /**
     * Return this DAOPool's DAO type (see {@link DBDefines#POOL_DAO DBDefines.POOL_DAO}).
     *
     * @return this DAOPool's DAO's type (see {@link DBDefines#POOL_DAO DBDefines.POOL_DAO}).
     */
    @Override
    public String getType() {
        return DBDefines.POOL_DAO;
    }

    /**
     * Returns URI for a table in the currently selected DAO.
     *
     * @param table the Table object whose URI is requested
     * @return the URI of a particular table in the currently selected DAO
     * @throws FatalDBUtilLibException if an error occurs when retrieving/creating the URI for the specified table
     */
    @Override
    public String getURI(Table table) throws FatalDBUtilLibException {
        return this.currentDAO.getURI(table);
    }

    /**
     * Return a ParInfo object populated with information about this dao object; this will include information for all
     * of the DAO types in this DAOPool object
     *
     * @return a ParInfo object populated with information about this dao object; this will include information for all
     * of the DAO types in this DAOPool object
     */
    @Override
    public ParInfo getParInfo() {
        ParInfo parInfo = new ParInfo();
        for (DAO dao : this.allDAOs.values())
            parInfo.updateAndAddParameters(dao.getParInfo());

        return parInfo;
    }

    /**
     * Return whether this DAOPool is equal to the specified Object. Two DAOPool's are not equal if they do not have the
     * same type ({@link DBDefines#POOL_DAO DBDefines.POOL_DAO}). Two DAOPool's are equal if and only if all of their
     * Database, Flat File, and XML DAOs are equal.
     *
     * @param other the object to which this DAOPool object is to be compared.
     * @return boolean true if they are the same; false otherwise
     */
    @Override
    public boolean equals(Object other) {
        // Can't be equal - not even from the same class!
        if (!other.getClass().toString().equals(this.getClass().toString()))
            return false;
        if (!((DAO) other).getType().equals(DBDefines.POOL_DAO))
            return false;
        DAOPool otherDAO = (DAOPool) other;
        boolean equalDB = this.getDatabaseDAO().equals(otherDAO.getDatabaseDAO());
        boolean equalFF = this.getFlatFileDAO().equals(otherDAO.getFlatFileDAO());
        boolean equalXML = this.getXMLDAO().equals(otherDAO.getXMLDAO());

        return equalDB && equalFF && equalXML;
    }

    /**
     * Calls the createConnection method for all of the DAOs in this DAOPool.
     *
     * @throws FatalDBUtilLibException if any fatal errors are encountered
     */
    @Override
    public void createConnection() throws FatalDBUtilLibException {
        for (DAO dao : this.allDAOs.values())
            dao.createConnection();
    }

    /**
     * Calls the closeConnection method for all of the DAOs in this DAOPool.
     */
    @Override
    public void closeConnection() {
        for (DAO dao : this.allDAOs.values())
            dao.closeConnection();
    }

    /**
     * Call the getAvailableTables method for the currently selected DAO.
     *
     * @param thisUserOnly    if true, and the currently selected DAO is the database DAO, then only tables in the current
     *                        user's account are returned and the names are not prepended with the account name. If not true, all tables that
     *                        are accessible to the user are returned, prepended with the account name.
     * @param tableNameFilter if not null and length > 0, then only table names that contain the specified string are
     *                        returned. Filter is not case sensitive.
     * @return the set of all the table names that are accessible to the user, sorted alphabetically.
     */
    @Override
    public TreeSet<String> getAvailableTables(boolean thisUserOnly, String tableNameFilter) {
        return this.currentDAO.getAvailableTables(thisUserOnly, tableNameFilter);
    }

    /**
     * Calls the sequenceExists method on the currently selected DAO.
     *
     * @param sequenceName String the name of the sequence whose accessibility is to be determined.
     * @return boolean whether or not the sequence is accessible. Flatfile and xml dao's always return false.
     */
    @Override
    public boolean sequenceExists(String sequenceName) throws FatalDBUtilLibException {
        return this.currentDAO.sequenceExists(sequenceName);
    }

    /**
     * Calls the tableExists(table) method on the currently selected DAO.
     *
     * @param table table being checked for existence
     * @return whether the table named tableName exists in the currently selected DAO or not - true, it does exist;
     * false, it does not
     */
    @Override
    public boolean tableExists(Table table) {
        return this.currentDAO.tableExists(table);
    }

    /**
     * Calls the tableExists(tableName) method on the currently selected DAO.
     *
     * @param tableName name of the table to be checked for existence
     * @return if the table named tableName exists or not - true, it exists; false, it does not exist
     */
    @Override
    public boolean tableExists(String tableName) {
        return this.currentDAO.tableExists(tableName);
    }

    /**
     * Calls the tableIsEmpty method on the currently selected DAO.
     *
     * @param tableName name of the table being checked to see if it's empty
     * @return whether table named tableName is empty or not - true, it is empty; false, it is not
     * @throws FatalDBUtilLibException if an error occurs when checking to see if the table is empty
     */
    @Override
    public boolean tableIsEmpty(String tableName) throws FatalDBUtilLibException {
        return this.currentDAO.tableIsEmpty(tableName);
    }

    /**
     * Calls the emptyTable method on the currently selected DAO.
     *
     * @param tableName name of the table having all of its rows deleted
     * @throws FatalDBUtilLibException if a SQL error occurs when deleting all data from the table
     */
    @Override
    public void emptyTable(String tableName) throws FatalDBUtilLibException {
        this.currentDAO.emptyTable(tableName);
    }

    /**
     * Calls the dropTable method on the currently selected DAO.
     *
     * @param tableName name of the table to be dropped
     * @return whether or not the table was dropped successfully
     */
    @Override
    public boolean dropTable(String tableName) {
        return this.currentDAO.dropTable(tableName);
    }

    /**
     * Calls the truncateTable method on the currently selected DAO.
     *
     * @param table the Table object whose corresponding table is to be truncated.
     * @return whether table was successfully truncated or not
     */
    @Override
    public boolean truncateTable(Table table) {
        return this.currentDAO.truncateTable(table);
    }

    /**
     * Calls the createTable(table) method for the currently selected DAO.
     *
     * @param table the Table object that defines the table type and name for the table that is to be created.
     * @return true if the table was created, false otherwise
     * @throws FatalDBUtilLibException if an error occurs when creating the table
     */
    @Override
    public boolean createTable(Table table) throws FatalDBUtilLibException {
        return this.currentDAO.createTable(table);
    }

    /**
     * Calls the createTable(table, setPrimaryKeys, setUniqueKeys) method for the currently selected DAO.
     *
     * @param table          the Table object that defines the table type and name for the table that is to be created.
     * @param setPrimaryKeys if true, then primary key constraints will be set for columns that have
     *                       COLUMN_TYPE=='primary key' in the table definition table.
     * @param setUniqueKeys  if true, then primary key constraints will be set for columns that have COLUMN_TYPE=='unique
     *                       key' in the table definition table.
     * @return true if the table was created, false otherwise
     * @throws FatalDBUtilLibException if an error occurs when creating the table
     */
    @Override
    public boolean createTable(Table table, boolean setPrimaryKeys, boolean setUniqueKeys)
            throws FatalDBUtilLibException {
        return this.currentDAO.createTable(table, setPrimaryKeys, setUniqueKeys);
    }

    /**
     * Calls the setPrimaryKey method for the currently selected DAO.
     *
     * @param table table to set the primary key on
     * @return name of the primary key ("" if the table does not have primary key information in the table definition
     * table)
     * @throws FatalDBUtilLibException if an error occurs when setting the primary key
     */
    @Override
    public String setPrimaryKey(Table table) throws FatalDBUtilLibException {
        return this.currentDAO.setPrimaryKey(table);
    }

    /**
     * Calls the setUniqueKey method for the currently selected DAO
     *
     * @param table table to set the unique key on
     * @return name of the unique key ("" if the table does not have unique key information in the table definition
     * table)
     * @throws FatalDBUtilLibException if an error occurs when setting the unique key
     */
    @Override
    public String setUniqueKey(Table table) throws FatalDBUtilLibException {
        return this.currentDAO.setUniqueKey(table);
    }

    /**
     * Calls the setForeignKeys(table, onDeleteCascade, onDeleteSetNull, deferrable, initiallyDeferred, enabled,
     * validate, printSql) method on the currently selected DAO.
     *
     * @param table             table whose foreign keys need to be set
     * @param onDeleteCascade   whether or not to use the ON DELETE CASCADE option when creating this foreign key. Use the
     *                          ON DELETE CASCADE option to specify that you want rows deleted in a child table when corresponding rows are
     *                          deleted in the parent table. (See Oracle documentation for more information.)
     * @param onDeleteSetNull   whether or not to use the ON DELETE SET NULL option when creating this foreign key. Use
     *                          the ON DELETE SET NULL option if when a record in the parent table is deleted, the corresponding records in the
     *                          child table will have the foreign key fields set to null. The records in the child table will not be deleted.
     * @param deferrable        whether or not this foreign key constraint is deferrable. A deferrable constraint is a
     *                          constraint with the option to not check the constraint's validity until a commit is issued
     * @param initiallyDeferred whether or not this constraint starts in the deferred state (only applies if it is a
     *                          deferrable constraint)
     * @param enabled           whether or not this constraint is enabled after it is created
     * @param validate          whether or not this constraint should be validated after it is created
     * @param printSql          if true, dbutillib will print to the screen the sql statement that generates the foreign key
     * @throws FatalDBUtilLibException if an error occurs while creating the foreign keys for the table
     */
    @Override
    public void setForeignKeys(Table table, boolean onDeleteCascade, boolean onDeleteSetNull, boolean deferrable,
                               boolean initiallyDeferred, boolean enabled, boolean validate, boolean printSql)
            throws FatalDBUtilLibException {
        this.currentDAO.setForeignKeys(table, onDeleteCascade, onDeleteSetNull, deferrable, initiallyDeferred, enabled,
                validate, printSql);
    }

    /**
     * Calls the setForeignKeys(table, columns, referencedTable, referencedColumns, onDeleteCascade, onDeleteSetNull,
     * deferrable, initiallyDeferred, enabled, validate, printSql) method on the currently selected DAO.
     *
     * @param tableType           the name of the table containing the foreign key to be set
     * @param columns             comma-separated list of column names that constitute the foreign key
     * @param referencedTableType the name of the table that the foreign key references.
     * @param referencedColumns   the primary key in referencedTableType that is what the foreign key refers to
     * @param onDeleteCascade     whether or not to use the ON DELETE CASCADE option when creating this foreign key. Use the
     *                            ON DELETE CASCADE option to specify that you want rows deleted in a child table when corresponding rows are
     *                            deleted in the parent table. (See Oracle documentation for more information.)
     * @param onDeleteSetNull     whether or not to use the ON DELETE SET NULL option when creating this foreign key. Use
     *                            the ON DELETE SET NULL option if when a record in the parent table is deleted, the corresponding records in the
     *                            child table will have the foreign key fields set to null. The records in the child table will not be deleted.
     * @param deferrable          whether or not this foreign key constraint is deferrable. A deferrable constraint is a
     *                            constraint with the option to not check the constraint's validity until a commit is issued
     * @param initiallyDeferred   whether or not this constraint starts in the deferred state (only applies if it is a
     *                            deferrable constraint)
     * @param enabled             whether or not this constraint is enabled after it is created
     * @param validate            whether or not this constraint should be validated after it is created
     * @param printSql            if true, dbutillib will print to the screen the sql statement that generates the foreign key
     * @throws FatalDBUtilLibException if an error occurs while creating the foreign keys for the table
     */
    @Override
    public void setForeignKeys(String tableType, String columns, String referencedTableType, String referencedColumns,
                               boolean onDeleteCascade, boolean onDeleteSetNull, boolean deferrable, boolean initiallyDeferred,
                               boolean enabled, boolean validate, boolean printSql) throws FatalDBUtilLibException {
        this.currentDAO.setForeignKeys(tableType, columns, referencedTableType, referencedColumns, onDeleteCascade,
                onDeleteSetNull, deferrable, initiallyDeferred, enabled, validate, printSql);
    }

    /**
     * Turn the constraints on a table on (contraintsOn = true) or off (constraintsOn = false).
     *
     * @param table         table that needs to have constraints turned on or off
     * @param constraintsOn if true, turn the constraints on the specified table on; if false, turn the constraints on
     *                      the specified table off
     * @return whether or not the changes to this table's constraints was successful
     */
    @Override
    public boolean setConstraints(Table table, boolean constraintsOn) {
        return this.currentDAO.setConstraints(table, constraintsOn);
    }

    /**
     * Calls the getAllData method on the currently selected DAO.
     *
     * @return a RowGraph that contains all the rows from all the tables in the schema; an empty RowGraph will be
     * returned. <b><i>Caution!</i></b> This reads everything in the schema into memory. If your schema contains tables
     * with a lot of data, you will most likely get an OutOfMemoryError.
     * @throws FatalDBUtilLibException if an error occurs when retrieving all data from all the tables in the schema
     */
    @Override
    public RowGraph getAllData() throws FatalDBUtilLibException {
        return this.currentDAO.getAllData();
    }

    /**
     * Calls executeSelectStatement(tableType, whereClause) on the currently selected DAO object.
     *
     * @param tableType   the type of the Table object against which the select statement should be executed.
     * @param whereClause the where clause of the sql statement
     * @return a list of Row objects that result from execution of the select statement.
     * @throws FatalDBUtilLibException if an error occurs when executing the select statement or creating the data
     *                                 objects to be returned
     */
    @Override
    public LinkedList<Row> executeSelectStatement(String tableType, String whereClause) throws FatalDBUtilLibException {
        return this.currentDAO.executeSelectStatement(tableType, whereClause);
    }

    /**
     * Calls executeSelectStatement(table, whereClause) on the currently selected DAO object.
     *
     * @param table       the Table against which the select statement should be executed
     * @param whereClause the where clause of the sql statement
     * @return Row objects created from the returned results
     * @throws FatalDBUtilLibException if an error occurs when executing the select statement or creating the data
     *                                 objects to be returned
     */
    @Override
    public LinkedList<Row> executeSelectStatement(Table table, String whereClause) throws FatalDBUtilLibException {
        return this.currentDAO.executeSelectStatement(table, whereClause);
    }

    /**
     * Calls executeSelectStatement(column, tableName, whereClause, orderClause) on the currently selected DAO.
     *
     * @param column      column to select from tableName
     * @param tableName   the name of the table to execute the statement against
     * @param whereClause the where clause part of the statement (with or without the word 'where' at the beginning)
     * @param orderClause the order by clause
     * @return a LinkedList of Objects that are the result of the query. Each item in the returned list represents one
     * returned row from the currently selected DAO with the value for the one column specified
     * @throws FatalDBUtilLibException if an error occurs while executing the select statement or creating the data
     *                                 objects to be returned
     */
    @Override
    public LinkedList<Object> executeSelectStatement(String column, String tableName, String whereClause,
                                                     String orderClause) throws FatalDBUtilLibException {
        return this.currentDAO.executeSelectStatement(column, tableName, whereClause, orderClause);
    }

    /**
     * Calls the executeSelectStatement(columns, tableName, whereClause, orderClause) method on the currently selected
     * DAO.
     *
     * @param columns     columns to select from tableName
     * @param tableName   the name of the table to execute the statement against
     * @param whereClause the where clause part of the statement (with or without the word 'where' at the beginning)
     * @param orderClause the order by clause
     * @return a LinkedList of LinkedLists of Objects that contain the results of the query; empty list if no rows
     * returned
     * @throws FatalDBUtilLibException if an error occurs while executing the select statement or creating the data
     *                                 objects to be returned
     */
    @Override
    public LinkedList<LinkedList<Object>> executeSelectStatement(LinkedList<String> columns, String tableName,
                                                                 String whereClause, String orderClause) throws FatalDBUtilLibException {
        return this.currentDAO.executeSelectStatement(columns, tableName, whereClause, orderClause);
    }

    /**
     * Calls the executeSelectStatement(relationship) method on the currently selected DAO.
     *
     * @param relationship the Relationship object that contains the PreparedStatement and other useful information
     *                     needed to execute that PreparedStatement
     * @return LinkedList of Row objects that are the result of executing the PreparedStatement in relationship; empty
     * list if no rows returned
     * @throws FatalDBUtilLibException if an error occurs while executing the relationship's select statement or while
     *                                 creating the data objects to be returned
     */
    @Override
    public LinkedList<Row> executeSelectStatement(Relationship relationship) throws FatalDBUtilLibException {
        return this.currentDAO.executeSelectStatement(relationship);
    }

    /**
     * Calls the executeSelectStatement(tableDef, tableType) for the currently selected DAO.
     *
     * @param tableDef  the TableDefinition object that contains the column metadata
     * @param tableType the type of Table whose column information is being requested
     * @return an ArrayList of HashMaps - one HashMap for each Column in the specified Table. Each column's HashMap
     * defines an association between the name of some piece of column metadata (a column in the table definition table)
     * and the value that corresponds to that column. As of 09/17/2008, the columns in the table definition table
     * contained in the HashMap are:
     * <p>
     * COLUMN_NAME <br>
     * KEY <br>
     * NA_ALLOWED <br>
     * NA_VALUE <br>
     * INTERNAL_FORMAT <br>
     * EXTERNAL_FORMAT <br>
     * EXTERNAL_WIDTH <br>
     * COLUMN_TYPE <br>
     * EXTERNAL_TYPE <br>
     * @throws FatalDBUtilLibException if a SQL error occurs when executing the TableDefinition's select statment or
     *                                 when creating the data objects to be returned
     */
    @Override
    public ArrayList<HashMap<String, String>> executeSelectStatement(TableDefinition tableDef, String tableType)
            throws FatalDBUtilLibException {
        return this.currentDAO.executeSelectStatement(tableDef, tableType);
    }

    /**
     * Calls the executeSelectStatement(statement) method on the currently selected DAO.
     *
     * @param statement SELECT statement to execute
     * @return LinkedList of RowWithoutATable Objects that are the result of executing statement; empty list if no rows
     * returned
     * @throws FatalDBUtilLibException if a SQL error occurs when executing the select statement or creating the data
     *                                 objects to be returned
     */
    @Override
    public LinkedList<DAO.RowWithoutATable> executeSelectStatement(String statement) throws FatalDBUtilLibException {
        return this.currentDAO.executeSelectStatement(statement);
    }

    /**
     * Calls the executeSelect method on the currently selected DAO.
     *
     * @param statement String the sql SELECT statement to be executed.
     * @param types     Class[] of the expected type of each returned column. There should be exactly one entry in types for
     *                  each column specified in the sql statement.
     * @return The data retrieved from the database; the ArrayList's size will be the number of rows returned from
     * executing the query and each Object[] will have a length = to the number of columns specified (which should be
     * the same length as types)
     * @throws FatalDBUtilLibException if an error occurs while executing the select statement or creating the data
     *                                 objects to be returned
     */
    @Override
    public ArrayList<Object[]> executeSelect(String statement, Class<?>[] types) throws FatalDBUtilLibException {
        return this.currentDAO.executeSelect(statement, types);
    }

    /**
     * Calls the executeSelectFromSequence method on the currently selected DAO.
     *
     * @param sequenceName the name of the sequence
     * @return the next value in the sequence
     * @throws FatalDBUtilLibException if an error occurs when retrieving the next value from a sequence
     */
    @Override
    public long executeSelectFromSequence(String sequenceName) throws FatalDBUtilLibException {
        return this.currentDAO.executeSelectFromSequence(sequenceName);
    }

    /**
     * Calls the selectOwnedIds method on the currently selected DAO.
     *
     * @param table       the idowner table against which the sql statement should be executed.
     * @param whereClause the where clause
     * @return the set of ownedIds that results.
     * @throws FatalDBUtilLibException if a SQL error occurs when executing the select statement or creating the data to
     *                                 be returned
     */
    @Override
    public TreeSet<Long> selectOwnedIds(Table table, String whereClause) throws FatalDBUtilLibException {
        return this.currentDAO.selectOwnedIds(table, whereClause);
    }

    /**
     * Calls the getMaxId method on the currently selected DAO.
     *
     * @param idName    the name of the id whose max value is being requested
     * @param tableName the name of the table
     * @return the highest id for the column named idName in the table named tableName
     * @throws FatalDBUtilLibException if a database error occurs when retrieving the max id
     */
    @Override
    public long getMaxID(String idName, String tableName) throws FatalDBUtilLibException {
        return this.currentDAO.getMaxID(idName, tableName);
    }

    /**
     * Calls the executeUpdateStatement method on the currently selected DAO.
     *
     * @param statement update statement to execute
     * @return result code indicating the status of executing the update statement (the row count for INSERT, UPDATE, or
     * DELETE statements or 0 for SQL statements that return nothing)
     * @throws FatalDBUtilLibException if an error occurs when executing the update statement
     */
    @Override
    public int executeUpdateStatement(String statement) throws FatalDBUtilLibException {
        return this.currentDAO.executeUpdateStatement(statement);
    }

    /**
     * Calls the insertRow(row) method on the currently selected DAO.
     *
     * @param row the Row whose information is to be inserted into the database.
     * @return whether the row insertion was successful or not
     * @throws FatalDBUtilLibException if an error occurs when inserting this row's data into the database
     */
    @Override
    public boolean insertRow(Row row) throws FatalDBUtilLibException {
        return this.currentDAO.insertRow(row);
    }

    /**
     * Calls the insertRow(row, ignoreUniqueError) method on the currently selected DAO.
     *
     * @param row               the Row whose information is to be inserted into the database.
     * @param ignoreUniqueError whether to ignore errors generated from inserting a row that results in a unique key
     *                          constraint violation (true) or not (false)
     * @return whether the row insertion was successful or not
     * @throws FatalDBUtilLibException if an error occurs when inserting this row's data into the database
     */
    @Override
    public boolean insertRow(Row row, boolean ignoreUniqueError) throws FatalDBUtilLibException {
        return this.currentDAO.insertRow(row, ignoreUniqueError);
    }

    /**
     * Calls the updateRow method on the currently selected DAO.
     *
     * @param row the Row whose information is being updated in the database
     * @return whether the update was successful or not
     * @throws FatalDBUtilLibException if an error occurs when updating the row in the database
     */
    @Override
    public boolean updateRow(Row row) throws FatalDBUtilLibException {
        return this.currentDAO.updateRow(row);
    }

    /**
     * Calls the deleteRow method on the currently selected DAO.
     *
     * @param row the Row that is to be deleted from the database
     * @return true if a row is actually deleted; false otherwise
     * @throws FatalDBUtilLibException if an error occurs when deleting the row from the database
     */
    @Override
    public boolean deleteRow(Row row) throws FatalDBUtilLibException {
        return this.currentDAO.deleteRow(row);
    }

    /**
     * Calls the writeIDGapsRow method on the currently selected DAO.
     *
     * @param idGapsTable IDGapsTable that needs to have a row written to the database
     * @param values      values used to replace the ?'s in the IDGapTable's PreparedStatement string
     * @throws FatalDBUtilLibException if an error occurs when writing the idgaps row
     */
    @Override
    public void writeIDGapsRow(IDGapsTable idGapsTable, Object[] values) throws FatalDBUtilLibException {
        this.currentDAO.writeIDGapsRow(idGapsTable, values);
    }

    /**
     * Calls the commit method on the currently selected DAO.
     */
    @Override
    public void commit() {
        this.currentDAO.commit();
    }

    /**
     * Calls the rollback method on the currently selected DAO.
     */
    @Override
    public void rollback() {
        this.currentDAO.rollback();
    }

    /**
     * Calls the iterator(table, whereClause) method on the currently selected DAO.
     *
     * @param table       the Table object against which the select statement should be executed; the Row objects that are
     *                    returned by this iterator will be members of this Table object.
     * @param whereClause a valid SQL where clause that can be used in a SQL statement of the form <BR>
     *                    "SELECT * FROM " + table.name + " " + whereClause <BR>
     *                    if whereClause does not start with "where " (not case sensitive), then "WHERE " will be automatically prepended.
     * @return an Iterator over {@link Row Row} objects; null if an error occurs
     */
    @Override
    public Iterator<Row> iterator(Table table, String whereClause) {
        return this.currentDAO.iterator(table, whereClause);
    }

    /**
     * Calls the iterator(selectStatement) method on the currently selected DAO.
     *
     * @param selectStatement selectStatement to be executed against the database
     * @return an Iterator over {@link DAO.RowWithoutATable DAO.RowWithoutATable} objects; null if an error occurs
     */
    @Override
    public Iterator<DAO.RowWithoutATable> iterator(String selectStatement) {
        return this.currentDAO.iterator(selectStatement);
    }

    /**
     * Return a String representation of this DAOPool. Calls toString on all of the DAO objects in this DAOPool.
     *
     * @return a String representation of this DAOPool. Calls toString on all of the DAO objects in this DAOPool.
     */
    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder("Data Access Object Pool: " + DBDefines.EOLN);
        for (DAO dao : this.allDAOs.values())
            returnString.append(dao.toString() + DBDefines.EOLN);
        return returnString.toString();
    }

    /**
     * Calls the outputColumnInfo method on the currently selected DAO.
     *
     * @param yesNo whether or not to output column information; this is only implemented for XML DAOs
     */
    @Override
    public void outputColumnInfo(boolean yesNo) {
        this.currentDAO.outputColumnInfo(yesNo);
    }

    /**
     * Calls the toJaxb method on the currently selected DAO.
     *
     * @return JAXB representation of the currently selected DAO
     * @throws JAXBException if an error occurs when converting the currently selected DAO object to JAXB
     */
    @Override
    public gov.sandia.gnem.dbutillib.jaxb.DAO toJaxb() throws JAXBException {
        return this.currentDAO.toJaxb();
    }
}
