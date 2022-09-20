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
 * netmag
 */
public class Netmag extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Network magnitude identifier. This value is assigned to identify a network magnitude in the
   * <B>netmag</B> table. This column is required for every network magnitude. Magnitudes given in
   * <B>origin</B> must reference a network magnitude with <I>magid</I> = <I>mbid</I>, <I>mlid</I>
   * or <I>msid</I>, whichever is appropriate (see <I>mbid</I>, <I>mlid</I>, or <I>msid</I>).
   */
  private long magid;

  static final public long MAGID_NA = Long.MIN_VALUE;

  /**
   * Unique network identifier. This character string is the name of a seismic network (for example,
   * WWSSN).
   */
  private String net;

  static final public String NET_NA = "-";

  /**
   * Origin identifier that relates a record in these tables to a record in the <B>origin</B> table
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Event identifier. Each event is assigned a unique positive integer that identifies it in a
   * database. Several records in the <B>origin</B> table can have the same <I>evid</I>. Analysts
   * have several opinions about the location of the event.
   */
  private long evid;

  static final public long EVID_NA = -1;

  /**
   * Magnitude type (for example, <I>mb</I>).
   */
  private String magtype;

  static final public String MAGTYPE_NA = null;

  /**
   * Number of stations. This column is the number of stations contributing to the network magnitude
   * estimate.
   */
  private long nsta;

  static final public long NSTA_NA = -1;

  /**
   * Magnitude. This column gives the magnitude value of the type indicated in <I>magtype</I>. The
   * value is derived in a variety of ways, which are not necessarily linked directly to an arrival
   * (see <I>magtype</I>, <I>mb</I>, <I>ml</I>, and <I>ms</I>).
   * <p>
   * Units: magnitude
   */
  private double magnitude;

  static final public double MAGNITUDE_NA = -999;

  /**
   * Magnitude uncertainty. This value is the standard deviation of the accompanying magnitude
   * measurement.
   */
  private double uncertainty;

  static final public double UNCERTAINTY_NA = -1;

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   */
  private String auth;

  static final public String AUTH_NA = "-";

  /**
   * Comment identifier. This value is a key that points to free-form comments entered in the
   * <B>remark</B> table. These comments store additional information about a record in another
   * table. The <B>remark</B> table can have many records with the same <I>commid</I> and different
   * <I>lineno</I>, but the same <I>commid</I> will appear in only one other record among the rest
   * of the tables in the database (see <I>lineno</I>).
   */
  private long commid;

  static final public long COMMID_NA = -1;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("magid", Columns.FieldType.LONG, "%d");
    columns.add("net", Columns.FieldType.STRING, "%s");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("evid", Columns.FieldType.LONG, "%d");
    columns.add("magtype", Columns.FieldType.STRING, "%s");
    columns.add("nsta", Columns.FieldType.LONG, "%d");
    columns.add("magnitude", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("uncertainty", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Netmag(long magid, String net, long orid, long evid, String magtype, long nsta,
      double magnitude, double uncertainty, String auth, long commid) {
    setValues(magid, net, orid, evid, magtype, nsta, magnitude, uncertainty, auth, commid);
  }

  private void setValues(long magid, String net, long orid, long evid, String magtype, long nsta,
      double magnitude, double uncertainty, String auth, long commid) {
    this.magid = magid;
    this.net = net;
    this.orid = orid;
    this.evid = evid;
    this.magtype = magtype;
    this.nsta = nsta;
    this.magnitude = magnitude;
    this.uncertainty = uncertainty;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Netmag(Netmag other) {
    this.magid = other.getMagid();
    this.net = other.getNet();
    this.orid = other.getOrid();
    this.evid = other.getEvid();
    this.magtype = other.getMagtype();
    this.nsta = other.getNsta();
    this.magnitude = other.getMagnitude();
    this.uncertainty = other.getUncertainty();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Netmag() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MAGID_NA, NET_NA, ORID_NA, EVID_NA, MAGTYPE_NA, NSTA_NA, MAGNITUDE_NA, UNCERTAINTY_NA,
        AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "net":
        return net;
      case "magtype":
        return magtype;
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
      case "magtype":
        magtype = value;
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
      case "magnitude":
        return magnitude;
      case "uncertainty":
        return uncertainty;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "magnitude":
        magnitude = value;
        break;
      case "uncertainty":
        uncertainty = value;
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
      case "orid":
        return orid;
      case "evid":
        return evid;
      case "nsta":
        return nsta;
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
      case "orid":
        orid = value;
        break;
      case "evid":
        evid = value;
        break;
      case "nsta":
        nsta = value;
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
  public Netmag(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Netmag(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), input.readLong(), input.readLong(), readString(input),
        input.readLong(), input.readDouble(), input.readDouble(), readString(input),
        input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Netmag(ByteBuffer input) {
    this(input.getLong(), readString(input), input.getLong(), input.getLong(), readString(input),
        input.getLong(), input.getDouble(), input.getDouble(), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Netmag(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Netmag(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getString(offset + 5), input.getLong(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getString(offset + 9),
        input.getLong(offset + 10));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[10];
    values[0] = magid;
    values[1] = net;
    values[2] = orid;
    values[3] = evid;
    values[4] = magtype;
    values[5] = nsta;
    values[6] = magnitude;
    values[7] = uncertainty;
    values[8] = auth;
    values[9] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[11];
    values[0] = magid;
    values[1] = net;
    values[2] = orid;
    values[3] = evid;
    values[4] = magtype;
    values[5] = nsta;
    values[6] = magnitude;
    values[7] = uncertainty;
    values[8] = auth;
    values[9] = commid;
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
    output.writeLong(magid);
    writeString(output, net);
    output.writeLong(orid);
    output.writeLong(evid);
    writeString(output, magtype);
    output.writeLong(nsta);
    output.writeDouble(magnitude);
    output.writeDouble(uncertainty);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(magid);
    writeString(output, net);
    output.putLong(orid);
    output.putLong(evid);
    writeString(output, magtype);
    output.putLong(nsta);
    output.putDouble(magnitude);
    output.putDouble(uncertainty);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Netmag objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Netmag objects.
   * @throws IOException
   */
  static public void readNetmags(BufferedReader input, Collection<Netmag> rows) throws IOException {
    String[] saved = Netmag.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Netmag.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Netmag(new Scanner(line)));
    }
    input.close();
    Netmag.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Netmag objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Netmag objects.
   * @throws IOException
   */
  static public void readNetmags(File inputFile, Collection<Netmag> rows) throws IOException {
    readNetmags(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Netmag objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Netmag objects.
   * @throws IOException
   */
  static public void readNetmags(InputStream inputStream, Collection<Netmag> rows)
      throws IOException {
    readNetmags(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Netmag objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Netmag objects
   * @throws IOException
   */
  static public Set<Netmag> readNetmags(BufferedReader input) throws IOException {
    Set<Netmag> rows = new LinkedHashSet<Netmag>();
    readNetmags(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Netmag objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Netmag objects
   * @throws IOException
   */
  static public Set<Netmag> readNetmags(File inputFile) throws IOException {
    return readNetmags(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Netmag objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Netmag objects
   * @throws IOException
   */
  static public Set<Netmag> readNetmags(InputStream input) throws IOException {
    return readNetmags(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Netmag objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param netmags the Netmag objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Netmag> netmags) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Netmag netmag : netmags)
      netmag.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Netmag objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param netmags the Netmag objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Netmag> netmags, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?)");
      for (Netmag netmag : netmags) {
        int i = 0;
        statement.setLong(++i, netmag.magid);
        statement.setString(++i, netmag.net);
        statement.setLong(++i, netmag.orid);
        statement.setLong(++i, netmag.evid);
        statement.setString(++i, netmag.magtype);
        statement.setLong(++i, netmag.nsta);
        statement.setDouble(++i, netmag.magnitude);
        statement.setDouble(++i, netmag.uncertainty);
        statement.setString(++i, netmag.auth);
        statement.setLong(++i, netmag.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Netmag
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Netmag> readNetmags(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Netmag> results = new HashSet<Netmag>();
    readNetmags(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Netmag
   *        table.
   * @param netmags
   * @throws SQLException
   */
  static public void readNetmags(Connection connection, String selectStatement, Set<Netmag> netmags)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        netmags.add(new Netmag(rs));
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
   * this Netmag object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Netmag object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "magid, net, orid, evid, magtype, nsta, magnitude, uncertainty, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(magid)).append(", ");
    sql.append("'").append(net).append("', ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(evid)).append(", ");
    sql.append("'").append(magtype).append("', ");
    sql.append(Long.toString(nsta)).append(", ");
    sql.append(Double.toString(magnitude)).append(", ");
    sql.append(Double.toString(uncertainty)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Netmag in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Netmag in the database
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
   * Generate a sql script to create a table of type Netmag in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Netmag in the database
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
    buf.append("net          varchar2(8)          NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("evid         number(9)            NOT NULL,\n");
    buf.append("magtype      varchar2(6)          NOT NULL,\n");
    buf.append("nsta         number(8)            NOT NULL,\n");
    buf.append("magnitude    float(24)            NOT NULL,\n");
    buf.append("uncertainty  float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (magid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (orid,magtype,auth)");
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
    return 102;
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
    return (other instanceof Netmag) && ((Netmag) other).magid == magid;
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
    return (other instanceof Netmag) && ((Netmag) other).orid == orid
        && ((Netmag) other).magtype.equals(magtype) && ((Netmag) other).auth.equals(auth);
  }

  /**
   * Network magnitude identifier. This value is assigned to identify a network magnitude in the
   * <B>netmag</B> table. This column is required for every network magnitude. Magnitudes given in
   * <B>origin</B> must reference a network magnitude with <I>magid</I> = <I>mbid</I>, <I>mlid</I>
   * or <I>msid</I>, whichever is appropriate (see <I>mbid</I>, <I>mlid</I>, or <I>msid</I>).
   * 
   * @return magid
   */
  public long getMagid() {
    return magid;
  }

  /**
   * Network magnitude identifier. This value is assigned to identify a network magnitude in the
   * <B>netmag</B> table. This column is required for every network magnitude. Magnitudes given in
   * <B>origin</B> must reference a network magnitude with <I>magid</I> = <I>mbid</I>, <I>mlid</I>
   * or <I>msid</I>, whichever is appropriate (see <I>mbid</I>, <I>mlid</I>, or <I>msid</I>).
   * 
   * @param magid
   * @throws IllegalArgumentException if magid >= 1000000000
   */
  public Netmag setMagid(long magid) {
    if (magid >= 1000000000L)
      throw new IllegalArgumentException("magid=" + magid + " but cannot be >= 1000000000");
    this.magid = magid;
    setHash(null);
    return this;
  }

  /**
   * Unique network identifier. This character string is the name of a seismic network (for example,
   * WWSSN).
   * 
   * @return net
   */
  public String getNet() {
    return net;
  }

  /**
   * Unique network identifier. This character string is the name of a seismic network (for example,
   * WWSSN).
   * 
   * @param net
   * @throws IllegalArgumentException if net.length() >= 8
   */
  public Netmag setNet(String net) {
    if (net.length() > 8)
      throw new IllegalArgumentException(String.format("net.length() cannot be > 8.  net=%s", net));
    this.net = net;
    setHash(null);
    return this;
  }

  /**
   * Origin identifier that relates a record in these tables to a record in the <B>origin</B> table
   * 
   * @return orid
   */
  public long getOrid() {
    return orid;
  }

  /**
   * Origin identifier that relates a record in these tables to a record in the <B>origin</B> table
   * 
   * @param orid
   * @throws IllegalArgumentException if orid >= 1000000000
   */
  public Netmag setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Event identifier. Each event is assigned a unique positive integer that identifies it in a
   * database. Several records in the <B>origin</B> table can have the same <I>evid</I>. Analysts
   * have several opinions about the location of the event.
   * 
   * @return evid
   */
  public long getEvid() {
    return evid;
  }

  /**
   * Event identifier. Each event is assigned a unique positive integer that identifies it in a
   * database. Several records in the <B>origin</B> table can have the same <I>evid</I>. Analysts
   * have several opinions about the location of the event.
   * 
   * @param evid
   * @throws IllegalArgumentException if evid >= 1000000000
   */
  public Netmag setEvid(long evid) {
    if (evid >= 1000000000L)
      throw new IllegalArgumentException("evid=" + evid + " but cannot be >= 1000000000");
    this.evid = evid;
    setHash(null);
    return this;
  }

  /**
   * Magnitude type (for example, <I>mb</I>).
   * 
   * @return magtype
   */
  public String getMagtype() {
    return magtype;
  }

  /**
   * Magnitude type (for example, <I>mb</I>).
   * 
   * @param magtype
   * @throws IllegalArgumentException if magtype.length() >= 6
   */
  public Netmag setMagtype(String magtype) {
    if (magtype.length() > 6)
      throw new IllegalArgumentException(
          String.format("magtype.length() cannot be > 6.  magtype=%s", magtype));
    this.magtype = magtype;
    setHash(null);
    return this;
  }

  /**
   * Number of stations. This column is the number of stations contributing to the network magnitude
   * estimate.
   * 
   * @return nsta
   */
  public long getNsta() {
    return nsta;
  }

  /**
   * Number of stations. This column is the number of stations contributing to the network magnitude
   * estimate.
   * 
   * @param nsta
   * @throws IllegalArgumentException if nsta >= 100000000
   */
  public Netmag setNsta(long nsta) {
    if (nsta >= 100000000L)
      throw new IllegalArgumentException("nsta=" + nsta + " but cannot be >= 100000000");
    this.nsta = nsta;
    setHash(null);
    return this;
  }

  /**
   * Magnitude. This column gives the magnitude value of the type indicated in <I>magtype</I>. The
   * value is derived in a variety of ways, which are not necessarily linked directly to an arrival
   * (see <I>magtype</I>, <I>mb</I>, <I>ml</I>, and <I>ms</I>).
   * <p>
   * Units: magnitude
   * 
   * @return magnitude
   */
  public double getMagnitude() {
    return magnitude;
  }

  /**
   * Magnitude. This column gives the magnitude value of the type indicated in <I>magtype</I>. The
   * value is derived in a variety of ways, which are not necessarily linked directly to an arrival
   * (see <I>magtype</I>, <I>mb</I>, <I>ml</I>, and <I>ms</I>).
   * <p>
   * Units: magnitude
   * 
   * @param magnitude
   */
  public Netmag setMagnitude(double magnitude) {
    this.magnitude = magnitude;
    setHash(null);
    return this;
  }

  /**
   * Magnitude uncertainty. This value is the standard deviation of the accompanying magnitude
   * measurement.
   * 
   * @return uncertainty
   */
  public double getUncertainty() {
    return uncertainty;
  }

  /**
   * Magnitude uncertainty. This value is the standard deviation of the accompanying magnitude
   * measurement.
   * 
   * @param uncertainty
   */
  public Netmag setUncertainty(double uncertainty) {
    this.uncertainty = uncertainty;
    setHash(null);
    return this;
  }

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 20
   */
  public Netmag setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
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
  public Netmag setCommid(long commid) {
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
    return "NNSA KB Core";
  }

}
