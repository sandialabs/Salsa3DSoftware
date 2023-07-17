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
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import gov.sandia.gmp.util.time.TimeInterface;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Event;

public class EventExtended extends Event implements TimeInterface {

    private static final long serialVersionUID = 1L;

    private OriginExtended preferredOrigin;

    private Map<Long, OriginExtended> origins = new HashMap<Long, OriginExtended>();

    static private Comparator<Event> sortByEvid = new Comparator<Event>() {
        @Override
        public int compare(Event o1, Event o2) {
            return (int) Math.signum(o1.getEvid() - o2.getEvid());
        }
    };

    static private Comparator<Event> sortByPrefor = new Comparator<Event>() {
        @Override
        public int compare(Event o1, Event o2) {
            return (int) Math.signum(o1.getPrefor() - o2.getPrefor());
        }
    };

    static private Comparator<EventExtended> sortByPreforTime = new Comparator<EventExtended>() {
        @Override
        public int compare(EventExtended o1, EventExtended o2) {
            return (int) Math.signum(o1.getPreferredOrigin().getTime() - o2.getPreferredOrigin().getTime());
        }
    };

    public EventExtended() {
        super();
        origins = new HashMap<Long, OriginExtended>();
    }

    /**
     * Parameterized constructor.
     *
     * <p>
     * WARNING: This method leaves this EventExtended object in an inconsistent state since prefor may
     * be set to a value > 0 and yet the preferredOrigin is null. User should call
     * setPreferredOrigin() immediately after calling this method in order to put this EventExtended
     * object into a valid state.
     *
     * @param evid
     * @param evname
     * @param prefor
     * @param auth
     * @param commid
     */
    public EventExtended(long evid, String evname, long prefor, String auth, long commid) {
        super(evid, evname, prefor, auth, commid);
        origins = new HashMap<Long, OriginExtended>();
    }

    /**
     * <p>
     * WARNING: This method leaves this EventExtended object in an inconsistent state since prefor may
     * be set to a value > 0 and yet the preferredOrigin is null. User should call
     * setPreferredOrigin() immediately after calling this method in order to put this EventExtended
     * object into a valid state.
     *
     * @param other
     */
    public EventExtended(Event other) {
        super(other);
        origins = new HashMap<Long, OriginExtended>();
    }

    /**
     * Copy constructor. Makes a shallow copy of other.origins. preferredOrigins is set to a reference
     * to other.preferredOrigin
     *
     * @param other
     */
    public EventExtended(EventExtended other) {
        super(other);
        this.origins = new HashMap<Long, OriginExtended>(other.origins.size());
        for (OriginExtended o : other.origins.values())
            this.origins.put(o.getOrid(), o);
        setPreferredOrigin(other.preferredOrigin);
    }

    public EventExtended(OriginExtended preferredOrigin) {
        super();
        origins = new HashMap<Long, OriginExtended>();
        setEvid(preferredOrigin.getEvid());
        setPreferredOrigin(preferredOrigin);
        setAuth(preferredOrigin.getAuth());
    }

    /**
     * Load Event info from a Scanner object.
     *
     * <p>
     * WARNING: This method leaves this EventExtended object in an inconsistent state since prefor may
     * be set to a value > 0 and yet the preferredOrigin is null. User should call
     * setPreferredOrigin() immediately after calling this method in order to put this EventExtended
     * object into a valid state.
     *
     * @param input
     * @throws IOException
     */
    public EventExtended(Scanner input) throws IOException {
        super(input);
        origins = new HashMap<Long, OriginExtended>();
    }

    /**
     * Load Event info from a DataInputStream object.
     *
     * <p>
     * WARNING: This method leaves this EventExtended object in an inconsistent state since prefor may
     * be set to a value > 0 and yet the preferredOrigin is null. User should call
     * setPreferredOrigin() immediately after calling this method in order to put this EventExtended
     * object into a valid state.
     *
     * @param input
     * @throws IOException
     */
    public EventExtended(DataInputStream input) throws IOException {
        super(input);
        origins = new HashMap<Long, OriginExtended>();
    }

