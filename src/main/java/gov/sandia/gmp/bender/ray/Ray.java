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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.bender.Bender;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;
import gov.sandia.gmp.bender.BenderConstants.RayStatus;
import gov.sandia.gmp.bender.BenderException;
import gov.sandia.gmp.bender.BenderException.ErrorCode;
import gov.sandia.gmp.bender.phase.PhaseLayerLevelDefinition;
import gov.sandia.gmp.bender.phase.PhaseRayBranchModel;
import gov.sandia.gmp.bender.phase.PhaseWaveTypeTracker;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle.GreatCircleException;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
//import gov.sandia.gmp.util.statistics.Statistic;

/**
 * <p>
 * Ray is used to compute the seismic ray from source to receiver.
 * </p>
 * 
 * <p>
 * The Ray is composed of one or more ray branches that are connected by
 * BOTTOM_SIDE_FIXED_REFLECTION segments. The first point of the first branch is
 * the source point. The last point of the last branch is the receiver. The
 * BOTTOM_SIDE_FIXED_REFLECTION nodes are constant during the Ray construction/
 * optimization(). Bender performs optimization on the BOTTOM_SIDE_FIXED_REFLECTION
 * nodes for multi-branched Rays.
 * 
 * <p>
 * RayBranch types include RayBranchBottom, a standard refracting branch that
 * can also become a diffraction or a reflection; a RayBranchBottomFixedReflection,
 * a downgoing branch followed by an upgoing branch that has a TOP_SIDE_FIXED_REFLECTION
 * segment defined between the downgoing and upgoing branches (e.g. PcP); a
 * RayBranchUpGoing, simply and upgoing component for example the little 'p' in
 * pP; and finally a RayBranchDownGoing, which is a simple downgoing component
 * that doesn't exist other than as a component of a 'bottoming' branch.
 * 
 * <p>
 * If an application wants to visualize a Ray object, it can implement the
 * ChangeListener interface. That interface requires a stateChanged(ChangeEvent
 * changeEvent) method. At several points during the lifetime of a Ray, the Ray
 * object will call visualizer.plot(this), where 'this' is a reference to this
 * Ray object. The visualizer can check the Ray.getStatus() method to decide
 * whether to present a visualization of the ray. The visualizer will have full
 * access to all the public methods of the Ray object to call to get information
 * needed to visualize the Ray.
 * 
 * <p>
 * Applications should not hold on to references to Ray objects for very long
 * since Ray objects maintain references to GeoTessPositions obtained from a
 * GeoTessModel. Better to construct a RayInfo object, which extracts relevant
 * information from a Ray object, but does not maintain any links back to
 * GeoTessModel. This means that RayInfo objects are good candidates for
 * serialization, but Ray objects are not.
 * 
 * <p>
 * Ray is not thread-safe.
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
// @SuppressWarnings("serialized")
public class Ray implements Comparable<Object>
{
	/**
	 * Minimum up-going only angle. If the angle between the vector from the
	 * last point to the first point with the vector from the COE to the last
	 * point is less than this value, then the resulting ray must be an upgoing
	 * only ray.
	 */
	private static final double minUpgoingOnlyAngle = 30.0;

	/**
	 * The Bender object that owns the reference to this Ray object. Mostly useful
	 * for accessing the GeoModelUUL object owned by the Bender. A Ray object
	 * needs access to the GeoModelUUL to get InterpolatedNodeLayered objects.
	 */
	protected Bender																bender;

	/**
	 * A Ray consists of a bunch of RaySegments where each RaySegment spans a
	 * single layer in which slowness if continuous. The nodes that reside on an
	 * interface are shared by the RaySegments above and below the interface. The
	 * nodes on a Ray start from the source and extend toward the receiver.
	 */
	//private ArrayList<RaySegment>										segments	= new ArrayList<RaySegment>();

	/**
	 * Used to determine the wave type (P or S) in each segment of the Ray as it
	 * is constructed.
	 */
	protected PhaseWaveTypeTracker    waveTypeMapper					= null;

	/**
	 * The list of ray branches defined for this ray from the source toward the
	 * receiver.
	 */
	protected ArrayList<RayBranch>    rayBranches             = new ArrayList<RayBranch>();

	/**
	 * The list of ray branch bottoms defined for this ray from the source toward
	 * the receiver.
	 */
	protected ArrayList<RayBranchBottom> rayBranchBottoms     = new ArrayList<RayBranchBottom>();

	/**
	 * Current RayType. Upon exiting this class, the RayType will be one of ERROR,
	 * INVALID, REFLECTION, DIFFRACTION, REFRACTION
	 */
	private RayType								rayType;

	/**
	 * rayDir will be one of UPGOING, DOWNGOING, REFLECTION
	 */
	protected RayDirection				rayDir;

	/**
	 * sourceIsDeep means that the radius of the source is < the radius of the
	 * activeLayer measured at the source. Similar for receiverIsDeep
	 */
	protected boolean							sourceIsDeep, receiverIsDeep;

	/**
	 * The deepest layer in the current branch under construction (not yet added)
	 * to the branch list. This is used by the visualizer.
	 */
	protected int									currentBottomLayer = -1;
	
	/**
	 * Nodes from GeoModel at source and receiver.
	 */
	final private GeoTessPosition	receiver, source;

	/**
	 * The current status of the ray. Will assume one of the following values:
	 * INITIALIZED, INITIAL_RAY, SNELL, BENT, DOUBLED, FINAL_RAY, FASTEST_RAY.
	 */
	private RayStatus							status													= null;

	/**
	 * for diffractions, this is the R/v evaluated at the point where the ray
	 * leaves the interface.
	 */
	//double												horizontalSlowDiffraction;

	/**
	 * travel time of the ray from source to receiver, seconds.
	 */
	protected double							travelTime											= Globals.NA_VALUE;

	/**
	 * Path length of the ray from source to receiver, km.
	 */
	protected double							pathLength											= Globals.NA_VALUE;

	/**
	 * Point spacing of the ray from source to receiver, km.
	 */
	protected double							pointSpacing										= Globals.NA_VALUE;

	/**
	 * Number of points defining the ray from source to receiver.
	 */
	protected int									nPoints													= 0;

	/**
	 * Angular distance from the source to receiver in degrees.
	 */
	protected double							sourceReceiverDistanceDeg				= 0.0;

