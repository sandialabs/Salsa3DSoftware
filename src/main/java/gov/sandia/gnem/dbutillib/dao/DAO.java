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
 * This class is an interface to be used when implementing Data Access Objects (DAOs). Some examples of classes that
 * might implement this interface are classes that provide access to an Oracle database, an Access database, or say an
 * XML file.
 */
public abstract class DAO {
    /**
     * Returns what DAO type a class represents. See DBDefines for a listing of the available DAO types.
     *
     * @return DAO type a class represents
     */
    abstract public String getType();

    /**
     * Returns URI for a table in this DAO object.
     *
     * @param table table to retrieve URI for
     * @return URI for this DAO object
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public String getURI(Table table) throws FatalDBUtilLibException;

    /**
     * Return a ParInfo object populated with information about this dao object.
     *
     * @return a ParInfo object populated with information about this dao object
     */
    abstract public ParInfo getParInfo();

    /**
     * Two DAO's are not equal if they do not have the same type (db, ff, xml). Two DB DAO's are equal if and only if
     * they have the same username, password and instance (driver does not matter). Two xml dao's are equal if they have
     * the same input xml File and the same output xml File.
     *
     * @param otherDAO Object the other dao object to which this dao object is to be compared.
     * @return boolean true if they are the same.
     */
    @Override
    abstract public boolean equals(Object otherDAO);

    /**
     * Create a connection with wherever the data will be coming from.
     *
     * @throws FatalDBUtilLibException if any fatal errors are encountered
     */
    abstract public void createConnection() throws FatalDBUtilLibException;

    /**
     * Closes the connection with wherever the data will be coming from.
     */
    abstract public void closeConnection();

    /**
     * Retrieve the set of table names that are accessible to the current user. For databases, includes tables, views,
     * aliases, and synonyms. Not yet implemented for flatfiles or xml files.
     *
     * @param thisUserOnly    boolean if true, then only tables in the current user's account are returned and the names
     *                        are not prepended with the account name. If not true, all tables that are accessible to the user are returned,
     *                        prepended with the account name.
     * @param tableNameFilter String if not null and length > 0, then only table names that contain the specified string
     *                        are returned. Filter is not case sensitive.
     * @return TreeSet<String> the set of all the table names that are accessible to the user, sorted alphabetically.
     */
    abstract public TreeSet<String> getAvailableTables(boolean thisUserOnly, String tableNameFilter);

    /**
     * Determine whether or not a database sequence is accessible. Flatfile and xml dao's always return false.
     *
     * @param sequenceName String the name of the sequence whose accessibility is to be determined.
     * @return boolean whether or not the sequence is accessible. Flatfile and xml dao's always return false.
     */
    abstract public boolean sequenceExists(String sequenceName) throws FatalDBUtilLibException;

    /**
     * Returns whether a table named exists in the data store this DAO represents or not.
     *
     * @param table table to be checked for existence
     * @return if the table exists or not - true, it exists; false, it does not exist
     */
    abstract public boolean tableExists(Table table);

    /**
     * Returns whether a table named tableName exists in the data store this DAO represents or not.
     *
     * @param tableName name of the table to be checked for existence
     * @return if the table named tableName exists or not - true, it exists; false, it does not exist
     */
    abstract public boolean tableExists(String tableName);

    /**
     * Returns whether a table named tableName is empty or not.
     *
     * @param tableName name of the table to be checked to see if it is empty
     * @return whether or not the table named tableName is empty - true, it is empty; false, it is not empty
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public boolean tableIsEmpty(String tableName) throws FatalDBUtilLibException;

    /**
     * Deletes all of the data from the table named tableName.
     *
     * @param tableName name of the table that will have all data deleted from it
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public void emptyTable(String tableName) throws FatalDBUtilLibException;

    /**
     * Gets rid of the table named tableName.
     *
     * @param tableName name of the table to be gotten rid of
     * @return whether or not table was dropped successfully or not
     */
    abstract public boolean dropTable(String tableName);

