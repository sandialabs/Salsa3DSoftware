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
 * search_link
 */
public class Search_link extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   */
  private long evid;

  static final public long EVID_NA = -1;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = -1;

  /**
   * Event latitude.
   * <p>
   * Units: degree
   */
  private double olat;

  static final public double OLAT_NA = -999;

  /**
   * Event longitude.
   * <p>
   * Units: degree
   */
  private double olon;

  static final public double OLON_NA = -999;

  /**
   * Event depth
   * <p>
   * Units: km
   */
  private double depth;

  static final public double DEPTH_NA = -999;

  /**
   * Body wave magnitude
   */
  private double mb;

  static final public double MB_NA = -999;

  /**
   * Surface wave magnitude.
   */
  private double ms;

  static final public double MS_NA = -999;

  /**
   * Local magnitude.
   */
  private double ml;

  static final public double ML_NA = -999;

  /**
   * Epoch time
   * <p>
   * Units: s
   */
  private double time;

  static final public double TIME_NA = -9999999999.999;

  /**
   * Julian date of origin.
   */
  private long jdate;

  static final public long JDATE_NA = -1;

  /**
   * Type of seismic event, when known. The following etypes are defined: ex generic explosion ec
   * chemical explosion ep probable explosion en nuclear explosion mc collapse me coal bump/mining
   * event mp probable mining event mb rock burst qt generic earthquake/tectonic qd damaging
   * earthquake qp unknown-probable earthquake qf felt earthquake qm multiple shocks qh quake with
   * associated Harmonic Tremor qv long period event e.g. slow earthquake q2 double shock q4
   * foreshock qa aftershock ge geyser xm meteoritic origin xl lights xo odors - unknown
   */
  private String etype;

  static final public String ETYPE_NA = "-";

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
   * Station latitude.
   * <p>
   * Units: degree
   */
  private double slat;

  static final public double SLAT_NA = -999;

  /**
   * Station longitude.
   * <p>
   * Units: degree
   */
  private double slon;

  static final public double SLON_NA = -999;

  /**
   * Station to event distance
   * <p>
   * Units: degree
   */
  private double degdist;

  static final public double DEGDIST_NA = -1;

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
   * Waveform identifier of the seismogram from which measurement was made. See <B>wfdisc</B>.
   */
  private long wfid;

  static final public long WFID_NA = -1;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("evid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("olat", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("olon", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("depth", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("mb", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("ms", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("ml", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("jdate", Columns.FieldType.LONG, "%d");
    columns.add("etype", Columns.FieldType.STRING, "%s");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("slat", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("slon", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("degdist", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("seaz", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("esaz", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("wfid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Search_link(long evid, long orid, double olat, double olon, double depth, double mb,
      double ms, double ml, double time, long jdate, String etype, String sta, String chan,
      double slat, double slon, double degdist, double seaz, double esaz, long wfid) {
    setValues(evid, orid, olat, olon, depth, mb, ms, ml, time, jdate, etype, sta, chan, slat, slon,
        degdist, seaz, esaz, wfid);
  }

  private void setValues(long evid, long orid, double olat, double olon, double depth, double mb,
      double ms, double ml, double time, long jdate, String etype, String sta, String chan,
      double slat, double slon, double degdist, double seaz, double esaz, long wfid) {
    this.evid = evid;
    this.orid = orid;
    this.olat = olat;
    this.olon = olon;
    this.depth = depth;
    this.mb = mb;
    this.ms = ms;
    this.ml = ml;
    this.time = time;
    this.jdate = jdate;
    this.etype = etype;
    this.sta = sta;
    this.chan = chan;
    this.slat = slat;
    this.slon = slon;
    this.degdist = degdist;
    this.seaz = seaz;
    this.esaz = esaz;
    this.wfid = wfid;
  }

  /**
   * Copy constructor.
   */
  public Search_link(Search_link other) {
    this.evid = other.getEvid();
    this.orid = other.getOrid();
    this.olat = other.getOlat();
    this.olon = other.getOlon();
    this.depth = other.getDepth();
    this.mb = other.getMb();
    this.ms = other.getMs();
    this.ml = other.getMl();
    this.time = other.getTime();
    this.jdate = other.getJdate();
    this.etype = other.getEtype();
    this.sta = other.getSta();
    this.chan = other.getChan();
    this.slat = other.getSlat();
    this.slon = other.getSlon();
    this.degdist = other.getDegdist();
    this.seaz = other.getSeaz();
    this.esaz = other.getEsaz();
    this.wfid = other.getWfid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Search_link() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(EVID_NA, ORID_NA, OLAT_NA, OLON_NA, DEPTH_NA, MB_NA, MS_NA, ML_NA, TIME_NA, JDATE_NA,
        ETYPE_NA, STA_NA, CHAN_NA, SLAT_NA, SLON_NA, DEGDIST_NA, SEAZ_NA, ESAZ_NA, WFID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "etype":
        return etype;
      case "sta":
        return sta;
      case "chan":
        return chan;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "etype":
        etype = value;
        break;
      case "sta":
        sta = value;
        break;
      case "chan":
        chan = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "olat":
        return olat;
      case "olon":
        return olon;
      case "depth":
        return depth;
      case "mb":
        return mb;
      case "ms":
        return ms;
      case "ml":
        return ml;
      case "time":
        return time;
      case "slat":
        return slat;
      case "slon":
        return slon;
      case "degdist":
        return degdist;
      case "seaz":
        return seaz;
      case "esaz":
        return esaz;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "olat":
        olat = value;
        break;
      case "olon":
        olon = value;
        break;
      case "depth":
        depth = value;
        break;
      case "mb":
        mb = value;
        break;
      case "ms":
        ms = value;
        break;
      case "ml":
        ml = value;
        break;
      case "time":
        time = value;
        break;
      case "slat":
        slat = value;
        break;
      case "slon":
        slon = value;
        break;
      case "degdist":
        degdist = value;
        break;
      case "seaz":
        seaz = value;
        break;
      case "esaz":
        esaz = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "evid":
        return evid;
      case "orid":
        return orid;
      case "jdate":
        return jdate;
      case "wfid":
        return wfid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "evid":
        evid = value;
        break;
      case "orid":
        orid = value;
        break;
      case "jdate":
        jdate = value;
        break;
      case "wfid":
        wfid = value;
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
  public Search_link(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Search_link(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readLong(), readString(input), readString(input),
        readString(input), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Search_link(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(), input.getLong(),
        readString(input), readString(input), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Search_link(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Search_link(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getLong(offset + 10), input.getString(offset + 11), input.getString(offset + 12),
        input.getString(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getDouble(offset + 17), input.getDouble(offset + 18),
        input.getLong(offset + 19));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[19];
    values[0] = evid;
    values[1] = orid;
    values[2] = olat;
    values[3] = olon;
    values[4] = depth;
    values[5] = mb;
    values[6] = ms;
    values[7] = ml;
    values[8] = time;
    values[9] = jdate;
    values[10] = etype;
    values[11] = sta;
    values[12] = chan;
    values[13] = slat;
    values[14] = slon;
    values[15] = degdist;
    values[16] = seaz;
    values[17] = esaz;
    values[18] = wfid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[20];
    values[0] = evid;
    values[1] = orid;
    values[2] = olat;
    values[3] = olon;
    values[4] = depth;
    values[5] = mb;
    values[6] = ms;
    values[7] = ml;
    values[8] = time;
    values[9] = jdate;
    values[10] = etype;
    values[11] = sta;
    values[12] = chan;
    values[13] = slat;
    values[14] = slon;
    values[15] = degdist;
    values[16] = seaz;
    values[17] = esaz;
    values[18] = wfid;
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
    output.writeLong(evid);
    output.writeLong(orid);
    output.writeDouble(olat);
    output.writeDouble(olon);
    output.writeDouble(depth);
    output.writeDouble(mb);
    output.writeDouble(ms);
    output.writeDouble(ml);
    output.writeDouble(time);
    output.writeLong(jdate);
    writeString(output, etype);
    writeString(output, sta);
    writeString(output, chan);
    output.writeDouble(slat);
    output.writeDouble(slon);
    output.writeDouble(degdist);
    output.writeDouble(seaz);
    output.writeDouble(esaz);
    output.writeLong(wfid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(evid);
    output.putLong(orid);
    output.putDouble(olat);
    output.putDouble(olon);
    output.putDouble(depth);
    output.putDouble(mb);
    output.putDouble(ms);
    output.putDouble(ml);
    output.putDouble(time);
    output.putLong(jdate);
    writeString(output, etype);
    writeString(output, sta);
    writeString(output, chan);
    output.putDouble(slat);
    output.putDouble(slon);
    output.putDouble(degdist);
    output.putDouble(seaz);
    output.putDouble(esaz);
    output.putLong(wfid);
  }

  /**
   * Read a Collection of Search_link objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Search_link objects.
   * @throws IOException
   */
  static public void readSearch_links(BufferedReader input, Collection<Search_link> rows)
      throws IOException {
    String[] saved = Search_link.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Search_link
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Search_link(new Scanner(line)));
    }
    input.close();
    Search_link.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Search_link objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Search_link objects.
   * @throws IOException
   */
  static public void readSearch_links(File inputFile, Collection<Search_link> rows)
      throws IOException {
    readSearch_links(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Search_link objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Search_link objects.
   * @throws IOException
   */
  static public void readSearch_links(InputStream inputStream, Collection<Search_link> rows)
      throws IOException {
    readSearch_links(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Search_link objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Search_link objects
   * @throws IOException
   */
  static public Set<Search_link> readSearch_links(BufferedReader input) throws IOException {
    Set<Search_link> rows = new LinkedHashSet<Search_link>();
    readSearch_links(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Search_link objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Search_link objects
   * @throws IOException
   */
  static public Set<Search_link> readSearch_links(File inputFile) throws IOException {
    return readSearch_links(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Search_link objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Search_link objects
   * @throws IOException
   */
  static public Set<Search_link> readSearch_links(InputStream input) throws IOException {
    return readSearch_links(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Search_link objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param search_links the Search_link objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Search_link> search_links)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Search_link search_link : search_links)
      search_link.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Search_link objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param search_links the Search_link objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Search_link> search_links, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Search_link search_link : search_links) {
        int i = 0;
        statement.setLong(++i, search_link.evid);
        statement.setLong(++i, search_link.orid);
        statement.setDouble(++i, search_link.olat);
        statement.setDouble(++i, search_link.olon);
        statement.setDouble(++i, search_link.depth);
        statement.setDouble(++i, search_link.mb);
        statement.setDouble(++i, search_link.ms);
        statement.setDouble(++i, search_link.ml);
        statement.setDouble(++i, search_link.time);
        statement.setLong(++i, search_link.jdate);
        statement.setString(++i, search_link.etype);
        statement.setString(++i, search_link.sta);
        statement.setString(++i, search_link.chan);
        statement.setDouble(++i, search_link.slat);
        statement.setDouble(++i, search_link.slon);
        statement.setDouble(++i, search_link.degdist);
        statement.setDouble(++i, search_link.seaz);
        statement.setDouble(++i, search_link.esaz);
        statement.setLong(++i, search_link.wfid);
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
   *        Search_link table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Search_link> readSearch_links(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Search_link> results = new HashSet<Search_link>();
    readSearch_links(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Search_link table.
   * @param search_links
   * @throws SQLException
   */
  static public void readSearch_links(Connection connection, String selectStatement,
      Set<Search_link> search_links) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        search_links.add(new Search_link(rs));
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
   * this Search_link object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Search_link object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "evid, orid, olat, olon, depth, mb, ms, ml, time, jdate, etype, sta, chan, slat, slon, degdist, seaz, esaz, wfid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(evid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Double.toString(olat)).append(", ");
    sql.append(Double.toString(olon)).append(", ");
    sql.append(Double.toString(depth)).append(", ");
    sql.append(Double.toString(mb)).append(", ");
    sql.append(Double.toString(ms)).append(", ");
    sql.append(Double.toString(ml)).append(", ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Long.toString(jdate)).append(", ");
    sql.append("'").append(etype).append("', ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(chan).append("', ");
    sql.append(Double.toString(slat)).append(", ");
    sql.append(Double.toString(slon)).append(", ");
    sql.append(Double.toString(degdist)).append(", ");
    sql.append(Double.toString(seaz)).append(", ");
    sql.append(Double.toString(esaz)).append(", ");
    sql.append(Long.toString(wfid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Search_link in the database. Primary and unique keys are set, if
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
   * Create a table of type Search_link in the database
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
   * Generate a sql script to create a table of type Search_link in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Search_link in the database
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
    buf.append("evid         number(9)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("olat         float(24)            NOT NULL,\n");
    buf.append("olon         float(24)            NOT NULL,\n");
    buf.append("depth        float(24)            NOT NULL,\n");
    buf.append("mb           float(24)            NOT NULL,\n");
    buf.append("ms           float(24)            NOT NULL,\n");
    buf.append("ml           float(24)            NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("jdate        number(8)            NOT NULL,\n");
    buf.append("etype        varchar2(7)          NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("slat         float(24)            NOT NULL,\n");
    buf.append("slon         float(24)            NOT NULL,\n");
    buf.append("degdist      float(24)            NOT NULL,\n");
    buf.append("seaz         float(24)            NOT NULL,\n");
    buf.append("esaz         float(24)            NOT NULL,\n");
    buf.append("wfid         number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (wfid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (orid,sta,chan)");
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
    return 161;
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
    return (other instanceof Search_link) && ((Search_link) other).wfid == wfid;
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
    return (other instanceof Search_link) && ((Search_link) other).orid == orid
        && ((Search_link) other).sta.equals(sta) && ((Search_link) other).chan.equals(chan);
  }

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   * 
   * @return evid
   */
  public long getEvid() {
    return evid;
  }

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   * 
   * @param evid
   * @throws IllegalArgumentException if evid >= 1000000000
   */
  public Search_link setEvid(long evid) {
    if (evid >= 1000000000L)
      throw new IllegalArgumentException("evid=" + evid + " but cannot be >= 1000000000");
    this.evid = evid;
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
  public Search_link setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Event latitude.
   * <p>
   * Units: degree
   * 
   * @return olat
   */
  public double getOlat() {
    return olat;
  }

  /**
   * Event latitude.
   * <p>
   * Units: degree
   * 
   * @param olat
   */
  public Search_link setOlat(double olat) {
    this.olat = olat;
    setHash(null);
    return this;
  }

  /**
   * Event longitude.
   * <p>
   * Units: degree
   * 
   * @return olon
   */
  public double getOlon() {
    return olon;
  }

  /**
   * Event longitude.
   * <p>
   * Units: degree
   * 
   * @param olon
   */
  public Search_link setOlon(double olon) {
    this.olon = olon;
    setHash(null);
    return this;
  }

  /**
   * Event depth
   * <p>
   * Units: km
   * 
   * @return depth
   */
  public double getDepth() {
    return depth;
  }

  /**
   * Event depth
   * <p>
   * Units: km
   * 
   * @param depth
   */
  public Search_link setDepth(double depth) {
    this.depth = depth;
    setHash(null);
    return this;
  }

  /**
   * Body wave magnitude
   * 
   * @return mb
   */
  public double getMb() {
    return mb;
  }

  /**
   * Body wave magnitude
   * 
   * @param mb
   */
  public Search_link setMb(double mb) {
    this.mb = mb;
    setHash(null);
    return this;
  }

  /**
   * Surface wave magnitude.
   * 
   * @return ms
   */
  public double getMs() {
    return ms;
  }

  /**
   * Surface wave magnitude.
   * 
   * @param ms
   */
  public Search_link setMs(double ms) {
    this.ms = ms;
    setHash(null);
    return this;
  }

  /**
   * Local magnitude.
   * 
   * @return ml
   */
  public double getMl() {
    return ml;
  }

  /**
   * Local magnitude.
   * 
   * @param ml
   */
  public Search_link setMl(double ml) {
    this.ml = ml;
    setHash(null);
    return this;
  }

  /**
   * Epoch time
   * <p>
   * Units: s
   * 
   * @return time
   */
  public double getTime() {
    return time;
  }

  /**
   * Epoch time
   * <p>
   * Units: s
   * 
   * @param time
   */
  public Search_link setTime(double time) {
    this.time = time;
    setHash(null);
    return this;
  }

  /**
   * Julian date of origin.
   * 
   * @return jdate
   */
  public long getJdate() {
    return jdate;
  }

  /**
   * Julian date of origin.
   * 
   * @param jdate
   * @throws IllegalArgumentException if jdate >= 100000000
   */
  public Search_link setJdate(long jdate) {
    if (jdate >= 100000000L)
      throw new IllegalArgumentException("jdate=" + jdate + " but cannot be >= 100000000");
    this.jdate = jdate;
    setHash(null);
    return this;
  }

  /**
   * Type of seismic event, when known. The following etypes are defined: ex generic explosion ec
   * chemical explosion ep probable explosion en nuclear explosion mc collapse me coal bump/mining
   * event mp probable mining event mb rock burst qt generic earthquake/tectonic qd damaging
   * earthquake qp unknown-probable earthquake qf felt earthquake qm multiple shocks qh quake with
   * associated Harmonic Tremor qv long period event e.g. slow earthquake q2 double shock q4
   * foreshock qa aftershock ge geyser xm meteoritic origin xl lights xo odors - unknown
   * 
   * @return etype
   */
  public String getEtype() {
    return etype;
  }

  /**
   * Type of seismic event, when known. The following etypes are defined: ex generic explosion ec
   * chemical explosion ep probable explosion en nuclear explosion mc collapse me coal bump/mining
   * event mp probable mining event mb rock burst qt generic earthquake/tectonic qd damaging
   * earthquake qp unknown-probable earthquake qf felt earthquake qm multiple shocks qh quake with
   * associated Harmonic Tremor qv long period event e.g. slow earthquake q2 double shock q4
   * foreshock qa aftershock ge geyser xm meteoritic origin xl lights xo odors - unknown
   * 
   * @param etype
   * @throws IllegalArgumentException if etype.length() >= 7
   */
  public Search_link setEtype(String etype) {
    if (etype.length() > 7)
      throw new IllegalArgumentException(
          String.format("etype.length() cannot be > 7.  etype=%s", etype));
    this.etype = etype;
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
  public Search_link setSta(String sta) {
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
  public Search_link setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
    setHash(null);
    return this;
  }

  /**
   * Station latitude.
   * <p>
   * Units: degree
   * 
   * @return slat
   */
  public double getSlat() {
    return slat;
  }

  /**
   * Station latitude.
   * <p>
   * Units: degree
   * 
   * @param slat
   */
  public Search_link setSlat(double slat) {
    this.slat = slat;
    setHash(null);
    return this;
  }

  /**
   * Station longitude.
   * <p>
   * Units: degree
   * 
   * @return slon
   */
  public double getSlon() {
    return slon;
  }

  /**
   * Station longitude.
   * <p>
   * Units: degree
   * 
   * @param slon
   */
  public Search_link setSlon(double slon) {
    this.slon = slon;
    setHash(null);
    return this;
  }

  /**
   * Station to event distance
   * <p>
   * Units: degree
   * 
   * @return degdist
   */
  public double getDegdist() {
    return degdist;
  }

  /**
   * Station to event distance
   * <p>
   * Units: degree
   * 
   * @param degdist
   */
  public Search_link setDegdist(double degdist) {
    this.degdist = degdist;
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
  public Search_link setSeaz(double seaz) {
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
  public Search_link setEsaz(double esaz) {
    this.esaz = esaz;
    setHash(null);
    return this;
  }

  /**
   * Waveform identifier of the seismogram from which measurement was made. See <B>wfdisc</B>.
   * 
   * @return wfid
   */
  public long getWfid() {
    return wfid;
  }

  /**
   * Waveform identifier of the seismogram from which measurement was made. See <B>wfdisc</B>.
   * 
   * @param wfid
   * @throws IllegalArgumentException if wfid >= 1000000000
   */
  public Search_link setWfid(long wfid) {
    if (wfid >= 1000000000L)
      throw new IllegalArgumentException("wfid=" + wfid + " but cannot be >= 1000000000");
    this.wfid = wfid;
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