//	/**
//	 * Used to determine node movement per optimization step.
//	 */
//	protected Statistic nodeMovementStatisticsBend = null;
//	protected Statistic nodeMovementStatisticsSnellsLaw = null;

	/**
	 * Great circle containing source/receiver.
	 */
	GreatCircle                   greatCircle = null;

	/**
	 * Outputs debug information at the beginning of Ray construction if requested.
	 */
	private void outputBenderRayStart()
	{
		if (bender.getVerbosity() > 0)
		{
			bender.println("Ray Evaluation Start " + Globals.repeat("R", 164));
			bender.println("  Ray Phase                      = " +
					           bender.getPhaseRayBranchModel().getSeismicPhase());
			bender.println("  Source-Receiver Distance (deg) = " +
					           sourceReceiverDistanceDeg);
			bender.println("  Ray Branch Count               = " +
					           (bender.getPhaseRayBranchModel().getUndersideReflectionCount() + 1));
			bender.println();
	  }
	}

	/**
	 * Outputs debug information at the end of Ray construction if requested.
	 * 
	 * @throws BenderException
	 * @throws GeoTessException
	 */
	private void outputBenderRayEnd() throws BenderException, GeoTessException
	{
		if (bender.getVerbosity() > 0)
		{
			bender.println("  Ray Travel Time (sec) = " + getTravelTime());
			bender.println("Ray Evaluation End   " + Globals.repeat("R", 164));
			bender.println();
			bender.println();
		}
	}

	/**
	 * Clears the branch and segment lists for this ray.
	 */
	private void clearLists()
	{
		rayBranches.clear();
		rayBranchBottoms.clear();
	}

	/**
	 * Primary constructor to build a ray from scratch and to optimize it's
	 * solution. The ray is composed of one or more branches anchored by the
	 * source and receiver as end points with branches connected by Bottto Side
	 * Fixed Reflection points.
	 * 
	 * @param bender		The Bender object that owns this Ray.
	 * @param receiver	The Ray Receiver end point.
	 * @param source		The Ray Source end point.
	 * @throws Exception
	 */
	public Ray(Bender bender, GeoTessPosition receiver, GeoTessPosition source)
			   throws Exception
	{
		// set up the GreatCircle. receiver on left, source on right, normal
		// points out of plane toward the observer.

		greatCircle = new GreatCircle(receiver.getVector(), source.getVector(), false);
		this.bender = bender;
//		if (bender.isNodeMovementStatisticsOn())
//		{
//			nodeMovementStatisticsBend = new Statistic();
//			nodeMovementStatisticsSnellsLaw = new Statistic();
//		}

		// Define the source and receiver positions

		this.receiver = GeoTessPosition.getGeoTessPosition(getGeoTessModel());
		this.receiver.set(receiver.getVector(), receiver.getRadius());

		GeoTessPosition src = GeoTessPosition.getGeoTessPosition(getGeoTessModel());
		src.set(source.getVector(), source.getRadius());

		sourceReceiverDistanceDeg = src.distanceDegrees(receiver);
		//horizontalSlowDiffraction = Globals.NA_VALUE;

		this.source = src;

		// changeNotifier will notify listeners such as visualization codes, when
		// the status of this Ray changes. This happens everytime setStatus() is
		// called. The following line of code tells the changeNotifier to send a
		// reference to this Ray when instructed to do so.

		bender.getChangeNotifier().setSource(this);

		// build the ray branches ... set the status to the final ray following
		// construction and optimization.

	  outputBenderRayStart();
		buildRayBranches(this.receiver, this.source);

		if (hasBouncePoints())
			optimize();
		else
			rayBranches.get(0).optimize();

//		RayBranchBottomLevels rbbl = ((RayBranchBottom) rayBranches.get(0)).rayBranchBottomLevels;
//		for (int i = 0; i < rbbl.rayBranchBottomLevelList.size(); ++i)
//		{
//			RayBranchBottom rbb = rbbl.rayBranchBottomLevelList.get(i);
//			System.out.print("Level " + rbb.currentLayerLevel + ", doublings " +
//											 rbb.innerLoopDoublingCount);
//			for (int j = 0; j < rbb.branchSegments.size(); ++j)
//			{
//				System.out.print(", seg(" + j + ") bad fitness count " +
//												 rbb.branchSegments.get(j).badFitnessCount);
//			}
//			System.out.println("");
//			rbbl.setCurrentRayBranchBottom(i, RayStatus.FINAL_RAY);
//		}
//	  setStatus(RayStatus.FINAL_RAY);
	  outputBenderRayEnd();
	}

	public void optimizeBranches(double tol, double minNodeSpc) throws Exception
	{
		for (int i = 0; i < rayBranches.size(); ++i)
		{
			rayBranches.get(i).optimize(tol, minNodeSpc);
		}
	}

	/**
	 * Primary constructor to build a ray from scratch and to optimize it's
	 * solution. The ray is composed of one or more branches anchored by the
	 * source and receiver as end points with branches connected by Bottto Side
	 * Fixed Reflection points.
	 * 
	 * @param bender		The Bender object that owns this Ray.
	 * @param receiver	The Ray Receiver end point.
	 * @param source		The Ray Source end point.
	 * @throws Exception
	 */
	public Ray(Bender bender, GeoTessPosition receiver, GeoTessPosition source,
			       boolean fixBP) throws Exception
	{
		fixBouncePoints = fixBP;

		// set up the GreatCircle. receiver on left, source on right, normal
		// points out of plane toward the observer.

		greatCircle = new GreatCircle(receiver.getVector(), source.getVector(), false);
		this.bender = bender;
//		if (bender.isNodeMovementStatisticsOn())
//		{
//			nodeMovementStatisticsBend = new Statistic();
//			nodeMovementStatisticsSnellsLaw = new Statistic();
//		}

		// Define the source and receiver positions

		this.receiver = GeoTessPosition.getGeoTessPosition(getGeoTessModel());
		this.receiver.set(receiver.getVector(), receiver.getRadius());

		GeoTessPosition src = GeoTessPosition.getGeoTessPosition(getGeoTessModel());
		src.set(source.getVector(), source.getRadius());

		sourceReceiverDistanceDeg = src.distanceDegrees(receiver);
		//horizontalSlowDiffraction = Globals.NA_VALUE;

		this.source = src;

		// changeNotifier will notify listeners such as visualization codes, when
		// the status of this Ray changes. This happens everytime setStatus() is
		// called. The following line of code tells the changeNotifier to send a
		// reference to this Ray when instructed to do so.

		bender.getChangeNotifier().setSource(this);

		// build the ray branches ... set the status to the final ray following
		// construction and optimization.

	  outputBenderRayStart();
		buildRayBranches(this.receiver, this.source);

		if (hasBouncePoints())
			optimize();
		else
		{
			rayBranches.get(0).optimize();
			if (!rayBranches.get(0).isBottomBranch && !rayBranches.get(0).isValidDepthPhase())
			{
				rayType = RayType.INVALID;
				throw new BenderException(ErrorCode.NONFATAL, "Error: Upgoing Only Ray has a Bottom \n");
			}
		}

//		RayBranchBottomLevels rbbl = ((RayBranchBottom) rayBranches.get(0)).rayBranchBottomLevels;
//		for (int i = 0; i < rbbl.rayBranchBottomLevelList.size(); ++i)
//		{
//			RayBranchBottom rbb = rbbl.rayBranchBottomLevelList.get(i);
//			System.out.print("Level " + rbb.currentLayerLevel + ", doublings " +
//											 rbb.innerLoopDoublingCount);
//			for (int j = 0; j < rbb.branchSegments.size(); ++j)
//			{
//				System.out.print(", seg(" + j + ") bad fitness count " +
//												 rbb.branchSegments.get(j).badFitnessCount);
//			}
//			System.out.println("");
//			rbbl.setCurrentRayBranchBottom(i, RayStatus.FINAL_RAY);
//		}
//	  setStatus(RayStatus.FINAL_RAY);
	  outputBenderRayEnd();
	}

	/**
	 * Primary constructor to build a ray from scratch and to optimize it's
	 * solution. The ray is composed of one or more branches anchored by the
	 * source and receiver as end points with branches connected by Bottto Side
	 * Fixed Reflection points.
	 * 
	 * @param bender		The Bender object that owns this Ray.
	 * @param receiver	The Ray Receiver end point.
	 * @param source		The Ray Source end point.
	 * @throws Exception
	 */
	public Ray(Bender bender, GeoTessPosition receiver, GeoTessPosition source,
			       double ttTol, double minNodeSpc) throws Exception
	{
		// set up the GreatCircle. receiver on left, source on right, normal
		// points out of plane toward the observer.

		greatCircle = new GreatCircle(receiver.getVector(), source.getVector(), false);
		this.bender = bender;
//		if (bender.isNodeMovementStatisticsOn())
//		{
//			nodeMovementStatisticsBend = new Statistic();
//			nodeMovementStatisticsSnellsLaw = new Statistic();
//		}

		// Define the source and receiver positions

		this.receiver = GeoTessPosition.getGeoTessPosition(getGeoTessModel());
		this.receiver.set(receiver.getVector(), receiver.getRadius());

		GeoTessPosition src = GeoTessPosition.getGeoTessPosition(getGeoTessModel());
		src.set(source.getVector(), source.getRadius());

		sourceReceiverDistanceDeg = src.distanceDegrees(receiver);
		//horizontalSlowDiffraction = Globals.NA_VALUE;

		this.source = src;

		// changeNotifier will notify listeners such as visualization codes, when
		// the status of this Ray changes. This happens everytime setStatus() is
		// called. The following line of code tells the changeNotifier to send a
		// reference to this Ray when instructed to do so.

		bender.getChangeNotifier().setSource(this);

		// build the ray branches ... set the status to the final ray following
		// construction and optimization.

	  outputBenderRayStart();
		buildRayBranches(this.receiver, this.source);

		if (hasBouncePoints())
			optimizeBranches(ttTol, minNodeSpc);
		else
			rayBranches.get(0).optimize();

//		RayBranchBottomLevels rbbl = ((RayBranchBottom) rayBranches.get(0)).rayBranchBottomLevels;
//		for (int i = 0; i < rbbl.rayBranchBottomLevelList.size(); ++i)
//		{
//			RayBranchBottom rbb = rbbl.rayBranchBottomLevelList.get(i);
//			System.out.print("Level " + rbb.currentLayerLevel + ", doublings " +
//											 rbb.innerLoopDoublingCount);
//			for (int j = 0; j < rbb.branchSegments.size(); ++j)
//			{
//				System.out.print(", seg(" + j + ") bad fitness count " +
//												 rbb.branchSegments.get(j).badFitnessCount);
//			}
//			System.out.println("");
//			rbbl.setCurrentRayBranchBottom(i, RayStatus.FINAL_RAY);
//		}
//	  setStatus(RayStatus.FINAL_RAY);
	  outputBenderRayEnd();
	}

	protected boolean bouncePointsFrozen = true;
  protected boolean fixBouncePoints    = false;

	private void optimize() throws Exception
	{

//		optimizeInitialize();
//
//		boolean outLoopDone = false;
//		while (!outLoopDone)
//		{
//			optimizeOuterBeforeInner();
//			while (!optimizeInner(hdr));
//			outLoopDone = optimizeOuterAfterInner(hdr);
//		}
//
//		optimizeFinalize();

	  // optimize ray to convergence
//		bender.setMaximumEarthInterfaceNotThinLayer(EarthInterface.MOHO);
//		if (!fixBouncePoints)
//			bender.addBouncePointIgnoreSnellsLawLayers(bender.getGeoTessModel().getMetaData());
	  optimizeInitialize();

	  boolean outerConverged	= false;
	  boolean firstIteration	= true;
	  int maxFirstInnerIterationCount = 20;
	  setStatus(RayStatus.INITIAL_RAY);
	  while (!outerConverged)
	  {
	  	travelTime = pathLength = Globals.NA_VALUE;
		  optimizeOuterBeforeInner();
	  	optimizeInnerInitialize();

	  	// if this is the first iteration then attempt to converge on inner iteration
		  // without bounce point Snell's law to get the proper ray shapes. If after
	  	// maxFirstInnerIterationCount inner iterations and convergence has not
	  	// been attained it probably as good as it going to get. Reinitialize
	  	// the inner iteration when complete so that it is ready for
		  // the bounce point inner iteration.

	  	if (fixBouncePoints)
	  	{
		  	while (!optimizeInner())
		  	{
		  		if (areFastBranchesInnerConverged())
		  			break;
		  	}
	  	}
	  	else
	  	{
		  	if (firstIteration)
			  {
		  		// this is the first outer iteration ... shape the coarse branches
		  		// for not more than maxFirstInnerIterationCount. If all branches/
		  		// levels, or just the fast levels, converge before that count then
		  		// fine. Reinitialize the inner optimization before reoptimizing with
		  		// moving bounce points

			  	int firstInnerIter = 0;
			  	while (!optimizeInner())
			  	{
		  		  ++firstInnerIter;
			  		if (areFastBranchesInnerConverged() ||
			  				(firstInnerIter == maxFirstInnerIterationCount))
			  			break;
			  	}
			  	
			  	optimizeInnerInitialize();
			  	firstIteration	= false;
			  }

			  // perform bounce point adjustment followed by inner iteration pass
			  // continue until inner iteration converges
		  	boolean innerConverged = false;
		  	int innerIter = 0;
		  	bouncePointsFrozen = false;
		  	while (!innerConverged)
		  	{
			  	boolean flip = optimizeBouncePoints();
			  	innerConverged = optimizeInner();
			  	if ((flip && (innerIter > 100)) || fastRaysConverged)
			  		break;
			  	travelTime = Globals.NA_VALUE;
			  	System.out.println("Total TT = " + getTravelTime());
			  	++innerIter;
		  	}
	  	}

	    // inner iteration completed. Perform branch outer optimization
	    // if even one fast branch fails to converge in the outer iteration then
	    // loop and continue optimization. Otherwise, outerConverged will be
	    // true (or fast levels will be) and the ray calculation will be complete.
	  	// Calculate the ray travel time to reset travelTime, pathLength and
	  	// nPoints. These are used in the optimizeOuterAfterInner() branch
	  	// calculation to evaluate if segment nodes should be doubled.
	  	// Recalculate travel time on return to account for node doubling reducing
	  	// the point spacing.

	  	getTravelTime();
	  	if (optimizeOuterAfterInner() ||
	  			areFastBranchesOuterConverged()) break;
	  	getTravelTime();

//	  	if (!fixBouncePoints &&
//	  			(bender.removeThinLayers() || bender.addThickLayers()))
//	  		checkThinLayers(bender.removeThinLayers(), bender.addThickLayers());

	  	// If the point spacing gets to small then the ray is not converging.
	    // throw an error if max outer convergence is more than 3 times the
	  	// requested value. Otherwise, exit.

	  	if (getPointSpacing() < 0.5)
	  	{
	  		double dt_outer = maxBranchOuterConvergence();
	  		if (dt_outer > 3.0 * bender.getTravelTimeConvergenceTolerance())
	  			throw new BenderException(ErrorCode.FATAL,
	  					"Ray did not converge (dt_outer = " + dt_outer +
	  					") ... and the ray point spacing is < .5 km ... \n");
	  		else
	  			break;
	  	}
	  }

	  // finish and exit

	  optimizeFinalize();
	}

	//***************************************************************************
	// Map<int Layer, boolean ignoreSnellsLaw>
	//   if a node is defined on a layer boundary that has an ignore boolean that is true
	//     and if that node is a member of an upgoing or downgoing branch connected
	//     to a bounce point, then
	//
	//       after getting node above and node below set the node on the boundary
	//       between the node above and node below and exit
	//
	//   SnellsLaw
	//***************************************************************************
	
	// if (removeBouncePointSuperLayers)
	//   removeBouncePointSuperLayers();
	
	// if (insertRemoveThinLayers ||
	//     (removeBouncePointSuperLayers && (outerIter == 1))
	//   checkForThinLayers();

	// remove top layers   true/false
	// remove thin layers  true/false
	// add    thick layers true/false
	//
	// if rmvTopLyrs is true
	//   all top layers above bender.getMaximumModelNotThinLayer() are removed
	//
	//   if addThickLyrs is false then
	//     top layers are not added back in
	//   else if addThickLyrs is true then
	//     top layers are added back in if they are thick enough
  //
	// if rmvThinLyrs is true
	//   all layers thinner than bender.minlayerThickness are removed
	//   
	//   if addThickLyrs is false then
	//     removed layers are never added back in
	//   else if addThickLyrs is true then
	//     removed layers are added back in if they are thick enough
	//  
