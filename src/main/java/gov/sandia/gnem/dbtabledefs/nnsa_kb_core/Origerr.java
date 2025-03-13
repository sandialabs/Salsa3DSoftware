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
package gov.sandia.gnem.dbtabledefs.nnsa_kb_core;

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

import gov.sandia.gmp.util.testingbuffer.TestBuffer;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * origerr
 */
public class Origerr extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Origin identifier that relates a record in these tables to a record in the <B>origin</B> table
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   */
  private double sxx;

  static final public double SXX_NA = -9999999999.999;

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   */
  private double syy;

  static final public double SYY_NA = -9999999999.999;

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   */
  private double szz;

  static final public double SZZ_NA = -9999999999.999;

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: s^2
   */
  private double stt;

  static final public double STT_NA = -9999999999.999;

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   */
  private double sxy;

  static final public double SXY_NA = -9999999999.999;

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   */
  private double sxz;

  static final public double SXZ_NA = -9999999999.999;

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   */
  private double syz;

  static final public double SYZ_NA = -9999999999.999;

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km/s
   */
  private double stx;

  static final public double STX_NA = -9999999999.999;

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km/s
   */
  private double sty;

  static final public double STY_NA = -9999999999.999;

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km/s
   */
  private double stz;

  static final public double STZ_NA = -9999999999.999;

  /**
   * Standard error of one observation. This column is derived from the discrepancies in the arrival
   * times of the phases used to locate an event. This column is defined as the square root of the
   * sum of the squares of the time residuals divided by the number of degrees of freedom. The
   * latter is the number of defining observations [<I>ndef</I> in <B>origin</B>] minus the
   * dimension of the system solved (4 if depth is allowed to be a free variable, 3 if depth is
   * constrained).
   */
  private double sdobs;

  static final public double SDOBS_NA = -1;

  /**
   * Semi-major axis of error ellipse for a given confidence. This value is the length of the
   * semi-major axis of the location error ellipse. The value is found by projecting the covariance
   * matrix onto the horizontal plane. The level of confidence is specified by <I>conf</I> (see
   * <I>sdepth</I>, <I>sminax</I>, and <I>sxx</I>, <I>syy</I>, <I>szz</I>, <I>stt</I>, <I>sxy</I>,
   * <I>sxz</I> <I>syz</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>,).
   * <p>
   * Units: km
   */
  private double smajax;

  static final public double SMAJAX_NA = -1;

  /**
   * Semi-minor axis of error ellipse. This value is the length of the semi-minor axis of the
   * location error ellipse. The value is found by projecting the covariance matrix onto the
   * horizontal plane. The level of confidence is specified by <I>conf</I> (see <I>sdepth</I>,
   * <I>smajax</I>, and <I>sxx</I>, <I>syy</I>, <I>szz</I>, <I>stt</I>, <I>sxy</I>, <I>sxz</I>
   * <I>syz</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>,).
   * <p>
   * Units: km
   */
  private double sminax;

  static final public double SMINAX_NA = -1;

  /**
   * Strike of major axis of error ellipse. This column is the strike of the semi-major axis of the
   * location error ellipse, measured in degrees clockwise from the North (see <I>smajax</I>).
   * <p>
   * Units: degree
   */
  private double strike;

  static final public double STRIKE_NA = -1;

  /**
   * Depth error. This is the maximum error of a depth estimate for a level of confidence given by
   * <I>conf</I> (see <I>smajax</I>, <I>sminax</I>, and <I>sxx</I>, <I>syy</I>, <I>szz</I>,
   * <I>stt</I>, <I>sxy</I>, <I>sxz</I> <I>syz</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>,)
   * <p>
   * Units: km
   */
  private double sdepth;

  static final public double SDEPTH_NA = -9999999999.999;

  /**
   * Origin time error. This column denotes the time uncertainty that accompanies the average error
   * ellipse location (see <I>smajax</I>, <I>sminax</I>, and <I>sdepth</I>).
   * <p>
   * Units: s
   */
  private double stime;

  static final public double STIME_NA = -1;

  /**
   * Confidence measure for a particular event identification method
   */
  private double conf;

  static final public double CONF_NA = -1;

  /**
   * Comment identifier. This value is a key that points to free-form comments entered in the
   * <B>remark</B> table. These comments store additional information about a record in another
   * table. The <B>remark</B> table can have many records with the same <I>commid</I> and different
   * <I>lineno</I>, but the same <I>commid</I> will appear in only one other record among the rest
   * of the tables in the database (see <I>lineno</I>).
   */
  private long commid;

  static final public long COMMID_NA = -1;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("sxx", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("syy", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("szz", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("stt", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("sxy", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("sxz", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("syz", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("stx", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("sty", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("stz", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("sdobs", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("smajax", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("sminax", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("strike", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("sdepth", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("stime", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("conf", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Origerr(long orid, double sxx, double syy, double szz, double stt, double sxy, double sxz,
      double syz, double stx, double sty, double stz, double sdobs, double smajax, double sminax,
      double strike, double sdepth, double stime, double conf, long commid) {
    setValues(orid, sxx, syy, szz, stt, sxy, sxz, syz, stx, sty, stz, sdobs, smajax, sminax, strike,
        sdepth, stime, conf, commid);
  }

  private void setValues(long orid, double sxx, double syy, double szz, double stt, double sxy,
      double sxz, double syz, double stx, double sty, double stz, double sdobs, double smajax,
      double sminax, double strike, double sdepth, double stime, double conf, long commid) {
    this.orid = orid;
    this.sxx = sxx;
    this.syy = syy;
    this.szz = szz;
    this.stt = stt;
    this.sxy = sxy;
    this.sxz = sxz;
    this.syz = syz;
    this.stx = stx;
    this.sty = sty;
    this.stz = stz;
    this.sdobs = sdobs;
    this.smajax = smajax;
    this.sminax = sminax;
    this.strike = strike;
    this.sdepth = sdepth;
    this.stime = stime;
    this.conf = conf;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Origerr(Origerr other) {
    this.orid = other.getOrid();
    this.sxx = other.getSxx();
    this.syy = other.getSyy();
    this.szz = other.getSzz();
    this.stt = other.getStt();
    this.sxy = other.getSxy();
    this.sxz = other.getSxz();
    this.syz = other.getSyz();
    this.stx = other.getStx();
    this.sty = other.getSty();
    this.stz = other.getStz();
    this.sdobs = other.getSdobs();
    this.smajax = other.getSmajax();
    this.sminax = other.getSminax();
    this.strike = other.getStrike();
    this.sdepth = other.getSdepth();
    this.stime = other.getStime();
    this.conf = other.getConf();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Origerr() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(ORID_NA, SXX_NA, SYY_NA, SZZ_NA, STT_NA, SXY_NA, SXZ_NA, SYZ_NA, STX_NA, STY_NA,
        STZ_NA, SDOBS_NA, SMAJAX_NA, SMINAX_NA, STRIKE_NA, SDEPTH_NA, STIME_NA, CONF_NA, COMMID_NA);
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
      case "sxx":
        return sxx;
      case "syy":
        return syy;
      case "szz":
        return szz;
      case "stt":
        return stt;
      case "sxy":
        return sxy;
      case "sxz":
        return sxz;
      case "syz":
        return syz;
      case "stx":
        return stx;
      case "sty":
        return sty;
      case "stz":
        return stz;
      case "sdobs":
        return sdobs;
      case "smajax":
        return smajax;
      case "sminax":
        return sminax;
      case "strike":
        return strike;
      case "sdepth":
        return sdepth;
      case "stime":
        return stime;
      case "conf":
        return conf;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "sxx":
        sxx = value;
        break;
      case "syy":
        syy = value;
        break;
      case "szz":
        szz = value;
        break;
      case "stt":
        stt = value;
        break;
      case "sxy":
        sxy = value;
        break;
      case "sxz":
        sxz = value;
        break;
      case "syz":
        syz = value;
        break;
      case "stx":
        stx = value;
        break;
      case "sty":
        sty = value;
        break;
      case "stz":
        stz = value;
        break;
      case "sdobs":
        sdobs = value;
        break;
      case "smajax":
        smajax = value;
        break;
      case "sminax":
        sminax = value;
        break;
      case "strike":
        strike = value;
        break;
      case "sdepth":
        sdepth = value;
        break;
      case "stime":
        stime = value;
        break;
      case "conf":
        conf = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
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
  public Origerr(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Origerr(DataInputStream input) throws IOException {
    this(input.readLong(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Origerr(ByteBuffer input) {
    this(input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Origerr(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Origerr(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getDouble(offset + 17), input.getDouble(offset + 18),
        input.getLong(offset + 19));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[19];
    values[0] = orid;
    values[1] = sxx;
    values[2] = syy;
    values[3] = szz;
    values[4] = stt;
    values[5] = sxy;
    values[6] = sxz;
    values[7] = syz;
    values[8] = stx;
    values[9] = sty;
    values[10] = stz;
    values[11] = sdobs;
    values[12] = smajax;
    values[13] = sminax;
    values[14] = strike;
    values[15] = sdepth;
    values[16] = stime;
    values[17] = conf;
    values[18] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[20];
    values[0] = orid;
    values[1] = sxx;
    values[2] = syy;
    values[3] = szz;
    values[4] = stt;
    values[5] = sxy;
    values[6] = sxz;
    values[7] = syz;
    values[8] = stx;
    values[9] = sty;
    values[10] = stz;
    values[11] = sdobs;
    values[12] = smajax;
    values[13] = sminax;
    values[14] = strike;
    values[15] = sdepth;
    values[16] = stime;
    values[17] = conf;
    values[18] = commid;
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
    output.writeLong(orid);
    output.writeDouble(sxx);
    output.writeDouble(syy);
    output.writeDouble(szz);
    output.writeDouble(stt);
    output.writeDouble(sxy);
    output.writeDouble(sxz);
    output.writeDouble(syz);
    output.writeDouble(stx);
    output.writeDouble(sty);
    output.writeDouble(stz);
    output.writeDouble(sdobs);
    output.writeDouble(smajax);
    output.writeDouble(sminax);
    output.writeDouble(strike);
    output.writeDouble(sdepth);
    output.writeDouble(stime);
    output.writeDouble(conf);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(orid);
    output.putDouble(sxx);
    output.putDouble(syy);
    output.putDouble(szz);
    output.putDouble(stt);
    output.putDouble(sxy);
    output.putDouble(sxz);
    output.putDouble(syz);
    output.putDouble(stx);
    output.putDouble(sty);
    output.putDouble(stz);
    output.putDouble(sdobs);
    output.putDouble(smajax);
    output.putDouble(sminax);
    output.putDouble(strike);
    output.putDouble(sdepth);
    output.putDouble(stime);
    output.putDouble(conf);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Origerr objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Origerr objects.
   * @throws IOException
   */
  static public void readOrigerrs(BufferedReader input, Collection<Origerr> rows)
      throws IOException {
    String[] saved = Origerr.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Origerr.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Origerr(new Scanner(line)));
    }
    input.close();
    Origerr.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Origerr objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Origerr objects.
   * @throws IOException
   */
  static public void readOrigerrs(File inputFile, Collection<Origerr> rows) throws IOException {
    readOrigerrs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Origerr objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Origerr objects.
   * @throws IOException
   */
  static public void readOrigerrs(InputStream inputStream, Collection<Origerr> rows)
      throws IOException {
    readOrigerrs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Origerr objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Origerr objects
   * @throws IOException
   */
  static public Set<Origerr> readOrigerrs(BufferedReader input) throws IOException {
    Set<Origerr> rows = new LinkedHashSet<Origerr>();
    readOrigerrs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Origerr objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Origerr objects
   * @throws IOException
   */
  static public Set<Origerr> readOrigerrs(File inputFile) throws IOException {
    return readOrigerrs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Origerr objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Origerr objects
   * @throws IOException
   */
  static public Set<Origerr> readOrigerrs(InputStream input) throws IOException {
    return readOrigerrs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Origerr objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param origerrs the Origerr objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Origerr> origerrs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Origerr origerr : origerrs)
      origerr.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Origerr objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param origerrs the Origerr objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Origerr> origerrs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Origerr origerr : origerrs) {
        int i = 0;
        statement.setLong(++i, origerr.orid);
        statement.setDouble(++i, origerr.sxx);
        statement.setDouble(++i, origerr.syy);
        statement.setDouble(++i, origerr.szz);
        statement.setDouble(++i, origerr.stt);
        statement.setDouble(++i, origerr.sxy);
        statement.setDouble(++i, origerr.sxz);
        statement.setDouble(++i, origerr.syz);
        statement.setDouble(++i, origerr.stx);
        statement.setDouble(++i, origerr.sty);
        statement.setDouble(++i, origerr.stz);
        statement.setDouble(++i, origerr.sdobs);
        statement.setDouble(++i, origerr.smajax);
        statement.setDouble(++i, origerr.sminax);
        statement.setDouble(++i, origerr.strike);
        statement.setDouble(++i, origerr.sdepth);
        statement.setDouble(++i, origerr.stime);
        statement.setDouble(++i, origerr.conf);
        statement.setLong(++i, origerr.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Origerr
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Origerr> readOrigerrs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Origerr> results = new HashSet<Origerr>();
    readOrigerrs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Origerr
   *        table.
   * @param origerrs
   * @throws SQLException
   */
  static public void readOrigerrs(Connection connection, String selectStatement,
      Set<Origerr> origerrs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        origerrs.add(new Origerr(rs));
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
   * this Origerr object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Origerr object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "orid, sxx, syy, szz, stt, sxy, sxz, syz, stx, sty, stz, sdobs, smajax, sminax, strike, sdepth, stime, conf, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Double.toString(sxx)).append(", ");
    sql.append(Double.toString(syy)).append(", ");
    sql.append(Double.toString(szz)).append(", ");
    sql.append(Double.toString(stt)).append(", ");
    sql.append(Double.toString(sxy)).append(", ");
    sql.append(Double.toString(sxz)).append(", ");
    sql.append(Double.toString(syz)).append(", ");
    sql.append(Double.toString(stx)).append(", ");
    sql.append(Double.toString(sty)).append(", ");
    sql.append(Double.toString(stz)).append(", ");
    sql.append(Double.toString(sdobs)).append(", ");
    sql.append(Double.toString(smajax)).append(", ");
    sql.append(Double.toString(sminax)).append(", ");
    sql.append(Double.toString(strike)).append(", ");
    sql.append(Double.toString(sdepth)).append(", ");
    sql.append(Double.toString(stime)).append(", ");
    sql.append(Double.toString(conf)).append(", ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Origerr in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Origerr in the database
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
   * Generate a sql script to create a table of type Origerr in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Origerr in the database
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
    buf.append("orid         number(18)            NOT NULL,\n");
    buf.append("sxx          float(24)            NOT NULL,\n");
    buf.append("syy          float(24)            NOT NULL,\n");
    buf.append("szz          float(24)            NOT NULL,\n");
    buf.append("stt          float(24)            NOT NULL,\n");
    buf.append("sxy          float(24)            NOT NULL,\n");
    buf.append("sxz          float(24)            NOT NULL,\n");
    buf.append("syz          float(24)            NOT NULL,\n");
    buf.append("stx          float(24)            NOT NULL,\n");
    buf.append("sty          float(24)            NOT NULL,\n");
    buf.append("stz          float(24)            NOT NULL,\n");
    buf.append("sdobs        float(24)            NOT NULL,\n");
    buf.append("smajax       float(24)            NOT NULL,\n");
    buf.append("sminax       float(24)            NOT NULL,\n");
    buf.append("strike       float(24)            NOT NULL,\n");
    buf.append("sdepth       float(24)            NOT NULL,\n");
    buf.append("stime        float(24)            NOT NULL,\n");
    buf.append("conf         float(24)            NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (orid)");
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
    return 152;
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
    return (other instanceof Origerr) && ((Origerr) other).orid == orid;
  }

  /**
   * Origin identifier that relates a record in these tables to a record in the <B>origin</B> table
   * 
   * @return orid
   */
  public long getOrid() {
    return orid;
  }

  /**
   * Origin identifier that relates a record in these tables to a record in the <B>origin</B> table
   * 
   * @param orid
   * @throws IllegalArgumentException if orid >= 1000000000
   */
  public Origerr setOrid(long orid) {
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @return sxx
   */
  public double getSxx() {
    return sxx;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @param sxx
   */
  public Origerr setSxx(double sxx) {
    this.sxx = sxx;
    setHash(null);
    return this;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @return syy
   */
  public double getSyy() {
    return syy;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @param syy
   */
  public Origerr setSyy(double syy) {
    this.syy = syy;
    setHash(null);
    return this;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @return szz
   */
  public double getSzz() {
    return szz;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @param szz
   */
  public Origerr setSzz(double szz) {
    this.szz = szz;
    setHash(null);
    return this;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: s^2
   * 
   * @return stt
   */
  public double getStt() {
    return stt;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: s^2
   * 
   * @param stt
   */
  public Origerr setStt(double stt) {
    this.stt = stt;
    setHash(null);
    return this;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @return sxy
   */
  public double getSxy() {
    return sxy;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @param sxy
   */
  public Origerr setSxy(double sxy) {
    this.sxy = sxy;
    setHash(null);
    return this;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @return sxz
   */
  public double getSxz() {
    return sxz;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @param sxz
   */
  public Origerr setSxz(double sxz) {
    this.sxz = sxz;
    setHash(null);
    return this;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @return syz
   */
  public double getSyz() {
    return syz;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km^2
   * 
   * @param syz
   */
  public Origerr setSyz(double syz) {
    this.syz = syz;
    setHash(null);
    return this;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km/s
   * 
   * @return stx
   */
  public double getStx() {
    return stx;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km/s
   * 
   * @param stx
   */
  public Origerr setStx(double stx) {
    this.stx = stx;
    setHash(null);
    return this;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km/s
   * 
   * @return sty
   */
  public double getSty() {
    return sty;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km/s
   * 
   * @param sty
   */
  public Origerr setSty(double sty) {
    this.sty = sty;
    setHash(null);
    return this;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km/s
   * 
   * @return stz
   */
  public double getStz() {
    return stz;
  }

  /**
   * <I>stt</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>, <I>sxx</I>, <I>sxy</I>, <I>sxz</I>, <I>syy</I>,
   * <I>syz</I>, <I>szz</I> are elements of the covariance matrix for the location identified by
   * <I>orid</I>. The covariance matrix is symmetric (and positive definite) so that <I>sxy</I> =
   * <I>syx</I>, and so on, (x, y, z, t) refer to latitude, longitude, depth, and origin time,
   * respectively. These columns (together with <I>sdobs</I>, <I>ndef</I>, and <I>dtype</I>) provide
   * the information necessary to construct the K-dimensional (K = 2, 3, 4) confidence ellipse or
   * ellipsoids at any confidence limit desired.
   * <p>
   * Units: km/s
   * 
   * @param stz
   */
  public Origerr setStz(double stz) {
    this.stz = stz;
    setHash(null);
    return this;
  }

  /**
   * Standard error of one observation. This column is derived from the discrepancies in the arrival
   * times of the phases used to locate an event. This column is defined as the square root of the
   * sum of the squares of the time residuals divided by the number of degrees of freedom. The
   * latter is the number of defining observations [<I>ndef</I> in <B>origin</B>] minus the
   * dimension of the system solved (4 if depth is allowed to be a free variable, 3 if depth is
   * constrained).
   * 
   * @return sdobs
   */
  public double getSdobs() {
    return sdobs;
  }

  /**
   * Standard error of one observation. This column is derived from the discrepancies in the arrival
   * times of the phases used to locate an event. This column is defined as the square root of the
   * sum of the squares of the time residuals divided by the number of degrees of freedom. The
   * latter is the number of defining observations [<I>ndef</I> in <B>origin</B>] minus the
   * dimension of the system solved (4 if depth is allowed to be a free variable, 3 if depth is
   * constrained).
   * 
   * @param sdobs
   */
  public Origerr setSdobs(double sdobs) {
    this.sdobs = sdobs;
    setHash(null);
    return this;
  }

  /**
   * Semi-major axis of error ellipse for a given confidence. This value is the length of the
   * semi-major axis of the location error ellipse. The value is found by projecting the covariance
   * matrix onto the horizontal plane. The level of confidence is specified by <I>conf</I> (see
   * <I>sdepth</I>, <I>sminax</I>, and <I>sxx</I>, <I>syy</I>, <I>szz</I>, <I>stt</I>, <I>sxy</I>,
   * <I>sxz</I> <I>syz</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>,).
   * <p>
   * Units: km
   * 
   * @return smajax
   */
  public double getSmajax() {
    return smajax;
  }

  /**
   * Semi-major axis of error ellipse for a given confidence. This value is the length of the
   * semi-major axis of the location error ellipse. The value is found by projecting the covariance
   * matrix onto the horizontal plane. The level of confidence is specified by <I>conf</I> (see
   * <I>sdepth</I>, <I>sminax</I>, and <I>sxx</I>, <I>syy</I>, <I>szz</I>, <I>stt</I>, <I>sxy</I>,
   * <I>sxz</I> <I>syz</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>,).
   * <p>
   * Units: km
   * 
   * @param smajax
   */
  public Origerr setSmajax(double smajax) {
    this.smajax = smajax;
    setHash(null);
    return this;
  }

  /**
   * Semi-minor axis of error ellipse. This value is the length of the semi-minor axis of the
   * location error ellipse. The value is found by projecting the covariance matrix onto the
   * horizontal plane. The level of confidence is specified by <I>conf</I> (see <I>sdepth</I>,
   * <I>smajax</I>, and <I>sxx</I>, <I>syy</I>, <I>szz</I>, <I>stt</I>, <I>sxy</I>, <I>sxz</I>
   * <I>syz</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>,).
   * <p>
   * Units: km
   * 
   * @return sminax
   */
  public double getSminax() {
    return sminax;
  }

  /**
   * Semi-minor axis of error ellipse. This value is the length of the semi-minor axis of the
   * location error ellipse. The value is found by projecting the covariance matrix onto the
   * horizontal plane. The level of confidence is specified by <I>conf</I> (see <I>sdepth</I>,
   * <I>smajax</I>, and <I>sxx</I>, <I>syy</I>, <I>szz</I>, <I>stt</I>, <I>sxy</I>, <I>sxz</I>
   * <I>syz</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>,).
   * <p>
   * Units: km
   * 
   * @param sminax
   */
  public Origerr setSminax(double sminax) {
    this.sminax = sminax;
    setHash(null);
    return this;
  }

  /**
   * Strike of major axis of error ellipse. This column is the strike of the semi-major axis of the
   * location error ellipse, measured in degrees clockwise from the North (see <I>smajax</I>).
   * <p>
   * Units: degree
   * 
   * @return strike
   */
  public double getStrike() {
    return strike;
  }

  /**
   * Strike of major axis of error ellipse. This column is the strike of the semi-major axis of the
   * location error ellipse, measured in degrees clockwise from the North (see <I>smajax</I>).
   * <p>
   * Units: degree
   * 
   * @param strike
   */
  public Origerr setStrike(double strike) {
    this.strike = strike;
    setHash(null);
    return this;
  }

  /**
   * Depth error. This is the maximum error of a depth estimate for a level of confidence given by
   * <I>conf</I> (see <I>smajax</I>, <I>sminax</I>, and <I>sxx</I>, <I>syy</I>, <I>szz</I>,
   * <I>stt</I>, <I>sxy</I>, <I>sxz</I> <I>syz</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>,)
   * <p>
   * Units: km
   * 
   * @return sdepth
   */
  public double getSdepth() {
    return sdepth;
  }

  /**
   * Depth error. This is the maximum error of a depth estimate for a level of confidence given by
   * <I>conf</I> (see <I>smajax</I>, <I>sminax</I>, and <I>sxx</I>, <I>syy</I>, <I>szz</I>,
   * <I>stt</I>, <I>sxy</I>, <I>sxz</I> <I>syz</I>, <I>stx</I>, <I>sty</I>, <I>stz</I>,)
   * <p>
   * Units: km
   * 
   * @param sdepth
   */
  public Origerr setSdepth(double sdepth) {
    this.sdepth = sdepth;
    setHash(null);
    return this;
  }

  /**
   * Origin time error. This column denotes the time uncertainty that accompanies the average error
   * ellipse location (see <I>smajax</I>, <I>sminax</I>, and <I>sdepth</I>).
   * <p>
   * Units: s
   * 
   * @return stime
   */
  public double getStime() {
    return stime;
  }

  /**
   * Origin time error. This column denotes the time uncertainty that accompanies the average error
   * ellipse location (see <I>smajax</I>, <I>sminax</I>, and <I>sdepth</I>).
   * <p>
   * Units: s
   * 
   * @param stime
   */
  public Origerr setStime(double stime) {
    this.stime = stime;
    setHash(null);
    return this;
  }

  /**
   * Confidence measure for a particular event identification method
   * 
   * @return conf
   */
  public double getConf() {
    return conf;
  }

  /**
   * Confidence measure for a particular event identification method
   * 
   * @param conf
   */
  public Origerr setConf(double conf) {
    this.conf = conf;
    setHash(null);
    return this;
  }

  /**
   * Comment identifier. This value is a key that points to free-form comments entered in the
   * <B>remark</B> table. These comments store additional information about a record in another
   * table. The <B>remark</B> table can have many records with the same <I>commid</I> and different
   * <I>lineno</I>, but the same <I>commid</I> will appear in only one other record among the rest
   * of the tables in the database (see <I>lineno</I>).
   * 
   * @return commid
   */
  public long getCommid() {
    return commid;
  }

  /**
   * Comment identifier. This value is a key that points to free-form comments entered in the
   * <B>remark</B> table. These comments store additional information about a record in another
   * table. The <B>remark</B> table can have many records with the same <I>commid</I> and different
   * <I>lineno</I>, but the same <I>commid</I> will appear in only one other record among the rest
   * of the tables in the database (see <I>lineno</I>).
   * 
   * @param commid
   * @throws IllegalArgumentException if commid >= 1000000000
   */
  public Origerr setCommid(long commid) {
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
    return "NNSA KB Core";
  }

  public TestBuffer getTestBuffer() {
	  	TestBuffer buffer = new TestBuffer(this.getClass().getSimpleName());
      buffer.add("origerr.orid", orid);
      buffer.add("origerr.sxx", sxx);
      buffer.add("origerr.syy", syy);
      buffer.add("origerr.szz", szz);
      buffer.add("origerr.stt", stt);
      buffer.add("origerr.sxy", sxy);
      buffer.add("origerr.sxz", sxz);
      buffer.add("origerr.syz", syz);
      buffer.add("origerr.stx", stx);
      buffer.add("origerr.sty", sty);
      buffer.add("origerr.stz", stz);
      buffer.add("origerr.sdobs", sdobs);
      buffer.add("origerr.smajax", smajax);
      buffer.add("origerr.sminax", sminax);
      buffer.add("origerr.strike", strike);
      buffer.add("origerr.sdepth", sdepth);
      buffer.add("origerr.stime", stime);
      buffer.add("origerr.conf", conf);
      buffer.add("origerr.commid", commid);
		buffer.add();
      return buffer;
  }

}
