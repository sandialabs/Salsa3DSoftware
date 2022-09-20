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
 * colassoc
 */
public class Colassoc extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Table name, lowercase version of that used in Oracle's data dictionary.
   */
  private String table_name;

  static final public String TABLE_NAME_NA = null;

  /**
   * Column name, lowercase version of that used in Oracle's data dictionary.
   */
  private String column_name;

  static final public String COLUMN_NAME_NA = null;

  /**
   * Column type, the administrative role played by this column in this table.
   */
  private String column_type;

  static final public String COLUMN_TYPE_NA = "-";

  /**
   * Column position, order of column in table definition, must start at 1 and increment by one
   * without duplicates for any given table.
   */
  private long column_position;

  static final public long COLUMN_POSITION_NA = Long.MIN_VALUE;

  /**
   * Determines whether the NA value for this column is allowed in this table or not.
   */
  private String na_allowed;

  static final public String NA_ALLOWED_NA = null;

  /**
   * Native key or identifier name as defined for the table where the key is not a foreign key. If
   * not foreign, then nativekeyname is 'ownedid!'; if the column is a multiple-key column where the
   * keyname for the numeric keys is given in a second column, nativekeyname is 'valueof:...' where
   * the name of the second column is provided in place of the ellipsis.
   */
  private String nativekeyname;

  static final public String NATIVEKEYNAME_NA = "-";

  /**
   * Schema name for schema to which table belongs that defines this key.
   */
  private String nativekeyschema;

  static final public String NATIVEKEYSCHEMA_NA = "-";

  /**
   * Schema name.
   */
  private String schema_name;

  static final public String SCHEMA_NAME_NA = null;

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
    columns.add("table_name", Columns.FieldType.STRING, "%s");
    columns.add("column_name", Columns.FieldType.STRING, "%s");
    columns.add("column_type", Columns.FieldType.STRING, "%s");
    columns.add("column_position", Columns.FieldType.LONG, "%d");
    columns.add("na_allowed", Columns.FieldType.STRING, "%s");
    columns.add("nativekeyname", Columns.FieldType.STRING, "%s");
    columns.add("nativekeyschema", Columns.FieldType.STRING, "%s");
    columns.add("schema_name", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Colassoc(String table_name, String column_name, String column_type, long column_position,
      String na_allowed, String nativekeyname, String nativekeyschema, String schema_name,
      String auth) {
    setValues(table_name, column_name, column_type, column_position, na_allowed, nativekeyname,
        nativekeyschema, schema_name, auth);
  }

  private void setValues(String table_name, String column_name, String column_type,
      long column_position, String na_allowed, String nativekeyname, String nativekeyschema,
      String schema_name, String auth) {
    this.table_name = table_name;
    this.column_name = column_name;
    this.column_type = column_type;
    this.column_position = column_position;
    this.na_allowed = na_allowed;
    this.nativekeyname = nativekeyname;
    this.nativekeyschema = nativekeyschema;
    this.schema_name = schema_name;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Colassoc(Colassoc other) {
    this.table_name = other.getTable_name();
    this.column_name = other.getColumn_name();
    this.column_type = other.getColumn_type();
    this.column_position = other.getColumn_position();
    this.na_allowed = other.getNa_allowed();
    this.nativekeyname = other.getNativekeyname();
    this.nativekeyschema = other.getNativekeyschema();
    this.schema_name = other.getSchema_name();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Colassoc() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(TABLE_NAME_NA, COLUMN_NAME_NA, COLUMN_TYPE_NA, COLUMN_POSITION_NA, NA_ALLOWED_NA,
        NATIVEKEYNAME_NA, NATIVEKEYSCHEMA_NA, SCHEMA_NAME_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "table_name":
        return table_name;
      case "column_name":
        return column_name;
      case "column_type":
        return column_type;
      case "na_allowed":
        return na_allowed;
      case "nativekeyname":
        return nativekeyname;
      case "nativekeyschema":
        return nativekeyschema;
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
      case "table_name":
        table_name = value;
        break;
      case "column_name":
        column_name = value;
        break;
      case "column_type":
        column_type = value;
        break;
      case "na_allowed":
        na_allowed = value;
        break;
      case "nativekeyname":
        nativekeyname = value;
        break;
      case "nativekeyschema":
        nativekeyschema = value;
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
      case "column_position":
        return column_position;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "column_position":
        column_position = value;
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
  public Colassoc(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Colassoc(DataInputStream input) throws IOException {
    this(readString(input), readString(input), readString(input), input.readLong(),
        readString(input), readString(input), readString(input), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Colassoc(ByteBuffer input) {
    this(readString(input), readString(input), readString(input), input.getLong(),
        readString(input), readString(input), readString(input), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Colassoc(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Colassoc(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getLong(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getString(offset + 9));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[9];
    values[0] = table_name;
    values[1] = column_name;
    values[2] = column_type;
    values[3] = column_position;
    values[4] = na_allowed;
    values[5] = nativekeyname;
    values[6] = nativekeyschema;
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
    values[0] = table_name;
    values[1] = column_name;
    values[2] = column_type;
    values[3] = column_position;
    values[4] = na_allowed;
    values[5] = nativekeyname;
    values[6] = nativekeyschema;
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
    writeString(output, table_name);
    writeString(output, column_name);
    writeString(output, column_type);
    output.writeLong(column_position);
    writeString(output, na_allowed);
    writeString(output, nativekeyname);
    writeString(output, nativekeyschema);
    writeString(output, schema_name);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, table_name);
    writeString(output, column_name);
    writeString(output, column_type);
    output.putLong(column_position);
    writeString(output, na_allowed);
    writeString(output, nativekeyname);
    writeString(output, nativekeyschema);
    writeString(output, schema_name);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Colassoc objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Colassoc objects.
   * @throws IOException
   */
  static public void readColassocs(BufferedReader input, Collection<Colassoc> rows)
      throws IOException {
    String[] saved = Colassoc.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Colassoc
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Colassoc(new Scanner(line)));
    }
    input.close();
    Colassoc.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Colassoc objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Colassoc objects.
   * @throws IOException
   */
  static public void readColassocs(File inputFile, Collection<Colassoc> rows) throws IOException {
    readColassocs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Colassoc objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Colassoc objects.
   * @throws IOException
   */
  static public void readColassocs(InputStream inputStream, Collection<Colassoc> rows)
      throws IOException {
    readColassocs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Colassoc objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Colassoc objects
   * @throws IOException
   */
  static public Set<Colassoc> readColassocs(BufferedReader input) throws IOException {
    Set<Colassoc> rows = new LinkedHashSet<Colassoc>();
    readColassocs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Colassoc objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Colassoc objects
   * @throws IOException
   */
  static public Set<Colassoc> readColassocs(File inputFile) throws IOException {
    return readColassocs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Colassoc objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Colassoc objects
   * @throws IOException
   */
  static public Set<Colassoc> readColassocs(InputStream input) throws IOException {
    return readColassocs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Colassoc objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param colassocs the Colassoc objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Colassoc> colassocs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Colassoc colassoc : colassocs)
      colassoc.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Colassoc objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param colassocs the Colassoc objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Colassoc> colassocs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?)");
      for (Colassoc colassoc : colassocs) {
        int i = 0;
        statement.setString(++i, colassoc.table_name);
        statement.setString(++i, colassoc.column_name);
        statement.setString(++i, colassoc.column_type);
        statement.setLong(++i, colassoc.column_position);
        statement.setString(++i, colassoc.na_allowed);
        statement.setString(++i, colassoc.nativekeyname);
        statement.setString(++i, colassoc.nativekeyschema);
        statement.setString(++i, colassoc.schema_name);
        statement.setString(++i, colassoc.auth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Colassoc
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Colassoc> readColassocs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Colassoc> results = new HashSet<Colassoc>();
    readColassocs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Colassoc
   *        table.
   * @param colassocs
   * @throws SQLException
   */
  static public void readColassocs(Connection connection, String selectStatement,
      Set<Colassoc> colassocs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        colassocs.add(new Colassoc(rs));
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
   * this Colassoc object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Colassoc object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "table_name, column_name, column_type, column_position, na_allowed, nativekeyname, nativekeyschema, schema_name, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(table_name).append("', ");
    sql.append("'").append(column_name).append("', ");
    sql.append("'").append(column_type).append("', ");
    sql.append(Long.toString(column_position)).append(", ");
    sql.append("'").append(na_allowed).append("', ");
    sql.append("'").append(nativekeyname).append("', ");
    sql.append("'").append(nativekeyschema).append("', ");
    sql.append("'").append(schema_name).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Colassoc in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Colassoc in the database
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
   * Generate a sql script to create a table of type Colassoc in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Colassoc in the database
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
    buf.append("table_name   varchar2(30)         NOT NULL,\n");
    buf.append("column_name  varchar2(30)         NOT NULL,\n");
    buf.append("column_type  varchar2(30)         NOT NULL,\n");
    buf.append("column_position number(8)            NOT NULL,\n");
    buf.append("na_allowed   varchar2(1)          NOT NULL,\n");
    buf.append("nativekeyname varchar2(30)         NOT NULL,\n");
    buf.append("nativekeyschema varchar2(30)         NOT NULL,\n");
    buf.append("schema_name  varchar2(30)         NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (table_name,column_name,schema_name)");
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
    return 241;
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
    return (other instanceof Colassoc) && ((Colassoc) other).table_name.equals(table_name)
        && ((Colassoc) other).column_name.equals(column_name)
        && ((Colassoc) other).schema_name.equals(schema_name);
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
  public Colassoc setTable_name(String table_name) {
    if (table_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("table_name.length() cannot be > 30.  table_name=%s", table_name));
    this.table_name = table_name;
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
  public Colassoc setColumn_name(String column_name) {
    if (column_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("column_name.length() cannot be > 30.  column_name=%s", column_name));
    this.column_name = column_name;
    setHash(null);
    return this;
  }

  /**
   * Column type, the administrative role played by this column in this table.
   * 
   * @return column_type
   */
  public String getColumn_type() {
    return column_type;
  }

  /**
   * Column type, the administrative role played by this column in this table.
   * 
   * @param column_type
   * @throws IllegalArgumentException if column_type.length() >= 30
   */
  public Colassoc setColumn_type(String column_type) {
    if (column_type.length() > 30)
      throw new IllegalArgumentException(
          String.format("column_type.length() cannot be > 30.  column_type=%s", column_type));
    this.column_type = column_type;
    setHash(null);
    return this;
  }

  /**
   * Column position, order of column in table definition, must start at 1 and increment by one
   * without duplicates for any given table.
   * 
   * @return column_position
   */
  public long getColumn_position() {
    return column_position;
  }

  /**
   * Column position, order of column in table definition, must start at 1 and increment by one
   * without duplicates for any given table.
   * 
   * @param column_position
   * @throws IllegalArgumentException if column_position >= 100000000
   */
  public Colassoc setColumn_position(long column_position) {
    if (column_position >= 100000000L)
      throw new IllegalArgumentException(
          "column_position=" + column_position + " but cannot be >= 100000000");
    this.column_position = column_position;
    setHash(null);
    return this;
  }

  /**
   * Determines whether the NA value for this column is allowed in this table or not.
   * 
   * @return na_allowed
   */
  public String getNa_allowed() {
    return na_allowed;
  }

  /**
   * Determines whether the NA value for this column is allowed in this table or not.
   * 
   * @param na_allowed
   * @throws IllegalArgumentException if na_allowed.length() >= 1
   */
  public Colassoc setNa_allowed(String na_allowed) {
    if (na_allowed.length() > 1)
      throw new IllegalArgumentException(
          String.format("na_allowed.length() cannot be > 1.  na_allowed=%s", na_allowed));
    this.na_allowed = na_allowed;
    setHash(null);
    return this;
  }

  /**
   * Native key or identifier name as defined for the table where the key is not a foreign key. If
   * not foreign, then nativekeyname is 'ownedid!'; if the column is a multiple-key column where the
   * keyname for the numeric keys is given in a second column, nativekeyname is 'valueof:...' where
   * the name of the second column is provided in place of the ellipsis.
   * 
   * @return nativekeyname
   */
  public String getNativekeyname() {
    return nativekeyname;
  }

  /**
   * Native key or identifier name as defined for the table where the key is not a foreign key. If
   * not foreign, then nativekeyname is 'ownedid!'; if the column is a multiple-key column where the
   * keyname for the numeric keys is given in a second column, nativekeyname is 'valueof:...' where
   * the name of the second column is provided in place of the ellipsis.
   * 
   * @param nativekeyname
   * @throws IllegalArgumentException if nativekeyname.length() >= 30
   */
  public Colassoc setNativekeyname(String nativekeyname) {
    if (nativekeyname.length() > 30)
      throw new IllegalArgumentException(
          String.format("nativekeyname.length() cannot be > 30.  nativekeyname=%s", nativekeyname));
    this.nativekeyname = nativekeyname;
    setHash(null);
    return this;
  }

  /**
   * Schema name for schema to which table belongs that defines this key.
   * 
   * @return nativekeyschema
   */
  public String getNativekeyschema() {
    return nativekeyschema;
  }

  /**
   * Schema name for schema to which table belongs that defines this key.
   * 
   * @param nativekeyschema
   * @throws IllegalArgumentException if nativekeyschema.length() >= 30
   */
  public Colassoc setNativekeyschema(String nativekeyschema) {
    if (nativekeyschema.length() > 30)
      throw new IllegalArgumentException(String
          .format("nativekeyschema.length() cannot be > 30.  nativekeyschema=%s", nativekeyschema));
    this.nativekeyschema = nativekeyschema;
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
  public Colassoc setSchema_name(String schema_name) {
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
  public Colassoc setAuth(String auth) {
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
