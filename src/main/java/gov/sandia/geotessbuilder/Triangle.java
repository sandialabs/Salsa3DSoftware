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

import static java.lang.Math.PI;

import java.util.ArrayList;
import java.util.Collection;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.gmp.util.numerical.polygon.Polygon;
import gov.sandia.gmp.util.numerical.polygon.PolygonPoints;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * Triangle is essentially an array of 3 Vertex objects which define a spherical
 * triangle on the surface of a unit sphere. Besides being an array of 3 Vertex
 * objects, a Triangle knows the tessellation level upon which it resides, how
 * many sub triangles it was divided into (if any), who its neighbor triangles
 * are and its descendant if it was subdivided. Each triangle also has an index
 * and a boolean flag indicating whether it is 'marked' or 'unmarked'.
 * 
 * @author sballar
 * 
 */
public class Triangle extends ArrayList<Vertex> {
	private static final long serialVersionUID = -7415600127766038820L;

	/**
	 * Index has a getter and setter and is never modified internally.
	 */
	private int index;

	/**
	 * marked has getter and setter and is never modified internally.
	 */
	private int marked;

	// public boolean markedForRefinement;

	/**
	 * The tessellation level with which this Triangle is associated. It is set by
	 * this class during construction and cannot be changed externally. There is a
	 * getter but no setter.
	 */
	private int tessLevel;

	/**
	 * The edge level of the triangles on level 0 is 0. Subsequently, whenever a
	 * triangle is subdivided into 4 smaller triangles, the edge level of the
	 * smaller triangles is set equal to to the edge level of the ancestor plus 1.
	 * The implication is that edge level of a triangle reflects how many times its
	 * ancestors have been divided into 4 subtriangles.
	 */
	private int edgeLevel;

	/**
	 * The number of sub-triangles into which this triangle has been divided. Values
	 * will range from 0 to 4, inclusive.
	 */
	protected int nDescendants;

	/**
	 * The three Edge objects which define the edges of this Triangle.
	 */
	private Edge[] edges;

	/**
	 * The three Triangles that are the neighbors of this Triangle. Consider
	 * vertices i, j, k. Edge[i] is the edge that connects vertex[j] and vertex[k],
	 * i.e., it is the edge that connects the two nodes that do not contain
	 * vertex[i].
	 */
	private Triangle[] neighbors;

	/**
	 * If this Triangle triangle is not a member of the top tessellation level, then
	 * descendant is a triangle on the next higher tessellation level. If this
	 * triangle was subdivided into 4 triangles, then descendant is the center
	 * triangle of the 4 triangles into which it was subdivided.
	 */
	private Triangle descendant;

	/**
	 * A triangle on the next lower tessellation level that contains this triangle.
	 * The triangles on the lowest tessellation level (the icosahedron) do not have
	 * an ancestor and hence the ancestor field will be null.
	 */
	Triangle ancestor;

	private double[] circumCenter;
	
	/**
	 * A Polygon object defined by the three corners of the triangle.
	 */
	private Polygon polygon;

	/**
	 * Constructor that specifies the three Vertex objects that define the corners
	 * of the triangle (in clockwise order when viewed from outside the unit
	 * sphere), and the tessellation level upon which the new triangle resides.
	 * 
	 * @param ancestor  the triangle that is the ancestor of this triangle
	 * @param n1
	 * @param n2
	 * @param n3
	 * @param tessLevel
	 */
	protected Triangle(Triangle ancestor, Vertex n1, Vertex n2, Vertex n3, int tessLevel, int edgeLevel) {
		super(3);
		this.tessLevel = tessLevel;
		this.edgeLevel = edgeLevel;
		neighbors = new Triangle[3];
		edges = new Edge[] { new Edge(), new Edge(), new Edge() };
		this.ancestor = ancestor;
		add(n1);
		add(n2);
		add(n3);
	}

