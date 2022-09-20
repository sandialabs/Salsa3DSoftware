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
 * lastid
 */
public class Lastid extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Identifier type. This attribute contains the actual name of a key whose last assigned numeric
   * value is saved in keyvalue.
   */
  private String keyname;

  static final public String KEYNAME_NA = null;

  /**
   * Current identifier value. This attribute maintains the last assigned value (a positive integer)
   * of the counter for the specified keyname. The number keyvalue is the last counter value used
   * for the attribute keyname. Key values are maintained in the database to ensure uniqueness.
   */
  private long keyvalue;

  static final public long KEYVALUE_NA = Long.MIN_VALUE;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("keyname", Columns.FieldType.STRING, "%s");
    columns.add("keyvalue", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Lastid(String keyname, long keyvalue) {
    setValues(keyname, keyvalue);
  }

  private void setValues(String keyname, long keyvalue) {
    this.keyname = keyname;
    this.keyvalue = keyvalue;
  }

  /**
   * Copy constructor.
   */
  public Lastid(Lastid other) {
    this.keyname = other.getKeyname();
    this.keyvalue = other.getKeyvalue();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Lastid() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(KEYNAME_NA, KEYVALUE_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "keyname":
        return keyname;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "keyname":
        keyname = value;
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
      case "keyvalue":
        return keyvalue;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "keyvalue":
        keyvalue = value;
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
  public Lastid(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Lastid(DataInputStream input) throws IOException {
    this(readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Lastid(ByteBuffer input) {
    this(readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Lastid(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Lastid(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getLong(offset + 2));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[2];
    values[0] = keyname;
    values[1] = keyvalue;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[3];
    values[0] = keyname;
    values[1] = keyvalue;
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
    writeString(output, keyname);
    output.writeLong(keyvalue);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, keyname);
    output.putLong(keyvalue);
  }

  /**
   * Read a Collection of Lastid objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Lastid objects.
   * @throws IOException
   */
  static public void readLastids(BufferedReader input, Collection<Lastid> rows) throws IOException {
    String[] saved = Lastid.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Lastid.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Lastid(new Scanner(line)));
    }
    input.close();
    Lastid.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Lastid objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Lastid objects.
   * @throws IOException
   */
  static public void readLastids(File inputFile, Collection<Lastid> rows) throws IOException {
    readLastids(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Lastid objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Lastid objects.
   * @throws IOException
   */
  static public void readLastids(InputStream inputStream, Collection<Lastid> rows)
      throws IOException {
    readLastids(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Lastid objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Lastid objects
   * @throws IOException
   */
  static public Set<Lastid> readLastids(BufferedReader input) throws IOException {
    Set<Lastid> rows = new LinkedHashSet<Lastid>();
    readLastids(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Lastid objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Lastid objects
   * @throws IOException
   */
  static public Set<Lastid> readLastids(File inputFile) throws IOException {
    return readLastids(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Lastid objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Lastid objects
   * @throws IOException
   */
  static public Set<Lastid> readLastids(InputStream input) throws IOException {
    return readLastids(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Lastid objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param lastids the Lastid objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Lastid> lastids) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Lastid lastid : lastids)
      lastid.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Lastid objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param lastids the Lastid objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Lastid> lastids, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?)");
      for (Lastid lastid : lastids) {
        int i = 0;
        statement.setString(++i, lastid.keyname);
        statement.setLong(++i, lastid.keyvalue);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Lastid
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Lastid> readLastids(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Lastid> results = new HashSet<Lastid>();
    readLastids(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Lastid
   *        table.
   * @param lastids
   * @throws SQLException
   */
  static public void readLastids(Connection connection, String selectStatement, Set<Lastid> lastids)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        lastids.add(new Lastid(rs));
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
   * this Lastid object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Lastid object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("keyname, keyvalue, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(keyname).append("', ");
    sql.append(Long.toString(keyvalue)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Lastid in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Lastid in the database
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
   * Generate a sql script to create a table of type Lastid in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Lastid in the database
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
    buf.append("keyname      varchar2(15)         NOT NULL,\n");
    buf.append("keyvalue     number(8)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (keyname)");
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
    return 27;
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
    return (other instanceof Lastid) && ((Lastid) other).keyname.equals(keyname);
  }

  /**
   * Identifier type. This attribute contains the actual name of a key whose last assigned numeric
   * value is saved in keyvalue.
   * 
   * @return keyname
   */
  public String getKeyname() {
    return keyname;
  }

  /**
   * Identifier type. This attribute contains the actual name of a key whose last assigned numeric
   * value is saved in keyvalue.
   * 
   * @param keyname
   * @throws IllegalArgumentException if keyname.length() >= 15
   */
  public Lastid setKeyname(String keyname) {
    if (keyname.length() > 15)
      throw new IllegalArgumentException(
          String.format("keyname.length() cannot be > 15.  keyname=%s", keyname));
    this.keyname = keyname;
    setHash(null);
    return this;
  }

  /**
   * Current identifier value. This attribute maintains the last assigned value (a positive integer)
   * of the counter for the specified keyname. The number keyvalue is the last counter value used
   * for the attribute keyname. Key values are maintained in the database to ensure uniqueness.
   * 
   * @return keyvalue
   */
  public long getKeyvalue() {
    return keyvalue;
  }

  /**
   * Current identifier value. This attribute maintains the last assigned value (a positive integer)
   * of the counter for the specified keyname. The number keyvalue is the last counter value used
   * for the attribute keyname. Key values are maintained in the database to ensure uniqueness.
   * 
   * @param keyvalue
   * @throws IllegalArgumentException if keyvalue >= 100000000
   */
  public Lastid setKeyvalue(long keyvalue) {
    if (keyvalue >= 100000000L)
      throw new IllegalArgumentException("keyvalue=" + keyvalue + " but cannot be >= 100000000");
    this.keyvalue = keyvalue;
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
