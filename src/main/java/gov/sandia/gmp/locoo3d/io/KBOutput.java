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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.flinnengdahl.FlinnEngdahlCodes;
import gov.sandia.gmp.baseobjects.hyperellipse.HyperEllipse;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.locoo3d.LocOOTaskResult;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origerr;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

public class KBOutput extends NativeOutput {

    protected KBInput dataInput;

    /**
     * True if output location uncertainty info is requested by te application
     */
    boolean uncertaintyRequested;

    /**
     * True if output azgap info is requested by te application
     */
    boolean azgapRequested;

    /**
     * True if output assoc info is requested by te application
     */
    boolean assocsRequested;

    /**
     * True if output arrival info is requested by te application
     */
    boolean arrivalsRequested;

    /**
     * True if output site info is requested by te application
     */
    boolean sitesRequested;

    protected Map<Long, OriginExtended> outputOrigins;

    public KBOutput() {
	super();
    }

    public KBOutput(PropertiesPlusGMP properties) throws Exception {
	this(properties, null);
    }

    public KBOutput(PropertiesPlusGMP properties, NativeInput dataInput) throws Exception {
	super(properties, dataInput);

	this.dataInput = (dataInput instanceof KBInput) ? (KBInput) dataInput : null;

	if (this.dataInput != null) {
	    this.uncertaintyRequested = this.dataInput.uncertaintyRequested;
	    this.azgapRequested = this.dataInput.azgapRequested;
	    this.assocsRequested = this.dataInput.assocsRequested;
	    this.arrivalsRequested = this.dataInput.arrivalsRequested;
	    this.sitesRequested = this.dataInput.sitesRequested;
	}

	// base class does not need to store the output sources returned in locooTaskResults.
	super.outputSources = null;

    }

    @Override
    public void writeTaskResult(LocOOTaskResult results) throws Exception {
	super.writeTaskResult(results);
	outputOrigins = new TreeMap<Long, OriginExtended>();
	for (Source aource : results.getSources().values()) {
	    OriginExtended origin = getOriginRow(aource);
	    outputOrigins.put(origin.getOrid(), origin);
	}
	if (dataInput != null && dataInput.inputOrigins != null)
	    for (Long sourceId : results.getSources().keySet())
		dataInput.inputOrigins.remove(sourceId);
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
	return new Origerr(
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
		he.getSmajax(),
		he.getSminax(),
		he.getStrike(),
		he.getSdepth(),
		he.getStime(),
		he.getConf(),
		Origerr.COMMID_NA
		);
    }

    private AssocExtended getAssocRow(Observation obs)
    {
	AssocExtended assoc = new AssocExtended(obs.getObservationId(), 
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
		(obs.getModelName().length() > 15 ? obs.getModelName().substring(0,15) : obs.getModelName()),
		Assoc.COMMID_NA);

	assoc.setPredictions(obs.getPredictions());

	if (arrivalsRequested) {
	    OriginExtended origin = dataInput.inputOrigins.get(assoc.getOrid());
	    if (origin != null) {
		AssocExtended inputAssoc = origin.getAssocs().get(assoc.getArid());
		if (inputAssoc != null && inputAssoc.getArrival() != null) 
		    assoc.setArrival(inputAssoc.getArrival());
	    }
	}

	return assoc;
    }

    public Map<Long, OriginExtended> getOutputOrigins() {
	return outputOrigins;
    }

    static public Buff getBuff(File f) throws FileNotFoundException {
	Scanner in = new Scanner(f);
	Buff buff = getBuff(in);
	in.close();
	return buff;
    }

    public Buff getBuff() {
	Buff buf = new Buff(this.getClass().getSimpleName());
	buf.add("format", 1);
	buf.add("nOrigins", outputOrigins.size());
	for (Origin o : outputOrigins.values())
	    buf.add(o.getBuff());
	return buf;
    }

    static public Buff getBuff(Scanner input) {
	Buff buf = new Buff(input);
	Integer n = buf.getInt("nOrigins");
	for (int i=0; i<(n == null ? 0 : n); ++i)
	    buf.add(OriginExtended.getBuff(input));
	return buf;	
    }

}