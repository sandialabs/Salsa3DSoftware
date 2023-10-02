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
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.geotess.GeoTessUtils;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.geovector.GeoVectorRay;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.WaveType;
import gov.sandia.gmp.bender.BenderConstants;
import gov.sandia.gmp.bender.BenderConstants.GradientCalculationMode;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;
import gov.sandia.gmp.bender.BenderException;
import gov.sandia.gmp.bender.BenderException.ErrorCode;
import gov.sandia.gmp.bender.level.Level;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle.GreatCircleException;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.resampleray.ResampleRay;

/**
 * Base class RaySegment that contains owning Ray and RayBranch, Ray and Branch
 * segment indices, the major layer index within which the segment lies, the
 * wave type (P or S), and the list of nodes that comprise the segment. All
 * segment types derive off of this class.
 * 
 * @author jrhipp
 *
 */
public abstract class RaySegment
{
	/**
	 * A reference to the Ray object that owns this RaySegment
	 */
	protected Ray ray;

	/**
	 * A reference to the RayBranch object that owns this RaySegment
	 */
	protected RayBranch branch;

	protected RaySegment prevInitialSegment = null;
	protected RaySegment nextInitialSegment = null;
	protected RaySegment prevActiveSegment = null;
	protected RaySegment nextActiveSegment = null;

	/**
	 * The index of this RaySegment in the ArrayList of RaySegments managed
	 * by Ray.
	 */
	protected int segmentIndex = -1;
//
//	/**
//	 * The index of this RaySegment in the ArrayList of Branch RaySegments managed
//	 * by branch.
//	 */
//	protected int branchSegmentIndex = -1;

	/**
	 * The index of the major layer through which this segment travels.
	 */
	protected int majorLayerIndex;

	/**
	 * True if the layer is a homogeneous constant velocity layer (water or ice). 
	 */
	protected boolean homogeneousConstantVelocityLayer = false;

	/**
	 * Usually the majorLayerIndex. But can be larger if segments above are
	 * ignored.
	 */
	protected int topLayerIndex = -1;

	/**
	 * The waveType of this RaySegment: either P or S.
	 */
	protected WaveType waveType;

	/**
	 * The attributeIndex of waveType in the model
	 */
	protected int attributeIndex;
	
	/**
	 * The list of InterpolatedNodeLayered objects that comprise this RaySegment.
	 */
	protected LinkedList<GeoTessPosition> nodes;

	/**
	 * References to the first and last nodes in the list of nodes.
	 */
	protected GeoTessPosition first, last;

	/**
	 * Iterator over the nodes in this segment
	 */
	protected ListIterator<GeoTessPosition> iterator;

	/**
	 * Three general purpose nodes for use throughout the class.
	 */
	protected GeoTessPosition previous, node, next;

	/**
	 * The path length of this ray segment in km.
	 */
	protected double pathLength = Globals.NA_VALUE;

	/**
	 * The traveltime of this ray segment in sec.
	 */
	protected double travelTime = Globals.NA_VALUE;

	/**
	 * Used by Snell's law to set the fitness of a segments major interface point
	 */
	protected double fitness = 0;

  protected boolean isActive = true;
//
//	/**
//	 * Debug statistic information that stores the amount of movement of the
//	 * segments nodes per optimization step.
//	 */
//	protected Statistic nodeMovementStats = null;

	/**
	 * Abstract method that returns the ray type for all concrete RaySegemnts.
	 * 
	 * @return The RaySegment RayType.
	 * @throws GeoTessException
	 */
	protected abstract RayType getRayType() throws GeoTessException;
	
	/**
	 * Abstract method that returns the Segment Type RayDirection.
	 * 
	 * @return The Segment Type RayDirection.
	 */
  public abstract RayDirection getSegmentType();
  
  /**
   * Abstract method that performs Ray bending if supported.
   * @throws GeoTessException
   */
	public abstract void bend() throws GeoTessException;
	
	/**
	 * Abstract method that doubles the node count in the segment and performs
	 * bending on all of the new nodes. The segment nodes are only doubled if the
	 * mean node spacing exceeds the tolerance.
	 * 
	 * @param threshold The node spacing tolerance.
	 * @throws GeoTessException
	 */
	protected abstract void doubleNodesBend(double threshold) throws GeoTessException;

	/**
	 * Constructor that defines the RaySegment.
	 * 
	 * @param ray
	 * @param branch
	 * @param nodes
	 * @param attributeIndex
	 * @throws GeoTessException
	 */
	public RaySegment(Ray ray, RayBranch branch, LinkedList<GeoTessPosition> nodes,
			               int attributeIndex) throws GeoTessException
	{
		initializeSegment(ray, branch, nodes, attributeIndex, null);
	}

	public RaySegment(Ray ray, RayBranch branch, LinkedList<GeoTessPosition> nodes,
										int attributeIndex, RaySegment prevSegment) throws GeoTessException
	{
		initializeSegment(ray, branch, nodes, attributeIndex, prevSegment);
  }

