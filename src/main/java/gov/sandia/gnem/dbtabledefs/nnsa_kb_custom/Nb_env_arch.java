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
 * nb_env_arch
 */
public class Nb_env_arch extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Envelope waveform identifier.
   */
  private long envwfid;

  static final public long ENVWFID_NA = Long.MIN_VALUE;

  /**
   * Waveform identifier of the seismogram from which measurement was made. See <B>wfdisc</B>.
   */
  private long wfid;

  static final public long WFID_NA = Long.MIN_VALUE;

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   */
  private long magyieldid;

  static final public long MAGYIELDID_NA = Long.MIN_VALUE;

  /**
   * Channel identifier. This value is a surrogate key used to uniquely identify a specific
   * recording. The column chanid duplicates the information of the compound key
   * <I>sta</I>/<I>chan</I>/<I>time</I>.
   */
  private long chanid;

  static final public long CHANID_NA = Long.MIN_VALUE;

  /**
   * Deconvolution identifier
   */
  private long deconid;

  static final public long DECONID_NA = Long.MIN_VALUE;

  /**
   * Instrument identifier.
   */
  private long inid;

  static final public long INID_NA = Long.MIN_VALUE;

  /**
   * Log pre-stack site correction applied to individual envelopes, most importantly from array
   * elements. Unitless log10 term.
   */
  private double presite;

  static final public double PRESITE_NA = -999;

  /**
   * Time shift used to stack envelope, primarily for array elements, undefined implies zero.
   * <p>
   * Units: s
   */
  private double timeshift;

  static final public double TIMESHIFT_NA = -999;

  /**
   * Calibration factor. This value is the conversion factor that maps digital data to earth
   * displacement. The factor holds true at the oscillation period specified by the column
   * <I>calper</I>. A positive value means ground motion increasing in component direction (up,
   * North, East) is indicated by increasing counts. A negative value means the opposite. The column
   * <I>calib</I> generally reflects the best calibration information available at the time of
   * recording, but refinement may be given in <B>sensor</B>, reflecting a subsequent recalibration
   * of the instrument (see <I>calratio</I>).
   * <p>
   * Units: nm/count
   */
  private double calib;

  static final public double CALIB_NA = Double.NaN;

  /**
   * Calibration period; gives the period for which <I>calib, ncalib,</I> and <I>calratio</I> are
   * valid.
   * <p>
   * Units: s
   */
  private double calper;

  static final public double CALPER_NA = Double.NaN;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Station-to-event azimuth calculated from the station and event locations and measured clockwise
   * from north.
   * <p>
   * Units: degree
   */
  private double seaz;

  static final public double SEAZ_NA = -1;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;

  /**
   * Unique comment identifier that points to free form comments in the <B> remark_master</B> or
   * <B>remark</B> tables. These comments store additional information about a record in another
   * table.
   */
  private long commid;

  static final public long COMMID_NA = -1;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("envwfid", Columns.FieldType.LONG, "%d");
    columns.add("wfid", Columns.FieldType.LONG, "%d");
    columns.add("magyieldid", Columns.FieldType.LONG, "%d");
    columns.add("chanid", Columns.FieldType.LONG, "%d");
    columns.add("deconid", Columns.FieldType.LONG, "%d");
    columns.add("inid", Columns.FieldType.LONG, "%d");
    columns.add("presite", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("timeshift", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("calib", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("calper", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("seaz", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Nb_env_arch(long envwfid, long wfid, long magyieldid, long chanid, long deconid, long inid,
      double presite, double timeshift, double calib, double calper, long orid, double seaz,
      String auth, long commid) {
    setValues(envwfid, wfid, magyieldid, chanid, deconid, inid, presite, timeshift, calib, calper,
        orid, seaz, auth, commid);
  }

  private void setValues(long envwfid, long wfid, long magyieldid, long chanid, long deconid,
      long inid, double presite, double timeshift, double calib, double calper, long orid,
      double seaz, String auth, long commid) {
    this.envwfid = envwfid;
    this.wfid = wfid;
    this.magyieldid = magyieldid;
    this.chanid = chanid;
    this.deconid = deconid;
    this.inid = inid;
    this.presite = presite;
    this.timeshift = timeshift;
    this.calib = calib;
    this.calper = calper;
    this.orid = orid;
    this.seaz = seaz;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Nb_env_arch(Nb_env_arch other) {
    this.envwfid = other.getEnvwfid();
    this.wfid = other.getWfid();
    this.magyieldid = other.getMagyieldid();
    this.chanid = other.getChanid();
    this.deconid = other.getDeconid();
    this.inid = other.getInid();
    this.presite = other.getPresite();
    this.timeshift = other.getTimeshift();
    this.calib = other.getCalib();
    this.calper = other.getCalper();
    this.orid = other.getOrid();
    this.seaz = other.getSeaz();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Nb_env_arch() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(ENVWFID_NA, WFID_NA, MAGYIELDID_NA, CHANID_NA, DECONID_NA, INID_NA, PRESITE_NA,
        TIMESHIFT_NA, CALIB_NA, CALPER_NA, ORID_NA, SEAZ_NA, AUTH_NA, COMMID_NA);
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
      case "presite":
        return presite;
      case "timeshift":
        return timeshift;
      case "calib":
        return calib;
      case "calper":
        return calper;
      case "seaz":
        return seaz;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "presite":
        presite = value;
        break;
      case "timeshift":
        timeshift = value;
        break;
      case "calib":
        calib = value;
        break;
      case "calper":
        calper = value;
        break;
      case "seaz":
        seaz = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "envwfid":
        return envwfid;
      case "wfid":
        return wfid;
      case "magyieldid":
        return magyieldid;
      case "chanid":
        return chanid;
      case "deconid":
        return deconid;
      case "inid":
        return inid;
      case "orid":
        return orid;
      case "commid":
        return commid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "envwfid":
        envwfid = value;
        break;
      case "wfid":
        wfid = value;
        break;
      case "magyieldid":
        magyieldid = value;
        break;
      case "chanid":
        chanid = value;
        break;
      case "deconid":
        deconid = value;
        break;
      case "inid":
        inid = value;
        break;
      case "orid":
        orid = value;
        break;
      case "commid":
        commid = value;
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
  public Nb_env_arch(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Nb_env_arch(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        input.readLong(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readLong(), input.readDouble(), readString(input),
        input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Nb_env_arch(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getLong(),
        input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getLong(), input.getDouble(), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Nb_env_arch(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Nb_env_arch(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getLong(offset + 11), input.getDouble(offset + 12),
        input.getString(offset + 13), input.getLong(offset + 14));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[14];
    values[0] = envwfid;
    values[1] = wfid;
    values[2] = magyieldid;
    values[3] = chanid;
    values[4] = deconid;
    values[5] = inid;
    values[6] = presite;
    values[7] = timeshift;
    values[8] = calib;
    values[9] = calper;
    values[10] = orid;
    values[11] = seaz;
    values[12] = auth;
    values[13] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[15];
    values[0] = envwfid;
    values[1] = wfid;
    values[2] = magyieldid;
    values[3] = chanid;
    values[4] = deconid;
    values[5] = inid;
    values[6] = presite;
    values[7] = timeshift;
    values[8] = calib;
    values[9] = calper;
    values[10] = orid;
    values[11] = seaz;
    values[12] = auth;
    values[13] = commid;
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
    output.writeLong(envwfid);
    output.writeLong(wfid);
    output.writeLong(magyieldid);
    output.writeLong(chanid);
    output.writeLong(deconid);
    output.writeLong(inid);
    output.writeDouble(presite);
    output.writeDouble(timeshift);
    output.writeDouble(calib);
    output.writeDouble(calper);
    output.writeLong(orid);
    output.writeDouble(seaz);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(envwfid);
    output.putLong(wfid);
    output.putLong(magyieldid);
    output.putLong(chanid);
    output.putLong(deconid);
    output.putLong(inid);
    output.putDouble(presite);
    output.putDouble(timeshift);
    output.putDouble(calib);
    output.putDouble(calper);
    output.putLong(orid);
    output.putDouble(seaz);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Nb_env_arch objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Nb_env_arch objects.
   * @throws IOException
   */
  static public void readNb_env_archs(BufferedReader input, Collection<Nb_env_arch> rows)
      throws IOException {
    String[] saved = Nb_env_arch.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Nb_env_arch
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Nb_env_arch(new Scanner(line)));
    }
    input.close();
    Nb_env_arch.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Nb_env_arch objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Nb_env_arch objects.
   * @throws IOException
   */
  static public void readNb_env_archs(File inputFile, Collection<Nb_env_arch> rows)
      throws IOException {
    readNb_env_archs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Nb_env_arch objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Nb_env_arch objects.
   * @throws IOException
   */
  static public void readNb_env_archs(InputStream inputStream, Collection<Nb_env_arch> rows)
      throws IOException {
    readNb_env_archs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Nb_env_arch objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Nb_env_arch objects
   * @throws IOException
   */
  static public Set<Nb_env_arch> readNb_env_archs(BufferedReader input) throws IOException {
    Set<Nb_env_arch> rows = new LinkedHashSet<Nb_env_arch>();
    readNb_env_archs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Nb_env_arch objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Nb_env_arch objects
   * @throws IOException
   */
  static public Set<Nb_env_arch> readNb_env_archs(File inputFile) throws IOException {
    return readNb_env_archs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Nb_env_arch objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Nb_env_arch objects
   * @throws IOException
   */
  static public Set<Nb_env_arch> readNb_env_archs(InputStream input) throws IOException {
    return readNb_env_archs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Nb_env_arch objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param nb_env_archs the Nb_env_arch objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Nb_env_arch> nb_env_archs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Nb_env_arch nb_env_arch : nb_env_archs)
      nb_env_arch.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Nb_env_arch objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param nb_env_archs the Nb_env_arch objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Nb_env_arch> nb_env_archs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Nb_env_arch nb_env_arch : nb_env_archs) {
        int i = 0;
        statement.setLong(++i, nb_env_arch.envwfid);
        statement.setLong(++i, nb_env_arch.wfid);
        statement.setLong(++i, nb_env_arch.magyieldid);
        statement.setLong(++i, nb_env_arch.chanid);
        statement.setLong(++i, nb_env_arch.deconid);
        statement.setLong(++i, nb_env_arch.inid);
        statement.setDouble(++i, nb_env_arch.presite);
        statement.setDouble(++i, nb_env_arch.timeshift);
        statement.setDouble(++i, nb_env_arch.calib);
        statement.setDouble(++i, nb_env_arch.calper);
        statement.setLong(++i, nb_env_arch.orid);
        statement.setDouble(++i, nb_env_arch.seaz);
        statement.setString(++i, nb_env_arch.auth);
        statement.setLong(++i, nb_env_arch.commid);
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
   *        Nb_env_arch table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Nb_env_arch> readNb_env_archs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Nb_env_arch> results = new HashSet<Nb_env_arch>();
    readNb_env_archs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Nb_env_arch table.
   * @param nb_env_archs
   * @throws SQLException
   */
  static public void readNb_env_archs(Connection connection, String selectStatement,
      Set<Nb_env_arch> nb_env_archs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        nb_env_archs.add(new Nb_env_arch(rs));
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
   * this Nb_env_arch object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Nb_env_arch object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "envwfid, wfid, magyieldid, chanid, deconid, inid, presite, timeshift, calib, calper, orid, seaz, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(envwfid)).append(", ");
    sql.append(Long.toString(wfid)).append(", ");
    sql.append(Long.toString(magyieldid)).append(", ");
    sql.append(Long.toString(chanid)).append(", ");
    sql.append(Long.toString(deconid)).append(", ");
    sql.append(Long.toString(inid)).append(", ");
    sql.append(Double.toString(presite)).append(", ");
    sql.append(Double.toString(timeshift)).append(", ");
    sql.append(Double.toString(calib)).append(", ");
    sql.append(Double.toString(calper)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Double.toString(seaz)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Nb_env_arch in the database. Primary and unique keys are set, if
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
   * Create a table of type Nb_env_arch in the database
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
   * Generate a sql script to create a table of type Nb_env_arch in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Nb_env_arch in the database
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
    buf.append("envwfid      number(9)            NOT NULL,\n");
    buf.append("wfid         number(9)            NOT NULL,\n");
    buf.append("magyieldid   number(9)            NOT NULL,\n");
    buf.append("chanid       number(8)            NOT NULL,\n");
    buf.append("deconid      number(9)            NOT NULL,\n");
    buf.append("inid         number(8)            NOT NULL,\n");
    buf.append("presite      float(24)            NOT NULL,\n");
    buf.append("timeshift    float(24)            NOT NULL,\n");
    buf.append("calib        float(24)            NOT NULL,\n");
    buf.append("calper       float(24)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("seaz         float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (envwfid,wfid)");
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
    return 128;
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
    return (other instanceof Nb_env_arch) && ((Nb_env_arch) other).envwfid == envwfid
        && ((Nb_env_arch) other).wfid == wfid;
  }

  /**
   * Envelope waveform identifier.
   * 
   * @return envwfid
   */
  public long getEnvwfid() {
    return envwfid;
  }

  /**
   * Envelope waveform identifier.
   * 
   * @param envwfid
   * @throws IllegalArgumentException if envwfid >= 1000000000
   */
  public Nb_env_arch setEnvwfid(long envwfid) {
    if (envwfid >= 1000000000L)
      throw new IllegalArgumentException("envwfid=" + envwfid + " but cannot be >= 1000000000");
    this.envwfid = envwfid;
    setHash(null);
    return this;
  }

  /**
   * Waveform identifier of the seismogram from which measurement was made. See <B>wfdisc</B>.
   * 
   * @return wfid
   */
  public long getWfid() {
    return wfid;
  }

  /**
   * Waveform identifier of the seismogram from which measurement was made. See <B>wfdisc</B>.
   * 
   * @param wfid
   * @throws IllegalArgumentException if wfid >= 1000000000
   */
  public Nb_env_arch setWfid(long wfid) {
    if (wfid >= 1000000000L)
      throw new IllegalArgumentException("wfid=" + wfid + " but cannot be >= 1000000000");
    this.wfid = wfid;
    setHash(null);
    return this;
  }

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   * 
   * @return magyieldid
   */
  public long getMagyieldid() {
    return magyieldid;
  }

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   * 
   * @param magyieldid
   * @throws IllegalArgumentException if magyieldid >= 1000000000
   */
  public Nb_env_arch setMagyieldid(long magyieldid) {
    if (magyieldid >= 1000000000L)
      throw new IllegalArgumentException(
          "magyieldid=" + magyieldid + " but cannot be >= 1000000000");
    this.magyieldid = magyieldid;
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
  public Nb_env_arch setChanid(long chanid) {
    if (chanid >= 100000000L)
      throw new IllegalArgumentException("chanid=" + chanid + " but cannot be >= 100000000");
    this.chanid = chanid;
    setHash(null);
    return this;
  }

  /**
   * Deconvolution identifier
   * 
   * @return deconid
   */
  public long getDeconid() {
    return deconid;
  }

  /**
   * Deconvolution identifier
   * 
   * @param deconid
   * @throws IllegalArgumentException if deconid >= 1000000000
   */
  public Nb_env_arch setDeconid(long deconid) {
    if (deconid >= 1000000000L)
      throw new IllegalArgumentException("deconid=" + deconid + " but cannot be >= 1000000000");
    this.deconid = deconid;
    setHash(null);
    return this;
  }

  /**
   * Instrument identifier.
   * 
   * @return inid
   */
  public long getInid() {
    return inid;
  }

  /**
   * Instrument identifier.
   * 
   * @param inid
   * @throws IllegalArgumentException if inid >= 100000000
   */
  public Nb_env_arch setInid(long inid) {
    if (inid >= 100000000L)
      throw new IllegalArgumentException("inid=" + inid + " but cannot be >= 100000000");
    this.inid = inid;
    setHash(null);
    return this;
  }

  /**
   * Log pre-stack site correction applied to individual envelopes, most importantly from array
   * elements. Unitless log10 term.
   * 
   * @return presite
   */
  public double getPresite() {
    return presite;
  }

  /**
   * Log pre-stack site correction applied to individual envelopes, most importantly from array
   * elements. Unitless log10 term.
   * 
   * @param presite
   */
  public Nb_env_arch setPresite(double presite) {
    this.presite = presite;
    setHash(null);
    return this;
  }

  /**
   * Time shift used to stack envelope, primarily for array elements, undefined implies zero.
   * <p>
   * Units: s
   * 
   * @return timeshift
   */
  public double getTimeshift() {
    return timeshift;
  }

  /**
   * Time shift used to stack envelope, primarily for array elements, undefined implies zero.
   * <p>
   * Units: s
   * 
   * @param timeshift
   */
  public Nb_env_arch setTimeshift(double timeshift) {
    this.timeshift = timeshift;
    setHash(null);
    return this;
  }

  /**
   * Calibration factor. This value is the conversion factor that maps digital data to earth
   * displacement. The factor holds true at the oscillation period specified by the column
   * <I>calper</I>. A positive value means ground motion increasing in component direction (up,
   * North, East) is indicated by increasing counts. A negative value means the opposite. The column
   * <I>calib</I> generally reflects the best calibration information available at the time of
   * recording, but refinement may be given in <B>sensor</B>, reflecting a subsequent recalibration
   * of the instrument (see <I>calratio</I>).
   * <p>
   * Units: nm/count
   * 
   * @return calib
   */
  public double getCalib() {
    return calib;
  }

  /**
   * Calibration factor. This value is the conversion factor that maps digital data to earth
   * displacement. The factor holds true at the oscillation period specified by the column
   * <I>calper</I>. A positive value means ground motion increasing in component direction (up,
   * North, East) is indicated by increasing counts. A negative value means the opposite. The column
   * <I>calib</I> generally reflects the best calibration information available at the time of
   * recording, but refinement may be given in <B>sensor</B>, reflecting a subsequent recalibration
   * of the instrument (see <I>calratio</I>).
   * <p>
   * Units: nm/count
   * 
   * @param calib
   */
  public Nb_env_arch setCalib(double calib) {
    this.calib = calib;
    setHash(null);
    return this;
  }

  /**
   * Calibration period; gives the period for which <I>calib, ncalib,</I> and <I>calratio</I> are
   * valid.
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
   * valid.
   * <p>
   * Units: s
   * 
   * @param calper
   */
  public Nb_env_arch setCalper(double calper) {
    this.calper = calper;
    setHash(null);
    return this;
  }

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   * 
   * @return orid
   */
  public long getOrid() {
    return orid;
  }

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   * 
   * @param orid
   * @throws IllegalArgumentException if orid >= 1000000000
   */
  public Nb_env_arch setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Station-to-event azimuth calculated from the station and event locations and measured clockwise
   * from north.
   * <p>
   * Units: degree
   * 
   * @return seaz
   */
  public double getSeaz() {
    return seaz;
  }

  /**
   * Station-to-event azimuth calculated from the station and event locations and measured clockwise
   * from north.
   * <p>
   * Units: degree
   * 
   * @param seaz
   */
  public Nb_env_arch setSeaz(double seaz) {
    this.seaz = seaz;
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
  public Nb_env_arch setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * Unique comment identifier that points to free form comments in the <B> remark_master</B> or
   * <B>remark</B> tables. These comments store additional information about a record in another
   * table.
   * 
   * @return commid
   */
  public long getCommid() {
    return commid;
  }

  /**
   * Unique comment identifier that points to free form comments in the <B> remark_master</B> or
   * <B>remark</B> tables. These comments store additional information about a record in another
   * table.
   * 
   * @param commid
   * @throws IllegalArgumentException if commid >= 1000000000
   */
  public Nb_env_arch setCommid(long commid) {
    if (commid >= 1000000000L)
      throw new IllegalArgumentException("commid=" + commid + " but cannot be >= 1000000000");
    this.commid = commid;
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
