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
 * pref_eventid_ver
 */
public class Pref_eventid_ver extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Preferred version. This column holds the version identifier (versionid) that points to the
   * preferred version for the set of eventid parameters.
   */
  private long prefvid;

  static final public long PREFVID_NA = Long.MIN_VALUE;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   */
  private String chan;

  static final public String CHAN_NA = null;

  /**
   * Unique identifier for version of a set of parameter files.
   */
  private long versionid;

  static final public long VERSIONID_NA = Long.MIN_VALUE;

  /**
   * A name that is applied to the particular set of eventid parameters that is currently preferred
   * for use for a given station, channel, phase, and frequency range, e.g. KB7.1. In cases where
   * more than one set of parameters is available, this tag will be used by an application to select
   * the set of parameters to use.
   */
  private String pref_tag;

  static final public String PREF_TAG_NA = null;

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
    columns.add("prefvid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("versionid", Columns.FieldType.LONG, "%d");
    columns.add("pref_tag", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Pref_eventid_ver(long prefvid, String sta, String chan, long versionid, String pref_tag,
      String auth) {
    setValues(prefvid, sta, chan, versionid, pref_tag, auth);
  }

  private void setValues(long prefvid, String sta, String chan, long versionid, String pref_tag,
      String auth) {
    this.prefvid = prefvid;
    this.sta = sta;
    this.chan = chan;
    this.versionid = versionid;
    this.pref_tag = pref_tag;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Pref_eventid_ver(Pref_eventid_ver other) {
    this.prefvid = other.getPrefvid();
    this.sta = other.getSta();
    this.chan = other.getChan();
    this.versionid = other.getVersionid();
    this.pref_tag = other.getPref_tag();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Pref_eventid_ver() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(PREFVID_NA, STA_NA, CHAN_NA, VERSIONID_NA, PREF_TAG_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "chan":
        return chan;
      case "pref_tag":
        return pref_tag;
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
      case "sta":
        sta = value;
        break;
      case "chan":
        chan = value;
        break;
      case "pref_tag":
        pref_tag = value;
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
      case "prefvid":
        return prefvid;
      case "versionid":
        return versionid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "prefvid":
        prefvid = value;
        break;
      case "versionid":
        versionid = value;
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
  public Pref_eventid_ver(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Pref_eventid_ver(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), input.readLong(),
        readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Pref_eventid_ver(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), input.getLong(), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Pref_eventid_ver(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Pref_eventid_ver(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getLong(offset + 4), input.getString(offset + 5), input.getString(offset + 6));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[6];
    values[0] = prefvid;
    values[1] = sta;
    values[2] = chan;
    values[3] = versionid;
    values[4] = pref_tag;
    values[5] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[7];
    values[0] = prefvid;
    values[1] = sta;
    values[2] = chan;
    values[3] = versionid;
    values[4] = pref_tag;
    values[5] = auth;
    values[6] = lddate;
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
    output.writeLong(prefvid);
    writeString(output, sta);
    writeString(output, chan);
    output.writeLong(versionid);
    writeString(output, pref_tag);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(prefvid);
    writeString(output, sta);
    writeString(output, chan);
    output.putLong(versionid);
    writeString(output, pref_tag);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Pref_eventid_ver objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Pref_eventid_ver objects.
   * @throws IOException
   */
  static public void readPref_eventid_vers(BufferedReader input, Collection<Pref_eventid_ver> rows)
      throws IOException {
    String[] saved = Pref_eventid_ver.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Pref_eventid_ver
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Pref_eventid_ver(new Scanner(line)));
    }
    input.close();
    Pref_eventid_ver.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Pref_eventid_ver objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Pref_eventid_ver objects.
   * @throws IOException
   */
  static public void readPref_eventid_vers(File inputFile, Collection<Pref_eventid_ver> rows)
      throws IOException {
    readPref_eventid_vers(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Pref_eventid_ver objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Pref_eventid_ver objects.
   * @throws IOException
   */
  static public void readPref_eventid_vers(InputStream inputStream,
      Collection<Pref_eventid_ver> rows) throws IOException {
    readPref_eventid_vers(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Pref_eventid_ver objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Pref_eventid_ver objects
   * @throws IOException
   */
  static public Set<Pref_eventid_ver> readPref_eventid_vers(BufferedReader input)
      throws IOException {
    Set<Pref_eventid_ver> rows = new LinkedHashSet<Pref_eventid_ver>();
    readPref_eventid_vers(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Pref_eventid_ver objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Pref_eventid_ver objects
   * @throws IOException
   */
  static public Set<Pref_eventid_ver> readPref_eventid_vers(File inputFile) throws IOException {
    return readPref_eventid_vers(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Pref_eventid_ver objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Pref_eventid_ver objects
   * @throws IOException
   */
  static public Set<Pref_eventid_ver> readPref_eventid_vers(InputStream input) throws IOException {
    return readPref_eventid_vers(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Pref_eventid_ver objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param pref_eventid_vers the Pref_eventid_ver objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Pref_eventid_ver> pref_eventid_vers)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Pref_eventid_ver pref_eventid_ver : pref_eventid_vers)
      pref_eventid_ver.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Pref_eventid_ver objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param pref_eventid_vers the Pref_eventid_ver objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Pref_eventid_ver> pref_eventid_vers, java.util.Date lddate,
      boolean commit) throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?)");
      for (Pref_eventid_ver pref_eventid_ver : pref_eventid_vers) {
        int i = 0;
        statement.setLong(++i, pref_eventid_ver.prefvid);
        statement.setString(++i, pref_eventid_ver.sta);
        statement.setString(++i, pref_eventid_ver.chan);
        statement.setLong(++i, pref_eventid_ver.versionid);
        statement.setString(++i, pref_eventid_ver.pref_tag);
        statement.setString(++i, pref_eventid_ver.auth);
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
   *        Pref_eventid_ver table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Pref_eventid_ver> readPref_eventid_vers(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Pref_eventid_ver> results = new HashSet<Pref_eventid_ver>();
    readPref_eventid_vers(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Pref_eventid_ver table.
   * @param pref_eventid_vers
   * @throws SQLException
   */
  static public void readPref_eventid_vers(Connection connection, String selectStatement,
      Set<Pref_eventid_ver> pref_eventid_vers) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        pref_eventid_vers.add(new Pref_eventid_ver(rs));
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
   * this Pref_eventid_ver object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Pref_eventid_ver object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("prefvid, sta, chan, versionid, pref_tag, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(prefvid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(chan).append("', ");
    sql.append(Long.toString(versionid)).append(", ");
    sql.append("'").append(pref_tag).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Pref_eventid_ver in the database. Primary and unique keys are set, if
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
   * Create a table of type Pref_eventid_ver in the database
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
   * Generate a sql script to create a table of type Pref_eventid_ver in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Pref_eventid_ver in the database
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
    buf.append("prefvid      number(9)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("versionid    number(9)            NOT NULL,\n");
    buf.append("pref_tag     varchar2(60)         NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (prefvid)");
    if (includeUniqueKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_uk unique (sta,chan)");
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
    return 126;
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
    return (other instanceof Pref_eventid_ver) && ((Pref_eventid_ver) other).prefvid == prefvid;
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
    return (other instanceof Pref_eventid_ver) && ((Pref_eventid_ver) other).sta.equals(sta)
        && ((Pref_eventid_ver) other).chan.equals(chan);
  }

  /**
   * Preferred version. This column holds the version identifier (versionid) that points to the
   * preferred version for the set of eventid parameters.
   * 
   * @return prefvid
   */
  public long getPrefvid() {
    return prefvid;
  }

  /**
   * Preferred version. This column holds the version identifier (versionid) that points to the
   * preferred version for the set of eventid parameters.
   * 
   * @param prefvid
   * @throws IllegalArgumentException if prefvid >= 1000000000
   */
  public Pref_eventid_ver setPrefvid(long prefvid) {
    if (prefvid >= 1000000000L)
      throw new IllegalArgumentException("prefvid=" + prefvid + " but cannot be >= 1000000000");
    this.prefvid = prefvid;
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
  public Pref_eventid_ver setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   * 
   * @return chan
   */
  public String getChan() {
    return chan;
  }

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   * 
   * @param chan
   * @throws IllegalArgumentException if chan.length() >= 8
   */
  public Pref_eventid_ver setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
    setHash(null);
    return this;
  }

  /**
   * Unique identifier for version of a set of parameter files.
   * 
   * @return versionid
   */
  public long getVersionid() {
    return versionid;
  }

  /**
   * Unique identifier for version of a set of parameter files.
   * 
   * @param versionid
   * @throws IllegalArgumentException if versionid >= 1000000000
   */
  public Pref_eventid_ver setVersionid(long versionid) {
    if (versionid >= 1000000000L)
      throw new IllegalArgumentException("versionid=" + versionid + " but cannot be >= 1000000000");
    this.versionid = versionid;
    setHash(null);
    return this;
  }

  /**
   * A name that is applied to the particular set of eventid parameters that is currently preferred
   * for use for a given station, channel, phase, and frequency range, e.g. KB7.1. In cases where
   * more than one set of parameters is available, this tag will be used by an application to select
   * the set of parameters to use.
   * 
   * @return pref_tag
   */
  public String getPref_tag() {
    return pref_tag;
  }

  /**
   * A name that is applied to the particular set of eventid parameters that is currently preferred
   * for use for a given station, channel, phase, and frequency range, e.g. KB7.1. In cases where
   * more than one set of parameters is available, this tag will be used by an application to select
   * the set of parameters to use.
   * 
   * @param pref_tag
   * @throws IllegalArgumentException if pref_tag.length() >= 60
   */
  public Pref_eventid_ver setPref_tag(String pref_tag) {
    if (pref_tag.length() > 60)
      throw new IllegalArgumentException(
          String.format("pref_tag.length() cannot be > 60.  pref_tag=%s", pref_tag));
    this.pref_tag = pref_tag;
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
  public Pref_eventid_ver setAuth(String auth) {
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
