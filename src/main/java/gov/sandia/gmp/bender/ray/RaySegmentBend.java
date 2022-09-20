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

import static java.lang.Math.sqrt;

import java.util.LinkedList;
import java.util.ListIterator;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.bender.BenderConstants.GradientCalculationMode;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;
import gov.sandia.gmp.bender.BenderConstants.RayStatus;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * A RaySegment that supports Ray Bending on the nodes of the segment. This
 * concrete class is defined for all segments in a ray defined across a non-
 * zero thickness interface layers. The primary Um Thurber bender is defined
 * in the bendNode() method which bends a single node that lies between two
 * adjacent fixed neighbors.
 * 
 * @author jrhipp
 *
 */
public class RaySegmentBend extends RaySegment
{
	protected final double minLayerThicknessNoBend = .1; // (km)
	protected final double maxLayerThicknessOnlyBend = 1.0; // (km)
	
	/**
	 * If either end-point of this segment has radius greater than
	 * the radius of the top interface of the model (the surface 
	 * of the solid earth), then aboveModel is true.
	 */
	protected boolean aboveModel;

	/**
	 * Assorted arrays used in bend().
	 */
	private double[] gradient = new double[3];
	private double[] x        = new double[3];
	private double[] n        = new double[3];
	private double[] vn       = new double[3];

	private double vmid, dot, xlen, c, rc;
	protected double rtop, rbottom;

	/**
	 * Standard constructor.
	 * 
	 * @param ray       The Ray that owns this RayBranch that owns this segment.
	 * @param branch    The RayBranch that owns this segment.
	 * @param nodes     The nodes defining this segment.
	 * @param attributeIndex The index of the wave type in the underlying GeoTessModel.
	 * @throws GeoTessException
	 */
	public RaySegmentBend(Ray ray, RayBranch branch, LinkedList<GeoTessPosition> nodes,
			                  int attributeIndex, RaySegment prevSegment) throws GeoTessException
	{
  	super(ray, branch, nodes, attributeIndex, prevSegment);

		aboveModel = first.isAboveModel() || last.isAboveModel();
	}

	/**
	 * Returns the segments RayDirection as an UPGOING or DOWNGOING ray.
	 */
	@Override
  public RayDirection getSegmentType()
	{
		if (nodes.getFirst().getIndex() > nodes.getLast().getIndex())
			return RayDirection.DOWNGOING;
		else
			return RayDirection.UPGOING;
	}

	/**
	 * Returns the RayType for this segment as UNKNOWN. This method is overridden
	 * by the derived type RaySegmentBottom.
	 */
	@Override
  protected RayType getRayType() throws GeoTessException
  {
  	return RayType.UNKNOWN;
  }

//	/**
//	 * Set up to apply bending algorithm of Um and Thurber to three adjacent nodes.
//	 * The method initializes the bend node (node) to midpoint between next and
//	 * previous, checks aboveModel settings and the next/previous separation
//	 * distance before calling the Um and Thurber algorithm. On exit the position
//	 * of node is modified but positions of previous and next are not.
//	 * 
//	 * @throws GeoTessException 
//	 */
//	protected void bend3nodesSaveMovement() throws GeoTessException
//	{
//		double[] beforePosition = node.get3DVector().clone();
//		bend3nodes();
//		nodeMovementStats.add(Vector3D.distance3D(beforePosition, node.get3DVector()));
//	}

	/**
	 * Set up to apply bending algorithm of Um and Thurber to three adjacent nodes.
	 * The method initializes the bend node (node) to midpoint between next and
	 * previous, checks aboveModel settings and the next/previous separation
	 * distance before calling the Um and Thurber algorithm. On exit the position
	 * of node is modified but positions of previous and next are not.
	 * 
	 * @throws GeoTessException 
	 */
	protected void bend3nodes() throws GeoTessException
	{
		// position node half way in between previous and next
	  node.setIntermediatePosition(previous, next, 0.5);

	  // check aboveModel and next/previous separation distance
		if (homogeneousConstantVelocityLayer || bendNodeCheckRadius()) return;

		double thickness = node.getLayerThickness();
		if (thickness <= minLayerThicknessNoBend)
			return; // average node only (already averaged)
		else if (thickness >= maxLayerThicknessOnlyBend)
			bendNode(); // bend only
		else
		{
			GeoTessPosition avgNode = node.deepClone();
			bendNode();
			double f = (thickness - minLayerThicknessNoBend) /
					 			 (maxLayerThicknessOnlyBend - minLayerThicknessNoBend);
			node.setIntermediatePosition(avgNode, node, f);
		}

		// make sure node is in major layer
		if (!aboveModel)
		{
			if (node.getRadius() < node.getRadiusBottom(majorLayerIndex))
				node.setRadius(majorLayerIndex, node.getRadiusBottom(majorLayerIndex));
			else if (node.getRadius() > node.getRadiusTop(topLayerIndex))
				node.setRadius(majorLayerIndex, node.getRadiusTop(topLayerIndex));

			//node.setRadiusConstrained(majorLayerIndex);
		}
	}

