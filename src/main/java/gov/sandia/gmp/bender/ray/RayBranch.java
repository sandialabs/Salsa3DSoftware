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

import static gov.sandia.gmp.util.globals.Globals.NL;
import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.EarthConst;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.bender.Bender;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;
import gov.sandia.gmp.bender.BenderConstants.RayStatus;
//import gov.sandia.gmp.bender.BenderConstants;
import gov.sandia.gmp.bender.BenderException;
import gov.sandia.gmp.bender.BenderException.ErrorCode;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
//import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * An abstract base class for all RayBranch types. A Ray is now composed of one
 * or more RayBranches where the end points of each RayBranch are terminated by
 * a Source or Bottom-Side Reflection as its first point, and a Receiver or a
 * Bottom-Side Reflection as its last point. A Ray is then constructed as a
 * linked set of consecutive RayBranches that share Bottom-Side Reflections at
 * the RayBranch intersection locations.
 * 
 * @author jrhipp
 *
 */
public abstract class RayBranch
{
	private static final int maximumRayBranchNodeCount = 30000;
	private static final int maximumOuterIterations    = 100;
	private static final int maximumInnerIterations    = 25000;
	private static final int maximumIncreaseingTTCount = 7;
	private static final int maximumTTFlipFlopCount    = 50;

	/**
	 * A static map of model layer index to a minimum node spacing criteria
	 * within each interface layer. This map is populated by Bender.setup() at
	 * start up and is used to specify the minimum node spacing within RaySegment
	 * objects created in the layers.
	 */
	protected static final HashMap<Integer, Double>
												 initialRayLayerNodeSeparationDistance =
												 new HashMap<Integer, Double>();

	/**
	 * Used by the RayBranch optimization algorithm to output a debug information
	 * header (Ln1, Ln2, Ln3 and Ln4).
	 */
	protected static final String optimizeHeaderLn1 =
            "   Ray            Ray                           Ray                 " +
            "Inner  Outer   #        #       Point   Travel      Path    " +
            "   Max      Out-Of-       dt            dt          flag";
	protected static final String optimizeHeaderLn2 =
            "  Branch      Level/Layer                       Type                " +
            "Iter   Iter  Points  Segments  Spacing   Time      Length   " +
            "  Depth      Plane       Inner         Outer";
	protected static final String optimizeHeaderLn3 =
			"                                                                    " +
			"                                (km)     (sec)      (km)    " +
			"  (km)       (km)        (sec)         (sec)";
	
	protected static final String optimizeHeaderLn4 = "  " + Globals.repeat("-",  184);
	
	/**
	 * Used by the RayBranch optimization algorithm to output inner and outer
	 * iteration debug information.
	 */
	protected static final String frmtOptIter = 
			      "    %2d   %23s  %32s  %5d %3d   %5d     %3d      %6.1f  %8.3f  " +
			      "%8.3f  %8.3f   %8.3f  %12.6f  %12.6f  %s";

	public static final int debugHeaderOutputLimit = 25;

	protected EvaluateSnellsLaw		evaluateSnellsLaw = null;

	/**
	 * The ray that owns this RayBranch.
	 */
	protected Ray                owningRay         = null;
	
	protected RayBranch          owningBranch      = null;

	/**
	 * The RayBranch index in the owning Rays ray branch list.
	 */
	protected int                branchIndex       = -1;

	/**
	 * A descriptor for the first point in the branch (SOURCE or BOTTOM_SIDE_REFLECTION).
	 */
	protected RayDirection       firstPointType    = null;
	
	/**
	 * The first point in the ray branch.
	 */
  protected GeoTessPosition    firstPoint        = null;

	/**
	 * A descriptor for the last point in the branch (RECEIVER or BOTTOM_SIDE_REFLECTION).
	 */
	protected RayDirection       lastPointType     = null;

	/**
	 * The last point in the ray branch.
	 */
  protected GeoTessPosition    lastPoint         = null;

  /**
   * A descriptor for the type of branch ray direction (UPGOING, DOWNGOING, or BOTTOM). 
   */
  protected RayDirection       branchDirection   = null;

  /**
   * The list of segments defined in an internal branch list.
   */
  protected  ArrayList<RaySegment> branchSegments = null;
  
  protected RaySegment					firstActiveSegment = null;
  protected RaySegment					lastActiveSegment = null;
  protected int									activeSegmentCount = 0;
  
  protected RaySegment					firstInitialSegment = null;
  protected RaySegment					lastInitialSegment = null;
  protected int									initialSegmentCount = 0;

  /**
   * Set to true if this branch is any part of a refraction branch (i.e the
   * up- or down-going component of a bottom branch).
   */
  protected boolean            isBottomBranch      = false;

  /**
   * The previous ray direction change segment connecting to the first
   * segment of this branch. If null the first point is the source.
   */
  protected RaySegment         prevDirctnChngSegment = null;

  /**
   * The next ray direction change segment connecting to the last
   * segment of this branch. If null the last point is the receiver.
   */
  protected RaySegment         nextDirctnChngSegment = null;

	/**
	 * Travel time of the ray from first point to the last in seconds.
	 */
	protected double						 travelTime						 = Globals.NA_VALUE;

	/**
	 * Path length of the RayBranch.
	 */
	protected double						 pathLength						 = Globals.NA_VALUE;

	/**
	 * The ray branch point spacing.
	 */
	protected double						 pointSpacing					 = Globals.NA_VALUE;

	/**
	 * The number of points defining the ray branch.
	 */
	protected int								 nPoints							 = -1;

	/**
	 * The first to last angular distance in degrees.
	 */
	protected double						 firstLastDistanceDeg	 = 0.0;
	
	/**
	 * A GreatCircle defined from the first to the last point in the branch.
	 */
	protected GreatCircle 			 greatCircle           = null;

	/**
	 * The branch optimization outer loop counter index. 
	 */
  private int nOuter = 0;

	/**
	 * The branch optimization inner loop counter index. 
	 */
  private int nInner = 0;

  /**
   * Outer Optimization flag set to true if the outer Iteration has converged. 
   */
  protected boolean outerOptimizationConverged = false;

  /**
   * Inner Optimization flag set to true if the inner Iteration has converged. 
   */
  protected boolean innerOptimizationConverged = false;

  /**
   * Current branch travel time used during optimization.
   */
  private double ttOptimize = 0.0;
  
  /**
   * Previous outer iteration travel time used during optimization.
   */
  private double tt_outer = 0.0;
  
  /**
   * Previous inner iteration travel time used during optimization.
   */
  private double tt_inner = 0.0;
  
	/**
	 * The branch optimization inner loop travel time delta between consecutive
	 * calls to inner optimization loop. 
	 */
  private double dt_inner = 0.0;

  /**
   * The branch optimization inner iteration travel time tolerance (sec).
   */
  private double dt_inner_tol = 0.001;

	/**
	 * The branch optimization outer loop travel time delta between consecutive
	 * calls to outer optimization loop. 
	 */
  private double dt_outer = 0.0;

  /**
   * The branch optimization outer iteration travel time tolerance (sec).
   */
  private double dt_outer_tol = 0.001;

  /**
   * The number of times the travel time increased during an inner iteration.
   */
  private int    increasingTTCount = 0;

  private boolean ttSign           = false;
  private int    ttFlipFlopCount   = 0;

  /**
   * Inner loop doubling count.
   */
  protected int  innerLoopDoublingCount = 0;

  /**
   * The branch optimization outer iteration minimum node spacing convergence
   * criteria.
   */
  private double optimizeMinNodeSpacing = 30.0;
  
  /**
   * Returns the top most layer occupied by this branch. Defined by each concrete
   * subclass.
   * 
   * @return The top most layer occupied by this branch.
   */
  public abstract int getTopLayer();

  /**
   * Returns the bottom most layer occupied by this branch. Defined by each concrete
   * subclass.
   * 
   * @return The bottom most layer occupied by this branch.
   */
  public abstract int getBottomLayer();
  
  /**
   * Returns the deepest depth (km) of a node on this branch. Defined by each concrete
   * subclass.
   * 
   * @return The deepest depth (km) of a node on this branch.
   */
  public abstract double getMaxRayBranchDepth();

  /**
   * Returns the deepest node on this branch. Defined by each concrete
   * subclass.
   * 
   * @return The deepest node on this branch.
   */
  public abstract GeoTessPosition getDeepestRayBranchNode();

  /**
   * Returns the Ray type name. Defined by each concrete subclass.
   * 
   * @return The Ray type name.
   */
  public abstract String getRayTypeName();
  
  /**
   * Returns the Ray branch type name. Defined by each concrete subclass.
   * 
   * @return The Ray branch type name.
   */
  public abstract String getRayBranchTypeName();

  /**
   * Performs the Snell's law enforcement of each segment end point node that
   * lies on a layer boundary (except the first and last node of the branch).
   * 
   * @param evenInnerIteration A boolean used by some branch type Snell's law
   *                           enforcement to alternate direction in the node
   *                           chain.
   * @throws Exception
   */
  protected abstract void enforceSnellsLaw(boolean evenInnerIteration) throws Exception;

