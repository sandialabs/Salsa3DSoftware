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
package gov.sandia.gnem.dbtabledefs.gmp;

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
 * ?
 */
public class Predictsim extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * -
   */
  private long predictionid1;

  static final public long PREDICTIONID1_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long predictionid2;

  static final public long PREDICTIONID2_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private double similarity;

  static final public double SIMILARITY_NA = Double.NaN;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("predictionid1", Columns.FieldType.LONG, "%d");
    columns.add("predictionid2", Columns.FieldType.LONG, "%d");
    columns.add("similarity", Columns.FieldType.DOUBLE, "%22.15e");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Predictsim(long predictionid1, long predictionid2, double similarity) {
    setValues(predictionid1, predictionid2, similarity);
  }

  private void setValues(long predictionid1, long predictionid2, double similarity) {
    this.predictionid1 = predictionid1;
    this.predictionid2 = predictionid2;
    this.similarity = similarity;
  }

  /**
   * Copy constructor.
   */
  public Predictsim(Predictsim other) {
    this.predictionid1 = other.getPredictionid1();
    this.predictionid2 = other.getPredictionid2();
    this.similarity = other.getSimilarity();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Predictsim() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(PREDICTIONID1_NA, PREDICTIONID2_NA, SIMILARITY_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
        + " is not a valid input name ...");
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "similarity":
        return similarity;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "similarity":
        similarity = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "predictionid1":
        return predictionid1;
      case "predictionid2":
        return predictionid2;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "predictionid1":
        predictionid1 = value;
        break;
      case "predictionid2":
        predictionid2 = value;
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
  public Predictsim(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Predictsim(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readDouble());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Predictsim(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getDouble());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Predictsim(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Predictsim(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getDouble(offset + 3));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[3];
    values[0] = predictionid1;
    values[1] = predictionid2;
    values[2] = similarity;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[4];
    values[0] = predictionid1;
    values[1] = predictionid2;
    values[2] = similarity;
    values[3] = lddate;
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
    output.writeLong(predictionid1);
    output.writeLong(predictionid2);
    output.writeDouble(similarity);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(predictionid1);
    output.putLong(predictionid2);
    output.putDouble(similarity);
  }

  /**
   * Read a Collection of Predictsim objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Predictsim objects.
   * @throws IOException
   */
  static public void readPredictsims(BufferedReader input, Collection<Predictsim> rows)
      throws IOException {
    String[] saved = Predictsim.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Predictsim
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Predictsim(new Scanner(line)));
    }
    input.close();
    Predictsim.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Predictsim objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Predictsim objects.
   * @throws IOException
   */
  static public void readPredictsims(File inputFile, Collection<Predictsim> rows)
      throws IOException {
    readPredictsims(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Predictsim objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Predictsim objects.
   * @throws IOException
   */
  static public void readPredictsims(InputStream inputStream, Collection<Predictsim> rows)
      throws IOException {
    readPredictsims(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Predictsim objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Predictsim objects
   * @throws IOException
   */
  static public Set<Predictsim> readPredictsims(BufferedReader input) throws IOException {
    Set<Predictsim> rows = new LinkedHashSet<Predictsim>();
    readPredictsims(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Predictsim objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Predictsim objects
   * @throws IOException
   */
  static public Set<Predictsim> readPredictsims(File inputFile) throws IOException {
    return readPredictsims(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Predictsim objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Predictsim objects
   * @throws IOException
   */
  static public Set<Predictsim> readPredictsims(InputStream input) throws IOException {
    return readPredictsims(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Predictsim objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param predictsims the Predictsim objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Predictsim> predictsims)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Predictsim predictsim : predictsims)
      predictsim.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Predictsim objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param predictsims the Predictsim objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Predictsim> predictsims, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?)");
      for (Predictsim predictsim : predictsims) {
        int i = 0;
        statement.setLong(++i, predictsim.predictionid1);
        statement.setLong(++i, predictsim.predictionid2);
        statement.setDouble(++i, predictsim.similarity);
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
   *        Predictsim table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Predictsim> readPredictsims(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Predictsim> results = new HashSet<Predictsim>();
    readPredictsims(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Predictsim table.
   * @param predictsims
   * @throws SQLException
   */
  static public void readPredictsims(Connection connection, String selectStatement,
      Set<Predictsim> predictsims) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        predictsims.add(new Predictsim(rs));
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
   * this Predictsim object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Predictsim object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("predictionid1, predictionid2, similarity, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(predictionid1)).append(", ");
    sql.append(Long.toString(predictionid2)).append(", ");
    sql.append(Double.toString(similarity)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Predictsim in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Predictsim in the database
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
   * Generate a sql script to create a table of type Predictsim in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Predictsim in the database
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
    buf.append("predictionid1 number(10)           NOT NULL,\n");
    buf.append("predictionid2 number(10)           NOT NULL,\n");
    buf.append("similarity   float(126)           NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (predictionid1,predictionid2)");
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
    return 24;
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
    return (other instanceof Predictsim) && ((Predictsim) other).predictionid1 == predictionid1
        && ((Predictsim) other).predictionid2 == predictionid2;
  }

  /**
   * -
   * 
   * @return predictionid1
   */
  public long getPredictionid1() {
    return predictionid1;
  }

  /**
   * -
   * 
   * @param predictionid1
   * @throws IllegalArgumentException if predictionid1 >= 10000000000
   */
  public Predictsim setPredictionid1(long predictionid1) {
    if (predictionid1 >= 10000000000L)
      throw new IllegalArgumentException(
          "predictionid1=" + predictionid1 + " but cannot be >= 10000000000");
    this.predictionid1 = predictionid1;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return predictionid2
   */
  public long getPredictionid2() {
    return predictionid2;
  }

  /**
   * -
   * 
   * @param predictionid2
   * @throws IllegalArgumentException if predictionid2 >= 10000000000
   */
  public Predictsim setPredictionid2(long predictionid2) {
    if (predictionid2 >= 10000000000L)
      throw new IllegalArgumentException(
          "predictionid2=" + predictionid2 + " but cannot be >= 10000000000");
    this.predictionid2 = predictionid2;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return similarity
   */
  public double getSimilarity() {
    return similarity;
  }

  /**
   * -
   * 
   * @param similarity
   */
  public Predictsim setSimilarity(double similarity) {
    this.similarity = similarity;
    setHash(null);
    return this;
  }

  /**
   * Retrieve the name of the schema.
   * 
   * @return schema name
   */
  static public String getSchemaName() {
    return "GMP";
  }

}
