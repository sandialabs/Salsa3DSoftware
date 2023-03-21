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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.locoo3d.LocOO;
import gov.sandia.gmp.locoo3d.LocOOTask;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;

public class NativeInput {

    protected PropertiesPlusGMP properties;
    protected ScreenWriterOutput logger;
    protected ScreenWriterOutput errorlog;

    /**
     * Map from sourceId to baseobjects.Source object containing all the input sources.
     * Note that Source contains a reference to a Collection of baseobjects.Observations
     * and each Observation has a reference to a baseobjects.Receiver object.
     * 
     */
    private Map<Long, Source> sources;

    /**
     * Map from sta/phase -> tt,az,sh corrections for master event relocation.
     * Units are tt (sec), az (radians), sh (sec/radian).
     * Default values are 0, 0, 0.
     * Map may be empty but will not be null.
     */
    protected Map<String, double[]> masterEventCorrections;

    public NativeInput() {

    }

    /**
     * Protected so it can only be referenced by classes that extend DataInput
     * @param properties
     * @param logger
     * @param errorlog
     * @throws Exception
     */
    protected NativeInput(PropertiesPlusGMP properties) throws Exception {
	this.properties = properties;
	setupLoggers();
	VectorGeo.earthShape = EarthShape.valueOf(
		properties.getProperty("earthShape", "WGS84"));	

	masterEventCorrections = new HashMap<String, double[]>();
    }

    public NativeInput(PropertiesPlusGMP properties, Collection<Source> sources) throws Exception {
	this(properties);
	setSources(sources);
    }

    /**
     * Factory method to return a concrete DataInput based on the properties
     * file setting "dataLoaderType". Current valid types include "file", "database",
     * and "application".  "oracle" can be specified in place of "database".
     * 
     * @param properties Input LocOO3D Properties object.
     * @param errorlog 
     * @param logger 
     * @return The new concrete DataLoader.
     * @throws Exception 
     */
    public static NativeInput create(PropertiesPlusGMP properties) throws Exception
    {
	/**
	 * One of file, database, application
	 */
	String type = properties.getProperty("dataLoaderInputType", 
		properties.getProperty("dataLoaderType", "")).toLowerCase();

	if (type.equalsIgnoreCase("oracle")) type = "database";

	/**
	 * format is one of kb, gmp, native
	 */
	String format = properties.getProperty("dataLoaderInputFormat", "kb").toLowerCase();

	if (format.equals("native"))
	    return new NativeInput(properties);

	if (format.equals("kb") && type.equals("file"))
	    return new KBFileInput(properties);

	if (format.equals("kb") && type.equals("database"))
	    return new KBDBInput(properties);

	if (format.equals("kb") && type.equals("application"))
	    return new KBInput(properties);


	if (format.equals("gmp") && type.equals("file"))
	    return new GMPFileInput(properties);

	if (format.equals("gmp") && type.equals("database"))
	    return new GMPDBInput(properties);

	if (format.equals("gmp") && type.equals("application"))
	    return new GMPInput(properties);


	// deal with legacy property definitions.

	if (type.equals("file")) {

	    if (properties.containsKey("dataLoaderFileInputOrigins"))    
		return new KBFileInput(properties);

	    if (properties.containsKey("dataLoaderFileInputSources"))    
		return new GMPFileInput(properties);

	    throw new Exception("dataLoaderInputType = "+type+",\n"
		    + "but neither dataLoaderFileInputOrigins nor dataLoaderFileInputSources is specified.");

	}
	else if (type.equals("database")){

	    if ((properties.containsKey("dbInputTableTypes") &&
		    properties.getProperty("dbInputTableTypes").toLowerCase().contains("origin"))
		    || properties.containsKey("dbInputOriginTable"))
		return new KBDBInput(properties);

	    if ((properties.containsKey("dbInputTableTypes") &&
		    properties.getProperty("dbInputTableTypes").toLowerCase().contains("source"))
		    || properties.containsKey("dbInputSourceTable"))
		return new GMPDBInput(properties);

	    throw new Exception("dataLoaderInputType = "+type+",\n"
		    + "but neither dbInputTableTypes nor dbInputOriginTable nor dbInputSourceTable is specified.");

	}
	else if (type.toUpperCase().equals("application")){

	    String inputApplication = properties.getProperty("dataLoaderInputApplication", "?");

	    if (inputApplication.equalsIgnoreCase("KB"))
		return new KBInput(properties);

	    if (inputApplication.equalsIgnoreCase("GMP"))
		return new GMPInput(properties);

	    return new NativeInput(properties);

	}

	throw new Exception("Must specify property dataLoaderInputType = file or database");
    }