	/**
	 * Returns true if the layer is a homogeneous constant velocity layer.
	 * 
	 * @return True if the layer is a homogeneous constant velocity layer.
	 */
	public boolean 	isHomogeneousConstantVelocityLayer()
	{
		return homogeneousConstantVelocityLayer;
	}

	private void initializeSegment(Ray ray, RayBranch branch,
		LinkedList<GeoTessPosition> nodes,
		int attributeIndex, RaySegment prevSegment)
	{
	    // set the owning ray, branch, and node list

	    this.ray = ray;
	    this.branch = branch;
	    this.nodes = nodes;

	    // get the first and last node references

	    first = this.nodes.getFirst();
	    last = this.nodes.getLast();

	    if (prevSegment != null)
	    {
		prevSegment.nextActiveSegment = prevSegment.nextInitialSegment = this;
		prevActiveSegment = prevInitialSegment = prevSegment;
		segmentIndex = prevSegment.segmentIndex + 1;
	    }
	    else
		segmentIndex = 0;

	    // set the major layer index, wave type index, and wave type name.

	    majorLayerIndex = topLayerIndex = max(first.getIndex(), last.getIndex());

	    this.attributeIndex = attributeIndex;
	    
	    GeoAttributes attribute = GeoAttributes.valueOf(ray.getGeoTessModel().getMetaData().getAttributeName(attributeIndex));

	    waveType = GeoAttributes.PSLOWNESS == attribute ? WaveType.P : WaveType.S;

	    homogeneousConstantVelocityLayer = ray.bender.getPhaseRayBranchModel().
		    getModelEarthInterface(majorLayerIndex).
		    isHomogeneousConstantVelocityLayer();
	    // force calculation of path length and travel time
	    pathLength = Globals.NA_VALUE;
	    travelTime = Globals.NA_VALUE;
	}

	public RaySegment getNextActiveSegment()
	{
		return nextActiveSegment;  
	}

	public RaySegment getPreviousActiveSegment()
	{
		return prevActiveSegment;  
	}

	public RaySegment getNextInitialSegment()
	{
		return nextInitialSegment;  
	}

	public RaySegment getPreviousInitialSegment()
	{
		return prevInitialSegment;  
	}

	public void setFirstNode(GeoTessPosition newFirstNode)
	{
		nodes.set(0,  newFirstNode);
		first = newFirstNode;
		topLayerIndex = max(first.getIndex(), last.getIndex());
	}

	public void setLastNode(GeoTessPosition newLastNode)
	{
		nodes.set(nodes.size() - 1,  newLastNode);
		last = newLastNode;
		topLayerIndex = max(first.getIndex(), last.getIndex());
	}

	public GeoTessPosition getTopLayerNode()
	{
		return (first.getIndex() > last.getIndex()) ? first : last;
	}

	public GeoTessPosition getBottomLayerNode()
	{
		return (first.getIndex() > last.getIndex()) ? last : first;
	}

	/**
	 * Returns the wave type name.
	 * 
	 * @return The wave type name.
	 */
	public String getWaveTypeName()
	{
		return waveType.name();
	}

	/**
	 * Returns the wave type.
	 * 
	 * @return The wave type.
	 */
	public WaveType getWaveType()
	{
		return waveType;
	}

	/**
	 * Default Level object for this RaySegment is always null. RaySegmentBottom
	 * types define this as a non-null object.
	 * 
	 * @return Level object for this RaySegment.
	 */
	protected Level   getRayInterface()
	{
		return null;
	}

	/**
	 * Retrieve the index of this segment in the array of segments managed
	 * by the Ray that owns this segment. A Segment with index 0 starts at the
	 * source.  Last segment ends at the receiver.
	 * 
	 * @return The Ray's segment index for this RaySegment.
	 */
	public int getSegmentIndex()
	{
		return segmentIndex;
	}
//
//	/**
//	 * Retrieve the branch index of this segment in the array of segments managed
//	 * by the RayBranch that owns this segment.  Segment with index 0 starts at
//	 * the first node of the branch. Last segment ends at the last node of the
//	 * branch.
//	 * 
//	 * @return The Branch's segment index for the owning RayBranch.
//	 */
//	public int getBranchSegmentIndex()
//	{
//		return branchSegmentIndex;
//	}

	/**
	 * Returns true if this segment is the one in which the ray bottoms. Overriden
	 * by RaySegmentBottom types.
	 * 
	 * @return boolean
	 */
	protected boolean isBottom()
	{
		return false;
	}

	/**
	 * Returns the middle node if there are more than two nodes in the segment.
	 * Otherwise null is returned. If the number of nodes is even then the lower
	 * index of the two middle nodes is returned.
	 * 
	 * Overridden by RaySegmentBottom types.
	 * 
	 * @return The middle node of the segment.
	 */
	public GeoTessPosition getMiddleNode()
	{
		if (nodes.size() > 2)
			return nodes.get((nodes.size() - 1) / 2);
		else
		  return null;
	}

