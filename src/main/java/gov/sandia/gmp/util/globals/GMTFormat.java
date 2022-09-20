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
package gov.sandia.gmp.util.globals;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import gov.sandia.gmp.util.exceptions.GMPException;

/**
 * Manages conversion of date and time information between 4 different
 * formats: Strings, Dates, epoch times, and julian days, with the 
 * Strings using primarily the GMT timezone.
 * 
 * <p>There are 6 static final DateFormat objects that can be used
 * by applications directly to format and parse Dates.  The Strings
 * that are returned by these DateFormat objects are complete in the 
 * sense that they can be parsed back to the Date that was used to 
 * generate them, with at least 1 second precision.  
 * <p>ASSUMPTION: if timezone information is not part of the DateFormat
 * format String, then the GMT time zone is assumed. 
 * 
 * <p>Method getDate(String) will take a String that was generated
 * by any of the DateFormat objects and return the correct Date.
 * 
 * <p>getDate(String) will try to parse the String as an int 
 * (interpreted as a jdate) and then as a double (interpreted as epoch time).  
 * It attempts to parse the String using the DataFormat objects only after 
 * attempts to parse the String as an int or a double fail.
 * 
 * <p>Methods toGMT() and toLocal() convert milliseconds since 1970 
 * between GMT timezone and local time zone.  
 * 
 * <p>Date objects store milliseconds since 1970 GMT.
 * Unfortunately, when JDBC returns an oracle.sql.TIMESTAMP object from the 
 * database, the year, month, day, hour, minute, second and nanosecond information
 * is converted to milliseconds since 1970 local time (not GMT).  
 * This means that in order to construct a java.util.Date object 
 * one should use:
 * <p>new java.util.Date(GMTFormat.toGMT(oracleTimestamp.timestampValue().getTime())); 
 * @author sballar
 *
 */
public class GMTFormat
{
	
	static final public long MIN_JDATE = -1;
	
	static final public long MAX_JDATE = 2286324;
	
	/** 
	 * Minimum epochTime.
	 * MINETIME = -1E10 corresponds to jdate 1653041
	 */
	static final public double MIN_EPOCH_TIME = -1E10;
	
	/** 
	 * Maximum epochTime.
	 * MAXETIME = 1E10 corresponds to jdate 2286324.
	 */
	static final public double MAX_EPOCH_TIME = 1E10;
	

	/**
	 * Uses format "yyyy-MM-dd HH:mm:ss" with GMT timezone.
	 */
	static final public DateFormat GMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Uses format "yyyy-MM-dd HH:mm:ss.SSS" with GMT timezone.
	 */
	static final public DateFormat GMT_MS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * Uses format "yyyy-MM-dd HH:mm:ss Z" with GMT timezone.
	 * GMT_Z.format() always ends with '+0000'.
	 */
	static final public DateFormat GMT_Z = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

	/**
	 * Uses format "yyyy-MM-dd HH:mm:ss.SSS Z" with GMT timezone.
	 * GMT_MS_Z.format() always ends with '+0000'.
	 */
	static final public DateFormat GMT_MS_Z = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");

	/**
	 * Uses format "yyyy-MM-dd HH:mm:ss Z" with local timezone.
	 */
	static final public DateFormat localTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

	/**
	 * Uses format "yyyy-MM-dd HH:mm:ss.SSS Z" with local timezone.
	 */
	static final public DateFormat localTime_MS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");

	/**
	 * GregorianCalendar object initialized with GMT timezone
	 */
	static final public Calendar GMTCalendar = 
		new GregorianCalendar(new SimpleTimeZone(0, "GMT"));

	/**
	 * GregorianCalendar object initialized with GMT timezone
	 */
	static final public Calendar LocalCalendar = new GregorianCalendar();
	
