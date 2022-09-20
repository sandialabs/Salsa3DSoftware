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
 * polygon
 */
public class Polygon extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   */
  private long polyid;

  static final public long POLYID_NA = Long.MIN_VALUE;

  /**
   * Name of region associated with polygon.
   */
  private String polyname;

  static final public String POLYNAME_NA = null;

  /**
   * Interpolation method between polygon vertices, recommended <I>perimitertype</I>s are: xy
   * Cartesian gc great circle
   */
  private String perimetertype;

  static final public String PERIMETERTYPE_NA = null;

  /**
   * Pass points inside polygon: y or n.
   */
  private String insideflag;

  static final public String INSIDEFLAG_NA = null;

  /**
   * Pass points outside polygon: y or n.
   */
  private String outsideflag;

  static final public String OUTSIDEFLAG_NA = null;

  /**
   * Include points falling on edge or vertex of a polygon, y or n.
   */
  private String edgeflag;

  static final public String EDGEFLAG_NA = null;

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
    columns.add("polyid", Columns.FieldType.LONG, "%d");
    columns.add("polyname", Columns.FieldType.STRING, "%s");
    columns.add("perimetertype", Columns.FieldType.STRING, "%s");
    columns.add("insideflag", Columns.FieldType.STRING, "%s");
    columns.add("outsideflag", Columns.FieldType.STRING, "%s");
    columns.add("edgeflag", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Polygon(long polyid, String polyname, String perimetertype, String insideflag,
      String outsideflag, String edgeflag, String auth, long commid) {
    setValues(polyid, polyname, perimetertype, insideflag, outsideflag, edgeflag, auth, commid);
  }

  private void setValues(long polyid, String polyname, String perimetertype, String insideflag,
      String outsideflag, String edgeflag, String auth, long commid) {
    this.polyid = polyid;
    this.polyname = polyname;
    this.perimetertype = perimetertype;
    this.insideflag = insideflag;
    this.outsideflag = outsideflag;
    this.edgeflag = edgeflag;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Polygon(Polygon other) {
    this.polyid = other.getPolyid();
    this.polyname = other.getPolyname();
    this.perimetertype = other.getPerimetertype();
    this.insideflag = other.getInsideflag();
    this.outsideflag = other.getOutsideflag();
    this.edgeflag = other.getEdgeflag();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Polygon() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(POLYID_NA, POLYNAME_NA, PERIMETERTYPE_NA, INSIDEFLAG_NA, OUTSIDEFLAG_NA, EDGEFLAG_NA,
        AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "polyname":
        return polyname;
      case "perimetertype":
        return perimetertype;
      case "insideflag":
        return insideflag;
      case "outsideflag":
        return outsideflag;
      case "edgeflag":
        return edgeflag;
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
      case "polyname":
        polyname = value;
        break;
      case "perimetertype":
        perimetertype = value;
        break;
      case "insideflag":
        insideflag = value;
        break;
      case "outsideflag":
        outsideflag = value;
        break;
      case "edgeflag":
        edgeflag = value;
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
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
        + " is not a valid input name ...");
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "polyid":
        return polyid;
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
      case "polyid":
        polyid = value;
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
  public Polygon(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Polygon(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Polygon(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Polygon(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Polygon(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7), input.getLong(offset + 8));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[8];
    values[0] = polyid;
    values[1] = polyname;
    values[2] = perimetertype;
    values[3] = insideflag;
    values[4] = outsideflag;
    values[5] = edgeflag;
    values[6] = auth;
    values[7] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[9];
    values[0] = polyid;
    values[1] = polyname;
    values[2] = perimetertype;
    values[3] = insideflag;
    values[4] = outsideflag;
    values[5] = edgeflag;
    values[6] = auth;
    values[7] = commid;
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
    output.writeLong(polyid);
    writeString(output, polyname);
    writeString(output, perimetertype);
    writeString(output, insideflag);
    writeString(output, outsideflag);
    writeString(output, edgeflag);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(polyid);
    writeString(output, polyname);
    writeString(output, perimetertype);
    writeString(output, insideflag);
    writeString(output, outsideflag);
    writeString(output, edgeflag);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Polygon objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Polygon objects.
   * @throws IOException
   */
  static public void readPolygons(BufferedReader input, Collection<Polygon> rows)
      throws IOException {
    String[] saved = Polygon.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Polygon.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Polygon(new Scanner(line)));
    }
    input.close();
    Polygon.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Polygon objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Polygon objects.
   * @throws IOException
   */
  static public void readPolygons(File inputFile, Collection<Polygon> rows) throws IOException {
    readPolygons(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Polygon objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Polygon objects.
   * @throws IOException
   */
  static public void readPolygons(InputStream inputStream, Collection<Polygon> rows)
      throws IOException {
    readPolygons(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Polygon objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Polygon objects
   * @throws IOException
   */
  static public Set<Polygon> readPolygons(BufferedReader input) throws IOException {
    Set<Polygon> rows = new LinkedHashSet<Polygon>();
    readPolygons(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Polygon objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Polygon objects
   * @throws IOException
   */
  static public Set<Polygon> readPolygons(File inputFile) throws IOException {
    return readPolygons(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Polygon objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Polygon objects
   * @throws IOException
   */
  static public Set<Polygon> readPolygons(InputStream input) throws IOException {
    return readPolygons(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Polygon objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param polygons the Polygon objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Polygon> polygons)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Polygon polygon : polygons)
      polygon.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Polygon objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param polygons the Polygon objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Polygon> polygons, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?)");
      for (Polygon polygon : polygons) {
        int i = 0;
        statement.setLong(++i, polygon.polyid);
        statement.setString(++i, polygon.polyname);
        statement.setString(++i, polygon.perimetertype);
        statement.setString(++i, polygon.insideflag);
        statement.setString(++i, polygon.outsideflag);
        statement.setString(++i, polygon.edgeflag);
        statement.setString(++i, polygon.auth);
        statement.setLong(++i, polygon.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Polygon
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Polygon> readPolygons(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Polygon> results = new HashSet<Polygon>();
    readPolygons(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Polygon
   *        table.
   * @param polygons
   * @throws SQLException
   */
  static public void readPolygons(Connection connection, String selectStatement,
      Set<Polygon> polygons) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        polygons.add(new Polygon(rs));
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
   * this Polygon object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Polygon object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "polyid, polyname, perimetertype, insideflag, outsideflag, edgeflag, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(polyid)).append(", ");
    sql.append("'").append(polyname).append("', ");
    sql.append("'").append(perimetertype).append("', ");
    sql.append("'").append(insideflag).append("', ");
    sql.append("'").append(outsideflag).append("', ");
    sql.append("'").append(edgeflag).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Polygon in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Polygon in the database
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
   * Generate a sql script to create a table of type Polygon in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Polygon in the database
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
    buf.append("polyid       number(9)            NOT NULL,\n");
    buf.append("polyname     varchar2(50)         NOT NULL,\n");
    buf.append("perimetertype varchar2(8)          NOT NULL,\n");
    buf.append("insideflag   varchar2(1)          NOT NULL,\n");
    buf.append("outsideflag  varchar2(1)          NOT NULL,\n");
    buf.append("edgeflag     varchar2(1)          NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (polyid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (polyname,auth)");
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
    return 121;
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
    return (other instanceof Polygon) && ((Polygon) other).polyid == polyid;
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
    return (other instanceof Polygon) && ((Polygon) other).polyname.equals(polyname)
        && ((Polygon) other).auth.equals(auth);
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
  public Polygon setPolyid(long polyid) {
    if (polyid >= 1000000000L)
      throw new IllegalArgumentException("polyid=" + polyid + " but cannot be >= 1000000000");
    this.polyid = polyid;
    setHash(null);
    return this;
  }

  /**
   * Name of region associated with polygon.
   * 
   * @return polyname
   */
  public String getPolyname() {
    return polyname;
  }

  /**
   * Name of region associated with polygon.
   * 
   * @param polyname
   * @throws IllegalArgumentException if polyname.length() >= 50
   */
  public Polygon setPolyname(String polyname) {
    if (polyname.length() > 50)
      throw new IllegalArgumentException(
          String.format("polyname.length() cannot be > 50.  polyname=%s", polyname));
    this.polyname = polyname;
    setHash(null);
    return this;
  }

  /**
   * Interpolation method between polygon vertices, recommended <I>perimitertype</I>s are: xy
   * Cartesian gc great circle
   * 
   * @return perimetertype
   */
  public String getPerimetertype() {
    return perimetertype;
  }

  /**
   * Interpolation method between polygon vertices, recommended <I>perimitertype</I>s are: xy
   * Cartesian gc great circle
   * 
   * @param perimetertype
   * @throws IllegalArgumentException if perimetertype.length() >= 8
   */
  public Polygon setPerimetertype(String perimetertype) {
    if (perimetertype.length() > 8)
      throw new IllegalArgumentException(
          String.format("perimetertype.length() cannot be > 8.  perimetertype=%s", perimetertype));
    this.perimetertype = perimetertype;
    setHash(null);
    return this;
  }

  /**
   * Pass points inside polygon: y or n.
   * 
   * @return insideflag
   */
  public String getInsideflag() {
    return insideflag;
  }

  /**
   * Pass points inside polygon: y or n.
   * 
   * @param insideflag
   * @throws IllegalArgumentException if insideflag.length() >= 1
   */
  public Polygon setInsideflag(String insideflag) {
    if (insideflag.length() > 1)
      throw new IllegalArgumentException(
          String.format("insideflag.length() cannot be > 1.  insideflag=%s", insideflag));
    this.insideflag = insideflag;
    setHash(null);
    return this;
  }

  /**
   * Pass points outside polygon: y or n.
   * 
   * @return outsideflag
   */
  public String getOutsideflag() {
    return outsideflag;
  }

  /**
   * Pass points outside polygon: y or n.
   * 
   * @param outsideflag
   * @throws IllegalArgumentException if outsideflag.length() >= 1
   */
  public Polygon setOutsideflag(String outsideflag) {
    if (outsideflag.length() > 1)
      throw new IllegalArgumentException(
          String.format("outsideflag.length() cannot be > 1.  outsideflag=%s", outsideflag));
    this.outsideflag = outsideflag;
    setHash(null);
    return this;
  }

  /**
   * Include points falling on edge or vertex of a polygon, y or n.
   * 
   * @return edgeflag
   */
  public String getEdgeflag() {
    return edgeflag;
  }

  /**
   * Include points falling on edge or vertex of a polygon, y or n.
   * 
   * @param edgeflag
   * @throws IllegalArgumentException if edgeflag.length() >= 1
   */
  public Polygon setEdgeflag(String edgeflag) {
    if (edgeflag.length() > 1)
      throw new IllegalArgumentException(
          String.format("edgeflag.length() cannot be > 1.  edgeflag=%s", edgeflag));
    this.edgeflag = edgeflag;
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
  public Polygon setAuth(String auth) {
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
  public Polygon setCommid(long commid) {
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
