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
package gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import gov.sandia.gnem.dbtabledefs.BaseRow;


public class ArrayAperture extends BaseRow {


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
     * Aperture of the array (in km)
     */
    private double aperture_km;

    static final public double APERTURE_KM_NA = -999;

    /**
     * Aperture of the array (in deg)
     */
    private double aperture_deg;

    static final public double APERTURE_DEG_NA = -999;

    /**
     * Spacing of the array (in km)
     */
    private double spacing_km;

    static final public double SPACING_KM_NA = -999;

    /**
     * Spacing of the array (in deg)
     */
    private double spacing_deg;

    static final public double SPACING_DEG_NA = -999;

    /**
     * Station name/Description. This value is the full name of the station whose code name is in
     * <I>sta</I> [for example, one record in the <B>site</B> table connects <I>sta</I> = ANMO to
     * staname = ALBUQUERQUE, NEW MEXICO (SRO)].
     */
    private String staname;

    static final public String STANAME_NA = "-";


    /**
     * An ordered set of the column names represented by this class. Does not include lddate.
     */
    public static final LinkedHashSet<String> columnNames;

    static {
        columnNames = new LinkedHashSet<String>(11);
        columnNames.add("sta");
        columnNames.add("ondate");
        columnNames.add("offdate");
        columnNames.add("aperture_km");
        columnNames.add("aperture_deg");
        columnNames.add("spacing_km");
        columnNames.add("spacing_deg");
        columnNames.add("staname");
    }

    /**
     * An ordered set of the column names represented by this class. Does not include lddate.
     */
    public LinkedHashSet<String> getColumnNames() {
        return ArrayAperture.columnNames;
    }

    /**
     * Parameterized constructor. Populates all values with specified values.
     */
    public ArrayAperture(String sta, long ondate, long offdate, double aperture_km,
                         double aperture_deg, double spacing_km, double spacing_deg, String staname) {
        this.sta = sta;
        this.ondate = ondate;
        this.offdate = offdate;
        this.aperture_km = aperture_km;
        this.aperture_deg = aperture_deg;
        this.spacing_km = spacing_km;
        this.spacing_deg = spacing_deg;
        this.staname = staname;
    }

    /**
     * Copy constructor.
     */
    public ArrayAperture(ArrayAperture other) {
        this.sta = other.getSta();
        this.ondate = other.getOndate();
        this.offdate = other.getOffdate();
        this.aperture_km = other.getAperture_km();
        this.aperture_deg = other.getAperture_deg();
        this.spacing_km = other.getSpacing_km();
        this.spacing_deg = other.getSpacing_deg();
        this.staname = other.getStaname();
    }

    /**
     * Default constructor that populates all values with na_values.
     */
    public ArrayAperture() {
        this(STA_NA, ONDATE_NA, OFFDATE_NA, APERTURE_KM_NA, APERTURE_DEG_NA, SPACING_KM_NA,
                SPACING_DEG_NA, STANAME_NA);
    }

    /**
     * Constructor that loads values from a ResultSet.
     */
    public ArrayAperture(ResultSet input) throws SQLException {
        this(input, 0);
    }

    /**
     * Constructor that loads values from a ResultSet.
     */
    public ArrayAperture(ResultSet input, int offset) throws SQLException {
        this(input.getString(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
                input.getDouble(offset + 4), input.getDouble(offset + 5), input.getDouble(offset + 6),
                input.getDouble(offset + 7), input.getString(offset + 8));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        ArrayAperture x = (ArrayAperture) o;
        return x.sta.equals(sta) && x.ondate == ondate && x.offdate == offdate && x.aperture_km == aperture_km &&
                x.aperture_deg == aperture_deg && x.spacing_km == spacing_km && x.spacing_deg == spacing_deg &&
                x.staname.equals(staname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sta, ondate, offdate, aperture_km, aperture_deg, spacing_km, spacing_deg, staname);
    }

    /**
     * Write this row to an Object[] array.
     */
    public Object[] getValues() {
        Object values[] = new Object[11];
        values[0] = sta;
        values[1] = ondate;
        values[2] = offdate;
        values[3] = aperture_km;
        values[4] = aperture_deg;
        values[5] = spacing_km;
        values[6] = spacing_deg;
        values[7] = staname;
        return values;
    }

    /**
     * Write this row to an Object[] array with load date appended.
     *
     * @param lddate load date
     */
    @Override
    public Object[] getValues(Date lddate) {
        Object values[] = new Object[11];
        values[0] = sta;
        values[1] = ondate;
        values[2] = offdate;
        values[3] = aperture_km;
        values[4] = aperture_deg;
        values[5] = spacing_km;
        values[6] = spacing_deg;
        values[7] = staname;
        return values;
    }

    /**
     * Write this row to an Object[] array with load date appended.
     * <p>
     * The supplied java.util.Date is converted to a java.sql.Date in the output.
     *
     * @param lddate load date
     */
    public Object[] getValues(java.util.Date lddate) {
        return getValues(new java.sql.Timestamp(lddate.getTime()));
    }

    /**
     * Write this row to a DataOutputStream.
     */
    public void write(DataOutputStream output) throws IOException {
        writeString(output, sta);
        output.writeLong(ondate);
        output.writeLong(offdate);
        output.writeDouble(aperture_km);
        output.writeDouble(aperture_deg);
        output.writeDouble(spacing_km);
        output.writeDouble(spacing_deg);
        writeString(output, staname);
    }

    /**
     * Write this row to a ByteBuffer.
     */
    public void write(ByteBuffer output) {
        writeString(output, sta);
        output.putLong(ondate);
        output.putLong(offdate);
        output.putDouble(aperture_km);
        output.putDouble(aperture_deg);
        output.putDouble(spacing_km);
        output.putDouble(spacing_deg);
        writeString(output, staname);
    }

    /**
     * @param connection
     * @param arrayApertureTable
     * @param arrayApertures
     * @throws SQLException
     */
    static public void readArrayApertures(Connection connection, String arrayApertureTable,
                                          Set<ArrayAperture> arrayApertures) throws SQLException {
        Statement statement = null;
        ResultSet rs = null;
        String sql = String.format("select * from %s", arrayApertureTable);

        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            while (rs.next())
                arrayApertures.add(new ArrayAperture(rs));
        } catch (Exception e) {
            throw new SQLException(e);
        } finally {
            if (rs != null)
                rs.close();
            if (statement != null)
                statement.close();
        }
    }