	/**
	 * Set the TimeZone in the GMT DateFormat objects
	 */
	static 
	{
		GMT.setTimeZone(TimeZone.getTimeZone("GMT"));
		GMT_MS.setTimeZone(TimeZone.getTimeZone("GMT"));
		GMT_Z.setTimeZone(TimeZone.getTimeZone("GMT"));
		GMT_MS_Z.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Convert a String to a Date. 
	 * <p>If the String can be converted to an integer, it is interpreted
	 * to be a julian date in GMT time zone.
	 * <p>Otherwise, if the String can be converted to a double, it is
	 * interpreted as an epoch time (seconds since 1970).
	 * <p>Otherwise, an attempt is made to parse the String using any of:
	 * <br>yyyy-mm-dd hh:mm:ss.SSS Z
	 * <br>yyyy-mm-dd hh:mm:ss.SSS
	 * <br>yyyy-mm-dd hh:mm:ss Z
	 * <br>yyyy-mm-dd hh:mm:ss
	 * <br>(If Z is not specified, then the GMT timezone is assumed.)
	 * 
	 * @param stime String
	 * @return java.util.Date
	 * @throws GMPException 
	 */
	static public java.util.Date getDate(String stime) throws GMPException 
	{
		try
		{
			// convert integer to julian day: YYYYDDD
			int jdate = Integer.parseInt(stime);
			Calendar calendar = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
			calendar.clear();
			calendar.set(Calendar.YEAR, jdate / 1000);
			calendar.set(Calendar.DAY_OF_YEAR, jdate % 1000);
			return calendar.getTime();

		} catch (NumberFormatException ex)
		{
		}

		try
		{
			// convert double: epoch time (seconds since 1970)
			return new java.util.Date(Math.round(1000.*Double.parseDouble(stime)));
		} 
		catch (NumberFormatException ex1)
		{
		}

		try
		{
			return GMT_MS_Z.parse(stime);
		} 
		catch (ParseException e)
		{
		}

		try
		{
			return GMT_MS.parse(stime);
		} 
		catch (ParseException e)
		{
		}

		try
		{
			return GMT_Z.parse(stime);
		} 
		catch (ParseException e)
		{
		}

		try
		{
			return GMT.parse(stime);
		} 
		catch (ParseException e)
		{
		}

		throw new GMPException("Unable to parse "+stime);
	}

	/**
	 * Convert epochTime (seconds since 1970) to Date
	 * @param epochTime must be double
	 * @return java.util.Date
	 */
	static public java.util.Date getDate(double epochTime)
	{
		return new java.util.Date(Math.round(epochTime*1000.));
	}

	/**
	 * Convert date to epoch time (seconds since 1970).
	 * @param date java.util.Date
	 * @return double epoch time (seconds since 1970).
	 */
	static public double getEpochTime(java.util.Date date) 
	{
		return date.getTime() * 1e-3;
	}

	/**
	 * Convert a String to epoch time (seconds since 1970). 
	 * <p>If the String can be converted to an integer, it is interpreted
	 * to be a julian date in GMT time zone.
	 * <p>Otherwise, if the String can be converted to a double, it is
	 * interpreted as an epoch time (seconds since 1970) and is returned
	 * without modification.
	 * <p>Otherwise, an attempt is made to parse the String using any of:
	 * <br>yyyy-mm-dd hh:mm:ss.SSS Z
	 * <br>yyyy-mm-dd hh:mm:ss.SSS
	 * <br>yyyy-mm-dd hh:mm:ss Z
	 * <br>yyyy-mm-dd hh:mm:ss
	 * <br>(If Z is not specified, then the GMT timezone is assumed.)
	 * 
	 * @param time String
	 * @return Date
	 * @throws GMPException 
	 */
	static public double getEpochTime(String time) throws GMPException 
	{
		try
		{
			int jdate = Integer.parseInt(time);
			return getEpochTime(jdate);
		} 
		catch (NumberFormatException ex)
		{
		}

		try
		{
			return Double.parseDouble(time);
		} 
		catch (NumberFormatException ex)
		{
		}

		return getEpochTime(getDate(time));
	}

	/**
	 * Convert a jdate (int yyyyddd) into an epoch time (double seconds since 1970).
	 * Result will range between -1E10 and 1E10.
	 * @param jdate
	 * @return int yyyyddd
	 */
	static public double getEpochTime(long jdate) 
	{
		return jdate >= MAX_JDATE ? MAX_EPOCH_TIME : Math.max(getEpochTime(getDate(jdate)), MIN_EPOCH_TIME);
	}

	/**
	 * Convert a String to epoch time (seconds since 1970). 
	 * <p>If the String can be converted to an integer, it is interpreted
	 * to be a julian date in GMT time zone.
	 * Before returning it, 1 day minus 1 millisecond is added to the
	 * epoch time.
	 * <p>Otherwise, if the String can be converted to a double, it is
	 * interpreted as an epoch time (seconds since 1970) and is returned
	 * without modification.
	 * <p>Otherwise, an attempt is made to parse the String using any of:
	 * <br>yyyy-mm-dd hh:mm:ss.SSS Z
	 * <br>yyyy-mm-dd hh:mm:ss.SSS
	 * <br>yyyy-mm-dd hh:mm:ss Z
	 * <br>yyyy-mm-dd hh:mm:ss
	 * <br>(If Z is not specified, then the GMT timezone is assumed.)
	 * 
	 * @param time String
	 * @return Date
	 * @throws GMPException 
	 */
	static public double getOffTime(String time) 
	throws GMPException
	{
		try
		{
			int jdate = Integer.parseInt(time);
			return getOffTime(jdate);
		} 
		catch (NumberFormatException ex)
		{
		}

		try
		{
			return Double.parseDouble(time);
		} 
		catch (NumberFormatException ex)
		{
		}

		return getEpochTime(getDate(time));
	}

	/**
	 * Convert a jdate (int yyyyddd) into an epoch time (double seconds since 1970).
	 * Before returning it, 1 day minus 1 millisecond is added to the epoch time.
	 * Range will be +/- 1e10 inclusive.
	 * @param jdate
	 * @return int yyyyddd
	 */
	static public double getOffTime(long jdate) 
	{
		return jdate <= MIN_JDATE ? MIN_EPOCH_TIME 
				: jdate >= MAX_JDATE ? MAX_EPOCH_TIME 
					: getEpochTime(getDate(jdate))+86399.999;
	}

	/**
	 * Convert jdate (int yyyyddd) into a Date object.
	 * @param jdate (int yyyyddd)
	 * @return java.util.Date
	 */
	static public synchronized java.util.Date getDate(long jdate)
	{
		GMTCalendar.clear();
		GMTCalendar.set(Calendar.YEAR,  (int)jdate / 1000);
		GMTCalendar.set(Calendar.DAY_OF_YEAR, (int)jdate % 1000);
		return GMTCalendar.getTime();
	}

	/**
	 * Convert jdate (int yyyyddd) into a Date
	 * @param date
	 * @return jdate (int yyyyddd)
	 */
	static public synchronized int getJDate(java.util.Date date)
	{
		GMTCalendar.setTime(date);
		return GMTCalendar.get(Calendar.YEAR) * 1000
		+ GMTCalendar.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * Convert a String to julian day. Fractional days are truncated. 
	 * <p>If the String can be converted to an integer, the integer 
	 * is returned without modification.
	 * <p>Otherwise, if the String can be converted to a double, it is
	 * interpreted as an epoch time (seconds since 1970).
	 * <p>Otherwise, an attempt is made to parse the String using any of:
	 * <br>yyyy-mm-dd hh:mm:ss.SSS Z
	 * <br>yyyy-mm-dd hh:mm:ss.SSS
	 * <br>yyyy-mm-dd hh:mm:ss Z
	 * <br>yyyy-mm-dd hh:mm:ss
	 * <br>(If Z is not specified, then the GMT timezone is assumed.)
	 * 
	 * @param stime String
	 * @return java.util.Date
	 * @throws GMPException 
	 */
	static public int getJDate(String stime) throws GMPException
	{
		try
		{
			// convert integer to julian day: YYYYDDD
			return Integer.parseInt(stime);
		} 
		catch (NumberFormatException ex)
		{
		}

		return getJDate(getDate(stime));
	}

	/**
	 * Convert epochTime (seconds since 1970) to jdate (int yyyyddd).
	 * Result will be between 1653041 and 2286324, inclusive.
	 * @param epochTime double seconds since 1970
	 * @return int yyyyddd
	 */
	static public int getJDate(double epochTime)
	{
		return (int) (epochTime <= MIN_EPOCH_TIME ? MIN_JDATE : epochTime >= MAX_EPOCH_TIME ? MAX_JDATE 
				: getJDate(getDate(epochTime)));
	}

	/**
	 * Convert milliseconds from 1970 from local to GMT timezone.  
	 * It is unusual to have to do this since Dates store time this way 
	 * already.  A situation where it is necessary is when working with a
	 * oracle.sql.TIMESTAMP object obtained from a database via JDBC
	 * (dbUtilLib does this).  If you call oracleTimestamp.timestampValue().getTime()
	 * time is converted from [year, month, day, hour, minute, second, nanosecond]
	 * to milliseconds since 1970 using the local time zone, not GMT.  This is
	 * not compatible with java.util.Date.  To construct a java.util.Date object
	 * from an oracle.sql.TIMESTAMP object do it this way:
	 * 
	 * <p>javaDate = new java.util.Date(GMTFormat.toGMT(oracleTimestamp.timestampValue().getTime()));
	 * @param msecLocal
	 * @return milliseconds since 1970 GMT.
	 */
	static public synchronized long toGMT(long msecLocal)
	{
		LocalCalendar.setTimeInMillis(msecLocal);
		GMTCalendar.set(Calendar.YEAR, LocalCalendar.get(Calendar.YEAR));
		GMTCalendar.set(Calendar.DAY_OF_YEAR, LocalCalendar.get(Calendar.DAY_OF_YEAR));
		GMTCalendar.set(Calendar.HOUR_OF_DAY, LocalCalendar.get(Calendar.HOUR_OF_DAY));
		GMTCalendar.set(Calendar.MINUTE, LocalCalendar.get(Calendar.MINUTE));
		GMTCalendar.set(Calendar.SECOND, LocalCalendar.get(Calendar.SECOND));
		GMTCalendar.set(Calendar.MILLISECOND, LocalCalendar.get(Calendar.MILLISECOND));
		return GMTCalendar.getTimeInMillis();		
	}

	/**
	 * Convert milliseconds from 1970 from GMT to local timezone.  
	 * It is unusual to have to do this since Dates store time this way 
	 * already.  A situation where it is necessary is when working with a
	 * oracle.sql.TIMESTAMP object obtained from a database via JDBC
	 * (dbUtilLib does this).  
	 * If you create a new java.sql.Timestamp(timeInMillis) then time 
	 * is converted from milliseconds since 1970 into 
	 * [year, month, day, hour, minute, second, nanosecond] using the 
	 * local time zone.  When transfered to oracle and stored in a 
	 * column of the TIMESTAMP, the year,day,etc will not be in GMT.
	 * To transfer time in milliseconds since 1970 to an oracle db column
	 * of type TIMESTAMP (without TIMEZONE), create a java.sql.Timestamp 
	 * object like this:
	 * <p>new java.sql.Timestamp(GMTFormat.toLocal(milliseconds));
	 * @param msecGMT
	 * @return milliseconds since 1970 Local.
	 */
	static public synchronized long toLocal(long msecGMT)
	{
		GMTCalendar.setTimeInMillis(msecGMT);
		LocalCalendar.set(Calendar.YEAR, GMTCalendar.get(Calendar.YEAR));
		LocalCalendar.set(Calendar.DAY_OF_YEAR, GMTCalendar.get(Calendar.DAY_OF_YEAR));
		LocalCalendar.set(Calendar.HOUR_OF_DAY, GMTCalendar.get(Calendar.HOUR_OF_DAY));
		LocalCalendar.set(Calendar.MINUTE, GMTCalendar.get(Calendar.MINUTE));
		LocalCalendar.set(Calendar.SECOND, GMTCalendar.get(Calendar.SECOND));
		LocalCalendar.set(Calendar.MILLISECOND, GMTCalendar.get(Calendar.MILLISECOND));
		return LocalCalendar.getTimeInMillis();		
	}
	
	/**
	 * Convert a java.sql.Timestamp object obtained by calling
	 * oracle.sql.TIMESTAMP.timestampValue(), into a java.util.Date object
	 * which is based on the correct milliseconds since 1970 GMT value.
	 * <p>Assume a database table has a column of type TIMESTAMP that stores date-time
	 * information in GMT timezone.  When retrieved from 
	 * JDBC, you get an oracle.sql.TIMESTAMP object, which stores date-time information
	 * in fields like [year, month, day, hour, min, sec, nanoseconds], with no 
	 * timezone information.  The easiest way to access this information is to use 
	 * the oracle.sql.TIMESTAMP.timestampValue() which returns a java.sql.Timestamp 
	 * object.  java.sql.Timestamp stores two fields: seconds since 1970 GMT and 
	 * nanoseconds.  Since no timezone information is available, the conversion is 
	 * made using the current default timezone, not GMT.  Given that the database
	 * stored information in the GMT timezone the milliseconds since 1970 stored 
	 * in the java.sql.Timestamp object is not correct.  This method converts the 
	 * milliseconds since 1970 to the GMT timezone.
	 * @param timestamp
	 * @return java.util.Date with correct milliseconds since 1970 GMT.
	 * @throws SQLException
	 */
	public java.util.Date getDateGMT(java.sql.Timestamp timestamp) 
	throws SQLException
	{
		return new java.util.Date(GMTFormat.toGMT(timestamp.getTime()));
	}

	/**
	 * Convert a java.util.Date object based on the correct milliseconds since 1970 GMT
	 * date-time, and convert it into a java.sql.Timestamp object based on correct 
	 * [year, month, day, hour, minute, second, milliseconds] information in GMT timezone,
	 * ready for sending to a database column of type TIMESTAMP (with no time zone information).
	 * @param date
	 * @return java.sql.Timestamp with correct calendar fields for sending to oracle DB
	 * column of type TIMESTAMP.
	 * @throws SQLException
	 */
	public java.sql.Timestamp getTimestampGMT(java.util.Date date) 
	throws SQLException
	{
		return new java.sql.Timestamp(GMTFormat.toLocal(date.getTime()));
	}

	/**
	 * Return the current date-time in local time zone with format "yyyy-MM-dd HH:mm:ss Z"
	 * @return string with local date-time
	 */
	public static String getNow()
	{
		return localTime.format(new java.util.Date());
	}
	
}
