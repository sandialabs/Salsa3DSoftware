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
 * nnsa_amp_descript
 */
public class Nnsa_amp_descript extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   */
  private long windowid;

  static final public long WINDOWID_NA = Long.MIN_VALUE;

  /**
   * Parameter set identification number used when making raw amplitude measurements.
   */
  private long paramsetid;

  static final public long PARAMSETID_NA = -1;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   */
  private String chan;

  static final public String CHAN_NA = null;

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   */
  private String phase;

  static final public String PHASE_NA = null;

  /**
   * Source-receiver distance. Calculated using origin specified by <I>orid</I>
   * (<B>nnsa_amp_descript</B>).
   * <p>
   * Units: degree
   */
  private double delta;

  static final public double DELTA_NA = Double.NaN;

  /**
   * Station-to-event azimuth calculated from the station and event locations and measured clockwise
   * from north.
   * <p>
   * Units: degree
   */
  private double seaz;

  static final public double SEAZ_NA = Double.NaN;

  /**
   * Event depth
   * <p>
   * Units: km
   */
  private double depth;

  static final public double DEPTH_NA = -999;

  /**
   * Low group velocity used to isolate phase or noise window.
   * <p>
   * Units: km/s
   */
  private double gvlo;

  static final public double GVLO_NA = -1;

  /**
   * High group velocity used to isolate phase or noise window.
   * <p>
   * Units: km/s
   */
  private double gvhi;

  static final public double GVHI_NA = -1;

  /**
   * Time offset used of Pn arrival from pre-specified group velocity in seconds. Used as an origin
   * offset correction for other phases. (<B>discrim_data</B>). Time offset relative to a
   * theoretical arrival time (<B>nnsa_amp_descript</B>)
   * <p>
   * Units: s
   */
  private double toff;

  static final public double TOFF_NA = -999;

  /**
   * Absolute begin time of window used to measure the amplitude.
   * <p>
   * Units: s
   */
  private double start_time;

  static final public double START_TIME_NA = Double.NaN;

  /**
   * Duration of window for amplitude measurement. Combined with <I>start_time</I>, the entire
   * amplitude time window is specified.
   * <p>
   * Units: s
   */
  private double duration;

  static final public double DURATION_NA = Double.NaN;

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

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Waveform identifier of the seismogram from which measurement was made. See <B>wfdisc</B>.
   */
  private long wfid;

  static final public long WFID_NA = -1;

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta</I>, <I>chan</I> and <I>time</I>.
   */
  private long arid;

  static final public long ARID_NA = -1;

  /**
   * Algorithm identifier. Identifies algorithm(s) used to measure data.
   */
  private long algoid;

  static final public long ALGOID_NA = -1;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("windowid", Columns.FieldType.LONG, "%d");
    columns.add("paramsetid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("phase", Columns.FieldType.STRING, "%s");
    columns.add("delta", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("seaz", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("depth", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("gvlo", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("gvhi", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("toff", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("start_time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("duration", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("evid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("wfid", Columns.FieldType.LONG, "%d");
    columns.add("arid", Columns.FieldType.LONG, "%d");
    columns.add("algoid", Columns.FieldType.LONG, "%d");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Nnsa_amp_descript(long windowid, long paramsetid, String sta, String chan, String phase,
      double delta, double seaz, double depth, double gvlo, double gvhi, double toff,
      double start_time, double duration, long evid, long orid, long wfid, long arid, long algoid,
      String auth) {
    setValues(windowid, paramsetid, sta, chan, phase, delta, seaz, depth, gvlo, gvhi, toff,
        start_time, duration, evid, orid, wfid, arid, algoid, auth);
  }

  private void setValues(long windowid, long paramsetid, String sta, String chan, String phase,
      double delta, double seaz, double depth, double gvlo, double gvhi, double toff,
      double start_time, double duration, long evid, long orid, long wfid, long arid, long algoid,
      String auth) {
    this.windowid = windowid;
    this.paramsetid = paramsetid;
    this.sta = sta;
    this.chan = chan;
    this.phase = phase;
    this.delta = delta;
    this.seaz = seaz;
    this.depth = depth;
    this.gvlo = gvlo;
    this.gvhi = gvhi;
    this.toff = toff;
    this.start_time = start_time;
    this.duration = duration;
    this.evid = evid;
    this.orid = orid;
    this.wfid = wfid;
    this.arid = arid;
    this.algoid = algoid;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Nnsa_amp_descript(Nnsa_amp_descript other) {
    this.windowid = other.getWindowid();
    this.paramsetid = other.getParamsetid();
    this.sta = other.getSta();
    this.chan = other.getChan();
    this.phase = other.getPhase();
    this.delta = other.getDelta();
    this.seaz = other.getSeaz();
    this.depth = other.getDepth();
    this.gvlo = other.getGvlo();
    this.gvhi = other.getGvhi();
    this.toff = other.getToff();
    this.start_time = other.getStart_time();
    this.duration = other.getDuration();
    this.evid = other.getEvid();
    this.orid = other.getOrid();
    this.wfid = other.getWfid();
    this.arid = other.getArid();
    this.algoid = other.getAlgoid();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Nnsa_amp_descript() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(WINDOWID_NA, PARAMSETID_NA, STA_NA, CHAN_NA, PHASE_NA, DELTA_NA, SEAZ_NA, DEPTH_NA,
        GVLO_NA, GVHI_NA, TOFF_NA, START_TIME_NA, DURATION_NA, EVID_NA, ORID_NA, WFID_NA, ARID_NA,
        ALGOID_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "chan":
        return chan;
      case "phase":
        return phase;
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
      case "chan":
        chan = value;
        break;
      case "phase":
        phase = value;
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
      case "delta":
        return delta;
      case "seaz":
        return seaz;
      case "depth":
        return depth;
      case "gvlo":
        return gvlo;
      case "gvhi":
        return gvhi;
      case "toff":
        return toff;
      case "start_time":
        return start_time;
      case "duration":
        return duration;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "delta":
        delta = value;
        break;
      case "seaz":
        seaz = value;
        break;
      case "depth":
        depth = value;
        break;
      case "gvlo":
        gvlo = value;
        break;
      case "gvhi":
        gvhi = value;
        break;
      case "toff":
        toff = value;
        break;
      case "start_time":
        start_time = value;
        break;
      case "duration":
        duration = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "windowid":
        return windowid;
      case "paramsetid":
        return paramsetid;
      case "evid":
        return evid;
      case "orid":
        return orid;
      case "wfid":
        return wfid;
      case "arid":
        return arid;
      case "algoid":
        return algoid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "windowid":
        windowid = value;
        break;
      case "paramsetid":
        paramsetid = value;
        break;
      case "evid":
        evid = value;
        break;
      case "orid":
        orid = value;
        break;
      case "wfid":
        wfid = value;
        break;
      case "arid":
        arid = value;
        break;
      case "algoid":
        algoid = value;
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
  public Nnsa_amp_descript(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Nnsa_amp_descript(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), readString(input), readString(input),
        readString(input), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        input.readLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Nnsa_amp_descript(ByteBuffer input) {
    this(input.getLong(), input.getLong(), readString(input), readString(input), readString(input),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(), input.getLong(),
        input.getLong(), input.getLong(), input.getLong(), input.getLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Nnsa_amp_descript(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Nnsa_amp_descript(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getLong(offset + 14), input.getLong(offset + 15),
        input.getLong(offset + 16), input.getLong(offset + 17), input.getLong(offset + 18),
        input.getString(offset + 19));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[19];
    values[0] = windowid;
    values[1] = paramsetid;
    values[2] = sta;
    values[3] = chan;
    values[4] = phase;
    values[5] = delta;
    values[6] = seaz;
    values[7] = depth;
    values[8] = gvlo;
    values[9] = gvhi;
    values[10] = toff;
    values[11] = start_time;
    values[12] = duration;
    values[13] = evid;
    values[14] = orid;
    values[15] = wfid;
    values[16] = arid;
    values[17] = algoid;
    values[18] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[20];
    values[0] = windowid;
    values[1] = paramsetid;
    values[2] = sta;
    values[3] = chan;
    values[4] = phase;
    values[5] = delta;
    values[6] = seaz;
    values[7] = depth;
    values[8] = gvlo;
    values[9] = gvhi;
    values[10] = toff;
    values[11] = start_time;
    values[12] = duration;
    values[13] = evid;
    values[14] = orid;
    values[15] = wfid;
    values[16] = arid;
    values[17] = algoid;
    values[18] = auth;
    values[19] = lddate;
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
    output.writeLong(windowid);
    output.writeLong(paramsetid);
    writeString(output, sta);
    writeString(output, chan);
    writeString(output, phase);
    output.writeDouble(delta);
    output.writeDouble(seaz);
    output.writeDouble(depth);
    output.writeDouble(gvlo);
    output.writeDouble(gvhi);
    output.writeDouble(toff);
    output.writeDouble(start_time);
    output.writeDouble(duration);
    output.writeLong(evid);
    output.writeLong(orid);
    output.writeLong(wfid);
    output.writeLong(arid);
    output.writeLong(algoid);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(windowid);
    output.putLong(paramsetid);
    writeString(output, sta);
    writeString(output, chan);
    writeString(output, phase);
    output.putDouble(delta);
    output.putDouble(seaz);
    output.putDouble(depth);
    output.putDouble(gvlo);
    output.putDouble(gvhi);
    output.putDouble(toff);
    output.putDouble(start_time);
    output.putDouble(duration);
    output.putLong(evid);
    output.putLong(orid);
    output.putLong(wfid);
    output.putLong(arid);
    output.putLong(algoid);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Nnsa_amp_descript objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Nnsa_amp_descript objects.
   * @throws IOException
   */
  static public void readNnsa_amp_descripts(BufferedReader input,
      Collection<Nnsa_amp_descript> rows) throws IOException {
    String[] saved = Nnsa_amp_descript.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Nnsa_amp_descript
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Nnsa_amp_descript(new Scanner(line)));
    }
    input.close();
    Nnsa_amp_descript.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Nnsa_amp_descript objects from an ascii file. The Collection is not
   * emptied before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Nnsa_amp_descript objects.
   * @throws IOException
   */
  static public void readNnsa_amp_descripts(File inputFile, Collection<Nnsa_amp_descript> rows)
      throws IOException {
    readNnsa_amp_descripts(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Nnsa_amp_descript objects from an ascii input stream. The Collection is
   * not emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Nnsa_amp_descript objects.
   * @throws IOException
   */
  static public void readNnsa_amp_descripts(InputStream inputStream,
      Collection<Nnsa_amp_descript> rows) throws IOException {
    readNnsa_amp_descripts(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Nnsa_amp_descript objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Nnsa_amp_descript objects
   * @throws IOException
   */
  static public Set<Nnsa_amp_descript> readNnsa_amp_descripts(BufferedReader input)
      throws IOException {
    Set<Nnsa_amp_descript> rows = new LinkedHashSet<Nnsa_amp_descript>();
    readNnsa_amp_descripts(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Nnsa_amp_descript objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Nnsa_amp_descript objects
   * @throws IOException
   */
  static public Set<Nnsa_amp_descript> readNnsa_amp_descripts(File inputFile) throws IOException {
    return readNnsa_amp_descripts(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Nnsa_amp_descript objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Nnsa_amp_descript objects
   * @throws IOException
   */
  static public Set<Nnsa_amp_descript> readNnsa_amp_descripts(InputStream input)
      throws IOException {
    return readNnsa_amp_descripts(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Nnsa_amp_descript objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param nnsa_amp_descripts the Nnsa_amp_descript objects to write
   * @throws IOException
   */
  static public void write(File fileName,
      Collection<? extends Nnsa_amp_descript> nnsa_amp_descripts) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Nnsa_amp_descript nnsa_amp_descript : nnsa_amp_descripts)
      nnsa_amp_descript.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Nnsa_amp_descript objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param nnsa_amp_descripts the Nnsa_amp_descript objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Nnsa_amp_descript> nnsa_amp_descripts, java.util.Date lddate,
      boolean commit) throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Nnsa_amp_descript nnsa_amp_descript : nnsa_amp_descripts) {
        int i = 0;
        statement.setLong(++i, nnsa_amp_descript.windowid);
        statement.setLong(++i, nnsa_amp_descript.paramsetid);
        statement.setString(++i, nnsa_amp_descript.sta);
        statement.setString(++i, nnsa_amp_descript.chan);
        statement.setString(++i, nnsa_amp_descript.phase);
        statement.setDouble(++i, nnsa_amp_descript.delta);
        statement.setDouble(++i, nnsa_amp_descript.seaz);
        statement.setDouble(++i, nnsa_amp_descript.depth);
        statement.setDouble(++i, nnsa_amp_descript.gvlo);
        statement.setDouble(++i, nnsa_amp_descript.gvhi);
        statement.setDouble(++i, nnsa_amp_descript.toff);
        statement.setDouble(++i, nnsa_amp_descript.start_time);
        statement.setDouble(++i, nnsa_amp_descript.duration);
        statement.setLong(++i, nnsa_amp_descript.evid);
        statement.setLong(++i, nnsa_amp_descript.orid);
        statement.setLong(++i, nnsa_amp_descript.wfid);
        statement.setLong(++i, nnsa_amp_descript.arid);
        statement.setLong(++i, nnsa_amp_descript.algoid);
        statement.setString(++i, nnsa_amp_descript.auth);
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
   *        Nnsa_amp_descript table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Nnsa_amp_descript> readNnsa_amp_descripts(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Nnsa_amp_descript> results = new HashSet<Nnsa_amp_descript>();
    readNnsa_amp_descripts(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Nnsa_amp_descript table.
   * @param nnsa_amp_descripts
   * @throws SQLException
   */
  static public void readNnsa_amp_descripts(Connection connection, String selectStatement,
      Set<Nnsa_amp_descript> nnsa_amp_descripts) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        nnsa_amp_descripts.add(new Nnsa_amp_descript(rs));
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
   * this Nnsa_amp_descript object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Nnsa_amp_descript object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "windowid, paramsetid, sta, chan, phase, delta, seaz, depth, gvlo, gvhi, toff, start_time, duration, evid, orid, wfid, arid, algoid, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(windowid)).append(", ");
    sql.append(Long.toString(paramsetid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(chan).append("', ");
    sql.append("'").append(phase).append("', ");
    sql.append(Double.toString(delta)).append(", ");
    sql.append(Double.toString(seaz)).append(", ");
    sql.append(Double.toString(depth)).append(", ");
    sql.append(Double.toString(gvlo)).append(", ");
    sql.append(Double.toString(gvhi)).append(", ");
    sql.append(Double.toString(toff)).append(", ");
    sql.append(Double.toString(start_time)).append(", ");
    sql.append(Double.toString(duration)).append(", ");
    sql.append(Long.toString(evid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(wfid)).append(", ");
    sql.append(Long.toString(arid)).append(", ");
    sql.append(Long.toString(algoid)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Nnsa_amp_descript in the database. Primary and unique keys are set, if
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
   * Create a table of type Nnsa_amp_descript in the database
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
   * Generate a sql script to create a table of type Nnsa_amp_descript in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Nnsa_amp_descript in the database
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
    buf.append("windowid     number(9)            NOT NULL,\n");
    buf.append("paramsetid   number(9)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("phase        varchar2(8)          NOT NULL,\n");
    buf.append("delta        float(24)            NOT NULL,\n");
    buf.append("seaz         float(24)            NOT NULL,\n");
    buf.append("depth        float(24)            NOT NULL,\n");
    buf.append("gvlo         float(24)            NOT NULL,\n");
    buf.append("gvhi         float(24)            NOT NULL,\n");
    buf.append("toff         float(24)            NOT NULL,\n");
    buf.append("start_time   float(53)            NOT NULL,\n");
    buf.append("duration     float(24)            NOT NULL,\n");
    buf.append("evid         number(9)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("wfid         number(9)            NOT NULL,\n");
    buf.append("arid         number(9)            NOT NULL,\n");
    buf.append("algoid       number(9)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (windowid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (paramsetid,sta,chan,phase,orid)");
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
    return 178;
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
    return (other instanceof Nnsa_amp_descript) && ((Nnsa_amp_descript) other).windowid == windowid;
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
    return (other instanceof Nnsa_amp_descript)
        && ((Nnsa_amp_descript) other).paramsetid == paramsetid
        && ((Nnsa_amp_descript) other).sta.equals(sta)
        && ((Nnsa_amp_descript) other).chan.equals(chan)
        && ((Nnsa_amp_descript) other).phase.equals(phase)
        && ((Nnsa_amp_descript) other).orid == orid;
  }

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   * 
   * @return windowid
   */
  public long getWindowid() {
    return windowid;
  }

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   * 
   * @param windowid
   * @throws IllegalArgumentException if windowid >= 1000000000
   */
  public Nnsa_amp_descript setWindowid(long windowid) {
    if (windowid >= 1000000000L)
      throw new IllegalArgumentException("windowid=" + windowid + " but cannot be >= 1000000000");
    this.windowid = windowid;
    setHash(null);
    return this;
  }

  /**
   * Parameter set identification number used when making raw amplitude measurements.
   * 
   * @return paramsetid
   */
  public long getParamsetid() {
    return paramsetid;
  }

  /**
   * Parameter set identification number used when making raw amplitude measurements.
   * 
   * @param paramsetid
   * @throws IllegalArgumentException if paramsetid >= 1000000000
   */
  public Nnsa_amp_descript setParamsetid(long paramsetid) {
    if (paramsetid >= 1000000000L)
      throw new IllegalArgumentException(
          "paramsetid=" + paramsetid + " but cannot be >= 1000000000");
    this.paramsetid = paramsetid;
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
  public Nnsa_amp_descript setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   * 
   * @return chan
   */
  public String getChan() {
    return chan;
  }

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   * 
   * @param chan
   * @throws IllegalArgumentException if chan.length() >= 8
   */
  public Nnsa_amp_descript setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
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
  public Nnsa_amp_descript setPhase(String phase) {
    if (phase.length() > 8)
      throw new IllegalArgumentException(
          String.format("phase.length() cannot be > 8.  phase=%s", phase));
    this.phase = phase;
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
  public Nnsa_amp_descript setDelta(double delta) {
    this.delta = delta;
    setHash(null);
    return this;
  }

  /**
   * Station-to-event azimuth calculated from the station and event locations and measured clockwise
   * from north.
   * <p>
   * Units: degree
   * 
   * @return seaz
   */
  public double getSeaz() {
    return seaz;
  }

  /**
   * Station-to-event azimuth calculated from the station and event locations and measured clockwise
   * from north.
   * <p>
   * Units: degree
   * 
   * @param seaz
   */
  public Nnsa_amp_descript setSeaz(double seaz) {
    this.seaz = seaz;
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
  public Nnsa_amp_descript setDepth(double depth) {
    this.depth = depth;
    setHash(null);
    return this;
  }

  /**
   * Low group velocity used to isolate phase or noise window.
   * <p>
   * Units: km/s
   * 
   * @return gvlo
   */
  public double getGvlo() {
    return gvlo;
  }

  /**
   * Low group velocity used to isolate phase or noise window.
   * <p>
   * Units: km/s
   * 
   * @param gvlo
   */
  public Nnsa_amp_descript setGvlo(double gvlo) {
    this.gvlo = gvlo;
    setHash(null);
    return this;
  }

  /**
   * High group velocity used to isolate phase or noise window.
   * <p>
   * Units: km/s
   * 
   * @return gvhi
   */
  public double getGvhi() {
    return gvhi;
  }

  /**
   * High group velocity used to isolate phase or noise window.
   * <p>
   * Units: km/s
   * 
   * @param gvhi
   */
  public Nnsa_amp_descript setGvhi(double gvhi) {
    this.gvhi = gvhi;
    setHash(null);
    return this;
  }

  /**
   * Time offset used of Pn arrival from pre-specified group velocity in seconds. Used as an origin
   * offset correction for other phases. (<B>discrim_data</B>). Time offset relative to a
   * theoretical arrival time (<B>nnsa_amp_descript</B>)
   * <p>
   * Units: s
   * 
   * @return toff
   */
  public double getToff() {
    return toff;
  }

  /**
   * Time offset used of Pn arrival from pre-specified group velocity in seconds. Used as an origin
   * offset correction for other phases. (<B>discrim_data</B>). Time offset relative to a
   * theoretical arrival time (<B>nnsa_amp_descript</B>)
   * <p>
   * Units: s
   * 
   * @param toff
   */
  public Nnsa_amp_descript setToff(double toff) {
    this.toff = toff;
    setHash(null);
    return this;
  }

  /**
   * Absolute begin time of window used to measure the amplitude.
   * <p>
   * Units: s
   * 
   * @return start_time
   */
  public double getStart_time() {
    return start_time;
  }

  /**
   * Absolute begin time of window used to measure the amplitude.
   * <p>
   * Units: s
   * 
   * @param start_time
   */
  public Nnsa_amp_descript setStart_time(double start_time) {
    this.start_time = start_time;
    setHash(null);
    return this;
  }

  /**
   * Duration of window for amplitude measurement. Combined with <I>start_time</I>, the entire
   * amplitude time window is specified.
   * <p>
   * Units: s
   * 
   * @return duration
   */
  public double getDuration() {
    return duration;
  }

  /**
   * Duration of window for amplitude measurement. Combined with <I>start_time</I>, the entire
   * amplitude time window is specified.
   * <p>
   * Units: s
   * 
   * @param duration
   */
  public Nnsa_amp_descript setDuration(double duration) {
    this.duration = duration;
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
  public Nnsa_amp_descript setEvid(long evid) {
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
  public Nnsa_amp_descript setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Waveform identifier of the seismogram from which measurement was made. See <B>wfdisc</B>.
   * 
   * @return wfid
   */
  public long getWfid() {
    return wfid;
  }

  /**
   * Waveform identifier of the seismogram from which measurement was made. See <B>wfdisc</B>.
   * 
   * @param wfid
   * @throws IllegalArgumentException if wfid >= 1000000000
   */
  public Nnsa_amp_descript setWfid(long wfid) {
    if (wfid >= 1000000000L)
      throw new IllegalArgumentException("wfid=" + wfid + " but cannot be >= 1000000000");
    this.wfid = wfid;
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
  public Nnsa_amp_descript setArid(long arid) {
    if (arid >= 1000000000L)
      throw new IllegalArgumentException("arid=" + arid + " but cannot be >= 1000000000");
    this.arid = arid;
    setHash(null);
    return this;
  }

  /**
   * Algorithm identifier. Identifies algorithm(s) used to measure data.
   * 
   * @return algoid
   */
  public long getAlgoid() {
    return algoid;
  }

  /**
   * Algorithm identifier. Identifies algorithm(s) used to measure data.
   * 
   * @param algoid
   * @throws IllegalArgumentException if algoid >= 1000000000
   */
  public Nnsa_amp_descript setAlgoid(long algoid) {
    if (algoid >= 1000000000L)
      throw new IllegalArgumentException("algoid=" + algoid + " but cannot be >= 1000000000");
    this.algoid = algoid;
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
  public Nnsa_amp_descript setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
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
