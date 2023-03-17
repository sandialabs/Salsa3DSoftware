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
package gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;

public class SiteExtended extends Site implements Comparable<SiteExtended> {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The unit vector that represents the geographic location of this Site
     */
    private double[] unitVector;

    /**
     * The distance from the center of the earth to the location of this Site, in km.
     */
    private double radius;

    /**
     * The radius of the earth at the latitude of this Site, in km.
     */
    private double earthRadius;

    /**
     * If this SiteExtended is an array, then arrayElements contains map from sta -> SiteExtended
     * objects for each element of the array. If not an array, then elements is empty. If null, then
     * it was never set.
     */
    private Map<String, SiteExtended> arrayElements;

    /**
     * Object containing array aperture info (aperture & spacing). Only applies when statype == 'ar'
     */
    private ArrayAperture arrayAperture;

    /**
     * All fields set to NA values. unitVector, radius and earthRadius are all set to NaN.
     */
    public SiteExtended() {
        super();
        unitVector = new double[]{Double.NaN, Double.NaN, Double.NaN};
        radius = earthRadius = Double.NaN;
        arrayAperture = null;
    }

    public SiteExtended(String sta, long ondate, long offdate, double lat, double lon, double elev,
                        String staname, String statype, String refsta, double dnorth, double deast) {
        super(sta, ondate, offdate, lat, lon, elev, staname, statype, refsta, dnorth, deast);
        initialize();
    }

    public SiteExtended(SiteExtended other) {
        super(other);
        this.unitVector = other.unitVector.clone();
        this.earthRadius = other.earthRadius;
        this.radius = other.radius;
        this.arrayAperture = null;
    }

    public SiteExtended(Site other) {
        super(other);
        initialize();
    }

    /**
     * Parameterized constructor. Populates all values with specified values.
     * Splits line on tab character.  Expects 11 tokens.
     * For space-delimited strings see Site(Scanner)
     * @throws IOException 
     */
    public SiteExtended(String line) throws IOException {
        super(line);
        initialize();
    }

    /**
     * Parameterized constructor. Populates all values with specified values.
     * Parameter s must have 11 elements: sta, ondate, offdate, lat, lon, elev,
     * staname, statype, refsta, dnorth, deast
     */
    public SiteExtended(String[] s) {
        super(s);
        initialize();
    }

    /**
     * Parameterized constructor. Populates all values with specified values.
     */
    public SiteExtended(String sta, String ondate, String offdate, String lat, String lon,
  	  String elev, String staname, String statype, String refsta,
  	  String dnorth, String deast) {
        super(sta, ondate, offdate, lat, lon, elev, staname, statype, refsta, dnorth, deast);
        initialize();
    }

    public SiteExtended(Scanner input) throws IOException {
        super(input);
        initialize();
    }

    public SiteExtended(DataInputStream input) throws IOException {
        super(input);
        initialize();
    }

    public SiteExtended(ByteBuffer input) {
        super(input);
        initialize();
    }

    public SiteExtended(ResultSet input) throws SQLException {
        super(input);
        initialize();
    }

    public SiteExtended(ResultSet input, int offset) throws SQLException {
        super(input, offset);
        initialize();
    }

    /**
     * return true if this Site was 'on' on the specified jdate.
     *
     * @param jdate
     * @return jdate >= ondate && (offdate == -1 || jdate <= offdate);
     */
    public boolean testJdate(long jdate) {
        return jdate >= getOndate() && (getOffdate() == -1 || jdate <= getOffdate());
    }

    private void initialize() {
        unitVector = VectorGeo.getVectorDegrees(getLat(), getLon());
        earthRadius = VectorGeo.getEarthRadius(unitVector);
        radius = earthRadius + getElev();
        arrayAperture = null;
    }

    /**
     * Change the lat, lon of the site.
     *
     * @param lat in degrees
     * @param lon in degrees
     * @return reference to this.
     */
    public SiteExtended setLatLon(double lat, double lon) {
        super.setLat(lat);
        super.setLon(lon);
        VectorGeo.getVectorDegrees(lat, lon, unitVector);
        earthRadius = VectorGeo.getEarthRadius(unitVector);
        radius = earthRadius + getElev();
        return this;
    }