  //protected HashMap<Integer, RaySegment> thinSegments = null;

  public void debugOutput(RaySegment segment, GeoTessPosition node)
  {
  	String s = String.format("seg layer = %2d, node index = %2d, node lon = %7.2f, node depth = %7.2f",
  			                     segment.getMajorLayerIndex(),
  			                     node.getIndex(), node.getLongitudeDegrees(),
  			                     node.getDepth());
		System.out.println (s);
  }

  /**
   * Standard Constructor.
   * 
   * @param ray			 Ray that owns this branch.
   * @param firstPnt The first point on the branch.
   * @param lastPnt  The last point on the branch.
   */
  public RayBranch(Ray ray, GeoTessPosition firstPnt,
                   GeoTessPosition lastPnt, RayBranch branch)
  {
  	greatCircle = new GreatCircle(lastPnt.getVector(), firstPnt.getVector(), false);
  	owningRay = ray;
  	owningBranch = branch;
    if (owningBranch != null)
    	lastInitialSegment = lastActiveSegment = owningBranch.lastInitialSegment;
  	firstPoint = firstPnt;
   	firstPointType = RayDirection.SOURCE;

  	lastPoint = lastPnt;
   	lastPointType = RayDirection.RECEIVER;
   	
   	//branchSegments = new ArrayList<RaySegment>();
  }

  /**
   * Creates a new RayBranch given an owning ray, an input branch used to
   * extract the initial layer intersection node positions, and the first
   * and last input points. The input ray is simply a container defining the
   * source and receiver location, Bender, which contains the phase layer model,
   * GeoTessModel, and wave type map object. This method is useful for recreating
   * a branch when a derivative change is evaluated, where the derivative may
   * be taken with a source or receiver position change or a bottom side
   * reflection position change. Note that RayBottomBranches created by this
   * method assume the same active level as the input branch.
   * 
   * @param ray       The input ray into which the ray branch is mapped.
   * @param branch    The map containing the layer pierce points and other branch
   *                  type information.
   * @param frstNode  The start node of the new branch. This will replace the
   *                  first node of the input branch.
   * @param lstNode   The last node of the new branch. This will replace the
   *                  last node of the input branch.
   * @param hdr       Bender.print(...) header string.
   * 
   * @return          The new branch.
   * @throws Exception
   */
  public static RayBranch createNewRayBranch(Ray ray, RayBranch branch,
  		                                       GeoTessPosition frstNode,
  		                                       GeoTessPosition lstNode) throws Exception
  {
    RayBranch newBranch = null;

    // get the branch type and create a new branch
    if (branch.isBottomBranch)
    {
    	RayBranchBottom inptBranch = (RayBranchBottom) branch;
    	newBranch = new RayBranchBottom(ray, inptBranch, frstNode, lstNode);
    }
    else if (branch instanceof RayBranchBottomFixedReflection)
    {
    	newBranch = new RayBranchBottomFixedReflection(ray,
    			            (RayBranchBottomFixedReflection) branch, frstNode,
    			            lstNode);
    }
    else if (branch instanceof RayBranchDownGoing)
    {
    	newBranch = new RayBranchDownGoing(ray, branch, frstNode, lstNode);
    }
    else if (branch instanceof RayBranchUpGoing)
    {
    	newBranch = new RayBranchUpGoing(ray, branch, frstNode, lstNode);
    }

    return newBranch;
  }

  public double getOuterOptimizationConvergence()
  {
  	return dt_outer;
  }

  public double getInnerOptimizationConvergence()
  {
  	return dt_inner;
  }

  /**
   * Build a copy of the input segment using the input previous node as the
   * new segments first node and a deep copy of the copy segments last node as
   * the new segments last node.
   * 
   * @param previous    The new segments first node.
   * @param copySegment The segment to be copied.
   * @return            The last node of the new segment.
   * @throws GeoTessException
   */
  protected GeoTessPosition buildInitialSegment(GeoTessPosition previous,
  		                                          RaySegment copySegment)
  		      throws GeoTessException
  {
  	GeoTessPosition next;

  	// make a deep copy of the copy segments last node. If that node's index
  	// equals the last points index then use the last point as the next node.

		if (copySegment.last.getIndex() == lastPoint.getIndex())
			next = lastPoint;
		else
 	    next = copySegment.last.deepClone();

		// add a new segment between previous and next and return next

		addNewBranchSegment(previous, next);
//
//		// look for the next interface in the wave type mapper. If found update
//  	// the wave type attribute index otherwise return the current wave type
//  	// attribute index.
//
//  	int waveTypeAttributeIndex = owningRay.waveTypeMapper.getCurrentWaveSpeedAttributeIndex();
//  	//if (owningRay.getNBranches() > 0)
//  	  waveTypeAttributeIndex = owningRay.waveTypeMapper.
//  		                         updateNextWaveSpeedIndex(previous.getIndex());
//
//    // create the node list with additional nodes between the segment end
//  	// points if necessary
//
//		LinkedList<GeoTessPosition> nodes = addInitialSegmentNodes(previous, next, 0);
//
//		// create the segment and add it to the owning rays segment list ...
//		// return next which will be set to previous for the next call.
//
//		addBranchSegment(new RaySegmentBend(owningRay, this, nodes,
//				                                waveTypeAttributeIndex,
//																				lastInitialSegment));
		return next;
  }

  /**
    * Builds a new "Initial" ray segment that is part of an up-going or down-
    * going branch defined between two consecutive layer boundaries that lie
    * somewhere between the firstPoint and lastPoint of this branch. The first
    * layer is defined in the input previous point as that point lies on that
    * interface. The second point will be created and assigned to the "next"
    * position node and will lie on the interface specified by the index
    * "nextLayer".
    *
    * @param previous  The starting point of the new segment.
    * @param nf        The inverse radius delta between lastPoint and
    *                  firstPoint.
    * @param nextLayer The layer index upon which the last point of the new
    *                  segment to be created (next) will lie.
	  * @return The end point of the new segment (next).
    * 
    */
  protected GeoTessPosition buildInitialSegment(GeoTessPosition previous,
  		                                          double nf, int nextlayer)
  		      throws GeoTessException
  {
  	GeoTessPosition next;

  	// set the next node to the last node if nextLayer == the last point index
		// otherwise calculate the next nodes position

  	//System.out.println(firstPoint.getPositionString());
  	//System.out.println(lastPoint.getPositionString());
  	//System.out.println(previous.getPositionString());
		if (nextlayer == lastPoint.getIndex())
			next = lastPoint;
		else
		{
			// get the average radius at layer index "nextLayer" between the first
			// point and last point of this branch ... then calculate the radial
			// fraction (f) represented by the average delta radius between the first
			// point and last point.

			double firstRad = firstPoint.getRadiusTop(nextlayer);
			double ravg = 0.5 * (lastPoint.getRadiusTop(nextlayer) + firstRad);
			double f = nf * (ravg - firstPoint.getRadius());

			// create the segment end point (next) and set its position as a fraction
			// between the first point and last point of this branch. Then set the
			// position to the top of layer = nextLayer and set its index

			next = firstPoint.deepClone();
			next.setIntermediatePosition(firstPoint, lastPoint, f);
			next.setTop(nextlayer);
			next.setIndex(nextlayer);
		}
  	//System.out.println(next.getPositionString());

		// add a new segment between previous and next and return next
		
		addNewBranchSegment(previous, next);
//		// look for the next interface in the wave type mapper. If found update
//  	// the wave type attribute index otherwise return the current wave type
//  	// attribute index.
//
//  	int waveTypeAttributeIndex = owningRay.waveTypeMapper.getCurrentWaveSpeedAttributeIndex();
//  	//if (owningRay.getNBranches() > 0)
//  	  waveTypeAttributeIndex = owningRay.waveTypeMapper.
//  		                         updateNextWaveSpeedIndex(previous.getIndex());
//
//    // create the node list with additional nodes between the segment end
//  	// points if necessary
//
//		LinkedList<GeoTessPosition> nodes = addInitialSegmentNodes(previous, next, 0);
//
//		// create the segment and add it to the owning rays segment list ...
//		// return next which will be set to previous for the next call.
//
//		addBranchSegment(new RaySegmentBend(owningRay, this, nodes,
//																				waveTypeAttributeIndex,
//																				lastInitialSegment));
		return next;
  }

