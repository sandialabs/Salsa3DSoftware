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
package gov.sandia.gmp.bender;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.simplex.Amoeba;
import gov.sandia.gmp.util.numerical.simplex.Simplex;
import gov.sandia.gmp.util.numerical.vector.Vector3D;

public class AmoebaBouncePoint extends Amoeba
{
	private Bender    bender     = null;
	//protected double snellsLawOnlyTolerance = 0.001;

	/**
	 * 3D Distance between simplex points defined as
	 *   simplexPointDistance[0] = distance between points 0 and 1
	 *   simplexPointDistance[1] = distance between points 1 and 2
	 *   simplexPointDistance[2] = distance between points 2 and 0
	 */
	protected double[]  simplexPointDistance = null;

	protected int[]     simplexValidPoint = null;
	protected double[]  ttTol = null;
	protected double[]  travelTime = null;
	protected double[]  snellsLawMisfit = null;
	protected GeoTessPosition[] simplexPos = null;

	protected double    currentTTTol = 0.0;
	protected int       currentValidResult = 0;
	protected double    currentTravelTime = 0.0;
	protected double    currentSnellsLawMisfit = 0.0;
  protected GeoTessPosition currentPosition = null;
  protected double[]  currentLatLon = {0.0, 0.0};

	protected double    previousTTTol = 0.0;
	protected int       previousValidResult = 0;
	protected double    previousTravelTime = 0.0;
	protected double    previousSnellsLawMisfit = 0.0;
  protected GeoTessPosition previousPosition = null;
  protected double[]  previousLatLon = {0.0, 0.0};
  
	protected double    minTTDiff = 0.001;
	protected double    minBPMove = 1.0;

	protected boolean   redefineSimplex = false;

	protected Simplex   simplex = null;

	//protected boolean   swapSnellsLaw = false;

	public void setCurrentExtendedData(double tt, double slmf,
																		 GeoTessPosition pos,
																		 double[] latlon, double tol)
				 throws GeoTessException
	{
		previousTTTol           = currentTTTol;
		previousValidResult     = currentValidResult;
	  previousTravelTime      = currentTravelTime;
	  previousSnellsLawMisfit = currentSnellsLawMisfit;
	  previousPosition        = currentPosition;
	  previousLatLon[0]       = currentLatLon[0]; 
	  previousLatLon[1]       = currentLatLon[1];

	  currentTTTol            = tol;
	  currentValidResult      = 0;
	  currentTravelTime       = tt;
	  currentSnellsLawMisfit  = slmf;
	  currentPosition         = pos.deepClone(); 
	  currentLatLon[0]        = latlon[0]; 
	  currentLatLon[1]        = latlon[1];
	}

	public void setSimplex(Simplex s)
	{
		simplex = s;
	}

	public void setMinTTDiffAndBPMove(double mttd, double mbpm)
	{
		minTTDiff = mttd;
		minBPMove = mbpm;
	}

	// override swap, replace, replaceHighPoint and initial setup in simplex
	public AmoebaBouncePoint(Bender b, double[][] simplexPoints) throws Exception
	{
		super(simplexPoints);
		bender = b;

		simplexPointDistance = new double [simplexPoints.length];
		travelTime = new double [simplexPoints.length];
		ttTol = new double [simplexPoints.length];
		simplexValidPoint = new int [simplexPoints.length];
		for (int i = 0; i < simplexPoints.length; ++i)
			travelTime[i] = Globals.NA_VALUE;
		snellsLawMisfit = new double [simplexPoints.length];
		simplexPos = new GeoTessPosition [simplexPoints.length];
	}

