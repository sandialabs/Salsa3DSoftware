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
package gov.sandia.gmp.bender.phase;

import java.io.IOException;
import java.util.HashMap;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.bender.bouncepoints.BouncePoints;

/**
 * Simple class to load bottom side reflection bounce points for a number of 
 * supported phases. The files are automatically read if requested supported
 * phase is input. The only way to get one of these objects is through the
 * static method factory getTauPPhaseBSR(String phaseName). If the phase is
 * supported it is returned. If not, null is returned. Once given the returned
 * TauPPhaseBottomSideReflection object the method
 * 
 * 		getBSRPoint(double depth, double distance)
 * 
 * can be called to obtain the angular distance from the source for the location
 * of the bounce point.
 * 
 * @author jrhipp
 *
 */
public class TauPPhaseBottomSideReflection
{
	/*
	 * Static map containing previously loaded TauPPhaseBottomSideReflection
	 * objects so that they are never created more than once.
	 */
	private static HashMap<SeismicPhase, TauPPhaseBottomSideReflection> BSRPhaseMap = null;
	
	/**
	 * The phase name of this objects data.
	 */
	private String							phase				= "";
	
	/**
	 * The array of distance locations (degrees) at which bounce points are
	 * recorded.
	 */
	private static final double[]	dist			= BouncePoints.bouncePointDistance;

	/**
	 * The distance delta in degrees. Note that the distance grid increment is
	 * assumed to be a constant.
	 */
	private static final double deldist			= dist[1] - dist[0];

	/**
	 * The array of depths (km) at which bounce points are
	 * recorded.
	 */
	private static final double[]	depth			= BouncePoints.bouncePointDepth;

	/**
	 * The depth delta in km. Note that the depth grid increment is
	 * assumed to be a constant.
	 */
	private static final double deldpth			= depth[1] - depth[0];

	/**
	 * The 2D array of bounce point locations [depth][distance].
	 */
  private double[][] BSRPoint							= null;

  /**
	 * Private constructor. Note that the only way to get one of these objects is
	 * through the static method factory getTauPPhaseBSR(String phaseName).
	 * 
	 * @param phase  Phase name for which the bounce point data will be loaded.
	 * @throws IOException
	 */
	private TauPPhaseBottomSideReflection(SeismicPhase phase) throws IOException
	{
		BSRPoint = BouncePoints.getBouncePoints(phase);

		synchronized(this)
		{
			if (BSRPhaseMap == null)
				BSRPhaseMap = new HashMap<SeismicPhase, TauPPhaseBottomSideReflection>();
			
			BSRPhaseMap.put(phase, this);
		}		
	}

	/**
	 * Returns true if the input phase name is supported.
	 * 
	 * @param phasename Name to be tested for support.
	 * @return True if the input phase name is supported.
	 */
	public static boolean isPhaseSupported(SeismicPhase phase)
	{
		return (BouncePoints.getBouncePoints(phase) != null);
	}

	/**
	 * Public static method to obtain a TauPPhaseBottomSideReflection object for
	 * the input phase name. If the input phase name is not supported null is
	 * returned.
	 * 
	 * @param phase The phase name for which the associated TauPPhaseBottomSideReflection
	 *              is returned.
	 * @return The TauPPhaseBottomSideReflection associated with the input phase
	 *             name, or null if the phase is not supported.
	 * @throws IOException
	 */
	public static TauPPhaseBottomSideReflection getTauPPhaseBSR(SeismicPhase phase) throws IOException
	{
		if (isPhaseSupported(phase))
		{
			if ((BSRPhaseMap == null) || (BSRPhaseMap.get(phase) == null))
				return new TauPPhaseBottomSideReflection(phase);
			else
				return BSRPhaseMap.get(phase);
		}
		else
			return null;
	}

	/**
	 * Interpolated the bounce point given the input depth (km) and distance (deg).
	 * 
	 * @param dpth Depth at which the bounce point is interpolated.
	 * @param dst  Distance at which the bounce point is interpolated.
	 * @return The bounce point for the specified depth and distance.
	 */
	public double getBSRPoint(double dpth, double dst)
	{
		// calculate the start index for the depth and distance

		int dpthIndx = (int) (dpth / deldpth);
		int distIndx = (int) (dst  / deldist);

		if (dpthIndx < 0)
		{
			if (dpth < depth[0])
			{
				dpth = 0.0;
				dpthIndx = 0;
			}
		}

		// calculate the depth fraction and interpolate the bounce point with
		// depth along the two fixed distance indexes (distIndx and distIndx+1).

		double fdpth = (dpth - depth[dpthIndx]) /
									 (depth[dpthIndx + 1] - depth[dpthIndx]);
		double bsrDpth1 = fdpth * BSRPoint[dpthIndx+1][distIndx] +
											(1.0 - fdpth) * BSRPoint[dpthIndx][distIndx];
		double bsrDpth2 = fdpth * BSRPoint[dpthIndx+1][distIndx+1] +
											(1.0 - fdpth) * BSRPoint[dpthIndx][distIndx+1];

		// calculate the distance fraction and interpolate in the distance direction

		double fdist = (dst - dist[distIndx]) /
									 (dist[distIndx + 1] - dist[distIndx]);
		double bsrPnt = fdist * bsrDpth2 + (1.0 - fdist) * bsrDpth1;

		// return the bounce point.

		return bsrPnt;
	}

	/**
	 * Returns the phase name.
	 * 
	 * @return The phase name.
	 */
	public String getPhase()
	{
		return phase;
	}

	/**
	 * Returns the distance array.
   *
	 * @return The distance array.
	 */
	public double[] getDistancePoints()
	{
		return dist;
	}

	/**
	 * Returns the depth array.
   *
	 * @return The depth array.
	 */
	public double[] getDepthPoints()
	{
		return depth;
	}
}
