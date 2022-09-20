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
 * csd_2d_corr
 */
public class Csd_2d_corr extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Coda shape decay 2d identifier
   */
  private long b2dcorid;

  static final public long B2DCORID_NA = Long.MIN_VALUE;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("b2dcorid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Csd_2d_corr(long b2dcorid) {
    setValues(b2dcorid);
  }

  private void setValues(long b2dcorid) {
    this.b2dcorid = b2dcorid;
  }

  /**
   * Copy constructor.
   */
  public Csd_2d_corr(Csd_2d_corr other) {
    this.b2dcorid = other.getB2dcorid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Csd_2d_corr() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(B2DCORID_NA);
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
      case "b2dcorid":
        return b2dcorid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "b2dcorid":
        b2dcorid = value;
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
  public Csd_2d_corr(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Csd_2d_corr(DataInputStream input) throws IOException {
    this(input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Csd_2d_corr(ByteBuffer input) {
    this(input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Csd_2d_corr(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Csd_2d_corr(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[1];
    values[0] = b2dcorid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[2];
    values[0] = b2dcorid;
    values[1] = lddate;
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
    output.writeLong(b2dcorid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(b2dcorid);
  }

  /**
   * Read a Collection of Csd_2d_corr objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Csd_2d_corr objects.
   * @throws IOException
   */
  static public void readCsd_2d_corrs(BufferedReader input, Collection<Csd_2d_corr> rows)
      throws IOException {
    String[] saved = Csd_2d_corr.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Csd_2d_corr
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Csd_2d_corr(new Scanner(line)));
    }
    input.close();
    Csd_2d_corr.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Csd_2d_corr objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Csd_2d_corr objects.
   * @throws IOException
   */
  static public void readCsd_2d_corrs(File inputFile, Collection<Csd_2d_corr> rows)
      throws IOException {
    readCsd_2d_corrs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Csd_2d_corr objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Csd_2d_corr objects.
   * @throws IOException
   */
  static public void readCsd_2d_corrs(InputStream inputStream, Collection<Csd_2d_corr> rows)
      throws IOException {
    readCsd_2d_corrs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Csd_2d_corr objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Csd_2d_corr objects
   * @throws IOException
   */
  static public Set<Csd_2d_corr> readCsd_2d_corrs(BufferedReader input) throws IOException {
    Set<Csd_2d_corr> rows = new LinkedHashSet<Csd_2d_corr>();
    readCsd_2d_corrs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Csd_2d_corr objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Csd_2d_corr objects
   * @throws IOException
   */
  static public Set<Csd_2d_corr> readCsd_2d_corrs(File inputFile) throws IOException {
    return readCsd_2d_corrs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Csd_2d_corr objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Csd_2d_corr objects
   * @throws IOException
   */
  static public Set<Csd_2d_corr> readCsd_2d_corrs(InputStream input) throws IOException {
    return readCsd_2d_corrs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Csd_2d_corr objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param csd_2d_corrs the Csd_2d_corr objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Csd_2d_corr> csd_2d_corrs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Csd_2d_corr csd_2d_corr : csd_2d_corrs)
      csd_2d_corr.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Csd_2d_corr objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param csd_2d_corrs the Csd_2d_corr objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Csd_2d_corr> csd_2d_corrs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?)");
      for (Csd_2d_corr csd_2d_corr : csd_2d_corrs) {
        int i = 0;
        statement.setLong(++i, csd_2d_corr.b2dcorid);
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
   *        Csd_2d_corr table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Csd_2d_corr> readCsd_2d_corrs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Csd_2d_corr> results = new HashSet<Csd_2d_corr>();
    readCsd_2d_corrs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Csd_2d_corr table.
   * @param csd_2d_corrs
   * @throws SQLException
   */
  static public void readCsd_2d_corrs(Connection connection, String selectStatement,
      Set<Csd_2d_corr> csd_2d_corrs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        csd_2d_corrs.add(new Csd_2d_corr(rs));
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
   * this Csd_2d_corr object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Csd_2d_corr object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("b2dcorid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(b2dcorid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Csd_2d_corr in the database. Primary and unique keys are set, if
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
   * Create a table of type Csd_2d_corr in the database
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
   * Generate a sql script to create a table of type Csd_2d_corr in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Csd_2d_corr in the database
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
    buf.append("b2dcorid     number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (b2dcorid)");
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
    return 8;
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
    return (other instanceof Csd_2d_corr) && ((Csd_2d_corr) other).b2dcorid == b2dcorid;
  }

  /**
   * Coda shape decay 2d identifier
   * 
   * @return b2dcorid
   */
  public long getB2dcorid() {
    return b2dcorid;
  }

  /**
   * Coda shape decay 2d identifier
   * 
   * @param b2dcorid
   * @throws IllegalArgumentException if b2dcorid >= 1000000000
   */
  public Csd_2d_corr setB2dcorid(long b2dcorid) {
    if (b2dcorid >= 1000000000L)
      throw new IllegalArgumentException("b2dcorid=" + b2dcorid + " but cannot be >= 1000000000");
    this.b2dcorid = b2dcorid;
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
