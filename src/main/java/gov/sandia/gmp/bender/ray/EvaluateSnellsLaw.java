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
package gov.sandia.gmp.bender.ray;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.bender.Bender;
import gov.sandia.gmp.bender.BenderConstants;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;
import gov.sandia.gmp.bender.BenderConstants.SearchMethod;
import gov.sandia.gmp.bender.BenderException;
import gov.sandia.gmp.bender.BenderException.ErrorCode;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.numerical.brents.Brents;
import gov.sandia.gmp.util.numerical.brents.BrentsFunction;
import gov.sandia.gmp.util.numerical.simplex.Simplex;
import gov.sandia.gmp.util.numerical.simplex.SimplexException;
import gov.sandia.gmp.util.numerical.simplex.SimplexFunction;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * Snells law enforcement class. Defines a single public method (SnellsLaw(...))
 * that performs the Snell's law calculation given a node and segment as input.
 * 
 * @author jrhipp
 *
 */
public class EvaluateSnellsLaw implements SimplexFunction, BrentsFunction
{
//	/**
//	 * Used as a thickness limit to avoid problems calculating Snell's law on
//	 * extremely thin layers. If a layer is thinner than this value (above or
//	 * below an interface in km) then Snell's law simply moves the interface node
//	 * to a position that is on a vector from nodeAbove to nodeBelow as it crosses
//	 * the interface. 
//	 * 
//	 */
//	private final static double minLayerThicknessUseAvgPosition = .5;
//	//private final static double minLayerThicknessUseAvgPosition = -1.0;
//
//	/**
//	 * Used as a thickness limit to avoid problems calculating Snell's law on
//	 * extremely thin layers. If a layer is thicker than this value (both above
//	 * and below an interface in km) then Snell's law is performed as normal and
//	 * returned. If it is thinner than this result, but thicker than the limit
//	 * minLayerThicknessUseAvgPosition described above, then it is moved to an
//	 * intermediate position between the Snell's law result and the average
//	 * position described for the minLayerThicknessUseAvgPosition limit.
//	 */
//	private final static double maxLayerThicknessUseSnellsLawPosition = 2.0;
//	//private final static double maxLayerThicknessUseSnellsLawPosition = -1.0;
	
	/**
	 * The current node whose movement will satisfy Snell's law at an interface.
	 */
	private GeoTessPosition node      = null;

	/**
	 * The node above the current node on a segment.
	 */
	private GeoTessPosition nodeAbove = null;
	
	/**
	 * The wave type (model velocity) of the layer above.
	 */
	private int             waveTypeIndexAbove = -1;

	/**
	 * The node below the current node on a segment.
	 */
	private GeoTessPosition nodeBelow = null;

	/**
	 * The wave type (model velocity) of the layer below.
	 */
	private int             waveTypeIndexBelow = -1;
	
	/**
	 * The layer index above the interface.
	 */
  private int             layerAbove = -1;

  /**
	 * The layer index below the interface.
	 */
  private int             majorLayerIndex = -1;
  
  /**
   * The direction of the ray (UPGOING, DOWNGOING, or REFLECTION).
   */
  private RayDirection    rayDir;
  
  /**
   * The first point on the owning branch.
   */
  private GeoTessPosition firstPoint;

  /**
   * The last point on the owning branch.
   */
  private GeoTessPosition lastPoint;
//
//  /**
//   * The first segment index of the owning branch.
//   */
//	private int										firstSegmentIndex	= -1;
//	
//  /**
//   * The last segment index of the owning branch.
//   */
//	private int										lastSegmentIndex	= -1;

	/**
	 * Simplex optimization.
	 */
	final private Simplex		simplex;

	/**
	 * Brents optimization.
	 */
	final private Brents		brents;

	/**
	 * Default Search method.
	 */
	private SearchMethod		requestedSearchMethod;
	
	/**
	 * Force Brents method if true.
	 */
  private boolean         autoSearchForceBrents = false;

	/**
	 * When evaluating Snell's Law, we have three points, previous is on one side
	 * of the interface, node is on the interface and next is on the other side of
	 * the interface. pnDist is the angular distance from previous to next.
	 * thickPrevious is the radial distance in km from previous to the interface,
	 * and thickNext is the radial distance in km from next to the interface.
	 */
	private double					searchRadius, thickAbove, thickBelow;

	/**
	 * vtp is a 3-element unit vector used to anchor the initial estimate of the
	 * point X that is where Snell's Law is honored. It is located PI/2 radians
	 * away from previous point on ray, measured in direction of next point on
	 * ray.
	 */
	private double[]				vtp															= new double[3];

	/**
	 * Set of vectors used to calculate Snell's law
	 */
	private double[] outOfPlane  = {0.0, 0.0, 0.0};
	private double[] normal      = {0.0, 0.0, 0.0};
	private double[] vectorAbove = {0.0, 0.0, 0.0};
	private double[] vectorBelow = {0.0, 0.0, 0.0};
  private double[] headWave    = {0.0, 0.0, 0.0};
  private double[] normalAbove = {0.0, 0.0, 0.0};
  
  /**
   * Set of scalars used calculate Snell's law
   */
  private double   fitness;
  private double   kink;
  private double   siniAbove;
  private double   siniBelow;
  private double   errSnell;
  private double   slowAbove;
  private double   slowBelow;

  /**
   * Dot product results used in calculating Snell's law
   */
  private double vectorAbove_dot_normal = -1.0;
  private double vectorBelow_dot_normal = -1.0;
  private double vectorAbove_dot_headWave = -1.0;
  private double vectorBelow_dot_headWave = -1.0;
  private double normalAbove_dot_normal = -1.0;

	/**
	 * Used in simplex algorithm. The x and y coordinates of the three corners of
	 * the simplex.
	 */
	private double[][]			simplex_p						= new double[3][2];

	/**
	 * Standard constructor.
	 * 
	 * @param frstPnt				The first point on the branch
	 * @param frstIndx      The segment index of the branch start defined by the
	 *                      owning ray.
	 * @param lastPnt       The last point on the branch.
	 * @param lastIndx      The segment index of the branch end defined by the
	 *                      owning ray.
	 * @param searchMethod  The default search method.
	 */
  public EvaluateSnellsLaw(GeoTessPosition frstPnt, GeoTessPosition lastPnt,
  		                     SearchMethod searchMethod)
  {
		simplex = new Simplex(this, 1e-6, 2000);
		brents = new Brents();
		brents.setFunction(this);

		firstPoint = frstPnt;
		lastPoint = lastPnt;
		requestedSearchMethod = searchMethod;
  }

  /**
   * Set the force brent's flag.
   * @param asfb The new setting of the force brent's flag.
   */
  protected void setAutoSearchForceBrents(boolean asfb)
  {
  	autoSearchForceBrents = asfb;
  }

