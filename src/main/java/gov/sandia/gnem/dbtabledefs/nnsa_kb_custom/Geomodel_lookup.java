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
 * geomodel_lookup
 */
public class Geomodel_lookup extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique identifier for model layer.
   */
  private long layerid;

  static final public long LAYERID_NA = Long.MIN_VALUE;

  /**
   * Depth to top of velocity model layer
   * <p>
   * Units: km
   */
  private double depth1;

  static final public double DEPTH1_NA = -1;

  /**
   * P wave velocity at top of velocity model layer
   * <p>
   * Units: km/s
   */
  private double pwave1;

  static final public double PWAVE1_NA = Double.NaN;

  /**
   * S wave velocity at top of velocity model layer
   * <p>
   * Units: km/s
   */
  private double swave1;

  static final public double SWAVE1_NA = Double.NaN;

  /**
   * Density at the top of the velocity model layer
   * <p>
   * Units: g/(cm^3)
   */
  private double density1;

  static final public double DENSITY1_NA = -1;

  /**
   * Depth to bottom of velocity model layer
   * <p>
   * Units: km
   */
  private double depth2;

  static final public double DEPTH2_NA = -1;

  /**
   * P wave velocity at bottom of velocity model layer
   * <p>
   * Units: km/s
   */
  private double pwave2;

  static final public double PWAVE2_NA = Double.NaN;

  /**
   * S wave velocity at bottom of velocity model layer
   * <p>
   * Units: km/s
   */
  private double swave2;

  static final public double SWAVE2_NA = Double.NaN;

  /**
   * Density at the bottom of the velocity model layer
   * <p>
   * Units: g/(cm^3)
   */
  private double density2;

  static final public double DENSITY2_NA = -1;

  /**
   * P-wave attenuation factor Qp for velocity layer.
   */
  private double qp;

  static final public double QP_NA = -1;

  /**
   * S-wave attenuation factor Qs for velocity layer.
   */
  private double qs;

  static final public double QS_NA = -1;

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
    columns.add("layerid", Columns.FieldType.LONG, "%d");
    columns.add("depth1", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("pwave1", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("swave1", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("density1", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("depth2", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("pwave2", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("swave2", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("density2", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("qp", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("qs", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Geomodel_lookup(long layerid, double depth1, double pwave1, double swave1, double density1,
      double depth2, double pwave2, double swave2, double density2, double qp, double qs,
      String auth) {
    setValues(layerid, depth1, pwave1, swave1, density1, depth2, pwave2, swave2, density2, qp, qs,
        auth);
  }

  private void setValues(long layerid, double depth1, double pwave1, double swave1, double density1,
      double depth2, double pwave2, double swave2, double density2, double qp, double qs,
      String auth) {
    this.layerid = layerid;
    this.depth1 = depth1;
    this.pwave1 = pwave1;
    this.swave1 = swave1;
    this.density1 = density1;
    this.depth2 = depth2;
    this.pwave2 = pwave2;
    this.swave2 = swave2;
    this.density2 = density2;
    this.qp = qp;
    this.qs = qs;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Geomodel_lookup(Geomodel_lookup other) {
    this.layerid = other.getLayerid();
    this.depth1 = other.getDepth1();
    this.pwave1 = other.getPwave1();
    this.swave1 = other.getSwave1();
    this.density1 = other.getDensity1();
    this.depth2 = other.getDepth2();
    this.pwave2 = other.getPwave2();
    this.swave2 = other.getSwave2();
    this.density2 = other.getDensity2();
    this.qp = other.getQp();
    this.qs = other.getQs();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Geomodel_lookup() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(LAYERID_NA, DEPTH1_NA, PWAVE1_NA, SWAVE1_NA, DENSITY1_NA, DEPTH2_NA, PWAVE2_NA,
        SWAVE2_NA, DENSITY2_NA, QP_NA, QS_NA, AUTH_NA);
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
      case "depth1":
        return depth1;
      case "pwave1":
        return pwave1;
      case "swave1":
        return swave1;
      case "density1":
        return density1;
      case "depth2":
        return depth2;
      case "pwave2":
        return pwave2;
      case "swave2":
        return swave2;
      case "density2":
        return density2;
      case "qp":
        return qp;
      case "qs":
        return qs;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "depth1":
        depth1 = value;
        break;
      case "pwave1":
        pwave1 = value;
        break;
      case "swave1":
        swave1 = value;
        break;
      case "density1":
        density1 = value;
        break;
      case "depth2":
        depth2 = value;
        break;
      case "pwave2":
        pwave2 = value;
        break;
      case "swave2":
        swave2 = value;
        break;
      case "density2":
        density2 = value;
        break;
      case "qp":
        qp = value;
        break;
      case "qs":
        qs = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
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
  public Geomodel_lookup(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Geomodel_lookup(DataInputStream input) throws IOException {
    this(input.readLong(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Geomodel_lookup(ByteBuffer input) {
    this(input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Geomodel_lookup(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Geomodel_lookup(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getString(offset + 12));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[12];
    values[0] = layerid;
    values[1] = depth1;
    values[2] = pwave1;
    values[3] = swave1;
    values[4] = density1;
    values[5] = depth2;
    values[6] = pwave2;
    values[7] = swave2;
    values[8] = density2;
    values[9] = qp;
    values[10] = qs;
    values[11] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[13];
    values[0] = layerid;
    values[1] = depth1;
    values[2] = pwave1;
    values[3] = swave1;
    values[4] = density1;
    values[5] = depth2;
    values[6] = pwave2;
    values[7] = swave2;
    values[8] = density2;
    values[9] = qp;
    values[10] = qs;
    values[11] = auth;
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
    output.writeLong(layerid);
    output.writeDouble(depth1);
    output.writeDouble(pwave1);
    output.writeDouble(swave1);
    output.writeDouble(density1);
    output.writeDouble(depth2);
    output.writeDouble(pwave2);
    output.writeDouble(swave2);
    output.writeDouble(density2);
    output.writeDouble(qp);
    output.writeDouble(qs);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(layerid);
    output.putDouble(depth1);
    output.putDouble(pwave1);
    output.putDouble(swave1);
    output.putDouble(density1);
    output.putDouble(depth2);
    output.putDouble(pwave2);
    output.putDouble(swave2);
    output.putDouble(density2);
    output.putDouble(qp);
    output.putDouble(qs);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Geomodel_lookup objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Geomodel_lookup objects.
   * @throws IOException
   */
  static public void readGeomodel_lookups(BufferedReader input, Collection<Geomodel_lookup> rows)
      throws IOException {
    String[] saved = Geomodel_lookup.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Geomodel_lookup
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Geomodel_lookup(new Scanner(line)));
    }
    input.close();
    Geomodel_lookup.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Geomodel_lookup objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Geomodel_lookup objects.
   * @throws IOException
   */
  static public void readGeomodel_lookups(File inputFile, Collection<Geomodel_lookup> rows)
      throws IOException {
    readGeomodel_lookups(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Geomodel_lookup objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Geomodel_lookup objects.
   * @throws IOException
   */
  static public void readGeomodel_lookups(InputStream inputStream, Collection<Geomodel_lookup> rows)
      throws IOException {
    readGeomodel_lookups(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Geomodel_lookup objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Geomodel_lookup objects
   * @throws IOException
   */
  static public Set<Geomodel_lookup> readGeomodel_lookups(BufferedReader input) throws IOException {
    Set<Geomodel_lookup> rows = new LinkedHashSet<Geomodel_lookup>();
    readGeomodel_lookups(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Geomodel_lookup objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Geomodel_lookup objects
   * @throws IOException
   */
  static public Set<Geomodel_lookup> readGeomodel_lookups(File inputFile) throws IOException {
    return readGeomodel_lookups(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Geomodel_lookup objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Geomodel_lookup objects
   * @throws IOException
   */
  static public Set<Geomodel_lookup> readGeomodel_lookups(InputStream input) throws IOException {
    return readGeomodel_lookups(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Geomodel_lookup objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param geomodel_lookups the Geomodel_lookup objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Geomodel_lookup> geomodel_lookups)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Geomodel_lookup geomodel_lookup : geomodel_lookups)
      geomodel_lookup.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Geomodel_lookup objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param geomodel_lookups the Geomodel_lookup objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Geomodel_lookup> geomodel_lookups, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Geomodel_lookup geomodel_lookup : geomodel_lookups) {
        int i = 0;
        statement.setLong(++i, geomodel_lookup.layerid);
        statement.setDouble(++i, geomodel_lookup.depth1);
        statement.setDouble(++i, geomodel_lookup.pwave1);
        statement.setDouble(++i, geomodel_lookup.swave1);
        statement.setDouble(++i, geomodel_lookup.density1);
        statement.setDouble(++i, geomodel_lookup.depth2);
        statement.setDouble(++i, geomodel_lookup.pwave2);
        statement.setDouble(++i, geomodel_lookup.swave2);
        statement.setDouble(++i, geomodel_lookup.density2);
        statement.setDouble(++i, geomodel_lookup.qp);
        statement.setDouble(++i, geomodel_lookup.qs);
        statement.setString(++i, geomodel_lookup.auth);
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
   *        Geomodel_lookup table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Geomodel_lookup> readGeomodel_lookups(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Geomodel_lookup> results = new HashSet<Geomodel_lookup>();
    readGeomodel_lookups(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Geomodel_lookup table.
   * @param geomodel_lookups
   * @throws SQLException
   */
  static public void readGeomodel_lookups(Connection connection, String selectStatement,
      Set<Geomodel_lookup> geomodel_lookups) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        geomodel_lookups.add(new Geomodel_lookup(rs));
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
   * this Geomodel_lookup object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Geomodel_lookup object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "layerid, depth1, pwave1, swave1, density1, depth2, pwave2, swave2, density2, qp, qs, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(layerid)).append(", ");
    sql.append(Double.toString(depth1)).append(", ");
    sql.append(Double.toString(pwave1)).append(", ");
    sql.append(Double.toString(swave1)).append(", ");
    sql.append(Double.toString(density1)).append(", ");
    sql.append(Double.toString(depth2)).append(", ");
    sql.append(Double.toString(pwave2)).append(", ");
    sql.append(Double.toString(swave2)).append(", ");
    sql.append(Double.toString(density2)).append(", ");
    sql.append(Double.toString(qp)).append(", ");
    sql.append(Double.toString(qs)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Geomodel_lookup in the database. Primary and unique keys are set, if
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
   * Create a table of type Geomodel_lookup in the database
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
   * Generate a sql script to create a table of type Geomodel_lookup in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Geomodel_lookup in the database
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
    buf.append("layerid      number(9)            NOT NULL,\n");
    buf.append("depth1       float(24)            NOT NULL,\n");
    buf.append("pwave1       float(24)            NOT NULL,\n");
    buf.append("swave1       float(24)            NOT NULL,\n");
    buf.append("density1     float(24)            NOT NULL,\n");
    buf.append("depth2       float(24)            NOT NULL,\n");
    buf.append("pwave2       float(24)            NOT NULL,\n");
    buf.append("swave2       float(24)            NOT NULL,\n");
    buf.append("density2     float(24)            NOT NULL,\n");
    buf.append("qp           float(24)            NOT NULL,\n");
    buf.append("qs           float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (layerid)");
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
    return 112;
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
    return (other instanceof Geomodel_lookup) && ((Geomodel_lookup) other).layerid == layerid;
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
  public Geomodel_lookup setLayerid(long layerid) {
    if (layerid >= 1000000000L)
      throw new IllegalArgumentException("layerid=" + layerid + " but cannot be >= 1000000000");
    this.layerid = layerid;
    setHash(null);
    return this;
  }

  /**
   * Depth to top of velocity model layer
   * <p>
   * Units: km
   * 
   * @return depth1
   */
  public double getDepth1() {
    return depth1;
  }

  /**
   * Depth to top of velocity model layer
   * <p>
   * Units: km
   * 
   * @param depth1
   */
  public Geomodel_lookup setDepth1(double depth1) {
    this.depth1 = depth1;
    setHash(null);
    return this;
  }

  /**
   * P wave velocity at top of velocity model layer
   * <p>
   * Units: km/s
   * 
   * @return pwave1
   */
  public double getPwave1() {
    return pwave1;
  }

  /**
   * P wave velocity at top of velocity model layer
   * <p>
   * Units: km/s
   * 
   * @param pwave1
   */
  public Geomodel_lookup setPwave1(double pwave1) {
    this.pwave1 = pwave1;
    setHash(null);
    return this;
  }

  /**
   * S wave velocity at top of velocity model layer
   * <p>
   * Units: km/s
   * 
   * @return swave1
   */
  public double getSwave1() {
    return swave1;
  }

  /**
   * S wave velocity at top of velocity model layer
   * <p>
   * Units: km/s
   * 
   * @param swave1
   */
  public Geomodel_lookup setSwave1(double swave1) {
    this.swave1 = swave1;
    setHash(null);
    return this;
  }

  /**
   * Density at the top of the velocity model layer
   * <p>
   * Units: g/(cm^3)
   * 
   * @return density1
   */
  public double getDensity1() {
    return density1;
  }

  /**
   * Density at the top of the velocity model layer
   * <p>
   * Units: g/(cm^3)
   * 
   * @param density1
   */
  public Geomodel_lookup setDensity1(double density1) {
    this.density1 = density1;
    setHash(null);
    return this;
  }

  /**
   * Depth to bottom of velocity model layer
   * <p>
   * Units: km
   * 
   * @return depth2
   */
  public double getDepth2() {
    return depth2;
  }

  /**
   * Depth to bottom of velocity model layer
   * <p>
   * Units: km
   * 
   * @param depth2
   */
  public Geomodel_lookup setDepth2(double depth2) {
    this.depth2 = depth2;
    setHash(null);
    return this;
  }

  /**
   * P wave velocity at bottom of velocity model layer
   * <p>
   * Units: km/s
   * 
   * @return pwave2
   */
  public double getPwave2() {
    return pwave2;
  }

  /**
   * P wave velocity at bottom of velocity model layer
   * <p>
   * Units: km/s
   * 
   * @param pwave2
   */
  public Geomodel_lookup setPwave2(double pwave2) {
    this.pwave2 = pwave2;
    setHash(null);
    return this;
  }

  /**
   * S wave velocity at bottom of velocity model layer
   * <p>
   * Units: km/s
   * 
   * @return swave2
   */
  public double getSwave2() {
    return swave2;
  }

  /**
   * S wave velocity at bottom of velocity model layer
   * <p>
   * Units: km/s
   * 
   * @param swave2
   */
  public Geomodel_lookup setSwave2(double swave2) {
    this.swave2 = swave2;
    setHash(null);
    return this;
  }

  /**
   * Density at the bottom of the velocity model layer
   * <p>
   * Units: g/(cm^3)
   * 
   * @return density2
   */
  public double getDensity2() {
    return density2;
  }

  /**
   * Density at the bottom of the velocity model layer
   * <p>
   * Units: g/(cm^3)
   * 
   * @param density2
   */
  public Geomodel_lookup setDensity2(double density2) {
    this.density2 = density2;
    setHash(null);
    return this;
  }

  /**
   * P-wave attenuation factor Qp for velocity layer.
   * 
   * @return qp
   */
  public double getQp() {
    return qp;
  }

  /**
   * P-wave attenuation factor Qp for velocity layer.
   * 
   * @param qp
   */
  public Geomodel_lookup setQp(double qp) {
    this.qp = qp;
    setHash(null);
    return this;
  }

  /**
   * S-wave attenuation factor Qs for velocity layer.
   * 
   * @return qs
   */
  public double getQs() {
    return qs;
  }

  /**
   * S-wave attenuation factor Qs for velocity layer.
   * 
   * @param qs
   */
  public Geomodel_lookup setQs(double qs) {
    this.qs = qs;
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
  public Geomodel_lookup setAuth(String auth) {
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