	protected void resetToInitialNodeDensity()
	{
		// only remove nodes if the list has more than 3 (the initial density of
		// all but RaySegmentBottom segments which override this method)

		if (nodes.size() > 3)
		{
	    travelTime = pathLength = Globals.NA_VALUE;

	    // get the middle node of the node list

	    GeoTessPosition midNode = nodes.get((nodes.size() - 1) / 2);
	    
	    // get a forward iterator and get the first node ... loop over all nodes
	    // in the list

			iterator = nodes.listIterator();
			node = iterator.next();
			while (iterator.hasNext())
			{
				// if the current node is not the first, last, or middle node then
				// remove it ... iterate to next node and continue

				if ((node != first) && (node != midNode) && (node != last))
					iterator.remove();
				node = iterator.next();
			}
		}
	}

	/**
	 * Retrieve the back azimuth between first and second point on the ray
	 * in this segment.
	 * 
	 * @return Segment back azimuth.
	 */
	protected double getBackAzimuth()
	{
		iterator = nodes.listIterator();
		node = iterator.next();
		double az = node.azimuth(iterator.next());
		while (Double.isNaN(az) && iterator.hasNext())
			az = node.azimuth(iterator.next());
		return az;
	}

	/**
	 * Retrieve the azimuth in radians between last and next-to-last point on the ray
	 * in this segment.
	 * 
	 * @return Segment azimuth.
	 */
	protected double getAzimuth()
	{
		iterator = nodes.listIterator(nodes.size());
		node = iterator.previous();
		double az = node.azimuth(iterator.previous());
		while (Double.isNaN(az) && iterator.hasPrevious())
			az = node.azimuth(iterator.previous());
		return az;
	}

	/**
	 * Returns the z value (out-of-plane) of the normal vector to this Segment, in km.
	 * Search each node in the segment except the first.  Transform the
	 * position of the node into the coordinate system where x and y are in
	 * the plane of the supplied GreatCircle and z points out of plane with
	 * z positive pointing in direction of source cross receiver.
	 * 
	 * @param gc GreatCircle
	 * @param point storage for the out-of-plane vector.
	 * @return Out-of-plane value, in km.
	 * @throws GreatCircleException 
	 * @throws GeoVectorException
	 */
	protected double getOutOfPlane(GreatCircle gc, double[] point)
	{
		double zmax = 0;
		if (gc.getDistance() > 0.)
		{
			iterator = nodes.listIterator();
			// skip the first one
			iterator.next();
			while (iterator.hasNext())
			{
				gc.transform(iterator.next().get3DVector(), point);
				if (abs(point[2]) > abs(zmax))
					zmax = point[2];
			}
		}
		else
			zmax = Globals.NA_VALUE;
		return zmax;
	}

	/**
	 * Return the major layer index.
	 * 
	 * @return The major layer index.
	 */
	protected int getMajorLayerIndex()
	{
		return majorLayerIndex;
	}

	/**
	 * Returns the segments wave type index.
	 * 
	 * @return The segments wave type index.
	 */
	protected int getWaveTypeIndex()
	{
		return attributeIndex;
	}

	/**
	 * Returns the segments node list.
	 * 
	 * @return The segments node list.
	 */
	public LinkedList<GeoTessPosition> getNodes()
	{
		return nodes;
	}

	/**
	 * Add the nodes owned by this segment to the input list.  If the list
	 * is empty on input all nodes are added, otherwise the first node in this
	 * segments nodes is skipped.
	 * 
	 * @param list The input list of nodes to which this segment's nodes are
	 *             added.
	 */
	public void addNodes(LinkedList<GeoTessPosition> list)
	{
		iterator = nodes.listIterator();
		if (list.size() > 0)
			iterator.next();
		while (iterator.hasNext())
			list.add(iterator.next());
	}

	/**
	 * Returns the number of nodes that comprise this RaySegment.
	 * 
	 * @return The number of nodes that comprise this RaySegment.
	 */
	public int size()
	{
		return nodes.size();
	}

	/**
	 * Returns the segment mean node spacing (the path length in km divided by
	 * number of nodes - 1).
	 * 
	 * @return The segment mean node spacing. 
	 */
	public double getSpacing()
	{
		return getPathLength() / (nodes.size() - 1);
	}

	/**
	 * Returns the angular distance in radians from first to last node.
	 * 
	 * @return The angular distance in radians from first to last node.
	 */
	public double getDistance()
	{
		return first.distance(nodes.getLast());
	}

	/**
	 * Returns the straight-line distance from first to last node, in km.
	 * 
	 * @return The straight-line distance from first to last node, in km.
	 */
	public double getDistance3D()
	{
		return first.getDistance3D(nodes.getLast());
	}

	/**
	 * Returns the segments first node.
	 * 
	 * @return The segments first node.
	 */
	public GeoTessPosition getFirst()
	{
		return first;
	}

