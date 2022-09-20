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
package gov.sandia.gnem.dbutillib;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.rowset.serial.SerialClob;
import javax.xml.bind.JAXBException;

import gov.sandia.gnem.dbutillib.jaxb.ObjectFactory;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * This class handles the encapsulation of column metadata for a column within a table, but it does not any actual data.
 * Instead, it maintains information such as the column's NAValue, file format, data type, etc. This class also knows
 * how to cast values, parse them from a database value, or populate a database prepared statement in accordance with
 * the type of Column represented.
 * <p>
 * A Table object maintains an array of Column objects, one for each column in the corresponding table. See
 * {@link Table Table class} for more information on Tables.
 */
public class Column {
    protected final static String PRIMARY_KEY_COLUMN_TYPE = "primary key";

    /**
     * This Column's name (column_name in table definition table).
     */
    protected String name = null;

    /**
     * The SQL (or Oracle) Type for this Column. Corresponds
     */
    protected String SQLType = null;

    // /**
    // * The corresponding java sql type (as defined in java.sql.Types).
    // */
    // private int javaSQLType;

    /**
     * The java type for this Column. This is determined based on the SQLType. See {@link DBDefines#javaTypes
     * DBDefines.javaTypes} for currently defined values and for information on byte representations of java types.
     */
    protected byte javaType = DBDefines.UNKNOWN_TYPE;

    /**
     * The NA value for this Column (na_value in the table definition table). The NAValues for a Column are set in the
     * Table Definition Table. If the NA value was set to "NOT ALLOWED" in the Table Definition Table, then NAValue will
     * be null.
     */
    protected Object NAValue = null;

    /**
     * Whether or not NAValues are allowed for this Column (na_allowed in the table definition table).
     */
    private boolean NAValueAllowed = false;

    /**
     * The type of column this is (column_type in the table definition table).
     */
    protected String columnType = "";

    /**
     * The format for this Column when it is written to a file (external_format in the table definition table).
     */
    protected String externalFormat = null;

    /**
     * The field width for this Column when it is written to a file (external_width in the table definition table).
     */
    protected int externalWidth = 0;

    /**
     * Whether or not this Column is an ownedID Column (key == ownedid! in the table definition table). If ownedID is
     * true, then this Column is the id owner for its parent Table. Otherwise, ownedID is set to false.
     */
    protected boolean ownedID = false;

    /**
     * The foreignKey for this Column. If this Column is not a foreignKey, then foreignKey is set to null. If this
     * Column is a foreign key, then foreignKey will be set to the name of the ID that this Column refers to. This
     * information is obtained from the Table Definition Table. Note that ownedIDs are included in foreign keys (e.g.
     * the ORID columns is a foreign key AND an ownedID in the ORIGIN table).
     * <p>
     * For example, in the ORIGIN Table, the MSID Column has foreignKey = MAGID. This means the MSID Column of the
     * ORIGIN Table refers to the MAGID Column of some other Table.
     * <p>
     * In the WFTAG Table, the TAGID Column has foreignKey = valueOf:TAGNAME. Let TN = whatever value is in TAGNAME.
     * This means the TAGID Column of the WFTAG Table refers to the Column of another Table whose name is the evaluation
     * of valueOf:(TN), and this.foreignKey = valueOf:(TN). If TN is EVID, then valueOf:(TN) would evaluate to EVID.
     * <p>
     * In the ARRIVAL table, ARID is the ownedID, so the ARID column has foreignKey = ARID.
     */
    protected String foreignKey = null;

    /**
     * The idLink for this Column. If this Column is not an ownedID Column, and there is a Relationship in the Schema
     * where this Column is related to an ownedID Column, then the idLink field will be set to that ownedID Column's
     * name. idLink is established in the {@link Relationship#setIdLinks Relationship.setIdLinks()} method.
     * <p>
     * For example, given a Relationship: <br>
     * origin assoc where orid=#orid# <br>
     * the ORID Column in the ASSOC Table will have idLink='ORID' indicating that ORID is a foreign key in the ORIGIN
     * table that relates to the ORID column in the ASSOC table.
     * <p>
     * For the Relationship: <br>
     * event origin where orid=#prefor# <br>
     * the PREFOR Column in the EVENT table will have idLink='ORID' indicating that PREFOR is a foreign key in the EVENT
     * table that relates to the ORID column in the ORIGIN table.
     * <p>
     * Given either or both of the relationships: <br>
     * origin event where evid=#evid# <br>
     * event origin where evid=#evid# <br>
     * the EVID column in the ORIGIN table will have idLink='EVID' but the EVID column in the EVENT table will not
     * because EVID is the ownedID for the EVENT table.
     * <p>
     * To restate: Columns that are not ownedID's but which are involved in Relationships where they are related to the
     * ownedID Columns in some other table, have their idLink fields set to the name of the ownedID they are related to.
     */
    protected String idLink = null;

    /**
     * If this column is a foreign key, then it's value can be fixed. If fixedFK is true, then remap will not change the
     * value of this foreign key. Used by Merge.mergeRows() (which calls Row(sourceRow, targetTable, remap).
     */
    protected boolean fixedFK = false;

    /**
     * The precision for this Column. Precision is the number of digits to the right of the decimal point when Column is
     * of type double or float. This is extracted from fileFormat in the Column constructor. This is not used for
     * Columns that are not of type double or float.
     */
    protected int precision = Integer.MIN_VALUE;

