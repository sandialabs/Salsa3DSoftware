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
package gov.sandia.gmp.locoo3d.io;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.locoo3d.LocOOTask;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.ArrivalExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.SiteExtended;

public class KBInput extends NativeInput {

    /**
     * Map from orid to OriginExtended object, which has references to associated
     * Assocs, Arrivals and Sites.  These input origins can be specified in constructors
     * defined below, or can be retrieved from files or databases.
     */
    protected Map<Long, OriginExtended> inputOrigins;
    
    /**
     * True if output location uncertainty info is requested by the application
     */
    boolean uncertaintyRequested;

    /**
     * True if output azgap info is requested by the application
     */
    boolean azgapRequested;

    /**
     * True if output assoc info is requested by the application
     */
    boolean assocsRequested;

    /**
     * True if output arrival info is requested by the application
     */
    boolean arrivalsRequested;

    /**
     * True if output site info is requested by the application
     */
    boolean sitesRequested;

   /**
     * Map from gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.SiteExtended to 
     * gov.sandia.gmp.baseobjects.Receiver.  This map is used during input of data
     * so that multiple references to the same Site don't generate many new 
     * Receiver objects.
     */
    protected Map<SiteExtended, Receiver> siteReceiverMap = new HashMap<>();

    /**
     * Default constructor does nothing.
     */
    public KBInput() {
	super();
    }

    /**
     * 
     * @param properties
     * @param logger
     * @param errorlog
     * @throws Exception
     */
    public KBInput(PropertiesPlusGMP properties) throws Exception {
	super(properties);
	
	String tableTypes = properties.getProperty("dbOutputTableTypes", properties.getProperty("outputTableTypes", " ")).toLowerCase();

	uncertaintyRequested = tableTypes.contains("origerr") || properties.containsKey("dataLoaderFileOutputOrigerrs")
		|| properties.containsKey("dbOutputOrigerrTable");

	azgapRequested = tableTypes.contains("azgap") || properties.containsKey("dataLoaderFileOutputAzgaps")
		|| properties.containsKey("dbOutputAzgapTable");

	assocsRequested = tableTypes.contains("assoc") || properties.containsKey("dataLoaderFileOutputAssocs")
		|| properties.containsKey("dbOutputAssocTable");

	arrivalsRequested = tableTypes.contains("arrival") || properties.containsKey("dataLoaderFileOutputArrivals")
		|| properties.containsKey("dbOutputArrivalTable");
	
	sitesRequested = tableTypes.contains("site") || properties.containsKey("dataLoaderFileOutputSites")
		|| properties.containsKey("dbOutputSiteTable");
	
	inputOrigins = new LinkedHashMap<>();

    }
    
    public void setInputOrigins(Collection<OriginExtended> origins) {
	inputOrigins = new LinkedHashMap<Long, OriginExtended>(origins.size());
	for (OriginExtended o : origins)
	    inputOrigins.put(o.getOrid(), o);
    }

    /**
     * This method is called by LocOO to retrieve input data.  Applications should
     * not call this.
     * Query the data and retrieve batches of orid|sourceid such that each batch has
     * less than some number of time defining phases.  Returns a 2D ragged array of longs.
     * The first index spans the batches and the second spans the orids|sourceids in each
     * batch.
     * <p>The maximum number of time defining phases in a batch is retrieve from the 
     * input property file with property batchSizeNdef, which defaults to 100.  
     */
    @Override
    public ArrayList<ArrayListLong> readTaskSourceIds() throws SQLException, GMPException
    {
	int ndefMax = properties.getInt("batchSizeNdef", 100);

	for (OriginExtended origin: inputOrigins.values())
	    origin.setNdef();

	List<OriginExtended> origins = new ArrayList<>(inputOrigins.values());

	OriginExtended.sortByNdefDescending(origins);

	int count=0;
	ArrayList<ArrayListLong> batches = new ArrayList<ArrayListLong>();

	ArrayListLong batch = new ArrayListLong();
	batches.add(batch);
	long n=0, ndef;
	for (OriginExtended origin : origins)
	{
	    ++count;
	    ndef = origin.getNdef();
	    if (ndef <= 0)
		ndef = 10;
	    if (batch.size() > 0 && n+ndef > ndefMax)
	    {
		batch = new ArrayListLong();
		batches.add(batch);
		n = 0;
	    }
	    batch.add(origin.getOrid());
	    n += ndef;
	}


	if (logger.isOutputOn() && logger.getVerbosity() > 0)
	    logger.write(String.format("%d Sources divided among %d batches with number of time defining phases approximately %d in each batch%n",
		    count, batches.size(), ndefMax));

	long check = 0;
	for (ArrayListLong b : batches)
	    check += b.size();

	if (check != count)
	    throw new GMPException(String.format("Sum of batch sizes (%d) != data size (%d)",
		    check, count));

	return batches;
    }

