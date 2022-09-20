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
 * bull_assoc
 */
public class Bull_assoc extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Bulletin identifier
   */
  private long bullid;

  static final public long BULLID_NA = Long.MIN_VALUE;

  /**
   * A version code for this particular bulletin if the same bulletin has been released in multiple
   * versions (for corrections, for example)
   */
  private String bullver;

  static final public String BULLVER_NA = null;

  /**
   * Key or identifier name, normally where numeric value is in another column.
   */
  private String keyname;

  static final public String KEYNAME_NA = null;

  /**
   * Numeric value for key, or -1 if key is string
   */
  private long keyvalue;

  static final public long KEYVALUE_NA = Long.MIN_VALUE;

  /**
   * Version (if any) for this instance of keyvalue.
   */
  private String keyver;

  static final public String KEYVER_NA = null;

  /**
   * For the <B>bulletin</B> table, offset to the location of the information e. g. start of
   * bulletin. For the <B>bull_assoc</B> table, offset to the object in the bulletin.
   */
  private long foff;

  static final public long FOFF_NA = Long.MIN_VALUE;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("bullid", Columns.FieldType.LONG, "%d");
    columns.add("bullver", Columns.FieldType.STRING, "%s");
    columns.add("keyname", Columns.FieldType.STRING, "%s");
    columns.add("keyvalue", Columns.FieldType.LONG, "%d");
    columns.add("keyver", Columns.FieldType.STRING, "%s");
    columns.add("foff", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Bull_assoc(long bullid, String bullver, String keyname, long keyvalue, String keyver,
      long foff) {
    setValues(bullid, bullver, keyname, keyvalue, keyver, foff);
  }

  private void setValues(long bullid, String bullver, String keyname, long keyvalue, String keyver,
      long foff) {
    this.bullid = bullid;
    this.bullver = bullver;
    this.keyname = keyname;
    this.keyvalue = keyvalue;
    this.keyver = keyver;
    this.foff = foff;
  }

  /**
   * Copy constructor.
   */
  public Bull_assoc(Bull_assoc other) {
    this.bullid = other.getBullid();
    this.bullver = other.getBullver();
    this.keyname = other.getKeyname();
    this.keyvalue = other.getKeyvalue();
    this.keyver = other.getKeyver();
    this.foff = other.getFoff();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Bull_assoc() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(BULLID_NA, BULLVER_NA, KEYNAME_NA, KEYVALUE_NA, KEYVER_NA, FOFF_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "bullver":
        return bullver;
      case "keyname":
        return keyname;
      case "keyver":
        return keyver;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "bullver":
        bullver = value;
        break;
      case "keyname":
        keyname = value;
        break;
      case "keyver":
        keyver = value;
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
      case "bullid":
        return bullid;
      case "keyvalue":
        return keyvalue;
      case "foff":
        return foff;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "bullid":
        bullid = value;
        break;
      case "keyvalue":
        keyvalue = value;
        break;
      case "foff":
        foff = value;
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
  public Bull_assoc(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Bull_assoc(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), input.readLong(),
        readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Bull_assoc(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), input.getLong(), readString(input),
        input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Bull_assoc(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Bull_assoc(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getLong(offset + 4), input.getString(offset + 5), input.getLong(offset + 6));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[6];
    values[0] = bullid;
    values[1] = bullver;
    values[2] = keyname;
    values[3] = keyvalue;
    values[4] = keyver;
    values[5] = foff;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[7];
    values[0] = bullid;
    values[1] = bullver;
    values[2] = keyname;
    values[3] = keyvalue;
    values[4] = keyver;
    values[5] = foff;
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
    output.writeLong(bullid);
    writeString(output, bullver);
    writeString(output, keyname);
    output.writeLong(keyvalue);
    writeString(output, keyver);
    output.writeLong(foff);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(bullid);
    writeString(output, bullver);
    writeString(output, keyname);
    output.putLong(keyvalue);
    writeString(output, keyver);
    output.putLong(foff);
  }

  /**
   * Read a Collection of Bull_assoc objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Bull_assoc objects.
   * @throws IOException
   */
  static public void readBull_assocs(BufferedReader input, Collection<Bull_assoc> rows)
      throws IOException {
    String[] saved = Bull_assoc.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Bull_assoc
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Bull_assoc(new Scanner(line)));
    }
    input.close();
    Bull_assoc.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Bull_assoc objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Bull_assoc objects.
   * @throws IOException
   */
  static public void readBull_assocs(File inputFile, Collection<Bull_assoc> rows)
      throws IOException {
    readBull_assocs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Bull_assoc objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Bull_assoc objects.
   * @throws IOException
   */
  static public void readBull_assocs(InputStream inputStream, Collection<Bull_assoc> rows)
      throws IOException {
    readBull_assocs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Bull_assoc objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Bull_assoc objects
   * @throws IOException
   */
  static public Set<Bull_assoc> readBull_assocs(BufferedReader input) throws IOException {
    Set<Bull_assoc> rows = new LinkedHashSet<Bull_assoc>();
    readBull_assocs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Bull_assoc objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Bull_assoc objects
   * @throws IOException
   */
  static public Set<Bull_assoc> readBull_assocs(File inputFile) throws IOException {
    return readBull_assocs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Bull_assoc objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Bull_assoc objects
   * @throws IOException
   */
  static public Set<Bull_assoc> readBull_assocs(InputStream input) throws IOException {
    return readBull_assocs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Bull_assoc objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param bull_assocs the Bull_assoc objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Bull_assoc> bull_assocs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Bull_assoc bull_assoc : bull_assocs)
      bull_assoc.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Bull_assoc objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param bull_assocs the Bull_assoc objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Bull_assoc> bull_assocs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?)");
      for (Bull_assoc bull_assoc : bull_assocs) {
        int i = 0;
        statement.setLong(++i, bull_assoc.bullid);
        statement.setString(++i, bull_assoc.bullver);
        statement.setString(++i, bull_assoc.keyname);
        statement.setLong(++i, bull_assoc.keyvalue);
        statement.setString(++i, bull_assoc.keyver);
        statement.setLong(++i, bull_assoc.foff);
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
   *        Bull_assoc table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Bull_assoc> readBull_assocs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Bull_assoc> results = new HashSet<Bull_assoc>();
    readBull_assocs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Bull_assoc table.
   * @param bull_assocs
   * @throws SQLException
   */
  static public void readBull_assocs(Connection connection, String selectStatement,
      Set<Bull_assoc> bull_assocs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        bull_assocs.add(new Bull_assoc(rs));
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
   * this Bull_assoc object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Bull_assoc object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("bullid, bullver, keyname, keyvalue, keyver, foff, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(bullid)).append(", ");
    sql.append("'").append(bullver).append("', ");
    sql.append("'").append(keyname).append("', ");
    sql.append(Long.toString(keyvalue)).append(", ");
    sql.append("'").append(keyver).append("', ");
    sql.append(Long.toString(foff)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Bull_assoc in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Bull_assoc in the database
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
   * Generate a sql script to create a table of type Bull_assoc in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Bull_assoc in the database
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
    buf.append("bullid       number(9)            NOT NULL,\n");
    buf.append("bullver      varchar2(8)          NOT NULL,\n");
    buf.append("keyname      varchar2(15)         NOT NULL,\n");
    buf.append("keyvalue     number(9)            NOT NULL,\n");
    buf.append("keyver       varchar2(8)          NOT NULL,\n");
    buf.append("foff         number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (bullid,bullver,keyname,keyvalue,keyver,foff)");
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
    return 67;
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
    return (other instanceof Bull_assoc) && ((Bull_assoc) other).bullid == bullid
        && ((Bull_assoc) other).bullver.equals(bullver)
        && ((Bull_assoc) other).keyname.equals(keyname) && ((Bull_assoc) other).keyvalue == keyvalue
        && ((Bull_assoc) other).keyver.equals(keyver) && ((Bull_assoc) other).foff == foff;
  }

  /**
   * Bulletin identifier
   * 
   * @return bullid
   */
  public long getBullid() {
    return bullid;
  }

  /**
   * Bulletin identifier
   * 
   * @param bullid
   * @throws IllegalArgumentException if bullid >= 1000000000
   */
  public Bull_assoc setBullid(long bullid) {
    if (bullid >= 1000000000L)
      throw new IllegalArgumentException("bullid=" + bullid + " but cannot be >= 1000000000");
    this.bullid = bullid;
    setHash(null);
    return this;
  }

  /**
   * A version code for this particular bulletin if the same bulletin has been released in multiple
   * versions (for corrections, for example)
   * 
   * @return bullver
   */
  public String getBullver() {
    return bullver;
  }

  /**
   * A version code for this particular bulletin if the same bulletin has been released in multiple
   * versions (for corrections, for example)
   * 
   * @param bullver
   * @throws IllegalArgumentException if bullver.length() >= 8
   */
  public Bull_assoc setBullver(String bullver) {
    if (bullver.length() > 8)
      throw new IllegalArgumentException(
          String.format("bullver.length() cannot be > 8.  bullver=%s", bullver));
    this.bullver = bullver;
    setHash(null);
    return this;
  }

  /**
   * Key or identifier name, normally where numeric value is in another column.
   * 
   * @return keyname
   */
  public String getKeyname() {
    return keyname;
  }

  /**
   * Key or identifier name, normally where numeric value is in another column.
   * 
   * @param keyname
   * @throws IllegalArgumentException if keyname.length() >= 15
   */
  public Bull_assoc setKeyname(String keyname) {
    if (keyname.length() > 15)
      throw new IllegalArgumentException(
          String.format("keyname.length() cannot be > 15.  keyname=%s", keyname));
    this.keyname = keyname;
    setHash(null);
    return this;
  }

  /**
   * Numeric value for key, or -1 if key is string
   * 
   * @return keyvalue
   */
  public long getKeyvalue() {
    return keyvalue;
  }

  /**
   * Numeric value for key, or -1 if key is string
   * 
   * @param keyvalue
   * @throws IllegalArgumentException if keyvalue >= 1000000000
   */
  public Bull_assoc setKeyvalue(long keyvalue) {
    if (keyvalue >= 1000000000L)
      throw new IllegalArgumentException("keyvalue=" + keyvalue + " but cannot be >= 1000000000");
    this.keyvalue = keyvalue;
    setHash(null);
    return this;
  }

  /**
   * Version (if any) for this instance of keyvalue.
   * 
   * @return keyver
   */
  public String getKeyver() {
    return keyver;
  }

  /**
   * Version (if any) for this instance of keyvalue.
   * 
   * @param keyver
   * @throws IllegalArgumentException if keyver.length() >= 8
   */
  public Bull_assoc setKeyver(String keyver) {
    if (keyver.length() > 8)
      throw new IllegalArgumentException(
          String.format("keyver.length() cannot be > 8.  keyver=%s", keyver));
    this.keyver = keyver;
    setHash(null);
    return this;
  }

  /**
   * For the <B>bulletin</B> table, offset to the location of the information e. g. start of
   * bulletin. For the <B>bull_assoc</B> table, offset to the object in the bulletin.
   * 
   * @return foff
   */
  public long getFoff() {
    return foff;
  }

  /**
   * For the <B>bulletin</B> table, offset to the location of the information e. g. start of
   * bulletin. For the <B>bull_assoc</B> table, offset to the object in the bulletin.
   * 
   * @param foff
   * @throws IllegalArgumentException if foff >= 1000000000
   */
  public Bull_assoc setFoff(long foff) {
    if (foff >= 1000000000L)
      throw new IllegalArgumentException("foff=" + foff + " but cannot be >= 1000000000");
    this.foff = foff;
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
