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
 * ttkbcit_log
 */
public class Ttkbcit_log extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta</I>, <I>chan</I> and <I>time</I>.
   */
  private long arid;

  static final public long ARID_NA = Long.MIN_VALUE;

  /**
   * Travel-time model identifier.
   */
  private long ttmodid;

  static final public long TTMODID_NA = Long.MIN_VALUE;

  /**
   * KBCIT project identifier
   */
  private long kbcitid;

  static final public long KBCITID_NA = Long.MIN_VALUE;

  /**
   * Indicator for whether or not outliers in this GT observation have been manually removed (y=yes,
   * n=no)
   */
  private String ismanremoved;

  static final public String ISMANREMOVED_NA = "-";

  /**
   * Indicator for whether or not outiliers in this GT observation have been removed by histogram
   * outlier screening (y=yes, n=no)
   */
  private String ishistoremoved;

  static final public String ISHISTOREMOVED_NA = "-";

  /**
   * The <I>arid</I> of the observation that this observation was clusted into, or -1 if not
   * clustered
   */
  private long clusterarid;

  static final public long CLUSTERARID_NA = -1;

  /**
   * The <I>orid</I> or the observation that this observation was clusted into, or -1 if not
   * clustered.
   */
  private long clusterorid;

  static final public long CLUSTERORID_NA = -1;

  /**
   * Indicator for whether or not this GT observation has been filtered out according to the
   * <I>gtlevel</I> in the <B>ttmod_kbcit</B> table (y=yes, n=no).
   */
  private String isgtfiltered;

  static final public String ISGTFILTERED_NA = "-";

  /**
   * Author who loaded data
   */
  private String ldauth;

  static final public String LDAUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("arid", Columns.FieldType.LONG, "%d");
    columns.add("ttmodid", Columns.FieldType.LONG, "%d");
    columns.add("kbcitid", Columns.FieldType.LONG, "%d");
    columns.add("ismanremoved", Columns.FieldType.STRING, "%s");
    columns.add("ishistoremoved", Columns.FieldType.STRING, "%s");
    columns.add("clusterarid", Columns.FieldType.LONG, "%d");
    columns.add("clusterorid", Columns.FieldType.LONG, "%d");
    columns.add("isgtfiltered", Columns.FieldType.STRING, "%s");
    columns.add("ldauth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Ttkbcit_log(long orid, long arid, long ttmodid, long kbcitid, String ismanremoved,
      String ishistoremoved, long clusterarid, long clusterorid, String isgtfiltered,
      String ldauth) {
    setValues(orid, arid, ttmodid, kbcitid, ismanremoved, ishistoremoved, clusterarid, clusterorid,
        isgtfiltered, ldauth);
  }

  private void setValues(long orid, long arid, long ttmodid, long kbcitid, String ismanremoved,
      String ishistoremoved, long clusterarid, long clusterorid, String isgtfiltered,
      String ldauth) {
    this.orid = orid;
    this.arid = arid;
    this.ttmodid = ttmodid;
    this.kbcitid = kbcitid;
    this.ismanremoved = ismanremoved;
    this.ishistoremoved = ishistoremoved;
    this.clusterarid = clusterarid;
    this.clusterorid = clusterorid;
    this.isgtfiltered = isgtfiltered;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Ttkbcit_log(Ttkbcit_log other) {
    this.orid = other.getOrid();
    this.arid = other.getArid();
    this.ttmodid = other.getTtmodid();
    this.kbcitid = other.getKbcitid();
    this.ismanremoved = other.getIsmanremoved();
    this.ishistoremoved = other.getIshistoremoved();
    this.clusterarid = other.getClusterarid();
    this.clusterorid = other.getClusterorid();
    this.isgtfiltered = other.getIsgtfiltered();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Ttkbcit_log() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(ORID_NA, ARID_NA, TTMODID_NA, KBCITID_NA, ISMANREMOVED_NA, ISHISTOREMOVED_NA,
        CLUSTERARID_NA, CLUSTERORID_NA, ISGTFILTERED_NA, LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "ismanremoved":
        return ismanremoved;
      case "ishistoremoved":
        return ishistoremoved;
      case "isgtfiltered":
        return isgtfiltered;
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
      case "ismanremoved":
        ismanremoved = value;
        break;
      case "ishistoremoved":
        ishistoremoved = value;
        break;
      case "isgtfiltered":
        isgtfiltered = value;
        break;
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
      case "orid":
        return orid;
      case "arid":
        return arid;
      case "ttmodid":
        return ttmodid;
      case "kbcitid":
        return kbcitid;
      case "clusterarid":
        return clusterarid;
      case "clusterorid":
        return clusterorid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "orid":
        orid = value;
        break;
      case "arid":
        arid = value;
        break;
      case "ttmodid":
        ttmodid = value;
        break;
      case "kbcitid":
        kbcitid = value;
        break;
      case "clusterarid":
        clusterarid = value;
        break;
      case "clusterorid":
        clusterorid = value;
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
  public Ttkbcit_log(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Ttkbcit_log(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), readString(input),
        readString(input), input.readLong(), input.readLong(), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Ttkbcit_log(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), readString(input),
        readString(input), input.getLong(), input.getLong(), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ttkbcit_log(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ttkbcit_log(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getLong(offset + 7), input.getLong(offset + 8), input.getString(offset + 9),
        input.getString(offset + 10));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[10];
    values[0] = orid;
    values[1] = arid;
    values[2] = ttmodid;
    values[3] = kbcitid;
    values[4] = ismanremoved;
    values[5] = ishistoremoved;
    values[6] = clusterarid;
    values[7] = clusterorid;
    values[8] = isgtfiltered;
    values[9] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[11];
    values[0] = orid;
    values[1] = arid;
    values[2] = ttmodid;
    values[3] = kbcitid;
    values[4] = ismanremoved;
    values[5] = ishistoremoved;
    values[6] = clusterarid;
    values[7] = clusterorid;
    values[8] = isgtfiltered;
    values[9] = ldauth;
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
    output.writeLong(orid);
    output.writeLong(arid);
    output.writeLong(ttmodid);
    output.writeLong(kbcitid);
    writeString(output, ismanremoved);
    writeString(output, ishistoremoved);
    output.writeLong(clusterarid);
    output.writeLong(clusterorid);
    writeString(output, isgtfiltered);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(orid);
    output.putLong(arid);
    output.putLong(ttmodid);
    output.putLong(kbcitid);
    writeString(output, ismanremoved);
    writeString(output, ishistoremoved);
    output.putLong(clusterarid);
    output.putLong(clusterorid);
    writeString(output, isgtfiltered);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Ttkbcit_log objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Ttkbcit_log objects.
   * @throws IOException
   */
  static public void readTtkbcit_logs(BufferedReader input, Collection<Ttkbcit_log> rows)
      throws IOException {
    String[] saved = Ttkbcit_log.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Ttkbcit_log
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Ttkbcit_log(new Scanner(line)));
    }
    input.close();
    Ttkbcit_log.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Ttkbcit_log objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Ttkbcit_log objects.
   * @throws IOException
   */
  static public void readTtkbcit_logs(File inputFile, Collection<Ttkbcit_log> rows)
      throws IOException {
    readTtkbcit_logs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Ttkbcit_log objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Ttkbcit_log objects.
   * @throws IOException
   */
  static public void readTtkbcit_logs(InputStream inputStream, Collection<Ttkbcit_log> rows)
      throws IOException {
    readTtkbcit_logs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Ttkbcit_log objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Ttkbcit_log objects
   * @throws IOException
   */
  static public Set<Ttkbcit_log> readTtkbcit_logs(BufferedReader input) throws IOException {
    Set<Ttkbcit_log> rows = new LinkedHashSet<Ttkbcit_log>();
    readTtkbcit_logs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Ttkbcit_log objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Ttkbcit_log objects
   * @throws IOException
   */
  static public Set<Ttkbcit_log> readTtkbcit_logs(File inputFile) throws IOException {
    return readTtkbcit_logs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Ttkbcit_log objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Ttkbcit_log objects
   * @throws IOException
   */
  static public Set<Ttkbcit_log> readTtkbcit_logs(InputStream input) throws IOException {
    return readTtkbcit_logs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Ttkbcit_log objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param ttkbcit_logs the Ttkbcit_log objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Ttkbcit_log> ttkbcit_logs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Ttkbcit_log ttkbcit_log : ttkbcit_logs)
      ttkbcit_log.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Ttkbcit_log objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param ttkbcit_logs the Ttkbcit_log objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Ttkbcit_log> ttkbcit_logs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?)");
      for (Ttkbcit_log ttkbcit_log : ttkbcit_logs) {
        int i = 0;
        statement.setLong(++i, ttkbcit_log.orid);
        statement.setLong(++i, ttkbcit_log.arid);
        statement.setLong(++i, ttkbcit_log.ttmodid);
        statement.setLong(++i, ttkbcit_log.kbcitid);
        statement.setString(++i, ttkbcit_log.ismanremoved);
        statement.setString(++i, ttkbcit_log.ishistoremoved);
        statement.setLong(++i, ttkbcit_log.clusterarid);
        statement.setLong(++i, ttkbcit_log.clusterorid);
        statement.setString(++i, ttkbcit_log.isgtfiltered);
        statement.setString(++i, ttkbcit_log.ldauth);
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
   *        Ttkbcit_log table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Ttkbcit_log> readTtkbcit_logs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Ttkbcit_log> results = new HashSet<Ttkbcit_log>();
    readTtkbcit_logs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Ttkbcit_log table.
   * @param ttkbcit_logs
   * @throws SQLException
   */
  static public void readTtkbcit_logs(Connection connection, String selectStatement,
      Set<Ttkbcit_log> ttkbcit_logs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        ttkbcit_logs.add(new Ttkbcit_log(rs));
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
   * this Ttkbcit_log object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Ttkbcit_log object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "orid, arid, ttmodid, kbcitid, ismanremoved, ishistoremoved, clusterarid, clusterorid, isgtfiltered, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(arid)).append(", ");
    sql.append(Long.toString(ttmodid)).append(", ");
    sql.append(Long.toString(kbcitid)).append(", ");
    sql.append("'").append(ismanremoved).append("', ");
    sql.append("'").append(ishistoremoved).append("', ");
    sql.append(Long.toString(clusterarid)).append(", ");
    sql.append(Long.toString(clusterorid)).append(", ");
    sql.append("'").append(isgtfiltered).append("', ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Ttkbcit_log in the database. Primary and unique keys are set, if
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
   * Create a table of type Ttkbcit_log in the database
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
   * Generate a sql script to create a table of type Ttkbcit_log in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Ttkbcit_log in the database
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
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("arid         number(9)            NOT NULL,\n");
    buf.append("ttmodid      number(9)            NOT NULL,\n");
    buf.append("kbcitid      number(9)            NOT NULL,\n");
    buf.append("ismanremoved varchar2(1)          NOT NULL,\n");
    buf.append("ishistoremoved varchar2(1)          NOT NULL,\n");
    buf.append("clusterarid  number(9)            NOT NULL,\n");
    buf.append("clusterorid  number(9)            NOT NULL,\n");
    buf.append("isgtfiltered varchar2(1)          NOT NULL,\n");
    buf.append("ldauth       varchar2(15)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (orid,arid,ttmodid,kbcitid)");
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
    return 82;
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
    return (other instanceof Ttkbcit_log) && ((Ttkbcit_log) other).orid == orid
        && ((Ttkbcit_log) other).arid == arid && ((Ttkbcit_log) other).ttmodid == ttmodid
        && ((Ttkbcit_log) other).kbcitid == kbcitid;
  }

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   * 
   * @return orid
   */
  public long getOrid() {
    return orid;
  }

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   * 
   * @param orid
   * @throws IllegalArgumentException if orid >= 1000000000
   */
  public Ttkbcit_log setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta</I>, <I>chan</I> and <I>time</I>.
   * 
   * @return arid
   */
  public long getArid() {
    return arid;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta</I>, <I>chan</I> and <I>time</I>.
   * 
   * @param arid
   * @throws IllegalArgumentException if arid >= 1000000000
   */
  public Ttkbcit_log setArid(long arid) {
    if (arid >= 1000000000L)
      throw new IllegalArgumentException("arid=" + arid + " but cannot be >= 1000000000");
    this.arid = arid;
    setHash(null);
    return this;
  }

  /**
   * Travel-time model identifier.
   * 
   * @return ttmodid
   */
  public long getTtmodid() {
    return ttmodid;
  }

  /**
   * Travel-time model identifier.
   * 
   * @param ttmodid
   * @throws IllegalArgumentException if ttmodid >= 1000000000
   */
  public Ttkbcit_log setTtmodid(long ttmodid) {
    if (ttmodid >= 1000000000L)
      throw new IllegalArgumentException("ttmodid=" + ttmodid + " but cannot be >= 1000000000");
    this.ttmodid = ttmodid;
    setHash(null);
    return this;
  }

  /**
   * KBCIT project identifier
   * 
   * @return kbcitid
   */
  public long getKbcitid() {
    return kbcitid;
  }

  /**
   * KBCIT project identifier
   * 
   * @param kbcitid
   * @throws IllegalArgumentException if kbcitid >= 1000000000
   */
  public Ttkbcit_log setKbcitid(long kbcitid) {
    if (kbcitid >= 1000000000L)
      throw new IllegalArgumentException("kbcitid=" + kbcitid + " but cannot be >= 1000000000");
    this.kbcitid = kbcitid;
    setHash(null);
    return this;
  }

  /**
   * Indicator for whether or not outliers in this GT observation have been manually removed (y=yes,
   * n=no)
   * 
   * @return ismanremoved
   */
  public String getIsmanremoved() {
    return ismanremoved;
  }

  /**
   * Indicator for whether or not outliers in this GT observation have been manually removed (y=yes,
   * n=no)
   * 
   * @param ismanremoved
   * @throws IllegalArgumentException if ismanremoved.length() >= 1
   */
  public Ttkbcit_log setIsmanremoved(String ismanremoved) {
    if (ismanremoved.length() > 1)
      throw new IllegalArgumentException(
          String.format("ismanremoved.length() cannot be > 1.  ismanremoved=%s", ismanremoved));
    this.ismanremoved = ismanremoved;
    setHash(null);
    return this;
  }

  /**
   * Indicator for whether or not outiliers in this GT observation have been removed by histogram
   * outlier screening (y=yes, n=no)
   * 
   * @return ishistoremoved
   */
  public String getIshistoremoved() {
    return ishistoremoved;
  }

  /**
   * Indicator for whether or not outiliers in this GT observation have been removed by histogram
   * outlier screening (y=yes, n=no)
   * 
   * @param ishistoremoved
   * @throws IllegalArgumentException if ishistoremoved.length() >= 1
   */
  public Ttkbcit_log setIshistoremoved(String ishistoremoved) {
    if (ishistoremoved.length() > 1)
      throw new IllegalArgumentException(String
          .format("ishistoremoved.length() cannot be > 1.  ishistoremoved=%s", ishistoremoved));
    this.ishistoremoved = ishistoremoved;
    setHash(null);
    return this;
  }

  /**
   * The <I>arid</I> of the observation that this observation was clusted into, or -1 if not
   * clustered
   * 
   * @return clusterarid
   */
  public long getClusterarid() {
    return clusterarid;
  }

  /**
   * The <I>arid</I> of the observation that this observation was clusted into, or -1 if not
   * clustered
   * 
   * @param clusterarid
   * @throws IllegalArgumentException if clusterarid >= 1000000000
   */
  public Ttkbcit_log setClusterarid(long clusterarid) {
    if (clusterarid >= 1000000000L)
      throw new IllegalArgumentException(
          "clusterarid=" + clusterarid + " but cannot be >= 1000000000");
    this.clusterarid = clusterarid;
    setHash(null);
    return this;
  }

  /**
   * The <I>orid</I> or the observation that this observation was clusted into, or -1 if not
   * clustered.
   * 
   * @return clusterorid
   */
  public long getClusterorid() {
    return clusterorid;
  }

  /**
   * The <I>orid</I> or the observation that this observation was clusted into, or -1 if not
   * clustered.
   * 
   * @param clusterorid
   * @throws IllegalArgumentException if clusterorid >= 1000000000
   */
  public Ttkbcit_log setClusterorid(long clusterorid) {
    if (clusterorid >= 1000000000L)
      throw new IllegalArgumentException(
          "clusterorid=" + clusterorid + " but cannot be >= 1000000000");
    this.clusterorid = clusterorid;
    setHash(null);
    return this;
  }

  /**
   * Indicator for whether or not this GT observation has been filtered out according to the
   * <I>gtlevel</I> in the <B>ttmod_kbcit</B> table (y=yes, n=no).
   * 
   * @return isgtfiltered
   */
  public String getIsgtfiltered() {
    return isgtfiltered;
  }

  /**
   * Indicator for whether or not this GT observation has been filtered out according to the
   * <I>gtlevel</I> in the <B>ttmod_kbcit</B> table (y=yes, n=no).
   * 
   * @param isgtfiltered
   * @throws IllegalArgumentException if isgtfiltered.length() >= 1
   */
  public Ttkbcit_log setIsgtfiltered(String isgtfiltered) {
    if (isgtfiltered.length() > 1)
      throw new IllegalArgumentException(
          String.format("isgtfiltered.length() cannot be > 1.  isgtfiltered=%s", isgtfiltered));
    this.isgtfiltered = isgtfiltered;
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
  public Ttkbcit_log setLdauth(String ldauth) {
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