  public double SnellsLawBouncePointFitness(RaySegmentFixedReflection currentSegment,
      																			RaySegment previousLastSegment,
      																			RaySegment nextFirstSegment)
      	 throws Exception
  {
  	// initialize node, rayDir, layerAbove, and majorLayerIndex. If currentSegment
  	// is thin return zero for fitness.

		if (!SnellsLawInitialize(currentSegment.getMiddleNode(),
														 currentSegment, RayDirection.REFLECTION))
			return 0.0;

		RaySegment segment = previousLastSegment;
		while (segment != null)
		{
			if (segment.majorLayerIndex <= layerAbove)
			{
				nodeAbove = segment.getNextToLast();
				waveTypeIndexAbove = segment.getWaveTypeIndex();
				break;
			}
      segment = segment.prevActiveSegment;
		}

		// if nodeAbove is null then just use previousLastSegment to find node above

		if (nodeAbove == null)
		{
			nodeAbove = previousLastSegment.getNextToLast();
			waveTypeIndexAbove = previousLastSegment.getWaveTypeIndex();
		}

//		for (int i = previousSegments.size() - 1; i >= 0; --i)
//			if ((previousSegments.get(i).majorLayerIndex <= layerAbove) &&
//					!previousSegments.get(i).ignoreSegment)
//			{
//				nodeAbove = previousSegments.get(i).getNextToLast();
//				waveTypeIndexAbove = previousSegments.get(i).getWaveTypeIndex();
//				break;
//			}

		segment = nextFirstSegment;
		while (segment != null)
		{
			if (segment.majorLayerIndex <= layerAbove)
			{
				nodeBelow = segment.getSecond();
				waveTypeIndexBelow = segment.getWaveTypeIndex();
				break;
			}
      segment = segment.nextActiveSegment;
		}

//		for (int i = 0; i < nextSegments.size(); ++i)
//			if ((nextSegments.get(i).majorLayerIndex <= layerAbove) &&
//					!nextSegments.get(i).ignoreSegment)
//			{
//				nodeBelow = nextSegments.get(i).getSecond();
//				waveTypeIndexBelow = nextSegments.get(i).getWaveTypeIndex();
//				break;
//			}

		 try
		 {
	  	 evaluateFitness(SearchMethod.BRENTS);			 
		 }
		 catch (BenderException bex)
		 {
			 if (bex.getErrorCode() != ErrorCode.BRENTS_OUT_OF_PLANE)
				 throw bex;
		 }
		 
		 return (1.0 - normalAbove_dot_normal) + abs(errSnell);
  }

  /**
   * Performs Snells law on a bottom side reflection bounce point. The previous
   * and next branch segments are provided so that the nodeAbove and nodeBelow
   * the bounce point can be determined along with their segments respective
   * wave type indices. On exit the bottoms side reflection will satisfy Snells
   * law.
   * 
   * @param currentSegment		The input BOTTOM_SIDE_REFLECTION segment.
   * @param previousSegments	The list of segments prior to the input segment.
   * @param nextSegments			The list of segments following the input segment.
   * 
   * @return									The Snells law fitness.
   * @throws Exception
   */
  public double SnellsLawBouncePoint(RaySegmentFixedReflection currentSegment,
  																	 RaySegment previousLastSegment,
  																	 RaySegment nextFirstSegment) throws Exception
  {
  	// initialize node, rayDir, layerAbove, and majorLayerIndex. If currentSegment
  	// is thin return zero for fitness.

		if (!SnellsLawInitialize(currentSegment.getMiddleNode(),
														 currentSegment, RayDirection.REFLECTION))
			return 0.0;

		// set node above and below and their wave speed indexes.

		RaySegment segment = previousLastSegment;
		while (segment != null)
		{
			if (segment.majorLayerIndex <= layerAbove)
			{
				nodeAbove = segment.getNextToLast();
				waveTypeIndexAbove = segment.getWaveTypeIndex();
				break;
			}
      segment = segment.prevActiveSegment;
		}

//		for (int i = previousSegments.size() - 1; i >= 0; --i)
//			if ((previousSegments.get(i).majorLayerIndex <= layerAbove) &&
//					!previousSegments.get(i).ignoreSegment)
//			{
//				nodeAbove = previousSegments.get(i).getNextToLast();
//				waveTypeIndexAbove = previousSegments.get(i).getWaveTypeIndex();
//				break;
//			}

		segment = nextFirstSegment;
		while (segment != null)
		{
			if (segment.majorLayerIndex <= layerAbove)
			{
				nodeBelow = segment.getSecond();
				waveTypeIndexBelow = segment.getWaveTypeIndex();
				break;
			}
      segment = segment.nextActiveSegment;
		}

//		for (int i = 0; i < nextSegments.size(); ++i)
//			if ((nextSegments.get(i).majorLayerIndex <= layerAbove) &&
//					!nextSegments.get(i).ignoreSegment)
//			{
//				nodeBelow = nextSegments.get(i).getSecond();
//				waveTypeIndexBelow = nextSegments.get(i).getWaveTypeIndex();
//				break;
//			}

		// perform search and return fitness

		return SnellsLawSearch(currentSegment);
  }
//
//  private double getSnellsLawThicknessUseFraction() throws GeoTessException
//  {
//  	double thcknsUseFrctn = -1.0;
//    if (node.getIndex() < node.getNLayers())
//    {
//      double thickAbove = node.getLayerThickness(node.getIndex() + 1);
//      if (thickAbove <= minLayerThicknessUseAvgPosition)
//      	thcknsUseFrctn = 0.0;
//      else if (thickAbove >= maxLayerThicknessUseSnellsLawPosition)
//      	thcknsUseFrctn = 1.0;
//      else
//      	thcknsUseFrctn = (thickAbove - minLayerThicknessUseAvgPosition) /
//      									 (maxLayerThicknessUseSnellsLawPosition - minLayerThicknessUseAvgPosition);
//    }
//    if (thcknsUseFrctn > 0.0)
//    {
//      double thickBelow = node.getLayerThickness(node.getIndex());
//      if (thickBelow <= minLayerThicknessUseAvgPosition)
//      	thcknsUseFrctn  = 0.0;
//      else if ((thcknsUseFrctn == 1.0) && (thickBelow < maxLayerThicknessUseSnellsLawPosition))
//      	thcknsUseFrctn = (thickBelow - minLayerThicknessUseAvgPosition) / (maxLayerThicknessUseSnellsLawPosition - minLayerThicknessUseAvgPosition);
//    }
//
//  	return thcknsUseFrctn;
//  }

