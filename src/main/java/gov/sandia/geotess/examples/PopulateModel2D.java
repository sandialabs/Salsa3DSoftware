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
package gov.sandia.geotess.examples;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import gov.sandia.geotess.Data;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessModelUtils;
import gov.sandia.geotessbuilder.GeoTessBuilderMain;
import gov.sandia.gmp.util.globals.DataType;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * An example of how to generate a 2D GeoTessModel and populate it with data.
 * 
 * @author Sandy Ballard (sballar@sandia.gov)
 * 
 */
public class PopulateModel2D
{
    public static void main(String[] args) {
	try
	{
	    // Must supply a single command line argument that specifies location of a 
	    // properties file that contains the following properties:

	    //	    # triangle edge length in the background, 
	    //	    # outside the high resolution region
	    //	    baseEdgeLengths = 64
	    //
	    //	    # latitude in degrees of center of high resolution region
	    //	    smallCircleCenterLat = 35
	    //
	    //	    # longitude in degrees of center of high resolution region
	    //	    smallCircleCenterLon = -107
	    //
	    //	    # radius in degrees of high resolution region
	    //	    smallCircleRadius = 18.0
	    //
	    //	    # triangle edge length inside high resolution region (degrees)
	    //	    smallCircleResolution = 1.0
	    //
	    //	    # specify a location to which to write the model.
	    //	    #outputFile = 
	    //
	    //	    # vtk file that can be loaded into Paraview to visualize the model.
	    //	    # (https://www.paraview.org)
	    //	    vtkFileModel = /Users/sballar/Desktop/model.vtk
	    //
	    //	    # vtk file that be loaded into Paraview to visualize the grid.
	    //	    # (https://www.paraview.org)
	    //	    vtkFileGrid = /Users/sballar/Desktop/grid.vtk

	    // load the properties file specified on the command line
	    PropertiesPlus properties = new PropertiesPlus(new File(args[0]));

	    // instantiate a new PopulateModel2D2 object.
	    PopulateModel2D populator = new PopulateModel2D();

	    // retrieve a GeoTessMetaData object.  Method needs extensive review.
	    GeoTessMetaData metaData = populator.getMetaData(properties);

	    // generate a GeoTessGrid object based on properties specified in the properties file. 
	    GeoTessGrid grid = populator.getGrid(properties);

	    // generate a new GeoTessModel object based in part on properties
	    // in the properties file.  Calls method getAttributeValues(lat, lon, inRange)
	    // that definitely needs to be completely replaced.
	    GeoTessModel model = populator.getModel(properties, metaData, grid);

	    // print some information about the model and grid to the screen
	    System.out.println(model);

	    // print some statistics about the attribute values.
	    System.out.println(GeoTessModelUtils.statistics(model));

	    // write the model (including the metaData and the grid) to output file.
	    String outputFile = properties.getProperty("outputFile");
	    if (outputFile != null)
		model.writeModel(new File(outputFile));

	    // if you have Paraview installed (https://www.paraview.org)
	    // this command will write out a vtk file that can be loaded into
	    // ParaView to visualize the model.
	    if (properties.containsKey("vtkFileModel"))
		GeoTessModelUtils.vtk(model, properties.getProperty("vtkFileModel"), 0, false, null);

	    System.out.println("Done.");
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}
    }

    /**
     * The version number of this software
     * @return
     */
    private String getVersion() { return "0.0.1"; }

    /**
     * Generate a GeoTessGrid object based on properties in the properties file.
     * @param properties
     * @return
     * @throws Exception
     */
    private GeoTessGrid getGrid(PropertiesPlus properties) throws Exception {

	// build a new properties object that we can copy grid builder properties into.
	PropertiesPlus gridProperties = new PropertiesPlus();

	// copy relevant properties into gridProperties.
	gridProperties.setProperty("gridConstructionMode", "scratch");

	gridProperties.setProperty("nTessellations", 1);

	gridProperties.setProperty("verbosity", 0);

	if (properties.containsKey("vtkFileGrid"))
	    gridProperties.setProperty("vtkFile", properties.getProperty("vtkFileGrid"));

	gridProperties.setProperty("baseEdgeLengths", properties.getDouble("baseEdgeLengths"));

	// apply Euler rotations to the grid so that grid vertex 0 will reside at the center 
	// of the high resolution region.
	gridProperties.setProperty("rotateGrid", String.format("%s, %s", 
		properties.getDouble("smallCircleCenterLat"),
		properties.getDouble("smallCircleCenterLon")));


	gridProperties.setProperty("polygons", String.format("spherical_cap, %s, %s, %s, 0, %s",
		properties.getProperty("smallCircleCenterLat"),
		properties.getProperty("smallCircleCenterLon"),
		properties.getProperty("smallCircleRadius"),
		properties.getProperty("smallCircleResolution")));

	// call GridBuilderMain to generate a new GeoTessGrid.
	GeoTessGrid grid = (GeoTessGrid)GeoTessBuilderMain.run(gridProperties);

	return grid;
    }

