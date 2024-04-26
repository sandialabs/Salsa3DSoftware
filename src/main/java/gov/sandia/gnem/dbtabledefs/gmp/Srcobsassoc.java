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
public class Srcobsassoc extends BaseRow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * -
     */
    private long sourceid;

    static final public long SOURCEID_NA = Long.MIN_VALUE;

    /**
     * -
     */
    private long observationid;

    static final public long OBSERVATIONID_NA = Long.MIN_VALUE;

    /**
     * -
     */
    private String phase;

    static final public String PHASE_NA = null;

    /**
     * -
     */
    private double delta;

    static final public double DELTA_NA = Double.NaN;

    /**
     * -
     */
    private double esaz;

    static final public double ESAZ_NA = Double.NaN;

    /**
     * -
     */
    private double seaz;

    static final public double SEAZ_NA = Double.NaN;

    /**
     * -
     */
    private String timedef;

    static final public String TIMEDEF_NA = null;

    /**
     * -
     */
    private String azdef;

    static final public String AZDEF_NA = null;

    /**
     * -
     */
    private String slowdef;

    static final public String SLOWDEF_NA = null;

    /**
     * -
     */
    private String auth;

    static final public String AUTH_NA = null;


    private static final Columns columns;
    static {
	columns = new Columns();
	columns.add("sourceid", Columns.FieldType.LONG, "%d");
	columns.add("observationid", Columns.FieldType.LONG, "%d");
	columns.add("phase", Columns.FieldType.STRING, "%s");
	columns.add("delta", Columns.FieldType.DOUBLE, "%10.6f");
	columns.add("esaz", Columns.FieldType.DOUBLE, "%8.3f");
	columns.add("seaz", Columns.FieldType.DOUBLE, "%8.3f");
	columns.add("timedef", Columns.FieldType.STRING, "%s");
	columns.add("azdef", Columns.FieldType.STRING, "%s");
	columns.add("slowdef", Columns.FieldType.STRING, "%s");
	columns.add("auth", Columns.FieldType.STRING, "%s");
    }

    private static String[] inputColumnNames = columns.getColumnNames();
    private static String[] outputColumnNames = columns.getColumnNames();

    /**
     * Parameterized constructor. Populates all values with specified values.
     */
    public Srcobsassoc(long sourceid, long observationid, String phase, double delta, double esaz,
	    double seaz, String timedef, String azdef, String slowdef, String auth) {
	setValues(sourceid, observationid, phase, delta, esaz, seaz, timedef, azdef, slowdef, auth);
    }

    private void setValues(long sourceid, long observationid, String phase, double delta, double esaz,
	    double seaz, String timedef, String azdef, String slowdef, String auth) {
	this.sourceid = sourceid;
	this.observationid = observationid;
	this.phase = phase;
	this.delta = delta;
	this.esaz = esaz;
	this.seaz = seaz;
	this.timedef = timedef;
	this.azdef = azdef;
	this.slowdef = slowdef;
	this.auth = auth;
    }

    /**
     * Copy constructor.
     */
    public Srcobsassoc(Srcobsassoc other) {
	this.sourceid = other.getSourceid();
	this.observationid = other.getObservationid();
	this.phase = other.getPhase();
	this.delta = other.getDelta();
	this.esaz = other.getEsaz();
	this.seaz = other.getSeaz();
	this.timedef = other.getTimedef();
	this.azdef = other.getAzdef();
	this.slowdef = other.getSlowdef();
	this.auth = other.getAuth();
    }

    /**
     * Default constructor that populates all values with na_values.
     */
    public Srcobsassoc() {
	setDefaultValues();
    }

    private void setDefaultValues() {
	setValues(SOURCEID_NA, OBSERVATIONID_NA, PHASE_NA, DELTA_NA, ESAZ_NA, SEAZ_NA, TIMEDEF_NA,
		AZDEF_NA, SLOWDEF_NA, AUTH_NA);
    }

    @Override
    public String getStringField(String name) throws IOException {
	switch (name) {
	case "phase":
	    return phase;
	case "timedef":
	    return timedef;
	case "azdef":
	    return azdef;
	case "slowdef":
	    return slowdef;
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
	case "timedef":
	    timedef = value;
	    break;
	case "azdef":
	    azdef = value;
	    break;
	case "slowdef":
	    slowdef = value;
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
	case "delta":
	    return delta;
	case "esaz":
	    return esaz;
	case "seaz":
	    return seaz;
	default:
	    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
		    + " is not a valid input name ...");
	}
    }

    @Override
    public void setDoubleField(String name, String input) throws IOException {
	double value = getInputDouble(input, name, this.getClass().getName());
	switch (name) {
	case "delta":
	    delta = value;
	    break;
	case "esaz":
	    esaz = value;
	    break;
	case "seaz":
	    seaz = value;
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
	case "sourceid":
	    sourceid = value;
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
    public Srcobsassoc(Scanner input) throws IOException {
	setDefaultValues();
	String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
	setInputValues(inputs, inputColumnNames, columns);
    }

    /**
     * Constructor that loads values from a DataInputStream.
     */
    public Srcobsassoc(DataInputStream input) throws IOException {
	this(input.readLong(), input.readLong(), readString(input), input.readDouble(),
		input.readDouble(), input.readDouble(), readString(input), readString(input),
		readString(input), readString(input));
    }

    /**
     * Constructor that loads values from a ByteBuffer.
     */
    public Srcobsassoc(ByteBuffer input) {
	this(input.getLong(), input.getLong(), readString(input), input.getDouble(), input.getDouble(),
		input.getDouble(), readString(input), readString(input), readString(input),
		readString(input));
    }

    /**
     * Constructor that loads values from a ResultSet.
     */
    public Srcobsassoc(ResultSet input) throws SQLException {
	this(input, 0);
    }

    /**
     * Constructor that loads values from a ResultSet.
     */
    public Srcobsassoc(ResultSet input, int offset) throws SQLException {
	this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3),
		input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
		input.getString(offset + 7), input.getString(offset + 8), input.getString(offset + 9),
		input.getString(offset + 10));
    }

    /**
     * Write this row to an Object[] array.
     */
    public Object[] getValues() {
	Object values[] = new Object[10];
	values[0] = sourceid;
	values[1] = observationid;
	values[2] = phase;
	values[3] = delta;
	values[4] = esaz;
	values[5] = seaz;
	values[6] = timedef;
	values[7] = azdef;
	values[8] = slowdef;
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
	values[1] = observationid;
	values[2] = phase;
	values[3] = delta;
	values[4] = esaz;
	values[5] = seaz;
	values[6] = timedef;
	values[7] = azdef;
	values[8] = slowdef;
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
	output.writeLong(observationid);
	writeString(output, phase);
	output.writeDouble(delta);
	output.writeDouble(esaz);
	output.writeDouble(seaz);
	writeString(output, timedef);
	writeString(output, azdef);
	writeString(output, slowdef);
	writeString(output, auth);
    }

    /**
     * Write this row to a ByteBuffer.
     */
    public void write(ByteBuffer output) {
	output.putLong(sourceid);
	output.putLong(observationid);
	writeString(output, phase);
	output.putDouble(delta);
	output.putDouble(esaz);
	output.putDouble(seaz);
	writeString(output, timedef);
	writeString(output, azdef);
	writeString(output, slowdef);
	writeString(output, auth);
    }

    /**
     * Read a Collection of Srcobsassoc objects from an ascii BufferedReader.
     * <p>
     * The BufferedReader is closed after reading all the data it contains.
     * 
     * @param input
     * @param rows a Collection of Srcobsassoc objects.
     * @throws IOException
     */
    static public void readSrcobsassocs(BufferedReader input, Collection<Srcobsassoc> rows)
	    throws IOException {
	String[] saved = Srcobsassoc.getInputColumnNames();
	String line;
	int linesRead = 0;
	while ((line = input.readLine()) != null) {
	    line = line.trim();
	    ++linesRead;
	    if (line.startsWith("#") && linesRead == 1) {
		Srcobsassoc
		.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
	    } else if (!line.startsWith("#"))
		rows.add(new Srcobsassoc(new Scanner(line)));
	}
	input.close();
	Srcobsassoc.setNewInputColumnNames(saved);
    }

    /**
     * Read a Collection of Srcobsassoc objects from an ascii file. The Collection is not emptied
     * before reading.
     * 
     * @param inputFile
     * @param rows a Collection of Srcobsassoc objects.
     * @throws IOException
     */
    static public void readSrcobsassocs(File inputFile, Collection<Srcobsassoc> rows)
	    throws IOException {
	readSrcobsassocs(new BufferedReader(new FileReader(inputFile)), rows);
    }

    /**
     * Read a Collection of Srcobsassoc objects from an ascii input stream. The Collection is not
     * emptied before reading.
     * 
     * @param inputStream
     * @param rows a Collection of Srcobsassoc objects.
     * @throws IOException
     */
    static public void readSrcobsassocs(InputStream inputStream, Collection<Srcobsassoc> rows)
	    throws IOException {
	readSrcobsassocs(new BufferedReader(new InputStreamReader(inputStream)), rows);
    }

    /**
     * Read a LinkedHashSet of Srcobsassoc objects from an ascii BufferedReader.
     * 
     * @param input
     * @return a LinkedHashSet of Srcobsassoc objects
     * @throws IOException
     */
    static public Set<Srcobsassoc> readSrcobsassocs(BufferedReader input) throws IOException {
	Set<Srcobsassoc> rows = new LinkedHashSet<Srcobsassoc>();
	readSrcobsassocs(input, rows);
	return rows;
    }

    /**
     * Read a LinkedHashSet of Srcobsassoc objects from an ascii file.
     * 
     * @param inputFile
     * @return a LinkedHashSet of Srcobsassoc objects
     * @throws IOException
     */
    static public Set<Srcobsassoc> readSrcobsassocs(File inputFile) throws IOException {
	return readSrcobsassocs(new BufferedReader(new FileReader(inputFile)));
    }

    /**
     * Read a LinkedHashSet of Srcobsassoc objects from an ascii InputStream.
     * 
     * @param input
     * @return a LinkedHashSet of Srcobsassoc objects
     * @throws IOException
     */
    static public Set<Srcobsassoc> readSrcobsassocs(InputStream input) throws IOException {
	return readSrcobsassocs(new BufferedReader(new InputStreamReader(input)));
    }

    /**
     * Write a batch of Srcobsassoc objects to an ascii file.
     * 
     * @param fileName name of file to write to.
     * @param srcobsassocs the Srcobsassoc objects to write
     * @throws IOException
     */
    static public void write(File fileName, Collection<? extends Srcobsassoc> srcobsassocs)
	    throws IOException {
	BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
	writeHeader(output);
	for (Srcobsassoc srcobsassoc : srcobsassocs)
	    srcobsassoc.writeln(output);
	output.close();
    }

    /**
     * Insert a batch of Srcobsassoc objects into a database table.
     * 
     * @param connection database Connection object
     * @param tableName the name of the table into which the rows should be inserted
     * @param srcobsassocs the Srcobsassoc objects to insert
     * @param lddate the supplied load date is inserted at the end of the row.
     * @param commit if true, a commit is executed after all the rows have been inserted.
     * @throws SQLException
     */
    static public void write(Connection connection, String tableName,
	    Collection<? extends Srcobsassoc> srcobsassocs, java.util.Date lddate, boolean commit)
		    throws SQLException {
	PreparedStatement statement = null;
	try {
	    statement = connection
		    .prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?)");
	    for (Srcobsassoc srcobsassoc : srcobsassocs) {
		int i = 0;
		statement.setLong(++i, srcobsassoc.sourceid);
		statement.setLong(++i, srcobsassoc.observationid);
		statement.setString(++i, srcobsassoc.phase);
		statement.setDouble(++i, srcobsassoc.delta);
		statement.setDouble(++i, srcobsassoc.esaz);
		statement.setDouble(++i, srcobsassoc.seaz);
		statement.setString(++i, srcobsassoc.timedef);
		statement.setString(++i, srcobsassoc.azdef);
		statement.setString(++i, srcobsassoc.slowdef);
		statement.setString(++i, srcobsassoc.auth);
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
     *        Srcobsassoc table.
     * @return data
     * @throws SQLException
     */
    static public HashSet<Srcobsassoc> readSrcobsassocs(Connection connection, String selectStatement)
	    throws SQLException {
	HashSet<Srcobsassoc> results = new HashSet<Srcobsassoc>();
	readSrcobsassocs(connection, selectStatement, results);
	return results;
    }

    /**
     * Read data from the database.
     * 
     * @param connection
     * @param selectStatement a valid SQL select statement that returns a complete row from a
     *        Srcobsassoc table.
     * @param srcobsassocs
     * @throws SQLException
     */
    static public void readSrcobsassocs(Connection connection, String selectStatement,
	    Set<Srcobsassoc> srcobsassocs) throws SQLException {
	Statement statement = null;
	ResultSet rs = null;
	try {
	    statement = connection.createStatement();
	    rs = statement.executeQuery(selectStatement);
	    while (rs.next())
		srcobsassocs.add(new Srcobsassoc(rs));
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
     * this Srcobsassoc object into a database.
     * 
     * @param tableName name of the table into which the values will be inserted.
     * @return a String representation of a sql statement that can be used to insert the values of
     *         this Srcobsassoc object into a database.
     */
    @Override
    public String getInsertSql(String tableName) {
	StringBuffer sql = new StringBuffer();
	sql.append("insert into ").append(tableName);
	sql.append(" (");
	sql.append(
		"sourceid, observationid, phase, delta, esaz, seaz, timedef, azdef, slowdef, auth, lddate");
	sql.append(")");
	sql.append(" values (");
	sql.append(Long.toString(sourceid)).append(", ");
	sql.append(Long.toString(observationid)).append(", ");
	sql.append("'").append(phase).append("', ");
	sql.append(Double.toString(delta)).append(", ");
	sql.append(Double.toString(esaz)).append(", ");
	sql.append(Double.toString(seaz)).append(", ");
	sql.append("'").append(timedef).append("', ");
	sql.append("'").append(azdef).append("', ");
	sql.append("'").append(slowdef).append("', ");
	sql.append("'").append(auth).append("', ");
	sql.append("SYSDATE)");
	return sql.toString();
    }

    /**
     * Create a table of type Srcobsassoc in the database. Primary and unique keys are set, if
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
     * Create a table of type Srcobsassoc in the database
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
     * Generate a sql script to create a table of type Srcobsassoc in the database Primary and unique
     * keys are set, if defined.
     * 
     * @param tableName
     * @throws SQLException
     */
    static public ArrayList<String> createTableScript(String tableName) throws SQLException {
	return createTableScript(tableName, true, true);
    }

    /**
     * Generate a sql script to create a table of type type Srcobsassoc in the database
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
	buf.append("observationid number(10)           NOT NULL,\n");
	buf.append("phase        varchar2(30)         NOT NULL,\n");
	buf.append("delta        float(126)           NOT NULL,\n");
	buf.append("esaz         float(126)           NOT NULL,\n");
	buf.append("seaz         float(126)           NOT NULL,\n");
	buf.append("timedef      varchar2(1)          NOT NULL,\n");
	buf.append("azdef        varchar2(1)          NOT NULL,\n");
	buf.append("slowdef      varchar2(1)          NOT NULL,\n");
	buf.append("auth         varchar2(64)         NOT NULL,\n");
	buf.append("lddate       date                 NOT NULL\n");
	buf.append(")");
	script.add(buf.toString());
	String[] tableNameParts = tableName.split("\\.");
	String constraint = tableNameParts[tableNameParts.length - 1];
	if (includePrimaryKeyConstraint)
	    script.add("alter table " + tableName + " add constraint " + constraint
		    + "_pk primary key (sourceid,observationid)");
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
	return 157;
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
	return (other instanceof Srcobsassoc) && ((Srcobsassoc) other).sourceid == sourceid
		&& ((Srcobsassoc) other).observationid == observationid;
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
    public Srcobsassoc setSourceid(long sourceid) {
	if (sourceid >= 10000000000L)
	    throw new IllegalArgumentException("sourceid=" + sourceid + " but cannot be >= 10000000000");
	this.sourceid = sourceid;
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
    public Srcobsassoc setObservationid(long observationid) {
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
    public Srcobsassoc setPhase(String phase) {
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
     * @return delta
     */
    public double getDelta() {
	return delta;
    }

    /**
     * -
     * 
     * @param delta
     */
    public Srcobsassoc setDelta(double delta) {
	this.delta = delta;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return esaz
     */
    public double getEsaz() {
	return esaz;
    }

    /**
     * -
     * 
     * @param esaz
     */
    public Srcobsassoc setEsaz(double esaz) {
	this.esaz = esaz;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return seaz
     */
    public double getSeaz() {
	return seaz;
    }

    /**
     * -
     * 
     * @param seaz
     */
    public Srcobsassoc setSeaz(double seaz) {
	this.seaz = seaz;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return timedef
     */
    public String getTimedef() {
	return timedef;
    }

    /**
     * -
     * 
     * @param timedef
     * @throws IllegalArgumentException if timedef.length() >= 1
     */
    public Srcobsassoc setTimedef(String timedef) {
	if (timedef.length() > 1)
	    throw new IllegalArgumentException(
		    String.format("timedef.length() cannot be > 1.  timedef=%s", timedef));
	this.timedef = timedef;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return azdef
     */
    public String getAzdef() {
	return azdef;
    }

    /**
     * -
     * 
     * @param azdef
     * @throws IllegalArgumentException if azdef.length() >= 1
     */
    public Srcobsassoc setAzdef(String azdef) {
	if (azdef.length() > 1)
	    throw new IllegalArgumentException(
		    String.format("azdef.length() cannot be > 1.  azdef=%s", azdef));
	this.azdef = azdef;
	setHash(null);
	return this;
    }

    /**
     * -
     * 
     * @return slowdef
     */
    public String getSlowdef() {
	return slowdef;
    }

    /**
     * -
     * 
     * @param slowdef
     * @throws IllegalArgumentException if slowdef.length() >= 1
     */
    public Srcobsassoc setSlowdef(String slowdef) {
	if (slowdef.length() > 1)
	    throw new IllegalArgumentException(
		    String.format("slowdef.length() cannot be > 1.  slowdef=%s", slowdef));
	this.slowdef = slowdef;
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
    public Srcobsassoc setAuth(String auth) {
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

    protected Source source;
    public Source getSource() {
	return source;
    }

    public void setSource(Source source) {
	this.source = source;
    }

    protected Observation observation;

    public Observation getObservation() {
	return observation;
    }

    public void setObservation(Observation observation) {
	this.observation = observation;
    }

    public static Map<Long, Srcobsassoc> readSrcobsassocs(Connection connection, Map<String, String> tableNames,
	    String sql) throws SQLException {

	Map<Long, Srcobsassoc> soas = new TreeMap<>();

	for (Srcobsassoc soa : Srcobsassoc.readSrcobsassocs(connection, sql))
	    soas.put(soa.getObservationid(), soa);

	if (tableNames.containsKey("observation")) {
	    sql = "";
	    for (Long obsid : soas.keySet())
		sql += ","+obsid;

	    sql = "where observationid in ("+sql.substring(1)+")";
	    sql = String.format("select * from %s %s", tableNames.get("observation"), sql);
	    
	    Map<Long, Observation> observations = Observation.readObservations(connection, tableNames, sql);
	    
	    for (Srcobsassoc soa : soas.values())
		soa.setObservation(observations.get(soa.getObservationid()));
	}

	return soas;
    }

    public TestBuffer getTestBuffer() {
    	TestBuffer buffer = new TestBuffer(this.getClass().getSimpleName());
	buffer.add("gmp.srcobsassoc.sourceid", sourceid);
	buffer.add("gmp.srcobsassoc.observationid", observationid);
	buffer.add("gmp.srcobsassoc.phase", phase);
	buffer.add("gmp.srcobsassoc.delta", delta);
	buffer.add("gmp.srcobsassoc.esaz", esaz);
	buffer.add("gmp.srcobsassoc.seaz", seaz);
	buffer.add("gmp.srcobsassoc.timedef", timedef);
	buffer.add("gmp.srcobsassoc.azdef", azdef);
	buffer.add("gmp.srcobsassoc.slowdef", slowdef);
	buffer.add("gmp.srcobsassoc.auth", auth);

	buffer.add("gmp.srcobsassoc.nObservations", (observation == null ? 0 : 1));
	buffer.add();
	if (observation != null)
	    buffer.add(observation.getTestBuffer());

	return buffer;
    }
}
