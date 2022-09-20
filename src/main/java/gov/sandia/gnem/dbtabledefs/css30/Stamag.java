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
package gov.sandia.gnem.dbtabledefs.css30;

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
 * stamag
 */
public class Stamag extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Network magnitude identifier. This key is assigned to identify a network magnitude in the
   * netmag relation. It is required for every network magnitude. Magnitudes given in origin must
   * reference a network magnitude with magid = mbid, mlid or msid, whichever is appropriate. See
   * mbid, mlid, or msid.
   */
  private long magid;

  static final public long MAGID_NA = Long.MIN_VALUE;

  /**
   * Station code. This is the common code-name of a seismic observatory. Generally only three or
   * four characters are used.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique sta, chan and time. This number is used in the assoc relation along with the origin
   * identifier to link arrival and origin.
   */
  private long arid;

  static final public long ARID_NA = -1;

  /**
   * Origin identification. Each origin is assigned a unique positive integer which identifies it in
   * a data base. The orid is used to identify one of the many hypotheses of the actual location of
   * the event.
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Event identifier. Each event is assigned a unique positive integer which identifies it in a
   * database. It is possible for several records in the origin relation to have the same evid. This
   * indicates there are several opinions about the location of the event.
   */
  private long evid;

  static final public long EVID_NA = -1;

  /**
   * Associated phase. This field holds the identity of a phase which has been associated to an
   * event. Standard seismological labels for phases are used (e.g., P, PKP, PcP, pP, etc.). Both
   * upper and lower case letters are available and should be used when appropriate, for example, pP
   * or PcP. See iphase.
   */
  private String phase;

  static final public String PHASE_NA = null;

  /**
   * Magnitude type. This character string is used to specify whether the magnitude value represents
   * mb (body wave magnitude), ms (surface wave magnitude), ml (local magnitude) or other
   * appropriate magnitude measure. See imb, iml, ims, magnitude, mb, ml, and ms.
   */
  private String magtype;

  static final public String MAGTYPE_NA = null;

  /**
   * Magnitude. This gives the magnitude value of the type indicated in magtype. It is derived in a
   * variety of ways, which are not necessarily linked directly to an arrival . See imb, iml, ims,
   * magtype, mb, ml, and ms.
   */
  private double magnitude;

  static final public double MAGNITUDE_NA = Double.NaN;

  /**
   * Magnitude uncertainty. This is the standard deviation of the accompanying magnitude
   * measurement.
   */
  private double uncertainty;

  static final public double UNCERTAINTY_NA = -1.0;

  /**
   * Author. This records the originator of an arrival (in arrival relation) or origin (in origin
   * relation). Possibilities include externally supplied arrivals identified according to their
   * original source, such as WMO, NEIS, CAN(adian), UK(array), etc. This may also be an identifier
   * of an application generating the attribute, such as an automated interpretation or
   * signal-processing program.
   */
  private String auth;

  static final public String AUTH_NA = "-";

  /**
   * Comment identification. This is a key used to point to free-form comments entered in the remark
   * relation. These comments store additional information about a tuple in another relation. Within
   * the remark relation, there may be many tuples with the same commid and different lineno, but
   * the same commid will appear in only one other tuple among the rest of the relations in the
   * database. See lineno.
   */
  private long commid;

  static final public long COMMID_NA = -1;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("magid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("arid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("evid", Columns.FieldType.LONG, "%d");
    columns.add("phase", Columns.FieldType.STRING, "%s");
    columns.add("magtype", Columns.FieldType.STRING, "%s");
    columns.add("magnitude", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("uncertainty", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Stamag(long magid, String sta, long arid, long orid, long evid, String phase,
      String magtype, double magnitude, double uncertainty, String auth, long commid) {
    setValues(magid, sta, arid, orid, evid, phase, magtype, magnitude, uncertainty, auth, commid);
  }

  private void setValues(long magid, String sta, long arid, long orid, long evid, String phase,
      String magtype, double magnitude, double uncertainty, String auth, long commid) {
    this.magid = magid;
    this.sta = sta;
    this.arid = arid;
    this.orid = orid;
    this.evid = evid;
    this.phase = phase;
    this.magtype = magtype;
    this.magnitude = magnitude;
    this.uncertainty = uncertainty;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Stamag(Stamag other) {
    this.magid = other.getMagid();
    this.sta = other.getSta();
    this.arid = other.getArid();
    this.orid = other.getOrid();
    this.evid = other.getEvid();
    this.phase = other.getPhase();
    this.magtype = other.getMagtype();
    this.magnitude = other.getMagnitude();
    this.uncertainty = other.getUncertainty();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Stamag() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MAGID_NA, STA_NA, ARID_NA, ORID_NA, EVID_NA, PHASE_NA, MAGTYPE_NA, MAGNITUDE_NA,
        UNCERTAINTY_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "phase":
        return phase;
      case "magtype":
        return magtype;
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
      case "phase":
        phase = value;
        break;
      case "magtype":
        magtype = value;
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
      case "magnitude":
        return magnitude;
      case "uncertainty":
        return uncertainty;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "magnitude":
        magnitude = value;
        break;
      case "uncertainty":
        uncertainty = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "magid":
        return magid;
      case "arid":
        return arid;
      case "orid":
        return orid;
      case "evid":
        return evid;
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
      case "magid":
        magid = value;
        break;
      case "arid":
        arid = value;
        break;
      case "orid":
        orid = value;
        break;
      case "evid":
        evid = value;
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
  public Stamag(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Stamag(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), input.readLong(), input.readLong(), input.readLong(),
        readString(input), readString(input), input.readDouble(), input.readDouble(),
        readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Stamag(ByteBuffer input) {
    this(input.getLong(), readString(input), input.getLong(), input.getLong(), input.getLong(),
        readString(input), readString(input), input.getDouble(), input.getDouble(),
        readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Stamag(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Stamag(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getString(offset + 10), input.getLong(offset + 11));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[11];
    values[0] = magid;
    values[1] = sta;
    values[2] = arid;
    values[3] = orid;
    values[4] = evid;
    values[5] = phase;
    values[6] = magtype;
    values[7] = magnitude;
    values[8] = uncertainty;
    values[9] = auth;
    values[10] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[12];
    values[0] = magid;
    values[1] = sta;
    values[2] = arid;
    values[3] = orid;
    values[4] = evid;
    values[5] = phase;
    values[6] = magtype;
    values[7] = magnitude;
    values[8] = uncertainty;
    values[9] = auth;
    values[10] = commid;
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
    output.writeLong(magid);
    writeString(output, sta);
    output.writeLong(arid);
    output.writeLong(orid);
    output.writeLong(evid);
    writeString(output, phase);
    writeString(output, magtype);
    output.writeDouble(magnitude);
    output.writeDouble(uncertainty);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(magid);
    writeString(output, sta);
    output.putLong(arid);
    output.putLong(orid);
    output.putLong(evid);
    writeString(output, phase);
    writeString(output, magtype);
    output.putDouble(magnitude);
    output.putDouble(uncertainty);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Stamag objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Stamag objects.
   * @throws IOException
   */
  static public void readStamags(BufferedReader input, Collection<Stamag> rows) throws IOException {
    String[] saved = Stamag.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Stamag.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Stamag(new Scanner(line)));
    }
    input.close();
    Stamag.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Stamag objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Stamag objects.
   * @throws IOException
   */
  static public void readStamags(File inputFile, Collection<Stamag> rows) throws IOException {
    readStamags(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Stamag objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Stamag objects.
   * @throws IOException
   */
  static public void readStamags(InputStream inputStream, Collection<Stamag> rows)
      throws IOException {
    readStamags(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Stamag objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Stamag objects
   * @throws IOException
   */
  static public Set<Stamag> readStamags(BufferedReader input) throws IOException {
    Set<Stamag> rows = new LinkedHashSet<Stamag>();
    readStamags(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Stamag objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Stamag objects
   * @throws IOException
   */
  static public Set<Stamag> readStamags(File inputFile) throws IOException {
    return readStamags(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Stamag objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Stamag objects
   * @throws IOException
   */
  static public Set<Stamag> readStamags(InputStream input) throws IOException {
    return readStamags(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Stamag objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param stamags the Stamag objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Stamag> stamags) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Stamag stamag : stamags)
      stamag.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Stamag objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param stamags the Stamag objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Stamag> stamags, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Stamag stamag : stamags) {
        int i = 0;
        statement.setLong(++i, stamag.magid);
        statement.setString(++i, stamag.sta);
        statement.setLong(++i, stamag.arid);
        statement.setLong(++i, stamag.orid);
        statement.setLong(++i, stamag.evid);
        statement.setString(++i, stamag.phase);
        statement.setString(++i, stamag.magtype);
        statement.setDouble(++i, stamag.magnitude);
        statement.setDouble(++i, stamag.uncertainty);
        statement.setString(++i, stamag.auth);
        statement.setLong(++i, stamag.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Stamag
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Stamag> readStamags(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Stamag> results = new HashSet<Stamag>();
    readStamags(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Stamag
   *        table.
   * @param stamags
   * @throws SQLException
   */
  static public void readStamags(Connection connection, String selectStatement, Set<Stamag> stamags)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        stamags.add(new Stamag(rs));
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
   * this Stamag object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Stamag object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "magid, sta, arid, orid, evid, phase, magtype, magnitude, uncertainty, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(magid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append(Long.toString(arid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(evid)).append(", ");
    sql.append("'").append(phase).append("', ");
    sql.append("'").append(magtype).append("', ");
    sql.append(Double.toString(magnitude)).append(", ");
    sql.append(Double.toString(uncertainty)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Stamag in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Stamag in the database
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
   * Generate a sql script to create a table of type Stamag in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Stamag in the database
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
    buf.append("magid        number(8)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("arid         number(8)            NOT NULL,\n");
    buf.append("orid         number(8)            NOT NULL,\n");
    buf.append("evid         number(8)            NOT NULL,\n");
    buf.append("phase        varchar2(8)          NOT NULL,\n");
    buf.append("magtype      varchar2(6)          NOT NULL,\n");
    buf.append("magnitude    float(24)            NOT NULL,\n");
    buf.append("uncertainty  float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(15)         NOT NULL,\n");
    buf.append("commid       number(8)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (magid,sta)");
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
    return 107;
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
    return (other instanceof Stamag) && ((Stamag) other).magid == magid
        && ((Stamag) other).sta.equals(sta);
  }

  /**
   * Network magnitude identifier. This key is assigned to identify a network magnitude in the
   * netmag relation. It is required for every network magnitude. Magnitudes given in origin must
   * reference a network magnitude with magid = mbid, mlid or msid, whichever is appropriate. See
   * mbid, mlid, or msid.
   * 
   * @return magid
   */
  public long getMagid() {
    return magid;
  }

  /**
   * Network magnitude identifier. This key is assigned to identify a network magnitude in the
   * netmag relation. It is required for every network magnitude. Magnitudes given in origin must
   * reference a network magnitude with magid = mbid, mlid or msid, whichever is appropriate. See
   * mbid, mlid, or msid.
   * 
   * @param magid
   * @throws IllegalArgumentException if magid >= 100000000
   */
  public Stamag setMagid(long magid) {
    if (magid >= 100000000L)
      throw new IllegalArgumentException("magid=" + magid + " but cannot be >= 100000000");
    this.magid = magid;
    setHash(null);
    return this;
  }

  /**
   * Station code. This is the common code-name of a seismic observatory. Generally only three or
   * four characters are used.
   * 
   * @return sta
   */
  public String getSta() {
    return sta;
  }

  /**
   * Station code. This is the common code-name of a seismic observatory. Generally only three or
   * four characters are used.
   * 
   * @param sta
   * @throws IllegalArgumentException if sta.length() >= 6
   */
  public Stamag setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique sta, chan and time. This number is used in the assoc relation along with the origin
   * identifier to link arrival and origin.
   * 
   * @return arid
   */
  public long getArid() {
    return arid;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique sta, chan and time. This number is used in the assoc relation along with the origin
   * identifier to link arrival and origin.
   * 
   * @param arid
   * @throws IllegalArgumentException if arid >= 100000000
   */
  public Stamag setArid(long arid) {
    if (arid >= 100000000L)
      throw new IllegalArgumentException("arid=" + arid + " but cannot be >= 100000000");
    this.arid = arid;
    setHash(null);
    return this;
  }

  /**
   * Origin identification. Each origin is assigned a unique positive integer which identifies it in
   * a data base. The orid is used to identify one of the many hypotheses of the actual location of
   * the event.
   * 
   * @return orid
   */
  public long getOrid() {
    return orid;
  }

  /**
   * Origin identification. Each origin is assigned a unique positive integer which identifies it in
   * a data base. The orid is used to identify one of the many hypotheses of the actual location of
   * the event.
   * 
   * @param orid
   * @throws IllegalArgumentException if orid >= 100000000
   */
  public Stamag setOrid(long orid) {
    if (orid >= 100000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 100000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Event identifier. Each event is assigned a unique positive integer which identifies it in a
   * database. It is possible for several records in the origin relation to have the same evid. This
   * indicates there are several opinions about the location of the event.
   * 
   * @return evid
   */
  public long getEvid() {
    return evid;
  }

  /**
   * Event identifier. Each event is assigned a unique positive integer which identifies it in a
   * database. It is possible for several records in the origin relation to have the same evid. This
   * indicates there are several opinions about the location of the event.
   * 
   * @param evid
   * @throws IllegalArgumentException if evid >= 100000000
   */
  public Stamag setEvid(long evid) {
    if (evid >= 100000000L)
      throw new IllegalArgumentException("evid=" + evid + " but cannot be >= 100000000");
    this.evid = evid;
    setHash(null);
    return this;
  }

  /**
   * Associated phase. This field holds the identity of a phase which has been associated to an
   * event. Standard seismological labels for phases are used (e.g., P, PKP, PcP, pP, etc.). Both
   * upper and lower case letters are available and should be used when appropriate, for example, pP
   * or PcP. See iphase.
   * 
   * @return phase
   */
  public String getPhase() {
    return phase;
  }

  /**
   * Associated phase. This field holds the identity of a phase which has been associated to an
   * event. Standard seismological labels for phases are used (e.g., P, PKP, PcP, pP, etc.). Both
   * upper and lower case letters are available and should be used when appropriate, for example, pP
   * or PcP. See iphase.
   * 
   * @param phase
   * @throws IllegalArgumentException if phase.length() >= 8
   */
  public Stamag setPhase(String phase) {
    if (phase.length() > 8)
      throw new IllegalArgumentException(
          String.format("phase.length() cannot be > 8.  phase=%s", phase));
    this.phase = phase;
    setHash(null);
    return this;
  }

  /**
   * Magnitude type. This character string is used to specify whether the magnitude value represents
   * mb (body wave magnitude), ms (surface wave magnitude), ml (local magnitude) or other
   * appropriate magnitude measure. See imb, iml, ims, magnitude, mb, ml, and ms.
   * 
   * @return magtype
   */
  public String getMagtype() {
    return magtype;
  }

  /**
   * Magnitude type. This character string is used to specify whether the magnitude value represents
   * mb (body wave magnitude), ms (surface wave magnitude), ml (local magnitude) or other
   * appropriate magnitude measure. See imb, iml, ims, magnitude, mb, ml, and ms.
   * 
   * @param magtype
   * @throws IllegalArgumentException if magtype.length() >= 6
   */
  public Stamag setMagtype(String magtype) {
    if (magtype.length() > 6)
      throw new IllegalArgumentException(
          String.format("magtype.length() cannot be > 6.  magtype=%s", magtype));
    this.magtype = magtype;
    setHash(null);
    return this;
  }

  /**
   * Magnitude. This gives the magnitude value of the type indicated in magtype. It is derived in a
   * variety of ways, which are not necessarily linked directly to an arrival . See imb, iml, ims,
   * magtype, mb, ml, and ms.
   * 
   * @return magnitude
   */
  public double getMagnitude() {
    return magnitude;
  }

  /**
   * Magnitude. This gives the magnitude value of the type indicated in magtype. It is derived in a
   * variety of ways, which are not necessarily linked directly to an arrival . See imb, iml, ims,
   * magtype, mb, ml, and ms.
   * 
   * @param magnitude
   */
  public Stamag setMagnitude(double magnitude) {
    this.magnitude = magnitude;
    setHash(null);
    return this;
  }

  /**
   * Magnitude uncertainty. This is the standard deviation of the accompanying magnitude
   * measurement.
   * 
   * @return uncertainty
   */
  public double getUncertainty() {
    return uncertainty;
  }

  /**
   * Magnitude uncertainty. This is the standard deviation of the accompanying magnitude
   * measurement.
   * 
   * @param uncertainty
   */
  public Stamag setUncertainty(double uncertainty) {
    this.uncertainty = uncertainty;
    setHash(null);
    return this;
  }

  /**
   * Author. This records the originator of an arrival (in arrival relation) or origin (in origin
   * relation). Possibilities include externally supplied arrivals identified according to their
   * original source, such as WMO, NEIS, CAN(adian), UK(array), etc. This may also be an identifier
   * of an application generating the attribute, such as an automated interpretation or
   * signal-processing program.
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * Author. This records the originator of an arrival (in arrival relation) or origin (in origin
   * relation). Possibilities include externally supplied arrivals identified according to their
   * original source, such as WMO, NEIS, CAN(adian), UK(array), etc. This may also be an identifier
   * of an application generating the attribute, such as an automated interpretation or
   * signal-processing program.
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 15
   */
  public Stamag setAuth(String auth) {
    if (auth.length() > 15)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 15.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * Comment identification. This is a key used to point to free-form comments entered in the remark
   * relation. These comments store additional information about a tuple in another relation. Within
   * the remark relation, there may be many tuples with the same commid and different lineno, but
   * the same commid will appear in only one other tuple among the rest of the relations in the
   * database. See lineno.
   * 
   * @return commid
   */
  public long getCommid() {
    return commid;
  }

  /**
   * Comment identification. This is a key used to point to free-form comments entered in the remark
   * relation. These comments store additional information about a tuple in another relation. Within
   * the remark relation, there may be many tuples with the same commid and different lineno, but
   * the same commid will appear in only one other tuple among the rest of the relations in the
   * database. See lineno.
   * 
   * @param commid
   * @throws IllegalArgumentException if commid >= 100000000
   */
  public Stamag setCommid(long commid) {
    if (commid >= 100000000L)
      throw new IllegalArgumentException("commid=" + commid + " but cannot be >= 100000000");
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
    return "CSS3.0";
  }

}