    /**
     * Retrieve a String representation of a sql statement that can be used to insert the values of
     * this ArrayAperture object into a database.
     *
     * @param tableName name of the table into which the values will be inserted.
     * @return a String representation of a sql statement that can be used to insert the values of
     * this ArrayAperture object into a database.
     */
    @Override
    public String getInsertSql(String tableName) {
        StringBuffer sql = new StringBuffer();
        sql.append("insert into ").append(tableName).append(" values (");
        sql.append("'").append(sta).append("', ");
        sql.append(Long.toString(ondate)).append(", ");
        sql.append(Long.toString(offdate)).append(", ");
        sql.append(Double.toString(aperture_km)).append(", ");
        sql.append(Double.toString(aperture_deg)).append(", ");
        sql.append(Double.toString(spacing_km)).append(", ");
        sql.append(Double.toString(spacing_deg)).append(", ");
        sql.append("'").append(staname).append("', ");
        sql.append("SYSDATE)");
        return sql.toString();
    }

    /**
     * Create a table of type ArrayAperture in the database
     *
     * @param connection
     * @param tableName
     * @throws SQLException
     */
    static public void createTable(Connection connection, String tableName) throws SQLException {
        Statement statement = connection.createStatement();
        for (String s : createTableScript(tableName))
            statement.execute(s);
        statement.close();
    }


    /**
     * Generate a sql script to create a table of type ArrayAperture in the database
     *
     * @param tableName
     * @throws SQLException
     */
    static public ArrayList<String> createTableScript(String tableName) throws SQLException {
        ArrayList<String> script = new ArrayList<String>();
        StringBuffer buf = new StringBuffer();
        buf.append("create table " + tableName + " (\n");
        buf.append("sta          varchar2(6)          NOT NULL,\n");
        buf.append("ondate       number(8)            NOT NULL,\n");
        buf.append("offdate      number(8)            NOT NULL,\n");
        buf.append("aperture_km  float(53)            NOT NULL,\n");
        buf.append("aperture_deg float(53)            NOT NULL,\n");
        buf.append("spacing_km   float(53)            NOT NULL,\n");
        buf.append("spacing_deg  float(53)            NOT NULL,\n");
        buf.append("staname      varchar2(50)         NOT NULL,\n");
        buf.append("lddate       date                 NOT NULL,\n");
        buf.append("constraint " + tableName + "_pk primary key (sta,ondate)\n");
        buf.append(")");
        script.add(buf.toString());
        script.add("grant select on " + tableName + " to public");
        return script;
    }

    /**
     * Write this row to an ascii String with no newline at the end.
     */
    @Override
    public String toString() {
        return String.format("\"%s\" %8d %8d %11.6f %11.6f %11.6f %11.6f \"%s\"", sta, ondate, offdate,
                aperture_km, aperture_deg, spacing_km, spacing_deg, staname);
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
     * Maximum number of bytes required to store an instance of this in a ByteBuffer or
     * DataOutputStream.
     */
    public int maxBytes() {
        return 112;
    }


    public String getSta() {
        return sta;
    }

    public ArrayAperture setSta(String sta) {
        this.sta = sta;
        //setHash(null);
        return this;
    }

    public long getOndate() {
        return ondate;
    }

    public ArrayAperture setOndate(long ondate) {
        this.ondate = ondate;
        //setHash(null);
        return this;
    }

    public long getOffdate() {
        return offdate;
    }

    public ArrayAperture setOffdate(long offdate) {
        this.offdate = offdate;
        //setHash(null);
        return this;
    }

    public double getAperture_km() {
        return aperture_km;
    }

    public ArrayAperture setAperture_km(double aperture_km) {
        this.aperture_km = aperture_km;
        //setHash(null);
        return this;
    }

    public double getAperture_deg() {
        return aperture_deg;
    }

    public ArrayAperture setAperture_deg(double aperture_deg) {
        this.aperture_deg = aperture_deg;
        //setHash(null);
        return this;
    }

    public double getSpacing_km() {
        return spacing_km;
    }

    public ArrayAperture setSpacing_km(double spacing_km) {
        this.spacing_km = spacing_km;
        //setHash(null);
        return this;
    }

    public double getSpacing_deg() {
        return spacing_deg;
    }

    public ArrayAperture setSpacing_deg(double spacing_deg) {
        this.spacing_deg = spacing_deg;
        //setHash(null);
        return this;
    }

    public String getStaname() {
        return staname;
    }

    public ArrayAperture setStaname(String staname) {
        this.staname = staname;
        //setHash(null);
        return this;
    }

    @Override
    public String getStringField(String name) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getDoubleField(String name) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getLongField(String name) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setStringField(String name, String input) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDoubleField(String name, String input) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setLongField(String name, String input) throws IOException {
        // TODO Auto-generated method stub

    }
}
