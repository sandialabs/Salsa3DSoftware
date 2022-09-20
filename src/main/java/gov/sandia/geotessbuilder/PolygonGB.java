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

import static java.lang.Math.ceil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.gmp.util.numerical.polygon.Polygon2D;

/**
 * Wrapper around Polygon that implements methods to work with Vertex objects
 * instead of unit vectors. Vertex is derived from ArrayListDouble and hence
 * represents a unit vector.
 * 
 * @author sballar
 */
public class PolygonGB implements Cloneable, Callable<Object> {
    Polygon2D polygon;

	private Collection<Vertex> vertices;

	public PolygonGB(Polygon2D polygon) {
		this.polygon = polygon;
	}

	public double[][] getPoints(boolean repeatFirstPoint) {
		return polygon.getPoints(repeatFirstPoint)[0];
	}

	/**
	 * Retrieve the tessellation level with which this Polygon is associated.
	 * 
	 * @return
	 */
	public int getTessLevel() {
		return ((Integer) polygon.attachment).intValue();
	}

	@Override
	public PolygonGB call() {
		for (Vertex v : vertices)
			if (!v.isMarked() && polygon.contains(v.getArray()))
				v.mark();
		return null;
	}

	/**
	 * Visit each of the supplied Vertex objects. If the vertex is not currently
	 * marked and the vertex resides inside this polygon, then mark it.
	 * <p>
	 * If the list of vertices is &gt;= 1000 and nProcessors is &gt; 1, then
	 * concurrency is used to speed up the evaluation process.
	 * 
	 * @param vertices
	 * @param nProcessors number of processors to use.
	 * @throws GeoTessException
	 */
	public void markContainedVertices(Collection<Vertex> vertices, int nProcessors) throws GeoTessException {
		try {
			int verticesPerTask = 1000;
			int nTasks = (int) ceil(vertices.size() / (double) verticesPerTask);

			if (nProcessors <= 1 || nTasks <= 1) {
				// do not use concurrency because either there is only one
				// processor available or because there simply are not enough
				// triangles to make it worth it.

				for (Vertex v : vertices)
					if (!v.isMarked() && polygon.contains(v.getArray()))
						v.mark();
			} else {
				// visit every vertex and mark it if it resides inside this polygon.
				// Use concurrency to process batches of vertices in parallel.

				// set up the thread pool.
				ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(nProcessors);

				CompletionService<Object> queue = new ExecutorCompletionService<Object>(threadPool);

				Iterator<Vertex> it = vertices.iterator();
				for (int task = 0; task < nTasks; ++task) {
					ArrayList<Vertex> taskVertices = new ArrayList<Vertex>(verticesPerTask);
					for (int i = 0; i < verticesPerTask && it.hasNext(); ++i)
						taskVertices.add(it.next());
					PolygonGB copy = (PolygonGB) clone();
					copy.vertices = taskVertices;
					queue.submit(copy);
				}

				// pause until all the tasks are complete.
				for (int task = 0; task < nTasks; ++task)
					queue.take().get();

				threadPool.shutdown();
			}
		} catch (Exception e) {
			throw new GeoTessException(e);
		}
	}

}
