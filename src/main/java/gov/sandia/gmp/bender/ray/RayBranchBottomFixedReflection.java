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
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * A fixed reflection bottom branch. This branch is always constructed from an
 * upgoing and downgoing branch connected by a single TOP_SIDE_FIXED_REFLECTION
 * point. For example the phase PcP is a phase with a downgoing branch that
 * reflects off of the CMB and travels as an upgoing branch back to a receiver
 * or some other interface layer.
 * 
 * @author jrhipp
 *
 */
public class RayBranchBottomFixedReflection extends RayBranch
{
	/**
	 * The layer index of the TOP_SIDE_FIXED_REFLECTION.
	 */
	protected int reflectionLayerIndex = -1;

	/**
	 * The fixed bottom segment.
	 */
	protected RaySegmentFixedReflection bottomSegment = null;
	
	/**
	 * The bottom segment index defined by the owning ray.
	 */
	protected int        bottomSegmentIndex = -1;
	
	/**
	 * The bottom segment index defined by this branch.
	 */
  protected int        branchBottomIndex = -1;

  /**
   * The down going branch of this RayBranch.
   */
  protected RayBranchDownGoing downGoingBranch = null;
  
  /**
   * The up going branch of this RayBranch.
   */
  protected RayBranchUpGoing   upGoingBranch   = null;

  /**
   * Standard constructor.
   * 
	 * @param ray								The owning ray.
	 * @param firstPnt					The first point of the branch.
	 * @param lastPnt						The last point of the branch.
   * @param reflctnLayerIndex	The bottom reflection layer index.
	 * @param hdr               A header that is prepended to all debug output lines.
   * @throws Exception
   */
	public RayBranchBottomFixedReflection(Ray ray, GeoTessPosition firstPnt,
			GeoTessPosition lastPnt, int reflctnLayerIndex) throws Exception
	{
		super(ray, firstPnt, lastPnt, null);
    branchDirection  = RayDirection.TOP_SIDE_REFLECTION;
    isBottomBranch = true;
    buildInitialBranch(reflctnLayerIndex);
		owningRay.currentBottomLayer = reflctnLayerIndex;

    buildSnellsLawEvaluator();
	}

  private void buildSnellsLawEvaluator()
  {
    evaluateSnellsLaw = new EvaluateSnellsLaw(firstPoint, lastPoint,
																							owningRay.bender.getSearchMethod());
    if (downGoingBranch != null)
    	downGoingBranch.evaluateSnellsLaw = this.evaluateSnellsLaw;
    if (upGoingBranch != null)
    	upGoingBranch.evaluateSnellsLaw = this.evaluateSnellsLaw;
  }

  /**
   * Standard constructor.
   * 
	 * @param ray         The owning ray.
   * @param prevBranch  Input branch used to set the reflection boundary node
   *                    position.
	 * @param firstPnt    The first point of the branch.
	 * @param lastPnt     The last point of the branch.
	 * @param hdr         A header that is prepended to all debug output lines.
   * @throws Exception
   */
	public RayBranchBottomFixedReflection(Ray ray, 
			                                  RayBranchBottomFixedReflection prevBranch,
			                                  GeoTessPosition firstPnt,
			                                  GeoTessPosition lastPnt)
			   throws Exception
	{
		super(ray, firstPnt, lastPnt, null);
    branchDirection  = RayDirection.TOP_SIDE_REFLECTION;
    isBottomBranch = true;
    buildInitialBranch(prevBranch);
		owningRay.currentBottomLayer = reflectionLayerIndex;

    buildSnellsLawEvaluator();
	}

	/**
	 * Validates the first and last point layer indices relative to the reflection
	 * layer index and assigns the first segment index.
	 * 
	 * @param reflctnLayerIndex The reflection layer index.
	 * @throws GeoTessException
	 */
	private void initializeInitialBranch(int reflctnLayerIndex) throws GeoTessException
	{
		// make sure first and last point are above reflection layer index

		if (firstPoint.getIndex() - 1 < reflectionLayerIndex)
		{
			// throw error
		}

		if (lastPoint.getIndex() - 1 < reflectionLayerIndex)
		{
			// throw error
		}

		// assign the first segment index and the reflection layer index

		reflectionLayerIndex = reflctnLayerIndex;
	}

	/**
	 * Builds an initial branch with the reflected node position lying half way
	 * between the branches first and last points.
	 * 
	 * @param reflctnLayerIndex The reflected boundary layer index.
	 * @throws Exception
	 */
	private void buildInitialBranch(int reflctnLayerIndex) throws Exception
	{
    // Initialize the branch from the input reflection layer
		initializeInitialBranch(reflctnLayerIndex);

    // make down going last node position

		double[] uv = {0.0, 0.0, 0.0};
		GeoTessPosition downGoingLastNode = firstPoint;
		VectorUnit.rotatePlane(firstPoint.getVector(), lastPoint.getVector(), .5, uv);
		downGoingLastNode = Ray.createRayLayerNode(firstPoint, reflectionLayerIndex, uv);

		// make down going branch, bottom segment, and up going branch

		finalizeInitialBranch(downGoingLastNode);
	}

