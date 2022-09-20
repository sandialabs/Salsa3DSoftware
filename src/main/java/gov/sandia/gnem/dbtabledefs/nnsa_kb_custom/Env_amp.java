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
 * env_amp
 */
public class Env_amp extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Envelope amplitude identifier
   */
  private long envampid;

  static final public long ENVAMPID_NA = Long.MIN_VALUE;

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   */
  private long magyieldid;

  static final public long MAGYIELDID_NA = Long.MIN_VALUE;

  /**
   * Waveform identifier of the seismogram from which measurement was made. See <B>wfdisc</B>.
   */
  private long wfid;

  static final public long WFID_NA = Long.MIN_VALUE;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Distance used in windowing and application of calibration parameters. May be undefined for
   * noise measurements only.
   * <p>
   * Units: km
   */
  private double distance;

  static final public double DISTANCE_NA = Double.NaN;

  /**
   * Error on distance used in windowing and application of calibration parameters.
   * <p>
   * Units: km
   */
  private double deldist;

  static final public double DELDIST_NA = -1;

  /**
   * Coda zero time (absolute epoch time) computed using <I>vg0</I>, <I>vg1</I>, <I>vg2</I> and
   * <I>distance</I> (Mayeda et al., 2003).
   * <p>
   * Units: s
   */
  private double codat0;

  static final public double CODAT0_NA = Double.NaN;

  /**
   * Error on coda zero time <I>codat0</I>.
   * <p>
   * Units: s
   */
  private double dcodat0;

  static final public double DCODAT0_NA = -1;

  /**
   * Coda shape spreading factor computed using <I>sspread0</I>, <I>sspread1</I>, <I>sspread2</I>,
   * and <I>distance</I> (Mayeda et al., 2003); undefined for direct wave measurements.
   */
  private double sspread;

  static final public double SSPREAD_NA = Double.NaN;

  /**
   * Error on coda shape spreading factor computed using <I>sspread0</I>, <I>sspread1</I>,
   * <I>sspread2</I> and <I>distance</I> (Mayeda et al., 2003); undefined for direct wave
   * measurements.
   */
  private double dsspread;

  static final public double DSSPREAD_NA = -1;

  /**
   * Coda shape attenuation factor computed using b0, b1, b2 and distance (Mayeda et al., 2003),
   * undefined for direct wave measurements.
   * <p>
   * Units: 1/s
   */
  private double bterm;

  static final public double BTERM_NA = Double.NaN;

  /**
   * Error on coda shape attenuation factor <I>bterm</I>.
   * <p>
   * Units: 1/s
   */
  private double dbterm;

  static final public double DBTERM_NA = -1;

  /**
   * Path correction method, note that the path correction type is indicated within
   * <I>wmodel_name</I> field in the <B>yield_fit</B> table.
   */
  private String pathcorrtype;

  static final public String PATHCORRTYPE_NA = null;

  /**
   * Log path correction applied to obtain source spectral estimate from raw amplitude.
   */
  private double pathcor;

  static final public double PATHCOR_NA = -1;

  /**
   * Error on log path correction applied to obtain source spectral estimate from raw amplitude.
   */
  private double delpathcor;

  static final public double DELPATHCOR_NA = -1;

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
   * Log envelope amplitude; can be a noise measurement, a direct or coda wave measurement, or a
   * source spectral estimate based on direct or coda wave measurements.
   * <p>
   * Units: log10(m/s), log10(nm), log10(counts)
   */
  private double envamp;

  static final public double ENVAMP_NA = Double.NaN;

  /**
   * Log envelope amplitude error.
   * <p>
   * Units: log10(m/s), log10(nm), log10(counts)
   */
  private double delenvamp;

  static final public double DELENVAMP_NA = -1;

  /**
   * Amplitude unit.
   */
  private String ampunit;

  static final public String AMPUNIT_NA = null;

  /**
   * Window start time (absolute epoch time) for amplitude measurement. See also <I>tend</I> and
   * <I>codat0</I>.
   * <p>
   * Units: s
   */
  private double tstart;

  static final public double TSTART_NA = Double.NaN;

  /**
   * Window end time (absolute epoch time) for amplitude measurement. (See <I>tstart</I> and
   * <I>codat0</I>).
   * <p>
   * Units: s
   */
  private double tend;

  static final public double TEND_NA = Double.NaN;

  /**
   * Manual coda window review action; recommended types are: reviewed - coda window reviewed,
   * modified - coda window reviewed and modified, none - coda window not reviewed.
   */
  private String reviewtype;

  static final public String REVIEWTYPE_NA = null;

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
    columns.add("envampid", Columns.FieldType.LONG, "%d");
    columns.add("magyieldid", Columns.FieldType.LONG, "%d");
    columns.add("wfid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("distance", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("deldist", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("codat0", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("dcodat0", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("sspread", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("dsspread", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("bterm", Columns.FieldType.DOUBLE, "%10.3e");
    columns.add("dbterm", Columns.FieldType.DOUBLE, "%10.3e");
    columns.add("pathcorrtype", Columns.FieldType.STRING, "%s");
    columns.add("pathcor", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("delpathcor", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("sitecor", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("delsitecor", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("envamp", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("delenvamp", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("ampunit", Columns.FieldType.STRING, "%s");
    columns.add("tstart", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("tend", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("reviewtype", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Env_amp(long envampid, long magyieldid, long wfid, long orid, double distance,
      double deldist, double codat0, double dcodat0, double sspread, double dsspread, double bterm,
      double dbterm, String pathcorrtype, double pathcor, double delpathcor, double sitecor,
      double delsitecor, double envamp, double delenvamp, String ampunit, double tstart,
      double tend, String reviewtype, String auth, long commid) {
    setValues(envampid, magyieldid, wfid, orid, distance, deldist, codat0, dcodat0, sspread,
        dsspread, bterm, dbterm, pathcorrtype, pathcor, delpathcor, sitecor, delsitecor, envamp,
        delenvamp, ampunit, tstart, tend, reviewtype, auth, commid);
  }

  private void setValues(long envampid, long magyieldid, long wfid, long orid, double distance,
      double deldist, double codat0, double dcodat0, double sspread, double dsspread, double bterm,
      double dbterm, String pathcorrtype, double pathcor, double delpathcor, double sitecor,
      double delsitecor, double envamp, double delenvamp, String ampunit, double tstart,
      double tend, String reviewtype, String auth, long commid) {
    this.envampid = envampid;
    this.magyieldid = magyieldid;
    this.wfid = wfid;
    this.orid = orid;
    this.distance = distance;
    this.deldist = deldist;
    this.codat0 = codat0;
    this.dcodat0 = dcodat0;
    this.sspread = sspread;
    this.dsspread = dsspread;
    this.bterm = bterm;
    this.dbterm = dbterm;
    this.pathcorrtype = pathcorrtype;
    this.pathcor = pathcor;
    this.delpathcor = delpathcor;
    this.sitecor = sitecor;
    this.delsitecor = delsitecor;
    this.envamp = envamp;
    this.delenvamp = delenvamp;
    this.ampunit = ampunit;
    this.tstart = tstart;
    this.tend = tend;
    this.reviewtype = reviewtype;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Env_amp(Env_amp other) {
    this.envampid = other.getEnvampid();
    this.magyieldid = other.getMagyieldid();
    this.wfid = other.getWfid();
    this.orid = other.getOrid();
    this.distance = other.getDistance();
    this.deldist = other.getDeldist();
    this.codat0 = other.getCodat0();
    this.dcodat0 = other.getDcodat0();
    this.sspread = other.getSspread();
    this.dsspread = other.getDsspread();
    this.bterm = other.getBterm();
    this.dbterm = other.getDbterm();
    this.pathcorrtype = other.getPathcorrtype();
    this.pathcor = other.getPathcor();
    this.delpathcor = other.getDelpathcor();
    this.sitecor = other.getSitecor();
    this.delsitecor = other.getDelsitecor();
    this.envamp = other.getEnvamp();
    this.delenvamp = other.getDelenvamp();
    this.ampunit = other.getAmpunit();
    this.tstart = other.getTstart();
    this.tend = other.getTend();
    this.reviewtype = other.getReviewtype();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Env_amp() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(ENVAMPID_NA, MAGYIELDID_NA, WFID_NA, ORID_NA, DISTANCE_NA, DELDIST_NA, CODAT0_NA,
        DCODAT0_NA, SSPREAD_NA, DSSPREAD_NA, BTERM_NA, DBTERM_NA, PATHCORRTYPE_NA, PATHCOR_NA,
        DELPATHCOR_NA, SITECOR_NA, DELSITECOR_NA, ENVAMP_NA, DELENVAMP_NA, AMPUNIT_NA, TSTART_NA,
        TEND_NA, REVIEWTYPE_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "pathcorrtype":
        return pathcorrtype;
      case "ampunit":
        return ampunit;
      case "reviewtype":
        return reviewtype;
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
      case "pathcorrtype":
        pathcorrtype = value;
        break;
      case "ampunit":
        ampunit = value;
        break;
      case "reviewtype":
        reviewtype = value;
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
      case "distance":
        return distance;
      case "deldist":
        return deldist;
      case "codat0":
        return codat0;
      case "dcodat0":
        return dcodat0;
      case "sspread":
        return sspread;
      case "dsspread":
        return dsspread;
      case "bterm":
        return bterm;
      case "dbterm":
        return dbterm;
      case "pathcor":
        return pathcor;
      case "delpathcor":
        return delpathcor;
      case "sitecor":
        return sitecor;
      case "delsitecor":
        return delsitecor;
      case "envamp":
        return envamp;
      case "delenvamp":
        return delenvamp;
      case "tstart":
        return tstart;
      case "tend":
        return tend;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "distance":
        distance = value;
        break;
      case "deldist":
        deldist = value;
        break;
      case "codat0":
        codat0 = value;
        break;
      case "dcodat0":
        dcodat0 = value;
        break;
      case "sspread":
        sspread = value;
        break;
      case "dsspread":
        dsspread = value;
        break;
      case "bterm":
        bterm = value;
        break;
      case "dbterm":
        dbterm = value;
        break;
      case "pathcor":
        pathcor = value;
        break;
      case "delpathcor":
        delpathcor = value;
        break;
      case "sitecor":
        sitecor = value;
        break;
      case "delsitecor":
        delsitecor = value;
        break;
      case "envamp":
        envamp = value;
        break;
      case "delenvamp":
        delenvamp = value;
        break;
      case "tstart":
        tstart = value;
        break;
      case "tend":
        tend = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "envampid":
        return envampid;
      case "magyieldid":
        return magyieldid;
      case "wfid":
        return wfid;
      case "orid":
        return orid;
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
      case "envampid":
        envampid = value;
        break;
      case "magyieldid":
        magyieldid = value;
        break;
      case "wfid":
        wfid = value;
        break;
      case "orid":
        orid = value;
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
  public Env_amp(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Env_amp(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), readString(input),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), readString(input), input.readDouble(),
        input.readDouble(), readString(input), readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Env_amp(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), readString(input),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), readString(input), input.getDouble(),
        input.getDouble(), readString(input), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Env_amp(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Env_amp(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getString(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getDouble(offset + 17), input.getDouble(offset + 18),
        input.getDouble(offset + 19), input.getString(offset + 20), input.getDouble(offset + 21),
        input.getDouble(offset + 22), input.getString(offset + 23), input.getString(offset + 24),
        input.getLong(offset + 25));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[25];
    values[0] = envampid;
    values[1] = magyieldid;
    values[2] = wfid;
    values[3] = orid;
    values[4] = distance;
    values[5] = deldist;
    values[6] = codat0;
    values[7] = dcodat0;
    values[8] = sspread;
    values[9] = dsspread;
    values[10] = bterm;
    values[11] = dbterm;
    values[12] = pathcorrtype;
    values[13] = pathcor;
    values[14] = delpathcor;
    values[15] = sitecor;
    values[16] = delsitecor;
    values[17] = envamp;
    values[18] = delenvamp;
    values[19] = ampunit;
    values[20] = tstart;
    values[21] = tend;
    values[22] = reviewtype;
    values[23] = auth;
    values[24] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[26];
    values[0] = envampid;
    values[1] = magyieldid;
    values[2] = wfid;
    values[3] = orid;
    values[4] = distance;
    values[5] = deldist;
    values[6] = codat0;
    values[7] = dcodat0;
    values[8] = sspread;
    values[9] = dsspread;
    values[10] = bterm;
    values[11] = dbterm;
    values[12] = pathcorrtype;
    values[13] = pathcor;
    values[14] = delpathcor;
    values[15] = sitecor;
    values[16] = delsitecor;
    values[17] = envamp;
    values[18] = delenvamp;
    values[19] = ampunit;
    values[20] = tstart;
    values[21] = tend;
    values[22] = reviewtype;
    values[23] = auth;
    values[24] = commid;
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
    output.writeLong(envampid);
    output.writeLong(magyieldid);
    output.writeLong(wfid);
    output.writeLong(orid);
    output.writeDouble(distance);
    output.writeDouble(deldist);
    output.writeDouble(codat0);
    output.writeDouble(dcodat0);
    output.writeDouble(sspread);
    output.writeDouble(dsspread);
    output.writeDouble(bterm);
    output.writeDouble(dbterm);
    writeString(output, pathcorrtype);
    output.writeDouble(pathcor);
    output.writeDouble(delpathcor);
    output.writeDouble(sitecor);
    output.writeDouble(delsitecor);
    output.writeDouble(envamp);
    output.writeDouble(delenvamp);
    writeString(output, ampunit);
    output.writeDouble(tstart);
    output.writeDouble(tend);
    writeString(output, reviewtype);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(envampid);
    output.putLong(magyieldid);
    output.putLong(wfid);
    output.putLong(orid);
    output.putDouble(distance);
    output.putDouble(deldist);
    output.putDouble(codat0);
    output.putDouble(dcodat0);
    output.putDouble(sspread);
    output.putDouble(dsspread);
    output.putDouble(bterm);
    output.putDouble(dbterm);
    writeString(output, pathcorrtype);
    output.putDouble(pathcor);
    output.putDouble(delpathcor);
    output.putDouble(sitecor);
    output.putDouble(delsitecor);
    output.putDouble(envamp);
    output.putDouble(delenvamp);
    writeString(output, ampunit);
    output.putDouble(tstart);
    output.putDouble(tend);
    writeString(output, reviewtype);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Env_amp objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Env_amp objects.
   * @throws IOException
   */
  static public void readEnv_amps(BufferedReader input, Collection<Env_amp> rows)
      throws IOException {
    String[] saved = Env_amp.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Env_amp.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Env_amp(new Scanner(line)));
    }
    input.close();
    Env_amp.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Env_amp objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Env_amp objects.
   * @throws IOException
   */
  static public void readEnv_amps(File inputFile, Collection<Env_amp> rows) throws IOException {
    readEnv_amps(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Env_amp objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Env_amp objects.
   * @throws IOException
   */
  static public void readEnv_amps(InputStream inputStream, Collection<Env_amp> rows)
      throws IOException {
    readEnv_amps(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Env_amp objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Env_amp objects
   * @throws IOException
   */
  static public Set<Env_amp> readEnv_amps(BufferedReader input) throws IOException {
    Set<Env_amp> rows = new LinkedHashSet<Env_amp>();
    readEnv_amps(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Env_amp objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Env_amp objects
   * @throws IOException
   */
  static public Set<Env_amp> readEnv_amps(File inputFile) throws IOException {
    return readEnv_amps(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Env_amp objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Env_amp objects
   * @throws IOException
   */
  static public Set<Env_amp> readEnv_amps(InputStream input) throws IOException {
    return readEnv_amps(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Env_amp objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param env_amps the Env_amp objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Env_amp> env_amps)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Env_amp env_amp : env_amps)
      env_amp.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Env_amp objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param env_amps the Env_amp objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Env_amp> env_amps, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName
          + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Env_amp env_amp : env_amps) {
        int i = 0;
        statement.setLong(++i, env_amp.envampid);
        statement.setLong(++i, env_amp.magyieldid);
        statement.setLong(++i, env_amp.wfid);
        statement.setLong(++i, env_amp.orid);
        statement.setDouble(++i, env_amp.distance);
        statement.setDouble(++i, env_amp.deldist);
        statement.setDouble(++i, env_amp.codat0);
        statement.setDouble(++i, env_amp.dcodat0);
        statement.setDouble(++i, env_amp.sspread);
        statement.setDouble(++i, env_amp.dsspread);
        statement.setDouble(++i, env_amp.bterm);
        statement.setDouble(++i, env_amp.dbterm);
        statement.setString(++i, env_amp.pathcorrtype);
        statement.setDouble(++i, env_amp.pathcor);
        statement.setDouble(++i, env_amp.delpathcor);
        statement.setDouble(++i, env_amp.sitecor);
        statement.setDouble(++i, env_amp.delsitecor);
        statement.setDouble(++i, env_amp.envamp);
        statement.setDouble(++i, env_amp.delenvamp);
        statement.setString(++i, env_amp.ampunit);
        statement.setDouble(++i, env_amp.tstart);
        statement.setDouble(++i, env_amp.tend);
        statement.setString(++i, env_amp.reviewtype);
        statement.setString(++i, env_amp.auth);
        statement.setLong(++i, env_amp.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Env_amp
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Env_amp> readEnv_amps(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Env_amp> results = new HashSet<Env_amp>();
    readEnv_amps(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Env_amp
   *        table.
   * @param env_amps
   * @throws SQLException
   */
  static public void readEnv_amps(Connection connection, String selectStatement,
      Set<Env_amp> env_amps) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        env_amps.add(new Env_amp(rs));
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
   * this Env_amp object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Env_amp object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "envampid, magyieldid, wfid, orid, distance, deldist, codat0, dcodat0, sspread, dsspread, bterm, dbterm, pathcorrtype, pathcor, delpathcor, sitecor, delsitecor, envamp, delenvamp, ampunit, tstart, tend, reviewtype, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(envampid)).append(", ");
    sql.append(Long.toString(magyieldid)).append(", ");
    sql.append(Long.toString(wfid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Double.toString(distance)).append(", ");
    sql.append(Double.toString(deldist)).append(", ");
    sql.append(Double.toString(codat0)).append(", ");
    sql.append(Double.toString(dcodat0)).append(", ");
    sql.append(Double.toString(sspread)).append(", ");
    sql.append(Double.toString(dsspread)).append(", ");
    sql.append(Double.toString(bterm)).append(", ");
    sql.append(Double.toString(dbterm)).append(", ");
    sql.append("'").append(pathcorrtype).append("', ");
    sql.append(Double.toString(pathcor)).append(", ");
    sql.append(Double.toString(delpathcor)).append(", ");
    sql.append(Double.toString(sitecor)).append(", ");
    sql.append(Double.toString(delsitecor)).append(", ");
    sql.append(Double.toString(envamp)).append(", ");
    sql.append(Double.toString(delenvamp)).append(", ");
    sql.append("'").append(ampunit).append("', ");
    sql.append(Double.toString(tstart)).append(", ");
    sql.append(Double.toString(tend)).append(", ");
    sql.append("'").append(reviewtype).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Env_amp in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Env_amp in the database
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
   * Generate a sql script to create a table of type Env_amp in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Env_amp in the database
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
    buf.append("envampid     number(9)            NOT NULL,\n");
    buf.append("magyieldid   number(9)            NOT NULL,\n");
    buf.append("wfid         number(9)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("distance     float(24)            NOT NULL,\n");
    buf.append("deldist      float(24)            NOT NULL,\n");
    buf.append("codat0       float(53)            NOT NULL,\n");
    buf.append("dcodat0      float(24)            NOT NULL,\n");
    buf.append("sspread      float(24)            NOT NULL,\n");
    buf.append("dsspread     float(24)            NOT NULL,\n");
    buf.append("bterm        float(24)            NOT NULL,\n");
    buf.append("dbterm       float(24)            NOT NULL,\n");
    buf.append("pathcorrtype varchar2(8)          NOT NULL,\n");
    buf.append("pathcor      float(24)            NOT NULL,\n");
    buf.append("delpathcor   float(24)            NOT NULL,\n");
    buf.append("sitecor      float(24)            NOT NULL,\n");
    buf.append("delsitecor   float(24)            NOT NULL,\n");
    buf.append("envamp       float(24)            NOT NULL,\n");
    buf.append("delenvamp    float(24)            NOT NULL,\n");
    buf.append("ampunit      varchar2(15)         NOT NULL,\n");
    buf.append("tstart       float(53)            NOT NULL,\n");
    buf.append("tend         float(53)            NOT NULL,\n");
    buf.append("reviewtype   varchar2(8)          NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (envampid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (magyieldid,orid,codat0,sspread,bterm,pathcorrtype,pathcor,sitecor,tstart,tend,auth)");
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
    return 235;
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
    return (other instanceof Env_amp) && ((Env_amp) other).envampid == envampid;
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
    return (other instanceof Env_amp) && ((Env_amp) other).magyieldid == magyieldid
        && ((Env_amp) other).orid == orid && ((Env_amp) other).codat0 == codat0
        && ((Env_amp) other).sspread == sspread && ((Env_amp) other).bterm == bterm
        && ((Env_amp) other).pathcorrtype.equals(pathcorrtype)
        && ((Env_amp) other).pathcor == pathcor && ((Env_amp) other).sitecor == sitecor
        && ((Env_amp) other).tstart == tstart && ((Env_amp) other).tend == tend
        && ((Env_amp) other).auth.equals(auth);
  }

  /**
   * Envelope amplitude identifier
   * 
   * @return envampid
   */
  public long getEnvampid() {
    return envampid;
  }

  /**
   * Envelope amplitude identifier
   * 
   * @param envampid
   * @throws IllegalArgumentException if envampid >= 1000000000
   */
  public Env_amp setEnvampid(long envampid) {
    if (envampid >= 1000000000L)
      throw new IllegalArgumentException("envampid=" + envampid + " but cannot be >= 1000000000");
    this.envampid = envampid;
    setHash(null);
    return this;
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
  public Env_amp setMagyieldid(long magyieldid) {
    if (magyieldid >= 1000000000L)
      throw new IllegalArgumentException(
          "magyieldid=" + magyieldid + " but cannot be >= 1000000000");
    this.magyieldid = magyieldid;
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
  public Env_amp setWfid(long wfid) {
    if (wfid >= 1000000000L)
      throw new IllegalArgumentException("wfid=" + wfid + " but cannot be >= 1000000000");
    this.wfid = wfid;
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
  public Env_amp setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Distance used in windowing and application of calibration parameters. May be undefined for
   * noise measurements only.
   * <p>
   * Units: km
   * 
   * @return distance
   */
  public double getDistance() {
    return distance;
  }

  /**
   * Distance used in windowing and application of calibration parameters. May be undefined for
   * noise measurements only.
   * <p>
   * Units: km
   * 
   * @param distance
   */
  public Env_amp setDistance(double distance) {
    this.distance = distance;
    setHash(null);
    return this;
  }

  /**
   * Error on distance used in windowing and application of calibration parameters.
   * <p>
   * Units: km
   * 
   * @return deldist
   */
  public double getDeldist() {
    return deldist;
  }

  /**
   * Error on distance used in windowing and application of calibration parameters.
   * <p>
   * Units: km
   * 
   * @param deldist
   */
  public Env_amp setDeldist(double deldist) {
    this.deldist = deldist;
    setHash(null);
    return this;
  }

  /**
   * Coda zero time (absolute epoch time) computed using <I>vg0</I>, <I>vg1</I>, <I>vg2</I> and
   * <I>distance</I> (Mayeda et al., 2003).
   * <p>
   * Units: s
   * 
   * @return codat0
   */
  public double getCodat0() {
    return codat0;
  }

  /**
   * Coda zero time (absolute epoch time) computed using <I>vg0</I>, <I>vg1</I>, <I>vg2</I> and
   * <I>distance</I> (Mayeda et al., 2003).
   * <p>
   * Units: s
   * 
   * @param codat0
   */
  public Env_amp setCodat0(double codat0) {
    this.codat0 = codat0;
    setHash(null);
    return this;
  }

  /**
   * Error on coda zero time <I>codat0</I>.
   * <p>
   * Units: s
   * 
   * @return dcodat0
   */
  public double getDcodat0() {
    return dcodat0;
  }

  /**
   * Error on coda zero time <I>codat0</I>.
   * <p>
   * Units: s
   * 
   * @param dcodat0
   */
  public Env_amp setDcodat0(double dcodat0) {
    this.dcodat0 = dcodat0;
    setHash(null);
    return this;
  }

  /**
   * Coda shape spreading factor computed using <I>sspread0</I>, <I>sspread1</I>, <I>sspread2</I>,
   * and <I>distance</I> (Mayeda et al., 2003); undefined for direct wave measurements.
   * 
   * @return sspread
   */
  public double getSspread() {
    return sspread;
  }

  /**
   * Coda shape spreading factor computed using <I>sspread0</I>, <I>sspread1</I>, <I>sspread2</I>,
   * and <I>distance</I> (Mayeda et al., 2003); undefined for direct wave measurements.
   * 
   * @param sspread
   */
  public Env_amp setSspread(double sspread) {
    this.sspread = sspread;
    setHash(null);
    return this;
  }

  /**
   * Error on coda shape spreading factor computed using <I>sspread0</I>, <I>sspread1</I>,
   * <I>sspread2</I> and <I>distance</I> (Mayeda et al., 2003); undefined for direct wave
   * measurements.
   * 
   * @return dsspread
   */
  public double getDsspread() {
    return dsspread;
  }

  /**
   * Error on coda shape spreading factor computed using <I>sspread0</I>, <I>sspread1</I>,
   * <I>sspread2</I> and <I>distance</I> (Mayeda et al., 2003); undefined for direct wave
   * measurements.
   * 
   * @param dsspread
   */
  public Env_amp setDsspread(double dsspread) {
    this.dsspread = dsspread;
    setHash(null);
    return this;
  }

  /**
   * Coda shape attenuation factor computed using b0, b1, b2 and distance (Mayeda et al., 2003),
   * undefined for direct wave measurements.
   * <p>
   * Units: 1/s
   * 
   * @return bterm
   */
  public double getBterm() {
    return bterm;
  }

  /**
   * Coda shape attenuation factor computed using b0, b1, b2 and distance (Mayeda et al., 2003),
   * undefined for direct wave measurements.
   * <p>
   * Units: 1/s
   * 
   * @param bterm
   */
  public Env_amp setBterm(double bterm) {
    this.bterm = bterm;
    setHash(null);
    return this;
  }

  /**
   * Error on coda shape attenuation factor <I>bterm</I>.
   * <p>
   * Units: 1/s
   * 
   * @return dbterm
   */
  public double getDbterm() {
    return dbterm;
  }

  /**
   * Error on coda shape attenuation factor <I>bterm</I>.
   * <p>
   * Units: 1/s
   * 
   * @param dbterm
   */
  public Env_amp setDbterm(double dbterm) {
    this.dbterm = dbterm;
    setHash(null);
    return this;
  }

  /**
   * Path correction method, note that the path correction type is indicated within
   * <I>wmodel_name</I> field in the <B>yield_fit</B> table.
   * 
   * @return pathcorrtype
   */
  public String getPathcorrtype() {
    return pathcorrtype;
  }

  /**
   * Path correction method, note that the path correction type is indicated within
   * <I>wmodel_name</I> field in the <B>yield_fit</B> table.
   * 
   * @param pathcorrtype
   * @throws IllegalArgumentException if pathcorrtype.length() >= 8
   */
  public Env_amp setPathcorrtype(String pathcorrtype) {
    if (pathcorrtype.length() > 8)
      throw new IllegalArgumentException(
          String.format("pathcorrtype.length() cannot be > 8.  pathcorrtype=%s", pathcorrtype));
    this.pathcorrtype = pathcorrtype;
    setHash(null);
    return this;
  }

  /**
   * Log path correction applied to obtain source spectral estimate from raw amplitude.
   * 
   * @return pathcor
   */
  public double getPathcor() {
    return pathcor;
  }

  /**
   * Log path correction applied to obtain source spectral estimate from raw amplitude.
   * 
   * @param pathcor
   */
  public Env_amp setPathcor(double pathcor) {
    this.pathcor = pathcor;
    setHash(null);
    return this;
  }

  /**
   * Error on log path correction applied to obtain source spectral estimate from raw amplitude.
   * 
   * @return delpathcor
   */
  public double getDelpathcor() {
    return delpathcor;
  }

  /**
   * Error on log path correction applied to obtain source spectral estimate from raw amplitude.
   * 
   * @param delpathcor
   */
  public Env_amp setDelpathcor(double delpathcor) {
    this.delpathcor = delpathcor;
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
  public Env_amp setSitecor(double sitecor) {
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
  public Env_amp setDelsitecor(double delsitecor) {
    this.delsitecor = delsitecor;
    setHash(null);
    return this;
  }

  /**
   * Log envelope amplitude; can be a noise measurement, a direct or coda wave measurement, or a
   * source spectral estimate based on direct or coda wave measurements.
   * <p>
   * Units: log10(m/s), log10(nm), log10(counts)
   * 
   * @return envamp
   */
  public double getEnvamp() {
    return envamp;
  }

  /**
   * Log envelope amplitude; can be a noise measurement, a direct or coda wave measurement, or a
   * source spectral estimate based on direct or coda wave measurements.
   * <p>
   * Units: log10(m/s), log10(nm), log10(counts)
   * 
   * @param envamp
   */
  public Env_amp setEnvamp(double envamp) {
    this.envamp = envamp;
    setHash(null);
    return this;
  }

  /**
   * Log envelope amplitude error.
   * <p>
   * Units: log10(m/s), log10(nm), log10(counts)
   * 
   * @return delenvamp
   */
  public double getDelenvamp() {
    return delenvamp;
  }

  /**
   * Log envelope amplitude error.
   * <p>
   * Units: log10(m/s), log10(nm), log10(counts)
   * 
   * @param delenvamp
   */
  public Env_amp setDelenvamp(double delenvamp) {
    this.delenvamp = delenvamp;
    setHash(null);
    return this;
  }

  /**
   * Amplitude unit.
   * 
   * @return ampunit
   */
  public String getAmpunit() {
    return ampunit;
  }

  /**
   * Amplitude unit.
   * 
   * @param ampunit
   * @throws IllegalArgumentException if ampunit.length() >= 15
   */
  public Env_amp setAmpunit(String ampunit) {
    if (ampunit.length() > 15)
      throw new IllegalArgumentException(
          String.format("ampunit.length() cannot be > 15.  ampunit=%s", ampunit));
    this.ampunit = ampunit;
    setHash(null);
    return this;
  }

  /**
   * Window start time (absolute epoch time) for amplitude measurement. See also <I>tend</I> and
   * <I>codat0</I>.
   * <p>
   * Units: s
   * 
   * @return tstart
   */
  public double getTstart() {
    return tstart;
  }

  /**
   * Window start time (absolute epoch time) for amplitude measurement. See also <I>tend</I> and
   * <I>codat0</I>.
   * <p>
   * Units: s
   * 
   * @param tstart
   */
  public Env_amp setTstart(double tstart) {
    this.tstart = tstart;
    setHash(null);
    return this;
  }

  /**
   * Window end time (absolute epoch time) for amplitude measurement. (See <I>tstart</I> and
   * <I>codat0</I>).
   * <p>
   * Units: s
   * 
   * @return tend
   */
  public double getTend() {
    return tend;
  }

  /**
   * Window end time (absolute epoch time) for amplitude measurement. (See <I>tstart</I> and
   * <I>codat0</I>).
   * <p>
   * Units: s
   * 
   * @param tend
   */
  public Env_amp setTend(double tend) {
    this.tend = tend;
    setHash(null);
    return this;
  }

  /**
   * Manual coda window review action; recommended types are: reviewed - coda window reviewed,
   * modified - coda window reviewed and modified, none - coda window not reviewed.
   * 
   * @return reviewtype
   */
  public String getReviewtype() {
    return reviewtype;
  }

  /**
   * Manual coda window review action; recommended types are: reviewed - coda window reviewed,
   * modified - coda window reviewed and modified, none - coda window not reviewed.
   * 
   * @param reviewtype
   * @throws IllegalArgumentException if reviewtype.length() >= 8
   */
  public Env_amp setReviewtype(String reviewtype) {
    if (reviewtype.length() > 8)
      throw new IllegalArgumentException(
          String.format("reviewtype.length() cannot be > 8.  reviewtype=%s", reviewtype));
    this.reviewtype = reviewtype;
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
  public Env_amp setAuth(String auth) {
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
  public Env_amp setCommid(long commid) {
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
