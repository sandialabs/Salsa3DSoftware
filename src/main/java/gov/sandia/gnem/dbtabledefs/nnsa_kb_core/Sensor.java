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
package gov.sandia.gnem.dbtabledefs.nnsa_kb_core;

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
 * sensor
 */
public class Sensor extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location recorded in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Channel code; an eight-character code which, taken together with <I>sta, jdate</I> and
   * <I>time</I>, uniquely identifies seismic time-series data, including the geographic location,
   * spatial orientation, sensor, and subsequent data processing (beam channel descriptor)
   */
  private String chan;

  static final public String CHAN_NA = null;

  /**
   * Epoch time, given as seconds since midnight, January 1, 1970, and stored in a double-precision
   * floating number. Time refers to the table in which it is found [for example, in <B>arrival</B>
   * it is the arrival time, in <B>origin</B> it is the origin time, and in <B>wfdisc</B> is the
   * start time of data]. Where the date of historical events is known, time is set to the start
   * time of that date. Where the date of contemporary arrival measurements is known but no time is
   * given, then time is set to the NA Value. The double-precision floating point number allows 15
   * decimal digits. At one millisecond accuracy, this is a range of 3.0e+4 years. Where the date is
   * unknown or prior to February 10, 1653, time is set to the NA Value.
   * <p>
   * Units: s
   */
  private double time;

  static final public double TIME_NA = -9999999999.999;

  /**
   * Ending epoch time. Epoch time is given as seconds and fractions of a second since hour 0
   * January 1, 1970 and stored in a double-precision floating number.
   * <p>
   * Units: s
   */
  private double endtime;

  static final public double ENDTIME_NA = 9999999999.999;

  /**
   * Instrument identifier. This column is a unique key to the <B>instrument</B> table. The
   * <I>inid</I> column provides the only link between <B>sensor</B> and <B>instrument</B>.
   */
  private long inid;

  static final public long INID_NA = -1;

  /**
   * Channel identifier. This value is a surrogate key used to uniquely identify a specific
   * recording. The column chanid duplicates the information of the compound key
   * <I>sta</I>/<I>chan</I>/<I>time</I>.
   */
  private long chanid;

  static final public long CHANID_NA = -1;

  /**
   * Julian date. Date of an arrival, origin, seismic recording, etc. The same information is
   * available in epoch time, but the Julian date format is more convenient for many types of
   * searches. Dates B.C. are negative. The year will never equal 0000, and the day will never equal
   * 000. Where only the year is known, the day of the year is 001; where only year and month are
   * known, the day of year is the first day of the month. Only the year is negated for B.C., so 1
   * January of 10 B.C. is -0010001 (see <I>time</I>).
   */
  private long jdate;

  static final public long JDATE_NA = -1;

  /**
   * Calibration conversion ratio. The value is a dimensionless calibration correction factor that
   * permits small refinements to the calibration correction made using <I>calib</I> and
   * <I>calper</I> from the <B>wfdisc</B> table. Often, the <B>wfdisc</B> <I>calib</I> contains the
   * nominal calibration assumed at the time of data recording. If the instrument is recalibrated,
   * <I>calratio</I> provides a mechanism to update calibrations from <B>wfdisc</B> the new
   * information without modifying the <B>wfdisc</B> table. A positive value means ground motion
   * increasing in component direction (up, North, East) is indicated by increasing counts. A
   * negative value means the opposite. The column <I>calratio</I> is meant to reflect the most
   * accurate calibration information for the time period for which the sensor record is
   * appropriate, but the nominal value may appear until other information is available.
   */
  private double calratio;

  static final public double CALRATIO_NA = 1;

  /**
   * Calibration period; gives the period for which <I>calib, ncalib,</I> and <I>calratio</I> are
   * valid. If calper is the NA value (-1) then the calib in wfdisc is not applicable.
   * <p>
   * Units: s
   */
  private double calper;

  static final public double CALPER_NA = -1;

  /**
   * Correction for clock errors. Values are very low precision, usually an integer multiple of 1
   * second and are on the order of 5 or 10 seconds. This attribute is Values are very low
   * precision, usually an integer multiple of 1 second and are on the order of 5 or 10 seconds.
   * This attribute is designed to accommodate discrepancies between the actual time and numerical
   * time written by data recording systems. Actual time is the sum of the reported time plus
   * tshift.
   * <p>
   * Units: s
   */
  private double tshift;

  static final public double TSHIFT_NA = Double.NaN;

  /**
   * Snapshot indicator. When instant = y, the snapshot was taken at the time of a discrete
   * procedural change, such as an adjustment of the instrument gain; when instant = n, the snapshot
   * is of a continuously changing process, such as calibration drift. This value is important for
   * tracking time corrections and calibrations. The default value is y.
   */
  private String instant;

  static final public String INSTANT_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("endtime", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("inid", Columns.FieldType.LONG, "%d");
    columns.add("chanid", Columns.FieldType.LONG, "%d");
    columns.add("jdate", Columns.FieldType.LONG, "%d");
    columns.add("calratio", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("calper", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("tshift", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("instant", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Sensor(String sta, String chan, double time, double endtime, long inid, long chanid,
      long jdate, double calratio, double calper, double tshift, String instant) {
    setValues(sta, chan, time, endtime, inid, chanid, jdate, calratio, calper, tshift, instant);
  }

  private void setValues(String sta, String chan, double time, double endtime, long inid,
      long chanid, long jdate, double calratio, double calper, double tshift, String instant) {
    this.sta = sta;
    this.chan = chan;
    this.time = time;
    this.endtime = endtime;
    this.inid = inid;
    this.chanid = chanid;
    this.jdate = jdate;
    this.calratio = calratio;
    this.calper = calper;
    this.tshift = tshift;
    this.instant = instant;
  }

  /**
   * Copy constructor.
   */
  public Sensor(Sensor other) {
    this.sta = other.getSta();
    this.chan = other.getChan();
    this.time = other.getTime();
    this.endtime = other.getEndtime();
    this.inid = other.getInid();
    this.chanid = other.getChanid();
    this.jdate = other.getJdate();
    this.calratio = other.getCalratio();
    this.calper = other.getCalper();
    this.tshift = other.getTshift();
    this.instant = other.getInstant();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Sensor() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(STA_NA, CHAN_NA, TIME_NA, ENDTIME_NA, INID_NA, CHANID_NA, JDATE_NA, CALRATIO_NA,
        CALPER_NA, TSHIFT_NA, INSTANT_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "chan":
        return chan;
      case "instant":
        return instant;
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
      case "chan":
        chan = value;
        break;
      case "instant":
        instant = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "time":
        return time;
      case "endtime":
        return endtime;
      case "calratio":
        return calratio;
      case "calper":
        return calper;
      case "tshift":
        return tshift;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "time":
        time = value;
        break;
      case "endtime":
        endtime = value;
        break;
      case "calratio":
        calratio = value;
        break;
      case "calper":
        calper = value;
        break;
      case "tshift":
        tshift = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "inid":
        return inid;
      case "chanid":
        return chanid;
      case "jdate":
        return jdate;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "inid":
        inid = value;
        break;
      case "chanid":
        chanid = value;
        break;
      case "jdate":
        jdate = value;
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
  public Sensor(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Sensor(DataInputStream input) throws IOException {
    this(readString(input), readString(input), input.readDouble(), input.readDouble(),
        input.readLong(), input.readLong(), input.readLong(), input.readDouble(),
        input.readDouble(), input.readDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Sensor(ByteBuffer input) {
    this(readString(input), readString(input), input.getDouble(), input.getDouble(),
        input.getLong(), input.getLong(), input.getLong(), input.getDouble(), input.getDouble(),
        input.getDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Sensor(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Sensor(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getString(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getLong(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getString(offset + 11));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[11];
    values[0] = sta;
    values[1] = chan;
    values[2] = time;
    values[3] = endtime;
    values[4] = inid;
    values[5] = chanid;
    values[6] = jdate;
    values[7] = calratio;
    values[8] = calper;
    values[9] = tshift;
    values[10] = instant;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[12];
    values[0] = sta;
    values[1] = chan;
    values[2] = time;
    values[3] = endtime;
    values[4] = inid;
    values[5] = chanid;
    values[6] = jdate;
    values[7] = calratio;
    values[8] = calper;
    values[9] = tshift;
    values[10] = instant;
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
    writeString(output, sta);
    writeString(output, chan);
    output.writeDouble(time);
    output.writeDouble(endtime);
    output.writeLong(inid);
    output.writeLong(chanid);
    output.writeLong(jdate);
    output.writeDouble(calratio);
    output.writeDouble(calper);
    output.writeDouble(tshift);
    writeString(output, instant);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, sta);
    writeString(output, chan);
    output.putDouble(time);
    output.putDouble(endtime);
    output.putLong(inid);
    output.putLong(chanid);
    output.putLong(jdate);
    output.putDouble(calratio);
    output.putDouble(calper);
    output.putDouble(tshift);
    writeString(output, instant);
  }

  /**
   * Read a Collection of Sensor objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Sensor objects.
   * @throws IOException
   */
  static public void readSensors(BufferedReader input, Collection<Sensor> rows) throws IOException {
    String[] saved = Sensor.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Sensor.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Sensor(new Scanner(line)));
    }
    input.close();
    Sensor.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Sensor objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Sensor objects.
   * @throws IOException
   */
  static public void readSensors(File inputFile, Collection<Sensor> rows) throws IOException {
    readSensors(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Sensor objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Sensor objects.
   * @throws IOException
   */
  static public void readSensors(InputStream inputStream, Collection<Sensor> rows)
      throws IOException {
    readSensors(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Sensor objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Sensor objects
   * @throws IOException
   */
  static public Set<Sensor> readSensors(BufferedReader input) throws IOException {
    Set<Sensor> rows = new LinkedHashSet<Sensor>();
    readSensors(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Sensor objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Sensor objects
   * @throws IOException
   */
  static public Set<Sensor> readSensors(File inputFile) throws IOException {
    return readSensors(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Sensor objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Sensor objects
   * @throws IOException
   */
  static public Set<Sensor> readSensors(InputStream input) throws IOException {
    return readSensors(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Sensor objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param sensors the Sensor objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Sensor> sensors) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Sensor sensor : sensors)
      sensor.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Sensor objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param sensors the Sensor objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Sensor> sensors, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Sensor sensor : sensors) {
        int i = 0;
        statement.setString(++i, sensor.sta);
        statement.setString(++i, sensor.chan);
        statement.setDouble(++i, sensor.time);
        statement.setDouble(++i, sensor.endtime);
        statement.setLong(++i, sensor.inid);
        statement.setLong(++i, sensor.chanid);
        statement.setLong(++i, sensor.jdate);
        statement.setDouble(++i, sensor.calratio);
        statement.setDouble(++i, sensor.calper);
        statement.setDouble(++i, sensor.tshift);
        statement.setString(++i, sensor.instant);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Sensor
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Sensor> readSensors(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Sensor> results = new HashSet<Sensor>();
    readSensors(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Sensor
   *        table.
   * @param sensors
   * @throws SQLException
   */
  static public void readSensors(Connection connection, String selectStatement, Set<Sensor> sensors)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        sensors.add(new Sensor(rs));
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
   * this Sensor object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Sensor object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "sta, chan, time, endtime, inid, chanid, jdate, calratio, calper, tshift, instant, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(chan).append("', ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Double.toString(endtime)).append(", ");
    sql.append(Long.toString(inid)).append(", ");
    sql.append(Long.toString(chanid)).append(", ");
    sql.append(Long.toString(jdate)).append(", ");
    sql.append(Double.toString(calratio)).append(", ");
    sql.append(Double.toString(calper)).append(", ");
    sql.append(Double.toString(tshift)).append(", ");
    sql.append("'").append(instant).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Sensor in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Sensor in the database
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
   * Generate a sql script to create a table of type Sensor in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Sensor in the database
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
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("endtime      float(53)            NOT NULL,\n");
    buf.append("inid         number(8)            NOT NULL,\n");
    buf.append("chanid       number(8)            NOT NULL,\n");
    buf.append("jdate        number(8)            NOT NULL,\n");
    buf.append("calratio     float(24)            NOT NULL,\n");
    buf.append("calper       float(24)            NOT NULL,\n");
    buf.append("tshift       float(24)            NOT NULL,\n");
    buf.append("instant      varchar2(1)          NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (sta,chan,time)");
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
    return 91;
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
    return (other instanceof Sensor) && ((Sensor) other).sta.equals(sta)
        && ((Sensor) other).chan.equals(chan) && ((Sensor) other).time == time;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location recorded in the <B>site</B> table.
   * 
   * @return sta
   */
  public String getSta() {
    return sta;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location recorded in the <B>site</B> table.
   * 
   * @param sta
   * @throws IllegalArgumentException if sta.length() >= 6
   */
  public Sensor setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * Channel code; an eight-character code which, taken together with <I>sta, jdate</I> and
   * <I>time</I>, uniquely identifies seismic time-series data, including the geographic location,
   * spatial orientation, sensor, and subsequent data processing (beam channel descriptor)
   * 
   * @return chan
   */
  public String getChan() {
    return chan;
  }

  /**
   * Channel code; an eight-character code which, taken together with <I>sta, jdate</I> and
   * <I>time</I>, uniquely identifies seismic time-series data, including the geographic location,
   * spatial orientation, sensor, and subsequent data processing (beam channel descriptor)
   * 
   * @param chan
   * @throws IllegalArgumentException if chan.length() >= 8
   */
  public Sensor setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
    setHash(null);
    return this;
  }

  /**
   * Epoch time, given as seconds since midnight, January 1, 1970, and stored in a double-precision
   * floating number. Time refers to the table in which it is found [for example, in <B>arrival</B>
   * it is the arrival time, in <B>origin</B> it is the origin time, and in <B>wfdisc</B> is the
   * start time of data]. Where the date of historical events is known, time is set to the start
   * time of that date. Where the date of contemporary arrival measurements is known but no time is
   * given, then time is set to the NA Value. The double-precision floating point number allows 15
   * decimal digits. At one millisecond accuracy, this is a range of 3.0e+4 years. Where the date is
   * unknown or prior to February 10, 1653, time is set to the NA Value.
   * <p>
   * Units: s
   * 
   * @return time
   */
  public double getTime() {
    return time;
  }

  /**
   * Epoch time, given as seconds since midnight, January 1, 1970, and stored in a double-precision
   * floating number. Time refers to the table in which it is found [for example, in <B>arrival</B>
   * it is the arrival time, in <B>origin</B> it is the origin time, and in <B>wfdisc</B> is the
   * start time of data]. Where the date of historical events is known, time is set to the start
   * time of that date. Where the date of contemporary arrival measurements is known but no time is
   * given, then time is set to the NA Value. The double-precision floating point number allows 15
   * decimal digits. At one millisecond accuracy, this is a range of 3.0e+4 years. Where the date is
   * unknown or prior to February 10, 1653, time is set to the NA Value.
   * <p>
   * Units: s
   * 
   * @param time
   */
  public Sensor setTime(double time) {
    this.time = time;
    setHash(null);
    return this;
  }

  /**
   * Ending epoch time. Epoch time is given as seconds and fractions of a second since hour 0
   * January 1, 1970 and stored in a double-precision floating number.
   * <p>
   * Units: s
   * 
   * @return endtime
   */
  public double getEndtime() {
    return endtime;
  }

  /**
   * Ending epoch time. Epoch time is given as seconds and fractions of a second since hour 0
   * January 1, 1970 and stored in a double-precision floating number.
   * <p>
   * Units: s
   * 
   * @param endtime
   */
  public Sensor setEndtime(double endtime) {
    this.endtime = endtime;
    setHash(null);
    return this;
  }

  /**
   * Instrument identifier. This column is a unique key to the <B>instrument</B> table. The
   * <I>inid</I> column provides the only link between <B>sensor</B> and <B>instrument</B>.
   * 
   * @return inid
   */
  public long getInid() {
    return inid;
  }

  /**
   * Instrument identifier. This column is a unique key to the <B>instrument</B> table. The
   * <I>inid</I> column provides the only link between <B>sensor</B> and <B>instrument</B>.
   * 
   * @param inid
   * @throws IllegalArgumentException if inid >= 100000000
   */
  public Sensor setInid(long inid) {
    if (inid >= 100000000L)
      throw new IllegalArgumentException("inid=" + inid + " but cannot be >= 100000000");
    this.inid = inid;
    setHash(null);
    return this;
  }

  /**
   * Channel identifier. This value is a surrogate key used to uniquely identify a specific
   * recording. The column chanid duplicates the information of the compound key
   * <I>sta</I>/<I>chan</I>/<I>time</I>.
   * 
   * @return chanid
   */
  public long getChanid() {
    return chanid;
  }

  /**
   * Channel identifier. This value is a surrogate key used to uniquely identify a specific
   * recording. The column chanid duplicates the information of the compound key
   * <I>sta</I>/<I>chan</I>/<I>time</I>.
   * 
   * @param chanid
   * @throws IllegalArgumentException if chanid >= 100000000
   */
  public Sensor setChanid(long chanid) {
    if (chanid >= 100000000L)
      throw new IllegalArgumentException("chanid=" + chanid + " but cannot be >= 100000000");
    this.chanid = chanid;
    setHash(null);
    return this;
  }

  /**
   * Julian date. Date of an arrival, origin, seismic recording, etc. The same information is
   * available in epoch time, but the Julian date format is more convenient for many types of
   * searches. Dates B.C. are negative. The year will never equal 0000, and the day will never equal
   * 000. Where only the year is known, the day of the year is 001; where only year and month are
   * known, the day of year is the first day of the month. Only the year is negated for B.C., so 1
   * January of 10 B.C. is -0010001 (see <I>time</I>).
   * 
   * @return jdate
   */
  public long getJdate() {
    return jdate;
  }

  /**
   * Julian date. Date of an arrival, origin, seismic recording, etc. The same information is
   * available in epoch time, but the Julian date format is more convenient for many types of
   * searches. Dates B.C. are negative. The year will never equal 0000, and the day will never equal
   * 000. Where only the year is known, the day of the year is 001; where only year and month are
   * known, the day of year is the first day of the month. Only the year is negated for B.C., so 1
   * January of 10 B.C. is -0010001 (see <I>time</I>).
   * 
   * @param jdate
   * @throws IllegalArgumentException if jdate >= 100000000
   */
  public Sensor setJdate(long jdate) {
    if (jdate >= 100000000L)
      throw new IllegalArgumentException("jdate=" + jdate + " but cannot be >= 100000000");
    this.jdate = jdate;
    setHash(null);
    return this;
  }

  /**
   * Calibration conversion ratio. The value is a dimensionless calibration correction factor that
   * permits small refinements to the calibration correction made using <I>calib</I> and
   * <I>calper</I> from the <B>wfdisc</B> table. Often, the <B>wfdisc</B> <I>calib</I> contains the
   * nominal calibration assumed at the time of data recording. If the instrument is recalibrated,
   * <I>calratio</I> provides a mechanism to update calibrations from <B>wfdisc</B> the new
   * information without modifying the <B>wfdisc</B> table. A positive value means ground motion
   * increasing in component direction (up, North, East) is indicated by increasing counts. A
   * negative value means the opposite. The column <I>calratio</I> is meant to reflect the most
   * accurate calibration information for the time period for which the sensor record is
   * appropriate, but the nominal value may appear until other information is available.
   * 
   * @return calratio
   */
  public double getCalratio() {
    return calratio;
  }

  /**
   * Calibration conversion ratio. The value is a dimensionless calibration correction factor that
   * permits small refinements to the calibration correction made using <I>calib</I> and
   * <I>calper</I> from the <B>wfdisc</B> table. Often, the <B>wfdisc</B> <I>calib</I> contains the
   * nominal calibration assumed at the time of data recording. If the instrument is recalibrated,
   * <I>calratio</I> provides a mechanism to update calibrations from <B>wfdisc</B> the new
   * information without modifying the <B>wfdisc</B> table. A positive value means ground motion
   * increasing in component direction (up, North, East) is indicated by increasing counts. A
   * negative value means the opposite. The column <I>calratio</I> is meant to reflect the most
   * accurate calibration information for the time period for which the sensor record is
   * appropriate, but the nominal value may appear until other information is available.
   * 
   * @param calratio
   */
  public Sensor setCalratio(double calratio) {
    this.calratio = calratio;
    setHash(null);
    return this;
  }

  /**
   * Calibration period; gives the period for which <I>calib, ncalib,</I> and <I>calratio</I> are
   * valid. If calper is the NA value (-1) then the calib in wfdisc is not applicable.
   * <p>
   * Units: s
   * 
   * @return calper
   */
  public double getCalper() {
    return calper;
  }

  /**
   * Calibration period; gives the period for which <I>calib, ncalib,</I> and <I>calratio</I> are
   * valid. If calper is the NA value (-1) then the calib in wfdisc is not applicable.
   * <p>
   * Units: s
   * 
   * @param calper
   */
  public Sensor setCalper(double calper) {
    this.calper = calper;
    setHash(null);
    return this;
  }

  /**
   * Correction for clock errors. Values are very low precision, usually an integer multiple of 1
   * second and are on the order of 5 or 10 seconds. This attribute is Values are very low
   * precision, usually an integer multiple of 1 second and are on the order of 5 or 10 seconds.
   * This attribute is designed to accommodate discrepancies between the actual time and numerical
   * time written by data recording systems. Actual time is the sum of the reported time plus
   * tshift.
   * <p>
   * Units: s
   * 
   * @return tshift
   */
  public double getTshift() {
    return tshift;
  }

  /**
   * Correction for clock errors. Values are very low precision, usually an integer multiple of 1
   * second and are on the order of 5 or 10 seconds. This attribute is Values are very low
   * precision, usually an integer multiple of 1 second and are on the order of 5 or 10 seconds.
   * This attribute is designed to accommodate discrepancies between the actual time and numerical
   * time written by data recording systems. Actual time is the sum of the reported time plus
   * tshift.
   * <p>
   * Units: s
   * 
   * @param tshift
   */
  public Sensor setTshift(double tshift) {
    this.tshift = tshift;
    setHash(null);
    return this;
  }

  /**
   * Snapshot indicator. When instant = y, the snapshot was taken at the time of a discrete
   * procedural change, such as an adjustment of the instrument gain; when instant = n, the snapshot
   * is of a continuously changing process, such as calibration drift. This value is important for
   * tracking time corrections and calibrations. The default value is y.
   * 
   * @return instant
   */
  public String getInstant() {
    return instant;
  }

  /**
   * Snapshot indicator. When instant = y, the snapshot was taken at the time of a discrete
   * procedural change, such as an adjustment of the instrument gain; when instant = n, the snapshot
   * is of a continuously changing process, such as calibration drift. This value is important for
   * tracking time corrections and calibrations. The default value is y.
   * 
   * @param instant
   * @throws IllegalArgumentException if instant.length() >= 1
   */
  public Sensor setInstant(String instant) {
    if (instant.length() > 1)
      throw new IllegalArgumentException(
          String.format("instant.length() cannot be > 1.  instant=%s", instant));
    this.instant = instant;
    setHash(null);
    return this;
  }

  /**
   * Retrieve the name of the schema.
   * 
   * @return schema name
   */
  static public String getSchemaName() {
    return "NNSA KB Core";
  }

}
