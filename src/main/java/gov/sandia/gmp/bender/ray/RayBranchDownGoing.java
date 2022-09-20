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

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;
import gov.sandia.gmp.bender.BenderConstants.RayStatus;

/**
 * A ray branch that begins with a SOURCE or a BOTTOM_SIDE_REFLECTION and
 * ends with the first point of a RaySegmentBottom, a RECEIVER, or a
 * TOP_SIDE_REFLECTION.
 * 
 * @author jrhipp
 *
 */
public class RayBranchDownGoing extends RayBranch
{
	/**
	 * Standard RayBranchDownGoing that has source, receiver, or Fixed Reflections
	 * as end points.
	 * 
	 * @param ray         The owning ray.
	 * @param firstPnt    The first point of the branch.
	 * @param lastPnt     The last point of the branch.
	 * @param hdr         A header that is prepended to all debug output lines.
	 * @throws Exception 
	 */
  public RayBranchDownGoing(Ray ray, GeoTessPosition firstPnt,
  		                      GeoTessPosition lastPnt) throws Exception
  {
    super(ray, firstPnt, lastPnt, null);
    branchDirection  = RayDirection.DOWNGOING;

    buildInitialBranch();

    owningRay.currentBottomLayer = getBottomLayer();
    evaluateSnellsLaw = new EvaluateSnellsLaw(firstPoint, lastPoint,
    																					owningRay.bender.getSearchMethod());
  }

  /**
   * Standard RayBranchDownGoing that is the first part of a RayBranchBottom.
   * The index is the branch index of the owning RayBranchBottom.
   * 
	 * @param ray         The owning ray.
	 * @param firstPnt    The first point of the branch.
	 * @param lastPnt     The last point of the branch.
	 * @param index       The branch index.
	 * @param hdr         A header that is prepended to all debug output lines.
   * @throws Exception 
   */
  public RayBranchDownGoing(Ray ray, GeoTessPosition firstPnt,
  		                      GeoTessPosition lastPnt,
  		                      RayBranch owningBranch) throws Exception
  {
  	super(ray, firstPnt, lastPnt, owningBranch);
  	branchDirection  = RayDirection.DOWNGOING;
    branchIndex = owningBranch.branchIndex;
    isBottomBranch = true;

    buildInitialBranch();
  }

  /**
   * Standard RayBranchDownGoing that is constructed from the first and last
   * nodes of the input RayBranchDownGoing. Only those input segments are
   * copied that lie above the lastPoint major layer index are copied. If the
   * input isBottomBranch flag is set then the branch index and flag are set
   * for the new branch accordingly
   * 
	 * @param ray         The owning ray.
	 * @param inputBranch A previous branch used to access layer pierce-points from
	 *                    which this branch will be constructed.
	 * @param firstPnt    The first point of the branch.
	 * @param lastPnt     The last point of the branch.
	 * @param hdr         A header that is prepended to all debug output lines.
   * @throws Exception 
   */
  public RayBranchDownGoing(Ray ray, RayBranch inputBranch,
                            GeoTessPosition firstPnt,
                            GeoTessPosition lastPnt) throws Exception
	{
	  super(ray, firstPnt, lastPnt, null);
	  branchDirection  = RayDirection.DOWNGOING;
 	  if (inputBranch.isBottomBranch)
 	  {
 	  	branchIndex    = inputBranch.branchIndex;
 	  	isBottomBranch = true;
 	  	owningBranch   = inputBranch;
 	  }

	  buildInitialBranch(inputBranch);

	  if (!isBottomBranch)
	  {
	  	owningRay.currentBottomLayer = getBottomLayer();
	    evaluateSnellsLaw = new EvaluateSnellsLaw(firstPoint, lastPoint,
	    	 																			 owningRay.bender.getSearchMethod());
	  }
	}

