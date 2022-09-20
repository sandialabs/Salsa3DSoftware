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
 * site_transfer
 */
public class Site_transfer extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   */
  private long magyieldid;

  static final public long MAGYIELDID_NA = Long.MIN_VALUE;

  /**
   * Site correction that takes path corrected amplitudes to absolute source spectral estimates.
   * <p>
   * Units: log10(Ns),log10(Nm/count),log10(Nm/nm)
   */
  private double sitecor;

  static final public double SITECOR_NA = -999;

  /**
   * Error on site correction that takes path corrected amplitudes to absolute source spectral
   * estimates.
   * <p>
   * Units: log10(Ns),log10(Nm/count),log10(Nm/nm)
   */
  private double delsitecor;

  static final public double DELSITECOR_NA = -1;

  /**
   * Site correction unit; recommended types are: log10(Ns) to correct m/s to Nm, log10(Nm/count) to
   * correct counts to Nm, log10(Nm/nm) to correct nm to Nm.
   */
  private String siteunit;

  static final public String SITEUNIT_NA = null;

  /**
   * Two dimension egf variation identifier
   */
  private long egf2did;

  static final public long EGF2DID_NA = -1;

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
    columns.add("magyieldid", Columns.FieldType.LONG, "%d");
    columns.add("sitecor", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("delsitecor", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("siteunit", Columns.FieldType.STRING, "%s");
    columns.add("egf2did", Columns.FieldType.LONG, "%d");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Site_transfer(long magyieldid, double sitecor, double delsitecor, String siteunit,
      long egf2did, String auth, long commid) {
    setValues(magyieldid, sitecor, delsitecor, siteunit, egf2did, auth, commid);
  }

  private void setValues(long magyieldid, double sitecor, double delsitecor, String siteunit,
      long egf2did, String auth, long commid) {
    this.magyieldid = magyieldid;
    this.sitecor = sitecor;
    this.delsitecor = delsitecor;
    this.siteunit = siteunit;
    this.egf2did = egf2did;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Site_transfer(Site_transfer other) {
    this.magyieldid = other.getMagyieldid();
    this.sitecor = other.getSitecor();
    this.delsitecor = other.getDelsitecor();
    this.siteunit = other.getSiteunit();
    this.egf2did = other.getEgf2did();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Site_transfer() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MAGYIELDID_NA, SITECOR_NA, DELSITECOR_NA, SITEUNIT_NA, EGF2DID_NA, AUTH_NA,
        COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "siteunit":
        return siteunit;
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
      case "siteunit":
        siteunit = value;
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
      case "sitecor":
        return sitecor;
      case "delsitecor":
        return delsitecor;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "sitecor":
        sitecor = value;
        break;
      case "delsitecor":
        delsitecor = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "magyieldid":
        return magyieldid;
      case "egf2did":
        return egf2did;
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
      case "magyieldid":
        magyieldid = value;
        break;
      case "egf2did":
        egf2did = value;
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
  public Site_transfer(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Site_transfer(DataInputStream input) throws IOException {
    this(input.readLong(), input.readDouble(), input.readDouble(), readString(input),
        input.readLong(), readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Site_transfer(ByteBuffer input) {
    this(input.getLong(), input.getDouble(), input.getDouble(), readString(input), input.getLong(),
        readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Site_transfer(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Site_transfer(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getString(offset + 4), input.getLong(offset + 5), input.getString(offset + 6),
        input.getLong(offset + 7));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[7];
    values[0] = magyieldid;
    values[1] = sitecor;
    values[2] = delsitecor;
    values[3] = siteunit;
    values[4] = egf2did;
    values[5] = auth;
    values[6] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[8];
    values[0] = magyieldid;
    values[1] = sitecor;
    values[2] = delsitecor;
    values[3] = siteunit;
    values[4] = egf2did;
    values[5] = auth;
    values[6] = commid;
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
    output.writeLong(magyieldid);
    output.writeDouble(sitecor);
    output.writeDouble(delsitecor);
    writeString(output, siteunit);
    output.writeLong(egf2did);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(magyieldid);
    output.putDouble(sitecor);
    output.putDouble(delsitecor);
    writeString(output, siteunit);
    output.putLong(egf2did);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Site_transfer objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Site_transfer objects.
   * @throws IOException
   */
  static public void readSite_transfers(BufferedReader input, Collection<Site_transfer> rows)
      throws IOException {
    String[] saved = Site_transfer.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Site_transfer
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Site_transfer(new Scanner(line)));
    }
    input.close();
    Site_transfer.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Site_transfer objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Site_transfer objects.
   * @throws IOException
   */
  static public void readSite_transfers(File inputFile, Collection<Site_transfer> rows)
      throws IOException {
    readSite_transfers(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Site_transfer objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Site_transfer objects.
   * @throws IOException
   */
  static public void readSite_transfers(InputStream inputStream, Collection<Site_transfer> rows)
      throws IOException {
    readSite_transfers(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Site_transfer objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Site_transfer objects
   * @throws IOException
   */
  static public Set<Site_transfer> readSite_transfers(BufferedReader input) throws IOException {
    Set<Site_transfer> rows = new LinkedHashSet<Site_transfer>();
    readSite_transfers(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Site_transfer objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Site_transfer objects
   * @throws IOException
   */
  static public Set<Site_transfer> readSite_transfers(File inputFile) throws IOException {
    return readSite_transfers(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Site_transfer objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Site_transfer objects
   * @throws IOException
   */
  static public Set<Site_transfer> readSite_transfers(InputStream input) throws IOException {
    return readSite_transfers(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Site_transfer objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param site_transfers the Site_transfer objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Site_transfer> site_transfers)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Site_transfer site_transfer : site_transfers)
      site_transfer.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Site_transfer objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param site_transfers the Site_transfer objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Site_transfer> site_transfers, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?)");
      for (Site_transfer site_transfer : site_transfers) {
        int i = 0;
        statement.setLong(++i, site_transfer.magyieldid);
        statement.setDouble(++i, site_transfer.sitecor);
        statement.setDouble(++i, site_transfer.delsitecor);
        statement.setString(++i, site_transfer.siteunit);
        statement.setLong(++i, site_transfer.egf2did);
        statement.setString(++i, site_transfer.auth);
        statement.setLong(++i, site_transfer.commid);
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
   *        Site_transfer table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Site_transfer> readSite_transfers(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Site_transfer> results = new HashSet<Site_transfer>();
    readSite_transfers(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Site_transfer table.
   * @param site_transfers
   * @throws SQLException
   */
  static public void readSite_transfers(Connection connection, String selectStatement,
      Set<Site_transfer> site_transfers) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        site_transfers.add(new Site_transfer(rs));
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
   * this Site_transfer object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Site_transfer object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("magyieldid, sitecor, delsitecor, siteunit, egf2did, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(magyieldid)).append(", ");
    sql.append(Double.toString(sitecor)).append(", ");
    sql.append(Double.toString(delsitecor)).append(", ");
    sql.append("'").append(siteunit).append("', ");
    sql.append(Long.toString(egf2did)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Site_transfer in the database. Primary and unique keys are set, if
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
   * Create a table of type Site_transfer in the database
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
   * Generate a sql script to create a table of type Site_transfer in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Site_transfer in the database
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
    buf.append("magyieldid   number(9)            NOT NULL,\n");
    buf.append("sitecor      float(24)            NOT NULL,\n");
    buf.append("delsitecor   float(24)            NOT NULL,\n");
    buf.append("siteunit     varchar2(30)         NOT NULL,\n");
    buf.append("egf2did      number(9)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (magyieldid)");
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
    return 98;
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
    return (other instanceof Site_transfer) && ((Site_transfer) other).magyieldid == magyieldid;
  }

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   * 
   * @return magyieldid
   */
  public long getMagyieldid() {
    return magyieldid;
  }

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   * 
   * @param magyieldid
   * @throws IllegalArgumentException if magyieldid >= 1000000000
   */
  public Site_transfer setMagyieldid(long magyieldid) {
    if (magyieldid >= 1000000000L)
      throw new IllegalArgumentException(
          "magyieldid=" + magyieldid + " but cannot be >= 1000000000");
    this.magyieldid = magyieldid;
    setHash(null);
    return this;
  }

  /**
   * Site correction that takes path corrected amplitudes to absolute source spectral estimates.
   * <p>
   * Units: log10(Ns),log10(Nm/count),log10(Nm/nm)
   * 
   * @return sitecor
   */
  public double getSitecor() {
    return sitecor;
  }

  /**
   * Site correction that takes path corrected amplitudes to absolute source spectral estimates.
   * <p>
   * Units: log10(Ns),log10(Nm/count),log10(Nm/nm)
   * 
   * @param sitecor
   */
  public Site_transfer setSitecor(double sitecor) {
    this.sitecor = sitecor;
    setHash(null);
    return this;
  }

  /**
   * Error on site correction that takes path corrected amplitudes to absolute source spectral
   * estimates.
   * <p>
   * Units: log10(Ns),log10(Nm/count),log10(Nm/nm)
   * 
   * @return delsitecor
   */
  public double getDelsitecor() {
    return delsitecor;
  }

  /**
   * Error on site correction that takes path corrected amplitudes to absolute source spectral
   * estimates.
   * <p>
   * Units: log10(Ns),log10(Nm/count),log10(Nm/nm)
   * 
   * @param delsitecor
   */
  public Site_transfer setDelsitecor(double delsitecor) {
    this.delsitecor = delsitecor;
    setHash(null);
    return this;
  }

  /**
   * Site correction unit; recommended types are: log10(Ns) to correct m/s to Nm, log10(Nm/count) to
   * correct counts to Nm, log10(Nm/nm) to correct nm to Nm.
   * 
   * @return siteunit
   */
  public String getSiteunit() {
    return siteunit;
  }

  /**
   * Site correction unit; recommended types are: log10(Ns) to correct m/s to Nm, log10(Nm/count) to
   * correct counts to Nm, log10(Nm/nm) to correct nm to Nm.
   * 
   * @param siteunit
   * @throws IllegalArgumentException if siteunit.length() >= 30
   */
  public Site_transfer setSiteunit(String siteunit) {
    if (siteunit.length() > 30)
      throw new IllegalArgumentException(
          String.format("siteunit.length() cannot be > 30.  siteunit=%s", siteunit));
    this.siteunit = siteunit;
    setHash(null);
    return this;
  }

  /**
   * Two dimension egf variation identifier
   * 
   * @return egf2did
   */
  public long getEgf2did() {
    return egf2did;
  }

  /**
   * Two dimension egf variation identifier
   * 
   * @param egf2did
   * @throws IllegalArgumentException if egf2did >= 1000000000
   */
  public Site_transfer setEgf2did(long egf2did) {
    if (egf2did >= 1000000000L)
      throw new IllegalArgumentException("egf2did=" + egf2did + " but cannot be >= 1000000000");
    this.egf2did = egf2did;
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
  public Site_transfer setAuth(String auth) {
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
  public Site_transfer setCommid(long commid) {
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
