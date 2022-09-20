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
package gov.sandia.gnem.dbtabledefs.nnsa_kb_custom;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * eventid_version
 */
public class Eventid_version extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique identifier for version of a set of parameter files.
   */
  private long versionid;

  static final public long VERSIONID_NA = Long.MIN_VALUE;

  /**
   * Version name. This name describes the set of EventID Tool parameter files associated with a
   * particular station.
   */
  private String version_name;

  static final public String VERSION_NAME_NA = null;

  /**
   * Description of the objected represented in the table.
   */
  private String descript;

  static final public String DESCRIPT_NA = null;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;

  /**
   * Author who loaded data
   */
  private String ldauth;

  static final public String LDAUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("versionid", Columns.FieldType.LONG, "%d");
    columns.add("version_name", Columns.FieldType.STRING, "%s");
    columns.add("descript", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("ldauth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Eventid_version(long versionid, String version_name, String descript, String auth,
      String ldauth) {
    setValues(versionid, version_name, descript, auth, ldauth);
  }

  private void setValues(long versionid, String version_name, String descript, String auth,
      String ldauth) {
    this.versionid = versionid;
    this.version_name = version_name;
    this.descript = descript;
    this.auth = auth;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Eventid_version(Eventid_version other) {
    this.versionid = other.getVersionid();
    this.version_name = other.getVersion_name();
    this.descript = other.getDescript();
    this.auth = other.getAuth();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Eventid_version() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(VERSIONID_NA, VERSION_NAME_NA, DESCRIPT_NA, AUTH_NA, LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "version_name":
        return version_name;
      case "descript":
        return descript;
      case "auth":
        return auth;
      case "ldauth":
        return ldauth;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "version_name":
        version_name = value;
        break;
      case "descript":
        descript = value;
        break;
      case "auth":
        auth = value;
        break;
      case "ldauth":
        ldauth = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
        + " is not a valid input name ...");
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "versionid":
        return versionid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "versionid":
        versionid = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  /**
   * Sets inputColumnNames to the input columnNames. If any entry in columnNames is invalid, or if
   * not all required columns are contained in columnNames then an error is thrown.
   * 
   * @param columnNames
   * @throws IOException
   */
  public static void setNewInputColumnNames(String[] columnNames) throws IOException {
    columns.containsValidColumnNames(columnNames);
    columns.containsAllRequiredColumns(columnNames);
    inputColumnNames = columnNames;
  }

  /**
   * Sets outputColumnNames to the input columnNames. If any entry in columnNames is invalid then an
   * error is thrown.
   * 
   * @param columnNames
   * @throws IOException
   */
  public static void setNewOutputColumnNames(String[] columnNames) throws IOException {
    columns.containsValidColumnNames(columnNames);
    outputColumnNames = columnNames;
  }

  public static Columns getColumns() {
    return columns;
  }

  public static String[] getInputColumnNames() {
    return inputColumnNames;
  }

  public static String[] getOutputColumnNames() {
    return outputColumnNames;
  }

  /**
   * Constructor that loads values from a Scanner. It can read the output of the toString()
   * function.
   */
  public Eventid_version(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Eventid_version(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Eventid_version(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Eventid_version(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Eventid_version(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[5];
    values[0] = versionid;
    values[1] = version_name;
    values[2] = descript;
    values[3] = auth;
    values[4] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[6];
    values[0] = versionid;
    values[1] = version_name;
    values[2] = descript;
    values[3] = auth;
    values[4] = ldauth;
    values[5] = lddate;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * <p>
   * The supplied java.util.Date is converted to a java.sql.Date in the output.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.util.Date lddate) {
    return getValues(new java.sql.Date(lddate.getTime()));
  }

  /**
   * Write this row to a DataOutputStream.
   */
  public void write(DataOutputStream output) throws IOException {
    output.writeLong(versionid);
    writeString(output, version_name);
    writeString(output, descript);
    writeString(output, auth);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(versionid);
    writeString(output, version_name);
    writeString(output, descript);
    writeString(output, auth);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Eventid_version objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Eventid_version objects.
   * @throws IOException
   */
  static public void readEventid_versions(BufferedReader input, Collection<Eventid_version> rows)
      throws IOException {
    String[] saved = Eventid_version.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Eventid_version
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Eventid_version(new Scanner(line)));
    }
    input.close();
    Eventid_version.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Eventid_version objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Eventid_version objects.
   * @throws IOException
   */
  static public void readEventid_versions(File inputFile, Collection<Eventid_version> rows)
      throws IOException {
    readEventid_versions(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Eventid_version objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Eventid_version objects.
   * @throws IOException
   */
  static public void readEventid_versions(InputStream inputStream, Collection<Eventid_version> rows)
      throws IOException {
    readEventid_versions(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Eventid_version objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Eventid_version objects
   * @throws IOException
   */
  static public Set<Eventid_version> readEventid_versions(BufferedReader input) throws IOException {
    Set<Eventid_version> rows = new LinkedHashSet<Eventid_version>();
    readEventid_versions(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Eventid_version objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Eventid_version objects
   * @throws IOException
   */
  static public Set<Eventid_version> readEventid_versions(File inputFile) throws IOException {
    return readEventid_versions(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Eventid_version objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Eventid_version objects
   * @throws IOException
   */
  static public Set<Eventid_version> readEventid_versions(InputStream input) throws IOException {
    return readEventid_versions(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Eventid_version objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param eventid_versions the Eventid_version objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Eventid_version> eventid_versions)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Eventid_version eventid_version : eventid_versions)
      eventid_version.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Eventid_version objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param eventid_versions the Eventid_version objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Eventid_version> eventid_versions, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?)");
      for (Eventid_version eventid_version : eventid_versions) {
        int i = 0;
        statement.setLong(++i, eventid_version.versionid);
        statement.setString(++i, eventid_version.version_name);
        statement.setString(++i, eventid_version.descript);
        statement.setString(++i, eventid_version.auth);
        statement.setString(++i, eventid_version.ldauth);
        statement.setTimestamp(++i, new java.sql.Timestamp(lddate.getTime()));
        statement.addBatch();
      }
      statement.executeBatch();
      statement.close();
      if (commit)
        connection.commit();
    } finally {
      if (statement != null)
        statement.close();
    }
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Eventid_version table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Eventid_version> readEventid_versions(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Eventid_version> results = new HashSet<Eventid_version>();
    readEventid_versions(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Eventid_version table.
   * @param eventid_versions
   * @throws SQLException
   */
  static public void readEventid_versions(Connection connection, String selectStatement,
      Set<Eventid_version> eventid_versions) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        eventid_versions.add(new Eventid_version(rs));
    } catch (Exception e) {
      throw new SQLException(String.format("%s%n%s%n", e.getMessage(), selectStatement));
    } finally {
      if (rs != null)
        rs.close();
      if (statement != null)
        statement.close();
    }
  }

  /**
   * Retrieve a String representation of a sql statement that can be used to insert the values of
   * this Eventid_version object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Eventid_version object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("versionid, version_name, descript, auth, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(versionid)).append(", ");
    sql.append("'").append(version_name).append("', ");
    sql.append("'").append(descript).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Eventid_version in the database. Primary and unique keys are set, if
   * defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Eventid_version in the database
   * 
   * @param connection
   * @param tableName
   * @param includePrimaryKeyConstraint
   * @param includeUniqueKeyConstraint
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName,
      boolean includePrimaryKeyConstraint, boolean includeUniqueKeyConstraint) throws SQLException {
    Statement statement = connection.createStatement();
    for (String s : createTableScript(tableName, includePrimaryKeyConstraint,
        includeUniqueKeyConstraint))
      statement.execute(s);
    statement.close();
  }

  /**
   * Generate a sql script to create a table of type Eventid_version in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Eventid_version in the database
   * 
   * @param tableName
   * @param includePrimaryKeyConstraint
   * @param includeUniqueKeyConstraint
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName,
      boolean includePrimaryKeyConstraint, boolean includeUniqueKeyConstraint) throws SQLException {
    ArrayList<String> script = new ArrayList<String>();
    StringBuffer buf = new StringBuffer();
    buf.append("create table " + tableName + " (\n");
    buf.append("versionid    number(9)            NOT NULL,\n");
    buf.append("version_name varchar2(30)         NOT NULL,\n");
    buf.append("descript     varchar2(1024)       NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("ldauth       varchar2(15)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (versionid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (version_name)");
    script.add("grant select on " + tableName + " to public");
    return script;
  }

  /**
   * Write this row to an ascii String with no newline at the end.
   */
  @Override
  public String toString() {
    try {
      return getOutputString(outputColumnNames, columns);
    } catch (Exception ex) {
      System.out.println(ex.getStackTrace());
      return "";
    }
  }

  /**
   * Write this row to an ascii file. No newline is appended at the end of the record.
   */
  public void write(BufferedWriter output) throws IOException {
    output.write(toString());
  }

  /**
   * Write this row to an ascii file, including a newline appended at the end of the record.
   */
  public void writeln(BufferedWriter output) throws IOException {
    output.write(toString());
    output.newLine();
  }

  /**
   * Return table output header line.
   * 
   * @param outputColumnNames output table column names.
   * @return Table output header line.
   */
  public static String getHeader() {
    return getOutputHeaderString(outputColumnNames);
  }

  /**
   * Writes the output header to the input buffered writer.
   * 
   * @param output The buffered writer.
   * @throws IOException
   */
  public static void writeHeader(BufferedWriter output) throws IOException {
    output.write(getOutputHeaderString(outputColumnNames));
    output.newLine();
  }

  /**
   * Maximum number of bytes required to store an instance of this in a ByteBuffer or
   * DataOutputStream.
   */
  @Override
  public int maxBytes() {
    return 1113;
  }

  /**
   * Return true if primary keys are equal in this and other. Returns false if primary keys are not
   * defined.
   * 
   * @param other
   * @return true if primary keys are equal in this and other.
   */
  @Override
  public boolean equalPrimaryKey(BaseRow other) {
    return (other instanceof Eventid_version) && ((Eventid_version) other).versionid == versionid;
  }

  /**
   * Return true if unique keys are equal in this and other. Returns false if unique keys are not
   * defined.
   * 
   * @param other
   * @return true if unique keys are equal in this and other.
   */
  @Override
  public boolean equalUniqueKey(BaseRow other) {
    return (other instanceof Eventid_version)
        && ((Eventid_version) other).version_name.equals(version_name);
  }

  /**
   * Unique identifier for version of a set of parameter files.
   * 
   * @return versionid
   */
  public long getVersionid() {
    return versionid;
  }

  /**
   * Unique identifier for version of a set of parameter files.
   * 
   * @param versionid
   * @throws IllegalArgumentException if versionid >= 1000000000
   */
  public Eventid_version setVersionid(long versionid) {
    if (versionid >= 1000000000L)
      throw new IllegalArgumentException("versionid=" + versionid + " but cannot be >= 1000000000");
    this.versionid = versionid;
    setHash(null);
    return this;
  }

  /**
   * Version name. This name describes the set of EventID Tool parameter files associated with a
   * particular station.
   * 
   * @return version_name
   */
  public String getVersion_name() {
    return version_name;
  }

  /**
   * Version name. This name describes the set of EventID Tool parameter files associated with a
   * particular station.
   * 
   * @param version_name
   * @throws IllegalArgumentException if version_name.length() >= 30
   */
  public Eventid_version setVersion_name(String version_name) {
    if (version_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("version_name.length() cannot be > 30.  version_name=%s", version_name));
    this.version_name = version_name;
    setHash(null);
    return this;
  }

  /**
   * Description of the objected represented in the table.
   * 
   * @return descript
   */
  public String getDescript() {
    return descript;
  }

  /**
   * Description of the objected represented in the table.
   * 
   * @param descript
   * @throws IllegalArgumentException if descript.length() >= 1024
   */
  public Eventid_version setDescript(String descript) {
    if (descript.length() > 1024)
      throw new IllegalArgumentException(
          String.format("descript.length() cannot be > 1024.  descript=%s", descript));
    this.descript = descript;
    setHash(null);
    return this;
  }

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 20
   */
  public Eventid_version setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * Author who loaded data
   * 
   * @return ldauth
   */
  public String getLdauth() {
    return ldauth;
  }

  /**
   * Author who loaded data
   * 
   * @param ldauth
   * @throws IllegalArgumentException if ldauth.length() >= 15
   */
  public Eventid_version setLdauth(String ldauth) {
    if (ldauth.length() > 15)
      throw new IllegalArgumentException(
          String.format("ldauth.length() cannot be > 15.  ldauth=%s", ldauth));
    this.ldauth = ldauth;
    setHash(null);
    return this;
  }

  /**
   * Retrieve the name of the schema.
   * 
   * @return schema name
   */
  static public String getSchemaName() {
    return "NNSA KB Custom";
  }

}
