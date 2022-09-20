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
package gov.sandia.gmp.baseobjects.globals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;

public class GMPGlobals {
	public final static int LAT = 0;
	public final static int LON = 1;
	public final static int DEPTH = 2;
	public final static int TIME = 3;

//	// These are the NA_VALUEs for arrival, assoc and site table entries
//	// as defined the NNSA Core Schema
//	final static public double TIME_NA_VALUE = -9999999999.999;
//	final static public double DELTIM_NA_VALUE = -1.;
//	final static public double TIMERES_NA_VALUE = -999.;
//	final static public double AZIMUTH_NA_VALUE = -1.;
//	final static public double DELAZ_NA_VALUE = -1.;
//	final static public double AZRES_NA_VALUE = -999.;
//	final static public double SLOW_NA_VALUE = -1.;
//	final static public double DELSLO_NA_VALUE = -1.;
//	final static public double SLORES_NA_VALUE = -999.;
//	final static public double DELTA_NA_VALUE = -1.;
//	final static public double SEAZ_NA_VALUE = -1.;
//	final static public double ESAZ_NA_VALUE = -1.;
//	final static public int ONDATE_NA_VALUE = -1;
//	final static public int OFFDATE_NA_VALUE = 2286324;
//
//	// These are the NA_VALUEs for arrival, assoc and site table entries
//	// as defined the NNSA Core Schema
//	final static public float DELTIM_NA_VALUE_FLOAT = -1f;
//	final static public float TIMERES_NA_VALUE_FLOAT = -999f;
//	final static public float AZIMUTH_NA_VALUE_FLOAT = -1f;
//	final static public float DELAZ_NA_VALUE_FLOAT = -1f;
//	final static public float AZRES_NA_VALUE_FLOAT = -999f;
//	final static public float SLOW_NA_VALUE_FLOAT = -1f;
//	final static public float DELSLO_NA_VALUE_FLOAT = -1f;
//	final static public float SLORES_NA_VALUE_FLOAT = -999f;
//
//	final static public double ORIGERR_NA_VLAUE = -9999999999.999;

	static private String auth = System.getProperty("user.name");

	static private Date lddate = new Date();

	/**
	 * Returns value of auth which defaults to System.getProperty("user.name").
	 * Value of auth can be changed with setAuth().
	 * 
	 * @return
	 */
	static public String getAuth() {
		return auth;
	}

	/**
	 * @param auth the auth to set
	 */
	public static void setAuth(String auth) {
		GMPGlobals.auth = auth;
	}

	/**
	 * Returns value of lddate, which defaults to the date and time when execution
	 * was started. Can be modified with call to setLddate().
	 * 
	 * @return
	 */
	static public Date getLddate() {
		return lddate;
	}

	/**
	 * Return lddate formated as yyyy-MM-dd HH:mm:ss in the GMT time zone
	 * 
	 * @return
	 */
	static public String getLddateString() {
		return GMTFormat.GMT.format(getLddate());
	}

	/**
	 * @param lddate the lddate to set
	 */
	public static void setLddate(Date lddate) {
		GMPGlobals.lddate = lddate;
	}

	/**
	 * convert a string to epochtime. if the string can be converted to an int, it
	 * is assumed to be a julian date. Otherwise it is assumed to be a string
	 * representation of an epochtime. Otherwise it is parsed using one of the
	 * DateFormat object in GMTFormat.
	 * 
	 * @param stime String
	 * @return double
	 * @throws GMPException
	 */
	static public double getTime(String stime) throws GMPException {
		return GMTFormat.getEpochTime(stime.replaceAll("/", "-"));
	}

	/**
	 * Retrieve the current time in the current time zone, formated like yyyy-MM-dd
	 * HH:mm:ss Z.
	 * 
	 * @return
	 */
	static public String getCurrentTimeString() {
		return GMTFormat.localTime.format(new Date());
	}

