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
package gov.sandia.gmp.lookupdz.distanceslowness;

import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.brents.BrentsFunction;

public class BrentsObject implements BrentsFunction {

	/**
	 * Distance in degrees
	 */
	private double[] distance;
	
	/**
	 * Slowness in sec/radian.  All values are valid and are either 
	 * monotonically increasing or decreasing.
	 */
	private double[] slowness;
	
	/**
	 * Slowness value for which distance is requested. seconds/degree
	 */
	private double slow;

	public BrentsObject(double[][] distance_slowness, double slow) throws Exception {
		this.distance = distance_slowness[0];
		this.slowness = distance_slowness[1];
		this.slow = slow;

		if (slowness[slowness.length-1] < slowness[0] 
				&& (slow > slowness[0] || slow < slowness[slowness.length-1])) 
			throw new Exception(String.format("slowness %1.4f is out of slowness range [%1.4f - %1.4f]", 
					slow, slowness[0], slowness[slowness.length-1]));
		else if (slowness[slowness.length-1] > slowness[0] 
				&& (slow < slowness[0] || slow > slowness[slowness.length-1])) 
			throw new Exception(String.format("slowness %1.4f is out of slowness range [%1.4f - %1.4f]", 
					slow, slowness[0], slowness[slowness.length-1]));
	}

	public double getMaxDistance() { return distance[distance.length-1]; }

	public double getMinDistance() { return distance[0]; }

	@Override
	public double bFunc(double dist) throws Exception {
		int i=Globals.hunt(distance, dist, false, false);
		if (i < 0 || i >= distance.length-1)
			throw new Exception(String.format("Distance %1.3f is out of range [%1.3f - %1.3f]",
					dist, distance[0], distance[distance.length-1]));
		double c = (dist-distance[i])/(distance[i+1]-distance[i]);
		return slowness[i]*(1.-c) + slowness[i+1]*c - slow;
	}

}
