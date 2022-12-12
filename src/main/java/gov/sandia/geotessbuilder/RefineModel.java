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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import gov.sandia.geotess.Data;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessModelUtils;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.geotess.Profile;
import gov.sandia.geotess.ProfileConstant;
import gov.sandia.geotess.ProfileEmpty;
import gov.sandia.geotess.ProfileNPoint;
import gov.sandia.geotess.ProfileSurface;
import gov.sandia.geotess.ProfileSurfaceEmpty;
import gov.sandia.geotess.ProfileThin;
import gov.sandia.geotess.ProfileType;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.containers.hash.sets.HashSetInteger;
import gov.sandia.gmp.util.containers.hash.sets.HashSetInteger.Iterator;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.InterpolatorType;

/**
 * Class to that implements resolution refinement of another GeoTessModel
 * @author sballar
 *
 */
public class RefineModel extends GeoTessModel {

	/**
	 * 
	 * @param model
	 * @param pointsToRefine
	 * @param vtkDir
	 * @return
	 * @throws Exception
	 */
	public static GeoTessModel refineModel(GeoTessModel model, ArrayListInt pointsToRefine, File vtkDir)
			throws Exception {
		return refineModel(model, pointsToRefine, null, 1, 1, vtkDir);
	}

	/**
	 * 
	 * @param model
	 * @param pointsToRefine
	 * @param verbosity
	 * @param vtkDir
	 * @return
	 * @throws Exception
	 */
	public static GeoTessModel refineModel(GeoTessModel model, ArrayListInt pointsToRefine, int verbosity, File vtkDir)
			throws Exception {
		return refineModel(model, pointsToRefine, null, 1, verbosity, vtkDir);
	}

	/**
	 * 
	 * @param oldModel
	 * @param pointsToRefine
	 * @param maxTessLevels
	 * @param markThreshold
	 * @param verbosity
	 * @param vtkDir
	 * @return
	 * @throws Exception
	 */
	public static GeoTessModel refineModel(GeoTessModel oldModel, ArrayListInt pointsToRefine, int[] maxTessLevels,
			int markThreshold, int verbosity, File vtkDir) throws Exception {
		long timer = System.currentTimeMillis();

		if (verbosity > 1)
			System.out.printf("Original model:%n%s%n", oldModel);

		if (vtkDir != null) {
			vtkDir.mkdirs();
			GeoTessModelUtils.vtkNPoints(oldModel, vtkDir, "npoints_original_%2d.vtk");
		}

		if (verbosity > 0)
			System.out.print("Constructing new grid ...");

		long tmr = System.currentTimeMillis();

		GeoTessGrid newGrid = buildNewGrid(oldModel, pointsToRefine, maxTessLevels, markThreshold);

		tmr = System.currentTimeMillis() - tmr;
		if (verbosity > 0)
			System.out.printf(" %1.3f sec%n", tmr * 1e-3);

		// an array of length equal to number of vertices in old grid.
		// Each element is the index of the colocated vertex in the new grid.
		// This array gets populated in method getNewVertices()
		ArrayListInt oldToNew = new ArrayListInt();

		// an array of length equal to number of vertices in new grid.
		// Each element is the index of the colocated vertex in the old grid,
		// or -1 for vertices that do now exist in the old grid.
		// This array gets populated in method getNewVertices()
		ArrayListInt newToOld = new ArrayListInt();

		// Find the newVertices connected to each other in the newModel, by layer.
		ArrayList<HashSet<Integer>> newVertices = getNewVertices(oldModel, newGrid, oldToNew, newToOld);

		if (verbosity > 0) {
			System.out.println("\nVertices that are connected in the newGrid but not in oldGrid:");
			for (int layer = 0; layer < newVertices.size(); ++layer) {
				HashSet<Integer> list = newVertices.get(layer);
				System.out.printf("   layer=%d size=%d: ", layer, list.size());
				for (int vertex : list)
					System.out.printf(" %d", vertex);
				System.out.println();
			}
			System.out.println();

			System.out.print("Populating new model with data ...");
		}

		tmr = System.currentTimeMillis();

		GeoTessModel newModel = buildNewModel(oldModel, newGrid, newVertices, oldToNew, newToOld, pointsToRefine);

		// check every Profile in the new model and throw an exception if it is null.
		for (int layer = 0; layer < newModel.getNLayers(); ++layer)
			for (int vertex = 0; vertex < newModel.getNVertices(); ++vertex)
				if (newModel.getProfile(vertex, layer) == null)
					throw new Exception("profile is null");

		if (verbosity > 0)
			System.out.printf(Globals.elapsedTime(tmr));

		if (verbosity > 1)
			System.out.printf("%nNew model:%n%s%n", newModel);

		if (verbosity > 0)
			System.out.printf("Total time required to refine model %s%n%n", Globals.elapsedTime(timer));

		if (vtkDir != null)
			GeoTessModelUtils.vtkNPoints(newModel, vtkDir, "npoints_refined_%2d.vtk");

		return newModel;
	}

