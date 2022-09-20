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
 * bw_filter
 */
public class Bw_filter extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Butterworth filter identifier
   */
  private long bwfilterid;

  static final public long BWFILTERID_NA = Long.MIN_VALUE;

  /**
   * Low cutoff frequency for bandpass and high pass filtering. Undefined for low pass filtering.
   * <p>
   * Units: 1/s
   */
  private double corner1;

  static final public double CORNER1_NA = -1;

  /**
   * High cutoff frequency for bandpass and low pass filtering. Undefined for high pass filtering.
   * <p>
   * Units: 1/s
   */
  private double corner2;

  static final public double CORNER2_NA = -1;

  /**
   * Order of Butterworth filter.
   */
  private long filterorder;

  static final public long FILTERORDER_NA = Long.MIN_VALUE;

  /**
   * Causality of filter, y = causal, n = non-causal.
   */
  private String causflag;

  static final public String CAUSFLAG_NA = null;

  /**
   * Pass (p) or reject (r). Undefined if high or low pass.
   */
  private String passflag;

  static final public String PASSFLAG_NA = null;

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
    columns.add("bwfilterid", Columns.FieldType.LONG, "%d");
    columns.add("corner1", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("corner2", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("filterorder", Columns.FieldType.LONG, "%d");
    columns.add("causflag", Columns.FieldType.STRING, "%s");
    columns.add("passflag", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Bw_filter(long bwfilterid, double corner1, double corner2, long filterorder,
      String causflag, String passflag, String auth, long commid) {
    setValues(bwfilterid, corner1, corner2, filterorder, causflag, passflag, auth, commid);
  }

  private void setValues(long bwfilterid, double corner1, double corner2, long filterorder,
      String causflag, String passflag, String auth, long commid) {
    this.bwfilterid = bwfilterid;
    this.corner1 = corner1;
    this.corner2 = corner2;
    this.filterorder = filterorder;
    this.causflag = causflag;
    this.passflag = passflag;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Bw_filter(Bw_filter other) {
    this.bwfilterid = other.getBwfilterid();
    this.corner1 = other.getCorner1();
    this.corner2 = other.getCorner2();
    this.filterorder = other.getFilterorder();
    this.causflag = other.getCausflag();
    this.passflag = other.getPassflag();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Bw_filter() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(BWFILTERID_NA, CORNER1_NA, CORNER2_NA, FILTERORDER_NA, CAUSFLAG_NA, PASSFLAG_NA,
        AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "causflag":
        return causflag;
      case "passflag":
        return passflag;
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
      case "causflag":
        causflag = value;
        break;
      case "passflag":
        passflag = value;
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
      case "corner1":
        return corner1;
      case "corner2":
        return corner2;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "corner1":
        corner1 = value;
        break;
      case "corner2":
        corner2 = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "bwfilterid":
        return bwfilterid;
      case "filterorder":
        return filterorder;
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
      case "bwfilterid":
        bwfilterid = value;
        break;
      case "filterorder":
        filterorder = value;
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
  public Bw_filter(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Bw_filter(DataInputStream input) throws IOException {
    this(input.readLong(), input.readDouble(), input.readDouble(), input.readLong(),
        readString(input), readString(input), readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Bw_filter(ByteBuffer input) {
    this(input.getLong(), input.getDouble(), input.getDouble(), input.getLong(), readString(input),
        readString(input), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Bw_filter(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Bw_filter(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getLong(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7), input.getLong(offset + 8));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[8];
    values[0] = bwfilterid;
    values[1] = corner1;
    values[2] = corner2;
    values[3] = filterorder;
    values[4] = causflag;
    values[5] = passflag;
    values[6] = auth;
    values[7] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[9];
    values[0] = bwfilterid;
    values[1] = corner1;
    values[2] = corner2;
    values[3] = filterorder;
    values[4] = causflag;
    values[5] = passflag;
    values[6] = auth;
    values[7] = commid;
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
    output.writeLong(bwfilterid);
    output.writeDouble(corner1);
    output.writeDouble(corner2);
    output.writeLong(filterorder);
    writeString(output, causflag);
    writeString(output, passflag);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(bwfilterid);
    output.putDouble(corner1);
    output.putDouble(corner2);
    output.putLong(filterorder);
    writeString(output, causflag);
    writeString(output, passflag);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Bw_filter objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Bw_filter objects.
   * @throws IOException
   */
  static public void readBw_filters(BufferedReader input, Collection<Bw_filter> rows)
      throws IOException {
    String[] saved = Bw_filter.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Bw_filter
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Bw_filter(new Scanner(line)));
    }
    input.close();
    Bw_filter.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Bw_filter objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Bw_filter objects.
   * @throws IOException
   */
  static public void readBw_filters(File inputFile, Collection<Bw_filter> rows) throws IOException {
    readBw_filters(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Bw_filter objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Bw_filter objects.
   * @throws IOException
   */
  static public void readBw_filters(InputStream inputStream, Collection<Bw_filter> rows)
      throws IOException {
    readBw_filters(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Bw_filter objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Bw_filter objects
   * @throws IOException
   */
  static public Set<Bw_filter> readBw_filters(BufferedReader input) throws IOException {
    Set<Bw_filter> rows = new LinkedHashSet<Bw_filter>();
    readBw_filters(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Bw_filter objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Bw_filter objects
   * @throws IOException
   */
  static public Set<Bw_filter> readBw_filters(File inputFile) throws IOException {
    return readBw_filters(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Bw_filter objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Bw_filter objects
   * @throws IOException
   */
  static public Set<Bw_filter> readBw_filters(InputStream input) throws IOException {
    return readBw_filters(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Bw_filter objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param bw_filters the Bw_filter objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Bw_filter> bw_filters)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Bw_filter bw_filter : bw_filters)
      bw_filter.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Bw_filter objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param bw_filters the Bw_filter objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Bw_filter> bw_filters, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?)");
      for (Bw_filter bw_filter : bw_filters) {
        int i = 0;
        statement.setLong(++i, bw_filter.bwfilterid);
        statement.setDouble(++i, bw_filter.corner1);
        statement.setDouble(++i, bw_filter.corner2);
        statement.setLong(++i, bw_filter.filterorder);
        statement.setString(++i, bw_filter.causflag);
        statement.setString(++i, bw_filter.passflag);
        statement.setString(++i, bw_filter.auth);
        statement.setLong(++i, bw_filter.commid);
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
   *        Bw_filter table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Bw_filter> readBw_filters(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Bw_filter> results = new HashSet<Bw_filter>();
    readBw_filters(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Bw_filter table.
   * @param bw_filters
   * @throws SQLException
   */
  static public void readBw_filters(Connection connection, String selectStatement,
      Set<Bw_filter> bw_filters) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        bw_filters.add(new Bw_filter(rs));
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
   * this Bw_filter object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Bw_filter object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "bwfilterid, corner1, corner2, filterorder, causflag, passflag, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(bwfilterid)).append(", ");
    sql.append(Double.toString(corner1)).append(", ");
    sql.append(Double.toString(corner2)).append(", ");
    sql.append(Long.toString(filterorder)).append(", ");
    sql.append("'").append(causflag).append("', ");
    sql.append("'").append(passflag).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Bw_filter in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Bw_filter in the database
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
   * Generate a sql script to create a table of type Bw_filter in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Bw_filter in the database
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
    buf.append("bwfilterid   number(9)            NOT NULL,\n");
    buf.append("corner1      float(24)            NOT NULL,\n");
    buf.append("corner2      float(24)            NOT NULL,\n");
    buf.append("filterorder  number(4)            NOT NULL,\n");
    buf.append("causflag     varchar2(1)          NOT NULL,\n");
    buf.append("passflag     varchar2(1)          NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (bwfilterid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (corner1,corner2,filterorder,causflag,passflag)");
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
    return 74;
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
    return (other instanceof Bw_filter) && ((Bw_filter) other).bwfilterid == bwfilterid;
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
    return (other instanceof Bw_filter) && ((Bw_filter) other).corner1 == corner1
        && ((Bw_filter) other).corner2 == corner2 && ((Bw_filter) other).filterorder == filterorder
        && ((Bw_filter) other).causflag.equals(causflag)
        && ((Bw_filter) other).passflag.equals(passflag);
  }

  /**
   * Butterworth filter identifier
   * 
   * @return bwfilterid
   */
  public long getBwfilterid() {
    return bwfilterid;
  }

  /**
   * Butterworth filter identifier
   * 
   * @param bwfilterid
   * @throws IllegalArgumentException if bwfilterid >= 1000000000
   */
  public Bw_filter setBwfilterid(long bwfilterid) {
    if (bwfilterid >= 1000000000L)
      throw new IllegalArgumentException(
          "bwfilterid=" + bwfilterid + " but cannot be >= 1000000000");
    this.bwfilterid = bwfilterid;
    setHash(null);
    return this;
  }

  /**
   * Low cutoff frequency for bandpass and high pass filtering. Undefined for low pass filtering.
   * <p>
   * Units: 1/s
   * 
   * @return corner1
   */
  public double getCorner1() {
    return corner1;
  }

  /**
   * Low cutoff frequency for bandpass and high pass filtering. Undefined for low pass filtering.
   * <p>
   * Units: 1/s
   * 
   * @param corner1
   */
  public Bw_filter setCorner1(double corner1) {
    this.corner1 = corner1;
    setHash(null);
    return this;
  }

  /**
   * High cutoff frequency for bandpass and low pass filtering. Undefined for high pass filtering.
   * <p>
   * Units: 1/s
   * 
   * @return corner2
   */
  public double getCorner2() {
    return corner2;
  }

  /**
   * High cutoff frequency for bandpass and low pass filtering. Undefined for high pass filtering.
   * <p>
   * Units: 1/s
   * 
   * @param corner2
   */
  public Bw_filter setCorner2(double corner2) {
    this.corner2 = corner2;
    setHash(null);
    return this;
  }

  /**
   * Order of Butterworth filter.
   * 
   * @return filterorder
   */
  public long getFilterorder() {
    return filterorder;
  }

  /**
   * Order of Butterworth filter.
   * 
   * @param filterorder
   * @throws IllegalArgumentException if filterorder >= 10000
   */
  public Bw_filter setFilterorder(long filterorder) {
    if (filterorder >= 10000L)
      throw new IllegalArgumentException("filterorder=" + filterorder + " but cannot be >= 10000");
    this.filterorder = filterorder;
    setHash(null);
    return this;
  }

  /**
   * Causality of filter, y = causal, n = non-causal.
   * 
   * @return causflag
   */
  public String getCausflag() {
    return causflag;
  }

  /**
   * Causality of filter, y = causal, n = non-causal.
   * 
   * @param causflag
   * @throws IllegalArgumentException if causflag.length() >= 1
   */
  public Bw_filter setCausflag(String causflag) {
    if (causflag.length() > 1)
      throw new IllegalArgumentException(
          String.format("causflag.length() cannot be > 1.  causflag=%s", causflag));
    this.causflag = causflag;
    setHash(null);
    return this;
  }

  /**
   * Pass (p) or reject (r). Undefined if high or low pass.
   * 
   * @return passflag
   */
  public String getPassflag() {
    return passflag;
  }

  /**
   * Pass (p) or reject (r). Undefined if high or low pass.
   * 
   * @param passflag
   * @throws IllegalArgumentException if passflag.length() >= 1
   */
  public Bw_filter setPassflag(String passflag) {
    if (passflag.length() > 1)
      throw new IllegalArgumentException(
          String.format("passflag.length() cannot be > 1.  passflag=%s", passflag));
    this.passflag = passflag;
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
  public Bw_filter setAuth(String auth) {
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
  public Bw_filter setCommid(long commid) {
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
