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
package gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * Schema manages a database Connection and a Map of tableTypes -> tableNames.
 *
 * @author sballar
 */
public class Schema {

    private boolean outputSchema;

    private Connection dbConnection;

    private String schemaName;
    private String wallet;
    private String instance;
    private String userName;
    private String password;
    private String driver;

    /**
     * Map from table type to table name, eg., "Site" -> "gnem_idcstatic.site". Capitalization of
     * table types must conform to the capitalization of the Java class that backs the table types.
     */
    private Map<String, String> tableMap;

    private StringBuffer log = new StringBuffer();

    public static final String defaultPackageNames =
	    "gov.sandia.gnem.dbtabledefs.nnsa_kb_core," + "gov.sandia.gnem.dbtabledefs.nnsa_kb_custom,"
		    + "gov.sandia.gnem.dbtabledefs.usndc_p3," + "gov.sandia.gnem.dbtabledefs.css30,"
		    + "gov.sandia.gnem.dbtabledefs.gmp," + "gov.sandia.gnem.dbtabledefs.ipf";

    private int fetchSize = 1000;

    /**
     * Constructs a new Schema object with all default values. A db Connection is instantiated with
     * the default instance (jdbc:oracle:thin:@oeldb17.sandia.gov:1526:dwdv2),
     * userName="gnem_"+System.getProperty("user.name") and password= System.getenv(userName).
     * Autocommit is turned off. The map of tableNames is empty. OutputSchema is false, meaning that
     * tables can be neither truncated nor autocreated.
     *
     * @throws Exception
     */
    public Schema() throws Exception { this(new HashMap<String, String>()); }

    /**
     * Constructs a new input Schema object with all default values that supports the database tables
     * specified tableNames.
     *
     * @param tableNames a map from a table type to an actual table name, eg., Origin -> my_origin
     * @throws Exception
     */
    public Schema(Map<String, String> tableNames) throws Exception {

	String userName, instance = null, password = null, driver, wallet = null;
	Map<String, String> defaults = parseDefaults();

	userName = defaults.get("DB_USERNAME");
	if (userName == null) 
	    userName = System.getenv("DB_USERNAME");

	instance = defaults.get("DB_INSTANCE");
	if (instance == null) {
	    instance = System.getenv("DB_INSTANCE");
	    // if default instance not found, search for default wallet
	    if (instance == null) {
		wallet = defaults.get("DB_WALLET");
		if (wallet == null) 
		    wallet = System.getenv("DB_WALLET");
		if (wallet == null) 
		    wallet = System.getenv("TNS_ADMIN");
	    }
	}

	if (instance != null) {
	    // if an instance was discovered, get the password. No password needed for wallets
	    password = defaults.get("DB_PASSWORD_"+userName.toUpperCase());
	    if (password == null) password = System.getenv("DB_PASSWORD_"+userName.toUpperCase());
	}

	driver = defaults.get("DB_DRIVER");
	if (driver == null) 
	    driver = System.getenv("DB_DRIVER");
	if (driver == null) 
	    driver = "oracle.jdbc.driver.OracleDriver";

	initialize("default", wallet, instance, userName, password, driver, 
		tableNames, false, false, true, false, null);
    }

