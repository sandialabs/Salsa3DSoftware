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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gmp.util.time.TimeInterface;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;

public class ArrivalExtended extends Arrival implements TimeInterface {

    private static final long serialVersionUID = 1L;

    private SiteExtended site;

    static public Comparator<Arrival> sortByArid = new Comparator<Arrival>() {
        @Override
        public int compare(Arrival o1, Arrival o2) {
            if (o1.equals(o2))
                return 0;
            int order = (int) Math.signum(o1.getArid() - o2.getArid());
            return order >= 0 ? 1 : -1;
        }
    };

    static public Comparator<Arrival> sortByStaTime = new Comparator<Arrival>() {
        @Override
        public int compare(Arrival o1, Arrival o2) {
            if (o1.equals(o2))
                return 0;

            int order = o1.getSta().compareTo(o2.getSta());
            if (order == 0)
                order = (int) Math.signum(o1.getTime() - o2.getTime());
            return order >= 0 ? 1 : -1;
        }
    };

    static public Comparator<Arrival> sortByTimeSta = new Comparator<Arrival>() {
        @Override
        public int compare(Arrival o1, Arrival o2) {
            if (o1.equals(o2))
                return 0;
            int order = (int) Math.signum(o1.getTime() - o2.getTime());
            if (order == 0)
                order = o1.getSta().compareTo(o2.getSta());
            return order >= 0 ? 1 : -1;
        }
    };


    static public Comparator<Arrival> sortByTime = new Comparator<Arrival>() {
        @Override
        public int compare(Arrival o1, Arrival o2) {
            if (o1.equals(o2))
                return 0;
            int order = (int) Math.signum(o1.getTime() - o2.getTime());
            return order >= 0 ? 1 : -1;
        }
    };


    public ArrivalExtended(String sta, double time, long arid, long jdate, long stassid, long chanid,
                           String chan, String iphase, String stype, double deltim, double azimuth, double delaz,
                           double slow, double delslo, double ema, double rect, double amp, double per, double logat,
                           String clip, String fm, double snr, String qual, String auth, long commid) {
        super(sta, time, arid, jdate, stassid, chanid, chan, iphase, stype, deltim, azimuth, delaz,
                slow, delslo, ema, rect, amp, per, logat, clip, fm, snr, qual, auth, commid);
    }

    private double deltimSqr;
    private double delsloSqr;
    private double delazSqr;

    public ArrivalExtended() {
        super();
        deltimSqr = delsloSqr = delazSqr = Double.NaN;
    }

    public ArrivalExtended(ArrivalExtended other) {
        super(other);
        this.deltimSqr = Globals.sqr(getDeltim());
        this.delazSqr = Globals.sqr(getDelaz());
        this.delsloSqr = Globals.sqr(getDelslo());
        this.site = other.site;
    }

    public ArrivalExtended(Arrival other, NetworkExtended network) {
        super(other);
        this.deltimSqr = Globals.sqr(getDeltim());
        this.delazSqr = Globals.sqr(getDelaz());
        this.delsloSqr = Globals.sqr(getDelslo());
        site = network.getSite(getSta(), getJdate());
    }

    public ArrivalExtended(Arrival other) {
        super(other);
        this.deltimSqr = Globals.sqr(getDeltim());
        this.delazSqr = Globals.sqr(getDelaz());
        this.delsloSqr = Globals.sqr(getDelslo());
    }

    public ArrivalExtended(Scanner input) throws IOException {
        super(input);
        this.deltimSqr = Globals.sqr(getDeltim());
        this.delazSqr = Globals.sqr(getDelaz());
        this.delsloSqr = Globals.sqr(getDelslo());
    }

    public ArrivalExtended(DataInputStream input) throws IOException {
        super(input);
        this.deltimSqr = Globals.sqr(getDeltim());
        this.delazSqr = Globals.sqr(getDelaz());
        this.delsloSqr = Globals.sqr(getDelslo());
    }

    public ArrivalExtended(ByteBuffer input) {
        super(input);
        this.deltimSqr = Globals.sqr(getDeltim());
        this.delazSqr = Globals.sqr(getDelaz());
        this.delsloSqr = Globals.sqr(getDelslo());
    }

    public ArrivalExtended(ResultSet input) throws SQLException {
        super(input);
        this.deltimSqr = Globals.sqr(getDeltim());
        this.delazSqr = Globals.sqr(getDelaz());
        this.delsloSqr = Globals.sqr(getDelslo());
    }

