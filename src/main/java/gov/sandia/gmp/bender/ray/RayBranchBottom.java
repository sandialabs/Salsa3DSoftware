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

//import static gov.sandia.gmp.util.globals.Globals.NL;
import static java.lang.Math.min;

//import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.bender.BenderConstants;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;
import gov.sandia.gmp.bender.BenderConstants.RayStatus;
import gov.sandia.gmp.bender.BenderException;
import gov.sandia.gmp.bender.BenderException.ErrorCode;
import gov.sandia.gmp.bender.level.Level;
import gov.sandia.gmp.bender.phase.PhaseLayerLevelDefinition;
//import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * The RayBranchBottom specifies the normal RayBranch typically encountered where
 * the ray bottoms at some depth using a RaySegmentBottom. This branch is
 * essentially the old Bender Ray when Bender could only do sing branch rays.
 * 
 * @author jrhipp
 *
 */
public class RayBranchBottom extends RayBranch implements Comparable<Object>
{
	/**
	 * The branch PhaseLayerLevelDefinition used to acquire the Level structure
	 * required to build the RayBranch.
	 */
  protected PhaseLayerLevelDefinition phaseLayerDefn = null;

  protected RayBranchBottomLevels			rayBranchBottomLevels = null;
  protected int												rayBranchBottomLevelIndex = -1;

  protected void setRayBranchBottomLevels(RayBranchBottomLevels rbbl)
  {
  	rayBranchBottomLevels = rbbl;
  	for (int i = rbbl.rayBranchBottomLevelList.size() - 1; i >= 0; ++i)
  		if (this == rbbl.rayBranchBottomLevelList.get(i))
  		{
  			rayBranchBottomLevelIndex = i;
  			break;
  		}
  }

  protected boolean isFastestRayBranchBottomLevel()
  {
  	return rayBranchBottomLevels.isFastestRayBranchBottom(this);
  }

  /**
   * The 410 index used by enforceSnellsLaw object to utilize Brents method
   * instead of simplex when the Ray bottom is deeper than this interface.
   */
  final private int indexM410 = 3;

	/**
	 * The current maximum radius of the ray branch
	 * bottom node cannot be above this level.
	 */
	protected Level activeLayerLevel         = null;

	/**
	 * The current deepest point of the ray branch
	 * bottom node cannot be below this level.
	 */
	protected Level previousActiveLayerLevel = null;

	/**
	 * firstPointIsDeep means that the radius of the starting point is < the radius of the
	 * activeLayer measured at the starting point. Similar for lastPointIsDeep
	 */
	protected boolean	firstPointIsDeep, lastPointIsDeep;

	/**
	 * The current major layer in which the bottom segment resides.
	 */
	protected int majorLayerIndex;

	/**
	 * The top Level of the phase definition model to be tested for a local
	 * travel time minimum.
	 */
	protected int topLayerLevel     = -1;

	/**
	 * The bottom Level of the phase definition model to be tested for a local
	 * travel time minimum.
	 */
	protected int bottomLayerLevel  = -1;
	
	/**
	 * The current Level of the phase definition model undergoing testing for a
	 * local travel time minimum.
	 */
	protected int currentLayerLevel = -1;

	/**
	 * The bottom segment reference.
	 */
	protected RaySegmentBottom bottomSegment = null;
	
	/**
	 * The bottom segment index in the owning rays segment list.
	 */
	protected int        bottomSegmentIndex = -1;
	
	/**
	 * The branch bottom index as defined by the owning Ray.
	 */
  protected int        branchBottomIndex = -1;

  /**
   * A reference to the branches down going half if it is defined.
   */
  protected RayBranchDownGoing downGoingBranch = null;
  
  /**
   * A reference to the branches up going half if it is defined.
   */
  protected RayBranchUpGoing   upGoingBranch   = null;

  /**
   * Returns the Branches bottom segment.
   * 
   * @return The Branches bottom segment.
   */
  public RaySegmentBottom getBottomSegment()
  {
  	return bottomSegment;
  }

	/**
	 * The minimum number of nodes that should constitute the segment of the ray
	 * in which the ray bottoms.
	 */
	protected int	minNodesBottomSegment = 7;

