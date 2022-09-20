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

import java.util.Arrays;
import java.util.Collection;

import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;

public class TessellationRefined extends Tessellation {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1676691836667629116L;

	private Tessellation originalTessellation;

	private int maxEdgeLevel;

	private int markThreshold;

	private long timer;

	/**
	 * Given an exiting tessellation and an array of vertices this constructor will
	 * build a new tessellation where all the triangles that touch any of the
	 * verticesToRefine will be subdivided an additional time.
	 * 
	 * @param grid              existing grid that is to be refined.
	 * @param tessellationIndex index of the tessellation in grid that is to be
	 *                          refined.
	 * @param verticesToRefine  an array of unit vectors containing the locations of
	 *                          the vertices that are to be refined.
	 * @throws Exception if any of the specified vertices are not colocated
	 *                          with a vertex in grid.
	 */
	public TessellationRefined(GeoTessGrid grid, InitialSolid initialSolid, int tessellationIndex,
			Collection<double[]> verticesToRefine, int maxEdgeLevel, int markThreshold) throws Exception {
		super(initialSolid, -1);
		this.maxEdgeLevel = maxEdgeLevel;

		this.markThreshold = markThreshold;

		timer = System.currentTimeMillis();

		originalTessellation = new Tessellation(grid, tessellationIndex);

		// temporary array to store interpolation coefficients that are never used.
		double[] c = new double[3];

		// mark every triangle in the originalTessellation that touches a
		// vertex set for refinement.
		for (double[] v : verticesToRefine) {
			Vertex vtx = originalTessellation.findVertex(v, c);
			if (vtx == null)
				throw new Exception(
						String.format("%nvertex not found in original tessellation%n%s%nlat,lon=%s%n",
								Arrays.toString(v), VectorGeo.getLatLonString(v)));

			for (int level = 0; level < originalTessellation.size(); ++level)
				for (Triangle t : vtx.getTriangles(level))
					t.mark();
		}

		build();

		// unmark all the triangles (and vertices) in the original tessellation
		originalTessellation.unMark();

		timer = System.currentTimeMillis() - timer;
	}

	/**
	 * Return true if the specified triangle should be subdivided into smaller
	 * elements.
	 * 
	 * @param triangle Triangle
	 * @return boolean
	 */
	public boolean isDivisible(Triangle triangle) {
		if (triangle.getEdgeLevel() >= maxEdgeLevel)
			return false;

		if (triangle.getTessLevel() == originalTessellation.size())
			return false;

		Triangle t0 = originalTessellation.get(0).get(0);

		// find the triangle in the original tessellation that resides on
		// same tessellation level as t, and contains the center of t.
		t0 = originalTessellation.findTriangle(t0, triangle.getTessLevel(), triangle.getCenter());

		// if original triangle was divided into 4 subtriangles on next level,
		// return true
		if (t0.getNDescendants() == 4 || (t0.getMarked() >= markThreshold && triangle.equals(t0)))
			return true;

		return false;
	}

	public long getTimer() {
		return timer;
	}

}