  /**
   * Builds a new segment from the input frst point to the input last point
   * stopping at a place close to where the segment crosses the top of the input
   * layer (nextLayer).
   *  
   * @param frst The first point.
   * @param last The last point.
   * @param nextlayer The layer at which the new segment terminates.
   * @return The new position that lies on the input layer (nextLayer) from
   *  			 the first point.
   * @throws GeoTessException
   */
  protected GeoTessPosition buildInitialSegment2(GeoTessPosition frst,
  																							 GeoTessPosition last,
  																							 int nextlayer) throws GeoTessException
  {
  	// get the average radius of the first and last points at the input layer
  	// top

		double firstRad = frst.getRadiusTop(nextlayer);
		double lastRad = last.getRadiusTop(nextlayer);
		double ravg = 0.5 * (lastRad + firstRad);

		// calculate the fraction of the radius between the first and last points
		// that is equal to the average radius at the input layer
		
		double f = (ravg - firstRad) / (lastRad - firstRad);
		
		// calculate the 3D position along the vector from frst to last that has the
		// average radius and normalize that vector as the next unit vector position

		double[] v = Vector3D.subtract(last.get3DVector(), frst.get3DVector());
		v = Vector3D.addMult(f, v, frst.get3DVector());
		VectorUnit.normalize(v);

		// set the new next node to be at the top of the input layer along the
		// calculated unit normal

		GeoTessPosition next = frst.deepClone();
		next.setTop(nextlayer, v);
		next.setIndex(nextlayer);

		// create a new branch segment and return it

		addNewBranchSegment(frst, next);
		return next;
  }

  protected void addNewBranchSegment(GeoTessPosition previous, GeoTessPosition next) throws GeoTessException
  {
		// look for the next interface in the wave type mapper. If found update
  	// the wave type attribute index otherwise return the current wave type
  	// attribute index.

  	//int waveTypeAttributeIndex = owningRay.waveTypeMapper.getCurrentWaveSpeedAttributeIndex();
  	int waveTypeAttributeIndex = owningRay.waveTypeMapper.
  		                         	 updateNextWaveSpeedIndex(previous.getIndex());

    // create the node list with additional nodes between the segment end
  	// points if necessary

		LinkedList<GeoTessPosition> nodes = addInitialSegmentNodes(previous, next, 0);

		// create the segment and add it to the owning rays segment list ...
		// return next which will be set to previous for the next call.

		addBranchSegment(new RaySegmentBend(owningRay, this, nodes,
				                                waveTypeAttributeIndex,
																				lastInitialSegment));  	
  }

  private boolean isDebugBranchIndexOutput()
  {
  	if ((owningRay.bender.debugBranchIndexOutput ==
 			   Bender.debugBranchOutputALL) ||
 			(owningRay.bender.debugBranchIndexOutput == branchIndex))
  		return true;
  	else
  		return false;
  }

  private boolean isDebugRayBranchBottomLevelOutput()
  {
  	if ((owningRay.bender.debugRayBranchBottomLevelOutput ==
 			   Bender.debugBranchOutputALL) ||
 			((owningRay.bender.debugRayBranchBottomLevelOutput ==
 					Bender.debugBranchBottomLevelFast) &&
 			 (getFastestBranchLevel() == this)) ||
  		((owningRay.bender.debugRayBranchBottomLevelOutput !=
 					Bender.debugBranchBottomLevelFast) &&
  		 (getRayBranchBottomLevel(owningRay.bender.debugRayBranchBottomLevelOutput) ==
					this)))
  		return true;
  	else
  		return false;
  }

  protected String getOptimizeIterationString()
  {
  	String level = "NA";
  	String rt    = "";
  	if (this instanceof RayBranchBottom)
  	{
  		RayBranchBottom rbb = (RayBranchBottom) this;
  		level = rbb.activeLayerLevel.getName() + "(" +
							rbb.currentLayerLevel;
  		if (rbb.rayBranchBottomLevels.getFastestRayBranchBottom() == this)
    		level += " FAST)";
  		else  			
  		  level += ")";

  		rt = "Bottom (" + rbb.getRayTypeName() + ")";
  	}
  	else if (isBottomBranch())
  	{
  		rt    = "Bottom (REFLECTION FIXED)";  		
  	}
  	else
  		rt = getRayTypeName();

  	String flg = "";
  	if (outerOptimizationConverged)
  		flg = "OUTER Convergence";
  	else if (innerOptimizationConverged)
  		flg = "INNER Convergence";
  	else if (isRayTypeInvalid())
  		flg = "INVALID";

  	if (owningRay.bender.debugHeaderOutputCount >= debugHeaderOutputLimit)
		{
			owningRay.bender.print(NL + optimizeHeaderLn1 + NL +
					optimizeHeaderLn2 + NL +
					optimizeHeaderLn3 + NL +
				   	optimizeHeaderLn4 + NL + NL);
			owningRay.bender.debugHeaderOutputCount = 0;
		}

  	++owningRay.bender.debugHeaderOutputCount;

  	return String.format(frmtOptIter, branchIndex, level, rt, nInner, nOuter, getNPoints(),
        	 activeSegmentCount, getPointSpacing(), travelTime, pathLength,
        	 getMaxRayBranchDepth(), getOutOfPlane(), dt_inner, dt_outer, flg) + NL;
  }

  /**
   * Optimizes this branch using Snell's law at the layer interfaces and the
   * Um Thurber Bending algorithm at interior nodes. The inner iteration performs
   * this function. When an inner iteration converges a check is made to see if
   * the outer node-doubling iteration has converged. If not the node density
   * is doubled and another inner/outer iteration is performed. This continues
   * until the outer (node-doubling) iteration also converges.
   * 
   * @throws Exception
   */
  protected void optimize() throws Exception
  {
		optimizeInitialize();
    optimizeLoop();
		optimizeFinalize();
  }

  /**
   * Optimizes this branch using Snell's law at the layer interfaces and the
   * Um Thurber Bending algorithm at interior nodes. The inner iteration performs
   * this function. When an inner iteration converges a check is made to see if
   * the outer node-doubling iteration has converged. If not the node density
   * is doubled and another inner/outer iteration is performed. This continues
   * until the outer (node-doubling) iteration also converges.
   * 
   * @throws Exception
   */
  protected void optimize(double tol, double minNodeSpc) throws Exception
  {
		optimizeInitialize(tol, minNodeSpc);
    optimizeLoop();
		optimizeFinalize();
  }

  private void optimizeLoop() throws Exception
  {
		boolean outLoopDone = false;
		while (!outLoopDone && !getFastestRayBranchLevel().outerOptimizationConverged)
		{
	  	owningRay.travelTime = owningRay.pathLength = Globals.NA_VALUE;
			optimizeOuterBeforeInner();
			optimizeInnerInitialize();
			while (!optimizeInner() && !getFastestRayBranchLevel().innerOptimizationConverged);
			owningRay.getTravelTime();
			outLoopDone = optimizeOuterAfterInner();
		}
  }

  private RayBranch getFastestRayBranchLevel()
  {
  	return owningRay.rayBranches.get(branchIndex).getFastestBranchLevel();
  }
//
//  private boolean isFirstRaySegment(RaySegment rs)
//  {
//  	if (owningBranch != null)
//  	{
//  		if ((owningBranch.firstInitialSegment == rs) &&
//  				(owningBranch.prevDirctnChngSegment == null))
//  			return true;
//  	}
//  	else if (prevDirctnChngSegment == null)
//			return true;
//  	
//  	return false;
//  }
//
//  private boolean isLastRaySegment(RaySegment rs)
//  {
//  	if (owningBranch != null)
//  	{
//  		if ((owningBranch.lastInitialSegment == rs) &&
//  				(owningBranch.nextDirctnChngSegment != null))
//  			return true;
//  	}
//  	else if (nextDirctnChngSegment != null)
//			return true;
//  	
//  	return false;
//  }
//
//  protected void checkThinLayers(boolean rmvThinLayers,
//  															 boolean addThickLayers) throws GeoTessException
//  {
//		GeoTessPosition topNode = null;
//  	RaySegment rs = firstInitialSegment;
//  	while (rs != lastInitialSegment.nextActiveSegment)
//  	{
//  		if (rs.majorLayerIndex >= owningRay.bender.getMaximumModelNotThinLayer())
//  		{
//  			if (!rs.isActive && addThickLayers)
//  			{
//  				// segment is inactive ... find nearest active top node and get the
//  				// thickness of this layer
//
//  				RaySegment rsnext = rs.nextInitialSegment;
//  				while ((rsnext != null) && (!rsnext.isActive))
//  					rsnext = rsnext.nextInitialSegment;
//  				if (rsnext == null)
//  					topNode = this.nextDirctnChngSegment.getMiddleNode();
//  				else
//  					topNode = rsnext.getTopLayerNode();
//  				double thickness = topNode.getLayerThickness(rs.majorLayerIndex);
//
//  				// if layer is greater than some defined segment thickness then
//  				// reactivate the layer.
//
//  				if (thickness >= getMinOuterIterSegmentAddThickness())
//  				{
//  					rs.activateSegment();
//  					activateSegment(rs);
//  				}
//  			}
//  			else if (rmvThinLayers)
//  			{
//  				if (!isFirstRaySegment(rs) && !isLastRaySegment(rs))
//  				{
//	  				// segment is active ... get the thickness of this layer
//	
//	  				topNode = rs.getTopLayerNode();
//	  				double thickness = topNode.getLayerThickness(rs.majorLayerIndex);
//	
//	  				// if layer is less than the minimum thickness in-activate the layer
//	
//	  				if (thickness <= BenderConstants.MIN_LAYER_THICKNESS)
//	  				{
//	  					inactivateSegment(rs);
//	  					rs.inactivateSegment();
//	  				}
//  				}
//  			}
//  		}
//  		rs = rs.nextInitialSegment;
//  	}
//  }
//
//  /**
//   * The minimum segment "add" thickness is a function of outer iterations count.
//   * It is larger for lower outer iteration counts so only really thick layers
//   * are reintroduced at the end of the first outer iteration. The default value
//   * is ultimately twice the BenderContants.MIN_LAYER_THICKNESS for larger outer
//   * iteration counts. See the bender method.
//   * 
//   *    getMinOuterIterSegmentAddThickness(int outerIter)
//   *
//   * for details.
//   * 
//   * @return The minimum outer iteration segment add thickness value. Inactivated
//   */
//  private double getMinOuterIterSegmentAddThickness()
//  {
//		if (owningBranch != null)
//			return owningRay.bender.getMinOuterIterSegmentAddThickness(owningBranch.nOuter);
//		else
//			return owningRay.bender.getMinOuterIterSegmentAddThickness(nOuter);
//  }
//
//  protected void activateSegment(RaySegment rs)
//  {
//  	if (rs.nextActiveSegment == firstActiveSegment)
//  		firstActiveSegment = rs;
//  	if (rs.prevActiveSegment == lastActiveSegment)
//  		lastActiveSegment = rs;
//
//		++activeSegmentCount;
//		if (owningBranch != null) owningBranch.activateSegment(rs);
//  }
//
//  protected void inactivateSegment(RaySegment rs)
//  {
//  	if (firstActiveSegment == rs)
//  		firstActiveSegment = rs.nextActiveSegment;
//  	if (lastActiveSegment == rs)
//  		lastActiveSegment = rs.prevActiveSegment;
//
//		--activeSegmentCount;
//		if (owningBranch != null) owningBranch.inactivateSegment(rs);
//  }

