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
package gov.sandia.gmp.infrasoundpredictor;

import static java.lang.Math.PI;
import static java.lang.Math.ceil;
import static java.lang.Math.toRadians;

import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

public class InfrasoundModel
{
/*
	From Kyle Jones (krjones@sandia.gov)
	Phase Name				Phase Description
	N							Noise
	I							Direct Infrasound wave
	Iw							Tropospheric ducted wave with a turing height of <15-20 km
	Is							Stratospheric ducted wave with a turning height of < 60 km
	It							Thermospheric ducted wave with a turning height of < 120 km					
*/

	/**
	 * Speed of sound in air, in km/sec
	 */
	private double soundSpeed;
	
	/**
	 * A small horizontal distance, in radians, used to calculate slowness
	 * (derivative of travel time with respect to source-receiver separation).
	 */
	private static final double DX = 0.1 / 6371.; // 0.1 km converted to radians
	
	public InfrasoundModel(PropertiesPlus properties) throws PropertiesPlusException
	{
		soundSpeed = properties.getDouble("infrasoundSoundSpeed", 0.343053) ;
	}
	
	/**
	 * Retrieve the soundSpeed at position u, in km/sec.
	 * @param u unit vector of a position on the surface of the earth.
	 * @return the soundSpeed at position u, in km/sec.
	 */
	public double getSoundSpeed(double[] u)
	{
		return soundSpeed;
	}
	
	/**
	 * Retrieve sound slowness at position u, in seconds/radian.
	 * @param u
	 * @return sound slowness at position u, in seconds/radian.
	 */
	public double getSoundSlowness(double[] u)
	{
		return 6371. / soundSpeed;
	}
	
	/**
	 * Retrieve azimuth predicted at the station given an infrasound source at 
	 * event, in radians.  Will return Double.NaN if the station and event are 
	 * colocated, or if the station is located at either of the poles.
	 * @param station unit vector representing the location of the station
	 * @param event unit vector representing the location of the event.
	 * @return azimuth predicted at the station given an infrasound source at 
	 * event, in radians.
	 */
	public double getAzimuth(double[] station, double[] event)
	{
		// just return great circle azimuth from station to event.
		return Vector3D.azimuth(station, event, Double.NaN);
	}

	/**
	 * Retrieve the uncertainty of the predicted station-to-event azimuth, in radians.
	 * @param station
	 * @param event
	 * @return the uncertainty of the predicted station-to-event azimuth, in radians.
	 */
	public double getAzimuthUncertainty(double[] station, double[] event)
	{
		return toRadians(10);
	}

	/**
	 * Compute travel time in seconds.
	 * @param station
	 * @param event
	 * @return travel time in seconds.
	 */
	public double getTravelTime(double[] station, double[] event)
	{
		// for this dumb model (constant sound speed) we could just return distance/soundspeed.
		// but we will pretend that soundspeed is spatially variable.
		
		GreatCircle gc = new GreatCircle(station, event);
		
		int n = (int)ceil(gc.getDistance()/DX);
		if (n == 0)
		{
			// station and event are colocated
			return 0.;
		}
		
		double delta = gc.getDistance()/n;
		double[] u = new double[3];
		double tt = 0;
		
		for (int i=0; i<n; ++i)
		{
			gc.getPoint((i+0.5)*delta, u);
			tt += delta*getSoundSlowness(u);
		}
		
		return tt;
	}
	
	/**
	 * Retrieve the uncertainty of the predicted travel time, in seconds.
	 * @param station
	 * @param event
	 * @return the uncertainty of the predicted travel time, in seconds.
	 */
	public double getTravelTimeUncertainty(double[] station, double[] event)
	{
		return 1.;
	}

	/**
	 * Compute horizontal slowness in seconds/radian.
	 * @param station
	 * @param event
	 * @param travel time for path from event to station, in seconds.
	 * @return  horizontal slowness in seconds/radian.
	 */
	public double getSlowness(double[] station, double[] event, double travelTime)
	{
		// for this dumb model (constant sound speed) we could just return getSoundSlowness().
		// but we will pretend that sound speed is spatially variable.
		
		// we need to find a point on the earth which is DX radians away from the event
		// in direction which is directly away from the station.
		double[] u = new double[3];
		// find event-to-station azimuth
		double azimuth = Vector3D.azimuth(event, station, Double.NaN);
		
		if (Double.isNaN(azimuth))
		{
			if (Vector3D.dot(event, station) < 1e-7)
			{
				// event and station are colocated
				return getSoundSlowness(event);
			}
			
			// the event is located at one of the poles.
			if (event[2] < -0.99)
			{
				// event is at south pole
				double longitude = Vector3D.getLon(station)+PI;
				double latitude = -PI/2 + DX;
				Vector3D.getVector(latitude, longitude, u);
			}
			else
			{
				// event is at north pole
				double longitude = Vector3D.getLon(station)+PI;
				double latitude = PI/2 - DX;
				Vector3D.getVector(latitude, longitude, u);
			}
		}
		else	
			Vector3D.move(event, DX, azimuth+PI, u);
		
		return (getTravelTime(station, u)-travelTime)/DX;
	}

	public String getDescription() 
	{
		return "very simple model to compute infrasound\n"
				+ "travel time and azimuth predictions and\n"
				+ "associated uncertainties."; 
	}

}
