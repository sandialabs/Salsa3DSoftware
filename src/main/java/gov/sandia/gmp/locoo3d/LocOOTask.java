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

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.parallelutils.ParallelTask;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.profiler.Profiler;

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
    private Collection<Source> sources;

    private LocOOTaskResult results;

    private transient ExecutorService predThreads = null;
    
    /**
     * Default constructor.
     */
    public LocOOTask() {
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
    public LocOOTask(PropertiesPlusGMP properties, Collection<Source> sources)
    {
	this.properties = properties;
	this.sources = sources;
    }

    @Override
    public void run()
    {
	VectorGeo.earthShape = EarthShape.valueOf(
		properties.getProperty("earthShape", "WGS84"));

	index = nextIndex++;
	results = new LocOOTaskResult();
	setResult(results); 
	
	ScreenWriterOutput errorlog = new ScreenWriterOutput();
	errorlog.setBufferOutputOn();

	Profiler profiler = null;
	try
	{
	    ScreenWriterOutput logger = new ScreenWriterOutput();
	    logger.setVerbosity(properties.getInt("io_verbosity", 0));
	    logger.setBufferOutputOn();
	    logger.setScreenOutputOff();

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
			orids.append(String.format(", %d(%d)", s.getSourceId(), s.getNass()));
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

	    EventList eventList = new EventList(properties, predThreads, predictorPrefix, logger,
		    errorlog, sources);

	    (new SolverLSQ(properties)).locateEvents(eventList);

	    eventList.setResults(results);

	    // turn off profiler if on and set into results
	    if (profiler != null)
	    {
		profiler.stop();
		profiler.printAccumulationString();
		results.setProfilerContent(profiler.getProfilerContent());
		profiler = null;
	    }      

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

	    results.clear(); // CLEAR ALL RESULTS!!
	    errorlog.write("Task was supposed to process the following source IDs: ");
	    for (Source s : getSources())
		errorlog.write(s.getSourceId() + " ");
	    errorlog.writeln();
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

    public Collection<Source> getSources()
    {
	return sources;
    }

    public void setSources(Collection<Source> sources)
    {
	this.sources = sources;
    }

    public void setPredictionsThreadPool(ExecutorService es) {
	predThreads = es;
    }

    public int getOriginCount() { return sources.size(); }

    public int getTotalNDef() {  
	int ndef = 0;
	for (Source source : sources)
	    for (Observation obs : source.getObservations().values())
		if (obs.isDefining())
		    ++ndef;
	return ndef;
    }

    @Override
    public LocOOTaskResult getResultObject() {
	return results;
    }

    @Override
    public Object getSharedObject(String key) throws Exception {
	throw new UnsupportedOperationException();
    }

}
