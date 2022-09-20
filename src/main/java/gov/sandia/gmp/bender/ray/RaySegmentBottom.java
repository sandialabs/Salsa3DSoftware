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

import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.sin;

import java.util.LinkedList;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.bender.BenderConstants;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;
import gov.sandia.gmp.bender.BenderException;
import gov.sandia.gmp.bender.level.Level;
import gov.sandia.gmp.bender.phase.PhaseLayerLevelDefinition;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * Defines a bottom segment that can REFLECT, DIFFRACT, or REFRACT at its middle
 * point and generall contains upgoing and downgoing nodes all in one segment.
 * This is the old Bender (pre-RayBranch) segment containing the remaining
 * methods that have not been moved down to RaySegement or RaySegmentBend. This
 * class extends RaySegmentBend.
 * 
 * @author jrhipp
 *
 */
public class RaySegmentBottom extends RaySegmentBend
{
	/**
	 * The bottom Level where the segment turns.
	 */
	protected Level rayInterface;
	
	/**
	 * The middle node of the segment.
	 */
	protected GeoTessPosition middle;
	
	/**
	 * The phase layer level definition object that is used to decompose the
	 * layers into finer "layer like" Levels. This is used to ensure that local
	 * minimum are encountered properly while searching for the actual bottom
	 * turning point in the segment.
	 */
	protected PhaseLayerLevelDefinition phaseLayerLevelDefinition;
	
	/**
	 * The current active Level, within which, the turning point is contrained to
	 * reside.
	 */
	protected Level activeLayer;
	
	/**
	 * The previous active Level, just below the active level.
	 */
	protected Level previousActiveLayer;
	
	/**
	 * Set to true if this bottom segment is a reflection.
	 */
	protected boolean isReflection;

	/**
	 * One of REFLECTION, DIFFRACTION, REFRACTION;
	 * Applies only to this segment of the ray.  Set in checkRayType().
	 */
	protected RayType rayType;

	private double[] vPrevious = {0.0, 0.0, 0.0};
	private double[] vNext     = {0.0, 0.0, 0.0};
	
	/**
	 * Standard contructor.
	 * 
	 * @param ray       The Ray that owns this RayBranch that owns this segment.
	 * @param branch    The RayBranch that owns this segment.
	 * @param nodes     The nodes defining this segment.
	 * @param waveIndex The index of the wave type in the underlying GeoTessModel.
	 * @throws GeoTessException
	 */
	public RaySegmentBottom(Ray ray, RayBranchBottom branch, LinkedList<GeoTessPosition> nodes,
                          int waveIndex, RaySegment prevSegment) throws GeoTessException
  {
  	super(ray, branch, nodes, waveIndex, prevSegment);
		
  	phaseLayerLevelDefinition = branch.getPhaseLayerLevelDefinition();
  	activeLayer = branch.getActiveLayerLevel();
  	previousActiveLayer = branch.getPreviousActiveLayerLevel();
  	//phaseLayerLevelDefinition = ray.bender.getPhaseLayerLevelDefinition();
  	//activeLayer = ray.activeLayer;
  	//previousActiveLayer = ray.previousActiveLayer;
		middle = nodes.get(nodes.size() / 2);
  }

	/**
	 * Returns the PhaseLayerLevelDefinition object assigned to this bottom segment.
	 * 
	 * @return The PhaseLayerLevelDefinition object assigned to this bottom segment.
	 */
	public PhaseLayerLevelDefinition getPhaseLayerLevelDefinition()
	{
		return phaseLayerLevelDefinition;
	}

	/**
	 * Returns the middle node.
	 * 
	 * @return The middle node.
	 */
	@Override
	public GeoTessPosition getMiddleNode()
	{
		return middle;
	}

