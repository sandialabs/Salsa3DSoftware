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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import javax.xml.bind.JAXBException;

import gov.sandia.gnem.dbutillib.dao.DAO;
import gov.sandia.gnem.dbutillib.dao.DAODatabase;
import gov.sandia.gnem.dbutillib.dao.DAOFactory;
import gov.sandia.gnem.dbutillib.jaxb.ObjectFactory;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * This class handles retrieving information from a table definition table. Currently, only Flatfile and Database Table
 * Definition tables are supported.
 */
public class TableDefinition {
    /**
     * Key containing the SQL String for the preparedStatement above.
     */
    protected String preparedStatementKey = null;

    // Data Access Object for this TableDefinition Object.
    private DAO dao = null;

    // This table definition table's name.
    private String name = null;

    // When reading in table definition information from a flat file, the info
    // is stored in this map. Maps table_name -> arraylist of columns (in
    // order of column_position). Each element of the arraylist is a HashMap
    // which maps column metadata titles to the actual column metadata.
    // (Eg: column_name->value, column_type -> value, etc.)
    // Here is a partially filled out example.
    // origin -> [ <column_name->lat, column_position->1, key->-,
    // external_format->f11.6, ...>,
    // <column_name->lon, column_position->2, key-> -,
    // external_format->f11.6, ...>,
    // ...
    // <column_name->lddatem column_position->25m key->-,
    // external_format->a17:YY/MM/DD HH24:MI:SS, ...>
    // ]
    // assoc -> [ <column_name->arid, column_position->1, key->arid,
    // external_format->i9, ...>,
    // ...
    // <column_name->lddate, column_position->19, key->-,
    // external_format->a17:YY/MM/DD HH24:MI:SS, ...>
    // ]
    private HashMap<String, ArrayList<HashMap<String, String>>> tableDefinitionTable = null;

    // Array of the column names for this Table.
    private String[] columnNames;

    // Column names in the Table Definition Table.
    private static final String tableName = "TABLE_NAME";

    private static final String columnName = "COLUMN_NAME";

    private static final String key = "KEY";

    private static final String naAllowed = "NA_ALLOWED";

    private static final String naValue = "NA_VALUE";

    private static final String internalFormat = "INTERNAL_FORMAT";

    private static final String externalFormat = "EXTERNAL_FORMAT";

    private static final String externalWidth = "EXTERNAL_WIDTH";

    private static final String columnPosition = "COLUMN_POSITION";

    private static final String columnType = "COLUMN_TYPE";

    // private static final String externalType = "EXTERNAL_TYPE";