  /**
   * The Snell's law method. The input currentNode is moved along the surface of
   * a model interface until Snell's law is satisfied. The node is an end point
   * of the input segment currentSegment.
   * 
   * @param currentNode			The node to be moved so that Snell's law is satisfied.
   * @param currentSegment	The current segment owning the current node.
   * @param rd							The ray direction (UPGOING, DOWNGOING, or REFLECTION).
   * @param ignoreSnellsLaw If true the point is simply moved to a position on
   *                        the line between the nodeAbove and nodeBelow and
   *                        moved back onto the interface.
   * @return The fitness of the SnellsLaw fit.
   * @throws Exception
   */
	public double SnellsLaw(GeoTessPosition currentNode, RaySegment currentSegment,
			                    RayDirection rd, boolean ignoreSnellsLaw) throws Exception
	{
		if (!SnellsLawInitialize(currentNode, currentSegment, rd)) return 0.0;

		//double thicknessUseFraction = getSnellsLawThicknessUseFraction();
		
		// now identify nodeAbove and nodeBelow.
		// nodeAbove is the node in layerAbove that is closest to node and
		// nodeBelow is the node in current layer that is closest to node.
		// Note that if there are thin layers above currentLayer then nodeAbove
		// may not be the next node along the ray path in direction of
		// nodeAbove.
		nodeAbove = nodeBelow = null;
		if (currentSegment.getRayDirectionChangeType() == RayDirection.TOP_SIDE_REFLECTION)
		{
			nodeAbove = currentSegment.prevActiveSegment.getNextToLast();
			waveTypeIndexAbove = currentSegment.prevActiveSegment.getWaveTypeIndex();
			nodeBelow = currentSegment.nextActiveSegment.getSecond();
			waveTypeIndexBelow = currentSegment.nextActiveSegment.getWaveTypeIndex();

//			nodeAbove = segments.get(currentSegment.getSegmentIndex() - 1).getNextToLast();
//			waveTypeIndexAbove = segments.get(currentSegment.getSegmentIndex() - 1).getWaveTypeIndex();
//			nodeBelow = segments.get(currentSegment.getSegmentIndex() + 1).getSecond();
//			waveTypeIndexBelow = segments.get(currentSegment.getSegmentIndex() + 1).getWaveTypeIndex();
		}
		else if (currentSegment.getRayDirectionChangeType() == RayDirection.BOTTOM_SIDE_REFLECTION)
		{
			nodeAbove = currentSegment.prevActiveSegment.getNextToLast();
			waveTypeIndexAbove = currentSegment.prevActiveSegment.getWaveTypeIndex();
			nodeBelow = currentSegment.nextActiveSegment.getSecond();
			waveTypeIndexBelow = currentSegment.nextActiveSegment.getWaveTypeIndex();

//			nodeAbove = segments.get(currentSegment.getSegmentIndex() - 1).getNextToLast();
//			waveTypeIndexAbove = segments.get(currentSegment.getSegmentIndex() - 1).getWaveTypeIndex();
//			nodeBelow = segments.get(currentSegment.getSegmentIndex() + 1).getSecond();
//			waveTypeIndexBelow = segments.get(currentSegment.getSegmentIndex() + 1).getWaveTypeIndex();
		}
		else if (currentSegment.isBottom())
		{
			// this is the bottom segment.
			// if first and last nodes in the bottom segment are really close
			// together, then this is a reflection

			if (currentSegment.isReflection())
			{
				rayDir = RayDirection.REFLECTION;
				node = currentSegment.getMiddleNode();
				int layerIndex = layerAbove;
				if (currentSegment.getRayDirectionChangeType() == RayDirection.BOTTOM_SIDE_REFLECTION)
					layerIndex = majorLayerIndex;

				// doesn't matter whether nodeAbove = previous or next.
				// decision is arbitrary for reflections
				RaySegment segment = currentSegment.prevActiveSegment;
				while (segment != null)
				{
					if (segment.getMajorLayerIndex() == layerIndex)
					{
						nodeAbove = segment.getNextToLast();
						waveTypeIndexAbove = segment.getWaveTypeIndex();

						break;
					}					
					segment = segment.prevActiveSegment;
				}

				segment = currentSegment.nextActiveSegment;
				while (segment != null)
				{
					if (segment.getMajorLayerIndex() == layerIndex)
					{
						nodeBelow = segment.getSecond();
						waveTypeIndexBelow = segment.getWaveTypeIndex();

						break;
					}					
					segment = segment.nextActiveSegment;
				}
				
//				for (int i = currentSegment.getSegmentIndex() - 1; i >= 0; --i)
//					if (segments.get(i).getMajorLayerIndex() == layerIndex)
//					{
//						nodeAbove = segments.get(i).getNextToLast();
//						waveTypeIndexAbove = segments.get(i).getWaveTypeIndex();
//
//						break;
//					}
//				for (int i = currentSegment.getSegmentIndex() + 1; i <= lastSegmentIndex; ++i)
//					if (segments.get(i).getMajorLayerIndex() == layerIndex)
//					{
//						nodeBelow = segments.get(i).getSecond();
//						waveTypeIndexBelow = segments.get(i).getWaveTypeIndex();
//
//						break;
//					}
				if (nodeBelow == null || nodeAbove == null)
				{
					// this is a reflection that is occurring right at either
					// the
					// source or the receiver. No need to honor snells law
					// because
					// is really a direct ray.
					return 0.;
				}
			}
			// this is the segment in which ray bottoms
			else if (rayDir == RayDirection.UPGOING)
			{
				nodeBelow = currentSegment.getNextToLast();
				waveTypeIndexBelow = currentSegment.getWaveTypeIndex();
				RaySegment segment = currentSegment.nextActiveSegment;
				while (segment != null)
				{
					if (segment.getMajorLayerIndex() == layerAbove)
					{
						nodeAbove = segment.getSecond();
						waveTypeIndexAbove = segment.getWaveTypeIndex();

						break;
					}
					segment = segment.nextActiveSegment;
				}
//				for (int i = currentSegment.getSegmentIndex() + 1; i <= lastSegmentIndex; ++i)
//					if (segments.get(i).getMajorLayerIndex() == layerAbove)
//					{
//						nodeAbove = segments.get(i).getSecond();
//						waveTypeIndexAbove = segments.get(i).getWaveTypeIndex();
//
//						break;
//					}
			}
			else
			// downgoing.
			{
				RaySegment segment = currentSegment.prevActiveSegment;
				while (segment != null)
				{
					if (segment.getMajorLayerIndex() == layerAbove)
					{
						nodeAbove = segment.getNextToLast();
						waveTypeIndexAbove = segment.getWaveTypeIndex();

						break;
					}
					segment = segment.prevActiveSegment;
				}
//				for (int i = currentSegment.getSegmentIndex() - 1; i >= 0; --i)
//					if (segments.get(i).getMajorLayerIndex() == layerAbove)
//					{
//						nodeAbove = segments.get(i).getNextToLast();
//						waveTypeIndexAbove = segments.get(i).getWaveTypeIndex();
//
//						break;
//					}
				nodeBelow = currentSegment.getSecond();
				waveTypeIndexBelow = currentSegment.getWaveTypeIndex();
			}
		} // end isBottom()
		else if (rayDir == RayDirection.UPGOING)
		{
			// this is not the segment in which ray bottoms
			nodeBelow = currentSegment.getNextToLast();
			waveTypeIndexBelow = currentSegment.getWaveTypeIndex();
			RaySegment segment = currentSegment.nextActiveSegment;
			while (segment != null)
			{
				if (segment.getMajorLayerIndex() == layerAbove)
				{
					nodeAbove = segment.getSecond();
					waveTypeIndexAbove = segment.getWaveTypeIndex();

					break;
				}
				segment = segment.nextActiveSegment;
			}

//			for (int i = currentSegment.getSegmentIndex() + 1; i <= lastSegmentIndex; ++i)
//				if (segments.get(i).getMajorLayerIndex() == layerAbove)
//				{
//					nodeAbove = segments.get(i).getSecond();
//					waveTypeIndexAbove = segments.get(i).getWaveTypeIndex();
//
//					break;
//				}
		}
		else
		{
			// !bottom and !upgoing, must = downgoing
			RaySegment segment = currentSegment.prevActiveSegment;
			while (segment != null)
			{
				if (segment.getMajorLayerIndex() == layerAbove)
				{
					nodeAbove = segment.getNextToLast();
					waveTypeIndexAbove = segment.getWaveTypeIndex();

					break;
				}
				segment = segment.prevActiveSegment;
			}

//			for (int i = currentSegment.getSegmentIndex() - 1; i >= 0; --i)
//				if (segments.get(i).getMajorLayerIndex() == layerAbove)
//				{
//					nodeAbove = segments.get(i).getNextToLast();
//					waveTypeIndexAbove = segments.get(i).getWaveTypeIndex();
//
//					break;
//				}
			nodeBelow = currentSegment.getSecond();
			waveTypeIndexBelow = currentSegment.getWaveTypeIndex();
		}

		if (nodeAbove == null)
		{
			if (rayDir == RayDirection.UPGOING)
			{
				nodeAbove = lastPoint;
				if (currentSegment.branch.owningBranch != null)
					waveTypeIndexAbove = currentSegment.branch.owningBranch.firstActiveSegment.getWaveTypeIndex();
				else
					waveTypeIndexAbove = currentSegment.branch.firstActiveSegment.getWaveTypeIndex();
//				waveTypeIndexAbove = segments.get(0).getWaveTypeIndex();
			}
			else
			{
				nodeAbove = firstPoint;
				if (currentSegment.branch.owningBranch != null)
					waveTypeIndexAbove = currentSegment.branch.owningBranch.lastActiveSegment.getWaveTypeIndex();
				else
					waveTypeIndexAbove = currentSegment.branch.lastActiveSegment.getWaveTypeIndex();
//				waveTypeIndexAbove = segments.get(lastSegmentIndex).getWaveTypeIndex();
			}
		}

		if (nodeBelow == null)
		{
			if (rayDir == RayDirection.UPGOING)
			{
				nodeBelow = firstPoint;
				if (currentSegment.branch.owningBranch != null)
					waveTypeIndexBelow = currentSegment.branch.owningBranch.lastActiveSegment.getWaveTypeIndex();
				else
					waveTypeIndexBelow = currentSegment.branch.lastActiveSegment.getWaveTypeIndex();
//				waveTypeIndexBelow = segments.get(lastSegmentIndex).getWaveTypeIndex();
			}
			else
			{
				nodeBelow = lastPoint;
				if (currentSegment.branch.owningBranch != null)
					waveTypeIndexBelow = currentSegment.branch.owningBranch.firstActiveSegment.getWaveTypeIndex();
				else
					waveTypeIndexBelow = currentSegment.branch.firstActiveSegment.getWaveTypeIndex();
//				waveTypeIndexBelow = segments.get(0).getWaveTypeIndex();
			}
		}

		// move the node

//		if (thicknessUseFraction < 1.0)
//		{
//			// if ignoreSnellsLaw flag is set then simply set the node on the
//			// interface between the nodeAbove and nodeBelow.
//	
//      fitness = 0.0;
//			double ra = nodeBelow.getRadiusTop(node.getIndex());
//			ra += nodeAbove.getRadiusTop(node.getIndex());
//			ra /= 2.0;
//			double a = (ra - nodeBelow.getRadius()) /
//								 (nodeAbove.getRadius() - nodeBelow.getRadius());
//	
//			// Reset the input node position to an intermediate location along
//			// the line between the nodeAbove and nodeBelow and move it to the
//			// interface. Return zero for fitness.
//			
//			if (thicknessUseFraction == 0.0)
//			{
//				node.setIntermediatePosition(nodeBelow, nodeAbove, a, node.getIndex());
//	      return fitness;
//			}
//			
//			GeoTessPosition nodeAvg = node.deepClone();
//			nodeAvg.setIntermediatePosition(nodeBelow, nodeAbove, a, node.getIndex());
//			
//			SnellsLawSearch(currentSegment);
//			GeoTessPosition nodeSnells = node.deepClone();
//
//			node.setIntermediatePosition(nodeAvg, nodeSnells, thicknessUseFraction, node.getIndex());
//      return fitness;
//		}


//		if (ignoreSnellsLaw)
//		{
//			// if ignoreSnellsLaw flag is set then simply set the node on the
//			// interface between the nodeAbove and nodeBelow.
//
//			double ra = nodeBelow.getRadiusTop(node.getIndex());
//			ra += nodeAbove.getRadiusTop(node.getIndex());
//			ra /= 2.0;
//			double a = (ra - nodeBelow.getRadius()) /
//								 (nodeAbove.getRadius() - nodeBelow.getRadius());
//
//			// Reset the input node position to an intermediate location along
//			// the line between the nodeAbove and nodeBelow and move it to the
//			// interface. Return zero for fitness.
//			node.setIntermediatePosition(nodeBelow, nodeAbove, a, node.getIndex());
//
//			return 0.0;
//		}
//		else
//
//		GeoTessPosition nodeBefore = node.deepClone();
//		SnellsLawSearch(currentSegment);
//		
//		double dotNode = Vector3D.dot(node.getVector(), nodeBefore.getVector());
//		double dotAbvBlw = Vector3D.dot(nodeAbove.getVector(), nodeBelow.getVector());
//		if (dotNode < dotAbvBlw)
//		{
//			double f = Math.acos(dotAbvBlw) / Math.acos(dotNode);
//			double[] z = {0.0, 0.0, 0.0};
//			Vector3D.rotatePlane(nodeBefore.getVector(), node.getVector(), f, z);
//			node.setTop(node.getIndex(), z);
//		}
//    return fitness;
		return SnellsLawSearch(currentSegment);
	}