    /**
     * Truncate a table.
     *
     * @param table the Table object to be truncated.
     * @return true if the table exists and was successfully truncated, false otherwise.
     */
    abstract public boolean truncateTable(Table table);

    /**
     * Creates a table named tableName of type tableType with information found in the TableDefinitionTable for that
     * table type. For database tables, the primary keys as defined by the COLUMN_TYPE in the table definition table
     * will be set, but foreign keys and unique keys will not.
     *
     * @param table the Table object that defines the metadata for the database table that is to be created.
     * @return true if table was created, false otherwise
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public boolean createTable(Table table) throws FatalDBUtilLibException;

    /**
     * Creates a table named tableName of type tableType with information found in the TableDefinitionTable for that
     * table type.
     *
     * @param table          the Table object that defines the metadata for the database table that is to be created.
     * @param setPrimaryKeys if true, then primary key constraint will be set using columns that have
     *                       COLUMN_TYPE=='primary key' in the table definition table.
     * @param setUniqueKeys  if true, then unique key constraint will be set using columns that have COLUMN_TYPE=='unique
     *                       key' in the table definition table.
     * @return true if table was created; false otherwise
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public boolean createTable(Table table, boolean setPrimaryKeys, boolean setUniqueKeys)
            throws FatalDBUtilLibException;

    /**
     * Set the primary key on a Table. Primary key definition is retrieved from the table definition table. Only applies
     * to database DAOs.
     *
     * @param table Table
     * @return the name of the generated primary key
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public String setPrimaryKey(Table table) throws FatalDBUtilLibException;

    /**
     * Set the unique key on a Table. Unique key definition is retrieved from the table definition table. Only applies
     * to database DAOs.
     *
     * @param table Table
     * @return the name of the generated unique key
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public String setUniqueKey(Table table) throws FatalDBUtilLibException;

    abstract public void setForeignKeys(Table table, boolean onDeleteCascade, boolean onDeleteSetNull,
                                        boolean deferrable, boolean initiallyDeferred, boolean enabled, boolean validate, boolean printSql)
            throws FatalDBUtilLibException;

    /**
     * Set a foreign key on a table.
     *
     * @param table             String the name of the table containing the foreign key to be set
     * @param columns           String a comma-separated list of column names that constitute the foreign key
     * @param referencedTable   String the name of the table that the foreign key in table references.
     * @param referencedColumns String the primary key in referencedTable
     * @param onDeleteCascade   boolean see Oracle documentation
     * @param onDeleteSetNull   boolean
     * @param deferrable        boolean
     * @param initiallyDeferred boolean
     * @param enabled           boolean
     * @param validate          boolean
     * @param printSql          boolean dbutillib will print to the screen the sql statement that generates the foreign key.
     * @throws FatalDBUtilLibException
     */
    abstract public void setForeignKeys(String table, String columns, String referencedTable, String referencedColumns,
                                        boolean onDeleteCascade, boolean onDeleteSetNull, boolean deferrable, boolean initiallyDeferred,
                                        boolean enabled, boolean validate, boolean printSql) throws FatalDBUtilLibException;

    /**
     * Turn the constraints on a table on (contraintsOn = true) or off (constraintsOn = false).
     *
     * @param table         table that needs to have constraints turned on or off
     * @param constraintsOn if true, turn the constraints on the specified table on; if false, turn the constraints on
     *                      the specified table off
     * @return whether or not the changes to this table's constraints was successful
     */
    abstract public boolean setConstraints(Table table, boolean constraintsOn);

