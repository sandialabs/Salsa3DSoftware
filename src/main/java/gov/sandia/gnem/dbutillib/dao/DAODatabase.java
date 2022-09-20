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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;

import gov.sandia.gnem.dbutillib.Column;
import gov.sandia.gnem.dbutillib.IDGapsTable;
import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Relationship;
import gov.sandia.gnem.dbutillib.Row;
import gov.sandia.gnem.dbutillib.RowGraph;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.Table;
import gov.sandia.gnem.dbutillib.TableDefinition;
import gov.sandia.gnem.dbutillib.jaxb.ObjectFactory;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * {@link DAO DAO (Data Access Object)} class implementation for database access. This class is intended to handle
 * <I>all</I> interaction with the database.
 */
public class DAODatabase extends DAO {
    /**
     * This database dao's name.
     */
    private String name = "";

    /**
     * Schema that owns this dao.
     */
    private Schema schema;

    /**
     * Type of dao this is. See DBDefines for a listing of the available DAO types.
     */
    private final static String TYPE = DBDefines.DATABASE_DAO;

    /**
     * Connection to the database.
     */
    private Connection connection;

    /**
     * Keeps track of whether or not this object has closed its connection. Don't want the finalize method to call
     * closeConnection if that connection has already been closed.
     */
    private boolean connectionClosed = false;

    /**
     * Pool of statement objects. These are needed for threading since only one ResultSet can be associated with a
     * Statement. However, it is time consuming to create these Statement objects, so keep them around for future use
     * when a method is done with them.
     */
    private LinkedList<Statement> statements;

    /**
     * An association of a userName/password@instance -> Connections so that multiple connections to the same database
     * account are not opened needlessly.
     */
    private static HashMap<String, Connection> connections = new HashMap<String, Connection>();

    /**
     * An association of userName/password@instance -> number of users of the Connection represented by that
     * connectionKey. By keeping track of this, the code can avoid closing down a database connection that was being
     * used by multiple users if a user is still using it.
     */
    private static HashMap<String, Integer> connectionsUsers = new HashMap<String, Integer>();

    /**
     * Database username/account.
     */
    private String username;

    /**
     * Database password.
     */
    private String password;

    /**
     * Database instance.
     */
    private String instance;

    /**
     * JDBC database driver class name.
     */
    private String driver;

    /**
     * Tablespace to create tables in. (Not necessarily specified.)
     */
    private String tableTablespace;

    /**
     * Tablespace to create indexes in. (Not necessarily specified.)
     */
    private String indexTablespace;

    /**
     * Base part of the URI (Uniform Resource Identifier) for a table in this database of the form:
     * database://username/password@instance Table name should be added, as appropriate.
     */
    private String uriBase;

    /**
     * Auto Commit settings for the database. If autoCommit is true (on), then a commit is performed after every
     * database call; this is very slow. If autoCommit is false (off), then commits must be forced by explicit calls to
     * {@link #commit() commit}. This can increase speed considerably since commits are only done when necessary.
     * ImportantNote: When the database connection is closed (either through explicitly calling
     * {@link #closeConnection() closeConnection()} or when the object is garbage collected and {@link #finalize()
     * finalize()} is called, a commit will be executed automatically.
     */
    private boolean autoCommit = false;

    /**
     * Key used in {@link #connections connections} HashMap. Combination of
     * username/password@instance+driver+autocommit.
     */
    private String connectionKey = null;

    // This is commented out because, according to http://java.sun.com/javase/6/docs/api/java/sql/Statement.html
    // "By default, only one ResultSet object per Statement object can be open at the same time. Therefore, if the
    // reading of one ResultSet object is interleaved with the reading of another, each must have been generated by
    // different Statement objects. All execution methods in the Statement interface implicitly close a statment's
    // current ResultSet object if an open one exists."
    // /** Statement object for this database connection */
    // private Statement stmt;

    /**
     * When applications request the set of all table names accessible to the user's account, only look for "tables" of
     * the types in this variable. This variable gets populated by the {@link #populateObjectTypes()
     * populateObjectTypes()} function.
     */
    private static LinkedList<String> objectTypes;

    /**
     * When applications request the set of all table names accessible to the user's account, do not include tables from
     * the accounts in this variable. This variable gets populated by the {@link #populateExcludeAccounts()
     * populateExcludeAccounts()} function.
     */
    private static LinkedList<String> excludeAccounts;

    /**
     * Initializes username/password/instance/driver/auto commit for this database connection with the following
     * parameters names (prefixed by prefix) in the configInfo object. <br>
     * Username - Database username (retrieved from ParInfo object using {@link ParInfo#getItem(String)
     * ParInfo.getItem(ParInfo.USERNAME)} <br>
     * Password - Database password (retrieved from ParInfo object using {@link ParInfo#getItem(String)
     * ParInfo.getItem(ParInfo.PASSWORD)} <br>
     * Instance - Database instance of the form jdbc:oracle:thin:@bikinifire:1521:histdb (retrieved from ParInfo object
     * using {@link ParInfo#getItem(String) ParInfo.getItem(ParInfo.INSTANCE)} <br>
     * Driver - Database jdbc driver of the form oracle.jdbc.driver.OracleDriver (retrieved from ParInfo object using
     * {@link ParInfo#getItem(String) ParInfo.getItem(ParInfo.DRIVER)} <br>
     * AutoCommit - Database auto commit behavior (optional) (retrieved from ParInfo object using
     * {@link ParInfo#getItem(String) ParInfo.getItem(ParInfo.AUTO_COMMIT)}
     *
     * @param schema     the schema that owns this database connection
     * @param configInfo ParInfo object that parameter values are being read from
     * @param prefix     String to prefix parameter names with before retrieving those parameters from configInfo
     * @throws FatalDBUtilLibException if any fatal errors are encountered while creating this DAODatabase object
     */
    protected DAODatabase(Schema schema, ParInfo configInfo, String prefix) throws FatalDBUtilLibException {
        this.schema = schema;
        this.name = prefix;
        this.username = configInfo.getItem(prefix + ParInfo.USERNAME);
        this.password = configInfo.getItem(prefix + ParInfo.PASSWORD);
        this.instance = configInfo.getItem(prefix + ParInfo.INSTANCE);
        this.driver = configInfo.getItem(prefix + ParInfo.DRIVER);
        this.tableTablespace = configInfo.getItem(prefix + ParInfo.TABLE_TABLESPACE);
        this.indexTablespace = configInfo.getItem(prefix + ParInfo.INDEX_TABLESPACE);

        if (this.username == null || this.password == null || this.instance == null || this.driver == null)
            throw new DBDefines.FatalDBUtilLibException(
                    "DAODatabase constructor could not find a valid username, password, instance, and/or"
                            + " driver\nValues - username: " + this.username + " password: " + this.password
                            + " instance: " + this.instance + " driver: " + this.driver + ". Prefix used: " + prefix);

        if (this.password.matches("[\\*]*"))
            throw new DBDefines.FatalDBUtilLibException(
                    "DAODatabase constructor was passed in a password whose value is all asterisks (*)."
                            + " This is not a valid password.");

        this.autoCommit = configInfo.getItem(prefix + ParInfo.AUTO_COMMIT, "false").toLowerCase().startsWith("t");

        // make a string out of all the connection information to be used as the key in the connection hashmap and
        // output in error messages.
        this.connectionKey = "UserName  = " + this.username + DBDefines.EOLN + "Password  = " + this.password
                + DBDefines.EOLN + "Instance  = " + this.instance + DBDefines.EOLN + "Driver    = " + this.driver
                + DBDefines.EOLN + "Autocommit= ";
        if (this.autoCommit)
            this.connectionKey = this.connectionKey + "true" + DBDefines.EOLN;
        else
            this.connectionKey = this.connectionKey + "false" + DBDefines.EOLN;
        this.statements = new LinkedList<Statement>();
        this.uriBase = "database://" + this.username + "@" + this.instance;
    }

    /** ~*~*~*~*~*~*~*~ START DAO Abstract Method Implementation ~*~*~*~*~*~*~*~ */
    /**
     * Returns what DAO type this class represents. See DBDefines for a listing of the available DAO types.
     *
     * @return DAO type this class represents
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Returns the URI of a particular table in this database. This will be something like:
     * database://username/password@instance.table_name
     *
     * @param table the Table object whose URI is requested
     * @return the URI of a particular table in this database
     * @throws FatalDBUtilLibException if an error occurs when retrieving/creating the URI for the specified table
     */
    @Override
    public String getURI(Table table) throws FatalDBUtilLibException {
        String inst = this.instance;
        // Given an instance of such as jdbc:oracle:thin:@bikinifire:1521:histdb, trim off everything up to the @. If
        // instance does not have an @ at the front, add it!
        int i = inst.indexOf("@");
        if (i >= 0)
            inst = inst.substring(i);
        else
            inst = "@" + inst;

        String tablename = table.getName().toUpperCase();
        String account = this.username.toUpperCase();
        // Extract account information out of the table name if it's there. This tends to happen in cases such as where
        // the username/password for this database connection is one thing and the user has specified
        // different_account.publicly_selectable_table.
        i = tablename.indexOf('.');
        if (i > 0) {
            account = tablename.substring(0, i).toUpperCase();
            tablename = tablename.substring(i + 1).toUpperCase();
        }

        // Extract information about this table. This creates a statement of the form:
        // SELECT object_type FROM all_objects WHERE owner=account AND object_name = tablename
        LinkedList<Object> objTypes = executeSelectStatement("object_type", "all_objects", "owner='" + account
                + "' and object_name='" + tablename + "'", "");

        // Handle if this is a synonym, not a table.
        if (objTypes.size() > 0 && objTypes.getFirst().toString().equals("SYNONYM")) {
            LinkedList<String> cols = new LinkedList<String>();
            cols.add("TABLE_OWNER");
            cols.add("TABLE_NAME");
            cols.add("DB_LINK");
            // SELECT table_owner, table_name, db_link FROM all_synonyms WHERE synonym_name=table_name
            LinkedList<LinkedList<Object>> objects = executeSelectStatement(cols, "all_synonyms", "synonym_name='"
                    + tablename + "'", "");

            if (objects.size() != 1)
                throw new DBDefines.FatalDBUtilLibException("ERROR in DBUtilLib.DAODatabase.getURI(Table).\n"
                        + tablename + " was listed as a synonym in the ALL_OBJECTS table but is not "
                        + "present in the ALL_SYNONYMS table.\nSQL to retrieve information from the "
                        + "ALL_OBJECTS table:\n\tSELECT object_type FROM all_objects WHERE owner = '" + account
                        + "' AND object_name = '" + tablename + "'\nSQL to to retrieve information from the "
                        + "ALL_SYNONYMS table:\n\tSELECT table_owner, table_name, db_link FROM all_synonyms "
                        + "WHERE synonym_name = '" + tablename + "'");

            // Extract account, tablename, and instance information
            LinkedList<Object> info = objects.getFirst();
            // TABLE_OWNER column
            account = info.get(0).toString();
            // TABLE_NAME column
            tablename = info.get(1).toString();
            // DB_LINK column (if present)
            if (info.size() > 2 && info.get(2) != null)
                inst = info.get(2).toString().toUpperCase();
        }
        // Not a synonym, not a table, not a view ... ERROR!
        else if (objTypes.size() == 0
                || (!objTypes.getFirst().toString().equalsIgnoreCase("TABLE") && !objTypes.getFirst().toString()
                .equalsIgnoreCase("VIEW")))
            throw new DBDefines.FatalDBUtilLibException("ERROR in DBUtilLib.DAODatabase.getURI(Table). " + tablename
                    + " is not a database table, view, or synonym.\n");

        // Deal with (remove) all the weird information that can be embedded in an instance definition
        i = inst.lastIndexOf("@");
        if (i >= 0)
            inst = inst.substring(i + 1);
        i = inst.lastIndexOf(":");
        if (i >= 0)
            inst = inst.substring(i + 1);
        if (inst.endsWith(".WORLD"))
            inst = inst.substring(0, inst.lastIndexOf(".WORLD"));

        return ("db:" + inst + "." + account + "." + tablename).toLowerCase();
    }

