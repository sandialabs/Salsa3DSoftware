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
 * env_peak
 */
public class Env_peak extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   */
  private long magyieldid;

  static final public long MAGYIELDID_NA = Long.MIN_VALUE;

  /**
   * Group velocity two-dimensional correction identifier.
   */
  private long vg2dcorid;

  static final public long VG2DCORID_NA = -1;

  /**
   * First of three distance hyperbola parameters used to compute the group velocity of the envelope
   * peak (Mayeda et al., 2003).
   * <p>
   * Units: km/s
   */
  private double vg0;

  static final public double VG0_NA = Double.NaN;

  /**
   * Error on the first of three distance hyperbola parameters used to compute the group velocity of
   * the envelope peak (Mayeda et al., 2003).
   * 
   * <p>
   * Units: km/s
   */
  private double dvg0;

  static final public double DVG0_NA = -1;

  /**
   * Second of three distance hyperbola parameters used to compute the group velocity of the
   * envelope peak (Mayeda et al., 2003); undefined implies zero.
   * <p>
   * Units: km^2/s
   */
  private double vg1;

  static final public double VG1_NA = Double.NaN;

  /**
   * Error on the second of three distance hyperbola parameters used to compute the group velocity
   * of the envelope peak (Mayeda et al., 2003).
   * <p>
   * Units: km^2/s
   */
  private double dvg1;

  static final public double DVG1_NA = -1;

  /**
   * Third of three distance hyperbola parameters used to compute the group velocity of the envelope
   * peak (Mayeda et al., 2003), undefined implies infinite.
   * <p>
   * Units: km
   */
  private double vg2;

  static final public double VG2_NA = Double.NaN;

  /**
   * Error on the third of three distance hyperbola parameters used to compute the group velocity of
   * the envelope peak (Mayeda et al., 2003).
   * <p>
   * Units: km
   */
  private double dvg2;

  static final public double DVG2_NA = -1;

  /**
   * Fraction of peak time (distance/group velocity), centered on peak time, to define a peak
   * amplitude search window. Allows the amplitude peak to shift in time, ie: <I>codat0</I> can be
   * moved to match the peak (shifting all other times as well). Recommended that this only be
   * considered for local distances.
   */
  private double vgsearchfactor;

  static final public double VGSEARCHFACTOR_NA = -1;

  /**
   * Fraction of peak time (distance/group velocity), centered on peak time, to define an amplitude
   * measurement window. Used for direct wave peak amplitude window. This then is the window for
   * measuring the amplitude as opposed to <I>vgsearchfactor</I>, the window over which to shift
   * this measurement window.
   */
  private double vgwindfactor;

  static final public double VGWINDFACTOR_NA = -1;

  /**
   * Minimum calibrated distance for 1-D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   */
  private double dmin;

  static final public double DMIN_NA = Double.NaN;

  /**
   * Maximum calibrated distance for 1D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   */
  private double dmax;

  static final public double DMAX_NA = Double.NaN;

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
    columns.add("magyieldid", Columns.FieldType.LONG, "%d");
    columns.add("vg2dcorid", Columns.FieldType.LONG, "%d");
    columns.add("vg0", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("dvg0", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("vg1", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("dvg1", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("vg2", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("dvg2", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("vgsearchfactor", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("vgwindfactor", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("dmin", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("dmax", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Env_peak(long magyieldid, long vg2dcorid, double vg0, double dvg0, double vg1, double dvg1,
      double vg2, double dvg2, double vgsearchfactor, double vgwindfactor, double dmin, double dmax,
      String auth, long commid) {
    setValues(magyieldid, vg2dcorid, vg0, dvg0, vg1, dvg1, vg2, dvg2, vgsearchfactor, vgwindfactor,
        dmin, dmax, auth, commid);
  }

  private void setValues(long magyieldid, long vg2dcorid, double vg0, double dvg0, double vg1,
      double dvg1, double vg2, double dvg2, double vgsearchfactor, double vgwindfactor, double dmin,
      double dmax, String auth, long commid) {
    this.magyieldid = magyieldid;
    this.vg2dcorid = vg2dcorid;
    this.vg0 = vg0;
    this.dvg0 = dvg0;
    this.vg1 = vg1;
    this.dvg1 = dvg1;
    this.vg2 = vg2;
    this.dvg2 = dvg2;
    this.vgsearchfactor = vgsearchfactor;
    this.vgwindfactor = vgwindfactor;
    this.dmin = dmin;
    this.dmax = dmax;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Env_peak(Env_peak other) {
    this.magyieldid = other.getMagyieldid();
    this.vg2dcorid = other.getVg2dcorid();
    this.vg0 = other.getVg0();
    this.dvg0 = other.getDvg0();
    this.vg1 = other.getVg1();
    this.dvg1 = other.getDvg1();
    this.vg2 = other.getVg2();
    this.dvg2 = other.getDvg2();
    this.vgsearchfactor = other.getVgsearchfactor();
    this.vgwindfactor = other.getVgwindfactor();
    this.dmin = other.getDmin();
    this.dmax = other.getDmax();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Env_peak() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MAGYIELDID_NA, VG2DCORID_NA, VG0_NA, DVG0_NA, VG1_NA, DVG1_NA, VG2_NA, DVG2_NA,
        VGSEARCHFACTOR_NA, VGWINDFACTOR_NA, DMIN_NA, DMAX_NA, AUTH_NA, COMMID_NA);
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
      case "vg0":
        return vg0;
      case "dvg0":
        return dvg0;
      case "vg1":
        return vg1;
      case "dvg1":
        return dvg1;
      case "vg2":
        return vg2;
      case "dvg2":
        return dvg2;
      case "vgsearchfactor":
        return vgsearchfactor;
      case "vgwindfactor":
        return vgwindfactor;
      case "dmin":
        return dmin;
      case "dmax":
        return dmax;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "vg0":
        vg0 = value;
        break;
      case "dvg0":
        dvg0 = value;
        break;
      case "vg1":
        vg1 = value;
        break;
      case "dvg1":
        dvg1 = value;
        break;
      case "vg2":
        vg2 = value;
        break;
      case "dvg2":
        dvg2 = value;
        break;
      case "vgsearchfactor":
        vgsearchfactor = value;
        break;
      case "vgwindfactor":
        vgwindfactor = value;
        break;
      case "dmin":
        dmin = value;
        break;
      case "dmax":
        dmax = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "magyieldid":
        return magyieldid;
      case "vg2dcorid":
        return vg2dcorid;
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
      case "magyieldid":
        magyieldid = value;
        break;
      case "vg2dcorid":
        vg2dcorid = value;
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
  public Env_peak(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Env_peak(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Env_peak(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), readString(input),
        input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Env_peak(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Env_peak(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getString(offset + 13), input.getLong(offset + 14));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[14];
    values[0] = magyieldid;
    values[1] = vg2dcorid;
    values[2] = vg0;
    values[3] = dvg0;
    values[4] = vg1;
    values[5] = dvg1;
    values[6] = vg2;
    values[7] = dvg2;
    values[8] = vgsearchfactor;
    values[9] = vgwindfactor;
    values[10] = dmin;
    values[11] = dmax;
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
    values[0] = magyieldid;
    values[1] = vg2dcorid;
    values[2] = vg0;
    values[3] = dvg0;
    values[4] = vg1;
    values[5] = dvg1;
    values[6] = vg2;
    values[7] = dvg2;
    values[8] = vgsearchfactor;
    values[9] = vgwindfactor;
    values[10] = dmin;
    values[11] = dmax;
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
    output.writeLong(magyieldid);
    output.writeLong(vg2dcorid);
    output.writeDouble(vg0);
    output.writeDouble(dvg0);
    output.writeDouble(vg1);
    output.writeDouble(dvg1);
    output.writeDouble(vg2);
    output.writeDouble(dvg2);
    output.writeDouble(vgsearchfactor);
    output.writeDouble(vgwindfactor);
    output.writeDouble(dmin);
    output.writeDouble(dmax);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(magyieldid);
    output.putLong(vg2dcorid);
    output.putDouble(vg0);
    output.putDouble(dvg0);
    output.putDouble(vg1);
    output.putDouble(dvg1);
    output.putDouble(vg2);
    output.putDouble(dvg2);
    output.putDouble(vgsearchfactor);
    output.putDouble(vgwindfactor);
    output.putDouble(dmin);
    output.putDouble(dmax);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Env_peak objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Env_peak objects.
   * @throws IOException
   */
  static public void readEnv_peaks(BufferedReader input, Collection<Env_peak> rows)
      throws IOException {
    String[] saved = Env_peak.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Env_peak
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Env_peak(new Scanner(line)));
    }
    input.close();
    Env_peak.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Env_peak objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Env_peak objects.
   * @throws IOException
   */
  static public void readEnv_peaks(File inputFile, Collection<Env_peak> rows) throws IOException {
    readEnv_peaks(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Env_peak objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Env_peak objects.
   * @throws IOException
   */
  static public void readEnv_peaks(InputStream inputStream, Collection<Env_peak> rows)
      throws IOException {
    readEnv_peaks(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Env_peak objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Env_peak objects
   * @throws IOException
   */
  static public Set<Env_peak> readEnv_peaks(BufferedReader input) throws IOException {
    Set<Env_peak> rows = new LinkedHashSet<Env_peak>();
    readEnv_peaks(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Env_peak objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Env_peak objects
   * @throws IOException
   */
  static public Set<Env_peak> readEnv_peaks(File inputFile) throws IOException {
    return readEnv_peaks(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Env_peak objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Env_peak objects
   * @throws IOException
   */
  static public Set<Env_peak> readEnv_peaks(InputStream input) throws IOException {
    return readEnv_peaks(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Env_peak objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param env_peaks the Env_peak objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Env_peak> env_peaks)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Env_peak env_peak : env_peaks)
      env_peak.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Env_peak objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param env_peaks the Env_peak objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Env_peak> env_peaks, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Env_peak env_peak : env_peaks) {
        int i = 0;
        statement.setLong(++i, env_peak.magyieldid);
        statement.setLong(++i, env_peak.vg2dcorid);
        statement.setDouble(++i, env_peak.vg0);
        statement.setDouble(++i, env_peak.dvg0);
        statement.setDouble(++i, env_peak.vg1);
        statement.setDouble(++i, env_peak.dvg1);
        statement.setDouble(++i, env_peak.vg2);
        statement.setDouble(++i, env_peak.dvg2);
        statement.setDouble(++i, env_peak.vgsearchfactor);
        statement.setDouble(++i, env_peak.vgwindfactor);
        statement.setDouble(++i, env_peak.dmin);
        statement.setDouble(++i, env_peak.dmax);
        statement.setString(++i, env_peak.auth);
        statement.setLong(++i, env_peak.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Env_peak
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Env_peak> readEnv_peaks(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Env_peak> results = new HashSet<Env_peak>();
    readEnv_peaks(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Env_peak
   *        table.
   * @param env_peaks
   * @throws SQLException
   */
  static public void readEnv_peaks(Connection connection, String selectStatement,
      Set<Env_peak> env_peaks) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        env_peaks.add(new Env_peak(rs));
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
   * this Env_peak object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Env_peak object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "magyieldid, vg2dcorid, vg0, dvg0, vg1, dvg1, vg2, dvg2, vgsearchfactor, vgwindfactor, dmin, dmax, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(magyieldid)).append(", ");
    sql.append(Long.toString(vg2dcorid)).append(", ");
    sql.append(Double.toString(vg0)).append(", ");
    sql.append(Double.toString(dvg0)).append(", ");
    sql.append(Double.toString(vg1)).append(", ");
    sql.append(Double.toString(dvg1)).append(", ");
    sql.append(Double.toString(vg2)).append(", ");
    sql.append(Double.toString(dvg2)).append(", ");
    sql.append(Double.toString(vgsearchfactor)).append(", ");
    sql.append(Double.toString(vgwindfactor)).append(", ");
    sql.append(Double.toString(dmin)).append(", ");
    sql.append(Double.toString(dmax)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Env_peak in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Env_peak in the database
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
   * Generate a sql script to create a table of type Env_peak in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Env_peak in the database
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
    buf.append("magyieldid   number(9)            NOT NULL,\n");
    buf.append("vg2dcorid    number(9)            NOT NULL,\n");
    buf.append("vg0          float(24)            NOT NULL,\n");
    buf.append("dvg0         float(24)            NOT NULL,\n");
    buf.append("vg1          float(24)            NOT NULL,\n");
    buf.append("dvg1         float(24)            NOT NULL,\n");
    buf.append("vg2          float(24)            NOT NULL,\n");
    buf.append("dvg2         float(24)            NOT NULL,\n");
    buf.append("vgsearchfactor float(24)            NOT NULL,\n");
    buf.append("vgwindfactor float(24)            NOT NULL,\n");
    buf.append("dmin         float(24)            NOT NULL,\n");
    buf.append("dmax         float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (magyieldid)");
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
    return (other instanceof Env_peak) && ((Env_peak) other).magyieldid == magyieldid;
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
  public Env_peak setMagyieldid(long magyieldid) {
    if (magyieldid >= 1000000000L)
      throw new IllegalArgumentException(
          "magyieldid=" + magyieldid + " but cannot be >= 1000000000");
    this.magyieldid = magyieldid;
    setHash(null);
    return this;
  }

  /**
   * Group velocity two-dimensional correction identifier.
   * 
   * @return vg2dcorid
   */
  public long getVg2dcorid() {
    return vg2dcorid;
  }

  /**
   * Group velocity two-dimensional correction identifier.
   * 
   * @param vg2dcorid
   * @throws IllegalArgumentException if vg2dcorid >= 1000000000
   */
  public Env_peak setVg2dcorid(long vg2dcorid) {
    if (vg2dcorid >= 1000000000L)
      throw new IllegalArgumentException("vg2dcorid=" + vg2dcorid + " but cannot be >= 1000000000");
    this.vg2dcorid = vg2dcorid;
    setHash(null);
    return this;
  }

  /**
   * First of three distance hyperbola parameters used to compute the group velocity of the envelope
   * peak (Mayeda et al., 2003).
   * <p>
   * Units: km/s
   * 
   * @return vg0
   */
  public double getVg0() {
    return vg0;
  }

  /**
   * First of three distance hyperbola parameters used to compute the group velocity of the envelope
   * peak (Mayeda et al., 2003).
   * <p>
   * Units: km/s
   * 
   * @param vg0
   */
  public Env_peak setVg0(double vg0) {
    this.vg0 = vg0;
    setHash(null);
    return this;
  }

  /**
   * Error on the first of three distance hyperbola parameters used to compute the group velocity of
   * the envelope peak (Mayeda et al., 2003).
   * 
   * <p>
   * Units: km/s
   * 
   * @return dvg0
   */
  public double getDvg0() {
    return dvg0;
  }

  /**
   * Error on the first of three distance hyperbola parameters used to compute the group velocity of
   * the envelope peak (Mayeda et al., 2003).
   * 
   * <p>
   * Units: km/s
   * 
   * @param dvg0
   */
  public Env_peak setDvg0(double dvg0) {
    this.dvg0 = dvg0;
    setHash(null);
    return this;
  }

  /**
   * Second of three distance hyperbola parameters used to compute the group velocity of the
   * envelope peak (Mayeda et al., 2003); undefined implies zero.
   * <p>
   * Units: km^2/s
   * 
   * @return vg1
   */
  public double getVg1() {
    return vg1;
  }

  /**
   * Second of three distance hyperbola parameters used to compute the group velocity of the
   * envelope peak (Mayeda et al., 2003); undefined implies zero.
   * <p>
   * Units: km^2/s
   * 
   * @param vg1
   */
  public Env_peak setVg1(double vg1) {
    this.vg1 = vg1;
    setHash(null);
    return this;
  }

  /**
   * Error on the second of three distance hyperbola parameters used to compute the group velocity
   * of the envelope peak (Mayeda et al., 2003).
   * <p>
   * Units: km^2/s
   * 
   * @return dvg1
   */
  public double getDvg1() {
    return dvg1;
  }

  /**
   * Error on the second of three distance hyperbola parameters used to compute the group velocity
   * of the envelope peak (Mayeda et al., 2003).
   * <p>
   * Units: km^2/s
   * 
   * @param dvg1
   */
  public Env_peak setDvg1(double dvg1) {
    this.dvg1 = dvg1;
    setHash(null);
    return this;
  }

  /**
   * Third of three distance hyperbola parameters used to compute the group velocity of the envelope
   * peak (Mayeda et al., 2003), undefined implies infinite.
   * <p>
   * Units: km
   * 
   * @return vg2
   */
  public double getVg2() {
    return vg2;
  }

  /**
   * Third of three distance hyperbola parameters used to compute the group velocity of the envelope
   * peak (Mayeda et al., 2003), undefined implies infinite.
   * <p>
   * Units: km
   * 
   * @param vg2
   */
  public Env_peak setVg2(double vg2) {
    this.vg2 = vg2;
    setHash(null);
    return this;
  }

  /**
   * Error on the third of three distance hyperbola parameters used to compute the group velocity of
   * the envelope peak (Mayeda et al., 2003).
   * <p>
   * Units: km
   * 
   * @return dvg2
   */
  public double getDvg2() {
    return dvg2;
  }

  /**
   * Error on the third of three distance hyperbola parameters used to compute the group velocity of
   * the envelope peak (Mayeda et al., 2003).
   * <p>
   * Units: km
   * 
   * @param dvg2
   */
  public Env_peak setDvg2(double dvg2) {
    this.dvg2 = dvg2;
    setHash(null);
    return this;
  }

  /**
   * Fraction of peak time (distance/group velocity), centered on peak time, to define a peak
   * amplitude search window. Allows the amplitude peak to shift in time, ie: <I>codat0</I> can be
   * moved to match the peak (shifting all other times as well). Recommended that this only be
   * considered for local distances.
   * 
   * @return vgsearchfactor
   */
  public double getVgsearchfactor() {
    return vgsearchfactor;
  }

  /**
   * Fraction of peak time (distance/group velocity), centered on peak time, to define a peak
   * amplitude search window. Allows the amplitude peak to shift in time, ie: <I>codat0</I> can be
   * moved to match the peak (shifting all other times as well). Recommended that this only be
   * considered for local distances.
   * 
   * @param vgsearchfactor
   */
  public Env_peak setVgsearchfactor(double vgsearchfactor) {
    this.vgsearchfactor = vgsearchfactor;
    setHash(null);
    return this;
  }

  /**
   * Fraction of peak time (distance/group velocity), centered on peak time, to define an amplitude
   * measurement window. Used for direct wave peak amplitude window. This then is the window for
   * measuring the amplitude as opposed to <I>vgsearchfactor</I>, the window over which to shift
   * this measurement window.
   * 
   * @return vgwindfactor
   */
  public double getVgwindfactor() {
    return vgwindfactor;
  }

  /**
   * Fraction of peak time (distance/group velocity), centered on peak time, to define an amplitude
   * measurement window. Used for direct wave peak amplitude window. This then is the window for
   * measuring the amplitude as opposed to <I>vgsearchfactor</I>, the window over which to shift
   * this measurement window.
   * 
   * @param vgwindfactor
   */
  public Env_peak setVgwindfactor(double vgwindfactor) {
    this.vgwindfactor = vgwindfactor;
    setHash(null);
    return this;
  }

  /**
   * Minimum calibrated distance for 1-D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   * 
   * @return dmin
   */
  public double getDmin() {
    return dmin;
  }

  /**
   * Minimum calibrated distance for 1-D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   * 
   * @param dmin
   */
  public Env_peak setDmin(double dmin) {
    this.dmin = dmin;
    setHash(null);
    return this;
  }

  /**
   * Maximum calibrated distance for 1D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   * 
   * @return dmax
   */
  public double getDmax() {
    return dmax;
  }

  /**
   * Maximum calibrated distance for 1D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   * 
   * @param dmax
   */
  public Env_peak setDmax(double dmax) {
    this.dmax = dmax;
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
  public Env_peak setAuth(String auth) {
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
  public Env_peak setCommid(long commid) {
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