    /**
     * TableDefinition constructor created with information from a ParInfo object. The parameter names expected (not
     * case sensitive) are listed listed below followed by what's expected to be in those parameters. <BR>
     * [prefix]<B>TableDefinitionTable</B>: this TableDefinition Table's name. If this TableDefinitionTable is in a flat
     * file, this parameter must be set to the name of that flat file. <BR>
     * [prefix]<B>TableDefinitionTableDAOType</B>: this TableDefinition Table's DAO type (FF or DAO - XML is not
     * currently supported) <BR>
     * [prefix]<B>TableDefinitionTableUsername</B>: username for the database account (if any) this TableDefinition
     * Table is in <BR>
     * [prefix]<B>TableDefinitionTablePassword</B>: password for the database account (if any) this TableDefinition
     * Table is in <BR>
     * [prefix]<B>TableDefinitionTableInstance</B>: instance for the database account (if any) this TableDefinition
     * Table is in (an instance example is jdbc:oracle:thin:@bikinifire:1521:histdb) <BR>
     * [prefix]<B>TableDefinitionTableDriver</B>: driver for the database account (if any) this TableDefinition Table is
     * in (a driver example is oracle.jdbc.driver.OracleDriver)
     * <p>
     * <I>Important Note:</I> Many of these parameters make their way into the configInfo object from the user's
     * environment. The following environment variables map to configInfo variables if no others are specified in
     * configInfo.
     * <p>
     * <TABLE>
     * <TR>
     * <TD>DBTOOLS_DAOTYPE</TD>
     * <TD>=></TD>
     * <TD>TableDefinitionDAOType</TD>
     * </TR>
     * <TR>
     * <TD>DBTOOLS_USERNAME</TD>
     * <TD>=></TD>
     * <TD>TableDefinitionTableUsername</TD>
     * </TR>
     * <TR>
     * <TD>DBTOOLS_PASSWORD</TD>
     * <TD>=></TD>
     * <TD>TableDefinitionTablePassword</TD>
     * </TR>
     * <TR>
     * <TD>DBTOOLS_INSTANCE</TD>
     * <TD>=></TD>
     * <TD>TableDefinitionInstance</TD>
     * </TR>
     * <TR>
     * <TD>DBTOOLS_DRIVER</TD>
     * <TD>=></TD>
     * <TD>TableDefinitionDriver</TD>
     * </TR>
     * <TR>
     * <TD>DBTOOLS_TABLEDEF</TD>
     * <TD>=></TD>
     * <TD>TableDefinitionTable</TD>
     * </TR>
     * </TABLE>
     *
     * @param configInfo parameter values
     * @param prefix     String that par file variables for a particular Schema are prefixed with (e.g. Source or Target or
     *                   My)
     * @throws FatalDBUtilLibException if an error occurs
     */
    public TableDefinition(ParInfo configInfo, String prefix) throws FatalDBUtilLibException {
        String daoType = configInfo.getItem(prefix + "TableDefinitionTableDAOType").toUpperCase();

        if (daoType.equals(DBDefines.XML_DAO)) {
            String error = "Error creating TableDefintion object - DAO of type " + " XML not supported.";
            DBDefines.ERROR_LOG.add(error);
            throw new FatalDBUtilLibException(error);
        }

        if (daoType.equals(DBDefines.FF_DAO)) {
            dao = DAOFactory.create(null, configInfo, prefix + "TableDefinitionTable");
            // We don't have to create the connection since we are going to
            // parse the file below.

            // When the DAOType is FF, the TableDefinitionTable should be the
            // file name that the Table Definition Table information is in.
            name = configInfo.getItem(prefix + "TableDefinitionTable");
            try {
                // See the comments above the declaration of this variable
                // for an explanation of how it functions.
                tableDefinitionTable = new HashMap<String, ArrayList<HashMap<String, String>>>();

                // Read in the table definition table information from a file.
                BufferedReader br = new BufferedReader(new FileReader(new File(name)));

                String line = br.readLine();
                while (line != null) {
                    // When a flat file table definition table is written out
                    // to a file, it is done using the external formats for the
                    // tables that make up the table definition table view.
                    // (Currently these are colassoc and coldescript). Here are
                    // the external formats currently:
                    // table_name a30 column_name a30
                    // column_position i8 key a30
                    // external_format a30 external_width i8
                    // internal_format a30 na_allowed a1
                    // na_value a80 column_type a30
                    // external_type a30

                    // Parse out column information from the line.

                    // table name
                    String table_name = line.substring(0, 30).trim();

                    ArrayList<HashMap<String, String>> columns = tableDefinitionTable.get(table_name);
                    if (columns == null) {
                        columns = new ArrayList<HashMap<String, String>>();
                        tableDefinitionTable.put(table_name, columns);
                    }

                    // Get the Column position out first because we need to add
                    // columns to the ArrayList in column position order.
                    Integer column_position = new Integer(line.substring(62, 70).trim());

                    // Add some empty HashMaps for column positions up to the
                    // column position we are currently processing. We will only
                    // fill the position we are currently processing, but we need
                    // to add some "HashMap spacers" for the column positions
                    // lower than the one we are processing for later use.
                    while (columns.size() < column_position)
                        columns.add(new HashMap<String, String>());

                    // Get the HashMap for the current Column position being
                    // processed
                    HashMap<String, String> column = columns.get(column_position - 1);
                    // column_name
                    column.put(columnName, line.substring(31, 61).trim());
                    // key (there is a leap between 61 and 71 here since this
                    // is where the column position would be, but that's been
                    // extracted already.
                    column.put(key, line.substring(71, 101).trim());
                    // external_format
                    column.put(externalFormat, line.substring(102, 132).trim());
                    // internal_width
                    column.put(externalWidth, line.substring(133, 141).trim());
                    // internal_format
                    column.put(internalFormat, line.substring(142, 172).trim());
                    // na_allowed
                    column.put(naAllowed, line.substring(204, 205).trim());
                    // na_value
                    column.put(naValue, line.substring(206, 286).trim());
                    // column_type
                    column.put(columnType, line.substring(286, 316).trim());
                    // external_type
                    // column.put(externalType, line.substring(317).trim());

                    // Read the next line
                    line = br.readLine();
                }
                br.close();
            }
            // This catch block for a generic Exception catches any Exceptions
            // that might be thrown from reading a file or calling substring
            // on different lines in the file.
            catch (Exception ex) {
                throw new FatalDBUtilLibException(("ERROR in TableDefinition constructor while trying to read file\n"
                        + name + "\n" + ex.getMessage()));
            }
        }
        // Read table definition table data from the database.
        else if (daoType.equals(DBDefines.DATABASE_DAO)) {
            dao = DAOFactory.create(null, configInfo, prefix + "TableDefinitionTable");
            dao.createConnection();

            name = configInfo.getItem(prefix + "TableDefinitionTable");

            if (name == null || name.length() == 0) {
                String error = "Error in TableDefintion constructor.  " + prefix + "TableDefinitionTable is undefined.";
                throw new FatalDBUtilLibException(error);
            }

            // Generate an array of column names that we will use when creating
            // a PreparedStatement key for retrieving table definition table
            // information from the database.
            // columnNames = new String[]{columnName, key, naAllowed, naValue,
            // internalFormat, externalFormat, externalWidth, columnType,
            // externalType
            // };

            columnNames = new String[]{columnName, key, naAllowed, naValue, internalFormat, externalFormat,
                    externalWidth, columnType};

            // create a SQL prepared statement key.
            StringBuilder buf = new StringBuilder("SELECT");
            String delimeter = " ";
            for (int i = 0; i < columnNames.length; i++) {
                buf.append(delimeter + columnNames[i]);
                delimeter = ", ";
            }
            buf.append(" FROM " + name);

            if (DBDefines.convertToUpperCase) {
                buf.append(" WHERE UPPER(");
                buf.append(tableName);
                buf.append(")=? ORDER BY ");
            } else {
                buf.append(" WHERE ");
                buf.append(tableName);
                buf.append(" =? ORDER BY ");
            }
            buf.append(columnPosition);
            preparedStatementKey = buf.toString();
        }
    }

