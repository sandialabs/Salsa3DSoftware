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
 * modcomptt
 */
public class Modcomptt extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Modcomp travel-time identifier
   */
  private long mcttid;

  static final public long MCTTID_NA = Long.MIN_VALUE;

  /**
   * Travel-time prediction identifier
   */
  private long predttid;

  static final public long PREDTTID_NA = Long.MIN_VALUE;

  /**
   * Base model name.
   */
  private String modname;

  static final public String MODNAME_NA = "-";

  /**
   * Region name assigned by the author of the kbcit project.
   */
  private String regname;

  static final public String REGNAME_NA = null;

  /**
   * Total predicted travel time
   * <p>
   * Units: s
   */
  private double model_tt;

  static final public double MODEL_TT_NA = Double.NaN;

  /**
   * Uncertainty of the total predicted travel time
   * <p>
   * Units: s
   */
  private double model_unc_tt;

  static final public double MODEL_UNC_TT_NA = Double.NaN;

  /**
   * Base component of the predicted travel time
   * <p>
   * Units: s
   */
  private double base_tt;

  static final public double BASE_TT_NA = Double.NaN;

  /**
   * Uncertainty of the base component of the travel time
   * <p>
   * Units: s
   */
  private double base_unc_tt;

  static final public double BASE_UNC_TT_NA = -1;

  /**
   * Path correction component of the predicted travel time
   * <p>
   * Units: s
   */
  private double path_corr_tt;

  static final public double PATH_CORR_TT_NA = Double.NaN;

  /**
   * Uncertainty in path correction component of the predicted travel time
   * <p>
   * Units: s
   */
  private double path_unc_tt;

  static final public double PATH_UNC_TT_NA = Double.NaN;

  /**
   * Three-dimensional Earth model correction component of the predicted travel time
   * <p>
   * Units: s
   */
  private double d3_corr_tt;

  static final public double D3_CORR_TT_NA = Double.NaN;

  /**
   * Uncertainty of the three-dimensional Earth model correction component of the predicted travel
   * time
   * <p>
   * Units: s
   */
  private double d3_unc_tt;

  static final public double D3_UNC_TT_NA = -1;

  /**
   * Ellipticity correcrtion component of the predicted travel time
   * <p>
   * Units: s
   */
  private double ellip_corr_tt;

  static final public double ELLIP_CORR_TT_NA = -999;

  /**
   * Elevation correcrtion component of the predicted travel time
   * <p>
   * Units: s
   */
  private double elev_corr_tt;

  static final public double ELEV_CORR_TT_NA = -999;

  /**
   * Static travel-time correction for a given station, to be added regardless of source position.
   * <p>
   * Units: s
   */
  private double bulk_corr_tt;

  static final public double BULK_CORR_TT_NA = -1;

  /**
   * Derivative of the predicted travel time with respect to longitude
   * <p>
   * Units: s/degree
   */
  private double dtt_dlon;

  static final public double DTT_DLON_NA = -999;

  /**
   * Derivative of the predicted travel time with respect to latitude
   * <p>
   * Units: s/degree
   */
  private double dtt_dlat;

  static final public double DTT_DLAT_NA = Double.NaN;

  /**
   * Derivative of the predicted travel time with respect to depth
   * <p>
   * Units: s/km
   */
  private double dtt_dz;

  static final public double DTT_DZ_NA = -999;

  /**
   * The weight of the prediction in the location.
   */
  private double blendweight;

  static final public double BLENDWEIGHT_NA = Double.NaN;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("mcttid", Columns.FieldType.LONG, "%d");
    columns.add("predttid", Columns.FieldType.LONG, "%d");
    columns.add("modname", Columns.FieldType.STRING, "%s");
    columns.add("regname", Columns.FieldType.STRING, "%s");
    columns.add("model_tt", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("model_unc_tt", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("base_tt", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("base_unc_tt", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("path_corr_tt", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("path_unc_tt", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("d3_corr_tt", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("d3_unc_tt", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("ellip_corr_tt", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("elev_corr_tt", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("bulk_corr_tt", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("dtt_dlon", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("dtt_dlat", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("dtt_dz", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("blendweight", Columns.FieldType.DOUBLE, "%22.15e");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Modcomptt(long mcttid, long predttid, String modname, String regname, double model_tt,
      double model_unc_tt, double base_tt, double base_unc_tt, double path_corr_tt,
      double path_unc_tt, double d3_corr_tt, double d3_unc_tt, double ellip_corr_tt,
      double elev_corr_tt, double bulk_corr_tt, double dtt_dlon, double dtt_dlat, double dtt_dz,
      double blendweight) {
    setValues(mcttid, predttid, modname, regname, model_tt, model_unc_tt, base_tt, base_unc_tt,
        path_corr_tt, path_unc_tt, d3_corr_tt, d3_unc_tt, ellip_corr_tt, elev_corr_tt, bulk_corr_tt,
        dtt_dlon, dtt_dlat, dtt_dz, blendweight);
  }

  private void setValues(long mcttid, long predttid, String modname, String regname,
      double model_tt, double model_unc_tt, double base_tt, double base_unc_tt, double path_corr_tt,
      double path_unc_tt, double d3_corr_tt, double d3_unc_tt, double ellip_corr_tt,
      double elev_corr_tt, double bulk_corr_tt, double dtt_dlon, double dtt_dlat, double dtt_dz,
      double blendweight) {
    this.mcttid = mcttid;
    this.predttid = predttid;
    this.modname = modname;
    this.regname = regname;
    this.model_tt = model_tt;
    this.model_unc_tt = model_unc_tt;
    this.base_tt = base_tt;
    this.base_unc_tt = base_unc_tt;
    this.path_corr_tt = path_corr_tt;
    this.path_unc_tt = path_unc_tt;
    this.d3_corr_tt = d3_corr_tt;
    this.d3_unc_tt = d3_unc_tt;
    this.ellip_corr_tt = ellip_corr_tt;
    this.elev_corr_tt = elev_corr_tt;
    this.bulk_corr_tt = bulk_corr_tt;
    this.dtt_dlon = dtt_dlon;
    this.dtt_dlat = dtt_dlat;
    this.dtt_dz = dtt_dz;
    this.blendweight = blendweight;
  }

  /**
   * Copy constructor.
   */
  public Modcomptt(Modcomptt other) {
    this.mcttid = other.getMcttid();
    this.predttid = other.getPredttid();
    this.modname = other.getModname();
    this.regname = other.getRegname();
    this.model_tt = other.getModel_tt();
    this.model_unc_tt = other.getModel_unc_tt();
    this.base_tt = other.getBase_tt();
    this.base_unc_tt = other.getBase_unc_tt();
    this.path_corr_tt = other.getPath_corr_tt();
    this.path_unc_tt = other.getPath_unc_tt();
    this.d3_corr_tt = other.getD3_corr_tt();
    this.d3_unc_tt = other.getD3_unc_tt();
    this.ellip_corr_tt = other.getEllip_corr_tt();
    this.elev_corr_tt = other.getElev_corr_tt();
    this.bulk_corr_tt = other.getBulk_corr_tt();
    this.dtt_dlon = other.getDtt_dlon();
    this.dtt_dlat = other.getDtt_dlat();
    this.dtt_dz = other.getDtt_dz();
    this.blendweight = other.getBlendweight();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Modcomptt() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MCTTID_NA, PREDTTID_NA, MODNAME_NA, REGNAME_NA, MODEL_TT_NA, MODEL_UNC_TT_NA,
        BASE_TT_NA, BASE_UNC_TT_NA, PATH_CORR_TT_NA, PATH_UNC_TT_NA, D3_CORR_TT_NA, D3_UNC_TT_NA,
        ELLIP_CORR_TT_NA, ELEV_CORR_TT_NA, BULK_CORR_TT_NA, DTT_DLON_NA, DTT_DLAT_NA, DTT_DZ_NA,
        BLENDWEIGHT_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "modname":
        return modname;
      case "regname":
        return regname;
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
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "model_tt":
        return model_tt;
      case "model_unc_tt":
        return model_unc_tt;
      case "base_tt":
        return base_tt;
      case "base_unc_tt":
        return base_unc_tt;
      case "path_corr_tt":
        return path_corr_tt;
      case "path_unc_tt":
        return path_unc_tt;
      case "d3_corr_tt":
        return d3_corr_tt;
      case "d3_unc_tt":
        return d3_unc_tt;
      case "ellip_corr_tt":
        return ellip_corr_tt;
      case "elev_corr_tt":
        return elev_corr_tt;
      case "bulk_corr_tt":
        return bulk_corr_tt;
      case "dtt_dlon":
        return dtt_dlon;
      case "dtt_dlat":
        return dtt_dlat;
      case "dtt_dz":
        return dtt_dz;
      case "blendweight":
        return blendweight;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "model_tt":
        model_tt = value;
        break;
      case "model_unc_tt":
        model_unc_tt = value;
        break;
      case "base_tt":
        base_tt = value;
        break;
      case "base_unc_tt":
        base_unc_tt = value;
        break;
      case "path_corr_tt":
        path_corr_tt = value;
        break;
      case "path_unc_tt":
        path_unc_tt = value;
        break;
      case "d3_corr_tt":
        d3_corr_tt = value;
        break;
      case "d3_unc_tt":
        d3_unc_tt = value;
        break;
      case "ellip_corr_tt":
        ellip_corr_tt = value;
        break;
      case "elev_corr_tt":
        elev_corr_tt = value;
        break;
      case "bulk_corr_tt":
        bulk_corr_tt = value;
        break;
      case "dtt_dlon":
        dtt_dlon = value;
        break;
      case "dtt_dlat":
        dtt_dlat = value;
        break;
      case "dtt_dz":
        dtt_dz = value;
        break;
      case "blendweight":
        blendweight = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "mcttid":
        return mcttid;
      case "predttid":
        return predttid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "mcttid":
        mcttid = value;
        break;
      case "predttid":
        predttid = value;
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
  public Modcomptt(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Modcomptt(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), readString(input), readString(input),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Modcomptt(ByteBuffer input) {
    this(input.getLong(), input.getLong(), readString(input), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Modcomptt(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Modcomptt(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getDouble(offset + 17), input.getDouble(offset + 18),
        input.getDouble(offset + 19));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[19];
    values[0] = mcttid;
    values[1] = predttid;
    values[2] = modname;
    values[3] = regname;
    values[4] = model_tt;
    values[5] = model_unc_tt;
    values[6] = base_tt;
    values[7] = base_unc_tt;
    values[8] = path_corr_tt;
    values[9] = path_unc_tt;
    values[10] = d3_corr_tt;
    values[11] = d3_unc_tt;
    values[12] = ellip_corr_tt;
    values[13] = elev_corr_tt;
    values[14] = bulk_corr_tt;
    values[15] = dtt_dlon;
    values[16] = dtt_dlat;
    values[17] = dtt_dz;
    values[18] = blendweight;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[20];
    values[0] = mcttid;
    values[1] = predttid;
    values[2] = modname;
    values[3] = regname;
    values[4] = model_tt;
    values[5] = model_unc_tt;
    values[6] = base_tt;
    values[7] = base_unc_tt;
    values[8] = path_corr_tt;
    values[9] = path_unc_tt;
    values[10] = d3_corr_tt;
    values[11] = d3_unc_tt;
    values[12] = ellip_corr_tt;
    values[13] = elev_corr_tt;
    values[14] = bulk_corr_tt;
    values[15] = dtt_dlon;
    values[16] = dtt_dlat;
    values[17] = dtt_dz;
    values[18] = blendweight;
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
    output.writeLong(mcttid);
    output.writeLong(predttid);
    writeString(output, modname);
    writeString(output, regname);
    output.writeDouble(model_tt);
    output.writeDouble(model_unc_tt);
    output.writeDouble(base_tt);
    output.writeDouble(base_unc_tt);
    output.writeDouble(path_corr_tt);
    output.writeDouble(path_unc_tt);
    output.writeDouble(d3_corr_tt);
    output.writeDouble(d3_unc_tt);
    output.writeDouble(ellip_corr_tt);
    output.writeDouble(elev_corr_tt);
    output.writeDouble(bulk_corr_tt);
    output.writeDouble(dtt_dlon);
    output.writeDouble(dtt_dlat);
    output.writeDouble(dtt_dz);
    output.writeDouble(blendweight);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(mcttid);
    output.putLong(predttid);
    writeString(output, modname);
    writeString(output, regname);
    output.putDouble(model_tt);
    output.putDouble(model_unc_tt);
    output.putDouble(base_tt);
    output.putDouble(base_unc_tt);
    output.putDouble(path_corr_tt);
    output.putDouble(path_unc_tt);
    output.putDouble(d3_corr_tt);
    output.putDouble(d3_unc_tt);
    output.putDouble(ellip_corr_tt);
    output.putDouble(elev_corr_tt);
    output.putDouble(bulk_corr_tt);
    output.putDouble(dtt_dlon);
    output.putDouble(dtt_dlat);
    output.putDouble(dtt_dz);
    output.putDouble(blendweight);
  }

  /**
   * Read a Collection of Modcomptt objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Modcomptt objects.
   * @throws IOException
   */
  static public void readModcomptts(BufferedReader input, Collection<Modcomptt> rows)
      throws IOException {
    String[] saved = Modcomptt.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Modcomptt
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Modcomptt(new Scanner(line)));
    }
    input.close();
    Modcomptt.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Modcomptt objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Modcomptt objects.
   * @throws IOException
   */
  static public void readModcomptts(File inputFile, Collection<Modcomptt> rows) throws IOException {
    readModcomptts(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Modcomptt objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Modcomptt objects.
   * @throws IOException
   */
  static public void readModcomptts(InputStream inputStream, Collection<Modcomptt> rows)
      throws IOException {
    readModcomptts(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Modcomptt objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Modcomptt objects
   * @throws IOException
   */
  static public Set<Modcomptt> readModcomptts(BufferedReader input) throws IOException {
    Set<Modcomptt> rows = new LinkedHashSet<Modcomptt>();
    readModcomptts(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Modcomptt objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Modcomptt objects
   * @throws IOException
   */
  static public Set<Modcomptt> readModcomptts(File inputFile) throws IOException {
    return readModcomptts(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Modcomptt objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Modcomptt objects
   * @throws IOException
   */
  static public Set<Modcomptt> readModcomptts(InputStream input) throws IOException {
    return readModcomptts(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Modcomptt objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param modcomptts the Modcomptt objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Modcomptt> modcomptts)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Modcomptt modcomptt : modcomptts)
      modcomptt.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Modcomptt objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param modcomptts the Modcomptt objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Modcomptt> modcomptts, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Modcomptt modcomptt : modcomptts) {
        int i = 0;
        statement.setLong(++i, modcomptt.mcttid);
        statement.setLong(++i, modcomptt.predttid);
        statement.setString(++i, modcomptt.modname);
        statement.setString(++i, modcomptt.regname);
        statement.setDouble(++i, modcomptt.model_tt);
        statement.setDouble(++i, modcomptt.model_unc_tt);
        statement.setDouble(++i, modcomptt.base_tt);
        statement.setDouble(++i, modcomptt.base_unc_tt);
        statement.setDouble(++i, modcomptt.path_corr_tt);
        statement.setDouble(++i, modcomptt.path_unc_tt);
        statement.setDouble(++i, modcomptt.d3_corr_tt);
        statement.setDouble(++i, modcomptt.d3_unc_tt);
        statement.setDouble(++i, modcomptt.ellip_corr_tt);
        statement.setDouble(++i, modcomptt.elev_corr_tt);
        statement.setDouble(++i, modcomptt.bulk_corr_tt);
        statement.setDouble(++i, modcomptt.dtt_dlon);
        statement.setDouble(++i, modcomptt.dtt_dlat);
        statement.setDouble(++i, modcomptt.dtt_dz);
        statement.setDouble(++i, modcomptt.blendweight);
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
   *        Modcomptt table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Modcomptt> readModcomptts(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Modcomptt> results = new HashSet<Modcomptt>();
    readModcomptts(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Modcomptt table.
   * @param modcomptts
   * @throws SQLException
   */
  static public void readModcomptts(Connection connection, String selectStatement,
      Set<Modcomptt> modcomptts) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        modcomptts.add(new Modcomptt(rs));
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
   * this Modcomptt object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Modcomptt object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "mcttid, predttid, modname, regname, model_tt, model_unc_tt, base_tt, base_unc_tt, path_corr_tt, path_unc_tt, d3_corr_tt, d3_unc_tt, ellip_corr_tt, elev_corr_tt, bulk_corr_tt, dtt_dlon, dtt_dlat, dtt_dz, blendweight, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(mcttid)).append(", ");
    sql.append(Long.toString(predttid)).append(", ");
    sql.append("'").append(modname).append("', ");
    sql.append("'").append(regname).append("', ");
    sql.append(Double.toString(model_tt)).append(", ");
    sql.append(Double.toString(model_unc_tt)).append(", ");
    sql.append(Double.toString(base_tt)).append(", ");
    sql.append(Double.toString(base_unc_tt)).append(", ");
    sql.append(Double.toString(path_corr_tt)).append(", ");
    sql.append(Double.toString(path_unc_tt)).append(", ");
    sql.append(Double.toString(d3_corr_tt)).append(", ");
    sql.append(Double.toString(d3_unc_tt)).append(", ");
    sql.append(Double.toString(ellip_corr_tt)).append(", ");
    sql.append(Double.toString(elev_corr_tt)).append(", ");
    sql.append(Double.toString(bulk_corr_tt)).append(", ");
    sql.append(Double.toString(dtt_dlon)).append(", ");
    sql.append(Double.toString(dtt_dlat)).append(", ");
    sql.append(Double.toString(dtt_dz)).append(", ");
    sql.append(Double.toString(blendweight)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Modcomptt in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Modcomptt in the database
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
   * Generate a sql script to create a table of type Modcomptt in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Modcomptt in the database
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
    buf.append("mcttid       number(9)            NOT NULL,\n");
    buf.append("predttid     number(9)            NOT NULL,\n");
    buf.append("modname      varchar2(60)         NOT NULL,\n");
    buf.append("regname      varchar2(60)         NOT NULL,\n");
    buf.append("model_tt     float(53)            NOT NULL,\n");
    buf.append("model_unc_tt float(53)            NOT NULL,\n");
    buf.append("base_tt      float(53)            NOT NULL,\n");
    buf.append("base_unc_tt  float(53)            NOT NULL,\n");
    buf.append("path_corr_tt float(53)            NOT NULL,\n");
    buf.append("path_unc_tt  float(53)            NOT NULL,\n");
    buf.append("d3_corr_tt   float(53)            NOT NULL,\n");
    buf.append("d3_unc_tt    float(53)            NOT NULL,\n");
    buf.append("ellip_corr_tt float(53)            NOT NULL,\n");
    buf.append("elev_corr_tt float(53)            NOT NULL,\n");
    buf.append("bulk_corr_tt float(53)            NOT NULL,\n");
    buf.append("dtt_dlon     float(53)            NOT NULL,\n");
    buf.append("dtt_dlat     float(53)            NOT NULL,\n");
    buf.append("dtt_dz       float(53)            NOT NULL,\n");
    buf.append("blendweight  float(53)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (mcttid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (predttid,regname)");
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
    return 264;
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
    return (other instanceof Modcomptt) && ((Modcomptt) other).mcttid == mcttid;
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
    return (other instanceof Modcomptt) && ((Modcomptt) other).predttid == predttid
        && ((Modcomptt) other).regname.equals(regname);
  }

  /**
   * Modcomp travel-time identifier
   * 
   * @return mcttid
   */
  public long getMcttid() {
    return mcttid;
  }

  /**
   * Modcomp travel-time identifier
   * 
   * @param mcttid
   * @throws IllegalArgumentException if mcttid >= 1000000000
   */
  public Modcomptt setMcttid(long mcttid) {
    if (mcttid >= 1000000000L)
      throw new IllegalArgumentException("mcttid=" + mcttid + " but cannot be >= 1000000000");
    this.mcttid = mcttid;
    setHash(null);
    return this;
  }

  /**
   * Travel-time prediction identifier
   * 
   * @return predttid
   */
  public long getPredttid() {
    return predttid;
  }

  /**
   * Travel-time prediction identifier
   * 
   * @param predttid
   * @throws IllegalArgumentException if predttid >= 1000000000
   */
  public Modcomptt setPredttid(long predttid) {
    if (predttid >= 1000000000L)
      throw new IllegalArgumentException("predttid=" + predttid + " but cannot be >= 1000000000");
    this.predttid = predttid;
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
  public Modcomptt setModname(String modname) {
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
  public Modcomptt setRegname(String regname) {
    if (regname.length() > 60)
      throw new IllegalArgumentException(
          String.format("regname.length() cannot be > 60.  regname=%s", regname));
    this.regname = regname;
    setHash(null);
    return this;
  }

  /**
   * Total predicted travel time
   * <p>
   * Units: s
   * 
   * @return model_tt
   */
  public double getModel_tt() {
    return model_tt;
  }

  /**
   * Total predicted travel time
   * <p>
   * Units: s
   * 
   * @param model_tt
   */
  public Modcomptt setModel_tt(double model_tt) {
    this.model_tt = model_tt;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty of the total predicted travel time
   * <p>
   * Units: s
   * 
   * @return model_unc_tt
   */
  public double getModel_unc_tt() {
    return model_unc_tt;
  }

  /**
   * Uncertainty of the total predicted travel time
   * <p>
   * Units: s
   * 
   * @param model_unc_tt
   */
  public Modcomptt setModel_unc_tt(double model_unc_tt) {
    this.model_unc_tt = model_unc_tt;
    setHash(null);
    return this;
  }

  /**
   * Base component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @return base_tt
   */
  public double getBase_tt() {
    return base_tt;
  }

  /**
   * Base component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @param base_tt
   */
  public Modcomptt setBase_tt(double base_tt) {
    this.base_tt = base_tt;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty of the base component of the travel time
   * <p>
   * Units: s
   * 
   * @return base_unc_tt
   */
  public double getBase_unc_tt() {
    return base_unc_tt;
  }

  /**
   * Uncertainty of the base component of the travel time
   * <p>
   * Units: s
   * 
   * @param base_unc_tt
   */
  public Modcomptt setBase_unc_tt(double base_unc_tt) {
    this.base_unc_tt = base_unc_tt;
    setHash(null);
    return this;
  }

  /**
   * Path correction component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @return path_corr_tt
   */
  public double getPath_corr_tt() {
    return path_corr_tt;
  }

  /**
   * Path correction component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @param path_corr_tt
   */
  public Modcomptt setPath_corr_tt(double path_corr_tt) {
    this.path_corr_tt = path_corr_tt;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty in path correction component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @return path_unc_tt
   */
  public double getPath_unc_tt() {
    return path_unc_tt;
  }

  /**
   * Uncertainty in path correction component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @param path_unc_tt
   */
  public Modcomptt setPath_unc_tt(double path_unc_tt) {
    this.path_unc_tt = path_unc_tt;
    setHash(null);
    return this;
  }

  /**
   * Three-dimensional Earth model correction component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @return d3_corr_tt
   */
  public double getD3_corr_tt() {
    return d3_corr_tt;
  }

  /**
   * Three-dimensional Earth model correction component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @param d3_corr_tt
   */
  public Modcomptt setD3_corr_tt(double d3_corr_tt) {
    this.d3_corr_tt = d3_corr_tt;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty of the three-dimensional Earth model correction component of the predicted travel
   * time
   * <p>
   * Units: s
   * 
   * @return d3_unc_tt
   */
  public double getD3_unc_tt() {
    return d3_unc_tt;
  }

  /**
   * Uncertainty of the three-dimensional Earth model correction component of the predicted travel
   * time
   * <p>
   * Units: s
   * 
   * @param d3_unc_tt
   */
  public Modcomptt setD3_unc_tt(double d3_unc_tt) {
    this.d3_unc_tt = d3_unc_tt;
    setHash(null);
    return this;
  }

  /**
   * Ellipticity correcrtion component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @return ellip_corr_tt
   */
  public double getEllip_corr_tt() {
    return ellip_corr_tt;
  }

  /**
   * Ellipticity correcrtion component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @param ellip_corr_tt
   */
  public Modcomptt setEllip_corr_tt(double ellip_corr_tt) {
    this.ellip_corr_tt = ellip_corr_tt;
    setHash(null);
    return this;
  }

  /**
   * Elevation correcrtion component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @return elev_corr_tt
   */
  public double getElev_corr_tt() {
    return elev_corr_tt;
  }

  /**
   * Elevation correcrtion component of the predicted travel time
   * <p>
   * Units: s
   * 
   * @param elev_corr_tt
   */
  public Modcomptt setElev_corr_tt(double elev_corr_tt) {
    this.elev_corr_tt = elev_corr_tt;
    setHash(null);
    return this;
  }

  /**
   * Static travel-time correction for a given station, to be added regardless of source position.
   * <p>
   * Units: s
   * 
   * @return bulk_corr_tt
   */
  public double getBulk_corr_tt() {
    return bulk_corr_tt;
  }

  /**
   * Static travel-time correction for a given station, to be added regardless of source position.
   * <p>
   * Units: s
   * 
   * @param bulk_corr_tt
   */
  public Modcomptt setBulk_corr_tt(double bulk_corr_tt) {
    this.bulk_corr_tt = bulk_corr_tt;
    setHash(null);
    return this;
  }

  /**
   * Derivative of the predicted travel time with respect to longitude
   * <p>
   * Units: s/degree
   * 
   * @return dtt_dlon
   */
  public double getDtt_dlon() {
    return dtt_dlon;
  }

  /**
   * Derivative of the predicted travel time with respect to longitude
   * <p>
   * Units: s/degree
   * 
   * @param dtt_dlon
   */
  public Modcomptt setDtt_dlon(double dtt_dlon) {
    this.dtt_dlon = dtt_dlon;
    setHash(null);
    return this;
  }

  /**
   * Derivative of the predicted travel time with respect to latitude
   * <p>
   * Units: s/degree
   * 
   * @return dtt_dlat
   */
  public double getDtt_dlat() {
    return dtt_dlat;
  }

  /**
   * Derivative of the predicted travel time with respect to latitude
   * <p>
   * Units: s/degree
   * 
   * @param dtt_dlat
   */
  public Modcomptt setDtt_dlat(double dtt_dlat) {
    this.dtt_dlat = dtt_dlat;
    setHash(null);
    return this;
  }

  /**
   * Derivative of the predicted travel time with respect to depth
   * <p>
   * Units: s/km
   * 
   * @return dtt_dz
   */
  public double getDtt_dz() {
    return dtt_dz;
  }

  /**
   * Derivative of the predicted travel time with respect to depth
   * <p>
   * Units: s/km
   * 
   * @param dtt_dz
   */
  public Modcomptt setDtt_dz(double dtt_dz) {
    this.dtt_dz = dtt_dz;
    setHash(null);
    return this;
  }

  /**
   * The weight of the prediction in the location.
   * 
   * @return blendweight
   */
  public double getBlendweight() {
    return blendweight;
  }

  /**
   * The weight of the prediction in the location.
   * 
   * @param blendweight
   */
  public Modcomptt setBlendweight(double blendweight) {
    this.blendweight = blendweight;
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
