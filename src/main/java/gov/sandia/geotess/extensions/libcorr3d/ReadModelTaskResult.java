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
package gov.sandia.geotess.extensions.libcorr3d;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.HTML.Attribute;

import gov.sandia.gmp.parallelutils.ParallelResult;
import gov.sandia.gmp.util.globals.Site;

public class ReadModelTaskResult extends ParallelResult {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<Integer> handles;
	private ArrayList<LibCorr3DModel> models;
	private ArrayList<Site> sites; 
	private ArrayList<List<String>> supportedPhases;
	private ArrayList<String> attributes;
	private ArrayList<String> errorMessages;
	private long ellapsedTime;


	public ReadModelTaskResult(int filesPerTask) {
		handles = new ArrayList<Integer>(filesPerTask);
		models = new ArrayList<LibCorr3DModel>(filesPerTask);
		sites = new ArrayList<Site>(filesPerTask);
		supportedPhases = new ArrayList<List<String>>(filesPerTask);
		attributes = new ArrayList<String>(filesPerTask);
		errorMessages = new ArrayList<String>(filesPerTask);
		
		for (int i=0; i<filesPerTask; ++i) {
			handles.add(-1);
			models.add(null);
			sites.add(null);
			supportedPhases.add(null);
			attributes.add(null);
			errorMessages.add("");
		}
	}
	
	public int size() { return handles.size(); }

	public void setHandle(int i, int handle) {
		this.handles.set(i, handle);
	}
	
	public int getHandle(int i) { return handles.get(i); }

	public LibCorr3DModel getModel(int i) {
		return models.get(i);
	}

	public void setModel(int i, LibCorr3DModel model) {
		this.models.set(i, model);
	}

	public Site getSite(int i) {
		return sites.get(i);
	}

	public void setSite(int i, Site site) {
		this.sites.set(i, site);
	}

	public List<String> getSupportedPhases(int i) {
		return supportedPhases.get(i);
	}

	public void setSupportedPhases(int i, List<String> supportedPhases) {
		this.supportedPhases.set(i, supportedPhases);
	}

	public String getAttribute(int i) {
		return attributes.get(i);
	}

	public void setAttribute(int i, String attribute) {
		this.attributes.set(i, attribute);
	}

	public String getErrorMessage(int i) {
		return errorMessages.get(i);
	}

	public void setErrorMessage(int i, String errorMessage) {
		this.errorMessages.set(i, errorMessage);
	}

	public long getEllapsedTime() {
		return ellapsedTime;
	}

	public void setEllapsedTime(long ellapsedTime) {
		this.ellapsedTime = ellapsedTime;
	}

}