    /**
     * Schema sets up a database connection and manages a set of database tables based on properties
     * in Properties file. The Connection created by this constructor has autocommit turned off.
     *
     * @param prefix       the prefix for property keys. Eg. if prefix is specified to be 'dbInput' then the
     *                     property key that determines the UserName will be 'dbInputUserName'.
     * @param properties   Expected properties (all preceded by prefix):
     *                     <ul>
     *                     <li>(prefix)Instance - defaults to "jdbc:oracle:thin:@oeldb17.sandia.gov:1526:dwdv2"
     *                     <li>(prefix)UserName - defaults to "gnem_"+System.getProperty("user.name")
     *                     <li>(prefix)Password - defaults to System.getenv(userName)
     *                     <li>(prefix)Driver - defaults to "oracle.jdbc.driver.OracleDriver"
     *                     <li>(prefix)TableTypes - eg., 'Origin, Assoc, Arrival, Site'. Capitalization must
     *                     conform to the capitalization of the Java class that backs the table types.
     *                     <li>(prefix)TablePrefix - eg., 'my_' will generate my_origin, my_assoc, my_arrival and
     *                     my_site. Default is "".
     *                     <li>(prefix)TableSuffix - eg., '_table' will generate origin_table, assoc_table,
     *                     arrival_table and site_table. Default is "". TablePrefix and TableSuffix can be used
     *                     together.
     *                     <li>(prefix)(tableType)Table - will override specifications above for specific table
     *                     types. Eg., '(prefix)SiteTable = global_site' will override previously specified site
     *                     table with 'global_site'.
     *                     <li>(prefix)TruncateTables - if true and outputSchema is also true, then all existing
     *                     tables specified above will be truncated. If outputSchema is false, this property is
     *                     ignored.
     *                     <li>(prefix)PromptBeforeTruncate - if true, user is prompted before output tables are
     *                     truncated, otherwise truncation simply proceeds. Default is true.
     *                     <li>(prefix)AutoTableCreation - if true and outputSchema is also true, then any tables
     *                     specified above that do not exist in the database will be created, provided the correct
     *                     java classname can be deduced. If outputSchema is false, this property is ignored.
     *                     <li>(prefix)PackgeNames - comma-separated list of java packages that will be searched
     *                     looking for a Class that corresponds to an output tableType. Used when a request to
     *                     create a new database table is being processed. Listed packages are searched in order,
     *                     until a matching java Class is discovered that implements method createTable(Connection
     *                     connection, String tableName). Default is:
     *                     <ol>
     *                     <li>gov.sandia.gnem.dbtabledefs.nnsa_kb_core
     *                     <li>gov.sandia.gnem.dbtabledefs.nnsa_kb_custom
     *                     <li>gov.sandia.gnem.dbtabledefs.usndc_p3
     *                     <li>gov.sandia.gnem.dbtabledefs.css30
     *                     <li>gov.sandia.gnem.dbtabledefs.gmp
     *                     <li>gov.sandia.gnem.dbtabledefs.ipf
     *                     </ol>
     *                     </ul>
     * @param outputSchema if true, then properties (prefix)TruncateTables and
     *                     (prefix)AutoTableCreation are allowed to take effect, otherwise those properties are
     *                     ignored
     * @throws Exception
     */
    public Schema(String prefix, PropertiesPlus properties, boolean outputSchema) throws Exception {
	LinkedHashMap<String, String> tableNames = new LinkedHashMap<String, String>();

	String userName, instance = null, password = null, driver, wallet = null;

	Map<String, String> defaults = parseDefaults();

	userName = properties.getProperty(prefix + "UserName");
	if (userName == null) userName = defaults.get("DB_USERNAME");
	if (userName == null) userName = System.getenv("DB_USERNAME");

	if (userName == null || userName.trim().length() == 0)
	    throw new Exception(String.format("%nUserName must be specified in one of:%n"
		    + "  - properties file with property %sUserName)%n"
		    + "  - file database.properties in user's root directory with property DB_USERNAME%n"
		    + "  - user's environment with variable DB_USERNAME%n", prefix));

	wallet = properties.getProperty(prefix + "Wallet");
	instance = properties.getProperty(prefix + "Instance");

	// if both wallet and instance specified in properties file, throw exception
	if (wallet != null && instance != null)
	    throw new Exception(String.format("Cannot specify both %sWallet and %sInstance in the properties file.",
		    prefix, prefix));

	// if neither instance nor wallet were specified in properties file, then look for defaults.
	if (wallet == null && instance == null) {
	    instance = defaults.get("DB_INSTANCE");
	    if (instance == null) {
		instance = System.getenv("DB_INSTANCE");
		// if default instance not found, search for default wallet
		if (instance == null) {
		    wallet = defaults.get("DB_WALLET");
		    if (wallet == null) 
			wallet = System.getenv("DB_WALLET");
		    if (wallet == null) 
			wallet = System.getenv("TNS_ADMIN");
		}
	    }
	}

	if (instance != null) {
	    // if an instance was discovered, get the password. No password needed for wallets
	    password = properties.getProperty(prefix + "Password");
	    if (password == null) password = defaults.get("DB_PASSWORD_"+userName.toUpperCase());
	    if (password == null) password = System.getenv("DB_PASSWORD_"+userName.toUpperCase());
	}

	driver = properties.getProperty(prefix + "Driver");
	if (driver == null) driver = defaults.get("DB_DRIVER");
	if (driver == null) driver = System.getenv("DB_DRIVER");
	if (driver == null) driver = "oracle.jdbc.driver.OracleDriver";

	setFetchSize(properties.getInt(prefix + "FetchSize", 1000));

	// parse TableTypes and TablePrefix to extract tableNames.
	// eg., if TableTypes = "origin, assoc, arrival" and
	// TablePrefix = "myname_", then the following entries will be
	// put into the tables map: origin->myname_origin, assoc->myname_assoc, etc.
	String tableTypes = properties.getProperty(prefix + "TableTypes");
	String tablePrefix = properties.getProperty(prefix + "TablePrefix", "");
	String tableSuffix = properties.getProperty(prefix + "TableSuffix", "");
	if (tableTypes != null) {
	    tableTypes = tableTypes.replaceAll(",", " ");
	    while (tableTypes.contains("  "))
		tableTypes = tableTypes.replaceAll("  ", " ");

	    for (String type : tableTypes.split(" ")) {
		type = type.trim();
		if (type.length() > 0)
		    tableNames.put(type, tablePrefix + type + tableSuffix);
	    }
	}

	// parse explicit table name definitions of the form prefix<type>Table,
	// eg., dbInputOriginTable = myname_origin. For example, a property definition
	// such as dbInputOriginTable=myname_origin will result in table entry origin->myname_origin.
	for (Object key : properties.keySet()) {
	    String property = (String) key;

	    // tables of type 'TableDefinitionTable' are not supported by Schema! They are simply ignored.
	    if (property.equalsIgnoreCase(prefix + "TableDefinitionTable"))
		continue;

	    if (property.startsWith(prefix) && property.endsWith("Table")) {
		String type = property.substring(prefix.length(), property.length() - 5);
		tableNames.put(type, properties.getProperty(property));
	    }
	}

	// extract 3 booleans (truncateTables, promptBeforeTruncate and autoTableCreation) and a
	// List<String> of java package names.
	boolean truncateTables =
		outputSchema && properties.getBoolean(prefix + "TruncateTables", false);
	boolean promptBeforeTruncate =
		outputSchema && properties.getBoolean(prefix + "PromptBeforeTruncate", true);
	boolean autoTableCreation = properties.getBoolean(prefix + "AutoTableCreation", false);
	String packageNamesString =
		properties.getProperty(prefix + "PackageNames", defaultPackageNames);

	String addPackage = properties.getProperty(prefix + "PackageNamesAdd");
	if (addPackage != null)
	    packageNamesString = addPackage + ", " + packageNamesString;

	List<String> packageNames = Arrays.asList(packageNamesString.replaceAll(" ", "").split(","));


	initialize(prefix, wallet, instance, userName, password, driver, tableNames, outputSchema, truncateTables,
		promptBeforeTruncate, autoTableCreation, packageNames);
    }

