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
 * tomo_info
 */
public class Tomo_info extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Tomographic identifier.
   */
  private long tomoid;

  static final public long TOMOID_NA = Long.MIN_VALUE;

  /**
   * Name of Q0 tomography.
   */
  private String tomoname;

  static final public String TOMONAME_NA = "-";

  /**
   * Minimum geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   */
  private double lat1;

  static final public double LAT1_NA = Double.NaN;

  /**
   * Maximum geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   */
  private double lat2;

  static final public double LAT2_NA = Double.NaN;

  /**
   * Geographic latitude interval for a grid (see also lat1, lat2)
   * <p>
   * Units: degree
   */
  private double dlat;

  static final public double DLAT_NA = Double.NaN;

  /**
   * Minimum geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   */
  private double lon1;

  static final public double LON1_NA = Double.NaN;

  /**
   * Maximum geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   */
  private double lon2;

  static final public double LON2_NA = Double.NaN;

  /**
   * Geographic longitude interval for a grid (see also lon1, lon2)
   * <p>
   * Units: degree
   */
  private double dlon;

  static final public double DLON_NA = Double.NaN;

  /**
   * The directory name that contains the flat file information. For the <B>tomo_info</B> table,
   * this is the directory where the Q0 tomography information is located.
   */
  private String dir;

  static final public String DIR_NA = null;

  /**
   * The name of the data file that contains the flat file information. For the <B>tomo_info</B>
   * table, this is the data file that contains the Q0 tomography information.
   */
  private String dfile;

  static final public String DFILE_NA = null;

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
    columns.add("tomoid", Columns.FieldType.LONG, "%d");
    columns.add("tomoname", Columns.FieldType.STRING, "%s");
    columns.add("lat1", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("lat2", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("dlat", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("lon1", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("lon2", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("dlon", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("dir", Columns.FieldType.STRING, "%s");
    columns.add("dfile", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Tomo_info(long tomoid, String tomoname, double lat1, double lat2, double dlat, double lon1,
      double lon2, double dlon, String dir, String dfile, String auth, long commid) {
    setValues(tomoid, tomoname, lat1, lat2, dlat, lon1, lon2, dlon, dir, dfile, auth, commid);
  }

  private void setValues(long tomoid, String tomoname, double lat1, double lat2, double dlat,
      double lon1, double lon2, double dlon, String dir, String dfile, String auth, long commid) {
    this.tomoid = tomoid;
    this.tomoname = tomoname;
    this.lat1 = lat1;
    this.lat2 = lat2;
    this.dlat = dlat;
    this.lon1 = lon1;
    this.lon2 = lon2;
    this.dlon = dlon;
    this.dir = dir;
    this.dfile = dfile;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Tomo_info(Tomo_info other) {
    this.tomoid = other.getTomoid();
    this.tomoname = other.getTomoname();
    this.lat1 = other.getLat1();
    this.lat2 = other.getLat2();
    this.dlat = other.getDlat();
    this.lon1 = other.getLon1();
    this.lon2 = other.getLon2();
    this.dlon = other.getDlon();
    this.dir = other.getDir();
    this.dfile = other.getDfile();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Tomo_info() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(TOMOID_NA, TOMONAME_NA, LAT1_NA, LAT2_NA, DLAT_NA, LON1_NA, LON2_NA, DLON_NA, DIR_NA,
        DFILE_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "tomoname":
        return tomoname;
      case "dir":
        return dir;
      case "dfile":
        return dfile;
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
      case "tomoname":
        tomoname = value;
        break;
      case "dir":
        dir = value;
        break;
      case "dfile":
        dfile = value;
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
      case "lat1":
        return lat1;
      case "lat2":
        return lat2;
      case "dlat":
        return dlat;
      case "lon1":
        return lon1;
      case "lon2":
        return lon2;
      case "dlon":
        return dlon;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "lat1":
        lat1 = value;
        break;
      case "lat2":
        lat2 = value;
        break;
      case "dlat":
        dlat = value;
        break;
      case "lon1":
        lon1 = value;
        break;
      case "lon2":
        lon2 = value;
        break;
      case "dlon":
        dlon = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "tomoid":
        return tomoid;
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
      case "tomoid":
        tomoid = value;
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
  public Tomo_info(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Tomo_info(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        readString(input), readString(input), readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Tomo_info(ByteBuffer input) {
    this(input.getLong(), readString(input), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        readString(input), readString(input), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Tomo_info(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Tomo_info(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getString(offset + 9),
        input.getString(offset + 10), input.getString(offset + 11), input.getLong(offset + 12));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[12];
    values[0] = tomoid;
    values[1] = tomoname;
    values[2] = lat1;
    values[3] = lat2;
    values[4] = dlat;
    values[5] = lon1;
    values[6] = lon2;
    values[7] = dlon;
    values[8] = dir;
    values[9] = dfile;
    values[10] = auth;
    values[11] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[13];
    values[0] = tomoid;
    values[1] = tomoname;
    values[2] = lat1;
    values[3] = lat2;
    values[4] = dlat;
    values[5] = lon1;
    values[6] = lon2;
    values[7] = dlon;
    values[8] = dir;
    values[9] = dfile;
    values[10] = auth;
    values[11] = commid;
    values[12] = lddate;
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
    output.writeLong(tomoid);
    writeString(output, tomoname);
    output.writeDouble(lat1);
    output.writeDouble(lat2);
    output.writeDouble(dlat);
    output.writeDouble(lon1);
    output.writeDouble(lon2);
    output.writeDouble(dlon);
    writeString(output, dir);
    writeString(output, dfile);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(tomoid);
    writeString(output, tomoname);
    output.putDouble(lat1);
    output.putDouble(lat2);
    output.putDouble(dlat);
    output.putDouble(lon1);
    output.putDouble(lon2);
    output.putDouble(dlon);
    writeString(output, dir);
    writeString(output, dfile);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Tomo_info objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Tomo_info objects.
   * @throws IOException
   */
  static public void readTomo_infos(BufferedReader input, Collection<Tomo_info> rows)
      throws IOException {
    String[] saved = Tomo_info.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Tomo_info
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Tomo_info(new Scanner(line)));
    }
    input.close();
    Tomo_info.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Tomo_info objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Tomo_info objects.
   * @throws IOException
   */
  static public void readTomo_infos(File inputFile, Collection<Tomo_info> rows) throws IOException {
    readTomo_infos(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Tomo_info objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Tomo_info objects.
   * @throws IOException
   */
  static public void readTomo_infos(InputStream inputStream, Collection<Tomo_info> rows)
      throws IOException {
    readTomo_infos(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Tomo_info objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Tomo_info objects
   * @throws IOException
   */
  static public Set<Tomo_info> readTomo_infos(BufferedReader input) throws IOException {
    Set<Tomo_info> rows = new LinkedHashSet<Tomo_info>();
    readTomo_infos(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Tomo_info objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Tomo_info objects
   * @throws IOException
   */
  static public Set<Tomo_info> readTomo_infos(File inputFile) throws IOException {
    return readTomo_infos(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Tomo_info objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Tomo_info objects
   * @throws IOException
   */
  static public Set<Tomo_info> readTomo_infos(InputStream input) throws IOException {
    return readTomo_infos(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Tomo_info objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param tomo_infos the Tomo_info objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Tomo_info> tomo_infos)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Tomo_info tomo_info : tomo_infos)
      tomo_info.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Tomo_info objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param tomo_infos the Tomo_info objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Tomo_info> tomo_infos, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Tomo_info tomo_info : tomo_infos) {
        int i = 0;
        statement.setLong(++i, tomo_info.tomoid);
        statement.setString(++i, tomo_info.tomoname);
        statement.setDouble(++i, tomo_info.lat1);
        statement.setDouble(++i, tomo_info.lat2);
        statement.setDouble(++i, tomo_info.dlat);
        statement.setDouble(++i, tomo_info.lon1);
        statement.setDouble(++i, tomo_info.lon2);
        statement.setDouble(++i, tomo_info.dlon);
        statement.setString(++i, tomo_info.dir);
        statement.setString(++i, tomo_info.dfile);
        statement.setString(++i, tomo_info.auth);
        statement.setLong(++i, tomo_info.commid);
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
   *        Tomo_info table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Tomo_info> readTomo_infos(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Tomo_info> results = new HashSet<Tomo_info>();
    readTomo_infos(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Tomo_info table.
   * @param tomo_infos
   * @throws SQLException
   */
  static public void readTomo_infos(Connection connection, String selectStatement,
      Set<Tomo_info> tomo_infos) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        tomo_infos.add(new Tomo_info(rs));
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
   * this Tomo_info object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Tomo_info object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "tomoid, tomoname, lat1, lat2, dlat, lon1, lon2, dlon, dir, dfile, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(tomoid)).append(", ");
    sql.append("'").append(tomoname).append("', ");
    sql.append(Double.toString(lat1)).append(", ");
    sql.append(Double.toString(lat2)).append(", ");
    sql.append(Double.toString(dlat)).append(", ");
    sql.append(Double.toString(lon1)).append(", ");
    sql.append(Double.toString(lon2)).append(", ");
    sql.append(Double.toString(dlon)).append(", ");
    sql.append("'").append(dir).append("', ");
    sql.append("'").append(dfile).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Tomo_info in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Tomo_info in the database
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
   * Generate a sql script to create a table of type Tomo_info in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Tomo_info in the database
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
    buf.append("tomoid       number(9)            NOT NULL,\n");
    buf.append("tomoname     varchar2(30)         NOT NULL,\n");
    buf.append("lat1         float(53)            NOT NULL,\n");
    buf.append("lat2         float(53)            NOT NULL,\n");
    buf.append("dlat         float(53)            NOT NULL,\n");
    buf.append("lon1         float(53)            NOT NULL,\n");
    buf.append("lon2         float(53)            NOT NULL,\n");
    buf.append("dlon         float(53)            NOT NULL,\n");
    buf.append("dir          varchar2(64)         NOT NULL,\n");
    buf.append("dfile        varchar2(32)         NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (tomoid)");
    if (includeUniqueKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_uk unique (dir,dfile)");
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
    return 226;
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
    return (other instanceof Tomo_info) && ((Tomo_info) other).tomoid == tomoid;
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
    return (other instanceof Tomo_info) && ((Tomo_info) other).dir.equals(dir)
        && ((Tomo_info) other).dfile.equals(dfile);
  }

  /**
   * Tomographic identifier.
   * 
   * @return tomoid
   */
  public long getTomoid() {
    return tomoid;
  }

  /**
   * Tomographic identifier.
   * 
   * @param tomoid
   * @throws IllegalArgumentException if tomoid >= 1000000000
   */
  public Tomo_info setTomoid(long tomoid) {
    if (tomoid >= 1000000000L)
      throw new IllegalArgumentException("tomoid=" + tomoid + " but cannot be >= 1000000000");
    this.tomoid = tomoid;
    setHash(null);
    return this;
  }

  /**
   * Name of Q0 tomography.
   * 
   * @return tomoname
   */
  public String getTomoname() {
    return tomoname;
  }

  /**
   * Name of Q0 tomography.
   * 
   * @param tomoname
   * @throws IllegalArgumentException if tomoname.length() >= 30
   */
  public Tomo_info setTomoname(String tomoname) {
    if (tomoname.length() > 30)
      throw new IllegalArgumentException(
          String.format("tomoname.length() cannot be > 30.  tomoname=%s", tomoname));
    this.tomoname = tomoname;
    setHash(null);
    return this;
  }

  /**
   * Minimum geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   * 
   * @return lat1
   */
  public double getLat1() {
    return lat1;
  }

  /**
   * Minimum geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   * 
   * @param lat1
   */
  public Tomo_info setLat1(double lat1) {
    this.lat1 = lat1;
    setHash(null);
    return this;
  }

  /**
   * Maximum geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   * 
   * @return lat2
   */
  public double getLat2() {
    return lat2;
  }

  /**
   * Maximum geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   * 
   * @param lat2
   */
  public Tomo_info setLat2(double lat2) {
    this.lat2 = lat2;
    setHash(null);
    return this;
  }

  /**
   * Geographic latitude interval for a grid (see also lat1, lat2)
   * <p>
   * Units: degree
   * 
   * @return dlat
   */
  public double getDlat() {
    return dlat;
  }

  /**
   * Geographic latitude interval for a grid (see also lat1, lat2)
   * <p>
   * Units: degree
   * 
   * @param dlat
   */
  public Tomo_info setDlat(double dlat) {
    this.dlat = dlat;
    setHash(null);
    return this;
  }

  /**
   * Minimum geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @return lon1
   */
  public double getLon1() {
    return lon1;
  }

  /**
   * Minimum geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @param lon1
   */
  public Tomo_info setLon1(double lon1) {
    this.lon1 = lon1;
    setHash(null);
    return this;
  }

  /**
   * Maximum geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @return lon2
   */
  public double getLon2() {
    return lon2;
  }

  /**
   * Maximum geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @param lon2
   */
  public Tomo_info setLon2(double lon2) {
    this.lon2 = lon2;
    setHash(null);
    return this;
  }

  /**
   * Geographic longitude interval for a grid (see also lon1, lon2)
   * <p>
   * Units: degree
   * 
   * @return dlon
   */
  public double getDlon() {
    return dlon;
  }

  /**
   * Geographic longitude interval for a grid (see also lon1, lon2)
   * <p>
   * Units: degree
   * 
   * @param dlon
   */
  public Tomo_info setDlon(double dlon) {
    this.dlon = dlon;
    setHash(null);
    return this;
  }

  /**
   * The directory name that contains the flat file information. For the <B>tomo_info</B> table,
   * this is the directory where the Q0 tomography information is located.
   * 
   * @return dir
   */
  public String getDir() {
    return dir;
  }

  /**
   * The directory name that contains the flat file information. For the <B>tomo_info</B> table,
   * this is the directory where the Q0 tomography information is located.
   * 
   * @param dir
   * @throws IllegalArgumentException if dir.length() >= 64
   */
  public Tomo_info setDir(String dir) {
    if (dir.length() > 64)
      throw new IllegalArgumentException(
          String.format("dir.length() cannot be > 64.  dir=%s", dir));
    this.dir = dir;
    setHash(null);
    return this;
  }

  /**
   * The name of the data file that contains the flat file information. For the <B>tomo_info</B>
   * table, this is the data file that contains the Q0 tomography information.
   * 
   * @return dfile
   */
  public String getDfile() {
    return dfile;
  }

  /**
   * The name of the data file that contains the flat file information. For the <B>tomo_info</B>
   * table, this is the data file that contains the Q0 tomography information.
   * 
   * @param dfile
   * @throws IllegalArgumentException if dfile.length() >= 32
   */
  public Tomo_info setDfile(String dfile) {
    if (dfile.length() > 32)
      throw new IllegalArgumentException(
          String.format("dfile.length() cannot be > 32.  dfile=%s", dfile));
    this.dfile = dfile;
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
  public Tomo_info setAuth(String auth) {
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
  public Tomo_info setCommid(long commid) {
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
