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
 * env_memb
 */
public class Env_memb extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   */
  private long magyieldid;

  static final public long MAGYIELDID_NA = Long.MIN_VALUE;

  /**
   * Channel identifier. This value is a surrogate key used to uniquely identify a specific
   * recording. The column chanid duplicates the information of the compound key
   * <I>sta</I>/<I>chan</I>/<I>time</I>.
   */
  private long chanid;

  static final public long CHANID_NA = Long.MIN_VALUE;

  /**
   * Deconvolution identifier
   */
  private long deconid;

  static final public long DECONID_NA = Long.MIN_VALUE;

  /**
   * Log pre-stack site correction applied to individual envelopes, most importantly from array
   * elements. Unitless log10 term.
   */
  private double presite;

  static final public double PRESITE_NA = -999;

  /**
   * Error on log pre-stack site correction applied to individual envelopes, most importantly from
   * array elements.
   */
  private double delpresite;

  static final public double DELPRESITE_NA = -1;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("magyieldid", Columns.FieldType.LONG, "%d");
    columns.add("chanid", Columns.FieldType.LONG, "%d");
    columns.add("deconid", Columns.FieldType.LONG, "%d");
    columns.add("presite", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("delpresite", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Env_memb(long magyieldid, long chanid, long deconid, double presite, double delpresite,
      String auth) {
    setValues(magyieldid, chanid, deconid, presite, delpresite, auth);
  }

  private void setValues(long magyieldid, long chanid, long deconid, double presite,
      double delpresite, String auth) {
    this.magyieldid = magyieldid;
    this.chanid = chanid;
    this.deconid = deconid;
    this.presite = presite;
    this.delpresite = delpresite;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Env_memb(Env_memb other) {
    this.magyieldid = other.getMagyieldid();
    this.chanid = other.getChanid();
    this.deconid = other.getDeconid();
    this.presite = other.getPresite();
    this.delpresite = other.getDelpresite();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Env_memb() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MAGYIELDID_NA, CHANID_NA, DECONID_NA, PRESITE_NA, DELPRESITE_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
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
      case "presite":
        return presite;
      case "delpresite":
        return delpresite;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "presite":
        presite = value;
        break;
      case "delpresite":
        delpresite = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "magyieldid":
        return magyieldid;
      case "chanid":
        return chanid;
      case "deconid":
        return deconid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "magyieldid":
        magyieldid = value;
        break;
      case "chanid":
        chanid = value;
        break;
      case "deconid":
        deconid = value;
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
  public Env_memb(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Env_memb(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readDouble(),
        input.readDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Env_memb(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getDouble(), input.getDouble(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Env_memb(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Env_memb(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getString(offset + 6));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[6];
    values[0] = magyieldid;
    values[1] = chanid;
    values[2] = deconid;
    values[3] = presite;
    values[4] = delpresite;
    values[5] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[7];
    values[0] = magyieldid;
    values[1] = chanid;
    values[2] = deconid;
    values[3] = presite;
    values[4] = delpresite;
    values[5] = auth;
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
    output.writeLong(magyieldid);
    output.writeLong(chanid);
    output.writeLong(deconid);
    output.writeDouble(presite);
    output.writeDouble(delpresite);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(magyieldid);
    output.putLong(chanid);
    output.putLong(deconid);
    output.putDouble(presite);
    output.putDouble(delpresite);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Env_memb objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Env_memb objects.
   * @throws IOException
   */
  static public void readEnv_membs(BufferedReader input, Collection<Env_memb> rows)
      throws IOException {
    String[] saved = Env_memb.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Env_memb
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Env_memb(new Scanner(line)));
    }
    input.close();
    Env_memb.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Env_memb objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Env_memb objects.
   * @throws IOException
   */
  static public void readEnv_membs(File inputFile, Collection<Env_memb> rows) throws IOException {
    readEnv_membs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Env_memb objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Env_memb objects.
   * @throws IOException
   */
  static public void readEnv_membs(InputStream inputStream, Collection<Env_memb> rows)
      throws IOException {
    readEnv_membs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Env_memb objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Env_memb objects
   * @throws IOException
   */
  static public Set<Env_memb> readEnv_membs(BufferedReader input) throws IOException {
    Set<Env_memb> rows = new LinkedHashSet<Env_memb>();
    readEnv_membs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Env_memb objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Env_memb objects
   * @throws IOException
   */
  static public Set<Env_memb> readEnv_membs(File inputFile) throws IOException {
    return readEnv_membs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Env_memb objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Env_memb objects
   * @throws IOException
   */
  static public Set<Env_memb> readEnv_membs(InputStream input) throws IOException {
    return readEnv_membs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Env_memb objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param env_membs the Env_memb objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Env_memb> env_membs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Env_memb env_memb : env_membs)
      env_memb.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Env_memb objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param env_membs the Env_memb objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Env_memb> env_membs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?)");
      for (Env_memb env_memb : env_membs) {
        int i = 0;
        statement.setLong(++i, env_memb.magyieldid);
        statement.setLong(++i, env_memb.chanid);
        statement.setLong(++i, env_memb.deconid);
        statement.setDouble(++i, env_memb.presite);
        statement.setDouble(++i, env_memb.delpresite);
        statement.setString(++i, env_memb.auth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Env_memb
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Env_memb> readEnv_membs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Env_memb> results = new HashSet<Env_memb>();
    readEnv_membs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Env_memb
   *        table.
   * @param env_membs
   * @throws SQLException
   */
  static public void readEnv_membs(Connection connection, String selectStatement,
      Set<Env_memb> env_membs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        env_membs.add(new Env_memb(rs));
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
   * this Env_memb object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Env_memb object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("magyieldid, chanid, deconid, presite, delpresite, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(magyieldid)).append(", ");
    sql.append(Long.toString(chanid)).append(", ");
    sql.append(Long.toString(deconid)).append(", ");
    sql.append(Double.toString(presite)).append(", ");
    sql.append(Double.toString(delpresite)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Env_memb in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Env_memb in the database
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
   * Generate a sql script to create a table of type Env_memb in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Env_memb in the database
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
    buf.append("magyieldid   number(9)            NOT NULL,\n");
    buf.append("chanid       number(8)            NOT NULL,\n");
    buf.append("deconid      number(9)            NOT NULL,\n");
    buf.append("presite      float(24)            NOT NULL,\n");
    buf.append("delpresite   float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (magyieldid,chanid)");
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
    return 64;
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
    return (other instanceof Env_memb) && ((Env_memb) other).magyieldid == magyieldid
        && ((Env_memb) other).chanid == chanid;
  }

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   * 
   * @return magyieldid
   */
  public long getMagyieldid() {
    return magyieldid;
  }

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   * 
   * @param magyieldid
   * @throws IllegalArgumentException if magyieldid >= 1000000000
   */
  public Env_memb setMagyieldid(long magyieldid) {
    if (magyieldid >= 1000000000L)
      throw new IllegalArgumentException(
          "magyieldid=" + magyieldid + " but cannot be >= 1000000000");
    this.magyieldid = magyieldid;
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
  public Env_memb setChanid(long chanid) {
    if (chanid >= 100000000L)
      throw new IllegalArgumentException("chanid=" + chanid + " but cannot be >= 100000000");
    this.chanid = chanid;
    setHash(null);
    return this;
  }

  /**
   * Deconvolution identifier
   * 
   * @return deconid
   */
  public long getDeconid() {
    return deconid;
  }

  /**
   * Deconvolution identifier
   * 
   * @param deconid
   * @throws IllegalArgumentException if deconid >= 1000000000
   */
  public Env_memb setDeconid(long deconid) {
    if (deconid >= 1000000000L)
      throw new IllegalArgumentException("deconid=" + deconid + " but cannot be >= 1000000000");
    this.deconid = deconid;
    setHash(null);
    return this;
  }

  /**
   * Log pre-stack site correction applied to individual envelopes, most importantly from array
   * elements. Unitless log10 term.
   * 
   * @return presite
   */
  public double getPresite() {
    return presite;
  }

  /**
   * Log pre-stack site correction applied to individual envelopes, most importantly from array
   * elements. Unitless log10 term.
   * 
   * @param presite
   */
  public Env_memb setPresite(double presite) {
    this.presite = presite;
    setHash(null);
    return this;
  }

  /**
   * Error on log pre-stack site correction applied to individual envelopes, most importantly from
   * array elements.
   * 
   * @return delpresite
   */
  public double getDelpresite() {
    return delpresite;
  }

  /**
   * Error on log pre-stack site correction applied to individual envelopes, most importantly from
   * array elements.
   * 
   * @param delpresite
   */
  public Env_memb setDelpresite(double delpresite) {
    this.delpresite = delpresite;
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
  public Env_memb setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
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
    return "NNSA KB Custom";
  }

}