    public ArrivalExtended(ResultSet input, int offset) throws SQLException {
        super(input, offset);
        this.deltimSqr = Globals.sqr(getDeltim());
        this.delazSqr = Globals.sqr(getDelaz());
        this.delsloSqr = Globals.sqr(getDelslo());
    }

    @Override
    public Arrival setAzimuth(double azimuth) {
        return super.setAzimuth(conditionAzDegrees(azimuth, Arrival.AZIMUTH_NA, 2));
    }

    @Override
    public ArrivalExtended setDeltim(double deltim) {
        super.setDeltim(deltim);
        this.deltimSqr = deltim * deltim;
        return this;
    }

    @Override
    public ArrivalExtended setDelaz(double delaz) {
        super.setDelaz(delaz);
        this.delazSqr = delaz * delaz;
        return this;
    }

    @Override
    public ArrivalExtended setDelslo(double delslo) {
        super.setDelslo(delslo);
        this.delsloSqr = delslo * delslo;
        return this;
    }

    public double getDeltimSqr() {
        return deltimSqr;
    }

    public double getDelazSqr() {
        return delazSqr;
    }

    public ArrivalExtended setDelazSqr(double delazSqr) {
        this.delazSqr = delazSqr;
        return this;
    }

    public double getDelsloSqr() {
        return delsloSqr;
    }

    public ArrivalExtended setDelsloSqr(double delslowSqr) {
        this.delsloSqr = delslowSqr;
        return this;
    }

    /**
     * Return this.azimuth - other.azimuth (degrees) but rotated as necessary to ensure that the
     * result is between -180 inclusive and 180 exclusive.
     *
     * @param other
     * @return difference in azimuth in degrees.
     */
    public double getAzDiff(Arrival other) {
        return getAzDiff(this.getAzimuth(), other.getAzimuth());
    }

    /**
     * Compute the azimuth residual given a predicted azimuth.
     *
     * @param predictedAzimuth predicted azimuth in degrees. Range must be 0 to 360.
     * @return observed azimuth - predicted azimuth in degrees, rotated such that residual is in range
     * -180 to 180.
     */
    public double getAzres(double predictedAzimuth) {
        double daz = getAzimuth() - predictedAzimuth;
        return daz < -180. ? daz + 360. : daz >= 180. ? daz - 360. : daz;
    }

    /**
     * Return az1 - az2 (degrees) but rotated as necessary to ensure that the result is between -180
     * inclusive and 180 exclusive.
     *
     * @param az1
     * @param az2
     * @return az1 - az2, in degrees.
     */
    static public double getAzDiff(double az1, double az2) {
        double daz = az1 - az2;
        if (daz >= 180.)
            return daz - 360.;
        if (daz < -180.)
            return daz + 360.;
        return daz;
    }

    public SiteExtended getSite() {
        return site;
    }

    public ArrivalExtended setSite(SiteExtended site) {
        this.site = site;
        //super.setHash(null);
        return this;
    }

    public ArrivalExtended setSite(NetworkExtended network) {
        setSite(network.getSite(getSta(), getJdate()));
        return this;
    }

//  @Override
//  public BigInteger getHash() {
//    if (super.isHashNull()) {
//      super.getHash();
//      if (site != null)
//        incrementHash(site.getHash());
//    }
//    return super.getHash();
//  }

    /**
     * Change the arrival time in seconds. The jdate is also updated with the appropriate value.
     *
     * @param arrivalTime arrival time in seconds.
     * @return reference to this.
     */
    @Override
    public ArrivalExtended setTime(double arrivalTime) {
        super.setTime(arrivalTime);
        super.setJdate(GMTFormat.getJDate(arrivalTime));
        return this;
    }

    /**
     * Iterate through all the Sites attached to the arrivals and add them to a Network. If two
     * arrivals reference different instances of Site which are in fact equal (same sta-ondate) then
     * the duplicate references are replaced with references to the unique reference.
     *
     * @param arrivals
     * @return Network populated with all the unique sites.
     */
    public static NetworkExtended getNetwork(Collection<ArrivalExtended> arrivals) {
        NetworkExtended network = new NetworkExtended();
        for (ArrivalExtended a : arrivals)
            if (a.getSite() != null) {
                SiteExtended site = network.getSite(a.getSite().getSta(), a.getSite().getOndate());
                if (site == null)
                    network.add(site);
                else
                    a.setSite(site);
            }
        return network;
    }

