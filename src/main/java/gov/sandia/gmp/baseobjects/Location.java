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
package gov.sandia.gmp.baseobjects;

import static java.lang.Math.atan2;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.io.Serializable;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * <p>
 * Location
 * </p>
 * 
 * <p>
 * Location represents a 4D position in space-time. It extends GeoVector by
 * adding a time attribute (epoch time: seconds since 1970).
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
public class Location extends GeoVector implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 554972255074308272L;

	/**
	 * The epoch time of this location (seconds since 1970).
	 */
	protected double time;

	/**
	 * 
	 * @param g
	 * @param time epoch time (seconds since 1970).
	 * @throws GMPException
	 */
	public Location(GeoVector g, double time) throws GMPException {
		super(g);
		this.time = time;
	}

	/**
	 * 
	 * @param lat
	 * @param lon
	 * @param depth
	 * @param inDegrees
	 * @param time
	 * @throws GMPException
	 */
	public Location(double lat, double lon, double depth, boolean inDegrees, double time) throws GMPException {
		this(new GeoVector(lat, lon, depth, inDegrees), time);
	}

	/**
	 * 
	 * @param v
	 * @param radius
	 * @param time
	 * @throws GMPException
	 */
	public Location(double[] v, double radius, double time) throws GMPException {
		this(new GeoVector(v, radius), time);
	}

	/**
	 * Copy construct that makes deep copies of everything.
	 * 
	 * @param other
	 * @throws GMPException
	 */
	public Location(Location other) throws GMPException {
		this(other, other.getTime());
	}

	/**
	 * Return a deep copy.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException  {  
		return super.clone();
	}
    

	/**
	 * Returns the 4 dimensional distance between two Locations, in km. Time is
	 * converted to distance by multiplying by 8 km/sec.
	 * 
	 * @param other Location
	 * @return double
	 */
	public double distance4D(Location other) {
		return sqrt(pow(distance3D(other), 2.) + pow((time - other.time) * 8.0, 2));
	}

	/**
	 * 
	 * @param other
	 */
	public void setLocation(Location other) {
		setGeoVector(other.getUnitVector(), other.getRadius());
		time = other.time;
	}
	
	/**
	 * Change the latitude and longitude of this Location
	 * @param latitude
	 * @param longitude
	 * @param inDegrees if true, latitude and longitude are assumed to be in degrees,
	 * otherwise, in radians.
	 */
	public void setLatLon(double latitude, double longitude, boolean inDegrees) {
		setLatLonDepth(latitude, longitude, getDepth(), inDegrees);
	}

	/**
	 * Change the latitude, longitude and depth of this Location
	 * @param latitude
	 * @param longitude
	 * @param depth depth in km
	 * @param inDegrees if true, latitude and longitude are assumed to be in degrees,
	 * otherwise, in radians.
	 */
	public void setLatLonDepth(double latitude, double longitude, double depth, boolean inDegrees) {
		setGeoVector(latitude, longitude, depth, inDegrees);
	}

	/**
	 * Change the latitude, longitude, depth and time of this Location
	 * @param latitude
	 * @param longitude
	 * @param depth depth in km
	 * @param time epoch time is seconds since 1970
	 * @param inDegrees if true, latitude and longitude are assumed to be in degrees,
	 * otherwise, in radians.
	 */
	public void setLatLonDepthTime(double latitude, double longitude, double depth, double time, boolean inDegrees) {
		setGeoVector(latitude, longitude, depth, inDegrees);
		setTime(time);
	}

	/**
	 * 
	 * @return epoch time in seconds
	 */
	public double getTime() {
		return time;
	}

	/**
	 * 
	 * @param time epoch time in seconds
	 */
	public void setTime(double time) {
		this.time = time;
	}

	/**
	 * Compute and return the julian date
	 * 
	 * @return the julian date
	 */
	public int getJDate() {
		return GMTFormat.getJDate(time);
	}

	/**
	 * Change the position of this Location by the specified amount. Note that
	 * change in longitude is measured along a great circle path that leaves the
	 * position in question in easterlly direction. That is not the same thing as
	 * leaving along a small circle.
	 * 
	 * @param dloc double[] change in lat (radians), lon (radians), depth (km), time
	 *             (seconds)
	 */
	public void change(double[] dloc) {
		change(dloc[GMPGlobals.LAT], dloc[GMPGlobals.LON], dloc[GMPGlobals.DEPTH], dloc[GMPGlobals.TIME]);
	}

	/**
	 * Change the position of this Location by the specified amount. Note that
	 * change in longitude is measured along a great circle path that leaves the
	 * position in question in easterlly direction. That is not the same thing as
	 * leaving along a small circle.
	 * 
	 * @param dlat   radians
	 * @param dlon   radians
	 * @param ddepth km
	 * @param dtime  seconds
	 */
	public void change(double dlat, double dlon, double ddepth, double dtime) {
		// change ddepth from (change in depth) to (new total depth)
		ddepth += getDepth();
		double[] u = v.clone();
		VectorUnit.move(u, sqrt(dlat * dlat + dlon * dlon), atan2(dlon, dlat), v);
		setDepth(ddepth);
		time += dtime;
	}

	/**
	 * Retrieve a new Location that results from changing the position of this
	 * Location by the specified amount.
	 * 
	 * @param dloc double[] change in lat (radians), lon (radians), depth (km), time
	 *             (seconds)
	 * @return new Location
	 * @throws CloneNotSupportedException 
	 */
	public Location move(double[] dloc) throws CloneNotSupportedException {
		return move(dloc[GMPGlobals.LAT], dloc[GMPGlobals.LON], dloc[GMPGlobals.DEPTH], dloc[GMPGlobals.TIME]);
	}

	/**
	 * Retrieve a new Location that results from changing the position of this
	 * Location by the specified amount.
	 * 
	 * @param dlat   radians
	 * @param dlon   radians
	 * @param ddepth km
	 * @param dtime  seconds
	 * @throws CloneNotSupportedException 
	 */
	public Location move(double dlat, double dlon, double ddepth, double dtime) throws CloneNotSupportedException {
		ddepth += getDepth();
		Location x = (Location) this.clone();
		x.change(dlat, dlon, 0., dtime);
		x.setDepth(ddepth);
		return x;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = super.hashCode();
	    long temp;
	    temp = Double.doubleToLongBits(time);
	    result = prime * result + (int) (temp ^ (temp >>> 32));
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (!super.equals(obj))
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    Location other = (Location) obj;
	    if (Double.doubleToLongBits(time) != Double.doubleToLongBits(other.time))
		return false;
	    return true;
	}

}
