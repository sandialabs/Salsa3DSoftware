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
 * table_defs_view
 */
public class Table_defs_view extends BaseRow implements Serializable {

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
   * Column position, order of column in table definition, must start at 1 and increment by one
   * without duplicates for any given table.
   */
  private long column_position;

  static final public long COLUMN_POSITION_NA = Long.MIN_VALUE;

  /**
   * native key or identifier name as defined for the table where the key is not a foreign key. If
   * not foreign, then nativekeyname is 'ownedid!'; if the column is a multiple-key column where the
   * keyname for the numeric keys is given in a second column, nativekeyname is 'valueof:...' where
   * the name of the second column is provided in place of the ellipsis.
   */
  private String key;

  static final public String KEY_NA = null;

  /**
   * External text format as would be used in read and write formats in programs. Of the form
   * [aife]length.precision, where a, i, f, e are ascii, integer, float, and exponential, length is
   * the total field width in characters, and precision is the number of digits to the right of the
   * decimal for numeric formats. For internal_format=date, the external format is the appropriate a
   * format, followed by a colon and then the oracle date format, as in "a19:YYYY/MM/DD HH24:MI:SS".
   * Note that e formats assume a leading sign, digit and decimal, then the number of digits right
   * of the decimal, then the e, a sign and a two-digit exponent.
   */
  private String external_format;

  static final public String EXTERNAL_FORMAT_NA = null;

  /**
   * External format width.
   * <p>
   * Units: byte
   */
  private long external_width;

  static final public long EXTERNAL_WIDTH_NA = Long.MIN_VALUE;

  /**
   * Internal storage format as given by Oracle, can be generated from Oracle data dictionary table
   * user_tab_columns via decode(data_type, 'NUMBER', 'NUMBER(' ||u.data_precision||')','FLOAT',
   * 'FLOAT(' ||u.data_precision||')','VARCHAR2', 'VARCHAR2('||u.data_length ||')',u.data_type).
   */
  private String internal_format;

  static final public String INTERNAL_FORMAT_NA = null;

  /**
   * schema name
   */
  private String schema;

  static final public String SCHEMA_NA = null;

  /**
   * Determines whether the NA value for this column is allowed in this table or not.
   */
  private String na_allowed;

  static final public String NA_ALLOWED_NA = null;

  /**
   * Value used for this column when not available or not applicable (if allowed).
   */
  private String na_value;

  static final public String NA_VALUE_NA = null;

  /**
   * Column type, the administrative role played by this column in this table.
   */
  private String column_type;

  static final public String COLUMN_TYPE_NA = null;

  /**
   * The data type that should be used to represent this value in a compiled computer language such
   * as java or c++. Typical values would be string, boolean, int, long, float or double. A value
   * such as bigdecimal(n,m) can also be used where n is the number of significant digits that
   * should be used to represent the number and m is the number of digits to the right of the
   * decimal point.
   */
  private String external_type;

