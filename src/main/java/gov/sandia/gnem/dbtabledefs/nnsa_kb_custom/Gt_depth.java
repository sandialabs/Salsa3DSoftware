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
 * gt_depth
 */
public class Gt_depth extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * GT depth identifier
   */
  private long depthid;

  static final public long DEPTHID_NA = Long.MIN_VALUE;

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   */
  private long evid;

  static final public long EVID_NA = Long.MIN_VALUE;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = -1;

  /**
   * Event depth
   * <p>
   * Units: km
   */
  private double depth;

  static final public double DEPTH_NA = Double.NaN;

  /**
   * The maximum difference in km between the GT value and the true value of an observation
   * <p>
   * Units: km
   */
  private double gtlevel_km;

  static final public double GTLEVEL_KM_NA = Double.NaN;

  /**
   * Confidence in GT level
   */
  private double confidence;

  static final public double CONFIDENCE_NA = Double.NaN;

  /**
   * Description of the method used to obtain the GT value.
   */
  private String method;

  static final public String METHOD_NA = null;

  /**
   * The organization code of the organization that produced this data.
   */
  private String contrib_org;

  static final public String CONTRIB_ORG_NA = null;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = "-";

  /**
   * Author who loaded data
   */
  private String ldauth;

  static final public String LDAUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("depthid", Columns.FieldType.LONG, "%d");
    columns.add("evid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("depth", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("gtlevel_km", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("confidence", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("method", Columns.FieldType.STRING, "%s");
    columns.add("contrib_org", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("ldauth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Gt_depth(long depthid, long evid, long orid, double depth, double gtlevel_km,
      double confidence, String method, String contrib_org, String auth, String ldauth) {
    setValues(depthid, evid, orid, depth, gtlevel_km, confidence, method, contrib_org, auth,
        ldauth);
  }

  private void setValues(long depthid, long evid, long orid, double depth, double gtlevel_km,
      double confidence, String method, String contrib_org, String auth, String ldauth) {
    this.depthid = depthid;
    this.evid = evid;
    this.orid = orid;
    this.depth = depth;
    this.gtlevel_km = gtlevel_km;
    this.confidence = confidence;
    this.method = method;
    this.contrib_org = contrib_org;
    this.auth = auth;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Gt_depth(Gt_depth other) {
    this.depthid = other.getDepthid();
    this.evid = other.getEvid();
    this.orid = other.getOrid();
    this.depth = other.getDepth();
    this.gtlevel_km = other.getGtlevel_km();
    this.confidence = other.getConfidence();
    this.method = other.getMethod();
    this.contrib_org = other.getContrib_org();
    this.auth = other.getAuth();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Gt_depth() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(DEPTHID_NA, EVID_NA, ORID_NA, DEPTH_NA, GTLEVEL_KM_NA, CONFIDENCE_NA, METHOD_NA,
        CONTRIB_ORG_NA, AUTH_NA, LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "method":
        return method;
      case "contrib_org":
        return contrib_org;
      case "auth":
        return auth;
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
      case "method":
        method = value;
        break;
      case "contrib_org":
        contrib_org = value;
        break;
      case "auth":
        auth = value;
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
      case "depth":
        return depth;
      case "gtlevel_km":
        return gtlevel_km;
      case "confidence":
        return confidence;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "depth":
        depth = value;
        break;
      case "gtlevel_km":
        gtlevel_km = value;
        break;
      case "confidence":
        confidence = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "depthid":
        return depthid;
      case "evid":
        return evid;
      case "orid":
        return orid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "depthid":
        depthid = value;
        break;
      case "evid":
        evid = value;
        break;
      case "orid":
        orid = value;
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
  public Gt_depth(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Gt_depth(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readDouble(),
        input.readDouble(), input.readDouble(), readString(input), readString(input),
        readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Gt_depth(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getDouble(), input.getDouble(),
        input.getDouble(), readString(input), readString(input), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Gt_depth(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Gt_depth(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getString(offset + 9),
        input.getString(offset + 10));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[10];
    values[0] = depthid;
    values[1] = evid;
    values[2] = orid;
    values[3] = depth;
    values[4] = gtlevel_km;
    values[5] = confidence;
    values[6] = method;
    values[7] = contrib_org;
    values[8] = auth;
    values[9] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[11];
    values[0] = depthid;
    values[1] = evid;
    values[2] = orid;
    values[3] = depth;
    values[4] = gtlevel_km;
    values[5] = confidence;
    values[6] = method;
    values[7] = contrib_org;
    values[8] = auth;
    values[9] = ldauth;
    values[10] = lddate;
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
    output.writeLong(depthid);
    output.writeLong(evid);
    output.writeLong(orid);
    output.writeDouble(depth);
    output.writeDouble(gtlevel_km);
    output.writeDouble(confidence);
    writeString(output, method);
    writeString(output, contrib_org);
    writeString(output, auth);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(depthid);
    output.putLong(evid);
    output.putLong(orid);
    output.putDouble(depth);
    output.putDouble(gtlevel_km);
    output.putDouble(confidence);
    writeString(output, method);
    writeString(output, contrib_org);
    writeString(output, auth);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Gt_depth objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Gt_depth objects.
   * @throws IOException
   */
  static public void readGt_depths(BufferedReader input, Collection<Gt_depth> rows)
      throws IOException {
    String[] saved = Gt_depth.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Gt_depth
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Gt_depth(new Scanner(line)));
    }
    input.close();
    Gt_depth.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Gt_depth objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Gt_depth objects.
   * @throws IOException
   */
  static public void readGt_depths(File inputFile, Collection<Gt_depth> rows) throws IOException {
    readGt_depths(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Gt_depth objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Gt_depth objects.
   * @throws IOException
   */
  static public void readGt_depths(InputStream inputStream, Collection<Gt_depth> rows)
      throws IOException {
    readGt_depths(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Gt_depth objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Gt_depth objects
   * @throws IOException
   */
  static public Set<Gt_depth> readGt_depths(BufferedReader input) throws IOException {
    Set<Gt_depth> rows = new LinkedHashSet<Gt_depth>();
    readGt_depths(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Gt_depth objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Gt_depth objects
   * @throws IOException
   */
  static public Set<Gt_depth> readGt_depths(File inputFile) throws IOException {
    return readGt_depths(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Gt_depth objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Gt_depth objects
   * @throws IOException
   */
  static public Set<Gt_depth> readGt_depths(InputStream input) throws IOException {
    return readGt_depths(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Gt_depth objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param gt_depths the Gt_depth objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Gt_depth> gt_depths)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Gt_depth gt_depth : gt_depths)
      gt_depth.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Gt_depth objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param gt_depths the Gt_depth objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Gt_depth> gt_depths, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?)");
      for (Gt_depth gt_depth : gt_depths) {
        int i = 0;
        statement.setLong(++i, gt_depth.depthid);
        statement.setLong(++i, gt_depth.evid);
        statement.setLong(++i, gt_depth.orid);
        statement.setDouble(++i, gt_depth.depth);
        statement.setDouble(++i, gt_depth.gtlevel_km);
        statement.setDouble(++i, gt_depth.confidence);
        statement.setString(++i, gt_depth.method);
        statement.setString(++i, gt_depth.contrib_org);
        statement.setString(++i, gt_depth.auth);
        statement.setString(++i, gt_depth.ldauth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Gt_depth
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Gt_depth> readGt_depths(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Gt_depth> results = new HashSet<Gt_depth>();
    readGt_depths(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Gt_depth
   *        table.
   * @param gt_depths
   * @throws SQLException
   */
  static public void readGt_depths(Connection connection, String selectStatement,
      Set<Gt_depth> gt_depths) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        gt_depths.add(new Gt_depth(rs));
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
   * this Gt_depth object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Gt_depth object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "depthid, evid, orid, depth, gtlevel_km, confidence, method, contrib_org, auth, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(depthid)).append(", ");
    sql.append(Long.toString(evid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Double.toString(depth)).append(", ");
    sql.append(Double.toString(gtlevel_km)).append(", ");
    sql.append(Double.toString(confidence)).append(", ");
    sql.append("'").append(method).append("', ");
    sql.append("'").append(contrib_org).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Gt_depth in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Gt_depth in the database
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
   * Generate a sql script to create a table of type Gt_depth in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Gt_depth in the database
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
    buf.append("depthid      number(9)            NOT NULL,\n");
    buf.append("evid         number(9)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("depth        float(24)            NOT NULL,\n");
    buf.append("gtlevel_km   float(53)            NOT NULL,\n");
    buf.append("confidence   float(53)            NOT NULL,\n");
    buf.append("method       varchar2(50)         NOT NULL,\n");
    buf.append("contrib_org  varchar2(15)         NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("ldauth       varchar2(15)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (depthid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (evid,method,contrib_org,auth)");
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
    return 164;
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
    return (other instanceof Gt_depth) && ((Gt_depth) other).depthid == depthid;
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
    return (other instanceof Gt_depth) && ((Gt_depth) other).evid == evid
        && ((Gt_depth) other).method.equals(method)
        && ((Gt_depth) other).contrib_org.equals(contrib_org)
        && ((Gt_depth) other).auth.equals(auth);
  }

  /**
   * GT depth identifier
   * 
   * @return depthid
   */
  public long getDepthid() {
    return depthid;
  }

  /**
   * GT depth identifier
   * 
   * @param depthid
   * @throws IllegalArgumentException if depthid >= 1000000000
   */
  public Gt_depth setDepthid(long depthid) {
    if (depthid >= 1000000000L)
      throw new IllegalArgumentException("depthid=" + depthid + " but cannot be >= 1000000000");
    this.depthid = depthid;
    setHash(null);
    return this;
  }

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   * 
   * @return evid
   */
  public long getEvid() {
    return evid;
  }

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   * 
   * @param evid
   * @throws IllegalArgumentException if evid >= 1000000000
   */
  public Gt_depth setEvid(long evid) {
    if (evid >= 1000000000L)
      throw new IllegalArgumentException("evid=" + evid + " but cannot be >= 1000000000");
    this.evid = evid;
    setHash(null);
    return this;
  }

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   * 
   * @return orid
   */
  public long getOrid() {
    return orid;
  }

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   * 
   * @param orid
   * @throws IllegalArgumentException if orid >= 1000000000
   */
  public Gt_depth setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Event depth
   * <p>
   * Units: km
   * 
   * @return depth
   */
  public double getDepth() {
    return depth;
  }

  /**
   * Event depth
   * <p>
   * Units: km
   * 
   * @param depth
   */
  public Gt_depth setDepth(double depth) {
    this.depth = depth;
    setHash(null);
    return this;
  }

  /**
   * The maximum difference in km between the GT value and the true value of an observation
   * <p>
   * Units: km
   * 
   * @return gtlevel_km
   */
  public double getGtlevel_km() {
    return gtlevel_km;
  }

  /**
   * The maximum difference in km between the GT value and the true value of an observation
   * <p>
   * Units: km
   * 
   * @param gtlevel_km
   */
  public Gt_depth setGtlevel_km(double gtlevel_km) {
    this.gtlevel_km = gtlevel_km;
    setHash(null);
    return this;
  }

  /**
   * Confidence in GT level
   * 
   * @return confidence
   */
  public double getConfidence() {
    return confidence;
  }

  /**
   * Confidence in GT level
   * 
   * @param confidence
   */
  public Gt_depth setConfidence(double confidence) {
    this.confidence = confidence;
    setHash(null);
    return this;
  }

  /**
   * Description of the method used to obtain the GT value.
   * 
   * @return method
   */
  public String getMethod() {
    return method;
  }

  /**
   * Description of the method used to obtain the GT value.
   * 
   * @param method
   * @throws IllegalArgumentException if method.length() >= 50
   */
  public Gt_depth setMethod(String method) {
    if (method.length() > 50)
      throw new IllegalArgumentException(
          String.format("method.length() cannot be > 50.  method=%s", method));
    this.method = method;
    setHash(null);
    return this;
  }

  /**
   * The organization code of the organization that produced this data.
   * 
   * @return contrib_org
   */
  public String getContrib_org() {
    return contrib_org;
  }

  /**
   * The organization code of the organization that produced this data.
   * 
   * @param contrib_org
   * @throws IllegalArgumentException if contrib_org.length() >= 15
   */
  public Gt_depth setContrib_org(String contrib_org) {
    if (contrib_org.length() > 15)
      throw new IllegalArgumentException(
          String.format("contrib_org.length() cannot be > 15.  contrib_org=%s", contrib_org));
    this.contrib_org = contrib_org;
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
  public Gt_depth setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
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
  public Gt_depth setLdauth(String ldauth) {
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
