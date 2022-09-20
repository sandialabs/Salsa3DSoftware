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
package gov.sandia.gnem.dbtabledefs.gmp;

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
 * ?
 */
public class Tomostats extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * -
   */
  private long tomostatsid;

  static final public long TOMOSTATSID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long tomorunid;

  static final public long TOMORUNID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long geomodelid;

  static final public long GEOMODELID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private String name;

  static final public String NAME_NA = null;

  /**
   * -
   */
  private double minimum;

  static final public double MINIMUM_NA = Double.NaN;

  /**
   * -
   */
  private double maximum;

  static final public double MAXIMUM_NA = Double.NaN;

  /**
   * -
   */
  private double mean;

  static final public double MEAN_NA = Double.NaN;

  /**
   * -
   */
  private double rms;

  static final public double RMS_NA = Double.NaN;

  /**
   * -
   */
  private double stddev;

  static final public double STDDEV_NA = Double.NaN;

  /**
   * -
   */
  private long count;

  static final public long COUNT_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private String units;

  static final public String UNITS_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("tomostatsid", Columns.FieldType.LONG, "%d");
    columns.add("tomorunid", Columns.FieldType.LONG, "%d");
    columns.add("geomodelid", Columns.FieldType.LONG, "%d");
    columns.add("name", Columns.FieldType.STRING, "%s");
    columns.add("minimum", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("maximum", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("mean", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("rms", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("stddev", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("count", Columns.FieldType.LONG, "%d");
    columns.add("units", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Tomostats(long tomostatsid, long tomorunid, long geomodelid, String name, double minimum,
      double maximum, double mean, double rms, double stddev, long count, String units) {
    setValues(tomostatsid, tomorunid, geomodelid, name, minimum, maximum, mean, rms, stddev, count,
        units);
  }

  private void setValues(long tomostatsid, long tomorunid, long geomodelid, String name,
      double minimum, double maximum, double mean, double rms, double stddev, long count,
      String units) {
    this.tomostatsid = tomostatsid;
    this.tomorunid = tomorunid;
    this.geomodelid = geomodelid;
    this.name = name;
    this.minimum = minimum;
    this.maximum = maximum;
    this.mean = mean;
    this.rms = rms;
    this.stddev = stddev;
    this.count = count;
    this.units = units;
  }

  /**
   * Copy constructor.
   */
  public Tomostats(Tomostats other) {
    this.tomostatsid = other.getTomostatsid();
    this.tomorunid = other.getTomorunid();
    this.geomodelid = other.getGeomodelid();
    this.name = other.getName();
    this.minimum = other.getMinimum();
    this.maximum = other.getMaximum();
    this.mean = other.getMean();
    this.rms = other.getRms();
    this.stddev = other.getStddev();
    this.count = other.getCount();
    this.units = other.getUnits();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Tomostats() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(TOMOSTATSID_NA, TOMORUNID_NA, GEOMODELID_NA, NAME_NA, MINIMUM_NA, MAXIMUM_NA, MEAN_NA,
        RMS_NA, STDDEV_NA, COUNT_NA, UNITS_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "name":
        return name;
      case "units":
        return units;
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
      case "units":
        units = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "minimum":
        return minimum;
      case "maximum":
        return maximum;
      case "mean":
        return mean;
      case "rms":
        return rms;
      case "stddev":
        return stddev;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "minimum":
        minimum = value;
        break;
      case "maximum":
        maximum = value;
        break;
      case "mean":
        mean = value;
        break;
      case "rms":
        rms = value;
        break;
      case "stddev":
        stddev = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "tomostatsid":
        return tomostatsid;
      case "tomorunid":
        return tomorunid;
      case "geomodelid":
        return geomodelid;
      case "count":
        return count;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "tomostatsid":
        tomostatsid = value;
        break;
      case "tomorunid":
        tomorunid = value;
        break;
      case "geomodelid":
        geomodelid = value;
        break;
      case "count":
        count = value;
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
  public Tomostats(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Tomostats(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), readString(input),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Tomostats(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(), input.getLong(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Tomostats(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Tomostats(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getString(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getLong(offset + 10), input.getString(offset + 11));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[11];
    values[0] = tomostatsid;
    values[1] = tomorunid;
    values[2] = geomodelid;
    values[3] = name;
    values[4] = minimum;
    values[5] = maximum;
    values[6] = mean;
    values[7] = rms;
    values[8] = stddev;
    values[9] = count;
    values[10] = units;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[12];
    values[0] = tomostatsid;
    values[1] = tomorunid;
    values[2] = geomodelid;
    values[3] = name;
    values[4] = minimum;
    values[5] = maximum;
    values[6] = mean;
    values[7] = rms;
    values[8] = stddev;
    values[9] = count;
    values[10] = units;
    values[11] = lddate;
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
    output.writeLong(tomostatsid);
    output.writeLong(tomorunid);
    output.writeLong(geomodelid);
    writeString(output, name);
    output.writeDouble(minimum);
    output.writeDouble(maximum);
    output.writeDouble(mean);
    output.writeDouble(rms);
    output.writeDouble(stddev);
    output.writeLong(count);
    writeString(output, units);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(tomostatsid);
    output.putLong(tomorunid);
    output.putLong(geomodelid);
    writeString(output, name);
    output.putDouble(minimum);
    output.putDouble(maximum);
    output.putDouble(mean);
    output.putDouble(rms);
    output.putDouble(stddev);
    output.putLong(count);
    writeString(output, units);
  }

  /**
   * Read a Collection of Tomostats objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Tomostats objects.
   * @throws IOException
   */
  static public void readTomostatss(BufferedReader input, Collection<Tomostats> rows)
      throws IOException {
    String[] saved = Tomostats.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Tomostats
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Tomostats(new Scanner(line)));
    }
    input.close();
    Tomostats.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Tomostats objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Tomostats objects.
   * @throws IOException
   */
  static public void readTomostatss(File inputFile, Collection<Tomostats> rows) throws IOException {
    readTomostatss(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Tomostats objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Tomostats objects.
   * @throws IOException
   */
  static public void readTomostatss(InputStream inputStream, Collection<Tomostats> rows)
      throws IOException {
    readTomostatss(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Tomostats objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Tomostats objects
   * @throws IOException
   */
  static public Set<Tomostats> readTomostatss(BufferedReader input) throws IOException {
    Set<Tomostats> rows = new LinkedHashSet<Tomostats>();
    readTomostatss(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Tomostats objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Tomostats objects
   * @throws IOException
   */
  static public Set<Tomostats> readTomostatss(File inputFile) throws IOException {
    return readTomostatss(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Tomostats objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Tomostats objects
   * @throws IOException
   */
  static public Set<Tomostats> readTomostatss(InputStream input) throws IOException {
    return readTomostatss(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Tomostats objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param tomostatss the Tomostats objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Tomostats> tomostatss)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Tomostats tomostats : tomostatss)
      tomostats.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Tomostats objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param tomostatss the Tomostats objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Tomostats> tomostatss, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Tomostats tomostats : tomostatss) {
        int i = 0;
        statement.setLong(++i, tomostats.tomostatsid);
        statement.setLong(++i, tomostats.tomorunid);
        statement.setLong(++i, tomostats.geomodelid);
        statement.setString(++i, tomostats.name);
        statement.setDouble(++i, tomostats.minimum);
        statement.setDouble(++i, tomostats.maximum);
        statement.setDouble(++i, tomostats.mean);
        statement.setDouble(++i, tomostats.rms);
        statement.setDouble(++i, tomostats.stddev);
        statement.setLong(++i, tomostats.count);
        statement.setString(++i, tomostats.units);
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
   *        Tomostats table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Tomostats> readTomostatss(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Tomostats> results = new HashSet<Tomostats>();
    readTomostatss(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Tomostats table.
   * @param tomostatss
   * @throws SQLException
   */
  static public void readTomostatss(Connection connection, String selectStatement,
      Set<Tomostats> tomostatss) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        tomostatss.add(new Tomostats(rs));
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
   * this Tomostats object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Tomostats object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "tomostatsid, tomorunid, geomodelid, name, minimum, maximum, mean, rms, stddev, count, units, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(tomostatsid)).append(", ");
    sql.append(Long.toString(tomorunid)).append(", ");
    sql.append(Long.toString(geomodelid)).append(", ");
    sql.append("'").append(name).append("', ");
    sql.append(Double.toString(minimum)).append(", ");
    sql.append(Double.toString(maximum)).append(", ");
    sql.append(Double.toString(mean)).append(", ");
    sql.append(Double.toString(rms)).append(", ");
    sql.append(Double.toString(stddev)).append(", ");
    sql.append(Long.toString(count)).append(", ");
    sql.append("'").append(units).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Tomostats in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Tomostats in the database
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
   * Generate a sql script to create a table of type Tomostats in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Tomostats in the database
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
    buf.append("tomostatsid  number(10)           NOT NULL,\n");
    buf.append("tomorunid    number(10)           NOT NULL,\n");
    buf.append("geomodelid   number(10)           NOT NULL,\n");
    buf.append("name         varchar2(64)         NOT NULL,\n");
    buf.append("minimum      float(126)           NOT NULL,\n");
    buf.append("maximum      float(126)           NOT NULL,\n");
    buf.append("mean         float(126)           NOT NULL,\n");
    buf.append("rms          float(126)           NOT NULL,\n");
    buf.append("stddev       float(126)           NOT NULL,\n");
    buf.append("count        number(10)           NOT NULL,\n");
    buf.append("units        varchar2(64)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (tomostatsid)");
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
    return 208;
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
    return (other instanceof Tomostats) && ((Tomostats) other).tomostatsid == tomostatsid;
  }

  /**
   * -
   * 
   * @return tomostatsid
   */
  public long getTomostatsid() {
    return tomostatsid;
  }

  /**
   * -
   * 
   * @param tomostatsid
   * @throws IllegalArgumentException if tomostatsid >= 10000000000
   */
  public Tomostats setTomostatsid(long tomostatsid) {
    if (tomostatsid >= 10000000000L)
      throw new IllegalArgumentException(
          "tomostatsid=" + tomostatsid + " but cannot be >= 10000000000");
    this.tomostatsid = tomostatsid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return tomorunid
   */
  public long getTomorunid() {
    return tomorunid;
  }

  /**
   * -
   * 
   * @param tomorunid
   * @throws IllegalArgumentException if tomorunid >= 10000000000
   */
  public Tomostats setTomorunid(long tomorunid) {
    if (tomorunid >= 10000000000L)
      throw new IllegalArgumentException(
          "tomorunid=" + tomorunid + " but cannot be >= 10000000000");
    this.tomorunid = tomorunid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return geomodelid
   */
  public long getGeomodelid() {
    return geomodelid;
  }

  /**
   * -
   * 
   * @param geomodelid
   * @throws IllegalArgumentException if geomodelid >= 10000000000
   */
  public Tomostats setGeomodelid(long geomodelid) {
    if (geomodelid >= 10000000000L)
      throw new IllegalArgumentException(
          "geomodelid=" + geomodelid + " but cannot be >= 10000000000");
    this.geomodelid = geomodelid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * -
   * 
   * @param name
   * @throws IllegalArgumentException if name.length() >= 64
   */
  public Tomostats setName(String name) {
    if (name.length() > 64)
      throw new IllegalArgumentException(
          String.format("name.length() cannot be > 64.  name=%s", name));
    this.name = name;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return minimum
   */
  public double getMinimum() {
    return minimum;
  }

  /**
   * -
   * 
   * @param minimum
   */
  public Tomostats setMinimum(double minimum) {
    this.minimum = minimum;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return maximum
   */
  public double getMaximum() {
    return maximum;
  }

  /**
   * -
   * 
   * @param maximum
   */
  public Tomostats setMaximum(double maximum) {
    this.maximum = maximum;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return mean
   */
  public double getMean() {
    return mean;
  }

  /**
   * -
   * 
   * @param mean
   */
  public Tomostats setMean(double mean) {
    this.mean = mean;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return rms
   */
  public double getRms() {
    return rms;
  }

  /**
   * -
   * 
   * @param rms
   */
  public Tomostats setRms(double rms) {
    this.rms = rms;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return stddev
   */
  public double getStddev() {
    return stddev;
  }

  /**
   * -
   * 
   * @param stddev
   */
  public Tomostats setStddev(double stddev) {
    this.stddev = stddev;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return count
   */
  public long getCount() {
    return count;
  }

  /**
   * -
   * 
   * @param count
   * @throws IllegalArgumentException if count >= 10000000000
   */
  public Tomostats setCount(long count) {
    if (count >= 10000000000L)
      throw new IllegalArgumentException("count=" + count + " but cannot be >= 10000000000");
    this.count = count;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return units
   */
  public String getUnits() {
    return units;
  }

  /**
   * -
   * 
   * @param units
   * @throws IllegalArgumentException if units.length() >= 64
   */
  public Tomostats setUnits(String units) {
    if (units.length() > 64)
      throw new IllegalArgumentException(
          String.format("units.length() cannot be > 64.  units=%s", units));
    this.units = units;
    setHash(null);
    return this;
  }

  /**
   * Retrieve the name of the schema.
   * 
   * @return schema name
   */
  static public String getSchemaName() {
    return "GMP";
  }

}