	/**
	 * Build a new, refined GeoTessGrid that includes all the multi-level
	 * tessellations.
	 * 
	 * @param oldModel
	 * @param pointsToRefine
	 * @param maxTessLevels
	 * @param markThreshold
	 * @return
	 * @throws Exception
	 */
	static public GeoTessGrid buildNewGrid(GeoTessModel oldModel, ArrayListInt pointsToRefine, int[] maxTessLevels,
			int markThreshold) throws Exception {

		GeoTessGrid oldGrid = oldModel.getGrid();

		ArrayList<Tessellation> tessellations = new ArrayList<Tessellation>(oldGrid.getNTessellations());

		// deduce the initial tessellation from the grid of the input model. This might
		// be
		// one of the PlatonicSolids, or a rotated version.
		InitialSolid solid = new InitialSolid(oldGrid, 0);

		// loop over all tessellations in the old model
		for (int tessid = 0; tessid < oldGrid.getNTessellations(); ++tessid) {
			// clear list of vertices that includes vertices specified in pointsToRefine.
			HashSet<double[]> vertices = new HashSet<double[]>(100);
			int[] pmap;

			// find list of layers that are associated with current tessellation
			int[] layers = oldModel.getMetaData().getLayers(tessid);

			// loop over points to refine.
			for (int i = 0; i < pointsToRefine.size(); ++i) {
				// 0:vertex index, 1:layer index, 2:node index
				pmap = oldModel.getPointMap().getPointIndices(pointsToRefine.get(i));
				// find all pointsToRefine whose layer index
				// corresponds to one of the layers associated with
				// this tessellation.
				for (int lid = 0; lid < layers.length; ++lid)
					if (layers[lid] == pmap[1])
						// retrieve the vertex that is associated with
						// the point to refine and add it to set
						// of vertices.
						vertices.add(oldGrid.getVertices()[pmap[0]]);
			}

			// create a Tessellation that incorporates the old tessellation but
			// that may have an extra level for the refined vertices. All the
			// triangles that have at least markThreshold corners on a vertex in
			// pointsToRefine will be subdivided an extra time.
			TessellationRefined tess = new TessellationRefined(oldGrid, solid, tessid, vertices,
					maxTessLevels == null ? Integer.MAX_VALUE : maxTessLevels[tessid], markThreshold);

			// add the Tessellation to the list of Tessellations included in newGrid.
			tessellations.add(tess);
		}

		// instantiate a new empty grid
		GridBuilder newGrid = new GridBuilder(tessellations);

		return newGrid;
	}