	/**
	 * Retrieve the specified epochTime (seconds since 1970) as a String in current
	 * time zone like yyyy-MM-dd HH:mm:ss.
	 * 
	 * @param epochTime
	 * @return
	 * @deprecated use methods in GMTFormat instead.
	 */
	@Deprecated
	static public String getTimeString(double epochTime) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(new Date(Math.round(epochTime * 1000.)));
	}

	/**
	 * Retrieve the specified epochTime (seconds since 1970) as a String in current
	 * time zone like yyyy-MM-dd HH:mm:ss.
	 * 
	 * @param epochTime
	 * @return
	 * @deprecated use methods in GMTFormat instead.
	 */
	@Deprecated
	static public String getTimeStringGMT(double epochTime) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(new Date(Math.round(epochTime * 1000.)));
	}

	/**
	 * Convert a Date to a java.sql.Timestamp
	 * 
	 * @param date
	 * @return
	 * @deprecated use GMTFormat.getTimeStamp(Date) instead
	 */
	@Deprecated
	static public java.sql.Timestamp getTimeStamp(Date date) {
		return new java.sql.Timestamp(date.getTime());
	}

	/**
	 * convert a java.sql.Timestamp to epochTime (sec since 1970).
	 * 
	 * @param epochTime
	 * @return
	 * @deprecated use GMTFormat.getTimeStamp() instead
	 */
	@Deprecated
	static public java.sql.Timestamp getTimeStamp(double epochTime) {
		return new java.sql.Timestamp((long) (epochTime * 1000.));
	}

	/**
	 * Convert a jdate to a java.sql.Timestamp
	 * 
	 * @param jdate
	 * @return
	 * @deprecated use GMTFormat.getTimeStamp() instead
	 */
	@Deprecated
	static public java.sql.Timestamp jdateToTimeStamp(int jdate) {
		return new java.sql.Timestamp(jdateToTimeInMillis(jdate));
	}

	/**
	 * Convert a jdate to milliseconds since 1970.
	 * 
	 * @param jdate
	 * @return
	 * @deprecated use methods in GMTFormat instead.
	 */
	@Deprecated
	static public long jdateToTimeInMillis(int jdate) {
		Calendar calendar = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
		calendar.clear();
		calendar.set(Calendar.YEAR, jdate / 1000);
		calendar.set(Calendar.DAY_OF_YEAR, (int) jdate % 1000);
		return calendar.getTimeInMillis();

	}

	/**
	 * Convert jdate to epochTime (sec since 1970).
	 * 
	 * @param jdate
	 * @return
	 * @deprecated use methods in GMTFormat instead.
	 */
	@Deprecated
	static public double jdateToEpochTime(int jdate) {
		return jdateToTimeInMillis(jdate) * 0.001;
	}

	/**
	 * Retrieve the jdate that corresponds to specified Date
	 * 
	 * @param date Date
	 * @return long
	 * @deprecated use methods in GMTFormat instead.
	 */
	@Deprecated
	static public int getJdate(Date date) {
		Calendar calendar = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
		calendar.setTime(date);
		return calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * Retrieve the jdate that corresponds to specified epochTime (sec since 1970)
	 * 
	 * @param seconds double number of seconds since Jan 1, 1970
	 * @return long
	 * @deprecated use methods in GMTFormat instead.
	 */
	@Deprecated
	static public int getJdate(double epochTime) {
		return getJdate(Math.round(epochTime * 1000.));
	}

	/**
	 * Retrieve the jdate that corresponds to specified time
	 * 
	 * @param milliSeconds number of msec since Jan 1, 1970
	 * @deprecated use methods in GMTFormat instead.
	 * @return long
	 */
	@Deprecated
	static public int getJdate(long milliSeconds) {
		return getJdate(new Date(milliSeconds));
	}

	static public Date jdateToDate(int ondate) {
		return new Date(jdateToTimeInMillis(ondate));
	}

	/**
	 * Requires either one or two Dates be specified. If only one is specified,
	 * second is set to current time. Computes the second time minus the first time.
	 * Then formats a String with the elapsed time scaled to ~optimal units, either
	 * seconds, minutes, hours or days, depending on how much time has elapsed.
	 * 
	 * @param time
	 * @return
	 */
	static public String ellapsedTime(Date... time) {
		Date endTime;
		if (time.length > 1)
			endTime = time[1];
		else
			endTime = new Date();
		return ellapsedTime((endTime.getTime() - time[0].getTime()) * 0.001);
	}

	/**
	 * Formats ellapsed time. Input is in seconds. Output is either seconds,
	 * minutes, hours or days, depending on the amount of time specified.
	 * 
	 * @param dt in seconds
	 * @return
	 */
	static public String ellapsedTime(double dt) {
		String units = "seconds";
		if (dt >= 60.) {
			dt /= 60.;
			units = "minutes";

			if (dt >= 60.) {
				dt /= 60.;
				units = "hours";

				if (dt >= 24.) {
					dt /= 24.;
					units = "days";
				}
			}
		}
		return String.format("%9.6f %s", dt, units);
	}

}