    /**
     * This method is called by LocOO to retrieve input data.  Applications should
     * not call this.
     * Given a list of sourceids, retrieve a LocOOTask that includes those 
     * sources.  The data includes the sources as well as the associated observations
     * and receivers.
     * @param sourceids
     * @return
     * @throws Exception
     */
    public LocOOTask readTaskObservations(ArrayListLong sourceids) throws Exception {
	Collection<Source> taskOriginSet = new LinkedHashSet<Source>(sourceids.size());
	for (int i = 0; i < sourceids.size(); ++i)
	    taskOriginSet.add(sources.get(sourceids.get(i)));
	LocOOTask task = new LocOOTask(properties, taskOriginSet, masterEventCorrections);
	return task;
    }

    /**
     * This method is called by LocOO to retrieve input data.  Applications should
     * not call this.
     * Query the data and retrieve batches of sourceid such that each batch has
     * less than some number of time defining phases.  Returns a 2D ragged array of longs.
     * The first index spans the batches and the second spans the orids|sourceids in each
     * batch.
     * <p>The maximum number of time defining phases in a batch is retrieve from the 
     * input property file with property batchSizeNdef, which defaults to 100.  
     */
    public ArrayList<ArrayListLong> readTaskSourceIds() throws Exception {
	// sort the sources by ndef decreasing and populate batches.
	List<Source>list = new ArrayList<>(sources.values());
	Source.sortByNdefDescending(list);

	int batchSizeNdef = (int) list.get((int)(list.size()*0.75)).getNdef();

	batchSizeNdef = properties.getInt("batchSizeNdef", batchSizeNdef);

	ArrayList<ArrayListLong> batches = new ArrayList<>(sources.size());
	ArrayListLong batch = new ArrayListLong();
	int n = 0;
	for (Source source : list) {
	    batch.add(source.getSourceId());
	    n += source.getNdef();
	    if (n >= batchSizeNdef) {
		batches.add(batch);
		batch = new ArrayListLong();
		n = 0;
	    }
	}
	if (batch.size() > 0)
	    batches.add(batch);
	return batches;
    }

    /**
     * This method is called by LocOO when it has finished retrieving data.  Applications should
     * not call this.
     * Perform any operations required to close a a data source (files, database connections,
     * etc.).  The default is to do nothing.
     * @throws Exception
     */
    public void close() throws Exception {}

    public void setSources(Collection<Source> sources) {
	this.sources = new LinkedHashMap<>(sources.size());
	for (Source s : sources)
	    this.sources.put(s.getSourceId(), s);
    }

    public Map<String, double[]> getMasterEventCorrections() {
	return masterEventCorrections;
    }

    public void setMasterEventCorrections(Map<String, double[]> masterEventCorrections) {
	this.masterEventCorrections = masterEventCorrections;
    }

    public PropertiesPlusGMP getProperties() {
	return properties;
    }

    public ScreenWriterOutput getLogger() {
	return logger;
    }

    public ScreenWriterOutput getErrorlog() {
	return errorlog;
    }

