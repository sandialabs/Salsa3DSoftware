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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.testingbuffer.TestBuffer;
import gov.sandia.gnem.dbtabledefs.css30.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;

public class AssocExtended extends Assoc {

    private static final long serialVersionUID = 1L;

    protected ArrivalExtended arrival;
    
    private OriginExtended origin;
    
	static public Comparator<AssocExtended> sortByOridArid = new Comparator<AssocExtended>() {
        @Override
        public int compare(AssocExtended o1, AssocExtended o2) {
            int order = (int) Math.signum(o1.getOrid() - o2.getOrid());
            if (order == 0)
                order = (int) Math.signum(o1.getArid() - o2.getArid());
            return order;
        }
    };

    static public Comparator<AssocExtended> sortByOridStaPhase = new Comparator<AssocExtended>() {
        @Override
        public int compare(AssocExtended o1, AssocExtended o2) {
            int order = (int) Math.signum(o1.getOrid() - o2.getOrid());
            if (order == 0)
                order = (int) Math.signum(o1.getSta().compareTo(o2.getSta()));
            if (order == 0)
                order = (int) Math.signum(o1.getPhase().compareTo(o2.getPhase()));
            return order;
        }
    };

    static public Comparator<AssocExtended> sortByOridArrayStaPhase =
            new Comparator<AssocExtended>() {
                @Override
                public int compare(AssocExtended o1, AssocExtended o2) {
                    int order = (int) Math.signum(o1.getOrid() - o2.getOrid());
                    if (order == 0)
                        order = (int) Math.signum(o1.getArrival().getSite().getStatype()
                                .compareTo(o2.getArrival().getSite().getStatype()));
                    if (order == 0)
                        order = (int) Math.signum(o1.getSta().compareTo(o2.getSta()));
                    if (order == 0)
                        order = (int) Math.signum(o1.getPhase().compareTo(o2.getPhase()));
                    return order;
                }
            };

    static public Comparator<AssocExtended> sortByStaPhaseTime = new Comparator<AssocExtended>() {
        @Override
        public int compare(AssocExtended o1, AssocExtended o2) {
            int order = (int) Math.signum(o1.getSta().compareTo(o2.getSta()));
            if (order == 0) {
                order = (int) Math.signum(o1.getPhase().compareTo(o2.getPhase()));
                if (order == 0)
                    order = (int) Math.signum(o1.getArrival().getTime() - o2.getArrival().getTime());
            }
            return order;
        }
    };

    static public Comparator<AssocExtended> sortByOridTime = new Comparator<AssocExtended>() {
        @Override
        public int compare(AssocExtended o1, AssocExtended o2) {
            int order = (int) Math.signum(o1.getOrid() - o2.getOrid());
            if (order == 0 && o1.getArrival() != null && o2.getArrival() != null)
                order = (int) Math.signum(o1.getArrival().getTime() - o2.getArrival().getTime());
            return order;
        }
    };

    static public Comparator<AssocExtended> sortByArrivalTime = new Comparator<AssocExtended>() {
        @Override
        public int compare(AssocExtended o1, AssocExtended o2) {
            int order = 0;
            if (o1.getArrival() != null && o2.getArrival() != null)
                order = (int) Math.signum(o1.getArrival().getTime() - o2.getArrival().getTime());
            return order;
        }
    };

    static public Comparator<AssocExtended> sortByDelta = new Comparator<AssocExtended>() {
        @Override
        public int compare(AssocExtended o1, AssocExtended o2) {
            return (int) Math.signum(o1.getDelta() - o2.getDelta());
        }
    };

    static public Comparator<AssocExtended> sortByDeltaDescending = new Comparator<AssocExtended>() {
        @Override
        public int compare(AssocExtended o1, AssocExtended o2) {
            return (int) Math.signum(o2.getDelta() - o1.getDelta());
        }
    };

