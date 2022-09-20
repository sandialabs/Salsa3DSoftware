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
 * wfdisc
 */
public class Wfdisc extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Station code. This is the common code-name of a seismic observatory. Generally only three or
   * four characters are used.
   */
  private String sta;

  static final public String STA_NA = null;

  /**
   * Channel identifier. This is an eight-character code which, taken together with sta, jdate and
   * time, uniquely identifies the source of the seismic data, including the geographic location,
   * spatial orientation, sensor, and subsequent data processing.
   */
  private String chan;

  static final public String CHAN_NA = null;

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
   * Waveform identifier. The key field is a unique identifier for a segment of digital waveform
   * data.
   */
  private long wfid;

  static final public long WFID_NA = Long.MIN_VALUE;

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
   * Time of last datum. In wfdisc and wftape, this attribute is the time of the last sample in the
   * waveform file. Endtime is equivalent to time + (nsamp - 1)/samprate. In sensor, this is the
   * last time the data in the record are valid.
   * <p>
   * Units: s
   */
  private double endtime;

  static final public double ENDTIME_NA = +9999999999.999;

  /**
   * Number of samples. This quantity is the number of samples in a waveform segment.
   */
  private long nsamp;

  static final public long NSAMP_NA = Long.MIN_VALUE;

  /**
   * Sampling rate. This attribute is the sample rate in samples/second. In the instrument relation
   * this is specifically the nominal sample rate, not accounting for clock drift. In wfdisc, the
   * value may vary slightly from the nominal to reflect clock drift.
   * <p>
   * Units: 1/s
   */
  private double samprate;

  static final public double SAMPRATE_NA = Double.NaN;

  /**
   * Calibration factor. This is the conversion factor that maps digital data to earth displacement.
   * The factor holds true at the oscillation period specified by the attribute calper. A positive
   * value means ground motion increasing in component direction (up, north, east) is indicated by
   * increasing counts. A negative value means the opposite. Calib generally reflects the best
   * calibration information available at the time of recording, but refinement may be given in
   * sensor reflecting a subsequent recalibration of the instrument. See calratio.
   * <p>
   * Units: nm/count
   */
  private double calib;

  static final public double CALIB_NA = Double.NaN;

  /**
   * Calibration period. This gives the period for which calib, ncalib, and calratio are valid.
   * <p>
   * Units: s
   */
  private double calper;

  static final public double CALPER_NA = Double.NaN;

  /**
   * Instrument type. This character string is used to indicate the instrument type. Some example
   * are: SRO, ASRO, DWWSSN, LRSM, and S-750.
   */
  private String instype;

  static final public String INSTYPE_NA = "-";

  /**
   * Segment type. This attribute indicates if a waveform is o (original), v (virtual), s
   * (segmented) or d (duplicate)
   */
  private String segtype;

  static final public String SEGTYPE_NA = "-";

  /**
   * Numeric data storage. This attribute specifies the format of a data series in the file system.
   * Datatypes i4, f4 and s4 are typical values. Datatype i4 denotes a 4-byte integer and f4 denotes
   * a 32-bit real number in DEC/VAX format. s4 is an integer where the most significant byte is in
   * the low address position in memory (used by Motrola and Sun chipsets) and is opposite to the
   * order used on DEC and Intel chipsets. Machine dependent formats are supported for common
   * hardwares to allow data transfer in native machine binary formats. ASCII formats have also been
   * defined to retain full precision of any binary data type. ASCII may be used when exchanging
   * data between computer systems with incompatible binary types. See the "wfport" command manual
   * page for information about converting formats. Datatype can only describe single values or
   * arrays of one data type.
   */
  private String datatype;

  static final public String DATATYPE_NA = "-";

  /**
   * Clipped data flag. This is a single-character flag to indicate whether (c) or not (n) the data
   * were clipped. Typically, this flag is derived from status bits supplied with GDSN or RSTN data,
   * but could also be supplied as a result of analyst review.
   */
  private String clip;

  static final public String CLIP_NA = "-";

  /**
   * Directory. This attribute is the directory-part of a path name. Relative path names or "."
   * (dot), the notation for the current directory, may be used.
   */
  private String dir;

  static final public String DIR_NA = null;

  /**
   * Data file. In wfdisc, this is the file name of a disk-based waveform file. In instrument, this
   * points to an instrument response file. See dir.
   */
  private String dfile;

  static final public String DFILE_NA = null;

  /**
   * File offset. This is the byte offset of a waveform segment within a data file. It is used when
   * data are multiplexed. See dir and dfile.
   */
  private long foff;

  static final public long FOFF_NA = Long.MIN_VALUE;

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
    columns.add("chan", Columns.FieldType.STRING, "%s");
    columns.add("time", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("wfid", Columns.FieldType.LONG, "%d");
    columns.add("chanid", Columns.FieldType.LONG, "%d");
    columns.add("jdate", Columns.FieldType.LONG, "%d");
    columns.add("endtime", Columns.FieldType.DOUBLE, "%1.5f");
    columns.add("nsamp", Columns.FieldType.LONG, "%d");
    columns.add("samprate", Columns.FieldType.DOUBLE, "%1.7f");
    columns.add("calib", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("calper", Columns.FieldType.DOUBLE, "%1.6f");
    columns.add("instype", Columns.FieldType.STRING, "%s");
    columns.add("segtype", Columns.FieldType.STRING, "%s");
    columns.add("datatype", Columns.FieldType.STRING, "%s");
    columns.add("clip", Columns.FieldType.STRING, "%s");
    columns.add("dir", Columns.FieldType.STRING, "%s");
    columns.add("dfile", Columns.FieldType.STRING, "%s");
    columns.add("foff", Columns.FieldType.LONG, "%d");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Wfdisc(String sta, String chan, double time, long wfid, long chanid, long jdate,
      double endtime, long nsamp, double samprate, double calib, double calper, String instype,
      String segtype, String datatype, String clip, String dir, String dfile, long foff,
      long commid) {
    setValues(sta, chan, time, wfid, chanid, jdate, endtime, nsamp, samprate, calib, calper,
        instype, segtype, datatype, clip, dir, dfile, foff, commid);
  }

  private void setValues(String sta, String chan, double time, long wfid, long chanid, long jdate,
      double endtime, long nsamp, double samprate, double calib, double calper, String instype,
      String segtype, String datatype, String clip, String dir, String dfile, long foff,
      long commid) {
    this.sta = sta;
    this.chan = chan;
    this.time = time;
    this.wfid = wfid;
    this.chanid = chanid;
    this.jdate = jdate;
    this.endtime = endtime;
    this.nsamp = nsamp;
    this.samprate = samprate;
    this.calib = calib;
    this.calper = calper;
    this.instype = instype;
    this.segtype = segtype;
    this.datatype = datatype;
    this.clip = clip;
    this.dir = dir;
    this.dfile = dfile;
    this.foff = foff;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Wfdisc(Wfdisc other) {
    this.sta = other.getSta();
    this.chan = other.getChan();
    this.time = other.getTime();
    this.wfid = other.getWfid();
    this.chanid = other.getChanid();
    this.jdate = other.getJdate();
    this.endtime = other.getEndtime();
    this.nsamp = other.getNsamp();
    this.samprate = other.getSamprate();
    this.calib = other.getCalib();
    this.calper = other.getCalper();
    this.instype = other.getInstype();
    this.segtype = other.getSegtype();
    this.datatype = other.getDatatype();
    this.clip = other.getClip();
    this.dir = other.getDir();
    this.dfile = other.getDfile();
    this.foff = other.getFoff();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Wfdisc() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(STA_NA, CHAN_NA, TIME_NA, WFID_NA, CHANID_NA, JDATE_NA, ENDTIME_NA, NSAMP_NA,
        SAMPRATE_NA, CALIB_NA, CALPER_NA, INSTYPE_NA, SEGTYPE_NA, DATATYPE_NA, CLIP_NA, DIR_NA,
        DFILE_NA, FOFF_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "sta":
        return sta;
      case "chan":
        return chan;
      case "instype":
        return instype;
      case "segtype":
        return segtype;
      case "datatype":
        return datatype;
      case "clip":
        return clip;
      case "dir":
        return dir;
      case "dfile":
        return dfile;
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
      case "instype":
        instype = value;
        break;
      case "segtype":
        segtype = value;
        break;
      case "datatype":
        datatype = value;
        break;
      case "clip":
        clip = value;
        break;
      case "dir":
        dir = value;
        break;
      case "dfile":
        dfile = value;
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
      case "samprate":
        return samprate;
      case "calib":
        return calib;
      case "calper":
        return calper;
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
      case "samprate":
        samprate = value;
        break;
      case "calib":
        calib = value;
        break;
      case "calper":
        calper = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "wfid":
        return wfid;
      case "chanid":
        return chanid;
      case "jdate":
        return jdate;
      case "nsamp":
        return nsamp;
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
      case "wfid":
        wfid = value;
        break;
      case "chanid":
        chanid = value;
        break;
      case "jdate":
        jdate = value;
        break;
      case "nsamp":
        nsamp = value;
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
  public Wfdisc(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Wfdisc(DataInputStream input) throws IOException {
    this(readString(input), readString(input), input.readDouble(), input.readLong(),
        input.readLong(), input.readLong(), input.readDouble(), input.readLong(),
        input.readDouble(), input.readDouble(), input.readDouble(), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), input.readLong(), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Wfdisc(ByteBuffer input) {
    this(readString(input), readString(input), input.getDouble(), input.getLong(), input.getLong(),
        input.getLong(), input.getDouble(), input.getLong(), input.getDouble(), input.getDouble(),
        input.getDouble(), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input), input.getLong(), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Wfdisc(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Wfdisc(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getString(offset + 2), input.getDouble(offset + 3),
        input.getLong(offset + 4), input.getLong(offset + 5), input.getLong(offset + 6),
        input.getDouble(offset + 7), input.getLong(offset + 8), input.getDouble(offset + 9),
        input.getDouble(offset + 10), input.getDouble(offset + 11), input.getString(offset + 12),
        input.getString(offset + 13), input.getString(offset + 14), input.getString(offset + 15),
        input.getString(offset + 16), input.getString(offset + 17), input.getLong(offset + 18),
        input.getLong(offset + 19));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[19];
    values[0] = sta;
    values[1] = chan;
    values[2] = time;
    values[3] = wfid;
    values[4] = chanid;
    values[5] = jdate;
    values[6] = endtime;
    values[7] = nsamp;
    values[8] = samprate;
    values[9] = calib;
    values[10] = calper;
    values[11] = instype;
    values[12] = segtype;
    values[13] = datatype;
    values[14] = clip;
    values[15] = dir;
    values[16] = dfile;
    values[17] = foff;
    values[18] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[20];
    values[0] = sta;
    values[1] = chan;
    values[2] = time;
    values[3] = wfid;
    values[4] = chanid;
    values[5] = jdate;
    values[6] = endtime;
    values[7] = nsamp;
    values[8] = samprate;
    values[9] = calib;
    values[10] = calper;
    values[11] = instype;
    values[12] = segtype;
    values[13] = datatype;
    values[14] = clip;
    values[15] = dir;
    values[16] = dfile;
    values[17] = foff;
    values[18] = commid;
    values[19] = lddate;
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
    output.writeDouble(time);
    output.writeLong(wfid);
    output.writeLong(chanid);
    output.writeLong(jdate);
    output.writeDouble(endtime);
    output.writeLong(nsamp);
    output.writeDouble(samprate);
    output.writeDouble(calib);
    output.writeDouble(calper);
    writeString(output, instype);
    writeString(output, segtype);
    writeString(output, datatype);
    writeString(output, clip);
    writeString(output, dir);
    writeString(output, dfile);
    output.writeLong(foff);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, sta);
    writeString(output, chan);
    output.putDouble(time);
    output.putLong(wfid);
    output.putLong(chanid);
    output.putLong(jdate);
    output.putDouble(endtime);
    output.putLong(nsamp);
    output.putDouble(samprate);
    output.putDouble(calib);
    output.putDouble(calper);
    writeString(output, instype);
    writeString(output, segtype);
    writeString(output, datatype);
    writeString(output, clip);
    writeString(output, dir);
    writeString(output, dfile);
    output.putLong(foff);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Wfdisc objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Wfdisc objects.
   * @throws IOException
   */
  static public void readWfdiscs(BufferedReader input, Collection<Wfdisc> rows) throws IOException {
    String[] saved = Wfdisc.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Wfdisc.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Wfdisc(new Scanner(line)));
    }
    input.close();
    Wfdisc.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Wfdisc objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Wfdisc objects.
   * @throws IOException
   */
  static public void readWfdiscs(File inputFile, Collection<Wfdisc> rows) throws IOException {
    readWfdiscs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Wfdisc objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Wfdisc objects.
   * @throws IOException
   */
  static public void readWfdiscs(InputStream inputStream, Collection<Wfdisc> rows)
      throws IOException {
    readWfdiscs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Wfdisc objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Wfdisc objects
   * @throws IOException
   */
  static public Set<Wfdisc> readWfdiscs(BufferedReader input) throws IOException {
    Set<Wfdisc> rows = new LinkedHashSet<Wfdisc>();
    readWfdiscs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Wfdisc objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Wfdisc objects
   * @throws IOException
   */
  static public Set<Wfdisc> readWfdiscs(File inputFile) throws IOException {
    return readWfdiscs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Wfdisc objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Wfdisc objects
   * @throws IOException
   */
  static public Set<Wfdisc> readWfdiscs(InputStream input) throws IOException {
    return readWfdiscs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Wfdisc objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param wfdiscs the Wfdisc objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Wfdisc> wfdiscs) throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Wfdisc wfdisc : wfdiscs)
      wfdisc.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Wfdisc objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param wfdiscs the Wfdisc objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Wfdisc> wfdiscs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(
          "insert into " + tableName + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Wfdisc wfdisc : wfdiscs) {
        int i = 0;
        statement.setString(++i, wfdisc.sta);
        statement.setString(++i, wfdisc.chan);
        statement.setDouble(++i, wfdisc.time);
        statement.setLong(++i, wfdisc.wfid);
        statement.setLong(++i, wfdisc.chanid);
        statement.setLong(++i, wfdisc.jdate);
        statement.setDouble(++i, wfdisc.endtime);
        statement.setLong(++i, wfdisc.nsamp);
        statement.setDouble(++i, wfdisc.samprate);
        statement.setDouble(++i, wfdisc.calib);
        statement.setDouble(++i, wfdisc.calper);
        statement.setString(++i, wfdisc.instype);
        statement.setString(++i, wfdisc.segtype);
        statement.setString(++i, wfdisc.datatype);
        statement.setString(++i, wfdisc.clip);
        statement.setString(++i, wfdisc.dir);
        statement.setString(++i, wfdisc.dfile);
        statement.setLong(++i, wfdisc.foff);
        statement.setLong(++i, wfdisc.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Wfdisc
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Wfdisc> readWfdiscs(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Wfdisc> results = new HashSet<Wfdisc>();
    readWfdiscs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Wfdisc
   *        table.
   * @param wfdiscs
   * @throws SQLException
   */
  static public void readWfdiscs(Connection connection, String selectStatement, Set<Wfdisc> wfdiscs)
      throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        wfdiscs.add(new Wfdisc(rs));
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
   * this Wfdisc object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Wfdisc object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "sta, chan, time, wfid, chanid, jdate, endtime, nsamp, samprate, calib, calper, instype, segtype, datatype, clip, dir, dfile, foff, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(sta).append("', ");
    sql.append("'").append(chan).append("', ");
    sql.append(Double.toString(time)).append(", ");
    sql.append(Long.toString(wfid)).append(", ");
    sql.append(Long.toString(chanid)).append(", ");
    sql.append(Long.toString(jdate)).append(", ");
    sql.append(Double.toString(endtime)).append(", ");
    sql.append(Long.toString(nsamp)).append(", ");
    sql.append(Double.toString(samprate)).append(", ");
    sql.append(Double.toString(calib)).append(", ");
    sql.append(Double.toString(calper)).append(", ");
    sql.append("'").append(instype).append("', ");
    sql.append("'").append(segtype).append("', ");
    sql.append("'").append(datatype).append("', ");
    sql.append("'").append(clip).append("', ");
    sql.append("'").append(dir).append("', ");
    sql.append("'").append(dfile).append("', ");
    sql.append(Long.toString(foff)).append(", ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Wfdisc in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Wfdisc in the database
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
   * Generate a sql script to create a table of type Wfdisc in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Wfdisc in the database
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
    buf.append("time         float(53)            NOT NULL,\n");
    buf.append("wfid         number(8)            NOT NULL,\n");
    buf.append("chanid       number(8)            NOT NULL,\n");
    buf.append("jdate        number(8)            NOT NULL,\n");
    buf.append("endtime      float(53)            NOT NULL,\n");
    buf.append("nsamp        number(8)            NOT NULL,\n");
    buf.append("samprate     float(24)            NOT NULL,\n");
    buf.append("calib        float(24)            NOT NULL,\n");
    buf.append("calper       float(24)            NOT NULL,\n");
    buf.append("instype      varchar2(6)          NOT NULL,\n");
    buf.append("segtype      varchar2(1)          NOT NULL,\n");
    buf.append("datatype     varchar2(2)          NOT NULL,\n");
    buf.append("clip         varchar2(1)          NOT NULL,\n");
    buf.append("dir          varchar2(64)         NOT NULL,\n");
    buf.append("dfile        varchar2(32)         NOT NULL,\n");
    buf.append("foff         number(10)           NOT NULL,\n");
    buf.append("commid       number(8)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (wfid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (sta,chan,time)");
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
    return 240;
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
    return (other instanceof Wfdisc) && ((Wfdisc) other).wfid == wfid;
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
    return (other instanceof Wfdisc) && ((Wfdisc) other).sta.equals(sta)
        && ((Wfdisc) other).chan.equals(chan) && ((Wfdisc) other).time == time;
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
  public Wfdisc setSta(String sta) {
    if (sta.length() > 6)
      throw new IllegalArgumentException(String.format("sta.length() cannot be > 6.  sta=%s", sta));
    this.sta = sta;
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
  public Wfdisc setChan(String chan) {
    if (chan.length() > 8)
      throw new IllegalArgumentException(
          String.format("chan.length() cannot be > 8.  chan=%s", chan));
    this.chan = chan;
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
  public Wfdisc setTime(double time) {
    this.time = time;
    setHash(null);
    return this;
  }

  /**
   * Waveform identifier. The key field is a unique identifier for a segment of digital waveform
   * data.
   * 
   * @return wfid
   */
  public long getWfid() {
    return wfid;
  }

  /**
   * Waveform identifier. The key field is a unique identifier for a segment of digital waveform
   * data.
   * 
   * @param wfid
   * @throws IllegalArgumentException if wfid >= 100000000
   */
  public Wfdisc setWfid(long wfid) {
    if (wfid >= 100000000L)
      throw new IllegalArgumentException("wfid=" + wfid + " but cannot be >= 100000000");
    this.wfid = wfid;
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
  public Wfdisc setChanid(long chanid) {
    if (chanid >= 100000000L)
      throw new IllegalArgumentException("chanid=" + chanid + " but cannot be >= 100000000");
    this.chanid = chanid;
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
  public Wfdisc setJdate(long jdate) {
    if (jdate >= 100000000L)
      throw new IllegalArgumentException("jdate=" + jdate + " but cannot be >= 100000000");
    this.jdate = jdate;
    setHash(null);
    return this;
  }

  /**
   * Time of last datum. In wfdisc and wftape, this attribute is the time of the last sample in the
   * waveform file. Endtime is equivalent to time + (nsamp - 1)/samprate. In sensor, this is the
   * last time the data in the record are valid.
   * <p>
   * Units: s
   * 
   * @return endtime
   */
  public double getEndtime() {
    return endtime;
  }

  /**
   * Time of last datum. In wfdisc and wftape, this attribute is the time of the last sample in the
   * waveform file. Endtime is equivalent to time + (nsamp - 1)/samprate. In sensor, this is the
   * last time the data in the record are valid.
   * <p>
   * Units: s
   * 
   * @param endtime
   */
  public Wfdisc setEndtime(double endtime) {
    this.endtime = endtime;
    setHash(null);
    return this;
  }

  /**
   * Number of samples. This quantity is the number of samples in a waveform segment.
   * 
   * @return nsamp
   */
  public long getNsamp() {
    return nsamp;
  }

  /**
   * Number of samples. This quantity is the number of samples in a waveform segment.
   * 
   * @param nsamp
   * @throws IllegalArgumentException if nsamp >= 100000000
   */
  public Wfdisc setNsamp(long nsamp) {
    if (nsamp >= 100000000L)
      throw new IllegalArgumentException("nsamp=" + nsamp + " but cannot be >= 100000000");
    this.nsamp = nsamp;
    setHash(null);
    return this;
  }

  /**
   * Sampling rate. This attribute is the sample rate in samples/second. In the instrument relation
   * this is specifically the nominal sample rate, not accounting for clock drift. In wfdisc, the
   * value may vary slightly from the nominal to reflect clock drift.
   * <p>
   * Units: 1/s
   * 
   * @return samprate
   */
  public double getSamprate() {
    return samprate;
  }

  /**
   * Sampling rate. This attribute is the sample rate in samples/second. In the instrument relation
   * this is specifically the nominal sample rate, not accounting for clock drift. In wfdisc, the
   * value may vary slightly from the nominal to reflect clock drift.
   * <p>
   * Units: 1/s
   * 
   * @param samprate
   */
  public Wfdisc setSamprate(double samprate) {
    this.samprate = samprate;
    setHash(null);
    return this;
  }

  /**
   * Calibration factor. This is the conversion factor that maps digital data to earth displacement.
   * The factor holds true at the oscillation period specified by the attribute calper. A positive
   * value means ground motion increasing in component direction (up, north, east) is indicated by
   * increasing counts. A negative value means the opposite. Calib generally reflects the best
   * calibration information available at the time of recording, but refinement may be given in
   * sensor reflecting a subsequent recalibration of the instrument. See calratio.
   * <p>
   * Units: nm/count
   * 
   * @return calib
   */
  public double getCalib() {
    return calib;
  }

  /**
   * Calibration factor. This is the conversion factor that maps digital data to earth displacement.
   * The factor holds true at the oscillation period specified by the attribute calper. A positive
   * value means ground motion increasing in component direction (up, north, east) is indicated by
   * increasing counts. A negative value means the opposite. Calib generally reflects the best
   * calibration information available at the time of recording, but refinement may be given in
   * sensor reflecting a subsequent recalibration of the instrument. See calratio.
   * <p>
   * Units: nm/count
   * 
   * @param calib
   */
  public Wfdisc setCalib(double calib) {
    this.calib = calib;
    setHash(null);
    return this;
  }

  /**
   * Calibration period. This gives the period for which calib, ncalib, and calratio are valid.
   * <p>
   * Units: s
   * 
   * @return calper
   */
  public double getCalper() {
    return calper;
  }

  /**
   * Calibration period. This gives the period for which calib, ncalib, and calratio are valid.
   * <p>
   * Units: s
   * 
   * @param calper
   */
  public Wfdisc setCalper(double calper) {
    this.calper = calper;
    setHash(null);
    return this;
  }

  /**
   * Instrument type. This character string is used to indicate the instrument type. Some example
   * are: SRO, ASRO, DWWSSN, LRSM, and S-750.
   * 
   * @return instype
   */
  public String getInstype() {
    return instype;
  }

  /**
   * Instrument type. This character string is used to indicate the instrument type. Some example
   * are: SRO, ASRO, DWWSSN, LRSM, and S-750.
   * 
   * @param instype
   * @throws IllegalArgumentException if instype.length() >= 6
   */
  public Wfdisc setInstype(String instype) {
    if (instype.length() > 6)
      throw new IllegalArgumentException(
          String.format("instype.length() cannot be > 6.  instype=%s", instype));
    this.instype = instype;
    setHash(null);
    return this;
  }

  /**
   * Segment type. This attribute indicates if a waveform is o (original), v (virtual), s
   * (segmented) or d (duplicate)
   * 
   * @return segtype
   */
  public String getSegtype() {
    return segtype;
  }

  /**
   * Segment type. This attribute indicates if a waveform is o (original), v (virtual), s
   * (segmented) or d (duplicate)
   * 
   * @param segtype
   * @throws IllegalArgumentException if segtype.length() >= 1
   */
  public Wfdisc setSegtype(String segtype) {
    if (segtype.length() > 1)
      throw new IllegalArgumentException(
          String.format("segtype.length() cannot be > 1.  segtype=%s", segtype));
    this.segtype = segtype;
    setHash(null);
    return this;
  }

  /**
   * Numeric data storage. This attribute specifies the format of a data series in the file system.
   * Datatypes i4, f4 and s4 are typical values. Datatype i4 denotes a 4-byte integer and f4 denotes
   * a 32-bit real number in DEC/VAX format. s4 is an integer where the most significant byte is in
   * the low address position in memory (used by Motrola and Sun chipsets) and is opposite to the
   * order used on DEC and Intel chipsets. Machine dependent formats are supported for common
   * hardwares to allow data transfer in native machine binary formats. ASCII formats have also been
   * defined to retain full precision of any binary data type. ASCII may be used when exchanging
   * data between computer systems with incompatible binary types. See the "wfport" command manual
   * page for information about converting formats. Datatype can only describe single values or
   * arrays of one data type.
   * 
   * @return datatype
   */
  public String getDatatype() {
    return datatype;
  }

  /**
   * Numeric data storage. This attribute specifies the format of a data series in the file system.
   * Datatypes i4, f4 and s4 are typical values. Datatype i4 denotes a 4-byte integer and f4 denotes
   * a 32-bit real number in DEC/VAX format. s4 is an integer where the most significant byte is in
   * the low address position in memory (used by Motrola and Sun chipsets) and is opposite to the
   * order used on DEC and Intel chipsets. Machine dependent formats are supported for common
   * hardwares to allow data transfer in native machine binary formats. ASCII formats have also been
   * defined to retain full precision of any binary data type. ASCII may be used when exchanging
   * data between computer systems with incompatible binary types. See the "wfport" command manual
   * page for information about converting formats. Datatype can only describe single values or
   * arrays of one data type.
   * 
   * @param datatype
   * @throws IllegalArgumentException if datatype.length() >= 2
   */
  public Wfdisc setDatatype(String datatype) {
    if (datatype.length() > 2)
      throw new IllegalArgumentException(
          String.format("datatype.length() cannot be > 2.  datatype=%s", datatype));
    this.datatype = datatype;
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
  public Wfdisc setClip(String clip) {
    if (clip.length() > 1)
      throw new IllegalArgumentException(
          String.format("clip.length() cannot be > 1.  clip=%s", clip));
    this.clip = clip;
    setHash(null);
    return this;
  }

  /**
   * Directory. This attribute is the directory-part of a path name. Relative path names or "."
   * (dot), the notation for the current directory, may be used.
   * 
   * @return dir
   */
  public String getDir() {
    return dir;
  }

  /**
   * Directory. This attribute is the directory-part of a path name. Relative path names or "."
   * (dot), the notation for the current directory, may be used.
   * 
   * @param dir
   * @throws IllegalArgumentException if dir.length() >= 64
   */
  public Wfdisc setDir(String dir) {
    if (dir.length() > 64)
      throw new IllegalArgumentException(
          String.format("dir.length() cannot be > 64.  dir=%s", dir));
    this.dir = dir;
    setHash(null);
    return this;
  }

  /**
   * Data file. In wfdisc, this is the file name of a disk-based waveform file. In instrument, this
   * points to an instrument response file. See dir.
   * 
   * @return dfile
   */
  public String getDfile() {
    return dfile;
  }

  /**
   * Data file. In wfdisc, this is the file name of a disk-based waveform file. In instrument, this
   * points to an instrument response file. See dir.
   * 
   * @param dfile
   * @throws IllegalArgumentException if dfile.length() >= 32
   */
  public Wfdisc setDfile(String dfile) {
    if (dfile.length() > 32)
      throw new IllegalArgumentException(
          String.format("dfile.length() cannot be > 32.  dfile=%s", dfile));
    this.dfile = dfile;
    setHash(null);
    return this;
  }

  /**
   * File offset. This is the byte offset of a waveform segment within a data file. It is used when
   * data are multiplexed. See dir and dfile.
   * 
   * @return foff
   */
  public long getFoff() {
    return foff;
  }

  /**
   * File offset. This is the byte offset of a waveform segment within a data file. It is used when
   * data are multiplexed. See dir and dfile.
   * 
   * @param foff
   * @throws IllegalArgumentException if foff >= 10000000000
   */
  public Wfdisc setFoff(long foff) {
    if (foff >= 10000000000L)
      throw new IllegalArgumentException("foff=" + foff + " but cannot be >= 10000000000");
    this.foff = foff;
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
  public Wfdisc setCommid(long commid) {
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