	private boolean SnellsLawInitialize(GeoTessPosition currentNode,
																			RaySegment currentSegment,
																			RayDirection rd) throws GeoTessException, BenderException
	{
		node = currentNode;
    rayDir = rd;

		// layer interface upon which node resides.
		majorLayerIndex = node.getIndex();

		// if this is not the bottom segment, and current layer has zero
		// thickness, then the node on the interface just below this interface was
		// put in the right position prior to this call and node can just be set to
		// a copy of it. Also need to ensure that the layer is thin.
		if (!currentSegment.isBottom()
				&& node.getLayerThickness(majorLayerIndex) <= BenderConstants.MIN_LAYER_THICKNESS)
		{
			currentSegment.makeThin();
			return false;
		}

		// majorLayerIndex of the first finite thickness layer above node.
		layerAbove = node.nextLayer(majorLayerIndex,
				BenderConstants.MIN_LAYER_THICKNESS);

		if (layerAbove == node.getNLayers())
			// all the layers above the current node are very thin.
			// This happens when the top layer of the model is very thin and
			// node is positioned at the bottom of that layer. For example,
			// if the top layer is a very thin sedimentary layer that overlies the crust,
			// then we are trying to evaluate Snell's Law at the sediment-crust
			// interface. Note that the ray may extend considerably above the surface
			// of the mode, so we do have to get this right. Set layerAbove = to the
			// outermost layer of the model that has finite thickness.
			do
				--layerAbove;
			while (node.getLayerThickness(layerAbove) < BenderConstants.MIN_LAYER_THICKNESS);
		
		return true;
	}