	/**
	 * Make a new triangle with the same vertices as the supplied triangle, but with
	 * a different tessellation level.
	 * 
	 * @param t
	 * @param tessLevel
	 */
	private Triangle(Triangle ancestor, int tessLevel) {
		super(ancestor);
		this.tessLevel = tessLevel;
		this.edgeLevel = ancestor.edgeLevel;
		this.ancestor = ancestor;
		neighbors = new Triangle[3];
		edges = new Edge[] { new Edge(), new Edge(), new Edge() };
	}

	/**
	 * Retrieve the solid angle subtended by this triangle, in square radians. This
	 * number times Earth radius squared is equal to the area of the spherical
	 * triangle. It is assumed that the corners of the triangle are not on same
	 * great circle.
	 * 
	 * @return double solid angle subtended by this triangle, in square radians.
	 */
	public double getSolidAngle() {
		double[][] c = new double[3][3];

		for (int i = 0; i < 3; i++)
			VectorUnit.crossNormal(get((i + 1) % 3).getArray(), get(i).getArray(), c[i]);

		double a = 2 * PI;
		for (int i = 0; i < 3; i++)
			a -= VectorUnit.angle(c[(i + 1) % 3], c[(i + 2) % 3]);
		return a;
	}

	/**
	 * Returns 1 if point is inside this Triangle, 0 if it is on the boundary, or -1
	 * if it is outside this Triangle.
	 * 
	 * @param point
	 * @return 1 (inside), 0 (on), or -1 (outside).
	 */
	public int inside(double[] point) {
		int x = 1; // assume point is inside.
		double stp;

		for (int i = 0; i < 3; i++) {
			stp = VectorUnit.scalarTripleProduct(get((i + 2) % 3).getArray(), get((i + 1) % 3).getArray(), point);

			// if point is on other side of any edge, then point is outside
			if (stp < -1e-15)
				return -1;

			// if stp is very small, then point cannot be inside.
			// If it is not outside, then it will be on the boundary.
			if (Math.abs(stp) < 1e-15)
				x = 0;
		}

		// point was not found to be outside.
		// it is either inside or on boundary.
		// If any of the 3 stp's were very small then point
		// is on boundary, otherwise it is inside.
		return x;
	}

	/**
	 * Returns true if the specified point is inside or on the boundary of this
	 * Triangle
	 * 
	 * @param point GeoVector
	 * @return boolean
	 */
	public boolean contains(double[] point) {
		return inside(point) > -1;
	}

	/**
	 * Returns true if the specified point is inside or on the boundary of this
	 * Triangle
	 * 
	 * @param points GeoVector
	 * @return boolean
	 */
	public boolean containsAny(Collection<? extends Vertex> points) {
		for (Vertex point : points)
			if (inside(point.getArray()) > -1)
				return true;
		return false;
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(String.format("%6d%n", index));
		for (int i = 0; i < size(); ++i)
			s.append(String.format("  %s%n", get(i).toString()));
		return s.toString();
	}

