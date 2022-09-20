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
 * mdac_fd
 */
public class Mdac_fd extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Frequency dependent correction identifier parameter for MDAC processing.
   */
  private long fdid;

  static final public long FDID_NA = Long.MIN_VALUE;

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   */
  private long corrid;

  static final public long CORRID_NA = Long.MIN_VALUE;

  /**
   * Tomographic identifier.
   */
  private long tomoid;

  static final public long TOMOID_NA = -1;

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   */
  private long polyid;

  static final public long POLYID_NA = -1;

  /**
   * Unique identifier for version of a set of parameter files.
   */
  private long versionid;

  static final public long VERSIONID_NA = -1;

  /**
   * Low frequency of amplitude measure used in MDAC processing.
   * <p>
   * Units: Hz
   */
  private double lfreq;

  static final public double LFREQ_NA = Double.NaN;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   */
  private String chan;

  static final public String CHAN_NA = null;

  /**
   * Surface fit coefficient for MDAC processing
   * <p>
   * Units: unitless proportional to log10(amplitude)
   */
  private double a;

  static final public double A_NA = Double.NaN;

  /**
   * Surface fit coefficient for MDAC processing
   * <p>
   * Units: 1/(N*m) using log10(amplitude)
   */
  private double b;

  static final public double B_NA = Double.NaN;

  /**
   * Surface fit coefficient for MDAC processing
   * <p>
   * Units: 1/m using log10(amplitude)
   */
  private double c;

  static final public double C_NA = Double.NaN;

  /**
   * Site effect factor for MDAC processing. Contains frequency dependent site effect. For a weak
   * site effect, sitefact~0.
   * <p>
   * Units: Log10 (unitless value proportional to log10(amplitude))
   */
  private double sitefact;

  static final public double SITEFACT_NA = Double.NaN;

  /**
   * Algorithm identifier. Identifies algorithm(s) used to measure data.
   */
  private long algoid;

  static final public long ALGOID_NA = -1;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("fdid", Columns.FieldType.LONG, "%d");
    columns.add("corrid", Columns.FieldType.LONG, "%d");
    columns.add("tomoid", Columns.FieldType.LONG, "%d");
    columns.add("polyid", Columns.FieldType.LONG, "%d");
    columns.add("versionid", Columns.FieldType.LONG, "%d");
    columns.add("lfreq", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("a", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("b", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("c", Columns.FieldType.DOUBLE, "%10.3e");
    columns.add("sitefact", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("algoid", Columns.FieldType.LONG, "%d");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Mdac_fd(long fdid, long corrid, long tomoid, long polyid, long versionid, double lfreq,
      String sta, String chan, double a, double b, double c, double sitefact, long algoid,
      String auth) {
    setValues(fdid, corrid, tomoid, polyid, versionid, lfreq, sta, chan, a, b, c, sitefact, algoid,
        auth);
  }

  private void setValues(long fdid, long corrid, long tomoid, long polyid, long versionid,
      double lfreq, String sta, String chan, double a, double b, double c, double sitefact,
      long algoid, String auth) {
    this.fdid = fdid;
    this.corrid = corrid;
    this.tomoid = tomoid;
    this.polyid = polyid;
    this.versionid = versionid;
    this.lfreq = lfreq;
    this.sta = sta;
    this.chan = chan;
    this.a = a;
    this.b = b;
    this.c = c;
    this.sitefact = sitefact;
    this.algoid = algoid;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Mdac_fd(Mdac_fd other) {
    this.fdid = other.getFdid();
    this.corrid = other.getCorrid();
    this.tomoid = other.getTomoid();
    this.polyid = other.getPolyid();
    this.versionid = other.getVersionid();
    this.lfreq = other.getLfreq();
    this.sta = other.getSta();
    this.chan = other.getChan();
    this.a = other.getA();
    this.b = other.getB();
    this.c = other.getC();
    this.sitefact = other.getSitefact();
    this.algoid = other.getAlgoid();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Mdac_fd() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(FDID_NA, CORRID_NA, TOMOID_NA, POLYID_NA, VERSIONID_NA, LFREQ_NA, STA_NA, CHAN_NA,
        A_NA, B_NA, C_NA, SITEFACT_NA, ALGOID_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "chan":
        return chan;
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
      case "sta":
        sta = value;
        break;
      case "chan":
        chan = value;
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
      case "lfreq":
        return lfreq;
      case "a":
        return a;
      case "b":
        return b;
      case "c":
        return c;
      case "sitefact":
        return sitefact;
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
      case "a":
        a = value;
        break;
      case "b":
        b = value;
        break;
      case "c":
        c = value;
        break;
      case "sitefact":
        sitefact = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "fdid":
        return fdid;
      case "corrid":
        return corrid;
      case "tomoid":
        return tomoid;
      case "polyid":
        return polyid;
      case "versionid":
        return versionid;
      case "algoid":
        return algoid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "fdid":
        fdid = value;
        break;
      case "corrid":
        corrid = value;
        break;
      case "tomoid":
        tomoid = value;
        break;
      case "polyid":
        polyid = value;
        break;
      case "versionid":
        versionid = value;
        break;
      case "algoid":
        algoid = value;
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
  public Mdac_fd(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Mdac_fd(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        input.readDouble(), readString(input), readString(input), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readLong(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Mdac_fd(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getLong(),
        input.getDouble(), readString(input), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getLong(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Mdac_fd(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Mdac_fd(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getDouble(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getLong(offset + 13), input.getString(offset + 14));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[14];
    values[0] = fdid;
    values[1] = corrid;
    values[2] = tomoid;
    values[3] = polyid;
    values[4] = versionid;
    values[5] = lfreq;
    values[6] = sta;
    values[7] = chan;
    values[8] = a;
    values[9] = b;
    values[10] = c;
    values[11] = sitefact;
    values[12] = algoid;
    values[13] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[15];
    values[0] = fdid;
    values[1] = corrid;
    values[2] = tomoid;
    values[3] = polyid;
    values[4] = versionid;
    values[5] = lfreq;
    values[6] = sta;
    values[7] = chan;
    values[8] = a;
    values[9] = b;
    values[10] = c;
    values[11] = sitefact;
    values[12] = algoid;
    values[13] = auth;
    values[14] = lddate;
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
    output.writeLong(fdid);
    output.writeLong(corrid);
    output.writeLong(tomoid);
    output.writeLong(polyid);
    output.writeLong(versionid);
    output.writeDouble(lfreq);
    writeString(output, sta);
    writeString(output, chan);
    output.writeDouble(a);
    output.writeDouble(b);
    output.writeDouble(c);
    output.writeDouble(sitefact);
    output.writeLong(algoid);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(fdid);
    output.putLong(corrid);
    output.putLong(tomoid);
    output.putLong(polyid);
    output.putLong(versionid);
    output.putDouble(lfreq);
    writeString(output, sta);
    writeString(output, chan);
    output.putDouble(a);
    output.putDouble(b);
    output.putDouble(c);
    output.putDouble(sitefact);
    output.putLong(algoid);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Mdac_fd objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Mdac_fd objects.
   * @throws IOException
   */
  static public void readMdac_fds(BufferedReader input, Collection<Mdac_fd> rows)
      throws IOException {
    String[] saved = Mdac_fd.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Mdac_fd.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Mdac_fd(new Scanner(line)));
    }
    input.close();
    Mdac_fd.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Mdac_fd objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Mdac_fd objects.
   * @throws IOException
   */
  static public void readMdac_fds(File inputFile, Collection<Mdac_fd> rows) throws IOException {
    readMdac_fds(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Mdac_fd objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Mdac_fd objects.
   * @throws IOException
   */
  static public void readMdac_fds(InputStream inputStream, Collection<Mdac_fd> rows)
      throws IOException {
    readMdac_fds(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Mdac_fd objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Mdac_fd objects
   * @throws IOException
   */
  static public Set<Mdac_fd> readMdac_fds(BufferedReader input) throws IOException {
    Set<Mdac_fd> rows = new LinkedHashSet<Mdac_fd>();
    readMdac_fds(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Mdac_fd objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Mdac_fd objects
   * @throws IOException
   */
  static public Set<Mdac_fd> readMdac_fds(File inputFile) throws IOException {
    return readMdac_fds(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Mdac_fd objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Mdac_fd objects
   * @throws IOException
   */
  static public Set<Mdac_fd> readMdac_fds(InputStream input) throws IOException {
    return readMdac_fds(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Mdac_fd objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param mdac_fds the Mdac_fd objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Mdac_fd> mdac_fds)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Mdac_fd mdac_fd : mdac_fds)
      mdac_fd.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Mdac_fd objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param mdac_fds the Mdac_fd objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Mdac_fd> mdac_fds, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Mdac_fd mdac_fd : mdac_fds) {
        int i = 0;
        statement.setLong(++i, mdac_fd.fdid);
        statement.setLong(++i, mdac_fd.corrid);
        statement.setLong(++i, mdac_fd.tomoid);
        statement.setLong(++i, mdac_fd.polyid);
        statement.setLong(++i, mdac_fd.versionid);
        statement.setDouble(++i, mdac_fd.lfreq);
        statement.setString(++i, mdac_fd.sta);
        statement.setString(++i, mdac_fd.chan);
        statement.setDouble(++i, mdac_fd.a);
        statement.setDouble(++i, mdac_fd.b);
        statement.setDouble(++i, mdac_fd.c);
        statement.setDouble(++i, mdac_fd.sitefact);
        statement.setLong(++i, mdac_fd.algoid);
        statement.setString(++i, mdac_fd.auth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Mdac_fd
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Mdac_fd> readMdac_fds(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Mdac_fd> results = new HashSet<Mdac_fd>();
    readMdac_fds(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Mdac_fd
   *        table.
   * @param mdac_fds
   * @throws SQLException
   */
  static public void readMdac_fds(Connection connection, String selectStatement,
      Set<Mdac_fd> mdac_fds) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        mdac_fds.add(new Mdac_fd(rs));
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
   * this Mdac_fd object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Mdac_fd object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "fdid, corrid, tomoid, polyid, versionid, lfreq, sta, chan, a, b, c, sitefact, algoid, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(fdid)).append(", ");
    sql.append(Long.toString(corrid)).append(", ");
    sql.append(Long.toString(tomoid)).append(", ");
    sql.append(Long.toString(polyid)).append(", ");
    sql.append(Long.toString(versionid)).append(", ");
    sql.append(Double.toString(lfreq)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(chan).append("', ");
    sql.append(Double.toString(a)).append(", ");
    sql.append(Double.toString(b)).append(", ");
    sql.append(Double.toString(c)).append(", ");
    sql.append(Double.toString(sitefact)).append(", ");
    sql.append(Long.toString(algoid)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Mdac_fd in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Mdac_fd in the database
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
   * Generate a sql script to create a table of type Mdac_fd in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Mdac_fd in the database
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
    buf.append("fdid         number(9)            NOT NULL,\n");
    buf.append("corrid       number(9)            NOT NULL,\n");
    buf.append("tomoid       number(9)            NOT NULL,\n");
    buf.append("polyid       number(9)            NOT NULL,\n");
    buf.append("versionid    number(9)            NOT NULL,\n");
    buf.append("lfreq        float(24)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("a            float(24)            NOT NULL,\n");
    buf.append("b            float(24)            NOT NULL,\n");
    buf.append("c            float(24)            NOT NULL,\n");
    buf.append("sitefact     float(24)            NOT NULL,\n");
    buf.append("algoid       number(9)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (fdid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (corrid,polyid,versionid,lfreq,sta,chan)");
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
    return 134;
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
    return (other instanceof Mdac_fd) && ((Mdac_fd) other).fdid == fdid;
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
    return (other instanceof Mdac_fd) && ((Mdac_fd) other).corrid == corrid
        && ((Mdac_fd) other).polyid == polyid && ((Mdac_fd) other).versionid == versionid
        && ((Mdac_fd) other).lfreq == lfreq && ((Mdac_fd) other).sta.equals(sta)
        && ((Mdac_fd) other).chan.equals(chan);
  }

  /**
   * Frequency dependent correction identifier parameter for MDAC processing.
   * 
   * @return fdid
   */
  public long getFdid() {
    return fdid;
  }

  /**
   * Frequency dependent correction identifier parameter for MDAC processing.
   * 
   * @param fdid
   * @throws IllegalArgumentException if fdid >= 1000000000
   */
  public Mdac_fd setFdid(long fdid) {
    if (fdid >= 1000000000L)
      throw new IllegalArgumentException("fdid=" + fdid + " but cannot be >= 1000000000");
    this.fdid = fdid;
    setHash(null);
    return this;
  }

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   * 
   * @return corrid
   */
  public long getCorrid() {
    return corrid;
  }

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   * 
   * @param corrid
   * @throws IllegalArgumentException if corrid >= 1000000000
   */
  public Mdac_fd setCorrid(long corrid) {
    if (corrid >= 1000000000L)
      throw new IllegalArgumentException("corrid=" + corrid + " but cannot be >= 1000000000");
    this.corrid = corrid;
    setHash(null);
    return this;
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
  public Mdac_fd setTomoid(long tomoid) {
    if (tomoid >= 1000000000L)
      throw new IllegalArgumentException("tomoid=" + tomoid + " but cannot be >= 1000000000");
    this.tomoid = tomoid;
    setHash(null);
    return this;
  }

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   * 
   * @return polyid
   */
  public long getPolyid() {
    return polyid;
  }

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   * 
   * @param polyid
   * @throws IllegalArgumentException if polyid >= 1000000000
   */
  public Mdac_fd setPolyid(long polyid) {
    if (polyid >= 1000000000L)
      throw new IllegalArgumentException("polyid=" + polyid + " but cannot be >= 1000000000");
    this.polyid = polyid;
    setHash(null);
    return this;
  }

  /**
   * Unique identifier for version of a set of parameter files.
   * 
   * @return versionid
   */
  public long getVersionid() {
    return versionid;
  }

  /**
   * Unique identifier for version of a set of parameter files.
   * 
   * @param versionid
   * @throws IllegalArgumentException if versionid >= 1000000000
   */
  public Mdac_fd setVersionid(long versionid) {
    if (versionid >= 1000000000L)
      throw new IllegalArgumentException("versionid=" + versionid + " but cannot be >= 1000000000");
    this.versionid = versionid;
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
  public Mdac_fd setLfreq(double lfreq) {
    this.lfreq = lfreq;
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
  public Mdac_fd setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   * 
   * @return chan
   */
  public String getChan() {
    return chan;
  }

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   * 
   * @param chan
   * @throws IllegalArgumentException if chan.length() >= 8
   */
  public Mdac_fd setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
    setHash(null);
    return this;
  }

  /**
   * Surface fit coefficient for MDAC processing
   * <p>
   * Units: unitless proportional to log10(amplitude)
   * 
   * @return a
   */
  public double getA() {
    return a;
  }

  /**
   * Surface fit coefficient for MDAC processing
   * <p>
   * Units: unitless proportional to log10(amplitude)
   * 
   * @param a
   */
  public Mdac_fd setA(double a) {
    this.a = a;
    setHash(null);
    return this;
  }

  /**
   * Surface fit coefficient for MDAC processing
   * <p>
   * Units: 1/(N*m) using log10(amplitude)
   * 
   * @return b
   */
  public double getB() {
    return b;
  }

  /**
   * Surface fit coefficient for MDAC processing
   * <p>
   * Units: 1/(N*m) using log10(amplitude)
   * 
   * @param b
   */
  public Mdac_fd setB(double b) {
    this.b = b;
    setHash(null);
    return this;
  }

  /**
   * Surface fit coefficient for MDAC processing
   * <p>
   * Units: 1/m using log10(amplitude)
   * 
   * @return c
   */
  public double getC() {
    return c;
  }

  /**
   * Surface fit coefficient for MDAC processing
   * <p>
   * Units: 1/m using log10(amplitude)
   * 
   * @param c
   */
  public Mdac_fd setC(double c) {
    this.c = c;
    setHash(null);
    return this;
  }

  /**
   * Site effect factor for MDAC processing. Contains frequency dependent site effect. For a weak
   * site effect, sitefact~0.
   * <p>
   * Units: Log10 (unitless value proportional to log10(amplitude))
   * 
   * @return sitefact
   */
  public double getSitefact() {
    return sitefact;
  }

  /**
   * Site effect factor for MDAC processing. Contains frequency dependent site effect. For a weak
   * site effect, sitefact~0.
   * <p>
   * Units: Log10 (unitless value proportional to log10(amplitude))
   * 
   * @param sitefact
   */
  public Mdac_fd setSitefact(double sitefact) {
    this.sitefact = sitefact;
    setHash(null);
    return this;
  }

  /**
   * Algorithm identifier. Identifies algorithm(s) used to measure data.
   * 
   * @return algoid
   */
  public long getAlgoid() {
    return algoid;
  }

  /**
   * Algorithm identifier. Identifies algorithm(s) used to measure data.
   * 
   * @param algoid
   * @throws IllegalArgumentException if algoid >= 1000000000
   */
  public Mdac_fd setAlgoid(long algoid) {
    if (algoid >= 1000000000L)
      throw new IllegalArgumentException("algoid=" + algoid + " but cannot be >= 1000000000");
    this.algoid = algoid;
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
  public Mdac_fd setAuth(String auth) {
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
