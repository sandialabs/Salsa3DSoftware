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
public class Tomorun extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * -
   */
  private long tomorunid;

  static final public long TOMORUNID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long algorithmid;

  static final public long ALGORITHMID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long polygonid;

  static final public long POLYGONID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long outeriter;

  static final public long OUTERITER_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long inneriter;

  static final public long INNERITER_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private String inputprefix;

  static final public String INPUTPREFIX_NA = null;

  /**
   * -
   */
  private String outputprefix;

  static final public String OUTPUTPREFIX_NA = null;

  /**
   * -
   */
  private String dir;

  static final public String DIR_NA = null;

  /**
   * -
   */
  private String description;

  static final public String DESCRIPTION_NA = "-";

  /**
   * -
   */
  private String auth;

  static final public String AUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("tomorunid", Columns.FieldType.LONG, "%d");
    columns.add("algorithmid", Columns.FieldType.LONG, "%d");
    columns.add("polygonid", Columns.FieldType.LONG, "%d");
    columns.add("outeriter", Columns.FieldType.LONG, "%d");
    columns.add("inneriter", Columns.FieldType.LONG, "%d");
    columns.add("inputprefix", Columns.FieldType.STRING, "%s");
    columns.add("outputprefix", Columns.FieldType.STRING, "%s");
    columns.add("dir", Columns.FieldType.STRING, "%s");
    columns.add("description", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Tomorun(long tomorunid, long algorithmid, long polygonid, long outeriter, long inneriter,
      String inputprefix, String outputprefix, String dir, String description, String auth) {
    setValues(tomorunid, algorithmid, polygonid, outeriter, inneriter, inputprefix, outputprefix,
        dir, description, auth);
  }

  private void setValues(long tomorunid, long algorithmid, long polygonid, long outeriter,
      long inneriter, String inputprefix, String outputprefix, String dir, String description,
      String auth) {
    this.tomorunid = tomorunid;
    this.algorithmid = algorithmid;
    this.polygonid = polygonid;
    this.outeriter = outeriter;
    this.inneriter = inneriter;
    this.inputprefix = inputprefix;
    this.outputprefix = outputprefix;
    this.dir = dir;
    this.description = description;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Tomorun(Tomorun other) {
    this.tomorunid = other.getTomorunid();
    this.algorithmid = other.getAlgorithmid();
    this.polygonid = other.getPolygonid();
    this.outeriter = other.getOuteriter();
    this.inneriter = other.getInneriter();
    this.inputprefix = other.getInputprefix();
    this.outputprefix = other.getOutputprefix();
    this.dir = other.getDir();
    this.description = other.getDescription();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Tomorun() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(TOMORUNID_NA, ALGORITHMID_NA, POLYGONID_NA, OUTERITER_NA, INNERITER_NA,
        INPUTPREFIX_NA, OUTPUTPREFIX_NA, DIR_NA, DESCRIPTION_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "inputprefix":
        return inputprefix;
      case "outputprefix":
        return outputprefix;
      case "dir":
        return dir;
      case "description":
        return description;
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
      case "inputprefix":
        inputprefix = value;
        break;
      case "outputprefix":
        outputprefix = value;
        break;
      case "dir":
        dir = value;
        break;
      case "description":
        description = value;
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
      case "tomorunid":
        return tomorunid;
      case "algorithmid":
        return algorithmid;
      case "polygonid":
        return polygonid;
      case "outeriter":
        return outeriter;
      case "inneriter":
        return inneriter;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "tomorunid":
        tomorunid = value;
        break;
      case "algorithmid":
        algorithmid = value;
        break;
      case "polygonid":
        polygonid = value;
        break;
      case "outeriter":
        outeriter = value;
        break;
      case "inneriter":
        inneriter = value;
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
  public Tomorun(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Tomorun(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        readString(input), readString(input), readString(input), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Tomorun(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getLong(),
        readString(input), readString(input), readString(input), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Tomorun(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Tomorun(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getString(offset + 9),
        input.getString(offset + 10));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[10];
    values[0] = tomorunid;
    values[1] = algorithmid;
    values[2] = polygonid;
    values[3] = outeriter;
    values[4] = inneriter;
    values[5] = inputprefix;
    values[6] = outputprefix;
    values[7] = dir;
    values[8] = description;
    values[9] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[11];
    values[0] = tomorunid;
    values[1] = algorithmid;
    values[2] = polygonid;
    values[3] = outeriter;
    values[4] = inneriter;
    values[5] = inputprefix;
    values[6] = outputprefix;
    values[7] = dir;
    values[8] = description;
    values[9] = auth;
    values[10] = lddate;
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
    output.writeLong(tomorunid);
    output.writeLong(algorithmid);
    output.writeLong(polygonid);
    output.writeLong(outeriter);
    output.writeLong(inneriter);
    writeString(output, inputprefix);
    writeString(output, outputprefix);
    writeString(output, dir);
    writeString(output, description);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(tomorunid);
    output.putLong(algorithmid);
    output.putLong(polygonid);
    output.putLong(outeriter);
    output.putLong(inneriter);
    writeString(output, inputprefix);
    writeString(output, outputprefix);
    writeString(output, dir);
    writeString(output, description);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Tomorun objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Tomorun objects.
   * @throws IOException
   */
  static public void readTomoruns(BufferedReader input, Collection<Tomorun> rows)
      throws IOException {
    String[] saved = Tomorun.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Tomorun.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Tomorun(new Scanner(line)));
    }
    input.close();
    Tomorun.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Tomorun objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Tomorun objects.
   * @throws IOException
   */
  static public void readTomoruns(File inputFile, Collection<Tomorun> rows) throws IOException {
    readTomoruns(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Tomorun objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Tomorun objects.
   * @throws IOException
   */
  static public void readTomoruns(InputStream inputStream, Collection<Tomorun> rows)
      throws IOException {
    readTomoruns(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Tomorun objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Tomorun objects
   * @throws IOException
   */
  static public Set<Tomorun> readTomoruns(BufferedReader input) throws IOException {
    Set<Tomorun> rows = new LinkedHashSet<Tomorun>();
    readTomoruns(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Tomorun objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Tomorun objects
   * @throws IOException
   */
  static public Set<Tomorun> readTomoruns(File inputFile) throws IOException {
    return readTomoruns(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Tomorun objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Tomorun objects
   * @throws IOException
   */
  static public Set<Tomorun> readTomoruns(InputStream input) throws IOException {
    return readTomoruns(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Tomorun objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param tomoruns the Tomorun objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Tomorun> tomoruns)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Tomorun tomorun : tomoruns)
      tomorun.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Tomorun objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param tomoruns the Tomorun objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Tomorun> tomoruns, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?)");
      for (Tomorun tomorun : tomoruns) {
        int i = 0;
        statement.setLong(++i, tomorun.tomorunid);
        statement.setLong(++i, tomorun.algorithmid);
        statement.setLong(++i, tomorun.polygonid);
        statement.setLong(++i, tomorun.outeriter);
        statement.setLong(++i, tomorun.inneriter);
        statement.setString(++i, tomorun.inputprefix);
        statement.setString(++i, tomorun.outputprefix);
        statement.setString(++i, tomorun.dir);
        statement.setString(++i, tomorun.description);
        statement.setString(++i, tomorun.auth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Tomorun
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Tomorun> readTomoruns(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Tomorun> results = new HashSet<Tomorun>();
    readTomoruns(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Tomorun
   *        table.
   * @param tomoruns
   * @throws SQLException
   */
  static public void readTomoruns(Connection connection, String selectStatement,
      Set<Tomorun> tomoruns) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        tomoruns.add(new Tomorun(rs));
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
   * this Tomorun object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Tomorun object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "tomorunid, algorithmid, polygonid, outeriter, inneriter, inputprefix, outputprefix, dir, description, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(tomorunid)).append(", ");
    sql.append(Long.toString(algorithmid)).append(", ");
    sql.append(Long.toString(polygonid)).append(", ");
    sql.append(Long.toString(outeriter)).append(", ");
    sql.append(Long.toString(inneriter)).append(", ");
    sql.append("'").append(inputprefix).append("', ");
    sql.append("'").append(outputprefix).append("', ");
    sql.append("'").append(dir).append("', ");
    sql.append("'").append(description).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Tomorun in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Tomorun in the database
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
   * Generate a sql script to create a table of type Tomorun in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Tomorun in the database
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
    buf.append("tomorunid    number(10)           NOT NULL,\n");
    buf.append("algorithmid  number(10)           NOT NULL,\n");
    buf.append("polygonid    number(10)           NOT NULL,\n");
    buf.append("outeriter    number(10)           NOT NULL,\n");
    buf.append("inneriter    number(10)           NOT NULL,\n");
    buf.append("inputprefix  varchar2(4000)       NOT NULL,\n");
    buf.append("outputprefix varchar2(4000)       NOT NULL,\n");
    buf.append("dir          varchar2(4000)       NOT NULL,\n");
    buf.append("description  varchar2(4000)       NOT NULL,\n");
    buf.append("auth         varchar2(64)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (tomorunid)");
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
    return 16124;
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
    return (other instanceof Tomorun) && ((Tomorun) other).tomorunid == tomorunid;
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
  public Tomorun setTomorunid(long tomorunid) {
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
   * @return algorithmid
   */
  public long getAlgorithmid() {
    return algorithmid;
  }

  /**
   * -
   * 
   * @param algorithmid
   * @throws IllegalArgumentException if algorithmid >= 10000000000
   */
  public Tomorun setAlgorithmid(long algorithmid) {
    if (algorithmid >= 10000000000L)
      throw new IllegalArgumentException(
          "algorithmid=" + algorithmid + " but cannot be >= 10000000000");
    this.algorithmid = algorithmid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return polygonid
   */
  public long getPolygonid() {
    return polygonid;
  }

  /**
   * -
   * 
   * @param polygonid
   * @throws IllegalArgumentException if polygonid >= 10000000000
   */
  public Tomorun setPolygonid(long polygonid) {
    if (polygonid >= 10000000000L)
      throw new IllegalArgumentException(
          "polygonid=" + polygonid + " but cannot be >= 10000000000");
    this.polygonid = polygonid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return outeriter
   */
  public long getOuteriter() {
    return outeriter;
  }

  /**
   * -
   * 
   * @param outeriter
   * @throws IllegalArgumentException if outeriter >= 10000000000
   */
  public Tomorun setOuteriter(long outeriter) {
    if (outeriter >= 10000000000L)
      throw new IllegalArgumentException(
          "outeriter=" + outeriter + " but cannot be >= 10000000000");
    this.outeriter = outeriter;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return inneriter
   */
  public long getInneriter() {
    return inneriter;
  }

  /**
   * -
   * 
   * @param inneriter
   * @throws IllegalArgumentException if inneriter >= 10000000000
   */
  public Tomorun setInneriter(long inneriter) {
    if (inneriter >= 10000000000L)
      throw new IllegalArgumentException(
          "inneriter=" + inneriter + " but cannot be >= 10000000000");
    this.inneriter = inneriter;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return inputprefix
   */
  public String getInputprefix() {
    return inputprefix;
  }

  /**
   * -
   * 
   * @param inputprefix
   * @throws IllegalArgumentException if inputprefix.length() >= 4000
   */
  public Tomorun setInputprefix(String inputprefix) {
    if (inputprefix.length() > 4000)
      throw new IllegalArgumentException(
          String.format("inputprefix.length() cannot be > 4000.  inputprefix=%s", inputprefix));
    this.inputprefix = inputprefix;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return outputprefix
   */
  public String getOutputprefix() {
    return outputprefix;
  }

  /**
   * -
   * 
   * @param outputprefix
   * @throws IllegalArgumentException if outputprefix.length() >= 4000
   */
  public Tomorun setOutputprefix(String outputprefix) {
    if (outputprefix.length() > 4000)
      throw new IllegalArgumentException(
          String.format("outputprefix.length() cannot be > 4000.  outputprefix=%s", outputprefix));
    this.outputprefix = outputprefix;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return dir
   */
  public String getDir() {
    return dir;
  }

  /**
   * -
   * 
   * @param dir
   * @throws IllegalArgumentException if dir.length() >= 4000
   */
  public Tomorun setDir(String dir) {
    if (dir.length() > 4000)
      throw new IllegalArgumentException(
          String.format("dir.length() cannot be > 4000.  dir=%s", dir));
    this.dir = dir;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return description
   */
  public String getDescription() {
    return description;
  }

  /**
   * -
   * 
   * @param description
   * @throws IllegalArgumentException if description.length() >= 4000
   */
  public Tomorun setDescription(String description) {
    if (description.length() > 4000)
      throw new IllegalArgumentException(
          String.format("description.length() cannot be > 4000.  description=%s", description));
    this.description = description;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * -
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 64
   */
  public Tomorun setAuth(String auth) {
    if (auth.length() > 64)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 64.  auth=%s", auth));
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
    return "GMP";
  }

}