  public RaySegment getLastActiveSegment()
  {
  	return lastActiveSegment;
  }

  public RaySegment getFirstActiveSegment()
  {
  	return firstActiveSegment;
  }

  protected void optimizeFinalize()
  {
		owningRay.setStatus(RayStatus.FINAL_RAY);  	
  }

  protected void optimizeInitialize(double tol, double minNodeSpc)
  {
		outerOptimizationConverged = false;
		nOuter = 0;
		dt_outer = 0.0;
		ttOptimize = 0.0;
		tt_outer = 0.0;
		
		dt_inner_tol = dt_outer_tol = tol;
		optimizeMinNodeSpacing = minNodeSpc;
  }

  protected void optimizeInitialize()
  {
		outerOptimizationConverged = false;
		nOuter = 0;
		dt_outer = 0.0;
		ttOptimize = 0.0;
		tt_outer = 0.0;

		// specify convergence criteria for loops. Distance dependent.
		dt_inner_tol = owningRay.bender.getTravelTimeConvergenceTolerance();
		dt_outer_tol = owningRay.bender.getTravelTimeConvergenceTolerance();
		optimizeMinNodeSpacing = owningRay.bender.getMinimumRayNodeSpacing();
//		for (int i = 1; i < owningRay.bender.getConvergenceCriteria().length; ++i)
//			if (firstLastDistanceDeg < owningRay.bender.getConvergenceCriteria()[i][0])
//			{
//				dt_outer_tol = owningRay.bender.getConvergenceCriteria()[i][1];
//				optimizeMinNodeSpacing = owningRay.bender.getConvergenceCriteria()[i][2];
//			}

//		if (owningRay.nodeMovementStatisticsSnellsLaw != null)
//		{
//			RaySegment segment = firstActiveSegment;
//			while (segment != null)
//			{
//				segment.turnOnNodeMovementStatistics();
//				segment = segment.nextActiveSegment;
//			}
//		}
//			for (int i = 0; i < branchSegments.size(); ++i)
//				branchSegments.get(i).turnOnNodeMovementStatistics();
  }

  protected void optimizeOuterBeforeInner() throws BenderException
  {
  	if (outerOptimizationConverged) return;

		++nOuter;
		outerOptimizationConverged = false;
		if (nOuter > maximumOuterIterations)
			throw new BenderException(ErrorCode.FATAL,
					"Too many iterations in outer loop. \n");
	}

  protected void optimizeInnerInitialize()
  {
  	if (outerOptimizationConverged) return;

		innerOptimizationConverged = false;
    dt_inner = tt_inner = 0.0;
		nInner = increasingTTCount = ttFlipFlopCount = 0;
  }

  protected boolean optimizeOuterAfterInner() throws GeoTessException, BenderException
  {
  	if (outerOptimizationConverged) return true;

    boolean outLoopDone = false;

		// calculate convergence for outer loop
		dt_outer = abs(tt_outer - ttOptimize);

		// bender v 3.2
		// converged = (nOuter > 3 && dt_outer < (distDeg <= 30 ? .002 :
		// .01));
		// bender v 3.3 (changed 4/29/2009)
		outerOptimizationConverged = ((dt_outer < dt_outer_tol) &&
								 									(getPointSpacing() < optimizeMinNodeSpacing));

		tt_outer = ttOptimize;
		// replace getRayTypeName().equals("INVALID") with isRayTypeInvalid()
		outLoopDone = outerOptimizationConverged ||
									isRayTypeInvalid() ||
									(getNPoints() > maximumRayBranchNodeCount);

		if (outLoopDone)
		{
			if (!outerOptimizationConverged && isRayTypeInvalid())
				if (!isMiddleOnlyActiveNode())
				  throw new BenderException(ErrorCode.FATAL,
						  "Failure to converge in Ray.optimize(). \n");
			else if (this instanceof RayBranchBottom)
			{
				RaySegmentBottom bottomSegment = ((RayBranchBottom) this).getBottomSegment();
				if ((bottomSegment.rayType == RayType.REFRACTION) &&
						bottomSegment.isMiddleOnlyActiveNode())
				{
					bottomSegment.rayType = RayType.INVALID;
				}
			}
		}

		if ((owningRay.bender.getVerbosity() > 1) ||
			  ((owningRay.bender.getVerbosity() == 1) && outLoopDone))
		{
			if (isDebugBranchIndexOutput() && isDebugRayBranchBottomLevelOutput())
		    owningRay.bender.print(getOptimizeIterationString());
		}

		owningRay.setStatus(RayStatus.OUTER_LOOP);

		// if ray has not converged, double number of nodes on the branch where the
		// point density is less than the 1/2 the total ray point density.
		if (!outLoopDone)
			doubleNodes(getPointSpacing() / 2.0);

    return outLoopDone;
  }

  protected boolean isBouncePointStillMoving()
  {
  	if (!owningRay.bouncePointsFrozen)
  	{
	  	if ((prevDirctnChngSegment != null) &&
	  			owningRay.isBouncePointStillMoving(branchIndex-1))
	  		return true;
	  	else if ((nextDirctnChngSegment != null) &&
	  			owningRay.isBouncePointStillMoving(branchIndex))
	  		return true;
	  	else return false;
  	}
  	else
  		return false;
  }

  protected boolean optimizeInner() throws Exception
  {
  	if (innerOptimizationConverged) return true;

		++nInner;

		if (System.currentTimeMillis() >= owningRay.bender.getTimeToAbort())
		{
			owningRay.setInvalid();
			throw new BenderException(ErrorCode.NONFATAL, String.format(
					"Maximum calculation time exceeded (%1.2f seconds) \n",
					owningRay.bender.getMaxEllapsedTime() * 1e-3));
		}
		
		// It is expected that dt_inner will ~monotonically decrease
		// after the very first inner loop. Sometimes the ray oscillates
		// between two local minima causing dt_inner to increase. If it
		// increases more than maximumIncreaseingTTCount times, double
		// the nodes. This seems to make it settle down.
		// Don't start doubling nodes until any bounce point movement is minimal.
		// If this branch's inner optimization convergence flag is set then do not
		// double the nodes.
		if ((increasingTTCount > maximumIncreaseingTTCount) &&
		    !innerOptimizationConverged)
		{
			increasingTTCount = 0;
			++innerLoopDoublingCount;
			doubleNodes();
		}

		if (nInner > maximumInnerIterations)
			throw new BenderException(ErrorCode.FATAL, String.format(
					"Too many iterations in inner loop ...\n"));

		// relax all nodes applying Snells Law or Um and Thurber bending.
		relaxNodes(nInner % 2 == 0);

		// compute travel time, pathLength and nPoints.
		ttOptimize = getTravelTime();

		// assemble ray type
		evaluateRayType();

		// calculate convergence for inner loop
		dt_inner = abs(tt_inner - ttOptimize);

		// bender v 3.2
		// converged = (nInner > 1 && dt_inner < .0005 && dt_inner >
		// -0.001);
		// bender v 3.3
		// converged = (nInner > 1 && dt_inner < .0001 && dt_inner >
		// -0.001);
//			if (nOuter == 0)
//			  converged = ((nInner > 2) && (dt_inner < dt_inner_tol_0));
//			else
		innerOptimizationConverged = (((nInner > 2) && (dt_inner < dt_inner_tol)) ||
																	((nInner > 5000) && (dt_inner < 3.0 * dt_inner_tol)));
		if (tt_inner < ttOptimize)
		{
			++increasingTTCount;
			if (ttSign) incrementFlipFlop();
			ttSign = false;
		}
		else
		{
			if (!ttSign) incrementFlipFlop();
			ttSign = true;
		}

		//innerLoopDone = converged || rtTypeName.equals("INVALID");
		boolean innerLoopDone = innerOptimizationConverged || isRayTypeInvalid();

		if ((owningRay.bender.getVerbosity() > 2) && !innerLoopDone)
		{
			if (isDebugBranchIndexOutput() && isDebugRayBranchBottomLevelOutput())
		    owningRay.bender.print(getOptimizeIterationString());
		}
//
//		if (owningRay.nodeMovementStatisticsSnellsLaw != null)
//			owningRay.bender.print(owningRay.getNodeRelaxationTable());

		tt_inner = ttOptimize;
		owningRay.setStatus(RayStatus.INNER_LOOP);
		//incrementBadFitness();

		return innerLoopDone;
  }
  