	/**
	 * Returns the second node in the segment's node list.
	 * 
	 * @return The second node in the segment's node list.
	 */
	public GeoTessPosition getSecond()
	{
		if (nodes.size() == 2)
			return nodes.getLast();

		iterator = nodes.listIterator();
		iterator.next();
		return iterator.next();
	}

	/**
	 * Returns the second to last node in the segment's node list.
	 *
	 * @return The second to last node in the segment's node list.
	 */
	public GeoTessPosition getNextToLast()
	{
		if (nodes.size() == 2)
			return first;

		iterator = nodes.listIterator(nodes.size());
		iterator.previous();
		return iterator.previous();
	}

	/**
	 * Returns the last node in the segment's node list.
	 * 
	 * @return The last node in the segment's node list.
	 */
	public GeoTessPosition getLast()
	{
		return nodes.getLast();
	}

	/**
	 * Returns the Snell's law fitness setting for this segment.
	 * 
	 * @return The Snell's law fitness setting for this segment.
	 */
	public double getFitness()
	{
		return fitness;
	}

	/**
	 * Outputs this RaySegments toString() method to the input StringBuffer.
	 * 
	 * @param buffer The StringBuffer containing this RaySegments toString()
	 *               contents on output.
	 * @throws GeoVectorException
	 */
	public void toString(StringBuffer buffer)
	{
		buffer.append(" Dist(deg)  Lat(deg)   Lon(deg)   Depth(km)  Slow(s/km)  Index   Layer\n");
		buffer.append("---------------------------------------------------------------------------\n");
		try
		{
			iterator = nodes.listIterator();
			while (iterator.hasNext())
			{
				node = iterator.next();
				buffer.append(String.format("%10.4f %10.4f %10.4f %10.4f  %10.4f   %4d %12s%n",
						ray.getReceiver().distanceDegrees(node),
						GeoTessUtils.getLatDegrees(node.getVector()),
						GeoTessUtils.getLonDegrees(node.getVector()),
						node.getDepth(),
						node.getValue(attributeIndex, majorLayerIndex),
						node.getIndex(),
						node.getModel().getMetaData().getLayerName(majorLayerIndex)));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			buffer.append("/n/n").append(ex.getMessage()).append("\n\n");
		}
	}

	/**
	 * The RaySegment toString method.
	 * 
	 * @return The RaySegment's toString contents. 
	 */
	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		toString(buffer);
		return buffer.toString();
	}

	/**
	 * Finds the node in this ray segment whose radius is closest to the input
	 * value. If forward is true, search traverses list of nodes in forward
	 * direction, otherwise search traverses list in backward direction. If this
	 * segment is the bottomSegment (RaySegmentBottom only) search starts at
	 * middle node.
	 *
	 * @param radius double
	 * @param forward boolean
	 * @return InterpolatedNodeLayered
	 */
	protected GeoTessPosition findNode(double radius, boolean forward)
	{
		GeoTessPosition minNode = null;
		if (forward)
		{
			iterator = nodes.listIterator();
			node = iterator.next();
			double rmin = abs(node.getRadius() - radius);
			minNode = node;
			while (iterator.hasNext())
			{
				node = iterator.next();
				if (abs(node.getRadius() - radius) < rmin)
				{
					rmin = abs(node.getRadius() - radius);
					minNode = node;
				}
			}
		}
		else
		{
			iterator = nodes.listIterator(nodes.size());
			node = iterator.previous();
			double rmin = abs(node.getRadius() - radius);
			minNode = node;
			while (iterator.hasPrevious())
			{
				node = iterator.previous();
				if (abs(node.getRadius() - radius) < rmin)
				{
					rmin = abs(node.getRadius() - radius);
					minNode = node;
				}
			}
		}
		return minNode;
	}

	/**
	 * Removes all but the first and last nodes from this RaySegment.
	 */
	public void decimate()
	{
		if (nodes.size() > 2)
		{
			iterator = nodes.listIterator();
			while (iterator.hasNext())
			{
				// skip one (even numbered nodes)
				iterator.next();
				if (iterator.hasNext())
				{
					node = iterator.next();
					if (node != getLast())
						iterator.remove();
				}
			}
			pathLength = Globals.NA_VALUE;
			travelTime = Globals.NA_VALUE;
		}
	}

	/**
	 * Calculates and returns the pathLength only for this RaySegment in km.
	 * 
	 * @return The pathLength only for this RaySegment in km.
	 */
	public double getPathLength()
	{
		if (pathLength < 0.)
		{
			pathLength = 0.;
			iterator = nodes.listIterator();
			previous = iterator.next();
			while (iterator.hasNext())
			{
				next = iterator.next();
				//X pathLength += next.distance3D(previous);
				pathLength += next.getDistance3D(previous);
				previous = next;
			}
		}
		return pathLength;
	}

