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
 * ttmod_assoc
 */
public class Ttmod_assoc extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Travel-time model identifier.
   */
  private long ttmodid;

  static final public long TTMODID_NA = Long.MIN_VALUE;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta</I>, <I>chan</I> and <I>time</I>.
   */
  private long arid;

  static final public long ARID_NA = Long.MIN_VALUE;

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
    columns.add("ttmodid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("arid", Columns.FieldType.LONG, "%d");
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
  public Ttmod_assoc(long ttmodid, long orid, long arid, long beamid, String cluster_tag,
      String comment_str, String ldauth) {
    setValues(ttmodid, orid, arid, beamid, cluster_tag, comment_str, ldauth);
  }

  private void setValues(long ttmodid, long orid, long arid, long beamid, String cluster_tag,
      String comment_str, String ldauth) {
    this.ttmodid = ttmodid;
    this.orid = orid;
    this.arid = arid;
    this.beamid = beamid;
    this.cluster_tag = cluster_tag;
    this.comment_str = comment_str;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Ttmod_assoc(Ttmod_assoc other) {
    this.ttmodid = other.getTtmodid();
    this.orid = other.getOrid();
    this.arid = other.getArid();
    this.beamid = other.getBeamid();
    this.cluster_tag = other.getCluster_tag();
    this.comment_str = other.getComment_str();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Ttmod_assoc() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(TTMODID_NA, ORID_NA, ARID_NA, BEAMID_NA, CLUSTER_TAG_NA, COMMENT_STR_NA, LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
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
      case "ttmodid":
        return ttmodid;
      case "orid":
        return orid;
      case "arid":
        return arid;
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
      case "ttmodid":
        ttmodid = value;
        break;
      case "orid":
        orid = value;
        break;
      case "arid":
        arid = value;
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
  public Ttmod_assoc(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Ttmod_assoc(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), readString(input),
        readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Ttmod_assoc(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), readString(input),
        readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ttmod_assoc(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Ttmod_assoc(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[7];
    values[0] = ttmodid;
    values[1] = orid;
    values[2] = arid;
    values[3] = beamid;
    values[4] = cluster_tag;
    values[5] = comment_str;
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
    values[0] = ttmodid;
    values[1] = orid;
    values[2] = arid;
    values[3] = beamid;
    values[4] = cluster_tag;
    values[5] = comment_str;
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
    output.writeLong(ttmodid);
    output.writeLong(orid);
    output.writeLong(arid);
    output.writeLong(beamid);
    writeString(output, cluster_tag);
    writeString(output, comment_str);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(ttmodid);
    output.putLong(orid);
    output.putLong(arid);
    output.putLong(beamid);
    writeString(output, cluster_tag);
    writeString(output, comment_str);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Ttmod_assoc objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Ttmod_assoc objects.
   * @throws IOException
   */
  static public void readTtmod_assocs(BufferedReader input, Collection<Ttmod_assoc> rows)
      throws IOException {
    String[] saved = Ttmod_assoc.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Ttmod_assoc
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Ttmod_assoc(new Scanner(line)));
    }
    input.close();
    Ttmod_assoc.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Ttmod_assoc objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Ttmod_assoc objects.
   * @throws IOException
   */
  static public void readTtmod_assocs(File inputFile, Collection<Ttmod_assoc> rows)
      throws IOException {
    readTtmod_assocs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Ttmod_assoc objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Ttmod_assoc objects.
   * @throws IOException
   */
  static public void readTtmod_assocs(InputStream inputStream, Collection<Ttmod_assoc> rows)
      throws IOException {
    readTtmod_assocs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Ttmod_assoc objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Ttmod_assoc objects
   * @throws IOException
   */
  static public Set<Ttmod_assoc> readTtmod_assocs(BufferedReader input) throws IOException {
    Set<Ttmod_assoc> rows = new LinkedHashSet<Ttmod_assoc>();
    readTtmod_assocs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Ttmod_assoc objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Ttmod_assoc objects
   * @throws IOException
   */
  static public Set<Ttmod_assoc> readTtmod_assocs(File inputFile) throws IOException {
    return readTtmod_assocs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Ttmod_assoc objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Ttmod_assoc objects
   * @throws IOException
   */
  static public Set<Ttmod_assoc> readTtmod_assocs(InputStream input) throws IOException {
    return readTtmod_assocs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Ttmod_assoc objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param ttmod_assocs the Ttmod_assoc objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Ttmod_assoc> ttmod_assocs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Ttmod_assoc ttmod_assoc : ttmod_assocs)
      ttmod_assoc.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Ttmod_assoc objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param ttmod_assocs the Ttmod_assoc objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Ttmod_assoc> ttmod_assocs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?)");
      for (Ttmod_assoc ttmod_assoc : ttmod_assocs) {
        int i = 0;
        statement.setLong(++i, ttmod_assoc.ttmodid);
        statement.setLong(++i, ttmod_assoc.orid);
        statement.setLong(++i, ttmod_assoc.arid);
        statement.setLong(++i, ttmod_assoc.beamid);
        statement.setString(++i, ttmod_assoc.cluster_tag);
        statement.setString(++i, ttmod_assoc.comment_str);
        statement.setString(++i, ttmod_assoc.ldauth);
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
   *        Ttmod_assoc table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Ttmod_assoc> readTtmod_assocs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Ttmod_assoc> results = new HashSet<Ttmod_assoc>();
    readTtmod_assocs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Ttmod_assoc table.
   * @param ttmod_assocs
   * @throws SQLException
   */
  static public void readTtmod_assocs(Connection connection, String selectStatement,
      Set<Ttmod_assoc> ttmod_assocs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        ttmod_assocs.add(new Ttmod_assoc(rs));
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
   * this Ttmod_assoc object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Ttmod_assoc object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("ttmodid, orid, arid, beamid, cluster_tag, comment_str, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(ttmodid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(arid)).append(", ");
    sql.append(Long.toString(beamid)).append(", ");
    sql.append("'").append(cluster_tag).append("', ");
    sql.append("'").append(comment_str).append("', ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Ttmod_assoc in the database. Primary and unique keys are set, if
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
   * Create a table of type Ttmod_assoc in the database
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
   * Generate a sql script to create a table of type Ttmod_assoc in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Ttmod_assoc in the database
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
    buf.append("ttmodid      number(9)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("arid         number(9)            NOT NULL,\n");
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
          + "_pk primary key (ttmodid,orid,arid)");
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
    return 2084;
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
    return (other instanceof Ttmod_assoc) && ((Ttmod_assoc) other).ttmodid == ttmodid
        && ((Ttmod_assoc) other).orid == orid && ((Ttmod_assoc) other).arid == arid;
  }

  /**
   * Travel-time model identifier.
   * 
   * @return ttmodid
   */
  public long getTtmodid() {
    return ttmodid;
  }

  /**
   * Travel-time model identifier.
   * 
   * @param ttmodid
   * @throws IllegalArgumentException if ttmodid >= 1000000000
   */
  public Ttmod_assoc setTtmodid(long ttmodid) {
    if (ttmodid >= 1000000000L)
      throw new IllegalArgumentException("ttmodid=" + ttmodid + " but cannot be >= 1000000000");
    this.ttmodid = ttmodid;
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
  public Ttmod_assoc setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta</I>, <I>chan</I> and <I>time</I>.
   * 
   * @return arid
   */
  public long getArid() {
    return arid;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta</I>, <I>chan</I> and <I>time</I>.
   * 
   * @param arid
   * @throws IllegalArgumentException if arid >= 1000000000
   */
  public Ttmod_assoc setArid(long arid) {
    if (arid >= 1000000000L)
      throw new IllegalArgumentException("arid=" + arid + " but cannot be >= 1000000000");
    this.arid = arid;
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
  public Ttmod_assoc setBeamid(long beamid) {
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
  public Ttmod_assoc setCluster_tag(String cluster_tag) {
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
  public Ttmod_assoc setComment_str(String comment_str) {
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
  public Ttmod_assoc setLdauth(String ldauth) {
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
