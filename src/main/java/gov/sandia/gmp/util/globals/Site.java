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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Scanner;

import gov.sandia.gmp.util.numerical.vector.VectorGeo;


/**
 * A representation of a seismic station.  Not used by GeoTess directly but used
 * by several classes that extend GeoTess.
 * 
 * @author jrhipp
 *
 */
public class Site implements SiteInterface, Serializable, Comparable<SiteInterface> 
{
    private static final long serialVersionUID = 1L;

    /**
     * Station code. This is the code name of a seismic observatory and
     * identifies a geographic location recorded in the <B>site</B> table.
     */
    protected String sta;

    static final public String STA_NA = null;

    /**
     * Turn on date. Date on which the station, or sensor indicated began
     * operating. The columns offdate and ondate are not intended to accommodate
     * temporary downtimes, but rather to indicate the time period for which the
     * columns of the station (<I>lat</I>, <I>lon</I>, <I>elev</I>,) are valid
     * for the given station code. Stations are often moved, but with the
     * station code remaining unchanged.
     */
    protected long ondate;

    static final public long ONDATE_NA = -1;

    /**
     * Turn off date. This column is the Julian Date on which the station or
     * sensor indicated was turned off, dismantled, or moved (see <I>ondate</I>)
     */
    protected long offdate;

    static final public long OFFDATE_NA = 2286324;

    /**
     * Geographic latitude. Locations north of equator have positive latitudes.
     * <p>
     * Units: degree
     */
    protected double lat;

    static final public double LAT_NA = -999;

    /**
     * Geographic longitude. Longitudes are measured positive East of the
     * Greenwich meridian.
     * <p>
     * Units: degree
     */
    protected double lon;

    static final public double LON_NA = -999;

    /**
     * Surface elevation. This column is the elevation of the surface of the
     * earth above the seismic station (<B>site</B>) relative to mean sea level
     * <p>
     * Units: km
     */
    protected double elev;

    static final public double ELEV_NA = -999;

    /**
     * Station name/Description. This value is the full name of the station
     * whose code name is in <I>sta</I> [for example, one record in the
     * <B>site</B> table connects <I>sta</I> = ANMO to staname = ALBUQUERQUE,
     * NEW MEXICO (SRO)].
     */
    protected String staname;

    static final public String STANAME_NA = "-";

    /**
     * Station type; character string specifies the station type. Recommended
     * entries are single station (ss) or array (ar).
     */
    protected String statype;

    static final public String STATYPE_NA = "-";

    /**
     * Reference station. This string specifies the reference station with
     * respect to which array members are located (see <I>deast</I>,
     * <I>dnorth</I>).
     */
    protected String refsta;

    static final public String REFSTA_NA = "-";

    /**
     * Distance North. This column gives the northing or relative position of
     * array element North of the array center specified by the value of
     * <I>refsta</I> (see <I>deast</I>).
     * <p>
     * Units: km
     */
    protected double dnorth;

    static final public double DNORTH_NA = 0.0;

    /**
     * Distance East. This column gives the easting or the relative position of
     * an array element East of the location of the array center specified by
     * the value of <I>refsta</I> (see <I>dnorth</I>).
     * <p>
     * Units: km
     */
    protected double deast;

    static final public double DEAST_NA = 0.0;

    private double[] unitVector = null;
    private double radius = -1;

    /**
     * Parameterized constructor. Populates all values with specified values.
     */
    public Site(String sta, long ondate, long offdate, double lat, double lon,
	    double elev, String staname, String statype, String refsta,
	    double dnorth, double deast) {
	this.sta = sta;
	this.ondate = ondate;
	this.offdate = offdate;
	this.lat = lat;
	this.lon = lon;
	this.elev = elev;
	this.staname = staname;
	this.statype = statype;
	this.refsta = refsta;
	this.dnorth = dnorth;
	this.deast = deast;
    }

    /**
     * Copy constructor.
     */
    public Site(Site other) {
	this.sta = other.getSta();
	this.ondate = other.getOndate();
	this.offdate = other.getOffdate();
	this.lat = other.getLat();
	this.lon = other.getLon();
	this.elev = other.getElev();
	this.staname = other.getStaname();
	this.statype = other.getStatype();
	this.refsta = other.getRefsta();
	this.dnorth = other.getDnorth();
	this.deast = other.getDeast();
    }

