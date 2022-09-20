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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Network;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;

/**
 * Manages a Network of Sites. Has all the fields of a normal nnsa_kb_core.Network but adds a
 * Map<String, Set<SiteExtended>> which maps site.sta to a set of Sites that have different on-off
 * dates, sorted in order of decreasing ondate (i.e., the first site in the set is the site with the
 * most recent ondate).
 *
 * @author sballar
 */
public class NetworkExtended extends Network {

    private static final long serialVersionUID = 1L;

    /**
     * Map from sta to a Set of SiteExtended objects that all have that sta and different on-off
     * dates. The sites in each TreeSet are held in order of decreasing ondate (the most recent Site
     * is listed first).
     */
    private Map<String, Set<SiteExtended>> networkSites = new TreeMap<String, Set<SiteExtended>>();

    /**
     * Construct and populate a network of SiteExtended objects from the database.
     * <p>
     * Example input for the IMS network is <br>
     * Network network = new NetworkExtended(dbConnection, <br>
     * "idcstatic.site", "idcstatic.affiliation", "idcstatic.network", "SEISMIC");
     *
     * <ul>
     * Some useful networks at the IDC:
     * <li>AA_AUX auxiliary seismic stations used in automated association
     * <li>AA_HYD all hydroacoustic stations used in automated association
     * <li>AA_IDC all stations used in automated association
     * <li>AA_INF all infrasound stations used in automated association
     * <li>AA_PRI primary seismic stations used in automated association
     * <li>ACOUSTIC all hydroacoustic and infrasonic stations ever contributing to the IDC
     * <li>AUX all auxiliary seismic stations ever contributing to the IDC
     * <li>CUR_AUX auxiliary seismic stations currently in use at the IDC
     * <li>CUR_HYD hydroacoustic stations currently in use at the IDC
     * <li>CUR_IDC all stations currently in use at the IDC
     * <li>CUR_INF infrasonic stations currently in use at the IDC
     * <li>CUR_PRI primary seismic stations currently in use at the IDC
     * <li>EVCH primary and auxiliary stations used for event characterization
     * <li>HYDRO Hydroacoustic Network
     * <li>IDC PRI, AUX, HYDRO and INFRA
     * <li>IMS_AUX IMS auxiliary seismic stations
     * <li>IMS_HYD IMS hydroacoustic stations
     * <li>IMS_IDC all IMS stations
     * <li>IMS_INF IMS infrasonic stations
     * <li>IMS_PRI IMS primary seismic stations
     * <li>INFRA Infrasonic Network
     * <li>PRI all primary seismic stations ever contributing to the IDC
     * <li>REQUEST auxiliary seismic stations currently in use at IDC and accepting requests
     * <li>SEISMIC all primary and auxiliary seismic stations ever contributing to the IDC
     * </ul>
     *
     * @param dbConnection
     * @param siteTable
     * @param affiliationTable
     * @param networkTable       (can be null)
     * @param networkName
     * @param arrayApertureTable (can be null)
     * @throws SQLException
     */
    public NetworkExtended(Connection dbConnection, String siteTable, String affiliationTable,
                           String networkTable, String networkName, String arrayApertureTable) throws SQLException {
        super();

        Statement statement = dbConnection.createStatement();
        ResultSet rs = null;

        if (networkTable != null) {
            rs = statement.executeQuery(
                    String.format("select * from %s where net='%s'", networkTable, networkName));

            if (rs.next()) {
                setNet(rs.getString(1));
                setNetname(rs.getString(2));
                setNettype(rs.getString(3));
                setAuth(rs.getString(4) == null ? "-" : rs.getString(4));
                setCommid(rs.getLong(5));
            }
            rs.close();
        } else
            setNet(networkName);

        String sql = String.format(
                "select * from %s where sta in " + "(select sta from %s where net='%s') order by sta",
                siteTable, affiliationTable, networkName);

        try {
            rs = statement.executeQuery(sql);
        } catch (SQLException ex) {
            throw new SQLException(ex.getMessage() + sql);
        }

        while (rs.next())
            add(new SiteExtended(rs));
        rs.close();
        statement.close();

        // Read in ArrayApertures and set to appropriate sites
        if (arrayApertureTable != null) {
            HashSet<ArrayAperture> arrayApertures = new HashSet<ArrayAperture>();
            ArrayAperture.readArrayApertures(dbConnection, arrayApertureTable, arrayApertures);
            for (ArrayAperture aa : arrayApertures) {
                Set<SiteExtended> sites = networkSites.get(aa.getSta());
                if (sites != null) {
                    for (SiteExtended site : sites) {
                        site.setArrayAperture(aa);
                    }
                }
            }
        }
    }