    /**
     * Change the lat, lon, elev of the site.
     *
     * @param lat  in degrees
     * @param lon  in degrees
     * @param elev in km above surface of the ellipsoid.
     * @return reference to this.
     */
    public SiteExtended setLatLonElev(double lat, double lon, double elev) {
        super.setLat(lat);
        super.setLon(lon);
        super.setElev(elev);
        VectorGeo.getVectorDegrees(lat, lon, unitVector);
        earthRadius = VectorGeo.getEarthRadius(unitVector);
        radius = earthRadius + elev;
        return this;
    }

    /**
     * Change the latitude of the site in degrees. If lon is being changed as well, consider calling
     * setLatLon() which is more efficient.
     *
     * @param lat in degrees
     * @return reference to this
     */
    @Override
    public SiteExtended setLat(double lat) {
        super.setLat(lat);
        VectorGeo.getVectorDegrees(lat, getLon(), unitVector);
        earthRadius = VectorGeo.getEarthRadius(unitVector);
        radius = earthRadius + getElev();
        return this;
    }

    /**
     * Change the longitude of the site in degrees. If lat is being changed as well, consider calling
     * setLatLon() which is more efficient.
     *
     * @param lat in degrees
     * @return reference to this
     */
    @Override
    public SiteExtended setLon(double lon) {
        super.setLon(lon);
        VectorGeo.getVectorDegrees(getLat(), lon, unitVector);
        return this;
    }

    /**
     * Change the elevation of the site in km.
     *
     * @param elev elevation in km above surface of the ellipsoid.
     * @return reference to this.
     */
    @Override
    public SiteExtended setElev(double elev) {
        super.setElev(elev);
        radius = earthRadius + elev;
        return this;
    }

    /**
     * Retrieve a reference to the unit vector that represents the geographic location of this Site.
     *
     * @return a reference to the unit vector that represents the geographic location of this Site.
     */
    public double[] getUnitVector() {
        return unitVector;
    }

    /**
     * Get the radius of the site in km relative to center of the earth.
     *
     * @return the radius of the site in km relative to center of the earth.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Get the radius of the surface of the ellipsoid at the current latitude, in km.
     *
     * @return the radius of the surface of the ellipsoid at the current latitude, in km.
     */
    public double getEarthRadius() {
        return earthRadius;
    }

    public void setArrayAperture(ArrayAperture aa) {
        this.arrayAperture = aa;
    }

    public ArrayAperture getArrayAperture() {
        return this.arrayAperture;
    }

    /**
     * Find the largest distance between any pair of sites, in degrees.
     *
     * @param sites
     * @return
     */
    static public double computeApertureDegrees(Collection<SiteExtended> sites) {
        return Math.toDegrees(computeAperture(sites));
    }

    /**
     * Find the largest distance between any pair of sites, in radians.
     *
     * @param sites
     * @return
     */
    static public double computeAperture(Collection<SiteExtended> sites) {
        double delta = 0;
        ArrayList<SiteExtended> siteList = new ArrayList<>(sites);
        for (int i = 0; i < siteList.size() - 1; ++i) {
            double[] sitei = siteList.get(i).unitVector;
            for (int j = i + 1; j < siteList.size(); ++j) {
                double d = VectorGeo.angle(sitei, siteList.get(j).unitVector);
                if (d > delta)
                    delta = d;
            }
        }
        return delta;
    }

    /**
     * Returns true if this Site is an array.
     *
     * @return statype.equals(" ar ")
     */
    public boolean isArray() {
        return getStatype().equals("ar");
    }

    static private Comparator<Site> sortByStaOndate = new Comparator<Site>() {
        @Override
        public int compare(Site o1, Site o2) {
            if (o1.equals(o2))
                return 0;
            int order = o1.getSta().compareTo(o2.getSta());
            if (order == 0)
                order = (int) Math.signum(o1.getOndate() - o2.getOndate());
            return order >= 0 ? 1 : -1;
        }
    };

    public static void sortByStaOndate(ArrayList<? extends Site> output) {
        Collections.sort(output, sortByStaOndate);
    }

