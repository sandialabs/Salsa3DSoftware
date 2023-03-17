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

import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * azgap
 */
public class Azgap extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = -1L;

  /**
   * azimuthal gap
   * <p>
   * Units: degree
   */
  private double azgap1;

  static final public double AZGAP1_NA = -1.;

  /**
   * Secondary azimuthal gap, determined by leaving out one station.
   * <p>
   * Units: degree
   */
  private double azgap2;

  static final public double AZGAP2_NA = -1.;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = "-";

  /**
   * number of stations viewing event
   */
  private long nsta;

  static final public long NSTA_NA = -1L;

  /**
   * number of stations within 30 km viewing event
   */
  private long nsta30;

  static final public long NSTA30_NA = -1L;

  /**
   * number of stations within 250 km viewing event
   */
  private long nsta250;

  static final public long NSTA250_NA = -1L;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("azgap1", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("azgap2", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("nsta", Columns.FieldType.LONG, "%d");
    columns.add("nsta30", Columns.FieldType.LONG, "%d");
    columns.add("nsta250", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Azgap(long orid, double azgap1, double azgap2, String sta, long nsta, long nsta30,
      long nsta250) {
    setValues(orid, azgap1, azgap2, sta, nsta, nsta30, nsta250);
  }

  private void setValues(long orid, double azgap1, double azgap2, String sta, long nsta,
      long nsta30, long nsta250) {
    this.orid = orid;
    this.azgap1 = azgap1;
    this.azgap2 = azgap2;
    this.sta = sta;
    this.nsta = nsta;
    this.nsta30 = nsta30;
    this.nsta250 = nsta250;
  }

  /**
   * Copy constructor.
   */
  public Azgap(Azgap other) {
    this.orid = other.getOrid();
    this.azgap1 = other.getAzgap1();
    this.azgap2 = other.getAzgap2();
    this.sta = other.getSta();
    this.nsta = other.getNsta();
    this.nsta30 = other.getNsta30();
    this.nsta250 = other.getNsta250();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Azgap() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(ORID_NA, AZGAP1_NA, AZGAP2_NA, STA_NA, NSTA_NA, NSTA30_NA, NSTA250_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "sta":
        sta = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "azgap1":
        return azgap1;
      case "azgap2":
        return azgap2;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "azgap1":
        azgap1 = value;
        break;
      case "azgap2":
        azgap2 = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "orid":
        return orid;
      case "nsta":
        return nsta;
      case "nsta30":
        return nsta30;
      case "nsta250":
        return nsta250;
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
      case "nsta":
        nsta = value;
        break;
      case "nsta30":
        nsta30 = value;
        break;
      case "nsta250":
        nsta250 = value;
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
  public Azgap(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Azgap(DataInputStream input) throws IOException {
    this(input.readLong(), input.readDouble(), input.readDouble(), readString(input),
        input.readLong(), input.readLong(), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Azgap(ByteBuffer input) {
    this(input.getLong(), input.getDouble(), input.getDouble(), readString(input), input.getLong(),
        input.getLong(), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Azgap(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Azgap(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getString(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getLong(offset + 7));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[7];
    values[0] = orid;
    values[1] = azgap1;
    values[2] = azgap2;
    values[3] = sta;
    values[4] = nsta;
    values[5] = nsta30;
    values[6] = nsta250;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[8];
    values[0] = orid;
    values[1] = azgap1;
    values[2] = azgap2;
    values[3] = sta;
    values[4] = nsta;
    values[5] = nsta30;
    values[6] = nsta250;
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
    output.writeLong(orid);
    output.writeDouble(azgap1);
    output.writeDouble(azgap2);
    writeString(output, sta);
    output.writeLong(nsta);
    output.writeLong(nsta30);
    output.writeLong(nsta250);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(orid);
    output.putDouble(azgap1);
    output.putDouble(azgap2);
    writeString(output, sta);
    output.putLong(nsta);
    output.putLong(nsta30);
    output.putLong(nsta250);
  }

  /**
   * Read a Collection of Azgap objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Azgap objects.
   * @throws IOException
   */
  static public void readAzgaps(BufferedReader input, Collection<Azgap> rows) throws IOException {
    String[] saved = Azgap.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Azgap.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Azgap(new Scanner(line)));
    }
    input.close();
    Azgap.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Azgap objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Azgap objects.
   * @throws IOException
   */
  static public void readAzgaps(File inputFile, Collection<Azgap> rows) throws IOException {
    readAzgaps(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Azgap objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Azgap objects.
   * @throws IOException
   */
  static public void readAzgaps(InputStream inputStream, Collection<Azgap> rows)
      throws IOException {
    readAzgaps(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Azgap objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Azgap objects
   * @throws IOException
   */
  static public Set<Azgap> readAzgaps(BufferedReader input) throws IOException {
    Set<Azgap> rows = new LinkedHashSet<Azgap>();
    readAzgaps(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Azgap objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Azgap objects
   * @throws IOException
   */
  static public Set<Azgap> readAzgaps(File inputFile) throws IOException {
    return readAzgaps(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Azgap objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Azgap objects
   * @throws IOException
   */
  static public Set<Azgap> readAzgaps(InputStream input) throws IOException {
    return readAzgaps(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Azgap objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param azgaps the Azgap objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Azgap> azgaps) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Azgap azgap : azgaps)
      azgap.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Azgap objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param azgaps the Azgap objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Azgap> azgaps, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?)");
      for (Azgap azgap : azgaps) {
        int i = 0;
        statement.setLong(++i, azgap.orid);
        statement.setDouble(++i, azgap.azgap1);
        statement.setDouble(++i, azgap.azgap2);
        statement.setString(++i, azgap.sta);
        statement.setLong(++i, azgap.nsta);
        statement.setLong(++i, azgap.nsta30);
        statement.setLong(++i, azgap.nsta250);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Azgap
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Azgap> readAzgaps(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Azgap> results = new HashSet<Azgap>();
    readAzgaps(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Azgap
   *        table.
   * @param azgaps
   * @throws SQLException
   */
  static public void readAzgaps(Connection connection, String selectStatement, Set<Azgap> azgaps)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        azgaps.add(new Azgap(rs));
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
   * this Azgap object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Azgap object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("orid, azgap1, azgap2, sta, nsta, nsta30, nsta250, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Double.toString(azgap1)).append(", ");
    sql.append(Double.toString(azgap2)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append(Long.toString(nsta)).append(", ");
    sql.append(Long.toString(nsta30)).append(", ");
    sql.append(Long.toString(nsta250)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Azgap in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Azgap in the database
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
   * Generate a sql script to create a table of type Azgap in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Azgap in the database
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
    buf.append("azgap1       float(24)            NOT NULL,\n");
    buf.append("azgap2       float(24)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("nsta         number(8)            NOT NULL,\n");
    buf.append("nsta30       number(8)            NOT NULL,\n");
    buf.append("nsta250      number(8)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (orid)");
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
    return 58;
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
    return (other instanceof Azgap) && ((Azgap) other).orid == orid;
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
  public Azgap setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * azimuthal gap
   * <p>
   * Units: degree
   * 
   * @return azgap1
   */
  public double getAzgap1() {
    return azgap1;
  }

  /**
   * azimuthal gap
   * <p>
   * Units: degree
   * 
   * @param azgap1
   */
  public Azgap setAzgap1(double azgap1) {
    this.azgap1 = azgap1;
    setHash(null);
    return this;
  }

  /**
   * Secondary azimuthal gap, determined by leaving out one station.
   * <p>
   * Units: degree
   * 
   * @return azgap2
   */
  public double getAzgap2() {
    return azgap2;
  }

  /**
   * Secondary azimuthal gap, determined by leaving out one station.
   * <p>
   * Units: degree
   * 
   * @param azgap2
   */
  public Azgap setAzgap2(double azgap2) {
    this.azgap2 = azgap2;
    setHash(null);
    return this;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   * 
   * @return sta
   */
  public String getSta() {
    return sta;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   * 
   * @param sta
   * @throws IllegalArgumentException if sta.length() >= 6
   */
  public Azgap setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * number of stations viewing event
   * 
   * @return nsta
   */
  public long getNsta() {
    return nsta;
  }

  /**
   * number of stations viewing event
   * 
   * @param nsta
   * @throws IllegalArgumentException if nsta >= 100000000
   */
  public Azgap setNsta(long nsta) {
    if (nsta >= 100000000L)
      throw new IllegalArgumentException("nsta=" + nsta + " but cannot be >= 100000000");
    this.nsta = nsta;
    setHash(null);
    return this;
  }

  /**
   * number of stations within 30 km viewing event
   * 
   * @return nsta30
   */
  public long getNsta30() {
    return nsta30;
  }

  /**
   * number of stations within 30 km viewing event
   * 
   * @param nsta30
   * @throws IllegalArgumentException if nsta30 >= 100000000
   */
  public Azgap setNsta30(long nsta30) {
    if (nsta30 >= 100000000L)
      throw new IllegalArgumentException("nsta30=" + nsta30 + " but cannot be >= 100000000");
    this.nsta30 = nsta30;
    setHash(null);
    return this;
  }

  /**
   * number of stations within 250 km viewing event
   * 
   * @return nsta250
   */
  public long getNsta250() {
    return nsta250;
  }

  /**
   * number of stations within 250 km viewing event
   * 
   * @param nsta250
   * @throws IllegalArgumentException if nsta250 >= 100000000
   */
  public Azgap setNsta250(long nsta250) {
    if (nsta250 >= 100000000L)
      throw new IllegalArgumentException("nsta250=" + nsta250 + " but cannot be >= 100000000");
    this.nsta250 = nsta250;
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

  public Buff getBuff() {
      Buff buffer = new Buff(this.getClass().getSimpleName());
      buffer.add("format", 1);
      buffer.add("azgap1", azgap1, 2);
      buffer.add("azgap2", azgap2, 2);
      buffer.add("sta", sta);
      buffer.add("nsta", nsta);
      buffer.add("nsta30", nsta30);
      buffer.add("nsta250", nsta250);

      return buffer;
  }

  static public Buff getBuff(Scanner input) {
      return new Buff(input);
  }

}