  static final public String EXTERNAL_TYPE_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("table_name", Columns.FieldType.STRING, "%s");
    columns.add("column_name", Columns.FieldType.STRING, "%s");
    columns.add("column_position", Columns.FieldType.LONG, "%d");
    columns.add("key", Columns.FieldType.STRING, "%s");
    columns.add("external_format", Columns.FieldType.STRING, "%s");
    columns.add("external_width", Columns.FieldType.LONG, "%d");
    columns.add("internal_format", Columns.FieldType.STRING, "%s");
    columns.add("schema", Columns.FieldType.STRING, "%s");
    columns.add("na_allowed", Columns.FieldType.STRING, "%s");
    columns.add("na_value", Columns.FieldType.STRING, "%s");
    columns.add("column_type", Columns.FieldType.STRING, "%s");
    columns.add("external_type", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Table_defs_view(String table_name, String column_name, long column_position, String key,
      String external_format, long external_width, String internal_format, String schema,
      String na_allowed, String na_value, String column_type, String external_type) {
    setValues(table_name, column_name, column_position, key, external_format, external_width,
        internal_format, schema, na_allowed, na_value, column_type, external_type);
  }

  private void setValues(String table_name, String column_name, long column_position, String key,
      String external_format, long external_width, String internal_format, String schema,
      String na_allowed, String na_value, String column_type, String external_type) {
    this.table_name = table_name;
    this.column_name = column_name;
    this.column_position = column_position;
    this.key = key;
    this.external_format = external_format;
    this.external_width = external_width;
    this.internal_format = internal_format;
    this.schema = schema;
    this.na_allowed = na_allowed;
    this.na_value = na_value;
    this.column_type = column_type;
    this.external_type = external_type;
  }

  /**
   * Copy constructor.
   */
  public Table_defs_view(Table_defs_view other) {
    this.table_name = other.getTable_name();
    this.column_name = other.getColumn_name();
    this.column_position = other.getColumn_position();
    this.key = other.getKey();
    this.external_format = other.getExternal_format();
    this.external_width = other.getExternal_width();
    this.internal_format = other.getInternal_format();
    this.schema = other.getSchema();
    this.na_allowed = other.getNa_allowed();
    this.na_value = other.getNa_value();
    this.column_type = other.getColumn_type();
    this.external_type = other.getExternal_type();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Table_defs_view() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(TABLE_NAME_NA, COLUMN_NAME_NA, COLUMN_POSITION_NA, KEY_NA, EXTERNAL_FORMAT_NA,
        EXTERNAL_WIDTH_NA, INTERNAL_FORMAT_NA, SCHEMA_NA, NA_ALLOWED_NA, NA_VALUE_NA,
        COLUMN_TYPE_NA, EXTERNAL_TYPE_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "table_name":
        return table_name;
      case "column_name":
        return column_name;
      case "key":
        return key;
      case "external_format":
        return external_format;
      case "internal_format":
        return internal_format;
      case "schema":
        return schema;
      case "na_allowed":
        return na_allowed;
      case "na_value":
        return na_value;
      case "column_type":
        return column_type;
      case "external_type":
        return external_type;
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
      case "key":
        key = value;
        break;
      case "external_format":
        external_format = value;
        break;
      case "internal_format":
        internal_format = value;
        break;
      case "schema":
        schema = value;
        break;
      case "na_allowed":
        na_allowed = value;
        break;
      case "na_value":
        na_value = value;
        break;
      case "column_type":
        column_type = value;
        break;
      case "external_type":
        external_type = value;
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
      case "external_width":
        return external_width;
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
      case "external_width":
        external_width = value;
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
  public Table_defs_view(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Table_defs_view(DataInputStream input) throws IOException {
    this(readString(input), readString(input), input.readLong(), readString(input),
        readString(input), input.readLong(), readString(input), readString(input),
        readString(input), readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Table_defs_view(ByteBuffer input) {
    this(readString(input), readString(input), input.getLong(), readString(input),
        readString(input), input.getLong(), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Table_defs_view(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Table_defs_view(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getString(offset + 2), input.getLong(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5), input.getLong(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getString(offset + 9),
        input.getString(offset + 10), input.getString(offset + 11), input.getString(offset + 12));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[12];
    values[0] = table_name;
    values[1] = column_name;
    values[2] = column_position;
    values[3] = key;
    values[4] = external_format;
    values[5] = external_width;
    values[6] = internal_format;
    values[7] = schema;
    values[8] = na_allowed;
    values[9] = na_value;
    values[10] = column_type;
    values[11] = external_type;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[13];
    values[0] = table_name;
    values[1] = column_name;
    values[2] = column_position;
    values[3] = key;
    values[4] = external_format;
    values[5] = external_width;
    values[6] = internal_format;
    values[7] = schema;
    values[8] = na_allowed;
    values[9] = na_value;
    values[10] = column_type;
    values[11] = external_type;
    values[12] = lddate;
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
    output.writeLong(column_position);
    writeString(output, key);
    writeString(output, external_format);
    output.writeLong(external_width);
    writeString(output, internal_format);
    writeString(output, schema);
    writeString(output, na_allowed);
    writeString(output, na_value);
    writeString(output, column_type);
    writeString(output, external_type);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, table_name);
    writeString(output, column_name);
    output.putLong(column_position);
    writeString(output, key);
    writeString(output, external_format);
    output.putLong(external_width);
    writeString(output, internal_format);
    writeString(output, schema);
    writeString(output, na_allowed);
    writeString(output, na_value);
    writeString(output, column_type);
    writeString(output, external_type);
  }

  /**
   * Read a Collection of Table_defs_view objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Table_defs_view objects.
   * @throws IOException
   */
  static public void readTable_defs_views(BufferedReader input, Collection<Table_defs_view> rows)
      throws IOException {
    String[] saved = Table_defs_view.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Table_defs_view
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Table_defs_view(new Scanner(line)));
    }
    input.close();
    Table_defs_view.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Table_defs_view objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Table_defs_view objects.
   * @throws IOException
   */
  static public void readTable_defs_views(File inputFile, Collection<Table_defs_view> rows)
      throws IOException {
    readTable_defs_views(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Table_defs_view objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Table_defs_view objects.
   * @throws IOException
   */
  static public void readTable_defs_views(InputStream inputStream, Collection<Table_defs_view> rows)
      throws IOException {
    readTable_defs_views(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Table_defs_view objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Table_defs_view objects
   * @throws IOException
   */
  static public Set<Table_defs_view> readTable_defs_views(BufferedReader input) throws IOException {
    Set<Table_defs_view> rows = new LinkedHashSet<Table_defs_view>();
    readTable_defs_views(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Table_defs_view objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Table_defs_view objects
   * @throws IOException
   */
  static public Set<Table_defs_view> readTable_defs_views(File inputFile) throws IOException {
    return readTable_defs_views(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Table_defs_view objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Table_defs_view objects
   * @throws IOException
   */
  static public Set<Table_defs_view> readTable_defs_views(InputStream input) throws IOException {
    return readTable_defs_views(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Table_defs_view objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param table_defs_views the Table_defs_view objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Table_defs_view> table_defs_views)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Table_defs_view table_defs_view : table_defs_views)
      table_defs_view.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Table_defs_view objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param table_defs_views the Table_defs_view objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Table_defs_view> table_defs_views, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Table_defs_view table_defs_view : table_defs_views) {
        int i = 0;
        statement.setString(++i, table_defs_view.table_name);
        statement.setString(++i, table_defs_view.column_name);
        statement.setLong(++i, table_defs_view.column_position);
        statement.setString(++i, table_defs_view.key);
        statement.setString(++i, table_defs_view.external_format);
        statement.setLong(++i, table_defs_view.external_width);
        statement.setString(++i, table_defs_view.internal_format);
        statement.setString(++i, table_defs_view.schema);
        statement.setString(++i, table_defs_view.na_allowed);
        statement.setString(++i, table_defs_view.na_value);
        statement.setString(++i, table_defs_view.column_type);
        statement.setString(++i, table_defs_view.external_type);
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
   *        Table_defs_view table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Table_defs_view> readTable_defs_views(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Table_defs_view> results = new HashSet<Table_defs_view>();
    readTable_defs_views(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Table_defs_view table.
   * @param table_defs_views
   * @throws SQLException
   */
  static public void readTable_defs_views(Connection connection, String selectStatement,
      Set<Table_defs_view> table_defs_views) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        table_defs_views.add(new Table_defs_view(rs));
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
   * this Table_defs_view object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Table_defs_view object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "table_name, column_name, column_position, key, external_format, external_width, internal_format, schema, na_allowed, na_value, column_type, external_type, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(table_name).append("', ");
    sql.append("'").append(column_name).append("', ");
    sql.append(Long.toString(column_position)).append(", ");
    sql.append("'").append(key).append("', ");
    sql.append("'").append(external_format).append("', ");
    sql.append(Long.toString(external_width)).append(", ");
    sql.append("'").append(internal_format).append("', ");
    sql.append("'").append(schema).append("', ");
    sql.append("'").append(na_allowed).append("', ");
    sql.append("'").append(na_value).append("', ");
    sql.append("'").append(column_type).append("', ");
    sql.append("'").append(external_type).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Table_defs_view in the database. Primary and unique keys are set, if
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
   * Create a table of type Table_defs_view in the database
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
   * Generate a sql script to create a table of type Table_defs_view in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Table_defs_view in the database
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
    buf.append("column_position number(8)            NOT NULL,\n");
    buf.append("key          varchar2(30)         NOT NULL,\n");
    buf.append("external_format varchar2(30)         NOT NULL,\n");
    buf.append("external_width number(8)            NOT NULL,\n");
    buf.append("internal_format varchar2(30)         NOT NULL,\n");
    buf.append("schema       varchar2(30)         NOT NULL,\n");
    buf.append("na_allowed   varchar2(1)          NOT NULL,\n");
    buf.append("na_value     varchar2(80)         NOT NULL,\n");
    buf.append("column_type  varchar2(30)         NOT NULL,\n");
    buf.append("external_type varchar2(30)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (table_name,column_name)");
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
    return 377;
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
    return (other instanceof Table_defs_view)
        && ((Table_defs_view) other).table_name.equals(table_name)
        && ((Table_defs_view) other).column_name.equals(column_name);
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
  public Table_defs_view setTable_name(String table_name) {
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
  public Table_defs_view setColumn_name(String column_name) {
    if (column_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("column_name.length() cannot be > 30.  column_name=%s", column_name));
    this.column_name = column_name;
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
  public Table_defs_view setColumn_position(long column_position) {
    if (column_position >= 100000000L)
      throw new IllegalArgumentException(
          "column_position=" + column_position + " but cannot be >= 100000000");
    this.column_position = column_position;
    setHash(null);
    return this;
  }

  /**
   * native key or identifier name as defined for the table where the key is not a foreign key. If
   * not foreign, then nativekeyname is 'ownedid!'; if the column is a multiple-key column where the
   * keyname for the numeric keys is given in a second column, nativekeyname is 'valueof:...' where
   * the name of the second column is provided in place of the ellipsis.
   * 
   * @return key
   */
  public String getKey() {
    return key;
  }

  /**
   * native key or identifier name as defined for the table where the key is not a foreign key. If
   * not foreign, then nativekeyname is 'ownedid!'; if the column is a multiple-key column where the
   * keyname for the numeric keys is given in a second column, nativekeyname is 'valueof:...' where
   * the name of the second column is provided in place of the ellipsis.
   * 
   * @param key
   * @throws IllegalArgumentException if key.length() >= 30
   */
  public Table_defs_view setKey(String key) {
    if (key.length() > 30)
      throw new IllegalArgumentException(
          String.format("key.length() cannot be > 30.  key=%s", key));
    this.key = key;
    setHash(null);
    return this;
  }

  /**
   * External text format as would be used in read and write formats in programs. Of the form
   * [aife]length.precision, where a, i, f, e are ascii, integer, float, and exponential, length is
   * the total field width in characters, and precision is the number of digits to the right of the
   * decimal for numeric formats. For internal_format=date, the external format is the appropriate a
   * format, followed by a colon and then the oracle date format, as in "a19:YYYY/MM/DD HH24:MI:SS".
   * Note that e formats assume a leading sign, digit and decimal, then the number of digits right
   * of the decimal, then the e, a sign and a two-digit exponent.
   * 
   * @return external_format
   */
  public String getExternal_format() {
    return external_format;
  }

  /**
   * External text format as would be used in read and write formats in programs. Of the form
   * [aife]length.precision, where a, i, f, e are ascii, integer, float, and exponential, length is
   * the total field width in characters, and precision is the number of digits to the right of the
   * decimal for numeric formats. For internal_format=date, the external format is the appropriate a
   * format, followed by a colon and then the oracle date format, as in "a19:YYYY/MM/DD HH24:MI:SS".
   * Note that e formats assume a leading sign, digit and decimal, then the number of digits right
   * of the decimal, then the e, a sign and a two-digit exponent.
   * 
   * @param external_format
   * @throws IllegalArgumentException if external_format.length() >= 30
   */
  public Table_defs_view setExternal_format(String external_format) {
    if (external_format.length() > 30)
      throw new IllegalArgumentException(String
          .format("external_format.length() cannot be > 30.  external_format=%s", external_format));
    this.external_format = external_format;
    setHash(null);
    return this;
  }

  /**
   * External format width.
   * <p>
   * Units: byte
   * 
   * @return external_width
   */
  public long getExternal_width() {
    return external_width;
  }

  /**
   * External format width.
   * <p>
   * Units: byte
   * 
   * @param external_width
   * @throws IllegalArgumentException if external_width >= 100000000
   */
  public Table_defs_view setExternal_width(long external_width) {
    if (external_width >= 100000000L)
      throw new IllegalArgumentException(
          "external_width=" + external_width + " but cannot be >= 100000000");
    this.external_width = external_width;
    setHash(null);
    return this;
  }

  /**
   * Internal storage format as given by Oracle, can be generated from Oracle data dictionary table
   * user_tab_columns via decode(data_type, 'NUMBER', 'NUMBER(' ||u.data_precision||')','FLOAT',
   * 'FLOAT(' ||u.data_precision||')','VARCHAR2', 'VARCHAR2('||u.data_length ||')',u.data_type).
   * 
   * @return internal_format
   */
  public String getInternal_format() {
    return internal_format;
  }

  /**
   * Internal storage format as given by Oracle, can be generated from Oracle data dictionary table
   * user_tab_columns via decode(data_type, 'NUMBER', 'NUMBER(' ||u.data_precision||')','FLOAT',
   * 'FLOAT(' ||u.data_precision||')','VARCHAR2', 'VARCHAR2('||u.data_length ||')',u.data_type).
   * 
   * @param internal_format
   * @throws IllegalArgumentException if internal_format.length() >= 30
   */
  public Table_defs_view setInternal_format(String internal_format) {
    if (internal_format.length() > 30)
      throw new IllegalArgumentException(String
          .format("internal_format.length() cannot be > 30.  internal_format=%s", internal_format));
    this.internal_format = internal_format;
    setHash(null);
    return this;
  }

  /**
   * schema name
   * 
   * @return schema
   */
  public String getSchema() {
    return schema;
  }

  /**
   * schema name
   * 
   * @param schema
   * @throws IllegalArgumentException if schema.length() >= 30
   */
  public Table_defs_view setSchema(String schema) {
    if (schema.length() > 30)
      throw new IllegalArgumentException(
          String.format("schema.length() cannot be > 30.  schema=%s", schema));
    this.schema = schema;
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
  public Table_defs_view setNa_allowed(String na_allowed) {
    if (na_allowed.length() > 1)
      throw new IllegalArgumentException(
          String.format("na_allowed.length() cannot be > 1.  na_allowed=%s", na_allowed));
    this.na_allowed = na_allowed;
    setHash(null);
    return this;
  }

  /**
   * Value used for this column when not available or not applicable (if allowed).
   * 
   * @return na_value
   */
  public String getNa_value() {
    return na_value;
  }

  /**
   * Value used for this column when not available or not applicable (if allowed).
   * 
   * @param na_value
   * @throws IllegalArgumentException if na_value.length() >= 80
   */
  public Table_defs_view setNa_value(String na_value) {
    if (na_value.length() > 80)
      throw new IllegalArgumentException(
          String.format("na_value.length() cannot be > 80.  na_value=%s", na_value));
    this.na_value = na_value;
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
  public Table_defs_view setColumn_type(String column_type) {
    if (column_type.length() > 30)
      throw new IllegalArgumentException(
          String.format("column_type.length() cannot be > 30.  column_type=%s", column_type));
    this.column_type = column_type;
    setHash(null);
    return this;
  }

  /**
   * The data type that should be used to represent this value in a compiled computer language such
   * as java or c++. Typical values would be string, boolean, int, long, float or double. A value
   * such as bigdecimal(n,m) can also be used where n is the number of significant digits that
   * should be used to represent the number and m is the number of digits to the right of the
   * decimal point.
   * 
   * @return external_type
   */
  public String getExternal_type() {
    return external_type;
  }

  /**
   * The data type that should be used to represent this value in a compiled computer language such
   * as java or c++. Typical values would be string, boolean, int, long, float or double. A value
   * such as bigdecimal(n,m) can also be used where n is the number of significant digits that
   * should be used to represent the number and m is the number of digits to the right of the
   * decimal point.
   * 
   * @param external_type
   * @throws IllegalArgumentException if external_type.length() >= 30
   */
  public Table_defs_view setExternal_type(String external_type) {
    if (external_type.length() > 30)
      throw new IllegalArgumentException(
          String.format("external_type.length() cannot be > 30.  external_type=%s", external_type));
    this.external_type = external_type;
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