	/**
	 * Divide this triangle up into 4 new triangles which will reside on the next
	 * higher tessellation level. Updates all the Edge objects and sets the
	 * descendant of this triangle to the central triangle of the 4 new triangles.
	 * All tessLevels are set properly. Does nothing about neighbors.
	 * 
	 * @param triangles
	 */
	public void divide(Collection<Triangle> triangles) {
		// 0
		// / \
		// / \
		// / \
		// / \
		// / \
		// / \
		// / \
		// / \
		// 1 --------------- 2
		// / \ / \
		// / \ / \
		// / \ / \
		// / \ / \
		// / \ / \
		// / \ / \
		// / \ / \
		// / \ / \
		// 2 --------------- 0 --------------- 1
		//
		// make 3 new nodes in centers of the edges of the current triangle.
		//

		// visit each edge. If it has a node then make that node a corner of
		// descendant. If not, make a new Node.
		for (int i = 0; i < 3; i++)
			if (getEdges()[i].getVertex() == null)
				getEdges()[i].setVertex(new Vertex(get((i + 1) % 3), get((i + 2) % 3)));

		Vertex[] centerVertex = new Vertex[3];
		for (int i = 0; i < 3; ++i) {
			centerVertex[i] = getEdges()[i].getVertex();
			getEdges()[i].incNDivisions();
		}

		// make the center triangle out of the 3 supplied nodes.
		// The center triangle does not share edges with this triangle,
		// so don't call setAncestor on any of this triangle's edges.
		Triangle triangle = new Triangle(this, centerVertex[0], centerVertex[1], centerVertex[2], tessLevel + 1,
				edgeLevel + 1);
		this.descendant = triangle;
		triangles.add(triangle);

		// make the other 3 triangles
		for (int i = 0; i < 3; ++i) {
			triangle = new Triangle(this, get(i), centerVertex[(i + 2) % 3], centerVertex[(i + 1) % 3], tessLevel + 1,
					edgeLevel + 1);
			// these descendants share 2 of their edges with this triangle's
			// edges. Specify ancestor relationships.
			triangle.getEdges()[1].setAncestor(this.getEdges()[(i + 1) % 3]);
			triangle.getEdges()[2].setAncestor(this.getEdges()[(i + 2) % 3]);
			triangles.add(triangle);
		}

		nDescendants = 4;
	}