    /**
     * Construct and populate a network of SiteExtended objects from the database.
     * <p>
     * Example input for the IMS network is <br>
     * Network network = new NetworkExtended(dbConnection, <br>
     * "idcstatic.site", "idcstatic.affiliation", "idcstatic.network", "SEISMIC");
     *
     * <ul>
     * Some useful networks at the IDC:
     * <li>AA_AUX auxiliary seismic stations used in automated association
     * <li>AA_HYD all hydroacoustic stations used in automated association
     * <li>AA_IDC all stations used in automated association
     * <li>AA_INF all infrasound stations used in automated association
     * <li>AA_PRI primary seismic stations used in automated association
     * <li>ACOUSTIC all hydroacoustic and infrasonic stations ever contributing to the IDC
     * <li>AUX all auxiliary seismic stations ever contributing to the IDC
     * <li>CUR_AUX auxiliary seismic stations currently in use at the IDC
     * <li>CUR_HYD hydroacoustic stations currently in use at the IDC
     * <li>CUR_IDC all stations currently in use at the IDC
     * <li>CUR_INF infrasonic stations currently in use at the IDC
     * <li>CUR_PRI primary seismic stations currently in use at the IDC
     * <li>EVCH primary and auxiliary stations used for event characterization
     * <li>HYDRO Hydroacoustic Network
     * <li>IDC PRI, AUX, HYDRO and INFRA
     * <li>IMS_AUX IMS auxiliary seismic stations
     * <li>IMS_HYD IMS hydroacoustic stations
     * <li>IMS_IDC all IMS stations
     * <li>IMS_INF IMS infrasonic stations
     * <li>IMS_PRI IMS primary seismic stations
     * <li>INFRA Infrasonic Network
     * <li>PRI all primary seismic stations ever contributing to the IDC
     * <li>REQUEST auxiliary seismic stations currently in use at IDC and accepting requests
     * <li>SEISMIC all primary and auxiliary seismic stations ever contributing to the IDC
     * </ul>
     *
     * @param dbConnection
     * @param siteTable
     * @param affiliationTable
     * @param networkTable     (can be null)
     * @param networkName
     * @throws SQLException
     */
    public NetworkExtended(Connection dbConnection, String siteTable, String affiliationTable,
                           String networkTable, String networkName) throws SQLException {
        this(dbConnection, siteTable, affiliationTable, networkTable, networkName, null);
    }