    /**
     * Initialization of all attributes managed by this Schema. The Connection created by this Schema
     * has autoCommit turned off.
     *
     * @param schemaName           a name for this schema.
     * @param wallet               if !null then instance, userName and password are ignored
     * @param instance             if null defaults to value from user.home/database.properties
     * @param userName             if null defaults to value from user.home/database.properties
     * @param password             if null defaults to value from user.home/database.properties
     * @param driver               if null defaults to oracle.jdbc.driver.OracleDriver
     * @param tableNames           map from table type to table name, eg., "Site" -> "idcstatic.site".
     *                             Capitalization of table types should conform to the capitalization of the Java class
     *                             that backs the table types, but some variations are supported.
     * @param outputSchema         if false, tables will not be truncated or autocreated (none of the
     *                             following parameters will have any effect).
     * @param truncateTables       if true and outputSchema is also true then all tables specified in the
     *                             current tables map will truncated, if they exist.
     * @param promptBeforeTruncate if true and outputSchema is also true, user is prompted before
     *                             output tables are truncated, otherwise truncation simply proceeds.
     * @param autoTableCreation    if true and outputSchema is also true then any tables specified in the
     *                             current tables map that do not exist in the database will be created, provided the
     *                             correct java classname can be deduced.
     * @param packageNames         comma-separated list of java packages that will be searched looking for a
     *                             Class that corresponds to an output tableType. Used when a request to create a new
     *                             database table is being processed. Listed packages are searched in order, until a
     *                             matching java Class is discovered that implements method createTable(Connection
     *                             connection, String tableName)
     * @throws Exception
     */
    private void initialize(String _schemaName, String _wallet, String _instance, String _userName, String _password,
	    String _driver, Map<String, String> _tableNames, boolean outputSchema, boolean truncateTables,
	    boolean promptBeforeTruncate, boolean autoTableCreation, List<String> packageNames)
		    throws Exception {
	this.schemaName = _schemaName;

	this.wallet = _wallet;
	this.instance = _instance;
	this.userName = _userName;
	this.password = _password;
	this.driver = _driver;
	this.tableMap = _tableNames;
	this.outputSchema = outputSchema;

	log.append(String.format("Schema:   %s%n", this.schemaName));
	log.append(String.format("Type:     %s%n", this.outputSchema ? "output" : "input"));

	log.append(String.format("UserName: %s%n", this.userName));
	if (this.instance != null) log.append(String.format("Instance: %s%n", this.instance));
	if (this.wallet != null) log.append(String.format("Wallet:   %s%n", this.wallet));

	log.append(String.format("Driver:   %s%n", this.driver));
	if (this.outputSchema) {
	    log.append(String.format("AutoTableCreation:    %b%n", autoTableCreation));
	    log.append(String.format("TruncateTables:       %b%n", truncateTables));
	    log.append(String.format("PromptBeforeTruncate: %b%n", promptBeforeTruncate));
	    log.append(String.format("PackageNames: %s%n", Arrays.toString(packageNames.toArray())));
	} else
	    log.append(String.format("Fetch size: %d%n", this.fetchSize));

	if (tableMap.size() > 0) {
	    log.append(String.format("Tables:%n"));
	    for (Entry<String, String> entry : tableMap.entrySet())
		log.append(String.format("    %-12s %s%n", entry.getKey(), entry.getValue()));
	}

	if (instance == null && wallet == null)
	    throw new IOException(String.format("%n%s%nSearch for one of 'instance' or 'wallet' unsuccessful in properties file, user's environment and user's database.properties file.%n",
		    log.toString()));

	try {
	    Class.forName(this.driver);
	} catch (Exception e1) {
	    throw new SQLException(String.format("%n%s%nSpecification of database driver failed.%n",
		    log.toString()));
	}

	if (this.wallet == null)
	{
	    try {
		dbConnection = DriverManager.getConnection(instance, userName, password);
	    } catch (SQLException e) {
		throw new SQLException(String.format("%s%n"
			+ "SchemaName=%s%n"
			+ "Instance=%s%n"
			+ "UserName=%s%n"
			+ "Password=%s%n",
			e.getMessage(), schemaName, instance, userName, "********"));
	    }
	}
	else
	{
	    System.setProperty("oracle.net.wallet_location", wallet); // oracle_wallet_location
	    System.setProperty("oracle.net.tns_admin", wallet); // tns_entry_location

	    // when using wallets, userName is often preceded by a jdbc string. if omitted by user, add it.
	    // (prefix added to local copy of userName, not class variable).
	    if (!_userName.startsWith("jdbc:oracle"))
		_userName = "jdbc:oracle:oci:@"+userName;

	    try {
		dbConnection = DriverManager.getConnection(_userName);
	    } catch (Exception e) {
		throw new Exception(String.format("%n%s%n"
			+ "schemaname=%s%n"
			+ "username=%s%n"
			+ "wallet=%s%n",
			e.getMessage(), schemaName, _userName, wallet));
	    }
	    catch (java.lang.UnsatisfiedLinkError e)
	    {
		throw new Exception(String.format("%n%s%n"
			+ "schemaname=%s%n"
			+ "username=%s%n"
			+ "wallet=%s%n"
			+ "%nLikely cause of this error is something wrong with username%n"
			+ "When using wallets, username generally needs to be preceded by a string such as 'jdbc:oracle:oci:@' or 'jdbc:oracle:thin:/@'%n",
			e.getMessage(), schemaName, _userName, wallet));
	    }
	}
	dbConnection.setAutoCommit(false);

	if (!this.outputSchema)
	{
	    // ensure that all input tables actually exist in the db
	    for (String tableName : tableMap.values())
		if (!doesTableExist(tableName))
		    throw new Exception(String.format("Table %s does not exist.%n"
			    + "%s%n",
			    tableName, log.toString()));
	}

	// only perform the following tasks if this is an output schema
	if (this.outputSchema && (truncateTables || autoTableCreation)) {
	    LinkedHashSet<String> existingTables = new LinkedHashSet<String>();

	    // figure out which of the output tables already exist in the db.
	    for (String tableName : tableMap.values())
		if (doesTableExist(tableName))
		    existingTables.add(tableName);

	    // if user requested that output tables be truncated, do it.
	    if (existingTables.size() > 0 && truncateTables) {
		if (promptBeforeTruncate) {
		    System.out.println("WARNING: THE FOLLOWING TABLES ARE ABOUT TO BE TRUNCATED:");
		    for (String table : existingTables)
			System.out.print(table + " ");
		    System.out.println();

		    System.out.print("do you want to proceed? (y/n) ");
		    Scanner in = new Scanner(System.in);
		    if (!in.nextLine().equalsIgnoreCase("y")) {
			System.out.println("Bye");
			System.exit(0);
		    }
		    in.close();
		}

		for (String table : existingTables) {
		    truncateTable(table);
		    log.append(String.format("%-20s truncated.%n", table));
		}
	    }

	    // if user requested that output tables be created if they don't exist, do it.
	    if (autoTableCreation) {
		for (Entry<String, String> entry : tableMap.entrySet()) {
		    String tableName = entry.getValue();

		    if (!existingTables.contains(tableName)) {
			// get the tableType with first letter in upper case
			String tableType = capitalize(entry.getKey());

			for (String packageName : packageNames) {
			    try {
				Class<?> c = Class.forName(String.format("%s.%s", packageName, tableType));

				// a fatal exception in here could be due to a table name that is too long.

				// Note that each class (Arrival, Origin, etc) also has a method to return
				// a script to create the table.
				Method createTableMethod = c.getDeclaredMethod("createTable",
					Class.forName("java.sql.Connection"), Class.forName("java.lang.String"));
				createTableMethod.invoke(null, getConnection(), tableName.toUpperCase());

				log.append(String.format("%-20s created.%n", tableName));
				break;
			    } catch (java.lang.ClassNotFoundException ex) {
			    }
			}
		    }
		}
	    }
	}

    }

