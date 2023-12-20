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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.parallelutils.ParallelResult;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.profiler.ProfilerContent;

/**
 * Container for an ArrrayList of LocOOResult objects, each of which represents
 * the result of locating a single seismic event.
 * 
 * @author sballar
 *
 */
public class LocOOTaskResult extends ParallelResult {
	private static final long serialVersionUID = 2335137646020639037L;

	private Map<Long, Source> sources;

	private int index;

	/**
	 * Used to retrieve profiler information if it was turned on in the task.
	 */
	private ProfilerContent aProfilerContent = null;

	private ScreenWriterOutput taskLog;

	private ScreenWriterOutput taskErrorLog;

	public LocOOTaskResult() {
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

	public void clear() {
	    if(sources != null) sources.clear();
	}

	public Map<Long, Source> getSources() {
	    if(sources != null) return sources;
	    return new HashMap<>();
	}

	public void setSources(Map<Long, Source> sources2) {
	    this.sources = sources2;
	}

	public ScreenWriterOutput getTaskLog() {
	    return taskLog;
	}

	public void setTaskLog(ScreenWriterOutput taskLog) {
	    this.taskLog = taskLog;
	    if (this.taskLog.getVerbosity() >= 1)
		this.taskLog.write(String.format(
			"Status Log - Finished LoOOTask %6d %s%n", index,
			GMTFormat.localTime.format(new Date())));
	}

	public ScreenWriterOutput getTaskErrorLog() {
	    return taskErrorLog;
	}

	public void setTaskErrorLog(ScreenWriterOutput taskErrorLog) {
	    this.taskErrorLog = taskErrorLog;
	}
	
	@Override
	public String toString() {
	  StringBuilder sb = new StringBuilder(getClass().getCanonicalName()).append(":\n");
      if (sources != null && !sources.isEmpty()) {
        for (Long id : new TreeSet<>(sources.keySet())) {
          sb.append(" - ID=").append(id).append(", source=").append(sources.get(id)).append("\n");
        }
      }
      else sb.append(" - empty/null");
	  return sb.toString();
	}
}
