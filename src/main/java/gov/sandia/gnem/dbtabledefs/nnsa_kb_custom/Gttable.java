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
 * gttable
 */
public class Gttable extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Identifier for ground truth source.
   */
  private long gtsid;

  static final public long GTSID_NA = Long.MIN_VALUE;

  /**
   * Evid of ground truth source in the master <B>origin</B> table (reconciled <I>evid</I> and
   * <I>orid</I>)
   */
  private long masterEvid;

  static final public long MASTEREVID_NA = Long.MIN_VALUE;

  /**
   * Orid of ground truth source in the master <B>origin</B> table (reconciled <I>evid</I> and
   * <I>orid</I>)
   */
  private long masterOrid;

  static final public long MASTERORID_NA = Long.MIN_VALUE;

  /**
   * <I>Evid</I> of ground-truth source in native <B>origin</B> table
   */
  private long nativeEvid;

  static final public long NATIVEEVID_NA = Long.MIN_VALUE;

  /**
   * <I>Orid</I> of ground-truth source in native <B>origin</B> table
   */
  private long nativeOrid;

  static final public long NATIVEORID_NA = Long.MIN_VALUE;

  /**
   * Preferred event origin for ground-truth source in the master <B>origin</B> table.
   */
  private long prefOrid;

  static final public long PREFORID_NA = Long.MIN_VALUE;

  /**
   * Two sigma epicenter accuracy of ground truth location.
   * <p>
   * Units: km
   */
  private double gtEpicenter;

  static final public double GTEPICENTER_NA = Double.NaN;

  /**
   * Two sigma depth accuracy of ground truth location.
   * <p>
   * Units: km
   */
  private double gtDepth;

  static final public double GTDEPTH_NA = Double.NaN;

  /**
   * Two sigma origin time accuracy of ground truth location.
   * <p>
   * Units: s
   */
  private double gtOT;

  static final public double GTOT_NA = Double.NaN;

  /**
   * Name of master <B>origin</B> table for ground truth source
   */
  private String masterOriginName;

  static final public String MASTERORIGINNAME_NA = null;

  /**
   * Name of native origin table for ground-truth source.
   */
  private String nativeOriginName;

  static final public String NATIVEORIGINNAME_NA = "-";

  /**
   * Epicenter criterion or method for ground truth source
   */
  private String methodEpicenter;

  static final public String METHODEPICENTER_NA = "-";

  /**
   * Depth criterion or method for ground truth source
   */
  private String methodDepth;

  static final public String METHODDEPTH_NA = "-";

  /**
   * Origin time criterion or method for ground truth source
   */
  private String methodOT;

  static final public String METHODOT_NA = "-";

  /**
   * Reference identifier for linking to supporting information about the ground-truth source.
   */
  private long refid;

  static final public long REFID_NA = -1;

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
    columns.add("gtsid", Columns.FieldType.LONG, "%d");
    columns.add("masterEvid", Columns.FieldType.LONG, "%d");
    columns.add("masterOrid", Columns.FieldType.LONG, "%d");
    columns.add("nativeEvid", Columns.FieldType.LONG, "%d");
    columns.add("nativeOrid", Columns.FieldType.LONG, "%d");
    columns.add("prefOrid", Columns.FieldType.LONG, "%d");
    columns.add("gtEpicenter", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("gtDepth", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("gtOT", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("masterOriginName", Columns.FieldType.STRING, "%s");
    columns.add("nativeOriginName", Columns.FieldType.STRING, "%s");
    columns.add("methodEpicenter", Columns.FieldType.STRING, "%s");
    columns.add("methodDepth", Columns.FieldType.STRING, "%s");
    columns.add("methodOT", Columns.FieldType.STRING, "%s");
    columns.add("refid", Columns.FieldType.LONG, "%d");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Gttable(long gtsid, long masterEvid, long masterOrid, long nativeEvid, long nativeOrid,
      long prefOrid, double gtEpicenter, double gtDepth, double gtOT, String masterOriginName,
      String nativeOriginName, String methodEpicenter, String methodDepth, String methodOT,
      long refid, String auth) {
    setValues(gtsid, masterEvid, masterOrid, nativeEvid, nativeOrid, prefOrid, gtEpicenter, gtDepth,
        gtOT, masterOriginName, nativeOriginName, methodEpicenter, methodDepth, methodOT, refid,
        auth);
  }

  private void setValues(long gtsid, long masterEvid, long masterOrid, long nativeEvid,
      long nativeOrid, long prefOrid, double gtEpicenter, double gtDepth, double gtOT,
      String masterOriginName, String nativeOriginName, String methodEpicenter, String methodDepth,
      String methodOT, long refid, String auth) {
    this.gtsid = gtsid;
    this.masterEvid = masterEvid;
    this.masterOrid = masterOrid;
    this.nativeEvid = nativeEvid;
    this.nativeOrid = nativeOrid;
    this.prefOrid = prefOrid;
    this.gtEpicenter = gtEpicenter;
    this.gtDepth = gtDepth;
    this.gtOT = gtOT;
    this.masterOriginName = masterOriginName;
    this.nativeOriginName = nativeOriginName;
    this.methodEpicenter = methodEpicenter;
    this.methodDepth = methodDepth;
    this.methodOT = methodOT;
    this.refid = refid;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Gttable(Gttable other) {
    this.gtsid = other.getGtsid();
    this.masterEvid = other.getMasterEvid();
    this.masterOrid = other.getMasterOrid();
    this.nativeEvid = other.getNativeEvid();
    this.nativeOrid = other.getNativeOrid();
    this.prefOrid = other.getPrefOrid();
    this.gtEpicenter = other.getGtEpicenter();
    this.gtDepth = other.getGtDepth();
    this.gtOT = other.getGtOT();
    this.masterOriginName = other.getMasterOriginName();
    this.nativeOriginName = other.getNativeOriginName();
    this.methodEpicenter = other.getMethodEpicenter();
    this.methodDepth = other.getMethodDepth();
    this.methodOT = other.getMethodOT();
    this.refid = other.getRefid();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Gttable() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(GTSID_NA, MASTEREVID_NA, MASTERORID_NA, NATIVEEVID_NA, NATIVEORID_NA, PREFORID_NA,
        GTEPICENTER_NA, GTDEPTH_NA, GTOT_NA, MASTERORIGINNAME_NA, NATIVEORIGINNAME_NA,
        METHODEPICENTER_NA, METHODDEPTH_NA, METHODOT_NA, REFID_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "masterOriginName":
        return masterOriginName;
      case "nativeOriginName":
        return nativeOriginName;
      case "methodEpicenter":
        return methodEpicenter;
      case "methodDepth":
        return methodDepth;
      case "methodOT":
        return methodOT;
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
      case "masterOriginName":
        masterOriginName = value;
        break;
      case "nativeOriginName":
        nativeOriginName = value;
        break;
      case "methodEpicenter":
        methodEpicenter = value;
        break;
      case "methodDepth":
        methodDepth = value;
        break;
      case "methodOT":
        methodOT = value;
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
      case "gtEpicenter":
        return gtEpicenter;
      case "gtDepth":
        return gtDepth;
      case "gtOT":
        return gtOT;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "gtEpicenter":
        gtEpicenter = value;
        break;
      case "gtDepth":
        gtDepth = value;
        break;
      case "gtOT":
        gtOT = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "gtsid":
        return gtsid;
      case "masterEvid":
        return masterEvid;
      case "masterOrid":
        return masterOrid;
      case "nativeEvid":
        return nativeEvid;
      case "nativeOrid":
        return nativeOrid;
      case "prefOrid":
        return prefOrid;
      case "refid":
        return refid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "gtsid":
        gtsid = value;
        break;
      case "masterEvid":
        masterEvid = value;
        break;
      case "masterOrid":
        masterOrid = value;
        break;
      case "nativeEvid":
        nativeEvid = value;
        break;
      case "nativeOrid":
        nativeOrid = value;
        break;
      case "prefOrid":
        prefOrid = value;
        break;
      case "refid":
        refid = value;
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
  public Gttable(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Gttable(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        input.readLong(), input.readDouble(), input.readDouble(), input.readDouble(),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), input.readLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Gttable(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getLong(),
        input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(), readString(input),
        readString(input), readString(input), readString(input), readString(input), input.getLong(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Gttable(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Gttable(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getString(offset + 10), input.getString(offset + 11), input.getString(offset + 12),
        input.getString(offset + 13), input.getString(offset + 14), input.getLong(offset + 15),
        input.getString(offset + 16));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[16];
    values[0] = gtsid;
    values[1] = masterEvid;
    values[2] = masterOrid;
    values[3] = nativeEvid;
    values[4] = nativeOrid;
    values[5] = prefOrid;
    values[6] = gtEpicenter;
    values[7] = gtDepth;
    values[8] = gtOT;
    values[9] = masterOriginName;
    values[10] = nativeOriginName;
    values[11] = methodEpicenter;
    values[12] = methodDepth;
    values[13] = methodOT;
    values[14] = refid;
    values[15] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[17];
    values[0] = gtsid;
    values[1] = masterEvid;
    values[2] = masterOrid;
    values[3] = nativeEvid;
    values[4] = nativeOrid;
    values[5] = prefOrid;
    values[6] = gtEpicenter;
    values[7] = gtDepth;
    values[8] = gtOT;
    values[9] = masterOriginName;
    values[10] = nativeOriginName;
    values[11] = methodEpicenter;
    values[12] = methodDepth;
    values[13] = methodOT;
    values[14] = refid;
    values[15] = auth;
    values[16] = lddate;
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
    output.writeLong(gtsid);
    output.writeLong(masterEvid);
    output.writeLong(masterOrid);
    output.writeLong(nativeEvid);
    output.writeLong(nativeOrid);
    output.writeLong(prefOrid);
    output.writeDouble(gtEpicenter);
    output.writeDouble(gtDepth);
    output.writeDouble(gtOT);
    writeString(output, masterOriginName);
    writeString(output, nativeOriginName);
    writeString(output, methodEpicenter);
    writeString(output, methodDepth);
    writeString(output, methodOT);
    output.writeLong(refid);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(gtsid);
    output.putLong(masterEvid);
    output.putLong(masterOrid);
    output.putLong(nativeEvid);
    output.putLong(nativeOrid);
    output.putLong(prefOrid);
    output.putDouble(gtEpicenter);
    output.putDouble(gtDepth);
    output.putDouble(gtOT);
    writeString(output, masterOriginName);
    writeString(output, nativeOriginName);
    writeString(output, methodEpicenter);
    writeString(output, methodDepth);
    writeString(output, methodOT);
    output.putLong(refid);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Gttable objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Gttable objects.
   * @throws IOException
   */
  static public void readGttables(BufferedReader input, Collection<Gttable> rows)
      throws IOException {
    String[] saved = Gttable.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Gttable.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Gttable(new Scanner(line)));
    }
    input.close();
    Gttable.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Gttable objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Gttable objects.
   * @throws IOException
   */
  static public void readGttables(File inputFile, Collection<Gttable> rows) throws IOException {
    readGttables(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Gttable objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Gttable objects.
   * @throws IOException
   */
  static public void readGttables(InputStream inputStream, Collection<Gttable> rows)
      throws IOException {
    readGttables(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Gttable objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Gttable objects
   * @throws IOException
   */
  static public Set<Gttable> readGttables(BufferedReader input) throws IOException {
    Set<Gttable> rows = new LinkedHashSet<Gttable>();
    readGttables(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Gttable objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Gttable objects
   * @throws IOException
   */
  static public Set<Gttable> readGttables(File inputFile) throws IOException {
    return readGttables(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Gttable objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Gttable objects
   * @throws IOException
   */
  static public Set<Gttable> readGttables(InputStream input) throws IOException {
    return readGttables(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Gttable objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param gttables the Gttable objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Gttable> gttables)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Gttable gttable : gttables)
      gttable.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Gttable objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param gttables the Gttable objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Gttable> gttables, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Gttable gttable : gttables) {
        int i = 0;
        statement.setLong(++i, gttable.gtsid);
        statement.setLong(++i, gttable.masterEvid);
        statement.setLong(++i, gttable.masterOrid);
        statement.setLong(++i, gttable.nativeEvid);
        statement.setLong(++i, gttable.nativeOrid);
        statement.setLong(++i, gttable.prefOrid);
        statement.setDouble(++i, gttable.gtEpicenter);
        statement.setDouble(++i, gttable.gtDepth);
        statement.setDouble(++i, gttable.gtOT);
        statement.setString(++i, gttable.masterOriginName);
        statement.setString(++i, gttable.nativeOriginName);
        statement.setString(++i, gttable.methodEpicenter);
        statement.setString(++i, gttable.methodDepth);
        statement.setString(++i, gttable.methodOT);
        statement.setLong(++i, gttable.refid);
        statement.setString(++i, gttable.auth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Gttable
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Gttable> readGttables(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Gttable> results = new HashSet<Gttable>();
    readGttables(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Gttable
   *        table.
   * @param gttables
   * @throws SQLException
   */
  static public void readGttables(Connection connection, String selectStatement,
      Set<Gttable> gttables) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        gttables.add(new Gttable(rs));
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
   * this Gttable object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Gttable object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "gtsid, masterEvid, masterOrid, nativeEvid, nativeOrid, prefOrid, gtEpicenter, gtDepth, gtOT, masterOriginName, nativeOriginName, methodEpicenter, methodDepth, methodOT, refid, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(gtsid)).append(", ");
    sql.append(Long.toString(masterEvid)).append(", ");
    sql.append(Long.toString(masterOrid)).append(", ");
    sql.append(Long.toString(nativeEvid)).append(", ");
    sql.append(Long.toString(nativeOrid)).append(", ");
    sql.append(Long.toString(prefOrid)).append(", ");
    sql.append(Double.toString(gtEpicenter)).append(", ");
    sql.append(Double.toString(gtDepth)).append(", ");
    sql.append(Double.toString(gtOT)).append(", ");
    sql.append("'").append(masterOriginName).append("', ");
    sql.append("'").append(nativeOriginName).append("', ");
    sql.append("'").append(methodEpicenter).append("', ");
    sql.append("'").append(methodDepth).append("', ");
    sql.append("'").append(methodOT).append("', ");
    sql.append(Long.toString(refid)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Gttable in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Gttable in the database
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
   * Generate a sql script to create a table of type Gttable in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Gttable in the database
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
    buf.append("gtsid        number(9)            NOT NULL,\n");
    buf.append("masterEvid   number(9)            NOT NULL,\n");
    buf.append("masterOrid   number(9)            NOT NULL,\n");
    buf.append("nativeEvid   number(9)            NOT NULL,\n");
    buf.append("nativeOrid   number(9)            NOT NULL,\n");
    buf.append("prefOrid     number(9)            NOT NULL,\n");
    buf.append("gtEpicenter  float(24)            NOT NULL,\n");
    buf.append("gtDepth      float(24)            NOT NULL,\n");
    buf.append("gtOT         float(24)            NOT NULL,\n");
    buf.append("masterOriginName varchar2(32)         NOT NULL,\n");
    buf.append("nativeOriginName varchar2(32)         NOT NULL,\n");
    buf.append("methodEpicenter varchar2(20)         NOT NULL,\n");
    buf.append("methodDepth  varchar2(20)         NOT NULL,\n");
    buf.append("methodOT     varchar2(20)         NOT NULL,\n");
    buf.append("refid        number(9)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (gtsid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (masterEvid,masterOrid,nativeEvid,nativeOrid,prefOrid)");
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
    return 248;
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
    return (other instanceof Gttable) && ((Gttable) other).gtsid == gtsid;
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
    return (other instanceof Gttable) && ((Gttable) other).masterEvid == masterEvid
        && ((Gttable) other).masterOrid == masterOrid && ((Gttable) other).nativeEvid == nativeEvid
        && ((Gttable) other).nativeOrid == nativeOrid && ((Gttable) other).prefOrid == prefOrid;
  }

  /**
   * Identifier for ground truth source.
   * 
   * @return gtsid
   */
  public long getGtsid() {
    return gtsid;
  }

  /**
   * Identifier for ground truth source.
   * 
   * @param gtsid
   * @throws IllegalArgumentException if gtsid >= 1000000000
   */
  public Gttable setGtsid(long gtsid) {
    if (gtsid >= 1000000000L)
      throw new IllegalArgumentException("gtsid=" + gtsid + " but cannot be >= 1000000000");
    this.gtsid = gtsid;
    setHash(null);
    return this;
  }

  /**
   * Evid of ground truth source in the master <B>origin</B> table (reconciled <I>evid</I> and
   * <I>orid</I>)
   * 
   * @return masterEvid
   */
  public long getMasterEvid() {
    return masterEvid;
  }

  /**
   * Evid of ground truth source in the master <B>origin</B> table (reconciled <I>evid</I> and
   * <I>orid</I>)
   * 
   * @param masterEvid
   * @throws IllegalArgumentException if masterEvid >= 1000000000
   */
  public Gttable setMasterEvid(long masterEvid) {
    if (masterEvid >= 1000000000L)
      throw new IllegalArgumentException(
          "masterEvid=" + masterEvid + " but cannot be >= 1000000000");
    this.masterEvid = masterEvid;
    setHash(null);
    return this;
  }

  /**
   * Orid of ground truth source in the master <B>origin</B> table (reconciled <I>evid</I> and
   * <I>orid</I>)
   * 
   * @return masterOrid
   */
  public long getMasterOrid() {
    return masterOrid;
  }

  /**
   * Orid of ground truth source in the master <B>origin</B> table (reconciled <I>evid</I> and
   * <I>orid</I>)
   * 
   * @param masterOrid
   * @throws IllegalArgumentException if masterOrid >= 1000000000
   */
  public Gttable setMasterOrid(long masterOrid) {
    if (masterOrid >= 1000000000L)
      throw new IllegalArgumentException(
          "masterOrid=" + masterOrid + " but cannot be >= 1000000000");
    this.masterOrid = masterOrid;
    setHash(null);
    return this;
  }

  /**
   * <I>Evid</I> of ground-truth source in native <B>origin</B> table
   * 
   * @return nativeEvid
   */
  public long getNativeEvid() {
    return nativeEvid;
  }

  /**
   * <I>Evid</I> of ground-truth source in native <B>origin</B> table
   * 
   * @param nativeEvid
   * @throws IllegalArgumentException if nativeEvid >= 1000000000
   */
  public Gttable setNativeEvid(long nativeEvid) {
    if (nativeEvid >= 1000000000L)
      throw new IllegalArgumentException(
          "nativeEvid=" + nativeEvid + " but cannot be >= 1000000000");
    this.nativeEvid = nativeEvid;
    setHash(null);
    return this;
  }

  /**
   * <I>Orid</I> of ground-truth source in native <B>origin</B> table
   * 
   * @return nativeOrid
   */
  public long getNativeOrid() {
    return nativeOrid;
  }

  /**
   * <I>Orid</I> of ground-truth source in native <B>origin</B> table
   * 
   * @param nativeOrid
   * @throws IllegalArgumentException if nativeOrid >= 1000000000
   */
  public Gttable setNativeOrid(long nativeOrid) {
    if (nativeOrid >= 1000000000L)
      throw new IllegalArgumentException(
          "nativeOrid=" + nativeOrid + " but cannot be >= 1000000000");
    this.nativeOrid = nativeOrid;
    setHash(null);
    return this;
  }

  /**
   * Preferred event origin for ground-truth source in the master <B>origin</B> table.
   * 
   * @return prefOrid
   */
  public long getPrefOrid() {
    return prefOrid;
  }

  /**
   * Preferred event origin for ground-truth source in the master <B>origin</B> table.
   * 
   * @param prefOrid
   * @throws IllegalArgumentException if prefOrid >= 1000000000
   */
  public Gttable setPrefOrid(long prefOrid) {
    if (prefOrid >= 1000000000L)
      throw new IllegalArgumentException("prefOrid=" + prefOrid + " but cannot be >= 1000000000");
    this.prefOrid = prefOrid;
    setHash(null);
    return this;
  }

  /**
   * Two sigma epicenter accuracy of ground truth location.
   * <p>
   * Units: km
   * 
   * @return gtEpicenter
   */
  public double getGtEpicenter() {
    return gtEpicenter;
  }

  /**
   * Two sigma epicenter accuracy of ground truth location.
   * <p>
   * Units: km
   * 
   * @param gtEpicenter
   */
  public Gttable setGtEpicenter(double gtEpicenter) {
    this.gtEpicenter = gtEpicenter;
    setHash(null);
    return this;
  }

  /**
   * Two sigma depth accuracy of ground truth location.
   * <p>
   * Units: km
   * 
   * @return gtDepth
   */
  public double getGtDepth() {
    return gtDepth;
  }

  /**
   * Two sigma depth accuracy of ground truth location.
   * <p>
   * Units: km
   * 
   * @param gtDepth
   */
  public Gttable setGtDepth(double gtDepth) {
    this.gtDepth = gtDepth;
    setHash(null);
    return this;
  }

  /**
   * Two sigma origin time accuracy of ground truth location.
   * <p>
   * Units: s
   * 
   * @return gtOT
   */
  public double getGtOT() {
    return gtOT;
  }

  /**
   * Two sigma origin time accuracy of ground truth location.
   * <p>
   * Units: s
   * 
   * @param gtOT
   */
  public Gttable setGtOT(double gtOT) {
    this.gtOT = gtOT;
    setHash(null);
    return this;
  }

  /**
   * Name of master <B>origin</B> table for ground truth source
   * 
   * @return masterOriginName
   */
  public String getMasterOriginName() {
    return masterOriginName;
  }

  /**
   * Name of master <B>origin</B> table for ground truth source
   * 
   * @param masterOriginName
   * @throws IllegalArgumentException if masterOriginName.length() >= 32
   */
  public Gttable setMasterOriginName(String masterOriginName) {
    if (masterOriginName.length() > 32)
      throw new IllegalArgumentException(String.format(
          "masterOriginName.length() cannot be > 32.  masterOriginName=%s", masterOriginName));
    this.masterOriginName = masterOriginName;
    setHash(null);
    return this;
  }

  /**
   * Name of native origin table for ground-truth source.
   * 
   * @return nativeOriginName
   */
  public String getNativeOriginName() {
    return nativeOriginName;
  }

  /**
   * Name of native origin table for ground-truth source.
   * 
   * @param nativeOriginName
   * @throws IllegalArgumentException if nativeOriginName.length() >= 32
   */
  public Gttable setNativeOriginName(String nativeOriginName) {
    if (nativeOriginName.length() > 32)
      throw new IllegalArgumentException(String.format(
          "nativeOriginName.length() cannot be > 32.  nativeOriginName=%s", nativeOriginName));
    this.nativeOriginName = nativeOriginName;
    setHash(null);
    return this;
  }

  /**
   * Epicenter criterion or method for ground truth source
   * 
   * @return methodEpicenter
   */
  public String getMethodEpicenter() {
    return methodEpicenter;
  }

  /**
   * Epicenter criterion or method for ground truth source
   * 
   * @param methodEpicenter
   * @throws IllegalArgumentException if methodEpicenter.length() >= 20
   */
  public Gttable setMethodEpicenter(String methodEpicenter) {
    if (methodEpicenter.length() > 20)
      throw new IllegalArgumentException(String
          .format("methodEpicenter.length() cannot be > 20.  methodEpicenter=%s", methodEpicenter));
    this.methodEpicenter = methodEpicenter;
    setHash(null);
    return this;
  }

  /**
   * Depth criterion or method for ground truth source
   * 
   * @return methodDepth
   */
  public String getMethodDepth() {
    return methodDepth;
  }

  /**
   * Depth criterion or method for ground truth source
   * 
   * @param methodDepth
   * @throws IllegalArgumentException if methodDepth.length() >= 20
   */
  public Gttable setMethodDepth(String methodDepth) {
    if (methodDepth.length() > 20)
      throw new IllegalArgumentException(
          String.format("methodDepth.length() cannot be > 20.  methodDepth=%s", methodDepth));
    this.methodDepth = methodDepth;
    setHash(null);
    return this;
  }

  /**
   * Origin time criterion or method for ground truth source
   * 
   * @return methodOT
   */
  public String getMethodOT() {
    return methodOT;
  }

  /**
   * Origin time criterion or method for ground truth source
   * 
   * @param methodOT
   * @throws IllegalArgumentException if methodOT.length() >= 20
   */
  public Gttable setMethodOT(String methodOT) {
    if (methodOT.length() > 20)
      throw new IllegalArgumentException(
          String.format("methodOT.length() cannot be > 20.  methodOT=%s", methodOT));
    this.methodOT = methodOT;
    setHash(null);
    return this;
  }

  /**
   * Reference identifier for linking to supporting information about the ground-truth source.
   * 
   * @return refid
   */
  public long getRefid() {
    return refid;
  }

  /**
   * Reference identifier for linking to supporting information about the ground-truth source.
   * 
   * @param refid
   * @throws IllegalArgumentException if refid >= 1000000000
   */
  public Gttable setRefid(long refid) {
    if (refid >= 1000000000L)
      throw new IllegalArgumentException("refid=" + refid + " but cannot be >= 1000000000");
    this.refid = refid;
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
  public Gttable setAuth(String auth) {
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
