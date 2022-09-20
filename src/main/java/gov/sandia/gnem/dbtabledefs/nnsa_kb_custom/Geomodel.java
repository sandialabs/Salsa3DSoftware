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
 * geomodel
 */
public class Geomodel extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Cell number
   */
  private long cellnum;

  static final public long CELLNUM_NA = Long.MIN_VALUE;

  /**
   * Geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   */
  private double lon;

  static final public double LON_NA = Double.NaN;

  /**
   * Geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   */
  private double lat;

  static final public double LAT_NA = Double.NaN;

  /**
   * Model layer number.
   */
  private long layernum;

  static final public long LAYERNUM_NA = Long.MIN_VALUE;

  /**
   * Unique identifier for model layer.
   */
  private long layerid;

  static final public long LAYERID_NA = Long.MIN_VALUE;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = "-";


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("cellnum", Columns.FieldType.LONG, "%d");
    columns.add("lon", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("lat", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("layernum", Columns.FieldType.LONG, "%d");
    columns.add("layerid", Columns.FieldType.LONG, "%d");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Geomodel(long cellnum, double lon, double lat, long layernum, long layerid, String auth) {
    setValues(cellnum, lon, lat, layernum, layerid, auth);
  }

  private void setValues(long cellnum, double lon, double lat, long layernum, long layerid,
      String auth) {
    this.cellnum = cellnum;
    this.lon = lon;
    this.lat = lat;
    this.layernum = layernum;
    this.layerid = layerid;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Geomodel(Geomodel other) {
    this.cellnum = other.getCellnum();
    this.lon = other.getLon();
    this.lat = other.getLat();
    this.layernum = other.getLayernum();
    this.layerid = other.getLayerid();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Geomodel() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(CELLNUM_NA, LON_NA, LAT_NA, LAYERNUM_NA, LAYERID_NA, AUTH_NA);
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
      case "lon":
        return lon;
      case "lat":
        return lat;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "lon":
        lon = value;
        break;
      case "lat":
        lat = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "cellnum":
        return cellnum;
      case "layernum":
        return layernum;
      case "layerid":
        return layerid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "cellnum":
        cellnum = value;
        break;
      case "layernum":
        layernum = value;
        break;
      case "layerid":
        layerid = value;
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
  public Geomodel(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Geomodel(DataInputStream input) throws IOException {
    this(input.readLong(), input.readDouble(), input.readDouble(), input.readLong(),
        input.readLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Geomodel(ByteBuffer input) {
    this(input.getLong(), input.getDouble(), input.getDouble(), input.getLong(), input.getLong(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Geomodel(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Geomodel(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getString(offset + 6));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[6];
    values[0] = cellnum;
    values[1] = lon;
    values[2] = lat;
    values[3] = layernum;
    values[4] = layerid;
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
    values[0] = cellnum;
    values[1] = lon;
    values[2] = lat;
    values[3] = layernum;
    values[4] = layerid;
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
    output.writeLong(cellnum);
    output.writeDouble(lon);
    output.writeDouble(lat);
    output.writeLong(layernum);
    output.writeLong(layerid);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(cellnum);
    output.putDouble(lon);
    output.putDouble(lat);
    output.putLong(layernum);
    output.putLong(layerid);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Geomodel objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Geomodel objects.
   * @throws IOException
   */
  static public void readGeomodels(BufferedReader input, Collection<Geomodel> rows)
      throws IOException {
    String[] saved = Geomodel.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Geomodel
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Geomodel(new Scanner(line)));
    }
    input.close();
    Geomodel.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Geomodel objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Geomodel objects.
   * @throws IOException
   */
  static public void readGeomodels(File inputFile, Collection<Geomodel> rows) throws IOException {
    readGeomodels(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Geomodel objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Geomodel objects.
   * @throws IOException
   */
  static public void readGeomodels(InputStream inputStream, Collection<Geomodel> rows)
      throws IOException {
    readGeomodels(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Geomodel objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Geomodel objects
   * @throws IOException
   */
  static public Set<Geomodel> readGeomodels(BufferedReader input) throws IOException {
    Set<Geomodel> rows = new LinkedHashSet<Geomodel>();
    readGeomodels(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Geomodel objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Geomodel objects
   * @throws IOException
   */
  static public Set<Geomodel> readGeomodels(File inputFile) throws IOException {
    return readGeomodels(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Geomodel objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Geomodel objects
   * @throws IOException
   */
  static public Set<Geomodel> readGeomodels(InputStream input) throws IOException {
    return readGeomodels(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Geomodel objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param geomodels the Geomodel objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Geomodel> geomodels)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Geomodel geomodel : geomodels)
      geomodel.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Geomodel objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param geomodels the Geomodel objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Geomodel> geomodels, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?)");
      for (Geomodel geomodel : geomodels) {
        int i = 0;
        statement.setLong(++i, geomodel.cellnum);
        statement.setDouble(++i, geomodel.lon);
        statement.setDouble(++i, geomodel.lat);
        statement.setLong(++i, geomodel.layernum);
        statement.setLong(++i, geomodel.layerid);
        statement.setString(++i, geomodel.auth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Geomodel
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Geomodel> readGeomodels(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Geomodel> results = new HashSet<Geomodel>();
    readGeomodels(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Geomodel
   *        table.
   * @param geomodels
   * @throws SQLException
   */
  static public void readGeomodels(Connection connection, String selectStatement,
      Set<Geomodel> geomodels) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        geomodels.add(new Geomodel(rs));
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
   * this Geomodel object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Geomodel object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("cellnum, lon, lat, layernum, layerid, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(cellnum)).append(", ");
    sql.append(Double.toString(lon)).append(", ");
    sql.append(Double.toString(lat)).append(", ");
    sql.append(Long.toString(layernum)).append(", ");
    sql.append(Long.toString(layerid)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Geomodel in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Geomodel in the database
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
   * Generate a sql script to create a table of type Geomodel in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Geomodel in the database
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
    buf.append("cellnum      number(8)            NOT NULL,\n");
    buf.append("lon          float(53)            NOT NULL,\n");
    buf.append("lat          float(53)            NOT NULL,\n");
    buf.append("layernum     number(9)            NOT NULL,\n");
    buf.append("layerid      number(9)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (cellnum,layerid)");
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
    return (other instanceof Geomodel) && ((Geomodel) other).cellnum == cellnum
        && ((Geomodel) other).layerid == layerid;
  }

  /**
   * Cell number
   * 
   * @return cellnum
   */
  public long getCellnum() {
    return cellnum;
  }

  /**
   * Cell number
   * 
   * @param cellnum
   * @throws IllegalArgumentException if cellnum >= 100000000
   */
  public Geomodel setCellnum(long cellnum) {
    if (cellnum >= 100000000L)
      throw new IllegalArgumentException("cellnum=" + cellnum + " but cannot be >= 100000000");
    this.cellnum = cellnum;
    setHash(null);
    return this;
  }

  /**
   * Geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @return lon
   */
  public double getLon() {
    return lon;
  }

  /**
   * Geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @param lon
   */
  public Geomodel setLon(double lon) {
    this.lon = lon;
    setHash(null);
    return this;
  }

  /**
   * Geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   * 
   * @return lat
   */
  public double getLat() {
    return lat;
  }

  /**
   * Geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   * 
   * @param lat
   */
  public Geomodel setLat(double lat) {
    this.lat = lat;
    setHash(null);
    return this;
  }

  /**
   * Model layer number.
   * 
   * @return layernum
   */
  public long getLayernum() {
    return layernum;
  }

  /**
   * Model layer number.
   * 
   * @param layernum
   * @throws IllegalArgumentException if layernum >= 1000000000
   */
  public Geomodel setLayernum(long layernum) {
    if (layernum >= 1000000000L)
      throw new IllegalArgumentException("layernum=" + layernum + " but cannot be >= 1000000000");
    this.layernum = layernum;
    setHash(null);
    return this;
  }

  /**
   * Unique identifier for model layer.
   * 
   * @return layerid
   */
  public long getLayerid() {
    return layerid;
  }

  /**
   * Unique identifier for model layer.
   * 
   * @param layerid
   * @throws IllegalArgumentException if layerid >= 1000000000
   */
  public Geomodel setLayerid(long layerid) {
    if (layerid >= 1000000000L)
      throw new IllegalArgumentException("layerid=" + layerid + " but cannot be >= 1000000000");
    this.layerid = layerid;
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
  public Geomodel setAuth(String auth) {
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