	/**
	 * Make transition triangles based on this triangle. This triangle will be
	 * divided into anywhere from one to four new triangles, depending on the number
	 * of populated edges in this triangle.
	 * 
	 * @return
	 * @throws GeoTessException
	 */
	public ArrayList<Triangle> getTransitionTriangles() throws GeoTessException {

		ArrayList<Triangle> triangles = new ArrayList<Triangle>(4);

		// Find the Vertex objects at the center of each edge of the triangle,
		// none, some or all might be null, indicating that the neighbors of
		// this triangle did not add any nodes to the edge.
		Vertex[] newNodes = new Vertex[] { getEdges()[0].getVertex(), getEdges()[1].getVertex(),
				getEdges()[2].getVertex() };

		// count the number of new nodes that have been added to the middle of
		// each edge of this triangle
		int n = 0;
		for (int i = 0; i < 3; ++i)
			if (newNodes[i] != null)
				++n;

		Triangle t = null;

		if (n == 0) {
			// No neighbors added any nodes to this triangles edges.
			// Make a new Triangle with same vertices but located on
			// next tessellation level.
			this.nDescendants = 1;
			t = new Triangle(this, tessLevel + 1);
			this.descendant = t;
			t.getEdges()[0].setAncestor(this.getEdges()[0]);
			t.getEdges()[1].setAncestor(this.getEdges()[1]);
			t.getEdges()[2].setAncestor(this.getEdges()[2]);
			triangles.add(t);
			return triangles;
		} else if (n == 1) {
			// One of this Triangles neighbors set a new node at the
			// center of their shared Edge. Must split this triangle
			// into two triangles.
			for (int i = 0; i < 3; ++i) {
				if (newNodes[i] != null) {
					nDescendants = 2;
					t = new Triangle(this, get(i), newNodes[i], get((i + 2) % 3), tessLevel + 1, edgeLevel);
					this.descendant = t;
					t.getEdges()[1].setAncestor(this.getEdges()[(i + 1) % 3]);
					t.getEdges()[2].setAncestor(this.getEdges()[(i + 2) % 3]);
					// t.setTessLevel(tessLevel+1);
					triangles.add(t);

					t = new Triangle(this, get(i), get((i + 1) % 3), newNodes[i], tessLevel + 1, edgeLevel);
					t.getEdges()[0].setAncestor(this.getEdges()[i]);
					t.getEdges()[1].setAncestor(this.getEdges()[(i + 1) % 3]);
					triangles.add(t);
					return triangles;
				}
			}
		} else if (n == 2) {
			// two of this triangle's neighbors added vertices to this
			// triangle's edges. Need to split this triangle into three new
			// triangles.
			double d1, d2;
			for (int i = 0; i < 3; ++i)
				if (newNodes[i] == null) {
					nDescendants = 3;
					t = new Triangle(this, get(i), newNodes[(i + 2) % 3], newNodes[(i + 1) % 3], tessLevel + 1,
							edgeLevel);
					t.getEdges()[0].setAncestor(this.getEdges()[i]);
					t.getEdges()[2].setAncestor(this.getEdges()[(i + 2) % 3]);
					triangles.add(t);

					int j;
					d1 = VectorUnit.angle(newNodes[(i + 2) % 3].getArray(), get((i + 2) % 3).getArray());
					d2 = VectorUnit.angle(newNodes[(i + 1) % 3].getArray(), get((i + 1) % 3).getArray());

					if (d1 > d2) {
						j = (i + 1) % 3;
						t = new Triangle(this, get((i + 1) % 3), get((i + 2) % 3), newNodes[j], tessLevel + 1,
								edgeLevel);
						t.getEdges()[0].setAncestor(this.getEdges()[(i + 1) % 3]);
						t.getEdges()[1].setAncestor(this.getEdges()[(i + 2) % 3]);
						triangles.add(t);

						t = new Triangle(this, get(j), newNodes[(i + 1) % 3], newNodes[(i + 2) % 3], tessLevel + 1,
								edgeLevel);
						t.getEdges()[2].setAncestor(this.getEdges()[i]);
						triangles.add(t);
					} else {
						j = (i + 2) % 3;
						t = new Triangle(this, get((i + 1) % 3), get((i + 2) % 3), newNodes[j], tessLevel + 1,
								edgeLevel);
						t.getEdges()[0].setAncestor(this.getEdges()[(i + 1) % 3]);
						t.getEdges()[2].setAncestor(this.getEdges()[i]);
						// t.setTessLevel(tessLevel+1);
						triangles.add(t);

						t = new Triangle(this, get(j), newNodes[(i + 1) % 3], newNodes[(i + 2) % 3], tessLevel + 1,
								edgeLevel);
						t.getEdges()[2].setAncestor(this.getEdges()[(i + 2) % 3]);
						triangles.add(t);
					}

					this.descendant = t;
					return triangles;
				}
		} else if (n == 3) {
			// All of this triangle's neighbors set new vertices on this
			// triangle's edges. Have to subdivide this triangle into 4 new
			// triangles.
			nDescendants = 4;
			// make 4 new triangles, set all their properties, and add them to
			// the collection.
			t = new Triangle(this, getEdges()[0].getVertex(), getEdges()[1].getVertex(), getEdges()[2].getVertex(),
					tessLevel + 1, edgeLevel + 1);
			this.descendant = t;
			// t.setTessLevel(tessLevel+1);
			triangles.add(t);

			for (int i = 0; i < 3; ++i) {
				t = new Triangle(this, get(i), getEdges()[(i + 2) % 3].getVertex(), getEdges()[(i + 1) % 3].getVertex(),
						tessLevel + 1, edgeLevel + 1);
				t.getEdges()[0].setAncestor(this.getEdges()[i]);
				t.getEdges()[2].setAncestor(this.getEdges()[(i + 2) % 3]);
				triangles.add(t);
			}
			return triangles;
		}
		// This "cant't" happen.
		throw new GeoTessException("Not good.");
	}

	/**
	 * Retrieve the index of this triangle. Index is local variable that is never
	 * modified in the Triangle class. Available for use by caller.
	 * 
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Set the value of index.
	 * 
	 * @param index
	 */
	public Triangle setIndex(int index) {
		this.index = index;
		return this;
	}

	public Triangle incIndex() {
		++index;
		return this;
	}

	public Triangle decIndex() {
		--index;
		return this;
	}

	/**
	 * Return true if this triangle has a descendant.
	 * 
	 * @return true if this triangle has a descendant.
	 */
	public boolean hasDescendant() {
		return descendant != null;
	}

	/**
	 * Return true if this triangle is marked.
	 * 
	 * @return true if this triangle is marked.
	 */
	public boolean isMarked() {
		return marked > 0;
	}