    /**
     * Generate a GeoTessMetaData object.  This method needs review.
     * @param properties
     * @return
     * @throws IOException
     */
    private GeoTessMetaData getMetaData(PropertiesPlus properties) throws IOException {

	// Create a MetaData object in which we can specify information
	// needed for model contruction.
	GeoTessMetaData metaData = new GeoTessMetaData();

	// Specify a description of the model. This information is not
	// processed in any way by GeoTess. It is carried around for
	// information purposes.
	metaData.setDescription(String
		.format("Include a description of this model.%n"
			+ "If you were searching for this model in 10 years%n"
			+ "and found yourself perusing dozens of models looking for this one%n"
			+ "what would you want to find in this description?%n"
			+ "content generator: <enter name here> (enter email here)%n"
			+ "software engineer: <enter name here> (enter email here)%n"));

	// Specify a list of layer names. A model could have many layers,
	// e.g., ("core", "mantle", "crust"), specified in order of
	// increasing radius. For a 2D model, layer name 'surface' is a good choice but
	// can be anything.
	metaData.setLayerNames("surface");

	// specify the names of the attributes and the units of the attributes in two String arrays. 
	// If this model has multiple attributes, they must be delimited with semicolons.
	// Give these attributes meaningful names and appropriate units.
	// For unitless attributes, specify a space (e.g. " ; ");
	metaData.setAttributes("ATTRIBUTE_1; ATTRIBUTE_2", "furlongs; fortnights");

	// specify the DataType for the data. All attributes will have the same data type
	// throughout the model.
	metaData.setDataType(DataType.FLOAT);

	// specify the name of the software that is generating the model (this software).  
	// This gets stored in the model for future reference.
	metaData.setModelSoftwareVersion(this.getClass().getCanonicalName()+"."+getVersion());

	// specify the date when the model was generated.  This gets 
	// stored in the model for future reference.
	metaData.setModelGenerationDate(new Date().toString());

	return metaData;
    }


    /**
     * Generate a GeoTessModel object based on properties, metaData and grid.  This method calls
     * method getAttributeValues(lat, lon, inRange) that must be rewritten for each application.
     * @param properties
     * @param metaData
     * @param grid
     * @return
     * @throws Exception
     */
    private GeoTessModel getModel(PropertiesPlus properties, GeoTessMetaData metaData, GeoTessGrid grid) throws Exception {
	// call a GeoTessModel constructor to build the model. This will initialize 
	// all the data structures to null. To be useful, we will have to populate the data structures.
	GeoTessModel model = new GeoTessModel(grid, metaData);

	// we will populate our model with random numbers inside the high resolution polygon
	// and NaNs outside the high resolution region

	// retrieve a unit vector at the center of the high resolution region
	double[] center = VectorGeo.getVectorDegrees(
		properties.getDouble("smallCircleCenterLat"), 
		properties.getDouble("smallCircleCenterLon"));

	// retrieve the radius of the high resolution region
	double smallCircleRadius = properties.getDouble("smallCircleRadius");

	// Iterate over every vertex in the grid and get the values of the 
	// attributes at the vertex location.  Stuff those values into the model.
	for (int vtx = 0; vtx < model.getGrid().getNVertices(); ++vtx)
	{
	    // retrieve the unit vector corresponding to this vertex of the grid.
	    double[] vertex = model.getVertex(vtx);

	    // get the latitude of the grid vertex in degrees
	    double lat = VectorGeo.getLatDegrees(vertex);

	    // get the longitude of the grid vertex in degrees
	    double lon = VectorGeo.getLonDegrees(vertex);

	    // find the distance in degrees from vertex to the center of the high resolution
	    // region.  inRange is true if the distance is < smallCircleRadius
	    boolean inRange = VectorGeo.angleDegrees(center, vertex) < smallCircleRadius;

	    // get the values of all the attribute values at the location of 
	    // this grid vertex.  attributeValues.length must be equal to the 
	    // number of attributes specified in the GeoTessMetaData object.
	    // Applications will want to rewrite method getAttributeValues().
	    float[] attributeValues = getAttributeValues(lat, lon, inRange);

	    // Construct a new Data object that holds the attribute values for 
	    // the current location in the model.
	    Data data = Data.getDataFloat(attributeValues);

	    // associate the Data object with the specified vertex of the model.  
	    // This instance of setProfile always creates a ProfileSurface object.
	    model.setProfile(vtx, data);
	}
	return model;
    }

    /**
     * Authors of this application must supply a method that generates the values of all 
     * attributes at the specified latitude, longitude location
     * @param lat in degrees where attribute values are being requested
     * @param lon in degrees where attribute values are being requested
     * @param inRange if true, valid values will be returned, otherwise NaNs
     * @return an array of floats of length equal to number of attributes specified 
     * in the metaData object.
     * @throws Exception 
     */
    private float[] getAttributeValues(double lat, double lon, boolean inRange) throws Exception {
	float[] attributeValues = new float[2];
	// this dumb example returns random numbers when inRange, and NaNs otherwise.
	if (inRange) {
	    attributeValues[0] = (float) Math.random();
	    attributeValues[1] = (float) Math.random();
	}
	else {
	    attributeValues[0] = Float.NaN;
	    attributeValues[1] = Float.NaN;	    
	}
	return attributeValues;
    }


}
