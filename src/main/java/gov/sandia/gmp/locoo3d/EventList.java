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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;

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
public class EventList extends LinkedHashMap<Long, Event>
{

    protected PropertiesPlusGMP properties;
    protected EventParameters parameters;

    protected ScreenWriterOutput logger, errorlog;

    public static enum CorrelationMethod { 
	/**
	 * Observations are uncorrelated.
	 */
	UNCORRELATED,
	
	/**
	 * Correlation coefficients read from a file.
	 */
	FILE, 
	
	/**
	 * Correlation coefficients will be computed based on station separation.
	 * cc = exp(-sqr(delta/scale))
	 */
	FUNCTION1, 
	
	/**
	 * cc = exp(-sqr(min(dist_between_stations/dist_sta1_to_event,
	      dist_between_stations/dist_sta2_to_event)/scale_factor))
	 */
	FUNCTION2, 
	
	/**
	 * Correlation coefficients will have been obtained from the Source object 
	 * and set in the Event constructor.
	 */
	SOURCE }

    /**
     * 
     * @param properties
     * @param predictionsThreads
     * @param logger
     * @param errorlog
     * @param sources
     * @param masterEventCorrections
     * @throws Exception
     */
    public EventList(PropertiesPlusGMP properties, ExecutorService predictionsThreads, 
	    ScreenWriterOutput logger, ScreenWriterOutput errorlog, 
	    Collection<Source> sources, Map<String, double[]> masterEventCorrections)
		    throws Exception
    {
	this.parameters = new EventParameters(properties, predictionsThreads, logger, errorlog);
	this.properties = properties;
	this.logger = logger;
	this.errorlog = errorlog;
	this.parameters = new EventParameters(properties,predictionsThreads,
        logger,errorlog);


	for (Source source : sources)
	{
	    if (source.getObservations() == null || source.getObservations().isEmpty())
		// this check added by sb 2013-10-01
		errorlog.writeln(String.format(
			"%nIgnoring event orid=%d evid=%d because it has no associated observations%n",
			source.getSourceId(), source.getEvid()));
	    else
	    {
		Event event = new Event(parameters, source, masterEventCorrections);

		this.put(source.getSourceId(), event);

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
	    throws Exception
    {
      results.setTaskLog(this.logger);
      results.setTaskErrorLog(this.errorlog);
      
	Map<Long, Source> sources = new TreeMap<>();
	for (Event event : this.values()) {
	    Source source = event.getSource();
	    sources.put(source.getSourceId(), source);
	}
	results.setSources(sources);

    }

    public PropertiesPlusGMP getProperties()
    {
	return properties;
    }
}