    /**
     * Load Event info from a ByteBuffer object.
     *
     * <p>
     * WARNING: This method leaves this EventExtended object in an inconsistent state since prefor may
     * be set to a value > 0 and yet the preferredOrigin is null. User should call
     * setPreferredOrigin() immediately after calling this method in order to put this EventExtended
     * object into a valid state.
     *
     * @param input
     */
    public EventExtended(ByteBuffer input) {
        super(input);
        origins = new HashMap<Long, OriginExtended>();
    }

    /**
     * Load Event info from a ResultSet object.
     *
     * <p>
     * WARNING: This method leaves this EventExtended object in an inconsistent state since prefor may
     * be set to a value > 0 and yet the preferredOrigin is null. User should call
     * setPreferredOrigin() immediately after calling this method in order to put this EventExtended
     * object into a valid state.
     *
     * @param input
     * @throws SQLException
     */
    public EventExtended(ResultSet input) throws SQLException {
        super(input);
        origins = new HashMap<Long, OriginExtended>();
    }

    /**
     * Load Event info from a ResultSet object.
     *
     * <p>
     * WARNING: This method leaves this EventExtended object in an inconsistent state since prefor may
     * be set to a value > 0 and yet the preferredOrigin is null. User should call
     * setPreferredOrigin() immediately after calling this method in order to put this EventExtended
     * object into a valid state.
     *
     * @param input
     * @param offset
     * @throws SQLException
     */
    public EventExtended(ResultSet input, int offset) throws SQLException {
        super(input, offset);
        origins = new HashMap<Long, OriginExtended>();
    }

    /**
     * Change the evid of this EventExtended object and also change the evids of all the
     * OriginExtended objects that are members of this EventExtended.
     */
    public EventExtended setEvid(long evid) {
        if (evid != getEvid()) {
            super.setEvid(evid);
            for (OriginExtended origin : origins.values())
                origin.setEvid(evid);
            //setHash(null);
        }
        return this;
    }

    /**
     * Retrieve a reference to the preferred origin.
     *
     * @return a reference to the preferred origin.
     */
    public OriginExtended getPreferredOrigin() {
        return preferredOrigin;
    }

    /**
     * Retrieve the List of preferredOrigins extracted from the supplied Events.
     *
     * @return the List of preferredOrigins extracted from the supplied Events.
     */
    static public ArrayList<OriginExtended> getPreferredOrigins(
            Collection<? extends EventExtended> events) {
        ArrayList<OriginExtended> preferredOrigins = new ArrayList<OriginExtended>(events.size());
        for (EventExtended event : events)
            preferredOrigins.add(event.preferredOrigin);
        return preferredOrigins;
    }

    /**
     * <ol>
     * <li>Change the isPreferred status of the previous preferredOrigin to false.
     * <li>Set the preferredOrigin of this EventExtended to the new preferredOrigin
     * <li>Change the isPreferred status of the new preferredOrigin to true.
     * <li>Update the prefor of this Event
     * <li>Add preferredOrigin to the set of origins.
     * </ol>
     * The new preferredOrigin may be null.
     *
     * @param preferredOrigin
     */
    public EventExtended setPreferredOrigin(OriginExtended preferredOrigin) {
        // if (this.preferredOrigin != null)
        // this.preferredOrigin.setPreferred(false);

        if (preferredOrigin != null) {
            setPrefor(preferredOrigin.getOrid());
            // preferredOrigin.setPreferred(true);
            origins.put(preferredOrigin.getOrid(), preferredOrigin);
        } else
            setPrefor(-1L);

        this.preferredOrigin = preferredOrigin;
        //setHash(null);
        return this;
    }

//  @Override
//  public BigInteger getHash() {
//    if (super.getHash() == null) {
//      super.getHash();
//      if (preferredOrigin != null)
//        incrementHash(preferredOrigin.getHash());
//      for (OriginExtended origin : origins.values())
//        incrementHash(origin.getHash());
//    }
//    return super.getHash();
//  }

    /**
     * Retrieve a reference to the set of origins owned by this EventExtended object. Can never be
     * null but may be empty.
     * <p>
     * CAUTION: Do not change any of the values of any of the origins returned by getOrigins(). Doing
     * so will change the hashCodes used to organize the origins in the Set<OriginExtended> owned by
     * this EventExtended and screw up the set buckets. To change the values of any of the origins
     * first call removeOrigin(origin), change the origin values, and then call addOrigin(origin) to
     * return it to the Set. If what you want to do is change the evid of all the origins owned by
     * this event, consider calling setEvid(evid) which will make the changes safely.
     *
     * @return a reference to the set of origins owned by this EventExtended object.
     */
    public Map<Long, OriginExtended> getOrigins() {
        return origins;
    }

