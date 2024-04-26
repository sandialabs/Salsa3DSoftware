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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import gov.sandia.gmp.util.testingbuffer.TestBuffer;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * ?
 */
public class Observation extends BaseRow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * -
     */
    private long observationid;

    static final public long OBSERVATIONID_NA = Long.MIN_VALUE;

    /**
     * -
     */
    private long receiverid;

    static final public long RECEIVERID_NA = Long.MIN_VALUE;

    /**
     * -
     */
    private String iphase;

    static final public String IPHASE_NA = null;

    /**
     * -
     */
    private double arrivaltime;

    static final public double ARRIVALTIME_NA = -9999999999.999;

    /**
     * -
     */
    private double timeuncertainty;

    static final public double TIMEUNCERTAINTY_NA = -999.0;

    /**
     * -
     */
    private double azimuth;

    static final public double AZIMUTH_NA = -999.0;

    /**
     * -
     */
    private double azuncertainty;

    static final public double AZUNCERTAINTY_NA = -999.0;

    /**
     * -
     */
    private double slowness;

    static final public double SLOWNESS_NA = -999.0;

    /**
     * -
     */
    private double slowuncertainty;

    static final public double SLOWUNCERTAINTY_NA = -999.0;

    /**
     * -
     */
    private String auth;

    static final public String AUTH_NA = null;


    private static final Columns columns;
    static {
	columns = new Columns();
	columns.add("observationid", Columns.FieldType.LONG, "%d");
	columns.add("receiverid", Columns.FieldType.LONG, "%d");
	columns.add("iphase", Columns.FieldType.STRING, "%s");
	columns.add("arrivaltime", Columns.FieldType.DOUBLE, "%12.3f");
	columns.add("timeuncertainty", Columns.FieldType.DOUBLE, "%6.3f");
	columns.add("azimuth", Columns.FieldType.DOUBLE, "%8.3f");
	columns.add("azuncertainty", Columns.FieldType.DOUBLE, "%8.3f");
	columns.add("slowness", Columns.FieldType.DOUBLE, "%8.3f");
	columns.add("slowuncertainty", Columns.FieldType.DOUBLE, "%8.3f");
	columns.add("auth", Columns.FieldType.STRING, "%s");
    }

    private static String[] inputColumnNames = columns.getColumnNames();
    private static String[] outputColumnNames = columns.getColumnNames();

    /**
     * Parameterized constructor. Populates all values with specified values.
     */
    public Observation(long observationid, long receiverid, String iphase, double arrivaltime,
	    double timeuncertainty, double azimuth, double azuncertainty, double slowness,
	    double slowuncertainty, String auth) {
	setValues(observationid, receiverid, iphase, arrivaltime, timeuncertainty, azimuth,
		azuncertainty, slowness, slowuncertainty, auth);
    }

    private void setValues(long observationid, long receiverid, String iphase, double arrivaltime,
	    double timeuncertainty, double azimuth, double azuncertainty, double slowness,
	    double slowuncertainty, String auth) {
	this.observationid = observationid;
	this.receiverid = receiverid;
	this.iphase = iphase;
	this.arrivaltime = arrivaltime;
	this.timeuncertainty = timeuncertainty;
	this.azimuth = azimuth;
	this.azuncertainty = azuncertainty;
	this.slowness = slowness;
	this.slowuncertainty = slowuncertainty;
	this.auth = auth;
    }

    /**
     * Copy constructor.
     */
    public Observation(Observation other) {
	this.observationid = other.getObservationid();
	this.receiverid = other.getReceiverid();
	this.iphase = other.getIphase();
	this.arrivaltime = other.getArrivaltime();
	this.timeuncertainty = other.getTimeuncertainty();
	this.azimuth = other.getAzimuth();
	this.azuncertainty = other.getAzuncertainty();
	this.slowness = other.getSlowness();
	this.slowuncertainty = other.getSlowuncertainty();
	this.auth = other.getAuth();
    }

    /**
     * Default constructor that populates all values with na_values.
     */
    public Observation() {
	setDefaultValues();
    }

    private void setDefaultValues() {
	setValues(OBSERVATIONID_NA, RECEIVERID_NA, IPHASE_NA, ARRIVALTIME_NA, TIMEUNCERTAINTY_NA,
		AZIMUTH_NA, AZUNCERTAINTY_NA, SLOWNESS_NA, SLOWUNCERTAINTY_NA, AUTH_NA);
    }

    @Override
    public String getStringField(String name) throws IOException {
	switch (name) {
	case "iphase":
	    return iphase;
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
	case "iphase":
	    iphase = value;
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
	case "arrivaltime":
	    return arrivaltime;
	case "timeuncertainty":
	    return timeuncertainty;
	case "azimuth":
	    return azimuth;
	case "azuncertainty":
	    return azuncertainty;
	case "slowness":
	    return slowness;
	case "slowuncertainty":
	    return slowuncertainty;
	default:
	    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
		    + " is not a valid input name ...");
	}
    }

    @Override
    public void setDoubleField(String name, String input) throws IOException {
	double value = getInputDouble(input, name, this.getClass().getName());
	switch (name) {
	case "arrivaltime":
	    arrivaltime = value;
	    break;
	case "timeuncertainty":
	    timeuncertainty = value;
	    break;
	case "azimuth":
	    azimuth = value;
	    break;
	case "azuncertainty":
	    azuncertainty = value;
	    break;
	case "slowness":
	    slowness = value;
	    break;
	case "slowuncertainty":
	    slowuncertainty = value;
	    break;
	default:
	    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
		    + " is not a valid input name ...");
	}
    }

    @Override
    public long getLongField(String name) throws IOException {
	switch (name) {
	case "observationid":
	    return observationid;
	case "receiverid":
	    return receiverid;
	default:
	    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
		    + " is not a valid input name ...");
	}
    }

    @Override
    public void setLongField(String name, String input) throws IOException {
	long value = getInputLong(input, name, this.getClass().getName());
	switch (name) {
	case "observationid":
	    observationid = value;
	    break;
	case "receiverid":
	    receiverid = value;
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
    public Observation(Scanner input) throws IOException {
	setDefaultValues();
	String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
	setInputValues(inputs, inputColumnNames, columns);
    }

    /**
     * Constructor that loads values from a DataInputStream.
     */
    public Observation(DataInputStream input) throws IOException {
	this(input.readLong(), input.readLong(), readString(input), input.readDouble(),
		input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
		input.readDouble(), readString(input));
    }

    /**
     * Constructor that loads values from a ByteBuffer.
     */
    public Observation(ByteBuffer input) {
	this(input.getLong(), input.getLong(), readString(input), input.getDouble(), input.getDouble(),
		input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
		readString(input));
    }

    /**
     * Constructor that loads values from a ResultSet.
     */
    public Observation(ResultSet input) throws SQLException {
	this(input, 0);
    }

    /**
     * Constructor that loads values from a ResultSet.
     */
    public Observation(ResultSet input, int offset) throws SQLException {
	this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3),
		input.getTimestamp(offset + 4).getTime()*1e-3, 
		input.getDouble(offset + 5), input.getDouble(offset + 6),
		input.getDouble(offset + 7), input.getDouble(offset + 8), input.getDouble(offset + 9),
		input.getString(offset + 10));
    }

    /**
     * Write this row to an Object[] array.
     */
    public Object[] getValues() {
	Object values[] = new Object[10];
	values[0] = observationid;
	values[1] = receiverid;
	values[2] = iphase;
	values[3] = arrivaltime;
	values[4] = timeuncertainty;
	values[5] = azimuth;
	values[6] = azuncertainty;
	values[7] = slowness;
	values[8] = slowuncertainty;
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
	values[0] = observationid;
	values[1] = receiverid;
	values[2] = iphase;
	values[3] = arrivaltime;
	values[4] = timeuncertainty;
	values[5] = azimuth;
	values[6] = azuncertainty;
	values[7] = slowness;
	values[8] = slowuncertainty;
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
	output.writeLong(observationid);
	output.writeLong(receiverid);
	writeString(output, iphase);
	output.writeDouble(arrivaltime);
	output.writeDouble(timeuncertainty);
	output.writeDouble(azimuth);
	output.writeDouble(azuncertainty);
	output.writeDouble(slowness);
	output.writeDouble(slowuncertainty);
	writeString(output, auth);
    }

    /**
     * Write this row to a ByteBuffer.
     */
    public void write(ByteBuffer output) {
	output.putLong(observationid);
	output.putLong(receiverid);
	writeString(output, iphase);
	output.putDouble(arrivaltime);
	output.putDouble(timeuncertainty);
	output.putDouble(azimuth);
	output.putDouble(azuncertainty);
	output.putDouble(slowness);
	output.putDouble(slowuncertainty);
	writeString(output, auth);
    }

    /**
     * Read a Collection of Observation objects from an ascii BufferedReader.
     * <p>
     * The BufferedReader is closed after reading all the data it contains.
     * 
     * @param input
     * @param rows a Collection of Observation objects.
     * @throws IOException
     */
    static public void readObservations(BufferedReader input, Collection<Observation> rows)
	    throws IOException {
	String[] saved = Observation.getInputColumnNames();
	String line;
	int linesRead = 0;
	while ((line = input.readLine()) != null) {
	    line = line.trim();
	    ++linesRead;
	    if (line.startsWith("#") && linesRead == 1) {
		Observation
		.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
	    } else if (!line.startsWith("#"))
		rows.add(new Observation(new Scanner(line)));
	}
	input.close();
	Observation.setNewInputColumnNames(saved);
    }

    /**
     * Read a Collection of Observation objects from an ascii file. The Collection is not emptied
     * before reading.
     * 
     * @param inputFile
     * @param rows a Collection of Observation objects.
     * @throws IOException
     */
    static public void readObservations(File inputFile, Collection<Observation> rows)
	    throws IOException {
	readObservations(new BufferedReader(new FileReader(inputFile)), rows);
    }

    /**
     * Read a Collection of Observation objects from an ascii input stream. The Collection is not
     * emptied before reading.
     * 
     * @param inputStream
     * @param rows a Collection of Observation objects.
     * @throws IOException
     */
    static public void readObservations(InputStream inputStream, Collection<Observation> rows)
	    throws IOException {
	readObservations(new BufferedReader(new InputStreamReader(inputStream)), rows);
    }

    /**
     * Read a LinkedHashSet of Observation objects from an ascii BufferedReader.
     * 
     * @param input
     * @return a LinkedHashSet of Observation objects
     * @throws IOException
     */
    static public Set<Observation> readObservations(BufferedReader input) throws IOException {
	Set<Observation> rows = new LinkedHashSet<Observation>();
	readObservations(input, rows);
	return rows;
    }

    /**
     * Read a LinkedHashSet of Observation objects from an ascii file.
     * 
     * @param inputFile
     * @return a LinkedHashSet of Observation objects
     * @throws IOException
     */
    static public Set<Observation> readObservations(File inputFile) throws IOException {
	return readObservations(new BufferedReader(new FileReader(inputFile)));
    }

    /**
     * Read a LinkedHashSet of Observation objects from an ascii InputStream.
     * 
     * @param input
     * @return a LinkedHashSet of Observation objects
     * @throws IOException
     */
    static public Set<Observation> readObservations(InputStream input) throws IOException {
	return readObservations(new BufferedReader(new InputStreamReader(input)));
    }

    /**
     * Write a batch of Observation objects to an ascii file.
     * 
     * @param fileName name of file to write to.
     * @param observations the Observation objects to write
     * @throws IOException
     */
    static public void write(File fileName, Collection<? extends Observation> observations)
	    throws IOException {
	BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
	writeHeader(output);
	for (Observation observation : observations)
	    observation.writeln(output);
	output.close();
    }

    /**
     * Insert a batch of Observation objects into a database table.
     * 
     * @param connection database Connection object
     * @param tableName the name of the table into which the rows should be inserted
     * @param observations the Observation objects to insert
     * @param lddate the supplied load date is inserted at the end of the row.
     * @param commit if true, a commit is executed after all the rows have been inserted.
     * @throws SQLException
     */
    static public void write(Connection connection, String tableName,
	    Collection<? extends Observation> observations, java.util.Date lddate, boolean commit)
		    throws SQLException {
	PreparedStatement statement = null;
	try {
	    statement = connection
		    .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?)");
	    for (Observation observation : observations) {
		int i = 0;
		statement.setLong(++i, observation.observationid);
		statement.setLong(++i, observation.receiverid);
		statement.setString(++i, observation.iphase);
		statement.setTimestamp(++i, new Timestamp((long)(observation.arrivaltime*1000)));
		statement.setDouble(++i, observation.timeuncertainty);
		statement.setDouble(++i, observation.azimuth);
		statement.setDouble(++i, observation.azuncertainty);
		statement.setDouble(++i, observation.slowness);
		statement.setDouble(++i, observation.slowuncertainty);
		statement.setString(++i, observation.auth);
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
     *        Observation table.
     * @return data
     * @throws SQLException
     */
    static public HashSet<Observation> readObservations(Connection connection, String selectStatement)
	    throws SQLException {
	HashSet<Observation> results = new HashSet<Observation>();
	readObservations(connection, selectStatement, results);
	return results;
    }

    /**
     * Read data from the database.
     * 
     * @param connection
     * @param selectStatement a valid SQL select statement that returns a complete row from a
     *        Observation table.
     * @param observations
     * @throws SQLException
     */
    static public void readObservations(Connection connection, String selectStatement,
	    Set<Observation> observations) throws SQLException {
	Statement statement = null;
	ResultSet rs = null;
	try {
	    statement = connection.createStatement();
	    rs = statement.executeQuery(selectStatement);
	    while (rs.next())
		observations.add(new Observation(rs));
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
     * this Observation object into a database.
     * 
     * @param tableName name of the table into which the values will be inserted.
     * @return a String representation of a sql statement that can be used to insert the values of
     *         this Observation object into a database.
     */
    @Override
    public String getInsertSql(String tableName) {
	StringBuffer sql = new StringBuffer();
	sql.append("insert into ").append(tableName);
	sql.append(" (");
	sql.append(
		"observationid, receiverid, iphase, arrivaltime, timeuncertainty, azimuth, azuncertainty, slowness, slowuncertainty, auth, lddate");
	sql.append(")");
	sql.append(" values (");
	sql.append(Long.toString(observationid)).append(", ");
	sql.append(Long.toString(receiverid)).append(", ");
	sql.append("'").append(iphase).append("', ");
	sql.append(Double.toString(arrivaltime)).append(", ");
	sql.append(Double.toString(timeuncertainty)).append(", ");
	sql.append(Double.toString(azimuth)).append(", ");
	sql.append(Double.toString(azuncertainty)).append(", ");
	sql.append(Double.toString(slowness)).append(", ");
	sql.append(Double.toString(slowuncertainty)).append(", ");
	sql.append("'").append(auth).append("', ");
	sql.append("SYSDATE)");
	return sql.toString();
    }

    /**
     * Create a table of type Observation in the database. Primary and unique keys are set, if
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
     * Create a table of type Observation in the database
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
     * Generate a sql script to create a table of type Observation in the database Primary and unique
     * keys are set, if defined.
     * 
     * @param tableName
     * @throws SQLException
     */
    static public ArrayList<String> createTableScript(String tableName) throws SQLException {
	return createTableScript(tableName, true, true);
    }

    /**
     * Generate a sql script to create a table of type type Observation in the database
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
	buf.append("observationid number(10)           NOT NULL,\n");
	buf.append("receiverid   number(10)           NOT NULL,\n");
	buf.append("iphase       varchar2(2000)       NOT NULL,\n");
	buf.append("arrivaltime  timestamp(3)         NOT NULL,\n");
	buf.append("timeuncertainty float(126)           NOT NULL,\n");
	buf.append("azimuth      float(126)           NOT NULL,\n");
	buf.append("azuncertainty float(126)           NOT NULL,\n");
	buf.append("slowness     float(126)           NOT NULL,\n");
	buf.append("slowuncertainty float(126)           NOT NULL,\n");
	buf.append("auth         varchar2(64)         NOT NULL,\n");
	buf.append("lddate       date                 NOT NULL\n");
	buf.append(")");
	script.add(buf.toString());
	String[] tableNameParts = tableName.split("\\.");
	String constraint = tableNameParts[tableNameParts.length - 1];
	if (includePrimaryKeyConstraint)
	    script.add("alter table " + tableName + " add constraint " + constraint
		    + "_pk primary key (observationid)");
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
	return 2136;
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
	return (other instanceof Observation) && ((Observation) other).observationid == observationid;
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
    public Observation setObservationid(long observationid) {
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
    public Observation setReceiverid(long receiverid) {
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
     * @return iphase
     */
    public String getIphase() {
	return iphase;
    }

    /**
     * -
     * 
     * @param iphase
     * @throws IllegalArgumentException if iphase.length() >= 2000
     */
    public Observation setIphase(String iphase) {
	if (iphase.length() > 2000)
	    throw new IllegalArgumentException(
		    String.format("iphase.length() cannot be > 2000.  iphase=%s", iphase));
	this.iphase = iphase;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return arrivaltime
     */
    public double getArrivaltime() {
	return arrivaltime;
    }

    /**
     * -
     * 
     * @param arrivaltime
     */
    public Observation setArrivaltime(double arrivaltime) {
	this.arrivaltime = arrivaltime;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return timeuncertainty
     */
    public double getTimeuncertainty() {
	return timeuncertainty;
    }

    /**
     * -
     * 
     * @param timeuncertainty
     */
    public Observation setTimeuncertainty(double timeuncertainty) {
	this.timeuncertainty = timeuncertainty;
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
    public Observation setAzimuth(double azimuth) {
	this.azimuth = azimuth;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return azuncertainty
     */
    public double getAzuncertainty() {
	return azuncertainty;
    }

    /**
     * -
     * 
     * @param azuncertainty
     */
    public Observation setAzuncertainty(double azuncertainty) {
	this.azuncertainty = azuncertainty;
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
    public Observation setSlowness(double slowness) {
	this.slowness = slowness;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return slowuncertainty
     */
    public double getSlowuncertainty() {
	return slowuncertainty;
    }

    /**
     * -
     * 
     * @param slowuncertainty
     */
    public Observation setSlowuncertainty(double slowuncertainty) {
	this.slowuncertainty = slowuncertainty;
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
    public Observation setAuth(String auth) {
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

    private Receiver receiver;

    public Receiver getReceiver() {
	return receiver;
    }

    public void setReceiver(Receiver receiver) {
	this.receiver = receiver;
    }

    public static Map<Long, Observation> readObservations(Connection connection, Map<String, String> tableNames,
	    String sql) throws SQLException {

	Map<Long, Observation> observations = new TreeMap<Long, Observation>();

	for (Observation obs : Observation.readObservations(connection, sql)) 
	    observations.put(obs.observationid, obs);

	if (tableNames.containsKey("receiver")) {

	    Map<Long, Receiver> receivers = new TreeMap<>();
	    for (Observation obs : observations.values()) 
		receivers.put(obs.getReceiverid(), null);

	    String r = "";
	    for (Long receiverid : receivers.keySet())
		r += ","+receiverid;

	    sql = "where receiverid in ("+r.substring(1)+")";
	    sql = String.format("select * from %s %s", tableNames.get("receiver"), sql);

	    Map<Long, Receiver> rc = Receiver.readReceivers(connection, sql);

	    for (Observation obs : observations.values())
		obs.setReceiver(receivers.get(obs.getReceiverid()));
	}

	return observations;
    }

    public TestBuffer getTestBuffer() {
    	TestBuffer buffer = new TestBuffer(this.getClass().getSimpleName());
	buffer.add("gmp.observation.observationid", observationid);
	buffer.add("gmp.observation.receiverid", receiverid);
	buffer.add("gmp.observation.iphase", iphase);
	buffer.add("gmp.observation.arrivaltime", arrivaltime);
	buffer.add("gmp.observation.timeuncertainty", timeuncertainty);
	buffer.add("gmp.observation.azimuth", azimuth);
	buffer.add("gmp.observation.azuncertainty", azuncertainty);
	buffer.add("gmp.observation.slowness", slowness);
	buffer.add("gmp.observation.slowuncertainty", slowuncertainty);
	buffer.add("gmp.observation.auth", auth);

	buffer.add("gmp.observation.nReceivers", (receiver == null ? 0 : 1));
	buffer.add();
	
	if (receiver != null)
	    buffer.add(receiver.getTestBuffer());

	return buffer;
    }

}
