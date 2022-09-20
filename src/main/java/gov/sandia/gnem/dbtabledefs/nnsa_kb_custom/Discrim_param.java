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
 * discrim_param
 */
public class Discrim_param extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Parameter set identification number used when making raw amplitude measurements.
   */
  private long paramsetid;

  static final public long PARAMSETID_NA = Long.MIN_VALUE;

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   */
  private long polyid;

  static final public long POLYID_NA = -1;

  /**
   * Unique identifier for version of a set of parameter files.
   */
  private long versionid;

  static final public long VERSIONID_NA = -1;

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
   * Low cut frequency given as fraction of <I>minlowpassfreq</I>. This is the low-frequency
   * termination of the entire pass band of the filter, including the low-frequency taper. For the
   * actural filter for a particular waveform, the resulting absolute limit may be higher, due to
   * short segment (see <I>tfactor</I>), but will always be the same fraction of the final
   * <I>minlowpassfreq</I>
   */
  private double lowcutfrac;

  static final public double LOWCUTFRAC_NA = Double.NaN;

  /**
   * The minimum frequency used as the low pass for instrument correction.
   * <p>
   * Units: Hz
   */
  private double minlowpassfreq;

  static final public double MINLOWPASSFREQ_NA = Double.NaN;

  /**
   * High pass given as fraction of Nyquist (obtained from nominal sample rate). This is the
   * high-frequency end of the flat pass band of the filter.
   */
  private double highpassfrac;

  static final public double HIGHPASSFRAC_NA = Double.NaN;

  /**
   * High cut given as fraction of Nyquist (obtained from nominal sample rate). This is the
   * high-frequency termination of the entire pass band of the filter, including the high frequency
   * taper
   */
  private double highcutfrac;

  static final public double HIGHCUTFRAC_NA = Double.NaN;

  /**
   * Beginning Pn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   */
  private double pngv1;

  static final public double PNGV1_NA = Double.NaN;

  /**
   * Ending Pn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   */
  private double pngv2;

  static final public double PNGV2_NA = Double.NaN;

  /**
   * Pn intercept time used to make raw amplitude measurements.
   * <p>
   * Units: s
   */
  private double pnint;

  static final public double PNINT_NA = Double.NaN;

  /**
   * Beginning Pg group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   */
  private double pggv1;

  static final public double PGGV1_NA = Double.NaN;

  /**
   * Ending Pg group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   */
  private double pggv2;

  static final public double PGGV2_NA = Double.NaN;

  /**
   * Pg intercept time used to make raw amplitude measurements.
   * <p>
   * Units: s
   */
  private double pgint;

  static final public double PGINT_NA = Double.NaN;

  /**
   * Beginning Sn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   */
  private double sngv1;

  static final public double SNGV1_NA = Double.NaN;

  /**
   * Ending Sn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   */
  private double sngv2;

  static final public double SNGV2_NA = Double.NaN;

  /**
   * Sn intercept time used to make raw amplitude measurements.
   * <p>
   * Units: s
   */
  private double snint;

  static final public double SNINT_NA = Double.NaN;

  /**
   * Beginning Lg group velocity parameter used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   */
  private double lggv1;

  static final public double LGGV1_NA = Double.NaN;

  /**
   * Ending Lg group velocity parameter used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   */
  private double lggv2;

  static final public double LGGV2_NA = Double.NaN;

  /**
   * Lg intercept time parameter used to make raw amplitude measurements.
   * <p>
   * Units: s
   */
  private double lgint;

  static final public double LGINT_NA = Double.NaN;

  /**
   * The minimum number of cycles in the window before calculating an amplitude. A typical value is
   * 2. <I>tractor</I> divided by the window length will replace <I>minlowpassfreq</I> if it is
   * larger than <I>minlowpassfreq</I>
   */
  private double tfactor;

  static final public double TFACTOR_NA = Double.NaN;

  /**
   * unit-less fraction between that is applied to the cut waveform before the fast-Fourier
   * transform.
   */
  private double taperwidth;

  static final public double TAPERWIDTH_NA = Double.NaN;

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
    columns.add("paramsetid", Columns.FieldType.LONG, "%d");
    columns.add("polyid", Columns.FieldType.LONG, "%d");
    columns.add("versionid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("lowcutfrac", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("minlowpassfreq", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("highpassfrac", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("highcutfrac", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("pngv1", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("pngv2", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("pnint", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("pggv1", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("pggv2", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("pgint", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("sngv1", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("sngv2", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("snint", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("lggv1", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("lggv2", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("lgint", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("tfactor", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("taperwidth", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Discrim_param(long paramsetid, long polyid, long versionid, String sta, String chan,
      double lowcutfrac, double minlowpassfreq, double highpassfrac, double highcutfrac,
      double pngv1, double pngv2, double pnint, double pggv1, double pggv2, double pgint,
      double sngv1, double sngv2, double snint, double lggv1, double lggv2, double lgint,
      double tfactor, double taperwidth, String auth) {
    setValues(paramsetid, polyid, versionid, sta, chan, lowcutfrac, minlowpassfreq, highpassfrac,
        highcutfrac, pngv1, pngv2, pnint, pggv1, pggv2, pgint, sngv1, sngv2, snint, lggv1, lggv2,
        lgint, tfactor, taperwidth, auth);
  }

  private void setValues(long paramsetid, long polyid, long versionid, String sta, String chan,
      double lowcutfrac, double minlowpassfreq, double highpassfrac, double highcutfrac,
      double pngv1, double pngv2, double pnint, double pggv1, double pggv2, double pgint,
      double sngv1, double sngv2, double snint, double lggv1, double lggv2, double lgint,
      double tfactor, double taperwidth, String auth) {
    this.paramsetid = paramsetid;
    this.polyid = polyid;
    this.versionid = versionid;
    this.sta = sta;
    this.chan = chan;
    this.lowcutfrac = lowcutfrac;
    this.minlowpassfreq = minlowpassfreq;
    this.highpassfrac = highpassfrac;
    this.highcutfrac = highcutfrac;
    this.pngv1 = pngv1;
    this.pngv2 = pngv2;
    this.pnint = pnint;
    this.pggv1 = pggv1;
    this.pggv2 = pggv2;
    this.pgint = pgint;
    this.sngv1 = sngv1;
    this.sngv2 = sngv2;
    this.snint = snint;
    this.lggv1 = lggv1;
    this.lggv2 = lggv2;
    this.lgint = lgint;
    this.tfactor = tfactor;
    this.taperwidth = taperwidth;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Discrim_param(Discrim_param other) {
    this.paramsetid = other.getParamsetid();
    this.polyid = other.getPolyid();
    this.versionid = other.getVersionid();
    this.sta = other.getSta();
    this.chan = other.getChan();
    this.lowcutfrac = other.getLowcutfrac();
    this.minlowpassfreq = other.getMinlowpassfreq();
    this.highpassfrac = other.getHighpassfrac();
    this.highcutfrac = other.getHighcutfrac();
    this.pngv1 = other.getPngv1();
    this.pngv2 = other.getPngv2();
    this.pnint = other.getPnint();
    this.pggv1 = other.getPggv1();
    this.pggv2 = other.getPggv2();
    this.pgint = other.getPgint();
    this.sngv1 = other.getSngv1();
    this.sngv2 = other.getSngv2();
    this.snint = other.getSnint();
    this.lggv1 = other.getLggv1();
    this.lggv2 = other.getLggv2();
    this.lgint = other.getLgint();
    this.tfactor = other.getTfactor();
    this.taperwidth = other.getTaperwidth();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Discrim_param() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(PARAMSETID_NA, POLYID_NA, VERSIONID_NA, STA_NA, CHAN_NA, LOWCUTFRAC_NA,
        MINLOWPASSFREQ_NA, HIGHPASSFRAC_NA, HIGHCUTFRAC_NA, PNGV1_NA, PNGV2_NA, PNINT_NA, PGGV1_NA,
        PGGV2_NA, PGINT_NA, SNGV1_NA, SNGV2_NA, SNINT_NA, LGGV1_NA, LGGV2_NA, LGINT_NA, TFACTOR_NA,
        TAPERWIDTH_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "chan":
        return chan;
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
      case "minlowpassfreq":
        return minlowpassfreq;
      case "highpassfrac":
        return highpassfrac;
      case "highcutfrac":
        return highcutfrac;
      case "pngv1":
        return pngv1;
      case "pngv2":
        return pngv2;
      case "pnint":
        return pnint;
      case "pggv1":
        return pggv1;
      case "pggv2":
        return pggv2;
      case "pgint":
        return pgint;
      case "sngv1":
        return sngv1;
      case "sngv2":
        return sngv2;
      case "snint":
        return snint;
      case "lggv1":
        return lggv1;
      case "lggv2":
        return lggv2;
      case "lgint":
        return lgint;
      case "tfactor":
        return tfactor;
      case "taperwidth":
        return taperwidth;
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
      case "minlowpassfreq":
        minlowpassfreq = value;
        break;
      case "highpassfrac":
        highpassfrac = value;
        break;
      case "highcutfrac":
        highcutfrac = value;
        break;
      case "pngv1":
        pngv1 = value;
        break;
      case "pngv2":
        pngv2 = value;
        break;
      case "pnint":
        pnint = value;
        break;
      case "pggv1":
        pggv1 = value;
        break;
      case "pggv2":
        pggv2 = value;
        break;
      case "pgint":
        pgint = value;
        break;
      case "sngv1":
        sngv1 = value;
        break;
      case "sngv2":
        sngv2 = value;
        break;
      case "snint":
        snint = value;
        break;
      case "lggv1":
        lggv1 = value;
        break;
      case "lggv2":
        lggv2 = value;
        break;
      case "lgint":
        lgint = value;
        break;
      case "tfactor":
        tfactor = value;
        break;
      case "taperwidth":
        taperwidth = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "paramsetid":
        return paramsetid;
      case "polyid":
        return polyid;
      case "versionid":
        return versionid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "paramsetid":
        paramsetid = value;
        break;
      case "polyid":
        polyid = value;
        break;
      case "versionid":
        versionid = value;
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
  public Discrim_param(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Discrim_param(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), readString(input), readString(input),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Discrim_param(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), readString(input), readString(input),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Discrim_param(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Discrim_param(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getDouble(offset + 17), input.getDouble(offset + 18),
        input.getDouble(offset + 19), input.getDouble(offset + 20), input.getDouble(offset + 21),
        input.getDouble(offset + 22), input.getDouble(offset + 23), input.getString(offset + 24));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[24];
    values[0] = paramsetid;
    values[1] = polyid;
    values[2] = versionid;
    values[3] = sta;
    values[4] = chan;
    values[5] = lowcutfrac;
    values[6] = minlowpassfreq;
    values[7] = highpassfrac;
    values[8] = highcutfrac;
    values[9] = pngv1;
    values[10] = pngv2;
    values[11] = pnint;
    values[12] = pggv1;
    values[13] = pggv2;
    values[14] = pgint;
    values[15] = sngv1;
    values[16] = sngv2;
    values[17] = snint;
    values[18] = lggv1;
    values[19] = lggv2;
    values[20] = lgint;
    values[21] = tfactor;
    values[22] = taperwidth;
    values[23] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[25];
    values[0] = paramsetid;
    values[1] = polyid;
    values[2] = versionid;
    values[3] = sta;
    values[4] = chan;
    values[5] = lowcutfrac;
    values[6] = minlowpassfreq;
    values[7] = highpassfrac;
    values[8] = highcutfrac;
    values[9] = pngv1;
    values[10] = pngv2;
    values[11] = pnint;
    values[12] = pggv1;
    values[13] = pggv2;
    values[14] = pgint;
    values[15] = sngv1;
    values[16] = sngv2;
    values[17] = snint;
    values[18] = lggv1;
    values[19] = lggv2;
    values[20] = lgint;
    values[21] = tfactor;
    values[22] = taperwidth;
    values[23] = auth;
    values[24] = lddate;
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
    output.writeLong(paramsetid);
    output.writeLong(polyid);
    output.writeLong(versionid);
    writeString(output, sta);
    writeString(output, chan);
    output.writeDouble(lowcutfrac);
    output.writeDouble(minlowpassfreq);
    output.writeDouble(highpassfrac);
    output.writeDouble(highcutfrac);
    output.writeDouble(pngv1);
    output.writeDouble(pngv2);
    output.writeDouble(pnint);
    output.writeDouble(pggv1);
    output.writeDouble(pggv2);
    output.writeDouble(pgint);
    output.writeDouble(sngv1);
    output.writeDouble(sngv2);
    output.writeDouble(snint);
    output.writeDouble(lggv1);
    output.writeDouble(lggv2);
    output.writeDouble(lgint);
    output.writeDouble(tfactor);
    output.writeDouble(taperwidth);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(paramsetid);
    output.putLong(polyid);
    output.putLong(versionid);
    writeString(output, sta);
    writeString(output, chan);
    output.putDouble(lowcutfrac);
    output.putDouble(minlowpassfreq);
    output.putDouble(highpassfrac);
    output.putDouble(highcutfrac);
    output.putDouble(pngv1);
    output.putDouble(pngv2);
    output.putDouble(pnint);
    output.putDouble(pggv1);
    output.putDouble(pggv2);
    output.putDouble(pgint);
    output.putDouble(sngv1);
    output.putDouble(sngv2);
    output.putDouble(snint);
    output.putDouble(lggv1);
    output.putDouble(lggv2);
    output.putDouble(lgint);
    output.putDouble(tfactor);
    output.putDouble(taperwidth);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Discrim_param objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Discrim_param objects.
   * @throws IOException
   */
  static public void readDiscrim_params(BufferedReader input, Collection<Discrim_param> rows)
      throws IOException {
    String[] saved = Discrim_param.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Discrim_param
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Discrim_param(new Scanner(line)));
    }
    input.close();
    Discrim_param.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Discrim_param objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Discrim_param objects.
   * @throws IOException
   */
  static public void readDiscrim_params(File inputFile, Collection<Discrim_param> rows)
      throws IOException {
    readDiscrim_params(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Discrim_param objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Discrim_param objects.
   * @throws IOException
   */
  static public void readDiscrim_params(InputStream inputStream, Collection<Discrim_param> rows)
      throws IOException {
    readDiscrim_params(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Discrim_param objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Discrim_param objects
   * @throws IOException
   */
  static public Set<Discrim_param> readDiscrim_params(BufferedReader input) throws IOException {
    Set<Discrim_param> rows = new LinkedHashSet<Discrim_param>();
    readDiscrim_params(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Discrim_param objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Discrim_param objects
   * @throws IOException
   */
  static public Set<Discrim_param> readDiscrim_params(File inputFile) throws IOException {
    return readDiscrim_params(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Discrim_param objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Discrim_param objects
   * @throws IOException
   */
  static public Set<Discrim_param> readDiscrim_params(InputStream input) throws IOException {
    return readDiscrim_params(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Discrim_param objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param discrim_params the Discrim_param objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Discrim_param> discrim_params)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Discrim_param discrim_param : discrim_params)
      discrim_param.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Discrim_param objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param discrim_params the Discrim_param objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Discrim_param> discrim_params, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName
          + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Discrim_param discrim_param : discrim_params) {
        int i = 0;
        statement.setLong(++i, discrim_param.paramsetid);
        statement.setLong(++i, discrim_param.polyid);
        statement.setLong(++i, discrim_param.versionid);
        statement.setString(++i, discrim_param.sta);
        statement.setString(++i, discrim_param.chan);
        statement.setDouble(++i, discrim_param.lowcutfrac);
        statement.setDouble(++i, discrim_param.minlowpassfreq);
        statement.setDouble(++i, discrim_param.highpassfrac);
        statement.setDouble(++i, discrim_param.highcutfrac);
        statement.setDouble(++i, discrim_param.pngv1);
        statement.setDouble(++i, discrim_param.pngv2);
        statement.setDouble(++i, discrim_param.pnint);
        statement.setDouble(++i, discrim_param.pggv1);
        statement.setDouble(++i, discrim_param.pggv2);
        statement.setDouble(++i, discrim_param.pgint);
        statement.setDouble(++i, discrim_param.sngv1);
        statement.setDouble(++i, discrim_param.sngv2);
        statement.setDouble(++i, discrim_param.snint);
        statement.setDouble(++i, discrim_param.lggv1);
        statement.setDouble(++i, discrim_param.lggv2);
        statement.setDouble(++i, discrim_param.lgint);
        statement.setDouble(++i, discrim_param.tfactor);
        statement.setDouble(++i, discrim_param.taperwidth);
        statement.setString(++i, discrim_param.auth);
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
   *        Discrim_param table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Discrim_param> readDiscrim_params(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Discrim_param> results = new HashSet<Discrim_param>();
    readDiscrim_params(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Discrim_param table.
   * @param discrim_params
   * @throws SQLException
   */
  static public void readDiscrim_params(Connection connection, String selectStatement,
      Set<Discrim_param> discrim_params) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        discrim_params.add(new Discrim_param(rs));
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
   * this Discrim_param object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Discrim_param object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "paramsetid, polyid, versionid, sta, chan, lowcutfrac, minlowpassfreq, highpassfrac, highcutfrac, pngv1, pngv2, pnint, pggv1, pggv2, pgint, sngv1, sngv2, snint, lggv1, lggv2, lgint, tfactor, taperwidth, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(paramsetid)).append(", ");
    sql.append(Long.toString(polyid)).append(", ");
    sql.append(Long.toString(versionid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(chan).append("', ");
    sql.append(Double.toString(lowcutfrac)).append(", ");
    sql.append(Double.toString(minlowpassfreq)).append(", ");
    sql.append(Double.toString(highpassfrac)).append(", ");
    sql.append(Double.toString(highcutfrac)).append(", ");
    sql.append(Double.toString(pngv1)).append(", ");
    sql.append(Double.toString(pngv2)).append(", ");
    sql.append(Double.toString(pnint)).append(", ");
    sql.append(Double.toString(pggv1)).append(", ");
    sql.append(Double.toString(pggv2)).append(", ");
    sql.append(Double.toString(pgint)).append(", ");
    sql.append(Double.toString(sngv1)).append(", ");
    sql.append(Double.toString(sngv2)).append(", ");
    sql.append(Double.toString(snint)).append(", ");
    sql.append(Double.toString(lggv1)).append(", ");
    sql.append(Double.toString(lggv2)).append(", ");
    sql.append(Double.toString(lgint)).append(", ");
    sql.append(Double.toString(tfactor)).append(", ");
    sql.append(Double.toString(taperwidth)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Discrim_param in the database. Primary and unique keys are set, if
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
   * Create a table of type Discrim_param in the database
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
   * Generate a sql script to create a table of type Discrim_param in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Discrim_param in the database
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
    buf.append("paramsetid   number(9)            NOT NULL,\n");
    buf.append("polyid       number(9)            NOT NULL,\n");
    buf.append("versionid    number(9)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("lowcutfrac   float(24)            NOT NULL,\n");
    buf.append("minlowpassfreq float(24)            NOT NULL,\n");
    buf.append("highpassfrac float(24)            NOT NULL,\n");
    buf.append("highcutfrac  float(24)            NOT NULL,\n");
    buf.append("pngv1        float(24)            NOT NULL,\n");
    buf.append("pngv2        float(24)            NOT NULL,\n");
    buf.append("pnint        float(24)            NOT NULL,\n");
    buf.append("pggv1        float(24)            NOT NULL,\n");
    buf.append("pggv2        float(24)            NOT NULL,\n");
    buf.append("pgint        float(24)            NOT NULL,\n");
    buf.append("sngv1        float(24)            NOT NULL,\n");
    buf.append("sngv2        float(24)            NOT NULL,\n");
    buf.append("snint        float(24)            NOT NULL,\n");
    buf.append("lggv1        float(24)            NOT NULL,\n");
    buf.append("lggv2        float(24)            NOT NULL,\n");
    buf.append("lgint        float(24)            NOT NULL,\n");
    buf.append("tfactor      float(24)            NOT NULL,\n");
    buf.append("taperwidth   float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (paramsetid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (polyid,versionid,sta,chan)");
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
    return 214;
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
    return (other instanceof Discrim_param) && ((Discrim_param) other).paramsetid == paramsetid;
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
    return (other instanceof Discrim_param) && ((Discrim_param) other).polyid == polyid
        && ((Discrim_param) other).versionid == versionid && ((Discrim_param) other).sta.equals(sta)
        && ((Discrim_param) other).chan.equals(chan);
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
  public Discrim_param setParamsetid(long paramsetid) {
    if (paramsetid >= 1000000000L)
      throw new IllegalArgumentException(
          "paramsetid=" + paramsetid + " but cannot be >= 1000000000");
    this.paramsetid = paramsetid;
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
  public Discrim_param setPolyid(long polyid) {
    if (polyid >= 1000000000L)
      throw new IllegalArgumentException("polyid=" + polyid + " but cannot be >= 1000000000");
    this.polyid = polyid;
    setHash(null);
    return this;
  }

  /**
   * Unique identifier for version of a set of parameter files.
   * 
   * @return versionid
   */
  public long getVersionid() {
    return versionid;
  }

  /**
   * Unique identifier for version of a set of parameter files.
   * 
   * @param versionid
   * @throws IllegalArgumentException if versionid >= 1000000000
   */
  public Discrim_param setVersionid(long versionid) {
    if (versionid >= 1000000000L)
      throw new IllegalArgumentException("versionid=" + versionid + " but cannot be >= 1000000000");
    this.versionid = versionid;
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
  public Discrim_param setSta(String sta) {
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
  public Discrim_param setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
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
  public Discrim_param setLowcutfrac(double lowcutfrac) {
    this.lowcutfrac = lowcutfrac;
    setHash(null);
    return this;
  }

  /**
   * The minimum frequency used as the low pass for instrument correction.
   * <p>
   * Units: Hz
   * 
   * @return minlowpassfreq
   */
  public double getMinlowpassfreq() {
    return minlowpassfreq;
  }

  /**
   * The minimum frequency used as the low pass for instrument correction.
   * <p>
   * Units: Hz
   * 
   * @param minlowpassfreq
   */
  public Discrim_param setMinlowpassfreq(double minlowpassfreq) {
    this.minlowpassfreq = minlowpassfreq;
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
  public Discrim_param setHighpassfrac(double highpassfrac) {
    this.highpassfrac = highpassfrac;
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
  public Discrim_param setHighcutfrac(double highcutfrac) {
    this.highcutfrac = highcutfrac;
    setHash(null);
    return this;
  }

  /**
   * Beginning Pn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @return pngv1
   */
  public double getPngv1() {
    return pngv1;
  }

  /**
   * Beginning Pn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @param pngv1
   */
  public Discrim_param setPngv1(double pngv1) {
    this.pngv1 = pngv1;
    setHash(null);
    return this;
  }

  /**
   * Ending Pn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @return pngv2
   */
  public double getPngv2() {
    return pngv2;
  }

  /**
   * Ending Pn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @param pngv2
   */
  public Discrim_param setPngv2(double pngv2) {
    this.pngv2 = pngv2;
    setHash(null);
    return this;
  }

  /**
   * Pn intercept time used to make raw amplitude measurements.
   * <p>
   * Units: s
   * 
   * @return pnint
   */
  public double getPnint() {
    return pnint;
  }

  /**
   * Pn intercept time used to make raw amplitude measurements.
   * <p>
   * Units: s
   * 
   * @param pnint
   */
  public Discrim_param setPnint(double pnint) {
    this.pnint = pnint;
    setHash(null);
    return this;
  }

  /**
   * Beginning Pg group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @return pggv1
   */
  public double getPggv1() {
    return pggv1;
  }

  /**
   * Beginning Pg group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @param pggv1
   */
  public Discrim_param setPggv1(double pggv1) {
    this.pggv1 = pggv1;
    setHash(null);
    return this;
  }

  /**
   * Ending Pg group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @return pggv2
   */
  public double getPggv2() {
    return pggv2;
  }

  /**
   * Ending Pg group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @param pggv2
   */
  public Discrim_param setPggv2(double pggv2) {
    this.pggv2 = pggv2;
    setHash(null);
    return this;
  }

  /**
   * Pg intercept time used to make raw amplitude measurements.
   * <p>
   * Units: s
   * 
   * @return pgint
   */
  public double getPgint() {
    return pgint;
  }

  /**
   * Pg intercept time used to make raw amplitude measurements.
   * <p>
   * Units: s
   * 
   * @param pgint
   */
  public Discrim_param setPgint(double pgint) {
    this.pgint = pgint;
    setHash(null);
    return this;
  }

  /**
   * Beginning Sn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @return sngv1
   */
  public double getSngv1() {
    return sngv1;
  }

  /**
   * Beginning Sn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @param sngv1
   */
  public Discrim_param setSngv1(double sngv1) {
    this.sngv1 = sngv1;
    setHash(null);
    return this;
  }

  /**
   * Ending Sn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @return sngv2
   */
  public double getSngv2() {
    return sngv2;
  }

  /**
   * Ending Sn group velocity used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @param sngv2
   */
  public Discrim_param setSngv2(double sngv2) {
    this.sngv2 = sngv2;
    setHash(null);
    return this;
  }

  /**
   * Sn intercept time used to make raw amplitude measurements.
   * <p>
   * Units: s
   * 
   * @return snint
   */
  public double getSnint() {
    return snint;
  }

  /**
   * Sn intercept time used to make raw amplitude measurements.
   * <p>
   * Units: s
   * 
   * @param snint
   */
  public Discrim_param setSnint(double snint) {
    this.snint = snint;
    setHash(null);
    return this;
  }

  /**
   * Beginning Lg group velocity parameter used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @return lggv1
   */
  public double getLggv1() {
    return lggv1;
  }

  /**
   * Beginning Lg group velocity parameter used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @param lggv1
   */
  public Discrim_param setLggv1(double lggv1) {
    this.lggv1 = lggv1;
    setHash(null);
    return this;
  }

  /**
   * Ending Lg group velocity parameter used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @return lggv2
   */
  public double getLggv2() {
    return lggv2;
  }

  /**
   * Ending Lg group velocity parameter used to make raw amplitude measurements.
   * <p>
   * Units: km/s
   * 
   * @param lggv2
   */
  public Discrim_param setLggv2(double lggv2) {
    this.lggv2 = lggv2;
    setHash(null);
    return this;
  }

  /**
   * Lg intercept time parameter used to make raw amplitude measurements.
   * <p>
   * Units: s
   * 
   * @return lgint
   */
  public double getLgint() {
    return lgint;
  }

  /**
   * Lg intercept time parameter used to make raw amplitude measurements.
   * <p>
   * Units: s
   * 
   * @param lgint
   */
  public Discrim_param setLgint(double lgint) {
    this.lgint = lgint;
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
  public Discrim_param setTfactor(double tfactor) {
    this.tfactor = tfactor;
    setHash(null);
    return this;
  }

  /**
   * unit-less fraction between that is applied to the cut waveform before the fast-Fourier
   * transform.
   * 
   * @return taperwidth
   */
  public double getTaperwidth() {
    return taperwidth;
  }

  /**
   * unit-less fraction between that is applied to the cut waveform before the fast-Fourier
   * transform.
   * 
   * @param taperwidth
   */
  public Discrim_param setTaperwidth(double taperwidth) {
    this.taperwidth = taperwidth;
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
  public Discrim_param setAuth(String auth) {
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