    /**
     * Retrieve a reference the database connection instantiated by this Schema.
     *
     * @return a reference the database connection instantiated by this Schema.
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
	return dbConnection;
    }

    /**
     * Retrieve a new Statement object from the Connection managed by this Schema. It is the
     * responsibility of the caller to close the Statement when done with it.
     *
     * @return
     * @throws SQLException
     */
    public Statement getStatement() throws SQLException {
	Statement statement = dbConnection.createStatement();
	statement.setFetchSize(fetchSize);
	return statement;
    }

    /**
     * Retrieve the Map<String, String> from tableType to tableName managed by this Schema.
     * <p>
     * This map contains entries like "Site" -> "idcstatic.site". Capitalization of table types must
     * conform to the capitalization of the Java class that backs the table types.
     *
     * @return
     */
    public Map<String, String> getTableNames() {
	return tableMap;
    }

    /**
     * Return the name of the table with the specified type. This method will try to find a table name
     * using the tableType as-is, tableType.toLowerCase(), tableType.toUpperCase(), and tableType with
     * the first letter capitalized.
     *
     * @param tableType
     * @return
     */
    public String getTableName(String tableType) {
	String tableName = tableMap.get(tableType);
	if (tableName != null)
	    return tableName;

	tableName = tableMap.get(tableType.toLowerCase());
	if (tableName != null)
	    return tableName;

	tableName = tableMap.get(tableType.toUpperCase());
	if (tableName != null)
	    return tableName;

	tableName = tableMap.get(capitalize(tableType));
	return tableName;
    }

