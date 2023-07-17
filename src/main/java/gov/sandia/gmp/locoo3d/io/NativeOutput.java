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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.locoo3d.LocOOTaskResult;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

public class NativeOutput {

    protected PropertiesPlusGMP properties;
    protected ScreenWriterOutput logger;
    protected ScreenWriterOutput errorlog;

    protected Map<Long, Source> outputSources;

    protected Map<Long, Map<Long, EnumMap<GeoAttributes, Double>>> predictions;

    public NativeOutput() {
    }

    public NativeOutput(PropertiesPlusGMP properties) throws Exception {
	this.properties = properties;
	predictions = new HashMap<>();
	outputSources = new TreeMap<Long, Source>();
    }

    public NativeOutput(PropertiesPlusGMP properties, NativeInput dataInput) throws Exception {
	this(properties);
	this.logger = dataInput.logger;
	this.errorlog = dataInput.errorlog;
    }

    public static NativeOutput create(PropertiesPlusGMP properties) throws Exception {
	return create(properties, null);
    }

    public static NativeOutput create(PropertiesPlusGMP properties, NativeInput dataInput) throws Exception {
	/**
	 * One of file, database, application
	 */
	String type = properties.getProperty("dataLoaderOutputType", 
		properties.getProperty("dataLoaderType", "application")).toLowerCase();
	
	if (type.equalsIgnoreCase("oracle")) type = "database";
	
	/**
	 * format is one of kb, gmp, native
	 */
	String format = properties.getProperty("dataLoaderOutputFormat", "-").toLowerCase();
	
	if (format.equals("native"))
	    return new NativeOutput(properties, dataInput);
	
	if (format.equals("kb") && type.equals("file"))
	    return new KBFileOutput(properties, dataInput);
	
	if (format.equals("kb") && type.equals("database"))
	    return new KBDBOutput(properties, dataInput);
	
	if (format.equals("kb") && type.equals("application"))
	    return new KBOutput(properties, dataInput);
	
	
	if (format.equals("gmp") && type.equals("file"))
	    return new GMPFileOutput(properties, dataInput);
	
	if (format.equals("gmp") && type.equals("database"))
	    return new GMPDBOutput(properties, dataInput);
	
	if (format.equals("gmp") && type.equals("application"))
	    return new GMPOutput(properties, dataInput);
	
	
	// deal with legacy property definitions.
	
	String dataTypeProperty = properties.getProperty("dataLoaderOutputType", 
		properties.getProperty("dataLoaderType", "application"));

	if (dataTypeProperty.toLowerCase().equals("file")) {

	    if (properties.containsKey("dataLoaderFileOutputOrigins"))    
		return new KBFileOutput(properties, dataInput);

	    if (properties.containsKey("dataLoaderFileOutputGMPSources"))    
		return new GMPFileOutput(properties, dataInput);

	    throw new Exception("dataLoaderOutputType = "+dataTypeProperty+",\n"
		    + "but neither dataLoaderFileOutputOrigins nor dataLoaderFileOutputSources is specified.");

	}
	else if (dataTypeProperty.toLowerCase().equals("oracle") || 
		dataTypeProperty.toLowerCase().equals("database")){

	    if ((properties.containsKey("dbOutputTableTypes") &&
		    properties.getProperty("dbOutputTableTypes").toLowerCase().contains("origin"))
		    || properties.getProperty("dbOutputOriginTable") != null)
		return new KBDBOutput(properties, dataInput);

	    if ((properties.containsKey("dbOutputTableTypes") &&
		    properties.getProperty("dbOutputTableTypes").toLowerCase().contains("source"))
		    || properties.getProperty("dbOutputSourceTable") != null)
		return new GMPDBOutput(properties, dataInput);

	    throw new Exception("dataLoaderOutputType = "+dataTypeProperty+" but dbOutputTableTypes is not specified.");

	}

	if (properties.containsKey("dataLoaderFileOutputOrigins"))    
	    return new KBFileOutput(properties, dataInput);

	if (properties.containsKey("dataLoaderFileOutputGMPSources"))    
	    return new GMPFileOutput(properties, dataInput);

	if (properties.containsKey("outputTableTypes") && properties.getProperty("outputTableTypes").contains("origin"))
	    return new KBOutput(properties, dataInput);

	return new NativeOutput(properties, dataInput);
    }

