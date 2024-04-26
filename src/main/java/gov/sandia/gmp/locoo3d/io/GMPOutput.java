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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.locoo3d.LocOOTaskResult;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.testingbuffer.TestBuffer;
import gov.sandia.gnem.dbtabledefs.gmp.Source;
import gov.sandia.gnem.dbtabledefs.gmp.Srcobsassoc;

public class GMPOutput extends NativeOutput {

    protected GMPInput dataInput;

    /**
     * Map from sourceid to gov.sandia.gnem.dbtabledefs.gmp.Source object.
     */
    protected Map<Long, Source> outputSources;

    protected Map<Long, Source> savedOutputSources;

    boolean srcobsassocsRequested;

    boolean observationsRequested;

    public GMPOutput() {
	super();
    }

    public GMPOutput(PropertiesPlusGMP properties) throws Exception {
	this(properties, null);
    }

    public GMPOutput(PropertiesPlusGMP properties, NativeInput dataInput) throws Exception {
	super(properties, dataInput);

	this.dataInput = (dataInput instanceof GMPInput) ? (GMPInput) dataInput : new GMPInput();

	if (properties.containsKey("gmp_output_keep_sources_in_memory") 
		&& properties.getBoolean("gmp_output_keep_sources_in_memory", false))	
	    savedOutputSources = new TreeMap<>();

	String tableTypes = properties.getProperty("dbOutputTableTypes", " ").toLowerCase();

	srcobsassocsRequested = tableTypes.contains("srcobsassoc") || properties.containsKey("dataLoaderFileOutputSrcobsassocs")
		|| properties.containsKey("dbOutputSrcobsassocTable");

	observationsRequested = tableTypes.contains("observation") || properties.containsKey("dataLoaderFileOutputObservations")
		|| properties.containsKey("dbOutputObservationTable");

	// base class does not need to store the output base-objects sources returned in locooTaskResults.
	super.outputSources = null;

    }

    @Override
    public void writeTaskResult(LocOOTaskResult results) throws Exception {
	super.writeTaskResult(results);
	// the sources in the results object are base-object sources.
	// the must be converted to dbtabledef sources.
	outputSources = new LinkedHashMap<>(results.getSources().size());
	for (gov.sandia.gmp.baseobjects.Source source : results.getSources().values()) {
	    Source src = convertSource(source);
	    outputSources.put(src.getSourceid(), src);
	    if (savedOutputSources != null)
		savedOutputSources.put(src.getSourceid(), src);
	}
	writeData();
    }

    /**
     * 
     * @param src a baseobjects.Source
     * @return a dbtabledefs.Source
     */
    private Source convertSource(gov.sandia.gmp.baseobjects.Source src) {
	Source source = new Source(src.getSourceId(), src.getEvid(), src.getLatDegrees(), src.getLonDegrees(), 
		src.getDepth(), src.getTime(), src.getGTLevel(), src.getNass(), -1L, src.getAuthor());

	if (srcobsassocsRequested)
	    for (gov.sandia.gmp.baseobjects.observation.Observation o : src.getObservations().values()) 
		source.getSrcobsassocs().put(o.getObservationId(), getSrcobsassocRow(o, source.getAuth()));

	return source;
    }

    /**
     * Retrieve data needed to make a dbtabledefs.SrcObsAssoc database row.
     * 
     * @return dbtabledef.srcobsassoc row
     */
    private Srcobsassoc getSrcobsassocRow(Observation obs, String author) {
	Srcobsassoc soAssoc = new Srcobsassoc(obs.getSourceId(), obs.getObservationId(), obs.getPhase().toString(),
		degrees(obs.getDistance(), Globals.NA_VALUE), 
		degrees(obs.getEsaz(), Globals.NA_VALUE),
		degrees(obs.getSeaz(), Globals.NA_VALUE), 
		(obs.isTimedef() ? "d" : "n"), 
		(obs.isAzdef() ? "d" : "n"),
		(obs.isSlodef() ? "d" : "n"), 
		author);

	// if user requested observations, then retrieve a reference to the input dbtabledefs Observation 
	// from the input data.
	if (dataInput.observationsRequested)    
	    soAssoc.setObservation(dataInput.inputSources.get(obs.getSourceId())
		    .getSrcobsassocs().get(obs.getObservationId()).getObservation());
	return soAssoc;
    }
    
    public TestBuffer getTestBuffer() throws Exception {
	if (savedOutputSources == null)
	    throw new Exception("savedOutputSources == null. \nYou must set property gmp_output_keep_sources_in_memory = true.");
	TestBuffer buf = new TestBuffer();
	for (Source o : savedOutputSources.values())
	    buf.add(o.getTestBuffer());
	return buf;
    }

//    static public Buff getBuff(File f) throws FileNotFoundException {
//	Scanner in = new Scanner(f);
//	Buff buff = getBuff(in);
//	in.close();
//	return buff;
//    }
//
//    static public Buff getBuff(Scanner input) {
//	Buff buf = new Buff(input);
//	Integer n = buf.getInt("nSources");
//	// loop over dbtabledef.gmp.Sources
//	for (int i=0; i<(n == null ? 0 : n); ++i)
//	    buf.add(Source.getBuff(input));
//	return buf;	
//    }
//
}
