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
package gov.sandia.gmp.locoo3d;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.parallelutils.ParallelTask;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.profiler.Profiler;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.ArrivalExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.NetworkExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.SiteExtended;

/**
 * 
 * @author sballar
 * 
 */
public class LocOOTask extends ParallelTask
{
	private static int nextIndex = 0;
	private int index;

	/**
	 * 
	 */
	private static final long serialVersionUID = 2279445066935187710L;

	/**
	 * 
	 */
	private PropertiesPlusGMP properties;

	/**
	 * Sources
	 */
	private ArrayList<Source> sources;

	/**
	 * Map ReceiverID -> Receiver
	 */
	private HashMap<Long, Receiver> receivers;

	/**
	 * Map SourceId -> ArrayList of Observations
	 */
	private HashMap<Long, ArrayList<LocOOObservation>> observations;
	
	private HashMap<Long, ArrivalExtended> originalArrivals;

	private LocOOTaskResult results;
	
	/**
	 * Map from sta/phase -> tt,az,sh corrections for master event relocation.
	 * Units are tt (sec), az (radians), sh (sec/radian)
	 */
	private HashMap<String, double[]> masterEventCorrections;
	
	private transient ExecutorService predThreads = null;
	
	/**
	 * Default constructor.
	 */
	public LocOOTask()
	{
		this.originalArrivals = new HashMap<Long, ArrivalExtended>();
	}

	/**
	 * 
	 * @param properties
	 * @param ArrayList
	 *            <Source> sources
	 * @param HashMap
	 *            <Long, ArrayList<LocOOObservation>> observations Map SourceId
	 *            -> ArrayList of LocOOObservation objects
	 * @param HashMap
	 *            <Long, Receiver> receivers Map ReceiverID -> Receiver
	 */
	public LocOOTask(PropertiesPlusGMP properties, ArrayList<Source> sources,
			HashMap<Long, ArrayList<LocOOObservation>> observations,
			HashMap<Long, Receiver> receivers)
	{
		this.properties = properties;
		this.sources = sources;
		this.observations = observations;
		this.receivers = receivers;
		this.originalArrivals = new HashMap<Long, ArrivalExtended>();
	}

	/**
	 * Construct a LocOOTask object from a set of origin/assoc/arrival/site
	 * Row objects.  This method will sort the input into a list of Sources,
	 * a map from orid -> list of LocOOObservation, and a map from 
	 * receiverId -> Receiver.  A LocOOObservation object basically contains
	 * the key columns from an assoc and an arrival.
	 * @param properties
	 * @param origins
	 * @param assocs
	 * @param arrivals
	 * @param sites
	 * @throws GMPException 
	 */
	public LocOOTask(PropertiesPlusGMP properties, Collection<? extends Origin> origins,
			Collection<? extends Assoc> assocs, Collection<? extends Arrival> arrivals,
			Collection<? extends Site> sites) throws GMPException
	{
		this.properties = properties;

		this.originalArrivals = new HashMap<Long, ArrivalExtended>();
		
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
		
		sources = new ArrayList<Source>(origins.size());
		
		// observations is a map from orid -> list of (assoc+arrival).
		observations = new HashMap<Long, ArrayList<LocOOObservation>>();
		
		// map from receiverId to Receiver
		receivers = new HashMap<Long, Receiver>();

		
		for (Origin origin : origins)
		{
			sources.add(new Source(origin));
			observations.put(origin.getOrid(), new ArrayList<LocOOObservation>());
		}
		
		Receiver receiver;
		
		// map from sta -> list of Receivers for that sta but with different on-off dates.
		HashMap<String, HashSet<Receiver>> stations = new HashMap<String, HashSet<Receiver>>();
		
		NetworkExtended network = new NetworkExtended();
		
		for (Site site : sites)
			if (!ignoreSites.contains(site.getSta()))
			{
				receiver = new Receiver(site);
				receivers.put(receiver.getReceiverId(), receiver);
				HashSet<Receiver> station = stations.get(receiver.getSta());
				if (station == null)
				{
					station = new HashSet<Receiver>();
					stations.put(receiver.getSta(), station);
				}
				station.add(receiver);
				
				network.add(new SiteExtended(site));
			}
		
		// map from arid -> arrivalRow
		for (Arrival arrival : arrivals)
		{
			//arrivalRows.put(arrival.getArid(), arrival);
			
			if (arrival instanceof ArrivalExtended)
				originalArrivals.put(arrival.getArid(), (ArrivalExtended) arrival);
			else
				originalArrivals.put(arrival.getArid(), new ArrivalExtended(arrival, network));
		}
		
		for (Assoc assoc : assocs)
			if (observations.containsKey(assoc.getOrid()) 
					&& !ignorePhases.contains(assoc.getPhase())
					&& !ignoreSites.contains(assoc.getSta())
					)
		{
			Arrival arrival = originalArrivals.get(assoc.getArid());
			
			if (arrival == null)
				throw new GMPException(String.format("\nHave assoc orid=%d, arid=%d, sta=%s but no arrival with that arid%n",
						assoc.getOrid(), assoc.getArid(), assoc.getSta()));
			
			receiver = null;
			HashSet<Receiver> station = stations.get(arrival.getSta());
			if (station == null)
				throw new GMPException(String.format("\nFound AssocRow orid=%d, arid=%d, sta=%s but no Sites with that sta%n",
						assoc.getOrid(), assoc.getArid(), assoc.getSta()));
			
			for (Receiver r : station)
				if (r.validJDate((int)arrival.getJdate()))
				{
					receiver = r;
					break;
				}
			
			if (receiver == null)
			{
				StringBuffer buf = new StringBuffer();
				buf.append(String.format("\nFound ArrivalRow arid=%d, sta=%s, jdate=%d but no corresponding site%n",
						arrival.getArid(), arrival.getSta(), arrival.getJdate()));
				buf.append(String.format("There are %d available SiteRows with sta=%s:%n", 
						station.size(),  arrival.getSta()));
				for (Receiver r : station)
					buf.append(String.format("   sta=%s  ondate=%d offdate=%d%n", r.getSta(), r.getOndate(), r.getOffdate()));
				buf.append("\n");
				throw new GMPException(buf.toString());
			}
			
			LocOOObservation observation = new LocOOObservation(arrival.getArid(), receiver.getReceiverId(), 
					assoc.getPhase(), 
					arrival.getTime(), arrival.getDeltim(), assoc.getTimedef().charAt(0), 
					Math.toRadians(arrival.getAzimuth()), Math.toRadians(arrival.getDelaz()), assoc.getAzdef().charAt(0), 
					Math.toDegrees(arrival.getSlow()), Math.toDegrees(arrival.getDelslo()), assoc.getSlodef().charAt(0)); 
			
			observations.get(assoc.getOrid()).add(observation);
		}

	}

