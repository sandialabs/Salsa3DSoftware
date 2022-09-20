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
 * shcorrsurf
 */
public class Shcorrsurf extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Slowness correction surface identifier.
   */
  private long shcorrsurfid;

  static final public long SHCORRSURFID_NA = Long.MIN_VALUE;

  /**
   * Slowness model identifier
   */
  private long shmodid;

  static final public long SHMODID_NA = Long.MIN_VALUE;

  /**
   * Modsource identifier.
   */
  private long modsrcid;

  static final public long MODSRCID_NA = Long.MIN_VALUE;

  /**
   * The type of surface representation (tesselated or kriged).
   */
  private String surftype;

  static final public String SURFTYPE_NA = null;

  /**
   * Keystring
   */
  private String modelkey;

  static final public String MODELKEY_NA = null;

  /**
   * The residual tolerance (tess - krig on the fly) that a tesselated surface was createded to
   * meet.
   */
  private double resid_tol;

  static final public double RESID_TOL_NA = -999;

  /**
   * Author who loaded data
   */
  private String ldauth;

  static final public String LDAUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("shcorrsurfid", Columns.FieldType.LONG, "%d");
    columns.add("shmodid", Columns.FieldType.LONG, "%d");
    columns.add("modsrcid", Columns.FieldType.LONG, "%d");
    columns.add("surftype", Columns.FieldType.STRING, "%s");
    columns.add("modelkey", Columns.FieldType.STRING, "%s");
    columns.add("resid_tol", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("ldauth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Shcorrsurf(long shcorrsurfid, long shmodid, long modsrcid, String surftype,
      String modelkey, double resid_tol, String ldauth) {
    setValues(shcorrsurfid, shmodid, modsrcid, surftype, modelkey, resid_tol, ldauth);
  }

  private void setValues(long shcorrsurfid, long shmodid, long modsrcid, String surftype,
      String modelkey, double resid_tol, String ldauth) {
    this.shcorrsurfid = shcorrsurfid;
    this.shmodid = shmodid;
    this.modsrcid = modsrcid;
    this.surftype = surftype;
    this.modelkey = modelkey;
    this.resid_tol = resid_tol;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Shcorrsurf(Shcorrsurf other) {
    this.shcorrsurfid = other.getShcorrsurfid();
    this.shmodid = other.getShmodid();
    this.modsrcid = other.getModsrcid();
    this.surftype = other.getSurftype();
    this.modelkey = other.getModelkey();
    this.resid_tol = other.getResid_tol();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Shcorrsurf() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(SHCORRSURFID_NA, SHMODID_NA, MODSRCID_NA, SURFTYPE_NA, MODELKEY_NA, RESID_TOL_NA,
        LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "surftype":
        return surftype;
      case "modelkey":
        return modelkey;
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
      case "surftype":
        surftype = value;
        break;
      case "modelkey":
        modelkey = value;
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
      case "resid_tol":
        return resid_tol;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "resid_tol":
        resid_tol = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "shcorrsurfid":
        return shcorrsurfid;
      case "shmodid":
        return shmodid;
      case "modsrcid":
        return modsrcid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "shcorrsurfid":
        shcorrsurfid = value;
        break;
      case "shmodid":
        shmodid = value;
        break;
      case "modsrcid":
        modsrcid = value;
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
  public Shcorrsurf(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Shcorrsurf(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), readString(input), readString(input),
        input.readDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Shcorrsurf(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), readString(input), readString(input),
        input.getDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Shcorrsurf(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Shcorrsurf(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5), input.getDouble(offset + 6),
        input.getString(offset + 7));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[7];
    values[0] = shcorrsurfid;
    values[1] = shmodid;
    values[2] = modsrcid;
    values[3] = surftype;
    values[4] = modelkey;
    values[5] = resid_tol;
    values[6] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[8];
    values[0] = shcorrsurfid;
    values[1] = shmodid;
    values[2] = modsrcid;
    values[3] = surftype;
    values[4] = modelkey;
    values[5] = resid_tol;
    values[6] = ldauth;
    values[7] = lddate;
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
    output.writeLong(shcorrsurfid);
    output.writeLong(shmodid);
    output.writeLong(modsrcid);
    writeString(output, surftype);
    writeString(output, modelkey);
    output.writeDouble(resid_tol);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(shcorrsurfid);
    output.putLong(shmodid);
    output.putLong(modsrcid);
    writeString(output, surftype);
    writeString(output, modelkey);
    output.putDouble(resid_tol);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Shcorrsurf objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Shcorrsurf objects.
   * @throws IOException
   */
  static public void readShcorrsurfs(BufferedReader input, Collection<Shcorrsurf> rows)
      throws IOException {
    String[] saved = Shcorrsurf.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Shcorrsurf
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Shcorrsurf(new Scanner(line)));
    }
    input.close();
    Shcorrsurf.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Shcorrsurf objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Shcorrsurf objects.
   * @throws IOException
   */
  static public void readShcorrsurfs(File inputFile, Collection<Shcorrsurf> rows)
      throws IOException {
    readShcorrsurfs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Shcorrsurf objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Shcorrsurf objects.
   * @throws IOException
   */
  static public void readShcorrsurfs(InputStream inputStream, Collection<Shcorrsurf> rows)
      throws IOException {
    readShcorrsurfs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Shcorrsurf objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Shcorrsurf objects
   * @throws IOException
   */
  static public Set<Shcorrsurf> readShcorrsurfs(BufferedReader input) throws IOException {
    Set<Shcorrsurf> rows = new LinkedHashSet<Shcorrsurf>();
    readShcorrsurfs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Shcorrsurf objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Shcorrsurf objects
   * @throws IOException
   */
  static public Set<Shcorrsurf> readShcorrsurfs(File inputFile) throws IOException {
    return readShcorrsurfs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Shcorrsurf objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Shcorrsurf objects
   * @throws IOException
   */
  static public Set<Shcorrsurf> readShcorrsurfs(InputStream input) throws IOException {
    return readShcorrsurfs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Shcorrsurf objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param shcorrsurfs the Shcorrsurf objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Shcorrsurf> shcorrsurfs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Shcorrsurf shcorrsurf : shcorrsurfs)
      shcorrsurf.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Shcorrsurf objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param shcorrsurfs the Shcorrsurf objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Shcorrsurf> shcorrsurfs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?)");
      for (Shcorrsurf shcorrsurf : shcorrsurfs) {
        int i = 0;
        statement.setLong(++i, shcorrsurf.shcorrsurfid);
        statement.setLong(++i, shcorrsurf.shmodid);
        statement.setLong(++i, shcorrsurf.modsrcid);
        statement.setString(++i, shcorrsurf.surftype);
        statement.setString(++i, shcorrsurf.modelkey);
        statement.setDouble(++i, shcorrsurf.resid_tol);
        statement.setString(++i, shcorrsurf.ldauth);
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
   *        Shcorrsurf table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Shcorrsurf> readShcorrsurfs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Shcorrsurf> results = new HashSet<Shcorrsurf>();
    readShcorrsurfs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Shcorrsurf table.
   * @param shcorrsurfs
   * @throws SQLException
   */
  static public void readShcorrsurfs(Connection connection, String selectStatement,
      Set<Shcorrsurf> shcorrsurfs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        shcorrsurfs.add(new Shcorrsurf(rs));
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
   * this Shcorrsurf object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Shcorrsurf object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("shcorrsurfid, shmodid, modsrcid, surftype, modelkey, resid_tol, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(shcorrsurfid)).append(", ");
    sql.append(Long.toString(shmodid)).append(", ");
    sql.append(Long.toString(modsrcid)).append(", ");
    sql.append("'").append(surftype).append("', ");
    sql.append("'").append(modelkey).append("', ");
    sql.append(Double.toString(resid_tol)).append(", ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Shcorrsurf in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Shcorrsurf in the database
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
   * Generate a sql script to create a table of type Shcorrsurf in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Shcorrsurf in the database
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
    buf.append("shcorrsurfid number(9)            NOT NULL,\n");
    buf.append("shmodid      number(9)            NOT NULL,\n");
    buf.append("modsrcid     number(9)            NOT NULL,\n");
    buf.append("surftype     varchar2(30)         NOT NULL,\n");
    buf.append("modelkey     varchar2(32)         NOT NULL,\n");
    buf.append("resid_tol    float(53)            NOT NULL,\n");
    buf.append("ldauth       varchar2(15)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (shcorrsurfid)");
    if (includeUniqueKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_uk unique (modelkey)");
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
    return (other instanceof Shcorrsurf) && ((Shcorrsurf) other).shcorrsurfid == shcorrsurfid;
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
    return (other instanceof Shcorrsurf) && ((Shcorrsurf) other).modelkey.equals(modelkey);
  }

  /**
   * Slowness correction surface identifier.
   * 
   * @return shcorrsurfid
   */
  public long getShcorrsurfid() {
    return shcorrsurfid;
  }

  /**
   * Slowness correction surface identifier.
   * 
   * @param shcorrsurfid
   * @throws IllegalArgumentException if shcorrsurfid >= 1000000000
   */
  public Shcorrsurf setShcorrsurfid(long shcorrsurfid) {
    if (shcorrsurfid >= 1000000000L)
      throw new IllegalArgumentException(
          "shcorrsurfid=" + shcorrsurfid + " but cannot be >= 1000000000");
    this.shcorrsurfid = shcorrsurfid;
    setHash(null);
    return this;
  }

  /**
   * Slowness model identifier
   * 
   * @return shmodid
   */
  public long getShmodid() {
    return shmodid;
  }

  /**
   * Slowness model identifier
   * 
   * @param shmodid
   * @throws IllegalArgumentException if shmodid >= 1000000000
   */
  public Shcorrsurf setShmodid(long shmodid) {
    if (shmodid >= 1000000000L)
      throw new IllegalArgumentException("shmodid=" + shmodid + " but cannot be >= 1000000000");
    this.shmodid = shmodid;
    setHash(null);
    return this;
  }

  /**
   * Modsource identifier.
   * 
   * @return modsrcid
   */
  public long getModsrcid() {
    return modsrcid;
  }

  /**
   * Modsource identifier.
   * 
   * @param modsrcid
   * @throws IllegalArgumentException if modsrcid >= 1000000000
   */
  public Shcorrsurf setModsrcid(long modsrcid) {
    if (modsrcid >= 1000000000L)
      throw new IllegalArgumentException("modsrcid=" + modsrcid + " but cannot be >= 1000000000");
    this.modsrcid = modsrcid;
    setHash(null);
    return this;
  }

  /**
   * The type of surface representation (tesselated or kriged).
   * 
   * @return surftype
   */
  public String getSurftype() {
    return surftype;
  }

  /**
   * The type of surface representation (tesselated or kriged).
   * 
   * @param surftype
   * @throws IllegalArgumentException if surftype.length() >= 30
   */
  public Shcorrsurf setSurftype(String surftype) {
    if (surftype.length() > 30)
      throw new IllegalArgumentException(
          String.format("surftype.length() cannot be > 30.  surftype=%s", surftype));
    this.surftype = surftype;
    setHash(null);
    return this;
  }

  /**
   * Keystring
   * 
   * @return modelkey
   */
  public String getModelkey() {
    return modelkey;
  }

  /**
   * Keystring
   * 
   * @param modelkey
   * @throws IllegalArgumentException if modelkey.length() >= 32
   */
  public Shcorrsurf setModelkey(String modelkey) {
    if (modelkey.length() > 32)
      throw new IllegalArgumentException(
          String.format("modelkey.length() cannot be > 32.  modelkey=%s", modelkey));
    this.modelkey = modelkey;
    setHash(null);
    return this;
  }

  /**
   * The residual tolerance (tess - krig on the fly) that a tesselated surface was createded to
   * meet.
   * 
   * @return resid_tol
   */
  public double getResid_tol() {
    return resid_tol;
  }

  /**
   * The residual tolerance (tess - krig on the fly) that a tesselated surface was createded to
   * meet.
   * 
   * @param resid_tol
   */
  public Shcorrsurf setResid_tol(double resid_tol) {
    this.resid_tol = resid_tol;
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
  public Shcorrsurf setLdauth(String ldauth) {
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
