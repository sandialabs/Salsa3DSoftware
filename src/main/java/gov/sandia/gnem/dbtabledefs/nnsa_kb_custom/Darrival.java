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
 * darrival
 */
public class Darrival extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Differential arrival identifier
   */
  private long darid;

  static final public long DARID_NA = Long.MIN_VALUE;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   */
  private long evid;

  static final public long EVID_NA = -1;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * The differential time is the time between the phases for a differential arrival. If the dphase
   * is <I>S-P</I>, then the differential time is the time of the S arrival minus that of the P
   * arrival for a single event at a single station.
   * <p>
   * Units: s
   */
  private double dtime;

  static final public double DTIME_NA = Double.NaN;

  /**
   * The differential phase code designates the phases that apply to a reported differential
   * arrival. For example, <I>S-P ,</I> means that the properties reported are for the interval
   * between the P and S phases; ie: the time would be the time of the S arrival minus that of the P
   * arrival for a single event at a single station.
   */
  private String dphase;

  static final public String DPHASE_NA = null;

  /**
   * Source-receiver distance. Calculated using origin specified by <I>orid</I>
   * (<B>nnsa_amp_descript</B>).
   * <p>
   * Units: degree
   */
  private double delta;

  static final public double DELTA_NA = -1;

  /**
   * Velocity model. This character string identifies the velocity model of the Earth used to
   * compute the travel times of seismic phases. A velocity model is required for event location (if
   * phase is defining) or for computing travel-time residuals.
   */
  private String vmodel;

  static final public String VMODEL_NA = "-";

  /**
   * Unspecified amplitude associated with a differential arrival.
   * <p>
   * Units: nm (time domain), nm/Hz (frequency domain) - see units in table
   */
  private double darrival_amp;

  static final public double DARRIVAL_AMP_NA = -999;

  /**
   * Measured period at the time of the amplitude measurement
   * <p>
   * Units: s
   */
  private double per;

  static final public double PER_NA = -1;

  /**
   * Log of amplitude divided by period. This measurement of signal size is often reported instead
   * of the amplitude and period separately. This column is only filled if the separate measurements
   * are not available.
   * <p>
   * Units: log10(nm/s)
   */
  private double logat;

  static final public double LOGAT_NA = -999;

  /**
   * Onset quality. This single-character flag is used to denote the sharpness of the onset of a
   * seismic phase. This relates to the timing accuracy as follows: i (impulsive) - accurate to
   * +/-0.2 seconds e (emergent) - accuracy between +/-(0.2 to 1.0 seconds) w (weak) - timing
   * uncertain to > 1 second 1 - power difference between first and second FK peak > 6dB 2 - power
   * difference between first and second FK peak 4-6 dB 3 - power difference between first and
   * second FK peak 2-4 dB 4 - power difference between first and second FK peak 0-2 dB A - error
   * bounds <0.5s (P,Pn); <2s (S,Sn); <4s (Lg) B - error bounds 0.5-1s (P,Pn); 2-4s (S,Sn) C - error
   * bounds 1-3s (P,Pn); 4-6s (S,Sn); 4-8s (Lg) D - error bounds 3-5s (P,Pn); 6-8s (S,Sn); 8-12s
   * (Lg) F - error bounds do not fall within any other error bounds q - questionable arrival
   * 
   */
  private String qual;

  static final public String QUAL_NA = "-";

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
    columns.add("darid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("evid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("dtime", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("dphase", Columns.FieldType.STRING, "%s");
    columns.add("delta", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("vmodel", Columns.FieldType.STRING, "%s");
    columns.add("darrival_amp", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("per", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("logat", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("qual", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Darrival(long darid, long orid, long evid, String sta, double dtime, String dphase,
      double delta, String vmodel, double darrival_amp, double per, double logat, String qual,
      String auth, long commid) {
    setValues(darid, orid, evid, sta, dtime, dphase, delta, vmodel, darrival_amp, per, logat, qual,
        auth, commid);
  }

  private void setValues(long darid, long orid, long evid, String sta, double dtime, String dphase,
      double delta, String vmodel, double darrival_amp, double per, double logat, String qual,
      String auth, long commid) {
    this.darid = darid;
    this.orid = orid;
    this.evid = evid;
    this.sta = sta;
    this.dtime = dtime;
    this.dphase = dphase;
    this.delta = delta;
    this.vmodel = vmodel;
    this.darrival_amp = darrival_amp;
    this.per = per;
    this.logat = logat;
    this.qual = qual;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Darrival(Darrival other) {
    this.darid = other.getDarid();
    this.orid = other.getOrid();
    this.evid = other.getEvid();
    this.sta = other.getSta();
    this.dtime = other.getDtime();
    this.dphase = other.getDphase();
    this.delta = other.getDelta();
    this.vmodel = other.getVmodel();
    this.darrival_amp = other.getDarrival_amp();
    this.per = other.getPer();
    this.logat = other.getLogat();
    this.qual = other.getQual();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Darrival() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(DARID_NA, ORID_NA, EVID_NA, STA_NA, DTIME_NA, DPHASE_NA, DELTA_NA, VMODEL_NA,
        DARRIVAL_AMP_NA, PER_NA, LOGAT_NA, QUAL_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "dphase":
        return dphase;
      case "vmodel":
        return vmodel;
      case "qual":
        return qual;
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
      case "dphase":
        dphase = value;
        break;
      case "vmodel":
        vmodel = value;
        break;
      case "qual":
        qual = value;
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
      case "dtime":
        return dtime;
      case "delta":
        return delta;
      case "darrival_amp":
        return darrival_amp;
      case "per":
        return per;
      case "logat":
        return logat;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "dtime":
        dtime = value;
        break;
      case "delta":
        delta = value;
        break;
      case "darrival_amp":
        darrival_amp = value;
        break;
      case "per":
        per = value;
        break;
      case "logat":
        logat = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "darid":
        return darid;
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
      case "darid":
        darid = value;
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
  public Darrival(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Darrival(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), readString(input),
        input.readDouble(), readString(input), input.readDouble(), readString(input),
        input.readDouble(), input.readDouble(), input.readDouble(), readString(input),
        readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Darrival(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), readString(input), input.getDouble(),
        readString(input), input.getDouble(), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), readString(input), readString(input),
        input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Darrival(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Darrival(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getString(offset + 4), input.getDouble(offset + 5), input.getString(offset + 6),
        input.getDouble(offset + 7), input.getString(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getString(offset + 12),
        input.getString(offset + 13), input.getLong(offset + 14));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[14];
    values[0] = darid;
    values[1] = orid;
    values[2] = evid;
    values[3] = sta;
    values[4] = dtime;
    values[5] = dphase;
    values[6] = delta;
    values[7] = vmodel;
    values[8] = darrival_amp;
    values[9] = per;
    values[10] = logat;
    values[11] = qual;
    values[12] = auth;
    values[13] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[15];
    values[0] = darid;
    values[1] = orid;
    values[2] = evid;
    values[3] = sta;
    values[4] = dtime;
    values[5] = dphase;
    values[6] = delta;
    values[7] = vmodel;
    values[8] = darrival_amp;
    values[9] = per;
    values[10] = logat;
    values[11] = qual;
    values[12] = auth;
    values[13] = commid;
    values[14] = lddate;
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
    output.writeLong(darid);
    output.writeLong(orid);
    output.writeLong(evid);
    writeString(output, sta);
    output.writeDouble(dtime);
    writeString(output, dphase);
    output.writeDouble(delta);
    writeString(output, vmodel);
    output.writeDouble(darrival_amp);
    output.writeDouble(per);
    output.writeDouble(logat);
    writeString(output, qual);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(darid);
    output.putLong(orid);
    output.putLong(evid);
    writeString(output, sta);
    output.putDouble(dtime);
    writeString(output, dphase);
    output.putDouble(delta);
    writeString(output, vmodel);
    output.putDouble(darrival_amp);
    output.putDouble(per);
    output.putDouble(logat);
    writeString(output, qual);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Darrival objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Darrival objects.
   * @throws IOException
   */
  static public void readDarrivals(BufferedReader input, Collection<Darrival> rows)
      throws IOException {
    String[] saved = Darrival.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Darrival
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Darrival(new Scanner(line)));
    }
    input.close();
    Darrival.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Darrival objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Darrival objects.
   * @throws IOException
   */
  static public void readDarrivals(File inputFile, Collection<Darrival> rows) throws IOException {
    readDarrivals(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Darrival objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Darrival objects.
   * @throws IOException
   */
  static public void readDarrivals(InputStream inputStream, Collection<Darrival> rows)
      throws IOException {
    readDarrivals(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Darrival objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Darrival objects
   * @throws IOException
   */
  static public Set<Darrival> readDarrivals(BufferedReader input) throws IOException {
    Set<Darrival> rows = new LinkedHashSet<Darrival>();
    readDarrivals(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Darrival objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Darrival objects
   * @throws IOException
   */
  static public Set<Darrival> readDarrivals(File inputFile) throws IOException {
    return readDarrivals(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Darrival objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Darrival objects
   * @throws IOException
   */
  static public Set<Darrival> readDarrivals(InputStream input) throws IOException {
    return readDarrivals(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Darrival objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param darrivals the Darrival objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Darrival> darrivals)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Darrival darrival : darrivals)
      darrival.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Darrival objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param darrivals the Darrival objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Darrival> darrivals, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Darrival darrival : darrivals) {
        int i = 0;
        statement.setLong(++i, darrival.darid);
        statement.setLong(++i, darrival.orid);
        statement.setLong(++i, darrival.evid);
        statement.setString(++i, darrival.sta);
        statement.setDouble(++i, darrival.dtime);
        statement.setString(++i, darrival.dphase);
        statement.setDouble(++i, darrival.delta);
        statement.setString(++i, darrival.vmodel);
        statement.setDouble(++i, darrival.darrival_amp);
        statement.setDouble(++i, darrival.per);
        statement.setDouble(++i, darrival.logat);
        statement.setString(++i, darrival.qual);
        statement.setString(++i, darrival.auth);
        statement.setLong(++i, darrival.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Darrival
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Darrival> readDarrivals(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Darrival> results = new HashSet<Darrival>();
    readDarrivals(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Darrival
   *        table.
   * @param darrivals
   * @throws SQLException
   */
  static public void readDarrivals(Connection connection, String selectStatement,
      Set<Darrival> darrivals) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        darrivals.add(new Darrival(rs));
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
   * this Darrival object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Darrival object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "darid, orid, evid, sta, dtime, dphase, delta, vmodel, darrival_amp, per, logat, qual, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(darid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(evid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append(Double.toString(dtime)).append(", ");
    sql.append("'").append(dphase).append("', ");
    sql.append(Double.toString(delta)).append(", ");
    sql.append("'").append(vmodel).append("', ");
    sql.append(Double.toString(darrival_amp)).append(", ");
    sql.append(Double.toString(per)).append(", ");
    sql.append(Double.toString(logat)).append(", ");
    sql.append("'").append(qual).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Darrival in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Darrival in the database
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
   * Generate a sql script to create a table of type Darrival in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Darrival in the database
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
    buf.append("darid        number(9)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("evid         number(9)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("dtime        float(53)            NOT NULL,\n");
    buf.append("dphase       varchar2(8)          NOT NULL,\n");
    buf.append("delta        float(24)            NOT NULL,\n");
    buf.append("vmodel       varchar2(15)         NOT NULL,\n");
    buf.append("darrival_amp float(24)            NOT NULL,\n");
    buf.append("per          float(24)            NOT NULL,\n");
    buf.append("logat        float(24)            NOT NULL,\n");
    buf.append("qual         varchar2(1)          NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (darid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (orid,sta,dtime,dphase)");
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
    return 142;
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
    return (other instanceof Darrival) && ((Darrival) other).darid == darid;
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
    return (other instanceof Darrival) && ((Darrival) other).orid == orid
        && ((Darrival) other).sta.equals(sta) && ((Darrival) other).dtime == dtime
        && ((Darrival) other).dphase.equals(dphase);
  }

  /**
   * Differential arrival identifier
   * 
   * @return darid
   */
  public long getDarid() {
    return darid;
  }

  /**
   * Differential arrival identifier
   * 
   * @param darid
   * @throws IllegalArgumentException if darid >= 1000000000
   */
  public Darrival setDarid(long darid) {
    if (darid >= 1000000000L)
      throw new IllegalArgumentException("darid=" + darid + " but cannot be >= 1000000000");
    this.darid = darid;
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
  public Darrival setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
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
  public Darrival setEvid(long evid) {
    if (evid >= 1000000000L)
      throw new IllegalArgumentException("evid=" + evid + " but cannot be >= 1000000000");
    this.evid = evid;
    setHash(null);
    return this;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   * 
   * @return sta
   */
  public String getSta() {
    return sta;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   * 
   * @param sta
   * @throws IllegalArgumentException if sta.length() >= 6
   */
  public Darrival setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * The differential time is the time between the phases for a differential arrival. If the dphase
   * is <I>S-P</I>, then the differential time is the time of the S arrival minus that of the P
   * arrival for a single event at a single station.
   * <p>
   * Units: s
   * 
   * @return dtime
   */
  public double getDtime() {
    return dtime;
  }

  /**
   * The differential time is the time between the phases for a differential arrival. If the dphase
   * is <I>S-P</I>, then the differential time is the time of the S arrival minus that of the P
   * arrival for a single event at a single station.
   * <p>
   * Units: s
   * 
   * @param dtime
   */
  public Darrival setDtime(double dtime) {
    this.dtime = dtime;
    setHash(null);
    return this;
  }

  /**
   * The differential phase code designates the phases that apply to a reported differential
   * arrival. For example, <I>S-P ,</I> means that the properties reported are for the interval
   * between the P and S phases; ie: the time would be the time of the S arrival minus that of the P
   * arrival for a single event at a single station.
   * 
   * @return dphase
   */
  public String getDphase() {
    return dphase;
  }

  /**
   * The differential phase code designates the phases that apply to a reported differential
   * arrival. For example, <I>S-P ,</I> means that the properties reported are for the interval
   * between the P and S phases; ie: the time would be the time of the S arrival minus that of the P
   * arrival for a single event at a single station.
   * 
   * @param dphase
   * @throws IllegalArgumentException if dphase.length() >= 8
   */
  public Darrival setDphase(String dphase) {
    if (dphase.length() > 8)
      throw new IllegalArgumentException(
          String.format("dphase.length() cannot be > 8.  dphase=%s", dphase));
    this.dphase = dphase;
    setHash(null);
    return this;
  }

  /**
   * Source-receiver distance. Calculated using origin specified by <I>orid</I>
   * (<B>nnsa_amp_descript</B>).
   * <p>
   * Units: degree
   * 
   * @return delta
   */
  public double getDelta() {
    return delta;
  }

  /**
   * Source-receiver distance. Calculated using origin specified by <I>orid</I>
   * (<B>nnsa_amp_descript</B>).
   * <p>
   * Units: degree
   * 
   * @param delta
   */
  public Darrival setDelta(double delta) {
    this.delta = delta;
    setHash(null);
    return this;
  }

  /**
   * Velocity model. This character string identifies the velocity model of the Earth used to
   * compute the travel times of seismic phases. A velocity model is required for event location (if
   * phase is defining) or for computing travel-time residuals.
   * 
   * @return vmodel
   */
  public String getVmodel() {
    return vmodel;
  }

  /**
   * Velocity model. This character string identifies the velocity model of the Earth used to
   * compute the travel times of seismic phases. A velocity model is required for event location (if
   * phase is defining) or for computing travel-time residuals.
   * 
   * @param vmodel
   * @throws IllegalArgumentException if vmodel.length() >= 15
   */
  public Darrival setVmodel(String vmodel) {
    if (vmodel.length() > 15)
      throw new IllegalArgumentException(
          String.format("vmodel.length() cannot be > 15.  vmodel=%s", vmodel));
    this.vmodel = vmodel;
    setHash(null);
    return this;
  }

  /**
   * Unspecified amplitude associated with a differential arrival.
   * <p>
   * Units: nm (time domain), nm/Hz (frequency domain) - see units in table
   * 
   * @return darrival_amp
   */
  public double getDarrival_amp() {
    return darrival_amp;
  }

  /**
   * Unspecified amplitude associated with a differential arrival.
   * <p>
   * Units: nm (time domain), nm/Hz (frequency domain) - see units in table
   * 
   * @param darrival_amp
   */
  public Darrival setDarrival_amp(double darrival_amp) {
    this.darrival_amp = darrival_amp;
    setHash(null);
    return this;
  }

  /**
   * Measured period at the time of the amplitude measurement
   * <p>
   * Units: s
   * 
   * @return per
   */
  public double getPer() {
    return per;
  }

  /**
   * Measured period at the time of the amplitude measurement
   * <p>
   * Units: s
   * 
   * @param per
   */
  public Darrival setPer(double per) {
    this.per = per;
    setHash(null);
    return this;
  }

  /**
   * Log of amplitude divided by period. This measurement of signal size is often reported instead
   * of the amplitude and period separately. This column is only filled if the separate measurements
   * are not available.
   * <p>
   * Units: log10(nm/s)
   * 
   * @return logat
   */
  public double getLogat() {
    return logat;
  }

  /**
   * Log of amplitude divided by period. This measurement of signal size is often reported instead
   * of the amplitude and period separately. This column is only filled if the separate measurements
   * are not available.
   * <p>
   * Units: log10(nm/s)
   * 
   * @param logat
   */
  public Darrival setLogat(double logat) {
    this.logat = logat;
    setHash(null);
    return this;
  }

  /**
   * Onset quality. This single-character flag is used to denote the sharpness of the onset of a
   * seismic phase. This relates to the timing accuracy as follows: i (impulsive) - accurate to
   * +/-0.2 seconds e (emergent) - accuracy between +/-(0.2 to 1.0 seconds) w (weak) - timing
   * uncertain to > 1 second 1 - power difference between first and second FK peak > 6dB 2 - power
   * difference between first and second FK peak 4-6 dB 3 - power difference between first and
   * second FK peak 2-4 dB 4 - power difference between first and second FK peak 0-2 dB A - error
   * bounds <0.5s (P,Pn); <2s (S,Sn); <4s (Lg) B - error bounds 0.5-1s (P,Pn); 2-4s (S,Sn) C - error
   * bounds 1-3s (P,Pn); 4-6s (S,Sn); 4-8s (Lg) D - error bounds 3-5s (P,Pn); 6-8s (S,Sn); 8-12s
   * (Lg) F - error bounds do not fall within any other error bounds q - questionable arrival
   * 
   * @return qual
   */
  public String getQual() {
    return qual;
  }

  /**
   * Onset quality. This single-character flag is used to denote the sharpness of the onset of a
   * seismic phase. This relates to the timing accuracy as follows: i (impulsive) - accurate to
   * +/-0.2 seconds e (emergent) - accuracy between +/-(0.2 to 1.0 seconds) w (weak) - timing
   * uncertain to > 1 second 1 - power difference between first and second FK peak > 6dB 2 - power
   * difference between first and second FK peak 4-6 dB 3 - power difference between first and
   * second FK peak 2-4 dB 4 - power difference between first and second FK peak 0-2 dB A - error
   * bounds <0.5s (P,Pn); <2s (S,Sn); <4s (Lg) B - error bounds 0.5-1s (P,Pn); 2-4s (S,Sn) C - error
   * bounds 1-3s (P,Pn); 4-6s (S,Sn); 4-8s (Lg) D - error bounds 3-5s (P,Pn); 6-8s (S,Sn); 8-12s
   * (Lg) F - error bounds do not fall within any other error bounds q - questionable arrival
   * 
   * @param qual
   * @throws IllegalArgumentException if qual.length() >= 1
   */
  public Darrival setQual(String qual) {
    if (qual.length() > 1)
      throw new IllegalArgumentException(
          String.format("qual.length() cannot be > 1.  qual=%s", qual));
    this.qual = qual;
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
  public Darrival setAuth(String auth) {
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
  public Darrival setCommid(long commid) {
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