    protected Map<String, double[]> getMasterEventCorrections(Source masterEvent, PredictorFactory predictors, 
	    String loggerHeader) throws Exception
    {
	Map<String, double[]> masterEventCorrections = new TreeMap<String, double[]>();

	PredictionRequest request = new PredictionRequest();
	request.setDefining(true);
	request.setSource(new Source(masterEvent));
	request.setRequestedAttributes(
		EnumSet.of(GeoAttributes.TRAVEL_TIME, GeoAttributes.AZIMUTH, GeoAttributes.SLOWNESS));

	for (Observation observation : masterEvent.getObservations().values())
	{
	    SeismicPhase phase = SeismicPhase.valueOf(observation.getPhase());
	    Predictor predictor = predictors.getPredictor(phase);
	    if (predictor != null)
	    {
		request.setReceiver(observation.getReceiver());
		request.setPhase(phase);

		Prediction prediction = predictor.getPrediction(request);
		if (prediction.isValid())
		{
		    // master event corrections for tt, az, sh
		    double[] corr = new double[3];

		    if (observation.getTime() != Globals.NA_VALUE
			    && prediction.getAttribute(GeoAttributes.TRAVEL_TIME) != Globals.NA_VALUE)
			corr[0] = observation.getTime()-masterEvent.getTime()-
			prediction.getAttribute(GeoAttributes.TRAVEL_TIME);

		    if (observation.isAzdef() && observation.getAzimuth() != Globals.NA_VALUE
			    && prediction.getAttribute(GeoAttributes.AZIMUTH) != Globals.NA_VALUE)
		    {
			// everything in radians
			corr[1] = observation.getAzimuth()-prediction.getAttribute(GeoAttributes.AZIMUTH);
			if (corr[1] < -Math.PI) 
			    corr[1] += 2*Math.PI;
			else if (corr[1] > Math.PI) 
			    corr[1] -= 2*Math.PI;
		    }

		    // everything in sec/radian
		    if (observation.getSlow() != Globals.NA_VALUE
			    && prediction.getAttribute(GeoAttributes.SLOWNESS) != Globals.NA_VALUE)
			corr[2] = observation.getSlow()-prediction.getAttribute(GeoAttributes.SLOWNESS);

		    masterEventCorrections.put(String.format("%s/%s", observation.getReceiver().getSta(), observation.getPhase()), corr);
		}
	    }
	}

	if (logger.getVerbosity() > 0)
	{
	    logger.writeln(loggerHeader);
	    logger.write(String.format("masterEvent loaded:%n"
		    + "  Evid    = %d%n"
		    + "  Orid    = %d%n"
		    + "  Lat     = %11.5f%n"
		    + "  Lon     = %11.5f%n"
		    + "  Depth   = %9.3f%n"
		    + "  Time    = %15.3f%n"
		    + "  Jdate   = %d%n"
		    + "  NAssocs = %d%n%n",
		    masterEvent.getEvid(),
		    masterEvent.getSourceId(),
		    masterEvent.getLat(),
		    masterEvent.getLon(),
		    masterEvent.getDepth(),
		    masterEvent.getTime(),
		    GMTFormat.getJDate(masterEvent.getTime()),
		    masterEvent.getObservations().size()
		    ));
	    for (String mec : new TreeSet<String>(masterEventCorrections.keySet()))
	    {
		double[] corr = masterEventCorrections.get(mec);
		String[] staPhase = mec.split("/");
		if (corr[0] != Assoc.TIMERES_NA)
		    logger.write(String.format("  %-6s %-6s %2s %8.3f seconds%n", staPhase[0],staPhase[1], "tt", corr[0]));
		if (corr[1] != Assoc.AZRES_NA)
		    logger.write(String.format("  %-6s %-6s %2s %8.3f degrees%n", staPhase[0], staPhase[1], "az", Math.toDegrees(corr[1])));
		if (corr[2] != Assoc.SLORES_NA)
		    logger.write(String.format("  %-6s %-6s %2s %8.3f sec/deg%n", staPhase[0], staPhase[1], "sh", Math.toRadians(corr[2])));
	    }
	    logger.writeln();
	}

	return masterEventCorrections;
    }