    /**
     * The tolerance for this Column. This is how much tolerance to apply when determining if values that are associated
     * with this Column are equal to something else. This does not apply to every type of Column, but it is especially
     * useful in cases such as determining whether or not 123.456789 and 123.456788 are equal - sometimes users will
     * want those numbers to be seen as equal (set the tolerance to allow for that) and other times as not equal (no
     * tolerance). tolerance defaults to 1e - precision in the constructor.
     */
    protected double tolerance = 0.0;

    /**
     * Number formatter for this Column. Depending on the type of this Column, it could be a Decimal formatter, Date
     * formatter, etc ...
     */
    protected Format numberFormatter = null;

    /**
     * Scientific notation number formatter for this Column. If this Column's type is FLOAT and has a number of digits >
     * 24, then this formatter will be used to format the output of values associated with this column when a user calls
     * the {@link #valueToString valueToString} function with scientificNotation set to true.
     */
    private Format scientificFormatter;

    /**
     * Whether or not formatting errors have occurred while formatting values associated with this Column. Initialized
     * to false and then set to true in the {@link #valueToString valueToString()} function the first time a formatting
     * error is encountered. Error message only issued the first time error occurs.
     */
    private boolean formatError = false;

    /**
     * Default constructor - does absolutely nothing!
     */
    protected Column() {
        // This is just to ensure that there's not some compiler created default constructor
    }