	@Override
	protected void resetToInitialNodeDensity()
	{
		// only remove nodes if there are more than 5 in the list (the initial
		// density of a RaySegmentBottom)

		if (nodes.size() > 5)
		{
			// remove all nodes except the first, last, middle, and downgoing middle,
			// and upgoing middle.
			
	    travelTime = pathLength = Globals.NA_VALUE;

	    // get the middle node of the node list

	    int midIndex = nodes.indexOf(middle);
	    GeoTessPosition downGoingMiddle = nodes.get(midIndex/2);
	    GeoTessPosition upGoingMiddle   = nodes.get((nodes.size() - 1 - midIndex)/2 + midIndex);

	    // get a forward iterator and get the first node ... loop over all nodes
	    // in the list and remove all but the first, last, middle, downgoing middle,
	    // and up going middle.

			iterator = nodes.listIterator();
			node = iterator.next();
			while (node != middle)
			{
				// if the current node is not the first, or downgoing middle then
				// remove it ... iterate to next node and continue

				if ((node != first) && (node != downGoingMiddle))
					iterator.remove();
				
				node = iterator.next();
			}
			
			while (node != last)
			{
				// if the current node is not the first, or downgoing middle then
				// remove it ... iterate to next node and continue

				if ((node != middle) && (node != upGoingMiddle))
					iterator.remove();
				
				node = iterator.next();
			}
		}
	}

	/**
	 * Returns the Level at which the segment middle point turns.
	 */
	@Override
	protected Level getRayInterface()
	{
		return rayInterface;
	}

	/**
	 * Returns the ray interface name string.
	 * 
	 * @return The ray interface name string.
	 */
	public String getRayInterfaceName()
	{
		return rayInterface.getName();
	}

	/**
	 * Returns the active layer name string.
	 * 
	 * @return The active layer name string.
	 */
	public String getActiveLayerName()
	{
		return activeLayer.getName();
	}

	/**
	 * Returns the node immediately before the middle node.
	 * 
	 * @return The node immediately before the middle node.
	 */
	protected GeoTessPosition getMiddlePreviousNode()
	{
		if (middle == null)
			return null;
		else
		  return nodes.get(nodes.size()/2 - 1);
	}

	/**
	 * Returns the node immediately after the middle node.
	 * 
	 * @return The node immediately after the middle node.
	 */
	protected GeoTessPosition getMiddleNextNode()
	{
		if (middle == null)
			return null;
		else
		  return nodes.get(nodes.size()/2 + 1);
	}

	/**
	 * Overridden method that always returns true (is a bottom segment) for this
	 * segment.
	 */
	@Override
	protected boolean isBottom()
	{
		return true;
	}

	/**
	 * Returns reference to the node with the smallest radius.
	 * @return InterpolatedNodeLayered
	 */
	protected GeoTessPosition getTurningPoint()
	{
		if (nodes.size() == 0)
			return null;

		// start at middle node
		// if prev < middle 
		iterator = nodes.listIterator();
		node = iterator.next();
		GeoTessPosition minNode = node;
		while (iterator.hasNext())
		{
			node = iterator.next();
			if (node.getRadius() < minNode.getRadius())
				minNode = node;
		}
		return minNode;
	}

	/**
	 * Performs the segment bend operation by calling RaySegmentBend.bend()
	 * method if this bottom segment is not a reflection.
	 * 
	 * @throws GeoTessException 
	 * @throws GeoVectorException
	 */
	@Override
	public void bend() throws GeoTessException
	{
		if (isReflection)
			return;

		super.bend();
	}

	/**
	 * Returns true if the radius of first point in this bottom segment is < the
	 * radius of the activeLayer measured at the starting point.
	 *  
	 * @return True if the radius of first point in this bottom segment is < the
	 *         radius of the activeLayer measured at the starting point.
	 */
	private boolean isFirstPointDeep()
	{
		//return this.ray.sourceIsDeep;
		return ((RayBranchBottom) branch).isFirstPointDeep();
	}

