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
 * discrim_data
 */
public class Discrim_data extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   */
  private String chan;

  static final public String CHAN_NA = null;

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   */
  private String phase;

  static final public String PHASE_NA = null;

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   */
  private long evid;

  static final public long EVID_NA = Long.MIN_VALUE;

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   */
  private long orid;

  static final public long ORID_NA = Long.MIN_VALUE;

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   */
  private long windowid;

  static final public long WINDOWID_NA = Long.MIN_VALUE;

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   */
  private long polyid;

  static final public long POLYID_NA = -1;

  /**
   * Frequency dependent correction identifier parameter for MDAC processing.
   */
  private long fdid;

  static final public long FDID_NA = Long.MIN_VALUE;

  /**
   * Geographic latitude. Locations north of equator have positive latitudes.
   * <p>
   * Units: degree
   */
  private double lat;

  static final public double LAT_NA = Double.NaN;

  /**
   * Geographic longitude. Longitudes are measured positive East of the Greenwich meridian.
   * <p>
   * Units: degree
   */
  private double lon;

  static final public double LON_NA = Double.NaN;

  /**
   * Event depth
   * <p>
   * Units: km
   */
  private double depth;

  static final public double DEPTH_NA = Double.NaN;

  /**
   * Julian date of origin.
   */
  private long jdate;

  static final public long JDATE_NA = Long.MIN_VALUE;

  /**
   * Epoch time
   * <p>
   * Units: s
   */
  private double time;

  static final public double TIME_NA = Double.NaN;

  /**
   * Body wave magnitude
   */
  private double mb;

  static final public double MB_NA = -999;

  /**
   * Magnitude identifier for <I>Mb</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>mbmagid</I> is a foreign key joining <B>discrim_data</B> to
   * <B>netmag</B> where discrim_data.mbmagid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   */
  private long mbmagid;

  static final public long MBMAGID_NA = -1;

  /**
   * Surface wave magnitude.
   */
  private double ms;

  static final public double MS_NA = -999;

  /**
   * Magnitude identifier for <I>Ms</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>msmagid</I> is a foreign key joining <B>discrim_data</B> to
   * <B>netmag</B> where discrim_data.msmagid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   */
  private long msmagid;

  static final public long MSMAGID_NA = -1;

  /**
   * Moment magnitude; for discrim_data it is the moment magnitude for the MDAC correction.
   */
  private double mw;

  static final public double MW_NA = Double.NaN;

  /**
   * Magnitude identifier for <I>Mw</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>mwmagid</I> is a foreign key joining <B>discrim_data</B> to
   * <B>netmag</B> where discrim_data.mwmagid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   */
  private long mwmagid;

  static final public long MWMAGID_NA = Long.MIN_VALUE;

  /**
   * Type of seismic event, when known. The following etypes are defined: ex generic explosion ec
   * chemical explosion ep probable explosion en nuclear explosion mc collapse me coal bump/mining
   * event mp probable mining event mb rock burst qt generic earthquake/tectonic qd damaging
   * earthquake qp unknown-probable earthquake qf felt earthquake qm multiple shocks qh quake with
   * associated Harmonic Tremor qv long period event e.g. slow earthquake q2 double shock q4
   * foreshock qa aftershock ge geyser xm meteoritic origin xl lights xo odors - unknown
   */
  private String etype;

  static final public String ETYPE_NA = null;

  /**
   * Source-receiver distance. Calculated using origin specified by <I>orid</I>
   * (<B>nnsa_amp_descript</B>).
   * <p>
   * Units: degree
   */
  private double delta;

  static final public double DELTA_NA = Double.NaN;

  /**
   * Time offset used of Pn arrival from pre-specified group velocity in seconds. Used as an origin
   * offset correction for other phases. (<B>discrim_data</B>). Time offset relative to a
   * theoretical arrival time (<B>nnsa_amp_descript</B>)
   * <p>
   * Units: s
   */
  private double toff;

  static final public double TOFF_NA = -999;

  /**
   * Amplitude measurement type as defined below. This identifies how the attributes
   * <I>f_t_value</I>, <I>f_t_del</I>, <I>f_t_low</I> and <I>f_t_hi</I> are recorded in
   * <B>nnsa_amplitude</B>. FREQ - Frequency Domain Record PTOP - Pseudo Spectral Conversion of
   * Peak-to-Peak Time PENV - Pseudo Spectral Conversion of Peak Time Domain Envelope Measure TRMS -
   * Pseudo Spectral Conversion of Time Domain RMS Measure
   */
  private String f_t_type;

  static final public String F_T_TYPE_NA = null;

  /**
   * Low frequency of amplitude measure used in MDAC processing.
   * <p>
   * Units: Hz
   */
  private double lfreq;

  static final public double LFREQ_NA = Double.NaN;

  /**
   * High frequency of amplitude measure used in MDAC processing.
   * <p>
   * Units: Hz
   */
  private double hfreq;

  static final public double HFREQ_NA = Double.NaN;

  /**
   * Pre-event signal-to-noise ratio for a given event-station-channel-phase-frequency band.
   * <p>
   * Units: linear
   */
  private double snr_event;

  static final public double SNR_EVENT_NA = -1;

  /**
   * Pre-phase signal-to-noise ratio for a given event-station-channel-phase-frequency band.
   * <p>
   * Units: linear
   */
  private double snr_phase;

  static final public double SNR_PHASE_NA = -1;

  /**
   * Amplitude of the given event-station-channel-phase-frequency band for the raw data (no
   * corrections).
   * <p>
   * Units: log10(m/Hz)
   */
  private double ampraw;

  static final public double AMPRAW_NA = Double.NaN;

  /**
   * Uncertainty in raw average amplitude of a given event-station-phase-frequency band.
   * <p>
   * Units: log10(m/Hz)
   */
  private double delampraw;

  static final public double DELAMPRAW_NA = -999;

  /**
   * MDAC amplitude in the specified frequency band.
   * <p>
   * Units: log10(m/Hz)
   */
  private double ampmdac;

  static final public double AMPMDAC_NA = Double.NaN;

  /**
   * Uncertainty in MDAC corrected amplitude.
   * <p>
   * Units: log10(m/Hz)
   */
  private double delampmdac;

  static final public double DELAMPMDAC_NA = -999;

  /**
   * Additional path correction (e.g. kriging) for <I>ampmdac</I>.
   * <p>
   * Units: log10(m/Hz)
   */
  private double amppath;

  static final public double AMPPATH_NA = -999;

  /**
   * Uncertainty in additional path (e.g. kriging) correction.
   * <p>
   * Units: log10(m/Hz)
   */
  private double delamppath;

  static final public double DELAMPPATH_NA = -999;

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   */
  private long corrid;

  static final public long CORRID_NA = Long.MIN_VALUE;

  /**
   * Name of correction or other processing table applied to the amplitude. Examples are raw,
   * band-averaged, MDAC, phase-match filter, Coda, stack, beam, etc.
   */
  private String corrname;

  static final public String CORRNAME_NA = null;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("sta", Columns.FieldType.STRING, "%s");
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("phase", Columns.FieldType.STRING, "%s");
    columns.add("evid", Columns.FieldType.LONG, "%d");
    columns.add("orid", Columns.FieldType.LONG, "%d");
    columns.add("windowid", Columns.FieldType.LONG, "%d");
    columns.add("polyid", Columns.FieldType.LONG, "%d");
    columns.add("fdid", Columns.FieldType.LONG, "%d");
    columns.add("lat", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("lon", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("depth", Columns.FieldType.DOUBLE, "%1.4f");
    columns.add("jdate", Columns.FieldType.LONG, "%d");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("mb", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("mbmagid", Columns.FieldType.LONG, "%d");
    columns.add("ms", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("msmagid", Columns.FieldType.LONG, "%d");
    columns.add("mw", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("mwmagid", Columns.FieldType.LONG, "%d");
    columns.add("etype", Columns.FieldType.STRING, "%s");
    columns.add("delta", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("toff", Columns.FieldType.DOUBLE, "%1.3f");
    columns.add("f_t_type", Columns.FieldType.STRING, "%s");
    columns.add("lfreq", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("hfreq", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("snr_event", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("snr_phase", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("ampraw", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("delampraw", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("ampmdac", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("delampmdac", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("amppath", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("delamppath", Columns.FieldType.DOUBLE, "%1.2f");
    columns.add("corrid", Columns.FieldType.LONG, "%d");
    columns.add("corrname", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Discrim_data(String sta, String chan, String phase, long evid, long orid, long windowid,
      long polyid, long fdid, double lat, double lon, double depth, long jdate, double time,
      double mb, long mbmagid, double ms, long msmagid, double mw, long mwmagid, String etype,
      double delta, double toff, String f_t_type, double lfreq, double hfreq, double snr_event,
      double snr_phase, double ampraw, double delampraw, double ampmdac, double delampmdac,
      double amppath, double delamppath, long corrid, String corrname, String auth) {
    setValues(sta, chan, phase, evid, orid, windowid, polyid, fdid, lat, lon, depth, jdate, time,
        mb, mbmagid, ms, msmagid, mw, mwmagid, etype, delta, toff, f_t_type, lfreq, hfreq,
        snr_event, snr_phase, ampraw, delampraw, ampmdac, delampmdac, amppath, delamppath, corrid,
        corrname, auth);
  }

  private void setValues(String sta, String chan, String phase, long evid, long orid, long windowid,
      long polyid, long fdid, double lat, double lon, double depth, long jdate, double time,
      double mb, long mbmagid, double ms, long msmagid, double mw, long mwmagid, String etype,
      double delta, double toff, String f_t_type, double lfreq, double hfreq, double snr_event,
      double snr_phase, double ampraw, double delampraw, double ampmdac, double delampmdac,
      double amppath, double delamppath, long corrid, String corrname, String auth) {
    this.sta = sta;
    this.chan = chan;
    this.phase = phase;
    this.evid = evid;
    this.orid = orid;
    this.windowid = windowid;
    this.polyid = polyid;
    this.fdid = fdid;
    this.lat = lat;
    this.lon = lon;
    this.depth = depth;
    this.jdate = jdate;
    this.time = time;
    this.mb = mb;
    this.mbmagid = mbmagid;
    this.ms = ms;
    this.msmagid = msmagid;
    this.mw = mw;
    this.mwmagid = mwmagid;
    this.etype = etype;
    this.delta = delta;
    this.toff = toff;
    this.f_t_type = f_t_type;
    this.lfreq = lfreq;
    this.hfreq = hfreq;
    this.snr_event = snr_event;
    this.snr_phase = snr_phase;
    this.ampraw = ampraw;
    this.delampraw = delampraw;
    this.ampmdac = ampmdac;
    this.delampmdac = delampmdac;
    this.amppath = amppath;
    this.delamppath = delamppath;
    this.corrid = corrid;
    this.corrname = corrname;
    this.auth = auth;
  }

  /**
   * Copy constructor.
   */
  public Discrim_data(Discrim_data other) {
    this.sta = other.getSta();
    this.chan = other.getChan();
    this.phase = other.getPhase();
    this.evid = other.getEvid();
    this.orid = other.getOrid();
    this.windowid = other.getWindowid();
    this.polyid = other.getPolyid();
    this.fdid = other.getFdid();
    this.lat = other.getLat();
    this.lon = other.getLon();
    this.depth = other.getDepth();
    this.jdate = other.getJdate();
    this.time = other.getTime();
    this.mb = other.getMb();
    this.mbmagid = other.getMbmagid();
    this.ms = other.getMs();
    this.msmagid = other.getMsmagid();
    this.mw = other.getMw();
    this.mwmagid = other.getMwmagid();
    this.etype = other.getEtype();
    this.delta = other.getDelta();
    this.toff = other.getToff();
    this.f_t_type = other.getF_t_type();
    this.lfreq = other.getLfreq();
    this.hfreq = other.getHfreq();
    this.snr_event = other.getSnr_event();
    this.snr_phase = other.getSnr_phase();
    this.ampraw = other.getAmpraw();
    this.delampraw = other.getDelampraw();
    this.ampmdac = other.getAmpmdac();
    this.delampmdac = other.getDelampmdac();
    this.amppath = other.getAmppath();
    this.delamppath = other.getDelamppath();
    this.corrid = other.getCorrid();
    this.corrname = other.getCorrname();
    this.auth = other.getAuth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Discrim_data() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(STA_NA, CHAN_NA, PHASE_NA, EVID_NA, ORID_NA, WINDOWID_NA, POLYID_NA, FDID_NA, LAT_NA,
        LON_NA, DEPTH_NA, JDATE_NA, TIME_NA, MB_NA, MBMAGID_NA, MS_NA, MSMAGID_NA, MW_NA,
        MWMAGID_NA, ETYPE_NA, DELTA_NA, TOFF_NA, F_T_TYPE_NA, LFREQ_NA, HFREQ_NA, SNR_EVENT_NA,
        SNR_PHASE_NA, AMPRAW_NA, DELAMPRAW_NA, AMPMDAC_NA, DELAMPMDAC_NA, AMPPATH_NA, DELAMPPATH_NA,
        CORRID_NA, CORRNAME_NA, AUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "chan":
        return chan;
      case "phase":
        return phase;
      case "etype":
        return etype;
      case "f_t_type":
        return f_t_type;
      case "corrname":
        return corrname;
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
      case "phase":
        phase = value;
        break;
      case "etype":
        etype = value;
        break;
      case "f_t_type":
        f_t_type = value;
        break;
      case "corrname":
        corrname = value;
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
      case "mb":
        return mb;
      case "ms":
        return ms;
      case "mw":
        return mw;
      case "delta":
        return delta;
      case "toff":
        return toff;
      case "lfreq":
        return lfreq;
      case "hfreq":
        return hfreq;
      case "snr_event":
        return snr_event;
      case "snr_phase":
        return snr_phase;
      case "ampraw":
        return ampraw;
      case "delampraw":
        return delampraw;
      case "ampmdac":
        return ampmdac;
      case "delampmdac":
        return delampmdac;
      case "amppath":
        return amppath;
      case "delamppath":
        return delamppath;
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
      case "mb":
        mb = value;
        break;
      case "ms":
        ms = value;
        break;
      case "mw":
        mw = value;
        break;
      case "delta":
        delta = value;
        break;
      case "toff":
        toff = value;
        break;
      case "lfreq":
        lfreq = value;
        break;
      case "hfreq":
        hfreq = value;
        break;
      case "snr_event":
        snr_event = value;
        break;
      case "snr_phase":
        snr_phase = value;
        break;
      case "ampraw":
        ampraw = value;
        break;
      case "delampraw":
        delampraw = value;
        break;
      case "ampmdac":
        ampmdac = value;
        break;
      case "delampmdac":
        delampmdac = value;
        break;
      case "amppath":
        amppath = value;
        break;
      case "delamppath":
        delamppath = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "evid":
        return evid;
      case "orid":
        return orid;
      case "windowid":
        return windowid;
      case "polyid":
        return polyid;
      case "fdid":
        return fdid;
      case "jdate":
        return jdate;
      case "mbmagid":
        return mbmagid;
      case "msmagid":
        return msmagid;
      case "mwmagid":
        return mwmagid;
      case "corrid":
        return corrid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "evid":
        evid = value;
        break;
      case "orid":
        orid = value;
        break;
      case "windowid":
        windowid = value;
        break;
      case "polyid":
        polyid = value;
        break;
      case "fdid":
        fdid = value;
        break;
      case "jdate":
        jdate = value;
        break;
      case "mbmagid":
        mbmagid = value;
        break;
      case "msmagid":
        msmagid = value;
        break;
      case "mwmagid":
        mwmagid = value;
        break;
      case "corrid":
        corrid = value;
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
  public Discrim_data(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Discrim_data(DataInputStream input) throws IOException {
    this(readString(input), readString(input), readString(input), input.readLong(),
        input.readLong(), input.readLong(), input.readLong(), input.readLong(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readLong(), input.readDouble(),
        input.readDouble(), input.readLong(), input.readDouble(), input.readLong(),
        input.readDouble(), input.readLong(), readString(input), input.readDouble(),
        input.readDouble(), readString(input), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readDouble(), input.readDouble(), input.readDouble(), input.readDouble(),
        input.readLong(), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Discrim_data(ByteBuffer input) {
    this(readString(input), readString(input), readString(input), input.getLong(), input.getLong(),
        input.getLong(), input.getLong(), input.getLong(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getLong(), input.getDouble(), input.getDouble(), input.getLong(),
        input.getDouble(), input.getLong(), input.getDouble(), input.getLong(), readString(input),
        input.getDouble(), input.getDouble(), readString(input), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getDouble(), input.getDouble(), input.getDouble(),
        input.getDouble(), input.getLong(), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Discrim_data(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Discrim_data(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getLong(offset + 7), input.getLong(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getLong(offset + 12),
        input.getDouble(offset + 13), input.getDouble(offset + 14), input.getLong(offset + 15),
        input.getDouble(offset + 16), input.getLong(offset + 17), input.getDouble(offset + 18),
        input.getLong(offset + 19), input.getString(offset + 20), input.getDouble(offset + 21),
        input.getDouble(offset + 22), input.getString(offset + 23), input.getDouble(offset + 24),
        input.getDouble(offset + 25), input.getDouble(offset + 26), input.getDouble(offset + 27),
        input.getDouble(offset + 28), input.getDouble(offset + 29), input.getDouble(offset + 30),
        input.getDouble(offset + 31), input.getDouble(offset + 32), input.getDouble(offset + 33),
        input.getLong(offset + 34), input.getString(offset + 35), input.getString(offset + 36));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[36];
    values[0] = sta;
    values[1] = chan;
    values[2] = phase;
    values[3] = evid;
    values[4] = orid;
    values[5] = windowid;
    values[6] = polyid;
    values[7] = fdid;
    values[8] = lat;
    values[9] = lon;
    values[10] = depth;
    values[11] = jdate;
    values[12] = time;
    values[13] = mb;
    values[14] = mbmagid;
    values[15] = ms;
    values[16] = msmagid;
    values[17] = mw;
    values[18] = mwmagid;
    values[19] = etype;
    values[20] = delta;
    values[21] = toff;
    values[22] = f_t_type;
    values[23] = lfreq;
    values[24] = hfreq;
    values[25] = snr_event;
    values[26] = snr_phase;
    values[27] = ampraw;
    values[28] = delampraw;
    values[29] = ampmdac;
    values[30] = delampmdac;
    values[31] = amppath;
    values[32] = delamppath;
    values[33] = corrid;
    values[34] = corrname;
    values[35] = auth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[37];
    values[0] = sta;
    values[1] = chan;
    values[2] = phase;
    values[3] = evid;
    values[4] = orid;
    values[5] = windowid;
    values[6] = polyid;
    values[7] = fdid;
    values[8] = lat;
    values[9] = lon;
    values[10] = depth;
    values[11] = jdate;
    values[12] = time;
    values[13] = mb;
    values[14] = mbmagid;
    values[15] = ms;
    values[16] = msmagid;
    values[17] = mw;
    values[18] = mwmagid;
    values[19] = etype;
    values[20] = delta;
    values[21] = toff;
    values[22] = f_t_type;
    values[23] = lfreq;
    values[24] = hfreq;
    values[25] = snr_event;
    values[26] = snr_phase;
    values[27] = ampraw;
    values[28] = delampraw;
    values[29] = ampmdac;
    values[30] = delampmdac;
    values[31] = amppath;
    values[32] = delamppath;
    values[33] = corrid;
    values[34] = corrname;
    values[35] = auth;
    values[36] = lddate;
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
    writeString(output, chan);
    writeString(output, phase);
    output.writeLong(evid);
    output.writeLong(orid);
    output.writeLong(windowid);
    output.writeLong(polyid);
    output.writeLong(fdid);
    output.writeDouble(lat);
    output.writeDouble(lon);
    output.writeDouble(depth);
    output.writeLong(jdate);
    output.writeDouble(time);
    output.writeDouble(mb);
    output.writeLong(mbmagid);
    output.writeDouble(ms);
    output.writeLong(msmagid);
    output.writeDouble(mw);
    output.writeLong(mwmagid);
    writeString(output, etype);
    output.writeDouble(delta);
    output.writeDouble(toff);
    writeString(output, f_t_type);
    output.writeDouble(lfreq);
    output.writeDouble(hfreq);
    output.writeDouble(snr_event);
    output.writeDouble(snr_phase);
    output.writeDouble(ampraw);
    output.writeDouble(delampraw);
    output.writeDouble(ampmdac);
    output.writeDouble(delampmdac);
    output.writeDouble(amppath);
    output.writeDouble(delamppath);
    output.writeLong(corrid);
    writeString(output, corrname);
    writeString(output, auth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, sta);
    writeString(output, chan);
    writeString(output, phase);
    output.putLong(evid);
    output.putLong(orid);
    output.putLong(windowid);
    output.putLong(polyid);
    output.putLong(fdid);
    output.putDouble(lat);
    output.putDouble(lon);
    output.putDouble(depth);
    output.putLong(jdate);
    output.putDouble(time);
    output.putDouble(mb);
    output.putLong(mbmagid);
    output.putDouble(ms);
    output.putLong(msmagid);
    output.putDouble(mw);
    output.putLong(mwmagid);
    writeString(output, etype);
    output.putDouble(delta);
    output.putDouble(toff);
    writeString(output, f_t_type);
    output.putDouble(lfreq);
    output.putDouble(hfreq);
    output.putDouble(snr_event);
    output.putDouble(snr_phase);
    output.putDouble(ampraw);
    output.putDouble(delampraw);
    output.putDouble(ampmdac);
    output.putDouble(delampmdac);
    output.putDouble(amppath);
    output.putDouble(delamppath);
    output.putLong(corrid);
    writeString(output, corrname);
    writeString(output, auth);
  }

  /**
   * Read a Collection of Discrim_data objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Discrim_data objects.
   * @throws IOException
   */
  static public void readDiscrim_datas(BufferedReader input, Collection<Discrim_data> rows)
      throws IOException {
    String[] saved = Discrim_data.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Discrim_data
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Discrim_data(new Scanner(line)));
    }
    input.close();
    Discrim_data.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Discrim_data objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Discrim_data objects.
   * @throws IOException
   */
  static public void readDiscrim_datas(File inputFile, Collection<Discrim_data> rows)
      throws IOException {
    readDiscrim_datas(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Discrim_data objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Discrim_data objects.
   * @throws IOException
   */
  static public void readDiscrim_datas(InputStream inputStream, Collection<Discrim_data> rows)
      throws IOException {
    readDiscrim_datas(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Discrim_data objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Discrim_data objects
   * @throws IOException
   */
  static public Set<Discrim_data> readDiscrim_datas(BufferedReader input) throws IOException {
    Set<Discrim_data> rows = new LinkedHashSet<Discrim_data>();
    readDiscrim_datas(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Discrim_data objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Discrim_data objects
   * @throws IOException
   */
  static public Set<Discrim_data> readDiscrim_datas(File inputFile) throws IOException {
    return readDiscrim_datas(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Discrim_data objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Discrim_data objects
   * @throws IOException
   */
  static public Set<Discrim_data> readDiscrim_datas(InputStream input) throws IOException {
    return readDiscrim_datas(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Discrim_data objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param discrim_datas the Discrim_data objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Discrim_data> discrim_datas)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Discrim_data discrim_data : discrim_datas)
      discrim_data.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Discrim_data objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param discrim_datas the Discrim_data objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Discrim_data> discrim_datas, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName
          + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Discrim_data discrim_data : discrim_datas) {
        int i = 0;
        statement.setString(++i, discrim_data.sta);
        statement.setString(++i, discrim_data.chan);
        statement.setString(++i, discrim_data.phase);
        statement.setLong(++i, discrim_data.evid);
        statement.setLong(++i, discrim_data.orid);
        statement.setLong(++i, discrim_data.windowid);
        statement.setLong(++i, discrim_data.polyid);
        statement.setLong(++i, discrim_data.fdid);
        statement.setDouble(++i, discrim_data.lat);
        statement.setDouble(++i, discrim_data.lon);
        statement.setDouble(++i, discrim_data.depth);
        statement.setLong(++i, discrim_data.jdate);
        statement.setDouble(++i, discrim_data.time);
        statement.setDouble(++i, discrim_data.mb);
        statement.setLong(++i, discrim_data.mbmagid);
        statement.setDouble(++i, discrim_data.ms);
        statement.setLong(++i, discrim_data.msmagid);
        statement.setDouble(++i, discrim_data.mw);
        statement.setLong(++i, discrim_data.mwmagid);
        statement.setString(++i, discrim_data.etype);
        statement.setDouble(++i, discrim_data.delta);
        statement.setDouble(++i, discrim_data.toff);
        statement.setString(++i, discrim_data.f_t_type);
        statement.setDouble(++i, discrim_data.lfreq);
        statement.setDouble(++i, discrim_data.hfreq);
        statement.setDouble(++i, discrim_data.snr_event);
        statement.setDouble(++i, discrim_data.snr_phase);
        statement.setDouble(++i, discrim_data.ampraw);
        statement.setDouble(++i, discrim_data.delampraw);
        statement.setDouble(++i, discrim_data.ampmdac);
        statement.setDouble(++i, discrim_data.delampmdac);
        statement.setDouble(++i, discrim_data.amppath);
        statement.setDouble(++i, discrim_data.delamppath);
        statement.setLong(++i, discrim_data.corrid);
        statement.setString(++i, discrim_data.corrname);
        statement.setString(++i, discrim_data.auth);
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
   *        Discrim_data table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Discrim_data> readDiscrim_datas(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Discrim_data> results = new HashSet<Discrim_data>();
    readDiscrim_datas(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Discrim_data table.
   * @param discrim_datas
   * @throws SQLException
   */
  static public void readDiscrim_datas(Connection connection, String selectStatement,
      Set<Discrim_data> discrim_datas) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        discrim_datas.add(new Discrim_data(rs));
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
   * this Discrim_data object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Discrim_data object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "sta, chan, phase, evid, orid, windowid, polyid, fdid, lat, lon, depth, jdate, time, mb, mbmagid, ms, msmagid, mw, mwmagid, etype, delta, toff, f_t_type, lfreq, hfreq, snr_event, snr_phase, ampraw, delampraw, ampmdac, delampmdac, amppath, delamppath, corrid, corrname, auth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(chan).append("', ");
    sql.append("'").append(phase).append("', ");
    sql.append(Long.toString(evid)).append(", ");
    sql.append(Long.toString(orid)).append(", ");
    sql.append(Long.toString(windowid)).append(", ");
    sql.append(Long.toString(polyid)).append(", ");
    sql.append(Long.toString(fdid)).append(", ");
    sql.append(Double.toString(lat)).append(", ");
    sql.append(Double.toString(lon)).append(", ");
    sql.append(Double.toString(depth)).append(", ");
    sql.append(Long.toString(jdate)).append(", ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Double.toString(mb)).append(", ");
    sql.append(Long.toString(mbmagid)).append(", ");
    sql.append(Double.toString(ms)).append(", ");
    sql.append(Long.toString(msmagid)).append(", ");
    sql.append(Double.toString(mw)).append(", ");
    sql.append(Long.toString(mwmagid)).append(", ");
    sql.append("'").append(etype).append("', ");
    sql.append(Double.toString(delta)).append(", ");
    sql.append(Double.toString(toff)).append(", ");
    sql.append("'").append(f_t_type).append("', ");
    sql.append(Double.toString(lfreq)).append(", ");
    sql.append(Double.toString(hfreq)).append(", ");
    sql.append(Double.toString(snr_event)).append(", ");
    sql.append(Double.toString(snr_phase)).append(", ");
    sql.append(Double.toString(ampraw)).append(", ");
    sql.append(Double.toString(delampraw)).append(", ");
    sql.append(Double.toString(ampmdac)).append(", ");
    sql.append(Double.toString(delampmdac)).append(", ");
    sql.append(Double.toString(amppath)).append(", ");
    sql.append(Double.toString(delamppath)).append(", ");
    sql.append(Long.toString(corrid)).append(", ");
    sql.append("'").append(corrname).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Discrim_data in the database. Primary and unique keys are set, if
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
   * Create a table of type Discrim_data in the database
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
   * Generate a sql script to create a table of type Discrim_data in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Discrim_data in the database
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
    buf.append("chan         varchar2(8)          NOT NULL,\n");
    buf.append("phase        varchar2(8)          NOT NULL,\n");
    buf.append("evid         number(9)            NOT NULL,\n");
    buf.append("orid         number(9)            NOT NULL,\n");
    buf.append("windowid     number(9)            NOT NULL,\n");
    buf.append("polyid       number(9)            NOT NULL,\n");
    buf.append("fdid         number(9)            NOT NULL,\n");
    buf.append("lat          float(53)            NOT NULL,\n");
    buf.append("lon          float(53)            NOT NULL,\n");
    buf.append("depth        float(24)            NOT NULL,\n");
    buf.append("jdate        number(8)            NOT NULL,\n");
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("mb           float(24)            NOT NULL,\n");
    buf.append("mbmagid      number(9)            NOT NULL,\n");
    buf.append("ms           float(24)            NOT NULL,\n");
    buf.append("msmagid      number(9)            NOT NULL,\n");
    buf.append("mw           float(24)            NOT NULL,\n");
    buf.append("mwmagid      number(9)            NOT NULL,\n");
    buf.append("etype        varchar2(7)          NOT NULL,\n");
    buf.append("delta        float(24)            NOT NULL,\n");
    buf.append("toff         float(24)            NOT NULL,\n");
    buf.append("f_t_type     varchar2(4)          NOT NULL,\n");
    buf.append("lfreq        float(24)            NOT NULL,\n");
    buf.append("hfreq        float(24)            NOT NULL,\n");
    buf.append("snr_event    float(24)            NOT NULL,\n");
    buf.append("snr_phase    float(24)            NOT NULL,\n");
    buf.append("ampraw       float(24)            NOT NULL,\n");
    buf.append("delampraw    float(24)            NOT NULL,\n");
    buf.append("ampmdac      float(24)            NOT NULL,\n");
    buf.append("delampmdac   float(24)            NOT NULL,\n");
    buf.append("amppath      float(24)            NOT NULL,\n");
    buf.append("delamppath   float(24)            NOT NULL,\n");
    buf.append("corrid       number(9)            NOT NULL,\n");
    buf.append("corrname     varchar2(32)         NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (windowid,f_t_type,lfreq)");
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
    return 345;
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
    return (other instanceof Discrim_data) && ((Discrim_data) other).windowid == windowid
        && ((Discrim_data) other).f_t_type.equals(f_t_type)
        && ((Discrim_data) other).lfreq == lfreq;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   * 
   * @return sta
   */
  public String getSta() {
    return sta;
  }

  /**
   * Station code. This is the code name of a seismic observatory and identifies a geographic
   * location record in the <B>site</B> table.
   * 
   * @param sta
   * @throws IllegalArgumentException if sta.length() >= 6
   */
  public Discrim_data setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
    setHash(null);
    return this;
  }

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   * 
   * @return chan
   */
  public String getChan() {
    return chan;
  }

  /**
   * Channel code. <I>Chan</I>,taken together with <I>sta</I>, <I>jdate</I>, and <I>time</I>,
   * uniquely identifies the seismic time-series data.
   * 
   * @param chan
   * @throws IllegalArgumentException if chan.length() >= 8
   */
  public Discrim_data setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
    setHash(null);
    return this;
  }

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   * 
   * @return phase
   */
  public String getPhase() {
    return phase;
  }

  /**
   * Phase type. The identity of a seismic phase that is associated with an amplitude or arrival
   * measurement.
   * 
   * @param phase
   * @throws IllegalArgumentException if phase.length() >= 8
   */
  public Discrim_data setPhase(String phase) {
    if (phase.length() > 8)
      throw new IllegalArgumentException(
          String.format("phase.length() cannot be > 8.  phase=%s", phase));
    this.phase = phase;
    setHash(null);
    return this;
  }

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   * 
   * @return evid
   */
  public long getEvid() {
    return evid;
  }

  /**
   * Event identifier; a unique positive integer that identifies the event in the database.
   * 
   * @param evid
   * @throws IllegalArgumentException if evid >= 1000000000
   */
  public Discrim_data setEvid(long evid) {
    if (evid >= 1000000000L)
      throw new IllegalArgumentException("evid=" + evid + " but cannot be >= 1000000000");
    this.evid = evid;
    setHash(null);
    return this;
  }

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   * 
   * @return orid
   */
  public long getOrid() {
    return orid;
  }

  /**
   * Origin identifier, from <B>event</B> and <B>origin</B> tables. For nnsa_amp_descript, this
   * could be, but is not restricted to the preferred origin (<I>prefor</I>) from the <B>event</B>
   * table.
   * 
   * @param orid
   * @throws IllegalArgumentException if orid >= 1000000000
   */
  public Discrim_data setOrid(long orid) {
    if (orid >= 1000000000L)
      throw new IllegalArgumentException("orid=" + orid + " but cannot be >= 1000000000");
    this.orid = orid;
    setHash(null);
    return this;
  }

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   * 
   * @return windowid
   */
  public long getWindowid() {
    return windowid;
  }

  /**
   * Unique identifier for waveform window, specific for an event-station-channel-phase.
   * 
   * @param windowid
   * @throws IllegalArgumentException if windowid >= 1000000000
   */
  public Discrim_data setWindowid(long windowid) {
    if (windowid >= 1000000000L)
      throw new IllegalArgumentException("windowid=" + windowid + " but cannot be >= 1000000000");
    this.windowid = windowid;
    setHash(null);
    return this;
  }

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   * 
   * @return polyid
   */
  public long getPolyid() {
    return polyid;
  }

  /**
   * Unique polygon identifier used for bounding Q tomography models.
   * 
   * @param polyid
   * @throws IllegalArgumentException if polyid >= 1000000000
   */
  public Discrim_data setPolyid(long polyid) {
    if (polyid >= 1000000000L)
      throw new IllegalArgumentException("polyid=" + polyid + " but cannot be >= 1000000000");
    this.polyid = polyid;
    setHash(null);
    return this;
  }

  /**
   * Frequency dependent correction identifier parameter for MDAC processing.
   * 
   * @return fdid
   */
  public long getFdid() {
    return fdid;
  }

  /**
   * Frequency dependent correction identifier parameter for MDAC processing.
   * 
   * @param fdid
   * @throws IllegalArgumentException if fdid >= 1000000000
   */
  public Discrim_data setFdid(long fdid) {
    if (fdid >= 1000000000L)
      throw new IllegalArgumentException("fdid=" + fdid + " but cannot be >= 1000000000");
    this.fdid = fdid;
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
  public Discrim_data setLat(double lat) {
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
  public Discrim_data setLon(double lon) {
    this.lon = lon;
    setHash(null);
    return this;
  }

  /**
   * Event depth
   * <p>
   * Units: km
   * 
   * @return depth
   */
  public double getDepth() {
    return depth;
  }

  /**
   * Event depth
   * <p>
   * Units: km
   * 
   * @param depth
   */
  public Discrim_data setDepth(double depth) {
    this.depth = depth;
    setHash(null);
    return this;
  }

  /**
   * Julian date of origin.
   * 
   * @return jdate
   */
  public long getJdate() {
    return jdate;
  }

  /**
   * Julian date of origin.
   * 
   * @param jdate
   * @throws IllegalArgumentException if jdate >= 100000000
   */
  public Discrim_data setJdate(long jdate) {
    if (jdate >= 100000000L)
      throw new IllegalArgumentException("jdate=" + jdate + " but cannot be >= 100000000");
    this.jdate = jdate;
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
  public Discrim_data setTime(double time) {
    this.time = time;
    setHash(null);
    return this;
  }

  /**
   * Body wave magnitude
   * 
   * @return mb
   */
  public double getMb() {
    return mb;
  }

  /**
   * Body wave magnitude
   * 
   * @param mb
   */
  public Discrim_data setMb(double mb) {
    this.mb = mb;
    setHash(null);
    return this;
  }

  /**
   * Magnitude identifier for <I>Mb</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>mbmagid</I> is a foreign key joining <B>discrim_data</B> to
   * <B>netmag</B> where discrim_data.mbmagid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   * 
   * @return mbmagid
   */
  public long getMbmagid() {
    return mbmagid;
  }

  /**
   * Magnitude identifier for <I>Mb</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>mbmagid</I> is a foreign key joining <B>discrim_data</B> to
   * <B>netmag</B> where discrim_data.mbmagid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   * 
   * @param mbmagid
   * @throws IllegalArgumentException if mbmagid >= 1000000000
   */
  public Discrim_data setMbmagid(long mbmagid) {
    if (mbmagid >= 1000000000L)
      throw new IllegalArgumentException("mbmagid=" + mbmagid + " but cannot be >= 1000000000");
    this.mbmagid = mbmagid;
    setHash(null);
    return this;
  }

  /**
   * Surface wave magnitude.
   * 
   * @return ms
   */
  public double getMs() {
    return ms;
  }

  /**
   * Surface wave magnitude.
   * 
   * @param ms
   */
  public Discrim_data setMs(double ms) {
    this.ms = ms;
    setHash(null);
    return this;
  }

  /**
   * Magnitude identifier for <I>Ms</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>msmagid</I> is a foreign key joining <B>discrim_data</B> to
   * <B>netmag</B> where discrim_data.msmagid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   * 
   * @return msmagid
   */
  public long getMsmagid() {
    return msmagid;
  }

  /**
   * Magnitude identifier for <I>Ms</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>msmagid</I> is a foreign key joining <B>discrim_data</B> to
   * <B>netmag</B> where discrim_data.msmagid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   * 
   * @param msmagid
   * @throws IllegalArgumentException if msmagid >= 1000000000
   */
  public Discrim_data setMsmagid(long msmagid) {
    if (msmagid >= 1000000000L)
      throw new IllegalArgumentException("msmagid=" + msmagid + " but cannot be >= 1000000000");
    this.msmagid = msmagid;
    setHash(null);
    return this;
  }

  /**
   * Moment magnitude; for discrim_data it is the moment magnitude for the MDAC correction.
   * 
   * @return mw
   */
  public double getMw() {
    return mw;
  }

  /**
   * Moment magnitude; for discrim_data it is the moment magnitude for the MDAC correction.
   * 
   * @param mw
   */
  public Discrim_data setMw(double mw) {
    this.mw = mw;
    setHash(null);
    return this;
  }

  /**
   * Magnitude identifier for <I>Mw</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>mwmagid</I> is a foreign key joining <B>discrim_data</B> to
   * <B>netmag</B> where discrim_data.mwmagid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   * 
   * @return mwmagid
   */
  public long getMwmagid() {
    return mwmagid;
  }

  /**
   * Magnitude identifier for <I>Mw</I>. This attribute stores the <I>magid</I> for a record in
   * <B>netmag</B>. The identifier <I>mwmagid</I> is a foreign key joining <B>discrim_data</B> to
   * <B>netmag</B> where discrim_data.mwmagid = netmag.magid (see <I>magid</I>, <I>mlid</I>, and
   * <I>msid</I>).
   * 
   * @param mwmagid
   * @throws IllegalArgumentException if mwmagid >= 1000000000
   */
  public Discrim_data setMwmagid(long mwmagid) {
    if (mwmagid >= 1000000000L)
      throw new IllegalArgumentException("mwmagid=" + mwmagid + " but cannot be >= 1000000000");
    this.mwmagid = mwmagid;
    setHash(null);
    return this;
  }

  /**
   * Type of seismic event, when known. The following etypes are defined: ex generic explosion ec
   * chemical explosion ep probable explosion en nuclear explosion mc collapse me coal bump/mining
   * event mp probable mining event mb rock burst qt generic earthquake/tectonic qd damaging
   * earthquake qp unknown-probable earthquake qf felt earthquake qm multiple shocks qh quake with
   * associated Harmonic Tremor qv long period event e.g. slow earthquake q2 double shock q4
   * foreshock qa aftershock ge geyser xm meteoritic origin xl lights xo odors - unknown
   * 
   * @return etype
   */
  public String getEtype() {
    return etype;
  }

  /**
   * Type of seismic event, when known. The following etypes are defined: ex generic explosion ec
   * chemical explosion ep probable explosion en nuclear explosion mc collapse me coal bump/mining
   * event mp probable mining event mb rock burst qt generic earthquake/tectonic qd damaging
   * earthquake qp unknown-probable earthquake qf felt earthquake qm multiple shocks qh quake with
   * associated Harmonic Tremor qv long period event e.g. slow earthquake q2 double shock q4
   * foreshock qa aftershock ge geyser xm meteoritic origin xl lights xo odors - unknown
   * 
   * @param etype
   * @throws IllegalArgumentException if etype.length() >= 7
   */
  public Discrim_data setEtype(String etype) {
    if (etype.length() > 7)
      throw new IllegalArgumentException(
          String.format("etype.length() cannot be > 7.  etype=%s", etype));
    this.etype = etype;
    setHash(null);
    return this;
  }

  /**
   * Source-receiver distance. Calculated using origin specified by <I>orid</I>
   * (<B>nnsa_amp_descript</B>).
   * <p>
   * Units: degree
   * 
   * @return delta
   */
  public double getDelta() {
    return delta;
  }

  /**
   * Source-receiver distance. Calculated using origin specified by <I>orid</I>
   * (<B>nnsa_amp_descript</B>).
   * <p>
   * Units: degree
   * 
   * @param delta
   */
  public Discrim_data setDelta(double delta) {
    this.delta = delta;
    setHash(null);
    return this;
  }

  /**
   * Time offset used of Pn arrival from pre-specified group velocity in seconds. Used as an origin
   * offset correction for other phases. (<B>discrim_data</B>). Time offset relative to a
   * theoretical arrival time (<B>nnsa_amp_descript</B>)
   * <p>
   * Units: s
   * 
   * @return toff
   */
  public double getToff() {
    return toff;
  }

  /**
   * Time offset used of Pn arrival from pre-specified group velocity in seconds. Used as an origin
   * offset correction for other phases. (<B>discrim_data</B>). Time offset relative to a
   * theoretical arrival time (<B>nnsa_amp_descript</B>)
   * <p>
   * Units: s
   * 
   * @param toff
   */
  public Discrim_data setToff(double toff) {
    this.toff = toff;
    setHash(null);
    return this;
  }

  /**
   * Amplitude measurement type as defined below. This identifies how the attributes
   * <I>f_t_value</I>, <I>f_t_del</I>, <I>f_t_low</I> and <I>f_t_hi</I> are recorded in
   * <B>nnsa_amplitude</B>. FREQ - Frequency Domain Record PTOP - Pseudo Spectral Conversion of
   * Peak-to-Peak Time PENV - Pseudo Spectral Conversion of Peak Time Domain Envelope Measure TRMS -
   * Pseudo Spectral Conversion of Time Domain RMS Measure
   * 
   * @return f_t_type
   */
  public String getF_t_type() {
    return f_t_type;
  }

  /**
   * Amplitude measurement type as defined below. This identifies how the attributes
   * <I>f_t_value</I>, <I>f_t_del</I>, <I>f_t_low</I> and <I>f_t_hi</I> are recorded in
   * <B>nnsa_amplitude</B>. FREQ - Frequency Domain Record PTOP - Pseudo Spectral Conversion of
   * Peak-to-Peak Time PENV - Pseudo Spectral Conversion of Peak Time Domain Envelope Measure TRMS -
   * Pseudo Spectral Conversion of Time Domain RMS Measure
   * 
   * @param f_t_type
   * @throws IllegalArgumentException if f_t_type.length() >= 4
   */
  public Discrim_data setF_t_type(String f_t_type) {
    if (f_t_type.length() > 4)
      throw new IllegalArgumentException(
          String.format("f_t_type.length() cannot be > 4.  f_t_type=%s", f_t_type));
    this.f_t_type = f_t_type;
    setHash(null);
    return this;
  }

  /**
   * Low frequency of amplitude measure used in MDAC processing.
   * <p>
   * Units: Hz
   * 
   * @return lfreq
   */
  public double getLfreq() {
    return lfreq;
  }

  /**
   * Low frequency of amplitude measure used in MDAC processing.
   * <p>
   * Units: Hz
   * 
   * @param lfreq
   */
  public Discrim_data setLfreq(double lfreq) {
    this.lfreq = lfreq;
    setHash(null);
    return this;
  }

  /**
   * High frequency of amplitude measure used in MDAC processing.
   * <p>
   * Units: Hz
   * 
   * @return hfreq
   */
  public double getHfreq() {
    return hfreq;
  }

  /**
   * High frequency of amplitude measure used in MDAC processing.
   * <p>
   * Units: Hz
   * 
   * @param hfreq
   */
  public Discrim_data setHfreq(double hfreq) {
    this.hfreq = hfreq;
    setHash(null);
    return this;
  }

  /**
   * Pre-event signal-to-noise ratio for a given event-station-channel-phase-frequency band.
   * <p>
   * Units: linear
   * 
   * @return snr_event
   */
  public double getSnr_event() {
    return snr_event;
  }

  /**
   * Pre-event signal-to-noise ratio for a given event-station-channel-phase-frequency band.
   * <p>
   * Units: linear
   * 
   * @param snr_event
   */
  public Discrim_data setSnr_event(double snr_event) {
    this.snr_event = snr_event;
    setHash(null);
    return this;
  }

  /**
   * Pre-phase signal-to-noise ratio for a given event-station-channel-phase-frequency band.
   * <p>
   * Units: linear
   * 
   * @return snr_phase
   */
  public double getSnr_phase() {
    return snr_phase;
  }

  /**
   * Pre-phase signal-to-noise ratio for a given event-station-channel-phase-frequency band.
   * <p>
   * Units: linear
   * 
   * @param snr_phase
   */
  public Discrim_data setSnr_phase(double snr_phase) {
    this.snr_phase = snr_phase;
    setHash(null);
    return this;
  }

  /**
   * Amplitude of the given event-station-channel-phase-frequency band for the raw data (no
   * corrections).
   * <p>
   * Units: log10(m/Hz)
   * 
   * @return ampraw
   */
  public double getAmpraw() {
    return ampraw;
  }

  /**
   * Amplitude of the given event-station-channel-phase-frequency band for the raw data (no
   * corrections).
   * <p>
   * Units: log10(m/Hz)
   * 
   * @param ampraw
   */
  public Discrim_data setAmpraw(double ampraw) {
    this.ampraw = ampraw;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty in raw average amplitude of a given event-station-phase-frequency band.
   * <p>
   * Units: log10(m/Hz)
   * 
   * @return delampraw
   */
  public double getDelampraw() {
    return delampraw;
  }

  /**
   * Uncertainty in raw average amplitude of a given event-station-phase-frequency band.
   * <p>
   * Units: log10(m/Hz)
   * 
   * @param delampraw
   */
  public Discrim_data setDelampraw(double delampraw) {
    this.delampraw = delampraw;
    setHash(null);
    return this;
  }

  /**
   * MDAC amplitude in the specified frequency band.
   * <p>
   * Units: log10(m/Hz)
   * 
   * @return ampmdac
   */
  public double getAmpmdac() {
    return ampmdac;
  }

  /**
   * MDAC amplitude in the specified frequency band.
   * <p>
   * Units: log10(m/Hz)
   * 
   * @param ampmdac
   */
  public Discrim_data setAmpmdac(double ampmdac) {
    this.ampmdac = ampmdac;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty in MDAC corrected amplitude.
   * <p>
   * Units: log10(m/Hz)
   * 
   * @return delampmdac
   */
  public double getDelampmdac() {
    return delampmdac;
  }

  /**
   * Uncertainty in MDAC corrected amplitude.
   * <p>
   * Units: log10(m/Hz)
   * 
   * @param delampmdac
   */
  public Discrim_data setDelampmdac(double delampmdac) {
    this.delampmdac = delampmdac;
    setHash(null);
    return this;
  }

  /**
   * Additional path correction (e.g. kriging) for <I>ampmdac</I>.
   * <p>
   * Units: log10(m/Hz)
   * 
   * @return amppath
   */
  public double getAmppath() {
    return amppath;
  }

  /**
   * Additional path correction (e.g. kriging) for <I>ampmdac</I>.
   * <p>
   * Units: log10(m/Hz)
   * 
   * @param amppath
   */
  public Discrim_data setAmppath(double amppath) {
    this.amppath = amppath;
    setHash(null);
    return this;
  }

  /**
   * Uncertainty in additional path (e.g. kriging) correction.
   * <p>
   * Units: log10(m/Hz)
   * 
   * @return delamppath
   */
  public double getDelamppath() {
    return delamppath;
  }

  /**
   * Uncertainty in additional path (e.g. kriging) correction.
   * <p>
   * Units: log10(m/Hz)
   * 
   * @param delamppath
   */
  public Discrim_data setDelamppath(double delamppath) {
    this.delamppath = delamppath;
    setHash(null);
    return this;
  }

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   * 
   * @return corrid
   */
  public long getCorrid() {
    return corrid;
  }

  /**
   * Correlation or post measurement processing identifier for the amplitude measurement. If no
   * correction or processing is done i.e. ('raw') measurements, the NA Value is used.
   * 
   * @param corrid
   * @throws IllegalArgumentException if corrid >= 1000000000
   */
  public Discrim_data setCorrid(long corrid) {
    if (corrid >= 1000000000L)
      throw new IllegalArgumentException("corrid=" + corrid + " but cannot be >= 1000000000");
    this.corrid = corrid;
    setHash(null);
    return this;
  }

  /**
   * Name of correction or other processing table applied to the amplitude. Examples are raw,
   * band-averaged, MDAC, phase-match filter, Coda, stack, beam, etc.
   * 
   * @return corrname
   */
  public String getCorrname() {
    return corrname;
  }

  /**
   * Name of correction or other processing table applied to the amplitude. Examples are raw,
   * band-averaged, MDAC, phase-match filter, Coda, stack, beam, etc.
   * 
   * @param corrname
   * @throws IllegalArgumentException if corrname.length() >= 32
   */
  public Discrim_data setCorrname(String corrname) {
    if (corrname.length() > 32)
      throw new IllegalArgumentException(
          String.format("corrname.length() cannot be > 32.  corrname=%s", corrname));
    this.corrname = corrname;
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
  public Discrim_data setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
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
    return "NNSA KB Custom";
  }

}