    /**
     * Return a ParInfo object populated with information about this dao object. The following parameters will be in the
     * returned ParInfo object (prefixed by this DAODatabase's name): <br> {@link ParInfo#DAO_TYPE ParInfo.DAO_TYPE} <br>
     * {@link ParInfo#USERNAME ParInfo.USERNAME} <br> {@link ParInfo#PASSWORD ParInfo.PASSWORD} <br> {@link ParInfo#INSTANCE
     * ParInfo.INSTANCE} <br> {@link ParInfo#DRIVER ParInfo.DRIVER} <br> {@link ParInfo#AUTO_COMMIT ParInfo.AUTO_COMMIT} <br>
     * {@link ParInfo#TABLE_TABLESPACE ParInfo.TABLE_TABLESPACE} <br> {@link ParInfo#INDEX_TABLESPACE
     * ParInfo.INDEX_TABLESPACE} <br>
     *
     * @return ParInfo object populated with information about this dao object
     */
    @Override
    public ParInfo getParInfo() {
        ParInfo parInfo = new ParInfo();
        parInfo.addParameter(this.name + ParInfo.DAO_TYPE, TYPE);
        parInfo.addParameter(this.name + ParInfo.USERNAME, this.username);
        parInfo.addParameter(this.name + ParInfo.PASSWORD, this.password);
        parInfo.addParameter(this.name + ParInfo.INSTANCE, this.instance);
        parInfo.addParameter(this.name + ParInfo.DRIVER, this.driver);
        if (this.autoCommit)
            parInfo.addParameter(this.name + ParInfo.AUTO_COMMIT, "true");
        else
            parInfo.addParameter(this.name + ParInfo.AUTO_COMMIT, "false");
        parInfo.addParameter(this.name + ParInfo.TABLE_TABLESPACE, this.tableTablespace);
        parInfo.addParameter(this.name + ParInfo.INDEX_TABLESPACE, this.indexTablespace);
        return parInfo;
    }

    /**
     * Return whether or not this DAODatabase object equals the specified DAO Object. Two DAO's are not equal if they do
     * not have the same type (db, ff, xml). Two DatabaseDAO's are equal if and only if they have the same username,
     * password and instance (driver does not matter).
     *
     * @param otherDAO Object the other dao object to which this dao object is to be compared.
     * @return boolean true if they are the same.
     */
    @Override
    public boolean equals(Object otherDAO) {
        if (!otherDAO.getClass().toString().equals(this.getClass().toString()))
            return false;
        if (!((DAO) otherDAO).getType().equals("DB"))
            return false;
        DAODatabase other = (DAODatabase) otherDAO;
        if (!other.getUserName().equals(this.username))
            return false;
        if (!other.getPassword().equals(this.password))
            return false;
        if (!other.getInstance().equals(this.instance))
            return false;
        return true;
    }

    /**
     * Create a connection with the database. Since it is possible that more than one user will want to connect to the
     * same database with the same username, password, driver, and instance, this function maintains a HashMap of
     * database connections. If the user is trying to connect to a database that already has an open connection to it, a
     * new connection will not be created and the existing connection will be used instead.
     *
     * @throws FatalDBUtilLibException if an error occurs while creating the connection to the database
     */
    @Override
    public void createConnection() throws FatalDBUtilLibException {
        this.connectionClosed = false;
        try {
            // If this dao already has an open connection to the database, don't create another one.
            if (this.connection != null && !this.connection.isClosed())
                return;

            // If the connection is null look for one in the hash map of connections
            if (this.connection == null)
                this.connection = connections.get(this.connectionKey);

            // if an open database connection already exists
            if (this.connection != null && !this.connection.isClosed()) {
                // Keep track of how many users (instantiations of this class) are using the same Connection. This is
                // useful because if more than one user is using the Connection, and one of the users calls
                // closeConnection, we don't want to actually close it for all of the users, it just needs to be closed
                // for the user who called the function. If the user who calls closeConnection is the last user to call
                // it, then the Connection is actually closed.
                connectionsUsers.put(this.connectionKey, new Integer(connectionsUsers.get(this.connectionKey)
                        .intValue() + 1));
            } else {
                // Check if the jdbc drivers could be loaded.
                try {
                    // Causes jdbc drivers to be initialized
                    Class.forName(this.driver);
                } catch (Exception e) {
                    StringBuilder msg = new StringBuilder(
                            "FATAL ERROR in DAODatabase.createConnection() when trying"
                                    + " to load the jdbc drivers.  "
                                    + this.driver
                                    + " was specified as the name of the jdbc "
                                    + "drivers, but the package containing these drivers cannot be located. Please ensure that "
                                    + "this package appears in the classpath being used to run this application. (DBUtilLib users "
                                    + "typically place the drivers in the $SNL_TOOL_ROOT/Support/oracle_jdbc_lib/oracle_jdbc_lib."
                                    + "<jdbc version> directory.)\n");
                    msg.append("Database state information: " + this.connectionKey);
                    msg.append("\nCurrent time: " + new Date());
                    msg.append("Exception message: " + e.getMessage());
                    throw new DBDefines.FatalDBUtilLibException(msg.toString());
                }

                // Attempt to connect to the database

                // The instance needs to be in lower case in order to work properly.
                this.connection = DriverManager
                        .getConnection(this.instance.toLowerCase(), this.username, this.password);
                this.connection.setAutoCommit(this.autoCommit);

                // add entries to the hashmaps that keep track of db connections.
                connections.put(this.connectionKey, this.connection);
                connectionsUsers.put(this.connectionKey, new Integer(1));
            }
        } catch (SQLException e) {
            StringBuilder msg = new StringBuilder("FATAL ERROR in DAODatabase.createConnection().\n");
            msg.append("Connection key: " + this.connectionKey);
            msg.append("\nCurrent time: " + new Date());
            msg.append("DAODatabase SQLException: " + e.getMessage());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        }

        //
        // // create a sql statement object for this db connection
        // if (this.stmt == null)
        // {
        // try
        // {
        // this.stmt = this.connection.createStatement();
        // }
        // catch (SQLException e)
        // {
        // String msg = "ERROR in DAODatabase createConnection encountered while attempting to create a Statement "
        // + "object for this DAO object.\nConnection key: "
        // + this.connectionKey
        // + "\nException Message: " + e.getMessage();
        // throw new DBDefines.FatalDBUtilLibException(msg);
        // }
        // }
    }

    /**
     * Return a connection object that can be managed external to DAODatabase for when threads need control over a
     * transaction for commit and rollback purposes.
     *
     * @return a connection object that can be managed external to DAODatabase for when threads need control over a
     * transaction for commit and rollback purposes.
     * @throws FatalDBUtilLibException is an error occurs when creating the connection
     */
    public Connection createConnectionForThreads() throws FatalDBUtilLibException {
        Connection conn = null;
        try {
            // The instance needs to be in lower case in order to work properly.
            conn = DriverManager.getConnection(this.instance.toLowerCase(), this.username, this.password);
            conn.setAutoCommit(this.autoCommit);
            return conn;
        } catch (SQLException e) {
            StringBuilder msg = new StringBuilder("FATAL ERROR in DAODatabase.createConnectionForThreads().\n");
            msg.append("Connection key: " + this.connectionKey);
            msg.append("\nCurrent time: " + new Date());
            DBDefines.ERROR_LOG.add("DAODatabase SQLException: " + e.getMessage());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        }
    }

    /**
     * Close a connection that was obtained for use in a threading context
     *
     * @param connectionToClose connection that was obtained for use in a threading context that needs to be closed
     * @throws SQLException if an error occurs while closing the connection
     */
    public void closeConnectionForThreads(Connection connectionToClose) throws SQLException {
        if (connectionToClose != null && !connectionToClose.isClosed()) {
            connectionToClose.commit();
            connectionToClose.close();
        }
    }

    /**
     * Close database connection. Connections do not get garbage collected in a reliable fashion, so they need to be
     * explicitly closed. This class has built into it the ability for separate users (instantiations of this class) to
     * share the same database connection. If more than one user is using a Connection, the Connection will only be
     * closed as far as the user who called this function is concerned - it will remain open for other users who are
     * using the Connection. If the user who calls this function is the last person using the Connection, the Connection
     * will be closed properly.
     */
    @Override
    public void closeConnection() {
        this.connectionClosed = true;

        // close the Statement objects that this DAODatabase object owns.
        try {
            for (Statement statement : this.statements)
                statement.close();
        } catch (SQLException ex) {
            DBDefines.WARNING_LOG.add("DAODatabase Error in closeConnection().  Can't close sql statement.\n");
        }

        // Probably already been closed before
        if (connectionsUsers.get(this.connectionKey) == null)
            return;

        // Check to see if the calling class is the last one using this connection.
        int numberOfUsers = connectionsUsers.get(this.connectionKey).intValue();

        // If there is more than 1 user, just set the calling user's connection to null, decrement the number of users
        // using the connection, and leave the Connection open for others to use it.
        if (numberOfUsers > 1) {
            connectionsUsers.put(this.connectionKey, new Integer(numberOfUsers - 1));
            this.connection = null;
        }
        // Otherwise, this user is the last one - close up the connection and take the Connection out of the connections
        // HashMap.
        else {
            try {
                // Check to see if there is an open, valid connection so we don't throw an exception.
                if (this.connection != null && !this.connection.isClosed())
                    this.connection.close();

                this.connection = null;
                connections.remove(this.connectionKey);
                connectionsUsers.remove(this.connectionKey);
            } catch (SQLException e) {
                String msg = "DAODatabase Error in closeConnection()\nException " + "message: " + e.getMessage();
                DBDefines.WARNING_LOG.add(msg);
            }
        }
    }

    /**
     * Retrieve the set of table names that are accessible to the current user. Includes tables, views, aliases, and
     * synonyms.
     *
     * @param thisUserOnly    if true, then only tables in the current user's account are returned and the names are not
     *                        prepended with the account name. If not true, all tables that are accessible to the user are returned, prepended
     *                        with the account name.
     * @param tableNameFilter if not null and length > 0, then only table names that contain the specified string are
     *                        returned. Filter is not case sensitive.
     * @return the set of all the table names that are accessible to the user, sorted alphabetically.
     */
    @Override
    public TreeSet<String> getAvailableTables(boolean thisUserOnly, String tableNameFilter) {
        if (objectTypes == null)
            populateObjectTypes();
        if (excludeAccounts == null)
            populateExcludeAccounts();

        TreeSet<String> tables = new TreeSet<String>();

        String tnFilter = tableNameFilter;
        if (tnFilter != null)
            tnFilter = tnFilter.trim().toUpperCase();

        // Create a string version of the object types suitable for using in sql
        StringBuilder objectTypesString = new StringBuilder();
        for (String objectType : objectTypes)
            objectTypesString.append("'" + objectType + "', ");
        objectTypesString = new StringBuilder(objectTypesString.substring(0, objectTypesString.length() - 2));

        // Create a string version of the exclude accounts suitable for using in sql
        String excludeAccountsString = excludeAccounts.toString();
        // Remove []
        excludeAccountsString = excludeAccountsString.substring(1, excludeAccountsString.length() - 1);

        // Create the sql indicating owners' information should be returned
        String ownerSql;
        if (thisUserOnly)
            ownerSql = "o.owner = '" + this.username + "'\n";
        else
            ownerSql = "o.owner NOT IN (" + excludeAccountsString + ")\n";

        // Determine what this user can see!
        String sqlStmt = "SELECT NULL AS table_cat,\n" + "    o.owner AS table_schema,\n"
                + "    o.object_name AS table_name,\n" + "    o.object_type AS table_type,\n" + "    NULL AS remarks\n"
                + "  FROM all_objects o\n" + "  WHERE " + ownerSql + "    AND o.object_name LIKE '%'\n"
                + "    AND o.object_type IN (" + objectTypesString.toString() + ")\n"
                + "ORDER BY table_schema, table_name\n";

        // Extract the schema.table_name information from the returned results
        try {
            LinkedList<RowWithoutATable> rows = executeSelectStatement(sqlStmt);
            for (RowWithoutATable row : rows) {
                String schemaName = row.getFromDataMap("table_schema").toString();
                String tableName = row.getFromDataMap("TABLE_NAME").toString();
                if (tnFilter == null || tnFilter.length() == 0 || tableName.indexOf(tnFilter) != -1)
                    tables.add(schemaName + "." + tableName);
            }
        } catch (DBDefines.FatalDBUtilLibException e) {
            DBDefines.ERROR_LOG.add("Error in DAODatabase.getAvailableTables.\n" + e.getMessage());
            e.printStackTrace();
            return new TreeSet<String>();
        }

        return tables;
    }