  /**
   * Standard constructor.
   * 
   * @param ray            The ray that owns this branch.
   * @param firstPnt       The first point of the branch.
   * @param lastPnt        The last point of the branch.
   * @param levelStructure The PhaseLayerLevel definition used to build this
   *                       RayBranchBottom.
   * @param hdr            An output header string prepended to all debug output.
   */
  public RayBranchBottom(Ray ray, GeoTessPosition firstPnt,
  		                   GeoTessPosition lastPnt,
  		                   PhaseLayerLevelDefinition levelStructure)
  		   throws Exception
  {
    super(ray, firstPnt, lastPnt, null);
    branchDirection  = RayDirection.BOTTOM;
    isBottomBranch = true;
    buildInitialBranch(levelStructure);
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
   * @param ray          The ray that owns this branch.
   * @param prevBranch   Another previous branch that will be used to contruct
   *                     this branch.
   * @param firstPnt     The first point of the branch.
   * @param lastPnt      The last point of the branch.
   * @param hdr          An output header string prepended to all debug output.
   */
  public RayBranchBottom(Ray ray, RayBranchBottom prevBranch,
  		                   GeoTessPosition firstPnt, GeoTessPosition lastPnt) throws Exception
  {
     super(ray, firstPnt, lastPnt, null);
     branchDirection  = RayDirection.BOTTOM;
     isBottomBranch = true;
   	 currentLayerLevel = prevBranch.getCurrentLayerLevel();

     buildInitialBranch(prevBranch);
     buildSnellsLawEvaluator();
  }

  /**
   * Standard constructor.
   * 
   * @param ray          The ray that owns this branch.
   * @param prevBranch   Another previous branch that will be used to contruct
   *                     this branch.
   * @param firstPnt     The first point of the branch.
   * @param lastPnt      The last point of the branch.
   * @param currentLevel The current Level of the branch.
   * @param hdr          An output header string prepended to all debug output.
   * @throws Exception
   */
  public RayBranchBottom(Ray ray, RayBranchBottom prevBranch,
  		                   GeoTessPosition firstPnt, GeoTessPosition lastPnt,
  		                   int currentLevel) throws Exception
  {
     super(ray, firstPnt, lastPnt, null);
     branchDirection  = RayDirection.BOTTOM;
     isBottomBranch = true;
   	 currentLayerLevel = currentLevel;

   	 buildInitialBranch(prevBranch);
     buildSnellsLawEvaluator();
  }

  /**
   * Returns the phase layer level definition object.
   */
	public PhaseLayerLevelDefinition	getPhaseLayerLevelDefinition()
	{
  	return this.phaseLayerDefn;
	}

  /**
   * Set the branch bottom index.
   * 
   * @param index The new branch bottom index.
   */
  public void setBranchBottomIndex(int index)
  {
  	branchBottomIndex = index;
  }

  /**
   * Return the branch bottom index.
   * 
   * @return The branch bottom index.
   */
  public int getBranchBottomIndex()
  {
  	return branchBottomIndex;
  }

	/**
	 * Returns the number of level settings for this bottom branch.
	 * 
	 * @return The number of level settings for this bottom branch.
	 */
	public int getLayerLevelCount()
	{
		return topLayerLevel - bottomLayerLevel + 1;
	}

	/**
	 * Returns the top layer level.
	 * 
	 * @return The top layer level.
	 */
	public int getTopLayerLevel()
	{
		return topLayerLevel;
	}

	/**
	 * Returns the bottom layer level.
	 * 
	 * @return The bottom layer level.
	 */
	public int getBottomLayerLevel()
	{
		return bottomLayerLevel;
	}

	/**
	 * Returns the current layer level.
	 * 
	 * @return The current layer level.
	 */
	public int getCurrentLayerLevel()
	{
		return currentLayerLevel;
	}

	/**
	 * Returns the bottom layer index of the current active layer level.
	 */
  @Override
  public int getBottomLayer()
  {
  	return activeLayerLevel.getIndex();
  }

  /**
   * Returns the top layer containing an end point of this branch.
   */
  @Override
  public int getTopLayer()
  {
  	if (upGoingBranch != null)
  	{
  		int topLayerUpGoing = upGoingBranch.getTopLayer();
  		if (downGoingBranch != null)
  		{
  			int topLayerDownGoing = downGoingBranch.getTopLayer();
  			if (topLayerDownGoing > topLayerUpGoing)
  				return topLayerDownGoing;
  			else
  				return topLayerUpGoing;
  		}
  		else
			  return topLayerUpGoing;
  	}
  	else if (downGoingBranch != null)
  		return downGoingBranch.getTopLayer();
  	else
  	{
  		int topLayerFirst = bottomSegment.getFirst().getIndex();
  		int topLayerLast  = bottomSegment.getLast().getIndex();
  		if (topLayerFirst > topLayerLast)
  			return topLayerFirst;
  		else
  			return topLayerLast;
  	}
  }

  /**
   * Returns the depth (km) of the deepest branch node.
   */
  @Override
  public double getMaxRayBranchDepth()
  {
  	return bottomSegment.getTurningPoint().getDepth();
  }

  /**
   * Returns the deepest branch node.
   */
  @Override
  public GeoTessPosition getDeepestRayBranchNode()
  {
  	return bottomSegment.getTurningPoint();
  }

	public Level											getActiveLayerLevel()
	{
  	return activeLayerLevel;
	}

	public Level											getPreviousActiveLayerLevel()
	{
  	return previousActiveLayerLevel;
	}

  /**
   * Constructs a new initial ray using the pierce points of the input
   * previous RayBranchBottom. The current layer level setting is taken from
   * the input master level value. This constructor is used once a bottom
   * layer level initial ray has been constructed providing first guess
   * layer intersection pierce points.
   * 
   * @param prevBranch  A previously constructed RayBranchBottom whose pierce
   *                    points are used to construct the new initial
   *                    RayBranchBottom.
   * @param masterLevel The input master level from which the current layer
   *                    level will be set.
   * @throws Exception 
   */
  private void buildInitialBranch(RayBranchBottom prevBranch)
  		    throws Exception
  {
  	// set phase layer level definition, top level, bottom level, and master
  	// layer level multiplier from the previous RayBranchBottom defined by a
  	// previous ray where the branch bottom was in the same position in the
  	// previous ray as this branch bottom is in this ray. Set the masterLayer
  	// Level to the input value (note: This value may be any
  	// valid layer level setting for this branch bottom. It will produce a 
  	// a currentLayerLevel that is bounded between bottomLayerLevel and
  	// topLayerLevel inclusive).

  	//phaseLayerDefn = prevBranch.phaseLayerDefn;
  	phaseLayerDefn = prevBranch.getPhaseLayerLevelDefinition();
  	topLayerLevel = prevBranch.getTopLayerLevel();
  	bottomLayerLevel = prevBranch.getBottomLayerLevel();

  	// initialize bottom branch construction

    initializeBottomBranchConstruction();
		
		// if prevBranch.downGoingBranch is defined
		//   loop over each segment from first to majorLayerIndex

		GeoTessPosition bottomSegmentFirstNode = firstPoint;
		if (firstPoint.getIndex() - 1 >= majorLayerIndex)
		{
      // loop through previous branch segments from first to last until a
			// segments last node == majorlayerindex

			bottomSegmentFirstNode = prevBranch.findLastNodeAtMajorLayerIndex(majorLayerIndex).deepClone();
			branchIndex = owningRay.rayBranches.size();
			downGoingBranch = new RayBranchDownGoing(owningRay, firstPoint, bottomSegmentFirstNode, this);
			//addBranchSegments(downGoingBranch.branchSegments);
		}

    // now build the bottom segment last node

		GeoTessPosition bottomSegmentLastNode = lastPoint; 
		if (lastPoint.getIndex() - 1 >= majorLayerIndex)
		{
      // loop through previous branch segments from last to first until a
			// segments first node == majorlayerindex

			bottomSegmentLastNode = prevBranch.findFirstNodeAtMajorLayerIndex(majorLayerIndex).deepClone();
		}

		// build the bottom segment and the upgoing segment if required

		finalizeBottomBranchConstruction(bottomSegmentFirstNode, bottomSegmentLastNode);
  }

  /**
   * This method returns the node defined as the last node of a segment whose
   * layer index is mli. This method uses the internal segment list (raySegments)
   * as the prevBranch that calls this method may have had the equivalent
   * segments removed from the owning ray (owningRay.getSegment(i)).
   * 
   * @param mli
   * @return Return the last node of the segment whose layer index is mli.
   */
  public GeoTessPosition findLastNodeAtMajorLayerIndex(int mli)
  {
  	GeoTessPosition lastNode = null;
  	RaySegment segment = firstActiveSegment;
  	while (segment != lastActiveSegment.nextActiveSegment)
  	{
  		lastNode = segment.getLast();
  		if (lastNode.getIndex() == mli) return lastNode;
  		
  		segment = segment.nextActiveSegment;
  	}
//  	for (int i = 0; i <= branchSegments.size() - 1; ++i)
//  	{
//  		lastNode = branchSegments.get(i).getLast();
//  		if (lastNode.getIndex() == mli) return lastNode;
//  	}
  	
  	return null;
  }

  /**
   * This method returns the node defined as the first node of a segment whose
   * layer index is mli. This method uses the internal segment list (raySegments)
   * as the prevBranch that calls this method may have had the equivalent
   * segments removed from the owning ray (owningRay.getSegment(i)).
   * 
   * @param mli
   * @return Return the first node of the segment whose layer index is mli.
   */
  public GeoTessPosition findFirstNodeAtMajorLayerIndex(int mli)
  {
  	GeoTessPosition firstNode = null;
  	RaySegment segment = lastActiveSegment;
  	while (segment != firstActiveSegment.prevActiveSegment)
  	{
  		firstNode = segment.getFirst();
  		if (firstNode.getIndex() == mli) return firstNode;
  		
  		segment = segment.prevActiveSegment;  		
  	}
//  	
//  	for (int i = branchSegments.size() - 1; i >= 0; --i)
//  	{
//  		firstNode = branchSegments.get(i).getFirst();
//  		if (firstNode.getIndex() == mli) return firstNode;
//  	}

  	return null;
  }

  /**
   * Builds a RayBranchBottom from scratch using the input phase layer level
   * definition. The master layer level multiplier is set from the input
   * previous RayBranchBottom. If this is the first RayBranchBottom then the
   * previous entry is null.
   * 
   * @param phsLayrDefn The phase layer level definition from which the
   *                    RayBranchBottom's layer level hierarchy is created.
   * @param prevBottom  A previous RayBranchBottom for the ray under constructed.
   *                    This input is only used to assign the master layer level
   *                    multiplier and may be null if this is the first
   *                    RayBranchBottom for the ray.
   * @throws Exception 
   */
  private void buildInitialBranch(PhaseLayerLevelDefinition phsLayrDefn)
  		    throws Exception
  {
  	// first point and last point are defined on a major layer interface
  	// use old style construction to build down-going branch, bottom segment,
  	// and up-going branch. add segments to list as they are constructed. This
  	// is an initial construction.
  	
  	// Find top and bottom layer level from the inputPhaseLayerLevelDefinition.
  	// Set current layer level to deepest value (bottom) and the master layer
  	// level index to 0 (first level).

  	phaseLayerDefn = phsLayrDefn;
  	double distDeg = firstPoint.distanceDegrees(lastPoint);
		topLayerLevel = phaseLayerDefn.getTopLayer(distDeg);
		int prevTopLayer = phaseLayerDefn.getPreviousMajorLayerLevelIndex(topLayerLevel) + 1;
		bottomLayerLevel = min(prevTopLayer, firstPoint.getInterfaceIndex());
		bottomLayerLevel = min(phaseLayerDefn.getBottomLayer(distDeg), bottomLayerLevel);
		currentLayerLevel = bottomLayerLevel;

//		double df = firstPoint.getDepth();
//		double dl = lastPoint.getDepth();
    initializeBottomBranchConstruction();

		// make sure downgoing branch exists

		double[] uv = {0.0, 0.0, 0.0};
		GeoTessPosition bottomSegmentFirstNode = firstPoint;
		
		if (firstPoint.getIndex() - 1 >= majorLayerIndex)
		{
			// It exists ... calculate unitvector that is 10% between firstPoint and
			// lastPoint

			VectorUnit.rotatePlane(firstPoint.getVector(), lastPoint.getVector(), .01, uv);

			// create the down-going branch last node at the 10% unit vector position on
			// the major layer

			bottomSegmentFirstNode = Ray.createRayLayerNode(firstPoint, majorLayerIndex, uv);
			branchIndex = owningRay.rayBranches.size();
			downGoingBranch = new RayBranchDownGoing(owningRay, firstPoint, bottomSegmentFirstNode, this);
			//addBranchSegments(downGoingBranch.branchSegments);
		}

    // now build the bottom segment last node

		GeoTessPosition bottomSegmentLastNode = lastPoint; 
		if (lastPoint.getIndex() - 1 >= majorLayerIndex)
		{
			// It exists ... calculate unitvector that is 90% between firstPoint and
			// lastPoint (thats 10% between lastPoint and firstPoint using rotate)

			VectorUnit.rotatePlane(firstPoint.getVector(), lastPoint.getVector(), .99, uv);

			// create the down-going branch last node at the 10% unit vector position on
			// the major layer

			bottomSegmentLastNode = Ray.createRayLayerNode(firstPoint, majorLayerIndex, uv);
		}

		// build the bottom segment and the upgoing segment if required

		finalizeBottomBranchConstruction(bottomSegmentFirstNode, bottomSegmentLastNode);
  }

  /**
   * Starts construction of the branch. Active layer level (and previous active
   * layer level) is initialized, the first segment index and the major layer
   * indexes are assigned and the first and last point are checked for
   * validity.
   * 
   * @throws BenderException
   * @throws GeoTessException
   */
  private void initializeBottomBranchConstruction() throws BenderException, GeoTessException
  {
  	// set active layer and previous active layer given the current layer level

		activeLayerLevel = phaseLayerDefn.getInterface(currentLayerLevel);
		previousActiveLayerLevel = (phaseLayerDefn.getInterface(currentLayerLevel - 1) == null) ?
				                        Level.earthCenter : phaseLayerDefn.getInterface(currentLayerLevel - 1);

		// set the first and last point is deep flags

		firstPointIsDeep = firstPoint.getRadius() < activeLayerLevel.getRadius(firstPoint);
		lastPointIsDeep  = lastPoint.getRadius() < activeLayerLevel.getRadius(lastPoint);

		// assign the the major layer index

		majorLayerIndex = activeLayerLevel.getMajorLayerIndex();

		// Move the source and/or receiver up a bit if they are nearly on an interface
		// boundary of the current active layer level.

		if (firstPoint == owningRay.getSource())
		{
			GeoTessPosition newFirstPoint = adjustEndPointRadii(firstPoint, "First");
			if (newFirstPoint != null) firstPoint = newFirstPoint;
		}

		if (lastPoint == owningRay.getReceiver())
		{
			GeoTessPosition newLastPoint = adjustEndPointRadii(lastPoint, "Last");
			if (newLastPoint != null) lastPoint = newLastPoint;
		}

		// Validate the input first and last points for this bottom branch. The
	  // points must not be above the model (index == number of model layers) and
	  // their radii must be greater than the radius of the previous active layer.

		validateEndPoint(firstPoint, "First");
		validateEndPoint(lastPoint, "Last");
  }

  /**
   * Ensure that the input end point is above the active layer interface if the
   * layer is a very thin layer.
   * 
   * @param endPoint The input end point.
   * @param endID    A descriptor for the end point.
   * @return
   * @throws GeoTessException
   */
  private GeoTessPosition adjustEndPointRadii(GeoTessPosition endPoint, String endID)
  		    throws GeoTessException
  {
  	GeoTessPosition newEndPoint = null;
		double topThick = endPoint.getRadius() - activeLayerLevel.getRadius(endPoint);
		if (activeLayerLevel.isMajorInterface() && topThick > 0
				&& topThick <= BenderConstants.MIN_LAYER_THICKNESS)
		{
			// end point is just barely above, a major layer
			// discontinuity ... move the point up a bit

			double delta = 2.0 * BenderConstants.MIN_LAYER_THICKNESS + 1e-6;
			double lastR = activeLayerLevel.getRadius(endPoint);
			newEndPoint = endPoint.deepClone();
			if (endID.equals("First"))
			{
				lastR -= delta;
				newEndPoint.setIndex(newEndPoint.getIndex() - 1);
			  newEndPoint.setRadius(newEndPoint.getIndex(), lastR);
			}
			else
			{
				lastR += delta;
			  newEndPoint.setRadius(newEndPoint.getIndex(), lastR);
			}
	  }

  	return newEndPoint;
  }

  /**
   * Ensures that the provided end point is valid. Point must be less than the
   * model surface and not deeper than the major layer within which the bottom
   * segment is defined.
   * 
   * @param endPoint The end point to be checked for validity.
   * @param endID    The end point id descriptor.
   * @throws GeoTessException
   * @throws BenderException
   */
  private void validateEndPoint(GeoTessPosition endPoint, String endID)
  		         throws GeoTessException, BenderException
  {
		endPoint.setIndex(endPoint.getInterfaceIndex());

		if (endPoint.getIndex() == endPoint.getNLayers())
			throw new BenderException(ErrorCode.NONFATAL,
					endID + " Point of Bottom Branch is shallower than surface of model.");

		if (endPoint.getRadius() < previousActiveLayerLevel.getRadius(endPoint))
			throw new BenderException(
					ErrorCode.NONFATAL, endID +
					" Point of Bottom Branch is deeper than requested bottom layer."
							+ String.format("  " + endID + 
									" Point in layer %1s, bottom interface=%1s%n",
									phaseLayerDefn.getInterface(endPoint).getName(),
									previousActiveLayerLevel.getName()));
  }
  
  /**
   * Finishes bottom branch construction using the two input and nodes as the
   * bottom segment first and last nodes. This method builds the bottom segment,
   * builds the up-going branch if defined and finishes initializing the up-going
   * and down-going branches (if defined). Finally, the last segment index is
   * assigned/
   * 
   * @param bottomSegmentFirstNode The bottom segment first node.
   * @param bottomSegmentLastNode  The bottom segment last node.
   * @throws Exception 
   */
  private void finalizeBottomBranchConstruction(GeoTessPosition bottomSegmentFirstNode,
  		                                          GeoTessPosition bottomSegmentLastNode)
  		    throws Exception
  {
  	// build the bottom segment and the upgoing segment if required

		buildBottomSegment(bottomSegmentFirstNode, bottomSegmentLastNode);
		if (bottomSegmentLastNode != lastPoint)
		{
			branchIndex = owningRay.rayBranches.size();
			upGoingBranch = new RayBranchUpGoing(owningRay, bottomSegmentLastNode, lastPoint, this);
			upGoingBranch.setPreviousDirectionChangeSegment(bottomSegment);
			//addBranchSegments(upGoingBranch.branchSegments);
		}

		// assign the bottom segment as the next direction change segment to the
		// down going branch if it was defined.
		if (bottomSegmentFirstNode != firstPoint)
			downGoingBranch.setNextDirectionChangeSegment(bottomSegment);
//
//		// set the last segment index and save segements ... exit
//
//		lastSegmentIndex = owningRay.getNSegments() - 1;
//		raySegments = new ArrayList<RaySegment>(lastSegmentIndex - firstSegmentIndex + 1);
//		for (int i = firstSegmentIndex; i <= lastSegmentIndex; ++i)
//			raySegments.add(owningRay.getSegment(i));
  }

  /**
   * The method builds the bottom segment given its first and last node.
   * 
   * @param first Bottom segment first node.
   * @param last  Bottom segment last node.
   * @throws GeoTessException
   */
  private void buildBottomSegment(GeoTessPosition first, GeoTessPosition last) throws GeoTessException
  {
		// look for the next interface in the wave type mapper. If found update
  	// the wave type attribute index otherwise return the current wave type
  	// attribute index.
  	int waveTypeAttributeIndex = owningRay.waveTypeMapper.getCurrentWaveSpeedAttributeIndex();
  	//if (owningRay.getNBranches() > 0)
  	  waveTypeAttributeIndex = owningRay.waveTypeMapper.updateNextWaveSpeedIndex(majorLayerIndex);

		// put the bottom point half way in between the first and last node.

  	GeoTessPosition node;
		node = GeoTessPosition.getGeoTessPosition(first);
		node.setIntermediateUnitVectorPosition(first, last, 0.5);

		// set the bottom radius just above the lower level boundary by 1 km

		node.setRadius(previousActiveLayerLevel.getRadius(node) + 1);
		node.setIndex(majorLayerIndex);
		owningRay.currentBottomLayer = majorLayerIndex;

		// get nodes between first and bottom and bottom and last (in nodesupgoing)

		int nodesPerSide = (minNodesBottomSegment - 1) / 2 - 1;
		LinkedList<GeoTessPosition> nodes = addInitialSegmentNodes(first, node, nodesPerSide);
		LinkedList<GeoTessPosition> nodesupgoing = addInitialSegmentNodes(node, last, nodesPerSide);

		// add all but the first of "nodesupgoing" to nodes (this is the up-going
		// part of the bottom segment ... the first node is the last node of nodes
		// and we don't want to add it twice)

		Iterator<GeoTessPosition> it = nodesupgoing.iterator();
    it.next();
		while (it.hasNext()) nodes.add(it.next());

		// this is the bottom segment. It is the only one that initially
		// has at least 5 nodes (more may be added if the bottom segment is
		// in a thick layer where the static method
		//   getInitialRayLayerNodeSeparation(majorLayerIndex);
		// returns a separation distance that is 1/2 or less of the layer thickness)

		bottomSegment = new RaySegmentBottom(owningRay, this, nodes,
				                                 waveTypeAttributeIndex,
				                                 lastInitialSegment);
		addBranchSegment(bottomSegment);
  }

  /**
   * Called by the owning ray to set the fixed ray segment at the first point
   * of this branch.
   */
  @Override
  public void setPreviousDirectionChangeSegment(RaySegment prevSegment)
  {
  	// Last node of prevSegment must = firstPoint

  	for (int i = 0; i < rayBranchBottomLevels.rayBranchBottomLevelList.size(); ++i)
  	{
  		RayBranchBottom rbb = rayBranchBottomLevels.rayBranchBottomLevelList.get(i);
  		rbb.prevDirctnChngSegment = prevSegment;
  		if (rbb.downGoingBranch != null)
  			rbb.downGoingBranch.setPreviousDirectionChangeSegment(prevSegment);
  	}
  	//prevDirctnChngSegment = prevSegment;
  	//if (downGoingBranch != null) downGoingBranch.setPreviousDirectionChangeSegment(prevSegment);
  }

  /**
   * Called by the owning ray to set the fixed ray segment at the last point
   * of this branch.
   */
  @Override
  public void setNextDirectionChangeSegment(RaySegment nextSegment)
  {
  	// First node of nextSegment must = lastPoint

  	for (int i = 0; i < rayBranchBottomLevels.rayBranchBottomLevelList.size(); ++i)
  	{
  		RayBranchBottom rbb = rayBranchBottomLevels.rayBranchBottomLevelList.get(i);
  		rbb.nextDirctnChngSegment = nextSegment;
  		if (rbb.upGoingBranch != null)
  			rbb.upGoingBranch.setNextDirectionChangeSegment(nextSegment);
  	}
  	//nextDirctnChngSegment = nextSegment;
  	//if (upGoingBranch != null) upGoingBranch.setNextDirectionChangeSegment(nextSegment);
  }

  /**
   * Called by the owning Ray to set the branch index.
   */
  @Override
  public void setBranchIndex(int index)
  {
  	branchIndex = index;
  	if (upGoingBranch != null) upGoingBranch.setBranchIndex(index);
  	if (downGoingBranch != null) downGoingBranch.setBranchIndex(index);
  }

  /**
   * Called by RayBranch.optimize() to enforce Snell's law at internal segment
   * boundaries.
   */
  @Override
  public void enforceSnellsLaw(boolean evenInnerIteration) throws Exception
  {
		bottomSegment.checkReflection();

		// Exit if the only segment is the bottom segment (no Snell's law ...
		// only bending).

		if (activeSegmentCount == 1) return;
		evaluateSnellsLaw.setAutoSearchForceBrents(activeLayerLevel.getMajorLayerIndex() <= indexM410);
		if (evenInnerIteration)
		{
			// iterate forward through segments from bottom to next to last
			// if up going branch is null then the bottomSegment last node is the
			// receiver or a Bottom Side Reflection, both of which are fixed, so move
			// on to the down going branch.

			if (upGoingBranch != null) 
			{
				//debugOutput(bottomSegment, bottomSegment.getLast());
				executeSnellsLaw(bottomSegment.getLast(), bottomSegment,
												 RayDirection.UPGOING, false);
				owningRay.setStatus(RayStatus.SNELL);
				upGoingBranch.enforceSnellsLaw(false);
			}

			// iterate backward through segments from bottom to one before the first
			// if down going branch is null then the bottomSegment first node is the
			// source or a Bottom Side Reflection, both of which are fixed, so we are
			// finished.
			
			if (downGoingBranch != null)
			{
				//debugOutput(bottomSegment, bottomSegment.getFirst());
				executeSnellsLaw(bottomSegment.getFirst(), bottomSegment,
												 RayDirection.DOWNGOING, false);
				owningRay.setStatus(RayStatus.SNELL);
				downGoingBranch.enforceSnellsLaw(true);
			}
		}
		else
		{
			// iterate backward through segments from bottom to one before the first
			// if down going branch is null then the bottomSegment first node is the
			// source or a Bottom Side Reflection, both of which are fixed, so move
			// on to the up going branch.
			
			if (downGoingBranch != null)
			{
				//debugOutput(bottomSegment, bottomSegment.getFirst());
				executeSnellsLaw(bottomSegment.getFirst(), bottomSegment,
												 RayDirection.DOWNGOING, false);
				owningRay.setStatus(RayStatus.SNELL);
				downGoingBranch.enforceSnellsLaw(true);
			}
			// iterate forward through segments from bottom to next to last
			// if up going branch is null then the bottomSegment last node is the
			// receiver or a Bottom Side Reflection, both of which are fixed, so move
			// on to the down going branch.

			if (upGoingBranch != null) 
			{
				//debugOutput(bottomSegment, bottomSegment.getLast());
				executeSnellsLaw(bottomSegment.getLast(), bottomSegment,
												 RayDirection.UPGOING, false);
				owningRay.setStatus(RayStatus.SNELL);
				upGoingBranch.enforceSnellsLaw(false);
			}
		}
  }

  /**
   * Returns true if the bottom segment middle node is the only node of the
   * entire branch that is bounded within the active layer.
   * 
   * @return True if the bottom segment middle node is the only node of the
   *         entire branch that is bounded within the active layer.
   * @throws GeoTessException
   */
  @Override
  protected boolean isMiddleOnlyActiveNode() throws GeoTessException
  {
  	return bottomSegment.isMiddleOnlyActiveNode();
  }

	/**
	 * Forces the evaluation of the ray type. Called in RayBranch.optimize().
	 */
	@Override
	public void evaluateRayType() throws GeoTessException
	{
		bottomSegment.getRayType();
	}

	/**
	 * Returns the ray type as defined by the bottom segment ray type.
	 */
	@Override
	public String getRayTypeName()
	{
		return bottomSegment.rayType.name();
	}

	public RayType getRayType()
	{
		return bottomSegment.rayType;
	}

	/**
	 * Returns the branch type as BOTTOM.
	 */
	@Override
	public String getRayBranchTypeName()
	{
		return "BOTTOM";
	}

	/**
	 * Returns true if the bottom segment ray type is an ERROR or INVALID.
	 * 
	 * @return True if the bottom segment ray type is an ERROR or INVALID.
	 */
	public boolean isInvalid()
	{
		return (bottomSegment.rayType == RayType.ERROR) ||
           (bottomSegment.rayType == RayType.INVALID);
	}

	@Override
	protected boolean isRayTypeInvalid()
	{
		return (bottomSegment.rayType == RayType.INVALID) ? true : false;
	}
	
	public boolean										isFirstPointDeep()
	{
		return firstPointIsDeep;
	}

	public boolean										isLastPointDeep()
	{
		return lastPointIsDeep;
	}
	
	@Override
	public boolean isValidRayBottomPhase()
	{
  	if (getFirstSegment() == bottomSegment)
  	  if (bottomSegment.getFirst().getRadius() <
  	  		bottomSegment.getSecond().getRadius())
  	  	return false;
  	
  	return true;
	}

	/**
	 * Comparison of one branch travel time to another for purposes of sorting
	 * the ray branches on travel time.
	 */
	@Override
	public int compareTo(Object object)
	{
		try
		{
			if (getTravelTime() < ((RayBranchBottom) object).getTravelTime())
				return -1;
			if (getTravelTime() > ((RayBranchBottom) object).getTravelTime())
				return 1;
		}
		catch (Exception ex)
		{
		}
		return 0;
	}

	@Override
  protected void resetToInitialNodeDensity()
  {
		rayBranchBottomLevels.resetToInitialNodeDensity();
  }
	
	protected void superResetToInitialNodeDensity()
	{
		super.resetToInitialNodeDensity();
	}
//
//	@Override
//	protected void inactivateDownGoingSuperCrustalSegments(int minSuperCrustalLayer)
//	{
//		rayBranchBottomLevels.inactivateDownGoingSuperCrustalSegments(minSuperCrustalLayer);
//	}
//
//	protected void superInactivateDownGoingSuperCrustalSegments(int minSuperCrustalLayer)
//	{
//		super.inactivateDownGoingSuperCrustalSegments(minSuperCrustalLayer);
//	}
//
//	@Override
//	protected void inactivateUpGoingSuperCrustalSegments(int minSuperCrustalLayer)
//	{
//		rayBranchBottomLevels.inactivateUpGoingSuperCrustalSegments(minSuperCrustalLayer);
//	}
//
//	protected void superInactivateUpGoingSuperCrustalSegments(int minSuperCrustalLayer)
//	{
//		super.inactivateUpGoingSuperCrustalSegments(minSuperCrustalLayer);
//	}
//
//	@Override
//	protected void activateDownGoingSuperCrustalSegments() throws GeoTessException
//	{
//		rayBranchBottomLevels.activateDownGoingSuperCrustalSegments();
//	}
//
//	protected void superActivateDownGoingSuperCrustalSegments() throws GeoTessException
//	{
//		super.activateDownGoingSuperCrustalSegments();
//	}
//
//	@Override
//	protected void activateUpGoingSuperCrustalSegments() throws GeoTessException
//	{
//		rayBranchBottomLevels.activateUpGoingSuperCrustalSegments();
//	}
//
//	protected void superActivateUpGoingSuperCrustalSegments() throws GeoTessException
//	{
//		super.activateUpGoingSuperCrustalSegments();
//	}

	@Override
	protected void optimizeInitialize()
	{
		rayBranchBottomLevels.optimizeInitialize();
	}

	@Override
	protected void optimizeInitialize(double tol, double minNodeSpc)
	{
		rayBranchBottomLevels.optimizeInitialize(tol, minNodeSpc);
	}

	protected void superOptimizeInitialize()
	{
		super.optimizeInitialize();
	}

	protected void superOptimizeInitialize(double tol, double minNodeSpc)
	{
		super.optimizeInitialize(tol, minNodeSpc);		
	}

	@Override
	protected void optimizeOuterBeforeInner() throws BenderException
	{
		rayBranchBottomLevels.optimizeOuterBeforeInner();		
	}

	protected void superOptimizeOuterBeforeInner() throws BenderException
	{
		super.optimizeOuterBeforeInner();		
	}

	@Override
	protected void optimizeInnerInitialize()
	{
		rayBranchBottomLevels.optimizeInnerInitialize();		
	}

	protected void superOptimizeInnerInitialize()
	{
		super.optimizeInnerInitialize();		
	}

	@Override
	protected boolean optimizeInner() throws Exception
	{
		return rayBranchBottomLevels.optimizeInner();		
	}

	protected boolean superOptimizeInner() throws Exception
	{
		return super.optimizeInner();		
	}

	@Override
	protected boolean optimizeOuterAfterInner()
						throws GeoTessException, BenderException
  {
		return rayBranchBottomLevels.optimizeOuterAfterInner();
  }

	protected boolean superOptimizeOuterAfterInner()
						throws GeoTessException, BenderException
	{
		return super.optimizeOuterAfterInner();
	}

	@Override
	protected void optimizeFinalize()
	{
		rayBranchBottomLevels.optimizeFinalize();
	}

	protected void superOptimizeFinalize()
	{
		super.optimizeFinalize();
	}
//
//	@Override
//	protected void checkThinLayers(boolean rmvThinLayers,
//																 boolean addThickLayers) throws GeoTessException
//	{
//		rayBranchBottomLevels.checkThinLayers(rmvThinLayers, addThickLayers);
//	}

  /**
   * Overridden by RayBranchBottom to return the fastest level. Everyone else
   * simply returns themselves.
   * 
   * @return This RayBranch.
   */
	@Override
  protected RayBranch getFastestBranchLevel()
  {
  	return rayBranchBottomLevels.getFastestRayBranchBottom();
  }

  /**
   * Overridden by RayBranchBottom to return the fastest level. Everyone else
   * simply returns themselves.
   * 
   * @return This RayBranch.
   */
	@Override
  protected RayBranch getRayBranchBottomLevel(int level)
  {
  	return rayBranchBottomLevels.getRayBranchBottomLevel(level);
  }

}