    /**
     * Column constructor that takes as parameters all necessary information to create a Column.
     *
     * @param name        this Column's name (will be converted to upper case if needed)
     * @param sqlType     the SQL type/internal format of this Column; example values are NUMBER(9), VARCHAR2(25), DATE, etc
     *                    ...
     * @param foreignKey  the name of the ownedID that this Column is a foreign key to; null for fields that are not
     *                    foreign keys, 'OWNEDID!' if the Table that this Column belongs to 'owns' this Column. See {@link #getForeignKey
     *                    getForeignKey()} for more information about the foreignKey field.
     * @param columnType  this Column's type; values include: primary key, unique key, foreign key, measurement data,
     *                    descriptive data, administrative data
     * @param fileFormat  external (flat file) format specifier for this Column; example values are: a8 (alphanumeric
     *                    field of length 8), f17.5 (floating point number with 17 digits to the left of the decimal and 5 to the right),
     *                    i9 (9 digit integer), etc
     * @param fieldWidth  external width specifier for this Column for use with flat files; this specifies the width of
     *                    the column within a flat file
     * @param naValue     NAValue - value to be used instead of null; 'NOT ALLOWED' or "NA" should be specified for for
     *                    columns that are not allowed to have naValues
     * @param units       units of measure for this Column; currently not needed (null is fine) - functionality incorporating
     *                    units will be added at a later date
     * @param range       range of permissible or recommended values for this Column; currently not needed (null is fine) -
     *                    functionality incorporating range will be added at a later date
     * @param description Column description; currently not needed (null is fine) - functionality incorporating
     *                    description will be added at a later date
     * @throws FatalDBUtilLibException if an error is encountered while creating this Column
     */
    protected Column(String name, String sqlType, String foreignKey, String columnType, String fileFormat,
                     String fieldWidth, String naValue, String units, String range, String description)
            throws FatalDBUtilLibException {
        // Put parameters into the appropriate Column fields.
        // -- name --
        this.name = name;
        if (DBDefines.convertToUpperCase)
            this.name = this.name.toUpperCase();

        // -- foreignkey --
        if (foreignKey != null && !foreignKey.equals("-") && foreignKey.length() > 0) {
            this.foreignKey = foreignKey;
            if (DBDefines.convertToUpperCase)
                this.foreignKey = this.foreignKey.toUpperCase();
        }

        // If foreignKey is OWNEDID!, then point Column's foreign key at itself
        // and set its ownedID field to true.
        if (this.foreignKey != null && this.foreignKey.equals("OWNEDID!")) {
            this.foreignKey = this.name;
            this.ownedID = true;
        }

        // -- columnType --
        this.columnType = columnType;

        // -- fileFormat --
        this.externalFormat = fileFormat;

        // -- fieldWidth --
        this.externalWidth = Integer.parseInt(fieldWidth);

        // -- naValue --
        // Strip out all white spaces and _, and replace NOTALLOWED with NA. If,
        // after all that, naValue equals NA, then set NAValueAllowed to false, indicating
        // that this Column is not allowed to have an NAValue.
        // "NOTALLOWED" and "NA" are both acceptable ways to represent the
        // concept of "NAValue not allowed"
        if (naValue != null
                && naValue.trim().toUpperCase().replaceAll(" ", "").replaceAll("_", "").replaceAll("NOTALLOWED", "NA")
                .equals("NA")) {
            this.NAValueAllowed = false;
            this.NAValue = null;
        } else {
            this.NAValueAllowed = true;
            if (naValue == null || naValue.equalsIgnoreCase("null"))
                this.NAValue = null;
            else
                this.NAValue = naValue;
        }

        // -- sqlType --
        this.SQLType = sqlType;

        // Set the javaType to a valid java type as defined in DBDefines based
        // on the SQLType and set the NAValue to a formatted NAValue.
        this.javaType = DBDefines.UNKNOWN_TYPE;

        // VARCHAR2 SQLType
        if (this.SQLType.startsWith("VARCHAR2")) {
            this.javaType = DBDefines.STRING;
            // this.javaSQLType = java.sql.Types.VARCHAR;
            // this.NAValue = naValue;
        }
        // DATE SQLType
        else if (this.SQLType.equals("DATE")) {
            this.javaType = DBDefines.DATE;
            // this.javaSQLType = java.sql.Types.DATE;

            // File formats are currently of the form a17:OracleDateFormat.
            // (Doesn't necessarily have to be a17 ...) Strip fileFormat of
            // anything that precedes the oracle date formatting information
            // and then get the java format.
            String javaDateFormat = DBDefines.oracleToJavaDateFormat(this.externalFormat.substring(this.externalFormat
                    .indexOf(":") + 1));
            this.numberFormatter = new SimpleDateFormat(javaDateFormat);
        }
        // TIMESTAMP SQLType
        else if (this.SQLType.startsWith("TIMESTAMP")) {
            this.javaType = DBDefines.TIMESTAMP;
            // File formats are currently of the form a17:OracleDateFormat.
            // (Doesn't necessarily have to be a17 ...) Strip fileFormat of
            // anything that precedes the oracle date formatting information
            // and then get the java format.
            String javaDateFormat = DBDefines.oracleToJavaDateFormat(this.externalFormat.substring(this.externalFormat
                    .indexOf(":") + 1));
            this.numberFormatter = new SimpleDateFormat(javaDateFormat);
        }
        // FLOAT SQLType
        else if (this.SQLType.startsWith("FLOAT")) {
            int size = this.SQLType.indexOf('(') + 1;
            if (size > 0)
                size = Integer.parseInt(this.SQLType.substring(size, this.SQLType.indexOf(')')));

            if (size == 53 && this.externalWidth > 22)
                // if (this.externalWidth > 22)
                this.javaType = DBDefines.BIG_DECIMAL;
            else
                this.javaType = DBDefines.DOUBLE;

            // this.javaSQLType = java.sql.Types.FLOAT;

            if (this.NAValueAllowed && this.NAValue != null) {
                if (size == 53 && this.externalWidth > 22)
                    // if (this.externalWidth > 22)
                    this.NAValue = new BigDecimal(this.NAValue.toString());
                else
                    this.NAValue = Double.valueOf(this.NAValue.toString());
            }

            if (size > 24)
                this.scientificFormatter = new DecimalFormat("0.##############E0");
            else
                this.scientificFormatter = new DecimalFormat("0.#######E0");

            // Find the "dot" in the file format specifier for a float.
            int dot = this.externalFormat.indexOf(".");
            if (dot >= 0) {
                // Figure out the precision ("dots to the right of the decimal"
                // ... needed to set tolerance)
                this.precision = (Integer.valueOf(this.externalFormat.substring(dot + 1))).intValue();
                // Set tolerance to 1e-precision
                this.tolerance = (new Double("1e-" + this.precision)).doubleValue();

                // A default double formatter.
                this.numberFormatter = NumberFormat.getInstance();

                // Set up the number formatter to output 3 digits to right of
                // decimal point and to not use groupings (i.e., commas between
                // every third digit).
                ((DecimalFormat) this.numberFormatter).setMinimumFractionDigits(this.precision);
                ((DecimalFormat) this.numberFormatter).setMaximumFractionDigits(this.precision);
                ((DecimalFormat) this.numberFormatter).setGroupingUsed(false);
            }
        }
        // BLOB SQLType
        else if (this.SQLType.startsWith("BLOB")) {
            this.javaType = DBDefines.BLOB;
            // this.javaSQLType = java.sql.Types.BLOB;
            this.NAValue = null;
            // TODO: Deal with this - is null okay?
        }
        // CLOB SQLType
        else if (this.SQLType.startsWith("CLOB")) {
            this.javaType = DBDefines.CLOB;
            this.NAValue = null;
        }
        // NUMBER SQLType
        else if (this.SQLType.startsWith("NUMBER")) {
            this.javaType = DBDefines.LONG;
            // this.javaSQLType = java.sql.Types.NUMERIC;
            if (this.NAValueAllowed && naValue != null)
                this.NAValue = Long.valueOf(naValue);
        } else {
            StringBuilder msg = new StringBuilder(
                    "FATAL ERROR in Column constructor.  Unrecognized internal data type (" + this.SQLType + ")."
                            + " \nColumn name = " + name + " \nsql type = " + sqlType + " \nforeignKey = " + foreignKey
                            + " \nfile format = " + fileFormat + " \nNA value = " + naValue + " \nunits = " + units
                            + " \nrange = " + range + " \ndescription = " + description);
            DBDefines.ERROR_LOG.add(msg.toString());
            throw new FatalDBUtilLibException(msg.toString());
        }
    }

    /**
     * Set the date format for this Column. This Column's type must be Date in order for this to appropriate.
     *
     * @param dateFormat String to be used when formatting Date fields - see java.text.SimpleDateFormat API for the
     *                   types of strings to set this to
     */
    public void setDateFormat(String dateFormat) {
        if (dateFormat == null || dateFormat.length() <= 0) {
            DBDefines.ERROR_LOG.add("Column's setDateFormat received a null" + " or length 0 dateFormat parameter.");
            return;
        }
        // if (this.javaType == DBDefines.DATE)
        if (this.javaType == DBDefines.DATE || this.javaType == DBDefines.TIMESTAMP)
            this.numberFormatter = new SimpleDateFormat(dateFormat);
    }

    /**
     * Returns this Column's NAValue.
     *
     * @return this Column's NAValue
     */
    public Object getNAValue() {
        return this.NAValue;
    }

