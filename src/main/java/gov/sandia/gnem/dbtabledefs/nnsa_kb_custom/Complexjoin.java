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
 * complexjoin
 */
public class Complexjoin extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * SQL join description identifier
   */
  private long joinid;

  static final public long JOINID_NA = Long.MIN_VALUE;

  /**
   * Table name, lowercase version of that used in Oracle's data dictionary.
   */
  private String table_name;

  static final public String TABLE_NAME_NA = null;

  /**
   * Column name, lowercase version of that used in Oracle's data dictionary.
   */
  private String column_name;

  static final public String COLUMN_NAME_NA = null;

  /**
   * Schema name.
   */
  private String schema_name;

  static final public String SCHEMA_NAME_NA = "-";

  /**
   * Table name for second instance; lowercase version of that used in Oracle's data dictionary.
   */
  private String table_name_2;

  static final public String TABLE_NAME_2_NA = "-";

  /**
   * schema name for second instance
   */
  private String schema_name_2;

  static final public String SCHEMA_NAME_2_NA = "-";

  /**
   * Table name for thrid instance; lowercase version of that used in Oracle's data dictionary.
   */
  private String table_name_3;

  static final public String TABLE_NAME_3_NA = "-";

  /**
   * schema name for thrid instance
   */
  private String schema_name_3;

  static final public String SCHEMA_NAME_3_NA = "-";

  /**
   * Join operator. Must be a standard SQL operator or the NA value.
   */
  private String joinop;

  static final public String JOINOP_NA = "-";

  /**
   * An SQL where clause, or portion thereof, as appropriate.
   */
  private String clause;

  static final public String CLAUSE_NA = null;

  /**
   * Determines if the information in the given row is to be used as active or ignored as inactive.
   */
  private String active;

  static final public String ACTIVE_NA = null;

  /**
   * Determines the level of enforcement: h (hard) or s (soft), where hard means that the relation
   * must be obeyed, and soft means that violations are possible but should be explained.
   */
  private String enforce;

  static final public String ENFORCE_NA = null;

  /**
   * Definition
   */
  private String definition;

  static final public String DEFINITION_NA = "-";

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
    columns.add("joinid", Columns.FieldType.LONG, "%d");
    columns.add("table_name", Columns.FieldType.STRING, "%s");
    columns.add("column_name", Columns.FieldType.STRING, "%s");
    columns.add("schema_name", Columns.FieldType.STRING, "%s");
    columns.add("table_name_2", Columns.FieldType.STRING, "%s");
    columns.add("schema_name_2", Columns.FieldType.STRING, "%s");
    columns.add("table_name_3", Columns.FieldType.STRING, "%s");
    columns.add("schema_name_3", Columns.FieldType.STRING, "%s");
    columns.add("joinop", Columns.FieldType.STRING, "%s");
    columns.add("clause", Columns.FieldType.STRING, "%s");
    columns.add("active", Columns.FieldType.STRING, "%s");
    columns.add("enforce", Columns.FieldType.STRING, "%s");
    columns.add("definition", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Complexjoin(long joinid, String table_name, String column_name, String schema_name,
      String table_name_2, String schema_name_2, String table_name_3, String schema_name_3,
      String joinop, String clause, String active, String enforce, String definition, String auth,
      long commid) {
    setValues(joinid, table_name, column_name, schema_name, table_name_2, schema_name_2,
        table_name_3, schema_name_3, joinop, clause, active, enforce, definition, auth, commid);
  }

  private void setValues(long joinid, String table_name, String column_name, String schema_name,
      String table_name_2, String schema_name_2, String table_name_3, String schema_name_3,
      String joinop, String clause, String active, String enforce, String definition, String auth,
      long commid) {
    this.joinid = joinid;
    this.table_name = table_name;
    this.column_name = column_name;
    this.schema_name = schema_name;
    this.table_name_2 = table_name_2;
    this.schema_name_2 = schema_name_2;
    this.table_name_3 = table_name_3;
    this.schema_name_3 = schema_name_3;
    this.joinop = joinop;
    this.clause = clause;
    this.active = active;
    this.enforce = enforce;
    this.definition = definition;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Complexjoin(Complexjoin other) {
    this.joinid = other.getJoinid();
    this.table_name = other.getTable_name();
    this.column_name = other.getColumn_name();
    this.schema_name = other.getSchema_name();
    this.table_name_2 = other.getTable_name_2();
    this.schema_name_2 = other.getSchema_name_2();
    this.table_name_3 = other.getTable_name_3();
    this.schema_name_3 = other.getSchema_name_3();
    this.joinop = other.getJoinop();
    this.clause = other.getClause();
    this.active = other.getActive();
    this.enforce = other.getEnforce();
    this.definition = other.getDefinition();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Complexjoin() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(JOINID_NA, TABLE_NAME_NA, COLUMN_NAME_NA, SCHEMA_NAME_NA, TABLE_NAME_2_NA,
        SCHEMA_NAME_2_NA, TABLE_NAME_3_NA, SCHEMA_NAME_3_NA, JOINOP_NA, CLAUSE_NA, ACTIVE_NA,
        ENFORCE_NA, DEFINITION_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "table_name":
        return table_name;
      case "column_name":
        return column_name;
      case "schema_name":
        return schema_name;
      case "table_name_2":
        return table_name_2;
      case "schema_name_2":
        return schema_name_2;
      case "table_name_3":
        return table_name_3;
      case "schema_name_3":
        return schema_name_3;
      case "joinop":
        return joinop;
      case "clause":
        return clause;
      case "active":
        return active;
      case "enforce":
        return enforce;
      case "definition":
        return definition;
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
      case "table_name":
        table_name = value;
        break;
      case "column_name":
        column_name = value;
        break;
      case "schema_name":
        schema_name = value;
        break;
      case "table_name_2":
        table_name_2 = value;
        break;
      case "schema_name_2":
        schema_name_2 = value;
        break;
      case "table_name_3":
        table_name_3 = value;
        break;
      case "schema_name_3":
        schema_name_3 = value;
        break;
      case "joinop":
        joinop = value;
        break;
      case "clause":
        clause = value;
        break;
      case "active":
        active = value;
        break;
      case "enforce":
        enforce = value;
        break;
      case "definition":
        definition = value;
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
      case "joinid":
        return joinid;
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
      case "joinid":
        joinid = value;
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
  public Complexjoin(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Complexjoin(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Complexjoin(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Complexjoin(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Complexjoin(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getString(offset + 9),
        input.getString(offset + 10), input.getString(offset + 11), input.getString(offset + 12),
        input.getString(offset + 13), input.getString(offset + 14), input.getLong(offset + 15));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[15];
    values[0] = joinid;
    values[1] = table_name;
    values[2] = column_name;
    values[3] = schema_name;
    values[4] = table_name_2;
    values[5] = schema_name_2;
    values[6] = table_name_3;
    values[7] = schema_name_3;
    values[8] = joinop;
    values[9] = clause;
    values[10] = active;
    values[11] = enforce;
    values[12] = definition;
    values[13] = auth;
    values[14] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[16];
    values[0] = joinid;
    values[1] = table_name;
    values[2] = column_name;
    values[3] = schema_name;
    values[4] = table_name_2;
    values[5] = schema_name_2;
    values[6] = table_name_3;
    values[7] = schema_name_3;
    values[8] = joinop;
    values[9] = clause;
    values[10] = active;
    values[11] = enforce;
    values[12] = definition;
    values[13] = auth;
    values[14] = commid;
    values[15] = lddate;
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
    output.writeLong(joinid);
    writeString(output, table_name);
    writeString(output, column_name);
    writeString(output, schema_name);
    writeString(output, table_name_2);
    writeString(output, schema_name_2);
    writeString(output, table_name_3);
    writeString(output, schema_name_3);
    writeString(output, joinop);
    writeString(output, clause);
    writeString(output, active);
    writeString(output, enforce);
    writeString(output, definition);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(joinid);
    writeString(output, table_name);
    writeString(output, column_name);
    writeString(output, schema_name);
    writeString(output, table_name_2);
    writeString(output, schema_name_2);
    writeString(output, table_name_3);
    writeString(output, schema_name_3);
    writeString(output, joinop);
    writeString(output, clause);
    writeString(output, active);
    writeString(output, enforce);
    writeString(output, definition);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Complexjoin objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Complexjoin objects.
   * @throws IOException
   */
  static public void readComplexjoins(BufferedReader input, Collection<Complexjoin> rows)
      throws IOException {
    String[] saved = Complexjoin.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Complexjoin
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Complexjoin(new Scanner(line)));
    }
    input.close();
    Complexjoin.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Complexjoin objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Complexjoin objects.
   * @throws IOException
   */
  static public void readComplexjoins(File inputFile, Collection<Complexjoin> rows)
      throws IOException {
    readComplexjoins(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Complexjoin objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Complexjoin objects.
   * @throws IOException
   */
  static public void readComplexjoins(InputStream inputStream, Collection<Complexjoin> rows)
      throws IOException {
    readComplexjoins(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Complexjoin objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Complexjoin objects
   * @throws IOException
   */
  static public Set<Complexjoin> readComplexjoins(BufferedReader input) throws IOException {
    Set<Complexjoin> rows = new LinkedHashSet<Complexjoin>();
    readComplexjoins(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Complexjoin objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Complexjoin objects
   * @throws IOException
   */
  static public Set<Complexjoin> readComplexjoins(File inputFile) throws IOException {
    return readComplexjoins(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Complexjoin objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Complexjoin objects
   * @throws IOException
   */
  static public Set<Complexjoin> readComplexjoins(InputStream input) throws IOException {
    return readComplexjoins(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Complexjoin objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param complexjoins the Complexjoin objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Complexjoin> complexjoins)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Complexjoin complexjoin : complexjoins)
      complexjoin.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Complexjoin objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param complexjoins the Complexjoin objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Complexjoin> complexjoins, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Complexjoin complexjoin : complexjoins) {
        int i = 0;
        statement.setLong(++i, complexjoin.joinid);
        statement.setString(++i, complexjoin.table_name);
        statement.setString(++i, complexjoin.column_name);
        statement.setString(++i, complexjoin.schema_name);
        statement.setString(++i, complexjoin.table_name_2);
        statement.setString(++i, complexjoin.schema_name_2);
        statement.setString(++i, complexjoin.table_name_3);
        statement.setString(++i, complexjoin.schema_name_3);
        statement.setString(++i, complexjoin.joinop);
        statement.setString(++i, complexjoin.clause);
        statement.setString(++i, complexjoin.active);
        statement.setString(++i, complexjoin.enforce);
        statement.setString(++i, complexjoin.definition);
        statement.setString(++i, complexjoin.auth);
        statement.setLong(++i, complexjoin.commid);
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
   *        Complexjoin table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Complexjoin> readComplexjoins(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Complexjoin> results = new HashSet<Complexjoin>();
    readComplexjoins(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Complexjoin table.
   * @param complexjoins
   * @throws SQLException
   */
  static public void readComplexjoins(Connection connection, String selectStatement,
      Set<Complexjoin> complexjoins) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        complexjoins.add(new Complexjoin(rs));
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
   * this Complexjoin object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Complexjoin object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "joinid, table_name, column_name, schema_name, table_name_2, schema_name_2, table_name_3, schema_name_3, joinop, clause, active, enforce, definition, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(joinid)).append(", ");
    sql.append("'").append(table_name).append("', ");
    sql.append("'").append(column_name).append("', ");
    sql.append("'").append(schema_name).append("', ");
    sql.append("'").append(table_name_2).append("', ");
    sql.append("'").append(schema_name_2).append("', ");
    sql.append("'").append(table_name_3).append("', ");
    sql.append("'").append(schema_name_3).append("', ");
    sql.append("'").append(joinop).append("', ");
    sql.append("'").append(clause).append("', ");
    sql.append("'").append(active).append("', ");
    sql.append("'").append(enforce).append("', ");
    sql.append("'").append(definition).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Complexjoin in the database. Primary and unique keys are set, if
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
   * Create a table of type Complexjoin in the database
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
   * Generate a sql script to create a table of type Complexjoin in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Complexjoin in the database
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
    buf.append("joinid       number(9)            NOT NULL,\n");
    buf.append("table_name   varchar2(30)         NOT NULL,\n");
    buf.append("column_name  varchar2(30)         NOT NULL,\n");
    buf.append("schema_name  varchar2(30)         NOT NULL,\n");
    buf.append("table_name_2 varchar2(30)         NOT NULL,\n");
    buf.append("schema_name_2 varchar2(30)         NOT NULL,\n");
    buf.append("table_name_3 varchar2(30)         NOT NULL,\n");
    buf.append("schema_name_3 varchar2(30)         NOT NULL,\n");
    buf.append("joinop       varchar2(30)         NOT NULL,\n");
    buf.append("clause       varchar2(1024)       NOT NULL,\n");
    buf.append("active       varchar2(1)          NOT NULL,\n");
    buf.append("enforce      varchar2(1)          NOT NULL,\n");
    buf.append("definition   varchar2(80)         NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (joinid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (table_name,column_name,schema_name,table_name_2,schema_name_2,table_name_3,schema_name_3,joinop,clause)");
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
    return 1434;
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
    return (other instanceof Complexjoin) && ((Complexjoin) other).joinid == joinid;
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
    return (other instanceof Complexjoin) && ((Complexjoin) other).table_name.equals(table_name)
        && ((Complexjoin) other).column_name.equals(column_name)
        && ((Complexjoin) other).schema_name.equals(schema_name)
        && ((Complexjoin) other).table_name_2.equals(table_name_2)
        && ((Complexjoin) other).schema_name_2.equals(schema_name_2)
        && ((Complexjoin) other).table_name_3.equals(table_name_3)
        && ((Complexjoin) other).schema_name_3.equals(schema_name_3)
        && ((Complexjoin) other).joinop.equals(joinop)
        && ((Complexjoin) other).clause.equals(clause);
  }

  /**
   * SQL join description identifier
   * 
   * @return joinid
   */
  public long getJoinid() {
    return joinid;
  }

  /**
   * SQL join description identifier
   * 
   * @param joinid
   * @throws IllegalArgumentException if joinid >= 1000000000
   */
  public Complexjoin setJoinid(long joinid) {
    if (joinid >= 1000000000L)
      throw new IllegalArgumentException("joinid=" + joinid + " but cannot be >= 1000000000");
    this.joinid = joinid;
    setHash(null);
    return this;
  }

  /**
   * Table name, lowercase version of that used in Oracle's data dictionary.
   * 
   * @return table_name
   */
  public String getTable_name() {
    return table_name;
  }

  /**
   * Table name, lowercase version of that used in Oracle's data dictionary.
   * 
   * @param table_name
   * @throws IllegalArgumentException if table_name.length() >= 30
   */
  public Complexjoin setTable_name(String table_name) {
    if (table_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("table_name.length() cannot be > 30.  table_name=%s", table_name));
    this.table_name = table_name;
    setHash(null);
    return this;
  }

  /**
   * Column name, lowercase version of that used in Oracle's data dictionary.
   * 
   * @return column_name
   */
  public String getColumn_name() {
    return column_name;
  }

  /**
   * Column name, lowercase version of that used in Oracle's data dictionary.
   * 
   * @param column_name
   * @throws IllegalArgumentException if column_name.length() >= 30
   */
  public Complexjoin setColumn_name(String column_name) {
    if (column_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("column_name.length() cannot be > 30.  column_name=%s", column_name));
    this.column_name = column_name;
    setHash(null);
    return this;
  }

  /**
   * Schema name.
   * 
   * @return schema_name
   */
  public String getSchema_name() {
    return schema_name;
  }

  /**
   * Schema name.
   * 
   * @param schema_name
   * @throws IllegalArgumentException if schema_name.length() >= 30
   */
  public Complexjoin setSchema_name(String schema_name) {
    if (schema_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("schema_name.length() cannot be > 30.  schema_name=%s", schema_name));
    this.schema_name = schema_name;
    setHash(null);
    return this;
  }

  /**
   * Table name for second instance; lowercase version of that used in Oracle's data dictionary.
   * 
   * @return table_name_2
   */
  public String getTable_name_2() {
    return table_name_2;
  }

  /**
   * Table name for second instance; lowercase version of that used in Oracle's data dictionary.
   * 
   * @param table_name_2
   * @throws IllegalArgumentException if table_name_2.length() >= 30
   */
  public Complexjoin setTable_name_2(String table_name_2) {
    if (table_name_2.length() > 30)
      throw new IllegalArgumentException(
          String.format("table_name_2.length() cannot be > 30.  table_name_2=%s", table_name_2));
    this.table_name_2 = table_name_2;
    setHash(null);
    return this;
  }

  /**
   * schema name for second instance
   * 
   * @return schema_name_2
   */
  public String getSchema_name_2() {
    return schema_name_2;
  }

  /**
   * schema name for second instance
   * 
   * @param schema_name_2
   * @throws IllegalArgumentException if schema_name_2.length() >= 30
   */
  public Complexjoin setSchema_name_2(String schema_name_2) {
    if (schema_name_2.length() > 30)
      throw new IllegalArgumentException(
          String.format("schema_name_2.length() cannot be > 30.  schema_name_2=%s", schema_name_2));
    this.schema_name_2 = schema_name_2;
    setHash(null);
    return this;
  }

  /**
   * Table name for thrid instance; lowercase version of that used in Oracle's data dictionary.
   * 
   * @return table_name_3
   */
  public String getTable_name_3() {
    return table_name_3;
  }

  /**
   * Table name for thrid instance; lowercase version of that used in Oracle's data dictionary.
   * 
   * @param table_name_3
   * @throws IllegalArgumentException if table_name_3.length() >= 30
   */
  public Complexjoin setTable_name_3(String table_name_3) {
    if (table_name_3.length() > 30)
      throw new IllegalArgumentException(
          String.format("table_name_3.length() cannot be > 30.  table_name_3=%s", table_name_3));
    this.table_name_3 = table_name_3;
    setHash(null);
    return this;
  }

  /**
   * schema name for thrid instance
   * 
   * @return schema_name_3
   */
  public String getSchema_name_3() {
    return schema_name_3;
  }

  /**
   * schema name for thrid instance
   * 
   * @param schema_name_3
   * @throws IllegalArgumentException if schema_name_3.length() >= 30
   */
  public Complexjoin setSchema_name_3(String schema_name_3) {
    if (schema_name_3.length() > 30)
      throw new IllegalArgumentException(
          String.format("schema_name_3.length() cannot be > 30.  schema_name_3=%s", schema_name_3));
    this.schema_name_3 = schema_name_3;
    setHash(null);
    return this;
  }

  /**
   * Join operator. Must be a standard SQL operator or the NA value.
   * 
   * @return joinop
   */
  public String getJoinop() {
    return joinop;
  }

  /**
   * Join operator. Must be a standard SQL operator or the NA value.
   * 
   * @param joinop
   * @throws IllegalArgumentException if joinop.length() >= 30
   */
  public Complexjoin setJoinop(String joinop) {
    if (joinop.length() > 30)
      throw new IllegalArgumentException(
          String.format("joinop.length() cannot be > 30.  joinop=%s", joinop));
    this.joinop = joinop;
    setHash(null);
    return this;
  }

  /**
   * An SQL where clause, or portion thereof, as appropriate.
   * 
   * @return clause
   */
  public String getClause() {
    return clause;
  }

  /**
   * An SQL where clause, or portion thereof, as appropriate.
   * 
   * @param clause
   * @throws IllegalArgumentException if clause.length() >= 1024
   */
  public Complexjoin setClause(String clause) {
    if (clause.length() > 1024)
      throw new IllegalArgumentException(
          String.format("clause.length() cannot be > 1024.  clause=%s", clause));
    this.clause = clause;
    setHash(null);
    return this;
  }

  /**
   * Determines if the information in the given row is to be used as active or ignored as inactive.
   * 
   * @return active
   */
  public String getActive() {
    return active;
  }

  /**
   * Determines if the information in the given row is to be used as active or ignored as inactive.
   * 
   * @param active
   * @throws IllegalArgumentException if active.length() >= 1
   */
  public Complexjoin setActive(String active) {
    if (active.length() > 1)
      throw new IllegalArgumentException(
          String.format("active.length() cannot be > 1.  active=%s", active));
    this.active = active;
    setHash(null);
    return this;
  }

  /**
   * Determines the level of enforcement: h (hard) or s (soft), where hard means that the relation
   * must be obeyed, and soft means that violations are possible but should be explained.
   * 
   * @return enforce
   */
  public String getEnforce() {
    return enforce;
  }

  /**
   * Determines the level of enforcement: h (hard) or s (soft), where hard means that the relation
   * must be obeyed, and soft means that violations are possible but should be explained.
   * 
   * @param enforce
   * @throws IllegalArgumentException if enforce.length() >= 1
   */
  public Complexjoin setEnforce(String enforce) {
    if (enforce.length() > 1)
      throw new IllegalArgumentException(
          String.format("enforce.length() cannot be > 1.  enforce=%s", enforce));
    this.enforce = enforce;
    setHash(null);
    return this;
  }

  /**
   * Definition
   * 
   * @return definition
   */
  public String getDefinition() {
    return definition;
  }

  /**
   * Definition
   * 
   * @param definition
   * @throws IllegalArgumentException if definition.length() >= 80
   */
  public Complexjoin setDefinition(String definition) {
    if (definition.length() > 80)
      throw new IllegalArgumentException(
          String.format("definition.length() cannot be > 80.  definition=%s", definition));
    this.definition = definition;
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
  public Complexjoin setAuth(String auth) {
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
  public Complexjoin setCommid(long commid) {
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