	/**
	 * For each layer of the oldModel, find the indices of the vertices in the
	 * newGrid that are not connected in the oldGrid. Return newVertices: an array
	 * of size nLayers where each element is a list of the vertices in that layer
	 * that are new (don't exist, or, are not connected in the old model).
	 * 
	 * @param oldModel
	 * @param newGrid
	 * @param oldToNew int[] of length = number of vertices in oldModel, populated
	 *                 with corresponding vertex in newGrid. Must have dimensions
	 *                 oldModel.getNVertices() on input and is populated in this
	 *                 method.
	 * @param newToOld int[] of length = number of vertices in newGrid, populated
	 *                 with corresponding vertex in oldGrid or -1 for vertices that
	 *                 do not exist in oldModel. Must have dimensions
	 *                 newGrid.getNVertices() on input and is populated in this
	 *                 method.
	 * @return array of length nLayers containing the set of vertices in newGrid
	 *         that are not connected in the oldModel in the corresponding layer.
	 */
	static public ArrayList<HashSet<Integer>> getNewVertices(GeoTessModel oldModel, GeoTessGrid newGrid,
			ArrayListInt oldToNew, ArrayListInt newToOld) {

		ArrayList<HashSet<Integer>> newVertices = new ArrayList<>();
		// ensure an entry for each layer
		for (int i = 0; i < oldModel.getNLayers(); ++i)
			newVertices.add(null);

		// instantiate arrays oldToNew and newToOld
		int[] x = new int[oldModel.getNVertices()];
		Arrays.fill(x, -1);
		oldToNew.setArray(new int[oldModel.getNVertices()]);

		x = new int[newGrid.getNVertices()];
		Arrays.fill(x, -1);
		newToOld.setArray(x);

		// loop over all the multi-level tessellations
		for (int tessId = 0; tessId < oldModel.getGrid().getNTessellations(); ++tessId) {

			// instantiate a new set of integers to contain the indices of the vertices
			// in the new model and add a reference to newVertices for each layer that
			// is associated with that tessellation
			HashSet<Integer> newvtx = new HashSet<Integer>();
			for (int layer : oldModel.getMetaData().getLayers(tessId))
				newVertices.set(layer, newvtx);

			// Iterator over all the vertices that are connected together by triangles on
			// the top level of the specified tessellation.
			Iterator it = newGrid.getVertexIndicesTopLevel(tessId).iterator();
			while (it.hasNext()) {
				// the index of a vertex in the new grid.
				int newGridVertex = it.next();

				// find the index of the newGridVertex in the oldGrid, considering only vertices
				// connected at this tessellation. will = -1 if does not exist.
				int oldGridVertex = oldModel.getGrid().getVertexIndex(newGrid.getVertex(newGridVertex), tessId);

				// if the current vertex is not connected by triangles in the oldGrid in the
				// current multi-level tessellation, then add it to newvtx.
				// Otherwise, add the vertex index to the newToOld array.
				if (oldGridVertex < 0)
					newvtx.add(newGridVertex);
				else
					oldToNew.set(oldGridVertex, newGridVertex);

				// make an entry in the map from newToOld vertex indices.
				newToOld.set(newGridVertex, oldGridVertex);
			}
		}
		return newVertices;
	}

