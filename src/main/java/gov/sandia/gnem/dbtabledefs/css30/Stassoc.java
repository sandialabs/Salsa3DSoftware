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
package gov.sandia.gnem.dbtabledefs.css30;

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
 * stassoc
 */
public class Stassoc extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Station association identification. The wavetrain from a single event may be made up of a
   * number of arrivals. A unique stassid joins those arrivals believed to have come from a common
   * event as measured at a single station. Stassid is also the key to the stassoc relation, which
   * contains addiitional signal measurements not contained within the arrival relation, such as
   * station magnitude estimates and computed signal characteristics.
   */
  private long stassid;

  static final public long STASSID_NA = Long.MIN_VALUE;

  /**
   * Station code. This is the common code-name of a seismic observatory. Generally only three or
   * four characters are used.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Event type. This attribute is used to identify the type of seismic event, when known. For
   * etypes l, r, t the value in origin will be the value determined by the station closest to the
   * event. The recommended codes (all lower case) are: etype code meaning of code qb Quarry blast
   * or mining explosion eq Earthquake me marine explosion ex Other explosion o Other source of
   * known origin l Local event of unknown origin r Regional event of unknown origin t Teleseismic
   * event of unknown origin
   */
  private String etype;

  static final public String ETYPE_NA = "-";

  /**
   * Location description. This character string describes the location of an event identified from
   * data recorded at a single station. Two examples are Fiji-Tonga and Semipalatinsk.
   */
  private String location;

  static final public String LOCATION_NA = "-";

  /**
   * Estimated distance. This attribute gives the approximate source-receiver distance as calculated
   * from slowness (array measurements only), incident angle, or S-P times.
   * <p>
   * Units: degree
   */
  private double dist;

  static final public double DIST_NA = Double.NaN;

  /**
   * Observed azimuth. This is the estimated station-to-event azimuth measured clockwise from north.
   * Azimuth is estimated from f-k or polarization analysis. In stassoc, the value may be an analyst
   * estimate.
   * <p>
   * Units: degree
   */
  private double azimuth;

  static final public double AZIMUTH_NA = Double.NaN;

  /**
   * Latitude. This attribute is the geographic latitude. Locations north of equator have positive
   * latitudes.
   * <p>
   * Units: degree
   */
  private double lat;

  static final public double LAT_NA = Double.NaN;

  /**
   * Longitude. This attribute is the geographic longitude in degrees. Longitudes are measured
   * positive east of the Greenwich meridian.
   * <p>
   * Units: degree
   */
  private double lon;

  static final public double LON_NA = Double.NaN;

  /**
   * Source depth. This attribute gives the depth of the event origin. In stassoc this may be an
   * analyst estimate.
   * <p>
   * Units: km
   */
  private double depth;

  static final public double DEPTH_NA = Double.NaN;

  /**
   * Epoch time. Epoch time given as seconds and fractions of a second since hour 0 January 1, 1970,
   * and stored in a double-precision floating number. Refers to the relation data object with which
   * it is found. E.g., in arrival - arrival time, in origin - origin time; in wfdisc - start time
   * of data. Where date of historical events is known, time is set to the start time of that date;
   * where the date of contemporary arrival measurements is known but no time is given, then the
   * time attribute is set to the NA Value. The double-precision floating point number allows 15
   * decimal digits. At 1 millisecond accuracy, this is a range of 3 *10^4 years. Where time is
   * unknown or prior to Feb. 10, 1653, set to the NA Value.
   * <p>
   * Units: s
   */
  private double time;

  static final public double TIME_NA = Double.NaN;

  /**
   * Initial body wave magnitude. This is an analyst's estimate of the body wave magnitude using
   * data from a single station. See iml, ims, magnitude, magtype, mb, ml and ms.
   * <p>
   * Units: magnitude
   */
  private double imb;

  static final public double IMB_NA = -999.0;

  /**
   * Initial surface wave magnitude. This is an analyst's estimate of the surface wave magnitude
   * using data from a single station. See imb, iml, magnitude, magtype, mb, ml and ms.
   * <p>
   * Units: magnitude
   */
  private double ims;

  static final public double IMS_NA = -999.0;

  /**
   * Initial local magnitude. This is an analyst's estimate of the local magnitude using data from a
   * single station. See imb, ims, magnitude, magtype, mb, ml and ms.
   * <p>
   * Units: magnitude
   */
  private double iml;

  static final public double IML_NA = -999.0;

  /**
   * Author. This records the originator of an arrival (in arrival relation) or origin (in origin
   * relation). Possibilities include externally supplied arrivals identified according to their
   * original source, such as WMO, NEIS, CAN(adian), UK(array), etc. This may also be an identifier
   * of an application generating the attribute, such as an automated interpretation or
   * signal-processing program.
   */
  private String auth;

  static final public String AUTH_NA = "-";

  /**
   * Comment identification. This is a key used to point to free-form comments entered in the remark
   * relation. These comments store additional information about a tuple in another relation. Within
   * the remark relation, there may be many tuples with the same commid and different lineno, but
   * the same commid will appear in only one other tuple among the rest of the relations in the
   * database. See lineno.
   */
  private long commid;

  static final public long COMMID_NA = -1;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("stassid", Columns.FieldType.LONG, "%d");
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("etype", Columns.FieldType.STRING, "%s");
    columns.add("location", Columns.FieldType.STRING, "%s");
    columns.add("dist", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("azimuth", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("lat", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("lon", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("depth", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("imb", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("ims", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("iml", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Stassoc(long stassid, String sta, String etype, String location, double dist,
      double azimuth, double lat, double lon, double depth, double time, double imb, double ims,
      double iml, String auth, long commid) {
    setValues(stassid, sta, etype, location, dist, azimuth, lat, lon, depth, time, imb, ims, iml,
        auth, commid);
  }

  private void setValues(long stassid, String sta, String etype, String location, double dist,
      double azimuth, double lat, double lon, double depth, double time, double imb, double ims,
      double iml, String auth, long commid) {
    this.stassid = stassid;
    this.sta = sta;
    this.etype = etype;
    this.location = location;
    this.dist = dist;
    this.azimuth = azimuth;
    this.lat = lat;
    this.lon = lon;
    this.depth = depth;
    this.time = time;
    this.imb = imb;
    this.ims = ims;
    this.iml = iml;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Stassoc(Stassoc other) {
    this.stassid = other.getStassid();
    this.sta = other.getSta();
    this.etype = other.getEtype();
    this.location = other.getLocation();
    this.dist = other.getDist();
    this.azimuth = other.getAzimuth();
    this.lat = other.getLat();
    this.lon = other.getLon();
    this.depth = other.getDepth();
    this.time = other.getTime();
    this.imb = other.getImb();
    this.ims = other.getIms();
    this.iml = other.getIml();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Stassoc() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(STASSID_NA, STA_NA, ETYPE_NA, LOCATION_NA, DIST_NA, AZIMUTH_NA, LAT_NA, LON_NA,
        DEPTH_NA, TIME_NA, IMB_NA, IMS_NA, IML_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "etype":
        return etype;
      case "location":
        return location;
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
      case "sta":
        sta = value;
        break;
      case "etype":
        etype = value;
        break;
      case "location":
        location = value;
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
      case "dist":
        return dist;
      case "azimuth":
        return azimuth;
      case "lat":
        return lat;
      case "lon":
        return lon;
      case "depth":
        return depth;
      case "time":
        return time;
      case "imb":
        return imb;
      case "ims":
        return ims;
      case "iml":
        return iml;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "dist":
        dist = value;
        break;
      case "azimuth":
        azimuth = value;
        break;
      case "lat":
        lat = value;
        break;
      case "lon":
        lon = value;
        break;
      case "depth":
        depth = value;
        break;
      case "time":
        time = value;
        break;
      case "imb":
        imb = value;
        break;
      case "ims":
        ims = value;
        break;
      case "iml":
        iml = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "stassid":
        return stassid;
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
      case "stassid":
        stassid = value;
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
  public Stassoc(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Stassoc(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), readString(input),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Stassoc(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), readString(input),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Stassoc(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Stassoc(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getString(offset + 14), input.getLong(offset + 15));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[15];
    values[0] = stassid;
    values[1] = sta;
    values[2] = etype;
    values[3] = location;
    values[4] = dist;
    values[5] = azimuth;
    values[6] = lat;
    values[7] = lon;
    values[8] = depth;
    values[9] = time;
    values[10] = imb;
    values[11] = ims;
    values[12] = iml;
    values[13] = auth;
    values[14] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[16];
    values[0] = stassid;
    values[1] = sta;
    values[2] = etype;
    values[3] = location;
    values[4] = dist;
    values[5] = azimuth;
    values[6] = lat;
    values[7] = lon;
    values[8] = depth;
    values[9] = time;
    values[10] = imb;
    values[11] = ims;
    values[12] = iml;
    values[13] = auth;
    values[14] = commid;
    values[15] = lddate;
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
    output.writeLong(stassid);
    writeString(output, sta);
    writeString(output, etype);
    writeString(output, location);
    output.writeDouble(dist);
    output.writeDouble(azimuth);
    output.writeDouble(lat);
    output.writeDouble(lon);
    output.writeDouble(depth);
    output.writeDouble(time);
    output.writeDouble(imb);
    output.writeDouble(ims);
    output.writeDouble(iml);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(stassid);
    writeString(output, sta);
    writeString(output, etype);
    writeString(output, location);
    output.putDouble(dist);
    output.putDouble(azimuth);
    output.putDouble(lat);
    output.putDouble(lon);
    output.putDouble(depth);
    output.putDouble(time);
    output.putDouble(imb);
    output.putDouble(ims);
    output.putDouble(iml);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Stassoc objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Stassoc objects.
   * @throws IOException
   */
  static public void readStassocs(BufferedReader input, Collection<Stassoc> rows)
      throws IOException {
    String[] saved = Stassoc.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Stassoc.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Stassoc(new Scanner(line)));
    }
    input.close();
    Stassoc.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Stassoc objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Stassoc objects.
   * @throws IOException
   */
  static public void readStassocs(File inputFile, Collection<Stassoc> rows) throws IOException {
    readStassocs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Stassoc objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Stassoc objects.
   * @throws IOException
   */
  static public void readStassocs(InputStream inputStream, Collection<Stassoc> rows)
      throws IOException {
    readStassocs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Stassoc objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Stassoc objects
   * @throws IOException
   */
  static public Set<Stassoc> readStassocs(BufferedReader input) throws IOException {
    Set<Stassoc> rows = new LinkedHashSet<Stassoc>();
    readStassocs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Stassoc objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Stassoc objects
   * @throws IOException
   */
  static public Set<Stassoc> readStassocs(File inputFile) throws IOException {
    return readStassocs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Stassoc objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Stassoc objects
   * @throws IOException
   */
  static public Set<Stassoc> readStassocs(InputStream input) throws IOException {
    return readStassocs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Stassoc objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param stassocs the Stassoc objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Stassoc> stassocs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Stassoc stassoc : stassocs)
      stassoc.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Stassoc objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param stassocs the Stassoc objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Stassoc> stassocs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Stassoc stassoc : stassocs) {
        int i = 0;
        statement.setLong(++i, stassoc.stassid);
        statement.setString(++i, stassoc.sta);
        statement.setString(++i, stassoc.etype);
        statement.setString(++i, stassoc.location);
        statement.setDouble(++i, stassoc.dist);
        statement.setDouble(++i, stassoc.azimuth);
        statement.setDouble(++i, stassoc.lat);
        statement.setDouble(++i, stassoc.lon);
        statement.setDouble(++i, stassoc.depth);
        statement.setDouble(++i, stassoc.time);
        statement.setDouble(++i, stassoc.imb);
        statement.setDouble(++i, stassoc.ims);
        statement.setDouble(++i, stassoc.iml);
        statement.setString(++i, stassoc.auth);
        statement.setLong(++i, stassoc.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Stassoc
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Stassoc> readStassocs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Stassoc> results = new HashSet<Stassoc>();
    readStassocs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Stassoc
   *        table.
   * @param stassocs
   * @throws SQLException
   */
  static public void readStassocs(Connection connection, String selectStatement,
      Set<Stassoc> stassocs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        stassocs.add(new Stassoc(rs));
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
   * this Stassoc object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Stassoc object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "stassid, sta, etype, location, dist, azimuth, lat, lon, depth, time, imb, ims, iml, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(stassid)).append(", ");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(etype).append("', ");
    sql.append("'").append(location).append("', ");
    sql.append(Double.toString(dist)).append(", ");
    sql.append(Double.toString(azimuth)).append(", ");
    sql.append(Double.toString(lat)).append(", ");
    sql.append(Double.toString(lon)).append(", ");
    sql.append(Double.toString(depth)).append(", ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Double.toString(imb)).append(", ");
    sql.append(Double.toString(ims)).append(", ");
    sql.append(Double.toString(iml)).append(", ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Stassoc in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Stassoc in the database
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
   * Generate a sql script to create a table of type Stassoc in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Stassoc in the database
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
    buf.append("stassid      number(8)            NOT NULL,\n");
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("etype        varchar2(7)          NOT NULL,\n");
    buf.append("location     varchar2(32)         NOT NULL,\n");
    buf.append("dist         float(24)            NOT NULL,\n");
    buf.append("azimuth      float(24)            NOT NULL,\n");
    buf.append("lat          float(24)            NOT NULL,\n");
    buf.append("lon          float(24)            NOT NULL,\n");
    buf.append("depth        float(24)            NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("imb          float(24)            NOT NULL,\n");
    buf.append("ims          float(24)            NOT NULL,\n");
    buf.append("iml          float(24)            NOT NULL,\n");
    buf.append("auth         varchar2(15)         NOT NULL,\n");
    buf.append("commid       number(8)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (stassid)");
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
    return 164;
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
    return (other instanceof Stassoc) && ((Stassoc) other).stassid == stassid;
  }

  /**
   * Station association identification. The wavetrain from a single event may be made up of a
   * number of arrivals. A unique stassid joins those arrivals believed to have come from a common
   * event as measured at a single station. Stassid is also the key to the stassoc relation, which
   * contains addiitional signal measurements not contained within the arrival relation, such as
   * station magnitude estimates and computed signal characteristics.
   * 
   * @return stassid
   */
  public long getStassid() {
    return stassid;
  }

  /**
   * Station association identification. The wavetrain from a single event may be made up of a
   * number of arrivals. A unique stassid joins those arrivals believed to have come from a common
   * event as measured at a single station. Stassid is also the key to the stassoc relation, which
   * contains addiitional signal measurements not contained within the arrival relation, such as
   * station magnitude estimates and computed signal characteristics.
   * 
   * @param stassid
   * @throws IllegalArgumentException if stassid >= 100000000
   */
  public Stassoc setStassid(long stassid) {
    if (stassid >= 100000000L)
      throw new IllegalArgumentException("stassid=" + stassid + " but cannot be >= 100000000");
    this.stassid = stassid;
    setHash(null);
    return this;
  }

  /**
   * Station code. This is the common code-name of a seismic observatory. Generally only three or
   * four characters are used.
   * 
   * @return sta
   */
  public String getSta() {
    return sta;
  }

  /**
   * Station code. This is the common code-name of a seismic observatory. Generally only three or
   * four characters are used.
   * 
   * @param sta
   * @throws IllegalArgumentException if sta.length() >= 6
   */
  public Stassoc setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * Event type. This attribute is used to identify the type of seismic event, when known. For
   * etypes l, r, t the value in origin will be the value determined by the station closest to the
   * event. The recommended codes (all lower case) are: etype code meaning of code qb Quarry blast
   * or mining explosion eq Earthquake me marine explosion ex Other explosion o Other source of
   * known origin l Local event of unknown origin r Regional event of unknown origin t Teleseismic
   * event of unknown origin
   * 
   * @return etype
   */
  public String getEtype() {
    return etype;
  }

  /**
   * Event type. This attribute is used to identify the type of seismic event, when known. For
   * etypes l, r, t the value in origin will be the value determined by the station closest to the
   * event. The recommended codes (all lower case) are: etype code meaning of code qb Quarry blast
   * or mining explosion eq Earthquake me marine explosion ex Other explosion o Other source of
   * known origin l Local event of unknown origin r Regional event of unknown origin t Teleseismic
   * event of unknown origin
   * 
   * @param etype
   * @throws IllegalArgumentException if etype.length() >= 7
   */
  public Stassoc setEtype(String etype) {
    if (etype.length() > 7)
      throw new IllegalArgumentException(
          String.format("etype.length() cannot be > 7.  etype=%s", etype));
    this.etype = etype;
    setHash(null);
    return this;
  }

  /**
   * Location description. This character string describes the location of an event identified from
   * data recorded at a single station. Two examples are Fiji-Tonga and Semipalatinsk.
   * 
   * @return location
   */
  public String getLocation() {
    return location;
  }

  /**
   * Location description. This character string describes the location of an event identified from
   * data recorded at a single station. Two examples are Fiji-Tonga and Semipalatinsk.
   * 
   * @param location
   * @throws IllegalArgumentException if location.length() >= 32
   */
  public Stassoc setLocation(String location) {
    if (location.length() > 32)
      throw new IllegalArgumentException(
          String.format("location.length() cannot be > 32.  location=%s", location));
    this.location = location;
    setHash(null);
    return this;
  }

  /**
   * Estimated distance. This attribute gives the approximate source-receiver distance as calculated
   * from slowness (array measurements only), incident angle, or S-P times.
   * <p>
   * Units: degree
   * 
   * @return dist
   */
  public double getDist() {
    return dist;
  }

  /**
   * Estimated distance. This attribute gives the approximate source-receiver distance as calculated
   * from slowness (array measurements only), incident angle, or S-P times.
   * <p>
   * Units: degree
   * 
   * @param dist
   */
  public Stassoc setDist(double dist) {
    this.dist = dist;
    setHash(null);
    return this;
  }

  /**
   * Observed azimuth. This is the estimated station-to-event azimuth measured clockwise from north.
   * Azimuth is estimated from f-k or polarization analysis. In stassoc, the value may be an analyst
   * estimate.
   * <p>
   * Units: degree
   * 
   * @return azimuth
   */
  public double getAzimuth() {
    return azimuth;
  }

  /**
   * Observed azimuth. This is the estimated station-to-event azimuth measured clockwise from north.
   * Azimuth is estimated from f-k or polarization analysis. In stassoc, the value may be an analyst
   * estimate.
   * <p>
   * Units: degree
   * 
   * @param azimuth
   */
  public Stassoc setAzimuth(double azimuth) {
    this.azimuth = azimuth;
    setHash(null);
    return this;
  }

  /**
   * Latitude. This attribute is the geographic latitude. Locations north of equator have positive
   * latitudes.
   * <p>
   * Units: degree
   * 
   * @return lat
   */
  public double getLat() {
    return lat;
  }

  /**
   * Latitude. This attribute is the geographic latitude. Locations north of equator have positive
   * latitudes.
   * <p>
   * Units: degree
   * 
   * @param lat
   */
  public Stassoc setLat(double lat) {
    this.lat = lat;
    setHash(null);
    return this;
  }

  /**
   * Longitude. This attribute is the geographic longitude in degrees. Longitudes are measured
   * positive east of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @return lon
   */
  public double getLon() {
    return lon;
  }

  /**
   * Longitude. This attribute is the geographic longitude in degrees. Longitudes are measured
   * positive east of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @param lon
   */
  public Stassoc setLon(double lon) {
    this.lon = lon;
    setHash(null);
    return this;
  }

  /**
   * Source depth. This attribute gives the depth of the event origin. In stassoc this may be an
   * analyst estimate.
   * <p>
   * Units: km
   * 
   * @return depth
   */
  public double getDepth() {
    return depth;
  }

  /**
   * Source depth. This attribute gives the depth of the event origin. In stassoc this may be an
   * analyst estimate.
   * <p>
   * Units: km
   * 
   * @param depth
   */
  public Stassoc setDepth(double depth) {
    this.depth = depth;
    setHash(null);
    return this;
  }

  /**
   * Epoch time. Epoch time given as seconds and fractions of a second since hour 0 January 1, 1970,
   * and stored in a double-precision floating number. Refers to the relation data object with which
   * it is found. E.g., in arrival - arrival time, in origin - origin time; in wfdisc - start time
   * of data. Where date of historical events is known, time is set to the start time of that date;
   * where the date of contemporary arrival measurements is known but no time is given, then the
   * time attribute is set to the NA Value. The double-precision floating point number allows 15
   * decimal digits. At 1 millisecond accuracy, this is a range of 3 *10^4 years. Where time is
   * unknown or prior to Feb. 10, 1653, set to the NA Value.
   * <p>
   * Units: s
   * 
   * @return time
   */
  public double getTime() {
    return time;
  }

  /**
   * Epoch time. Epoch time given as seconds and fractions of a second since hour 0 January 1, 1970,
   * and stored in a double-precision floating number. Refers to the relation data object with which
   * it is found. E.g., in arrival - arrival time, in origin - origin time; in wfdisc - start time
   * of data. Where date of historical events is known, time is set to the start time of that date;
   * where the date of contemporary arrival measurements is known but no time is given, then the
   * time attribute is set to the NA Value. The double-precision floating point number allows 15
   * decimal digits. At 1 millisecond accuracy, this is a range of 3 *10^4 years. Where time is
   * unknown or prior to Feb. 10, 1653, set to the NA Value.
   * <p>
   * Units: s
   * 
   * @param time
   */
  public Stassoc setTime(double time) {
    this.time = time;
    setHash(null);
    return this;
  }

  /**
   * Initial body wave magnitude. This is an analyst's estimate of the body wave magnitude using
   * data from a single station. See iml, ims, magnitude, magtype, mb, ml and ms.
   * <p>
   * Units: magnitude
   * 
   * @return imb
   */
  public double getImb() {
    return imb;
  }

  /**
   * Initial body wave magnitude. This is an analyst's estimate of the body wave magnitude using
   * data from a single station. See iml, ims, magnitude, magtype, mb, ml and ms.
   * <p>
   * Units: magnitude
   * 
   * @param imb
   */
  public Stassoc setImb(double imb) {
    this.imb = imb;
    setHash(null);
    return this;
  }

  /**
   * Initial surface wave magnitude. This is an analyst's estimate of the surface wave magnitude
   * using data from a single station. See imb, iml, magnitude, magtype, mb, ml and ms.
   * <p>
   * Units: magnitude
   * 
   * @return ims
   */
  public double getIms() {
    return ims;
  }

  /**
   * Initial surface wave magnitude. This is an analyst's estimate of the surface wave magnitude
   * using data from a single station. See imb, iml, magnitude, magtype, mb, ml and ms.
   * <p>
   * Units: magnitude
   * 
   * @param ims
   */
  public Stassoc setIms(double ims) {
    this.ims = ims;
    setHash(null);
    return this;
  }

  /**
   * Initial local magnitude. This is an analyst's estimate of the local magnitude using data from a
   * single station. See imb, ims, magnitude, magtype, mb, ml and ms.
   * <p>
   * Units: magnitude
   * 
   * @return iml
   */
  public double getIml() {
    return iml;
  }

  /**
   * Initial local magnitude. This is an analyst's estimate of the local magnitude using data from a
   * single station. See imb, ims, magnitude, magtype, mb, ml and ms.
   * <p>
   * Units: magnitude
   * 
   * @param iml
   */
  public Stassoc setIml(double iml) {
    this.iml = iml;
    setHash(null);
    return this;
  }

  /**
   * Author. This records the originator of an arrival (in arrival relation) or origin (in origin
   * relation). Possibilities include externally supplied arrivals identified according to their
   * original source, such as WMO, NEIS, CAN(adian), UK(array), etc. This may also be an identifier
   * of an application generating the attribute, such as an automated interpretation or
   * signal-processing program.
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * Author. This records the originator of an arrival (in arrival relation) or origin (in origin
   * relation). Possibilities include externally supplied arrivals identified according to their
   * original source, such as WMO, NEIS, CAN(adian), UK(array), etc. This may also be an identifier
   * of an application generating the attribute, such as an automated interpretation or
   * signal-processing program.
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 15
   */
  public Stassoc setAuth(String auth) {
    if (auth.length() > 15)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 15.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * Comment identification. This is a key used to point to free-form comments entered in the remark
   * relation. These comments store additional information about a tuple in another relation. Within
   * the remark relation, there may be many tuples with the same commid and different lineno, but
   * the same commid will appear in only one other tuple among the rest of the relations in the
   * database. See lineno.
   * 
   * @return commid
   */
  public long getCommid() {
    return commid;
  }

  /**
   * Comment identification. This is a key used to point to free-form comments entered in the remark
   * relation. These comments store additional information about a tuple in another relation. Within
   * the remark relation, there may be many tuples with the same commid and different lineno, but
   * the same commid will appear in only one other tuple among the rest of the relations in the
   * database. See lineno.
   * 
   * @param commid
   * @throws IllegalArgumentException if commid >= 100000000
   */
  public Stassoc setCommid(long commid) {
    if (commid >= 100000000L)
      throw new IllegalArgumentException("commid=" + commid + " but cannot be >= 100000000");
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
    return "CSS3.0";
  }

}
