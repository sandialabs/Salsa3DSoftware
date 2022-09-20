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
 * deconvolution
 */
public class Deconvolution extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Deconvolution identifier
   */
  private long deconid;

  static final public long DECONID_NA = Long.MIN_VALUE;

  /**
   * Low cut frequency given as fraction of <I>minlowpassfreq</I>. This is the low-frequency
   * termination of the entire pass band of the filter, including the low-frequency taper. For the
   * actural filter for a particular waveform, the resulting absolute limit may be higher, due to
   * short segment (see <I>tfactor</I>), but will always be the same fraction of the final
   * <I>minlowpassfreq</I>
   */
  private double lowcutfrac;

  static final public double LOWCUTFRAC_NA = Double.NaN;

  /**
   * The minimum frequency used as the low pass for instrument correction. This is the low-frequency
   * end of the flat pass band of the filter in absolute units (Hz). For the actual filter for a
   * particular waveform, this limit may be higher, due to short segments (see <I>tfactor<\I>).
   * <p>
   * Units: 1/s
   */
  private double lowpassfreq;

  static final public double LOWPASSFREQ_NA = Double.NaN;

  /**
   * High cut given as fraction of Nyquist (obtained from nominal sample rate). This is the
   * high-frequency termination of the entire pass band of the filter, including the high frequency
   * taper
   */
  private double highcutfrac;

  static final public double HIGHCUTFRAC_NA = Double.NaN;

  /**
   * High pass given as fraction of Nyquist (obtained from nominal sample rate). This is the
   * high-frequency end of the flat pass band of the filter.
   */
  private double highpassfrac;

  static final public double HIGHPASSFRAC_NA = Double.NaN;

  /**
   * Order of prewhitening filter (or 0 for no filter).
   */
  private long prewhiteorder;

  static final public long PREWHITEORDER_NA = -1;

  /**
   * Taper fraction relative to 1 / <I>lowpassfreq</I> (See <I>lowpassfreq</I>, <I>tfactor</I>).
   * <I>Lowpassfreq</I> is the only value of the 4 points of the trapezoidal frequency band for
   * deconvolution that is a true value rather than a fraction of another value. Therefore, the
   * taper fraction is defined relative to <I>lowpassfreq</I> to eliminate intermediate
   * dependencies. <I>Lowcutfrac</I> would determine the lowest frequencies in, for example, a
   * resulting deconvolution, so taper fraction should not be defined such that it would reach
   * beyond the period defined by 1 / (<I>lowcutfrac</I> * <I>lowpasfreq</I>). The taper should also
   * be chosen so as not to affect the signal in question. This may imply choosing a sufficiently
   * long amount of noise before any signal in question.
   */
  private double tapfrac;

  static final public double TAPFRAC_NA = -1;

  /**
   * The minimum number of cycles in the window before calculating an amplitude. A typical value is
   * 2. <I>tractor</I> divided by the window length will replace <I>minlowpassfreq</I> if it is
   * larger than <I>minlowpassfreq</I>
   */
  private double tfactor;

  static final public double TFACTOR_NA = Double.NaN;

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
    columns.add("deconid", Columns.FieldType.LONG, "%d");
    columns.add("lowcutfrac", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("lowpassfreq", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("highcutfrac", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("highpassfrac", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("prewhiteorder", Columns.FieldType.LONG, "%d");
    columns.add("tapfrac", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("tfactor", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Deconvolution(long deconid, double lowcutfrac, double lowpassfreq, double highcutfrac,
      double highpassfrac, long prewhiteorder, double tapfrac, double tfactor, String auth,
      long commid) {
    setValues(deconid, lowcutfrac, lowpassfreq, highcutfrac, highpassfrac, prewhiteorder, tapfrac,
        tfactor, auth, commid);
  }

  private void setValues(long deconid, double lowcutfrac, double lowpassfreq, double highcutfrac,
      double highpassfrac, long prewhiteorder, double tapfrac, double tfactor, String auth,
      long commid) {
    this.deconid = deconid;
    this.lowcutfrac = lowcutfrac;
    this.lowpassfreq = lowpassfreq;
    this.highcutfrac = highcutfrac;
    this.highpassfrac = highpassfrac;
    this.prewhiteorder = prewhiteorder;
    this.tapfrac = tapfrac;
    this.tfactor = tfactor;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Deconvolution(Deconvolution other) {
    this.deconid = other.getDeconid();
    this.lowcutfrac = other.getLowcutfrac();
    this.lowpassfreq = other.getLowpassfreq();
    this.highcutfrac = other.getHighcutfrac();
    this.highpassfrac = other.getHighpassfrac();
    this.prewhiteorder = other.getPrewhiteorder();
    this.tapfrac = other.getTapfrac();
    this.tfactor = other.getTfactor();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Deconvolution() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(DECONID_NA, LOWCUTFRAC_NA, LOWPASSFREQ_NA, HIGHCUTFRAC_NA, HIGHPASSFRAC_NA,
        PREWHITEORDER_NA, TAPFRAC_NA, TFACTOR_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
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
      case "lowcutfrac":
        return lowcutfrac;
      case "lowpassfreq":
        return lowpassfreq;
      case "highcutfrac":
        return highcutfrac;
      case "highpassfrac":
        return highpassfrac;
      case "tapfrac":
        return tapfrac;
      case "tfactor":
        return tfactor;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "lowcutfrac":
        lowcutfrac = value;
        break;
      case "lowpassfreq":
        lowpassfreq = value;
        break;
      case "highcutfrac":
        highcutfrac = value;
        break;
      case "highpassfrac":
        highpassfrac = value;
        break;
      case "tapfrac":
        tapfrac = value;
        break;
      case "tfactor":
        tfactor = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "deconid":
        return deconid;
      case "prewhiteorder":
        return prewhiteorder;
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
      case "deconid":
        deconid = value;
        break;
      case "prewhiteorder":
        prewhiteorder = value;
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
  public Deconvolution(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Deconvolution(DataInputStream input) throws IOException {
    this(input.readLong(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readLong(), input.readDouble(), input.readDouble(),
        readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Deconvolution(ByteBuffer input) {
    this(input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getLong(), input.getDouble(), input.getDouble(), readString(input),
        input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Deconvolution(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Deconvolution(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getLong(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getString(offset + 9),
        input.getLong(offset + 10));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[10];
    values[0] = deconid;
    values[1] = lowcutfrac;
    values[2] = lowpassfreq;
    values[3] = highcutfrac;
    values[4] = highpassfrac;
    values[5] = prewhiteorder;
    values[6] = tapfrac;
    values[7] = tfactor;
    values[8] = auth;
    values[9] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[11];
    values[0] = deconid;
    values[1] = lowcutfrac;
    values[2] = lowpassfreq;
    values[3] = highcutfrac;
    values[4] = highpassfrac;
    values[5] = prewhiteorder;
    values[6] = tapfrac;
    values[7] = tfactor;
    values[8] = auth;
    values[9] = commid;
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
    output.writeLong(deconid);
    output.writeDouble(lowcutfrac);
    output.writeDouble(lowpassfreq);
    output.writeDouble(highcutfrac);
    output.writeDouble(highpassfrac);
    output.writeLong(prewhiteorder);
    output.writeDouble(tapfrac);
    output.writeDouble(tfactor);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(deconid);
    output.putDouble(lowcutfrac);
    output.putDouble(lowpassfreq);
    output.putDouble(highcutfrac);
    output.putDouble(highpassfrac);
    output.putLong(prewhiteorder);
    output.putDouble(tapfrac);
    output.putDouble(tfactor);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Deconvolution objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Deconvolution objects.
   * @throws IOException
   */
  static public void readDeconvolutions(BufferedReader input, Collection<Deconvolution> rows)
      throws IOException {
    String[] saved = Deconvolution.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Deconvolution
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Deconvolution(new Scanner(line)));
    }
    input.close();
    Deconvolution.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Deconvolution objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Deconvolution objects.
   * @throws IOException
   */
  static public void readDeconvolutions(File inputFile, Collection<Deconvolution> rows)
      throws IOException {
    readDeconvolutions(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Deconvolution objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Deconvolution objects.
   * @throws IOException
   */
  static public void readDeconvolutions(InputStream inputStream, Collection<Deconvolution> rows)
      throws IOException {
    readDeconvolutions(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Deconvolution objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Deconvolution objects
   * @throws IOException
   */
  static public Set<Deconvolution> readDeconvolutions(BufferedReader input) throws IOException {
    Set<Deconvolution> rows = new LinkedHashSet<Deconvolution>();
    readDeconvolutions(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Deconvolution objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Deconvolution objects
   * @throws IOException
   */
  static public Set<Deconvolution> readDeconvolutions(File inputFile) throws IOException {
    return readDeconvolutions(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Deconvolution objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Deconvolution objects
   * @throws IOException
   */
  static public Set<Deconvolution> readDeconvolutions(InputStream input) throws IOException {
    return readDeconvolutions(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Deconvolution objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param deconvolutions the Deconvolution objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Deconvolution> deconvolutions)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Deconvolution deconvolution : deconvolutions)
      deconvolution.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Deconvolution objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param deconvolutions the Deconvolution objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Deconvolution> deconvolutions, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?)");
      for (Deconvolution deconvolution : deconvolutions) {
        int i = 0;
        statement.setLong(++i, deconvolution.deconid);
        statement.setDouble(++i, deconvolution.lowcutfrac);
        statement.setDouble(++i, deconvolution.lowpassfreq);
        statement.setDouble(++i, deconvolution.highcutfrac);
        statement.setDouble(++i, deconvolution.highpassfrac);
        statement.setLong(++i, deconvolution.prewhiteorder);
        statement.setDouble(++i, deconvolution.tapfrac);
        statement.setDouble(++i, deconvolution.tfactor);
        statement.setString(++i, deconvolution.auth);
        statement.setLong(++i, deconvolution.commid);
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
   *        Deconvolution table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Deconvolution> readDeconvolutions(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Deconvolution> results = new HashSet<Deconvolution>();
    readDeconvolutions(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Deconvolution table.
   * @param deconvolutions
   * @throws SQLException
   */
  static public void readDeconvolutions(Connection connection, String selectStatement,
      Set<Deconvolution> deconvolutions) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        deconvolutions.add(new Deconvolution(rs));
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
   * this Deconvolution object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Deconvolution object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "deconid, lowcutfrac, lowpassfreq, highcutfrac, highpassfrac, prewhiteorder, tapfrac, tfactor, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(deconid)).append(", ");
    sql.append(Double.toString(lowcutfrac)).append(", ");
    sql.append(Double.toString(lowpassfreq)).append(", ");
    sql.append(Double.toString(highcutfrac)).append(", ");
    sql.append(Double.toString(highpassfrac)).append(", ");
    sql.append(Long.toString(prewhiteorder)).append(", ");
    sql.append(Double.toString(tapfrac)).append(", ");
    sql.append(Double.toString(tfactor)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Deconvolution in the database. Primary and unique keys are set, if
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
   * Create a table of type Deconvolution in the database
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
   * Generate a sql script to create a table of type Deconvolution in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Deconvolution in the database
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
    buf.append("deconid      number(9)            NOT NULL,\n");
    buf.append("lowcutfrac   float(24)            NOT NULL,\n");
    buf.append("lowpassfreq  float(24)            NOT NULL,\n");
    buf.append("highcutfrac  float(24)            NOT NULL,\n");
    buf.append("highpassfrac float(24)            NOT NULL,\n");
    buf.append("prewhiteorder number(4)            NOT NULL,\n");
    buf.append("tapfrac      float(24)            NOT NULL,\n");
    buf.append("tfactor      float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (deconid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (lowcutfrac,lowpassfreq,highcutfrac,highpassfrac,prewhiteorder,tapfrac,tfactor)");
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
    return 96;
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
    return (other instanceof Deconvolution) && ((Deconvolution) other).deconid == deconid;
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
    return (other instanceof Deconvolution) && ((Deconvolution) other).lowcutfrac == lowcutfrac
        && ((Deconvolution) other).lowpassfreq == lowpassfreq
        && ((Deconvolution) other).highcutfrac == highcutfrac
        && ((Deconvolution) other).highpassfrac == highpassfrac
        && ((Deconvolution) other).prewhiteorder == prewhiteorder
        && ((Deconvolution) other).tapfrac == tapfrac && ((Deconvolution) other).tfactor == tfactor;
  }

  /**
   * Deconvolution identifier
   * 
   * @return deconid
   */
  public long getDeconid() {
    return deconid;
  }

  /**
   * Deconvolution identifier
   * 
   * @param deconid
   * @throws IllegalArgumentException if deconid >= 1000000000
   */
  public Deconvolution setDeconid(long deconid) {
    if (deconid >= 1000000000L)
      throw new IllegalArgumentException("deconid=" + deconid + " but cannot be >= 1000000000");
    this.deconid = deconid;
    setHash(null);
    return this;
  }

  /**
   * Low cut frequency given as fraction of <I>minlowpassfreq</I>. This is the low-frequency
   * termination of the entire pass band of the filter, including the low-frequency taper. For the
   * actural filter for a particular waveform, the resulting absolute limit may be higher, due to
   * short segment (see <I>tfactor</I>), but will always be the same fraction of the final
   * <I>minlowpassfreq</I>
   * 
   * @return lowcutfrac
   */
  public double getLowcutfrac() {
    return lowcutfrac;
  }

  /**
   * Low cut frequency given as fraction of <I>minlowpassfreq</I>. This is the low-frequency
   * termination of the entire pass band of the filter, including the low-frequency taper. For the
   * actural filter for a particular waveform, the resulting absolute limit may be higher, due to
   * short segment (see <I>tfactor</I>), but will always be the same fraction of the final
   * <I>minlowpassfreq</I>
   * 
   * @param lowcutfrac
   */
  public Deconvolution setLowcutfrac(double lowcutfrac) {
    this.lowcutfrac = lowcutfrac;
    setHash(null);
    return this;
  }

  /**
   * The minimum frequency used as the low pass for instrument correction. This is the low-frequency
   * end of the flat pass band of the filter in absolute units (Hz). For the actual filter for a
   * particular waveform, this limit may be higher, due to short segments (see <I>tfactor<\I>).
   * <p>
   * Units: 1/s
   * 
   * @return lowpassfreq
   */
  public double getLowpassfreq() {
    return lowpassfreq;
  }

  /**
   * The minimum frequency used as the low pass for instrument correction. This is the low-frequency
   * end of the flat pass band of the filter in absolute units (Hz). For the actual filter for a
   * particular waveform, this limit may be higher, due to short segments (see <I>tfactor<\I>).
   * <p>
   * Units: 1/s
   * 
   * @param lowpassfreq
   */
  public Deconvolution setLowpassfreq(double lowpassfreq) {
    this.lowpassfreq = lowpassfreq;
    setHash(null);
    return this;
  }

  /**
   * High cut given as fraction of Nyquist (obtained from nominal sample rate). This is the
   * high-frequency termination of the entire pass band of the filter, including the high frequency
   * taper
   * 
   * @return highcutfrac
   */
  public double getHighcutfrac() {
    return highcutfrac;
  }

  /**
   * High cut given as fraction of Nyquist (obtained from nominal sample rate). This is the
   * high-frequency termination of the entire pass band of the filter, including the high frequency
   * taper
   * 
   * @param highcutfrac
   */
  public Deconvolution setHighcutfrac(double highcutfrac) {
    this.highcutfrac = highcutfrac;
    setHash(null);
    return this;
  }

  /**
   * High pass given as fraction of Nyquist (obtained from nominal sample rate). This is the
   * high-frequency end of the flat pass band of the filter.
   * 
   * @return highpassfrac
   */
  public double getHighpassfrac() {
    return highpassfrac;
  }

  /**
   * High pass given as fraction of Nyquist (obtained from nominal sample rate). This is the
   * high-frequency end of the flat pass band of the filter.
   * 
   * @param highpassfrac
   */
  public Deconvolution setHighpassfrac(double highpassfrac) {
    this.highpassfrac = highpassfrac;
    setHash(null);
    return this;
  }

  /**
   * Order of prewhitening filter (or 0 for no filter).
   * 
   * @return prewhiteorder
   */
  public long getPrewhiteorder() {
    return prewhiteorder;
  }

  /**
   * Order of prewhitening filter (or 0 for no filter).
   * 
   * @param prewhiteorder
   * @throws IllegalArgumentException if prewhiteorder >= 10000
   */
  public Deconvolution setPrewhiteorder(long prewhiteorder) {
    if (prewhiteorder >= 10000L)
      throw new IllegalArgumentException(
          "prewhiteorder=" + prewhiteorder + " but cannot be >= 10000");
    this.prewhiteorder = prewhiteorder;
    setHash(null);
    return this;
  }

  /**
   * Taper fraction relative to 1 / <I>lowpassfreq</I> (See <I>lowpassfreq</I>, <I>tfactor</I>).
   * <I>Lowpassfreq</I> is the only value of the 4 points of the trapezoidal frequency band for
   * deconvolution that is a true value rather than a fraction of another value. Therefore, the
   * taper fraction is defined relative to <I>lowpassfreq</I> to eliminate intermediate
   * dependencies. <I>Lowcutfrac</I> would determine the lowest frequencies in, for example, a
   * resulting deconvolution, so taper fraction should not be defined such that it would reach
   * beyond the period defined by 1 / (<I>lowcutfrac</I> * <I>lowpasfreq</I>). The taper should also
   * be chosen so as not to affect the signal in question. This may imply choosing a sufficiently
   * long amount of noise before any signal in question.
   * 
   * @return tapfrac
   */
  public double getTapfrac() {
    return tapfrac;
  }

  /**
   * Taper fraction relative to 1 / <I>lowpassfreq</I> (See <I>lowpassfreq</I>, <I>tfactor</I>).
   * <I>Lowpassfreq</I> is the only value of the 4 points of the trapezoidal frequency band for
   * deconvolution that is a true value rather than a fraction of another value. Therefore, the
   * taper fraction is defined relative to <I>lowpassfreq</I> to eliminate intermediate
   * dependencies. <I>Lowcutfrac</I> would determine the lowest frequencies in, for example, a
   * resulting deconvolution, so taper fraction should not be defined such that it would reach
   * beyond the period defined by 1 / (<I>lowcutfrac</I> * <I>lowpasfreq</I>). The taper should also
   * be chosen so as not to affect the signal in question. This may imply choosing a sufficiently
   * long amount of noise before any signal in question.
   * 
   * @param tapfrac
   */
  public Deconvolution setTapfrac(double tapfrac) {
    this.tapfrac = tapfrac;
    setHash(null);
    return this;
  }

  /**
   * The minimum number of cycles in the window before calculating an amplitude. A typical value is
   * 2. <I>tractor</I> divided by the window length will replace <I>minlowpassfreq</I> if it is
   * larger than <I>minlowpassfreq</I>
   * 
   * @return tfactor
   */
  public double getTfactor() {
    return tfactor;
  }

  /**
   * The minimum number of cycles in the window before calculating an amplitude. A typical value is
   * 2. <I>tractor</I> divided by the window length will replace <I>minlowpassfreq</I> if it is
   * larger than <I>minlowpassfreq</I>
   * 
   * @param tfactor
   */
  public Deconvolution setTfactor(double tfactor) {
    this.tfactor = tfactor;
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
  public Deconvolution setAuth(String auth) {
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
  public Deconvolution setCommid(long commid) {
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
