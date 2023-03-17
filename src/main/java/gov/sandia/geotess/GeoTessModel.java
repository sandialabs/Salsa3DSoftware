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
package gov.sandia.geotess;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import gov.sandia.geotess.extensions.amplitude.GeoTessModelAmplitude;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.geotess.extensions.rstt.GeoTessModelSLBM;
import gov.sandia.geotess.extensions.rstt.GeoTessModelSLBMPDU;
import gov.sandia.geotess.extensions.siteterms.GeoTessModelSiteData;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerDouble;
import gov.sandia.gmp.util.containers.hash.sets.HashSetInteger;
import gov.sandia.gmp.util.containers.hash.sets.HashSetInteger.Iterator;
import gov.sandia.gmp.util.globals.DataType;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.polygon.Polygon;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * <b>GeoTessModel</b> manages the <i>grid</i> and <i>data</i> that comprise a
 * 3D Earth model. The Earth is assumed to be composed of a number of
 * <i>layers</i> each of which spans the entire geographic extent of the Earth.
 * It is assumed that layer boundaries do not fold back on themselves, i.e.,
 * along any radial profile through the model, each layer boundary is
 * intersected exactly one time. Layers may have zero thickness over some or all
 * of their geographic extent. Earth properties stored in the model are assumed
 * to be continuous within a layer, both geographically and radially, but may be
 * discontinuous across layer boundaries.
 * 
 * <p>
 * A <b>GeoTessModel</b> is comprised of 4 major components:
 * <ul>
 * <li>The model <i>grid</i> (<i>geometry</i> and <i>topology</i>) is managed by
 * a <b>GeoTessGrid</b> object. The grid is made up of one or more 2D triangular
 * tessellations of a unit sphere.
 * 
 * <li>The <i>data</i> are managed by a 2D array of <b>Profile</b> objects. A
 * <b>Profile</b> is essentially a list of radii and <b>Data</b> objects
 * distributed along a radial profile that spans a single layer at a single
 * vertex of the 2D grid. The 2D Profile array has dimensions nVertices by
 * nLayers.
 * 
 * <li>Important metadata about the model, such as the names of the major
 * layers, the names of the data attributes stored in the model, etc., are
 * managed by a <b>GeoTessMetaData</b> object.
 * 
 * <li>PointMap facilitates accessing information in the model by providing a
 * map between point indexes, vertex indexes, layer indexes and node indexes.
 * See documentation for PointMap for more information.
 * </ul>
 * 
 * 
 * There are many different indexes used to access information:
 * <ul>
 * <li><i>vertex</i> refers to a position in the 2D tessellation. They are 2D
 * positions represented by unit vectors on a unit sphere. Vertices are managed
 * in the GeoTessGrid object which can be accessed via model.getGrid().
 * <li><i>layer</i> refers to one of the layers in the model, such as the core,
 * mantle or crust. Layers are defined in the GeoTessMetaData object which can
 * be accessed from model.getMetaData().
 * <li><i>node</i> refers to a Data object on a radial Profile associated with a
 * vertex and a layer in the model. Node indexes are unique only within a given
 * Profile (all Profile have a node with index 0 for example).
 * <li><i>point</i> refers to all the nodes in all the Profiles of the model.
 * There is only one 'point' in the model with index 0.
 * </ul>
 * 
 * PointMap is introduced to help manage all these different indexes.
 * 
 * <p>
 * GeoTessModel is thread-safe in that its internal state is ony modified in
 * synchronized methods after its data has been loaded into memory. The design
 * intention is that single instances of a GeoTessModel object can be shared
 * among all the threads in a multi-threaded application and each thread will
 * have it's own instance of a GeoTessPosition object that references the common
 * GeoTessModel.
 * 
 * @author Sandy Ballard
 * 
 */
public class GeoTessModel
{

	/**
	 * The GeoTessGrid object that supports the 2D components of the model grid.
	 */
	private GeoTessGrid grid;

	/**
	 * The data stored in the model. An nVertices x nLayers array of Profile
	 * objects. Each Profile consists of an array of radii and the associated
	 * Data.
	 */
	private Profile[][] profiles;

	/**
	 * metaData stores basic information about a GeoTessModel including:
	 * <ul>
	 * <li>textual description of the model
	 * <li>the names of all the layers in the model, e.g., ["core", "mantle",
	 * "crust"]. Layer names are specified in order of increasing radius.
	 * <li>dataType; all the data values stored in the model are of this type.
	 * Must be one of DOUBLE, FLOAT, LONG, INT, SHORTINT, BYTE.
	 * <li>number of data attributes
	 * <li>the names of all the data attributes ["P Velocity", "S Velocity",
	 * "Density", etc]
	 * <li>the units of the data attributes ["km/sec", "km/sec", "g/cc", etc]
	 * <li>layerTessIds: an integer map from layer index to tessellation index.
	 * </ul>
	 * Each GeoTessModel has a single instance of MetaData that it passes around
	 * to wherever the information is needed.
	 */
	private GeoTessMetaData metaData = null;

	/**
	 * A nPoints by 3 array of indexes. For each point in the 3D grid, pointMap
	 * stores 3 indexes: (0) the index of the 2D vertex, (1) the layer index,
	 * and (2) the node index within the layer.
	 * <p>
	 * Note that the number of nodes in each profile corresponds to the number
	 * of data objects associated with the profile, not the number of radii.
	 * <ul>
	 * Number of pointMap nodes in each type of Profile:
	 * <li>empty: 0
	 * <li>thin:1
	 * <li>constant: 1
	 * <li>npoint: n
	 * <li>surface: 1
	 * </ul>
	 */
	private PointMap pointMap;

	/**
	 * Grid reuse map enabling multiple models to use the same grid
	 * instantiation.
	 */
	private static HashMap<String, GeoTessGrid> reuseGridMap;

	/**
	 * Pool of GradientCalculator objects used to compute attribute field
	 * gradients in DataLayer objects. Since the gradient calculations modify
	 * the internal state of a GeoTessModel, the calculations have to be done
	 * inside synchronized methods.  They also need to use synchronized
	 * GradientCalculator objects.
	 */
	private ConcurrentLinkedQueue<GradientCalculator> gradientCalculatorPool =
			new ConcurrentLinkedQueue<GradientCalculator>();
	
	/**
	 * Simple return class that can be over-ridden by derived types to return
	 * extended meta-data objects.
	 * 
	 * @return new meta-data object
	 */
	protected GeoTessMetaData getNewMetaData()
	{
		return new GeoTessMetaData();
	}