    /**
     * Return true if the specified type is supported. This method will try to find a table name using
     * the tableType as-is, tableType.toLowerCase(), tableType.toUpperCase(), and tableType with the
     * first letter capitalized.
     *
     * @param tableType
     * @return tableMap.containsKey(tableType) where tableType is somewhat case-insensitive.
     */
    public boolean isSupported(String tableType) {
	return getTableName(tableType) != null;
    }

    public String getInstance() {
	return instance;
    }

    public String getUserName() {
	return userName;
    }

    public String getPassword() {
	return password;
    }

    /**
     * Retrieve whether or not this is an outputSchema. Output schemas may auto-create tables and
     * truncate tables. Input schemas cannot. Users may truncate and drop tables managed by output
     * schemas but not by input schemas.
     *
     * @return
     */
    public boolean isOutputSchema() {
	return outputSchema;
    }

    /**
     * Test the existence of the table with the specified type. Eg., if the tableNames map contains
     * entry "Site" -> "idcstatic.site", and tableType "Site" is specified, then this method returns
     * true if the table "idcstatic.site" exists.
     *
     * @param table type.
     * @return true if table exists.
     * @throws SQLException
     */
    public boolean doesTableTypeExist(String tableType) throws SQLException {
	String tableName = getTableName(tableType);
	return tableName != null && doesTableExist(tableName);
    }

