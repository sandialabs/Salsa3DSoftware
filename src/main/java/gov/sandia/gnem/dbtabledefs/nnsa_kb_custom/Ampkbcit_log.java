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
 * ampkbcit_log
 */
public class Ampkbcit_log extends BaseRow implements Serializable {

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
   * The <I>orid</I> or the observation that this observation was clusted into, or -1 if not
   * clustered.
   */
  private long clusterorid;

  static final public long CLUSTERORID_NA = -1;

  /**
   * The <I>evid</I> of the observation that this observation was clustered into, or -1 if not
   * clustered
   */
  private long clusterevid;

  static final public long CLUSTEREVID_NA = -1;

  /**
   * Indicator for whether or not this GT observation has been filtered out according to the
   * <I>gtlevel</I> in the <B>ttmod_kbcit</B> table (y=yes, n=no).
   */
  private String isgtfiltered;

  static final public String ISGTFILTERED_NA = "-";

  /**
   * Amplitude measurement type as defined below. This identifies how the attributes
   * <I>f_t_value</I>, <I>f_t_del</I>, <I>f_t_low</I> and <I>f_t_hi</I> are recorded in
   * <B>nnsa_amplitude</B>. FREQ - Frequency Domain Record PTOP - Pseudo Spectral Conversion of
   * Peak-to-Peak Time PENV - Pseudo Spectral Conversion of Peak Time Domain Envelope Measure TRMS -
   * Pseudo Spectral Conversion of Time Domain RMS Measure
   */
  private String f_t_type;

  static final public String F_T_TYPE_NA = null;

  /**
   * Low frequency of amplitude measure used in MDAC processing.
   * <p>
   * Units: Hz
   */
  private double lfreq;

  static final public double LFREQ_NA = Double.NaN;

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   */
  private long windowid;