//
//	private void checkThinLayers(boolean rmvThinLayers,
//															 boolean addThickLayers) throws GeoTessException
//	{
//		for (int i = 0; i < rayBranches.size(); ++i)
//		{
//			RayBranch rb = rayBranches.get(i);
//			rb.checkThinLayers(rmvThinLayers, addThickLayers);
//		}		
//	}

	private double maxBranchOuterConvergence()
	{
		double maxOuterConvergence = 0.0;
		for (int i = 0; i < rayBranches.size(); ++i)
		{
			RayBranch rb = rayBranches.get(i);
			if (maxOuterConvergence < rb.getOuterOptimizationConvergence())
				maxOuterConvergence = rb.getOuterOptimizationConvergence();
		}
		
		return maxOuterConvergence;
	}

	protected boolean areFastBranchesOuterConverged()
	{
		for (int i = 0; i < rayBranches.size(); ++i)
			if (!rayBranches.get(i).outerOptimizationConverged) return false;

		return true;
	}


	protected boolean areFastBranchesInnerConverged()
	{
		for (int i = 0; i < rayBranches.size(); ++i)
			if (!rayBranches.get(i).innerOptimizationConverged) return false;

		return true;
	}
	
	// issues
	//   must stop bending from placing nodes into majorLayerIndex
	//   node must use slowness evaluated from majorLayerIndex but node can
	//   move above that layer if its top node (first or last) is > than
	//   majorLayerIndex
	//
	//   If pointDensity on ray exceeds some limit return ray as "caveated valid"
	//   where error message or log indicated final outer convergence tolerance.
	
	//   Allow increasing of inner and outer tolerance if rays have difficulty
	//   converging
	//     for inner use m*dtol_inner based on convergence count
	//     for outer use f(pointDensity)*dtol_outer
	//        f(pd > minPd) then f = 1
	//        f(pd < absMinPd) then f = maxF (like 3)
	//        f(in between) f = int maxf * (pd - absMinPd)/(minPd - absMinPd)
	
//	
//	private void activateSuperCrustalLayers() throws Exception
//	{
//		for (int i = 0; i < rayBranches.size(); ++i)
//		{
//			RayBranch rb = rayBranches.get(i);
//			if (i > 0)
//				rb.activateDownGoingSuperCrustalSegments();
//
//			if (i < rayBranches.size() - 1)
//				rb.activateUpGoingSuperCrustalSegments();
//		}
//
//		// reevaluate travel time
//		travelTime = pathLength = Globals.NA_VALUE;
//		getTravelTime();
//	  setStatus(RayStatus.OUTER_LOOP);
//	}

