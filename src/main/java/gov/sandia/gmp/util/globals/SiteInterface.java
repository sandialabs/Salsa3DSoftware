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

public interface SiteInterface
{
	static final public String STA_NA = null;
	static final public long ONDATE_NA = -1;
	static final public long OFFDATE_NA = 2286324;
	static final public double LAT_NA = -999;
	static final public double LON_NA = -999;
	static final public double ELEV_NA = -999;
	static final public String STANAME_NA = "-";
	static final public String STATYPE_NA = "-";
	static final public String REFSTA_NA = "-";
	static final public double DNORTH_NA = 0.0;
	static final public double DEAST_NA = 0.0;
	
	/**
	 * Station code. This is the code name of a seismic observatory and
	 * identifies a geographic location recorded in the <B>site</B> table.
	 */
	String getSta();
	
	/**
	 * Turn on date. Date on which the station, or sensor indicated began
	 * operating. The columns offdate and ondate are not intended to accommodate
	 * temporary downtimes, but rather to indicate the time period for which the
	 * columns of the station (<I>lat</I>, <I>lon</I>, <I>elev</I>,) are valid
	 * for the given station code. Stations are often moved, but with the
	 * station code remaining unchanged.
	 */
	long getOndate();
	
	/**
	 * Turn off date. This column is the Julian Date on which the station or
	 * sensor indicated was turned off, dismantled, or moved (see <I>ondate</I>)
	 */
	long getOffdate();
	
	/**
	 * Geographic latitude. Locations north of equator have positive latitudes.
	 * <p>
	 * Units: degree
	 */
	double getLat();
	
	/**
	 * Geographic longitude. Longitudes are measured positive East of the
	 * Greenwich meridian.
	 * <p>
	 * Units: degree
	 */
	double getLon();
	
	/**
	 * Surface elevation. This column is the elevation of the surface of the
	 * earth above the seismic station (<B>site</B>) relative to mean sea level
	 * <p>
	 * Units: km
	 */
	double getElev();
	
	/**
	 * Station name/Description. This value is the full name of the station
	 * whose code name is in <I>sta</I> [for example, one record in the
	 * <B>site</B> table connects <I>sta</I> = ANMO to staname = ALBUQUERQUE,
	 * NEW MEXICO (SRO)].
	 */
	String getStaname();
	
	/**
	 * Station type; character string specifies the station type. Recommended
	 * entries are single station (ss) or array (ar).
	 */
	String getStatype();
	
	/**
	 * Reference station. This string specifies the reference station with
	 * respect to which array members are located (see <I>deast</I>,
	 * <I>dnorth</I>).
	 */
	String getRefsta();
	
	/**
	 * Distance North. This column gives the northing or relative position of
	 * array element North of the array center specified by the value of
	 * <I>refsta</I> (see <I>deast</I>).
	 * <p>
	 * Units: km
	 */
	double getDnorth();
	
	/**
	 * Distance East. This column gives the easting or the relative position of
	 * an array element East of the location of the array center specified by
	 * the value of <I>refsta</I> (see <I>dnorth</I>).
	 * <p>
	 * Units: km
	 */
	double getDeast();

	int compareTo(SiteInterface o);
//	@Override
//	public int compareTo(SiteInterface o) {
//		int x = sta.compareTo(o.getSta());
//		if (x == 0)
//			x = (int) Math.signum(this.ondate-o.getOndate());
//		return x;
//	}
	
	boolean equals(Object other);
//	@Override
//	public boolean equals(Object other) {
//		if (this == other) {
//			return true;
//		}
//		if (other == null || !(other instanceof SiteInterface)) {
//			return false;
//		}
//		return this.ondate == ((SiteInterface)other).getOndate() 
//      	&& this.sta.equals(((SiteInterface)other).getSta());
//	}
	
	int hashCode();
//	@Override
//	public int hashCode() {
//		return ((int)ondate) * sta.hashCode();
//	}

}
