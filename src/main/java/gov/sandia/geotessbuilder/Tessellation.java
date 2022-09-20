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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.gmp.util.numerical.polygon.Polygon2D;

/**
 * Tessellation manages a single instance of multi-level tessellation of a unit
 * sphere. Strictly speaking, a tessellation is a single 2D surface that is
 * completely filled with geometric shapes without gaps or overlaps. Using that
 * definition, each level of our multi-level tessellation is a tessellation but
 * we refer to those as 'levels' and reserve the term 'Tessellation' for a
 * multilevel tessellation.
 * 
 * <p>
 * Tessellation is implemented as a 2D array of Triangles where the outer array
 * index spans the levels of the multi-level tessellation and the inner index
 * spans the triangles that reside on each level.
 * 
 * @author sballar
 * 
 */
public class Tessellation extends ArrayList<ArrayList<Triangle>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5910983875461712665L;

	/**
	 * The index of the last uniform tessellation level.
	 */
	protected int baseTessLevel;

	protected ArrayList<PolygonGB> polygons;

	protected InitialSolid initialSolid;

	/**
	 * Map from tessellation level to a list of points.
	 */
	private HashMap<Integer, ArrayList<double[]>> points;

	private int maxProcessors = Runtime.getRuntime().availableProcessors();

	public Tessellation(InitialSolid initialSolid, int baseTessLevel) {
		this(initialSolid, baseTessLevel, 1);
	}

	public Tessellation(InitialSolid initialSolid, int baseTessLevel, int maxProcessors) {
		this.baseTessLevel = baseTessLevel;
		this.maxProcessors = maxProcessors;
		this.initialSolid = initialSolid;

		// polygons = new HashMap<Integer, ArrayList<PolygonGB>>();
		polygons = new ArrayList<PolygonGB>();
		points = new HashMap<Integer, ArrayList<double[]>>();
	}

	/**
	 * Return true if the specified triangle should be subdivided into smaller
	 * elements.
	 * 
	 * @param triangle Triangle
	 * @return boolean true if the triangle should be subdivided.
	 * @throws GeoTessException
	 */
	protected boolean isDivisible(Triangle triangle) throws Exception {
		// all triangles on tessellation levels less than baseTessLevel should be
		// divided.
		if (triangle.getTessLevel() < baseTessLevel)
			return true;

		// if any vertices of triangle were marked in method
		// populateNodes() then triangle needs to be subdivided.
		if (triangle.get(0).isMarked() || triangle.get(1).isMarked() || triangle.get(2).isMarked())
			return true;
		
		for (PolygonGB polygongb : polygons)
			if (triangle.getTessLevel() < polygongb.getTessLevel()
					&& triangle.getPolygon().overlaps(polygongb.polygon))
				return true;
		
//		for (Annulus annulus : annuli)
//			if (triangle.getTessLevel() < annulus.getTessLevel())
//				if (annulus.overlap(triangle))
//					return true;
//
//
//		// if triangle contains any points that define any of the polygons
//		// then subdivide this triangle
//		for (PolygonGB polygon : polygons)
//			if (triangle.getTessLevel() < polygon.getTessLevel())
//				for (double[] point : polygon.getPoints(false))
//					if (triangle.contains(point))
//						return true;
//		
		// tessLevelPoints is a map from tessLevel to collection of double[].
		// - visit every represented tessLevel.
		// - if triangle.tessLevel is less than the current tessLevel:
		// - check every point associated with the current tessLevel
		// and return true if triangle.contains(point).
		for (Integer tessLevelPoints : points.keySet())
			if (triangle.getTessLevel() < tessLevelPoints)
				for (double[] point : points.get(tessLevelPoints))
					if (triangle.contains(point))
						return true;

		return false;
	}

	/**
	 * This method is called after each level of a multi-level Tessellation is
	 * assembled. If derived classes wish to populate unpopulated Nodes before
	 * construction of the next level of the Tessellation, they can override this
	 * method and do that. Derived classes would want to do this if their
	 * isDivisble() method requires that Data be assigned to the Nodes before
	 * deciding if an Triangle is to be divided.
	 * 
	 * @param tessLevel the current tessellation level.
	 * @throws GeoTessException
	 */
	public void populateNodes(int tessLevel) throws GeoTessException

	{
		if (tessLevel >= baseTessLevel) {
			HashSet<Vertex> vertices = new HashSet<Vertex>(get(tessLevel).size());
			for (Triangle t : get(tessLevel))
				for (Vertex v : t)
					vertices.add(v);

			for (PolygonGB p : polygons)
				if (p.getTessLevel() > tessLevel)
					p.markContainedVertices(vertices, maxProcessors);
		}
	}

	public Tessellation addPoints(Collection<double[]> points, int tessLevelPoints) {
		ArrayList<double[]> pointList = this.points.get(tessLevelPoints);
		if (pointList == null) {
			pointList = new ArrayList<double[]>();
			this.points.put(tessLevelPoints, pointList);
		}
		pointList.addAll(points);
		return this;
	}

	public Tessellation addPoint(double[] point, int tessLevel) {
		ArrayList<double[]> pointList = this.points.get(tessLevel);
		if (pointList == null) {
			pointList = new ArrayList<double[]>();
			this.points.put(tessLevel, pointList);
		}
		pointList.add(point);
		return this;
	}

	public Tessellation addPolygon(Polygon2D polygon) {
		polygons.add(new PolygonGB(polygon));
		return this;
	}

	public Tessellation addPolygons(Collection<Polygon2D> polygons) {
		for (Polygon2D p : polygons)
			addPolygon(p);
		return this;
	}
	
	/**
	 * Extract a Tessellation object from a GeoTessGrid object.
	 * 
	 * @param grid
	 * @param tessid
	 * @throws GeoTessException
	 */
	public Tessellation(GeoTessGrid grid, int tessid) throws GeoTessException {
		super(grid.getNLevels(tessid));

		grid.testGrid();

		// make Vertex object for every element of grid.vertices
		ArrayList<Vertex> vertices = new ArrayList<Vertex>(grid.getNVertices());
		for (int i = 0; i < grid.getNVertices(); ++i)
			vertices.add(new Vertex(grid.getVertex(i)));

		// loop over every level of the specified tessellation
		for (int lid = 0; lid < grid.getNLevels(tessid); ++lid) {
			// create an array of Triangle objects with capacity to hold all
			// the triangles on this level and add the level to this
			// Tessellation
			ArrayList<Triangle> level = new ArrayList<Triangle>(grid.getNTriangles(tessid, lid));
			add(level);

			// build all the triangles on this level
			for (int tid = 0; tid < grid.getNTriangles(tessid, lid); ++tid) {
				Triangle t = new Triangle(null, vertices.get(grid.getVertexIndex(tessid, lid, tid, 0)),
						vertices.get(grid.getVertexIndex(tessid, lid, tid, 1)),
						vertices.get(grid.getVertexIndex(tessid, lid, tid, 2)), lid, lid);
				level.add(t);
			}
		}

		setConnectivity();
	}

	/**
	 * Builds the tessellation.
	 * 
	 * @throws GeoTessException
	 */
	public Tessellation build() throws Exception {
		ArrayList<Vertex> tnodes = new ArrayList<Vertex>();

		// get Nodes at the vertices of the initialSolid
		for (double[] vertex : initialSolid.getVertices())
			tnodes.add(new Vertex(vertex));

		// construct a Triangle object for each face of the initialSolid,
		// identifying the vertexes that define each face, in clockwise order as
		// viewed from outside the initialSolid. Specify that the triangles
		// reside on tessellation level 0.
		add(new ArrayList<Triangle>(initialSolid.getNFaces()));
		for (int j = 0; j < initialSolid.getNFaces(); ++j) {
			int[] i = initialSolid.getFace(j);
			get(0).add(new Triangle(null, tnodes.get(i[0]), tnodes.get(i[1]), tnodes.get(i[2]), 0, 0));
		}

		// define triangle neighbor relations for all the elements on
		// tessellation level 0.
		establishNeighbors(0);

		ArrayList<Triangle> trianglesNextLevel;

		boolean more;
		int currentLevel = 0;

			do {
				// populate nodes on the top level added so far.
				populateNodes(currentLevel);

				trianglesNextLevel = new ArrayList<Triangle>(get(currentLevel).size() * 4);

				// visit every triangle and divide it if required.
				for (Triangle triangle : get(currentLevel))
					if (isDivisible(triangle))
						triangle.divide(trianglesNextLevel);

				if (trianglesNextLevel.size() > 0) {
					add(trianglesNextLevel);

					establishNeighbors(currentLevel + 1);

					++currentLevel;

					more = true;
				} else
					more = false;

				if (polygons.size() > 0)
					for (Triangle t : get(currentLevel))
						for (Vertex v : t)
							v.unmark();

			} while (more);


		trianglesNextLevel = new ArrayList<Triangle>(get(currentLevel).size());
		do {
			more = false;
			for (int tessLevel = 0; tessLevel < size() - 1; ++tessLevel) {
				trianglesNextLevel.clear();
				// visit elements again, looking for ones that have had more
				// than one grid node on an edge then divide.
				for (Triangle triangle : get(tessLevel))
					if (triangle.needsDivision())
						triangle.divide(trianglesNextLevel);

				if (trianglesNextLevel.size() > 0) {
					get(tessLevel + 1).addAll(trianglesNextLevel);

					establishNeighbors(tessLevel + 1);

					populateNodes(tessLevel);

					more = true;
				}

			}
		} while (more);

		// visit elements on all but the highest tessellation level searching
		// for ones that do not have descendants. If no descendant, get
		// transition triangles.
		for (int tessLevel = 0; tessLevel < size() - 1; ++tessLevel) {
			trianglesNextLevel.clear();
			for (Triangle triangle : get(tessLevel))
				if (!triangle.hasDescendant())
					trianglesNextLevel.addAll((triangle.getTransitionTriangles()));

			if (trianglesNextLevel.size() > 0) {
				get(tessLevel + 1).addAll(trianglesNextLevel);
				establishNeighbors(tessLevel + 1);
			}
		}

		setConnectivity();

		// delaunay(false);

		return this;
	}

	/**
	 * Perform the following operations:
	 * <ul>
	 * <li>reindex all the vertices and triangles with indices starting from 0.
	 * <li>ensure that all triangles are unmarked. throws an exception if this is
	 * not the case
	 * <li>establish neighbors on all tessellation levels
	 * <li>set descendant for all triangles on all levels except the last level
	 * <li>count the number of descendants each triangle has
	 * <li>test the tessellation to ensure it is valid.
	 * </ul>
	 * 
	 * @throws GeoTessException
	 */
	private void setConnectivity() throws GeoTessException {
		int index = 0;
		// first, set the indeces of the triangles to new values
		// and the indeces of the vertices to -1
		for (ArrayList<Triangle> list : this)
			for (Triangle t : list) {
				t.setIndex(index++);
				for (Vertex v : t)
					v.setIndex(-1);
			}
		index = 0;
		// now set the indeces of the vertices to new values.
		for (ArrayList<Triangle> list : this)
			for (Triangle t : list)
				for (Vertex v : t)
					if (v.getIndex() < 0)
						v.setIndex(index++);

		/**
		 * ensure that no triangles are marked. It should be true that every algorithm
		 * that marked a triangle also unmarked it. This just checks to make sure that
		 * was true.
		 */
		for (ArrayList<Triangle> list : this)
			for (Triangle t : list)
				if (t.isMarked())
					throw new GeoTessException("Triangle is marked\n" + t.toString());

		for (int lid = 0; lid < size(); ++lid)
			establishNeighbors(lid);

		for (int lid = 0; lid < size() - 1; ++lid) {
			Triangle t0 = get(lid + 1).get(0);
			for (Triangle t : get(lid)) {
				t0 = findTriangle(t0, lid + 1, t.getCenter());
				t.setDescendant(t0);
				t.nDescendants = 1;
				for (Triangle neighbor : t0.getNeighbors())
					if (t.contains(neighbor.getCenter()))
						++t.nDescendants;
			}
		}

		testTessellation();
	}

	/**
	 *
	 * @param tessLevel
	 * @throws GeoTessException
	 */
	public void establishNeighbors(int tessLevel) throws GeoTessException {
		// clear the vertex triangle membership set
		for (Triangle t : get(tessLevel))
			for (Vertex v : t)
				v.clearTriangles(tessLevel);

		// populate the vertex triangle membership sets
		for (Triangle t : get(tessLevel))
			for (Vertex v : t)
				v.addTriangle(tessLevel, t);

		// For every element find the neighbors of the element at the specified
		// level.
		for (Triangle triangle : get(tessLevel))
			for (int i = 0; i < 3; ++i) {
				// mark all the triangles that are at current level of the
				// tessellation and which have node i-1 as a corner.
				triangle.get((i + 2) % 3).markTriangles(tessLevel);

				// find the next node in this triangle. Visit all of
				// the triangles of which it is a corner. There will
				// be at most one triangle which is not triangle triangle and
				// which is currently marked. That triangle is a
				// neighbor of triangle.
				for (Triangle t : triangle.get((i + 1) % 3).getTriangles(tessLevel))
					if (t.isMarked() && t != triangle) {
						triangle.setNeighbor(i, t);
						break;
					}

				// unmark node i membership.
				triangle.get((i + 2) % 3).unmarkTriangles(tessLevel);
			}
	}

	/**
	 * Unmark all vertices and triangles.
	 */
	public void unMark() {
		for (ArrayList<Triangle> level : this)
			for (Triangle t : level) {
				t.unmark();
				for (Vertex v : t)
					v.unmark();
			}
	}

	/**
	 * Loop over all the levels, triangles and vertices and build a list of the
	 * unique Vertex objects in the tessellation. The Triangle and Vertex objects
	 * have their indexes set during this process so vertex.get(i).getIndex() will
	 * == i.
	 * 
	 * @return
	 */
	public ArrayList<Vertex> getVertices() {
		for (ArrayList<Triangle> level : this)
			for (Triangle triangle : level)
				for (Vertex vertex : triangle)
					vertex.setIndex(-1);

		int index = 0;
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		for (ArrayList<Triangle> level : this)
			for (Triangle triangle : level) {
				triangle.setIndex(index++);
				for (Vertex vertex : triangle)
					if (vertex.getIndex() < 0)
						vertices.add(vertex.setIndex(vertices.size()));
			}
		return vertices;
	}

	/**
	 * Perform walking triangle search to find the index of the triangle that
	 * contains position defined by vector and which has no descendant.
	 * 
	 * @param t the Triangle from which to start the search.
	 * @param u the unit vector representing the position for which to search.
	 * @return a reference to the triangle containing the specified position.
	 */
	public Triangle findTriangle(Triangle t, double[] u) {
		while (true) {
			if (t.scalarTripleProduct(2, 1, u) > -1e-15) {
				if (t.scalarTripleProduct(0, 2, u) > -1e-15) {
					if (t.scalarTripleProduct(1, 0, u) > -1e-15) {
						if (t.getDescendant() == null)
							return t;
						else
							t = t.getDescendant();
					} else
						t = t.getNeighbor(2);
				} else
					t = t.getNeighbor(1);
			} else
				t = t.getNeighbor(0);
		}
	}

	/**
	 * Perform walking triangle search to find the index of the triangle that
	 * contains position defined by vector and which resides on the specified
	 * tessellation level.
	 * 
	 * @param t         the Triangle from which to start the search.
	 * @param tessLevel search no higher than this level.
	 * @param u         the unit vector representing the position for which to
	 *                  search.
	 * @return a reference to the triangle containing the specified position.
	 */
	public Triangle findTriangle(Triangle t, int tessLevel, double[] u) {
		while (true) {
			if (t.scalarTripleProduct(2, 1, u) > -1e-15) {
				if (t.scalarTripleProduct(0, 2, u) > -1e-15) {
					if (t.scalarTripleProduct(1, 0, u) > -1e-15) {
						if (t.getDescendant() == null || t.getTessLevel() == tessLevel)
							return t;
						else
							t = t.getDescendant();
					} else
						t = t.getNeighbor(2);
				} else
					t = t.getNeighbor(1);
			} else
				t = t.getNeighbor(0);
		}
	}

	/**
	 * Perform walking triangle search to find the index of the triangle that
	 * contains position defined by vector and which has no descendant.
	 * 
	 * @param t         the Triangle from which to start the search.
	 * @param tessLevel search no higher than this level.
	 * @param u         the unit vector representing the position for which to
	 *                  search.
	 * @param c         a 3-element array that will be filled with the interpolation
	 *                  coefficients.
	 * @return a reference to the triangle containing the specified position.
	 */
	public Triangle findTriangle(Triangle t, int tessLevel, double[] u, double[] c) {
		while (true) {
			c[0] = t.scalarTripleProduct(2, 1, u);
			if (c[0] > -1e-15) {
				c[1] = t.scalarTripleProduct(0, 2, u);
				if (c[1] > -1e-15) {
					c[2] = t.scalarTripleProduct(1, 0, u);
					if (c[2] > -1e-15) {
						if (t.getDescendant() == null || t.getTessLevel() == tessLevel) {
							// the correct triangle has been found.
							// Normalize the x.coefficients
							// such that they sum to one.
							double sum = c[0] + c[1] + c[2];
							c[0] /= sum;
							c[1] /= sum;
							c[2] /= sum;
							return t;
						} else
							t = t.getDescendant();
					} else
						t = t.getNeighbor(2);
				} else
					t = t.getNeighbor(1);
			} else
				t = t.getNeighbor(0);
		}
	}

	/**
	 * Search, starting from Triangle t, for a vertex that coincides with supplied
	 * unit vector u. Returns null if no such Vertex was found.
	 * 
	 * @param t
	 * @param u
	 * @return
	 */
	public Vertex findVertex(Triangle t, int tessLevel, double[] u, double[] c) {
		t = findTriangle(t, tessLevel, u, c);

		if (c[0] > .999999999)
			return t.get(0);
		if (c[1] > .999999999)
			return t.get(1);
		if (c[2] > .999999999)
			return t.get(2);
		return null;
	}

	/**
	 * Search for a vertex that coincides with supplied unit vector u. Returns null
	 * if no such Vertex was found.
	 * 
	 * @param u unit vector of vertex to search for
	 * @param c interpolation coefficients (never used).
	 * @return Vertex or null if not found
	 */
	public Vertex findVertex(double[] u, double[] c) {
		Triangle t = findTriangle(get(0).get(0), size() - 1, u, c);

		if (c[0] > .999999999)
			return t.get(0);
		if (c[1] > .999999999)
			return t.get(1);
		if (c[2] > .999999999)
			return t.get(2);
		return null;
	}

	/**
	 * Tests the integrity of the grid. Visits every triangle T, and (1) checks to
	 * ensure that every neighbor of T includes T in its list of neighbors, and (2)
	 * checks that every neighbor of T shares exactly two nodes with T.
	 * 
	 * @throws GeoTessException if anything is amiss.
	 */
	public void testTessellation() throws GeoTessException {
		for (int level = 0; level < size(); ++level) {
			for (Triangle triangle : get(level)) {
				for (int i = 0; i < 3; i++) {
					Triangle n = triangle.getNeighbor(i);
					if (n == null) {
						StringBuffer buf = new StringBuffer();
						buf.append(String.format("%n%s%n", toString()));
						buf.append(String.format("%nTriangle %1d (%d), tessLevel %d, neighbor %1d is null.%n",
								triangle.getIndex(), i, triangle.getTessLevel(), i));
						throw new GeoTessException(buf.toString());
					}

					if (triangle.getEdge(n) == null)
						throw new GeoTessException(String.format(
								"%nTriangle %1d, tessLevel %d, is a neighbor of triangle %1d, they do not share an Edge.%n",
								n.getIndex(), n.getTessLevel(), triangle.getIndex()));

					if (n.getEdge(triangle) == null)
						throw new GeoTessException(String.format(
								"%nTriangle %1d, tessLevel %d, is a neighbor of triangle %1d, they do not share an Edge.%n",
								n.getIndex(), n.getTessLevel(), triangle.getIndex()));

					if (triangle.getEdge(n) != n.getEdge(triangle))
						throw new GeoTessException(String.format("%nEdges are not equal."));

					int j = triangle.getNeighborIndex(n);
					if (j < 0)
						throw new GeoTessException(String.format(
								"%nTriangle %1d, tessLevel %d, is a neighbor of triangle %1d, but reverse is not true.%n",
								n.getIndex(), n.getTessLevel(), triangle.getIndex()));

					int k = n.getNeighborIndex(triangle);
					if (k < 0)
						throw new GeoTessException(String.format(
								"%nTriangle %1d, tessLevel %d, is a neighbor of triangle %1d, but reverse is not true.%n",
								triangle.getIndex(), triangle.getTessLevel(), n.getIndex()));

					if (!triangle.get((j + 1) % 3).equals(n.get((k + 2) % 3))
							|| !triangle.get((j + 2) % 3).equals(n.get((k + 1) % 3))) {
						StringBuffer buf = new StringBuffer();
						buf.append(String.format("%n%s%n", toString()));
						buf.append(String.format(
								"%nTriangle %1d and triangle %1d do not have exactly two nodes in common.%n",
								triangle.getIndex(), n.getIndex()));
						buf.append(String.format("%n%s%n%s%n", triangle, n));
						throw new GeoTessException(buf.toString());
					}

				}

				// if (level > 0)
				// {
				// if (triangle.getAncestor() == null)
				// throw new GeoTessException(
				// String.format(
				// "%nTriangle %d on tessLevel %d has no ancestor%n",
				// triangle.getIndex(), level));
				//
				// if (triangle.getTessLevel() != triangle.getAncestor().getTessLevel() + 1)
				// throw new GeoTessException(
				// String.format(
				// "%nTriangle %d is on tessLevel %d and its ancestor is on tessLevel %d%n",
				// triangle.getIndex(), level, triangle
				// .getAncestor().getTessLevel()));
				// }

				if (level < size() - 1) {
					Triangle descendant = triangle.getDescendant();
					if (descendant == null)
						throw new GeoTessException(String.format(
								"%nTriangle %d resides on tessellation level %d of %d but its descendant is null",
								triangle.getIndex(), level, size()));

					if (descendant.getTessLevel() != triangle.getTessLevel() + 1)
						throw new GeoTessException(String.format(
								"%nTriangle %d has tessLevel %d but it's descendant, element %d, has tessLevel %d%n",
								triangle.getIndex(), triangle.getTessLevel(), descendant.getIndex(),
								triangle.getDescendant().getTessLevel()));

					// if (descendant.getAncestor() != triangle)
					// throw new GeoTessException(
					// String.format(
					// "%nTriangle %d has descendant element %d, but descendant's ancestor is
					// element %d%n",
					// triangle.getIndex(), descendant.getIndex(),
					// descendant.getAncestor().getIndex()));

					// if (!triangle.contains(descendant.getCenter()))
					// throw new GeoTessException(
					// String.format(
					// "%nTriangle %d has descendant element %d, but triangle does not contain
					// descendant's center%n",
					// triangle.getIndex(), descendant.getIndex()));
				}
			}
		}
	}

