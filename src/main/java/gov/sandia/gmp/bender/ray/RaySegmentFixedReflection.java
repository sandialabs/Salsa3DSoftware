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

import java.util.LinkedList;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;

/**
 * A special 3 node segment that represents a fixed interface layer position.
 * This segment only exists as a connector of neighboring RayBranches and always
 * represents a top-side or bottom-side reflection point.
 *  
 * @author jrhipp
 *
 */
public class RaySegmentFixedReflection extends RaySegment
{
	/**
	 * The RayDirection type for this segment. It is always set as
	 * TOP_SIDE_REFLECTION or BOTTOM_SIDE_REFLECTION.
	 */
	private RayDirection rayDirctnChngType;

	/**
	 * Standard constructor.
	 * 
	 * @param ray       The Ray that owns this segment.
	 * @param branch    The RayBranch that owns this segment.
	 * @param nodes     Always 3 nodes set at the same position.
	 * @param waveIndex The wave type index (P or S) of the underlying GeoTessModel.
	 *									This value is irrelevant for this segment since the
	 *									segment really has no thickness and its wave type is
	 *									never used directly.
	 * @param rdc       The RayDirection. Either TOP_SIDE_REFLECTION or
	 * 									BOTTOM_SIDE_REFLECTION.
	 * @throws GeoTessException
	 */
  public RaySegmentFixedReflection(Ray ray, RayBranch branch, LinkedList<GeoTessPosition> nodes,
                                   int waveIndex, RayDirection rdc) throws GeoTessException
  {
    super(ray, branch, nodes, waveIndex);
    rayDirctnChngType = rdc;
  }

	/**
	 * Standard constructor.
	 * 
	 * @param ray       The Ray that owns this segment.
	 * @param branch    The RayBranch that owns this segment.
	 * @param nodes     Always 3 nodes set at the same position.
	 * @param waveIndex The wave type index (P or S) of the underlying GeoTessModel.
	 *									This value is irrelevant for this segment since the
	 *									segment really has no thickness and its wave type is
	 *									never used directly.
	 * @param rdc       The RayDirection. Either TOP_SIDE_REFLECTION or
	 * 									BOTTOM_SIDE_REFLECTION.
	 * @throws GeoTessException
	 */
  public RaySegmentFixedReflection(Ray ray, RayBranch branch, LinkedList<GeoTessPosition> nodes,
                                   int waveIndex, RayDirection rdc, RaySegment prevSegment) throws GeoTessException
  {
    super(ray, branch, nodes, waveIndex, prevSegment);
    rayDirctnChngType = rdc;
  }

	/**
	 * Returns the middle node of this fixed reflection segment.
	 */
	@Override
	public GeoTessPosition getMiddleNode()
	{
		return nodes.get(1);
	}

	/**
	 * Returns the RayType for this fixed reflection segment (always REFLECTION).
	 */
	@Override
	public RayType getRayType()
	{
		return RayType.REFLECTION;
	}
	
	/**
	 * Returns the RayDirection type for this fixed reflection segment.
	 */
	@Override
	public RayDirection getRayDirectionChangeType()
	{
		return rayDirctnChngType;
	}

	/**
	 * Returns the RayDirection type for this fixed reflection segment.
	 */
	@Override
  public RayDirection getSegmentType()
	{
  	return rayDirctnChngType;
	}

  /**
   * Always returns true for a fixed reflection segment.
   */
  @Override
	public boolean isFixedReflection()
	{
		return true;
	}

  /**
   * Always returns true for a fixed reflection segment.
   */
  @Override
	public boolean isReflection()
	{
		return true;
	}

	/**
	 * A fixed reflection segment performs no node ray bending.
	 */
	@Override
	public void bend() throws GeoTessException
	{
		// does nothing
	}

	/**
	 * A fixed reflection segment performs no node doubling or ray bending.
	 */
	@Override
	protected void doubleNodesBend(double threshold) throws GeoTessException
	{
		// does nothing
	}

	/**
	 * Returns a zero travel time always.
	 *  									
	 * @return Zero travel time .
	 */
	@Override
	protected double getSegmentTravelTime() throws GeoTessException
	{
		return 0.0;
	}

	/**
	 * Returns a zero path length always.
	 *  									
	 * @return Zero path length.
	 */
	@Override
	public double getPathLength()
	{
		return 0.0;
	}

	/**
	 * Sets this fixed reflection position to the new latitude longitude position.
	 * The position still lies on the layer with which this fixed segment was 
	 * created.
	 * 
	 * @param lat The new segment latitude (radians).
	 * @param lon The new segment longitude (radians).
	 * @throws GeoTessException
	 */
	public void setPosition(double lat, double lon) throws GeoTessException
	{
		double[] newpos = nodes.get(1).getModel().getEarthShape().getVector(lat, lon);
		GeoTessPosition middle = nodes.get(1);
		middle.setTop(middle.getIndex(), newpos);
		nodes.get(0).copy(middle);
		nodes.get(2).copy(middle);
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
}
