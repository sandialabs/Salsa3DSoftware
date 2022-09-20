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
 * origin
 */
public class Origin extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Latitude. This attribute is the geographic latitude. Locations north of equator have positive
   * latitudes.
   * <p>
   * Units: degree
   */
  private double lat;

  static final public double LAT_NA = -999.0;

  /**
   * Longitude. This attribute is the geographic longitude in degrees. Longitudes are measured
   * positive east of the Greenwich meridian.
   * <p>
   * Units: degree
   */
  private double lon;

  static final public double LON_NA = -999.0;

  /**
   * Source depth. This attribute gives the depth of the event origin. In stassoc this may be an
   * analyst estimate.
   * <p>
   * Units: km
   */
  private double depth;

  static final public double DEPTH_NA = -999.0;

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
   * Origin identification. Each origin is assigned a unique positive integer which identifies it in
   * a data base. The orid is used to identify one of the many hypotheses of the actual location of
   * the event.
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Event identifier. Each event is assigned a unique positive integer which identifies it in a
   * database. It is possible for several records in the origin relation to have the same evid. This
   * indicates there are several opinions about the location of the event.
   */
  private long evid;

  static final public long EVID_NA = -1;

  /**
   * Julian date. This attribute is the date of an arrival, origin, seismic recording, etc. The same
   * information is available in epoch time, but the Julian date format is more convenient for many
   * types of searches. Dates B.C. are negative. Note: there is no year = 0000 or day = 000. Where
   * only the year is known, day of year = 001; where only year and month are known, day of year =
   * first day of month. Note: only the year is negated for BC., so Jan 1 of 10 BC is -0010001. See
   * time.
   */
  private long jdate;

  static final public long JDATE_NA = -1;

  /**
   * Number of associated arrivals. This attribute gives the number of arrivals associated with the
   * origin
   */
  private long nass;

  static final public long NASS_NA = -1;

  /**
   * Number of time-defining phases. This attribute is the number of arrivals used to locate an
   * event. See timedef.
   */
  private long ndef;

  static final public long NDEF_NA = -1;

  /**
   * Number of depth phases. This attribute gives the number of depth phases used in calculating
   * depth and/or depdp. See depdp.
   */
  private long ndp;

  static final public long NDP_NA = -1;

  /**
   * Geographic region number. This is a geographic region number as defined by Flinn, Engdahl and
   * Hill (Bull, Seism. Soc. Amer. vol 64, pp. 771-992, 1974). See grname.
   */
  private long grn;

  static final public long GRN_NA = -1;

  /**
   * Region number. This is a seismic region number as given in Flinn, Engdahl and Hill (Bull,
   * Seism. Soc. Amer. vol 64, pp. 771-992, 1974). See grn, grname and srname.
   */
  private long srn;

  static final public long SRN_NA = -1;

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
   * Depth as estimated from depth phases. This is a measure of event depth estimated from a depth
   * phase or an average of several depth phases. Depth is measured positive in a downwards
   * direction, starting from the earth's surface. See ndp.
   * <p>
   * Units: km
   */
  private double depdp;

  static final public double DEPDP_NA = -999.0;

  /**
   * Depth determination flag. This single-character flag indicates the method by which the depth
   * was determined or constrained during the location process. The recommended values are: f
   * (free), d (from depth phases), r (restrained by location program) or g (restrained by
   * geophysicist). In cases r or g, either the auth field should indicate the agency or person
   * responsible for this action, or the commid field should point to an explanation in the remark
   * relation.
   */
  private String dtype;

  static final public String DTYPE_NA = "-";

  /**
   * Body wave magnitude. This is the body wave magnitude of an event. Associated with this
   * attribute is the identifier mbid which points to magid in the netmag relation. The information
   * in that record summarizes the method of analysis and data used. See imb, iml, ims, magnitude,
   * magtype, ml, and ms.
   */
  private double mb;

  static final public double MB_NA = -999.0;

  /**
   * Magnitude identifier for mb. This stores the magid for a record in netmag. Mbid is a foreign
   * key joining origin to netmag where origin.mbid = netmag.magid. See magid, mlid, and msid.
   */
  private long mbid;

  static final public long MBID_NA = -1;

  /**
   * Surface wave magnitude. This is the surface wave magnitude for an event. Associated with this
   * attribute is the identifier msid, which points to magid in the netmag relation. The information
   * in that record summarizes the method of analysis and the data used. See imb, iml, ims,
   * magnitude, magtype, mb, and ml.
   */
  private double ms;

  static final public double MS_NA = -999.0;

  /**
   * Magnitude identifier for ms. This stores the magid for a record in netmag. Msid is a foreign
   * key joining origin to netmag where origin.msid = netmag.magid. See magid, mbid, and mlid.
   */
  private long msid;

  static final public long MSID_NA = -1;

  /**
   * Local magnitude. This is the local magnitude of an event. Associated with this attribute is the
   * identifier mlid, which points to magid in the netmag relation. The information in that record
   * summarizes the method of analysis and the data used. See imb, iml, ims, magnitude, magtype, mb,
   * and ms.
   */
  private double ml;

  static final public double ML_NA = -999.0;

  /**
   * Magnitude identifier for ml. This stores the magid for a record in netmag. Mlid is a foreign
   * key joining origin to netmag where origin.mlid = netmag.magid. See magid, mbid, and msid.
   */
  private long mlid;

  static final public long MLID_NA = -1;

  /**
   * Location algorithm used. This is a brief textual description of the algorithm used for
   * computing a seismic origin.
   */
  private String algorithm;

  static final public String ALGORITHM_NA = "-";

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
    columns.add("lat", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("lon", Columns.FieldType.DOUBLE, "%1.4f");
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
    buf.append("lat          float(24)            NOT NULL,\n");
    buf.append("lon          float(24)            NOT NULL,\n");
    buf.append("depth        float(24)            NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("orid         number(8)            NOT NULL,\n");
    buf.append("evid         number(8)            NOT NULL,\n");
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
    buf.append("mbid         number(8)            NOT NULL,\n");
    buf.append("ms           float(24)            NOT NULL,\n");
    buf.append("msid         number(8)            NOT NULL,\n");
    buf.append("ml           float(24)            NOT NULL,\n");
    buf.append("mlid         number(8)            NOT NULL,\n");
    buf.append("algorithm    varchar2(15)         NOT NULL,\n");
    buf.append("auth         varchar2(15)         NOT NULL,\n");
    buf.append("commid       number(8)            NOT NULL,\n");
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
          + "_uk unique (lat,lon,depth,time)");
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
    return 214;
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
        && ((Origin) other).depth == depth && ((Origin) other).time == time;
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
  public Origin setLat(double lat) {
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
  public Origin setLon(double lon) {
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
  public Origin setDepth(double depth) {
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
  public Origin setTime(double time) {
    this.time = time;
    setHash(null);
    return this;
  }

  /**
   * Origin identification. Each origin is assigned a unique positive integer which identifies it in
   * a data base. The orid is used to identify one of the many hypotheses of the actual location of
   * the event.
   * 
   * @return orid
   */
  public long getOrid() {
    return orid;
  }

  /**
   * Origin identification. Each origin is assigned a unique positive integer which identifies it in
   * a data base. The orid is used to identify one of the many hypotheses of the actual location of
   * the event.
   * 
   * @param orid
   * @throws IllegalArgumentException if orid >= 100000000
   */
  public Origin setOrid(long orid) {
    if (orid >= 100000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 100000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Event identifier. Each event is assigned a unique positive integer which identifies it in a
   * database. It is possible for several records in the origin relation to have the same evid. This
   * indicates there are several opinions about the location of the event.
   * 
   * @return evid
   */
  public long getEvid() {
    return evid;
  }

  /**
   * Event identifier. Each event is assigned a unique positive integer which identifies it in a
   * database. It is possible for several records in the origin relation to have the same evid. This
   * indicates there are several opinions about the location of the event.
   * 
   * @param evid
   * @throws IllegalArgumentException if evid >= 100000000
   */
  public Origin setEvid(long evid) {
    if (evid >= 100000000L)
      throw new IllegalArgumentException("evid=" + evid + " but cannot be >= 100000000");
    this.evid = evid;
    setHash(null);
    return this;
  }

  /**
   * Julian date. This attribute is the date of an arrival, origin, seismic recording, etc. The same
   * information is available in epoch time, but the Julian date format is more convenient for many
   * types of searches. Dates B.C. are negative. Note: there is no year = 0000 or day = 000. Where
   * only the year is known, day of year = 001; where only year and month are known, day of year =
   * first day of month. Note: only the year is negated for BC., so Jan 1 of 10 BC is -0010001. See
   * time.
   * 
   * @return jdate
   */
  public long getJdate() {
    return jdate;
  }

  /**
   * Julian date. This attribute is the date of an arrival, origin, seismic recording, etc. The same
   * information is available in epoch time, but the Julian date format is more convenient for many
   * types of searches. Dates B.C. are negative. Note: there is no year = 0000 or day = 000. Where
   * only the year is known, day of year = 001; where only year and month are known, day of year =
   * first day of month. Note: only the year is negated for BC., so Jan 1 of 10 BC is -0010001. See
   * time.
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
   * Number of associated arrivals. This attribute gives the number of arrivals associated with the
   * origin
   * 
   * @return nass
   */
  public long getNass() {
    return nass;
  }

  /**
   * Number of associated arrivals. This attribute gives the number of arrivals associated with the
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
   * Number of time-defining phases. This attribute is the number of arrivals used to locate an
   * event. See timedef.
   * 
   * @return ndef
   */
  public long getNdef() {
    return ndef;
  }

  /**
   * Number of time-defining phases. This attribute is the number of arrivals used to locate an
   * event. See timedef.
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
   * Number of depth phases. This attribute gives the number of depth phases used in calculating
   * depth and/or depdp. See depdp.
   * 
   * @return ndp
   */
  public long getNdp() {
    return ndp;
  }

  /**
   * Number of depth phases. This attribute gives the number of depth phases used in calculating
   * depth and/or depdp. See depdp.
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
   * Geographic region number. This is a geographic region number as defined by Flinn, Engdahl and
   * Hill (Bull, Seism. Soc. Amer. vol 64, pp. 771-992, 1974). See grname.
   * 
   * @return grn
   */
  public long getGrn() {
    return grn;
  }

  /**
   * Geographic region number. This is a geographic region number as defined by Flinn, Engdahl and
   * Hill (Bull, Seism. Soc. Amer. vol 64, pp. 771-992, 1974). See grname.
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
   * Region number. This is a seismic region number as given in Flinn, Engdahl and Hill (Bull,
   * Seism. Soc. Amer. vol 64, pp. 771-992, 1974). See grn, grname and srname.
   * 
   * @return srn
   */
  public long getSrn() {
    return srn;
  }

  /**
   * Region number. This is a seismic region number as given in Flinn, Engdahl and Hill (Bull,
   * Seism. Soc. Amer. vol 64, pp. 771-992, 1974). See grn, grname and srname.
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
  public Origin setEtype(String etype) {
    if (etype.length() > 7)
      throw new IllegalArgumentException(
          String.format("etype.length() cannot be > 7.  etype=%s", etype));
    this.etype = etype;
    setHash(null);
    return this;
  }

  /**
   * Depth as estimated from depth phases. This is a measure of event depth estimated from a depth
   * phase or an average of several depth phases. Depth is measured positive in a downwards
   * direction, starting from the earth's surface. See ndp.
   * <p>
   * Units: km
   * 
   * @return depdp
   */
  public double getDepdp() {
    return depdp;
  }

  /**
   * Depth as estimated from depth phases. This is a measure of event depth estimated from a depth
   * phase or an average of several depth phases. Depth is measured positive in a downwards
   * direction, starting from the earth's surface. See ndp.
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
   * Depth determination flag. This single-character flag indicates the method by which the depth
   * was determined or constrained during the location process. The recommended values are: f
   * (free), d (from depth phases), r (restrained by location program) or g (restrained by
   * geophysicist). In cases r or g, either the auth field should indicate the agency or person
   * responsible for this action, or the commid field should point to an explanation in the remark
   * relation.
   * 
   * @return dtype
   */
  public String getDtype() {
    return dtype;
  }

  /**
   * Depth determination flag. This single-character flag indicates the method by which the depth
   * was determined or constrained during the location process. The recommended values are: f
   * (free), d (from depth phases), r (restrained by location program) or g (restrained by
   * geophysicist). In cases r or g, either the auth field should indicate the agency or person
   * responsible for this action, or the commid field should point to an explanation in the remark
   * relation.
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
   * Body wave magnitude. This is the body wave magnitude of an event. Associated with this
   * attribute is the identifier mbid which points to magid in the netmag relation. The information
   * in that record summarizes the method of analysis and data used. See imb, iml, ims, magnitude,
   * magtype, ml, and ms.
   * 
   * @return mb
   */
  public double getMb() {
    return mb;
  }

  /**
   * Body wave magnitude. This is the body wave magnitude of an event. Associated with this
   * attribute is the identifier mbid which points to magid in the netmag relation. The information
   * in that record summarizes the method of analysis and data used. See imb, iml, ims, magnitude,
   * magtype, ml, and ms.
   * 
   * @param mb
   */
  public Origin setMb(double mb) {
    this.mb = mb;
    setHash(null);
    return this;
  }

  /**
   * Magnitude identifier for mb. This stores the magid for a record in netmag. Mbid is a foreign
   * key joining origin to netmag where origin.mbid = netmag.magid. See magid, mlid, and msid.
   * 
   * @return mbid
   */
  public long getMbid() {
    return mbid;
  }

  /**
   * Magnitude identifier for mb. This stores the magid for a record in netmag. Mbid is a foreign
   * key joining origin to netmag where origin.mbid = netmag.magid. See magid, mlid, and msid.
   * 
   * @param mbid
   * @throws IllegalArgumentException if mbid >= 100000000
   */
  public Origin setMbid(long mbid) {
    if (mbid >= 100000000L)
      throw new IllegalArgumentException("mbid=" + mbid + " but cannot be >= 100000000");
    this.mbid = mbid;
    setHash(null);
    return this;
  }

  /**
   * Surface wave magnitude. This is the surface wave magnitude for an event. Associated with this
   * attribute is the identifier msid, which points to magid in the netmag relation. The information
   * in that record summarizes the method of analysis and the data used. See imb, iml, ims,
   * magnitude, magtype, mb, and ml.
   * 
   * @return ms
   */
  public double getMs() {
    return ms;
  }

  /**
   * Surface wave magnitude. This is the surface wave magnitude for an event. Associated with this
   * attribute is the identifier msid, which points to magid in the netmag relation. The information
   * in that record summarizes the method of analysis and the data used. See imb, iml, ims,
   * magnitude, magtype, mb, and ml.
   * 
   * @param ms
   */
  public Origin setMs(double ms) {
    this.ms = ms;
    setHash(null);
    return this;
  }

  /**
   * Magnitude identifier for ms. This stores the magid for a record in netmag. Msid is a foreign
   * key joining origin to netmag where origin.msid = netmag.magid. See magid, mbid, and mlid.
   * 
   * @return msid
   */
  public long getMsid() {
    return msid;
  }

  /**
   * Magnitude identifier for ms. This stores the magid for a record in netmag. Msid is a foreign
   * key joining origin to netmag where origin.msid = netmag.magid. See magid, mbid, and mlid.
   * 
   * @param msid
   * @throws IllegalArgumentException if msid >= 100000000
   */
  public Origin setMsid(long msid) {
    if (msid >= 100000000L)
      throw new IllegalArgumentException("msid=" + msid + " but cannot be >= 100000000");
    this.msid = msid;
    setHash(null);
    return this;
  }

  /**
   * Local magnitude. This is the local magnitude of an event. Associated with this attribute is the
   * identifier mlid, which points to magid in the netmag relation. The information in that record
   * summarizes the method of analysis and the data used. See imb, iml, ims, magnitude, magtype, mb,
   * and ms.
   * 
   * @return ml
   */
  public double getMl() {
    return ml;
  }

  /**
   * Local magnitude. This is the local magnitude of an event. Associated with this attribute is the
   * identifier mlid, which points to magid in the netmag relation. The information in that record
   * summarizes the method of analysis and the data used. See imb, iml, ims, magnitude, magtype, mb,
   * and ms.
   * 
   * @param ml
   */
  public Origin setMl(double ml) {
    this.ml = ml;
    setHash(null);
    return this;
  }

  /**
   * Magnitude identifier for ml. This stores the magid for a record in netmag. Mlid is a foreign
   * key joining origin to netmag where origin.mlid = netmag.magid. See magid, mbid, and msid.
   * 
   * @return mlid
   */
  public long getMlid() {
    return mlid;
  }

  /**
   * Magnitude identifier for ml. This stores the magid for a record in netmag. Mlid is a foreign
   * key joining origin to netmag where origin.mlid = netmag.magid. See magid, mbid, and msid.
   * 
   * @param mlid
   * @throws IllegalArgumentException if mlid >= 100000000
   */
  public Origin setMlid(long mlid) {
    if (mlid >= 100000000L)
      throw new IllegalArgumentException("mlid=" + mlid + " but cannot be >= 100000000");
    this.mlid = mlid;
    setHash(null);
    return this;
  }

  /**
   * Location algorithm used. This is a brief textual description of the algorithm used for
   * computing a seismic origin.
   * 
   * @return algorithm
   */
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * Location algorithm used. This is a brief textual description of the algorithm used for
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
  public Origin setAuth(String auth) {
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
  public Origin setCommid(long commid) {
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
