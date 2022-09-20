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
 * mb_assoc_net
 */
public class Mb_assoc_net extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique magnitude identifier.
   */
  private long magid;

  static final public long MAGID_NA = Long.MIN_VALUE;

  /**
   * Envelope amplitude identifier
   */
  private long envampid;

  static final public long ENVAMPID_NA = Long.MIN_VALUE;

  /**
   * Unique network identifier.
   */
  private String net;

  static final public String NET_NA = null;

  /**
   * mb line fit identifier
   */
  private long mbfitid;

  static final public long MBFITID_NA = Long.MIN_VALUE;

  /**
   * The weight of each record contributing to the overall result
   */
  private double data_weight;

  static final public double DATA_WEIGHT_NA = Double.NaN;

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
    columns.add("magid", Columns.FieldType.LONG, "%d");
    columns.add("envampid", Columns.FieldType.LONG, "%d");
    columns.add("net", Columns.FieldType.STRING, "%s");
    columns.add("mbfitid", Columns.FieldType.LONG, "%d");
    columns.add("data_weight", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Mb_assoc_net(long magid, long envampid, String net, long mbfitid, double data_weight,
      String auth, long commid) {
    setValues(magid, envampid, net, mbfitid, data_weight, auth, commid);
  }

  private void setValues(long magid, long envampid, String net, long mbfitid, double data_weight,
      String auth, long commid) {
    this.magid = magid;
    this.envampid = envampid;
    this.net = net;
    this.mbfitid = mbfitid;
    this.data_weight = data_weight;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Mb_assoc_net(Mb_assoc_net other) {
    this.magid = other.getMagid();
    this.envampid = other.getEnvampid();
    this.net = other.getNet();
    this.mbfitid = other.getMbfitid();
    this.data_weight = other.getData_weight();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Mb_assoc_net() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MAGID_NA, ENVAMPID_NA, NET_NA, MBFITID_NA, DATA_WEIGHT_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "net":
        return net;
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
      case "net":
        net = value;
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
      case "data_weight":
        return data_weight;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "data_weight":
        data_weight = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "magid":
        return magid;
      case "envampid":
        return envampid;
      case "mbfitid":
        return mbfitid;
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
      case "magid":
        magid = value;
        break;
      case "envampid":
        envampid = value;
        break;
      case "mbfitid":
        mbfitid = value;
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
  public Mb_assoc_net(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Mb_assoc_net(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), readString(input), input.readLong(),
        input.readDouble(), readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Mb_assoc_net(ByteBuffer input) {
    this(input.getLong(), input.getLong(), readString(input), input.getLong(), input.getDouble(),
        readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Mb_assoc_net(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Mb_assoc_net(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3),
        input.getLong(offset + 4), input.getDouble(offset + 5), input.getString(offset + 6),
        input.getLong(offset + 7));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[7];
    values[0] = magid;
    values[1] = envampid;
    values[2] = net;
    values[3] = mbfitid;
    values[4] = data_weight;
    values[5] = auth;
    values[6] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[8];
    values[0] = magid;
    values[1] = envampid;
    values[2] = net;
    values[3] = mbfitid;
    values[4] = data_weight;
    values[5] = auth;
    values[6] = commid;
    values[7] = lddate;
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
    output.writeLong(magid);
    output.writeLong(envampid);
    writeString(output, net);
    output.writeLong(mbfitid);
    output.writeDouble(data_weight);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(magid);
    output.putLong(envampid);
    writeString(output, net);
    output.putLong(mbfitid);
    output.putDouble(data_weight);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Mb_assoc_net objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Mb_assoc_net objects.
   * @throws IOException
   */
  static public void readMb_assoc_nets(BufferedReader input, Collection<Mb_assoc_net> rows)
      throws IOException {
    String[] saved = Mb_assoc_net.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Mb_assoc_net
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Mb_assoc_net(new Scanner(line)));
    }
    input.close();
    Mb_assoc_net.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Mb_assoc_net objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Mb_assoc_net objects.
   * @throws IOException
   */
  static public void readMb_assoc_nets(File inputFile, Collection<Mb_assoc_net> rows)
      throws IOException {
    readMb_assoc_nets(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Mb_assoc_net objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Mb_assoc_net objects.
   * @throws IOException
   */
  static public void readMb_assoc_nets(InputStream inputStream, Collection<Mb_assoc_net> rows)
      throws IOException {
    readMb_assoc_nets(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Mb_assoc_net objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Mb_assoc_net objects
   * @throws IOException
   */
  static public Set<Mb_assoc_net> readMb_assoc_nets(BufferedReader input) throws IOException {
    Set<Mb_assoc_net> rows = new LinkedHashSet<Mb_assoc_net>();
    readMb_assoc_nets(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Mb_assoc_net objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Mb_assoc_net objects
   * @throws IOException
   */
  static public Set<Mb_assoc_net> readMb_assoc_nets(File inputFile) throws IOException {
    return readMb_assoc_nets(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Mb_assoc_net objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Mb_assoc_net objects
   * @throws IOException
   */
  static public Set<Mb_assoc_net> readMb_assoc_nets(InputStream input) throws IOException {
    return readMb_assoc_nets(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Mb_assoc_net objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param mb_assoc_nets the Mb_assoc_net objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Mb_assoc_net> mb_assoc_nets)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Mb_assoc_net mb_assoc_net : mb_assoc_nets)
      mb_assoc_net.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Mb_assoc_net objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param mb_assoc_nets the Mb_assoc_net objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Mb_assoc_net> mb_assoc_nets, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?)");
      for (Mb_assoc_net mb_assoc_net : mb_assoc_nets) {
        int i = 0;
        statement.setLong(++i, mb_assoc_net.magid);
        statement.setLong(++i, mb_assoc_net.envampid);
        statement.setString(++i, mb_assoc_net.net);
        statement.setLong(++i, mb_assoc_net.mbfitid);
        statement.setDouble(++i, mb_assoc_net.data_weight);
        statement.setString(++i, mb_assoc_net.auth);
        statement.setLong(++i, mb_assoc_net.commid);
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
   *        Mb_assoc_net table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Mb_assoc_net> readMb_assoc_nets(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Mb_assoc_net> results = new HashSet<Mb_assoc_net>();
    readMb_assoc_nets(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Mb_assoc_net table.
   * @param mb_assoc_nets
   * @throws SQLException
   */
  static public void readMb_assoc_nets(Connection connection, String selectStatement,
      Set<Mb_assoc_net> mb_assoc_nets) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        mb_assoc_nets.add(new Mb_assoc_net(rs));
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
   * this Mb_assoc_net object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Mb_assoc_net object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("magid, envampid, net, mbfitid, data_weight, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(magid)).append(", ");
    sql.append(Long.toString(envampid)).append(", ");
    sql.append("'").append(net).append("', ");
    sql.append(Long.toString(mbfitid)).append(", ");
    sql.append(Double.toString(data_weight)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Mb_assoc_net in the database. Primary and unique keys are set, if
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
   * Create a table of type Mb_assoc_net in the database
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
   * Generate a sql script to create a table of type Mb_assoc_net in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Mb_assoc_net in the database
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
    buf.append("magid        number(9)            NOT NULL,\n");
    buf.append("envampid     number(9)            NOT NULL,\n");
    buf.append("net          varchar2(8)          NOT NULL,\n");
    buf.append("mbfitid      number(9)            NOT NULL,\n");
    buf.append("data_weight  float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (magid,envampid)");
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
    return 76;
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
    return (other instanceof Mb_assoc_net) && ((Mb_assoc_net) other).magid == magid
        && ((Mb_assoc_net) other).envampid == envampid;
  }

  /**
   * Unique magnitude identifier.
   * 
   * @return magid
   */
  public long getMagid() {
    return magid;
  }

  /**
   * Unique magnitude identifier.
   * 
   * @param magid
   * @throws IllegalArgumentException if magid >= 1000000000
   */
  public Mb_assoc_net setMagid(long magid) {
    if (magid >= 1000000000L)
      throw new IllegalArgumentException("magid=" + magid + " but cannot be >= 1000000000");
    this.magid = magid;
    setHash(null);
    return this;
  }

  /**
   * Envelope amplitude identifier
   * 
   * @return envampid
   */
  public long getEnvampid() {
    return envampid;
  }

  /**
   * Envelope amplitude identifier
   * 
   * @param envampid
   * @throws IllegalArgumentException if envampid >= 1000000000
   */
  public Mb_assoc_net setEnvampid(long envampid) {
    if (envampid >= 1000000000L)
      throw new IllegalArgumentException("envampid=" + envampid + " but cannot be >= 1000000000");
    this.envampid = envampid;
    setHash(null);
    return this;
  }

  /**
   * Unique network identifier.
   * 
   * @return net
   */
  public String getNet() {
    return net;
  }

  /**
   * Unique network identifier.
   * 
   * @param net
   * @throws IllegalArgumentException if net.length() >= 8
   */
  public Mb_assoc_net setNet(String net) {
    if (net.length() > 8)
      throw new IllegalArgumentException(String.format("net.length() cannot be > 8.  net=%s", net));
    this.net = net;
    setHash(null);
    return this;
  }

  /**
   * mb line fit identifier
   * 
   * @return mbfitid
   */
  public long getMbfitid() {
    return mbfitid;
  }

  /**
   * mb line fit identifier
   * 
   * @param mbfitid
   * @throws IllegalArgumentException if mbfitid >= 1000000000
   */
  public Mb_assoc_net setMbfitid(long mbfitid) {
    if (mbfitid >= 1000000000L)
      throw new IllegalArgumentException("mbfitid=" + mbfitid + " but cannot be >= 1000000000");
    this.mbfitid = mbfitid;
    setHash(null);
    return this;
  }

  /**
   * The weight of each record contributing to the overall result
   * 
   * @return data_weight
   */
  public double getData_weight() {
    return data_weight;
  }

  /**
   * The weight of each record contributing to the overall result
   * 
   * @param data_weight
   */
  public Mb_assoc_net setData_weight(double data_weight) {
    this.data_weight = data_weight;
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
  public Mb_assoc_net setAuth(String auth) {
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
  public Mb_assoc_net setCommid(long commid) {
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
