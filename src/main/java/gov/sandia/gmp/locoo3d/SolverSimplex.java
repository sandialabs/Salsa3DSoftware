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

import java.util.Arrays;

import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.numerical.simplex.Simplex;
import gov.sandia.gmp.util.numerical.simplex.SimplexFunction;

public class SolverSimplex implements SimplexFunction
{
	
	private Event event;
	
	private Simplex simplex;
	
	/**
	 * The indices of the event location parameters that are 
	 * free to change during relocation.
	 */
	protected ArrayListInt locPar = new ArrayListInt(4);
	
	int ndim;

	private Location originalLocation;
	
	private double originalRMS;
	
	public SolverSimplex()
	{
		this.simplex = new Simplex(this, 1e-7, 1000);
	}
	
	public double locate(Event event) throws Exception
	{
		this.event = event;
		this.originalLocation = event.getLocation();
		//this.originalLocation = event.getInitialLocation();
		this.originalRMS = event.rmsWeightedResidual();
		
		Arrays.fill(event.dloc, 0.);

		locPar.clear();
		
		if (event.isFree(GMPGlobals.LAT))
			locPar.add(GMPGlobals.LAT);
		if (event.isFree(GMPGlobals.LON))
			locPar.add(GMPGlobals.LON);
		if (event.isFree(GMPGlobals.DEPTH))
			locPar.add(GMPGlobals.DEPTH);
		if (event.isFree(GMPGlobals.TIME))
			locPar.add(GMPGlobals.TIME);
		
		ndim = locPar.size();
		
		double[][] p = new double[ndim+1][ndim];
		int index = 0;
		
		if (event.isFree(GMPGlobals.LAT))
			p[index][index++] = 100./originalLocation.getRadius();
		if (event.isFree(GMPGlobals.LON))
			p[index][index++] = 100./originalLocation.getRadius();
		if (event.isFree(GMPGlobals.DEPTH))
			p[index][index++] = 50.;
		if (event.isFree(GMPGlobals.TIME))
			p[index][index++] = 10;
		
		for (int i=0; i<ndim; ++i)
			p[ndim][i] = -p[i][i];

		try
		{
			simplex.search(p);
			
			if (simplexFunction(p[0]) > originalRMS)
			{
				// do no harm!
				event.setLocation(originalLocation);
				event.dkm = 0;
				Arrays.fill(event.dloc, 0.);
				event.getSumSqrWeightedResiduals();
			}

			return event.dkm;
		} 
		catch (Exception e)
		{
			throw new GMPException(e);
		}		
	}

	@Override
	public double simplexFunction(double[] dx) throws Exception
	{
		for (int i = 0; i < locPar.size(); i++)
			event.dloc[locPar.get(i)] = dx[i];

		event.dkm = event.moveLocation(originalLocation, event.dloc);
		
		//System.out.printf("Simplex rmsWeightedResiduals %16.12f%n", event.rmsWeightedResidual());
		
		return event.rmsWeightedResidual();
	}

}
