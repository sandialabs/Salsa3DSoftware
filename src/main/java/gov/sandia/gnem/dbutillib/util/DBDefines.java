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
package gov.sandia.gnem.dbutillib.util;

import static java.lang.Math.round;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Row;
import gov.sandia.gnem.dbutillib.Version;

/**
 * This class is intended to be a repository for commonly used static values and functions.
 */
public abstract class DBDefines {
    /**
     * Constructor that does nothing.
     */
    private DBDefines() {
        // does nothing - private constructor here just to prevent users from instantiating this class.
    }

    /**
     * Consistent way to begin a log message for this class.
     */
    private final static String log = "DBDefines";

    /**
     * Returns a String that represents the stack trace for a Throwable object.
     *
     * @param exception a Throwable object
     * @return a formatted String representation of the stack trace for that object
     */
    public static String STACK_TRACE_STRING(Throwable exception) {
        if (exception == null)
            ERROR_LOG.add(log + " STACK_TRACE_STRING exception = null");

        StackTraceElement[] ste = exception.getStackTrace();
        StringBuilder error = new StringBuilder();

        // Recreate the stack trace into the error String.
        for (int i = 0; i < ste.length; i++)
            error.append(ste[i].toString() + "\n");

        return error.toString();
    }

    /************************ LOG OBJECTS ************************/

    /**
     * Log where all status messages are sent.
     */
    public static Log STATUS_LOG = new Log("STATUS LOG");

    /**
     * Log where all error messages are sent.
     */
    public static Log ERROR_LOG = new Log("ERROR LOG");

    /**
     * Log where all warning messages are sent.
     */
    public static Log WARNING_LOG = new Log("WARNING LOG");

    /**
     * Initializes the status, warning, and error error Logs in DBDefines. <BR>
     * Sets up the error Log so that errors will be printed to the console in addition to the Log files. <BR>
     * If any of the file names contain the string #timestamp#, that string will be replaced with a current date and
     * time timestamp. If any of the file names contain the string #Date#, that string will be replaced with the current
     * date.
     *
     * @param statusLog  name of the status Log file
     * @param warningLog name of the warning Log file
     * @param errorLog   name of the error Log file
     */
    public static void setupLogs(String statusLog, String warningLog, String errorLog) {
        setupLogs(statusLog, warningLog, errorLog, new Date(), true);
    }

    /**
     * Initializes the status, warning, and error Logs in DBDefines. <BR>
     * If any of the file names contain the string #timestamp#, that string will be replaced with a current date and
     * time timestamp. If any of the file names contain the string #Date#, that string will be replaced with the current
     * date.
     *
     * @param statusLog     name of the status Log file
     * @param warningLog    name of the warning Log file
     * @param errorLog      name of the error Log file
     * @param consoleOutput whether or not to send error messages to the screen in addition to the Log files
     */
    public static void setupLogs(String statusLog, String warningLog, String errorLog, boolean consoleOutput) {
        setupLogs(statusLog, warningLog, errorLog, new Date(), consoleOutput);
    }

    /**
     * Initializes the status, warning, and error Logs in DBDefines. <BR>
     * Sets up the error Log so that errors will be printed to the console in addition to the Log files. <BR>
     * If any of the file names contain the string #timestamp#, that string will be replaced with a date and time
     * timestamp generated from ldDate. If any of the file names contain the string #Date#, that string will be replaced
     * with the date represented in ldDate.
     *
     * @param statusLog  name of the status Log file
     * @param warningLog name of the warning Log file
     * @param errorLog   name of the error Log file
     * @param ldDate     date to be used for log file names
     */
    public static void setupLogs(String statusLog, String warningLog, String errorLog, Date ldDate) {
        setupLogs(statusLog, warningLog, errorLog, ldDate, true);
    }

    /**
     * Initializes the status, warning, and error logs in DBDefines with default log file names. <BR>
     * The default log file names are as follows if the application variable is not set: <BR>
     * Error Log File = Error.log <BR>
     * Warning Log File = Warning.log <BR>
     * Status Log File = Status.log <BR>
     * If the application variable is set, then the default log file names will have the application name plus an
     * underscore prepended to them. <BR>
     * Sets up the error Logs so that errors will be printed to the console in addition to the log files.
     */
    public static void setupLogs() {
        // Set up default log file names
        String statusLog = "Status.log", warningLog = "Warning.log", errorLog = "Error.log";

        // If the application is defined, and the application name to the name
        // of the log files.
        if (application.length() > 0) {
            statusLog = application + "_" + statusLog;
            warningLog = application + "_" + warningLog;
            errorLog = application + "_" + errorLog;
        }
        setupLogs(statusLog, warningLog, errorLog, new Date(), true);
    }

    /**
     * Initializes the status, warning, and error logs in DBDefines using information defined in a parameter file. <BR>
     * In order to retrieve log file names from a ParInfo object, the following parameters must be found within the
     * ParInfo object (with their values set to the desired log file name): <BR>
     * ErrorLogFile <BR>
     * WarningLogFile <BR>
     * StatusLogFile <BR>
     * (Note: If not at all of the log file names are specified in the ParInfo object, default log file names will be
     * specified for the ones that are not present.) <BR>
     * If any of the file names contain the string #timestamp#, that string will be replaced with a date and time
     * timestamp generated from the current date. If any of the file names contain the string #Date#, that string will
     * be replaced with the current date.
     *
     * @param configInfo ParInfo object that contains information to create the Log files
     */
    public static void setupLogs(ParInfo configInfo) {
        setupLogs(configInfo, new Date());

    }

