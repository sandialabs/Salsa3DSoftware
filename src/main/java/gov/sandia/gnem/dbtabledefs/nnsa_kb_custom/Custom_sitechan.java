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
 * custom_sitechan
 */
public class Custom_sitechan extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

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
   * Turn on date. This column is the julian date that the node is promoted for use
   */
  private long ondate;

  static final public long ONDATE_NA = -1;

  /**
   * Channel identifier. This value is a surrogate key used to uniquely identify a specific
   * recording. The column chanid duplicates the information of the compound key
   * <I>sta</I>/<I>chan</I>/<I>time</I>.
   */
  private long chanid;

  static final public long CHANID_NA = -1;

  /**
   * Turn off date. This column is the julian date that the node is no longer used
   */
  private long offdate;

  static final public long OFFDATE_NA = 2286324;

  /**
   * Channel type. This column specifies the type of data channel: normal (n) -- a normal instrument
   * response, beam (b) -- a coherent beam formed with array data, or incoherent (i) -- an
   * incoherent beam or energy stack.
   */
  private String ctype;

  static final public String CTYPE_NA = "-";

  /**
   * Emplacement depth at which instrument is positioned relative to the value of <I>elev</I> in the
   * <B>site</B> table
   * <p>
   * Units: km
   */
  private double edepth;

  static final public double EDEPTH_NA = Double.NaN;

  /**
   * Horizontal orientation of seismometer. This column specifies the orientation of the seismometer
   * in the horizontal plane, measured clockwise from North. For a North-South orientation with the
   * seismometer pointing toward the North, hang = 0.0 For East-West orientation with the
   * seismometer pointing toward the West, hang = 270.0 The hang is indeterminate for some cases
   * such as horizontal beam channels, hydrophones, infrasound, and various state of health channels
   * The hang is indeterminate for some cases such as horizontal beam channels, hydrophones,
   * infrasound, and various state of health channels (see <I>vang</I>)
   * <p>
   * Units: degree
   */
  private double hang;

  static final public double HANG_NA = -1;

  /**
   * Vertical orientation of seismometer. This column measures the angle between the sensitive axis
   * of a seismometer and the outward-pointing vertical direction. For a vertically oriented
   * seismometer, <I>vang</I> = 0 For a horizontally oriented seismometer, <I>vang</I> = 90 For
   * vertical beams, <I>vang</I> = 0 is indeterminate for some cases such as hydrophones,
   * infrasound, and various state of health channels For vertical beans, <I>vang</I> = 0 is
   * indeterminate for some cases such as hydrophones, infrasound, and various state of health
   * channels (see <I>hang</I>)
   * <p>
   * Units: degree
   */
  private double vang;

  static final public double VANG_NA = -1;

  /**
   * Text description, for <B>sitechan</B> it is the channel description
   */
  private String descrip;

  static final public String DESCRIP_NA = "-";

  /**
   * Site identifier that either uniquely idenitifes a station or relations a record to a station
   */
  private long siteid;

  static final public long SITEID_NA = Long.MIN_VALUE;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("ondate", Columns.FieldType.LONG, "%d");
    columns.add("chanid", Columns.FieldType.LONG, "%d");
    columns.add("offdate", Columns.FieldType.LONG, "%d");
    columns.add("ctype", Columns.FieldType.STRING, "%s");
    columns.add("edepth", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("hang", Columns.FieldType.DOUBLE, "%1.1f");
    columns.add("vang", Columns.FieldType.DOUBLE, "%1.1f");
    columns.add("descrip", Columns.FieldType.STRING, "%s");
    columns.add("siteid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Custom_sitechan(String sta, String chan, long ondate, long chanid, long offdate,
      String ctype, double edepth, double hang, double vang, String descrip, long siteid) {
    setValues(sta, chan, ondate, chanid, offdate, ctype, edepth, hang, vang, descrip, siteid);
  }

  private void setValues(String sta, String chan, long ondate, long chanid, long offdate,
      String ctype, double edepth, double hang, double vang, String descrip, long siteid) {
    this.sta = sta;
    this.chan = chan;
    this.ondate = ondate;
    this.chanid = chanid;
    this.offdate = offdate;
    this.ctype = ctype;
    this.edepth = edepth;
    this.hang = hang;
    this.vang = vang;
    this.descrip = descrip;
    this.siteid = siteid;
  }

  /**
   * Copy constructor.
   */
  public Custom_sitechan(Custom_sitechan other) {
    this.sta = other.getSta();
    this.chan = other.getChan();
    this.ondate = other.getOndate();
    this.chanid = other.getChanid();
    this.offdate = other.getOffdate();
    this.ctype = other.getCtype();
    this.edepth = other.getEdepth();
    this.hang = other.getHang();
    this.vang = other.getVang();
    this.descrip = other.getDescrip();
    this.siteid = other.getSiteid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Custom_sitechan() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(STA_NA, CHAN_NA, ONDATE_NA, CHANID_NA, OFFDATE_NA, CTYPE_NA, EDEPTH_NA, HANG_NA,
        VANG_NA, DESCRIP_NA, SITEID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "chan":
        return chan;
      case "ctype":
        return ctype;
      case "descrip":
        return descrip;
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
      case "ctype":
        ctype = value;
        break;
      case "descrip":
        descrip = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "edepth":
        return edepth;
      case "hang":
        return hang;
      case "vang":
        return vang;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "edepth":
        edepth = value;
        break;
      case "hang":
        hang = value;
        break;
      case "vang":
        vang = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "ondate":
        return ondate;
      case "chanid":
        return chanid;
      case "offdate":
        return offdate;
      case "siteid":
        return siteid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "ondate":
        ondate = value;
        break;
      case "chanid":
        chanid = value;
        break;
      case "offdate":
        offdate = value;
        break;
      case "siteid":
        siteid = value;
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
  public Custom_sitechan(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Custom_sitechan(DataInputStream input) throws IOException {
    this(readString(input), readString(input), input.readLong(), input.readLong(), input.readLong(),
        readString(input), input.readDouble(), input.readDouble(), input.readDouble(),
        readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Custom_sitechan(ByteBuffer input) {
    this(readString(input), readString(input), input.getLong(), input.getLong(), input.getLong(),
        readString(input), input.getDouble(), input.getDouble(), input.getDouble(),
        readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Custom_sitechan(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Custom_sitechan(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getString(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getString(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getString(offset + 10), input.getLong(offset + 11));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[11];
    values[0] = sta;
    values[1] = chan;
    values[2] = ondate;
    values[3] = chanid;
    values[4] = offdate;
    values[5] = ctype;
    values[6] = edepth;
    values[7] = hang;
    values[8] = vang;
    values[9] = descrip;
    values[10] = siteid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[12];
    values[0] = sta;
    values[1] = chan;
    values[2] = ondate;
    values[3] = chanid;
    values[4] = offdate;
    values[5] = ctype;
    values[6] = edepth;
    values[7] = hang;
    values[8] = vang;
    values[9] = descrip;
    values[10] = siteid;
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
    writeString(output, sta);
    writeString(output, chan);
    output.writeLong(ondate);
    output.writeLong(chanid);
    output.writeLong(offdate);
    writeString(output, ctype);
    output.writeDouble(edepth);
    output.writeDouble(hang);
    output.writeDouble(vang);
    writeString(output, descrip);
    output.writeLong(siteid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, sta);
    writeString(output, chan);
    output.putLong(ondate);
    output.putLong(chanid);
    output.putLong(offdate);
    writeString(output, ctype);
    output.putDouble(edepth);
    output.putDouble(hang);
    output.putDouble(vang);
    writeString(output, descrip);
    output.putLong(siteid);
  }

  /**
   * Read a Collection of Custom_sitechan objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Custom_sitechan objects.
   * @throws IOException
   */
  static public void readCustom_sitechans(BufferedReader input, Collection<Custom_sitechan> rows)
      throws IOException {
    String[] saved = Custom_sitechan.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Custom_sitechan
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Custom_sitechan(new Scanner(line)));
    }
    input.close();
    Custom_sitechan.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Custom_sitechan objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Custom_sitechan objects.
   * @throws IOException
   */
  static public void readCustom_sitechans(File inputFile, Collection<Custom_sitechan> rows)
      throws IOException {
    readCustom_sitechans(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Custom_sitechan objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Custom_sitechan objects.
   * @throws IOException
   */
  static public void readCustom_sitechans(InputStream inputStream, Collection<Custom_sitechan> rows)
      throws IOException {
    readCustom_sitechans(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Custom_sitechan objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Custom_sitechan objects
   * @throws IOException
   */
  static public Set<Custom_sitechan> readCustom_sitechans(BufferedReader input) throws IOException {
    Set<Custom_sitechan> rows = new LinkedHashSet<Custom_sitechan>();
    readCustom_sitechans(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Custom_sitechan objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Custom_sitechan objects
   * @throws IOException
   */
  static public Set<Custom_sitechan> readCustom_sitechans(File inputFile) throws IOException {
    return readCustom_sitechans(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Custom_sitechan objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Custom_sitechan objects
   * @throws IOException
   */
  static public Set<Custom_sitechan> readCustom_sitechans(InputStream input) throws IOException {
    return readCustom_sitechans(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Custom_sitechan objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param custom_sitechans the Custom_sitechan objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Custom_sitechan> custom_sitechans)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Custom_sitechan custom_sitechan : custom_sitechans)
      custom_sitechan.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Custom_sitechan objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param custom_sitechans the Custom_sitechan objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Custom_sitechan> custom_sitechans, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Custom_sitechan custom_sitechan : custom_sitechans) {
        int i = 0;
        statement.setString(++i, custom_sitechan.sta);
        statement.setString(++i, custom_sitechan.chan);
        statement.setLong(++i, custom_sitechan.ondate);
        statement.setLong(++i, custom_sitechan.chanid);
        statement.setLong(++i, custom_sitechan.offdate);
        statement.setString(++i, custom_sitechan.ctype);
        statement.setDouble(++i, custom_sitechan.edepth);
        statement.setDouble(++i, custom_sitechan.hang);
        statement.setDouble(++i, custom_sitechan.vang);
        statement.setString(++i, custom_sitechan.descrip);
        statement.setLong(++i, custom_sitechan.siteid);
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
   *        Custom_sitechan table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Custom_sitechan> readCustom_sitechans(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Custom_sitechan> results = new HashSet<Custom_sitechan>();
    readCustom_sitechans(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Custom_sitechan table.
   * @param custom_sitechans
   * @throws SQLException
   */
  static public void readCustom_sitechans(Connection connection, String selectStatement,
      Set<Custom_sitechan> custom_sitechans) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        custom_sitechans.add(new Custom_sitechan(rs));
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
   * this Custom_sitechan object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Custom_sitechan object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "sta, chan, ondate, chanid, offdate, ctype, edepth, hang, vang, descrip, siteid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(chan).append("', ");
    sql.append(Long.toString(ondate)).append(", ");
    sql.append(Long.toString(chanid)).append(", ");
    sql.append(Long.toString(offdate)).append(", ");
    sql.append("'").append(ctype).append("', ");
    sql.append(Double.toString(edepth)).append(", ");
    sql.append(Double.toString(hang)).append(", ");
    sql.append(Double.toString(vang)).append(", ");
    sql.append("'").append(descrip).append("', ");
    sql.append(Long.toString(siteid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Custom_sitechan in the database. Primary and unique keys are set, if
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
   * Create a table of type Custom_sitechan in the database
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
   * Generate a sql script to create a table of type Custom_sitechan in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Custom_sitechan in the database
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
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("ondate       number(8)            NOT NULL,\n");
    buf.append("chanid       number(8)            NOT NULL,\n");
    buf.append("offdate      number(8)            NOT NULL,\n");
    buf.append("ctype        varchar2(4)          NOT NULL,\n");
    buf.append("edepth       float(24)            NOT NULL,\n");
    buf.append("hang         float(24)            NOT NULL,\n");
    buf.append("vang         float(24)            NOT NULL,\n");
    buf.append("descrip      varchar2(50)         NOT NULL,\n");
    buf.append("siteid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (chanid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (sta,chan,ondate)");
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
    return 140;
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
    return (other instanceof Custom_sitechan) && ((Custom_sitechan) other).chanid == chanid;
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
    return (other instanceof Custom_sitechan) && ((Custom_sitechan) other).sta.equals(sta)
        && ((Custom_sitechan) other).chan.equals(chan)
        && ((Custom_sitechan) other).ondate == ondate;
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
  public Custom_sitechan setSta(String sta) {
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
  public Custom_sitechan setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
    setHash(null);
    return this;
  }

  /**
   * Turn on date. This column is the julian date that the node is promoted for use
   * 
   * @return ondate
   */
  public long getOndate() {
    return ondate;
  }

  /**
   * Turn on date. This column is the julian date that the node is promoted for use
   * 
   * @param ondate
   * @throws IllegalArgumentException if ondate >= 100000000
   */
  public Custom_sitechan setOndate(long ondate) {
    if (ondate >= 100000000L)
      throw new IllegalArgumentException("ondate=" + ondate + " but cannot be >= 100000000");
    this.ondate = ondate;
    setHash(null);
    return this;
  }

  /**
   * Channel identifier. This value is a surrogate key used to uniquely identify a specific
   * recording. The column chanid duplicates the information of the compound key
   * <I>sta</I>/<I>chan</I>/<I>time</I>.
   * 
   * @return chanid
   */
  public long getChanid() {
    return chanid;
  }

  /**
   * Channel identifier. This value is a surrogate key used to uniquely identify a specific
   * recording. The column chanid duplicates the information of the compound key
   * <I>sta</I>/<I>chan</I>/<I>time</I>.
   * 
   * @param chanid
   * @throws IllegalArgumentException if chanid >= 100000000
   */
  public Custom_sitechan setChanid(long chanid) {
    if (chanid >= 100000000L)
      throw new IllegalArgumentException("chanid=" + chanid + " but cannot be >= 100000000");
    this.chanid = chanid;
    setHash(null);
    return this;
  }

  /**
   * Turn off date. This column is the julian date that the node is no longer used
   * 
   * @return offdate
   */
  public long getOffdate() {
    return offdate;
  }

  /**
   * Turn off date. This column is the julian date that the node is no longer used
   * 
   * @param offdate
   * @throws IllegalArgumentException if offdate >= 100000000
   */
  public Custom_sitechan setOffdate(long offdate) {
    if (offdate >= 100000000L)
      throw new IllegalArgumentException("offdate=" + offdate + " but cannot be >= 100000000");
    this.offdate = offdate;
    setHash(null);
    return this;
  }

  /**
   * Channel type. This column specifies the type of data channel: normal (n) -- a normal instrument
   * response, beam (b) -- a coherent beam formed with array data, or incoherent (i) -- an
   * incoherent beam or energy stack.
   * 
   * @return ctype
   */
  public String getCtype() {
    return ctype;
  }

  /**
   * Channel type. This column specifies the type of data channel: normal (n) -- a normal instrument
   * response, beam (b) -- a coherent beam formed with array data, or incoherent (i) -- an
   * incoherent beam or energy stack.
   * 
   * @param ctype
   * @throws IllegalArgumentException if ctype.length() >= 4
   */
  public Custom_sitechan setCtype(String ctype) {
    if (ctype.length() > 4)
      throw new IllegalArgumentException(
          String.format("ctype.length() cannot be > 4.  ctype=%s", ctype));
    this.ctype = ctype;
    setHash(null);
    return this;
  }

  /**
   * Emplacement depth at which instrument is positioned relative to the value of <I>elev</I> in the
   * <B>site</B> table
   * <p>
   * Units: km
   * 
   * @return edepth
   */
  public double getEdepth() {
    return edepth;
  }

  /**
   * Emplacement depth at which instrument is positioned relative to the value of <I>elev</I> in the
   * <B>site</B> table
   * <p>
   * Units: km
   * 
   * @param edepth
   */
  public Custom_sitechan setEdepth(double edepth) {
    this.edepth = edepth;
    setHash(null);
    return this;
  }

  /**
   * Horizontal orientation of seismometer. This column specifies the orientation of the seismometer
   * in the horizontal plane, measured clockwise from North. For a North-South orientation with the
   * seismometer pointing toward the North, hang = 0.0 For East-West orientation with the
   * seismometer pointing toward the West, hang = 270.0 The hang is indeterminate for some cases
   * such as horizontal beam channels, hydrophones, infrasound, and various state of health channels
   * The hang is indeterminate for some cases such as horizontal beam channels, hydrophones,
   * infrasound, and various state of health channels (see <I>vang</I>)
   * <p>
   * Units: degree
   * 
   * @return hang
   */
  public double getHang() {
    return hang;
  }

  /**
   * Horizontal orientation of seismometer. This column specifies the orientation of the seismometer
   * in the horizontal plane, measured clockwise from North. For a North-South orientation with the
   * seismometer pointing toward the North, hang = 0.0 For East-West orientation with the
   * seismometer pointing toward the West, hang = 270.0 The hang is indeterminate for some cases
   * such as horizontal beam channels, hydrophones, infrasound, and various state of health channels
   * The hang is indeterminate for some cases such as horizontal beam channels, hydrophones,
   * infrasound, and various state of health channels (see <I>vang</I>)
   * <p>
   * Units: degree
   * 
   * @param hang
   */
  public Custom_sitechan setHang(double hang) {
    this.hang = hang;
    setHash(null);
    return this;
  }

  /**
   * Vertical orientation of seismometer. This column measures the angle between the sensitive axis
   * of a seismometer and the outward-pointing vertical direction. For a vertically oriented
   * seismometer, <I>vang</I> = 0 For a horizontally oriented seismometer, <I>vang</I> = 90 For
   * vertical beams, <I>vang</I> = 0 is indeterminate for some cases such as hydrophones,
   * infrasound, and various state of health channels For vertical beans, <I>vang</I> = 0 is
   * indeterminate for some cases such as hydrophones, infrasound, and various state of health
   * channels (see <I>hang</I>)
   * <p>
   * Units: degree
   * 
   * @return vang
   */
  public double getVang() {
    return vang;
  }

  /**
   * Vertical orientation of seismometer. This column measures the angle between the sensitive axis
   * of a seismometer and the outward-pointing vertical direction. For a vertically oriented
   * seismometer, <I>vang</I> = 0 For a horizontally oriented seismometer, <I>vang</I> = 90 For
   * vertical beams, <I>vang</I> = 0 is indeterminate for some cases such as hydrophones,
   * infrasound, and various state of health channels For vertical beans, <I>vang</I> = 0 is
   * indeterminate for some cases such as hydrophones, infrasound, and various state of health
   * channels (see <I>hang</I>)
   * <p>
   * Units: degree
   * 
   * @param vang
   */
  public Custom_sitechan setVang(double vang) {
    this.vang = vang;
    setHash(null);
    return this;
  }

  /**
   * Text description, for <B>sitechan</B> it is the channel description
   * 
   * @return descrip
   */
  public String getDescrip() {
    return descrip;
  }

  /**
   * Text description, for <B>sitechan</B> it is the channel description
   * 
   * @param descrip
   * @throws IllegalArgumentException if descrip.length() >= 50
   */
  public Custom_sitechan setDescrip(String descrip) {
    if (descrip.length() > 50)
      throw new IllegalArgumentException(
          String.format("descrip.length() cannot be > 50.  descrip=%s", descrip));
    this.descrip = descrip;
    setHash(null);
    return this;
  }

  /**
   * Site identifier that either uniquely idenitifes a station or relations a record to a station
   * 
   * @return siteid
   */
  public long getSiteid() {
    return siteid;
  }

  /**
   * Site identifier that either uniquely idenitifes a station or relations a record to a station
   * 
   * @param siteid
   * @throws IllegalArgumentException if siteid >= 1000000000
   */
  public Custom_sitechan setSiteid(long siteid) {
    if (siteid >= 1000000000L)
      throw new IllegalArgumentException("siteid=" + siteid + " but cannot be >= 1000000000");
    this.siteid = siteid;
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