    /**
     * Test the existence of the specified table.
     *
     * @param tableName
     * @return true if table exists.
     * @throws SQLException
     */
    public boolean doesTableExist(String tableName) throws SQLException {
	boolean exists = false;
	Statement stmt = null;
	ResultSet rs = null;
	try {
	    stmt = dbConnection.createStatement();
	    rs = stmt.executeQuery(
		    String.format("select count(*) from %s where 1=2", tableName.trim().toUpperCase()));
	    exists = true;
	} catch (Exception e) {
	    exists = false;
	} finally {
	    try {
		if (rs != null)
		    rs.close();
	    } catch (Exception e) {
	    }
	    ;
	    try {
		if (stmt != null)
		    stmt.close();
	    } catch (Exception e) {
	    }
	    ;
	}
	return exists;
    }

    /**
     * Truncate one of the tables managed by this Schema (uses table type, not table name). Eg., if
     * the tableName map contains entry "Origin" -> "output_origin", and table type "Origin" is
     * specified, then the "output_origin" table is truncated.
     *
     * @param dbConnection
     * @param tableType
     * @throws Exception
     */
    public void truncateTableType(String tableType) throws Exception {
	String tableName = getTableName(tableType);
	if (tableName != null)
	    truncateTable(tableName);
    }

    /**
     * Truncate one of the tables managed by this Schema (uses table name, not table type).
     *
     * @param dbConnection
     * @param tableName
     * @throws Exception
     */
    public void truncateTable(String tableName) throws Exception {
	if (!tableMap.values().contains(tableName))
	    throw new Exception("Cannot truncate table " + tableName + " because this schema ("
		    + schemaName + ") knows nothing about a table with that name.");

	Statement stmt = null;
	ResultSet rs = null;
	try {
	    stmt = dbConnection.createStatement();
	    rs = stmt.executeQuery("truncate table " + tableName.trim().toUpperCase());
	} catch (Exception e) {

	} finally {
	    try {
		if (rs != null)
		    rs.close();
	    } catch (Exception e) {
	    }
	    ;
	    try {
		if (stmt != null)
		    stmt.close();
	    } catch (Exception e) {
	    }
	    ;
	}
    }

