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
package gov.sandia.gmp.baseobjects.interfaces;

import java.io.IOException;
import java.util.HashSet;

import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.DataType;
import gov.sandia.gmp.util.globals.InterpolatorType;

public interface ModelInterface {

	/**
	 * Read model data and grid from a file.
	 * 
	 * <p>
	 * If the grid is stored externally, it is assumed that the grid file is located
	 * in the same directory as the model file.
	 * 
	 * @param inputFile name of file containing the model.
	 * @return a reference to this.
	 * @throws IOException
	 */
	ModelInterface load(String inputFile) throws IOException;

	/**
	 * Read model data and grid from a file.
	 * 
	 * @param inputFile       name of file containing the model.
	 * @param relGridFilePath the relative path from the directory where the model
	 *                        is located to the directory where external grid can be
	 *                        found. Ignored if grid is stored in the model file.
	 * @return a reference to this.
	 * @throws IOException
	 */
	ModelInterface load(String inputFile, String relGridFilePath) throws IOException;

	/**
	 * Write the model out to a file.
	 * 
	 * @param filename name of file to which to write the model
	 * @throws IOException
	 */
	void writeModel(String filename, String gridFileName) throws Exception;

	/**
	 * Retrieve the number of active nodes. Nodes refer to data values, not radii.
	 * For example, a layer that has constant values is defined by two radii, one
	 * for the top of the layer and one for the bottom of the layer, but only a
	 * single data value which applies to the entire layer. This would count as only
	 * a single node.
	 * 
	 * @return
	 */
	int getPointCount();

	/**
	 * A 3-element array that defines 3 indexes: the index of the 2D vertex, the
	 * layer index, and the node index within the layer. A node refers to data
	 * values not radii.
	 */
	int[] getPointMap(int pointIndex);

	/**
	 * Retrieve the vertex id of the specified point.
	 * 
	 * @return the vertex id of the specified point.
	 */
	int getPointVertexId(int pointIndex);

	/**
	 * Retrieve the layer id of the specified point.
	 * 
	 * @return the layer id of the specified point.
	 */
	int getPointLayerId(int pointIndex);

	/**
	 * Retrieve the node id of the specified point.
	 * 
	 * @return the node id of the specified point.
	 */
	int getPointNodeId(int pointIndex);

	/**
	 * Retrieve a reference to the unit vector that defines a particular node.
	 * 
	 * @param pointIndex
	 * @return
	 */
	double[] getPointUnitVector(int pointIndex);

	/**
	 * Retrieve the radius of a particular point. Note that nodes refer to data
	 * values not radii in a profile. For layers defined by a bunch of radii/data
	 * values, this is not a problem because they have the same number of radii and
	 * data values. For constant layers, the radius at the top of the layer is
	 * returned. For surface layers, which have no radius, returns NaN.
	 * 
	 * @param pointIndex
	 * @return
	 */
	double getPointRadius(int pointIndex);

	/**
	 * Retrieve the GeoVector of a particular point. Note that points refer to data
	 * values not radii in a profile. For layers defined by a bunch of radii/data
	 * values, this is not a problem because they have the same number of radii and
	 * data values. For constant layers, the radius at the top of the layer is
	 * returned. For surface layers, which have no radius, returns a GeoVector with
	 * the correct unit vector, but radius set to NaN.
	 * 
	 * @param pointIndex
	 * @return
	 */
	GeoVector getPointGeoVector(int pointIndex);

	/**
	 * Replace the value of the specified point with the supplied value.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @param value
	 */
	void setPointValue(int pointIndex, int attributeIndex, double value);

	/**
	 * Retrieve the value of the specified point.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @param value
	 */
	double getPointValue(int pointIndex, int attributeIndex);

	/**
	 * Retrieve a reference to the unit vector that corresponds to the vertex with
	 * the specified index.
	 * 
	 * @param vertex the index of the vertex
	 * @return unit vector
	 */
	double[] getVertexUnitVector(int vertex);

	/**
	 * Returns the angular distance between two unit vectors, in radians.
	 * 
	 * @param vertex1
	 * @param vertex2
	 * @return angular distance in radians
	 */
	double getDistance(int vertex1, int vertex2);

	/**
	 * Find the straight-line distance in km between the tip of vector to
	 * pointIndex1 to the tip of vector to pointIndex2.
	 * 
	 * @param pointIndex1
	 * @param pointIndex2
	 * @return distance in km.
	 */
	double getDistance3D(int pointIndex1, int pointIndex2);

	/**
	 * Return the name of the class. Either GeoModel or GeoTessModel
	 * 
	 * @return
	 */
	String getClassName();

	/**
	 * Set the modelDescription String owned by the model.
	 * 
	 * @param desc
	 */
	void setModelDescription(String desc);

