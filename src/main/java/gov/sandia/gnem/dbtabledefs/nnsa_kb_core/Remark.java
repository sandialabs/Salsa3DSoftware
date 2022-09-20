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
package gov.sandia.gnem.dbtabledefs.nnsa_kb_core;

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
 * remark
 */
public class Remark extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Comment identifier. This value is a key that points to free-form comments entered in the
   * <B>remark</B> table. These comments store additional information about a record in another
   * table. The <B>remark</B> table can have many records with the same <I>commid</I> and different
   * <I>lineno</I>, but the same <I>commid</I> will appear in only one other record among the rest
   * of the tables in the database (see <I>lineno</I>).
   */
  private long commid;

  static final public long COMMID_NA = Long.MIN_VALUE;

  /**
   * Comment line number. This number is assigned as a sequence number for multiple line comments.
   */
  private long lineno;

  static final public long LINENO_NA = Long.MIN_VALUE;

  /**
   * Descriptive text. This single line of text is an arbitrary comment about a record in the
   * database. The comment is linked to its parent table only by forward reference from
   * <I>commid</I> in the record of the table of interest. (see <I>commid</I>, <I>lineno</I>)
   */
  private String remark;

  static final public String REMARK_NA = "-";


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("commid", Columns.FieldType.LONG, "%d");
    columns.add("lineno", Columns.FieldType.LONG, "%d");
    columns.add("remark", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Remark(long commid, long lineno, String remark) {
    setValues(commid, lineno, remark);
  }

  private void setValues(long commid, long lineno, String remark) {
    this.commid = commid;
    this.lineno = lineno;
    this.remark = remark;
  }

  /**
   * Copy constructor.
   */
  public Remark(Remark other) {
    this.commid = other.getCommid();
    this.lineno = other.getLineno();
    this.remark = other.getRemark();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Remark() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(COMMID_NA, LINENO_NA, REMARK_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "remark":
        return remark;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "remark":
        remark = value;
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
      case "commid":
        return commid;
      case "lineno":
        return lineno;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "commid":
        commid = value;
        break;
      case "lineno":
        lineno = value;
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
  public Remark(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Remark(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Remark(ByteBuffer input) {
    this(input.getLong(), input.getLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Remark(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Remark(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[3];
    values[0] = commid;
    values[1] = lineno;
    values[2] = remark;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[4];
    values[0] = commid;
    values[1] = lineno;
    values[2] = remark;
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
    output.writeLong(commid);
    output.writeLong(lineno);
    writeString(output, remark);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(commid);
    output.putLong(lineno);
    writeString(output, remark);
  }

  /**
   * Read a Collection of Remark objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Remark objects.
   * @throws IOException
   */
  static public void readRemarks(BufferedReader input, Collection<Remark> rows) throws IOException {
    String[] saved = Remark.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Remark.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Remark(new Scanner(line)));
    }
    input.close();
    Remark.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Remark objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Remark objects.
   * @throws IOException
   */
  static public void readRemarks(File inputFile, Collection<Remark> rows) throws IOException {
    readRemarks(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Remark objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Remark objects.
   * @throws IOException
   */
  static public void readRemarks(InputStream inputStream, Collection<Remark> rows)
      throws IOException {
    readRemarks(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Remark objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Remark objects
   * @throws IOException
   */
  static public Set<Remark> readRemarks(BufferedReader input) throws IOException {
    Set<Remark> rows = new LinkedHashSet<Remark>();
    readRemarks(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Remark objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Remark objects
   * @throws IOException
   */
  static public Set<Remark> readRemarks(File inputFile) throws IOException {
    return readRemarks(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Remark objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Remark objects
   * @throws IOException
   */
  static public Set<Remark> readRemarks(InputStream input) throws IOException {
    return readRemarks(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Remark objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param remarks the Remark objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Remark> remarks) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Remark remark : remarks)
      remark.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Remark objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param remarks the Remark objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Remark> remarks, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?)");
      for (Remark remark : remarks) {
        int i = 0;
        statement.setLong(++i, remark.commid);
        statement.setLong(++i, remark.lineno);
        statement.setString(++i, remark.remark);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Remark
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Remark> readRemarks(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Remark> results = new HashSet<Remark>();
    readRemarks(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Remark
   *        table.
   * @param remarks
   * @throws SQLException
   */
  static public void readRemarks(Connection connection, String selectStatement, Set<Remark> remarks)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        remarks.add(new Remark(rs));
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
   * this Remark object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Remark object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("commid, lineno, remark, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(commid)).append(", ");
    sql.append(Long.toString(lineno)).append(", ");
    sql.append("'").append(remark).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Remark in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Remark in the database
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
   * Generate a sql script to create a table of type Remark in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Remark in the database
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
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lineno       number(8)            NOT NULL,\n");
    buf.append("remark       varchar2(80)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (commid,lineno)");
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
    return 100;
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
    return (other instanceof Remark) && ((Remark) other).commid == commid
        && ((Remark) other).lineno == lineno;
  }

  /**
   * Comment identifier. This value is a key that points to free-form comments entered in the
   * <B>remark</B> table. These comments store additional information about a record in another
   * table. The <B>remark</B> table can have many records with the same <I>commid</I> and different
   * <I>lineno</I>, but the same <I>commid</I> will appear in only one other record among the rest
   * of the tables in the database (see <I>lineno</I>).
   * 
   * @return commid
   */
  public long getCommid() {
    return commid;
  }

  /**
   * Comment identifier. This value is a key that points to free-form comments entered in the
   * <B>remark</B> table. These comments store additional information about a record in another
   * table. The <B>remark</B> table can have many records with the same <I>commid</I> and different
   * <I>lineno</I>, but the same <I>commid</I> will appear in only one other record among the rest
   * of the tables in the database (see <I>lineno</I>).
   * 
   * @param commid
   * @throws IllegalArgumentException if commid >= 1000000000
   */
  public Remark setCommid(long commid) {
    if (commid >= 1000000000L)
      throw new IllegalArgumentException("commid=" + commid + " but cannot be >= 1000000000");
    this.commid = commid;
    setHash(null);
    return this;
  }

  /**
   * Comment line number. This number is assigned as a sequence number for multiple line comments.
   * 
   * @return lineno
   */
  public long getLineno() {
    return lineno;
  }

  /**
   * Comment line number. This number is assigned as a sequence number for multiple line comments.
   * 
   * @param lineno
   * @throws IllegalArgumentException if lineno >= 100000000
   */
  public Remark setLineno(long lineno) {
    if (lineno >= 100000000L)
      throw new IllegalArgumentException("lineno=" + lineno + " but cannot be >= 100000000");
    this.lineno = lineno;
    setHash(null);
    return this;
  }

  /**
   * Descriptive text. This single line of text is an arbitrary comment about a record in the
   * database. The comment is linked to its parent table only by forward reference from
   * <I>commid</I> in the record of the table of interest. (see <I>commid</I>, <I>lineno</I>)
   * 
   * @return remark
   */
  public String getRemark() {
    return remark;
  }

  /**
   * Descriptive text. This single line of text is an arbitrary comment about a record in the
   * database. The comment is linked to its parent table only by forward reference from
   * <I>commid</I> in the record of the table of interest. (see <I>commid</I>, <I>lineno</I>)
   * 
   * @param remark
   * @throws IllegalArgumentException if remark.length() >= 80
   */
  public Remark setRemark(String remark) {
    if (remark.length() > 80)
      throw new IllegalArgumentException(
          String.format("remark.length() cannot be > 80.  remark=%s", remark));
    this.remark = remark;
    setHash(null);
    return this;
  }

  /**
   * Retrieve the name of the schema.
   * 
   * @return schema name
   */
  static public String getSchemaName() {
    return "NNSA KB Core";
  }

}