    public HashMap<Long, ArrivalExtended> getArrivals() {
        HashMap<Long, ArrivalExtended> arrivals = new HashMap<Long, ArrivalExtended>(20);
        for (OriginExtended origin : origins.values())
            for (AssocExtended assoc : origin.getAssocs().values())
                arrivals.put(assoc.getArrival().getArid(), assoc.getArrival());
        return arrivals;
    }

    /**
     * Remove the specified origin. If it is the preferredOrigin then this.preferredOrigin is set to
     * null.
     *
     * @param origin
     * @return true if size of origins changed.
     */
    public boolean removeOrigin(OriginExtended origin) {
        if (origins.remove(origin.getOrid()) != null) {
            if (origin.equals(preferredOrigin)) {
                // origin.setPreferred(false);
                preferredOrigin = null;
            }
            //super.setHash(null);
            return true;
        }
        return false;
    }

    /**
     * Add the specified origin to the set of origins.
     *
     * @param origin
     * @return true if size of origins changed.
     */
    public boolean addOrigin(OriginExtended origin) {
        if (origin.getEvid() != getEvid())
            origin.setEvid(getEvid());
        if (origins.put(origin.getOrid(), origin) == null) {
            if (origins.size() == 1)
                preferredOrigin = origin; // .setPreferred(true);
            //super.setHash(null);;
            return true;
        }
        return false;
    }

    /**
     * Add the specified origin to the set of origins.
     *
     * @param origin
     * @return true if Collection changed.
     */
    public boolean addOrigins(Collection<? extends OriginExtended> origins) {
        boolean changed = false;
        for (OriginExtended origin : origins)
            if (addOrigin(origin))
                changed = true;
        return changed;
    }

    /**
     * Specify the List of OriginExtended objects that are owned by this EventExtended and the
     * preferred Origin.
     *
     * @param origins         if null, then this.origins is set to a new empty List.
     * @param preferredOrigin
     */
    public void setOrigins(HashMap<Long, OriginExtended> origins, OriginExtended preferredOrigin) {
        this.origins = origins == null ? new HashMap<Long, OriginExtended>() : origins;
        setPreferredOrigin(preferredOrigin);
        //super.setHash(null);;
    }

    /**
     * Specify the List of OriginExtended objects that are owned by this EventExtended and the
     * preferred Origin. The preferredOrigin is set to null.
     *
     * @param origins if null, then this.origins is set to a new empty List.
     */
    public void setOrigins(HashMap<Long, OriginExtended> origins) {
        this.origins = origins == null ? new HashMap<Long, OriginExtended>() : origins;
        setPreferredOrigin(null);
        //super.setHash(null);;
    }

    /**
     * Read EventExtended object from a database without any descendant information.
     *
     * @param connection
     * @param sql        a valid SQL select statement that returns a complete row from a Event table.
     * @return a Set of EventExtended objects
     * @throws SQLException
     */
    static public HashSet<EventExtended> readEventExtended(Connection connection, String sql)
            throws SQLException {
        HashSet<EventExtended> events = new HashSet<EventExtended>();
        readEventExtended(connection, sql, events);
        return events;
    }