  private void incrementFlipFlop() throws GeoTessException
  {
		++ttFlipFlopCount;
		
		if (ttFlipFlopCount > RayBranch.maximumTTFlipFlopCount)
		{
			ttFlipFlopCount = 0;
			if (this == getFastestRayBranchLevel())
			  doubleNodes();
		}
  }
  
//
//  private void incrementBadFitness()
//  {
//  	RaySegment segment = firstActiveSegment;
//  	while (segment != lastActiveSegment.nextActiveSegment)
//  	{
//  		segment.badFitnessIncrement();
//  		segment = segment.nextActiveSegment;
//  	}
////  	for (int i = 0; i < branchSegments.size(); ++i)
////  		if (!branchSegments.get(i).ignoreSegment)
////  		  branchSegments.get(i).badFitnessIncrement();
//  }

  /**
   * The actual Snell's law and bending node relation of the optimization inner
   * iteration.
   * 
   * @param evenInnerIteration A boolean used by some branch type Snell's law
   *                           enforcement to alternate direction in the node
   *                           chain.
   * @throws Exception
   */
  protected void relaxNodes(boolean evenInnerIteration) throws Exception
  {
		travelTime = Globals.NA_VALUE;
		pathLength = Globals.NA_VALUE;
		nPoints = Integer.MIN_VALUE;

		// move the nodes on interfaces to honor snell's law
		enforceSnellsLaw(evenInnerIteration);
		//outputSegments();
		
		// bend the nodes in the interior of layers.
		bend();
  }
//
//  private void outputSegments()
//  {
//  	System.out.print("Branch " + branchIndex);
//  	if (this instanceof RayBranchBottom)
//  		System.out.print(", level " + ((RayBranchBottom) this).currentLayerLevel);
//  	RaySegment segment = firstActiveSegment;
//  	int i = 0;
//  	while (segment != lastActiveSegment.nextActiveSegment)
//  	{
//  		System.out.print(", seg("+ i++ + ") = " + segment.fitness);
//			segment = segment.nextActiveSegment;  		
//  	}
//
////  	for (int i = 0; i < branchSegments.size(); ++i)
////  	{
////  		System.out.print(", seg("+ i + ") = " + branchSegments.get(i).fitness);
////  	}
//  	System.out.println("");
//  }

  /**
   * Performs Um Thurber bending of each segment in the branch.
   * 
   * @throws GeoTessException
   */
  private void bend() throws GeoTessException
	{
  	RaySegment currentSegment = firstActiveSegment;
  	while (currentSegment != lastActiveSegment.nextActiveSegment)
  	{
			currentSegment.bend();
			currentSegment = currentSegment.nextActiveSegment;
  	}
//		for (int i = 0; i < branchSegments.size(); ++i)
//		{
//				//if (segments.get(i).isBottom())
//				//{
//				//	bendSegmentToConvergence(segments.get(i), 0.002);
//				//}
//				//else
//				bendSegment(branchSegments.get(i));
//		}

		owningRay.setStatus(RayStatus.BENT);
	}
//
//	/**
//	 * Bends the input segments nodes (except first and last). If node movement
//	 * statistics is on then the segments node movement are accumulated into
//	 * the rays total node movement "Bend" statistic.
//	 * 
//	 * @param segment The segment to undego bending.
//	 * @throws GeoTessException
//	 */
//	private void bendSegment(RaySegment segment) throws GeoTessException
//	{
//		segment.bend();
////		if (owningRay.nodeMovementStatisticsBend != null)
////			owningRay.nodeMovementStatisticsBend.add(segment.nodeMovementStats);
//	}

	/**
	 * Returns the first node on the branch.
	 * 
	 * @return The first node on the branch.
	 */
  public GeoTessPosition getBranchFirstNode()
  {
  	return firstPoint;
  }
  
	/**
	 * Returns the last node on the branch.
	 * 
	 * @return The last node on the branch.
	 */
  public GeoTessPosition getBranchLastNode()
  {
  	return lastPoint;
  }

  /**
   * Sets a BOTTOM-SIDE Reflection segment if one is connected to the start of
   * this branch.
   * @param prevSegment The Bottom-Side Reflection segment connected to the
   *                    start of this branch.
   */
  public void setPreviousDirectionChangeSegment(RaySegment prevSegment)
  {
  	// Last node of prevSegment must = firstPoint

  	prevDirctnChngSegment = prevSegment;
    if (prevDirctnChngSegment == null)
    	firstPointType = RayDirection.SOURCE;
    else
    	firstPointType = prevDirctnChngSegment.getRayDirectionChangeType();
  }

  /**
   * Sets a BOTTOM-SIDE Reflection segment if one is connected to the end of
   * this branch.
   * @param prevSegment The Bottom-Side Reflection segment connected to the
   *                    end of this branch.
   */
  public void setNextDirectionChangeSegment(RaySegment nextSegment)
  {
  	// First node of nextSegment must = lastPoint

  	nextDirctnChngSegment = nextSegment;
    if (nextDirctnChngSegment == null)
    	lastPointType = RayDirection.RECEIVER;
    else
    	lastPointType = nextDirctnChngSegment.getRayDirectionChangeType();
  }

  /**
   * Returns the previous Bottom-Side Reflection Segment (Null if not defined.)
   * 
   * @return The previous Bottom-Side Reflection Segment (Null if not defined.)
   */
  public RaySegment getPreviousDirectionChangeSegment()
  {
  	return prevDirctnChngSegment;
  }

  /**
   * Returns the next Bottom-Side Reflection Segment (Null if not defined.)
   * 
   * @return The next Bottom-Side Reflection Segment (Null if not defined.)
   */
  public RaySegment getNextDirectionChangeSegment()
  {
  	return nextDirctnChngSegment;
  }

	/**
	 * Retrieve the azimuth in radians observed at receiver looking along the ray toward the
	 * source.
	 * 
	 * @return double
	 */
	public double getAzimuth()
	{
		double az;
		RaySegment segment = lastActiveSegment;
		while (segment != firstActiveSegment.prevActiveSegment)
		{
			az = segment.getAzimuth();
			if (!Double.isNaN(az))
				return az;
			segment = segment.prevActiveSegment;
		}
//		for (int i = branchSegments.size() - 1; i >= 0; --i)
//		{
//			if (!branchSegments.get(i).ignoreSegment)
//			{
//				az = branchSegments.get(i).getAzimuth();
//				if (!Double.isNaN(az))
//					return az;
//			}
//		}
		return Globals.NA_VALUE;
	}

	/**
	 * Retrieve the azimuth observed at source looking along the ray toward the
	 * receiver.
	 * 
	 * @return double
	 */
	public double getBackAzimuth()
	{
		double az;
		RaySegment segment = firstActiveSegment;
		while (segment != lastActiveSegment.nextActiveSegment)
		{
			az = segment.getBackAzimuth();
			if (!Double.isNaN(az))
				return az;

			segment = segment.nextActiveSegment;
		}
//		for (RaySegment segment : branchSegments)
//		{
//			if (!segment.ignoreSegment)
//			{
//				az = segment.getBackAzimuth();
//				if (!Double.isNaN(az))
//					return az;
//			}
//		}
		return Globals.NA_VALUE;
	}

  /**
   * Calculates the branch travel time.
   * 
   * @return The branch travel time.
   * @throws GeoTessException
   */
  public double getTravelTime() throws GeoTessException
  {
		if (travelTime == Globals.NA_VALUE)
		{
			travelTime = 0;
			pathLength = 0;
			nPoints = 1;
			RaySegment currentSegment = firstActiveSegment;
      while (currentSegment != lastActiveSegment.nextActiveSegment)
      {
				if (!currentSegment.isFixedReflection())
				{
				  travelTime += currentSegment.getSegmentTravelTime();
				  pathLength += currentSegment.getPathLength();
				  nPoints += currentSegment.size() - 1;
				}
				currentSegment = currentSegment.nextActiveSegment;
      }
//			
//			for (int i = 0; i < branchSegments.size(); ++i)
//			{
//				RaySegment segment = branchSegments.get(i);
//				if (!segment.isFixedReflection() && !segment.ignoreSegment)
//				{
//				  travelTime += segment.getSegmentTravelTime();
//				  pathLength += segment.getPathLength();
//				  nPoints += segment.size() - 1;
//				}
//			}
		}

		return travelTime;
  }