//	private void inactivateSuperCrustalLayers() throws Exception
//	{
//		int minSuperCrustalLayer = bender.getMaximumModelNotThinLayer();
//		for (int i = 0; i < rayBranches.size(); ++i)
//		{
//			RayBranch rb = rayBranches.get(i);
//
//			if (i > 0)
//				rb.inactivateDownGoingSuperCrustalSegments(minSuperCrustalLayer);
//
//			if (i < rayBranches.size() - 1)
//				rb.inactivateUpGoingSuperCrustalSegments(minSuperCrustalLayer);
//		}
//	}

	private void optimizeInitialize()
	{
	  for (int i = 0; i < rayBranches.size(); ++i)
	  	rayBranches.get(i).optimizeInitialize();
	}

	private void optimizeOuterBeforeInner() throws BenderException
	{
	  for (int i = 0; i < rayBranches.size(); ++i)
	  	rayBranches.get(i).optimizeOuterBeforeInner();
	}

	private void optimizeInnerInitialize()
	{
	  for (int i = 0; i < rayBranches.size(); ++i)
	  	rayBranches.get(i).optimizeInnerInitialize();
	}

	private boolean optimizeInner() throws Exception
	{
		boolean innerConverged = true;
	  for (int i = 0; i < rayBranches.size(); ++i)
	  	if (!rayBranches.get(i).optimizeInner())
		    innerConverged = false;
	  
	  return innerConverged;
	}

	private boolean optimizeOuterAfterInner() throws Exception
	{
		boolean outerConverged = true;
	  for (int i = 0; i < rayBranches.size(); ++i)
	  	if (!rayBranches.get(i).optimizeOuterAfterInner())
		    outerConverged = false;
	  
	  return outerConverged;
	}

	private void optimizeFinalize()
	{
	  for (int i = 0; i < rayBranches.size(); ++i)
	  	rayBranches.get(i).optimizeFinalize();
	}
	
	private ArrayList<EvaluateSnellsLaw> bouncePointSnellsLaw = null;

	/**
	 * Distance of last bouncePoint move (km)
	 */
	private double[] lastMove   = null;

	/**
	 * Distance of first bouncePoint move (km)
	 */
	private double[] firstMove   = null;
	
	/**
	 * Initial 3D position of each bounce point
	 */
	private double[][] firstPos = null;
	
	/**
	 * Last 3D position of each bounce point
	 */
	private double[][] lastPos = null;

	/**
	 * Total distance moved of each bounce point (km)
	 */
	private double[] totalDist  = null;

	protected int[]  moveCount  = null;

	protected boolean fastRaysConverged = false;
	
	protected void outputBouncePoints()
	{
		// branch lat, lon, interface, last move, total move
	}

	protected boolean isBouncePointStillMoving(int i)
	{
		if ((firstMove == null) || (firstMove[i] == 0.0) ||
				((Math.abs(lastMove[i] / firstMove[i]) > 0.005) &&
				 (Math.abs(lastMove[i]) > 0.003)))
			return true;
		else
			return false;
	}

	protected boolean optimizeBouncePoints() throws Exception
	{
		// fill bounce point eevaluator list if this is the first use.

		if (bouncePointSnellsLaw == null)
		{
			moveCount = new int[rayBranches.size() - 1];
			lastMove  = new double[rayBranches.size() - 1];
			firstMove = new double[rayBranches.size() - 1];
			totalDist = new double[rayBranches.size() - 1];
			firstPos  = new double[rayBranches.size() - 1][];
			lastPos   = new double[rayBranches.size() - 1][];
			bouncePointSnellsLaw = new ArrayList<EvaluateSnellsLaw>();
			for (int i = 0; i < rayBranches.size() - 1; ++i)
			{
				RayBranch rb0 = rayBranches.get(i);
				RayBranch rb1 = rayBranches.get(i+1);
				bouncePointSnellsLaw.add(new EvaluateSnellsLaw(rb0.firstPoint,
						 																					 rb1.lastPoint,
						 																					 bender.getSearchMethod()));
				lastMove[i]  = 0.0;
				firstMove[i] = 0.0;
				totalDist[i] = 0.0;
				firstPos[i]  = null;
				lastPos[i]   = null;
			}
		}

		// loop over all bounce points and enforce snells law at each

		boolean flip = false;
		double maxBPMove = 0.0;
		fastRaysConverged = true;
		for (int i = 0; i < rayBranches.size() - 1; ++i)
		{
			// only update if one or both fastest level sharing branches have not converged

			if (!rayBranches.get(i).getFastestBranchLevel().innerOptimizationConverged ||
					!rayBranches.get(i+1).getFastestBranchLevel().innerOptimizationConverged)
			{
				// get next bounce point and its snells law eevaluator.

				EvaluateSnellsLaw bpSnells = bouncePointSnellsLaw.get(i); 
				RaySegmentFixedReflection bpSeg = (RaySegmentFixedReflection)
																					rayBranches.get(i).nextDirctnChngSegment;

				// save pre enforcement position, enforce, and get post-enformcement
				// position
	
				//double[] prevNodePosInit = rayBranches.get(i).lastPoint.get3DVector().clone();
				//double[] nextNodePosInit = rayBranches.get(i+1).firstPoint.get3DVector().clone();
				double[] prePos = bpSeg.getMiddleNode().get3DVector().clone();
				if (firstPos[i] == null) firstPos[i] = prePos;
				bpSeg.setFitness(bpSnells.SnellsLawBouncePoint(bpSeg,
																											 rayBranches.get(i).lastActiveSegment,
																											 rayBranches.get(i+1).firstActiveSegment));
				setStatus(RayStatus.SNELL);
				double[] postPos = bpSeg.getMiddleNode().get3DVector();
				lastPos[i] = postPos;
	
				double dist = Vector3D.distance3D(prePos, postPos);
				if (Vector3D.distance3D(prePos, rayBranches.get(i).firstPoint.get3DVector()) >
						Vector3D.distance3D(postPos, rayBranches.get(i).firstPoint.get3DVector()))
					dist = -dist;
				if (Math.abs(maxBPMove) < Math.abs(dist)) maxBPMove = dist;
				totalDist[i] = Vector3D.distance3D(postPos, firstPos[i]);
				if (lastMove[i] != 0.0)
				{
					if (dist * lastMove[i] < 0.0)
						flip = true;
				}
				lastMove[i] = dist;
				if (firstMove[i] == 0.0) firstMove[i] = dist;
				++moveCount[i];
				fastRaysConverged = false;
			}
		}

		// done ... return 

		return flip;
	}

	public double getBouncePointFitness(int j) throws Exception
	{
		if (bouncePointSnellsLaw == null)
		{
			bouncePointSnellsLaw = new ArrayList<EvaluateSnellsLaw>();
			for (int i = 0; i < rayBranches.size() - 1; ++i)
			{
				RayBranch rb0 = rayBranches.get(i);
				RayBranch rb1 = rayBranches.get(i+1);
				bouncePointSnellsLaw.add(new EvaluateSnellsLaw(rb0.firstPoint,
						 																					 rb1.lastPoint,
						 																					 bender.getSearchMethod()));
		  }
		}
		
		EvaluateSnellsLaw bpSnells = bouncePointSnellsLaw.get(j); 
		RaySegmentFixedReflection bpSeg = (RaySegmentFixedReflection)
																			rayBranches.get(j).nextDirctnChngSegment;
		return bpSnells.SnellsLawBouncePointFitness(bpSeg,
																								rayBranches.get(j).lastActiveSegment,
																								rayBranches.get(j+1).firstActiveSegment);
	}

	public double getBouncePointOutOfPlane(int j)
	{
		return VectorUnit.angleDegrees(greatCircle.getNormal(), rayBranches.get(j).nextDirctnChngSegment.getMiddleNode().getVector()) - 90.0;
	}

	/**
	 * Standard constructor.
	 * 
	 * @param prevRay		Previous Ray from which peircepoints will be extracted to
	 * 									form thisRay.
	 * @param source		Source position.
	 * @param receiver	Receiver position.
	 * @param hdr				Debug header supplied by Bender output to the beginning
	 * 									of each debug output line.
	 * @throws Exception
	 */
	public Ray(Ray prevRay, GeoTessPosition source, GeoTessPosition receiver) throws Exception
	{
		// set up the GreatCircle. receiver on left, source on right, normal
		// points out of plane toward the observer.

		greatCircle = new GreatCircle(receiver.getVector(), source.getVector(), false);
		this.bender = prevRay.bender;
//		if (bender.isNodeMovementStatisticsOn())
//		{
//			nodeMovementStatisticsBend = new Statistic();
//			nodeMovementStatisticsSnellsLaw = new Statistic();
//		}
		
		// set up the receiver and source
		this.receiver = GeoTessPosition.getGeoTessPosition(getGeoTessModel());
		this.receiver.set(receiver.getVector(), receiver.getRadius());

		GeoTessPosition src = GeoTessPosition.getGeoTessPosition(getGeoTessModel());
		src.set(source.getVector(), source.getRadius());

		this.source = src;

		// get the source to receiver distance. 
		sourceReceiverDistanceDeg = src.distanceDegrees(receiver);
		//horizontalSlowDiffraction = Globals.NA_VALUE;

		// changeNotifier will notify listeners such as visualization codes,
		// when the status of this Ray changes. This happens everytime setStatus() is
		// called. The following line of code tells the changeNotifier to
		// send a reference to this Ray when instructed to do so.
		bender.getChangeNotifier().setSource(this);

		// inform listeners that a new Ray has been initialized.

	  outputBenderRayStart();
	  buildRayBranches(prevRay);

		if (hasBouncePoints())
			optimize();
		else
			rayBranches.get(0).optimize();

	  setStatus(RayStatus.FINAL_RAY);
	  outputBenderRayEnd();
	}

	/**
	 * Standard constructor.
	 * 
	 * @param prevRay		Previous Ray from which peircepoints will be extracted to
	 * 									form thisRay.
	 * @param source		Source position.
	 * @param receiver	Receiver position.
	 * @param hdr				Debug header supplied by Bender output to the beginning
	 * 									of each debug output line.
	 * @throws Exception
	 */
	public Ray(Ray prevRay, boolean ignoreBranches) throws Exception
	{
		greatCircle = new GreatCircle(prevRay.receiver.getVector(), prevRay.source.getVector(), false);
		this.bender   = prevRay.bender;
//		if (bender.isNodeMovementStatisticsOn())
//		{
//			nodeMovementStatisticsBend = new Statistic();
//			nodeMovementStatisticsSnellsLaw = new Statistic();
//		}

		receiver = prevRay.receiver.deepClone();
		source   = prevRay.source.deepClone();

		sourceReceiverDistanceDeg = source.distanceDegrees(receiver);
		//horizontalSlowDiffraction = Globals.NA_VALUE;

		// changeNotifier will notify listeners such as visualization codes,
		// when
		// the status of this Ray changes. This happens everytime setStatus() is
		// called.
		// The following line of code tells the changeNotifier to
		// send a reference to this Ray when instructed to do so.
		bender.getChangeNotifier().setSource(this);

		// build the ray and inform listeners of the final ray.

		if (!ignoreBranches)
		{
			outputBenderRayStart();
			buildRayBranches(prevRay);

			if (hasBouncePoints())
				optimize();
			else
				rayBranches.get(0).optimize();

		  setStatus(RayStatus.FINAL_RAY);
		  outputBenderRayEnd();
		}
		else
		{
			waveTypeMapper = new PhaseWaveTypeTracker(bender.getPhaseWaveTypeModel());
			waveTypeMapper.resetIndex();
		}
	}

	/**
	 * Builds a new set of ray branches for this ray. The rays are constructed
	 * PhaseRayBranchModel and PhaseWaveTypeTracker defined by the owning Bender
	 * object.
	 * 
	 * @param receiver The input ray receiver.
	 * @param source	 The input ray source.
	 * @throws Exception
	 */
	protected void buildRayBranches(GeoTessPosition receiver,
                                  GeoTessPosition source)
			      throws Exception
	{
    // set receiver and source interface indexes and make sure they are less
		// than the model surface

		clearLists();

		receiver.setIndex(receiver.getInterfaceIndex());
		if (receiver.getIndex() == receiver.getNLayers())
			throw new BenderException(ErrorCode.NONFATAL,
					"Receiver is shallower than surface of model. \n");

		source.setIndex(source.getInterfaceIndex());
		if (source.getIndex() == source.getNLayers())
			throw new BenderException(ErrorCode.NONFATAL,
					"Source is shallower than surface of model. \n");

		// get ray branch direction change model and create segment wave type tracker

		PhaseRayBranchModel drctnChngModl = bender.getPhaseRayBranchModel();
    waveTypeMapper = new PhaseWaveTypeTracker(bender.getPhaseWaveTypeModel());

		if (drctnChngModl.getFirstPhaseLayerLevelDefinition().getInterfaceLayerTypeName().equals("CRUST_TOP"))
		{
			String[] names = {"MOHO", "MANTLE_TOP"};
			if (source.getIndex() <= drctnChngModl.getPhaseLayerLevelBuilder().getInterfaceIndex(names))
			{
				throw new BenderException(ErrorCode.NONFATAL,
							NL + "Source is deeper than MOHO ...\n");				
			}
		}
    
    // initialize and loop over all ray branches

    GeoTessPosition nextNode = null;
    GeoTessPosition prevNode = source;
    RayDirection prevType = RayDirection.SOURCE;

    RaySegment      nextFixdRefl = null;
    RaySegment      prevFixdRefl = null;
    for (int i = 1; i < drctnChngModl.size(); ++i)
    {
    	RayDirection nextType = drctnChngModl.getRayBranchDirectionChangeType(i);

    	if (nextType == RayDirection.BOTTOM)
    	{
    		// refraction: set up for the next reflection end point and get the
    		// refraction level structure
    		PhaseLayerLevelDefinition levelStructure = drctnChngModl.getRefractionPhaseLayerDefinition(i);
      	++i;
    		nextType = drctnChngModl.getRayBranchDirectionChangeType(i);
    		int nextLayer = drctnChngModl.getRayBranchInterfaceIndex(i);

    		// create the node at the next reflection or set it as the receiver
    		// if the last type is defined

      	if (nextType == RayDirection.RECEIVER)
      		nextNode = receiver;
      	else if (nextType == RayDirection.BOTTOM_SIDE_REFLECTION)
      	{
      		nextNode = getBottomSideReflectionNode(drctnChngModl, nextLayer, i);
      	}
      	else // nextType.equals("TOP_SIDE_REFLECTION")
      	{
      		// throw error ... can't have top side fixed reflection as the
      		// up-going side of a refraction
      	}

      	// test for upgoing only at this point
    		// If the angle between the direction from the first point to the last point
    		// of the branch makes an angle that is less than the minUpgoingOnlyAngle
    		// then set the bottomLayerLevel to the Level of the first point

    		double[] tmp = {0.0, 0.0, 0.0};
    		Vector3D.subtract(tmp, nextNode.get3DVector(), prevNode.get3DVector());
    		VectorUnit.normalize(tmp);
    		double ang = VectorUnit.angleDegrees(tmp, prevNode.getVector());

//    		double ang2 = VectorUnit.angleDegrees(tmp, nextNode.getVector());
//    		double angfl = VectorUnit.angleDegrees(prevNode.getVector(), nextNode.getVector());
//    		double dp      = VectorUnit.dot(tmp, nextNode.getVector());
//    		double dpfl    = VectorUnit.dot(prevNode.getVector(), nextNode.getVector());

    		RayBranch rayBranch = null;
    		if (ang <= minUpgoingOnlyAngle)
    		{
    			// Upgoing only ray.
        	rayBranch = new RayBranchUpGoing(this, prevNode, nextNode);
    		}
    		else
    		{
    			RayBranchBottomLevels rbbl = new RayBranchBottomLevels(this, prevNode, nextNode,
              																									 levelStructure,
              																									 rayBranches.size(),
              																									 rayBranchBottoms.size());
    			rayBranch = rbbl.rayBranchBottomLevelList.get(0);
    		}
        addBranch(rayBranch);

        // set previous transition segment, if it was defined, and calculate
        // next transition segment if one is defined.

        if (prevFixdRefl != null)
        	rayBranch.setPreviousDirectionChangeSegment(prevFixdRefl);

        if (nextType == RayDirection.BOTTOM_SIDE_REFLECTION)
        {
          nextFixdRefl = this.createFixedReflectionSegment(nextType, nextNode, null);
          nextNode = nextFixdRefl.getLast();
          rayBranch.setNextDirectionChangeSegment(nextFixdRefl);
        }
      }
    	else if (nextType == RayDirection.TOP_SIDE_REFLECTION)
    	{
    		int thisLayer = drctnChngModl.getRayBranchInterfaceIndex(i);
      	++i;
    		nextType = drctnChngModl.getRayBranchDirectionChangeType(i);
    		int nextLayer = drctnChngModl.getRayBranchInterfaceIndex(i);

    		// create the node at the next reflection or set it as the receiver
    		// if the last type is defined

      	if (nextType == RayDirection.RECEIVER)
      		nextNode = receiver;
      	else if (nextType == RayDirection.BOTTOM_SIDE_REFLECTION)
      	{
      		nextNode = getBottomSideReflectionNode(drctnChngModl, nextLayer, i);
      	}
      	else // nextType.equals("TOP_SIDE_REFLECTION")
      	{
      		// throw error ... can't have top side fixed reflection as the
      		// up-going side of a refraction
      	}

      	// make the topside reflection branch

        RayBranch rayBranch = new RayBranchBottomFixedReflection(this, prevNode,
        		                                                     nextNode,
        		                                                     thisLayer);
        addBranch(rayBranch);

        // set previous transition segment, if it was defined, and calculate
        // next transition segment if one is defined.

        if (prevFixdRefl != null)
        	rayBranch.setPreviousDirectionChangeSegment(prevFixdRefl);
        
        if (nextType == RayDirection.BOTTOM_SIDE_REFLECTION)
        {
          nextFixdRefl = createFixedReflectionSegment(nextType, nextNode, null);
          nextNode = nextFixdRefl.getLast();
          rayBranch.setNextDirectionChangeSegment(nextFixdRefl);
        }
    	}
    	else if (nextType == RayDirection.BOTTOM_SIDE_REFLECTION)
    	{
    		int nextLayer = drctnChngModl.getRayBranchInterfaceIndex(i);
    		nextNode = getBottomSideReflectionNode(drctnChngModl, nextLayer, i);

      	// this must be an up-going ray ... make sure the previous node radius
      	// is less than the next node radius

      	if (prevNode.getRadius() >= nextNode.getRadius())
      	{
      		String s = NL + "Error: Previous Node Radius of type \"" + prevType +
      				       "\" at Branch index " + i + NL +
      				       "       is Less than the Next Node Radius of type \"" +
      				       nextType + "\" ..." + NL + 
      				       "       (i.e. " + nextType + " is deeper than " + prevType +
      				       ") ...";
      		throw new IOException(s);
      	}

      	// form up-going branch

      	RayBranch rayBranch = new RayBranchUpGoing(this, prevNode, nextNode);
      	addBranch(rayBranch);

        // set previous branch transition segment if it was not "SOURCE"

        if (prevType != RayDirection.SOURCE)
        	rayBranch.setPreviousDirectionChangeSegment(prevFixdRefl);

        // create new bottom side reflection and set next branch transition segment

        nextFixdRefl = createFixedReflectionSegment(nextType, nextNode, null);
        nextNode = nextFixdRefl.getLast();
        rayBranch.setNextDirectionChangeSegment(nextFixdRefl);
    	}
    	else if (nextType == RayDirection.RECEIVER)
    	{
    		nextNode = receiver;

    		RayBranch rayBranch = null;
    		if (prevType == RayDirection.TOP_SIDE_REFLECTION)
    		{
    			if (nextNode.getRadius() <= prevNode.getRadius())
    			{
        		String s = NL + "Error: Next Node Radius of type \"" + nextType +
 				       "\" at Branch index " + i + NL +
 				       "       is Less than the Previous Node Radius of type \"" +
 				       prevType + "\" ...";
 		        throw new IOException(s);
    			}

        	// form up-going branch

        	rayBranch = new RayBranchUpGoing(this, prevNode, nextNode);
    		}
    		else if (prevType == RayDirection.BOTTOM_SIDE_REFLECTION)
    		{
        	if (prevNode.getRadius() <= nextNode.getRadius())
        	{
        		String s = NL + "Error: Previous Node Radius of type \"" + prevType +
        				       "\" at Branch index " + i + NL +
        				       "       Exceeds Next Node Radius of type \"" +
        				       nextType + "\" ...";
        		throw new IOException(s);
        	}

        	// form down-going branch

        	rayBranch = new RayBranchDownGoing(this, prevNode, nextNode);
    		}

      	rayBranch.setPreviousDirectionChangeSegment(prevFixdRefl);
      	addBranch(rayBranch);
    	}

    	prevNode     = nextNode;
    	prevType     = nextType;
    	prevFixdRefl = nextFixdRefl;
    }

    setStatus(RayStatus.INITIAL_RAY);
	}
