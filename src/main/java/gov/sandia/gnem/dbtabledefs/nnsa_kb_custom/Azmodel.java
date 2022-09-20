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
 * azmodel
 */
public class Azmodel extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Azimuth model indentifier
   */
  private long azmodid;

  static final public long AZMODID_NA = Long.MIN_VALUE;

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
   * Static azimuth correction for a given station, to be added regardless of source position.
   * <p>
   * Units: degree
   */
  private double bulk_corr_az;

  static final public double BULK_CORR_AZ_NA = -999;

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
    columns.add("azmodid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("phase", Columns.FieldType.STRING, "%s");
    columns.add("stalat", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("stalon", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("staelev", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("staloctol", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("bulk_corr_az", Columns.FieldType.DOUBLE, "%22.15e");
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
  public Azmodel(long azmodid, String sta, String phase, double stalat, double stalon,
      double staelev, double staloctol, double bulk_corr_az, String auth,
      String default_contrib_org, String descript, double time, double endtime, String ldauth) {
    setValues(azmodid, sta, phase, stalat, stalon, staelev, staloctol, bulk_corr_az, auth,
        default_contrib_org, descript, time, endtime, ldauth);
  }

  private void setValues(long azmodid, String sta, String phase, double stalat, double stalon,
      double staelev, double staloctol, double bulk_corr_az, String auth,
      String default_contrib_org, String descript, double time, double endtime, String ldauth) {
    this.azmodid = azmodid;
    this.sta = sta;
    this.phase = phase;
    this.stalat = stalat;
    this.stalon = stalon;
    this.staelev = staelev;
    this.staloctol = staloctol;
    this.bulk_corr_az = bulk_corr_az;
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
  public Azmodel(Azmodel other) {
    this.azmodid = other.getAzmodid();
    this.sta = other.getSta();
    this.phase = other.getPhase();
    this.stalat = other.getStalat();
    this.stalon = other.getStalon();
    this.staelev = other.getStaelev();
    this.staloctol = other.getStaloctol();
    this.bulk_corr_az = other.getBulk_corr_az();
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
  public Azmodel() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(AZMODID_NA, STA_NA, PHASE_NA, STALAT_NA, STALON_NA, STAELEV_NA, STALOCTOL_NA,
        BULK_CORR_AZ_NA, AUTH_NA, DEFAULT_CONTRIB_ORG_NA, DESCRIPT_NA, TIME_NA, ENDTIME_NA,
        LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "phase":
        return phase;
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
      case "bulk_corr_az":
        return bulk_corr_az;
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
      case "bulk_corr_az":
        bulk_corr_az = value;
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
      case "azmodid":
        return azmodid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "azmodid":
        azmodid = value;
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
  public Azmodel(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Azmodel(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        readString(input), readString(input), readString(input), input.readDouble(),
        input.readDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Azmodel(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        readString(input), readString(input), readString(input), input.getDouble(),
        input.getDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Azmodel(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Azmodel(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getString(offset + 9),
        input.getString(offset + 10), input.getString(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getString(offset + 14));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[14];
    values[0] = azmodid;
    values[1] = sta;
    values[2] = phase;
    values[3] = stalat;
    values[4] = stalon;
    values[5] = staelev;
    values[6] = staloctol;
    values[7] = bulk_corr_az;
    values[8] = auth;
    values[9] = default_contrib_org;
    values[10] = descript;
    values[11] = time;
    values[12] = endtime;
    values[13] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[15];
    values[0] = azmodid;
    values[1] = sta;
    values[2] = phase;
    values[3] = stalat;
    values[4] = stalon;
    values[5] = staelev;
    values[6] = staloctol;
    values[7] = bulk_corr_az;
    values[8] = auth;
    values[9] = default_contrib_org;
    values[10] = descript;
    values[11] = time;
    values[12] = endtime;
    values[13] = ldauth;
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
    output.writeLong(azmodid);
    writeString(output, sta);
    writeString(output, phase);
    output.writeDouble(stalat);
    output.writeDouble(stalon);
    output.writeDouble(staelev);
    output.writeDouble(staloctol);
    output.writeDouble(bulk_corr_az);
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
    output.putLong(azmodid);
    writeString(output, sta);
    writeString(output, phase);
    output.putDouble(stalat);
    output.putDouble(stalon);
    output.putDouble(staelev);
    output.putDouble(staloctol);
    output.putDouble(bulk_corr_az);
    writeString(output, auth);
    writeString(output, default_contrib_org);
    writeString(output, descript);
    output.putDouble(time);
    output.putDouble(endtime);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Azmodel objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Azmodel objects.
   * @throws IOException
   */
  static public void readAzmodels(BufferedReader input, Collection<Azmodel> rows)
      throws IOException {
    String[] saved = Azmodel.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Azmodel.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Azmodel(new Scanner(line)));
    }
    input.close();
    Azmodel.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Azmodel objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Azmodel objects.
   * @throws IOException
   */
  static public void readAzmodels(File inputFile, Collection<Azmodel> rows) throws IOException {
    readAzmodels(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Azmodel objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Azmodel objects.
   * @throws IOException
   */
  static public void readAzmodels(InputStream inputStream, Collection<Azmodel> rows)
      throws IOException {
    readAzmodels(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Azmodel objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Azmodel objects
   * @throws IOException
   */
  static public Set<Azmodel> readAzmodels(BufferedReader input) throws IOException {
    Set<Azmodel> rows = new LinkedHashSet<Azmodel>();
    readAzmodels(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Azmodel objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Azmodel objects
   * @throws IOException
   */
  static public Set<Azmodel> readAzmodels(File inputFile) throws IOException {
    return readAzmodels(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Azmodel objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Azmodel objects
   * @throws IOException
   */
  static public Set<Azmodel> readAzmodels(InputStream input) throws IOException {
    return readAzmodels(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Azmodel objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param azmodels the Azmodel objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Azmodel> azmodels)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Azmodel azmodel : azmodels)
      azmodel.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Azmodel objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param azmodels the Azmodel objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Azmodel> azmodels, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Azmodel azmodel : azmodels) {
        int i = 0;
        statement.setLong(++i, azmodel.azmodid);
        statement.setString(++i, azmodel.sta);
        statement.setString(++i, azmodel.phase);
        statement.setDouble(++i, azmodel.stalat);
        statement.setDouble(++i, azmodel.stalon);
        statement.setDouble(++i, azmodel.staelev);
        statement.setDouble(++i, azmodel.staloctol);
        statement.setDouble(++i, azmodel.bulk_corr_az);
        statement.setString(++i, azmodel.auth);
        statement.setString(++i, azmodel.default_contrib_org);
        statement.setString(++i, azmodel.descript);
        statement.setDouble(++i, azmodel.time);
        statement.setDouble(++i, azmodel.endtime);
        statement.setString(++i, azmodel.ldauth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Azmodel
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Azmodel> readAzmodels(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Azmodel> results = new HashSet<Azmodel>();
    readAzmodels(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Azmodel
   *        table.
   * @param azmodels
   * @throws SQLException
   */
  static public void readAzmodels(Connection connection, String selectStatement,
      Set<Azmodel> azmodels) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        azmodels.add(new Azmodel(rs));
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
   * this Azmodel object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Azmodel object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "azmodid, sta, phase, stalat, stalon, staelev, staloctol, bulk_corr_az, auth, default_contrib_org, descript, time, endtime, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(azmodid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(phase).append("', ");
    sql.append(Double.toString(stalat)).append(", ");
    sql.append(Double.toString(stalon)).append(", ");
    sql.append(Double.toString(staelev)).append(", ");
    sql.append(Double.toString(staloctol)).append(", ");
    sql.append(Double.toString(bulk_corr_az)).append(", ");
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
   * Create a table of type Azmodel in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Azmodel in the database
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
   * Generate a sql script to create a table of type Azmodel in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Azmodel in the database
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
    buf.append("azmodid      number(9)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("phase        varchar2(8)          NOT NULL,\n");
    buf.append("stalat       float(53)            NOT NULL,\n");
    buf.append("stalon       float(53)            NOT NULL,\n");
    buf.append("staelev      float(53)            NOT NULL,\n");
    buf.append("staloctol    float(53)            NOT NULL,\n");
    buf.append("bulk_corr_az float(53)            NOT NULL,\n");
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
          + "_pk primary key (azmodid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (sta,phase,stalat,stalon,staloctol,bulk_corr_az,auth,default_contrib_org,descript)");
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
    return 1176;
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
    return (other instanceof Azmodel) && ((Azmodel) other).azmodid == azmodid;
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
    return (other instanceof Azmodel) && ((Azmodel) other).sta.equals(sta)
        && ((Azmodel) other).phase.equals(phase) && ((Azmodel) other).stalat == stalat
        && ((Azmodel) other).stalon == stalon && ((Azmodel) other).staloctol == staloctol
        && ((Azmodel) other).bulk_corr_az == bulk_corr_az && ((Azmodel) other).auth.equals(auth)
        && ((Azmodel) other).default_contrib_org.equals(default_contrib_org)
        && ((Azmodel) other).descript.equals(descript);
  }

  /**
   * Azimuth model indentifier
   * 
   * @return azmodid
   */
  public long getAzmodid() {
    return azmodid;
  }

  /**
   * Azimuth model indentifier
   * 
   * @param azmodid
   * @throws IllegalArgumentException if azmodid >= 1000000000
   */
  public Azmodel setAzmodid(long azmodid) {
    if (azmodid >= 1000000000L)
      throw new IllegalArgumentException("azmodid=" + azmodid + " but cannot be >= 1000000000");
    this.azmodid = azmodid;
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
  public Azmodel setSta(String sta) {
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
  public Azmodel setPhase(String phase) {
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
  public Azmodel setStalat(double stalat) {
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
  public Azmodel setStalon(double stalon) {
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
  public Azmodel setStaelev(double staelev) {
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
  public Azmodel setStaloctol(double staloctol) {
    this.staloctol = staloctol;
    setHash(null);
    return this;
  }

  /**
   * Static azimuth correction for a given station, to be added regardless of source position.
   * <p>
   * Units: degree
   * 
   * @return bulk_corr_az
   */
  public double getBulk_corr_az() {
    return bulk_corr_az;
  }

  /**
   * Static azimuth correction for a given station, to be added regardless of source position.
   * <p>
   * Units: degree
   * 
   * @param bulk_corr_az
   */
  public Azmodel setBulk_corr_az(double bulk_corr_az) {
    this.bulk_corr_az = bulk_corr_az;
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
  public Azmodel setAuth(String auth) {
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
  public Azmodel setDefault_contrib_org(String default_contrib_org) {
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
  public Azmodel setDescript(String descript) {
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
  public Azmodel setTime(double time) {
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
  public Azmodel setEndtime(double endtime) {
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
  public Azmodel setLdauth(String ldauth) {
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