    /**
     * Default constructor that populates all values with na_values.
     */
    public Site() {
	this(STA_NA, ONDATE_NA, OFFDATE_NA, LAT_NA, LON_NA, ELEV_NA,
		STANAME_NA, STATYPE_NA, REFSTA_NA, DNORTH_NA, DEAST_NA);
    }

    /**
     * Constructor that loads values from a Scanner. It can read the output of
     * the toString() function.  Expects space-delimited tokens with double quote 
     * around staname.  For tab-delimited strings, see Site(String).
     */
    public Site(Scanner input) throws IOException {
	this(input.next(), input.nextLong(), input.nextLong(), input.nextDouble(), 
		input.nextDouble(), input.nextDouble(), 
		input.findInLine("\".*?\"").replaceAll("\"", "").trim(), 
		input.next(), input.next(), input.nextDouble(), input.nextDouble());
    }

    /**
     * Constructor that loads values from a DataInputStream.
     */
    public Site(DataInputStream input) throws IOException
    {
	this(Globals.readString(input), input.readLong(), input.readLong(), 
		input.readDouble(), input.readDouble(), input.readDouble(),
		Globals.readString(input), Globals.readString(input), 
		Globals.readString(input), input.readDouble(), input.readDouble());
    }

    /**
     * Parse string into a new Site object.
     * <ul>
     * <li>If string contains a tab character, it is parsed using tab as delimiter.  
     * <li>Otherwise it is parsed with white space.
     * <li>If parsing with white space and line contains two or more quotes, staname is 
     * assumed to be in quotes, otherwise staname is assumed to not contain any white space.
     * <li>Any quotes that enclose any tokens other than those enclosing staname are ignored.
     * </ul>
     * The string may contain fewer than 11 tokens:
     * <ul>
     * <li> if string contains only 4 tokens, they are assumed to be sta, lat, lon, elev
     * <li> otherwise, as many values as possible are parsed and all others are default values.
     * </ul>
     * @param string String to be parsed
     * @return a gov.sandia.gmp.util.globals.Site object
     * @throws Exception 
     */
    public Site(String line) throws Exception {
	this(SiteInterface.parseSite(line));
    }

    /**
     * Parameterized constructor. Populates all values with specified values.
     * Splits line on specified character.  Expects 11 tokens.
     * @throws Exception 
     */
    public Site(String line, String delimiter) throws Exception {
	this(line.split(delimiter));
    }

