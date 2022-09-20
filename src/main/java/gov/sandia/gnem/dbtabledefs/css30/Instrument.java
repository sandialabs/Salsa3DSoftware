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
package gov.sandia.gnem.dbtabledefs.css30;

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
 * instrument
 */
public class Instrument extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Instrument identifier. This is a unique key to the instrument relation. Inid provides the only
   * link between sensor and instrument.
   */
  private long inid;

  static final public long INID_NA = Long.MIN_VALUE;

  /**
   * Instrument name. This is a character string containing the name of the instrument.
   */
  private String insname;

  static final public String INSNAME_NA = "-";

  /**
   * Instrument type. This character string is used to indicate the instrument type. Some example
   * are: SRO, ASRO, DWWSSN, LRSM, and S-750.
   */
  private String instype;

  static final public String INSTYPE_NA = "-";

  /**
   * Frequency band. This is a qualitative indicator of frequency pass-band for an instrument.
   * Values should reflect the response curve rather than just the sample rate. Recommended values
   * are: s (short-period) m (mid-period) i (intermediate-period) l (long-period) b (broad-band) h
   * (high frequency, very short-period) v (very long-period) For a better notion of the instrument
   * characteristics, see the instrument response curve.
   */
  private String band;

  static final public String BAND_NA = "-";

  /**
   * Digital/Analog. This attribute is a single character flag denoting whether this instrument
   * record describes an analog or digital recording system.
   */
  private String digital;

  static final public String DIGITAL_NA = "-";

  /**
   * Sampling rate. This attribute is the sample rate in samples/second. In the instrument relation
   * this is specifically the nominal sample rate, not accounting for clock drift. In wfdisc, the
   * value may vary slightly from the nominal to reflect clock drift.
   * <p>
   * Units: 1/s
   */
  private double samprate;

  static final public double SAMPRATE_NA = Double.NaN;

  /**
   * Nominal calibration factor. This is the conversion factor that maps digital data to earth
   * displacement. The factor holds true at the oscillation period specified by ncalper. A positive
   * value means ground motion increasing in component direction (up, north, east) is indicated by
   * increasing counts. A negative value means the opposite. Actual calibration for a particular
   * recording is determined using the wfdisc and sensor relations. See calratio.
   * <p>
   * Units: nm/count
   */
  private double ncalib;

  static final public double NCALIB_NA = Double.NaN;

  /**
   * Calibration period. This attribute is the period for which ncalib is valid.
   */
  private double ncalper;

  static final public double NCALPER_NA = Double.NaN;

  /**
   * Directory. This attribute is the directory-part of a path name. Relative path names or "."
   * (dot), the notation for the current directory, may be used.
   */
  private String dir;

  static final public String DIR_NA = null;

  /**
   * Data file. In wfdisc, this is the file name of a disk-based waveform file. In instrument, this
   * points to an instrument response file. See dir.
   */
  private String dfile;

  static final public String DFILE_NA = null;

  /**
   * Instrument response type. This denotes the style in which detailed calibration data are stored.
   * The neighboring attribute dfile tells where the calibration data are saved. When rsptype = paz,
   * it indicates the data are the poles and zeroes of the Laplace transform. rsptype = fap
   * indicates they are amplitude/phase values at a range of frequencies. rsptype = fir indicates it
   * is a finite impulse response table. rsptype = pazfir indicates a combination of poles, zeros
   * and finite impulse response. Other codes may be defined.
   */
  private String rsptype;

  static final public String RSPTYPE_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("inid", Columns.FieldType.LONG, "%d");
    columns.add("insname", Columns.FieldType.STRING, "%s");
    columns.add("instype", Columns.FieldType.STRING, "%s");
    columns.add("band", Columns.FieldType.STRING, "%s");
    columns.add("digital", Columns.FieldType.STRING, "%s");
    columns.add("samprate", Columns.FieldType.DOUBLE, "%1.7f");
    columns.add("ncalib", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("ncalper", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("dir", Columns.FieldType.STRING, "%s");
    columns.add("dfile", Columns.FieldType.STRING, "%s");
    columns.add("rsptype", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Instrument(long inid, String insname, String instype, String band, String digital,
      double samprate, double ncalib, double ncalper, String dir, String dfile, String rsptype) {
    setValues(inid, insname, instype, band, digital, samprate, ncalib, ncalper, dir, dfile,
        rsptype);
  }

  private void setValues(long inid, String insname, String instype, String band, String digital,
      double samprate, double ncalib, double ncalper, String dir, String dfile, String rsptype) {
    this.inid = inid;
    this.insname = insname;
    this.instype = instype;
    this.band = band;
    this.digital = digital;
    this.samprate = samprate;
    this.ncalib = ncalib;
    this.ncalper = ncalper;
    this.dir = dir;
    this.dfile = dfile;
    this.rsptype = rsptype;
  }

  /**
   * Copy constructor.
   */
  public Instrument(Instrument other) {
    this.inid = other.getInid();
    this.insname = other.getInsname();
    this.instype = other.getInstype();
    this.band = other.getBand();
    this.digital = other.getDigital();
    this.samprate = other.getSamprate();
    this.ncalib = other.getNcalib();
    this.ncalper = other.getNcalper();
    this.dir = other.getDir();
    this.dfile = other.getDfile();
    this.rsptype = other.getRsptype();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Instrument() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(INID_NA, INSNAME_NA, INSTYPE_NA, BAND_NA, DIGITAL_NA, SAMPRATE_NA, NCALIB_NA,
        NCALPER_NA, DIR_NA, DFILE_NA, RSPTYPE_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "insname":
        return insname;
      case "instype":
        return instype;
      case "band":
        return band;
      case "digital":
        return digital;
      case "dir":
        return dir;
      case "dfile":
        return dfile;
      case "rsptype":
        return rsptype;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "insname":
        insname = value;
        break;
      case "instype":
        instype = value;
        break;
      case "band":
        band = value;
        break;
      case "digital":
        digital = value;
        break;
      case "dir":
        dir = value;
        break;
      case "dfile":
        dfile = value;
        break;
      case "rsptype":
        rsptype = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "samprate":
        return samprate;
      case "ncalib":
        return ncalib;
      case "ncalper":
        return ncalper;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "samprate":
        samprate = value;
        break;
      case "ncalib":
        ncalib = value;
        break;
      case "ncalper":
        ncalper = value;
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
  public Instrument(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Instrument(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), readString(input),
        readString(input), input.readDouble(), input.readDouble(), input.readDouble(),
        readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Instrument(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), readString(input),
        readString(input), input.getDouble(), input.getDouble(), input.getDouble(),
        readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Instrument(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Instrument(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getString(offset + 9),
        input.getString(offset + 10), input.getString(offset + 11));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[11];
    values[0] = inid;
    values[1] = insname;
    values[2] = instype;
    values[3] = band;
    values[4] = digital;
    values[5] = samprate;
    values[6] = ncalib;
    values[7] = ncalper;
    values[8] = dir;
    values[9] = dfile;
    values[10] = rsptype;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[12];
    values[0] = inid;
    values[1] = insname;
    values[2] = instype;
    values[3] = band;
    values[4] = digital;
    values[5] = samprate;
    values[6] = ncalib;
    values[7] = ncalper;
    values[8] = dir;
    values[9] = dfile;
    values[10] = rsptype;
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
    output.writeLong(inid);
    writeString(output, insname);
    writeString(output, instype);
    writeString(output, band);
    writeString(output, digital);
    output.writeDouble(samprate);
    output.writeDouble(ncalib);
    output.writeDouble(ncalper);
    writeString(output, dir);
    writeString(output, dfile);
    writeString(output, rsptype);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(inid);
    writeString(output, insname);
    writeString(output, instype);
    writeString(output, band);
    writeString(output, digital);
    output.putDouble(samprate);
    output.putDouble(ncalib);
    output.putDouble(ncalper);
    writeString(output, dir);
    writeString(output, dfile);
    writeString(output, rsptype);
  }

  /**
   * Read a Collection of Instrument objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Instrument objects.
   * @throws IOException
   */
  static public void readInstruments(BufferedReader input, Collection<Instrument> rows)
      throws IOException {
    String[] saved = Instrument.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Instrument
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Instrument(new Scanner(line)));
    }
    input.close();
    Instrument.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Instrument objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Instrument objects.
   * @throws IOException
   */
  static public void readInstruments(File inputFile, Collection<Instrument> rows)
      throws IOException {
    readInstruments(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Instrument objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Instrument objects.
   * @throws IOException
   */
  static public void readInstruments(InputStream inputStream, Collection<Instrument> rows)
      throws IOException {
    readInstruments(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Instrument objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Instrument objects
   * @throws IOException
   */
  static public Set<Instrument> readInstruments(BufferedReader input) throws IOException {
    Set<Instrument> rows = new LinkedHashSet<Instrument>();
    readInstruments(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Instrument objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Instrument objects
   * @throws IOException
   */
  static public Set<Instrument> readInstruments(File inputFile) throws IOException {
    return readInstruments(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Instrument objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Instrument objects
   * @throws IOException
   */
  static public Set<Instrument> readInstruments(InputStream input) throws IOException {
    return readInstruments(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Instrument objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param instruments the Instrument objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Instrument> instruments)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Instrument instrument : instruments)
      instrument.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Instrument objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param instruments the Instrument objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Instrument> instruments, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Instrument instrument : instruments) {
        int i = 0;
        statement.setLong(++i, instrument.inid);
        statement.setString(++i, instrument.insname);
        statement.setString(++i, instrument.instype);
        statement.setString(++i, instrument.band);
        statement.setString(++i, instrument.digital);
        statement.setDouble(++i, instrument.samprate);
        statement.setDouble(++i, instrument.ncalib);
        statement.setDouble(++i, instrument.ncalper);
        statement.setString(++i, instrument.dir);
        statement.setString(++i, instrument.dfile);
        statement.setString(++i, instrument.rsptype);
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
   *        Instrument table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Instrument> readInstruments(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Instrument> results = new HashSet<Instrument>();
    readInstruments(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Instrument table.
   * @param instruments
   * @throws SQLException
   */
  static public void readInstruments(Connection connection, String selectStatement,
      Set<Instrument> instruments) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        instruments.add(new Instrument(rs));
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
   * this Instrument object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Instrument object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "inid, insname, instype, band, digital, samprate, ncalib, ncalper, dir, dfile, rsptype, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(inid)).append(", ");
    sql.append("'").append(insname).append("', ");
    sql.append("'").append(instype).append("', ");
    sql.append("'").append(band).append("', ");
    sql.append("'").append(digital).append("', ");
    sql.append(Double.toString(samprate)).append(", ");
    sql.append(Double.toString(ncalib)).append(", ");
    sql.append(Double.toString(ncalper)).append(", ");
    sql.append("'").append(dir).append("', ");
    sql.append("'").append(dfile).append("', ");
    sql.append("'").append(rsptype).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Instrument in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Instrument in the database
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
   * Generate a sql script to create a table of type Instrument in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Instrument in the database
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
    buf.append("inid         number(8)            NOT NULL,\n");
    buf.append("insname      varchar2(50)         NOT NULL,\n");
    buf.append("instype      varchar2(6)          NOT NULL,\n");
    buf.append("band         varchar2(1)          NOT NULL,\n");
    buf.append("digital      varchar2(1)          NOT NULL,\n");
    buf.append("samprate     float(24)            NOT NULL,\n");
    buf.append("ncalib       float(24)            NOT NULL,\n");
    buf.append("ncalper      float(24)            NOT NULL,\n");
    buf.append("dir          varchar2(64)         NOT NULL,\n");
    buf.append("dfile        varchar2(32)         NOT NULL,\n");
    buf.append("rsptype      varchar2(6)          NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (inid)");
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
    return 220;
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
    return (other instanceof Instrument) && ((Instrument) other).inid == inid;
  }

  /**
   * Instrument identifier. This is a unique key to the instrument relation. Inid provides the only
   * link between sensor and instrument.
   * 
   * @return inid
   */
  public long getInid() {
    return inid;
  }

  /**
   * Instrument identifier. This is a unique key to the instrument relation. Inid provides the only
   * link between sensor and instrument.
   * 
   * @param inid
   * @throws IllegalArgumentException if inid >= 100000000
   */
  public Instrument setInid(long inid) {
    if (inid >= 100000000L)
      throw new IllegalArgumentException("inid=" + inid + " but cannot be >= 100000000");
    this.inid = inid;
    setHash(null);
    return this;
  }

  /**
   * Instrument name. This is a character string containing the name of the instrument.
   * 
   * @return insname
   */
  public String getInsname() {
    return insname;
  }

  /**
   * Instrument name. This is a character string containing the name of the instrument.
   * 
   * @param insname
   * @throws IllegalArgumentException if insname.length() >= 50
   */
  public Instrument setInsname(String insname) {
    if (insname.length() > 50)
      throw new IllegalArgumentException(
          String.format("insname.length() cannot be > 50.  insname=%s", insname));
    this.insname = insname;
    setHash(null);
    return this;
  }

  /**
   * Instrument type. This character string is used to indicate the instrument type. Some example
   * are: SRO, ASRO, DWWSSN, LRSM, and S-750.
   * 
   * @return instype
   */
  public String getInstype() {
    return instype;
  }

  /**
   * Instrument type. This character string is used to indicate the instrument type. Some example
   * are: SRO, ASRO, DWWSSN, LRSM, and S-750.
   * 
   * @param instype
   * @throws IllegalArgumentException if instype.length() >= 6
   */
  public Instrument setInstype(String instype) {
    if (instype.length() > 6)
      throw new IllegalArgumentException(
          String.format("instype.length() cannot be > 6.  instype=%s", instype));
    this.instype = instype;
    setHash(null);
    return this;
  }

  /**
   * Frequency band. This is a qualitative indicator of frequency pass-band for an instrument.
   * Values should reflect the response curve rather than just the sample rate. Recommended values
   * are: s (short-period) m (mid-period) i (intermediate-period) l (long-period) b (broad-band) h
   * (high frequency, very short-period) v (very long-period) For a better notion of the instrument
   * characteristics, see the instrument response curve.
   * 
   * @return band
   */
  public String getBand() {
    return band;
  }

  /**
   * Frequency band. This is a qualitative indicator of frequency pass-band for an instrument.
   * Values should reflect the response curve rather than just the sample rate. Recommended values
   * are: s (short-period) m (mid-period) i (intermediate-period) l (long-period) b (broad-band) h
   * (high frequency, very short-period) v (very long-period) For a better notion of the instrument
   * characteristics, see the instrument response curve.
   * 
   * @param band
   * @throws IllegalArgumentException if band.length() >= 1
   */
  public Instrument setBand(String band) {
    if (band.length() > 1)
      throw new IllegalArgumentException(
          String.format("band.length() cannot be > 1.  band=%s", band));
    this.band = band;
    setHash(null);
    return this;
  }

  /**
   * Digital/Analog. This attribute is a single character flag denoting whether this instrument
   * record describes an analog or digital recording system.
   * 
   * @return digital
   */
  public String getDigital() {
    return digital;
  }

  /**
   * Digital/Analog. This attribute is a single character flag denoting whether this instrument
   * record describes an analog or digital recording system.
   * 
   * @param digital
   * @throws IllegalArgumentException if digital.length() >= 1
   */
  public Instrument setDigital(String digital) {
    if (digital.length() > 1)
      throw new IllegalArgumentException(
          String.format("digital.length() cannot be > 1.  digital=%s", digital));
    this.digital = digital;
    setHash(null);
    return this;
  }

  /**
   * Sampling rate. This attribute is the sample rate in samples/second. In the instrument relation
   * this is specifically the nominal sample rate, not accounting for clock drift. In wfdisc, the
   * value may vary slightly from the nominal to reflect clock drift.
   * <p>
   * Units: 1/s
   * 
   * @return samprate
   */
  public double getSamprate() {
    return samprate;
  }

  /**
   * Sampling rate. This attribute is the sample rate in samples/second. In the instrument relation
   * this is specifically the nominal sample rate, not accounting for clock drift. In wfdisc, the
   * value may vary slightly from the nominal to reflect clock drift.
   * <p>
   * Units: 1/s
   * 
   * @param samprate
   */
  public Instrument setSamprate(double samprate) {
    this.samprate = samprate;
    setHash(null);
    return this;
  }

  /**
   * Nominal calibration factor. This is the conversion factor that maps digital data to earth
   * displacement. The factor holds true at the oscillation period specified by ncalper. A positive
   * value means ground motion increasing in component direction (up, north, east) is indicated by
   * increasing counts. A negative value means the opposite. Actual calibration for a particular
   * recording is determined using the wfdisc and sensor relations. See calratio.
   * <p>
   * Units: nm/count
   * 
   * @return ncalib
   */
  public double getNcalib() {
    return ncalib;
  }

  /**
   * Nominal calibration factor. This is the conversion factor that maps digital data to earth
   * displacement. The factor holds true at the oscillation period specified by ncalper. A positive
   * value means ground motion increasing in component direction (up, north, east) is indicated by
   * increasing counts. A negative value means the opposite. Actual calibration for a particular
   * recording is determined using the wfdisc and sensor relations. See calratio.
   * <p>
   * Units: nm/count
   * 
   * @param ncalib
   */
  public Instrument setNcalib(double ncalib) {
    this.ncalib = ncalib;
    setHash(null);
    return this;
  }

  /**
   * Calibration period. This attribute is the period for which ncalib is valid.
   * 
   * @return ncalper
   */
  public double getNcalper() {
    return ncalper;
  }

  /**
   * Calibration period. This attribute is the period for which ncalib is valid.
   * 
   * @param ncalper
   */
  public Instrument setNcalper(double ncalper) {
    this.ncalper = ncalper;
    setHash(null);
    return this;
  }

  /**
   * Directory. This attribute is the directory-part of a path name. Relative path names or "."
   * (dot), the notation for the current directory, may be used.
   * 
   * @return dir
   */
  public String getDir() {
    return dir;
  }

  /**
   * Directory. This attribute is the directory-part of a path name. Relative path names or "."
   * (dot), the notation for the current directory, may be used.
   * 
   * @param dir
   * @throws IllegalArgumentException if dir.length() >= 64
   */
  public Instrument setDir(String dir) {
    if (dir.length() > 64)
      throw new IllegalArgumentException(
          String.format("dir.length() cannot be > 64.  dir=%s", dir));
    this.dir = dir;
    setHash(null);
    return this;
  }

  /**
   * Data file. In wfdisc, this is the file name of a disk-based waveform file. In instrument, this
   * points to an instrument response file. See dir.
   * 
   * @return dfile
   */
  public String getDfile() {
    return dfile;
  }

  /**
   * Data file. In wfdisc, this is the file name of a disk-based waveform file. In instrument, this
   * points to an instrument response file. See dir.
   * 
   * @param dfile
   * @throws IllegalArgumentException if dfile.length() >= 32
   */
  public Instrument setDfile(String dfile) {
    if (dfile.length() > 32)
      throw new IllegalArgumentException(
          String.format("dfile.length() cannot be > 32.  dfile=%s", dfile));
    this.dfile = dfile;
    setHash(null);
    return this;
  }

  /**
   * Instrument response type. This denotes the style in which detailed calibration data are stored.
   * The neighboring attribute dfile tells where the calibration data are saved. When rsptype = paz,
   * it indicates the data are the poles and zeroes of the Laplace transform. rsptype = fap
   * indicates they are amplitude/phase values at a range of frequencies. rsptype = fir indicates it
   * is a finite impulse response table. rsptype = pazfir indicates a combination of poles, zeros
   * and finite impulse response. Other codes may be defined.
   * 
   * @return rsptype
   */
  public String getRsptype() {
    return rsptype;
  }

  /**
   * Instrument response type. This denotes the style in which detailed calibration data are stored.
   * The neighboring attribute dfile tells where the calibration data are saved. When rsptype = paz,
   * it indicates the data are the poles and zeroes of the Laplace transform. rsptype = fap
   * indicates they are amplitude/phase values at a range of frequencies. rsptype = fir indicates it
   * is a finite impulse response table. rsptype = pazfir indicates a combination of poles, zeros
   * and finite impulse response. Other codes may be defined.
   * 
   * @param rsptype
   * @throws IllegalArgumentException if rsptype.length() >= 6
   */
  public Instrument setRsptype(String rsptype) {
    if (rsptype.length() > 6)
      throw new IllegalArgumentException(
          String.format("rsptype.length() cannot be > 6.  rsptype=%s", rsptype));
    this.rsptype = rsptype;
    setHash(null);
    return this;
  }

  /**
   * Retrieve the name of the schema.
   * 
   * @return schema name
   */
  static public String getSchemaName() {
    return "CSS3.0";
  }

}
