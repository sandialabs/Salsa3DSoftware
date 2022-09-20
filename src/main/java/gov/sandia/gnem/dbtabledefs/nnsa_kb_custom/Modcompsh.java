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
 * modcompsh
 */
public class Modcompsh extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Modcomp slowness identifier
   */
  private long mcshid;

  static final public long MCSHID_NA = Long.MIN_VALUE;

  /**
   * Slowness prediction identifier
   */
  private long predshid;

  static final public long PREDSHID_NA = Long.MIN_VALUE;

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

  static final public double MODEL_UNC_SH_NA = -1;

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

  static final public double PATH_UNC_SH_NA = Double.NaN;

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
   * Derivative of the predicted slowness with respect to longitude
   * <p>
   * Units: s/degree^2
   */
  private double dsh_dlon;

  static final public double DSH_DLON_NA = Double.NaN;

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
   * The weight of the prediction in the location.
   */
  private double blendweight;

  static final public double BLENDWEIGHT_NA = Double.NaN;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("mcshid", Columns.FieldType.LONG, "%d");
    columns.add("predshid", Columns.FieldType.LONG, "%d");
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
    columns.add("dsh_dlon", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("dsh_dlat", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("dsh_dz", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("blendweight", Columns.FieldType.DOUBLE, "%22.15e");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Modcompsh(long mcshid, long predshid, String modname, String regname, double model_sh,
      double model_unc_sh, double base_sh, double base_unc_sh, double path_corr_sh,
      double path_unc_sh, double d3_corr_sh, double d3_unc_sh, double bulk_corr_sh, double dsh_dlon,
      double dsh_dlat, double dsh_dz, double blendweight) {
    setValues(mcshid, predshid, modname, regname, model_sh, model_unc_sh, base_sh, base_unc_sh,
        path_corr_sh, path_unc_sh, d3_corr_sh, d3_unc_sh, bulk_corr_sh, dsh_dlon, dsh_dlat, dsh_dz,
        blendweight);
  }

  private void setValues(long mcshid, long predshid, String modname, String regname,
      double model_sh, double model_unc_sh, double base_sh, double base_unc_sh, double path_corr_sh,
      double path_unc_sh, double d3_corr_sh, double d3_unc_sh, double bulk_corr_sh, double dsh_dlon,
      double dsh_dlat, double dsh_dz, double blendweight) {
    this.mcshid = mcshid;
    this.predshid = predshid;
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
    this.dsh_dlon = dsh_dlon;
    this.dsh_dlat = dsh_dlat;
    this.dsh_dz = dsh_dz;
    this.blendweight = blendweight;
  }

  /**
   * Copy constructor.
   */
  public Modcompsh(Modcompsh other) {
    this.mcshid = other.getMcshid();
    this.predshid = other.getPredshid();
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
    this.dsh_dlon = other.getDsh_dlon();
    this.dsh_dlat = other.getDsh_dlat();
    this.dsh_dz = other.getDsh_dz();
    this.blendweight = other.getBlendweight();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Modcompsh() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MCSHID_NA, PREDSHID_NA, MODNAME_NA, REGNAME_NA, MODEL_SH_NA, MODEL_UNC_SH_NA,
        BASE_SH_NA, BASE_UNC_SH_NA, PATH_CORR_SH_NA, PATH_UNC_SH_NA, D3_CORR_SH_NA, D3_UNC_SH_NA,
        BULK_CORR_SH_NA, DSH_DLON_NA, DSH_DLAT_NA, DSH_DZ_NA, BLENDWEIGHT_NA);
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
      case "dsh_dlon":
        return dsh_dlon;
      case "dsh_dlat":
        return dsh_dlat;
      case "dsh_dz":
        return dsh_dz;
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
      case "dsh_dlon":
        dsh_dlon = value;
        break;
      case "dsh_dlat":
        dsh_dlat = value;
        break;
      case "dsh_dz":
        dsh_dz = value;
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
      case "mcshid":
        return mcshid;
      case "predshid":
        return predshid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "mcshid":
        mcshid = value;
        break;
      case "predshid":
        predshid = value;
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
  public Modcompsh(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Modcompsh(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), readString(input), readString(input),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Modcompsh(ByteBuffer input) {
    this(input.getLong(), input.getLong(), readString(input), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Modcompsh(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Modcompsh(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getDouble(offset + 17));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[17];
    values[0] = mcshid;
    values[1] = predshid;
    values[2] = modname;
    values[3] = regname;
    values[4] = model_sh;
    values[5] = model_unc_sh;
    values[6] = base_sh;
    values[7] = base_unc_sh;
    values[8] = path_corr_sh;
    values[9] = path_unc_sh;
    values[10] = d3_corr_sh;
    values[11] = d3_unc_sh;
    values[12] = bulk_corr_sh;
    values[13] = dsh_dlon;
    values[14] = dsh_dlat;
    values[15] = dsh_dz;
    values[16] = blendweight;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[18];
    values[0] = mcshid;
    values[1] = predshid;
    values[2] = modname;
    values[3] = regname;
    values[4] = model_sh;
    values[5] = model_unc_sh;
    values[6] = base_sh;
    values[7] = base_unc_sh;
    values[8] = path_corr_sh;
    values[9] = path_unc_sh;
    values[10] = d3_corr_sh;
    values[11] = d3_unc_sh;
    values[12] = bulk_corr_sh;
    values[13] = dsh_dlon;
    values[14] = dsh_dlat;
    values[15] = dsh_dz;
    values[16] = blendweight;
    values[17] = lddate;
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
    output.writeLong(mcshid);
    output.writeLong(predshid);
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
    output.writeDouble(dsh_dlon);
    output.writeDouble(dsh_dlat);
    output.writeDouble(dsh_dz);
    output.writeDouble(blendweight);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(mcshid);
    output.putLong(predshid);
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
    output.putDouble(dsh_dlon);
    output.putDouble(dsh_dlat);
    output.putDouble(dsh_dz);
    output.putDouble(blendweight);
  }

  /**
   * Read a Collection of Modcompsh objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Modcompsh objects.
   * @throws IOException
   */
  static public void readModcompshs(BufferedReader input, Collection<Modcompsh> rows)
      throws IOException {
    String[] saved = Modcompsh.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Modcompsh
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Modcompsh(new Scanner(line)));
    }
    input.close();
    Modcompsh.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Modcompsh objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Modcompsh objects.
   * @throws IOException
   */
  static public void readModcompshs(File inputFile, Collection<Modcompsh> rows) throws IOException {
    readModcompshs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Modcompsh objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Modcompsh objects.
   * @throws IOException
   */
  static public void readModcompshs(InputStream inputStream, Collection<Modcompsh> rows)
      throws IOException {
    readModcompshs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Modcompsh objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Modcompsh objects
   * @throws IOException
   */
  static public Set<Modcompsh> readModcompshs(BufferedReader input) throws IOException {
    Set<Modcompsh> rows = new LinkedHashSet<Modcompsh>();
    readModcompshs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Modcompsh objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Modcompsh objects
   * @throws IOException
   */
  static public Set<Modcompsh> readModcompshs(File inputFile) throws IOException {
    return readModcompshs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Modcompsh objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Modcompsh objects
   * @throws IOException
   */
  static public Set<Modcompsh> readModcompshs(InputStream input) throws IOException {
    return readModcompshs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Modcompsh objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param modcompshs the Modcompsh objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Modcompsh> modcompshs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Modcompsh modcompsh : modcompshs)
      modcompsh.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Modcompsh objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param modcompshs the Modcompsh objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Modcompsh> modcompshs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Modcompsh modcompsh : modcompshs) {
        int i = 0;
        statement.setLong(++i, modcompsh.mcshid);
        statement.setLong(++i, modcompsh.predshid);
        statement.setString(++i, modcompsh.modname);
        statement.setString(++i, modcompsh.regname);
        statement.setDouble(++i, modcompsh.model_sh);
        statement.setDouble(++i, modcompsh.model_unc_sh);
        statement.setDouble(++i, modcompsh.base_sh);
        statement.setDouble(++i, modcompsh.base_unc_sh);
        statement.setDouble(++i, modcompsh.path_corr_sh);
        statement.setDouble(++i, modcompsh.path_unc_sh);
        statement.setDouble(++i, modcompsh.d3_corr_sh);
        statement.setDouble(++i, modcompsh.d3_unc_sh);
        statement.setDouble(++i, modcompsh.bulk_corr_sh);
        statement.setDouble(++i, modcompsh.dsh_dlon);
        statement.setDouble(++i, modcompsh.dsh_dlat);
        statement.setDouble(++i, modcompsh.dsh_dz);
        statement.setDouble(++i, modcompsh.blendweight);
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
   *        Modcompsh table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Modcompsh> readModcompshs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Modcompsh> results = new HashSet<Modcompsh>();
    readModcompshs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Modcompsh table.
   * @param modcompshs
   * @throws SQLException
   */
  static public void readModcompshs(Connection connection, String selectStatement,
      Set<Modcompsh> modcompshs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        modcompshs.add(new Modcompsh(rs));
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
   * this Modcompsh object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Modcompsh object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "mcshid, predshid, modname, regname, model_sh, model_unc_sh, base_sh, base_unc_sh, path_corr_sh, path_unc_sh, d3_corr_sh, d3_unc_sh, bulk_corr_sh, dsh_dlon, dsh_dlat, dsh_dz, blendweight, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(mcshid)).append(", ");
    sql.append(Long.toString(predshid)).append(", ");
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
    sql.append(Double.toString(dsh_dlon)).append(", ");
    sql.append(Double.toString(dsh_dlat)).append(", ");
    sql.append(Double.toString(dsh_dz)).append(", ");
    sql.append(Double.toString(blendweight)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Modcompsh in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Modcompsh in the database
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
   * Generate a sql script to create a table of type Modcompsh in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Modcompsh in the database
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
    buf.append("mcshid       number(9)            NOT NULL,\n");
    buf.append("predshid     number(9)            NOT NULL,\n");
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
    buf.append("dsh_dlon     float(53)            NOT NULL,\n");
    buf.append("dsh_dlat     float(53)            NOT NULL,\n");
    buf.append("dsh_dz       float(53)            NOT NULL,\n");
    buf.append("blendweight  float(53)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (mcshid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (predshid,regname)");
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
    return 248;
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
    return (other instanceof Modcompsh) && ((Modcompsh) other).mcshid == mcshid;
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
    return (other instanceof Modcompsh) && ((Modcompsh) other).predshid == predshid
        && ((Modcompsh) other).regname.equals(regname);
  }

  /**
   * Modcomp slowness identifier
   * 
   * @return mcshid
   */
  public long getMcshid() {
    return mcshid;
  }

  /**
   * Modcomp slowness identifier
   * 
   * @param mcshid
   * @throws IllegalArgumentException if mcshid >= 1000000000
   */
  public Modcompsh setMcshid(long mcshid) {
    if (mcshid >= 1000000000L)
      throw new IllegalArgumentException("mcshid=" + mcshid + " but cannot be >= 1000000000");
    this.mcshid = mcshid;
    setHash(null);
    return this;
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
  public Modcompsh setPredshid(long predshid) {
    if (predshid >= 1000000000L)
      throw new IllegalArgumentException("predshid=" + predshid + " but cannot be >= 1000000000");
    this.predshid = predshid;
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
  public Modcompsh setModname(String modname) {
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
  public Modcompsh setRegname(String regname) {
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
  public Modcompsh setModel_sh(double model_sh) {
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
  public Modcompsh setModel_unc_sh(double model_unc_sh) {
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
  public Modcompsh setBase_sh(double base_sh) {
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
  public Modcompsh setBase_unc_sh(double base_unc_sh) {
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
  public Modcompsh setPath_corr_sh(double path_corr_sh) {
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
  public Modcompsh setPath_unc_sh(double path_unc_sh) {
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
  public Modcompsh setD3_corr_sh(double d3_corr_sh) {
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
  public Modcompsh setD3_unc_sh(double d3_unc_sh) {
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
  public Modcompsh setBulk_corr_sh(double bulk_corr_sh) {
    this.bulk_corr_sh = bulk_corr_sh;
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
  public Modcompsh setDsh_dlon(double dsh_dlon) {
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
  public Modcompsh setDsh_dlat(double dsh_dlat) {
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
  public Modcompsh setDsh_dz(double dsh_dz) {
    this.dsh_dz = dsh_dz;
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
  public Modcompsh setBlendweight(double blendweight) {
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