    /**
     * Returns a RowGraph containing all the Rows contained in the current DAO object.
     *
     * @return a RowGraph of all the data.
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public RowGraph getAllData() throws FatalDBUtilLibException;

    /**
     * Creates a SQL select statement of the form <BR>
     * "SELECT * FROM " + tableName + " " + whereClause <br>
     * executes it, and returns all the Row objects that result.
     *
     * @param tableType   the type of the Table object against which the select statement should be executed.
     * @param whereClause the where clause of the sql statement. This can be anything that that comes after table.name
     *                    in the select statement. It must start with the word 'where '.
     * @return a list of Row objects that result from execution of the select statement.
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public LinkedList<Row> executeSelectStatement(String tableType, String whereClause)
            throws FatalDBUtilLibException;

    /*******************************************************************************************************************
     * Sandy, in the comments below that state that the whereClause parameter must start with the word 'where' - is that
     * 'where' case sensitive?
     ******************************************************************************************************************/
    /**
     * Creates a SQL SELECT statement of the form <BR>
     * "SELECT * FROM " + table.name + " " + whereClause <br>
     * executes it, and returns a LinkedList<Row> of what is returned
     *
     * @param table       the Table object against which the select statement should be executed
     * @param whereClause the where clause of the sql statement; it must start with the word 'where'
     * @return a LinkedList<Row> that results from execution of the select statement
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public LinkedList<Row> executeSelectStatement(Table table, String whereClause)
            throws FatalDBUtilLibException;

    /**
     * Execute a SQL select statement, independently specifying each of the components of the select statement.
     *
     * @param column      a single column name
     * @param tableName   the name of the table to execute the statement against
     * @param whereClause the where clause part of the statement (with or without the word 'where' at the begining)
     * @param orderClause the order by clause
     * @return a LinkedList of Objects that contain the results of the query; empty list if no rows returned
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public LinkedList<Object> executeSelectStatement(String column, String tableName, String whereClause,
                                                              String orderClause) throws FatalDBUtilLibException;

    /**
     * Executes a SQL SELECT statement that is created by assembling independently specified components of the SELECT
     * statement. The query would look something like: <BR>
     * SELECT [columns turned into comma delimited list] FROM tableName WHERE whereClause ORDER BY orderClause
     *
     * @param columns     a LinkedList of (String) column names
     * @param tableName   the name of the table to execute the statement against
     * @param whereClause the where clause part of the statement (with or without the word 'where' at the begining)
     * @param orderClause the order by clause
     * @return a LinkedList of LinkedLists of Objects that contain the results of the query; empty list if no rows
     * returned
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public LinkedList<LinkedList<Object>> executeSelectStatement(LinkedList<String> columns, String tableName,
                                                                          String whereClause, String orderClause) throws FatalDBUtilLibException;

    /**
     * Return a LinkedList of Rows that are the result of executing the PreparedStatement that is stored within the
     * Relationship object. Returns an empty LinkedList if no rows were returned by the query.
     *
     * @param relationship the Relationship object that contains the PreparedStatement and other useful information
     *                     needed to execute that PreparedStatement
     * @return LinkedList of Row objects that are the result of executing the PreparedStatement in relationship; empty
     * list if no rows returned
     * @throws FatalDBUtilLibException if an exception occurs
     */
    abstract public LinkedList<Row> executeSelectStatement(Relationship relationship) throws FatalDBUtilLibException;

