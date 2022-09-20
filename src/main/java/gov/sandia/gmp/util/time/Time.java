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
package gov.sandia.gmp.util.time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Time implements TimeInterface, Comparable<TimeInterface>, Comparator<TimeInterface>
{
	private double time;
	
	public void setTime(double time) { this.time = time; }
	
	@Override
	public double getTime() { return time; }
	
	public Time(double time) { this.time = time; }
	
	static public final Comparator<TimeInterface> comparator = new Comparator<TimeInterface>()  {
		@Override
		public int compare(TimeInterface o1, TimeInterface o2) {
			return (int) Math.signum(o1.getTime()-o2.getTime());
		}
	};
	
	@Override
	public int compareTo(TimeInterface arg0) { return comparator.compare(this, arg0); }

	@Override
	public int compare(TimeInterface o1, TimeInterface o2) { return comparator.compare(o1, o2); }

	/**
	 * convert days to seconds.
	 * @param days
	 * @return seconds
	 */
	static public double days(double days) {return days*86400.;}

	/**
	 * convert hours to seconds.
	 * @param hours
	 * @return seconds
	 */
	static public double hours(double hours) {return hours*3600.;}

	/**
	 * convert minutes to seconds.
	 * @param minutes
	 * @return seconds
	 */
	static public double minutes(double minutes) {return minutes*60.;}
	
	static public double getTime(String timeString) throws Exception
	{
		String[] parts = timeString.trim().split("\\s+");
		if (parts.length != 2)
			throw new Exception("Cannot parse time from string "+timeString);
		
		double t = Double.parseDouble(parts[0]);
		String units = parts[1].trim().toLowerCase();
		if (units.startsWith("sec"))
			return t;
		if (units.startsWith("min"))
			return minutes(t);
		if (units.startsWith("hour"))
			return hours(t);
		if (units.startsWith("day"))
			return days(t);
		return Double.NaN;
	}

	/**
	 * Formats elapsed time.  Input is in milliseconds. Output is
	 * either milliseconds, seconds, minutes, hours or days, depending on the 
	 * amount of time specified.
	 * @param dt in seconds
	 * @return elapsed time
	 */
	static public String elapsedTime(long msec)
	{
		double dt;
		String units;
		if (msec >= 86400000L)
		{
			dt = msec/86400000.0;
			units = "days";
		}
		else if (msec >= 3600000L)
		{
			dt = msec/3600000.0;
			units = "hours";
		}
		else if (msec >= 60000L)
		{
			dt = msec/60000.0;
			units = "minutes";
		}
		else if (msec >= 1000L)
		{
			dt = msec/1000.0;
			units = "seconds";
		}
		else
		{
			dt = msec;
			units = "msec";
		}
		return String.format("%9.6f %s", dt, units);
	}

	/**
	 * Retrieve a sublist of TimeInterface objects that contains all elements with 
	 * getTime() >= time.  It is assumed that the elements of times are already sorted
	 * into time ascending order.
	 * @param times
	 * @param time
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static public List<? extends TimeInterface> getYoungElements(List<? extends TimeInterface> times, double time)
	{
		if (times.isEmpty() || time > times.get(times.size()-1).getTime()) 
			return (List<TimeInterface>) times.subList(0, 0);

		int imin = Time.hunt(times, time);
		if (imin < 0) 
			imin = 0;
		else if (time != times.get(imin).getTime()) 
			++imin;
		return (List<TimeInterface>) times.subList(imin, times.size());
	}

	/**
	 * Retrieve a sublist of TimeInterface objects that contains all elements with 
	 * getTime() < time.  It is assumed that the elements of times are already sorted
	 * into time ascending order.
	 * @param times
	 * @param time
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static public List<? extends TimeInterface> getOldElements(List<? extends TimeInterface> times, double time)
	{
		if (times.isEmpty()  || time <= times.get(0).getTime()) 
			return (List<TimeInterface>) times.subList(0, 0);
		int imax = Time.hunt(times, time);
		if (time != times.get(imax).getTime()) ++imax;

		return (List<TimeInterface>) times.subList(0, imax);
	}


	/**
	 * Return a sublist of the specified list of TimeInterface objects such that all elements in sublist
	 * are >= tmin and < tmax.  It is assumed that the elements of times are already sorted
	 * into time ascending order.
	 * @param times
	 * @param tmin
	 * @param tmax
	 * @return
	 */
	@SuppressWarnings("unchecked")
	static public List<? extends TimeInterface> timeRange(List<? extends TimeInterface> times, double tmin, double tmax)
	{
		if (times.isEmpty() || tmax <= times.get(0).getTime() || tmin > times.get(times.size()-1).getTime()) 
			return times.subList(0, 0);

		int imin = Time.hunt(times, tmin);
		if (imin < 0) 
			imin = 0;
		else if (tmin != times.get(imin).getTime()) 
			++imin;

		int imax = Time.hunt(times, tmax);
		if (tmax != times.get(imax).getTime()) 
			++imax;

		return (List<TimeInterface>) times.subList(imin, imax);
	}

	/**
	 * Find index i such that t is >= times[i] and < times[i+1]. 
	 * If t <  times[0] returns -1. 
	 * If t >= times[times.length-1] return times.length-1
	 * <p>
	 * This method is translation from Numerical Recipes in C++.
	 * 
	 * @param times a List of objects that implement the TimeInterface
	 * @param t the time for which to search.
	 */
	static public int hunt(List<? extends TimeInterface> times, double t)
	{
		if (times.isEmpty()) return -1;

		int jl=-1;
		int ju=times.size();
		int jm;
		while (ju-jl > 1) 
		{
			jm=(ju+jl) >> 1;
			if (t >= times.get(jm).getTime()) // == ascnd)
				jl=jm;
			else
				ju=jm;
		}
		return jl;
	}

	/**
	 * Given an array of objects that implement TimerInterface, break it up into an ArrayList of sublists where the first 
	 * sublist starts at tstart and each sublist spans time range tinterval. The elements of the returned arraylist 
	 * are java sublists of the specified array so modifications of the sublists will be reflected in the input array. 
	 * @param list
	 * @param tstart
	 * @param tinterval
	 * @return
	 */
	static public ArrayList<List<? extends TimeInterface>> sublists(List<? extends TimeInterface> list, double tstart, double tinterval)
	{
		int nIntervals = (int) Math.ceil((list.get(list.size()-1).getTime() - tstart)/tinterval);
		ArrayList<List<? extends TimeInterface>> slists = new ArrayList<List<? extends TimeInterface>>(nIntervals);
		for (int i=0; i<nIntervals; ++i)
			slists.add(timeRange(list, i*tinterval+tstart, (i+1)*tinterval+tstart));
		return slists;
	}

}
