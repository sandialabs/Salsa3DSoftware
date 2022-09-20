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
 * nb_env
 */
public class Nb_env extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   */
  private long magyieldid;

  static final public long MAGYIELDID_NA = Long.MIN_VALUE;

  /**
   * Butterworth filter identifier
   */
  private long bwfilterid;

  static final public long BWFILTERID_NA = Long.MIN_VALUE;

  /**
   * Boxcar smoothing identfier.
   */
  private long smooid;

  static final public long SMOOID_NA = Long.MIN_VALUE;

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   */
  private long polyid;

  static final public long POLYID_NA = -1;

  /**
   * The stack slowness used in combining the elements of an array to create a resultant narrowband
   * envelope.
   * <p>
   * Units: s/km
   */
  private double slowness;

  static final public double SLOWNESS_NA = -1;

  /**
   * Path correction type (none, 1D, or 2D).
   */
  private String corrtype;

  static final public String CORRTYPE_NA = null;

  /**
   * Sampling rate. This column is the sample rate in samples per second. In the <B>instrument</B>
   * table, the column value is specifically the nominal sample rate, not accounting for clock
   * drift. In <B>wfdisc</B>, the value may vary slightly from the nominal to reflect clock drift.
   * <p>
   * Units: 1/s
   */
  private double samprate;

  static final public double SAMPRATE_NA = Double.NaN;

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   */
  private String phase;

  static final public String PHASE_NA = null;

  /**
   * The <I>phase</I> column has the format phase:type. <I>rootphase</I> is the standard direct
   * phase name portion of this (i.e. precedes the ':'). Following the colon would be indication of
   * code or direct portion of the signal and the code for the type of measurement. See <I>type</I>.
   */
  private String rootphase;

  static final public String ROOTPHASE_NA = null;

  /**
   * This is the code (a-z) indexing the narrowband corresponding to this record. <I>bandcode</I> is
   * part of the complex, composite <I>chan</I> name in the corresponding <B>sitechan</B> record.
   */
  private String bandcode;

  static final public String BANDCODE_NA = null;

  /**
   * This is the second to last character in the <I>chan</I> column in the corresponding
   * <B>sitechan</B> record and corresponds to the phase that is given in the <I>phase</I> column.
   * Lowercase codes represent 1D corrections for the phase, and uppercase codes represent 2D
   * corrections for the phase where available.
   */
  private String phasecode;

  static final public String PHASECODE_NA = null;

  /**
   * This is the channel from <B>sitechan</B> minus the leading 'e' and the band and phase codes.
   */
  private String rootchan;

  static final public String ROOTCHAN_NA = null;

  /**
   * The portion of <I>phase</I> after the ':'. Indicates coda or direct wave and the measurement
   * type (e.g. c1=coda, time window set 1). See <I>rootphase</I>.
   */
  private String type;

  static final public String TYPE_NA = null;

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
    columns.add("bwfilterid", Columns.FieldType.LONG, "%d");
    columns.add("smooid", Columns.FieldType.LONG, "%d");
    columns.add("polyid", Columns.FieldType.LONG, "%d");
    columns.add("slowness", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("corrtype", Columns.FieldType.STRING, "%s");
    columns.add("samprate", Columns.FieldType.DOUBLE, "%1.7f");
    columns.add("phase", Columns.FieldType.STRING, "%s");
    columns.add("rootphase", Columns.FieldType.STRING, "%s");
    columns.add("bandcode", Columns.FieldType.STRING, "%s");
    columns.add("phasecode", Columns.FieldType.STRING, "%s");
    columns.add("rootchan", Columns.FieldType.STRING, "%s");
    columns.add("type", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Nb_env(long magyieldid, long bwfilterid, long smooid, long polyid, double slowness,
      String corrtype, double samprate, String phase, String rootphase, String bandcode,
      String phasecode, String rootchan, String type, String auth, long commid) {
    setValues(magyieldid, bwfilterid, smooid, polyid, slowness, corrtype, samprate, phase,
        rootphase, bandcode, phasecode, rootchan, type, auth, commid);
  }

  private void setValues(long magyieldid, long bwfilterid, long smooid, long polyid,
      double slowness, String corrtype, double samprate, String phase, String rootphase,
      String bandcode, String phasecode, String rootchan, String type, String auth, long commid) {
    this.magyieldid = magyieldid;
    this.bwfilterid = bwfilterid;
    this.smooid = smooid;
    this.polyid = polyid;
    this.slowness = slowness;
    this.corrtype = corrtype;
    this.samprate = samprate;
    this.phase = phase;
    this.rootphase = rootphase;
    this.bandcode = bandcode;
    this.phasecode = phasecode;
    this.rootchan = rootchan;
    this.type = type;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Nb_env(Nb_env other) {
    this.magyieldid = other.getMagyieldid();
    this.bwfilterid = other.getBwfilterid();
    this.smooid = other.getSmooid();
    this.polyid = other.getPolyid();
    this.slowness = other.getSlowness();
    this.corrtype = other.getCorrtype();
    this.samprate = other.getSamprate();
    this.phase = other.getPhase();
    this.rootphase = other.getRootphase();
    this.bandcode = other.getBandcode();
    this.phasecode = other.getPhasecode();
    this.rootchan = other.getRootchan();
    this.type = other.getType();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Nb_env() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MAGYIELDID_NA, BWFILTERID_NA, SMOOID_NA, POLYID_NA, SLOWNESS_NA, CORRTYPE_NA,
        SAMPRATE_NA, PHASE_NA, ROOTPHASE_NA, BANDCODE_NA, PHASECODE_NA, ROOTCHAN_NA, TYPE_NA,
        AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "corrtype":
        return corrtype;
      case "phase":
        return phase;
      case "rootphase":
        return rootphase;
      case "bandcode":
        return bandcode;
      case "phasecode":
        return phasecode;
      case "rootchan":
        return rootchan;
      case "type":
        return type;
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
      case "corrtype":
        corrtype = value;
        break;
      case "phase":
        phase = value;
        break;
      case "rootphase":
        rootphase = value;
        break;
      case "bandcode":
        bandcode = value;
        break;
      case "phasecode":
        phasecode = value;
        break;
      case "rootchan":
        rootchan = value;
        break;
      case "type":
        type = value;
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
      case "slowness":
        return slowness;
      case "samprate":
        return samprate;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "slowness":
        slowness = value;
        break;
      case "samprate":
        samprate = value;
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
      case "bwfilterid":
        return bwfilterid;
      case "smooid":
        return smooid;
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
      case "magyieldid":
        magyieldid = value;
        break;
      case "bwfilterid":
        bwfilterid = value;
        break;
      case "smooid":
        smooid = value;
        break;
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
  public Nb_env(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Nb_env(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readDouble(),
        readString(input), input.readDouble(), readString(input), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Nb_env(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getDouble(),
        readString(input), input.getDouble(), readString(input), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Nb_env(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Nb_env(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getDouble(offset + 5), input.getString(offset + 6),
        input.getDouble(offset + 7), input.getString(offset + 8), input.getString(offset + 9),
        input.getString(offset + 10), input.getString(offset + 11), input.getString(offset + 12),
        input.getString(offset + 13), input.getString(offset + 14), input.getLong(offset + 15));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[15];
    values[0] = magyieldid;
    values[1] = bwfilterid;
    values[2] = smooid;
    values[3] = polyid;
    values[4] = slowness;
    values[5] = corrtype;
    values[6] = samprate;
    values[7] = phase;
    values[8] = rootphase;
    values[9] = bandcode;
    values[10] = phasecode;
    values[11] = rootchan;
    values[12] = type;
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
    values[0] = magyieldid;
    values[1] = bwfilterid;
    values[2] = smooid;
    values[3] = polyid;
    values[4] = slowness;
    values[5] = corrtype;
    values[6] = samprate;
    values[7] = phase;
    values[8] = rootphase;
    values[9] = bandcode;
    values[10] = phasecode;
    values[11] = rootchan;
    values[12] = type;
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
    output.writeLong(magyieldid);
    output.writeLong(bwfilterid);
    output.writeLong(smooid);
    output.writeLong(polyid);
    output.writeDouble(slowness);
    writeString(output, corrtype);
    output.writeDouble(samprate);
    writeString(output, phase);
    writeString(output, rootphase);
    writeString(output, bandcode);
    writeString(output, phasecode);
    writeString(output, rootchan);
    writeString(output, type);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(magyieldid);
    output.putLong(bwfilterid);
    output.putLong(smooid);
    output.putLong(polyid);
    output.putDouble(slowness);
    writeString(output, corrtype);
    output.putDouble(samprate);
    writeString(output, phase);
    writeString(output, rootphase);
    writeString(output, bandcode);
    writeString(output, phasecode);
    writeString(output, rootchan);
    writeString(output, type);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Nb_env objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Nb_env objects.
   * @throws IOException
   */
  static public void readNb_envs(BufferedReader input, Collection<Nb_env> rows) throws IOException {
    String[] saved = Nb_env.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Nb_env.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Nb_env(new Scanner(line)));
    }
    input.close();
    Nb_env.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Nb_env objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Nb_env objects.
   * @throws IOException
   */
  static public void readNb_envs(File inputFile, Collection<Nb_env> rows) throws IOException {
    readNb_envs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Nb_env objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Nb_env objects.
   * @throws IOException
   */
  static public void readNb_envs(InputStream inputStream, Collection<Nb_env> rows)
      throws IOException {
    readNb_envs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Nb_env objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Nb_env objects
   * @throws IOException
   */
  static public Set<Nb_env> readNb_envs(BufferedReader input) throws IOException {
    Set<Nb_env> rows = new LinkedHashSet<Nb_env>();
    readNb_envs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Nb_env objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Nb_env objects
   * @throws IOException
   */
  static public Set<Nb_env> readNb_envs(File inputFile) throws IOException {
    return readNb_envs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Nb_env objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Nb_env objects
   * @throws IOException
   */
  static public Set<Nb_env> readNb_envs(InputStream input) throws IOException {
    return readNb_envs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Nb_env objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param nb_envs the Nb_env objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Nb_env> nb_envs) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Nb_env nb_env : nb_envs)
      nb_env.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Nb_env objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param nb_envs the Nb_env objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Nb_env> nb_envs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Nb_env nb_env : nb_envs) {
        int i = 0;
        statement.setLong(++i, nb_env.magyieldid);
        statement.setLong(++i, nb_env.bwfilterid);
        statement.setLong(++i, nb_env.smooid);
        statement.setLong(++i, nb_env.polyid);
        statement.setDouble(++i, nb_env.slowness);
        statement.setString(++i, nb_env.corrtype);
        statement.setDouble(++i, nb_env.samprate);
        statement.setString(++i, nb_env.phase);
        statement.setString(++i, nb_env.rootphase);
        statement.setString(++i, nb_env.bandcode);
        statement.setString(++i, nb_env.phasecode);
        statement.setString(++i, nb_env.rootchan);
        statement.setString(++i, nb_env.type);
        statement.setString(++i, nb_env.auth);
        statement.setLong(++i, nb_env.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Nb_env
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Nb_env> readNb_envs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Nb_env> results = new HashSet<Nb_env>();
    readNb_envs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Nb_env
   *        table.
   * @param nb_envs
   * @throws SQLException
   */
  static public void readNb_envs(Connection connection, String selectStatement, Set<Nb_env> nb_envs)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        nb_envs.add(new Nb_env(rs));
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
   * this Nb_env object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Nb_env object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "magyieldid, bwfilterid, smooid, polyid, slowness, corrtype, samprate, phase, rootphase, bandcode, phasecode, rootchan, type, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(magyieldid)).append(", ");
    sql.append(Long.toString(bwfilterid)).append(", ");
    sql.append(Long.toString(smooid)).append(", ");
    sql.append(Long.toString(polyid)).append(", ");
    sql.append(Double.toString(slowness)).append(", ");
    sql.append("'").append(corrtype).append("', ");
    sql.append(Double.toString(samprate)).append(", ");
    sql.append("'").append(phase).append("', ");
    sql.append("'").append(rootphase).append("', ");
    sql.append("'").append(bandcode).append("', ");
    sql.append("'").append(phasecode).append("', ");
    sql.append("'").append(rootchan).append("', ");
    sql.append("'").append(type).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Nb_env in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Nb_env in the database
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
   * Generate a sql script to create a table of type Nb_env in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Nb_env in the database
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
    buf.append("bwfilterid   number(9)            NOT NULL,\n");
    buf.append("smooid       number(9)            NOT NULL,\n");
    buf.append("polyid       number(9)            NOT NULL,\n");
    buf.append("slowness     float(24)            NOT NULL,\n");
    buf.append("corrtype     varchar2(8)          NOT NULL,\n");
    buf.append("samprate     float(24)            NOT NULL,\n");
    buf.append("phase        varchar2(8)          NOT NULL,\n");
    buf.append("rootphase    varchar2(8)          NOT NULL,\n");
    buf.append("bandcode     varchar2(1)          NOT NULL,\n");
    buf.append("phasecode    varchar2(1)          NOT NULL,\n");
    buf.append("rootchan     varchar2(8)          NOT NULL,\n");
    buf.append("type         varchar2(6)          NOT NULL,\n");
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
    return 148;
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
    return (other instanceof Nb_env) && ((Nb_env) other).magyieldid == magyieldid;
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
  public Nb_env setMagyieldid(long magyieldid) {
    if (magyieldid >= 1000000000L)
      throw new IllegalArgumentException(
          "magyieldid=" + magyieldid + " but cannot be >= 1000000000");
    this.magyieldid = magyieldid;
    setHash(null);
    return this;
  }

  /**
   * Butterworth filter identifier
   * 
   * @return bwfilterid
   */
  public long getBwfilterid() {
    return bwfilterid;
  }

  /**
   * Butterworth filter identifier
   * 
   * @param bwfilterid
   * @throws IllegalArgumentException if bwfilterid >= 1000000000
   */
  public Nb_env setBwfilterid(long bwfilterid) {
    if (bwfilterid >= 1000000000L)
      throw new IllegalArgumentException(
          "bwfilterid=" + bwfilterid + " but cannot be >= 1000000000");
    this.bwfilterid = bwfilterid;
    setHash(null);
    return this;
  }

  /**
   * Boxcar smoothing identfier.
   * 
   * @return smooid
   */
  public long getSmooid() {
    return smooid;
  }

  /**
   * Boxcar smoothing identfier.
   * 
   * @param smooid
   * @throws IllegalArgumentException if smooid >= 1000000000
   */
  public Nb_env setSmooid(long smooid) {
    if (smooid >= 1000000000L)
      throw new IllegalArgumentException("smooid=" + smooid + " but cannot be >= 1000000000");
    this.smooid = smooid;
    setHash(null);
    return this;
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
  public Nb_env setPolyid(long polyid) {
    if (polyid >= 1000000000L)
      throw new IllegalArgumentException("polyid=" + polyid + " but cannot be >= 1000000000");
    this.polyid = polyid;
    setHash(null);
    return this;
  }

  /**
   * The stack slowness used in combining the elements of an array to create a resultant narrowband
   * envelope.
   * <p>
   * Units: s/km
   * 
   * @return slowness
   */
  public double getSlowness() {
    return slowness;
  }

  /**
   * The stack slowness used in combining the elements of an array to create a resultant narrowband
   * envelope.
   * <p>
   * Units: s/km
   * 
   * @param slowness
   */
  public Nb_env setSlowness(double slowness) {
    this.slowness = slowness;
    setHash(null);
    return this;
  }

  /**
   * Path correction type (none, 1D, or 2D).
   * 
   * @return corrtype
   */
  public String getCorrtype() {
    return corrtype;
  }

  /**
   * Path correction type (none, 1D, or 2D).
   * 
   * @param corrtype
   * @throws IllegalArgumentException if corrtype.length() >= 8
   */
  public Nb_env setCorrtype(String corrtype) {
    if (corrtype.length() > 8)
      throw new IllegalArgumentException(
          String.format("corrtype.length() cannot be > 8.  corrtype=%s", corrtype));
    this.corrtype = corrtype;
    setHash(null);
    return this;
  }

  /**
   * Sampling rate. This column is the sample rate in samples per second. In the <B>instrument</B>
   * table, the column value is specifically the nominal sample rate, not accounting for clock
   * drift. In <B>wfdisc</B>, the value may vary slightly from the nominal to reflect clock drift.
   * <p>
   * Units: 1/s
   * 
   * @return samprate
   */
  public double getSamprate() {
    return samprate;
  }

  /**
   * Sampling rate. This column is the sample rate in samples per second. In the <B>instrument</B>
   * table, the column value is specifically the nominal sample rate, not accounting for clock
   * drift. In <B>wfdisc</B>, the value may vary slightly from the nominal to reflect clock drift.
   * <p>
   * Units: 1/s
   * 
   * @param samprate
   */
  public Nb_env setSamprate(double samprate) {
    this.samprate = samprate;
    setHash(null);
    return this;
  }

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   * 
   * @return phase
   */
  public String getPhase() {
    return phase;
  }

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   * 
   * @param phase
   * @throws IllegalArgumentException if phase.length() >= 8
   */
  public Nb_env setPhase(String phase) {
    if (phase.length() > 8)
      throw new IllegalArgumentException(
          String.format("phase.length() cannot be > 8.  phase=%s", phase));
    this.phase = phase;
    setHash(null);
    return this;
  }

  /**
   * The <I>phase</I> column has the format phase:type. <I>rootphase</I> is the standard direct
   * phase name portion of this (i.e. precedes the ':'). Following the colon would be indication of
   * code or direct portion of the signal and the code for the type of measurement. See <I>type</I>.
   * 
   * @return rootphase
   */
  public String getRootphase() {
    return rootphase;
  }

  /**
   * The <I>phase</I> column has the format phase:type. <I>rootphase</I> is the standard direct
   * phase name portion of this (i.e. precedes the ':'). Following the colon would be indication of
   * code or direct portion of the signal and the code for the type of measurement. See <I>type</I>.
   * 
   * @param rootphase
   * @throws IllegalArgumentException if rootphase.length() >= 8
   */
  public Nb_env setRootphase(String rootphase) {
    if (rootphase.length() > 8)
      throw new IllegalArgumentException(
          String.format("rootphase.length() cannot be > 8.  rootphase=%s", rootphase));
    this.rootphase = rootphase;
    setHash(null);
    return this;
  }

  /**
   * This is the code (a-z) indexing the narrowband corresponding to this record. <I>bandcode</I> is
   * part of the complex, composite <I>chan</I> name in the corresponding <B>sitechan</B> record.
   * 
   * @return bandcode
   */
  public String getBandcode() {
    return bandcode;
  }

  /**
   * This is the code (a-z) indexing the narrowband corresponding to this record. <I>bandcode</I> is
   * part of the complex, composite <I>chan</I> name in the corresponding <B>sitechan</B> record.
   * 
   * @param bandcode
   * @throws IllegalArgumentException if bandcode.length() >= 1
   */
  public Nb_env setBandcode(String bandcode) {
    if (bandcode.length() > 1)
      throw new IllegalArgumentException(
          String.format("bandcode.length() cannot be > 1.  bandcode=%s", bandcode));
    this.bandcode = bandcode;
    setHash(null);
    return this;
  }

  /**
   * This is the second to last character in the <I>chan</I> column in the corresponding
   * <B>sitechan</B> record and corresponds to the phase that is given in the <I>phase</I> column.
   * Lowercase codes represent 1D corrections for the phase, and uppercase codes represent 2D
   * corrections for the phase where available.
   * 
   * @return phasecode
   */
  public String getPhasecode() {
    return phasecode;
  }

  /**
   * This is the second to last character in the <I>chan</I> column in the corresponding
   * <B>sitechan</B> record and corresponds to the phase that is given in the <I>phase</I> column.
   * Lowercase codes represent 1D corrections for the phase, and uppercase codes represent 2D
   * corrections for the phase where available.
   * 
   * @param phasecode
   * @throws IllegalArgumentException if phasecode.length() >= 1
   */
  public Nb_env setPhasecode(String phasecode) {
    if (phasecode.length() > 1)
      throw new IllegalArgumentException(
          String.format("phasecode.length() cannot be > 1.  phasecode=%s", phasecode));
    this.phasecode = phasecode;
    setHash(null);
    return this;
  }

  /**
   * This is the channel from <B>sitechan</B> minus the leading 'e' and the band and phase codes.
   * 
   * @return rootchan
   */
  public String getRootchan() {
    return rootchan;
  }

  /**
   * This is the channel from <B>sitechan</B> minus the leading 'e' and the band and phase codes.
   * 
   * @param rootchan
   * @throws IllegalArgumentException if rootchan.length() >= 8
   */
  public Nb_env setRootchan(String rootchan) {
    if (rootchan.length() > 8)
      throw new IllegalArgumentException(
          String.format("rootchan.length() cannot be > 8.  rootchan=%s", rootchan));
    this.rootchan = rootchan;
    setHash(null);
    return this;
  }

  /**
   * The portion of <I>phase</I> after the ':'. Indicates coda or direct wave and the measurement
   * type (e.g. c1=coda, time window set 1). See <I>rootphase</I>.
   * 
   * @return type
   */
  public String getType() {
    return type;
  }

  /**
   * The portion of <I>phase</I> after the ':'. Indicates coda or direct wave and the measurement
   * type (e.g. c1=coda, time window set 1). See <I>rootphase</I>.
   * 
   * @param type
   * @throws IllegalArgumentException if type.length() >= 6
   */
  public Nb_env setType(String type) {
    if (type.length() > 6)
      throw new IllegalArgumentException(
          String.format("type.length() cannot be > 6.  type=%s", type));
    this.type = type;
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
  public Nb_env setAuth(String auth) {
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
  public Nb_env setCommid(long commid) {
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