	/**
	 * Create a new GeoTessModel using the newGrid and populate it with data copied
	 * or interpolated from the oldModel.
	 * 
	 * @param oldModel
	 * @param newGrid
	 * @param newVertices
	 * @param oldToNew
	 * @param newToOld
	 * @param pointsToRefine
	 * @return
	 * @throws Exception
	 */
	static public GeoTessModel buildNewModel(GeoTessModel oldModel, GeoTessGrid newGrid,
			ArrayList<HashSet<Integer>> newVertices, ArrayListInt oldToNew, ArrayListInt newToOld,
			ArrayListInt pointsToRefine) throws Exception {

		// create a new model of the same derived class as the old model, using the new
		// Grid and a copy of the metaData from the oldModel.
		GeoTessModel newModel = GeoTessModel.getGeoTessModel(newGrid, new GeoTessMetaData(oldModel.getMetaData()));

		// copy the 'extra' data from the oldModel.
		newModel.copyDerivedClassData(oldModel);

		// for all vertices in the old model copy the Profiles from the old
		// model to the new model.
		for (int vertex = 0; vertex < oldToNew.size(); ++vertex)
			for (int layer = 0; layer < oldModel.getNLayers(); ++layer)
				newModel.setProfile(oldToNew.get(vertex), layer, oldModel.getProfile(vertex, layer).copy());

		// Create a GeoTessPosition object to use to interpolate model data from the
		// oldModel.
		GeoTessPosition posOld = oldModel.getGeoTessPosition(InterpolatorType.LINEAR);

		// for all vertices in the new model that are not in the old model, populate the
		// Profiles with empty profiles.
		if (newModel.is2D()) 
		{
			for (int v = 0; v < newModel.getNVertices(); ++v)
				if (newToOld.get(v) < 0)
					newModel.setProfile(v);
		} 
		else 
		{
			float rbot=0, rtop=0;
			for (int v = 0; v < newModel.getNVertices(); ++v)
				if (newToOld.get(v) < 0) 
				{
					// ensure that radii at the top of layer(i-1) == bottom of layer(i)
					for (int layer = 0; layer < newModel.getNLayers(); ++layer)
					{
						posOld.set(layer, newGrid.getVertex(v), 1e4);

						if (layer == 0)
							rbot = (float) posOld.getRadiusBottom(layer);
						rtop = (float) posOld.getRadiusTop(layer);

						newModel.setProfile(v, layer, new float[] {rbot, rtop});

						rbot = rtop;
					}
				}
		}

		// build a map VertexIndex -> LayerIndex -> ArrayList of NodeIndex
		// containing the points to be refined but with indices in the new model
		HashMap<Integer, HashMap<Integer, HashSetInteger>> map = new HashMap<>();
		for (int i = 0; i < pointsToRefine.size(); ++i) {
			// 0: vertex index, 1: layer index, 2: node index; in old model
			int[] pmap = oldModel.getPointMap().getPointIndices(pointsToRefine.get(i));
			int newVertex = oldToNew.get(pmap[0]);
			int layerIndex = pmap[1];
			int nodeIndex = pmap[2];
			HashMap<Integer, HashSetInteger> layermap = map.get(newVertex);
			if (layermap == null)
				map.put(newVertex, layermap = new HashMap<Integer, HashSetInteger>());

			HashSetInteger nodes = layermap.get(layerIndex);
			if (nodes == null)
				layermap.put(layerIndex, nodes = new HashSetInteger());

			nodes.add(nodeIndex);
		}

		// Refine the profiles in the new model that have entries in the
		// pointToRefine array. Only if ProfileType is NPOINTS.
		TreeSet<Float> setOfRadii = new TreeSet<>();
		ArrayList<Data> dataList = new ArrayList<Data>();
		// iterate over newVertices
		for (Integer vertex : map.keySet()) {
			// iterate over the layer of this new vertex
			HashMap<Integer, HashSetInteger> layerMap = map.get(vertex);
			for (Integer layer : layerMap.keySet()) {
				// get the list of node indices for this vertex-layer.
				HashSetInteger nodes = layerMap.get(layer);
				// get a reference to the Profile for vertex-layer in the newModel
				Profile profile = newModel.getProfile(vertex, layer);
				// only refine in the radial dimension if this profile is of type npoint.
				// profiles of other types do not get refined radially.
				if (profile.getType() == ProfileType.NPOINT) {
					// add more radii on either side of the radius of the current node.
					// Since setOfRaii is a TreeSet, the radii will be ordered.
					for (int i = 0; i < profile.getNRadii(); ++i) {
						setOfRadii.add((float) profile.getRadius(i));
						if (nodes.contains(i)) {
							if (i > 0)
								setOfRadii.add((float) ((profile.getRadius(i) + profile.getRadius(i - 1)) / 2));
							if (i < profile.getNRadii() - 1)
								setOfRadii.add((float) ((profile.getRadius(i) + profile.getRadius(i + 1)) / 2));
						}
					}

					// copy radii from the TreeSet to a float array.
					float[] radii = new float[setOfRadii.size()];
					int i = 0;
					for (Float r : setOfRadii) {
						radii[i++] = r;
						// interpolate data from the Profile for both new and old nodes.
						// Interpolation is only happening in the radial direction (along the profile).
						dataList.add(((ProfileNPoint) profile).getData(InterpolatorType.LINEAR, r, true));
					}

					// set the Profile at the current vertex-layer.
					newModel.setProfile(vertex, layer,
							new ProfileNPoint(radii, dataList.toArray(new Data[dataList.size()])));

					setOfRadii.clear();
					dataList.clear();
				}
			}
		}

		// at this point all profiles in the new model that have corresponding
		// profiles in the old model have had their profiles updated, including
		// radial refinement where necessary. Remains to populate profiles in
		// the new model that were added during refinement. These are currently
		// ProfileEmpty objects but need to be replaced with new Profiles of the
		// appropriate type.

		// newVertices is an array (of length nLayers) where each element is
		// the Set of new vertices in the newModel that are connected at the
		// corresponding layer, and which need to be populated.
		for (int layer = 0; layer < newVertices.size(); ++layer) {
			HashSet<Integer> vertices = newVertices.get(layer);
			// get the id of the multi-level tessellation that supports this layer.
			int tessId = newModel.getMetaData().getTessellation(layer);
			// find the index of the top tessellation level in this multi-level
			// tessellation.
			int level = newGrid.getNLevels(tessId) - 1;
			for (int newVertex : vertices) {

				// set the interpolator in the oldModel to the current layer and vertex
				// location. The radius is set to 1e4 and is not relevant at this point.
				posOld.set(layer, newGrid.getVertex(newVertex), 1e4);

				Profile profile = newModel.getProfile(newVertex, layer);

				// now search for a profile that is a neighbor of the new vertex and which
				// is populated in the oldModel, and which has the highest number of nodes
				// (Data objects).
				Profile neighborProfile = null;

				// retrieve the set of vertices that are neighbors of the new vertex in the
				// newGrid at the current tessellation and tessellation level.
				HashSet<Integer> neighbors = newGrid.getVertexNeighbors(tessId, level, newVertex);
				for (int neighbor : neighbors)
					// ignore neighbors that are newVertices (i.e., consider only vertices that
					// are connected in the oldModel).
					if (!vertices.contains(neighbor)) {
						// get the neighbor's Profile object.
						Profile p = newModel.getProfile(neighbor, layer);
						// if the neighbor has more Nodes (Data value), select it.
						if (neighborProfile == null || p.getNData() > neighborProfile.getNData())
							neighborProfile = p;
					}

				// so now neighborProfile is the Profile object which is a neighbor of the
				// newVertex in the current layer, which is connected in this layer in the
				// oldModel, and which has the highest number of Data values.
				// Now make a new Profile of the same ProfileType using the same radii and
				// new data values interpolated from the oldModel.
				switch (neighborProfile.getType()) {
				case EMPTY:
					// empty layer defined by two radii and no data
					newModel.setProfile(newVertex, layer, new ProfileEmpty(profile.getRadii()[0], profile.getRadii()[profile.getNRadii()-1]));
					break;
				case THIN:
					// zero-thickness layer defined by one radius and one data
					newModel.setProfile(newVertex, layer, new ProfileThin(profile.getRadii()[0], posOld.getData()));
					break;
				case CONSTANT:
					// constant layer defined by two radii and one data object
					newModel.setProfile(newVertex, layer,
							new ProfileConstant(profile.getRadii()[0], profile.getRadii()[profile.getNRadii()-1], posOld.getData()));
					break;
				case NPOINT:
					// n radii and n data objects, n >= 2
					float[] radii = radii(neighborProfile.getRadii(), profile.getRadii()[0], profile.getRadii()[profile.getNRadii()-1]);
					Data dataArray[] = new Data[radii.length];

					for (int j = 0; j < radii.length; ++j) {
						posOld.setRadius(layer, radii[j]);
						dataArray[j] = posOld.getData();
					}
					newModel.setProfile(newVertex, layer, new ProfileNPoint(radii, dataArray));
					break;
				case SURFACE:
					// layer with 0 radii and one data object
					newModel.setProfile(newVertex, layer, new ProfileSurface(posOld.getData()));
					break;
				case SURFACE_EMPTY:
					// empty layer defined by no radii and no data (pretty simple!)
					newModel.setProfile(newVertex, layer, new ProfileSurfaceEmpty());
					break;
				default:
					throw new Exception("Unrecognized ProfileType");
				}
			}
		}

		// throw an exception if any of the nVertices x nLayers Profiles in the
		// new model are null.
		for (int layer = 0; layer < newModel.getNLayers(); ++layer)
			for (int v = 0; v < newModel.getNVertices(); ++v)
				if (newModel.getProfile(v, layer) == null)
					throw new Exception(String.format("Profile vertex=%d layer=%d is null", v, layer));

		// if the oldModel had a Polygon set, set the same Polygon in the
		// newModel. Otherwise set activeRegion to include all nodes in the model.
		newModel.setActiveRegion(oldModel.getPointMap().getPolygon());

		newModel.testModelIntegrity();

		return newModel;
	}

	/**
	 * Given an array of radii, retrieve a new array of radii where r[0] and r[n]
	 * are set to rbot and rtop and the intervening radii maintain the same
	 * fractional spacing.
	 * 
	 * @param oldRadii
	 * @param rbot
	 * @param rtop
	 * @return
	 */
	static private float[] radii(float[] oldRadii, float rbot, float rtop) {
		float[] radii = new float[oldRadii.length];
		double oldThickness = oldRadii[oldRadii.length - 1] - oldRadii[0];

		for (int j = radii.length - 1; j >= 0; --j)
			radii[j] = (float) (rbot + (rtop - rbot) * (oldRadii[j] - oldRadii[0]) / oldThickness);

		return radii;
	}

}
