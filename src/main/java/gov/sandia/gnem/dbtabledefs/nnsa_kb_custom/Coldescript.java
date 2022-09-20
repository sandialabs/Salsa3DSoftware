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
 * coldescript
 */
public class Coldescript extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Column name, lowercase version of that used in Oracle's data dictionary.
   */
  private String column_name;

  static final public String COLUMN_NAME_NA = null;

  /**
   * Internal storage format as given by Oracle, can be generated from Oracle data dictionary table
   * user_tab_columns via decode(data_type, 'NUMBER', 'NUMBER(' ||u.data_precision||')','FLOAT',
   * 'FLOAT(' ||u.data_precision||')','VARCHAR2', 'VARCHAR2('||u.data_length ||')',u.data_type).
   */
  private String internal_format;

  static final public String INTERNAL_FORMAT_NA = null;

  /**
   * External text format as would be used in read and write formats in programs. Of the form
   * [aife]length.precision, where a, i, f, e are ascii, integer, float, and exponential, length is
   * the total field width in characters, and precision is the number of digits to the right of the
   * decimal for numeric formats. For internal_format=date, the external format is the appropriate a
   * format, followed by a colon and then the oracle date format, as in "a19:YYYY/MM/DD HH24:MI:SS".
   * Note that e formats assume a leading sign, digit and decimal, then the number of digits right
   * of the decimal, then the e, a sign and a two-digit exponent.
   */
  private String external_format;

  static final public String EXTERNAL_FORMAT_NA = null;

  /**
   * External format width.
   * <p>
   * Units: byte
   */
  private long external_width;

  static final public long EXTERNAL_WIDTH_NA = Long.MIN_VALUE;

  /**
   * Value used for this column when not available or not applicable (if allowed).
   */
  private String na_value;

  static final public String NA_VALUE_NA = null;

  /**
   * Physical units for measured quantities
   */
  private String unit;

  static final public String UNIT_NA = "-";

  /**
   * Text version of range, deprecated.
   */
  private String range;

  static final public String RANGE_NA = "-";

  /**
   * Range type.
   */
  private String rangetype;

  static final public String RANGETYPE_NA = null;

  /**
   * Nominal minimum if range type is numeric and a universal minimum applies, is not a minimum for
   * some cases of nminop.
   */
  private double nmin;

  static final public double NMIN_NA = -999;

  /**
   * Nominal minimum operator if range type is numeric and a universal minimum applies, must be set
   * if nmin is not NA.
   */
  private String nminop;

  static final public String NMINOP_NA = "-";

  /**
   * Nominal maximum if range type is numeric and a universal maximum applies, is not a maximum for
   * some cases of nmaxop.
   */
  private double nmax;

  static final public double NMAX_NA = -999;

  /**
   * Nominal maximum operator if range type is numeric and a universal maximum applies, must be set
   * if nmax is not NA.
   */
  private String nmaxop;

  static final public String NMAXOP_NA = "-";

  /**
   * Minimum for numeric range from empirical evidence or expert opinion, normally much more
   * constrained than nmin. Violations of this limit should be very rare and normally but not
   * necessarily in error.
   */
  private double emin;

  static final public double EMIN_NA = -999;

  /**
   * Operator for minimum for numeric range from empirical evidence or expert opinion, must be set
   * if emin is not NA.
   */
  private String eminop;

  static final public String EMINOP_NA = "-";

  /**
   * Maximum for numeric range from empirical evidence or expert opinion, normally much more
   * constrained than nmax. Violations of this limit should be very rare and normally but not
   * necessarily in error.
   */
  private double emax;

  static final public double EMAX_NA = -999;

  /**
   * Operator for maximum for numeric range from empirical evidence or expert opinion, must be set
   * if emax is not NA.
   */
  private String emaxop;

  static final public String EMAXOP_NA = "-";

  /**
   * Standard regular expression used to constrain strings fields having rangetype "any string".
   * Does not apply to defined or reference rangetypes.
   */
  private String regexp;

  static final public String REGEXP_NA = "-";

  /**
   * Reference table if range type is reference set, the reference table defines the possible values
   * for the column.
   */
  private String reftab;

  static final public String REFTAB_NA = "-";

  /**
   * Name of reference column in reftab (reference table).
   */
  private String refcol;

  static final public String REFCOL_NA = "-";

  /**
   * Name of schema to which reftab (reference table) belongs.
   */
  private String refschema;

  static final public String REFSCHEMA_NA = "-";

  /**
   * Short description.
   */
  private String short_descript;

  static final public String SHORT_DESCRIPT_NA = null;

  /**
   * Long description.
   */
  private String long_descript;

  static final public String LONG_DESCRIPT_NA = null;

  /**
   * Schema name.
   */
  private String schema_name;

  static final public String SCHEMA_NAME_NA = null;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;

  /**
   * The data type that should be used to represent this value in a compiled computer language such
   * as java or c++. Typical values would be string, boolean, int, long, float or double. A value
   * such as bigdecimal(n,m) can also be used where n is the number of significant digits that
   * should be used to represent the number and m is the number of digits to the right of the
   * decimal point.
   */
  private String external_type;

  static final public String EXTERNAL_TYPE_NA = "-";


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("column_name", Columns.FieldType.STRING, "%s");
    columns.add("internal_format", Columns.FieldType.STRING, "%s");
    columns.add("external_format", Columns.FieldType.STRING, "%s");
    columns.add("external_width", Columns.FieldType.LONG, "%d");
    columns.add("na_value", Columns.FieldType.STRING, "%s");
    columns.add("unit", Columns.FieldType.STRING, "%s");
    columns.add("range", Columns.FieldType.STRING, "%s");
    columns.add("rangetype", Columns.FieldType.STRING, "%s");
    columns.add("nmin", Columns.FieldType.DOUBLE, "%19.12e");
    columns.add("nminop", Columns.FieldType.STRING, "%s");
    columns.add("nmax", Columns.FieldType.DOUBLE, "%19.12e");
    columns.add("nmaxop", Columns.FieldType.STRING, "%s");
    columns.add("emin", Columns.FieldType.DOUBLE, "%19.12e");
    columns.add("eminop", Columns.FieldType.STRING, "%s");
    columns.add("emax", Columns.FieldType.DOUBLE, "%19.12e");
    columns.add("emaxop", Columns.FieldType.STRING, "%s");
    columns.add("regexp", Columns.FieldType.STRING, "%s");
    columns.add("reftab", Columns.FieldType.STRING, "%s");
    columns.add("refcol", Columns.FieldType.STRING, "%s");
    columns.add("refschema", Columns.FieldType.STRING, "%s");
    columns.add("short_descript", Columns.FieldType.STRING, "%s");
    columns.add("long_descript", Columns.FieldType.STRING, "%s");
    columns.add("schema_name", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("external_type", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Coldescript(String column_name, String internal_format, String external_format,
      long external_width, String na_value, String unit, String range, String rangetype,
      double nmin, String nminop, double nmax, String nmaxop, double emin, String eminop,
      double emax, String emaxop, String regexp, String reftab, String refcol, String refschema,
      String short_descript, String long_descript, String schema_name, String auth,
      String external_type) {
    setValues(column_name, internal_format, external_format, external_width, na_value, unit, range,
        rangetype, nmin, nminop, nmax, nmaxop, emin, eminop, emax, emaxop, regexp, reftab, refcol,
        refschema, short_descript, long_descript, schema_name, auth, external_type);
  }

  private void setValues(String column_name, String internal_format, String external_format,
      long external_width, String na_value, String unit, String range, String rangetype,
      double nmin, String nminop, double nmax, String nmaxop, double emin, String eminop,
      double emax, String emaxop, String regexp, String reftab, String refcol, String refschema,
      String short_descript, String long_descript, String schema_name, String auth,
      String external_type) {
    this.column_name = column_name;
    this.internal_format = internal_format;
    this.external_format = external_format;
    this.external_width = external_width;
    this.na_value = na_value;
    this.unit = unit;
    this.range = range;
    this.rangetype = rangetype;
    this.nmin = nmin;
    this.nminop = nminop;
    this.nmax = nmax;
    this.nmaxop = nmaxop;
    this.emin = emin;
    this.eminop = eminop;
    this.emax = emax;
    this.emaxop = emaxop;
    this.regexp = regexp;
    this.reftab = reftab;
    this.refcol = refcol;
    this.refschema = refschema;
    this.short_descript = short_descript;
    this.long_descript = long_descript;
    this.schema_name = schema_name;
    this.auth = auth;
    this.external_type = external_type;
  }

  /**
   * Copy constructor.
   */
  public Coldescript(Coldescript other) {
    this.column_name = other.getColumn_name();
    this.internal_format = other.getInternal_format();
    this.external_format = other.getExternal_format();
    this.external_width = other.getExternal_width();
    this.na_value = other.getNa_value();
    this.unit = other.getUnit();
    this.range = other.getRange();
    this.rangetype = other.getRangetype();
    this.nmin = other.getNmin();
    this.nminop = other.getNminop();
    this.nmax = other.getNmax();
    this.nmaxop = other.getNmaxop();
    this.emin = other.getEmin();
    this.eminop = other.getEminop();
    this.emax = other.getEmax();
    this.emaxop = other.getEmaxop();
    this.regexp = other.getRegexp();
    this.reftab = other.getReftab();
    this.refcol = other.getRefcol();
    this.refschema = other.getRefschema();
    this.short_descript = other.getShort_descript();
    this.long_descript = other.getLong_descript();
    this.schema_name = other.getSchema_name();
    this.auth = other.getAuth();
    this.external_type = other.getExternal_type();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Coldescript() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(COLUMN_NAME_NA, INTERNAL_FORMAT_NA, EXTERNAL_FORMAT_NA, EXTERNAL_WIDTH_NA,
        NA_VALUE_NA, UNIT_NA, RANGE_NA, RANGETYPE_NA, NMIN_NA, NMINOP_NA, NMAX_NA, NMAXOP_NA,
        EMIN_NA, EMINOP_NA, EMAX_NA, EMAXOP_NA, REGEXP_NA, REFTAB_NA, REFCOL_NA, REFSCHEMA_NA,
        SHORT_DESCRIPT_NA, LONG_DESCRIPT_NA, SCHEMA_NAME_NA, AUTH_NA, EXTERNAL_TYPE_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "column_name":
        return column_name;
      case "internal_format":
        return internal_format;
      case "external_format":
        return external_format;
      case "na_value":
        return na_value;
      case "unit":
        return unit;
      case "range":
        return range;
      case "rangetype":
        return rangetype;
      case "nminop":
        return nminop;
      case "nmaxop":
        return nmaxop;
      case "eminop":
        return eminop;
      case "emaxop":
        return emaxop;
      case "regexp":
        return regexp;
      case "reftab":
        return reftab;
      case "refcol":
        return refcol;
      case "refschema":
        return refschema;
      case "short_descript":
        return short_descript;
      case "long_descript":
        return long_descript;
      case "schema_name":
        return schema_name;
      case "auth":
        return auth;
      case "external_type":
        return external_type;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "column_name":
        column_name = value;
        break;
      case "internal_format":
        internal_format = value;
        break;
      case "external_format":
        external_format = value;
        break;
      case "na_value":
        na_value = value;
        break;
      case "unit":
        unit = value;
        break;
      case "range":
        range = value;
        break;
      case "rangetype":
        rangetype = value;
        break;
      case "nminop":
        nminop = value;
        break;
      case "nmaxop":
        nmaxop = value;
        break;
      case "eminop":
        eminop = value;
        break;
      case "emaxop":
        emaxop = value;
        break;
      case "regexp":
        regexp = value;
        break;
      case "reftab":
        reftab = value;
        break;
      case "refcol":
        refcol = value;
        break;
      case "refschema":
        refschema = value;
        break;
      case "short_descript":
        short_descript = value;
        break;
      case "long_descript":
        long_descript = value;
        break;
      case "schema_name":
        schema_name = value;
        break;
      case "auth":
        auth = value;
        break;
      case "external_type":
        external_type = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      case "nmin":
        return nmin;
      case "nmax":
        return nmax;
      case "emin":
        return emin;
      case "emax":
        return emax;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "nmin":
        nmin = value;
        break;
      case "nmax":
        nmax = value;
        break;
      case "emin":
        emin = value;
        break;
      case "emax":
        emax = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "external_width":
        return external_width;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "external_width":
        external_width = value;
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
  public Coldescript(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Coldescript(DataInputStream input) throws IOException {
    this(readString(input), readString(input), readString(input), input.readLong(),
        readString(input), readString(input), readString(input), readString(input),
        input.readDouble(), readString(input), input.readDouble(), readString(input),
        input.readDouble(), readString(input), input.readDouble(), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Coldescript(ByteBuffer input) {
    this(readString(input), readString(input), readString(input), input.getLong(),
        readString(input), readString(input), readString(input), readString(input),
        input.getDouble(), readString(input), input.getDouble(), readString(input),
        input.getDouble(), readString(input), input.getDouble(), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input), readString(input), readString(input), readString(input),
        readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Coldescript(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Coldescript(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getLong(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7), input.getString(offset + 8), input.getDouble(offset + 9),
        input.getString(offset + 10), input.getDouble(offset + 11), input.getString(offset + 12),
        input.getDouble(offset + 13), input.getString(offset + 14), input.getDouble(offset + 15),
        input.getString(offset + 16), input.getString(offset + 17), input.getString(offset + 18),
        input.getString(offset + 19), input.getString(offset + 20), input.getString(offset + 21),
        input.getString(offset + 22), input.getString(offset + 23), input.getString(offset + 24),
        input.getString(offset + 25));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[25];
    values[0] = column_name;
    values[1] = internal_format;
    values[2] = external_format;
    values[3] = external_width;
    values[4] = na_value;
    values[5] = unit;
    values[6] = range;
    values[7] = rangetype;
    values[8] = nmin;
    values[9] = nminop;
    values[10] = nmax;
    values[11] = nmaxop;
    values[12] = emin;
    values[13] = eminop;
    values[14] = emax;
    values[15] = emaxop;
    values[16] = regexp;
    values[17] = reftab;
    values[18] = refcol;
    values[19] = refschema;
    values[20] = short_descript;
    values[21] = long_descript;
    values[22] = schema_name;
    values[23] = auth;
    values[24] = external_type;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[26];
    values[0] = column_name;
    values[1] = internal_format;
    values[2] = external_format;
    values[3] = external_width;
    values[4] = na_value;
    values[5] = unit;
    values[6] = range;
    values[7] = rangetype;
    values[8] = nmin;
    values[9] = nminop;
    values[10] = nmax;
    values[11] = nmaxop;
    values[12] = emin;
    values[13] = eminop;
    values[14] = emax;
    values[15] = emaxop;
    values[16] = regexp;
    values[17] = reftab;
    values[18] = refcol;
    values[19] = refschema;
    values[20] = short_descript;
    values[21] = long_descript;
    values[22] = schema_name;
    values[23] = auth;
    values[24] = external_type;
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
    writeString(output, column_name);
    writeString(output, internal_format);
    writeString(output, external_format);
    output.writeLong(external_width);
    writeString(output, na_value);
    writeString(output, unit);
    writeString(output, range);
    writeString(output, rangetype);
    output.writeDouble(nmin);
    writeString(output, nminop);
    output.writeDouble(nmax);
    writeString(output, nmaxop);
    output.writeDouble(emin);
    writeString(output, eminop);
    output.writeDouble(emax);
    writeString(output, emaxop);
    writeString(output, regexp);
    writeString(output, reftab);
    writeString(output, refcol);
    writeString(output, refschema);
    writeString(output, short_descript);
    writeString(output, long_descript);
    writeString(output, schema_name);
    writeString(output, auth);
    writeString(output, external_type);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, column_name);
    writeString(output, internal_format);
    writeString(output, external_format);
    output.putLong(external_width);
    writeString(output, na_value);
    writeString(output, unit);
    writeString(output, range);
    writeString(output, rangetype);
    output.putDouble(nmin);
    writeString(output, nminop);
    output.putDouble(nmax);
    writeString(output, nmaxop);
    output.putDouble(emin);
    writeString(output, eminop);
    output.putDouble(emax);
    writeString(output, emaxop);
    writeString(output, regexp);
    writeString(output, reftab);
    writeString(output, refcol);
    writeString(output, refschema);
    writeString(output, short_descript);
    writeString(output, long_descript);
    writeString(output, schema_name);
    writeString(output, auth);
    writeString(output, external_type);
  }

  /**
   * Read a Collection of Coldescript objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Coldescript objects.
   * @throws IOException
   */
  static public void readColdescripts(BufferedReader input, Collection<Coldescript> rows)
      throws IOException {
    String[] saved = Coldescript.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Coldescript
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Coldescript(new Scanner(line)));
    }
    input.close();
    Coldescript.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Coldescript objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Coldescript objects.
   * @throws IOException
   */
  static public void readColdescripts(File inputFile, Collection<Coldescript> rows)
      throws IOException {
    readColdescripts(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Coldescript objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Coldescript objects.
   * @throws IOException
   */
  static public void readColdescripts(InputStream inputStream, Collection<Coldescript> rows)
      throws IOException {
    readColdescripts(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Coldescript objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Coldescript objects
   * @throws IOException
   */
  static public Set<Coldescript> readColdescripts(BufferedReader input) throws IOException {
    Set<Coldescript> rows = new LinkedHashSet<Coldescript>();
    readColdescripts(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Coldescript objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Coldescript objects
   * @throws IOException
   */
  static public Set<Coldescript> readColdescripts(File inputFile) throws IOException {
    return readColdescripts(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Coldescript objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Coldescript objects
   * @throws IOException
   */
  static public Set<Coldescript> readColdescripts(InputStream input) throws IOException {
    return readColdescripts(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Coldescript objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param coldescripts the Coldescript objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Coldescript> coldescripts)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Coldescript coldescript : coldescripts)
      coldescript.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Coldescript objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param coldescripts the Coldescript objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Coldescript> coldescripts, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName
          + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
      for (Coldescript coldescript : coldescripts) {
        int i = 0;
        statement.setString(++i, coldescript.column_name);
        statement.setString(++i, coldescript.internal_format);
        statement.setString(++i, coldescript.external_format);
        statement.setLong(++i, coldescript.external_width);
        statement.setString(++i, coldescript.na_value);
        statement.setString(++i, coldescript.unit);
        statement.setString(++i, coldescript.range);
        statement.setString(++i, coldescript.rangetype);
        statement.setDouble(++i, coldescript.nmin);
        statement.setString(++i, coldescript.nminop);
        statement.setDouble(++i, coldescript.nmax);
        statement.setString(++i, coldescript.nmaxop);
        statement.setDouble(++i, coldescript.emin);
        statement.setString(++i, coldescript.eminop);
        statement.setDouble(++i, coldescript.emax);
        statement.setString(++i, coldescript.emaxop);
        statement.setString(++i, coldescript.regexp);
        statement.setString(++i, coldescript.reftab);
        statement.setString(++i, coldescript.refcol);
        statement.setString(++i, coldescript.refschema);
        statement.setString(++i, coldescript.short_descript);
        statement.setString(++i, coldescript.long_descript);
        statement.setString(++i, coldescript.schema_name);
        statement.setString(++i, coldescript.auth);
        statement.setString(++i, coldescript.external_type);
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
   *        Coldescript table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Coldescript> readColdescripts(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Coldescript> results = new HashSet<Coldescript>();
    readColdescripts(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Coldescript table.
   * @param coldescripts
   * @throws SQLException
   */
  static public void readColdescripts(Connection connection, String selectStatement,
      Set<Coldescript> coldescripts) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        coldescripts.add(new Coldescript(rs));
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
   * this Coldescript object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Coldescript object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append(
        "column_name, internal_format, external_format, external_width, na_value, unit, range, rangetype, nmin, nminop, nmax, nmaxop, emin, eminop, emax, emaxop, regexp, reftab, refcol, refschema, short_descript, long_descript, schema_name, auth, external_type, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(column_name).append("', ");
    sql.append("'").append(internal_format).append("', ");
    sql.append("'").append(external_format).append("', ");
    sql.append(Long.toString(external_width)).append(", ");
    sql.append("'").append(na_value).append("', ");
    sql.append("'").append(unit).append("', ");
    sql.append("'").append(range).append("', ");
    sql.append("'").append(rangetype).append("', ");
    sql.append(Double.toString(nmin)).append(", ");
    sql.append("'").append(nminop).append("', ");
    sql.append(Double.toString(nmax)).append(", ");
    sql.append("'").append(nmaxop).append("', ");
    sql.append(Double.toString(emin)).append(", ");
    sql.append("'").append(eminop).append("', ");
    sql.append(Double.toString(emax)).append(", ");
    sql.append("'").append(emaxop).append("', ");
    sql.append("'").append(regexp).append("', ");
    sql.append("'").append(reftab).append("', ");
    sql.append("'").append(refcol).append("', ");
    sql.append("'").append(refschema).append("', ");
    sql.append("'").append(short_descript).append("', ");
    sql.append("'").append(long_descript).append("', ");
    sql.append("'").append(schema_name).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("'").append(external_type).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Coldescript in the database. Primary and unique keys are set, if
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
   * Create a table of type Coldescript in the database
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
   * Generate a sql script to create a table of type Coldescript in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Coldescript in the database
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
    buf.append("column_name  varchar2(30)         NOT NULL,\n");
    buf.append("internal_format varchar2(30)         NOT NULL,\n");
    buf.append("external_format varchar2(30)         NOT NULL,\n");
    buf.append("external_width number(8)            NOT NULL,\n");
    buf.append("na_value     varchar2(80)         NOT NULL,\n");
    buf.append("unit         varchar2(80)         NOT NULL,\n");
    buf.append("range        varchar2(1024)       NOT NULL,\n");
    buf.append("rangetype    varchar2(30)         NOT NULL,\n");
    buf.append("nmin         float(53)            NOT NULL,\n");
    buf.append("nminop       varchar2(2)          NOT NULL,\n");
    buf.append("nmax         float(53)            NOT NULL,\n");
    buf.append("nmaxop       varchar2(2)          NOT NULL,\n");
    buf.append("emin         float(53)            NOT NULL,\n");
    buf.append("eminop       varchar2(2)          NOT NULL,\n");
    buf.append("emax         float(53)            NOT NULL,\n");
    buf.append("emaxop       varchar2(2)          NOT NULL,\n");
    buf.append("regexp       varchar2(80)         NOT NULL,\n");
    buf.append("reftab       varchar2(30)         NOT NULL,\n");
    buf.append("refcol       varchar2(30)         NOT NULL,\n");
    buf.append("refschema    varchar2(30)         NOT NULL,\n");
    buf.append("short_descript varchar2(80)         NOT NULL,\n");
    buf.append("long_descript varchar2(1024)       NOT NULL,\n");
    buf.append("schema_name  varchar2(30)         NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("external_type varchar2(30)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (column_name,schema_name)");
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
    return 2786;
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
    return (other instanceof Coldescript) && ((Coldescript) other).column_name.equals(column_name)
        && ((Coldescript) other).schema_name.equals(schema_name);
  }

  /**
   * Column name, lowercase version of that used in Oracle's data dictionary.
   * 
   * @return column_name
   */
  public String getColumn_name() {
    return column_name;
  }

  /**
   * Column name, lowercase version of that used in Oracle's data dictionary.
   * 
   * @param column_name
   * @throws IllegalArgumentException if column_name.length() >= 30
   */
  public Coldescript setColumn_name(String column_name) {
    if (column_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("column_name.length() cannot be > 30.  column_name=%s", column_name));
    this.column_name = column_name;
    setHash(null);
    return this;
  }

  /**
   * Internal storage format as given by Oracle, can be generated from Oracle data dictionary table
   * user_tab_columns via decode(data_type, 'NUMBER', 'NUMBER(' ||u.data_precision||')','FLOAT',
   * 'FLOAT(' ||u.data_precision||')','VARCHAR2', 'VARCHAR2('||u.data_length ||')',u.data_type).
   * 
   * @return internal_format
   */
  public String getInternal_format() {
    return internal_format;
  }

  /**
   * Internal storage format as given by Oracle, can be generated from Oracle data dictionary table
   * user_tab_columns via decode(data_type, 'NUMBER', 'NUMBER(' ||u.data_precision||')','FLOAT',
   * 'FLOAT(' ||u.data_precision||')','VARCHAR2', 'VARCHAR2('||u.data_length ||')',u.data_type).
   * 
   * @param internal_format
   * @throws IllegalArgumentException if internal_format.length() >= 30
   */
  public Coldescript setInternal_format(String internal_format) {
    if (internal_format.length() > 30)
      throw new IllegalArgumentException(String
          .format("internal_format.length() cannot be > 30.  internal_format=%s", internal_format));
    this.internal_format = internal_format;
    setHash(null);
    return this;
  }

  /**
   * External text format as would be used in read and write formats in programs. Of the form
   * [aife]length.precision, where a, i, f, e are ascii, integer, float, and exponential, length is
   * the total field width in characters, and precision is the number of digits to the right of the
   * decimal for numeric formats. For internal_format=date, the external format is the appropriate a
   * format, followed by a colon and then the oracle date format, as in "a19:YYYY/MM/DD HH24:MI:SS".
   * Note that e formats assume a leading sign, digit and decimal, then the number of digits right
   * of the decimal, then the e, a sign and a two-digit exponent.
   * 
   * @return external_format
   */
  public String getExternal_format() {
    return external_format;
  }

  /**
   * External text format as would be used in read and write formats in programs. Of the form
   * [aife]length.precision, where a, i, f, e are ascii, integer, float, and exponential, length is
   * the total field width in characters, and precision is the number of digits to the right of the
   * decimal for numeric formats. For internal_format=date, the external format is the appropriate a
   * format, followed by a colon and then the oracle date format, as in "a19:YYYY/MM/DD HH24:MI:SS".
   * Note that e formats assume a leading sign, digit and decimal, then the number of digits right
   * of the decimal, then the e, a sign and a two-digit exponent.
   * 
   * @param external_format
   * @throws IllegalArgumentException if external_format.length() >= 30
   */
  public Coldescript setExternal_format(String external_format) {
    if (external_format.length() > 30)
      throw new IllegalArgumentException(String
          .format("external_format.length() cannot be > 30.  external_format=%s", external_format));
    this.external_format = external_format;
    setHash(null);
    return this;
  }

  /**
   * External format width.
   * <p>
   * Units: byte
   * 
   * @return external_width
   */
  public long getExternal_width() {
    return external_width;
  }

  /**
   * External format width.
   * <p>
   * Units: byte
   * 
   * @param external_width
   * @throws IllegalArgumentException if external_width >= 100000000
   */
  public Coldescript setExternal_width(long external_width) {
    if (external_width >= 100000000L)
      throw new IllegalArgumentException(
          "external_width=" + external_width + " but cannot be >= 100000000");
    this.external_width = external_width;
    setHash(null);
    return this;
  }

  /**
   * Value used for this column when not available or not applicable (if allowed).
   * 
   * @return na_value
   */
  public String getNa_value() {
    return na_value;
  }

  /**
   * Value used for this column when not available or not applicable (if allowed).
   * 
   * @param na_value
   * @throws IllegalArgumentException if na_value.length() >= 80
   */
  public Coldescript setNa_value(String na_value) {
    if (na_value.length() > 80)
      throw new IllegalArgumentException(
          String.format("na_value.length() cannot be > 80.  na_value=%s", na_value));
    this.na_value = na_value;
    setHash(null);
    return this;
  }

  /**
   * Physical units for measured quantities
   * 
   * @return unit
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Physical units for measured quantities
   * 
   * @param unit
   * @throws IllegalArgumentException if unit.length() >= 80
   */
  public Coldescript setUnit(String unit) {
    if (unit.length() > 80)
      throw new IllegalArgumentException(
          String.format("unit.length() cannot be > 80.  unit=%s", unit));
    this.unit = unit;
    setHash(null);
    return this;
  }

  /**
   * Text version of range, deprecated.
   * 
   * @return range
   */
  public String getRange() {
    return range;
  }

  /**
   * Text version of range, deprecated.
   * 
   * @param range
   * @throws IllegalArgumentException if range.length() >= 1024
   */
  public Coldescript setRange(String range) {
    if (range.length() > 1024)
      throw new IllegalArgumentException(
          String.format("range.length() cannot be > 1024.  range=%s", range));
    this.range = range;
    setHash(null);
    return this;
  }

  /**
   * Range type.
   * 
   * @return rangetype
   */
  public String getRangetype() {
    return rangetype;
  }

  /**
   * Range type.
   * 
   * @param rangetype
   * @throws IllegalArgumentException if rangetype.length() >= 30
   */
  public Coldescript setRangetype(String rangetype) {
    if (rangetype.length() > 30)
      throw new IllegalArgumentException(
          String.format("rangetype.length() cannot be > 30.  rangetype=%s", rangetype));
    this.rangetype = rangetype;
    setHash(null);
    return this;
  }

  /**
   * Nominal minimum if range type is numeric and a universal minimum applies, is not a minimum for
   * some cases of nminop.
   * 
   * @return nmin
   */
  public double getNmin() {
    return nmin;
  }

  /**
   * Nominal minimum if range type is numeric and a universal minimum applies, is not a minimum for
   * some cases of nminop.
   * 
   * @param nmin
   */
  public Coldescript setNmin(double nmin) {
    this.nmin = nmin;
    setHash(null);
    return this;
  }

  /**
   * Nominal minimum operator if range type is numeric and a universal minimum applies, must be set
   * if nmin is not NA.
   * 
   * @return nminop
   */
  public String getNminop() {
    return nminop;
  }

  /**
   * Nominal minimum operator if range type is numeric and a universal minimum applies, must be set
   * if nmin is not NA.
   * 
   * @param nminop
   * @throws IllegalArgumentException if nminop.length() >= 2
   */
  public Coldescript setNminop(String nminop) {
    if (nminop.length() > 2)
      throw new IllegalArgumentException(
          String.format("nminop.length() cannot be > 2.  nminop=%s", nminop));
    this.nminop = nminop;
    setHash(null);
    return this;
  }

  /**
   * Nominal maximum if range type is numeric and a universal maximum applies, is not a maximum for
   * some cases of nmaxop.
   * 
   * @return nmax
   */
  public double getNmax() {
    return nmax;
  }

  /**
   * Nominal maximum if range type is numeric and a universal maximum applies, is not a maximum for
   * some cases of nmaxop.
   * 
   * @param nmax
   */
  public Coldescript setNmax(double nmax) {
    this.nmax = nmax;
    setHash(null);
    return this;
  }

  /**
   * Nominal maximum operator if range type is numeric and a universal maximum applies, must be set
   * if nmax is not NA.
   * 
   * @return nmaxop
   */
  public String getNmaxop() {
    return nmaxop;
  }

  /**
   * Nominal maximum operator if range type is numeric and a universal maximum applies, must be set
   * if nmax is not NA.
   * 
   * @param nmaxop
   * @throws IllegalArgumentException if nmaxop.length() >= 2
   */
  public Coldescript setNmaxop(String nmaxop) {
    if (nmaxop.length() > 2)
      throw new IllegalArgumentException(
          String.format("nmaxop.length() cannot be > 2.  nmaxop=%s", nmaxop));
    this.nmaxop = nmaxop;
    setHash(null);
    return this;
  }

  /**
   * Minimum for numeric range from empirical evidence or expert opinion, normally much more
   * constrained than nmin. Violations of this limit should be very rare and normally but not
   * necessarily in error.
   * 
   * @return emin
   */
  public double getEmin() {
    return emin;
  }

  /**
   * Minimum for numeric range from empirical evidence or expert opinion, normally much more
   * constrained than nmin. Violations of this limit should be very rare and normally but not
   * necessarily in error.
   * 
   * @param emin
   */
  public Coldescript setEmin(double emin) {
    this.emin = emin;
    setHash(null);
    return this;
  }

  /**
   * Operator for minimum for numeric range from empirical evidence or expert opinion, must be set
   * if emin is not NA.
   * 
   * @return eminop
   */
  public String getEminop() {
    return eminop;
  }

  /**
   * Operator for minimum for numeric range from empirical evidence or expert opinion, must be set
   * if emin is not NA.
   * 
   * @param eminop
   * @throws IllegalArgumentException if eminop.length() >= 2
   */
  public Coldescript setEminop(String eminop) {
    if (eminop.length() > 2)
      throw new IllegalArgumentException(
          String.format("eminop.length() cannot be > 2.  eminop=%s", eminop));
    this.eminop = eminop;
    setHash(null);
    return this;
  }

  /**
   * Maximum for numeric range from empirical evidence or expert opinion, normally much more
   * constrained than nmax. Violations of this limit should be very rare and normally but not
   * necessarily in error.
   * 
   * @return emax
   */
  public double getEmax() {
    return emax;
  }

  /**
   * Maximum for numeric range from empirical evidence or expert opinion, normally much more
   * constrained than nmax. Violations of this limit should be very rare and normally but not
   * necessarily in error.
   * 
   * @param emax
   */
  public Coldescript setEmax(double emax) {
    this.emax = emax;
    setHash(null);
    return this;
  }

  /**
   * Operator for maximum for numeric range from empirical evidence or expert opinion, must be set
   * if emax is not NA.
   * 
   * @return emaxop
   */
  public String getEmaxop() {
    return emaxop;
  }

  /**
   * Operator for maximum for numeric range from empirical evidence or expert opinion, must be set
   * if emax is not NA.
   * 
   * @param emaxop
   * @throws IllegalArgumentException if emaxop.length() >= 2
   */
  public Coldescript setEmaxop(String emaxop) {
    if (emaxop.length() > 2)
      throw new IllegalArgumentException(
          String.format("emaxop.length() cannot be > 2.  emaxop=%s", emaxop));
    this.emaxop = emaxop;
    setHash(null);
    return this;
  }

  /**
   * Standard regular expression used to constrain strings fields having rangetype "any string".
   * Does not apply to defined or reference rangetypes.
   * 
   * @return regexp
   */
  public String getRegexp() {
    return regexp;
  }

  /**
   * Standard regular expression used to constrain strings fields having rangetype "any string".
   * Does not apply to defined or reference rangetypes.
   * 
   * @param regexp
   * @throws IllegalArgumentException if regexp.length() >= 80
   */
  public Coldescript setRegexp(String regexp) {
    if (regexp.length() > 80)
      throw new IllegalArgumentException(
          String.format("regexp.length() cannot be > 80.  regexp=%s", regexp));
    this.regexp = regexp;
    setHash(null);
    return this;
  }

  /**
   * Reference table if range type is reference set, the reference table defines the possible values
   * for the column.
   * 
   * @return reftab
   */
  public String getReftab() {
    return reftab;
  }

  /**
   * Reference table if range type is reference set, the reference table defines the possible values
   * for the column.
   * 
   * @param reftab
   * @throws IllegalArgumentException if reftab.length() >= 30
   */
  public Coldescript setReftab(String reftab) {
    if (reftab.length() > 30)
      throw new IllegalArgumentException(
          String.format("reftab.length() cannot be > 30.  reftab=%s", reftab));
    this.reftab = reftab;
    setHash(null);
    return this;
  }

  /**
   * Name of reference column in reftab (reference table).
   * 
   * @return refcol
   */
  public String getRefcol() {
    return refcol;
  }

  /**
   * Name of reference column in reftab (reference table).
   * 
   * @param refcol
   * @throws IllegalArgumentException if refcol.length() >= 30
   */
  public Coldescript setRefcol(String refcol) {
    if (refcol.length() > 30)
      throw new IllegalArgumentException(
          String.format("refcol.length() cannot be > 30.  refcol=%s", refcol));
    this.refcol = refcol;
    setHash(null);
    return this;
  }

  /**
   * Name of schema to which reftab (reference table) belongs.
   * 
   * @return refschema
   */
  public String getRefschema() {
    return refschema;
  }

  /**
   * Name of schema to which reftab (reference table) belongs.
   * 
   * @param refschema
   * @throws IllegalArgumentException if refschema.length() >= 30
   */
  public Coldescript setRefschema(String refschema) {
    if (refschema.length() > 30)
      throw new IllegalArgumentException(
          String.format("refschema.length() cannot be > 30.  refschema=%s", refschema));
    this.refschema = refschema;
    setHash(null);
    return this;
  }

  /**
   * Short description.
   * 
   * @return short_descript
   */
  public String getShort_descript() {
    return short_descript;
  }

  /**
   * Short description.
   * 
   * @param short_descript
   * @throws IllegalArgumentException if short_descript.length() >= 80
   */
  public Coldescript setShort_descript(String short_descript) {
    if (short_descript.length() > 80)
      throw new IllegalArgumentException(String
          .format("short_descript.length() cannot be > 80.  short_descript=%s", short_descript));
    this.short_descript = short_descript;
    setHash(null);
    return this;
  }

  /**
   * Long description.
   * 
   * @return long_descript
   */
  public String getLong_descript() {
    return long_descript;
  }

  /**
   * Long description.
   * 
   * @param long_descript
   * @throws IllegalArgumentException if long_descript.length() >= 1024
   */
  public Coldescript setLong_descript(String long_descript) {
    if (long_descript.length() > 1024)
      throw new IllegalArgumentException(String
          .format("long_descript.length() cannot be > 1024.  long_descript=%s", long_descript));
    this.long_descript = long_descript;
    setHash(null);
    return this;
  }

  /**
   * Schema name.
   * 
   * @return schema_name
   */
  public String getSchema_name() {
    return schema_name;
  }

  /**
   * Schema name.
   * 
   * @param schema_name
   * @throws IllegalArgumentException if schema_name.length() >= 30
   */
  public Coldescript setSchema_name(String schema_name) {
    if (schema_name.length() > 30)
      throw new IllegalArgumentException(
          String.format("schema_name.length() cannot be > 30.  schema_name=%s", schema_name));
    this.schema_name = schema_name;
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
  public Coldescript setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * The data type that should be used to represent this value in a compiled computer language such
   * as java or c++. Typical values would be string, boolean, int, long, float or double. A value
   * such as bigdecimal(n,m) can also be used where n is the number of significant digits that
   * should be used to represent the number and m is the number of digits to the right of the
   * decimal point.
   * 
   * @return external_type
   */
  public String getExternal_type() {
    return external_type;
  }

  /**
   * The data type that should be used to represent this value in a compiled computer language such
   * as java or c++. Typical values would be string, boolean, int, long, float or double. A value
   * such as bigdecimal(n,m) can also be used where n is the number of significant digits that
   * should be used to represent the number and m is the number of digits to the right of the
   * decimal point.
   * 
   * @param external_type
   * @throws IllegalArgumentException if external_type.length() >= 30
   */
  public Coldescript setExternal_type(String external_type) {
    if (external_type.length() > 30)
      throw new IllegalArgumentException(
          String.format("external_type.length() cannot be > 30.  external_type=%s", external_type));
    this.external_type = external_type;
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