	/**
	 * Retrieve the model description String owned by the model.
	 * 
	 * @return
	 */
	String getModelDescription();

	/**
	 * Given a list of keys, return the corresponding attribute index. List of keys
	 * can have anywhere from 1 to 5 objects.
	 * 
	 * @param keys
	 * @return attribute index or -1 if key set is not supported.
	 * @throws IOException
	 */
	int getAttributeIndex(Object... keys) throws IOException;

	/**
	 * Retrieve the name of the file from which the model was most recently loaded
	 * or written.
	 * 
	 * @return
	 * @throws IOException
	 */
	String getModelFileName() throws IOException;

	/**
	 * Return whether or not the specified data value for the specified point and
	 * attribute is NaN.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @return
	 */
	boolean isNaN(int pointIndex, int attributeIndex);

	/**
	 * Return whether or not the specified data value for the specified point and
	 * attribute is NaN.
	 * 
	 * @param vertexIndex
	 * @param layerIndex
	 * @param nodeIndex
	 * @param attributeIndex
	 * @return
	 */
	boolean isNaN(int vertexIndex, int layerIndex, int nodeIndex, int attributeIndex);

	/**
	 * Retrieve the value of the specified attribute interpolated along a profile
	 * associated with the specified vertex and layer. The value is interpolated at
	 * the specifie radius.
	 * 
	 * @param pointIndex
	 * @param attributeIndex
	 * @param radius
	 * @param interpType     InterpolatorType.LINEAR or
	 *                       InterpolatorType.NATURAL_NEIGHBOR (ignored by GeoModels
	 *                       which only do linear interpolation).
	 * @return
	 * @throws GMPException
	 */
	double getValue(int pointIndex, int attributeIndex, double radius, InterpolatorType interpType) throws GMPException;

	double getValue(int vertexIndex, int layerIndex, int attributeIndex, double radius, InterpolatorType interpType)
			throws GMPException;

	/**
	 * Retrieve a reference to the AttributeIndexer that supports this Model.
	 * 
	 * @return
	 * @throws IOException
	 */
	AttributeIndexerSmart getAttributeIndexer() throws IOException;

	/**
	 * Set the AttributeIndexer for this model. The geometry and connectivity of the
	 * model will be preserved, but all of the data will lost. All data objects will
	 * be replaced with new ones with enough entries to support the number of
	 * attributes supported by the attribute indexer. For GeoModels, all data
	 * objects will be of type float, but for GeoTessModels, the data objects will
	 * be of the type of the specified Number. The data objects will be initialized
	 * with fillValue.
	 * 
	 * @param attributeIndexer
	 * @param fillValue
	 */
	void setAttributeIndexer(AttributeIndexerSmart attributeIndexer, Number fillValue);

	/**
	 * Tell the model to compute vertex triangle neighbors, i.e., for every vertex
	 * in the specified layer, find the set of triangles the vertex is a member of.
	 * This will vary as a function of level and multi-level tessellation.
	 * 
	 * <p>
	 * GeoModel's GridNode objects can only store a single set of triangles for each
	 * GridNode. GeoTessGrid stores a separate set of triangles for each level so we
	 * don't need to do this for GeoTess (GeoTessGMP ignores this function).
	 * 
	 * @param layerIndex
	 * @throws GMPException
	 */
	void setGridNodeElementNeighbors(int layerIndex) throws GMPException;

	/**
	 * Clear vertex triangle neighbors for the specified layer. Necessary for
	 * GeoModel, ignored by GeoTessGMP.
	 * 
	 * @param layerIndex
	 */
	void clearGridNodeElementNeighbors(int layerIndex);

	/**
	 * Find the set of vertices that are connected to the specified vertex at the
	 * top level of the tessellation that is assigned to the specified layer.
	 * 
	 * @param vertexIndex
	 * @param layerIndex
	 * @return the set of vertex indices that are connected to specified vertex by a
	 *         single triangle edge, at the specified layer.
	 * @throws GMPException
	 */
	HashSet<Integer> getVertexNeighbors(int vertexIndex, int layerIndex) throws GMPException;

	/**
	 * Find the set of points that are neighbors of the specified point. In
	 * neighboring profiles, the point with the radius that is closest to the radius
	 * of the supplied point is identified and its poitnIndex included in the
	 * returned set of point indices.
	 * 
	 * @param pointIndex
	 * @return the set of points that are neighbors of the specified point.
	 * @throws GMPException
	 */
	HashSet<Integer> getPointNeighbors(int pointIndex) throws GMPException;

	/**
	 * Retrieve the DataType of the values stored in the model. Will be one of
	 * DOUBLE, FLOAT, LONG, INT, SHORT, BYTE. Note that GeoModel only supports
	 * FLOAT.
	 * 
	 * @return
	 */
	DataType getDateType();

}
