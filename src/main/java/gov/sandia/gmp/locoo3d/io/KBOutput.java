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

import java.util.Map;
import java.util.TreeMap;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.flinnengdahl.FlinnEngdahlCodes;
import gov.sandia.gmp.baseobjects.hyperellipse.Ellipse;
import gov.sandia.gmp.baseobjects.hyperellipse.HyperEllipse;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.locoo3d.LocOOTaskResult;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.testingbuffer.TestBuffer;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origerr;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.ArrivalExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

public class KBOutput extends NativeOutput {

    protected KBInput dataInput;

    /**
     * True if output location uncertainty info is requested by te application
     */
    private boolean uncertaintyRequested;

    /**
     * True if output azgap info is requested by te application
     */
    private boolean azgapRequested;

    /**
     * True if output assoc info is requested by te application
     */
    private boolean assocsRequested;

    /**
     * True if output arrival info is requested by te application
     */
    private boolean arrivalsRequested;

    protected Map<Long, OriginExtended> outputOrigins;

    protected Map<Long, OriginExtended> savedOutputOrigins;

    public KBOutput() {
	super();
    }

    public KBOutput(PropertiesPlusGMP properties) throws Exception {
	this(properties, new KBInput());
    }

    public KBOutput(PropertiesPlusGMP properties, NativeInput dInput) throws Exception {
	super(properties, dInput);
	
	outputOrigins = new TreeMap<Long, OriginExtended>();

	savedOutputOrigins = null;
	if (properties.containsKey("kb_output_keep_origins_in_memory") 
		&& properties.getBoolean("kb_output_keep_origins_in_memory", false))	
	    savedOutputOrigins = new TreeMap<>();

	// base class does not need to store the output sources returned in locooTaskResults.
	super.outputSources = null;

	// if dataInput is an instance of KBInput, get a reference to it, otherwise construct a new KBInput()
	this.dataInput = (dInput instanceof KBInput) ? (KBInput) dInput : new KBInput();

	String tableTypes = properties.getProperty("dbOutputTableTypes", properties.getProperty("outputTableTypes", " ")).toLowerCase();

	uncertaintyRequested = tableTypes.contains("origerr") || properties.containsKey("dataLoaderFileOutputOrigerrs")
		|| properties.containsKey("dbOutputOrigerrTable");

	azgapRequested = tableTypes.contains("azgap") || properties.containsKey("dataLoaderFileOutputAzgaps")
		|| properties.containsKey("dbOutputAzgapTable");

	assocsRequested = tableTypes.contains("assoc") || properties.containsKey("dataLoaderFileOutputAssocs")
		|| properties.containsKey("dbOutputAssocTable");

	arrivalsRequested = tableTypes.contains("arrival") || properties.containsKey("dataLoaderFileOutputArrivals")
		|| properties.containsKey("dbOutputArrivalTable");
	
//	sitesRequested = tableTypes.contains("site") || properties.containsKey("dataLoaderFileOutputSites")
//		|| properties.containsKey("dbOutputSiteTable");
	
    }

    @Override
    public void writeTaskResult(LocOOTaskResult results) throws Exception {
	super.writeTaskResult(results);
	
	for (Source source : results.getSources().values()) {
	    if (source.isValid()) {
		OriginExtended origin = getOriginRow(source);
		outputOrigins.put(origin.getOrid(), origin);
		if (savedOutputOrigins != null)
		    savedOutputOrigins.put(origin.getOrid(), origin);
	    }
	}
	
	if (dataInput != null && dataInput.inputOrigins != null)
	    for (Long sourceId : results.getSources().keySet())
		dataInput.inputOrigins.remove(sourceId);
	
	// super class has a writeData() method that does nothing. This class has no writeData()
	// method and hence also does nothing.  Derived classes can implement writeData() and write
	// results to files, databases, or elsewhere.
	writeData();
    }

