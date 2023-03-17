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
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.Date;
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
import java.util.TreeMap;
import java.util.TreeSet;

import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gmp.util.time.TimeInterface;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origerr;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_custom.Azgap;

public class OriginExtended extends Origin implements TimeInterface, Cloneable {

    private static final long serialVersionUID = 1L;

    /**
     * The unit vector that represents the geographic location of this Origin
     */
    private double[] unitVector;

    /**
     * The distance from the center of the earth to the location of this origin, in km.
     */
    private double radius;

    /**
     * The radius of the earth at the latitude of this origin, in km.
     */
    private double earthRadius;

    /**
     * The Map from arid to AssocExtended that includes entries for all of the Arrivals associated to
     * this Origin.
     */
    protected Map<Long, AssocExtended> assocs;

    /**
     * The Origerr object that contains the uncertainty information for this Origin
     */
    private Origerr origerr;

    private Azgap azgap;

    public static void main(String[] args)
    {
	try {
	    PropertiesPlus properties = new PropertiesPlus(new File(args[0]));

	    List<OriginExtended> origins = new ArrayList<OriginExtended>();
	    NetworkExtended network = new NetworkExtended();

	    if (properties.getProperty("dataLoaderTypeInput", "").equals("oracle"))
	    {
		Schema schema = new Schema("dbInput", properties, false);

		System.out.println(schema.toString());

		String dbInputWhereClause = properties.getProperty("dbInputWhereClause", "");
		if (dbInputWhereClause.isEmpty())
		    throw new Exception("must specify property dbInputWhereClause");

		String dbInputAssocClause = properties.getProperty("dbInputAssocClause");

		ArrayList<String> sql = new ArrayList<String>();

		origins.addAll(OriginExtended.readOriginExtended(
			schema, network, dbInputWhereClause, dbInputAssocClause, sql).values());

		sortByOrid(origins);

		System.out.println("SQL:");
		for (String s : sql) System.out.println(s);
		System.out.println();

		for (Entry<String, Integer> entry : count(origins).entrySet())
		    if (entry.getValue() > 0)
			System.out.printf("Loaded %6d %-9s from %s%n",
				entry.getValue(), (entry.getKey()+"s"), 
				schema.getTableName(entry.getKey()));
		System.out.println();
	    }
	    else if (properties.getProperty("dataLoaderTypeInput", "").equals("file"))
	    {
		Map<String, File> inputFiles = new LinkedHashMap<String, File>();

		for (String type : new String[]{"Origin", "Origerr", "Azgap", "Assoc", "Arrival", "Site"})
		{
		    String property = "dataLoaderFileInput"+type+"s";
		    if (properties.containsKey(property))
			inputFiles.put(type, properties.getFile(property));
		}

		origins.addAll(OriginExtended.readOriginExtended(inputFiles, null));
		sortByOrid(origins);

		Map<String, Integer> counts = count(origins);

		for (String type : new String[]{"Origin", "Origerr", "Azgap", "Assoc", "Arrival", "Site"})
		{
		    if (counts.get(type.toLowerCase()) > 0)
			System.out.printf("Loaded %6d %-9s from %s%n",
				counts.get(type.toLowerCase()), (counts.get(type.toLowerCase())+"s"), 
				inputFiles.get(type));
		}
		System.out.println();
	    }

	    for (OriginExtended o : origins) { o.setNdef(); o.setNass();  }

	    if (properties.getProperty("dataLoaderTypeOutput", "").equals("oracle"))
	    {
		Schema schema = new Schema("dbOutput", properties, true);

		System.out.println(schema.toString());

		OriginExtended.writeOriginExtendeds(origins, schema, true);

		Map<String, Integer> counts = count(origins);

		for (Entry<String, Integer> entry : counts.entrySet())
		    //if (entry.getValue() > 0)
		    System.out.printf("Wrote %6d %-9s to %s%n",
			    entry.getValue(), (entry.getKey()+"s"), 
			    schema.getTableName(entry.getKey()));
		System.out.println();
	    }
	    else if (properties.getProperty("dataLoaderTypeOutput", "").equals("file"))
	    {
		Map<String, File> outputFiles = new LinkedHashMap<String, File>();

		Map<String, Integer> count = new LinkedHashMap<String, Integer>();

		for (String type : new String[]{"Origin", "Origerr", "Azgap", "Assoc", "Arrival", "Site"})
		{
		    String property = "dataLoaderFileOutput"+type+"s";
		    if (properties.containsKey(property))
		    {
			outputFiles.put(type, properties.getFile(property));
			count.put(type, 0);
		    }
		}

		for (File outputFile : outputFiles.values())
		    outputFile.getParentFile().mkdirs();

		OriginExtended.writeOriginExtendeds(origins, outputFiles);

		for (OriginExtended o : origins)
		{
		    if (count.containsKey("Origin"))
			count.put("Origin", count.get("Origin")+1);
		    if (count.containsKey("Assoc"))
			count.put("Assoc", count.get("Assoc")+o.getAssocs().size());
		    if (count.containsKey("Arrival"))
			count.put("Arrival", count.get("Arrival")+o.getAssocs().size());
		    if (count.containsKey("Origerr"))
			count.put("Origerr", count.get("Origerr")+
				(o.getOrigerr() == null ? 0 : 1));
		    if (count.containsKey("Azgap"))
			count.put("Azgap", count.get("Azgap")+
				(o.getAzgap() == null ? 0 : 1));
		}

		count.put("Site", network.size());

		for (String type : new String[]{"Origin", "Origerr", "Azgap", "Assoc", "Arrival", "Site"})
		{
		    if (count.containsKey(type) && count.get(type) > 0)
			System.out.printf("Wrote %6d %-9s to %s%n", 
				count.get(type), type+"s", outputFiles.get(type));
		}
		System.out.println();
	    }
	    System.out.println("Done");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    static public Comparator<Origin> sortByOrid = new Comparator<Origin>() {
	@Override
	public int compare(Origin o1, Origin o2) {
	    if (o1.equals(o2))
		return 0;
	    int order = (int) Math.signum(o1.getOrid() - o2.getOrid());
	    return order >= 0 ? 1 : -1;
	}
    };

    static public Comparator<Origin> sortByEvid = new Comparator<Origin>() {
	@Override
	public int compare(Origin o1, Origin o2) {
	    if (o1.equals(o2))
		return 0;
	    int order = (int) Math.signum(o1.getEvid() - o2.getEvid());
	    return order >= 0 ? 1 : -1;
	}
    };

    static public Comparator<Origin> sortByTime = new Comparator<Origin>() {
	@Override
	public int compare(Origin o1, Origin o2) {
	    if (o1.equals(o2))
		return 0;
	    int order = (int) Math.signum(o1.getTime() - o2.getTime());
	    return order >= 0 ? 1 : -1;
	}
    };

    static public Comparator<Origin> sortByNdef = new Comparator<Origin>() {
	@Override
	public int compare(Origin o1, Origin o2) {
	    if (o1.equals(o2))
		return 0;
	    int order = (int) Math.signum(o1.getNdef() - o2.getNdef());
	    return order >= 0 ? 1 : -1;
	}
    };

    static public Comparator<Origin> sortByNass = new Comparator<Origin>() {
	@Override
	public int compare(Origin o1, Origin o2) {
	    if (o1.equals(o2))
		return 0;
	    int order = (int) Math.signum(o1.getNass() - o2.getNass());
	    return order >= 0 ? 1 : -1;
	}
    };

    static public Comparator<Origin> sortByNassDescending = new Comparator<Origin>() {
	@Override
	public int compare(Origin o1, Origin o2) {
	    if (o1.equals(o2))
		return 0;
	    int order = (int) Math.signum(o2.getNass() - o1.getNass());
	    return order; // >= 0 ? 1 : -1;
	}
    };

    static public Comparator<Origin> sortByNdefDescending = new Comparator<Origin>() {
	@Override
	public int compare(Origin o1, Origin o2) {
	    if (o1.equals(o2))
		return 0;
	    int order = (int) Math.signum(o2.getNdef() - o1.getNdef());
	    return order >= 0 ? 1 : -1;
	}
    };

    /**
     * Count the number of rows of each table type.
     * Types include "origin", "assoc", "arrival", "site", "origerr", "azgap".
     * There will be an entry for each type.  Values could be zero.
     * @param origins
     * @return Map<String, Integer>
     */
    static public Map<String, Integer> count(Collection<OriginExtended> origins)
    {
	Map<String, Integer> count = new LinkedHashMap<>();

	for (String s : new String[] {"origin", "assoc", "arrival", "site", "origerr", "azgap"})
	    count.put(s, 0);

	NetworkExtended network = new NetworkExtended();

	for (OriginExtended o : origins)
	{
	    count.put("origin", count.get("origin")+1);

	    for (AssocExtended assoc : o.getAssocs().values())
	    {
		count.put("assoc", count.get("assoc")+1);

		if (assoc.getArrival() != null)
		{
		    count.put("arrival", count.get("arrival")+1);

		    if (assoc.getArrival().getSite() != null)
			network.add(assoc.getArrival().getSite());
		}
	    }

	    if (o.getOrigerr() != null)
		count.put("origerr", count.get("origerr")+1);
	    if (o.getAzgap() != null)
		count.put("azgap", count.get("azgap")+1);
	}
	int n=0;
	for (Set<SiteExtended> set : network.getNetworkSites().values())
	    n += set.size();
	count.put("site", n);
	return count;
    }

    /**
     * All fields set to NA values. unitVector, radius and earthRadius are all set to NaN.
     */
    public OriginExtended() {
	super();
	unitVector = new double[]{Double.NaN, Double.NaN, Double.NaN};
	radius = earthRadius = Double.NaN;
	assocs = new HashMap<Long, AssocExtended>();
    }

    public OriginExtended(double lat, double lon, double depth, double time, long orid, long evid,
	    long jdate, long nass, long ndef, long ndp, long grn, long srn, String etype, double depdp,
	    String dtype, double mb, long mbid, double ms, long msid, double ml, long mlid,
	    String algorithm, String auth, long commid) {
	super(lat, lon, depth, time, orid, evid, jdate, nass, ndef, ndp, grn, srn, etype, depdp, dtype,
		mb, mbid, ms, msid, ml, mlid, algorithm, auth, commid);
	initialize();
    }

    public OriginExtended(double[] unitVector, double radius, double originTime) {
	super(VectorGeo.earthShape.getLatDegrees(unitVector),
		VectorGeo.earthShape.getLonDegrees(unitVector),
		VectorGeo.earthShape.getEarthRadius(unitVector) - radius, originTime, ORID_NA, EVID_NA,
		GMTFormat.getJDate(originTime), 0, 0, NDP_NA, GRN_NA, SRN_NA, ETYPE_NA, DEPDP_NA, DTYPE_NA,
		MB_NA, MBID_NA, MS_NA, MSID_NA, ML_NA, MLID_NA, ALGORITHM_NA, AUTH_NA, COMMID_NA);

	this.unitVector = unitVector;
	this.radius = radius;
	this.earthRadius = radius + getDepth();
	this.assocs = new HashMap<Long, AssocExtended>();
    }

    /**
     * Copy constructor.
     *
     * @param other
     */
    public OriginExtended(OriginExtended other) {
	super(other);
	this.unitVector = other.unitVector.clone();
	this.earthRadius = other.earthRadius;
	this.radius = other.radius;
	this.origerr = other.origerr;
	this.azgap = other.azgap;
	this.assocs = new HashMap<Long, AssocExtended>(other.assocs);
    }

    public OriginExtended(Origin other) {
	super(other);
	initialize();
    }

    public OriginExtended(Scanner input) throws IOException {
	super(input);
	initialize();
    }

    public OriginExtended(DataInputStream input) throws IOException {
	super(input);
	initialize();
    }

    public OriginExtended(ByteBuffer input) {
	super(input);
	initialize();
    }

    public OriginExtended(ResultSet input) throws SQLException {
	super(input);
	initialize();
    }

    public OriginExtended(ResultSet input, int offset) throws SQLException {
	super(input, offset);
	initialize();
    }

    /**
     * Create a clone of this OriginExtended object that references
     * a new HashMap<Long, AssocExtended> containing references to all the original
     * assocs.  Origerr and Azgap references, if not null, are cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
	OriginExtended cloned = (OriginExtended) super.clone();
	cloned.assocs = new HashMap<Long, AssocExtended>(this.assocs);
	cloned.unitVector = this.unitVector.clone();
	if (this.origerr != null)
	    cloned.origerr = (Origerr) this.origerr.clone();
	if (this.azgap != null)
	    cloned.azgap = (Azgap) this.azgap.clone();
	return cloned;
    }

    private void initialize() {
	unitVector = VectorGeo.earthShape.getVectorDegrees(getLat(), getLon());
	earthRadius = VectorGeo.earthShape.getEarthRadius(unitVector);
	radius = earthRadius - getDepth();
	assocs = new HashMap<Long, AssocExtended>();
    }

    /**
     * Create a deep copy of this OriginExtended. The 'depth' of the copy depends on the number and
     * values of the optional boolean arguments:
     * <ol start=0>
     * <li>origerr: if true use a deep copy of origerr
     * <li>azgap: if true make a deep copy of azgap
     * <li>assoc: if true make a deep copy of assoc
     * <li>arrival: if true make a deep copy of arrival
     * <li>site: if true make a deep copy of site
     * </ol>
     *
     * @return
     */
    public OriginExtended getDeepCopy(boolean... depth) {
	// start with a shallow copy
	OriginExtended copy = new OriginExtended(this);

	boolean copyOrigerr = depth.length > 0 && depth[0];
	boolean copyAzgap = depth.length > 1 && depth[1];
	boolean copyAssoc = depth.length > 2 && depth[2];
	boolean copyArrival = depth.length > 3 && depth[3];
	boolean copySite = depth.length > 4 && depth[4];

	if (copyOrigerr && origerr != null)
	    copy.setOrigerr(new Origerr(origerr));
	if (copyAzgap && azgap != null)
	    copy.setAzgap(new Azgap(azgap));

	// loop over all the assocs in this
	if (copyAssoc)
	    for (AssocExtended assoc : assocs.values()) {
		// create a copy of assoc and set its arrival to a copy of arrival.
		AssocExtended assocCopy = new AssocExtended(assoc);
		if (copyArrival) {
		    ArrivalExtended arrival = new ArrivalExtended(assoc.getArrival());
		    assocCopy.setArrival(arrival);
		    if (copySite) {
			SiteExtended site = new SiteExtended(arrival.getSite());
			arrival.setSite(site);
		    }
		}
		copy.assocs.put(assocCopy.getArid(), assocCopy);
	    }
	return copy;
    }

    /**
     * Set the location of this Origin. This.unitVector is set equal to a reference to the specified
     * unitVector. The geometry of member assocs is NOT updated by this call. Call
     * updateAssocGeometry() as necessary.
     *
     * @param unitVector
     * @param radius     in km
     * @return reference to this.
     */
    public OriginExtended setLocation(double[] unitVector, double radius) {
	this.unitVector = unitVector;
	this.radius = radius;
	this.earthRadius = VectorGeo.earthShape.getEarthRadius(unitVector);

	super.setLat(VectorGeo.earthShape.getLatDegrees(unitVector));
	super.setLon(VectorGeo.earthShape.getLonDegrees(unitVector));
	super.setDepth(earthRadius - radius);
	return this;
    }

    /**
     * Change the lat, lon of this origin. The geometry of member assocs is NOT updated by this call.
     * Call updateAssocGeometry() as necessary.
     *
     * @param lat in degrees
     * @param lon in degrees
     * @return reference to this.
     */
    public OriginExtended setLatLon(double lat, double lon) {
	super.setLat(lat);
	super.setLon(lon);
	VectorGeo.earthShape.getVectorDegrees(lat, lon, unitVector);
	earthRadius = VectorGeo.earthShape.getEarthRadius(unitVector);
	radius = earthRadius - getDepth();
	return this;
    }

    /**
     * Change the lat, lon, depth of this origin. The geometry of member assocs is NOT updated by this
     * call. Call updateAssocGeometry() as necessary.
     *
     * @param lat   in degrees
     * @param lon   in degrees
     * @param depth in km below surface of the ellipsoid.
     * @return reference to this.
     */
    public OriginExtended setLatLonDepth(double lat, double lon, double depth) {
	super.setLat(lat);
	super.setLon(lon);
	super.setDepth(depth);
	VectorGeo.earthShape.getVectorDegrees(lat, lon, unitVector);
	earthRadius = VectorGeo.earthShape.getEarthRadius(unitVector);
	radius = earthRadius - depth;
	return this;
    }

    /**
     * Change the latitude of this origin in degrees. If lon is being changed as well, consider
     * calling setLatLon() which is more efficient. The geometry of member assocs is NOT updated by
     * this call. Call updateAssocGeometry() as necessary.
     *
     * @param lat in degrees
     * @return reference to this
     */
    @Override
    public OriginExtended setLat(double lat) {
	super.setLat(lat);
	VectorGeo.earthShape.getVectorDegrees(lat, getLon(), unitVector);
	earthRadius = VectorGeo.earthShape.getEarthRadius(unitVector);
	radius = earthRadius - getDepth();
	return this;
    }

    /**
     * Change the longitude of this origin in degrees. If lat is being changed as well, consider
     * calling setLatLon() which is more efficient. The geometry of member assocs is NOT updated by
     * this call. Call updateAssocGeometry() as necessary.
     *
     * @param lat in degrees
     * @return reference to this
     */
    @Override
    public OriginExtended setLon(double lon) {
	super.setLon(lon);
	VectorGeo.earthShape.getVectorDegrees(getLat(), lon, unitVector);
	return this;
    }

    /**
     * Change the depth of this origin in km.
     *
     * @param depth depth in km below surface of the ellipsoid.
     * @return reference to this.
     */
    @Override
    public OriginExtended setDepth(double depth) {
	super.setDepth(depth);
	radius = earthRadius - depth;
	return this;
    }

    /**
     * Change the epochtime of this origin in seconds. The jdate is also updated with the appropriate
     * value.
     *
     * @param time epochtime of this origin in seconds.
     * @return reference to this.
     */
    @Override
    public OriginExtended setTime(double time) {
	super.setTime(time);
	super.setJdate(GMTFormat.getJDate(time));
	return this;
    }

    /**
     * Set ndef equal to the number of time-defining AssocExtended objects that are members of this
     * OriginExtended object.
     *
     * @return reference to this.
     */
    public OriginExtended setNdef() {
	int n = 0;
	for (AssocExtended assoc : assocs.values())
	    if (assoc.isTimedef())
		++n;
	if (n != getNdef())
	    super.setNdef(n);
	return this;
    }

    /**
     * Set nass equal to the number of AssocExtended objects that are members of this OriginExtended
     * object.
     *
     * @return reference to this.
     */
    public OriginExtended setNass() {
	if (getNass() != assocs.size())
	    super.setNass(assocs.size());
	return this;
    }

    /**
     * Set the orid of this OriginExtended object and also change the orids in all the AssocExtended
     * objects that are members of this OriginExtended object. Note that copies are made of all the
     * assocs before changing their orids.
     *
     * @param orid the new orid
     * @return reference to this.
     */
    public OriginExtended setOrid(long orid) {
	super.setOrid(orid);

	if (origerr != null)
	    origerr.setOrid(orid);
	if (azgap != null)
	    azgap.setOrid(orid);

	for (AssocExtended assoc : new ArrayList<AssocExtended>(getAssocs().values())) {
	    assocs.remove(assoc.getArid());
	    assoc.setOrid(orid);
	    assocs.put(assoc.getArid(), assoc);
	}
	return this;
    }

    /**
     * Load the rowgraph associated with this origin from database tables.
     *
     * @param dbConnection
     * @param inputTables  map from row type to table name. Row types include [ origerr, azgap, assoc,
     *                     arrival, site].
     * @param network      this network will be searched for sites to attach to appropriate arrivals.
     * @return reference to this.
     * @throws SQLException
     */
    public OriginExtended loadRowGraph(Connection dbConnection, HashMap<String, String> inputTables,
	    NetworkExtended network) throws SQLException {
	ResultSet rs, rsa;
	Statement statement = dbConnection.createStatement();
	statement.setFetchSize(1000);

	if (inputTables.get("origerr") == null)
	    origerr = null;
	else {
	    rs = statement.executeQuery(
		    String.format("select * from %s where orid = %d", inputTables.get("origerr"), getOrid()));
	    if (rs.next())
		origerr = new Origerr(rs);
	    rs.close();
	}

	if (inputTables.get("azgap") == null)
	    azgap = null;
	else {
	    rs = statement.executeQuery(
		    String.format("select * from %s where orid = %d", inputTables.get("azgap"), getOrid()));
	    if (rs.next())
		azgap = new Azgap(rs);
	    rs.close();
	}

	if (network == null)
	    network = new NetworkExtended();

	assocs.clear();
	if (inputTables.get("assoc") != null) {
	    PreparedStatement stmtArrival = null;
	    if (inputTables.get("arrival") != null)
		stmtArrival = dbConnection.prepareStatement(
			String.format("select * from %s where arid=?", inputTables.get("arrival")));

	    rs = statement.executeQuery(
		    String.format("select * from %s where orid = %d", inputTables.get("assoc"), getOrid()));
	    while (rs.next()) {
		AssocExtended assoc = new AssocExtended(rs);
		assocs.put(assoc.getArid(), assoc);
		if (stmtArrival != null) {
		    stmtArrival.setLong(1, assoc.getArid());
		    rsa = stmtArrival.executeQuery();
		    if (rsa.next())
			assoc.setArrival(new ArrivalExtended(rsa).setSite(network));
		    rsa.close();
		}
	    }
	    rs.close();
	    if (stmtArrival != null)
		stmtArrival.close();
	}
	statement.close();
	setNdef();
	setNass();
	return this;
    }

    /**
     * Load the rowgraph associated with this origin from database tables.
     *
     * @param schema a Schema that manages a database connection and a map from String tableType to
     *               String tableName. Recognized tableTypes are [ origin, origerr, azgap, assoc, arrival,
     *               site ]
     * @return reference to this
     * @throws SQLException
     */
    public OriginExtended loadRowGraph(Schema schema) throws SQLException {
	ResultSet rs;
	Statement statement = schema.getStatement();

	if (schema.getTableName("origerr") == null)
	    origerr = null;
	else {
	    rs = statement.executeQuery(String.format("select * from %s where orid = %d",
		    schema.getTableName("origerr"), getOrid()));
	    if (rs.next())
		origerr = new Origerr(rs);
	    rs.close();
	}

	if (schema.getTableName("azgap") == null)
	    azgap = null;
	else {
	    rs = statement.executeQuery(String.format("select * from %s where orid = %d",
		    schema.getTableName("azgap"), getOrid()));
	    if (rs.next())
		azgap = new Azgap(rs);
	    rs.close();
	}

	assocs.clear();
	if (schema.getTableName("assoc") != null) {
	    // will use this network temporarily for the purpose of ensuring
	    // that we don't load multiple instances of the same SiteExtended
	    // objects.
	    NetworkExtended network = new NetworkExtended();
	    ResultSet rsa;

	    PreparedStatement stmtArrival = null;
	    if (schema.getTableName("arrival") != null)
		stmtArrival = schema.getConnection().prepareStatement(
			String.format("select * from %s where arid=?", schema.getTableName("arrival")));

	    PreparedStatement stmtSite = null;
	    if (schema.getTableName("site") != null)
		stmtSite = schema.getConnection().prepareStatement(
			String.format("select * from %s where sta=? and ? between ondate and offdate",
				schema.getTableName("site")));

	    rs = statement.executeQuery(String.format("select * from %s where orid = %d",
		    schema.getTableName("assoc"), getOrid()));
	    while (rs.next()) {
		AssocExtended assoc = new AssocExtended(rs);
		assocs.put(assoc.getArid(), assoc);
		if (stmtArrival != null) {
		    stmtArrival.setLong(1, assoc.getArid());
		    rsa = stmtArrival.executeQuery();
		    if (rsa.next()) {
			ArrivalExtended arrival = new ArrivalExtended(rsa);
			rsa.close();
			assoc.setArrival(arrival);
			if (stmtSite != null) {
			    arrival.setSite(network);
			    if (arrival.getSite() == null) {
				stmtSite.setString(1, arrival.getSta());
				stmtSite.setLong(2, arrival.getJdate());
				rsa = stmtSite.executeQuery();
				if (rsa.next()) {
				    SiteExtended site = new SiteExtended(rsa);
				    arrival.setSite(site);
				    network.add(site);
				}
				rsa.close();
			    }
			}
		    }
		}
	    }
	    rs.close();
	    if (stmtArrival != null)
		stmtArrival.close();
	}
	statement.close();
	setNdef();
	setNass();
	return this;
    }

    /**
     * Force an update of delta, seaz and esaz in all the assocs associated with this origin.
     */
    public void updateAssocGeometry() {
	for (AssocExtended assoc : assocs.values())
	    assoc.updateGeometry(this);
	//super.setHash(null);
    }

    /**
     * Retrieve a reference to the unit vector that represents the geographic location of this origin.
     *
     * @return a reference to the unit vector that represents the geographic location of this origin.
     */
    public double[] getUnitVector() {
	return unitVector;
    }

    /**
     * Get the radius of this origin in km relative to surface of the ellipsoid.
     *
     * @return the radius of this origin in km relative to surface of the ellipsoid.
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

    public Origerr getOrigerr() {
	return origerr;
    }

    public void setOrigerr(Origerr origerr) {
	this.origerr = origerr;
	//super.setHash(null);
    }

    public Azgap getAzgap() {
	return azgap;
    }

    public void setAzgap(Azgap azgap) {
	this.azgap = azgap;
	//super.setHash(null);
    }

    /**
     * Retrieve a reference to the set of assocs owned by this OriginExtended object. Can never be
     * null but may be empty. Map from arid -> assoc
     *
     * @return
     */
    public Map<Long, AssocExtended> getAssocs() {
	return assocs;
    }

    /**
     * Add the supplied assoc and update nass and ndef as appropriate.
     *
     * @param assoc
     * @return true if assocs.size() increased.
     */
    public boolean addAssoc(AssocExtended assoc) {
	assoc.setOrigin(this);
	int size = assocs.size();
	assocs.put(assoc.getArid(), assoc);
	setNass();
	setNdef();
	return size != assocs.size();
    }

    /**
     * Add all the specified assocs to this origin's assoc set. Nass and ndef are updated as needed.
     * The orid of each newAssoc is set to the orid of this OriginExtended.
     *
     * @param newAssocs
     */
    public void addAssocs(Collection<? extends AssocExtended> newAssocs) {
	for (AssocExtended assoc : newAssocs) {
	    assoc.setOrid(getOrid());
	    assocs.put(assoc.getArid(), assoc);
	}
	setNass();
	setNdef();
    }

    /**
     * Remove all the assocs linked to the specified arrivals.
     *
     * @param arrivals
     * @return number of arrivals removed.
     */
    public int removeArrivals(Collection<? extends ArrivalExtended> arrivals) {
	long oldNass = getNass();

	Iterator<Entry<Long, AssocExtended>> it = assocs.entrySet().iterator();
	while (it.hasNext()) {
	    Entry<Long, AssocExtended> entry = it.next();
	    long arid = entry.getKey();
	    AssocExtended assoc = entry.getValue();

	    boolean found = false;
	    for (ArrivalExtended arrival : arrivals) {
		if (arid == arrival.getArid()) {
		    it.remove();
		    found = true;
		    break;
		}
	    }
	    if (!found)
		for (ArrivalExtended arrival : arrivals) {
		    if (assoc.getArrival().isSimilar(arrival)) {
			it.remove();
			break;
		    }
		}
	}
	setNass();
	setNdef();

	return (int) (oldNass - getNass());
    }

    /**
     * Remove the assoc with the specified arid and update nass and ndef as appropriate.
     *
     * @param arid
     * @return the removed AssocExtended, or null.
     */
    public AssocExtended removeAssoc(Long arid) {
	AssocExtended removedAssoc = getAssocs().remove(arid);
	if (removedAssoc != null && removedAssoc.isTimedef())
	    setNdef(getNdef() - 1);
	setNass(getAssocs().size());
	return removedAssoc;
    }

    /**
     * Remove the specified assoc and update nass and ndef as appropriate.
     *
     * @param assoc
     * @return true if assocs.size() changed.
     */
    public boolean removeAssoc(AssocExtended assoc) {
	if (assocs.remove(assoc.getArid()) != null) {
	    setNass(getNass() - 1);
	    if (assoc.isTimedef())
		setNdef(getNdef() - 1);
	    return true;
	}
	return false;
    }

    /**
     * Remove the specified assocs and update nass and ndef as appropriate.
     *
     * @param assoc
     * @return true if assocs.size() changed.
     */
    public boolean removeAssocs(Collection<? extends AssocExtended> assocCollection) {
	boolean changed = false;
	for (AssocExtended assoc : assocCollection)
	    if (removeAssoc(assoc))
		changed = true;
	return changed;
    }

    /**
     * Clear the assocs and set ndef and nass to zero.
     *
     * @return reference to this
     */
    public OriginExtended clearAssocs() {
	assocs.clear();
	setNdef(0);
	setNass(0);
	return this;
    }

    //  @Override
    //  public BigInteger getHash() {
    //    if (isHashNull()) {
    //      super.getHash();
    //      if (origerr != null)
    //        incrementHash(origerr.getHash());
    //      if (azgap != null)
    //        incrementHash(azgap.getHash());
    //      for (AssocExtended assoc : assocs.values())
    //        incrementHash(assoc.getHash());
    //    }
    //    return super.getHash();
    //  }

    //  @Override
    //  public boolean equals(Object o) {
    //    if (this == o) {
    //      return true;
    //    }
    //    if (o == null || o.getClass() != this.getClass()) {
    //      return false;
    //    }
    //    return super.equals((Origin)o) && 
    //        this.assocs.keySet().equals(((OriginExtended) o).assocs.keySet());
    //  }
    //
    //  @Override
    //  public int hashCode() {
    //    return super.hashCode()+assocs.keySet().hashCode();
    //  }

    public HashMap<Long, ArrivalExtended> getArrivals() {
	HashMap<Long, ArrivalExtended> arrivals = new HashMap<Long, ArrivalExtended>(assocs.size());
	for (AssocExtended assoc : assocs.values())
	    arrivals.put(assoc.getArrival().getArid(), assoc.getArrival());
	return arrivals;
    }

    /**
     * Retrieve the assoc that has the arrival with the earliest arrival time.
     *
     * @return
     */
    public AssocExtended getFirstArrival() {
	if (assocs.size() == 0)
	    return null;

	Iterator<AssocExtended> it = assocs.values().iterator();
	AssocExtended firstArrival = it.next();
	while (it.hasNext()) {
	    AssocExtended assoc = it.next();
	    if (assoc.getArrival() != null
		    && assoc.getArrival().getTime() < firstArrival.getArrival().getTime())
		firstArrival = assoc;
	}
	return firstArrival;
    }

    /**
     * Return the event commonality score of this and other origin. Value will be between zero and
     * one, with 1 implying the events are identical. Both spatial/temporal proximity and common
     * assocs are considered.
     * <p>
     * Uses default values for distanceWeight (0.5), distanceScaleFactor (10 deg), timeScaleFactor
     * (200 seconds), radiusScaleFactor (infinity km)
     *
     * @param other
     * @return similarity, a number between 0 and 1.
     */
    public double getSimilarity(OriginExtended other) {
	return getSimilarity(other, 0.5);
    }

    /**
     * Return true if the event commonality score of this and other origin is close to 1. Both
     * spatial/temporal proximity and common assocs are considered.
     * <p>
     * Uses default values for distanceWeight (0.5), distanceScaleFactor (10 deg), timeScaleFactor
     * (200 seconds), radiusScaleFactor (infinity km)
     *
     * @param other
     * @return true if the event commonality score of this and other origin is close to 1.
     */
    public boolean isIdentical(OriginExtended other) {
	return isIdentical(other, 0.5);
    }

    /**
     * Return the event commonality score of this and other origin. Value will be between zero and
     * one, with 1 implying the events are identical. Both spatial/temporal proximity and common
     * assocs are considered.
     * <p>
     * Uses specified value of distanceWeight and default values for distanceScaleFactor (10 deg),
     * radiusScaleFactor (infinity km), and timeScaleFactor (200 seconds)
     *
     * @param other
     * @param distanceWeight number between 0 and 1 that controls the relative weight given to
     *                       (distance,time) as opposed to overlapping assocs. If 1 only (distance,time) are
     *                       considered and overlapping assocs have no effect. If 0, only common assocs are
     *                       considered.
     * @return true if the event commonality score of this and other origin is close to 1.
     */
    public double getSimilarity(OriginExtended other, double distanceWeight) {
	return getSimilarity(other, distanceWeight, 10., Double.POSITIVE_INFINITY, 200.);
    }

    /**
     * Return true if the event commonality score of this and other origin is close to 1. Both
     * spatial/temporal proximity and common assocs are considered.
     * <p>
     * Uses specified value of distanceWeight and default values for distanceScaleFactor (10 deg),
     * radiusScaleFactor (infinity km), and timeScaleFactor (200 seconds)
     *
     * @param other
     * @param distanceWeight number between 0 and 1 that controls the relative weight given to
     *                       (distance,time) as opposed to overlapping assocs. If 1 only (distance,time) are
     *                       considered and overlapping assocs have no effect. If 0, only common assocs are
     *                       considered.
     * @return true if the event commonality score of this and other origin is close to 1.
     */
    public boolean isIdentical(OriginExtended other, double distanceWeight) {
	return isIdentical(other, distanceWeight, 10., Double.POSITIVE_INFINITY, 200.);
    }

    /**
     * Return the event commonality score of this and other origin. Value will be between zero and
     * one, with 1 implying the events are identical. Both spatial/temporal proximity and common
     * assocs are considered.
     *
     * @param other
     * @param distanceWeight      number between 0 and 1 that controls the relative weight given to
     *                            (distance,time) as opposed to overlapping assocs. If 1 only (distance,time) are
     *                            considered and overlapping assocs have no effect. If 0, only common assocs are
     *                            considered.
     * @param distanceScaleFactor in degrees
     * @param radiusScaleFactor   in km.
     * @param timeScaleFactor     in seconds.
     * @return similarity, a number between 0 and 1.
     */
    public double getSimilarity(OriginExtended other, double distanceWeight,
	    double distanceScaleFactor, double radiusScaleFactor, double timeScaleFactor) {
	double ecs = 0.;
	if (distanceWeight > 0.) {
	    double dx = VectorGeo.angleDegrees(this.unitVector, other.unitVector) / distanceScaleFactor;

	    double dr = (radius - other.radius) / radiusScaleFactor;

	    double dt = (getTime() - other.getTime()) / timeScaleFactor; // 80 seconds is ak135 travel
	    // time from 700 km to surface

	    ecs = distanceWeight * Math.exp(-(dx * dx + dr * dr + dt * dt));
	}

	if (distanceWeight < 1.)
	    ecs += (1. - distanceWeight) * jaccard(other, true);

	return ecs;
    }

    /**
     * Return true if the event commonality score of this and other origin is close to 1. Both
     * spatial/temporal proximity and common assocs are considered.
     *
     * @param other
     * @param distanceWeight      number between 0 and 1 that controls the relative weight given to
     *                            (distance,time) as opposed to overlapping assocs. If 1 only (distance,time) are
     *                            considered and overlapping assocs have no effect. If 0, only common assocs are
     *                            considered.
     * @param distanceScaleFactor in degrees
     * @param radiusScaleFactor   in km.
     * @param timeScaleFactor     in seconds.
     * @return true if the event commonality score of this and other origin is close to 1.
     */
    public boolean isIdentical(OriginExtended other, double distanceWeight,
	    double distanceScaleFactor, double radiusScaleFactor, double timeScaleFactor) {
	return Math.abs(getSimilarity(other, distanceWeight, distanceScaleFactor, radiusScaleFactor,
		timeScaleFactor) - 1.) < 1e-9;
    }


    /**
     * Return the event commonality score of this and other origin. Value will be between zero and
     * one, with 1 implying the events are identical. Both spatial/temporal proximity, common
     * time-defining assocs, and all common assocs are considered.
     *
     * @param other
     * @param distanceWeight      number between 0 and 1 that controls the relative weight given to
     *                            (distance,time) as opposed to overlapping assocs. If 1, only (distance,time) are
     *                            considered and overlapping assocs have no effect. If 0, only common assocs are
     *                            considered.
     * @param timedefWeight       number between 0 and 1 that controls the relative weight given to
     *                            overlapping time-defining assocs.
     * @param allAssocsWeight     number between 0 and 1 that controls the relative weight given to
     *                            overlapping assocs (includes all assocs, not just time-defining).
     * @param distanceScaleFactor in degrees
     * @param radiusScaleFactor   in km.
     * @param timeScaleFactor     in seconds.
     * @return similarity, a number between 0 and 1.
     * @throws Exception
     */
    public double getSimilarity(OriginExtended other, double distanceWeight, double timedefWeight,
	    double allAssocsWeight, double distanceScaleFactor, double radiusScaleFactor,
	    double timeScaleFactor) throws Exception {
	double totalWeight = distanceWeight + timedefWeight + allAssocsWeight;

	if (Math.abs(totalWeight - 1) > 1e-12)
	    throw new Exception("Weights do not sum to 1:  " + totalWeight);

	double ecs = 0.;
	if (distanceWeight > 1e-12) {
	    double dx = VectorGeo.angleDegrees(this.unitVector, other.unitVector) / distanceScaleFactor;

	    double dr = (radius - other.radius) / radiusScaleFactor;

	    double dt = (getTime() - other.getTime()) / timeScaleFactor; // 80 seconds is ak135 travel
	    // time from 700 km to surface

	    ecs = distanceWeight * Math.exp(-(dx * dx + dr * dr + dt * dt));
	}

	if (timedefWeight > 1e-12)
	    ecs += timedefWeight * jaccard(other, false); // time defining only

	if (allAssocsWeight > 1e-12)
	    ecs += allAssocsWeight * jaccard(other, true); // all assocs

	return ecs;
    }

    /**
     * find the Jaccard Similarity of the arrivals associated with each set of assocs, i.e., the ratio
     * of the size of the intersection to the size of the union.
     *
     * @param other
     * @param allAssocs if true, use all assocs. If false, use only time defining assocs.
     * @return
     */
    private double jaccard(OriginExtended other, boolean allAssocs) {
	Set<ArrivalExtended> thisArrivals = new HashSet<ArrivalExtended>(assocs.size());
	for (AssocExtended assoc : assocs.values())
	    if (allAssocs || assoc.isTimedef())
		thisArrivals.add(assoc.getArrival());

	Set<ArrivalExtended> otherArrivals = new HashSet<ArrivalExtended>(other.assocs.size());
	for (AssocExtended assoc : other.assocs.values())
	    if (allAssocs || assoc.isTimedef())
		otherArrivals.add(assoc.getArrival());

	if (thisArrivals.isEmpty() && otherArrivals.isEmpty())
	    // if both are empty, then similarity based on assocs is 1
	    return 1.;

	if (thisArrivals.isEmpty() != otherArrivals.isEmpty())
	    // if one is empty and the other is not, similarity is 0.
	    return 0.;

	double similarityThreshold = 0.01;
	int intersectionCount = 0;
	int disjointCount = 0;
	for (ArrivalExtended arrival : thisArrivals) {
	    if (arrival != null) {
		Entry<Double, ArrivalExtended> mostSimilar =
			arrival.sortBySimilarity(otherArrivals).entrySet().iterator().next();
		if (mostSimilar.getKey() >= similarityThreshold) {
		    intersectionCount++;
		} else {
		    disjointCount++;
		}
	    }
	}

	for (ArrivalExtended arrival : otherArrivals) {
	    if (arrival != null) {
		Entry<Double, ArrivalExtended> mostSimilar =
			arrival.sortBySimilarity(thisArrivals).entrySet().iterator().next();
		if (mostSimilar.getKey() < similarityThreshold) {
		    disjointCount++;
		}
	    }
	}

	return (double) intersectionCount / (double) (intersectionCount + disjointCount);

	/*
	 * // if neither is empty, compute ratio of intersection/union. Set<Long> thisArids = new
	 * HashSet<Long>(assocs.size()); for (AssocExtended assoc : assocs) if (allAssocs ||
	 * assoc.isTimedef()) thisArids.add(assoc.getArid());
	 *
	 * Set<Long> otherArids = new HashSet<Long>(other.assocs.size()); for (AssocExtended assoc :
	 * other.assocs) if (allAssocs || assoc.isTimedef()) otherArids.add(assoc.getArid());
	 *
	 * Set<Long> intersection = new HashSet<Long>(thisArids); intersection.retainAll(otherArids);
	 *
	 * // convert thisArids into the union of thisArids and otherArids thisArids.addAll(otherArids);
	 *
	 * return (double)intersection.size()/(double)thisArids.size();
	 */
    }

    /**
     * Sort the supplied origins in order of decreasing similarity with this OriginExtended.
     * Similarity values will be between zero and one, with 1 implying the events are identical.
     * Spatial/temporal proximity and common assocs are given equal weight. To get a similarity score
     * of 1, the two origins must be colocated in space and time, and their assoc sets must be equal
     * (same size and same arrivals in each).
     * <p>
     * CAUTION: the size of the output Map will be smaller than the size of the input Collection if
     * the input contains Origins that have the same similarity with regard to this OriginExtended.
     *
     * @param origins the collection of origins to be sorted.
     * @return the input origins sorted in order of decreasing similarity..
     */
    public TreeMap<Double, OriginExtended> sortBySimilarity(
	    Collection<? extends OriginExtended> origins) {
	return sortBySimilarity(origins, 0.5);
    }

    /**
     * Sort the supplied origins in order of decreasing similarity with this OriginExtended.
     * Similarity values will be between zero and one, with 1 implying the events are identical. Both
     * spatial/temporal proximity and common assocs are considered. To get a similarity score of 1,
     * the two origins must be colocated in space and time, and their assoc sets must be equal (same
     * size and same arrivals in each).
     * <p>
     * CAUTION: the size of the output Map will be smaller than the size of the input Collection if
     * the input contains Origins that have the same similarity with regard to this OriginExtended.
     *
     * @param origins        the collection of origins to be sorted.
     * @param distanceWeight number between 0 and 1 that controls the relative weight given to
     *                       (distance,time) as opposed to overlapping assocs. If 1 only (distance,time) are
     *                       considered and overlapping assocs have no effect. If 0, only common assocs are
     *                       considered.
     * @return the input origins sorted in order of decreasing similarity..
     */
    public TreeMap<Double, OriginExtended> sortBySimilarity(
	    Collection<? extends OriginExtended> origins, double distanceWeight) {
	return sortBySimilarity(origins, distanceWeight, 10., Double.POSITIVE_INFINITY, 200.);
    }

    /**
     * Sort the supplied origins in order of decreasing similarity with this OriginExtended.
     * Similarity values will be between zero and one, with 1 implying the events are identical. Both
     * spatial/temporal proximity and common assocs are considered. To get a similarity score of 1,
     * the two origins must be colocated in space and time, and their assoc sets must be equal (same
     * size and same arrivals in each).
     * <p>
     * CAUTION: the size of the output Map will be smaller than the size of the input Collection if
     * the input contains Origins that have the same similarity with regard to this OriginExtended.
     *
     * @param origins             the collection of origins to be sorted.
     * @param distanceWeight      number between 0 and 1 that controls the relative weight given to
     *                            (distance,time) as opposed to overlapping assocs. If 1 only (distance,time) are
     *                            considered and overlapping assocs have no effect. If 0, only common assocs are
     *                            considered.
     * @param distanceScaleFactor in degrees
     * @param radiusScaleFactor   in km.
     * @param timeScaleFactor     in seconds.
     * @return the input origins sorted in order of decreasing similarity..
     */
    public TreeMap<Double, OriginExtended> sortBySimilarity(
	    Collection<? extends OriginExtended> origins, double distanceWeight,
	    double distanceScaleFactor, double radiusScaleFactor, double timeScaleFactor) {
	// instantiate a TreeMap<Similarity, Origin> that will sort the origins in order of decreasing
	// similarity.
	TreeMap<Double, OriginExtended> sortedOrigins =
		new TreeMap<Double, OriginExtended>(new Comparator<Double>() {
		    @Override
		    public int compare(Double s1, Double s2) {
			return (int) Math.signum(s2 - s1);
		    }
		});
	for (OriginExtended o : origins)
	    sortedOrigins.put(
		    getSimilarity(o, distanceWeight, distanceScaleFactor, radiusScaleFactor, timeScaleFactor),
		    o);

	return sortedOrigins;
    }

    /**
     * Generate a String representation of this OriginExtended object and optionally its children.
     * <ol start=0>
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

	boolean printAssoc = includeChildren.length > 0 && includeChildren[0];
	boolean printArrival = includeChildren.length > 1 && includeChildren[1];
	boolean printSite = includeChildren.length > 2 && includeChildren[2];
	boolean printOrigerr = includeChildren.length > 3 && includeChildren[3];
	boolean printAzgap = includeChildren.length > 4 && includeChildren[4];

	StringBuffer out = new StringBuffer();

	if (printAssoc)
	    out.append(NL).append(toStringHeader()).append(NL);

	out.append(toStringCustom()).append(NL);

	if (printOrigerr && origerr != null)
	    out.append(String.format("    %-10s%s%n", "origerr:", origerr.toString()));

	if (printAzgap && azgap != null)
	    out.append(String.format("    %-10s%s%n", "azgap:", azgap.toString()));

	if (printAssoc) {
	    ArrayList<AssocExtended> a = new ArrayList<AssocExtended>(assocs.values());
	    AssocExtended.sortByArrivalTime(a);
	    if (!printArrival)
		out.append(NL).append(AssocExtended.toStringHeader()).append(NL);

	    for (AssocExtended assoc : a)
		out.append(assoc.toString(printArrival, printSite));
	}

	return out.toString();
    }

    private String toStringHeader() {
	return String.format("%12s %12s %11s %12s %8s %15s %7s %4s %4s %5s %15s %20s", "evid", "orid",
		"lat", "lon", "depth", "time", "jdate", "nass", "ndef", "mb", "algorithm", "auth");
    }

    private String toStringCustom() {
	return String.format("%12d %12d %11.6f %12.6f %8.3f %15.3f %7d %4d %4d %5.2f %15s %20s",
		getEvid(), getOrid(), getLat(), getLon(), getDepth(), getTime(), getJdate(), getNass(),
		getNdef(), getMb(), getAlgorithm(), getAuth());
    }

    /**
     * Read OriginExtended object from a database without any descendant information.
     *
     * @param connection
     * @param sql        a valid SQL select statement that returns a complete row from a Origin table.
     * @return a Set of OriginExtended objects
     * @throws SQLException
     */
    static public HashSet<OriginExtended> readOriginExtended(Connection connection, String sql)
	    throws SQLException {
	HashSet<OriginExtended> origins = new HashSet<OriginExtended>();
	readOriginExtended(connection, sql, origins);
	return origins;
    }

    /**
     * Read OriginExtended object from a database without any descendant information.
     *
     * @param connection
     * @param sql        a valid SQL select statement that returns a complete row from a Origin table.
     * @param origins    Set<OriginExtended> into which to place the results.
     * @throws SQLException
     */
    static public void readOriginExtended(Connection connection, String sql,
	    Collection<OriginExtended> origins) throws SQLException {
	Statement statement = connection.createStatement();
	ResultSet rs = statement.executeQuery(sql);
	while (rs.next())
	    origins.add(new OriginExtended(rs));
	rs.close();
	statement.close();
    }

    /**
     * Load OriginExtended objects and descendant objects from a database.
     *
     * @param schema            a Schema that manages a database connection and a map from String tableType to
     *                          String tableName. Recognized tableTypes are [ origin, origerr, azgap, assoc, arrival,
     *                          site ]
     * @param network           3 possibilities:
     *                          <ul>
     *                          <li>if network is populated with sites, then sites from the network will be attached to
     *                          arrivals and any site table specified in tableNames will be ignored.
     *                          <li>if network is null, and a site table is specified in tableNames, then sites from the
     *                          input table will be attached to arrivals.
     *                          <li>if network is empty, and a site table is specified in inputTables then network will
     *                          be populated with sites from the site table and those sites will be attached to
     *                          arrivals.
     *                          </ul>
     * @param originWhereClause a where clause to be executed against an origin table. Eg., 'orid in
     *                          (98127, 789009)'
     * @param assocWhereClause  additional elements of a where clause to be executed against the assoc
     *                          table. Eg., "and phase = 'P'", which will be combined with the originWhereClause to
     *                          produce 'orid in (98127, 789009) and phase = 'P''. Can be an empty String.
     * @param executedSQL       (output; ignored if null) the sql commands that are actually executed by
     *                          this method.
     * @return Map from orid to OriginExtended objects with requested descendants attached.
     * @throws Exception
     */
    static public Map<Long, OriginExtended> readOriginExtended(Schema schema, NetworkExtended network,
	    String originWhereClause, String assocWhereClause, ArrayList<String> executedSQL)
		    throws Exception {
	Map<Long, OriginExtended> origins = new LinkedHashMap<>();
	readOriginExtended(schema, network, originWhereClause, assocWhereClause, executedSQL, origins);
	return origins;
    }

    /**
     * Load an OriginExtended object and descendant objects from a database.
     *
     * @param schema            a Schema that manages a database connection and a map from String tableType to
     *                          String tableName. Recognized tableTypes are [ origin, origerr, azgap, assoc, arrival,
     *                          site ]
     * @param network           3 possibilities:
     *                          <ul>
     *                          <li>if network is populated with sites, then sites from the network will be attached to
     *                          arrivals and any site table specified in tableNames will be ignored.
     *                          <li>if network is null, and a site table is specified in tableNames, then sites from the
     *                          input table will be attached to arrivals.
     *                          <li>if network is empty, and a site table is specified in inputTables then network will
     *                          be populated with sites from the site table and those sites will be attached to
     *                          arrivals.
     * @param originWhereClause a where clause to be executed against an origin table. Eg., 'orid in
     *                          (98127, 789009)'
     * @param assocWhereClause  additional elements of a where clause to be executed against the assoc
     *                          table. Eg., "and phase = 'P'", which will be combined with the originWhereClause to
     *                          produce 'orid in (98127, 789009) and phase = 'P''. Can be an empty String.
     * @param executedSQL       (output; ignored if null) the sql commands that are actually executed by
     *                          this method.
     * @param origins Map from orid to OriginExtended objects with requested descendants attached.
     * @throws Exception
     */
    static public void readOriginExtended(Schema schema, NetworkExtended network,
	    String originWhereClause, String assocWhereClause, ArrayList<String> executedSQL,
	    Map<Long, OriginExtended> origins) throws Exception 
    {
	originWhereClause = originWhereClause == null ? "" : originWhereClause.trim();
	if (originWhereClause.toLowerCase().startsWith("where "))
	    originWhereClause = originWhereClause.substring(6);
	originWhereClause = originWhereClause.replaceAll("orid", "origin.orid")
		.replaceAll("origin.origin.orid", "origin.orid");

	assocWhereClause = assocWhereClause == null ? "" : assocWhereClause.trim();
	if (assocWhereClause.toLowerCase().startsWith("and "))
	    assocWhereClause = assocWhereClause.substring(4);

	ResultSet rs = null;
	Statement statement = schema.getStatement();

	long timer = System.currentTimeMillis();

	String sql = "select * from "+schema.getTableName("origin") + " origin";
	if (originWhereClause.length() > 0)
	    sql += " where "+originWhereClause;

	try {
	    rs = statement.executeQuery(sql);
	    while (rs.next()) {
		OriginExtended origin = new OriginExtended(rs);
		if (origin.getAuth() == null) origin.setAuth(Origin.AUTH_NA);
		origins.put(origin.getOrid(), origin);
	    }
	} catch (SQLException ex) {
	    throw new SQLException(String.format("%s%n%s%nUserName=%s%nInstance=%s", ex.getMessage(),
		    sql, schema.getUserName(), schema.getInstance()));
	}
	finally { rs.close(); } 

	timer = System.currentTimeMillis() - timer;

	if (executedSQL != null)
	    executedSQL.add(String.format("%s returned %d origins in %d msec", 
		    sql, origins.size(), timer));

	if (schema.isSupported("origerr")) {

	    sql = "select * from "+schema.getTableName("origerr")+" origerr, "
		    +schema.getTableName("origin")+" origin where origerr.orid=origin.orid";
	    if (originWhereClause.length() > 0)
		sql += " and "+originWhereClause;

	    timer = System.currentTimeMillis();

	    int count = 0;
	    try {
		rs = statement.executeQuery(sql);
		while (rs.next()) {
		    Origerr origerr = new Origerr(rs);
		    OriginExtended origin = origins.get(origerr.getOrid());
		    if (origin != null)
		    {
			origin.setOrigerr(origerr);
			++count;
		    }
		}
	    } 
	    catch (SQLException ex) {
		throw new SQLException(String.format("%s%n%s%nUserName=%s%nInstance=%s", ex.getMessage(),
			sql, schema.getUserName(), schema.getInstance()));
	    }
	    finally { rs.close(); }

	    timer = System.currentTimeMillis() - timer;

	    if (executedSQL != null)
		executedSQL.add(String.format("%s returned %d origerr rows in %d msec", 
			sql, count, timer));
	}

	if (schema.isSupported("azgap")) {

	    sql = "select * from "+schema.getTableName("azgap")+" azgap, "
		    +schema.getTableName("origin")+" origin where azgap.orid=origin.orid";
	    if (originWhereClause.length() > 0)
		sql += " and "+originWhereClause;

	    timer = System.currentTimeMillis();

	    int count = 0;
	    try {
		rs = statement.executeQuery(sql);
		while (rs.next()) {
		    Azgap azgap = new Azgap(rs);
		    OriginExtended origin = origins.get(azgap.getOrid());
		    if (origin != null)
		    {
			origin.setAzgap(azgap);
			++count;
		    }
		}
	    } catch (SQLException ex) {
		throw new SQLException(String.format("%s%n%s%nUserName=%s%nInstance=%s", ex.getMessage(),
			sql, schema.getUserName(), schema.getInstance()));
	    }
	    finally { rs.close(); } 

	    timer = System.currentTimeMillis() - timer;

	    if (executedSQL != null)
		executedSQL.add(String.format("%s returned %d azgap rows in %d msec", 
			sql, count, timer));

	}

	NetworkExtended tempNetwork = network == null ? new NetworkExtended() : network;

	if (tempNetwork.size() == 0 && schema.isSupported("assoc") && schema.isSupported("arrival") && schema.isSupported("site")) {

	    sql = String.format("select unique site.* from %s origin, %s assoc, %s arrival, %s site "
		    + "where assoc.orid = origin.orid and arrival.arid = assoc.arid and site.sta = arrival.sta "
		    + "and arrival.jdate between site.ondate and decode(site.offdate, -1, 2286324, site.offdate)",
		    schema.getTableName("origin"), schema.getTableName("assoc"), schema.getTableName("arrival"), 
		    schema.getTableName("site"));

	    if (originWhereClause.length() > 0)
		sql += " and "+originWhereClause;
	    if (assocWhereClause.length() > 0)
		sql += " and "+assocWhereClause;

	    timer = System.currentTimeMillis();

	    for (Site site : Site.readSites(schema.getConnection(), sql))
		tempNetwork.add(site);

	    timer = System.currentTimeMillis() - timer;

	    if (executedSQL != null)
		executedSQL.add(String.format("%s returned %d site rows in t%d msec", 
			sql, tempNetwork.size(), timer));
	}

	if (schema.isSupported("assoc") && schema.isSupported("arrival")) {

	    sql = String.format("select assoc.*, arrival.* from %s origin, %s assoc, %s arrival "
		    + "where assoc.orid = origin.orid and arrival.arid = assoc.arid",
		    schema.getTableName("origin"), schema.getTableName("assoc"), schema.getTableName("arrival"));

	    if (originWhereClause.length() > 0)
		sql += " and "+originWhereClause;
	    if (assocWhereClause.length() > 0)
		sql += " and "+assocWhereClause;

	    timer = System.currentTimeMillis();
	    int count = 0;
	    try {
		rs = statement.executeQuery(sql);
		while (rs.next()) {
		    AssocExtended assoc = new AssocExtended(rs, 0);
		    ArrivalExtended arrival = new ArrivalExtended(rs, 19);
		    if (arrival.getAuth() == null) arrival.setAuth(Arrival.AUTH_NA);
		    arrival.setSite(tempNetwork);
		    //if (arrival.getSite() != null) {
		    assoc.setArrival(arrival);
		    OriginExtended origin = origins.get(assoc.getOrid());
		    if (origin != null)
		    {
			origin.getAssocs().put(assoc.getArid(), assoc);
			++count;
		    }
		    //}
		}
	    } 
	    catch (SQLException ex) {
		throw new SQLException(String.format("%s%n%s%nUserName=%s%nInstance=%s", ex.getMessage(),
			sql, schema.getUserName(), schema.getInstance()));
	    }
	    finally { rs.close(); }

	    timer = System.currentTimeMillis() - timer;

	    if (executedSQL != null)
		executedSQL.add(String.format("%s returned %d assoc and arrival rows in %d msec", 
			sql, count, timer));
	}
	else if (schema.isSupported("assoc")) {

	    sql = String.format("select assoc.* from %s origin, %s assoc where assoc.orid = origin.orid",
		    schema.getTableName("origin"), schema.getTableName("assoc"));

	    if (originWhereClause.length() > 0)
		sql += " and "+originWhereClause;
	    if (assocWhereClause.length() > 0)
		sql += " and "+assocWhereClause;

	    timer = System.currentTimeMillis();

	    int count = 0;
	    try {
		rs = statement.executeQuery(sql);
		while (rs.next()) {
		    AssocExtended assoc = new AssocExtended(rs, 0);
		    OriginExtended origin = origins.get(assoc.getOrid());
		    if (origin != null)
		    {
			origin.getAssocs().put(assoc.getArid(), assoc);
			++count;
		    }
		}
	    } catch (SQLException ex) {
		throw new SQLException(String.format("%s%n%s%nUserName=%s%nInstance=%s", ex.getMessage(),
			sql, schema.getUserName(), schema.getInstance()));
	    }
	    finally { rs.close(); }

	    timer = System.currentTimeMillis() - timer;

	    if (executedSQL != null)
		executedSQL.add(String.format("%s returned %d assoc rows in %d msec", sql, count, timer));
	}

	statement.close();
    }

    /**
     * Given collections of origins, origerrs, azgaps, assocs and arrivals:
     * <ol>
     * <li>construct new OriginExtended from the origins.
     * <li>transfer origerrs to originExtended.origerr based on orid
     * <li>transfer assocs to originExtended.assocs based on orid
     * <li>transfer arrivals to the assocExtended objects based on arid.
     * <li>attach references to the SiteExtended objects to each arrivalExtended.
     * </ol>
     * Origins, Origerrs, Azgaps, Assocs and Arrivals are removed from the input collections when they
     * are moved to the origin rowgraphs.
     *
     * @param origins
     * @param origerrs
     * @param assocs
     * @param arrivals
     * @param network
     * @return new list of OriginExtended objects populated with assocs, arrivals and sites.
     */
    public static ArrayList<OriginExtended> organize(Collection<? extends Origin> origins,
	    Collection<? extends Origerr> origerrs, Collection<? extends Azgap> azgaps,
	    Collection<? extends Assoc> assocs, Collection<? extends Arrival> arrivals,
	    NetworkExtended network) {
	ArrayList<OriginExtended> extendedOrigins = new ArrayList<OriginExtended>(origins.size());

	ArrayList<Assoc> assocsToRemove = new ArrayList<Assoc>();
	BaseRow rowToRemove;

	for (Origin origin : origins) {
	    OriginExtended extendedOrigin = new OriginExtended(origin);
	    extendedOrigins.add(extendedOrigin);

	    rowToRemove = null;
	    if (origerrs != null)
		for (Origerr row : origerrs)
		    if (row.getOrid() == origin.getOrid()) {
			rowToRemove = row;
			extendedOrigin.setOrigerr(row);
			break;
		    }
	    if (rowToRemove != null)
		origerrs.remove(rowToRemove);

	    rowToRemove = null;
	    if (azgaps != null)
		for (Azgap row : azgaps)
		    if (row.getOrid() == origin.getOrid()) {
			rowToRemove = row;
			extendedOrigin.setAzgap(row);
			break;
		    }
	    if (rowToRemove != null)
		azgaps.remove(rowToRemove);

	    for (Assoc assoc : assocs)
		if (assoc.getOrid() == origin.getOrid()) {
		    assocsToRemove.add(assoc);
		    AssocExtended extendedAssoc = new AssocExtended(assoc);
		    extendedOrigin.getAssocs().put(extendedAssoc.getArid(), extendedAssoc);

		    rowToRemove = null;
		    for (Arrival arrival : arrivals)
			if (arrival.getArid() == extendedAssoc.getArid()) {
			    extendedAssoc.setArrival(new ArrivalExtended(arrival).setSite(network));
			    rowToRemove = arrival;
			    break;
			}
		    if (rowToRemove != null)
			arrivals.remove(rowToRemove);
		}
	    assocs.removeAll(assocsToRemove);
	    assocsToRemove.clear();

	    extendedOrigin.setNass().setNdef();
	}
	origins.clear();
	return extendedOrigins;
    }

    /**
     * Read a Set of OriginExtended objects from a File. No descendants are loaded (assocs, origerrs,
     * etc.).
     *
     * @param input
     * @return HashSet of OriginExtended objects.
     * @throws IOException
     */
    static public Set<OriginExtended> readOriginExtended(BufferedReader input) throws IOException {
	Set<OriginExtended> rows = new LinkedHashSet<OriginExtended>();
	readOriginExtended(input, rows);
	return rows;
    }

    /**
     * Read a Set of OriginExtended objects from a File. No descendants are loaded (assocs, origerrs,
     * etc.). Records that begin with '#' are ignored.
     *
     * @param input
     * @param HashSet of OriginExtended objects to which the new OriginExtended objects will be added.
     *                This Set is not cleared before additions.
     * @throws IOException
     */
    static public void readOriginExtended(BufferedReader input, Set<OriginExtended> rows)
	    throws IOException {
	for (Origin origin : Origin.readOrigins(input))
	    rows.add(new OriginExtended(origin));
    }

    /**
     * Read a Set of OriginExtended objects from a File. No descendants are loaded (assocs, origerrs,
     * etc.).
     *
     * @param inputFile
     * @return HashSet of OriginExtended objects.
     * @throws IOException
     */
    static public Set<OriginExtended> readOriginExtended(File inputFile) throws IOException {
	Set<OriginExtended> rows = new HashSet<OriginExtended>();
	readOriginExtended(inputFile, rows);
	return rows;
    }

    /**
     * Read a Set of OriginExtended objects from a File. No descendants are loaded (assocs, origerrs,
     * etc.). Records that begin with '#' are ignored.
     *
     * @param inputFile
     * @param HashSet   of OriginExtended objects to which the new OriginExtended objects will be added.
     *                  This Set is not cleared before additions.
     * @throws IOException
     */
    static public void readOriginExtended(File inputFile, Set<OriginExtended> rows)
	    throws IOException {
	if (inputFile.exists())
	    for (Origin origin : Origin.readOrigins(inputFile))
		rows.add(new OriginExtended(origin));
    }

    /**
     * Load a set of OriginExtended objects and associated descendants from Files.
     *
     * @param inputFiles map from row type to Files object. inputFiles keys may include orign,
     *                   origerr, azgap, assoc and arrival.
     * @return origins a HashSet of OriginExtended objects
     * @throws IOException
     */
    static public Set<OriginExtended> readOriginExtended(HashMap<String, File> inputFiles) throws IOException {
	return readOriginExtended(inputFiles, null);
    }

    /**
     * Load a set of OriginExtended objects and associated descendants from Files.
     *
     * @param inputFiles map from row type to Files object. inputFiles keys may include orign,
     *                   origerr, azgap, assoc and arrival.
     * @param network    3 possibilities:
     *                   <ul>
     *                   <li>if network is populated with sites, then sites from the network will be attached to
     *                   arrivals and any site file specified in inputFiles will be ignored.
     *                   <li>if network is null, and a site file is specified in inputFiles then sites from the
     *                   input file will be attached to arrivals.
     *                   <li>if network is empty, and a site file is specified in inputFiles then network will be
     *                   populated with sites from the site file and those sites will be attached to arrivals.
     *                   </ul>
     * @return origins a HashSet of OriginExtended objects
     * @throws IOException
     */
    static public Set<OriginExtended> readOriginExtended(HashMap<String, File> inputFiles,
	    NetworkExtended network) throws IOException {
	Map<String, BufferedReader> map = new LinkedHashMap<String, BufferedReader>(inputFiles.size());
	for (Entry<String, File> f : inputFiles.entrySet())
	    map.put(f.getKey(), new BufferedReader(new FileReader(f.getValue())));
	return readBufferedReaders(map, network);
    }

    /**
     * Load a set of OriginExtended objects from Files.
     *
     * @param inputReaders map from row type to File object. inputFiles keys may include orign,
     *                     origerr, azgap, assoc and arrival.
     * @param network      3 possibilities:
     *                     <ul>
     *                     <li>if network is populated with sites, then sites from the network will be attached to
     *                     arrivals and any site file specified in inputFiles will be ignored.
     *                     <li>if network is null, and a site file is specified in inputFiles then sites from the
     *                     input file will be attached to arrivals.
     *                     <li>if network is empty, and a site file is specified in inputFiles then network will be
     *                     populated with sites from the site file and those sites will be attached to arrivals.
     *                     </ul>
     * @return
     * @throws IOException
     */
    static public Set<OriginExtended> readOriginExtended(Map<String, File> inputFiles, NetworkExtended network) throws IOException {
	Map<String, BufferedReader> map = new LinkedHashMap<String, BufferedReader>(inputFiles.size());
	for (Entry<String, File> f : inputFiles.entrySet())
	    map.put(f.getKey(), new BufferedReader(new FileReader(f.getValue())));
	return readBufferedReaders(map, network);
    }

    /**
     * Load a set of OriginExtended objects from Files.
     *
     * @param inputReaders map from row type to File object. inputFiles keys may include orign,
     *                     origerr, azgap, assoc and arrival.
     * @param network      3 possibilities:
     *                     <ul>
     *                     <li>if network is populated with sites, then sites from the network will be attached to
     *                     arrivals and any site file specified in inputFiles will be ignored.
     *                     <li>if network is null, and a site file is specified in inputFiles then sites from the
     *                     input file will be attached to arrivals.
     *                     <li>if network is empty, and a site file is specified in inputFiles then network will be
     *                     populated with sites from the site file and those sites will be attached to arrivals.
     *                     </ul>
     * @param origins      a Collection of OriginExtended objects to which origins from the inputFiles will
     *                     be added. This Collection is not cleared before population.
     * @throws IOException
     */
    static public void readOriginExtended(Map<String, File> inputFiles, NetworkExtended network,
	    Collection<OriginExtended> origins) throws IOException {
	Map<String, BufferedReader> map = new LinkedHashMap<String, BufferedReader>(inputFiles.size());
	for (Entry<String, File> f : inputFiles.entrySet())
	    map.put(f.getKey(), new BufferedReader(new FileReader(f.getValue())));
	readBufferedReaders(map, network, origins);
    }


    /**
     * Load a set of OriginExtended objects and associated descendants from InputStreams.
     *
     * @param map2    map from row type to InputStream object. inputFiles keys may include orign,
     *                origerr, azgap, assoc and arrival.
     * @param network 3 possibilities:
     *                <ul>
     *                <li>if network is populated with sites, then sites from the network will be attached to
     *                arrivals and any site file specified in inputFiles will be ignored.
     *                <li>if network is null, and a site file is specified in inputFiles then sites from the
     *                input file will be attached to arrivals.
     *                <li>if network is empty, and a site file is specified in inputFiles then network will be
     *                populated with sites from the site file and those sites will be attached to arrivals.
     *                </ul>
     * @return origins a HashSet of OriginExtended objects
     * @throws IOException
     */
    static public Set<OriginExtended> readInputStreams(Map<String, InputStream> map2,
	    NetworkExtended network) throws IOException {
	Map<String, BufferedReader> map = new LinkedHashMap<String, BufferedReader>(map2.size());
	for (Entry<String, InputStream> f : map2.entrySet())
	    map.put(f.getKey(), new BufferedReader(new InputStreamReader(f.getValue())));
	return readBufferedReaders(map, network);
    }

    /**
     * Load a set of OriginExtended objects from Files.
     *
     * @param inputReaders map from row type to File object. inputFiles keys may include orign,
     *                     origerr, azgap, assoc and arrival.
     * @param network      3 possibilities:
     *                     <ul>
     *                     <li>if network is populated with sites, then sites from the network will be attached to
     *                     arrivals and any site file specified in inputFiles will be ignored.
     *                     <li>if network is null, and a site file is specified in inputFiles then sites from the
     *                     input file will be attached to arrivals.
     *                     <li>if network is empty, and a site file is specified in inputFiles then network will be
     *                     populated with sites from the site file and those sites will be attached to arrivals.
     *                     </ul>
     * @param origins      a Collection of OriginExtended objects to which origins from the inputFiles will
     *                     be added. This Collection is not cleared before population.
     * @throws IOException
     */
    static public void readInputStreams(Map<String, InputStream> inputFiles, NetworkExtended network,
	    Collection<OriginExtended> origins) throws IOException {
	Map<String, BufferedReader> map = new LinkedHashMap<String, BufferedReader>(inputFiles.size());
	for (Entry<String, InputStream> f : inputFiles.entrySet())
	    map.put(f.getKey(), new BufferedReader(new InputStreamReader(f.getValue())));
	readBufferedReaders(map, network, origins);
    }


    /**
     * Load a set of OriginExtended objects and associated descendants from BufferedReaders.
     * <p>
     * The BufferedReaders are closed after all their data has been read.
     *
     * @param inputReaders map from row type to BufferedReader object. inputFiles keys may include
     *                     orign, origerr, azgap, assoc and arrival.
     * @param network      3 possibilities:
     *                     <ul>
     *                     <li>if network is populated with sites, then sites from the network will be attached to
     *                     arrivals and any site file specified in inputFiles will be ignored.
     *                     <li>if network is null, and a site file is specified in inputFiles then sites from the
     *                     input file will be attached to arrivals.
     *                     <li>if network is empty, and a site file is specified in inputFiles then network will be
     *                     populated with sites from the site file and those sites will be attached to arrivals.
     *                     </ul>
     * @return origins a HashSet of OriginExtended objects
     * @throws IOException
     */
    static public Set<OriginExtended> readBufferedReaders(Map<String, BufferedReader> inputReaders,
	    NetworkExtended network) throws IOException {
	Set<OriginExtended> origins = new LinkedHashSet<OriginExtended>();
	OriginExtended.readBufferedReaders(inputReaders, network, origins);
	return origins;
    }

    /**
     * Load a set of OriginExtended objects from BufferedReaders.
     * <p>
     * The BufferedReaders are closed after all their data has been read.
     *
     * @param inputReaders map from row type to File object. inputFiles keys may include orign,
     *                     origerr, azgap, assoc and arrival.
     * @param network      3 possibilities:
     *                     <ul>
     *                     <li>if network is populated with sites, then sites from the network will be attached to
     *                     arrivals and any site file specified in inputFiles will be ignored.
     *                     <li>if network is null, and a site file is specified in inputFiles then sites from the
     *                     input file will be attached to arrivals.
     *                     <li>if network is empty, and a site file is specified in inputFiles then network will be
     *                     populated with sites from the site file and those sites will be attached to arrivals.
     *                     </ul>
     * @param origins      a Collection of OriginExtended objects to which origins from the inputFiles will
     *                     be added. This Collection is not cleared before population.
     * @throws IOException
     */
    static public void readBufferedReaders(Map<String, BufferedReader> inputReaders,
	    NetworkExtended network, Collection<OriginExtended> origins) throws IOException {
	BufferedReader f = getBufferedReader(inputReaders, "Origin");
	if (f == null)
	    return;

	Set<OriginExtended> moreOrigins = OriginExtended.readOriginExtended(f);

	// if no origins were loaded, or if no origin children were requested,
	// just return.
	if (moreOrigins.isEmpty() || inputReaders.size() == 1) {
	    origins.addAll(moreOrigins);
	    return;
	}

	// make map from orid to Origin.
	HashMap<Long, OriginExtended> originMap = new HashMap<Long, OriginExtended>(moreOrigins.size());
	for (OriginExtended o : moreOrigins)
	    originMap.put(o.getOrid(), o);

	f = getBufferedReader(inputReaders, "Origerr");
	if (f != null)
	    for (Origerr row : Origerr.readOrigerrs(f)) {
		OriginExtended o = originMap.get(row.getOrid());
		if (o != null)
		    o.setOrigerr(row);
	    }

	f = getBufferedReader(inputReaders, "Azgap");
	if (f != null)
	    for (Azgap row : Azgap.readAzgaps(f)) {
		OriginExtended o = originMap.get(row.getOrid());
		if (o != null)
		    o.setAzgap(row);
	    }

	f = getBufferedReader(inputReaders, "Site");
	if (network == null)
	    network = new NetworkExtended();
	if (network.size() == 0 && f != null) {
	    for (Site site : Site.readSites(f))
		try {
		    network.add(new SiteExtended(site));
		} catch (Exception ex) {
		    System.out.println(ex);
		}
	}

	f = getBufferedReader(inputReaders, "Assoc");
	if (f != null) {
	    HashMap<Long, ArrivalExtended> arrivalMap = new HashMap<Long, ArrivalExtended>();
	    BufferedReader fa = getBufferedReader(inputReaders, "Arrival");
	    if (fa != null)
		for (Arrival arrival : Arrival.readArrivals(fa))
		    arrivalMap.put(arrival.getArid(), new ArrivalExtended(arrival).setSite(network));

	    for (Assoc assoc : Assoc.readAssocs(f)) {
		OriginExtended o = originMap.get(assoc.getOrid());
		if (o != null) {
		    ArrivalExtended arrival = arrivalMap.get(assoc.getArid());
		    o.getAssocs().put(assoc.getArid(), new AssocExtended(assoc).setArrival(arrival));
		}
	    }
	}

	for (OriginExtended o : moreOrigins)
	    o.setNass().setNdef();

	origins.addAll(moreOrigins);
    }

    // /**
    // * Load a set of OriginExtended objects from ascii files.
    // *
    // * @param inputFiles
    // * map from row type to File object. inputFiles keys may include
    // * orign, origerr, azgap, assoc and arrival.
    // * @param network
    // * 3 possibilities:
    // * <ul>
    // * <li>if network is populated with sites, then sites from the
    // * network will be attached to arrivals and any site file
    // * specified in inputFiles will be ignored.
    // * <li>if network is null, and a site file is specified in
    // * inputFiles then sites from the input file will be attached to
    // * arrivals.
    // * <li>if network is empty, and a site file is specified in
    // * inputFiles then network will be populated with sites from the
    // * site file and those sites will be attached to arrivals.
    // * </ul>
    // * @param origins
    // * a Collection of OriginExtended objects to which origins from
    // * the inputFiles will be added. This Collection is not cleared
    // * before population.
    // * @throws IOException
    // */
    // static public void readOriginExtended(Map<String, File> inputFiles,
    // NetworkExtended network, Collection<OriginExtended> origins)
    // throws IOException
    // {
    // File f = getFile(inputFiles, "Origin");
    // if (f == null) return;
    //
    // Set<OriginExtended> moreOrigins = new HashSet<OriginExtended>();
    //
    // OriginExtended.readOriginExtended(getFile(inputFiles, "Origin"), moreOrigins);
    //
    // // if no origins were loaded, or if no origin children were requested,
    // // just return.
    // if (moreOrigins.isEmpty() || inputFiles.size() == 1)
    // {
    // origins.addAll(moreOrigins);
    // return;
    // }
    //
    // // make map from orid to Origin.
    // HashMap<Long, OriginExtended> originMap = new HashMap<Long, OriginExtended>(
    // moreOrigins.size());
    // for (OriginExtended o : moreOrigins)
    // originMap.put(o.getOrid(), o);
    //
    // f = getFile(inputFiles, "Origerr");
    // if (f != null)
    // for (Origerr row : Origerr.readOrigerrs(f)) {
    // OriginExtended o = originMap.get(row.getOrid());
    // if (o != null)
    // o.setOrigerr(row);
    // }
    //
    // f = getFile(inputFiles, "Azgap");
    // if (f != null)
    // for (Azgap row : Azgap.readAzgaps(f)) {
    // OriginExtended o = originMap.get(row.getOrid());
    // if (o != null)
    // o.setAzgap(row);
    // }
    //
    // f = getFile(inputFiles, "Site");
    // if (network == null)
    // network = new NetworkExtended();
    // if (network.size() == 0 && f != null) {
    // BufferedReader input = new BufferedReader(new FileReader(f));
    // String line;
    // while ((line = input.readLine()) != null)
    // {
    // line = line.trim();
    // if (!line.startsWith("#"))
    // {
    // try {
    // //System.out.println(line);
    // network.add(new SiteExtended(new Scanner(line)));
    // }
    // catch (Exception ex)
    // {
    // System.out.println(ex);
    // }
    // }
    // }
    // input.close();
    // }
    //
    // f = getFile(inputFiles, "Assoc");
    // if (f != null)
    // {
    // HashMap<Long, ArrivalExtended> arrivalMap = new HashMap<Long, ArrivalExtended>();
    // File fa = getFile(inputFiles, "Arrival");
    // if (fa != null)
    // for (ArrivalExtended arrival : ArrivalExtended
    // .readArrivalExtendeds(fa))
    // arrivalMap.put(arrival.getArid(), arrival.setSite(network));
    //
    // for (AssocExtended assoc : AssocExtended.readAssocExtendeds(f))
    // {
    // OriginExtended o = originMap.get(assoc.getOrid());
    // if (o != null)
    // {
    // ArrivalExtended arrival = arrivalMap.get(assoc.getArid());
    // assoc.setArrival(arrival);
    // o.getAssocs().put(assoc.getArid(), assoc);
    // }
    // }
    // }
    //
    // origins.addAll(moreOrigins);
    // }

    /**
     * Write an entire Set of OriginExtended to an ascii file. If <it>outputFiles</it> contains
     * entries for descendants, then they are written to separate files, as appropriate. For examples,
     * if outputFiles.get("assoc") = File f, then all the assocs associated with all origins are
     * written to File f.
     *
     * @param origins
     * @param outputFiles map from row type to File object. outputFiles keys may include orign,
     *                    origerr, azgap, assoc and arrival.
     * @throws IOException
     */
    static public void writeOriginExtendeds(Collection<? extends OriginExtended> origins,
	    Map<String, File> outputFiles) throws IOException {
	HashMap<String, BufferedWriter> writers =
		new HashMap<String, BufferedWriter>(outputFiles.size());

	String delim = BaseRow.getTokenDelimiter();

	for (String type : new String[]{"origin", "Origin", "origerr", "Origerr", "azgap", "Azgap",
		"assoc", "Assoc", "arrival", "Arrival", "site", "Site"})
	    if (outputFiles.containsKey(type)) {
		writers.put(type.toLowerCase(), new BufferedWriter(new FileWriter(outputFiles.get(type))));
		if (type.toLowerCase().equals("origin"))
		    writers.get("origin").write(Origin.getHeader().replaceAll(" ", delim) + "\n");
		else if (type.toLowerCase().equals("origerr"))
		    writers.get("origerr").write(Origerr.getHeader().replaceAll(" ", delim) + "\n");
		else if (type.toLowerCase().equals("azgap"))
		    writers.get("azgap").write(Azgap.getHeader().replaceAll(" ", delim) + "\n");
		else if (type.toLowerCase().equals("assoc"))
		    writers.get("assoc").write(Assoc.getHeader().replaceAll(" ", delim) + "\n");
		else if (type.toLowerCase().equals("arrival"))
		    writers.get("arrival").write(Arrival.getHeader().replaceAll(" ", delim) + "\n");
		else if (type.toLowerCase().equals("site"))
		    writers.get("site").write(Site.getHeader().replaceAll(" ", delim) + "\n");
	    }

	HashSet<SiteExtended> sites = new HashSet<>();
	HashSet<ArrivalExtended> arrivals = new HashSet<>();
	for (OriginExtended origin : origins) {
	    if (writers.get("origin") != null)
		origin.writeln(writers.get("origin"));
	    if (origin.origerr != null && writers.get("origerr") != null)
		origin.origerr.writeln(writers.get("origerr"));
	    if (origin.azgap != null && writers.get("azgap") != null)
		origin.azgap.writeln(writers.get("azgap"));
	    for (AssocExtended assoc : origin.getAssocs().values()) {
		if (writers.get("assoc") != null) {
		    assoc.writeln(writers.get("assoc"));
		    if (assoc.arrival != null) {
			if (writers.get("arrival") != null && !arrivals.contains(assoc.arrival)) {
			    arrivals.add(assoc.arrival);
			    assoc.arrival.writeln(writers.get("arrival"));
			}
			if (assoc.arrival.getSite() != null && writers.get("site") != null
				&& !sites.contains(assoc.arrival.getSite())) {
			    sites.add(assoc.arrival.getSite());
			    assoc.arrival.getSite().writeln(writers.get("site"));
			}
		    }
		}
	    }
	}

	for (BufferedWriter writer : writers.values())
	    writer.close();

    }

    /**
     * Write an origin and all associated rowgraph data to an output Schema.
     *
     * @param origin
     * @param outputSchema
     * @param commit       if true, a commit is executed after all data is successfully written to the db.
     * @return number of rows of each tableType written to db. null indicates that a SQL exception
     * occurred, in which case a connection.rollback() was executed so no data was written to
     * the db.
     * @throws Exception
     */
    static public LinkedHashMap<String, Integer> writeOriginExtended(OriginExtended origin,
	    Schema outputSchema, boolean commit) throws Exception {
	ArrayList<OriginExtended> o = new ArrayList<OriginExtended>();
	o.add(origin);
	return writeOriginExtendeds(o, outputSchema, commit);
    }

    /**
     * Write a collection of origins and all associated rowgraph data to an output Schema.
     *
     * @param origins
     * @param outputSchema
     * @param commit       if true, a commit is executed after all data is successfully written to the db.
     * @return number of rows of each tableType written to db. null indicates that a SQL exception
     * occurred, in which case a connection.rollback() was executed so no data was written to
     * the db.
     * @throws Exception
     */
    static public LinkedHashMap<String, Integer> writeOriginExtendeds(
	    Collection<? extends OriginExtended> origins, Schema outputSchema, boolean commit)
		    throws Exception {
	java.util.Date lddate = new java.util.Date();

	String originTable = outputSchema.getTableName("Origin");
	String origerrTable = outputSchema.getTableName("Origerr");
	String azgapTable = outputSchema.getTableName("Azgap");
	String assocTable = outputSchema.getTableName("Assoc");
	String arrivalTable = outputSchema.getTableName("Arrival");
	String siteTable = outputSchema.getTableName("Site");

	LinkedHashMap<String, Integer> count =
		new LinkedHashMap<String, Integer>(outputSchema.getTableNames().size());

	try {
	    if (originTable != null) {
		Origin.write(outputSchema.getConnection(), originTable, origins, lddate, false);
		count.put("Origin", origins.size());
	    }

	    if (origerrTable != null) {
		HashSet<Origerr> rows = new HashSet<Origerr>(origins.size());
		for (OriginExtended origin : origins)
		    if (origin.getOrigerr() != null)
			rows.add(origin.getOrigerr());

		Origerr.write(outputSchema.getConnection(), origerrTable, rows, lddate, false);
		count.put("Origerr", rows.size());
	    }

	    if (azgapTable != null) {
		HashSet<Azgap> rows = new HashSet<Azgap>(origins.size());
		for (OriginExtended origin : origins)
		    if (origin.getAzgap() != null)
			rows.add(origin.getAzgap());

		Azgap.write(outputSchema.getConnection(), azgapTable, rows, lddate, false);
		count.put("Azgap", rows.size());
	    }

	    if (assocTable != null) {
		HashSet<AssocExtended> assocs = new HashSet<AssocExtended>(origins.size() * 20);
		HashSet<ArrivalExtended> arrivals = new HashSet<ArrivalExtended>(origins.size() * 20);
		HashSet<SiteExtended> sites = new HashSet<SiteExtended>(200);

		for (OriginExtended origin : origins) {
		    assocs.addAll(origin.getAssocs().values());

		    if (arrivalTable != null)
			for (AssocExtended assoc : origin.getAssocs().values()) {
			    if (assoc.getArrival() != null) {
				arrivals.add(assoc.getArrival());
				if (siteTable != null)
				    sites.add(assoc.getArrival().getSite());
			    }

			}
		}

		Assoc.write(outputSchema.getConnection(), assocTable, assocs, lddate, false);
		count.put("Assoc", assocs.size());

		if (arrivals.size() > 0) {
		    Arrival.write(outputSchema.getConnection(), arrivalTable, arrivals, lddate, false);
		    count.put("Arrival", arrivals.size());

		    if (sites.size() > 0) {
			Site.write(outputSchema.getConnection(), siteTable, sites, lddate, false);
			count.put("Site", sites.size());
		    }
		}
	    }

	    if (commit)
		outputSchema.getConnection().commit();

	    int total = 0;
	    for (Integer c : count.values())
		total += c;
	    count.put("Total", total);
	} catch (SQLException e) {
	    String orids = "while writing origins ";
	    for (OriginExtended origin : origins)
		orids += " " + origin.getOrid();

	    System.out.printf("%s Error %s%n%s", new Date().toString(), orids, 
		    e.getMessage());

	    try {
		outputSchema.getConnection().rollback();
		System.out.println("Rollback successful.");
	    } catch (Exception e1) {
		System.out.println("Rollback failed.");
		e1.printStackTrace();
	    }
	    count = null;
	    throw new Exception(orids,e);
	}

	return count;
    }

    private static BufferedReader getBufferedReader(Map<String, BufferedReader> readers, String key) {
	BufferedReader f = readers.get(key);
	if (f != null)
	    return f;
	f = readers.get(key.toLowerCase());
	if (f != null)
	    return f;
	f = readers.get(key.toUpperCase());
	if (f != null)
	    return f;
	return readers.get(key.substring(0, 1).toUpperCase() + key.substring(1));
    }

    /**
     * Sort the supplied List of Arrivals by orid.
     *
     * @param arrivals
     */
    static public void sortByOrid(List<? extends Origin> origins) {
	Collections.sort(origins, sortByOrid);
    }

    /**
     * Sort the supplied List of Arrivals by evid.
     *
     * @param arrivals
     */
    static public void sortByEvid(List<? extends Origin> origins) {
	Collections.sort(origins, sortByEvid);
    }

    /**
     * Sort the supplied List of Arrivals by time.
     *
     * @param arrivals
     */
    static public void sortByTime(List<? extends Origin> origins) {
	Collections.sort(origins, sortByTime);
    }

    /**
     * Sort the supplied List of Arrivals by ndef.
     *
     * @param arrivals
     */
    static public void sortByNdef(List<? extends Origin> origins) {
	Collections.sort(origins, sortByNdef);
    }

    /**
     * Sort the supplied List of Arrivals by nass.
     *
     * @param arrivals
     */
    static public void sortByNdefDescending(List<? extends Origin> origins) {
	Collections.sort(origins, sortByNdefDescending);
    }

    /**
     * Sort the supplied List of Arrivals by nass.
     *
     * @param arrivals
     */
    static public void sortByNass(List<? extends Origin> origins) {
	Collections.sort(origins, sortByNass);
    }

    /**
     * Sort the supplied List of Arrivals by nass.
     *
     * @param arrivals
     */
    static public void sortByNassDescending(List<? extends Origin> origins) {
	Collections.sort(origins, sortByNassDescending);
    }

    /**
     * Copy the specified origins into an ArrayList<OriginExtended>, sort the list using the specified
     * Comparator, and return the list.
     *
     * @param origins
     * @param comparator
     * @return
     */
    static public ArrayList<OriginExtended> sort(Collection<? extends OriginExtended> origins,
	    Comparator<Origin> comparator) {
	ArrayList<OriginExtended> sorted = new ArrayList<OriginExtended>(origins);
	Collections.sort(sorted, comparator);
	return sorted;
    }

    /**
     * Count all the different types of rows. Keys are 'Origin', 'Assoc', etc.
     *
     * @param origins
     * @param print
     * @return
     * @throws Exception
     */
    static public LinkedHashMap<String, Integer> getCounts(
	    Collection<? extends OriginExtended> origins, boolean print) throws Exception {
	int norigerrs = 0;
	int nazgaps = 0;
	int nassocs = 0;
	int narrivals = 0;
	NetworkExtended network = new NetworkExtended();
	for (OriginExtended origin : origins) {
	    if (origin.getOrigerr() != null)
		++norigerrs;
	    if (origin.getAzgap() != null)
		++nazgaps;
	    nassocs += origin.getAssocs().size();
	    for (AssocExtended assoc : origin.getAssocs().values()) {
		if (assoc.getArrival() != null) {
		    ++narrivals;
		    if (assoc.getArrival().getSite() != null)
			network.add(assoc.getArrival().getSite());
		}

	    }
	}

	int nsites = network.getSites().size();

	LinkedHashMap<String, Integer> counts = new LinkedHashMap<String, Integer>();

	counts.put("Origin", origins.size());
	if (norigerrs > 0)
	    counts.put("Origerr", norigerrs);
	if (nazgaps > 0)
	    counts.put("Azgap", nazgaps);
	if (nassocs > 0)
	    counts.put("Assoc", nassocs);
	if (narrivals > 0)
	    counts.put("Arrival", narrivals);
	if (nsites > 0)
	    counts.put("Site", nsites);

	if (print)
	    for (Entry<String, Integer> entry : counts.entrySet())
		System.out.printf("%-10s %6d%n", entry.getKey(), entry.getValue());

	return counts;
    }

    public HashMap<Long, ArrivalExtended> getCommonArrivals(OriginExtended other) {
	HashMap<Long, ArrivalExtended> arrivals = new HashMap<Long, ArrivalExtended>((int) getNass());
	for (Long arid : getArrivals().keySet()) {
	    ArrivalExtended otherArrival = other.getArrivals().get(arid);
	    if (otherArrival != null)
		arrivals.put(arid, otherArrival);
	}
	return arrivals;
    }

    /**
     * Iterate through all the Sites attached to the arrivals associated with this Origin and add them
     * to a Network. If two arrivals reference different instances of Site which are in fact equal
     * (same sta-ondate) then the duplicate references are replaced with references to the unique
     * reference.
     *
     * @param arrivals
     * @return Network populated with all the unique sites.
     */
    public NetworkExtended getNetwork() {
	return getNetwork(new NetworkExtended());
    }

    /**
     * Iterate through all the Sites attached to the arrivals associated with this Origin and add them
     * to a Network. If two arrivals reference different instances of Site which are in fact equal
     * (same sta-ondate) then the duplicate references are replaced with references to the unique
     * reference.
     *
     * @param arrivals
     * @return Network populated with all the unique sites.
     */
    public NetworkExtended getNetwork(NetworkExtended network) {
	for (AssocExtended a : assocs.values())
	    if (a.getSite() != null) {
		SiteExtended site = network.getSite(a.getSite().getSta(), a.getSite().getOndate());
		if (site == null)
		    network.add(a.getSite());
		else
		    a.getArrival().setSite(site);
	    }
	return network;
    }

    /**
     * Iterate through all the Sites attached to the arrivals associated with this Origin and add them
     * to a Network. If two arrivals reference different instances of Site which are in fact equal
     * (same sta-ondate) then the duplicate references are replaced with references to the unique
     * reference.
     *
     * @param arrivals
     * @return Network populated with all the unique sites.
     */
    public static NetworkExtended getNetwork(Collection<OriginExtended> origins) {
	NetworkExtended network = new NetworkExtended();
	for (OriginExtended o : origins)
	    network = o.getNetwork(network);
	return network;
    }


    public Buff getBuff() {
	      Buff buffer = new Buff(this.getClass().getSimpleName());
	      buffer.insert(super.getBuff());
	      
	      buffer.add("nOrigerrs", origerr == null ? 0 : 1);
	      if (origerr != null) buffer.add(origerr.getBuff());
	      
	      buffer.add("nAzgaps", azgap == null ? 0 : 1);
	      if (azgap != null) buffer.add(azgap.getBuff());
	      
	      buffer.add("nAssocs", assocs.size());
	      TreeSet<Long> arids = new TreeSet<>(assocs.keySet());
	      for (Long arid : arids)
		  buffer.add(assocs.get(arid).getBuff());
	      
	      return buffer;
	  }

    static public Buff getBuff(Scanner input) {
	Buff buf = new Buff(input);
	
	for (int i=0; i<buf.getInt("nOrigerrs"); ++i)
	    buf.add(Origerr.getBuff(input));
	for (int i=0; i<buf.getInt("nAzgaps"); ++i)
	    buf.add(Azgap.getBuff(input));
	for (int i=0; i<buf.getInt("nAssocs"); ++i)
	    buf.add(AssocExtended.getBuff(input));

	return buf;
	
    }
    
}