    /**
     * Close this object's database connections (if any).
     */
    public void close() {
        if (tableDefinitionTable != null)
            tableDefinitionTable.clear();
        if (dao != null)
            dao.closeConnection();
        for (ArrayList<PreparedStatement> preparedStatements : this.preparedStatementPool.values()) {
            for (PreparedStatement preparedStatement : preparedStatements) {
                try {
                    preparedStatement.close();
                } catch (SQLException ex) {
                    // Ignore closing errors
                }
            }
        }
        tableDefinitionTable = null;
    }

    /**
     * Retrieve this table definition table's dao object
     *
     * @return this table definition table's dao object
     */
    public DAO getDAO() {
        return this.dao;
    }

    /**
     * Retrieve the name of this TableDefinition object.
     *
     * @return the name of this TableDefinition object.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Retrieve the names of the columns in this Table Definition Table
     *
     * @return the names of the columns in this Table Definition Table
     */
    public String[] getColumnNames() {
        return this.columnNames;
    }

    /**
     * Retrieve an array of Column objects for the columns in a particular Table type.
     *
     * @param tableType table type to retrieve Columns for
     * @return Column array containing Columns for the specified table type
     * @throws FatalDBUtilLibException if an error occurs
     */
    public Column[] getColumns(String tableType) throws FatalDBUtilLibException {
        boolean convertToUpperCase = "true".equals(System.getProperty("dbutillib.convertToUpperCase"));

        // Since TableDefinition owns the PreparedStatement key string that
        // upper cases the table name within the SQL, do the upper casing of
        // the table type to search for in this class instead of DAODatabase.

        // Returns an ArrayList of HashMaps. Each HashMap defines an association
        // between the name of some piece of column metadata and the value that
        // corresponds to that name. Some names are COLUMN_NAME and NA_VALUE
        // which could have values like ORID and -1 respectively.
        ArrayList<HashMap<String, String>> rows = null;
        if (dao != null && dao.getType().equals(DBDefines.DATABASE_DAO)) {
            try {
                rows = dao.executeSelectStatement(this, tableType.toUpperCase());
            } catch (FatalDBUtilLibException e) {
                String error = "Error in TableDefinition.getColumns(" + tableType + ").\nError message: "
                        + e.getMessage();
                throw new FatalDBUtilLibException(error);
            }
        }
        // DAOType == FF (can't be XML since the constructor does not allow it)
        else if (tableDefinitionTable != null) {
            // / Populate rows from the FF information.
            rows = tableDefinitionTable.get(tableType.toLowerCase());
            if (rows == null)
                throw new FatalDBUtilLibException("ERROR in TableDefinition.getColumns().  Table "
                        + "definition table in flatfile\n" + name
                        + "does not contain information for a table of type\n" + tableType);
        }

        // Create the Column objects.
        Column[] columns = new Column[rows.size()];
        int i = 0;
        // Get all the column information out of rows.
        for (HashMap<String, String> row : rows) {
            // Get all of column's metadata out of the returned result.
            String naVal = row.get(naValue);
            String naAll = row.get(naAllowed).toUpperCase();
            if (naAll.startsWith("N") || naAll.startsWith("F"))
                naVal = "NA";
            if (convertToUpperCase) {
                columns[i++] = new Column(row.get(columnName).toUpperCase(), row.get(internalFormat).toUpperCase(), row
                        .get(key).toUpperCase(), row.get(columnType), row.get(externalFormat), row.get(externalWidth),
                        naVal, null, // no units
                        null, // no range
                        null // no description
                );
            } else {
                columns[i++] = new Column(row.get(columnName), row.get(internalFormat).toUpperCase(), row.get(key), row
                        .get(columnType), row.get(externalFormat), row.get(externalWidth), naVal, null, // no units
                        null, // no range
                        null // no description
                );
            }
        }
        return columns;
    }