	/**
	 * Accumulate this segments travelTime, pathLength and nPoints to the
	 * owning rays equivalent parameters. This method forces a recalculation.
	 * 
	 * @throws GeoTessException 
	 */
	protected void accumulateRayTravelTime() throws GeoTessException
	{
		travelTime = pathLength = Globals.NA_VALUE;
		ray.travelTime += getSegmentTravelTime();
		ray.pathLength += pathLength;
		ray.nPoints += nodes.size() - 1;
	}

	/**
	 * Calculate travelTime and pathLength for this segment and return the
	 * travel time.
	 * 
	 * @return This segment's travel time.
	 * @throws GeoTessException 
	 */
	protected double getSegmentTravelTime() throws GeoTessException
	{
		if (travelTime < 0.0)
		{
			iterator = nodes.listIterator();
			previous = iterator.next();
	
			double dkm, slow2, slow1 = previous.getValue(attributeIndex, majorLayerIndex);
	
			travelTime = 0.0;
			pathLength = 0.0;
			while (iterator.hasNext())
			{
				node = iterator.next();
	
				dkm = node.getDistance3D(previous);
	      pathLength += dkm;

				slow2 = node.getValue(attributeIndex, majorLayerIndex);
	
				if (slow1 > 1e-6 && slow2 > 1e-6)
					travelTime += dkm * 0.5 * (slow1 + slow2);
	
				slow1 = slow2;
				previous = node;
			}
		}

		return travelTime;
	}

	/**
	 * Returns mean layer thickness across the segment.
	 * 
	 * @return The segment's mean layer thickness.
	 * @throws GeoTessException 
	 */
	public double getLayerThickness() throws GeoTessException
	{
		double thickness = 0;
		for (GeoTessPosition node : nodes)
			thickness += node.getLayerThickness(majorLayerIndex);
		//return thickness / (nodes.size() - 1);
		return thickness / nodes.size();
	}

	/**
	 * Makes this segment lie within a single layer (zero thickness).
	 * 
	 * @throws BenderException
	 */
	protected void makeThin() throws BenderException
	{
		if (nodes.size() > 2)
		{
			nodes.clear();
			nodes.add(first);
			nodes.add(last);
		}

		if (first.getIndex() != last.getIndex())
		{
			//if (first.getIndex() == last.getIndex() + 1)
			if (first.getIndex() > last.getIndex())
				first.copy(last);
			//else if (last.getIndex() == first.getIndex() + 1)
			else if (last.getIndex() > first.getIndex())
				last.copy(first);
			else
				throw new BenderException(ErrorCode.FATAL, String.format(
						"Difference between first.userIndex(%d) and last.userIndex(%d) is not 1",
						first.getIndex(), last.getIndex()));
		}
	}

	/**
	 * Returns a string defining the contents of this RaySegment.
	 * 
	 * @return A string defining the contents of this RaySegment.
	 */
	public String segmentToString()
	{
		StringBuffer buf = new StringBuffer();
		iterator = nodes.listIterator();
		while (iterator.hasNext())
		{
			GeoTessPosition node = iterator.next();
			buf.append(node.getPositionString());
			buf.append(String.format("   x=%10.6f  %2d  %s%n",
					ray.getReceiver().distanceDegrees(node),
					node.getIndex(),
					node.getModel().getMetaData().getLayerName(node.
							getIndex())));
		}
		return buf.toString();
	}

	/**
	 * For each node from first to last, exclusive, add a record with the
	 * following information:
	 * <br>Distance (degrees)
	 * <br>Depth (km)
	 * <br>Angle between ray direction and vertical (degrees)
	 * <br>Velocity (km/sec)
	 * <br>Magnitude of velocity gradient (sec^-^1)
	 * <br>Angle between velocity gradient and vertical (degrees)
	 * <br>Ray Parameter (sec/km)
	 *
	 * @param rayParameters ArrayList
	 * @param origin GeoTessPosition
	 * @param property int
	 * @throws GeoTessException 
	 */
	public void getRayParameterInfo(ArrayList<ArrayList<Double>> rayParameters,
																	GeoTessPosition origin,
																	GeoAttributes property) throws GeoTessException
	{
		iterator = nodes.listIterator();
		previous = iterator.next();

		double[] rayDirection = new double[3];
		double[] gradient = new double[3];
		double velocity, gradientMag;

		while (iterator.hasNext())
		{
			next = iterator.next();

			ArrayList<Double> record = new ArrayList<Double> ();

			//InterpolatedNodeLayered node = previous.clone();
			//      InterpolatedNodeLayered node = ray.bender.geoModel.getInterpolatedNodeLayered(
			//          previous.getElement(), previous.getGeoVector());

			node = previous.deepClone();

			node.setIntermediatePosition(previous, next, 0.5);

			record.add(node.distanceDegrees(origin));
			record.add(node.getDepth());

			// angle between ray direction and vertically up.
			next.minus(previous, rayDirection);
			VectorUnit.normalize(rayDirection);
			record.add(toDegrees(VectorUnit.angle(node.getVector(), rayDirection)));

			// velocity.
			int attrIndx = node.getModel().getMetaData().getAttributeIndex(property.name());
			velocity = node.getValue(attrIndx, majorLayerIndex);
			record.add(velocity);

			// gradient magnitude
			//node.getGradient(property, majorLayer, gradient);
			if (ray.bender.getGradientCalculatorMode() == GradientCalculationMode.ON_THE_FLY)
				ray.bender.getGradientCalculator().getGradient(node.getVector(),
						       node.getRadius(),  attributeIndex,  majorLayerIndex,
						       true,  gradient);
			else
			  node.getGradient(attributeIndex, majorLayerIndex, true, gradient);

			gradientMag = VectorUnit.normalize(gradient);
			record.add(gradientMag);

			if (gradientMag < 1e-9)
				gradient = node.getVector();

			// angle between gradient direction and vertically up.
			record.add(toDegrees(VectorUnit.angle(node.getVector(), gradient)));

			// ray parameter
			record.add(sin(VectorUnit.angle(rayDirection, gradient)) / velocity);

			rayParameters.add(record);

			previous = next;
		}
	}