    static public Comparator<AssocExtended> sortByPhaseDelta = new Comparator<AssocExtended>() {
        @Override
        public int compare(AssocExtended o1, AssocExtended o2) {
            int order = (int) Math.signum(o1.getPhase().compareTo(o2.getPhase()));
            if (order == 0)
                order = (int) Math.signum(o1.getDelta() - o2.getDelta());
            return order;
        }
    };

    /**
     * Constructor that populates all the fields that it can from the origin and arrival information
     * (arid, orid, sta, delta, seaz and esaz). Remaining fields are populated with na values,
     * including phase, timeres, timedef, azres, azdef, slores, slodef, etc..
     *
     * @param origin
     * @param arrival the arrival must have a valid, non-null, SiteExtended field
     */
    public AssocExtended(OriginExtended origin, ArrivalExtended arrival, String phase) {
        super(arrival.getArid(), origin.getOrid(), arrival.getSta(), phase, BELIEF_NA,
                1e-5 * Math.round(1e5 * VectorUnit.angleDegrees(origin.getUnitVector(), arrival.getSite().getUnitVector())),
                VectorUnit.azimuthDegrees(arrival.getSite().getUnitVector(), origin.getUnitVector(), Double.NaN),
                VectorUnit.azimuthDegrees(origin.getUnitVector(), arrival.getSite().getUnitVector(), Double.NaN),
                TIMERES_NA, TIMEDEF_NA, AZRES_NA, AZDEF_NA, SLORES_NA, SLODEF_NA, EMARES_NA, WGT_NA,
                VMODEL_NA, COMMID_NA);

        // ensure that az values are not nan and between 0 and 360.
        setEsaz(getEsaz());
        setSeaz(getSeaz());

        this.origin = origin;
        this.arrival = arrival;
    }

    public AssocExtended(long arid, long orid, String sta, String phase, double belief, double delta,
                         double seaz, double esaz, double timeres, String timedef, double azres, String azdef,
                         double slores, String slodef, double emares, double wgt, String vmodel, long commid) {
        super(arid, orid, sta, phase, belief, delta, seaz, esaz, timeres, timedef, azres, azdef, slores,
                slodef, emares, wgt, vmodel, commid);
    }

    public AssocExtended(AssocExtended other) {
        super(other);
        this.origin = other.origin;
        this.arrival = other.arrival;
    }

    public AssocExtended(Assoc other) {
        super(other);
    }

    public AssocExtended() {
        super();
    }

    public AssocExtended(Scanner input) throws IOException {
        super(input);
    }

    public AssocExtended(DataInputStream input) throws IOException {
        super(input);
    }

    public AssocExtended(ByteBuffer input) {
        super(input);
    }

    public AssocExtended(ResultSet input) throws SQLException {
        super(input);
    }

    public AssocExtended(ResultSet input, int offset) throws SQLException {
        super(input, offset);
    }

    public OriginExtended getOrigin() {
        return origin;
    }

    public AssocExtended setOrigin(OriginExtended origin) {
        this.origin = origin;
        this.setOrid(origin.getOrid());
        return this;
    }

    public ArrivalExtended getArrival() {
        return arrival;
    }

    /**
     * Set the arrival. It is caller's responsibility to call updateGeometry() to update delta, seaz
     * and esaz.
     *
     * @param arrival
     * @return reference to this
     */
    public AssocExtended setArrival(ArrivalExtended arrival) {
        this.arrival = arrival;
        //setHash(null);
        return this;
    }

    public SiteExtended getSite() {
        return arrival == null ? null : arrival.getSite();
    }

    /**
     * Read a Set of Assoc objects from an ascii file.
     *
     * <p>
     * Note that all Strings must be enclosed in double quotes.
     *
     * @param inputFile
     * @return a Set of Assoc objects
     * @throws IOException
     */
    static public Set<AssocExtended> readAssocExtendeds(File inputFile) throws IOException {
        Set<AssocExtended> rows = new HashSet<AssocExtended>();
        readAssocExtendeds(inputFile, rows);
        return rows;
    }