    /**
     * Query a DAO for column metadata for each column in a specified table.
     *
     * @param tableDef  the TableDefinition object that is requesting the column metadata. A TableDefinition object has a
     *                  SQL PreparedStatement that can be used by a DAODataBase object, or other information that can be used by other
     *                  types of DAO objects that cannot use PreparedStatements
     * @param tableType the type of Table whose column information is being requested
     * @return a LinkedList of HashMaps (one HashMap for each Column in the specified Table). Each HashMap defines an
     * association between the name of some piece of column metadata and the value that corresponds to that name. Two
     * examples of some of the names are 'COLUMN_NAME' and 'NA_VALUE'.
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public ArrayList<HashMap<String, String>> executeSelectStatement(TableDefinition tableDef, String tableType)
            throws FatalDBUtilLibException;

    /**
     * Execute the SELECT statement in statement and return the results of that SELECT statement in a LinkedList of
     * RowWithoutATable Objects. Since this function does not take a table as an argument, then it cannot create the
     * usual Row objects, and must then make RowWithoutATable Objects. See the {@link DAO.RowWithoutATable
     * RowWithoutATable} class for more information on RowWithoutATable Objects. <BR>
     * This function is one that should be used only in the rarest of cases since it does not return actual Row objects,
     * and most of the functionality within DBUtilLib needs actual Row objects. The RowWithoutATable objects cannot be
     * used in any way like the DBUtilLib Row objects.
     *
     * @param statement SELECT statement to execute
     * @return LinkedList of RowWithoutATable Objects that are the result of executing statement; empty list if no rows
     * returned
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public LinkedList<DAO.RowWithoutATable> executeSelectStatement(String statement)
            throws FatalDBUtilLibException;

    /**
     * Executes a SQL SELECT statement and returns the results in an ArrayList<Object[]>. Use the types Class[] to
     * specify the type of each expected return value. For data of type int, long and double, oracle/jdbc returns data
     * of type BigDecimal. For every entry in the types array that is of type Double.class, Long.class or Integer.class,
     * this method will attempt to cast the data item retrieved from the database into a BigDecimal and then into the
     * requested data type. For all other specified types (including null), this method will simply return the object
     * returned from oracle, without doing any casting at all.
     *
     * @param statement String the sql statement to be executed.
     * @param types     Class[] the expected type of each returned value. There should be exactly one entry in types for
     *                  each column specified in the sql statement.
     * @return ArrayList<Object [ ]> The data retrieved from the database. Each Object[] will have the same dimensions as
     * the input Class[] types.
     * @throws FatalDBUtilLibException if a SQL error occurs.
     */
    abstract public ArrayList<Object[]> executeSelect(String statement, Class<?>[] types)
            throws FatalDBUtilLibException;

    /**
     * Get the next value of an id from an oracle sequence.
     *
     * @param sequenceName String Name of the sequence from which a value is to be extracted.
     * @return long The value extracted from the sequence. Returns -1 if the sequence does not exist.
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public long executeSelectFromSequence(String sequenceName) throws FatalDBUtilLibException;

    /**
     * Retrieve all the ownedID values from an idOwner table that satisfy some condition specified with a where clause.
     *
     * @param table       the idowned table against which the sql statement should be executed
     * @param whereClause the where clause
     * @return set of ownedIds
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public TreeSet<Long> selectOwnedIds(Table table, String whereClause) throws FatalDBUtilLibException;

    /**
     * Get the maximum value of an ID in a specified table.
     *
     * @param idName    The ID whose maximum value is to be retrieved.
     * @param tableName The table which is to be searched for the maximum value of the specified ID.
     * @return the maximum value of the specified ID in the specified table.
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public long getMaxID(String idName, String tableName) throws FatalDBUtilLibException;

    /**
     * Execute the update statement (insert, delete, etc - statements that do not query the database) in statement.
     *
     * @param statement update statement to execute
     * @return result code indicating the status of executing the update statement (the row count for INSERT, UPDATE, or
     * DELETE statements or 0 for SQL statements that return nothing)
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public int executeUpdateStatement(String statement) throws FatalDBUtilLibException;

    /**
     * Insert all information in row into the data store represented by this DAO.
     *
     * @param row the Row whose information is to be inserted into the data store represented by this DAO
     * @return whether the insert was successful or not
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public boolean insertRow(Row row) throws FatalDBUtilLibException;

    /**
     * Insert all of the information in row into the database. @param row the Row whose information is to be added to
     * the database. @param ignoreUniqueError whether to ignore errors (true) generated from inserting a row that
     * results in a unique key constraint violation or not (false) @throws FatalDBUtilLibException if a SQL error occurs
     */
    abstract public boolean insertRow(Row row, boolean ignoreUniqueError) throws FatalDBUtilLibException;