//	/**
//	 * Convert tessellation to a Delaunay tessellation.
//	 * 
//	 * @param allLevels if true, then every level of the tessellation
//	 * is converted to delaunay.  If false, only the top level is
//	 * converted.
//	 * @return execution time in msec and number of changes.
//	 * @throws GeoModelException
//	 */
//	public long[] delaunay(boolean allLevels) throws GeoTessException
//	{
//		long timer = System.currentTimeMillis();
//
//		HashSet<Edge> edges = new HashSet<Edge>();
//
//		long nChanges = 0;
//
//		int firstLevel = allLevels ? 0 : size()-1;
//		
//		for (int level=firstLevel; level < size(); ++level)
//		{
//			// add all the Edges to a HashSet.
//			for (Triangle t : get(level))
//				for (int i = 0; i < 3; ++i)
//					edges.add(t.getEdge(i));
//
//			// loop over the set of edges until the set is empty.
//			do
//			{
//				// remove one of the Edges.
//				Edge edge = edges.iterator().next();
//				edges.remove(edge);
//
//				// retrieve the circumcenter of one of the triangles that
//				// defines this edge.
//				double[] center = edge.side1.getCircumCenter();
//
//				// find the index of the node in the other triangle (side2),
//				// that is not on the edge.
//				int i2 = edge.side2.getNeighborIndex(edge.side1);
//
//				if (VectorUnit.dot(center, edge.side2.get(i2).getArray()) >
//				VectorUnit.dot(center, edge.side1.get(0).getArray()))
//				{
//					// find the index of the vertex in side1 that is not on the edge.
//					int i1 = edge.side1.getNeighborIndex(edge.side2);
//
//					// make two new triangles that occupy the same area as
//					// side1 and side2 but that have the Edge in the other orientation
//					Triangle t1 = new Triangle(null, 
//							edge.side1.get((i1+1)%3),
//							edge.side2.get(i2), 
//							edge.side1.get(i1), 
//							level, edge.side1.getEdgeLevel());
//
//					Triangle t2 = new Triangle(null, 
//							edge.side2.get((i2+1)%3),
//							edge.side1.get(i1), 
//							edge.side2.get(i2), 
//							level, edge.side2.getEdgeLevel());
//
//					t1.setIndex(-edge.side1.getIndex());
//					t2.setIndex(-edge.side2.getIndex());
//
//					//					// transfer ancestor and dependent relationships
//					//					if (level > 0)
//					//					{
//					//						Triangle ancestor = t1.getAncestor();
//					//						if (ancestor.getDescendant() == edge.side1)
//					//							ancestor.setDescendant(t1);
//					//						else if (ancestor.getDescendant() == edge.side2)
//					//							ancestor.setDescendant(t2);
//					//
//					//						ancestor = t2.getAncestor();
//					//						if (ancestor.getDescendant() == edge.side1)
//					//							ancestor.setDescendant(t1);
//					//						else if (ancestor.getDescendant() == edge.side2)
//					//							ancestor.setDescendant(t2);
//					//					}
//					//
//					//					if (level < size()-1)
//					//					{
//					//						edge.side1.getDescendant().setAncestor(t1);
//					//						edge.side2.getDescendant().setAncestor(t2);
//					//					}
//
//					t1.setDescendant(edge.side1.getDescendant());
//					t2.setDescendant(edge.side2.getDescendant());
//
//					// now need to visit the 4 neighbors of these two
//					// triangles and transfer all the neighbor relations 
//					// and edge relations from side1 and side2 to t1 and t2.
//					Triangle neighbor = edge.side1.getNeighbor((i1+2)%3);
//					int neighborIndex = neighbor.getNeighborIndex(edge.side1);
//					Edge neighborEdge = neighbor.setNeighbor(neighborIndex, t1, 1);
//					edges.add(neighborEdge);
//
//					neighbor = edge.side2.getNeighbor((i2+2)%3);
//					neighborIndex = neighbor.getNeighborIndex(edge.side2);
//					neighborEdge = neighbor.setNeighbor(neighborIndex, t2, 1);
//					edges.add(neighborEdge);
//
//					neighbor = edge.side1.getNeighbor((i1+1)%3);
//					neighborIndex = neighbor.getNeighborIndex(edge.side1);
//					neighborEdge = neighbor.setNeighbor(neighborIndex, t2, 2);
//					edges.add(neighborEdge);
//
//					neighbor = edge.side2.getNeighbor((i2+1)%3);
//					neighborIndex = neighbor.getNeighborIndex(edge.side2);
//					neighborEdge = neighbor.setNeighbor(neighborIndex, t1, 2);
//					edges.add(neighborEdge);
//
//					// remove triangles side1 and side2 from the tessellation
//					get(level).remove(edge.side1);
//					get(level).remove(edge.side2);
//
//					// establish neighbor and edge relations for new triangles t1 and 2.
//					edge.setSides(t1, t2);
//					t1.getEdges()[0] = t2.getEdges()[0] = edge;
//					t1.getNeighbors()[0] = t2;
//					t2.getNeighbors()[0] = t1;
//
//					// add new triangles to the tessellaiton
//					get(level).add(t1);
//					get(level).add(t2);
//
//					++nChanges;
//				}
//			}
//			while (edges.size() > 0);
//		}
//
//		if (nChanges > 0)
//			setConnectivity();
//
//		timer = System.currentTimeMillis()-timer;
//
//		return new long[] {timer, nChanges};
//	}

	/**
	 * Retrieve the PlatonicSolid that serves as tessellation level zero.
	 * 
	 * @return the PlatonicSolid that serves as tessellation level zero.
	 */
	public InitialSolid getInitialSolid() {
		return initialSolid;
	}

}
