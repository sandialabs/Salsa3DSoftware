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
 * predict_sh
 */
public class Predict_sh extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Slowness prediction identifier
   */
  private long predshid;

  static final public long PREDSHID_NA = Long.MIN_VALUE;

  /**
   * Slowness correction surface identifier.
   */
  private long shcorrsurfid;

  static final public long SHCORRSURFID_NA = Long.MIN_VALUE;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta</I>, <I>chan</I> and <I>time</I>.
   */
  private long arid;

  static final public long ARID_NA = Long.MIN_VALUE;

  /**
   * Base model name.
   */
  private String modname;

  static final public String MODNAME_NA = "-";

  /**
   * Region name assigned by the author of the kbcit project.
   */
  private String regname;

  static final public String REGNAME_NA = "-";

  /**
   * Total predicted slowness
   * <p>
   * Units: s/degree
   */
  private double model_sh;

  static final public double MODEL_SH_NA = Double.NaN;

  /**
   * Uncertainty of the total predicted slowness
   * <p>
   * Units: s/degree
   */
  private double model_unc_sh;

  static final public double MODEL_UNC_SH_NA = Double.NaN;

  /**
   * Base component of the predicted slowness
   * <p>
   * Units: s/degree
   */
  private double base_sh;

  static final public double BASE_SH_NA = -1;

  /**
   * Uncertainty of the base component of the slowness
   * <p>
   * Units: s/degree
   */
  private double base_unc_sh;

  static final public double BASE_UNC_SH_NA = Double.NaN;

  /**
   * Path correction component of the predicted slowness
   * <p>
   * Units: s/degree
   */
  private double path_corr_sh;

  static final public double PATH_CORR_SH_NA = Double.NaN;

  /**
   * Uncertainty in path correction component of the predicted slowness
   * <p>
   * Units: s/degree
   */
  private double path_unc_sh;

  static final public double PATH_UNC_SH_NA = -1;

  /**
   * Three-dimensional Earth model correction component of the predicted slowness
   * <p>
   * Units: s/degree
   */
  private double d3_corr_sh;

  static final public double D3_CORR_SH_NA = -999;

  /**
   * Uncertainty of the three-dimensional Earth model correction component of the predicted slowness
   * <p>
   * Units: s/degree
   */
  private double d3_unc_sh;

  static final public double D3_UNC_SH_NA = Double.NaN;

  /**
   * Static slowness correction for a given station, to be added regardless of source position.
   * <p>
   * Units: s/degree
   */
  private double bulk_corr_sh;

  static final public double BULK_CORR_SH_NA = -999;

  /**
   * JHD correction component of the predicted slowness
   * <p>
   * Units: s/degree
   */
  private double jhd_corr_sh;

  static final public double JHD_CORR_SH_NA = -999;

  /**
   * Total uncertainty of the slowness (model plus pick)
   * <p>
   * Units: s/degree
   */
  private double total_unc_sh;

  static final public double TOTAL_UNC_SH_NA = Double.NaN;

  /**
   * Derivative of the predicted slowness with respect to longitude
   * <p>
   * Units: s/degree^2
   */
  private double dsh_dlon;

  static final public double DSH_DLON_NA = -999;

  /**
   * Derivative of the predicted slowness with respect to latitude
   * <p>
   * Units: s/degree^2
   */
  private double dsh_dlat;

  static final public double DSH_DLAT_NA = -999;

  /**
   * Derivative of the predicted slowness with respect to depth
   * <p>
   * Units: s/degree/km
   */
  private double dsh_dz;

  static final public double DSH_DZ_NA = Double.NaN;

  /**
   * The weight of each record contributing to the overall result
   * <p>
   * Units: tt: 1/seconds, az:1/degrees, sh:degree/second
   */
  private double weight;

  static final public double WEIGHT_NA = Double.NaN;

  /**
   * Error code
   */
  private String err_code;

  static final public String ERR_CODE_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("predshid", Columns.FieldType.LONG, "%d");
    columns.add("shcorrsurfid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("arid", Columns.FieldType.LONG, "%d");
    columns.add("modname", Columns.FieldType.STRING, "%s");
    columns.add("regname", Columns.FieldType.STRING, "%s");
    columns.add("model_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("model_unc_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("base_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("base_unc_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("path_corr_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("path_unc_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("d3_corr_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("d3_unc_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("bulk_corr_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("jhd_corr_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("total_unc_sh", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("dsh_dlon", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("dsh_dlat", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("dsh_dz", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("weight", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("err_code", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Predict_sh(long predshid, long shcorrsurfid, long orid, long arid, String modname,
      String regname, double model_sh, double model_unc_sh, double base_sh, double base_unc_sh,
      double path_corr_sh, double path_unc_sh, double d3_corr_sh, double d3_unc_sh,
      double bulk_corr_sh, double jhd_corr_sh, double total_unc_sh, double dsh_dlon,
      double dsh_dlat, double dsh_dz, double weight, String err_code) {
    setValues(predshid, shcorrsurfid, orid, arid, modname, regname, model_sh, model_unc_sh, base_sh,
        base_unc_sh, path_corr_sh, path_unc_sh, d3_corr_sh, d3_unc_sh, bulk_corr_sh, jhd_corr_sh,
        total_unc_sh, dsh_dlon, dsh_dlat, dsh_dz, weight, err_code);
  }

  private void setValues(long predshid, long shcorrsurfid, long orid, long arid, String modname,
      String regname, double model_sh, double model_unc_sh, double base_sh, double base_unc_sh,
      double path_corr_sh, double path_unc_sh, double d3_corr_sh, double d3_unc_sh,
      double bulk_corr_sh, double jhd_corr_sh, double total_unc_sh, double dsh_dlon,
      double dsh_dlat, double dsh_dz, double weight, String err_code) {
    this.predshid = predshid;
    this.shcorrsurfid = shcorrsurfid;
    this.orid = orid;
    this.arid = arid;
    this.modname = modname;
    this.regname = regname;
    this.model_sh = model_sh;
    this.model_unc_sh = model_unc_sh;
    this.base_sh = base_sh;
    this.base_unc_sh = base_unc_sh;
    this.path_corr_sh = path_corr_sh;
    this.path_unc_sh = path_unc_sh;
    this.d3_corr_sh = d3_corr_sh;
    this.d3_unc_sh = d3_unc_sh;
    this.bulk_corr_sh = bulk_corr_sh;
    this.jhd_corr_sh = jhd_corr_sh;
    this.total_unc_sh = total_unc_sh;
    this.dsh_dlon = dsh_dlon;
    this.dsh_dlat = dsh_dlat;
    this.dsh_dz = dsh_dz;
    this.weight = weight;
    this.err_code = err_code;
  }

  /**
   * Copy constructor.
   */
  public Predict_sh(Predict_sh other) {
    this.predshid = other.getPredshid();
    this.shcorrsurfid = other.getShcorrsurfid();
    this.orid = other.getOrid();
    this.arid = other.getArid();
    this.modname = other.getModname();
    this.regname = other.getRegname();
    this.model_sh = other.getModel_sh();
    this.model_unc_sh = other.getModel_unc_sh();
    this.base_sh = other.getBase_sh();
    this.base_unc_sh = other.getBase_unc_sh();
    this.path_corr_sh = other.getPath_corr_sh();
    this.path_unc_sh = other.getPath_unc_sh();
    this.d3_corr_sh = other.getD3_corr_sh();
    this.d3_unc_sh = other.getD3_unc_sh();
    this.bulk_corr_sh = other.getBulk_corr_sh();
    this.jhd_corr_sh = other.getJhd_corr_sh();
    this.total_unc_sh = other.getTotal_unc_sh();
    this.dsh_dlon = other.getDsh_dlon();
    this.dsh_dlat = other.getDsh_dlat();
    this.dsh_dz = other.getDsh_dz();
    this.weight = other.getWeight();
    this.err_code = other.getErr_code();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Predict_sh() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(PREDSHID_NA, SHCORRSURFID_NA, ORID_NA, ARID_NA, MODNAME_NA, REGNAME_NA, MODEL_SH_NA,
        MODEL_UNC_SH_NA, BASE_SH_NA, BASE_UNC_SH_NA, PATH_CORR_SH_NA, PATH_UNC_SH_NA, D3_CORR_SH_NA,
        D3_UNC_SH_NA, BULK_CORR_SH_NA, JHD_CORR_SH_NA, TOTAL_UNC_SH_NA, DSH_DLON_NA, DSH_DLAT_NA,
        DSH_DZ_NA, WEIGHT_NA, ERR_CODE_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "modname":
        return modname;
      case "regname":
        return regname;
      case "err_code":
        return err_code;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "modname":
        modname = value;
        break;
      case "regname":
        regname = value;
        break;
      case "err_code":
        err_code = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "model_sh":
        return model_sh;
      case "model_unc_sh":
        return model_unc_sh;
      case "base_sh":
        return base_sh;
      case "base_unc_sh":
        return base_unc_sh;
      case "path_corr_sh":
        return path_corr_sh;
      case "path_unc_sh":
        return path_unc_sh;
      case "d3_corr_sh":
        return d3_corr_sh;
      case "d3_unc_sh":
        return d3_unc_sh;
      case "bulk_corr_sh":
        return bulk_corr_sh;
      case "jhd_corr_sh":
        return jhd_corr_sh;
      case "total_unc_sh":
        return total_unc_sh;
      case "dsh_dlon":
        return dsh_dlon;
      case "dsh_dlat":
        return dsh_dlat;
      case "dsh_dz":
        return dsh_dz;
      case "weight":
        return weight;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "model_sh":
        model_sh = value;
        break;
      case "model_unc_sh":
        model_unc_sh = value;
        break;
      case "base_sh":
        base_sh = value;
        break;
      case "base_unc_sh":
        base_unc_sh = value;
        break;
      case "path_corr_sh":
        path_corr_sh = value;
        break;
      case "path_unc_sh":
        path_unc_sh = value;
        break;
      case "d3_corr_sh":
        d3_corr_sh = value;
        break;
      case "d3_unc_sh":
        d3_unc_sh = value;
        break;
      case "bulk_corr_sh":
        bulk_corr_sh = value;
        break;
      case "jhd_corr_sh":
        jhd_corr_sh = value;
        break;
      case "total_unc_sh":
        total_unc_sh = value;
        break;
      case "dsh_dlon":
        dsh_dlon = value;
        break;
      case "dsh_dlat":
        dsh_dlat = value;
        break;
      case "dsh_dz":
        dsh_dz = value;
        break;
      case "weight":
        weight = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "predshid":
        return predshid;
      case "shcorrsurfid":
        return shcorrsurfid;
      case "orid":
        return orid;
      case "arid":
        return arid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "predshid":
        predshid = value;
        break;
      case "shcorrsurfid":
        shcorrsurfid = value;
        break;
      case "orid":
        orid = value;
        break;
      case "arid":
        arid = value;
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
  public Predict_sh(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Predict_sh(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), readString(input),
        readString(input), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Predict_sh(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), readString(input),
        readString(input), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Predict_sh(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Predict_sh(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getDouble(offset + 17), input.getDouble(offset + 18),
        input.getDouble(offset + 19), input.getDouble(offset + 20), input.getDouble(offset + 21),
        input.getString(offset + 22));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[22];
    values[0] = predshid;
    values[1] = shcorrsurfid;
    values[2] = orid;
    values[3] = arid;
    values[4] = modname;
    values[5] = regname;
    values[6] = model_sh;
    values[7] = model_unc_sh;
    values[8] = base_sh;
    values[9] = base_unc_sh;
    values[10] = path_corr_sh;
    values[11] = path_unc_sh;
    values[12] = d3_corr_sh;
    values[13] = d3_unc_sh;
    values[14] = bulk_corr_sh;
    values[15] = jhd_corr_sh;
    values[16] = total_unc_sh;
    values[17] = dsh_dlon;
    values[18] = dsh_dlat;
    values[19] = dsh_dz;
    values[20] = weight;
    values[21] = err_code;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[23];
    values[0] = predshid;
    values[1] = shcorrsurfid;
    values[2] = orid;
    values[3] = arid;
    values[4] = modname;
    values[5] = regname;
    values[6] = model_sh;
    values[7] = model_unc_sh;
    values[8] = base_sh;
    values[9] = base_unc_sh;
    values[10] = path_corr_sh;
    values[11] = path_unc_sh;
    values[12] = d3_corr_sh;
    values[13] = d3_unc_sh;
    values[14] = bulk_corr_sh;
    values[15] = jhd_corr_sh;
    values[16] = total_unc_sh;
    values[17] = dsh_dlon;
    values[18] = dsh_dlat;
    values[19] = dsh_dz;
    values[20] = weight;
    values[21] = err_code;
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
    output.writeLong(predshid);
    output.writeLong(shcorrsurfid);
    output.writeLong(orid);
    output.writeLong(arid);
    writeString(output, modname);
    writeString(output, regname);
    output.writeDouble(model_sh);
    output.writeDouble(model_unc_sh);
    output.writeDouble(base_sh);
    output.writeDouble(base_unc_sh);
    output.writeDouble(path_corr_sh);
    output.writeDouble(path_unc_sh);
    output.writeDouble(d3_corr_sh);
    output.writeDouble(d3_unc_sh);
    output.writeDouble(bulk_corr_sh);
    output.writeDouble(jhd_corr_sh);
    output.writeDouble(total_unc_sh);
    output.writeDouble(dsh_dlon);
    output.writeDouble(dsh_dlat);
    output.writeDouble(dsh_dz);
    output.writeDouble(weight);
    writeString(output, err_code);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(predshid);
    output.putLong(shcorrsurfid);
    output.putLong(orid);
    output.putLong(arid);
    writeString(output, modname);
    writeString(output, regname);
    output.putDouble(model_sh);
    output.putDouble(model_unc_sh);
    output.putDouble(base_sh);
    output.putDouble(base_unc_sh);
    output.putDouble(path_corr_sh);
    output.putDouble(path_unc_sh);
    output.putDouble(d3_corr_sh);
    output.putDouble(d3_unc_sh);
    output.putDouble(bulk_corr_sh);
    output.putDouble(jhd_corr_sh);
    output.putDouble(total_unc_sh);
    output.putDouble(dsh_dlon);
    output.putDouble(dsh_dlat);
    output.putDouble(dsh_dz);
    output.putDouble(weight);
    writeString(output, err_code);
  }

  /**
   * Read a Collection of Predict_sh objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Predict_sh objects.
   * @throws IOException
   */
  static public void readPredict_shs(BufferedReader input, Collection<Predict_sh> rows)
      throws IOException {
    String[] saved = Predict_sh.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Predict_sh
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Predict_sh(new Scanner(line)));
    }
    input.close();
    Predict_sh.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Predict_sh objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Predict_sh objects.
   * @throws IOException
   */
  static public void readPredict_shs(File inputFile, Collection<Predict_sh> rows)
      throws IOException {
    readPredict_shs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Predict_sh objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Predict_sh objects.
   * @throws IOException
   */
  static public void readPredict_shs(InputStream inputStream, Collection<Predict_sh> rows)
      throws IOException {
    readPredict_shs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Predict_sh objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Predict_sh objects
   * @throws IOException
   */
  static public Set<Predict_sh> readPredict_shs(BufferedReader input) throws IOException {
    Set<Predict_sh> rows = new LinkedHashSet<Predict_sh>();
    readPredict_shs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Predict_sh objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Predict_sh objects
   * @throws IOException
   */
  static public Set<Predict_sh> readPredict_shs(File inputFile) throws IOException {
    return readPredict_shs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Predict_sh objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Predict_sh objects
   * @throws IOException
   */
  static public Set<Predict_sh> readPredict_shs(InputStream input) throws IOException {
    return readPredict_shs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Predict_sh objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param predict_shs the Predict_sh objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Predict_sh> predict_shs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Predict_sh predict_sh : predict_shs)
      predict_sh.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Predict_sh objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param predict_shs the Predict_sh objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Predict_sh> predict_shs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Predict_sh predict_sh : predict_shs) {
        int i = 0;
        statement.setLong(++i, predict_sh.predshid);
        statement.setLong(++i, predict_sh.shcorrsurfid);
        statement.setLong(++i, predict_sh.orid);
        statement.setLong(++i, predict_sh.arid);
        statement.setString(++i, predict_sh.modname);
        statement.setString(++i, predict_sh.regname);
        statement.setDouble(++i, predict_sh.model_sh);
        statement.setDouble(++i, predict_sh.model_unc_sh);
        statement.setDouble(++i, predict_sh.base_sh);
        statement.setDouble(++i, predict_sh.base_unc_sh);
        statement.setDouble(++i, predict_sh.path_corr_sh);
        statement.setDouble(++i, predict_sh.path_unc_sh);
        statement.setDouble(++i, predict_sh.d3_corr_sh);
        statement.setDouble(++i, predict_sh.d3_unc_sh);
        statement.setDouble(++i, predict_sh.bulk_corr_sh);
        statement.setDouble(++i, predict_sh.jhd_corr_sh);
        statement.setDouble(++i, predict_sh.total_unc_sh);
        statement.setDouble(++i, predict_sh.dsh_dlon);
        statement.setDouble(++i, predict_sh.dsh_dlat);
        statement.setDouble(++i, predict_sh.dsh_dz);
        statement.setDouble(++i, predict_sh.weight);
        statement.setString(++i, predict_sh.err_code);
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
   *        Predict_sh table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Predict_sh> readPredict_shs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Predict_sh> results = new HashSet<Predict_sh>();
    readPredict_shs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Predict_sh table.
   * @param predict_shs
   * @throws SQLException
   */
  static public void readPredict_shs(Connection connection, String selectStatement,
      Set<Predict_sh> predict_shs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        predict_shs.add(new Predict_sh(rs));
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
   * this Predict_sh object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Predict_sh object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "predshid, shcorrsurfid, orid, arid, modname, regname, model_sh, model_unc_sh, base_sh, base_unc_sh, path_corr_sh, path_unc_sh, d3_corr_sh, d3_unc_sh, bulk_corr_sh, jhd_corr_sh, total_unc_sh, dsh_dlon, dsh_dlat, dsh_dz, weight, err_code, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(predshid)).append(", ");
    sql.append(Long.toString(shcorrsurfid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(arid)).append(", ");
    sql.append("'").append(modname).append("', ");
    sql.append("'").append(regname).append("', ");
    sql.append(Double.toString(model_sh)).append(", ");
    sql.append(Double.toString(model_unc_sh)).append(", ");
    sql.append(Double.toString(base_sh)).append(", ");
    sql.append(Double.toString(base_unc_sh)).append(", ");
    sql.append(Double.toString(path_corr_sh)).append(", ");
    sql.append(Double.toString(path_unc_sh)).append(", ");
    sql.append(Double.toString(d3_corr_sh)).append(", ");
    sql.append(Double.toString(d3_unc_sh)).append(", ");
    sql.append(Double.toString(bulk_corr_sh)).append(", ");
    sql.append(Double.toString(jhd_corr_sh)).append(", ");
    sql.append(Double.toString(total_unc_sh)).append(", ");
    sql.append(Double.toString(dsh_dlon)).append(", ");
    sql.append(Double.toString(dsh_dlat)).append(", ");
    sql.append(Double.toString(dsh_dz)).append(", ");
    sql.append(Double.toString(weight)).append(", ");
    sql.append("'").append(err_code).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Predict_sh in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Predict_sh in the database
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
   * Generate a sql script to create a table of type Predict_sh in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Predict_sh in the database
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
    buf.append("predshid     number(9)            NOT NULL,\n");
    buf.append("shcorrsurfid number(9)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("arid         number(9)            NOT NULL,\n");
    buf.append("modname      varchar2(60)         NOT NULL,\n");
    buf.append("regname      varchar2(60)         NOT NULL,\n");
    buf.append("model_sh     float(53)            NOT NULL,\n");
    buf.append("model_unc_sh float(53)            NOT NULL,\n");
    buf.append("base_sh      float(53)            NOT NULL,\n");
    buf.append("base_unc_sh  float(53)            NOT NULL,\n");
    buf.append("path_corr_sh float(53)            NOT NULL,\n");
    buf.append("path_unc_sh  float(53)            NOT NULL,\n");
    buf.append("d3_corr_sh   float(53)            NOT NULL,\n");
    buf.append("d3_unc_sh    float(53)            NOT NULL,\n");
    buf.append("bulk_corr_sh float(53)            NOT NULL,\n");
    buf.append("jhd_corr_sh  float(53)            NOT NULL,\n");
    buf.append("total_unc_sh float(53)            NOT NULL,\n");
    buf.append("dsh_dlon     float(53)            NOT NULL,\n");
    buf.append("dsh_dlat     float(53)            NOT NULL,\n");
    buf.append("dsh_dz       float(53)            NOT NULL,\n");
    buf.append("weight       float(53)            NOT NULL,\n");
    buf.append("err_code     varchar2(30)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (predshid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (shcorrsurfid,orid,arid)");
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
    return 314;
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
    return (other instanceof Predict_sh) && ((Predict_sh) other).predshid == predshid;
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
    return (other instanceof Predict_sh) && ((Predict_sh) other).shcorrsurfid == shcorrsurfid
        && ((Predict_sh) other).orid == orid && ((Predict_sh) other).arid == arid;
  }

  /**
   * Slowness prediction identifier
   * 
   * @return predshid
   */
  public long getPredshid() {
    return predshid;
  }

  /**
   * Slowness prediction identifier
   * 
   * @param predshid
   * @throws IllegalArgumentException if predshid >= 1000000000
   */
  public Predict_sh setPredshid(long predshid) {
    if (predshid >= 1000000000L)
      throw new IllegalArgumentException("predshid=" + predshid + " but cannot be >= 1000000000");
    this.predshid = predshid;
    setHash(null);
    return this;
  }

  /**
   * Slowness correction surface identifier.
   * 
   * @return shcorrsurfid
   */
  public long getShcorrsurfid() {
    return shcorrsurfid;
  }

  /**
   * Slowness correction surface identifier.
   * 
   * @param shcorrsurfid
   * @throws IllegalArgumentException if shcorrsurfid >= 1000000000
   */
  public Predict_sh setShcorrsurfid(long shcorrsurfid) {
    if (shcorrsurfid >= 1000000000L)
      throw new IllegalArgumentException(
          "shcorrsurfid=" + shcorrsurfid + " but cannot be >= 1000000000");
    this.shcorrsurfid = shcorrsurfid;
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
  public Predict_sh setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
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
  public Predict_sh setArid(long arid) {
    if (arid >= 1000000000L)
      throw new IllegalArgumentException("arid=" + arid + " but cannot be >= 1000000000");
    this.arid = arid;
    setHash(null);
    return this;
  }

  /**
   * Base model name.
   * 
   * @return modname
   */
  public String getModname() {
    return modname;
  }

  /**
   * Base model name.
   * 
   * @param modname
   * @throws IllegalArgumentException if modname.length() >= 60
   */
  public Predict_sh setModname(String modname) {
    if (modname.length() > 60)
      throw new IllegalArgumentException(
          String.format("modname.length() cannot be > 60.  modname=%s", modname));
    this.modname = modname;
    setHash(null);
    return this;
  }

  /**
   * Region name assigned by the author of the kbcit project.
   * 
   * @return regname
   */
  public String getRegname() {
    return regname;
  }

  /**
   * Region name assigned by the author of the kbcit project.
   * 
   * @param regname
   * @throws IllegalArgumentException if regname.length() >= 60
   */
  public Predict_sh setRegname(String regname) {
    if (regname.length() > 60)
      throw new IllegalArgumentException(
          String.format("regname.length() cannot be > 60.  regname=%s", regname));
    this.regname = regname;
    setHash(null);
    return this;
  }

  /**
   * Total predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @return model_sh
   */
  public double getModel_sh() {
    return model_sh;
  }

  /**
   * Total predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @param model_sh
   */
  public Predict_sh setModel_sh(double model_sh) {
    this.model_sh = model_sh;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty of the total predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @return model_unc_sh
   */
  public double getModel_unc_sh() {
    return model_unc_sh;
  }

  /**
   * Uncertainty of the total predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @param model_unc_sh
   */
  public Predict_sh setModel_unc_sh(double model_unc_sh) {
    this.model_unc_sh = model_unc_sh;
    setHash(null);
    return this;
  }

  /**
   * Base component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @return base_sh
   */
  public double getBase_sh() {
    return base_sh;
  }

  /**
   * Base component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @param base_sh
   */
  public Predict_sh setBase_sh(double base_sh) {
    this.base_sh = base_sh;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty of the base component of the slowness
   * <p>
   * Units: s/degree
   * 
   * @return base_unc_sh
   */
  public double getBase_unc_sh() {
    return base_unc_sh;
  }

  /**
   * Uncertainty of the base component of the slowness
   * <p>
   * Units: s/degree
   * 
   * @param base_unc_sh
   */
  public Predict_sh setBase_unc_sh(double base_unc_sh) {
    this.base_unc_sh = base_unc_sh;
    setHash(null);
    return this;
  }

  /**
   * Path correction component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @return path_corr_sh
   */
  public double getPath_corr_sh() {
    return path_corr_sh;
  }

  /**
   * Path correction component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @param path_corr_sh
   */
  public Predict_sh setPath_corr_sh(double path_corr_sh) {
    this.path_corr_sh = path_corr_sh;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty in path correction component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @return path_unc_sh
   */
  public double getPath_unc_sh() {
    return path_unc_sh;
  }

  /**
   * Uncertainty in path correction component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @param path_unc_sh
   */
  public Predict_sh setPath_unc_sh(double path_unc_sh) {
    this.path_unc_sh = path_unc_sh;
    setHash(null);
    return this;
  }

  /**
   * Three-dimensional Earth model correction component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @return d3_corr_sh
   */
  public double getD3_corr_sh() {
    return d3_corr_sh;
  }

  /**
   * Three-dimensional Earth model correction component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @param d3_corr_sh
   */
  public Predict_sh setD3_corr_sh(double d3_corr_sh) {
    this.d3_corr_sh = d3_corr_sh;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty of the three-dimensional Earth model correction component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @return d3_unc_sh
   */
  public double getD3_unc_sh() {
    return d3_unc_sh;
  }

  /**
   * Uncertainty of the three-dimensional Earth model correction component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @param d3_unc_sh
   */
  public Predict_sh setD3_unc_sh(double d3_unc_sh) {
    this.d3_unc_sh = d3_unc_sh;
    setHash(null);
    return this;
  }

  /**
   * Static slowness correction for a given station, to be added regardless of source position.
   * <p>
   * Units: s/degree
   * 
   * @return bulk_corr_sh
   */
  public double getBulk_corr_sh() {
    return bulk_corr_sh;
  }

  /**
   * Static slowness correction for a given station, to be added regardless of source position.
   * <p>
   * Units: s/degree
   * 
   * @param bulk_corr_sh
   */
  public Predict_sh setBulk_corr_sh(double bulk_corr_sh) {
    this.bulk_corr_sh = bulk_corr_sh;
    setHash(null);
    return this;
  }

  /**
   * JHD correction component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @return jhd_corr_sh
   */
  public double getJhd_corr_sh() {
    return jhd_corr_sh;
  }

  /**
   * JHD correction component of the predicted slowness
   * <p>
   * Units: s/degree
   * 
   * @param jhd_corr_sh
   */
  public Predict_sh setJhd_corr_sh(double jhd_corr_sh) {
    this.jhd_corr_sh = jhd_corr_sh;
    setHash(null);
    return this;
  }

  /**
   * Total uncertainty of the slowness (model plus pick)
   * <p>
   * Units: s/degree
   * 
   * @return total_unc_sh
   */
  public double getTotal_unc_sh() {
    return total_unc_sh;
  }

  /**
   * Total uncertainty of the slowness (model plus pick)
   * <p>
   * Units: s/degree
   * 
   * @param total_unc_sh
   */
  public Predict_sh setTotal_unc_sh(double total_unc_sh) {
    this.total_unc_sh = total_unc_sh;
    setHash(null);
    return this;
  }

  /**
   * Derivative of the predicted slowness with respect to longitude
   * <p>
   * Units: s/degree^2
   * 
   * @return dsh_dlon
   */
  public double getDsh_dlon() {
    return dsh_dlon;
  }

  /**
   * Derivative of the predicted slowness with respect to longitude
   * <p>
   * Units: s/degree^2
   * 
   * @param dsh_dlon
   */
  public Predict_sh setDsh_dlon(double dsh_dlon) {
    this.dsh_dlon = dsh_dlon;
    setHash(null);
    return this;
  }

  /**
   * Derivative of the predicted slowness with respect to latitude
   * <p>
   * Units: s/degree^2
   * 
   * @return dsh_dlat
   */
  public double getDsh_dlat() {
    return dsh_dlat;
  }

  /**
   * Derivative of the predicted slowness with respect to latitude
   * <p>
   * Units: s/degree^2
   * 
   * @param dsh_dlat
   */
  public Predict_sh setDsh_dlat(double dsh_dlat) {
    this.dsh_dlat = dsh_dlat;
    setHash(null);
    return this;
  }

  /**
   * Derivative of the predicted slowness with respect to depth
   * <p>
   * Units: s/degree/km
   * 
   * @return dsh_dz
   */
  public double getDsh_dz() {
    return dsh_dz;
  }

  /**
   * Derivative of the predicted slowness with respect to depth
   * <p>
   * Units: s/degree/km
   * 
   * @param dsh_dz
   */
  public Predict_sh setDsh_dz(double dsh_dz) {
    this.dsh_dz = dsh_dz;
    setHash(null);
    return this;
  }

  /**
   * The weight of each record contributing to the overall result
   * <p>
   * Units: tt: 1/seconds, az:1/degrees, sh:degree/second
   * 
   * @return weight
   */
  public double getWeight() {
    return weight;
  }

  /**
   * The weight of each record contributing to the overall result
   * <p>
   * Units: tt: 1/seconds, az:1/degrees, sh:degree/second
   * 
   * @param weight
   */
  public Predict_sh setWeight(double weight) {
    this.weight = weight;
    setHash(null);
    return this;
  }

  /**
   * Error code
   * 
   * @return err_code
   */
  public String getErr_code() {
    return err_code;
  }

  /**
   * Error code
   * 
   * @param err_code
   * @throws IllegalArgumentException if err_code.length() >= 30
   */
  public Predict_sh setErr_code(String err_code) {
    if (err_code.length() > 30)
      throw new IllegalArgumentException(
          String.format("err_code.length() cannot be > 30.  err_code=%s", err_code));
    this.err_code = err_code;
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