	/**
	 * Construct a LocOOTask object from a set of origin/assoc/arrival/site
	 * Row objects.  This method will sort the input into a list of Sources,
	 * a map from orid -> list of LocOOObservation, and a map from 
	 * receiverId -> Receiver.  A LocOOObservation object basically contains
	 * the key columns from an assoc and an arrival.
	 * @param properties
	 * @param origins
	 * @param assocs
	 * @param arrivals
	 * @param sites
	 * @throws GMPException 
	 */
	public LocOOTask(PropertiesPlusGMP properties, Collection<? extends OriginExtended> origins) throws GMPException
	{
		this.properties = properties;
		
		VectorGeo.earthShape = EarthShape.valueOf(
				properties.getProperty("earthShape", "WGS84"));

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
		
		sources = new ArrayList<Source>(origins.size());
		
		// observations is a map from orid -> list of (assoc+arrival).
		observations = new HashMap<Long, ArrayList<LocOOObservation>>();
		
		// map from receiverId to Receiver
		receivers = new HashMap<Long, Receiver>();

		// map from sta -> ondate -> Receiver
		HashMap<String, HashMap<Long, Receiver>> stations = new HashMap<>();
		
		originalArrivals = new HashMap<Long, ArrivalExtended>();
		
		for (OriginExtended origin : origins)
		{
			sources.add(new Source(origin));
			ArrayList<LocOOObservation> originObservations = new ArrayList<LocOOObservation>();
			observations.put(origin.getOrid(), originObservations);

			for (AssocExtended assoc : origin.getAssocs().values())
			{
				ArrivalExtended arrival = assoc.getArrival();
				
				if (arrival == null)
				{
					//throw new GMPException(
					System.out.print(String.format("LocOOTask constructor: Assoc orid=%d arid=%d sta=%s phase=%s has no associated arrival. The assoc is being ignored.%n",
							assoc.getOrid(), assoc.getArid(), assoc.getSta(), assoc.getPhase()));
					continue;
				}
				
				originalArrivals.put(arrival.getArid(), arrival);
				
				SiteExtended site = arrival.getSite();
				
				if (site == null)
				{
					//throw new GMPException(
					System.out.print(String.format("LocOOTask constructor: Arrival arid=%d sta=%s iphase=%s jdate=%d has no associated site%n",
							arrival.getArid(), arrival.getSta(), arrival.getIphase(), arrival.getJdate()));
					continue;
				}
				
				if (ignoreSites.contains(site.getSta())) continue;
				
				Receiver receiver = new Receiver(site);
				
				HashMap<Long, Receiver> station = stations.get(receiver.getSta());
				if (station == null)
				{
					station = new HashMap<Long, Receiver>();
					stations.put(receiver.getSta(), station);
				}
				
				Receiver r = station.get(receiver.getOndate());
				if (r == null) station.put(receiver.getOndate(), receiver);
				else receiver = r;

				receivers.put(receiver.getReceiverId(), receiver);

				LocOOObservation observation = new LocOOObservation(arrival.getArid(), receiver.getReceiverId(), 
						assoc.getPhase(),
						arrival.getTime(), arrival.getDeltim(), assoc.getTimedef().charAt(0), 
						Math.toRadians(arrival.getAzimuth()), Math.toRadians(arrival.getDelaz()), assoc.getAzdef().charAt(0), 
						Math.toDegrees(arrival.getSlow()), Math.toDegrees(arrival.getDelslo()), assoc.getSlodef().charAt(0)); 
				
				originObservations.add(observation);
			}
		}
		
	}

