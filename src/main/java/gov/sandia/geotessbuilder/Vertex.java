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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * Vertex represents a single point on the surface of a unit sphere, represented
 * by a unit vector. Two nodes are .equal() if their dot product is very close
 * to one.
 * 
 * Vertex maintains a list of Triangles of which it is a member. This list is
 * complicated by the fact that a Vertex can belong to triangles in many
 * different tessellations and on different tessellation levels. It is the
 * responsibility of the caller to populate the list of triangles with the
 * appropriate set for the task at hand and them clear the set when the task is
 * complete.
 * 
 * @author sballar
 * 
 */
public class Vertex extends ArrayListDouble {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1615554716536338690L;

	/**
	 * Index is never modified in this class. Getter and setter are provided.
	 */
	private int index;

	private int marked;

	/**
	 * Set of Triangles of which this Node is a corner. This list is complicated by
	 * the fact that a Vertex can belong to triangles in many different
	 * tessellations and on different tessellation levels. It is the responsibility
	 * of the caller to populate the list of triangles with the appropriate set for
	 * the task at hand and them clear the set when the task is complete.
	 */
	protected ArrayList<HashSet<Triangle>> triangles;

	/**
	 * Default constructor initializes position to north pole.
	 */
	public Vertex() {
		this(new double[] { 0., 0., 1. });
	}

	/**
	 * Parameterized constructor sets this Vertex's position to a copy of the
	 * supplied unit vector.
	 * 
	 * @param unitVector
	 */
	public Vertex(double[] unitVector) {
		super(unitVector);
		triangles = new ArrayList<HashSet<Triangle>>();
	}

	/**
	 * Parameterized constructor sets this Vertex's position to the vector sum of
	 * the supplied vectors, normalized to unit length.
	 * 
	 * @param v
	 */
	public Vertex(Vertex... v) {
		super(3);
		double[] u = getArray();
		for (Vertex n : v) {
			u[0] += n.get(0);
			u[1] += n.get(1);
			u[2] += n.get(2);
		}
		VectorUnit.normalize(u);
		triangles = new ArrayList<HashSet<Triangle>>();
	}

	/**
	 * This vertex and other vertex are equal if their dot product is &gt;
	 * Math.cos(1e-7)
	 * 
	 * @param other
	 * @return
	 */
	@Override
	public boolean equals(Object other) {
		return VectorUnit.dot(((Vertex) other).getArray(), this.getArray()) > Math.cos(1e-7);
	}

	/**
	 * Retrieve the dot product of this Vertex and other Vertex.
	 * 
	 * @param other
	 * @return
	 */
	public double dot(Vertex other) {
		return VectorUnit.dot(getArray(), other.getArray());
	}

	/**
	 * Triangles is the set of triangles on a particular tessellation level that
	 * have this Vertex as one of their corners. This method clears the set.
	 */
	protected void clearTriangles(int tessLevel) {
		while (triangles.size() - 1 < tessLevel)
			triangles.add(new HashSet<Triangle>(6));
		triangles.get(tessLevel).clear();
	}

	/**
	 * Triangles is the set of triangles on a particular tessellation level that
	 * have this Vertex as one of their corners.
	 * 
	 * @param t Triangle to be added to the set.
	 * @return
	 */
	protected Vertex addTriangle(int tessLevel, Triangle t) {
		while (triangles.size() - 1 < tessLevel)
			triangles.add(new HashSet<Triangle>(6));
		triangles.get(tessLevel).add(t);
		return this;
	}

	/**
	 * Retrieve the set of triangles on a particular tessellation level that have
	 * this Vertex as one of their corners.
	 * 
	 * @return
	 */
	protected Set<Triangle> getTriangles(int tessLevel) {
		while (triangles.size() - 1 < tessLevel)
			triangles.add(new HashSet<Triangle>(6));
		return triangles.get(tessLevel);
	}

	/**
	 * Mark all the triangles in the set of triangles at a particular tessellation
	 * level of which this Vertex is a member.
	 * 
	 * @return a reference to this Vertex
	 */
	protected Vertex markTriangles(int tessLevel) {
		while (triangles.size() - 1 < tessLevel)
			triangles.add(new HashSet<Triangle>(6));
		for (Triangle t : triangles.get(tessLevel))
			t.mark();
		return this;
	}

	/**
	 * Unmark all the triangles in the set of triangles at a particular tessellation
	 * level of which this Vertex is a member.
	 * 
	 * @return a reference to this Vertex
	 */
	protected Vertex unmarkTriangles(int tessLevel) {
		while (triangles.size() - 1 < tessLevel)
			triangles.add(new HashSet<Triangle>(6));
		for (Triangle t : triangles.get(tessLevel))
			t.unmark();
		return this;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 * @return a reference to this
	 */
	public Vertex setIndex(int index) {
		this.index = index;
		return this;
	}

	/**
	 * True if current mark is &gt; 0
	 * 
	 * @return true if current mark is &gt; 0
	 */
	public boolean isMarked() {
		return marked > 0;
	}

	/**
	 * Set value of mark
	 * 
	 * @param mark any integer
	 * @return reference to this
	 */
	public Vertex mark(int mark) {
		marked = mark;
		return this;
	}

	/**
	 * Increment current value of mark.
	 * 
	 * @return reference to this
	 */
	public Vertex mark() {
		++marked;
		return this;
	}

	/**
	 * Retrieve current value of mark
	 * 
	 * @return current value of mark
	 */
	public int getMark() {
		return marked;
	}

	/**
	 * Set mark equal to zero
	 * 
	 * @return reference to this
	 */
	public Vertex unmark() {
		marked = 0;
		return this;
	}

	public String toString() {
		return String.format("index=%7d, lat, lon = %s, mark=%d", index, VectorGeo.getLatLonString(getArray()), marked);
	}

	/**
	 * Set marked = 0 if false or 1 if true;
	 * 
	 * @param mark
	 */
	public void mark(boolean mark) {
		marked = mark ? 1 : 0;
	}

}