    /**
     * if property masterEventUseOnlyStationsWithCorrections is true (default is false)
     * then set every observation to non-defining if it does not have a master event correction.
     * @param sources
     * @throws Exception
     */
    void checkMasterEventObservations(Collection<Source> sources) throws Exception {
	int nChanges = 0;
	if (!masterEventCorrections.isEmpty() &&
		properties.getBoolean("masterEventUseOnlyStationsWithCorrections", false)) 
	    for (Source source : sources)
		for (Observation obs : source.getObservations().values())
		    if (!masterEventCorrections.containsKey(String.format("%s/%s", 
			    obs.getReceiver().getSta(), obs.getPhase()))) {
			if (obs.isTimedef()) {
			    obs.setTimedef(false);
			    ((Observation)obs).setTimedef(false);
			}
			if (obs.isAzdef()) {
			    obs.setAzdef(false);
			    ((Observation)obs).setAzdef(false);
			}
			if (obs.isSlodef()) {
			    obs.setSlodef(false);
			    ((Observation)obs).setSlodef(false);
			}
			++nChanges;
		    }

	if (logger.getVerbosity() > 0 && nChanges > 0) {
	    logger.write(String.format("%d observations were set to non-defining because masterEventUseOnlyStationsWithCorrections is true.%n", 
		    nChanges));
	}


    }

    /**
     * Sets up status log and error log based on properties in property file:
     * <ul>
     * <li><b>io_verbosity</b> int
     * <li><b>io_print_to_screen</b> boolean
     * <li><b>io_log_file</b> String name of file to which status log will be output. Default is no
     * output.
     * <li><b>io_print_errors_to_screen</b> boolean defaults to true
     * <li><b>io_error_file</b> String name of file to which error messages are written. Defaults to
     * "locoo_errors.txt"
     * </ul>
     * 
     * @param properties
     */
    private void setupLoggers() {

	try {
	    errorlog = new ScreenWriterOutput();

	    logger = new ScreenWriterOutput();
	    logger.setVerbosity(properties.getInt("io_verbosity", 1));

	    File logfile = null;
	    File errFile = null;

	    if (properties.getBoolean("io_print_to_screen", true))
		logger.setScreenOutputOn();
	    else
		logger.setScreenOutputOff();
	    if (properties.getProperty("io_log_file") != null) {
		logfile = new File(properties.getProperty("io_log_file"));
		logger.setWriter(new BufferedWriter(new FileWriter(logfile)));
		logger.setWriterOutputOn();
	    }
	    logger.setBufferOutputOn();

	    // turn logger off and back on to ensure current status is stored.
	    logger.turnOff();
	    logger.restore();

	    if (!logger.isOutputOn())
		logger.setVerbosity(0);

	    if (properties.getBoolean("io_print_errors_to_screen", logger.getVerbosity() > 0))
		errorlog.setScreenOutputOn();

	    errFile = new File(properties.getProperty("io_error_file", "locoo_errors.txt"));
	    errorlog.setWriter(new BufferedWriter(new FileWriter(errFile)));
	    errorlog.setWriterOutputOn();
	    // turn logger off and back on to ensure current status is stored.
	    errorlog.turnOff();
	    errorlog.restore();

	    if (logger.getVerbosity() > 0) {
		logger.write(String.format("LocOO3D v. %s started %s%n%n", LocOO.getVersion(),
			GMTFormat.localTime.format(new Date())));

		if (properties.getPropertyFile() != null)
		    logger.writeln("Properties from file " + properties.getPropertyFile().getCanonicalPath());
		else
		    logger.writeln("Properties:");
		logger.writeln(properties.toString());

	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}

    }

}