    /**
     * Load ArrivalExtended objects and descendant SiteExtended objects from a database.
     *
     * @param schema
     * @param network     3 possibilities:
     *                    <ul>
     *                    <li>if network is populated with sites, then sites from the network will be attached to
     *                    arrivals and any site table specified in tableNames will be ignored.
     *                    <li>if network is null, and a site table is specified in tableNames, then sites from the
     *                    input table will be attached to arrivals.
     *                    <li>if network is empty, and a site table is specified in inputTables then network will
     *                    be populated with sites from the site table and those sites will be attached to
     *                    arrivals.
     *                    </ul>
     * @param whereClause a where clause to be executed against an arrival table. Eg., 'arid in
     *                    (98127, 789009)'
     * @param executedSQL (output; ignored if null) the sql commands that are actually executed by
     *                    this method.
     * @return HashSet of ArrivalExtended object with requested descendants attached.
     * @throws Exception
     */
    static public HashSet<ArrivalExtended> readArrivalExtended(Schema schema, NetworkExtended network,
                                                               String whereClause, ArrayList<String> executedSQL) throws Exception {
        HashSet<ArrivalExtended> arrivals = new HashSet<ArrivalExtended>();
        readArrivalExtended(schema, network, whereClause, executedSQL, arrivals);
        return arrivals;
    }

    /**
     * Load ArrivalExtended objects and descendant SiteExtended objects from a database.
     *
     * @param schema
     * @param network     3 possibilities:
     *                    <ul>
     *                    <li>if network is populated with sites, then sites from the network will be attached to
     *                    arrivals and any site table specified in tableNames will be ignored.
     *                    <li>if network is null, and a site table is specified in tableNames, then sites from the
     *                    input table will be attached to arrivals.
     *                    <li>if network is empty, and a site table is specified in inputTables then network will
     *                    be populated with sites from the site table and those sites will be attached to
     *                    arrivals.
     *                    </ul>
     * @param whereClause a where clause to be executed against an arrival table. Eg., 'arid in
     *                    (98127, 789009)'
     * @param executedSQL (output; ignored if null) the sql commands that are actually executed by
     *                    this method.
     * @param arrivals    Collection of ArrivalExtended object to which new ArrivalExtended objects will
     *                    be added. This Collection is not cleared before addition of new objects.
     * @throws Exception
     */
    static public void readArrivalExtended(Schema schema, NetworkExtended network, String whereClause,
                                           ArrayList<String> executedSQL, Collection<ArrivalExtended> arrivals) throws Exception {
        String arrivalTable = schema.getTableName("arrival");
        String siteTable = schema.getTableName("site");

        String arrivalSql;
        if (whereClause != null && !whereClause.isEmpty())
            arrivalSql =
                    whereClause.startsWith("where ") ? String.format("%s %s", arrivalTable, whereClause)
                            : String.format("%s where %s", arrivalTable, whereClause);
        else
            arrivalSql = arrivalTable;

        if (executedSQL != null)
            executedSQL.add("select * from " + arrivalSql);


        ResultSet rs = null;
        Statement statement = schema.getConnection().createStatement();

        // set of station names actually associated with an arrival.
        HashSet<String> stations = new HashSet<String>();

        try {
            rs = statement.executeQuery("select * from " + arrivalSql);
        } catch (SQLException ex) {
            throw new SQLException(
                    String.format("%s%n%s%n%s %s %s", ex.getMessage(), ("select * from " + arrivalSql),
                            schema.getInstance(), schema.getUserName(), schema.getTableName("assoc")));
        }

        while (rs.next()) {
            ArrivalExtended arrival = new ArrivalExtended(rs);
            stations.add(arrival.getSta());
            arrivals.add(arrival);
        }
        rs.close();


        // If the input network was null, instantiate a new one.
        // If the network is empty, then populate it by querying the
        // site table for sites specified in arrivals.
        if (network == null)
            network = new NetworkExtended();
        if (network.size() == 0 && siteTable != null && siteTable.trim().length() > 0
                && stations.size() > 0) {
            ArrayList<String> sta = new ArrayList<String>(stations);
            int index = 0;
            while (index < sta.size()) {
                StringBuffer buf = new StringBuffer();
                int count = 0;
                while (index < sta.size()) {
                    buf.append(String.format(",'%s'", sta.get(index++)));
                    if (++count == 1000)
                        break;
                }
                String siteSql = String.format("select * from %s where sta in (%s)", siteTable,
                        buf.toString().substring(1));
                if (executedSQL != null)
                    executedSQL.add(siteSql);

                try {
                    rs = statement.executeQuery(siteSql);
                } catch (SQLException ex) {
                    throw new SQLException(String.format("%s%n%s%n%s %s %s", ex.getMessage(), siteSql,
                            schema.getInstance(), schema.getUserName(), schema.getTableName("assoc")));
                }

                while (rs.next())
                    network.add(new SiteExtended(rs));
                rs.close();
            }
        }
        statement.close();

        // attach sites to arrivals.
        for (ArrivalExtended arrival : arrivals)
            arrival.setSite(network);
    }


