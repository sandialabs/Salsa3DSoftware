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
 * predict_az
 */
public class Predict_az extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Azimuth prediction identifier
   */
  private long predazid;

  static final public long PREDAZID_NA = Long.MIN_VALUE;

  /**
   * Azimuth correction surface identifier.
   */
  private long azcorrsurfid;

  static final public long AZCORRSURFID_NA = Long.MIN_VALUE;

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
   * Total predicted azimuth
   * <p>
   * Units: degree
   */
  private double model_az;

  static final public double MODEL_AZ_NA = Double.NaN;

  /**
   * Uncertainty of the total predicted azimuth
   * <p>
   * Units: degree
   */
  private double model_unc_az;

  static final public double MODEL_UNC_AZ_NA = Double.NaN;

  /**
   * Base component of the predicted azimuth measured clockwise from North
   * <p>
   * Units: degree
   */
  private double base_az;

  static final public double BASE_AZ_NA = Double.NaN;

  /**
   * Uncertainty of the base component of the azimuth
   * <p>
   * Units: degree
   */
  private double base_unc_az;

  static final public double BASE_UNC_AZ_NA = -1;

  /**
   * Path correction component of the predicted azimuth
   * <p>
   * Units: degree
   */
  private double path_corr_az;

  static final public double PATH_CORR_AZ_NA = -999;

  /**
   * Uncertainty in path correction component of the predicted azimuth
   * <p>
   * Units: degree
   */
  private double path_unc_az;

  static final public double PATH_UNC_AZ_NA = -1;

  /**
   * Three-dimensional Earth model correction component of the predicted azimuth
   * <p>
   * Units: degree
   */
  private double d3_corr_az;

  static final public double D3_CORR_AZ_NA = -999;

  /**
   * Uncertainty of the three-dimensional Earth model correction component of the predicted azimuth
   * <p>
   * Units: degree
   */
  private double d3_unc_az;

  static final public double D3_UNC_AZ_NA = -1;

  /**
   * Static azimuth correction for a given station, to be added regardless of source position.
   * <p>
   * Units: degree
   */
  private double bulk_corr_az;

  static final public double BULK_CORR_AZ_NA = -999;

  /**
   * JHD correction component of the predicted azimuth
   * <p>
   * Units: degree
   */
  private double jhd_corr_az;

  static final public double JHD_CORR_AZ_NA = -999;

  /**
   * Total uncertainty of the azimuth (model plus pick)
   * <p>
   * Units: degree
   */
  private double total_unc_az;

  static final public double TOTAL_UNC_AZ_NA = Double.NaN;

  /**
   * Derivative of the predicted azimuth with respect to longitude
   * <p>
   * Units: degree/degree
   */
  private double daz_dlon;

  static final public double DAZ_DLON_NA = Double.NaN;

  /**
   * Derivative of the predicted azimuth with respect to latitude
   * <p>
   * Units: degree/degree
   */
  private double daz_dlat;

  static final public double DAZ_DLAT_NA = Double.NaN;

  /**
   * Derivative of the predicted azimuth with respect to depth
   * <p>
   * Units: degree/km
   */
  private double daz_dz;

  static final public double DAZ_DZ_NA = Double.NaN;

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
    columns.add("predazid", Columns.FieldType.LONG, "%d");
    columns.add("azcorrsurfid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("arid", Columns.FieldType.LONG, "%d");
    columns.add("modname", Columns.FieldType.STRING, "%s");
    columns.add("regname", Columns.FieldType.STRING, "%s");
    columns.add("model_az", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("model_unc_az", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("base_az", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("base_unc_az", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("path_corr_az", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("path_unc_az", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("d3_corr_az", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("d3_unc_az", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("bulk_corr_az", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("jhd_corr_az", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("total_unc_az", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("daz_dlon", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("daz_dlat", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("daz_dz", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("weight", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("err_code", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Predict_az(long predazid, long azcorrsurfid, long orid, long arid, String modname,
      String regname, double model_az, double model_unc_az, double base_az, double base_unc_az,
      double path_corr_az, double path_unc_az, double d3_corr_az, double d3_unc_az,
      double bulk_corr_az, double jhd_corr_az, double total_unc_az, double daz_dlon,
      double daz_dlat, double daz_dz, double weight, String err_code) {
    setValues(predazid, azcorrsurfid, orid, arid, modname, regname, model_az, model_unc_az, base_az,
        base_unc_az, path_corr_az, path_unc_az, d3_corr_az, d3_unc_az, bulk_corr_az, jhd_corr_az,
        total_unc_az, daz_dlon, daz_dlat, daz_dz, weight, err_code);
  }

  private void setValues(long predazid, long azcorrsurfid, long orid, long arid, String modname,
      String regname, double model_az, double model_unc_az, double base_az, double base_unc_az,
      double path_corr_az, double path_unc_az, double d3_corr_az, double d3_unc_az,
      double bulk_corr_az, double jhd_corr_az, double total_unc_az, double daz_dlon,
      double daz_dlat, double daz_dz, double weight, String err_code) {
    this.predazid = predazid;
    this.azcorrsurfid = azcorrsurfid;
    this.orid = orid;
    this.arid = arid;
    this.modname = modname;
    this.regname = regname;
    this.model_az = model_az;
    this.model_unc_az = model_unc_az;
    this.base_az = base_az;
    this.base_unc_az = base_unc_az;
    this.path_corr_az = path_corr_az;
    this.path_unc_az = path_unc_az;
    this.d3_corr_az = d3_corr_az;
    this.d3_unc_az = d3_unc_az;
    this.bulk_corr_az = bulk_corr_az;
    this.jhd_corr_az = jhd_corr_az;
    this.total_unc_az = total_unc_az;
    this.daz_dlon = daz_dlon;
    this.daz_dlat = daz_dlat;
    this.daz_dz = daz_dz;
    this.weight = weight;
    this.err_code = err_code;
  }

  /**
   * Copy constructor.
   */
  public Predict_az(Predict_az other) {
    this.predazid = other.getPredazid();
    this.azcorrsurfid = other.getAzcorrsurfid();
    this.orid = other.getOrid();
    this.arid = other.getArid();
    this.modname = other.getModname();
    this.regname = other.getRegname();
    this.model_az = other.getModel_az();
    this.model_unc_az = other.getModel_unc_az();
    this.base_az = other.getBase_az();
    this.base_unc_az = other.getBase_unc_az();
    this.path_corr_az = other.getPath_corr_az();
    this.path_unc_az = other.getPath_unc_az();
    this.d3_corr_az = other.getD3_corr_az();
    this.d3_unc_az = other.getD3_unc_az();
    this.bulk_corr_az = other.getBulk_corr_az();
    this.jhd_corr_az = other.getJhd_corr_az();
    this.total_unc_az = other.getTotal_unc_az();
    this.daz_dlon = other.getDaz_dlon();
    this.daz_dlat = other.getDaz_dlat();
    this.daz_dz = other.getDaz_dz();
    this.weight = other.getWeight();
    this.err_code = other.getErr_code();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Predict_az() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(PREDAZID_NA, AZCORRSURFID_NA, ORID_NA, ARID_NA, MODNAME_NA, REGNAME_NA, MODEL_AZ_NA,
        MODEL_UNC_AZ_NA, BASE_AZ_NA, BASE_UNC_AZ_NA, PATH_CORR_AZ_NA, PATH_UNC_AZ_NA, D3_CORR_AZ_NA,
        D3_UNC_AZ_NA, BULK_CORR_AZ_NA, JHD_CORR_AZ_NA, TOTAL_UNC_AZ_NA, DAZ_DLON_NA, DAZ_DLAT_NA,
        DAZ_DZ_NA, WEIGHT_NA, ERR_CODE_NA);
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
      case "model_az":
        return model_az;
      case "model_unc_az":
        return model_unc_az;
      case "base_az":
        return base_az;
      case "base_unc_az":
        return base_unc_az;
      case "path_corr_az":
        return path_corr_az;
      case "path_unc_az":
        return path_unc_az;
      case "d3_corr_az":
        return d3_corr_az;
      case "d3_unc_az":
        return d3_unc_az;
      case "bulk_corr_az":
        return bulk_corr_az;
      case "jhd_corr_az":
        return jhd_corr_az;
      case "total_unc_az":
        return total_unc_az;
      case "daz_dlon":
        return daz_dlon;
      case "daz_dlat":
        return daz_dlat;
      case "daz_dz":
        return daz_dz;
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
      case "model_az":
        model_az = value;
        break;
      case "model_unc_az":
        model_unc_az = value;
        break;
      case "base_az":
        base_az = value;
        break;
      case "base_unc_az":
        base_unc_az = value;
        break;
      case "path_corr_az":
        path_corr_az = value;
        break;
      case "path_unc_az":
        path_unc_az = value;
        break;
      case "d3_corr_az":
        d3_corr_az = value;
        break;
      case "d3_unc_az":
        d3_unc_az = value;
        break;
      case "bulk_corr_az":
        bulk_corr_az = value;
        break;
      case "jhd_corr_az":
        jhd_corr_az = value;
        break;
      case "total_unc_az":
        total_unc_az = value;
        break;
      case "daz_dlon":
        daz_dlon = value;
        break;
      case "daz_dlat":
        daz_dlat = value;
        break;
      case "daz_dz":
        daz_dz = value;
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
      case "predazid":
        return predazid;
      case "azcorrsurfid":
        return azcorrsurfid;
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
      case "predazid":
        predazid = value;
        break;
      case "azcorrsurfid":
        azcorrsurfid = value;
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
  public Predict_az(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Predict_az(DataInputStream input) throws IOException {
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
  public Predict_az(ByteBuffer input) {
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
  public Predict_az(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Predict_az(ResultSet input, int offset) throws SQLException {
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
    values[0] = predazid;
    values[1] = azcorrsurfid;
    values[2] = orid;
    values[3] = arid;
    values[4] = modname;
    values[5] = regname;
    values[6] = model_az;
    values[7] = model_unc_az;
    values[8] = base_az;
    values[9] = base_unc_az;
    values[10] = path_corr_az;
    values[11] = path_unc_az;
    values[12] = d3_corr_az;
    values[13] = d3_unc_az;
    values[14] = bulk_corr_az;
    values[15] = jhd_corr_az;
    values[16] = total_unc_az;
    values[17] = daz_dlon;
    values[18] = daz_dlat;
    values[19] = daz_dz;
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
    values[0] = predazid;
    values[1] = azcorrsurfid;
    values[2] = orid;
    values[3] = arid;
    values[4] = modname;
    values[5] = regname;
    values[6] = model_az;
    values[7] = model_unc_az;
    values[8] = base_az;
    values[9] = base_unc_az;
    values[10] = path_corr_az;
    values[11] = path_unc_az;
    values[12] = d3_corr_az;
    values[13] = d3_unc_az;
    values[14] = bulk_corr_az;
    values[15] = jhd_corr_az;
    values[16] = total_unc_az;
    values[17] = daz_dlon;
    values[18] = daz_dlat;
    values[19] = daz_dz;
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
    output.writeLong(predazid);
    output.writeLong(azcorrsurfid);
    output.writeLong(orid);
    output.writeLong(arid);
    writeString(output, modname);
    writeString(output, regname);
    output.writeDouble(model_az);
    output.writeDouble(model_unc_az);
    output.writeDouble(base_az);
    output.writeDouble(base_unc_az);
    output.writeDouble(path_corr_az);
    output.writeDouble(path_unc_az);
    output.writeDouble(d3_corr_az);
    output.writeDouble(d3_unc_az);
    output.writeDouble(bulk_corr_az);
    output.writeDouble(jhd_corr_az);
    output.writeDouble(total_unc_az);
    output.writeDouble(daz_dlon);
    output.writeDouble(daz_dlat);
    output.writeDouble(daz_dz);
    output.writeDouble(weight);
    writeString(output, err_code);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(predazid);
    output.putLong(azcorrsurfid);
    output.putLong(orid);
    output.putLong(arid);
    writeString(output, modname);
    writeString(output, regname);
    output.putDouble(model_az);
    output.putDouble(model_unc_az);
    output.putDouble(base_az);
    output.putDouble(base_unc_az);
    output.putDouble(path_corr_az);
    output.putDouble(path_unc_az);
    output.putDouble(d3_corr_az);
    output.putDouble(d3_unc_az);
    output.putDouble(bulk_corr_az);
    output.putDouble(jhd_corr_az);
    output.putDouble(total_unc_az);
    output.putDouble(daz_dlon);
    output.putDouble(daz_dlat);
    output.putDouble(daz_dz);
    output.putDouble(weight);
    writeString(output, err_code);
  }

  /**
   * Read a Collection of Predict_az objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Predict_az objects.
   * @throws IOException
   */
  static public void readPredict_azs(BufferedReader input, Collection<Predict_az> rows)
      throws IOException {
    String[] saved = Predict_az.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Predict_az
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Predict_az(new Scanner(line)));
    }
    input.close();
    Predict_az.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Predict_az objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Predict_az objects.
   * @throws IOException
   */
  static public void readPredict_azs(File inputFile, Collection<Predict_az> rows)
      throws IOException {
    readPredict_azs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Predict_az objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Predict_az objects.
   * @throws IOException
   */
  static public void readPredict_azs(InputStream inputStream, Collection<Predict_az> rows)
      throws IOException {
    readPredict_azs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Predict_az objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Predict_az objects
   * @throws IOException
   */
  static public Set<Predict_az> readPredict_azs(BufferedReader input) throws IOException {
    Set<Predict_az> rows = new LinkedHashSet<Predict_az>();
    readPredict_azs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Predict_az objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Predict_az objects
   * @throws IOException
   */
  static public Set<Predict_az> readPredict_azs(File inputFile) throws IOException {
    return readPredict_azs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Predict_az objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Predict_az objects
   * @throws IOException
   */
  static public Set<Predict_az> readPredict_azs(InputStream input) throws IOException {
    return readPredict_azs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Predict_az objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param predict_azs the Predict_az objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Predict_az> predict_azs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Predict_az predict_az : predict_azs)
      predict_az.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Predict_az objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param predict_azs the Predict_az objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Predict_az> predict_azs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Predict_az predict_az : predict_azs) {
        int i = 0;
        statement.setLong(++i, predict_az.predazid);
        statement.setLong(++i, predict_az.azcorrsurfid);
        statement.setLong(++i, predict_az.orid);
        statement.setLong(++i, predict_az.arid);
        statement.setString(++i, predict_az.modname);
        statement.setString(++i, predict_az.regname);
        statement.setDouble(++i, predict_az.model_az);
        statement.setDouble(++i, predict_az.model_unc_az);
        statement.setDouble(++i, predict_az.base_az);
        statement.setDouble(++i, predict_az.base_unc_az);
        statement.setDouble(++i, predict_az.path_corr_az);
        statement.setDouble(++i, predict_az.path_unc_az);
        statement.setDouble(++i, predict_az.d3_corr_az);
        statement.setDouble(++i, predict_az.d3_unc_az);
        statement.setDouble(++i, predict_az.bulk_corr_az);
        statement.setDouble(++i, predict_az.jhd_corr_az);
        statement.setDouble(++i, predict_az.total_unc_az);
        statement.setDouble(++i, predict_az.daz_dlon);
        statement.setDouble(++i, predict_az.daz_dlat);
        statement.setDouble(++i, predict_az.daz_dz);
        statement.setDouble(++i, predict_az.weight);
        statement.setString(++i, predict_az.err_code);
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
   *        Predict_az table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Predict_az> readPredict_azs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Predict_az> results = new HashSet<Predict_az>();
    readPredict_azs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Predict_az table.
   * @param predict_azs
   * @throws SQLException
   */
  static public void readPredict_azs(Connection connection, String selectStatement,
      Set<Predict_az> predict_azs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        predict_azs.add(new Predict_az(rs));
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
   * this Predict_az object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Predict_az object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "predazid, azcorrsurfid, orid, arid, modname, regname, model_az, model_unc_az, base_az, base_unc_az, path_corr_az, path_unc_az, d3_corr_az, d3_unc_az, bulk_corr_az, jhd_corr_az, total_unc_az, daz_dlon, daz_dlat, daz_dz, weight, err_code, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(predazid)).append(", ");
    sql.append(Long.toString(azcorrsurfid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(arid)).append(", ");
    sql.append("'").append(modname).append("', ");
    sql.append("'").append(regname).append("', ");
    sql.append(Double.toString(model_az)).append(", ");
    sql.append(Double.toString(model_unc_az)).append(", ");
    sql.append(Double.toString(base_az)).append(", ");
    sql.append(Double.toString(base_unc_az)).append(", ");
    sql.append(Double.toString(path_corr_az)).append(", ");
    sql.append(Double.toString(path_unc_az)).append(", ");
    sql.append(Double.toString(d3_corr_az)).append(", ");
    sql.append(Double.toString(d3_unc_az)).append(", ");
    sql.append(Double.toString(bulk_corr_az)).append(", ");
    sql.append(Double.toString(jhd_corr_az)).append(", ");
    sql.append(Double.toString(total_unc_az)).append(", ");
    sql.append(Double.toString(daz_dlon)).append(", ");
    sql.append(Double.toString(daz_dlat)).append(", ");
    sql.append(Double.toString(daz_dz)).append(", ");
    sql.append(Double.toString(weight)).append(", ");
    sql.append("'").append(err_code).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Predict_az in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Predict_az in the database
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
   * Generate a sql script to create a table of type Predict_az in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Predict_az in the database
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
    buf.append("predazid     number(9)            NOT NULL,\n");
    buf.append("azcorrsurfid number(9)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("arid         number(9)            NOT NULL,\n");
    buf.append("modname      varchar2(60)         NOT NULL,\n");
    buf.append("regname      varchar2(60)         NOT NULL,\n");
    buf.append("model_az     float(53)            NOT NULL,\n");
    buf.append("model_unc_az float(53)            NOT NULL,\n");
    buf.append("base_az      float(53)            NOT NULL,\n");
    buf.append("base_unc_az  float(53)            NOT NULL,\n");
    buf.append("path_corr_az float(53)            NOT NULL,\n");
    buf.append("path_unc_az  float(53)            NOT NULL,\n");
    buf.append("d3_corr_az   float(53)            NOT NULL,\n");
    buf.append("d3_unc_az    float(53)            NOT NULL,\n");
    buf.append("bulk_corr_az float(53)            NOT NULL,\n");
    buf.append("jhd_corr_az  float(53)            NOT NULL,\n");
    buf.append("total_unc_az float(53)            NOT NULL,\n");
    buf.append("daz_dlon     float(53)            NOT NULL,\n");
    buf.append("daz_dlat     float(53)            NOT NULL,\n");
    buf.append("daz_dz       float(53)            NOT NULL,\n");
    buf.append("weight       float(53)            NOT NULL,\n");
    buf.append("err_code     varchar2(30)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (predazid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (azcorrsurfid,orid,arid)");
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
    return (other instanceof Predict_az) && ((Predict_az) other).predazid == predazid;
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
    return (other instanceof Predict_az) && ((Predict_az) other).azcorrsurfid == azcorrsurfid
        && ((Predict_az) other).orid == orid && ((Predict_az) other).arid == arid;
  }

  /**
   * Azimuth prediction identifier
   * 
   * @return predazid
   */
  public long getPredazid() {
    return predazid;
  }

  /**
   * Azimuth prediction identifier
   * 
   * @param predazid
   * @throws IllegalArgumentException if predazid >= 1000000000
   */
  public Predict_az setPredazid(long predazid) {
    if (predazid >= 1000000000L)
      throw new IllegalArgumentException("predazid=" + predazid + " but cannot be >= 1000000000");
    this.predazid = predazid;
    setHash(null);
    return this;
  }

  /**
   * Azimuth correction surface identifier.
   * 
   * @return azcorrsurfid
   */
  public long getAzcorrsurfid() {
    return azcorrsurfid;
  }

  /**
   * Azimuth correction surface identifier.
   * 
   * @param azcorrsurfid
   * @throws IllegalArgumentException if azcorrsurfid >= 1000000000
   */
  public Predict_az setAzcorrsurfid(long azcorrsurfid) {
    if (azcorrsurfid >= 1000000000L)
      throw new IllegalArgumentException(
          "azcorrsurfid=" + azcorrsurfid + " but cannot be >= 1000000000");
    this.azcorrsurfid = azcorrsurfid;
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
  public Predict_az setOrid(long orid) {
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
  public Predict_az setArid(long arid) {
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
  public Predict_az setModname(String modname) {
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
  public Predict_az setRegname(String regname) {
    if (regname.length() > 60)
      throw new IllegalArgumentException(
          String.format("regname.length() cannot be > 60.  regname=%s", regname));
    this.regname = regname;
    setHash(null);
    return this;
  }

  /**
   * Total predicted azimuth
   * <p>
   * Units: degree
   * 
   * @return model_az
   */
  public double getModel_az() {
    return model_az;
  }

  /**
   * Total predicted azimuth
   * <p>
   * Units: degree
   * 
   * @param model_az
   */
  public Predict_az setModel_az(double model_az) {
    this.model_az = model_az;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty of the total predicted azimuth
   * <p>
   * Units: degree
   * 
   * @return model_unc_az
   */
  public double getModel_unc_az() {
    return model_unc_az;
  }

  /**
   * Uncertainty of the total predicted azimuth
   * <p>
   * Units: degree
   * 
   * @param model_unc_az
   */
  public Predict_az setModel_unc_az(double model_unc_az) {
    this.model_unc_az = model_unc_az;
    setHash(null);
    return this;
  }

  /**
   * Base component of the predicted azimuth measured clockwise from North
   * <p>
   * Units: degree
   * 
   * @return base_az
   */
  public double getBase_az() {
    return base_az;
  }

  /**
   * Base component of the predicted azimuth measured clockwise from North
   * <p>
   * Units: degree
   * 
   * @param base_az
   */
  public Predict_az setBase_az(double base_az) {
    this.base_az = base_az;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty of the base component of the azimuth
   * <p>
   * Units: degree
   * 
   * @return base_unc_az
   */
  public double getBase_unc_az() {
    return base_unc_az;
  }

  /**
   * Uncertainty of the base component of the azimuth
   * <p>
   * Units: degree
   * 
   * @param base_unc_az
   */
  public Predict_az setBase_unc_az(double base_unc_az) {
    this.base_unc_az = base_unc_az;
    setHash(null);
    return this;
  }

  /**
   * Path correction component of the predicted azimuth
   * <p>
   * Units: degree
   * 
   * @return path_corr_az
   */
  public double getPath_corr_az() {
    return path_corr_az;
  }

  /**
   * Path correction component of the predicted azimuth
   * <p>
   * Units: degree
   * 
   * @param path_corr_az
   */
  public Predict_az setPath_corr_az(double path_corr_az) {
    this.path_corr_az = path_corr_az;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty in path correction component of the predicted azimuth
   * <p>
   * Units: degree
   * 
   * @return path_unc_az
   */
  public double getPath_unc_az() {
    return path_unc_az;
  }

  /**
   * Uncertainty in path correction component of the predicted azimuth
   * <p>
   * Units: degree
   * 
   * @param path_unc_az
   */
  public Predict_az setPath_unc_az(double path_unc_az) {
    this.path_unc_az = path_unc_az;
    setHash(null);
    return this;
  }

  /**
   * Three-dimensional Earth model correction component of the predicted azimuth
   * <p>
   * Units: degree
   * 
   * @return d3_corr_az
   */
  public double getD3_corr_az() {
    return d3_corr_az;
  }

  /**
   * Three-dimensional Earth model correction component of the predicted azimuth
   * <p>
   * Units: degree
   * 
   * @param d3_corr_az
   */
  public Predict_az setD3_corr_az(double d3_corr_az) {
    this.d3_corr_az = d3_corr_az;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty of the three-dimensional Earth model correction component of the predicted azimuth
   * <p>
   * Units: degree
   * 
   * @return d3_unc_az
   */
  public double getD3_unc_az() {
    return d3_unc_az;
  }

  /**
   * Uncertainty of the three-dimensional Earth model correction component of the predicted azimuth
   * <p>
   * Units: degree
   * 
   * @param d3_unc_az
   */
  public Predict_az setD3_unc_az(double d3_unc_az) {
    this.d3_unc_az = d3_unc_az;
    setHash(null);
    return this;
  }

  /**
   * Static azimuth correction for a given station, to be added regardless of source position.
   * <p>
   * Units: degree
   * 
   * @return bulk_corr_az
   */
  public double getBulk_corr_az() {
    return bulk_corr_az;
  }

  /**
   * Static azimuth correction for a given station, to be added regardless of source position.
   * <p>
   * Units: degree
   * 
   * @param bulk_corr_az
   */
  public Predict_az setBulk_corr_az(double bulk_corr_az) {
    this.bulk_corr_az = bulk_corr_az;
    setHash(null);
    return this;
  }

  /**
   * JHD correction component of the predicted azimuth
   * <p>
   * Units: degree
   * 
   * @return jhd_corr_az
   */
  public double getJhd_corr_az() {
    return jhd_corr_az;
  }

  /**
   * JHD correction component of the predicted azimuth
   * <p>
   * Units: degree
   * 
   * @param jhd_corr_az
   */
  public Predict_az setJhd_corr_az(double jhd_corr_az) {
    this.jhd_corr_az = jhd_corr_az;
    setHash(null);
    return this;
  }

  /**
   * Total uncertainty of the azimuth (model plus pick)
   * <p>
   * Units: degree
   * 
   * @return total_unc_az
   */
  public double getTotal_unc_az() {
    return total_unc_az;
  }

  /**
   * Total uncertainty of the azimuth (model plus pick)
   * <p>
   * Units: degree
   * 
   * @param total_unc_az
   */
  public Predict_az setTotal_unc_az(double total_unc_az) {
    this.total_unc_az = total_unc_az;
    setHash(null);
    return this;
  }

  /**
   * Derivative of the predicted azimuth with respect to longitude
   * <p>
   * Units: degree/degree
   * 
   * @return daz_dlon
   */
  public double getDaz_dlon() {
    return daz_dlon;
  }

  /**
   * Derivative of the predicted azimuth with respect to longitude
   * <p>
   * Units: degree/degree
   * 
   * @param daz_dlon
   */
  public Predict_az setDaz_dlon(double daz_dlon) {
    this.daz_dlon = daz_dlon;
    setHash(null);
    return this;
  }

  /**
   * Derivative of the predicted azimuth with respect to latitude
   * <p>
   * Units: degree/degree
   * 
   * @return daz_dlat
   */
  public double getDaz_dlat() {
    return daz_dlat;
  }

  /**
   * Derivative of the predicted azimuth with respect to latitude
   * <p>
   * Units: degree/degree
   * 
   * @param daz_dlat
   */
  public Predict_az setDaz_dlat(double daz_dlat) {
    this.daz_dlat = daz_dlat;
    setHash(null);
    return this;
  }

  /**
   * Derivative of the predicted azimuth with respect to depth
   * <p>
   * Units: degree/km
   * 
   * @return daz_dz
   */
  public double getDaz_dz() {
    return daz_dz;
  }

  /**
   * Derivative of the predicted azimuth with respect to depth
   * <p>
   * Units: degree/km
   * 
   * @param daz_dz
   */
  public Predict_az setDaz_dz(double daz_dz) {
    this.daz_dz = daz_dz;
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
  public Predict_az setWeight(double weight) {
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
  public Predict_az setErr_code(String err_code) {
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