    /**
     * Update information in the row that is in the data store represented by this DAO that corresponds to the Row
     * object.
     *
     * @param row the Row whose information is being updated in the data store represented by this DAO
     * @return whether the update was successful or not
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public boolean updateRow(Row row) throws FatalDBUtilLibException;

    /**
     * Delete the row that corresponds to the row object from the data store represented by this DAO.
     *
     * @param row the Row that is to be deleted from the data store represented by this DAO
     * @return whether the delete was successful or not
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public boolean deleteRow(Row row) throws FatalDBUtilLibException;

    /**
     * Write a Row of IDGapsTable information to the data store represented by this DAO.
     *
     * @param idGapsTable IDGapsTable Object that needs to have a row written to the data store represented by this DAO
     * @param values      values used to replace the ?'s in the IDGapTable's PreparedStatement string
     * @throws FatalDBUtilLibException if an error occurs
     */
    abstract public void writeIDGapsRow(IDGapsTable idGapsTable, Object[] values) throws FatalDBUtilLibException;

    /**
     * Forces all in memory information to be written out to the data store represented by this DAO.
     */
    abstract public void commit();

    /**
     * Rolls data store represented by this DAO state back to last commit point.
     */
    abstract public void rollback();

    /*******************************************************************************************************************
     * Sandy, in the comments below that state that the whereClause parameter must start with the word 'where' - is that
     * 'where' case sensitive? Can we not add the word where if it's not there?
     ******************************************************************************************************************/
    /**
     * Get an Iterator for all the rows that result from executing a SELECT statement against DAO data. The statement
     * that is executed is of the form: <BR>
     * "SELECT * FROM " + table.name + " " + whereClause. <BR>
     * This method is preferable to using one of the executeSelectStatement methods for situations where the number of
     * Row objects that might be returned could be very large.
     *
     * @param table       the Table object against which the select statement should be executed; the Row objects that are
     *                    returned by this iterator will be members of this Table object
     * @param whereClause the where clause of the sql statement; it must start with the word 'where'
     * @return an Iterator over {@link Row Row} objects
     */
    abstract public Iterator<Row> iterator(Table table, String whereClause);

    /**
     * Return an Iterator over all the rows that result from executing the selectStatement. <br>
     * This method is preferable to using one of the executeSelectStatement methods for situations where the number of
     * {@link DAO.RowWithoutATable DAO.RowWithoutATable} objects that might be returned could be very large. It is
     * preferable because it does not read all of the returned results into memory, but instead maintains an open
     * connection to the database. <br>
     * This method instantiates a {@link DAO.RowWithoutATable DAO.RowWithoutATable} iterator which executes
     * selectStatement. {@link DAO.RowWithoutATable DAO.RowWithoutATable} objects are only created when the next()
     * method is called. <b>Note:</b> {@link DAO.RowWithoutATable DAO.RowWithoutATable} objects are used since this
     * iterator assumes that the selectStatement is not executing against a table defined in the schema. If the table it
     * is executing against is within the schema, and complete rows are what need to be returned (not just a subset of
     * the columns), then use {@link DAO.DAOIterator DAO.DAOIterator} iterator.
     *
     * @param selectStatement select statement to be executed
     * @return an Iterator over {@link DAO.RowWithoutATable DAO.RowWithoutATable} objects; null if an error occurs
     */
    abstract public Iterator<DAO.RowWithoutATable> iterator(String selectStatement);

    @SuppressWarnings("unused")
    abstract private class DAOIterator implements Iterator<Row> {
    }

    @SuppressWarnings("unused")
    abstract private class DAORowWithoutATableIterator implements Iterator<DAO.RowWithoutATable> {
    }

    /**
     * Return a String representation of this DAO object.
     *
     * @return String representation of this DAO object
     */
    @Override
    abstract public String toString();

    /**
     * Specify whether or not to include column information in xml output files. This only affects xml dao's when
     * outputing xml files.
     *
     * @param yesNo if true, column information is included in xml output files, otherwise, column information is not
     *              included.
     */
    abstract public void outputColumnInfo(boolean yesNo);

