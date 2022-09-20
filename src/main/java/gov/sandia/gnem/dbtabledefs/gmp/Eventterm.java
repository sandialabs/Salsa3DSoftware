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
package gov.sandia.gnem.dbtabledefs.gmp;

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
 * ?
 */
public class Eventterm extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * -
   */
  private long eventtermid;

  static final public long EVENTTERMID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long sourceid;

  static final public long SOURCEID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long tomorunid;

  static final public long TOMORUNID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long geomodelid;

  static final public long GEOMODELID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private double eterm;

  static final public double ETERM_NA = Double.NaN;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("eventtermid", Columns.FieldType.LONG, "%d");
    columns.add("sourceid", Columns.FieldType.LONG, "%d");
    columns.add("tomorunid", Columns.FieldType.LONG, "%d");
    columns.add("geomodelid", Columns.FieldType.LONG, "%d");
    columns.add("eterm", Columns.FieldType.DOUBLE, "%22.15e");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Eventterm(long eventtermid, long sourceid, long tomorunid, long geomodelid, double eterm) {
    setValues(eventtermid, sourceid, tomorunid, geomodelid, eterm);
  }

  private void setValues(long eventtermid, long sourceid, long tomorunid, long geomodelid,
      double eterm) {
    this.eventtermid = eventtermid;
    this.sourceid = sourceid;
    this.tomorunid = tomorunid;
    this.geomodelid = geomodelid;
    this.eterm = eterm;
  }

  /**
   * Copy constructor.
   */
  public Eventterm(Eventterm other) {
    this.eventtermid = other.getEventtermid();
    this.sourceid = other.getSourceid();
    this.tomorunid = other.getTomorunid();
    this.geomodelid = other.getGeomodelid();
    this.eterm = other.getEterm();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Eventterm() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(EVENTTERMID_NA, SOURCEID_NA, TOMORUNID_NA, GEOMODELID_NA, ETERM_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
        + " is not a valid input name ...");
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "eterm":
        return eterm;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "eterm":
        eterm = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "eventtermid":
        return eventtermid;
      case "sourceid":
        return sourceid;
      case "tomorunid":
        return tomorunid;
      case "geomodelid":
        return geomodelid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "eventtermid":
        eventtermid = value;
        break;
      case "sourceid":
        sourceid = value;
        break;
      case "tomorunid":
        tomorunid = value;
        break;
      case "geomodelid":
        geomodelid = value;
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
  public Eventterm(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Eventterm(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        input.readDouble());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Eventterm(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getDouble());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Eventterm(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Eventterm(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getDouble(offset + 5));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[5];
    values[0] = eventtermid;
    values[1] = sourceid;
    values[2] = tomorunid;
    values[3] = geomodelid;
    values[4] = eterm;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[6];
    values[0] = eventtermid;
    values[1] = sourceid;
    values[2] = tomorunid;
    values[3] = geomodelid;
    values[4] = eterm;
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
    output.writeLong(eventtermid);
    output.writeLong(sourceid);
    output.writeLong(tomorunid);
    output.writeLong(geomodelid);
    output.writeDouble(eterm);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(eventtermid);
    output.putLong(sourceid);
    output.putLong(tomorunid);
    output.putLong(geomodelid);
    output.putDouble(eterm);
  }

  /**
   * Read a Collection of Eventterm objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Eventterm objects.
   * @throws IOException
   */
  static public void readEventterms(BufferedReader input, Collection<Eventterm> rows)
      throws IOException {
    String[] saved = Eventterm.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Eventterm
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Eventterm(new Scanner(line)));
    }
    input.close();
    Eventterm.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Eventterm objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Eventterm objects.
   * @throws IOException
   */
  static public void readEventterms(File inputFile, Collection<Eventterm> rows) throws IOException {
    readEventterms(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Eventterm objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Eventterm objects.
   * @throws IOException
   */
  static public void readEventterms(InputStream inputStream, Collection<Eventterm> rows)
      throws IOException {
    readEventterms(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Eventterm objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Eventterm objects
   * @throws IOException
   */
  static public Set<Eventterm> readEventterms(BufferedReader input) throws IOException {
    Set<Eventterm> rows = new LinkedHashSet<Eventterm>();
    readEventterms(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Eventterm objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Eventterm objects
   * @throws IOException
   */
  static public Set<Eventterm> readEventterms(File inputFile) throws IOException {
    return readEventterms(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Eventterm objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Eventterm objects
   * @throws IOException
   */
  static public Set<Eventterm> readEventterms(InputStream input) throws IOException {
    return readEventterms(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Eventterm objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param eventterms the Eventterm objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Eventterm> eventterms)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Eventterm eventterm : eventterms)
      eventterm.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Eventterm objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param eventterms the Eventterm objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Eventterm> eventterms, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?)");
      for (Eventterm eventterm : eventterms) {
        int i = 0;
        statement.setLong(++i, eventterm.eventtermid);
        statement.setLong(++i, eventterm.sourceid);
        statement.setLong(++i, eventterm.tomorunid);
        statement.setLong(++i, eventterm.geomodelid);
        statement.setDouble(++i, eventterm.eterm);
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
   *        Eventterm table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Eventterm> readEventterms(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Eventterm> results = new HashSet<Eventterm>();
    readEventterms(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Eventterm table.
   * @param eventterms
   * @throws SQLException
   */
  static public void readEventterms(Connection connection, String selectStatement,
      Set<Eventterm> eventterms) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        eventterms.add(new Eventterm(rs));
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
   * this Eventterm object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Eventterm object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("eventtermid, sourceid, tomorunid, geomodelid, eterm, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(eventtermid)).append(", ");
    sql.append(Long.toString(sourceid)).append(", ");
    sql.append(Long.toString(tomorunid)).append(", ");
    sql.append(Long.toString(geomodelid)).append(", ");
    sql.append(Double.toString(eterm)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Eventterm in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Eventterm in the database
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
   * Generate a sql script to create a table of type Eventterm in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Eventterm in the database
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
    buf.append("eventtermid  number(10)           NOT NULL,\n");
    buf.append("sourceid     number(10)           NOT NULL,\n");
    buf.append("tomorunid    number(10)           NOT NULL,\n");
    buf.append("geomodelid   number(10)           NOT NULL,\n");
    buf.append("eterm        float(126)           NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (eventtermid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (sourceid,tomorunid,geomodelid)");
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
    return 40;
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
    return (other instanceof Eventterm) && ((Eventterm) other).eventtermid == eventtermid;
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
    return (other instanceof Eventterm) && ((Eventterm) other).sourceid == sourceid
        && ((Eventterm) other).tomorunid == tomorunid
        && ((Eventterm) other).geomodelid == geomodelid;
  }

  /**
   * -
   * 
   * @return eventtermid
   */
  public long getEventtermid() {
    return eventtermid;
  }

  /**
   * -
   * 
   * @param eventtermid
   * @throws IllegalArgumentException if eventtermid >= 10000000000
   */
  public Eventterm setEventtermid(long eventtermid) {
    if (eventtermid >= 10000000000L)
      throw new IllegalArgumentException(
          "eventtermid=" + eventtermid + " but cannot be >= 10000000000");
    this.eventtermid = eventtermid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return sourceid
   */
  public long getSourceid() {
    return sourceid;
  }

  /**
   * -
   * 
   * @param sourceid
   * @throws IllegalArgumentException if sourceid >= 10000000000
   */
  public Eventterm setSourceid(long sourceid) {
    if (sourceid >= 10000000000L)
      throw new IllegalArgumentException("sourceid=" + sourceid + " but cannot be >= 10000000000");
    this.sourceid = sourceid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return tomorunid
   */
  public long getTomorunid() {
    return tomorunid;
  }

  /**
   * -
   * 
   * @param tomorunid
   * @throws IllegalArgumentException if tomorunid >= 10000000000
   */
  public Eventterm setTomorunid(long tomorunid) {
    if (tomorunid >= 10000000000L)
      throw new IllegalArgumentException(
          "tomorunid=" + tomorunid + " but cannot be >= 10000000000");
    this.tomorunid = tomorunid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return geomodelid
   */
  public long getGeomodelid() {
    return geomodelid;
  }

  /**
   * -
   * 
   * @param geomodelid
   * @throws IllegalArgumentException if geomodelid >= 10000000000
   */
  public Eventterm setGeomodelid(long geomodelid) {
    if (geomodelid >= 10000000000L)
      throw new IllegalArgumentException(
          "geomodelid=" + geomodelid + " but cannot be >= 10000000000");
    this.geomodelid = geomodelid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return eterm
   */
  public double getEterm() {
    return eterm;
  }

  /**
   * -
   * 
   * @param eterm
   */
  public Eventterm setEterm(double eterm) {
    this.eterm = eterm;
    setHash(null);
    return this;
  }

  /**
   * Retrieve the name of the schema.
   * 
   * @return schema name
   */
  static public String getSchemaName() {
    return "GMP";
  }

}