    private OriginExtended getOriginRow(Source source) throws Exception
    {
	int grn = FlinnEngdahlCodes.getGeoRegionIndex(source.getLatDegrees(), source.getLonDegrees());
	int srn = FlinnEngdahlCodes.getSeismicRegionIndex(grn);
	String algorithm = source.getAlgorithm();
	String author = source.getAuthor();

	OriginExtended origin = new OriginExtended(
		Math.round(source.getLatDegrees()*1e6)/1e6,
		Math.round(source.getLonDegrees()*1e6)/1e6,
		Math.round(source.getDepth()*1e4)/1e4,
		Math.round(source.getTime()*1e5)/1e5,
		source.getSourceId(),
		source.getEvid(),
		GMTFormat.getJDate(source.getTime()),
		source.getNass(),
		source.getNdef(),
		Origin.NDP_NA,
		grn,
		srn,
		Origin.ETYPE_NA,
		Origin.DEPDP_NA,
		source.getDtype(),
		Origin.MB_NA,
		Origin.MBID_NA,
		Origin.MS_NA,
		Origin.MSID_NA,
		Origin.ML_NA,
		Origin.MLID_NA,
		(algorithm.length() > 15 ? algorithm.substring(0, 15) : algorithm),
		(author.length() > 15 ? author.substring(0, 15) : author),
		Origin.COMMID_NA
		);

	if (uncertaintyRequested)
	    origin.setOrigerr(getOrigerrRow(source));

	if (azgapRequested)
	    origin.setAzgap(source.getAzgap());

	if (assocsRequested)
	    for (Observation obs : source.getObservations().values())
		origin.addAssoc(getAssocRow(obs));

	return origin;
    }

    private Origerr getOrigerrRow(Source source) throws Exception
    {
	HyperEllipse he = source.getHyperEllipse();
	double ellipse_majax = Origerr.SMAJAX_NA;
	double ellipse_minax = Origerr.SMINAX_NA;
	double ellipse_trend = Origerr.STRIKE_NA;
	try {
		Ellipse ellipse = he.getEllipse();
		ellipse_majax = ellipse.getMajaxLength();
		ellipse_minax = ellipse.getMinaxLength();
		ellipse_trend = ellipse.getMajaxTrend();

	} catch (Exception e) {
	}
	return he == null ? new Origerr() : new Origerr(
		source.getSourceId(),
		he.getSxx(),
		he.getSyy(),
		he.getSzz(),
		he.getStt(),
		he.getSxy(),
		he.getSxz(),
		he.getSyz(),
		he.getStx(),
		he.getSty(),
		he.getStz(),
		source.getSdobs(),
		ellipse_majax,
		ellipse_minax,
		ellipse_trend,
		he.getSdepth(),
		he.getStime(),
		he.getConfidence(),
		Origerr.COMMID_NA
		);
    }

    private AssocExtended getAssocRow(Observation obs)
    {
	AssocExtended assoc = new AssocLocOO(obs.getObservationId(), 
		obs.getSourceId(), obs.getReceiver().getSta(), obs.getPhase().toString(), -1.,
		degrees(obs.getDistance(), -1.), degrees(obs.getSeaz(Globals.NA_VALUE), -1.),
		degrees(obs.getEsaz(Globals.NA_VALUE), -1.),
		(obs.getTimeres() == Globals.NA_VALUE ? Assoc.TIMERES_NA : obs.getTimeres()),
		(obs.isTimedef() ? "d" : "n"), 
		degrees(obs.getAzres(), Assoc.AZRES_NA), 
		(obs.isAzdef() ? "d" : "n"),
		radians(obs.getSlores(), Assoc.SLORES_NA), 
		(obs.isSlodef() ? "d" : "n"), 
		Assoc.EMARES_NA, 
		(obs.getTtWeight() == Globals.NA_VALUE ? Assoc.WGT_NA : obs.getTtWeight()), 
		Globals.truncate(obs.getModelName(), 15),
		Assoc.COMMID_NA);

	((AssocLocOO)assoc).setPredictions(obs.getPredictions());

	if (arrivalsRequested) {
	    ArrivalExtended arrival = null;
	    OriginExtended origin = dataInput.inputOrigins.get(obs.getSourceId());
	    if (origin != null) {
		AssocExtended inputAssoc = origin.getAssocs().get(obs.getObservationId());
		if (inputAssoc != null) 
		    arrival = inputAssoc.getArrival();
	    }
	    if (arrival == null)
		arrival = obs.getArrivalExtended();
		assoc.setArrival(arrival);
	}

	return assoc;
    }

    public Map<Long, OriginExtended> getOutputOrigins() {
    	return savedOutputOrigins;
    }

    public TestBuffer getTestBuffer() {
    	TestBuffer buffer = new TestBuffer();
    	for (OriginExtended o : savedOutputOrigins.values())
    		buffer.add(o.getTestBuffer());
    	return buffer;
    }

}
