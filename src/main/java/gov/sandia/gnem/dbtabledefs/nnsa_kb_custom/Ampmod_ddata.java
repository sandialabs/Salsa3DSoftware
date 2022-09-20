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
 * ampmod_ddata
 */
public class Ampmod_ddata extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Amplitude model identifier
   */
  private long ampmodid;

  static final public long AMPMODID_NA = Long.MIN_VALUE;

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
   * Beam identifier.
   */
  private long beamid;

  static final public long BEAMID_NA = -1;

  /**
   * Descriptive string that is used to group origins together for the purpose of declustering in
   * KBCIT. For example, all of the GT5 events in a single aftershock sequence might be given a
   * common tag. The assumption is that they will have a common bias that might be different from
   * the bias of some other set of GT5 events.
   */
  private String cluster_tag;

  static final public String CLUSTER_TAG_NA = "-";

  /**
   * A comment about this record.
   */
  private String comment_str;

  static final public String COMMENT_STR_NA = "-";

  /**
   * Author who loaded data
   */
  private String ldauth;

  static final public String LDAUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("ampmodid", Columns.FieldType.LONG, "%d");
    columns.add("f_t_type", Columns.FieldType.STRING, "%s");
    columns.add("lfreq", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("windowid", Columns.FieldType.LONG, "%d");
    columns.add("beamid", Columns.FieldType.LONG, "%d");
    columns.add("cluster_tag", Columns.FieldType.STRING, "%s");
    columns.add("comment_str", Columns.FieldType.STRING, "%s");
    columns.add("ldauth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Ampmod_ddata(long ampmodid, String f_t_type, double lfreq, long windowid, long beamid,
      String cluster_tag, String comment_str, String ldauth) {
    setValues(ampmodid, f_t_type, lfreq, windowid, beamid, cluster_tag, comment_str, ldauth);
  }

  private void setValues(long ampmodid, String f_t_type, double lfreq, long windowid, long beamid,
      String cluster_tag, String comment_str, String ldauth) {
    this.ampmodid = ampmodid;
    this.f_t_type = f_t_type;
    this.lfreq = lfreq;
    this.windowid = windowid;
    this.beamid = beamid;
    this.cluster_tag = cluster_tag;
    this.comment_str = comment_str;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Ampmod_ddata(Ampmod_ddata other) {
    this.ampmodid = other.getAmpmodid();
    this.f_t_type = other.getF_t_type();
    this.lfreq = other.getLfreq();
    this.windowid = other.getWindowid();
    this.beamid = other.getBeamid();
    this.cluster_tag = other.getCluster_tag();
    this.comment_str = other.getComment_str();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Ampmod_ddata() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(AMPMODID_NA, F_T_TYPE_NA, LFREQ_NA, WINDOWID_NA, BEAMID_NA, CLUSTER_TAG_NA,
        COMMENT_STR_NA, LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "f_t_type":
        return f_t_type;
      case "cluster_tag":
        return cluster_tag;
      case "comment_str":
        return comment_str;
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
      case "f_t_type":
        f_t_type = value;
        break;
      case "cluster_tag":
        cluster_tag = value;
        break;
      case "comment_str":
        comment_str = value;
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
      case "windowid":
        return windowid;
      case "beamid":
        return beamid;
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
      case "windowid":
        windowid = value;
        break;
      case "beamid":
        beamid = value;
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
  public Ampmod_ddata(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Ampmod_ddata(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), input.readDouble(), input.readLong(),
        input.readLong(), readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Ampmod_ddata(ByteBuffer input) {
    this(input.getLong(), readString(input), input.getDouble(), input.getLong(), input.getLong(),
        readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ampmod_ddata(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ampmod_ddata(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getDouble(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[8];
    values[0] = ampmodid;
    values[1] = f_t_type;
    values[2] = lfreq;
    values[3] = windowid;
    values[4] = beamid;
    values[5] = cluster_tag;
    values[6] = comment_str;
    values[7] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[9];
    values[0] = ampmodid;
    values[1] = f_t_type;
    values[2] = lfreq;
    values[3] = windowid;
    values[4] = beamid;
    values[5] = cluster_tag;
    values[6] = comment_str;
    values[7] = ldauth;
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
    output.writeLong(ampmodid);
    writeString(output, f_t_type);
    output.writeDouble(lfreq);
    output.writeLong(windowid);
    output.writeLong(beamid);
    writeString(output, cluster_tag);
    writeString(output, comment_str);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(ampmodid);
    writeString(output, f_t_type);
    output.putDouble(lfreq);
    output.putLong(windowid);
    output.putLong(beamid);
    writeString(output, cluster_tag);
    writeString(output, comment_str);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Ampmod_ddata objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Ampmod_ddata objects.
   * @throws IOException
   */
  static public void readAmpmod_ddatas(BufferedReader input, Collection<Ampmod_ddata> rows)
      throws IOException {
    String[] saved = Ampmod_ddata.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Ampmod_ddata
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Ampmod_ddata(new Scanner(line)));
    }
    input.close();
    Ampmod_ddata.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Ampmod_ddata objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Ampmod_ddata objects.
   * @throws IOException
   */
  static public void readAmpmod_ddatas(File inputFile, Collection<Ampmod_ddata> rows)
      throws IOException {
    readAmpmod_ddatas(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Ampmod_ddata objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Ampmod_ddata objects.
   * @throws IOException
   */
  static public void readAmpmod_ddatas(InputStream inputStream, Collection<Ampmod_ddata> rows)
      throws IOException {
    readAmpmod_ddatas(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Ampmod_ddata objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Ampmod_ddata objects
   * @throws IOException
   */
  static public Set<Ampmod_ddata> readAmpmod_ddatas(BufferedReader input) throws IOException {
    Set<Ampmod_ddata> rows = new LinkedHashSet<Ampmod_ddata>();
    readAmpmod_ddatas(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Ampmod_ddata objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Ampmod_ddata objects
   * @throws IOException
   */
  static public Set<Ampmod_ddata> readAmpmod_ddatas(File inputFile) throws IOException {
    return readAmpmod_ddatas(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Ampmod_ddata objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Ampmod_ddata objects
   * @throws IOException
   */
  static public Set<Ampmod_ddata> readAmpmod_ddatas(InputStream input) throws IOException {
    return readAmpmod_ddatas(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Ampmod_ddata objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param ampmod_ddatas the Ampmod_ddata objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Ampmod_ddata> ampmod_ddatas)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Ampmod_ddata ampmod_ddata : ampmod_ddatas)
      ampmod_ddata.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Ampmod_ddata objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param ampmod_ddatas the Ampmod_ddata objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Ampmod_ddata> ampmod_ddatas, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?)");
      for (Ampmod_ddata ampmod_ddata : ampmod_ddatas) {
        int i = 0;
        statement.setLong(++i, ampmod_ddata.ampmodid);
        statement.setString(++i, ampmod_ddata.f_t_type);
        statement.setDouble(++i, ampmod_ddata.lfreq);
        statement.setLong(++i, ampmod_ddata.windowid);
        statement.setLong(++i, ampmod_ddata.beamid);
        statement.setString(++i, ampmod_ddata.cluster_tag);
        statement.setString(++i, ampmod_ddata.comment_str);
        statement.setString(++i, ampmod_ddata.ldauth);
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
   *        Ampmod_ddata table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Ampmod_ddata> readAmpmod_ddatas(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Ampmod_ddata> results = new HashSet<Ampmod_ddata>();
    readAmpmod_ddatas(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Ampmod_ddata table.
   * @param ampmod_ddatas
   * @throws SQLException
   */
  static public void readAmpmod_ddatas(Connection connection, String selectStatement,
      Set<Ampmod_ddata> ampmod_ddatas) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        ampmod_ddatas.add(new Ampmod_ddata(rs));
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
   * this Ampmod_ddata object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Ampmod_ddata object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "ampmodid, f_t_type, lfreq, windowid, beamid, cluster_tag, comment_str, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(ampmodid)).append(", ");
    sql.append("'").append(f_t_type).append("', ");
    sql.append(Double.toString(lfreq)).append(", ");
    sql.append(Long.toString(windowid)).append(", ");
    sql.append(Long.toString(beamid)).append(", ");
    sql.append("'").append(cluster_tag).append("', ");
    sql.append("'").append(comment_str).append("', ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Ampmod_ddata in the database. Primary and unique keys are set, if
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
   * Create a table of type Ampmod_ddata in the database
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
   * Generate a sql script to create a table of type Ampmod_ddata in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Ampmod_ddata in the database
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
    buf.append("f_t_type     varchar2(4)          NOT NULL,\n");
    buf.append("lfreq        float(24)            NOT NULL,\n");
    buf.append("windowid     number(9)            NOT NULL,\n");
    buf.append("beamid       number(9)            NOT NULL,\n");
    buf.append("cluster_tag  varchar2(25)         NOT NULL,\n");
    buf.append("comment_str  varchar2(2000)       NOT NULL,\n");
    buf.append("ldauth       varchar2(15)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (ampmodid,f_t_type,lfreq,windowid)");
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
    return 2092;
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
    return (other instanceof Ampmod_ddata) && ((Ampmod_ddata) other).ampmodid == ampmodid
        && ((Ampmod_ddata) other).f_t_type.equals(f_t_type) && ((Ampmod_ddata) other).lfreq == lfreq
        && ((Ampmod_ddata) other).windowid == windowid;
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
  public Ampmod_ddata setAmpmodid(long ampmodid) {
    if (ampmodid >= 1000000000L)
      throw new IllegalArgumentException("ampmodid=" + ampmodid + " but cannot be >= 1000000000");
    this.ampmodid = ampmodid;
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
  public Ampmod_ddata setF_t_type(String f_t_type) {
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
  public Ampmod_ddata setLfreq(double lfreq) {
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
  public Ampmod_ddata setWindowid(long windowid) {
    if (windowid >= 1000000000L)
      throw new IllegalArgumentException("windowid=" + windowid + " but cannot be >= 1000000000");
    this.windowid = windowid;
    setHash(null);
    return this;
  }

  /**
   * Beam identifier.
   * 
   * @return beamid
   */
  public long getBeamid() {
    return beamid;
  }

  /**
   * Beam identifier.
   * 
   * @param beamid
   * @throws IllegalArgumentException if beamid >= 1000000000
   */
  public Ampmod_ddata setBeamid(long beamid) {
    if (beamid >= 1000000000L)
      throw new IllegalArgumentException("beamid=" + beamid + " but cannot be >= 1000000000");
    this.beamid = beamid;
    setHash(null);
    return this;
  }

  /**
   * Descriptive string that is used to group origins together for the purpose of declustering in
   * KBCIT. For example, all of the GT5 events in a single aftershock sequence might be given a
   * common tag. The assumption is that they will have a common bias that might be different from
   * the bias of some other set of GT5 events.
   * 
   * @return cluster_tag
   */
  public String getCluster_tag() {
    return cluster_tag;
  }

  /**
   * Descriptive string that is used to group origins together for the purpose of declustering in
   * KBCIT. For example, all of the GT5 events in a single aftershock sequence might be given a
   * common tag. The assumption is that they will have a common bias that might be different from
   * the bias of some other set of GT5 events.
   * 
   * @param cluster_tag
   * @throws IllegalArgumentException if cluster_tag.length() >= 25
   */
  public Ampmod_ddata setCluster_tag(String cluster_tag) {
    if (cluster_tag.length() > 25)
      throw new IllegalArgumentException(
          String.format("cluster_tag.length() cannot be > 25.  cluster_tag=%s", cluster_tag));
    this.cluster_tag = cluster_tag;
    setHash(null);
    return this;
  }

  /**
   * A comment about this record.
   * 
   * @return comment_str
   */
  public String getComment_str() {
    return comment_str;
  }

  /**
   * A comment about this record.
   * 
   * @param comment_str
   * @throws IllegalArgumentException if comment_str.length() >= 2000
   */
  public Ampmod_ddata setComment_str(String comment_str) {
    if (comment_str.length() > 2000)
      throw new IllegalArgumentException(
          String.format("comment_str.length() cannot be > 2000.  comment_str=%s", comment_str));
    this.comment_str = comment_str;
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
  public Ampmod_ddata setLdauth(String ldauth) {
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
