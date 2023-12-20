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
package gov.sandia.gmp.ak135rays;

import java.util.ArrayList;
import edu.sc.seis.TauP.Arrival;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;

public class TaupArrivalList extends ArrayList<Arrival> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//Declare these variables outside the constructor. Then when you make a new object inheriting this class, it will assign the values inside the constructor
	//to the variables declared here by default, making them global inside this class. In AK135Rays.java, can pull these parameters out as taupArrivalList.mohoDepth, etc.
	private PredictionRequest request;
	double mohoDepth;
	double cmbDepth;
	double icbDepth;

	public TaupArrivalList() {
		super();
	}
	
	//Below will turn any type of list passed in into an ArrayList. This constructor constructs a TaupArrivalList object
	//public TaupArrivalList(List<Arrival> arrivals, PredictionRequest request, AK135Rays ak135RaysPredictor)
	/** Construct a TaupArrivalList object based on request
	 * @param request
	 * @param ak135RaysPredictor
	 */
	public TaupArrivalList(PredictionRequest request, AK135Rays ak135RaysPredictor) {

		this.request = request;
		mohoDepth = ak135RaysPredictor.getMohoDepth();
		cmbDepth = ak135RaysPredictor.getCmbDepth();
		icbDepth = ak135RaysPredictor.getIcbDepth();
		
	}
	
	/** Get the taup phase list corresponding to the user requested phase contained in request
	 * @return phaseList
	 */
	public String[] getPhaseList() {
		
		String[] phaseList = null;
		
		
		if (request.getPhase() == SeismicPhase.Pg)
			phaseList = new String[] { "p", "Pg" };
		else if (request.getPhase() == SeismicPhase.Sg
				|| request.getPhase() == SeismicPhase.Lg)
			phaseList = new String[] { "s", "Sg" };
		else if (request.getPhase() == SeismicPhase.Pn)
		{
			// taup toolkit defines Pn as a headwave while the definition
			// below produces refracted waves. Also, at distances less than the
			// minimum refraction distance (critical angle at moho) taup
			// returns no value while this code returns PmP.
			if (request.getSource().getDepth() < mohoDepth)
				phaseList = new String[] { "P", "Pvmp" };
			else
				phaseList = new String[] { "p", "P" };
		}
		else if (request.getPhase() == SeismicPhase.Sn)
		{
			// taup toolkit defines Sn as a headwave while the definition
			// below produces refracted waves.
			if (request.getSource().getDepth() < mohoDepth)
				phaseList = new String[] { "S", "SvmS" };
			else
				phaseList = new String[] { "s", "S" };
		}
		else if (request.getPhase() == SeismicPhase.PmP)
		{
			phaseList = new String[] { "Pvmp" };
		}
		else if (request.getPhase() == SeismicPhase.P)
		{
			if (request.getDistanceDegrees() < 80.)
				phaseList = new String[] { "p", "P" };
			else
				phaseList = new String[] { "P", "Pdiff", "PKP", "PKIKP" };
		}
		else if (request.getPhase() == SeismicPhase.S)
		{
			if (request.getDistanceDegrees() < 80.)
				phaseList = new String[] { "s", "S" };
			else
				phaseList = new String[] { "S", "Sdiff", "SKS", "SKIKS" };
		}
		else if (request.getPhase() == SeismicPhase.PKP
				|| request.getPhase() == SeismicPhase.PKPdf)
		{
			phaseList = new String[] { "PKiKP", "PKIKP" };
		}
		else if (request.getPhase() == SeismicPhase.PKPab
				|| request.getPhase() == SeismicPhase.PKPbc)
		{
			phaseList = new String[] { "PKP" };
		}
		else if (request.getPhase() == SeismicPhase.Pmantle)
		{
			if (request.getDistanceDegrees() < 30.)
			{
				// either PmP or Pn
				if (request.getSource().getDepth() <= mohoDepth)
					phaseList = new String[] { "P", "Pvmp" };
				else
					phaseList = new String[] { "p", "P" };
			}
			else
				phaseList = new String[] { "P", "Pdiff" };
		}
		else
			phaseList = new String[] { request.getPhase().toString() };
		
		return phaseList;
		
	}

}