	private RaySegment curSeg = null;
	private double SnellsLawSearch(RaySegment currentSegment) throws Exception
	{
    curSeg = currentSegment;

		if (layerAbove < nodeAbove.getInterfaceIndex())
			layerAbove = nodeAbove.getInterfaceIndex();

		// at this point, we have identified closest nodeAbove and closest
		// nodeBelow interface upon which node resides.

		// find the height of nodeAbove relative to the interface, measured at
		// nodeAbove, and the height of nodeBelow relative to the interface,
		// measured at nodeBelow.
		thickAbove = abs(nodeAbove.getRadius() - nodeAbove.getRadiusTop(majorLayerIndex));
		thickBelow = abs(nodeBelow.getRadius() - nodeBelow.getRadiusTop(majorLayerIndex));

		// find the straight-line distance from nodeAbove to nodeBelow, in km.
		//separation = nodeAbove.getDistance3D(nodeBelow);

		// if (snellDebug)
		// {
		//   System.out.printf("Distance3D nodeAbove-node     =%1.6f%n",
		//   nodeAbove.distance3D(node));
		//   System.out.printf("Distance3D node-nodeBelow     =%1.6f%n",
		//   node.distance3D(nodeBelow));
		//   System.out.printf("Distance3D nodeAbove-nodeBelow=%1.6f%n",
		//   nodeAbove.distance3D(nodeBelow));
		//   System.out.printf("nodeAbove=%1.6f, %1.6f, thick=%1.6f%nnodeBelow=%1.6f, %1.6f, thick=%1.6f%n",
		//   nodeAbove.distanceDegrees(receiver),
		//   nodeAbove.getDepth(),
		//   thickAbove,
		//   nodeBelow.distanceDegrees(receiver),
		//   nodeBelow.getDepth(),
		//   thickBelow);
		//
		//   System.out.print("");
		// }

		// if the layer below and the layer above are both very thin
		// just put the node at the midpoint, with radius on the appropriate
		// interface, and return.
		if (thickBelow < BenderConstants.MIN_LAYER_THICKNESS
				&& thickAbove < BenderConstants.MIN_LAYER_THICKNESS)
		{
			// if both previous and next are positioned very close to the
			// current interface, simplex will fail. Position node on the
			// interface half way in between previous and next and return.
			node.setIntermediatePosition(nodeAbove, nodeBelow, 0.5);
			node.setRadius(node.getRadiusTop(majorLayerIndex));
			return 0.0;
		}

		// if this is not a reflection and the layer below is very thin
		if (!rayDir.isReflection()
				&& thickBelow < BenderConstants.MIN_LAYER_THICKNESS)
		{
			// if node is very close to the nodeBelow, then set node to copy of
			// nodeBelow and return
			// X if (node.distance3D(nodeBelow) <= BenderConstants.RELECTION_DISTANCE)
			if (node.getDistance3D(nodeBelow) <= BenderConstants.RELECTION_DISTANCE)
			{
				node.copy(nodeBelow);
				node.setRadius(node.getRadiusTop(majorLayerIndex));
				return 0.0;
			}
		}

		// find the unit vector normal to the plane of the great circle
		// that contains nodeAbove and nodeBelow. (nodeAbove x nodeBelow)
		VectorUnit.crossNormal(nodeAbove.getVector(), nodeBelow.getVector(),
				outOfPlane);

		// find the vector triple product (nodeAbove x nodeBelow) x nodeAbove
		if (VectorUnit.crossNormal(outOfPlane, nodeAbove.getVector(), vtp) == 0.)
		{
			// if next and previous are at same lat, lon position, then simply
			// move previous north 90 degrees.
			if (!nodeAbove.move_north(PI / 2, vtp))
			{
				// if previous and next are both located on the north or
				// south pole, then set vtp at lat, lon = 0, 0.
				vtp[0] = 1.;
				vtp[1] = 0.;
				vtp[2] = 0.;
			}
		}

		// initialize the current search method to the one specified in Bender
		SearchMethod currentSearchMethod = requestedSearchMethod;

		// if thickAbove or thickBelow is small, then node cannot
		// move significantly out of plane. Ok to use Brents.
		if (thickAbove < 0.1 || thickBelow < 0.1)
			currentSearchMethod = SearchMethod.BRENTS;

		if (currentSearchMethod == SearchMethod.AUTO)
		{
			//if (activeLayer.getMajorLayerIndex() <= indexM410)
			if (autoSearchForceBrents)
				// if the current active layer is at or below the 660
				// discontinuity
				// then set the search method to Brents.
				currentSearchMethod = SearchMethod.BRENTS;
			else
			{
				// table relating angle in degrees and dot product.
				// 90 - 20 0.342020143325669
				// 90 - 19 0.325568154457157
				// 90 - 18 0.309016994374947
				// 90 - 17 0.292371704722737
				// 90 - 16 0.275637355816999
				// 90 - 15 0.258819045102521
				// 90 - 14 0.241921895599668
				// 90 - 13 0.224951054343865
				// 90 - 12 0.207911690817759
				// 90 - 11 0.190808995376545
				// 90 - 10 0.173648177666930
				// 90 - 9 0.156434465040231
				// 90 - 8 0.139173100960065
				// 90 - 7 0.121869343405147
				// 90 - 6 0.104528463267653
				// 90 - 5 0.087155742747658
				// 90 - 4 0.069756473744125
				// 90 - 3 0.052335956242944
				// 90 - 2 0.034899496702501
				// 90 - 1 0.017452406437284
				// 90 - 0 0.000000000000000

				// find a point halfway between nodeAbove and nodeBelow.
				// and get the unit vector normal to the interface at that
				// point. Find the angle between the normal to the interface and the
				// normal to the plane containing nodeAbove and nodeBelow (outOfPlane).
				// if the angle is near 90 degrees, then we can use Brents method
				// otherwise use Simplex.
				node.setIntermediatePosition(nodeAbove, nodeBelow, 0.5);
				node.getLayerNormal(majorLayerIndex, normal);
				if (Math.abs(VectorUnit.dot(normal, outOfPlane)) < 0.052335956242944)
					currentSearchMethod = SearchMethod.BRENTS;
				else
					currentSearchMethod = SearchMethod.SIMPLEX;
			}
		}

		if (currentSearchMethod == SearchMethod.BRENTS)
		{
			// try brents ... if it fails because of an OUT_OF_PLANE error or
			// out-of-limit root then do simplex
			try
			{
				double x = brents.zeroF(0., 1.);
				if ((x == 0.0) || (x == 1.0))
					simplexSearch();
				else
				{
					outputDebug = true;
				  bFunc(x);
					outputDebug = false;
					//simplexSearch();
				}
			}
			catch (BenderException bex)
			{
				if (bex.getErrorCode() == ErrorCode.BRENTS_OUT_OF_PLANE)
					simplexSearch();
				else
					throw bex;
			}
		}
		else if (currentSearchMethod == SearchMethod.SIMPLEX)
		{
			simplexSearch();
		}

		if (rayDir.isReflection())
		{
			// whether or not the ray interacts with this interface as a
			// reflection was determined early in this method by calling
			// RaySegement.checkReflection(). If we get here, then bottom segment
			// only has 3 nodes and current node is a reference to the middle node.
			// Set the other two nodes equal to copies of node. Using copy(node)
			// ensures that their references in the previous and next segments are
			// preserved. Only their contents are updated to the contents of node.
			currentSegment.getNodes().getFirst().copy(node);
			currentSegment.getNodes().getLast().copy(node);
		}
		return fitness;
	}