	/**
	 * Returns true if the radius of last point in this bottom segment is < the
	 * radius of the activeLayer measured at the starting point.
	 *  
	 * @return True if the radius of last point in this bottom segment is < the
	 *         radius of the activeLayer measured at the starting point.
	 */
	private boolean isLastPointDeep()
	{
		//return this.ray.receiverIsDeep;
		return ((RayBranchBottom) branch).isLastPointDeep();
	}

	/**
	 * Apply bending algorithm of Um and Thurber to three adjacent nodes:
	 * previous, node and next.  Position of node is modified but positions
	 * of previous and next are not.
	 * @throws GeoTessException 
	 */
	@Override
	protected void bend3nodes() throws GeoTessException
	{
		// position node half way in between previous and next
		//if (node != middle)
	  node.setIntermediatePosition(previous, next, 0.5);
		//else
		//  node.setIntermediateUnitVectorPosition(previous, next, 0.5);

		if (homogeneousConstantVelocityLayer || bendNodeCheckRadius()) return;

		if (node == middle && node.getRadius() > activeLayer.getRadius(middle))
			node.setRadius(activeLayer.getRadius(middle));

		double thickness = node.getLayerThickness();
		if (thickness < minLayerThicknessNoBend)
			return; // node already averaged.
		else if (thickness < maxLayerThicknessOnlyBend)
		{
			GeoTessPosition avgNode = node.deepClone();
			bendNode();
			double f = (thickness - minLayerThicknessNoBend) /
					 			 (maxLayerThicknessOnlyBend - minLayerThicknessNoBend);
			node.setIntermediatePosition(avgNode, node, f);
		}
		else
			bendNode(); // bend only

		rbottom = previousActiveLayer.getRadius(node);
		if (node.getRadius() < rbottom)
			node.setRadius(rbottom);

		// firstPointIsDeep is false when firstPoint.Radius > activeLayer.getRadius(firstPoint)

		if (node == middle && !isFirstPointDeep() && !isLastPointDeep())
		{
			rtop = activeLayer.getRadius(middle);
			if (middle.getRadius() > rtop)
			{
				if (!activeLayer.isMajorInterface())
				{
					double z1 = abs(previous.getRadius() -
							activeLayer.getRadius(previous));
					double z2 = abs(next.getRadius() -
							activeLayer.getRadius(next));
					if (z1 + z2 < 1e-6)
						middle.setIntermediatePosition(previous, next, 0.5);
					else
						middle.setIntermediatePosition(previous, next, z1 / (z1 + z2));

				}
				middle.setRadius(activeLayer.getRadius(middle));
			}
		}
		else if (!aboveModel)
		{
			rtop = node.getRadiusTop(majorLayerIndex);
			if (node.getRadius() > rtop)
				node.setRadius(rtop);
		}
	}

	/**
	 * Doubles the nodes along this segment and then bends all of the new nodes
	 * using the bend3nodes() method. The nodes are only doubled if there current
	 * node spacing exceeds the input threshold. This method overrides the same
	 * method in RaySegmentBend so that the segments upgoing and downgoing nodes
	 * can be handled separately. 
	 * 
	 * @param threshold The value of node spacing above which causes the segment
	 *                  node population to be doubled and bent.
	 * @throws GeoTessException 
	 */
	@Override
	protected void doubleNodesBend(double threshold) throws GeoTessException
	{
		if (isReflection || homogeneousConstantVelocityLayer) return;

		double l1 = 0, l2 = 0, ltotal=0, len;
		int n1 = 0, n2 = 0;
		iterator = nodes.listIterator();
		previous = iterator.next();
		// first determine spacing on each side of middle
		while (iterator.hasNext())
		{
			next = iterator.next();
			len = previous.getDistance3D(next);
			ltotal += len;
			if (previous == middle || n2 > 0)
			{
				++n2;
				l2 += len;
			}
			else
			{
				++n1;
				l1 += len;
			}
			previous = next;
		}

		// 2/18/2011 redefine the threshold for the bottom segment
		// to be half the average spacing of the nodes.  
		threshold = ltotal/(2*(n1+n2));

		if (n1 > 1)
			l1 /= n1;
		if (n2 > 1)
			l2 /= n2;

		iterator = nodes.listIterator();
		previous = iterator.next();
		if (l1 > threshold)
		{
			while (previous != middle)
			{
				//node = previous.deepClone();
				node = GeoTessPosition.getGeoTessPosition(previous);

				node.setIndex(majorLayerIndex);

				// add it to list immediately following previous.
				iterator.add(node);

				// get the node immediately following the new node.
				next = iterator.next();

				// bend the three nodes
				bend3nodes();
				previous = next;
			}
			travelTime = pathLength = Globals.NA_VALUE;
		}
		else
		{
			while (previous != middle)
			{
				// get the node immediately following the new node.
				next = iterator.next();
				previous = next;
			}
		}

		if (l2 > threshold)
		{
			while (iterator.hasNext())
			{
				//node = previous.deepClone();
				node = GeoTessPosition.getGeoTessPosition(previous);

				node.setIndex(majorLayerIndex);

				// add it to list immediately following previous.
				iterator.add(node);

				// get the node immediately following the new node.
				next = iterator.next();

				// bend the three nodes
				bend3nodes();
				previous = next;
			}
		}
	}