    /**
     * Return all table types for this particular TableDefinition object.
     *
     * @return all table types for this particular TableDefinition object.
     * @throws FatalDBUtilLibException if an error occurs
     */
    public String[] getTableTypes() throws FatalDBUtilLibException {
        String[] tables = null;
        if (dao != null && dao.getType().equals(DBDefines.DATABASE_DAO)) {
            // Retrieve all of the unique table names.
            String sql = "SELECT unique(" + tableName + ") FROM " + name;

            // We get out DAO.RowWithoutATable objects when we use the
            // executeSelectStatement(String).
            LinkedList<DAO.RowWithoutATable> rows = new LinkedList<DAO.RowWithoutATable>();
            try {
                rows = dao.executeSelectStatement(sql);
            } catch (FatalDBUtilLibException e) {
                String error = "Error in TableDefinition.getTableTypes when executing " + "select statement: " + sql
                        + ".\nError " + "message: " + e.getMessage();
                throw new FatalDBUtilLibException(error);
            }

            tables = new String[rows.size()];
            int i = 0;
            for (DAO.RowWithoutATable row : rows)
                tables[i++] = (String) row.getFromDataMap(tableName);
        }
        // DAOType == FF (cannot be XML because that is not allowed in the
        // constructor.
        else if (tableDefinitionTable != null) {
            tables = new String[tableDefinitionTable.size()];
            int i = 0;
            for (String tName : tableDefinitionTable.keySet())
                tables[i++] = tName;
        }
        return tables;
    }

    private DAO getDao() {
        return this.dao;
    }

    /**
     * Return a string representation of this TableDefinition object
     *
     * @return a string representation of this TableDefinition object
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Table Definition Table:  " + name + DBDefines.EOLN);
        if (dao != null && dao.getType().equals(DBDefines.DATABASE_DAO))
            s.append(dao.toString());
        else if (tableDefinitionTable != null)
            s.append("Flatfile: " + name);
        return s.toString();
    }

    /**
     * Return a jaxb version of this TableDefinition object.
     *
     * @return a jaxb version of this TableDefinition object.
     * @throws JAXBException if a conversion error occurs
     */
    public gov.sandia.gnem.dbutillib.jaxb.TableDefinition toJaxb() throws JAXBException {
        gov.sandia.gnem.dbutillib.jaxb.TableDefinition jaxbTableDef = new ObjectFactory().createTableDefinition();
        jaxbTableDef.setName(this.name);
        jaxbTableDef.setDAO(this.dao.toJaxb());

        return jaxbTableDef;
    }

