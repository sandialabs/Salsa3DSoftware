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
 * surfvel
 */
public class Surfvel extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique identifier of surface wave group velocity measurement.
   */
  private long velid;

  static final public long VELID_NA = Long.MIN_VALUE;

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
   * Channel code for second channel used in measurement
   */
  private String chan2;

  static final public String CHAN2_NA = null;

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   */
  private String phase;

  static final public String PHASE_NA = null;

  /**
   * Type of velocity measurement: group velocity (group) or phase velocity (phase) for surface wave
   * group velocity measurement.
   */
  private String veltype;

  static final public String VELTYPE_NA = "-";

  /**
   * Instantaneous period for surface wave group velocity measurement.
   * <p>
   * Units: s
   */
  private double iper;

  static final public double IPER_NA = -1;

  /**
   * Filter period of surface wave group velocity measurement.
   * <p>
   * Units: s
   */
  private double fper;

  static final public double FPER_NA = -1;

  /**
   * Predicted surface wave group or phase velocity (<B>surfinv</B>). Measured surface wave group or
   * phase velocity (<B>surfinv</B>)
   * <p>
   * Units: km/s
   */
  private double vel;

  static final public double VEL_NA = -1;

  /**
   * Uncertainty estimate of lower bound of velocity
   * <p>
   * Units: km/s
   */
  private double uncl;

  static final public double UNCL_NA = -1;

  /**
   * Uncertainty estimate of upper bound of velocity
   * <p>
   * Units: km/s
   */
  private double uncu;

  static final public double UNCU_NA = -1;

  /**
   * Distance of surface wave path.
   * <p>
   * Units: km
   */
  private double dist;

  static final public double DIST_NA = -1;

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   */
  private long evid;

  static final public long EVID_NA = -1;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = -1;

  /**
   * Waveform identifier of the seismogram from which measurement was made. See <B>wfdisc</B>.
   */
  private long wfid;

  static final public long WFID_NA = -1;

  /**
   * Waveform identifier for second channel.
   */
  private long wfid2;

  static final public long WFID2_NA = -1;

  /**
   * Algorithm identifier. Identifies algorithm(s) used to measure data.
   */
  private long algoid;

  static final public long ALGOID_NA = -1;

  /**
   * Quality of measurement. <I>Quality</I> = a is the highest quality and <I>quality</I> = f is the
   * lowest quality.
   */
  private String quality;

  static final public String QUALITY_NA = null;

  /**
   * Gaussian width of filter used to calculate surface wave group velocities
   * <p>
   * Units: s
   */
  private double alpha;

  static final public double ALPHA_NA = -1;

  /**
   * Minimum of velocity range for surface wave group velocity measurement.
   * <p>
   * Units: km/s
   */
  private double velmin;

  static final public double VELMIN_NA = -1;

  /**
   * Maximum of velocity range for surface wave group velocity measurement.
   * <p>
   * Units: km/s
   */
  private double velmax;

  static final public double VELMAX_NA = -1;

  /**
   * Minimum of period range for surface wave group velocity measurement.
   * <p>
   * Units: s
   */
  private double pmin;

  static final public double PMIN_NA = Double.NaN;

  /**
   * Maximum of period range for surface wave group velocity measurement.
   * <p>
   * Units: s
   */
  private double pmax;

  static final public double PMAX_NA = Double.NaN;

  /**
   * Number of periods within range.
   */
  private long nperiods;

  static final public long NPERIODS_NA = -1;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = "-";


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("velid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("chan2", Columns.FieldType.STRING, "%s");
    columns.add("phase", Columns.FieldType.STRING, "%s");
    columns.add("veltype", Columns.FieldType.STRING, "%s");
    columns.add("iper", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("fper", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("vel", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("uncl", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("uncu", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("dist", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("evid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("wfid", Columns.FieldType.LONG, "%d");
    columns.add("wfid2", Columns.FieldType.LONG, "%d");
    columns.add("algoid", Columns.FieldType.LONG, "%d");
    columns.add("quality", Columns.FieldType.STRING, "%s");
    columns.add("alpha", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("velmin", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("velmax", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("pmin", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("pmax", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("nperiods", Columns.FieldType.LONG, "%d");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Surfvel(long velid, String sta, String chan, String chan2, String phase, String veltype,
      double iper, double fper, double vel, double uncl, double uncu, double dist, long evid,
      long orid, long wfid, long wfid2, long algoid, String quality, double alpha, double velmin,
      double velmax, double pmin, double pmax, long nperiods, String auth) {
    setValues(velid, sta, chan, chan2, phase, veltype, iper, fper, vel, uncl, uncu, dist, evid,
        orid, wfid, wfid2, algoid, quality, alpha, velmin, velmax, pmin, pmax, nperiods, auth);
  }

  private void setValues(long velid, String sta, String chan, String chan2, String phase,
      String veltype, double iper, double fper, double vel, double uncl, double uncu, double dist,
      long evid, long orid, long wfid, long wfid2, long algoid, String quality, double alpha,
      double velmin, double velmax, double pmin, double pmax, long nperiods, String auth) {
    this.velid = velid;
    this.sta = sta;
    this.chan = chan;
    this.chan2 = chan2;
    this.phase = phase;
    this.veltype = veltype;
    this.iper = iper;
    this.fper = fper;
    this.vel = vel;
    this.uncl = uncl;
    this.uncu = uncu;
    this.dist = dist;
    this.evid = evid;
    this.orid = orid;
    this.wfid = wfid;
    this.wfid2 = wfid2;
    this.algoid = algoid;
    this.quality = quality;
    this.alpha = alpha;
    this.velmin = velmin;
    this.velmax = velmax;
    this.pmin = pmin;
    this.pmax = pmax;
    this.nperiods = nperiods;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Surfvel(Surfvel other) {
    this.velid = other.getVelid();
    this.sta = other.getSta();
    this.chan = other.getChan();
    this.chan2 = other.getChan2();
    this.phase = other.getPhase();
    this.veltype = other.getVeltype();
    this.iper = other.getIper();
    this.fper = other.getFper();
    this.vel = other.getVel();
    this.uncl = other.getUncl();
    this.uncu = other.getUncu();
    this.dist = other.getDist();
    this.evid = other.getEvid();
    this.orid = other.getOrid();
    this.wfid = other.getWfid();
    this.wfid2 = other.getWfid2();
    this.algoid = other.getAlgoid();
    this.quality = other.getQuality();
    this.alpha = other.getAlpha();
    this.velmin = other.getVelmin();
    this.velmax = other.getVelmax();
    this.pmin = other.getPmin();
    this.pmax = other.getPmax();
    this.nperiods = other.getNperiods();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Surfvel() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(VELID_NA, STA_NA, CHAN_NA, CHAN2_NA, PHASE_NA, VELTYPE_NA, IPER_NA, FPER_NA, VEL_NA,
        UNCL_NA, UNCU_NA, DIST_NA, EVID_NA, ORID_NA, WFID_NA, WFID2_NA, ALGOID_NA, QUALITY_NA,
        ALPHA_NA, VELMIN_NA, VELMAX_NA, PMIN_NA, PMAX_NA, NPERIODS_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "chan":
        return chan;
      case "chan2":
        return chan2;
      case "phase":
        return phase;
      case "veltype":
        return veltype;
      case "quality":
        return quality;
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
      case "chan2":
        chan2 = value;
        break;
      case "phase":
        phase = value;
        break;
      case "veltype":
        veltype = value;
        break;
      case "quality":
        quality = value;
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
      case "iper":
        return iper;
      case "fper":
        return fper;
      case "vel":
        return vel;
      case "uncl":
        return uncl;
      case "uncu":
        return uncu;
      case "dist":
        return dist;
      case "alpha":
        return alpha;
      case "velmin":
        return velmin;
      case "velmax":
        return velmax;
      case "pmin":
        return pmin;
      case "pmax":
        return pmax;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "iper":
        iper = value;
        break;
      case "fper":
        fper = value;
        break;
      case "vel":
        vel = value;
        break;
      case "uncl":
        uncl = value;
        break;
      case "uncu":
        uncu = value;
        break;
      case "dist":
        dist = value;
        break;
      case "alpha":
        alpha = value;
        break;
      case "velmin":
        velmin = value;
        break;
      case "velmax":
        velmax = value;
        break;
      case "pmin":
        pmin = value;
        break;
      case "pmax":
        pmax = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "velid":
        return velid;
      case "evid":
        return evid;
      case "orid":
        return orid;
      case "wfid":
        return wfid;
      case "wfid2":
        return wfid2;
      case "algoid":
        return algoid;
      case "nperiods":
        return nperiods;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "velid":
        velid = value;
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
      case "wfid2":
        wfid2 = value;
        break;
      case "algoid":
        algoid = value;
        break;
      case "nperiods":
        nperiods = value;
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
  public Surfvel(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Surfvel(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), readString(input),
        readString(input), readString(input), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        readString(input), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Surfvel(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), readString(input),
        readString(input), readString(input), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(), input.getLong(),
        input.getLong(), input.getLong(), input.getLong(), input.getLong(), readString(input),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getLong(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Surfvel(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Surfvel(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getLong(offset + 13), input.getLong(offset + 14), input.getLong(offset + 15),
        input.getLong(offset + 16), input.getLong(offset + 17), input.getString(offset + 18),
        input.getDouble(offset + 19), input.getDouble(offset + 20), input.getDouble(offset + 21),
        input.getDouble(offset + 22), input.getDouble(offset + 23), input.getLong(offset + 24),
        input.getString(offset + 25));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[25];
    values[0] = velid;
    values[1] = sta;
    values[2] = chan;
    values[3] = chan2;
    values[4] = phase;
    values[5] = veltype;
    values[6] = iper;
    values[7] = fper;
    values[8] = vel;
    values[9] = uncl;
    values[10] = uncu;
    values[11] = dist;
    values[12] = evid;
    values[13] = orid;
    values[14] = wfid;
    values[15] = wfid2;
    values[16] = algoid;
    values[17] = quality;
    values[18] = alpha;
    values[19] = velmin;
    values[20] = velmax;
    values[21] = pmin;
    values[22] = pmax;
    values[23] = nperiods;
    values[24] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[26];
    values[0] = velid;
    values[1] = sta;
    values[2] = chan;
    values[3] = chan2;
    values[4] = phase;
    values[5] = veltype;
    values[6] = iper;
    values[7] = fper;
    values[8] = vel;
    values[9] = uncl;
    values[10] = uncu;
    values[11] = dist;
    values[12] = evid;
    values[13] = orid;
    values[14] = wfid;
    values[15] = wfid2;
    values[16] = algoid;
    values[17] = quality;
    values[18] = alpha;
    values[19] = velmin;
    values[20] = velmax;
    values[21] = pmin;
    values[22] = pmax;
    values[23] = nperiods;
    values[24] = auth;
    values[25] = lddate;
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
    output.writeLong(velid);
    writeString(output, sta);
    writeString(output, chan);
    writeString(output, chan2);
    writeString(output, phase);
    writeString(output, veltype);
    output.writeDouble(iper);
    output.writeDouble(fper);
    output.writeDouble(vel);
    output.writeDouble(uncl);
    output.writeDouble(uncu);
    output.writeDouble(dist);
    output.writeLong(evid);
    output.writeLong(orid);
    output.writeLong(wfid);
    output.writeLong(wfid2);
    output.writeLong(algoid);
    writeString(output, quality);
    output.writeDouble(alpha);
    output.writeDouble(velmin);
    output.writeDouble(velmax);
    output.writeDouble(pmin);
    output.writeDouble(pmax);
    output.writeLong(nperiods);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(velid);
    writeString(output, sta);
    writeString(output, chan);
    writeString(output, chan2);
    writeString(output, phase);
    writeString(output, veltype);
    output.putDouble(iper);
    output.putDouble(fper);
    output.putDouble(vel);
    output.putDouble(uncl);
    output.putDouble(uncu);
    output.putDouble(dist);
    output.putLong(evid);
    output.putLong(orid);
    output.putLong(wfid);
    output.putLong(wfid2);
    output.putLong(algoid);
    writeString(output, quality);
    output.putDouble(alpha);
    output.putDouble(velmin);
    output.putDouble(velmax);
    output.putDouble(pmin);
    output.putDouble(pmax);
    output.putLong(nperiods);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Surfvel objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Surfvel objects.
   * @throws IOException
   */
  static public void readSurfvels(BufferedReader input, Collection<Surfvel> rows)
      throws IOException {
    String[] saved = Surfvel.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Surfvel.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Surfvel(new Scanner(line)));
    }
    input.close();
    Surfvel.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Surfvel objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Surfvel objects.
   * @throws IOException
   */
  static public void readSurfvels(File inputFile, Collection<Surfvel> rows) throws IOException {
    readSurfvels(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Surfvel objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Surfvel objects.
   * @throws IOException
   */
  static public void readSurfvels(InputStream inputStream, Collection<Surfvel> rows)
      throws IOException {
    readSurfvels(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Surfvel objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Surfvel objects
   * @throws IOException
   */
  static public Set<Surfvel> readSurfvels(BufferedReader input) throws IOException {
    Set<Surfvel> rows = new LinkedHashSet<Surfvel>();
    readSurfvels(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Surfvel objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Surfvel objects
   * @throws IOException
   */
  static public Set<Surfvel> readSurfvels(File inputFile) throws IOException {
    return readSurfvels(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Surfvel objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Surfvel objects
   * @throws IOException
   */
  static public Set<Surfvel> readSurfvels(InputStream input) throws IOException {
    return readSurfvels(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Surfvel objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param surfvels the Surfvel objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Surfvel> surfvels)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Surfvel surfvel : surfvels)
      surfvel.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Surfvel objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param surfvels the Surfvel objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Surfvel> surfvels, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName
          + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Surfvel surfvel : surfvels) {
        int i = 0;
        statement.setLong(++i, surfvel.velid);
        statement.setString(++i, surfvel.sta);
        statement.setString(++i, surfvel.chan);
        statement.setString(++i, surfvel.chan2);
        statement.setString(++i, surfvel.phase);
        statement.setString(++i, surfvel.veltype);
        statement.setDouble(++i, surfvel.iper);
        statement.setDouble(++i, surfvel.fper);
        statement.setDouble(++i, surfvel.vel);
        statement.setDouble(++i, surfvel.uncl);
        statement.setDouble(++i, surfvel.uncu);
        statement.setDouble(++i, surfvel.dist);
        statement.setLong(++i, surfvel.evid);
        statement.setLong(++i, surfvel.orid);
        statement.setLong(++i, surfvel.wfid);
        statement.setLong(++i, surfvel.wfid2);
        statement.setLong(++i, surfvel.algoid);
        statement.setString(++i, surfvel.quality);
        statement.setDouble(++i, surfvel.alpha);
        statement.setDouble(++i, surfvel.velmin);
        statement.setDouble(++i, surfvel.velmax);
        statement.setDouble(++i, surfvel.pmin);
        statement.setDouble(++i, surfvel.pmax);
        statement.setLong(++i, surfvel.nperiods);
        statement.setString(++i, surfvel.auth);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Surfvel
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Surfvel> readSurfvels(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Surfvel> results = new HashSet<Surfvel>();
    readSurfvels(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Surfvel
   *        table.
   * @param surfvels
   * @throws SQLException
   */
  static public void readSurfvels(Connection connection, String selectStatement,
      Set<Surfvel> surfvels) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        surfvels.add(new Surfvel(rs));
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
   * this Surfvel object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Surfvel object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "velid, sta, chan, chan2, phase, veltype, iper, fper, vel, uncl, uncu, dist, evid, orid, wfid, wfid2, algoid, quality, alpha, velmin, velmax, pmin, pmax, nperiods, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(velid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(chan).append("', ");
    sql.append("'").append(chan2).append("', ");
    sql.append("'").append(phase).append("', ");
    sql.append("'").append(veltype).append("', ");
    sql.append(Double.toString(iper)).append(", ");
    sql.append(Double.toString(fper)).append(", ");
    sql.append(Double.toString(vel)).append(", ");
    sql.append(Double.toString(uncl)).append(", ");
    sql.append(Double.toString(uncu)).append(", ");
    sql.append(Double.toString(dist)).append(", ");
    sql.append(Long.toString(evid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(wfid)).append(", ");
    sql.append(Long.toString(wfid2)).append(", ");
    sql.append(Long.toString(algoid)).append(", ");
    sql.append("'").append(quality).append("', ");
    sql.append(Double.toString(alpha)).append(", ");
    sql.append(Double.toString(velmin)).append(", ");
    sql.append(Double.toString(velmax)).append(", ");
    sql.append(Double.toString(pmin)).append(", ");
    sql.append(Double.toString(pmax)).append(", ");
    sql.append(Long.toString(nperiods)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Surfvel in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Surfvel in the database
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
   * Generate a sql script to create a table of type Surfvel in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Surfvel in the database
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
    buf.append("velid        number(9)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("chan2        varchar2(8)          NOT NULL,\n");
    buf.append("phase        varchar2(8)          NOT NULL,\n");
    buf.append("veltype      varchar2(6)          NOT NULL,\n");
    buf.append("iper         float(24)            NOT NULL,\n");
    buf.append("fper         float(24)            NOT NULL,\n");
    buf.append("vel          float(24)            NOT NULL,\n");
    buf.append("uncl         float(24)            NOT NULL,\n");
    buf.append("uncu         float(24)            NOT NULL,\n");
    buf.append("dist         float(24)            NOT NULL,\n");
    buf.append("evid         number(9)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("wfid         number(9)            NOT NULL,\n");
    buf.append("wfid2        number(9)            NOT NULL,\n");
    buf.append("algoid       number(9)            NOT NULL,\n");
    buf.append("quality      varchar2(1)          NOT NULL,\n");
    buf.append("alpha        float(24)            NOT NULL,\n");
    buf.append("velmin       float(24)            NOT NULL,\n");
    buf.append("velmax       float(24)            NOT NULL,\n");
    buf.append("pmin         float(24)            NOT NULL,\n");
    buf.append("pmax         float(24)            NOT NULL,\n");
    buf.append("nperiods     number(8)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (velid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (sta,chan,chan2,phase,veltype,fper,orid,alpha,pmin,pmax,auth)");
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
    return 229;
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
    return (other instanceof Surfvel) && ((Surfvel) other).velid == velid;
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
    return (other instanceof Surfvel) && ((Surfvel) other).sta.equals(sta)
        && ((Surfvel) other).chan.equals(chan) && ((Surfvel) other).chan2.equals(chan2)
        && ((Surfvel) other).phase.equals(phase) && ((Surfvel) other).veltype.equals(veltype)
        && ((Surfvel) other).fper == fper && ((Surfvel) other).orid == orid
        && ((Surfvel) other).alpha == alpha && ((Surfvel) other).pmin == pmin
        && ((Surfvel) other).pmax == pmax && ((Surfvel) other).auth.equals(auth);
  }

  /**
   * Unique identifier of surface wave group velocity measurement.
   * 
   * @return velid
   */
  public long getVelid() {
    return velid;
  }

  /**
   * Unique identifier of surface wave group velocity measurement.
   * 
   * @param velid
   * @throws IllegalArgumentException if velid >= 1000000000
   */
  public Surfvel setVelid(long velid) {
    if (velid >= 1000000000L)
      throw new IllegalArgumentException("velid=" + velid + " but cannot be >= 1000000000");
    this.velid = velid;
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
  public Surfvel setSta(String sta) {
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
  public Surfvel setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
    setHash(null);
    return this;
  }

  /**
   * Channel code for second channel used in measurement
   * 
   * @return chan2
   */
  public String getChan2() {
    return chan2;
  }

  /**
   * Channel code for second channel used in measurement
   * 
   * @param chan2
   * @throws IllegalArgumentException if chan2.length() >= 8
   */
  public Surfvel setChan2(String chan2) {
    if (chan2.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan2.length() cannot be > 8.  chan2=%s", chan2));
    this.chan2 = chan2;
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
  public Surfvel setPhase(String phase) {
    if (phase.length() > 8)
      throw new IllegalArgumentException(
          String.format("phase.length() cannot be > 8.  phase=%s", phase));
    this.phase = phase;
    setHash(null);
    return this;
  }

  /**
   * Type of velocity measurement: group velocity (group) or phase velocity (phase) for surface wave
   * group velocity measurement.
   * 
   * @return veltype
   */
  public String getVeltype() {
    return veltype;
  }

  /**
   * Type of velocity measurement: group velocity (group) or phase velocity (phase) for surface wave
   * group velocity measurement.
   * 
   * @param veltype
   * @throws IllegalArgumentException if veltype.length() >= 6
   */
  public Surfvel setVeltype(String veltype) {
    if (veltype.length() > 6)
      throw new IllegalArgumentException(
          String.format("veltype.length() cannot be > 6.  veltype=%s", veltype));
    this.veltype = veltype;
    setHash(null);
    return this;
  }

  /**
   * Instantaneous period for surface wave group velocity measurement.
   * <p>
   * Units: s
   * 
   * @return iper
   */
  public double getIper() {
    return iper;
  }

  /**
   * Instantaneous period for surface wave group velocity measurement.
   * <p>
   * Units: s
   * 
   * @param iper
   */
  public Surfvel setIper(double iper) {
    this.iper = iper;
    setHash(null);
    return this;
  }

  /**
   * Filter period of surface wave group velocity measurement.
   * <p>
   * Units: s
   * 
   * @return fper
   */
  public double getFper() {
    return fper;
  }

  /**
   * Filter period of surface wave group velocity measurement.
   * <p>
   * Units: s
   * 
   * @param fper
   */
  public Surfvel setFper(double fper) {
    this.fper = fper;
    setHash(null);
    return this;
  }

  /**
   * Predicted surface wave group or phase velocity (<B>surfinv</B>). Measured surface wave group or
   * phase velocity (<B>surfinv</B>)
   * <p>
   * Units: km/s
   * 
   * @return vel
   */
  public double getVel() {
    return vel;
  }

  /**
   * Predicted surface wave group or phase velocity (<B>surfinv</B>). Measured surface wave group or
   * phase velocity (<B>surfinv</B>)
   * <p>
   * Units: km/s
   * 
   * @param vel
   */
  public Surfvel setVel(double vel) {
    this.vel = vel;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty estimate of lower bound of velocity
   * <p>
   * Units: km/s
   * 
   * @return uncl
   */
  public double getUncl() {
    return uncl;
  }

  /**
   * Uncertainty estimate of lower bound of velocity
   * <p>
   * Units: km/s
   * 
   * @param uncl
   */
  public Surfvel setUncl(double uncl) {
    this.uncl = uncl;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty estimate of upper bound of velocity
   * <p>
   * Units: km/s
   * 
   * @return uncu
   */
  public double getUncu() {
    return uncu;
  }

  /**
   * Uncertainty estimate of upper bound of velocity
   * <p>
   * Units: km/s
   * 
   * @param uncu
   */
  public Surfvel setUncu(double uncu) {
    this.uncu = uncu;
    setHash(null);
    return this;
  }

  /**
   * Distance of surface wave path.
   * <p>
   * Units: km
   * 
   * @return dist
   */
  public double getDist() {
    return dist;
  }

  /**
   * Distance of surface wave path.
   * <p>
   * Units: km
   * 
   * @param dist
   */
  public Surfvel setDist(double dist) {
    this.dist = dist;
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
  public Surfvel setEvid(long evid) {
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
  public Surfvel setOrid(long orid) {
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
  public Surfvel setWfid(long wfid) {
    if (wfid >= 1000000000L)
      throw new IllegalArgumentException("wfid=" + wfid + " but cannot be >= 1000000000");
    this.wfid = wfid;
    setHash(null);
    return this;
  }

  /**
   * Waveform identifier for second channel.
   * 
   * @return wfid2
   */
  public long getWfid2() {
    return wfid2;
  }

  /**
   * Waveform identifier for second channel.
   * 
   * @param wfid2
   * @throws IllegalArgumentException if wfid2 >= 1000000000
   */
  public Surfvel setWfid2(long wfid2) {
    if (wfid2 >= 1000000000L)
      throw new IllegalArgumentException("wfid2=" + wfid2 + " but cannot be >= 1000000000");
    this.wfid2 = wfid2;
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
  public Surfvel setAlgoid(long algoid) {
    if (algoid >= 1000000000L)
      throw new IllegalArgumentException("algoid=" + algoid + " but cannot be >= 1000000000");
    this.algoid = algoid;
    setHash(null);
    return this;
  }

  /**
   * Quality of measurement. <I>Quality</I> = a is the highest quality and <I>quality</I> = f is the
   * lowest quality.
   * 
   * @return quality
   */
  public String getQuality() {
    return quality;
  }

  /**
   * Quality of measurement. <I>Quality</I> = a is the highest quality and <I>quality</I> = f is the
   * lowest quality.
   * 
   * @param quality
   * @throws IllegalArgumentException if quality.length() >= 1
   */
  public Surfvel setQuality(String quality) {
    if (quality.length() > 1)
      throw new IllegalArgumentException(
          String.format("quality.length() cannot be > 1.  quality=%s", quality));
    this.quality = quality;
    setHash(null);
    return this;
  }

  /**
   * Gaussian width of filter used to calculate surface wave group velocities
   * <p>
   * Units: s
   * 
   * @return alpha
   */
  public double getAlpha() {
    return alpha;
  }

  /**
   * Gaussian width of filter used to calculate surface wave group velocities
   * <p>
   * Units: s
   * 
   * @param alpha
   */
  public Surfvel setAlpha(double alpha) {
    this.alpha = alpha;
    setHash(null);
    return this;
  }

  /**
   * Minimum of velocity range for surface wave group velocity measurement.
   * <p>
   * Units: km/s
   * 
   * @return velmin
   */
  public double getVelmin() {
    return velmin;
  }

  /**
   * Minimum of velocity range for surface wave group velocity measurement.
   * <p>
   * Units: km/s
   * 
   * @param velmin
   */
  public Surfvel setVelmin(double velmin) {
    this.velmin = velmin;
    setHash(null);
    return this;
  }

  /**
   * Maximum of velocity range for surface wave group velocity measurement.
   * <p>
   * Units: km/s
   * 
   * @return velmax
   */
  public double getVelmax() {
    return velmax;
  }

  /**
   * Maximum of velocity range for surface wave group velocity measurement.
   * <p>
   * Units: km/s
   * 
   * @param velmax
   */
  public Surfvel setVelmax(double velmax) {
    this.velmax = velmax;
    setHash(null);
    return this;
  }

  /**
   * Minimum of period range for surface wave group velocity measurement.
   * <p>
   * Units: s
   * 
   * @return pmin
   */
  public double getPmin() {
    return pmin;
  }

  /**
   * Minimum of period range for surface wave group velocity measurement.
   * <p>
   * Units: s
   * 
   * @param pmin
   */
  public Surfvel setPmin(double pmin) {
    this.pmin = pmin;
    setHash(null);
    return this;
  }

  /**
   * Maximum of period range for surface wave group velocity measurement.
   * <p>
   * Units: s
   * 
   * @return pmax
   */
  public double getPmax() {
    return pmax;
  }

  /**
   * Maximum of period range for surface wave group velocity measurement.
   * <p>
   * Units: s
   * 
   * @param pmax
   */
  public Surfvel setPmax(double pmax) {
    this.pmax = pmax;
    setHash(null);
    return this;
  }

  /**
   * Number of periods within range.
   * 
   * @return nperiods
   */
  public long getNperiods() {
    return nperiods;
  }

  /**
   * Number of periods within range.
   * 
   * @param nperiods
   * @throws IllegalArgumentException if nperiods >= 100000000
   */
  public Surfvel setNperiods(long nperiods) {
    if (nperiods >= 100000000L)
      throw new IllegalArgumentException("nperiods=" + nperiods + " but cannot be >= 100000000");
    this.nperiods = nperiods;
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
  public Surfvel setAuth(String auth) {
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
