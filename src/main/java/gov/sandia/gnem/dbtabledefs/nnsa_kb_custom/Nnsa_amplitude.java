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
 * nnsa_amplitude
 */
public class Nnsa_amplitude extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Amplitude identifier, unique for a particular frequency/period of a spectral or time domain
   * amplitude measurement.
   */
  private long ampid;

  static final public long AMPID_NA = Long.MIN_VALUE;

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   */
  private long windowid;

  static final public long WINDOWID_NA = Long.MIN_VALUE;

  /**
   * Measured amplitude. For <B>nnsa_amplitude</B>, the value in units attribute of type specified
   * by <I>f_t_type</I> and <I>meastype</I> attributes.
   * <p>
   * Units: nm (time domain), nm/Hz (frequency domain) - see units in table
   */
  private double amp;

  static final public double AMP_NA = Double.NaN;

  /**
   * Measured amplitude uncertainty, value in <I>units</I> attribute.
   * <p>
   * Units: nm (time domain), nm/Hz (frequency domain) - see units in table
   */
  private double delamp;

  static final public double DELAMP_NA = -1;

  /**
   * Frequency or period of amplitude measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   */
  private double f_t_value;

  static final public double F_T_VALUE_NA = Double.NaN;

  /**
   * Uncertainty of frequency or period for amplitude measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   */
  private double f_t_del;

  static final public double F_T_DEL_NA = -1;

  /**
   * Amplitude measurement type as defined below. This identifies how the attributes
   * <I>f_t_value</I>, <I>f_t_del</I>, <I>f_t_low</I> and <I>f_t_hi</I> are recorded in
   * <B>nnsa_amplitude</B>. FREQ - Frequency Domain Record PTOP - Pseudo Spectral Conversion of
   * Peak-to-Peak Time PENV - Pseudo Spectral Conversion of Peak Time Domain Envelope Measure TRMS -
   * Pseudo Spectral Conversion of Time Domain RMS Measure
   */
  private String f_t_type;

  static final public String F_T_TYPE_NA = null;

  /**
   * Lower frequency or period limit of amplitude measure, e.g. low pass of band-pass for time
   * domain measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   */
  private double f_t_low;

  static final public double F_T_LOW_NA = Double.NaN;

  /**
   * Higher frequency or period limit of amplitude measure, e.g. high pass of band-pass for time
   * domain measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   */
  private double f_t_hi;

  static final public double F_T_HI_NA = Double.NaN;

  /**
   * Units for amplitude measurement
   * <p>
   * Units: nm for time displacment, nm/Hz for frequency domain displacement
   */
  private String units;

  static final public String UNITS_NA = null;

  /**
   * Measurement algorithm type. Defined types include: freq_fft - frequency domain via fft td_rms -
   * frequency domain root mean square td_pp - time domain peak-to-peak td_pkenv - time domain peak
   * envelope td_coda - time domain coda envelope td_pspec - time domain psuedo spectrum
   */
  private String meastype;

  static final public String MEASTYPE_NA = null;

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   */
  private long corrid;

  static final public long CORRID_NA = -1;

  /**
   * Name of correction or other processing table applied to the amplitude. Examples are raw,
   * band-averaged, MDAC, phase-match filter, Coda, stack, beam, etc.
   */
  private String corrname;

  static final public String CORRNAME_NA = "-";

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
    columns.add("ampid", Columns.FieldType.LONG, "%d");
    columns.add("windowid", Columns.FieldType.LONG, "%d");
    columns.add("amp", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("delamp", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("f_t_value", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("f_t_del", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("f_t_type", Columns.FieldType.STRING, "%s");
    columns.add("f_t_low", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("f_t_hi", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("units", Columns.FieldType.STRING, "%s");
    columns.add("meastype", Columns.FieldType.STRING, "%s");
    columns.add("corrid", Columns.FieldType.LONG, "%d");
    columns.add("corrname", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Nnsa_amplitude(long ampid, long windowid, double amp, double delamp, double f_t_value,
      double f_t_del, String f_t_type, double f_t_low, double f_t_hi, String units, String meastype,
      long corrid, String corrname, String auth) {
    setValues(ampid, windowid, amp, delamp, f_t_value, f_t_del, f_t_type, f_t_low, f_t_hi, units,
        meastype, corrid, corrname, auth);
  }

  private void setValues(long ampid, long windowid, double amp, double delamp, double f_t_value,
      double f_t_del, String f_t_type, double f_t_low, double f_t_hi, String units, String meastype,
      long corrid, String corrname, String auth) {
    this.ampid = ampid;
    this.windowid = windowid;
    this.amp = amp;
    this.delamp = delamp;
    this.f_t_value = f_t_value;
    this.f_t_del = f_t_del;
    this.f_t_type = f_t_type;
    this.f_t_low = f_t_low;
    this.f_t_hi = f_t_hi;
    this.units = units;
    this.meastype = meastype;
    this.corrid = corrid;
    this.corrname = corrname;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Nnsa_amplitude(Nnsa_amplitude other) {
    this.ampid = other.getAmpid();
    this.windowid = other.getWindowid();
    this.amp = other.getAmp();
    this.delamp = other.getDelamp();
    this.f_t_value = other.getF_t_value();
    this.f_t_del = other.getF_t_del();
    this.f_t_type = other.getF_t_type();
    this.f_t_low = other.getF_t_low();
    this.f_t_hi = other.getF_t_hi();
    this.units = other.getUnits();
    this.meastype = other.getMeastype();
    this.corrid = other.getCorrid();
    this.corrname = other.getCorrname();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Nnsa_amplitude() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(AMPID_NA, WINDOWID_NA, AMP_NA, DELAMP_NA, F_T_VALUE_NA, F_T_DEL_NA, F_T_TYPE_NA,
        F_T_LOW_NA, F_T_HI_NA, UNITS_NA, MEASTYPE_NA, CORRID_NA, CORRNAME_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "f_t_type":
        return f_t_type;
      case "units":
        return units;
      case "meastype":
        return meastype;
      case "corrname":
        return corrname;
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
      case "f_t_type":
        f_t_type = value;
        break;
      case "units":
        units = value;
        break;
      case "meastype":
        meastype = value;
        break;
      case "corrname":
        corrname = value;
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
      case "delamp":
        return delamp;
      case "f_t_value":
        return f_t_value;
      case "f_t_del":
        return f_t_del;
      case "f_t_low":
        return f_t_low;
      case "f_t_hi":
        return f_t_hi;
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
      case "delamp":
        delamp = value;
        break;
      case "f_t_value":
        f_t_value = value;
        break;
      case "f_t_del":
        f_t_del = value;
        break;
      case "f_t_low":
        f_t_low = value;
        break;
      case "f_t_hi":
        f_t_hi = value;
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
      case "windowid":
        return windowid;
      case "corrid":
        return corrid;
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
      case "windowid":
        windowid = value;
        break;
      case "corrid":
        corrid = value;
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
  public Nnsa_amplitude(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Nnsa_amplitude(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), readString(input), input.readDouble(),
        input.readDouble(), readString(input), readString(input), input.readLong(),
        readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Nnsa_amplitude(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), readString(input), input.getDouble(), input.getDouble(),
        readString(input), readString(input), input.getLong(), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Nnsa_amplitude(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Nnsa_amplitude(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getString(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getString(offset + 10), input.getString(offset + 11), input.getLong(offset + 12),
        input.getString(offset + 13), input.getString(offset + 14));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[14];
    values[0] = ampid;
    values[1] = windowid;
    values[2] = amp;
    values[3] = delamp;
    values[4] = f_t_value;
    values[5] = f_t_del;
    values[6] = f_t_type;
    values[7] = f_t_low;
    values[8] = f_t_hi;
    values[9] = units;
    values[10] = meastype;
    values[11] = corrid;
    values[12] = corrname;
    values[13] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[15];
    values[0] = ampid;
    values[1] = windowid;
    values[2] = amp;
    values[3] = delamp;
    values[4] = f_t_value;
    values[5] = f_t_del;
    values[6] = f_t_type;
    values[7] = f_t_low;
    values[8] = f_t_hi;
    values[9] = units;
    values[10] = meastype;
    values[11] = corrid;
    values[12] = corrname;
    values[13] = auth;
    values[14] = lddate;
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
    output.writeLong(windowid);
    output.writeDouble(amp);
    output.writeDouble(delamp);
    output.writeDouble(f_t_value);
    output.writeDouble(f_t_del);
    writeString(output, f_t_type);
    output.writeDouble(f_t_low);
    output.writeDouble(f_t_hi);
    writeString(output, units);
    writeString(output, meastype);
    output.writeLong(corrid);
    writeString(output, corrname);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(ampid);
    output.putLong(windowid);
    output.putDouble(amp);
    output.putDouble(delamp);
    output.putDouble(f_t_value);
    output.putDouble(f_t_del);
    writeString(output, f_t_type);
    output.putDouble(f_t_low);
    output.putDouble(f_t_hi);
    writeString(output, units);
    writeString(output, meastype);
    output.putLong(corrid);
    writeString(output, corrname);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Nnsa_amplitude objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Nnsa_amplitude objects.
   * @throws IOException
   */
  static public void readNnsa_amplitudes(BufferedReader input, Collection<Nnsa_amplitude> rows)
      throws IOException {
    String[] saved = Nnsa_amplitude.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Nnsa_amplitude
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Nnsa_amplitude(new Scanner(line)));
    }
    input.close();
    Nnsa_amplitude.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Nnsa_amplitude objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Nnsa_amplitude objects.
   * @throws IOException
   */
  static public void readNnsa_amplitudes(File inputFile, Collection<Nnsa_amplitude> rows)
      throws IOException {
    readNnsa_amplitudes(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Nnsa_amplitude objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Nnsa_amplitude objects.
   * @throws IOException
   */
  static public void readNnsa_amplitudes(InputStream inputStream, Collection<Nnsa_amplitude> rows)
      throws IOException {
    readNnsa_amplitudes(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Nnsa_amplitude objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Nnsa_amplitude objects
   * @throws IOException
   */
  static public Set<Nnsa_amplitude> readNnsa_amplitudes(BufferedReader input) throws IOException {
    Set<Nnsa_amplitude> rows = new LinkedHashSet<Nnsa_amplitude>();
    readNnsa_amplitudes(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Nnsa_amplitude objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Nnsa_amplitude objects
   * @throws IOException
   */
  static public Set<Nnsa_amplitude> readNnsa_amplitudes(File inputFile) throws IOException {
    return readNnsa_amplitudes(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Nnsa_amplitude objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Nnsa_amplitude objects
   * @throws IOException
   */
  static public Set<Nnsa_amplitude> readNnsa_amplitudes(InputStream input) throws IOException {
    return readNnsa_amplitudes(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Nnsa_amplitude objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param nnsa_amplitudes the Nnsa_amplitude objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Nnsa_amplitude> nnsa_amplitudes)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Nnsa_amplitude nnsa_amplitude : nnsa_amplitudes)
      nnsa_amplitude.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Nnsa_amplitude objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param nnsa_amplitudes the Nnsa_amplitude objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Nnsa_amplitude> nnsa_amplitudes, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Nnsa_amplitude nnsa_amplitude : nnsa_amplitudes) {
        int i = 0;
        statement.setLong(++i, nnsa_amplitude.ampid);
        statement.setLong(++i, nnsa_amplitude.windowid);
        statement.setDouble(++i, nnsa_amplitude.amp);
        statement.setDouble(++i, nnsa_amplitude.delamp);
        statement.setDouble(++i, nnsa_amplitude.f_t_value);
        statement.setDouble(++i, nnsa_amplitude.f_t_del);
        statement.setString(++i, nnsa_amplitude.f_t_type);
        statement.setDouble(++i, nnsa_amplitude.f_t_low);
        statement.setDouble(++i, nnsa_amplitude.f_t_hi);
        statement.setString(++i, nnsa_amplitude.units);
        statement.setString(++i, nnsa_amplitude.meastype);
        statement.setLong(++i, nnsa_amplitude.corrid);
        statement.setString(++i, nnsa_amplitude.corrname);
        statement.setString(++i, nnsa_amplitude.auth);
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
   *        Nnsa_amplitude table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Nnsa_amplitude> readNnsa_amplitudes(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Nnsa_amplitude> results = new HashSet<Nnsa_amplitude>();
    readNnsa_amplitudes(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Nnsa_amplitude table.
   * @param nnsa_amplitudes
   * @throws SQLException
   */
  static public void readNnsa_amplitudes(Connection connection, String selectStatement,
      Set<Nnsa_amplitude> nnsa_amplitudes) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        nnsa_amplitudes.add(new Nnsa_amplitude(rs));
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
   * this Nnsa_amplitude object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Nnsa_amplitude object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "ampid, windowid, amp, delamp, f_t_value, f_t_del, f_t_type, f_t_low, f_t_hi, units, meastype, corrid, corrname, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(ampid)).append(", ");
    sql.append(Long.toString(windowid)).append(", ");
    sql.append(Double.toString(amp)).append(", ");
    sql.append(Double.toString(delamp)).append(", ");
    sql.append(Double.toString(f_t_value)).append(", ");
    sql.append(Double.toString(f_t_del)).append(", ");
    sql.append("'").append(f_t_type).append("', ");
    sql.append(Double.toString(f_t_low)).append(", ");
    sql.append(Double.toString(f_t_hi)).append(", ");
    sql.append("'").append(units).append("', ");
    sql.append("'").append(meastype).append("', ");
    sql.append(Long.toString(corrid)).append(", ");
    sql.append("'").append(corrname).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Nnsa_amplitude in the database. Primary and unique keys are set, if
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
   * Create a table of type Nnsa_amplitude in the database
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
   * Generate a sql script to create a table of type Nnsa_amplitude in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Nnsa_amplitude in the database
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
    buf.append("windowid     number(9)            NOT NULL,\n");
    buf.append("amp          float(24)            NOT NULL,\n");
    buf.append("delamp       float(24)            NOT NULL,\n");
    buf.append("f_t_value    float(24)            NOT NULL,\n");
    buf.append("f_t_del      float(24)            NOT NULL,\n");
    buf.append("f_t_type     varchar2(4)          NOT NULL,\n");
    buf.append("f_t_low      float(24)            NOT NULL,\n");
    buf.append("f_t_hi       float(24)            NOT NULL,\n");
    buf.append("units        varchar2(15)         NOT NULL,\n");
    buf.append("meastype     varchar2(12)         NOT NULL,\n");
    buf.append("corrid       number(9)            NOT NULL,\n");
    buf.append("corrname     varchar2(32)         NOT NULL,\n");
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
          + "_uk unique (windowid,f_t_type,f_t_low,f_t_hi,meastype,corrid)");
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
    return 175;
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
    return (other instanceof Nnsa_amplitude) && ((Nnsa_amplitude) other).ampid == ampid;
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
    return (other instanceof Nnsa_amplitude) && ((Nnsa_amplitude) other).windowid == windowid
        && ((Nnsa_amplitude) other).f_t_type.equals(f_t_type)
        && ((Nnsa_amplitude) other).f_t_low == f_t_low && ((Nnsa_amplitude) other).f_t_hi == f_t_hi
        && ((Nnsa_amplitude) other).meastype.equals(meastype)
        && ((Nnsa_amplitude) other).corrid == corrid;
  }

  /**
   * Amplitude identifier, unique for a particular frequency/period of a spectral or time domain
   * amplitude measurement.
   * 
   * @return ampid
   */
  public long getAmpid() {
    return ampid;
  }

  /**
   * Amplitude identifier, unique for a particular frequency/period of a spectral or time domain
   * amplitude measurement.
   * 
   * @param ampid
   * @throws IllegalArgumentException if ampid >= 1000000000
   */
  public Nnsa_amplitude setAmpid(long ampid) {
    if (ampid >= 1000000000L)
      throw new IllegalArgumentException("ampid=" + ampid + " but cannot be >= 1000000000");
    this.ampid = ampid;
    setHash(null);
    return this;
  }

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   * 
   * @return windowid
   */
  public long getWindowid() {
    return windowid;
  }

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   * 
   * @param windowid
   * @throws IllegalArgumentException if windowid >= 1000000000
   */
  public Nnsa_amplitude setWindowid(long windowid) {
    if (windowid >= 1000000000L)
      throw new IllegalArgumentException("windowid=" + windowid + " but cannot be >= 1000000000");
    this.windowid = windowid;
    setHash(null);
    return this;
  }

  /**
   * Measured amplitude. For <B>nnsa_amplitude</B>, the value in units attribute of type specified
   * by <I>f_t_type</I> and <I>meastype</I> attributes.
   * <p>
   * Units: nm (time domain), nm/Hz (frequency domain) - see units in table
   * 
   * @return amp
   */
  public double getAmp() {
    return amp;
  }

  /**
   * Measured amplitude. For <B>nnsa_amplitude</B>, the value in units attribute of type specified
   * by <I>f_t_type</I> and <I>meastype</I> attributes.
   * <p>
   * Units: nm (time domain), nm/Hz (frequency domain) - see units in table
   * 
   * @param amp
   */
  public Nnsa_amplitude setAmp(double amp) {
    this.amp = amp;
    setHash(null);
    return this;
  }

  /**
   * Measured amplitude uncertainty, value in <I>units</I> attribute.
   * <p>
   * Units: nm (time domain), nm/Hz (frequency domain) - see units in table
   * 
   * @return delamp
   */
  public double getDelamp() {
    return delamp;
  }

  /**
   * Measured amplitude uncertainty, value in <I>units</I> attribute.
   * <p>
   * Units: nm (time domain), nm/Hz (frequency domain) - see units in table
   * 
   * @param delamp
   */
  public Nnsa_amplitude setDelamp(double delamp) {
    this.delamp = delamp;
    setHash(null);
    return this;
  }

  /**
   * Frequency or period of amplitude measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   * 
   * @return f_t_value
   */
  public double getF_t_value() {
    return f_t_value;
  }

  /**
   * Frequency or period of amplitude measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   * 
   * @param f_t_value
   */
  public Nnsa_amplitude setF_t_value(double f_t_value) {
    this.f_t_value = f_t_value;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty of frequency or period for amplitude measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   * 
   * @return f_t_del
   */
  public double getF_t_del() {
    return f_t_del;
  }

  /**
   * Uncertainty of frequency or period for amplitude measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   * 
   * @param f_t_del
   */
  public Nnsa_amplitude setF_t_del(double f_t_del) {
    this.f_t_del = f_t_del;
    setHash(null);
    return this;
  }

  /**
   * Amplitude measurement type as defined below. This identifies how the attributes
   * <I>f_t_value</I>, <I>f_t_del</I>, <I>f_t_low</I> and <I>f_t_hi</I> are recorded in
   * <B>nnsa_amplitude</B>. FREQ - Frequency Domain Record PTOP - Pseudo Spectral Conversion of
   * Peak-to-Peak Time PENV - Pseudo Spectral Conversion of Peak Time Domain Envelope Measure TRMS -
   * Pseudo Spectral Conversion of Time Domain RMS Measure
   * 
   * @return f_t_type
   */
  public String getF_t_type() {
    return f_t_type;
  }

  /**
   * Amplitude measurement type as defined below. This identifies how the attributes
   * <I>f_t_value</I>, <I>f_t_del</I>, <I>f_t_low</I> and <I>f_t_hi</I> are recorded in
   * <B>nnsa_amplitude</B>. FREQ - Frequency Domain Record PTOP - Pseudo Spectral Conversion of
   * Peak-to-Peak Time PENV - Pseudo Spectral Conversion of Peak Time Domain Envelope Measure TRMS -
   * Pseudo Spectral Conversion of Time Domain RMS Measure
   * 
   * @param f_t_type
   * @throws IllegalArgumentException if f_t_type.length() >= 4
   */
  public Nnsa_amplitude setF_t_type(String f_t_type) {
    if (f_t_type.length() > 4)
      throw new IllegalArgumentException(
          String.format("f_t_type.length() cannot be > 4.  f_t_type=%s", f_t_type));
    this.f_t_type = f_t_type;
    setHash(null);
    return this;
  }

  /**
   * Lower frequency or period limit of amplitude measure, e.g. low pass of band-pass for time
   * domain measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   * 
   * @return f_t_low
   */
  public double getF_t_low() {
    return f_t_low;
  }

  /**
   * Lower frequency or period limit of amplitude measure, e.g. low pass of band-pass for time
   * domain measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   * 
   * @param f_t_low
   */
  public Nnsa_amplitude setF_t_low(double f_t_low) {
    this.f_t_low = f_t_low;
    setHash(null);
    return this;
  }

  /**
   * Higher frequency or period limit of amplitude measure, e.g. high pass of band-pass for time
   * domain measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   * 
   * @return f_t_hi
   */
  public double getF_t_hi() {
    return f_t_hi;
  }

  /**
   * Higher frequency or period limit of amplitude measure, e.g. high pass of band-pass for time
   * domain measurement.
   * <p>
   * Units: Hz (frequency); s (period)
   * 
   * @param f_t_hi
   */
  public Nnsa_amplitude setF_t_hi(double f_t_hi) {
    this.f_t_hi = f_t_hi;
    setHash(null);
    return this;
  }

  /**
   * Units for amplitude measurement
   * <p>
   * Units: nm for time displacment, nm/Hz for frequency domain displacement
   * 
   * @return units
   */
  public String getUnits() {
    return units;
  }

  /**
   * Units for amplitude measurement
   * <p>
   * Units: nm for time displacment, nm/Hz for frequency domain displacement
   * 
   * @param units
   * @throws IllegalArgumentException if units.length() >= 15
   */
  public Nnsa_amplitude setUnits(String units) {
    if (units.length() > 15)
      throw new IllegalArgumentException(
          String.format("units.length() cannot be > 15.  units=%s", units));
    this.units = units;
    setHash(null);
    return this;
  }

  /**
   * Measurement algorithm type. Defined types include: freq_fft - frequency domain via fft td_rms -
   * frequency domain root mean square td_pp - time domain peak-to-peak td_pkenv - time domain peak
   * envelope td_coda - time domain coda envelope td_pspec - time domain psuedo spectrum
   * 
   * @return meastype
   */
  public String getMeastype() {
    return meastype;
  }

  /**
   * Measurement algorithm type. Defined types include: freq_fft - frequency domain via fft td_rms -
   * frequency domain root mean square td_pp - time domain peak-to-peak td_pkenv - time domain peak
   * envelope td_coda - time domain coda envelope td_pspec - time domain psuedo spectrum
   * 
   * @param meastype
   * @throws IllegalArgumentException if meastype.length() >= 12
   */
  public Nnsa_amplitude setMeastype(String meastype) {
    if (meastype.length() > 12)
      throw new IllegalArgumentException(
          String.format("meastype.length() cannot be > 12.  meastype=%s", meastype));
    this.meastype = meastype;
    setHash(null);
    return this;
  }

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   * 
   * @return corrid
   */
  public long getCorrid() {
    return corrid;
  }

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   * 
   * @param corrid
   * @throws IllegalArgumentException if corrid >= 1000000000
   */
  public Nnsa_amplitude setCorrid(long corrid) {
    if (corrid >= 1000000000L)
      throw new IllegalArgumentException("corrid=" + corrid + " but cannot be >= 1000000000");
    this.corrid = corrid;
    setHash(null);
    return this;
  }

  /**
   * Name of correction or other processing table applied to the amplitude. Examples are raw,
   * band-averaged, MDAC, phase-match filter, Coda, stack, beam, etc.
   * 
   * @return corrname
   */
  public String getCorrname() {
    return corrname;
  }

  /**
   * Name of correction or other processing table applied to the amplitude. Examples are raw,
   * band-averaged, MDAC, phase-match filter, Coda, stack, beam, etc.
   * 
   * @param corrname
   * @throws IllegalArgumentException if corrname.length() >= 32
   */
  public Nnsa_amplitude setCorrname(String corrname) {
    if (corrname.length() > 32)
      throw new IllegalArgumentException(
          String.format("corrname.length() cannot be > 32.  corrname=%s", corrname));
    this.corrname = corrname;
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
  public Nnsa_amplitude setAuth(String auth) {
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
