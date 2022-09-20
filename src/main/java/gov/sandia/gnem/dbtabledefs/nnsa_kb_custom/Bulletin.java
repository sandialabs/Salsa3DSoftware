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
 * bulletin
 */
public class Bulletin extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Bulletin identifier
   */
  private long bullid;

  static final public long BULLID_NA = Long.MIN_VALUE;

  /**
   * A version code for this particular bulletin if the same bulletin has been released in multiple
   * versions (for corrections, for example)
   */
  private String bullver;

  static final public String BULLVER_NA = null;

  /**
   * The directory name that contains the flat file information. For the <B>tomo_info</B> table,
   * this is the directory where the Q0 tomography information is located.
   */
  private String dir;

  static final public String DIR_NA = null;

  /**
   * The name of the data file that contains the flat file information. For the <B>tomo_info</B>
   * table, this is the data file that contains the Q0 tomography information.
   */
  private String dfile;

  static final public String DFILE_NA = null;

  /**
   * For the <B>bulletin</B> table, offset to the location of the information e. g. start of
   * bulletin. For the <B>bull_assoc</B> table, offset to the object in the bulletin.
   */
  private long foff;

  static final public long FOFF_NA = -1;

  /**
   * Author used in other tables for this bulletin source
   */
  private String bullauth;

  static final public String BULLAUTH_NA = null;

  /**
   * Epoch time
   * <p>
   * Units: s
   */
  private double time;

  static final public double TIME_NA = -9999999999.999;

  /**
   * End of time period covered
   * <p>
   * Units: s
   */
  private double endtime;

  static final public double ENDTIME_NA = 9999999999.999;

  /**
   * Description of region covered
   */
  private String region;

  static final public String REGION_NA = "-";

  /**
   * Immediate source of data: name of agency, webpage or person
   */
  private String came_from;

  static final public String CAME_FROM_NA = "-";

  /**
   * Contains information about origins (y/n)
   */
  private String origin;

  static final public String ORIGIN_NA = null;

  /**
   * Contains information about arrivals (y/n)
   */
  private String arrival;

  static final public String ARRIVAL_NA = null;

  /**
   * Contains information about stations (y/n)
   */
  private String station;

  static final public String STATION_NA = null;

  /**
   * units for foff in bull_assoc (e.g.: line)
   */
  private String foff_unit;

  static final public String FOFF_UNIT_NA = null;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = "-";

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
    columns.add("bullid", Columns.FieldType.LONG, "%d");
    columns.add("bullver", Columns.FieldType.STRING, "%s");
    columns.add("dir", Columns.FieldType.STRING, "%s");
    columns.add("dfile", Columns.FieldType.STRING, "%s");
    columns.add("foff", Columns.FieldType.LONG, "%d");
    columns.add("bullauth", Columns.FieldType.STRING, "%s");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("endtime", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("region", Columns.FieldType.STRING, "%s");
    columns.add("came_from", Columns.FieldType.STRING, "%s");
    columns.add("origin", Columns.FieldType.STRING, "%s");
    columns.add("arrival", Columns.FieldType.STRING, "%s");
    columns.add("station", Columns.FieldType.STRING, "%s");
    columns.add("foff_unit", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Bulletin(long bullid, String bullver, String dir, String dfile, long foff, String bullauth,
      double time, double endtime, String region, String came_from, String origin, String arrival,
      String station, String foff_unit, String auth, long commid) {
    setValues(bullid, bullver, dir, dfile, foff, bullauth, time, endtime, region, came_from, origin,
        arrival, station, foff_unit, auth, commid);
  }

  private void setValues(long bullid, String bullver, String dir, String dfile, long foff,
      String bullauth, double time, double endtime, String region, String came_from, String origin,
      String arrival, String station, String foff_unit, String auth, long commid) {
    this.bullid = bullid;
    this.bullver = bullver;
    this.dir = dir;
    this.dfile = dfile;
    this.foff = foff;
    this.bullauth = bullauth;
    this.time = time;
    this.endtime = endtime;
    this.region = region;
    this.came_from = came_from;
    this.origin = origin;
    this.arrival = arrival;
    this.station = station;
    this.foff_unit = foff_unit;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Bulletin(Bulletin other) {
    this.bullid = other.getBullid();
    this.bullver = other.getBullver();
    this.dir = other.getDir();
    this.dfile = other.getDfile();
    this.foff = other.getFoff();
    this.bullauth = other.getBullauth();
    this.time = other.getTime();
    this.endtime = other.getEndtime();
    this.region = other.getRegion();
    this.came_from = other.getCame_from();
    this.origin = other.getOrigin();
    this.arrival = other.getArrival();
    this.station = other.getStation();
    this.foff_unit = other.getFoff_unit();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Bulletin() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(BULLID_NA, BULLVER_NA, DIR_NA, DFILE_NA, FOFF_NA, BULLAUTH_NA, TIME_NA, ENDTIME_NA,
        REGION_NA, CAME_FROM_NA, ORIGIN_NA, ARRIVAL_NA, STATION_NA, FOFF_UNIT_NA, AUTH_NA,
        COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "bullver":
        return bullver;
      case "dir":
        return dir;
      case "dfile":
        return dfile;
      case "bullauth":
        return bullauth;
      case "region":
        return region;
      case "came_from":
        return came_from;
      case "origin":
        return origin;
      case "arrival":
        return arrival;
      case "station":
        return station;
      case "foff_unit":
        return foff_unit;
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
      case "bullver":
        bullver = value;
        break;
      case "dir":
        dir = value;
        break;
      case "dfile":
        dfile = value;
        break;
      case "bullauth":
        bullauth = value;
        break;
      case "region":
        region = value;
        break;
      case "came_from":
        came_from = value;
        break;
      case "origin":
        origin = value;
        break;
      case "arrival":
        arrival = value;
        break;
      case "station":
        station = value;
        break;
      case "foff_unit":
        foff_unit = value;
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
      case "time":
        return time;
      case "endtime":
        return endtime;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "time":
        time = value;
        break;
      case "endtime":
        endtime = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "bullid":
        return bullid;
      case "foff":
        return foff;
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
      case "bullid":
        bullid = value;
        break;
      case "foff":
        foff = value;
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
  public Bulletin(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Bulletin(DataInputStream input) throws IOException {
    this(input.readLong(), readString(input), readString(input), readString(input),
        input.readLong(), readString(input), input.readDouble(), input.readDouble(),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Bulletin(ByteBuffer input) {
    this(input.getLong(), readString(input), readString(input), readString(input), input.getLong(),
        readString(input), input.getDouble(), input.getDouble(), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Bulletin(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Bulletin(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getLong(offset + 5), input.getString(offset + 6),
        input.getDouble(offset + 7), input.getDouble(offset + 8), input.getString(offset + 9),
        input.getString(offset + 10), input.getString(offset + 11), input.getString(offset + 12),
        input.getString(offset + 13), input.getString(offset + 14), input.getString(offset + 15),
        input.getLong(offset + 16));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[16];
    values[0] = bullid;
    values[1] = bullver;
    values[2] = dir;
    values[3] = dfile;
    values[4] = foff;
    values[5] = bullauth;
    values[6] = time;
    values[7] = endtime;
    values[8] = region;
    values[9] = came_from;
    values[10] = origin;
    values[11] = arrival;
    values[12] = station;
    values[13] = foff_unit;
    values[14] = auth;
    values[15] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[17];
    values[0] = bullid;
    values[1] = bullver;
    values[2] = dir;
    values[3] = dfile;
    values[4] = foff;
    values[5] = bullauth;
    values[6] = time;
    values[7] = endtime;
    values[8] = region;
    values[9] = came_from;
    values[10] = origin;
    values[11] = arrival;
    values[12] = station;
    values[13] = foff_unit;
    values[14] = auth;
    values[15] = commid;
    values[16] = lddate;
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
    output.writeLong(bullid);
    writeString(output, bullver);
    writeString(output, dir);
    writeString(output, dfile);
    output.writeLong(foff);
    writeString(output, bullauth);
    output.writeDouble(time);
    output.writeDouble(endtime);
    writeString(output, region);
    writeString(output, came_from);
    writeString(output, origin);
    writeString(output, arrival);
    writeString(output, station);
    writeString(output, foff_unit);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(bullid);
    writeString(output, bullver);
    writeString(output, dir);
    writeString(output, dfile);
    output.putLong(foff);
    writeString(output, bullauth);
    output.putDouble(time);
    output.putDouble(endtime);
    writeString(output, region);
    writeString(output, came_from);
    writeString(output, origin);
    writeString(output, arrival);
    writeString(output, station);
    writeString(output, foff_unit);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Bulletin objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Bulletin objects.
   * @throws IOException
   */
  static public void readBulletins(BufferedReader input, Collection<Bulletin> rows)
      throws IOException {
    String[] saved = Bulletin.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Bulletin
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Bulletin(new Scanner(line)));
    }
    input.close();
    Bulletin.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Bulletin objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Bulletin objects.
   * @throws IOException
   */
  static public void readBulletins(File inputFile, Collection<Bulletin> rows) throws IOException {
    readBulletins(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Bulletin objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Bulletin objects.
   * @throws IOException
   */
  static public void readBulletins(InputStream inputStream, Collection<Bulletin> rows)
      throws IOException {
    readBulletins(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Bulletin objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Bulletin objects
   * @throws IOException
   */
  static public Set<Bulletin> readBulletins(BufferedReader input) throws IOException {
    Set<Bulletin> rows = new LinkedHashSet<Bulletin>();
    readBulletins(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Bulletin objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Bulletin objects
   * @throws IOException
   */
  static public Set<Bulletin> readBulletins(File inputFile) throws IOException {
    return readBulletins(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Bulletin objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Bulletin objects
   * @throws IOException
   */
  static public Set<Bulletin> readBulletins(InputStream input) throws IOException {
    return readBulletins(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Bulletin objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param bulletins the Bulletin objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Bulletin> bulletins)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Bulletin bulletin : bulletins)
      bulletin.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Bulletin objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param bulletins the Bulletin objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Bulletin> bulletins, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Bulletin bulletin : bulletins) {
        int i = 0;
        statement.setLong(++i, bulletin.bullid);
        statement.setString(++i, bulletin.bullver);
        statement.setString(++i, bulletin.dir);
        statement.setString(++i, bulletin.dfile);
        statement.setLong(++i, bulletin.foff);
        statement.setString(++i, bulletin.bullauth);
        statement.setDouble(++i, bulletin.time);
        statement.setDouble(++i, bulletin.endtime);
        statement.setString(++i, bulletin.region);
        statement.setString(++i, bulletin.came_from);
        statement.setString(++i, bulletin.origin);
        statement.setString(++i, bulletin.arrival);
        statement.setString(++i, bulletin.station);
        statement.setString(++i, bulletin.foff_unit);
        statement.setString(++i, bulletin.auth);
        statement.setLong(++i, bulletin.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Bulletin
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Bulletin> readBulletins(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Bulletin> results = new HashSet<Bulletin>();
    readBulletins(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Bulletin
   *        table.
   * @param bulletins
   * @throws SQLException
   */
  static public void readBulletins(Connection connection, String selectStatement,
      Set<Bulletin> bulletins) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        bulletins.add(new Bulletin(rs));
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
   * this Bulletin object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Bulletin object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "bullid, bullver, dir, dfile, foff, bullauth, time, endtime, region, came_from, origin, arrival, station, foff_unit, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(bullid)).append(", ");
    sql.append("'").append(bullver).append("', ");
    sql.append("'").append(dir).append("', ");
    sql.append("'").append(dfile).append("', ");
    sql.append(Long.toString(foff)).append(", ");
    sql.append("'").append(bullauth).append("', ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Double.toString(endtime)).append(", ");
    sql.append("'").append(region).append("', ");
    sql.append("'").append(came_from).append("', ");
    sql.append("'").append(origin).append("', ");
    sql.append("'").append(arrival).append("', ");
    sql.append("'").append(station).append("', ");
    sql.append("'").append(foff_unit).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Bulletin in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Bulletin in the database
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
   * Generate a sql script to create a table of type Bulletin in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Bulletin in the database
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
    buf.append("bullid       number(9)            NOT NULL,\n");
    buf.append("bullver      varchar2(8)          NOT NULL,\n");
    buf.append("dir          varchar2(64)         NOT NULL,\n");
    buf.append("dfile        varchar2(32)         NOT NULL,\n");
    buf.append("foff         number(9)            NOT NULL,\n");
    buf.append("bullauth     varchar2(15)         NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("endtime      float(53)            NOT NULL,\n");
    buf.append("region       varchar2(50)         NOT NULL,\n");
    buf.append("came_from    varchar2(50)         NOT NULL,\n");
    buf.append("origin       varchar2(1)          NOT NULL,\n");
    buf.append("arrival      varchar2(1)          NOT NULL,\n");
    buf.append("station      varchar2(1)          NOT NULL,\n");
    buf.append("foff_unit    varchar2(15)         NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (bullid,bullver)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (dir,dfile,foff,bullauth,time)");
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
    return 341;
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
    return (other instanceof Bulletin) && ((Bulletin) other).bullid == bullid
        && ((Bulletin) other).bullver.equals(bullver);
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
    return (other instanceof Bulletin) && ((Bulletin) other).dir.equals(dir)
        && ((Bulletin) other).dfile.equals(dfile) && ((Bulletin) other).foff == foff
        && ((Bulletin) other).bullauth.equals(bullauth) && ((Bulletin) other).time == time;
  }

  /**
   * Bulletin identifier
   * 
   * @return bullid
   */
  public long getBullid() {
    return bullid;
  }

  /**
   * Bulletin identifier
   * 
   * @param bullid
   * @throws IllegalArgumentException if bullid >= 1000000000
   */
  public Bulletin setBullid(long bullid) {
    if (bullid >= 1000000000L)
      throw new IllegalArgumentException("bullid=" + bullid + " but cannot be >= 1000000000");
    this.bullid = bullid;
    setHash(null);
    return this;
  }

  /**
   * A version code for this particular bulletin if the same bulletin has been released in multiple
   * versions (for corrections, for example)
   * 
   * @return bullver
   */
  public String getBullver() {
    return bullver;
  }

  /**
   * A version code for this particular bulletin if the same bulletin has been released in multiple
   * versions (for corrections, for example)
   * 
   * @param bullver
   * @throws IllegalArgumentException if bullver.length() >= 8
   */
  public Bulletin setBullver(String bullver) {
    if (bullver.length() > 8)
      throw new IllegalArgumentException(
          String.format("bullver.length() cannot be > 8.  bullver=%s", bullver));
    this.bullver = bullver;
    setHash(null);
    return this;
  }

  /**
   * The directory name that contains the flat file information. For the <B>tomo_info</B> table,
   * this is the directory where the Q0 tomography information is located.
   * 
   * @return dir
   */
  public String getDir() {
    return dir;
  }

  /**
   * The directory name that contains the flat file information. For the <B>tomo_info</B> table,
   * this is the directory where the Q0 tomography information is located.
   * 
   * @param dir
   * @throws IllegalArgumentException if dir.length() >= 64
   */
  public Bulletin setDir(String dir) {
    if (dir.length() > 64)
      throw new IllegalArgumentException(
          String.format("dir.length() cannot be > 64.  dir=%s", dir));
    this.dir = dir;
    setHash(null);
    return this;
  }

  /**
   * The name of the data file that contains the flat file information. For the <B>tomo_info</B>
   * table, this is the data file that contains the Q0 tomography information.
   * 
   * @return dfile
   */
  public String getDfile() {
    return dfile;
  }

  /**
   * The name of the data file that contains the flat file information. For the <B>tomo_info</B>
   * table, this is the data file that contains the Q0 tomography information.
   * 
   * @param dfile
   * @throws IllegalArgumentException if dfile.length() >= 32
   */
  public Bulletin setDfile(String dfile) {
    if (dfile.length() > 32)
      throw new IllegalArgumentException(
          String.format("dfile.length() cannot be > 32.  dfile=%s", dfile));
    this.dfile = dfile;
    setHash(null);
    return this;
  }

  /**
   * For the <B>bulletin</B> table, offset to the location of the information e. g. start of
   * bulletin. For the <B>bull_assoc</B> table, offset to the object in the bulletin.
   * 
   * @return foff
   */
  public long getFoff() {
    return foff;
  }

  /**
   * For the <B>bulletin</B> table, offset to the location of the information e. g. start of
   * bulletin. For the <B>bull_assoc</B> table, offset to the object in the bulletin.
   * 
   * @param foff
   * @throws IllegalArgumentException if foff >= 1000000000
   */
  public Bulletin setFoff(long foff) {
    if (foff >= 1000000000L)
      throw new IllegalArgumentException("foff=" + foff + " but cannot be >= 1000000000");
    this.foff = foff;
    setHash(null);
    return this;
  }

  /**
   * Author used in other tables for this bulletin source
   * 
   * @return bullauth
   */
  public String getBullauth() {
    return bullauth;
  }

  /**
   * Author used in other tables for this bulletin source
   * 
   * @param bullauth
   * @throws IllegalArgumentException if bullauth.length() >= 15
   */
  public Bulletin setBullauth(String bullauth) {
    if (bullauth.length() > 15)
      throw new IllegalArgumentException(
          String.format("bullauth.length() cannot be > 15.  bullauth=%s", bullauth));
    this.bullauth = bullauth;
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
  public Bulletin setTime(double time) {
    this.time = time;
    setHash(null);
    return this;
  }

  /**
   * End of time period covered
   * <p>
   * Units: s
   * 
   * @return endtime
   */
  public double getEndtime() {
    return endtime;
  }

  /**
   * End of time period covered
   * <p>
   * Units: s
   * 
   * @param endtime
   */
  public Bulletin setEndtime(double endtime) {
    this.endtime = endtime;
    setHash(null);
    return this;
  }

  /**
   * Description of region covered
   * 
   * @return region
   */
  public String getRegion() {
    return region;
  }

  /**
   * Description of region covered
   * 
   * @param region
   * @throws IllegalArgumentException if region.length() >= 50
   */
  public Bulletin setRegion(String region) {
    if (region.length() > 50)
      throw new IllegalArgumentException(
          String.format("region.length() cannot be > 50.  region=%s", region));
    this.region = region;
    setHash(null);
    return this;
  }

  /**
   * Immediate source of data: name of agency, webpage or person
   * 
   * @return came_from
   */
  public String getCame_from() {
    return came_from;
  }

  /**
   * Immediate source of data: name of agency, webpage or person
   * 
   * @param came_from
   * @throws IllegalArgumentException if came_from.length() >= 50
   */
  public Bulletin setCame_from(String came_from) {
    if (came_from.length() > 50)
      throw new IllegalArgumentException(
          String.format("came_from.length() cannot be > 50.  came_from=%s", came_from));
    this.came_from = came_from;
    setHash(null);
    return this;
  }

  /**
   * Contains information about origins (y/n)
   * 
   * @return origin
   */
  public String getOrigin() {
    return origin;
  }

  /**
   * Contains information about origins (y/n)
   * 
   * @param origin
   * @throws IllegalArgumentException if origin.length() >= 1
   */
  public Bulletin setOrigin(String origin) {
    if (origin.length() > 1)
      throw new IllegalArgumentException(
          String.format("origin.length() cannot be > 1.  origin=%s", origin));
    this.origin = origin;
    setHash(null);
    return this;
  }

  /**
   * Contains information about arrivals (y/n)
   * 
   * @return arrival
   */
  public String getArrival() {
    return arrival;
  }

  /**
   * Contains information about arrivals (y/n)
   * 
   * @param arrival
   * @throws IllegalArgumentException if arrival.length() >= 1
   */
  public Bulletin setArrival(String arrival) {
    if (arrival.length() > 1)
      throw new IllegalArgumentException(
          String.format("arrival.length() cannot be > 1.  arrival=%s", arrival));
    this.arrival = arrival;
    setHash(null);
    return this;
  }

  /**
   * Contains information about stations (y/n)
   * 
   * @return station
   */
  public String getStation() {
    return station;
  }

  /**
   * Contains information about stations (y/n)
   * 
   * @param station
   * @throws IllegalArgumentException if station.length() >= 1
   */
  public Bulletin setStation(String station) {
    if (station.length() > 1)
      throw new IllegalArgumentException(
          String.format("station.length() cannot be > 1.  station=%s", station));
    this.station = station;
    setHash(null);
    return this;
  }

  /**
   * units for foff in bull_assoc (e.g.: line)
   * 
   * @return foff_unit
   */
  public String getFoff_unit() {
    return foff_unit;
  }

  /**
   * units for foff in bull_assoc (e.g.: line)
   * 
   * @param foff_unit
   * @throws IllegalArgumentException if foff_unit.length() >= 15
   */
  public Bulletin setFoff_unit(String foff_unit) {
    if (foff_unit.length() > 15)
      throw new IllegalArgumentException(
          String.format("foff_unit.length() cannot be > 15.  foff_unit=%s", foff_unit));
    this.foff_unit = foff_unit;
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
  public Bulletin setAuth(String auth) {
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
  public Bulletin setCommid(long commid) {
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
