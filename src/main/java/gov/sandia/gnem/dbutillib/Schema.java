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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBException;

import gov.sandia.gnem.dbutillib.dao.DAO;
import gov.sandia.gnem.dbutillib.dao.DAODatabase;
import gov.sandia.gnem.dbutillib.dao.DAOFactory;
import gov.sandia.gnem.dbutillib.dao.DAOFlatFile;
import gov.sandia.gnem.dbutillib.jaxb.ObjectFactory;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * This is a class to manage a group of related tables. A Schema is modeled as a directed graph. The vertices of the
 * graph are the tables, and the edges are SQL SELECT statements that constitute the relationships between tables. A
 * Schema object can be seen as an encapsulation of all the information contained in an Entity Relationship Diagram
 * (ERD). All ids within rows that belong to the same Schema must be internally consistent.
 * <p>
 * Schema has methods to add tables to the schema, add relationships to the schema, complete schema setup, and manage a
 * connection to a {@link DAO DAO} (Data Access Object) that can execute queries against data.
 * <p>
 * <b>Important Note:</b> {@link #completeSetup completeSetup} must be called after all tables and relationships are
 * added to the Schema after the constructor is called. If the Schema constructor is given a {@link ParInfo ParInfo}
 * object with tables in it, then the constructor will call {@link #completeSetup completeSetup}. Once
 * {@link #completeSetup completeSetup} is called, no more tables or relationships may be added to the Schema without
 * calling {@link #completeSetup completeSetup} again.
 * <p>
 * Since a Schema object can own a connection to a database, it is essential essential that applications that
 * instantiate a Schema object close the object when they are done with it using the {@link #close close} method.
 * <p>
 * Below is an example of setting up a schema using a {@link ParInfo ParInfo} object. Note that parameter names are case
 * insensitive, and '_' (underscore) characters are ignored. This example sets up a schema named "Source". Note also
 * that the {@link ParInfo#appendParameter appendParameter} function is overloaded such that it can accept anywhere from
 * 2 to 6 parameters of type String. <br>
 * <code>
 * <pre>
 * ParInfo configInfo = new ParInfo();
 * // Configure how to access the data
 * configInfo.addParameter(&quot;Source_DAOType&quot;, &quot;DB&quot;);
 * configInfo.addParameter(&quot;Source_UserName&quot;, &quot;DBTOOLS&quot;);
 * configInfo.addParameter(&quot;Source_PassWord&quot;, &quot;DBTOOLS&quot;);
 * configInfo.addParameter(&quot;Source_Instance&quot;, &quot;jdbc:oracle:thin:@bikinifire:1521:histdb&quot;);
 * configInfo.addParameter(&quot;Source_Driver&quot;, &quot;oracle.jdbc.driver.OracleDriver&quot;);
 * // Configure the table definition information
 * configInfo.addParameter(&quot;Source_TableDefinitionTable&quot;, &quot;NNSA_TABLE_DEFS&quot;);
 * configInfo.addParameter(&quot;Source_TableDefinitionTableDAOType&quot;, &quot;DB&quot;);
 * configInfo.addParameter(&quot;Source_TableDefinitionTableUserName&quot;, &quot;DBTOOLS&quot;);
 * configInfo.addParameter(&quot;Source_TableDefinitionTablePassWord&quot;, &quot;DBTOOLS&quot;);
 * configInfo.addParameter(&quot;Source_TableDefinitionTableInstance&quot;, &quot;jdbc:oracle:thin:@bikinifire:1521:histdb&quot;);
 * configInfo.addParameter(&quot;Source_TableDefinitionTableDriver&quot;, &quot;oracle.jdbc.driver.OracleDriver&quot;);
 * // Source Schema tables ...
 * // Table named idc_origin of type origin with unique keys turned on
 * configInfo.appendParameter(&quot;Source_Tables&quot;, &quot;idc_origin&quot;, &quot;origin&quot;, &quot;on&quot;);
 * // Table named idc_arrival of type arrival with no specification regarding
 * // unique keys, so they will be turned on.
 * configInfo.appendParameter(&quot;Source_Tables&quot;, &quot;idc_arrival&quot;, &quot;arrival&quot;);
 * // Table named idc_assoc of type assoc. (Note how both of those parameters
 * // are included in a single string.)
 * configInfo.appendParameter(&quot;Source_Tables&quot;, &quot;idc_assoc  assoc&quot;);
 * // Source Schema relationships ...
 * // Establish a relationship between the origin and the assoc table.  For
 * // a given row in the origin table, related assoc rows are those whose
 * // orid matches the orid of the given origin row.  The 0/N indicates
 * // that 0 or more assoc rows are expected to be related to the given
 * // origin row.  (Note how all of the parameters needed for a relationship
 * // are specified in one single string.
 * configInfo.appendParameter(&quot;Source_Relationships&quot;, &quot;1 origin assoc orid=#orid#  0/N&quot;);
 * // Establish a relationship between the assoc and the arrival table.  For
 * // a given row in the assoc table, related arrival rows are those whose
 * // arid matches the arid of the given assoc row.  The 1 indicates that
 * // 1 and only 1 arrival row may be related to the given assoc row.
 * configInfo.appendParameter(&quot;Source_Relationships&quot;, &quot;2&quot;, &quot;assoc&quot;, &quot;arrival&quot;, &quot;arid=#arid#&quot;, &quot;1&quot;);
 * </pre>
 * </code> Source tables and relationships can also be added to a schema using the {@link #addTable addTable} and the
 * {@link #addRelationship addRelationship} methods. If those methods are used, {@link #completeSetup completeSetup}
 * must be called before the Schema is ready for use.
 * <p>
 * In all of the above the substring 'Source' is the name of the Schema that you are creating and can be any String,
 * including "".
 * <p>
 * To construct this schema: <br>
 * <code>Schema sourceSchema = new Schema(configInfo, "Source");</code>
 * <p>
 * <b><i>Many</i></b> other parameters can be specifed when creating a Schema. Please see the
 * {@link #Schema(gov.sandia.gnem.dbutillib.ParInfo, java.lang.String) Schema constructor} for more information on
 * Schema parameters.
 * <p>
 * Note, a ParInfo item knows about several environment properties that it will use as defaults. The way this works is
 * that if the value of a parameter is requested from a ParInfo object, and the name of the parameter is not currently
 * in the ParInfo object, then the value from the system properties is returned that ends in the same value as the
 * parameter requested will be returned. For example, if SourceUserName is not in the ParInfo object, and
 * DBTOOLS_USERNAME is specified in the user's environment, then the value in the user's environment is returned by the
 * ParInfo object.
 * <p>
 * A typical .cshrc file might include the following environment variables: <code>
 * <br>DBTOOLS_USERNAME = DBTOOLS
 * <br>DBTOOLS_PASSWORD = DBTOOLS
 * <br>DBTOOLS_INSTANCE = jdbc:oracle:thin:@bikinifire:1521:histdb
 * <br>DBTOOLS_DRIVER   = oracle.jdbc.driver.OracleDriver
 * <br>DBTOOLS_TABLEDEF = GNEM_SCHEMAS.NNSA_TABLE_DEFS
 * </code>
 */
public class Schema {
    /**
     * This Schema's name.
     */
    protected String name = null;

    /**
     * A HashMap of (Table)startTable -> (Table)endTable -> Relationship. This defines a graph where the vertices are
     * startTable objects, and each vertex has a HashMap of links to targetTable objects. Each link is a Relationship
     * object which defines a sourceTable and targetTable as well as the select statement template that will retrieve
     * Row objects from the targetTable. (See Relationship class.)
     */
    protected LinkedHashMap<Table, LinkedHashMap<Table, Relationship>> tableGraph = new LinkedHashMap<Table, LinkedHashMap<Table, Relationship>>();

    /**
     * HashMap from table type to relationships where that table type is the source table.
     */
    protected HashMap<String, ArrayList<Relationship>> sourceTableToRelationships = new HashMap<String, ArrayList<Relationship>>();
    /**
     * HashMap from table type to relationships where that table type is the target table.
     */
    protected HashMap<String, ArrayList<Relationship>> targetTableToRelationships = new HashMap<String, ArrayList<Relationship>>();

    /**
     * A HashMap from (String) tableName -> Table (Table object).
     */
    protected LinkedHashMap<String, Table> tableNameToTable = new LinkedHashMap<String, Table>();

    /**
     * A HashMap from (String) tableType -> (String) tableName.
     */
    protected HashMap<String, String> tableTypeToName = new HashMap<String, String>();

    /**
     * A LinkedList of Relationship objects that contains all the Relationship objects contained in this schema.
     */
    protected LinkedList<Relationship> relationships = new LinkedList<Relationship>();

    /**
     * A HashMap from table type to the hint for that table. Note that the hint must contain a reference to the table
     * type enclosed in ##. So, if the table type is origin, the hint must contain #origin#.
     */
    protected HashMap<String, String> tableTypeToHint = new HashMap<String, String>();

    /**
     * A map from (String) table name -> (String) row name components. The components are the names of the fields in a
     * row that will make up the toString() of a Row. Examples would be event -> evid_prefor_auth. These must be stored
     * in the Schema since a scenario exists where a Schema could be constructed from a ParInfo object that contains no
     * Table information, but that does contain RowNameComponents information. Those RowNameComponents need to be saved
     * to be used when tables are added later using {@link #addTable addTable} and then {@link #completeSetup
     * completeSetup} is called.
     */
    protected HashMap<String, String> rowNameComponents = new HashMap<String, String>();

    /**
     * A HashMap from (String) ownedID name -> (Table) table that owns the ID.
     */
    protected HashMap<String, Table> ownedIDToTable = new HashMap<String, Table>();

    /**
     * This Schema's DAO (Data Access Object).
     */
    protected DAO dao = null;

    /**
     * This Schema's table definition table.
     */
    protected TableDefinition tableDefinitionTable = null;

    /**
     * The date format that will be applied to all the columns of javaType DATE in all the tables in this schema. This
     * can be set to override the date formats that DATE columns retrieve from the table defininition table since these
     * can vary wildly across different representations of the same table definition table information.
     */
    protected String dateFormat = null;

    /**
     * If autoTableCreation is true, then any tables that don't exist in this schema will be created. This must be
     * stored in the Schema since a scenario exists where a Schema could be constructed from a ParInfo object that
     * contains no Table information, but that does contain AutoTableCreation information. That AutoTableCreation
     * information needs to be saved to be used when tables are added later using {@link #addTable addTable} and then
     * {@link #completeSetup completeSetup} is called.
     */
    private boolean autoTableCreation = false;

    /**
     * A list of sql commands the user would like to have executed within the Schema's dao connection immediately after
     * the connection to the dao is established if the dao type is DB. This must be saved since a scenario exists where
     * a Schema could be constructed from a ParInfo object that contains no Table information, but that does contain
     * InitializationSQL information. That InitializationSQL information needs to be saved to be used when tables are
     * added later using {@link #addTable addTable} and then {@link #completeSetup completeSetup} is called since the
     * connection is established via {@link #completeSetup completeSetup}
     */
    private String initializationSQL = "";

    /**
     * The name of the remap table associated with this Schema. This name can also be retrieved via
     * remapTable.getName(). However, it also needs to be stored since the remap table name information will be read in
     * in the Schema constructor, but the actual remap table creation takes place later.
     */
    private String remapTableName = "";

    /**
     * The name of the idgaps table associated with this Schema. This name can also be retrieved via
     * idGapsTable.getName(). However, it also needs to be stored since the idgaps table name information will be read
     * in in the Schema constructor, but the actual idgaps table creation takes place later.
     */
    private String idGapsTableName = "";

    /**
     * If the user is going to use sequences instead of idgaps tables, that information will be used when creating the
     * IdGapsTable object instead of idgaps tables.
     */
    private String nextIdSequences = "";

    /**
     * This Schema's Remap table.
     */
    protected RemapTable remapTable = null;

    /**
     * Value to be placed in the SOURCE column of the remap table.
     */
    private String remapSource = null;

    /**
     * This Schema's IDGaps table.
     */
    protected IDGapsTable idGapsTable = null;

    /**
     * A HashMap from a (String)ownedID -> (String) the remap source of the associated item. A remap source is the
     * string that goes into the SOURCE column in the Remap table for an id in this Schema.
     */
    protected HashMap<String, String> ownedIDToRemapSource = new HashMap<String, String>();

    /**
     * Whether or not to truncate tables in this schema before beginning processing. Truncating a table will remove all
     * data from the table without dropping the table itself.
     */
    private boolean truncateTables = false;

    /**
     * Whether or not to prompt the user before truncating tables. When true, a command line prompt will appear that
     * informs the users of which tables will be truncated and ask if it is okay to continue.
     */
    private boolean promptBeforeTruncate = true;

    /**
     * If SQLTimer is true, then everytime a Relationship object is executed it will send a message to the
     * DBDefines.STATUS_LOG specifying the number of milliseconds it took to execute the Select statement.
     */
    protected boolean SQLTimer = false;

    /**
     * Whether or not to use the DAO in the XML file (if the DAO type is XML) as opposed to dao information specified at
     * Schema construction time.
     */
    protected boolean includeXMLDAO = false;

    /**
     * How load dates are to be handled. This will need to be set to one of the load date handling options in DBDefines
     * (currently only {@link DBDefines#FIX_LDDATE FIX_LDDATE} and {@link DBDefines#IGNORE_FF_LDDATE IGNORE_FF_LDDATE}
     */
    protected String lddateOption = "";

    /**
     * Keeps track of whether or not this object has been closed. Don't want the finalize method to call close() if the
     * object has already been closed.
     */
    private boolean schemaClosed = false;

    /**
     * Schema constructor created with information from a ParInfo object. The parameter names expected (not case
     * sensitive) are listed below followed by what's expected to be in those parameters. Note that these parameter
     * names must be prefixed with the value in name. Many times par files have information for more than one Schema, so
     * say this Schema is for data for an application called Pancakes. Then, your tables for that application could be
     * in the tables variable (in which case the name parameter would be ""), or it could be in the PancakesTables
     * variable (in which case the name parameter would be "Pancakes"). If there is table information in the ParInfo
     * object, then this constructor will call {@link #completeSetup completeSetup}. If there is not table information,
     * then {@link #completeSetup completeSetup} must be called once it is added.
     * <table cellpadding="5">
     * <tr valign="top">
     * <td>Tables</td>
     * <td>Schema tables. See {@link #addTables addTables} for more information about what sort of information is
     * required for tables parameters.</td>
     * </tr>
     * <tr valign="top">
     * <td>Relationships</td>
     * <td>Schema relationships (optional) See {@link #addRelationships addRelationships} for more information about
     * what sort of information is required for relationships parameters.</td>
     * </tr>
     * <tr valign="top">
     * <td>RowNameComponents</td>
     * <td>Schema RowNameComponents (optional). RowNameComponents are the names of the columns that make up all of the
     * components of a row name.</td>
     * </tr>
     * <tr valign="top">
     * <td>DateFormat</td>
     * <td>Schema DateFormat (optional) The date format that will be applied to all the columns of javaType DATE in all
     * the tables in this schema. This can be set to override the date formats that DATE columns retrieve from the table
     * defininition table since these can vary wildly across different representations of the same table definition
     * table information. See java.text.SimpleDateFormat API for examples of what this variable should be set to)</td>
     * </tr>
     * <tr valign="top">
     * <td>DAOType</td>
     * <td>This Schema's DAO type (optional - environment variables are used if not specified)</td>
     * </tr>
     * <tr valign="top">
     * <td>Username</td>
     * <td>This Schema's username if the DAO type is DB (optional - environment variables are used if not specified)</td>
     * </tr>
     * <tr valign="top">
     * <td>Password</td>
     * <td>This Schema's password if the DAO type is DB - (optional - environment variables are used if not specified)</td>
     * </tr>
     * <tr valign="top">
     * <td>Instance</td>
     * <td>This Schema's instance if the DAO type is DB - (optional - environment variables are used if not specified)</td>
     * </tr>
     * <tr valign="top">
     * <td>Driver</td>
     * <td>This Schema's driver if the DAO type is DB - (optional - environment variables are used if not specified)</td>
     * </tr>
     * <tr valign="top">
     * <td>XMLInputFile</td>
     * <td>This Schema's XML file to read data in from if the DAO type is XML (optional)</td>
     * </tr>
     * <tr valign="top">
     * <td>XMLOutputFile</td>
     * <td>This Schema's XML file that output will be written to if the DAO type is XML (optional)</td>
     * </tr>
     * <tr valign="top">
     * <td>TableDefinitionTable</td> </td>
     * <td>Table definition table name (either a table in the database if the DAOType is DB or or the name of a flat
     * file table definition table if the DAOType is FF) (optional - environment variables are used if not specified)</td>
     * </tr>
     * <tr valign="top">
     * <td>TableDefinitionTableDAOType</td>
     * <td>DAO Type for the table definition table (optional - environment variables are used if not specified. If
     * neither this parameter nor environment variables are used, then the DAOType parameter for this Schema is used)</td>
     * </tr>
     * <tr valign="top">
     * <td>TableDefinitionTableUsername</td>
     * <td>Username for the table definition table (optional - environment variables are used if not specified. If
     * neither this parameter nor environment variables are used, then the Username parameter for this Schema is used)</td>
     * </tr>
     * <tr valign="top">
     * <td>TableDefinitionTablePassword</td>
     * <td>Password for the table definition table (optional - environment variables are used if not specified. If
     * neither this parameter nor environment variables are used, then the Password parameter for this Schema is used)</td>
     * </tr>
     * <tr valign="top">
     * <td>TableDefinitionTableDriver</td>
     * <td>Driver for the table definition table (optional - environment variables are used if not specified. If neither
     * this parameter nor environment variables are used, then the Driver parameter for this Schema is used)</td>
     * </tr>
     * <tr valign="top">
     * <td>TableDefinitionTableInstance</td>
     * <td>Instance for the table definition table (optional - environment variables are used if not specified. If
     * neither this parameter nor environment variables are used, then the Instance parameter for this Schema is used)</td>
     * </tr>
     * <tr valign="top">
     * <td>RemapTableName</td>
     * <td>Name of the Remap table for this Schema (optional)</td>
     * </tr>
     * <tr valign="top">
     * <td>RemapSource</td>
     * <td>Value to place in the SOURCE column of the remap table (optional)</td>
     * </tr>
     * <tr valign="top">
     * <td>IDGapsTableName</td>
     * <td>Name of the IDGaps table for this Schema (optional)</td>
     * </tr>
     * <tr valign="top">
     * <td>AutoTableCreation</td>
     * <td>When true, create schema tables if they do not already exist (optional)</td>
     * </tr>
     * <tr valign="top">
     * <td>TruncateTables</td>
     * <td>When true, drop and recreate schema tables if they already exist (optional)</td>
     * </tr>
     * <tr valign="top">
     * <td>PromptBeforeTruncate</td>
     * <td>When true, prompt user before truncating tables (TruncateTables would also need to be set to true (optional)</td>
     * </tr>
     * <tr valign="top">
     * <td>InitializationSQL</td>
     * <td>A list of sql commands the user would like to have executed within the Schema's dao connection immediately
     * after the connection to the dao is established.(optional)</td>
     * </tr>
     * <tr valign="top">
     * <td>NextIdSequences</td>
     * <td>When this parameter is nonempty, the schema will get next id information from oracle sequences instead of the
     * idgaps table. The format for this parameter is <br>
     * column_name sequence_name <br>
     * For example: <code> NextIdSequences = orid orid_sequence_name evid evid_sequence_name </code> (optional)</td>
     * </tr>
     * <tr valign="top">
     * <td>IncludeXMLDAO</td>
     * <td>If this schema's dao type is XML, set this to true if you wish to use the DAO information in the XML file
     * associated with this Schema instead of the environment (optional)</td>
     * </tr>
     * <tr valign="top">
     * <td>LddateOption</td>
     * <td>How load dates are to be handled. (optional) Currently, the only supported values are: <br>
     * FIX_LDDATE: Do not change LDDATE to current date when writing out row data. <br>
     * IGNORE_FF_LDDATE: Ignore LDDATEs when reading in data from flat files. This will only work for columns named
     * LDDATE that are the last column in each flat file row</td>
     * </tr>
     * </table>
     * <p>
     * <b>Important Note:</b>These variable names can be prefixed by the value in <code>name</code>.
     *
     * @param configInfo par file that parameter values are being read from; if this parameter is null, the schema will
     *                   be completely empty
     * @param name       prefix String that par file variables for this Schema are prefixed with - this will also be the name
     *                   of this Schema
     * @throws FatalDBUtilLibException if any DBUtilLib exceptions are encountered
     */
    public Schema(ParInfo configInfo, String name) throws FatalDBUtilLibException {
        if (configInfo == null)
            return;
        if (name == null)
            this.name = "";
        else
            this.name = name;

        // This must come first so setting other parameters will be done correctly.
        DBDefines.convertToUpperCase = configInfo.getItem("ConvertToUpperCase", "true").equalsIgnoreCase("true");

        // Create appropriate dao object
        try {
            this.dao = DAOFactory.create(this, configInfo, this.name);
        } catch (FatalDBUtilLibException e) {
            String error = "Error in Schema constructor for " + name + " schema when creating DAO.\nError message: "
                    + e.getMessage();
            throw new FatalDBUtilLibException(error);
        }

        // Create table definition table
        try {
            if (configInfo.getItem(name + "TableDefinitionTableDAOType") == null)
                configInfo.addParameter(name + "TableDefinitionTableDAOType", configInfo.getItem(name + "DAOType"));

            if (configInfo.getItem(name + "TableDefinitionTableUsername") == null)
                configInfo.addParameter(name + "TableDefinitionTableUsername", configInfo.getItem(name + "Username"));

            if (configInfo.getItem(name + "TableDefinitionTablePassword") == null)
                configInfo.addParameter(name + "TableDefinitionTablePassword", configInfo.getItem(name + "Password"));

            if (configInfo.getItem(name + "TableDefinitionTableInstance") == null)
                configInfo.addParameter(name + "TableDefinitionTableInstance", configInfo.getItem(name + "Instance"));

            if (configInfo.getItem(name + "TableDefinitionTableDriver") == null)
                configInfo.addParameter(name + "TableDefinitionTableDriver", configInfo.getItem(name + "Driver"));

            this.tableDefinitionTable = new TableDefinition(configInfo, this.name);
        } catch (FatalDBUtilLibException e) {
            String error = "Error in Schema constructor when creating Table " + "Definition Table.\nError message: "
                    + e.getMessage();
            throw new FatalDBUtilLibException(error);
        }

        setRowNameComponents(configInfo
                .getItem(name + "RowNameComponents", configInfo.getItem("RowNameComponents", "")));

        dateFormat = configInfo.getItem(name + "DateFormat", null);

        autoTableCreation = configInfo.getItem(name + "AutoTableCreation", "false").equalsIgnoreCase("true");

        truncateTables = configInfo.getItem(name + "TruncateTables", "false").equalsIgnoreCase("true");

        promptBeforeTruncate = configInfo.getItem(name + "PromptBeforeTruncate", "true").equalsIgnoreCase("true");

        remapTableName = remapTableToUse(configInfo);
        // If no remap source is specified, default to the account the user
        // is connected to (only really applies when the daotype is DB).
        remapSource = configInfo.getItem(name + "RemapSource", "#ACCOUNT#");

        idGapsTableName = idGapsTableToUse(configInfo);
        initializationSQL = configInfo.getItem(name + "InitializationSQL", "");
        nextIdSequences = configInfo.getItem(name + "NextIdSequences", "");

        String loadDateOption = configInfo.getItem(name + "LddateOption", "");
        if (loadDateOption.equalsIgnoreCase(DBDefines.FIX_LDDATE))
            lddateOption = DBDefines.FIX_LDDATE;
        if (loadDateOption.equalsIgnoreCase(DBDefines.IGNORE_FF_LDDATE))
            lddateOption = DBDefines.IGNORE_FF_LDDATE;

        String tableInfo = tablesToUse(configInfo);

        addHints(configInfo.getItem(this.name + "Hints", ""));

        if (this.dao.getType().equals(DBDefines.XML_DAO))
            includeXMLDAO = configInfo.getItem(name + "IncludeXMLDAO", "").toLowerCase().startsWith("t");

        // Only call completeSetup in the constructor if there is table
        // information. Otherwise, the user must add tables later in order
        // for Schema to do anything significant, and they must explicitly
        // call completeSetup when they are done.
        if (tableInfo.length() > 0) {
            try {
                addTables(tableInfo);
                addRelationships(configInfo.getItem(this.name + "Relationships", ""));
                // completeSetup calls dao.createConnection(). Thus, it does not
                // need to be explicitly called here.
                completeSetup();
            } catch (FatalDBUtilLibException e) {
                String error = "Error in Schema constructor for " + name
                        + " schema when calling addTables() addRelationships(), "
                        + "or completeSetup().\nError message: " + e.getMessage();
                throw new FatalDBUtilLibException(error);
            }
        } else {
            try {
                // createConnection to the Data Access Object. For database dao, this
                // creates the jdbc connections. For flatfile and xml dao's, this call
                // reads in the data from the files into internal row graphs maintained by
                // the dao objects.
                dao.createConnection();
            } catch (FatalDBUtilLibException e) {
                String error = "Error in Schema constructor when calling " + "dao.createConnection().\nError message: "
                        + e.getMessage();
                throw new FatalDBUtilLibException(error);
            }
        }
    }

    /**
     * This constructor calls the {@link #Schema(ParInfo, String) Schema} constructor with name set to "". See the
     * {@link #Schema(ParInfo, String) Schema} constructor comments for more information.
     *
     * @param configInfo par file that parameter values are being read from
     * @throws FatalDBUtilLibException if any DBUtilLib exceptions are encountered
     */
    public Schema(ParInfo configInfo) throws FatalDBUtilLibException {
        this(configInfo, "");
    }

    /**
     * This constructor call the {@link #Schema(ParInfo, String) Schema} constructor with name set to "" and parInfo set
     * to new ParInfo(). See the {@link #Schema(ParInfo, String) Schema} constructor for more information.
     *
     * @throws FatalDBUtilLibException if an error occurs
     */
    public Schema() throws FatalDBUtilLibException {
        this(new ParInfo(), "");
    }

    /**
     * Make sure that connections to the database (if any) are closed up when this object goes away.
     */
    @Override
    public void finalize() {
        if (!this.schemaClosed)
            close();
    }

    /**
     * Turn the SQL timer on (true) or off (false). When on, everytime a Relationship object is executed it will send a
     * message to the DBDefines.STATUS_LOG specifying the number of milliseconds it took to execute the Select
     * statement.
     *
     * @param on set to true to turn on the SQLTimer.
     */
    public void setSQLTimer(boolean on) {
        SQLTimer = on;
    }

    /**
     * Add information about multiple tables to the schema. The input string may consist of multiple lines of
     * information, separated by newlines. Each record should contain two or three words, separated by one or more
     * spaces and/or tab characters. <br>
     * The first word is the name of the table (e.g. my_origin). <br>
     * The second word is the type of table it is (e.g. origin). <br>
     * The third (optional) word is whether to turn unique keys on (true) or not (false). This defaults to true.
     *
     * @param tableInfo table information (potentially for multiple tables) encapsulated in a single string. <br>
     *                  Example: <code>
     *                  my_origin   origin   on
     *                  my_event    event    on
     *                  my_assoc    assoc    on
     *                  my_arrival  arrival  on
     *                  </code>
     * @return true if all of the tables were successfully added to the schema.
     * @throws FatalDBUtilLibException if any DBUtilLib exceptions are encountered.
     */
    public boolean addTables(String tableInfo) throws FatalDBUtilLibException {
        if (tableInfo == null || tableInfo.length() == 0) {
            throw new FatalDBUtilLibException("Error in Schema.addTables()."
                    + "  Received a null or length 0 tableInfo parameter." + "  Tables could not be added.");
        }

        // Whether or not table information has been added successfully.
        boolean ok = true;

        // replace all tab characters with spaces and all instances of more
        // than one space with only one space
        // tableInfo = tableInfo.replaceAll("\t", " ").replaceAll(" *", " ");
        tableInfo = DBDefines.removeExtraSpaces(tableInfo);

        // Get each line of table information
        String[] lines = DBDefines.splitOnNewLine(tableInfo);

        // Look at all of the lines that came in from tables - each line
        // represents a set of related table information.
        for (String currLine : lines) {
            String[] tableWords = ParInfo.handleParametersWithSpaces(currLine);

            // 2 words - currLine had a table name and a table type. No unique keys.
            if (tableWords.length == 2) {
                String tableName = tableWords[0];
                String tableType = tableWords[1];

                // Add the table.
                if (!addTable(tableName, tableType, "")) {
                    DBDefines.ERROR_LOG.add("Schema.addTable(" + tableName + ", " + tableType
                            + ") failed. addTables returning false.");
                    ok = false;
                } else if (false) {
                    DBDefines.STATUS_LOG.add("Schema " + tableName + " table successfully created.");
                }
            }
            // 3 words - currLine had a table name, table type and unique keys.
            else if (tableWords.length == 3) {
                String tableName = tableWords[0];
                String tableType = tableWords[1];
                String uniqueKeys = tableWords[2];

                // Add the table.
                if (!addTable(tableName, tableType, uniqueKeys)) {
                    DBDefines.ERROR_LOG.add("Schema.addTable(" + tableName + ", " + tableType + ", " + uniqueKeys
                            + ") failed. addTables returning false.");
                    ok = false;
                } else {
                    DBDefines.STATUS_LOG.add("Schema " + tableName + " table successfully created.");
                }
            } else {
                DBDefines.ERROR_LOG.add("Schema: " + tableWords.length + " is not"
                        + " an acceptable number of words (either 2 or 3) for "
                        + "creating table information. Words received: " + currLine + ". addTables returning false.");
                ok = false;
            }
        }
        return ok;
    }

    /**
     * Add a single table to the schema. Duplicate table types are not allowed.
     *
     * @param tableName  the name of the table to add to the schema
     * @param tableType  the type of the table to add to the schema.
     * @param uniqueKeys if this string is empty, equal to "false" or equal to "0", then no unique keys are set. If the
     *                   string is anything else, then unique keys are set to the unique keys specified in the table definition table.
     * @return true if the table was successfully added.
     * @throws FatalDBUtilLibException if any DBUtilLib exceptions are encountered.
     */
    public boolean addTable(String tableName, String tableType, String uniqueKeys) throws FatalDBUtilLibException {
        // Check to see if parameters are valid.
        if ((tableName == null) || (tableType == null) || (tableName.length() == 0) || (tableType.length() == 0))
            throw new FatalDBUtilLibException("FATAL ERROR in Schema.addTable().  Received a null "
                    + " and/or length 0 table name and/or table type with tableName: " + tableName + " and tableType: "
                    + tableType + ".");

        tableName = tableName.trim();
        tableType = tableType.trim();
        if (DBDefines.convertToUpperCase)
            tableType = tableType.toUpperCase();

        Table newTable = new Table(tableName, tableType, uniqueKeys, this);

        return addTable(newTable);
    }

    /**
     * Add a table to the schema.
     *
     * @param newTable Table to be added
     * @return whether or not newTable was successfully added to the schema
     * @throws FatalDBUtilLibException if any DBUtilLib exceptions are encountered
     */
    private boolean addTable(Table newTable) throws FatalDBUtilLibException {
        // Adding a Table that is already in the Schema is not allowed.
        if (this.tableTypeToName.keySet().contains(newTable.tableType))
            return false;

        // Add the new Table to the HashMap of Table names to Table objects.
        this.tableNameToTable.put(newTable.name, newTable);

        // Add an entry to the Table type to Table name HashMap for this Table.
        this.tableTypeToName.put(newTable.tableType, newTable.name);

        // Add this table to the HashMap that is the graph of Tables with their
        // associated HashMap of Relationships with other Tables. Right now,
        // this Table is just being created, so it has no Relationships
        // established yet, so its HashMap is empty.
        this.tableGraph.put(newTable, new LinkedHashMap<Table, Relationship>());

        return true;
    }

    /**
     * Add information about multiple relationships to the schema. Each relationship consists of five pieces of
     * information:
     * <p>
     * 1) A relationship identifier. This string is used only to identify a particular relationship within output. It
     * does not affect the functional logic of the relationship in any way.
     * <p>
     * 2) The source table type. This identifies the table from which values will be extracted to populate the where
     * clause of the select statement.
     * <p>
     * 3) The target table type. This identifies the table against which the select statement will be executed.
     * <p>
     * 4) The SQL WHERE clause that defines the relationship. Words enclosed in '#' characters are assumed to be column
     * names in the source table. Given a row from the source table, those column names are replaced by the value from
     * that column. Then the select statement is executed against the target table. <br>
     * For example, if the relationship is specified to be "WHERE PREFOR=#ORID#" the substring #ORID# is replaced with
     * the value of ORID in the current row of the source table. Say that ORID value is 123, then the select statement
     * "SELECT * FROM end_table WHERE ORID=123" is executed.
     * <p>
     * 5) The relationship constraint. This defines the number of rows that should be returned when the relationship is
     * executed.
     * <p>
     * Valid constraints are: <br>
     * 0/1 - zero or one row should be returned <br>
     * 1 - exactly one row should be returned <br>
     * 0/N - any number of rows may be returned (no constraint); <br>
     * N - one or more rows must be returned, zero rows is not allowed.
     *
     * @param relationshipInfo relationship information (potentially for multiple relationships) encapsulated in a
     *                         single string. Each line defines a single relationships and must have all of the pieces of information listed
     *                         above. <br>
     *                         Example: <code>
     *                         event->origin    event    origin   prefor=#orid#    1
     *                         origin->assoc    origin   assoc    orid=#orid#      N
     *                         assoc->arrival   assoc    arrival  arid=#arid#      1
     *                         </code>
     * @return true if all relationships are successfully added to the schema.
     * @throws FatalDBUtilLibException if any DBUtilLib exceptions are encountered.
     */
    public boolean addRelationships(String relationshipInfo) throws FatalDBUtilLibException {
        // Relationships are not mandatory for a Schema like tables are
        if ((relationshipInfo == null) || (relationshipInfo.length() == 0))
            return true;

        // Whether or not Relationship information is added successfully.
        boolean ok = true;

        // replace all tab characters with spaces and all instances of more than
        // one space with only one space
        // relationshipInfo = relationshipInfo.replaceAll("\t", " ").replaceAll(" *", " ");
        relationshipInfo = DBDefines.removeExtraSpaces(relationshipInfo);

        // Get each line of relationship information
        String[] lines = DBDefines.splitOnNewLine(relationshipInfo);

        // Look at all the lines that came in from relationships - each line
        // represents a set of related Relationship information.
        for (String currLine : lines) {
            // "Words" of relationship information
            String[] relationshipWords = currLine.split(" ");

            // 5 words - currLine had an id, source table name, target table
            // name, relationship, and constraint.
            if (relationshipWords.length >= 5) {
                String id = relationshipWords[0];
                String sourceTable = relationshipWords[1];
                String targetTable = relationshipWords[2];

                // The relationship's where clause is of variable length. It
                // starts after the 3rd word (the target table) and includes
                // everything but the last word (the constraints).
                StringBuilder relationship = new StringBuilder();
                for (int i = 3; i < relationshipWords.length - 1; i++)
                    relationship.append(relationshipWords[i] + " ");
                String constraint = relationshipWords[relationshipWords.length - 1];

                // Add the Relationship.
                if (!addRelationship(id, sourceTable, targetTable, relationship.toString(), constraint))
                    ok = false;
                else if (false)
                    DBDefines.STATUS_LOG.add("Schema Relationship with id: " + id + ", sourceTableType: " + sourceTable
                            + ", targetTableType: " + targetTable + ", relationship: " + relationship
                            + ", constraint: " + constraint + " successfully created.");
            } else {
                DBDefines.ERROR_LOG.add("Schema: " + relationshipWords.length
                        + " is not an acceptable number of words for creating"
                        + " relationship information. Words received: " + currLine
                        + ". addRelationships returning false.");
                ok = false;
            }
        }
        return ok;
    }

    /**
     * Add a single relationship to the Schema. It is permissible to only specify the first two parameters (id and
     * sourceTableType), with the last 3 parameters specified as null. In this case the table that corresponds to
     * sourceTableType will be an isolated vertex in the table graph.
     *
     * @param id              A relationship identifier. This string is used only to identify a particular relationship within
     *                        output. It does not affect the functional logic of the relationship in any way. If this is equal to "", ?, or
     *                        null, it will be initialized to sourceTableType->targetTableType.
     * @param sourceTableType The source table type. This identifies the table from which values will be extracted to
     *                        populate the where clause of the select statement.
     * @param targetTableType The target table type. This identifies the table against which the select statement will
     *                        be executed.
     * @param relationship    The SQL WHERE clause that defines the relationship. Words enclosed in '#' characters are
     *                        assumed to be column names in the source table. Given a row from the source table, those column names are
     *                        replaced by the value from that column. Then the select statement is executed against the target table. <br>
     *                        For example, if the relationship is specified to be "WHERE PREFOR=#ORID#" the substring #ORID# is replaced with
     *                        the value of ORID in the current row of the source table. Say that ORID value is 123, then the select statement
     *                        "SELECT * FROM end_table WHERE ORID=123" is executed.
     * @param constraint      The relationship constraint. This defines the number of rows that should be returned when the
     *                        relationship is executed.
     *                        <p>
     *                        Valid constraints are: <br>
     *                        0/1 - zero or one row should be returned <br>
     *                        1 - exactly one row should be returned <br>
     *                        0/N - any number of rows may be returned (no constraint); <br>
     *                        N - one or more rows must be returned, zero rows is not allowed.
     * @return true if the relationship was added successfully.
     * @throws FatalDBUtilLibException if an error occurs
     */
    public boolean addRelationship(String id, String sourceTableType, String targetTableType, String relationship,
                                   String constraint) throws FatalDBUtilLibException {
        sourceTableType = sourceTableType.trim();
        targetTableType = targetTableType.trim();

        // Check to see if parameters are valid.
        if ((sourceTableType == null) || (sourceTableType.length() == 0)) {
            DBDefines.ERROR_LOG.add("Schema.addRelationship received a null"
                    + " and/or length 0 sourceTableType where sourceTableType: " + sourceTableType
                    + ". Relationship could not be added." + " Returning false.");
            return false;
        }

        if (DBDefines.convertToUpperCase) {
            sourceTableType = sourceTableType.toUpperCase();
            targetTableType = targetTableType.toUpperCase();
            constraint = constraint.toUpperCase();

            // convert relationship (where clause) to uppercase but do not
            // convert any internal strings that are enclosed in single quotes.
            String[] parts = relationship.split("'");
            relationship = "";
            for (int i = 0; i < parts.length; i++) {
                if (i % 2 == 0)
                    relationship += parts[i].toUpperCase();
                else
                    relationship += "'" + parts[i] + "'";
            }
        }

        if (id == null || id.trim().length() == 0 || id.equals("?"))
            id = sourceTableType + "->" + targetTableType;

        Table sourceTable = tableNameToTable.get(tableTypeToName.get(sourceTableType));
        Table targetTable = tableNameToTable.get(tableTypeToName.get(targetTableType));

        // If no Table object corresponds to sourceTableName, then generate
        // an error since any processing involving sourceTable will now fail
        // since there's no object associated with it.
        if (sourceTable == null) {
            DBDefines.ERROR_LOG.add("Schema.addRelationship unable to add" + " relationship because sourceTableName "
                    + sourceTableType + " does not exist in the schema.");
            return false;
        }

        // If no Table object corresponds to targetTableName, then generate
        // an error since any processing involving targetTable will now fail
        // since there's no object associated with it.
        if (targetTable == null) {
            DBDefines.ERROR_LOG.add("Schema.addRelationship unable to add" + " relationship because targetTableName "
                    + targetTableType + " does not exist in the schema.");
            return false;
        }

        Relationship newRelationship = null;
        try {
            newRelationship = new Relationship(id, sourceTable, targetTable, relationship, constraint);
        } catch (FatalDBUtilLibException e) {
            String error = "Error in Schema.addRelationship when creating a new" + " Relationship with id: " + id
                    + " sourceTable: " + sourceTableType + " targetTable: " + targetTableType + " where clause: "
                    + relationship + " constraint: " + constraint + "\nError message: " + e.getMessage();
            throw new FatalDBUtilLibException(error);
        }
        return addRelationship(newRelationship);
    }

    /**
     * Add a single Relationship to the Schema.
     *
     * @param relation Relationship to be added to the Schema
     * @return whether or not relation was added successfully
     */
    private boolean addRelationship(Relationship relation) {
        // If this relationship is already in the schema, remove it.
        if (relation.relationship.equalsIgnoreCase("null"))
            tableGraph.get(relation.sourceTable).remove(relation.targetTable);

        // Get the LinkedList of Relationships for sourceTableName out
        // of TableGraph, and add a new Relationship between sourceTable
        // and targetTable to that list.
        tableGraph.get(relation.sourceTable).put(relation.targetTable, relation);

        String sourceTableType = relation.sourceTable.getTableType();
        String targetTableType = relation.targetTable.getTableType();

        if (this.sourceTableToRelationships.get(sourceTableType) == null)
            this.sourceTableToRelationships.put(sourceTableType, new ArrayList<Relationship>());
        if (this.targetTableToRelationships.get(targetTableType) == null)
            this.targetTableToRelationships.put(targetTableType, new ArrayList<Relationship>());

        this.sourceTableToRelationships.get(sourceTableType).remove(relation);
        this.targetTableToRelationships.get(targetTableType).remove(relation);

        this.sourceTableToRelationships.get(sourceTableType).add(relation);
        this.targetTableToRelationships.get(targetTableType).add(relation);

        return true;
    }

    /**
     * Make Schema aware of hints that exist for tables within the schema. Hints allow users to make decisions usually
     * reserved for the database optimizer.
     *
     * @param records a String of information that can be parsed to extract hints for tables within the Schema. records
     *                are expected to be of the form: <br>
     *                a_table_type hint for that #a_table_type# b_table_type hint for that #b_table_type# <br>
     *                A hint string is expected to have the table type that the hint is for occuring somewhere within the hint string
     *                enclosed in ##.
     * @return whether or not hints were added successfully
     */
    public boolean addHints(String records) {
        if (records == null || records.length() == 0)
            return true;

        // Whether or not hint information is handled successfully.
        boolean ok = true;

        // replace all tab characters with spaces.
        records = records.replaceAll("\t", " ");

        // Get the hint specified for each tabletype (one tabletype per line).
        String[] lines = DBDefines.splitOnNewLine(records);

        for (int i = 0; i < lines.length; i++) {
            // A line of hint information related to one table.
            String currLine = lines[i].trim();

            // find the first space
            int pos = currLine.indexOf(" ");

            // If there is a hint ... Check this since users might specify a
            // table with no hint
            if (pos > 0) {
                String tableType = currLine.substring(0, pos);
                if (DBDefines.convertToUpperCase)
                    tableType = tableType.toUpperCase();

                if (getTableOfType(tableType) == null) {
                    DBDefines.ERROR_LOG.add("Hint specified for table of type: " + tableType
                            + ", yet this table is not in the schema." + "  Hint is being ignored.");
                    ok = false;
                }
                // Add an entry to the hints HashMap from table type to hint
                tableTypeToHint.put(tableType, currLine.substring(pos + 1).trim());
            }
            if (currLine.length() == 0)
                break;
        }
        return ok;
    }

    /**
     * Specify the field names that should be combined to form the output representation of a Row object. This only
     * influences the appearance of a row in text output, it has no influence on code logic or data representations. The
     * component string can contain multiple lines of row name components - each line reperesents the row name
     * components for a given table type and should contain the type of the table followed by a space followed by the
     * field components separated by commas or underscores. For example: <br>
     * event evid, prefor, auth <br>
     * orid orid_evid_auth <br>
     * All event rows, when output as Strings, would have their evid, prefor, and auth information output. All origin
     * rows, when output as Strings, would have their orid, evid, and auth information output. The default for tables
     * whose rowNameComponents are not specified is to use the ownedID or idLinks (in alphabetical orider).
     *
     * @param components a string containing multiple lines separated by end of line characters. Each line should
     *                   contain the type of the table followed by white space followed by the field components separated by commas or
     *                   underscores. For example: <br>
     *                   event evid,prefor,auth <br>
     *                   orid orid_evid_auth
     */
    public void setRowNameComponents(String components) {
        if (components == null || components.length() == 0)
            return;

        // Get rid of tabs and replace all occurrences of two spaces with
        // space
        // components = components.replaceAll("\t", " ").replaceAll(" *", " ");
        components = DBDefines.removeExtraSpaces(components);

        if (DBDefines.convertToUpperCase)
            components = components.trim().toUpperCase();
        else
            components = components.trim();

        // Each array entry in lines contains row name components for a particular
        // table type
        String[] lines = DBDefines.splitOnNewLine(components);

        for (String line : lines) {
            String[] words = line.trim().split(" ");
            // words[0]: table type
            // words[1] - words[words.length-1]: components
            if (words.length > 1) {
                StringBuilder comps = new StringBuilder();
                for (int i = 1; i < words.length; i++)
                    comps.append(words[i]);
                rowNameComponents.put(words[0], comps.toString());
            }
        }
    }

    /**
     * Sets all the foreign keys on all the tables that are members of the schema. Foreign key definitions are derived
     * from the table definition tables - if a column's column_type is "foreign key" in the table definition table, then
     * that column is a foreign key.
     *
     * @param onDeleteCascade   if true, then when a record in the parent table is deleted, the record referenced through
     *                          the foreign key will also be deleted
     * @param onDeleteSetNull   if true, then when a record in the parent table is deleted, the foreign key field that
     *                          referred to that parent table record will be set to null
     * @param deferrable        if true, then foreign key checking can be deferrable - the validity of the foreign key
     *                          references will only be checked at a commit point
     * @param initiallyDeferred if ture and foreign key checking is deferrable, then the validity of foreign key
     *                          references will only be checked at a commit point (if this is true, then deferrable must also be true)
     * @param enabled           if true, then this foreign key is enabled and its validity will be checked
     * @param validate          if true, then the validity of the constraint will be checked and future operations that affect
     *                          columns involved in a foreign key constraint will be checked to ensure that they do not lead to a foreign key
     *                          violation
     * @param printSql          if true, the SQL generated to set the foreign keys will be output to the screen
     * @throws FatalDBUtilLibException if an error occurs
     */
    public void setForeignKeys(boolean onDeleteCascade, boolean onDeleteSetNull, boolean deferrable,
                               boolean initiallyDeferred, boolean enabled, boolean validate, boolean printSql)
            throws FatalDBUtilLibException {
        for (Table table : tableNameToTable.values())
            dao.setForeignKeys(table, onDeleteCascade, onDeleteSetNull, deferrable, initiallyDeferred, enabled,
                    validate, printSql);
    }

    /**
     * Turn the constraints for all the tables in this schema on (contraintsOn = true) or off (constraintsOn = false).
     *
     * @param constraintsOn if true, turn the constraints for all the tables in this schema on; if false, turn the
     *                      constraints for all the tables in this schema off
     * @return whether or not the changes to the constraints for the tables in this schema were successful
     */
    public boolean setTableConstraints(boolean constraintsOn) {
        boolean successful = true;
        for (Table table : getTables())
            successful = successful && table.setConstraints(constraintsOn);
        return successful;

    }

    /**
     * Set autoTableCreation to true or false. When true, tables specified in the schema that do not exist in the
     * database are created using table information in the table definition table. This parameter is false by default
     * but may be set in the par file with the name+"autoTableCreation" parameter. Has no effect after
     * {@link #completeSetup completeSetup()} has been called - either by the Schema constructor or manually.
     *
     * @param autoTableCreation true or false.
     */
    public void setAutoTableCreation(boolean autoTableCreation) {
        this.autoTableCreation = autoTableCreation;
    }

    /**
     * This method completes the setup of a Schema object. It must be called after the addition of tables and
     * relationships to the schema, and prior to use of the schema. This method is automatically called by the
     * constructor if there is table information given to the constructor.
     *
     * @throws FatalDBUtilLibException if any DBUtilLib exceptions are encountered.
     */
    public void completeSetup() throws FatalDBUtilLibException {
        // Specify the format of date type fields in all tables in this schema.
        setDateFormat(dateFormat);

        // Set up the row name components (columns used in textual row output)
        // for each Table.
        for (Table table : tableGraph.keySet())
            table.setRowNameComponents(rowNameComponents.get(table.tableType));

        // createConnection to the Data Access Object. For database dao, this
        // creates the jdbc connections. For flatfile and xml dao's, this call
        // reads in the data from the files into internal row graphs maintained by
        // the dao objects.
        dao.createConnection();

        // Run any sql commands the user would like to have executed within the
        // Schema's dao connection immediately after the connection to the dao
        // is established if the dao type is DB.
        if (dao.getType().equals(DBDefines.DATABASE_DAO) && initializationSQL.length() > 0) {
            String[] lines = DBDefines.splitOnNewLine(initializationSQL);

            for (String line : lines) {
                if (dao.executeUpdateStatement(line) < 0)
                    DBDefines.STATUS_LOG.add("InitializationSQL statement \"" + line
                            + "\" generated a SQL exception.\n");
                else
                    DBDefines.STATUS_LOG.add("InitializationSQL statement \"" + line + "\" executed successfully.\n");
            }
        }

        // Visit every relationship object in the schema. For each relationship,
        // set the idLink information and add the relationship to the
        // relationships variable.
        for (Table startTable : tableGraph.keySet()) {
            for (Relationship relation : tableGraph.get(startTable).values()) {
                relation.setIdLinks();
                relationships.add(relation);
            }
        }

        // Keep track of which tables are idowner tables and which table owns
        // which ownedid.
        for (Table table : tableGraph.keySet())
            if (table.ownedID != null)
                ownedIDToTable.put(table.ownedID, table);

        // Populate each table's foreignKeys array (int[] that keeps track of
        // column indices for the columns that are foreign keys).
        for (Table table : tableGraph.keySet())
            table.setIdLinks();

        // Set all the foreign keys in all the Table objects.
        updateForeignKeys();

        // If a remapTable name is specified, then the RemapTable constructor
        // will create a Table object that represents this RemapTable in the dao.
        // Otherwise, the RemapTable will just be used in memory.
        this.remapTable = new RemapTable(this, remapTableName);

        // If the user specifed the NextIdSequences parameter in the par file
        // for use in place of a table for retrieving next id information, then
        // extract the id->sequence relations and stuff them into a hash map.
        // Call the IdGapsTable constructor with the resulting hash map.
        if (nextIdSequences.length() > 0) {
            // nextIdSequences should have information in the form
            // column_name sequence_name
            // Example:
            // orid orid_sequence_name
            // evid evid_sequence_name
            HashMap<String, String> sequences = new HashMap<String, String>();
            String[] lines = DBDefines.splitOnNewLine(nextIdSequences);

            for (String line : lines) {
                // replace all tab characters with spaces and all instances of more
                // than one space with only one space
                // line = line.replaceAll("\t", " ").replaceAll(" *", " ");
                line = DBDefines.removeExtraSpaces(line);

                String[] lineSplit = line.split(" ");
                // lineSplit[0]: column name
                // lineSplit[1]: sequence name
                if (lineSplit[0].length() > 0 && lineSplit[1].length() > 0)
                    sequences.put(lineSplit[0], lineSplit[1]);
            }
            this.idGapsTable = new IDGapsTable(this, sequences);
        }
        // If idGapsTableName is the string 'null', then id's will not be
        // renumbered. ID's in a target schema will have the same id as the id
        // in the source schema. This may result in primary key violations.
        else if (idGapsTableName.equalsIgnoreCase("NULL"))
            this.idGapsTable = null;
            // Otherwise, create a IDGapsTable object with the specified idGapsTableName.
            // If no idGapsTableName is specified, new ids will be retrieved in a
            // max(id) + 1 fashion.
        else
            this.idGapsTable = new IDGapsTable(this, idGapsTableName);

        // remap source is the string that will be put in the 'source' column
        // of the remap table.
        setRemapSource(remapSource);

        if (truncateTables)
            truncateTables();

        // if autotable creation is on, create tables that do not exist
        if (autoTableCreation)
            createTables();

    }

    /**
     * This method sets all foreign keys in all the Table object in this Schema. These foreign keys are set in the Java
     * objects, not the Oracle database. This method is called in completeSetup(), but applications should call it again
     * if they modify any of the foreign keys in the Schema after the Schema has been constructed and completeSetup()
     * called.
     */
    public void updateForeignKeys() {
        // For each table, populate the fkFields int[] with the indeces of the
        // columns of the table that are idLinks. Also populate fkMap
        // which maps (String)idLink -> (LinkedList of Integers) indeces.
        for (Table table : tableGraph.keySet()) {
            int nfk = 0;
            // Count the number of idLinks and foreign keys in the columns
            // of this table.
            for (int i = 0; i < table.columns.length; i++)
                if (table.columns[i].foreignKey != null)
                    ++nfk;

            // Make an int[] big enough to hold their indeces.
            table.foreignKeys = new int[nfk];

            // Populate the fkFields and fkMap collections.
            nfk = 0;
            for (int i = 0; i < table.columns.length; i++)
                if (table.columns[i].foreignKey != null)
                    table.foreignKeys[nfk++] = i;
        }

        // Each key is a Table object.
        for (Table table : tableGraph.keySet()) {
            // For idowner tables, iterate over all the other tables and create
            // a HashMap of Tables that have the owned id as a foreign key ->
            // linked list of column names in that table that are foreign keys
            // linked to owned id.
            if (table.ownedID != null) {
                HashMap<Table, LinkedList<String>> linkedTables = new HashMap<Table, LinkedList<String>>();
                for (Table foreignTable : tableGraph.keySet())
                    if (foreignTable != table) {
                        LinkedList<String> connectedColumns = new LinkedList<String>();
                        for (int i = 0; i < foreignTable.columns.length; i++)
                            if (table.ownedID.equals(foreignTable.columns[i].foreignKey))
                                connectedColumns.add(foreignTable.columns[i].name);
                        linkedTables.put(foreignTable, connectedColumns);
                    }
                table.setLinkedTables(linkedTables);
            }
        }
    }

    /**
     * Visit every table in the Schema. If autoTableCreation is true (default is false) and the table does not exist, it
     * is created. If truncateTables is true (default is false), tables are truncated. If a table is to be truncated,
     * and promptBeforeTruncate is true (default is true), then user is prompted on the command line to make sure they
     * really do want the tables truncated.
     */
    public void truncateTables() {
        boolean okToTruncate = !promptBeforeTruncate;
        if (promptBeforeTruncate) {
            StringBuilder tableNames = new StringBuilder("");

            for (String tableName : tableNameToTable.keySet())
                if (dao.tableExists(tableName))
                    tableNames.append(tableName + DBDefines.EOLN);

            if (tableNames.length() == 0)
                System.out.println("None of the output tables exist.");
            else {
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
                if (dao.getType().equals("DB"))
                    System.out.println("\nWARNING:  The following database tables in account "
                            + ((DAODatabase) dao).getUserName() + " will be truncated.\n");
                else if (dao.getType().equals("FF"))
                    System.out.println("\nWARNING:  The following flatfile tables will be truncated.\n");
                else if (dao.getType().equals("XML"))
                    try {
                        System.out.println("\nWARNING:  The following tables will be truncated from XML file "
                                + ((DAOFlatFile) dao).getURI() + ".\n");
                    } catch (Exception ex) {
                    }
                System.out.println(tableNames.toString());
                System.out.print("\nAre you sure you want to truncate these tables? (y/n)  ");
                try {
                    String response = console.readLine();
                    okToTruncate = response.toLowerCase().startsWith("y");
                } catch (IOException e) {
                    System.out.println("ERROR in Schema.completeSetup().  "
                            + "Could not read console response to query.   Are "
                            + "you sure you want to truncate these tables? (y/n)\n" + e);
                }
            }
        }
        if (okToTruncate) {
            for (Table t : tableNameToTable.values()) {
                if (dao.truncateTable(t))
                    System.out.println("Table " + t.name + " truncated.");
                else if (dao.tableExists(t.name))
                    System.out.println("ERROR: Could not truncate table " + t.name);
            }
            remapTable.clear();
            dao.commit();
        } else
            System.out.println("No tables truncated.");
    }

    public void createTables() throws FatalDBUtilLibException {
        for (Table table : tableGraph.keySet()) {
            if (!dao.tableExists(table.name) && dao.createTable(table))
                System.out.println("Table " + table.name + " created.");
        }

        // create remap table if necessary
        if (remapTable.table != null && !dao.tableExists(remapTable.table.name))
            dao.createTable(remapTable.table);
    }

    /**
     * Close a Schema object. This method will release for garbage collection all data containers that this Schema owns.
     * It will also close all the database cursors owned by all the objects that are owned by this Schema object.
     */
    public void close() {
        this.schemaClosed = true;
        // close all the Table and Relationship objects owned by this schema. This will close
        // the preparedStatements, which will in turn release the database cursors.
        for (Relationship r : relationships)
            r.close();

        for (Table table : tableNameToTable.values())
            table.close();

        tableGraph.clear();
        this.sourceTableToRelationships.clear();
        this.targetTableToRelationships.clear();

        tableNameToTable.clear();
        tableTypeToName.clear();
        if (tableDefinitionTable != null)
            tableDefinitionTable.close();
        if (remapTable != null)
            remapTable.close();
        if (idGapsTable != null)
            idGapsTable.close();
        if (dao != null)
            dao.closeConnection();
    }

    /**
     * Get the DAO type for this schema (see DBDefines.java for acceptable DAO types; DB, FF, XML).
     *
     * @return DAO type (see DBDefines.java for acceptable DAO types)
     */
    public String getDAOType() {
        return this.dao.getType();
    }

    public String getRemapTableName() {
        if (this.remapTable != null && this.remapTable.table != null)
            return this.remapTable.table.name;
        return "";
    }

    public String getIDGapsTableName() {
        if (this.idGapsTable == null || this.idGapsTable.name == null)
            return "";
        return this.idGapsTable.name;
    }

    /**
     * Gets set to true each time a constraint violation is encountered during RowGraph construction.
     */
    protected boolean rowGraphConstraintViolation = false;

    /**
     * Return true if a constraint violation has occurred during row graph construction since the last time that
     * clearRowGraphConstraintViolation() was called.
     *
     * @return return true if a constraint violation has occurred during RowGraph construction since the last time that
     * clearRowGraphConstraintViolation() was called
     */
    public boolean getRowGraphConstraintViolation() {
        return rowGraphConstraintViolation;
    }

    /**
     * Clears all memory of any RowGraph constraint violations.
     */
    public void clearRowGraphConstraintViolation() {
        rowGraphConstraintViolation = false;
    }

    /**
     * Get a List of all the Table objects that are members of this schema.
     *
     * @return a Collection of Table objects.
     */
    public Collection<Table> getTables() {
        return this.tableGraph.keySet();
    }

    /**
     * Return a reference to the first Table object that was added to the schema
     *
     * @return a reference to the first Table object that was added to the schema.
     */
    public Table getTopLevelTable() {
        // This returns the first one entered since tableGraph is a LinkedHashMap.
        return tableGraph.keySet().iterator().next();
    }

    /**
     * Get a LinkedList of all the Relationship objects in the schema.
     *
     * @return a LinkedList of all the Relationship objects in the schema
     */
    public LinkedList<Relationship> getRelationships() {
        return relationships;
    }

    /**
     * Return this Schema's name.
     *
     * @return this Schema's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Return whether or not this schema should automatically create tables that do not exist at the time of schema
     * creation
     *
     * @return whether or not this schema should automatically create tables that do not exist at the time of schema
     * creation
     */
    public boolean getAutoTableCreation() {
        return this.autoTableCreation;
    }

    /**
     * Returns a specific Table object based on the Table's name. Returns null if the Table could not be found.
     *
     * @param tableName name of the Table to be returned
     * @return Table object with the name tableName; null if none exists
     */
    public Table getTable(String tableName) {
        if (tableName == null) {
            DBDefines.ERROR_LOG.add("Schema.getTable received a null tableName." + " Returning null.");
            return null;
        }
        if (tableNameToTable.size() <= 0) {
            DBDefines.ERROR_LOG.add("Error in Schema.getTable(" + tableName + "). There are no tables in this schema");
            return null;
        }
        return tableNameToTable.get(tableName.trim());
    }

    /**
     * Returns a specific Table object based on the Table's type. Returns null if the Table could not be found.
     *
     * @param tableType type of Table to be returned
     * @return Table object with type tableType; null if none exists
     */
    public Table getTableOfType(String tableType) {
        if (DBDefines.convertToUpperCase) {
            tableType = tableType.toUpperCase();
        }
        return tableNameToTable.get(tableTypeToName.get(tableType));
    }

    /**
     * Return the Relationship object that relates the specified start and end tables.
     *
     * @param startTable the start table.
     * @param endTable   the end table.
     * @return the Relationship object that relates the start and end tables.
     */
    public Relationship getRelationship(Table startTable, Table endTable) {
        if (startTable == null || endTable == null)
            return null;
        LinkedHashMap<Table, Relationship> rels = tableGraph.get(startTable);
        if (rels == null)
            return null;
        return rels.get(endTable);
    }

    /**
     * Return the Relationships in this schema that have a target table whose type equals targetTableType (empty list if
     * there are none)
     *
     * @param targetTableType the table type that is the target table in the returned relationships
     * @return the Relationships in this schema that have a target table whose type equals targetTableType (empty list
     * if there are none)
     */
    protected ArrayList<Relationship> getRelationshipsForTargetTable(String targetTableType) {
        ArrayList<Relationship> relationshipsForTargetTable = this.targetTableToRelationships.get(targetTableType);

        if (relationshipsForTargetTable == null)
            return new ArrayList<Relationship>();
        else
            return relationshipsForTargetTable;
    }

    /**
     * Return the Relationships in this schema that have a source table whose type equals sourceTableType (empty list if
     * there are none)
     *
     * @param sourceTableType the table type that is the source table in the returned relationships
     * @return the Relationships in this schema that have a source table whose type equals sourceTableType (empty list
     * if there are none)
     */
    protected ArrayList<Relationship> getRelationshipsForSourceTable(String sourceTableType) {
        ArrayList<Relationship> relationshipsForSourceTable = this.sourceTableToRelationships.get(sourceTableType);

        if (relationshipsForSourceTable == null)
            return new ArrayList<Relationship>();
        else
            return relationshipsForSourceTable;
    }

    /**
     * Return this Schema's TableDefinitionTable.
     *
     * @return this Schema's TableDefinitionTable
     */
    public TableDefinition getTableDefinition() {
        return this.tableDefinitionTable;
    }

    /**
     * Specify the format of all fields of type Date in all tables in this schema.
     *
     * @param dateFormat the date format string. See java.text.SimpleDateFormat API for valid format strings.
     */
    public void setDateFormat(String dateFormat) {
        if (dateFormat != null && dateFormat.length() > 0) {
            this.dateFormat = dateFormat;
            for (Table table : tableNameToTable.values())
                table.setDateFormat(dateFormat);
        }
    }

    /**
     * Set the Remap source (what the SOURCE column of the Remap table gets set to for an id in this Schema).
     *
     * @param source the remap source string.
     */
    public void setRemapSource(String source) {
        if (source != null && source.length() > 0) {
            for (String id : ownedIDToTable.keySet()) {
                Table table = ownedIDToTable.get(id);
                // for db tables, table.name could be account.tablename.
                // for files, table.name could include path information.
                // Want to strip off all but the table name or file name
                // (exclude account or path information).
                String tableName = table.name;
                int dot = tableName.indexOf('.');
                if (dao.getType().equals(DBDefines.DATABASE_DAO) && dot > -1)
                    tableName = tableName.substring(dot + 1);
                else
                    tableName = (new File(table.name)).getName();

                String src = source.replaceAll("#TABLE#", tableName).replaceAll("#ACCOUNT#", table.getDataSource());

                if (source.length() > 512) {
                    String msg = "ERROR in Schema.setRemapSource(" + source + ").  Source string " + source
                            + " is too long.  Being truncated to ";

                    source = source.substring(0, 512);

                    msg += source;

                    DBDefines.ERROR_LOG.add(msg);
                }

                ownedIDToRemapSource.put(table.ownedID, src);
            }
        }
    }

    public void setRemapSource(String id, String source) {
        ownedIDToRemapSource.put(id.toUpperCase(), source);
    }

    /**
     * Return the remap source (what the SOURCE column of the Remap table gets set to for an id in this Schema) for an
     * id.
     *
     * @param id the ownedID whose remap source is being requested.
     * @return the remap source that corresponds to ownedID.
     */
    public String getRemapSource(String id) {
        return ownedIDToRemapSource.get(id.toUpperCase());
    }

    /*******************************************************************************************************************
     * Format conversion functions
     ******************************************************************************************************************/
    /**
     * Get a String representation of the information in this Schema object, including column information for each table
     * in the schema.
     *
     * @return a string representation of the information represented in this schema object.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Schema: " + name + DBDefines.EOLN);
        s.append(DBDefines.EOLN);
        s.append(dao.toString());
        s.append(DBDefines.EOLN);
        s.append(tableDefinitionTable.toString());
        s.append(DBDefines.EOLN + "Tables:" + DBDefines.EOLN);
        for (Table table : tableNameToTable.values())
            s.append(table.toString() + DBDefines.EOLN);

        s
                .append("Foreign keys with *'s are ones that are involved in relationships." + DBDefines.EOLN
                        + DBDefines.EOLN);

        s.append("Relationships:" + DBDefines.EOLN);

        LinkedList<String> id = new LinkedList<String>();
        LinkedList<String> sourceTable = new LinkedList<String>();
        LinkedList<String> targetTable = new LinkedList<String>();
        LinkedList<String> select = new LinkedList<String>();
        LinkedList<String> constraint = new LinkedList<String>();

        for (Table table : tableNameToTable.values()) {
            LinkedHashMap<Table, Relationship> rels = tableGraph.get(table);
            if (rels != null)
                for (Relationship relationship : rels.values()) {
                    id.add(relationship.id);
                    sourceTable.add(relationship.sourceTable.name);
                    targetTable.add(relationship.targetTable.name);
                    select.add(relationship.relationship);
                    constraint.add(relationship.constraint);
                }
        }

        String[] relations = new String[id.size()];
        int j = 0;
        for (String i : id)
            relations[j++] = i;
        DBDefines.evenLength(relations);

        j = 0;
        for (String st : sourceTable)
            relations[j++] += "  " + st;
        DBDefines.evenLength(relations);

        j = 0;
        for (String st : targetTable)
            relations[j++] += " -> SELECT * FROM " + st;

        j = 0;
        for (String sel : select)
            relations[j++] += " " + sel;

        j = 0;
        for (String c : constraint)
            relations[j++] += "   " + c;

        for (j = 0; j < relations.length; j++)
            s.append(relations[j] + DBDefines.EOLN);

        s.append(DBDefines.EOLN);
        return s.toString();
    }

    /**
     * Create a jaxb version of this Schema.
     *
     * @return jaxb version of this Schema
     * @throws JAXBException if an error occurs during the jaxb DAO object construction
     */
    public gov.sandia.gnem.dbutillib.jaxb.Schema toJaxb() throws JAXBException {
        return toJaxb(true);
    }

    /**
     * Create a jaxb version of this Schema.
     *
     * @param includeColumnInfo whether or not column information for the tables in this schema should be written out to
     *                          the xml (true) or not (false)
     * @return jaxb version of this Schema
     * @throws JAXBException if an error occurs during the jaxb DAO object construction
     */
    public gov.sandia.gnem.dbutillib.jaxb.Schema toJaxb(boolean includeColumnInfo) throws JAXBException {
        // Create "blank slate" jaxb schema.
        gov.sandia.gnem.dbutillib.jaxb.Schema jaxbSchema = new ObjectFactory().createSchema();

        // Name
        jaxbSchema.setName(this.name);

        // Dao
        jaxbSchema.setDAO(this.dao.toJaxb());

        // Table Definition
        jaxbSchema.setTableDefinition(this.tableDefinitionTable.toJaxb());

        // Tables
        List<gov.sandia.gnem.dbutillib.jaxb.Table> tables = jaxbSchema.getTable();
        for (Table table : this.tableNameToTable.values())
            tables.add(table.toJaxb(includeColumnInfo));

        // Relationship
        List<gov.sandia.gnem.dbutillib.jaxb.Relationship> rels = jaxbSchema.getRelationship();
        for (Relationship relationship : this.relationships)
            rels.add(relationship.toJaxb());

        return jaxbSchema;
    }

    /**
     * Returns a DBUtilLib Schema constructed from a Jaxb Schema.
     *
     * @param jaxbSchema Jaxb Schema used to construct a DBUtilLib Schema
     * @return a DBUtilLib Schema constructed from jaxbSchema
     * @throws FatalDBUtilLibException if an error occurs during the DAO object construction
     */
    public static Schema fromJaxb(gov.sandia.gnem.dbutillib.jaxb.Schema jaxbSchema) throws FatalDBUtilLibException {
        return fromJaxb(jaxbSchema, false);
    }

    /**
     * Returns a DBUtilLib Schema constructed from a Jaxb Schema.
     *
     * @param jaxbSchema Jaxb Schema used to construct a DBUtilLib Schema
     * @param daoInclude whether or not to use the dao information from jaxbSchema (true) or not (false)
     * @return a DBUtilLib Schema constructed from jaxbSchema
     * @throws FatalDBUtilLibException if an error occurs during the DAO object construction
     */
    public static Schema fromJaxb(gov.sandia.gnem.dbutillib.jaxb.Schema jaxbSchema, boolean daoInclude)
            throws FatalDBUtilLibException {
        // Keep track of whether or not we are going to need to get default
        // information or whether it's all included in the xml file.
        boolean needDefaults = false;

        // See if there is dao information in the xml or if we need to use defaults.
        gov.sandia.gnem.dbutillib.jaxb.DAO jaxbDao = jaxbSchema.getDAO();
        if (jaxbDao.getType() == null)
            needDefaults = true;

        // Table Definition Table - this is optional, so handle the case where
        // it may not have been specified in the xml and we need to use defaults.
        gov.sandia.gnem.dbutillib.jaxb.TableDefinition jaxbTableDef = jaxbSchema.getTableDefinition();
        if (jaxbTableDef == null || jaxbTableDef.getDAO().getType() == null)
            needDefaults = true;

        // Create the schema
        Schema schema = null;

        // Create the schema using default environment information.
        if (needDefaults)
            schema = new Schema();
            // Create the schema using dao/table definition information from the xml.
        else {
            schema = new Schema(null);
            schema.dao = DAO.fromJaxb(jaxbSchema.getDAO(), daoInclude, schema);
            schema.tableDefinitionTable = TableDefinition.fromJaxb(jaxbSchema.getTableDefinition(), daoInclude);
        }
        // Name
        schema.name = jaxbSchema.getName();

        // Add Tables
        List<gov.sandia.gnem.dbutillib.jaxb.Table> jaxbTables = jaxbSchema.getTable();
        for (gov.sandia.gnem.dbutillib.jaxb.Table jaxbTable : jaxbTables)
            schema.addTable(Table.fromJaxb(jaxbTable, schema));

        // Add Relationships
        List<gov.sandia.gnem.dbutillib.jaxb.Relationship> jaxbRelationships = jaxbSchema.getRelationship();
        for (gov.sandia.gnem.dbutillib.jaxb.Relationship jaxbRelationship : jaxbRelationships)
            schema.addRelationship(Relationship.fromJaxb(jaxbRelationship, schema));

        schema.completeSetup();
        return schema;
    }

    public Table getOwnedIDTable(String id) {
        return this.ownedIDToTable.get(id);
    }

    public String getHint(String tableType) {
        String hint = this.tableTypeToHint.get(tableType);
        if (hint == null)
            return null;

        Table table = getTable(tableType);
        // Hints are required to have table types in them surrounded by ##. This split extracts the table name out of
        // the hint
        String[] parts = hint.split("#");
        // Surround the hint with /*+ */ which is notation that Oracle needs.
        if (parts.length == 3 && parts[1].equalsIgnoreCase(tableType))
            return "/*+ " + parts[0] + table.getName() + parts[2] + " */ ";

        return null;
    }

    public boolean getIncludeXMLDAO() {
        return includeXMLDAO;
    }

    public String getLddateOption() {
        return lddateOption;
    }

    public IDGapsTable getIdGapsTable() {
        return idGapsTable;
    }

    public RemapTable getRemapTable() {
        return remapTable;
    }

    public DAO getDAO() {
        return dao;
    }

    public void setDAO(DAO dao) throws FatalDBUtilLibException {
        this.dao = dao;
        completeSetup();
    }

    /**
     * Returns a comma separated list of the names of the tables that belong to this schema but which do not exist in
     * the dao.
     *
     * @return comma separated list of the names of the tables that belong to this schema but which do not exist in the
     * dao
     */
    public String tablesThatDontExist() {
        StringBuilder missing = new StringBuilder();
        for (Table table : getTables()) {
            if (!dao.tableExists(table))
                missing.append(",").append(table.name);
        }
        if (missing.length() > 0)
            missing.deleteCharAt(0);
        return missing.toString();
    }

    /**
     * Return table information for tables to be used by this Schema from the {@link ParInfo#TABLES ParInfo.TABLES}
     * parameter. If parInfo contains the {@link ParInfo#USE_TABLE_TYPES ParInfo.USE_TABLE_TYPES} parameter, only return
     * tables that are represented in that parameter.
     *
     * @param parInfo ParInfo object to extract this information from
     * @return the table information for tables to be used by this Schema from the {@link ParInfo#TABLES ParInfo.TABLES}
     * if those tables types are represented in {@link ParInfo#USE_TABLE_TYPES ParInfo.USE_TABLE_TYPES} or if
     * {@link ParInfo#USE_TABLE_TYPES ParInfo.USE_TABLE_TYPES} is not present
     */
    private String tablesToUse(ParInfo parInfo) {
        String tableInfo = parInfo.getItem(this.name + ParInfo.TABLES, "");
        // If this table info was created from a gui, it's possible that some tables had their "Use" checkbox
        // unchecked. Don't incorporate these tables into the processing.
        String useTableTypes = parInfo.getItem(this.name + ParInfo.USE_TABLE_TYPES, "");
        if (tableInfo.length() == 0 || useTableTypes.length() == 0)
            return tableInfo;

        String[] tablesToUseArray = useTableTypes.split(",");

        // Get each line of table information
        tableInfo = DBDefines.removeExtraSpaces(tableInfo);
        String[] tablesArray = DBDefines.splitOnNewLine(tableInfo);

        StringBuilder tablesToUse = new StringBuilder();
        for (String tableType : tablesToUseArray) {
            for (String table : tablesArray) {
                // if (table.split(" ")[1].equals(tableType))
                String[] tableSplit = ParInfo.handleParametersWithSpaces(table);
                if (tableSplit[1].equals(tableType))
                    tablesToUse.append(table + DBDefines.EOLN);
            }
        }
        return tablesToUse.toString();
    }

    /**
     * Return the idgaps table named to be used by this Schema from the {@link ParInfo#IDGAPS_TABLE
     * ParInfo.IDGAPS_TABLE} parameter. If parInfo contains the {@link ParInfo#IDGAPS_TABLE_USE_TABLE
     * ParInfo.IDGAPS_TABLE_USE_TABLE} parameter, only return the idgaps table name in {@link ParInfo#IDGAPS_TABLE
     * ParInfo.IDGAPS_TABLE} if {@link ParInfo#IDGAPS_TABLE_USE_TABLE ParInfo.IDGAPS_TABLE_USE_TABLE} is set to "true"
     *
     * @param parInfo ParInfo object to extract this parameter information from
     * @return the idName in the {@link ParInfo#IDGAPS_TABLE ParInfo.IDGAPS_TABLE} if
     * {@link ParInfo#IDGAPS_TABLE_USE_TABLE ParInfo.IDGAPS_TABLE_USE_TABLE} is set to "true" or is not present; ""
     * otherwise
     */
    private String idGapsTableToUse(ParInfo parInfo) {
        String idGapsTableName = parInfo.getItem(this.name + ParInfo.IDGAPS_TABLE, "");

        String useIdGapsTable = parInfo.getItem(this.name + ParInfo.IDGAPS_TABLE_USE_TABLE);

        if (useIdGapsTable == null || useIdGapsTable.toLowerCase().equals("true"))
            return idGapsTableName;
        else
            return "";
    }

    /**
     * Return the remap table named to be used by this Schema from the {@link ParInfo#REMAP_TABLE ParInfo.REMAP_TABLE}
     * parameter. If parInfo contains the {@link ParInfo#REMAP_TABLE_USE_TABLE ParInfo.REMAP_TABLE_USE_TABLE} parameter,
     * only return the remap table name in {@link ParInfo#REMAP_TABLE ParInfo.REMAP_TABLE} if
     * {@link ParInfo#REMAP_TABLE_USE_TABLE ParInfo.REMAP_TABLE_USE_TABLE} is set to "true"
     *
     * @param parInfo ParInfo object to extract this parameter information from
     * @return the idName in the {@link ParInfo#REMAP_TABLE ParInfo.REMAP_TABLE} if
     * {@link ParInfo#REMAP_TABLE_USE_TABLE ParInfo.REMAP_TABLE_USE_TABLE} is set to "true" or is not present; ""
     * otherwise
     */
    private String remapTableToUse(ParInfo parInfo) {
        String remapTableName = parInfo.getItem(this.name + ParInfo.REMAP_TABLE, "");

        String useRemapTable = parInfo.getItem(this.name + ParInfo.REMAP_TABLE_USE_TABLE);

        if (useRemapTable == null || useRemapTable.toLowerCase().equals("true"))
            return remapTableName;
        else
            return "";
    }
}
