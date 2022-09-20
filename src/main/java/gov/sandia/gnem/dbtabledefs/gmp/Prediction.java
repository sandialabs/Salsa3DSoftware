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
package gov.sandia.gnem.dbtabledefs.gmp;

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
 * ?
 */
public class Prediction extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * -
   */
  private long predictionid;

  static final public long PREDICTIONID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long sourceid;

  static final public long SOURCEID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long receiverid;

  static final public long RECEIVERID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long geomodelid;

  static final public long GEOMODELID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long algorithmid;

  static final public long ALGORITHMID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long observationid;

  static final public long OBSERVATIONID_NA = -1;

  /**
   * -
   */
  private String phase;

  static final public String PHASE_NA = null;

  /**
   * -
   */
  private String raytype;

  static final public String RAYTYPE_NA = null;

  /**
   * -
   */
  private double activefraction;

  static final public double ACTIVEFRACTION_NA = Double.NaN;

  /**
   * -
   */
  private double traveltime;

  static final public double TRAVELTIME_NA = Double.NaN;

  /**
   * -
   */
  private double ttresidual;

  static final public double TTRESIDUAL_NA = -999.0;

  /**
   * -
   */
  private double azimuth;

  static final public double AZIMUTH_NA = Double.NaN;

  /**
   * -
   */
  private double slowness;

  static final public double SLOWNESS_NA = -999.0;

  /**
   * -
   */
  private double backazimuth;

  static final public double BACKAZIMUTH_NA = -999.0;

  /**
   * -
   */
  private double turndepth;

  static final public double TURNDEPTH_NA = -999.0;

  /**
   * -
   */
  private double maxoutplane;

  static final public double MAXOUTPLANE_NA = -999.0;

  /**
   * -
   */
  private double calctime;

  static final public double CALCTIME_NA = -999.0;

  /**
   * -
   */
  private String inpolygon;

  static final public String INPOLYGON_NA = null;

  /**
   * -
   */
  private String auth;

  static final public String AUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("predictionid", Columns.FieldType.LONG, "%d");
    columns.add("sourceid", Columns.FieldType.LONG, "%d");
    columns.add("receiverid", Columns.FieldType.LONG, "%d");
    columns.add("geomodelid", Columns.FieldType.LONG, "%d");
    columns.add("algorithmid", Columns.FieldType.LONG, "%d");
    columns.add("observationid", Columns.FieldType.LONG, "%d");
    columns.add("phase", Columns.FieldType.STRING, "%s");
    columns.add("raytype", Columns.FieldType.STRING, "%s");
    columns.add("activefraction", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("traveltime", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("ttresidual", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("azimuth", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("slowness", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("backazimuth", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("turndepth", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("maxoutplane", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("calctime", Columns.FieldType.DOUBLE, "%22.15e");
    columns.add("inpolygon", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Prediction(long predictionid, long sourceid, long receiverid, long geomodelid,
      long algorithmid, long observationid, String phase, String raytype, double activefraction,
      double traveltime, double ttresidual, double azimuth, double slowness, double backazimuth,
      double turndepth, double maxoutplane, double calctime, String inpolygon, String auth) {
    setValues(predictionid, sourceid, receiverid, geomodelid, algorithmid, observationid, phase,
        raytype, activefraction, traveltime, ttresidual, azimuth, slowness, backazimuth, turndepth,
        maxoutplane, calctime, inpolygon, auth);
  }

  private void setValues(long predictionid, long sourceid, long receiverid, long geomodelid,
      long algorithmid, long observationid, String phase, String raytype, double activefraction,
      double traveltime, double ttresidual, double azimuth, double slowness, double backazimuth,
      double turndepth, double maxoutplane, double calctime, String inpolygon, String auth) {
    this.predictionid = predictionid;
    this.sourceid = sourceid;
    this.receiverid = receiverid;
    this.geomodelid = geomodelid;
    this.algorithmid = algorithmid;
    this.observationid = observationid;
    this.phase = phase;
    this.raytype = raytype;
    this.activefraction = activefraction;
    this.traveltime = traveltime;
    this.ttresidual = ttresidual;
    this.azimuth = azimuth;
    this.slowness = slowness;
    this.backazimuth = backazimuth;
    this.turndepth = turndepth;
    this.maxoutplane = maxoutplane;
    this.calctime = calctime;
    this.inpolygon = inpolygon;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Prediction(Prediction other) {
    this.predictionid = other.getPredictionid();
    this.sourceid = other.getSourceid();
    this.receiverid = other.getReceiverid();
    this.geomodelid = other.getGeomodelid();
    this.algorithmid = other.getAlgorithmid();
    this.observationid = other.getObservationid();
    this.phase = other.getPhase();
    this.raytype = other.getRaytype();
    this.activefraction = other.getActivefraction();
    this.traveltime = other.getTraveltime();
    this.ttresidual = other.getTtresidual();
    this.azimuth = other.getAzimuth();
    this.slowness = other.getSlowness();
    this.backazimuth = other.getBackazimuth();
    this.turndepth = other.getTurndepth();
    this.maxoutplane = other.getMaxoutplane();
    this.calctime = other.getCalctime();
    this.inpolygon = other.getInpolygon();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Prediction() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(PREDICTIONID_NA, SOURCEID_NA, RECEIVERID_NA, GEOMODELID_NA, ALGORITHMID_NA,
        OBSERVATIONID_NA, PHASE_NA, RAYTYPE_NA, ACTIVEFRACTION_NA, TRAVELTIME_NA, TTRESIDUAL_NA,
        AZIMUTH_NA, SLOWNESS_NA, BACKAZIMUTH_NA, TURNDEPTH_NA, MAXOUTPLANE_NA, CALCTIME_NA,
        INPOLYGON_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "phase":
        return phase;
      case "raytype":
        return raytype;
      case "inpolygon":
        return inpolygon;
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
      case "phase":
        phase = value;
        break;
      case "raytype":
        raytype = value;
        break;
      case "inpolygon":
        inpolygon = value;
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
      case "activefraction":
        return activefraction;
      case "traveltime":
        return traveltime;
      case "ttresidual":
        return ttresidual;
      case "azimuth":
        return azimuth;
      case "slowness":
        return slowness;
      case "backazimuth":
        return backazimuth;
      case "turndepth":
        return turndepth;
      case "maxoutplane":
        return maxoutplane;
      case "calctime":
        return calctime;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "activefraction":
        activefraction = value;
        break;
      case "traveltime":
        traveltime = value;
        break;
      case "ttresidual":
        ttresidual = value;
        break;
      case "azimuth":
        azimuth = value;
        break;
      case "slowness":
        slowness = value;
        break;
      case "backazimuth":
        backazimuth = value;
        break;
      case "turndepth":
        turndepth = value;
        break;
      case "maxoutplane":
        maxoutplane = value;
        break;
      case "calctime":
        calctime = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "predictionid":
        return predictionid;
      case "sourceid":
        return sourceid;
      case "receiverid":
        return receiverid;
      case "geomodelid":
        return geomodelid;
      case "algorithmid":
        return algorithmid;
      case "observationid":
        return observationid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "predictionid":
        predictionid = value;
        break;
      case "sourceid":
        sourceid = value;
        break;
      case "receiverid":
        receiverid = value;
        break;
      case "geomodelid":
        geomodelid = value;
        break;
      case "algorithmid":
        algorithmid = value;
        break;
      case "observationid":
        observationid = value;
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
  public Prediction(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Prediction(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        input.readLong(), readString(input), readString(input), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Prediction(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getLong(),
        input.getLong(), readString(input), readString(input), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Prediction(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Prediction(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getDouble(offset + 17), input.getString(offset + 18),
        input.getString(offset + 19));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[19];
    values[0] = predictionid;
    values[1] = sourceid;
    values[2] = receiverid;
    values[3] = geomodelid;
    values[4] = algorithmid;
    values[5] = observationid;
    values[6] = phase;
    values[7] = raytype;
    values[8] = activefraction;
    values[9] = traveltime;
    values[10] = ttresidual;
    values[11] = azimuth;
    values[12] = slowness;
    values[13] = backazimuth;
    values[14] = turndepth;
    values[15] = maxoutplane;
    values[16] = calctime;
    values[17] = inpolygon;
    values[18] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[20];
    values[0] = predictionid;
    values[1] = sourceid;
    values[2] = receiverid;
    values[3] = geomodelid;
    values[4] = algorithmid;
    values[5] = observationid;
    values[6] = phase;
    values[7] = raytype;
    values[8] = activefraction;
    values[9] = traveltime;
    values[10] = ttresidual;
    values[11] = azimuth;
    values[12] = slowness;
    values[13] = backazimuth;
    values[14] = turndepth;
    values[15] = maxoutplane;
    values[16] = calctime;
    values[17] = inpolygon;
    values[18] = auth;
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
    output.writeLong(predictionid);
    output.writeLong(sourceid);
    output.writeLong(receiverid);
    output.writeLong(geomodelid);
    output.writeLong(algorithmid);
    output.writeLong(observationid);
    writeString(output, phase);
    writeString(output, raytype);
    output.writeDouble(activefraction);
    output.writeDouble(traveltime);
    output.writeDouble(ttresidual);
    output.writeDouble(azimuth);
    output.writeDouble(slowness);
    output.writeDouble(backazimuth);
    output.writeDouble(turndepth);
    output.writeDouble(maxoutplane);
    output.writeDouble(calctime);
    writeString(output, inpolygon);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(predictionid);
    output.putLong(sourceid);
    output.putLong(receiverid);
    output.putLong(geomodelid);
    output.putLong(algorithmid);
    output.putLong(observationid);
    writeString(output, phase);
    writeString(output, raytype);
    output.putDouble(activefraction);
    output.putDouble(traveltime);
    output.putDouble(ttresidual);
    output.putDouble(azimuth);
    output.putDouble(slowness);
    output.putDouble(backazimuth);
    output.putDouble(turndepth);
    output.putDouble(maxoutplane);
    output.putDouble(calctime);
    writeString(output, inpolygon);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Prediction objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Prediction objects.
   * @throws IOException
   */
  static public void readPredictions(BufferedReader input, Collection<Prediction> rows)
      throws IOException {
    String[] saved = Prediction.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Prediction
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Prediction(new Scanner(line)));
    }
    input.close();
    Prediction.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Prediction objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Prediction objects.
   * @throws IOException
   */
  static public void readPredictions(File inputFile, Collection<Prediction> rows)
      throws IOException {
    readPredictions(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Prediction objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Prediction objects.
   * @throws IOException
   */
  static public void readPredictions(InputStream inputStream, Collection<Prediction> rows)
      throws IOException {
    readPredictions(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Prediction objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Prediction objects
   * @throws IOException
   */
  static public Set<Prediction> readPredictions(BufferedReader input) throws IOException {
    Set<Prediction> rows = new LinkedHashSet<Prediction>();
    readPredictions(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Prediction objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Prediction objects
   * @throws IOException
   */
  static public Set<Prediction> readPredictions(File inputFile) throws IOException {
    return readPredictions(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Prediction objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Prediction objects
   * @throws IOException
   */
  static public Set<Prediction> readPredictions(InputStream input) throws IOException {
    return readPredictions(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Prediction objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param predictions the Prediction objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Prediction> predictions)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Prediction prediction : predictions)
      prediction.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Prediction objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param predictions the Prediction objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Prediction> predictions, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Prediction prediction : predictions) {
        int i = 0;
        statement.setLong(++i, prediction.predictionid);
        statement.setLong(++i, prediction.sourceid);
        statement.setLong(++i, prediction.receiverid);
        statement.setLong(++i, prediction.geomodelid);
        statement.setLong(++i, prediction.algorithmid);
        statement.setLong(++i, prediction.observationid);
        statement.setString(++i, prediction.phase);
        statement.setString(++i, prediction.raytype);
        statement.setDouble(++i, prediction.activefraction);
        statement.setDouble(++i, prediction.traveltime);
        statement.setDouble(++i, prediction.ttresidual);
        statement.setDouble(++i, prediction.azimuth);
        statement.setDouble(++i, prediction.slowness);
        statement.setDouble(++i, prediction.backazimuth);
        statement.setDouble(++i, prediction.turndepth);
        statement.setDouble(++i, prediction.maxoutplane);
        statement.setDouble(++i, prediction.calctime);
        statement.setString(++i, prediction.inpolygon);
        statement.setString(++i, prediction.auth);
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
   *        Prediction table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Prediction> readPredictions(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Prediction> results = new HashSet<Prediction>();
    readPredictions(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Prediction table.
   * @param predictions
   * @throws SQLException
   */
  static public void readPredictions(Connection connection, String selectStatement,
      Set<Prediction> predictions) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        predictions.add(new Prediction(rs));
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
   * this Prediction object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Prediction object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "predictionid, sourceid, receiverid, geomodelid, algorithmid, observationid, phase, raytype, activefraction, traveltime, ttresidual, azimuth, slowness, backazimuth, turndepth, maxoutplane, calctime, inpolygon, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(predictionid)).append(", ");
    sql.append(Long.toString(sourceid)).append(", ");
    sql.append(Long.toString(receiverid)).append(", ");
    sql.append(Long.toString(geomodelid)).append(", ");
    sql.append(Long.toString(algorithmid)).append(", ");
    sql.append(Long.toString(observationid)).append(", ");
    sql.append("'").append(phase).append("', ");
    sql.append("'").append(raytype).append("', ");
    sql.append(Double.toString(activefraction)).append(", ");
    sql.append(Double.toString(traveltime)).append(", ");
    sql.append(Double.toString(ttresidual)).append(", ");
    sql.append(Double.toString(azimuth)).append(", ");
    sql.append(Double.toString(slowness)).append(", ");
    sql.append(Double.toString(backazimuth)).append(", ");
    sql.append(Double.toString(turndepth)).append(", ");
    sql.append(Double.toString(maxoutplane)).append(", ");
    sql.append(Double.toString(calctime)).append(", ");
    sql.append("'").append(inpolygon).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Prediction in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Prediction in the database
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
   * Generate a sql script to create a table of type Prediction in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Prediction in the database
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
    buf.append("predictionid number(10)           NOT NULL,\n");
    buf.append("sourceid     number(10)           NOT NULL,\n");
    buf.append("receiverid   number(10)           NOT NULL,\n");
    buf.append("geomodelid   number(10)           NOT NULL,\n");
    buf.append("algorithmid  number(10)           NOT NULL,\n");
    buf.append("observationid number(10)           NOT NULL,\n");
    buf.append("phase        varchar2(30)         NOT NULL,\n");
    buf.append("raytype      varchar2(64)         NOT NULL,\n");
    buf.append("activefraction float(126)           NOT NULL,\n");
    buf.append("traveltime   float(126)           NOT NULL,\n");
    buf.append("ttresidual   float(126)           NOT NULL,\n");
    buf.append("azimuth      float(126)           NOT NULL,\n");
    buf.append("slowness     float(126)           NOT NULL,\n");
    buf.append("backazimuth  float(126)           NOT NULL,\n");
    buf.append("turndepth    float(126)           NOT NULL,\n");
    buf.append("maxoutplane  float(126)           NOT NULL,\n");
    buf.append("calctime     float(126)           NOT NULL,\n");
    buf.append("inpolygon    varchar2(2)          NOT NULL,\n");
    buf.append("auth         varchar2(64)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (predictionid)");
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
    return 296;
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
    return (other instanceof Prediction) && ((Prediction) other).predictionid == predictionid;
  }

  /**
   * -
   * 
   * @return predictionid
   */
  public long getPredictionid() {
    return predictionid;
  }

  /**
   * -
   * 
   * @param predictionid
   * @throws IllegalArgumentException if predictionid >= 10000000000
   */
  public Prediction setPredictionid(long predictionid) {
    if (predictionid >= 10000000000L)
      throw new IllegalArgumentException(
          "predictionid=" + predictionid + " but cannot be >= 10000000000");
    this.predictionid = predictionid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return sourceid
   */
  public long getSourceid() {
    return sourceid;
  }

  /**
   * -
   * 
   * @param sourceid
   * @throws IllegalArgumentException if sourceid >= 10000000000
   */
  public Prediction setSourceid(long sourceid) {
    if (sourceid >= 10000000000L)
      throw new IllegalArgumentException("sourceid=" + sourceid + " but cannot be >= 10000000000");
    this.sourceid = sourceid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return receiverid
   */
  public long getReceiverid() {
    return receiverid;
  }

  /**
   * -
   * 
   * @param receiverid
   * @throws IllegalArgumentException if receiverid >= 10000000000
   */
  public Prediction setReceiverid(long receiverid) {
    if (receiverid >= 10000000000L)
      throw new IllegalArgumentException(
          "receiverid=" + receiverid + " but cannot be >= 10000000000");
    this.receiverid = receiverid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return geomodelid
   */
  public long getGeomodelid() {
    return geomodelid;
  }

  /**
   * -
   * 
   * @param geomodelid
   * @throws IllegalArgumentException if geomodelid >= 10000000000
   */
  public Prediction setGeomodelid(long geomodelid) {
    if (geomodelid >= 10000000000L)
      throw new IllegalArgumentException(
          "geomodelid=" + geomodelid + " but cannot be >= 10000000000");
    this.geomodelid = geomodelid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return algorithmid
   */
  public long getAlgorithmid() {
    return algorithmid;
  }

  /**
   * -
   * 
   * @param algorithmid
   * @throws IllegalArgumentException if algorithmid >= 10000000000
   */
  public Prediction setAlgorithmid(long algorithmid) {
    if (algorithmid >= 10000000000L)
      throw new IllegalArgumentException(
          "algorithmid=" + algorithmid + " but cannot be >= 10000000000");
    this.algorithmid = algorithmid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return observationid
   */
  public long getObservationid() {
    return observationid;
  }

  /**
   * -
   * 
   * @param observationid
   * @throws IllegalArgumentException if observationid >= 10000000000
   */
  public Prediction setObservationid(long observationid) {
    if (observationid >= 10000000000L)
      throw new IllegalArgumentException(
          "observationid=" + observationid + " but cannot be >= 10000000000");
    this.observationid = observationid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return phase
   */
  public String getPhase() {
    return phase;
  }

  /**
   * -
   * 
   * @param phase
   * @throws IllegalArgumentException if phase.length() >= 30
   */
  public Prediction setPhase(String phase) {
    if (phase.length() > 30)
      throw new IllegalArgumentException(
          String.format("phase.length() cannot be > 30.  phase=%s", phase));
    this.phase = phase;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return raytype
   */
  public String getRaytype() {
    return raytype;
  }

  /**
   * -
   * 
   * @param raytype
   * @throws IllegalArgumentException if raytype.length() >= 64
   */
  public Prediction setRaytype(String raytype) {
    if (raytype.length() > 64)
      throw new IllegalArgumentException(
          String.format("raytype.length() cannot be > 64.  raytype=%s", raytype));
    this.raytype = raytype;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return activefraction
   */
  public double getActivefraction() {
    return activefraction;
  }

  /**
   * -
   * 
   * @param activefraction
   */
  public Prediction setActivefraction(double activefraction) {
    this.activefraction = activefraction;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return traveltime
   */
  public double getTraveltime() {
    return traveltime;
  }

  /**
   * -
   * 
   * @param traveltime
   */
  public Prediction setTraveltime(double traveltime) {
    this.traveltime = traveltime;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return ttresidual
   */
  public double getTtresidual() {
    return ttresidual;
  }

  /**
   * -
   * 
   * @param ttresidual
   */
  public Prediction setTtresidual(double ttresidual) {
    this.ttresidual = ttresidual;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return azimuth
   */
  public double getAzimuth() {
    return azimuth;
  }

  /**
   * -
   * 
   * @param azimuth
   */
  public Prediction setAzimuth(double azimuth) {
    this.azimuth = azimuth;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return slowness
   */
  public double getSlowness() {
    return slowness;
  }

  /**
   * -
   * 
   * @param slowness
   */
  public Prediction setSlowness(double slowness) {
    this.slowness = slowness;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return backazimuth
   */
  public double getBackazimuth() {
    return backazimuth;
  }

  /**
   * -
   * 
   * @param backazimuth
   */
  public Prediction setBackazimuth(double backazimuth) {
    this.backazimuth = backazimuth;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return turndepth
   */
  public double getTurndepth() {
    return turndepth;
  }

  /**
   * -
   * 
   * @param turndepth
   */
  public Prediction setTurndepth(double turndepth) {
    this.turndepth = turndepth;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return maxoutplane
   */
  public double getMaxoutplane() {
    return maxoutplane;
  }

  /**
   * -
   * 
   * @param maxoutplane
   */
  public Prediction setMaxoutplane(double maxoutplane) {
    this.maxoutplane = maxoutplane;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return calctime
   */
  public double getCalctime() {
    return calctime;
  }

  /**
   * -
   * 
   * @param calctime
   */
  public Prediction setCalctime(double calctime) {
    this.calctime = calctime;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return inpolygon
   */
  public String getInpolygon() {
    return inpolygon;
  }

  /**
   * -
   * 
   * @param inpolygon
   * @throws IllegalArgumentException if inpolygon.length() >= 2
   */
  public Prediction setInpolygon(String inpolygon) {
    if (inpolygon.length() > 2)
      throw new IllegalArgumentException(
          String.format("inpolygon.length() cannot be > 2.  inpolygon=%s", inpolygon));
    this.inpolygon = inpolygon;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * -
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 64
   */
  public Prediction setAuth(String auth) {
    if (auth.length() > 64)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 64.  auth=%s", auth));
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
    return "GMP";
  }

}
