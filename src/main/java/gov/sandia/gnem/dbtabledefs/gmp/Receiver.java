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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * ?
 */
public class Receiver extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * -
   */
  private long receiverid;

  static final public long RECEIVERID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * -
   */
  private double lat;

  static final public double LAT_NA = Double.NaN;

  /**
   * -
   */
  private double lon;

  static final public double LON_NA = Double.NaN;

  /**
   * -
   */
  private double elevation;

  static final public double ELEVATION_NA = Double.NaN;

  /**
   * -
   */
  private double starttime;

  static final public double STARTTIME_NA = Double.NaN;

  /**
   * -
   */
  private double endtime;

  static final public double ENDTIME_NA = Double.NaN;

  /**
   * -
   */
  private long polygonid;

  static final public long POLYGONID_NA = -1;

  /**
   * -
   */
  private String auth;

  static final public String AUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("receiverid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("lat", Columns.FieldType.DOUBLE, "%10.6f");
    columns.add("lon", Columns.FieldType.DOUBLE, "%11.6f");
    columns.add("elevation", Columns.FieldType.DOUBLE, "%8.3f");
    columns.add("starttime", Columns.FieldType.DOUBLE, "%10.3f");
    columns.add("endtime", Columns.FieldType.DOUBLE, "%10.3f");
    columns.add("polygonid", Columns.FieldType.LONG, "%d");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Receiver(long receiverid, String sta, double lat, double lon, double elevation,
      double starttime, double endtime, long polygonid, String auth) {
    setValues(receiverid, sta, lat, lon, elevation, starttime, endtime, polygonid, auth);
  }

  private void setValues(long receiverid, String sta, double lat, double lon, double elevation,
      double starttime, double endtime, long polygonid, String auth) {
    this.receiverid = receiverid;
    this.sta = sta;
    this.lat = lat;
    this.lon = lon;
    this.elevation = elevation;
    this.starttime = starttime;
    this.endtime = endtime;
    this.polygonid = polygonid;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Receiver(Receiver other) {
    this.receiverid = other.getReceiverid();
    this.sta = other.getSta();
    this.lat = other.getLat();
    this.lon = other.getLon();
    this.elevation = other.getElevation();
    this.starttime = other.getStarttime();
    this.endtime = other.getEndtime();
    this.polygonid = other.getPolygonid();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Receiver() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(RECEIVERID_NA, STA_NA, LAT_NA, LON_NA, ELEVATION_NA, STARTTIME_NA, ENDTIME_NA,
        POLYGONID_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
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
      case "sta":
        sta = value;
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
      case "lat":
        return lat;
      case "lon":
        return lon;
      case "elevation":
        return elevation;
      case "starttime":
        return starttime;
      case "endtime":
        return endtime;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "lat":
        lat = value;
        break;
      case "lon":
        lon = value;
        break;
      case "elevation":
        elevation = value;
        break;
      case "starttime":
        starttime = value;
        break;
      case "endtime":
        endtime = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "receiverid":
        return receiverid;
      case "polygonid":
        return polygonid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "receiverid":
        receiverid = value;
        break;
      case "polygonid":
        polygonid = value;
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
  public Receiver(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Receiver(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readLong(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Receiver(ByteBuffer input) {
    this(input.getLong(), readString(input), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getLong(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Receiver(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Receiver(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), 
        input.getTimestamp(offset + 6).getTime()*1e-3,
        input.getTimestamp(offset + 7).getTime()*1e-3, 
        input.getLong(offset + 8), input.getString(offset + 9));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[9];
    values[0] = receiverid;
    values[1] = sta;
    values[2] = lat;
    values[3] = lon;
    values[4] = elevation;
    values[5] = starttime;
    values[6] = endtime;
    values[7] = polygonid;
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
    values[0] = receiverid;
    values[1] = sta;
    values[2] = lat;
    values[3] = lon;
    values[4] = elevation;
    values[5] = starttime;
    values[6] = endtime;
    values[7] = polygonid;
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
    output.writeLong(receiverid);
    writeString(output, sta);
    output.writeDouble(lat);
    output.writeDouble(lon);
    output.writeDouble(elevation);
    output.writeDouble(starttime);
    output.writeDouble(endtime);
    output.writeLong(polygonid);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(receiverid);
    writeString(output, sta);
    output.putDouble(lat);
    output.putDouble(lon);
    output.putDouble(elevation);
    output.putDouble(starttime);
    output.putDouble(endtime);
    output.putLong(polygonid);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Receiver objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Receiver objects.
   * @throws IOException
   */
  static public void readReceivers(BufferedReader input, Collection<Receiver> rows)
      throws IOException {
    String[] saved = Receiver.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Receiver
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Receiver(new Scanner(line)));
    }
    input.close();
    Receiver.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Receiver objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Receiver objects.
   * @throws IOException
   */
  static public void readReceivers(File inputFile, Collection<Receiver> rows) throws IOException {
    readReceivers(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Receiver objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Receiver objects.
   * @throws IOException
   */
  static public void readReceivers(InputStream inputStream, Collection<Receiver> rows)
      throws IOException {
    readReceivers(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Receiver objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Receiver objects
   * @throws IOException
   */
  static public Set<Receiver> readReceivers(BufferedReader input) throws IOException {
    Set<Receiver> rows = new LinkedHashSet<Receiver>();
    readReceivers(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Receiver objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Receiver objects
   * @throws IOException
   */
  static public Set<Receiver> readReceivers(File inputFile) throws IOException {
    return readReceivers(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Receiver objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Receiver objects
   * @throws IOException
   */
  static public Set<Receiver> readReceivers(InputStream input) throws IOException {
    return readReceivers(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Receiver objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param receivers the Receiver objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Receiver> receivers)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Receiver receiver : receivers)
      receiver.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Receiver objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param receivers the Receiver objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Receiver> receivers, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?)");
      for (Receiver receiver : receivers) {
        int i = 0;
        statement.setLong(++i, receiver.receiverid);
        statement.setString(++i, receiver.sta);
        statement.setDouble(++i, receiver.lat);
        statement.setDouble(++i, receiver.lon);
        statement.setDouble(++i, receiver.elevation);
        statement.setTimestamp(++i, new Timestamp((long)(receiver.starttime*1000)));
        statement.setTimestamp(++i, new Timestamp((long)(receiver.endtime*1000)));
        statement.setLong(++i, receiver.polygonid);
        statement.setString(++i, receiver.auth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Receiver
   *        table.
   * @return data
   * @throws SQLException
   */
  static public Map<Long, Receiver> readReceivers(Connection connection, String selectStatement)
      throws SQLException {
    Map<Long, Receiver> results = new TreeMap<Long, Receiver>();
    readReceivers(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Receiver
   *        table.
   * @param receivers
   * @throws SQLException
   */
  static public void readReceivers(Connection connection, String selectStatement,
      Map<Long, Receiver> receivers) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next()) {
        Receiver r = new Receiver(rs);
        receivers.put(r.getReceiverid(), r);
      }
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
   * this Receiver object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Receiver object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("receiverid, sta, lat, lon, elevation, starttime, endtime, polygonid, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(receiverid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append(Double.toString(lat)).append(", ");
    sql.append(Double.toString(lon)).append(", ");
    sql.append(Double.toString(elevation)).append(", ");
    sql.append(Double.toString(starttime)).append(", ");
    sql.append(Double.toString(endtime)).append(", ");
    sql.append(Long.toString(polygonid)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Receiver in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Receiver in the database
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
   * Generate a sql script to create a table of type Receiver in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Receiver in the database
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
    buf.append("receiverid   number(10)           NOT NULL,\n");
    buf.append("sta          varchar2(60)         NOT NULL,\n");
    buf.append("lat          float(126)           NOT NULL,\n");
    buf.append("lon          float(126)           NOT NULL,\n");
    buf.append("elevation    float(126)           NOT NULL,\n");
    buf.append("starttime    timestamp(3)         NOT NULL,\n");
    buf.append("endtime      timestamp(3)         NOT NULL,\n");
    buf.append("polygonid    number(10)           NOT NULL,\n");
    buf.append("auth         varchar2(64)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (receiverid)");
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
    return 188;
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
    return (other instanceof Receiver) && ((Receiver) other).receiverid == receiverid;
  }

  /**
   * -
   * 
   * @return receiverid
   */
  public long getReceiverid() {
    return receiverid;
  }

  /**
   * -
   * 
   * @param receiverid
   * @throws IllegalArgumentException if receiverid >= 10000000000
   */
  public Receiver setReceiverid(long receiverid) {
    if (receiverid >= 10000000000L)
      throw new IllegalArgumentException(
          "receiverid=" + receiverid + " but cannot be >= 10000000000");
    this.receiverid = receiverid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return sta
   */
  public String getSta() {
    return sta;
  }

  /**
   * -
   * 
   * @param sta
   * @throws IllegalArgumentException if sta.length() >= 60
   */
  public Receiver setSta(String sta) {
    if (sta.length() > 60)
      throw new IllegalArgumentException(
          String.format("sta.length() cannot be > 60.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return lat
   */
  public double getLat() {
    return lat;
  }

  /**
   * -
   * 
   * @param lat
   */
  public Receiver setLat(double lat) {
    this.lat = lat;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return lon
   */
  public double getLon() {
    return lon;
  }

  /**
   * -
   * 
   * @param lon
   */
  public Receiver setLon(double lon) {
    this.lon = lon;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return elevation
   */
  public double getElevation() {
    return elevation;
  }

  /**
   * -
   * 
   * @param elevation
   */
  public Receiver setElevation(double elevation) {
    this.elevation = elevation;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return starttime
   */
  public double getStarttime() {
    return starttime;
  }

  /**
   * -
   * 
   * @param starttime
   */
  public Receiver setStarttime(double starttime) {
    this.starttime = starttime;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return endtime
   */
  public double getEndtime() {
    return endtime;
  }

  /**
   * -
   * 
   * @param endtime
   */
  public Receiver setEndtime(double endtime) {
    this.endtime = endtime;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return polygonid
   */
  public long getPolygonid() {
    return polygonid;
  }

  /**
   * -
   * 
   * @param polygonid
   * @throws IllegalArgumentException if polygonid >= 10000000000
   */
  public Receiver setPolygonid(long polygonid) {
    if (polygonid >= 10000000000L)
      throw new IllegalArgumentException(
          "polygonid=" + polygonid + " but cannot be >= 10000000000");
    this.polygonid = polygonid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * -
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 64
   */
  public Receiver setAuth(String auth) {
    if (auth.length() > 64)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 64.  auth=%s", auth));
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
    return "GMP";
  }

  public Buff getBuff() {
      Buff buffer = new Buff(this.getClass().getSimpleName());
      buffer.add("format", 1);
      buffer.add("receiverid", receiverid);
      buffer.add("sta", sta);
      buffer.add("lat", lat, 6);
      buffer.add("lon", lon, 6);
      buffer.add("elevation", elevation, 3);
      buffer.add("starttime", starttime, 3);
      buffer.add("endtime", endtime, 3);
      buffer.add("polygonid", polygonid);
      buffer.add("auth", auth);
      return buffer;
  }

  static public Buff getBuff(Scanner input) {
	return new Buff(input);
  }
  
}