  /**
   * Doubles the node density along the branch.
   * 
   * @throws GeoTessException
   */
  private void doubleNodes() throws GeoTessException
  {
		double dxMax = getPointSpacing() / 2;
		doubleNodes(dxMax);
  }

  /**
   * Doubles the node density along the branch if the node spacing in a segment
   * exceeds the input limit.
   * 
   * @param maxSpacing The maximum node spacing above which a segments node
   *                   count is doubled.
   * @throws GeoTessException
   */
  private void doubleNodes(double maxSpacing) throws GeoTessException
  {
		RaySegment currentSegment = firstActiveSegment;
    while (currentSegment != lastActiveSegment.nextActiveSegment)
    {
    	currentSegment.doubleNodesBend(maxSpacing);
			currentSegment = currentSegment.nextActiveSegment;
    }

//		for (int i = 0; i < branchSegments.size(); ++i)
//		{
//			RaySegment segment = branchSegments.get(i);
//			if (!segment.ignoreSegment)
//			  segment.doubleNodesBend(maxSpacing);
//		}
		owningRay.setStatus(RayStatus.DOUBLED);
		travelTime = Globals.NA_VALUE;
		pathLength = Globals.NA_VALUE;
		nPoints = Integer.MIN_VALUE;
  }

	/**
	 * Search each node along the branch except the first. Transform the position of
	 * the node into the coordinate system where x and y are in the plane of
	 * branch from the last point to the first point and z points out of plane with z
	 * z positive pointing in direction of first point cross last point. Return
	 * the z value with the largest absolute value.
	 * 
	 * @return double Out-Of-Plane component.
	 */
  protected double getOutOfPlane()
  {
  	return getOutOfPlane(greatCircle);
  }

	/**
	 * Search each node along the branch except the first. Transform the position of
	 * the node into the coordinate system where x and y are in the plane of the 
	 * input GreatCircle, gc, and z points out of plane with z positive pointing
	 * in the direction of the first gc point cross the last gc point. Return the
	 * z value with the largest absolute value, in km.
	 *
	 * @param gc The great circle with which the reported out-of-plane component
	 *           is measured. 
	 * @return Out-Of-Plane component relative to the input great circle, in km.
	 */
  protected double getOutOfPlane(GreatCircle gc)
  {
		double z, zmax = 0.;
		double[] workSpace = new double[3];
		RaySegment currentSegment = firstActiveSegment;
		while (currentSegment != lastActiveSegment.nextActiveSegment)
		{
			z = currentSegment.getOutOfPlane(gc, workSpace);
			if (abs(z) > abs(zmax))
				zmax = z;
			
			currentSegment = currentSegment.nextActiveSegment;
		}
//		for (int i = 0; i < branchSegments.size(); ++i)
//		{
//			RaySegment rs = branchSegments.get(i);
//			if (!rs.ignoreSegment)
//			{
//				z = rs.getOutOfPlane(gc, workSpace);
//				if (abs(z) > abs(zmax))
//					zmax = z;
//			}
//		}
		return zmax;
  }

  /**
   * Returns the number of points defining this branch.
   * 
   * @return The number of points defining this branch.
   */
  public int getNPoints()
  {
  	return nPoints;
  }

  public double getPathLength()
  {
  	return pathLength;
  }

  /**
	 * Returns the average point spacing on the branch, in km.
   * 
   * @return The average point spacing on the branch, in km.
   */
	public double getPointSpacing()
	{
		return pathLength / nPoints;
	}

	/**
	 * Returns the number of segments that define this branch.
	 * 
	 * @return The number of segments that define this branch.
	 */
  public int size()
  {
  	return activeSegmentCount;
  }

  /**
   * Returns the first segment index in the owning ray segment list.
   * 
   * @return The first segment index in the owning ray segment list.
   */
  public int getFirstSegmentIndex()
  {
  	return 0;
  }

  /**
   * Returns the last segment index in the owning ray segment list.
   * 
   * @return The last segment index in the owning ray segment list.
   */
  public int getLastSegmentIndex()
  {
  	return activeSegmentCount-1;
  }

  /**
   * Returns the angular distance in degrees between the first and last node of
   * this branch.
   * 
   * @return The angular distance in degrees between the first and last node of
   *         this branch.
   */
  public double angleDegrees()
  {
  	return getFirstSegment().getFirst().distanceDegrees(getLastSegment().getLast());
  }

  /**
   * Same as size except nPoints is checked for assignment before return.
   * 
   * @return Total number of nodes defining this branch.
   */
  public int nodeCount()
  {
  	if (nPoints == Integer.MIN_VALUE)
  	{
	  	nPoints = 0;
	  	RaySegment currentSegment = firstActiveSegment;
	  	while (currentSegment != lastActiveSegment.nextActiveSegment)
	  	{
	  		  nPoints += currentSegment.size();
	  		  currentSegment = currentSegment.nextActiveSegment;
	  	}
//	  	for (int i = 0; i < branchSegments.size(); ++i)
//	  		if (!branchSegments.get(i).ignoreSegment)
//	  		  nPoints += branchSegments.get(i).size();
  	}
  	
	  return nPoints;
  }

  /**
   * Sets the branch index.
   * 
   * @param index The new branch index.
   */
  protected void setBranchIndex(int index)
  {
  	branchIndex = index;
  }

  /**
   * Returns the branch index.
   * 
   * @return The branch index.
   */
  public int getBranchIndex()
  {
  	return branchIndex;
  }

  /**
   * Returns true if this branch is a RayBranchBottom.
   * 
   * @return True if this branch is a RayBranchBottom.
   */
  public boolean isBottomBranch()
  {
  	return isBottomBranch;
  }
//
//  protected void addBranchSegments(ArrayList<RaySegment> segments)
//  {
//  	for (int i = 0; i < segments.size(); ++i)
// 		{
//  		RaySegment rs = segments.get(i);
//  		rs.setSegmentIndex(branchSegments.size());
//  		branchSegments.add(rs);
// 		}
//  }

  /**
   * Adds a new segment to the branch segment list. This is always called by
   * the branch that creates the segment. Some branches (upgoing and downgoing)
   * can be high level branches in a Ray branch list, or lower level branches
   * that are part of another superior branch (e.g. RayBranchBottom and
   * RayBranchBottomFixedReflection both use upgoing and downgoing branches as
   * part of their definition). If a branch is owned by a superior branch then
   * the superior branch must call the addBranchSegments(...) method above to
   * add the inferior branch segments to the superior branch segment list
   * which will also set the segment index (not the branch segment index) of
   * each segment to reflect its position in the superior branch's segment list.
   * 
   * @param rs The new segment added to the branch segment list.
   */
	protected void addBranchSegment(RaySegment rs)
	{
		if (firstInitialSegment == null)
			firstInitialSegment = firstActiveSegment = rs;

		++initialSegmentCount;
		++activeSegmentCount;

		lastInitialSegment = lastActiveSegment = rs;

		if (owningBranch != null)
			owningBranch.addBranchSegment(rs);

		//rs.setSegmentIndex(initialSegmentList.size());
		//initialSegmentList.add(rs);
		//activeSegmentList.add(rs);
		
		//rs.setBranchSegmentIndex(branchSegments.size());
		//rs.setSegmentIndex(branchSegments.size());
		//branchSegments.add(rs);
	}

	protected RaySegment getBranchSegment(int i)
	{
		RaySegment currentSegment = firstActiveSegment;
		int k = 0;
		while (k < i)
		{
			currentSegment = currentSegment.nextActiveSegment;
			++k;
		}
		return currentSegment;
		//return branchSegments.get(i);
	}
//
//	protected ArrayList<RaySegment> getBranchSegments()
//	{
//		return branchSegments;
//	}

	protected int getNSegments()
	{
		return activeSegmentCount;
		//return branchSegments.size();
	}

	protected RaySegment getFirstSegment()
	{
		return firstActiveSegment;
		//return branchSegments.get(0);
	}

	protected RaySegment getLastSegment()
	{
		return lastActiveSegment;
		//return branchSegments.get(branchSegments.size() - 1);
	}

	/**
	 * Performs no function. Overridden by RayBranchBottom to actually determine
	 * the current RayType status.
	 * 
	 * @throws GeoTessException
	 */
  protected void evaluateRayType() throws GeoTessException
  {
  	// overridden by RayBranchBottom
  }

