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
 * wftag
 */
public class Wftag extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Tagname type. This value is the name of the foreign key whose value is in <I>tagid</I>
   */
  private String tagname;

  static final public String TAGNAME_NA = null;

  /**
   * Tagname value. This column contains the value of a foreign key identified in <I>tagname</I>
   * [for example, if <I>tagname</I> is <I>arid</I>, then <B>wftag</B> may be joined to
   * <B>arrival</B> where <B>arrival</B>.<I>arid</I> = <B>wftag</B>.<I>tagid</I>. If <I>tagname</I>
   * is <I>orid</I>, then <B>wftag</B> and <B>origin</B> may be joined where
   * <B>origin</B>.<I>orid</I> = <B>wftag</B>.<I>tagid</I>.]
   */
  private long tagid;

  static final public long TAGID_NA = Long.MIN_VALUE;

  /**
   * Unique waveform identifier for a <B>wfdisc</B> record.
   */
  private long wfid;

  static final public long WFID_NA = Long.MIN_VALUE;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("tagname", Columns.FieldType.STRING, "%s");
    columns.add("tagid", Columns.FieldType.LONG, "%d");
    columns.add("wfid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Wftag(String tagname, long tagid, long wfid) {
    setValues(tagname, tagid, wfid);
  }

  private void setValues(String tagname, long tagid, long wfid) {
    this.tagname = tagname;
    this.tagid = tagid;
    this.wfid = wfid;
  }

  /**
   * Copy constructor.
   */
  public Wftag(Wftag other) {
    this.tagname = other.getTagname();
    this.tagid = other.getTagid();
    this.wfid = other.getWfid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Wftag() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(TAGNAME_NA, TAGID_NA, WFID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "tagname":
        return tagname;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "tagname":
        tagname = value;
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
      case "tagid":
        return tagid;
      case "wfid":
        return wfid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "tagid":
        tagid = value;
        break;
      case "wfid":
        wfid = value;
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
  public Wftag(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Wftag(DataInputStream input) throws IOException {
    this(readString(input), input.readLong(), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Wftag(ByteBuffer input) {
    this(readString(input), input.getLong(), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Wftag(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Wftag(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[3];
    values[0] = tagname;
    values[1] = tagid;
    values[2] = wfid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[4];
    values[0] = tagname;
    values[1] = tagid;
    values[2] = wfid;
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
    writeString(output, tagname);
    output.writeLong(tagid);
    output.writeLong(wfid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, tagname);
    output.putLong(tagid);
    output.putLong(wfid);
  }

  /**
   * Read a Collection of Wftag objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Wftag objects.
   * @throws IOException
   */
  static public void readWftags(BufferedReader input, Collection<Wftag> rows) throws IOException {
    String[] saved = Wftag.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Wftag.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Wftag(new Scanner(line)));
    }
    input.close();
    Wftag.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Wftag objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Wftag objects.
   * @throws IOException
   */
  static public void readWftags(File inputFile, Collection<Wftag> rows) throws IOException {
    readWftags(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Wftag objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Wftag objects.
   * @throws IOException
   */
  static public void readWftags(InputStream inputStream, Collection<Wftag> rows)
      throws IOException {
    readWftags(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Wftag objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Wftag objects
   * @throws IOException
   */
  static public Set<Wftag> readWftags(BufferedReader input) throws IOException {
    Set<Wftag> rows = new LinkedHashSet<Wftag>();
    readWftags(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Wftag objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Wftag objects
   * @throws IOException
   */
  static public Set<Wftag> readWftags(File inputFile) throws IOException {
    return readWftags(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Wftag objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Wftag objects
   * @throws IOException
   */
  static public Set<Wftag> readWftags(InputStream input) throws IOException {
    return readWftags(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Wftag objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param wftags the Wftag objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Wftag> wftags) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Wftag wftag : wftags)
      wftag.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Wftag objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param wftags the Wftag objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Wftag> wftags, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?)");
      for (Wftag wftag : wftags) {
        int i = 0;
        statement.setString(++i, wftag.tagname);
        statement.setLong(++i, wftag.tagid);
        statement.setLong(++i, wftag.wfid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Wftag
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Wftag> readWftags(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Wftag> results = new HashSet<Wftag>();
    readWftags(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Wftag
   *        table.
   * @param wftags
   * @throws SQLException
   */
  static public void readWftags(Connection connection, String selectStatement, Set<Wftag> wftags)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        wftags.add(new Wftag(rs));
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
   * this Wftag object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Wftag object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("tagname, tagid, wfid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(tagname).append("', ");
    sql.append(Long.toString(tagid)).append(", ");
    sql.append(Long.toString(wfid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Wftag in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Wftag in the database
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
   * Generate a sql script to create a table of type Wftag in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Wftag in the database
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
    buf.append("tagname      varchar2(8)          NOT NULL,\n");
    buf.append("tagid        number(9)            NOT NULL,\n");
    buf.append("wfid         number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (tagname,tagid,wfid)");
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
    return 28;
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
    return (other instanceof Wftag) && ((Wftag) other).tagname.equals(tagname)
        && ((Wftag) other).tagid == tagid && ((Wftag) other).wfid == wfid;
  }

  /**
   * Tagname type. This value is the name of the foreign key whose value is in <I>tagid</I>
   * 
   * @return tagname
   */
  public String getTagname() {
    return tagname;
  }

  /**
   * Tagname type. This value is the name of the foreign key whose value is in <I>tagid</I>
   * 
   * @param tagname
   * @throws IllegalArgumentException if tagname.length() >= 8
   */
  public Wftag setTagname(String tagname) {
    if (tagname.length() > 8)
      throw new IllegalArgumentException(
          String.format("tagname.length() cannot be > 8.  tagname=%s", tagname));
    this.tagname = tagname;
    setHash(null);
    return this;
  }

  /**
   * Tagname value. This column contains the value of a foreign key identified in <I>tagname</I>
   * [for example, if <I>tagname</I> is <I>arid</I>, then <B>wftag</B> may be joined to
   * <B>arrival</B> where <B>arrival</B>.<I>arid</I> = <B>wftag</B>.<I>tagid</I>. If <I>tagname</I>
   * is <I>orid</I>, then <B>wftag</B> and <B>origin</B> may be joined where
   * <B>origin</B>.<I>orid</I> = <B>wftag</B>.<I>tagid</I>.]
   * 
   * @return tagid
   */
  public long getTagid() {
    return tagid;
  }

  /**
   * Tagname value. This column contains the value of a foreign key identified in <I>tagname</I>
   * [for example, if <I>tagname</I> is <I>arid</I>, then <B>wftag</B> may be joined to
   * <B>arrival</B> where <B>arrival</B>.<I>arid</I> = <B>wftag</B>.<I>tagid</I>. If <I>tagname</I>
   * is <I>orid</I>, then <B>wftag</B> and <B>origin</B> may be joined where
   * <B>origin</B>.<I>orid</I> = <B>wftag</B>.<I>tagid</I>.]
   * 
   * @param tagid
   * @throws IllegalArgumentException if tagid >= 1000000000
   */
  public Wftag setTagid(long tagid) {
    if (tagid >= 1000000000L)
      throw new IllegalArgumentException("tagid=" + tagid + " but cannot be >= 1000000000");
    this.tagid = tagid;
    setHash(null);
    return this;
  }

  /**
   * Unique waveform identifier for a <B>wfdisc</B> record.
   * 
   * @return wfid
   */
  public long getWfid() {
    return wfid;
  }

  /**
   * Unique waveform identifier for a <B>wfdisc</B> record.
   * 
   * @param wfid
   * @throws IllegalArgumentException if wfid >= 1000000000
   */
  public Wftag setWfid(long wfid) {
    if (wfid >= 1000000000L)
      throw new IllegalArgumentException("wfid=" + wfid + " but cannot be >= 1000000000");
    this.wfid = wfid;
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
