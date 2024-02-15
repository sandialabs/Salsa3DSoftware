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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * ?
 */
public class Source extends BaseRow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * -
     */
    private long sourceid;

    static final public long SOURCEID_NA = Long.MIN_VALUE;

    /**
     * -
     */
    private long eventid;

    static final public long EVENTID_NA = -1;

    /**
     * -
     */
    private double lat;

    static final public double LAT_NA = Double.NaN;

    /**
     * -
     */
    private double lon;

    static final public double LON_NA = Double.NaN;

    /**
     * -
     */
    private double depth;

    static final public double DEPTH_NA = Double.NaN;

    /**
     * -
     */
    private double origintime;

    static final public double ORIGINTIME_NA = Double.NaN;

    /**
     * -
     */
    private double gtlevel;

    static final public double GTLEVEL_NA = Double.NaN;

    /**
     * -
     */
    private long numassoc;

    static final public long NUMASSOC_NA = Long.MIN_VALUE;

    /**
     * -
     */
    private long polygonid;

    static final public long POLYGONID_NA = -1;

    /**
     * -
     */
    private String auth;

    static final public String AUTH_NA = null;

    private Map<Long, Srcobsassoc> srcobsassocs = new LinkedHashMap<Long, Srcobsassoc>();
    public Map<Long, Srcobsassoc> getSrcobsassocs() { return srcobsassocs; }

    private static final Columns columns;
    static {
	columns = new Columns();
	columns.add("sourceid", Columns.FieldType.LONG, "%d");
	columns.add("eventid", Columns.FieldType.LONG, "%d");
	columns.add("lat", Columns.FieldType.DOUBLE, "%10.6f");
	columns.add("lon", Columns.FieldType.DOUBLE, "%11.6f");
	columns.add("depth", Columns.FieldType.DOUBLE, "%8.3f");
	columns.add("origintime", Columns.FieldType.DOUBLE, "%12.3f");
	columns.add("gtlevel", Columns.FieldType.DOUBLE, "%6.2f");
	columns.add("numassoc", Columns.FieldType.LONG, "%d");
	columns.add("polygonid", Columns.FieldType.LONG, "%d");
	columns.add("auth", Columns.FieldType.STRING, "%s");
    }

    private static String[] inputColumnNames = columns.getColumnNames();
    private static String[] outputColumnNames = columns.getColumnNames();

    /**
     * Parameterized constructor. Populates all values with specified values.
     */
    public Source(long sourceid, long eventid, double lat, double lon, double depth,
	    double origintime, double gtlevel, long numassoc, long polygonid, String auth) {
	setValues(sourceid, eventid, lat, lon, depth, origintime, gtlevel, numassoc, polygonid, auth);
    }

    private void setValues(long sourceid, long eventid, double lat, double lon, double depth,
	    double origintime, double gtlevel, long numassoc, long polygonid, String auth) {
	this.sourceid = sourceid;
	this.eventid = eventid;
	this.lat = lat;
	this.lon = lon;
	this.depth = depth;
	this.origintime = origintime;
	this.gtlevel = gtlevel;
	this.numassoc = numassoc;
	this.polygonid = polygonid;
	this.auth = auth;
    }

    /**
     * Copy constructor.
     */
    public Source(Source other) {
	this.sourceid = other.getSourceid();
	this.eventid = other.getEventid();
	this.lat = other.getLat();
	this.lon = other.getLon();
	this.depth = other.getDepth();
	this.origintime = other.getOrigintime();
	this.gtlevel = other.getGtlevel();
	this.numassoc = other.getNumassoc();
	this.polygonid = other.getPolygonid();
	this.auth = other.getAuth();
    }

    /**
     * Default constructor that populates all values with na_values.
     */
    public Source() {
	setDefaultValues();
    }

    private void setDefaultValues() {
	setValues(SOURCEID_NA, EVENTID_NA, LAT_NA, LON_NA, DEPTH_NA, ORIGINTIME_NA, GTLEVEL_NA,
		NUMASSOC_NA, POLYGONID_NA, AUTH_NA);
    }

    @Override
    public String getStringField(String name) throws IOException {
	switch (name) {
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
	case "lat":
	    return lat;
	case "lon":
	    return lon;
	case "depth":
	    return depth;
	case "origintime":
	    return origintime;
	case "gtlevel":
	    return gtlevel;
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
	case "depth":
	    depth = value;
	    break;
	case "origintime":
	    origintime = value;
	    break;
	case "gtlevel":
	    gtlevel = value;
	    break;
	default:
	    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
		    + " is not a valid input name ...");
	}
    }

    @Override
    public long getLongField(String name) throws IOException {
	switch (name) {
	case "sourceid":
	    return sourceid;
	case "eventid":
	    return eventid;
	case "numassoc":
	    return numassoc;
	case "polygonid":
	    return polygonid;
	default:
	    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
		    + " is not a valid input name ...");
	}
    }

    @Override
    public void setLongField(String name, String input) throws IOException {
	long value = getInputLong(input, name, this.getClass().getName());
	switch (name) {
	case "sourceid":
	    sourceid = value;
	    break;
	case "eventid":
	    eventid = value;
	    break;
	case "numassoc":
	    numassoc = value;
	    break;
	case "polygonid":
	    polygonid = value;
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
    public Source(Scanner input) throws IOException {
	setDefaultValues();
	String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
	setInputValues(inputs, inputColumnNames, columns);
    }

    /**
     * Constructor that loads values from a DataInputStream.
     */
    public Source(DataInputStream input) throws IOException {
	this(input.readLong(), input.readLong(), input.readDouble(), input.readDouble(),
		input.readDouble(), input.readDouble(), input.readDouble(), input.readLong(),
		input.readLong(), readString(input));
    }

    /**
     * Constructor that loads values from a ByteBuffer.
     */
    public Source(ByteBuffer input) {
	this(input.getLong(), input.getLong(), input.getDouble(), input.getDouble(), input.getDouble(),
		input.getDouble(), input.getDouble(), input.getLong(), input.getLong(), readString(input));
    }

    /**
     * Constructor that loads values from a ResultSet.
     */
    public Source(ResultSet input) throws SQLException {
	this(input, 0);
    }

    /**
     * Constructor that loads values from a ResultSet.
     */
    public Source(ResultSet input, int offset) throws SQLException {
	this(input.getLong(offset + 1), input.getLong(offset + 2), input.getDouble(offset + 3),
		input.getDouble(offset + 4), input.getDouble(offset + 5), 
		input.getTimestamp(offset + 6).getTime()*1e-3,
		input.getDouble(offset + 7), input.getLong(offset + 8), input.getLong(offset + 9),
		input.getString(offset + 10));
    }

    /**
     * Write this row to an Object[] array.
     */
    public Object[] getValues() {
	Object values[] = new Object[10];
	values[0] = sourceid;
	values[1] = eventid;
	values[2] = lat;
	values[3] = lon;
	values[4] = depth;
	values[5] = origintime;
	values[6] = gtlevel;
	values[7] = numassoc;
	values[8] = polygonid;
	values[9] = auth;
	return values;
    }

    /**
     * / Write this row to an Object[] array with load date appended.
     * 
     * @param lddate load date
     */
    public Object[] getValues(java.sql.Date lddate) {
	Object values[] = new Object[11];
	values[0] = sourceid;
	values[1] = eventid;
	values[2] = lat;
	values[3] = lon;
	values[4] = depth;
	values[5] = origintime;
	values[6] = gtlevel;
	values[7] = numassoc;
	values[8] = polygonid;
	values[9] = auth;
	values[10] = lddate;
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
	output.writeLong(sourceid);
	output.writeLong(eventid);
	output.writeDouble(lat);
	output.writeDouble(lon);
	output.writeDouble(depth);
	output.writeDouble(origintime);
	output.writeDouble(gtlevel);
	output.writeLong(numassoc);
	output.writeLong(polygonid);
	writeString(output, auth);
    }

    /**
     * Write this row to a ByteBuffer.
     */
    public void write(ByteBuffer output) {
	output.putLong(sourceid);
	output.putLong(eventid);
	output.putDouble(lat);
	output.putDouble(lon);
	output.putDouble(depth);
	output.putDouble(origintime);
	output.putDouble(gtlevel);
	output.putLong(numassoc);
	output.putLong(polygonid);
	writeString(output, auth);
    }

    /**
     * Read a Collection of Source objects from an ascii BufferedReader.
     * <p>
     * The BufferedReader is closed after reading all the data it contains.
     * 
     * @param input
     * @param rows a Collection of Source objects.
     * @throws IOException
     */
    static public void readSources(BufferedReader input, Collection<Source> rows) throws IOException {
	String[] saved = Source.getInputColumnNames();
	String line;
	int linesRead = 0;
	while ((line = input.readLine()) != null) {
	    line = line.trim();
	    ++linesRead;
	    if (line.startsWith("#") && linesRead == 1) {
		Source.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
	    } else if (!line.startsWith("#"))
		rows.add(new Source(new Scanner(line)));
	}
	input.close();
	Source.setNewInputColumnNames(saved);
    }

    /**
     * Read a Collection of Source objects from an ascii file. The Collection is not emptied before
     * reading.
     * 
     * @param inputFile
     * @param rows a Collection of Source objects.
     * @throws IOException
     */
    static public void readSources(File inputFile, Collection<Source> rows) throws IOException {
	readSources(new BufferedReader(new FileReader(inputFile)), rows);
    }

    /**
     * Read a Collection of Source objects from an ascii input stream. The Collection is not emptied
     * before reading.
     * 
     * @param inputStream
     * @param rows a Collection of Source objects.
     * @throws IOException
     */
    static public void readSources(InputStream inputStream, Collection<Source> rows)
	    throws IOException {
	readSources(new BufferedReader(new InputStreamReader(inputStream)), rows);
    }

    /**
     * Read a LinkedHashSet of Source objects from an ascii BufferedReader.
     * 
     * @param input
     * @return a LinkedHashSet of Source objects
     * @throws IOException
     */
    static public Set<Source> readSources(BufferedReader input) throws IOException {
	Set<Source> rows = new LinkedHashSet<Source>();
	readSources(input, rows);
	return rows;
    }

    /**
     * Read a LinkedHashSet of Source objects from an ascii file.
     * 
     * @param inputFile
     * @return a LinkedHashSet of Source objects
     * @throws IOException
     */
    static public Set<Source> readSources(File inputFile) throws IOException {
	return readSources(new BufferedReader(new FileReader(inputFile)));
    }

    /**
     * Read a LinkedHashSet of Source objects from an ascii InputStream.
     * 
     * @param input
     * @return a LinkedHashSet of Source objects
     * @throws IOException
     */
    static public Set<Source> readSources(InputStream input) throws IOException {
	return readSources(new BufferedReader(new InputStreamReader(input)));
    }

    /**
     * Write a batch of Source objects to an ascii file.
     * 
     * @param fileName name of file to write to.
     * @param sources the Source objects to write
     * @throws IOException
     */
    static public void write(File fileName, Collection<? extends Source> sources) throws IOException {
	BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
	writeHeader(output);
	for (Source source : sources)
	    source.writeln(output);
	output.close();
    }

    /**
     * Insert a batch of Source objects into a database table.
     * 
     * @param connection database Connection object
     * @param tableName the name of the table into which the rows should be inserted
     * @param sources the Source objects to insert
     * @param lddate the supplied load date is inserted at the end of the row.
     * @param commit if true, a commit is executed after all the rows have been inserted.
     * @throws SQLException
     */
    static public void write(Connection connection, String tableName,
	    Collection<? extends Source> sources, java.util.Date lddate, boolean commit)
		    throws SQLException {
	PreparedStatement statement = null;
	try {
	    statement = connection
		    .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?)");
	    for (Source source : sources) {
		int i = 0;
		statement.setLong(++i, source.sourceid);
		statement.setLong(++i, source.eventid);
		statement.setDouble(++i, source.lat);
		statement.setDouble(++i, source.lon);
		statement.setDouble(++i, source.depth);
		statement.setTimestamp(++i, new Timestamp((long)(source.origintime*1000)));
		statement.setDouble(++i, source.gtlevel);
		statement.setLong(++i, source.numassoc);
		statement.setLong(++i, source.polygonid);
		statement.setString(++i, source.auth);
		statement.setTimestamp(++i, new Timestamp(lddate.getTime()));
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
     * @param selectStatement a valid SQL select statement that returns a complete row from a Source
     *        table.
     * @return data
     * @throws SQLException
     */
    static public HashSet<Source> readSources(Connection connection, String selectStatement)
	    throws SQLException {
	HashSet<Source> results = new HashSet<Source>();
	readSources(connection, selectStatement, results);
	return results;
    }

    /**
     * Read data from the database.
     * 
     * @param connection
     * @param selectStatement a valid SQL select statement that returns a complete row from a Source
     *        table.
     * @param sources
     * @throws SQLException
     */
    static public void readSources(Connection connection, String selectStatement, Set<Source> sources)
	    throws SQLException {
	Statement statement = null;
	ResultSet rs = null;
	try {
	    statement = connection.createStatement();
	    rs = statement.executeQuery(selectStatement);
	    while (rs.next()) {
		sources.add(new Source(rs));
	    }
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
     * this Source object into a database.
     * 
     * @param tableName name of the table into which the values will be inserted.
     * @return a String representation of a sql statement that can be used to insert the values of
     *         this Source object into a database.
     */
    @Override
    public String getInsertSql(String tableName) {
	StringBuffer sql = new StringBuffer();
	sql.append("insert into ").append(tableName);
	sql.append(" (");
	sql.append(
		"sourceid, eventid, lat, lon, depth, origintime, gtlevel, numassoc, polygonid, auth, lddate");
	sql.append(")");
	sql.append(" values (");
	sql.append(Long.toString(sourceid)).append(", ");
	sql.append(Long.toString(eventid)).append(", ");
	sql.append(Double.toString(lat)).append(", ");
	sql.append(Double.toString(lon)).append(", ");
	sql.append(Double.toString(depth)).append(", ");
	sql.append(Double.toString(origintime)).append(", ");
	sql.append(Double.toString(gtlevel)).append(", ");
	sql.append(Long.toString(numassoc)).append(", ");
	sql.append(Long.toString(polygonid)).append(", ");
	sql.append("'").append(auth).append("', ");
	sql.append("SYSDATE)");
	return sql.toString();
    }

    /**
     * Create a table of type Source in the database. Primary and unique keys are set, if defined.
     * 
     * @param connection
     * @param tableName
     * @throws SQLException
     */
    static public void createTable(Connection connection, String tableName) throws SQLException {
	createTable(connection, tableName, true, true);
    }

    /**
     * Create a table of type Source in the database
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
     * Generate a sql script to create a table of type Source in the database Primary and unique keys
     * are set, if defined.
     * 
     * @param tableName
     * @throws SQLException
     */
    static public ArrayList<String> createTableScript(String tableName) throws SQLException {
	return createTableScript(tableName, true, true);
    }

    /**
     * Generate a sql script to create a table of type type Source in the database
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
	buf.append("sourceid     number(10)           NOT NULL,\n");
	buf.append("eventid      number(10)           NOT NULL,\n");
	buf.append("lat          float(126)           NOT NULL,\n");
	buf.append("lon          float(126)           NOT NULL,\n");
	buf.append("depth        float(126)           NOT NULL,\n");
	buf.append("origintime   timestamp(3)         NOT NULL,\n");
	buf.append("gtlevel      float(126)           NOT NULL,\n");
	buf.append("numassoc     number(10)           NOT NULL,\n");
	buf.append("polygonid    number(10)           NOT NULL,\n");
	buf.append("auth         varchar2(64)         NOT NULL,\n");
	buf.append("lddate       date                 NOT NULL\n");
	buf.append(")");
	script.add(buf.toString());
	String[] tableNameParts = tableName.split("\\.");
	String constraint = tableNameParts[tableNameParts.length - 1];
	if (includePrimaryKeyConstraint)
	    script.add("alter table " + tableName + " add constraint " + constraint
		    + "_pk primary key (sourceid)");
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
	return 140;
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
	return (other instanceof Source) && ((Source) other).sourceid == sourceid;
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
    public Source setSourceid(long sourceid) {
	if (sourceid >= 10000000000L)
	    throw new IllegalArgumentException("sourceid=" + sourceid + " but cannot be >= 10000000000");
	this.sourceid = sourceid;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return eventid
     */
    public long getEventid() {
	return eventid;
    }

    /**
     * -
     * 
     * @param eventid
     * @throws IllegalArgumentException if eventid >= 10000000000
     */
    public Source setEventid(long eventid) {
	if (eventid >= 10000000000L)
	    throw new IllegalArgumentException("eventid=" + eventid + " but cannot be >= 10000000000");
	this.eventid = eventid;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return lat
     */
    public double getLat() {
	return lat;
    }

    /**
     * -
     * 
     * @param lat
     */
    public Source setLat(double lat) {
	this.lat = lat;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return lon
     */
    public double getLon() {
	return lon;
    }

    /**
     * -
     * 
     * @param lon
     */
    public Source setLon(double lon) {
	this.lon = lon;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return depth
     */
    public double getDepth() {
	return depth;
    }

    /**
     * -
     * 
     * @param depth
     */
    public Source setDepth(double depth) {
	this.depth = depth;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return origintime
     */
    public double getOrigintime() {
	return origintime;
    }

    /**
     * -
     * 
     * @param origintime
     */
    public Source setOrigintime(double origintime) {
	this.origintime = origintime;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return gtlevel
     */
    public double getGtlevel() {
	return gtlevel;
    }

    /**
     * -
     * 
     * @param gtlevel
     */
    public Source setGtlevel(double gtlevel) {
	this.gtlevel = gtlevel;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return numassoc
     */
    public long getNumassoc() {
	return numassoc;
    }

    public long getNdef() {
    	int ndef = 0;
    	for (Srcobsassoc a : srcobsassocs.values())
    		if (a.getTimedef().equals("d"))
    			++ndef;
    	return ndef;
    }

    /**
     * -
     * 
     * @param numassoc
     * @throws IllegalArgumentException if numassoc >= 10000000000
     */
    public Source setNumassoc(long numassoc) {
	if (numassoc >= 10000000000L)
	    throw new IllegalArgumentException("numassoc=" + numassoc + " but cannot be >= 10000000000");
	this.numassoc = numassoc;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return polygonid
     */
    public long getPolygonid() {
	return polygonid;
    }

    /**
     * -
     * 
     * @param polygonid
     * @throws IllegalArgumentException if polygonid >= 10000000000
     */
    public Source setPolygonid(long polygonid) {
	if (polygonid >= 10000000000L)
	    throw new IllegalArgumentException(
		    "polygonid=" + polygonid + " but cannot be >= 10000000000");
	this.polygonid = polygonid;
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
    public Source setAuth(String auth) {
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

    public static Map<Long, Source> readSources(Connection connection, Map<String, String> tableNames,
	    String whereClause) throws SQLException {

	Map<Long, Source> sources = new TreeMap<Long, Source>();
	HashSet<Srcobsassoc> soas = new HashSet<>();
	Map<Long, Observation> observations = new TreeMap<Long, Observation>();
	Map<Long, Receiver> receivers = new TreeMap<>();

	String sql = String.format("select * from %s %s", tableNames.get("source"), whereClause);
	String sourceids = "";
	for (Source s : Source.readSources(connection, sql)) {
	    sources.put(s.getSourceid(), s);
	    sourceids += ","+s.getSourceid();
	}

	if (tableNames.containsKey("srcobsassoc")) {
	    whereClause = "where sourceid in ("+sourceids.substring(1)+")";
	    sql = String.format("select * from %s %s", tableNames.get("srcobsassoc"), whereClause);
	    soas = Srcobsassoc.readSrcobsassocs(connection, sql);

	    if (tableNames.containsKey("observation")) {
		whereClause = "";
		for (Srcobsassoc soa : soas)
		    whereClause += ","+soa.getObservationid();

		Set<Long> receiverIds = new TreeSet<>();
		whereClause = "where observationid in ("+whereClause.substring(1)+")";
		sql = String.format("select * from %s %s", tableNames.get("observation"), whereClause);
		for (Observation obs : Observation.readObservations(connection, sql)) {
		    observations.put(obs.getObservationid(), obs);
		    receiverIds.add(obs.getReceiverid());
		}

		if (receiverIds.size() > 0 ) {
		    String r = "";
		    for (Long receiverid : receiverIds)
			r += ","+receiverid;

		    whereClause = "where receiverid in ("+r.substring(1)+")";
		    sql = String.format("select * from %s %s", tableNames.get("receiver"), whereClause);
		    for (Receiver receiver : Receiver.readReceivers(connection, sql).values())
			receivers.put(receiver.getReceiverid(), receiver);
		}
	    }
	}

	for (Observation obs : observations.values())
	    obs.setReceiver(receivers.get(obs.getReceiverid()));

	for (Srcobsassoc soa : soas) {
	    soa.setObservation(observations.get(soa.getObservationid()));
	    sources.get(soa.getSourceid()).getSrcobsassocs().put(soa.getObservationid(), soa);
	}
	return sources;
    }

    public Buff getBuff() {
	Buff buffer = new Buff(this.getClass().getSimpleName());
	buffer.add("format", 1);
	buffer.add("sourceid", sourceid);
	buffer.add("eventid", eventid);
	buffer.add("lat", lat, 6);
	buffer.add("lon", lon, 6);
	buffer.add("depth", depth, 3);
	buffer.add("origintime", origintime, 2);
	buffer.add("gtlevel", gtlevel, 2);
	buffer.add("numassoc", numassoc);
	buffer.add("polygonid", polygonid);
	buffer.add("auth", auth);

	buffer.add("nSrcobsassocs", srcobsassocs.size());
	TreeSet<Long> observationid = new TreeSet<>(srcobsassocs.keySet());
	for (Long arid : observationid)
	    buffer.add(srcobsassocs.get(arid).getBuff());

	return buffer;
    }

    static public Buff getBuff(Scanner input) {
	Buff buf = new Buff(input);

	for (int i=0; i<buf.getInt("nSrcobsassocs"); ++i)
	    buf.add(Srcobsassoc.getBuff(input));

	return buf;

    }

    public static Buff getBuff(Collection<Source> sources) {
	Buff buffer = new Buff("Sources");
	buffer.add("format", 1);
	buffer.add("nSources", sources.size());
	for (Source s : sources)
	    buffer.add(s.getBuff());
	return buffer;
    }

}
