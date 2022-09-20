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
 * ampmod_kbcit
 */
public class Ampmod_kbcit extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Amplitude model identifier
   */
  private long ampmodid;

  static final public long AMPMODID_NA = Long.MIN_VALUE;

  /**
   * KBCIT project identifier
   */
  private long kbcitid;

  static final public long KBCITID_NA = Long.MIN_VALUE;

  /**
   * The name of the kriging set for this model.
   */
  private String krigsetname;

  static final public String KRIGSETNAME_NA = null;

  /**
   * Description of the objected represented in the table.
   */
  private String descript;

  static final public String DESCRIPT_NA = null;

  /**
   * GT filter level used in the kriging set to filter observations
   * <p>
   * Units: km
   */
  private double gtfilter_km;

  static final public double GTFILTER_KM_NA = -999;

  /**
   * Author who loaded data
   */
  private String ldauth;

  static final public String LDAUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("ampmodid", Columns.FieldType.LONG, "%d");
    columns.add("kbcitid", Columns.FieldType.LONG, "%d");
    columns.add("krigsetname", Columns.FieldType.STRING, "%s");
    columns.add("descript", Columns.FieldType.STRING, "%s");
    columns.add("gtfilter_km", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("ldauth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Ampmod_kbcit(long ampmodid, long kbcitid, String krigsetname, String descript,
      double gtfilter_km, String ldauth) {
    setValues(ampmodid, kbcitid, krigsetname, descript, gtfilter_km, ldauth);
  }

  private void setValues(long ampmodid, long kbcitid, String krigsetname, String descript,
      double gtfilter_km, String ldauth) {
    this.ampmodid = ampmodid;
    this.kbcitid = kbcitid;
    this.krigsetname = krigsetname;
    this.descript = descript;
    this.gtfilter_km = gtfilter_km;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Ampmod_kbcit(Ampmod_kbcit other) {
    this.ampmodid = other.getAmpmodid();
    this.kbcitid = other.getKbcitid();
    this.krigsetname = other.getKrigsetname();
    this.descript = other.getDescript();
    this.gtfilter_km = other.getGtfilter_km();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Ampmod_kbcit() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(AMPMODID_NA, KBCITID_NA, KRIGSETNAME_NA, DESCRIPT_NA, GTFILTER_KM_NA, LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "krigsetname":
        return krigsetname;
      case "descript":
        return descript;
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
      case "krigsetname":
        krigsetname = value;
        break;
      case "descript":
        descript = value;
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
      case "gtfilter_km":
        return gtfilter_km;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "gtfilter_km":
        gtfilter_km = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "ampmodid":
        return ampmodid;
      case "kbcitid":
        return kbcitid;
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
      case "kbcitid":
        kbcitid = value;
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
  public Ampmod_kbcit(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Ampmod_kbcit(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), readString(input), readString(input),
        input.readDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Ampmod_kbcit(ByteBuffer input) {
    this(input.getLong(), input.getLong(), readString(input), readString(input), input.getDouble(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ampmod_kbcit(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ampmod_kbcit(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getDouble(offset + 5), input.getString(offset + 6));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[6];
    values[0] = ampmodid;
    values[1] = kbcitid;
    values[2] = krigsetname;
    values[3] = descript;
    values[4] = gtfilter_km;
    values[5] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[7];
    values[0] = ampmodid;
    values[1] = kbcitid;
    values[2] = krigsetname;
    values[3] = descript;
    values[4] = gtfilter_km;
    values[5] = ldauth;
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
    output.writeLong(ampmodid);
    output.writeLong(kbcitid);
    writeString(output, krigsetname);
    writeString(output, descript);
    output.writeDouble(gtfilter_km);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(ampmodid);
    output.putLong(kbcitid);
    writeString(output, krigsetname);
    writeString(output, descript);
    output.putDouble(gtfilter_km);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Ampmod_kbcit objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Ampmod_kbcit objects.
   * @throws IOException
   */
  static public void readAmpmod_kbcits(BufferedReader input, Collection<Ampmod_kbcit> rows)
      throws IOException {
    String[] saved = Ampmod_kbcit.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Ampmod_kbcit
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Ampmod_kbcit(new Scanner(line)));
    }
    input.close();
    Ampmod_kbcit.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Ampmod_kbcit objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Ampmod_kbcit objects.
   * @throws IOException
   */
  static public void readAmpmod_kbcits(File inputFile, Collection<Ampmod_kbcit> rows)
      throws IOException {
    readAmpmod_kbcits(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Ampmod_kbcit objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Ampmod_kbcit objects.
   * @throws IOException
   */
  static public void readAmpmod_kbcits(InputStream inputStream, Collection<Ampmod_kbcit> rows)
      throws IOException {
    readAmpmod_kbcits(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Ampmod_kbcit objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Ampmod_kbcit objects
   * @throws IOException
   */
  static public Set<Ampmod_kbcit> readAmpmod_kbcits(BufferedReader input) throws IOException {
    Set<Ampmod_kbcit> rows = new LinkedHashSet<Ampmod_kbcit>();
    readAmpmod_kbcits(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Ampmod_kbcit objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Ampmod_kbcit objects
   * @throws IOException
   */
  static public Set<Ampmod_kbcit> readAmpmod_kbcits(File inputFile) throws IOException {
    return readAmpmod_kbcits(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Ampmod_kbcit objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Ampmod_kbcit objects
   * @throws IOException
   */
  static public Set<Ampmod_kbcit> readAmpmod_kbcits(InputStream input) throws IOException {
    return readAmpmod_kbcits(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Ampmod_kbcit objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param ampmod_kbcits the Ampmod_kbcit objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Ampmod_kbcit> ampmod_kbcits)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Ampmod_kbcit ampmod_kbcit : ampmod_kbcits)
      ampmod_kbcit.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Ampmod_kbcit objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param ampmod_kbcits the Ampmod_kbcit objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Ampmod_kbcit> ampmod_kbcits, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?)");
      for (Ampmod_kbcit ampmod_kbcit : ampmod_kbcits) {
        int i = 0;
        statement.setLong(++i, ampmod_kbcit.ampmodid);
        statement.setLong(++i, ampmod_kbcit.kbcitid);
        statement.setString(++i, ampmod_kbcit.krigsetname);
        statement.setString(++i, ampmod_kbcit.descript);
        statement.setDouble(++i, ampmod_kbcit.gtfilter_km);
        statement.setString(++i, ampmod_kbcit.ldauth);
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
   *        Ampmod_kbcit table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Ampmod_kbcit> readAmpmod_kbcits(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Ampmod_kbcit> results = new HashSet<Ampmod_kbcit>();
    readAmpmod_kbcits(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Ampmod_kbcit table.
   * @param ampmod_kbcits
   * @throws SQLException
   */
  static public void readAmpmod_kbcits(Connection connection, String selectStatement,
      Set<Ampmod_kbcit> ampmod_kbcits) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        ampmod_kbcits.add(new Ampmod_kbcit(rs));
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
   * this Ampmod_kbcit object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Ampmod_kbcit object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("ampmodid, kbcitid, krigsetname, descript, gtfilter_km, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(ampmodid)).append(", ");
    sql.append(Long.toString(kbcitid)).append(", ");
    sql.append("'").append(krigsetname).append("', ");
    sql.append("'").append(descript).append("', ");
    sql.append(Double.toString(gtfilter_km)).append(", ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Ampmod_kbcit in the database. Primary and unique keys are set, if
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
   * Create a table of type Ampmod_kbcit in the database
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
   * Generate a sql script to create a table of type Ampmod_kbcit in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Ampmod_kbcit in the database
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
    buf.append("kbcitid      number(9)            NOT NULL,\n");
    buf.append("krigsetname  varchar2(100)        NOT NULL,\n");
    buf.append("descript     varchar2(1024)       NOT NULL,\n");
    buf.append("gtfilter_km  float(53)            NOT NULL,\n");
    buf.append("ldauth       varchar2(15)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (ampmodid,kbcitid)");
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
    return 1175;
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
    return (other instanceof Ampmod_kbcit) && ((Ampmod_kbcit) other).ampmodid == ampmodid
        && ((Ampmod_kbcit) other).kbcitid == kbcitid;
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
  public Ampmod_kbcit setAmpmodid(long ampmodid) {
    if (ampmodid >= 1000000000L)
      throw new IllegalArgumentException("ampmodid=" + ampmodid + " but cannot be >= 1000000000");
    this.ampmodid = ampmodid;
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
  public Ampmod_kbcit setKbcitid(long kbcitid) {
    if (kbcitid >= 1000000000L)
      throw new IllegalArgumentException("kbcitid=" + kbcitid + " but cannot be >= 1000000000");
    this.kbcitid = kbcitid;
    setHash(null);
    return this;
  }

  /**
   * The name of the kriging set for this model.
   * 
   * @return krigsetname
   */
  public String getKrigsetname() {
    return krigsetname;
  }

  /**
   * The name of the kriging set for this model.
   * 
   * @param krigsetname
   * @throws IllegalArgumentException if krigsetname.length() >= 100
   */
  public Ampmod_kbcit setKrigsetname(String krigsetname) {
    if (krigsetname.length() > 100)
      throw new IllegalArgumentException(
          String.format("krigsetname.length() cannot be > 100.  krigsetname=%s", krigsetname));
    this.krigsetname = krigsetname;
    setHash(null);
    return this;
  }

  /**
   * Description of the objected represented in the table.
   * 
   * @return descript
   */
  public String getDescript() {
    return descript;
  }

  /**
   * Description of the objected represented in the table.
   * 
   * @param descript
   * @throws IllegalArgumentException if descript.length() >= 1024
   */
  public Ampmod_kbcit setDescript(String descript) {
    if (descript.length() > 1024)
      throw new IllegalArgumentException(
          String.format("descript.length() cannot be > 1024.  descript=%s", descript));
    this.descript = descript;
    setHash(null);
    return this;
  }

  /**
   * GT filter level used in the kriging set to filter observations
   * <p>
   * Units: km
   * 
   * @return gtfilter_km
   */
  public double getGtfilter_km() {
    return gtfilter_km;
  }

  /**
   * GT filter level used in the kriging set to filter observations
   * <p>
   * Units: km
   * 
   * @param gtfilter_km
   */
  public Ampmod_kbcit setGtfilter_km(double gtfilter_km) {
    this.gtfilter_km = gtfilter_km;
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
  public Ampmod_kbcit setLdauth(String ldauth) {
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