	/**
	 * Simplex Snells Law searcher. The initial amoeba is resized to successively
	 * larger sizes if simplex fails find the minimum.
	 * 
	 * @throws Exception
	 */
	private void simplexSearch() throws Exception
	{
		double distance = nodeAbove.distance(nodeBelow);

		boolean done = false;
		int ntries = 0, maxtries = 100;
		double dist = 0.4999;

		// find angular distance from previous to next.
		searchRadius = max(1e-4, distance / 2);

		while (!done && ++ntries <= maxtries)
		{
			if (ntries % 2 == 1)
			{
				simplex_p[0][1] = searchRadius * 0.0001;
				simplex_p[0][0] = 0;

				simplex_p[1][1] = searchRadius * dist;
				simplex_p[1][0] = -searchRadius * dist;

				simplex_p[2][1] = searchRadius * dist;
				simplex_p[2][0] = searchRadius * dist;
			}
			else
			{
				simplex_p[0][1] = searchRadius * 0.999;
				simplex_p[0][0] = 0;

				simplex_p[1][1] = searchRadius * (1 - dist);
				simplex_p[1][0] = -searchRadius * dist;

				simplex_p[2][1] = searchRadius * (1 - dist);
				simplex_p[2][0] = searchRadius * dist;

				dist *= 0.5;
			}

			// call the simplex algorithm. Moves 'node' around to minimize
			// fitness.
			try
			{
				simplex.reset();
				simplex.search(simplex_p);
				simplexFunction(simplex_p[0]);

				// simplex only requires that variation in fitness over the
				// 3 points of the simplex be less than tolerance. We also want the
				// fitness to be very small (not just the simplex). If fitness
				// is still substantial, try again. In other words, global mininum
				// will have very small fitness but local minima may have substantial
				// fitness.
				done = fitness < 10. * simplex.getTolerance();
				// System.out.println("Simplex Fitness= " + fitness);
			}
			catch (Exception ex)
			{
				if (ntries == maxtries) throw ex;
				done = false;
			}
		}
	}

	/**
	 * This is the method that is called by the Simplex to evaluate the fitness of
	 * a point on a major layer interface.
	 * 
	 * @param x
	 *          double[] a 2-element array that specifies the manner in which node
	 *          is to be moved relative to nodeAbove. First, nodeAbove is moved in
	 *          direction of vector triple product, vtp, distance x[1]. Then that
	 *          position is rotated about vtp by angle x[0]. Both elements of x
	 *          are in radians.
	 * @return double The fitness.
	 * @throws GeoModelException
	 * @throws GMPException
	 * @throws GeoModelException
	 * @throws GeoModelException
	 * @throws SimplexException
	 * @throws GMPException
	 * @throws GMPException
	 */
	public double simplexFunction(double[] x) throws SimplexException
	{
		try
		{
			node.move(nodeAbove, vtp, x[0], x[1], majorLayerIndex);

			return evaluateFitness(SearchMethod.SIMPLEX);
		}
		catch (Exception e)
		{
			throw new SimplexException(e);
		}
	}

	/**
	 * This is the method that is called by the Brents method to evaluate the
	 * fitness of a point on a major layer interface.
	 */
	@Override
	public double bFunc(double x) throws Exception
	{
		node.setIntermediatePosition(nodeAbove, nodeBelow, x, majorLayerIndex);

		return evaluateFitness(SearchMethod.BRENTS);
	}
	
	private boolean outputDebug = false;
  public void debugOutput()
  {
  	String s = String.format("    nodeAbove index, lon, depth, slow = %2d, %7.2f, %7.2f, %9.4f",
  			                     nodeAbove.getIndex(), nodeAbove.getLongitudeDegrees(),
  			                     nodeAbove.getDepth(), slowAbove);
		System.out.println (s);
  	       s = String.format("    nodeBelow index, lon, depth, slow = %2d, %7.2f, %7.2f, %9.4f",
  	      		 							 nodeBelow.getIndex(), nodeBelow.getLongitudeDegrees(),
  	      		 							 nodeBelow.getDepth(), slowBelow);
  	System.out.println (s);
  }