	/**
	 * Builds a new branch using the reflected node position of the input branch
	 * to construct the branches.
	 * 
	 * @param prevBranch The input branch used to set the reflected node position.
	 * @throws Exception
	 */
	private void buildInitialBranch(RayBranchBottomFixedReflection prevBranch) throws Exception
	{
    // Initialize the branch from the previous branch, make a clone of the
		// down-going last node, and finalize the initial branch

		initializeInitialBranch(prevBranch.reflectionLayerIndex);
		GeoTessPosition downGoingLastNode = prevBranch.bottomSegment.getFirst().deepClone();
		finalizeInitialBranch(downGoingLastNode);		
	}

	/**
	 * builds the upgoing and downgoing branches of this branch.
	 * 
	 * @param downGoingLastNode The downgoing last node.
	 * @throws Exception
	 */
	private void finalizeInitialBranch(GeoTessPosition downGoingLastNode) throws Exception
	{
		// make down going branch, bottom segment, and up going branch

		branchIndex = owningRay.rayBranches.size();
		downGoingBranch = new RayBranchDownGoing(owningRay, firstPoint, downGoingLastNode, this);
		//addBranchSegments(downGoingBranch.branchSegments);

		bottomSegment = (RaySegmentFixedReflection)
		                owningRay.createFixedReflectionSegment(RayDirection.TOP_SIDE_REFLECTION,
		                		                                   downGoingLastNode, this);
		addBranchSegment(bottomSegment);
		
		GeoTessPosition upGoingFirstNode = bottomSegment.getLast();
		upGoingBranch = new RayBranchUpGoing(owningRay, upGoingFirstNode, lastPoint, this);
		//addBranchSegments(upGoingBranch.branchSegments);

		// set previous segment of up going branch and next segment of down going
		// branch to bottom segment

		upGoingBranch.setPreviousDirectionChangeSegment(bottomSegment);
		downGoingBranch.setNextDirectionChangeSegment(bottomSegment);
	}

  /**
   * Returns the bottom segment.
   * 
   * @return The bottom segment.
   */
  public RaySegmentFixedReflection getBottomSegment()
  {
  	return bottomSegment;
  }

	/**
	 * Returns the topmost layer interface index of the branch.
	 */
	@Override
	public int getTopLayer()
	{
		int topLayerUpGoing   = upGoingBranch.getTopLayer();
		int topLayerDownGoing = downGoingBranch.getTopLayer();
		if (topLayerDownGoing > topLayerUpGoing)
			return topLayerDownGoing;
		else
			return topLayerUpGoing;
	}
//
//	@Override
//	protected void checkThinLayers(boolean rmvThinLayers,
//																 boolean addThickLayers) throws GeoTessException
//	{
//  	if (downGoingBranch != null)
//  		downGoingBranch.checkThinLayers(rmvThinLayers, addThickLayers);
//  	if (upGoingBranch != null)
//  		upGoingBranch.checkThinLayers(rmvThinLayers, addThickLayers);
//	}

	/**
	 * Returns the bottom-most layer interface index of the branch.
	 */
	@Override
	public int getBottomLayer()
	{
		return reflectionLayerIndex;
	}

  /**
   * Returns the depth (km) of the deepest point of the branch.
   */
	@Override
	public double getMaxRayBranchDepth()
	{
		return bottomSegment.getMiddleNode().getDepth();
	}

  /**
   * Returns the the deepest branch node.
   */
	@Override
	public GeoTessPosition getDeepestRayBranchNode()
	{
		return bottomSegment.getMiddleNode();
	}

  /**
   * Called by RayBranch.optimize() to enforce Snell's law at internal segment
   * boundaries.
   */
	@Override
	protected void enforceSnellsLaw(boolean evenInnerIteration) throws Exception
	{
    executeSnellsLaw(bottomSegment.getMiddleNode(), bottomSegment,
    								 RayDirection.REFLECTION, false);
		owningRay.setStatus(RayStatus.SNELL);
		if (evenInnerIteration)
		{
			upGoingBranch.enforceSnellsLaw(false);
			downGoingBranch.enforceSnellsLaw(true);
		}
		else
		{
			downGoingBranch.enforceSnellsLaw(true);
			upGoingBranch.enforceSnellsLaw(false);
		}
	}

  /**
   * Returns the RayType name (always Reflection).
   */
	@Override
	public String getRayTypeName()
	{
		return "Reflection";
	}

  /**
   * Returns the Branch Type name (BOTTOM (Fixed Reflection)).
   */
	@Override
	public String getRayBranchTypeName()
	{
		return "BOTTOM (Fixed Reflection)";
	}
}
