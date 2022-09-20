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
 * cal_origin
 */
public class Cal_origin extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   */
  private long evid;

  static final public long EVID_NA = Long.MIN_VALUE;

  /**
   * GT epicenter identifier
   */
  private long epicenterid;

  static final public long EPICENTERID_NA = -1;

  /**
   * GT time identifier
   */
  private long timeid;

  static final public long TIMEID_NA = -1;

  /**
   * GT depth identifier
   */
  private long depthid;

  static final public long DEPTHID_NA = -1;

  /**
   * GT etype identifier
   */
  private long etypeid;

  static final public long ETYPEID_NA = -1;

  /**
   * The organization code of the organization that produced this data.
   */
  private String contrib_org;

  static final public String CONTRIB_ORG_NA = null;

  /**
   * Author who loaded data
   */
  private String ldauth;

  static final public String LDAUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("evid", Columns.FieldType.LONG, "%d");
    columns.add("epicenterid", Columns.FieldType.LONG, "%d");
    columns.add("timeid", Columns.FieldType.LONG, "%d");
    columns.add("depthid", Columns.FieldType.LONG, "%d");
    columns.add("etypeid", Columns.FieldType.LONG, "%d");
    columns.add("contrib_org", Columns.FieldType.STRING, "%s");
    columns.add("ldauth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Cal_origin(long orid, long evid, long epicenterid, long timeid, long depthid, long etypeid,
      String contrib_org, String ldauth) {
    setValues(orid, evid, epicenterid, timeid, depthid, etypeid, contrib_org, ldauth);
  }

  private void setValues(long orid, long evid, long epicenterid, long timeid, long depthid,
      long etypeid, String contrib_org, String ldauth) {
    this.orid = orid;
    this.evid = evid;
    this.epicenterid = epicenterid;
    this.timeid = timeid;
    this.depthid = depthid;
    this.etypeid = etypeid;
    this.contrib_org = contrib_org;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Cal_origin(Cal_origin other) {
    this.orid = other.getOrid();
    this.evid = other.getEvid();
    this.epicenterid = other.getEpicenterid();
    this.timeid = other.getTimeid();
    this.depthid = other.getDepthid();
    this.etypeid = other.getEtypeid();
    this.contrib_org = other.getContrib_org();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Cal_origin() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(ORID_NA, EVID_NA, EPICENTERID_NA, TIMEID_NA, DEPTHID_NA, ETYPEID_NA, CONTRIB_ORG_NA,
        LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "contrib_org":
        return contrib_org;
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
      case "contrib_org":
        contrib_org = value;
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
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
        + " is not a valid input name ...");
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "orid":
        return orid;
      case "evid":
        return evid;
      case "epicenterid":
        return epicenterid;
      case "timeid":
        return timeid;
      case "depthid":
        return depthid;
      case "etypeid":
        return etypeid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "orid":
        orid = value;
        break;
      case "evid":
        evid = value;
        break;
      case "epicenterid":
        epicenterid = value;
        break;
      case "timeid":
        timeid = value;
        break;
      case "depthid":
        depthid = value;
        break;
      case "etypeid":
        etypeid = value;
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
  public Cal_origin(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Cal_origin(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        input.readLong(), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Cal_origin(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getLong(),
        input.getLong(), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Cal_origin(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Cal_origin(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[8];
    values[0] = orid;
    values[1] = evid;
    values[2] = epicenterid;
    values[3] = timeid;
    values[4] = depthid;
    values[5] = etypeid;
    values[6] = contrib_org;
    values[7] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[9];
    values[0] = orid;
    values[1] = evid;
    values[2] = epicenterid;
    values[3] = timeid;
    values[4] = depthid;
    values[5] = etypeid;
    values[6] = contrib_org;
    values[7] = ldauth;
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
    output.writeLong(orid);
    output.writeLong(evid);
    output.writeLong(epicenterid);
    output.writeLong(timeid);
    output.writeLong(depthid);
    output.writeLong(etypeid);
    writeString(output, contrib_org);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(orid);
    output.putLong(evid);
    output.putLong(epicenterid);
    output.putLong(timeid);
    output.putLong(depthid);
    output.putLong(etypeid);
    writeString(output, contrib_org);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Cal_origin objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Cal_origin objects.
   * @throws IOException
   */
  static public void readCal_origins(BufferedReader input, Collection<Cal_origin> rows)
      throws IOException {
    String[] saved = Cal_origin.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Cal_origin
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Cal_origin(new Scanner(line)));
    }
    input.close();
    Cal_origin.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Cal_origin objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Cal_origin objects.
   * @throws IOException
   */
  static public void readCal_origins(File inputFile, Collection<Cal_origin> rows)
      throws IOException {
    readCal_origins(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Cal_origin objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Cal_origin objects.
   * @throws IOException
   */
  static public void readCal_origins(InputStream inputStream, Collection<Cal_origin> rows)
      throws IOException {
    readCal_origins(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Cal_origin objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Cal_origin objects
   * @throws IOException
   */
  static public Set<Cal_origin> readCal_origins(BufferedReader input) throws IOException {
    Set<Cal_origin> rows = new LinkedHashSet<Cal_origin>();
    readCal_origins(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Cal_origin objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Cal_origin objects
   * @throws IOException
   */
  static public Set<Cal_origin> readCal_origins(File inputFile) throws IOException {
    return readCal_origins(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Cal_origin objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Cal_origin objects
   * @throws IOException
   */
  static public Set<Cal_origin> readCal_origins(InputStream input) throws IOException {
    return readCal_origins(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Cal_origin objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param cal_origins the Cal_origin objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Cal_origin> cal_origins)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Cal_origin cal_origin : cal_origins)
      cal_origin.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Cal_origin objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param cal_origins the Cal_origin objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Cal_origin> cal_origins, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?)");
      for (Cal_origin cal_origin : cal_origins) {
        int i = 0;
        statement.setLong(++i, cal_origin.orid);
        statement.setLong(++i, cal_origin.evid);
        statement.setLong(++i, cal_origin.epicenterid);
        statement.setLong(++i, cal_origin.timeid);
        statement.setLong(++i, cal_origin.depthid);
        statement.setLong(++i, cal_origin.etypeid);
        statement.setString(++i, cal_origin.contrib_org);
        statement.setString(++i, cal_origin.ldauth);
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
   *        Cal_origin table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Cal_origin> readCal_origins(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Cal_origin> results = new HashSet<Cal_origin>();
    readCal_origins(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Cal_origin table.
   * @param cal_origins
   * @throws SQLException
   */
  static public void readCal_origins(Connection connection, String selectStatement,
      Set<Cal_origin> cal_origins) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        cal_origins.add(new Cal_origin(rs));
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
   * this Cal_origin object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Cal_origin object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("orid, evid, epicenterid, timeid, depthid, etypeid, contrib_org, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(evid)).append(", ");
    sql.append(Long.toString(epicenterid)).append(", ");
    sql.append(Long.toString(timeid)).append(", ");
    sql.append(Long.toString(depthid)).append(", ");
    sql.append(Long.toString(etypeid)).append(", ");
    sql.append("'").append(contrib_org).append("', ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Cal_origin in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Cal_origin in the database
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
   * Generate a sql script to create a table of type Cal_origin in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Cal_origin in the database
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
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("evid         number(9)            NOT NULL,\n");
    buf.append("epicenterid  number(9)            NOT NULL,\n");
    buf.append("timeid       number(9)            NOT NULL,\n");
    buf.append("depthid      number(9)            NOT NULL,\n");
    buf.append("etypeid      number(9)            NOT NULL,\n");
    buf.append("contrib_org  varchar2(15)         NOT NULL,\n");
    buf.append("ldauth       varchar2(15)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (orid)");
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
    return 86;
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
    return (other instanceof Cal_origin) && ((Cal_origin) other).orid == orid;
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
  public Cal_origin setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   * 
   * @return evid
   */
  public long getEvid() {
    return evid;
  }

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   * 
   * @param evid
   * @throws IllegalArgumentException if evid >= 1000000000
   */
  public Cal_origin setEvid(long evid) {
    if (evid >= 1000000000L)
      throw new IllegalArgumentException("evid=" + evid + " but cannot be >= 1000000000");
    this.evid = evid;
    setHash(null);
    return this;
  }

  /**
   * GT epicenter identifier
   * 
   * @return epicenterid
   */
  public long getEpicenterid() {
    return epicenterid;
  }

  /**
   * GT epicenter identifier
   * 
   * @param epicenterid
   * @throws IllegalArgumentException if epicenterid >= 1000000000
   */
  public Cal_origin setEpicenterid(long epicenterid) {
    if (epicenterid >= 1000000000L)
      throw new IllegalArgumentException(
          "epicenterid=" + epicenterid + " but cannot be >= 1000000000");
    this.epicenterid = epicenterid;
    setHash(null);
    return this;
  }

  /**
   * GT time identifier
   * 
   * @return timeid
   */
  public long getTimeid() {
    return timeid;
  }

  /**
   * GT time identifier
   * 
   * @param timeid
   * @throws IllegalArgumentException if timeid >= 1000000000
   */
  public Cal_origin setTimeid(long timeid) {
    if (timeid >= 1000000000L)
      throw new IllegalArgumentException("timeid=" + timeid + " but cannot be >= 1000000000");
    this.timeid = timeid;
    setHash(null);
    return this;
  }

  /**
   * GT depth identifier
   * 
   * @return depthid
   */
  public long getDepthid() {
    return depthid;
  }

  /**
   * GT depth identifier
   * 
   * @param depthid
   * @throws IllegalArgumentException if depthid >= 1000000000
   */
  public Cal_origin setDepthid(long depthid) {
    if (depthid >= 1000000000L)
      throw new IllegalArgumentException("depthid=" + depthid + " but cannot be >= 1000000000");
    this.depthid = depthid;
    setHash(null);
    return this;
  }

  /**
   * GT etype identifier
   * 
   * @return etypeid
   */
  public long getEtypeid() {
    return etypeid;
  }

  /**
   * GT etype identifier
   * 
   * @param etypeid
   * @throws IllegalArgumentException if etypeid >= 1000000000
   */
  public Cal_origin setEtypeid(long etypeid) {
    if (etypeid >= 1000000000L)
      throw new IllegalArgumentException("etypeid=" + etypeid + " but cannot be >= 1000000000");
    this.etypeid = etypeid;
    setHash(null);
    return this;
  }

  /**
   * The organization code of the organization that produced this data.
   * 
   * @return contrib_org
   */
  public String getContrib_org() {
    return contrib_org;
  }

  /**
   * The organization code of the organization that produced this data.
   * 
   * @param contrib_org
   * @throws IllegalArgumentException if contrib_org.length() >= 15
   */
  public Cal_origin setContrib_org(String contrib_org) {
    if (contrib_org.length() > 15)
      throw new IllegalArgumentException(
          String.format("contrib_org.length() cannot be > 15.  contrib_org=%s", contrib_org));
    this.contrib_org = contrib_org;
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
  public Cal_origin setLdauth(String ldauth) {
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