//
//	/**
//	 * Builds a set of RayBranchBottom objects from the input phase layer level
//	 * definition. The end points of each ray branch bottom are defined to begin
//	 * with prevNode for the beginning and nextNode for their end points.
//	 * 
//	 * @param prevNode				The start node of each constructed RayBranchBottom.
//	 * @param nextNode				The end node of each constructed RayBranchBottom.
//	 * @param levelStructure	The phase layer level structure from which the
//	 * 												level structure is determined.
//	 * @param hdr							A debug header provided by the caller.
//	 * @return								A list of all valid RayBranchBottoms constructed
//	 * 												using the inputs. Invalid RayBranchBottoms are not
//	 * 												included. The list is sorted on travel time from
//	 * 												minimum to maximum. The first entry is the fastest
//	 * 												valid ray branch.
//	 * @throws Exception
//	 */
//	private ArrayList<RayBranchBottom> buildBottomBranchLevelSet(GeoTessPosition prevNode,
//  		                                   GeoTessPosition nextNode,
//  		                                   PhaseLayerLevelDefinition levelStructure,
//  		                                   String hdr) throws Exception
//  {
//		// construct a ray branch bottom using the lowest level defined by the
//		// input level structure. Add the branch to the list if it is valid.
//
//		RayBranchBottom prevBottom = new RayBranchBottom(this, prevNode, nextNode,
//                                                     levelStructure, hdr + ".BL");
//		ArrayList<RayBranchBottom> rayBranchBottomList = new ArrayList<RayBranchBottom>(prevBottom.getLayerLevelCount());
//		if (!prevBottom.isInvalid()) rayBranchBottomList.add(prevBottom);
//
//		// Now loop over all valid levels and construct new RayBranchBottom's for
//		// each level ... add them to the list if they are valid.
//
//		int currentLevel = prevBottom.getCurrentLayerLevel();
//		int lastLevel    = prevBottom.getTopLayerLevel();
//		for (int i = currentLevel+1; i <= lastLevel; ++i)
//		{
//			try
//			{
//				prevBottom = new RayBranchBottom(this, prevBottom, prevNode, nextNode, i, hdr + ".BL");
//				
//				if (!prevBottom.isInvalid()) rayBranchBottomList.add(prevBottom);
//			}
//			catch (BenderException bex)
//			{
//				if (bex.getErrorCode() != ErrorCode.NONFATAL) throw bex;
//			}
//		}
//
//		// look for cases where a reflection is followed immediately by
//		// a diffraction along the same interface. Convert the faster one
//		// to a refraction.
//		for (int i = 1; i < rayBranchBottomList.size(); ++i)
//		{
//			RaySegmentBottom bsegPrev = rayBranchBottomList.get(i - 1).getBottomSegment();
//			RaySegmentBottom bseg     = rayBranchBottomList.get(i).getBottomSegment();
//			if (bsegPrev.rayType == RayType.REFLECTION
//					&& bseg.rayType == RayType.BOTTOM_SIDE_DIFFRACTION
//					&& bsegPrev.getRayInterface() == bseg.getRayInterface())
//			{
//				// we don't think this can happen but were not sure. So were throwing an
//				// error so if it does we can look at it.
//				bender.getErrorMessages().append(String.format(
//						"Ray is invalid because property allowCMBDiffraction is false and "
//						+"the ray diffracts along the CMB.%n"));
//				bender.getErrorMessages().append(String.format("Version = %s%n", Bender.getVersion()));
//
//				bender.getErrorMessages().append(String.format("Receiver = %1.8f, %1.8f, %1.6f%n",
//						receiver.getLatitudeDegrees(), receiver.getLongitudeDegrees(),
//						receiver.getDepth()));
//
//				bender.getErrorMessages().append(String.format("Source = %1.8f, %1.8f, %1.6f%n",
//						source.getLatitudeDegrees(), source.getLongitudeDegrees(),
//						source.getDepth()));
//
//				bender.getErrorMessages().append(String.format("Phase = %s%n",
//							bender.getPhaseRayBranchModel().getSeismicPhase().toString()));
//
//				bender.getErrorMessages().append(String.format("layer = %s%n", bseg.activeLayer.getName()));
//
//				bender.getErrorMessages().append(String.format("distance = %1.6f%n",
//						receiver.distanceDegrees(source)));
//				
//				throw new BenderException(ErrorCode.FATAL,
//																	"Unknown Error?");
////				if (rayBranchBottomList.get(i - 1).getTravelTime() <= rayBranchBottomList.get(i)
////						.getTravelTime())
////					bsegPrev.rayType = RayType.REFRACTION;
////				else
////					bseg.rayType = RayType.REFRACTION;
//			}
//		}
//
//		// check for diffraction or non-interface reflections and remove
//		
//		for (int i = rayBranchBottomList.size()-1; i>=0; --i)
//		{
//			boolean invalid = false;
//			
//			// remove non major interface diffraction and reflections if any
//
//			RaySegmentBottom bseg     = rayBranchBottomList.get(i).getBottomSegment();
//			if (!bseg.getRayInterface().isMajorInterface() &&
//					((bseg.getRayType() == RayType.TOP_SIDE_DIFFRACTION) ||
//					(bseg.getRayType() == RayType.REFLECTION)))
//				invalid = true;
//			
//			if (invalid) rayBranchBottomList.remove(i);
//		}
//
//		// sort the list on travel time
//
//		Collections.sort(rayBranchBottomList);
//
//		// remove all slow rays (rays slower than the fastest ray by the defined
//		// tolerance) from the list
//
//		for (int i = rayBranchBottomList.size() - 1; i > 0; --i)
//			if (rayBranchBottomList.get(i).getTravelTime() -
//					rayBranchBottomList.get(0).getTravelTime() >
//			    BenderConstants.FAST_TRAVELTIME_TOLERANCE)
//				rayBranchBottomList.remove(i);
//
//		// return the list
//
//		rayBranchBottomList.get(0).outputBenderBranchEnd();
//		return rayBranchBottomList;
//  }

	/**
	 * Ray branch builder where the branches are constructed from the templates
	 * provided by the input previous ray.
	 *  
	 * @param prevRay The previous ray whose branches act as templates to form the
	 * 								the new rays branches.
	 * @throws Exception
	 */
	private void buildRayBranches(Ray prevRay)
			    throws Exception
	{
		clearLists();

		waveTypeMapper = new PhaseWaveTypeTracker(bender.getPhaseWaveTypeModel());
		waveTypeMapper.resetIndex();

		// loop over all branches and build initial ray

		GeoTessPosition prevNode = source;
		GeoTessPosition nextNode = null;
		RaySegment      prevTrnsRaySeg = null;
		RaySegment      nextTrnsRaySeg = null;
		for (int i = 0; i < prevRay.rayBranches.size(); ++i)
		{
			RayBranch inputBranch = prevRay.rayBranches.get(i);

			// next node is either the last node (receiver) or it is an underside
			// reflection

			if (i == prevRay.rayBranches.size() - 1)
				nextNode = receiver;
			else
			{
				// get next node clone

				nextNode = prevRay.rayBranches.get(i).lastPoint.deepClone();

				// if bottom side reflection update from the bender vector is requested
				// then reset this nodes position accordingly

				if (bender.updateUndersideReflections())
				{
					double lat = bender.undersideReflectionVector()[2*i];
					double lon = bender.undersideReflectionVector()[2*i+1];
					nextNode.set(nextNode.getIndex(),  lat, lon, 0.0);
					nextNode.setTop(nextNode.getIndex());
				}
			}

			// build the requested branch using the input first and last nodes
			// (i.e. prevNode and nextNode)

			RayBranch newBranch;
			if (inputBranch.branchDirection == RayDirection.BOTTOM)
			{
				// pass in masterLevel and set level from it ... retreive new masterLevel
				// setting on exit (after creation)

				PhaseLayerLevelDefinition plld = ((RayBranchBottom) inputBranch).phaseLayerDefn;
				RayBranchBottomLevels rbbl = new RayBranchBottomLevels(this, prevNode,
																															 nextNode,
																															 plld,
            																									 rayBranches.size(),
            																									 rayBranchBottoms.size());
			  newBranch = rbbl.rayBranchBottomLevelList.get(0);
			}
			else if (inputBranch.branchDirection == RayDirection.TOP_SIDE_REFLECTION)
			{
				newBranch = new RayBranchBottomFixedReflection(this, (RayBranchBottomFixedReflection) inputBranch,
						                                           prevNode, nextNode);
			}
			else if (inputBranch.branchDirection == RayDirection.DOWNGOING)
				newBranch = new RayBranchDownGoing(this, inputBranch, prevNode, nextNode);
			else
				newBranch = new RayBranchUpGoing(this, inputBranch, prevNode, nextNode);
			addBranch(newBranch);

			// set previous bottom side reflection segment if defined

    	if (prevTrnsRaySeg != null)
    		newBranch.setPreviousDirectionChangeSegment(prevTrnsRaySeg);

    	// build the next bottom side reflection segment if defined

    	if (inputBranch.nextDirctnChngSegment != null)
    	{
        RaySegment nextFixdRefl =
        		createFixedReflectionSegment(inputBranch.nextDirctnChngSegment.getRayDirectionChangeType(),
        																 nextNode, null);
        nextNode = nextFixdRefl.getLast();
        newBranch.setNextDirectionChangeSegment(nextFixdRefl);
    	}

    	// set prev* to next* and continue to next branch

			prevNode = nextNode;
			prevTrnsRaySeg = nextTrnsRaySeg;
		}
		
    setStatus(RayStatus.INITIAL_RAY);
	}
	
	public void resetToInitialNodeDensity()
	{
		for (int i = 0; i < rayBranches.size(); ++i)
			rayBranches.get(i).resetToInitialNodeDensity();
	}

