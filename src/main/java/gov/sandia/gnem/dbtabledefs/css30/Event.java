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
 * event
 */
public class Event extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Event identifier. Each event is assigned a unique positive integer which identifies it in a
   * database. It is possible for several records in the origin relation to have the same evid. This
   * indicates there are several opinions about the location of the event.
   */
  private long evid;

  static final public long EVID_NA = Long.MIN_VALUE;

  /**
   * Event name. This is the common name of the event identified by evid.
   */
  private String evname;

  static final public String EVNAME_NA = "-";

  /**
   * Preferred origin. This attribute holds the origin identifier, orid, that points to the
   * preferred origin for a seismic event.
   */
  private long prefor;

  static final public long PREFOR_NA = Long.MIN_VALUE;

  /**
   * Author. This records the originator of an arrival (in arrival relation) or origin (in origin
   * relation). Possibilities include externally supplied arrivals identified according to their
   * original source, such as WMO, NEIS, CAN(adian), UK(array), etc. This may also be an identifier
   * of an application generating the attribute, such as an automated interpretation or
   * signal-processing program.
   */
  private String auth;

  static final public String AUTH_NA = "-";

  /**
   * Comment identification. This is a key used to point to free-form comments entered in the remark
   * relation. These comments store additional information about a tuple in another relation. Within
   * the remark relation, there may be many tuples with the same commid and different lineno, but
   * the same commid will appear in only one other tuple among the rest of the relations in the
   * database. See lineno.
   */
  private long commid;

  static final public long COMMID_NA = -1;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("evid", Columns.FieldType.LONG, "%d");
    columns.add("evname", Columns.FieldType.STRING, "%s");
    columns.add("prefor", Columns.FieldType.LONG, "%d");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Event(long evid, String evname, long prefor, String auth, long commid) {
    setValues(evid, evname, prefor, auth, commid);
  }

  private void setValues(long evid, String evname, long prefor, String auth, long commid) {
    this.evid = evid;
    this.evname = evname;
    this.prefor = prefor;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Event(Event other) {
    this.evid = other.getEvid();
    this.evname = other.getEvname();
    this.prefor = other.getPrefor();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Event() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(EVID_NA, EVNAME_NA, PREFOR_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "evname":
        return evname;
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
      case "evname":
        evname = value;
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
      case "evid":
        return evid;
      case "prefor":
        return prefor;
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
      case "evid":
        evid = value;
        break;
      case "prefor":
        prefor = value;
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
  public Event(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Event(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), input.readLong(), readString(input),
        input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Event(ByteBuffer input) {
    this(input.getLong(), readString(input), input.getLong(), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Event(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Event(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getLong(offset + 3),
        input.getString(offset + 4), input.getLong(offset + 5));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[5];
    values[0] = evid;
    values[1] = evname;
    values[2] = prefor;
    values[3] = auth;
    values[4] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[6];
    values[0] = evid;
    values[1] = evname;
    values[2] = prefor;
    values[3] = auth;
    values[4] = commid;
    values[5] = lddate;
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
    output.writeLong(evid);
    writeString(output, evname);
    output.writeLong(prefor);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(evid);
    writeString(output, evname);
    output.putLong(prefor);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Event objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Event objects.
   * @throws IOException
   */
  static public void readEvents(BufferedReader input, Collection<Event> rows) throws IOException {
    String[] saved = Event.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Event.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Event(new Scanner(line)));
    }
    input.close();
    Event.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Event objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Event objects.
   * @throws IOException
   */
  static public void readEvents(File inputFile, Collection<Event> rows) throws IOException {
    readEvents(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Event objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Event objects.
   * @throws IOException
   */
  static public void readEvents(InputStream inputStream, Collection<Event> rows)
      throws IOException {
    readEvents(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Event objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Event objects
   * @throws IOException
   */
  static public Set<Event> readEvents(BufferedReader input) throws IOException {
    Set<Event> rows = new LinkedHashSet<Event>();
    readEvents(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Event objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Event objects
   * @throws IOException
   */
  static public Set<Event> readEvents(File inputFile) throws IOException {
    return readEvents(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Event objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Event objects
   * @throws IOException
   */
  static public Set<Event> readEvents(InputStream input) throws IOException {
    return readEvents(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Event objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param events the Event objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Event> events) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Event event : events)
      event.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Event objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param events the Event objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Event> events, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?)");
      for (Event event : events) {
        int i = 0;
        statement.setLong(++i, event.evid);
        statement.setString(++i, event.evname);
        statement.setLong(++i, event.prefor);
        statement.setString(++i, event.auth);
        statement.setLong(++i, event.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Event
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Event> readEvents(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Event> results = new HashSet<Event>();
    readEvents(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Event
   *        table.
   * @param events
   * @throws SQLException
   */
  static public void readEvents(Connection connection, String selectStatement, Set<Event> events)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        events.add(new Event(rs));
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
   * this Event object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Event object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("evid, evname, prefor, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(evid)).append(", ");
    sql.append("'").append(evname).append("', ");
    sql.append(Long.toString(prefor)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Event in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Event in the database
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
   * Generate a sql script to create a table of type Event in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Event in the database
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
    buf.append("evid         number(8)            NOT NULL,\n");
    buf.append("evname       varchar2(15)         NOT NULL,\n");
    buf.append("prefor       number(8)            NOT NULL,\n");
    buf.append("auth         varchar2(15)         NOT NULL,\n");
    buf.append("commid       number(8)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (evid)");
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
    return 62;
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
    return (other instanceof Event) && ((Event) other).evid == evid;
  }

  /**
   * Event identifier. Each event is assigned a unique positive integer which identifies it in a
   * database. It is possible for several records in the origin relation to have the same evid. This
   * indicates there are several opinions about the location of the event.
   * 
   * @return evid
   */
  public long getEvid() {
    return evid;
  }

  /**
   * Event identifier. Each event is assigned a unique positive integer which identifies it in a
   * database. It is possible for several records in the origin relation to have the same evid. This
   * indicates there are several opinions about the location of the event.
   * 
   * @param evid
   * @throws IllegalArgumentException if evid >= 100000000
   */
  public Event setEvid(long evid) {
    if (evid >= 100000000L)
      throw new IllegalArgumentException("evid=" + evid + " but cannot be >= 100000000");
    this.evid = evid;
    setHash(null);
    return this;
  }

  /**
   * Event name. This is the common name of the event identified by evid.
   * 
   * @return evname
   */
  public String getEvname() {
    return evname;
  }

  /**
   * Event name. This is the common name of the event identified by evid.
   * 
   * @param evname
   * @throws IllegalArgumentException if evname.length() >= 15
   */
  public Event setEvname(String evname) {
    if (evname.length() > 15)
      throw new IllegalArgumentException(
          String.format("evname.length() cannot be > 15.  evname=%s", evname));
    this.evname = evname;
    setHash(null);
    return this;
  }

  /**
   * Preferred origin. This attribute holds the origin identifier, orid, that points to the
   * preferred origin for a seismic event.
   * 
   * @return prefor
   */
  public long getPrefor() {
    return prefor;
  }

  /**
   * Preferred origin. This attribute holds the origin identifier, orid, that points to the
   * preferred origin for a seismic event.
   * 
   * @param prefor
   * @throws IllegalArgumentException if prefor >= 100000000
   */
  public Event setPrefor(long prefor) {
    if (prefor >= 100000000L)
      throw new IllegalArgumentException("prefor=" + prefor + " but cannot be >= 100000000");
    this.prefor = prefor;
    setHash(null);
    return this;
  }

  /**
   * Author. This records the originator of an arrival (in arrival relation) or origin (in origin
   * relation). Possibilities include externally supplied arrivals identified according to their
   * original source, such as WMO, NEIS, CAN(adian), UK(array), etc. This may also be an identifier
   * of an application generating the attribute, such as an automated interpretation or
   * signal-processing program.
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * Author. This records the originator of an arrival (in arrival relation) or origin (in origin
   * relation). Possibilities include externally supplied arrivals identified according to their
   * original source, such as WMO, NEIS, CAN(adian), UK(array), etc. This may also be an identifier
   * of an application generating the attribute, such as an automated interpretation or
   * signal-processing program.
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 15
   */
  public Event setAuth(String auth) {
    if (auth.length() > 15)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 15.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * Comment identification. This is a key used to point to free-form comments entered in the remark
   * relation. These comments store additional information about a tuple in another relation. Within
   * the remark relation, there may be many tuples with the same commid and different lineno, but
   * the same commid will appear in only one other tuple among the rest of the relations in the
   * database. See lineno.
   * 
   * @return commid
   */
  public long getCommid() {
    return commid;
  }

  /**
   * Comment identification. This is a key used to point to free-form comments entered in the remark
   * relation. These comments store additional information about a tuple in another relation. Within
   * the remark relation, there may be many tuples with the same commid and different lineno, but
   * the same commid will appear in only one other tuple among the rest of the relations in the
   * database. See lineno.
   * 
   * @param commid
   * @throws IllegalArgumentException if commid >= 100000000
   */
  public Event setCommid(long commid) {
    if (commid >= 100000000L)
      throw new IllegalArgumentException("commid=" + commid + " but cannot be >= 100000000");
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
    return "CSS3.0";
  }

}