	/**
	 * Increment by one the number of times that mark() has been called.
	 * 
	 * @return a reference to this.
	 */
	public Triangle mark() {
		++marked;
		return this;
	}

	/**
	 * Set marked to 0 (false) or 1 (true)
	 * 
	 * @return a reference to this.
	 */
	public Triangle mark(boolean mark) {
		marked = mark ? 1 : 0;
		return this;
	}

	/**
	 * Unmark this triangle.
	 * 
	 * @return a reference to this.
	 */
	public Triangle unmark() {
		marked = 0;
		return this;
	}

	/**
	 * Retrieve the number of times that mark() has been called since the last time
	 * it was unmarked().
	 * 
	 * @return the number of times that mark() has been called since the last time
	 *         it was unmarked().
	 */
	public int getMarked() {
		return marked;
	}

	/**
	 * If this element does not have a descendant and any one of its edges has been
	 * subdivided more than once, return true. Otherwise false.
	 * 
	 * @return boolean
	 */
	public boolean needsDivision() {
		if (!hasDescendant())
			for (Edge edge : getEdges())
				if (edge.getNDivisions() > 1)
					return true;
		return false;
	}

	/**
	 * Given some other triangle t, see if t is neighbor of this triangle. If so,
	 * return the index of t in this triangle's array of neighbors (an int in range
	 * 0..2). If not, return -1;
	 * 
	 * @param t
	 * @return
	 */
	public int getNeighborIndex(Triangle t) {
		for (int i = 0; i < 3; ++i)
			if (neighbors[i] == t)
				return i;
		return -1;
	}

	/**
	 * Set the neighbor of this triangle that resides on the other side of edge
	 * 'side'. Also update the edge.
	 * 
	 * @param side     int
	 * @param neighbor Element
	 */
	protected Edge setNeighbor(int side, Triangle neighbor) {
		neighbors[side] = neighbor;

		// search through neighbor's edges to see if this
		// Triangle is already one of it's neighbors.
		for (Edge neighborsEdge : neighbor.getEdges())
			if (neighborsEdge.getNeighbor(neighbor) == this) {
				// neighbor has an edge where this Triangle
				// is on the other side. Set this Triangle's
				// edge to reference to the same Edge.
				getEdges()[side] = neighborsEdge;
				return neighborsEdge;
			}

		getEdges()[side].setSides(this, neighbor);

		return getEdges()[side];

	}

	/**
	 * Establish neighbor relations between this Triangle and Triangle 'neighbor'
	 * when it is already known which side 'neighbor' is on, and it is also know
	 * which side of 'neighbor' this is on.
	 * 
	 * @param side
	 * @param neighbor
	 * @param neighborSide
	 * @return
	 */
	protected Edge setNeighbor(int side, Triangle neighbor, int neighborSide) {
		neighbors[side] = neighbor;
		neighbor.neighbors[neighborSide] = this;

		edges[side].setSides(this, neighbor);
		neighbor.edges[neighborSide] = edges[side];
		return edges[side];

	}

	/**
	 * Retrieve the 4 neighbors of this triangle.
	 * 
	 * @return
	 */
	public Triangle[] getNeighbors() {
		return neighbors;
	}

	/**
	 * Return a reference to the triangle that is the i'th neighbor of this
	 * triangle. The i'th neighbor is the triangle that resides on the other side of
	 * the edge of this triangle that does not contain node i.
	 * 
	 * @param i
	 * @return
	 */
	public Triangle getNeighbor(int i) {
		return neighbors[i];
	}

	/**
	 * @return the tessLevel
	 */
	public int getTessLevel() {
		return tessLevel;
	}

	/**
	 * Retrieve a reference to the triangle that is the descendant of this triangle.
	 * If this triangle does not have a descendant, return null. The descendant of
	 * this triangle resides on the next higher tessellation level from this
	 * trinagle.
	 * 
	 * @return
	 */
	protected Triangle getDescendant() {
		return descendant;
	}

