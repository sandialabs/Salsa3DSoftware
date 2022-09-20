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
 * remap_evloader
 */
public class Remap_evloader extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Source of the catalog information, e.g. author name.
   */
  private String source;

  static final public String SOURCE_NA = null;

  /**
   * Identifier name (<I>orid</I>, <I>evid</I>, <I>arid</I>, <I>magid</I>, <I>wfid</I>, etc.).
   */
  private String id_name;

  static final public String ID_NAME_NA = null;

  /**
   * Value of identifier in the source catalog table.
   */
  private long original_id;

  static final public long ORIGINAL_ID_NA = Long.MIN_VALUE;

  /**
   * Value of the identifier in the merged catalog table.
   */
  private long current_id;

  static final public long CURRENT_ID_NA = Long.MIN_VALUE;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("source", Columns.FieldType.STRING, "%s");
    columns.add("id_name", Columns.FieldType.STRING, "%s");
    columns.add("original_id", Columns.FieldType.LONG, "%d");
    columns.add("current_id", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Remap_evloader(String source, String id_name, long original_id, long current_id) {
    setValues(source, id_name, original_id, current_id);
  }

  private void setValues(String source, String id_name, long original_id, long current_id) {
    this.source = source;
    this.id_name = id_name;
    this.original_id = original_id;
    this.current_id = current_id;
  }

  /**
   * Copy constructor.
   */
  public Remap_evloader(Remap_evloader other) {
    this.source = other.getSource();
    this.id_name = other.getId_name();
    this.original_id = other.getOriginal_id();
    this.current_id = other.getCurrent_id();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Remap_evloader() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(SOURCE_NA, ID_NAME_NA, ORIGINAL_ID_NA, CURRENT_ID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "source":
        return source;
      case "id_name":
        return id_name;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "source":
        source = value;
        break;
      case "id_name":
        id_name = value;
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
      case "original_id":
        return original_id;
      case "current_id":
        return current_id;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "original_id":
        original_id = value;
        break;
      case "current_id":
        current_id = value;
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
  public Remap_evloader(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Remap_evloader(DataInputStream input) throws IOException {
    this(readString(input), readString(input), input.readLong(), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Remap_evloader(ByteBuffer input) {
    this(readString(input), readString(input), input.getLong(), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Remap_evloader(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Remap_evloader(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getString(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[4];
    values[0] = source;
    values[1] = id_name;
    values[2] = original_id;
    values[3] = current_id;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[5];
    values[0] = source;
    values[1] = id_name;
    values[2] = original_id;
    values[3] = current_id;
    values[4] = lddate;
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
    writeString(output, source);
    writeString(output, id_name);
    output.writeLong(original_id);
    output.writeLong(current_id);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, source);
    writeString(output, id_name);
    output.putLong(original_id);
    output.putLong(current_id);
  }

  /**
   * Read a Collection of Remap_evloader objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Remap_evloader objects.
   * @throws IOException
   */
  static public void readRemap_evloaders(BufferedReader input, Collection<Remap_evloader> rows)
      throws IOException {
    String[] saved = Remap_evloader.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Remap_evloader
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Remap_evloader(new Scanner(line)));
    }
    input.close();
    Remap_evloader.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Remap_evloader objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Remap_evloader objects.
   * @throws IOException
   */
  static public void readRemap_evloaders(File inputFile, Collection<Remap_evloader> rows)
      throws IOException {
    readRemap_evloaders(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Remap_evloader objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Remap_evloader objects.
   * @throws IOException
   */
  static public void readRemap_evloaders(InputStream inputStream, Collection<Remap_evloader> rows)
      throws IOException {
    readRemap_evloaders(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Remap_evloader objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Remap_evloader objects
   * @throws IOException
   */
  static public Set<Remap_evloader> readRemap_evloaders(BufferedReader input) throws IOException {
    Set<Remap_evloader> rows = new LinkedHashSet<Remap_evloader>();
    readRemap_evloaders(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Remap_evloader objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Remap_evloader objects
   * @throws IOException
   */
  static public Set<Remap_evloader> readRemap_evloaders(File inputFile) throws IOException {
    return readRemap_evloaders(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Remap_evloader objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Remap_evloader objects
   * @throws IOException
   */
  static public Set<Remap_evloader> readRemap_evloaders(InputStream input) throws IOException {
    return readRemap_evloaders(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Remap_evloader objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param remap_evloaders the Remap_evloader objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Remap_evloader> remap_evloaders)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Remap_evloader remap_evloader : remap_evloaders)
      remap_evloader.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Remap_evloader objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param remap_evloaders the Remap_evloader objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Remap_evloader> remap_evloaders, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?)");
      for (Remap_evloader remap_evloader : remap_evloaders) {
        int i = 0;
        statement.setString(++i, remap_evloader.source);
        statement.setString(++i, remap_evloader.id_name);
        statement.setLong(++i, remap_evloader.original_id);
        statement.setLong(++i, remap_evloader.current_id);
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
   *        Remap_evloader table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Remap_evloader> readRemap_evloaders(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Remap_evloader> results = new HashSet<Remap_evloader>();
    readRemap_evloaders(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Remap_evloader table.
   * @param remap_evloaders
   * @throws SQLException
   */
  static public void readRemap_evloaders(Connection connection, String selectStatement,
      Set<Remap_evloader> remap_evloaders) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        remap_evloaders.add(new Remap_evloader(rs));
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
   * this Remap_evloader object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Remap_evloader object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("source, id_name, original_id, current_id, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(source).append("', ");
    sql.append("'").append(id_name).append("', ");
    sql.append(Long.toString(original_id)).append(", ");
    sql.append(Long.toString(current_id)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Remap_evloader in the database. Primary and unique keys are set, if
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
   * Create a table of type Remap_evloader in the database
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
   * Generate a sql script to create a table of type Remap_evloader in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Remap_evloader in the database
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
    buf.append("source       varchar2(512)        NOT NULL,\n");
    buf.append("id_name      varchar2(12)         NOT NULL,\n");
    buf.append("original_id  number(9)            NOT NULL,\n");
    buf.append("current_id   number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
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
    return 548;
  }

  /**
   * Source of the catalog information, e.g. author name.
   * 
   * @return source
   */
  public String getSource() {
    return source;
  }

  /**
   * Source of the catalog information, e.g. author name.
   * 
   * @param source
   * @throws IllegalArgumentException if source.length() >= 512
   */
  public Remap_evloader setSource(String source) {
    if (source.length() > 512)
      throw new IllegalArgumentException(
          String.format("source.length() cannot be > 512.  source=%s", source));
    this.source = source;
    setHash(null);
    return this;
  }

  /**
   * Identifier name (<I>orid</I>, <I>evid</I>, <I>arid</I>, <I>magid</I>, <I>wfid</I>, etc.).
   * 
   * @return id_name
   */
  public String getId_name() {
    return id_name;
  }

  /**
   * Identifier name (<I>orid</I>, <I>evid</I>, <I>arid</I>, <I>magid</I>, <I>wfid</I>, etc.).
   * 
   * @param id_name
   * @throws IllegalArgumentException if id_name.length() >= 12
   */
  public Remap_evloader setId_name(String id_name) {
    if (id_name.length() > 12)
      throw new IllegalArgumentException(
          String.format("id_name.length() cannot be > 12.  id_name=%s", id_name));
    this.id_name = id_name;
    setHash(null);
    return this;
  }

  /**
   * Value of identifier in the source catalog table.
   * 
   * @return original_id
   */
  public long getOriginal_id() {
    return original_id;
  }

  /**
   * Value of identifier in the source catalog table.
   * 
   * @param original_id
   * @throws IllegalArgumentException if original_id >= 1000000000
   */
  public Remap_evloader setOriginal_id(long original_id) {
    if (original_id >= 1000000000L)
      throw new IllegalArgumentException(
          "original_id=" + original_id + " but cannot be >= 1000000000");
    this.original_id = original_id;
    setHash(null);
    return this;
  }

  /**
   * Value of the identifier in the merged catalog table.
   * 
   * @return current_id
   */
  public long getCurrent_id() {
    return current_id;
  }

  /**
   * Value of the identifier in the merged catalog table.
   * 
   * @param current_id
   * @throws IllegalArgumentException if current_id >= 1000000000
   */
  public Remap_evloader setCurrent_id(long current_id) {
    if (current_id >= 1000000000L)
      throw new IllegalArgumentException(
          "current_id=" + current_id + " but cannot be >= 1000000000");
    this.current_id = current_id;
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