    /**
     * Read a Set of Arrival objects from an ascii file.
     *
     * <p>
     * Note that all Strings must be enclosed in double quotes.
     *
     * @param inputFile
     * @return a Set of Arrival objects
     * @throws IOException
     */
    static public Set<ArrivalExtended> readArrivalExtendeds(File inputFile) throws IOException {
        Set<ArrivalExtended> rows = new HashSet<ArrivalExtended>();
        readArrivalExtendeds(inputFile, rows);
        return rows;
    }

    /**
     * Read a Set of Arrival objects from an ascii file. Records that begin with '#' are ignored.
     *
     * @param inputFile
     * @param rows      a Set of Arrival objects.
     * @throws IOException
     */
    static public void readArrivalExtendeds(File inputFile, Collection<ArrivalExtended> rows)
            throws IOException {
        if (inputFile.exists()) {
            BufferedReader input = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#"))
                    rows.add(new ArrivalExtended(new Scanner(line)));
            }
            input.close();
        }
    }

    public static void sort(List<? extends ArrivalExtended> arrivals,
                            Comparator<Arrival> comparator) {
        Collections.sort(arrivals, comparator);
    }

    /**
     * Sort the supplied List of Arrivals by arid
     *
     * @param arrivals
     */
    static public void sortByArid(List<? extends Arrival> arrivals) {
        Collections.sort(arrivals, sortByArid);
    }

    /**
     * Sort the supplied List of Arrivals by sta, time
     *
     * @param arrivals
     */
    static public void sortByStaTime(List<? extends Arrival> arrivals) {
        Collections.sort(arrivals, sortByStaTime);
    }

    /**
     * Sort the supplied List of Arrivals by time.
     *
     * @param arrivals
     */
    static public void sortByTime(List<? extends Arrival> arrivals) {
        Collections.sort(arrivals, sortByTime);
    }

    /**
     * Sort the supplied List of Arrivals by time, sta
     *
     * @param arrivals
     */
    static public void sortByTimeSta(List<? extends Arrival> arrivals) {
        Collections.sort(arrivals, sortByTimeSta);
    }

    /**
     * Returns true if this Arrival comes from a Site that is an array.
     *
     * @return site.statype.equals(" ar ")
     */
    public boolean isArray() {
        return site == null ? false : site.isArray();
    }

    /**
     * Return the arrival commonality score of this and another Arrival. Value will be between zero
     * and one, with 1 implying the arrivals are identical. Similarity computed based on time, azimuth
     * and slowness. Two arrivals with same arid are assumed to be identical (similarity = 1). Two
     * arrivals from different stations are assumed to be completely different (similarity = 0).
     *
     * @param compareTimeOnly - whether or not to compute similarity using just time.
     * @return similarity, a number between 0 and 1.
     */
    public double getSimilarity(ArrivalExtended other, boolean compareTimeOnly) {
        if (this.getArid() != -1 && this.getArid() == other.getArid())
            return 1.;
        if (!this.getSta().equals(other.getSta()))
            return 0;
        double dt = Globals.sqr(getTime() - other.getTime()) / (deltimSqr + other.deltimSqr);
        if (compareTimeOnly) {
            return Math.exp(-dt);
        }
        double daz = Globals.sqr(getAzDiff(other)) / (delazSqr + other.delazSqr);
        double dslo = Globals.sqr(getSlow() - other.getSlow()) / (delsloSqr + other.delsloSqr);
        return Math.exp(-(dt + daz + dslo) / 3);
    }

    /**
     * Return the arrival commonality score of this and another Arrival. Value will be between zero
     * and one, with 1 implying the arrivals are identical. Similarity computed based on time, azimuth
     * and slowness. Two arrivals with same arid are assumed to be identical (similarity = 1). Two
     * arrivals from different stations are assumed to be completely different (similarity = 0).
     *
     * @return similarity, a number between 0 and 1.
     */
    public double getSimilarity(ArrivalExtended other) {
        return getSimilarity(other, false);
    }

    /**
     * Compute the similarity score and return true if it is greater than or equal to 0.01. Similarity
     * is a number between zero and one, with 1 implying the arrivals are identical. Similarity is
     * computed based on time, azimuth and slowness. Two arrivals with same arid are assumed to be
     * identical (similarity = 1). Two arrivals from different stations are assumed to be completely
     * different (similarity = 0).
     *
     * @return similarity >= 0.01.
     */
    public boolean isSimilar(ArrivalExtended other) {
        return getSimilarity(other) >= 0.01;
    }

    public TreeMap<Double, ArrivalExtended> sortBySimilarity(
            Collection<? extends ArrivalExtended> arrivals) {
        return sortBySimilarity(arrivals, false);
    }

    public TreeMap<Double, ArrivalExtended> sortBySimilarity(
            Collection<? extends ArrivalExtended> arrivals, boolean compareTimeOnly) {
        // instantiate a TreeMap<Similarity, Arrivals> that will sort the arrivals in order of
        // decreasing
        // similarity.
        TreeMap<Double, ArrivalExtended> sortedArrivals =
                new TreeMap<Double, ArrivalExtended>(new Comparator<Double>() {
                    @Override
                    public int compare(Double s1, Double s2) {
                        return (int) Math.signum(s2 - s1);
                    }
                });
        for (ArrivalExtended a : arrivals)
            sortedArrivals.put(getSimilarity(a, compareTimeOnly), a);
        return sortedArrivals;
    }

    public static String toStringHeader() {
        return String.format("%12s %6s %8s %8s %7s %15s %6s %7s %7s %7s %7s %11s %7s %7s %10s", "arid",
                "sta", "chan", "iphase", "jdate", "time", "deltim", "azimuth", "delaz", "slow", "delslo",
                "amp", "per", "logat", "snr");
    }

    public String toStringCustom() {
        return String.format(
                "%12d %6s %8s %8s %7d %15.3f %6.3f %7.2f %7.2f %7.2f %7.2f %11.2f %7.2f %7.2f %10.2f",
                getArid(), getSta(), getChan(), getIphase(), getJdate(), getTime(), getDeltim(),
                getAzimuth(), getDelaz(), getSlow(), getDelslo(), getAmp(), getPer(), getLogat(), getSnr());
    }

    public String toString(boolean... includeChildren) {
        boolean printSite = includeChildren.length > 0 && includeChildren[0];

        StringBuffer out = new StringBuffer();

        if (printSite)
            out.append(NL).append(toStringHeader()).append(NL);

        out.append(toStringCustom()).append(NL);


        if (printSite) {
            out.append(getSite() == null ? "null" : getSite().toString(true));
        }

        return out.toString();
    }

    /**
     * If az is NaN or == na, return na. Otherwise, ensure that az is >= 0. and < 360.
     * Also ensure that az only has precision significant digits, e.g.,
     * if precision == 3, then if az = 180.123456 will be returned as 180.123000.
     *
     * @param az
     * @param na
     * @param precision
     * @return
     */
    static public double conditionAzDegrees(double az, double na, int precision) {
        double x = Math.pow(10., precision);
        return Double.isNaN(az) || az == na ? na : Math.round(((az + 4 * 360.) % 360.) * x) / x;
    }

    public Buff getBuff() {
	      Buff buffer = new Buff(this.getClass().getSimpleName());
	      buffer.insert(super.getBuff());
	      
	      buffer.add("nSites", site == null ? 0 : 1);
	      if (site != null) buffer.add(site.getBuff());

	      return buffer;
	  }

    static public Buff getBuff(Scanner input) {
	Buff buf = new Buff(input);
	for (int i=0; i<buf.getInt("nSites"); ++i)
	    buf.add(SiteExtended.getBuff(input));
	return buf;
	
    }
    
}