    /**
     * Drop one of the tables managed by this Schema (uses table type, not table name). Eg., if the
     * tableName map contains entry "Origin" -> "output_origin", and table type "Origin" is specified,
     * then the "output_origin" table is dropped.
     *
     * @param dbConnection
     * @param tableType
     * @throws Exception
     */
    public void dropTableType(String tableType) throws Exception {
	String tableName = getTableName(tableType);
	if (tableName != null)
	    dropTable(tableName);
    }

    /**
     * Drop one of the tables managed by this Schema (uses table name, not table type).
     *
     * @param dbConnection
     * @param tableName
     * @throws Exception
     */
    public void dropTable(String tableName) throws Exception {
	if (!tableMap.values().contains(tableName))
	    throw new Exception("Cannot drop table " + tableName + " because this schema (" + schemaName
		    + ") knows nothing about a table with that name.");


	Statement stmt = null;
	ResultSet rs = null;
	try {
	    stmt = dbConnection.createStatement();
	    rs = stmt.executeQuery("drop table " + tableName.trim().toUpperCase());
	} catch (Exception e) {

	} finally {
	    try {
		if (rs != null)
		    rs.close();
	    } catch (Exception e) {
	    }
	    ;
	    try {
		if (stmt != null)
		    stmt.close();
	    } catch (Exception e) {
	    }
	    ;
	}
    }

    public Long getMaxIdType(String tableType, String id) throws Exception {
	String tableName = getTableName(tableType);
	if (tableName == null)
	    return null;
	return getMaxId(tableName, id);
    }

    /**
     * Find the maximum value of the specified ID.
     *
     * @param tableName a concrete table name (not a table type)
     * @param id        name of the id (orid, arid, etc.)
     * @return the maximum value of the specified id in the table.
     * @throws SQLException
     */
    public Long getMaxId(String tableName, String id) throws SQLException {
	if (!tableMap.values().contains(tableName))
	    throw new SQLException(
		    "Cannot get max " + id + " from table " + tableName + " because this schema ("
			    + schemaName + ") knows nothing about a table with that name.");

	Long value = null;
	String sql = String.format("select max(%s) from %s", id, tableName);

	try {
	    Statement statement = getConnection().createStatement();
	    ResultSet rs = statement.executeQuery(sql);

	    value = rs.next() ? rs.getLong(1) : null;

	    rs.close();
	    statement.close();
	} catch (Exception e) {
	    throw new SQLException(String.format("%s%s%nUserName=%s  Instance=%s", e.getMessage(), sql,
		    getUserName(), getInstance()));
	}

	return value;
    }

    /**
     * Execute a commit on the database connection.
     *
     * @throws Exception if this is not an outputSchema
     */
    public void commit() throws Exception {
	if (!outputSchema)
	    throw new Exception("Cannot execute commit because this is not an outputSchema.");
	dbConnection.commit();
    }

    /**
     * Execute a rollback on the database connection.
     *
     * @throws Exception if this is not an outputSchema
     */
    public void rollback() throws Exception {
	if (!outputSchema)
	    throw new Exception("Cannot execute rollback because this is not an outputSchema.");
	dbConnection.rollback();
    }

    /**
     * Closes the database connection.
     *
     * @throws SQLException
     */
    public void close() throws SQLException {
	dbConnection.close();
    }

    @Override
    public String toString() {
	return log.toString();
    }