    /**
     * Return a TableDefinition object created from a jaxb version of a TableDefinition object.
     *
     * @param jaxbTableDef jaxb version of a TableDefinition table
     * @return a TableDefinition object created from a jaxb version of a TableDefinition object.
     * @throws FatalDBUtilLibException if a conversion error occurs
     */
    public static TableDefinition fromJaxb(gov.sandia.gnem.dbutillib.jaxb.TableDefinition jaxbTableDef)
            throws FatalDBUtilLibException {
        if (jaxbTableDef == null)
            return null;

        gov.sandia.gnem.dbutillib.jaxb.DAO jaxbDao = jaxbTableDef.getDAO();

        ParInfo parInfo = new ParInfo();

        parInfo.addParameter("TableDefinitionTable", jaxbTableDef.getName());
        parInfo.addParameter("TableDefinitionTableDAOType", jaxbDao.getType());

        TableDefinition tableDef = new TableDefinition(parInfo, "");
        return tableDef;
    }

    /**
     * Return a TableDefinition object created from a jaxb version of a TableDefinition object that includes dao
     * information.
     *
     * @param jaxbTableDef jaxb version of a TableDefinition table
     * @param includeDAO   whether to include dao information (true) or not (false)
     * @return a TableDefinition object created from a jaxb version of a TableDefinition object.
     * @throws FatalDBUtilLibException if a conversion error occurs
     */
    public static TableDefinition fromJaxb(gov.sandia.gnem.dbutillib.jaxb.TableDefinition jaxbTableDef,
                                           boolean includeDAO) throws FatalDBUtilLibException {
        if (!includeDAO)
            return fromJaxb(jaxbTableDef);

        gov.sandia.gnem.dbutillib.jaxb.DAO jaxbDao = jaxbTableDef.getDAO();

        ParInfo parInfo = new ParInfo();
        parInfo.addParameter("TableDefinitionTable", jaxbTableDef.getName());
        parInfo.addParameter("TableDefinitionTableDAOType", jaxbDao.getType());
        parInfo.addParameter("TableDefinitionTableUsername", jaxbDao.getUsername());
        parInfo.addParameter("TableDefinitionTablePassword", jaxbDao.getPassword());
        parInfo.addParameter("TableDefinitionTableDriver", jaxbDao.getDriver());
        parInfo.addParameter("TableDefinitionTableInstance", jaxbDao.getInstance());
        parInfo.addParameter("TableDefinitionTableFilename", jaxbDao.getFilename());
        TableDefinition tableDef = new TableDefinition(parInfo, "");
        return tableDef;
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
    public PreparedStatement getPreparedStatement(Connection connection) throws SQLException {
        synchronized (this.preparedStatementPool) {
            if (this.preparedStatementPool.get(connection) == null)
                this.preparedStatementPool.put(connection, new ArrayList<PreparedStatement>());
            if (this.preparedStatementPool.get(connection).size() == 0)
                this.preparedStatementPool.get(connection).add(connection.prepareStatement(this.preparedStatementKey));
            return this.preparedStatementPool.get(connection).remove(0);
        }
    }

    /**
     * Release a PreparedStatement for future use by this object.
     *
     * @param connection        Connection preparedStatement is associated with
     * @param preparedStatement PreparedStatement to release for future use by this object.
     */
    public void releasePreparedStatement(Connection connection, PreparedStatement preparedStatement) {
        if (preparedStatement != null)
            synchronized (this.preparedStatementPool) {
                this.preparedStatementPool.get(connection).add(preparedStatement);
            }
    }

    /**
     * Return the key used to create PreparedStatments for this Relationship object.
     *
     * @return the key used to create PreparedStatments for this Relationship object.
     */
    public String getPreparedStatementKey() {
        return this.preparedStatementKey;
    }

    public boolean equals(TableDefinition otherTableDefinition) {
        // First check dao types
        if (!otherTableDefinition.getDao().getType().equals(this.dao.getType()))
            return false;

        // Then check the actual table name
        if (!otherTableDefinition.getName().equals(this.name))
            return false;

        // If we have gotten to here, the dao types are the same and the table names are the same.
        // If the dao type is flat file, then that is as far as we need to go.
        if (this.dao.getType().equals(DBDefines.FF_DAO))
            return true;

        // Now compare the fields in a database dao
        DAODatabase thisDao = (DAODatabase) this.dao;
        DAODatabase otherDao = (DAODatabase) otherTableDefinition.getDao();

        return thisDao.getUserName().equals(otherDao.getUserName())
                && thisDao.getPassword().equals(otherDao.getPassword())
                && thisDao.getDriver().equals(otherDao.getDriver())
                && thisDao.getInstance().equals(otherDao.getInstance());
    }
}