    public void writeTaskResult(LocOOTaskResult results) throws Exception {
	for (Source source : results.getSources().values()) {
	    Map<Long, EnumMap<GeoAttributes, Double>> obsMap = new HashMap<Long, EnumMap<GeoAttributes,Double>>();
	    predictions.put(source.getSourceId(), obsMap);
	    for (Observation obs : source.getObservations().values()) 
		obsMap.put(obs.getObservationId(), obs.getPredictions());
	}

	// if outputSources is not null, save the new results.
	if (outputSources != null)
	    for (Source source : results.getSources().values())
		outputSources.put(source.getSourceId(), source);
    }

    public void close() throws Exception {}

    void writeData() throws Exception {}

    public Map<Long, Source> getOutputSources() { return outputSources; }

    public Map<Long, Map<Long, EnumMap<GeoAttributes, Double>>> getPredictions() {
	return predictions;
    }

    public PropertiesPlusGMP getProperties() {
	return properties;
    }

    /**
     * if x is equal to Globals.NA_VALUE, return the specified na_value. Otherwise,
     * return toDegrees(x).
     * 
     * @param x
     * @param na_value
     * @return
     */
    protected double degrees(double x, double na_value) {
	if (x == Globals.NA_VALUE)
	    return na_value;
	return Math.toDegrees(x);
    }

    /**
     * if x is equal to Globals.NA_VALUE, return the specified na_value. Otherwise,
     * return toRadians(x).
     * 
     * @param x
     * @param na_value
     * @return
     */
    protected double radians(double x, double na_value) {
	if (x == Globals.NA_VALUE)
	    return na_value;
	return Math.toRadians(x);
    }

    public void writePredictions(File outputFile) throws Exception {
	writePredictions(outputFile, predictions);
    }

    static public void writePredictions(File outputFile, Map<Long, Map<Long, EnumMap<GeoAttributes, Double>>> predictions) throws Exception {
	BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
	output.write("1\n"); // format version number
	output.write(String.format("%d%n", predictions.size()));
	for (Entry<Long, Map<Long, EnumMap<GeoAttributes, Double>>> e1 : predictions.entrySet()) {
	    output.write(String.format("%d %d%n", e1.getKey(), e1.getValue().size()));
	    for (Entry<Long, EnumMap<GeoAttributes, Double>> e2 : e1.getValue().entrySet()) {
		output.write(String.format("%d %d%n", e2.getKey(), e2.getValue().size()));
		for (Entry<GeoAttributes, Double> e3 : e2.getValue().entrySet())
		    output.write(String.format("%s %f%n", e3.getKey().toString(), e3.getValue()));
	    }
	}
	output.close();
    }

    static public Map<Long, Map<Long, EnumMap<GeoAttributes, Double>>> readPredictions(File inputFile) throws Exception {
	Scanner in = new Scanner(inputFile);
	int formatVersion = in.nextInt();
	if (formatVersion != 1) {
	    in.close();
	    throw new Exception("Format version "+formatVersion+" is not recognized.");
	}

	int nsources = in.nextInt();
	Map<Long, Map<Long, EnumMap<GeoAttributes, Double>>> srcMap = new HashMap<>(nsources);
	for (int i=0; i<nsources; ++i) {
	    long sourceid = in.nextLong();
	    int nobs = in.nextInt();
	    Map<Long, EnumMap<GeoAttributes, Double>> obsMap = new HashMap<Long, EnumMap<GeoAttributes,Double>>(nobs);
	    srcMap.put(sourceid, obsMap);
	    for (int j=0; j<nobs; ++j) {
		long obsid = in.nextLong();
		int nvals = in.nextInt();
		EnumMap<GeoAttributes,Double> attMap = new EnumMap<>(GeoAttributes.class);
		obsMap.put(obsid, attMap);
		for (int k=0; k<nvals; ++k) {
		    GeoAttributes a = GeoAttributes.valueOf(in.next());
		    attMap.put(a, in.nextDouble());
		}
	    }
	}
	in.close();
	return srcMap;
    }

    public Buff getBuff() {
	if (outputSources == null)
	    return null;

	Buff buf = new Buff(this.getClass().getSimpleName());
	buf.add("format", 1);
	buf.add("nSources", outputSources.size());
	for (Source o : outputSources.values())
	    buf.add(o.getBuff());
	return buf;
    }

    static public Buff getBuff(Scanner input) throws Exception {
	Buff buf = new Buff(input);

	if (buf.containsKey("nSources")) {
	    Integer n = buf.getInt("nSources");
	    for (int i=0; i<(n == null ? 0 : n); ++i)
		buf.add(Source.getBuff(input));
	}

	if (buf.containsKey("nOrigins")) {
	    Integer n = buf.getInt("nOrigins");
	    for (int i=0; i<(n == null ? 0 : n); ++i)
		buf.add(OriginExtended.getBuff(input));
	}

	return buf;	
    }

}
