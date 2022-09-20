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
 * path_corr
 */
public class Path_corr extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique magyield identifier for a particular codamag narrowband envelope calibration
   */
  private long magyieldid;

  static final public long MAGYIELDID_NA = Long.MIN_VALUE;

  /**
   * Initial spreading rate for the extended Street-Herrmann path model.
   */
  private double pspread1;

  static final public double PSPREAD1_NA = Double.NaN;

  /**
   * Error on initial spreading rate for the extended Street-Herrmann path model.
   */
  private double dpspread1;

  static final public double DPSPREAD1_NA = -1;

  /**
   * Final spreading rate for the extended Street-Herrmann path model.
   */
  private double pspread2;

  static final public double PSPREAD2_NA = Double.NaN;

  /**
   * Error on final spreading rate for the extended Street-Herrmann path model.
   */
  private double dpspread2;

  static final public double DPSPREAD2_NA = -1;

  /**
   * Critical distance term in extended Street-Herrmann model.
   * <p>
   * Units: km
   */
  private double xcross;

  static final public double XCROSS_NA = Double.NaN;

  /**
   * Error on critical distance term in extended Street-Herrmann model.
   * <p>
   * Units: km
   */
  private double dxcross;

  static final public double DXCROSS_NA = -1;

  /**
   * Transition term in extended Street-Herrmann model.
   * 
   */
  private double xtrans;

  static final public double XTRANS_NA = Double.NaN;

  /**
   * Error on transition term in extended Street-Herrmann model.
   */
  private double dxtrans;

  static final public double DXTRANS_NA = -1;

  /**
   * Quality factor (Q) for one-dimensional path correction
   */
  private double q;

  static final public double Q_NA = -1;

  /**
   * Quality factor (Q) error for one-dimensional path correction.
   */
  private double delq;

  static final public double DELQ_NA = -1;

  /**
   * Tomographic identifier.
   */
  private long tomoid;

  static final public long TOMOID_NA = -1;

  /**
   * Group velocity used to calculate path correction Q term, undefined only if q also undefined,
   * implying zero attenuation.
   * <p>
   * Units: km/s
   */
  private double vphase;

  static final public double VPHASE_NA = Double.NaN;

  /**
   * Threshold distance used to avoid singularity in path correction at small distance when
   * <I>pspread1</I> not zero. For distance less than <I>nfieldlim</I>, reset distance to
   * <I>nfieldlim</I> for purposes of computing a path correction.
   * <p>
   * Units: km
   */
  private double nfieldlim;

  static final public double NFIELDLIM_NA = Double.NaN;

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
    columns.add("pspread1", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("dpspread1", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("pspread2", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("dpspread2", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("xcross", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("dxcross", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("xtrans", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("dxtrans", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("q", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("delq", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("tomoid", Columns.FieldType.LONG, "%d");
    columns.add("vphase", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("nfieldlim", Columns.FieldType.DOUBLE, "%1.4f");
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
  public Path_corr(long magyieldid, double pspread1, double dpspread1, double pspread2,
      double dpspread2, double xcross, double dxcross, double xtrans, double dxtrans, double q,
      double delq, long tomoid, double vphase, double nfieldlim, double dmin, double dmax,
      String auth, long commid) {
    setValues(magyieldid, pspread1, dpspread1, pspread2, dpspread2, xcross, dxcross, xtrans,
        dxtrans, q, delq, tomoid, vphase, nfieldlim, dmin, dmax, auth, commid);
  }

  private void setValues(long magyieldid, double pspread1, double dpspread1, double pspread2,
      double dpspread2, double xcross, double dxcross, double xtrans, double dxtrans, double q,
      double delq, long tomoid, double vphase, double nfieldlim, double dmin, double dmax,
      String auth, long commid) {
    this.magyieldid = magyieldid;
    this.pspread1 = pspread1;
    this.dpspread1 = dpspread1;
    this.pspread2 = pspread2;
    this.dpspread2 = dpspread2;
    this.xcross = xcross;
    this.dxcross = dxcross;
    this.xtrans = xtrans;
    this.dxtrans = dxtrans;
    this.q = q;
    this.delq = delq;
    this.tomoid = tomoid;
    this.vphase = vphase;
    this.nfieldlim = nfieldlim;
    this.dmin = dmin;
    this.dmax = dmax;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Path_corr(Path_corr other) {
    this.magyieldid = other.getMagyieldid();
    this.pspread1 = other.getPspread1();
    this.dpspread1 = other.getDpspread1();
    this.pspread2 = other.getPspread2();
    this.dpspread2 = other.getDpspread2();
    this.xcross = other.getXcross();
    this.dxcross = other.getDxcross();
    this.xtrans = other.getXtrans();
    this.dxtrans = other.getDxtrans();
    this.q = other.getQ();
    this.delq = other.getDelq();
    this.tomoid = other.getTomoid();
    this.vphase = other.getVphase();
    this.nfieldlim = other.getNfieldlim();
    this.dmin = other.getDmin();
    this.dmax = other.getDmax();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Path_corr() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MAGYIELDID_NA, PSPREAD1_NA, DPSPREAD1_NA, PSPREAD2_NA, DPSPREAD2_NA, XCROSS_NA,
        DXCROSS_NA, XTRANS_NA, DXTRANS_NA, Q_NA, DELQ_NA, TOMOID_NA, VPHASE_NA, NFIELDLIM_NA,
        DMIN_NA, DMAX_NA, AUTH_NA, COMMID_NA);
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
      case "pspread1":
        return pspread1;
      case "dpspread1":
        return dpspread1;
      case "pspread2":
        return pspread2;
      case "dpspread2":
        return dpspread2;
      case "xcross":
        return xcross;
      case "dxcross":
        return dxcross;
      case "xtrans":
        return xtrans;
      case "dxtrans":
        return dxtrans;
      case "q":
        return q;
      case "delq":
        return delq;
      case "vphase":
        return vphase;
      case "nfieldlim":
        return nfieldlim;
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
      case "pspread1":
        pspread1 = value;
        break;
      case "dpspread1":
        dpspread1 = value;
        break;
      case "pspread2":
        pspread2 = value;
        break;
      case "dpspread2":
        dpspread2 = value;
        break;
      case "xcross":
        xcross = value;
        break;
      case "dxcross":
        dxcross = value;
        break;
      case "xtrans":
        xtrans = value;
        break;
      case "dxtrans":
        dxtrans = value;
        break;
      case "q":
        q = value;
        break;
      case "delq":
        delq = value;
        break;
      case "vphase":
        vphase = value;
        break;
      case "nfieldlim":
        nfieldlim = value;
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
      case "tomoid":
        return tomoid;
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
      case "tomoid":
        tomoid = value;
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
  public Path_corr(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Path_corr(DataInputStream input) throws IOException {
    this(input.readLong(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readLong(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Path_corr(ByteBuffer input) {
    this(input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getLong(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), readString(input),
        input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Path_corr(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Path_corr(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getLong(offset + 12),
        input.getDouble(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getString(offset + 17), input.getLong(offset + 18));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[18];
    values[0] = magyieldid;
    values[1] = pspread1;
    values[2] = dpspread1;
    values[3] = pspread2;
    values[4] = dpspread2;
    values[5] = xcross;
    values[6] = dxcross;
    values[7] = xtrans;
    values[8] = dxtrans;
    values[9] = q;
    values[10] = delq;
    values[11] = tomoid;
    values[12] = vphase;
    values[13] = nfieldlim;
    values[14] = dmin;
    values[15] = dmax;
    values[16] = auth;
    values[17] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[19];
    values[0] = magyieldid;
    values[1] = pspread1;
    values[2] = dpspread1;
    values[3] = pspread2;
    values[4] = dpspread2;
    values[5] = xcross;
    values[6] = dxcross;
    values[7] = xtrans;
    values[8] = dxtrans;
    values[9] = q;
    values[10] = delq;
    values[11] = tomoid;
    values[12] = vphase;
    values[13] = nfieldlim;
    values[14] = dmin;
    values[15] = dmax;
    values[16] = auth;
    values[17] = commid;
    values[18] = lddate;
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
    output.writeDouble(pspread1);
    output.writeDouble(dpspread1);
    output.writeDouble(pspread2);
    output.writeDouble(dpspread2);
    output.writeDouble(xcross);
    output.writeDouble(dxcross);
    output.writeDouble(xtrans);
    output.writeDouble(dxtrans);
    output.writeDouble(q);
    output.writeDouble(delq);
    output.writeLong(tomoid);
    output.writeDouble(vphase);
    output.writeDouble(nfieldlim);
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
    output.putDouble(pspread1);
    output.putDouble(dpspread1);
    output.putDouble(pspread2);
    output.putDouble(dpspread2);
    output.putDouble(xcross);
    output.putDouble(dxcross);
    output.putDouble(xtrans);
    output.putDouble(dxtrans);
    output.putDouble(q);
    output.putDouble(delq);
    output.putLong(tomoid);
    output.putDouble(vphase);
    output.putDouble(nfieldlim);
    output.putDouble(dmin);
    output.putDouble(dmax);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Path_corr objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Path_corr objects.
   * @throws IOException
   */
  static public void readPath_corrs(BufferedReader input, Collection<Path_corr> rows)
      throws IOException {
    String[] saved = Path_corr.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Path_corr
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Path_corr(new Scanner(line)));
    }
    input.close();
    Path_corr.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Path_corr objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Path_corr objects.
   * @throws IOException
   */
  static public void readPath_corrs(File inputFile, Collection<Path_corr> rows) throws IOException {
    readPath_corrs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Path_corr objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Path_corr objects.
   * @throws IOException
   */
  static public void readPath_corrs(InputStream inputStream, Collection<Path_corr> rows)
      throws IOException {
    readPath_corrs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Path_corr objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Path_corr objects
   * @throws IOException
   */
  static public Set<Path_corr> readPath_corrs(BufferedReader input) throws IOException {
    Set<Path_corr> rows = new LinkedHashSet<Path_corr>();
    readPath_corrs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Path_corr objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Path_corr objects
   * @throws IOException
   */
  static public Set<Path_corr> readPath_corrs(File inputFile) throws IOException {
    return readPath_corrs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Path_corr objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Path_corr objects
   * @throws IOException
   */
  static public Set<Path_corr> readPath_corrs(InputStream input) throws IOException {
    return readPath_corrs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Path_corr objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param path_corrs the Path_corr objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Path_corr> path_corrs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Path_corr path_corr : path_corrs)
      path_corr.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Path_corr objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param path_corrs the Path_corr objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Path_corr> path_corrs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Path_corr path_corr : path_corrs) {
        int i = 0;
        statement.setLong(++i, path_corr.magyieldid);
        statement.setDouble(++i, path_corr.pspread1);
        statement.setDouble(++i, path_corr.dpspread1);
        statement.setDouble(++i, path_corr.pspread2);
        statement.setDouble(++i, path_corr.dpspread2);
        statement.setDouble(++i, path_corr.xcross);
        statement.setDouble(++i, path_corr.dxcross);
        statement.setDouble(++i, path_corr.xtrans);
        statement.setDouble(++i, path_corr.dxtrans);
        statement.setDouble(++i, path_corr.q);
        statement.setDouble(++i, path_corr.delq);
        statement.setLong(++i, path_corr.tomoid);
        statement.setDouble(++i, path_corr.vphase);
        statement.setDouble(++i, path_corr.nfieldlim);
        statement.setDouble(++i, path_corr.dmin);
        statement.setDouble(++i, path_corr.dmax);
        statement.setString(++i, path_corr.auth);
        statement.setLong(++i, path_corr.commid);
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
   *        Path_corr table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Path_corr> readPath_corrs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Path_corr> results = new HashSet<Path_corr>();
    readPath_corrs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Path_corr table.
   * @param path_corrs
   * @throws SQLException
   */
  static public void readPath_corrs(Connection connection, String selectStatement,
      Set<Path_corr> path_corrs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        path_corrs.add(new Path_corr(rs));
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
   * this Path_corr object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Path_corr object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "magyieldid, pspread1, dpspread1, pspread2, dpspread2, xcross, dxcross, xtrans, dxtrans, q, delq, tomoid, vphase, nfieldlim, dmin, dmax, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(magyieldid)).append(", ");
    sql.append(Double.toString(pspread1)).append(", ");
    sql.append(Double.toString(dpspread1)).append(", ");
    sql.append(Double.toString(pspread2)).append(", ");
    sql.append(Double.toString(dpspread2)).append(", ");
    sql.append(Double.toString(xcross)).append(", ");
    sql.append(Double.toString(dxcross)).append(", ");
    sql.append(Double.toString(xtrans)).append(", ");
    sql.append(Double.toString(dxtrans)).append(", ");
    sql.append(Double.toString(q)).append(", ");
    sql.append(Double.toString(delq)).append(", ");
    sql.append(Long.toString(tomoid)).append(", ");
    sql.append(Double.toString(vphase)).append(", ");
    sql.append(Double.toString(nfieldlim)).append(", ");
    sql.append(Double.toString(dmin)).append(", ");
    sql.append(Double.toString(dmax)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Path_corr in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Path_corr in the database
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
   * Generate a sql script to create a table of type Path_corr in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Path_corr in the database
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
    buf.append("pspread1     float(24)            NOT NULL,\n");
    buf.append("dpspread1    float(24)            NOT NULL,\n");
    buf.append("pspread2     float(24)            NOT NULL,\n");
    buf.append("dpspread2    float(24)            NOT NULL,\n");
    buf.append("xcross       float(24)            NOT NULL,\n");
    buf.append("dxcross      float(24)            NOT NULL,\n");
    buf.append("xtrans       float(24)            NOT NULL,\n");
    buf.append("dxtrans      float(24)            NOT NULL,\n");
    buf.append("q            float(24)            NOT NULL,\n");
    buf.append("delq         float(24)            NOT NULL,\n");
    buf.append("tomoid       number(9)            NOT NULL,\n");
    buf.append("vphase       float(24)            NOT NULL,\n");
    buf.append("nfieldlim    float(24)            NOT NULL,\n");
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
    return 160;
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
    return (other instanceof Path_corr) && ((Path_corr) other).magyieldid == magyieldid;
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
  public Path_corr setMagyieldid(long magyieldid) {
    if (magyieldid >= 1000000000L)
      throw new IllegalArgumentException(
          "magyieldid=" + magyieldid + " but cannot be >= 1000000000");
    this.magyieldid = magyieldid;
    setHash(null);
    return this;
  }

  /**
   * Initial spreading rate for the extended Street-Herrmann path model.
   * 
   * @return pspread1
   */
  public double getPspread1() {
    return pspread1;
  }

  /**
   * Initial spreading rate for the extended Street-Herrmann path model.
   * 
   * @param pspread1
   */
  public Path_corr setPspread1(double pspread1) {
    this.pspread1 = pspread1;
    setHash(null);
    return this;
  }

  /**
   * Error on initial spreading rate for the extended Street-Herrmann path model.
   * 
   * @return dpspread1
   */
  public double getDpspread1() {
    return dpspread1;
  }

  /**
   * Error on initial spreading rate for the extended Street-Herrmann path model.
   * 
   * @param dpspread1
   */
  public Path_corr setDpspread1(double dpspread1) {
    this.dpspread1 = dpspread1;
    setHash(null);
    return this;
  }

  /**
   * Final spreading rate for the extended Street-Herrmann path model.
   * 
   * @return pspread2
   */
  public double getPspread2() {
    return pspread2;
  }

  /**
   * Final spreading rate for the extended Street-Herrmann path model.
   * 
   * @param pspread2
   */
  public Path_corr setPspread2(double pspread2) {
    this.pspread2 = pspread2;
    setHash(null);
    return this;
  }

  /**
   * Error on final spreading rate for the extended Street-Herrmann path model.
   * 
   * @return dpspread2
   */
  public double getDpspread2() {
    return dpspread2;
  }

  /**
   * Error on final spreading rate for the extended Street-Herrmann path model.
   * 
   * @param dpspread2
   */
  public Path_corr setDpspread2(double dpspread2) {
    this.dpspread2 = dpspread2;
    setHash(null);
    return this;
  }

  /**
   * Critical distance term in extended Street-Herrmann model.
   * <p>
   * Units: km
   * 
   * @return xcross
   */
  public double getXcross() {
    return xcross;
  }

  /**
   * Critical distance term in extended Street-Herrmann model.
   * <p>
   * Units: km
   * 
   * @param xcross
   */
  public Path_corr setXcross(double xcross) {
    this.xcross = xcross;
    setHash(null);
    return this;
  }

  /**
   * Error on critical distance term in extended Street-Herrmann model.
   * <p>
   * Units: km
   * 
   * @return dxcross
   */
  public double getDxcross() {
    return dxcross;
  }

  /**
   * Error on critical distance term in extended Street-Herrmann model.
   * <p>
   * Units: km
   * 
   * @param dxcross
   */
  public Path_corr setDxcross(double dxcross) {
    this.dxcross = dxcross;
    setHash(null);
    return this;
  }

  /**
   * Transition term in extended Street-Herrmann model.
   * 
   * @return xtrans
   */
  public double getXtrans() {
    return xtrans;
  }

  /**
   * Transition term in extended Street-Herrmann model.
   * 
   * @param xtrans
   */
  public Path_corr setXtrans(double xtrans) {
    this.xtrans = xtrans;
    setHash(null);
    return this;
  }

  /**
   * Error on transition term in extended Street-Herrmann model.
   * 
   * @return dxtrans
   */
  public double getDxtrans() {
    return dxtrans;
  }

  /**
   * Error on transition term in extended Street-Herrmann model.
   * 
   * @param dxtrans
   */
  public Path_corr setDxtrans(double dxtrans) {
    this.dxtrans = dxtrans;
    setHash(null);
    return this;
  }

  /**
   * Quality factor (Q) for one-dimensional path correction
   * 
   * @return q
   */
  public double getQ() {
    return q;
  }

  /**
   * Quality factor (Q) for one-dimensional path correction
   * 
   * @param q
   */
  public Path_corr setQ(double q) {
    this.q = q;
    setHash(null);
    return this;
  }

  /**
   * Quality factor (Q) error for one-dimensional path correction.
   * 
   * @return delq
   */
  public double getDelq() {
    return delq;
  }

  /**
   * Quality factor (Q) error for one-dimensional path correction.
   * 
   * @param delq
   */
  public Path_corr setDelq(double delq) {
    this.delq = delq;
    setHash(null);
    return this;
  }

  /**
   * Tomographic identifier.
   * 
   * @return tomoid
   */
  public long getTomoid() {
    return tomoid;
  }

  /**
   * Tomographic identifier.
   * 
   * @param tomoid
   * @throws IllegalArgumentException if tomoid >= 1000000000
   */
  public Path_corr setTomoid(long tomoid) {
    if (tomoid >= 1000000000L)
      throw new IllegalArgumentException("tomoid=" + tomoid + " but cannot be >= 1000000000");
    this.tomoid = tomoid;
    setHash(null);
    return this;
  }

  /**
   * Group velocity used to calculate path correction Q term, undefined only if q also undefined,
   * implying zero attenuation.
   * <p>
   * Units: km/s
   * 
   * @return vphase
   */
  public double getVphase() {
    return vphase;
  }

  /**
   * Group velocity used to calculate path correction Q term, undefined only if q also undefined,
   * implying zero attenuation.
   * <p>
   * Units: km/s
   * 
   * @param vphase
   */
  public Path_corr setVphase(double vphase) {
    this.vphase = vphase;
    setHash(null);
    return this;
  }

  /**
   * Threshold distance used to avoid singularity in path correction at small distance when
   * <I>pspread1</I> not zero. For distance less than <I>nfieldlim</I>, reset distance to
   * <I>nfieldlim</I> for purposes of computing a path correction.
   * <p>
   * Units: km
   * 
   * @return nfieldlim
   */
  public double getNfieldlim() {
    return nfieldlim;
  }

  /**
   * Threshold distance used to avoid singularity in path correction at small distance when
   * <I>pspread1</I> not zero. For distance less than <I>nfieldlim</I>, reset distance to
   * <I>nfieldlim</I> for purposes of computing a path correction.
   * <p>
   * Units: km
   * 
   * @param nfieldlim
   */
  public Path_corr setNfieldlim(double nfieldlim) {
    this.nfieldlim = nfieldlim;
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
  public Path_corr setDmin(double dmin) {
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
  public Path_corr setDmax(double dmax) {
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
  public Path_corr setAuth(String auth) {
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
  public Path_corr setCommid(long commid) {
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