    /**
     * Retrieve a whole network of SiteExtended objects from the database. Note that all the fields of
     * Network will be set to default values, but sites will be loaded into internal structures.
     * <p>
     * Example select statements: <br>
     * select * from idcstatic.site where sta in ('ASAR', 'WRA') order by sta; <br>
     * select * from idcstatic.site where sta in (select sta from idcstatic.affiliation where
     * net='SEISMIC') order by sta, ondate;
     *
     * <ul>
     * Some useful networks at the IDC:
     * <li>AA_AUX auxiliary seismic stations used in automated association
     * <li>AA_HYD all hydroacoustic stations used in automated association
     * <li>AA_IDC all stations used in automated association
     * <li>AA_INF all infrasound stations used in automated association
     * <li>AA_PRI primary seismic stations used in automated association
     * <li>ACOUSTIC all hydroacoustic and infrasonic stations ever contributing to the IDC
     * <li>AUX all auxiliary seismic stations ever contributing to the IDC
     * <li>CUR_AUX auxiliary seismic stations currently in use at the IDC
     * <li>CUR_HYD hydroacoustic stations currently in use at the IDC
     * <li>CUR_IDC all stations currently in use at the IDC
     * <li>CUR_INF infrasonic stations currently in use at the IDC
     * <li>CUR_PRI primary seismic stations currently in use at the IDC
     * <li>EVCH primary and auxiliary stations used for event characterization
     * <li>HYDRO Hydroacoustic Network
     * <li>IDC PRI, AUX, HYDRO and INFRA
     * <li>IMS_AUX IMS auxiliary seismic stations
     * <li>IMS_HYD IMS hydroacoustic stations
     * <li>IMS_IDC all IMS stations
     * <li>IMS_INF IMS infrasonic stations
     * <li>IMS_PRI IMS primary seismic stations
     * <li>INFRA Infrasonic Network
     * <li>PRI all primary seismic stations ever contributing to the IDC
     * <li>REQUEST auxiliary seismic stations currently in use at IDC and accepting requests
     * <li>SEISMIC all primary and auxiliary seismic stations ever contributing to the IDC
     * </ul>
     *
     * @param dbConnection
     * @param selectStatement
     * @return
     * @throws Exception
     */
    public NetworkExtended(Connection dbConnection, String selectStatement) throws Exception {
        super();

        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = dbConnection.createStatement();
            rs = statement.executeQuery(selectStatement);
            while (rs.next())
                add(new SiteExtended(rs));
        } catch (SQLException e) {
            throw new SQLException(String.format("%s%s%n", e.getMessage(), selectStatement));
        } finally {
            if (rs != null)
                rs.close();
            if (statement != null)
                statement.close();
        }
    }

    /**
     * Retrieve a whole network of SiteExtended objects from the database. Note that all the fields of
     * Network will be set to default values, but sites will be loaded into internal structures.
     * <p>
     * Example select statements: <br>
     * select * from idcstatic.site where sta in ('ASAR', 'WRA') order by sta; <br>
     * select * from idcstatic.site where sta in (select sta from idcstatic.affiliation where
     * net='SEISMIC') order by sta, ondate;
     *
     * <ul>
     * Some useful networks at the IDC:
     * <li>AA_AUX auxiliary seismic stations used in automated association
     * <li>AA_HYD all hydroacoustic stations used in automated association
     * <li>AA_IDC all stations used in automated association
     * <li>AA_INF all infrasound stations used in automated association
     * <li>AA_PRI primary seismic stations used in automated association
     * <li>ACOUSTIC all hydroacoustic and infrasonic stations ever contributing to the IDC
     * <li>AUX all auxiliary seismic stations ever contributing to the IDC
     * <li>CUR_AUX auxiliary seismic stations currently in use at the IDC
     * <li>CUR_HYD hydroacoustic stations currently in use at the IDC
     * <li>CUR_IDC all stations currently in use at the IDC
     * <li>CUR_INF infrasonic stations currently in use at the IDC
     * <li>CUR_PRI primary seismic stations currently in use at the IDC
     * <li>EVCH primary and auxiliary stations used for event characterization
     * <li>HYDRO Hydroacoustic Network
     * <li>IDC PRI, AUX, HYDRO and INFRA
     * <li>IMS_AUX IMS auxiliary seismic stations
     * <li>IMS_HYD IMS hydroacoustic stations
     * <li>IMS_IDC all IMS stations
     * <li>IMS_INF IMS infrasonic stations
     * <li>IMS_PRI IMS primary seismic stations
     * <li>INFRA Infrasonic Network
     * <li>PRI all primary seismic stations ever contributing to the IDC
     * <li>REQUEST auxiliary seismic stations currently in use at IDC and accepting requests
     * <li>SEISMIC all primary and auxiliary seismic stations ever contributing to the IDC
     * </ul>
     *
     * @param dbConnection
     * @param selectStatement
     * @return
     * @throws Exception
     */
    public NetworkExtended(Schema schema, String selectStatement) throws SQLException {
        super();

        try {
            Statement statement = schema.getConnection().createStatement();
            ResultSet rs = statement.executeQuery(selectStatement);
            while (rs.next())
                add(new SiteExtended(rs));
            rs.close();
            statement.close();
        } catch (SQLException e) {
            throw new SQLException(
                    String.format("%s%s%n%s%n", e.getMessage(), selectStatement, schema.toString()));
        }
    }

    /**
     * Retrieve a network composed of all the Sites that are members of the network with the specified
     * name.
     *
     * <ul>
     * Some useful networks at the IDC:
     * <li>AA_AUX auxiliary seismic stations used in automated association
     * <li>AA_HYD all hydroacoustic stations used in automated association
     * <li>AA_IDC all stations used in automated association
     * <li>AA_INF all infrasound stations used in automated association
     * <li>AA_PRI primary seismic stations used in automated association
     * <li>ACOUSTIC all hydroacoustic and infrasonic stations ever contributing to the IDC
     * <li>AUX all auxiliary seismic stations ever contributing to the IDC
     * <li>CUR_AUX auxiliary seismic stations currently in use at the IDC
     * <li>CUR_HYD hydroacoustic stations currently in use at the IDC
     * <li>CUR_IDC all stations currently in use at the IDC
     * <li>CUR_INF infrasonic stations currently in use at the IDC
     * <li>CUR_PRI primary seismic stations currently in use at the IDC
     * <li>EVCH primary and auxiliary stations used for event characterization
     * <li>HYDRO Hydroacoustic Network
     * <li>IDC PRI, AUX, HYDRO and INFRA
     * <li>IMS_AUX IMS auxiliary seismic stations
     * <li>IMS_HYD IMS hydroacoustic stations
     * <li>IMS_IDC all IMS stations
     * <li>IMS_INF IMS infrasonic stations
     * <li>IMS_PRI IMS primary seismic stations
     * <li>INFRA Infrasonic Network
     * <li>PRI all primary seismic stations ever contributing to the IDC
     * <li>REQUEST auxiliary seismic stations currently in use at IDC and accepting requests
     * <li>SEISMIC all primary and auxiliary seismic stations ever contributing to the IDC
     * </ul>
     *
     * @param networkName name of the network in the affiliation table.
     * @param schema
     * @throws Exception
     */
    public NetworkExtended(String networkName, Schema schema) throws Exception {
        super();

        String siteTable = schema.getTableName("Site");
        if (siteTable == null)
            throw new Exception("Schema does not support a table of type Site");

        String affiliationTable = schema.getTableName("Affiliation");

        if (affiliationTable == null)
            throw new Exception("Schema does not support a table of type Affiliation");

        String sql = String.format(
                "select * from %s where sta in (select sta from %s where net='%s') order by sta, ondate",
                siteTable, affiliationTable, networkName);

        Statement statement = schema.getConnection().createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next())
            add(new SiteExtended(rs));
        rs.close();
        statement.close();
    }

    public NetworkExtended(String net, String netname, String nettype, String auth, long commid) {
        super(net, netname, nettype, auth, commid);
    }

    public NetworkExtended(NetworkExtended other) {
        super(other);
        for (Set<SiteExtended> s : other.networkSites.values())
            for (Site site : s)
                add(site);
    }

    public NetworkExtended() {
        super();
    }

    public NetworkExtended(Scanner input) throws IOException {
        super();
        while (input.hasNext())
            add(new Site(input));
    }

    public NetworkExtended(DataInputStream input) throws IOException {
        super();
        while (input.available() > 0)
            add(new Site(input));
    }

    public NetworkExtended(ByteBuffer input) {
        super();
        while (input.hasRemaining())
            add(new Site(input));
    }

    public NetworkExtended(InputStream input) throws IOException {
        super();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            add(new Site(new Scanner(line)));
        }
    }

    public NetworkExtended(ResultSet input) throws SQLException {
        super();
        while (input.next())
            add(new SiteExtended(input));
    }

    /**
     * Load a network of SiteExtended objects from the specified ascii file. All records that start
     * with '#' are ignored.
     *
     * @param siteFile
     * @throws IOException
     */
    public NetworkExtended(File siteFile) throws IOException {
        super();
        for (Site site : Site.readSites(siteFile))
            add(new SiteExtended(site));
    }

    public NetworkExtended(Collection<? extends Site> sites) {
        super();
        for (Site site : sites)
            add(site);
    }

    /**
     * Add a single Site or SiteExtended object to the network
     *
     * @param site
     */
    public void add(Site site) {
        Set<SiteExtended> set = networkSites.get(site.getSta());
        if (set == null) {
            // create a TreeSet where sites are ordered by ondate
            Comparator<SiteExtended> cmp = new Comparator<SiteExtended>() {
                @Override
                public int compare(SiteExtended o1, SiteExtended o2) {
                    return (int) Math.signum(o2.getOndate() - o1.getOndate());
                }
            };
            set = new TreeSet<SiteExtended>(cmp);
            networkSites.put(site.getSta(), set);
        }
        if (site instanceof SiteExtended)
            set.add((SiteExtended) site);
        else
            set.add(new SiteExtended(site));
    }

    /**
     * Add a Collection of SiteExtended objects to the network
     *
     * @param sites
     */
    public void addAll(Collection<? extends SiteExtended> sites) {
        for (SiteExtended site : sites)
            add(site);
    }

    /**
     * @return list of all sites including all on/off dates.
     */
    public ArrayList<SiteExtended> getSites() {
        ArrayList<SiteExtended> allSites = new ArrayList<SiteExtended>(networkSites.size());
        for (Set<SiteExtended> set : networkSites.values())
            for (SiteExtended r : set)
                allSites.add(r);
        return allSites;
    }

    /**
     * return list of all sites that were active on the specified jdate. For currently active sites,
     * specify jdate = 2286324
     *
     * @return list of all sites that were active on the specified jdate.
     */
    public ArrayList<SiteExtended> getSites(long jdate) {
        ArrayList<SiteExtended> allSites = new ArrayList<SiteExtended>(networkSites.size());
        for (Set<SiteExtended> set : networkSites.values())
            for (SiteExtended r : set)
                if (r.testJdate(jdate))
                    allSites.add(r);
        return allSites;
    }

    /**
     * return a list with one site per sta (the one with the largest ondate).
     *
     * @return a list with one site per sta.
     */
    public ArrayList<SiteExtended> getSitesLatest() {
        ArrayList<SiteExtended> allSites = new ArrayList<SiteExtended>(networkSites.size());
        for (Set<SiteExtended> set : networkSites.values())
            allSites.add(set.iterator().next());
        return allSites;
    }

    /**
     * Return a site with specified sta that was active on the specified jdate or null if no such Site
     * is available.
     *
     * @param sta
     * @param l
     * @return a site with specified sta that was active on the specified jdate or null if no such
     * Site is available.
     */
    public SiteExtended getSite(String sta, long jdate) {
        Set<SiteExtended> set = networkSites.get(sta);
        if (set != null)
            for (SiteExtended site : set)
                if (site.testJdate(jdate))
                    return site;
        return null;
    }

    /**
     * Return a site with specified sta that is currently active.
     *
     * @param sta
     * @param jdate
     * @return a site with specified sta that is currently active.
     */
    public SiteExtended getSite(String sta) {
        Set<SiteExtended> set = networkSites.get(sta);
        if (set != null)
            for (SiteExtended site : set)
                if (site.testJdate(2286324))
                    return site;
        return null;
    }

    /**
     * Retrieve a reference to the Map<String, Set<SiteExtended>> that backs this NetworkExtended
     * object.
     *
     * @return a reference to the Map<String, Set<SiteExtended>> that backs this NetworkExtended
     * object.
     */
    public Map<String, Set<SiteExtended>> getNetworkSites() {
        return networkSites;
    }

    /**
     * Set the Map<String, Set<SiteExtended>> object that will back this NetworkExtended object.
     *
     * @param networkSites the Map<String, Set<SiteExtended>> object that will back this
     *                     NetworkExtended object.
     */
    public void setNetworkSites(Map<String, Set<SiteExtended>> networkSites) {
        this.networkSites = networkSites;
    }

    /**
     * Return a String representation of the Network object and all the Site objects supported by this
     * Netork.
     */
    @Override
    public String toString() {
        StringBuffer out = new StringBuffer(super.toString() + '\n');
        for (Set<SiteExtended> set : networkSites.values())
            for (SiteExtended r : set)
                out.append(r.toString() + '\n');
        return out.toString();
    }

    /**
     * Retrieve the number of stations in the network. This is the number of unique sta, not the total
     * number of Sites.
     *
     * @return
     */
    public int size() {
        return networkSites.size();
    }

    /**
     * Retrieve a list of the station names all included in a single String.
     *
     * @param format the format specifier for each element of the list, e.g., "'%s', ".
     * @return
     */
    public String getStationList(String format) {
        StringBuffer buf = new StringBuffer();
        for (String sta : networkSites.keySet())
            buf.append(String.format(format, sta));
        return buf.toString();
    }

    /**
     * Retrieve the site with the specified sta which has the latest ondate.
     *
     * @param sta
     * @return
     */
    public SiteExtended getSiteLatest(String sta) {
        Set<SiteExtended> set = networkSites.get(sta);
        return set == null ? null : set.iterator().next();
    }

    /**
     * Iterate through each set of sites that have the same sta. For each pair where s1.sta=s2.sta and
     * s1.offdate==s2.ondate, decrement s1.offdate by one day.
     *
     * @return a reference to this after fixing the offdates.
     */
    public NetworkExtended fixOnOffDates() {
        SiteExtended sj;
        for (Set<SiteExtended> set : networkSites.values()) {
            ArrayList<SiteExtended> sites = new ArrayList<>(set);
            for (int i = 0; i < sites.size() - 1; ++i) {
                int ondate = (int) sites.get(i).getOndate();
                for (int j = i + 1; j < sites.size(); ++j) {
                    sj = sites.get(j);
                    int offdate = (int) sj.getOffdate();
                    if (offdate == ondate) {
                        Calendar calendar = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
                        calendar.set(Calendar.YEAR, offdate / 1000);
                        calendar.set(Calendar.DAY_OF_YEAR, offdate % 1000);
                        calendar.add(Calendar.DAY_OF_MONTH, -1);
                        sj.setOffdate(calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR));
                    }
                }
            }
        }
        return this;
    }

}