//
//	/**
//	 * Used to reset the Ray's segment list to n which is generally the start of
//	 * a new branch. This is necessary to rebuild a RayBranchBottom and multiple
//	 * Levels without modifying previous branches that have been stored in the
//	 * Ray.
//	 * 
//	 * @param n The number of segments to keep in the segemnt list.
//	 */
//	public void resetSegmentCount(int n)
//	{
//		while (segments.size() > n) segments.remove(segments.size() - 1);
//	}

	/**
	 * Returns true if the bottom node layer index of each RayBranchBottom of ray1
	 * is the same as the bottom node layer index of each RayBranchBottom of ray2.
	 * If not false is returned. If the rays have different number of RayBranchBottom
	 * objects then false is alsow returned.
	 * 
	 * @param ray1 The first ray for comparison.
	 * @param ray2 The second ray for comparison.
	 * 
	 * @return True if the bottom node layer index of each RayBranchBottom of ray1
	 *         is the same as the bottom node layer index of each RayBranchBottom
	 *         of ray2.
	 */
	public static boolean areBottomLayersEquivalent(Ray ray1, Ray ray2)
	{
		// compare each ray bottom (must have the same in same order in ray1 and
		// ray2) to make sure the bottom layer index is the same. Return false if
		// any branch bottom node is in a layer different than the other.
		
		if (ray1.rayBranchBottoms.size() != ray2.rayBranchBottoms.size()) return false;
		for (int i = 0; i < ray1.rayBranchBottoms.size(); ++i)
		{
			RayBranchBottom b1 = ray1.rayBranchBottoms.get(i);
			RayBranchBottom b2 = ray2.rayBranchBottoms.get(i);
			if (b1.getBottomSegment().getMiddleNode().getIndex() !=
					b2.getBottomSegment().getMiddleNode().getIndex()) return false;
		}

		return true;
	}

	/**
	 * Used to determine an initial position of a bottom side reflection node as
	 * defined in the PhasRayBranchModel.
	 * 
	 * @param drctnChngModl	The PhaseRayBranchModel for this phase.
	 * @param nextLayer			The Layer on which the bottom side reflection resides.
	 * @param i							The branch index in the PhaseRayBranchModel.
	 * @return							A node position for the bottom side reflection.
	 * @throws GeoTessException
	 */
	private GeoTessPosition getBottomSideReflectionNode(PhaseRayBranchModel drctnChngModl,
			                                                int nextLayer, int i)
			    throws GeoTessException
	{
		if (bender.updateUndersideReflections())
		{
			int usrindx = 2 * drctnChngModl.getBSRIndexFromBMIndex(i);
			double lat = bender.undersideReflectionVector()[usrindx];
			double lon = bender.undersideReflectionVector()[usrindx+1];
		  return createRayLayerNode(source, nextLayer, lat, lon);
		}
		else
		{
		  double[] z = getRotatedUnitVector(drctnChngModl.getFixedReflectionInitialAngleFraction(i));
		  return createRayLayerNode(source, nextLayer, z);
		}
	}

	/**
	 * Rotates a vector from the last position in the great circle toward the first
	 * by the input fraction f and returns the result as a unit vector.
	 * 
	 * @param f The fractional position between the last node toward the first of
	 * 					the great circle definition.
	 * @return	A vector between the last node and the first node of the great
	 * 					circle by a fraction f.
	 */
	private double[] getRotatedUnitVector(double f)
	{
		// rotate from source (last) to receiver (first) by a fraction f of the
		// total angle difference between source and receiver.

		double[] z = {0.0, 0.0, 0.0};
		VectorUnit.rotatePlane(greatCircle.getLast(), greatCircle.getFirst(), f, z);
		return z;
	}

	/**
	 * Adds branches to the Ray. The branch index is set before the branch is
	 * added. This method also adds the branch to the bottom list if it is a
	 * "bottom" branch.
	 * 
	 * @param branch The branch to be added to the Ray Branch list.
	 */
	protected void addBranch(RayBranch branch)
	{
		branch.setBranchIndex(rayBranches.size());
		rayBranches.add(branch);
		if (branch.getRayBranchTypeName().equals("BOTTOM"))
			addBranchBottom((RayBranchBottom) branch);
		currentBottomLayer = -1;
	}

	/**
	 * Adds "bottom" branches to the Ray "bottom" branch list. The branch index
	 * is set before the branch is added.
	 * 
	 * @param branch The branch to be added to the Ray Branch "bottom" list.
	 */
	protected void addBranchBottom(RayBranchBottom branch)
	{
		branch.setBranchBottomIndex(rayBranchBottoms.size());
		rayBranchBottoms.add(branch);
	}

	protected int getNBranches()
	{
		return rayBranches.size();
	}

	protected boolean hasBouncePoints()
	{
		return (rayBranches.size() > 1);
	}

	/**
	 * A static method used to create a new Ray layer interface node. The base
	 * node is used to make a clone for the new node preserving the underlying
	 * model properties. The layer and position of the cloned node are then set
	 * with the remaining inputs.
	 * 
	 * @param baseNode	The base node used as a cloning representative.
	 * @param layer			The new nodes layer interface index.
	 * @param uv				The new nodes unit vector position.
	 * @return	The new node.
	 * @throws GeoTessException
	 */
	public static GeoTessPosition createRayLayerNode(GeoTessPosition baseNode,
			int layer, double[] uv) throws GeoTessException
	{
		//GeoTessPosition layerNode = baseNode.deepClone();
		GeoTessPosition layerNode = GeoTessPosition.getGeoTessPosition(baseNode);
		layerNode.setIndex(layer);
		layerNode.setTop(layerNode.getIndex(), uv);
		return layerNode;
	}

	/**
	 * A static method used to create a new Ray layer interface node. The base
	 * node is used to make a clone for the new node preserving the underlying
	 * model properties. The layer and position of the cloned node are then set
	 * with the remaining inputs.
	 * 
	 * @param baseNode	The base node used as a cloning representative.
	 * @param layer			The new nodes layer interface index.
	 * @param lat				The new nodes latitude (radians).
	 * @param lon				The new nodes longitude (radians).
	 * @return	The new node.
	 * @throws GeoTessException
	 */
	public static GeoTessPosition createRayLayerNode(GeoTessPosition baseNode,
			int layer, double lat, double lon) throws GeoTessException
	{
		//GeoTessPosition layerNode = baseNode.deepClone();
		GeoTessPosition layerNode = GeoTessPosition.getGeoTessPosition(baseNode);
		layerNode.setIndex(layer);
		double[] unitVector = layerNode.getModel().getEarthShape().getVector(lat, lon);
		layerNode.setTop(layerNode.getIndex(), unitVector);
		return layerNode;
	}

	/**
	 * Creates and returns a new RaySegmentFixedReflection object (Bottom-Side or
	 * Top-Side) used to connect two distinct RayBranches together.
	 * 
	 * @param rdc				The Direction (Bottom or Top) of the new fixed reflection
	 *                  segment.
	 * @param prevNode	The previous node representing the last node of the
	 * 									previous branch. This nodes position will be the position
	 * 									of the fixed reflection segment's node.
	 * @return 	A new RaySegmentFixedReflection object used to connect two distinct
	 * 					RayBranches together.
	 * @throws GeoTessException
	 */
	protected RaySegment createFixedReflectionSegment(RayDirection rdc,
			GeoTessPosition prevNode, RayBranch branch) throws GeoTessException
	{
		LinkedList<GeoTessPosition> nodes = new LinkedList<GeoTessPosition>();

		nodes.add(prevNode);
		nodes.add(prevNode.deepClone());
		nodes.add(prevNode.deepClone());
		RaySegmentFixedReflection raySegment;
		if (branch == null)
			raySegment = new RaySegmentFixedReflection(this, branch, nodes,
																								 waveTypeMapper.getCurrentWaveSpeedAttributeIndex(),
																								 rdc);
		else
			raySegment = new RaySegmentFixedReflection(this, branch, nodes,
																								 waveTypeMapper.getCurrentWaveSpeedAttributeIndex(),
																								 rdc, branch.lastInitialSegment);

		return raySegment;
	}
//
//	/**
//	 * Adds the input segment to this ray's segment list.
//	 * 
//	 * @param rs The input segment to be added to this ray's segment list.
//	 */
//	protected void addSegment(RaySegment rs)
//	{
//		rs.setSegmentIndex(branchSegments.size());
//		if (segments.size() > 0)
//		{
//			if (segments.get(segments.size() - 1).getLast() != rs.getFirst())
//			{
//				System.out
//						.println("Error: segment last and first node not identical ...");
//			}
//		}
//		segments.add(rs);
//	}
//
//	/**
//	 * Builds a table of the ray branch structure.
//	 * 
//	 * @return A table of the ray branch structure.
//	 */
//	public String getNodeRelaxationTable()
//	{
//		String hdr = "    ";
//		String title = "Node Relaxation Statistics" ;
//
//		String rowColHdr = "Relaxation Type";
//		String[][] colHdr =
//		{
//		  { "Node",     "Minimum",  "Maximum",  "Mean",     "Standard",  ""},
//		  { "Movement", "Movement", "Movement", "Movement", "Deviation", "RMS"},
//		  { "Count",    "(km)",     "(km)",     "(km)",     "(km)",      "(km)"}
//		};
//
//		Globals.TableAlignment algn = Globals.TableAlignment.CENTER;
//		Globals.TableAlignment[] colAlign = { algn, algn, algn, algn, algn, algn };
//
//		String[][] data = new String[3][];
//		Statistic total = new Statistic();
//		total.add(nodeMovementStatisticsSnellsLaw);
//		total.add(nodeMovementStatisticsBend);
//		Statistic[] statArray = {nodeMovementStatisticsBend, nodeMovementStatisticsSnellsLaw, total};
//		String[] rowHdr = {"Bending", "Snells Law", "Both"};
//		for (int i = 0; i < statArray.length; ++i)
//		{
//			String[] rowData = new String[colAlign.length];
//			rowData[0] = String.format("%10d",  statArray[i].getCount());
//			rowData[1] = String.format("%10.3f",  statArray[i].getMinimum());
//			rowData[2] = String.format("%10.3f",  statArray[i].getMaximum());
//			rowData[3] = String.format("%10.3f",  statArray[i].getMean());
//			rowData[4] = String.format("%10.3f",  statArray[i].getStdDev());
//			rowData[5] = String.format("%10.3f",  statArray[i].getRMS());
//			data[i] = rowData;
//		}
//
//		return Globals.makeTable(hdr, title, rowColHdr, colHdr, colAlign, rowHdr,
//				                     algn, data, 2);
//	}

	/**
	 * Returns the current status of the ray. Will be one of INITIALIZED,
	 * INITIAL_RAY, SNELL, BENT, DOUBLED, FINAL_RAY, FASTEST_RAY, but see
	 * definition of RayStatus for latest definitions.
	 * 
	 * @return RayStatus
	 */
	public RayStatus getStatus()
	{
		return status;
	}

	/**
	 * Set the status of the ray and calls
	 * Bender.changeNotifier.fireStateChanged() to inform listeners
	 * (visualization objects) that status has changed.
	 * 
	 * @param rayStatus The RayStatus.
	 */
	public void setStatus(RayStatus rayStatus)
	{
		status = rayStatus;
		bender.getChangeNotifier().fireStateChanged();
	}

	/**
	 * Retrieve a reference to the Bender object that owns this Ray.
	 * 
	 * @return The Bender object that owns this Ray.
	 */
	public Bender getBender()
	{
		return bender;
	}

	/**
	 * Retrieve a reference to the GeoTessModel object that supports this Ray.
	 * 
	 * @return The GeoTessModel object that supports this Ray.
	 */
	public GeoTessModel getGeoTessModel()
	{
		return bender.getGeoTessModel();
	}

	/**
	 * Retrieve all the RaySegments that constitute this Ray.
	 * 
	 * @return The list of this ray's segments.
	 */
//	protected ArrayList<RaySegment> getSegments()
//	{
//		return segments;
//	}

	/**
	 * Retrieve the ith ray segment defining this ray.
	 * 
	 * @return The ith ray segment defining this ray.
	 */