	/**
	 * Apply bending algorithm of Um and Thurber to three adjacent nodes:
	 * previous, node and next.  Position of node is modified but positions
	 * of previous and next are not.
	 * @throws GeoTessException 
	 */
	protected void bendNode() throws GeoTessException
	{
		if (ray.bender.getGradientCalculatorMode() == GradientCalculationMode.ON_THE_FLY)
			ray.bender.getGradientCalculator().getGradient(node.getVector(),
		             node.getRadius(),  attributeIndex,  majorLayerIndex,
		             true,  gradient);			
		else
		  node.getGradient(attributeIndex, majorLayerIndex, true, gradient);

		// make sure the gradient is large enough to perform the operation
		// (i.e. length > 1e-8)

		if (Vector3D.lengthSquared(gradient) > 1e-16)
		{
			// get velocity at midpoint
			vmid = 1. / node.getValue(attributeIndex, majorLayerIndex);

			// find the dot product of gradV . (Xk+1 - Xk-1)
			dot = VectorUnit.dot(x, gradient);

			// normalize x to unit vector using its length (xlen ... guaranteed to be
			// > 1e-6 to get to this point)
			
			Vector3D.scale(x, 1.0/xlen);

			// set n = (-dot/len) * x + gradient
			
			Vector3D.addMult(n, -dot/xlen, x, gradient);
			//for (int i = 0; i < 3; ++i)
			//	n[i] = gradient[i] - x[i] * dot / xlen;

			// normalize n to unit vector.
			VectorUnit.normalize(n);

			c = 0.5 * previous.getValue(attributeIndex, majorLayerIndex) +
			    0.5 * next.getValue(attributeIndex, majorLayerIndex);

			rc = (c * vmid + 1) / (4. * c * VectorUnit.dot(n, gradient));

			// len is Xk+1 - Xk-1, not Xk+1-Xk as in Thurber.  Hence the 8.
			rc = sqrt(rc * rc + xlen * xlen / (8. * c * vmid)) - rc;

			// get the full vector (unit vector * radius) and
			// add offset to full vector (vn += rc * n)

			vn = node.get3DVector().clone();
			Vector3D.multIncrement(vn, rc, n);
			//for (int i = 0; i < 3; ++i)
			//	vn[i] += n[i] * rc;

			// set node's unit vector and radius to a copy of vn, normalized
			// to unit length.
			node.set(vn, VectorUnit.normalize(vn));
		}
	}

	/**
	 * Check the radius of the node to be bent and make sure it lies in the model
	 * (not above) and within the major layer. Also, check the separation
	 * distance between the next and previous nodes used to bend the node. If the
	 * nodes radial position is not bendable or the bend segment (next to previous)
	 * is to small (<10^-6) then return true (not bendable).
	 * 
	 * @param  maxLayer The maximum layer of previous and next. This may be larger
	 *                  than majorLayerIndex if the segment(s) above the current
	 *                  segment are pinched out and inactivated.
	 * @return Boolean, which if true the node will not be bent.
	 * @throws GeoTessException
	 */
	protected boolean bendNodeCheckRadius() throws GeoTessException
	{
		if (aboveModel && node.getRadius() > node.getRadiusTop(
					ray.bender.getGeoTessModel().getNLayers()-1))
			return true;

		// aboveModel is true when either first or last node in this
		// segment are above surface of model.
		if (!aboveModel)
		{
			double r = node.getRadius();
			if (r <= node.getRadiusBottom(majorLayerIndex))
				node.setRadius(majorLayerIndex, node.getRadiusBottom(majorLayerIndex) + .001);
			else if (r >= node.getRadiusTop(topLayerIndex))
				node.setRadius(majorLayerIndex, node.getRadiusTop(topLayerIndex) - .001);

			//double r = node.setRadiusConstrained(majorLayerIndex);
			//if ((r == node.getRadiusTop(majorLayerIndex)) || (r == node.getRadiusBottom(majorLayerIndex)))
			//node.setIntermediateUnitVectorPosition(previous, next, 0.5);
		}

		// set x = next - previous, i.e., Xk+1 - Xk-1
		next.minus(previous, x);

		// if previous and next are super close together, node will simply be
		// the midpoint.

		xlen = VectorUnit.length(x);
		if (xlen < 1e-6)
			return true;

		// bend the node

		return false;
	}

