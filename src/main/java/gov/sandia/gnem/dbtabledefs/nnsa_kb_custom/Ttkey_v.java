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
 * ttkey_v
 */
public class Ttkey_v extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Travel-time model identifier.
   */
  private long ttmodid;

  static final public long TTMODID_NA = -1;

  /**
   * Travel time correction surface identifier.
   */
  private long ttcorrsurfid;

  static final public long TTCORRSURFID_NA = -1;

  /**
   * Modsource identifier.
   */
  private long modsrcid;

  static final public long MODSRCID_NA = -1;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = "-";

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   */
  private String phase;

  static final public String PHASE_NA = "-";

  /**
   * Turn on date. This column is the julian date that the node is promoted for use
   */
  private long ondate;

  static final public long ONDATE_NA = -1;

  /**
   * Turn off date. This column is the julian date that the node is no longer used
   */
  private long offdate;

  static final public long OFFDATE_NA = 2286324;

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
   * The URL of the directory that contains the PGL fdb.
   */
  private String url;

  static final public String URL_NA = "-";

  /**
   * The type of surface representation (tesselated or kriged).
   */
  private String surftype;

  static final public String SURFTYPE_NA = "-";

  /**
   * Keystring
   */
  private String modelkey;

  static final public String MODELKEY_NA = "-";


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("ttmodid", Columns.FieldType.LONG, "%d");
    columns.add("ttcorrsurfid", Columns.FieldType.LONG, "%d");
    columns.add("modsrcid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("phase", Columns.FieldType.STRING, "%s");
    columns.add("ondate", Columns.FieldType.LONG, "%d");
    columns.add("offdate", Columns.FieldType.LONG, "%d");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("endtime", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("url", Columns.FieldType.STRING, "%s");
    columns.add("surftype", Columns.FieldType.STRING, "%s");
    columns.add("modelkey", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Ttkey_v(long ttmodid, long ttcorrsurfid, long modsrcid, String sta, String phase,
      long ondate, long offdate, double time, double endtime, String url, String surftype,
      String modelkey) {
    setValues(ttmodid, ttcorrsurfid, modsrcid, sta, phase, ondate, offdate, time, endtime, url,
        surftype, modelkey);
  }

  private void setValues(long ttmodid, long ttcorrsurfid, long modsrcid, String sta, String phase,
      long ondate, long offdate, double time, double endtime, String url, String surftype,
      String modelkey) {
    this.ttmodid = ttmodid;
    this.ttcorrsurfid = ttcorrsurfid;
    this.modsrcid = modsrcid;
    this.sta = sta;
    this.phase = phase;
    this.ondate = ondate;
    this.offdate = offdate;
    this.time = time;
    this.endtime = endtime;
    this.url = url;
    this.surftype = surftype;
    this.modelkey = modelkey;
  }

  /**
   * Copy constructor.
   */
  public Ttkey_v(Ttkey_v other) {
    this.ttmodid = other.getTtmodid();
    this.ttcorrsurfid = other.getTtcorrsurfid();
    this.modsrcid = other.getModsrcid();
    this.sta = other.getSta();
    this.phase = other.getPhase();
    this.ondate = other.getOndate();
    this.offdate = other.getOffdate();
    this.time = other.getTime();
    this.endtime = other.getEndtime();
    this.url = other.getUrl();
    this.surftype = other.getSurftype();
    this.modelkey = other.getModelkey();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Ttkey_v() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(TTMODID_NA, TTCORRSURFID_NA, MODSRCID_NA, STA_NA, PHASE_NA, ONDATE_NA, OFFDATE_NA,
        TIME_NA, ENDTIME_NA, URL_NA, SURFTYPE_NA, MODELKEY_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "phase":
        return phase;
      case "url":
        return url;
      case "surftype":
        return surftype;
      case "modelkey":
        return modelkey;
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
      case "url":
        url = value;
        break;
      case "surftype":
        surftype = value;
        break;
      case "modelkey":
        modelkey = value;
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
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "ttmodid":
        return ttmodid;
      case "ttcorrsurfid":
        return ttcorrsurfid;
      case "modsrcid":
        return modsrcid;
      case "ondate":
        return ondate;
      case "offdate":
        return offdate;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "ttmodid":
        ttmodid = value;
        break;
      case "ttcorrsurfid":
        ttcorrsurfid = value;
        break;
      case "modsrcid":
        modsrcid = value;
        break;
      case "ondate":
        ondate = value;
        break;
      case "offdate":
        offdate = value;
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
  public Ttkey_v(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Ttkey_v(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), readString(input), readString(input),
        input.readLong(), input.readLong(), input.readDouble(), input.readDouble(),
        readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Ttkey_v(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), readString(input), readString(input),
        input.getLong(), input.getLong(), input.getDouble(), input.getDouble(), readString(input),
        readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ttkey_v(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ttkey_v(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5), input.getLong(offset + 6),
        input.getLong(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getString(offset + 10), input.getString(offset + 11), input.getString(offset + 12));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[12];
    values[0] = ttmodid;
    values[1] = ttcorrsurfid;
    values[2] = modsrcid;
    values[3] = sta;
    values[4] = phase;
    values[5] = ondate;
    values[6] = offdate;
    values[7] = time;
    values[8] = endtime;
    values[9] = url;
    values[10] = surftype;
    values[11] = modelkey;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[13];
    values[0] = ttmodid;
    values[1] = ttcorrsurfid;
    values[2] = modsrcid;
    values[3] = sta;
    values[4] = phase;
    values[5] = ondate;
    values[6] = offdate;
    values[7] = time;
    values[8] = endtime;
    values[9] = url;
    values[10] = surftype;
    values[11] = modelkey;
    values[12] = lddate;
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
    output.writeLong(ttmodid);
    output.writeLong(ttcorrsurfid);
    output.writeLong(modsrcid);
    writeString(output, sta);
    writeString(output, phase);
    output.writeLong(ondate);
    output.writeLong(offdate);
    output.writeDouble(time);
    output.writeDouble(endtime);
    writeString(output, url);
    writeString(output, surftype);
    writeString(output, modelkey);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(ttmodid);
    output.putLong(ttcorrsurfid);
    output.putLong(modsrcid);
    writeString(output, sta);
    writeString(output, phase);
    output.putLong(ondate);
    output.putLong(offdate);
    output.putDouble(time);
    output.putDouble(endtime);
    writeString(output, url);
    writeString(output, surftype);
    writeString(output, modelkey);
  }

  /**
   * Read a Collection of Ttkey_v objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Ttkey_v objects.
   * @throws IOException
   */
  static public void readTtkey_vs(BufferedReader input, Collection<Ttkey_v> rows)
      throws IOException {
    String[] saved = Ttkey_v.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Ttkey_v.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Ttkey_v(new Scanner(line)));
    }
    input.close();
    Ttkey_v.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Ttkey_v objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Ttkey_v objects.
   * @throws IOException
   */
  static public void readTtkey_vs(File inputFile, Collection<Ttkey_v> rows) throws IOException {
    readTtkey_vs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Ttkey_v objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Ttkey_v objects.
   * @throws IOException
   */
  static public void readTtkey_vs(InputStream inputStream, Collection<Ttkey_v> rows)
      throws IOException {
    readTtkey_vs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Ttkey_v objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Ttkey_v objects
   * @throws IOException
   */
  static public Set<Ttkey_v> readTtkey_vs(BufferedReader input) throws IOException {
    Set<Ttkey_v> rows = new LinkedHashSet<Ttkey_v>();
    readTtkey_vs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Ttkey_v objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Ttkey_v objects
   * @throws IOException
   */
  static public Set<Ttkey_v> readTtkey_vs(File inputFile) throws IOException {
    return readTtkey_vs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Ttkey_v objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Ttkey_v objects
   * @throws IOException
   */
  static public Set<Ttkey_v> readTtkey_vs(InputStream input) throws IOException {
    return readTtkey_vs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Ttkey_v objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param ttkey_vs the Ttkey_v objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Ttkey_v> ttkey_vs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Ttkey_v ttkey_v : ttkey_vs)
      ttkey_v.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Ttkey_v objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param ttkey_vs the Ttkey_v objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Ttkey_v> ttkey_vs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Ttkey_v ttkey_v : ttkey_vs) {
        int i = 0;
        statement.setLong(++i, ttkey_v.ttmodid);
        statement.setLong(++i, ttkey_v.ttcorrsurfid);
        statement.setLong(++i, ttkey_v.modsrcid);
        statement.setString(++i, ttkey_v.sta);
        statement.setString(++i, ttkey_v.phase);
        statement.setLong(++i, ttkey_v.ondate);
        statement.setLong(++i, ttkey_v.offdate);
        statement.setDouble(++i, ttkey_v.time);
        statement.setDouble(++i, ttkey_v.endtime);
        statement.setString(++i, ttkey_v.url);
        statement.setString(++i, ttkey_v.surftype);
        statement.setString(++i, ttkey_v.modelkey);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Ttkey_v
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Ttkey_v> readTtkey_vs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Ttkey_v> results = new HashSet<Ttkey_v>();
    readTtkey_vs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Ttkey_v
   *        table.
   * @param ttkey_vs
   * @throws SQLException
   */
  static public void readTtkey_vs(Connection connection, String selectStatement,
      Set<Ttkey_v> ttkey_vs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        ttkey_vs.add(new Ttkey_v(rs));
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
   * this Ttkey_v object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Ttkey_v object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "ttmodid, ttcorrsurfid, modsrcid, sta, phase, ondate, offdate, time, endtime, url, surftype, modelkey, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(ttmodid)).append(", ");
    sql.append(Long.toString(ttcorrsurfid)).append(", ");
    sql.append(Long.toString(modsrcid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(phase).append("', ");
    sql.append(Long.toString(ondate)).append(", ");
    sql.append(Long.toString(offdate)).append(", ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Double.toString(endtime)).append(", ");
    sql.append("'").append(url).append("', ");
    sql.append("'").append(surftype).append("', ");
    sql.append("'").append(modelkey).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Ttkey_v in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Ttkey_v in the database
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
   * Generate a sql script to create a table of type Ttkey_v in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Ttkey_v in the database
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
    buf.append("ttmodid      number(9)            NOT NULL,\n");
    buf.append("ttcorrsurfid number(9)            NOT NULL,\n");
    buf.append("modsrcid     number(9)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("phase        varchar2(8)          NOT NULL,\n");
    buf.append("ondate       number(8)            NOT NULL,\n");
    buf.append("offdate      number(8)            NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("endtime      float(53)            NOT NULL,\n");
    buf.append("url          varchar2(256)        NOT NULL,\n");
    buf.append("surftype     varchar2(30)         NOT NULL,\n");
    buf.append("modelkey     varchar2(32)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
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
    return 408;
  }

  /**
   * Travel-time model identifier.
   * 
   * @return ttmodid
   */
  public long getTtmodid() {
    return ttmodid;
  }

  /**
   * Travel-time model identifier.
   * 
   * @param ttmodid
   * @throws IllegalArgumentException if ttmodid >= 1000000000
   */
  public Ttkey_v setTtmodid(long ttmodid) {
    if (ttmodid >= 1000000000L)
      throw new IllegalArgumentException("ttmodid=" + ttmodid + " but cannot be >= 1000000000");
    this.ttmodid = ttmodid;
    setHash(null);
    return this;
  }

  /**
   * Travel time correction surface identifier.
   * 
   * @return ttcorrsurfid
   */
  public long getTtcorrsurfid() {
    return ttcorrsurfid;
  }

  /**
   * Travel time correction surface identifier.
   * 
   * @param ttcorrsurfid
   * @throws IllegalArgumentException if ttcorrsurfid >= 1000000000
   */
  public Ttkey_v setTtcorrsurfid(long ttcorrsurfid) {
    if (ttcorrsurfid >= 1000000000L)
      throw new IllegalArgumentException(
          "ttcorrsurfid=" + ttcorrsurfid + " but cannot be >= 1000000000");
    this.ttcorrsurfid = ttcorrsurfid;
    setHash(null);
    return this;
  }

  /**
   * Modsource identifier.
   * 
   * @return modsrcid
   */
  public long getModsrcid() {
    return modsrcid;
  }

  /**
   * Modsource identifier.
   * 
   * @param modsrcid
   * @throws IllegalArgumentException if modsrcid >= 1000000000
   */
  public Ttkey_v setModsrcid(long modsrcid) {
    if (modsrcid >= 1000000000L)
      throw new IllegalArgumentException("modsrcid=" + modsrcid + " but cannot be >= 1000000000");
    this.modsrcid = modsrcid;
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
  public Ttkey_v setSta(String sta) {
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
  public Ttkey_v setPhase(String phase) {
    if (phase.length() > 8)
      throw new IllegalArgumentException(
          String.format("phase.length() cannot be > 8.  phase=%s", phase));
    this.phase = phase;
    setHash(null);
    return this;
  }

  /**
   * Turn on date. This column is the julian date that the node is promoted for use
   * 
   * @return ondate
   */
  public long getOndate() {
    return ondate;
  }

  /**
   * Turn on date. This column is the julian date that the node is promoted for use
   * 
   * @param ondate
   * @throws IllegalArgumentException if ondate >= 100000000
   */
  public Ttkey_v setOndate(long ondate) {
    if (ondate >= 100000000L)
      throw new IllegalArgumentException("ondate=" + ondate + " but cannot be >= 100000000");
    this.ondate = ondate;
    setHash(null);
    return this;
  }

  /**
   * Turn off date. This column is the julian date that the node is no longer used
   * 
   * @return offdate
   */
  public long getOffdate() {
    return offdate;
  }

  /**
   * Turn off date. This column is the julian date that the node is no longer used
   * 
   * @param offdate
   * @throws IllegalArgumentException if offdate >= 100000000
   */
  public Ttkey_v setOffdate(long offdate) {
    if (offdate >= 100000000L)
      throw new IllegalArgumentException("offdate=" + offdate + " but cannot be >= 100000000");
    this.offdate = offdate;
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
  public Ttkey_v setTime(double time) {
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
  public Ttkey_v setEndtime(double endtime) {
    this.endtime = endtime;
    setHash(null);
    return this;
  }

  /**
   * The URL of the directory that contains the PGL fdb.
   * 
   * @return url
   */
  public String getUrl() {
    return url;
  }

  /**
   * The URL of the directory that contains the PGL fdb.
   * 
   * @param url
   * @throws IllegalArgumentException if url.length() >= 256
   */
  public Ttkey_v setUrl(String url) {
    if (url.length() > 256)
      throw new IllegalArgumentException(
          String.format("url.length() cannot be > 256.  url=%s", url));
    this.url = url;
    setHash(null);
    return this;
  }

  /**
   * The type of surface representation (tesselated or kriged).
   * 
   * @return surftype
   */
  public String getSurftype() {
    return surftype;
  }

  /**
   * The type of surface representation (tesselated or kriged).
   * 
   * @param surftype
   * @throws IllegalArgumentException if surftype.length() >= 30
   */
  public Ttkey_v setSurftype(String surftype) {
    if (surftype.length() > 30)
      throw new IllegalArgumentException(
          String.format("surftype.length() cannot be > 30.  surftype=%s", surftype));
    this.surftype = surftype;
    setHash(null);
    return this;
  }

  /**
   * Keystring
   * 
   * @return modelkey
   */
  public String getModelkey() {
    return modelkey;
  }

  /**
   * Keystring
   * 
   * @param modelkey
   * @throws IllegalArgumentException if modelkey.length() >= 32
   */
  public Ttkey_v setModelkey(String modelkey) {
    if (modelkey.length() > 32)
      throw new IllegalArgumentException(
          String.format("modelkey.length() cannot be > 32.  modelkey=%s", modelkey));
    this.modelkey = modelkey;
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
