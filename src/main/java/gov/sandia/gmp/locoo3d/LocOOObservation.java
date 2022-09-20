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

import java.io.Serializable;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.ArrivalExtended;

public class LocOOObservation implements Serializable
{
	private static final long serialVersionUID = 6868320048232791689L;

	// na values are all Globals.NA_VALUE and 
	// all angular values are in radians.
	public long observationid, receiverid;
	public String phaseName;
	public SeismicPhase phase;
	public double time, deltim, azimuth, delaz, slow, delslo;
	public char timedef, azdef, slodef;
	
	
	public LocOOObservation() { }
	
	/**
	 * 
	 * @param observationid 
	 * @param receiverid
	 * @param phase
	 * @param time arrival time (epoch time: seconds since 1970)
	 * @param deltim arrival time uncertainty in seconds.
	 * @param timedef is time defining
	 * @param azimuth observed azimuth in radians.
	 * @param delaz uncertainty of the observed azimuth in radians.
	 * @param azdef is azimuth defining
	 * @param slow observed horizontal slowness in seconds/radian.
	 * @param delslo uncertainty of observed horizontal slowness in seconds/radian.
	 * @param slodef is slowness defining.
	 */
	public LocOOObservation(long observationid, long receiverid, 
			String phaseName, 
			double time, double deltim, char timedef,
			double azimuth, double delaz, char azdef,
			double slow, double delslo, char slodef)
	{
		this.observationid = observationid;
		this.receiverid = receiverid;
		this.phaseName = phaseName;
		try
		{
			this.phase = SeismicPhase.valueOf(phaseName);
		}
		catch (Exception e)
		{
			this.phase = SeismicPhase.NULL;
		}
		this.time = time;
		this.deltim = deltim;
		this.timedef = timedef;
		this.azimuth = azimuth;
		this.delaz = delaz;
		this.azdef = azdef;
		this.slow = slow;
		this.delslo = delslo;
		this.slodef = slodef;
	}

	@Override
	public String toString()
	{
		return String.format("%12d %7d %9s %10.3f %7.3f %5c %10.3f %7.3f %5c %10.3f %7.3f %5c",
				observationid, receiverid,
				phaseName,
				time, deltim, timedef, 
				(azimuth == ArrivalExtended.AZIMUTH_NA ? azimuth : Math.toDegrees(azimuth)), 
				(delaz == ArrivalExtended.DELAZ_NA ? delaz : Math.toDegrees(delaz)), 
				azdef,
				(slow == ArrivalExtended.SLOW_NA ? slow : Math.toRadians(slow)), 
				(delslo == ArrivalExtended.DELSLO_NA ? delslo : Math.toRadians(delslo)), 
				slodef
		);

	}

}