    public String toString(boolean printHeader) {
        StringBuffer out = new StringBuffer();
        if (printHeader)
            out.append(NL).append(toStringHeader()).append(NL);

        out.append(String.format("%6s %6s %4s %7d %7d %11.6f %12.6f %8.3f %8.3f %8.3f %-50s", getSta(),
                getRefsta(), getStatype(), getOndate(), getOffdate(), getLat(), getLon(), getElev(),
                getDnorth(), getDeast(), getStaname()));
        return out.toString();
    }

    public static String toStringHeader() {
        return String.format("%6s %6s %4s %7s %7s %11s %12s %8s %8s %8s %s", "sta", "refsta", "type",
                "ondate", "offdate", "lat", "lon", "elev", "dnorth", "deast", "staname");
    }

    @Override
    public int compareTo(SiteExtended o) {
        int order = getSta().compareTo(o.getSta());
        if (order == 0)
            order = (int) Math.signum(getOndate() - o.getOndate());
        return order;
    }

    /**
     * If this SiteExtended object is an array, then this method returns a map from sta to
     * arrayElement with an entry for each element of the array.
     *
     * @return
     */
    public Map<String, SiteExtended> getArrayElements() {
        if (arrayElements == null)
            arrayElements = new LinkedHashMap<>();
        return arrayElements;
    }

    /**
     * If this SiteExtended object is an array, then specify a map from sta to arrayElement with an
     * entry for each element of the array.
     *
     * @return reference to this.
     */
    public SiteExtended setArrayElements(Map<String, SiteExtended> elements) {
        this.arrayElements = elements;
        return this;
    }

    /**
     * If this SiteExtended object is an array, then add an array element to the map from sta to
     * arrayElement with an entry for each element of the array.
     *
     * @return reference to this.
     */
    public SiteExtended addArrayElement(SiteExtended arrayElement) {
        if (arrayElements == null)
            arrayElements = new LinkedHashMap<>();
        arrayElements.put(arrayElement.getSta(), arrayElement);
        return this;
    }

    /**
     * If this SiteExtended object is an array, then add an array element to the map from sta to
     * arrayElement with an entry for each element of the array.
     *
     * @return reference to this.
     */
    public SiteExtended addArrayElements(Collection<SiteExtended> elements) {
        if (arrayElements == null)
            arrayElements = new LinkedHashMap<>(elements.size());
        for (SiteExtended element : elements)
            arrayElements.put(element.getSta(), element);
        return this;
    }

    /**
     * Use dnorth and deast to compute the azimuth from refsta to this element of the array, in
     * radians.
     *
     * @return azimuth from refsta to this element of the array, in radians.
     */
    public double getDazimuth() {
        return Math.atan2(getDnorth(), getDeast());
    }

    /**
     * Use dnorth and deast to compute the distance from refsta to this element of the array, in km.
     *
     * @return distance from refsta to this element of the array, in km.
     */
    public double getDdistanceKm() {
        return Math.sqrt(getDnorth() * getDnorth() + getDeast() * getDeast());
    }

    /**
     * Use dnorth and deast to compute the distance from refsta to this element of the array, in
     * radians.
     *
     * @return distance from refsta to this element of the array, in radians.
     */
    public double getDdistance() {
        return getDdistanceKm() / radius;
    }

    /**
     * If this is an array, then use dnorth and deast of all the elements of the array to compute the
     * max distance from refsta to any element of this array, in km.
     *
     * @return max distance from refsta to any element of this array, in km.
     */
    public double getMaxDdistanceKm() {
        if (arrayElements == null)
            return Double.NaN;
        double d = 0;
        for (SiteExtended element : arrayElements.values())
            d = Math.max(d, element.getDdistanceKm());
        return d;
    }

    /**
     * If this is an array, then use dnorth and deast of all the elements of the array to compute the
     * max distance from refsta to any element of this array, in radians.
     *
     * @return max distance from refsta to any element of this array, in radians.
     */
    public double getMaxDdistance() {
        if (arrayElements == null)
            return Double.NaN;
        double d = 0;
        for (SiteExtended element : arrayElements.values())
            d = Math.max(d, element.getDdistance());
        return d;
    }

    static public Buff getBuff(Scanner input) {
	return new Buff(input);
    }
    
}