	/**
	 * Resample the RaySegment. Add samples to the supplied array of samples.
	 * The first sample should be taken 'start' distance into this RaySegment
	 * and samples are to be spaced dkm apart.
	 *
	 * @param start double distance from first node of this segment to first
	 *   sample to be taken, in km.
	 * @param dkm double spacing between samples, in km.
	 * @param samples ArrayList add new samples to this list.
	 * @return double after last sample is added, which is still in this
	 *   RaySegment, find distance from last node in this segment to last
	 *   sample. Subtract that number from dkm and return the result.
	 * @throws GeoTessException 
	 */
	public double getSamples(double start, double dkm,
													 ArrayList<GeoTessPosition> samples)
				 throws GeoTessException
	{
		iterator = nodes.listIterator();
		previous = node = iterator.next();
		double increment;
		while (iterator.hasNext())
		{
			next = iterator.next();
			increment = next.getDistance3D(previous);
			while (start <= increment)
			{
				node = GeoTessPosition.getGeoTessPosition(previous,  next, start / increment);
				samples.add(node);
				start += dkm;
			}
			start -= increment;
			previous = next;
		}

		return start;
	}

	/**
	 * Divide this ray segment up into equal sized intervals such that each 
	 * interval is no larger than dkmMax.  Interval lengths are measured along 
	 * the actual ray path, not along the surface.  Actual interval lengths will
	 * be somewhat smaller than dkMax such that an integral number of equal size
	 * intervals will fit along the ray path.  The first sample will correspond
	 * to the first node on the RaySegment and the last sample will correspond to
	 * the last node of the RaySegment.  If getPathLength is very small, no sammples
	 * are added.
	 * 
	 * @param dkmMax desired spacing, in km
	 * @param testSamples if true, the new samples are tested to ensure that they are
	 * evenly spaced and that new samples fall on a line connecting two of the original
	 * points.  This is expensive and should only be set to true when developing new
	 * algorithms that use resampling.
	 * @return new list of GeoVectorRay objects that lie along the ray.
	 * @throws Exception 
	 */
	public ArrayList<GeoVector> resample(double dkmMax, boolean testSamples) throws Exception
	{
	    if (getPathLength() < BenderConstants.MIN_LAYER_THICKNESS)
		return new ArrayList<GeoVector>();

	    ArrayList<double[]> points = new ArrayList<double[]>(nodes.size());
	    for (GeoTessPosition n : nodes)
		points.add(n.get3DVector());

	    ArrayList<double[]> newsamples = ResampleRay.resample(points, dkmMax, testSamples);
	    
	    ArrayList<GeoVector> samples = new ArrayList<GeoVector>(newsamples.size());
	    for (double[] point : newsamples)
		samples.add(new GeoVectorRay(point, majorLayerIndex, waveType));
	    
	    return samples;
	}
	
	/**
	 * Set this segments index relative to the owning ray.
	 * 
	 * @param indx The new ray segment index.
	 */
	protected void setSegmentIndex(int indx)
	{
		segmentIndex = indx;
	}
//
//	/**
//	 * Set this segments index relative to the owning branch.
//	 * 
//	 * @param indx The new branch segment index.
//	 */
//	protected void setBranchSegmentIndex(int indx)
//	{
//		branchSegmentIndex = indx;
//	}

	/**
	 * Set this segments Snell's law fitness.
	 * 
	 * @param fitness The segments Snell's law fitness.
	 */
	public void setFitness(double fitness)
	{
		this.fitness = fitness;
	}
	
	/**
	 * Base class reflection check flag. Always false. Overridden by concrete
	 * classes.
	 * 
	 * @return True if this segment is a reflection segment.
	 * @throws GeoTessException
	 */
	protected boolean checkReflection() throws GeoTessException
	{
		return false;
	}
	
	/**
	 * Similar to checkReflection except the current setting is returned instead
	 * of checking to see if the reflective state exists. False always for the
	 * base class. Overridden by the concrete class.
	 * 
	 * @return True if this segment is a reflection segment.
	 */
	public boolean isReflection()
	{
		return false;
	}
	
