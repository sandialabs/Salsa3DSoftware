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
 * coda_shape
 */
public class Coda_shape extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   */
  private long magyieldid;

  static final public long MAGYIELDID_NA = Long.MIN_VALUE;

  /**
   * Time relative to <I>codat0</I> at which the amplitude is measured.
   * <p>
   * Units: s
   */
  private double tmeas;

  static final public double TMEAS_NA = Double.NaN;

  /**
   * Minimum allowed begin time of the coda measurement window, relative to the zero time defined by
   * <B>env_peak</B> table parameters and <I>distance</I> (<I>codat0</I>).
   * <p>
   * Units: s
   */
  private double codaoffset;

  static final public double CODAOFFSET_NA = Double.NaN;

  /**
   * Maximum allowed end time of the coda measurement window is <I>codaoffset</I> + <I>codadur</I>
   * relative to the zero time defined by <B>env_peak</B> table parameters and <I>distance</I>
   * (<I>codat0</I>). No limit if undefined; however, this is not accepted practice. Note: the
   * maximum end time is not affected by an increase in the coda start time beyond
   * <I>codaoffset</I>.
   * <p>
   * Units: s
   */
  private double codadur;

  static final public double CODADUR_NA = Double.NaN;

  /**
   * First of three distance hyperbola parameters used to compute the coda shape spreading factor
   * (Mayeda et al., 2003).
   */
  private double sspread0;

  static final public double SSPREAD0_NA = Double.NaN;

  /**
   * Error on the first of three distance hyperbola parameters used to compute the coda shape
   * spreading factor (Mayeda et al., 2003).
   */
  private double dsspread0;

  static final public double DSSPREAD0_NA = -1;

  /**
   * Second of three distance hyperbola parameters used to compute the coda shape spreading factor
   * (Mayeda et al., 2003); undefined implies zero.
   * <p>
   * Units: km
   */
  private double sspread1;

  static final public double SSPREAD1_NA = Double.NaN;

  /**
   * Error on the second of three distance hyperbola parameters used to compute the coda shape
   * spreading factor (Mayeda et al., 2003).
   * <p>
   * Units: km
   */
  private double dsspread1;

  static final public double DSSPREAD1_NA = -1;

  /**
   * Third of three distance hyperbola parameters used to compute the coda shape spreading factor
   * (Mayeda et al., 2003), undefined implies infinite.
   * <p>
   * Units: km
   */
  private double sspread2;

  static final public double SSPREAD2_NA = Double.NaN;

  /**
   * Error on the third of three distance hyperbola parameters used to compute the coda shape
   * spreading factor (Mayeda et al., 2003).
   * <p>
   * Units: km
   */
  private double dsspread2;

  static final public double DSSPREAD2_NA = -1;

  /**
   * CSS 2D correction identifier
   */
  private long ssp2dcorid;

  static final public long SSP2DCORID_NA = -1;

  /**
   * Coda shape decay (CSD) exponent term 0. First of three distance hyperbola parameters used to
   * compute the coda shape attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: 1/s
   */
  private double b0;

  static final public double B0_NA = Double.NaN;

  /**
   * Error on the first of three distance hyperbola parameters used to compute the coda shape
   * attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: 1/s
   */
  private double db0;

  static final public double DB0_NA = -1;

  /**
   * Second of three distance hyperbola parameters used to compute the coda shape attenuation factor
   * (Mayeda et al., 2003); undefined implies zero.
   * <p>
   * Units: km/s
   */
  private double b1;

  static final public double B1_NA = Double.NaN;

  /**
   * Error on the second of three distance hyperbola parameters used to compute the coda shape
   * attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: km/s
   */
  private double db1;

  static final public double DB1_NA = -1;

  /**
   * Third of three distance hyperbola parameters used to compute the coda shape attenuation factor
   * (Mayeda et al., 2003), undefined implies infinite.
   * <p>
   * Units: km
   */
  private double b2;

  static final public double B2_NA = Double.NaN;

  /**
   * Error on the third of three distance hyperbola parameters used to compute the coda shape
   * attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: km
   */
  private double db2;

  static final public double DB2_NA = -1;

  /**
   * Coda shape decay 2d identifier
   */
  private long b2dcorid;

  static final public long B2DCORID_NA = -1;

  /**
   * Minimum calibrated distance for 1-D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   */
  private double dmin;

  static final public double DMIN_NA = -1;

  /**
   * Maximum calibrated distance for 1D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   */
  private double dmax;

  static final public double DMAX_NA = -1;

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
    columns.add("tmeas", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("codaoffset", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("codadur", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("sspread0", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("dsspread0", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("sspread1", Columns.FieldType.DOUBLE, "%10.3e");
    columns.add("dsspread1", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("sspread2", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("dsspread2", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("ssp2dcorid", Columns.FieldType.LONG, "%d");
    columns.add("b0", Columns.FieldType.DOUBLE, "%10.3e");
    columns.add("db0", Columns.FieldType.DOUBLE, "%10.3e");
    columns.add("b1", Columns.FieldType.DOUBLE, "%10.3e");
    columns.add("db1", Columns.FieldType.DOUBLE, "%10.3e");
    columns.add("b2", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("db2", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("b2dcorid", Columns.FieldType.LONG, "%d");
    columns.add("dmin", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("dmax", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Coda_shape(long magyieldid, double tmeas, double codaoffset, double codadur,
      double sspread0, double dsspread0, double sspread1, double dsspread1, double sspread2,
      double dsspread2, long ssp2dcorid, double b0, double db0, double b1, double db1, double b2,
      double db2, long b2dcorid, double dmin, double dmax, String auth, long commid) {
    setValues(magyieldid, tmeas, codaoffset, codadur, sspread0, dsspread0, sspread1, dsspread1,
        sspread2, dsspread2, ssp2dcorid, b0, db0, b1, db1, b2, db2, b2dcorid, dmin, dmax, auth,
        commid);
  }

  private void setValues(long magyieldid, double tmeas, double codaoffset, double codadur,
      double sspread0, double dsspread0, double sspread1, double dsspread1, double sspread2,
      double dsspread2, long ssp2dcorid, double b0, double db0, double b1, double db1, double b2,
      double db2, long b2dcorid, double dmin, double dmax, String auth, long commid) {
    this.magyieldid = magyieldid;
    this.tmeas = tmeas;
    this.codaoffset = codaoffset;
    this.codadur = codadur;
    this.sspread0 = sspread0;
    this.dsspread0 = dsspread0;
    this.sspread1 = sspread1;
    this.dsspread1 = dsspread1;
    this.sspread2 = sspread2;
    this.dsspread2 = dsspread2;
    this.ssp2dcorid = ssp2dcorid;
    this.b0 = b0;
    this.db0 = db0;
    this.b1 = b1;
    this.db1 = db1;
    this.b2 = b2;
    this.db2 = db2;
    this.b2dcorid = b2dcorid;
    this.dmin = dmin;
    this.dmax = dmax;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Coda_shape(Coda_shape other) {
    this.magyieldid = other.getMagyieldid();
    this.tmeas = other.getTmeas();
    this.codaoffset = other.getCodaoffset();
    this.codadur = other.getCodadur();
    this.sspread0 = other.getSspread0();
    this.dsspread0 = other.getDsspread0();
    this.sspread1 = other.getSspread1();
    this.dsspread1 = other.getDsspread1();
    this.sspread2 = other.getSspread2();
    this.dsspread2 = other.getDsspread2();
    this.ssp2dcorid = other.getSsp2dcorid();
    this.b0 = other.getB0();
    this.db0 = other.getDb0();
    this.b1 = other.getB1();
    this.db1 = other.getDb1();
    this.b2 = other.getB2();
    this.db2 = other.getDb2();
    this.b2dcorid = other.getB2dcorid();
    this.dmin = other.getDmin();
    this.dmax = other.getDmax();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Coda_shape() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MAGYIELDID_NA, TMEAS_NA, CODAOFFSET_NA, CODADUR_NA, SSPREAD0_NA, DSSPREAD0_NA,
        SSPREAD1_NA, DSSPREAD1_NA, SSPREAD2_NA, DSSPREAD2_NA, SSP2DCORID_NA, B0_NA, DB0_NA, B1_NA,
        DB1_NA, B2_NA, DB2_NA, B2DCORID_NA, DMIN_NA, DMAX_NA, AUTH_NA, COMMID_NA);
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
      case "tmeas":
        return tmeas;
      case "codaoffset":
        return codaoffset;
      case "codadur":
        return codadur;
      case "sspread0":
        return sspread0;
      case "dsspread0":
        return dsspread0;
      case "sspread1":
        return sspread1;
      case "dsspread1":
        return dsspread1;
      case "sspread2":
        return sspread2;
      case "dsspread2":
        return dsspread2;
      case "b0":
        return b0;
      case "db0":
        return db0;
      case "b1":
        return b1;
      case "db1":
        return db1;
      case "b2":
        return b2;
      case "db2":
        return db2;
      case "dmin":
        return dmin;
      case "dmax":
        return dmax;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "tmeas":
        tmeas = value;
        break;
      case "codaoffset":
        codaoffset = value;
        break;
      case "codadur":
        codadur = value;
        break;
      case "sspread0":
        sspread0 = value;
        break;
      case "dsspread0":
        dsspread0 = value;
        break;
      case "sspread1":
        sspread1 = value;
        break;
      case "dsspread1":
        dsspread1 = value;
        break;
      case "sspread2":
        sspread2 = value;
        break;
      case "dsspread2":
        dsspread2 = value;
        break;
      case "b0":
        b0 = value;
        break;
      case "db0":
        db0 = value;
        break;
      case "b1":
        b1 = value;
        break;
      case "db1":
        db1 = value;
        break;
      case "b2":
        b2 = value;
        break;
      case "db2":
        db2 = value;
        break;
      case "dmin":
        dmin = value;
        break;
      case "dmax":
        dmax = value;
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
      case "ssp2dcorid":
        return ssp2dcorid;
      case "b2dcorid":
        return b2dcorid;
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
      case "ssp2dcorid":
        ssp2dcorid = value;
        break;
      case "b2dcorid":
        b2dcorid = value;
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
  public Coda_shape(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Coda_shape(DataInputStream input) throws IOException {
    this(input.readLong(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readLong(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readLong(), input.readDouble(), input.readDouble(),
        readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Coda_shape(ByteBuffer input) {
    this(input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getLong(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(), input.getLong(),
        input.getDouble(), input.getDouble(), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Coda_shape(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Coda_shape(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getLong(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getDouble(offset + 17), input.getLong(offset + 18),
        input.getDouble(offset + 19), input.getDouble(offset + 20), input.getString(offset + 21),
        input.getLong(offset + 22));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[22];
    values[0] = magyieldid;
    values[1] = tmeas;
    values[2] = codaoffset;
    values[3] = codadur;
    values[4] = sspread0;
    values[5] = dsspread0;
    values[6] = sspread1;
    values[7] = dsspread1;
    values[8] = sspread2;
    values[9] = dsspread2;
    values[10] = ssp2dcorid;
    values[11] = b0;
    values[12] = db0;
    values[13] = b1;
    values[14] = db1;
    values[15] = b2;
    values[16] = db2;
    values[17] = b2dcorid;
    values[18] = dmin;
    values[19] = dmax;
    values[20] = auth;
    values[21] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[23];
    values[0] = magyieldid;
    values[1] = tmeas;
    values[2] = codaoffset;
    values[3] = codadur;
    values[4] = sspread0;
    values[5] = dsspread0;
    values[6] = sspread1;
    values[7] = dsspread1;
    values[8] = sspread2;
    values[9] = dsspread2;
    values[10] = ssp2dcorid;
    values[11] = b0;
    values[12] = db0;
    values[13] = b1;
    values[14] = db1;
    values[15] = b2;
    values[16] = db2;
    values[17] = b2dcorid;
    values[18] = dmin;
    values[19] = dmax;
    values[20] = auth;
    values[21] = commid;
    values[22] = lddate;
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
    output.writeDouble(tmeas);
    output.writeDouble(codaoffset);
    output.writeDouble(codadur);
    output.writeDouble(sspread0);
    output.writeDouble(dsspread0);
    output.writeDouble(sspread1);
    output.writeDouble(dsspread1);
    output.writeDouble(sspread2);
    output.writeDouble(dsspread2);
    output.writeLong(ssp2dcorid);
    output.writeDouble(b0);
    output.writeDouble(db0);
    output.writeDouble(b1);
    output.writeDouble(db1);
    output.writeDouble(b2);
    output.writeDouble(db2);
    output.writeLong(b2dcorid);
    output.writeDouble(dmin);
    output.writeDouble(dmax);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(magyieldid);
    output.putDouble(tmeas);
    output.putDouble(codaoffset);
    output.putDouble(codadur);
    output.putDouble(sspread0);
    output.putDouble(dsspread0);
    output.putDouble(sspread1);
    output.putDouble(dsspread1);
    output.putDouble(sspread2);
    output.putDouble(dsspread2);
    output.putLong(ssp2dcorid);
    output.putDouble(b0);
    output.putDouble(db0);
    output.putDouble(b1);
    output.putDouble(db1);
    output.putDouble(b2);
    output.putDouble(db2);
    output.putLong(b2dcorid);
    output.putDouble(dmin);
    output.putDouble(dmax);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Coda_shape objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Coda_shape objects.
   * @throws IOException
   */
  static public void readCoda_shapes(BufferedReader input, Collection<Coda_shape> rows)
      throws IOException {
    String[] saved = Coda_shape.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Coda_shape
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Coda_shape(new Scanner(line)));
    }
    input.close();
    Coda_shape.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Coda_shape objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Coda_shape objects.
   * @throws IOException
   */
  static public void readCoda_shapes(File inputFile, Collection<Coda_shape> rows)
      throws IOException {
    readCoda_shapes(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Coda_shape objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Coda_shape objects.
   * @throws IOException
   */
  static public void readCoda_shapes(InputStream inputStream, Collection<Coda_shape> rows)
      throws IOException {
    readCoda_shapes(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Coda_shape objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Coda_shape objects
   * @throws IOException
   */
  static public Set<Coda_shape> readCoda_shapes(BufferedReader input) throws IOException {
    Set<Coda_shape> rows = new LinkedHashSet<Coda_shape>();
    readCoda_shapes(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Coda_shape objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Coda_shape objects
   * @throws IOException
   */
  static public Set<Coda_shape> readCoda_shapes(File inputFile) throws IOException {
    return readCoda_shapes(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Coda_shape objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Coda_shape objects
   * @throws IOException
   */
  static public Set<Coda_shape> readCoda_shapes(InputStream input) throws IOException {
    return readCoda_shapes(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Coda_shape objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param coda_shapes the Coda_shape objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Coda_shape> coda_shapes)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Coda_shape coda_shape : coda_shapes)
      coda_shape.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Coda_shape objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param coda_shapes the Coda_shape objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Coda_shape> coda_shapes, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Coda_shape coda_shape : coda_shapes) {
        int i = 0;
        statement.setLong(++i, coda_shape.magyieldid);
        statement.setDouble(++i, coda_shape.tmeas);
        statement.setDouble(++i, coda_shape.codaoffset);
        statement.setDouble(++i, coda_shape.codadur);
        statement.setDouble(++i, coda_shape.sspread0);
        statement.setDouble(++i, coda_shape.dsspread0);
        statement.setDouble(++i, coda_shape.sspread1);
        statement.setDouble(++i, coda_shape.dsspread1);
        statement.setDouble(++i, coda_shape.sspread2);
        statement.setDouble(++i, coda_shape.dsspread2);
        statement.setLong(++i, coda_shape.ssp2dcorid);
        statement.setDouble(++i, coda_shape.b0);
        statement.setDouble(++i, coda_shape.db0);
        statement.setDouble(++i, coda_shape.b1);
        statement.setDouble(++i, coda_shape.db1);
        statement.setDouble(++i, coda_shape.b2);
        statement.setDouble(++i, coda_shape.db2);
        statement.setLong(++i, coda_shape.b2dcorid);
        statement.setDouble(++i, coda_shape.dmin);
        statement.setDouble(++i, coda_shape.dmax);
        statement.setString(++i, coda_shape.auth);
        statement.setLong(++i, coda_shape.commid);
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
   *        Coda_shape table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Coda_shape> readCoda_shapes(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Coda_shape> results = new HashSet<Coda_shape>();
    readCoda_shapes(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Coda_shape table.
   * @param coda_shapes
   * @throws SQLException
   */
  static public void readCoda_shapes(Connection connection, String selectStatement,
      Set<Coda_shape> coda_shapes) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        coda_shapes.add(new Coda_shape(rs));
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
   * this Coda_shape object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Coda_shape object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "magyieldid, tmeas, codaoffset, codadur, sspread0, dsspread0, sspread1, dsspread1, sspread2, dsspread2, ssp2dcorid, b0, db0, b1, db1, b2, db2, b2dcorid, dmin, dmax, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(magyieldid)).append(", ");
    sql.append(Double.toString(tmeas)).append(", ");
    sql.append(Double.toString(codaoffset)).append(", ");
    sql.append(Double.toString(codadur)).append(", ");
    sql.append(Double.toString(sspread0)).append(", ");
    sql.append(Double.toString(dsspread0)).append(", ");
    sql.append(Double.toString(sspread1)).append(", ");
    sql.append(Double.toString(dsspread1)).append(", ");
    sql.append(Double.toString(sspread2)).append(", ");
    sql.append(Double.toString(dsspread2)).append(", ");
    sql.append(Long.toString(ssp2dcorid)).append(", ");
    sql.append(Double.toString(b0)).append(", ");
    sql.append(Double.toString(db0)).append(", ");
    sql.append(Double.toString(b1)).append(", ");
    sql.append(Double.toString(db1)).append(", ");
    sql.append(Double.toString(b2)).append(", ");
    sql.append(Double.toString(db2)).append(", ");
    sql.append(Long.toString(b2dcorid)).append(", ");
    sql.append(Double.toString(dmin)).append(", ");
    sql.append(Double.toString(dmax)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Coda_shape in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Coda_shape in the database
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
   * Generate a sql script to create a table of type Coda_shape in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Coda_shape in the database
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
    buf.append("tmeas        float(24)            NOT NULL,\n");
    buf.append("codaoffset   float(24)            NOT NULL,\n");
    buf.append("codadur      float(24)            NOT NULL,\n");
    buf.append("sspread0     float(24)            NOT NULL,\n");
    buf.append("dsspread0    float(24)            NOT NULL,\n");
    buf.append("sspread1     float(24)            NOT NULL,\n");
    buf.append("dsspread1    float(24)            NOT NULL,\n");
    buf.append("sspread2     float(24)            NOT NULL,\n");
    buf.append("dsspread2    float(24)            NOT NULL,\n");
    buf.append("ssp2dcorid   number(9)            NOT NULL,\n");
    buf.append("b0           float(24)            NOT NULL,\n");
    buf.append("db0          float(24)            NOT NULL,\n");
    buf.append("b1           float(24)            NOT NULL,\n");
    buf.append("db1          float(24)            NOT NULL,\n");
    buf.append("b2           float(24)            NOT NULL,\n");
    buf.append("db2          float(24)            NOT NULL,\n");
    buf.append("b2dcorid     number(9)            NOT NULL,\n");
    buf.append("dmin         float(24)            NOT NULL,\n");
    buf.append("dmax         float(24)            NOT NULL,\n");
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
    return 192;
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
    return (other instanceof Coda_shape) && ((Coda_shape) other).magyieldid == magyieldid;
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
  public Coda_shape setMagyieldid(long magyieldid) {
    if (magyieldid >= 1000000000L)
      throw new IllegalArgumentException(
          "magyieldid=" + magyieldid + " but cannot be >= 1000000000");
    this.magyieldid = magyieldid;
    setHash(null);
    return this;
  }

  /**
   * Time relative to <I>codat0</I> at which the amplitude is measured.
   * <p>
   * Units: s
   * 
   * @return tmeas
   */
  public double getTmeas() {
    return tmeas;
  }

  /**
   * Time relative to <I>codat0</I> at which the amplitude is measured.
   * <p>
   * Units: s
   * 
   * @param tmeas
   */
  public Coda_shape setTmeas(double tmeas) {
    this.tmeas = tmeas;
    setHash(null);
    return this;
  }

  /**
   * Minimum allowed begin time of the coda measurement window, relative to the zero time defined by
   * <B>env_peak</B> table parameters and <I>distance</I> (<I>codat0</I>).
   * <p>
   * Units: s
   * 
   * @return codaoffset
   */
  public double getCodaoffset() {
    return codaoffset;
  }

  /**
   * Minimum allowed begin time of the coda measurement window, relative to the zero time defined by
   * <B>env_peak</B> table parameters and <I>distance</I> (<I>codat0</I>).
   * <p>
   * Units: s
   * 
   * @param codaoffset
   */
  public Coda_shape setCodaoffset(double codaoffset) {
    this.codaoffset = codaoffset;
    setHash(null);
    return this;
  }

  /**
   * Maximum allowed end time of the coda measurement window is <I>codaoffset</I> + <I>codadur</I>
   * relative to the zero time defined by <B>env_peak</B> table parameters and <I>distance</I>
   * (<I>codat0</I>). No limit if undefined; however, this is not accepted practice. Note: the
   * maximum end time is not affected by an increase in the coda start time beyond
   * <I>codaoffset</I>.
   * <p>
   * Units: s
   * 
   * @return codadur
   */
  public double getCodadur() {
    return codadur;
  }

  /**
   * Maximum allowed end time of the coda measurement window is <I>codaoffset</I> + <I>codadur</I>
   * relative to the zero time defined by <B>env_peak</B> table parameters and <I>distance</I>
   * (<I>codat0</I>). No limit if undefined; however, this is not accepted practice. Note: the
   * maximum end time is not affected by an increase in the coda start time beyond
   * <I>codaoffset</I>.
   * <p>
   * Units: s
   * 
   * @param codadur
   */
  public Coda_shape setCodadur(double codadur) {
    this.codadur = codadur;
    setHash(null);
    return this;
  }

  /**
   * First of three distance hyperbola parameters used to compute the coda shape spreading factor
   * (Mayeda et al., 2003).
   * 
   * @return sspread0
   */
  public double getSspread0() {
    return sspread0;
  }

  /**
   * First of three distance hyperbola parameters used to compute the coda shape spreading factor
   * (Mayeda et al., 2003).
   * 
   * @param sspread0
   */
  public Coda_shape setSspread0(double sspread0) {
    this.sspread0 = sspread0;
    setHash(null);
    return this;
  }

  /**
   * Error on the first of three distance hyperbola parameters used to compute the coda shape
   * spreading factor (Mayeda et al., 2003).
   * 
   * @return dsspread0
   */
  public double getDsspread0() {
    return dsspread0;
  }

  /**
   * Error on the first of three distance hyperbola parameters used to compute the coda shape
   * spreading factor (Mayeda et al., 2003).
   * 
   * @param dsspread0
   */
  public Coda_shape setDsspread0(double dsspread0) {
    this.dsspread0 = dsspread0;
    setHash(null);
    return this;
  }

  /**
   * Second of three distance hyperbola parameters used to compute the coda shape spreading factor
   * (Mayeda et al., 2003); undefined implies zero.
   * <p>
   * Units: km
   * 
   * @return sspread1
   */
  public double getSspread1() {
    return sspread1;
  }

  /**
   * Second of three distance hyperbola parameters used to compute the coda shape spreading factor
   * (Mayeda et al., 2003); undefined implies zero.
   * <p>
   * Units: km
   * 
   * @param sspread1
   */
  public Coda_shape setSspread1(double sspread1) {
    this.sspread1 = sspread1;
    setHash(null);
    return this;
  }

  /**
   * Error on the second of three distance hyperbola parameters used to compute the coda shape
   * spreading factor (Mayeda et al., 2003).
   * <p>
   * Units: km
   * 
   * @return dsspread1
   */
  public double getDsspread1() {
    return dsspread1;
  }

  /**
   * Error on the second of three distance hyperbola parameters used to compute the coda shape
   * spreading factor (Mayeda et al., 2003).
   * <p>
   * Units: km
   * 
   * @param dsspread1
   */
  public Coda_shape setDsspread1(double dsspread1) {
    this.dsspread1 = dsspread1;
    setHash(null);
    return this;
  }

  /**
   * Third of three distance hyperbola parameters used to compute the coda shape spreading factor
   * (Mayeda et al., 2003), undefined implies infinite.
   * <p>
   * Units: km
   * 
   * @return sspread2
   */
  public double getSspread2() {
    return sspread2;
  }

  /**
   * Third of three distance hyperbola parameters used to compute the coda shape spreading factor
   * (Mayeda et al., 2003), undefined implies infinite.
   * <p>
   * Units: km
   * 
   * @param sspread2
   */
  public Coda_shape setSspread2(double sspread2) {
    this.sspread2 = sspread2;
    setHash(null);
    return this;
  }

  /**
   * Error on the third of three distance hyperbola parameters used to compute the coda shape
   * spreading factor (Mayeda et al., 2003).
   * <p>
   * Units: km
   * 
   * @return dsspread2
   */
  public double getDsspread2() {
    return dsspread2;
  }

  /**
   * Error on the third of three distance hyperbola parameters used to compute the coda shape
   * spreading factor (Mayeda et al., 2003).
   * <p>
   * Units: km
   * 
   * @param dsspread2
   */
  public Coda_shape setDsspread2(double dsspread2) {
    this.dsspread2 = dsspread2;
    setHash(null);
    return this;
  }

  /**
   * CSS 2D correction identifier
   * 
   * @return ssp2dcorid
   */
  public long getSsp2dcorid() {
    return ssp2dcorid;
  }

  /**
   * CSS 2D correction identifier
   * 
   * @param ssp2dcorid
   * @throws IllegalArgumentException if ssp2dcorid >= 1000000000
   */
  public Coda_shape setSsp2dcorid(long ssp2dcorid) {
    if (ssp2dcorid >= 1000000000L)
      throw new IllegalArgumentException(
          "ssp2dcorid=" + ssp2dcorid + " but cannot be >= 1000000000");
    this.ssp2dcorid = ssp2dcorid;
    setHash(null);
    return this;
  }

  /**
   * Coda shape decay (CSD) exponent term 0. First of three distance hyperbola parameters used to
   * compute the coda shape attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: 1/s
   * 
   * @return b0
   */
  public double getB0() {
    return b0;
  }

  /**
   * Coda shape decay (CSD) exponent term 0. First of three distance hyperbola parameters used to
   * compute the coda shape attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: 1/s
   * 
   * @param b0
   */
  public Coda_shape setB0(double b0) {
    this.b0 = b0;
    setHash(null);
    return this;
  }

  /**
   * Error on the first of three distance hyperbola parameters used to compute the coda shape
   * attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: 1/s
   * 
   * @return db0
   */
  public double getDb0() {
    return db0;
  }

  /**
   * Error on the first of three distance hyperbola parameters used to compute the coda shape
   * attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: 1/s
   * 
   * @param db0
   */
  public Coda_shape setDb0(double db0) {
    this.db0 = db0;
    setHash(null);
    return this;
  }

  /**
   * Second of three distance hyperbola parameters used to compute the coda shape attenuation factor
   * (Mayeda et al., 2003); undefined implies zero.
   * <p>
   * Units: km/s
   * 
   * @return b1
   */
  public double getB1() {
    return b1;
  }

  /**
   * Second of three distance hyperbola parameters used to compute the coda shape attenuation factor
   * (Mayeda et al., 2003); undefined implies zero.
   * <p>
   * Units: km/s
   * 
   * @param b1
   */
  public Coda_shape setB1(double b1) {
    this.b1 = b1;
    setHash(null);
    return this;
  }

  /**
   * Error on the second of three distance hyperbola parameters used to compute the coda shape
   * attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: km/s
   * 
   * @return db1
   */
  public double getDb1() {
    return db1;
  }

  /**
   * Error on the second of three distance hyperbola parameters used to compute the coda shape
   * attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: km/s
   * 
   * @param db1
   */
  public Coda_shape setDb1(double db1) {
    this.db1 = db1;
    setHash(null);
    return this;
  }

  /**
   * Third of three distance hyperbola parameters used to compute the coda shape attenuation factor
   * (Mayeda et al., 2003), undefined implies infinite.
   * <p>
   * Units: km
   * 
   * @return b2
   */
  public double getB2() {
    return b2;
  }

  /**
   * Third of three distance hyperbola parameters used to compute the coda shape attenuation factor
   * (Mayeda et al., 2003), undefined implies infinite.
   * <p>
   * Units: km
   * 
   * @param b2
   */
  public Coda_shape setB2(double b2) {
    this.b2 = b2;
    setHash(null);
    return this;
  }

  /**
   * Error on the third of three distance hyperbola parameters used to compute the coda shape
   * attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: km
   * 
   * @return db2
   */
  public double getDb2() {
    return db2;
  }

  /**
   * Error on the third of three distance hyperbola parameters used to compute the coda shape
   * attenuation factor (Mayeda et al., 2003).
   * <p>
   * Units: km
   * 
   * @param db2
   */
  public Coda_shape setDb2(double db2) {
    this.db2 = db2;
    setHash(null);
    return this;
  }

  /**
   * Coda shape decay 2d identifier
   * 
   * @return b2dcorid
   */
  public long getB2dcorid() {
    return b2dcorid;
  }

  /**
   * Coda shape decay 2d identifier
   * 
   * @param b2dcorid
   * @throws IllegalArgumentException if b2dcorid >= 1000000000
   */
  public Coda_shape setB2dcorid(long b2dcorid) {
    if (b2dcorid >= 1000000000L)
      throw new IllegalArgumentException("b2dcorid=" + b2dcorid + " but cannot be >= 1000000000");
    this.b2dcorid = b2dcorid;
    setHash(null);
    return this;
  }

  /**
   * Minimum calibrated distance for 1-D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   * 
   * @return dmin
   */
  public double getDmin() {
    return dmin;
  }

  /**
   * Minimum calibrated distance for 1-D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   * 
   * @param dmin
   */
  public Coda_shape setDmin(double dmin) {
    this.dmin = dmin;
    setHash(null);
    return this;
  }

  /**
   * Maximum calibrated distance for 1D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   * 
   * @return dmax
   */
  public double getDmax() {
    return dmax;
  }

  /**
   * Maximum calibrated distance for 1D models. Undefined field implies no limit check should be
   * performed in applying calibration to new data. The limits dmin/dmax apply only to the
   * correction described by the table in which they appear and are not necessarily the same from
   * one 1D correction to the next.
   * <p>
   * Units: km
   * 
   * @param dmax
   */
  public Coda_shape setDmax(double dmax) {
    this.dmax = dmax;
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
  public Coda_shape setAuth(String auth) {
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
  public Coda_shape setCommid(long commid) {
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