    /**
     * Return whether or not NA values are allowed.
     *
     * @return whether or not an NAValue is allowed for ths column
     */
    public boolean NAValueAllowed() {
        return this.NAValueAllowed;
    }

    public boolean getFixedFK() {
        return this.fixedFK;
    }

    /**
     * Returns this Column's external format.
     *
     * @return this Column's external format
     */
    public String getExternalFormat() {
        return this.externalFormat;
    }

    /**
     * Returns this Column's javaType. javaTypes are byte values which are defined in DBDefines. See
     * {@link DBDefines#javaTypes DBDefines.javaTypes} for currently defined values and for information on byte
     * representations of java types.
     * <p>
     * To test if someColumn is of type DOUBLE (for example), use: <br>
     * <code>if (someColumn.getJavaType() == DBDefines.DOUBLE) ... </code>
     *
     * @return javaType as a byte
     */
    public byte getJavaType() {
        return this.javaType;
    }

    /**
     * Returns the javaType of this Column, in String format. See {@link DBDefines#javaTypes DBDefines.javaTypes} for
     * currently defined values and for information on byte representations of java types.
     *
     * @return javaType as a String
     */
    public String getJavaTypeString() {
        return DBDefines.javaTypes[this.javaType];
    }

    /**
     * Return this column's type (column_type in the table definition table).
     *
     * @return this column's type
     */
    public String getType() {
        return this.columnType;
    }

    /**
     * Returns this Column's name.
     *
     * @return this Column's name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns this Column's SQL type.
     *
     * @return this Column's SQL type
     */
    public String getSQLType() {
        return this.SQLType;
    }

    /**
     * Returns the foreignKey for this Column. If this Column is not a foreignKey, then foreignKey is set to null. If
     * this Column is a foreign key, then foreignKey will be set to the name of the ID that this Column refers to. This
     * information is obtained from the Table Definition Table. Note that ownedIDs are included in foreign keys (e.g.
     * the ORID columns is a foreign key AND an ownedID in the ORIGIN table).
     * <p>
     * For example, in the ORIGIN Table, the MSID Column has foreignKey = MAGID. This means the MSID Column of the
     * ORIGIN Table refers to the MAGID Column of some other Table.
     * <p>
     * In the WFTAG Table, the TAGID Column has foreignKey = valueOf:TAGNAME. Let TN = whatever value is in TAGNAME.
     * This means the TAGID Column of the WFTAG Table refers to the Column of another Table whose name is the evaluation
     * of valueOf:(TN), and this.foreignKey = valueOf:(TN). If TN is EVID, then valueOf:(TN) would evaluate to EVID.
     * <p>
     * In the ARRIVAL table, ARID is the ownedID, so the ARID column has foreignKey = ARID.
     *
     * @return this Column's foreignKey if there is one, null otherwise
     */
    public String getForeignKey() {
        return this.foreignKey;
    }

    /**
     * Returns whether this Column is an ownedID or not.
     *
     * @return whether this Column is an ownedID or not
     */
    public boolean isOwnedID() {
        return this.ownedID;
    }

    /**
     * If this column is a foreign key, then when Row are remapped, the values in this column will normally be changed
     * to new values. That behavior can be overriden by calling {@link #setFixId setFixId(true)}. If this is done, then
     * the foreign key will not be changed by remap operations. This should only be done in cases where the application
     * can ensure that the foriegn key will not be 'dangling'. Calling {@link #setFixId setFixId(true)} changes the
     * definition of this columns such that it no longer a foreign key at all. It will be treated like regular data in
     * all respects. {@link #setFixId setFixId(true)} can be called multiple times for multiple columns, but after all
     * fk's that are to be fixed have been fixed, call schema.updateForeignKeys() which will ensure that foreign key
     * information is updated in all the Table objects in the schema. schema.updateForeignKeys() may be called after
     * schema.completeSetup() and even after a RowGraph has been loaded, but it must be called before the merge
     * constructor if data is to be merged.
     *
     * @param fixId if true, then "fix" the id; if false, then "un-fix" it
     */
    public void setFixId(boolean fixId) {
        if (this.foreignKey != null)
            this.fixedFK = fixId;
    }

    /**
     * Returns a "SQL query friendly" String that can be used within a SQL query to determine if a value associated with
     * this Column is equal to another value. This sort of "equality relation" is needed to order to incorporate this
     * Column's tolerance (if any) into SQL queries involving equality.
     *
     * @return "SQL query friendly" String that can be used to determine if a value associated with this Column is equal
     * to another value of the following forms: <BR>
     * <I>Column needs to handle precision</I> <BR>
     * BETWEEN #columnName# - 1E-precision AND #columnName# + 1E-precision <BR>
     * <I>Column does not need to handle precision</I> <BR>
     * =#columnName#
     */
    public String getEqualityRelation() {
        StringBuilder r = new StringBuilder(this.name);
        // If precision is = Integer.MIN_VALUE, then this Column's precision is
        // still set to the "default" and no tolerance must be taken into account
        if (this.precision > Integer.MIN_VALUE) {
            r.append(" BETWEEN #" + this.name + "# - 1E-" + this.precision);
            r.append(" AND #" + this.name + "# + 1E-" + this.precision);
        } else
            r.append("=#" + this.name + "#");

        return r.toString();
    }