	/**
	 * Returns the current ray type setting. Does not re-evaluate the setting.
	 * 
	 * @return The current ray type setting.
	 */
	public RayType getRayTypeSetting()
	{
		return rayType;
	}

	/**
	 * Determines and returns the RayType for this RaySegmentBottom. The result
	 * can be REFLECTION, REFRACTION, or DIFFRACTION.
	 *  
	 * @throws GeoTessException 
	 * @throws BenderException
	 */
	@Override
	protected RayType getRayType() throws GeoTessException
	{
		// isReflection is true only when this is the bottom segment and the
		// ray reflected off of a major layer interface.  Don't need to check
		// anything else.
		if (isReflection)
		{
			rayInterface = activeLayer;
			rayType = RayType.REFLECTION;
			return rayType;
		}

		// default to refraction, with rayInterface set to top of major layer
		// at or above current active layer.
		rayType = RayType.REFRACTION;
		rayInterface = phaseLayerLevelDefinition.getMajorLayer(majorLayerIndex);

		iterator = nodes.listIterator();
		previous = iterator.next();
		node = iterator.next();

		int inLayer = 0;

		//      // at the start, previous is the first node, which is on an interface,
		//      // and node is the second node in the segment.  Process all interior
		//      // nodes.  The while-loop does not process the first or last nodes in
		//      // the segment.
		while (iterator.hasNext())
		{
			//        commented out code checks for negative gradients at diffraction interface.
			//        if (node == middle)
			//        {
			//      	  System.out.print("debug "+(node.getRadius()-previousActiveLayer.getRadius(node)));
			//      	  System.out.print("  "+(1.0/node.getValue(GeoAttributes.PSLOWNESS, activeLayer.getMajorLayerIndex(), node.getRadius()+1)));
			//      	  System.out.print("  "+(1.0/node.getValue(GeoAttributes.PSLOWNESS, activeLayer.getMajorLayerIndex())));
			//      	  
			//      	  System.out.println("  "+((1.0/node.getValue(GeoAttributes.PSLOWNESS, activeLayer.getMajorLayerIndex(), node.getRadius()+1))
			//      			  -(1.0/node.getValue(GeoAttributes.PSLOWNESS, activeLayer.getMajorLayerIndex()))));
			//      	  
			//        }

			// if ray touches top of major layer, then it is a diffraction.
			// We aren't done though, because it could still be a diffraction
			// along the bottom of the active layer, or it could be a reflection.
			if (!aboveModel && node.getRadius() >= node.getRadiusTop(majorLayerIndex))
			{
				rayType = RayType.BOTTOM_SIDE_DIFFRACTION;
				if (node == middle)
					return rayType;
			}
			else if (node.getRadius() <= previousActiveLayer.getRadius(node)+1e-3)
			{
				// if this node is not on the major layer interface, and it is within one meter of
				// the top of the previous layer, then it is a diffraction on the
				// top of the previous layer.
				// Set rayType and rayInterface and return.

				rayType = RayType.TOP_SIDE_DIFFRACTION;
				rayInterface = previousActiveLayer;
				return rayType;
			}

			// count nodes that penetrated the top of the activeLayer.
			if (node.getRadius() < activeLayer.getRadius(node))
				++inLayer;

			previous = node;
			node = iterator.next();
		}

		// at this point we know it is not a reflection off of a major layer,
		// and it is not a diffraction along the top of the previous layer.
		// If it is not the bottom layer, then it is either a refraction or
		// diffraction at the major layer boundary above.

		if (isFirstPointDeep() || isLastPointDeep())
			return rayType;

		// if this is bottom, and no nodes penetrated the top of the layer,
		// then it is a reflection.
		if (inLayer == 0)
		{
			rayInterface = activeLayer;
			rayType = RayType.REFLECTION;
			return rayType;
		}

		return rayType;
	}