    /**
     * Parameterized constructor. Populates all values with specified values.
     * Parameter s must have 11 elements: sta, ondate, offdate, lat, lon, elev,
     * staname, statype, refsta, dnorth, deast
     * @throws Exception 
     */
    public Site(String[] s) throws Exception {
	this(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], s[10]);
    }

    /**
     * Parameterized constructor. Populates all values with specified values.
     * @throws Exception 
     */
    public Site(String sta, String ondate, String offdate, String lat, String lon,
	    String elev, String staname, String statype, String refsta,
	    String dnorth, String deast) throws Exception {
	try {
	    this.sta = sta.trim();
	    this.ondate = Long.parseLong(ondate.trim());
	    this.offdate = Long.parseLong(offdate.trim());
	    this.lat = Double.parseDouble(lat.trim());
	    this.lon = Double.parseDouble(lon.trim());
	    this.elev = Double.parseDouble(elev.trim());
	    this.staname = staname.trim();
	    this.statype = statype.trim();
	    this.refsta = refsta.trim();
	    this.dnorth = Double.parseDouble(dnorth.trim());
	    this.deast = Double.parseDouble(deast.trim());
	} catch (Exception e) {
	    throw new Exception(String.format("Unable to parse tokens:%n"
		    	+ "sta:     %s%n"
		    	+ "ondate:  %s%n"
		    	+ "offdate: %s%n"
		    	+ "lat:     %s%n"
		    	+ "lon:     %s%n"
		    	+ "elev:    %s%n"
		    	+ "staname: %s%n"
		    	+ "statype: %s%n"
		    	+ "refsta:  %s%n"
		    	+ "dnorth:  %s%n"
		    	+ "deast:   %s%n",
	    	sta, ondate, offdate, lat, lon, elev, staname, statype, refsta, dnorth, deast));
	}
    }

    /**
     * Write this row to a DataOutputStream.
     */
    public void write(DataOutputStream output) throws IOException {
	Globals.writeString(output, sta);
	output.writeLong(ondate);
	output.writeLong(offdate);
	output.writeDouble(lat);
	output.writeDouble(lon);
	output.writeDouble(elev);
	Globals.writeString(output, staname);
	Globals.writeString(output, statype);
	Globals.writeString(output, refsta);
	output.writeDouble(dnorth);
	output.writeDouble(deast);
    }

    /**
     * Write this row to an ascii String with no newline at the end. 
     * Space delimited with double quotes around staname.
     */
    @Override
    public String toString() {
	return String.format("%s %d %d %1.6f %1.6f %.4f \"%s\" %s %s %1.4f %1.4f",
		sta, ondate, offdate, lat, lon, elev, staname, statype,
		refsta, dnorth, deast);

    }

    /**
     * Write this row to a tab-delimited ascii String with no newline at the end. 
     */
    public String toStringTabs() {
	return String.format("%s\t%d\t%d\t%1.6f\t%1.6f\t%1.4f\t%s\t%s\t%s\t%1.4f\t%1.4f",
		sta, ondate, offdate, lat, lon, elev, staname, statype,
		refsta, dnorth, deast);

    }

    /**
     * Station code. This is the code name of a seismic observatory and
     * identifies a geographic location recorded in the <B>site</B> table.
     * 
     * @return sta
     */
    public String getSta() {
	return sta;
    }

    /**
     * Station code. This is the code name of a seismic observatory and
     * identifies a geographic location recorded in the <B>site</B> table.
     * 
     * @param sta
     * @return reference to this
     */
    public Site setSta(String sta) {
	this.sta = sta;
	return this;
    }

    /**
     * Turn on date. Date on which the station, or sensor indicated began
     * operating. The columns offdate and ondate are not intended to accommodate
     * temporary downtimes, but rather to indicate the time period for which the
     * columns of the station (<I>lat</I>, <I>lon</I>, <I>elev</I>,) are valid
     * for the given station code. Stations are often moved, but with the
     * station code remaining unchanged.
     * 
     * @return ondate
     */
    public long getOndate() {
	return ondate;
    }

    public double getOntime() {
	return GMTFormat.getEpochTime(ondate);
    }

    /**
     * Turn on date. Date on which the station, or sensor indicated began
     * operating. The columns offdate and ondate are not intended to accommodate
     * temporary downtimes, but rather to indicate the time period for which the
     * columns of the station (<I>lat</I>, <I>lon</I>, <I>elev</I>,) are valid
     * for the given station code. Stations are often moved, but with the
     * station code remaining unchanged.
     * 
     * @param ondate
     * @return reference to this
     */
    public Site setOndate(long ondate) {
	this.ondate = ondate;
	return this;
    }

    public Site setOntime(double epochtime) {
	this.ondate = GMTFormat.getJDate(epochtime);
	return this;
    }

    /**
     * Turn off date. This column is the Julian Date on which the station or
     * sensor indicated was turned off, dismantled, or moved (see <I>ondate</I>)
     * 
     * @return offdate
     */
    public long getOffdate() {
	return offdate;
    }

    public double getOfftime() {
	return GMTFormat.getOffTime(offdate);
    }

    /**
     * Turn off date. This column is the Julian Date on which the station or
     * sensor indicated was turned off, dismantled, or moved (see <I>ondate</I>)
     * 
     * @param offdate
     * @return reference to this
     */
    public Site setOffdate(long offdate) {
	this.offdate = offdate;
	return this;
    }

    public Site setOfftime(double offtime) {
	this.offdate = GMTFormat.getJDate(offtime);
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
     * @return reference to this
     */
    public Site setLat(double lat) {
	this.unitVector = null;
	this.radius = -1;
	this.lat = lat;
	return this;
    }

    /**
     * Geographic longitude. Longitudes are measured positive East of the
     * Greenwich meridian.
     * <p>
     * Units: degree
     * 
     * @return lon
     */
    public double getLon() {
	return lon;
    }

    /**
     * Geographic longitude. Longitudes are measured positive East of the
     * Greenwich meridian.
     * <p>
     * Units: degree
     * 
     * @param lon
     * @return reference to this
     */
    public Site setLon(double lon) {
	this.unitVector = null;
	this.radius = -1;
	this.lon = lon;
	return this;
    }

    /**
     * Surface elevation. This column is the elevation of the surface of the
     * earth above the seismic station (<B>site</B>) relative to mean sea level
     * <p>
     * Units: km
     * 
     * @return elev
     */
    public double getElev() {
	return elev;
    }

    /**
     * Surface elevation. This column is the elevation of the surface of the
     * earth above the seismic station (<B>site</B>) relative to mean sea level
     * <p>
     * Units: km
     * 
     * @param elev
     * @return reference to this
     */
    public Site setElev(double elev) {
	this.radius = -1.;
	this.elev = elev;
	return this;
    }

    /**
     * Station name/Description. This value is the full name of the station
     * whose code name is in <I>sta</I> [for example, one record in the
     * <B>site</B> table connects <I>sta</I> = ANMO to staname = ALBUQUERQUE,
     * NEW MEXICO (SRO)].
     * 
     * @return staname
     */
    public String getStaname() {
	return staname;
    }

    /**
     * Station name/Description. This value is the full name of the station
     * whose code name is in <I>sta</I> [for example, one record in the
     * <B>site</B> table connects <I>sta</I> = ANMO to staname = ALBUQUERQUE,
     * NEW MEXICO (SRO)].
     * 
     * @param staname
     * @return reference to this
     */
    public Site setStaname(String staname) {
	this.staname = staname;
	return this;
    }

    /**
     * Station type; character string specifies the station type. Recommended
     * entries are single station (ss) or array (ar).
     * 
     * @return statype
     */
    public String getStatype() {
	return statype;
    }

    /**
     * Station type; character string specifies the station type. Recommended
     * entries are single station (ss) or array (ar).
     * 
     * @param statype
     * @return reference to this
     */
    public Site setStatype(String statype) {
	this.statype = statype;
	return this;
    }

    /**
     * Reference station. This string specifies the reference station with
     * respect to which array members are located (see <I>deast</I>,
     * <I>dnorth</I>).
     * 
     * @return refsta
     */
    public String getRefsta() {
	return refsta;
    }

    /**
     * Reference station. This string specifies the reference station with
     * respect to which array members are located (see <I>deast</I>,
     * <I>dnorth</I>).
     * 
     * @param refsta
     * @return reference to this
     */
    public Site setRefsta(String refsta) {
	this.refsta = refsta;
	return this;
    }

    /**
     * Distance North. This column gives the northing or relative position of
     * array element North of the array center specified by the value of
     * <I>refsta</I> (see <I>deast</I>).
     * <p>
     * Units: km
     * 
     * @return dnorth
     */
    public double getDnorth() {
	return dnorth;
    }

    /**
     * Distance North. This column gives the northing or relative position of
     * array element North of the array center specified by the value of
     * <I>refsta</I> (see <I>deast</I>).
     * <p>
     * Units: km
     * 
     * @param dnorth
     * @return reference to this
     */
    public Site setDnorth(double dnorth) {
	this.dnorth = dnorth;
	return this;
    }

    /**
     * Distance East. This column gives the easting or the relative position of
     * an array element East of the location of the array center specified by
     * the value of <I>refsta</I> (see <I>dnorth</I>).
     * <p>
     * Units: km
     * 
     * @return deast
     */
    public double getDeast() {
	return deast;
    }

    /**
     * Distance East. This column gives the easting or the relative position of
     * an array element East of the location of the array center specified by
     * the value of <I>refsta</I> (see <I>dnorth</I>).
     * <p>
     * Units: km
     * 
     * @param deast
     * @return reference to this
     */
    public Site setDeast(double deast) {
	this.deast = deast;
	return this;
    }

    /**
     * Returns a unit vector of the site location.
     * 
     * @return A unit vector of the site location.
     */
    public double[] getUnitVector()
    {
	if (unitVector == null)
	    unitVector = VectorGeo.getVectorDegrees(lat, lon);
	return unitVector;
    }

    /**
     * Returns the site location radius (km).
     * 
     * @return The site location radius (km).
     */
    public double getRadius()
    {
	if (radius < 0.)
	    radius = VectorGeo.getEarthRadius(getUnitVector()) + elev;
	return radius;
    }

    @Override
    public int compareTo(SiteInterface o) {
	int x = sta.compareTo(o.getSta());
	if (x == 0)
	    x = (int) Math.signum(this.ondate-o.getOndate());
	return x;
    }

    @Override
    public boolean equals(Object o) {
	// all classes that implement SiteInterface must have identical definitions of equals() and hashCode()
	if (this == o) { return true; }
	if (o == null || !(o instanceof SiteInterface)) { return false; }
	return this.ondate == ((SiteInterface)o).getOndate() && this.sta.equals(((SiteInterface)o).getSta());
    }

    @Override
    public int hashCode() {
	// all classes that implement SiteInterface must have identical definitions of equals() and hashCode()
	return ((int)ondate) * sta.hashCode();
    }

}
