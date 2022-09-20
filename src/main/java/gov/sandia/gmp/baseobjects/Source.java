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

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

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

	private long sourceId; // = orid

	private long evid;

	private double gtLevel = -1.0;

	private boolean gtTime = false;

	private int numberOfAssocs = -1;

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
	 * @throws GMPException
	 */
	public Source(OriginExtended origin) throws GMPException {
		this(origin.getOrid(), origin.getEvid(),
				new GeoVector(origin.getLat(), origin.getLon(), origin.getDepth(), true), origin.getTime(),
				Globals.NA_VALUE);
	}

	public Source(long evid, double[] unitVector, double radius, double time) throws GMPException {
		super(new GeoVector(unitVector, radius), time);
		this.evid = evid;
	}

	public Source(gov.sandia.gnem.dbtabledefs.gmp.Source s) throws Exception {
		super(new GeoVector(s.getLat(), s.getLon(), s.getDepth(), true), s.getOrigintime());
		sourceId = s.getSourceid();
		evid = s.getEventid();
		numberOfAssocs = (int) s.getNumassoc();
		gtLevel = s.getGtlevel();
		gtTime = false;
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

	public int getNumberOfAssocs() {
		return numberOfAssocs;
	}

	public void setNumberOfAssocs(int numberOfAssocs) {
		this.numberOfAssocs = numberOfAssocs;
	}

	/**
	 * Retrieve data needed to make a SOURCE database row.
	 * 
	 * @return Object[]
	 */
	public gov.sandia.gnem.dbtabledefs.gmp.Source getSourceRow()
	{
		return new gov.sandia.gnem.dbtabledefs.gmp.Source(sourceId, evid, getLatDegrees(), getLonDegrees(), getDepth(),
				time, gtLevel, numberOfAssocs, -1L, // polygonid ???
				GMPGlobals.getAuth());
	}

	/**
	 * Retrieve data needed to make a SOURCE database row.
	 * 
	 * @return Object[]
	 */
	public Origin getOriginRow()

	{
		return new Origin(getLatDegrees(), getLonDegrees(), getDepth(), time, sourceId, evid, GMTFormat.getJDate(time),
				-1, -1, -1, -1, -1, "-", -999., "-", -999., -1, -999., -1, -999., -1, "-", GMPGlobals.getAuth(), -1);
	}

}