    /**
     * Read a Set of Assoc objects from an ascii file. Records that begin with '#' are ignored.
     *
     * @param inputFile
     * @param rows      a Set of Assoc objects.
     * @throws IOException
     */
    static public void readAssocExtendeds(File inputFile, Set<AssocExtended> rows)
            throws IOException {
        if (inputFile.exists()) {
            BufferedReader input = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#"))
                    rows.add(new AssocExtended(new Scanner(line)));
            }
            input.close();
        }
    }

    /**
     * Execute a sql statement similar to the following and then parse the results into appropriate
     * origin, assoc, arrival, and site rows and pack them all up into an Assoc RowGraph.
     *
     * <p>
     * select origin.*, assoc.*, arrival.*, site.* <br>
     * from leb_origin origin, leb_assoc assoc, leb_arrival arrival, idc_site site, idc_affiliation
     * affiliation <br>
     * where origin.orid=assoc.orid and assoc.arid=arrival.arid and arrival.sta=site.sta <br>
     * and arrival.jdate greater than or equal to site.ondate and (site.offdate = -1 or arrival.jdate
     * &<= site.offdate) <br>
     * and site.sta=affiliation.sta and [whereClause]
     *
     * <p>
     * The affiliation table is optional and is only implemented if the Schema has an affiliation
     * table specified.
     */
    public static Set<AssocExtended> readAssocExtendeds(Schema schema, String whereClause,
                                                        ArrayList<String> executedSQL) throws Exception {
        return readAssocExtendeds1(schema, whereClause, executedSQL, null);
    }

    /**
     * Execute a sql statement similar to the following and then parse the results into appropriate
     * origin, assoc, arrival, and site rows and pack them all up into an Assoc RowGraph.
     *
     * <p>
     * select origin.*, assoc.*, arrival.*, site.* <br>
     * from leb_origin origin, leb_assoc assoc, leb_arrival arrival, idc_site site, idc_affiliation
     * affiliation <br>
     * where origin.orid=assoc.orid and assoc.arid=arrival.arid and arrival.sta=site.sta <br>
     * and arrival.jdate greater than or equal to site.ondate and (site.offdate = -1 or arrival.jdate
     * &<= site.offdate) <br>
     * and site.sta=affiliation.sta and [whereClause]
     *
     * <p>
     * The affiliation table is optional and is only implemented if the Schema has an affiliation
     * table specified.
     */
    public static Set<AssocExtended> readAssocExtendeds2(Schema schema, String whereClause,
                                                         ArrayList<String> executedSQL, NetworkExtended network) throws Exception {
        String originTable = schema.getTableName("Origin");
        String assocTable = schema.getTableName("Assoc");
        String arrivalTable = schema.getTableName("Arrival");
        String siteTable = schema.getTableName("Site");
        String affiliationTable = schema.getTableName("Affiliation");

        if (network == null)
            network = new NetworkExtended();

        // siteInfo will be true if site information was included in the join in the sql statement
        boolean siteInfo = true;

        if (whereClause == null)
            whereClause = "";
        whereClause = whereClause.trim();
        if (whereClause.length() > 0)
            whereClause = " and " + whereClause;

        String sql_origin;

        String sql_assoc;

        if (siteTable == null || network.size() > 0) {
            siteInfo = false;
            sql_origin = String.format(
                    "select * from %s where orid in (select unique(origin.orid) from %s origin, %s assoc, %s arrival "
                            + "where origin.orid=assoc.orid and assoc.arid=arrival.arid%s)",
                    originTable, originTable, assocTable, arrivalTable, whereClause);
            sql_assoc = String.format(
                    "select assoc.*, arrival.* from %s origin, %s assoc, %s arrival "
                            + "where origin.orid=assoc.orid and assoc.arid=arrival.arid%s",
                    originTable, assocTable, arrivalTable, whereClause);
        } else if (affiliationTable == null) {
            sql_origin = String.format(
                    "select * from %s where orid in (select unique(origin.orid) from %s origin, %s assoc, %s arrival, %s site "
                            + "where origin.orid=assoc.orid and assoc.arid=arrival.arid and arrival.sta=site.sta "
                            + "and arrival.jdate >= site.ondate and (site.offdate = -1 or arrival.jdate <= site.offdate)%s)",
                    originTable, originTable, assocTable, arrivalTable, siteTable, whereClause);
            sql_assoc = String.format(
                    "select assoc.*, arrival.*, site.* from %s origin, %s assoc, %s arrival, %s site "
                            + "where origin.orid=assoc.orid and assoc.arid=arrival.arid and arrival.sta=site.sta "
                            + "and arrival.jdate >= site.ondate and (site.offdate = -1 or arrival.jdate <= site.offdate)%s",
                    originTable, assocTable, arrivalTable, siteTable, whereClause);
        } else {
            sql_origin = String.format(
                    "select * from %s where orid in (select unique(origin.orid) from %s origin, %s assoc, %s arrival, %s site, %s affiliation "
                            + "where origin.orid=assoc.orid and assoc.arid=arrival.arid and arrival.sta=site.sta "
                            + "and arrival.jdate >= site.ondate and (site.offdate = -1 or arrival.jdate <= site.offdate) "
                            + "and site.sta=affiliation.sta%s)",
                    originTable, originTable, assocTable, arrivalTable, siteTable, affiliationTable,
                    whereClause);
            sql_assoc = String.format(
                    "select assoc.*, arrival.*, site.* from %s origin, %s assoc, %s arrival, %s site, %s affiliation "
                            + "where origin.orid=assoc.orid and assoc.arid=arrival.arid and arrival.sta=site.sta "
                            + "and arrival.jdate >= site.ondate and (site.offdate = -1 or arrival.jdate <= site.offdate) "
                            + "and site.sta=affiliation.sta%s",
                    originTable, assocTable, arrivalTable, siteTable, affiliationTable, whereClause);
        }

        if (executedSQL != null) {
            executedSQL.add(sql_origin);
            executedSQL.add(sql_assoc);
        }

        HashMap<Long, OriginExtended> origins = new HashMap<Long, OriginExtended>();
        try (Statement statement = schema.getStatement();
             ResultSet rs = statement.executeQuery(sql_origin)) {
            while (rs.next()) {
                OriginExtended origin = new OriginExtended(rs);
                origins.put(origin.getOrid(), origin);
            }
        } catch (SQLException ex) {
            throw new SQLException(String.format("%s%n%s%nUserName=%s%nInstance=%s", ex.getMessage(),
                    sql_origin, schema.getUserName(), schema.getInstance()));
        }

        LinkedHashSet<AssocExtended> assocs = new LinkedHashSet<>(origins.size());

        try (Statement statement = schema.getStatement();
             ResultSet rs = statement.executeQuery(sql_assoc)) {
            while (rs.next()) {
                ArrivalExtended arrival = new ArrivalExtended(rs, Assoc.getColumns().size() + 1);

                // see if the site for this arrival has already been loaded into the network
                arrival.setSite(network);

                if (arrival.getSite() == null && siteInfo) {
                    arrival.setSite(
                            new SiteExtended(rs, Assoc.getColumns().size() + Arrival.getColumns().size() + 2));
                    network.add(arrival.getSite());
                }

                AssocExtended assoc = new AssocExtended(rs);
                assoc.setArrival(arrival);

                origins.get(assoc.getOrid()).addAssoc(assoc);

                assocs.add(assoc);

            }
        } catch (SQLException ex) {
            throw new SQLException(String.format("%s%n%s%nUserName=%s%nInstance=%s", ex.getMessage(),
                    sql_assoc, schema.getUserName(), schema.getInstance()));
        }

        return assocs;
    }

    /**
     * Execute a sql statement similar to the following and then parse the results into appropriate
     * origin, assoc, arrival, and site rows and pack them all up into an Assoc RowGraph.
     *
     * <p>
     * select origin.*, assoc.*, arrival.*, site.* <br>
     * from leb_origin origin, leb_assoc assoc, leb_arrival arrival, idc_site site, idc_affiliation
     * affiliation <br>
     * where origin.orid=assoc.orid and assoc.arid=arrival.arid and arrival.sta=site.sta <br>
     * and arrival.jdate greater than or equal to site.ondate and (site.offdate = -1 or arrival.jdate
     * &<= site.offdate) <br>
     * and site.sta=affiliation.sta and [whereClause]
     *
     * <p>
     * The affiliation table is optional and is only implemented if the Schema has an affiliation
     * table specified.
     */
    public static Set<AssocExtended> readAssocExtendeds1(Schema schema, String whereClause,
                                                         ArrayList<String> executedSQL, NetworkExtended network) throws Exception {
        LinkedHashSet<AssocExtended> assocs = new LinkedHashSet<>();

        String originTable = schema.getTableName("Origin");
        String assocTable = schema.getTableName("Assoc");
        String arrivalTable = schema.getTableName("Arrival");
        String siteTable = schema.getTableName("Site");
        String affiliationTable = schema.getTableName("Affiliation");

        if (network == null)
            network = new NetworkExtended();

        // siteInfo will be true if site information was included in the join in the sql statement
        boolean siteInfo = true;

        String sql;

        if (siteTable == null || network.size() > 0) {
            siteInfo = false;
            sql = String.format(
                    "select origin.*, assoc.*, arrival.* from %s origin, %s assoc, %s arrival "
                            + "where origin.orid=assoc.orid and assoc.arid=arrival.arid",
                    originTable, assocTable, arrivalTable);
        } else if (affiliationTable == null)
            sql = String.format(
                    "select origin.*, assoc.*, arrival.*, site.* from %s origin, %s assoc, %s arrival, %s site "
                            + "where origin.orid=assoc.orid and assoc.arid=arrival.arid and arrival.sta=site.sta "
                            + "and arrival.jdate >= site.ondate and (site.offdate = -1 or arrival.jdate <= site.offdate)",
                    originTable, assocTable, arrivalTable, siteTable);
        else
            sql = String.format(
                    "select origin.*, assoc.*, arrival.*, site.* from %s origin, %s assoc, %s arrival, %s site, %s affiliation "
                            + "where origin.orid=assoc.orid and assoc.arid=arrival.arid and arrival.sta=site.sta "
                            + "and arrival.jdate >= site.ondate and (site.offdate = -1 or arrival.jdate <= site.offdate) "
                            + "and site.sta=affiliation.sta",
                    originTable, assocTable, arrivalTable, siteTable, affiliationTable);


        if (whereClause != null && whereClause.trim().length() > 0)
            sql = sql + " and " + whereClause.trim();

        if (executedSQL != null)
            executedSQL.add(sql);

        try (Statement statement = schema.getStatement(); ResultSet rs = statement.executeQuery(sql)) {
            HashMap<Long, OriginExtended> origins = new HashMap<Long, OriginExtended>();
            while (rs.next()) {
                ArrivalExtended arrival =
                        new ArrivalExtended(rs, Origin.getColumns().size() + Assoc.getColumns().size() + 2);

                // see if the site for this arrival has already been loaded into the network
                arrival.setSite(network);

                if (arrival.getSite() == null && siteInfo) {
                    arrival.setSite(new SiteExtended(rs, Origin.getColumns().size()
                            + Assoc.getColumns().size() + Arrival.getColumns().size() + 3));
                    network.add(arrival.getSite());
                }

                AssocExtended assoc = new AssocExtended(rs, Origin.getColumns().size() + 1);
                assoc.setArrival(arrival);

                OriginExtended origin = origins.get(assoc.getOrid());
                if (origin == null)
                    origins.put(assoc.getOrid(), origin = new OriginExtended(rs));
                origin.addAssoc(assoc);
                assoc.setOrigin(origin);

                assocs.add(assoc);

            }
        } catch (SQLException ex) {
            throw new SQLException(String.format("%s%n%s%nUserName=%s%nInstance=%s", ex.getMessage(), sql,
                    schema.getUserName(), schema.getInstance()));
        }

        return assocs;
    }

    /**
     * Update delta, seaz and esaz using the location of the supplied origin and the current
     * arrival.getSite().
     *
     * @return reference to this.
     */
    public AssocExtended updateGeometry(OriginExtended origin) {
        if (arrival == null || arrival.getSite() == null) {
            super.setDelta(DELTA_NA);
            super.setEsaz(ESAZ_NA);
            super.setSeaz(SEAZ_NA);
        } else {
            setDelta(VectorUnit.angleDegrees(origin.getUnitVector(), arrival.getSite().getUnitVector()));
            setSeaz(VectorUnit.azimuthDegrees(arrival.getSite().getUnitVector(), origin.getUnitVector(),
                    Double.NaN));
            setEsaz(VectorUnit.azimuthDegrees(origin.getUnitVector(), arrival.getSite().getUnitVector(),
                    Double.NaN));
        }
        return this;
    }

    /**
     * return timedef.equals("d")
     *
     * @return timedef.equals(" d ")
     */
    public boolean isTimedef() {
        return getTimedef().equalsIgnoreCase("d");
    }

    /**
     * set timedef to "d" or "n"
     *
     * @param defining
     * @return reference to this
     */
    public AssocExtended setTimedef(boolean defining) {
        setTimedef(defining ? "d" : "n");
        return this;
    }

    /**
     * return azdef.equals("d")
     *
     * @return azdef.equals(" d ")
     */
    public boolean isAzdef() {
        return getAzdef().equalsIgnoreCase("d");
    }

    /**
     * set azdef to "d" or "n"
     *
     * @param defining
     * @return reference to this
     */
    public AssocExtended setAzdef(boolean defining) {
        setAzdef(defining ? "d" : "n");
        return this;
    }

    /**
     * return slodef.equals("d")
     *
     * @return slodef.equals(" d ")
     */
    public boolean isSlodef() {
        return getSlodef().equalsIgnoreCase("d");
    }

    /**
     * set slodef to "d" or "n"
     *
     * @param defining
     * @return reference to this
     */
    public AssocExtended setSlodef(boolean defining) {
        setSlodef(defining ? "d" : "n");
        return this;
    }

    @Override
    public AssocExtended setEsaz(double esaz) {
        super.setEsaz(conditionAzDegrees(esaz, ESAZ_NA, 2));
        return this;
    }

    @Override
    public AssocExtended setSeaz(double seaz) {
        //super.setSeaz(Double.isNaN(seaz) ? SEAZ_NA : seaz < 0. ? seaz + 360. : seaz);
        super.setSeaz(conditionAzDegrees(seaz, SEAZ_NA, 2));
        return this;
    }

