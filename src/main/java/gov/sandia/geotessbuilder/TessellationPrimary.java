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

public class TessellationPrimary extends Tessellation {
	private static final long serialVersionUID = 1L;

	private Collection<Tessellation> tessellations;

	private double[] center;

	private Triangle t;

	private long timer;

	public TessellationPrimary(Collection<Tessellation> tessList) throws Exception {
		super(tessList.iterator().next().initialSolid, -1);
		timer = System.currentTimeMillis();
		tessellations = new ArrayList<Tessellation>(tessList.size());
		for (Tessellation tess : tessList)
			tessellations.add(tess);
		center = new double[3];
		build();
		mergeNodes();
		timer = System.currentTimeMillis() - timer;
	}

	public TessellationPrimary(Tessellation... tessList) throws Exception {
		super(tessList[0].initialSolid, -1);
		timer = System.currentTimeMillis();
		tessellations = new ArrayList<Tessellation>(tessList.length);
		for (Tessellation tess : tessList)
			tessellations.add(tess);
		center = new double[3];
		build();
		mergeNodes();
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
		triangle.getCenter(center);

		// loop over all supplied tessellations
		for (Tessellation tess : tessellations) {
			// find the triangle in tessellation that contains center
			t = tess.findTriangle(tess.get(0).get(0), triangle.getTessLevel(), center);
			// if the triangle was divided into 4
			// sub-triangles, then this triangle also needs to be subdivided.
			if (t.getNDescendants() == 4)
				return true;
		}
		return false;
	}

	/**
	 * Search through all the tessellations that were supplied in the constructor.
	 * It should be true that every vertex in those tessellations has a colocated
	 * vertex in this primary tessellation. Replace all the vertices in the supplied
	 * tessellations with references to vertices from this primary tessellation.
	 * 
	 * @throws Exception
	 */
	private void mergeNodes() throws Exception {
		Vertex v; // a vertex in this primary tessellation
		double[] c = new double[3]; // interpolation coefficients.

		for (Tessellation tess : tessellations) {
			// loop over very vertex of every triangle of every level
			// of the supplied tessellation.
			for (ArrayList<Triangle> level : tess)
				for (Triangle triangle : level)
					for (int i = 0; i < 3; ++i) {
						// find the vertex in the primary tessellation that
						// is colocated with the vertex in the supplied
						// tessellation.
						v = findVertex(get(0).get(0), triangle.getTessLevel(), triangle.get(i).getArray(), c);
						// if this primary does not contain a vertex that is
						// colocated with a vertex in the supplied tessellation,
						// throw error.
						if (v == null)
							throw new Exception("Vertex not found.");
						// replace vertex reference in the supplied tessellation
						// with reference to vertex in this primary tessellation.
						triangle.set(i, v);
					}

		}
	}

	/**
	 * Retrieve the time required to constuct this TessellationPrimary in msec.
	 * 
	 * @return the time required to constuct this TessellationPrimary in msec.
	 */
	public long getTimer() {
		return timer;
	}

}
