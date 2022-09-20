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
 * arrival
 */
public class Arrival extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Station code. This is the common code-name of a seismic observatory. Generally only three or
   * four characters are used.
   */
  private String sta;

  static final public String STA_NA = null;

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
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique sta, chan and time. This number is used in the assoc relation along with the origin
   * identifier to link arrival and origin.
   */
  private long arid;

  static final public long ARID_NA = Long.MIN_VALUE;

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
   * Station association identification. The wavetrain from a single event may be made up of a
   * number of arrivals. A unique stassid joins those arrivals believed to have come from a common
   * event as measured at a single station. Stassid is also the key to the stassoc relation, which
   * contains addiitional signal measurements not contained within the arrival relation, such as
   * station magnitude estimates and computed signal characteristics.
   */
  private long stassid;

  static final public long STASSID_NA = -1;

  /**
   * Channel recording identifier. This is a surrogate key used to uniquely identify a specific
   * recording. Chanid duplicates the information of the compound key sta, chan, time. As a single
   * identifier it is often convenient. Chanid is very database dependent and is included only for
   * backward compatibility with historic databases. Sta, chan and time is more appropriate to the
   * human interface.
   */
  private long chanid;

  static final public long CHANID_NA = -1;

  /**
   * Channel identifier. This is an eight-character code which, taken together with sta, jdate and
   * time, uniquely identifies the source of the seismic data, including the geographic location,
   * spatial orientation, sensor, and subsequent data processing.
   */
  private String chan;

  static final public String CHAN_NA = "-";

  /**
   * Reported phase. This eight-character field holds the name initially given to a seismic phase.
   * Standard seismological labels for the types of signals (or phases) are used (e.g., P, PKP, PcP,
   * pP). Both upper and lower case letters are available and should be used when appropriate, for
   * example, pP or PcP. See phase.
   */
  private String iphase;

  static final public String IPHASE_NA = "-";

  /**
   * Signal type. This single-character flag indicates the event or signal type. The following
   * definitions hold: l = local event r = regional event t = teleseismic event m = mixed or
   * multiple event g = glitch (i.e., non-seismic detection) c = calibration activity upsets the
   * data l, r, and t are supplied by the reporting station, or as an output of post-detection
   * processing. g and c come from analyst comment or from status bits from GDSN and RSTN data.
   */
  private String stype;

  static final public String STYPE_NA = "-";

  /**
   * Delta time. This attribute gives the standard deviation of a detection time.
   * <p>
   * Units: s
   */
  private double deltim;

  static final public double DELTIM_NA = -1.0;

  /**
   * Observed azimuth. This is the estimated station-to-event azimuth measured clockwise from north.
   * Azimuth is estimated from f-k or polarization analysis. In stassoc, the value may be an analyst
   * estimate.
   * <p>
   * Units: degree
   */
  private double azimuth;

  static final public double AZIMUTH_NA = -1.0;

  /**
   * Delta azimuth. This attribute gives the standard deviation of the azimuth of a signal.
   * <p>
   * Units: degree
   */
  private double delaz;

  static final public double DELAZ_NA = -1.0;

  /**
   * Observed slowness. This is the observed slowness of a wave as it sweeps across an array.
   * <p>
   * Units: s/degree
   */
  private double slow;

  static final public double SLOW_NA = -1.0;

  /**
   * Delta slowness. This attribute gives the standard deviation of the slowness of a signal.
   * <p>
   * Units: s/degree
   */
  private double delslo;

  static final public double DELSLO_NA = -1.0;

  /**
   * Emergence angle. This attribute is the emergence angle of an arrival, as observed at a three-
   * component station or array. The value increases from the vertical direction towards the
   * horizontal.
   * <p>
   * Units: degree
   */
  private double ema;

  static final public double EMA_NA = -1.0;

  /**
   * Rectilinearity. This attribute is a measure of signal rectilinearity. The value is obtained
   * from polarization analysis of 3-component data.
   */
  private double rect;

  static final public double RECT_NA = -1.0;

  /**
   * Signal amplitude. This is the zero-to-peak amplitude of the earth's displacement for a seismic
   * phase. Amp is assumed to be corrected for the response of the instrument.
   * <p>
   * Units: nm
   */
  private double amp;

  static final public double AMP_NA = -1.0;

  /**
   * Signal period. This attribute is the period of the signal describe by the arrival record.
   * <p>
   * Units: s
   */
  private double per;

  static final public double PER_NA = -1.0;

  /**
   * Log of amplitude divided by period. This measurement of signal size is often reported instead
   * of the amplitude and period separately. This attribute is only filled if the separate
   * measurements are not available.
   * <p>
   * Units: log10(nm/s)
   */
  private double logat;

  static final public double LOGAT_NA = -999.0;

  /**
   * Clipped data flag. This is a single-character flag to indicate whether (c) or not (n) the data
   * were clipped. Typically, this flag is derived from status bits supplied with GDSN or RSTN data,
   * but could also be supplied as a result of analyst review.
   */
  private String clip;

  static final public String CLIP_NA = "-";

  /**
   * First motion. This is a two-character indication of first motion. The first character describes
   * first motion seen on short-period channels and the second holds for long-period instruments.
   * Compression (dilatation) on a short-period sensor is denoted by c(d) and compression
   * (dilatation) on a long-period sensor is denoted by u(r). Empty character positions will be
   * indicated by dots (e.g., ".r").
   */
  private String fm;

  static final public String FM_NA = "-";

  /**
   * Signal-to-noise ratio. This is an estimate of the size of the signal to that of the noise
   * immediately preceding it.
   */
  private double snr;

  static final public double SNR_NA = -1.0;

  /**
   * Onset quality. This single-character flag is used to denote the sharpness of the onset of a
   * seismic phase. This relates to the timing accuracy as follows: i (impulsive) - accurate to +/-
   * 0.2 seconds e (emergent) - accuracy between +/- (0.2 to 1.0 seconds) w (weak) - timing
   * uncertain to > 1 second.
   */
  private String qual;

  static final public String QUAL_NA = "-";

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
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("arid", Columns.FieldType.LONG, "%d");
    columns.add("jdate", Columns.FieldType.LONG, "%d");
    columns.add("stassid", Columns.FieldType.LONG, "%d");
    columns.add("chanid", Columns.FieldType.LONG, "%d");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("iphase", Columns.FieldType.STRING, "%s");
    columns.add("stype", Columns.FieldType.STRING, "%s");
    columns.add("deltim", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("azimuth", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("delaz", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("slow", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("delslo", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("ema", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("rect", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("amp", Columns.FieldType.DOUBLE, "%1.1f");
    columns.add("per", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("logat", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("clip", Columns.FieldType.STRING, "%s");
    columns.add("fm", Columns.FieldType.STRING, "%s");
    columns.add("snr", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("qual", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Arrival(String sta, double time, long arid, long jdate, long stassid, long chanid,
      String chan, String iphase, String stype, double deltim, double azimuth, double delaz,
      double slow, double delslo, double ema, double rect, double amp, double per, double logat,
      String clip, String fm, double snr, String qual, String auth, long commid) {
    setValues(sta, time, arid, jdate, stassid, chanid, chan, iphase, stype, deltim, azimuth, delaz,
        slow, delslo, ema, rect, amp, per, logat, clip, fm, snr, qual, auth, commid);
  }

  private void setValues(String sta, double time, long arid, long jdate, long stassid, long chanid,
      String chan, String iphase, String stype, double deltim, double azimuth, double delaz,
      double slow, double delslo, double ema, double rect, double amp, double per, double logat,
      String clip, String fm, double snr, String qual, String auth, long commid) {
    this.sta = sta;
    this.time = time;
    this.arid = arid;
    this.jdate = jdate;
    this.stassid = stassid;
    this.chanid = chanid;
    this.chan = chan;
    this.iphase = iphase;
    this.stype = stype;
    this.deltim = deltim;
    this.azimuth = azimuth;
    this.delaz = delaz;
    this.slow = slow;
    this.delslo = delslo;
    this.ema = ema;
    this.rect = rect;
    this.amp = amp;
    this.per = per;
    this.logat = logat;
    this.clip = clip;
    this.fm = fm;
    this.snr = snr;
    this.qual = qual;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Arrival(Arrival other) {
    this.sta = other.getSta();
    this.time = other.getTime();
    this.arid = other.getArid();
    this.jdate = other.getJdate();
    this.stassid = other.getStassid();
    this.chanid = other.getChanid();
    this.chan = other.getChan();
    this.iphase = other.getIphase();
    this.stype = other.getStype();
    this.deltim = other.getDeltim();
    this.azimuth = other.getAzimuth();
    this.delaz = other.getDelaz();
    this.slow = other.getSlow();
    this.delslo = other.getDelslo();
    this.ema = other.getEma();
    this.rect = other.getRect();
    this.amp = other.getAmp();
    this.per = other.getPer();
    this.logat = other.getLogat();
    this.clip = other.getClip();
    this.fm = other.getFm();
    this.snr = other.getSnr();
    this.qual = other.getQual();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Arrival() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(STA_NA, TIME_NA, ARID_NA, JDATE_NA, STASSID_NA, CHANID_NA, CHAN_NA, IPHASE_NA,
        STYPE_NA, DELTIM_NA, AZIMUTH_NA, DELAZ_NA, SLOW_NA, DELSLO_NA, EMA_NA, RECT_NA, AMP_NA,
        PER_NA, LOGAT_NA, CLIP_NA, FM_NA, SNR_NA, QUAL_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "chan":
        return chan;
      case "iphase":
        return iphase;
      case "stype":
        return stype;
      case "clip":
        return clip;
      case "fm":
        return fm;
      case "qual":
        return qual;
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
      case "chan":
        chan = value;
        break;
      case "iphase":
        iphase = value;
        break;
      case "stype":
        stype = value;
        break;
      case "clip":
        clip = value;
        break;
      case "fm":
        fm = value;
        break;
      case "qual":
        qual = value;
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
      case "deltim":
        return deltim;
      case "azimuth":
        return azimuth;
      case "delaz":
        return delaz;
      case "slow":
        return slow;
      case "delslo":
        return delslo;
      case "ema":
        return ema;
      case "rect":
        return rect;
      case "amp":
        return amp;
      case "per":
        return per;
      case "logat":
        return logat;
      case "snr":
        return snr;
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
      case "deltim":
        deltim = value;
        break;
      case "azimuth":
        azimuth = value;
        break;
      case "delaz":
        delaz = value;
        break;
      case "slow":
        slow = value;
        break;
      case "delslo":
        delslo = value;
        break;
      case "ema":
        ema = value;
        break;
      case "rect":
        rect = value;
        break;
      case "amp":
        amp = value;
        break;
      case "per":
        per = value;
        break;
      case "logat":
        logat = value;
        break;
      case "snr":
        snr = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "arid":
        return arid;
      case "jdate":
        return jdate;
      case "stassid":
        return stassid;
      case "chanid":
        return chanid;
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
      case "arid":
        arid = value;
        break;
      case "jdate":
        jdate = value;
        break;
      case "stassid":
        stassid = value;
        break;
      case "chanid":
        chanid = value;
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
  public Arrival(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Arrival(DataInputStream input) throws IOException {
    this(readString(input), input.readDouble(), input.readLong(), input.readLong(),
        input.readLong(), input.readLong(), readString(input), readString(input), readString(input),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), readString(input), readString(input),
        input.readDouble(), readString(input), readString(input), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Arrival(ByteBuffer input) {
    this(readString(input), input.getDouble(), input.getLong(), input.getLong(), input.getLong(),
        input.getLong(), readString(input), readString(input), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), readString(input), readString(input), input.getDouble(),
        readString(input), readString(input), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Arrival(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Arrival(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getDouble(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getString(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getDouble(offset + 12),
        input.getDouble(offset + 13), input.getDouble(offset + 14), input.getDouble(offset + 15),
        input.getDouble(offset + 16), input.getDouble(offset + 17), input.getDouble(offset + 18),
        input.getDouble(offset + 19), input.getString(offset + 20), input.getString(offset + 21),
        input.getDouble(offset + 22), input.getString(offset + 23), input.getString(offset + 24),
        input.getLong(offset + 25));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[25];
    values[0] = sta;
    values[1] = time;
    values[2] = arid;
    values[3] = jdate;
    values[4] = stassid;
    values[5] = chanid;
    values[6] = chan;
    values[7] = iphase;
    values[8] = stype;
    values[9] = deltim;
    values[10] = azimuth;
    values[11] = delaz;
    values[12] = slow;
    values[13] = delslo;
    values[14] = ema;
    values[15] = rect;
    values[16] = amp;
    values[17] = per;
    values[18] = logat;
    values[19] = clip;
    values[20] = fm;
    values[21] = snr;
    values[22] = qual;
    values[23] = auth;
    values[24] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[26];
    values[0] = sta;
    values[1] = time;
    values[2] = arid;
    values[3] = jdate;
    values[4] = stassid;
    values[5] = chanid;
    values[6] = chan;
    values[7] = iphase;
    values[8] = stype;
    values[9] = deltim;
    values[10] = azimuth;
    values[11] = delaz;
    values[12] = slow;
    values[13] = delslo;
    values[14] = ema;
    values[15] = rect;
    values[16] = amp;
    values[17] = per;
    values[18] = logat;
    values[19] = clip;
    values[20] = fm;
    values[21] = snr;
    values[22] = qual;
    values[23] = auth;
    values[24] = commid;
    values[25] = lddate;
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
    output.writeDouble(time);
    output.writeLong(arid);
    output.writeLong(jdate);
    output.writeLong(stassid);
    output.writeLong(chanid);
    writeString(output, chan);
    writeString(output, iphase);
    writeString(output, stype);
    output.writeDouble(deltim);
    output.writeDouble(azimuth);
    output.writeDouble(delaz);
    output.writeDouble(slow);
    output.writeDouble(delslo);
    output.writeDouble(ema);
    output.writeDouble(rect);
    output.writeDouble(amp);
    output.writeDouble(per);
    output.writeDouble(logat);
    writeString(output, clip);
    writeString(output, fm);
    output.writeDouble(snr);
    writeString(output, qual);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, sta);
    output.putDouble(time);
    output.putLong(arid);
    output.putLong(jdate);
    output.putLong(stassid);
    output.putLong(chanid);
    writeString(output, chan);
    writeString(output, iphase);
    writeString(output, stype);
    output.putDouble(deltim);
    output.putDouble(azimuth);
    output.putDouble(delaz);
    output.putDouble(slow);
    output.putDouble(delslo);
    output.putDouble(ema);
    output.putDouble(rect);
    output.putDouble(amp);
    output.putDouble(per);
    output.putDouble(logat);
    writeString(output, clip);
    writeString(output, fm);
    output.putDouble(snr);
    writeString(output, qual);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Arrival objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Arrival objects.
   * @throws IOException
   */
  static public void readArrivals(BufferedReader input, Collection<Arrival> rows)
      throws IOException {
    String[] saved = Arrival.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Arrival.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Arrival(new Scanner(line)));
    }
    input.close();
    Arrival.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Arrival objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Arrival objects.
   * @throws IOException
   */
  static public void readArrivals(File inputFile, Collection<Arrival> rows) throws IOException {
    readArrivals(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Arrival objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Arrival objects.
   * @throws IOException
   */
  static public void readArrivals(InputStream inputStream, Collection<Arrival> rows)
      throws IOException {
    readArrivals(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Arrival objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Arrival objects
   * @throws IOException
   */
  static public Set<Arrival> readArrivals(BufferedReader input) throws IOException {
    Set<Arrival> rows = new LinkedHashSet<Arrival>();
    readArrivals(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Arrival objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Arrival objects
   * @throws IOException
   */
  static public Set<Arrival> readArrivals(File inputFile) throws IOException {
    return readArrivals(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Arrival objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Arrival objects
   * @throws IOException
   */
  static public Set<Arrival> readArrivals(InputStream input) throws IOException {
    return readArrivals(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Arrival objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param arrivals the Arrival objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Arrival> arrivals)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Arrival arrival : arrivals)
      arrival.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Arrival objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param arrivals the Arrival objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Arrival> arrivals, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName
          + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Arrival arrival : arrivals) {
        int i = 0;
        statement.setString(++i, arrival.sta);
        statement.setDouble(++i, arrival.time);
        statement.setLong(++i, arrival.arid);
        statement.setLong(++i, arrival.jdate);
        statement.setLong(++i, arrival.stassid);
        statement.setLong(++i, arrival.chanid);
        statement.setString(++i, arrival.chan);
        statement.setString(++i, arrival.iphase);
        statement.setString(++i, arrival.stype);
        statement.setDouble(++i, arrival.deltim);
        statement.setDouble(++i, arrival.azimuth);
        statement.setDouble(++i, arrival.delaz);
        statement.setDouble(++i, arrival.slow);
        statement.setDouble(++i, arrival.delslo);
        statement.setDouble(++i, arrival.ema);
        statement.setDouble(++i, arrival.rect);
        statement.setDouble(++i, arrival.amp);
        statement.setDouble(++i, arrival.per);
        statement.setDouble(++i, arrival.logat);
        statement.setString(++i, arrival.clip);
        statement.setString(++i, arrival.fm);
        statement.setDouble(++i, arrival.snr);
        statement.setString(++i, arrival.qual);
        statement.setString(++i, arrival.auth);
        statement.setLong(++i, arrival.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Arrival
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Arrival> readArrivals(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Arrival> results = new HashSet<Arrival>();
    readArrivals(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Arrival
   *        table.
   * @param arrivals
   * @throws SQLException
   */
  static public void readArrivals(Connection connection, String selectStatement,
      Set<Arrival> arrivals) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        arrivals.add(new Arrival(rs));
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
   * this Arrival object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Arrival object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "sta, time, arid, jdate, stassid, chanid, chan, iphase, stype, deltim, azimuth, delaz, slow, delslo, ema, rect, amp, per, logat, clip, fm, snr, qual, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(sta).append("', ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Long.toString(arid)).append(", ");
    sql.append(Long.toString(jdate)).append(", ");
    sql.append(Long.toString(stassid)).append(", ");
    sql.append(Long.toString(chanid)).append(", ");
    sql.append("'").append(chan).append("', ");
    sql.append("'").append(iphase).append("', ");
    sql.append("'").append(stype).append("', ");
    sql.append(Double.toString(deltim)).append(", ");
    sql.append(Double.toString(azimuth)).append(", ");
    sql.append(Double.toString(delaz)).append(", ");
    sql.append(Double.toString(slow)).append(", ");
    sql.append(Double.toString(delslo)).append(", ");
    sql.append(Double.toString(ema)).append(", ");
    sql.append(Double.toString(rect)).append(", ");
    sql.append(Double.toString(amp)).append(", ");
    sql.append(Double.toString(per)).append(", ");
    sql.append(Double.toString(logat)).append(", ");
    sql.append("'").append(clip).append("', ");
    sql.append("'").append(fm).append("', ");
    sql.append(Double.toString(snr)).append(", ");
    sql.append("'").append(qual).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Arrival in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Arrival in the database
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
   * Generate a sql script to create a table of type Arrival in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Arrival in the database
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
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("arid         number(8)            NOT NULL,\n");
    buf.append("jdate        number(8)            NOT NULL,\n");
    buf.append("stassid      number(8)            NOT NULL,\n");
    buf.append("chanid       number(8)            NOT NULL,\n");
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("iphase       varchar2(8)          NOT NULL,\n");
    buf.append("stype        varchar2(1)          NOT NULL,\n");
    buf.append("deltim       float(24)            NOT NULL,\n");
    buf.append("azimuth      float(24)            NOT NULL,\n");
    buf.append("delaz        float(24)            NOT NULL,\n");
    buf.append("slow         float(24)            NOT NULL,\n");
    buf.append("delslo       float(24)            NOT NULL,\n");
    buf.append("ema          float(24)            NOT NULL,\n");
    buf.append("rect         float(24)            NOT NULL,\n");
    buf.append("amp          float(24)            NOT NULL,\n");
    buf.append("per          float(24)            NOT NULL,\n");
    buf.append("logat        float(24)            NOT NULL,\n");
    buf.append("clip         varchar2(1)          NOT NULL,\n");
    buf.append("fm           varchar2(2)          NOT NULL,\n");
    buf.append("snr          float(24)            NOT NULL,\n");
    buf.append("qual         varchar2(1)          NOT NULL,\n");
    buf.append("auth         varchar2(15)         NOT NULL,\n");
    buf.append("commid       number(8)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (arid)");
    if (includeUniqueKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_uk unique (sta,time)");
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
    return 210;
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
    return (other instanceof Arrival) && ((Arrival) other).arid == arid;
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
    return (other instanceof Arrival) && ((Arrival) other).sta.equals(sta)
        && ((Arrival) other).time == time;
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
  public Arrival setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
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
  public Arrival setTime(double time) {
    this.time = time;
    setHash(null);
    return this;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique sta, chan and time. This number is used in the assoc relation along with the origin
   * identifier to link arrival and origin.
   * 
   * @return arid
   */
  public long getArid() {
    return arid;
  }

  /**
   * Arrival identifier. Each arrival is assigned a unique positive integer identifying it with a
   * unique sta, chan and time. This number is used in the assoc relation along with the origin
   * identifier to link arrival and origin.
   * 
   * @param arid
   * @throws IllegalArgumentException if arid >= 100000000
   */
  public Arrival setArid(long arid) {
    if (arid >= 100000000L)
      throw new IllegalArgumentException("arid=" + arid + " but cannot be >= 100000000");
    this.arid = arid;
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
  public Arrival setJdate(long jdate) {
    if (jdate >= 100000000L)
      throw new IllegalArgumentException("jdate=" + jdate + " but cannot be >= 100000000");
    this.jdate = jdate;
    setHash(null);
    return this;
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
  public Arrival setStassid(long stassid) {
    if (stassid >= 100000000L)
      throw new IllegalArgumentException("stassid=" + stassid + " but cannot be >= 100000000");
    this.stassid = stassid;
    setHash(null);
    return this;
  }

  /**
   * Channel recording identifier. This is a surrogate key used to uniquely identify a specific
   * recording. Chanid duplicates the information of the compound key sta, chan, time. As a single
   * identifier it is often convenient. Chanid is very database dependent and is included only for
   * backward compatibility with historic databases. Sta, chan and time is more appropriate to the
   * human interface.
   * 
   * @return chanid
   */
  public long getChanid() {
    return chanid;
  }

  /**
   * Channel recording identifier. This is a surrogate key used to uniquely identify a specific
   * recording. Chanid duplicates the information of the compound key sta, chan, time. As a single
   * identifier it is often convenient. Chanid is very database dependent and is included only for
   * backward compatibility with historic databases. Sta, chan and time is more appropriate to the
   * human interface.
   * 
   * @param chanid
   * @throws IllegalArgumentException if chanid >= 100000000
   */
  public Arrival setChanid(long chanid) {
    if (chanid >= 100000000L)
      throw new IllegalArgumentException("chanid=" + chanid + " but cannot be >= 100000000");
    this.chanid = chanid;
    setHash(null);
    return this;
  }

  /**
   * Channel identifier. This is an eight-character code which, taken together with sta, jdate and
   * time, uniquely identifies the source of the seismic data, including the geographic location,
   * spatial orientation, sensor, and subsequent data processing.
   * 
   * @return chan
   */
  public String getChan() {
    return chan;
  }

  /**
   * Channel identifier. This is an eight-character code which, taken together with sta, jdate and
   * time, uniquely identifies the source of the seismic data, including the geographic location,
   * spatial orientation, sensor, and subsequent data processing.
   * 
   * @param chan
   * @throws IllegalArgumentException if chan.length() >= 8
   */
  public Arrival setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
    setHash(null);
    return this;
  }

  /**
   * Reported phase. This eight-character field holds the name initially given to a seismic phase.
   * Standard seismological labels for the types of signals (or phases) are used (e.g., P, PKP, PcP,
   * pP). Both upper and lower case letters are available and should be used when appropriate, for
   * example, pP or PcP. See phase.
   * 
   * @return iphase
   */
  public String getIphase() {
    return iphase;
  }

  /**
   * Reported phase. This eight-character field holds the name initially given to a seismic phase.
   * Standard seismological labels for the types of signals (or phases) are used (e.g., P, PKP, PcP,
   * pP). Both upper and lower case letters are available and should be used when appropriate, for
   * example, pP or PcP. See phase.
   * 
   * @param iphase
   * @throws IllegalArgumentException if iphase.length() >= 8
   */
  public Arrival setIphase(String iphase) {
    if (iphase.length() > 8)
      throw new IllegalArgumentException(
          String.format("iphase.length() cannot be > 8.  iphase=%s", iphase));
    this.iphase = iphase;
    setHash(null);
    return this;
  }

  /**
   * Signal type. This single-character flag indicates the event or signal type. The following
   * definitions hold: l = local event r = regional event t = teleseismic event m = mixed or
   * multiple event g = glitch (i.e., non-seismic detection) c = calibration activity upsets the
   * data l, r, and t are supplied by the reporting station, or as an output of post-detection
   * processing. g and c come from analyst comment or from status bits from GDSN and RSTN data.
   * 
   * @return stype
   */
  public String getStype() {
    return stype;
  }

  /**
   * Signal type. This single-character flag indicates the event or signal type. The following
   * definitions hold: l = local event r = regional event t = teleseismic event m = mixed or
   * multiple event g = glitch (i.e., non-seismic detection) c = calibration activity upsets the
   * data l, r, and t are supplied by the reporting station, or as an output of post-detection
   * processing. g and c come from analyst comment or from status bits from GDSN and RSTN data.
   * 
   * @param stype
   * @throws IllegalArgumentException if stype.length() >= 1
   */
  public Arrival setStype(String stype) {
    if (stype.length() > 1)
      throw new IllegalArgumentException(
          String.format("stype.length() cannot be > 1.  stype=%s", stype));
    this.stype = stype;
    setHash(null);
    return this;
  }

  /**
   * Delta time. This attribute gives the standard deviation of a detection time.
   * <p>
   * Units: s
   * 
   * @return deltim
   */
  public double getDeltim() {
    return deltim;
  }

  /**
   * Delta time. This attribute gives the standard deviation of a detection time.
   * <p>
   * Units: s
   * 
   * @param deltim
   */
  public Arrival setDeltim(double deltim) {
    this.deltim = deltim;
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
  public Arrival setAzimuth(double azimuth) {
    this.azimuth = azimuth;
    setHash(null);
    return this;
  }

  /**
   * Delta azimuth. This attribute gives the standard deviation of the azimuth of a signal.
   * <p>
   * Units: degree
   * 
   * @return delaz
   */
  public double getDelaz() {
    return delaz;
  }

  /**
   * Delta azimuth. This attribute gives the standard deviation of the azimuth of a signal.
   * <p>
   * Units: degree
   * 
   * @param delaz
   */
  public Arrival setDelaz(double delaz) {
    this.delaz = delaz;
    setHash(null);
    return this;
  }

  /**
   * Observed slowness. This is the observed slowness of a wave as it sweeps across an array.
   * <p>
   * Units: s/degree
   * 
   * @return slow
   */
  public double getSlow() {
    return slow;
  }

  /**
   * Observed slowness. This is the observed slowness of a wave as it sweeps across an array.
   * <p>
   * Units: s/degree
   * 
   * @param slow
   */
  public Arrival setSlow(double slow) {
    this.slow = slow;
    setHash(null);
    return this;
  }

  /**
   * Delta slowness. This attribute gives the standard deviation of the slowness of a signal.
   * <p>
   * Units: s/degree
   * 
   * @return delslo
   */
  public double getDelslo() {
    return delslo;
  }

  /**
   * Delta slowness. This attribute gives the standard deviation of the slowness of a signal.
   * <p>
   * Units: s/degree
   * 
   * @param delslo
   */
  public Arrival setDelslo(double delslo) {
    this.delslo = delslo;
    setHash(null);
    return this;
  }

  /**
   * Emergence angle. This attribute is the emergence angle of an arrival, as observed at a three-
   * component station or array. The value increases from the vertical direction towards the
   * horizontal.
   * <p>
   * Units: degree
   * 
   * @return ema
   */
  public double getEma() {
    return ema;
  }

  /**
   * Emergence angle. This attribute is the emergence angle of an arrival, as observed at a three-
   * component station or array. The value increases from the vertical direction towards the
   * horizontal.
   * <p>
   * Units: degree
   * 
   * @param ema
   */
  public Arrival setEma(double ema) {
    this.ema = ema;
    setHash(null);
    return this;
  }

  /**
   * Rectilinearity. This attribute is a measure of signal rectilinearity. The value is obtained
   * from polarization analysis of 3-component data.
   * 
   * @return rect
   */
  public double getRect() {
    return rect;
  }

  /**
   * Rectilinearity. This attribute is a measure of signal rectilinearity. The value is obtained
   * from polarization analysis of 3-component data.
   * 
   * @param rect
   */
  public Arrival setRect(double rect) {
    this.rect = rect;
    setHash(null);
    return this;
  }

  /**
   * Signal amplitude. This is the zero-to-peak amplitude of the earth's displacement for a seismic
   * phase. Amp is assumed to be corrected for the response of the instrument.
   * <p>
   * Units: nm
   * 
   * @return amp
   */
  public double getAmp() {
    return amp;
  }

  /**
   * Signal amplitude. This is the zero-to-peak amplitude of the earth's displacement for a seismic
   * phase. Amp is assumed to be corrected for the response of the instrument.
   * <p>
   * Units: nm
   * 
   * @param amp
   */
  public Arrival setAmp(double amp) {
    this.amp = amp;
    setHash(null);
    return this;
  }

  /**
   * Signal period. This attribute is the period of the signal describe by the arrival record.
   * <p>
   * Units: s
   * 
   * @return per
   */
  public double getPer() {
    return per;
  }

  /**
   * Signal period. This attribute is the period of the signal describe by the arrival record.
   * <p>
   * Units: s
   * 
   * @param per
   */
  public Arrival setPer(double per) {
    this.per = per;
    setHash(null);
    return this;
  }

  /**
   * Log of amplitude divided by period. This measurement of signal size is often reported instead
   * of the amplitude and period separately. This attribute is only filled if the separate
   * measurements are not available.
   * <p>
   * Units: log10(nm/s)
   * 
   * @return logat
   */
  public double getLogat() {
    return logat;
  }

  /**
   * Log of amplitude divided by period. This measurement of signal size is often reported instead
   * of the amplitude and period separately. This attribute is only filled if the separate
   * measurements are not available.
   * <p>
   * Units: log10(nm/s)
   * 
   * @param logat
   */
  public Arrival setLogat(double logat) {
    this.logat = logat;
    setHash(null);
    return this;
  }

  /**
   * Clipped data flag. This is a single-character flag to indicate whether (c) or not (n) the data
   * were clipped. Typically, this flag is derived from status bits supplied with GDSN or RSTN data,
   * but could also be supplied as a result of analyst review.
   * 
   * @return clip
   */
  public String getClip() {
    return clip;
  }

  /**
   * Clipped data flag. This is a single-character flag to indicate whether (c) or not (n) the data
   * were clipped. Typically, this flag is derived from status bits supplied with GDSN or RSTN data,
   * but could also be supplied as a result of analyst review.
   * 
   * @param clip
   * @throws IllegalArgumentException if clip.length() >= 1
   */
  public Arrival setClip(String clip) {
    if (clip.length() > 1)
      throw new IllegalArgumentException(
          String.format("clip.length() cannot be > 1.  clip=%s", clip));
    this.clip = clip;
    setHash(null);
    return this;
  }

  /**
   * First motion. This is a two-character indication of first motion. The first character describes
   * first motion seen on short-period channels and the second holds for long-period instruments.
   * Compression (dilatation) on a short-period sensor is denoted by c(d) and compression
   * (dilatation) on a long-period sensor is denoted by u(r). Empty character positions will be
   * indicated by dots (e.g., ".r").
   * 
   * @return fm
   */
  public String getFm() {
    return fm;
  }

  /**
   * First motion. This is a two-character indication of first motion. The first character describes
   * first motion seen on short-period channels and the second holds for long-period instruments.
   * Compression (dilatation) on a short-period sensor is denoted by c(d) and compression
   * (dilatation) on a long-period sensor is denoted by u(r). Empty character positions will be
   * indicated by dots (e.g., ".r").
   * 
   * @param fm
   * @throws IllegalArgumentException if fm.length() >= 2
   */
  public Arrival setFm(String fm) {
    if (fm.length() > 2)
      throw new IllegalArgumentException(String.format("fm.length() cannot be > 2.  fm=%s", fm));
    this.fm = fm;
    setHash(null);
    return this;
  }

  /**
   * Signal-to-noise ratio. This is an estimate of the size of the signal to that of the noise
   * immediately preceding it.
   * 
   * @return snr
   */
  public double getSnr() {
    return snr;
  }

  /**
   * Signal-to-noise ratio. This is an estimate of the size of the signal to that of the noise
   * immediately preceding it.
   * 
   * @param snr
   */
  public Arrival setSnr(double snr) {
    this.snr = snr;
    setHash(null);
    return this;
  }

  /**
   * Onset quality. This single-character flag is used to denote the sharpness of the onset of a
   * seismic phase. This relates to the timing accuracy as follows: i (impulsive) - accurate to +/-
   * 0.2 seconds e (emergent) - accuracy between +/- (0.2 to 1.0 seconds) w (weak) - timing
   * uncertain to > 1 second.
   * 
   * @return qual
   */
  public String getQual() {
    return qual;
  }

  /**
   * Onset quality. This single-character flag is used to denote the sharpness of the onset of a
   * seismic phase. This relates to the timing accuracy as follows: i (impulsive) - accurate to +/-
   * 0.2 seconds e (emergent) - accuracy between +/- (0.2 to 1.0 seconds) w (weak) - timing
   * uncertain to > 1 second.
   * 
   * @param qual
   * @throws IllegalArgumentException if qual.length() >= 1
   */
  public Arrival setQual(String qual) {
    if (qual.length() > 1)
      throw new IllegalArgumentException(
          String.format("qual.length() cannot be > 1.  qual=%s", qual));
    this.qual = qual;
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
  public Arrival setAuth(String auth) {
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
  public Arrival setCommid(long commid) {
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