//	public RaySegment getSegment(int i)
//	{
//		return segments.get(i);
//	}

	public void setInvalid()
	{
		rayType = RayType.INVALID;
	}

	/**
	 * Returns one of INVALID, REFLECTION, DIFFRACTION, REFRACTION
	 * 
	 * @return RayType
	 */
	public RayType getRayType()
	{
		if ((rayType == RayType.ERROR) || (rayType == RayType.INVALID)) return rayType;
		
		rayType = RayType.UNKNOWN;
		if (rayBranches.size() == 1)
		{
			if (rayBranches.get(0) instanceof RayBranchBottom)
				rayType = ((RayBranchBottom) rayBranches.get(0)).getRayType();
			else if (rayBranches.get(0) instanceof RayBranchBottomFixedReflection)
				rayType = RayType.FIXED_REFLECTION;
		}
		else
		{
		  for (int i = 0; i < rayBranches.size(); ++i)
		  {
		  	if (rayBranches.get(i).getRayTypeName().equals("ERROR"))
		  	{
				  rayType = RayType.ERROR;
				  return rayType;
		  	}
				else if (rayBranches.get(i).getRayTypeName().equals("INVALID"))
				{
				  rayType = RayType.INVALID;
				  return rayType;
				}
		  }
		}
		return rayType;
	}

	/**
	 * Appends the RayType string together for all ray branches defining this Ray.
	 * The indivdual branch ray types are separated by ':'.
	 * 
	 * @return The RayType string together for all ray branches defining this Ray.
	 */
	public String getRayTypeString()
	{
		if ((rayBranches.size() == 1) && (rayBranches.get(0).isBottomBranch()))
			return rayBranches.get(0).getRayTypeName();
		else
		{
			String rt = "";
			for (int i = 0; i < rayBranches.size(); ++i)
			{
				rt += rayBranches.get(i).getRayBranchTypeName();
				if (rayBranches.get(i).isBottomBranch())
					rt += "." + rayBranches.get(i).getRayTypeName();
				if (i < rayBranches.size() - 1)
					rt += " : ";
			}
			return rt;
		}
	}

	/**
	 * Returns list of defining branches.
	 * 
	 * @return List of defining branches.
	 */
	public ArrayList<RayBranch> getBranches()
	{
		return rayBranches;
	}

	/**
	 * Returns list of defining "bottom" branches.
	 * 
	 * @return List of defining "bottom" branches.
	 */
	public ArrayList<RayBranchBottom> getBranchBottoms()
	{
		return rayBranchBottoms;
	}

	/**
	 * Retrieve a reference to the receiver.
	 * 
	 * @return InterpolatedNodeLayered
	 */
	public GeoTessPosition getReceiver()
	{
		return receiver;
	}

	/**
	 * Retrieve a reference to the source.
	 * 
	 * @return InterpolatedNodeLayered
	 */
	public GeoTessPosition getSource()
	{
		return source;
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
		for (int i = 0; i < rayBranches.size(); ++i)
		{
			az = rayBranches.get(i).getAzimuth();
			if (!Double.isNaN(az))
				return az;
		}
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
		for (RayBranch branch : rayBranches)
		{
			az = branch.getBackAzimuth();
			if (!Double.isNaN(az))
				return az;
		}
		return Globals.NA_VALUE;
	}

	/**
	 * Search each node along the ray except the first. Transform the position of
	 * the node into the coordinate system where x and y are in the plane of
	 * GreatCircle from receiver to source and z points out of plane with z
	 * positive pointing in direction of source cross receiver. Return the z value
	 * with the largest absolute value.
	 * 
	 * @return double Out-Of-Plane component, in km.
	 */
	public double getOutOfPlane()
	{
		return getOutOfPlane(greatCircle);
	}

	/**
	 * Search each node along the ray except the first. Transform the position of
	 * the node into the coordinate system where x and y are in the plane of the 
	 * input GreatCircle, gc, and z points out of plane with z positive pointing
	 * in the direction of the first gc point cross the last gc point. Return the
	 * z value with the largest absolute value.
	 *
	 * @param gc The great circle with which the reported out-of-plane component
	 *           is measured. 
	 * @return Out-Of-Plane component relative to the input great circle, in km.
	 */
	public double getOutOfPlane(GreatCircle gc)
	{
		double z, zmax = 0.;
		for (RayBranch branch : rayBranches)
		{
			z = branch.getOutOfPlane(gc);
			if (abs(z) > abs(zmax))
				zmax = z;
		}
		return zmax;
	}

	/**
	 * Retrieve the length of the ray, in km. Calculated by summing up the lengths
	 * of all the ray segments.
	 * 
	 * @return double
	 */
	public double getPathLength()
	{
		return pathLength;
	}

	/**
	 * Return the total number of nodes that define the ray. There is only one
	 * node on each internal discontinuity.
	 * 
	 * @return int
	 */
	public int getNPoints()
	{
		return nPoints;
	}

	/**
	 * Retreive the average spacing between points on the ray, in km.
	 */
	public double getPointSpacing()
	{
		return pathLength / nPoints;
	}

	/**
	 * Retrieve a list of the nodes that define the ray. If
	 * duplicateDiscontinuities is false, there will only be one node on each
	 * discontinuity. If true, then there will be two nodes on each interface, one
	 * from segment above and the other from segment below the interface (they
	 * will have different velocity).
	 * 
	 * @param duplicateDiscontinuities
	 *          boolean
	 * @return LinkedList
	 */
	public ArrayList<GeoTessPosition> getNodes(boolean duplicateDiscontinuities)
	{
		int n = 0;
		for (RayBranch branch: rayBranches)
		{
			RaySegment segment = branch.firstActiveSegment;
			while (segment != branch.lastActiveSegment.nextActiveSegment)
			{
				n += segment.size();
				segment = segment.nextActiveSegment;
			}
		}
//			for (RaySegment segment: branch.getBranchSegments())
//				if (!segment.ignoreSegment)
//				  n += segment.size();

		ArrayList<GeoTessPosition> nodes = new ArrayList<GeoTessPosition>(n);
		ListIterator<GeoTessPosition> it;

		if (duplicateDiscontinuities)
		{
			for (int i = 0; i < rayBranches.size(); ++i)
			{
				RayBranch rb = rayBranches.get(i);
				RaySegment segment = rb.firstActiveSegment;
				while (segment != rb.lastActiveSegment.nextActiveSegment)
				{
					it = segment.getNodes().listIterator(0);
					while (it.hasNext())
						nodes.add(it.next());

					segment = segment.nextActiveSegment;
				}
			}
//			
//			for (RayBranch branch: rayBranches)
//				for (RaySegment segment: branch.getBranchSegments())
//				{
//					if (!segment.ignoreSegment)
//					{
//						it = segment.getNodes().listIterator(0);
//						while (it.hasNext())
//							nodes.add(it.next());
//					}
//				}
		}
		else
		{
			// add the first node of the first segment
			nodes.add(rayBranches.get(0).firstActiveSegment.getNodes().getFirst());
			for (int i = 0; i < rayBranches.size(); ++i)
			{
				RayBranch rb = rayBranches.get(i);
				RaySegment segment = rb.firstActiveSegment;
				while (segment != rb.lastActiveSegment.nextActiveSegment)
				{
					it = segment.getNodes().listIterator(1);
					while (it.hasNext())
						nodes.add(it.next());

					segment = segment.nextActiveSegment;
				}
			}
//			for (RayBranch branch: rayBranches)
//			{
//				for (RaySegment segment : branch.getBranchSegments())
//				{
//					if (!segment.ignoreSegment)
//					{
//						it = segment.getNodes().listIterator(1);
//						while (it.hasNext())
//							nodes.add(it.next());
//					}
//				}
//			}
		}

		return nodes;
	}

	/**
	 * Default toString method.
	 */
	@Override
	public String toString()
	{
		return getNodeInformationTable(false);
	}

	/**
	 * Output basic information for each defined ray branch. Outputs the branch
	 * index, branch type, starting transition segment type, starting interface,
	 * ending transition segment type, ending interface, number of segments,
	 * number of nodes, and the angle between the starting node and ending node
	 * in the branch. 
	 *  
	 * @return The branch information table as a formatted string.
	 */
	public String getBranchInformationTable()
	{
		GeoTessMetaData md = bender.getGeoTessModel().getMetaData();

		// make title

		String hdr = "    ";
		String title = "Ray Branch Information Table";

		// make column headers and alignment

		String rowColHdr = "";
		String[][] colHdr = 
			{
				{"Branch", "Branch", "Start",      "Start",     "End",        "End",      "Segment", "Node",  "Angle"},
				{"Index",  "Type",   "Transition", "Interface", "Transition", "Interface", "Count",  "Count", "Distance" },
				{"",       "",       "Segment",    "",          "Segment",    "",          "",       "",      "(deg)"} 
			};
		
  	Globals.TableAlignment algn = Globals.TableAlignment.CENTER;
	  Globals.TableAlignment[] colAlign = new Globals.TableAlignment [colHdr[0].length];
    for (int i = 0; i < colAlign.length; ++i) colAlign[i] = algn;

		// fill data matrix

    String[][] data = new String[rayBranches.size()][];
		for (int i = 0; i < rayBranches.size(); ++i)
		{
			RayBranch branch = rayBranches.get(i);
			String[] rowData = new String[colHdr[0].length];
			rowData[0] = Integer.toString(i);
      rowData[1] = branch.branchDirection.name();

      if (branch.prevDirctnChngSegment == null)
      	rowData[2] = "NA (SOURCE)";
      else
      	rowData[2] = branch.prevDirctnChngSegment.getRayDirectionChangeType().name();
      rowData[3] = md.getLayerName(branch.getFirstSegment().getFirst().getIndex());

      if (branch.nextDirctnChngSegment == null)
      	rowData[4] = "NA (RECIEVER)";
      else
      	rowData[4] = branch.nextDirctnChngSegment.getRayDirectionChangeType().name();
      rowData[5] = md.getLayerName(branch.getLastSegment().getLast().getIndex());

      rowData[6] = Integer.toString(branch.size());
      rowData[7] = Integer.toString(branch.nodeCount());
      rowData[8] = Double.toString(branch.angleDegrees());

			data[i] = rowData;
		}

		// build table and return

		return Globals.makeTable(hdr, title, rowColHdr, colHdr, colAlign, null,
  				                   algn, data, 2);
	}

	/**
	 * Output basic information for each segment defined by this ray. If the
	 * boolean compact is true then the branch index, branch type, segment index,
	 * segment type, start interface name, end interface name, slowness attribute
	 * name, and node count are output. If false, then in addition to the compact
	 * information the segment angle distance, direct distance, path length,
	 * travel time, azimuth, back azimuth, mean point spacing, mean layer
	 * thickness, and out-of-plane amount are also output. 
	 *  
	 * @param compact Output compact form if true.
	 * @return The segment information table as a formatted string.
	 * 
	 * @throws GeoTessException
	 * @throws GreatCircleException
	 */
	public String getSegmentInformationTable(boolean compact) throws GeoTessException
	{
		GeoTessMetaData md = bender.getGeoTessModel().getMetaData();

		// make title

		String hdr = "    ";
		String title = "Ray Segment Information Table";

		// make column headers and alignment

		String rowColHdr = "";
		String[][] colHdr;
		if (compact)
			colHdr = getCompactSegmentInfoTableHeader(); 
		else
			colHdr = getSegmentInfoTableHeader();
			
		Globals.TableAlignment algn = Globals.TableAlignment.CENTER;
		Globals.TableAlignment[] colAlign = new Globals.TableAlignment [colHdr[0].length];
    for (int i = 0; i < colAlign.length; ++i) colAlign[i] = algn;

		// fill data matrix

    
    int nSeg = getNSegments();
    String[][] data = new String[nSeg][];
    int i = -1;
    for (int j = 0; j < rayBranches.size(); ++j)
    	for (int k = 0; k < rayBranches.get(j).getNSegments(); ++k)
    	{
    		++i;
    		RaySegment segment = rayBranches.get(j).getBranchSegment(k);
				
				// set branch index and branch type
	
				int branchIndex;
				String branchType;
				if (segment.branch == null)
				{
					branchIndex = -1; // transition segment
					branchType = "TRANSITION";
				}
				else
				{
					branchIndex = segment.branch.branchIndex;
					if (segment.branch.isBottomBranch && segment.branch.branchDirection != RayDirection.BOTTOM)
						branchType  = "BOTTOM (" + segment.branch.branchDirection.name() + ")";
					else 
	  				branchType  = segment.branch.branchDirection.name();
				}
	
				// create row data and set all compact form data: branch index, branch
				// type, segment index, segment type, start layer name, end layer name,
				// slowness type, and node count
				String[] rowData = new String[colHdr[0].length];
				if (branchIndex == -1)
				  rowData[0] = "NA";
				else
				  rowData[0] = Integer.toString(branchIndex);
				rowData[1] = branchType;
				rowData[2] = Integer.toString(i);
				rowData[3] = segment.getSegmentType().name();
				rowData[4] = md.getLayerName(segment.getFirst().getIndex());
				if (i == 0) rowData[4] += " (SOURCE)";
				rowData[5] = md.getLayerName(segment.getLast().getIndex());
				if (i == nSeg - 1) rowData[5] += " (RECEIVER)";
				rowData[6] = md.getAttributeName(segment.getWaveTypeIndex());
				rowData[7] = Integer.toString(segment.size());
				
				// add additional data if not compact
	
				if (!compact)
				{
					// angle dist, dirct dist, path length, travel time, azimuth, back azimuth, 
					// point spacing, layer thickness, out-of-plane
					
					rowData[8]  = Double.toString(Math.toDegrees(segment.getDistance()));
					rowData[9]  = Double.toString(segment.getDistance3D());
					rowData[10] = Double.toString(segment.getPathLength());
					rowData[11] = Double.toString(segment.getSegmentTravelTime());
					rowData[12] = Double.toString(segment.getAzimuth());
					rowData[13] = Double.toString(segment.getBackAzimuth());
					rowData[14] = Double.toString(segment.getSpacing());
					rowData[15] = Double.toString(segment.getLayerThickness());
					
					double[] tmp = {0.0, 0.0, 0.0};
					rowData[16] = Double.toString(segment.getOutOfPlane(greatCircle, tmp));
				}
	
				data[i] = rowData;
			}

		// build table and return

		return Globals.makeTable(hdr, title, rowColHdr, colHdr, colAlign, null,
  				                   algn, data, 2);
	}

	/**
	 * Returns compact header information used to build the segment information
	 * table (see String getSegmentInformationTable(boolean compact)).
	 * 
	 * @return Segment information header.
	 */
	private String[][] getCompactSegmentInfoTableHeader()
	{
		String[][] colHdr = 
		    {
			  { "Branch", "Branch", "Segment", "Segment", "Start",     "End",       "Wave", "Node"  },
		    { "Index",  "Type",   "Index",   "Type",    "Interface", "Interface", "Type", "Count" } };
		return colHdr;
	}

	/**
	 * Returns header information used to build the segment information table
	 * (see String getSegmentInformationTable(boolean compact)).
	 * 
	 * @return Segment information header.
	 */
	private String[][] getSegmentInfoTableHeader()
	{
		String[][] colHdr = 
		    {
			  { "Branch", "Branch", "Segment", "Segment", "Start",     "End",       "Wave", "Node",  "Angle",       "Direct",     "Path",        "Travel",     "Azimuth", "Back Azimuth", "Point Spacing", "Layer Thickness", "Out-of-Plane" },
		    { "Index",  "Type",   "Index",   "Type",    "Interface", "Interface", "Type", "Count", "Dist. (deg)", "Dist. (km)", "Length (km)", "Time (sec)", "(deg)",   "(deg)",        "(km)",          "(km)",            "(km)"         } };
		return colHdr;
	}

	/**
	 * Produces a table of all segment nodes. If the flag "includeDuplicates" is
	 * true then duplicate interface nodes are included in the table.
	 * 
	 * @param includeDuplicates
	 *          Flag
	 * @return The table of all segment nodes.
	 */
	public String getNodeInformationTable(boolean includeDuplicates)
	{
		GeoTessMetaData md = bender.getGeoTessModel().getMetaData();

		// make title

		String hdr = "    ";
		String title = "Ray Node Information Table";
		int noDupl;
		if (includeDuplicates)
		{
			title += " (with Layer Boundary Duplicates)";
			noDupl = 0;
		}
		else
		{
			title += " (without Layer Boundary Duplicates)";
			noDupl = 1;
		}

		// make column headers and alignment

		String rowColHdr = "";
		String[][] colHdr =
		{
		{ "Entry", "Layer", "Layer", "Segment", "Latitude", "Longitude", "Radius", "Depth" },
		{ "Index", "Name",  "Index", "Index",   " (Deg) ", " (Deg) ", "(km)", "(km)" } };

		Globals.TableAlignment algn = Globals.TableAlignment.CENTER;
		Globals.TableAlignment[] colAlign =
		{ algn, algn, algn, algn, algn, algn, algn, algn };

		// get array of segment node counts

		int[] segmentNodes = new int [getNSegments()];
		int k = 0;
		for (RayBranch branch : rayBranches)
		{
			RaySegment segment = branch.firstActiveSegment;
			while (segment != branch.lastActiveSegment.nextActiveSegment)
			{
				segmentNodes[k++] = segment.size();
        segment = segment.nextActiveSegment;
			}
		}
//			for (RaySegment segment : branch.getBranchSegments())
//				segmentNodes[k++] = segment.size();

		// fill data matrix

		ArrayList<GeoTessPosition> nodes = getNodes(includeDuplicates);
		String[][] data = new String[nodes.size()][];
		int prevSegmentNodeCount = 0;
		int currentSegment = 0;
		for (int i = 0; i < nodes.size(); ++i)
		{
			// set current segment index

			if (i == prevSegmentNodeCount + segmentNodes[currentSegment] - noDupl)
			{
				prevSegmentNodeCount = i;
				++currentSegment;
				if (i == nodes.size() - 1) currentSegment = getNSegments() - 1;
			}
			
			// get node and fill data row

			GeoTessPosition node = nodes.get(i);
			String[] rowData = new String[colHdr[0].length];
			rowData[0] = Integer.toString(i);
			rowData[1] = md.getLayerName(node.getIndex());
			rowData[2] = Integer.toString(node.getIndex());
			rowData[3] = Integer.toString(currentSegment);
			rowData[4] = Double.toString(node.getEarthShape().getLatDegrees(
					                         node.getVector()));
			rowData[5] = Double.toString(node.getEarthShape().getLonDegrees(
					                         node.getVector()));
			rowData[6] = Double.toString(node.getRadius());
			rowData[7] = Double.toString(node.getDepth());
			data[i] = rowData;
		}

		// build table and return

		return Globals.makeTable(hdr, title, rowColHdr, colHdr, colAlign, null,
  				                   algn, data, 2);
	}

	/**
	 * Retrieve the total travel time of this ray, in seconds. Also updates the
	 * values of pathLength and nPoints
	 * 
	 * @return double
	 * @throws GeoModelException
	 * @throws GeoVectorException
	 * @throws BenderException
	 * @throws GeoTessException
	 */
	public double getTravelTime() throws BenderException, GeoTessException
	{
		if (travelTime == Globals.NA_VALUE)
		{
			travelTime = 0;
			pathLength = 0;
			nPoints = 1;

			// if ray ends up being a diffraction, this will be R/velocity at
			// point where ray leaves the diffraction interface.
			//horizontalSlowDiffraction = Globals.NA_VALUE;

//			for (RayBranch branch : rayBranches)
//			  for (RaySegment segment : branch.getBranchSegments())
//				  segment.accumulateRayTravelTime();

			for (int i = 0; i < rayBranches.size(); ++i)
			{
				travelTime += rayBranches.get(i).getTravelTime();
				pathLength += rayBranches.get(i).getPathLength();
				nPoints    += rayBranches.get(i).getNPoints() - 1;
			}
		}

		return travelTime;
	}

	/**
	 * Retrieve the node on this Ray's bottom branches that has the smallest
	 * radius.
	 * 
	 * @return Minimum radius node.
	 */
	public GeoTessPosition getTurningPoint()
	{
		GeoTessPosition minNode = null;
		GeoTessPosition turnNode = null;
//		if (rayBranchBottoms.size() == 0)
//		{
//			for (int i = 0; i < rayBranches.size(); ++i)
//			{
//				RayBranch branch = rayBranches.get(i);
//				GeoTessPosition deepNode = branch.getDeepestRayBranchNode();
//				if ((minNode == null) || (deepNode.getRadius() < minNode.getRadius()))
//	        minNode = deepNode;
//			}
//		}
//		else
//		{
			for (int i = 0; i < rayBranches.size(); ++i)
			{
				RayBranch rb = rayBranches.get(i);
				if (rb instanceof RayBranchBottom)
				{
					RayBranchBottom branch = (RayBranchBottom) rb;
					turnNode = branch.getBottomSegment().getTurningPoint();
				}
				else
				{
					turnNode = rb.getDeepestRayBranchNode();
				}
				
				if ((minNode == null) || (turnNode.getRadius() < minNode.getRadius()))
	        minNode = turnNode;
			}
//		}
		return minNode;
	}

	/**
	 * Retrieve the maximum ray depth (km).
	 * 
	 * @return The maximum ray depth (km).
	 */
	public double getMaxRayDepth()
	{
		double maxDepth = 0.0;
		for (int i = 0; i < rayBranches.size(); ++i)
		{
			double branchMaxDepth = rayBranches.get(i).getMaxRayBranchDepth();
			if (branchMaxDepth < maxDepth) maxDepth = branchMaxDepth;
		}
		return maxDepth;
	}

	/**
	 * Retrieve the index of the deepest layer with which this Ray interacts.
	 * 
	 * @return  The index of the deepest layer with which this Ray interacts.
	 */
	public int getBottomLayer()
	{
		int minLayer = bender.getGeoTessModel().getNLayers() + 1;
		if ((currentBottomLayer != -1) &&
				(currentBottomLayer < minLayer))
			minLayer = currentBottomLayer;
		for (int i = 0; i < rayBranches.size(); ++i)
		{
			int branchBotLayer = rayBranches.get(i).getBottomLayer();
			if (branchBotLayer < minLayer) minLayer = branchBotLayer;
		}
		return minLayer;
	}

	/**
	 * Returns the total segment count maintained by this Ray.
	 * 
	 * @return The total segment count maintained by this Ray.
	 */
	public int getNSegments()
	{
		int n = 0;
		for (int i = 0; i < rayBranches.size(); ++i)
			n += rayBranches.get(i).getNSegments();
		return n;
	}

	/**
	 * For each node on the ray that is not on an interface, accumulate the
	 * following information: <br>
	 * Distance (degrees) <br>
	 * Depth (km) <br>
	 * Angle between ray direction and vertical (degrees) <br>
	 * Velocity (km/sec) <br>
	 * Magnitude of velocity gradient (sec^-^1) <br>
	 * Angle between velocity gradient and vertical (degrees) <br>
	 * Ray Parameter (sec/km)
	 * 
	 * @param property
	 *          int
	 * @throws GeoModelException
	 * @throws GeoVectorException
	 * @return ArrayList
	 * @throws GeoTessException
	 */
	public ArrayList<ArrayList<Double>> getRayParameterInfo(GeoAttributes property)
			throws GeoTessException
	{
		ArrayList<ArrayList<Double>> rayParameters = new ArrayList<ArrayList<Double>>();
		GeoTessPosition origin = rayBranches.get(0).firstActiveSegment.getFirst();
		for (RayBranch branch : rayBranches)
		{
			RaySegment segment = branch.firstActiveSegment;
			while (segment != branch.lastActiveSegment.nextActiveSegment)
			{
			  segment.getRayParameterInfo(rayParameters, origin, property);
        segment = segment.nextActiveSegment;				
			}
		}
//		  for (RaySegment segment : branch.getBranchSegments())
//			  segment.getRayParameterInfo(rayParameters, origin, property);

		return rayParameters;
	}

	/**
	 * Retrieve a list of GeoVectors that are evenly spaced along the ray.
	 * 
	 * @param dkmMax
	 *          double spacing of the desired points, in km. This value will be
	 *          adjusted downward slightly so that an even number of intervals fit
	 *          on the ray
	 * @param samples
	 *          list of InterpolatedNodeLayered objects that are equally spaced
	 *          along the ray.
	 * @param testSamples if true, the new samples are tested to ensure that they are
	 * evenly spaced and that new samples fall on a line connecting two of the original
	 * points.  This is expensive and should only be set to true when developing new
	 * algorithms that use resampling.
	 * @throws Exception 
	 */
	public void resample(double dkmMax, ArrayList<GeoVector> samples, boolean testSamples) throws Exception
	{
		samples.clear();
		
		for (RayBranch branch : rayBranches)
		{
		    RaySegment segment = branch.firstActiveSegment;
		    while (segment != branch.lastActiveSegment.nextActiveSegment)
		    {
			samples.addAll(segment.resample(dkmMax, testSamples));				
			segment = segment.nextActiveSegment;
		    }
		}
	}

	/**
	 * Comparison operator that sorts Rays defined in a collection based on their
	 * travel time (min to max).
	 */
	public int compareTo(Object object)
	{
		try
		{
			if (getTravelTime() < ((Ray) object).getTravelTime())
				return -1;
			if (getTravelTime() > ((Ray) object).getTravelTime())
				return 1;
		}
		catch (Exception ex)
		{
		}
		return 0;
	}

	/**
	 * All branches begin with a source or bottom side reflection, and end with
	 * a receiver or bottom side reflection. If the input branch index is less
	 * than the last branch index, then its last node is the bottom side reflection
	 * location and its radius is returned. Otherwise, -1.0 is returned if the
	 * index is out-of-bounds.
	 * 
	 * @param i The index of the bottom side reflection whose radius is to be returned.
	 * @return The ith bottom-side reflection radius. 
	 */
	public double getBottomSideReflectionRadius(int i)
	{
		if (i < rayBranches.size()-1)
		{
			return rayBranches.get(i).getBranchLastNode().getRadius();
		}
		else
			return -1.0;
	}
}