    /**
     * Initializes the status, warning, and error logs in DBDefines using information defined in a parameter file. <BR>
     * In order to retrieve log file names from a ParInfo object, the following parameters must be found within the
     * ParInfo object (with their values set to the desired log file name): <BR>
     * ErrorLogFile <BR>
     * WarningLogFile <BR>
     * StatusLogFile <BR>
     * (Note: If not at all of the log file names are specified in the ParInfo object, default log file names will be
     * specified for the ones that are not present.) <BR>
     * If any of the file names contain the string #timestamp#, that string will be replaced with a date and time
     * timestamp generated from ldDate. If any of the file names contain the string #Date#, that string will be replaced
     * with the date represented by ldDate.
     *
     * @param configInfo ParInfo object that contains information to create the Log files
     * @param ldDate     the Date to use in log file names
     */
    public static void setupLogs(ParInfo configInfo, Date ldDate) {
        String errorLog;
        String warningLog;
        String statusLog;
        if (application.length() > 0) {
            errorLog = configInfo.getItem("ErrorLogFile", application + "_Error.log");
            warningLog = configInfo.getItem("WarningLogFile", application + "_Warning.log");
            statusLog = configInfo.getItem("StatusLogFile", application + "_Status.log");
        } else {
            errorLog = configInfo.getItem("ErrorLogFile", "Error.log");
            warningLog = configInfo.getItem("WarningLogFile", "Warning.log");
            statusLog = configInfo.getItem("StatusLogFile", "Status.log");
        }
        boolean consoleOutput = configInfo.getItem("ConsoleOutput", "true").equalsIgnoreCase("true");
        setupLogs(statusLog, warningLog, errorLog, ldDate, consoleOutput);
    }

    /**
     * Initializes the status, warning, and error Logs in DBDefines. <BR>
     * If any of the file names contain the string #timestamp#, that string will be replaced with a date and time
     * timestamp generated from ldDate. If any of the file names contain the string #Date#, that string will be replaced
     * with the date represented in ldDate.
     *
     * @param statusLog     name of the status Log file
     * @param warningLog    name of the warning Log file
     * @param errorLog      name of the error Log file
     * @param ldDate        date to be used for log file names
     * @param consoleOutput whether or not to send error messages to the screen in addition to the Log files
     */
    public static void setupLogs(String statusLog, String warningLog, String errorLog, Date ldDate,
                                 boolean consoleOutput) {
        // Generate a String that represents a date/time timestamp.
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timeString = timeFormat.format(ldDate);

        // Generate a String that represents a date.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(ldDate);

        // Replace the string #Date# in the file names with dateString.
        errorLog = errorLog.replaceAll("#Date#", dateString);
        warningLog = warningLog.replaceAll("#Date#", dateString);
        statusLog = statusLog.replaceAll("#Date#", dateString);

        // Replace the string #timestamp# in the file names with timeString.
        errorLog = errorLog.replaceAll("#timestamp#", timeString);
        warningLog = warningLog.replaceAll("#timestamp#", timeString);
        statusLog = statusLog.replaceAll("#timestamp#", timeString);

        // "Start up" the logs - tell them where to write out their messages.
        STATUS_LOG.filePrintOn(statusLog);
        WARNING_LOG.filePrintOn(warningLog);
        ERROR_LOG.filePrintOn(errorLog);

        // If consoleOutput is true, also tell the error log to write messages to the screen.
        if (consoleOutput)
            DBDefines.ERROR_LOG.consolePrintOn();
    }

    /**
     * Returns the combined size of all the Log file objects.
     *
     * @return combined size of all the Log file objects
     */
    public static int logsSize() {
        int size = DBDefines.STATUS_LOG.size() + DBDefines.WARNING_LOG.size() + DBDefines.ERROR_LOG.size();
        return size;
    }

    /**
     * Flush all of the Log file objects to their appropriate files and/or the screen.
     */
    public static void outputLogs() {
        DBDefines.STATUS_LOG.flush();
        DBDefines.WARNING_LOG.flush();
        DBDefines.ERROR_LOG.flush();
    }

    /*********************** DATE CONVERSION FUNCTIONS ***********************/

    /**
     * Need this class only because GregorianCalendar.computeTime() is protected.
     */
    @SuppressWarnings("serial")
    public static class CustomCalendar extends GregorianCalendar {
        public CustomCalendar() {
            super();
        }

        public CustomCalendar(SimpleTimeZone tz) {
            super(tz);
        }

        @Override
        public void computeTime() {
            super.computeTime();
        }
    }

    private final static String dateFormatString = "yyyy/MM/dd HH:mm:ss";
    private final static SimpleDateFormat df = new SimpleDateFormat(dateFormatString);
    private final static CustomCalendar localCalendar = new CustomCalendar();
    private final static CustomCalendar gmtCalendar = new CustomCalendar(new SimpleTimeZone(0, "gmt"));
    private static int[] calendarFields = new int[]{Calendar.YEAR, Calendar.DAY_OF_YEAR, Calendar.HOUR_OF_DAY,
            Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND};

    /**
     * Convert a Date to epoch time (number of seconds since 1970/01/01 00:00:00.000)
     *
     * @param date Date the Date object to be converted
     * @return double epoch time (number of seconds since 1970/01/01 00:00:00.000)
     */
    public static double dateToEpochTime(Date date) {
        localCalendar.setTime(date);
        for (int i : calendarFields)
            gmtCalendar.set(i, localCalendar.get(i));
        gmtCalendar.computeTime();
        return gmtCalendar.getTimeInMillis() * 1e-3;
    }

    /**
     * Convert an epoch time (number of seconds since 1970/01/01 00:00:00.000) to a Date object
     *
     * @param epochTime double the epoch time to be converted
     * @return Date Date version of the epoch time.
     */
    public static Date epochTimeToDate(double epochTime) {
        gmtCalendar.setTime(new Date(round(epochTime * 1000.0)));
        for (int i : calendarFields)
            localCalendar.set(i, gmtCalendar.get(i));
        localCalendar.computeTime();
        return new Date(localCalendar.getTimeInMillis());
    }

    /**
     * Convert a string representation of a Date (format = yyyy/MM/dd HH:mm:ss) to an epoch time.
     *
     * @param date String string representation of a Date.
     * @return double epoch time (number of seconds since 1970/01/01 00:00:00.000)
     * @throws FatalDBUtilLibException
     */
    public static double stringDateToEpochTime(String date) throws FatalDBUtilLibException {
        try {
            return dateToEpochTime(df.parse(date));
        } catch (ParseException ex) {
            throw new FatalDBUtilLibException("ERROR in DBDefines.stringDateToEpochTime().  Cannot parse " + date
                    + " with format " + dateFormatString);
        }
    }