	/**
	 * Regardless of the optimization method (Brents or Simplex), this method
	 * evaluates the fitness of the current nodes Snell's law satisfaction. 
	 * 
	 * @param searchMethod The current search method which modifies the final
	 *                     fitness magnitude.
	 * @return The fitness.
	 * @throws BenderException
	 * @throws GeoTessException
	 */
	private double evaluateFitness(SearchMethod searchMethod)
			    throws BenderException, GeoTessException
	{
		// get a new unit vector normal to the discontinuity
		// at the point where ray intersects the discontinuity.
		// normal is outward pointing. In general, it will not lie in the plane
		// defined by nodes nodeAbove-nodeBelow-node, but for global tomography
		// problems it will not deviate by more than a few degrees.
		node.getLayerNormal(majorLayerIndex, normal);
		//evaluateAboveBelowSlowness(rayDir.isReflection());

		// set vectorAbove = a unit vector pointing from node to nodeAbove
		// and vectorBelow = a unit vector pointing from node to nodeBelow.
		// if either one is of length zero, it means that an interior node
		// in a layer lies on the discontinuity and is coincident with the
		// node that is being evaluated. This is highly undesirable. This can
		// happen in the limiting case of Brents (0.0 or 1.0) or Simplex (a
		// simplex point is on nodeAbove or nodeBelow). If it does
		// try to move node back toward the other node (above or below) by a
		// distance of 2.0 * zeroLengthVector. If that still produces a zero
		// length vector then throw an error.
		nodeAbove.minus(node, vectorAbove);
		if (VectorUnit.normalize(vectorAbove) < 1.0e-7)
			moveNodeOffAboveOrBelow(nodeAbove, nodeBelow, vectorAbove, "vectorAbove");

		nodeBelow.minus(node, vectorBelow);
		if (VectorUnit.normalize(vectorBelow) < 1.0e-7)
			moveNodeOffAboveOrBelow(nodeBelow, nodeAbove, vectorBelow, "vectorBelow");

		vectorAbove_dot_normal = VectorUnit.dot(vectorAbove, normal);
		vectorBelow_dot_normal = VectorUnit.dot(vectorBelow, normal);

		// Define plane AB as the plane that contains vectorAbove, and vectorBelow.
		// Find the vector normal to AB.  
		if (VectorUnit.crossNormal(vectorAbove, vectorBelow, outOfPlane) < 1.0e-6)
		{
			// vectorAbove and vectorBelow are colinear.  Try getting outOfPlane
			// from vectorAbove cross normal.
			if (VectorUnit.crossNormal(vectorAbove, normal, outOfPlane) < 1.0e-6)
			{
				// this ray passes straight through the interface in a direction that 
				// is colinear with the normal to the interface.  sin(i) is zero both
				// above and below the interface so Snell's Law is automatically 
				// satisfied.
				return 0.0;
			}			
		}

		// find the direction of the headWave. This unit vector 
		// (1) lies in plane AB and
		// (2) is tangent to the interface
		VectorUnit.crossNormal(outOfPlane, normal, headWave);

		if (rayDir == RayDirection.REFLECTION)
		{
			if ((vectorAbove_dot_normal <= 0.0) && (vectorBelow_dot_normal <= 0.0))
			{
				Vector3D.negate(outOfPlane);
				Vector3D.negate(headWave);
			}
			vectorAbove_dot_headWave = VectorUnit.dot(vectorAbove, headWave);
			vectorBelow_dot_headWave = VectorUnit.dot(vectorBelow, headWave);

			if (vectorAbove_dot_headWave > 0.0)
			{
				fitness = -2.0 * (1.0 + vectorAbove_dot_headWave);
				return searchMethod == SearchMethod.SIMPLEX ? -fitness : fitness;
			}
			else if (vectorBelow_dot_headWave < 0.0)
			{
				fitness = 2.0 * (1.0 - vectorBelow_dot_headWave);
				return fitness;
			}

	    // calculate slowness for reflections
			evaluateAboveBelowSlowness(true);
		}
		else
		{
			// we want headWave to point away from vectorAbove and toward vectorBelow.
			vectorAbove_dot_headWave = VectorUnit.dot(vectorAbove, headWave);
			vectorBelow_dot_headWave = VectorUnit.dot(vectorBelow, headWave);
	
			if (vectorAbove_dot_headWave >= 0.0 && vectorBelow_dot_headWave >= 0.0)
			{
				if (abs(vectorAbove_dot_normal) < abs(vectorBelow_dot_normal))
				{
					// headWave currently points away from vectorBelow and toward vectorAbove. Flip it.
					Vector3D.negate(outOfPlane);
					Vector3D.negate(headWave);
					vectorAbove_dot_headWave = -vectorAbove_dot_headWave;
					vectorBelow_dot_headWave = -vectorBelow_dot_headWave;
				}
			}
			else if (vectorAbove_dot_headWave >= 0.0)
			{
				// headWave currently points away from vectorBelow and toward vectorAbove. Flip it.
				Vector3D.negate(outOfPlane);
				Vector3D.negate(headWave);
				vectorAbove_dot_headWave = -vectorAbove_dot_headWave;
				vectorBelow_dot_headWave = -vectorBelow_dot_headWave;
			}

			// at this point, headwave points to the right and outOfPlane is into the AB
	    // plane. If vectorAbove is less than 90 deg away from the head wave or
			// vectorBelow is more than 90 deg away from the head wave then set the
			// fitness to a big value and return
			if (vectorAbove_dot_headWave > 0.0)
			{
				fitness = 2.0 * (1 + vectorAbove_dot_headWave);
	
				return searchMethod == SearchMethod.BRENTS ? -fitness : fitness;
				//return fitness;
			}
			else if (vectorBelow_dot_headWave < 0.0)
			{
				fitness = 2.0 * (1 - vectorBelow_dot_headWave);
	
				//return searchMethod == SearchMethod.BRENTS ? -fitness : fitness;
				return fitness;
			}

	    // calculate slowness for non-reflections
			evaluateAboveBelowSlowness(false);
		}

		// calculate the Snell's law misfit
		computeSnellsLawMisfit();

    //if (outputDebug) debugOutput();

		// Finally, calculate the normalAbove and it's dot product with the true
		// normal and evaluate fitness

		VectorUnit.crossNormal(headWave,  outOfPlane, normalAbove);
		normalAbove_dot_normal = VectorUnit.dot(normalAbove, normal);
		if (Double.isNaN(errSnell))
		{
			if (Double.isNaN(slowAbove))
			  throw new BenderException(ErrorCode.FATAL,
			  													"Error (Snell's Law): Slowness (Above) evaluation was NaN ... \n");
			else if (Double.isNaN(slowBelow))
			  throw new BenderException(ErrorCode.FATAL,
			  													"Error (Snell's Law): Slowness (Below) evaluation was NaN ... \n");
			else
			  throw new BenderException(ErrorCode.FATAL,
			  													"Error (Snell's Law): Snell's Law misfit evaluation was NaN ... \n");
		}
		switch (searchMethod)
		{
			case BRENTS:
				if (normalAbove_dot_normal < 1.0 - 5.0e-5)
					throw new BenderException(ErrorCode.BRENTS_OUT_OF_PLANE,
							String.format("BRENTS: assumption that interface normal lies in AB" +
					                  " plane is violated. %1.15e  %1.4f degrees%n",
									          1.0 - normalAbove_dot_normal,
									          toDegrees(Math.acos(normalAbove_dot_normal))));

				fitness = errSnell;
				break;
			case SIMPLEX:
				fitness = (1.0 - normalAbove_dot_normal) + abs(errSnell);
				break;
			default:
				throw new BenderException(ErrorCode.FATAL,
						String.format("searchMethod = %s but must be one of [ BRENTS | SIMPLEX ]%n",
								searchMethod.toString()));
		}
		return fitness;
	}

	private void computeSnellsLawMisfit()
	{
    // calculate the Snell's law misfit

		// compute sin(i) below the interface.
		double cosiSqr = vectorBelow_dot_normal * vectorBelow_dot_normal; // cosine i squared
		// cos^2(i) might be infintesimally > 1 due to rounding errors
		if (cosiSqr < 1.0)
			siniBelow = sqrt(1.0 - cosiSqr); // this is sin(i)
		else
			siniBelow = 0.0;

		// compute sin(i) above the interface.
		cosiSqr = vectorAbove_dot_normal * vectorAbove_dot_normal; // cosine i squared
		// cos^2(i) might be infintesimally > 1 due to rounding errors
		if (cosiSqr < 1.)
			siniAbove = sqrt(1.0 - cosiSqr); // this is sin(i)
		else
			siniAbove = 0.0;

		errSnell = siniAbove * slowAbove - siniBelow * slowBelow;
	}

	/**
	 * Attempts to fix the Snell node when it lies directly on nodeAbove or
	 * nodeBelow (node1 and node2 below represent Above and Below or Below and
	 * Above).
	 *  
	 * @param node1  nodeBelow or nodeAbove.
	 * @param node2  nodeAbove or nodeBelow.
	 * @param v1     The vector difference between node and node1.
	 * @param vdir   A string used in the error message ("vectorAbove" or
	 *               "vectorBelow").
	 * @throws GeoTessException
	 * @throws BenderException
	 */
	private void moveNodeOffAboveOrBelow(GeoTessPosition node1,
																			 GeoTessPosition node2, double[] v1,
																			 String vdir)
					throws GeoTessException, BenderException
	{
		// node1 is on or extremely near the interface and node has been
		// moved onto node1. This can happen in the limiting case of Brents
		// (0.0 or 1.0) or Simplex (a simplex point is on nodeBelow). If it does
		// try to move node back toward node2 by a distance of 2.0 *
		// zeroLengthVector.  If that produces a zero length vector then throw
		// an error.
		//
		// nD = node1 x node2 x node1 is a direction normal to
		// node1 in the direction of nodeAbove. Set a position equal to
		//   node = ||node1(3D) + 2.0 * zeroLengthVector * nD||
		// and place the node back on the layer boundary.
		//
		// If v1 is still nearly zero throw an error.
		
		double[] nD = {0.0, 0.0, 0.0};
		VectorUnit.vectorTripleProduct(node1.getVector(), node2.getVector(), nD);
		VectorUnit.normalize(nD);
		Vector3D.addMult(nD, 2.0 * 1.0e-7, nD, node1.get3DVector());
		VectorUnit.normalize(nD);
		node.setTop(majorLayerIndex, nD);
		nodeBelow.minus(node, v1);
		if (VectorUnit.normalize(v1) < 1.0e-7)			
		  throw new BenderException(ErrorCode.NONFATAL,
			  	String.format("Node on layer " + node.getIndex() +
	            " produces a zero length " + vdir + "...%n"));
	}

