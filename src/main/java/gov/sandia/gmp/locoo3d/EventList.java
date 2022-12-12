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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.DBTableTypes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;

/**
 * <p>
 * Title: LocOO
 * </p>
 * 
 * <p>
 * Description: Seismic Event Locator
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
@SuppressWarnings("serial")
public class EventList extends HashMap<Long, Event>
{

	// if this is true, and correlated observations is on,
	// then voluminous output related to the correlation info
	// is output to the logger.
	protected static boolean debugCorrelatedObservations = false;

	protected PropertiesPlusGMP properties;
	protected EventParameters parameters;

	protected ScreenWriterOutput logger, errorlog;

	public static enum CorrelationMethod { UNCORRELATED, FILE, FUNCTION }

	private boolean masterEventCorrectionsOn;
	private boolean masterEventUseOnlyStationsWithCorrections;
	
	/**
	 * sballar 7/12/2022
	 */
	PredictorFactory predictorFactory;

	/**
	 * 
	 * @param properties
	 * @param logger
	 * @param errorlog
	 * @param sources
	 * @param observations Map from orid (or sourceid) -> list of observations
	 * @param receivers Map from receiverId -> receiver
	 * @param predictors
	 * @throws Exception 
	 * @parameters masterEventCorrections
	 */
	public EventList(PropertiesPlusGMP properties, ExecutorService predictionsThreads, 
	    String predictorPrefix, ScreenWriterOutput logger, ScreenWriterOutput errorlog, 
		Collection<Source> sources, HashMap<Long, ArrayList<LocOOObservation>> observations, 
		HashMap<Long, Receiver> receivers, HashMap<String, double[]> masterEventCorrections)
					throws Exception
	{
	    this.parameters = new EventParameters(properties,predictorPrefix,predictionsThreads,logger,
	        errorlog);
		this.properties = properties;
		this.logger = logger;
		this.errorlog = errorlog;

		// master event corrections;
		double[] meCorr = new double[] {Assoc.TIMERES_NA, Assoc.AZRES_NA, Assoc.SLORES_NA};
		
		HashMap<SeismicPhase, Predictor> phasePredictorMap = new HashMap<>();

		for (Source source : sources)
		{
			ArrayList<LocOOObservation> obslist = observations.get(source.getSourceId());

			if (obslist == null || obslist.isEmpty())
				// this check added by sb 2013-10-01
				errorlog.writeln(String.format(
						"%nIgnoring event orid=%d evid=%d because it has no associated observations%n",
						source.getSourceId(), source.getEvid()));
			else
			{
				Event event = new Event(parameters, source);

				this.put(source.getSourceId(), event);

				for (LocOOObservation obs : obslist)
				{
					Receiver receiver = receivers.get(obs.receiverid);
					if (receiver == null)
						errorlog.writeln(String.format(
								"%nIgnoring observation that has no receiver associated with it. "
										+"orid %d, evid %d, arid %d, receiverId %d%n",
										source.getSourceId(), source.getEvid(),
										obs.observationid, obs.receiverid));
					else if (obs.phase == SeismicPhase.NULL)
						errorlog.writeln(String.format(
								"%nIgnoring observation with unrecognized phase %s. "
										+"orid %d, evid %d, arid %d, receiverId %d%n",
										obs.phaseName,
										source.getSourceId(), source.getEvid(),
										obs.observationid, obs.receiverid));
					else
					{
						if (masterEventCorrectionsOn)
						{
							// Units are tt (sec), az (radians), sh (sec/radian)
							meCorr = masterEventCorrections.get(receiver.getSta()+"/"+obs.phaseName);
							if (meCorr == null)
							{
								meCorr = new double[] {Assoc.TIMERES_NA, Assoc.AZRES_NA, Assoc.SLORES_NA};
								if (masterEventUseOnlyStationsWithCorrections)
								{
									obs.timedef = 'n';
									obs.azdef = 'n';
									obs.slodef = 'n';
								}
							}
						}

						Predictor predictor = phasePredictorMap.get(obs.phase);
						if (predictor == null)
						{
							predictor = parameters.predictorFactory().getPredictor(obs.phase);
							phasePredictorMap.put(obs.phase, predictor);
						}
						event.addArrival(new Arrival(receivers.get(obs.receiverid), 
						    event.getSource(), event.getEventParameters(), obs, 
								predictor, meCorr));
					}
				}

				event.checkStationsAndPhases();
			}
		}		
	}

	/**
	 * After calling locateEvents() call this method to extract the results in the 
	 * form of a LocooTaskResult object.
	 * @param results
	 * @throws GMPException 
	 * @throws IOException 
	 */
	public void setResults(LocOOTaskResult results) 
			throws GMPException, IOException
	{
		//Set<DBTableTypes> tables = new HashSet<DBTableTypes>();
		EnumSet<DBTableTypes> tables = EnumSet.noneOf(DBTableTypes.class);

		String outputTableTypes = properties.getProperty("outputTableTypes");
		if (outputTableTypes == null)
			outputTableTypes = properties.getProperty("dbOutputTableTypes");

		if (outputTableTypes == null)
			return;

		for (String tableType : outputTableTypes.split(","))
		{
			tableType = tableType.trim();
			if (tableType.length() > 0)
			{
				DBTableTypes type = DBTableTypes.valueOf(tableType.toUpperCase());
				if (type == null)
					errorlog.writeln(tableType
							+" is specified in property file with parameter "
							+"dbOutputTableTypes but is not an element of enum DBTableTypes");
				else
					tables.add(type);
			}
		}

		int index=0;
		for (Event event : this.values())
			results.addResult(new LocOOResult(index++, event, tables, results.getOriginalArrivals()));
	}

	public PropertiesPlusGMP getProperties()
	{
		return properties;
	}
}