    /**
     * Returns whether or not this Column and the Column in other are equal. Two Column objects are considered equal is
     * they have the same name, javaType, fileFormat, NAValue, and foreignKey.
     *
     * @param other the other Column object to which this Column object is to be compared
     * @return true if the two Column objects have the same name, javaType, fileFormat, NAValue, and foreignKey; false
     * otherwise
     */
    public boolean equals(Column other) {
        if (other == null)
            return false;

        if (!this.name.equals(other.name))
            return false;
        if (this.javaType != other.javaType)
            return false;
        if (!this.externalFormat.equals(other.externalFormat))
            return false;

        if (this.NAValue == null && other.NAValue != null)
            return false;
        if (this.NAValue != null && other.NAValue == null)
            return false;
        if (this.NAValue != null && other.NAValue != null && !this.NAValue.equals(other.NAValue))
            return false;

        if (this.foreignKey == null && other.foreignKey != null)
            return false;
        if (this.foreignKey != null && other.foreignKey == null)
            return false;
        if (this.foreignKey != null && other.foreignKey != null && !this.foreignKey.equals(other.foreignKey))
            return false;
        return true;
    }

    /**
     * Returns whether or not the Object in value equals this Column's NAValue.
     *
     * @param value Object that represents an NAValue to compare to this Column's NAValue to see if they are equal
     * @return true if value and this Column's NAValue are both not null and are equal; false otherwise
     */
    public boolean equalsNAValue(Object value) {
        return evaluateEquality(this.NAValue, value);
    }

    /**
     * Returns whether or not value1 and value2 are equal. Equality does not necessarily mean strict equality - a level
     * of tolerance can be applied (if the precision and tolerance for this Column are set). This is so that numbers
     * such as 123.456789 and 123.456788 can be considered equal if the user so desires.
     *
     * @param value1 value associated with this Column
     * @param value2 value to compare to value1 for equality
     * @return true if value1 and value2 are equal; false otherwise
     */
    protected boolean evaluateEquality(Object value1, Object value2) {
        if (value1 == null && value2 == null)
            return true;
        else if (value1 == null || value2 == null)
            return false;

        // DOUBLE javaType, precision set
        if (this.javaType == DBDefines.DOUBLE && this.precision > Integer.MIN_VALUE)
            return Math.abs(((Double) value1).doubleValue() - ((Double) value2).doubleValue()) < this.tolerance;

        // DOUBLE javaType, precision not set
        if (this.javaType == DBDefines.DOUBLE)
            return ((Double) value1).equals(value2);

        // FLOAT javaType, precision set
        if (this.javaType == DBDefines.FLOAT && this.precision > Integer.MIN_VALUE)
            return Math.abs(((Float) value1).floatValue() - ((Float) value2).floatValue()) < this.tolerance;

        // FLOAT javaType, precision not set
        if (this.javaType == DBDefines.FLOAT)
            return ((Float) value1).equals(value2);

        // BIG_DECIMAL javaType, precision set
        if (this.javaType == DBDefines.BIG_DECIMAL && this.precision > Integer.MIN_VALUE)
            return Math.abs((((BigDecimal) value1).subtract((BigDecimal) value2)).doubleValue()) < this.tolerance;

        // BIG_DECIMAL javaType, precision not set
        if (this.javaType == DBDefines.BIG_DECIMAL)
            return ((BigDecimal) value1).compareTo((BigDecimal) value2) == 0;

        // STRING javaType
        if (this.javaType == DBDefines.STRING)
            return ((String) value1).equals(value2);

        // LONG javaType
        if (this.javaType == DBDefines.LONG)
            return ((Long) value1).equals(value2);

        // INTEGER javaType
        if (this.javaType == DBDefines.INTEGER)
            return ((Integer) value1).equals(value2);

        // DATE javaType
        if (this.javaType == DBDefines.DATE)
            return ((Date) value1).equals(value2);

        // TIMESTAMP javaType
        if (this.javaType == DBDefines.TIMESTAMP)
            return ((Date) value1).equals(value2);

        // BYTE javaType
        if (this.javaType == DBDefines.BYTE)
            return ((Byte) value1).equals(value2);

        // BOOLEAN javaType
        if (this.javaType == DBDefines.BOOLEAN)
            return ((Boolean) value1).equals(value2);

        if (this.javaType == DBDefines.BLOB || this.javaType == DBDefines.CLOB) {
            // TODO: Handle this
            System.err.println("Column.evaluateEquality(Object,Object) has not been "
                    + "implemented for the BLOB or CLOB types yet.");
            return false;
        }
        // All else
        return value1.equals(value2);
    }

