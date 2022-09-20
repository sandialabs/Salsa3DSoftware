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

import java.util.ArrayList;
import java.util.HashMap;

import gov.sandia.gmp.parallelutils.ParallelResult;
import gov.sandia.gmp.util.profiler.ProfilerContent;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.ArrivalExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

/**
 * Container for an ArrrayList of LocOOResult objects, each of which represents
 * the result of locating a single seismic event.
 * 
 * @author sballar
 *
 */
public class LocOOTaskResult extends ParallelResult {
	private static final long serialVersionUID = 2335137646020639037L;

	/**
	 * The list of LocOOResult objects corresponding to EventList inputs.
	 */
	private ArrayList<LocOOResult> locooResults = null;

	/**
	 * a reference to all the arrivals passed in to LocooTask when it was
	 * constructed.
	 */
	private HashMap<Long, ArrivalExtended> originalArrivals;

	private StringBuffer log;

	private StringBuffer errorlog;

	private int index;

	/**
	 * Used to retrieve profiler information if it was turned on in the task.
	 */
	private ProfilerContent aProfilerContent = null;

	public LocOOTaskResult(int size) {
		locooResults = new ArrayList<LocOOResult>(size);
	}

	public void addResult(LocOOResult result) {
		locooResults.add(result);
	}

	public LocOOResult getResult(int i) {
		return locooResults.get(i);
	}

	public ArrayList<LocOOResult> getResults() {
		return locooResults;
	}
	
	public ArrayList<OriginExtended> getOutputOrigins()
	{
		ArrayList<OriginExtended> outputOrigins = new ArrayList<OriginExtended>(locooResults.size());
		for (LocOOResult result : locooResults)
			if (result.getOriginRow() != null)
				outputOrigins.add(result.getOriginRow());
		return outputOrigins;
	}

	public void setOriginalArrivals(
			HashMap<Long, ArrivalExtended> originalArrivals) {
		this.originalArrivals = originalArrivals;
	}

	public HashMap<Long, ArrivalExtended> getOriginalArrivals() {
		return this.originalArrivals;
	}

	/**
	 * @param log
	 *            the log to set
	 */
	public void setLog(StringBuffer log) {
		this.log = log;
	}

	/**
	 * @return the log
	 */
	public StringBuffer getLog() {
		return log;
	}

	/**
	 * @param errorlog
	 *            the errorlog to set
	 */
	public void setErrorlog(StringBuffer errorlog) {
		this.errorlog = errorlog;
	}

	/**
	 * @return the errorlog
	 */
	public StringBuffer getErrorlog() {
		return errorlog;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Set profiler content.
	 * 
	 * @param pc
	 *            The profiler content to set.
	 */
	public void setProfilerContent(ProfilerContent pc) {
		aProfilerContent = pc;
	}

	/**
	 * Returns the profiler content.
	 * 
	 * @return The profiler content.
	 */
	public ProfilerContent getProfilerContent() {
		return aProfilerContent;
	}

	// @Override
	// public String getHostName()
	// {
	// return hostName;
	// }
	//
	// @Override
	// public void setHostName(String hostName)
	// {
	// this.hostName = hostName;
	// }

}
