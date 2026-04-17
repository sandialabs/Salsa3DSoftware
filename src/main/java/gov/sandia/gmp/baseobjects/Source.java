/**
 * Copyright 2009 Sandia Corporation. Under the terms of Contract DE-AC04-94AL85000 with Sandia
 * Corporation, the U.S. Government retains certain rights in this software.
 * 
 * BSD Open Source License.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * 
 * - Neither the name of Sandia National Laboratories nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.sandia.gmp.baseobjects;

import static gov.sandia.gmp.util.globals.Globals.sqr;
import static java.lang.Math.sqrt;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import gov.sandia.gmp.baseobjects.flinnengdahl.FlinnEngdahlCodes;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.hyperellipse.Ellipse;
import gov.sandia.gmp.baseobjects.hyperellipse.HyperEllipse;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.baseobjects.observation.ObservationComponent;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.vector.GeoMath;
import gov.sandia.gmp.util.testingbuffer.TestBuffer;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AzgapExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.NetworkExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.SiteExtended;

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
public class Source extends Location implements Serializable, Cloneable {
  /**
   * 
   */
  private static final long serialVersionUID = -773420255233247552L;

  private long sourceId;

  private long evid;

  private Map<Long, Observation> observations;

  /**
   * All the observationComponents of type TT, AZ, SH, including defining, non-defining, invalids,
   * etc. These are references to the same observationComponents that are owned by Observation
   * objects.
   */
  private ArrayList<ObservationComponent> obsComponents;

  /**
   * Accumulation of all warnings and errors generated during relocation
   */
  private String errorMessage = "";

  /**
   * True if Source was relocated successfully.
   */
  private boolean valid;

  /**
   * Defaults to the system username. Author can be specified in the properties file with property
   * outputAuthor or dbOutputAuthor
   */
  private String author = Origin.AUTH_NA;

  /**
   * Defaults to LocOO3D.<version number> but can be specified in the properties file with property
   * outputAlgorithm
   */
  private String algorithm = Origin.ALGORITHM_NA;

  /**
   * Azimuthal gap information
   */
  private AzgapExtended azgap;

  /**
   * Complete location uncertainty information, including the 4D hyper-ellipse, 3D ellipsoid, 2D
   * ellipse and 1D time and depth uncertainties.
   * 
   * If hyperEllipse is null, it indicates that the location calculation failed.
   */
  private HyperEllipse hyperEllipse;

  /**
   * Time required to perform the location calculation, in seconds. It does not include any time
   * spent doing IO, only time actually computing this location.
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
   * Correlations specifies the correlation coefficient between two observations. Each String is
   * composed of station name/phase/attribute where attribute is one of [ TT, AZ, SH ]. An example
   * of an entry in this map would be: <br>
   * ASAR/Pg/TT -> WRA/Pg/TT -> 0.5 <br>
   * Coefficient values must be in the range [ -1 to 1 ]
   * 
   * @return correlation map
   */
  private Map<String, Map<String, Double>> correlationCoefficients;

  /**
   * Whether or not any derivatives wrt lat, lon, depth, time were required during the calculation
   * of predictions.
   */
  private boolean needDerivatives;

  /**
   * Whether or not model uncertainty is to be included in total uncertainty when weighting
   * residuals and derivatives.
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

  private double sumSQRWeightedResiduals = Globals.NA_VALUE;

  private boolean hasLocalMinima;

  private List<Location> locationTrack;

  private static Map<Long, Observation> newObsMap(Map<Long, Observation> otherObs) {
    Map<Long, Observation> base = new LinkedHashMap<>();
    if (otherObs != null)
      base.putAll(otherObs);
    return Collections.synchronizedMap(base);
  }

  private static Map<Long, Observation> newObsMap() {
    return newObsMap(null);
  }

  /**
   * @param sourceid (orid)
   * @param evid
   * @param position
   * @param time epoch time (seconds since 1970).
   * @param gtLevel
   * 
   */
  public Source(long sourceid, long evid, GeoVector position, double time, double gtLevel,
      boolean gttime) throws GMPException {
    super(position, time);

    this.sourceId = sourceid;
    this.evid = evid;
    this.gtLevel = gtLevel;
    gtTime = gttime;

    observations = newObsMap();
  }

  /**
   * @param sourceid (orid)
   * @param evid
   * @param position
   * @param time epoch time (seconds since 1970).
   * @param gtLevel
   * 
   */
  public Source(long sourceid, long evid, GeoVector position, double time, double gtLevel)
      throws GMPException {
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
    observations = newObsMap(other.observations);
  }

  /**
   * Construct a Source at supplied position, with originTime=0, sourceID=-1, evid=-1, and gtLevel =
   * Globals.NA_VALUE
   * 
   * @param geoVector
   * @throws GMPException
   */
  public Source(GeoVector geoVector) throws GMPException {
    super(geoVector, 0.);
    sourceId = -1;
    evid = -1;
    gtLevel = Globals.NA_VALUE;
    observations = newObsMap();
  }

  /**
   * Construct a Source with deep copy of supplied position and originTime, but with sourceID=-1,
   * evid=-1, and gtLevel = Globals.NA_VALUE
   * 
   * @param geoVector
   * @throws GMPException
   */
  public Source(GeoVector geoVector, double originTime) throws GMPException {
    super(geoVector, originTime);
    sourceId = -1;
    evid = -1;
    gtLevel = Globals.NA_VALUE;
    observations = newObsMap();
  }

  public Source(double lat, double lon, double depth, boolean inDegrees) throws GMPException {
    this(new GeoVector(lat, lon, depth, inDegrees));
  }

  /**
   * Construct new Source.
   * 
   * @param lat
   * @param lon
   * @param depth in km
   * @param time epochTime in seconds
   * @param inDegrees specify true if lat, lon are in degrees, false if in radians
   * @throws GMPException
   */
  public Source(double lat, double lon, double depth, double time, boolean inDegrees)
      throws GMPException {
    this(new GeoVector(lat, lon, depth, inDegrees), time);
  }

  /**
   * Construct a Source object from an OriginRow object. The only fields from the origin row that
   * get used are orid, evid, lat, lon, depth, time. Everything else is lost. Source.gtLevel is set
   * to Globals.NA_VALUE.
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
   * Construct a Source object from an OriginRow object. The only fields from the origin row that
   * get used are orid, evid, lat, lon, depth, time. Everything else is lost. Source.gtLevel is set
   * to Globals.NA_VALUE.
   * 
   * @param origin an OriginRow object
   * @throws Exception
   */
  public Source(OriginExtended origin) throws Exception {

    this(origin.getOrid(), origin.getEvid(),
        new GeoVector(origin.getLat(), origin.getLon(), origin.getDepth(), true), origin.getTime(),
        Globals.NA_VALUE);

    observations = newObsMap();
    for (AssocExtended assoc : origin.getAssocs().values())
      if (assoc.getSite() != null)
        observations.put(assoc.getArid(), new Observation(this, assoc));
  }

  public Source(long evid, double[] unitVector, double radius, double time) throws GMPException {
    super(new GeoVector(unitVector, radius), time);
    this.evid = evid;
  }

  public Source(gov.sandia.gnem.dbtabledefs.gmp.Source s) throws Exception {
    super(new GeoVector(s.getLat(), s.getLon(), s.getDepth(), true), s.getOrigintime());
    sourceId = s.getSourceid();
    evid = s.getEventid();
    // nass = (int) s.getNumassoc();
    gtLevel = s.getGtlevel();
    gtTime = false;
    observations = newObsMap();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    Source s = (Source) super.clone();
    if (fixed != null)
      s.fixed = fixed.clone();
    s.observations = newObsMap();
    for (Entry<Long, Observation> entry : observations.entrySet()) {
      Observation obs = (Observation) entry.getValue().clone();
      try {
        obs.setSource(s);
      } catch (Exception e) {
        e.printStackTrace();
      }
      s.observations.put(entry.getKey(), obs);
    }
    s.locationTrack = null;
    s.azgap = null;
    return s;
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

  /**
   * Retrieve data needed to make a dbtabledefs.gmp.Source database row.
   * 
   * @return Object[]
   */
  public gov.sandia.gnem.dbtabledefs.gmp.Source getSourceRow() {
    return new gov.sandia.gnem.dbtabledefs.gmp.Source(sourceId, evid, getLatDegrees(),
        getLonDegrees(), getDepth(), time, gtLevel, getNass(), -1L, // polygonid ???
        GMPGlobals.getAuth());
  }

  /**
   * Retrieve data needed to make a SOURCE database row.
   * 
   * @return Object[]
   */
  public Origin getOriginRow() {
    return new Origin(getLatDegrees(), getLonDegrees(), getDepth(), time, sourceId, evid,
        GMTFormat.getJDate(time), -1, -1, -1, -1, -1, "-", -999., "-", -999., -1, -999., -1, -999.,
        -1, "-", GMPGlobals.getAuth(), -1);
  }

  public OriginExtended getOriginExtended() {
    OriginExtended origin = new OriginExtended(getLatDegrees(), getLonDegrees(), getDepth(), time,
        sourceId, evid, GMTFormat.getJDate(time), -1, -1, -1, -1, -1, "-", -999., "-", -999., -1,
        -999., -1, -999., -1, "-", GMPGlobals.getAuth(), -1);
    for (Observation obs : getObservations().values())
      origin.addAssoc(obs.getAssocExtended());
    return origin;
  }

  public Map<Long, Observation> getObservations() {
    return observations;
  }

  public void setObservations(Collection<Observation> observations) {
    this.observations.clear();
    for (Observation o : observations) {
      this.observations.put(o.getObservationId(), o);
    }
  }

  public int getNass() {
    return observations.size();
  }

  public long getNdef() {
    int ndef = 0;
    for (Observation o : observations.values())
      if (o.isTimedef())
        ++ndef;
    return ndef;
  }

  /**
   * Retrieve number of defining azimuth observations
   * 
   * @return number of defining azimuth observations
   */
  public long getNaz() {
    int n = 0;
    for (Observation o : observations.values())
      if (o.isAzdef())
        ++n;
    return n;
  }

  /**
   * Retrieve number of defining slowness observations
   * 
   * @return number of defining slowness observations
   */
  public long getNslo() {
    int n = 0;
    for (Observation o : observations.values())
      if (o.isSlodef())
        ++n;
    return n;
  }

  public int getNobs() {
    int nobs = 0;
    for (Observation o : observations.values())
      for (ObservationComponent oc : o.getObservationComponents().values())
        if (oc.isDefining())
          ++nobs;
    return nobs;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public String getDtype() {
    return fixed[2] ? "g" : "f";
  }

  /**
   * Compute the azgap of this Source using only sites with defining observations. Lazy evaluation.
   * 
   * @return
   * @throws Exception
   */
  public AzgapExtended getAzgap() throws Exception {
    if (azgap == null)
      azgap = new AzgapExtended(getSourceId(), getUnitVector(), getReceivers(true).values());
    return azgap;
  }

  public void setHyperEllipse(HyperEllipse hyperEllipse) {
    this.hyperEllipse = hyperEllipse;
  }

  public HyperEllipse getHyperEllipse() {
    return hyperEllipse;
  }

  public Ellipse getEllipse() throws Exception {
    return hyperEllipse.getEllipse();
  }

  public double getSdobs() {
    if (!valid)
      return -1;
    int n = 0;
    double sum = 0;
    for (Observation obs : observations.values())
      if (obs.isTimedef()) {
        sum += obs.getTimeres();
        ++n;
      }
    if (n == 0)
      return -1.;
    double mean = sum / n;
    sum = 0.;
    for (Observation obs : observations.values())
      if (obs.isTimedef())
        sum += sqr(obs.getTimeres() - mean);
    return sqrt(sum / n);
  }

  public void setCalculationTime(double calcTime) {
    this.calcTime = calcTime;
  }

  public double getCalculationTime() {
    return calcTime;
  }

  public void setNIterations(int n) {
    nIterations = n;
  }

  public int getNIterations() {
    return nIterations;
  }

  public void setNFunc(int nFunc) {
    this.nFunc = nFunc;
  }

  public int getNFunc() {
    return nFunc;
  }

  public void setPredictionTime(double t) {
    this.predictionTime = t;
  }

  public double getPredictionTime() {
    return predictionTime;
  }

  public boolean[] getFixed() {
    return fixed;
  }

  public Source setFixed(boolean[] fixed) {
    this.fixed = fixed;
    return this;
  }

  public boolean isFixed(int i) {
    return fixed[i];
  }

  public boolean isFree(int i) {
    return !fixed[i];
  }

  public boolean depthFixed() {
    return fixed[2];
  }

  public boolean depthFree() {
    return !fixed[2];
  }

  public int nFree() {
    int n = 0;
    for (int i = 0; i < 4; ++i)
      if (!fixed[i])
        ++n;
    return n;
  }

  public boolean needDerivatives() {
    return needDerivatives;
  }

  public void needDerivatives(boolean b) {
    this.needDerivatives = b;
  }

  public boolean useTTModelUncertainty() {
    return useTTModelUncertainty;
  }

  public void useTTModelUncertainty(boolean useTTModelUncertainty) {
    this.useTTModelUncertainty = useTTModelUncertainty;
  }

  public boolean useAzModelUncertainty() {
    return useAzModelUncertainty;
  }

  public void useAzModelUncertainty(boolean useAzModelUncertainty) {
    this.useAzModelUncertainty = useAzModelUncertainty;
  }

  public boolean useShModelUncertainty() {
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
    public int compare(Source o1, Source o2) {
      return (int) Math.signum(o2.getNdef() - o1.getNdef());
    }
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

  public TestBuffer getTestBuffer() {

    TestBuffer buffer = new TestBuffer(this.getClass().getSimpleName());
    buffer.add("source.sourceId", sourceId);
    buffer.add("source.evid", evid);
    buffer.add("source.lat", getLatDegrees());
    buffer.add("source.lon", getLonDegrees());
    buffer.add("source.depth", getDepth());
    buffer.add("source.time", getTime());
    buffer.add("source.author", author);
    buffer.add("source.algorithm", algorithm);
    buffer.add("source.gtLevel", gtLevel);
    buffer.add("source.gtTime", gtTime);
    buffer.add("source.sdobs", getSdobs());
    // buffer.add("source.nIterations", nIterations);
    // buffer.add("source.nFunc", nFunc);
    buffer.add("source.valid", valid);
    buffer.add("source.errorMessage", errorMessage);
    buffer.add("source.hasHyperEllipse", hyperEllipse != null);
    buffer.add("source.hasAzgap", azgap != null);
    buffer.add("source.nObservations", observations.size());
    buffer.add();

    if (hyperEllipse != null)
      buffer.add(getHyperEllipse().getTestBuffer());

    for (Observation o : observations.values())
      buffer.add(o.getTestBuffer());

    return buffer;
  }

  @Override
  public String toString() {
    return String.format("SourceId= %d, lat,lon,depth= %s, %1.3f, err=%s", sourceId,
        GeoMath.getLatLonString(v), getDepth(), errorMessage);
  }

  public void addObservation(Observation obs) {
    observations.put(obs.getObservationId(), obs);
  }

  public Observation getObservation(Long obsid) {
    return observations.get(obsid);
  }

  /**
   * Correlations specifies the correlation coefficient between two observations. Each String is
   * composed of station name/phase/attribute where attribute is one of [ TT, AZ, SH ]. An example
   * of an entry in this map would be: <br>
   * ASAR/Pg/TT -> WRA/Pg/TT -> 0.5 <br>
   * Coefficient values must be in the range [ -1 to 1 ]
   * 
   * @return correlation map
   */
  public Map<String, Map<String, Double>> getCorrelationCoefficients() {
    return correlationCoefficients;
  }

  /**
   * Correlations specifies the correlation coefficient between two observations. Each String is
   * composed of station name/phase/attribute where attribute is one of [ TT, AZ, SH ]. An example
   * of an entry in this map would be: <br>
   * ASAR/Pg/TT -> WRA/Pg/TT -> 0.5 <br>
   * Coefficient values must be in the range [ -1 to 1 ]
   * 
   * @return correlation map
   */
  public void setCorrelationCoefficients(Map<String, Map<String, Double>> correlationCoefficients) {
    this.correlationCoefficients = correlationCoefficients;
  }

  /**
   * Accumulation of all warnings and errors generated during relocation
   * 
   * @param message
   */
  public void setErrorMessage(String message) {
    this.errorMessage = message;
  }

  /**
   * Accumulation of all warnings and errors generated during relocation
   * 
   * @return
   */
  public String getErrorMessage() {
    return this.errorMessage;
  }

  /**
   * Accumulation of all warnings and errors generated during relocation
   * 
   * @return
   */
  public void appendErrorMessage(String message) {
    this.errorMessage = this.errorMessage + message;
  }

  /**
   * Returns true if a valid location was successfully computed for this Source. False if the
   * calculation failed for some reason. See getErrorMessage() and/or log file for information about
   * why the location failed.
   * 
   * @return
   */
  public boolean isValid() {
    return this.valid;
  }

  /**
   * Defaults to false. Then set to true if the event was successfully relocated.
   * 
   * @param valid
   */
  public void setValid(boolean valid) {
    this.valid = valid;
  }

  /**
   * Retrieve a deep copy of this Location
   * 
   * @return
   * @throws Exception
   */
  public Location getLocation() {
    return new Location(this.v, this.radius, this.time);
  }

  public int getFlinnEngdahlGeoRegionIndex() {
    return FlinnEngdahlCodes.getGeoRegionIndex(getLatDegrees(), getLonDegrees());
  }

  public int getFlinnEngdahlSeismicRegionIndex() {
    return FlinnEngdahlCodes.getSeismicRegionIndex(getLatDegrees(), getLonDegrees());
  }

  public String getFlinnEngdahlGeoRegionName() {
    return FlinnEngdahlCodes.getGeoRegionName(getLatDegrees(), getLonDegrees());
  }

  public String getFlinnEngdahlSeismicRegionName() {
    return FlinnEngdahlCodes.getSeismicRegionName(getLatDegrees(), getLonDegrees());
  }

  public void setSumSQRWeightedResiduals(double sumSQRWeightedResiduals) {
    this.sumSQRWeightedResiduals = sumSQRWeightedResiduals;
  }

  public double getSumSQRWeightedResiduals() {
    return sumSQRWeightedResiduals;
  }

  public double getRMSWeightedResiduals() {
    return sumSQRWeightedResiduals >= 0 ? sqrt(sumSQRWeightedResiduals / getNobs()) : -1.;
  }

  public int restoreOriginalDefiningStatus() {
    int nobs = 0;
    for (Observation obs : observations.values()) {
      obs.setTimedef(obs.isTimedefOriginal());
      obs.setAzdef(obs.isAzdefOriginal());
      obs.setSlodef(obs.isSlodefOriginal());

      if (obs.isTimedef())
        ++nobs;
      if (obs.isAzdef())
        ++nobs;
      if (obs.isSlodef())
        ++nobs;
    }
    return nobs;
  }

  /**
   * Return set of unique Receivers, including those with defining and non-defining observations.
   * 
   * @return
   */
  public Map<String, Receiver> getReceivers() {
    return getReceivers(null);
  }

  /**
   * Return set of unique Receivers. If defining is true, include only stations with defining
   * observations. If defining is false, include only stations with all non-defining observations.
   * If defining is null, include all stations.
   * 
   * @param defining
   * @return
   */
  public Map<String, Receiver> getReceivers(Boolean defining) {
    Map<String, Receiver> receivers = new TreeMap<>();
    for (Observation obs : observations.values())
      if (defining == null || (obs.isDefining() == defining))
        receivers.put(obs.getReceiver().getSta(), obs.getReceiver());
    return receivers;
  }

  /**
   * Return number of unique Receivers, including those with defining and non-defining observations.
   * 
   * @return
   */
  public int getNSta() {
    return getReceivers(null).size();
  }

  /**
   * Return number of unique Receivers. If defining is true, include only stations with defining
   * observations. If defining is false, include only stations with all non-defining observations.
   * If defining is null, include all stations.
   * 
   * @param defining
   * @return
   */
  public int getNSta(Boolean defining) {
    return getReceivers(defining).size();
  }

  /**
   * Return number of observation components (tt,az,slo) that were switched from defining to
   * nondefining during location calculation
   * 
   * @return
   */
  public int countRemovedObservations() {
    int n = 0;
    for (Observation observation : observations.values())
      for (ObservationComponent obs : observation.getObservationComponents().values())
        if (obs.isDefiningOriginal() && !obs.isDefining())
          ++n;
    return n;
  }

  public boolean hasLocalMinima() {
    return hasLocalMinima;
  }

  public void hasLocalMinima(boolean hasLocalMinima) {
    this.hasLocalMinima = hasLocalMinima;
  }

  public List<Location> getLocationTrack() {
    if (locationTrack == null)
      locationTrack = new ArrayList<>();
    return locationTrack;
  }

  /**
   * All the observationComponents of type TT, AZ, SH, including defining, non-defining, invalids,
   * etc. These are references to the same observationComponents that are owned by Observation
   * objects.
   * 
   * @return
   */
  public List<ObservationComponent> getObsComponents() {
    if (obsComponents == null) {
      obsComponents = new ArrayList<>(observations.size() * 3);
      for (Observation obs : observations.values())
        obsComponents.addAll(obs.getObservationComponents().values());
    }
    return obsComponents;
  }

  /**
   * All the observationComponents of type TT, AZ, SH, including either defining or non-defining,
   * depending on the value of argument. These are references to the same observationComponents that
   * are owned by Observation objects.
   * 
   * @param defining if true, include only defining observation compoenents. If false include only
   *        non-defining.
   * @return a copy of the observation components list.
   */
  public List<ObservationComponent> getObsComponents(boolean defining) {
    List<ObservationComponent> obs = new ArrayList<>(getObsComponents().size());
    for (ObservationComponent o : getObsComponents())
      if (o.isDefining() == defining)
        obs.add(o);
    return obs;
  }

  /**
   * Sort the list of ObservationComponents in order of sta/phase/attribute
   */
  public void sortObservationComponentss() {
    sortObservationComponents("sta/phase/attribute");
  }

  /**
   * Sort the list of ObservationComponents in order defined by sortOrder.
   * 
   * @param sortOrder one of "distance", "weightedResidual", "observationid", "sta/phase/attribute"
   */
  public void sortObservationComponents(String sortOrder) {
    List<ObservationComponent> list = getObsComponents();

    if (sortOrder == null)
      sortOrder = "sta/phase/attribute";

    if (sortOrder.equalsIgnoreCase("distance")) {
      Collections.sort(list, new Comparator<ObservationComponent>() {
        @Override
        public int compare(ObservationComponent o1, ObservationComponent o2) {
          int compare = (int) Math
              .signum(o1.getObservation().getDistance() - o2.getObservation().getDistance());
          if (compare != 0)
            return compare;
          if (o1.getSta().equals(o2.getSta()))
            if (o1.getPhase() == o2.getPhase())
              return o2.getObsTypeShort().compareTo(o1.getObsTypeShort());
            else
              return o1.getPhase().toString().compareTo(o2.getPhase().toString());
          return o1.getSta().compareTo(o2.getSta());
        }
      });
    } else if (sortOrder.contains("weightedresidual")) {

      Collections.sort(list, new Comparator<ObservationComponent>() {

        @Override
        public int compare(ObservationComponent o1, ObservationComponent o2) {
          int compare = (int) Math
              .signum(Math.abs(o1.getWeightedResidual()) - Math.abs(o2.getWeightedResidual()));
          if (compare != 0)
            return compare;
          if (o1.getSta().equals(o2.getSta()))
            if (o1.getPhase() == o2.getPhase())
              return o2.getObsTypeShort().compareTo(o1.getObsTypeShort());
            else
              return o1.getPhase().toString().compareTo(o2.getPhase().toString());
          return o1.getSta().compareTo(o2.getSta());
        }

      });
    } else if (sortOrder.equals("observationid")) {

      Collections.sort(list, new Comparator<ObservationComponent>() {

        @Override
        public int compare(ObservationComponent o1, ObservationComponent o2) {
          int compare =
              (int) Math.signum(Math.abs(o1.getObservationid()) - Math.abs(o2.getObservationid()));
          if (compare != 0)
            return compare;
          if (o1.getSta().equals(o2.getSta()))
            if (o1.getPhase() == o2.getPhase())
              return o2.getObsTypeShort().compareTo(o1.getObsTypeShort());
            else
              return o1.getPhase().toString().compareTo(o2.getPhase().toString());
          return o1.getSta().compareTo(o2.getSta());
        }

      });
    } else if (sortOrder.equals("sta/phase/attribute")) {
      Collections.sort(list, new Comparator<ObservationComponent>() {

        @Override
        public int compare(ObservationComponent o1, ObservationComponent o2) {
          if (o1.getSta().equals(o2.getSta()))
            if (o1.getPhase() == o2.getPhase())
              return o2.getObsTypeShort().compareTo(o1.getObsTypeShort());
            else
              return o1.getPhase().toString().compareTo(o2.getPhase().toString());
          return o1.getSta().compareTo(o2.getSta());
        }

      });
    }
  }

  /**
   * Search all the time defining observations and find the one that is closest to the source.
   * 
   * @return
   */
  public Observation findClosestObservation() {
    double minDistance = Double.POSITIVE_INFINITY;
    Observation closest = null;
    for (Observation obs : getObservations().values())
      if (obs.isTimedef() && obs.getDistance() < minDistance) {
        minDistance = obs.getDistance();
        closest = obs;
      }
    return closest;
  }


  /**
   * Parse origins, assocs, arrivals and sites from a locoo3d output log file.
   * 
   * @param log_file
   * @return
   * @throws Exception
   */
  static public Map<Long, Source> readLocOOLogFile(File log_file) throws Exception {

    // map from sourceId to Source
    Map<Long, Source> sources = new HashMap<>();

    NetworkExtended network = new NetworkExtended();

    Source currentSource = null;
    Scanner in = new Scanner(log_file);
    String line = in.nextLine();
    String algorithm = line.substring(0, line.indexOf(("started"))).replace(" v. ", "").trim();
    while (in.hasNextLine()) {
      line = in.nextLine();
      if (line.startsWith("Input location:")) {
        line = in.nextLine();
        line = in.nextLine();
        line = in.nextLine();
        Scanner s = new Scanner(line);
        long sourceId = s.nextLong();
        long evid = s.nextLong();
        currentSource =
            new Source(new GeoVector(s.nextDouble(), s.nextDouble(), s.nextDouble(), true));
        currentSource.setAlgorithm(algorithm);
        currentSource.setSourceId(sourceId);
        currentSource.setEvid(evid);
        currentSource.setTime(s.nextDouble());
        s.close();
        sources.put(currentSource.getSourceId(), currentSource);
      }

      if (line.startsWith("Site Table:")) {
        line = in.nextLine();
        line = in.nextLine();
        int iStaName = line.indexOf("StaName");
        line = in.nextLine();
        while (line.length() > 0) {
          Scanner s = new Scanner(line);
          SiteExtended site = new SiteExtended();
          site.setSta(s.next());
          site.setOndate(s.nextLong());
          site.setOffdate(s.nextLong());
          site.setLatLonElev(s.nextDouble(), s.nextDouble(), s.nextDouble());
          site.setStaname(line.substring(iStaName).trim());
          network.add(site);
          s.close();
          line = in.nextLine();
        }
      }

      if (line.startsWith("Observation Table:")) {
        line = in.nextLine();
        line = in.nextLine();
        line = in.nextLine();
        while (line.length() > 0) {
          Scanner s = new Scanner(line);
          boolean defining = line.indexOf('*') >= 0;
          long arid = s.nextLong();
          String sta = s.next();
          String phase = s.next();
          String type = s.next();
          if (defining)
            s.next(); // skip the * character
          s.nextDouble(); // ignore distance
          double observed = s.nextDouble();
          double err = s.nextDouble();
          s.close();

          Observation obs = currentSource.getObservation(arid);
          if (obs == null) {
            currentSource.getObservations().put(arid, obs = new Observation());
            obs.setSource(currentSource);
            obs.setObservationId(arid);
            obs.setRequestedAttributes(EnumSet.noneOf(GeoAttributes.class));
          }

          obs.setPhase(SeismicPhase.valueOf(phase));
          if (type.equals("TT")) {
            double arrivalTime = currentSource.getTime() + observed;
            obs.setTime(arrivalTime);
            obs.setDeltim(err);
            obs.setTimedef(defining);
            long jdate = GMTFormat.getJDate(arrivalTime);
            SiteExtended site = network.getSite(sta, jdate);
            obs.setReceiver(new Receiver(site));
          } else if (type.equals("SH")) {
            // convert slowness from s/deg to s/radian
            obs.setSlow(Math.toDegrees(observed));
            obs.setDelslo(Math.toDegrees(err));
            obs.setSlodef(defining);
          } else if (type.equals("AZ")) {
            // convert azimuth from degrees to radians
            obs.setAzimuth(Math.toRadians(observed));
            obs.setDelaz(Math.toRadians(err));
            obs.setAzdef(defining);
          }
          line = in.nextLine();
        }
      }

      if (line.startsWith("Itt=1 It=1 ")) {
        boolean[] fixed = new boolean[4];
        if (line.split("\\s+")[3].equals("M=3"))
          fixed[2] = true;
        currentSource.setFixed(fixed);
      }
    }

    in.close();
    return sources;
  }


}