	/**
	 * Performs the segment bend operation by calling bendNodesForward or
	 * bendNodesBackward for all nodes that define this segment.
	 * 
	 * @throws GeoTessException 
	 * @throws GeoVectorException
	 */
	@Override
	public void bend() throws GeoTessException
	{
		if (nodes.size() > 2)
		{
			//if(nodeMovementStats != null) nodeMovementStats.reset();
			
			// reset travel time so that a new calculation is performed after the bend.

			travelTime = pathLength = Globals.NA_VALUE;

			int decCnt = 0;
			if (nodes.size() % 2 == 0) decCnt = 1; // even node count
			int n = nodes.size() / 2;
			boolean bendFromMiddleOut = true;
			if (bendFromMiddleOut)
			{
				iterator = nodes.listIterator(n-1);
			  bendNodesForward(n-decCnt, iterator);
				iterator = nodes.listIterator(n+1);
				bendNodesBackward(n-1, iterator);
			}
			else // bend nodes from ends toward middle
			{
				iterator = nodes.listIterator(nodes.size());
				bendNodesBackward(n-1, iterator);
				iterator = nodes.listIterator();
			  bendNodesForward(n-decCnt, iterator);
			}
		}
	}

	/**
	 * Bends the next n nodes pointed to by the input ListIterator
	 * (nodeIterator) beginning with the iterators input position + 1 and
	 * ending with input position + n. The list iterator must be setting on
	 * the node just before the first node to be bent on entry. On exit the
	 * iterator will be setting on the position of the last node bent.
	 * 
	 * @param n            The number of nodes to be bent.
	 * @param nodeIterator The node iterator setting on the position just
	 *                     before the first node to be bent on entry.
	 * @throws GeoTessException
	 */
	private void bendNodesForward(int n, ListIterator<GeoTessPosition> nodeIterator)
			    throws GeoTessException
	{
		for (int i = 0; i < n; ++i)
		{
			// get the next three nodes
			previous = nodeIterator.next();
			node = nodeIterator.next();
			next = nodeIterator.next();

			// back up two
			nodeIterator.previous();
			nodeIterator.previous();

			//bend node
//			if(nodeMovementStats != null)
//				this.bend3nodesSaveMovement();
//			else
			  bend3nodes();		
		}
	}

	/**
	 * Bends the previous n nodes pointed to by the input ListIterator
	 * (nodeIterator) beginning with the iterators input position - 2 and
	 * ending with input position - n - 1. The list iterator must be setting on
	 * the node two after the first node to be bent on entry. On exit the
	 * iterator will be setting on the position of the node after the last
	 * node bent.
	 * 
	 * @param n            The number of nodes to be bent.
	 * @param nodeIterator The node iterator setting on the position two
	 *                     after the first node to be bent on entry.
	 * @throws GeoTessException
	 */
	private void bendNodesBackward(int n, ListIterator<GeoTessPosition> nodeIterator)
			    throws GeoTessException
	{
		for (int i = 0; i < n; ++i)
		{
			// get previous three nodes
			next = nodeIterator.previous();
			node = nodeIterator.previous();
			previous = nodeIterator.previous();

			// step forward two
			nodeIterator.next();
			nodeIterator.next();

			//bend node
//			if(nodeMovementStats != null)
//				this.bend3nodesSaveMovement();
//			else
			  bend3nodes();		
		}
	}

	/**
	 * Doubles the nodes along this segment and then bends all of the new nodes
	 * using the bend3nodes() method. The nodes are only doubled if there current
	 * node spacing exceeds the input threshold.
	 * 
	 * @param threshold The value of node spacing above which causes the segment
	 *                  node population to be doubled and bent.
	 * @throws GeoTessException 
	 */
	@Override
	protected void doubleNodesBend(double threshold) throws GeoTessException
	{
		if (homogeneousConstantVelocityLayer) return;

		if (getSpacing() > threshold)
		{
			iterator = nodes.listIterator();
			previous = iterator.next();
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
				ray.setStatus(RayStatus.DOUBLED);

				// prepare for next iteration
				previous = next;
			}
		}
	}

	/**
	 * Doubles this segments node population by placing new nodes between each
	 * pair of existing nodes.
	 * 
	 * @throws GeoTessException 
	 */
	protected void doubleNodes() throws GeoTessException
	{
		iterator = nodes.listIterator();
		previous = iterator.next();
		while (iterator.hasNext())
		{
			// make a new node
			//node = previous.deepClone();
			node = GeoTessPosition.getGeoTessPosition(previous);

			// add it to list immediately following previous.
			iterator.add(node);

			// get the node immediately following the new node.
			next = iterator.next();

			// put new node half way between previous and next
			node.setIntermediatePosition(previous, next, 0.5);
			if (!aboveModel)
			  node.setRadiusConstrained(majorLayerIndex);
			node.setIndex(previous.getIndex());

			// prepare for next iteration
			previous = next;
		}
		travelTime = pathLength = Globals.NA_VALUE;
	}
}
