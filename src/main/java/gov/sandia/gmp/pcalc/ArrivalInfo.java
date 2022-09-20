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
package gov.sandia.gmp.pcalc;

import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;

/**
 * Container class for arid, arrival time, observed azimuth, observed slowness,
 * timedef, azdef and slodef.
 * @author sballar
 *
 */
public class ArrivalInfo
{
	long arid;
	double time, azimuth, slow;
	char timedef, azdef, slodef;

	ArrivalInfo(long arid, double time, char timedef, double azimuth, char azdef, double slow, char slodef)
	{
		this.arid = arid;
		this.time = time;
		this.timedef = timedef;
		this.azimuth = azimuth;
		this.azdef = azdef;
		this.slow = slow;
		this.slodef = slodef;
	}

	public ArrivalInfo(AssocExtended assoc) {
		this(assoc.getArid(), 
				assoc.getArrival().getTime(), (assoc.isTimedef() ? 'd' : 'n'),
				assoc.getArrival().getAzimuth(), (assoc.isAzdef() ? 'd' : 'n'),
				assoc.getArrival().getSlow(), (assoc.isSlodef() ? 'd' : 'n'));
	}
}
