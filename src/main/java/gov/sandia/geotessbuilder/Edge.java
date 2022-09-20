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
package gov.sandia.geotessbuilder;

/**
 * Edge represents the edge of a triangle. An Edge maintains references to the
 * following entities:
 * 
 * <ul>
 * <li>Two Triangle objects, side1 and side2, which are references to the two
 * triangles on either side of the Edge.
 * <li>A Vertex object that resides at the center of the Edge.
 * <li>A reference to the Edge that is the ancestor of this Edge. In other
 * words, if Edge A gets subdivided into two smaller Edges B and C, then both B
 * and C have their ancestor field set to a reference to A.
 * <li>A count of the number of times this Edge or any of its descendants got
 * subdivided. When Edge A got subdivided into Edges B and C, A's descendant
 * count was set to 1. If B was later subdivided into more Edges, A's descendant
 * count would be incremented to 2. If C were also subdivided, then A's
 * descendant count would be incremented to 3, etc.
 * 
 * </ul>
 * 
 * @author sandy
 *
 */
public class Edge {
	private Vertex vertex = null;

	private int nDivisions = 0;

	private Edge ancestor = null;

	protected Triangle side1 = null;
	protected Triangle side2 = null;

	public Edge() {
		// do nothing
	}

	public Edge(Triangle side1, Triangle side2) {
		setSides(side1, side2);
	}

	public void setSides(Triangle side1, Triangle side2) {
		this.side1 = side1;
		this.side2 = side2;
	}

	/**
	 * Specify the Vertex that is to reside at the center of this Edge.
	 * 
	 * @param center the newNode to set
	 */
	public void setVertex(Vertex center) {
		this.vertex = center;
	}

	/**
	 * @return a reference to the Vertex that resides at the center of this Edge.
	 */
	public Vertex getVertex() {
		return vertex;
	}

	/**
	 * @return the number of times this Edge, or its descendants, have been
	 *         subdivided.
	 */
	public int getNDivisions() {
		return nDivisions;
	}

	/**
	 * Increment the number of times this Edge has been subdivided. Also increment
	 * the subdivision count of all of its ancestors.
	 */
	public void incNDivisions() {
		++nDivisions;
		if (ancestor != null)
			ancestor.incNDivisions();
	}

	/**
	 * @return the ancestor
	 */
	public Edge getAncestor() {
		return ancestor;
	}

	public void setAncestor(Edge ancestor) {
		this.ancestor = ancestor;
	}

	/**
	 * Every Edge is defined by 2 Triangles, one on each side of the Edge. Given one
	 * of the triangles, return the other triangle. If the supplied triangle is not
	 * one of the Triangles that define this Edge, return null.
	 * 
	 * @param triangle
	 * @return
	 */
	public Triangle getNeighbor(Triangle triangle) {
		if (triangle == side1)
			return side2;
		if (triangle == side2)
			return side1;
		return null;
	}

	/**
	 * Retrieve the indices (0 to 2) of the three nodes that define one of the
	 * triangles that defines this Edge. The indices of the nodes are ordered such
	 * that i[0] is the node that is not contained by this Edge. i[0], i[1] and i[2]
	 * define the nodes in clockwise order when viewed from outside the unit sphere.
	 * The triangles of i have values between 0 and 2, inclusive.
	 * 
	 * @param triangle must equal either 1 or 2 indicating which of the two
	 *                 triangles are desired.
	 * @return
	 */
	int[] getNodeIndeces(int triangle) {
		int[] i = new int[3];
		if (triangle == 2)
			i[0] = side1.getNeighborIndex(side2);
		else
			i[0] = side2.getNeighborIndex(side1);

		i[1] = (i[0] + 1) % 3;
		i[2] = (i[1] + 1) % 3;
		return i;
	}

}
