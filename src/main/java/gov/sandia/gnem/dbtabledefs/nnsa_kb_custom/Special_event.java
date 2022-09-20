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
 * special_event
 */
public class Special_event extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Special event identifier
   */
  private long sevid;

  static final public long SEVID_NA = Long.MIN_VALUE;

  /**
   * Special event preferred origin
   */
  private long sprefor;

  static final public long SPREFOR_NA = Long.MIN_VALUE;

  /**
   * Geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   */
  private double lat;

  static final public double LAT_NA = Double.NaN;

  /**
   * Geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   */
  private double lon;

  static final public double LON_NA = Double.NaN;

  /**
   * Event depth
   * <p>
   * Units: km
   */
  private double depth;

  static final public double DEPTH_NA = Double.NaN;

  /**
   * Epoch time
   * <p>
   * Units: s
   */
  private double time;

  static final public double TIME_NA = Double.NaN;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;

  /**
   * Type of seismic event, when known. The following etypes are defined: ex generic explosion ec
   * chemical explosion ep probable explosion en nuclear explosion mc collapse me coal bump/mining
   * event mp probable mining event mb rock burst qt generic earthquake/tectonic qd damaging
   * earthquake qp unknown-probable earthquake qf felt earthquake qm multiple shocks qh quake with
   * associated Harmonic Tremor qv long period event e.g. slow earthquake q2 double shock q4
   * foreshock qa aftershock ge geyser xm meteoritic origin xl lights xo odors - unknown
   */
  private String etype;

  static final public String ETYPE_NA = null;

  /**
   * Event identifier in the Knowledge Base
   */
  private long kbevid;

  static final public long KBEVID_NA = Long.MIN_VALUE;

  /**
   * Preferred origin in the Knowledge Base
   */
  private long kbprefor;

  static final public long KBPREFOR_NA = Long.MIN_VALUE;

  /**
   * Event identifier at LANL
   */
  private long lanlevid;

  static final public long LANLEVID_NA = -1;

  /**
   * Preferred origin at LANL
   */
  private long lanlprefor;

  static final public long LANLPREFOR_NA = -1;

  /**
   * Event identifier at LLNL
   */
  private long llnlevid;

  static final public long LLNLEVID_NA = -1;

  /**
   * Preferred origin at LLNL
   */
  private long llnlprefor;

  static final public long LLNLPREFOR_NA = -1;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("sevid", Columns.FieldType.LONG, "%d");
    columns.add("sprefor", Columns.FieldType.LONG, "%d");
    columns.add("lat", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("lon", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("depth", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("etype", Columns.FieldType.STRING, "%s");
    columns.add("kbevid", Columns.FieldType.LONG, "%d");
    columns.add("kbprefor", Columns.FieldType.LONG, "%d");
    columns.add("lanlevid", Columns.FieldType.LONG, "%d");
    columns.add("lanlprefor", Columns.FieldType.LONG, "%d");
    columns.add("llnlevid", Columns.FieldType.LONG, "%d");
    columns.add("llnlprefor", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Special_event(long sevid, long sprefor, double lat, double lon, double depth, double time,
      String auth, String etype, long kbevid, long kbprefor, long lanlevid, long lanlprefor,
      long llnlevid, long llnlprefor) {
    setValues(sevid, sprefor, lat, lon, depth, time, auth, etype, kbevid, kbprefor, lanlevid,
        lanlprefor, llnlevid, llnlprefor);
  }

  private void setValues(long sevid, long sprefor, double lat, double lon, double depth,
      double time, String auth, String etype, long kbevid, long kbprefor, long lanlevid,
      long lanlprefor, long llnlevid, long llnlprefor) {
    this.sevid = sevid;
    this.sprefor = sprefor;
    this.lat = lat;
    this.lon = lon;
    this.depth = depth;
    this.time = time;
    this.auth = auth;
    this.etype = etype;
    this.kbevid = kbevid;
    this.kbprefor = kbprefor;
    this.lanlevid = lanlevid;
    this.lanlprefor = lanlprefor;
    this.llnlevid = llnlevid;
    this.llnlprefor = llnlprefor;
  }

  /**
   * Copy constructor.
   */
  public Special_event(Special_event other) {
    this.sevid = other.getSevid();
    this.sprefor = other.getSprefor();
    this.lat = other.getLat();
    this.lon = other.getLon();
    this.depth = other.getDepth();
    this.time = other.getTime();
    this.auth = other.getAuth();
    this.etype = other.getEtype();
    this.kbevid = other.getKbevid();
    this.kbprefor = other.getKbprefor();
    this.lanlevid = other.getLanlevid();
    this.lanlprefor = other.getLanlprefor();
    this.llnlevid = other.getLlnlevid();
    this.llnlprefor = other.getLlnlprefor();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Special_event() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(SEVID_NA, SPREFOR_NA, LAT_NA, LON_NA, DEPTH_NA, TIME_NA, AUTH_NA, ETYPE_NA, KBEVID_NA,
        KBPREFOR_NA, LANLEVID_NA, LANLPREFOR_NA, LLNLEVID_NA, LLNLPREFOR_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "auth":
        return auth;
      case "etype":
        return etype;
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
      case "etype":
        etype = value;
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
      case "depth":
        return depth;
      case "time":
        return time;
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
      case "depth":
        depth = value;
        break;
      case "time":
        time = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "sevid":
        return sevid;
      case "sprefor":
        return sprefor;
      case "kbevid":
        return kbevid;
      case "kbprefor":
        return kbprefor;
      case "lanlevid":
        return lanlevid;
      case "lanlprefor":
        return lanlprefor;
      case "llnlevid":
        return llnlevid;
      case "llnlprefor":
        return llnlprefor;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "sevid":
        sevid = value;
        break;
      case "sprefor":
        sprefor = value;
        break;
      case "kbevid":
        kbevid = value;
        break;
      case "kbprefor":
        kbprefor = value;
        break;
      case "lanlevid":
        lanlevid = value;
        break;
      case "lanlprefor":
        lanlprefor = value;
        break;
      case "llnlevid":
        llnlevid = value;
        break;
      case "llnlprefor":
        llnlprefor = value;
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
  public Special_event(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Special_event(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), readString(input), readString(input),
        input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Special_event(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), readString(input), readString(input), input.getLong(), input.getLong(),
        input.getLong(), input.getLong(), input.getLong(), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Special_event(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Special_event(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getLong(offset + 9),
        input.getLong(offset + 10), input.getLong(offset + 11), input.getLong(offset + 12),
        input.getLong(offset + 13), input.getLong(offset + 14));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[14];
    values[0] = sevid;
    values[1] = sprefor;
    values[2] = lat;
    values[3] = lon;
    values[4] = depth;
    values[5] = time;
    values[6] = auth;
    values[7] = etype;
    values[8] = kbevid;
    values[9] = kbprefor;
    values[10] = lanlevid;
    values[11] = lanlprefor;
    values[12] = llnlevid;
    values[13] = llnlprefor;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[15];
    values[0] = sevid;
    values[1] = sprefor;
    values[2] = lat;
    values[3] = lon;
    values[4] = depth;
    values[5] = time;
    values[6] = auth;
    values[7] = etype;
    values[8] = kbevid;
    values[9] = kbprefor;
    values[10] = lanlevid;
    values[11] = lanlprefor;
    values[12] = llnlevid;
    values[13] = llnlprefor;
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
    output.writeLong(sevid);
    output.writeLong(sprefor);
    output.writeDouble(lat);
    output.writeDouble(lon);
    output.writeDouble(depth);
    output.writeDouble(time);
    writeString(output, auth);
    writeString(output, etype);
    output.writeLong(kbevid);
    output.writeLong(kbprefor);
    output.writeLong(lanlevid);
    output.writeLong(lanlprefor);
    output.writeLong(llnlevid);
    output.writeLong(llnlprefor);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(sevid);
    output.putLong(sprefor);
    output.putDouble(lat);
    output.putDouble(lon);
    output.putDouble(depth);
    output.putDouble(time);
    writeString(output, auth);
    writeString(output, etype);
    output.putLong(kbevid);
    output.putLong(kbprefor);
    output.putLong(lanlevid);
    output.putLong(lanlprefor);
    output.putLong(llnlevid);
    output.putLong(llnlprefor);
  }

  /**
   * Read a Collection of Special_event objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Special_event objects.
   * @throws IOException
   */
  static public void readSpecial_events(BufferedReader input, Collection<Special_event> rows)
      throws IOException {
    String[] saved = Special_event.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Special_event
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Special_event(new Scanner(line)));
    }
    input.close();
    Special_event.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Special_event objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Special_event objects.
   * @throws IOException
   */
  static public void readSpecial_events(File inputFile, Collection<Special_event> rows)
      throws IOException {
    readSpecial_events(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Special_event objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Special_event objects.
   * @throws IOException
   */
  static public void readSpecial_events(InputStream inputStream, Collection<Special_event> rows)
      throws IOException {
    readSpecial_events(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Special_event objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Special_event objects
   * @throws IOException
   */
  static public Set<Special_event> readSpecial_events(BufferedReader input) throws IOException {
    Set<Special_event> rows = new LinkedHashSet<Special_event>();
    readSpecial_events(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Special_event objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Special_event objects
   * @throws IOException
   */
  static public Set<Special_event> readSpecial_events(File inputFile) throws IOException {
    return readSpecial_events(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Special_event objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Special_event objects
   * @throws IOException
   */
  static public Set<Special_event> readSpecial_events(InputStream input) throws IOException {
    return readSpecial_events(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Special_event objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param special_events the Special_event objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Special_event> special_events)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Special_event special_event : special_events)
      special_event.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Special_event objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param special_events the Special_event objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Special_event> special_events, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Special_event special_event : special_events) {
        int i = 0;
        statement.setLong(++i, special_event.sevid);
        statement.setLong(++i, special_event.sprefor);
        statement.setDouble(++i, special_event.lat);
        statement.setDouble(++i, special_event.lon);
        statement.setDouble(++i, special_event.depth);
        statement.setDouble(++i, special_event.time);
        statement.setString(++i, special_event.auth);
        statement.setString(++i, special_event.etype);
        statement.setLong(++i, special_event.kbevid);
        statement.setLong(++i, special_event.kbprefor);
        statement.setLong(++i, special_event.lanlevid);
        statement.setLong(++i, special_event.lanlprefor);
        statement.setLong(++i, special_event.llnlevid);
        statement.setLong(++i, special_event.llnlprefor);
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
   *        Special_event table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Special_event> readSpecial_events(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Special_event> results = new HashSet<Special_event>();
    readSpecial_events(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Special_event table.
   * @param special_events
   * @throws SQLException
   */
  static public void readSpecial_events(Connection connection, String selectStatement,
      Set<Special_event> special_events) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        special_events.add(new Special_event(rs));
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
   * this Special_event object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Special_event object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "sevid, sprefor, lat, lon, depth, time, auth, etype, kbevid, kbprefor, lanlevid, lanlprefor, llnlevid, llnlprefor, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(sevid)).append(", ");
    sql.append(Long.toString(sprefor)).append(", ");
    sql.append(Double.toString(lat)).append(", ");
    sql.append(Double.toString(lon)).append(", ");
    sql.append(Double.toString(depth)).append(", ");
    sql.append(Double.toString(time)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("'").append(etype).append("', ");
    sql.append(Long.toString(kbevid)).append(", ");
    sql.append(Long.toString(kbprefor)).append(", ");
    sql.append(Long.toString(lanlevid)).append(", ");
    sql.append(Long.toString(lanlprefor)).append(", ");
    sql.append(Long.toString(llnlevid)).append(", ");
    sql.append(Long.toString(llnlprefor)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Special_event in the database. Primary and unique keys are set, if
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
   * Create a table of type Special_event in the database
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
   * Generate a sql script to create a table of type Special_event in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Special_event in the database
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
    buf.append("sevid        number(9)            NOT NULL,\n");
    buf.append("sprefor      number(9)            NOT NULL,\n");
    buf.append("lat          float(53)            NOT NULL,\n");
    buf.append("lon          float(53)            NOT NULL,\n");
    buf.append("depth        float(24)            NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("etype        varchar2(7)          NOT NULL,\n");
    buf.append("kbevid       number(9)            NOT NULL,\n");
    buf.append("kbprefor     number(9)            NOT NULL,\n");
    buf.append("lanlevid     number(9)            NOT NULL,\n");
    buf.append("lanlprefor   number(9)            NOT NULL,\n");
    buf.append("llnlevid     number(9)            NOT NULL,\n");
    buf.append("llnlprefor   number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (sevid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (sprefor,lat,lon,depth,time,auth)");
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
    return 131;
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
    return (other instanceof Special_event) && ((Special_event) other).sevid == sevid;
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
    return (other instanceof Special_event) && ((Special_event) other).sprefor == sprefor
        && ((Special_event) other).lat == lat && ((Special_event) other).lon == lon
        && ((Special_event) other).depth == depth && ((Special_event) other).time == time
        && ((Special_event) other).auth.equals(auth);
  }

  /**
   * Special event identifier
   * 
   * @return sevid
   */
  public long getSevid() {
    return sevid;
  }

  /**
   * Special event identifier
   * 
   * @param sevid
   * @throws IllegalArgumentException if sevid >= 1000000000
   */
  public Special_event setSevid(long sevid) {
    if (sevid >= 1000000000L)
      throw new IllegalArgumentException("sevid=" + sevid + " but cannot be >= 1000000000");
    this.sevid = sevid;
    setHash(null);
    return this;
  }

  /**
   * Special event preferred origin
   * 
   * @return sprefor
   */
  public long getSprefor() {
    return sprefor;
  }

  /**
   * Special event preferred origin
   * 
   * @param sprefor
   * @throws IllegalArgumentException if sprefor >= 1000000000
   */
  public Special_event setSprefor(long sprefor) {
    if (sprefor >= 1000000000L)
      throw new IllegalArgumentException("sprefor=" + sprefor + " but cannot be >= 1000000000");
    this.sprefor = sprefor;
    setHash(null);
    return this;
  }

  /**
   * Geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   * 
   * @return lat
   */
  public double getLat() {
    return lat;
  }

  /**
   * Geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   * 
   * @param lat
   */
  public Special_event setLat(double lat) {
    this.lat = lat;
    setHash(null);
    return this;
  }

  /**
   * Geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @return lon
   */
  public double getLon() {
    return lon;
  }

  /**
   * Geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @param lon
   */
  public Special_event setLon(double lon) {
    this.lon = lon;
    setHash(null);
    return this;
  }

  /**
   * Event depth
   * <p>
   * Units: km
   * 
   * @return depth
   */
  public double getDepth() {
    return depth;
  }

  /**
   * Event depth
   * <p>
   * Units: km
   * 
   * @param depth
   */
  public Special_event setDepth(double depth) {
    this.depth = depth;
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
  public Special_event setTime(double time) {
    this.time = time;
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
  public Special_event setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * Type of seismic event, when known. The following etypes are defined: ex generic explosion ec
   * chemical explosion ep probable explosion en nuclear explosion mc collapse me coal bump/mining
   * event mp probable mining event mb rock burst qt generic earthquake/tectonic qd damaging
   * earthquake qp unknown-probable earthquake qf felt earthquake qm multiple shocks qh quake with
   * associated Harmonic Tremor qv long period event e.g. slow earthquake q2 double shock q4
   * foreshock qa aftershock ge geyser xm meteoritic origin xl lights xo odors - unknown
   * 
   * @return etype
   */
  public String getEtype() {
    return etype;
  }

  /**
   * Type of seismic event, when known. The following etypes are defined: ex generic explosion ec
   * chemical explosion ep probable explosion en nuclear explosion mc collapse me coal bump/mining
   * event mp probable mining event mb rock burst qt generic earthquake/tectonic qd damaging
   * earthquake qp unknown-probable earthquake qf felt earthquake qm multiple shocks qh quake with
   * associated Harmonic Tremor qv long period event e.g. slow earthquake q2 double shock q4
   * foreshock qa aftershock ge geyser xm meteoritic origin xl lights xo odors - unknown
   * 
   * @param etype
   * @throws IllegalArgumentException if etype.length() >= 7
   */
  public Special_event setEtype(String etype) {
    if (etype.length() > 7)
      throw new IllegalArgumentException(
          String.format("etype.length() cannot be > 7.  etype=%s", etype));
    this.etype = etype;
    setHash(null);
    return this;
  }

  /**
   * Event identifier in the Knowledge Base
   * 
   * @return kbevid
   */
  public long getKbevid() {
    return kbevid;
  }

  /**
   * Event identifier in the Knowledge Base
   * 
   * @param kbevid
   * @throws IllegalArgumentException if kbevid >= 1000000000
   */
  public Special_event setKbevid(long kbevid) {
    if (kbevid >= 1000000000L)
      throw new IllegalArgumentException("kbevid=" + kbevid + " but cannot be >= 1000000000");
    this.kbevid = kbevid;
    setHash(null);
    return this;
  }

  /**
   * Preferred origin in the Knowledge Base
   * 
   * @return kbprefor
   */
  public long getKbprefor() {
    return kbprefor;
  }

  /**
   * Preferred origin in the Knowledge Base
   * 
   * @param kbprefor
   * @throws IllegalArgumentException if kbprefor >= 1000000000
   */
  public Special_event setKbprefor(long kbprefor) {
    if (kbprefor >= 1000000000L)
      throw new IllegalArgumentException("kbprefor=" + kbprefor + " but cannot be >= 1000000000");
    this.kbprefor = kbprefor;
    setHash(null);
    return this;
  }

  /**
   * Event identifier at LANL
   * 
   * @return lanlevid
   */
  public long getLanlevid() {
    return lanlevid;
  }

  /**
   * Event identifier at LANL
   * 
   * @param lanlevid
   * @throws IllegalArgumentException if lanlevid >= 1000000000
   */
  public Special_event setLanlevid(long lanlevid) {
    if (lanlevid >= 1000000000L)
      throw new IllegalArgumentException("lanlevid=" + lanlevid + " but cannot be >= 1000000000");
    this.lanlevid = lanlevid;
    setHash(null);
    return this;
  }

  /**
   * Preferred origin at LANL
   * 
   * @return lanlprefor
   */
  public long getLanlprefor() {
    return lanlprefor;
  }

  /**
   * Preferred origin at LANL
   * 
   * @param lanlprefor
   * @throws IllegalArgumentException if lanlprefor >= 1000000000
   */
  public Special_event setLanlprefor(long lanlprefor) {
    if (lanlprefor >= 1000000000L)
      throw new IllegalArgumentException(
          "lanlprefor=" + lanlprefor + " but cannot be >= 1000000000");
    this.lanlprefor = lanlprefor;
    setHash(null);
    return this;
  }

  /**
   * Event identifier at LLNL
   * 
   * @return llnlevid
   */
  public long getLlnlevid() {
    return llnlevid;
  }

  /**
   * Event identifier at LLNL
   * 
   * @param llnlevid
   * @throws IllegalArgumentException if llnlevid >= 1000000000
   */
  public Special_event setLlnlevid(long llnlevid) {
    if (llnlevid >= 1000000000L)
      throw new IllegalArgumentException("llnlevid=" + llnlevid + " but cannot be >= 1000000000");
    this.llnlevid = llnlevid;
    setHash(null);
    return this;
  }

  /**
   * Preferred origin at LLNL
   * 
   * @return llnlprefor
   */
  public long getLlnlprefor() {
    return llnlprefor;
  }

  /**
   * Preferred origin at LLNL
   * 
   * @param llnlprefor
   * @throws IllegalArgumentException if llnlprefor >= 1000000000
   */
  public Special_event setLlnlprefor(long llnlprefor) {
    if (llnlprefor >= 1000000000L)
      throw new IllegalArgumentException(
          "llnlprefor=" + llnlprefor + " but cannot be >= 1000000000");
    this.llnlprefor = llnlprefor;
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