    /**
     * This method is called by LocOO to retrieve indput data.  Applications should
     * not call this.
     * Given a list of orids, retrieve a LocOOTask that includes those 
     * sources.  The data includes the sources as well as the associated observations
     * and receivers.
     * @param orids
     * @return
     * @throws Exception
     */
    @Override
    public LocOOTask readTaskObservations(ArrayListLong orids) throws Exception
    {		
	HashSet<OriginExtended> taskOriginSet = new HashSet<OriginExtended>(orids.size());
	for (int i = 0; i < orids.size(); ++i) 
	    taskOriginSet.add(inputOrigins.get(orids.get(i)));

	LocOOTask task = new LocOOTask(properties, convertOriginsToSources(
		properties, taskOriginSet, masterEventCorrections));

	return task;
    }

    /**
     * Translate Origin/Assoc/Arrival/Site to Source/Observation/Receiver.
     * @param properties
     * @param origins
     * @param masterEventCorrections
     * @return
     * @throws Exception
     */
    protected Collection<Source> convertOriginsToSources(PropertiesPlusGMP properties, 
	    Collection<? extends OriginExtended> origins, Map<String, double[]> masterEventCorrections) throws Exception
    {
	HashSet<String> ignorePhases = new HashSet<String>();
	String property = properties.getProperty("invalidPhases");
	if (property != null)
	{
	    String[] p = property.replaceAll(",", " ").replaceAll("  ", " ").split(" ");
	    for (String s : p)
	    {
		s = s.trim();
		if (s.length() > 0)
		    ignorePhases.add(s);
	    }
	}

	HashSet<String> ignoreSites = new HashSet<String>();
	property = properties.getProperty("invalidSites");
	if (property != null)
	{
	    String[] p = property.replaceAll(",", " ").replaceAll("  ", " ").split(" ");
	    for (String s : p)
	    {
		s = s.trim();
		if (s.length() > 0)
		    ignoreSites.add(s);
	    }
	}

	Collection<Source> sources = new ArrayList<Source>(origins.size());

	for (OriginExtended origin : origins)
	{
	    Source source = new Source(origin.getOrid(), origin.getEvid(),
			new GeoVector(origin.getLat(), origin.getLon(), origin.getDepth(), true), origin.getTime(),
			Globals.NA_VALUE);
	    
	    sources.add(source);
	    
	    for (AssocExtended assoc : origin.getAssocs().values())
	    {
		if (ignoreSites.contains(assoc.getSta())) continue;

		ArrivalExtended arrival = assoc.getArrival();

		if (arrival == null)
		{

		    String emsg = String.format("\nDataInputKB: assoc orid=%d arid=%d sta=%s phase=%s "
			    + "has no associated arrival. The assoc is being ignored.%n",
			    assoc.getOrid(), assoc.getArid(), assoc.getSta(), assoc.getPhase());

		    logger.writeln(emsg);
		    errorlog.writeln(emsg);

		    continue;
		}

		SiteExtended site = arrival.getSite();

		if (site == null)
		{
		    String emsg = String.format("\nDataInputKB: assoc orid=%d arid=%d sta=%s iphase=%s jdate=%d "
			    + "has no associated site. Assoc is being ignored.%n", assoc.getOrid(),
			    arrival.getArid(), arrival.getSta(), arrival.getIphase(), arrival.getJdate());
		    logger.writeln(emsg);
		    errorlog.writeln(emsg);

		    continue;
		}

		Receiver receiver = siteReceiverMap.get(site);
		if (receiver == null) 
		    siteReceiverMap.put(site, receiver=new Receiver(site));

		double[] mecorr = masterEventCorrections.get(assoc.getSta()+"/"+assoc.getPhase());
		if (mecorr == null)
		    mecorr = new double[3];

		Observation o = new Observation(receiver, source, assoc, mecorr);
		

		source.getObservations().put(o.getObservationId(), o);
	    }
	}
		
	checkMasterEventObservations(sources);

	return sources;
    }

}
