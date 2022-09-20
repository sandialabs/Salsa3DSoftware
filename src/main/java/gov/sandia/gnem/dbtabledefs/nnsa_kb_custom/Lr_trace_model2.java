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
 * lr_trace_model2
 */
public class Lr_trace_model2 extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Model number.
   */
  private long model;

  static final public long MODEL_NA = Long.MIN_VALUE;

  /**
   * Instantaneous period.
   * <p>
   * Units: s
   */
  private double period;

  static final public double PERIOD_NA = -1;

  /**
   * Predicted group velocity.
   * <p>
   * Units: km/s
   */
  private double velocity;

  static final public double VELOCITY_NA = -1;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = "-";


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("model", Columns.FieldType.LONG, "%d");
    columns.add("period", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("velocity", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Lr_trace_model2(long model, double period, double velocity, String auth) {
    setValues(model, period, velocity, auth);
  }

  private void setValues(long model, double period, double velocity, String auth) {
    this.model = model;
    this.period = period;
    this.velocity = velocity;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Lr_trace_model2(Lr_trace_model2 other) {
    this.model = other.getModel();
    this.period = other.getPeriod();
    this.velocity = other.getVelocity();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Lr_trace_model2() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MODEL_NA, PERIOD_NA, VELOCITY_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
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
      case "period":
        return period;
      case "velocity":
        return velocity;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "period":
        period = value;
        break;
      case "velocity":
        velocity = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "model":
        return model;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "model":
        model = value;
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
  public Lr_trace_model2(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Lr_trace_model2(DataInputStream input) throws IOException {
    this(input.readLong(), input.readDouble(), input.readDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Lr_trace_model2(ByteBuffer input) {
    this(input.getLong(), input.getDouble(), input.getDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Lr_trace_model2(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Lr_trace_model2(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getString(offset + 4));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[4];
    values[0] = model;
    values[1] = period;
    values[2] = velocity;
    values[3] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[5];
    values[0] = model;
    values[1] = period;
    values[2] = velocity;
    values[3] = auth;
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
    output.writeLong(model);
    output.writeDouble(period);
    output.writeDouble(velocity);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(model);
    output.putDouble(period);
    output.putDouble(velocity);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Lr_trace_model2 objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Lr_trace_model2 objects.
   * @throws IOException
   */
  static public void readLr_trace_model2s(BufferedReader input, Collection<Lr_trace_model2> rows)
      throws IOException {
    String[] saved = Lr_trace_model2.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Lr_trace_model2
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Lr_trace_model2(new Scanner(line)));
    }
    input.close();
    Lr_trace_model2.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Lr_trace_model2 objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Lr_trace_model2 objects.
   * @throws IOException
   */
  static public void readLr_trace_model2s(File inputFile, Collection<Lr_trace_model2> rows)
      throws IOException {
    readLr_trace_model2s(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Lr_trace_model2 objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Lr_trace_model2 objects.
   * @throws IOException
   */
  static public void readLr_trace_model2s(InputStream inputStream, Collection<Lr_trace_model2> rows)
      throws IOException {
    readLr_trace_model2s(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Lr_trace_model2 objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Lr_trace_model2 objects
   * @throws IOException
   */
  static public Set<Lr_trace_model2> readLr_trace_model2s(BufferedReader input) throws IOException {
    Set<Lr_trace_model2> rows = new LinkedHashSet<Lr_trace_model2>();
    readLr_trace_model2s(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Lr_trace_model2 objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Lr_trace_model2 objects
   * @throws IOException
   */
  static public Set<Lr_trace_model2> readLr_trace_model2s(File inputFile) throws IOException {
    return readLr_trace_model2s(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Lr_trace_model2 objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Lr_trace_model2 objects
   * @throws IOException
   */
  static public Set<Lr_trace_model2> readLr_trace_model2s(InputStream input) throws IOException {
    return readLr_trace_model2s(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Lr_trace_model2 objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param lr_trace_model2s the Lr_trace_model2 objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Lr_trace_model2> lr_trace_model2s)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Lr_trace_model2 lr_trace_model2 : lr_trace_model2s)
      lr_trace_model2.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Lr_trace_model2 objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param lr_trace_model2s the Lr_trace_model2 objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Lr_trace_model2> lr_trace_model2s, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?)");
      for (Lr_trace_model2 lr_trace_model2 : lr_trace_model2s) {
        int i = 0;
        statement.setLong(++i, lr_trace_model2.model);
        statement.setDouble(++i, lr_trace_model2.period);
        statement.setDouble(++i, lr_trace_model2.velocity);
        statement.setString(++i, lr_trace_model2.auth);
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
   *        Lr_trace_model2 table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Lr_trace_model2> readLr_trace_model2s(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Lr_trace_model2> results = new HashSet<Lr_trace_model2>();
    readLr_trace_model2s(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Lr_trace_model2 table.
   * @param lr_trace_model2s
   * @throws SQLException
   */
  static public void readLr_trace_model2s(Connection connection, String selectStatement,
      Set<Lr_trace_model2> lr_trace_model2s) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        lr_trace_model2s.add(new Lr_trace_model2(rs));
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
   * this Lr_trace_model2 object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Lr_trace_model2 object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("model, period, velocity, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(model)).append(", ");
    sql.append(Double.toString(period)).append(", ");
    sql.append(Double.toString(velocity)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Lr_trace_model2 in the database. Primary and unique keys are set, if
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
   * Create a table of type Lr_trace_model2 in the database
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
   * Generate a sql script to create a table of type Lr_trace_model2 in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Lr_trace_model2 in the database
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
    buf.append("model        number(8)            NOT NULL,\n");
    buf.append("period       float(24)            NOT NULL,\n");
    buf.append("velocity     float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (model,period)");
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
    return 48;
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
    return (other instanceof Lr_trace_model2) && ((Lr_trace_model2) other).model == model
        && ((Lr_trace_model2) other).period == period;
  }

  /**
   * Model number.
   * 
   * @return model
   */
  public long getModel() {
    return model;
  }

  /**
   * Model number.
   * 
   * @param model
   * @throws IllegalArgumentException if model >= 100000000
   */
  public Lr_trace_model2 setModel(long model) {
    if (model >= 100000000L)
      throw new IllegalArgumentException("model=" + model + " but cannot be >= 100000000");
    this.model = model;
    setHash(null);
    return this;
  }

  /**
   * Instantaneous period.
   * <p>
   * Units: s
   * 
   * @return period
   */
  public double getPeriod() {
    return period;
  }

  /**
   * Instantaneous period.
   * <p>
   * Units: s
   * 
   * @param period
   */
  public Lr_trace_model2 setPeriod(double period) {
    this.period = period;
    setHash(null);
    return this;
  }

  /**
   * Predicted group velocity.
   * <p>
   * Units: km/s
   * 
   * @return velocity
   */
  public double getVelocity() {
    return velocity;
  }

  /**
   * Predicted group velocity.
   * <p>
   * Units: km/s
   * 
   * @param velocity
   */
  public Lr_trace_model2 setVelocity(double velocity) {
    this.velocity = velocity;
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
  public Lr_trace_model2 setAuth(String auth) {
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
