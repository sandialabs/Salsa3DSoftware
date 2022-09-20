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
package gov.sandia.gmp.geotessgmp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.interfaces.ModelInterface;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

public class GeoTessModelGMP extends GeoTessModel implements ModelInterface
{
	
	private AttributeIndexerSmart attributeIndexer;
	
	public GeoTessModelGMP(File inputFile, String relativeGridPath) throws IOException
	{ super(inputFile, relativeGridPath); }
	
	public GeoTessModelGMP(File inputFile) throws IOException
	{ super(inputFile, ""); }
	
	public GeoTessModelGMP(String inputFile, String relativeGridPath) throws IOException
	{ super(inputFile, relativeGridPath); }
	
	public GeoTessModelGMP(String inputFile) throws IOException
	{ super(inputFile, ""); }
	
	public GeoTessModelGMP(String gridFileName, GeoTessMetaData metaData) throws IOException
	{ super(gridFileName, metaData); }

	public GeoTessModelGMP(GeoTessGrid grid, GeoTessMetaData metaData) throws GeoTessException, IOException
	{ super(grid, metaData); }
	
	
	@Override
	public String getClassName()
	{
		return "GeoTessModel";
	}

	/**
	 * Read model data and grid from a file.
	 * 
	 * <p>
	 * If the grid is stored externally, it is assumed that the grid file is
	 * located in the same directory as the model file.
	 * 
	 * @param inputFile
	 *            name of file containing the model.
	 * @return a reference to this.
	 * @throws IOException
	 */
	@Override
	public ModelInterface load(String inputFile) throws IOException
	{
		return (ModelInterface) loadModel(inputFile);
	}

	/**
	 * Read model data and grid from a file.
	 * 
	 * @param inputFile
	 *            name of file containing the model.
	 * @param relGridFilePath
	 *            the relative path from the directory where the model is
	 *            located to the directory where external grid can be found.
	 *            Ignored if grid is stored in the model file.
	 * @return a reference to this.
	 * @throws IOException
	 */
	@Override
	public ModelInterface load(String inputFile, String relGridFilePath) throws IOException
	{
		return (ModelInterface) loadModel(inputFile, relGridFilePath);
	}

	@Override
	public GeoVector getPointGeoVector(int pointIndex)
	{
		return new GeoVector(getPointUnitVector(pointIndex),
				getPointRadius(pointIndex));
	}

	/**
	 * Set the GeoTessAttributes based on the keys of the specified
	 * attributeIndexer. This will also change the DataType of the model to the
	 * type of the fillValue parameter and fill all the data values in the
	 * entire model with fillValue.
	 * 
	 * @param attributeIndexer
	 * @param fillValue
	 * @throws GMPException 
	 * @throws GeoTessException
	 */
	public void setAttributeIndexer(AttributeIndexerSmart attributeIndexer, Number fillValue)
	{
		this.attributeIndexer = attributeIndexer;
		String[] attributeNames = attributeIndexer.getKeyStrings();
		String[] units = new String[attributeNames.length];
		Arrays.fill(units, "na");

		try
		{
			initializeData(attributeNames, units, fillValue);
		}
		catch (GeoTessException e)
		{
			throw new Error(e);
		}
	}
	
	@Override
	public int getAttributeIndex(Object... keys) throws IOException
	{
		return getAttributeIndexer().getIndex(keys);
	}

	@Override
	public AttributeIndexerSmart getAttributeIndexer() throws IOException
	{
		if (attributeIndexer == null)
		{
			attributeIndexer = new AttributeIndexerSmart(getMetaData().getAttributeNames());
		}
		return attributeIndexer;
	}

	@Override
	public void setGridNodeElementNeighbors(int layerIndex)
	{
		// This method is only here because ModelInterface and GeoModel require it.
		// Ignored.
	}

	@Override
	public void clearGridNodeElementNeighbors(int layerIndex)
	{
		// This method is only here because ModelInterface and GeoModel require it.
		// Ignored.
	}

	@Override
	public void setModelDescription(String description)
	{
		getMetaData().setDescription(description);
	}

	@Override
	public String getModelDescription()
	{
		return getMetaData().getDescription();
	}

	/**
	 * Retrieve the full path+fileName of the file containing the model.
	 * 
	 * @return String
	 * @throws IOException
	 */
	@Override
	public String getModelFileName() throws IOException
	{
		return getMetaData().getInputModelFile().getCanonicalPath();
	}
	
