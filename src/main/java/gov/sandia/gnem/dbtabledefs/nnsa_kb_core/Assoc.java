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

import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * assoc
 */
public class Assoc extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta, chan</I> and <I>time</I>.
   */
  private long arid;

  static final public long ARID_NA = Long.MIN_VALUE;

  /**
   * Origin identifier that relates a record in these tables to a record in the <B>origin</B> table
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location recorded in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Phase type. The identity of a phase that has been associated to an arrival. Standard labels for
   * phases are used (for example, P, PKP, PcP, pP, etc.). Both upper- and lower-case letters are
   * available and should be used when appropriate (for example, pP or PcP).
   */
  private String phase;

  static final public String PHASE_NA = "-";

  /**
   * Phase identification confidence level. This value is a qualitative estimate of the confidence
   * that a seismic phase is correctly identified.
   */
  private double belief;

  static final public double BELIEF_NA = -1;

  /**
   * Source-receiver distance. This column is the arc length, over the Earth s surface, of the path
   * the seismic phase follows from source to receiver. The location of the origin is specified in
   * the <B>origin</B> record referenced by the column <I>orid</I>. The column <I>arid</I> points to
   * the record in the <B>arrival</B> table that identifies the receiver. The value of the column
   * can exceed 360 degrees. The geographic distance between source and receiver is delta
   * modulo(180).
   * <p>
   * Units: degree
   */
  private double delta;

  static final public double DELTA_NA = -1;

  /**
   * Station-to-event azimuth calculated from the station and event locations and measured clockwise
   * from north.
   * <p>
   * Units: degree
   */
  private double seaz;

  static final public double SEAZ_NA = -1;

  /**
   * Event to station azimuth measured in degrees clockwise from north.
   * <p>
   * Units: degree
   */
  private double esaz;

  static final public double ESAZ_NA = -1;

  /**
   * Time residual. This column is a travel-time residual measured in seconds. The residual is found
   * by taking the observed arrival time (saved in the <B>arrival</B> table) of a seismic phase and
   * subtracting the expected arrival time. The expected arrival time is calculated by a formula
   * based on an earth velocity model (column <I>vmodel</I>), an event location and origin time
   * (saved in <B>origin</B> table), and the particular seismic phase (column <I>phase</I> in
   * <B>assoc</B> table).
   * <p>
   * Units: s
   */
  private double timeres;

  static final public double TIMERES_NA = -999;

  /**
   * Time-defining code. This one-character flag indicates whether or not the time of a phase was
   * used to constrain the event location. This column is defining (<I>timedef</I> = d) or
   * nondefining (<I>timedef</I> = n).
   */
  private String timedef;

  static final public String TIMEDEF_NA = "-";

  /**
   * Azimuth residual. This value is the difference between the measured station-to-event azimuth
   * for an arrival and the true azimuth. The true azimuth is the bearing to the inferred event
   * origin.
   * <p>
   * Units: degree
   */
  private double azres;

  static final public double AZRES_NA = -999;

  /**
   * Azimuth-defining code; one-character flag indicates whether or not the azimuth of a phase was
   * used to constrain the event location solution. This column is defining (<I>azdef</I> = d) if it
   * was used in the location, nondefining (<I>azdef</I> = n) if it was not.
   */
  private String azdef;

  static final public String AZDEF_NA = "-";

  /**
   * Slowness residual. This column gives the difference between an observed slowness and a
   * theoretical prediction. The prediction is calculated for the related phase and event origin
   * described in the record.
   * <p>
   * Units: s/degree
   */
  private double slores;

  static final public double SLORES_NA = -999;

  /**
   * Slowness defining code. This one-character flag indicates whether or not the slowness of a
   * phase was used to constrain the event location. This column is defining (<I>slodef</I> = d) or
   * nondefining (<I>slodef</I> = n) for this arrival.
   */
  private String slodef;

  static final public String SLODEF_NA = "-";

  /**
   * Emergence angle residual. This column is the difference between an observed emergence angle and
   * the theoretical prediction for the same phase, assuming an event location as specified by the
   * accompanying <I>orid</I>.
   * <p>
   * Units: degree
   */
  private double emares;

  static final public double EMARES_NA = -999;

  /**
   * Location weight. This column gives the final weight assigned to the allied arrival by the
   * location program. This column is used primarily for location programs that adaptively weight
   * data by their residuals.
   */
  private double wgt;

  static final public double WGT_NA = -1;

  /**
   * Velocity model. This character string identifies the velocity model of the Earth used to
   * compute the travel times of seismic phases. A velocity model is required for event location (if
   * <I>phase</I> is defining) or for computing travel-time residuals.
   */
  private String vmodel;

  static final public String VMODEL_NA = "-";

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
    columns.add("arid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("phase", Columns.FieldType.STRING, "%s");
    columns.add("belief", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("delta", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("seaz", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("esaz", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("timeres", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("timedef", Columns.FieldType.STRING, "%s");
    columns.add("azres", Columns.FieldType.DOUBLE, "%1.1f");
    columns.add("azdef", Columns.FieldType.STRING, "%s");
    columns.add("slores", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("slodef", Columns.FieldType.STRING, "%s");
    columns.add("emares", Columns.FieldType.DOUBLE, "%1.1f");
    columns.add("wgt", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("vmodel", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Assoc(long arid, long orid, String sta, String phase, double belief, double delta,
      double seaz, double esaz, double timeres, String timedef, double azres, String azdef,
      double slores, String slodef, double emares, double wgt, String vmodel, long commid) {
    setValues(arid, orid, sta, phase, belief, delta, seaz, esaz, timeres, timedef, azres, azdef,
        slores, slodef, emares, wgt, vmodel, commid);
  }

  private void setValues(long arid, long orid, String sta, String phase, double belief,
      double delta, double seaz, double esaz, double timeres, String timedef, double azres,
      String azdef, double slores, String slodef, double emares, double wgt, String vmodel,
      long commid) {
    this.arid = arid;
    this.orid = orid;
    this.sta = sta;
    this.phase = phase;
    this.belief = belief;
    this.delta = delta;
    this.seaz = seaz;
    this.esaz = esaz;
    this.timeres = timeres;
    this.timedef = timedef;
    this.azres = azres;
    this.azdef = azdef;
    this.slores = slores;
    this.slodef = slodef;
    this.emares = emares;
    this.wgt = wgt;
    this.vmodel = vmodel;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Assoc(Assoc other) {
    this.arid = other.getArid();
    this.orid = other.getOrid();
    this.sta = other.getSta();
    this.phase = other.getPhase();
    this.belief = other.getBelief();
    this.delta = other.getDelta();
    this.seaz = other.getSeaz();
    this.esaz = other.getEsaz();
    this.timeres = other.getTimeres();
    this.timedef = other.getTimedef();
    this.azres = other.getAzres();
    this.azdef = other.getAzdef();
    this.slores = other.getSlores();
    this.slodef = other.getSlodef();
    this.emares = other.getEmares();
    this.wgt = other.getWgt();
    this.vmodel = other.getVmodel();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Assoc() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(ARID_NA, ORID_NA, STA_NA, PHASE_NA, BELIEF_NA, DELTA_NA, SEAZ_NA, ESAZ_NA, TIMERES_NA,
        TIMEDEF_NA, AZRES_NA, AZDEF_NA, SLORES_NA, SLODEF_NA, EMARES_NA, WGT_NA, VMODEL_NA,
        COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "phase":
        return phase;
      case "timedef":
        return timedef;
      case "azdef":
        return azdef;
      case "slodef":
        return slodef;
      case "vmodel":
        return vmodel;
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
      case "phase":
        phase = value;
        break;
      case "timedef":
        timedef = value;
        break;
      case "azdef":
        azdef = value;
        break;
      case "slodef":
        slodef = value;
        break;
      case "vmodel":
        vmodel = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "belief":
        return belief;
      case "delta":
        return delta;
      case "seaz":
        return seaz;
      case "esaz":
        return esaz;
      case "timeres":
        return timeres;
      case "azres":
        return azres;
      case "slores":
        return slores;
      case "emares":
        return emares;
      case "wgt":
        return wgt;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "belief":
        belief = value;
        break;
      case "delta":
        delta = value;
        break;
      case "seaz":
        seaz = value;
        break;
      case "esaz":
        esaz = value;
        break;
      case "timeres":
        timeres = value;
        break;
      case "azres":
        azres = value;
        break;
      case "slores":
        slores = value;
        break;
      case "emares":
        emares = value;
        break;
      case "wgt":
        wgt = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "arid":
        return arid;
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
      case "arid":
        arid = value;
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
  public Assoc(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Assoc(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), readString(input), readString(input),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), readString(input), input.readDouble(), readString(input),
        input.readDouble(), readString(input), input.readDouble(), input.readDouble(),
        readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Assoc(ByteBuffer input) {
    this(input.getLong(), input.getLong(), readString(input), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        readString(input), input.getDouble(), readString(input), input.getDouble(),
        readString(input), input.getDouble(), input.getDouble(), readString(input),
        input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Assoc(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Assoc(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getString(offset + 10), input.getDouble(offset + 11), input.getString(offset + 12),
        input.getDouble(offset + 13), input.getString(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getString(offset + 17), input.getLong(offset + 18));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[18];
    values[0] = arid;
    values[1] = orid;
    values[2] = sta;
    values[3] = phase;
    values[4] = belief;
    values[5] = delta;
    values[6] = seaz;
    values[7] = esaz;
    values[8] = timeres;
    values[9] = timedef;
    values[10] = azres;
    values[11] = azdef;
    values[12] = slores;
    values[13] = slodef;
    values[14] = emares;
    values[15] = wgt;
    values[16] = vmodel;
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
    values[0] = arid;
    values[1] = orid;
    values[2] = sta;
    values[3] = phase;
    values[4] = belief;
    values[5] = delta;
    values[6] = seaz;
    values[7] = esaz;
    values[8] = timeres;
    values[9] = timedef;
    values[10] = azres;
    values[11] = azdef;
    values[12] = slores;
    values[13] = slodef;
    values[14] = emares;
    values[15] = wgt;
    values[16] = vmodel;
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
    output.writeLong(arid);
    output.writeLong(orid);
    writeString(output, sta);
    writeString(output, phase);
    output.writeDouble(belief);
    output.writeDouble(delta);
    output.writeDouble(seaz);
    output.writeDouble(esaz);
    output.writeDouble(timeres);
    writeString(output, timedef);
    output.writeDouble(azres);
    writeString(output, azdef);
    output.writeDouble(slores);
    writeString(output, slodef);
    output.writeDouble(emares);
    output.writeDouble(wgt);
    writeString(output, vmodel);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(arid);
    output.putLong(orid);
    writeString(output, sta);
    writeString(output, phase);
    output.putDouble(belief);
    output.putDouble(delta);
    output.putDouble(seaz);
    output.putDouble(esaz);
    output.putDouble(timeres);
    writeString(output, timedef);
    output.putDouble(azres);
    writeString(output, azdef);
    output.putDouble(slores);
    writeString(output, slodef);
    output.putDouble(emares);
    output.putDouble(wgt);
    writeString(output, vmodel);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Assoc objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Assoc objects.
   * @throws IOException
   */
  static public void readAssocs(BufferedReader input, Collection<Assoc> rows) throws IOException {
    String[] saved = Assoc.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Assoc.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Assoc(new Scanner(line)));
    }
    input.close();
    Assoc.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Assoc objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Assoc objects.
   * @throws IOException
   */
  static public void readAssocs(File inputFile, Collection<Assoc> rows) throws IOException {
    readAssocs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Assoc objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Assoc objects.
   * @throws IOException
   */
  static public void readAssocs(InputStream inputStream, Collection<Assoc> rows)
      throws IOException {
    readAssocs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Assoc objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Assoc objects
   * @throws IOException
   */
  static public Set<Assoc> readAssocs(BufferedReader input) throws IOException {
    Set<Assoc> rows = new LinkedHashSet<Assoc>();
    readAssocs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Assoc objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Assoc objects
   * @throws IOException
   */
  static public Set<Assoc> readAssocs(File inputFile) throws IOException {
    return readAssocs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Assoc objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Assoc objects
   * @throws IOException
   */
  static public Set<Assoc> readAssocs(InputStream input) throws IOException {
    return readAssocs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Assoc objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param assocs the Assoc objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Assoc> assocs) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Assoc assoc : assocs)
      assoc.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Assoc objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param assocs the Assoc objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Assoc> assocs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Assoc assoc : assocs) {
        int i = 0;
        statement.setLong(++i, assoc.arid);
        statement.setLong(++i, assoc.orid);
        statement.setString(++i, assoc.sta);
        statement.setString(++i, assoc.phase);
        statement.setDouble(++i, assoc.belief);
        statement.setDouble(++i, assoc.delta);
        statement.setDouble(++i, assoc.seaz);
        statement.setDouble(++i, assoc.esaz);
        statement.setDouble(++i, assoc.timeres);
        statement.setString(++i, assoc.timedef);
        statement.setDouble(++i, assoc.azres);
        statement.setString(++i, assoc.azdef);
        statement.setDouble(++i, assoc.slores);
        statement.setString(++i, assoc.slodef);
        statement.setDouble(++i, assoc.emares);
        statement.setDouble(++i, assoc.wgt);
        statement.setString(++i, assoc.vmodel);
        statement.setLong(++i, assoc.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Assoc
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Assoc> readAssocs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Assoc> results = new HashSet<Assoc>();
    readAssocs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Assoc
   *        table.
   * @param assocs
   * @throws SQLException
   */
  static public void readAssocs(Connection connection, String selectStatement, Set<Assoc> assocs)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        assocs.add(new Assoc(rs));
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
   * this Assoc object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Assoc object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "arid, orid, sta, phase, belief, delta, seaz, esaz, timeres, timedef, azres, azdef, slores, slodef, emares, wgt, vmodel, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(arid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(phase).append("', ");
    sql.append(Double.toString(belief)).append(", ");
    sql.append(Double.toString(delta)).append(", ");
    sql.append(Double.toString(seaz)).append(", ");
    sql.append(Double.toString(esaz)).append(", ");
    sql.append(Double.toString(timeres)).append(", ");
    sql.append("'").append(timedef).append("', ");
    sql.append(Double.toString(azres)).append(", ");
    sql.append("'").append(azdef).append("', ");
    sql.append(Double.toString(slores)).append(", ");
    sql.append("'").append(slodef).append("', ");
    sql.append(Double.toString(emares)).append(", ");
    sql.append(Double.toString(wgt)).append(", ");
    sql.append("'").append(vmodel).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Assoc in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Assoc in the database
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
   * Generate a sql script to create a table of type Assoc in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Assoc in the database
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
    buf.append("arid         number(9)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("phase        varchar2(8)          NOT NULL,\n");
    buf.append("belief       float(24)            NOT NULL,\n");
    buf.append("delta        float(24)            NOT NULL,\n");
    buf.append("seaz         float(24)            NOT NULL,\n");
    buf.append("esaz         float(24)            NOT NULL,\n");
    buf.append("timeres      float(24)            NOT NULL,\n");
    buf.append("timedef      varchar2(1)          NOT NULL,\n");
    buf.append("azres        float(24)            NOT NULL,\n");
    buf.append("azdef        varchar2(1)          NOT NULL,\n");
    buf.append("slores       float(24)            NOT NULL,\n");
    buf.append("slodef       varchar2(1)          NOT NULL,\n");
    buf.append("emares       float(24)            NOT NULL,\n");
    buf.append("wgt          float(24)            NOT NULL,\n");
    buf.append("vmodel       varchar2(15)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (arid,orid)");
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
    return (other instanceof Assoc) && ((Assoc) other).arid == arid && ((Assoc) other).orid == orid;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta, chan</I> and <I>time</I>.
   * 
   * @return arid
   */
  public long getArid() {
    return arid;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique <I>sta, chan</I> and <I>time</I>.
   * 
   * @param arid
   * @throws IllegalArgumentException if arid >= 1000000000
   */
  public Assoc setArid(long arid) {
    if (arid >= 1000000000L)
      throw new IllegalArgumentException("arid=" + arid + " but cannot be >= 1000000000");
    this.arid = arid;
    setHash(null);
    return this;
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
  public Assoc setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location recorded in the <B>site</B> table.
   * 
   * @return sta
   */
  public String getSta() {
    return sta;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location recorded in the <B>site</B> table.
   * 
   * @param sta
   * @throws IllegalArgumentException if sta.length() >= 6
   */
  public Assoc setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * Phase type. The identity of a phase that has been associated to an arrival. Standard labels for
   * phases are used (for example, P, PKP, PcP, pP, etc.). Both upper- and lower-case letters are
   * available and should be used when appropriate (for example, pP or PcP).
   * 
   * @return phase
   */
  public String getPhase() {
    return phase;
  }

  /**
   * Phase type. The identity of a phase that has been associated to an arrival. Standard labels for
   * phases are used (for example, P, PKP, PcP, pP, etc.). Both upper- and lower-case letters are
   * available and should be used when appropriate (for example, pP or PcP).
   * 
   * @param phase
   * @throws IllegalArgumentException if phase.length() >= 8
   */
  public Assoc setPhase(String phase) {
    if (phase.length() > 8)
      throw new IllegalArgumentException(
          String.format("phase.length() cannot be > 8.  phase=%s", phase));
    this.phase = phase;
    setHash(null);
    return this;
  }

  /**
   * Phase identification confidence level. This value is a qualitative estimate of the confidence
   * that a seismic phase is correctly identified.
   * 
   * @return belief
   */
  public double getBelief() {
    return belief;
  }

  /**
   * Phase identification confidence level. This value is a qualitative estimate of the confidence
   * that a seismic phase is correctly identified.
   * 
   * @param belief
   */
  public Assoc setBelief(double belief) {
    this.belief = belief;
    setHash(null);
    return this;
  }

  /**
   * Source-receiver distance. This column is the arc length, over the Earth s surface, of the path
   * the seismic phase follows from source to receiver. The location of the origin is specified in
   * the <B>origin</B> record referenced by the column <I>orid</I>. The column <I>arid</I> points to
   * the record in the <B>arrival</B> table that identifies the receiver. The value of the column
   * can exceed 360 degrees. The geographic distance between source and receiver is delta
   * modulo(180).
   * <p>
   * Units: degree
   * 
   * @return delta
   */
  public double getDelta() {
    return delta;
  }

  /**
   * Source-receiver distance. This column is the arc length, over the Earth s surface, of the path
   * the seismic phase follows from source to receiver. The location of the origin is specified in
   * the <B>origin</B> record referenced by the column <I>orid</I>. The column <I>arid</I> points to
   * the record in the <B>arrival</B> table that identifies the receiver. The value of the column
   * can exceed 360 degrees. The geographic distance between source and receiver is delta
   * modulo(180).
   * <p>
   * Units: degree
   * 
   * @param delta
   */
  public Assoc setDelta(double delta) {
    this.delta = delta;
    setHash(null);
    return this;
  }

  /**
   * Station-to-event azimuth calculated from the station and event locations and measured clockwise
   * from north.
   * <p>
   * Units: degree
   * 
   * @return seaz
   */
  public double getSeaz() {
    return seaz;
  }

  /**
   * Station-to-event azimuth calculated from the station and event locations and measured clockwise
   * from north.
   * <p>
   * Units: degree
   * 
   * @param seaz
   */
  public Assoc setSeaz(double seaz) {
    this.seaz = seaz;
    setHash(null);
    return this;
  }

  /**
   * Event to station azimuth measured in degrees clockwise from north.
   * <p>
   * Units: degree
   * 
   * @return esaz
   */
  public double getEsaz() {
    return esaz;
  }

  /**
   * Event to station azimuth measured in degrees clockwise from north.
   * <p>
   * Units: degree
   * 
   * @param esaz
   */
  public Assoc setEsaz(double esaz) {
    this.esaz = esaz;
    setHash(null);
    return this;
  }

  /**
   * Time residual. This column is a travel-time residual measured in seconds. The residual is found
   * by taking the observed arrival time (saved in the <B>arrival</B> table) of a seismic phase and
   * subtracting the expected arrival time. The expected arrival time is calculated by a formula
   * based on an earth velocity model (column <I>vmodel</I>), an event location and origin time
   * (saved in <B>origin</B> table), and the particular seismic phase (column <I>phase</I> in
   * <B>assoc</B> table).
   * <p>
   * Units: s
   * 
   * @return timeres
   */
  public double getTimeres() {
    return timeres;
  }

  /**
   * Time residual. This column is a travel-time residual measured in seconds. The residual is found
   * by taking the observed arrival time (saved in the <B>arrival</B> table) of a seismic phase and
   * subtracting the expected arrival time. The expected arrival time is calculated by a formula
   * based on an earth velocity model (column <I>vmodel</I>), an event location and origin time
   * (saved in <B>origin</B> table), and the particular seismic phase (column <I>phase</I> in
   * <B>assoc</B> table).
   * <p>
   * Units: s
   * 
   * @param timeres
   */
  public Assoc setTimeres(double timeres) {
    this.timeres = timeres;
    setHash(null);
    return this;
  }

  /**
   * Time-defining code. This one-character flag indicates whether or not the time of a phase was
   * used to constrain the event location. This column is defining (<I>timedef</I> = d) or
   * nondefining (<I>timedef</I> = n).
   * 
   * @return timedef
   */
  public String getTimedef() {
    return timedef;
  }

  /**
   * Time-defining code. This one-character flag indicates whether or not the time of a phase was
   * used to constrain the event location. This column is defining (<I>timedef</I> = d) or
   * nondefining (<I>timedef</I> = n).
   * 
   * @param timedef
   * @throws IllegalArgumentException if timedef.length() >= 1
   */
  public Assoc setTimedef(String timedef) {
    if (timedef.length() > 1)
      throw new IllegalArgumentException(
          String.format("timedef.length() cannot be > 1.  timedef=%s", timedef));
    this.timedef = timedef;
    setHash(null);
    return this;
  }

  /**
   * Azimuth residual. This value is the difference between the measured station-to-event azimuth
   * for an arrival and the true azimuth. The true azimuth is the bearing to the inferred event
   * origin.
   * <p>
   * Units: degree
   * 
   * @return azres
   */
  public double getAzres() {
    return azres;
  }

  /**
   * Azimuth residual. This value is the difference between the measured station-to-event azimuth
   * for an arrival and the true azimuth. The true azimuth is the bearing to the inferred event
   * origin.
   * <p>
   * Units: degree
   * 
   * @param azres
   */
  public Assoc setAzres(double azres) {
    this.azres = azres;
    setHash(null);
    return this;
  }

  /**
   * Azimuth-defining code; one-character flag indicates whether or not the azimuth of a phase was
   * used to constrain the event location solution. This column is defining (<I>azdef</I> = d) if it
   * was used in the location, nondefining (<I>azdef</I> = n) if it was not.
   * 
   * @return azdef
   */
  public String getAzdef() {
    return azdef;
  }

  /**
   * Azimuth-defining code; one-character flag indicates whether or not the azimuth of a phase was
   * used to constrain the event location solution. This column is defining (<I>azdef</I> = d) if it
   * was used in the location, nondefining (<I>azdef</I> = n) if it was not.
   * 
   * @param azdef
   * @throws IllegalArgumentException if azdef.length() >= 1
   */
  public Assoc setAzdef(String azdef) {
    if (azdef.length() > 1)
      throw new IllegalArgumentException(
          String.format("azdef.length() cannot be > 1.  azdef=%s", azdef));
    this.azdef = azdef;
    setHash(null);
    return this;
  }

  /**
   * Slowness residual. This column gives the difference between an observed slowness and a
   * theoretical prediction. The prediction is calculated for the related phase and event origin
   * described in the record.
   * <p>
   * Units: s/degree
   * 
   * @return slores
   */
  public double getSlores() {
    return slores;
  }

  /**
   * Slowness residual. This column gives the difference between an observed slowness and a
   * theoretical prediction. The prediction is calculated for the related phase and event origin
   * described in the record.
   * <p>
   * Units: s/degree
   * 
   * @param slores
   */
  public Assoc setSlores(double slores) {
    this.slores = slores;
    setHash(null);
    return this;
  }

  /**
   * Slowness defining code. This one-character flag indicates whether or not the slowness of a
   * phase was used to constrain the event location. This column is defining (<I>slodef</I> = d) or
   * nondefining (<I>slodef</I> = n) for this arrival.
   * 
   * @return slodef
   */
  public String getSlodef() {
    return slodef;
  }

  /**
   * Slowness defining code. This one-character flag indicates whether or not the slowness of a
   * phase was used to constrain the event location. This column is defining (<I>slodef</I> = d) or
   * nondefining (<I>slodef</I> = n) for this arrival.
   * 
   * @param slodef
   * @throws IllegalArgumentException if slodef.length() >= 1
   */
  public Assoc setSlodef(String slodef) {
    if (slodef.length() > 1)
      throw new IllegalArgumentException(
          String.format("slodef.length() cannot be > 1.  slodef=%s", slodef));
    this.slodef = slodef;
    setHash(null);
    return this;
  }

  /**
   * Emergence angle residual. This column is the difference between an observed emergence angle and
   * the theoretical prediction for the same phase, assuming an event location as specified by the
   * accompanying <I>orid</I>.
   * <p>
   * Units: degree
   * 
   * @return emares
   */
  public double getEmares() {
    return emares;
  }

  /**
   * Emergence angle residual. This column is the difference between an observed emergence angle and
   * the theoretical prediction for the same phase, assuming an event location as specified by the
   * accompanying <I>orid</I>.
   * <p>
   * Units: degree
   * 
   * @param emares
   */
  public Assoc setEmares(double emares) {
    this.emares = emares;
    setHash(null);
    return this;
  }

  /**
   * Location weight. This column gives the final weight assigned to the allied arrival by the
   * location program. This column is used primarily for location programs that adaptively weight
   * data by their residuals.
   * 
   * @return wgt
   */
  public double getWgt() {
    return wgt;
  }

  /**
   * Location weight. This column gives the final weight assigned to the allied arrival by the
   * location program. This column is used primarily for location programs that adaptively weight
   * data by their residuals.
   * 
   * @param wgt
   */
  public Assoc setWgt(double wgt) {
    this.wgt = wgt;
    setHash(null);
    return this;
  }

  /**
   * Velocity model. This character string identifies the velocity model of the Earth used to
   * compute the travel times of seismic phases. A velocity model is required for event location (if
   * <I>phase</I> is defining) or for computing travel-time residuals.
   * 
   * @return vmodel
   */
  public String getVmodel() {
    return vmodel;
  }

  /**
   * Velocity model. This character string identifies the velocity model of the Earth used to
   * compute the travel times of seismic phases. A velocity model is required for event location (if
   * <I>phase</I> is defining) or for computing travel-time residuals.
   * 
   * @param vmodel
   * @throws IllegalArgumentException if vmodel.length() >= 15
   */
  public Assoc setVmodel(String vmodel) {
    if (vmodel.length() > 15)
      throw new IllegalArgumentException(
          String.format("vmodel.length() cannot be > 15.  vmodel=%s", vmodel));
    this.vmodel = vmodel;
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
  public Assoc setCommid(long commid) {
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

}