    /**
     * Returns a String representation of a value associated with this Column object, formatted according to the rules
     * for this Column as defined by the file format and external width.
     *
     * @param value              value/data to be formatted into a string
     * @param widen              whether or not to pad the formatted string to make it as wide as the external width
     * @param scientificNotation if true, numbers are formatted in scientific notation with about 13 significant digits.
     * @return a String representation of value, formatted according to the rules for this Column as defined by the file
     * format and external width specifiers for this column.
     */
    public String valueToString(Object value, boolean widen, boolean scientificNotation) {
        StringBuilder sValue;
        // null value
        if (value == null)
            sValue = new StringBuilder("null");
            // DOUBLE javaType
        else if (this.javaType == DBDefines.DOUBLE) {
            if (((Double) value).isNaN())
                sValue = new StringBuilder("NaN");
            else if (((Double) value).doubleValue() == Double.NEGATIVE_INFINITY)
                sValue = new StringBuilder("-INF");
            else if (((Double) value).isInfinite())
                sValue = new StringBuilder("INF");
            else if (scientificNotation)
                sValue = new StringBuilder(this.scientificFormatter.format(value));
            else
                sValue = new StringBuilder(this.numberFormatter.format(value));
        }
        // FLOAT javaType
        else if (this.javaType == DBDefines.FLOAT) {
            if (((Float) value).isNaN())
                sValue = new StringBuilder("NaN");
            else if (((Float) value).floatValue() == Float.NEGATIVE_INFINITY)
                sValue = new StringBuilder("-INF");
            else if (((Float) value).isInfinite())
                sValue = new StringBuilder("INF");
            else if (scientificNotation)
                sValue = new StringBuilder(this.scientificFormatter.format(value));
            else
                sValue = new StringBuilder(this.numberFormatter.format(value));
        } // BIG_DECIMAL javaType
        else if (this.javaType == DBDefines.BIG_DECIMAL) {
            Float tempValue = new Float(((BigDecimal) value).floatValue());
            if (tempValue.isNaN())
                sValue = new StringBuilder("NaN");
            else if (tempValue.floatValue() == Float.NEGATIVE_INFINITY)
                sValue = new StringBuilder("-INF");
            else if (tempValue.isInfinite())
                sValue = new StringBuilder("INF");
            else if (scientificNotation)
                sValue = new StringBuilder(this.scientificFormatter.format(value));
            else
                sValue = new StringBuilder(this.numberFormatter.format(value));
        }
        // DATE javaType
        else if (this.javaType == DBDefines.DATE)
            sValue = new StringBuilder(this.numberFormatter.format(value));
            // TIMESTAMP javaType
        else if (this.javaType == DBDefines.TIMESTAMP)
            sValue = new StringBuilder(this.numberFormatter.format(value));
            // BLOB javaType
        else if (this.javaType == DBDefines.BLOB) {
            // TODO: Handle this
            System.err.println("Column.valueToString(Object) has not been implemented for BLOB yet.");
            sValue = new StringBuilder("<blob value>");
        }
        // CLOB javaType
        else if (this.javaType == DBDefines.CLOB) {
            sValue = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(((Clob) value).getCharacterStream());
                String line = reader.readLine();
                while (line != null) {
                    sValue.append(line);
                    line = reader.readLine();
                }
                reader.close();
            } catch (Exception e) {
                DBDefines.ERROR_LOG.add("Error converting " + this.name + " CLOB value to string. Error message:\n"
                        + e.getMessage());
            }
        }
        // All else
        else
            sValue = new StringBuilder(value.toString());