  /**
   * Called by RayBranch.optimize() to enforce Snell's law at internal segment
   * boundaries.
   */
  @Override
  protected void enforceSnellsLaw(boolean evenInnerIteration) throws Exception
  {
//		int currentSegmentIndex = -1;
		RaySegment currentSegment = null;
		GeoTessPosition node = null;

		if (evenInnerIteration)
		{
			// iterate backward through segments from last to one before the first
			// relax first node of segment
			currentSegment = lastActiveSegment;
			while (currentSegment != firstActiveSegment)
			{
				node = currentSegment.getNodes().getFirst();

				//boolean ignoreSnellsLaw = false;
				//if (getOwningBranch().prevDirctnChngSegment != null)
				//	ignoreSnellsLaw = owningRay.bender.isLayerBouncePointIgnoreSnellsLaw(node.getIndex());

				//debugOutput(currentSegment, node);
				executeSnellsLaw(node, currentSegment, RayDirection.DOWNGOING, false);
				owningRay.setStatus(RayStatus.SNELL);

				currentSegment = currentSegment.prevActiveSegment;
			}

//			boolean nxtIgnore = false;
//			for (currentSegmentIndex = getLastSegmentIndex();
//					 currentSegmentIndex > 0; --currentSegmentIndex)
//			{
//				nxtIgnore = getBranchSegments().get(currentSegmentIndex-1).ignoreSegment;
//				currentSegment = getBranchSegments().get(currentSegmentIndex);
//				if (!currentSegment.ignoreSegment && !nxtIgnore)
//				{
//					node = currentSegment.getNodes().getFirst();
//					debugOutput(currentSegment, node);
//					executeSnellsLaw(node, currentSegment, RayDirection.DOWNGOING);
//					owningRay.setStatus(RayStatus.SNELL);
//				}
//			}
		}
		else
		{
			// iterate forward through segments from second to the last
			// relax first node of segment
			currentSegment = firstActiveSegment.nextActiveSegment;
			while (currentSegment != lastActiveSegment.nextActiveSegment)
			{
				node = currentSegment.getNodes().getFirst();

				//boolean ignoreSnellsLaw = false;
				//if (getOwningBranch().prevDirctnChngSegment != null)
				//	ignoreSnellsLaw = owningRay.bender.isLayerBouncePointIgnoreSnellsLaw(node.getIndex());

				//debugOutput(currentSegment, node);
				executeSnellsLaw(node, currentSegment, RayDirection.DOWNGOING, false);
				owningRay.setStatus(RayStatus.SNELL);

				currentSegment = currentSegment.nextActiveSegment;
			}

//			boolean prvIgnore = false;
//			for (currentSegmentIndex = 1;
//					 currentSegmentIndex <= getLastSegmentIndex(); ++currentSegmentIndex)
//			{
//				prvIgnore = getBranchSegments().get(currentSegmentIndex-1).ignoreSegment;
//				currentSegment = getBranchSegments().get(currentSegmentIndex);
//				if (!currentSegment.ignoreSegment && !prvIgnore)
//				{
//					node = currentSegment.getNodes().getFirst();
//					debugOutput(currentSegment, node);
//					executeSnellsLaw(node, currentSegment, RayDirection.DOWNGOING);
//					owningRay.setStatus(RayStatus.SNELL);
//				}
//			}
		}
  }

	/**
	 * Returns the topmost layer interface index of the branch.
	 */
  @Override
  public int getTopLayer()
  {
  	return getFirstSegment().getFirst().getIndex();
  }

	/**
	 * Returns the bottom-most layer interface index of the branch.
	 */
  @Override
  public int getBottomLayer()
  {
  	return getLastSegment().getLast().getIndex();
  }

  /**
   * Returns the depth (km) of the deepest point of the branch.
   */
  @Override
  public double getMaxRayBranchDepth()
  {
  	return getLastSegment().getLast().getDepth();
  }

  /**
   * Returns the the deepest branch node.
   */
  @Override
  public GeoTessPosition getDeepestRayBranchNode()
  {
  	return getLastSegment().getLast();
  }


  /**
   * Returns true if the last node of the last segment (should be the deepest)
   * has a radius that is less than the next to the last node. Otherwise, false
   * is returned. 
   * 
   * @return True if the last node on this branch has a radius that is less
   *         than the next to the last node radius..
   */
  @Override
  public boolean isValidDepthPhase()
  {
  	if (getLastSegment().getLast().getRadius() <
  			getLastSegment().getNextToLast().getRadius())
  		return true;
  	else
  		return false;
  }

  /**
   * Builds an initial branch structure from the input branch using the input
   * branches pierce-points to construct the initial branch.
   * 
   * @param inputBranch The input branch from which this branch is constructed.
   * @throws GeoTessException
   */
  private void buildInitialBranch(RayBranch inputBranch) throws GeoTessException
  {
  	// loop over all requested segments and make new segments for this down-going
  	// branch. If the lastPoint index is larger than the segment to be copied
		// then exit the loop as any remaining segments will not be used.
 
  	GeoTessPosition previous = firstPoint;
  	RaySegment currentSegment = inputBranch.firstInitialSegment;
  	while (currentSegment != inputBranch.lastInitialSegment.nextActiveSegment)
  	{
  		if (currentSegment.getLast().getIndex() < lastPoint.getIndex()) break;
  		previous = buildInitialSegment(previous, currentSegment);
  		currentSegment = currentSegment.nextInitialSegment;
  	}

//  	for (int i = 0; i < inputBranch.getNSegments(); ++i)
//  	{
//  		RaySegment copySegment = inputBranch.getBranchSegments().get(i);
//  		if (copySegment.getLast().getIndex() < lastPoint.getIndex()) break;
//  		previous = buildInitialSegment(previous, copySegment);
//  	}
  }

  /**
   * Builds an initial branch structure using the point spacing from the the
   * first to the last point to construct the interface segments.
   * 
   * @throws GeoTessException
   */
  private void buildInitialBranch() throws GeoTessException
  {
		// define inverse radial delta between first and last point, set previous
  	// position to first point, and then loop over all layer interfaces and
		// create new segments

		//int inc = 1;
		//if (firstPoint.getRadius() < firstPoint.getRadiusTop()) inc = 0;
		double nf = 1.0 / (lastPoint.getRadius() - firstPoint.getRadius());
  	GeoTessPosition previous = firstPoint;
  	if (firstPoint.getIndex() == lastPoint.getIndex())
  		addNewBranchSegment(firstPoint, lastPoint);
  	else
			for (int i = firstPoint.getIndex() - 1; i >= lastPoint.getIndex(); i--)
				previous = buildInitialSegment(previous, nf, i);
  }

  /**
   * Returns the RayType name (always DownGoing).
   */
	@Override
	public String getRayTypeName()
	{
		return "DownGoing";
	}

  /**
   * Returns the Branch Type name (always DOWNGOING).
   */
	@Override
	public String getRayBranchTypeName()
	{
		return "DOWNGOING";
	}
}