	/**
	 * Calculates the slowness above and below the current node.
	 * 
	 * @param isReflection True if the node is a reflection node.
	 * @throws GeoTessException
	 */
	private void evaluateAboveBelowSlowness(boolean isReflection)
			throws GeoTessException
	{
		// average slowness at node and nodeAbove
		// slowAbove = 0.5 * (nodeAbove.getValue(waveTypeIndex, layerAbove) + node
		// .getValue(waveTypeIndex, layerAbove,
		// (layerAbove > majorLayerIndex ? LayerSide.BOTTOM
		// : LayerSide.TOP)));

		double fracNode = 0.5;
		double fracAbvBlwNode = 1 - fracNode;
		slowAbove = nodeAbove.getValue(waveTypeIndexAbove, layerAbove);
		if (Double.isNaN(slowAbove))
		{
			if (layerAbove > majorLayerIndex)
				slowAbove = node.getValueBottom(waveTypeIndexAbove, layerAbove);
			else
				slowAbove = node.getValueTop(waveTypeIndexAbove, layerAbove);
		}
		else
		{
			if (layerAbove > majorLayerIndex)
				slowAbove = fracAbvBlwNode * slowAbove + fracNode *
										node.getValueBottom(waveTypeIndexAbove, layerAbove);
			else
				slowAbove = fracAbvBlwNode * slowAbove + fracNode *
										node.getValueTop(waveTypeIndexAbove, layerAbove);
		}

		// average slowness at node and nodeBelow
		if (isReflection)
		// slowBelow = 0.5 * (nodeBelow.getValue(waveTypeIndex, layerAbove) + node
		// .getValue(waveTypeIndex, layerAbove,
		// (layerAbove > majorLayerIndex ? LayerSide.BOTTOM
		// : LayerSide.TOP)));
		{
			if (layerAbove > majorLayerIndex)
				slowBelow = fracAbvBlwNode
						* nodeBelow.getValue(waveTypeIndexBelow, layerAbove) + fracNode
						* node.getValueBottom(waveTypeIndexBelow, layerAbove);
			else
				slowBelow = fracAbvBlwNode
						* nodeBelow.getValue(waveTypeIndexBelow, layerAbove) + fracNode
						* node.getValueTop(waveTypeIndexBelow, layerAbove);
		}
		else
			// X slowBelow = 0.5 * (nodeBelow.getValue(waveTypeIndex,
			// X majorLayerIndex) + node.getValue(waveTypeIndex,
			// X majorLayerIndex, LayerSide.TOP));
			slowBelow = fracAbvBlwNode
					* nodeBelow.getValue(waveTypeIndexBelow, majorLayerIndex) + fracNode
					* node.getValueTop(waveTypeIndexBelow, majorLayerIndex);
	}

	/**
	 * Debug output to Bender for Brent's method.
	 * 
	 * @param bender The owning Bender object.
	 */
	@SuppressWarnings("unused")
	private void fitnessTransect(Bender bender)
	{
		try
		{
			System.out.printf("Plotting fitness transect for Brent's Method.%n");
			// X System.out.printf("NodeAbove = %s%n", nodeAbove.getGeoVector());
			// X System.out.printf("nodeBelow = %s%n", nodeBelow.getGeoVector());
			System.out.printf("NodeAbove = %s%n", nodeAbove.getPositionString());
			System.out.printf("nodeBelow = %s%n", nodeBelow.getPositionString());
			File f = new File("t:\\Bender\\testing\\fitnessTransect.dat");
			bender
					.println("Writing fitness transect to file " + f.getCanonicalPath());

			BufferedWriter output = new BufferedWriter(new FileWriter(f, false));
			output
					.write("variables = \"X (deg)\" \"errSnell\" \"kink\" \"fitness\"\n");

			double distance = nodeAbove.distance(nodeBelow);

			int nx = 301;
			double dx = distance / (nx - 1);

			output.write(String.format("zone t=\"fitness\" i=%d%n", nx));
			for (int i = 0; i < nx; ++i)
			{
				bFunc(i * dx);
				output.write(String.format("%10.6f %g %g %g%n", toDegrees(i * dx),
						errSnell, kink, fitness));
			}
			output.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Debug output to Bender for Simplex method.
	 * 
	 * @param bender The owning Bender object.
	 */
	@SuppressWarnings("unused")
	private void fitnessMap(Bender bender)
	{
		try
		{
			File f = new File("testing/fitnessMap.dat");
			bender.println("Writing fitness map to file " + f.getCanonicalPath());

			BufferedWriter map = new BufferedWriter(new FileWriter(f, false));
			map.write("variables = \"X (deg)\" \"Y (deg)\" \"errSnell\" \"tripleProduct\" \"kink\" \"fitness\"\n");

			double distance = nodeAbove.distance(nodeBelow);

			double[] x = new double[2];
			double padding = 0.1 * distance;
			double xmin = -distance / 2 - padding;
			double xmax = distance / 2 + padding;
			int nx = 301;
			double dx = (xmax - xmin) / (nx - 1);

			double ymin = -padding;
			double ymax = distance + padding;
			int ny = nx;
			double dy = (ymax - ymin) / (ny - 1);

			map.write(String.format("zone t=\"previous, next\" i=2%n"));
			x[0] = 0;
			x[1] = 0;
			simplexFunction(x);
			map.write(String.format("%10.6f %10.6f %g %g %g %g%n", toDegrees(x[0]),
					toDegrees(x[1]), errSnell, vtp, kink, fitness));
			x[1] = distance;
			simplexFunction(x);
			map.write(String.format("%10.6f %10.6f %g %g %g %g%n", toDegrees(x[0]),
					toDegrees(x[1]), errSnell, vtp, kink, fitness));
			map.write(String.format("zone t=\"best node\" i=1%n"));
			simplexFunction(simplex_p[0]);
			map.write(String.format("%10.6f %10.6f %g %g %g %g%n",
					toDegrees(simplex_p[0][0]), toDegrees(simplex_p[0][1]), errSnell,
					vtp, kink, fitness));

			map.write(String.format("zone t=\"transect\"i=%1d%n", ny));
			x[0] = 0;
			for (int j = 0; j < ny; ++j)
			{
				x[1] = ymin + j * dy;
				simplexFunction(x);
				map.write(String.format("%10.6f %10.6f %g %g %g %g%n", toDegrees(x[0]),
						toDegrees(x[1]), errSnell, vtp, kink, fitness));
			}

			map.write(String.format("zone t=\"map\"i=%1d j=%1d%n", nx, ny));
			for (int i = 0; i < nx; ++i)
			{
				x[0] = xmin + i * dx;
				for (int j = 0; j < ny; ++j)
				{
					x[1] = ymin + j * dy;
					simplexFunction(x);
					map.write(String.format("%10.6f %10.6f %g %g %g %g%n",
							toDegrees(x[0]), toDegrees(x[1]), errSnell, vtp, kink,
							fitness));
				}
			}
			map.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				simplexFunction(simplex_p[0]);
			}
			catch (Exception ex1)
			{
				ex1.printStackTrace();
			}
		}
	}
}