	/**
	 * Construct a new GeoTessModel object with uninitialized meta data.
	 */
	protected GeoTessModel() 
	{
		metaData = getNewMetaData();
		metaData.setModelClassName(this.getClass().getSimpleName());

		if (reuseGridMap == null)
			reuseGridMap = new HashMap<String, GeoTessGrid>();
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param relativeGridPath
	 *            the relative path from the directory where the model is stored
	 *            to the directory where the grid is stored. Often, the model
	 *            and grid are stored together in the same file in which case
	 *            this parameter is ignored. Sometimes, however, the grid is
	 *            stored in a separate file and only the name of the grid file
	 *            (without path information) is stored in the model file. In
	 *            this case, the code needs to know which directory to search
	 *            for the grid file. The default is "" (empty string), which
	 *            will cause the code to search for the grid file in the same
	 *            directory in which the model file resides. Bottom line is that
	 *            the default value is appropriate when the grid is stored in
	 *            the same file as the model, or the model file is in the same
	 *            directory as the model file.
	 * @throws IOException
	 */
	public GeoTessModel(File modelInputFile, String relativeGridPath) throws IOException
	{
		this();
		loadModel(modelInputFile, relativeGridPath);
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param relativeGridPath
	 *            the relative path from the directory where the model is stored
	 *            to the directory where the grid is stored. Often, the model
	 *            and grid are stored together in the same file in which case
	 *            this parameter is ignored. Sometimes, however, the grid is
	 *            stored in a separate file and only the name of the grid file
	 *            (without path information) is stored in the model file. In
	 *            this case, the code needs to know which directory to search
	 *            for the grid file. The default is "" (empty string), which
	 *            will cause the code to search for the grid file in the same
	 *            directory in which the model file resides. Bottom line is that
	 *            the default value is appropriate when the grid is stored in
	 *            the same file as the model, or the model file is in the same
	 *            directory as the model file.
	 * @throws IOException
	 */
	public GeoTessModel(String modelInputFile, String relativeGridPath) throws IOException
	{
		this(new File(modelInputFile), relativeGridPath);
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
	 * 
	 * <p>
	 * relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as
	 * the model file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @throws IOException
	 */
	public GeoTessModel(File modelInputFile) throws IOException
	{
		this(modelInputFile, "");
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
	 * 
	 * <p>
	 * relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as
	 * the model file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @throws IOException
	 */
	public GeoTessModel(String modelInputFile) throws IOException
	{
		this(new File(modelInputFile), "");
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified DataInputStream.  The GeoTessGrid will be read directly from
	 * the inputStream as well.
	 * @param inputStream
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public GeoTessModel(DataInputStream inputStream) throws GeoTessException, IOException
	{
		this();
		loadModelBinary(inputStream, null, "*");	
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified Scanner.  The GeoTessGrid will be read directly from
	 * the inputScanner as well.
	 * @param inputScanner
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public GeoTessModel(Scanner inputScanner) throws GeoTessException, IOException
	{
		this();
		loadModelAscii(inputScanner, null, "*");
	}

	/**
	 * Parameterized constructor, specifying the grid and metadata for the
	 * model. The grid is constructed and the data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * </ul>
	 * 
	 * @param gridFileName
	 *            name of file from which to load the grid.
	 * @param metaData
	 *            MetaData the new GeoTessModel instantiates a reference to the
	 *            supplied metaData. No copy is made.
	 * @throws IOException
	 *             if metadata is incomplete.
	 */
	public GeoTessModel(File gridFileName, GeoTessMetaData metaData)
			throws IOException
	{
		this(gridFileName.getCanonicalPath(), metaData);
	}

	/**
	 * Parameterized constructor, specifying the grid and metadata for the
	 * model. The grid is constructed and the data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * </ul>
	 * 
	 * @param gridFileName
	 *            name of file from which to load the grid.
	 * @param metaData
	 *            MetaData the new GeoTessModel instantiates a reference to the
	 *            supplied metaData. No copy is made.
	 * @throws IOException
	 *             if metadata is incomplete.
	 */
	public GeoTessModel(String gridFileName, GeoTessMetaData metaData)
			throws IOException
	{
		this();
		try
		{
			metaData.checkComplete();
			this.metaData = metaData;
			metaData.setModelClassName(this.getClass().getSimpleName());

			if (reuseGridMap == null)
				reuseGridMap = new HashMap<String, GeoTessGrid>();

			// see if grid reuse is on

			if (metaData.isGridReuseOn())
			{
				// grid reuse is on ... see if grid exists in map

				String gridID = GeoTessGrid.getGridID(gridFileName);
				grid = reuseGridMap.get(gridID);
				if (grid == null)
				{
					// not in map ... create and add

					grid = new GeoTessGrid()
							.loadGrid(gridFileName);
					reuseGridMap.put(gridID, grid);
				}
			}
			else
				// reuse is not on ... simply create and continue
				grid = new GeoTessGrid()
				.loadGrid(gridFileName);
			
			grid.incRefCount();

			profiles = new Profile[grid.getNVertices()][metaData.getNLayers()];
		}
		catch (GeoTessException e)
		{
			throw new IOException(e);
		}
	}

	/**
	 * Parameterized constructor, specifying the grid and metadata for the
	 * model. The grid is constructed and the data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * <li>setSoftwareVersion()
	 * <li>setGenerationDate()
	 * </ul>
	 * 
	 * @param grid
	 *            a reference to the GeoTessGrid that will support this
	 *            GeoTessModel.
	 * @param metaData
	 *            MetaData the new GeoTessModel instantiates a reference to the
	 *            supplied metaData. No copy is made.
	 * @throws GeoTessException
	 *             if metadata is incomplete.
	 */
	public GeoTessModel(GeoTessGrid grid, GeoTessMetaData metaData)
			throws GeoTessException
	{
		this();
		metaData.checkComplete();
		this.metaData = metaData;
		metaData.setModelClassName(this.getClass().getSimpleName());

		if (reuseGridMap == null)
			reuseGridMap = new HashMap<String, GeoTessGrid>();

		if (metaData.isGridReuseOn())
		{
			// see if we already have a reference to a grid with the same gridID
			GeoTessGrid savedGrid = reuseGridMap.get(grid.getGridID());

			// if not, save a reference to this grid in the reuseGridMap
			if (savedGrid == null)
				reuseGridMap.put(grid.getGridID(), grid);
			else
				// if we already have a reference to a grid with same gridID
				// we will use the grid we have a reference to instead of the 
				// supplied grid.
				grid = savedGrid;
		}

		this.grid = grid;
		
		this.grid.incRefCount();

		initializeProfiles();
	}

	/**
	 * initialize the Profile[][] array with dimensions
	 * nVertices x nLayers.  All elements will be null.
	 */
	protected void initializeProfiles()
	{
		profiles = new Profile[grid.getNVertices()][metaData.getNLayers()];
	}

	/**
	 * GeoTessModel will attempt to reuse grids that it has already loaded into
	 * memory when a new model tries to reload the same grid. This method clears
	 * the map that supports this functionality.
	 */
	public static void clearReuseGridMap()
	{
		if (reuseGridMap != null)
			reuseGridMap.clear();
	}

	/**
	 * GeoTessModel will attempt to reuse grids that it has already loaded into
	 * memory when a new model tries to reload the same grid. This method
	 * returns the size of the map that supports this functionality.
	 * 
	 * @return size of reuseGridMap.
	 */
	public static int getReuseGridMapSize()
	{
		return reuseGridMap == null ? 0 : reuseGridMap.size();
	}

	/**
	 * Retrieve a reference to the reuseGridMap.
	 * @return a reference to the reuseGridMap.
	 */
	static public HashMap<String, GeoTessGrid> getGridMap()
	{
		if (reuseGridMap == null)
			reuseGridMap = new HashMap<String, GeoTessGrid>();
		return reuseGridMap;
	}

	/**
	 * Retrieve a GeoTessPosition object configured to interpolate data from the
	 * model using linear interpolation in both the geographic and radial
	 * dimensions.
	 * 
	 * @return a GeoTessPosition object configured to interpolate data from the
	 *         model
	 * @throws GeoTessException
	 */
	public GeoTessPosition getGeoTessPosition() throws GeoTessException
	{
		return getGeoTessPosition(InterpolatorType.LINEAR,
				InterpolatorType.LINEAR);
	}

	/**
	 * Retrieve a GeoTessPosition object configured to interpolate data from the
	 * model. If intperpType is InterpolatorType.LINEAR then linear
	 * interpolation will be used in both the geographic and radial dimensions.
	 * In interpType is InterpolatorType.NATURAL_NEIGHBOR, then natural neighbor
	 * interpolation is used in the geographic dimensions and cubic spline
	 * interpolation is used in the radial dimension.
	 * 
	 * @param interpType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @return a GeoTessPosition object configured to interpolate data from the
	 *         model
	 * @throws GeoTessException
	 */
	public GeoTessPosition getGeoTessPosition(InterpolatorType interpType)
			throws GeoTessException
	{
		return getGeoTessPosition(interpType,
				interpType == InterpolatorType.LINEAR ? InterpolatorType.LINEAR
						: InterpolatorType.CUBIC_SPLINE);
	}

	/**
	 * Retrieve a GeoTessPosition object configured to interpolate data from the
	 * model. The type of interpolation in the geographic and radial dimensions
	 * are specified independently.
	 * 
	 * @param horizontalType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType
	 *            either InterpolatorType.LINEAR or
	 *            InterpolatorType.CUBIC_SPLINE
	 * @return a GeoTessPosition object configured to interpolate data from the
	 *         model
	 * @throws GeoTessException
	 */
	public GeoTessPosition getGeoTessPosition(InterpolatorType horizontalType,
			InterpolatorType radialType) throws GeoTessException
	{
		switch (horizontalType)
		{
		case LINEAR:
			return new GeoTessPositionLinear(this, radialType);
		case NATURAL_NEIGHBOR:
			return new GeoTessPositionNatualNeighbor(this, radialType);
		default:
			throw new GeoTessException("\n" + horizontalType
					+ " is not supported.  \n"
					+ "Must specify either InterpolatorType.LINEAR, \n"
					+ "or InterpolatorType.NATURAL_NEIGHBOR\n");
		}
	}

	/**
	 * Retrieve a reference to the 2D grid object.
	 * Note that if grid rotation is active, this will return 
	 * the un-rotated grid.  See getGridRotated() to get the 
	 * rotated grid.
	 * 
	 * @return reference to the 2D grid object.
	 */
	public GeoTessGrid getGrid() { return grid; }

	/**
	 * Retrieve the grid associated with this model.  If grid rotation
	 * is active, this method will rotate a copy of the grid managed by
	 * this model from grid to model coordinates and return the copy
	 * (this can be quite expensive).  
	 * If grid rotation is not active, this method returns a reference
	 * to the existing grid (cheap).
	 * <p>There are possibly two geographic coordinate systems at play:
	 * <ul>
	 * <li>Grid coordinates, where grid vertex 0 points to the north pole.
	 * <li>Model coordinates, where grid vertex 0 points to some other location,
	 * typically a station location.
	 * </ul>
	 * 
	 * @return the grid managed by this model.
	 */
	public GeoTessGrid getGridRotated() { 
		if (metaData.getEulerGridToModel() == null) return this.grid;
		return new GeoTessGrid(this.grid, metaData.getEulerGridToModel());
		}
	
	/**
	 * Retrieve a reference to the MetaData object which stores all manner of
	 * information about the model, including the names of the layers, the names
	 * and units of the attributes, etc.
	 * 
	 * @return a reference to the MetaData.
	 */
	public GeoTessMetaData getMetaData() { return metaData; }

	/**
	 * Retrieve a new GeoTessModel object that is a copy of this. The new model
	 * has a reference to the same GeoTessGrid object as this but deep copies of
	 * metaData and profiles.
	 * 
	 * @return a copy of this.
	 * @throws GeoTessException
	 */
	public GeoTessModel copy() throws GeoTessException
	{
		GeoTessModel copy = new GeoTessModel(grid, metaData.copy());
		Profile[] p, c;
		for (int i = 0; i < profiles.length; ++i)
		{
			p = profiles[i];
			c = copy.profiles[i];
			for (int j = 0; j < p.length; ++j)
				c[j] = p[j].copy();
		}

		if (getPointMap().isPopulated())
			copy.setActiveRegion(getPointMap().getPolygon());

		copy.getPointMap();

		return copy;
	}
	
	/**
	 * Return number of vertices in the 2D geographic grid.
	 * 
	 * @return number of vertices in the 2D geographic grid.
	 */
	public int getNVertices()
	{
		return grid.getNVertices();
	}

	/**
	 * Get the specified vertex in model coordinates.
	 * <p>There are possibly two geographic coordinate systems at play:
	 * <ul>
	 * <li>Grid coordinates, where grid vertex 0 points to the north pole.
	 * <li>Model coordinates, where grid vertex 0 points to some other location,
	 * typically a station location.
	 * </ul> 
	 * @param i the index of the desired grid vertex
	 * @return unit vector of the specified vertex in model coordinates
	 */
	public double[] getVertex(int i)
	{
		if (metaData.getEulerGridToModel() == null)
			return grid.getVertex(i);
		return VectorUnit.eulerRotation(grid.getVertex(i), 
				metaData.getEulerGridToModel());
	}

	/**
	 * Get the unit vector of the vertex that occupies the specified position in
	 * the hierarchy, in model coordinates.
	 * <p>There are possibly two geographic coordinate systems at play:
	 * <ul>
	 * <li>Grid coordinates, where grid vertex 0 points to the north pole.
	 * <li>Model coordinates, where grid vertex 0 points to some other location,
	 * typically a station location.
	 * </ul> 
	 * @param tessId
	 *            tessellation index
	 * @param level
	 *            index of a level relative to the first level of the specified
	 *            tessellation
	 * @param triangle
	 *            the i'th triangle in the specified tessellation/level
	 * @param corner
	 *            the i'th corner of the specified tessellation/level/triangle
	 * @return unit vector of a vertex in model coordinates
	 */
	public double[] getVertex(int tessId, int level, int triangle, int corner)
	{
		if (metaData.getEulerGridToModel() == null)
			return grid.getVertex(tessId, level, triangle, corner);
		return VectorUnit.eulerRotation(grid.getVertex(tessId, level, triangle, corner), 
				metaData.getEulerGridToModel());
	}
	
	/**
	 * Retrieve all of the vertices in model coordinates. 
	 * Vertices consists of an
	 * nVertices x 3 array of doubles. The double[3] array associated with each
	 * vertex is the 3 component unit vector that defines the position of the
	 * vertex.
	 * <p>There are possibly two geographic coordinate systems at play:
	 * <ul>
	 * <li>Grid coordinates, where grid vertex 0 points to the north pole.
	 * <li>Model coordinates, where grid vertex 0 points to some other location,
	 * typically a station location.
	 * </ul> 
	 * @return vertices in model coordinates.
	 */
	public double[][] getVertices()
	{
		if (metaData.getEulerGridToModel() == null)
			return grid.getVertices();
		
		double[][] vertices = new double[getNVertices()][];
		for (int i=0; i<getNVertices(); ++i)
			vertices[i] = VectorUnit.eulerRotation(grid.getVertex(i), 
					metaData.getEulerGridToModel());
		return vertices;
	}

	/**
	 * Retrieve a set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the specified level, in model coordinates.
	 * 
	 * <p>There are possibly two geographic coordinate systems at play:
	 * <ul>
	 * <li>Grid coordinates, where grid vertex 0 points to the north pole.
	 * <li>Model coordinates, where grid vertex 0 points to some other location,
	 * typically a station location.
	 * </ul> 
	 * <p>
	 * @param tessId the tessellation index
	 * @param level index of a level relative to the first level of the 
	 * specified tessellation.
	 * @return the set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the specified level, in model coordinates.
	 */
	public HashSet<double[]> getVertices(int tessId, int level)
	{ 
		if (metaData.getEulerGridToModel() == null)
			return grid.getVertices(tessId, level);
		
		HashSet<double[]> vertices = new HashSet<>();
		for (double[] v : grid.getVertices(tessId, level))
			vertices.add(VectorUnit.eulerRotation(v, 
					metaData.getEulerGridToModel()));
		return vertices;
	}

	/**
	 * Retrieve a set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the specified level. The vertices will
	 * be in model coordinates.
	 * 
	 * <p>There are possibly two geographic coordinate systems at play:
	 * <ul>
	 * <li>Grid coordinates, where grid vertex 0 points to the north pole.
	 * <li>Model coordinates, where grid vertex 0 points to some other location,
	 * typically a station location.
	 * </ul> 
	 * <p>
	 * @param level index of a level relative to all levels of all tessellations
	 * @return the set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the specified level, in model coordinates.
	 */
	public HashSet<double[]> getVertices(int level)
	{
		if (metaData.getEulerGridToModel() == null)
			return grid.getVertices(level);
		
		HashSet<double[]> vertices = new HashSet<>();
		for (double[] v : grid.getVertices(level))
			vertices.add(VectorUnit.eulerRotation(v, 
					metaData.getEulerGridToModel()));
		return vertices;
	}

	/**
	 * Retrieve a set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the top level of the specified 
	 * tessellation, in model coordinates.
	 * 
	 * <p>There are possibly two geographic coordinate systems at play:
	 * <ul>
	 * <li>Grid coordinates, where grid vertex 0 points to the north pole.
	 * <li>Model coordinates, where grid vertex 0 points to some other location,
	 * typically a station location.
	 * </ul> 
	 * <p>
	 * @param tessId tessellation index
	 * @return the set containing the unit vectors of all the vertices that are
	 * connected together by triangles on the top level of the specified 
	 * tessellation, in model coordinates.
	 */
	public HashSet<double[]> getVerticesTopLevel(int tessId)
	{
		if (metaData.getEulerGridToModel() == null)
			return grid.getVerticesTopLevel(tessId);
		
		HashSet<double[]> vertices = new HashSet<>();
		for (double[] v : grid.getVerticesTopLevel(tessId))
			vertices.add(VectorUnit.eulerRotation(v, 
					metaData.getEulerGridToModel()));
		return vertices;
	}

	/**
	 * Return the number of layers that comprise the model.
	 * 
	 * @return the number of layers that comprise the model.
	 */
	public int getNLayers()
	{
		return metaData.getNLayers();
	}

	/**
	 * Return the number of radii that are specified in the Profile at
	 * vertexId, layerId.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @return the number of radii that are specified in profile[vertexId][layerId]
	 */
	public int getNRadii(int vertexId, int layerId) { return profiles[vertexId][layerId].getNRadii(); }

	/**
	 * Return the number of Data objects that are specified in the Profile at
	 * vertexId, layerId
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @return the number of Data that are specified in profile[vertexId][layerId]
	 */
	public int getNData(int vertexId, int layerId) { return profiles[vertexId][layerId].getNData(); }

	/**
	 * Return the number of attributes that associated with each node in the model.
	 */
	public int getNAttributes()
	{
		return metaData.getNAttributes();
	}

	/**
	 * Return the radius in km of the node at vertexId, layerId, nodeId.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @return the radius in km of the node at vertexId, layerId, nodeId.
	 */
	public double getRadius(int vertexId, int layerId, int nodeId)
	{ return profiles[vertexId][layerId].getRadius(nodeId); }

	/**
	 * Return the depth below surface of WGS84 ellipsoid in km of the node
	 * at vertexId, layerId, nodeId.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @return the depth in km of the node at vertexId, layerId, nodeId.
	 */
	public double getDepth(int vertexId, int layerId, int nodeId)
	{ return getEarthShape().getEarthRadius(getVertex(vertexId))
			- profiles[vertexId][layerId].getRadius(nodeId);
	}

	/**
	 * Return the depth below surface of WGS84 ellipsoid in km of the node
	 * at vertexId, layerId, nodeId.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @return the depth in km of the node at vertexId, layerId, nodeId.
	 */
	public double getLayerDepthTop(int vertexId, int layerId)
	{
		return getEarthShape().getEarthRadius(getVertex(vertexId))
				- profiles[vertexId][layerId].getRadiusTop();
	}

	/**
	 * Return the depth below surface of WGS84 ellipsoid in km of the node
	 * at vertexId, layerId, nodeId.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @return the depth in km of the node at vertexId, layerId, nodeId.
	 */
	public double getLayerDepthBottom(int vertexId, int layerId)
	{
		return getEarthShape().getEarthRadius(getVertex(vertexId))
				- profiles[vertexId][layerId].getRadiusBottom();
	}

	/**
	 * Return the value of the attribute at the specified vertexId, layerId, nodeId,
	 * attributeIndex, cast to a double if necessary.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to double if necessary
	 */
	public double getValueDouble(int vertexId, int layerId, int nodeId, int attributeIndex)
	{
		Data data = profiles[vertexId][layerId].getData(nodeId);
		return data == null ? Double.NaN : data.getDouble(attributeIndex);
	}

	/**
	 * Return the value of the attribute at the specified vertexId, layerId, nodeId,
	 * attributeIndex, cast to a float if necessary.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to float if necessary
	 */
	public float getValueFloat(int vertexId, int layerId, int nodeId, int attributeIndex)
	{
		Data data = profiles[vertexId][layerId].getData(nodeId);
		return data == null ? Float.NaN : data.getFloat(attributeIndex);
	}

	/**
	 * Return the value of the attribute at the specified vertexId, layerId, nodeId,
	 * attributeIndex, cast to a LONG_INT if necessary.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to LONG_INT if necessary
	 */
	public long getValueLong(int vertexId, int layerId, int nodeId, int attributeIndex)
	{
		Data data = profiles[vertexId][layerId].getData(nodeId);
		return data == null ? Long.MIN_VALUE : data.getLong(attributeIndex);
	}

	/**
	 * Return the value of the attribute at the specified vertexId, layerId, nodeId,
	 * attributeIndex, cast to a int if necessary.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to int if necessary
	 */
	public int getValueInt(int vertexId, int layerId, int nodeId, int attributeIndex)
	{
		Data data = profiles[vertexId][layerId].getData(nodeId);
		return data == null ? Integer.MIN_VALUE : data.getInt(attributeIndex);
	}

	/**
	 * Return the value of the attribute at the specified vertexId, layerId, nodeId,
	 * attributeIndex, cast to a short if necessary.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to short if necessary
	 */
	public short getValueShort(int vertexId, int layerId, int nodeId, int attributeIndex)
	{
		Data data = profiles[vertexId][layerId].getData(nodeId);
		return data == null ? Short.MIN_VALUE : data.getShort(attributeIndex);
	}

	/**
	 * Return the value of the attribute at the specified vertexId, layerId, nodeId,
	 * attributeIndex, cast to a byte if necessary.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to byte if necessary
	 */
	public byte getValueByte(int vertexId, int layerId, int nodeId, int attributeIndex)
	{
		Data data = profiles[vertexId][layerId].getData(nodeId);
		return data == null ? Byte.MIN_VALUE : data.getByte(attributeIndex);
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int vertexId, int layerId, int nodeId, int attributeIndex, double value)
	{ 
		Data data = profiles[vertexId][layerId].getData(nodeId);
		if (data != null) data.setValue(attributeIndex, value); 
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int vertexId, int layerId, int nodeId, int attributeIndex, float value)
	{ 
		Data data = profiles[vertexId][layerId].getData(nodeId);
		if (data != null) data.setValue(attributeIndex, value); 
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param vertexId the vertex indexc
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int vertexId, int layerId, int nodeId, int attributeIndex, long value)
	{ 
		Data data = profiles[vertexId][layerId].getData(nodeId);
		if (data != null) data.setValue(attributeIndex, value); 
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int vertexId, int layerId, int nodeId, int attributeIndex, int value)
	{ 
		Data data = profiles[vertexId][layerId].getData(nodeId);
		if (data != null) data.setValue(attributeIndex, value); 
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int vertexId, int layerId, int nodeId, int attributeIndex, short value)
	{ 
		Data data = profiles[vertexId][layerId].getData(nodeId);
		if (data != null) data.setValue(attributeIndex, value); 
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param vertexId the vertex index
	 * @param layerId the layer index
	 * @param nodeId the node index
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int vertexId, int layerId, int nodeId, int attributeIndex, byte value)
	{ 
		Data data = profiles[vertexId][layerId].getData(nodeId);
		if (data != null) data.setValue(attributeIndex, value); 
	}

	/**
	 * Return the radius in km of the node at pointIndex.
	 * @param pointIndex
	 * @return the radius in km of the node at pointIndex.
	 */
	public double getRadius(int pointIndex)
	{ return getPointMap().getPointRadius(pointIndex); }

	/**
	 * Return the depth below surface of WGS84 ellipsoid in km of the node
	 * at pointIndex.
	 * @param pointIndex
	 * @return the depth in km of the node at pointIndex.
	 */
	public double getDepth(int pointIndex)
	{ return getPointMap().getPointDepth(pointIndex); }

	/**
	 * Return the value of the attribute at the specified pointIndex,
	 * attributeIndex, cast to a double if necessary.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to double if necessary
	 */
	public double getValueDouble(int pointIndex, int attributeIndex)
	{
		int[] i = getPointMap().getPointIndices(pointIndex);
		return getValueDouble(i[0], i[1], i[2], attributeIndex);
	}

	/**
	 * Return the value of the attribute at the specified pointIndex,
	 * attributeIndex, cast to a float if necessary.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to float if necessary
	 */
	public float getValueFloat(int pointIndex, int attributeIndex)
	{
		int[] i = getPointMap().getPointIndices(pointIndex);
		return getValueFloat(i[0], i[1], i[2], attributeIndex);
	}

	/**
	 * Return the value of the attribute at the specified pointIndex,
	 * attributeIndex, cast to a LONG_INT if necessary.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to LONG_INT if necessary
	 */
	public long getValueLong(int pointIndex, int attributeIndex)
	{
		int[] i = getPointMap().getPointIndices(pointIndex);
		return getValueLong(i[0], i[1], i[2], attributeIndex);
	}

	/**
	 * Return the value of the attribute at the specified pointIndex,
	 * attributeIndex, cast to a int if necessary.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to int if necessary
	 */
	public int getValueInt(int pointIndex, int attributeIndex)
	{
		int[] i = getPointMap().getPointIndices(pointIndex);
		return getValueInt(i[0], i[1], i[2], attributeIndex);
	}

	/**
	 * Return the value of the attribute at the specified pointIndex,
	 * attributeIndex, cast to a short if necessary.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to short if necessary
	 */
	public short getValueShort(int pointIndex, int attributeIndex)
	{
		int[] i = getPointMap().getPointIndices(pointIndex);
		return getValueShort(i[0], i[1], i[2], attributeIndex);
	}

	/**
	 * Return the value of the attribute at the specified pointIndex,
	 * attributeIndex, cast to a byte if necessary.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @return the value of the specifed attribute, cast to byte if necessary
	 */
	public byte getValueByte(int pointIndex, int attributeIndex)
	{
		int[] i = getPointMap().getPointIndices(pointIndex);
		return getValueByte(i[0], i[1], i[2], attributeIndex);
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int pointIndex, int attributeIndex, double value)
	{ 
		int[] i = getPointMap().getPointIndices(pointIndex);
		setValue(i[0], i[1], i[2], attributeIndex, value);
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int pointIndex, int attributeIndex, float value)
	{ 
		int[] i = getPointMap().getPointIndices(pointIndex);
		setValue(i[0], i[1], i[2], attributeIndex, value);
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int pointIndex, int attributeIndex, long value)
	{ 
		int[] i = getPointMap().getPointIndices(pointIndex);
		setValue(i[0], i[1], i[2], attributeIndex, value);
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int pointIndex, int attributeIndex, int value)
	{ 
		int[] i = getPointMap().getPointIndices(pointIndex);
		setValue(i[0], i[1], i[2], attributeIndex, value);
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int pointIndex, int attributeIndex, short value)
	{ 
		int[] i = getPointMap().getPointIndices(pointIndex);
		setValue(i[0], i[1], i[2], attributeIndex, value);
	}

	/**
	 * Modify the attribute value stored at the specified vertex, layer, node, attribute.
	 * @param pointIndex
	 * @param attributeIndex the attributeIndex
	 * @param value the new attribute value (must be of type double, float, long,
	 * int, short or byte).
	 */
	public void setValue(int pointIndex, int attributeIndex, byte value)
	{ 
		int[] i = getPointMap().getPointIndices(pointIndex);
		setValue(i[0], i[1], i[2], attributeIndex, value);
	}

	/**
	 * Find the set of all vertex indices that are connected by the tessellation
	 * that supports the specified layer.
	 * 
	 * @param layerIndex
	 * @return the indices of all the vertices that are connected together by
	 *         triangles that support the specified layer.
	 */
	public HashSetInteger getConnectedVertices(int layerIndex)
	{
		return grid.getVertexIndicesTopLevel(metaData
				.getTessellation(layerIndex));
	}

	/**
	 * Retrieve the number of points in the model, including all nodes along all
	 * profiles in all layers, at all grid vertices. There is a node for each
	 * Data object, not each radius.
	 * Number of pointMap nodes in each type of Profile:
	 * <ul>
	 * <li>empty: 0
	 * <li>thin:1
	 * <li>constant: 1
	 * <li>npoint: n
	 * <li>surface: 1
	 * </ul>
	 * 
	 * @return the number of points in the model.
	 */
	public int getNPoints()
	{
		return getPointMap().size();
	}

	/**
	 * Return true if this and other are equal, i.e., their grids have the same
	 * gridIDs, they have the same number of layers, all their Profiles are
	 * equal. For Profiles to be equal, they must be of the same type and size,
	 * all their radii must be equal and all their data must be equal.
	 * 
	 * @param other
	 * @return true if this and other are equal
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof GeoTessModel))
			return false;

		if (!metaData.equals(((GeoTessModel) other).metaData))
			return false;

		if (!grid.equals(((GeoTessModel) other).grid))
			return false;

		for (int i = 0; i < profiles.length; ++i)
			for (int j = 0; j < profiles[i].length; ++j)
				if (!profiles[i][j].equals(((GeoTessModel) other).getProfile(i, j)))
				{
					System.out.println(profiles[i][j]);
					System.out.println(((GeoTessModel) other).getProfile(i, j));
					System.out.println();
					return false;
				}
		return true;
	}

	/**
	 * Defines, or resets, the attribute and data definitions for this model. The
	 * metadata attributes are reset to the input values and all data are filled
	 * with the input fillValue on exit.
	 * 
	 * @param attributeNames The new attribute names.
	 * @param attributeUnits The new attribute units.
	 * @param fillValue      The new attribute data value assigned to all grid
	 * 											 nodes.
	 * @throws GeoTessException
	 */
	public void initializeAttributeData(String attributeNames,
			String attributeUnits,
			Number fillValue) throws GeoTessException
	{
		initializeAttributeData(attributeNames.split(";"), attributeUnits.split(";"),
				fillValue);
	}

	/**
	 * Defines, or resets, the attribute and data definitions for this model. The
	 * metadata attributes are reset to the input values and all data are filled
	 * with the input fillValue on exit.
	 * 
	 * @param attributeNames The new attribute names.
	 * @param attributeUnits The new attribute units.
	 * @param fillValue      The new attribute data value assigned to all grid
	 * 											 nodes.
	 * @throws GeoTessException
	 */
	public void initializeAttributeData(String[] attributeNames,
			String[] attributeUnits,
			Number fillValue) throws GeoTessException
	{
		if (profiles == null)
			throw new GeoTessException(
					"\nAttempting to initialize model data before \n"
							+ "before Profiles have been specified.\n");

		// get the new data type

		DataType dataType = DataType.getDataType(fillValue);

		// set the attributes and data type

		metaData.setAttributes(attributeNames, attributeUnits);
		metaData.setDataType(dataType);

		// create a data object with the fill value

		Data data = Data.getData(dataType, attributeNames.length);
		data.fill(fillValue);

		// loop over all profiles and all data objects within the profiles and reset
		// their data to a copy of the the data object initialized with the fill
		// value.

		for (int v = 0; v < getNVertices(); ++v)
		{
			Profile[] pv = profiles[v];
			for (int lid = 0; lid < getNLayers(); ++lid)
			{
				Profile p = pv[lid];
				for (int n = 0; n < p.getNData(); ++n)
					p.setData(n, data.copy());
			}
		}
	}

	/**
	 * Reset all the Data objects in the model. The number of attributes in the
	 * new Data objects will equal the number of elements in the supplied lists
	 * of attributeNames and attributeUnits (which must be equal in length).
	 * <p>
	 * The DataType of the new Data objects will be the type of the supplied
	 * fillValue. Data values will be copied from the old to the new Data
	 * objects with casting to the type of the new data objects. It is the
	 * responsibility of the caller to ensure that casting of data values does
	 * not result in corruption of data values or the loss of important
	 * information.
	 * <p>
	 * If the number of attributes in the new Data objects is less than the
	 * number in the old Data objects, old data values are lost. If the number
	 * of attributes in the new Data objects is greater than the number in the
	 * old Data objects, new values are populated with fillValue.
	 * <p>
	 * This method can only modify the Data objects associated with existing
	 * Profiles. It does not instantiate Profiles, change the ProfileType of
	 * existing profiles, modify the number of nodes in existing Profiles, or
	 * modify the radii within a Profile. To do any of those things requires
	 * replacing the Profile.
	 * 
	 * @param attributeNames
	 *            new names of the attributes. These names will replace the old
	 *            names in the MetaData object.
	 * @param attributeUnits
	 *            new units of the attributes. These units will replace the old
	 *            units in the MetaData object. The number of attributeNames and
	 *            attributeUntis must be the same.
	 * @param fillValue
	 *            a value of type Double, Float, Long, Int, Short or Byte. All
	 *            data values in this models' Data objects will be of this type
	 *            after this method executes. The supplied value is used to
	 *            populate data values that are not copied from the old Data
	 *            object.
	 * @throws GeoTessException
	 *             if the model has not been populated with Profiles or if the
	 *             number of attributeNames and attributeUnits is not the same.
	 */
	public void initializeData(String[] attributeNames,
			String[] attributeUnits, Number fillValue) throws GeoTessException
	{
		if (profiles == null)
			throw new GeoTessException(
					"\nAttempting to initialize model data before \n"
							+ "before Profiles have been specified.\n");

		DataType oldDataType = metaData.getDataType();
		DataType newDataType = DataType.getDataType(fillValue);

		int nAttributesOld = metaData.getNAttributes();
		int nAttributesNew = attributeNames.length;

		metaData.setAttributes(attributeNames, attributeUnits);
		metaData.setDataType(newDataType);

		// if neither the dataType or the number of attributes has changed,
		// there is no need to
		// copy the data. The only thing that happens is that the attributeNames
		// or attributeUnits might change.
		//@formatter:off
		if (newDataType != oldDataType || nAttributesNew != nAttributesOld)
		{
			Profile[] pp;
			Profile p;
			Data oldData, newData;
			Data[] data;
			for (int v = 0; v < getNVertices(); ++v)
			{
				pp = profiles[v];
				for (int lid = 0; lid < getNLayers(); ++lid)
				{
					p = pp[lid];
					data = new Data[p.getNData()];
					for (int n = 0; n < p.getNData(); ++n)
					{
						oldData = p.getData(n);
						data[n] = newData = Data.getData(newDataType,
								nAttributesNew);
						switch (newDataType)
						{
						case DOUBLE:
							for (int a = 0; a < nAttributesNew; ++a)
								newData.setValue(a, 
										a < nAttributesOld 
										? oldData.getDouble(a) 
												: fillValue.doubleValue());
							break;
						case FLOAT:
							for (int a = 0; a < nAttributesNew; ++a)
								newData.setValue( a,
										a < nAttributesOld 
										? oldData.getFloat(a) 
												: fillValue.floatValue());
							break;
						case LONG:
							for (int a = 0; a < nAttributesNew; ++a)
								newData.setValue(a,
										a < nAttributesOld 
										? oldData.getLong(a) 
												: fillValue.longValue());
							break;
						case INT:
							for (int a = 0; a < nAttributesNew; ++a)
								newData.setValue(a,
										a < nAttributesOld 
										? oldData.getInt(a) 
												: fillValue.intValue());
							break;
						case SHORT:
							for (int a = 0; a < nAttributesNew; ++a)
								newData.setValue(a,
										a < nAttributesOld 
										? oldData.getShort(a) 
												: fillValue.shortValue());
							break;
						case BYTE:
							for (int a = 0; a < nAttributesNew; ++a)
								newData.setValue(a,
										a < nAttributesOld 
										? oldData.getByte(a) 
												: fillValue.byteValue());
							break;
						default:
							newData = null;
							break;
						}

					}
					p.setData(data);
				}
			}
		}
		//@formatter:on
	}

	/**
	 * Retrieve a reference to the current pointMap.  If the current pointMap
	 * has already been specified using any of the setPointMap() methods, then
	 * a reference to the existing PointMap is returned.  If a PointMap
	 * has not been previously requested (isPointMapPopulated() returns false) 
	 * then a  new one is instantiated that includes every point in the entire model.
	 * 
	 * PointMap is an nPoints by 3 array of
	 * indexes. For each point in the 3D grid, pointMap stores 3 indexes: the
	 * vertex index, the layer index, and the node index.
	 * 
	 * <p>
	 * The term 'vertex' refers to a position in the 2D tessellation. A vertex
	 * is a 2D point represented by unit vectors on a unit sphere. The term
	 * 'node' refers to a Data object on a radial profile associated with a
	 * vertex and a layer in the model. Node indexes are unique only within a
	 * given profile (all profiles have a node with index 0 for example). The
	 * term 'point' refers to all the nodes in all the profiles of the model.
	 * There is only one 'point' in the model with index 0. PointMap is
	 * introduced to help map back and forth between all these different
	 * indexes.
	 * Number of pointMap nodes in each type of Profile:
	 * <ul>
	 * <li>empty: 0
	 * <li>thin:1
	 * <li>constant: 1
	 * <li>npoint: n
	 * <li>surface: 1
	 * </ul>
	 * 
	 * @return a reference to the pointMap
	 */
	public PointMap getPointMap()
	{
		if (pointMap == null || !pointMap.isPopulated())
			synchronized(this)
			{
				if (pointMap == null) 
					pointMap = new PointMap(this);
				if (!pointMap.isPopulated())
					pointMap.setActiveRegion();
			}
		return pointMap;
	}

	/**
	 * Query whether or not the PointMap is currently populated
	 * 
	 * @return true if the pointMap currently populated.
	 */
	public boolean isPointMapPopulated()
	{
		if (pointMap == null)
			getPointMap();
		return pointMap.isPopulated();
	}

	/**
	 * Set the active region such that it encompasses all the nodes in the
	 * model.
	 */
	public void setActiveRegion()
	{
		synchronized(this)
		{
			if (pointMap == null)
				getPointMap();
			else
				pointMap.setActiveRegion();
		}
	}

	/**
	 * Set the active region to encompass only the nodes contained within the
	 * specified Polygon or Polygon3D object.
	 * 
	 * @param polygon
	 *            a Polygon or Polygon3D object.
	 */
	public void setActiveRegion(Polygon polygon)
	{
		synchronized(this)
		{
			if (pointMap == null) 
				pointMap = new PointMap(this);
			pointMap.setActiveRegion(polygon);
		}
	}

	/**
	 * Set the active region to encompass only the nodes contained within the
	 * specified Polygon or Polygon3D object.
	 * 
	 * @param polygonFile
	 * @throws IOException
	 */
	public void setActiveRegion(String polygonFile) throws Exception
	{
		setActiveRegion(new File(polygonFile));
	}

	/**
	 * Set the active region to encompass only the nodes contained within the
	 * specified Polygon or Polygon3D object.
	 * 
	 * @param polygonFile
	 * @throws IOException
	 */
	public void setActiveRegion(File polygonFile) throws Exception
	{
		synchronized(this)
		{
			if (pointMap == null) 
				pointMap = new PointMap(this);
			pointMap.setActiveRegion(polygonFile);
		}
	}

	/**
	 * Returns the active region polygon file if one was set and it was set by a
	 * polygon read from a file. Otherwise, null is returned.
	 * 
	 * @return The active region polygon file if one was set.
	 */
	public File getActiveRegionPolygonFile()
	{
		if (pointMap.getPolygon() == null)
			return null;
		else
			return new File(pointMap.getPolygon().getPolygonFile());
	}

	/**
	 * Find the pointIndex of the point in this PointMap that is closest to the 
	 * supplied location.  Will return -1 if the closest [vertex, layerIndex, nodeIndex]
	 * is not in the current pointMap.
	 * @param location
	 * @param radius 
	 * @param layerIndex
	 * @return the pointIndex of the point in this PointMap that is closest to the 
	 * supplied location. Will return -1 if the closest [vertex, layerIndex, nodeIndex]
	 * is not in the current pointMap.
	 * @throws GeoTessException 
	 */
	public int getClosestPoint(double[] location, double radius, int layerIndex) throws GeoTessException
	{
		GeoTessPosition pos = getGeoTessPosition(InterpolatorType.LINEAR, InterpolatorType.LINEAR);
		pos.set(layerIndex, location, radius);
		Map<Integer, Double> weights = new HashMap<Integer, Double>();
		pos.getWeights(weights, 1.);
		int closestPoint = -1;
		double maxWeight = -1;
		for (Map.Entry<Integer, Double> entry : weights.entrySet())
			if (entry.getValue() > maxWeight)
			{
				maxWeight = entry.getValue();
				closestPoint = entry.getKey();
			}
		return closestPoint;
	}

	/**
	 * Find the [vertex, layer, node] of the point in this model that is closest to the 
	 * supplied location.  
	 * @param location
	 * @param radius 
	 * @param layerIndex
	 * @return int[3] containing the [vertex, layer, node] of the point in this PointMap 
	 * that is closest to the supplied location. 
	 */
	public int[] getClosestNode(double[] location, double radius, int layerIndex)
	{
		int[] map = new int[3];

		int triangle = grid.getTriangle(grid.getFirstTriangle(metaData.getTessellation(layerIndex), 0), location);
		double distance, minDistance = Double.POSITIVE_INFINITY;
		Profile p;
		for (int vertex : grid.getTriangles()[triangle])
		{
			p = profiles[vertex][layerIndex];
			int node = p.findClosestRadiusIndex(radius);

			distance = GeoTessUtils.getDistance3D(location, radius, getVertex(vertex), p.getRadius(node));
			if (distance < minDistance)
			{
				minDistance = distance;
				map[0] = vertex;
				map[1] = layerIndex;
				map[2] = node;
			}
		}
		return map;
	}

	/**
	 * Compute the weights on each model point that results from interpolating
	 * positions along the specified ray path. The following procedure is
	 * implemented:
	 * <ol>
	 * <li>find the midpoint of each increment of the path (line segment between
	 * adjacent positions on the path).
	 * <li>find the interpolation coefficients of all the model points that are
	 * 'touched' by the midpoint of the increment. The midpoint of path increment i
	 * is constrained to reside in layer[layerIds[i]].
	 * <li>find the length of the path increment in km.
	 * <li>find the product of the length of the path increment times each
	 * interpolation coefficient and sum that value for each model point.
	 * </ol>
	 * 
	 * @param rayPath an ordered list of unit vectors that define a ray path.
	 * @param radii the radii of the points along the ray path
	 * @param layerIds input array of layer indices that specifies the layer in which
	 * increment i resides where increment i is the path increment between points i 
	 * and i+1.  If layerIds is null or layerIds[i] is &lt; 0, then the layer index will
	 * be determined based on the radius of the midpoint of the i'th path increment.
	 * @param horizontalType InterpolatorType.LINEAR or InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType InterpolatorType.LINEAR or InterpolatorType.CUBIC_SPLINE
	 * @param weights a map from the pointIndex of a point in the model to the weight
	 *         that accrued to that point from the ray path. The sum of all the
	 *         weights in the map will equal the length of the ray path in km.
	 *         The input map is NOT cleared before population with new values.
	 * @return true if all of the points touched by the rayPath are active.  If any
	 * of the points are inactive, then weights will contain a key == -1. 
	 * @throws GeoTessException
	 */
	public boolean getWeights(ArrayList<double[]> rayPath,
			double[] radii, int[] layerIds,
			InterpolatorType horizontalType,
			InterpolatorType radialType,
			Map<Integer, Double> weights ) throws GeoTessException
	{
		//weights.clear();

		GeoTessPosition pos = getGeoTessPosition(horizontalType, radialType);

		double[] v1, v2, v = new double[3];;
		double dist, r1, r2;
		int layer;

		for (int i = 1; i < rayPath.size(); ++i)
		{
		    v1 = rayPath.get(i-1);
		    v2 = rayPath.get(i);
		    r1 = radii[i-1];
		    r2 = radii[i];
		    dist = GeoTessUtils.getDistance3D(v1,r1,v2,r2);
		    if (dist > 1e-4)
		    {
			v[0] = v1[0]+v2[0];
			v[1] = v1[1]+v2[1];
			v[2] = v1[2]+v2[2];
			GeoTessUtils.normalize(v);
			layer = (layerIds == null || layerIds[i] != layerIds[i]) ? -1 : layerIds[i-1];
			pos.set(layer, v, (r1+r2) / 2.);
			pos.getWeights(weights, dist);
		    }
		}
		return !weights.containsKey(Integer.valueOf(-1));
	}

	/**
	 * Compute the weights on each model point that results from interpolating
	 * positions along the specified ray path. The following procedure is
	 * implemented:
	 * <ol>
	 * <li>find the midpoint of each increment of the path (line segment between
	 * adjacent positions on the path).
	 * <li>find the interpolation coefficients of all the model points that are
	 * 'touched' by the midpoint of the increment. The midpoint of path increment i
	 * is constrained to reside in layer[layerIds[i]].
	 * <li>find the length of the path increment in km.
	 * <li>find the product of the length of the path increment times each
	 * interpolation coefficient and sum that value for each model point.
	 * </ol>
	 * 
	 * <p>For sample code that iterates over the entries of the weights map,
	 * see <code>getPathIntegral(int attribute, HashMapIntegerDouble weights);</code>
	 * 
	 * @param vectors an ordered list of unit vectors that define a ray path.
	 * @param radii the radii of the points along the ray path
	 * @param layerIds input array of layer indices that specifies the layer in which
	 * increment i resides where increment i is the path increment between points i 
	 * and i+1.  If layerIds[i] is &lt; 0, then the layer index will be determined based
	 * on the radius of the midpoint of the i'th path increment.
	 * @param horizontalType InterpolatorType.LINEAR or InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType InterpolatorType.LINEAR or InterpolatorType.CUBIC_SPLINE
	 * @param weights a map from the pointIndex of a point in the model to the weight
	 *         that accrued to that point from the ray path. The sum of all the
	 *         weights in the map will equal the length of the ray path in km.
	 *         The input map is NOT cleared before population with new values.
	 * @return true if all of the points touched by the rayPath are active.  If any
	 * of the points are inactive, then weights will contain a key == -1. 
	 * @throws GeoTessException
	 * @deprecated instead use getWeights(ArrayList<double[]> vectors,
			ArrayListDouble radii, ArrayListInt layerIds,
			InterpolatorType horizontalType,
			InterpolatorType radialType,
			HashMapIntegerDouble weights)
	 */
	public boolean getWeights(ArrayList<double[]> vectors,
			ArrayListDouble radii, int[] layerIds,
			InterpolatorType horizontalType,
			InterpolatorType radialType,
			HashMapIntegerDouble weights) throws GeoTessException {
		return getWeights(vectors, radii, new ArrayListInt(layerIds), horizontalType, radialType, weights);
	}

	/**
	 * Compute the weights on each model point that results from interpolating
	 * positions along the specified ray path. The following procedure is
	 * implemented:
	 * <ol>
	 * <li>find the midpoint of each increment of the path (line segment between
	 * adjacent positions on the path).
	 * <li>find the interpolation coefficients of all the model points that are
	 * 'touched' by the midpoint of the increment. The midpoint of path increment i
	 * is constrained to reside in layer[layerIds[i]].
	 * <li>find the length of the path increment in km.
	 * <li>find the product of the length of the path increment times each
	 * interpolation coefficient and sum that value for each model point.
	 * </ol>
	 * 
	 * <p>For sample code that iterates over the entries of the weights map,
	 * see <code>getPathIntegral(int attribute, HashMapIntegerDouble weights);</code>
	 * 
	 * @param vectors an ordered list of unit vectors that define a ray path.
	 * @param radii the radii of the points along the ray path
	 * @param layerIds input array of layer indices that specifies the layer in which
	 * increment i resides where increment i is the path increment between points i 
	 * and i+1.  If layerIds[i] is &lt; 0, then the layer index will be determined based
	 * on the radius of the midpoint of the i'th path increment.
	 * @param horizontalType InterpolatorType.LINEAR or InterpolatorType.NATURAL_NEIGHBOR
	 * @param radialType InterpolatorType.LINEAR or InterpolatorType.CUBIC_SPLINE
	 * @param weights a map from the pointIndex of a point in the model to the weight
	 *         that accrued to that point from the ray path. The sum of all the
	 *         weights in the map will equal the length of the ray path in km.
	 *         The input map is NOT cleared before population with new values.
	 * @return true if all of the points touched by the rayPath are active.  If any
	 * of the points are inactive, then weights will contain a key == -1. 
	 * @throws GeoTessException
	 */
	public boolean getWeights(ArrayList<double[]> vectors,
			ArrayListDouble radii, ArrayListInt layerIds,
			InterpolatorType horizontalType,
			InterpolatorType radialType,
			HashMapIntegerDouble weights) throws GeoTessException
	{
		// the code here is identical to another method with the same name but this method
		// populates a HashMapIntegerDouble instead of a HashMap<Integer, Double>.
		//weights.clear();

		GeoTessPosition pos = getGeoTessPosition(horizontalType, radialType);

		double[] v1, v2, v = new double[3];
		double dist, r1, r2;
		int layer;

		for (int i = 1; i < vectors.size(); ++i)
		{
		    v1 = vectors.get(i-1);
		    v2 = vectors.get(i);
		    r1 = radii.get(i-1);
		    r2 = radii.get(i);
		    dist = GeoTessUtils.getDistance3D(v1,r1,v2,r2);
		    if (dist > 1e-4)
		    {
			v[0] = v1[0]+v2[0];
			v[1] = v1[1]+v2[1];
			v[2] = v1[2]+v2[2];
			GeoTessUtils.normalize(v);
			layer = (layerIds == null || layerIds.get(i-1) != layerIds.get(i)) ? -1 : layerIds.get(i-1);
			pos.set(layer, v, (r1+r2) / 2.);
			pos.getWeights(weights, dist);
		    }
		}
		return !weights.contains(-1);
	}

	/**
	 * Compute the weights on each model point that results from interpolating
	 * positions along the specified great circle ray path. 
	 * 
	 * <p>This method only applies to 2D GeoTessModels.
	 * 
	 * <p>To obtain a GreatCircle object from pointA to pointB (both unit vectors), call
	 * <code>new GreatCircle(pointA, pointB)</code>.  More complex GreatCircle constructors
	 * are available for special situations.
	 * 
	 * <p>The following procedure is implemented:
	 * <ol>
	 * <li>find the midpoint of each increment of the path (line segment between
	 * adjacent positions on the path).
	 * <li>find the interpolation coefficients of all the model points that are
	 * 'touched' by the midpoint of the increment. The midpoint of path increment i
	 * is constrained to reside in layer[layerIds[i]].
	 * <li>find the length of the path increment in km.
	 * <li>find the product of the length of the path increment times each
	 * interpolation coefficient and sum that value for each model point.
	 * </ol>
	 * 
	 * @param greatCircle a GreatCircle object that defines the desired rayPath.
	 * @param pointSpacing maximum point separation in radians.  The actual point spacing
	 * will generally be slightly less than the specified value so that there will be an 
	 * integral number of uniformly spaced points along the path.
	 * @param earthRadius the radius of the earth in km.  If specified value is &lt;= 0 then
	 * earthRadius is calculated to be the local radius of the WGS84 ellipsoid.
	 * @param horizontalType either InterpolatorType.NATURAL_NEIGHBOR or InterpolatorType.LINEAR.
	 * @param weights a map from the pointIndex of a point in the model to the weight
	 *         that accrued to that point from the ray path. The sum of all the
	 *         weights in the map will equal the length of the ray path in km.
	 *         The input map is NOT cleared before population with new values.
	 * @return true if all of the points touched by the rayPath are active.  If any
	 * of the points are inactive, then weights will contain a key == -1. 
	 * @throws GeoTessException if this model is not a 2D model
	 */
	public boolean getWeights(GreatCircle greatCircle, double pointSpacing,
			double earthRadius, InterpolatorType horizontalType,
			Map<Integer, Double> weights) throws GeoTessException
	{
		// the code here is identical to another method with the same name but this method
		// populates a HashMap<Integer, Double> instead of a HashMapIntegerDouble.
		if (!is2D())
			throw new GeoTessException("\nCan only apply this method to 2D models.\n");

		//weights.clear();

		int nIntervals = (int) Math.ceil(greatCircle.getDistance()/pointSpacing);

		if (nIntervals > 0) 
		{
			GeoTessPosition pos = getGeoTessPosition(horizontalType, InterpolatorType.LINEAR);

			double r, delta = greatCircle.getDistance()/nIntervals;
			double[] u = new double[3];
			for (int i = 0; i < nIntervals; ++i)
			{
				greatCircle.getPoint((i+0.5)*delta, u);
				r = earthRadius > 0 ? earthRadius : getEarthShape().getEarthRadius(u);
				pos.set(0, u, r);
				pos.getWeights(weights, delta*r);
			}
		}
		return !weights.containsKey(Integer.valueOf(-1));
	}

	/**
	 * Compute the weights on each model point that results from interpolating
	 * positions along the specified great circle ray path. 
	 * 
	 * <p>This method only applies to 2D GeoTessModels.
	 * 
	 * <p>To obtain a GreatCircle object from pointA to pointB (both unit vectors), call
	 * <code>new GreatCircle(pointA, pointB)</code>.  More complex GreatCircle constructors
	 * are available for special situations.
	 * 
	 * <p>The following procedure is implemented:
	 * <ol>
	 * <li>find the midpoint of each increment of the path (line segment between
	 * adjacent positions on the path).
	 * <li>find the interpolation coefficients of all the model points that are
	 * 'touched' by the midpoint of the increment. The midpoint of path increment i
	 * is constrained to reside in layer[layerIds[i]].
	 * <li>find the length of the path increment in km.
	 * <li>find the product of the length of the path increment times each
	 * interpolation coefficient and sum that value for each model point.
	 * </ol>
	 * 
	 * <p>For sample code that iterates over the entries of the weights map,
	 * see <code>getPathIntegral(int attribute, HashMapIntegerDouble weights);</code>
	 * 
	 * @param greatCircle a GreatCircle object that defines the desired rayPath.
	 * @param pointSpacing maximum point separation in radians.  The actual point spacing
	 * will generally be slightly less than the specified value so that there will be an 
	 * integral number of uniformly spaced points along the path.
	 * @param earthRadius the radius of the earth in km.  If specified value is &lt;= 0 then
	 * earthRadius is calculated to be the local radius of the WGS84 ellipsoid.
	 * @param horizontalType either InterpolatorType.NATURAL_NEIGHBOR or InterpolatorType.LINEAR.
	 * @param weights a map from the pointIndex of a point in the model to the weight
	 *         that accrued to that point from the ray path. The sum of all the
	 *         weights in the map will equal the length of the ray path in km.
	 *         The input map is NOT cleared before population with new values.
	 * @return true if all of the points touched by the rayPath are active.  If any
	 * of the points are inactive, then weights will contain a key == -1. 
	 * @throws GeoTessException if this model is not a 2D model
	 */
	public boolean getWeights(GreatCircle greatCircle, double pointSpacing,
			double earthRadius, InterpolatorType horizontalType,
			HashMapIntegerDouble weights) throws GeoTessException
	{
		// the code here is identical to another method with the same name but this method
		// populates a HashMapIntegerDouble instead of a HashMap<Integer, Double>.
		if (!is2D())
			throw new GeoTessException("\nCan only apply this method to 2D models.\n");

		//weights.clear();

		int nIntervals = (int) Math.ceil(greatCircle.getDistance()/pointSpacing);

		if (nIntervals > 0) 
		{
			GeoTessPosition pos = getGeoTessPosition(horizontalType, InterpolatorType.LINEAR);

			double r, delta = greatCircle.getDistance()/nIntervals;
			double[] u = new double[3];
			for (int i = 0; i < nIntervals; ++i)
			{
				greatCircle.getPoint((i+0.5)*delta, u);
				r = earthRadius > 0 ? earthRadius : getEarthShape().getEarthRadius(u);
				pos.set(0, u, r);
				pos.getWeights(weights, delta*r);
			}
		}
		return !weights.contains(-1);
	}

	/**
	 * Compute the path integral of the specified attribute along the specified rayPath.
	 * The integral is equal to the sum of the weights times the attribute value evaluated
	 * at the corresponding point index.
	 *
	 * @param attribute the index of the attribute that is to be integrated.  If a value
	 * less than zero is specified then only the length of the path increments is summed
	 * and the function returns the total length of the rayPath in km.
	 * @param weights a map from the pointIndex of a point in the model to the weight
	 *         that accrued to that point from the ray path. The sum of all the
	 *         weights in the map will equal the length of the ray path in km.
	 * @return  the path integral of the specified attribute along the specified rayPath.
	 */
	public double getPathIntegral(int attribute, HashMapIntegerDouble weights) 
	{
		double integral = 0;
		HashMapIntegerDouble.Iterator it = weights.iterator();
		HashMapIntegerDouble.Entry e;
		if (attribute < 0)
			while (it.hasNext())
				integral += it.next();
		else
			while (it.hasNext())
			{
				e = it.nextEntry();
				if (e.getKey() >= 0)
					integral += e.getValue() * pointMap.getPointValueDouble(e.getKey(), attribute);
			}

		return integral;
	}

	/**
	 * Compute the path integral of the specified attribute along the specified rayPath.
	 * The integral is equal to the sum of the weights times the attribute value evaluated
	 * at the corresponding point index.
	 *
	 * @param attribute the index of the attribute that is to be integrated.  If a value
	 * less than zero is specified then only the length of the path increments is summed
	 * and the function returns the total length of the rayPath in km.
	 * @param weights a map from the pointIndex of a point in the model to the weight
	 *         that accrued to that point from the ray path. The sum of all the
	 *         weights in the map will equal the length of the ray path in km.
	 * @return  the path integral of the specified attribute along the specified rayPath.
	 */
	public double getPathIntegral(int attribute, Map<Integer, Double> weights) 
	{
		double integral = 0;
		if (attribute < 0)
			for (Double weight : weights.values())
				integral += weight.doubleValue();
		else
			for (Entry<Integer, Double> e : weights.entrySet())
				if (e.getKey() >= 0)
					integral += e.getValue() * pointMap.getPointValueDouble(e.getKey(), attribute);
		return integral;
	}

	/**
	 * Compute the path integral of the specified attribute along the specified rayPath.
	 * <p>The following procedure is implemented:
	 * <ol>
	 * <li>find the midpoint of each increment of the path (line segment between
	 * adjacent positions on the path).
	 * <li>find the straight line distance between the two points, in km.
	 * <li>calculate the interpolated value of the specified attribute at the
	 * center of the path increment, constrained to reside in layer layerIds[i]
	 * <li>sum the length of the path increment times the attribute value, along the path.
	 * </ol>
	 *
	 * @param attribute the index of the attribute that is to be integrated.  If a value
	 * less than zero is specified then only the length of the path increments is summed
	 * and the function returns the total length of the rayPath in km.
	 * @param rayPath input array of 3-component unit vectors that contains points
	 * that define the ray path.
	 * @param radii input array of radius values, in km, that define
	 * the radius of each unit_vector supplied in 'rayPath'.
	 * @param layerIds input array of layer indices that specifies the layer in which
	 * increment i resides where increment i is the path increment between points i 
	 * and i+1.  If layerIds is null or layerIds[i] is &lt; 0, then the layer index will
	 * be determined based on the radius of the midpoint of the i'th path increment.
	 * @param horizontalType (input) the type of interpolator to use in the geographic
	 * dimensions, either LINEAR or NATURAL_NEIGHBOR
	 * @param radialType (input) the type of interpolator to use in the radial
	 * dimension, either LINEAR or CUBIC_SPLINE
	 * @throws GeoTessException
	 */
	public double getPathIntegral(int attribute, 
			ArrayList<double[]> rayPath, double[] radii, int[] layerIds,
			InterpolatorType horizontalType, InterpolatorType radialType) throws GeoTessException
	{
		double integral=0.;
		double[] v1, v2=rayPath.get(0);
		double r1, r2 = radii[0];

		if (attribute < 0)
			for (int i = 1; i < rayPath.size(); ++i)
			{
				v1 = v2;
				r1 = r2;
				v2 = rayPath.get(i);
				r2 = radii[i];
				integral += GeoTessUtils.getDistance3D(v1,r1, v2, r2);
			}
		else
		{
			GeoTessPosition pos = getGeoTessPosition(horizontalType, radialType);

			double[] v = new double[3];
			double dkm;

			for (int i = 1; i < rayPath.size(); ++i)
			{
				v1 = v2;
				r1 = r2;
				v2 = rayPath.get(i);
				r2 = radii[i];
				dkm = GeoTessUtils.getDistance3D(v1,r1, v2, r2);
				v[0] = v1[0]+v2[0];
				v[1] = v1[1]+v2[1];
				v[2] = v1[2]+v2[2];
				GeoTessUtils.normalize(v);

				pos.set(layerIds == null ? -1 : layerIds[i-1], v, (r1+r2) / 2.);

				integral += dkm*pos.getValue(attribute);

			}
		}
		return integral;
	}

	/**
	 * Compute the path integral of the specified attribute along the specified rayPath
	 * and the weights on each model point that results from interpolating positions
	 * along the specified ray path.  
	 * <p>The following procedure is implemented:
	 * <ol>
	 * <li>find the midpoint of each increment of the path (line segment between
	 * adjacent positions on the path).
	 * <li>find the straight line distance between the two points, in km.
	 * <li>calculate the interpolated value of the specified attribute at the
	 * center of the path increment, constrained to reside in layer layerIds[i]
	 * <li>sum the length of the path increment times the attribute value, along the path.
	 * <li>find the interpolation coefficients of all the model points that
	 * are 'touched' by the midpoint of the increment.
	 * <li>find the product of the length of the path increment times each
	 * interpolation coefficient and sum that value for each model point.
	 * </ol>
	 *
	 * @param attribute the index of the attribute that is to be integrated.  If a value
	 * less than zero is specified then only the length of the path increments is summed
	 * and the function returns the total length of the rayPath in km.
	 * @param rayPath input array of 3-component unit vectors that contains points
	 * that define the ray path.
	 * @param radii input array of radius values, in km, that define
	 * the radius of each unit_vector supplied in 'rayPath'.
	 * @param layerIds input array of layer indices that specifies the layer in which
	 * increment i resides where increment i is the path increment between points i 
	 * and i+1.  If layerIds is null or layerIds[i] is &lt; 0, then the layer index will
	 * be determined based on the radius of the midpoint of the i'th path increment.
	 * @param horizontalType (input) the type of interpolator to use in the geographic
	 * dimensions, either LINEAR or NATURAL_NEIGHBOR
	 * @param radialType (input) the type of interpolator to use in the radial
	 * dimension, either LINEAR or CUBIC_SPLINE
	 * @param weights a map from the pointIndex of a point in the model to the weight
	 *         that accrued to that point from the ray path. The sum of all the
	 *         weights in the map will equal the length of the ray path in km.
	 * @throws GeoTessException
	 */
	public double getPathIntegral(int attribute, 
			ArrayList<double[]> rayPath, double[] radii, int[] layerIds,
			InterpolatorType horizontalType, InterpolatorType radialType,
			Map<Integer, Double> weights) throws GeoTessException
	{
		weights.clear();

		GeoTessPosition pos = getGeoTessPosition(horizontalType, radialType);

		double[] v1, v2=rayPath.get(0), v = new double[3];;
		double r1, r2 = radii[0], dkm, integral=0.;
		int layer;

		for (int i = 1; i < rayPath.size(); ++i)
		{
			v1 = v2;
			r1 = r2;
			v2 = rayPath.get(i);
			r2 = radii[i];
			dkm = GeoTessUtils.getDistance3D(v1,r1, v2, r2);

			v[0] = v1[0]+v2[0];
			v[1] = v1[1]+v2[1];
			v[2] = v1[2]+v2[2];
			GeoTessUtils.normalize(v);
			layer = layerIds == null ? -1 : layerIds[i-1];

			pos.set(layer, v, (r1+r2) / 2.);

			integral += attribute < 0 ? dkm : dkm*pos.getValue(attribute);

			pos.getWeights(weights,dkm);
		}
		return integral;
	}

	/**
	 * Compute the path integral of the specified attribute along the specified rayPath
	 * and the weights on each model point that results from interpolating positions
	 * along the specified ray path.  
	 * <p>The following procedure is implemented:
	 * <ol>
	 * <li>find the midpoint of each increment of the path (line segment between
	 * adjacent positions on the path).
	 * <li>find the straight line distance between the two points, in km.
	 * <li>calculate the interpolated value of the specified attribute at the
	 * center of the path increment, constrained to reside in layer layerIds[i]
	 * <li>sum the length of the path increment times the attribute value, along the path.
	 * <li>find the interpolation coefficients of all the model points that
	 * are 'touched' by the midpoint of the increment.
	 * <li>find the product of the length of the path increment times each
	 * interpolation coefficient and sum that value for each model point.
	 * </ol>
	 *
	 * @param attribute the index of the attribute that is to be integrated.  If a value
	 * less than zero is specified then only the length of the path increments is summed
	 * and the function returns the total length of the rayPath in km.
	 * @param rayPath input array of 3-component unit vectors that contains points
	 * that define the ray path.
	 * @param radii input array of radius values, in km, that define
	 * the radius of each unit_vector supplied in 'rayPath'.
	 * @param layerIds input array of layer indices that specifies the layer in which
	 * increment i resides where increment i is the path increment between points i 
	 * and i+1.  If layerIds is null or layerIds[i] is &lt; 0, then the layer index will
	 * be determined based on the radius of the midpoint of the i'th path increment.
	 * @param horizontalType (input) the type of interpolator to use in the geographic
	 * dimensions, either LINEAR or NATURAL_NEIGHBOR
	 * @param radialType (input) the type of interpolator to use in the radial
	 * dimension, either LINEAR or CUBIC_SPLINE
	 * @param weights a map from the pointIndex of a point in the model to the weight
	 *         that accrued to that point from the ray path. The sum of all the
	 *         weights in the map will equal the length of the ray path in km.
	 * @throws GeoTessException
	 */
	public double getPathIntegral(int attribute, 
			ArrayList<double[]> rayPath, double[] radii, int[] layerIds,
			InterpolatorType horizontalType, InterpolatorType radialType,
			HashMapIntegerDouble weights) throws GeoTessException
	{
		weights.clear();

		GeoTessPosition pos = getGeoTessPosition(horizontalType, radialType);

		double[] v1, v2=rayPath.get(0), v = new double[3];;
		double r1, r2 = radii[0], dkm, integral=0.;
		int layer;

		for (int i = 1; i < rayPath.size(); ++i)
		{
			v1 = v2;
			r1 = r2;
			v2 = rayPath.get(i);
			r2 = radii[i];
			dkm = GeoTessUtils.getDistance3D(v1,r1, v2, r2);
			v[0] = v1[0]+v2[0];
			v[1] = v1[1]+v2[1];
			v[2] = v1[2]+v2[2];
			GeoTessUtils.normalize(v);
			layer = layerIds == null ? -1 : layerIds[i-1];

			pos.set(layer, v, (r1+r2) / 2.);

			integral += attribute < 0 ? dkm :
				dkm*pos.getValue(attribute);

			pos.getWeights(weights,dkm);
		}
		return integral;
	}

	/**
	 * Compute the path integral of the specified attribute along the specified 
	 * great circle rayPath.
	 * 
	 * <p>This method only applies to 2D GeoTessModels.
	 * 
	 * <p>To obtain a GreatCircle object from pointA to pointB (both unit vectors), call
	 * <code>new GreatCircle(pointA, pointB)</code>.  More complex GreatCircle constructors
	 * are available for special situations.
	 * 
	 * <p>The following procedure is implemented:
	 * <ol>
	 * <li>divide the great circle path into nIntervals
	 * which each are of length less than or equal to pointSpacing.
	 * <li>multiply the length of the interval by the radius of the earth
	 * at the center of the interval, which converts the length of the interval into km.
	 * <li>interpolate the value of the specified attribute at the center of the path increment.
	 * <li>sum the length of the path increment times the attribute value, along the path.
	 * </ol>
	 *
	 * @param attribute index of the attribute to be integrated.  If a value
	 * less than zero is specified then only the length of the path increments is summed
	 * and the function returns the total length of the rayPath in km.
	 * @param rayPath a GreatCircle object defining the rayPath
	 * @param pointSpacing maximum point separation in radians.  The actual point spacing
	 * will generally be slightly less than the specified value so that there will be an 
	 * integral number of uniformly spaced points along the path.
	 * @param earthRadius the radius of the earth in km.  If specified value is &lt;= 0 then
	 * earthRadius is calculated to be the local radius of the WGS84 ellipsoid.
	 * @param horizontalType either InterpolatorType.NATURAL_NEIGHBOR or InterpolatorType.LINEAR.
	 * @return attribute value integrated along the specified great circle path.
	 * @throws GeoTessException if the model is not a 2D model.
	 */
	public double getPathIntegral2D(int attribute, 
			GreatCircle rayPath, double pointSpacing, double earthRadius, 
			InterpolatorType horizontalType) throws GeoTessException
	{
		if (!is2D())
			throw new GeoTessException("\nCan only apply this method to 2D models.\n");

		int nIntervals = (int) Math.ceil(rayPath.getDistance()/pointSpacing);

		if (nIntervals == 0) return 0.;

		double integral=0, r, delta = rayPath.getDistance()/nIntervals;
		double[] u = new double[3];

		GeoTessPosition pos = getGeoTessPosition(horizontalType, InterpolatorType.LINEAR);

		for (int i = 0; i < nIntervals; ++i)
		{
			rayPath.getPoint((i+0.5)*delta, u);
			r = earthRadius > 0 ? earthRadius : getEarthShape().getEarthRadius(u);
			pos.set(0, u, r);

			integral += attribute < 0 ? delta*r :
				delta*r*pos.getValue(attribute);
		}
		return integral;
	}

	/**
	 * Compute the path integral of the specified attribute along the specified rayPath
	 * and the weights on each model point that results from interpolating positions
	 * along the specified ray path.  
	 * 	 
	 * <p>This method only applies to 2D GeoTessModels.
	 * 
	 * <p>To obtain a GreatCircle object from pointA to pointB (both unit vectors), call
	 * <code>new GreatCircle(pointA, pointB)</code>.  More complex GreatCircle constructors
	 * are available for special situations.
	 * 
	 * <p>The following procedure is implemented:
	 * <ol>
	 * <li>divide the great circle path from firstPoint to lastPoint into nIntervals
	 * which each are of length less than or equal to pointSpacing.
	 * <li>multiply the length of the interval by the radius of the earth
	 * at the center of the interval, which converts the length of the interval into km.
	 * <li>interpolate the value of the specified attribute at the center of the path increment.
	 * <li>sum the length of the path increment times the attribute value, along the path.
	 * <li>find the interpolation coefficients of all the model points that
	 * are 'touched' by the midpoint of the increment.
	 * </ol>
	 *
	 * @param attribute index of the attribute to be integrated.  If a value
	 * less than zero is specified then only the length of the path increments is summed
	 * and the function returns the total length of the rayPath in km.
	 * @param rayPath a GreatCircle object defining the rayPath
	 * @param pointSpacing maximum point separation in radians.  The actual point spacing
	 * will generally be slightly less than the specified value so that there will be an 
	 * integral number of uniformly spaced points along the path.
	 * @param earthRadius the radius of the earth in km.  If specified value is &lt;= 0 then
	 * earthRadius is calculated to be the local radius of the WGS84 ellipsoid.
	 * @param horizontalType either InterpolatorType.NATURAL_NEIGHBOR or InterpolatorType.LINEAR.
	 * @param weights a map from the pointIndex of a point in the model to the weight
	 *         that accrued to that point from the ray path. The sum of all the
	 *         weights in the map will equal the length of the ray path in km.
	 * @return attribute value integrated along the specified great circle path.
	 * @throws GeoTessException if the model is not a 2D model.
	 */
	public double getPathIntegral2D(int attribute,  
			GreatCircle rayPath, double pointSpacing, double earthRadius, 
			InterpolatorType horizontalType, Map<Integer, Double> weights) throws GeoTessException
	{
		if (!is2D())
			throw new GeoTessException("\nCan only apply this method to 2D models.\n");

		weights.clear();

		int nIntervals = (int) Math.ceil(rayPath.getDistance()/pointSpacing);

		if (nIntervals == 0) return 0.;

		double integral=0, r, delta = rayPath.getDistance()/nIntervals;
		double[] u = new double[3];

		GeoTessPosition pos = getGeoTessPosition(horizontalType, InterpolatorType.LINEAR);

		for (int i = 0; i < nIntervals; ++i)
		{
			rayPath.getPoint((i+0.5)*delta, u);
			r = earthRadius > 0 ? earthRadius : getEarthShape().getEarthRadius(u);
			pos.set(0, u, r);

			integral += attribute < 0 ? delta*r :
				delta*r*pos.getValue(attribute);

			pos.getWeights(weights, delta*r);
		}
		return integral;
	}

	/**
	 * Compute the path integral of the specified attribute along the specified rayPath
	 * and the weights on each model point that results from interpolating positions
	 * along the specified ray path.  
	 * 	 
	 * <p>This method only applies to 2D GeoTessModels.
	 * 
	 * <p>To obtain a GreatCircle object from pointA to pointB (both unit vectors), call
	 * <code>new GreatCircle(pointA, pointB)</code>.  More complex GreatCircle constructors
	 * are available for special situations.
	 * 
	 * <p>The following procedure is implemented:
	 * <ol>
	 * <li>divide the great circle path from firstPoint to lastPoint into nIntervals
	 * which each are of length less than or equal to pointSpacing.
	 * <li>multiply the length of the interval by the radius of the earth
	 * at the center of the interval, which converts the length of the interval into km.
	 * <li>interpolate the value of the specified attribute at the center of the path increment.
	 * <li>sum the length of the path increment times the attribute value, along the path.
	 * <li>find the interpolation coefficients of all the model points that
	 * are 'touched' by the midpoint of the increment.
	 * </ol>
	 *
	 * @param attribute index of the attribute to be integrated.  If a value
	 * less than zero is specified then only the length of the path increments is summed
	 * and the function returns the total length of the rayPath in km.
	 * @param rayPath a GreatCircle object defining the rayPath
	 * @param pointSpacing maximum point separation in radians.  The actual point spacing
	 * will generally be slightly less than the specified value so that there will be an 
	 * integral number of uniformly spaced points along the path.
	 * @param earthRadius the radius of the earth in km.  If specified value is &lt;= 0 then
	 * earthRadius is calculated to be the local radius of the WGS84 ellipsoid.
	 * @param horizontalType either InterpolatorType.NATURAL_NEIGHBOR or InterpolatorType.LINEAR.
	 * @param weights a map from the pointIndex of a point in the model to the weight
	 *         that accrued to that point from the ray path. The sum of all the
	 *         weights in the map will equal the length of the ray path in km.
	 * @return attribute value integrated along the specified great circle path.
	 * @throws GeoTessException if the model is not a 2D model.
	 */
	public double getPathIntegral2D(int attribute,  
			GreatCircle rayPath, double pointSpacing, double earthRadius, 
			InterpolatorType horizontalType, HashMapIntegerDouble weights) throws GeoTessException
	{
		if (!is2D())
			throw new GeoTessException("\nCan only apply this method to 2D models.\n");

		weights.clear();

		int nIntervals = (int) Math.ceil(rayPath.getDistance()/pointSpacing);

		if (nIntervals == 0) return 0.;

		double integral=0, r, delta = rayPath.getDistance()/nIntervals;
		double[] u = new double[3];

		GeoTessPosition pos = getGeoTessPosition(horizontalType, InterpolatorType.LINEAR);

		for (int i = 0; i < nIntervals; ++i)
		{
			rayPath.getPoint((i+0.5)*delta, u);
			r = earthRadius > 0 ? earthRadius : getEarthShape().getEarthRadius(u);
			pos.set(0, u, r);

			integral += attribute < 0 ? delta*r :
				delta*r*pos.getValue(attribute);

			pos.getWeights(weights, delta*r);
		}
		return integral;
	}

	/**
	 * Retrieve a reference to the current Profile[][]. This is an nVertices x
	 * nLayers array of Profile objects that provides access to the Data stored
	 * in the model.
	 * 
	 * @return a reference to the current Profile[][]. This is an nVertices x
	 *         nLayers array of Profile objects that provides access to the Data
	 *         stored in the model.
	 */
	public Profile[][] getProfiles()
	{
		return profiles;
	}

	/**
	 * Get a reference to the Profile object for the specified vertex and layer.
	 * 
	 * @param vertex
	 * @param layer
	 * @return reference to the Profile object for the specified vertex and
	 *         layer.
	 */
	public Profile getProfile(int vertex, int layer)
	{
		return profiles[vertex][layer];
	}

	/**
	 * Get a reference to the radial array of Profiles defined at the specified
	 * vertex. The returned array of Profiles will have nLayers elements
	 * arranged in order of monotonically increasing radius. Note that in some
	 * layers it is possible that the array will include Profiles that are not
	 * connected by the tessellation that supports that layer. Those Profiles
	 * will be of ProfileType.Empty.
	 * 
	 * @param vertex
	 * @return a reference to the radial array of Profiles defined at the
	 *         specified vertex.
	 */
	public Profile[] getProfiles(int vertex)
	{
		return profiles[vertex];
	}

	/**
	 * Retrieve the radii of all the layer interfaces in km.
	 * The returned array has nLayers+1 elements.  The first 
	 * element is the radius of the bottom of the deepest layer
	 * and the last element is the radius of the top of the 
	 * shallowest layer.
	 * @param vertex 
	 * @return the radii of all the layer interfaces in km.
	 */
	public double[] getLayerRadii(int vertex)
	{
		double[] layerRadii = new double[metaData.getNLayers()+1];
		Profile[] p = profiles[vertex];
		layerRadii[0] = p[0].getRadiusBottom();
		for (int i=0; i<metaData.getNLayers(); ++i)
			layerRadii[i+1] = p[i].getRadiusTop();
		return layerRadii;
	}

	/**
	 * Replace the Profile object at the specified vertex and layer with a new
	 * one.
	 * 
	 * @param vertex
	 * @param layer
	 * @param profile
	 * @throws GeoTessException
	 *             if
	 *             <ul>
	 *             <li>radii are not monotonically increasing.
	 *             <li>if Profile type is NPOINT and nRadii &lt; 2
	 *             <li>if Profile type is NPOINT and nData != nRadii
	 *             <li>if number of attributes in the Data object is != number
	 *             of attributes specified in metaData.
	 *             </ul>
	 */
	public void setProfile(int vertex, int layer, Profile profile)
			throws GeoTessException
	{
		// ensure that radiusBottom <= radiusTop
		if (profile.getType() != ProfileType.SURFACE
				&& profile.getRadiusBottom() > profile.getRadiusTop())
			throw new GeoTessException("\nradiusBottom > radiusTop\n");

		// if type is NPOINT, ensure that nRadii >= 2 and nRadii == nData
		if (profile.getType() == ProfileType.NPOINT
				&& (profile.getNRadii() < 2 || profile.getNRadii() != profile
				.getNData()))
			throw new GeoTessException(
					String.format(
							"\nProfile type is NPOINT, nRadii = %d and nData = %d\n"
									+ "When type is NPOINT, nRadii must equal nData, and both must be >= 2\n",
									profile.getNRadii(), profile.getNData()));

		for (int i = 0; i < profile.getNData(); ++i)
		{
			// make sure data has the right number of attributes
			if (profile.getData(i).size() != metaData.getNAttributes())
				throw new GeoTessException(
						String.format(
								"\nData object has %d attributes but metaData.nAttributres = %d%n",
								profile.getData(i).size(),
								metaData.getNAttributes()));

			// make sure data is of the correct type (same as metadata.datatype)
			if (profile.getData(i).getDataType() != metaData.getDataType())
				throw new GeoTessException(
						String.format(
								"\nData object is of type %s but MetaData is expecting data of type %s\n",
								profile.getData(i).getDataType(),
								metaData.getDataType()));
		}

		profiles[vertex][layer] = profile;
	}

	/**
	 * Replace the Profile object at the specified vertex and layer with a new
	 * ProfileEmpty profile.
	 * 
	 * @param vertex
	 * @param layer
	 * @param radii only radii[0] and radii[radii.length-1] are used.
	 * @throws GeoTessException
	 */
	public void setProfile(int vertex, int layer, float[] radii) throws GeoTessException
	{ setProfile(vertex, layer, new ProfileEmpty(radii[0], radii[radii.length-1])); }

	/**
	 * Replace the Profile object at the specified vertex and layer with a new
	 * one.
	 * 
	 * @param vertex
	 * @param layer
	 * @param radii
	 * @param data
	 * @throws GeoTessException
	 */
	public void setProfile(int vertex, int layer, float[] radii, Data[] data) throws GeoTessException
	{
		setProfile(vertex, layer, Profile.newProfile(radii, data));
	}

	/**
	 * Replace the Profile object at the specified vertex and layer with a new
	 * one.
	 * 
	 * @param vertex
	 *            index of the vertex
	 * @param layer
	 *            index of the layer
	 * @param radii
	 *            the radii of the nodes that span the layer, in km.
	 * @param values
	 *            and nNodes by nAttributes array of model values.
	 * @throws GeoTessException
	 */
	public void setProfile(int vertex, int layer, float[] radii,
			double[][] values) throws GeoTessException
	{
		if (!getConnectedVertices(layer).contains(vertex))
			setProfile(vertex, layer, new ProfileEmpty(radii[0],
					radii[radii.length - 1]));
		else
			setProfile(vertex, layer, Profile.newProfile(radii, values));
	}

	/**
	 * Replace the Profile object at the specified vertex and layer with a new
	 * one.
	 * 
	 * @param vertex
	 *            index of the vertex
	 * @param layer
	 *            index of the layer
	 * @param radii
	 *            the radii of the nodes that span the layer, in km.
	 * @param values
	 *            and nNodes by nAttributes array of model values.
	 * @throws GeoTessException
	 */
	public void setProfile(int vertex, int layer, float[] radii,
			float[][] values) throws GeoTessException
	{
		if (!getConnectedVertices(layer).contains(vertex))
			setProfile(vertex, layer, new ProfileEmpty(radii[0],
					radii[radii.length - 1]));
		else
			setProfile(vertex, layer, Profile.newProfile(radii, values));
	}

	/**
	 * Replace the Profile object at the specified vertex and layer with a new
	 * one.
	 * 
	 * @param vertex
	 *            index of the vertex
	 * @param layer
	 *            index of the layer
	 * @param radii
	 *            the radii of the nodes that span the layer, in km.
	 * @param values
	 *            and nNodes by nAttributes array of model values.
	 * @throws GeoTessException
	 */
	public void setProfile(int vertex, int layer, float[] radii, long[][] values)
			throws GeoTessException
	{
		if (!getConnectedVertices(layer).contains(vertex))
			setProfile(vertex, layer, new ProfileEmpty(radii[0],
					radii[radii.length - 1]));
		else
			setProfile(vertex, layer, Profile.newProfile(radii, values));
	}

	/**
	 * Replace the Profile object at the specified vertex and layer with a new
	 * one.
	 * 
	 * @param vertex
	 *            index of the vertex
	 * @param layer
	 *            index of the layer
	 * @param radii
	 *            the radii of the nodes that span the layer, in km.
	 * @param values
	 *            and nNodes by nAttributes array of model values.
	 * @throws GeoTessException
	 */
	public void setProfile(int vertex, int layer, float[] radii, int[][] values)
			throws GeoTessException
	{
		if (!getConnectedVertices(layer).contains(vertex))
			setProfile(vertex, layer, new ProfileEmpty(radii[0],
					radii[radii.length - 1]));
		else
			setProfile(vertex, layer, Profile.newProfile(radii, values));
	}

	/**
	 * Replace the Profile object at the specified vertex and layer with a new
	 * one.
	 * 
	 * @param vertex
	 *            index of the vertex
	 * @param layer
	 *            index of the layer
	 * @param radii
	 *            the radii of the nodes that span the layer, in km.
	 * @param values
	 *            and nNodes by nAttributes array of model values.
	 * @throws GeoTessException
	 */
	public void setProfile(int vertex, int layer, float[] radii,
			short[][] values) throws GeoTessException
	{
		if (!getConnectedVertices(layer).contains(vertex))
			setProfile(vertex, layer, new ProfileEmpty(radii[0],
					radii[radii.length - 1]));
		else
			setProfile(vertex, layer, Profile.newProfile(radii, values));
	}

	/**
	 * Replace the Profile object at the specified vertex and layer with a new
	 * one.
	 * 
	 * @param vertex
	 *            index of the vertex
	 * @param layer
	 *            index of the layer
	 * @param radii
	 *            the radii of the nodes that span the layer, in km.
	 * @param values
	 *            and nNodes by nAttributes array of model values.
	 * @throws GeoTessException
	 */
	public void setProfile(int vertex, int layer, float[] radii, byte[][] values)
			throws GeoTessException
	{
		if (!getConnectedVertices(layer).contains(vertex))
			setProfile(vertex, layer, new ProfileEmpty(radii[0],
					radii[radii.length - 1]));
		else
			setProfile(vertex, layer, Profile.newProfile(radii, values));
	}

	/**
	 * Replace the Profile object at the specified vertex with a ProfileSurfaceEmpty
	 * object.
	 * 
	 * @param vertex
	 * @throws GeoTessException
	 */
	public void setProfile(int vertex)
			throws GeoTessException
	{
		setProfile(vertex, 0, new ProfileSurfaceEmpty());
	}

	/**
	 * Replace the Profile object at the specified vertex with a ProfileSurface
	 * object populated with the specified Data value(s).
	 * 
	 * @param vertex
	 * @param data the Data object to assign to the specified vertex.
	 * @throws GeoTessException
	 */
	public void setProfile(int vertex, Data data)
			throws GeoTessException
	{
		setProfile(vertex, 0, new ProfileSurface(data));
	}

	/**
	 * Retrieve the number of active points in each layer of the model.
	 * 
	 * @return the number of active points in each layer of the model.
	 */
	public int[] getActiveVertexCount()
	{
		int[] count = new int[getNLayers()+1];
		for (int layer = 0; layer < getNLayers(); ++layer)
		{
			HashSetInteger vertices = getConnectedVertices(layer);
			Iterator it = vertices.iterator();
			while (it.hasNext())
			{
				Profile p = profiles[it.next()][layer];
				for (int node=0; node < p.getNData(); ++node)
					if (p.getPointIndex(node) >= 0)
					{
						++count[layer];
						break;
					}
			}
			count[getNLayers()] += count[layer];
		}
		return count;
	}

	/**
	 * Retrieve the number of points in each layer of the model.
	 * 
	 * @param activeOnly
	 *            if true, counts only active nodes otherwise counts all nodes.
	 * @return the number of points in each layer of the model.
	 */
	public int[] getLayerCount(boolean activeOnly)
	{
		getPointMap();
		int[] count = new int[getNLayers()];
		for (Profile[] pp : profiles)
			for (int layer = 0; layer < pp.length; ++layer)
				if (activeOnly)
				{
					Profile p = pp[layer];
					for (int n = 0; n < p.getNData(); ++n)
						if (p.getPointIndex(n) >= 0)
							++count[layer];
				}
				else
					count[layer] += pp[layer].getNData();
		return count;
	}

	/**
	 * For each layer, count the number of Profiles of each type and return the
	 * results in an array list with entry for each layer. For each layer the
	 * entry is a map from ProfileType to Integer count.
	 * 
	 * @return number of Profiles of each ProfileType in each layer of the
	 *         model.
	 */
	public ArrayList<HashMap<ProfileType, Integer>> profileCount()
	{
		ProfileType ptype;

		ArrayList<HashMap<ProfileType, Integer>> count = new ArrayList<HashMap<ProfileType, Integer>>(
				getNLayers() + 1);
		for (int layer = 0; layer < getNLayers(); ++layer)
		{
			HashMap<ProfileType, Integer> typeCount = new HashMap<ProfileType, Integer>(
					ProfileType.values().length);
			for (ProfileType type : ProfileType.values())
				typeCount.put(type, 0);
			count.add(typeCount);

			for (int vertex = 0; vertex < profiles.length; ++vertex)
			{
				ptype = profiles[vertex][layer].getType();
				typeCount.put(ptype, typeCount.get(ptype) + 1);
			}
		}

		// now count the totals in all layers
		HashMap<ProfileType, Integer> total = new HashMap<ProfileType, Integer>(
				ProfileType.values().length);
		for (ProfileType type : ProfileType.values())
			total.put(type, 0);
		count.add(total);
		for (int layer = 0; layer < getNLayers(); ++layer)
			for (ProfileType t : ProfileType.values())
			{
				total.put(t, total.get(t) + count.get(layer).get(t));
			}

		return count;
	}

	/**
	 * Returns a string with summary information about the model.
	 * 
	 * @return a string with summary information about the model.
	 */
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(
				"********************************************************************************")
		.append(GeoTessUtils.NL);
		buf.append(metaData.toString());

		ArrayList<HashMap<ProfileType, Integer>> pcount = profileCount();
		HashMap<ProfileType, Integer> count;

		int[] layerCount = getLayerCount(false);
		int totalLayerCount = 0;
		for (int i : layerCount) totalLayerCount += i;

		int[] activeCount = getLayerCount(true);
		//		int totalActiveCount = 0;
		//		for (int i : layerCount) totalActiveCount += i;

		int[] activeVertexCount = getActiveVertexCount();

		buf.append("Layer  connected   active     number     active    profile    profile    profile    profile    profile    profile\n");
		buf.append("Index  vertices   vertices  of points    points    npoints    constant    thin       empty     surface   surface_empty\n");
		buf.append("-----  --------  ---------  ---------  ---------  ---------  ---------  ---------  ---------  ---------  -------------\n");
		for (int i = getNLayers() - 1; i >= 0; --i)
		{
			count = pcount.get(i);
			buf.append(String.format("%3d %10d %10d %10d %10d %10d %10d %10d %10d %10d %10d%n", i,
					grid.getVertexIndicesTopLevel(metaData.getTessellation(i)).size(), 
					activeVertexCount[i],
					layerCount[i],
					activeCount[i],
					count.get(ProfileType.NPOINT), 
					count.get(ProfileType.CONSTANT), 
					count.get(ProfileType.THIN), 
					count.get(ProfileType.EMPTY), 
					count.get(ProfileType.SURFACE),
					count.get(ProfileType.SURFACE_EMPTY)
					));
		}

		count = pcount.get(getNLayers());
		buf.append("-----  --------  ---------  ---------  ---------  ---------  ---------  ---------  ---------  ---------  -------------\n");
		buf.append(String.format("Total %8d %10d %10d %10d %10d %10d %10d %10d %10d %10d%n",
				grid.getNVertices(), 
				activeVertexCount[getNLayers()],
				totalLayerCount,
				getPointMap().size(),
				count.get(ProfileType.NPOINT), 
				count.get(ProfileType.CONSTANT),
				count.get(ProfileType.THIN), 
				count.get(ProfileType.EMPTY),
				count.get(ProfileType.SURFACE),
				count.get(ProfileType.SURFACE_EMPTY)
				));

		if (grid != null)
			buf.append("\n").append(grid.toString());
		buf.append(
				"********************************************************************************")
		.append(GeoTessUtils.NL);
		return buf.toString();
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
	protected GeoTessModel loadModel(String inputFile, String relGridFilePath)
			throws IOException
	{
		return loadModel(new File(inputFile), relGridFilePath);
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
	protected GeoTessModel loadModel(File inputFile, String relGridFilePath)
			throws IOException
	{
		try
		{
			metaData.setInputModelFile(inputFile);

			if (relGridFilePath == null)
				relGridFilePath = "";

			long timer = System.nanoTime();

			if (inputFile.getName().endsWith(".ascii"))
				loadModelAscii(inputFile, relGridFilePath);
			else
				loadModelBinary(inputFile, relGridFilePath);

			metaData.setLoadTimeModel((System.nanoTime() - timer) * 1e-9);

			return this;
		}
		catch (GeoTessException e)
		{
			throw new IOException(e);
		}
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
	protected GeoTessModel loadModel(String inputFile) throws IOException
	{
		return loadModel(inputFile, "");
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
	protected GeoTessModel loadModel(File inputFile) throws IOException
	{
		return loadModel(inputFile.getCanonicalPath(), "");
	}

	/**
	 * Write the model to file. The data (radii and attribute values) are
	 * written to outputFile. If gridFileName is '*' or the string "null" then
	 * the grid information is written to the same file as the data. If
	 * gridFileName is something else, it should be the name of the file that
	 * contains the grid information. In the latter case, the gridFile
	 * referenced by gridFileName is not overwritten; all that happens is that
	 * the name of the grid file (with no path information) is stored in the
	 * data file.
	 * 
	 * @param outputFile
	 *            name of the file to receive the model
	 * @param gridFileName
	 *            name of file that contains the grid, or "*", "null".
	 *            If a full path is specified only the final name is used.
	 * @throws IOException
	 */
	public void writeModel(String outputFile, String gridFileName)
		throws IOException
	{
	    if (gridFileName == null || gridFileName.trim().length() == 0 || 
		    gridFileName.equalsIgnoreCase("null"))
		gridFileName = "*";

	    if (gridFileName.equals("*"))
		metaData.getProperties().remove("gridRelativePath");
	    else
	    {
		File gridFile = new File(gridFileName);

		if (!gridFile.exists())
		    grid.writeGrid(gridFile);
		else if (!grid.getGridID().equals(GeoTessGrid.getGridID(gridFileName)))
		    throw new IOException(String.format(
			  "GridID's do not match.  This model is supported by a GeoTessGrid \n"
			  + "with gridID %s but the GeoTessGrid specified with command line argument "
			  + "gridFileName, \n%s\n has gridID %s%n",
		    	grid.getGridID(), gridFileName, GeoTessGrid.getGridID(gridFileName)));

		Path modelPath = new File(outputFile).getCanonicalFile().getParentFile().toPath();
		Path gridPath = gridFile.getCanonicalFile().toPath();
		Path relativePath = modelPath.relativize(gridPath);
		metaData.getProperties().put("gridRelativePath", relativePath.toString());

		gridFileName = new File(gridFileName).getName();
	    }

	    long timer = System.nanoTime();

	    if (outputFile.endsWith(".ascii"))
		writeModelAscii(outputFile, gridFileName);
	    else
		writeModelBinary(outputFile, gridFileName);

	    metaData.setWriteTimeModel((System.nanoTime() - timer) * 1e-9);

	    metaData.setOutputModelFile(outputFile);
	}

	/**
	 * If model has been written to an output file, then name 
	 * of the outputFile is returned. Else if model was read from
	 * a file, the name of the input file is returned. 
	 * Otherwise, "null" is returned.
	 * 
	 * @return The current file name.
	 */
	public String getCurrentModelFileName()
	{
		
		try {
			if ((metaData.getOutputModelFile() == null) ||
					metaData.getOutputModelFile().equals(""))
				return metaData.getInputModelFile().getCanonicalPath();
			else
				return metaData.getOutputModelFile();
		} catch (IOException e) {
			return "null";
		}
	}

	/**
	 * Uses the current file name to write this model state back to the disk.
	 * If the model was created from scratch (not read from an existing file) and
	 * it has not been written using a standard write method containing a file
	 * name then an error is thrown.
	 * 
	 * @throws IOException
	 */
	public void writeModel() throws Exception
	{
		writeModel(getCurrentModelFileName());
	}

	/**
	 * Write the model to file. The data (radii and attribute values) are
	 * written to outputFile. If gridFileName is '*' then the grid information
	 * is written to the same file as the data. If gridFileName is something
	 * else, it should be the name of the file that contains the grid
	 * information. In the latter case, the gridFile referenced by gridFileName
	 * is not overwriiten; all that happens is that the name of the grid file
	 * (with no path information) is stored in the data file.
	 * 
	 * @param outputFile
	 *            name of the file to receive the model
	 * @param gridFileName
	 *            name of file that contains the grid, or "*"
	 * @throws IOException
	 */
	public void writeModel(File outputFile, String gridFileName) throws IOException
	{ writeModel(outputFile.getCanonicalPath(), gridFileName); }

	public void writeModelBinary(OutputStream outputStream) throws IOException
	{ 
		writeModelBinary(new DataOutputStream(outputStream), "*");
		outputStream.close();
	}

	public void writeModelAscii(OutputStream outputStream) throws IOException
	{ 
		writeModelAscii(new OutputStreamWriter(outputStream), "*"); 
		outputStream.close();
	}

	/**
	 * Write the model to file. The data (radii and attribute values) are
	 * written to outputFile. gridFilePath is the path to the grid file that
	 * supports this model. If the grid file does not exist, it is created and
	 * the grid is written to it. The grid is not written to the model output
	 * file. The name of the grid file (without any path information) is
	 * included in the model file.
	 * 
	 * @param outputFile
	 *            name of the file to receive the model
	 * @param gridFilePath
	 *            full path to the grid file that supports this
	 *            model. If the file does not exist it is created and the 
	 *            grid is written to it.
	 * @throws IOException
	 */
	public void writeModel(String outputFile, File gridFilePath)
			throws IOException
	{
		long timer = System.currentTimeMillis();

		if (!gridFilePath.exists())
		{
			File gridDir = gridFilePath.getParentFile();
			if (gridDir == null) gridDir = new File(".");
			gridDir.mkdirs();
			grid.writeGrid(gridFilePath);
		}
		
		Path modelPath = new File(outputFile).getCanonicalFile().getParentFile().toPath();
		Path gridPath = gridFilePath.getCanonicalFile().toPath();
		Path relativePath = modelPath.relativize(gridPath);
		metaData.getProperties().put("gridRelativePath", relativePath.toString());

		if (outputFile.endsWith(".ascii"))
			writeModelAscii(outputFile, gridFilePath.getName());
		else
			writeModelBinary(outputFile,  gridFilePath.getName());

		metaData.setWriteTimeModel((System.currentTimeMillis() - timer) * 1e-3);

		metaData.setOutputModelFile(outputFile);
	}

	/**
	 * Write the model to file. The data (radii and attribute values) are
	 * written to outputFile. gridFilePath is the path to the grid file that
	 * supports this model. If the grid file does not exist, it is created and
	 * the grid is written to it. The grid is not written to the model output
	 * file. The name of the grid file (without any path information) is
	 * included in the model file.
	 * 
	 * @param outputFile
	 *            name of the file to receive the model
	 * @param gridFilePath
	 *            full path to the grid file that supports this
	 *            model. If the file does not exist it is created and the 
	 *            grid is written to it.
	 * @throws IOException
	 */
	public void writeModel(File outputFile, File gridFilePath)
			throws IOException
	{
		writeModel(outputFile.getCanonicalPath(), gridFilePath);
	}

	/**
	 * Write the model to file. The grid information is written to the same file
	 * along with the data.
	 * 
	 * @param outputFile
	 *            name of the file to receive the model
	 * @throws IOException
	 */
	public void writeModel(String outputFile) throws IOException
	{
		writeModel(outputFile, "*");
	}

	/**
	 * Write the model to file. The grid information is written to the same file
	 * along with the data.
	 * 
	 * @param outputFile
	 *            name of the file to receive the model
	 * @throws IOException
	 */
	public void writeModel(File outputFile) throws IOException
	{
		writeModel(outputFile, "*");
	}

	/**
	 * Load a model (3D grid and data) from a binary File.
	 * <p>
	 * The format of the file is: <br>
	 * int fileFormatVersion (currently only recognizes 1). <br>
	 * String gridFile: either *, or relative path to gridFile. <br>
	 * int nVertices, nLayers, nAttributes, dataType(DOUBLE or FLOAT). <br>
	 * int[] tessellations = new int[nLayers]; <br>
	 * Profile[nVertices][nLayers]: data
	 *
	 * @param inputFile
	 * @param relGridFilePath
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected void loadModelBinary(File inputFile, String relGridFilePath)
			throws GeoTessException, IOException
	{
		DataInputStream input = new DataInputStream(new BufferedInputStream(
				new FileInputStream(inputFile)));

		loadModelBinary(input, inputFile.getParent(), relGridFilePath);

		input.close();
	}

	/**
	 * Load a model (3D grid and data) from a binary File.
	 * <p>
	 * The format of the file is: <br>
	 * int fileFormatVersion (currently only recognizes 1). <br>
	 * String gridFile: either *, or relative path to gridFile. <br>
	 * int nVertices, nLayers, nAttributes, dataType(DOUBLE or FLOAT). <br>
	 * int[] tessellations = new int[nLayers]; <br>
	 * Profile[nVertices][nLayers]: data
	 *
	 * @param input
	 * @param inputDirectory
	 * @param relGridFilePath
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected void loadModelBinary(DataInputStream input,
			String inputDirectory, String relGridFilePath)
					throws GeoTessException, IOException
	{
		metaData.load(input);

		int nVertices = metaData.getNVertices();
		int nLayers = metaData.getNLayers();

		profiles = new Profile[nVertices][nLayers];

		// loop over all the vertices of the 2D grid and load the data
		for (int i = 0; i < nVertices; ++i)
			for (int j = 0; j < nLayers; ++j)
				profiles[i][j] = Profile.newProfile(input, metaData);

		// read the name of the gridFile
		String inputGridFile = GeoTessUtils.readString(input, 2048);

		// read the gridID from the model file.
		String gridID = GeoTessUtils.readString(input, 32);

		loadGrid(input, inputDirectory, relGridFilePath, inputGridFile, gridID);
		
		pointMap = new PointMap(this);
	}
	
	public static String getGridID(File inputFile) throws Exception
	{
		DataInputStream input = new DataInputStream(new BufferedInputStream(
			new FileInputStream(inputFile)));

		GeoTessMetaData metaData = new GeoTessMetaData();
		metaData.load(input);
		
		String gridID = metaData.getProperties().get("gridID");
		if (gridID != null)
		    return gridID;
		    
		int nVertices = metaData.getNVertices();
		int nLayers = metaData.getNLayers();

		// loop over all the vertices of the 2D grid and load the data
		for (int i = 0; i < nVertices; ++i)
			for (int j = 0; j < nLayers; ++j)
				Profile.newProfile(input, metaData);

		// read the name of the gridFile
		GeoTessUtils.readString(input, 1024);

		// read the gridID from the model file.
		gridID = GeoTessUtils.readString(input, 32);
		
		return gridID;
	}

	/**
	 * Load the grid.
	 * 
	 * @param input
	 *            an Object of type Scanner, DataInputStream
	 * @param modelDirectory
	 *            the name of the directory where the model file resides
	 * @param gridRelativePath
	 *            the relative path from the modelDirectory to the directory where
	 *            the grid file resides
	 * @param gridFileName
	 *            the name of the grid file. If this argument is "*", then the
	 *            grid will be loaded from the model file.
	 * @param gridID
	 *            the unique id of the grid as stored in the model file. If a
	 *            grid is loaded from file and it does not have this gridID, an
	 *            exception is thrown.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	private synchronized void loadGrid(Object input, String modelDirectory,
		String gridRelativePath, String gridFileName, String gridID)
			throws IOException, GeoTessException
	{
	    // See if grid can be retrieved from the reuseGridMap.
	    grid = metaData.isGridReuseOn() ? reuseGridMap.get(gridID) : null;

	    if (gridFileName.equals("*"))
	    {
		metaData.setGridInputFileName(gridFileName);
		
		// load the grid from this input file. The grid has to be read from
		// the file, even if a reference was retrieved from the reuseGridMap, 
		// so that the file is positioned where classes that extend
		// GeoTessModel can read additional data.
		GeoTessGrid g = null;
		// load the grid from this input file.
		if (input instanceof Scanner)
		    g = new GeoTessGrid((Scanner) input);
		else if (input instanceof DataInputStream)
		    g = new GeoTessGrid((DataInputStream) input);
		else
		    throw new GeoTessException("Cannot load grid from input device of type "
			    +input.getClass().getCanonicalName());

		if (grid == null)
		{
		    grid = g;

		    if (metaData.isGridReuseOn())
			reuseGridMap.put(grid.getGridID(), grid);
		}
	    }
	    else if (grid == null)
	    {
		// grid was not retrieved from the reuseGridMap and it is to be loaded 
		// from an external file
		
		if (modelDirectory == null) modelDirectory = ".";
		if (gridRelativePath == null) gridRelativePath = ".";
		
		Path modelPath = new File(modelDirectory).getCanonicalFile().toPath();

		// look for the grid file in the directory specified by the user, with
		// filename read in from the model file
		Path gridPath = modelPath.resolve(gridRelativePath);
		File gridFile = null;

		File userSpecifiedFile = new File(gridPath.toFile().getCanonicalFile(), gridFileName);
		if (userSpecifiedFile.exists()) {
		    grid = new GeoTessGrid().loadGrid(userSpecifiedFile);
		    gridFile = userSpecifiedFile;
		}
		
		// if grid file not found where user specified, or if that is the wrong grid,
		// look for grid file in metadata properties
		File metaDataFile = null;
		if (grid == null || !grid.getGridID().equals(gridID)) {
		    gridRelativePath = metaData.getProperties().get("gridRelativePath");
		    if (gridRelativePath != null)
		    {
			gridPath = modelPath.resolve(gridRelativePath);
			metaDataFile = gridPath.toFile().getCanonicalFile();
			
			if (metaDataFile.exists()) {
			    grid = new GeoTessGrid().loadGrid(metaDataFile);
			    gridFile = metaDataFile;
			}
		    }
		}

		// if neither grid file exists throw an exception
		if (gridFile == null)
		    throw new IOException(String.format("GeoTessGrid file does not exist. Searched for:%n%s%n%s%n",
			    userSpecifiedFile.getAbsolutePath(), 
			    (metaDataFile==null ? "" : metaDataFile.getAbsolutePath())));

		if (!grid.getGridID().equals(gridID)) {
		    File inputModel = metaData.getInputModelFile();
		    throw new GeoTessException(String.format(
			    "gridIDs in model file and grid file are not equal%n"
				    + "gridID stored in Model file is %s%n"
				    + "gridID stored in Grid  file is %s%n"
				    + "model file: %s%n"
				    + "grid file: %s%n",
				    gridID, grid.getGridID(), 
				    inputModel == null ? "-" : inputModel.getAbsolutePath(),
				    gridFile.getAbsolutePath()));
		}
		
		metaData.setGridInputFileName(gridFile.getCanonicalPath());
		
		metaData.getProperties().put("gridRelativePath", gridRelativePath);
		
		if (metaData.isGridReuseOn())
		    reuseGridMap.put(grid.getGridID(), grid);

	    }

	    grid.incRefCount();
	    metaData.getProperties().put("gridID", grid.getGridID());
	}

	/**
	 * Replace the current grid with a new one.  
	 * @param newGrid
	 * @throws Exception 
	 */
	public void setGrid(GeoTessGrid newGrid) throws Exception
	{
		if (this.grid != null)
		{
			if (this.grid.getNTessellations() != newGrid.getNTessellations())
				throw new Exception("this.grid.getNTessellations() != newGrid.getNTessellations()");

			if (this.grid.getNLevels() != newGrid.getNLevels())
				throw new Exception("this.grid.getNLevels() != newGrid.getNLevels()");

			if (this.grid.getNTriangles() != newGrid.getNTriangles())
				throw new Exception("this.grid.getNTriangles() != newGrid.getNTriangles()");

			if (this.grid.getNVertices() != newGrid.getNVertices())
				throw new Exception("this.grid.getNVertices() != newGrid.getNVertices()");
		}
		
		this.grid.decRecCount();

		if (metaData.isGridReuseOn())
		{
			this.grid = reuseGridMap.get(newGrid.getGridID());
			if (this.grid == null)
			{
				this.grid = newGrid;
				reuseGridMap.put(this.grid.getGridID(), this.grid);
			}
		}
		else
			this.grid = newGrid;
		
		this.grid.incRefCount();
	}

	/**
	 * Write the model currently in memory to a binary file. A model can be
	 * stored with the data and grid in the same or separate files. This method
	 * will write the data from the 3D model to the specified outputfile. If the
	 * supplied gridFileName is the single character "*", then the grid
	 * information is written to the same file as the data. If the gridFileName
	 * is anything else, it is assumed to be the relative path from the data
	 * file to an existing file where the grid information is stored. In the
	 * latter case, the grid information is not actually written to the
	 * specified file; all that happens is that the relative path is stored in
	 * the data file.
	 *
	 * @param outputFile
	 *            the name of the file to which the data should be written.
	 * @param gridFileName
	 *            either "*" or the name of the file that contains the grid definition
	 *            (without path information).
	 * @throws IOException
	 */
	protected void writeModelBinary(String outputFile, String gridFileName)
			throws IOException
	{
		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(outputFile)));

		writeModelBinary(output, gridFileName);

		output.close();
	}

	/**
	 * Write the model currently in memory to a binary file. A model can be
	 * stored with the data and grid in the same or separate files. This method
	 * will write the data from the 3D model to the specified outputfile. If the
	 * supplied gridFileName is the single character "*", then the grid
	 * information is written to the same file as the data. If the gridFileName
	 * is anything else, it is assumed to be the relative path from the data
	 * file to an existing file where the grid information is stored. In the
	 * latter case, the grid information is not actually written to the
	 * specified file; all that happens is that the relative path is stored in
	 * the data file.
	 *
	 * @param output
	 *            the OutputStream to which the data should be written.
	 * @param gridFileName
	 *            either "*" or the name of the file that contains the grid definition.
	 * @throws IOException
	 */
	protected void writeModelBinary(DataOutputStream output, String gridFileName)
			throws IOException
	{
		try {
			testModelIntegrity();
		} catch (GeoTessException e) {
			throw new IOException(e);
		}
		metaData.writeModelBinary(output, grid.getNVertices());

		for (Profile[] profiles : this.profiles)
			for (Profile profile : profiles)
				profile.write(output);

		GeoTessUtils.writeString(output, gridFileName);
		GeoTessUtils.writeString(output, grid.getGridID());

		if (gridFileName.equals("*"))
			grid.writeGridBinary(output);

		output.flush();
	}

	/**
	 * Load a model (3D grid and data) from an ascii File.
	 * <p>
	 * The format of the file is: <br>
	 * int fileFormatVersion (currently only recognizes 1). <br>
	 * String gridFile: either *, or relative path to gridFile. <br>
	 * int nVertices, nLayers, nAttributes, dataType(DOUBLE or FLOAT). <br>
	 * int[] tessellations = new int[nLayers]; <br>
	 * Profile[nVertices][nLayers]: data
	 *
	 * @param inputFile
	 * @param relGridFilePath
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected void loadModelAscii(File inputFile, String relGridFilePath)
			throws GeoTessException, IOException
	{
		Scanner input = new Scanner(inputFile);

		loadModelAscii(input, inputFile.getParent(), relGridFilePath);

		input.close();
	}

	/**
	 * Load a model (3D grid and data) from an ascii File.
	 * <p>
	 * The format of the file is: <br>
	 * int fileFormatVersion (currently only recognizes 1). <br>
	 * String gridFile: either *, or relative path to gridFile. <br>
	 * int nVertices, nLayers, nAttributes, dataType(DOUBLE or FLOAT). <br>
	 * int[] tessellations = new int[nLayers]; <br>
	 * Profile[nVertices][nLayers]: data
	 *
	 * @param input
	 * @param inputDirectory
	 * @param relGridFilePath
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected void loadModelAscii(Scanner input, String inputDirectory,
			String relGridFilePath) throws GeoTessException, IOException
	{
		metaData.load(input);

		int nVertices = metaData.getNVertices();
		int nLayers = metaData.getNLayers();

		profiles = new Profile[nVertices][nLayers];

		// loop over all the vertices of the 2D grid
		for (int i = 0; i < nVertices; ++i)
			for (int j = 0; j < nLayers; ++j)
				profiles[i][j] = Profile.newProfile(input, metaData);

		input.nextLine();
		String inputGridFile = input.nextLine().trim();

		// read the gridID from the model file.
		String gridID = input.nextLine().trim();

		loadGrid(input, inputDirectory, relGridFilePath, inputGridFile, gridID);

		pointMap = new PointMap(this);
	}

	/**
	 * Write the model currently in memory to an ascii file. A model can be
	 * stored with the data and grid in the same or separate files. This method
	 * will write the data from the 3D model to the specified outputfile. If the
	 * supplied gridFileName is the single character "*", then the grid
	 * information is written to the same file as the data. If the gridFileName
	 * is anything else, it is assumed to be the relative path from the data
	 * file to an existing file where the grid information is stored. In the
	 * latter case, the grid information is not actually written to the
	 * specified file; all that happens is that the relative path is stored in
	 * the data file.
	 *
	 * @param outputFile
	 *            the name of the file to which the data should be written.
	 * @param gridFileName
	 *            either "*" or the name of the file that contains the grid definition.
	 * @throws IOException
	 */
	protected void writeModelAscii(String outputFile, String gridFileName)
			throws IOException
	{
		BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
		writeModelAscii(output, gridFileName);
		output.close();
	}

	/**
	 * Write the model currently in memory to an ascii file. A model can be
	 * stored with the data and grid in the same or separate files. This method
	 * will write the data from the 3D model to the specified outputfile. If the
	 * supplied gridFileName is the single character "*", then the grid
	 * information is written to the same file as the data. If the gridFileName
	 * is anything else, it is assumed to be the relative path from the data
	 * file to an existing file where the grid information is stored. In the
	 * latter case, the grid information is not actually written to the
	 * specified file; all that happens is that the relative path is stored in
	 * the data file.
	 *
	 * @param output
	 *            the name of the file to which the data should be written.
	 * @param gridFileName
	 *            either "*" or the name of the file that contains the grid definition.
	 * @throws IOException
	 */
	protected void writeModelAscii(Writer output, String gridFileName)
			throws IOException
	{
		try {
			testModelIntegrity();
		} catch (GeoTessException e) {
			throw new IOException(e);
		}

		metaData.writeModelAscii(output, grid.getNVertices());
		for (int n = 0; n < grid.getNVertices(); ++n)
		{
			Profile[] profiles = this.profiles[n];
			for (int l = 0; l < profiles.length; ++l)
				profiles[l].write(output);
		}

		output.write(String.format("%s%n", gridFileName));
		output.write(grid.getGridID() + GeoTessUtils.NL);

		if (gridFileName.equals("*"))
			grid.writeGridAscii(output);

	}

	/**
	 * Test the array of profiles at each vertex to ensure that the top of one
	 * layer and the bottom of the layer above it have the same radii, within
	 * 0.01 km. If they do not, throw an exception.
	 * 
	 * @throws GeoTessException
	 */
	public void testModelIntegrity() throws GeoTessException
	{
		ProfileType ptype = profiles[0][0].getType();
		boolean isSurface = ptype == ProfileType.SURFACE || ptype == ProfileType.SURFACE_EMPTY;

		if (isSurface && getNLayers() != 1)
			throw new GeoTessException("Model comprised of profiles of type ProfileSurface must have exactly 1 layer.\n" +
					"This model contains layers " + metaData.getLayerNamesString());

		StringBuffer repairs = new StringBuffer();

		for (int vertex = 0; vertex < profiles.length; ++vertex)
		{
			Profile[] p = profiles[vertex];

			ptype = p[0].getType();

			for (int layer = 0; layer < p.length; ++layer)
				if ((p[0].getType() == ProfileType.SURFACE || p[0].getType() == ProfileType.SURFACE_EMPTY) != isSurface)
					throw new GeoTessException("Model may not contain a mix of ProfileSurface profiles and profiles of other types.");

			// ensure that radii at interfaces are equal
			for (int layer=p.length-1; layer>=1; --layer)
			{
				// throw an exception if radii missmatch is more than 0.01 km.
				double dr = p[layer].getRadiusBottom() - p[layer - 1].getRadiusTop();
				if (Math.abs(dr) > 0.01)
					throw new GeoTessException(
							String.format(
									"%nAt vertex %d the radius at top of layer %d is %1.3f %n"
											+ "and the radius at the bottom of layer %d is %1.3f.  They "
											+ "differ by %1.3f",
											vertex,
											layer - 1,
											p[layer - 1].getRadiusTop(),
											layer,
											p[layer].getRadiusBottom(),
											dr));
				else if (Math.abs(dr) > 0.)
				{
					// repair any missmatch that is less than 0.01 km
					int up = layer;
					while (up < p.length-1 && p[up].getType() == ProfileType.THIN)
						++up;

					float r = p[up].getRadii()[0];

					int below = layer-1;
					while (below > 0 && p[below].getType() == ProfileType.THIN)
						--below;

					float[] radii = p[below].getRadii();

					repairs.append(String.format("GeoTessModel.testLayerRadii() -- Changing radius vertex=%6d, layer=%2d, from %8.3f to %8.3f, dr=%6.3f%n",
							vertex, below, radii[radii.length-1], r, r-radii[radii.length-1]));
					radii[radii.length-1] = r;

					setProfile(vertex, below, radii, p[below].getData());

					for (int i=below+1; i<=up; ++i)
					{
						radii = p[i].getRadii();

						repairs.append(String.format("GeoTessModel.testLayerRadii() -- Changing radius vertex=%6d, layer=%2d, from %8.3f to %8.3f, dr=%6.3f%n",
								vertex, i, radii[0], r, r-radii[0]));
						radii[0] = r;

						setProfile(vertex, i, radii, p[i].getData());
					}
				}
			}

			// check to ensure that radiusBottom <= radiusTop
			for (int layer = 0; layer < p.length; ++layer)
				if (p[layer].getNRadii() > 1
						&& p[layer].getRadiusBottom() > p[layer].getRadiusTop())
					throw new GeoTessException(
							String.format(
									"%nradiusBottom > radiusTop%n"
											+ "radiusTop    = %9.4f%n"
											+ "radiutBottom = %9.4f%n"
											+ "layer=%d, vertex=%d, lat=%1.4f, lon=%1.4f",
											p[layer].getRadiusBottom(), p[layer]
													.getRadiusTop(), layer, vertex,
													getEarthShape().getLatDegrees(
															getVertex(vertex)), getEarthShape()
													.getLonDegrees(getVertex(vertex))));
		}

		//System.out.print(repairs.toString());

		// throw an exception if radii don't match exactly at any interface.		
		for (int vertex = 0; vertex < profiles.length; ++vertex)
		{
			Profile[] p = profiles[vertex];

			// ensure that radii at interfaces are equal
			for (int layer=p.length-1; layer>=1; --layer)
			{
				double dr = p[layer].getRadiusBottom() - p[layer - 1].getRadiusTop();
				if (Math.abs(dr) > 0.0)
					throw new GeoTessException(
							String.format(
									"%nAt vertex %d the radius at top of layer %d is %1.3f %n"
											+ "and the radius at the bottom of layer %d is %1.3f.  They "
											+ "differ by %1.6f",
											vertex,
											layer - 1,
											p[layer - 1].getRadiusTop(),
											layer,
											p[layer].getRadiusBottom(),
											dr));
			}
		}

		// ensure that every Data object is of the correct size and data type.
		for (int p=0; p<getPointMap().size(); ++p)
		{
			if (pointMap.getPointData(p).size() != metaData.getNAttributes())
				throw new GeoTessException(String.format("pointMap.getPointData().size() [%d] != metaData.getNAttributes() [%d] at pointIndex %d",
						pointMap.getPointData(p).size(), metaData.getNAttributes(), p));
			if (pointMap.getPointData(p).getDataType() != metaData.getDataType())
				throw new GeoTessException(String.format("pointMap.getPointData().getDataType() [%s] != metaData.getDataType() [%s] at pointIndex %d",
						pointMap.getPointData(p).getDataType().toString(), metaData.getDataType().toString(), p));
		}
	}

	/**
	 * Static factory method that determines the derived class of the specified
	 * model in the specified inputFile and returns a model of that class.
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static GeoTessModel getGeoTessModel(String input) throws Exception {
		return getGeoTessModel(input, "");
	}

	/**
	 * Static factory method that determines the derived class of the specified
	 * model in the specified inputFile and returns a model of that class.
	 * 
	 * @param input
	 * @param pathToGridDir
	 * @return
	 * @throws Exception
	 */
	public static GeoTessModel getGeoTessModel(String input, String pathToGridDir) throws Exception {
		return getGeoTessModel(new File(input), "");
	}
	
	/**
	 * Static factory method that determines the derived class of the specified
	 * model in the specified inputFile and returns a model of that class.
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static GeoTessModel getGeoTessModel(File input) throws Exception
	{
		return getGeoTessModel(input, "");
	}

	/**
	 * Static factory method that determines the derived class of the specified
	 * model in the specified inputFile and returns a model of that class.
	 * @param input
	 * @param pathToGridDir
	 * @return
	 * @throws Exception
	 */
	public static GeoTessModel getGeoTessModel(File input, String pathToGridDir) throws Exception
	{
		String className = GeoTessModel.getClassName(input, pathToGridDir);

		if (className.equalsIgnoreCase("GeoTessModelSiteData"))
			return new GeoTessModelSiteData(input, pathToGridDir);

		if (className.equalsIgnoreCase("LibCorr3DModel"))
			return new LibCorr3DModel(input, pathToGridDir);

		if (className.equalsIgnoreCase("GeoTessModelAmplitude"))
			return new GeoTessModelAmplitude(input, pathToGridDir);

		if (className.equalsIgnoreCase("GeoTessModelSLBM") || className.equalsIgnoreCase("SLBM"))
			return new GeoTessModelSLBM(input, pathToGridDir);

		if (className.equalsIgnoreCase("GeoTessModelSLBMPDU"))
			return new GeoTessModelSLBMPDU(input, pathToGridDir);

		return new GeoTessModel(input, pathToGridDir);
	}

	/**
	 * Static factory method to instantiate a new GeoTessModel or derived class.
	 * The class of the returned model is determined by querying the metaData object
	 * for the class name.
	 * Data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * <li>setSoftwareVersion()
	 * <li>setGenerationDate()
	 * </ul>
	 * 
	 * @param grid
	 *            a reference to the GeoTessGrid that will support this
	 *            GeoTessModel.
	 * @param metaData
	 *            MetaData the new GeoTessModel instantiates a reference to the
	 *            supplied metaData. No copy is made.
	 * @throws GeoTessException
	 *             if metadata is incomplete.
	 */
	public static GeoTessModel getGeoTessModel(GeoTessGrid grid, GeoTessMetaData metaData) throws Exception
	{
		String className = metaData.getModelClassName();

		if (className.equalsIgnoreCase("GeoTessModelSiteData"))
			return new GeoTessModelSiteData(grid, metaData);

		if (className.equalsIgnoreCase("LibCorr3DModel"))
			return new LibCorr3DModel(grid, metaData);

		if (className.equalsIgnoreCase("GeoTessModelAmplitude"))
			return new GeoTessModelAmplitude(grid, metaData);

		if (className.equalsIgnoreCase("GeoTessModelSLBM"))
			return new GeoTessModelSLBM(grid, metaData);

		if (className.equalsIgnoreCase("GeoTessModelSLBMPDU"))
			return new GeoTessModelSLBMPDU(grid, metaData);

		return new GeoTessModel(grid, metaData);

	}

	/**
	 * Test a file to see if it is a GeoTessModel file or derivative.
	 * 
	 * @param inputFile
	 * @return true if inputFile is a GeoTessModel or one of it's derivatives.
	 */
	public static boolean isGeoTessModel(File inputFile)
	{
		String line = "";
		try
		{
			if (inputFile.getName().endsWith(".ascii"))
			{
				Scanner input = new Scanner(inputFile);
				line = input.nextLine();
				input.close();
			}
			else
			{
				DataInputStream input = new DataInputStream(
						new BufferedInputStream(new FileInputStream(inputFile)));
				byte[] bytes = new byte[12];
				input.read(bytes);
				line = new String(bytes);
				input.close();
			}
		}
		catch (Exception ex)
		{
			return false;
		}
		return line.equals("GEOTESSMODEL");
	}

	/**
	 * Retrieve the class name of the model stored in the specified File.
	 * @param inputFile
	 * @return
	 * @throws Exception
	 */
	public static String getClassName(File inputFile) throws Exception
	{
		return getClassName(inputFile, "");
	}

	/**
	 * Retrieve the class name of the model stored in the specified File.
	 * @param inputFile
	 * @param pathToGridDir
	 * @return
	 * @throws Exception
	 */
	public static String getClassName(File inputFile, String pathToGridDir) throws Exception
	{
		if (GeoTessGrid.isGeoTessGrid(inputFile))
			return "GeoTessGrid";

		String className = "?";
		int formatVersion = -1;
		if (inputFile.getName().endsWith(".ascii"))
		{
			Scanner input = new Scanner(inputFile);
			String line = input.nextLine();
			if (line.equals("GEOTESSMODEL"))
			{
				formatVersion = input.nextInt(); input.nextLine();
				if (formatVersion >= 3)
					className = input.nextLine();
				input.close();
				if (className.equals("?"))
				{
					// have to read the whole file
					input = new Scanner(inputFile);
					GeoTessModel model = new GeoTessModel();
					model.loadModelAscii(input, inputFile.getParent(), pathToGridDir);
					if (input.hasNext())
						className = input.nextLine();
					else
						className = "GeoTessModel";
					input.close();
				}
			}
			else
				input.close();
		}
		else
		{
			DataInputStream input = new DataInputStream(
					new BufferedInputStream(new FileInputStream(inputFile)));
			byte[] bytes = new byte[12];
			input.read(bytes);
			String line = new String(bytes);
			if (line.equals("GEOTESSMODEL"))
			{
				formatVersion = input.readInt();
				if (formatVersion >= 3)
					className = GeoTessUtils.readString(input, 1024);
				input.close();
				if (className.equals("?"))
				{
					// have to read the whole file
					input = new DataInputStream(
							new BufferedInputStream(new FileInputStream(inputFile)));              
					GeoTessModel model = new GeoTessModel();
					model.loadModelBinary(input, inputFile.getParent(), pathToGridDir);
					try
					{
						className = GeoTessUtils.readString(input, 1024);
					}
					catch (EOFException ex)
					{
						className = "GeoTessModel";
					}
					finally
					{
						input.close();
					}
					// there are some very old LibCorr3DModels where the 'extra' derived class
					// data did not start with the word LibCorr3DModels but rather began directly
					// with the station name.  Those models had 3 characteristics that allow us
					// to identify them: (1) the file date stored in the file will be old (< 2014),
					// (2) the file name will start with the station name which is equal to the 
					// first word read from the file and (3) station names have 6 or fewer characters.
					if (className.length() <= 6 && inputFile.getName().startsWith(className))
					{
						className = "LibCorr3DModel";
					}
				}
			}
			else
				input.close();
		}
		return className;
	}

	/**
	 * Returns true if this model is a 2D model which is the case when
	 * all the Profiles are of type 
	 * ProfileType.SURFACE and/or ProfileType.SURFACE_EMPTY.
	 * @return true if this is a 2D model.
	 */
	public boolean is2D() 
	{
		return getProfile(0, 0).getType() == ProfileType.SURFACE
				|| getProfile(0, 0).getType() == ProfileType.SURFACE_EMPTY;
	}

	/**
	 * Returns true if this model is a 3D model which is the case when none
	 * of the Profiles are of type ProfileType.SURFACE or ProfileType.SURFACE_EMPTY.
	 * @return true if this is a 3D model.
	 */
	public boolean is3D() 
	{
		return !is2D();
	}

	/**
	 *  Retrieve a reference to the ellipsoid that is currently associated with this GeoTessModel and
	 *  which is being used to convert between geocentric and
	 *  geographic latitude and between depth and radius.  
	 *  <p>
	 *  The following EarthShapes are supported:
	 * <ul>
	 * <li>SPHERE - Geocentric and geographic latitudes are identical and
	 * conversion between depth and radius assume the Earth is a sphere
	 * with constant radius of 6371 km.
	 * <li>GRS80 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the GRS80 ellipsoid.
	 * <li>GRS80_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the GRS80 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>WGS84 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the WGS84 ellipsoid.
	 * <li>WGS84_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the WGS84 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>IERS2003 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the IERS2003 ellipsoid.
	 * <li>IERS2003_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the IERS2003 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * </ul>
	 * @return a reference to the EarthShape currently in use.
	 */
	public EarthShape getEarthShape()
	{
		return metaData.getEarthShape();
	}

	/**
	 *  Set the EarthShape object that is to be used to convert between geocentric and
	 *  geographic latitude and between depth and radius.  The EarthShape will be saved to file with 
	 *  this GeoTessModel if this model is written tot file.
	 *  <p>
	 *  The following EarthShapes are supported:
	 * <ul>
	 * <li>SPHERE - Geocentric and geographic latitudes are identical and
	 * conversion between depth and radius assume the Earth is a sphere
	 * with constant radius of 6371 km.
	 * <li>GRS80 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the GRS80 ellipsoid.
	 * <li>GRS80_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the GRS80 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>WGS84 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the WGS84 ellipsoid.
	 * <li>WGS84_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the WGS84 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>IERS2003 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the IERS2003 ellipsoid.
	 * <li>IERS2003_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the IERS2003 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * </ul>
	 */
	public void setEarthShape(EarthShape earthShape)
	{
		metaData.setEarthShape(earthShape);
	}

	/**
	 *  Set the EarthShape object that is to be used to convert between geocentric and
	 *  geographic latitude and between depth and radius.  The following EarthShapes are supported:
	 * <ul>
	 * <li>SPHERE - Geocentric and geographic latitudes are identical and
	 * conversion between depth and radius assume the Earth is a sphere
	 * with constant radius of 6371 km.
	 * <li>GRS80 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the GRS80 ellipsoid.
	 * <li>GRS80_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the GRS80 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>WGS84 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the WGS84 ellipsoid.
	 * <li>WGS84_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the WGS84 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * <li>IERS2003 - Conversion between geographic and geocentric latitudes, and between depth
	 * and radius are performed using the parameters of the IERS2003 ellipsoid.
	 * <li>IERS2003_RCONST - Conversion between geographic and geocentric latitudes are performed using
	 * the parameters of the IERS2003 ellipsoid.  Conversions between depth and radius
	 * assume the Earth is a sphere with radius 6371.
	 * </ul>
	 */
	public void setEarthShape(String earthShape)
	{
		metaData.setEarthShape(EarthShape.valueOf(earthShape));
	}

	/**
	 * Extract a GradientCalculator from gradientCalculatorPool and return it. If
	 * gradientCalculatorPool is empty a new GradientCalculator is created and
	 * returned.  GradientCalculator's retrieved by this method should be
	 * returned to GradientCalculatorPool by calling returnGradientCalculator.
	 * 
	 * @return A GradientCalculator object
	 * @throws GeoTessException 
	 */
	protected GradientCalculator getGradientCalculator() throws GeoTessException
	{
		GradientCalculator gc = gradientCalculatorPool.poll();
		if (gc == null)
			return new GradientCalculator(this, getMetaData().getGradientCalculatorTetSize());
		return gc;
	}

	/**
	 * Return a GradientCalculator object to gradientCalculatorPool managed by
	 * this GeoTessModel.
	 * 
	 * @param gradientCalculator The returned gradient calculator.
	 */
	protected void returnGradientCalculator(GradientCalculator gradientCalculator)
	{
		gradientCalculatorPool.offer(gradientCalculator);
	}

	/**
	 * Force calculation of gradient information at many nodes.
	 * 
	 * @param attributeIndex The index of the attribute whose gradients are to be
	 *                       calculated.
	 * @param reciprocal     If true the gradient of the inverse attribute is
	 *                       calcualted.
	 * @param layers         The indexes of the layers where gradient calculations
	 *                       are to be performed.
	 * @throws GeoTessException
	 */
	public void computeGradients(int attributeIndex, boolean reciprocal,
			int[] layers)
					throws GeoTessException
	{
		// loop over all vertices
		for (int vrtx = 0; vrtx < getNVertices(); ++vrtx)
		{
			// loop over all layers
			Profile[] pLayers = profiles[vrtx];
			double[] vrtxUnitVec = getVertex(vrtx);
			for (int layer = 0; layer < layers.length; ++layer)
			{
				// get the profile for this vertex/layer and compute its gradients
				Profile pL = pLayers[layers[layer]];
				pL.computeGradients(this, attributeIndex, vrtxUnitVec,
						layers[layer], reciprocal);
			}
		}
	}

	/**
	 * Retrieve the requested attribute gradient at the specified active point
	 * index. If reciprocal is true the gradient of the inverse attribute is
	 * returned. This method computes and stores the gradient for later
	 * retreival. Subsequent calls to the method with the same argument list
	 * simply returns the previously stored result.
	 * 
	 * @param pointIndex     The active point index for which the gradient will
	 *                       be returned.
	 * @param attributeIndex The attribute index for which the gradient will be
	 *                       returned.
	 * @param reciprocal     A boolean flag, which if true, returns the gradient
	 *                       of the inverse attribute. 
	 * @param gradient       The gradient filled on exit.
	 * @throws GeoTessException
	 */
	public void getPointGradient(int pointIndex, int attributeIndex, 
			boolean reciprocal, double[] gradient)
					throws GeoTessException
	{
		PointMap pm = getPointMap();
		int vertexIndex = pm.getVertexIndex(pointIndex);
		int layerId     = pm.getLayerIndex(pointIndex);
		int nodeIndex   = pm.getNodeIndex(pointIndex);

		Profile p       = profiles[vertexIndex][layerId];
		p.computeGradients(this, attributeIndex, getVertex(vertexIndex),
				layerId, reciprocal);
		p.getGradient(nodeIndex, attributeIndex, gradient);
	}

	/**
	 * Retrieve the requested attribute gradient at the specified vertex/layer/node
	 * index. If reciprocal is true the gradient of the inverse attribute is
	 * returned. This method computes and stores the gradient for later
	 * retreival. Subsequent calls to the method with the same argument list
	 * simply returns the previously stored result.
	 * 
	 * @param vertexIndex    The index of the vertex for which the gradient will
	 *                       be returned.
	 * @param layerId        The layer id for which the gradient will be
	 *                       returned.
	 * @param nodeIndex      The index of the layers sub node for which the
	 *                       gradient will be returned.
	 * @param attributeIndex The attribute index for which the gradient will be
	 *                       returned.
	 * @param reciprocal     A boolean flag, which if true, returns the gradient
	 *                       of the inverse attribute. 
	 * @param gradient       The gradient filled on exit.
	 * @throws GeoTessException
	 */
	public void getGradient(int vertexIndex, int layerId,
			int nodeIndex, int attributeIndex,
			boolean reciprocal, double[] gradient)
					throws GeoTessException
	{
		Profile p = profiles[vertexIndex][layerId];
		p.computeGradients(this, attributeIndex, getVertex(vertexIndex),
				layerId, reciprocal);
		p.getGradient(nodeIndex, attributeIndex, gradient);
	}

	/**
	 * Retrieve the requested attribute gradient at the specified vertex/layer
	 * index and radius. If reciprocal is true the gradient of the inverse
	 * attribute is returned. This method computes and stores the gradient for later
	 * retreival. Subsequent calls to the method with the same argument list
	 * simply returns the previously stored result.
	 * 
	 * @param vertexIndex    The index of the vertex for which the gradient will
	 *                       be returned.
	 * @param layerId        The layer id for which the gradient will be
	 *                       returned.
	 * @param radius         The radius along the profile for which the
	 *                       gradient will be returned. If the radius lies out-
	 *                       of-range of the layers interfaces the appropriate
	 *                       interface gradient is returned.
	 * @param attributeIndex The attribute index for which the gradient will be
	 *                       returned.
	 * @param reciprocal     A boolean flag, which if true, returns the gradient
	 *                       of the inverse attribute. 
	 * @param gradient       The gradient filled on exit.
	 * @throws GeoTessException
	 */
	public void getGradient(int vertexIndex, int layerId,
			double radius, int attributeIndex,
			boolean reciprocal, double[] gradient)
					throws GeoTessException
	{
		Profile p = profiles[vertexIndex][layerId];
		p.computeGradients(this, attributeIndex, getVertex(vertexIndex),
				layerId, reciprocal);
		p.getGradient(attributeIndex, radius, gradient);
	}

	/**
	 * Returns true if the gradient has been calculated and set for the input
	 * vertex/layer/attribute indices.
	 * 
	 * @param vertexIndex
	 * @param layerId
	 * @param attributeIndex
	 * @return True if the gradient has been set.
	 */
	public boolean isComputedGradientSet(int vertexIndex, int layerId,
			int attributeIndex)
	{
		return profiles[vertexIndex][layerId].isGradientSet(attributeIndex);
	}

	/**
	 * Returns true if the reciprocal flag of the requested vertex/layer/attribute
	 * indices is true.
	 * 
	 * @param vertexIndex    The vertex index.
	 * @param layerId        The layer id.
	 * @param attributeIndex The attribute index.
	 * @return True if the reciprocal flag of the requested vertex/layer/attribute
	 *         indices is true.
	 */
	public boolean getComputedGradientReciprocalFlag(int vertexIndex, int layerId,
			int attributeIndex)
	{
		return profiles[vertexIndex][layerId].getGradientReciprocalFlag(attributeIndex);
	}

	/**
	 * Pre-computes the layer normals at the top of all vertex layer interfaces.
	 * The normals are calculated using the weighted average of the facet normals
	 * for all triangles shared by a vertex at each layer boundary. If the input
	 * flag (useAreaWeights) is true each facet normal is weighted by the triangle
	 * normal. Otherwise, the normals are unit (1.0) weighted.
	 * 
	 * @param useAreaWeights If true the layer normals are calculated by weighting
	 *                       each shared triangle normal by it's area. The
	 *                       weighting flag is saved in the GeoTessModel metadata
	 *                       object.
	 */
	public void computeLayerNormals(boolean useAreaWeights)
	{
		metaData.setLayerNormalAreaWeight(useAreaWeights);
		// map of layer index associated with map of triangle vertex associated with
		// it's double-area normal vector (the area is 1/2 the magnitude of this
		// vector). This map is used to avoid recalculating the normal of a triangle
		// more than once.
		HashMap<Integer, HashMap<Integer, double[]>> tri2Area;
		tri2Area = new HashMap<Integer, HashMap<Integer, double[]>>();

		// loop over all vertices and all layers
		double[] nt = {0.0, 0.0, 0.0};
		for (int i = 0; i < profiles.length; ++i)
		{
			// get vertex profiles and loop over each layer
			Profile[] profLayers = profiles[i];
			for (int lid = 0; lid < profLayers.length; ++lid)
			{
				// get map of triangle indices associated with the triangle 2-area
				// normal. If it doesn't exist create it
				HashMap<Integer, double[]> layerTri2Area = tri2Area.get(lid);
				if (layerTri2Area == null)
				{
					layerTri2Area = new HashMap<Integer, double[]>();
					tri2Area.put(lid,  layerTri2Area);
				}

				// get vertex/layer profile and see if it is defined and its definition
				// has at least 1 radius specified
				Profile p = profLayers[lid];
				if ((p != null) && (p.getNRadii() > 0))
				{
					// this profile is defined and has at least one radius ...
					// calculate normal
					double[] n = new double [3];

					// get the vertex triangles for the top level of this layer
					int tessId = metaData.getTessellation(lid);
					ArrayListInt trias = grid.getVertexTriangles(tessId, i);

					// loop over each triangle index and get the triangles normal
					for (int k = 0; k < trias.size(); ++k)
					{
						int triIndx = trias.get(k);

						// see if triangles doubled area normal vector is in the temporary map
						double[] t2A = layerTri2Area.get(triIndx);
						if (t2A == null)
						{
							// doubled area normal vector not yet defined ... calculate and
							// store
							t2A = this.getTriangleDoubleAreaVector(triIndx, lid);
							layerTri2Area.put(triIndx, t2A);
						}

						// calculate triangle normal weight as either unit or area weighted
						if (useAreaWeights)
						{
							// use area triangle normal weighting
							n[0] += t2A[0];
							n[1] += t2A[1];
							n[2] += t2A[2];
						}
						else
						{
							// use unit triangle normal weighting
							nt[0] = t2A[0];
							nt[1] = t2A[1];
							nt[2] = t2A[2];
							GeoTessUtils.normalize(nt);
							n[0] += nt[0];
							n[1] += nt[1];
							n[2] += nt[2];
						}
					}

					// normalize result and save as layer normal
					GeoTessUtils.normalize(n);
					p.setLayerNormal(n);
				}
			}
		}
	}

	/**
	 * Calculates and returns the layer normal for the input point index. If the
	 * normal exists the method simply returns the result. Otherwise, the normal
	 * is calculated and set so that subsequent calls do not need to repeat the
	 * calculation. The type of facet weighting (area or unit) is defined by the
	 * GeoTessMetaData method useLayerNormalAreaWeight(). If true facet area
	 * weighting is used. Otherwise, unit facet normals are averaged to obtain
	 * the layer normal.
	 * 
	 * @param pointIndex The point index for which the layer normal is returned.
	 * @return The layer normal at the requested point index.
	 */
	public double[] getLayerNormal(int pointIndex)
	{
		int vi  = pointMap.getVertexIndex(pointIndex);
		int lid = pointMap.getLayerIndex(pointIndex);
		return getLayerNormal(vi, lid);
	}

	/**
	 * Calculates and returns the layer normal for the input vertex index/
	 * layer id. If the normal exists the method simply returns the result.
	 * Otherwise, the normal is calculated and set so that subsequent calls do
	 * not need to repeat the calculation. The type of facet weighting (area or
	 * unit) is defined by the GeoTessMetaData method useLayerNormalAreaWeight().
	 * If true facet area weighting is used. Otherwise, unit facet normals are
	 * averaged to obtain the layer normal.
	 * 
	 * @param vertexIndex The vertex index for which the layer normal is returned.
	 * @param layerId     The layer id for which the layer normal is returned.
	 * @return The layer normal at the requested vertex index / layer id.
	 */
	public double[] getLayerNormal(int vertexIndex, int layerId)
	{
		// get the requested vertex / layer profile and its normal ... see if it
		// has not yet been evaluated (null)
		Profile p = profiles[vertexIndex][layerId];
		double[] layerNormal = p.getLayerNormal();
		if (layerNormal == null)
		{
			// the normal needs to be calculated ... id this is a surface profile
			// simply return the grid vertex unit normal.
			if ((p.getType() == ProfileType.SURFACE) ||
					(p.getType() == ProfileType.SURFACE_EMPTY))
			{
				return getVertex(vertexIndex);
			}
			else
			{
				// this profile is defined and has at least one radius ...
				// calculate new layer normal
				layerNormal = new double [3];

				// get the vertex triangles for the top level of this layer
				int tessId = metaData.getTessellation(layerId);
				ArrayListInt trias = grid.getVertexTriangles(tessId, vertexIndex);

				// loop over each triangle index
				for (int k = 0; k < trias.size(); ++k)
				{
					// get triangles area weighted normal
					int triIndx = trias.get(k);
					double[] t2AreaNormal = getTriangleDoubleAreaVector(triIndx, layerId);

					// normalize the area weighted normal if unit weighting is defined
					if (!metaData.useLayerNormalAreaWeight())
						GeoTessUtils.normalize(t2AreaNormal);

					// sum the facet normal to the layer normal
					layerNormal[0] += t2AreaNormal[0];
					layerNormal[1] += t2AreaNormal[1];
					layerNormal[2] += t2AreaNormal[2];
				}

				// done ... normalize the layer normal and set it into the profile.
				GeoTessUtils.normalize(layerNormal);
				p.setLayerNormal(layerNormal);
			}
		}

		// return the result
		return layerNormal;
	}

	/**
	 * Calculates the triangle normal vector scaled by twice the triangle area.
	 * Used to calculate weighted normals of the triangles surrounding a grid
	 * vertex at the top of the layer boundary.
	 * 
	 * @param triangleIndex
	 * @param layerId
	 * @return The triangle normal scaled by twice the triangle area. 
	 */
	protected double[] getTriangleDoubleAreaVector(int triangleIndex, int layerId)
	{
		// get the triangle vertex positions in v0, v1, and v2 ... get each vertex
		// unit vector into v and scale by the top most radius of the input layer
		// (layerId) r ...
		double r;
		double[] v;
		int[] vIndexes = grid.getTriangleVertexIndexes(triangleIndex);
		v = getVertex(vIndexes[0]);
		r = profiles[vIndexes[0]][layerId].getRadiusTop();
		double[] v0 = {r * v[0], r * v[1], r * v[2]};
		v = getVertex(vIndexes[1]);
		r = profiles[vIndexes[1]][layerId].getRadiusTop();
		double[] v1 = {r * v[0], r * v[1], r * v[2]};
		v = getVertex(vIndexes[2]);
		r = profiles[vIndexes[2]][layerId].getRadiusTop();
		double[] v2 = {r * v[0], r * v[1], r * v[2]};

		// get double area normal vector (2 * triangle area along normal direction) 
		// and return the result
		double[] t2AreaNormal = new double [3];
		Vector3D.cross(v0,  v2,  v1, t2AreaNormal); // assumes clockwise ordering
		return t2AreaNormal;
	}

	/**
	 * Retrieve the pointIndices of all the points connected together 
	 * at the specified layer and tessellation level
	 * @param layer
	 * @param level the tessellation layer relative the first level of 
	 * the tessellation that supports the specified layer.
	 * @return the pointIndices of all the points connected together 
	 * at the specified layer and tessellation level
	 */
	public HashSetInteger getPoints(int layer, int level) 
	{
		HashSetInteger points = new HashSetInteger(1000);
		int tessId = metaData.getTessellation(layer);
		HashSetInteger vertices = grid.getVertexIndices(tessId, level);
		Iterator it = vertices.iterator();
		while (it.hasNext())
		{
			Profile p = profiles[it.next()][layer];
			for (int n=0; n<p.getNData(); ++n)
				points.add(p.getPointIndex(n));
		}
		return points;
	}

	public GeoTessModel resample(GeoTessGrid newGrid) throws Exception
	{
		// create a new GeoTessModel model with the new grid and a copy of 
		// the metadata from the this model.  If this GeoTessModel is an instance 
		// of a derived class, then newModel will also be an instance of the derived 
		// class, but all the extra data will be uninitialized.
		// This constructor does not populate the new model with any data; we will do that below.
		GeoTessModel newModel = GeoTessModel.getGeoTessModel(newGrid, getMetaData().copy());

		// Get a GeoTessPosition object associated with the this model.  This 
		// object lets us interpolate data from the this model at any position 
		// we want, e.g., all the vertices of the new grid, even if they do not
		// coincide with vertices in this model.
		GeoTessPosition pos = getGeoTessPosition();

		// now iterate over every vertex of the new grid, interpolate data from 
		// this model, and use it to populate the data in the new model.
		if (is2D()) 
		{
			// interpolating data from 2D models is much simpler than 3D models 
			// because there is only one kind of Profile (ProfileSurfacre).
			// For this reason, it is dealt with separately from the 3D models.
			for (int vertex=0; vertex < newGrid.getNVertices(); ++vertex)
			{
				// set the position where we want to interpolate data from the old model.
				// Radius is irrelevant at this point.
				pos.set(newModel.getVertex(vertex), 1.);
				
				if (pos.getVertices().length == 1)
					newModel.setProfile(vertex, 0, getProfile(pos.getVertices()[0], 0).copy());
				else
				{
					Data data = pos.getData();
					boolean allNaN = true;
					for (int a=0; a<data.size(); ++a)
						allNaN = allNaN && data.isNaN(a);
					
					if (allNaN)
						newModel.setProfile(vertex, 0, new ProfileSurfaceEmpty());
					else
					{
						// construct a ProfileSurface with the data. A ProfileSurface has data
						// but no radii and hence can only be added to a model that is a 2D model
						// (geographic dimensions only; no radii).
						Profile profile = new ProfileSurface(data);

						// associate the Profile object with the specified vertex and layer of the
						// new model. This model has only 1 layer, so specify layer index = 0
						newModel.setProfile(vertex, 0, profile);
					}
				}
			}
		}
		else if (is3D()) 
		{
			for (int vertex=0; vertex < newGrid.getNVertices(); ++vertex)
			{
				for (int layer=0; layer < getNLayers(); ++layer)
				{
					// set the position to the layer and unit vector where we want to interpolate
					// data from the old model. Radius is irrelevant at this point.
					pos.set(layer, newModel.getVertex(vertex), 6371.);

					// get the indices of the vertices in the old model that contribute to 
					// interpolation of data.
					int[] neighbors = pos.getVertices();

					if (neighbors.length == 1)
					{
						// populate the new model with a copy of the profile from the old model.
						newModel.setProfile(vertex, layer, getProfile(neighbors[0], layer).copy());
					}
					else
					{
						// figure out what type of Profile we need to construct, and the maximum
						// number of radii in any of the neighbors.
						ProfileType pType = ProfileType.THIN;
						int maxRadii = 0;
						for (int n : neighbors)
						{
							// Get the ProfileType of one of the neighbors.
							ProfileType pt = getProfile(n, layer).getType();
							
							if (pt == ProfileType.EMPTY)
							{
								// If any neighbor's profile is of type EMPTY
								// then we must make a ProfileEmpty also.
								pType = ProfileType.EMPTY;
								break;
							}
							// we want to make a profile with the most number of 
							// radii. 
							if (pt.ordinal() > pType.ordinal())
								pType = pt;

							// also want to know what the maximum number of 
							// radii is in any neighbor profile.
							if (getProfile(n, layer).getNRadii() > maxRadii)
								maxRadii = getProfile(n, layer).getNRadii();
						}
						
						float rBottom = (float) pos.getRadiusBottom(layer);
						float rTop = (float) pos.getRadiusTop(layer);
						
						if ((pType == ProfileType.CONSTANT || pType == ProfileType.NPOINT) 
								&& rTop <= rBottom)
							pType = ProfileType.THIN;

						switch (pType) {
						case EMPTY: 
						{
							newModel.setProfile(vertex, layer, new ProfileEmpty(rBottom, rTop));
							break;
						}
						case THIN:
						{
							newModel.setProfile(vertex, layer, new ProfileThin(rTop, pos.getData()));
							break;
						}
						case CONSTANT: 
						{
							newModel.setProfile(vertex, layer, new ProfileConstant(rBottom, rTop, 
									pos.getData()));

							break;
						}
						case NPOINT: 
						{
							float[] radii = Globals.getArrayFloat(rBottom, rTop, (rTop-rBottom)/(maxRadii-1));
							Data[] data = new Data[radii.length];
							// get data values interpolated from the old model at the locations of
							// of the nodes in the new model
							for (int i=0; i<radii.length; ++i)
								data[i] = pos.setRadius(radii[i]).getData();

							newModel.setProfile(vertex, layer, new ProfileNPoint(radii, data));

							break;
						}
						default: 
						{
							throw new Exception("Imossible! (famous last words...)");
							//break;
						}
						}
					}
				}
			}
		}
		
		// if this model is a class that extends GeoTessModel, then allow the newModel
		// to copy extra data from this model.
		newModel.copyDerivedClassData(this);

		// we now have a new GeoTessModel with all the same data as 
		// this model but re-sampled onto the new grid.

		return newModel;
	}

	/**
	 * Classes that extend GeoTessModel must override this method
	 * and populate their 'extra' data with shallow copies from the model 
	 * specified in the parameter list.  
	 * @param other the other model from which to copy extra data
	 * @throws Exception 
	 */
	public void copyDerivedClassData(GeoTessModel other) throws Exception
	{
		// base class GeoTessModels don't have extra data, so nothing to do.
		// Derived classes must override this and copy their data.
	}
	
	public String getGridID() { return grid.getGridID(); }
	
	public void close() throws Exception {
	    if (grid.decRecCount() <= 0)
		reuseGridMap.remove(grid.getGridID());
	}

}