	/**
	 * Special fixed type reflection segment. Always false. Overridden by the
	 * RaySegmentFixedReflection concrete class as true.
	 * 
	 * @return True if this segment is a fixed reflection segment.
	 */
	public boolean isFixedReflection()
	{
		return false;
	}
	
	/**
	 * Returns the RayDirection of this segment. Overridden by concrete classes.
	 * 
	 * @return The segments RayDirection setting.
	 */
	public RayDirection getRayDirectionChangeType()
	{
		return null;
	}
//
//	/**
//	 * Returns the debug node movement statistics 
//	 */
//	public void turnOnNodeMovementStatistics()
//	{
//		nodeMovementStats = new Statistic();
//	}

	/**
	 * This method will completely remove an active segment from the active
	 * segment list. It sets the upper most node of the segment lower than this
	 * segment to the upper most node of this segment. It readjusts the active
	 * list segments to remove this segment from it's doubly linked list.
	 * 
	 * This method recalculates the segments travel time but does not ensure that
	 * the owning branch/ray travel time are adjusted accordingly. This method
	 * does not test to see if the segment should be removed. This should be
	 * done before calling this method. If the segment is not active and this
	 * method is called the method exits with no changes..
	 * 
	 * Note: This method does not change the owning branch first segment and
	 * last segment reference, should this segment become said reference, and it
	 * does not decrement the active segment count. The caller must do this before
	 * or after this call.
	 * 
	 * @throws GeoTessException
	 */
	protected void inactivateSegment() throws GeoTessException
	{
		if (!isActive) return;
		
		// If up-going then previous is the lower layer and the last point of previous
		// is set to this last point. If down-going then next is the lower layer and
		// the first point of next is set to the this first point.
		
		if (first.getIndex() > last.getIndex())
		{
			// down-going ... set first point of next to this first point.

			if (nextActiveSegment == null)
				throw new GeoTessException("Error: Lower Layer is not defined .. cannot remove last layer ...");

			nextActiveSegment.setFirstNode(first);

			// set next segments previous to this segment previous and the previous
			// segment (if defined) next to this next

			nextActiveSegment.prevActiveSegment = prevActiveSegment;
			if (prevActiveSegment != null)
				prevActiveSegment.nextActiveSegment = nextActiveSegment;

			nextActiveSegment.travelTime = nextActiveSegment.pathLength = Globals.NA_VALUE;
			nextActiveSegment.getSegmentTravelTime();		
		}
		else
		{
			// up-going ... set last point of previous to this last point.

			if (prevActiveSegment == null)
				throw new GeoTessException("Error: Lower Layer is not defined .. cannot remove last layer ...");

			prevActiveSegment.setLastNode(last);

			// set previous segments next to this segment next and the next
			// segment (if defined) previous to this previous

			prevActiveSegment.nextActiveSegment = nextActiveSegment;
			if (nextActiveSegment != null)
				nextActiveSegment.prevActiveSegment = prevActiveSegment;
			
			prevActiveSegment.travelTime = prevActiveSegment.pathLength = Globals.NA_VALUE;
			prevActiveSegment.getSegmentTravelTime();		
		}
		
    // make inactive and exit

		isActive = false;
	}