    /**
     * Convert an epoch time (number of seconds since 1970/01/01 00:00:00.000) into a String representation of a Date.
     * Format = yyyy/MM/dd HH:mm:ss
     *
     * @param epochTime double epoch time to be converted
     * @return String string representation of a Date. Format = yyyy/MM/dd HH:mm:ss
     */
    public static String epochTimeToStringDate(double epochTime) {
        return df.format(epochTimeToDate(epochTime));
    }

    /**
     * Convert a Date object to a Julian date (yyyyddd). Note that days are truncated to compute jdate.
     *
     * @param date Date The Date object to be converted.
     * @return int the jdate representation of the date.
     */
    public static int dateToJulianDate(Date date) {
        localCalendar.setTime(date);
        return localCalendar.get(Calendar.YEAR) * 1000 + localCalendar.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Convert a julian date (yyyyddd) into a Date object.
     *
     * @param jdate int the julian date to be converted.
     * @return Date the Date version of the jdate. hours, minutes, seconds and milliseconds will all be zero.
     */
    public static Date julianDateToDate(int jdate) {
        localCalendar.set(Calendar.YEAR, jdate / 1000);
        localCalendar.set(Calendar.DAY_OF_YEAR, jdate % 1000);
        localCalendar.set(Calendar.HOUR_OF_DAY, 0);
        localCalendar.set(Calendar.MINUTE, 0);
        localCalendar.set(Calendar.SECOND, 0);
        localCalendar.set(Calendar.MILLISECOND, 0);
        return new Date(localCalendar.getTimeInMillis());
    }

    /**
     * Convert a julian date to epoch time. Epoch time is seconds since 1970-01-01 00:00:00.000. A julian date is the
     * date in format YYYYDDD where YYYY is the year and DDD is the day in the year (001 - 365).
     *
     * @param jdate julian date
     * @return epoch time version of julian date jdate
     */
    public static double julianDateToEpochTime(int jdate) {
        return dateToEpochTime(julianDateToDate(jdate));
    }

    /**
     * Convert an epoch time to a julian date. Epoch time is seconds since 1970-01-01 00:00:00.000. A julian date is the
     * date in format YYYYDDD where YYYY is the year and DDD is the day in the year (001 - 365).
     *
     * @param time epoch time
     * @return julian date version of epoch time t
     */
    public static int epochTimeToJulianDate(double time) {
        return dateToJulianDate(epochTimeToDate(time));
    }

    /**
     * Convert an epoch time to a julian date. Epoch time is seconds since 1970-01-01 00:00:00.000. A julian date is the
     * date in format YYYYDDD where YYYY is the year and DDD is the day in the year (001 - 365).
     *
     * @param time epoch time
     * @return julian date version of epoch time
     */
    public static String epochTimeToJulianDate(String time) {
        return String.valueOf(epochTimeToJulianDate(Double.parseDouble(time)));
    }

    /**
     * Takes an Oracle date format string and returns an equivalent Java format String. This function does not cover all
     * possible Oracle date formats - just the most common ones.
     *
     * @param oracleFormat Oracle date format
     * @return java date format string that is equivalent to oracleFormat; this will be a string that is compatible with
     * Java's SimpleDateFormat class
     */
    public static String oracleToJavaDateFormat(String oracleFormat) {
        String javaFormat = oracleFormat.trim();

        // Oracle Years
        // Oracle Java
        // YY yy
        // yy yy
        // YYYY yyyy
        // yyyy yyyy
        javaFormat = javaFormat.replaceAll("YY", "yy");
        javaFormat = javaFormat.replaceAll("YYYY", "yyyy");

        // Oracle Months
        // Oracle Java
        // MM MM
        // mm MM
        // MONTH MMMMM
        // month MMMMM
        // Month MMMMM (not sure how to do mixed Month case in Java ...
        // lower case m in Java is minutes)
        javaFormat = javaFormat.replaceAll("mm", "MM");
        javaFormat = javaFormat.replaceAll("MONTH", "MMMMM");
        javaFormat = javaFormat.replaceAll("month", "MMMMM");
        javaFormat = javaFormat.replaceAll("Month", "MMMMM");

        // Oracle Days
        // Oracle Java
        // DD dd
        // dd dd
        javaFormat = javaFormat.replaceAll("DD", "dd");

        // Oracle hours
        // Oracle Java
        // HH hh
        // hh hh
        // HH12 hh
        // hh12 hh
        // HH24 HH
        // hh24 HH
        javaFormat = javaFormat.replaceAll("HH", "hh");
        javaFormat = javaFormat.replaceAll("HH12", "hh");
        javaFormat = javaFormat.replaceAll("hh12", "hh");
        javaFormat = javaFormat.replaceAll("HH24", "HH");
        javaFormat = javaFormat.replaceAll("hh24", "HH");

        // Oracle minutes
        // Oracle Java
        // MI mm
        // mi mm
        javaFormat = javaFormat.replaceAll("mi", "mm");
        javaFormat = javaFormat.replaceAll("MI", "mm");

        // Oracle seconds
        // Oracle Java
        // SS ss
        // ss ss
        javaFormat = javaFormat.replaceAll("SS", "ss");

        // If the hour format is one that returns a 1-12 value for the hour,
        // add an AM/PM marker.
        if (javaFormat.indexOf("hh") > 0) {
            // If the format has seconds in it, put the AM/PM marker after
            // the seconds.
            if (javaFormat.indexOf("ss") > 0)
                javaFormat.replaceAll("ss", "ss a");
                // If the format has no seconds, but has minutes in it, put
                // the AM/PM marker after the minutes.
            else if (javaFormat.indexOf("mm") > 0)
                javaFormat.replaceAll("mm", "mm a");
                // If the format has no seconds and no minutes, put the AM/PM marker
                // after the hour.
            else
                javaFormat.replaceAll("hh", "hh a");
        }
        return javaFormat;
    }

    /********************** NUMBER FORMATTING FUNCTIONS **********************/

    /**
     * Returns a string version of a formatted double number. The number is formatted to the width specified in the
     * width parameter using the precision specified in the precision parameter.
     *
     * @param x         double number to be formatted
     * @param width     desired width of the double to be formatted
     * @param precision desired precision for the double to be formatted
     * @param nformat   NumberFormatter to be used; a default DecimalFormatter will be used if this is null
     * @return String version of the formatted double number in x
     */
    public static String formatNumber(double x, int width, int precision, NumberFormat nformat) {
        // Set up a default formatter.
        if (nformat == null) {
            nformat = new DecimalFormat();
            nformat.setGroupingUsed(false);
        }

        // Set up the number formatter to output 3 digits to right of
        // decimal point and to not use groupings (i.e., commas between
        // every third digit).
        nformat.setMinimumFractionDigits(precision);
        nformat.setMaximumFractionDigits(precision);

        // Build up a String version of the number
        StringBuilder s = new StringBuilder(nformat.format(x));
        while (s.length() < width)
            s.insert(0, ' ');
        return s.toString();
    }

    /**
     * Returns a string version of a formatted double number. The number is formatted to the width specified in the
     * width parameter using the precision specified in the precision parameter.
     *
     * @param x         double number to be formatted
     * @param width     desired width of the double to be formatted
     * @param precision desired precision for the double to be formatted
     * @return String version of the formatted double number in x
     */
    public static String formatNumber(double x, int width, int precision) {
        return formatNumber(x, width, precision, null);
    }

    /**
     * Returns a string version of a formatted double number. The number is formatted to the width specified in the
     * width parameter using the precision specified in the precision parameter.
     *
     * @param x         Object representing a double number to be formatted
     * @param width     desired width of the double to be formatted
     * @param precision desired precision for the double to be formatted
     * @return String version of the formatted double number in x
     */
    public static String formatNumber(Object x, int width, int precision) {
        return formatNumber(((Double) x).doubleValue(), width, precision);
    }

    /*********************** STRING AND FILE FUNCTIONS ***********************/

    /**
     * Split the given string into an array of Strings based on either DBDefines.EOLN or \n.
     *
     * @param stringToSplit String to split into an array of Strings based on either DBDefines.EOLN or \n
     * @return an array of Strings representing stringToSplit after being split into an array of Strings based on either
     * DBDefines.EOLN or \n
     */
    public static String[] splitOnNewLine(String stringToSplit) {
        String[] splitString;
        if (stringToSplit.contains(DBDefines.EOLN))
            splitString = stringToSplit.split(DBDefines.EOLN);
        else
            splitString = stringToSplit.split("\n");
        return splitString;
    }

    /**
     * Removes all the "extra" spaces and tabs in str. This method leaves only a single whitespace where tabs or more
     * than one space previously existed.
     *
     * @param str string to remove extra spaces from
     * @return str with extra spaces removed
     */
    public static String removeExtraSpaces(String str) {
        return str.replaceAll("\t", " ").replaceAll("  *", " ");
    }

    /**
     * Takes an array of strings and ensures that they are all the same length. Any elements whose length is less than
     * the length of the longest element is right padded with spaces until it is as long as the longest element.
     *
     * @param list a list of Strings.
     */
    public static void evenLength(String[] list) {
        if (list == null)
            return;

        int max = 0;
        // Find the length of the longest string.
        for (String i : list) {
            if (i.length() > max)
                max = i.length();
        }

        // Pad everything in list to make all elements of max length.
        for (int i = 0; i < list.length; i++) {
            if (list[i].length() < max) {
                StringBuilder sb = new StringBuilder(list[i]);
                while (sb.length() < max)
                    sb.append(" ");
                list[i] = sb.toString();
            }
        }
    }

    /**
     * Takes a list of rows and ensures that their history StringBuilders are all the same length. Any history whose
     * length is less than the length of the longest history is right padded with spaces until it is as long as the
     * longest history.
     *
     * @param rows a Collection of Row objects whose histories are to be evened.
     */
    public static void evenLength(Collection<Row> rows) {
        if (rows == null)
            return;
        int max = 0;

        // Find the length of the longest history.
        for (Row row : rows) {
            if (row.history != null && row.history.length() > max)
                max = row.history.length();
        }

        // Pad everything in rows to make all histories of max length.
        for (Row row : rows) {
            if (row.history != null)
                while (row.history.length() < max)
                    row.history.append(' ');
        }
    }

    /**
     * Write a Collection of Strings out to a text file.
     *
     * @param fileName the name of the file to which the Strings should be written. If the file already exists, it is
     *                 overwritten without warning.
     * @param strings  the strings to be written to the file
     * @throws IOException
     */
    public static void outputToFile(String fileName, Collection<String> strings) throws IOException {
        outputToFile(fileName, strings.toArray(new String[1]));
    }

    /**
     * Write an array of Strings out to a text file.
     *
     * @param fileName the name of the file to which the Strings should be written. If the file already exists, it is
     *                 overwritten without warning.
     * @param strings  the strings to be written to the file
     * @throws IOException
     */
    public static void outputToFile(String fileName, String[] strings) throws IOException {
        if (fileName == null || fileName.length() == 0)
            return;
        FileWriter fw = new FileWriter(fileName);
        BufferedWriter bw = new BufferedWriter(fw);
        for (String currString : strings) {
            bw.write(currString);
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }

    /**
     * Append information to the end of a text file.
     *
     * @param fileName   the name of the file to which the information should be appended
     * @param appendInfo Collection of Strings to append to the file specified in filename; each String in the
     *                   collection will be followed by a newline
     * @throws IOException
     */
    public static void appendToFile(String fileName, Collection<String> appendInfo) throws IOException {
        if (fileName == null || fileName.length() == 0)
            return;
        FileWriter fw = new FileWriter(fileName, true);
        for (String currString : appendInfo)
            fw.append(currString + EOLN);
        fw.flush();
        fw.close();
    }

    /**
     * Performs a file copy. If the file already exists, it is overwritten.
     *
     * @param inputFile  file to be copied
     * @param outputFile copy of inputFile
     * @throws java.io.IOException if an error occurs
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void reverseFile(String fileName) throws Exception {
        ReverseFileReader rfr = new ReverseFileReader(fileName);
        File sourceFile = new File(fileName);
        File reversedFile = new File("temp");

        String line = rfr.readLine();
        ArrayList<String> lines = new ArrayList<String>();
        while (line != null) {
            lines.add(line);
            if (lines.size() == 5000)
                appendToFile("temp", lines);
            line = rfr.readLine();
        }
        rfr.close();
        appendToFile("temp", lines);

        sourceFile.delete();
        reversedFile.renameTo(sourceFile);
    }

    /**
     * Extract the table name the String in select. The table name is expected to follow the text "from " (case
     * insensitive).
     *
     * @param select String containing the table name to be extracted
     * @return table name extracted from select String; null if select did not have the text "from " in it.
     */
    public static String extractTableName(String select) {
        // Find the word "from" - the table name should be after that.
        int i = select.toLowerCase().indexOf("from ");

        // No "from" = no table.
        if (i < 0)
            return null;

        // Get rid of "from"
        select = select.substring(i + 5).trim();

        // Either the table name will be the last word in select or it will be
        // followed by a space. If it's followed by a space, extract the table
        // name from select up to the space. Otherwise, the table name is just
        // what's left in select.
        i = select.indexOf(' ');
        if (i >= 0)
            select = select.substring(0, i);
        return select.trim();
    }

    /**
     * Extract the where clause from the String in select. The where clause is expected to follow the text "where "
     * (case insensitive) and is everything in select after that word.
     *
     * @param select String containing the where clause to be extracted
     * @return where clause extracted from select String; null if select does not contain the text "where "
     */
    public static String extractWhereClause(String select) {
        // Find the word "where" - the where clause should be after that.
        int i = select.toLowerCase().indexOf("where ");
        if (i < 0)
            return null;
        return select.substring(i).trim();
    }

    /**
     * Convert a byte[] array to readable string format. This makes the "hex" readable!
     *
     * @param in byte[] to convert to string format
     * @return byte array in String format; null if in is null or is empty
     */
    public static String byteArrayToHexString(byte in[]) {
        if (in == null || in.length <= 0)
            return null;

        byte ch = 0x00;
        int i = 0;

        String pseudo[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        StringBuilder out = new StringBuilder(in.length * 2);

        while (i < in.length) {
            ch = (byte) (in[i] & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4); // shift the bits down
            ch = (byte) (ch & 0x0F); // must do this is high order bit is on!
            out.append(pseudo[(int) ch]); // convert the nibble to a String Character
            ch = (byte) (in[i] & 0x0F); // Strip off low nibble
            out.append(pseudo[(int) ch]); // convert the nibble to a String Character
            i++;
        }
        String rslt = new String(out);
        return rslt;
    }

    /**
     * Convert a hex String into a byte array.
     *
     * @param hex hex String to convert to a byte[]
     * @return byte array representation of the hex String
     * @throws DBDefines.FatalDBUtilLibException if hex's length is not 32
     */
    public static byte[] hexStringToByteArray(String hex) throws DBDefines.FatalDBUtilLibException {

        if (hex.length() != 32) {
            StringBuilder msg = new StringBuilder("ERROR in DBDefines.hexStringToByteArray(" + hex
                    + ").  hex string is not 32 characters long./n");
            ERROR_LOG.add(msg.toString());
            throw new DBDefines.FatalDBUtilLibException(msg.toString());
        }

        byte[] bytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            byte hi = (byte) (charToByte(hex.charAt(i * 2)) << 4);
            byte lo = charToByte(hex.charAt(i * 2 + 1));
            bytes[i] = (byte) (hi + lo);
        }
        return bytes;
    }

    /**
     * Takes a character and returns a byte that corresponds to that character in the hexadecimal world. So, for ch =
     * 'D', 13 will be returned and for ch = '4', 4 will be returned.
     *
     * @param ch hex character the function will return a corresponding byte for
     * @return byte representation of the hexadecimal char in ch
     */
    private static byte charToByte(char ch) {
        if (ch == '0')
            return 0;
        if (ch == '1')
            return 1;
        if (ch == '2')
            return 2;
        if (ch == '3')
            return 3;
        if (ch == '4')
            return 4;
        if (ch == '5')
            return 5;
        if (ch == '6')
            return 6;
        if (ch == '7')
            return 7;
        if (ch == '8')
            return 8;
        if (ch == '9')
            return 9;
        if (ch == 'A')
            return 10;
        if (ch == 'B')
            return 11;
        if (ch == 'C')
            return 12;
        if (ch == 'D')
            return 13;
        if (ch == 'E')
            return 14;
        if (ch == 'F')
            return 15;
        // error
        return Byte.MIN_VALUE;
    }

    /************************ HANDY VARIABLES ************************/

    /**
     * Contains the value returned by <code>new Object().getClass()</code>
     */
    public final static Class<?> OBJECT_CLASS = new Object().getClass();

    /**
     * This variable is what foreign keys that have to be processed at runtime start with. Foreign keys that start with
     * VALUEOF: need to have actual data to determine what id the foreign key refers to. For example, the wftag table
     * serves as a link between the wfdisc table and some other (unknown) table. Among wftag's columns are the tagname
     * and tagid columns. If tagname contains 'evid', then the corresponding tagid value is an evid value. If tagname
     * contains 'orid', then the corresponding tagid value is an orid value. This non-straightforward means of referring
     * to values in another table is represented in the table definition table with the VALUEOF: keyword. The table
     * definition entry for wftag's tagid column is thus valueof:tagname.
     */
    public final static String VALUEOF = "VALUEOF:";

    /**
     * The name of the application that is being run. Many applications check the application variable (set by a value
     * in the parameter file) to make sure it is set to the name of the application the user is running to ensure that
     * users don't use parameter files intended for other applications.
     */
    public static String application = "";

    /**
     * Commonly used delimiter "_".
     */
    public final static String DELIMITER = "_";

    /**
     * "1" constraint definition String.
     */
    public final static String CONSTRAINT_1 = "1";

    /**
     * "0-1" constraint definition String.
     */
    public final static String CONSTRAINT_0_1 = "0/1";

    /**
     * "N" constraint definition String.
     */
    public final static String CONSTRAINT_N = "N";

    /**
     * "0-N" constraint definition String.
     */
    public final static String CONSTRAINT_0_N = "0/N";

    /* Java type handling. */

    /**
     * JAVA_TYPE values that can be found in the table definition table. Indices into this array can be found in the
     * helpfully named constants below.
     */
    public final static String[] javaTypes = new String[]{"UNKNOWN_TYPE", "STRING", "LONG", "DOUBLE", "FLOAT",
            "DATE", "INTEGER", "BOOLEAN", "BYTE", "BIG_DECIMAL", "BLOB", "TIMESTAMP"};

    /**
     * Index into javaTypes array for the UNKNOWN_TYPE javaType String.
     */
    public final static byte UNKNOWN_TYPE = 0;

    /**
     * Index into javaTypes array for the STRING javaType String.
     */
    public final static byte STRING = 1;

    /**
     * Index into javaTypes array for the LONG javaType String.
     */
    public final static byte LONG = 2;

    /**
     * Index into javaTypes array for the DOUBLE javaType String.
     */
    public final static byte DOUBLE = 3;

    /**
     * Index into javaTypes array for the FLOAT javaType String.
     */
    public final static byte FLOAT = 4;

    /**
     * Index into javaTypes array for the DATE javaType String.
     */
    public final static byte DATE = 5;

    /**
     * Index into javaTypes array for the INTEGER javaType String.
     */
    public final static byte INTEGER = 6;

    /**
     * Index into javaTypes array for the BOOLEAN javaType String.
     */
    public final static byte BOOLEAN = 7;

    /**
     * Index into javaTypes array for the BYTE javaType String.
     */
    public final static byte BYTE = 8;

    /**
     * Index into javaTypes array for the BIG_DECIMAL javaType String.
     */
    public final static byte BIG_DECIMAL = 9;

    /**
     * Index into javaTypes array for the BLOB javaType String.
     */
    public final static byte BLOB = 10;

    /**
     * Index into javaTypes array for the CLOB javaType String.
     */
    public final static byte CLOB = 11;

    /**
     * Index into javaTypes array for the TIMESTAMP javaType String.
     */
    public final static byte TIMESTAMP = 12;

    /**
     * Return the byte index into the javaTypes array for a given javaType.
     *
     * @param javaType the String version of a java type
     * @return the byte version of a java type (also an index into the javaTypes array).
     */
    public static byte getJavaTypesIndex(String javaType) {
        javaType = javaType.toUpperCase();
        if (javaType.equals("STRING"))
            return STRING;
        else if (javaType.equals("LONG"))
            return LONG;
        else if (javaType.equals("DOUBLE"))
            return DOUBLE;
        else if (javaType.equals("FLOAT"))
            return FLOAT;
        else if (javaType.equals("DATE"))
            return DATE;
        else if (javaType.equals("INTEGER"))
            return INTEGER;
        else if (javaType.equals("BOOLEAN"))
            return BOOLEAN;
        else if (javaType.equals("BYTE"))
            return BYTE;
        else if (javaType.equals("BIG_DECIMAL"))
            return BIG_DECIMAL;
        else if (javaType.equals("BLOB"))
            return BLOB;
        else if (javaType.equals("TIMESTAMP"))
            return TIMESTAMP;
        else
            return UNKNOWN_TYPE;
    }

    /* Row status bytes. */

    /**
     * UNDETERMINED status.
     */
    public final static int UNDETERMINED = 0;

    /**
     * DROP status.
     */
    public final static int DROP = 2;

    /**
     * DELETE status.
     */
    public final static int DELETE = 4;

    /**
     * INSERT status.
     */
    public final static int INSERT = 8;

    /**
     * UPDATE status.
     */
    public final static int UPDATE = 16;

    /**
     * FIX_ID status.
     */
    public final static int FIX_ID = 32;

    /**
     * FORCE_NEW_ID status.
     */
    public final static int FORCE_NEW_ID = 64;

    /**
     * FORCE_UPDATE status.
     */
    public final static int FORCE_UPDATE = 128;

    /**
     * INVALID status.
     */
    public final static int INVALID = 256;

    /* Load date handling options */

    /**
     * Do not change LDDATE to current date when writing out row data.
     */
    public final static String FIX_LDDATE = "FIX_LDDATE";

    /**
     * Ignore LDDATEs when reading in data from flat files. This will only work for columns named LDDATE that are the
     * last column in each flat file row.
     */
    public final static String IGNORE_FF_LDDATE = "IGNORE_FF_LDDATE";

    /* System properties. */

    /*
     * The constants below are intialized based on information found in the user/application's runtime environment. When
     * these variables exist in the environment, the user/application does not have to specify values for those
     * variables in the ParInfo object(s) - the runtime environment values will be used. These variables are obtained
     * either from the user using an application that passes the variables in to the application using Java's -D option
     * (such as the DBTools applications) or just specifying them as environment variables in their environment. If the
     * user has the same property being handed in via the -D option and set in the environment, the -D option variable
     * will override the environment.
     */

    /**
     * SNL_TOOL_ROOT obtained from user's runtime environment.
     */
    public static String SNL_TOOL_ROOT;

    /**
     * Location of the KBDB account information file.
     */
    public static String KBDB_ACCOUNTS_FILE;

    /**
     * DBTOOLS obtained from user's runtime environment. Only used when DBTools is run in stand_alone mode.
     */
    public static String DBTOOLS;

    /**
     * Default database username obtained from user's runtime environment.
     */
    public static String DEFAULT_USERNAME;

    /**
     * Default database password obtained from user's runtime environment.
     */
    public static String DEFAULT_PASSWORD;

    /**
     * Default database instance obtained from user's runtime environment.
     */
    public static String DEFAULT_INSTANCE;

    /**
     * Default database driver obtained from user's runtime environment.
     */
    public static String DEFAULT_DRIVER;

    /**
     * Default table definition table obtained from user's runtime environment.
     */
    public static String DEFAULT_TABLEDEF;

    /**
     * Default dao type obtained from the user's runtime environment.
     */
    public static String DEFAULT_DAOTYPE;

    /**
     * Default table tablespace obtained from the user's runtime environment.
     */
    public static String DEFAULT_TABLE_TABLESPACE;

    /**
     * Default index tablespace obtained from the user's runtime environment.
     */
    public static String DEFAULT_INDEX_TABLESPACE;

    /**
     * propertiesSet does not really get used for much. It is just a way to get the setProperties() function called when
     * this class is loaded since some checking needs to be done to see how the DEFAULT_X variables above get
     * initialized, and that sort of checking cannot be done outside of a function.
     */
    @SuppressWarnings("unused")
    private final static boolean propertiesSet = setProperties();

    /**
     * Set the DEFAULT_X parameters to the values assigned to DBTOOLS_X using either a) Java's -D option or b) the
     * user's runtime environment. If the user has the same property being handed in via the -D option and set in the
     * environment, the -D option variable will override the environment.
     *
     * @return true - this function is just a way to incorporate some if statement checking into the DBDefines class
     * initialization and gets called by propertiesSet
     */
    private static boolean setProperties() {
        // System.getProperty gets things passed in via -D. System.getenv gets
        // environment variables in the user's environment.

        if (System.getProperty("SNL_TOOL_ROOT") != null)
            SNL_TOOL_ROOT = System.getProperty("SNL_TOOL_ROOT");
        else
            SNL_TOOL_ROOT = System.getenv("SNL_TOOL_ROOT");

        if (System.getProperty("KBDB_ACCOUNTS_FILE") != null)
            KBDB_ACCOUNTS_FILE = System.getProperty("KBDB_ACCOUNTS_FILE");
        else
            KBDB_ACCOUNTS_FILE = System.getenv("KBDB_ACCOUNTS_FILE");

        if (System.getProperty("DBTOOLS") != null)
            DBTOOLS = System.getProperty("DBTOOLS");
        else
            DBTOOLS = System.getenv("DBTOOLS");

        if (System.getProperty("DBTOOLS_USERNAME") != null)
            DEFAULT_USERNAME = System.getProperty("DBTOOLS_USERNAME");
        else
            DEFAULT_USERNAME = System.getenv("DBTOOLS_USERNAME");

        if (System.getProperty("DBTOOLS_PASSWORD") != null)
            DEFAULT_PASSWORD = System.getProperty("DBTOOLS_PASSWORD");
        else
            DEFAULT_PASSWORD = System.getenv("DBTOOLS_PASSWORD");

        if (System.getProperty("DBTOOLS_INSTANCE") != null)
            DEFAULT_INSTANCE = System.getProperty("DBTOOLS_INSTANCE");
        else
            DEFAULT_INSTANCE = System.getenv("DBTOOLS_INSTANCE");

        if (System.getProperty("DBTOOLS_DRIVER") != null)
            DEFAULT_DRIVER = System.getProperty("DBTOOLS_DRIVER");
        else
            DEFAULT_DRIVER = System.getenv("DBTOOLS_DRIVER");

        if (System.getProperty("DBTOOLS_TABLEDEF") != null)
            DEFAULT_TABLEDEF = System.getProperty("DBTOOLS_TABLEDEF");
        else
            DEFAULT_TABLEDEF = System.getenv("DBTOOLS_TABLEDEF");

        if (System.getProperty("DBTOOLS_DAOTYPE") != null)
            DEFAULT_DAOTYPE = System.getProperty("DBTOOLS_DAOTYPE");
        else
            DEFAULT_DAOTYPE = System.getenv("DBTOOLS_DAOTYPE");

        if (System.getProperty("DBTOOLS_TABLE_TABLESPACE") != null)
            DEFAULT_TABLE_TABLESPACE = System.getProperty("DBTOOLS_TABLE_TABLESPACE");
        else
            DEFAULT_TABLE_TABLESPACE = System.getenv("DBTOOLS_TABLE_TABLESPACE");

        if (System.getProperty("DBTOOLS_INDEX_TABLESPACE") != null)
            DEFAULT_INDEX_TABLESPACE = System.getProperty("DBTOOLS_INDEX_TABLESPACE");
        else
            DEFAULT_INDEX_TABLESPACE = System.getenv("DBTOOLS_INDEX_TABLESPACE");

        return true;
    }

    /**
     * Default end of line value obtained from user's runtime environment.
     */
    public static String EOLN = System.getProperty("line.separator");

    /**
     * Default upper case conversion flag obtained from par file, set in Schema. If set to true, all things get
     * uppercased. Otherwise, they retain their original case.
     */
    public static boolean convertToUpperCase = true;

    /**
     * Default path separator obtained from user's runtime environment.
     */
    public static String PATH_SEPARATOR = System.getProperty("file.separator");

    /**
     * Returns a String that contains all of the current System settings that are relevant to DBUtilLib.
     *
     * @return String that contains all of the current System settings that are relevatnt to DBUtilLib
     */
    public static String systemProperties() {
        StringBuilder sys = new StringBuilder();
        sys.append("user.name        = " + System.getProperty("user.name") + EOLN);
        sys.append("user.dir         = " + System.getProperty("user.dir") + EOLN);
        sys.append("os.name          = " + System.getProperty("os.name") + EOLN);
        sys.append("os.version       = " + System.getProperty("os.version") + EOLN);
        sys.append("java.home        = " + System.getProperty("java.home") + EOLN);
        sys.append("java.version     = " + System.getProperty("java.version") + EOLN);

        sys.append("SNL_TOOL_ROOT    = " + SNL_TOOL_ROOT + EOLN);

        String pwstr = "";
        if (DEFAULT_PASSWORD != null && DEFAULT_PASSWORD.length() > 0)
            pwstr = DEFAULT_PASSWORD.replaceAll(".", "*");

        sys.append("DBTOOLS_USERNAME = " + DEFAULT_USERNAME + EOLN);
        sys.append("DBTOOLS_PASSWORD = " + pwstr + EOLN);
        sys.append("DBTOOLS_INSTANCE = " + DEFAULT_INSTANCE + EOLN);
        sys.append("DBTOOLS_DRIVER   = " + DEFAULT_DRIVER + EOLN);
        sys.append("DBTOOLS_TABLEDEF = " + DEFAULT_TABLEDEF + EOLN);
        sys.append("DBTOOLS_DAOTYPE  = " + DEFAULT_DAOTYPE + EOLN);
        sys.append("DBTOOLS_DAOTYPE  = " + DEFAULT_TABLE_TABLESPACE + EOLN);
        sys.append("DBTOOLS_DAOTYPE  = " + DEFAULT_INDEX_TABLESPACE + EOLN);

        sys.append("DBUtilLib version       = " + Version.buildVersion + EOLN);
        sys.append("DBUtilLib build date    = " + Version.buildDate + EOLN);
        sys.append("DBUtilLib build user    = " + Version.buildUser + EOLN);
        sys.append("DBUtilLib build machine = " + Version.buildMachine + EOLN);

        return sys.toString();
    }

    /* DAO types. */

    /**
     * Database DAO type.
     */
    public final static String DATABASE_DAO = "DB";

    /**
     * XML DAO type.
     */
    public final static String XML_DAO = "XML";

    /**
     * Flat file DAO type.
     */
    public final static String FF_DAO = "FF";

    /**
     * DAO Type to indicate that a pool of many DAO types is to be used.
     */
    public final static String POOL_DAO = "POOL_DAO";

    /* FatalDBUtilLibException class. */

    /**
     * This class just extends the Exception interface to create a customized exception that DBUtilLib can throw when it
     * encounters fatal errors.
     */
    @SuppressWarnings("serial")
    public static class FatalDBUtilLibException extends java.lang.Exception {
        /**
         * Creates a new instance of <code>FatalDBUtilLibException</code> without detail message.
         */
        public FatalDBUtilLibException() {
        }

        /**
         * Constructs an instance of <code>FatalDBUtilLibException</code> with the specified detail message.
         *
         * @param msg the detail message.
         */
        public FatalDBUtilLibException(String msg) {
            super("Version: " + (Version.buildVersion.length() == 0 ? "DEVL" : Version.buildVersion) + "\n" + msg);
        }
    }

    /*******************************************************************
     * Author: Ryan D. Emerle Date: 10.12.2004 Desc: Reverse file reader. Reads a file from the end to the beginning
     * Known Issues: Does not support unicode! http://www.emerle.net/comments/view.cfm/p/185
     *******************************************************************/

    public static class ReverseFileReader {
        private RandomAccessFile randomfile;
        private long position;

        public ReverseFileReader(String filename) throws Exception {
            // Open up a random access file
            this.randomfile = new RandomAccessFile(filename, "r");
            // Set our seek position to the end of the file
            this.position = this.randomfile.length();

            // Seek to the end of the file
            this.randomfile.seek(this.position);
            // Move our pointer to the first valid position at the end of the file.
            String thisLine = this.randomfile.readLine();
            while (thisLine == null) {
                this.position--;
                this.randomfile.seek(this.position);
                thisLine = this.randomfile.readLine();
                this.randomfile.seek(this.position);
            }
        }

        // Read one line from the current position towards the beginning
        public String readLine() throws Exception {
            int thisCode;
            char thisChar;
            String finalLine = "";

            // If our position is less than zero already, we are at the beginning
            // with nothing to return.
            if (this.position < 0) {
                return null;
            }

            for (; ; ) {
                // we've reached the beginning of the file
                if (this.position < 0) {
                    break;
                }
                // Seek to the current position
                this.randomfile.seek(this.position);

                // Read the data at this position
                thisCode = this.randomfile.readByte();
                thisChar = (char) thisCode;

                // If this is a line break or carrige return, stop looking
                if (thisCode == 13 || thisCode == 10) {
                    // See if the previous character is also a line break character.
                    // this accounts for crlf combinations
                    this.randomfile.seek(this.position - 1);
                    int nextCode = this.randomfile.readByte();
                    if ((thisCode == 10 && nextCode == 13) || (thisCode == 13 && nextCode == 10)) {
                        // If we found another linebreak character, ignore it
                        this.position = this.position - 1;
                    }
                    // Move the pointer for the next readline
                    this.position--;
                    break;
                }
                // This is a valid character append to the string
                finalLine = thisChar + finalLine;

                // Move to the next char
                this.position--;
            }
            // return the line
            return finalLine;
        }

        public void close() {
            try {
                this.randomfile.close();
            } catch (IOException e) {
                // do nothing since we are just trying to close the file
            }
        }
    }
}

// DEAD CODE:

// private final static long days1970 = 1969*365+1969/4-1969/100+1969/400;
//
// /**
// * Convert a Date to epoch time (number of seconds since 1970/01/01 00:00:00.000)
// * @param date Date the Date object to be converted
// * @return double epoch time (number of seconds since 1970/01/01 00:00:00.000)
// */
// public static double dateToEpochTime(Date date)
// {
// localCalendar.setTime(date);
// int years = localCalendar.get(Calendar.YEAR) - 1;
// long days = years * 365 + years / 4 - years / 100 + years / 400;
// days -= days1970;
// days += localCalendar.get(Calendar.DAY_OF_YEAR) - 1;
//
// return days * 86400.0
// + localCalendar.get(Calendar.HOUR_OF_DAY) * 3600.0
// + localCalendar.get(Calendar.MINUTE) * 60.0
// + localCalendar.get(Calendar.SECOND)
// + localCalendar.get(Calendar.MILLISECOND) * 1e-3;
//
// }
//
// /**
// * Convert an epoch time (number of seconds since 1970/01/01 00:00:00.000)
// * to a Date object
// * @param epochTime double the epoch time to be converted
// * @return Date Date version of the epoch time.
// */
// public static Date epochTimeToDate(double epochTime)
// {
// long i, y, d;
// d = (long) (epochTime / 86400.0);
// double t = epochTime - d * 86400;
// d += +days1970;
// i = d / 146097;
// y = i * 400;
// d -= i * 146097;
// i = d / 36524;
// y += (int) (i * 100);
// d -= i * 36524; // 100 yr intrvls
// i = d / 1461;
// y += (int) (i * 4);
// d -= i * 1461; // 4 yr intrvls
// if (d == 1460)
// {
// // this is december 31 of a leap year
// y += 3; // actual year-1
// d = 365; // actual day-1
// }
// else
// {
// i = d / 365;
// y += (int) (i);
// d -= i * 365; // 1 yr intrvls
// }
//
// // increment year and day (years start at 1 ... there was no year 0)
//
// y++;
// d++;
//
// int h = (int) (t / 3600);
// t -= h * 3600;
// int min = (int) (t / 60);
// t -= min * 60;
// int sec = (int) t;
// t -= sec;
// int ms = (int) (t * 1e3);
//
// localCalendar.set(Calendar.YEAR, (int) y);
// localCalendar.set(Calendar.DAY_OF_YEAR, (int) d);
// localCalendar.set(Calendar.HOUR_OF_DAY, h);
// localCalendar.set(Calendar.MINUTE, min);
// localCalendar.set(Calendar.SECOND, sec);
// localCalendar.set(Calendar.MILLISECOND, ms);
// return new Date(localCalendar.getTimeInMillis());
// }
