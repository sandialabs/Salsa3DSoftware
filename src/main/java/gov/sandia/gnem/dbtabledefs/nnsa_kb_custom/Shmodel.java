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
 * shmodel
 */
public class Shmodel extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Slowness model identifier
   */
  private long shmodid;

  static final public long SHMODID_NA = Long.MIN_VALUE;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   */
  private String phase;

  static final public String PHASE_NA = null;

  /**
   * Station latitude.
   * <p>
   * Units: degree
   */
  private double stalat;

  static final public double STALAT_NA = Double.NaN;

  /**
   * Station longitude
   * <p>
   * Units: degree
   */
  private double stalon;

  static final public double STALON_NA = Double.NaN;

  /**
   * Station elevation
   * <p>
   * Units: km
   */
  private double staelev;

  static final public double STAELEV_NA = Double.NaN;

  /**
   * Location tolerance. This is the allowable difference in site location when using this TT Model
   * (i.e. when locating an event, if the current pick comes from a site more than loctol for the
   * position recorded in TTMod, the model cannot be used).
   * <p>
   * Units: km
   */
  private double staloctol;

  static final public double STALOCTOL_NA = Double.NaN;

  /**
   * Sedimentary velocity in vicinity of station (km/s)
   * <p>
   * Units: km/s
   */
  private double vel_sed;

  static final public double VEL_SED_NA = Double.NaN;

  /**
   * Static slowness correction for a given station, to be added regardless of source position.
   * <p>
   * Units: s/degree
   */
  private double bulk_corr_sh;

  static final public double BULK_CORR_SH_NA = Double.NaN;

  /**
   * The name of the base model.
   */
  private String basemodname;

  static final public String BASEMODNAME_NA = null;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;

  /**
   * The default contributing organization for this data.
   */
  private String default_contrib_org;

  static final public String DEFAULT_CONTRIB_ORG_NA = null;

  /**
   * Description of the objected represented in the table.
   */
  private String descript;

  static final public String DESCRIPT_NA = null;

  /**
   * Epoch time
   * <p>
   * Units: s
   */
  private double time;

  static final public double TIME_NA = -9999999999.999;

  /**
   * End of time period covered
   * <p>
   * Units: s
   */
  private double endtime;

  static final public double ENDTIME_NA = 9999999999.999;

  /**
   * Author who loaded data
   */
  private String ldauth;

  static final public String LDAUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("shmodid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("phase", Columns.FieldType.STRING, "%s");
    columns.add("stalat", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("stalon", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("staelev", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("staloctol", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("vel_sed", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("bulk_corr_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("basemodname", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("default_contrib_org", Columns.FieldType.STRING, "%s");
    columns.add("descript", Columns.FieldType.STRING, "%s");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("endtime", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("ldauth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Shmodel(long shmodid, String sta, String phase, double stalat, double stalon,
      double staelev, double staloctol, double vel_sed, double bulk_corr_sh, String basemodname,
      String auth, String default_contrib_org, String descript, double time, double endtime,
      String ldauth) {
    setValues(shmodid, sta, phase, stalat, stalon, staelev, staloctol, vel_sed, bulk_corr_sh,
        basemodname, auth, default_contrib_org, descript, time, endtime, ldauth);
  }

  private void setValues(long shmodid, String sta, String phase, double stalat, double stalon,
      double staelev, double staloctol, double vel_sed, double bulk_corr_sh, String basemodname,
      String auth, String default_contrib_org, String descript, double time, double endtime,
      String ldauth) {
    this.shmodid = shmodid;
    this.sta = sta;
    this.phase = phase;
    this.stalat = stalat;
    this.stalon = stalon;
    this.staelev = staelev;
    this.staloctol = staloctol;
    this.vel_sed = vel_sed;
    this.bulk_corr_sh = bulk_corr_sh;
    this.basemodname = basemodname;
    this.auth = auth;
    this.default_contrib_org = default_contrib_org;
    this.descript = descript;
    this.time = time;
    this.endtime = endtime;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Shmodel(Shmodel other) {
    this.shmodid = other.getShmodid();
    this.sta = other.getSta();
    this.phase = other.getPhase();
    this.stalat = other.getStalat();
    this.stalon = other.getStalon();
    this.staelev = other.getStaelev();
    this.staloctol = other.getStaloctol();
    this.vel_sed = other.getVel_sed();
    this.bulk_corr_sh = other.getBulk_corr_sh();
    this.basemodname = other.getBasemodname();
    this.auth = other.getAuth();
    this.default_contrib_org = other.getDefault_contrib_org();
    this.descript = other.getDescript();
    this.time = other.getTime();
    this.endtime = other.getEndtime();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Shmodel() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(SHMODID_NA, STA_NA, PHASE_NA, STALAT_NA, STALON_NA, STAELEV_NA, STALOCTOL_NA,
        VEL_SED_NA, BULK_CORR_SH_NA, BASEMODNAME_NA, AUTH_NA, DEFAULT_CONTRIB_ORG_NA, DESCRIPT_NA,
        TIME_NA, ENDTIME_NA, LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "phase":
        return phase;
      case "basemodname":
        return basemodname;
      case "auth":
        return auth;
      case "default_contrib_org":
        return default_contrib_org;
      case "descript":
        return descript;
      case "ldauth":
        return ldauth;
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
      case "phase":
        phase = value;
        break;
      case "basemodname":
        basemodname = value;
        break;
      case "auth":
        auth = value;
        break;
      case "default_contrib_org":
        default_contrib_org = value;
        break;
      case "descript":
        descript = value;
        break;
      case "ldauth":
        ldauth = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "stalat":
        return stalat;
      case "stalon":
        return stalon;
      case "staelev":
        return staelev;
      case "staloctol":
        return staloctol;
      case "vel_sed":
        return vel_sed;
      case "bulk_corr_sh":
        return bulk_corr_sh;
      case "time":
        return time;
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
      case "stalat":
        stalat = value;
        break;
      case "stalon":
        stalon = value;
        break;
      case "staelev":
        staelev = value;
        break;
      case "staloctol":
        staloctol = value;
        break;
      case "vel_sed":
        vel_sed = value;
        break;
      case "bulk_corr_sh":
        bulk_corr_sh = value;
        break;
      case "time":
        time = value;
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
      case "shmodid":
        return shmodid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "shmodid":
        shmodid = value;
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
  public Shmodel(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Shmodel(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), readString(input), readString(input), readString(input),
        readString(input), input.readDouble(), input.readDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Shmodel(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), readString(input), readString(input), readString(input),
        readString(input), input.getDouble(), input.getDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Shmodel(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Shmodel(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getString(offset + 10), input.getString(offset + 11), input.getString(offset + 12),
        input.getString(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getString(offset + 16));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[16];
    values[0] = shmodid;
    values[1] = sta;
    values[2] = phase;
    values[3] = stalat;
    values[4] = stalon;
    values[5] = staelev;
    values[6] = staloctol;
    values[7] = vel_sed;
    values[8] = bulk_corr_sh;
    values[9] = basemodname;
    values[10] = auth;
    values[11] = default_contrib_org;
    values[12] = descript;
    values[13] = time;
    values[14] = endtime;
    values[15] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[17];
    values[0] = shmodid;
    values[1] = sta;
    values[2] = phase;
    values[3] = stalat;
    values[4] = stalon;
    values[5] = staelev;
    values[6] = staloctol;
    values[7] = vel_sed;
    values[8] = bulk_corr_sh;
    values[9] = basemodname;
    values[10] = auth;
    values[11] = default_contrib_org;
    values[12] = descript;
    values[13] = time;
    values[14] = endtime;
    values[15] = ldauth;
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
    output.writeLong(shmodid);
    writeString(output, sta);
    writeString(output, phase);
    output.writeDouble(stalat);
    output.writeDouble(stalon);
    output.writeDouble(staelev);
    output.writeDouble(staloctol);
    output.writeDouble(vel_sed);
    output.writeDouble(bulk_corr_sh);
    writeString(output, basemodname);
    writeString(output, auth);
    writeString(output, default_contrib_org);
    writeString(output, descript);
    output.writeDouble(time);
    output.writeDouble(endtime);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(shmodid);
    writeString(output, sta);
    writeString(output, phase);
    output.putDouble(stalat);
    output.putDouble(stalon);
    output.putDouble(staelev);
    output.putDouble(staloctol);
    output.putDouble(vel_sed);
    output.putDouble(bulk_corr_sh);
    writeString(output, basemodname);
    writeString(output, auth);
    writeString(output, default_contrib_org);
    writeString(output, descript);
    output.putDouble(time);
    output.putDouble(endtime);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Shmodel objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Shmodel objects.
   * @throws IOException
   */
  static public void readShmodels(BufferedReader input, Collection<Shmodel> rows)
      throws IOException {
    String[] saved = Shmodel.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Shmodel.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Shmodel(new Scanner(line)));
    }
    input.close();
    Shmodel.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Shmodel objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Shmodel objects.
   * @throws IOException
   */
  static public void readShmodels(File inputFile, Collection<Shmodel> rows) throws IOException {
    readShmodels(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Shmodel objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Shmodel objects.
   * @throws IOException
   */
  static public void readShmodels(InputStream inputStream, Collection<Shmodel> rows)
      throws IOException {
    readShmodels(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Shmodel objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Shmodel objects
   * @throws IOException
   */
  static public Set<Shmodel> readShmodels(BufferedReader input) throws IOException {
    Set<Shmodel> rows = new LinkedHashSet<Shmodel>();
    readShmodels(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Shmodel objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Shmodel objects
   * @throws IOException
   */
  static public Set<Shmodel> readShmodels(File inputFile) throws IOException {
    return readShmodels(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Shmodel objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Shmodel objects
   * @throws IOException
   */
  static public Set<Shmodel> readShmodels(InputStream input) throws IOException {
    return readShmodels(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Shmodel objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param shmodels the Shmodel objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Shmodel> shmodels)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Shmodel shmodel : shmodels)
      shmodel.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Shmodel objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param shmodels the Shmodel objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Shmodel> shmodels, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Shmodel shmodel : shmodels) {
        int i = 0;
        statement.setLong(++i, shmodel.shmodid);
        statement.setString(++i, shmodel.sta);
        statement.setString(++i, shmodel.phase);
        statement.setDouble(++i, shmodel.stalat);
        statement.setDouble(++i, shmodel.stalon);
        statement.setDouble(++i, shmodel.staelev);
        statement.setDouble(++i, shmodel.staloctol);
        statement.setDouble(++i, shmodel.vel_sed);
        statement.setDouble(++i, shmodel.bulk_corr_sh);
        statement.setString(++i, shmodel.basemodname);
        statement.setString(++i, shmodel.auth);
        statement.setString(++i, shmodel.default_contrib_org);
        statement.setString(++i, shmodel.descript);
        statement.setDouble(++i, shmodel.time);
        statement.setDouble(++i, shmodel.endtime);
        statement.setString(++i, shmodel.ldauth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Shmodel
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Shmodel> readShmodels(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Shmodel> results = new HashSet<Shmodel>();
    readShmodels(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Shmodel
   *        table.
   * @param shmodels
   * @throws SQLException
   */
  static public void readShmodels(Connection connection, String selectStatement,
      Set<Shmodel> shmodels) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        shmodels.add(new Shmodel(rs));
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
   * this Shmodel object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Shmodel object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "shmodid, sta, phase, stalat, stalon, staelev, staloctol, vel_sed, bulk_corr_sh, basemodname, auth, default_contrib_org, descript, time, endtime, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(shmodid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(phase).append("', ");
    sql.append(Double.toString(stalat)).append(", ");
    sql.append(Double.toString(stalon)).append(", ");
    sql.append(Double.toString(staelev)).append(", ");
    sql.append(Double.toString(staloctol)).append(", ");
    sql.append(Double.toString(vel_sed)).append(", ");
    sql.append(Double.toString(bulk_corr_sh)).append(", ");
    sql.append("'").append(basemodname).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("'").append(default_contrib_org).append("', ");
    sql.append("'").append(descript).append("', ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Double.toString(endtime)).append(", ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Shmodel in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Shmodel in the database
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
   * Generate a sql script to create a table of type Shmodel in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Shmodel in the database
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
    buf.append("shmodid      number(9)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("phase        varchar2(8)          NOT NULL,\n");
    buf.append("stalat       float(53)            NOT NULL,\n");
    buf.append("stalon       float(53)            NOT NULL,\n");
    buf.append("staelev      float(53)            NOT NULL,\n");
    buf.append("staloctol    float(53)            NOT NULL,\n");
    buf.append("vel_sed      float(53)            NOT NULL,\n");
    buf.append("bulk_corr_sh float(53)            NOT NULL,\n");
    buf.append("basemodname  varchar2(60)         NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("default_contrib_org varchar2(15)         NOT NULL,\n");
    buf.append("descript     varchar2(1024)       NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("endtime      float(53)            NOT NULL,\n");
    buf.append("ldauth       varchar2(15)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (shmodid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (sta,phase,stalat,stalon,staelev,staloctol,vel_sed,bulk_corr_sh,auth,default_contrib_org,descript)");
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
    return 1248;
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
    return (other instanceof Shmodel) && ((Shmodel) other).shmodid == shmodid;
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
    return (other instanceof Shmodel) && ((Shmodel) other).sta.equals(sta)
        && ((Shmodel) other).phase.equals(phase) && ((Shmodel) other).stalat == stalat
        && ((Shmodel) other).stalon == stalon && ((Shmodel) other).staelev == staelev
        && ((Shmodel) other).staloctol == staloctol && ((Shmodel) other).vel_sed == vel_sed
        && ((Shmodel) other).bulk_corr_sh == bulk_corr_sh && ((Shmodel) other).auth.equals(auth)
        && ((Shmodel) other).default_contrib_org.equals(default_contrib_org)
        && ((Shmodel) other).descript.equals(descript);
  }

  /**
   * Slowness model identifier
   * 
   * @return shmodid
   */
  public long getShmodid() {
    return shmodid;
  }

  /**
   * Slowness model identifier
   * 
   * @param shmodid
   * @throws IllegalArgumentException if shmodid >= 1000000000
   */
  public Shmodel setShmodid(long shmodid) {
    if (shmodid >= 1000000000L)
      throw new IllegalArgumentException("shmodid=" + shmodid + " but cannot be >= 1000000000");
    this.shmodid = shmodid;
    setHash(null);
    return this;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   * 
   * @return sta
   */
  public String getSta() {
    return sta;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   * 
   * @param sta
   * @throws IllegalArgumentException if sta.length() >= 6
   */
  public Shmodel setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   * 
   * @return phase
   */
  public String getPhase() {
    return phase;
  }

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   * 
   * @param phase
   * @throws IllegalArgumentException if phase.length() >= 8
   */
  public Shmodel setPhase(String phase) {
    if (phase.length() > 8)
      throw new IllegalArgumentException(
          String.format("phase.length() cannot be > 8.  phase=%s", phase));
    this.phase = phase;
    setHash(null);
    return this;
  }

  /**
   * Station latitude.
   * <p>
   * Units: degree
   * 
   * @return stalat
   */
  public double getStalat() {
    return stalat;
  }

  /**
   * Station latitude.
   * <p>
   * Units: degree
   * 
   * @param stalat
   */
  public Shmodel setStalat(double stalat) {
    this.stalat = stalat;
    setHash(null);
    return this;
  }

  /**
   * Station longitude
   * <p>
   * Units: degree
   * 
   * @return stalon
   */
  public double getStalon() {
    return stalon;
  }

  /**
   * Station longitude
   * <p>
   * Units: degree
   * 
   * @param stalon
   */
  public Shmodel setStalon(double stalon) {
    this.stalon = stalon;
    setHash(null);
    return this;
  }

  /**
   * Station elevation
   * <p>
   * Units: km
   * 
   * @return staelev
   */
  public double getStaelev() {
    return staelev;
  }

  /**
   * Station elevation
   * <p>
   * Units: km
   * 
   * @param staelev
   */
  public Shmodel setStaelev(double staelev) {
    this.staelev = staelev;
    setHash(null);
    return this;
  }

  /**
   * Location tolerance. This is the allowable difference in site location when using this TT Model
   * (i.e. when locating an event, if the current pick comes from a site more than loctol for the
   * position recorded in TTMod, the model cannot be used).
   * <p>
   * Units: km
   * 
   * @return staloctol
   */
  public double getStaloctol() {
    return staloctol;
  }

  /**
   * Location tolerance. This is the allowable difference in site location when using this TT Model
   * (i.e. when locating an event, if the current pick comes from a site more than loctol for the
   * position recorded in TTMod, the model cannot be used).
   * <p>
   * Units: km
   * 
   * @param staloctol
   */
  public Shmodel setStaloctol(double staloctol) {
    this.staloctol = staloctol;
    setHash(null);
    return this;
  }

  /**
   * Sedimentary velocity in vicinity of station (km/s)
   * <p>
   * Units: km/s
   * 
   * @return vel_sed
   */
  public double getVel_sed() {
    return vel_sed;
  }

  /**
   * Sedimentary velocity in vicinity of station (km/s)
   * <p>
   * Units: km/s
   * 
   * @param vel_sed
   */
  public Shmodel setVel_sed(double vel_sed) {
    this.vel_sed = vel_sed;
    setHash(null);
    return this;
  }

  /**
   * Static slowness correction for a given station, to be added regardless of source position.
   * <p>
   * Units: s/degree
   * 
   * @return bulk_corr_sh
   */
  public double getBulk_corr_sh() {
    return bulk_corr_sh;
  }

  /**
   * Static slowness correction for a given station, to be added regardless of source position.
   * <p>
   * Units: s/degree
   * 
   * @param bulk_corr_sh
   */
  public Shmodel setBulk_corr_sh(double bulk_corr_sh) {
    this.bulk_corr_sh = bulk_corr_sh;
    setHash(null);
    return this;
  }

  /**
   * The name of the base model.
   * 
   * @return basemodname
   */
  public String getBasemodname() {
    return basemodname;
  }

  /**
   * The name of the base model.
   * 
   * @param basemodname
   * @throws IllegalArgumentException if basemodname.length() >= 60
   */
  public Shmodel setBasemodname(String basemodname) {
    if (basemodname.length() > 60)
      throw new IllegalArgumentException(
          String.format("basemodname.length() cannot be > 60.  basemodname=%s", basemodname));
    this.basemodname = basemodname;
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
  public Shmodel setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * The default contributing organization for this data.
   * 
   * @return default_contrib_org
   */
  public String getDefault_contrib_org() {
    return default_contrib_org;
  }

  /**
   * The default contributing organization for this data.
   * 
   * @param default_contrib_org
   * @throws IllegalArgumentException if default_contrib_org.length() >= 15
   */
  public Shmodel setDefault_contrib_org(String default_contrib_org) {
    if (default_contrib_org.length() > 15)
      throw new IllegalArgumentException(
          String.format("default_contrib_org.length() cannot be > 15.  default_contrib_org=%s",
              default_contrib_org));
    this.default_contrib_org = default_contrib_org;
    setHash(null);
    return this;
  }

  /**
   * Description of the objected represented in the table.
   * 
   * @return descript
   */
  public String getDescript() {
    return descript;
  }

  /**
   * Description of the objected represented in the table.
   * 
   * @param descript
   * @throws IllegalArgumentException if descript.length() >= 1024
   */
  public Shmodel setDescript(String descript) {
    if (descript.length() > 1024)
      throw new IllegalArgumentException(
          String.format("descript.length() cannot be > 1024.  descript=%s", descript));
    this.descript = descript;
    setHash(null);
    return this;
  }

  /**
   * Epoch time
   * <p>
   * Units: s
   * 
   * @return time
   */
  public double getTime() {
    return time;
  }

  /**
   * Epoch time
   * <p>
   * Units: s
   * 
   * @param time
   */
  public Shmodel setTime(double time) {
    this.time = time;
    setHash(null);
    return this;
  }

  /**
   * End of time period covered
   * <p>
   * Units: s
   * 
   * @return endtime
   */
  public double getEndtime() {
    return endtime;
  }

  /**
   * End of time period covered
   * <p>
   * Units: s
   * 
   * @param endtime
   */
  public Shmodel setEndtime(double endtime) {
    this.endtime = endtime;
    setHash(null);
    return this;
  }

  /**
   * Author who loaded data
   * 
   * @return ldauth
   */
  public String getLdauth() {
    return ldauth;
  }

  /**
   * Author who loaded data
   * 
   * @param ldauth
   * @throws IllegalArgumentException if ldauth.length() >= 15
   */
  public Shmodel setLdauth(String ldauth) {
    if (ldauth.length() > 15)
      throw new IllegalArgumentException(
          String.format("ldauth.length() cannot be > 15.  ldauth=%s", ldauth));
    this.ldauth = ldauth;
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