    /**
     * Read EventExtended object from a database without any descendant information.
     *
     * @param connection
     * @param sql        a valid SQL select statement that returns a complete row from a Event table.
     * @param events     Set<EventExtended> into which to place the results.
     * @throws SQLException
     */
    static public void readEventExtended(Connection connection, String sql,
                                         Collection<EventExtended> events) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        while (rs.next())
            events.add(new EventExtended(rs));
        rs.close();
        statement.close();
    }

    /**
     * Load EventExtended objects and descendant objects from a database.
     *
     * @param schema           a Schema that manages a database connection and a map from String tableType to
     *                         String tableName. Recognized tableTypes are [ event, origin, origerr, azgap, assoc,
     *                         arrival, site ]
     * @param network          3 possibilities:
     *                         <ul>
     *                         <li>if network is populated with sites, then sites from the network will be attached to
     *                         arrivals and any site table specified in tableNames will be ignored.
     *                         <li>if network is null, and a site table is specified in tableNames, then sites from the
     *                         input table will be attached to arrivals.
     *                         <li>if network is empty, and a site table is specified in inputTables then network will
     *                         be populated with sites from the site table and those sites will be attached to
     *                         arrivals.
     *                         </ul>
     * @param eventWhereClause a where clause to be executed against an Event table. Eg., 'evid in
     *                         (98127, 789009)'
     * @param assocWhereClause additional elements of a where clause to be executed against the assoc
     *                         table. Eg., "and phase = 'P'", which will be combined with the eventWhereClause to
     *                         produce 'orid in (98127, 789009) and phase = 'P''. Can be an empty String.
     * @param executedSQL      (output; ignored if null) the sql commands that are actually executed by
     *                         this method.
     * @return HashSet of EventExtended object with requested descendants attached.
     * @throws Exception
     */
    static public HashSet<EventExtended> readEventExtended(Schema schema, NetworkExtended network,
                                                           String eventWhereClause, String assocWhereClause, ArrayList<String> executedSQL)
            throws Exception {
        HashSet<EventExtended> events = new HashSet<EventExtended>();
        readEventExtended(schema, network, eventWhereClause, assocWhereClause, executedSQL, events);
        return events;
    }

    /**
     * Load an EventExtended object and descendant objects from a database.
     *
     * @param schema           a Schema that manages a database connection and a map from String tableType to
     *                         String tableName. Recognized tableTypes are [ event, origin, origerr, azgap, assoc,
     *                         arrival, site ]
     * @param network          3 possibilities:
     *                         <ul>
     *                         <li>if network is populated with sites, then sites from the network will be attached to
     *                         arrivals and any site table specified in tableNames will be ignored.
     *                         <li>if network is null, and a site table is specified in tableNames, then sites from the
     *                         input table will be attached to arrivals.
     *                         <li>if network is empty, and a site table is specified in inputTables then network will
     *                         be populated with sites from the site table and those sites will be attached to
     *                         arrivals.
     * @param eventWhereClause a where clause to be executed against an Event table. Eg., 'evid in
     *                         (98127, 789009)'
     * @param assocWhereClause additional elements of a where clause to be executed against the assoc
     *                         table. Eg., "and phase = 'P'", which will be combined with the eventWhereClause to
     *                         produce 'orid in (98127, 789009) and phase = 'P''. Can be an empty String.
     * @param executedSQL      (output; ignored if null) the sql commands that are actually executed by
     *                         this method.
     * @return HashSet of EventExtended object with requested descendants attached.
     * @throws Exception
     */
    static public void readEventExtended(Schema schema, NetworkExtended network,
                                         String eventWhereClause, String assocWhereClause, ArrayList<String> executedSQL,
                                         Collection<EventExtended> events) throws Exception {
        ResultSet rs;
        Statement statement = schema.getConnection().createStatement();

        String tableName = schema.getTableName("event");
        if (tableName == null)
            tableName = schema.getTableName("Event");

        String eventSql = String.format("select * from %s", tableName);
        if (eventWhereClause != null && eventWhereClause.trim().length() > 0) {
            if (eventWhereClause.startsWith("where "))
                eventSql = eventSql + " " + eventWhereClause;
            else
                eventSql = eventSql + " where " + eventWhereClause;

        }

        HashMap<Long, EventExtended> eventSet = new HashMap<Long, EventExtended>();

        // System.out.println(eventSql);

        rs = statement.executeQuery(eventSql);
        while (rs.next()) {
            EventExtended event = new EventExtended(rs);
            events.add(event);
            eventSet.put(event.getEvid(), event);
        }
        rs.close();

        boolean networkWasEmpty = network != null && network.size() == 0;

        if (schema.isSupported("origin")) {
            StringBuffer evidList = new StringBuffer();
            int count = 0;

            Iterator<Long> it = eventSet.keySet().iterator();
            while (it.hasNext()) {
                long evid = it.next();
                evidList.append(",").append(String.format("%d", evid));
                ++count;
                if (count == 1000 || !it.hasNext()) {
                    StringBuffer oridList = new StringBuffer();
                    rs = statement.executeQuery(String.format("select orid from %s where evid in (%s)",
                            schema.getTableName("origin"), evidList.toString().substring(1)));
                    while (rs.next())
                        oridList.append(String.format(",%d", rs.getLong(1)));
                    rs.close();

                    String originWhereClause =
                            String.format("orid in (%s)", oridList.toString().substring(1));

                    NetworkExtended net = network;
                    if (networkWasEmpty)
                        net = new NetworkExtended();
                    for (OriginExtended origin : OriginExtended.readOriginExtended(schema, net,
                            originWhereClause, assocWhereClause, null).values()) {
                        EventExtended event = eventSet.get(origin.getEvid());
                        event.getOrigins().put(origin.getOrid(), origin);
                        if (origin.getOrid() == event.getPrefor())
                            event.setPreferredOrigin(origin);
                    }
                    if (networkWasEmpty)
                        network.addAll(net.getSites());
                }
            }

        }
        statement.close();
    }

    /**
     * Read a Set of EventExtended objects from a File. No descendants are loaded (assocs, origerrs,
     * etc.).
     *
     * @param inputFile
     * @return HashSet of EventExtended objects.
     * @throws IOException
     */
    static public Set<EventExtended> readEventExtended(File inputFile) throws IOException {
        Set<EventExtended> rows = new HashSet<EventExtended>();
        readEventExtended(inputFile, rows);
        return rows;
    }

    /**
     * Read a Set of EventExtended objects from a File. No descendants are loaded (assocs, origerrs,
     * etc.). Records that begin with '#' are ignored.
     *
     * @param inputFile
     * @param HashSet   of EventExtended objects to which the new EventExtended objects will be added.
     *                  This Set is not cleared before additions.
     * @throws IOException
     */
    static public void readEventExtended(File inputFile, Set<EventExtended> rows) throws IOException {
        if (inputFile.exists()) {
            BufferedReader input = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#"))
                    rows.add(new EventExtended(new Scanner(line)));
            }
            input.close();
        }
    }

    /**
     * Load a set of EventExtended objects and associated descendants from ascii files.
     *
     * @param inputFiles map from row type to File object. inputFiles keys may include orign, origerr,
     *                   azgap, assoc and arrival.
     * @param network    3 possibilities:
     *                   <ul>
     *                   <li>if network is populated with sites, then sites from the network will be attached to
     *                   arrivals and any site file specified in inputFiles will be ignored.
     *                   <li>if network is null, and a site file is specified in inputFiles then sites from the
     *                   input file will be attached to arrivals.
     *                   <li>if network is empty, and a site file is specified in inputFiles then network will be
     *                   populated with sites from the site file and those sites will be attached to arrivals.
     *                   </ul>
     * @param evids      subset of evids that should actually be loaded. If none are specified all are
     *                   loaded.
     * @return events a HashSet of EventExtended objects
     * @throws IOException
     */
    static public Set<EventExtended> readEventExtended(HashMap<String, File> inputFiles,
                                                       NetworkExtended network, long... evids) throws IOException {
        Set<EventExtended> events = new HashSet<EventExtended>();
        EventExtended.readEventExtended(inputFiles, network, events, evids);
        return events;
    }

    /**
     * Load a set of EventExtended objects from ascii files.
     *
     * @param inputFiles map from row type to File object. inputFiles keys may include orign, origerr,
     *                   azgap, assoc and arrival.
     * @param network    3 possibilities:
     *                   <ul>
     *                   <li>if network is populated with sites, then sites from the network will be attached to
     *                   arrivals and any site file specified in inputFiles will be ignored.
     *                   <li>if network is null, and a site file is specified in inputFiles then sites from the
     *                   input file will be attached to arrivals.
     *                   <li>if network is empty, and a site file is specified in inputFiles then network will be
     *                   populated with sites from the site file and those sites will be attached to arrivals.
     *                   </ul>
     * @param events     a Collection of EventExtended objects to which events from the inputFiles will be
     *                   added. This Collection is not cleared before population.
     * @param evids      subset of evids that should actually be loaded. If none are specified all are
     *                   loaded.
     * @throws IOException
     */
    static public void readEventExtended(HashMap<String, File> inputFiles, NetworkExtended network,
                                         Collection<EventExtended> events, long... evids) throws IOException {
        File eventFile = inputFiles.get("event");
        if (eventFile == null)
            eventFile = inputFiles.get("Event");

        if (eventFile == null)
            return;

        HashSet<Long> evidSet = new HashSet<Long>(evids.length);
        for (long evid : evids)
            evidSet.add(evid);

        HashMap<Long, EventExtended> eventSet = new HashMap<Long, EventExtended>();

        for (EventExtended event : EventExtended.readEventExtended(eventFile))
            if (evidSet.size() == 0 || evidSet.contains(event.getEvid()))
                eventSet.put(event.getEvid(), event);

        events.addAll(eventSet.values());

        if (inputFiles.size() > 1) {
            for (OriginExtended origin : OriginExtended.readOriginExtended(inputFiles, network)) {
                EventExtended event = eventSet.get(origin.getEvid());
                if (event != null) {
                    event.getOrigins().put(origin.getOrid(), origin);
                    if (event.getPrefor() == origin.getOrid())
                        event.setPreferredOrigin(origin);
                    ;
                }
            }

        }
    }

    /**
     * Update events based on their evids
     *
     * @param connection
     * @param tableName
     * @param events
     * @param lddate
     * @param commit
     * @throws SQLException
     */
    static public void updateEventsOnEvid(Connection connection, String tableName,
                                          Collection<? extends EventExtended> events, java.util.Date lddate, boolean commit)
            throws SQLException {
        PreparedStatement statement = null;
        try {
            String sql = "";
            for (EventExtended event : events) {
                sql = "update " + tableName
                        + " set evid = ?, evname = ?, prefor = ?, auth = ?, commid = ?, lddate = ? where evid = ?";
                statement = connection.prepareStatement(sql);
                int i = 0;
                statement.setLong(++i, event.getEvid());
                statement.setString(++i, event.getEvname());
                statement.setLong(++i, event.getPrefor());
                statement.setString(++i, event.getAuth());
                statement.setLong(++i, event.getCommid());
                statement.setTimestamp(++i, new java.sql.Timestamp(lddate.getTime()));
                statement.setLong(++i, event.getEvid());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
            if (commit)
                connection.commit();
        } finally {
            if (statement != null)
                statement.close();
        }
    }

    /**
     * Update an event based on its evid
     *
     * @param connection
     * @param tableName
     * @param event
     * @param lddate
     * @param commit
     * @throws SQLException
     */
    static public void updateEventOnEvid(Connection connection, String tableName, Event event,
                                         java.util.Date lddate, boolean commit) throws SQLException {
        PreparedStatement statement = null;
        try {
            String sql = "update " + tableName
                    + " set evid = ?, evname = ?, prefor = ?, auth = ?, commid = ?, lddate = ? where evid = ?";
            statement = connection.prepareStatement(sql);
            int i = 0;
            statement.setLong(++i, event.getEvid());
            statement.setString(++i, event.getEvname());
            statement.setLong(++i, event.getPrefor());
            statement.setString(++i, event.getAuth());
            statement.setLong(++i, event.getCommid());
            statement.setTimestamp(++i, new java.sql.Timestamp(lddate.getTime()));
            statement.setLong(++i, event.getEvid());
            statement.addBatch();

            statement.executeBatch();
            statement.close();
            if (commit)
                connection.commit();
        } finally {
            if (statement != null)
                statement.close();
        }
    }


    /**
     * Write an entire Set of EventExtended to an ascii file. If <it>outputFiles</it> contains entries
     * for descendants, then they are written to separate files, as appropriate. For examples, if
     * outputFiles.get("assoc") = File f, then all the assocs associated with all origins are written
     * to File f.
     *
     * @param events
     * @param outputFiles map from row type to File object. outputFiles keys may include event,
     *                    origin, origerr, azgap, assoc and arrival.
     * @throws IOException
     */
    static public void writeEventExtended(Collection<? extends EventExtended> events,
                                          Map<String, File> outputFiles) throws IOException {
        File outputFile = outputFiles.get("event");
        if (outputFile == null)
            outputFile = outputFiles.get("Event");

        if (outputFile != null) {
            BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
            output.write(Event.getHeader() + "\n");
            for (EventExtended event : events)
                event.writeln(output);
            output.close();
        }

        if (outputFiles.containsKey("origin") || outputFiles.containsKey("Origin")) {
            ArrayList<OriginExtended> origins = new ArrayList<OriginExtended>(events.size());
            for (EventExtended event : events)
                origins.addAll(event.getOrigins().values());

            if (origins.size() > 0)
                OriginExtended.writeOriginExtendeds(origins, outputFiles);
        }
    }

    /**
     * Write an event and all associated rowgraph data to the outputSchema.
     *
     * @param events
     * @param outputSchema
     * @param commit       if true, a commit is executed after all data is successfully written to the db.
     * @return number of rows of each tableType written to db. null indicates that a SQL exception
     * occurred, in which case a connection.rollback() was executed so no data was written to
     * the db.
     * @throws Exception
     */
    static public LinkedHashMap<String, Integer> writeEventExtended(EventExtended event,
                                                                    Schema outputSchema, boolean commit) throws Exception {
        ArrayList<EventExtended> e = new ArrayList<EventExtended>();
        e.add(event);
        return writeEventExtended(e, outputSchema, commit);
    }

    /**
     * Write a collection of events and all associated rowgraph data to the output Schema.
     *
     * @param events
     * @param outputSchema
     * @param commit       if true, a commit is executed after all data is successfully written to the db.
     * @return number of rows of each tableType written to db. null indicates that a SQL exception
     * occurred, in which case a connection.rollback() was executed so no data was written to
     * the db.
     * @throws Exception
     */
    static public LinkedHashMap<String, Integer> writeEventExtended(
            Collection<? extends EventExtended> events, Schema outputSchema, boolean commit)
            throws Exception {
        java.util.Date lddate = new java.util.Date(System.currentTimeMillis());

        String eventTable = outputSchema.getTableName("Event");
        if (eventTable != null)
            Event.write(outputSchema.getConnection(), eventTable, events, lddate, false);

        LinkedHashMap<String, Integer> counts = null;

        if (outputSchema.isSupported("Origin")) {
            ArrayList<OriginExtended> origins = new ArrayList<OriginExtended>(events.size());
            for (EventExtended event : events)
                // origins.addAll(event.getOrigins().values());
                if (event.getPreferredOrigin() != null)
                    origins.add(event.getPreferredOrigin());

            if (origins.size() > 0)
                counts = OriginExtended.writeOriginExtendeds(origins, outputSchema, false);
        }

        if (commit)
            outputSchema.commit();

        if (counts == null)
            counts = new LinkedHashMap<String, Integer>();

        counts.put("Event", events.size());

        return counts;
    }


    static public LinkedHashMap<String, Integer> getCounts(Collection<? extends EventExtended> events,
                                                           boolean print) throws Exception {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();

        counts.put("Event", events.size());

        ArrayList<OriginExtended> origins = new ArrayList<OriginExtended>(2 * events.size());
        for (EventExtended event : events)
            origins.addAll(event.getOrigins().values());

        for (Entry<String, Integer> entry : OriginExtended.getCounts(origins, false).entrySet())
            counts.put(entry.getKey(), entry.getValue());

        if (print)
            for (Entry<String, Integer> entry : counts.entrySet())
                System.out.printf("%-10s %6d%n", entry.getKey(), entry.getValue());

        return counts;
    }


    public void print(String... tables) {
        LinkedHashSet<String> t = new LinkedHashSet<String>(tables.length);
        for (String s : tables)
            t.add(s.toLowerCase());
        if (t.contains("event")) {
            System.out.printf("Event : %4d  %4d%n", getEvid(), getPrefor());
            if (t.contains("origin")) {
                for (OriginExtended origin : getOrigins().values()) {
                    System.out.printf("    Origin: %s%n", origin);
                    if (t.contains("assoc")) {
                        for (AssocExtended assoc : origin.getAssocs().values()) {
                            System.out.printf("        Assoc  : %s%n", assoc);
                            if (t.contains("arrival"))
                                System.out.printf("        Arrival: %s%n", assoc.getArrival());
                            if (t.contains("site"))
                                System.out.printf("        Site   : %s%n", assoc.getArrival().getSite());
                        }
                    }
                }
            }
        }
    }

    /**
     * Sort the supplied List of Events by Evid.
     *
     * @param events
     */
    static public void sortByEvid(List<? extends Event> events) {
        Collections.sort(events, sortByEvid);
    }

    /**
     * Sort the supplied List of Events by Evid.
     *
     * @param events
     */
    static public void sortByPrefor(List<? extends Event> events) {
        Collections.sort(events, sortByPrefor);
    }

    /**
     * Sort the supplied List of Events by the time of the preferred origin.
     *
     * @param events
     */
    static public void sortByPreforTime(List<? extends EventExtended> events) {
        Collections.sort(events, sortByPreforTime);
    }

    /**
     * @return the epoch time of the preferred origin, or NaN if preferredOrigin is null.
     */
    public double getTime() {
        return preferredOrigin == null ? Double.NaN : preferredOrigin.getTime();
    }


    /**
     * Generate a sql script to create a table of type Site in the database
     *
     * @param tableName
     * @throws SQLException
     */
    static public ArrayList<String> createTableScript(String tableName, boolean addUniqueKey)
            throws SQLException {
        ArrayList<String> script = new ArrayList<String>();
        StringBuffer buf = new StringBuffer();
        buf.append("create table " + tableName + " (\n");
        buf.append("evid         number(9)            NOT NULL,\n");
        buf.append("evname       varchar2(32)         NOT NULL,\n");
        buf.append("prefor       number(9)            NOT NULL,\n");
        buf.append("auth         varchar2(20)         NOT NULL,\n");
        buf.append("commid       number(9)            NOT NULL,\n");
        buf.append("lddate       date                 NOT NULL,\n");
        buf.append("constraint " + tableName + "_pk primary key (evid)\n");
        if (addUniqueKey) {
            buf.append(",\n");
            buf.append("constraint " + tableName + "_uk unique (prefor)\n");
        }
        buf.append(")");
        script.add(buf.toString());
        script.add("grant select on " + tableName + " to public");
        return script;
    }

    /**
     * Generate a String representation of this EventExtended object and optionally its children.
     * <ol start=0>
     * <li>origins: if true print origins (preferred first)
     * <li>assoc: if true print assocs
     * <li>arrival: if true print arrivals
     * <li>site: if true print sites
     * <li>origerr: if true print origerr
     * <li>azgap: if true print azgap
     * </ol>
     *
     * @param includeChildren
     * @return
     */
    public String toString(boolean... includeChildren) {

        boolean printOrigins = includeChildren.length > 0 && includeChildren[0];
        boolean printAssoc = includeChildren.length > 1 && includeChildren[1];
        boolean printArrival = includeChildren.length > 2 && includeChildren[2];
        boolean printSite = includeChildren.length > 3 && includeChildren[3];
        boolean printOrigerr = includeChildren.length > 4 && includeChildren[4];
        boolean printAzgap = includeChildren.length > 5 && includeChildren[5];

        StringBuffer out = new StringBuffer();
        out.append(String.format("Event:  evid=%-10d prefor=%-10d auth=%-15s evname=%s commid=%d%n",
                getEvid(), getPrefor(), getAuth(), getEvname(), getCommid()));
        if (printOrigins) {
            if (preferredOrigin != null)
                out.append(preferredOrigin.toString(printAssoc, printArrival, printSite, printOrigerr,
                        printAzgap));
            for (OriginExtended o : origins.values())
                if (!o.equals(preferredOrigin))
                    out.append(o.toString(printAssoc, printArrival, printSite, printOrigerr, printAzgap));
        }
        return out.toString();
    }

    /**
     * Compare the origins in this event and other event. If the number of origins in the two events
     * is different returns false. For each pair of origins, compute similarity. If the locations are
     * different, times are different or assoc sets are different, returns false. If the similarity
     * for all origins is close to 1, returns true.
     *
     * @param other
     * @return
     */
    public boolean equalOrigins(EventExtended other) {
        if (this.origins.size() != other.origins.size())
            return false;
        for (OriginExtended thisOrigin : origins.values()) {
            boolean equal = false;
            for (OriginExtended otherOrigin : other.getOrigins().values())
                if (thisOrigin.isIdentical(otherOrigin)) {
                    equal = true;
                    break;
                }
            if (!equal)
                return false;
        }
        return true;
    }

    public OriginExtended getOrigin(long orid) {
        return origins.get(orid);
    }

}
