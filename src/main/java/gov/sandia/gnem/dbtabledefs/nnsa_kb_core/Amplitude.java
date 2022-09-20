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
 * amplitude
 */
public class Amplitude extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Amplitude identifier. Every amplitude measure is assigned a unique positive integer that
   * identifies it in the database.
   */
  private long ampid;

  static final public long AMPID_NA = Long.MIN_VALUE;

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta, chan</I> and <I>time</I>.
   */
  private long arid;

  static final public long ARID_NA = Long.MIN_VALUE;

  /**
   * Predicted arrival identifier
   */
  private long parid;

  static final public long PARID_NA = -1;

  /**
   * Channel code; an eight-character code which, taken together with <I>sta, jdate</I> and
   * <I>time</I>, uniquely identifies seismic time-series data, including the geographic location,
   * spatial orientation, sensor, and subsequent data processing (beam channel descriptor)
   */
  private String chan;

  static final public String CHAN_NA = null;

  /**
   * Measured instrument corrected amplitude
   * <p>
   * Units: nm, nm/s or dimensionless depending on the type of channel
   */
  private double amp;

  static final public double AMP_NA = -1;

  /**
   * Measured period at the time of the amplitude measurement
   * <p>
   * Units: s
   */
  private double per;

  static final public double PER_NA = -1;

  /**
   * Signal-to-noise ratio. This is an estimate of the ratio of the amplitude of the signal to
   * amplitude of the noise immediately preceding it. This column is the average signal-to-noise
   * ratio for the frequency bands that contributed to the final polarization estimates.
   */
  private double snr;

  static final public double SNR_NA = -1;

  /**
   * Epoch time of amplitude measure
   * <p>
   * Units: s
   */
  private double amptime;

  static final public double AMPTIME_NA = -9999999999.999;

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
   * Duration of the time region for chan_groups and wfactivity. Total duration of amplitude window
   * for amplitude. Combined with time, the entire amplitude time window is specified. May also be
   * employed to compute a coda duration magnitude if amp and per columns contain NA Values.
   * <p>
   * Units: s
   */
  private double duration;

  static final public double DURATION_NA = -1;

  /**
   * Sample interval width
   */
  private double deltaf;

  static final public double DELTAF_NA = -1.0;

  /**
   * Amplitude measure descriptor. This descriptor is used to uniquely identify an amplitude
   * measurement and link the description in ampdescript with actual measurements in amplitude.
   */
  private String amptype;

  static final public String AMPTYPE_NA = "-";

  /**
   * Units of amplitude measure
   */
  private String units;

  static final public String UNITS_NA = null;

  /**
   * Clipped data flag. This value is a single-character flag to indicate whether (c) or not (n) the
   * data was clipped
   */
  private String clip;

  static final public String CLIP_NA = "-";

  /**
   * Flag to indicate whether or not <I>amp</I> is the same as it is in the arrival table
   */
  private String inarrival;

  static final public String INARRIVAL_NA = null;

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   */
  private String auth;

  static final public String AUTH_NA = "-";


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("ampid", Columns.FieldType.LONG, "%d");
    columns.add("arid", Columns.FieldType.LONG, "%d");
    columns.add("parid", Columns.FieldType.LONG, "%d");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("amp", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("per", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("snr", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("amptime", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("duration", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("deltaf", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("amptype", Columns.FieldType.STRING, "%s");
    columns.add("units", Columns.FieldType.STRING, "%s");
    columns.add("clip", Columns.FieldType.STRING, "%s");
    columns.add("inarrival", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Amplitude(long ampid, long arid, long parid, String chan, double amp, double per,
      double snr, double amptime, double time, double duration, double deltaf, String amptype,
      String units, String clip, String inarrival, String auth) {
    setValues(ampid, arid, parid, chan, amp, per, snr, amptime, time, duration, deltaf, amptype,
        units, clip, inarrival, auth);
  }

  private void setValues(long ampid, long arid, long parid, String chan, double amp, double per,
      double snr, double amptime, double time, double duration, double deltaf, String amptype,
      String units, String clip, String inarrival, String auth) {
    this.ampid = ampid;
    this.arid = arid;
    this.parid = parid;
    this.chan = chan;
    this.amp = amp;
    this.per = per;
    this.snr = snr;
    this.amptime = amptime;
    this.time = time;
    this.duration = duration;
    this.deltaf = deltaf;
    this.amptype = amptype;
    this.units = units;
    this.clip = clip;
    this.inarrival = inarrival;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Amplitude(Amplitude other) {
    this.ampid = other.getAmpid();
    this.arid = other.getArid();
    this.parid = other.getParid();
    this.chan = other.getChan();
    this.amp = other.getAmp();
    this.per = other.getPer();
    this.snr = other.getSnr();
    this.amptime = other.getAmptime();
    this.time = other.getTime();
    this.duration = other.getDuration();
    this.deltaf = other.getDeltaf();
    this.amptype = other.getAmptype();
    this.units = other.getUnits();
    this.clip = other.getClip();
    this.inarrival = other.getInarrival();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Amplitude() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(AMPID_NA, ARID_NA, PARID_NA, CHAN_NA, AMP_NA, PER_NA, SNR_NA, AMPTIME_NA, TIME_NA,
        DURATION_NA, DELTAF_NA, AMPTYPE_NA, UNITS_NA, CLIP_NA, INARRIVAL_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "chan":
        return chan;
      case "amptype":
        return amptype;
      case "units":
        return units;
      case "clip":
        return clip;
      case "inarrival":
        return inarrival;
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
      case "chan":
        chan = value;
        break;
      case "amptype":
        amptype = value;
        break;
      case "units":
        units = value;
        break;
      case "clip":
        clip = value;
        break;
      case "inarrival":
        inarrival = value;
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
      case "amp":
        return amp;
      case "per":
        return per;
      case "snr":
        return snr;
      case "amptime":
        return amptime;
      case "time":
        return time;
      case "duration":
        return duration;
      case "deltaf":
        return deltaf;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "amp":
        amp = value;
        break;
      case "per":
        per = value;
        break;
      case "snr":
        snr = value;
        break;
      case "amptime":
        amptime = value;
        break;
      case "time":
        time = value;
        break;
      case "duration":
        duration = value;
        break;
      case "deltaf":
        deltaf = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "ampid":
        return ampid;
      case "arid":
        return arid;
      case "parid":
        return parid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "ampid":
        ampid = value;
        break;
      case "arid":
        arid = value;
        break;
      case "parid":
        parid = value;
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
  public Amplitude(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Amplitude(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), readString(input),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), readString(input),
        readString(input), readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Amplitude(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), readString(input), readString(input),
        readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Amplitude(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Amplitude(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getString(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getString(offset + 12),
        input.getString(offset + 13), input.getString(offset + 14), input.getString(offset + 15),
        input.getString(offset + 16));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[16];
    values[0] = ampid;
    values[1] = arid;
    values[2] = parid;
    values[3] = chan;
    values[4] = amp;
    values[5] = per;
    values[6] = snr;
    values[7] = amptime;
    values[8] = time;
    values[9] = duration;
    values[10] = deltaf;
    values[11] = amptype;
    values[12] = units;
    values[13] = clip;
    values[14] = inarrival;
    values[15] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[17];
    values[0] = ampid;
    values[1] = arid;
    values[2] = parid;
    values[3] = chan;
    values[4] = amp;
    values[5] = per;
    values[6] = snr;
    values[7] = amptime;
    values[8] = time;
    values[9] = duration;
    values[10] = deltaf;
    values[11] = amptype;
    values[12] = units;
    values[13] = clip;
    values[14] = inarrival;
    values[15] = auth;
    values[16] = lddate;
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
    output.writeLong(ampid);
    output.writeLong(arid);
    output.writeLong(parid);
    writeString(output, chan);
    output.writeDouble(amp);
    output.writeDouble(per);
    output.writeDouble(snr);
    output.writeDouble(amptime);
    output.writeDouble(time);
    output.writeDouble(duration);
    output.writeDouble(deltaf);
    writeString(output, amptype);
    writeString(output, units);
    writeString(output, clip);
    writeString(output, inarrival);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(ampid);
    output.putLong(arid);
    output.putLong(parid);
    writeString(output, chan);
    output.putDouble(amp);
    output.putDouble(per);
    output.putDouble(snr);
    output.putDouble(amptime);
    output.putDouble(time);
    output.putDouble(duration);
    output.putDouble(deltaf);
    writeString(output, amptype);
    writeString(output, units);
    writeString(output, clip);
    writeString(output, inarrival);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Amplitude objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Amplitude objects.
   * @throws IOException
   */
  static public void readAmplitudes(BufferedReader input, Collection<Amplitude> rows)
      throws IOException {
    String[] saved = Amplitude.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Amplitude
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Amplitude(new Scanner(line)));
    }
    input.close();
    Amplitude.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Amplitude objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Amplitude objects.
   * @throws IOException
   */
  static public void readAmplitudes(File inputFile, Collection<Amplitude> rows) throws IOException {
    readAmplitudes(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Amplitude objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Amplitude objects.
   * @throws IOException
   */
  static public void readAmplitudes(InputStream inputStream, Collection<Amplitude> rows)
      throws IOException {
    readAmplitudes(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Amplitude objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Amplitude objects
   * @throws IOException
   */
  static public Set<Amplitude> readAmplitudes(BufferedReader input) throws IOException {
    Set<Amplitude> rows = new LinkedHashSet<Amplitude>();
    readAmplitudes(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Amplitude objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Amplitude objects
   * @throws IOException
   */
  static public Set<Amplitude> readAmplitudes(File inputFile) throws IOException {
    return readAmplitudes(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Amplitude objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Amplitude objects
   * @throws IOException
   */
  static public Set<Amplitude> readAmplitudes(InputStream input) throws IOException {
    return readAmplitudes(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Amplitude objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param amplitudes the Amplitude objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Amplitude> amplitudes)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Amplitude amplitude : amplitudes)
      amplitude.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Amplitude objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param amplitudes the Amplitude objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Amplitude> amplitudes, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Amplitude amplitude : amplitudes) {
        int i = 0;
        statement.setLong(++i, amplitude.ampid);
        statement.setLong(++i, amplitude.arid);
        statement.setLong(++i, amplitude.parid);
        statement.setString(++i, amplitude.chan);
        statement.setDouble(++i, amplitude.amp);
        statement.setDouble(++i, amplitude.per);
        statement.setDouble(++i, amplitude.snr);
        statement.setDouble(++i, amplitude.amptime);
        statement.setDouble(++i, amplitude.time);
        statement.setDouble(++i, amplitude.duration);
        statement.setDouble(++i, amplitude.deltaf);
        statement.setString(++i, amplitude.amptype);
        statement.setString(++i, amplitude.units);
        statement.setString(++i, amplitude.clip);
        statement.setString(++i, amplitude.inarrival);
        statement.setString(++i, amplitude.auth);
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
   *        Amplitude table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Amplitude> readAmplitudes(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Amplitude> results = new HashSet<Amplitude>();
    readAmplitudes(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Amplitude table.
   * @param amplitudes
   * @throws SQLException
   */
  static public void readAmplitudes(Connection connection, String selectStatement,
      Set<Amplitude> amplitudes) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        amplitudes.add(new Amplitude(rs));
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
   * this Amplitude object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Amplitude object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "ampid, arid, parid, chan, amp, per, snr, amptime, time, duration, deltaf, amptype, units, clip, inarrival, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(ampid)).append(", ");
    sql.append(Long.toString(arid)).append(", ");
    sql.append(Long.toString(parid)).append(", ");
    sql.append("'").append(chan).append("', ");
    sql.append(Double.toString(amp)).append(", ");
    sql.append(Double.toString(per)).append(", ");
    sql.append(Double.toString(snr)).append(", ");
    sql.append(Double.toString(amptime)).append(", ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Double.toString(duration)).append(", ");
    sql.append(Double.toString(deltaf)).append(", ");
    sql.append("'").append(amptype).append("', ");
    sql.append("'").append(units).append("', ");
    sql.append("'").append(clip).append("', ");
    sql.append("'").append(inarrival).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Amplitude in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Amplitude in the database
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
   * Generate a sql script to create a table of type Amplitude in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Amplitude in the database
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
    buf.append("ampid        number(9)            NOT NULL,\n");
    buf.append("arid         number(9)            NOT NULL,\n");
    buf.append("parid        number(9)            NOT NULL,\n");
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("amp          float(24)            NOT NULL,\n");
    buf.append("per          float(24)            NOT NULL,\n");
    buf.append("snr          float(24)            NOT NULL,\n");
    buf.append("amptime      float(53)            NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("duration     float(24)            NOT NULL,\n");
    buf.append("deltaf       float(24)            NOT NULL,\n");
    buf.append("amptype      varchar2(8)          NOT NULL,\n");
    buf.append("units        varchar2(15)         NOT NULL,\n");
    buf.append("clip         varchar2(1)          NOT NULL,\n");
    buf.append("inarrival    varchar2(1)          NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (ampid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (arid,chan,per,amptime,time,duration,deltaf,amptype,auth)");
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
    return 157;
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
    return (other instanceof Amplitude) && ((Amplitude) other).ampid == ampid;
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
    return (other instanceof Amplitude) && ((Amplitude) other).arid == arid
        && ((Amplitude) other).chan.equals(chan) && ((Amplitude) other).per == per
        && ((Amplitude) other).amptime == amptime && ((Amplitude) other).time == time
        && ((Amplitude) other).duration == duration && ((Amplitude) other).deltaf == deltaf
        && ((Amplitude) other).amptype.equals(amptype) && ((Amplitude) other).auth.equals(auth);
  }

  /**
   * Amplitude identifier. Every amplitude measure is assigned a unique positive integer that
   * identifies it in the database.
   * 
   * @return ampid
   */
  public long getAmpid() {
    return ampid;
  }

  /**
   * Amplitude identifier. Every amplitude measure is assigned a unique positive integer that
   * identifies it in the database.
   * 
   * @param ampid
   * @throws IllegalArgumentException if ampid >= 1000000000
   */
  public Amplitude setAmpid(long ampid) {
    if (ampid >= 1000000000L)
      throw new IllegalArgumentException("ampid=" + ampid + " but cannot be >= 1000000000");
    this.ampid = ampid;
    setHash(null);
    return this;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta, chan</I> and <I>time</I>.
   * 
   * @return arid
   */
  public long getArid() {
    return arid;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta, chan</I> and <I>time</I>.
   * 
   * @param arid
   * @throws IllegalArgumentException if arid >= 1000000000
   */
  public Amplitude setArid(long arid) {
    if (arid >= 1000000000L)
      throw new IllegalArgumentException("arid=" + arid + " but cannot be >= 1000000000");
    this.arid = arid;
    setHash(null);
    return this;
  }

  /**
   * Predicted arrival identifier
   * 
   * @return parid
   */
  public long getParid() {
    return parid;
  }

  /**
   * Predicted arrival identifier
   * 
   * @param parid
   * @throws IllegalArgumentException if parid >= 1000000000
   */
  public Amplitude setParid(long parid) {
    if (parid >= 1000000000L)
      throw new IllegalArgumentException("parid=" + parid + " but cannot be >= 1000000000");
    this.parid = parid;
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
  public Amplitude setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
    setHash(null);
    return this;
  }

  /**
   * Measured instrument corrected amplitude
   * <p>
   * Units: nm, nm/s or dimensionless depending on the type of channel
   * 
   * @return amp
   */
  public double getAmp() {
    return amp;
  }

  /**
   * Measured instrument corrected amplitude
   * <p>
   * Units: nm, nm/s or dimensionless depending on the type of channel
   * 
   * @param amp
   */
  public Amplitude setAmp(double amp) {
    this.amp = amp;
    setHash(null);
    return this;
  }

  /**
   * Measured period at the time of the amplitude measurement
   * <p>
   * Units: s
   * 
   * @return per
   */
  public double getPer() {
    return per;
  }

  /**
   * Measured period at the time of the amplitude measurement
   * <p>
   * Units: s
   * 
   * @param per
   */
  public Amplitude setPer(double per) {
    this.per = per;
    setHash(null);
    return this;
  }

  /**
   * Signal-to-noise ratio. This is an estimate of the ratio of the amplitude of the signal to
   * amplitude of the noise immediately preceding it. This column is the average signal-to-noise
   * ratio for the frequency bands that contributed to the final polarization estimates.
   * 
   * @return snr
   */
  public double getSnr() {
    return snr;
  }

  /**
   * Signal-to-noise ratio. This is an estimate of the ratio of the amplitude of the signal to
   * amplitude of the noise immediately preceding it. This column is the average signal-to-noise
   * ratio for the frequency bands that contributed to the final polarization estimates.
   * 
   * @param snr
   */
  public Amplitude setSnr(double snr) {
    this.snr = snr;
    setHash(null);
    return this;
  }

  /**
   * Epoch time of amplitude measure
   * <p>
   * Units: s
   * 
   * @return amptime
   */
  public double getAmptime() {
    return amptime;
  }

  /**
   * Epoch time of amplitude measure
   * <p>
   * Units: s
   * 
   * @param amptime
   */
  public Amplitude setAmptime(double amptime) {
    this.amptime = amptime;
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
  public Amplitude setTime(double time) {
    this.time = time;
    setHash(null);
    return this;
  }

  /**
   * Duration of the time region for chan_groups and wfactivity. Total duration of amplitude window
   * for amplitude. Combined with time, the entire amplitude time window is specified. May also be
   * employed to compute a coda duration magnitude if amp and per columns contain NA Values.
   * <p>
   * Units: s
   * 
   * @return duration
   */
  public double getDuration() {
    return duration;
  }

  /**
   * Duration of the time region for chan_groups and wfactivity. Total duration of amplitude window
   * for amplitude. Combined with time, the entire amplitude time window is specified. May also be
   * employed to compute a coda duration magnitude if amp and per columns contain NA Values.
   * <p>
   * Units: s
   * 
   * @param duration
   */
  public Amplitude setDuration(double duration) {
    this.duration = duration;
    setHash(null);
    return this;
  }

  /**
   * Sample interval width
   * 
   * @return deltaf
   */
  public double getDeltaf() {
    return deltaf;
  }

  /**
   * Sample interval width
   * 
   * @param deltaf
   */
  public Amplitude setDeltaf(double deltaf) {
    this.deltaf = deltaf;
    setHash(null);
    return this;
  }

  /**
   * Amplitude measure descriptor. This descriptor is used to uniquely identify an amplitude
   * measurement and link the description in ampdescript with actual measurements in amplitude.
   * 
   * @return amptype
   */
  public String getAmptype() {
    return amptype;
  }

  /**
   * Amplitude measure descriptor. This descriptor is used to uniquely identify an amplitude
   * measurement and link the description in ampdescript with actual measurements in amplitude.
   * 
   * @param amptype
   * @throws IllegalArgumentException if amptype.length() >= 8
   */
  public Amplitude setAmptype(String amptype) {
    if (amptype.length() > 8)
      throw new IllegalArgumentException(
          String.format("amptype.length() cannot be > 8.  amptype=%s", amptype));
    this.amptype = amptype;
    setHash(null);
    return this;
  }

  /**
   * Units of amplitude measure
   * 
   * @return units
   */
  public String getUnits() {
    return units;
  }

  /**
   * Units of amplitude measure
   * 
   * @param units
   * @throws IllegalArgumentException if units.length() >= 15
   */
  public Amplitude setUnits(String units) {
    if (units.length() > 15)
      throw new IllegalArgumentException(
          String.format("units.length() cannot be > 15.  units=%s", units));
    this.units = units;
    setHash(null);
    return this;
  }

  /**
   * Clipped data flag. This value is a single-character flag to indicate whether (c) or not (n) the
   * data was clipped
   * 
   * @return clip
   */
  public String getClip() {
    return clip;
  }

  /**
   * Clipped data flag. This value is a single-character flag to indicate whether (c) or not (n) the
   * data was clipped
   * 
   * @param clip
   * @throws IllegalArgumentException if clip.length() >= 1
   */
  public Amplitude setClip(String clip) {
    if (clip.length() > 1)
      throw new IllegalArgumentException(
          String.format("clip.length() cannot be > 1.  clip=%s", clip));
    this.clip = clip;
    setHash(null);
    return this;
  }

  /**
   * Flag to indicate whether or not <I>amp</I> is the same as it is in the arrival table
   * 
   * @return inarrival
   */
  public String getInarrival() {
    return inarrival;
  }

  /**
   * Flag to indicate whether or not <I>amp</I> is the same as it is in the arrival table
   * 
   * @param inarrival
   * @throws IllegalArgumentException if inarrival.length() >= 1
   */
  public Amplitude setInarrival(String inarrival) {
    if (inarrival.length() > 1)
      throw new IllegalArgumentException(
          String.format("inarrival.length() cannot be > 1.  inarrival=%s", inarrival));
    this.inarrival = inarrival;
    setHash(null);
    return this;
  }

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 20
   */
  public Amplitude setAuth(String auth) {
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
    return "NNSA KB Core";
  }

}