	/**
	 * Returns a linked list of segment nodes lying  between the input nodes
	 * previous and next in an equally spaced manner. If the spacing between
	 * previous and next is less than 1.5 * maxInitNodeLayerSeparation then the
	 * list returns with previous and next as the only entries. Otherwise,
	 * previous and next are the first and last members of the returned list and
	 * an equally spaced set of approximately the distance between previous and
	 * next divided by maxInitNodeLayerSeparation are returned.
	 * 
	 * @param previous The first node in the returned list.
	 * @param next     The last node in the returned list.
	 * @return The list of equally spaced segment nodes between previous and next.
	 * @throws GeoTessException
	 */
  protected static LinkedList<GeoTessPosition> addInitialSegmentNodes
	                 (GeoTessPosition previous, GeoTessPosition next,
	                	int minAdditionalNodes)
			   throws GeoTessException
	{
		LinkedList<GeoTessPosition> nodes = new LinkedList<GeoTessPosition>();
		nodes.add(previous);

		// determine if this is the down going (source to bottom) or upgoing
		// (bottom to receiver) portion of the ray

		double thkness = previous.getRadius() - next.getRadius();
		int intfcIndx = previous.getIndex();
    if (thkness < 0.0)
    {
    	thkness = -thkness;
    	intfcIndx = next.getIndex();
    }

    // add intermediate nodes if the layer is thicker than
		// maxLayerNodeSeparation

		//double maxLayerNodeSeparation = getInitialRayLayerNodeSeparation(intfcIndx);
    //int nnodes = Math.max((int) (thkness / maxLayerNodeSeparation), minAdditionalNodes + 1);
		int nnodes = 2;
		if (intfcIndx == 1) nnodes = 3;
    if (nnodes > 0)
    {
    	//System.out.println("prev rad = " + previous.getRadius());
      //if (thkness - nnodes * maxLayerNodeSeparation >
      //   maxLayerNodeSeparation / 2) ++nnodes;
      for (int k = 1; k < nnodes; ++k)
      {
      	GeoTessPosition intrmdte = previous.deepClone();
      	//intrmdte.setIntermediatePosition(previous, next, (double) k / nnodes);
      	intrmdte.setIntermediateUnitVectorPosition(previous, next, (double) k / nnodes);
      	intrmdte.setIndex(intfcIndx);
      	//System.out.println("k(" + k + ") rad = " + intrmdte.getRadius());
      	nodes.add(intrmdte);
      }
    	//System.out.println("next rad = " + next.getRadius());
    }

		nodes.add(next);
		return nodes;
	}

	/**
	 * clears the initial ray layer node separation map.
	 */
	public static void clearInitialRayLayerNodeSeparation()
	{
		synchronized(RayBranch.class)
		{
		  initialRayLayerNodeSeparationDistance.clear();
		}
	}

	/**
	 * Adds a node separation distance (initial) associated with the input layer
	 * index.
	 * 
	 * @param layerIndex The layer index to be associated with an initial node
	 *                   separation distance.
	 * @param separation The initial node separation distance associated with the
	 *                   input layer index.
	 */
  public static void addInitialRayLayerNodeSeparation(int layerIndex, double separation)
  {
		synchronized(RayBranch.class)
		{
  	  initialRayLayerNodeSeparationDistance.put(layerIndex, separation);
		}
  }

  /**
   * Returns the initial node separation distance for the input layer index.
   * If not defined for the input layer index, then the EARTH_RAD (Earth radius)
   * constant is returned.
   * 
   * @param layerIndex The layer index for which the initial node separation
   *                   distance is returned.
   * @return The initial node separation distance for the input layer index.
   *         If not defined for the input layer index, then the EARTH_RAD
   *         (Earth radius) constant is returned.
   */
  static protected double getInitialRayLayerNodeSeparation(int layerIndex)
  {
  	Double nodeSep = initialRayLayerNodeSeparationDistance.get(layerIndex);
  	if (nodeSep == null)
  		return EarthConst.EARTH_RAD;
  	else
  		return nodeSep.doubleValue();
  }

  protected void executeSnellsLaw(GeoTessPosition node, RaySegment segment,
  																RayDirection rayDir, boolean ignoreSnellsLaw) throws Exception
  {
//  	if (segment.ray.nodeMovementStatisticsSnellsLaw != null)
//  	{
//  		double[] beforePosition = node.get3DVector().clone();
//			segment.setFitness(evaluateSnellsLaw.SnellsLaw(node, segment, rayDir, ignoreSnellsLaw));
//			segment.ray.nodeMovementStatisticsSnellsLaw.add(Vector3D.distance3D(beforePosition, node.get3DVector()));
//  	}
//  	else

		segment.setFitness(evaluateSnellsLaw.SnellsLaw(node, segment, rayDir, ignoreSnellsLaw));
  	segment.travelTime = Globals.NA_VALUE;
  }

  protected boolean isRayTypeInvalid()
  {
  	return false;
  }

  protected boolean isMiddleOnlyActiveNode() throws GeoTessException
  {
  	return false;
  }

  /**
   * Always returns true. Up-going and Down-going branches override this method
   * and return false if the node next to the required deepest node is deeper
   * (i.e. has smaller radius).
   * 
   * @return true.
   */
  public boolean isValidDepthPhase()
  {
  	return true;
  }

  /**
   * Always returns true. RayBranchBottom overrides this method
   * and returns false if the first segment is a bottom segment and the source
   * node is not the deepest (i.e. has smaller radius).
   * 
   * @return true.
   */
  public boolean isValidRayBottomPhase()
  {
  	return true;
  }

//
//  protected void removeThinSegment(RaySegment thinSeg)
//  {
//    // get owningBranch (this or a RayBranchBottom*)
//
//    RayBranch owningBranch = this;
//  	if (isBottomBranch())
//  		owningBranch = owningRay.rayBranches.get(branchIndex);
//
//  	// update end point node (previous segment last if up going or next segment
//  	// first if down going) and save removed segment in thinSegment map associated
//  	// with its layer id.
//
//  	if (this instanceof RayBranchUpGoing)
//  	{
//  		// upgoing ... set previous segment last node to thin segments last node 
//  		// prevSeg.last = thinSeg.last
//    	RaySegment prevSeg = owningBranch.branchSegments.get(thinSeg.segmentIndex-1);
//  		prevSeg.last = thinSeg.last;
//  		prevSeg.nodes.set(prevSeg.nodes.size()-1, thinSeg.last);
//  	}
//  	else if (this instanceof RayBranchDownGoing)
//  	{
//  		// downgoing ... set next segment first node to thin segments first node 
//  		// nextSeg.first = thinSeg.first
//    	RaySegment nextSeg = owningBranch.branchSegments.get(thinSeg.segmentIndex+1);
//  		nextSeg.first = thinSeg.first;
//  		nextSeg.nodes.set(0, thinSeg.first);
//  	}
//
//  	// create thinSegments map if it does not exist and add thinSeg to map
//
//    if (thinSegments == null) thinSegments = new HashMap<Integer, RaySegment>();
//    thinSegments.put(thinSeg.majorLayerIndex, thinSeg);
//
//  	// update branchSegment and segment indexes and thin remove thinSeg from
//    // the owningBranch segment list.
//
//  	for (int i = thinSeg.segmentIndex+1; i < owningBranch.branchSegments.size(); ++i)
//  	{
//  		owningBranch.branchSegments.get(i).branchSegmentIndex--;
//  		owningBranch.branchSegments.get(i).segmentIndex--;
//  	}
//  	owningBranch.branchSegments.remove(thinSeg.segmentIndex);
//  }

  /**
   * Overridden by RayBranchBottom to return the fastest level. Everyone else
   * simply returns themselves.
   * 
   * @return This RayBranch.
   */
  protected RayBranch getFastestBranchLevel()
  {
  	return this;
  }

  /**
   * Overridden by RayBranchBottom to return the level assigned to the input
   * index.  Everyone else simply returns themselves.
   * 
   * @return This RayBranch.
   */
  protected RayBranch getRayBranchBottomLevel(int level)
  {
  	return this;
  }

  /**
   * This is global check that inner optimization convergence has occurred for
   * this RayBranch and that it's neighboring left/right branches, should they
   * exist, have also converged such that the shared bounce point is not moving.
   * If that is the case and this branch has convergence then true is returned.
   * If this branch has not converged or the fastest level of either shared
   * branch (left/right) have not converged then this branch must continue as
   * the bounce point is still moving.
   * 
   * @return True if this branch and its fastest neighbors, should they exist,
   *         have converged on their inner iterations.
   */
//  private boolean isInnerOptimizationConvergenceFrozen()
//  {
//  	if (innerOptimizationConverged)
//  	{
//  		if (owningRay.bouncePointsFrozen) return true;
//      if ((nextDirctnChngSegment == null) ||
//          (owningRay.rayBranches.get(branchIndex+1).getFastestBranchLevel().innerOptimizationConverged))
//      {
//        if ((prevDirctnChngSegment == null) ||
//            (owningRay.rayBranches.get(branchIndex-1).getFastestBranchLevel().innerOptimizationConverged))
//          return true;
//      }
//  	}
//
//  	return false;
//  }

