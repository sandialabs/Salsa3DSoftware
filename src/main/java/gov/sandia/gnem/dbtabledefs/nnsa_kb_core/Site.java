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

import gov.sandia.gmp.util.globals.SiteInterface;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * site
 */
public class Site extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location recorded in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Turn on date. Date on which the station, or sensor indicated began operating. The columns
   * offdate and ondate are not intended to accommodate temporary downtimes, but rather to indicate
   * the time period for which the columns of the station (<I>lat</I>, <I>lon</I>, <I>elev</I>,) are
   * valid for the given station code. Stations are often moved, but with the station code remaining
   * unchanged.
   */
  private long ondate;

  static final public long ONDATE_NA = -1;

  /**
   * Turn off date. This column is the Julian Date on which the station or sensor indicated was
   * turned off, dismantled, or moved (see <I>ondate</I>)
   */
  private long offdate;

  static final public long OFFDATE_NA = 2286324;

  /**
   * Geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   */
  private double lat;

  static final public double LAT_NA = -999;

  /**
   * Geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   */
  private double lon;

  static final public double LON_NA = -999;

  /**
   * Surface elevation. This column is the elevation of the surface of the earth above the seismic
   * station (<B>site</B>) relative to mean sea level
   * <p>
   * Units: km
   */
  private double elev;

  static final public double ELEV_NA = -999;

  /**
   * Station name/Description. This value is the full name of the station whose code name is in
   * <I>sta</I> [for example, one record in the <B>site</B> table connects <I>sta</I> = ANMO to
   * staname = ALBUQUERQUE, NEW MEXICO (SRO)].
   */
  private String staname;

  static final public String STANAME_NA = "-";

  /**
   * Station type; character string specifies the station type. Recommended entries are single
   * station (ss) or array (ar).
   */
  private String statype;

  static final public String STATYPE_NA = "-";

  /**
   * Reference station. This string specifies the reference station with respect to which array
   * members are located (see <I>deast</I>, <I>dnorth</I>).
   */
  private String refsta;

  static final public String REFSTA_NA = "-";

  /**
   * Distance North. This column gives the northing or relative position of array element North of
   * the array center specified by the value of <I>refsta</I> (see <I>deast</I>).
   * <p>
   * Units: km
   */
  private double dnorth;

  static final public double DNORTH_NA = 0.0;

  /**
   * Distance East. This column gives the easting or the relative position of an array element East
   * of the location of the array center specified by the value of <I>refsta</I> (see
   * <I>dnorth</I>).
   * <p>
   * Units: km
   */
  private double deast;

  static final public double DEAST_NA = 0.0;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("ondate", Columns.FieldType.LONG, "%d");
    columns.add("offdate", Columns.FieldType.LONG, "%d");
    columns.add("lat", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("lon", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("elev", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("staname", Columns.FieldType.STRING, "%s");
    columns.add("statype", Columns.FieldType.STRING, "%s");
    columns.add("refsta", Columns.FieldType.STRING, "%s");
    columns.add("dnorth", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("deast", Columns.FieldType.DOUBLE, "%1.4f");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Site(String sta, long ondate, long offdate, double lat, double lon, double elev,
	  String staname, String statype, String refsta, double dnorth, double deast) {
      setValues(sta, ondate, offdate, lat, lon, elev, staname, statype, refsta, dnorth, deast);
  }

  /**
   * Parameterized constructor. Populates all values with specified values.
   * Splits line on tab character.  Expects 11 tokens.
   * For space-delimited strings see Site(Scanner)
   */
  public Site(String line) {
      this(SiteInterface.parseSite(line));
  }

  /**
   * Parameterized constructor. Populates all values with specified values.
   * Parameter s must have 11 elements: sta, ondate, offdate, lat, lon, elev,
   * staname, statype, refsta, dnorth, deast
   */
  public Site(String[] s) {
      this(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10]);
  }

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Site(String sta, String ondate, String offdate, String lat, String lon,
	  String elev, String staname, String statype, String refsta,
	  String dnorth, String deast) {
      this.sta = sta.trim();
      this.ondate = Long.parseLong(ondate.trim());
      this.offdate = Long.parseLong(offdate.trim());
      this.lat = Double.parseDouble(lat.trim());
      this.lon = Double.parseDouble(lon.trim());
      this.elev = Double.parseDouble(elev.trim());
      this.staname = staname.trim();
      this.statype = statype.trim();
      this.refsta = refsta.trim();
      this.dnorth = Double.parseDouble(dnorth.trim());
      this.deast = Double.parseDouble(deast.trim());
  }

  private void setValues(String sta, long ondate, long offdate, double lat, double lon, double elev,
      String staname, String statype, String refsta, double dnorth, double deast) {
    this.sta = sta;
    this.ondate = ondate;
    this.offdate = offdate;
    this.lat = lat;
    this.lon = lon;
    this.elev = elev;
    this.staname = staname;
    this.statype = statype;
    this.refsta = refsta;
    this.dnorth = dnorth;
    this.deast = deast;
  }

  /**
   * Copy constructor.
   */
  public Site(Site other) {
    this.sta = other.getSta();
    this.ondate = other.getOndate();
    this.offdate = other.getOffdate();
    this.lat = other.getLat();
    this.lon = other.getLon();
    this.elev = other.getElev();
    this.staname = other.getStaname();
    this.statype = other.getStatype();
    this.refsta = other.getRefsta();
    this.dnorth = other.getDnorth();
    this.deast = other.getDeast();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Site() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(STA_NA, ONDATE_NA, OFFDATE_NA, LAT_NA, LON_NA, ELEV_NA, STANAME_NA, STATYPE_NA,
        REFSTA_NA, DNORTH_NA, DEAST_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "staname":
        return staname;
      case "statype":
        return statype;
      case "refsta":
        return refsta;
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
      case "staname":
        staname = value;
        break;
      case "statype":
        statype = value;
        break;
      case "refsta":
        refsta = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "lat":
        return lat;
      case "lon":
        return lon;
      case "elev":
        return elev;
      case "dnorth":
        return dnorth;
      case "deast":
        return deast;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "lat":
        lat = value;
        break;
      case "lon":
        lon = value;
        break;
      case "elev":
        elev = value;
        break;
      case "dnorth":
        dnorth = value;
        break;
      case "deast":
        deast = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "ondate":
        return ondate;
      case "offdate":
        return offdate;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "ondate":
        ondate = value;
        break;
      case "offdate":
        offdate = value;
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
  public Site(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Site(DataInputStream input) throws IOException {
    this(readString(input), input.readLong(), input.readLong(), input.readDouble(),
        input.readDouble(), input.readDouble(), readString(input), readString(input),
        readString(input), input.readDouble(), input.readDouble());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Site(ByteBuffer input) {
    this(readString(input), input.getLong(), input.getLong(), input.getDouble(), input.getDouble(),
        input.getDouble(), readString(input), readString(input), readString(input),
        input.getDouble(), input.getDouble());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Site(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Site(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getString(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[11];
    values[0] = sta;
    values[1] = ondate;
    values[2] = offdate;
    values[3] = lat;
    values[4] = lon;
    values[5] = elev;
    values[6] = staname;
    values[7] = statype;
    values[8] = refsta;
    values[9] = dnorth;
    values[10] = deast;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[12];
    values[0] = sta;
    values[1] = ondate;
    values[2] = offdate;
    values[3] = lat;
    values[4] = lon;
    values[5] = elev;
    values[6] = staname;
    values[7] = statype;
    values[8] = refsta;
    values[9] = dnorth;
    values[10] = deast;
    values[11] = lddate;
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
    writeString(output, sta);
    output.writeLong(ondate);
    output.writeLong(offdate);
    output.writeDouble(lat);
    output.writeDouble(lon);
    output.writeDouble(elev);
    writeString(output, staname);
    writeString(output, statype);
    writeString(output, refsta);
    output.writeDouble(dnorth);
    output.writeDouble(deast);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, sta);
    output.putLong(ondate);
    output.putLong(offdate);
    output.putDouble(lat);
    output.putDouble(lon);
    output.putDouble(elev);
    writeString(output, staname);
    writeString(output, statype);
    writeString(output, refsta);
    output.putDouble(dnorth);
    output.putDouble(deast);
  }

  /**
   * Read a Collection of Site objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Site objects.
   * @throws IOException
   */
  static public void readSites(BufferedReader input, Collection<Site> rows) throws IOException {
    String[] saved = Site.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Site.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Site(new Scanner(line)));
    }
    input.close();
    Site.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Site objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Site objects.
   * @throws IOException
   */
  static public void readSites(File inputFile, Collection<Site> rows) throws IOException {
    readSites(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Site objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Site objects.
   * @throws IOException
   */
  static public void readSites(InputStream inputStream, Collection<Site> rows) throws IOException {
    readSites(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Site objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Site objects
   * @throws IOException
   */
  static public Set<Site> readSites(BufferedReader input) throws IOException {
    Set<Site> rows = new LinkedHashSet<Site>();
    readSites(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Site objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Site objects
   * @throws IOException
   */
  static public Set<Site> readSites(File inputFile) throws IOException {
    return readSites(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Site objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Site objects
   * @throws IOException
   */
  static public Set<Site> readSites(InputStream input) throws IOException {
    return readSites(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Site objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param sites the Site objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Site> sites) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Site site : sites)
      site.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Site objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param sites the Site objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Site> sites, java.util.Date lddate, boolean commit) throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection
          .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Site site : sites) {
        int i = 0;
        statement.setString(++i, site.sta);
        statement.setLong(++i, site.ondate);
        statement.setLong(++i, site.offdate);
        statement.setDouble(++i, site.lat);
        statement.setDouble(++i, site.lon);
        statement.setDouble(++i, site.elev);
        statement.setString(++i, site.staname);
        statement.setString(++i, site.statype);
        statement.setString(++i, site.refsta);
        statement.setDouble(++i, site.dnorth);
        statement.setDouble(++i, site.deast);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Site
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Site> readSites(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Site> results = new HashSet<Site>();
    readSites(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Site
   *        table.
   * @param sites
   * @throws SQLException
   */
  static public void readSites(Connection connection, String selectStatement, Set<Site> sites)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        sites.add(new Site(rs));
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
   * this Site object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Site object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "sta, ondate, offdate, lat, lon, elev, staname, statype, refsta, dnorth, deast, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(sta).append("', ");
    sql.append(Long.toString(ondate)).append(", ");
    sql.append(Long.toString(offdate)).append(", ");
    sql.append(Double.toString(lat)).append(", ");
    sql.append(Double.toString(lon)).append(", ");
    sql.append(Double.toString(elev)).append(", ");
    sql.append("'").append(staname).append("', ");
    sql.append("'").append(statype).append("', ");
    sql.append("'").append(refsta).append("', ");
    sql.append(Double.toString(dnorth)).append(", ");
    sql.append(Double.toString(deast)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Site in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Site in the database
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
   * Generate a sql script to create a table of type Site in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Site in the database
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
    buf.append("sta          varchar2(6)          NOT NULL,\n");
    buf.append("ondate       number(8)            NOT NULL,\n");
    buf.append("offdate      number(8)            NOT NULL,\n");
    buf.append("lat          float(53)            NOT NULL,\n");
    buf.append("lon          float(53)            NOT NULL,\n");
    buf.append("elev         float(24)            NOT NULL,\n");
    buf.append("staname      varchar2(50)         NOT NULL,\n");
    buf.append("statype      varchar2(4)          NOT NULL,\n");
    buf.append("refsta       varchar2(6)          NOT NULL,\n");
    buf.append("dnorth       float(24)            NOT NULL,\n");
    buf.append("deast        float(24)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (sta,ondate)");
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
    return 138;
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
    return (other instanceof Site) && ((Site) other).sta.equals(sta)
        && ((Site) other).ondate == ondate;
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
  public Site setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * Turn on date. Date on which the station, or sensor indicated began operating. The columns
   * offdate and ondate are not intended to accommodate temporary downtimes, but rather to indicate
   * the time period for which the columns of the station (<I>lat</I>, <I>lon</I>, <I>elev</I>,) are
   * valid for the given station code. Stations are often moved, but with the station code remaining
   * unchanged.
   * 
   * @return ondate
   */
  public long getOndate() {
    return ondate;
  }

  /**
   * Turn on date. Date on which the station, or sensor indicated began operating. The columns
   * offdate and ondate are not intended to accommodate temporary downtimes, but rather to indicate
   * the time period for which the columns of the station (<I>lat</I>, <I>lon</I>, <I>elev</I>,) are
   * valid for the given station code. Stations are often moved, but with the station code remaining
   * unchanged.
   * 
   * @param ondate
   * @throws IllegalArgumentException if ondate >= 100000000
   */
  public Site setOndate(long ondate) {
    if (ondate >= 100000000L)
      throw new IllegalArgumentException("ondate=" + ondate + " but cannot be >= 100000000");
    this.ondate = ondate;
    setHash(null);
    return this;
  }

  /**
   * Turn off date. This column is the Julian Date on which the station or sensor indicated was
   * turned off, dismantled, or moved (see <I>ondate</I>)
   * 
   * @return offdate
   */
  public long getOffdate() {
    return offdate;
  }

  /**
   * Turn off date. This column is the Julian Date on which the station or sensor indicated was
   * turned off, dismantled, or moved (see <I>ondate</I>)
   * 
   * @param offdate
   * @throws IllegalArgumentException if offdate >= 100000000
   */
  public Site setOffdate(long offdate) {
    if (offdate >= 100000000L)
      throw new IllegalArgumentException("offdate=" + offdate + " but cannot be >= 100000000");
    this.offdate = offdate;
    setHash(null);
    return this;
  }

  /**
   * Geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   * 
   * @return lat
   */
  public double getLat() {
    return lat;
  }

  /**
   * Geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   * 
   * @param lat
   */
  public Site setLat(double lat) {
    this.lat = lat;
    setHash(null);
    return this;
  }

  /**
   * Geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @return lon
   */
  public double getLon() {
    return lon;
  }

  /**
   * Geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   * 
   * @param lon
   */
  public Site setLon(double lon) {
    this.lon = lon;
    setHash(null);
    return this;
  }

  /**
   * Surface elevation. This column is the elevation of the surface of the earth above the seismic
   * station (<B>site</B>) relative to mean sea level
   * <p>
   * Units: km
   * 
   * @return elev
   */
  public double getElev() {
    return elev;
  }

  /**
   * Surface elevation. This column is the elevation of the surface of the earth above the seismic
   * station (<B>site</B>) relative to mean sea level
   * <p>
   * Units: km
   * 
   * @param elev
   */
  public Site setElev(double elev) {
    this.elev = elev;
    setHash(null);
    return this;
  }

  /**
   * Station name/Description. This value is the full name of the station whose code name is in
   * <I>sta</I> [for example, one record in the <B>site</B> table connects <I>sta</I> = ANMO to
   * staname = ALBUQUERQUE, NEW MEXICO (SRO)].
   * 
   * @return staname
   */
  public String getStaname() {
    return staname;
  }

  /**
   * Station name/Description. This value is the full name of the station whose code name is in
   * <I>sta</I> [for example, one record in the <B>site</B> table connects <I>sta</I> = ANMO to
   * staname = ALBUQUERQUE, NEW MEXICO (SRO)].
   * 
   * @param staname
   * @throws IllegalArgumentException if staname.length() >= 50
   */
  public Site setStaname(String staname) {
    if (staname.length() > 50)
      throw new IllegalArgumentException(
          String.format("staname.length() cannot be > 50.  staname=%s", staname));
    this.staname = staname;
    setHash(null);
    return this;
  }

  /**
   * Station type; character string specifies the station type. Recommended entries are single
   * station (ss) or array (ar).
   * 
   * @return statype
   */
  public String getStatype() {
    return statype;
  }

  /**
   * Station type; character string specifies the station type. Recommended entries are single
   * station (ss) or array (ar).
   * 
   * @param statype
   * @throws IllegalArgumentException if statype.length() >= 4
   */
  public Site setStatype(String statype) {
    if (statype.length() > 4)
      throw new IllegalArgumentException(
          String.format("statype.length() cannot be > 4.  statype=%s", statype));
    this.statype = statype;
    setHash(null);
    return this;
  }

  /**
   * Reference station. This string specifies the reference station with respect to which array
   * members are located (see <I>deast</I>, <I>dnorth</I>).
   * 
   * @return refsta
   */
  public String getRefsta() {
    return refsta;
  }

  /**
   * Reference station. This string specifies the reference station with respect to which array
   * members are located (see <I>deast</I>, <I>dnorth</I>).
   * 
   * @param refsta
   * @throws IllegalArgumentException if refsta.length() >= 6
   */
  public Site setRefsta(String refsta) {
    if (refsta.length() > 6)
      throw new IllegalArgumentException(
          String.format("refsta.length() cannot be > 6.  refsta=%s", refsta));
    this.refsta = refsta;
    setHash(null);
    return this;
  }

  /**
   * Distance North. This column gives the northing or relative position of array element North of
   * the array center specified by the value of <I>refsta</I> (see <I>deast</I>).
   * <p>
   * Units: km
   * 
   * @return dnorth
   */
  public double getDnorth() {
    return dnorth;
  }

  /**
   * Distance North. This column gives the northing or relative position of array element North of
   * the array center specified by the value of <I>refsta</I> (see <I>deast</I>).
   * <p>
   * Units: km
   * 
   * @param dnorth
   */
  public Site setDnorth(double dnorth) {
    this.dnorth = dnorth;
    setHash(null);
    return this;
  }

  /**
   * Distance East. This column gives the easting or the relative position of an array element East
   * of the location of the array center specified by the value of <I>refsta</I> (see
   * <I>dnorth</I>).
   * <p>
   * Units: km
   * 
   * @return deast
   */
  public double getDeast() {
    return deast;
  }

  /**
   * Distance East. This column gives the easting or the relative position of an array element East
   * of the location of the array center specified by the value of <I>refsta</I> (see
   * <I>dnorth</I>).
   * <p>
   * Units: km
   * 
   * @param deast
   */
  public Site setDeast(double deast) {
    this.deast = deast;
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
