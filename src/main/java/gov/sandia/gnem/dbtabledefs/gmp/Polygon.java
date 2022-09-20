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
package gov.sandia.gnem.dbtabledefs.gmp;

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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * ?
 */
public class Polygon extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * -
   */
  private long polygonid;

  static final public long POLYGONID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private String dir;

  static final public String DIR_NA = null;

  /**
   * -
   */
  private String dfile;

  static final public String DFILE_NA = null;

  /**
   * -
   */
  private double filedate;

  static final public double FILEDATE_NA = Double.NaN;

  /**
   * -
   */
  private String description;

  static final public String DESCRIPTION_NA = null;

  /**
   * -
   */
  private String bottomlayer;

  static final public String BOTTOMLAYER_NA = null;

  /**
   * -
   */
  private String toplayer;

  static final public String TOPLAYER_NA = null;

  /**
   * -
   */
  private String auth;

  static final public String AUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("polygonid", Columns.FieldType.LONG, "%d");
    columns.add("dir", Columns.FieldType.STRING, "%s");
    columns.add("dfile", Columns.FieldType.STRING, "%s");
    columns.add("filedate", Columns.FieldType.DOUBLE, "%s");
    columns.add("description", Columns.FieldType.STRING, "%s");
    columns.add("bottomlayer", Columns.FieldType.STRING, "%s");
    columns.add("toplayer", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Polygon(long polygonid, String dir, String dfile, double filedate, String description,
      String bottomlayer, String toplayer, String auth) {
    setValues(polygonid, dir, dfile, filedate, description, bottomlayer, toplayer, auth);
  }

  private void setValues(long polygonid, String dir, String dfile, double filedate,
      String description, String bottomlayer, String toplayer, String auth) {
    this.polygonid = polygonid;
    this.dir = dir;
    this.dfile = dfile;
    this.filedate = filedate;
    this.description = description;
    this.bottomlayer = bottomlayer;
    this.toplayer = toplayer;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Polygon(Polygon other) {
    this.polygonid = other.getPolygonid();
    this.dir = other.getDir();
    this.dfile = other.getDfile();
    this.filedate = other.getFiledate();
    this.description = other.getDescription();
    this.bottomlayer = other.getBottomlayer();
    this.toplayer = other.getToplayer();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Polygon() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(POLYGONID_NA, DIR_NA, DFILE_NA, FILEDATE_NA, DESCRIPTION_NA, BOTTOMLAYER_NA,
        TOPLAYER_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "dir":
        return dir;
      case "dfile":
        return dfile;
      case "description":
        return description;
      case "bottomlayer":
        return bottomlayer;
      case "toplayer":
        return toplayer;
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
      case "dir":
        dir = value;
        break;
      case "dfile":
        dfile = value;
        break;
      case "description":
        description = value;
        break;
      case "bottomlayer":
        bottomlayer = value;
        break;
      case "toplayer":
        toplayer = value;
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
      case "filedate":
        return filedate;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "filedate":
        filedate = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "polygonid":
        return polygonid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "polygonid":
        polygonid = value;
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
  public Polygon(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Polygon(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), input.readDouble(),
        readString(input), readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Polygon(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), input.getDouble(),
        readString(input), readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Polygon(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Polygon(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getDouble(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[8];
    values[0] = polygonid;
    values[1] = dir;
    values[2] = dfile;
    values[3] = filedate;
    values[4] = description;
    values[5] = bottomlayer;
    values[6] = toplayer;
    values[7] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[9];
    values[0] = polygonid;
    values[1] = dir;
    values[2] = dfile;
    values[3] = filedate;
    values[4] = description;
    values[5] = bottomlayer;
    values[6] = toplayer;
    values[7] = auth;
    values[8] = lddate;
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
    output.writeLong(polygonid);
    writeString(output, dir);
    writeString(output, dfile);
    output.writeDouble(filedate);
    writeString(output, description);
    writeString(output, bottomlayer);
    writeString(output, toplayer);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(polygonid);
    writeString(output, dir);
    writeString(output, dfile);
    output.putDouble(filedate);
    writeString(output, description);
    writeString(output, bottomlayer);
    writeString(output, toplayer);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Polygon objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Polygon objects.
   * @throws IOException
   */
  static public void readPolygons(BufferedReader input, Collection<Polygon> rows)
      throws IOException {
    String[] saved = Polygon.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Polygon.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Polygon(new Scanner(line)));
    }
    input.close();
    Polygon.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Polygon objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Polygon objects.
   * @throws IOException
   */
  static public void readPolygons(File inputFile, Collection<Polygon> rows) throws IOException {
    readPolygons(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Polygon objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Polygon objects.
   * @throws IOException
   */
  static public void readPolygons(InputStream inputStream, Collection<Polygon> rows)
      throws IOException {
    readPolygons(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Polygon objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Polygon objects
   * @throws IOException
   */
  static public Set<Polygon> readPolygons(BufferedReader input) throws IOException {
    Set<Polygon> rows = new LinkedHashSet<Polygon>();
    readPolygons(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Polygon objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Polygon objects
   * @throws IOException
   */
  static public Set<Polygon> readPolygons(File inputFile) throws IOException {
    return readPolygons(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Polygon objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Polygon objects
   * @throws IOException
   */
  static public Set<Polygon> readPolygons(InputStream input) throws IOException {
    return readPolygons(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Polygon objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param polygons the Polygon objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Polygon> polygons)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Polygon polygon : polygons)
      polygon.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Polygon objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param polygons the Polygon objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Polygon> polygons, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?)");
      for (Polygon polygon : polygons) {
        int i = 0;
        statement.setLong(++i, polygon.polygonid);
        statement.setString(++i, polygon.dir);
        statement.setString(++i, polygon.dfile);
        statement.setTimestamp(++i, new Timestamp((long)(polygon.filedate*1000)));
        statement.setString(++i, polygon.description);
        statement.setString(++i, polygon.bottomlayer);
        statement.setString(++i, polygon.toplayer);
        statement.setString(++i, polygon.auth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Polygon
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Polygon> readPolygons(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Polygon> results = new HashSet<Polygon>();
    readPolygons(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Polygon
   *        table.
   * @param polygons
   * @throws SQLException
   */
  static public void readPolygons(Connection connection, String selectStatement,
      Set<Polygon> polygons) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        polygons.add(new Polygon(rs));
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
   * this Polygon object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Polygon object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("polygonid, dir, dfile, filedate, description, bottomlayer, toplayer, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(polygonid)).append(", ");
    sql.append("'").append(dir).append("', ");
    sql.append("'").append(dfile).append("', ");
    sql.append(Double.toString(filedate)).append(", ");
    sql.append("'").append(description).append("', ");
    sql.append("'").append(bottomlayer).append("', ");
    sql.append("'").append(toplayer).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Polygon in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Polygon in the database
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
   * Generate a sql script to create a table of type Polygon in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Polygon in the database
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
    buf.append("polygonid    number(10)           NOT NULL,\n");
    buf.append("dir          varchar2(4000)       NOT NULL,\n");
    buf.append("dfile        varchar2(4000)       NOT NULL,\n");
    buf.append("filedate     timestamp(3)         NOT NULL,\n");
    buf.append("description  varchar2(4000)       NOT NULL,\n");
    buf.append("bottomlayer  varchar2(2000)       NOT NULL,\n");
    buf.append("toplayer     varchar2(2000)       NOT NULL,\n");
    buf.append("auth         varchar2(64)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (polygonid)");
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
    return 16104;
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
    return (other instanceof Polygon) && ((Polygon) other).polygonid == polygonid;
  }

  /**
   * -
   * 
   * @return polygonid
   */
  public long getPolygonid() {
    return polygonid;
  }

  /**
   * -
   * 
   * @param polygonid
   * @throws IllegalArgumentException if polygonid >= 10000000000
   */
  public Polygon setPolygonid(long polygonid) {
    if (polygonid >= 10000000000L)
      throw new IllegalArgumentException(
          "polygonid=" + polygonid + " but cannot be >= 10000000000");
    this.polygonid = polygonid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return dir
   */
  public String getDir() {
    return dir;
  }

  /**
   * -
   * 
   * @param dir
   * @throws IllegalArgumentException if dir.length() >= 4000
   */
  public Polygon setDir(String dir) {
    if (dir.length() > 4000)
      throw new IllegalArgumentException(
          String.format("dir.length() cannot be > 4000.  dir=%s", dir));
    this.dir = dir;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return dfile
   */
  public String getDfile() {
    return dfile;
  }

  /**
   * -
   * 
   * @param dfile
   * @throws IllegalArgumentException if dfile.length() >= 4000
   */
  public Polygon setDfile(String dfile) {
    if (dfile.length() > 4000)
      throw new IllegalArgumentException(
          String.format("dfile.length() cannot be > 4000.  dfile=%s", dfile));
    this.dfile = dfile;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return filedate
   */
  public double getFiledate() {
    return filedate;
  }

  /**
   * -
   * 
   * @param filedate
   */
  public Polygon setFiledate(double filedate) {
    this.filedate = filedate;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return description
   */
  public String getDescription() {
    return description;
  }

  /**
   * -
   * 
   * @param description
   * @throws IllegalArgumentException if description.length() >= 4000
   */
  public Polygon setDescription(String description) {
    if (description.length() > 4000)
      throw new IllegalArgumentException(
          String.format("description.length() cannot be > 4000.  description=%s", description));
    this.description = description;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return bottomlayer
   */
  public String getBottomlayer() {
    return bottomlayer;
  }

  /**
   * -
   * 
   * @param bottomlayer
   * @throws IllegalArgumentException if bottomlayer.length() >= 2000
   */
  public Polygon setBottomlayer(String bottomlayer) {
    if (bottomlayer.length() > 2000)
      throw new IllegalArgumentException(
          String.format("bottomlayer.length() cannot be > 2000.  bottomlayer=%s", bottomlayer));
    this.bottomlayer = bottomlayer;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return toplayer
   */
  public String getToplayer() {
    return toplayer;
  }

  /**
   * -
   * 
   * @param toplayer
   * @throws IllegalArgumentException if toplayer.length() >= 2000
   */
  public Polygon setToplayer(String toplayer) {
    if (toplayer.length() > 2000)
      throw new IllegalArgumentException(
          String.format("toplayer.length() cannot be > 2000.  toplayer=%s", toplayer));
    this.toplayer = toplayer;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * -
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 64
   */
  public Polygon setAuth(String auth) {
    if (auth.length() > 64)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 64.  auth=%s", auth));
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
    return "GMP";
  }

}