  /**
   * This is global check that outer optimization convergence has occurred for
   * this RayBranch and that it's neighboring left/right branches, should they
   * exist, have also converged such that the shared bounce point is not moving.
   * If that is the case and this branch has convergence then true is returned.
   * If this branch has not converged or the fastest level of either shared
   * branch (left/right) have not converged then this branch must continue as
   * the bounce point is still moving.
   * 
   * @return True if this branch and its fastest neighbors, should they exist,
   *         have converged on their outer iterations.
   */
//  private boolean isOuterOptimizationConvergenceFrozen()
//  {
//  	if (outerOptimizationConverged)
//  	{
//  		if (owningRay.bouncePointsFrozen) return true;
//      if ((nextDirctnChngSegment == null) ||
//          (owningRay.rayBranches.get(branchIndex+1).getFastestBranchLevel().outerOptimizationConverged))
//      {
//        if ((prevDirctnChngSegment == null) ||
//            (owningRay.rayBranches.get(branchIndex-1).getFastestBranchLevel().outerOptimizationConverged))
//          return true;
//      }
//  	}
//
//  	return false;
//  }
//
//  protected void inactivateDownGoingSuperCrustalSegments(int minSuperCrustalLayer)
//  {
//		for (int j = 0; j < branchSegments.size(); ++j)
//		{
//			RaySegment rs = branchSegments.get(j);
//			if (rs.majorLayerIndex >= minSuperCrustalLayer)
//				rs.ignoreSegment = true;
//			else
//			{
//				if (j > 0)
//					 branchSegments.get(j).setFirstNode(prevDirctnChngSegment.getLast());
//				break;
//			}
//		}
//  }
//
//  protected void inactivateUpGoingSuperCrustalSegments(int minSuperCrustalLayer)
//  {
//		for (int j = branchSegments.size() - 1; j >= 0; --j)
//		{
//			RaySegment rs = branchSegments.get(j);
//			if (rs.majorLayerIndex >= minSuperCrustalLayer)
//				rs.ignoreSegment = true;
//			else
//			{
//				if (j < branchSegments.size() - 1)
//					branchSegments.get(j).setLastNode(nextDirctnChngSegment.getFirst());
//				break;
//			}
//		}
//  }
//
//  /**
//   * Loops over all down going branchSegments (index 0 to size() - 1) and
//   * restores any super crustal segments (those that lay above CRUST_TOP) to
//   * active status.
//   * 
//   * @throws GeoTessException
//   */
//  protected void activateDownGoingSuperCrustalSegments() throws GeoTessException
//  {
//  	// exit if no inactive segments exist
//  	if (!branchSegments.get(0).ignoreSegment) return;
//  	
//  	// loop over all segments from first to last and find first active segment
//		for (int j = 0; j < branchSegments.size(); ++j)
//		{
//			// get segment and see if it is the first valid segment
//			RaySegment rs = branchSegments.get(j);
//			if (!rs.ignoreSegment)
//			{
//				// rs is the first active segment ... get its first and last node
//				// and process all invalid segments to make them valid again
//	
//				GeoTessPosition frst = rs.getFirst();
//				GeoTessPosition last = rs.getLast();
//
//				// loop over all segments j-1 to first and set new position for the
//				// last node.
//				for (int k = j - 1; k >= 0; --k)
//				{
//					RaySegment rsi = branchSegments.get(k);
//
//					// reset ignore flag and get average radius at segment major layer
//					// index. Use that average to calculate a fractional radius from
//					// the first to the last node.
//
//					rsi.ignoreSegment = false;
//					double ra = frst.getRadiusTop(rsi.last.getIndex());
//					ra += last.getRadiusTop(rsi.last.getIndex());
//					ra /= 2.0;
//					double a = (ra - frst.getRadius()) /
//										 (last.getRadius() - frst.getRadius());
//
//					// Reset the segments last node to an intermediate location along
//					// the first to the last direction and move it to the interface.
//					rsi.last.setIntermediatePosition(frst, last, a, rsi.last.getIndex());
//				}
//
//				// now reset segment rs first node to the segment j-1 last node
//				rs.setFirstNode(branchSegments.get(j-1).last);
//				rs.travelTime = rs.pathLength = Globals.NA_VALUE;
//				rs.getSegmentTravelTime();
//
//				for (int k = j - 1; k >= 0; --k)
//				{
//					RaySegment rsi = branchSegments.get(k);
//					for (int m = 1; m < rsi.nodes.size() - 1; ++m)
//					{
//						GeoTessPosition n = rsi.nodes.get(m);
//						double f = (double) m / (rsi.nodes.size() - 1);
//						n.setIntermediatePosition(frst, last, f);
//					}
//					rsi.travelTime = rsi.pathLength = Globals.NA_VALUE;
//					rsi.getSegmentTravelTime();
//				}	
//
//				// done ... break out of the j loop and exit
//				travelTime = pathLength = Globals.NA_VALUE;
//				getTravelTime();
//				break;
//			}			
//		}
//  }
//
//  /**
//   * Loops over all up going branchSegments (index size() - 1 to 0) and
//   * restores any super crustal segments (those that lay above CRUST_TOP) to
//   * active status.
//   * 
//   * @throws GeoTessException
//   */
//  protected void activateUpGoingSuperCrustalSegments() throws GeoTessException
//  {
//  	// exit if no inactive segments exist
//  	if (!branchSegments.get(branchSegments.size() - 1).ignoreSegment) return;
//  	
//  	// loop over all segments from last to first and find first active segment
//		for (int j = branchSegments.size() - 1; j >= 0; --j)
//		{
//			// get segment and see if it is the first valid segment
//			RaySegment rs = branchSegments.get(j);
//			if (!rs.ignoreSegment)
//			{
//				// rs is the first active segment ... get its first and last node
//				// and process all invalid segments to make them valid again
//	
//				GeoTessPosition frst = rs.getFirst();
//				GeoTessPosition last = rs.getLast();
//				
//				// loop over all segments j+1 to last and set new position for the
//				// first node.
//				for (int k = j + 1; k < branchSegments.size(); ++k)
//				{
//					RaySegment rsi = branchSegments.get(k);
//					
//					// reset ignore flag and get average radius at segment major layer
//					// index. Use that average to calculate a fractional radius from
//					// the first to the last node.
//	
//					rsi.ignoreSegment = false;
//					double ra = frst.getRadiusTop(rsi.first.getIndex());
//					ra += last.getRadiusTop(rsi.first.getIndex());
//					ra /= 2.0;
//					double a = (ra - frst.getRadius()) /
//										 (last.getRadius() - frst.getRadius());
//	
//					// Reset the segments first node to an intermediate location along
//					// the first to the last direction and move it to the interface.
//					rsi.first.setIntermediatePosition(frst, last, a, rsi.first.getIndex());
//				}
//
//				// now reset segment rs last node to the segment j+1 first node
//				rs.setLastNode(branchSegments.get(j+1).first);
//				rs.travelTime = rs.pathLength = Globals.NA_VALUE;
//				rs.getSegmentTravelTime();
//
//				// loop over each segment above rs again and set the intermediate
//				// nodes to positions along the direction between their first and
//				// last nodes of the segment.
//				for (int k = j + 1; k < branchSegments.size(); ++k)
//				{
//					RaySegment rsi = branchSegments.get(k);
//					for (int m = 1; m < rsi.nodes.size() - 1; ++m)
//					{
//						GeoTessPosition n = rsi.nodes.get(m);
//						double f = (double) m / (rsi.nodes.size() - 1);
//						n.setIntermediatePosition(frst, last, f);
//					}
//					rsi.travelTime = rsi.pathLength = Globals.NA_VALUE;
//					rsi.getSegmentTravelTime();
//				}
//
//				// done ... break out of the j loop and exit
//				travelTime = pathLength = Globals.NA_VALUE;
//				getTravelTime();
//				break;
//			} // end if (!rs.ignoreSegment)
//	  } // end for (int j = branchSegments.size() - 1; j >= 0; --j)
//  }

  protected RayBranch getOwningBranch()
  {
  	if (owningBranch != null)
  		return owningBranch;
  	else
  		return this;
  }

  
  // need ability to efficiently remove segment from the active list using
  // layer thickness and layer index
  // need ability to efficiently add segment back to active list using layer
  // thickness only
  
  // if segment is marked for removal
  //   if segment.nextActiveSegment != null
  //      // set the next segments previous pointer to this segments previous
  //      segment.nextActiveSegment.prevActiveSegment = prevActiveSegment
  //   if segment.prevActiveSegment != null
  //      // set the prev segments next pointer to this segments next
  //      segment.prevActiveSegment.nextActiveSegment = nextActiveSegment;
  //
  // now we must set the last node of the previous segment to the first node of
  // the next segment (or the last node of the removed segment)
  protected void resetToInitialNodeDensity()
  {
		travelTime = Globals.NA_VALUE;
		pathLength = Globals.NA_VALUE;
		nPoints = Integer.MIN_VALUE;

		RaySegment rs = firstActiveSegment;
    while (rs != lastActiveSegment.nextActiveSegment)
    {
    	rs.resetToInitialNodeDensity();
    	rs = rs.nextActiveSegment;
  		owningRay.setStatus(RayStatus.INITIALIZED);
    }
  }
}
