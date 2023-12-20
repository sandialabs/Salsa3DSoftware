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
package gov.sandia.gmp.baseobjects.seismicitydepth;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import gov.sandia.geotess.Data;
import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessModelUtils;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.geotess.PointMap;
import gov.sandia.geotess.ProfileType;
import gov.sandia.geotessbuilder.GeoTessBuilderMain;
import gov.sandia.gmp.util.globals.DataType;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.io.GlobalInputStreamProvider;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.Schema;

public class SeismicityDepthModelGenerator {

	public static void main(String[] args) {
		try {
			if (args.length == 0)
			{
				// if no command line arguments print instructions
				printComments();
				System.exit(0);
			}

			// load properties
			PropertiesPlus properties = new PropertiesPlus(new File(args[0]));

			// run appropriate mode.
			if (properties.getProperty("mode").equals("smoothed_max_depth"))
				new SeismicityDepthModelGenerator().smoothedMaxDepth(properties);
			else if (properties.getProperty("mode").equals("seismicity_depth_model"))
				new SeismicityDepthModelGenerator().seismicityDepthModel(properties);
			else if (properties.getProperty("mode").equals("seismicity_depth_resample"))
				new SeismicityDepthModelGenerator().seismicityDepthResample(properties);

			System.out.println("Done.");

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	static private void printComments()
	{
		ArrayList<String> comments = new ArrayList<>();
		comments.add("This app operates in two modes: smoothed_max_depth and seismicity_depth_model.");
		comments.add("In the smoothed_max_depth mode, the app queries a database for lat, lon, depth");
		comments.add("info and creates a 2D geotess model with the max depth at the vertices.");
		comments.add("These values go into attribute index 0.  Then the app smooths the values a ");
		comments.add("bunch of times (propety nSmooth), and stores the smoothed values in attribute");
		comments.add("indexes 1 - nSmooth.  The app also outputs a vtk file which can be viewed in ");
		comments.add("ParaView.  The user should run the app in smoothed_max_depth mode a bunch of times,");
		comments.add("modifying smoothing properties gridResolution and nSmooth and identify property ");
		comments.add("values that give optimal results.");
		comments.add("");
		comments.add("Then the user should run the app in seismicity_depth_model mode, setting property");
		comments.add("nSmooth to the desired level of smoothing.  The geotess model produced when the ");
		comments.add("app was run in smoothed_max_depth mode will be queried for the value of ");
		comments.add("seismicity_depth_max at each grid node.  Note the the gridResolution can be ");
		comments.add("different when the app is run in the two different modes.  ");
		comments.add("A topography model is queried for the value of seismicity_depth_min.");
		comments.add("");
		comments.add("Here is sample properties file for running in smoothed_max_depth mode:");
		comments.add("");
		comments.add("////////////////////////////////////////////////////////////////////////////////");
		comments.add("");
		comments.add("mode = smoothed_max_depth");
		comments.add("");
		comments.add("# the size of the triangles used to determine seismicity_depth_max");
		comments.add("gridResolution = 2");
		comments.add("");
		comments.add("# how many smoothed representations to produce");
		comments.add("nSmooth = 100");
		comments.add("");
		comments.add("# the results will be written to this output file");
		comments.add("smoothedMaxDepthModel = /Users/sballar/Documents/locoo3d/seismicity_depth/new_model/smoothed_max_depth.geotess");
		comments.add("");
		comments.add("# vtk file with smoothed results");
		comments.add("vtkFile = /Users/sballar/Documents/locoo3d/seismicity_depth/new_model/smoothed_max_depth.vtk");
		comments.add("");
		comments.add("# where to center the map in the vtk file");
		comments.add("vtkCenterLon = 162");
		comments.add("");
		comments.add("# this sql query must produce 3 values for each origin in the database:");
		comments.add("# origin_latitude, origin_longitude, origin_depth");
		comments.add("sqlQuery = select lat, lon, least(700., depth+greatest(0.,2*sdepth)) \\");
		comments.add("from gnem_idcreb.origin o, gnem_idcreb.origerr oe \\");
		comments.add("where o.orid=oe.orid and depth+greatest(0,2*sdepth) > 0");
		comments.add("");
		comments.add("# database connection parameters:");
		comments.add("dbInputInstance = jdbc:oracle:thin:@dwpr2.sandia.gov:1523:dwpr2");
		comments.add("dbInputUserName = GNEM_SBALLAR");
		comments.add("dbInputPassword = xxxxxx");
		comments.add("dbInputDriver = oracle.jdbc.driver.OracleDriver");
		comments.add("");
		comments.add("////////////////////////////////////////////////////////////////////////////////");
		comments.add("");
		comments.add("and here is a sample properties file for running in seismicity_depth_model mode:");
		comments.add("");
		comments.add("////////////////////////////////////////////////////////////////////////////////");
		comments.add("");
		comments.add("mode = seismicity_depth_model");
		comments.add("");
		comments.add("# the size of the triangles in the grid");
		comments.add("gridResolution = 1");
		comments.add("");
		comments.add("# the selected smoothing parameter");
		comments.add("nSmooth = 20");
		comments.add("");
		comments.add("# the minimum value of seismicity_depth_max will be this value, in km");
		comments.add("minimumMaxDepth = 50");
		comments.add("");
		comments.add("# the geotess model produced by properties file smoothed_max_depth.properties");
		comments.add("smoothedMaxDepthModel = /Users/sballar/Documents/locoo3d/seismicity_depth/new_model/smoothed_max_depth.geotess");
		comments.add("");
		comments.add("# topography model used to deduce seismicity_depth_min");
		comments.add("topographyModel = /Users/sballar/Documents/etopo1/etopo1_ice_g_i2.zip");
		comments.add("");
		comments.add("# output file");
		comments.add("outputModel = /Users/sballar/Documents/locoo3d/seismicity_depth/new_model/seismicity_depth_v2.geotess");
		comments.add("");
		comments.add("vtkFile = /Users/sballar/Documents/locoo3d/seismicity_depth/new_model/seismicity_depth_v2.vtk");
		comments.add("");
		comments.add("# where to center the map in the vtk file");
		comments.add("vtkCenterLon = 162");
		comments.add("");
		comments.add("");
		comments.add("////////////////////////////////////////////////////////////////////////////////");
		comments.add("");
		comments.add("and here is a sample properties file for running in resample mode:");
		comments.add("");
		comments.add("////////////////////////////////////////////////////////////////////////////////");
		comments.add("");
		comments.add("mode = seismicity_depth_resample");
		comments.add("");
		comments.add("# specify a bunch of properties that will control construction of a ");
		comments.add("# new GeoTessGrid which will be the grid upon which the new ");
		comments.add("# seismicity_depth model will be built.  See the GeoTess User's Manual");
		comments.add("# for information about these properties and others.");
		comments.add("initialSolid = tetrahexahedron");
		comments.add("");
		comments.add("rotateGrid = 39.5 -111.5");
		comments.add("");
		comments.add("baseEdgeLengths = 2");
		comments.add("");
		comments.add("polygons = /Users/sballar/Documents/locoo3d/seismicity_depth/UtahBorder.polygon, 0, 1; \\");
		comments.add("/Users/sballar/Documents/locoo3d/seismicity_depth/WasatchFront.polygon, 0, 0.03125");
		comments.add("");
		comments.add("# Now specify the topography model that will be used to deduce seismicity_depth_min.  ");
		comments.add("# This can be a previously computed seismicity_depth.geotess model, or it can be ");
		comments.add("# the etopo1 model.");
		comments.add("topographyModel = /Users/sballar/Documents/etopo1/etopo1_ice_g_i2.zip");
		comments.add("");
		comments.add("");
		comments.add("# Now specify information about how to compute seismicity_depth_min.");
		comments.add("# This can be either a constant depth, or it can be a previously computed");
		comments.add("# seismicity_depth.geotess model.  If you specify both, the code will throw");
		comments.add("# an exception.");
		comments.add("");
		comments.add("#constantMaxDepth = 35");
		comments.add("");
		comments.add("inputMaxDepthModel = /Users/sballar/Documents/locoo3d/seismicity_depth/new_model/seismicity_depth_sm100.geotess");
		comments.add("");
		comments.add("# the minimum value of seismicity_depth_max will be this value.");
		comments.add("# Ignored if constantMaxDepth is specified.");
		comments.add("minimumMaxDepth = 50");
		comments.add("");
		comments.add("# output file");
		comments.add("outputModel = /Users/sballar/Documents/locoo3d/seismicity_depth/new_model/seismicity_depth_resampled.geotess");
		comments.add("");
		comments.add("vtkFile = /Users/sballar/Documents/locoo3d/seismicity_depth/new_model/seismicity_depth_resampled.vtk");
		comments.add("");
		comments.add("vtkCenterLon = -111.5");

		for (String s : comments)
			System.out.println(s);
	}


	public void seismicityDepthResample(PropertiesPlus properties) throws Exception, GeoTessException, IOException {

		if (!(properties.containsKey("constantMaxDepth") 
				^ properties.containsKey("inputMaxDepthModel")))
			throw new Exception("Properties must contain one of 'constantMaxDepth' or 'inputMaxDepthModel' "
					+"but not both.");

		GeoTessPosition inputMaxDepthModel = null;
		float constantMaxDepth = properties.getFloat("constantMaxDepth", Float.NaN);
		if (Float.isNaN(constantMaxDepth))
			inputMaxDepthModel = new GeoTessModel(properties.getFile("inputMaxDepthModel"))
			.getGeoTessPosition();


		// retrieve value for the minimum value of seismicity_depth_max.  Values less than this value
		// will be replaced.
		float minimumMaxDepth = properties.getFloat("minimumMaxDepth", -1000000f);

		// retrieve vtkFile from properties then remove it from properties so that 
		// GridBuilderMain won't use it.  
		File vtkFile = properties.getFile("vtkFile");
		properties.remove("vtkFile");

		properties.setProperty("gridConstructionMode = scratch");

		// build the grid
		GeoTessGrid grid = (GeoTessGrid)GeoTessBuilderMain.run(properties);

		// Create a MetaData object in which we can specify information needed for model construction.
		GeoTessMetaData metaData = new GeoTessMetaData();

		// Specify a description of the model. This information is not
		// processed in any way by GeoTess. It is carried around for information purposes.
		metaData.setDescription(String
				.format("Seismicity Depth Model constructed with the following properties:\n\n"
						+ properties.toString()));

		// Specify a list of layer names. 
		metaData.setLayerNames("seismicity_depth");

		// specify the DataType for the data. All attributes, in all
		// profiles, will have the same data type.
		metaData.setDataType(DataType.FLOAT);

		// specify the name of the software that is going to generate
		// the model.  This gets stored in the model for future reference.
		metaData.setModelSoftwareVersion(this.getClass().getCanonicalName());

		// specify the date when the model was generated.  This gets 
		// stored in the model for future reference.
		metaData.setModelGenerationDate(new Date().toString());

		// specify the names of the attributes and the units of the
		// attributes in two String arrays. 
		metaData.setAttributes("SEISMICITY_DEPTH_MIN; SEISMICITY_DEPTH_MAX", "km;km");

		// call a GeoTessModel constructor to build the model. This will
		// load the grid, and initialize all the data structures to null.
		// To be useful, we will have to populate the data structures.
		GeoTessModel model = new GeoTessModel(grid, metaData);

		// populate the model with topography and smoothed_max_depth values
		// interpolated from the input models.
		if (!Double.isNaN(constantMaxDepth))
			for (int vtx = 0; vtx < model.getNVertices(); ++vtx)
				model.setProfile(vtx, Data.getDataFloat(Float.NaN, constantMaxDepth));
		else
			for (int vtx = 0; vtx < model.getNVertices(); ++vtx)
			{
				// if (outside.contains(vtx)) model.setProfile(vtx); else 
				if (!Double.isNaN(constantMaxDepth))
					model.setProfile(vtx, Data.getDataFloat(Float.NaN, constantMaxDepth));
				else
					model.setProfile(vtx, Data.getDataFloat(Float.NaN, (float)Math.max(minimumMaxDepth, 
							inputMaxDepthModel.set(model.getVertex(vtx), 1e4).getValue(1))));
			}

		System.out.println("Populating model with topography data...");

		File topoFile = properties.getFile("topographyModel");
		if (GeoTessModel.isGeoTessModel(topoFile))
			populateTopographyGeoTess(topoFile,  model, 0);
		else
			populateTopographyEtopo1(topoFile, model, 0);

		System.out.println();
		System.out.println(model);

		System.out.println(GeoTessModelUtils.statistics(model));

		// write the results to a vtk file for viewing
		if (vtkFile != null)
			GeoTessModelUtils.vtkRobinson(model, vtkFile, 
					properties.getDouble("vtkCenterLon", 162), 
					0., 0, true, InterpolatorType.LINEAR, false, null);

		// write the seismicity_depth_model to file.
		model.writeModel(properties.getFile("outputModel"));

	}

	public void seismicityDepthModel(PropertiesPlus properties) throws Exception
	{
		// retrieve vtkFile from properties then remove it from properties so that 
		// GridBuilderMain won't use it.  
		File vtkFile = properties.getFile("vtkFile");
		properties.remove("vtkFile");

		// load the max depth model produced by running this app in smoothed_max_depth mode
		GeoTessPosition maxDepthModel = new GeoTessModel(properties.getFile("smoothedMaxDepthModel"))
				.getGeoTessPosition();

		// the maxDepthModel contains many values of seismicity_depth_max with different amount of smoothing.
		// Specify which one to copy into the seismicity_depth_model.
		int nSmooth = properties.getInt("nSmooth");

		// figure out which model attribute to copy into the seismicity_depth_model
		int maxDepthAttribute = maxDepthModel.getModel().getMetaData().getAttributeIndex(
				String.format("%03d", nSmooth));

		// retrieve value for the minimum value of seismicity_depth_max.  Values less than this value
		// will be replaced.
		double minimumMaxDepth = properties.getDouble("minimumMaxDepth");

		double gridResolution = properties.getDouble("gridResolution");
		// build a uniform grid with the specified triangle edge length in degrees.  
		GeoTessGrid grid = GeoTessBuilderMain.getGrid(gridResolution);

		// Create a MetaData object in which we can specify information needed for model construction.
		GeoTessMetaData metaData = new GeoTessMetaData();

		// Specify a description of the model. This information is not
		// processed in any way by GeoTess. It is carried around for information purposes.
		metaData.setDescription(String
				.format("Seismicity Depth Model constructed with the following properties:\n\n"
						+ properties.toString()));

		// Specify a list of layer names. 
		metaData.setLayerNames("seismicity_depth");

		// specify the DataType for the data. All attributes, in all
		// profiles, will have the same data type.
		metaData.setDataType(DataType.FLOAT);

		// specify the name of the software that is going to generate
		// the model.  This gets stored in the model for future reference.
		metaData.setModelSoftwareVersion(this.getClass().getCanonicalName());

		// specify the date when the model was generated.  This gets 
		// stored in the model for future reference.
		metaData.setModelGenerationDate(new Date().toString());

		// specify the names of the attributes and the units of the
		// attributes in two String arrays. 
		metaData.setAttributes("SEISMICITY_DEPTH_MIN; SEISMICITY_DEPTH_MAX", "km;km");

		// call a GeoTessModel constructor to build the model. This will
		// load the grid, and initialize all the data structures to null.
		// To be useful, we will have to populate the data structures.
		GeoTessModel model = new GeoTessModel(grid, metaData);

		// populate the model with topography and smoothed_max_depth values
		// interpolated from the input models.
		for (int vtx = 0; vtx < model.getNVertices(); ++vtx)
		{
			// set the geographic locations in the two input models.
			maxDepthModel.set(model.getVertex(vtx), 1.);

			// retrieve interpolated values and put them in the model.
			// topography is set to NaN and computed later.
			model.setProfile(vtx, Data.getDataFloat(Float.NaN,
					(float)Math.max(minimumMaxDepth, maxDepthModel.getValue(maxDepthAttribute))
					));
		}

		System.out.println("Populating model with topography data...");

		File topoFile = properties.getFile("topographyModel");
		if (GeoTessModel.isGeoTessModel(topoFile))
			populateTopographyGeoTess(topoFile,  model, 0);
		else 
			populateTopographyEtopo1(topoFile, model, 0);

		System.out.println();
		System.out.println(model);

		System.out.println(GeoTessModelUtils.statistics(model));

		// write the results to a vtk file for viewing
		if (vtkFile != null)
			GeoTessModelUtils.vtkRobinson(model, vtkFile, 
					properties.getDouble("vtkCenterLon", 162), 
					0., 0, true, InterpolatorType.LINEAR, false, null);

		// write the seismicity_depth_model to file.
		model.writeModel(properties.getFile("outputModel"));

	}

	public void smoothedMaxDepth(PropertiesPlus properties) throws Exception
	{
		// retrieve vtkFile from properties then remove it from properties so that 
		// GridBuilderMain won't use it.  
		File vtkFile = properties.getFile("vtkFile");
		properties.remove("vtkFile");

		// build a uniform grid with the specified triangle edge length in degrees.  
		GeoTessGrid grid = GeoTessBuilderMain.getGrid(properties.getDouble("gridResolution"));

		// Create a MetaData object in which we can specify information
		// needed for model construction.
		GeoTessMetaData metaData = new GeoTessMetaData();

		// Specify a description of the model. This information is not
		// processed in any way by GeoTess. It is carried around for
		// information purposes.
		metaData.setDescription(String
				.format("Smoothed Max Depth model constructed with the following properties:\n"
						+ properties.toString()));

		// Specify a list of layer names. 
		metaData.setLayerNames("seismicity_depth");

		// specify the DataType for the data. All attributes, in all
		// profiles, will have the same data type.
		metaData.setDataType(DataType.FLOAT);

		// specify the name of the software that is going to generate
		// the model.  This gets stored in the model for future reference.
		metaData.setModelSoftwareVersion(this.getClass().getCanonicalName());

		// specify the date when the model was generated.  This gets 
		// stored in the model for future reference.
		metaData.setModelGenerationDate(new Date().toString());

		// retrieve number of smoothing iterations to apply
		int nSmooth = properties.getInt("nSmooth");

		// make an attribute for the raw data and for each smoothing level
		String[] attributes = new String[nSmooth+1];
		for (int i=0; i<=nSmooth; ++i)
			attributes[i] = String.format("%03d", i);

		String[] units = new String[attributes.length];
		Arrays.fill(units, "km");

		// specify the names of the attributes and the units of the
		// attributes in two String arrays. 
		metaData.setAttributes(attributes, units);

		// call a GeoTessModel constructor to build the model. This will
		// load the grid, and initialize all the data structures to null.
		// To be useful, we will have to populate the data structures.
		GeoTessModel model = new GeoTessModel(grid, metaData);

		// Initialize the model data values to zero.
		for (int vtx = 0; vtx < model.getGrid().getNVertices(); ++vtx)
			model.setProfile(vtx, Data.getDataDouble(new double[model.getNAttributes()]));

		// retrieve value for the minimum value of seismicity_depth_max.  
		double minimumMaxDepth = properties.getDouble("minimumMaxDepth", 0.);
		if (minimumMaxDepth > 0.)
			for (int i=0; i<grid.getNVertices(); ++i)
				model.setValue(i, 0, minimumMaxDepth);

		// get a GeoTessPosition object which will be used to figure out which triangle
		// a given origin is located in.
		GeoTessPosition pos = model.getGeoTessPosition();

		// build database connection and statement.
		Schema schema = new Schema("dbInput", properties, false);
		System.out.println(schema);

		Statement statement = schema.getStatement();

		// retrieve the sql query that will be used to retrieve origin information.
		// query must return 3 values: origin_latitude in degrees, origin_longitude in degrees,
		// origin depth in km.
		String sqlQuery = properties.getProperty("sqlQuery");
		System.out.println(sqlQuery);

		// execute the database query
		boolean ok = statement.execute(sqlQuery);
		if (!ok)
			throw new Exception("Execution of sqlQuery failed\n"+properties.getProperty("sqlQuery"));

		// retrieve the result set containing the results of the query.
		ResultSet resultSet = statement.getResultSet();

		// instantiate some variables
		int triangleIndex, vertex;
		double lat, lon, depth, maxDepth;
		double[] u;

		// iterate over the results of the query.
		while (resultSet.next())
		{
			lat = resultSet.getDouble(1);
			lon = resultSet.getDouble(2);
			depth = resultSet.getDouble(3);

			// convert lat, lon to unit vector.
			u = VectorGeo.getVectorDegrees(lat, lon);

			// set the location of the origin in the GeoTessPosition object
			pos.set(u, 1.);

			// retrieve the index of the triangle in which the origin is located.
			triangleIndex = pos.getTriangle();

			// for each of the 3 vertices at the corners of the triangle
			for (int i=0; i<3; ++i)
			{
				// get the index of the vertex at the corner of the triangle
				vertex = grid.getTriangleVertexIndex(triangleIndex, i);

				// get the current value of max depth at the vertex
				maxDepth = model.getValueDouble(vertex, 0, 0, 0);

				// if origin depth is > current value of maxDepth stored at the vertex
				// replace current value.
				if (depth > maxDepth)
					model.setValue(vertex, 0, 0, 0, depth);
			}
		}

		// shut down the database connection
		statement.close();
		schema.close();

		// at this point, model has number of attributes equal to 1 + nSmooth.
		// only attribute 0 is populated and it contains the unsmoothed value
		// of maximum origin depth.

		// SMOOTHING

		// weight assigned to central vertex vs weight assigned to surrounding vertices.
		double smoothingFactor = properties.getDouble("smoothingFactor", 0.5);

		// initialize an array of size equal to number of vertices, used as temporary 
		// work space.
		double[] temp = new double[grid.getNVertices()];

		// perform loop nSmooth times
		for (int iSmooth=1; iSmooth<=nSmooth; ++iSmooth)
		{
			// iterate over all the vertices
			for (int vtx=0; vtx<grid.getNVertices(); ++vtx)
			{
				// find the indexes of the vertices that are connected to the current vertex by 
				// a single triangle edge,  There is almost always 6 of these neighbors.  In 12 cases,
				// (vertices of the icosahedron) there are only 5.
				HashSet<Integer> neighbors = grid.getVertexNeighbors(0, grid.getNLevels(0)-1, vtx);

				// set the temporary value of maxDepth to value at the current vertex, 
				// multiplied by smoothingFactor.
				temp[vtx] = model.getValueDouble(vtx, 0, 0, iSmooth-1)*smoothingFactor;

				// for each neighbor, add the value of maxDepth at the neighbor multiplied by
				// (1-smoothingFactor) and divided by the number of neighbors.
				for (Integer i : neighbors)
					temp[vtx] += model.getValueDouble(i, 0, 0, iSmooth-1)*(1-smoothingFactor)/neighbors.size();
			}

			// smoothed values of maxDepth are now stored in temp array.
			// We will now copy the smoothed values into the model at the appropriate attribute index.
			// NOTE: values are not replaced if the smoothed value is less than the raw data.
			// iterate over the vertices again.
			for (int vtx=0; vtx<grid.getNVertices(); ++vtx)
			{
				// if the smoothed value of maxDepth is > then the unsmoothed value
				// replace the current value with the smoothed value.
				if (temp[vtx] > model.getValueDouble(vtx, 0, 0, 0))
					model.setValue(vtx, 0, 0, iSmooth, temp[vtx]);
				else
					// otherwise, if the smoothed value is < the raw value, simply copy the 
					// previous value into the new value.
					model.setValue(vtx, 0, 0, iSmooth, model.getValueDouble(vtx, 0, 0, iSmooth-1));
			}
		}

		// write the results to a vtk file.
		if (vtkFile != null)
			GeoTessModelUtils.vtkRobinson(model, vtkFile, 
					properties.getDouble("vtkCenterLon", 162.), 
					0., 0, true, InterpolatorType.LINEAR, false, null);

		// write the populated model to file.
		model.writeModel(properties.getFile("smoothedMaxDepthModel"));
	}

	/**
	 * Populate the model with topography values.  The model values at supplied attributeIndex
	 * are replaced with topography values.
	 * @param etopo1File
	 * @param model
	 * @param attributeIndex
	 * @throws Exception
	 */
	private void populateTopographyGeoTess(File topoFile, GeoTessModel model, int attributeIndex) throws Exception
	{
		GeoTessModel topoModel = new GeoTessModel(topoFile);
		GeoTessPosition topo = topoModel.getGeoTessPosition();

		double factor = 1.0;
		int topoAttribute = topoModel.getMetaData().getAttributeIndex("Topography");
		if (topoAttribute >= 0)
			factor = topoModel.getMetaData().getAttributeUnit(topoAttribute).equalsIgnoreCase("meters") ? 
					-1e-3 : -1.;
		else
		{
			topoAttribute = topoModel.getMetaData().getAttributeIndex("SEISMICITY_DEPTH_MIN");
			if (topoAttribute >= 0)
				factor = topoModel.getMetaData().getAttributeUnit(topoAttribute).equalsIgnoreCase("meters") ? 
						-1e-3 : 1.;
			else
				throw new Exception("Could not find an attribute with name 'Topography' or 'SEISMICITY_DEPTH_MIN' in model"
						+topoFile.getAbsolutePath());
		}

		// interpolate values from the topoModel. Convert to depth in km if necessary.
		PointMap map = model.getPointMap();
		for (int i=0; i<map.size(); ++i)
		{
			topo.set(map.getPointUnitVector(i), 1e4);
			map.setPointValue(i, attributeIndex, topo.getValue(topoAttribute)*factor);
		}

	}

	/**
	 * Populate the model with topography values.  The model values at supplied attributeIndex
	 * are replaced with topography values.
	 * @param etopo1File
	 * @param model
	 * @param attributeIndex
	 * @throws Exception
	 */
	private void populateTopographyEtopo1(File etopo1File, GeoTessModel model, int attributeIndex) throws Exception
	{
		short[][] topo = loadEtopo1(etopo1File);
		int nlat = topo.length;
		int nlon = topo[0].length;

		GeoTessPosition x = model.getGeoTessPosition(InterpolatorType.LINEAR);

		double lat, lon;
		short z;
		int[] vertices;
		double[] coef;

		double[] sum = new double[model.getNVertices()];
		double[] weight = new double[model.getNVertices()];

		for (int i = 0; i < nlat; ++i)
		{
			lat = (i/60.)-90.;

			if (i % 60 == 0)
				System.out.printf("lat = %6.2f%n", lat);

			short[] topo_i = topo[i];
			for (int j = 0; j < nlon; ++j)
			{
				lon = (j/60.)-180.;

				x.set(lat, lon, 0.);
				vertices = x.getVertices();
				coef = x.getHorizontalCoefficients();

				z = topo_i[j];
				for (int k = 0; k < vertices.length; ++k)
				{
					sum[vertices[k]] += z * coef[k];
					weight[vertices[k]] += coef[k];
				}
			}
		}

		// convert elevation in meters to depth in km.
		for (int v = 0; v < model.getGrid().getNVertices(); ++v)
			if (model.getProfile(v, 0).getType() != ProfileType.SURFACE_EMPTY)
			{
				if (weight[v] > 3.)
					model.getProfile(v, 0).getData()[0].setValue(attributeIndex, 
							-1e-3*Math.round(sum[v]/weight[v]));
				else
					model.getProfile(v, 0).getData()[0].setValue(attributeIndex, 
							-1e-3*Math.round(interpolateEtopo1(topo, model.getVertex(v))));
			}

	}

	private double interpolateEtopo1(short[][] topo, double[] u) throws Exception
	{
		double dnorth = (topo.length-1.0) / 180.0;
		double deast = (topo[0].length-1.0) / 360.0;

		// find distance north of south pole, in minutes.
		double north = (VectorGeo.getLatDegrees(u)+90)*dnorth;
		// find distance east of international date line, in minutes.
		double east = (VectorGeo.getLonDegrees(u)+180.)*deast;

		int x = (int) Math.floor(east);
		if (x == topo[0].length-1) 
			--x;
		east -= x;

		int y = (int) Math.floor(north);
		if (y == topo.length-1) 
			--y;
		north -= y;

		return topo[y][x] * (1.-east)*(1.-north) + 
				topo[y][x+1] * east*(1.-north) + 
				topo[y+1][x] * (1.-east)*north + 
				topo[y+1][x+1] * east*north; 

	}

	private short[][] loadEtopo1(File etopo1File) throws Exception
	{
		long timer = System.currentTimeMillis();

		System.out.println("Loading "+etopo1File.getCanonicalPath());

		int nlon = -1;
		int nlat = -1;
		short[][] topo = null;

		if (etopo1File.getName().toLowerCase().endsWith("zip"))
		{
			// ASSUMPTION: the name of the .bin file inside the zip file
			// has the same name as the zip file with extension 'bin'.
			String name = etopo1File.getName();
			name = name.substring(0, name.length()-3);

			ZipFile zipFile = new ZipFile(etopo1File);
			ZipEntry zipEntry = zipFile.getEntry(name+"hdr");
			Scanner hdr = new Scanner(zipFile.getInputStream(zipEntry));
			while (hdr.hasNext())
			{
				String key = hdr.next();
				String value = hdr.next();
				if (key.toLowerCase().equals("ncols"))
					nlon = Integer.parseInt(value);
				if (key.toLowerCase().equals("nrows"))
					nlat = Integer.parseInt(value);
			}
			hdr.close();

			topo = new short[nlat][nlon];

			zipEntry = zipFile.getEntry(name+"bin");
			DataInputStream input = new DataInputStream(new BufferedInputStream(
					zipFile.getInputStream(zipEntry)));

			for (int i=nlat-1; i>=0; --i)
			{
				short[] z = topo[i];
				for (int j=0; j<nlon; ++j)
					z[j] = Short.reverseBytes(input.readShort());
			}
			input.close();
			zipFile.close();
		}
		else
		{
			DataInputStream input = new DataInputStream(
			    GlobalInputStreamProvider.forFiles().newStream(etopo1File));

			nlon = 21601;
			nlat = 10801;
			topo = new short[nlat][nlon];
			for (int i=nlat-1; i>=0; --i)
			{
				short[] z = topo[i];
				for (int j=0; j<nlon; ++j)
					z[j] = Short.reverseBytes(input.readShort());
			}
			input.close();
		}

		System.out.println("Model loaded. "+Globals.elapsedTime(timer));
		return topo;
	}

}
