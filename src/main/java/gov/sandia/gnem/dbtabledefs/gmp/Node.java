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
public class Node extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * -
   */
  private long nodeid;

  static final public long NODEID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private double lat;

  static final public double LAT_NA = Double.NaN;

  /**
   * -
   */
  private double lon;

  static final public double LON_NA = Double.NaN;

  /**
   * -
   */
  private double depth;

  static final public double DEPTH_NA = -999.0;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("nodeid", Columns.FieldType.LONG, "%d");
    columns.add("lat", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("lon", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("depth", Columns.FieldType.DOUBLE, "%22.15e");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Node(long nodeid, double lat, double lon, double depth) {
    setValues(nodeid, lat, lon, depth);
  }

  private void setValues(long nodeid, double lat, double lon, double depth) {
    this.nodeid = nodeid;
    this.lat = lat;
    this.lon = lon;
    this.depth = depth;
  }

  /**
   * Copy constructor.
   */
  public Node(Node other) {
    this.nodeid = other.getNodeid();
    this.lat = other.getLat();
    this.lon = other.getLon();
    this.depth = other.getDepth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Node() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(NODEID_NA, LAT_NA, LON_NA, DEPTH_NA);
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
      case "lat":
        return lat;
      case "lon":
        return lon;
      case "depth":
        return depth;
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
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "nodeid":
        return nodeid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "nodeid":
        nodeid = value;
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
  public Node(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Node(DataInputStream input) throws IOException {
    this(input.readLong(), input.readDouble(), input.readDouble(), input.readDouble());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Node(ByteBuffer input) {
    this(input.getLong(), input.getDouble(), input.getDouble(), input.getDouble());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Node(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Node(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[4];
    values[0] = nodeid;
    values[1] = lat;
    values[2] = lon;
    values[3] = depth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[5];
    values[0] = nodeid;
    values[1] = lat;
    values[2] = lon;
    values[3] = depth;
    values[4] = lddate;
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
    output.writeLong(nodeid);
    output.writeDouble(lat);
    output.writeDouble(lon);
    output.writeDouble(depth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(nodeid);
    output.putDouble(lat);
    output.putDouble(lon);
    output.putDouble(depth);
  }

  /**
   * Read a Collection of Node objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Node objects.
   * @throws IOException
   */
  static public void readNodes(BufferedReader input, Collection<Node> rows) throws IOException {
    String[] saved = Node.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Node.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Node(new Scanner(line)));
    }
    input.close();
    Node.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Node objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Node objects.
   * @throws IOException
   */
  static public void readNodes(File inputFile, Collection<Node> rows) throws IOException {
    readNodes(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Node objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Node objects.
   * @throws IOException
   */
  static public void readNodes(InputStream inputStream, Collection<Node> rows) throws IOException {
    readNodes(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Node objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Node objects
   * @throws IOException
   */
  static public Set<Node> readNodes(BufferedReader input) throws IOException {
    Set<Node> rows = new LinkedHashSet<Node>();
    readNodes(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Node objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Node objects
   * @throws IOException
   */
  static public Set<Node> readNodes(File inputFile) throws IOException {
    return readNodes(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Node objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Node objects
   * @throws IOException
   */
  static public Set<Node> readNodes(InputStream input) throws IOException {
    return readNodes(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Node objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param nodes the Node objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Node> nodes) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Node node : nodes)
      node.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Node objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param nodes the Node objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Node> nodes, java.util.Date lddate, boolean commit) throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?)");
      for (Node node : nodes) {
        int i = 0;
        statement.setLong(++i, node.nodeid);
        statement.setDouble(++i, node.lat);
        statement.setDouble(++i, node.lon);
        statement.setDouble(++i, node.depth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Node
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Node> readNodes(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Node> results = new HashSet<Node>();
    readNodes(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Node
   *        table.
   * @param nodes
   * @throws SQLException
   */
  static public void readNodes(Connection connection, String selectStatement, Set<Node> nodes)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        nodes.add(new Node(rs));
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
   * this Node object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Node object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("nodeid, lat, lon, depth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(nodeid)).append(", ");
    sql.append(Double.toString(lat)).append(", ");
    sql.append(Double.toString(lon)).append(", ");
    sql.append(Double.toString(depth)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Node in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Node in the database
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
   * Generate a sql script to create a table of type Node in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Node in the database
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
    buf.append("nodeid       number(10)           NOT NULL,\n");
    buf.append("lat          float(126)           NOT NULL,\n");
    buf.append("lon          float(126)           NOT NULL,\n");
    buf.append("depth        float(126)           NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (nodeid)");
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
    return 32;
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
    return (other instanceof Node) && ((Node) other).nodeid == nodeid;
  }

  /**
   * -
   * 
   * @return nodeid
   */
  public long getNodeid() {
    return nodeid;
  }

  /**
   * -
   * 
   * @param nodeid
   * @throws IllegalArgumentException if nodeid >= 10000000000
   */
  public Node setNodeid(long nodeid) {
    if (nodeid >= 10000000000L)
      throw new IllegalArgumentException("nodeid=" + nodeid + " but cannot be >= 10000000000");
    this.nodeid = nodeid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return lat
   */
  public double getLat() {
    return lat;
  }

  /**
   * -
   * 
   * @param lat
   */
  public Node setLat(double lat) {
    this.lat = lat;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return lon
   */
  public double getLon() {
    return lon;
  }

  /**
   * -
   * 
   * @param lon
   */
  public Node setLon(double lon) {
    this.lon = lon;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return depth
   */
  public double getDepth() {
    return depth;
  }

  /**
   * -
   * 
   * @param depth
   */
  public Node setDepth(double depth) {
    this.depth = depth;
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