    /**
     * Return whether or not a sequence exists.
     *
     * @param sequenceName name of the sequence
     * @return whether or not the sequence named sequenceName exists
     */
    @Override
    public boolean sequenceExists(String sequenceName) throws FatalDBUtilLibException {
        String sql = "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
        ResultSet resultSet = null;
        Statement statement = null;
        boolean exists = true;
        try {
            statement = getStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next())
                resultSet.getLong(1);
        } catch (SQLException e) {
            if (e.getMessage().startsWith("ORA-02289: sequence does not exist"))
                exists = false;
            else
                throw new FatalDBUtilLibException("Error in DAODatabase.sequenceExists. " + e.getMessage());
        } finally {
            closeResultSet(resultSet);
            releaseStatement(statement);
        }
        return exists;
    }

    /**
     * Returns whether the specified table exists in this database or not.
     *
     * @param table table to be checked for existence
     * @return if the table exists or not - true, it exists; false, it does not exist
     */
    @Override
    public boolean tableExists(Table table) {
        return tableExists(table.getName());
    }

    /**
     * Returns whether the database table named tableName exists in the database or not.
     *
     * @param tableName name of the table being checked for existence
     * @return whether the database table named tableName exists in the database or not - true, it does exist; false, it
     * does not
     */
    @Override
    public boolean tableExists(String tableName) {
        String tableNamePattern = tableName.toUpperCase();
        String schemaPattern = this.username.toUpperCase();

        // If tableName has a . in it, then the name is probably prefixed by the schema name. Extract that information.
        int i = tableNamePattern.indexOf('.');
        if (i >= 0) {
            schemaPattern = tableNamePattern.substring(0, i);
            tableNamePattern = tableNamePattern.substring(i + 1);
        }

        boolean exists = false;
        ResultSet resultSet = null;
        try {
            // See if the table exists (possibly in another schema with permission for the user to see it)
            if (objectTypes == null)
                populateObjectTypes();
            resultSet = this.connection.getMetaData().getTables(null, schemaPattern, tableNamePattern,
                    objectTypes.toArray(new String[]{}));
            exists = resultSet.next();
        } catch (SQLException e) {
            String msg = "ERROR in DAODatabase.tableExists().  TableName = " + tableName + "\nException message: "
                    + e.getMessage();
            DBDefines.ERROR_LOG.add(msg);
            exists = false;
        } finally {
            closeResultSet(resultSet);
        }
        return exists;
    }

    /**
     * Returns whether the database table named tableName is empty or not.
     *
     * @param tableName name of the table being checked to see if it's empty
     * @return whether table named tableName is empty or not - true, it is empty; false, it is not
     * @throws FatalDBUtilLibException if a database error occurs when checking to see if the table is empty
     */
    @Override
    public boolean tableIsEmpty(String tableName) throws FatalDBUtilLibException {
        // Create sql to see how many rows are in tableName.
        String sql = "SELECT count(*) ROWCOUNT FROM " + tableName.toUpperCase();

        boolean empty = false;
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = getStatement();
            resultSet = statement.executeQuery(sql);
            // Move the cursor forward to the one row (the row count) returned.
            resultSet.next();
            empty = resultSet.getInt("ROWCOUNT") <= 0;
        } catch (SQLException e) {
            throw new DBDefines.FatalDBUtilLibException("Error in DAODatabase.tableIsEmpty(" + tableName
                    + ") SQLException with SQL:\n" + sql + "\nException message " + e.getMessage());
        } finally {
            closeResultSet(resultSet);
            releaseStatement(statement);
        }
        return empty;
    }

    /**
     * Deletes all rows from the table named tableName in the database.
     *
     * @param tableName name of the table having all of its rows deleted
     * @throws FatalDBUtilLibException if a SQL error occurs when deleting all data from the table
     */
    @Override
    public void emptyTable(String tableName) throws FatalDBUtilLibException {
        // Create sql to delete all rows from tablename.
        String sql = "DELETE FROM " + tableName.toUpperCase();
        Statement statement = null;
        try {
            // Delete rows.
            statement = getStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            String msg = "DAODatabase.emptyTable(" + tableName + ") SQLException with SQL:\n" + sql
                    + "\nException message: " + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(msg);
        } finally {
            releaseStatement(statement);
        }
    }

    /**
     * Drop a table named tableName from the database.
     *
     * @param tableName name of the table to be dropped
     * @return whether or not the table was dropped successfully
     */
    @Override
    public boolean dropTable(String tableName) {
        // Create sql to drop the table.
        String sql = "DROP TABLE " + tableName.toUpperCase() + " CASCADE CONSTRAINTS";

        Statement statement = null;
        try {
            // Drop the table.
            statement = getStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            String msg = "ERROR in DAODatabase.dropTable(" + tableName + ") SQLException with SQL:\n" + sql
                    + "\nException message: " + e.getMessage();
            DBDefines.ERROR_LOG.add(msg);
            return false;
        } finally {
            releaseStatement(statement);
        }
        return true;
    }

    /**
     * Truncate the database table that corresponds to this Table object. Truncate deletes all data in the table.
     *
     * @param table the Table object whose corresponding database table is to be truncated.
     * @return whether table was successfully truncated or not
     */
    @Override
    public boolean truncateTable(Table table) {
        String tablename = table.getName();
        if (!tableExists(tablename))
            return false;

        // Create sql to drop the table.
        String sql = "TRUNCATE TABLE " + tablename.toUpperCase();
        Statement statement = null;
        try {
            // Drop the table.
            statement = getStatement();
            statement.execute(sql);
            return true;
        } catch (SQLException e) {
            String msg = "ERROR in DAODatabase.truncateTable(" + tablename + ") SQLException with SQL:\n" + sql
                    + "Exception message: " + e.getMessage();
            DBDefines.ERROR_LOG.add(msg);
            return false;
        } finally {
            releaseStatement(statement);
        }
    }

    /**
     * Creates a table in the database. The table type and name information are extracted from the specified table
     * object. Table Definition Table information for the table type will determine the structure of the table to be
     * created. The primary keys and unique keys as defined by the COLUMN_TYPE in the table definition table will be
     * set, but foreign keys will not.
     *
     * @param table the Table object that defines the table type and name for the database table that is to be created.
     * @return true if the table was created, false otherwise
     * @throws FatalDBUtilLibException if an error occurs when creating the table
     */
    @Override
    public boolean createTable(Table table) throws FatalDBUtilLibException {
        return createTable(table, true, true);
    }

    /**
     * Creates a table in the database. The table type and name information are extracted from the specified table
     * object. Table Definition Table information for the table type will determine the structure of the table to be
     * created.
     *
     * @param table          the Table object that defines the table type and name for the database table that is to be created.
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
        StringBuilder sql = new StringBuilder();
        String tableName = table.getName().toUpperCase();

        // Put together the SQL to create the table.
        sql.append("CREATE TABLE " + tableName + "(" + DBDefines.EOLN);

        // Add column names and types to SQL.
        for (Column column : table.getColumns()) {
            sql.append(column.getName() + " " + column.getSQLType());
            if (!column.NAValueAllowed() || column.getNAValue() != null)
                sql.append(" NOT NULL");

            sql.append("," + DBDefines.EOLN);
        }

        // Remove the last comma and newline and add closing parentheses
        sql.setLength(sql.length() - (1 + DBDefines.EOLN.length()));
        sql.append(")");

        // Add tablespace information if the tablespace was specified
        if (this.tableTablespace != null)
            sql.append(" TABLESPACE " + this.tableTablespace);

        Statement statement = null;
        // Create the table.
        try {
            statement = getStatement();
            statement.execute(sql.toString());
        } catch (SQLException e) {
            String msg = "ERROR in DAODatabase.createTable(" + tableName + "," + setPrimaryKeys + "," + setUniqueKeys
                    + ") when creating the table.\n" + "SQL = " + sql + "\nException message" + e.getMessage();
            DBDefines.ERROR_LOG.add(msg);
            throw new DBDefines.FatalDBUtilLibException(msg);
        } finally {
            releaseStatement(statement);
        }

        // Set the primary and unique key constraints regardless of what the setPrimaryKeys and setUniqueKeys parameters
        // are. The constraints will be disabled later if the corresponding parameter is false.
        setPrimaryKey(table);
        String ukName = setUniqueKey(table);

        // Disable the primary keys if setPrimaryKeys is false.
        if (!setPrimaryKeys) {
            sql.setLength(0);
            sql.append("ALTER TABLE " + tableName + " DISABLE PRIMARY KEY");
            try {
                statement = getStatement();
                statement.execute(sql.toString());
            } catch (SQLException ex) {
                String msg = "ERROR in DAODatabase.createTable(" + tableName + ") when setting the primary keys\n"
                        + "SQL = " + sql + "\nException message" + ex.getMessage();
                DBDefines.ERROR_LOG.add(msg);
                throw new DBDefines.FatalDBUtilLibException(msg);
            } finally {
                releaseStatement(statement);
            }
        }

        // Disable the unique keys if setUniqueKeys is false and we have the name of the constraint to disable. Since
        // there can only be one primary key, the primary key name doesn't have to be specified in the ALTER TABLE
        // statement. However, with unique keys, the constraint name must be specified.
        if (!setUniqueKeys && ukName.length() > 0) {
            sql.setLength(0);
            sql.append("ALTER TABLE " + tableName + " DISABLE CONSTRAINT ");
            sql.append(ukName);
            try {
                statement = getStatement();
                statement.execute(sql.toString());
            } catch (SQLException ex) {
                String msg = "ERROR in DAODatabase.createTable(" + tableName + ") when setting the unique keys\n"
                        + "SQL = " + sql + "\nException message" + ex.getMessage();
                DBDefines.ERROR_LOG.add(msg);
                throw new DBDefines.FatalDBUtilLibException(msg);
            } finally {
                releaseStatement(statement);
            }
        }

        return true;
    }

    /**
     * Set the primary key on the specified Table. Primary key information is retrieved from the table definition table.
     *
     * @param table table to set the primary key on
     * @return name of the primary key ("" if the table does not have primary key information in the table definition
     * table)
     * @throws FatalDBUtilLibException if an error occurs when setting the primary key
     */
    @Override
    public String setPrimaryKey(Table table) throws FatalDBUtilLibException {
        String primaryKeys = table.getPrimaryKey();
        // No primary keys to set on this table
        if (primaryKeys.length() == 0)
            return "";

        String tableName = table.getName();
        String pkName = generateConstraintName("PK_" + tableName.substring(tableName.indexOf(".") + 1));

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE " + tableName);
        sql.append(" ADD CONSTRAINT " + pkName);
        sql.append(" PRIMARY KEY (" + primaryKeys + ") ENABLE VALIDATE");
        if (this.indexTablespace != null)
            sql.append(" USING INDEX TABLESPACE " + this.indexTablespace);

        Statement statement = null;
        try {
            statement = getStatement();
            statement.execute(sql.toString().toUpperCase());
        } catch (SQLException ex) {
            throw new DBDefines.FatalDBUtilLibException("ERROR in DAODatabase.setPrimaryKey(). SQL = \n" + sql + "\n"
                    + ex.getMessage() + "\n");
        } finally {
            releaseStatement(statement);
        }
        return pkName;
    }

    /**
     * Set the unique key on the specified Table. Unique key information is retrieved from the table definition table.
     *
     * @param table table to set the unique key on
     * @return name of the unique key ("" if the table does not have unique key information in the table definition
     * table)
     * @throws FatalDBUtilLibException if an error occurs when setting the unique key
     */
    @Override
    public String setUniqueKey(Table table) throws FatalDBUtilLibException {
        String uniqueKeys = table.getUniqueKeys();
        // No unique keys to set on this table
        if (uniqueKeys.length() == 0)
            return "";

        String tableName = table.getName();
        String ukName = generateConstraintName("UK_" + tableName.substring(tableName.indexOf(".") + 1));

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE " + tableName);
        sql.append(" ADD CONSTRAINT " + ukName);
        sql.append(" UNIQUE (" + uniqueKeys + ") ENABLE VALIDATE");
        if (this.indexTablespace != null)
            sql.append(" USING INDEX TABLESPACE " + this.indexTablespace);

        Statement statement = null;
        try {
            statement = getStatement();
            statement.execute(sql.toString().toUpperCase());
        } catch (SQLException ex) {
            throw new DBDefines.FatalDBUtilLibException("ERROR in DAODatabase.setPrimaryKey(). SQL = \n" + sql + "\n"
                    + ex.getMessage() + "\n");
        } finally {
            releaseStatement(statement);
        }
        return ukName;
    }

    /**
     * Set the foreign keys on a table based on the foreign key definitions defined in the table definition table.
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
        // Map from table to the column names in that columns in the Table parameter refer to
        HashMap<Table, String[]> fk = new HashMap<Table, String[]>();

        Column[] columns = table.getColumns();
        for (int i : table.getForeignKeyColumnIndexes()) {
            // ownedid columns show up in this foreign key list, but we aren't going to create foreign key constraints
            // on a database table that links to itself
            if (columns[i].isOwnedID())
                continue;

            // set referencedID to the column name that this foreign key refers to. Example: if table = event and
            // columns[i].name = prefor, then set referencedID = orid since prefor refers to the orid column in the
            // origin table (typically)
            String referencedID = columns[i].getForeignKey();

            // find the table that referencedID is the ownedId in. In the example above, referencedTable would be
            // origin.
            Table referencedTable = table.getSchema().getOwnedIDTable(referencedID);
            // Foreign key refers to a column in a table not in the schema
            if (referencedTable == null)
                continue;

            // Build up the columns that are referenced this referencedTable (there could be just one column that is
            // referenced in the table or multiple)
            String[] referencedColumns = fk.get(referencedTable);
            if (referencedColumns == null) {
                referencedColumns = new String[2];
                referencedColumns[0] = columns[i].getName();
                referencedColumns[1] = referencedID;
                fk.put(referencedTable, referencedColumns);
            } else {
                referencedColumns[0] += "," + columns[i].getName();
                referencedColumns[1] += "," + referencedID;
            }
        }

        // Set the foreign key constraints on all columns in the specified table that reference columns for the tables
        // in fk
        for (Table refTable : fk.keySet())
            setForeignKeys(table.getTableType(), fk.get(refTable)[0], refTable.getTableType(), fk.get(refTable)[1],
                    onDeleteCascade, onDeleteSetNull, deferrable, initiallyDeferred, enabled, validate, printSql);
    }

    /**
     * Set the foreign keys on a table based on the foreign key definitions defined in the table definition table.
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
        if (onDeleteCascade && onDeleteSetNull)
            throw new DBDefines.FatalDBUtilLibException("ERROR in DBUtilLib.DAODatabase.setForeignKeys().  "
                    + "onDeleteCascade and onDeleteSetNull cannot both be true.");
        if (this.schema.getTableOfType(tableType) == null)
            throw new DBDefines.FatalDBUtilLibException("ERROR in DBUtilLib.DAODatabase.setForeignKeys().  "
                    + "Schema does not contain a table of type " + tableType + ".");
        if (this.schema.getTableOfType(referencedTableType) == null)
            throw new DBDefines.FatalDBUtilLibException("ERROR in DBUtilLib.DAODatabase.setForeignKeys().  "
                    + "Schema does not contain a table of type " + referencedTableType + ".");

        String tableName = this.schema.getTableOfType(tableType).getName();
        String referencedTableName = this.schema.getTableOfType(referencedTableType).getName();
        String fkName = generateConstraintName(tableName.substring(tableName.indexOf(".") + 1)
                + DBDefines.removeExtraSpaces(columns.replaceAll(",", "")));

        // Create the SQL to create the foreign keys
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE " + tableName.toUpperCase());
        sql.append(" ADD CONSTRAINT " + fkName.toUpperCase());
        sql.append(" FOREIGN KEY (" + columns.toUpperCase() + ")");
        sql.append("REFERENCES " + referencedTableName.toUpperCase());
        sql.append(" (" + referencedColumns.toUpperCase() + ")");
        if (onDeleteCascade)
            sql.append(" ON DELETE CASCADE");
        if (onDeleteSetNull)
            sql.append(" ON DELETE SET NULL");

        if (deferrable) {
            sql.append(" DEFERRABLE");
            if (initiallyDeferred)
                sql.append(" INITIALLY DEFERRED");
            else
                sql.append(" INITIALLY IMMEDIATE");
        }
        if (enabled)
            sql.append(" ENABLE");
        else
            sql.append(" DISABLE");
        if (validate)
            sql.append(" VALIDATE");
        else
            sql.append(" NOVALIDATE");

        if (printSql)
            System.out.println(DBDefines.EOLN + sql.toString().toUpperCase());

        Statement statement = null;
        try {
            statement = getStatement();
            statement.execute(sql.toString().toUpperCase());
        } catch (SQLException ex) {
            throw new DBDefines.FatalDBUtilLibException("ERROR in DAODatabase.setForeignKeys(" + tableName + " ("
                    + columns + ") to " + referencedTableName + " (" + referencedColumns + ") " + ")\n" + "SQL = "
                    + sql + "\n" + ex.getMessage() + "\n");
        } finally {
            releaseStatement(statement);
        }
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
        boolean successful = true;
        Statement statement = null;
        try {
            statement = getStatement();
            StringBuilder sql = new StringBuilder("SELECT constraint_name FROM user_constraints WHERE table_name = '"
                    + table.getName().toUpperCase() + "'");
            Iterator<RowWithoutATable> constraintNamesIterator = iterator(sql.toString());
            String enable = constraintsOn ? " ENABLE " : " DISABLE ";
            while (constraintNamesIterator.hasNext()) {
                RowWithoutATable constraintNameRow = constraintNamesIterator.next();
                String constraintName = constraintNameRow.getFromDataMap("constraint_name").toString().toUpperCase();
                statement.addBatch("ALTER TABLE " + table.getName() + enable + "CONSTRAINT " + constraintName);
            }
            statement.executeBatch();
        } catch (SQLException e) {
            DBDefines.ERROR_LOG.add("Error in DAODatabase.setConstraints(" + table.getName() + ", " + constraintsOn
                    + ".\nError message: " + e.getMessage());
            successful = false;
            e.printStackTrace();
        } finally {
            releaseStatement(statement);
        }

        return successful;
    }

    /**
     * Return a RowGraph that contains all the rows from all the tables in the schema.
     *
     * @return a RowGraph that contains all the rows from all the tables in the schema; an empty RowGraph will be
     * returned. <b><i>Caution!</i></b> This reads everything in the schema into memory. If your schema contains tables
     * with a lot of data, you will most likely get an OutOfMemoryError.
     * @throws FatalDBUtilLibException if an error occurs when retrieving all data from all the tables in the schema
     */
    @Override
    public RowGraph getAllData() throws FatalDBUtilLibException {
        RowGraph rg = new RowGraph();
        try {
            for (Table table : this.schema.getTables())
                rg.addAll(executeSelectStatement(table, "WHERE 1=1"));
        } catch (DBDefines.FatalDBUtilLibException e) {
            String msg = "Error in DAODatabase.getAllData.\nException message: " + e.getMessage();
            DBDefines.ERROR_LOG.add(msg);
            throw new DBDefines.FatalDBUtilLibException(msg);
        }
        return rg;
    }

    /**
     * Creates a SQL select statement against the specified table type restricted by the specified where clause. This
     * method just calls {@link #executeSelectStatement(Table, String) executeSelectStatement(Table, whereClause)} with
     * the specified whereClause and with a Table object extracted from the schema using the specified table type.
     *
     * @param tableType   the type of the Table object against which the select statement should be executed.
     * @param whereClause the where clause of the sql statement
     * @return a list of Row objects that result from execution of the select statement.
     * @throws FatalDBUtilLibException if an error occurs when executing the select statement or creating the data
     *                                 objects to be returned
     */
    @Override
    public LinkedList<Row> executeSelectStatement(String tableType, String whereClause) throws FatalDBUtilLibException {
        return executeSelectStatement(this.schema.getTableOfType(tableType), whereClause);
    }

    /**
     * Executes a SQL SELECT statement of the form <br>
     * "SELECT * FROM " + table.name + " " + whereClause <br>
     * and returns Row objects created from the returned results. If table has hints associated with it, they will be
     * included in the select statement
     *
     * @param table       the Table against which the select statement should be executed
     * @param whereClause the where clause of the sql statement
     * @return Row objects created from the returned results
     * @throws FatalDBUtilLibException if an error occurs when executing the select statement or creating the data
     *                                 objects to be returned
     */
    @Override
    public LinkedList<Row> executeSelectStatement(Table table, String whereClause) throws FatalDBUtilLibException {
        StringBuilder sql = new StringBuilder("SELECT ");

        // Handle hints (if there are any)
        if (this.schema.getTable(table.getName()) != null) {
            String hint = this.schema.getHint(table.getTableType());
            if (hint != null)
                sql.append(hint + " ");
        }

        sql.append("* FROM ").append(table.getName()).append(" ");

        if (whereClause.trim().length() > 0 && !whereClause.trim().toLowerCase().startsWith("where "))
            sql.append("WHERE ");
        sql.append(whereClause);

        LinkedList<Row> rows = new LinkedList<Row>();
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = getStatement();
            resultSet = statement.executeQuery(sql.toString());
            while (resultSet.next()) {
                Row row = newRow(resultSet, table);
                if (row != null)
                    rows.add(row);
            }
        } catch (Exception e) {
            String msg = "DAODatabase executeSelectStatement(" + table.getName() + "," + whereClause
                    + ")\n with statement: " + statement + "\n" + "connection key: " + this.connectionKey + "\n"
                    + "Current time: " + new Date() + "\n" + "Exception message: " + e.getMessage();
            e.printStackTrace();
            throw new DBDefines.FatalDBUtilLibException(msg);
        } finally {
            closeResultSet(resultSet);
            releaseStatement(statement);
        }
        return rows;
    }

    /**
     * Executes a SQL SELECT statement that queries a table for a value from only one column. The SQL SELECT statement
     * is created by assembling independently specified components of the SELECT statement. The resulting query looks
     * like: <BR>
     * SELECT column FROM tableName WHERE whereClause ORDER BY orderClause
     *
     * @param column      column to select from tableName
     * @param tableName   the name of the table to execute the statement against
     * @param whereClause the where clause part of the statement (with or without the word 'where' at the beginning)
     * @param orderClause the order by clause
     * @return a LinkedList of Objects that are the result of the query. Each item in the returned list represents one
     * returned row from the database with the value for the one column specified
     * @throws FatalDBUtilLibException if an error occurs while executing the select statement or creating the data
     *                                 objects to be returned
     */
    @Override
    public LinkedList<Object> executeSelectStatement(String column, String tableName, String whereClause,
                                                     String orderClause) throws FatalDBUtilLibException {
        // Create the SELECT statement.
        StringBuilder sql = new StringBuilder("SELECT ");

        // Handle hints (if there are any)
        if (this.schema.getTable(tableName) != null) {
            String hint = this.schema.getHint(this.schema.getTable(tableName).getTableType());
            if (hint != null)
                sql.append(hint + " ");
        }

        sql.append(column);
        sql.append(" FROM ");
        sql.append(tableName);
        // Set up the whereClause.
        String where = whereClause.trim();
        if (where.length() > 0) {
            if (where.toLowerCase().indexOf("where ") != 0)
                where = "WHERE " + where;
            sql.append(" " + where);
        }

        // Set up the order by clause.
        if (orderClause.length() > 0)
            sql.append(" " + orderClause.trim());

        LinkedList<Object> results = new LinkedList<Object>();
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = getStatement();
            resultSet = statement.executeQuery(sql.toString());

            // Make 'row' objects out of the rows in the returned ResultSet and add those to the rows LinkedList.
            while (resultSet.next())
                results.add(resultSet.getObject(1));
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("DAODatabase Error in executeSelectStatement(" + column + ", "
                    + tableName + ", " + whereClause + ", " + orderClause + ")\n");
            msg.append("while trying to execute statement:\n" + sql.toString() + "\n");
            msg.append("Exception message: " + e.getMessage() + '\n');
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        } finally {
            closeResultSet(resultSet);
            releaseStatement(statement);
        }
        return results;
    }

    /**
     * Executes a SQL SELECT statement against the specified tableName. The SQL SELECT statement is created by
     * assembling independently specified components of the SELECT statement. The resulting query looks like: <BR>
     * SELECT [columns turned into comma delimited list] FROM tableName WHERE whereClause ORDER BY orderClause
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
        // Create the SELECT statement.
        StringBuilder sql = new StringBuilder("SELECT ");

        // Handle hints (if there are any)
        if (this.schema.getTable(tableName) != null) {
            String hint = this.schema.getHint(this.schema.getTable(tableName).getTableType());
            if (hint != null)
                sql.append(hint + " ");
        }

        // Add all of the column information to the SELECT statement.
        for (String column : columns)
            sql.append(column + ", ");
        // Remove the last comma.
        sql.setLength(sql.length() - 2);

        // Add the FROM table information.
        sql.append(" FROM ");
        sql.append(tableName);

        // Set up the whereClause.
        String where = whereClause.trim();
        if (where.length() > 0) {
            if (where.toLowerCase().indexOf("where ") != 0)
                where = "WHERE " + where;
            sql.append(" " + where);
        }

        // Set up the order by clause.
        if (orderClause.length() > 0)
            sql.append(" " + orderClause.trim());

        LinkedList<LinkedList<Object>> results = new LinkedList<LinkedList<Object>>();
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = getStatement();
            resultSet = statement.executeQuery(sql.toString());

            // Make 'row' objects out of the rows in the returned ResultSet and add those to the rows LinkedList.
            while (resultSet.next()) {
                LinkedList<Object> row = new LinkedList<Object>();
                for (int i = 0; i < columns.size(); i++)
                    row.add(resultSet.getObject(i + 1));
                results.add(row);
            }
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("DAODatabase Error in executeSelectStatement(" + columns.toString()
                    + ", " + tableName + ", " + whereClause + ", " + orderClause + ")\n");
            msg.append("while trying to execute statement:\n" + sql.toString() + "\n");
            msg.append("Exception message: " + e.getMessage() + '\n');
            msg.append("Current time: " + new Date() + '\n');
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        } finally {
            closeResultSet(resultSet);
            releaseStatement(statement);
        }
        return results;
    }

    /**
     * Return a LinkedList of Rows that are the result of executing the PreparedStatement obtained from the Relationship
     * object. Returns an empty LinkedList if no rows were returned by the query.
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
        LinkedList<Row> rows = new LinkedList<Row>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = relationship.getPreparedStatement(this.connection);
            // relationship's sourceVariables populate the prepared statement.
            Object[] sourceVariables = relationship.getSourceVariables();
            for (int i = 0; i < sourceVariables.length; i++)
                preparedStatement.setObject(i + 1, sourceVariables[i]);

            // Execute relationship's PreparedStatement.
            resultSet = preparedStatement.executeQuery();

            // Make Row objects out of the rows in the returned ResultSet and
            // add those to the rows LinkedList.
            while (resultSet.next())
                rows.add(newRow(resultSet, relationship.getTargetTable()));

        } catch (Exception e) {
            String msg = "Error in DB.executeSelectStatment(Relationship).\n" + relationship.getSelectStatement()
                    + "\nCurrent time is " + new Date() + '\n' + "Exception message: " + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(msg);
        } finally {
            closeResultSet(resultSet);
            relationship.releasePreparedStatement(this.connection, preparedStatement);
        }
        return rows;
    }

    /**
     * Retrieve column metadata for each column in the specified table type from the table definition table.
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
     * @throws FatalDBUtilLibException if a SQL error occurs when executing the TableDefinition's select statement or
     *                                 when creating the data objects to be returned
     */
    @Override
    public ArrayList<HashMap<String, String>> executeSelectStatement(TableDefinition tableDef, String tableType)
            throws FatalDBUtilLibException {
        ArrayList<HashMap<String, String>> colDefs = new ArrayList<HashMap<String, String>>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = tableDef.getPreparedStatement(this.connection);
            // This sets the table type whose information is to be retrieved from the table definition table
            preparedStatement.setObject(1, tableType);
            resultSet = preparedStatement.executeQuery();

            // Create the HashMap from column name to column value for each row returned (each row contains column
            // metadata for columns in the the specified tableType)
            while (resultSet.next()) {
                HashMap<String, String> row = new HashMap<String, String>();
                String[] sourceVars = tableDef.getColumnNames();
                for (int i = 0; i < sourceVars.length; i++)
                    row.put(sourceVars[i], resultSet.getString(sourceVars[i]));
                colDefs.add(row);
            }
        } catch (Exception e) {
            String msg = "ERROR in DAO.executeSelectStatment(" + "TableDef.name=" + tableDef.getName()
                    + ", table.type=" + tableType + ").\n" + "Prepared statement key = "
                    + tableDef.getPreparedStatementKey() + "\n Exception message: " + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(msg);
        } finally {
            closeResultSet(resultSet);
            tableDef.releasePreparedStatement(this.connection, preparedStatement);
        }
        return colDefs;
    }

    /**
     * Execute a SELECT statement in statement and return the results of that SELECT statement in a LinkedList of
     * RowWithoutATable Objects. Since this function does not take a table as an argument, then it cannot create the
     * usual Row objects, and must then make RowWithoutATable Objects. See the {@link DAO.RowWithoutATable
     * RowWithoutATable} class for more information on RowWithoutATable Objects. <BR>
     * This function is one that should be used only in the rarest of cases since it does not return actual Row objects,
     * and most of the functionality within DBUtilLib needs actual Row objects. The RowWithoutATable objects cannot be
     * used in any way like the DBUtilLib Row objects.
     *
     * @param selectStatement SELECT statement to execute
     * @return LinkedList of RowWithoutATable Objects that are the result of executing statement; empty list if no rows
     * returned
     * @throws FatalDBUtilLibException if a SQL error occurs when executing the select statement or creating the data
     *                                 objects to be returned
     */
    @Override
    public LinkedList<RowWithoutATable> executeSelectStatement(String selectStatement) throws FatalDBUtilLibException {
        LinkedList<RowWithoutATable> rows = new LinkedList<RowWithoutATable>();
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = getStatement();
            resultSet = statement.executeQuery(selectStatement);

            // Get the ResultSetMetaData since that's where we are going to get column names from since we don't have a
            // Table object to get them from.
            ResultSetMetaData rsmd = resultSet.getMetaData();

            int colNbr = rsmd.getColumnCount();
            while (resultSet.next()) {
                RowWithoutATable row = new RowWithoutATable();
                for (int i = 0; i < colNbr; i++) {
                    String colName = rsmd.getColumnName(i + 1);
                    row.addToDataMap(colName, resultSet.getObject(colName));
                }
                rows.add(row);
            }
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("ERROR in DAODatabase.executeSelectStatement(String statement).\n");
            msg.append("statement = " + selectStatement + "\nException message: " + e.getMessage());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        } finally {
            closeResultSet(resultSet);
            releaseStatement(statement);
        }
        return rows;
    }

    /**
     * Executes a SQL SELECT statement and returns the results in an ArrayList<Object[]> (each returned row is an
     * Object[] in the returned ArrayList, and each element of the Object[] is a value in that row). The types parameter
     * uses Class[] to specify the type of each expected return value; thus, the ith item in each Object[] returned will
     * be of type types[i]. For data of type int, long and double, oracle/jdbc returns data of type BigDecimal. For
     * every entry in the types array that is of type Double.class, Long.class or Integer.class, this method will
     * attempt to cast the data item retrieved from the database into a BigDecimal and then into the requested data
     * type. For all other specified types (including null), this method will simply return the object returned from
     * oracle, without doing any casting at all.
     *
     * @param selectStatement String the sql SELECT statement to be executed.
     * @param types           Class[] of the expected type of each returned column. There should be exactly one entry in types for
     *                        each column specified in the sql statement.
     * @return The data retrieved from the database; the ArrayList's size will be the number of rows returned from
     * executing the query and each Object[] will have a length = to the number of columns specified (which should be
     * the same length as types)
     * @throws FatalDBUtilLibException if an error occurs while executing the select statement or creating the data
     *                                 objects to be returned
     */
    @Override
    public ArrayList<Object[]> executeSelect(String selectStatement, Class<?>[] types) throws FatalDBUtilLibException {
        ArrayList<Object[]> results = new ArrayList<Object[]>();
        ResultSet resultSet = null;
        Statement statement = null;
        int i = -2;
        try {
            statement = getStatement();
            resultSet = statement.executeQuery(selectStatement);

            // Make 'row' objects out of the rows in the returned ResultSet and add those to the rows LinkedList.
            while (resultSet.next()) {
                Object[] row = new Object[types.length];
                for (i = 0; i < types.length; i++) {
                    if (types[i] == Double.class)
                        row[i] = Double.valueOf(((BigDecimal) resultSet.getObject(i + 1)).doubleValue());
                    else if (types[i] == Long.class)
                        row[i] = Long.valueOf(((BigDecimal) resultSet.getObject(i + 1)).longValue());
                    else if (types[i] == Integer.class)
                        row[i] = Integer.valueOf(((BigDecimal) resultSet.getObject(i + 1)).intValue());
                    else
                        row[i] = resultSet.getObject(i + 1);
                }
                results.add(row);
            }
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("ERROR in DBUtilLib.DAODatabase.executeSelect() ");
            msg.append("while trying to execute statement:\n" + selectStatement + "\n");
            msg.append("Exception message: " + e.getMessage() + '\n');
            msg.append("Error occurred while trying to parse item number " + (i + 1) + "\n");
            msg.append("Current time: " + new Date() + '\n');
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        } finally {
            closeResultSet(resultSet);
            releaseStatement(statement);
        }
        return results;
    }

    /**
     * Get the next value from a sequence.
     *
     * @param sequenceName the name of the sequence
     * @return the next value in the sequence
     * @throws FatalDBUtilLibException if an error occurs when retrieving the next value from a sequence
     */
    @Override
    public long executeSelectFromSequence(String sequenceName) throws FatalDBUtilLibException {
        String sql = "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
        long nextID = -1;
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = getStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next())
                nextID = resultSet.getLong(1);
        } catch (SQLException e) {
            String msg = "ERROR in DAODatabase.executeSelectFromSequence(). SQL " + "statement:\n" + sql
                    + "\nException message: " + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(msg);
        } finally {
            closeResultSet(resultSet);
            releaseStatement(statement);
        }
        return nextID;
    }

    /**
     * Retrieve all the ownedID values from an idOwner table that satisfy some condition specified with a where clause.
     *
     * @param table       the idowner table against which the sql statement should be executed.
     * @param whereClause the where clause
     * @return the set of ownedIds that results.
     * @throws FatalDBUtilLibException if a SQL error occurs when executing the select statement or creating the data to
     *                                 be returned
     */
    @Override
    public TreeSet<Long> selectOwnedIds(Table table, String whereClause) throws FatalDBUtilLibException {
        TreeSet<Long> results = new TreeSet<Long>();
        String column = table.getOwnedID();
        // This table is not an idowner table
        if (column == null)
            return results;

        // Create the SELECT statement.
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(column);
        sql.append(" FROM ");
        sql.append(table.getName());

        // Set up the whereClause.
        String where = whereClause.trim();
        if (where.length() > 0) {
            if (where.toLowerCase().indexOf("where ") != 0)
                where = "WHERE " + where;
            sql.append(" " + where);
        }

        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = getStatement();
            resultSet = statement.executeQuery(sql.toString());

            // Make 'row' objects out of the rows in the returned ResultSet and add those to the rows LinkedList.
            while (resultSet.next())
                results.add(new Long(resultSet.getLong(1)));
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("DAODatabase Error in selectOwnedIds(" + table.getName() + ", "
                    + whereClause + ")\n");
            msg.append("while trying to execute statement:\n" + sql.toString() + "\n");
            msg.append("Exception message: " + e.getMessage() + '\n');
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        } finally {
            closeResultSet(resultSet);
            releaseStatement(statement);
        }

        return results;
    }

    /**
     * Get the highest id for the column named idName in the table named tableName.
     *
     * @param idName    the name of the id whose max value is being requested
     * @param tableName the name of the table
     * @return the highest id for the column named idName in the table named tableName
     * @throws FatalDBUtilLibException if a database error occurs when retrieving the max id
     */
    @Override
    public long getMaxID(String idName, String tableName) throws FatalDBUtilLibException {
        long maxId = 0;
        if (!tableExists(tableName) || tableIsEmpty(tableName))
            return maxId;

        String sql = "SELECT MAX(" + idName + ") FROM " + tableName;
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = getStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next())
                maxId = resultSet.getLong(1);
        } catch (SQLException e) {
            String msg = "ERROR in DAODatabase.getMaxID(" + idName + ", " + tableName + "). SQL statement:\n" + sql
                    + "\nException message: " + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(msg);
        } finally {
            closeResultSet(resultSet);
            releaseStatement(statement);
        }
        return maxId;
    }

    /**
     * Execute the update statement (insert, delete, etc - statements that do not query the database) in statement.
     *
     * @param updateStatement update statement to execute
     * @return result code indicating the status of executing the update statement (the row count for INSERT, UPDATE, or
     * DELETE statements or 0 for SQL statements that return nothing)
     * @throws FatalDBUtilLibException if an error occurs when executing the update statement
     */
    @Override
    public int executeUpdateStatement(String updateStatement) throws FatalDBUtilLibException {
        int j = -1;
        Statement statement = null;
        try {
            statement = getStatement();
            j = statement.executeUpdate(updateStatement);
        } catch (Exception e) {
            String msg = "DAODatabase.Error in executeUpdateStatement(" + updateStatement + ")\nException message: "
                    + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(msg);
        } finally {
            releaseStatement(statement);
        }
        return j;
    }

    /**
     * Insert all of the information in the specified row into the database. This method just calls
     * {@link #insertRow(Row, boolean) insertRow(row, true)}
     *
     * @param row the Row whose information is to be inserted into the database.
     * @return whether the row insertion was successful or not
     * @throws FatalDBUtilLibException if an error occurs when inserting this row's data into the database
     */
    @Override
    public boolean insertRow(Row row) throws FatalDBUtilLibException {
        return insertRow(row, false);
    }

    /**
     * Insert all of the information in the specified row into the database.
     *
     * @param row               the Row whose information is to be inserted into the database.
     * @param ignoreUniqueError whether to ignore errors generated from inserting a row that results in a unique key
     *                          constraint violation (true) or not (false)
     * @return whether the row insertion was successful or not
     * @throws FatalDBUtilLibException if an error occurs when inserting this row's data into the database
     */
    @Override
    public boolean insertRow(Row row, boolean ignoreUniqueError) throws FatalDBUtilLibException {
        PreparedStatement preparedStatement = null;
        Table table = row.getTable();
        boolean insertStatus = false;
        try {
            preparedStatement = table.getInsertPreparedStatement(this.connection);

            // Put row's values into the insert prepared statement and insert the data!
            table.populateInsertPreparedStatement(row, preparedStatement);
            preparedStatement.executeUpdate();
            insertStatus = true;
        } catch (Exception e) {
            // Tried to insert a Row into the database and got a unique key violation. Execute a select on the
            // targetSchema.dao for a row with the same primary key as target row. If one is returned, and its RowID is
            // equal to the RowID of targetRow, then there is an exact duplicate of targetRow (based on all fields
            // except LDDATE) already in the database. It is not an error to ignore this insertRow error.
            // IMPORTANT NOTE! This check should only be made for tables that have a primary key where clause.
            // Otherwise, entire tables - with no pk indexes - get read in only to turn right around and fail the
            // rows.size() == 1 check.
            if (e.getMessage().contains("unique constraint") && e.getMessage().contains("violated")
                    && row.getPrimaryKeyWhereClause().length() > 0 && ignoreUniqueError) {
                LinkedList<Row> rows = row.getSchema().getDAO().executeSelectStatement(row.getTable(),
                        row.getPrimaryKeyWhereClause());
                if (rows.size() == 1 && rows.getFirst().getRowId().equals(row.getRowId()))
                    insertStatus = true;
            } else {
                // Normal sort of exception handling ...
                StringBuilder msg = new StringBuilder("ERROR in DAODatabase.insertRow(" + row + ")\n");
                msg.append("Connection key: " + this.connectionKey + "\n");
                msg.append("insertPreparedStatement: " + row.getTable().getInsertPreparedStatementKey() + '\n');
                msg.append("row.values = " + row.valuesToString(false, ','));
                msg.append("\nException message: " + e.getMessage());
                throw new DBDefines.FatalDBUtilLibException(msg.toString());
            }
        } finally {
            table.releaseInsertPreparedStatement(this.connection, preparedStatement);
        }

        return insertStatus;
    }

    /**
     * Insert all of the information in the specified row into the database using the specified connection. This method
     * is needed when the user needs to have control over the connection for commit and rollback independence amongst
     * threads.
     *
     * @param row  the Row whose information is to be inserted into the database.
     * @param conn the connection to use when inserting the row
     * @return whether the row insertion was successful or not
     * @throws FatalDBUtilLibException if an error occurs when inserting this row's data into the database
     */
    public boolean insertRow(Row row, Connection conn) throws FatalDBUtilLibException {
        boolean returnStatus = false;
        PreparedStatement preparedStatement = null;
        Table table = row.getTable();
        try {
            preparedStatement = table.getInsertPreparedStatement(conn);

            // Put row's values into the insert prepared statement and insert the data!
            table.populateInsertPreparedStatement(row, preparedStatement);
            preparedStatement.executeUpdate();

            returnStatus = true;
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("ERROR in DAODatabase.insertRow(" + row + ", connection)\n");
            msg.append("Connection key: " + this.connectionKey + "\n");
            msg.append("insertPreparedStatement: " + row.getTable().getInsertPreparedStatementKey() + '\n');
            msg.append("row.values = " + row.valuesToString(false, ','));
            msg.append("\nException message: " + e.getMessage());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        } finally {
            table.releaseInsertPreparedStatement(conn, preparedStatement);
        }
        return returnStatus;
    }

    /**
     * Perform a batch of insert statements for the specified items.
     *
     * @param table table to insert into
     * @param items items to be inserted (each Object[] in this items list is expected to be in the same order as the
     *              columns for the specified table)
     * @return update counts for each insert command generated for the specified items. The elements of the array are
     * ordered according to the order in which commands were added to the batch.
     * @throws FatalDBUtilLibException
     */
    public int[] batchInsert(Table table, ArrayList<Object[]> items) throws FatalDBUtilLibException {
        int[] insertStatus;

        try {
            // Create SQL statement
            Column[] columns = table.getColumns();
            String[] columnNames = new String[columns.length];

            StringBuilder sql = new StringBuilder("INSERT INTO " + table.getName() + "(");
            StringBuilder temp = new StringBuilder(") VALUES(");
            for (int i = 0; i < columnNames.length; i++) {
                columnNames[i] = columns[i].getName();
                sql.append(columnNames[i] + ", ");
                temp.append("?, ");
            }

            // Create prepared statement
            PreparedStatement prepStatement = this.connection.prepareStatement(sql.substring(0, sql.length() - 2)
                    + temp.substring(0, temp.length() - 2) + ")");
            Object[] firstItem = items.get(0);
            boolean[] dateIndex = new boolean[firstItem.length];
            Arrays.fill(dateIndex, false);
            for (int i = 0; i < firstItem.length; i++)
                if (firstItem[i].getClass().getName().toLowerCase().contains("date"))
                    dateIndex[i] = true;

            // Populate prepared statement
            for (Object[] item : items) {
                prepStatement.clearParameters();
                for (int i = 0; i < item.length; i++) {
                    if (!dateIndex[i])
                        prepStatement.setObject(i + 1, item[i]);
                    else
                        prepStatement.setDate(i + 1, new java.sql.Date(((Date) item[i]).getTime()));
                }
                prepStatement.addBatch();
            }

            insertStatus = prepStatement.executeBatch();
            prepStatement.close();
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("ERROR in DAODatabase.batchInsert\n");
            msg.append("\nException message: " + e.getMessage());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        }
        return insertStatus;
    }

    /**
     * Update information in the database row that corresponds to the Row object using the specified connection. This
     * method is needed when the user needs to have control over the connection for commit and rollback independence
     * amongst threads. Thus, the database row that is equal to row (has the same unique key information) must be
     * located. Then, all of it's data will be updated with the values from the row.
     *
     * @param row  the Row whose information is being updated in the database
     * @param conn the connection to use when updating the row
     * @return whether the update was successful or not
     * @throws FatalDBUtilLibException if an error occurs when updating the row in the database
     */
    public boolean updateRow(Row row, Connection conn) throws FatalDBUtilLibException {
        Table table = row.getTable();
        PreparedStatement preparedStatement = null;
        boolean returnStatus = false;
        try {
            preparedStatement = table.getUpdatePreparedStatement(conn);
            // Put row's values into the update prepared statement and update the data!
            table.populateUpdatePreparedStatement(row, preparedStatement);
            preparedStatement.executeUpdate();
            returnStatus = true;
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("ERROR in DAODatabase.updateRow(" + row + ", connection)\n");
            msg.append("Connection key: " + this.connectionKey);
            msg.append("Exception message: " + e.getMessage());
            msg.append("Current time: " + new Date());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        } finally {
            table.releaseUpdatePreparedStatement(conn, preparedStatement);
        }
        return returnStatus;
    }

    /**
     * Update information in the database row that corresponds to the Row object. Thus, the database row that is equal
     * to row (has the same unique key information) must be located. Then, all of it's data will be updated with the
     * values from the row.
     *
     * @param row the Row whose information is being updated in the database
     * @return whether the update was successful or not
     * @throws FatalDBUtilLibException if an error occurs when updating the row in the database
     */
    @Override
    public boolean updateRow(Row row) throws FatalDBUtilLibException {
        Table table = row.getTable();
        PreparedStatement preparedStatement = null;
        boolean returnStatus = false;
        try {
            preparedStatement = table.getUpdatePreparedStatement(this.connection);
            // Put row's values into the update prepared statement and update the data!
            table.populateUpdatePreparedStatement(row, preparedStatement);
            preparedStatement.executeUpdate();
            returnStatus = true;
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("ERROR in DAODatabase.updateRow(" + row + ")\n");
            msg.append("Connection key: " + this.connectionKey);
            msg.append("Exception message: " + e.getMessage());
            msg.append("Current time: " + new Date());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        } finally {
            table.releaseUpdatePreparedStatement(this.connection, preparedStatement);
        }
        return returnStatus;
    }

    /**
     * Delete the database row that corresponds to the row object.
     *
     * @param row the Row that is to be deleted from the database
     * @return true if a row is actually deleted; false otherwise
     * @throws FatalDBUtilLibException if an error occurs when deleting the row from the database
     */
    @Override
    public boolean deleteRow(Row row) throws FatalDBUtilLibException {
        Table table = row.getTable();
        PreparedStatement preparedStatement = null;
        boolean returnStatus = false;
        try {
            preparedStatement = table.getDeletePreparedStatement(this.connection);
            // Put row's values into the delete prepared statement and delete the data!
            table.populateDeletePreparedStatement(row, preparedStatement);
            preparedStatement.executeUpdate();
            returnStatus = true;
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("ERROR in DAODatabase.deleteRow(" + row + ")\n");
            msg.append("Connection key: " + this.connectionKey);
            msg.append("deletePreparedStatement = " + row.getTable().getDeletePreparedStatementKey());
            msg.append("Exception message: " + e.getMessage());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        } finally {
            table.releaseDeletePreparedStatement(this.connection, preparedStatement);
        }
        return returnStatus;
    }

    /**
     * Delete the database row that corresponds to the row object using the specified connection. This method is needed
     * when the user needs to have control over the connection for commit and rollback independence amongst threads.
     *
     * @param row  the Row that is to be deleted from the database
     * @param conn the connection to use when deleting the row
     * @return true if a row is actually deleted; false otherwise
     * @throws FatalDBUtilLibException if an error occurs when deleting the row from the database
     */
    public boolean deleteRow(Row row, Connection conn) throws FatalDBUtilLibException {
        Table table = row.getTable();
        PreparedStatement preparedStatement = null;
        boolean returnStatus = false;
        try {
            preparedStatement = table.getDeletePreparedStatement(conn);

            // Put row's values into the delete prepared statement and delete the data!
            table.populateDeletePreparedStatement(row, preparedStatement);
            preparedStatement.executeUpdate();
            returnStatus = true;
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder("ERROR in DAODatabase.deleteRow(" + row + ", connection)\n");
            msg.append("Connection key: " + this.connectionKey);
            msg.append("deletePreparedStatement = " + row.getTable().getDeletePreparedStatementKey());
            msg.append("Exception message: " + e.getMessage());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        } finally {
            table.releaseDeletePreparedStatement(conn, preparedStatement);
        }
        return returnStatus;
    }

    /**
     * Writes a Row of IDGaps Table information to the database using the PreparedStatement a IDGapsTable Object has for
     * just such a reason.
     *
     * @param idGapsTable IDGapsTable that needs to have a row written to the database
     * @param values      values used to replace the ?'s in the IDGapTable's PreparedStatement string
     * @throws FatalDBUtilLibException if an error occurs when writing the idgaps row
     */
    @Override
    public void writeIDGapsRow(IDGapsTable idGapsTable, Object[] values) throws FatalDBUtilLibException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = idGapsTable.getPreparedStatement(this.connection);
            // Set all of the ?s in the PreparedStatement to values in values Object array.
            for (int i = 0; i < values.length; i++)
                preparedStatement.setObject(i + 1, values[i]);

            // Write the row
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            StringBuilder msg = new StringBuilder();
            msg.append("Error in DAODatabase.writeIDGapsRow(" + idGapsTable + ", [ ");
            for (int i = 0; i < values.length; i++)
                msg.append(values[i] + " ");
            msg.append("])");
            msg.append("\nException message: " + e.getMessage());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        } finally {
            idGapsTable.releasePreparedStatement(this.connection, preparedStatement);
        }
    }

    /**
     * Forces a commit.
     */
    @Override
    public void commit() {
        try {
            this.connection.commit();
        } catch (SQLException e) {
            DBDefines.ERROR_LOG.add("DAODatabase.commit() SQLException: " + e.getMessage());
        }
    }

    /**
     * Forces a rollback.
     */
    @Override
    public void rollback() {
        try {
            this.connection.rollback();
        } catch (SQLException e) {
            DBDefines.ERROR_LOG.add("DAODatabase.rollback() SQLException: " + e.getMessage());
        }
    }

    /**
     * Create an Iterator over all the rows that result from executing a SELECT * SQL statement (limited by the
     * specified whereClause) against the specified database table. The statement that is executed is of the form: <BR>
     * "SELECT * FROM " + table.getName() + " " + whereClause. ("where" will be prepended to the where clause if it is
     * not present)<BR>
     * This method is preferable to using one of the executeSelectStatement methods for situations where the number of
     * Row objects that might be returned could be very large since it does not read all of the returned information
     * into memory, but instead maintains an open connection to the database and retrieves the next row of information
     * only when requested to do so. <BR>
     * This method instantiates an iterator which executes the above SELECT statement with the specified where clause
     * and holds an instance of a ResultSet (the open connection to the database). Row objects are only created when the
     * next() method is called.
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
        try {
            return new DAOIterator(table, whereClause);
        } catch (DBDefines.FatalDBUtilLibException e) {
            String msg = "Error in DAODatabase.iterator(" + table.getName() + ", " + whereClause
                    + ").\nException message: " + e.getMessage();
            DBDefines.ERROR_LOG.add(msg);
            return null;
        }
    }

    /**
     * Create an Iterator over all the rows that result from executing the specified selectStatement. This method
     * instantiates an iterator which executes the specified selectStatement and returns the results as
     * {@link DAO.RowWithoutATable RowWithoutATable} objects as well as maintaining an instance of a ResultSet (the open
     * connection to the database). RowWithoutATable objects are only created when the next() method is called.
     *
     * @param selectStatement selectStatement to be executed against the database
     * @return an Iterator over {@link DAO.RowWithoutATable DAO.RowWithoutATable} objects; null if an error occurs
     */
    @Override
    public Iterator<RowWithoutATable> iterator(String selectStatement) {
        try {
            return new DAORowWithoutATableIterator(selectStatement);
        } catch (DBDefines.FatalDBUtilLibException e) {
            String msg = "Error in DAODatabase.iterator(" + selectStatement + ").\nException message: "
                    + e.getMessage();
            DBDefines.ERROR_LOG.add(msg);
            return null;
        }
    }

    /**
     * Return a String representation of this DAODatabase object.
     *
     * @return String representation of this DAODatabase object
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Database Data Access Object: " + DBDefines.EOLN);
        s.append("Name:      " + this.name + DBDefines.EOLN);
        s.append("Type:      " + TYPE + DBDefines.EOLN);
        s.append("UserName:  " + this.username + DBDefines.EOLN);
        s.append("PassWord:  " + this.password.replaceAll(".", "*") + DBDefines.EOLN);
        s.append("Instance:  " + this.instance + DBDefines.EOLN);
        s.append("Driver:    " + this.driver + DBDefines.EOLN);
        s.append("AutoCommit:    " + this.autoCommit + DBDefines.EOLN);
        return s.toString();
    }

    /**
     * Specify whether or not to include column information in xml output files; this only affects xml dao's when
     * outputting xml files and is not implemented for DAODatabase.
     *
     * @param yesNo whether or not to output column information; this is only implemented for XML DAOs
     */
    @Override
    public void outputColumnInfo(boolean yesNo) {
        // do nothing. this method only affects xml output dao.
    }

    /**
     * Returns a Jaxb representation of this DAO.
     *
     * @return Jaxb representation of this DAO
     * @throws JAXBException if an error occurs when converting this DAODatabase object into a
     *                       {@link gov.sandia.gnem.dbutillib.jaxb.DAO jaxbDAO} object
     */
    @Override
    public gov.sandia.gnem.dbutillib.jaxb.DAO toJaxb() throws JAXBException {
        // Create a jaxb Dao object and add all of this DAODatabase object's information to it.
        gov.sandia.gnem.dbutillib.jaxb.DAO jaxbDao = new ObjectFactory().createDAO();
        jaxbDao.setType(TYPE);
        jaxbDao.setUsername(this.username);
        jaxbDao.setPassword(this.password);
        jaxbDao.setDriver(this.driver);
        jaxbDao.setInstance(this.instance);
        jaxbDao.setTabletablespace(this.tableTablespace);
        jaxbDao.setIndextablespace(this.indexTablespace);

        return jaxbDao;
    }

    /** ~*~*~*~*~*~*~*~ END DAO Abstract Method Implementation ~*~*~*~*~*~*~*~ */

    /** ~*~*~*~*~*~*~*~ Miscellaneous DAODatabase Methods ~*~*~*~*~*~*~*~ */
    /**
     * Execute the select statement contained in prepStmt.
     *
     * @param table    the table type of the Row objects to be returned (this should be the table that prepStmt is
     *                 querying)
     * @param prepStmt PreparedStatement containing select statement to execute against table
     * @return Row objects constructed from the results from executing the select statement
     * @throws FatalDBUtilLibException if an error occurs when executing the select statement in prepStmt or when
     *                                 creating the data objects to be returned
     */
    public LinkedList<Row> executeSelectStatement(Table table, PreparedStatement prepStmt)
            throws FatalDBUtilLibException {
        LinkedList<Row> rows = new LinkedList<Row>();
        ResultSet resultSet = null;
        try {
            resultSet = prepStmt.executeQuery();

            // Make Row objects out of the rows in the returned ResultSet and add those to the rows LinkedList.
            while (resultSet.next())
                rows.add(newRow(resultSet, table));
        } catch (SQLException e) {
            String error = "Error in DAODatabase.executeSelectStatement(" + table.getName()
                    + ", [PreparedStatement]).  " + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(error);
        } finally {
            closeResultSet(resultSet);
        }
        return rows;
    }

    /**
     * Constructor that takes a ResultSet that is current referring to a database row containing this Row's values and a
     * Table object that the returned row belongs to. The Row's values will be obtained from the information found at at
     * the current cursor position within the ResultSet and the "metadata" about the Row will be constructed from
     * information within the Table object.
     *
     * @param rs    ResultSet containing the data from which the Row will be constructed
     * @param table Table this Row is belongs to
     * @return a Row that belongs to the specified table constructed from the data at the current ResultSet position
     * @throws FatalDBUtilLibException if an error occurs when retrieving the row data or constructing the Row object
     */
    protected Row newRow(ResultSet rs, Table table) throws FatalDBUtilLibException {
        Object[] values = null;
        int i = -1;
        try {
            // This columns information indicates what's in each column of data where the resultset is currently
            // pointing
            Column[] columns = table.getColumns();
            // Initialize values to have as many entries as there are columns.
            values = new Object[columns.length];

            // Put the values from the ResultSet into the values array. The values are gotten out of the ResultSet based
            // on the Column names and the casting is done using column information.
            for (i = 0; i < values.length; i++) {
                if (columns[i].getJavaType() == DBDefines.LONG)
                    values[i] = new Long(rs.getLong(columns[i].getName()));
                else if (columns[i].getJavaType() == DBDefines.DOUBLE)
                    values[i] = new Double(rs.getDouble(columns[i].getName()));
                else if (columns[i].getJavaType() == DBDefines.FLOAT)
                    values[i] = new Float(rs.getFloat(columns[i].getName()));
                else if (columns[i].getJavaType() == DBDefines.INTEGER)
                    values[i] = new Integer(rs.getInt(columns[i].getName()));
                else if (columns[i].getJavaType() == DBDefines.STRING)
                    values[i] = rs.getString(columns[i].getName());
                else if (columns[i].getJavaType() == DBDefines.DATE || columns[i].getJavaType() == DBDefines.TIMESTAMP) {
                    java.sql.Timestamp sqlDate = rs.getTimestamp(columns[i].getName());
                    if (sqlDate == null)
                        values[i] = null;
                    else {
                        Date newDate = new Date(sqlDate.getTime());
                        values[i] = newDate;
                    }
                } else if (columns[i].getJavaType() == DBDefines.BOOLEAN)
                    values[i] = new Boolean(rs.getBoolean(columns[i].getName()));
                else if (columns[i].getJavaType() == DBDefines.BYTE)
                    values[i] = new Byte(rs.getByte(columns[i].getName()));
                else if (columns[i].getJavaType() == DBDefines.BLOB)
                    values[i] = rs.getBlob(columns[i].getName());
                else if (columns[i].getJavaType() == DBDefines.CLOB)
                    values[i] = rs.getClob(columns[i].getName());
                else if (columns[i].getJavaType() == DBDefines.UNKNOWN_TYPE)
                    values[i] = rs.getObject(columns[i].getName()); // shallow copy!

                else {
                    DBDefines.WARNING_LOG.add("DAODatabase has a Java Type (" + columns[i].getJavaType()
                            + " that it doesn't know what to do with");
                    values[i] = rs.getObject(columns[i].getName()); // shallow copy!
                }
            }
        } catch (SQLException e) {
            StringBuilder msg = new StringBuilder("Error in DAODatabase.newRow().\n");
            Column[] columns = table.getColumns();
            if (i >= 0 && i < columns.length)
                msg.append("Cannot create entry for column ").append(table.getName().toUpperCase()).append('.').append(
                        columns[i].getName().toUpperCase());

            msg.append("\nException message: ");
            msg.append(e.getMessage());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        }

        try {
            Row row = new Row(table, values, false);
            return row;
        } catch (DBDefines.FatalDBUtilLibException e) {
            String msg = "Error in DAODatabase.newRow\nException message: " + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(msg);
        }
    }

    /**
     * Populate {@link #objectTypes objectTypes}. When applications request the set of all table names accessible to the
     * user's account, only look for "tables" of the types in this variable. (Typically, this is TABLE, VIEW, ALIAS, and
     * SYNONYM.)
     */
    private void populateObjectTypes() {
        objectTypes = new LinkedList<String>();
        objectTypes.clear();
        objectTypes.add("TABLE");
        objectTypes.add("VIEW");
        objectTypes.add("ALIAS");
        objectTypes.add("SYNONYM");
    }

    /**
     * Populate {@link #excludeAccounts excludeAccounts}. When applications request the set of all table names
     * accessible to the user's account, do not include tables from these accounts in {@link #excludeAccounts
     * excludeAccounts}.
     */
    private void populateExcludeAccounts() {
        excludeAccounts = new LinkedList<String>();
        excludeAccounts.clear();
        excludeAccounts.add("'ANONYMOUS'");
        excludeAccounts.add("'CTXSYS'");
        excludeAccounts.add("'DBSNMP'");
        excludeAccounts.add("'DIP'");
        excludeAccounts.add("'DMSYS'");
        excludeAccounts.add("'EXFSYS'");
        excludeAccounts.add("'MDDATA'");
        excludeAccounts.add("'MDSYS'");
        excludeAccounts.add("'MGMT_VIEW'");
        excludeAccounts.add("'ODM'");
        excludeAccounts.add("'ODM_MTR'");
        excludeAccounts.add("'OLAPSYS'");
        excludeAccounts.add("'ORDPLUGINS'");
        excludeAccounts.add("'ORDSYS'");
        excludeAccounts.add("'OUTLN'");
        excludeAccounts.add("'PUBLIC'");
        excludeAccounts.add("'SI_INFORMTN_SCHEMA'");
        excludeAccounts.add("'SYS'");
        excludeAccounts.add("'SYSMAN'");
        excludeAccounts.add("'SYSTEM'");
        excludeAccounts.add("'TSMSYS'");
        excludeAccounts.add("'WKPROXY'");
        excludeAccounts.add("'WKSYS'");
        excludeAccounts.add("'WMSYS'");
        excludeAccounts.add("'XDB'");
    }

    /**
     * Generate a valid constraint name that is 30 characters or less and does not duplicate an existing constraint
     * name.
     *
     * @param constraintName suggested constraint name that will be checked for validity and modified as appropriate
     * @return valid constraint name that equals constraintName if allowable (constraintName is the appropriate length
     * and does not already exist in the database) or that is based on the suggested constraintName
     */
    private String generateConstraintName(String constraintName) {
        String cName = constraintName;
        // Constraint names cannot exceed 30 characters in length
        if (cName.length() > 30)
            cName = cName.substring(0, 30);

        ResultSet resultSet = null;
        Statement statement = null;
        try {
            statement = getStatement();
            // Make sure this constraint does not exist in the database
            resultSet = statement.executeQuery("SELECT constraint_name FROM user_constraints WHERE constraint_name = '"
                    + cName.toUpperCase() + "'");

            // constraint by that name already exists ...
            if (resultSet.next()) {
                // Find out how many constraints start with the same name as constraintName
                resultSet = statement.executeQuery("SELECT constraint_name FROM user_constraints"
                        + " WHERE constraint_name like '" + cName.toUpperCase() + "%'");
                HashSet<String> suffixes = new HashSet<String>();
                while (resultSet.next()) {
                    String existingConstraint = resultSet.getString(1);
                    suffixes.add(existingConstraint.substring(cName.length()));
                }
                int i = 1;
                while (suffixes.contains(String.valueOf(i)))
                    i++;

                cName += i;
                // If this is the case, then it's i characters past 30 ... this scenario would occur when the
                // constraintName was already 30 characters long and then adding on i made it too long.
                // We want to preserve the PK/UK/FK part of it and the index and as much of the table name as possible
                if (cName.length() > 30)
                    cName = cName.substring(0, 15) + "-" + cName.substring(cName.length() - 14);
            }
        } catch (SQLException e) {
            DBDefines.ERROR_LOG.add("DAODatabase ERROR creating constraint name based on " + cName + "\n"
                    + e.getMessage());
        } finally {
            closeResultSet(resultSet);
            releaseStatement(statement);
        }

        return cName.toUpperCase();
    }

    /**
     * Obtain a database statement from {@link #statements statements} if on is available or a new one if
     * {@link #statements statements} is empty.
     *
     * @return a database statement from {@link #statements statements} if on is available or a new one if
     * {@link #statements statements} is empty
     * @throws SQLException if an error occurs while creating a Statement object
     */
    private synchronized Statement getStatement() throws SQLException {
        if (this.statements.size() != 0)
            return this.statements.removeFirst();

        return this.connection.createStatement();
    }

    /**
     * Releases a Statement object for future use.
     *
     * @param statement statement object to be released for future use
     */
    private synchronized void releaseStatement(Statement statement) {
        this.statements.add(statement);
    }

    /**
     * Closes the specified ResultSet
     *
     * @param resultSet the ResultSet to be closed
     */
    private void closeResultSet(ResultSet resultSet) {
        try {
            if (resultSet != null)
                resultSet.close();
        } catch (Exception ex) {
            DBDefines.WARNING_LOG.add("DAODatabase Error closing ResultSet. " + ex.getMessage());
        }
    }

    /**
     * Called when the garbage collector grabs this object. Makes sure that the connection gets closed.
     */
    @Override
    public void finalize() {
        if (!this.connectionClosed)
            closeConnection();
    }

    /** ~*~*~*~*~*~*~*~ GETTERS/SETTERS ~*~*~*~*~*~*~*~ */
    /**
     * Returns the database user name.
     *
     * @return the database user name
     */
    public String getUserName() {
        return this.username;
    }

    /**
     * Returns the database password.
     *
     * @return the database password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Returns the database driver
     *
     * @return the database driver
     */
    public String getDriver() {
        return this.driver;
    }

    /**
     * Returns the database instance.
     *
     * @return the database instance
     */
    public String getInstance() {
        return this.instance;
    }

    /**
     * Returns the URI of this database account; this will be something like: database://username/password@instance
     *
     * @return URI of this database account; this will be something like: database://username/password@instance
     */
    public String getURI() {
        return this.uriBase;
    }

    /**
     * Return the connection to the database
     *
     * @return the connection to the database
     */
    protected Connection getConnection() {
        return this.connection;
    }

    /** ~*~*~*~*~*~*~*~ Inner Classes ~*~*~*~*~*~*~*~ */

    /**
     * This class implements the Iterator interface that iterates over rows returned from executing a select statement
     * against a specified table in the database with the results restricted based on a specified where clause. The
     * next() method returns {@link Row Row} objects constructed from the returned data.
     */
    private class DAOIterator implements Iterator<Row> {
        /**
         * Table whose rows will be iterated over and that the Row objects returned by the iterator belong to.
         */
        private Table table;

        /**
         * Iterator's own statement object for accessing the database. This way, the DAODatabase statement object will
         * not be tied up.
         */
        private Statement statement;

        /**
         * Iterator's ResultSet that will maintain an open connection to the database while the returned rows are being
         * iterated over.
         */
        private ResultSet resultSet = null;

        /**
         * Whether or not what the ResultSet is currently pointing to is a valid record
         */
        private boolean currentValid = false;

        /**
         * Keep track of if a FetchOutOfSequence error has been caught, ignored, and next() recalled. This is an attempt
         * to be able to continue execution if a fetch out of sequence error is encountered since the error has yet to
         * be reproduced and thus is still unresolved.
         */
        private boolean reissuedCallToNext = false;

        /**
         * Select statement that this iterator executes to retrieve rows to iterate over. This is only used in the
         * constructor and for error output.
         */
        private String selectStatement;

        /**
         * Constructor.
         *
         * @param table       table that the iterator will be returned rows from and that the returned Row objects will belong
         *                    to
         * @param whereClause restricts what data is read in
         * @throws FatalDBUtilLibException if an error occurs while establishing a connection to the database
         */
        public DAOIterator(Table table, String whereClause) throws FatalDBUtilLibException {
            this.table = table;

            String wClause = whereClause;
            // Handle null whereClauses and whereClauses that do not have the word "where" at the beginning
            if (wClause == null)
                wClause = "";
            if (wClause.trim().length() > 0 && !wClause.trim().toUpperCase().startsWith("WHERE "))
                wClause = "WHERE " + wClause;

            this.selectStatement = "SELECT * FROM " + table.getName() + " " + wClause;
            try {
                this.statement = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY);
                this.resultSet = this.statement.executeQuery(this.selectStatement);

                // Advance the result set to the first row.
                this.currentValid = this.resultSet.next();

                if (!this.currentValid) {
                    this.resultSet.close();
                    this.statement.close();
                }
            } catch (Exception e) {
                String msg = "DAODatabase ERROR in DAOIterator constructor.  Returning " + "null.\nSelect statment = "
                        + this.selectStatement + "\n" + "Exception message: " + e.getMessage();
                throw new DBDefines.FatalDBUtilLibException(msg);
            }
        }

        /**
         * Return whether or not there are more rows to iterate over
         *
         * @return whether or not there are more rows to iterate over
         */
        public boolean hasNext() {
            return this.currentValid;
        }

        /**
         * Return the row that is the next to be iterated over
         *
         * @return the row that is the next to be iterated over
         */
        public Row next() {
            try {
                if (!this.currentValid) {
                    this.resultSet.close();
                    this.statement.close();
                    return null;
                }
                Row row = null;
                try {
                    row = newRow(this.resultSet, this.table);
                } catch (DBDefines.FatalDBUtilLibException e) {
                    String msg = "Error in DAODatabase.DAOIterator.next().\n" + "Exception message: " + e.getMessage();
                    DBDefines.ERROR_LOG.add(msg);
                }
                this.currentValid = this.resultSet.next();
                if (!this.currentValid) {
                    this.resultSet.close();
                    this.statement.close();
                }
                return row;
            } catch (SQLException e) {
                if (e.getMessage().toLowerCase().contains("fetch out of sequence")) {
                    // I have been unable to produce test code that generates this message. It seems to happen
                    // sporadically for reasons I cannot determine. All of the fixes for this error that I have found
                    // online apply to situations that are not happening here (SQL select statements with a "for update"
                    // clauses, PL/SQL code with cursors, autoCommit settings in both of the previous situations, and so
                    // on). The best I could do was to set the ResultSet to use ResultSet.TYPE_FORWARD_ONLY and
                    // ResultSet.CONCUR_READ_ONLY, but even then, the error was still happening -- just never to me. In
                    // could be that our development environment is somehow configured differently, but I don't know
                    // how. It could be related to running multiple versions of EvLoader at the same time. It could be
                    // the weird SQL clauses such as WHERE evid in (a, b, c, d, e, f, g, h) and evid > 0 and evid < 100
                    // (where a - h are actual evids), but I couldn't get it to happen with any of those situations
                    // either. So ... who knows? For now, I am going to try to collect a lot of information and try to
                    // continue, if possible ... JELewis, Winter 2008
                    StringBuilder msg = new StringBuilder("\n\n--- FETCH OUT OF SEQUENCE ERROR DIAGNOSTIC INFORMATION "
                            + "---\n");
                    msg.append("ERROR in DAOIterator.next().\nSelect statment = " + this.selectStatement
                            + "\nException message: " + e.getMessage() + "\nStack Trace:\n");
                    msg.append(DBDefines.STACK_TRACE_STRING(e));
                    msg.append("\n\tconnection: " + getConnection() + "\tstatement: " + this.statement
                            + "\tresult set: " + this.resultSet + "\tcurrentValid: " + this.currentValid);
                    msg.append("\n---------------------------------\n\n");
                    DBDefines.ERROR_LOG.add(msg.toString());

                    // Try calling next again to see if maybe the second time around it won't be an issue
                    if (!this.reissuedCallToNext) {
                        this.reissuedCallToNext = true;
                        return next();
                    }
                    // Only re-call next() again once
                    return null;
                }
                String msg = "ERROR in DAOIterator.next().\n" + "Select statment = " + this.selectStatement
                        + "\nException message: " + e.getMessage();
                DBDefines.ERROR_LOG.add(msg);
                return null;
            }
        }

        /**
         * Remove a row from the list of rows being iterated over.
         */
        public void remove() {
            // This is not implemented for this iterator since it's yucky to mess with removing things from ResultSets,
            // but it is present since it is necessary to implement the Iterator interface. Also, it has yet to be
            // needed.
        }
    }

    /**
     * This class implements the Iterator interface that iterates over rows returned from executing a select statement.
     * The next() method returns {@link DAO.RowWithoutATable RowWithoutATable} objects constructed from the returned
     * data.
     */
    private class DAORowWithoutATableIterator implements Iterator<RowWithoutATable> {
        /**
         * Iterator's own statement object for accessing the database. This way, the DAODatabase statement object will
         * not be tied up.
         */
        private Statement statement = null;

        /**
         * Iterator's ResultSet that will maintain an open connection to the database while the returned rows are being
         * iterated over.
         */
        private ResultSet resultSet = null;

        /**
         * Whether or not what the ResultSet is currently pointing to is a valid record
         */
        private boolean currentValid = false;

        /**
         * Keep track of if a FetchOutOfSequence error has been caught, ignored, and next() recalled. This is an attempt
         * to be able to continue execution if a fetch out of sequence error is encountered since the error has yet to
         * be reproduced and thus is still unresolved.
         */
        private boolean reissuedCallToNext = false;

        /**
         * Select statement that this iterator executes to retrieve rows to iterate over. This is only used in the
         * constructor and for error output.
         */
        private String selectStatement;

        /**
         * Constructor.
         *
         * @param selectStatement select statement to execute against the database
         * @throws FatalDBUtilLibException if an error occurs while establishing a connection to the database
         */
        public DAORowWithoutATableIterator(String selectStatement) throws FatalDBUtilLibException {
            try {
                this.selectStatement = selectStatement;

                // Create the statement object and execute the select statement
                this.statement = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,
                        ResultSet.CONCUR_READ_ONLY);
                this.resultSet = this.statement.executeQuery(this.selectStatement);

                // advance the result set to the first row. Set currentValid to true if resultSet points to a valid row.
                this.currentValid = this.resultSet.next();
                if (!this.currentValid) {
                    this.resultSet.close();
                    this.statement.close();
                }
            } catch (Exception e) {
                String msg = "DAODatabase ERROR in DAORowWithoutATableIterator(" + selectStatement
                        + ") constructor.  Returning null.\n" + "Exception message: " + e.getMessage();
                throw new DBDefines.FatalDBUtilLibException(msg);
            }
        }

        /**
         * Return whether or not there are more rows to iterate over.
         *
         * @return whether or not there are more rows to iterate over
         */
        public boolean hasNext() {
            return this.currentValid;
        }

        /**
         * Return the row that is the next to be iterated over
         *
         * @return the row that is the next to be iterated over
         */
        public RowWithoutATable next() {
            try {
                if (!this.currentValid) {
                    this.resultSet.close();
                    this.statement.close();
                    return null;
                }

                // Get the ResultSetMetaData since that's where we are going to get column names from since we don't
                // have a table to get them from.
                ResultSetMetaData rsmd = this.resultSet.getMetaData();
                // Retrieve how many columns are returned
                int colNbr = rsmd.getColumnCount();

                // Create the row that will hold the values to be returned.
                RowWithoutATable row = new RowWithoutATable();
                for (int i = 0; i < colNbr; i++) {
                    String colName = rsmd.getColumnName(i + 1);
                    row.addToDataMap(colName, this.resultSet.getObject(colName));
                }

                // Advance ResultSet cursor
                this.currentValid = this.resultSet.next();
                if (!this.currentValid) {
                    this.resultSet.close();
                    this.statement.close();
                }
                return row;
            } catch (SQLException e) {
                if (e.getMessage().toLowerCase().contains("fetch out of sequence")) {
                    // I have been unable to produce test code that generates this message. It seems to happen
                    // sporadically for reasons I cannot determine. All of the fixes for this error that I have found
                    // online apply to situations that are not happening here (SQL select statements with a "for update"
                    // clauses, PL/SQL code with cursors, autoCommit settings in both of the previous situations, and so
                    // on). The best I could do was to set the ResultSet to use ResultSet.TYPE_FORWARD_ONLY and
                    // ResultSet.CONCUR_READ_ONLY, but even then, the error was still happening -- just never to me. In
                    // could be that our development environment is somehow configured differently, but I don't know
                    // how. It could be related to running multiple versions of EvLoader at the same time. It could be
                    // the weird SQL clauses such as WHERE evid in (a, b, c, d, e, f, g, h) and evid > 0 and evid < 100
                    // (where a - h are actual evids), but I couldn't get it to happen with any of those situations
                    // either. So ... who knows? For now, I am going to try to collect a lot of information and try to
                    // continue, if possible ... JELewis, Winter 2008
                    StringBuilder msg = new StringBuilder("\n\n--- FETCH OUT OF SEQUENCE ERROR DIAGNOSTIC INFORMATION "
                            + "---\n");
                    msg.append("ERROR in DAOIterator.next().\nSelect statment = " + this.selectStatement
                            + "\nException message: " + e.getMessage() + "\nStack Trace:\n");
                    msg.append(DBDefines.STACK_TRACE_STRING(e));
                    msg.append("\n\tconnection: " + getConnection() + "\tstatement: " + this.statement
                            + "\tresult set: " + this.resultSet + "\tcurrentValid: " + this.currentValid);
                    msg.append("\n---------------------------------\n\n");
                    DBDefines.ERROR_LOG.add(msg.toString());

                    // Try calling next again to see if maybe the second time around it won't be an issue
                    if (!this.reissuedCallToNext) {
                        this.reissuedCallToNext = true;
                        return next();
                    }
                    // Only re-call next() again once
                    return null;
                }
                String msg = "ERROR in DAORowWithoutATableIterator.next().\n" + "Select statment = "
                        + this.selectStatement + "\nException message: " + e.getMessage();
                DBDefines.ERROR_LOG.add(msg);
                return null;
            }
        }

        /**
         * Remove a row from the list of rows being iterated over
         */
        public void remove() {
            // This is not implemented for this iterator since it's yucky to mess with removing things from ResultSets,
            // but it is present since it is necessary to implement the Iterator interface. Also, it has yet to be
            // needed.
        }
    }
}