    /**
     * Returns a Jaxb representation of this DAO.
     *
     * @return Jaxb representation of this DAO
     * @throws JAXBException if a JAXB error occurs
     */
    abstract public gov.sandia.gnem.dbutillib.jaxb.DAO toJaxb() throws JAXBException;

    /**
     * Return a DBUtilLib DAO object created from a Jaxb DAO object.
     *
     * @param jaxbDao Jaxb DAO object used to create the DAO object
     * @param schema  schema to be associated with this dao object
     * @return DAO object created from a Jaxb DAO object
     * @throws FatalDBUtilLibException if an error occurs during the DAO object construction
     */
    public static DAO fromJaxb(gov.sandia.gnem.dbutillib.jaxb.DAO jaxbDao, Schema schema)
            throws FatalDBUtilLibException {
        return fromJaxb(jaxbDao, false, schema);
    }

    /**
     * Return a DBUtilLib DAO object created from a Jaxb DAO object.
     *
     * @param jaxbDao    Jaxb DAO object used to create the DAO object
     * @param includeAll whether or not to include all of the jaxbDao information in the new DAO object (includeAll =
     *                   true) or to let the user environment override the jaxbDao settings (includeAll = false)
     * @param schema     schema to be associated with this dao object
     * @return DAO object created from a Jaxb DAO object
     * @throws FatalDBUtilLibException if an error occurs during the DAO object construction
     */
    public static DAO fromJaxb(gov.sandia.gnem.dbutillib.jaxb.DAO jaxbDao, boolean includeAll, Schema schema)
            throws FatalDBUtilLibException {
        ParInfo parInfo = new ParInfo();
        if (includeAll) {
            parInfo.addParameter("Filename", jaxbDao.getFilename());
            parInfo.addParameter("Driver", jaxbDao.getDriver());
            parInfo.addParameter("Instance", jaxbDao.getInstance());
            parInfo.addParameter("Password", jaxbDao.getPassword());
            parInfo.addParameter("DAOType", jaxbDao.getType());
            parInfo.addParameter("Username", jaxbDao.getUsername());
            parInfo.addParameter("TableTablespace", jaxbDao.getTabletablespace());
            parInfo.addParameter("IndexTablespace", jaxbDao.getIndextablespace());
        }
        return DAOFactory.create(schema, parInfo, "");
    }

    /**
     * This class represents a Row that is not associated with a Table. It is essentially a HashMap where the keys in
     * the HashMap are column names and the values are the values that correspond to those column names. Instantiations
     * of this class cannot be used in the same way that Row objects can.
     */
    public class RowWithoutATable {
        /**
         * Where the Row data is stored. Keys are column names, values are the values in the row associated with the
         * column name.
         */
        private HashMap<String, Object> dataMap = new HashMap<String, Object>();

        /**
         * Add a column name/value pair to the dataMap.
         *
         * @param columnName name of the column to be added to the dataMap as the key into the HashMap
         * @param value      value to be associated with columnName within the dataMap HashMap
         */
        protected void addToDataMap(String columnName, Object value) {
            if (columnName == null || columnName.length() == 0) {
                DBDefines.ERROR_LOG.add("RowWithoutATable's addToDataMap"
                        + " recieved a null or length 0 column name. Returning.");
                return;
            }

            this.dataMap.put(columnName.trim().toUpperCase(), value);
        }

        /**
         * Retrieve the value associated with columnName from dataMap.
         *
         * @param columnName name of the column associated with the value to be retrieved from the dataMap HashMap
         * @return value associated with columnName with the dataMap HashMap
         */
        public Object getFromDataMap(String columnName) {
            if (columnName == null || columnName.length() == 0) {
                DBDefines.ERROR_LOG.add("RowWithoutATable's getFromDataMap"
                        + " recieved a null or length 0 column name. Returning null.");
                return null;
            }
            return this.dataMap.get(columnName.trim().toUpperCase());
        }

        public HashMap<String, Object> getDataMap() {
            return this.dataMap;
        }
    }
}
