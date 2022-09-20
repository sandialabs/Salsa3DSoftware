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
public class Cell extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * -
   */
  private long cellid;

  static final public long CELLID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long geomodelid;

  static final public long GEOMODELID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long node0;

  static final public long NODE0_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long node1;

  static final public long NODE1_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long node2;

  static final public long NODE2_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long node3;

  static final public long NODE3_NA = -1;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("cellid", Columns.FieldType.LONG, "%d");
    columns.add("geomodelid", Columns.FieldType.LONG, "%d");
    columns.add("node0", Columns.FieldType.LONG, "%d");
    columns.add("node1", Columns.FieldType.LONG, "%d");
    columns.add("node2", Columns.FieldType.LONG, "%d");
    columns.add("node3", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Cell(long cellid, long geomodelid, long node0, long node1, long node2, long node3) {
    setValues(cellid, geomodelid, node0, node1, node2, node3);
  }

  private void setValues(long cellid, long geomodelid, long node0, long node1, long node2,
      long node3) {
    this.cellid = cellid;
    this.geomodelid = geomodelid;
    this.node0 = node0;
    this.node1 = node1;
    this.node2 = node2;
    this.node3 = node3;
  }

  /**
   * Copy constructor.
   */
  public Cell(Cell other) {
    this.cellid = other.getCellid();
    this.geomodelid = other.getGeomodelid();
    this.node0 = other.getNode0();
    this.node1 = other.getNode1();
    this.node2 = other.getNode2();
    this.node3 = other.getNode3();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Cell() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(CELLID_NA, GEOMODELID_NA, NODE0_NA, NODE1_NA, NODE2_NA, NODE3_NA);
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
      case "cellid":
        return cellid;
      case "geomodelid":
        return geomodelid;
      case "node0":
        return node0;
      case "node1":
        return node1;
      case "node2":
        return node2;
      case "node3":
        return node3;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "cellid":
        cellid = value;
        break;
      case "geomodelid":
        geomodelid = value;
        break;
      case "node0":
        node0 = value;
        break;
      case "node1":
        node1 = value;
        break;
      case "node2":
        node2 = value;
        break;
      case "node3":
        node3 = value;
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
  public Cell(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Cell(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Cell(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getLong(),
        input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Cell(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Cell(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[6];
    values[0] = cellid;
    values[1] = geomodelid;
    values[2] = node0;
    values[3] = node1;
    values[4] = node2;
    values[5] = node3;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[7];
    values[0] = cellid;
    values[1] = geomodelid;
    values[2] = node0;
    values[3] = node1;
    values[4] = node2;
    values[5] = node3;
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
    output.writeLong(cellid);
    output.writeLong(geomodelid);
    output.writeLong(node0);
    output.writeLong(node1);
    output.writeLong(node2);
    output.writeLong(node3);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(cellid);
    output.putLong(geomodelid);
    output.putLong(node0);
    output.putLong(node1);
    output.putLong(node2);
    output.putLong(node3);
  }

  /**
   * Read a Collection of Cell objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Cell objects.
   * @throws IOException
   */
  static public void readCells(BufferedReader input, Collection<Cell> rows) throws IOException {
    String[] saved = Cell.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Cell.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Cell(new Scanner(line)));
    }
    input.close();
    Cell.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Cell objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Cell objects.
   * @throws IOException
   */
  static public void readCells(File inputFile, Collection<Cell> rows) throws IOException {
    readCells(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Cell objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Cell objects.
   * @throws IOException
   */
  static public void readCells(InputStream inputStream, Collection<Cell> rows) throws IOException {
    readCells(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Cell objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Cell objects
   * @throws IOException
   */
  static public Set<Cell> readCells(BufferedReader input) throws IOException {
    Set<Cell> rows = new LinkedHashSet<Cell>();
    readCells(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Cell objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Cell objects
   * @throws IOException
   */
  static public Set<Cell> readCells(File inputFile) throws IOException {
    return readCells(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Cell objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Cell objects
   * @throws IOException
   */
  static public Set<Cell> readCells(InputStream input) throws IOException {
    return readCells(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Cell objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param cells the Cell objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Cell> cells) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Cell cell : cells)
      cell.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Cell objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param cells the Cell objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Cell> cells, java.util.Date lddate, boolean commit) throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?)");
      for (Cell cell : cells) {
        int i = 0;
        statement.setLong(++i, cell.cellid);
        statement.setLong(++i, cell.geomodelid);
        statement.setLong(++i, cell.node0);
        statement.setLong(++i, cell.node1);
        statement.setLong(++i, cell.node2);
        statement.setLong(++i, cell.node3);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Cell
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Cell> readCells(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Cell> results = new HashSet<Cell>();
    readCells(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Cell
   *        table.
   * @param cells
   * @throws SQLException
   */
  static public void readCells(Connection connection, String selectStatement, Set<Cell> cells)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        cells.add(new Cell(rs));
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
   * this Cell object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Cell object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("cellid, geomodelid, node0, node1, node2, node3, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(cellid)).append(", ");
    sql.append(Long.toString(geomodelid)).append(", ");
    sql.append(Long.toString(node0)).append(", ");
    sql.append(Long.toString(node1)).append(", ");
    sql.append(Long.toString(node2)).append(", ");
    sql.append(Long.toString(node3)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Cell in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Cell in the database
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
   * Generate a sql script to create a table of type Cell in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Cell in the database
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
    buf.append("cellid       number(10)           NOT NULL,\n");
    buf.append("geomodelid   number(10)           NOT NULL,\n");
    buf.append("node0        number(10)           NOT NULL,\n");
    buf.append("node1        number(10)           NOT NULL,\n");
    buf.append("node2        number(10)           NOT NULL,\n");
    buf.append("node3        number(10)           NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (cellid)");
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
    return 48;
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
    return (other instanceof Cell) && ((Cell) other).cellid == cellid;
  }

  /**
   * -
   * 
   * @return cellid
   */
  public long getCellid() {
    return cellid;
  }

  /**
   * -
   * 
   * @param cellid
   * @throws IllegalArgumentException if cellid >= 10000000000
   */
  public Cell setCellid(long cellid) {
    if (cellid >= 10000000000L)
      throw new IllegalArgumentException("cellid=" + cellid + " but cannot be >= 10000000000");
    this.cellid = cellid;
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
  public Cell setGeomodelid(long geomodelid) {
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
   * @return node0
   */
  public long getNode0() {
    return node0;
  }

  /**
   * -
   * 
   * @param node0
   * @throws IllegalArgumentException if node0 >= 10000000000
   */
  public Cell setNode0(long node0) {
    if (node0 >= 10000000000L)
      throw new IllegalArgumentException("node0=" + node0 + " but cannot be >= 10000000000");
    this.node0 = node0;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return node1
   */
  public long getNode1() {
    return node1;
  }

  /**
   * -
   * 
   * @param node1
   * @throws IllegalArgumentException if node1 >= 10000000000
   */
  public Cell setNode1(long node1) {
    if (node1 >= 10000000000L)
      throw new IllegalArgumentException("node1=" + node1 + " but cannot be >= 10000000000");
    this.node1 = node1;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return node2
   */
  public long getNode2() {
    return node2;
  }

  /**
   * -
   * 
   * @param node2
   * @throws IllegalArgumentException if node2 >= 10000000000
   */
  public Cell setNode2(long node2) {
    if (node2 >= 10000000000L)
      throw new IllegalArgumentException("node2=" + node2 + " but cannot be >= 10000000000");
    this.node2 = node2;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return node3
   */
  public long getNode3() {
    return node3;
  }

  /**
   * -
   * 
   * @param node3
   * @throws IllegalArgumentException if node3 >= 10000000000
   */
  public Cell setNode3(long node3) {
    if (node3 >= 10000000000L)
      throw new IllegalArgumentException("node3=" + node3 + " but cannot be >= 10000000000");
    this.node3 = node3;
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