	/**
	 * Retrieve the number of points in the model, including all nodes along all
	 * profiles in all layers, at all grid vertices.
	 * 
	 * @return the number of points in the model.
	 */
	@Override
	public int getPointCount()
	{
		return getNPoints();
	}

	@Override
	public double getValue(
			int pointIndex,
			int attributeIndex,
			double radius,
			InterpolatorType interpType)
	{
		int[] map = getPointMap().getPointIndices(pointIndex);
		return getValue(map[0], map[1], attributeIndex, radius, interpType);
	}

	@Override
	public HashSet<Integer> getVertexNeighbors(int vertexIndex, int layerIndex)
	{
		int tessId = getMetaData().getTessellation(layerIndex);
		return getGrid().getVertexNeighbors(tessId,
				getGrid().getNLevels(tessId)-1, vertexIndex);
	}

	/**
	 * Retrieve the value of the specified attribute interpolated along a
	 * profile associated with the specified vertex and layer. The value is
	 * interpolated at the specific radius.
	 * 
	 * @param vertexIndex
	 * @param layerIndex
	 * @param attributeIndex
	 * @param interpType
	 *            InterpolatorType.LINEAR or InterpolatorType.NATURAL_NEIGHBOR
	 *            (ignored by GeoModels which only do linear interpolation).
	 * @return
	 * @throws GMPException 
	 */
	public double getValue(
			int vertexIndex,
			int layerIndex,
			int attributeIndex,
			double radius,
			InterpolatorType interpType)
	{
		return getProfile(vertexIndex, layerIndex).getValue(
				interpType, attributeIndex, radius, false);
	}

	/**
	 * Retrieve a reference to the unit vector that corresponds to the vertex with
	 * the specified index.
	 * @param vertex the index of the vertex 
	 * @return unit vector
	 */
	public double[] getVertexUnitVector(int vertex)
	{
		return getVertex(vertex);
	}

	/**
	 * Returns the angular distance between two vertices, in radians.
	 * @param vertex1
	 * @param vertex2
	 * @return angular distance in radians
	 */
	public double getDistance(int vertex1, int vertex2)
	{
		return VectorUnit.angle(getVertex(vertex1), getVertex(vertex2));
	}

	public boolean isNaN(int vertexIndex, int layerIndex, int nodeIndex,
			int attributeIndex)
	{
		return getProfile(vertexIndex, layerIndex).isNaN(nodeIndex,
				attributeIndex);
	}

	@Override
	public gov.sandia.gmp.util.globals.DataType getDateType()
	{
		return getMetaData().getDataType();
	}

	@Override
	public int[] getPointMap(int pointIndex)
	{
		return getPointMap().getPointIndices(pointIndex);
	}

	@Override
	public double[] getPointUnitVector(int pointIndex)
	{
		return getPointMap().getPointUnitVector(pointIndex);
	}

	@Override
	public double getPointRadius(int pointIndex)
	{
		return getPointMap().getPointRadius(pointIndex);
	}

	@Override
	public void setPointValue(int pointIndex, int attributeIndex, double value)
	{
		getPointMap().setPointValue(pointIndex, attributeIndex, value);
	}

	@Override
	public double getPointValue(int pointIndex, int attributeIndex)
	{
		return getPointMap().getPointValue(pointIndex, attributeIndex);
	}

	@Override
	public double getDistance3D(int pointIndex1, int pointIndex2)
	{
		return getPointMap().getDistance3D(pointIndex1, pointIndex2);
	}

	@Override
	public boolean isNaN(int pointIndex, int attributeIndex)
	{
		return getPointMap().isNaN(pointIndex, attributeIndex);
	}

	@Override
	public HashSet<Integer> getPointNeighbors(int pointIndex) throws GMPException
	{
		return getPointMap().getPointNeighbors(pointIndex);
	}

	@Override
	public int getPointVertexId(int pointIndex)
	{
		return getPointMap().getVertexIndex(pointIndex);
	}

	@Override
	public int getPointLayerId(int pointIndex)
	{
		return getPointMap().getLayerIndex(pointIndex);
	}

	@Override
	public int getPointNodeId(int pointIndex)
	{
		return getPointMap().getNodeIndex(pointIndex);
	}

}
