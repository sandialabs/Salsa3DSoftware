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
package gov.sandia.gmp.baseobjects;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.hyperellipse.HyperEllipse;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_custom.Azgap;

/**
 * <p>
 * Title: Origin
 * </p>
 * 
 * <p>
 * Represents a seismic event (Origin) and associated uncertainty (Origerr)
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
public class Source extends Location implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -773420255233247552L;

    private long sourceId;

    private long evid;

    private Map<Long, Observation> observations;

    /**
     * Destination for log information about this source
     */
    private ScreenWriterOutput log;
    
    /**
     * Destination for error information generated during the location
     * calculation.
     */
    private ScreenWriterOutput errorLog;

    /**
     * Number of time-defining observations
     */
    private long ndef;

    /**
     * Defaults to the system username.
     * Author can be specified in the properties file with property outputAuthor 
     * or dbOutputAuthor
     */
    private String author = Origin.AUTH_NA;

    /**
     * Defaults to LocOO3D.<version number> but can be specified in the 
     * properties file with property outputAlgorithm
     */
    private String algorithm = Origin.ALGORITHM_NA;

    /**
     * Azimuthal gap information
     */
    private Azgap azgap;

    /**
     * Complete location uncertainty information, including the 4D hyper-ellipse,
     * 3D ellipsoid, 2D ellipse and 1D time and depth uncertainties.
     * 
     * If hyperEllipse is null, it indicates that the location calculation failed.
     */
    private HyperEllipse hyperEllipse;

    /**
     * The standard deviation of the weighted residuals, including all defining
     * tt, az, and slowness observations
     */
    private double sdobs = Double.NaN;

    /**
     * Time required to perform the location calculation, in seconds.
     * It does not include any time spent doing IO, only time actually 
     * computing this location.
     */
    private double calcTime = Double.NaN;

    /**
     * Amount of time, in seconds, spent computing predictions.
     */
    private double predictionTime = Double.NaN;

    /** 
     * Number of iterations performed by the locator
     */
    private int nIterations = -1;

    /**
     * Number of times that the sum-squared-weighted-residuals were calculated
     */
    private int nFunc = -1;

    /**
     * whether lat, lon, depth and time are fixed during location
     */
    private boolean[] fixed;


    /**
     * Whether or not any derivatives wrt lat, lon, depth, time were
     * required during the calculation of predictions.
     */
    private boolean needDerivatives;

    /**
     * Whether or not model uncertainty is to be included in total
     * uncertainty when weighting residuals and derivatives.
     */
    private boolean useTTModelUncertainty;
    private boolean useAzModelUncertainty;
    private boolean useShModelUncertainty;

    /**
     * whether or not path corrections are to be included in predictions.
     */
    private boolean useTTPathCorrections;
    private boolean useAzPathCorrections;
    private boolean useShPathCorrections;

    private double gtLevel = -1.0;
    private boolean gtTime = false;

    /**
     * @param sourceid (orid)
     * @param evid
     * @param position
     * @param time     epoch time (seconds since 1970).
     * @param gtLevel
     * 
     */
    public Source(long sourceid, long evid, GeoVector position, double time, double gtLevel, boolean gttime)
	    throws GMPException {
	super(position, time);

	this.sourceId = sourceid;
	this.evid = evid;
	this.gtLevel = gtLevel;
	gtTime = gttime;

	observations = new LinkedHashMap<Long, Observation>();
    }

    /**
     * @param sourceid (orid)
     * @param evid
     * @param position
     * @param time     epoch time (seconds since 1970).
     * @param gtLevel
     * 
     */
    public Source(long sourceid, long evid, GeoVector position, double time, double gtLevel) throws GMPException {
	this(sourceid, evid, position, time, gtLevel, false);
    }

    /**
     * Copy constructor
     * 
     * @param other
     * @throws GMPException
     */
    public Source(Source other) throws GMPException {
	super(other);
	sourceId = other.sourceId;
	evid = other.evid;
	gtLevel = other.gtLevel;
	observations = new LinkedHashMap<Long, Observation>(other.observations);
	ndef = other.ndef;
    }

    /**
     * Construct a Source at supplied position, with originTime=0, sourceID=-1,
     * evid=-1, and gtLevel = Globals.NA_VALUE
     * 
     * @param geoVector
     * @throws GMPException
     */
    public Source(GeoVector geoVector) throws GMPException {
	super(geoVector, 0.);
	sourceId = -1;
	evid = -1;
	gtLevel = Globals.NA_VALUE;
	observations = new LinkedHashMap<Long, Observation>();
    }

    /**
     * Construct a Source with deep copy of supplied position and originTime, but
     * with sourceID=-1, evid=-1, and gtLevel = Globals.NA_VALUE
     * 
     * @param geoVector
     * @throws GMPException
     */
    public Source(GeoVector geoVector, double originTime) throws GMPException {
	super(geoVector, originTime);
	sourceId = -1;
	evid = -1;
	gtLevel = Globals.NA_VALUE;
	observations = new LinkedHashMap<Long, Observation>();
    }

    public Source(double lat, double lon, double depth, boolean inDegrees) throws GMPException {
	this(new GeoVector(lat, lon, depth, inDegrees));
    }

    /**
     * Construct a Source object from an OriginRow object. The only fields from the
     * origin row that get used are orid, evid, lat, lon, depth, time. Everything
     * else is lost. Source.gtLevel is set to Globals.NA_VALUE.
     * 
     * @param origin an OriginRow object
     * @throws GMPException
     */
    public Source(Origin origin) throws GMPException {
	this(origin.getOrid(), origin.getEvid(),
		new GeoVector(origin.getLat(), origin.getLon(), origin.getDepth(), true), origin.getTime(),
		Globals.NA_VALUE);
    }

    /**
     * Construct a Source object from an OriginRow object. The only fields from the
     * origin row that get used are orid, evid, lat, lon, depth, time. Everything
     * else is lost. Source.gtLevel is set to Globals.NA_VALUE.
     * 
     * @param origin an OriginRow object
     * @throws Exception
     */
    public Source(OriginExtended origin) throws Exception {

	this(origin.getOrid(), origin.getEvid(),
		new GeoVector(origin.getLat(), origin.getLon(), origin.getDepth(), true), origin.getTime(),
		Globals.NA_VALUE);

	observations = new LinkedHashMap<Long, Observation>((int)origin.getNass());
	for (AssocExtended assoc : origin.getAssocs().values())
	    observations.put(assoc.getArid(), new Observation(this, assoc));
	ndef = origin.getNdef();
    }

    public Source(long evid, double[] unitVector, double radius, double time) throws GMPException {
	super(new GeoVector(unitVector, radius), time);
	this.evid = evid;
    }

    public Source(gov.sandia.gnem.dbtabledefs.gmp.Source s) throws Exception {
	super(new GeoVector(s.getLat(), s.getLon(), s.getDepth(), true), s.getOrigintime());
	sourceId = s.getSourceid();
	evid = s.getEventid();
	//nass = (int) s.getNumassoc();
	gtLevel = s.getGtlevel();
	gtTime = false;
	observations = new LinkedHashMap<Long, Observation>();
    }

    /**
     * Returns true if a valid location was successfully computed for this Source.
     * False if the calculation failed for some reason.  See log file for information 
     * about why the location failed.
     * @return
     */
    public boolean isValid() {
	return hyperEllipse != null;
    }

    public Source setSourceId(long sourceId) {
	this.sourceId = sourceId;
	return this;
    }

    public long getSourceId() {
	return sourceId;
    }

    public Source setEvid(long evid) {
	this.evid = evid;
	return this;
    }

    public long getEvid() {
	return evid;
    }

    public double getOriginTime() {
	return super.time;
    }

    public double getGTLevel() {
	return gtLevel;
    }

    public Source setGTLevel(double gtLevel) {
	this.gtLevel = gtLevel;
	return this;
    }

    public boolean isGTTime() {
	return gtTime;
    }

    public Source setGTTime(boolean gttime) {
	gtTime = gttime;
	return this;
    }

    public int getNass() {
	return observations.size();
    }

    /**
     * Retrieve data needed to make a dbtabledefs.gmp.Source database row.
     * 
     * @return Object[]
     */
    public gov.sandia.gnem.dbtabledefs.gmp.Source getSourceRow()
    {
	return new gov.sandia.gnem.dbtabledefs.gmp.Source(sourceId, evid, getLatDegrees(), getLonDegrees(), getDepth(),
		time, gtLevel, getNass(), -1L, // polygonid ???
		GMPGlobals.getAuth());
    }

    /**
     * Retrieve data needed to make a SOURCE database row.
     * 
     * @return Object[]
     */
    public Origin getOriginRow() {
	return new Origin(getLatDegrees(), getLonDegrees(), getDepth(), time, sourceId, evid, GMTFormat.getJDate(time),
		-1, -1, -1, -1, -1, "-", -999., "-", -999., -1, -999., -1, -999., -1, "-", GMPGlobals.getAuth(), -1);
    }

    public Map<Long, Observation> getObservations() {
	return observations;
    }

    public void setObservations(Collection<Observation> observations) {
	this.observations = new LinkedHashMap<Long, Observation>();
	ndef = 0;
	for (Observation o : observations) {
	    this.observations.put(o.getObservationId(), o);
	    if (o.isTimedef())
		++ndef;
	}
    }

    public long getNdef() { return ndef; }

    public String getAuthor() {return author;}
    public void setAuthor(String author) {this.author = author;}

    public String getAlgorithm() {return algorithm;}
    public void setAlgorithm(String algorithm) {this.algorithm = algorithm;}

    public String getDtype() { return fixed[2] ? "g" : "f"; }

    public Azgap getAzgap() {return azgap;}
    public void setAzgap(Azgap azgap) {this.azgap = azgap;}

    public void setHyperEllipse(HyperEllipse hyperEllipse) { this.hyperEllipse = hyperEllipse; }
    public HyperEllipse getHyperEllipse() { return hyperEllipse; }

    public void setSdobs(double sdobs) { this.sdobs=sdobs;}
    public double getSdobs() { return sdobs; }

    public void setCalculationTime(double calcTime) {this.calcTime=calcTime;}
    public double getCalculationTime() { return calcTime; }

    public void setNIterations(int n) {nIterations=n;}
    public int getNIterations() { return nIterations;}

    public void setNFunc(int nFunc) {this.nFunc = nFunc;}
    public int getNFunc() { return nFunc;}

    public void setPredictionTime(double t) {this.predictionTime=t;}
    public double getPredictionTime() {return predictionTime;}

    public boolean[] getFixed() { return fixed; }
    public void setFixed(boolean[] fixed) { this.fixed = fixed; }

    public boolean isFree(int i) { return !fixed[i]; }

    public boolean needDerivatives() { return needDerivatives; }
    public void needDerivatives(boolean b) { this.needDerivatives = b; }

    public boolean getUseTTModelUncertainty() {
	return useTTModelUncertainty;
    }

    public void useTTModelUncertainty(boolean useTTModelUncertainty) {
	this.useTTModelUncertainty = useTTModelUncertainty;
    }

    public boolean getUseAzModelUncertainty() {
	return useAzModelUncertainty;
    }

    public void useAzModelUncertainty(boolean useAzModelUncertainty) {
	this.useAzModelUncertainty = useAzModelUncertainty;
    }

    public boolean getUseShModelUncertainty() {
	return useShModelUncertainty;
    }

    public void useShModelUncertainty(boolean useShModelUncertainty) {
	this.useShModelUncertainty = useShModelUncertainty;
    }

    public boolean getUseTTPathCorrections() {
	return useTTPathCorrections;
    }

    public void useTTPathCorrections(boolean useTTPathCorrections) {
	this.useTTPathCorrections = useTTPathCorrections;
    }

    public boolean getUseAzPathCorrections() {
	return useAzPathCorrections;
    }

    public void useAzPathCorrections(boolean useAzPathCorrections) {
	this.useAzPathCorrections = useAzPathCorrections;
    }

    public boolean getUseShPathCorrections() {
	return useShPathCorrections;
    }

    public void useShPathCorrections(boolean useShPathCorrections) {
	this.useShPathCorrections = useShPathCorrections;
    }

    /**
     * Sort the supplied List of Sources by ndef
     *
     * @param arrivals
     */
    static public void sortByNdefDescending(List<? extends Source> sources) {
	Collections.sort(sources, sortByNdefDescending);
    }
    static public Comparator<Source> sortByNdefDescending = new Comparator<Source>() {
	@Override
	public int compare(Source o1, Source o2) { return (int) Math.signum(o2.getNdef() - o1.getNdef());}
    };

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + (int) (sourceId ^ (sourceId >>> 32));
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Source other = (Source) obj;
	if (sourceId != other.sourceId)
	    return false;
	return true;
    }

    public Buff getBuff() {

	Buff buffer = new Buff(getClass().getSimpleName());
	buffer.add("format", 1);
	buffer.add("sourceId", sourceId);
	buffer.add("evid", evid);
	buffer.add("lat", getLatDegrees(),5);
	buffer.add("lon", getLonDegrees(),5);
	buffer.add("depth", getDepth());
	buffer.add("time", getTime());
	buffer.add("author", author);
	buffer.add("algorithm", algorithm);
	buffer.add("gtLevel", gtLevel);
	buffer.add("gtTime", gtTime);
	buffer.add("sdobs", sdobs);
	//buffer.add("calcTime", calcTime);
	//buffer.add("predictionTime", predictionTime);
	buffer.add("nIterations", nIterations);
	buffer.add("nFunc", nFunc);
	buffer.add("nObservations", observations.size());
	buffer.add("nHyperEllipses", hyperEllipse == null ? 0 : 1);
	buffer.add("nAzgaps", azgap == null ? 0 : 1);


	for (Observation o : observations.values())
	    buffer.add(o.getBuff());

	return buffer;
    }

    static public Buff getBuff(Scanner input) {
	Buff buf = new Buff(input);

	for (int i=0; i<buf.getInt("nObservations"); ++i)
	    buf.add(Observation.getBuff(input));
	return buf;

    }

    @Override
    public String toString() {
	return getBuff().toString();
    }

    public void addObservation(Observation obs) {
	observations.put(obs.getObservationId(), obs);
    }

    public Observation getObservation(Long obsid) { return observations.get(obsid); }

    public ScreenWriterOutput getLog() {
	return log;
    }

    public void setLog(ScreenWriterOutput log) {
	this.log = log;
    }

    public ScreenWriterOutput getErrorLog() {
	return errorLog;
    }

    public void setErrorLog(ScreenWriterOutput errorLog) {
	this.errorLog = errorLog;
    }

}