	/**
	 * Returns true if the only node below the top of the current active layer is
	 * the middle node. This is an invalid ray condition for a fictitious layer
	 * setting (a Level) that simulates a relflection.
	 * 
	 * @return True if the only node below the top of the current active layer is
	 *         the middle node.
	 * @throws GeoTessException
	 */
  protected boolean isMiddleOnlyActiveNode() throws GeoTessException
  {
		if (middle.getRadius() <= activeLayer.getRadius(middle))
		{
			if ((rayType == RayType.REFRACTION) &&
					(getMiddlePreviousNode().getRadius() >
					 activeLayer.getRadius(getMiddlePreviousNode())) &&
					(getMiddleNextNode().getRadius() >
					 activeLayer.getRadius(getMiddleNextNode())))
			{
				// one point still in active region ... set to invalid (cross fingers!)
				return true;
			}
		}

		return false;
  }

  /**
   * Always returns BOTTOM for the segment type.
   */
	@Override
  public RayDirection getSegmentType()
	{
		return RayDirection.BOTTOM;
	}

  /**
   * Always returns BOTTOM for the RayDirection change type.
   */
	@Override
	public RayDirection getRayDirectionChangeType()
	{
		return RayDirection.BOTTOM;
	}

	/**
	 * If on entry isReflection is false, then this method checks to see if first and
	 * last nodes are very close together (distance3D < ~0.1 km), or if first
	 * and last have changed positions relative to middle.  If so, then
	 * first, last and middle are all set equal to a point half way between
	 * first and last, and isReflection is set to true.
	 *
	 * <p>If on entry isReflection is true, then geometry of the nodes just
	 * before and just after the nodes in this segment is checked to see if the
	 * ray is no longer a reflection.  If it is not, then first and last are
	 * split apart a little bit so that the ray can become a refraction,
	 * and isReflection is set to false.
	 *
	 * <p>First time this is called, isReflection is false.
	 * @return boolean
	 * @throws GeoTessException 
	 */
	@Override
	protected boolean checkReflection() throws GeoTessException
	{
		// middle == null for all segments except the bottom segment.
		// segmentIndex == 0 means the source is in the bottom segment.
		// segmentIndex == ray.getNSegments() - 1 means the receiver is
		// in the bottom segment.
		// Under all of these circumstances, isReflection can never be true.
		if (middle == null || segmentIndex == 0 ||
				segmentIndex == branch.getNSegments() - 1)
			return false;

		// this has been checked before and determined to be a reflection. If it's
		// not a fixed reflection then see if the nodes need to be split apart.

		if (isReflection)
		{
			// Need to check to see if the 3 co-located nodes in this the bottom segment
			// need to be split apart a bit so that they can be considered a refraction
			// on next call to SnellsLaw().  Only want to split them apart if the
			// incidence angles of the reflection are near the critical angle.

			int layerAbove = middle.nextLayer(majorLayerIndex,
					BenderConstants.MIN_LAYER_THICKNESS);
			int layerBelow = middle.previousLayer(majorLayerIndex,
					BenderConstants.MIN_LAYER_THICKNESS);

			// find the next-to-last node in the segment that precedes this segment
			// and which is in a layer with finite-thickness.
			previous = next = null;
			RaySegment rs = prevActiveSegment;
			while (rs != branch.firstActiveSegment.prevActiveSegment)
			{
			  if (rs.getMajorLayerIndex() == layerAbove)
			  {
		      previous = rs.getNextToLast();
			    break;
			  }
			  rs = rs.prevActiveSegment;
			}
//			for (int i = segmentIndex - 1; i >= 0; --i)
//				if (branch.getBranchSegment(i).getMajorLayerIndex() == layerAbove)
//				{
//					previous = branch.getBranchSegment(i).getNextToLast();
//					break;
//				}

			// find the second node in the segment that comes after this segment
			// and which is in a layer with finite-thickness.
			rs = nextActiveSegment;
			while (rs != branch.lastActiveSegment.nextActiveSegment)
			{
				if (rs.getMajorLayerIndex() == layerAbove)
				{
				  next = rs.getSecond();
					break;
				}
				rs = rs.nextActiveSegment;
			}
//			for (int i = segmentIndex + 1; i < branch.getNSegments(); ++i)
//				if (branch.getBranchSegment(i).getMajorLayerIndex() == layerAbove)
//				{
//					next = branch.getBranchSegment(i).getSecond();
//					break;
//				}
			
			// find the slowness at position of middle, in the layer above the bottom.
			double slowAbove = middle.getValueBottom(attributeIndex, layerAbove);

			// find the slowness at position of middle, in the bottom layer.
			double slowBelow = middle.getValueTop(attributeIndex, layerBelow);

			// find the average slowness at previous node and middle node in layer above.
			double slowPrevious = 0.5 *
			(previous.getValue(attributeIndex, layerAbove) + slowAbove);

			// find the average slowness at next node and middle node in layer above.
			double slowNext = 0.5 *
			(next.getValue(attributeIndex, layerAbove) + slowAbove);

			// find critical incidence angles.
			double thetaPrevious = asin(slowBelow / slowPrevious);
			double thetaNext = asin(slowBelow / slowNext);

			// find 3-component vector previous-middle and next-middle
			previous.minus(middle, vPrevious);
			next.minus(middle, vNext);

			// normalize the vectors to unit length but save their lengths prior
			// to normalization.  Lengths are in km.
			double xPrevious = VectorUnit.normalize(vPrevious);
			double xNext = VectorUnit.normalize(vNext);

			// find the angle in radians between vPrevious and vNext.  If you
			// consider triangle previous-middle-next, this is the angle at middle.
			double angle = VectorUnit.angle(vPrevious, vNext);

			//      boolean debug = false; //angle > thetaPrevious+thetaNext;
			//      if (debug)
			//      {
			//        System.out.println("Receiver " + ray.getReceiver());
			//        System.out.println("Source " + ray.getSource());
			//        System.out.println("Phase " + ray.getPhase().getPhaseName());
			//        System.out.printf("Distance (deg)    = %10.4f%n",
			//                          toDegrees(ray.getDistance()));
			//
			//        System.out.printf("layer above       = %s%n",
			//                          ray.getGeoModel().
			//                          getInterfaces().getInterfaceName(layerAbove));
			//        System.out.printf("current layer     = %s%n",
			//                          ray.getGeoModel().
			//                          getInterfaces().getInterfaceName(majorLayerIndex));
			//        System.out.printf("velocity previous = %10.4f%n", 1. / slowPrevious);
			//        System.out.printf("velocity next     = %10.4f%n", 1. / slowNext);
			//        System.out.printf("velocity above    = %10.4f%n", 1. / slowAbove);
			//        System.out.printf("velocity below    = %10.4f%n", 1. / slowBelow);
			//        System.out.printf("length of previous (km)   = %10.4f%n", xPrevious);
			//        System.out.printf("length of next     (km)   = %10.4f%n", xNext);
			//        System.out.printf("critical angle previous   = %10.4f%n",
			//                          toDegrees(thetaPrevious));
			//        System.out.printf("critical angle next       = %10.4f%n",
			//                          toDegrees(thetaNext));
			//        System.out.printf("sum critical angles       = %10.4f%n",
			//                          toDegrees(thetaPrevious + thetaNext));
			//        System.out.printf("angle previous.next       = %10.4f%n",
			//                          toDegrees(angle));
			//        if (angle <= thetaPrevious + thetaNext)
			//          System.out.printf("is reflection  because sum critical angles >= angle previous.next%n");
			//        else
			//          System.out.printf("not reflection  because angle previous.next > sum critical angles%n");
			//        System.out.println();
			//      }

			// if angle between the two vectors is greater than critical angle then
			// might need to expand the bottomSegment.
			if (angle > thetaPrevious + thetaNext)
			{
				// replace length of hypotenuse in km with horizontal distance in km.
				xPrevious *= sin(thetaPrevious);
				xNext *= sin(thetaNext);

				// find slope, which is the cos of the angle between the plane tangent
				// to interface at middle, and the horizontal plane.
				double slope = VectorUnit.dot(middle.getLayerNormal(majorLayerIndex),
	                                    middle.getVector());
				// distance from previous to next in km, measured in tangent plane.
				double dist = previous.distance(next) *
				middle.getRadiusTop(majorLayerIndex) / slope;

				//        if (debug)
				//        {
				//          System.out.printf("slope     (deg)    = %10.4f%n",
				//                            toDegrees(acos(slope)));
				//          System.out.printf("xPrevious (km)     = %10.4f%n", xPrevious);
				//          System.out.printf("xNext     (km)     = %10.4f%n", xNext);
				//          System.out.printf("sum       (km)     = %10.4f%n", xPrevious + xNext);
				//          System.out.printf("previous-next (km) =%10.4f%n", dist);
				//          System.out.printf("pierce point separation (km) = %10.4f%n",
				//                            dist - xPrevious - xNext);
				//          if (dist <= BenderConstants.RELECTION_DISTANCE)
				//            System.out.printf(
				//                "is reflection  because pierce point separation  <= %1.6f%n",
				//                BenderConstants.RELECTION_DISTANCE);
				//          else
				//            System.out.printf(
				//                "not reflection  because pierce point separation  > %1.6f%n",
				//                BenderConstants.RELECTION_DISTANCE);
				//          System.out.println();
				//        }

				// distance in km between the pierce points.
				dist -= xPrevious + xNext;

				// if distance between the pierce points, in km, is greater than tolerance
				// then expand the bottomsegment.
				if (dist > BenderConstants.RELECTION_DISTANCE)
				{
					// fractional part of dist that should be assigned to each side.
					double f = xPrevious / (xPrevious + xNext);

					// dist is currently distance in km measured in tangent plane.
					// Convert to geocentric angle.
					dist *= slope / middle.getRadiusTop(majorLayerIndex);

					xPrevious = dist * (1 - f);
					xNext = dist * f;
					
					//          if (debug)
					//          {
					//            System.out.printf("xPrevious (deg)     = %10.4f%n",
					//                              toDegrees(xPrevious));
					//            System.out.printf("xNext     (deg)     = %10.4f%n", toDegrees(xNext));
					//            System.out.println();
					//          }

					// make a temporary position and radius
					
					double [] pstn = {1.0, 0.0, 0.0};
					double pstnRad = 1.0;
					
					// find the distance from receiver to middle node, in radians
					dist = branch.firstPoint.distance(middle);
					// find a geovector that is closer to the source.
					branch.greatCircle.getPoint(dist + xPrevious, pstn);
					// set position of the first node in this segment to the new position,
					// reinterpolate, and set radius to radius of the interface.
					node = first;
					pstnRad = node.getRadius();
					node.set(pstn, pstnRad);
					node.setRadius(node.getRadiusTop(majorLayerIndex));

					// now find a position closer to the receiver and move last
					// node in this segment to that position.
					branch.greatCircle.getPoint(dist - xNext, pstn);
					node = nodes.getLast();
					pstnRad = node.getRadius();
					//ray.geoVector.setRadius(node.getRadius());
					node.set(pstn, pstnRad);
					node.setRadius(node.getRadiusTop(majorLayerIndex));

					middle.setRadius(middle.getRadius() -
							1.00001 * BenderConstants.MIN_LAYER_THICKNESS);

					isReflection = false;
				}
			}

		}

		// ray.greatCircle is the great circle path from receiver to source.  Its
		// normal is source cross receiver, normalized to unit length.  With
		// receiver on left, source on right, normal points out of plane toward
		// the observer.  Since the ray travels from source to receiver (right to
		// left), last should be to the left of first; last cross first should
		//  point into the plane of the great circle and the dot product with the
		// normal should be negative.  If scalar triple product is positive, it
		// means that last is to the right of first, which is the wrong order.
		else if ((first.getDistance3D(last) <= BenderConstants.RELECTION_DISTANCE) ||
				      (VectorUnit.scalarTripleProduct(last.getVector(),
						              first.getVector(), branch.greatCircle.getNormal()) >= 0))
		{
			isReflection = true;

			// Need to check to see if the 3 co-located nodes in this the bottom segment
			// need to be split apart a bit so that they can be considered a refraction
			// on next call to SnellsLaw().  Only want to split them apart if the
			// incidence angles of the reflection are near the critical angle.

			int layerAbove = middle.nextLayer(majorLayerIndex,
					BenderConstants.MIN_LAYER_THICKNESS);

			// find the next-to-last node in the segment that precedes this segment
			// and which is in a layer with finite-thickness.
			previous = next = null;
			RaySegment rs = prevActiveSegment;
			while (rs != branch.firstActiveSegment.prevActiveSegment)
			{
			  if (rs.getMajorLayerIndex() == layerAbove)
			  {
		      previous = rs.getNextToLast();
			    break;
			  }
			  rs = rs.prevActiveSegment;
			}
//			for (int i = segmentIndex - 1; i >= 0; --i)
//				if (branch.getBranchSegment(i).getMajorLayerIndex() == layerAbove)
//				{
//					previous = branch.getBranchSegment(i).getNextToLast();
//					break;
//				}

			// find the second node in the segment that comes after this segment
			// and which is in a layer with finite-thickness.
			rs = nextActiveSegment;
			while (rs != branch.lastActiveSegment.nextActiveSegment)
			{
				if (rs.getMajorLayerIndex() == layerAbove)
				{
				  next = rs.getSecond();
					break;
				}
				rs = rs.nextActiveSegment;
			}
//			for (int i = segmentIndex + 1; i < branch.getNSegments(); ++i)
//				if (branch.getBranchSegment(i).getMajorLayerIndex() == layerAbove)
//				{
//					next = branch.getBranchSegment(i).getSecond();
//					break;
//				}

			double hPrevious = previous.getRadius() - previous.getRadiusTop(majorLayerIndex);
			double hNext = next.getRadius() - next.getRadiusTop(majorLayerIndex);

			if (hPrevious+hNext > 1)
				middle.setIntermediatePosition(first, last, hPrevious/(hPrevious+hNext));
			else
				middle.setIntermediatePosition(first, last, 0.5);
			
			first.copy(middle);
			last.copy(middle);

			nodes.clear();
			nodes.add(first);
			nodes.add(middle);
			nodes.add(last);
		}

		return isReflection;
	}
}