        // Handle whitespace padding.
        if (widen) {
            // Numerical values - pad with whitespace BEFORE value.
            if (this.javaType == DBDefines.DOUBLE || this.javaType == DBDefines.FLOAT
                    || this.javaType == DBDefines.INTEGER || this.javaType == DBDefines.LONG
                    || this.javaType == DBDefines.BIG_DECIMAL)
                while (sValue.length() < this.externalWidth)
                    sValue.insert(0, ' ');
                // Non numerical values - pad with whitespace AFTER value.
            else
                while (sValue.length() < this.externalWidth)
                    sValue.append(' ');

            if (sValue.length() > this.externalWidth) {
                // If sValue's length is greater than this.fieldWidth, generate an
                // error since some of the value will be truncated. However, do
                // not generate an error if the value being truncated is just 0.
                String removed = sValue.substring(this.externalWidth, sValue.length());
                boolean allZeros = true;
                for (int i = 0; i < removed.length(); i++)
                    if (removed.charAt(i) != '0')
                        allZeros = false;
                if (allZeros == false) {
                    sValue.setLength(this.externalWidth);
                    String msg = "ERROR in Column.valueToString().  " + "Column " + this.name + "  Unable to format \""
                            + ((value == null) ? "null" : value.toString()) + "\" using file format specifier "
                            + getExternalFormat() + ".  Returning " + sValue.toString() + "  " + sValue.toString()
                            + " is the value that will " + "be used.";

                    // Only send formatError messages to the error log the
                    // first time they happen.
                    if (this.formatError)
                        DBDefines.WARNING_LOG.add(msg);
                    else
                        DBDefines.ERROR_LOG.add(msg + ". Additional occurrences" + " will be sent to WARNING_LOG.");
                    this.formatError = true;
                } else
                    sValue = new StringBuilder(sValue.substring(0, this.externalWidth));
            }
        }
        return sValue.toString();
    }

    /**
     * Returns a value associated with this Column object, formatted according to rules appropriate for this Column as
     * defined by the file format and external width.
     *
     * @param value value/data to be formatted into a string
     * @param widen whether or not to pad the formatted string to make it as wide as the external width
     * @return a String representation of the value, formatted according to the file format and external width
     * specifiers for this column.
     */
    public String valueToString(Object value, boolean widen) {
        return valueToString(value, widen, false);
    }

    /**
     * Cast a String into the type defined by this Column's javaType.
     *
     * @param value String value to cast
     * @return an Object that is the javaType casting of value
     * @throws FatalDBUtilLibException if a casting error occurs
     */
    public Object cast(String value) throws FatalDBUtilLibException {
        if (value == null || value.compareToIgnoreCase("null") == 0)
            return null;

        // STRING javaType
        if (this.javaType == DBDefines.STRING)
            return value;
        try {
            // LONG javaType
            if (this.javaType == DBDefines.LONG)
                return new Long(value);

            // INTEGER javaType
            if (this.javaType == DBDefines.INTEGER)
                return new Integer(value);

            // DOUBLE javaType
            if (this.javaType == DBDefines.DOUBLE) {
                String upperCaseValue = value.toUpperCase();
                if (upperCaseValue.equals("NAN"))
                    return new Double(Double.NaN);
                if (upperCaseValue.equals("-INF"))
                    return new Double(Double.NEGATIVE_INFINITY);
                if (upperCaseValue.equals("INF"))
                    return new Double(Double.POSITIVE_INFINITY);
                double x = Double.valueOf(value).doubleValue();
                if (Math.abs(x) < 1e-100)
                    x = 0.;
                return new Double(x);
            }

            // BIG_DECIMAL javaType
            if (this.javaType == DBDefines.BIG_DECIMAL)
                return new BigDecimal(value);

            // FLOAT javaType
            if (this.javaType == DBDefines.FLOAT) {
                String upperCaseValue = value.toUpperCase();
                if (upperCaseValue.equals("NAN"))
                    return new Float(Float.NaN);
                if (upperCaseValue.equals("-INF"))
                    return new Float(Float.NEGATIVE_INFINITY);
                if (upperCaseValue.equals("INF"))
                    return new Float(Float.POSITIVE_INFINITY);
                return new Float(value);
            }

            // BYTE javaType
            if (this.javaType == DBDefines.BYTE)
                return new Byte(value);
        } catch (java.lang.NumberFormatException nex) {
            throw new FatalDBUtilLibException("FATAL ERROR in Column.cast(). Cannot cast value '" + value
                    + "' into type " + DBDefines.javaTypes[this.javaType] + " for column " + this.name);
        }
        // BOOLEAN javaType
        if (this.javaType == DBDefines.BOOLEAN)
            return new Boolean(value);

        // DATE java type - requires special formatting
        if (this.javaType == DBDefines.DATE || this.javaType == DBDefines.TIMESTAMP) {
            try {
                return this.numberFormatter.parseObject(value);
            } catch (Exception ex) {
                throw new FatalDBUtilLibException("FATAL ERROR in Column.cast()." + "  Cannot cast " + value
                        + " to type java.util.Date" + " using format "
                        + ((SimpleDateFormat) this.numberFormatter).toPattern());
            }
        }
        // CLOB javaType
        if (this.javaType == DBDefines.CLOB) {
            try {
                return new SerialClob(value.toCharArray());
            } catch (Exception e) {
                throw new FatalDBUtilLibException("Error in Column.cast() when casting " + value
                        + " into a a CLOB for the " + this.name + " column. Error message: " + e.getMessage());
            }
        }

        // BLOB javaType
        if (this.javaType == DBDefines.BLOB) {
            // TODO: Handle this
            System.err.println("Column.cast(String) has not been implemented for the BLOB type yet.");
            return null;
        }
        return null;
    }

    /**
     * Given a SQL PreparedStatement object, an index in the preparedStatement that refers to the position (starting at
     * position 1) of a ? in the statement, and a value, insert the value into the preparedStatement at the specified
     * index. See the java.sql.PreparedStatement for more information about PreparedStatements and their indexes.
     *
     * @param stmt  PreparedStatement that needs to have a ? in it replaced with a value
     * @param index index into stmt to a ? that needs to be replaced with a value
     * @param val   value to replace the ? at index in stmt with
     * @throws java.sql.SQLException or IOException if an error occurs
     */
    protected void insertValue(PreparedStatement stmt, int index, Object val) throws FatalDBUtilLibException {
        try {
            if (val == null)
                // stmt.setNull(index, this.javaSQLType);
                stmt.setNull(index, java.sql.Types.NULL);
            else if (this.javaType == DBDefines.UNKNOWN_TYPE)
                stmt.setObject(index, val);
            else if (this.javaType == DBDefines.STRING)
                stmt.setString(index, (String) val);
            else if (this.javaType == DBDefines.DATE || this.javaType == DBDefines.TIMESTAMP) {
                java.util.Date date = (java.util.Date) val;
                java.sql.Timestamp tstamp = new java.sql.Timestamp(date.getTime());
                stmt.setTimestamp(index, tstamp);
            } else if (this.javaType == DBDefines.DOUBLE)
                stmt.setDouble(index, ((Double) val).doubleValue());
            else if (this.javaType == DBDefines.FLOAT)
                stmt.setFloat(index, ((Float) val).floatValue());
            else if (this.javaType == DBDefines.LONG)
                stmt.setLong(index, ((Long) val).longValue());
            else if (this.javaType == DBDefines.BIG_DECIMAL)
                stmt.setBigDecimal(index, ((BigDecimal) val));
            else if (this.javaType == DBDefines.INTEGER)
                stmt.setInt(index, ((Integer) val).intValue());
            else if (this.javaType == DBDefines.BOOLEAN)
                stmt.setBoolean(index, ((Boolean) val).booleanValue());
            else if (this.javaType == DBDefines.BYTE)
                stmt.setByte(index, ((Byte) val).byteValue());
            else if (this.javaType == DBDefines.BLOB) {
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                    objectOutputStream.writeObject(val);
                    objectOutputStream.flush();

                    stmt.setObject(index, byteArrayOutputStream.toByteArray());
                    byteArrayOutputStream.close();
                    objectOutputStream.close();
                } catch (IOException e) {
                    throw new FatalDBUtilLibException("Error writing BLOB in Column.insertValue for " + this.name
                            + " column.\nError message: " + e.getMessage());
                }
            } else if (this.javaType == DBDefines.CLOB) {
                stmt.setClob(index, (Clob) val);
            } else
                stmt.setObject(index, val);
        } catch (SQLException e) {
            throw new FatalDBUtilLibException("Error populating PreparedStatement in Column.insertValue for "
                    + this.name + " column.\nError message: " + e.getMessage());
        }
    }

    /*******************************************************************************************************************
     * Format conversion functions *
     ******************************************************************************************************************/
    /**
     * Returns a String representation of this Column object.
     *
     * @return String representation of this Column object
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        // name
        s.append(this.name);

        // foreignKey
        if (this.foreignKey == null)
            s.append("\t-");
        else
            s.append("\t" + this.foreignKey);

        // idLink
        if (this.idLink != null)
            s.append("*");

        // NAValue
        if (!this.NAValueAllowed)
            s.append("\tNA");
        else
            s.append("\t" + this.NAValue);

        // javaType
        s.append("\t" + DBDefines.javaTypes[this.javaType]);

        // fileFormat
        s.append("\t" + this.externalFormat);

        return s.toString();
    }

    /**
     * Returns a Jaxb Column representation of this Column.
     *
     * @return JAXB version of this Column
     * @throws JAXBException if a JAXBException occurs
     */
    public gov.sandia.gnem.dbutillib.jaxb.Column toJaxb() throws JAXBException {
        // Make Jaxb Column object.
        gov.sandia.gnem.dbutillib.jaxb.Column jaxbColumn = new ObjectFactory().createColumn();

        // Populate jaxbColumn with information from this Column.

        // name
        if (this.name == null)
            jaxbColumn.setName("");
        else
            jaxbColumn.setName(this.name);

        // columnType
        if (this.columnType == null)
            jaxbColumn.setType("");
        else
            jaxbColumn.setType(this.columnType);

        // javaType
        jaxbColumn.setDataType(DBDefines.javaTypes[this.javaType]);

        // SQLType
        if (this.SQLType == null)
            jaxbColumn.setSqlType("");
        else
            jaxbColumn.setSqlType(this.SQLType);

        // foreignKey
        if (this.foreignKey == null)
            jaxbColumn.setForeignKey("");
        else
            jaxbColumn.setForeignKey(this.foreignKey);

        // fileFormat
        if (this.externalFormat == null)
            jaxbColumn.setFileFormat("");
        else
            jaxbColumn.setFileFormat(this.externalFormat);

        // fieldWidth
        jaxbColumn.setFieldWidth(Integer.toString(this.externalWidth));

        // NAValue
        if (!this.NAValueAllowed)
            jaxbColumn.setNaValue("NA");
        else if (this.NAValue == null)
            jaxbColumn.setNaValue("");
        else
            jaxbColumn.setNaValue(this.NAValue.toString());

        // dateFormat
        if (this.javaType == DBDefines.DATE || this.javaType == DBDefines.TIMESTAMP)
            jaxbColumn.setDateFormat(((SimpleDateFormat) this.numberFormatter).toPattern());

        return jaxbColumn;
    }

    /**
     * Creates a Column object from a Jaxb Column object.
     *
     * @param jaxbColumn Jaxb Column object containing the necessary information to create a Column object
     * @return newly created Column containing information that matches the information in jaxbColumn
     * @throws FatalDBUtilLibException if a DBUtilLib Exception occurs
     */
    public static Column fromJaxb(gov.sandia.gnem.dbutillib.jaxb.Column jaxbColumn) throws FatalDBUtilLibException {
        String name, columnType, sqlType, foreignKey, fileFormat, fieldWidth, naValue;

        // Name
        if (jaxbColumn.getName().length() == 0)
            throw new FatalDBUtilLibException("Error in Column.fromJaxb - no Column name specified.");
        name = jaxbColumn.getName();

        // ColumnType
        if (jaxbColumn.getType().length() == 0)
            throw new FatalDBUtilLibException("Error in Column.fromJaxb - no Column type specified.");
        columnType = jaxbColumn.getType();

        // SQL type
        if (jaxbColumn.getSqlType().length() == 0)
            throw new FatalDBUtilLibException("Error in Column.fromJaxb - no Column SQLType specified.");
        sqlType = jaxbColumn.getSqlType();

        // Foreign Key
        if (jaxbColumn.getForeignKey().length() == 0)
            foreignKey = null;
        else
            foreignKey = jaxbColumn.getForeignKey();

        // File Format
        if (jaxbColumn.getFileFormat().length() == 0)
            throw new FatalDBUtilLibException("Error in Column.fromJaxb - no Column file format specified.");
        fileFormat = jaxbColumn.getFileFormat();

        // External Width
        if (jaxbColumn.getFieldWidth().length() == 0)
            fieldWidth = Integer.toString(0);
        else
            fieldWidth = jaxbColumn.getFieldWidth();

        // NA Value
        if (jaxbColumn.getNaValue().length() == 0)
            naValue = null;
        else
            naValue = jaxbColumn.getNaValue(); // Create new Column
        Column column = new Column(name, sqlType, foreignKey, columnType, fileFormat, fieldWidth, naValue, null, null,
                null);

        // Set up dateFormat if necessary.
        if (column.javaType == DBDefines.DATE || column.javaType == DBDefines.TIMESTAMP)
            column.numberFormatter = new SimpleDateFormat(jaxbColumn.getDateFormat());

        // Java Type
        column.javaType = DBDefines.getJavaTypesIndex(jaxbColumn.getDataType());
        return column;
    }

    public int getExternalWidth() {
        return this.externalWidth;
    }
}
