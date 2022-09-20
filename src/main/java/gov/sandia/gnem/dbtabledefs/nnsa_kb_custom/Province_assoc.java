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
 * province_assoc
 */
public class Province_assoc extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Cell number
   */
  private long cellnum;

  static final public long CELLNUM_NA = Long.MIN_VALUE;

  /**
   * Unique province identifier used for velocity models.
   */
  private long provid;

  static final public long PROVID_NA = Long.MIN_VALUE;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("cellnum", Columns.FieldType.LONG, "%d");
    columns.add("provid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Province_assoc(long cellnum, long provid) {
    setValues(cellnum, provid);
  }

  private void setValues(long cellnum, long provid) {
    this.cellnum = cellnum;
    this.provid = provid;
  }

  /**
   * Copy constructor.
   */
  public Province_assoc(Province_assoc other) {
    this.cellnum = other.getCellnum();
    this.provid = other.getProvid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Province_assoc() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(CELLNUM_NA, PROVID_NA);
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
      case "cellnum":
        return cellnum;
      case "provid":
        return provid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "cellnum":
        cellnum = value;
        break;
      case "provid":
        provid = value;
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
  public Province_assoc(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Province_assoc(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Province_assoc(ByteBuffer input) {
    this(input.getLong(), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Province_assoc(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Province_assoc(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[2];
    values[0] = cellnum;
    values[1] = provid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[3];
    values[0] = cellnum;
    values[1] = provid;
    values[2] = lddate;
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
    output.writeLong(cellnum);
    output.writeLong(provid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(cellnum);
    output.putLong(provid);
  }

  /**
   * Read a Collection of Province_assoc objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Province_assoc objects.
   * @throws IOException
   */
  static public void readProvince_assocs(BufferedReader input, Collection<Province_assoc> rows)
      throws IOException {
    String[] saved = Province_assoc.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Province_assoc
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Province_assoc(new Scanner(line)));
    }
    input.close();
    Province_assoc.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Province_assoc objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Province_assoc objects.
   * @throws IOException
   */
  static public void readProvince_assocs(File inputFile, Collection<Province_assoc> rows)
      throws IOException {
    readProvince_assocs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Province_assoc objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Province_assoc objects.
   * @throws IOException
   */
  static public void readProvince_assocs(InputStream inputStream, Collection<Province_assoc> rows)
      throws IOException {
    readProvince_assocs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Province_assoc objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Province_assoc objects
   * @throws IOException
   */
  static public Set<Province_assoc> readProvince_assocs(BufferedReader input) throws IOException {
    Set<Province_assoc> rows = new LinkedHashSet<Province_assoc>();
    readProvince_assocs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Province_assoc objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Province_assoc objects
   * @throws IOException
   */
  static public Set<Province_assoc> readProvince_assocs(File inputFile) throws IOException {
    return readProvince_assocs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Province_assoc objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Province_assoc objects
   * @throws IOException
   */
  static public Set<Province_assoc> readProvince_assocs(InputStream input) throws IOException {
    return readProvince_assocs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Province_assoc objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param province_assocs the Province_assoc objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Province_assoc> province_assocs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Province_assoc province_assoc : province_assocs)
      province_assoc.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Province_assoc objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param province_assocs the Province_assoc objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Province_assoc> province_assocs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?)");
      for (Province_assoc province_assoc : province_assocs) {
        int i = 0;
        statement.setLong(++i, province_assoc.cellnum);
        statement.setLong(++i, province_assoc.provid);
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
   *        Province_assoc table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Province_assoc> readProvince_assocs(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Province_assoc> results = new HashSet<Province_assoc>();
    readProvince_assocs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Province_assoc table.
   * @param province_assocs
   * @throws SQLException
   */
  static public void readProvince_assocs(Connection connection, String selectStatement,
      Set<Province_assoc> province_assocs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        province_assocs.add(new Province_assoc(rs));
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
   * this Province_assoc object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Province_assoc object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("cellnum, provid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(cellnum)).append(", ");
    sql.append(Long.toString(provid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Province_assoc in the database. Primary and unique keys are set, if
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
   * Create a table of type Province_assoc in the database
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
   * Generate a sql script to create a table of type Province_assoc in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Province_assoc in the database
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
    buf.append("cellnum      number(8)            NOT NULL,\n");
    buf.append("provid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (cellnum)");
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
    return 16;
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
    return (other instanceof Province_assoc) && ((Province_assoc) other).cellnum == cellnum;
  }

  /**
   * Cell number
   * 
   * @return cellnum
   */
  public long getCellnum() {
    return cellnum;
  }

  /**
   * Cell number
   * 
   * @param cellnum
   * @throws IllegalArgumentException if cellnum >= 100000000
   */
  public Province_assoc setCellnum(long cellnum) {
    if (cellnum >= 100000000L)
      throw new IllegalArgumentException("cellnum=" + cellnum + " but cannot be >= 100000000");
    this.cellnum = cellnum;
    setHash(null);
    return this;
  }

  /**
   * Unique province identifier used for velocity models.
   * 
   * @return provid
   */
  public long getProvid() {
    return provid;
  }

  /**
   * Unique province identifier used for velocity models.
   * 
   * @param provid
   * @throws IllegalArgumentException if provid >= 1000000000
   */
  public Province_assoc setProvid(long provid) {
    if (provid >= 1000000000L)
      throw new IllegalArgumentException("provid=" + provid + " but cannot be >= 1000000000");
    this.provid = provid;
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
