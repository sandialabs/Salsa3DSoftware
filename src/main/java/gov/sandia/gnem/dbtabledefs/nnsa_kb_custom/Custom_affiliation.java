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
 * custom_affiliation
 */
public class Custom_affiliation extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique network identifier.
   */
  private String net;

  static final public String NET_NA = null;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = null;

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
   * Network identifier that either uniquely identifies a network or relates a record to a network
   */
  private long netid;

  static final public long NETID_NA = Long.MIN_VALUE;

  /**
   * Site identifier that either uniquely idenitifes a station or relations a record to a station
   */
  private long siteid;

  static final public long SITEID_NA = Long.MIN_VALUE;

  /**
   * Turn on date. This column is the julian date that the node is promoted for use
   */
  private long ondate;

  static final public long ONDATE_NA = Long.MIN_VALUE;

  /**
   * Turn off date. This column is the julian date that the node is no longer used
   */
  private long offdate;

  static final public long OFFDATE_NA = Long.MIN_VALUE;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("net", Columns.FieldType.STRING, "%s");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("endtime", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("netid", Columns.FieldType.LONG, "%d");
    columns.add("siteid", Columns.FieldType.LONG, "%d");
    columns.add("ondate", Columns.FieldType.LONG, "%d");
    columns.add("offdate", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Custom_affiliation(String net, String sta, double time, double endtime, long netid,
      long siteid, long ondate, long offdate) {
    setValues(net, sta, time, endtime, netid, siteid, ondate, offdate);
  }

  private void setValues(String net, String sta, double time, double endtime, long netid,
      long siteid, long ondate, long offdate) {
    this.net = net;
    this.sta = sta;
    this.time = time;
    this.endtime = endtime;
    this.netid = netid;
    this.siteid = siteid;
    this.ondate = ondate;
    this.offdate = offdate;
  }

  /**
   * Copy constructor.
   */
  public Custom_affiliation(Custom_affiliation other) {
    this.net = other.getNet();
    this.sta = other.getSta();
    this.time = other.getTime();
    this.endtime = other.getEndtime();
    this.netid = other.getNetid();
    this.siteid = other.getSiteid();
    this.ondate = other.getOndate();
    this.offdate = other.getOffdate();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Custom_affiliation() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(NET_NA, STA_NA, TIME_NA, ENDTIME_NA, NETID_NA, SITEID_NA, ONDATE_NA, OFFDATE_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "net":
        return net;
      case "sta":
        return sta;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "net":
        net = value;
        break;
      case "sta":
        sta = value;
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
      case "netid":
        return netid;
      case "siteid":
        return siteid;
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
      case "netid":
        netid = value;
        break;
      case "siteid":
        siteid = value;
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
  public Custom_affiliation(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Custom_affiliation(DataInputStream input) throws IOException {
    this(readString(input), readString(input), input.readDouble(), input.readDouble(),
        input.readLong(), input.readLong(), input.readLong(), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Custom_affiliation(ByteBuffer input) {
    this(readString(input), readString(input), input.getDouble(), input.getDouble(),
        input.getLong(), input.getLong(), input.getLong(), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Custom_affiliation(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Custom_affiliation(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getString(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getLong(offset + 7), input.getLong(offset + 8));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[8];
    values[0] = net;
    values[1] = sta;
    values[2] = time;
    values[3] = endtime;
    values[4] = netid;
    values[5] = siteid;
    values[6] = ondate;
    values[7] = offdate;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[9];
    values[0] = net;
    values[1] = sta;
    values[2] = time;
    values[3] = endtime;
    values[4] = netid;
    values[5] = siteid;
    values[6] = ondate;
    values[7] = offdate;
    values[8] = lddate;
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
    writeString(output, net);
    writeString(output, sta);
    output.writeDouble(time);
    output.writeDouble(endtime);
    output.writeLong(netid);
    output.writeLong(siteid);
    output.writeLong(ondate);
    output.writeLong(offdate);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, net);
    writeString(output, sta);
    output.putDouble(time);
    output.putDouble(endtime);
    output.putLong(netid);
    output.putLong(siteid);
    output.putLong(ondate);
    output.putLong(offdate);
  }

  /**
   * Read a Collection of Custom_affiliation objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Custom_affiliation objects.
   * @throws IOException
   */
  static public void readCustom_affiliations(BufferedReader input,
      Collection<Custom_affiliation> rows) throws IOException {
    String[] saved = Custom_affiliation.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Custom_affiliation
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Custom_affiliation(new Scanner(line)));
    }
    input.close();
    Custom_affiliation.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Custom_affiliation objects from an ascii file. The Collection is not
   * emptied before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Custom_affiliation objects.
   * @throws IOException
   */
  static public void readCustom_affiliations(File inputFile, Collection<Custom_affiliation> rows)
      throws IOException {
    readCustom_affiliations(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Custom_affiliation objects from an ascii input stream. The Collection is
   * not emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Custom_affiliation objects.
   * @throws IOException
   */
  static public void readCustom_affiliations(InputStream inputStream,
      Collection<Custom_affiliation> rows) throws IOException {
    readCustom_affiliations(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Custom_affiliation objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Custom_affiliation objects
   * @throws IOException
   */
  static public Set<Custom_affiliation> readCustom_affiliations(BufferedReader input)
      throws IOException {
    Set<Custom_affiliation> rows = new LinkedHashSet<Custom_affiliation>();
    readCustom_affiliations(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Custom_affiliation objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Custom_affiliation objects
   * @throws IOException
   */
  static public Set<Custom_affiliation> readCustom_affiliations(File inputFile) throws IOException {
    return readCustom_affiliations(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Custom_affiliation objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Custom_affiliation objects
   * @throws IOException
   */
  static public Set<Custom_affiliation> readCustom_affiliations(InputStream input)
      throws IOException {
    return readCustom_affiliations(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Custom_affiliation objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param custom_affiliations the Custom_affiliation objects to write
   * @throws IOException
   */
  static public void write(File fileName,
      Collection<? extends Custom_affiliation> custom_affiliations) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Custom_affiliation custom_affiliation : custom_affiliations)
      custom_affiliation.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Custom_affiliation objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param custom_affiliations the Custom_affiliation objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Custom_affiliation> custom_affiliations, java.util.Date lddate,
      boolean commit) throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?)");
      for (Custom_affiliation custom_affiliation : custom_affiliations) {
        int i = 0;
        statement.setString(++i, custom_affiliation.net);
        statement.setString(++i, custom_affiliation.sta);
        statement.setDouble(++i, custom_affiliation.time);
        statement.setDouble(++i, custom_affiliation.endtime);
        statement.setLong(++i, custom_affiliation.netid);
        statement.setLong(++i, custom_affiliation.siteid);
        statement.setLong(++i, custom_affiliation.ondate);
        statement.setLong(++i, custom_affiliation.offdate);
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
   *        Custom_affiliation table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Custom_affiliation> readCustom_affiliations(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Custom_affiliation> results = new HashSet<Custom_affiliation>();
    readCustom_affiliations(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Custom_affiliation table.
   * @param custom_affiliations
   * @throws SQLException
   */
  static public void readCustom_affiliations(Connection connection, String selectStatement,
      Set<Custom_affiliation> custom_affiliations) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        custom_affiliations.add(new Custom_affiliation(rs));
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
   * this Custom_affiliation object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Custom_affiliation object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("net, sta, time, endtime, netid, siteid, ondate, offdate, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(net).append("', ");
    sql.append("'").append(sta).append("', ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Double.toString(endtime)).append(", ");
    sql.append(Long.toString(netid)).append(", ");
    sql.append(Long.toString(siteid)).append(", ");
    sql.append(Long.toString(ondate)).append(", ");
    sql.append(Long.toString(offdate)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Custom_affiliation in the database. Primary and unique keys are set, if
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
   * Create a table of type Custom_affiliation in the database
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
   * Generate a sql script to create a table of type Custom_affiliation in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Custom_affiliation in the database
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
    buf.append("net          varchar2(8)          NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("endtime      float(53)            NOT NULL,\n");
    buf.append("netid        number(9)            NOT NULL,\n");
    buf.append("siteid       number(9)            NOT NULL,\n");
    buf.append("ondate       number(8)            NOT NULL,\n");
    buf.append("offdate      number(8)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (netid,siteid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (net,sta,time)");
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
    return 70;
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
    return (other instanceof Custom_affiliation) && ((Custom_affiliation) other).netid == netid
        && ((Custom_affiliation) other).siteid == siteid;
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
    return (other instanceof Custom_affiliation) && ((Custom_affiliation) other).net.equals(net)
        && ((Custom_affiliation) other).sta.equals(sta)
        && ((Custom_affiliation) other).time == time;
  }

  /**
   * Unique network identifier.
   * 
   * @return net
   */
  public String getNet() {
    return net;
  }

  /**
   * Unique network identifier.
   * 
   * @param net
   * @throws IllegalArgumentException if net.length() >= 8
   */
  public Custom_affiliation setNet(String net) {
    if (net.length() > 8)
      throw new IllegalArgumentException(String.format("net.length() cannot be > 8.  net=%s", net));
    this.net = net;
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
  public Custom_affiliation setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
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
  public Custom_affiliation setTime(double time) {
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
  public Custom_affiliation setEndtime(double endtime) {
    this.endtime = endtime;
    setHash(null);
    return this;
  }

  /**
   * Network identifier that either uniquely identifies a network or relates a record to a network
   * 
   * @return netid
   */
  public long getNetid() {
    return netid;
  }

  /**
   * Network identifier that either uniquely identifies a network or relates a record to a network
   * 
   * @param netid
   * @throws IllegalArgumentException if netid >= 1000000000
   */
  public Custom_affiliation setNetid(long netid) {
    if (netid >= 1000000000L)
      throw new IllegalArgumentException("netid=" + netid + " but cannot be >= 1000000000");
    this.netid = netid;
    setHash(null);
    return this;
  }

  /**
   * Site identifier that either uniquely idenitifes a station or relations a record to a station
   * 
   * @return siteid
   */
  public long getSiteid() {
    return siteid;
  }

  /**
   * Site identifier that either uniquely idenitifes a station or relations a record to a station
   * 
   * @param siteid
   * @throws IllegalArgumentException if siteid >= 1000000000
   */
  public Custom_affiliation setSiteid(long siteid) {
    if (siteid >= 1000000000L)
      throw new IllegalArgumentException("siteid=" + siteid + " but cannot be >= 1000000000");
    this.siteid = siteid;
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
  public Custom_affiliation setOndate(long ondate) {
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
  public Custom_affiliation setOffdate(long offdate) {
    if (offdate >= 100000000L)
      throw new IllegalArgumentException("offdate=" + offdate + " but cannot be >= 100000000");
    this.offdate = offdate;
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
