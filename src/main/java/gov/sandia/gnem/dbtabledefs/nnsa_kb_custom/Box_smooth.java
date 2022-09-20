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
 * box_smooth
 */
public class Box_smooth extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Boxcar smoothing identfier.
   */
  private long smooid;

  static final public long SMOOID_NA = Long.MIN_VALUE;

  /**
   * The central tendency method (mean, median, mode, p-norm).
   */
  private String midtype;

  static final public String MIDTYPE_NA = null;

  /**
   * Smoother half width.
   * <p>
   * Units: s
   */
  private double hwide;

  static final public double HWIDE_NA = Double.NaN;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;

  /**
   * Unique comment identifier that points to free form comments in the <B> remark_master</B> or
   * <B>remark</B> tables. These comments store additional information about a record in another
   * table.
   */
  private long commid;

  static final public long COMMID_NA = -1;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("smooid", Columns.FieldType.LONG, "%d");
    columns.add("midtype", Columns.FieldType.STRING, "%s");
    columns.add("hwide", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Box_smooth(long smooid, String midtype, double hwide, String auth, long commid) {
    setValues(smooid, midtype, hwide, auth, commid);
  }

  private void setValues(long smooid, String midtype, double hwide, String auth, long commid) {
    this.smooid = smooid;
    this.midtype = midtype;
    this.hwide = hwide;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Box_smooth(Box_smooth other) {
    this.smooid = other.getSmooid();
    this.midtype = other.getMidtype();
    this.hwide = other.getHwide();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Box_smooth() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(SMOOID_NA, MIDTYPE_NA, HWIDE_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "midtype":
        return midtype;
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
      case "midtype":
        midtype = value;
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
      case "hwide":
        return hwide;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "hwide":
        hwide = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "smooid":
        return smooid;
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
      case "smooid":
        smooid = value;
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
  public Box_smooth(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Box_smooth(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), input.readDouble(), readString(input),
        input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Box_smooth(ByteBuffer input) {
    this(input.getLong(), readString(input), input.getDouble(), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Box_smooth(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Box_smooth(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getDouble(offset + 3),
        input.getString(offset + 4), input.getLong(offset + 5));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[5];
    values[0] = smooid;
    values[1] = midtype;
    values[2] = hwide;
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
    values[0] = smooid;
    values[1] = midtype;
    values[2] = hwide;
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
    output.writeLong(smooid);
    writeString(output, midtype);
    output.writeDouble(hwide);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(smooid);
    writeString(output, midtype);
    output.putDouble(hwide);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Box_smooth objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Box_smooth objects.
   * @throws IOException
   */
  static public void readBox_smooths(BufferedReader input, Collection<Box_smooth> rows)
      throws IOException {
    String[] saved = Box_smooth.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Box_smooth
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Box_smooth(new Scanner(line)));
    }
    input.close();
    Box_smooth.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Box_smooth objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Box_smooth objects.
   * @throws IOException
   */
  static public void readBox_smooths(File inputFile, Collection<Box_smooth> rows)
      throws IOException {
    readBox_smooths(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Box_smooth objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Box_smooth objects.
   * @throws IOException
   */
  static public void readBox_smooths(InputStream inputStream, Collection<Box_smooth> rows)
      throws IOException {
    readBox_smooths(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Box_smooth objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Box_smooth objects
   * @throws IOException
   */
  static public Set<Box_smooth> readBox_smooths(BufferedReader input) throws IOException {
    Set<Box_smooth> rows = new LinkedHashSet<Box_smooth>();
    readBox_smooths(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Box_smooth objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Box_smooth objects
   * @throws IOException
   */
  static public Set<Box_smooth> readBox_smooths(File inputFile) throws IOException {
    return readBox_smooths(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Box_smooth objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Box_smooth objects
   * @throws IOException
   */
  static public Set<Box_smooth> readBox_smooths(InputStream input) throws IOException {
    return readBox_smooths(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Box_smooth objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param box_smooths the Box_smooth objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Box_smooth> box_smooths)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Box_smooth box_smooth : box_smooths)
      box_smooth.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Box_smooth objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param box_smooths the Box_smooth objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Box_smooth> box_smooths, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?)");
      for (Box_smooth box_smooth : box_smooths) {
        int i = 0;
        statement.setLong(++i, box_smooth.smooid);
        statement.setString(++i, box_smooth.midtype);
        statement.setDouble(++i, box_smooth.hwide);
        statement.setString(++i, box_smooth.auth);
        statement.setLong(++i, box_smooth.commid);
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
   *        Box_smooth table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Box_smooth> readBox_smooths(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Box_smooth> results = new HashSet<Box_smooth>();
    readBox_smooths(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Box_smooth table.
   * @param box_smooths
   * @throws SQLException
   */
  static public void readBox_smooths(Connection connection, String selectStatement,
      Set<Box_smooth> box_smooths) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        box_smooths.add(new Box_smooth(rs));
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
   * this Box_smooth object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Box_smooth object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("smooid, midtype, hwide, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(smooid)).append(", ");
    sql.append("'").append(midtype).append("', ");
    sql.append(Double.toString(hwide)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Box_smooth in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Box_smooth in the database
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
   * Generate a sql script to create a table of type Box_smooth in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Box_smooth in the database
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
    buf.append("smooid       number(9)            NOT NULL,\n");
    buf.append("midtype      varchar2(8)          NOT NULL,\n");
    buf.append("hwide        float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (smooid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (midtype,hwide,auth)");
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
    return 60;
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
    return (other instanceof Box_smooth) && ((Box_smooth) other).smooid == smooid;
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
    return (other instanceof Box_smooth) && ((Box_smooth) other).midtype.equals(midtype)
        && ((Box_smooth) other).hwide == hwide && ((Box_smooth) other).auth.equals(auth);
  }

  /**
   * Boxcar smoothing identfier.
   * 
   * @return smooid
   */
  public long getSmooid() {
    return smooid;
  }

  /**
   * Boxcar smoothing identfier.
   * 
   * @param smooid
   * @throws IllegalArgumentException if smooid >= 1000000000
   */
  public Box_smooth setSmooid(long smooid) {
    if (smooid >= 1000000000L)
      throw new IllegalArgumentException("smooid=" + smooid + " but cannot be >= 1000000000");
    this.smooid = smooid;
    setHash(null);
    return this;
  }

  /**
   * The central tendency method (mean, median, mode, p-norm).
   * 
   * @return midtype
   */
  public String getMidtype() {
    return midtype;
  }

  /**
   * The central tendency method (mean, median, mode, p-norm).
   * 
   * @param midtype
   * @throws IllegalArgumentException if midtype.length() >= 8
   */
  public Box_smooth setMidtype(String midtype) {
    if (midtype.length() > 8)
      throw new IllegalArgumentException(
          String.format("midtype.length() cannot be > 8.  midtype=%s", midtype));
    this.midtype = midtype;
    setHash(null);
    return this;
  }

  /**
   * Smoother half width.
   * <p>
   * Units: s
   * 
   * @return hwide
   */
  public double getHwide() {
    return hwide;
  }

  /**
   * Smoother half width.
   * <p>
   * Units: s
   * 
   * @param hwide
   */
  public Box_smooth setHwide(double hwide) {
    this.hwide = hwide;
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
  public Box_smooth setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * Unique comment identifier that points to free form comments in the <B> remark_master</B> or
   * <B>remark</B> tables. These comments store additional information about a record in another
   * table.
   * 
   * @return commid
   */
  public long getCommid() {
    return commid;
  }

  /**
   * Unique comment identifier that points to free form comments in the <B> remark_master</B> or
   * <B>remark</B> tables. These comments store additional information about a record in another
   * table.
   * 
   * @param commid
   * @throws IllegalArgumentException if commid >= 1000000000
   */
  public Box_smooth setCommid(long commid) {
    if (commid >= 1000000000L)
      throw new IllegalArgumentException("commid=" + commid + " but cannot be >= 1000000000");
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
    return "NNSA KB Custom";
  }

}
