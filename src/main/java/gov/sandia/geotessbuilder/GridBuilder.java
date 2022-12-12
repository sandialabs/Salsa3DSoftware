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
import java.util.Date;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessJava;
import gov.sandia.gmp.util.md5.MD5Hash;

public class GridBuilder extends GeoTessGrid {
	private Collection<Tessellation> tessList;

	public GridBuilder(Collection<Tessellation> tessList) throws GeoTessException {
		this.tessList = tessList;
		gridSoftwareVersion = "GridBuilder." + GeoTessJava.getVersion();
		gridGenerationDate = new Date().toString();
		process();
	}

	/**
	 * Construct the primary tessellation which includes the union of all vertices
	 * in all the separate Tessellations that have been added to this Grid. Then
	 * build all the normal data structures for a regular GeoTessGrid object,
	 * including tessellations, levels, triangles and vertices. Also computes the
	 * gridID using an MD5 hash built from the aforementioned data structures.
	 * 
	 * @throws GeoTessException
	 * @throws PolygonException
	 */
	private long process() throws GeoTessException {
		long timer = System.currentTimeMillis();

		// discover the set of common vertices in all tessellations.
		ArrayList<Vertex> commonVertices = null;

		if (tessList.size() == 1)
			// if there is only one tessellation, then it already
			// has the common vertices. getVertices will reindex them.
			commonVertices = tessList.iterator().next().getVertices();
		else
			try {
				// build a primary tessellation which will have all the
				// common vertices. The vertices will be properly indexed.
				commonVertices = new TessellationPrimary(tessList).getVertices();
			} catch (Exception e) {
				throw new GeoTessException(e);
			}

		// store the common vertices in the grid.vertices array
		vertices = new double[commonVertices.size()][];
		for (int i = 0; i < vertices.length; ++i)
			vertices[i] = commonVertices.get(i).getArray();

		// count the number of tessellation levels in all tessellations
		int nLevels = 0;
		for (Tessellation tess : tessList)
			nLevels += tess.size();

		// count triangles in all levels of all tessellations
		int nTriangles = 0;
		for (Tessellation tess : tessList)
			for (ArrayList<Triangle> level : tess)
				for (Triangle t : level)
					t.setIndex(nTriangles++);

		triangles = new int[nTriangles][3];
		levels = new int[nLevels][2];
		tessellations = new int[tessList.size()][2];

		nTriangles = 0;
		nLevels = 0;
		int nTess = 0;

		for (Tessellation tess : tessList) {
			tessellations[nTess][0] = nLevels;
			for (ArrayList<Triangle> level : tess) {
				levels[nLevels][0] = nTriangles;
				for (Triangle t : level) {
					triangles[nTriangles][0] = t.get(0).getIndex();
					triangles[nTriangles][1] = t.get(1).getIndex();
					triangles[nTriangles][2] = t.get(2).getIndex();
					++nTriangles;
				}
				levels[nLevels++][1] = nTriangles;
			}
			tessellations[nTess++][1] = nLevels;
		}

		initialize();

		delaunay();

		MD5Hash md5 = new MD5Hash();
		md5.update(tessellations);
		md5.update(levels);
		md5.update(triangles);
		md5.update(vertices);
		gridID = md5.toString().toUpperCase();
		gridSoftwareVersion = "GridBuilder " + GeoTessJava.getVersion();

		testGrid();

		// release for garbage collection all the Tessellation objects.
		tessList.clear();

		return System.currentTimeMillis() - timer;
	}

}
