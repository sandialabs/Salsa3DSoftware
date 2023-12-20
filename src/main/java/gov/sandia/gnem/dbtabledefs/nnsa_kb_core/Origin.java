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

import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * origin
 */
public class Origin extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

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
   * Source depth. This column gives the depth (positive down) of the event origin. Negative depth
   * implies an atmospheric event.
   * <p>
   * Units: km
   */
  private double depth;

  static final public double DEPTH_NA = -999;

  /**
   * Epoch time, given as seconds since midnight, January 1, 1970, and stored in a double-precision
   * floating number. Time refers to the table in which it is found [for example, in <B>arrival</B>
   * it is the arrival time, in <B>origin</B> it is the origin time, and in <B>wfdisc</B> is the
   * start time of data]. Where the date of historical events is known, time is set to the start
   * time of that date. Where the date of contemporary arrival measurements is known but no time is
   * given, then time is set to the NA Value. The double-precision floating point number allows 15
   * decimal digits. At one millisecond accuracy, this is a range of 3.0e+4 years. Where the date is
   * unknown or prior to February 10, 1653, time is set to the NA Value.
   * <p>
   * Units: s
   */
  private double time;

  static final public double TIME_NA = -9999999999.999;

  /**
   * Origin identifier that relates a record in these tables to a record in the <B>origin</B> table
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Event identifier. Each event is assigned a unique positive integer that identifies it in a
   * database. Several records in the <B>origin</B> table can have the same <I>evid</I>. Analysts
   * have several opinions about the location of the event.
   */
  private long evid;

  static final public long EVID_NA = -1;

  /**
   * Julian date. Date of an arrival, origin, seismic recording, etc. The same information is
   * available in epoch time, but the Julian date format is more convenient for many types of
   * searches. Dates B.C. are negative. The year will never equal 0000, and the day will never equal
   * 000. Where only the year is known, the day of the year is 001; where only year and month are
   * known, the day of year is the first day of the month. Only the year is negated for B.C., so 1
   * January of 10 B.C. is -0010001 (see <I>time</I>).
   */
  private long jdate;

  static final public long JDATE_NA = -1;

  /**
   * Number of associated arrivals. This column gives the number of arrivals associated with the
   * origin
   */
  private long nass;

  static final public long NASS_NA = -1;

  /**
   * Number of time-defining phases
   */
  private long ndef;

  static final public long NDEF_NA = -1;

  /**
   * Number of depth phases. This column gives the number of depth phases used in calculating
   * depth/depdp (see depdp)
   */
  private long ndp;

  static final public long NDP_NA = -1;

  /**
   * Geographic region number.
   */
  private long grn;

  static final public long GRN_NA = -1;

  /**
   * Seismic region number (see <I>grn</I>).
   */
  private long srn;

  static final public long SRN_NA = -1;

  /**
   * An event type that is used to identify the type of seismic event, when known. The recommended
   * event types are: ex generic explosion ec chemical explosion ep probable explosion en nuclear
   * explosion mc collapse me coal bump/mining event mp probable mining event mb rock burst qt
   * generic earthquake/tectonic qd damaging earthquake qp unknown-probable earthquake qf felt
   * earthquake qm multiple shock qh quake with associated Harmonic Tremor qv long period event e.g.
   * slow earthquake q2 double shock q4 foreshock qa aftershock l local event of unknown origin r
   * regional event of unknown origin t teleseismic event of unknown origin ge geyser xm meteoritic
   * origin xl lights xo odors - unknown
   */
  private String etype;

  static final public String ETYPE_NA = "-";

  /**
   * Depth as estimated from depth phases. This value is a measure of event depth estimated from a
   * depth phase or an average of several depth phases. Depth is measured positive in a downwards
   * direction, starting from the Earth s surface (see <I>ndp</I>).
   * <p>
   * Units: km
   */
  private double depdp;

  static final public double DEPDP_NA = -999;

  /**
   * Depth determination flag. This character flag indicates the method by which the depth was
   * determined or constrained during the location process. The following <I>dtypes</I> are defined:
   * <ul>
   * <li>A (assigned), <li>D (depth restrained > 2pP phases), <li>F (good depth estimate - < 8.5 km), 
   * <li>N (restrained to normal depth - 33 km), <li>L (less reliable - 8.5-16 km 90% conf), <li>P (poor depth
   * estimate - > 16 km), <li>G (from FINR, unknown meaning), <li>Q (from FINR, unknown meaning), <li>B (from
   * EHB, depth fixed at "broadband" depth), <li>W (from EHB, depth fixed at "waveform" depth), <li>f
   * (free,unconstrained), <li>d (from depth phases), <li>r (restrained by location program), <li>g (restrained
   * by geophysicist), <li>q (questionable), <li>w (free, less-well constrained, - (unknown). 
   * </ul>
   * The
   * <I>auth</I> column should indicate the agency or person responsible for this action, or the
   * <I>commid</I> column should point to an explanation in the <B>remark</B> table.
   */
  private String dtype;

  static final public String DTYPE_NA = "-";

  /**
   * Body wave magnitude, <I>mb</I> [<B>origin</B>]. This is the body wave magnitude of an event.
   * The identifier <I>mbid</I> that points to <I>magid</I> in the <B>netmag</B> table is associated
   * with this column. The information in that record summarizes the method of analysis and data
   * used (see magnitude, magtype, ml, and ms).
   * <p>
   * Units: magnitude
   */
  private double mb;

  static final public double MB_NA = -999;

  /**
   * Magnitude identifier for <I>mb</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>mbid</I> is a foreign key joining <B>origin</B> to
   * <B>netmag</B> where origin.mbid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   */
  private long mbid;

  static final public long MBID_NA = -1;

  /**
   * This is the surface wave magnitude for an event. The identifier <I>msid</I>, which points to
   * <I>magid</I> in the <B>netmag</B> table, is associated with this column. The information in
   * that record summarizes the method of analysis and the data used (see <I>magnitude</I>,
   * <I>magtype</I>, <I>mb</I>, and <I>ml</I>).
   * <p>
   * Units: magnitude
   */
  private double ms;

  static final public double MS_NA = -999;

  /**
   * Magnitude identifier for <I>ms</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>msid</I> is a foreign key joining <B>origin</B> to
   * <B>netmag</B> where origin.msid = netmag.magid (see <I>magid</I>, <I>mbid</I>, and
   * <I>mlid</I>).
   */
  private long msid;

  static final public long MSID_NA = -1;

  /**
   * Local magnitude (<I>ML</I>) of an event. The identifier <I>mlid</I>, which points to
   * <I>magid</I> in the <B>netmag</B> tables, is associated with this column. The information in
   * that record summarizes the method of analysis and the data used (see <I>magnitude</I>,
   * <I>magtype</I>, <I>mb</I>, and <I>ms</I>).
   * <p>
   * Units: magnitude
   */
  private double ml;

  static final public double ML_NA = -999;

  /**
   * Magnitude identifier for local magnitude (<I>ML</I>). This attribute stores the <I>magid</I>
   * for a record in <B>netmag</B>. The identifier <I>mlid</I> is a foreign key joining
   * <B>origin</B> to <B>netmag</B> where <B>origin</B>.<I>mlid</I> = <B>netmag</B>.<I>magid</I>
   * (see <I>magid</I>, <I>mbid</I>, and <I>msid</I>).
   */
  private long mlid;

  static final public long MLID_NA = -1;

  /**
   * Location algorithm used. This column is a brief textual description of the algorithm used for
   * computing a seismic origin.
   */
  private String algorithm;

  static final public String ALGORITHM_NA = "-";

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   */
  private String auth;

  static final public String AUTH_NA = "-";

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
    columns.add("lat", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("lon", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("depth", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("evid", Columns.FieldType.LONG, "%d");
    columns.add("jdate", Columns.FieldType.LONG, "%d");
    columns.add("nass", Columns.FieldType.LONG, "%d");
    columns.add("ndef", Columns.FieldType.LONG, "%d");
    columns.add("ndp", Columns.FieldType.LONG, "%d");
    columns.add("grn", Columns.FieldType.LONG, "%d");
    columns.add("srn", Columns.FieldType.LONG, "%d");
    columns.add("etype", Columns.FieldType.STRING, "%s");
    columns.add("depdp", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("dtype", Columns.FieldType.STRING, "%s");
    columns.add("mb", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("mbid", Columns.FieldType.LONG, "%d");
    columns.add("ms", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("msid", Columns.FieldType.LONG, "%d");
    columns.add("ml", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("mlid", Columns.FieldType.LONG, "%d");
    columns.add("algorithm", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Origin(double lat, double lon, double depth, double time, long orid, long evid, long jdate,
      long nass, long ndef, long ndp, long grn, long srn, String etype, double depdp, String dtype,
      double mb, long mbid, double ms, long msid, double ml, long mlid, String algorithm,
      String auth, long commid) {
    setValues(lat, lon, depth, time, orid, evid, jdate, nass, ndef, ndp, grn, srn, etype, depdp,
        dtype, mb, mbid, ms, msid, ml, mlid, algorithm, auth, commid);
  }

  private void setValues(double lat, double lon, double depth, double time, long orid, long evid,
      long jdate, long nass, long ndef, long ndp, long grn, long srn, String etype, double depdp,
      String dtype, double mb, long mbid, double ms, long msid, double ml, long mlid,
      String algorithm, String auth, long commid) {
    this.lat = lat;
    this.lon = lon;
    this.depth = depth;
    this.time = time;
    this.orid = orid;
    this.evid = evid;
    this.jdate = jdate;
    this.nass = nass;
    this.ndef = ndef;
    this.ndp = ndp;
    this.grn = grn;
    this.srn = srn;
    this.etype = etype;
    this.depdp = depdp;
    this.dtype = dtype;
    this.mb = mb;
    this.mbid = mbid;
    this.ms = ms;
    this.msid = msid;
    this.ml = ml;
    this.mlid = mlid;
    this.algorithm = algorithm;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Origin(Origin other) {
    this.lat = other.getLat();
    this.lon = other.getLon();
    this.depth = other.getDepth();
    this.time = other.getTime();
    this.orid = other.getOrid();
    this.evid = other.getEvid();
    this.jdate = other.getJdate();
    this.nass = other.getNass();
    this.ndef = other.getNdef();
    this.ndp = other.getNdp();
    this.grn = other.getGrn();
    this.srn = other.getSrn();
    this.etype = other.getEtype();
    this.depdp = other.getDepdp();
    this.dtype = other.getDtype();
    this.mb = other.getMb();
    this.mbid = other.getMbid();
    this.ms = other.getMs();
    this.msid = other.getMsid();
    this.ml = other.getMl();
    this.mlid = other.getMlid();
    this.algorithm = other.getAlgorithm();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Origin() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(LAT_NA, LON_NA, DEPTH_NA, TIME_NA, ORID_NA, EVID_NA, JDATE_NA, NASS_NA, NDEF_NA,
        NDP_NA, GRN_NA, SRN_NA, ETYPE_NA, DEPDP_NA, DTYPE_NA, MB_NA, MBID_NA, MS_NA, MSID_NA, ML_NA,
        MLID_NA, ALGORITHM_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "etype":
        return etype;
      case "dtype":
        return dtype;
      case "algorithm":
        return algorithm;
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
      case "etype":
        etype = value;
        break;
      case "dtype":
        dtype = value;
        break;
      case "algorithm":
        algorithm = value;
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
      case "lat":
        return lat;
      case "lon":
        return lon;
      case "depth":
        return depth;
      case "time":
        return time;
      case "depdp":
        return depdp;
      case "mb":
        return mb;
      case "ms":
        return ms;
      case "ml":
        return ml;
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
      case "time":
        time = value;
        break;
      case "depdp":
        depdp = value;
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
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "orid":
        return orid;
      case "evid":
        return evid;
      case "jdate":
        return jdate;
      case "nass":
        return nass;
      case "ndef":
        return ndef;
      case "ndp":
        return ndp;
      case "grn":
        return grn;
      case "srn":
        return srn;
      case "mbid":
        return mbid;
      case "msid":
        return msid;
      case "mlid":
        return mlid;
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
      case "orid":
        orid = value;
        break;
      case "evid":
        evid = value;
        break;
      case "jdate":
        jdate = value;
        break;
      case "nass":
        nass = value;
        break;
      case "ndef":
        ndef = value;
        break;
      case "ndp":
        ndp = value;
        break;
      case "grn":
        grn = value;
        break;
      case "srn":
        srn = value;
        break;
      case "mbid":
        mbid = value;
        break;
      case "msid":
        msid = value;
        break;
      case "mlid":
        mlid = value;
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
  public Origin(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Origin(DataInputStream input) throws IOException {
    this(input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        input.readLong(), input.readLong(), input.readLong(), readString(input), input.readDouble(),
        readString(input), input.readDouble(), input.readLong(), input.readDouble(),
        input.readLong(), input.readDouble(), input.readLong(), readString(input),
        readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Origin(ByteBuffer input) {
    this(input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getLong(),
        input.getLong(), input.getLong(), input.getLong(), readString(input), input.getDouble(),
        readString(input), input.getDouble(), input.getLong(), input.getDouble(), input.getLong(),
        input.getDouble(), input.getLong(), readString(input), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Origin(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Origin(ResultSet input, int offset) throws SQLException {
    this(input.getDouble(offset + 1), input.getDouble(offset + 2), input.getDouble(offset + 3),
        input.getDouble(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getLong(offset + 7), input.getLong(offset + 8), input.getLong(offset + 9),
        input.getLong(offset + 10), input.getLong(offset + 11), input.getLong(offset + 12),
        input.getString(offset + 13), input.getDouble(offset + 14), input.getString(offset + 15),
        input.getDouble(offset + 16), input.getLong(offset + 17), input.getDouble(offset + 18),
        input.getLong(offset + 19), input.getDouble(offset + 20), input.getLong(offset + 21),
        input.getString(offset + 22), input.getString(offset + 23), input.getLong(offset + 24));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[24];
    values[0] = lat;
    values[1] = lon;
    values[2] = depth;
    values[3] = time;
    values[4] = orid;
    values[5] = evid;
    values[6] = jdate;
    values[7] = nass;
    values[8] = ndef;
    values[9] = ndp;
    values[10] = grn;
    values[11] = srn;
    values[12] = etype;
    values[13] = depdp;
    values[14] = dtype;
    values[15] = mb;
    values[16] = mbid;
    values[17] = ms;
    values[18] = msid;
    values[19] = ml;
    values[20] = mlid;
    values[21] = algorithm;
    values[22] = auth;
    values[23] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[25];
    values[0] = lat;
    values[1] = lon;
    values[2] = depth;
    values[3] = time;
    values[4] = orid;
    values[5] = evid;
    values[6] = jdate;
    values[7] = nass;
    values[8] = ndef;
    values[9] = ndp;
    values[10] = grn;
    values[11] = srn;
    values[12] = etype;
    values[13] = depdp;
    values[14] = dtype;
    values[15] = mb;
    values[16] = mbid;
    values[17] = ms;
    values[18] = msid;
    values[19] = ml;
    values[20] = mlid;
    values[21] = algorithm;
    values[22] = auth;
    values[23] = commid;
    values[24] = lddate;
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
    output.writeDouble(lat);
    output.writeDouble(lon);
    output.writeDouble(depth);
    output.writeDouble(time);
    output.writeLong(orid);
    output.writeLong(evid);
    output.writeLong(jdate);
    output.writeLong(nass);
    output.writeLong(ndef);
    output.writeLong(ndp);
    output.writeLong(grn);
    output.writeLong(srn);
    writeString(output, etype);
    output.writeDouble(depdp);
    writeString(output, dtype);
    output.writeDouble(mb);
    output.writeLong(mbid);
    output.writeDouble(ms);
    output.writeLong(msid);
    output.writeDouble(ml);
    output.writeLong(mlid);
    writeString(output, algorithm);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putDouble(lat);
    output.putDouble(lon);
    output.putDouble(depth);
    output.putDouble(time);
    output.putLong(orid);
    output.putLong(evid);
    output.putLong(jdate);
    output.putLong(nass);
    output.putLong(ndef);
    output.putLong(ndp);
    output.putLong(grn);
    output.putLong(srn);
    writeString(output, etype);
    output.putDouble(depdp);
    writeString(output, dtype);
    output.putDouble(mb);
    output.putLong(mbid);
    output.putDouble(ms);
    output.putLong(msid);
    output.putDouble(ml);
    output.putLong(mlid);
    writeString(output, algorithm);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Origin objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Origin objects.
   * @throws IOException
   */
  static public void readOrigins(BufferedReader input, Collection<Origin> rows) throws IOException {
    String[] saved = Origin.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Origin.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Origin(new Scanner(line)));
    }
    input.close();
    Origin.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Origin objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Origin objects.
   * @throws IOException
   */
  static public void readOrigins(File inputFile, Collection<Origin> rows) throws IOException {
    readOrigins(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Origin objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Origin objects.
   * @throws IOException
   */
  static public void readOrigins(InputStream inputStream, Collection<Origin> rows)
      throws IOException {
    readOrigins(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Origin objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Origin objects
   * @throws IOException
   */
  static public Set<Origin> readOrigins(BufferedReader input) throws IOException {
    Set<Origin> rows = new LinkedHashSet<Origin>();
    readOrigins(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Origin objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Origin objects
   * @throws IOException
   */
  static public Set<Origin> readOrigins(File inputFile) throws IOException {
    return readOrigins(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Origin objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Origin objects
   * @throws IOException
   */
  static public Set<Origin> readOrigins(InputStream input) throws IOException {
    return readOrigins(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Origin objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param origins the Origin objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Origin> origins) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Origin origin : origins)
      origin.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Origin objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param origins the Origin objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Origin> origins, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName
          + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Origin origin : origins) {
        int i = 0;
        statement.setDouble(++i, origin.lat);
        statement.setDouble(++i, origin.lon);
        statement.setDouble(++i, origin.depth);
        statement.setDouble(++i, origin.time);
        statement.setLong(++i, origin.orid);
        statement.setLong(++i, origin.evid);
        statement.setLong(++i, origin.jdate);
        statement.setLong(++i, origin.nass);
        statement.setLong(++i, origin.ndef);
        statement.setLong(++i, origin.ndp);
        statement.setLong(++i, origin.grn);
        statement.setLong(++i, origin.srn);
        statement.setString(++i, origin.etype);
        statement.setDouble(++i, origin.depdp);
        statement.setString(++i, origin.dtype);
        statement.setDouble(++i, origin.mb);
        statement.setLong(++i, origin.mbid);
        statement.setDouble(++i, origin.ms);
        statement.setLong(++i, origin.msid);
        statement.setDouble(++i, origin.ml);
        statement.setLong(++i, origin.mlid);
        statement.setString(++i, origin.algorithm);
        statement.setString(++i, origin.auth);
        statement.setLong(++i, origin.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Origin
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Origin> readOrigins(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Origin> results = new HashSet<Origin>();
    readOrigins(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Origin
   *        table.
   * @param origins
   * @throws SQLException
   */
  static public void readOrigins(Connection connection, String selectStatement, Set<Origin> origins)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        origins.add(new Origin(rs));
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
   * this Origin object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Origin object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "lat, lon, depth, time, orid, evid, jdate, nass, ndef, ndp, grn, srn, etype, depdp, dtype, mb, mbid, ms, msid, ml, mlid, algorithm, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Double.toString(lat)).append(", ");
    sql.append(Double.toString(lon)).append(", ");
    sql.append(Double.toString(depth)).append(", ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(evid)).append(", ");
    sql.append(Long.toString(jdate)).append(", ");
    sql.append(Long.toString(nass)).append(", ");
    sql.append(Long.toString(ndef)).append(", ");
    sql.append(Long.toString(ndp)).append(", ");
    sql.append(Long.toString(grn)).append(", ");
    sql.append(Long.toString(srn)).append(", ");
    sql.append("'").append(etype).append("', ");
    sql.append(Double.toString(depdp)).append(", ");
    sql.append("'").append(dtype).append("', ");
    sql.append(Double.toString(mb)).append(", ");
    sql.append(Long.toString(mbid)).append(", ");
    sql.append(Double.toString(ms)).append(", ");
    sql.append(Long.toString(msid)).append(", ");
    sql.append(Double.toString(ml)).append(", ");
    sql.append(Long.toString(mlid)).append(", ");
    sql.append("'").append(algorithm).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Origin in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Origin in the database
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
   * Generate a sql script to create a table of type Origin in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Origin in the database
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
    buf.append("lat          float(53)            NOT NULL,\n");
    buf.append("lon          float(53)            NOT NULL,\n");
    buf.append("depth        float(24)            NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("evid         number(9)            NOT NULL,\n");
    buf.append("jdate        number(8)            NOT NULL,\n");
    buf.append("nass         number(4)            NOT NULL,\n");
    buf.append("ndef         number(4)            NOT NULL,\n");
    buf.append("ndp          number(4)            NOT NULL,\n");
    buf.append("grn          number(8)            NOT NULL,\n");
    buf.append("srn          number(8)            NOT NULL,\n");
    buf.append("etype        varchar2(7)          NOT NULL,\n");
    buf.append("depdp        float(24)            NOT NULL,\n");
    buf.append("dtype        varchar2(1)          NOT NULL,\n");
    buf.append("mb           float(24)            NOT NULL,\n");
    buf.append("mbid         number(9)            NOT NULL,\n");
    buf.append("ms           float(24)            NOT NULL,\n");
    buf.append("msid         number(9)            NOT NULL,\n");
    buf.append("ml           float(24)            NOT NULL,\n");
    buf.append("mlid         number(9)            NOT NULL,\n");
    buf.append("algorithm    varchar2(15)         NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (orid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (lat,lon,depth,time,auth)");
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
    return 219;
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
    return (other instanceof Origin) && ((Origin) other).orid == orid;
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
    return (other instanceof Origin) && ((Origin) other).lat == lat && ((Origin) other).lon == lon
        && ((Origin) other).depth == depth && ((Origin) other).time == time
        && ((Origin) other).auth.equals(auth);
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
  public Origin setLat(double lat) {
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
  public Origin setLon(double lon) {
    this.lon = lon;
    setHash(null);
    return this;
  }

  /**
   * Source depth. This column gives the depth (positive down) of the event origin. Negative depth
   * implies an atmospheric event.
   * <p>
   * Units: km
   * 
   * @return depth
   */
  public double getDepth() {
    return depth;
  }

  /**
   * Source depth. This column gives the depth (positive down) of the event origin. Negative depth
   * implies an atmospheric event.
   * <p>
   * Units: km
   * 
   * @param depth
   */
  public Origin setDepth(double depth) {
    this.depth = depth;
    setHash(null);
    return this;
  }

  /**
   * Epoch time, given as seconds since midnight, January 1, 1970, and stored in a double-precision
   * floating number. Time refers to the table in which it is found [for example, in <B>arrival</B>
   * it is the arrival time, in <B>origin</B> it is the origin time, and in <B>wfdisc</B> is the
   * start time of data]. Where the date of historical events is known, time is set to the start
   * time of that date. Where the date of contemporary arrival measurements is known but no time is
   * given, then time is set to the NA Value. The double-precision floating point number allows 15
   * decimal digits. At one millisecond accuracy, this is a range of 3.0e+4 years. Where the date is
   * unknown or prior to February 10, 1653, time is set to the NA Value.
   * <p>
   * Units: s
   * 
   * @return time
   */
  public double getTime() {
    return time;
  }

  /**
   * Epoch time, given as seconds since midnight, January 1, 1970, and stored in a double-precision
   * floating number. Time refers to the table in which it is found [for example, in <B>arrival</B>
   * it is the arrival time, in <B>origin</B> it is the origin time, and in <B>wfdisc</B> is the
   * start time of data]. Where the date of historical events is known, time is set to the start
   * time of that date. Where the date of contemporary arrival measurements is known but no time is
   * given, then time is set to the NA Value. The double-precision floating point number allows 15
   * decimal digits. At one millisecond accuracy, this is a range of 3.0e+4 years. Where the date is
   * unknown or prior to February 10, 1653, time is set to the NA Value.
   * <p>
   * Units: s
   * 
   * @param time
   */
  public Origin setTime(double time) {
    this.time = time;
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
  public Origin setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Event identifier. Each event is assigned a unique positive integer that identifies it in a
   * database. Several records in the <B>origin</B> table can have the same <I>evid</I>. Analysts
   * have several opinions about the location of the event.
   * 
   * @return evid
   */
  public long getEvid() {
    return evid;
  }

  /**
   * Event identifier. Each event is assigned a unique positive integer that identifies it in a
   * database. Several records in the <B>origin</B> table can have the same <I>evid</I>. Analysts
   * have several opinions about the location of the event.
   * 
   * @param evid
   * @throws IllegalArgumentException if evid >= 1000000000
   */
  public Origin setEvid(long evid) {
    if (evid >= 1000000000L)
      throw new IllegalArgumentException("evid=" + evid + " but cannot be >= 1000000000");
    this.evid = evid;
    setHash(null);
    return this;
  }

  /**
   * Julian date. Date of an arrival, origin, seismic recording, etc. The same information is
   * available in epoch time, but the Julian date format is more convenient for many types of
   * searches. Dates B.C. are negative. The year will never equal 0000, and the day will never equal
   * 000. Where only the year is known, the day of the year is 001; where only year and month are
   * known, the day of year is the first day of the month. Only the year is negated for B.C., so 1
   * January of 10 B.C. is -0010001 (see <I>time</I>).
   * 
   * @return jdate
   */
  public long getJdate() {
    return jdate;
  }

  /**
   * Julian date. Date of an arrival, origin, seismic recording, etc. The same information is
   * available in epoch time, but the Julian date format is more convenient for many types of
   * searches. Dates B.C. are negative. The year will never equal 0000, and the day will never equal
   * 000. Where only the year is known, the day of the year is 001; where only year and month are
   * known, the day of year is the first day of the month. Only the year is negated for B.C., so 1
   * January of 10 B.C. is -0010001 (see <I>time</I>).
   * 
   * @param jdate
   * @throws IllegalArgumentException if jdate >= 100000000
   */
  public Origin setJdate(long jdate) {
    if (jdate >= 100000000L)
      throw new IllegalArgumentException("jdate=" + jdate + " but cannot be >= 100000000");
    this.jdate = jdate;
    setHash(null);
    return this;
  }

  /**
   * Number of associated arrivals. This column gives the number of arrivals associated with the
   * origin
   * 
   * @return nass
   */
  public long getNass() {
    return nass;
  }

  /**
   * Number of associated arrivals. This column gives the number of arrivals associated with the
   * origin
   * 
   * @param nass
   * @throws IllegalArgumentException if nass >= 10000
   */
  public Origin setNass(long nass) {
    if (nass >= 10000L)
      throw new IllegalArgumentException("nass=" + nass + " but cannot be >= 10000");
    this.nass = nass;
    setHash(null);
    return this;
  }

  /**
   * Number of time-defining phases
   * 
   * @return ndef
   */
  public long getNdef() {
    return ndef;
  }

  /**
   * Number of time-defining phases
   * 
   * @param ndef
   * @throws IllegalArgumentException if ndef >= 10000
   */
  public Origin setNdef(long ndef) {
    if (ndef >= 10000L)
      throw new IllegalArgumentException("ndef=" + ndef + " but cannot be >= 10000");
    this.ndef = ndef;
    setHash(null);
    return this;
  }

  /**
   * Number of depth phases. This column gives the number of depth phases used in calculating
   * depth/depdp (see depdp)
   * 
   * @return ndp
   */
  public long getNdp() {
    return ndp;
  }

  /**
   * Number of depth phases. This column gives the number of depth phases used in calculating
   * depth/depdp (see depdp)
   * 
   * @param ndp
   * @throws IllegalArgumentException if ndp >= 10000
   */
  public Origin setNdp(long ndp) {
    if (ndp >= 10000L)
      throw new IllegalArgumentException("ndp=" + ndp + " but cannot be >= 10000");
    this.ndp = ndp;
    setHash(null);
    return this;
  }

  /**
   * Geographic region number.
   * 
   * @return grn
   */
  public long getGrn() {
    return grn;
  }

  /**
   * Geographic region number.
   * 
   * @param grn
   * @throws IllegalArgumentException if grn >= 100000000
   */
  public Origin setGrn(long grn) {
    if (grn >= 100000000L)
      throw new IllegalArgumentException("grn=" + grn + " but cannot be >= 100000000");
    this.grn = grn;
    setHash(null);
    return this;
  }

  /**
   * Seismic region number (see <I>grn</I>).
   * 
   * @return srn
   */
  public long getSrn() {
    return srn;
  }

  /**
   * Seismic region number (see <I>grn</I>).
   * 
   * @param srn
   * @throws IllegalArgumentException if srn >= 100000000
   */
  public Origin setSrn(long srn) {
    if (srn >= 100000000L)
      throw new IllegalArgumentException("srn=" + srn + " but cannot be >= 100000000");
    this.srn = srn;
    setHash(null);
    return this;
  }

  /**
   * An event type that is used to identify the type of seismic event, when known. The recommended
   * event types are: ex generic explosion ec chemical explosion ep probable explosion en nuclear
   * explosion mc collapse me coal bump/mining event mp probable mining event mb rock burst qt
   * generic earthquake/tectonic qd damaging earthquake qp unknown-probable earthquake qf felt
   * earthquake qm multiple shock qh quake with associated Harmonic Tremor qv long period event e.g.
   * slow earthquake q2 double shock q4 foreshock qa aftershock l local event of unknown origin r
   * regional event of unknown origin t teleseismic event of unknown origin ge geyser xm meteoritic
   * origin xl lights xo odors - unknown
   * 
   * @return etype
   */
  public String getEtype() {
    return etype;
  }

  /**
   * An event type that is used to identify the type of seismic event, when known. The recommended
   * event types are: ex generic explosion ec chemical explosion ep probable explosion en nuclear
   * explosion mc collapse me coal bump/mining event mp probable mining event mb rock burst qt
   * generic earthquake/tectonic qd damaging earthquake qp unknown-probable earthquake qf felt
   * earthquake qm multiple shock qh quake with associated Harmonic Tremor qv long period event e.g.
   * slow earthquake q2 double shock q4 foreshock qa aftershock l local event of unknown origin r
   * regional event of unknown origin t teleseismic event of unknown origin ge geyser xm meteoritic
   * origin xl lights xo odors - unknown
   * 
   * @param etype
   * @throws IllegalArgumentException if etype.length() >= 7
   */
  public Origin setEtype(String etype) {
    if (etype.length() > 7)
      throw new IllegalArgumentException(
          String.format("etype.length() cannot be > 7.  etype=%s", etype));
    this.etype = etype;
    setHash(null);
    return this;
  }

  /**
   * Depth as estimated from depth phases. This value is a measure of event depth estimated from a
   * depth phase or an average of several depth phases. Depth is measured positive in a downwards
   * direction, starting from the Earth s surface (see <I>ndp</I>).
   * <p>
   * Units: km
   * 
   * @return depdp
   */
  public double getDepdp() {
    return depdp;
  }

  /**
   * Depth as estimated from depth phases. This value is a measure of event depth estimated from a
   * depth phase or an average of several depth phases. Depth is measured positive in a downwards
   * direction, starting from the Earth s surface (see <I>ndp</I>).
   * <p>
   * Units: km
   * 
   * @param depdp
   */
  public Origin setDepdp(double depdp) {
    this.depdp = depdp;
    setHash(null);
    return this;
  }

  /**
   * Depth determination flag. This character flag indicates the method by which the depth was
   * determined or constrained during the location process. The following <I>dtypes</I> are defined:
   * A (assigned), D (depth restrained > 2pP phases), F (good depth estimate - < 8.5 km), N
   * (restrained to normal depth - 33 km), L (less reliable - 8.5-16 km 90% conf), P (poor depth
   * estimate - > 16 km), G (from FINR, unknown meaning), Q (from FINR, unknown meaning), B (from
   * EHB, depth fixed at "broadband" depth), W (from EHB, depth fixed at "waveform" depth), f
   * (free,unconstrained), d (from depth phases), r (restrained by location program), g (restrained
   * by geophysicist), q (questionable), w (free, less-well constrained, - (unknown). The
   * <I>auth</I> column should indicate the agency or person responsible for this action, or the
   * <I>commid</I> column should point to an explanation in the <B>remark</B> table.
   * 
   * @return dtype
   */
  public String getDtype() {
    return dtype;
  }

  /**
   * Depth determination flag. This character flag indicates the method by which the depth was
   * determined or constrained during the location process. The following <I>dtypes</I> are defined:
   * A (assigned), D (depth restrained > 2pP phases), F (good depth estimate - < 8.5 km), N
   * (restrained to normal depth - 33 km), L (less reliable - 8.5-16 km 90% conf), P (poor depth
   * estimate - > 16 km), G (from FINR, unknown meaning), Q (from FINR, unknown meaning), B (from
   * EHB, depth fixed at "broadband" depth), W (from EHB, depth fixed at "waveform" depth), f
   * (free,unconstrained), d (from depth phases), r (restrained by location program), g (restrained
   * by geophysicist), q (questionable), w (free, less-well constrained, - (unknown). The
   * <I>auth</I> column should indicate the agency or person responsible for this action, or the
   * <I>commid</I> column should point to an explanation in the <B>remark</B> table.
   * 
   * @param dtype
   * @throws IllegalArgumentException if dtype.length() >= 1
   */
  public Origin setDtype(String dtype) {
    if (dtype.length() > 1)
      throw new IllegalArgumentException(
          String.format("dtype.length() cannot be > 1.  dtype=%s", dtype));
    this.dtype = dtype;
    setHash(null);
    return this;
  }

  /**
   * Body wave magnitude, <I>mb</I> [<B>origin</B>]. This is the body wave magnitude of an event.
   * The identifier <I>mbid</I> that points to <I>magid</I> in the <B>netmag</B> table is associated
   * with this column. The information in that record summarizes the method of analysis and data
   * used (see magnitude, magtype, ml, and ms).
   * <p>
   * Units: magnitude
   * 
   * @return mb
   */
  public double getMb() {
    return mb;
  }

  /**
   * Body wave magnitude, <I>mb</I> [<B>origin</B>]. This is the body wave magnitude of an event.
   * The identifier <I>mbid</I> that points to <I>magid</I> in the <B>netmag</B> table is associated
   * with this column. The information in that record summarizes the method of analysis and data
   * used (see magnitude, magtype, ml, and ms).
   * <p>
   * Units: magnitude
   * 
   * @param mb
   */
  public Origin setMb(double mb) {
    this.mb = mb;
    setHash(null);
    return this;
  }

  /**
   * Magnitude identifier for <I>mb</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>mbid</I> is a foreign key joining <B>origin</B> to
   * <B>netmag</B> where origin.mbid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   * 
   * @return mbid
   */
  public long getMbid() {
    return mbid;
  }

  /**
   * Magnitude identifier for <I>mb</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>mbid</I> is a foreign key joining <B>origin</B> to
   * <B>netmag</B> where origin.mbid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   * 
   * @param mbid
   * @throws IllegalArgumentException if mbid >= 1000000000
   */
  public Origin setMbid(long mbid) {
    if (mbid >= 1000000000L)
      throw new IllegalArgumentException("mbid=" + mbid + " but cannot be >= 1000000000");
    this.mbid = mbid;
    setHash(null);
    return this;
  }

  /**
   * This is the surface wave magnitude for an event. The identifier <I>msid</I>, which points to
   * <I>magid</I> in the <B>netmag</B> table, is associated with this column. The information in
   * that record summarizes the method of analysis and the data used (see <I>magnitude</I>,
   * <I>magtype</I>, <I>mb</I>, and <I>ml</I>).
   * <p>
   * Units: magnitude
   * 
   * @return ms
   */
  public double getMs() {
    return ms;
  }

  /**
   * This is the surface wave magnitude for an event. The identifier <I>msid</I>, which points to
   * <I>magid</I> in the <B>netmag</B> table, is associated with this column. The information in
   * that record summarizes the method of analysis and the data used (see <I>magnitude</I>,
   * <I>magtype</I>, <I>mb</I>, and <I>ml</I>).
   * <p>
   * Units: magnitude
   * 
   * @param ms
   */
  public Origin setMs(double ms) {
    this.ms = ms;
    setHash(null);
    return this;
  }

  /**
   * Magnitude identifier for <I>ms</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>msid</I> is a foreign key joining <B>origin</B> to
   * <B>netmag</B> where origin.msid = netmag.magid (see <I>magid</I>, <I>mbid</I>, and
   * <I>mlid</I>).
   * 
   * @return msid
   */
  public long getMsid() {
    return msid;
  }

  /**
   * Magnitude identifier for <I>ms</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>msid</I> is a foreign key joining <B>origin</B> to
   * <B>netmag</B> where origin.msid = netmag.magid (see <I>magid</I>, <I>mbid</I>, and
   * <I>mlid</I>).
   * 
   * @param msid
   * @throws IllegalArgumentException if msid >= 1000000000
   */
  public Origin setMsid(long msid) {
    if (msid >= 1000000000L)
      throw new IllegalArgumentException("msid=" + msid + " but cannot be >= 1000000000");
    this.msid = msid;
    setHash(null);
    return this;
  }

  /**
   * Local magnitude (<I>ML</I>) of an event. The identifier <I>mlid</I>, which points to
   * <I>magid</I> in the <B>netmag</B> tables, is associated with this column. The information in
   * that record summarizes the method of analysis and the data used (see <I>magnitude</I>,
   * <I>magtype</I>, <I>mb</I>, and <I>ms</I>).
   * <p>
   * Units: magnitude
   * 
   * @return ml
   */
  public double getMl() {
    return ml;
  }

  /**
   * Local magnitude (<I>ML</I>) of an event. The identifier <I>mlid</I>, which points to
   * <I>magid</I> in the <B>netmag</B> tables, is associated with this column. The information in
   * that record summarizes the method of analysis and the data used (see <I>magnitude</I>,
   * <I>magtype</I>, <I>mb</I>, and <I>ms</I>).
   * <p>
   * Units: magnitude
   * 
   * @param ml
   */
  public Origin setMl(double ml) {
    this.ml = ml;
    setHash(null);
    return this;
  }

  /**
   * Magnitude identifier for local magnitude (<I>ML</I>). This attribute stores the <I>magid</I>
   * for a record in <B>netmag</B>. The identifier <I>mlid</I> is a foreign key joining
   * <B>origin</B> to <B>netmag</B> where <B>origin</B>.<I>mlid</I> = <B>netmag</B>.<I>magid</I>
   * (see <I>magid</I>, <I>mbid</I>, and <I>msid</I>).
   * 
   * @return mlid
   */
  public long getMlid() {
    return mlid;
  }

  /**
   * Magnitude identifier for local magnitude (<I>ML</I>). This attribute stores the <I>magid</I>
   * for a record in <B>netmag</B>. The identifier <I>mlid</I> is a foreign key joining
   * <B>origin</B> to <B>netmag</B> where <B>origin</B>.<I>mlid</I> = <B>netmag</B>.<I>magid</I>
   * (see <I>magid</I>, <I>mbid</I>, and <I>msid</I>).
   * 
   * @param mlid
   * @throws IllegalArgumentException if mlid >= 1000000000
   */
  public Origin setMlid(long mlid) {
    if (mlid >= 1000000000L)
      throw new IllegalArgumentException("mlid=" + mlid + " but cannot be >= 1000000000");
    this.mlid = mlid;
    setHash(null);
    return this;
  }

  /**
   * Location algorithm used. This column is a brief textual description of the algorithm used for
   * computing a seismic origin.
   * 
   * @return algorithm
   */
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * Location algorithm used. This column is a brief textual description of the algorithm used for
   * computing a seismic origin.
   * 
   * @param algorithm
   * @throws IllegalArgumentException if algorithm.length() >= 15
   */
  public Origin setAlgorithm(String algorithm) {
    if (algorithm.length() > 15)
      throw new IllegalArgumentException(
          String.format("algorithm.length() cannot be > 15.  algorithm=%s", algorithm));
    this.algorithm = algorithm;
    setHash(null);
    return this;
  }

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 20
   */
  public Origin setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
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
  public Origin setCommid(long commid) {
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

  public Buff getBuff() {
      Buff buffer = new Buff(this.getClass().getSimpleName());
      buffer.add("format", 1);
      buffer.add("lat", lat, columns.getColumnNameFormatSpecification("lat"));
      buffer.add("lon", lon, columns.getColumnNameFormatSpecification("lon"));
      buffer.add("depth", depth, columns.getColumnNameFormatSpecification("depth"));
      buffer.add("time", time, columns.getColumnNameFormatSpecification("time"));
      buffer.add("orid", orid);
      buffer.add("evid", evid);
      buffer.add("jdate", jdate);
      buffer.add("nass", nass);
      buffer.add("ndef", ndef);
      buffer.add("ndp", ndp);
      buffer.add("grn", grn);
      buffer.add("srn", srn);
      buffer.add("etype", etype);
      buffer.add("depdp", depdp, columns.getColumnNameFormatSpecification("depdp"));
      buffer.add("dtype", dtype);
      buffer.add("mb", mb, columns.getColumnNameFormatSpecification("mb"));
      buffer.add("mbid", mbid);
      buffer.add("ms", ms, columns.getColumnNameFormatSpecification("ms"));
      buffer.add("msid", msid);
      buffer.add("ml", ml, columns.getColumnNameFormatSpecification("ml"));
      buffer.add("mlid", mlid);
      buffer.add("algorithm", algorithm);
      buffer.add("auth", auth);
      buffer.add("commid", commid);

      return buffer;
  }

}