//  @Override
//  public BigInteger getHash() {
//    if (isHashNull()) {
//      super.getHash();
//      if (arrival != null)
//        incrementHash(arrival.getHash());
//      if (origin != null)
//        incrementHash(origin.getHash());
//    }
//    return super.getHash();
//  }

    /**
     * Sort the supplied List of Assocs by orid,arid
     *
     * @param assocs
     */
    static public void sortByOridArid(List<? extends AssocExtended> assocs) {
        Collections.sort(assocs, sortByOridArid);
    }

    /**
     * Sort the supplied List of Assocs by orid,sta,phase
     *
     * @param assocs
     */
    static public void sortByOridStaPhase(List<? extends AssocExtended> assocs) {
        Collections.sort(assocs, sortByOridStaPhase);
    }

    /**
     * Sort the supplied List of Assocs by orid,sta,phase
     *
     * @param assocs
     */
    static public void sortByStaPhaseTime(List<? extends AssocExtended> assocs) {
        Collections.sort(assocs, sortByStaPhaseTime);
    }

    /**
     * Sort the supplied List of Assocs by delta
     *
     * @param assocs
     */
    static public void sortByDelta(List<? extends AssocExtended> assocs) {
        Collections.sort(assocs, sortByDelta);
    }

    /**
     * Sort the supplied List of Assocs by delta
     *
     * @param assocs
     */
    static public void sortByDeltaDescending(List<? extends AssocExtended> assocs) {
        Collections.sort(assocs, sortByDeltaDescending);
    }

    /**
     * Sort the supplied List of Assocs by orid, arrival.time
     *
     * @param assocs
     */
    static public void sortByOridTime(List<? extends AssocExtended> assocs) {
        Collections.sort(assocs, sortByOridTime);
    }

    /**
     * Sort the supplied List of Assocs by orid, arrival.time
     *
     * @param assocs
     */
    static public void sortByArrivalTime(List<? extends AssocExtended> assocs) {
        Collections.sort(assocs, sortByArrivalTime);
    }

    /**
     * Sort the supplied List of Assocs by orid, arrival.time
     *
     * @param assocs
     */
    static public void sortByPhaseDelta(List<? extends AssocExtended> assocs) {
        Collections.sort(assocs, sortByPhaseDelta);
    }

    /**
     * Copy the specified assocs into an ArrayList, sort the list using the specified Comparator, and
     * return the list.
     *
     * @param assocs
     * @param comparator
     * @return
     */
    static public ArrayList<AssocExtended> sort(Collection<? extends AssocExtended> assocs,
                                                Comparator<AssocExtended> comparator) {
        ArrayList<AssocExtended> sorted = new ArrayList<AssocExtended>(assocs);
        Collections.sort(sorted, comparator);
        return sorted;
    }

    /**
     * Returns true if the Arrival associated with this Assoc comes from a Site that is an array.
     *
     * @return site.statype.equals(" ar ")
     */
    public boolean isArray() {
        return arrival == null ? false : arrival.isArray();
    }

    /**
     * Generate a String representation of this AssocExtended object and optionally its children.
     * <ol start=0>
     * <li>arrival: if true print arrivals
     * <li>site: if true print sites
     * </ol>
     *
     * @param includeChildren
     * @return
     */
    public String toString(boolean... includeChildren) {
        boolean printArrival = includeChildren.length > 0 && includeChildren[0];
        boolean printSite = includeChildren.length > 1 && includeChildren[1];
        StringBuffer out = new StringBuffer();

        if (printArrival)
            out.append(NL).append(toStringHeader()).append(NL);

        out.append(toStringCustom()).append(NL);


        if (printArrival) {
            if (!printSite)
                out.append(NL).append(ArrivalExtended.toStringHeader()).append(NL);

            out.append(getArrival() == null ? "null" : getArrival().toString(printSite)).append(NL);
        }

        return out.toString();
    }

    public static String toStringHeader() {
        return String.format("%12s %12s %6s %8s %8s %7s %7s %8s %7s %7s %7s %7s %7s %15s", "orid",
                "arid", "sta", "phase", "delta", "seaz", "esaz", "timeres", "timedef", "azres", "azdef",
                "slores", "slodef", "vmodel");
    }


    private String toStringCustom() {
        return String.format("%12d %12d %6s %8s %8.3f %7.2f %7.2f %8.3f %7s %7.1f %7s %7.2f %7s %15s",
                getOrid(), getArid(), getSta(), getPhase(), getDelta(), getSeaz(), getEsaz(), getTimeres(),
                getTimedef(), getAzres(), getAzdef(), getSlores(), getSlodef(), getVmodel());
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

    public TestBuffer getTestBuffer() {
    	TestBuffer buffer = new TestBuffer();
    	buffer.add(super.getTestBuffer());

    	if (arrival != null) 
    		buffer.add(arrival.getTestBuffer());

    	return buffer;
    }

}
