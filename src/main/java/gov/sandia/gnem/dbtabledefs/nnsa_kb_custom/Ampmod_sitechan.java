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
 * ampmod_sitechan
 */
public class Ampmod_sitechan extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Amplitude model identifier
   */
  private long ampmodid;

  static final public long AMPMODID_NA = Long.MIN_VALUE;

  /**
   * Channel identifier. This value is a surrogate key used to uniquely identify a specific
   * recording. The column chanid duplicates the information of the compound key
   * <I>sta</I>/<I>chan</I>/<I>time</I>.
   */
  private long chanid;

  static final public long CHANID_NA = Long.MIN_VALUE;

  /**
   * Author who loaded data
   */
  private String ldauth;

  static final public String LDAUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("ampmodid", Columns.FieldType.LONG, "%d");
    columns.add("chanid", Columns.FieldType.LONG, "%d");
    columns.add("ldauth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Ampmod_sitechan(long ampmodid, long chanid, String ldauth) {
    setValues(ampmodid, chanid, ldauth);
  }

  private void setValues(long ampmodid, long chanid, String ldauth) {
    this.ampmodid = ampmodid;
    this.chanid = chanid;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Ampmod_sitechan(Ampmod_sitechan other) {
    this.ampmodid = other.getAmpmodid();
    this.chanid = other.getChanid();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Ampmod_sitechan() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(AMPMODID_NA, CHANID_NA, LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "ldauth":
        return ldauth;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "ldauth":
        ldauth = value;
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
      case "ampmodid":
        return ampmodid;
      case "chanid":
        return chanid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "ampmodid":
        ampmodid = value;
        break;
      case "chanid":
        chanid = value;
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
  public Ampmod_sitechan(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Ampmod_sitechan(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Ampmod_sitechan(ByteBuffer input) {
    this(input.getLong(), input.getLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ampmod_sitechan(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ampmod_sitechan(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[3];
    values[0] = ampmodid;
    values[1] = chanid;
    values[2] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[4];
    values[0] = ampmodid;
    values[1] = chanid;
    values[2] = ldauth;
    values[3] = lddate;
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
    output.writeLong(ampmodid);
    output.writeLong(chanid);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(ampmodid);
    output.putLong(chanid);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Ampmod_sitechan objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Ampmod_sitechan objects.
   * @throws IOException
   */
  static public void readAmpmod_sitechans(BufferedReader input, Collection<Ampmod_sitechan> rows)
      throws IOException {
    String[] saved = Ampmod_sitechan.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Ampmod_sitechan
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Ampmod_sitechan(new Scanner(line)));
    }
    input.close();
    Ampmod_sitechan.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Ampmod_sitechan objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Ampmod_sitechan objects.
   * @throws IOException
   */
  static public void readAmpmod_sitechans(File inputFile, Collection<Ampmod_sitechan> rows)
      throws IOException {
    readAmpmod_sitechans(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Ampmod_sitechan objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Ampmod_sitechan objects.
   * @throws IOException
   */
  static public void readAmpmod_sitechans(InputStream inputStream, Collection<Ampmod_sitechan> rows)
      throws IOException {
    readAmpmod_sitechans(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Ampmod_sitechan objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Ampmod_sitechan objects
   * @throws IOException
   */
  static public Set<Ampmod_sitechan> readAmpmod_sitechans(BufferedReader input) throws IOException {
    Set<Ampmod_sitechan> rows = new LinkedHashSet<Ampmod_sitechan>();
    readAmpmod_sitechans(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Ampmod_sitechan objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Ampmod_sitechan objects
   * @throws IOException
   */
  static public Set<Ampmod_sitechan> readAmpmod_sitechans(File inputFile) throws IOException {
    return readAmpmod_sitechans(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Ampmod_sitechan objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Ampmod_sitechan objects
   * @throws IOException
   */
  static public Set<Ampmod_sitechan> readAmpmod_sitechans(InputStream input) throws IOException {
    return readAmpmod_sitechans(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Ampmod_sitechan objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param ampmod_sitechans the Ampmod_sitechan objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Ampmod_sitechan> ampmod_sitechans)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Ampmod_sitechan ampmod_sitechan : ampmod_sitechans)
      ampmod_sitechan.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Ampmod_sitechan objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param ampmod_sitechans the Ampmod_sitechan objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Ampmod_sitechan> ampmod_sitechans, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?)");
      for (Ampmod_sitechan ampmod_sitechan : ampmod_sitechans) {
        int i = 0;
        statement.setLong(++i, ampmod_sitechan.ampmodid);
        statement.setLong(++i, ampmod_sitechan.chanid);
        statement.setString(++i, ampmod_sitechan.ldauth);
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
   *        Ampmod_sitechan table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Ampmod_sitechan> readAmpmod_sitechans(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Ampmod_sitechan> results = new HashSet<Ampmod_sitechan>();
    readAmpmod_sitechans(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Ampmod_sitechan table.
   * @param ampmod_sitechans
   * @throws SQLException
   */
  static public void readAmpmod_sitechans(Connection connection, String selectStatement,
      Set<Ampmod_sitechan> ampmod_sitechans) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        ampmod_sitechans.add(new Ampmod_sitechan(rs));
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
   * this Ampmod_sitechan object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Ampmod_sitechan object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("ampmodid, chanid, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(ampmodid)).append(", ");
    sql.append(Long.toString(chanid)).append(", ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Ampmod_sitechan in the database. Primary and unique keys are set, if
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
   * Create a table of type Ampmod_sitechan in the database
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
   * Generate a sql script to create a table of type Ampmod_sitechan in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Ampmod_sitechan in the database
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
    buf.append("ampmodid     number(9)            NOT NULL,\n");
    buf.append("chanid       number(8)            NOT NULL,\n");
    buf.append("ldauth       varchar2(15)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (ampmodid,chanid)");
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
    return 35;
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
    return (other instanceof Ampmod_sitechan) && ((Ampmod_sitechan) other).ampmodid == ampmodid
        && ((Ampmod_sitechan) other).chanid == chanid;
  }

  /**
   * Amplitude model identifier
   * 
   * @return ampmodid
   */
  public long getAmpmodid() {
    return ampmodid;
  }

  /**
   * Amplitude model identifier
   * 
   * @param ampmodid
   * @throws IllegalArgumentException if ampmodid >= 1000000000
   */
  public Ampmod_sitechan setAmpmodid(long ampmodid) {
    if (ampmodid >= 1000000000L)
      throw new IllegalArgumentException("ampmodid=" + ampmodid + " but cannot be >= 1000000000");
    this.ampmodid = ampmodid;
    setHash(null);
    return this;
  }

  /**
   * Channel identifier. This value is a surrogate key used to uniquely identify a specific
   * recording. The column chanid duplicates the information of the compound key
   * <I>sta</I>/<I>chan</I>/<I>time</I>.
   * 
   * @return chanid
   */
  public long getChanid() {
    return chanid;
  }

  /**
   * Channel identifier. This value is a surrogate key used to uniquely identify a specific
   * recording. The column chanid duplicates the information of the compound key
   * <I>sta</I>/<I>chan</I>/<I>time</I>.
   * 
   * @param chanid
   * @throws IllegalArgumentException if chanid >= 100000000
   */
  public Ampmod_sitechan setChanid(long chanid) {
    if (chanid >= 100000000L)
      throw new IllegalArgumentException("chanid=" + chanid + " but cannot be >= 100000000");
    this.chanid = chanid;
    setHash(null);
    return this;
  }

  /**
   * Author who loaded data
   * 
   * @return ldauth
   */
  public String getLdauth() {
    return ldauth;
  }

  /**
   * Author who loaded data
   * 
   * @param ldauth
   * @throws IllegalArgumentException if ldauth.length() >= 15
   */
  public Ampmod_sitechan setLdauth(String ldauth) {
    if (ldauth.length() > 15)
      throw new IllegalArgumentException(
          String.format("ldauth.length() cannot be > 15.  ldauth=%s", ldauth));
    this.ldauth = ldauth;
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