  static final public long WINDOWID_NA = Long.MIN_VALUE;

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
    columns.add("ismanremoved", Columns.FieldType.STRING, "%s");
    columns.add("ishistoremoved", Columns.FieldType.STRING, "%s");
    columns.add("clusterorid", Columns.FieldType.LONG, "%d");
    columns.add("clusterevid", Columns.FieldType.LONG, "%d");
    columns.add("isgtfiltered", Columns.FieldType.STRING, "%s");
    columns.add("f_t_type", Columns.FieldType.STRING, "%s");
    columns.add("lfreq", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("windowid", Columns.FieldType.LONG, "%d");
    columns.add("ldauth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Ampkbcit_log(long ampmodid, long kbcitid, String ismanremoved, String ishistoremoved,
      long clusterorid, long clusterevid, String isgtfiltered, String f_t_type, double lfreq,
      long windowid, String ldauth) {
    setValues(ampmodid, kbcitid, ismanremoved, ishistoremoved, clusterorid, clusterevid,
        isgtfiltered, f_t_type, lfreq, windowid, ldauth);
  }

  private void setValues(long ampmodid, long kbcitid, String ismanremoved, String ishistoremoved,
      long clusterorid, long clusterevid, String isgtfiltered, String f_t_type, double lfreq,
      long windowid, String ldauth) {
    this.ampmodid = ampmodid;
    this.kbcitid = kbcitid;
    this.ismanremoved = ismanremoved;
    this.ishistoremoved = ishistoremoved;
    this.clusterorid = clusterorid;
    this.clusterevid = clusterevid;
    this.isgtfiltered = isgtfiltered;
    this.f_t_type = f_t_type;
    this.lfreq = lfreq;
    this.windowid = windowid;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Ampkbcit_log(Ampkbcit_log other) {
    this.ampmodid = other.getAmpmodid();
    this.kbcitid = other.getKbcitid();
    this.ismanremoved = other.getIsmanremoved();
    this.ishistoremoved = other.getIshistoremoved();
    this.clusterorid = other.getClusterorid();
    this.clusterevid = other.getClusterevid();
    this.isgtfiltered = other.getIsgtfiltered();
    this.f_t_type = other.getF_t_type();
    this.lfreq = other.getLfreq();
    this.windowid = other.getWindowid();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Ampkbcit_log() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(AMPMODID_NA, KBCITID_NA, ISMANREMOVED_NA, ISHISTOREMOVED_NA, CLUSTERORID_NA,
        CLUSTEREVID_NA, ISGTFILTERED_NA, F_T_TYPE_NA, LFREQ_NA, WINDOWID_NA, LDAUTH_NA);
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
      case "f_t_type":
        return f_t_type;
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
      case "f_t_type":
        f_t_type = value;
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
      case "lfreq":
        return lfreq;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "lfreq":
        lfreq = value;
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
      case "clusterorid":
        return clusterorid;
      case "clusterevid":
        return clusterevid;
      case "windowid":
        return windowid;
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
      case "clusterorid":
        clusterorid = value;
        break;
      case "clusterevid":
        clusterevid = value;
        break;
      case "windowid":
        windowid = value;
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
  public Ampkbcit_log(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Ampkbcit_log(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), readString(input), readString(input), input.readLong(),
        input.readLong(), readString(input), readString(input), input.readDouble(),
        input.readLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Ampkbcit_log(ByteBuffer input) {
    this(input.getLong(), input.getLong(), readString(input), readString(input), input.getLong(),
        input.getLong(), readString(input), readString(input), input.getDouble(), input.getLong(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ampkbcit_log(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ampkbcit_log(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getDouble(offset + 9),
        input.getLong(offset + 10), input.getString(offset + 11));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[11];
    values[0] = ampmodid;
    values[1] = kbcitid;
    values[2] = ismanremoved;
    values[3] = ishistoremoved;
    values[4] = clusterorid;
    values[5] = clusterevid;
    values[6] = isgtfiltered;
    values[7] = f_t_type;
    values[8] = lfreq;
    values[9] = windowid;
    values[10] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[12];
    values[0] = ampmodid;
    values[1] = kbcitid;
    values[2] = ismanremoved;
    values[3] = ishistoremoved;
    values[4] = clusterorid;
    values[5] = clusterevid;
    values[6] = isgtfiltered;
    values[7] = f_t_type;
    values[8] = lfreq;
    values[9] = windowid;
    values[10] = ldauth;
    values[11] = lddate;
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
    writeString(output, ismanremoved);
    writeString(output, ishistoremoved);
    output.writeLong(clusterorid);
    output.writeLong(clusterevid);
    writeString(output, isgtfiltered);
    writeString(output, f_t_type);
    output.writeDouble(lfreq);
    output.writeLong(windowid);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(ampmodid);
    output.putLong(kbcitid);
    writeString(output, ismanremoved);
    writeString(output, ishistoremoved);
    output.putLong(clusterorid);
    output.putLong(clusterevid);
    writeString(output, isgtfiltered);
    writeString(output, f_t_type);
    output.putDouble(lfreq);
    output.putLong(windowid);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Ampkbcit_log objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Ampkbcit_log objects.
   * @throws IOException
   */
  static public void readAmpkbcit_logs(BufferedReader input, Collection<Ampkbcit_log> rows)
      throws IOException {
    String[] saved = Ampkbcit_log.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Ampkbcit_log
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Ampkbcit_log(new Scanner(line)));
    }
    input.close();
    Ampkbcit_log.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Ampkbcit_log objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Ampkbcit_log objects.
   * @throws IOException
   */
  static public void readAmpkbcit_logs(File inputFile, Collection<Ampkbcit_log> rows)
      throws IOException {
    readAmpkbcit_logs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Ampkbcit_log objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Ampkbcit_log objects.
   * @throws IOException
   */
  static public void readAmpkbcit_logs(InputStream inputStream, Collection<Ampkbcit_log> rows)
      throws IOException {
    readAmpkbcit_logs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Ampkbcit_log objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Ampkbcit_log objects
   * @throws IOException
   */
  static public Set<Ampkbcit_log> readAmpkbcit_logs(BufferedReader input) throws IOException {
    Set<Ampkbcit_log> rows = new LinkedHashSet<Ampkbcit_log>();
    readAmpkbcit_logs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Ampkbcit_log objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Ampkbcit_log objects
   * @throws IOException
   */
  static public Set<Ampkbcit_log> readAmpkbcit_logs(File inputFile) throws IOException {
    return readAmpkbcit_logs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Ampkbcit_log objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Ampkbcit_log objects
   * @throws IOException
   */
  static public Set<Ampkbcit_log> readAmpkbcit_logs(InputStream input) throws IOException {
    return readAmpkbcit_logs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Ampkbcit_log objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param ampkbcit_logs the Ampkbcit_log objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Ampkbcit_log> ampkbcit_logs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Ampkbcit_log ampkbcit_log : ampkbcit_logs)
      ampkbcit_log.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Ampkbcit_log objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param ampkbcit_logs the Ampkbcit_log objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Ampkbcit_log> ampkbcit_logs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Ampkbcit_log ampkbcit_log : ampkbcit_logs) {
        int i = 0;
        statement.setLong(++i, ampkbcit_log.ampmodid);
        statement.setLong(++i, ampkbcit_log.kbcitid);
        statement.setString(++i, ampkbcit_log.ismanremoved);
        statement.setString(++i, ampkbcit_log.ishistoremoved);
        statement.setLong(++i, ampkbcit_log.clusterorid);
        statement.setLong(++i, ampkbcit_log.clusterevid);
        statement.setString(++i, ampkbcit_log.isgtfiltered);
        statement.setString(++i, ampkbcit_log.f_t_type);
        statement.setDouble(++i, ampkbcit_log.lfreq);
        statement.setLong(++i, ampkbcit_log.windowid);
        statement.setString(++i, ampkbcit_log.ldauth);
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
   *        Ampkbcit_log table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Ampkbcit_log> readAmpkbcit_logs(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Ampkbcit_log> results = new HashSet<Ampkbcit_log>();
    readAmpkbcit_logs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Ampkbcit_log table.
   * @param ampkbcit_logs
   * @throws SQLException
   */
  static public void readAmpkbcit_logs(Connection connection, String selectStatement,
      Set<Ampkbcit_log> ampkbcit_logs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        ampkbcit_logs.add(new Ampkbcit_log(rs));
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
   * this Ampkbcit_log object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Ampkbcit_log object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "ampmodid, kbcitid, ismanremoved, ishistoremoved, clusterorid, clusterevid, isgtfiltered, f_t_type, lfreq, windowid, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(ampmodid)).append(", ");
    sql.append(Long.toString(kbcitid)).append(", ");
    sql.append("'").append(ismanremoved).append("', ");
    sql.append("'").append(ishistoremoved).append("', ");
    sql.append(Long.toString(clusterorid)).append(", ");
    sql.append(Long.toString(clusterevid)).append(", ");
    sql.append("'").append(isgtfiltered).append("', ");
    sql.append("'").append(f_t_type).append("', ");
    sql.append(Double.toString(lfreq)).append(", ");
    sql.append(Long.toString(windowid)).append(", ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Ampkbcit_log in the database. Primary and unique keys are set, if
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
   * Create a table of type Ampkbcit_log in the database
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
   * Generate a sql script to create a table of type Ampkbcit_log in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Ampkbcit_log in the database
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
    buf.append("ismanremoved varchar2(1)          NOT NULL,\n");
    buf.append("ishistoremoved varchar2(1)          NOT NULL,\n");
    buf.append("clusterorid  number(9)            NOT NULL,\n");
    buf.append("clusterevid  number(9)            NOT NULL,\n");
    buf.append("isgtfiltered varchar2(1)          NOT NULL,\n");
    buf.append("f_t_type     varchar2(4)          NOT NULL,\n");
    buf.append("lfreq        float(24)            NOT NULL,\n");
    buf.append("windowid     number(9)            NOT NULL,\n");
    buf.append("ldauth       varchar2(15)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (ampmodid,kbcitid,f_t_type,lfreq,windowid)");
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
    return 90;
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
    return (other instanceof Ampkbcit_log) && ((Ampkbcit_log) other).ampmodid == ampmodid
        && ((Ampkbcit_log) other).kbcitid == kbcitid
        && ((Ampkbcit_log) other).f_t_type.equals(f_t_type) && ((Ampkbcit_log) other).lfreq == lfreq
        && ((Ampkbcit_log) other).windowid == windowid;
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
  public Ampkbcit_log setAmpmodid(long ampmodid) {
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
  public Ampkbcit_log setKbcitid(long kbcitid) {
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
  public Ampkbcit_log setIsmanremoved(String ismanremoved) {
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
  public Ampkbcit_log setIshistoremoved(String ishistoremoved) {
    if (ishistoremoved.length() > 1)
      throw new IllegalArgumentException(String
          .format("ishistoremoved.length() cannot be > 1.  ishistoremoved=%s", ishistoremoved));
    this.ishistoremoved = ishistoremoved;
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
  public Ampkbcit_log setClusterorid(long clusterorid) {
    if (clusterorid >= 1000000000L)
      throw new IllegalArgumentException(
          "clusterorid=" + clusterorid + " but cannot be >= 1000000000");
    this.clusterorid = clusterorid;
    setHash(null);
    return this;
  }

  /**
   * The <I>evid</I> of the observation that this observation was clustered into, or -1 if not
   * clustered
   * 
   * @return clusterevid
   */
  public long getClusterevid() {
    return clusterevid;
  }

  /**
   * The <I>evid</I> of the observation that this observation was clustered into, or -1 if not
   * clustered
   * 
   * @param clusterevid
   * @throws IllegalArgumentException if clusterevid >= 1000000000
   */
  public Ampkbcit_log setClusterevid(long clusterevid) {
    if (clusterevid >= 1000000000L)
      throw new IllegalArgumentException(
          "clusterevid=" + clusterevid + " but cannot be >= 1000000000");
    this.clusterevid = clusterevid;
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
  public Ampkbcit_log setIsgtfiltered(String isgtfiltered) {
    if (isgtfiltered.length() > 1)
      throw new IllegalArgumentException(
          String.format("isgtfiltered.length() cannot be > 1.  isgtfiltered=%s", isgtfiltered));
    this.isgtfiltered = isgtfiltered;
    setHash(null);
    return this;
  }

  /**
   * Amplitude measurement type as defined below. This identifies how the attributes
   * <I>f_t_value</I>, <I>f_t_del</I>, <I>f_t_low</I> and <I>f_t_hi</I> are recorded in
   * <B>nnsa_amplitude</B>. FREQ - Frequency Domain Record PTOP - Pseudo Spectral Conversion of
   * Peak-to-Peak Time PENV - Pseudo Spectral Conversion of Peak Time Domain Envelope Measure TRMS -
   * Pseudo Spectral Conversion of Time Domain RMS Measure
   * 
   * @return f_t_type
   */
  public String getF_t_type() {
    return f_t_type;
  }

  /**
   * Amplitude measurement type as defined below. This identifies how the attributes
   * <I>f_t_value</I>, <I>f_t_del</I>, <I>f_t_low</I> and <I>f_t_hi</I> are recorded in
   * <B>nnsa_amplitude</B>. FREQ - Frequency Domain Record PTOP - Pseudo Spectral Conversion of
   * Peak-to-Peak Time PENV - Pseudo Spectral Conversion of Peak Time Domain Envelope Measure TRMS -
   * Pseudo Spectral Conversion of Time Domain RMS Measure
   * 
   * @param f_t_type
   * @throws IllegalArgumentException if f_t_type.length() >= 4
   */
  public Ampkbcit_log setF_t_type(String f_t_type) {
    if (f_t_type.length() > 4)
      throw new IllegalArgumentException(
          String.format("f_t_type.length() cannot be > 4.  f_t_type=%s", f_t_type));
    this.f_t_type = f_t_type;
    setHash(null);
    return this;
  }

  /**
   * Low frequency of amplitude measure used in MDAC processing.
   * <p>
   * Units: Hz
   * 
   * @return lfreq
   */
  public double getLfreq() {
    return lfreq;
  }

  /**
   * Low frequency of amplitude measure used in MDAC processing.
   * <p>
   * Units: Hz
   * 
   * @param lfreq
   */
  public Ampkbcit_log setLfreq(double lfreq) {
    this.lfreq = lfreq;
    setHash(null);
    return this;
  }

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   * 
   * @return windowid
   */
  public long getWindowid() {
    return windowid;
  }

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   * 
   * @param windowid
   * @throws IllegalArgumentException if windowid >= 1000000000
   */
  public Ampkbcit_log setWindowid(long windowid) {
    if (windowid >= 1000000000L)
      throw new IllegalArgumentException("windowid=" + windowid + " but cannot be >= 1000000000");
    this.windowid = windowid;
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
  public Ampkbcit_log setLdauth(String ldauth) {
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