	@Override
	public void redefine()
	{
		// redefine point 3 of the simplex using so that it is a well-formed 
	  // triangle again
		if (redefineSimplex)
		{
			try
			{
			  bender.redefineSimplex();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			redefineSimplex = false;
		}
//		else if (!swapSnellsLaw && bender.optimizeSnellsLaw)
//		{
//			y[0] = snellsLawMisfit[0];
//			y[1] = snellsLawMisfit[1];
//			y[2] = snellsLawMisfit[2];
//			swapSnellsLaw = true;
//		}
	}

	public void redefineSimplexPoints()
	{
		redefineSimplex = true;
	}
//
//	public void setSnellsLawOnlyTolerance(double slotol)
//	{
//		//snellsLawOnlyTolerance = slotol;
//	}

	@Override
	public boolean isConverged(double tolerance)
	{
//		if (bender.optimizeSnellsLaw)
//		{
//			return super.isConverged(snellsLawOnlyTolerance);
//		}
//		else
		if (bender.isTravelTimeToleranceMinimum() && (super.isConverged(tolerance) ||
				isTravelTimeDifferenceMinimum(minTTDiff) ||
				isBadBouncePointSeparationMinimum(minBPMove / 2.0)))
//			  isBouncePointSeparationMinimum(minBPMove))
			return true;
		else
			return false;
	}

	public boolean isTravelTimeDifferenceMinimum(double minTTDiff)
	{
		if ((Math.abs(travelTime[1] - travelTime[0]) < minTTDiff) && 
				(Math.abs(travelTime[2] - travelTime[1]) < minTTDiff) &&
				(Math.abs(travelTime[0] - travelTime[2]) < minTTDiff))
			return true;
		else
			return false;
	}

	public boolean isBouncePointSeparationMinimum(double minBPMove)
	{
		if ((simplexPointDistance[0] < minBPMove) &&
				(simplexPointDistance[1] < minBPMove) &&
				(simplexPointDistance[2] < minBPMove))
			return true;
		else
			return false;
	}

	/**
	 * Returns true if the 3D distance between the two worst points (1 and 2) is
	 * less than minBPMove.
	 * 
	 * @param minBPMove The minimum bounce point movement in km.
	 * @return True if the 3D distance between the two worst points (1 and 2) is
	 *				 less than minBPMove.
	 */
	public boolean isBadBouncePointSeparationMinimum(double minBPMove)
	{
		if (simplexPointDistance[1] < minBPMove)
			return true;
		else
			return false;
	}

	/**
	 * Returns true if all travel time values for the simplex have been
	 * initialized;
	 * @return True if all travel time values for the simplex have been
	 * 				 initialized;
	 */
	public boolean isInitialized()
	{
		return ((travelTime[0] != Globals.NA_VALUE) &&
						(travelTime[1] != Globals.NA_VALUE) &&
						(travelTime[2] != Globals.NA_VALUE));
	}

	public double getMeanTravelTimeDifference()
	{
		 return (Math.abs(travelTime[0] - travelTime[1]) +
						 Math.abs(travelTime[1] - travelTime[2]) +
						 Math.abs(travelTime[2] - travelTime[0])) / 3.0;
	}

	public double getMeanSimplexPointSeparation()
	{
		 return (simplexPointDistance[0] + simplexPointDistance[1] +
				 		 simplexPointDistance[2]) / 3.0;
	}

	public double getMaximumSimplexPointSeparation()
	{
		 return Math.max(Math.max(simplexPointDistance[0],
				 											simplexPointDistance[1]),
				 						 simplexPointDistance[2]);
	}

	public double getMinimumSimplexPointSeparation()
	{
		 return Math.min(Math.min(simplexPointDistance[0],
				 											simplexPointDistance[1]),
				 						 simplexPointDistance[2]);
	}

	public double getMinMaxSimplexPointSeparationRatio()
	{
		double min = getMinimumSimplexPointSeparation();
		if (min != 0.0)
			return min / getMaximumSimplexPointSeparation();
		else
			return 1.0;
	}

	// when a simplex point is set/replaced the y (dtt) and p (lat, lon) of the
	// point is modified. At that time the traveltime[], snellsLawMisfit[], and
	// simplexPos[] must be set from the current equivalents.
	// 

	@Override
	protected void replace(int i, double[] pnew, double ynew) throws Exception
	{
		super.replace(i, pnew, ynew);
		if ((pnew[0] == currentLatLon[0]) && (pnew[1] == currentLatLon[1]))
		  setSimplexFromCurrentData(i);
		else
		  setSimplexFromPreviousData(i);
		setPoint3DDistance(i);
		setPoint3DDistance((i == 0) ? 2 : i-1);
	}

  protected void setSimplexFromCurrentData(int i)
  {
  	ttTol[i]             = currentTTTol;
  	simplexValidPoint[i] = currentValidResult;
    travelTime[i]        = currentTravelTime;
		snellsLawMisfit[i]   = currentSnellsLawMisfit;
		simplexPos[i]        = currentPosition;
  }

  protected void setSimplexFromPreviousData(int i)
  {
  	ttTol[i]             = previousTTTol;
  	simplexValidPoint[i] = previousValidResult;
    travelTime[i]        = previousTravelTime;
		snellsLawMisfit[i]   = previousSnellsLawMisfit;
		simplexPos[i]        = previousPosition;
  }

  private void setPoint3DDistance(int i)
  {
  	simplexPointDistance[i] = Vector3D.distance3D(simplexPos[i].get3DVector(),
  																								simplexPos[(i+1) % 3].get3DVector());
  }
  
	@Override
	protected void swap(int i, int j)
	{
		double x;

		super.swap(i, j);
		x = travelTime[i];
		travelTime[i] = travelTime[j];
		travelTime[j] = x;
		x = snellsLawMisfit[i];
		snellsLawMisfit[i] = snellsLawMisfit[j];
		snellsLawMisfit[j] = x;
		x = simplexPointDistance[i];
		simplexPointDistance[i] = simplexPointDistance[j];
		simplexPointDistance[j] = x;
		GeoTessPosition p = simplexPos[i];
		simplexPos[i] = simplexPos[j];
		simplexPos[j] = p;
		x = ttTol[i];
		ttTol[i] = ttTol[j];
		ttTol[j] = x;
		int b = simplexValidPoint[i];
		simplexValidPoint[i] = simplexValidPoint[j];
		simplexValidPoint[j] = b;
	}
}