	/**
	 * This method will completely reinsert an inactive segment back into the
	 * active segment list. It removes any nodes in the first active segment lower
	 * than this one (up-going or down-going) that lie in or above this segments
	 * major layer. It sets this segments upper most node to the upper most node
	 * of the next active lower segment and readjusts this segments lower node
	 * (and the next active segments upper node) to lie on a line of the lower
	 * segment nodes). Finally, it readjusts all internal bend nodes of this
	 * segment to lie on the line from its new first and last node positions.
	 * 
	 * This method recalculates the segments travel time but does not ensure that
	 * the owning branch/ray travel time are adjusted accordingly. This method
	 * does not test to see if the segment should be reinserted. This should be
	 * done before calling this method. If the segment is inactive and this method
	 * is called the segment is reinserted into the active list.
	 * 
	 * Note: This method does not change the owning branch first segment and
	 * last segment reference, should this segment become said reference, and it
	 * does not increment the active segment count. The caller must do this on
	 * return.
	 * 
	 * @throws GeoTessException
	 */
	protected void activateSegment() throws GeoTessException
	{
		if (isActive) return;

		if (first.getIndex() > majorLayerIndex)
		{
			// downgoing ... find first active lower segment and first active upper
			// segment (if any)
			
			RaySegment lowerSegment = nextInitialSegment;
			while (!lowerSegment.isActive)
				lowerSegment = lowerSegment.nextInitialSegment;
			
			RaySegment upperSegment = prevInitialSegment;
			while ((upperSegment != null) && !upperSegment.isActive)
				upperSegment = upperSegment.prevInitialSegment;

			// insert this segment
			
			lowerSegment.prevActiveSegment = this;
			if (upperSegment != null)
				upperSegment.nextActiveSegment = this;
			
			nextInitialSegment = lowerSegment;
			prevInitialSegment = upperSegment;

			// find first node in lowerSegment (after first) that lies in layer
			// majorLayerIndex. Remove any others that lie in layer index > than
			// majorLayerIndex.
			
			ListIterator<GeoTessPosition> nodeIter = lowerSegment.nodes.listIterator();
			nodeIter.next();
			GeoTessPosition bottomLineNode = nodeIter.next();
			while (bottomLineNode.getLayerId() >= majorLayerIndex)
			{
				nodeIter.remove();
				bottomLineNode = nodeIter.next();
			}

			// lowerSegment has now removed any internal nodes that lie in or above
			// this segments layer. It has also found the first internal node that
			// lies in the lowerSegments layer, which is now the second node in the
			// node list. The line for node positioning is from this node to the
			// current first node in lowerSegment.
			
			// set the first node in this segment to the first node from the
			// lowerSegment
			
			setFirstNode(lowerSegment.first);
			
			// calculate the last node for this segment and the first node for the
			// lower segment on a line from first to bottomLineNode
			
			double ra = bottomLineNode.getRadiusTop(majorLayerIndex);
			ra += first.getRadiusTop(majorLayerIndex);
			ra /= 2.0;
			double a = (ra - bottomLineNode.getRadius()) /
								 (first.getRadius() - bottomLineNode.getRadius());

			// Reset the segments last node to an intermediate location along
			// the first to the last direction and move it to the interface.
			// set the lower segments first node to this node.
			last.setIntermediatePosition(bottomLineNode, first, a, majorLayerIndex);
      lowerSegment.setFirstNode(last);
		}
		else if (last.getIndex() > majorLayerIndex)
		{
			// upgoing ... find first active lower segment and first active upper
			// segment (if any)
			
			RaySegment lowerSegment = prevInitialSegment;
			while (!lowerSegment.isActive)
				lowerSegment = lowerSegment.prevInitialSegment;
			
			RaySegment upperSegment = nextInitialSegment;
			while ((upperSegment != null) && !upperSegment.isActive)
				upperSegment = upperSegment.nextInitialSegment;

			// insert this segment
			
			lowerSegment.nextActiveSegment = this;
			if (upperSegment != null)
				upperSegment.prevActiveSegment = this;
			
			prevInitialSegment = lowerSegment;
			nextInitialSegment = upperSegment;

			// find last node in lowerSegment (before the last) that lies in or
			// above majorLayerIndex. Remove any others that lie in layer index > than
			// than majorLayerIndex.
			
			ListIterator<GeoTessPosition> nodeIter = lowerSegment.nodes.listIterator(nodes.size()-1);
			nodeIter.previous();
			GeoTessPosition bottomLineNode = nodeIter.previous();
			while (bottomLineNode.getLayerId() >= majorLayerIndex)
			{
				nodeIter.remove();
				bottomLineNode = nodeIter.previous();
			}

			// lowerSegment has now removed any internal nodes that lie in or above
			// this segments layer. It has also found the first internal node that
			// lies in the lowerSegments layer, which is now the second to last node
			// in the node list. The line for node positioning is from this node to the
			// current last node in lowerSegment.
			
			// set the last node in this segment to the last node from the
			// lowerSegment

			setLastNode(lowerSegment.last);
			
			// calculate the first node for this segment and the last node for the
			// lower segment on a line from last to bottomLineNode
			
			double ra = bottomLineNode.getRadiusTop(majorLayerIndex);
			ra += last.getRadiusTop(majorLayerIndex);
			ra /= 2.0;
			double a = (ra - bottomLineNode.getRadius()) /
								 (last.getRadius() - bottomLineNode.getRadius());

			// Reset the segments first node to an intermediate location along
			// the last to the first direction and move it to the interface.
			// set the lower segments last node to this node.
			first.setIntermediatePosition(bottomLineNode, last, a, majorLayerIndex);
      lowerSegment.setLastNode(first);
		}
    
    // now loop over all intermediate nodes in this segment and set them on
    // a line from the first to the last. Reset active flag and exit
    
    repositionInternalNodes();
		isActive = true;
	}
	
	private void repositionInternalNodes() throws GeoTessException
	{
		// loop over all internal nodes (second node to second-t-last node) and
		// reset their position based on the first and last node of this segment
		
		ListIterator<GeoTessPosition> nodeIter = nodes.listIterator();
		nodeIter.next();
		GeoTessPosition rePosNode = nodeIter.next();
		int m = 1;
    while (rePosNode != last)
    {
			double f = (double) m++ / (nodes.size() - 1);
			rePosNode.setIntermediatePosition(first, last, f);
    	rePosNode = nodeIter.next();
    }

    // reset travel time
    travelTime = pathLength = Globals.NA_VALUE;
    getSegmentTravelTime();		
	}
}