	/**
	 * Set the descendant of this triangle. The descendant of this triangle resides
	 * on the next higher tessellation level from this triangle.
	 */
	protected void setDescendant(Triangle descendant) {
		this.descendant = descendant;
	}

//	/**
//	 * Retrieve a reference to the triangle that is the ancestor of this
//	 * triangle. The triangles on the first level of the tessellation do not
//	 * have ancestors and hence return null. .
//	 * 
//	 * @return
//	 */
//	protected Triangle getAncestor()
//	{
//		return ancestor;
//	}
//
//	public void setAncestor(Triangle ancestor)
//	{
//		this.ancestor = ancestor;
//	}
//
	/**
	 * Retrieve the scalar triple product of (n[i] x n[j]) . u
	 * 
	 * @param i
	 * @param u
	 * @return
	 */
	protected double scalarTripleProduct(int i, int j, double[] u) {
		return VectorUnit.scalarTripleProduct(get(i).getArray(), get(j).getArray(), u);
	}

	/**
	 * Retrieve a reference to the array of Edges of this triangle.
	 * 
	 * @return the edges
	 */
	public Edge[] getEdges() {
		return edges;
	}

	/**
	 * Retrieve a reference to one of the Edges of this triangle.
	 * 
	 * @return the edge
	 */
	public Edge getEdge(int i) {
		return edges[i];
	}

	/**
	 * Retrieve the Edge object shared by this Element and Element otherSide. If
	 * this element does not share an Edge with otherSide, return null.
	 * 
	 * @param otherSide
	 * @return
	 */
	public Edge getEdge(Triangle otherSide) {
		for (Edge edge : edges)
			if (edge.getNeighbor(this) == otherSide)
				return edge;
		return null;
	}

	/**
	 * Retrieve the number of triangles into which this triangle was subdivided, or
	 * zero if never subdivided.
	 * 
	 * @return the nDescendants
	 */
	public int getNDescendants() {
		return nDescendants;
	}

	public int getNSiblings() {
		return ancestor == null ? 0 : ancestor.getNDescendants();
	}

	public double[][] getCorners() {
		return new double[][] { get(0).getArray(), get(1).getArray(), get(2).getArray() };
	}

	public double[] getCenter() {
		double[] center = new double[] { get(0).getArray()[0] + get(1).getArray()[0] + get(2).getArray()[0],
				get(0).getArray()[1] + get(1).getArray()[1] + get(2).getArray()[1],
				get(0).getArray()[2] + get(1).getArray()[2] + get(2).getArray()[2] };
		VectorUnit.normalize(center);
		return center;
	}

	public void getCenter(double[] center) {
		center[0] = get(0).getArray()[0] + get(1).getArray()[0] + get(2).getArray()[0];
		center[1] = get(0).getArray()[1] + get(1).getArray()[1] + get(2).getArray()[1];
		center[2] = get(0).getArray()[2] + get(1).getArray()[2] + get(2).getArray()[2];
		VectorUnit.normalize(center);
	}

	/**
	 * Two Triangles are equal if they reside on the same tessellation level and all
	 * 3 of their Vertexes are equal.
	 */
	public boolean equals(Object other) {
		return other.getClass().equals(Triangle.class) && this.tessLevel == ((Triangle) other).tessLevel
				&& this.get(0).equals(((Triangle) other).get(0)) && this.get(1).equals(((Triangle) other).get(1))
				&& this.get(2).equals(((Triangle) other).get(2));
	}

	/**
	 * @return the circumCenter
	 */
	public double[] getCircumCenter() {
		if (circumCenter == null)
			circumCenter = VectorUnit.circumCenter(get(0).getArray(), get(1).getArray(), get(2).getArray());
		return circumCenter;
	}

	public int getEdgeLevel() {
		return edgeLevel;
	}
	
	public Polygon getPolygon() throws Exception
	{
		if (polygon == null)
		{
			polygon = new PolygonPoints(get(0).getArray(), get(1).getArray(), get(2).getArray());
			polygon.setReferencePoint(getCenter(), true);
		}
		return polygon;
	}

}