	@Override
	public void run()
	{
		VectorGeo.earthShape = EarthShape.valueOf(
				properties.getProperty("earthShape", "WGS84"));

		index = nextIndex++;
		results = new LocOOTaskResult(sources.size());
		setResult(results); // Set JPPF result
		
		results.setOriginalArrivals(originalArrivals);

		ScreenWriterOutput errorlog = new ScreenWriterOutput();
		errorlog.setBufferOutputOn();

		// ensure that results and errorlog share a reference to the
		// same StringBuffer, which will ensure that all error
		// messages get passed back in results object.
		results.setErrorlog(errorlog.getStringBuffer());

		Profiler profiler = null;
		try
		{
			ScreenWriterOutput logger = new ScreenWriterOutput();
			logger.setVerbosity(properties.getInt("io_verbosity", 0));
			logger.setBufferOutputOn();
			if (properties.getBoolean("io_print_to_screen", true))
			    logger.setScreenOutputOn();
			else
			    logger.setScreenOutputOff();

			// ensure that results and logger share a reference to the
			// same StringBuffer, which might be null. This will
			// ensure that all logged information will be returned in
			// results.
			results.setLog(logger.getStringBuffer());

			String hostname = Globals.getComputerName();
			
			if (logger.getVerbosity() >= 1)
			{
			    logger.write(String.format(
				    "Status Log - Starting LoOOTask %6d on %s %s%n", index,
				    hostname, GMTFormat.localTime.format(new Date())));

			    if (logger.getVerbosity() == 1)
			    {
				StringBuffer orids = new StringBuffer();
				for (Source s : sources)
				    orids.append(String.format(", %d(%d)", s.getSourceId(), s.getNumberOfAssocs()));
			    }
			}

			// create profiler if requested
			long profilerSamplePeriod = properties.getInt("profilerSamplePeriod", -1);
			if (profilerSamplePeriod > 0)
			{
				profiler = new Profiler(Thread.currentThread(), profilerSamplePeriod,
                                "LocOOTask:" + hostname);
				profiler.setTopClass("gov.sandia.gmp.locoo3d.LocOOTask");
				profiler.setTopMethod("run");
				profiler.accumulateOn();
			}

			// Create the predictors, using the PredictorFactory
			String predictorPrefix = "loc_predictor_type";
			
			logger.setScreenOutputOn();
			EventList eventList = new EventList(properties, predThreads, predictorPrefix, logger,
			    errorlog, sources, observations, receivers, masterEventCorrections);
			
			(new SolverLSQ(properties))
			  .locateEvents(eventList);
      
			eventList.setResults(results);

			// turn off profiler if on and set into results
			if (profiler != null)
			{
				profiler.stop();
				profiler.printAccumulationString();
				results.setProfilerContent(profiler.getProfilerContent());
				profiler = null;
			}      

			if (logger.getVerbosity() >= 1)
				logger.write(String.format(
						"Status Log - Finished LoOOTask %6d on %s %s%n", index,
						hostname, GMTFormat.localTime.format(new Date())));
		}
		catch (Exception e)
		{
		    e.printStackTrace();
			// turn off profiler if on
			if (profiler != null)
			{
				profiler.stop();
				profiler.printAccumulationString();
				profiler = null;
			}      

			results.getResults().clear(); // CLEAR ALL RESULTS!!
			errorlog.write("Task was supposed to process the following source IDs: ");
			for (Source s : getSources())
			{
				errorlog.write(s.getSourceId() + " ");
			}
			errorlog.writeln("");
			errorlog.write(e);
		}
	}

	public PropertiesPlusGMP getProperties()
	{
		return properties;
	}

	public void setProperties(PropertiesPlusGMP properties)
	{
		this.properties = properties;
	}

	public ArrayList<Source> getSources()
	{
		return sources;
	}

	public void setSources(ArrayList<Source> sources)
	{
		this.sources = sources;
	}

	public HashMap<Long, Receiver> getReceivers()
	{
		return receivers;
	}
	
	public void setPredictionsThreadPool(ExecutorService es) {
	  predThreads = es;
	}

	public void setReceivers(HashMap<Long, Receiver> receivers)
	{
		this.receivers = receivers;
	}

	public HashMap<Long, ArrayList<LocOOObservation>> getObservations()
	{
		return observations;
	}
	
	public int getOriginCount() { return observations.size(); }
	
	public int getTotalNDef() { return observations.values().stream().mapToInt(List::size).sum(); }

	public void setObservations(
			HashMap<Long, ArrayList<LocOOObservation>> observations)
	{
		this.observations = observations;
	}

	@Override
	public LocOOTaskResult getResultObject()
	{
		return results;
	}

	@Override
	public Object getSharedObject(String key) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * Map from sta/phase -> tt,az,sh corrections for master event relocation.
	 * Units are tt (sec), az (radians), sh (sec/radian)
	 */
	public void setMasterEventCorrections(
			HashMap<String, double[]> masterEventCorrections) {
		this.masterEventCorrections = masterEventCorrections;
	}

}