    /**
     * Change the first letter of the input string to a capital letter. All other letters remain
     * unchanged.
     *
     * @param s
     * @return
     */
    private String capitalize(String s) {
	return s == null ? null
		: s.length() == 0 ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * Load default database parameters from System.getProperty("user.home")+"/database.properties"
     *
     * @return default database parameters. Might be empty but will not be null.
     */
    private static Map<String, String> parseDefaults() {
	TreeMap<String, String> defaults = new TreeMap<String, String>();
	try {
	    File profile = new File(System.getProperty("user.home") + "/database.properties");
	    if (profile.exists()) {
		BufferedReader input = new BufferedReader(new FileReader(profile));
		String line;
		while ((line = input.readLine()) != null) {
		    line = line.trim();
		    if (!line.startsWith("#")) {
			int i = line.indexOf('=');
			if (i > 0)
			    defaults.put(line.substring(0, i).trim(), line.substring(i + 1).trim());
		    }

		}
		input.close();
	    }
	} catch (Exception e) {
	    defaults.clear();
	}
	return defaults;
    }

    public void executeStatement(String sql) throws SQLException {
	Statement statement = null;
	try {
	    statement = getConnection().createStatement();
	    statement.execute(sql);
	} catch (SQLException e) {
	    throw new SQLException(e.getMessage() + sql);
	} finally {
	    if (statement != null)
		statement.close();
	}
    }

    public int getFetchSize() {
	return this.fetchSize;
    }

    public void setFetchSize(int fetchSize) {
	this.fetchSize = fetchSize;
    }

    /**
     * Initialization of all attributes managed by this Schema. The Connection created by this Schema
     * has autoCommit turned off.
     *
     * @param wallet               if !null then instance, userName and password are ignored
     * @param instance             if null defaults to value from user.home/database.properties
     * @param userName             if null defaults to value from user.home/database.properties
     * @param password             if null defaults to value from user.home/database.properties
     * @param driver               if null defaults to oracle.jdbc.driver.OracleDriver
     * @throws ClassNotFoundException 
     * @throws Exception
     */
    public static boolean testConnection(String... args) throws SQLException, ClassNotFoundException {
	String wallet   = args.length > 1 ? args[1] : null; 
	String instance = args.length > 2 ? args[2] : null; 
	String userName = args.length > 3 ? args[3] : null; 
	String password = args.length > 4 ? args[4] : null; 
	String driver   = args.length > 5 ? args[5] : null; 

	if (wallet == null)
	{
	    // try and load default database parameters from
	    // System.getProperty("user.home")+"/database.properties"
	    Map<String, String> defaults = parseDefaults();
	    if (!defaults.isEmpty()) {
		if (instance == null)
		    instance = defaults.get("DB_INSTANCE");
		if (driver == null)
		    driver = defaults.get("DB_DRIVER");
		if (userName == null)
		    userName = defaults.get("DB_USERNAME");
		if (password == null)
		    password = defaults.get(String.format("DB_PASSWORD_%s", userName.toUpperCase()));
	    } else {
		// database.properties did not exist, try System.getenv()
			if (instance == null)
			    instance = System.getenv("DB_INSTANCE");
			if (driver == null)
			    driver = System.getenv("DB_DRIVER");
			if (userName == null)
			    userName = System.getenv("DB_USERNAME");
			if (userName == null)
			    userName = String.format("gnem_%s", System.getProperty("user.name"));
			if (password == null)
			    password = System.getenv(String.format("DB_PASSWORD_%s", userName.toUpperCase()));
		    }

	    boolean ok = true;
		if (instance == null) {
			//System.err.println("DB_INSTANCE = null");
			ok = false;
		}
		if (userName == null){
			//System.err.println("DB_USERNAME = null");
			ok = false;
		}
		if (password == null){
			//System.err.println("DB_PASSWORD = null");
			ok = false;
		}
		if (!ok) return false;

	    if (driver == null)
		driver = "oracle.jdbc.driver.OracleDriver";
	}

	Class.forName(driver);

	Connection connection = null;
	boolean connected = false;
	try {
	    if (wallet == null)
		connection = DriverManager.getConnection(instance, userName, password);
	    else 
		connection = DriverManager.getConnection(wallet);
	    connected = true;
	}
	catch (SQLException e) {
	    connected = false;
	    if (!e.getMessage().contains("The Network Adapter could not establish the connection"))
		e.printStackTrace();
	}
	finally { if (connection != null) connection.close(); }
	return connected;
    }
}
