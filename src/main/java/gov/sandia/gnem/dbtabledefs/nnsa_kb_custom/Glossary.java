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
 * glossary
 */
public class Glossary extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique glossary identifier.
   */
  private long glossid;

  static final public long GLOSSID_NA = Long.MIN_VALUE;

  /**
   * Comment line number. This number is assigned as a sequence number for multiple line comments.
   */
  private long lineno;

  static final public long LINENO_NA = Long.MIN_VALUE;

  /**
   * Name of correction surface (<B>corrsurf_az</B>, <B>corrsurf_sh</B>); String name to be defined
   * (<B>glossary</B>).
   */
  private String name;

  static final public String NAME_NA = null;

  /**
   * Column name, lowercase version of that used in Oracle's data dictionary.
   */
  private String column_name;

  static final public String COLUMN_NAME_NA = "-";

  /**
   * Table name, lowercase version of that used in Oracle's data dictionary.
   */
  private String table_name;

  static final public String TABLE_NAME_NA = "-";

  /**
   * Database account name of table's owner, if name is specific to owners.
   */
  private String owner;

  static final public String OWNER_NA = "-";

  /**
   * Definition
   */
  private String definition;

  static final public String DEFINITION_NA = "-";

  /**
   * Schema name.
   */
  private String schema_name;

  static final public String SCHEMA_NAME_NA = "-";

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("glossid", Columns.FieldType.LONG, "%d");
    columns.add("lineno", Columns.FieldType.LONG, "%d");
    columns.add("name", Columns.FieldType.STRING, "%s");
    columns.add("column_name", Columns.FieldType.STRING, "%s");
    columns.add("table_name", Columns.FieldType.STRING, "%s");
    columns.add("owner", Columns.FieldType.STRING, "%s");
    columns.add("definition", Columns.FieldType.STRING, "%s");
    columns.add("schema_name", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Glossary(long glossid, long lineno, String name, String column_name, String table_name,
      String owner, String definition, String schema_name, String auth) {
    setValues(glossid, lineno, name, column_name, table_name, owner, definition, schema_name, auth);
  }

  private void setValues(long glossid, long lineno, String name, String column_name,
      String table_name, String owner, String definition, String schema_name, String auth) {
    this.glossid = glossid;
    this.lineno = lineno;
    this.name = name;
    this.column_name = column_name;
    this.table_name = table_name;
    this.owner = owner;
    this.definition = definition;
    this.schema_name = schema_name;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Glossary(Glossary other) {
    this.glossid = other.getGlossid();
    this.lineno = other.getLineno();
    this.name = other.getName();
    this.column_name = other.getColumn_name();
    this.table_name = other.getTable_name();
    this.owner = other.getOwner();
    this.definition = other.getDefinition();
    this.schema_name = other.getSchema_name();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Glossary() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(GLOSSID_NA, LINENO_NA, NAME_NA, COLUMN_NAME_NA, TABLE_NAME_NA, OWNER_NA,
        DEFINITION_NA, SCHEMA_NAME_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "name":
        return name;
      case "column_name":
        return column_name;
      case "table_name":
        return table_name;
      case "owner":
        return owner;
      case "definition":
        return definition;
      case "schema_name":
        return schema_name;
      case "auth":
        return auth;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "name":
        name = value;
        break;
      case "column_name":
        column_name = value;
        break;
      case "table_name":
        table_name = value;
        break;
      case "owner":
        owner = value;
        break;
      case "definition":
        definition = value;
        break;
      case "schema_name":
        schema_name = value;
        break;
      case "auth":
        auth = value;
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
      case "glossid":
        return glossid;
      case "lineno":
        return lineno;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "glossid":
        glossid = value;
        break;
      case "lineno":
        lineno = value;
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
  public Glossary(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Glossary(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), readString(input), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Glossary(ByteBuffer input) {
    this(input.getLong(), input.getLong(), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Glossary(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Glossary(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getString(offset + 9));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[9];
    values[0] = glossid;
    values[1] = lineno;
    values[2] = name;
    values[3] = column_name;
    values[4] = table_name;
    values[5] = owner;
    values[6] = definition;
    values[7] = schema_name;
    values[8] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[10];
    values[0] = glossid;
    values[1] = lineno;
    values[2] = name;
    values[3] = column_name;
    values[4] = table_name;
    values[5] = owner;
    values[6] = definition;
    values[7] = schema_name;
    values[8] = auth;
    values[9] = lddate;
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
    output.writeLong(glossid);
    output.writeLong(lineno);
    writeString(output, name);
    writeString(output, column_name);
    writeString(output, table_name);
    writeString(output, owner);
    writeString(output, definition);
    writeString(output, schema_name);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(glossid);
    output.putLong(lineno);
    writeString(output, name);
    writeString(output, column_name);
    writeString(output, table_name);
    writeString(output, owner);
    writeString(output, definition);
    writeString(output, schema_name);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Glossary objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Glossary objects.
   * @throws IOException
   */
  static public void readGlossarys(BufferedReader input, Collection<Glossary> rows)
      throws IOException {
    String[] saved = Glossary.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Glossary
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Glossary(new Scanner(line)));
    }
    input.close();
    Glossary.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Glossary objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Glossary objects.
   * @throws IOException
   */
  static public void readGlossarys(File inputFile, Collection<Glossary> rows) throws IOException {
    readGlossarys(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Glossary objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Glossary objects.
   * @throws IOException
   */
  static public void readGlossarys(InputStream inputStream, Collection<Glossary> rows)
      throws IOException {
    readGlossarys(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Glossary objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Glossary objects
   * @throws IOException
   */
  static public Set<Glossary> readGlossarys(BufferedReader input) throws IOException {
    Set<Glossary> rows = new LinkedHashSet<Glossary>();
    readGlossarys(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Glossary objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Glossary objects
   * @throws IOException
   */
  static public Set<Glossary> readGlossarys(File inputFile) throws IOException {
    return readGlossarys(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Glossary objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Glossary objects
   * @throws IOException
   */
  static public Set<Glossary> readGlossarys(InputStream input) throws IOException {
    return readGlossarys(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Glossary objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param glossarys the Glossary objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Glossary> glossarys)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Glossary glossary : glossarys)
      glossary.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Glossary objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param glossarys the Glossary objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Glossary> glossarys, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?)");
      for (Glossary glossary : glossarys) {
        int i = 0;
        statement.setLong(++i, glossary.glossid);
        statement.setLong(++i, glossary.lineno);
        statement.setString(++i, glossary.name);
        statement.setString(++i, glossary.column_name);
        statement.setString(++i, glossary.table_name);
        statement.setString(++i, glossary.owner);
        statement.setString(++i, glossary.definition);
        statement.setString(++i, glossary.schema_name);
        statement.setString(++i, glossary.auth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Glossary
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Glossary> readGlossarys(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Glossary> results = new HashSet<Glossary>();
    readGlossarys(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Glossary
   *        table.
   * @param glossarys
   * @throws SQLException
   */
  static public void readGlossarys(Connection connection, String selectStatement,
      Set<Glossary> glossarys) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        glossarys.add(new Glossary(rs));
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
   * this Glossary object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Glossary object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "glossid, lineno, name, column_name, table_name, owner, definition, schema_name, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(glossid)).append(", ");
    sql.append(Long.toString(lineno)).append(", ");
    sql.append("'").append(name).append("', ");
    sql.append("'").append(column_name).append("', ");
    sql.append("'").append(table_name).append("', ");
    sql.append("'").append(owner).append("', ");
    sql.append("'").append(definition).append("', ");
    sql.append("'").append(schema_name).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Glossary in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Glossary in the database
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
   * Generate a sql script to create a table of type Glossary in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Glossary in the database
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
    buf.append("glossid      number(9)            NOT NULL,\n");
    buf.append("lineno       number(9)            NOT NULL,\n");
    buf.append("name         varchar2(30)         NOT NULL,\n");
    buf.append("column_name  varchar2(30)         NOT NULL,\n");
    buf.append("table_name   varchar2(30)         NOT NULL,\n");
    buf.append("owner        varchar2(30)         NOT NULL,\n");
    buf.append("definition   varchar2(80)         NOT NULL,\n");
    buf.append("schema_name  varchar2(30)         NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (glossid,lineno)");
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
    return 294;
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
    return (other instanceof Glossary) && ((Glossary) other).glossid == glossid
        && ((Glossary) other).lineno == lineno;
  }

  /**
   * Unique glossary identifier.
   * 
   * @return glossid
   */
  public long getGlossid() {
    return glossid;
  }

  /**
   * Unique glossary identifier.
   * 
   * @param glossid
   * @throws IllegalArgumentException if glossid >= 1000000000
   */
  public Glossary setGlossid(long glossid) {
    if (glossid >= 1000000000L)
      throw new IllegalArgumentException("glossid=" + glossid + " but cannot be >= 1000000000");
    this.glossid = glossid;
    setHash(null);
    return this;
  }

  /**
   * Comment line number. This number is assigned as a sequence number for multiple line comments.
   * 
   * @return lineno
   */
  public long getLineno() {
    return lineno;
  }

  /**
   * Comment line number. This number is assigned as a sequence number for multiple line comments.
   * 
   * @param lineno
   * @throws IllegalArgumentException if lineno >= 1000000000
   */
  public Glossary setLineno(long lineno) {
    if (lineno >= 1000000000L)
      throw new IllegalArgumentException("lineno=" + lineno + " but cannot be >= 1000000000");
    this.lineno = lineno;
    setHash(null);
    return this;
  }

  /**
   * Name of correction surface (<B>corrsurf_az</B>, <B>corrsurf_sh</B>); String name to be defined
   * (<B>glossary</B>).
   * 
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * Name of correction surface (<B>corrsurf_az</B>, <B>corrsurf_sh</B>); String name to be defined
   * (<B>glossary</B>).
   * 
   * @param name
   * @throws IllegalArgumentException if name.length() >= 30
   */
  public Glossary setName(String name) {
    if (name.length() > 30)
      throw new IllegalArgumentException(
          String.format("name.length() cannot be > 30.  name=%s", name));
    this.name = name;
    setHash(null);
    return this;
  }

  /**
   * Column name, lowercase version of that used in Oracle's data dictionary.
   * 
   * @return column_name
   */
  public String getColumn_name() {
    return column_name;
  }

  /**
   * Column name, lowercase version of that used in Oracle's data dictionary.
   * 
   * @param column_name
   * @throws IllegalArgumentException if column_name.length() >= 30
   */
  public Glossary setColumn_name(String column_name) {
    if (column_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("column_name.length() cannot be > 30.  column_name=%s", column_name));
    this.column_name = column_name;
    setHash(null);
    return this;
  }

  /**
   * Table name, lowercase version of that used in Oracle's data dictionary.
   * 
   * @return table_name
   */
  public String getTable_name() {
    return table_name;
  }

  /**
   * Table name, lowercase version of that used in Oracle's data dictionary.
   * 
   * @param table_name
   * @throws IllegalArgumentException if table_name.length() >= 30
   */
  public Glossary setTable_name(String table_name) {
    if (table_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("table_name.length() cannot be > 30.  table_name=%s", table_name));
    this.table_name = table_name;
    setHash(null);
    return this;
  }

  /**
   * Database account name of table's owner, if name is specific to owners.
   * 
   * @return owner
   */
  public String getOwner() {
    return owner;
  }

  /**
   * Database account name of table's owner, if name is specific to owners.
   * 
   * @param owner
   * @throws IllegalArgumentException if owner.length() >= 30
   */
  public Glossary setOwner(String owner) {
    if (owner.length() > 30)
      throw new IllegalArgumentException(
          String.format("owner.length() cannot be > 30.  owner=%s", owner));
    this.owner = owner;
    setHash(null);
    return this;
  }

  /**
   * Definition
   * 
   * @return definition
   */
  public String getDefinition() {
    return definition;
  }

  /**
   * Definition
   * 
   * @param definition
   * @throws IllegalArgumentException if definition.length() >= 80
   */
  public Glossary setDefinition(String definition) {
    if (definition.length() > 80)
      throw new IllegalArgumentException(
          String.format("definition.length() cannot be > 80.  definition=%s", definition));
    this.definition = definition;
    setHash(null);
    return this;
  }

  /**
   * Schema name.
   * 
   * @return schema_name
   */
  public String getSchema_name() {
    return schema_name;
  }

  /**
   * Schema name.
   * 
   * @param schema_name
   * @throws IllegalArgumentException if schema_name.length() >= 30
   */
  public Glossary setSchema_name(String schema_name) {
    if (schema_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("schema_name.length() cannot be > 30.  schema_name=%s", schema_name));
    this.schema_name = schema_name;
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
  public Glossary setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
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
