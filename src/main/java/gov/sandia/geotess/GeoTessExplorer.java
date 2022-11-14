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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.geotess.extensions.rstt.GeoTessModelSLBM;
import gov.sandia.geotess.extensions.rstt.Uncertainty;
import gov.sandia.geotess.extensions.rstt.UncertaintyPDU;
import gov.sandia.geotess.extensions.siteterms.GeoTessModelSiteData;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.globals.DataType;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.polygon.Polygon;
import gov.sandia.gmp.util.numerical.polygon.PolygonGlobal;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

/**
 * A command line driven set of utilities that can be used to extract maps,
 * vertical slices, boreholes and vtk plot files from aƒGeoTessModel. 
 *
 * Available functions include:
 * <ul>
 * <li>version              -- output the GeoTess version number
 * <li>toString             -- print summary information about a model
 * <li>updateModelDescription -- update the description information for a model
 * <li>statistics           -- print summary statistics about the data in a model
 * <li>extractGrid          -- load a model or grid and write its grid to stdout, vtk, kml, ascii or binary file
 * <li>resample             -- resample a model onto a new grid
 * <li>extractActiveNodes   -- load a model and extract the positions of all active nodes
 * <li>replaceAttributeValues -- replace the attribute values associated with all active nodes
 * <li>reformat             -- load a model and write it out in another format
 * <li>getValues            -- interpolate values at a single point
 * <li>getValuesFile        -- interpolate values at points specified in an ascii file
 * <li>interpolatePoint     -- interpolate values at a single point, verbose output
 * <li>borehole             -- interpolate values along a radial profile
 * <li>profile              -- extract model values at vertex closest to specified latitude, longitude position
 * <li>findClosestPoint     -- find the closest point to a supplied geographic location and return information about it
 * <li>slice                -- interpolate values on a vertical plane defined by a great circle
 * <li>sliceDistAz          -- interpolate values on a vertical plane defined by a great circle defined by a point, a distance and a direction
 * <li>mapValuesDepth       -- interpolate values on a lat, lon grid at constant depths
 * <li>mapValuesLayer       -- interpolate values on a lat, lon grid at fractional radius in a layer
 * <li>mapLayerBoundary     -- depth of layer boundaries on a lat, lon grid
 * <li>mapLayerThickness    -- layer thickness on a lat, lon grid
 * <li>values3DBlock        -- interpolate values on a regular lat, lon, radius grid
 * <li>function             -- new model with attributes calculated from two input models
 * <li>vtkLayers            -- generate vtk plot file of values at the tops of layers
 * <li>vtkDepths            -- generate vtk plot file of values at specified depths
 * <li>vtkLayerThickness    -- generate vtk plot file of layer thicknesses
 * <li>vtkLayerBoundary     -- generate vtk plot file of depth or elevation of layer boundaries
 * <li>vtkSlice             -- generate vtk plot file of vertical slice
 * <li>vtkSolid             -- generate vtk plot file of entire globe
 * <li>vtk3DBlock           -- generate vtk plot file of values on a lat-lon-depth grid
 * <li>vtkPoints            -- generate vtk plot of point data
 * <li>vtkRobinson          -- generate vtk plot of a Robinson projection of model data
 * <li>vtkRobinsonLayers    -- generate vtk plot of a Robinson projection of model data at tops of multiple layers
 * <li>vtkRobinsonPoints    -- generate vtk plot of a Robinson projection of point data
 * <li>vtkRobinsonTriangleSize -- generate vtk plot of triangle size on Robinson projection
 * <li>vtkLayerAverage		-- generate vtk plot of the average values within the crust of a designated model
 * <li>reciprocalModel		-- generates a reciprocal GeoTessModel, where all values are inverted
 * <li>renameLayer	    -- renames an individual layer in a GeoTessModel
 * <li>renameLayers	    -- renames all layers in a GeoTessModel
 * <li>getLatitudes         -- array of equally spaced latitude values
 * <li>getLongitudes        -- array of equally spaced longitude values
 * <li>getDistanceDegrees   -- array of equally spaced distances along a great circle
 * <li>translatePolygon     -- translate polygon between kml/kmz and ascii format
 * <li>extractSiteTerms     -- extract site terms from a GeoTessModelSiteData and print to screen
 * <li>replaceSiteTerms     -- replace site terms in a GeoTessModelSiteData with values loaded from a file
 * <li>getClassName         -- discover the class name of a specified model
 * <li>extractPathDependentUncertaintyRSTT -- extract all the path dependent uncertainty information from a GeoTessModelSLBM
 * <li>replacePathDependentUncertaintyRSTT -- replace all the path dependent uncertainty information in a GeoTessModelSLBM
 * </ul>
 * 
 * <p>For all options
 * except the vtk option, output is sent to standard out. The intention is that
 * users would either pipe the output to a file or insert the call to this
 * program into a script with the output piped to some other program.
 * <p>
 * If no arguments are supplied, a list of the recognized functions is
 * output. If the first argument is a recognized function but other required
 * arguments are missing, a list of the required arguments is output.
 * <p>
 * Many functions require a 'list of attributes'. This list can be a
 * string similar to '0,2,4-n', which would return attributes 0, 2 and 4
 * through the number of available attributes. 'n' would return only the
 * last attribute. 'all' and '0-n' would both return all attributes. 
 * 'none' would return no values at all. The list may not include any spaces.
 * <p>
 * For most functions, the first two arguments after the function name are the 
 * name of the input model file and the relative path to the grid directory.  
 * Some models have the grid stored in the same file with the model while other
 * models reference a grid stored in a separate file. If the grid is stored in the same file 
 * with the model, then the relative path to the grid directory is irrelevant
 * but something must be supplied in order to maintain the order of the argument
 * list.  If the grid is stored in a separate file then
 * the name of the file that contains the grid, without any directory information,
 * is stored in the model file.  When the model is loaded, it has to be told the 
 * relative path from the directory where the model is located to the directory 
 * where the grid file is located.  If the grid is in a separate file located in 
 * the same directory as the model file, provide the single character '.'.  
 * Note that models and grids also contain an MD5 hash of the grid file contents
 * so the danger of a model referencing the wrong grid is vanishingly small.
 * 
 * <p>
 * All the functions whose names start with 'vtk' extract information from a 
 * GeoTessModel and store it in a file in VTK format 
 * (http://www.vtk.org/VTK/img/file-formats.pdf).  These files can be 
 * visualized with free software called ParaView.  Visit http://www.paraview.org
 * for more information and downloads for various platforms.
 * 
 */
public class GeoTessExplorer
{

    /**
     * A command line driven set of utilities that can be used to extract maps,
     * vertical slices, boreholes and vtk plot files from a GeoTessModel. 
     *
     * Available functions include:
     * <ul>
     * <li>version              -- output the GeoTess version number
     * <li>toString             -- print summary information about a model
     * <li>updateModelDescription -- update the description information for a model
     * <li>statistics           -- print summary statistics about the data in a model
     * <li>extractGrid          -- load a model or grid and write its grid to stdout, vtk, kml, ascii or binary file
     * <li>resample             -- resample a model onto a new grid
     * <li>extractActiveNodes   -- load a model and extract the positions of all active nodes
     * <li>replaceAttributeValues -- replace the attribute values associated with all active nodes
     * <li>reformat             -- load a model and write it out in another format
     * <li>getValues            -- interpolate values at a single point
     * <li>getValuesFile        -- interpolate values at points specified in an ascii file
     * <li>interpolatePoint     -- interpolate values at a single point, verbose output
     * <li>borehole             -- interpolate values along a radial profile
     * <li>profile              -- extract model values at vertex closest to specified latitude, longitude position
     * <li>findClosestPoint     -- find the closest point to a supplied geographic location and return information about it
     * <li>slice                -- interpolate values on a vertical plane defined by a great circle
     * <li>sliceDistAz          -- interpolate values on a vertical plane defined by a great circle defined by a point, a distance and a direction
     * <li>mapValuesDepth       -- interpolate values on a lat, lon grid at constant depths
     * <li>mapValuesLayer       -- interpolate values on a lat, lon grid at fractional radius in a layer 
     * <li>mapLayerBoundary     -- depth of layer boundaries on a lat, lon grid
     * <li>mapLayerThickness    -- layer thickness on a lat, lon grid
     * <li>values3DBlock        -- interpolate values on a regular lat, lon, radius grid
     * <li>function             -- new model with attributes calculated from two input models
     * <li>vtkLayers            -- generate vtk plot file of values at the tops of layers
     * <li>vtkDepths            -- generate vtk plot file of values at specified depths
     * <li>vtkLayerThickness    -- generate vtk plot file of layer thicknesses
     * <li>vtkLayerBoundary     -- generate vtk plot file of depth or elevation of layer boundaries
     * <li>vtkSlice             -- generate vtk plot file of vertical slice
     * <li>vtkSolid             -- generate vtk plot file of entire globe
     * <li>vtk3DBlock           -- generate vtk plot file of values on a lat-lon-depth grid
     * <li>vtkPoints            -- generate vtk plot of point data
     * <li>vtkRobinson          -- generate vtk plot of a Robinson projection of model data
     * <li>vtkRobinsonLayers    -- generate vtk plot of a Robinson projection of model data at tops of multiple layers
     * <li>vtkRobinsonPoints    -- generate vtk plot of a Robinson projection of point data
     * <li>vtkRobinsonTriangleSize -- generate vtk plot of triangle size on Robinson projection
     * <li>vtkLayerAverage		-- generate vtk plot of the average values within the crust of a designated model
     * <li>reciprocalModel		-- generates a reciprocal GeoTessModel, where all values are inverted
     * <li>renameLayer	    -- renames an individual layer in a GeoTessModel
     * <li>renameLayers	    -- renames all layers in a GeoTessModel
     * <li>getLatitudes         -- array of equally spaced latitude values
     * <li>getLongitudes        -- array of equally spaced longitude values
     * <li>getDistanceDegrees   -- array of equally spaced distances along a great circle
     * <li>translatePolygon     -- translate polygon between kml/kmz and ascii format
     * <li>extractSiteTerms     -- extract site terms from a GeoTessModelSiteData and print to screen
     * <li>replaceSiteTerms     -- replace site terms in a GeoTessModelSiteData with values loaded from a file
     * <li>getClassName         -- discover the class name of a specified model
     * <li>extractPathDependentUncertaintyRSTT -- extract all the path dependent uncertainty information from a GeoTessModelSLBM
     * <li>replacePathDependentUncertaintyRSTT -- replace all the path dependent uncertainty information in a GeoTessModelSLBM
     * </ul>
     * 
     * <p>For all options
     * except the vtk option, output is sent to standard out. The intention is that
     * users would either pipe the output to a file or insert the call to this
     * program into a script with the output piped to some other program.
     * <p>
     * If no arguments are supplied, a list of the recognized functions is
     * output. If the first argument is a recognized function but other required
     * arguments are missing, a list of the required arguments is output.
     * <p>
     * Many functions require a 'list of attributes'. This list can be a
     * string similar to '0,2,4-n', which would return attributes 0, 2 and 4
     * through the number of available attributes. 'n' would return only the
     * last attribute. 'all' and '0-n' would both return all attributes.  
     * 'none' would return no values at all. The list may not include any spaces.
     * <p>
     * For most functions, the first two arguments after the function name are the 
     * name of the input model file and the relative path to the grid directory.  
     * Some models have the grid stored in the same file with the model while other
     * models reference a grid stored in a separate file. If the grid is stored in the same file 
     * with the model, then the relative path to the grid directory is irrelevant
     * but something must be supplied in order to maintain the order of the argument
     * list.  If the grid is stored in a separate file then
     * the name of the file that contains the grid, without any directory information,
     * is stored in the model file.  When the model is loaded, it has to be told the 
     * relative path from the directory where the model is located to the directory 
     * where the grid file is located.  If the grid is in a separate file located in 
     * the same directory as the model file, provide the single character '.'.  
     * Note that models and grids also contain an MD5 hash of the grid file contents
     * so the danger of a model referencing the wrong grid is vanishingly small.
     * 
     * <p>
     * All the functions whose names start with 'vtk' extract information from a 
     * GeoTessModel and store it in a file in VTK format 
     * (http://www.vtk.org/VTK/img/file-formats.pdf).  These files can be 
     * visualized with free software called ParaView.  Visit http://www.paraview.org
     * for more information and downloads for various platforms.
     * 
     * @param args
     *            the first argument should be one of the functions defined above.
     *            Subsequent arguments depend on the first argument.
     */
    public static void main(String[] args)
    {
	try
	{
	    //			GeoTessModelSLBM test = new GeoTessModelSLBM("C:\\TomographyTransitions2019\\israel_resampled.geotess");
	    //			System.out.println(test.toString());
	    new GeoTessExplorer().run(args);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    protected static LinkedHashMap<String, String> functionMap;
    static
    {
	functionMap = new LinkedHashMap<String, String>();
	functionMap.put("version", "output the GeoTess version number");
	functionMap.put("toString", "print summary information about a model");
	functionMap.put("updateModelDescription", "update the description information for a model");
	functionMap.put("statistics", "print summary statistics about the data in a model");
	functionMap.put("getClassName", "discover the class name of a specified model");
	functionMap.put("equal", "given two GeoTessModels test that all radii and attribute values of all nodes are ==.  Metadata can differ.");
	functionMap.put("extractGrid", "load a model or grid and write its grid to stdout, vtk, kml, ascii or binary file");
	functionMap.put("resample", "resample a model onto a new grid");
	functionMap.put("extractActiveNodes", "load a model and extract the positions of all active nodes");
	functionMap.put("replaceAttributeValues", "replace the attribute values associated with all active nodes");
	functionMap.put("reformat", "load a model and write it out in another format");
	functionMap.put("getValues", "interpolate values at a single point");
	functionMap.put("getValuesFile", "interpolate values at points specified in an ascii file");
	functionMap.put("interpolatePoint", "interpolate values at a single point (verbose)");
	functionMap.put("borehole", "interpolate values along a radial profile");
	functionMap.put("profile", "extract model values at vertex closest to specified latitude, longitude position");
	functionMap.put("findClosestPoint", "find the closest point to a supplied geographic location and return information about it");
	functionMap.put("slice ", "interpolate values on a vertical plane defined by a great circle connecting two points");
	functionMap.put("sliceDistAz", "interpolate values on a vertical plane defined by a great circle defined by a point, a distance and a direction");
	functionMap.put("mapValuesDepth", "interpolate values on a lat, lon grid at constant depths");
	functionMap.put("mapValuesLayer", "interpolate values on a lat, lon grid at fractional radius in a layer ");
	functionMap.put("mapLayerBoundary", "depth of layer boundaries on a lat, lon grid");
	functionMap.put("mapLayerThickness", "layer thickness on a lat, lon grid");
	functionMap.put("values3DBlock", "interpolate values on a regular lat, lon, radius grid");
	functionMap.put("function", "new model with attributes calculated from two input models");
	functionMap.put("vtkLayers", "generate vtk plot file of values at the tops of layers");
	functionMap.put("vtkDepths", "generate vtk plot file of values at specified depths");
	functionMap.put("vtkDepths2", "generate vtk plot file of values at specified depths");
	functionMap.put("vtkLayerThickness", "generate vtk plot file of layer thicknesses");
	functionMap.put("vtkLayerBoundary", "generate vtk plot file of depth or elevation of layer boundary");
	functionMap.put("vtkSlice", "generate vtk plot file of vertical slice");
	functionMap.put("vtkSolid", "generate vtk plot file of entire globe");
	functionMap.put("vtk3DBlock", "generate vtk plot file of values on a lat-lon-depth grid");
	functionMap.put("vtkPoints", "generate vtk plot of point data");
	functionMap.put("vtkRobinson", "generate vtk plot of a Robinson projection of model data");
	functionMap.put("vtkRobinsonLayers", "generate vtk plot of a Robinson projection of model data at tops of multiple layers");
	functionMap.put("vtkRobinsonPoints", "generate vtk plot of a Robinson projection of point data");
	functionMap.put("vtkRobinsonTriangleSize", "generate vtk plot of triangle size on Robinson projection");
	functionMap.put("vtkLayerAverage", "generate vtk plot of the average values within the crust of a designated model");
	functionMap.put("reciprocalModel", "generates a reciprocal GeoTessModel, where all values are inverted");
	functionMap.put("renameLayer", "renames an individual layer in a GeoTessModel");
	functionMap.put("renameLayers", "renames all layers in a GeoTessModel");
	functionMap.put("getLatitudes", "array of equally spaced latitude values");
	functionMap.put("getLongitudes", "array of equally spaced longitude values");
	functionMap.put("getDistanceDegrees", "array of equally spaced distances along a great circle");
	functionMap.put("translatePolygon", "translate polygon between kml/kmz and ascii format");

	functionMap.put("GeoTessModelSiteData:", "");
	functionMap.put("extractSiteTerms", "extract site terms from a GeoTessModelSiteData and print to screen");
	functionMap.put("replaceSiteTerms", "replace site terms in a GeoTessModelSiteData with values loaded from a file");

	functionMap.put("RSTT:", "");
	functionMap.put("extractPathDependentUncertaintyRSTT", "extract all the path dependent uncertainty information from a GeoTessModelSLBM");
	functionMap.put("replacePathDependentUncertaintyRSTT", "replace all the path dependent uncertainty information in a GeoTessModelSLBM");

    }


    public void run(String[] args) throws Exception
    {
	if (args.length == 0)
	{
	    System.out.println("GeoTessExplorer "+GeoTessJava.getVersion());
	    System.out .println("\nSpecify one of the following functions:\n"
		    + parseFunctionList());

	    System.out.println("Note that when a function requests a 'list of attributes'\n" +
		    "specify a string like '0' or '0,2' or '0-2' or 'n' or '1-n' or 'all'\n" +
		    "where 'n' is interpreted to be the index of the last attribute\n");
	    System.exit(0);
	}

	String cmd = args[0];
	if (cmd.equalsIgnoreCase("version"))
	    System.out.println("GeoTessJava."+GeoTessJava.getVersion());
	else if (cmd.equalsIgnoreCase("toString"))
	    toString(args);
	else if (cmd.equalsIgnoreCase("updateModelDescription"))
	    updateModelDescription(args);
	else if (cmd.equalsIgnoreCase("statistics"))
	    statistics(args);
	else if (cmd.equalsIgnoreCase("extractActiveNodes"))
	    extractActiveNodes(args);
	else if (cmd.equalsIgnoreCase("replaceAttributeValues"))
	    replaceAttributeValues(args);
	else if (cmd.equalsIgnoreCase("extractGrid"))
	    extractGrid(args);
	else if (cmd.equalsIgnoreCase("resample"))
	    resample(args);
	else if (cmd.equalsIgnoreCase("reformat"))
	    reformat(args);
	else if (cmd.equalsIgnoreCase("getValues"))
	    getValues(args);
	else if (cmd.equalsIgnoreCase("getValuesFile"))
	    getValuesFile(args);
	else if (cmd.equalsIgnoreCase("interpolatePoint"))
	    interpolatePoint(args);
	else if (cmd.equalsIgnoreCase("borehole"))
	    borehole(args);
	else if (cmd.equalsIgnoreCase("profile"))
	    profile(args);
	else if (cmd.equalsIgnoreCase("findClosestPoint"))
	    findClosestPoint(args);
	else if (cmd.equalsIgnoreCase("slice"))
	    slice(args);
	else if (cmd.equalsIgnoreCase("sliceDistAz"))
	    sliceDistAz(args);
	else if (cmd.equalsIgnoreCase("mapvaluesdepth"))
	    mapValuesDepth(args);
	else if (cmd.equalsIgnoreCase("mapvalueslayer"))
	    mapValuesLayer(args);
	else if (cmd.equalsIgnoreCase("maplayerboundary"))
	    mapLayerBoundary(args);
	else if (cmd.equalsIgnoreCase("maplayerthickness"))
	    mapLayerThickness(args);
	//		else if (cmd.equalsIgnoreCase("triangleEdges"))
	//			triangleEdges(args);
	//		else if (cmd.equalsIgnoreCase("gridToKML"))
	//			gridToKML(args);
	else if (cmd.equalsIgnoreCase("values3DBlock"))
	    values3DBlock(args);
	else if (cmd.equalsIgnoreCase("function"))
	    function(args);
	else if (cmd.equalsIgnoreCase("vtkLayers"))
	    vtkLayers(args);
	else if (cmd.equalsIgnoreCase("vtkdepths"))
	    vtkDepths(args);
	else if (cmd.equalsIgnoreCase("vtkdepths2"))
	    vtkDepths2(args);
	else if (cmd.equalsIgnoreCase("vtklayerthickness"))
	    vtkLayerThickness(args);
	else if (cmd.equalsIgnoreCase("vtklayerboundary"))
	    vtkLayerBoundary(args);
	else if (cmd.equalsIgnoreCase("vtkslice"))
	    vtkSlice(args);
	else if (cmd.equalsIgnoreCase("vtksolid"))
	    vtkSolid(args);
	else if (cmd.equalsIgnoreCase("vtk3DBlock"))
	    vtk3DBlock(args);
	else if (cmd.equalsIgnoreCase("getLatitudes"))
	    getLatitudes(args);
	else if (cmd.equalsIgnoreCase("getLongitudes"))
	    getLongitudes(args);
	else if (cmd.equalsIgnoreCase("getDistanceDegrees"))
	    getDistanceDegrees(args);
	else if (cmd.equalsIgnoreCase("translatePolygon"))
	    translatePolygon(args);
	else if (cmd.equalsIgnoreCase("vtkRobinson"))
	    vtkRobinson(args);
	else if (cmd.equalsIgnoreCase("vtkRobinsonLayers"))
	    vtkRobinsonLayers(args);
	else if (cmd.equalsIgnoreCase("vtkRobinsonTriangleSize"))
	    vtkRobinsonTriangleSize(args);
	else if (cmd.equalsIgnoreCase("vtkRobinsonPoints"))
	    vtkRobinsonPoints(args);
	else if (cmd.equalsIgnoreCase("vtkPoints"))
	    vtkPoints(args);
	else if (cmd.equalsIgnoreCase("vtkLayerAverage"))
	    vtkLayerAverage(args);
	else if (cmd.equalsIgnoreCase("reciprocalModel"))
	    reciprocalModel(args);
	else if (cmd.equalsIgnoreCase("renameLayer"))
	    renameLayer(args);
	else if (cmd.equalsIgnoreCase("renameLayers"))
	    renameLayers(args);
	else if (cmd.equalsIgnoreCase("equal") || cmd.equalsIgnoreCase("equals"))
	    equal(args);
	else if (cmd.equalsIgnoreCase("extractSiteTerms"))
	    extractSiteTerms(args);
	else if (cmd.equalsIgnoreCase("replaceSiteTerms"))
	    replaceSiteTerms(args);
	else if (cmd.equalsIgnoreCase("getClassName"))
	    getClassName(args);
	else if (cmd.equalsIgnoreCase("extractPathDependentUncertaintyRSTT"))
	    extractPathDependentUncertaintyRSTT(args);
	else if (cmd.equalsIgnoreCase("replacePathDependentUncertaintyRSTT"))
	    replacePathDependentUncertaintyRSTT(args);
	else
	    throw new Exception(String.format(
		    "%n%s is not a recognized command%n"
			    + "Please specify one of the following command line parameters:\n"
			    + parseFunctionList(),
			    args[0]));		
    }

    protected void getClassName(String[] args) throws Exception
    {
	int nmin = 2;
	if (args.length < nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- getClassName%n"
			    + "  2 -- model file name%n"
			    + "  3 -- relative path to grid directory (optional, not used if grid stored in model file)%n",
			    nmin));
	    System.exit(0);
	}

	File inputFile = new File(args[1]);
	String pathToGridDir = args.length == 2 ? "." : args[2];

	System.out.println(GeoTessModel.getClassName(inputFile, pathToGridDir));

    }

    protected void replacePathDependentUncertaintyRSTT(String[] args)  throws Exception {
	int nmin = 5;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- replacePathDependentUncertaintyRSTT%n"
			    + "  2 -- path to the GeoTessModelSLBM input file%n"
			    + "  3 -- relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4 -- path to file with new PDU info.  If not null must contain '<phase>' which will be replaced with phase name%n"
			    + "  5 -- path of the file to receive the GeoTessModelSLBM model%n",

			    nmin));
	    System.exit(0);
	}

	int a = 0;
	File inputFile = new File(args[++a]);

	String pathToGridDir = args[++a];

	String pduFileName = args[++a];

	File outputFile = new File(args[++a]);

	if (!pduFileName.equalsIgnoreCase("null") && !pduFileName.contains("<phase>"))
	    throw new Exception("Path to file with new PDU info does not contain substring '<phase>'");

	System.out.printf("Loading GeoTessSLBM file %s (%1.3f MB)%n",
		inputFile.getCanonicalPath(), inputFile.length()/(1024.*1024.));
	GeoTessModelSLBM model = new GeoTessModelSLBM(inputFile, pathToGridDir);

	if (pduFileName.equalsIgnoreCase("null"))
	    model.clearPathDependentUncertainty();
	else
	    for (int phaseIndex=0; phaseIndex<4; ++phaseIndex)
	    {
		String phase = Uncertainty.getPhase(phaseIndex);
		File pduFile = new File(pduFileName.replace("<phase>", phase));
		System.out.printf("Loading UncertaintyPDU file %s (%1.3f MB)%n",
			pduFile.getCanonicalPath(), pduFile.length()/(1024.*1024.));
		UncertaintyPDU pdu = new UncertaintyPDU(pduFile);
		if (!pdu.getPhaseStr().equals(phase))
		    throw new Exception(String.format("Phase in pduFile=%s is not equal to phase %s%n",
			    pdu.getPhaseStr(), phase));
		if (!pdu.getGridId().equals(model.getGrid().getGridID()))
		    throw new Exception(String.format("GridID in pduFile=%s is not equal to gridID in input model %s%n",
			    pdu.getGridId(), model.getGrid().getGridID()));
		model.setPathDependentUncertainty(pdu, phaseIndex);
	    }

	model.writeModel(outputFile);
	System.out.printf("Wrote GeoTessSLBM file %s (%1.3f MB)%n",
		outputFile.getCanonicalPath(), outputFile.length()/(1024.*1024.));

    }

    protected void extractPathDependentUncertaintyRSTT(String[] args)  throws Exception {
	int nmin = 5;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- extractPathDependentUncertaintyRSTT%n"
			    + "  2 -- path to the GeoTessModelSLBM input file%n"
			    + "  3 -- relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4 -- names of files to receive PDU files.  Must contain '#phase#' which will be replaced with phase name.%n"
			    + "  5 -- format = one of [ binary | ascii | geotess ]%n",

			    nmin));
	    System.exit(0);
	}

	int a = 0;
	File inputFile = new File(args[++a]);
	String pathToGridDir = args[++a];

	String pduFileName = args[++a];

	String format = args[++a].toLowerCase();

	if (!pduFileName.contains("#phase#"))
	    throw new Exception("PDU file name must contain substring '#phase#'");

	if (!format.equalsIgnoreCase("binary") && !format.equalsIgnoreCase("ascii") 
		&& !format.equalsIgnoreCase("geotess"))
	    throw new Exception (String.format("format = %s must be one of [ binary | ascii | geotess ]", format));

	System.out.printf("Reading input file %s (%s)%n", inputFile.getCanonicalFile(), 
		getFileSize(inputFile));
	GeoTessModelSLBM model = new GeoTessModelSLBM(inputFile, pathToGridDir);

	for (int phaseIndex=0; phaseIndex<4; ++phaseIndex)
	{
	    UncertaintyPDU pdu = model.getPathDependentUncertainty(phaseIndex);
	    if (pdu == null)
		throw new Exception(String.format("Model %s%ndoes not support path dependent uncertainty.",
			inputFile.getAbsolutePath()));

	    String phase = Uncertainty.getPhase(phaseIndex);
	    if (!pdu.getPhaseStr().equals(phase))
		throw new Exception(String.format("Phase in pduFile = %s is not equal to phase %s%n",
			pdu.getPhaseStr(), phase));
	    File pduFile = new File(pduFileName.replace("#phase#", phase));
	    System.out.printf("Writing file %s ", pduFile.getAbsoluteFile());
	    if(format.equals("binary"))
		pdu.writeFileBinary(pduFile);
	    if(format.equals("ascii"))
		pdu.writeFileAscii(pduFile);
	    if(format.equals("geotess"))
		pdu.writeFileGeoTess(pduFile);
	    System.out.printf("%s%n", getFileSize(pduFile));
	}
    }

    public void getPhase(String[] args) throws Exception {
	int nmin = 2;
	if (args.length < nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- getPhase%n"
			    + "  2 -- model file name%n"
			    + "  3 -- relative path to grid directory (optional, not used if grid stored in model file)%n",
			    nmin));
	    System.exit(0);
	}

	File inputFile = new File(args[1]);
	String pathToGridDir = args.length == 2 ? "." : args[2];

	LibCorr3DModel model = new LibCorr3DModel(inputFile, pathToGridDir);

	System.out.println(model.getPhase());
    }


    public void getSupportedPhases(String[] args) throws Exception {
	int nmin = 2;
	if (args.length < nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- getSupportedPhases%n"
			    + "  2 -- model file name%n"
			    + "  3 -- relative path to grid directory (optional, not used if grid stored in model file)%n",
			    nmin));
	    System.exit(0);
	}

	File inputFile = new File(args[1]);
	String pathToGridDir = args.length == 2 ? "." : args[2];

	LibCorr3DModel model = new LibCorr3DModel(inputFile, pathToGridDir);

	System.out.println(model.getSupportedPhasesString());
    }


    public void equal(String[] args) throws Exception
    {
	int nmin = 5;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- equal%n"
			    + "  2 -- first input model file name%n"
			    + "  3 -- relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4 -- second input model file name%n"
			    + "  5 -- relative path to grid directory (not used if grid stored in model file)%n",
			    nmin));
	    System.exit(0);
	}

	int arg = 1;
	File inputFile = new File(args[arg++]);
	String pathToGridDir = args[arg++];

	GeoTessModel model1 = GeoTessModel.getGeoTessModel(inputFile, pathToGridDir);

	File inputFile2 = new File(args[arg++]);
	String pathToGridDir2 = args[arg++];

	GeoTessModel model2 = GeoTessModel.getGeoTessModel(inputFile2, pathToGridDir2);

	if (model1.equals(model2))
	    System.out.println("equal\n");
	else
	    System.out.println("NOT equal\n");
    }

    /**
     * Translate a Polygon from Google Earth kml/kmz format to ascii format
     * readable by GeoTess C++ libraries.
     * 
     * @param args
     *            <ol>
     *            <li>translatePolygon
     *            <li>input polygon file name
     *            <li>output polygon file name. If the file extension is 'vtk'
     *            then the file is written in vtk format. If the file extension
     *            is 'kml' or 'kmz' the file is written in a format compatible
     *            with Google Earth. Otherwise the file is written in ascii
     *            format with boundary points writtein in lat-lon order.
     *            </ol>
     * @throws Exception
     */
    public void translatePolygon(String[] args) throws Exception
    {
	int nmin = 3;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1  --  translatePolygon%n"
			    + "  2  --  input polygon file name%n"
			    + "  3  --  output polygon file name. If the file extension is 'vtk'%n"
			    + "         then the file is written in vtk format. If the file extension%n"
			    + "         is 'kml' or 'kmz' the file is written in a format compatible%n"
			    + "         with Google Earth. Otherwise the file is written in ascii%n"
			    + "         format with boundary points writtein in lat-lon order.%n", 
			    nmin));
	    System.exit(0);
	}

	Polygon polygon = Polygon.getPolygon(new File(args[1]));
	polygon.write(new File(args[2]));		
    }

    public void extractSiteTerms(String[] args) throws IOException {
	int nmin = 2;
	if (args.length != 2 && args.length != 3)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- extractSiteTerms%n"
			    + "  2 -- model file name%n"
			    + "  3 -- relative path to grid directory (optional, not used if grid stored in model file)%n"
			    + "%n"
			    + "Output columns: sta, lat, lon, elev, ondate, offdate, site terms%n"
			    ,nmin));
	    System.exit(0);
	}

	File inputFile = new File(args[1]);
	String pathToGridDir = args.length == 2 ? "" : args[2];

	GeoTessModelSiteData model = new GeoTessModelSiteData(inputFile, pathToGridDir);

	System.out.printf("attributes: %s%n", model.getSiteTermAttributes().getAttributeNamesString());
	System.out.printf("units: %s%n", model.getSiteTermAttributes().getAttributeUnitsString());
	System.out.println(model.getSiteTermAttributes().getDataType());

	System.out.print(model.getSiteTermHistory());
    }

    public void replaceSiteTerms(String[] args) throws Exception {
	int nmin = 5;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- replaceSiteTerms%n"
			    + "  2 -- input model file name%n"
			    + "  3 -- relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4 -- name of file containing new site terms%n"
			    + "  5 -- output model file name%n"
			    + "%n"
			    + "Columns in site terms file: sta, lat, lon, elev, ondate, offdate, attributes%n"
			    ,nmin));
	    System.exit(0);
	}

	int arg = 1;
	File inputFile = new File(args[arg++]);
	String gridDirectory = args[arg++];

	File siteTermFile = new File(args[arg++]);

	String outputFileString = args[arg++];
	File outputFile = new File(outputFileString);

	// load the input model, ignoring any site terms it may contain.
	GeoTessModel inputModel = new GeoTessModel(inputFile, gridDirectory);

	Scanner siteTermScanner = new Scanner(siteTermFile);

	// read the siteterm attribute definitions from the site term file.
	AttributeDataDefinitions attributes = new AttributeDataDefinitions(siteTermScanner);
	attributes.setDataType(siteTermScanner.nextLine());

	// make a shallow copy of the input model.
	GeoTessModelSiteData outputModel = new GeoTessModelSiteData(inputModel, attributes);

	// read site term info from file and set the values in the output model.
	outputModel.setSiteTerms(siteTermScanner);
	siteTermScanner.close();

	outputModel.writeModel(outputFile, inputModel.getMetaData().getGridInputFileName());
    }

    /**
     * Output a GeoTessGrid tessellation to a kml/kmz file for viewing
     * with Google Earth.
     * @deprecated use extractGrid instead.  
     * @param args
     *            <ol>
     *            <li>gridToKML
     *            <li>input model or grid file name
     *            <li>relative path to grid directory, otherwise ignored
     *            <li>output file (must have kml or kmz extension)
     *            <li>layerIndex if 2 is a model, tessId if 2 is a grid
     *            </ol>
     * @throws Exception
     */
    public void gridToKML(String[] args) throws Exception
    {
	int nmin = 5;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  gridToKML%n"
			    + "  2  --  input model or grid file name%n"
			    + "  3  --  relative path to grid directory, otherwise ignored%n"
			    + "  4  --  output file (must have kml or kmz extension)%n"
			    + "  5  --  layerIndex if 2 is a model, tessId if 2 is a grid"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;

	File inputFile = new File(args[arg++]);
	String gridDirectory = args[arg++];

	File outputFile = new File(args[arg++]);
	if (!outputFile.getName().endsWith("kml") && !outputFile.getName().endsWith("kmz"))
	    throw new IOException("\noutput file name must have '.kml' or '.kmz' extension\n");

	int tessId=-1;

	GeoTessGrid grid = null;
	if (GeoTessModel.isGeoTessModel(inputFile))
	{
	    GeoTessModel model = GeoTessModel.getGeoTessModel(inputFile, gridDirectory);
	    tessId = model.getMetaData().getTessellation(Integer.parseInt(args[arg++]));
	    model.getGridRotated().writeGridKML(outputFile, tessId);
	}
	else
	{
	    grid = new GeoTessGrid().loadGrid(inputFile);
	    tessId = Integer.parseInt(args[arg++]);
	    if (tessId < 0 || tessId >= grid.getNTessellations())
		tessId = grid.getNTessellations()-1;

	    grid.writeGridKML(outputFile, tessId);
	}
	System.out.printf("Grid written to file %s%n", outputFile.getCanonicalPath());
    }

    /**
     * Send basic information about a GeoTessModel to standard out.
     * 
     * @param args
     *            <ol>
     *            <li>toString
     *            <li>input model or grid file name
     *            <li>relative path to grid directory (optional; not used if grid stored in model file)
     *            </ol>
     * @throws Exception
     */
    public void toString(String[] args) throws Exception
    {
	int nmin = 2;
	if (args.length < nmin || args.length > 3)
	{
	    System.out .println(
		    String.format("%n%nMust supply either 2 or 3 arguments:%n"
			    + "  1  --  toString%n"
			    + "  2  --  name of file containing a GeoTessModel or GeoTessGrid%n"
			    + "  3  --  relative path to grid directory "
			    + "             (only needed when (2) is a model and grid is stored in separate file)%n", 
			    nmin));
	    System.exit(0);
	}

	File f = new File(args[1]);
	if (GeoTessGrid.isGeoTessGrid(f))
	    System.out.println(new GeoTessGrid(f));
	else
	    System.out.println(GeoTessModel.getGeoTessModel(f, 
		    args.length == 2 ? "" : args[2]));

    }

    /**
     * Send basic statistics about the data in a GeoTessModel to standard out.
     * 
     * @param args
     *            <ol>
     *            <li>statistics
     *            <li>input model file name
     *            <li>relative path to grid directory (optional; not used if grid stored in model file)
     *            </ol>
     * @throws Exception
     */
    public void statistics(String[] args) throws Exception
    {
	int nmin = 2;
	if (args.length < nmin || args.length > 3)
	{
	    System.out .println(
		    String.format("%n%nMust supply either 2 or 3 arguments:%n"
			    + "  1  --  statistics%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n", 
			    nmin));
	    System.exit(0);
	}

	if (args.length == 2)
	    System.out.println(GeoTessModelUtils.statistics(GeoTessModel.getGeoTessModel(args[1])));
	else if (args.length == 3)
	    System.out.println(GeoTessModelUtils.statistics(GeoTessModel.getGeoTessModel(args[1], args[2])));	
    }

    /**
     * Load a model and write its grid to a new file.
     * 
     * @param args
     *            <ol>
     *            <li>extractGridFile
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>name of output grid file
     *            </ol>
     * @throws Exception
     */
    public void extractGrid(String[] args) throws Exception
    {
	int nmin = 5;
	if (args.length < nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- extractGrid%n"
			    + "  2 -- input model or grid file name%n"
			    + "  3 -- relative path to grid directory (required but only used if grid and model are stored in separate files)%n"
			    + "  4 -- output file:%n"
			    + "       if 'stdout', triangle edges are output to standard out in lat1, lon1, lat2, lon2 format%n"
			    + "       if 'gmt', triangle edges are output to standard out in gmt-compatible format%n"
			    + "       if filename with extension 'kml' or 'kmz', edges are written to kml/kmz file viewable with Google Earth%n"
			    + "       if filename with extension 'vtk', triangles are written to vtk file(s) viewable with paraview.%n"
			    + "       if filename with extension 'ascii', grid written to GeoTessGrid file in ascii format%n"
			    + "       if filename with any other extension, grid written to GeoTessGrid file in binary format.%n"
			    + "  5 -- if the input specified in item 2 is a GeoTessModel, specify the layer number,%n"
			    + "       if it is a GeoTessGrid, then specify the tessellation id."
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File inputFile = new File(args[arg++]);
	String pathToGridDir = args[arg++];
	String output = args[arg++];
	int tessId = arg >= args.length ? -1 : Integer.parseInt(args[arg++]);

	GeoTessModel model = null;
	GeoTessGrid grid = null;
	EarthShape earthShape = null;
	if (GeoTessModel.isGeoTessModel(inputFile))
	{
	    model = GeoTessModel.getGeoTessModel(inputFile, pathToGridDir);
	    grid = model.getGridRotated();
	    earthShape = model.getEarthShape();
	    // tessid is really a layer index.  convert it to tessid.
	    if (tessId >= 0)
		tessId = model.getMetaData().getTessellation(tessId);
	}
	else
	{
	    grid = new GeoTessGrid().loadGrid(inputFile);
	    earthShape = EarthShape.WGS84;
	}

	if (output.equalsIgnoreCase("stdout"))
	{
	    if (tessId < 0 && grid.getNTessellations() == 1) tessId = 0;
	    if (tessId < 0)
	    {
		if (model == null)
		    throw new Exception("\nMust specify a 5th argument that specifies a tessellation index in range 0 to "
			    +(grid.getNTessellations()-1));
		else
		    throw new Exception("\nMust specify a 5th argument that specifies a layer index in range 0 to "
			    +(model.getNLayers()-1));
	    }
	    for (int[] edge : grid.getEdges(tessId))
		System.out.printf("%1.6f %1.6f %1.6f %1.6f%n", 
			earthShape.getLatDegrees(grid.getVertex(edge[0])),
			earthShape.getLonDegrees(grid.getVertex(edge[0])),
			earthShape.getLatDegrees(grid.getVertex(edge[1])),
			earthShape.getLonDegrees(grid.getVertex(edge[1])));
	}
	else if (output.equalsIgnoreCase("gmt"))
	{
	    if (tessId < 0 && grid.getNTessellations() == 1) tessId = 0;
	    if (tessId < 0)
	    {
		if (model == null)
		    throw new Exception("\nMust specify a 5th argument that specifies a tessellation index in range 0 to "
			    +(grid.getNTessellations()-1));
		else
		    throw new Exception("\nMust specify a 5th argument that specifies a layer index in range 0 to "
			    +(model.getNLayers()-1));
	    }
	    for (int[] edge : grid.getEdges(tessId))
		System.out.printf(">%n%1.6f %1.6f%n%1.6f %1.6f%n", 
			earthShape.getLonDegrees(grid.getVertex(edge[0])),
			earthShape.getLatDegrees(grid.getVertex(edge[0])),
			earthShape.getLonDegrees(grid.getVertex(edge[1])),
			earthShape.getLatDegrees(grid.getVertex(edge[1])));
	}
	else if (output.toLowerCase().endsWith("kml") || output.toLowerCase().endsWith("kmz"))
	{
	    if (tessId < 0 && grid.getNTessellations() == 1) tessId = 0;
	    if (tessId < 0)
	    {
		if (!output.contains("%"))
		    output = GeoTessModelUtils.expandFileName(output, "_tess_%d");

		for (int i=0; i<grid.getNTessellations(); ++i)
		{
		    File fout = new File(String.format(output, i));
		    grid.writeGridKML(fout, i);
		    System.out.println(fout);
		}
	    }
	    else
		grid.writeGridKML(new File(output), tessId);
	    System.out.println("Done.");
	}
	else if (output.toLowerCase().endsWith("vtk"))
	{
	    if (tessId < 0 && grid.getNTessellations() == 1) tessId = 0;
	    if (tessId < 0)
	    {
		if (!output.contains("%"))
		    output = GeoTessModelUtils.expandFileName(output, "_tess_%d");

		for (int i=0; i<grid.getNTessellations(); ++i)
		{
		    File fout = new File(String.format(output, i));
		    GeoTessModelUtils.vtkTriangleSize(grid, fout, i);
		    System.out.println(fout);
		}
	    }
	    else
		GeoTessModelUtils.vtkTriangleSize(grid, new File(output), tessId);

	    File outputDirectory = new File(output).getParentFile();
	    if (outputDirectory == null) outputDirectory = new File(".");
	    if (!(new File(outputDirectory, "continent_boundaries.vtk")).exists())
		GeoTessModelUtils.copyContinentBoundaries(outputDirectory);

	    System.out.println("Done.");
	}
	else
	{
	    grid.writeGrid(output);
	    System.out.println("Grid written to "+output);
	    System.out.println("Done.");
	}
    }

    /**
     * Resample a model onto a new grid.
     * 
     * @param args
     *            <ol>
     *            <li>resample
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>name of input grid file
     *            <li>name of file where resampled model will be written
     *            </ol>
     * @throws Exception
     */
    public void resample(String[] args) throws Exception
    {
	int nmin = 5;
	if (args.length < nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- resample%n"
			    + "  2 -- input model or grid file name%n"
			    + "  3 -- relative path to grid directory (required but only used if grid and model are stored in separate files)%n"
			    + "  4 -- name of file containing the new grid (if a model is specified the grid is extracted)%n"
			    + "  5 -- output file where resampled model will be written%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File inputFile = new File(args[arg++]);
	String pathToGridDir = args[arg++];
	File gridFile = new File(args[arg++]);
	File outputFile = new File(args[arg++]);

	System.out.printf("Loading model %s (%s)%n",
		inputFile.getAbsoluteFile(), getFileSize(inputFile));
	GeoTessModel model = GeoTessModel.getGeoTessModel(inputFile, pathToGridDir);

	System.out.printf("Loading new grid %s (%s)%n", 
		gridFile.getAbsoluteFile(), getFileSize(gridFile));

	GeoTessGrid newGrid;
	if (GeoTessGrid.isGeoTessGrid(gridFile))
	    newGrid = new GeoTessGrid(gridFile);
	else if (GeoTessModel.isGeoTessModel(gridFile))
	    newGrid = new GeoTessModel(gridFile).getGridRotated();
	else
	    throw new Exception(gridFile.getAbsolutePath()+" is neither a GeoTessGrid nor GeoTessModel.");


	System.out.printf("Resampling model from old grid with %d vertices to new grid with %d vertices (%1.2f%%)%n",
		model.getNVertices(), newGrid.getNVertices(), 100.*newGrid.getNVertices()/model.getNVertices());

	String className = model.getMetaData().getModelClassName();
	System.out.println(className);
	GeoTessModel newModel = model.resample(newGrid);

	System.out.printf("Writing output model %s ", outputFile.getAbsolutePath());
	newModel.writeModel(outputFile);
	System.out.printf("(%s)%n", getFileSize(outputFile));

	System.out.printf("%nNew model:%n%s%n", newModel.toString());

	System.out.printf("%nStatistics of original model:%n%s%n"
		+ "Statistics of new model%n%s%n",
		GeoTessModelUtils.statistics(model),
		GeoTessModelUtils.statistics(newModel));
    }

    /**
     * Extract the position of all active nodes in a model, 
     * in 'lat lon depth layerIndex' format.
     * 
     * <p>Output is streamed to standard out.
     * 
     * @param args
     *            <ol>
     *            <li>extractActiveNodes
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>list of attribute indexes
     *            <li>reciprocal (true or false)
     *            <li>path of polygon file (if 'null' then all grid nodes will be included)
     *            </ol>
     * @throws Exception
     */
    public void extractActiveNodes(String[] args) throws Exception
    {
	int nmin = 6;
	if (args.length < nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- extractActiveNodes%n"
			    + "  2 -- input model file name%n"
			    + "  3 -- relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4 -- list of attribute indexes%n"
			    + "  5 -- reciprocal (true or false)%n"
			    + "  6 -- path of polygon file (if 'null' then all grid nodes will be included)%n"
			    + "%n"
			    + "Output columns for 3D models: latitude, longitude, depth, layer number, attribute values%n"
			    + "Output columns for 2D models: latitude, longitude, attribute values%n"
			    + "Output columns for GeoTessGrids: latitude, longitude%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File inputFile = new File(args[arg++]);
	String pathToGridDir = args[arg++];

	String attributeList = args[arg++];

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	String polygonFileName = null;
	if (args.length > 5)
	    polygonFileName = args[arg++];

	if (GeoTessModel.isGeoTessModel(inputFile))
	{
	    GeoTessModel model = GeoTessModel.getGeoTessModel(inputFile, pathToGridDir);

	    int[] attributes = parseList(attributeList, model.getMetaData()
		    .getNAttributes() - 1);

	    if (polygonFileName != null && !polygonFileName.equalsIgnoreCase("null"))
		model.setActiveRegion(polygonFileName);

	    PointMap pm = model.getPointMap();
	    for (int pointIndex=0; pointIndex<pm.size(); ++pointIndex)
	    {
		System.out.print(model.getEarthShape().getLatLonString(pm.getPointUnitVector(pointIndex), "%10.6f %11.6f"));

		if (model.is3D())
		    System.out.printf(" %1.3f %d",  pm.getPointDepth(pointIndex), pm.getLayerIndex(pointIndex));

		for (int a : attributes)
		{
		    Data data = pm.getPointData(pointIndex);

		    System.out.print(' ');
		    switch (data.getDataType())
		    {
		    case DOUBLE:
			System.out.print(Double.toString(reciprocal ? 1./ data.getDouble(a) : data.getDouble(a)));
			break;
		    case FLOAT:
			System.out.print(Float.toString(reciprocal ? 1.F/ data.getFloat(a) : data.getFloat(a)));
			break;
		    default:
			System.out.print(data.getLong(a));
			break;
		    }
		}
		System.out.println();
	    }
	} 
	else if (GeoTessGrid.isGeoTessGrid(inputFile))
	{
	    GeoTessGrid grid = new GeoTessGrid(inputFile);
	    Polygon polygon = new PolygonGlobal(true);
	    if (polygonFileName != null && !polygonFileName.equalsIgnoreCase("null"))
		polygon = Polygon.getPolygon(new File(polygonFileName));

	    for (int vertex=0; vertex < grid.getNVertices(); ++vertex)
		if (polygon.contains(grid.getVertex(vertex)))
		    System.out.println(EarthShape.WGS84.getLatLonString(grid.getVertex(vertex)));

	}
	else
	    throw new Exception(inputFile.getCanonicalPath()
		    +"\ncontains neither a GeoTessModel nor a GeoTessGrid.");
    }

    /**
     * Populate model.
     * 
     * @param args
     *            <ol>
     *            <li>replaceAttributeValues
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>polygon file (if 'null' then all grid nodes will be included)
     *            <li>file of attribute values
     *            <li>output model file name
     *            </ol>
     * @throws Exception
     */
    public void replaceAttributeValues(String[] args) throws Exception
    {
	int nmin = 6;
	if (args.length < nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply at least %d arguments:%n"
			    + "  1 -- replaceAttributeValues%n"
			    + "  2 -- input model file name%n"
			    + "  3 -- relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4 -- polygon file (if 'null' then all grid nodes will be included)%n"
			    + "  5 -- file of attribute values %n"
			    + "  6 -- output model file name%n"
			    + "%n"
			    + "Columns in file of attribute values (see function extractActiveNodes):"
			    + "3D models: latitude, longitude, depth, layer number, attribute values%n"
			    + "2D models: latitude, longitude, attribute values%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File inputFile = new File(args[arg++]);
	String pathToGridDir = args[arg++];

	GeoTessModel model = GeoTessModel.getGeoTessModel(inputFile, pathToGridDir);
	String polygonFileName = args[arg++];
	if (!polygonFileName.equalsIgnoreCase("null"))
	    model.setActiveRegion(polygonFileName);

	Scanner input = new Scanner(new File(args[arg++]));

	File outputFile = new File(args[arg++]);

	PointMap pm = model.getPointMap();
	int pointIndex = 0;

	while (input.hasNext())
	{
	    if (pointIndex >= pm.size())
	    {
		while (input.hasNext()) ++pointIndex;
		input.close();
		throw new IOException(String.format("%nInput file has too many records.%n"
			+ "There are %d records in the input file and %d active nodes in the model.%n",
			pointIndex, pm.size()));
	    }

	    Scanner record = new Scanner(input.nextLine());
	    double[] u = model.getEarthShape().getVectorDegrees(record.nextDouble(), record.nextDouble());
	    if (VectorUnit.dot(u, pm.getPointUnitVector(pointIndex)) < Math.cos(1e-7))
	    {
		record.close();
		input.close();
		throw new IOException(String.format("%nPoints at record %d don't match.%n"
			+ "Input file location: %s%n"
			+ "Model location     : %s%n", pointIndex,
			model.getEarthShape().getLatLonString(u),
			model.getEarthShape().getLatLonString(pm.getPointUnitVector(pointIndex))));
	    }

	    if (model.is3D())
	    {
		double depth = record.nextDouble();
		if (Math.abs(depth - pm.getPointDepth(pointIndex)) > 1e-3)
		{
		    record.close();
		    input.close();
		    throw new IOException(String.format("%nPoints at record %d don't match.%n"
			    + "Input file location: %s %9.4f%n"
			    + "Model location     : %s %9.4f%n", pointIndex,
			    model.getEarthShape().getLatLonString(u), depth,
			    model.getEarthShape().getLatLonString(pm.getPointUnitVector(pointIndex)),
			    pm.getPointDepth(pointIndex)));
		}

		int layerIndex = record.nextInt();
		if (layerIndex != pm.getLayerIndex(pointIndex))
		{
		    record.close();
		    input.close();
		    throw new IOException(String.format("%nPoints at record %d have different layerIndexes.%n"
			    + "Input file location: %s %9.4f %d%n"
			    + "Model location     : %s %9.4f% d%n", pointIndex,
			    model.getEarthShape().getLatLonString(u), depth, layerIndex,
			    model.getEarthShape().getLatLonString(pm.getPointUnitVector(pointIndex)),
			    pm.getPointDepth(pointIndex), pm.getLayerIndex(pointIndex)));
		}
	    }

	    int attributeIndex=0;
	    while (record.hasNext())
	    {
		if (attributeIndex == model.getNAttributes())
		{
		    while (record.hasNext()) ++attributeIndex;
		    record.close();
		    input.close();
		    throw new IOException(String.format("%nInput file has too many attributes on record %d.%n"
			    + "There are %d attributes in the input file and %d attributes in the model.%n",
			    pointIndex, attributeIndex, model.getNAttributes()));
		}
		pm.setPointValue(pointIndex, attributeIndex++, record.nextDouble());
	    }

	    if (attributeIndex < model.getNAttributes())
	    {
		record.close();
		input.close();
		record.close();
		input.close();
		throw new IOException(String.format("%nInput file does not have enough attributes on record %d.%n"
			+ "There are %d attributes in the input file and %d attributes in the model.%n",
			pointIndex, attributeIndex, model.getNAttributes()));
	    }

	    record.close();

	    ++pointIndex;
	}
	input.close();

	if (pointIndex < pm.size())
	{
	    throw new IOException(String.format("%nInput file does not have enough records.%n"
		    + "There are %d records in the input file and %d active nodes in the model.%n",
		    pointIndex, pm.size()));
	}

	model.writeModel(outputFile);
    }

    /**
     * Translate a model file from one format to another, or change the grid
     * path information in a file.
     * <p>
     * If the supplied input model file name is a directory then the operation
     * is performed on every GeoTessModel file in the directory.
     * <p>
     * If an attempt is made to make a GeoTessModel reference a Grid in another
     * file, and the internal gridIDs in the model file and grid file do not
     * agree, the attempt will fail.
     * 
     * @param args
     *            <ol>
     *            <li>reformat 
     *            <li>input model file name or directory containing multiple models 
     *            <li>output model file name 
     *            <li>relative path to grid file, or '*' to have the grid information stored in
     *            the output model file.
     *            </ol>
     * @throws Exception
     */
    public void reformat(String[] args) throws Exception
    {
	int nmin = 4;
	if (args.length < nmin)
	{
	    System.out .printf("%n%nMust supply at least %d arguments:%n"
		    + "  1  --  reformat%n"
		    + "  2  --  input model file name or directory.  If directory, reformat is applied to %n"
		    + "         every GeoTessModel in the directory%n"
		    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
		    + "  4  --  output model file name or directory%n"
		    + "  5  --  outputGridFile or directory: %n"
		    + "         o - If outputGridFile is '*', write the grid internally to the outputFile.  %n"
		    + "         o - If outputGridFile is specified, write the grid to the specified file or directory.%n"
		    + "         o - If only 4 arguments are supplied, or outputGridFile = 'null', then treat the grid %n"
		    + "             file the same way the input model did.%n"
		    + "  6  --  geotess integer output file format (optional; defaults to default value %n"
		    + "  7  --  libcorr3d integer output file format (applies only to libcorr3d models; optional)%n"
		    , nmin);
	    System.exit(0);
	}

	File inputFile = new File(args[1]);
	String gridDirectory = args[2];

	File outputFile = new File(args[3]);

	String outputGridFile = null;
	if (args.length > 4 && !args[4].equalsIgnoreCase("null"))
	    outputGridFile = args[4];

	int geotessFileFormat = -1;
	if (args.length > 5)
	    geotessFileFormat = Integer.valueOf(args[5]);

	int libcorr3dFileFormat = -1;
	if (args.length > 6)
	    libcorr3dFileFormat = Integer.valueOf(args[6]);

	if (inputFile.isDirectory() && !outputFile.exists())
	    outputFile.mkdirs();

	if (inputFile.isDirectory() && outputFile.isFile())
	    throw new Exception("inputFile is a directory but outputFile is a file.  They must either both be directories, or neither.");

	if (inputFile.isFile() && outputFile.isDirectory())
	    throw new Exception("inputFile is a file but outputFile is a directory.  They must either both be directories, or neither.");

	if (inputFile.isFile())
	{
	    GeoTessModel inputModel = GeoTessModel.getGeoTessModel(inputFile, gridDirectory);
	    if (geotessFileFormat > 0)
		inputModel.getMetaData().setModelFileFormat(geotessFileFormat);
	    if (inputModel.getClass().getSimpleName().equals("LibCorr3DModel") && libcorr3dFileFormat > 0)
		((LibCorr3DModel)inputModel).setFormatVersion(libcorr3dFileFormat);
	    if (outputGridFile == null)
	    {	
		// do what the input model did.  Don't have to check anything because if the inputModel
		// loaded its grif from a file, then we know that file exists.
		inputModel.writeModel(outputFile, inputModel.getMetaData().getGridInputFileName());
	    }
	    else
	    {
		// user specified a grid file. Make sure it exists.  If it does not exist, write it. 
		checkGridFile(inputModel, outputGridFile);
		inputModel.writeModel(outputFile, outputGridFile);
	    }
	}
	else
	{
	    File outputDir = outputFile;  // want to call outputFile outputDir for clarity.
	    for (File inputModelFile : inputFile.listFiles())
	    {
		if (inputModelFile.isFile() && GeoTessModel.isGeoTessModel(inputModelFile))
		{
		    System.out.println(inputModelFile.getName());
		    GeoTessModel inputModel = GeoTessModel.getGeoTessModel(inputModelFile, gridDirectory);

		    if (geotessFileFormat > 0)
			inputModel.getMetaData().setModelFileFormat(geotessFileFormat);
		    if (inputModel.getClass().getSimpleName().equals("LibCorr3DModel") && libcorr3dFileFormat > 0)
			((LibCorr3DModel)inputModel).setFormatVersion(libcorr3dFileFormat);

		    if (outputGridFile == null)
		    {
			// Do what the inputModel did with its grid.
			if (inputModel.getMetaData().getGridInputFileName().equals("*"))
			    outputGridFile = "*";
			else if (inputModel.getMetaData().getProperties().containsKey("gridRelativePath"))
			    outputGridFile = inputFile.toPath().resolve(inputModel.getMetaData().getProperties().get("gridRelativePath"))
			    .toFile().getCanonicalPath();
			else
			    throw new Exception("Unable to determine how the input model dealt with grid. \n"
			    	+ "Must specify outputGridFile with argument 5");
		    }
		    
		    if (outputGridFile.equals("*"))
		    {
			// User specified that grid should be written internally
			inputModel.writeModel(new File(outputDir, inputModelFile.getName()), "*");
		    }
		    else 
		    {
			// user specified the name of either a file or a directory
			File ogf = inputFile.toPath().resolve(new File(outputGridFile).toPath()).toFile().getCanonicalFile();

			if (ogf.exists() && ogf.isDirectory())
			{
			    File gridFile = new File(ogf, "grid_"+inputModel.getGrid().getGridID()+".geotess");
			    checkGridFile(inputModel, gridFile.getAbsolutePath());
			    inputModel.writeModel(new File(outputDir, inputModelFile.getName()), gridFile);
			}
			else
			{
			    checkGridFile(inputModel, ogf.getCanonicalPath());
			    inputModel.writeModel(new File(outputDir, inputModelFile.getName()), ogf);
			}
		    }
		}
	    }
	}
	System.out.println("Done.");
    }

    /**
     * We are about to write a model to file.  The model is supposed to reference the 
     * specified gridFile.  
     * <ol>If:
     * <li>gridFile.name is "*": do nothing.
     * <li>gridFile does not exist: write the model's grid to the specified file.
     * <li>gridFile exists: check that the grid in the model and the grid in the file
     * have the same gridIDs.  If the do, do nothing.  If they don't, throw an exception.
     * </ol>
     * @param model
     * @param gridFile
     * @throws IOException 
     * @throws Exception 
     */
    private void checkGridFile(GeoTessModel model, String gridFile) throws IOException 
    {
	if (gridFile.equals("*"))
	    return;

	if (new File(gridFile).exists())
	{
	    // outputGrid is a file and it exists.  Ensure the 
	    // gridIDs of the grid in the model and the grid in the 
	    // file are the same.
	    String gridID = GeoTessGrid.getGridID(gridFile);
	    if (!gridID.equals(model.getGrid().getGridID()))
		throw new IOException(String.format("Model %s %n"
			+ "Cannot reference grid %s because their gridIDs are different.%n"
			+ "Model gridID    = %s%n"
			+ "GridFile gridID = %s%n",
			model.getCurrentModelFileName(), gridFile,
			model.getGrid().getGridID(), gridID));
	}
	else
	{
	    // outputGrid file does not exist.  Write it to file.
	    model.getGrid().writeGrid(gridFile);
	}

    }


    /**
     * Find the closest point to a supplied geographic location and return
     * information about the point such as, lat, lon, depth, vertex index, etc.
     * 
     * @param args
     *            <ol>
     *            <li>findClosestPoint
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>latitude, degrees
     *            <li>longitude, degrees
     *            <li>depth, km
     *            <li>layer ID. Ignored if -1
     *            <li>ouput: some subset of [lat,lon,depth,radius,vertex,layer,node,point], comma separated, no spaces
     *            </ol>
     * @throws Exception
     */
    public void findClosestPoint(String[] args) throws Exception
    {
	int nmin = 8;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  findClosestPoint%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  latitude, degrees%n"
			    + "  5  --  longitude, degrees%n"
			    + "  6  --  depth, km%n"
			    + "  7  --  layer ID. Ignored if -1.%n"
			    + "  8  --  ouput: some subset of [lat,lon,depth,radius,vertex,layer,node,point],%n"
			    + "         comma separated, no spaces%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	String modelFile = args[arg++];
	String gridDirectory = args[arg++];
	double lat = Double.parseDouble(args[arg++]);
	double lon = Double.parseDouble(args[arg++]);
	double depth = Double.parseDouble(args[arg++]);
	int layerId = Integer.parseInt(args[arg++]);

	String[] outputList = args[arg++].toLowerCase().split(",");

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFile, gridDirectory);

	GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model);

	if (layerId < 0)
	    pos.set(lat, lon, depth);
	else
	    pos.set(layerId, lat, lon, depth);

	Iterator<Entry<Integer, Double>> it = pos.getCoefficients().entrySet().iterator();
	int pointIndex = -1;
	Double cmax = 0.;
	while (it.hasNext())
	{
	    Entry<Integer, Double> e = it.next();
	    if (e.getValue() > cmax)
	    {
		pointIndex = e.getKey();
		cmax = e.getValue();
	    }
	}
	PointMap pm = model.getPointMap();
	int[] map = pm.getPointIndices(pointIndex);

	for (String out : outputList)
	{
	    if (out.startsWith("lat")) System.out.printf(" %1.6f",  model.getEarthShape().getLatDegrees(pm.getPointUnitVector(pointIndex)));
	    else if (out.startsWith("lon")) System.out.printf(" %1.6f",  model.getEarthShape().getLonDegrees(pm.getPointUnitVector(pointIndex)));
	    else if (out.equals("depth")) System.out.printf(" %1.3f",  pm.getPointDepth(pointIndex));
	    else if (out.equals("radius")) System.out.printf(" %1.3f",  pm.getPointRadius(pointIndex));
	    else if (out.startsWith("vertex")) System.out.printf(" %d",  map[0]);
	    else if (out.startsWith("layer")) System.out.printf(" %d",  map[1]);
	    else if (out.startsWith("node")) System.out.printf(" %d",  map[2]);
	    else if (out.startsWith("point")) System.out.printf(" %d",  pointIndex);
	}
	System.out.println();
    }

    /**
     * Output to standard out interpolated values and coefficients for a single point.
     * 
     * @param args
     *            <ol>
     *            <li>interpolatePoint
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>latitude, degrees
     *            <li>longitude, degrees
     *            <li>depth, km
     *            <li>layer ID. If -1, then interpolation is not contrained to a layer
     *            <li>interpolation type horizontal: linear or natural_neighbor (nn)
     *            <li>interpolation type radial: linear or cubic_spline (cs)
     *            <li>reciprocal (true or false)
     *            </ol>
     * @throws Exception
     */
    public void interpolatePoint(String[] args) throws Exception
    {
	int nmin = 11;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  interpolatePoint%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  latitude, degrees%n"
			    + "  5  --  longitude, degrees%n"
			    + "  6  --  depth, km%n"
			    + "  7  --  layer ID. Layer to which interpolation should be constrained. n"
			    + "         If -1 then interpolation is not constrained to a layer.%n"
			    + "  8  --  list of attribute indexes%n"
			    + "  9  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + " 10  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + " 11  --  reciprocal (true or false)%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	String modelFile = args[arg++];
	String gridDirectory = args[arg++];
	double lat = Double.parseDouble(args[arg++]);
	double lon = Double.parseDouble(args[arg++]);
	double depth = Double.parseDouble(args[arg++]);

	int layerId = Integer.parseInt(args[arg++]);

	String attributeList = args[arg++];

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	if (radialType == InterpolatorType.CUBIC_SPLINE)
	    throw new Exception("InterpolatorType.CUBIC_SPLINE is not currently supported.");

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFile, gridDirectory);

	GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model, horizontalType, radialType);

	if (layerId < 0)
	    pos.set(lat, lon, depth);
	else
	    pos.set(layerId, lat, lon, depth);

	int[] attributes = parseList(attributeList, model.getMetaData()
		.getNAttributes() - 1);

	System.out.printf("Lat, lon, depth = %1.6f, %1.6f, %1.3f%n%n", lat, lon, depth);
	System.out.printf("Layer %2d - %s%n%n", pos.getLayerId(),
		model.getMetaData().getLayerNames()[pos.getLayerId()]);

	System.out.print("   Point       Lat        Lon    Depth  Dist(deg)  Coeff ");
	for (int atrib=0; atrib<model.getMetaData().getNAttributes(); ++atrib)
	    System.out.printf(" %10s", model.getMetaData().getAttributeName(atrib));
	System.out.println();

	HashMap<Integer, Double> coeff = pos.getCoefficients();
	for (Integer pt : coeff.keySet())
	{
	    double[] v = model.getPointMap().getPointUnitVector(pt);
	    System.out.printf("%8d %9.5f %10.5f %9.3f %7.3f %9.6f",
		    pt, 
		    model.getEarthShape().getLatDegrees(v),
		    model.getEarthShape().getLonDegrees(v),
		    model.getPointMap().getPointDepth(pt),
		    VectorUnit.angleDegrees(pos.getVector(), v),
		    coeff.get(pt));
	    for (int atrib=0; atrib < attributes.length; ++atrib)
		System.out.printf(" %10.3f%s", 
			reciprocal ? 1./model.getPointMap().getPointValueDouble(pt, attributes[atrib])
				: model.getPointMap().getPointValueDouble(pt, attributes[atrib]),
				reciprocal ? " (inverse)" : "");
	    System.out.println();
	}
	System.out.println();

	for (int i=0; i<attributes.length; ++i)
	    System.out.printf("%-30s %10.3f%s%n", 
		    String.format("%s (%s)", 
			    model.getMetaData().getAttributeNames()[attributes[i]],
			    model.getMetaData().getAttributeUnits()[attributes[i]]),
		    reciprocal ? 1./ pos.getValue(i) : pos.getValue(attributes[i]),
			    reciprocal ? " (inverse)" : "");

	System.out.println();
    }

    /**
     * Output to standard out interpolated values at a single point.
     * 
     * @param args
     *            <ol>
     *            <li>getValues
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>latitude, degrees
     *            <li>longitude, degrees
     *            <li>depth, km
     *            <li>layer ID. Ignored if -1
     *            <li>list of attribute indexes
     *            <li>interpolation type horizontal: linear or natural_neighbor (nn)
     *            <li>interpolation type radial: linear or cubic_spline (cs)
     *            <li>reciprocal (true or false)
     *            </ol>
     * @throws Exception
     */
    public void getValues(String[] args) throws Exception
    {
	int nmin = 11;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  getValues%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  latitude, degrees%n"
			    + "  5  --  longitude, degrees%n"
			    + "  6  --  depth, km%n"
			    + "  7  --  layer ID. Layer to which interpolation should be constrained. %n"
			    + "         If -1 then interpolation is not constrained to a layer.%n"
			    + "  8  --  list of attribute indexes%n"
			    + "  9  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + " 10  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + " 11  --  reciprocal (true or false)", nmin));
	    System.exit(0);
	}

	int arg = 1;
	String modelFile = args[arg++];
	String gridDirectory = args[arg++];
	double lat = Double.parseDouble(args[arg++]);
	double lon = Double.parseDouble(args[arg++]);
	double depth = Double.parseDouble(args[arg++]);

	int layerId = Integer.parseInt(args[arg++]);

	String attributeList = args[arg++];

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFile, gridDirectory);

	int[] attributes = parseList(attributeList, model.getMetaData()
		.getNAttributes() - 1);

	GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model, horizontalType, radialType);

	double[] u = VectorGeo.getVectorDegrees(lat, lon);
	double r = VectorGeo.getEarthRadius(u)-depth;
	if (layerId < 0)
	    pos.set(u, r);
	else
	    pos.set(layerId, u, r);

	switch (model.getMetaData().getDataType())
	{
	case DOUBLE:
	    for (int i : attributes)
		System.out.printf("%1.16g ", reciprocal ? 1./ pos.getValue(i) : pos.getValue(i));
	    break;
	case FLOAT:
	    for (int i : attributes)
		System.out.printf("%1.7g ", reciprocal ? 1./ pos.getValue(i) : pos.getValue(i));
	    break;
	default:
	    for (int i : attributes)
		System.out.printf("%d ", reciprocal ? 
			Math.round(1./ pos.getValue(i)) : Math.round(pos.getValue(i)));
	    break;
	}
	System.out.println();
    }

    /**
     * Output to standard out interpolated values at a single point.
     * 
     * @param args
     *            <ol>
     *            <li>getValue
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>name of file with input lat, lon, depth values
     *            <li>layer ID. Ignored if -1
     *            <li>list of attribute indexes
     *            <li>interpolation type horizontal: linear or natural_neighbor (nn)
     *            <li>interpolation type radial: linear or cubic_spline (cs)
     *            <li>reciprocal (true or false)
     *            </ol>
     * @throws Exception
     */
    public void getValuesFile(String[] args) throws Exception
    {
	int nmin = 8;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  getValuesFile%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  name of file with input lat, lon, depth, layerIndex values%n"
			    + "         In the file, if layerIndex is >= 0, interpolation is constrained to the specified layer%n"
			    + "         If layerIndex is missing or -1 then interpolation is not constrained to any layer%n"
			    + "  5  --  list of attribute indexes%n"
			    + "  6  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + "  7  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + "  8  --  reciprocal (true or false)", nmin));
	    System.exit(0);
	}

	int arg = 1;
	String modelFile = args[arg++];
	String gridDirectory = args[arg++];
	File inputFile = new File(args[arg++]);
	if (!inputFile.exists())
	    throw new IOException(String.format("%nInput file %s does not exist.%n", inputFile.getCanonicalPath()));

	String attributeList = args[arg++];

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFile, gridDirectory);
	DataType dataType = model.getMetaData().getDataType();

	int[] attributes = parseList(attributeList, model.getMetaData()
		.getNAttributes() - 1);

	Scanner input = new Scanner(inputFile);

	GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model, horizontalType, radialType);

	int layerIndex;
	double lat, lon, depth;
	String record, separator;
	String[] ss;
	while (input.hasNext())
	{
	    record = input.nextLine();
	    separator = record.contains(",") ? ", " : " ";

	    ss = record.replaceAll(",", " ").split("\\s+");
	    if (ss.length >= 3)
	    {
		try 
		{
		    lat = Double.parseDouble(ss[0]);
		    lon = Double.parseDouble(ss[1]);
		    depth = Double.parseDouble(ss[2]);
		    layerIndex = ss.length > 3 ? Integer.parseInt(ss[3]) : -1;

		    pos.set(layerIndex, lat, lon, depth);
		    switch (dataType)
		    {
		    case DOUBLE:
			for (int j : attributes)
			    record += String.format("%s%1.16g", separator, reciprocal ? 1./ pos.getValue(j) : pos.getValue(j));
			break;
		    case FLOAT:
			for (int j : attributes)
			    record += String.format("%s%1.7g", separator, reciprocal ? 1./ pos.getValue(j) : pos.getValue(j));
			break;
		    default:
			for (int j : attributes)
			    record += String.format("%s%d", separator, reciprocal ? 
				    Math.round(1./ pos.getValue(j)) : Math.round(pos.getValue(j)));
			break;
		    }
		} 
		catch (Exception e) 
		{
		}
	    }
	    System.out.println(record);
	}
	input.close();
    }

    /**
     * Output to standard out a 2D array of values representing attribute values
     * on a radial profile through the model. The array has dimensions
     * nRadialValues x nAttributes with the attributes index changing fastest.
     * 
     * @param args
     *            <ol>
     *            <li>borehole
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>latitude, degrees
     *            <li>longitude, degrees
     *            <li>maximum radial spacing, km
     *            <li>index of first layer (deepest)
     *            <li>index of last layer (shallowest)
     *            <li>interpolation type, either 'linear', or 'natutal_neighbor'
     *            <li>[ depth | radius ]
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes
     *            </ol>
     * @throws Exception
     */
    public void borehole(String[] args) throws Exception
    {
	int nmin = 13;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply 12 arguments:%n"
			    + "  1  --  borehole%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  latitude, degrees%n"
			    + "  5  --  longitude, degrees%n"
			    + "  6  --  maximum radial spacing, km%n"
			    + "  7  --  index of first layer (deepest)%n"
			    + "  8  --  index of last layer (shallowest)%n"
			    + "  9  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + " 10  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + " 11  --  specify output 'depth' or 'radius'%n"
			    + " 12  --  reciprocal [true | false], report 1./value%n"
			    + " 13  --  list of attribute indexes%n"));
	    System.exit(0);
	}

	int arg = 1;
	String modelFile = args[arg++];
	String gridDirectory = args[arg++];
	double lat = Double.parseDouble(args[arg++]);
	double lon = Double.parseDouble(args[arg++]);

	double maxSpacing = Double.parseDouble(args[arg++]);
	int firstLayer = Integer.parseInt(args[arg++]);
	int lastLayer = Integer.parseInt(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	boolean convertToDepth = args[arg++].toLowerCase().startsWith("d");
	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFile, gridDirectory);

	GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model,
		horizontalType, radialType);

	lastLayer = Math.min(lastLayer, model.getMetaData().getNLayers() - 1);

	pos.set(lastLayer, lat, lon, 0.);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	System.out.print(GeoTessModelUtils.getBoreholeString(pos, maxSpacing,
		firstLayer, lastLayer, convertToDepth, reciprocal, attributes));
    }

    /**
     * Output to standard out a 2D array of values representing attribute values
     * on a radial profile through the model. The array has dimensions
     * nRadialValues x nAttributes with the attributes index changing fastest.
     * Data and radius values obtained from the vertex that is closest to the 
     * specified lat, lon position.
     * 
     * @param args
     *            <ol>
     *            <li>profile
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>latitude, degrees
     *            <li>longitude, degrees
     *            <li>index of first layer (deepest)
     *            <li>index of last layer (shallowest)
     *            <li>[ depth | radius ]
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes
     *            </ol>
     * @throws Exception
     */
    public void profile(String[] args) throws Exception
    {
	int nmin = 10;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply 12 arguments:%n"
			    + "  1  --  profile%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  latitude, degrees%n"
			    + "  5  --  longitude, degrees%n"
			    + "  6  --  index of first layer (deepest)%n"
			    + "  7  --  index of last layer (shallowest)%n"
			    + "  8  --  specify output 'depth' or 'radius'%n"
			    + "  9  --  reciprocal [true | false], report 1./value%n"
			    + " 10  --  list of attribute indexes%n"));
	    System.exit(0);
	}

	int arg = 1;
	String modelFile = args[arg++];
	String gridDirectory = args[arg++];
	double lat = Double.parseDouble(args[arg++]);
	double lon = Double.parseDouble(args[arg++]);

	int firstLayer = Integer.parseInt(args[arg++]);
	int lastLayer = Integer.parseInt(args[arg++]);

	boolean convertToDepth = args[arg++].toLowerCase().startsWith("d");
	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFile, gridDirectory);

	GeoTessPosition pos = GeoTessPosition.getGeoTessPosition(model);
	pos.set(lat, lon, 1000);

	int vertex = pos.getIndexOfClosestVertex();
	double earthRadius = model.getEarthShape().getEarthRadius(model.getVertex(vertex));

	lastLayer = Math.min(lastLayer, model.getMetaData().getNLayers() - 1);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	for (int layer=lastLayer; layer>=firstLayer; --layer)
	{
	    Profile p = model.getProfile(vertex, layer);
	    if (p.getType() == ProfileType.NPOINT)
	    {
		for (int i=p.getNRadii()-1; i>=0; --i)
		{
		    //					System.out.printf("%8d %s ", model.getPointMap().getPointIndex(vertex, layer, i), 
		    //							model.earthShape.getLatLonString(model.getGrid().getVertex(vertex)));
		    System.out.printf("%9.3f", convertToDepth ? 
			    earthRadius - p.getRadius(i) : p.getRadius(i));
		    for (int j=0; j<attributes.length; ++j)
			System.out.printf(" %12.6f", 
				reciprocal ? 1./p.getValue(attributes[j], i) 
					: p.getValue(attributes[j], i));
		    System.out.println();
		}
	    }
	    else
	    {
		System.out.printf("%9.3f", convertToDepth ? 
			earthRadius - p.getRadiusTop() : p.getRadiusTop());
		for (int j=0; j<attributes.length; ++j)
		    System.out.printf(" %12.6f", 
			    reciprocal ? 1./p.getValueTop(attributes[j]) 
				    : p.getValueTop(attributes[j]));
		System.out.println();

		System.out.printf("%9.3f", convertToDepth ? 
			earthRadius - p.getRadiusBottom() : p.getRadiusBottom());
		for (int j=0; j<attributes.length; ++j)
		    System.out.printf(" %12.6f", 
			    reciprocal ? 1./p.getValueBottom(attributes[j]) 
				    : p.getValueBottom(attributes[j]));
		System.out.println();

	    }
	    System.out.println();
	}
    }


    /**
     * Output to standard out a 3D array of values representing attribute values
     * at a contant depth on a regular lat, lon grid of points. The array has
     * dimensions nLat x nLon x nAttributes with the attributes index changing
     * fastest.
     * 
     * @param args
     *            <ol>
     *            <li>mapValuesDepth
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>first latitude, degrees
     *            <li>last latitude, degrees
     *            <li>either dlat (include '.') or nlat (no '.')
     *            <li>first longitude, degrees
     *            <li>last longitude, degrees
     *            <li>either dlon (include '.') or nlon (no '.')
     *            <li>layerID. If &gt;=0 interpolated values constrained to layer radii.
     *            <li>depth, km
     *            <li>interpolation type, either 'linear', or 'natutal_neighbor'
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes 
     *            </ol>
     * @throws Exception
     */
    public void mapValuesDepth(String[] args) throws Exception
    {
	int nmin = 15;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  mapValuesDepth%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  first latitude, degrees%n"
			    + "  5  --  last latitude, degrees%n"
			    + "  6  --  either the number of latitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the latitude values (if the value can be interpreted as a double)%n"
			    + "  7  --  first longitude, degrees%n"
			    + "  8  --  last longitude, degrees%n"
			    + "  9  --  either the number of longitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the longitude values (if the value can be interpreted as a double)%n"
			    + " 10  --  layerID. If >=0 interpolated values constrained to specified layer %n"
			    + "           If < 0  values at specified depth|radius reported ignoring layer boundaries%n"
			    + " 11  --  depth, km%n"
			    + " 12  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + " 13  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + " 14  --  reciprocal [true | false], report 1./value%n"
			    + " 15  --  list of attribute indexes%n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	double[] latitudes = GeoTessModelUtils.getLatitudes(args[arg++],
		args[arg++], args[arg++]);

	double[] longitudes = GeoTessModelUtils.getLongitudes(args[arg++],
		args[arg++], args[arg++], "true");

	int layerId = Integer.parseInt(args[arg++]);
	double depth = Double.parseDouble(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	double[][][] results = GeoTessModelUtils.getMapValuesDepth(model,
		latitudes, longitudes, layerId, depth, horizontalType, radialType, reciprocal,
		attributes);

	for (int i = 0; i < results.length; ++i)
	    for (int j = 0; j < results[i].length; ++j)
	    {
		System.out.printf("%10.6f %11.6f", latitudes[i], longitudes[j]);
		for (int k = 0; k < results[i][j].length; ++k)
		    System.out.printf(" %1.7g", results[i][j][k]);
		System.out.println();
	    }
    }

    /**
     * Output to standard out a 3D array of values representing attribute values
     * at a constant fractional radius in a layer, on a regular lat, lon grid of
     * points. The array has dimensions nLat x nLon x nAttributes with the
     * attributes index changing fastest.
     * 
     * @param args
     *            <ol>
     *            <li>mapValuesLayer
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>first latitude, degrees
     *            <li>last latitude, degrees
     *            <li>either dlat (include '.') or nlat (no '.')
     *            <li>first longitude, degrees
     *            <li>last longitude, degrees
     *            <li>either dlon (include '.') or nlon (no '.')
     *            <li>layer number
     *            <li>fractional radius within the layer (0=bottom, 0.5=middle, 1=top)
     *            <li>interpolation type, either 'linear', or 'natutal_neighbor'
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes
     *            </ol>
     * @throws Exception
     */
    public void mapValuesLayer(String[] args) throws Exception
    {
	int nmin = 15;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  mapValuesLayer%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  first latitude, degrees%n"
			    + "  5  --  last latitude, degrees%n"
			    + "  6  --  either the number of latitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the latitude values (if the value can be interpreted as a double)%n"
			    + "  7  --  first longitude, degrees%n"
			    + "  8  --  last longitude, degrees%n"
			    + "  9  --  either the number of longitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the longitude values (if the value can be interpreted as a double)%n"
			    + " 10  --  layer id%n"
			    + " 11  --  fractional radius (0=bottom, 1=top)%n"
			    + " 12  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + " 13  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + " 14  --  reciprocal [true | false], report 1./value%n"
			    + " 15  --  list of attribute indexes%n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	double[] latitudes = GeoTessModelUtils.getLatitudes(args[arg++],
		args[arg++], args[arg++]);
	double[] longitudes = GeoTessModelUtils.getLongitudes(args[arg++],
		args[arg++], args[arg++], "true");

	int layerId = Integer.parseInt(args[arg++]);

	if (layerId < 0)
	    throw new Exception("Cannot compute values in layer "+layerId);

	double fractionalRadius = Double.parseDouble(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	if (layerId >= model.getNLayers())
	    layerId = model.getNLayers()-1;

	double[][][] results = GeoTessModelUtils.getMapValuesLayer(model,
		latitudes, longitudes, layerId, fractionalRadius, horizontalType, radialType,
		reciprocal, attributes);

	for (int i = 0; i < results.length; ++i)
	    for (int j = 0; j < results[i].length; ++j)
	    {
		System.out.printf("%10.6f %11.6f", latitudes[i], longitudes[j]);
		for (int k = 0; k < results[i][j].length; ++k)
		    System.out.printf(" %1.7g", results[i][j][k]);
		System.out.println();
	    }
    }

    //	/**
    //	 * Output to standard out the edges of all of the triangles that reside on the 
    //	 * top level of the specified tessellation. 
    //	 * @deprecated use extractGrid instead. 
    //	 * @param args
    //	 *            <ol>
    //	 *            <li>triangleEdges
    //	 *            <li>input model or grid file name
    //	 *            <li>relative path to grid directory, otherwise ignored
    //	 *            <li>layerIndex if 2 is a model, tessId if 2 is a grid
    //	 *            </ol>
    //	 * @throws Exception
    //	 */
    //	public void triangleEdges(String[] args) throws Exception
    //	{
    //		int nmin = 4;
    //		if (args.length != nmin)
    //		{
    //			System.out .println(
    //					String.format("%n%nMust supply %d arguments:%n"
    //							+ "  1  --  triangleEdges%n"
    //							+ "  2  --  input model or grid file name%n"
    //							+ "  3  --  relative path to grid directory, otherwise ignored%n"
    //							+ "  4  --  layerIndex if 2 is a model, tessId if 2 is a grid"
    //							, nmin));
    //			System.exit(0);
    //		}
    //
    //		int arg = 1;
    //
    //		File inputFile = new File(args[arg++]);
    //		String gridDirectory = args[arg++];
    //		int tessId=-1;
    //
    //		EarthShape earthShape = EarthShape.WGS84_RCONST;
    //		GeoTessGrid grid = null;
    //		if (GeoTessModel.isGeoTessModel(inputFile))
    //		{
    //			GeoTessModel model = GeoTessModel.getGeoTessModel(inputFile, gridDirectory);
    //			tessId = model.getMetaData().getTessellation(Integer.parseInt(args[arg++]));
    //			grid = model.getGrid();
    //			earthShape = model.getEarthShape();
    //		}
    //		else
    //		{
    //			grid = new GeoTessGrid().loadGrid(inputFile);
    //			tessId = Integer.parseInt(args[arg++]);
    //		}
    //
    //		ArrayList<int[]> edges = grid.getEdges(tessId);
    //		for (int[] edge : edges)
    //			System.out.printf("%1.6f %1.6f %1.6f %1.6f%n", 
    //					earthShape.getLatDegrees(grid.getVertex(edge[0])),
    //					earthShape.getLonDegrees(grid.getVertex(edge[0])),
    //					earthShape.getLatDegrees(grid.getVertex(edge[1])),
    //					earthShape.getLonDegrees(grid.getVertex(edge[1])));
    //	}

    /**
     * Output to standard out a 4D array of values representing attribute values
     * on a regular lat, lon, radius grid of points. The array has dimensions 
     * nLon x nLat x nRadii x nAttributes with the attributes index changing fastest.
     * 
     * @param args
     *            <ol>
     *            <li>values3DBlock
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>first latitude, degrees
     *            <li>last latitude, degrees
     *            <li>either dlat (include '.') or nlat (no '.')
     *            <li>first longitude, degrees
     *            <li>last longitude, degrees
     *            <li>either dlon (include '.') or nlon (no '.')
     *            <li>index of first layer
     *            <li>index of last layer
     *            <li>radialDimension: depth or radius.
     *            <li>max radial spacing of points, in km.
     *            <li>interpolation type horizontal: linear or natural_neighbor (nn)
     *            <li>interpolation type radial: linear or cubic_spline (cs)
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes
     *            </ol>
     * @throws Exception
     */
    public void values3DBlock(String[] args) throws Exception
    {
	int nmin = 17;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  values3DBlock%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  first latitude, degrees%n"
			    + "  5  --  last latitude, degrees%n"
			    + "  6  --  either the number of latitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the latitude values (if the value can be interpreted as a double)%n"
			    + "  7  --  first longitude, degrees%n"
			    + "  8  --  last longitude, degrees%n"
			    + "  9  --  either the number of longitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the longitude values (if the value can be interpreted as a double)%n"
			    + " 10  --  index of first layer id%n"
			    + " 11  --  index of last layer id%n"
			    + " 12  --  radialDimension: depth or radius.%n"
			    + " 13  --  max radial spacing of points%n"
			    + " 14  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + " 15  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + " 16  --  reciprocal [true | false], report 1./value%n"
			    + " 17  --  list of attribute indexes%n"
			    //+ " 18  --  output format (0 or 1 or 2)%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	double[] latitudes = GeoTessModelUtils.getLatitudes(args[arg++],
		args[arg++], args[arg++]);
	double[] longitudes = GeoTessModelUtils.getLongitudes(args[arg++],
		args[arg++], args[arg++], "true");

	int firstLayer = Integer.parseInt(args[arg++]);
	int lastLayer = Integer.parseInt(args[arg++]);

	if (firstLayer < 0)
	    throw new Exception("index of firstLayer cannot be "+firstLayer);

	String radialDimension = args[arg++];

	double maxRadialSpacing = Double.parseDouble(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	//int outputFormat = Integer.parseInt(args[arg++]);

	if (lastLayer >= model.getNLayers())
	    lastLayer = model.getNLayers()-1;

	double[][][][] values3D = GeoTessModelUtils.getValues3D(model,
		latitudes, longitudes, firstLayer, lastLayer, radialDimension, maxRadialSpacing, 
		horizontalType, radialType, reciprocal, attributes);

	// output lat, lon, radius, value on separate records.
	String format = "%1.5f %1.5f %1.3f";
	if (model.getMetaData().getDataType() == DataType.DOUBLE)
	    format = format+" %1.16g%n";
	else 
	    format = format+" %1.7g%n";

	for (int i = 0; i < values3D.length; ++i)
	{
	    double[][][] vlat = values3D[i];
	    for (int j = 0; j < vlat.length; ++j)
	    {
		double[][] vr = vlat[j];
		for (int k = 0; k < vr.length; ++k)
		{
		    double[] va = vr[k];
		    for (int a=1; a < va.length; ++a)
			System.out.printf(format, longitudes[i], latitudes[j], va[0], va[a]);
		}
	    }
	}
    }

    /**
     * Output to standard out a 2D array of values representing the depth or
     * radius (in km) of the top or bottom of a specified layer on a regular
     * lat, lon grid. The output array has dimensions nLat x nLon with longitude
     * index changing fastest.
     * 
     * @param args
     *            <ol>
     *            <li>mapLayerBoundary
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>first latitude, degrees
     *            <li>last latitude, degrees
     *            <li>either dlat (include '.') or nlat (no '.')
     *            <li>first longitude, degrees
     *            <li>last longitude, degrees
     *            <li>either dlon (include '.') or nlon (no '.')
     *            <li>layer number
     *            <li>[ top | bottom ] of layer
     *            <li>[ depth | radius ]
     *            <li>interpolation type, either 'linear', or 'natutal_neighbor'
     *            </ol>
     * @throws Exception
     */
    public void mapLayerBoundary(String[] args) throws Exception
    {
	int nmin = 13;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  mapLayerBoundary%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  first latitude, degrees%n"
			    + "  5  --  last latitude, degrees%n"
			    + "  6  --  either the number of latitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the latitude values (if the value can be interpreted as a double)%n"
			    + "  7  --  first longitude, degrees%n"
			    + "  8  --  last longitude, degrees%n"
			    + "  9  --  either the number of longitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the longitude values (if the value can be interpreted as a double)%n"
			    + " 10  --  layer id%n"
			    + " 11  --  top or bottom of layer%n"
			    + " 12  --  ['depth' or 'radius']%n"
			    + " 13  --  interpolation type: linear or natural_neighbor (nn)%n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	double[] latitudes = GeoTessModelUtils.getLatitudes(args[arg++],
		args[arg++], args[arg++]);
	double[] longitudes = GeoTessModelUtils.getLongitudes(args[arg++],
		args[arg++], args[arg++], "true");

	int layerId = Integer.parseInt(args[arg++]);

	boolean top = args[arg++].startsWith("t");

	boolean convertToDepth = args[arg++].startsWith("d");

	String s = args[arg++].toUpperCase();
	if (s.equals("NN"))
	    s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	double[][] results = GeoTessModelUtils
		.getMapLayerBoundary(model, latitudes, longitudes, layerId,
			top, convertToDepth, horizontalType);

	for (int i = 0; i < results.length; ++i)
	    for (int j = 0; j < results[i].length; ++j)
		System.out.printf("%1.7g %1.7g %1.7g%n", latitudes[i], longitudes[j], results[i][j]);
    }

    /**
     * Output to standard out a 2D array of values representing the thickness (in km) 
     * of the specified layers on a regular lat, lon grid. The output array has dimensions 
     * nLat x nLon  with nLon index changing fastest.  Thickness values are the sum of the 
     * thicknesses from bottom of first layer to top of last layer.
     * 
     * @param args
     *            <ol>
     *            <li>mapLayerThickness
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>first latitude, degrees
     *            <li>last latitude, degrees
     *            <li>either dlat (include '.') or nlat (no '.')
     *            <li>first longitude, degrees
     *            <li>last longitude, degrees
     *            <li>either dlon (include '.') or nlon (no '.')
     *            <li>index of first layer
     *            <li>index of last layer. . Thickness will include first through last layer, inclusive
     *            <li>interpolation type, either 'linear', or 'natutal_neighbor'
     *            </ol>
     * @throws Exception
     */
    public void mapLayerThickness(String[] args) throws Exception
    {
	int nmin = 12;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  mapLayerThickness%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  first latitude, degrees%n"
			    + "  5  --  last latitude, degrees%n"
			    + "  6  --  either the number of latitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the latitude values (if the value can be interpreted as a double)%n"
			    + "  7  --  first longitude, degrees%n"
			    + "  8  --  last longitude, degrees%n"
			    + "  9  --  either the number of longitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the longitude values (if the value can be interpreted as a double)%n"
			    + " 10  --  first layer%n"
			    + " 11  --  last layer. Thickness will include first through last layer, inclusive%n"
			    + " 12  --  interpolation type: linear or natural_neighbor (nn)%n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	double[] latitudes = GeoTessModelUtils.getLatitudes(args[arg++],
		args[arg++], args[arg++]);
	double[] longitudes = GeoTessModelUtils.getLongitudes(args[arg++],
		args[arg++], args[arg++], "true");

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	int firstLayer = Integer.parseInt(args[arg++]);
	int lastLayer = Integer.parseInt(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("NN"))
	    s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);

	double[][] results = GeoTessModelUtils.getMapLayerThickness(model, 
		latitudes, longitudes, firstLayer, lastLayer, horizontalType);

	for (int i = 0; i < results.length; ++i)
	    for (int j = 0; j < results[i].length; ++j)
		System.out.printf("%1.7g %1.7g %1.7g%n", latitudes[i], longitudes[j], results[i][j]);
    }

    /**
     * Output to standard out a 3D array of values representing attribute values
     * on a vertical slice through the model. The array has dimensions
     * nDistanceValues x nRadialValues x nAttributes with the attributes index
     * changing fastest.
     * 
     * @param args
     *            <ol>
     *            <li>slice
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>latitude of first point, degrees
     *            <li>longitude of first point, degrees
     *            <li>latitude of last point, degrees
     *            <li>longitude of last point, degrees
     *            <li>shortest path (true or false). If false, go the long way around the earth (path length &gt; 180 degrees).
     *            <li>nPoints
     *            <li>maximum radial spacing, km
     *            <li>index of first layer (deepest)
     *            <li>index of last layer (shallowest)
     *            <li>interpolation type, either 'linear', or 'natutal_neighbor'
     *            <li>spatialCoordinates. Subset of
     *            distance,depth,radius,x,y,z,lat,lon
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes
     *            </ol>
     * @throws Exception
     */
    public void slice(String[] args) throws Exception
    {
	int nmin = 17;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  slice%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  first latitude, degrees%n"
			    + "  5  --  first longitude, degrees%n"
			    + "  6  --  last latitude, degrees%n"
			    + "  7  --  last longitude, degrees%n"
			    + "  8  --  shortest path (true or false). If false, go the long way around the earth (path length > 180 degrees).%n"
			    + "  9  --  npoints%n"
			    + " 10  --  maximum radial spacing, km%n"
			    + " 11  --  index of first layer (deepest)%n"
			    + " 12  --  index of last layer (shallowest)%n"
			    + " 13  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + " 14  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + " 15  --  spatialCoordinates. Subset of distance,depth,radius,x,y,z,lat,lon%n"
			    + " 16  --  reciprocal [true | false], report 1./value%n"
			    + " 17  --  list of attribute indexes%n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	double lat1 = Double.parseDouble(args[arg++]);
	double lon1 = Double.parseDouble(args[arg++]);
	double lat2 = Double.parseDouble(args[arg++]);
	double lon2 = Double.parseDouble(args[arg++]);

	boolean shortestPath = args[arg++].equalsIgnoreCase("true");

	int nx = Integer.parseInt(args[arg++]);
	double rspacing = Double.parseDouble(args[arg++]);
	int firstLayer = Integer.parseInt(args[arg++]);
	int lastLayer = Integer.parseInt(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	// boolean convertToDepth = args[arg++].startsWith("d");
	String spatialCoordinates = args[arg++];

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	lastLayer = Math.min(lastLayer, model.getMetaData().getNLayers() - 1);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	GreatCircle greatCircle = new GreatCircle(model.getEarthShape().getVectorDegrees(lat1, lon1),
		model.getEarthShape().getVectorDegrees(lat2, lon2), shortestPath);

	//		System.out.println("X direction (lat, lon) = "+model.earthShape.getLatLonString(greatCircle.getTransform()[0]));
	//		System.out.println("Y direction (lat, lon) = "+model.earthShape.getLatLonString(greatCircle.getTransform()[1]));
	//		System.out.println("Z direction (lat, lon) = "+model.earthShape.getLatLonString(greatCircle.getTransform()[2]));

	double[][][] results = GeoTessModelUtils.getSlice(model,
		greatCircle, nx, rspacing,
		firstLayer, lastLayer, horizontalType, radialType, spatialCoordinates,
		reciprocal, attributes);

	for (int i = 0; i < results.length; ++i)
	    for (int j = 0; j < results[i].length; ++j)
	    {
		for (int k = 0; k < results[i][j].length; ++k)
		    System.out.printf(" %1.7g", results[i][j][k]);
		System.out.println();
	    }
    }

    /**
     * Output to standard out a 3D array of values representing attribute values
     * on a vertical slice through the model. The array has dimensions
     * nDistanceValues x nRadialValues x nAttributes with the attributes index
     * changing fastest.
     * 
     * @param args
     *            <ol>
     *            <li>slice
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>latitude of first point, degrees
     *            <li>longitude of first point, degrees
     *            <li>distance to last point, degrees
     *            <li>azimuth to last point, degrees
     *            <li>nPoints
     *            <li>maximum radial spacing, km
     *            <li>index of first layer (deepest)
     *            <li>index of last layer (shallowest)
     *            <li>interpolation type, either 'linear', or 'natutal_neighbor'
     *            <li>spatialCoordinates. Subset of
     *            distance,depth,radius,x,y,z,lat,lon
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes
     *            </ol>
     * @throws Exception
     */
    public void sliceDistAz(String[] args) throws Exception
    {
	int nmin = 16;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  sliceDistAz%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  first latitude, degrees%n"
			    + "  5  --  first longitude, degrees%n"
			    + "  6  --  distance to last point, degrees%n"
			    + "  7  --  azimuth to last point, degrees%n"
			    + "  8  --  npoints%n"
			    + "  9  --  maximum radial spacing, km%n"
			    + " 10  --  index of first layer (deepest)%n"
			    + " 11  --  index of last layer (shallowest)%n"
			    + " 12  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + " 13  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + " 14  --  spatialCoordinates. Subset of distance,depth,radius,x,y,z,lat,lon%n"
			    + " 15  --  reciprocal [true | false], report 1./value%n"
			    + " 16  --  list of attribute indexes%n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	double lat1 = Double.parseDouble(args[arg++]);
	double lon1 = Double.parseDouble(args[arg++]);
	double dist = Double.parseDouble(args[arg++]);
	double az = Double.parseDouble(args[arg++]);

	int nx = Integer.parseInt(args[arg++]);
	double rspacing = Double.parseDouble(args[arg++]);
	int firstLayer = Integer.parseInt(args[arg++]);
	int lastLayer = Integer.parseInt(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	// boolean convertToDepth = args[arg++].startsWith("d");
	String spatialCoordinates = args[arg++];

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	lastLayer = Math.min(lastLayer, model.getMetaData().getNLayers() - 1);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	GreatCircle greatCircle = new GreatCircle(model.getEarthShape().getVectorDegrees(lat1, lon1), 
		Math.toRadians(dist), Math.toRadians(az));

	//		System.out.println("X direction (lat, lon) = "+model.earthShape.getLatLonString(greatCircle.getTransform()[0]));
	//		System.out.println("Y direction (lat, lon) = "+model.earthShape.getLatLonString(greatCircle.getTransform()[1]));
	//		System.out.println("Z direction (lat, lon) = "+model.earthShape.getLatLonString(greatCircle.getTransform()[2]));

	double[][][] results = GeoTessModelUtils.getSlice(model,
		greatCircle, nx, rspacing,
		firstLayer, lastLayer, horizontalType, radialType, spatialCoordinates,
		reciprocal, attributes);

	for (int i = 0; i < results.length; ++i)
	    for (int j = 0; j < results[i].length; ++j)
	    {
		for (int k = 0; k < results[i][j].length; ++k)
		    System.out.printf(" %1.7g", results[i][j][k]);
		System.out.println();
	    }
    }

    private static void checkUpdateModelDescriptionArgs(String[] args) {
	int nmin = 5;
	if (args.length >= nmin) return;

	System.out .println(
		String.format("%n%nMust supply %d arguments:%n"
			+ "  1  --  updateModelDescription%n"
			+ "  2  --  input model file name%n"
			+ "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			+ "  4  --  file containing updated description%n"
			+ "  5  --  output model file name%n"
			, nmin));
	System.exit(0);
    }

    /**
     * Updates the description field of a GeoTessModel.
     *
     * @param args
     *            <ol>
     *            <li>updateModelDescription
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>file containing updated description.
     *            <li>output model file name
     *            </ol>
     * @throws IOException
     */
    public static void updateModelDescription(String[] args) throws IOException {
	checkUpdateModelDescriptionArgs(args);

	GeoTessModel model = new GeoTessModel(new File(args[1]), args[2]);
	String outputFilename = args[4];

	Scanner input = new Scanner(new File(args[3]));
	StringBuffer description = new StringBuffer();
	while (input.hasNextLine())
	{
	    String line = input.nextLine();
	    description.append(line);
	    if (input.hasNextLine())
		description.append("\n");
	}

	model.getMetaData().setDescription(description.toString());

	System.out.println(model);

	if (model.getGrid().getGridInputFile() == null 
		|| model.getGrid().getGridInputFile().equals(model.getMetaData().getInputModelFile()))
	    model.writeModel(outputFilename);
	else 
	    model.writeModel(outputFilename, model.getGrid().getGridInputFile());

	System.out.println("Wrote model to " + outputFilename);
    }

    private static void checkLayerAverageArgs(String[] args) {
	int nmin = 4;
	if (args.length >= nmin) return;

	System.out .println(
		String.format("%n%nMust supply %d arguments:%n"
			+ "  1  --  vtkLayerAverage%n"
			+ "  2  --  input model file name%n"
			+ "  3  --  output file name%n"
			+ "  4  --  layer name or index%n"
			+ "  5  --  (optional) list of attribute names or indexes.  If none, uses all attributes.%n", nmin));
	System.exit(0);
    }

    private static int parseIndex(String index) {
	try {
	    return Integer.parseInt(index);
	} catch (NumberFormatException e) {
	    return -1;
	}
    }

    private static String[] getAttributeUnitPair(GeoTessMetaData metaData, String name) {
	String[] pair = new String[2];
	int nAttributes = metaData.getNAttributes();

	int attIndex = metaData.getAttributeIndex(name.toUpperCase());
	if (attIndex < 0) attIndex = parseIndex(name);
	if (attIndex < 0) {
	    System.err.println("Attribute name or index \"" + name + "\" not recognized.");
	    System.exit(0);
	}
	if (attIndex < 0 || attIndex > nAttributes - 1) {
	    System.err.println("Attribute index \"" + name + "\" does not correspond to an existing attribute.");
	    System.exit(0);
	}

	pair[0] = metaData.getAttributeName(attIndex);
	pair[1] = metaData.getAttributeUnit(attIndex);
	return pair;
    }

    private static String[][] parseAttributeList(String[] attributeList, GeoTessMetaData metaData) {
	String[][] outputTuple = new String[2][attributeList.length];

	for (int i = 0; i < outputTuple[0].length; i++) {
	    String[] attributeUnitPair = getAttributeUnitPair(metaData, attributeList[i]);
	    outputTuple[0][i] = attributeUnitPair[0];
	    outputTuple[1][i] = attributeUnitPair[1];
	}
	return outputTuple;
    }


    // Gets the attribute list for the layerAverage function. If the user has not specified any attributes, takes
    // all of them instead.
    private static String[][] getAttributeUnitTuple(GeoTessModel model, String[] args, int startIndex, int endIndex) {
	GeoTessMetaData metaData = model.getMetaData();

	// return all attributes
	if (args.length == startIndex)
	    return new String[][] {metaData.getAttributeNames(), metaData.getAttributeUnits()};

	    // return user-specified set of attributes
	    return parseAttributeList(Arrays.copyOfRange(args, startIndex, endIndex), metaData);
    }

    private static double average(double[] arr) {
	double sum = 0;
	for (double val : arr) sum += val;
	return sum / (double)arr.length;
    }

    private static int getLayerIndex(GeoTessModel model, String name) {
	int layerIndex = model.getMetaData().getLayerIndex(name.toUpperCase());
	if (layerIndex < 0) layerIndex = parseIndex(name);
	if (layerIndex < 0) {
	    System.err.println("Layer name or index \"" + name + "\" not recognized.");
	    System.exit(0);
	}
	return layerIndex;
    }

    private static boolean hasInvertFlag(String[] args, int index) {
	if (args.length < index + 1) return false;
	if (!args[index].equalsIgnoreCase("-i")) return false;
	return true;
    }

    /**
     * Output to vtk file the averages of the specified attributes at each vertex.
     *
     * @param args
     *            <ol>
     *            <li>vtkLayerAverage
     *            <li>input model file name
     *            <li>output file name
     *            <li>layer name or index
     *            <li>(optional) list of attribute names or indexes. If none, uses all attributes.
     *            <li>(optional) -i if present will invert the model.
     *            </ol>
     * @throws Exception
     */
    public static void vtkLayerAverage(String[] args) throws Exception {
	// parse the input arguments
	checkLayerAverageArgs(args);
	GeoTessModel model = new GeoTessModel(new File(args[1]));
	String outputFilename = args[2];
	int layerIndex = getLayerIndex(model, args[3]);
	boolean invert = hasInvertFlag(args, args.length-1);

	// invert model if required, and get attributes and units
	if (invert) invertModel(model);
	String[][] attUnitTuple = getAttributeUnitTuple(model, args, 4, invert ? args.length-1 : args.length);

	// make a new model
	GeoTessMetaData metaData = new GeoTessMetaData();
	metaData.setAttributes(String.join("; ", attUnitTuple[0]), String.join(" ; ", attUnitTuple[1]));
	metaData.setLayerNames("AVERAGE");
	metaData.setDataType("float");
	metaData.setModelSoftwareVersion(model.getClass().getCanonicalName());

	// make a new model with new metadata and same grid as old model
	GeoTessModel newModel = new GeoTessModel(model.getGridRotated(), metaData);

	// loop over all the vertices
	for (int vertIndex = 0; vertIndex < newModel.getNVertices(); vertIndex++) {
	    Profile profile = model.getProfile(vertIndex, layerIndex);

	    // handle case of an empty profile
	    if (profile.getType() == ProfileType.EMPTY || profile.getType() == ProfileType.SURFACE_EMPTY)
		newModel.setProfile(vertIndex, 0, profile.copy());
	    else {
		float[] avgAttributes = new float[newModel.getNAttributes()];

		// take average of each attribute over profile
		for (int attributeIndex = 0; attributeIndex < avgAttributes.length; attributeIndex++)
		    avgAttributes[attributeIndex] = (float)average(profile.getValues(attributeIndex));

		newModel.setProfile(vertIndex, Data.getDataFloat(avgAttributes));
	    }
	}
	GeoTessModelUtils.vtk(newModel, outputFilename, 0, false, null);

	System.out.println(newModel.toString());
    }

    private static void checkReciprocalModelArgs(String[] args) {
	int minargs = 3;
	if (args.length == minargs) return;
	if (args.length >= minargs && (args.length - minargs) % 2 == 0) return;

	System.out .println(
		String.format("%n%nMust supply %d arguments:%n"
			+ "  1  --  reciprocalModel%n"
			+ "  2  --  input model file name%n"
			+ "  3  --  output file name%n"
			+ "  4  --  list of inverted label pairs (i.e. vel slow ...)%n", minargs));
	System.exit(0);
    }

    private static class CaselessBiMap {
	private Map<String, String> biMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	public CaselessBiMap(String[]... pairs) {for (String[] pair : pairs) put(pair[0], pair[1]);}

	private String get(String key) {return biMap.get(key);}

	private void put(String keyA, String keyB) {
	    biMap.put(keyA, keyB);
	    biMap.put(keyB, keyA);
	}
    }

    private static String[][] bidirectionalPairs = {
	    {"PSLOWNESS", "PVELOCITY"},
	    {"SSLOWNESS", "SVELOCITY"},
	    {"VELOCITY", "SLOWNESS"},
	    {"km/sec", "sec/km"},
	    {"km\\sec", "sec\\km"},
	    {"km/s", "s/km"},
	    {"km\\s", "s\\km"}};
    private static CaselessBiMap inverseMap = new CaselessBiMap(bidirectionalPairs);

    private static String[] mapToInverse(String[] attributes, CaselessBiMap userInverseMap) {
	String[] invertedAtts = new String[attributes.length];

	for (int i = 0; i < attributes.length; i++) {
	    invertedAtts[i] = userInverseMap == null ? null : userInverseMap.get(attributes[i]);
	    if (invertedAtts[i] == null)
		invertedAtts[i] = inverseMap.get(attributes[i]);
	    if (invertedAtts[i] == null) {
		System.err.println("Cannot invert label " + attributes[i]);
		throw new IllegalArgumentException();
	    }
	}
	return invertedAtts;
    }

    public static CaselessBiMap getUserInverseMap(String args[]) {
	CaselessBiMap userBiMap = new CaselessBiMap();

	for (int i = 0; i < args.length; i += 2) userBiMap.put(args[i], args[i + 1]);

	return userBiMap;
    }

    /**
     * Output a reciprocal GeoTessModel (a GeoTessModel with all of the values inverted).
     *
     * @param args
     *            <ol>
     *            <li>reciprocalModel
     *            <li>input model file name
     *            <li>output model file name
     *            <li>starting label
     *            <li>inverted label
     *            </ol>
     * @throws Exception
     */
    public static void reciprocalModel(String[] args) throws Exception {
	// parse the input arguments
	checkReciprocalModelArgs(args);
	GeoTessModel model = new GeoTessModel(new File(args[1]));
	String outputFileName = args[2];
	CaselessBiMap userInverseMap = args.length <= 3 ? null : getUserInverseMap(Arrays.copyOfRange(args, 3, args.length));

	invertModel(model, userInverseMap);
	model.writeModel(new File(outputFileName));
    }

    private static void invertModel(GeoTessModel model) { invertModel(model, null); }

    private static void invertModel(GeoTessModel model, CaselessBiMap userInverseMap) {
	// Invert attribute unit strings. This operation is limited and might be incorrect for some models.
	GeoTessMetaData metaData = model.getMetaData();
	String inverseAttributes = String.join("; ", mapToInverse(metaData.getAttributeNames(), userInverseMap));
	String inverseUnits = String.join(" ; ", mapToInverse(metaData.getAttributeUnits(), userInverseMap));
	metaData.setAttributes(inverseAttributes, inverseUnits);

	// loop over all the vertices
	for (int vertIndex = 0; vertIndex < model.getNVertices(); vertIndex++) {
	    for (int layerIndex = 0; layerIndex < model.getNLayers(); layerIndex++) {
		// profile data is a list of n Data objects where n is the number of radii in the profile
		Profile profile = model.getProfile(vertIndex, layerIndex);

		// if profile is empty, do nothing
		if (profile.getType() == ProfileType.EMPTY || profile.getType() == ProfileType.SURFACE_EMPTY) continue;

		// invert each data value
		for (int dataIndex = 0; dataIndex < profile.getData().length; dataIndex++) {
		    float[] invertedAttVals = new float[model.getNAttributes()];

		    for (int attIndex = 0; attIndex < model.getNAttributes(); attIndex++)
			invertedAttVals[attIndex] = (float) (1. / profile.getValues(attIndex)[dataIndex]);

		    profile.setData(dataIndex, Data.getDataFloat(invertedAttVals));
		}
	    }
	}
    }

    /**
     * Renames a single layer in a GeoTess model.
     *
     * @param args
     *            <ol>
     *            <li>renameLayer
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>output model file name
     *            <li>index of old layer name
     *            <li>new layer name
     *            </ol>
     * @throws Exception
     */
    public static void renameLayer(String[] args) throws Exception {

	int nmin = 6;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  renameLayer%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  output file name%n"
			    + "  5  --  index of old layer name%n"
			    + "  6  --  new layer name%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	String outputFile = args[arg++];
	int index = Integer.parseInt(args[arg++]);		
	String newName = args[arg++];

	if (modelFileName.getAbsolutePath().equals(new File(outputFile).getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	// rename the specified layer
	GeoTessMetaData metaData = model.getMetaData();
	String[] layerNames = metaData.getLayerNames();
	layerNames[index] = newName;
	metaData.setLayerNames(layerNames);

	// output new model
	model.writeModel(new File(outputFile));
    }

    /**
     * Renames a single layer in a GeoTess model.
     *
     * @param args
     *            <ol>
     *            <li>renameLayers
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>output model file name
     *            <li>list of new names.  Must provide names for all layers.
     *            </ol>
     * @throws Exception
     */
    public static void renameLayers(String[] args) throws Exception {

	int nmin = 4;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  renameLayers%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  output file name%n"
			    + "  5+ --  list of new layer names.  Must provide names for all layers.%n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	String outputFile = args[arg++];

	if (modelFileName.getAbsolutePath().equals(new File(outputFile).getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	// rename the specified layer
	GeoTessMetaData metaData = model.getMetaData();
	String[] layerNames = metaData.getLayerNames();

	if (args.length-layerNames.length != 4)
	    throw new Exception("Must provide "+layerNames.length+" layer names.");


	for (int i=0; i<layerNames.length; ++i)
	    layerNames[i] = args[i+4];

	metaData.setLayerNames(layerNames);

	// output new model
	model.writeModel(new File(outputFile));
    }

    /**
     * Output to vtk file attribute values at a specified range of depths.
     * 
     * @param args
     *            <ol>
     *            <li>vtkDepths
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>output file name with vtk extension
     *            <li>layer index
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes.  
     *            </ol>
     * @throws Exception
     */
    public void vtkDepths(String[] args) throws Exception
    {
	int nmin = 10;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkDepths%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  output file name%n"
			    + "  5  --  layerID. If >=0 interpolated values constrained to layer radii.%n"
			    + "  6  --  first depth, km%n"
			    + "  7  --  last depth, km%n"
			    + "  8  --  depth spacing, km%n"
			    + "  9  --  reciprocal [true | false], report 1./value%n"
			    + " 10  --  list of attribute indexes%n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	String outputFile = args[arg++];

	if (modelFileName.getAbsolutePath().equals(new File(outputFile).getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	int layerid = Integer.parseInt(args[arg++]);

	double firstDepth = Double.parseDouble(args[arg++]);
	double lastDepth = Double.parseDouble(args[arg++]);
	double spacing = Double.parseDouble(args[arg++]);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	GeoTessModelUtils
	.vtkDepths(model, outputFile, InterpolatorType.LINEAR, InterpolatorType.LINEAR, layerid,
		firstDepth, lastDepth, spacing, reciprocal, attributes);

	System.out.println("VTK output successfully written to " + outputFile);
    }

    /**
     * Output to vtk file attribute values at a specified range of depths.
     * 
     * @param args
     *            <ol>
     *            <li>vtkDepths
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>output file name with vtk extension
     *            <li>layer index
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes.  
     *            </ol>
     * @throws Exception
     */
    public void vtkDepths2(String[] args) throws Exception
    {
	int nmin = 8;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkDepths2%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  output file name%n"
			    + "  5  --  layerID. If >=0 interpolated values constrained to layer radii.%n"
			    + "  6  --  depths: comma-separated list of depths with no spaces%n"
			    + "  7  --  reciprocal [true | false], report 1./value%n"
			    + "  8  --  list of attribute indexes%n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	String outputFile = args[arg++];

	if (modelFileName.getAbsolutePath().equals(new File(outputFile).getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	int layerid = Integer.parseInt(args[arg++]);

	String[] z = args[arg++].split(",");
	double[] depths = new double[z.length];
	for (int i=0; i<z.length; ++i) depths[i] = Double.parseDouble(z[i]);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	GeoTessModelUtils .vtkDepths(model, outputFile, InterpolatorType.LINEAR, InterpolatorType.LINEAR, layerid,
		depths, reciprocal, attributes);

	System.out.println("VTK output successfully written to " + outputFile);
    }

    /**
     * Output to vtk file the combined thickness of a range of layers.
     * 
     * @param args
     *            <ol>
     *            <li>vtkLayerThickness
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>output file name
     *            <li>index of first layer
     *            <li>index of last layer. . Thickness will include first through last layer, inclusive
     *            <li>interpolation type, either linear, or natutal_neighbor (nn)
     *            </ol>
     * @throws Exception
     */
    public void vtkLayerThickness(String[] args) throws Exception
    {
	// vtk model fileName layerId reciprocal attributes
	int nmin = 7;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkLayerThickness%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  output file name%n"
			    + "  5  --  index of first layer%n"
			    + "  6  --  index of last layer. . Thickness will include first through last layer, inclusive%n"
			    + "  7  --  interpolation type, either linear, or natutal_neighbor (nn) %n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	String outputFile = args[arg++];

	if (modelFileName.getAbsolutePath().equals(new File(outputFile).getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	int firstLayer = Integer.parseInt(args[arg++]);
	int lastLayer = Integer.parseInt(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	GeoTessModelUtils.vtkLayerThickness(model, outputFile, firstLayer, lastLayer, horizontalType);

	System.out.println("VTK output successfully written to " + outputFile);
    }

    /**
     * Output to vtk file the depth or elevation to the top of each layer boundary.
     * 
     * @param args
     *            <ol>
     *            <li>vtkLayerBoundary
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>output file name
     *            <li>either 'depth' or 'elevation'
     *            <li>interpolation type, either linear, or natutal_neighbor (nn)
     *            </ol>
     * @throws Exception
     */
    public void vtkLayerBoundary(String[] args) throws Exception
    {
	int nmin = 6;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkLayerBoundary%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  output file name%n"
			    + "  5  --  either 'depth' or 'elevation'%n"
			    + "  6  --  interpolation type, either linear, or natutal_neighbor (nn) %n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	String outputFile = args[arg++];

	if (modelFileName.getAbsolutePath().equals(new File(outputFile).getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	String z = args[arg++];

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	GeoTessModelUtils.vtkLayerBoundary(model, outputFile, z, horizontalType);

	System.out.println("VTK output successfully written to " + outputFile);
    }

    /**
     * Output to vtk file attribute values at the top of a specified set of layers.
     * 
     * @param args
     *            <ol>
     *            <li>vtkLayers
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>output file names. Substring %d will be replaced with layer index.
     *            <li>layer index
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes. 
     *            </ol>
     * @throws Exception
     */
    public void vtkLayers(String[] args) throws Exception
    {
	// vtk model fileName layerId reciprocal attributes
	int nmin = 7;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkLayers%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  output file names. Substring %%d will be replaced with layer index and/or %%s will be %n"
			    + "         replaced with layer name. One of them required for models with more than 1 layer%n"
			    + "  5  --  list of layer indexes%n"
			    + "  6  --  reciprocal [true | false], report 1./value%n"
			    + "  7  --  list of attribute indexes%n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];

	String outputFile = args[arg++];

	if (modelFileName.getAbsolutePath().equals(new File(outputFile).getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	int[] layids = parseList(args[arg++], model.getNLayers() - 1);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	if (model.getNLayers() > 1 && !outputFile.contains("%s") && !outputFile.contains("%d"))
	    throw new Exception("\noutput file name must contain substring '%s' and/or '%d' "
		    + "because model has more than one layer.");

	for (int lid : layids)
	{
	    String fout = outputFile.replaceAll("%s", model.getMetaData().getLayerName(lid))
		    .replaceAll("%d", String.format("%d", lid));

	    GeoTessModelUtils.vtk(model, fout, lid, reciprocal, attributes);

	    System.out.println("VTK output successfully written to "
		    + fout);
	}
    }

    /**
     * Construct a new GeoTessModel that has Data values that are a function of the 
     * Data values in the two supplied GeoTessModels.
     * <p>
     * Supported functions:
     * <ol start=0>
     * <li>v1 - v0;  // simple difference
     * <li>1/v1 - 1/v0; // difference of reciprocals  
     * <li>100 * (v1 - v0) / v0;  // % change 
     * <li>100.*(1/v1 - 1/v0) / (1/v0);  // % change of reciprocals
     * </ol>
     * If function is anything else, new model will be populated with NaN.
     * @param args
     *            <ol>
     *            <li>function
     *            <li>input model0 file name
     *            <li>relative path to grid0 directory (not used if grid stored in model file)
     *            <li>attribute index 0

     *            <li>input model1 file name
     *            <li>relative path to grid1 directory (not used if grid stored in model file)
     *            <li>attribute index 1

     *            <li>geometry model file name
     *            <li>relative path to geometry grid directory (not used if grid stored in model file)

     *            <li>output file name
     *            <li>reference to grid file ( if '.', then grid and data written together to same file )

     *            <li>function index:<ol start=0>
     *            <li>v1 - v0;  // simple difference
     *            <li>1/v1 - 1/v0; // difference of reciprocals  
     *            <li>100 * (v1 - v0) / v0;  // % change 
     *            <li>100.*(1/v1 - 1/v0) / (1/v0);  // % change of reciprocals	
     *            </ol>

     *            <li>new attribute name
     *            <li>new attribute units

     *            <li>InterpolatorType linear or natural_neighbor (nn)
     *            </ol>
     * @throws Exception
     */
    public void function(String[] args) throws Exception
    {
	int nmin = 15;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nFound %d arguments but %d required:%n"
			    + "  1  --  function%n"

							+ "  2  --  input model0 file name%n"
							+ "  3  --  relative path to grid0 directory (not used if grid stored in model file)%n"
							+ "  4  --  attribute index 0%n"

							+ "  5  --  input model1 file name%n"
							+ "  6  --  relative path to grid1 directory (not used if grid stored in model file)%n"
							+ "  7  --  attribute index 1%n"

							+ "  8  --  name of model containing the geometry for the new model (grid and radii); %n"
							+ "         often one of the two input models%n"
							+ "  9  --  relative path to geometry grid directory (not used if grid stored in model file)%n"

							+ " 10  --  output file name%n"

							+ " 11  --  reference to grid file ( \"null\" is a good choice )%n"

							+ " 12  --  function index.  Specify one of the following integers:%n"
							+ "           0: x1 - x0;                       // simple difference%n"
							+ "           1: 1/x1 - 1/x0;                   // difference of reciprocals%n"  
							+ "           2: 100 * (x1 - x0) / x0;          // %% change%n" 
							+ "           3: 100 * (1/x1 - 1/x0) / (1/x0);  // %% change of reciprocals%n"
							+ "           4: 100. * v0 / v1;                // simple percentage%n"

							+ " 13  --  new attribute name (no spaces)%n"
							+ " 14  --  new attribute units (no spaces)%n"

							+ " 15  --  InterpolatorType linear or natural_neighbor (nn)%n"
							, args.length, nmin));
	    System.exit(0);
	}

	int arg = 1;
	File model0FileName = new File(args[arg++]);
	String grid0Directory = args[arg++];
	int attribute0 = Integer.parseInt(args[arg++]);

	File model1FileName = new File(args[arg++]);
	String grid1Directory = args[arg++];
	int attribute1 = Integer.parseInt(args[arg++]);

	File geometryFileName = new File(args[arg++]);
	String geometryGridDirectory = args[arg++];

	String outputFile = args[arg++];
	String outputGridRef = args[arg++];
	if (outputGridRef.equals(".") || outputGridRef.equalsIgnoreCase("null"))
	    outputGridRef = "*";

	int function = Integer.parseInt(args[arg++]);

	String newAttributeName = args[arg++];
	String newAttributeUnits = args[arg++];

	String s = args[arg++].toUpperCase();
	if (s.equals("NN"))
	    s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	InterpolatorType radialType = horizontalType == InterpolatorType.LINEAR 
		? InterpolatorType.LINEAR : InterpolatorType.CUBIC_SPLINE;

	GeoTessModel model0 = GeoTessModel.getGeoTessModel(model0FileName, grid0Directory);
	GeoTessModel model1 = GeoTessModel.getGeoTessModel(model1FileName, grid1Directory);

	System.out.printf("Applying function %d to attributes %s and %s%n%n", function, 
		model0.getMetaData().getAttributeName(attribute0),
		model1.getMetaData().getAttributeName(attribute1));

	GeoTessModel geometryModel = null;

	if (geometryFileName.equals(model0FileName))
	    geometryModel = model0;
	else if (geometryFileName.equals(model1FileName))
	    geometryModel = model1;
	else
	    geometryModel = GeoTessModel.getGeoTessModel(geometryFileName,  geometryGridDirectory);

	GeoTessModel newModel = GeoTessModelUtils.function(function,
		model0, attribute0, model1, attribute1, geometryModel, 
		newAttributeName, newAttributeUnits, horizontalType, radialType);

	newModel.writeModel(outputFile, outputGridRef);

	System.out.println(newModel);

    }

    /**
     * Output to vtk file attribute values on a solid 3D globe.
     * 
     * @param args
     *            <ol>
     *            <li>vtkSolid
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>output file name with vtk extension
     *            <li>max radial spacing in km (50 km usually adequate)
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes
     *            </ol>
     * @throws Exception
     */
    public void vtkSolid(String[] args) throws Exception
    {
	int nmin = 11;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkSolid%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  output file name%n"
			    + "  5  --  max radial spacing in km (50 km usually adequate)%n"
			    + "  6  --  index of first layer%n"
			    + "  7  --  index of last layer%n"
			    + "  8  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + "  9  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + " 10  --  reciprocal [true | false], report 1./value%n"
			    + " 11  --  list of attribute indexes%n", nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	String outputFile = args[arg++];

	if (modelFileName.getAbsolutePath().equals(new File(outputFile).getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	double maxSpacing = Double.parseDouble(args[arg++]);

	int firstLayer = Integer.parseInt(args[arg++]);
	int lastLayer = Integer.parseInt(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	//new File(outputFile).getParentFile().mkdirs();

	if (lastLayer >= model.getNLayers())
	    lastLayer = model.getNLayers()-1;

	GeoTessModelUtils.vtkSolid(model, outputFile, maxSpacing,
		firstLayer, lastLayer, horizontalType, radialType, reciprocal, attributes);
	System.out.println("VTK output written to " + outputFile);
    }

    /**
     * Output to vtk file attribute values on a great circle slice through the model.
     * 
     * @param args
     *            <ol>
     *            <li>vtkSlice
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>output file name with vtk extension
     *            <li>latitude of first point, degrees
     *            <li>longitude of first point, degrees
     *            <li>latitude of last point, degrees
     *            <li>longitude of last point, degrees
     *            <li>shortest path (true or false)
     *            <li>nPoints
     *            <li>max radial spacing in km (50 km usually adequate)
     *            <li>index of first layer (deepest)
     *            <li>index of last layer (shallowest)
     *            <li>interpolation type, either 'linear', or 'natutal_neighbor'
     *            <li>reciprocal [true | false]. If true, reported values are
     *            1./(model value). Useful when model stores slowness, but
     *            velocity is desired.
     *            <li>list of attribute indexes
     *            </ol>
     * @throws Exception
     */
    public void vtkSlice(String[] args) throws Exception
    {
	int nmin = 17;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkSlice%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4 --   output file name with vtk extension%n"
			    + "  5  --  first latitude, degrees%n"
			    + "  6  --  first longitude, degrees%n"
			    + "  7  --  last latitude, degrees%n"
			    + "  8  --  last longitude, degrees%n"
			    + "  9  --  shortest path (true or false)%n"
			    + " 10  --  npoints%n"
			    + " 11  --  max radial spacing in km (50 km usually adequate)%n"
			    + " 12  --  index of first layer (deepest)%n"
			    + " 13  --  index of last layer (shallowest)%n"
			    + " 14  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + " 15  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + " 16  --  reciprocal [true | false], report 1./value%n"
			    + " 17  --  list of attribute indexes%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	String outputFileName = args[arg++];

	if (modelFileName.getAbsolutePath().equals(new File(outputFileName).getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	double lat1 = Double.parseDouble(args[arg++]);
	double lon1 = Double.parseDouble(args[arg++]);
	double lat2 = Double.parseDouble(args[arg++]);
	double lon2 = Double.parseDouble(args[arg++]);

	boolean shortestPath = args[arg++].equalsIgnoreCase("true");

	int nx = Integer.parseInt(args[arg++]);
	double rspacing = Double.parseDouble(args[arg++]);
	int firstLayer = Integer.parseInt(args[arg++]);
	int lastLayer = Integer.parseInt(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	// boolean convertToDepth = args[arg++].startsWith("d");
	// String spatialCoordinates = args[arg++];

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	lastLayer = Math.min(lastLayer, model.getMetaData().getNLayers() - 1);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);

	GreatCircle greatCircle = new GreatCircle(model.getEarthShape().getVectorDegrees(lat1, lon1),
		model.getEarthShape().getVectorDegrees(lat2, lon2), shortestPath);

	System.out.println("X direction (lat, lon) = "
		+model.getEarthShape().getLatLonString(greatCircle.getTransform()[0]));
	System.out.println("Y direction (lat, lon) = "
		+model.getEarthShape().getLatLonString(greatCircle.getTransform()[1]));
	System.out.println("Z direction (lat, lon) = "
		+model.getEarthShape().getLatLonString(greatCircle.getTransform()[2]));

	GeoTessModelUtils.vtkSlice(model, outputFileName,
		greatCircle, nx, rspacing,
		firstLayer, lastLayer, horizontalType, radialType, reciprocal, attributes);

	//System.out.println("VTK output written to " + outputFileName);
    }

    /**
     * Output to vtk file attribute values in a 3D block of points.
     * 
     * @param args
     *            <ol>
     *            <li>vtk3DBlock
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>first latitude, degrees
     *            <li>last latitude, degrees
     *            <li>either dlat (include decimal point) or nlat (no decimal point)
     *            <li>first longitude, degrees
     *            <li>last longitude, degrees"
     *            <li>either dlon (include decimal point) or nlon (no decimal point)
     *            <li>index of first layer id
     *            <li>index of last layer id
     *            <li>radialDimension: depth or radius.
     *            <li>max radial spacing of points"
     *            <li>interpolation type: linear or natural_neighbor (nn)
     *            <li>reciprocal [true | false], report 1./value
     *            <li>list of attribute indexes
     *            <li>output file
     *            </ol>
     * @throws Exception
     */
    public void vtk3DBlock(String[] args) throws Exception
    {
	int nmin = 18;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtk3DBlock%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  output file%n"
			    + "  5  --  first latitude, degrees%n"
			    + "  6  --  last latitude, degrees%n"
			    + "  7  --  either the number of latitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the latitude values (if the value can be interpreted as a double)%n"
			    + "  8  --  first longitude, degrees%n"
			    + "  9  --  last longitude, degrees%n"
			    + " 10  --  either the number of longitude values (if the value can be interpreted as an integer), %n"
			    + "         or the spacing of the longitude values (if the value can be interpreted as a double)%n"
			    + " 11  --  index of first layer id%n"
			    + " 12  --  index of last layer id%n"
			    + " 13  --  radialDimension: depth or radius.%n"
			    + " 14  --  max radial spacing of points%n"
			    + " 15  --  interpolation type horizontal: linear or natural_neighbor (nn)%n"
			    + " 16  --  interpolation type radial: linear or cubic_spline (cs)%n"
			    + " 17  --  reciprocal [true | false], report 1./value%n"
			    + " 18  --  list of attribute indexes%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];

	String outputFile = args[arg++];

	if (modelFileName.getAbsolutePath().equals(new File(outputFile).getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	double[] latitudes = GeoTessModelUtils.getLatitudes(args[arg++],
		args[arg++], args[arg++]);
	double[] longitudes = GeoTessModelUtils.getLongitudes(args[arg++],
		args[arg++], args[arg++], "true");

	int firstLayer = Integer.parseInt(args[arg++]);
	int lastLayer = Integer.parseInt(args[arg++]);

	if (firstLayer < 0)
	    throw new Exception("index of firstLayer cannot be "+firstLayer);

	String radialDimension = args[arg++];

	double maxRadialSpacing = Double.parseDouble(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("NN")) s = "NATURAL_NEIGHBOR";
	InterpolatorType horizontalType = InterpolatorType.valueOf(s);
	s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	int[] attributes = parseList(args[arg++], model.getMetaData()
		.getNAttributes() - 1);


	if (lastLayer >= model.getNLayers())
	    lastLayer = model.getNLayers()-1;

	GeoTessModelUtils.vtk3DBlock(model, outputFile, latitudes, longitudes, 
		firstLayer, lastLayer, radialDimension, maxRadialSpacing, 
		horizontalType, radialType, reciprocal, attributes);

	System.out.println("VTK output written to "+outputFile);
    }

    /**
     * Generate a contour map of some data values on a Robinson projection of the Earth.
     * <p>Also generates another file that contains the outlines of the continents 
     * plotted on the same map projection.  The file is located in the same directory
     * as the outputFile, with the name 'map_coastlines_centerLon_%d.vtk' where %d is 
     * replaced with the longitude of the center of the map rounded to the nearest degree.
     * 
     * @param args
     *            <ol>
     *            <li>vtkRobinson
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>output file name
     *            <li>longitude of center of map in degrees
     *            <li>depth in the Earth where the data is to be interpolated.
     *            <li>layer the index of the layer in which depth resides.
     *            <li>radiusOutOfRangeAllowed if true and depth is above the top of layer or
     *                below bottom of layer then the values at the top or bottom of layer are plotted.
     *                If false and depth is above the top of layer or below bottom of layer then NaN
     *                is plotted.
     *            <li>interpolation type (radial): linear or cubic_spline (cs)
     *            <li>reciprocal [true | false], report 1./value
     *            <li>list of attribute indexes
     *            </ol>
     * @throws Exception
     */
    public void vtkRobinson(String[] args) throws Exception
    {
	int nmin = 11;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkRobinson%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  output file%n"
			    + "  5  --  longitude of center of map in degrees%n"
			    + "  6  --  depth in the Earth where the data is to be interpolated.%n"
			    + "  7  --  layer the index of the layer in which depth resides.%n"
			    + "  8  --  radiusOutOfRangeAllowed if true and depth is above the top of layer or%n"
			    + "         below bottom of layer then the values at the top or bottom of layer are plotted.%n"
			    + "         If false and depth is above the top of layer or below bottom of layer then NaN%n"
			    + "         is plotted.%n"
			    + "  9  --  interpolation type (radial): linear or cubic_spline (cs)%n"
			    + " 10  --  reciprocal [true | false], report 1./value%n"
			    + " 11  --  list of attribute indexes%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	File outputFile = new File(args[arg++]);

	if (modelFileName.getAbsolutePath().equals(outputFile.getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	double centerLonDegrees = Double.parseDouble(args[arg++]);

	double depth = Double.parseDouble(args[arg++]);

	int layer = Integer.parseInt(args[arg++]);

	boolean radiusOutOfRangeAllowed = Boolean.parseBoolean(args[arg++]);

	String s = args[arg++].toUpperCase();
	if (s.equals("CS")) s = "CUBIC_SPLINE";
	InterpolatorType radialType = InterpolatorType.valueOf(s);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	int[] attributes = parseList(args[arg++], model.getMetaData().getNAttributes() - 1);

	if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists())
	{
	    System.out.printf("Creating directory %s%n", outputFile.getParentFile().getCanonicalPath());
	    outputFile.getParentFile().mkdirs();
	}

	GeoTessModelUtils.vtkRobinson(model, outputFile, centerLonDegrees, 
		depth, layer, radiusOutOfRangeAllowed, radialType, reciprocal, attributes);

	System.out.println("VTK output written to "+(outputFile.getCanonicalPath()));
	System.out.println("Coastlines written to file "
		+GeoTessModelUtils.mostRecentCoastLinesFile.getCanonicalPath());
    }

    /**
     * Generate contour maps of some data values on a Robinson projection of the Earth.
     * A separate map is generated for each of a specified list of layer indexes.
     * Values at the tops of the layers are generated.
     * <p>Also generates another file that contains the outlines of the continents 
     * plotted on the same map projection.  The file is located in the same directory
     * as the outputFile, with the name 'map_coastlines_centerLon_%d.vtk' where %d is 
     * replaced with the longitude of the center of the map rounded to the nearest degree.
     * 
     * @param args
     *            <ol>
     *            <li>vtkRobinsonLayers
     *            <li>input model file name
     *            <li>relative path to grid directory (not used if grid stored in model file)
     *            <li>output file, '%d' replaced with layer index
     *            <li>longitude of center of map in degrees
     *            <li>list of layer indexes
     *            <li>reciprocal [true | false], report 1./value
     *            <li>list of attribute indexes
     *            </ol>
     * @throws Exception
     */
    public void vtkRobinsonLayers(String[] args) throws Exception
    {
	int nmin = 8;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkRobinsonLayers%n"
			    + "  2  --  input model file name%n"
			    + "  3  --  relative path to grid directory (not used if grid stored in model file)%n"
			    + "  4  --  output file, '%%d' replaced with layer index%n"
			    + "  5  --  longitude of center of map in degrees%n"
			    + "  6  --  list of layer indexes.%n"
			    + "  7  --  reciprocal [true | false], report 1./value%n"
			    + "  8  --  list of attribute indexes%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File modelFileName = new File(args[arg++]);
	String gridDirectory = args[arg++];
	String outputFile = args[arg++];

	if (modelFileName.getAbsolutePath().equals(new File(outputFile).getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	double centerLonDegrees = Double.parseDouble(args[arg++]);

	GeoTessModel model = GeoTessModel.getGeoTessModel(modelFileName, gridDirectory);

	int[] layers = parseList(args[arg++], model.getNLayers() - 1);

	boolean reciprocal = Boolean.parseBoolean(args[arg++]);

	int[] attributes = parseList(args[arg++], model.getMetaData().getNAttributes() - 1);

	boolean radiusOutOfRangeAllowed = true;

	for (int layer=0; layer < layers.length; ++layer)
	{
	    File f = new File(String.format(outputFile, layers[layer]));
	    GeoTessModelUtils.vtkRobinson(model, f, centerLonDegrees, 
		    -1000, layers[layer], radiusOutOfRangeAllowed, InterpolatorType.LINEAR, reciprocal, attributes);

	    System.out.println("VTK output written to "+(f.getCanonicalPath()));
	}

	System.out.println("Coastlines written to file "
		+GeoTessModelUtils.mostRecentCoastLinesFile.getCanonicalPath());

	System.out.println("Done.");
    }

    /**
     * Generate a contour map of some data values on a Robinson projection of the Earth.
     * <p>Also generates another file that contains the outlines of the continents 
     * plotted on the same map projection.  The file is located in the same directory
     * as the outputFile, with the name 'map_coastlines_centerLon_%d.vtk' where %d is 
     * replaced with the longitude of the center of the map rounded to the nearest degree.
     * 
     * @param args
     *            <ol>
     *            <li>vtkRobinsonTriangleSize
     *            <li>input model or grid file name
     *            <li>relative path to grid directory, otherwise ignored
     *            <li>output file
     *            <li>layerIndex if 1 is a model, tessId if 1 is a grid
     *            <li>longitude of center of map in degrees
     *            </ol>
     * @throws Exception
     */
    public void vtkRobinsonTriangleSize(String[] args) throws Exception
    {
	int nmin = 6;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkRobinsonTriangleSize%n"
			    + "  2  --  input model or grid file name%n"
			    + "  3  --  relative path to grid directory, otherwise ignored%n"
			    + "  4  --  output file%n"
			    + "  5  --  layerIndex if 2 is a model, tessId if 2 is a grid%n"
			    + "  6  --  longitude of center of map in degrees%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;

	File inputFile = new File(args[arg++]);
	String gridDirectory = args[arg++];
	File outputFile = new File(args[arg++]);
	int tessId=-1;

	if (inputFile.getAbsolutePath().equals(outputFile.getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	GeoTessGrid grid = null;
	if (GeoTessModel.isGeoTessModel(inputFile))
	{
	    GeoTessModel model = GeoTessModel.getGeoTessModel(inputFile, gridDirectory);
	    tessId = model.getMetaData().getTessellation(Integer.parseInt(args[arg++]));
	    grid = model.getGridRotated();
	}
	else
	{
	    grid = new GeoTessGrid().loadGrid(inputFile);
	    tessId = Integer.parseInt(args[arg++]);
	}

	double centerLonDegrees = Double.parseDouble(args[arg++]);

	if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists())
	{
	    System.out.printf("Creating directory %s%n", outputFile.getParentFile().getCanonicalPath());
	    outputFile.getParentFile().mkdirs();
	}

	GeoTessModelUtils.vtkRobinsonTriangleSize(grid, outputFile, centerLonDegrees, tessId);
	System.out.println("VTK output written to "+(outputFile.getCanonicalPath()));
	System.out.println("Coastlines written to file "
		+GeoTessModelUtils.mostRecentCoastLinesFile.getCanonicalPath());
    }

    /**
     * Given a file containing lat-lon points, project the points to a Robinson projection
     * and output the results to a vtk plot file.
     * 
     * @param args
     *            <ol>
     *            <li>vtkRobinsonPoints
     *            <li>input file containing lat-lon pairs
     *            <li>output file
     *            <li>longitude of center of map in degrees
     *            </ol>
     * @throws Exception
     */
    public void vtkRobinsonPoints(String[] args) throws Exception
    {
	int nmin = 4;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkRobinsonPoints%n"
			    + "  2  --  intput file containing lat-lon pairs%n"
			    + "  3  --  output file%n"
			    + "  4  --  longitude of center of map in degrees%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File inputFile = new File(args[arg++]);
	File outputFile = new File(args[arg++]);

	if (inputFile.getAbsolutePath().equals(outputFile.getAbsolutePath()))
	    throw new Exception("input model file name is equal to output file name");

	double centerLonDegrees = Double.parseDouble(args[arg++]);

	ArrayList<double[]> points = new ArrayList<double[]>(1000);

	Scanner input = new Scanner(inputFile);
	Scanner line;
	while (input.hasNext())
	{
	    line = new Scanner(input.nextLine());
	    try
	    {
		points.add(EarthShape.WGS84.getVectorDegrees(line.nextDouble(), line.nextDouble()));
	    }
	    catch(Exception e)
	    {

	    }
	}
	input.close();

	System.out.println("Loaded "+points.size()+" points from file "+inputFile.getCanonicalPath());

	GeoTessModelUtils.vtkRobinsonPoints(centerLonDegrees, points, outputFile);

	System.out.println("VTK output written to "+outputFile.getCanonicalPath());
    }

    /**
     * Given a file containing lat-lon points, project the points to a sphere
     * and output the results to a vtk plot file.
     * 
     * @param args
     *            <ol>
     *            <li>vtkPoints
     *            <li>intput file containing lat-lon pairs
     *            <li>output file
     *            <li>longitude of center of map in degrees
     *            </ol>
     * @throws Exception
     */
    public void vtkPoints(String[] args) throws Exception
    {
	int nmin = 3;
	if (args.length != nmin)
	{
	    System.out .println(
		    String.format("%n%nMust supply %d arguments:%n"
			    + "  1  --  vtkPoints%n"
			    + "  2  --  intput file containing lat-lon pairs%n"
			    + "  3  --  output file%n"
			    , nmin));
	    System.exit(0);
	}

	int arg = 1;
	File inputFile = new File(args[arg++]);
	File outputFile = new File(args[arg++]);

	if (inputFile.getAbsolutePath().equals(outputFile.getAbsolutePath()))
	    throw new Exception("input file name is equal to output file name");

	ArrayList<double[]> points = new ArrayList<double[]>(1000);

	Scanner input = new Scanner(inputFile);
	Scanner line;
	while (input.hasNext())
	{
	    line = new Scanner(input.nextLine().replaceAll(",", " "));
	    try
	    {
		points.add(EarthShape.WGS84.getVectorDegrees(line.nextDouble(), line.nextDouble()));
	    }
	    catch(Exception e)
	    {

	    }
	}
	input.close();

	System.out.println("Loaded "+points.size()+" points from file "+inputFile.getCanonicalPath());

	GeoTessModelUtils.vtkPoints(points, outputFile);

	System.out.println("VTK output written to "+outputFile.getCanonicalPath());
    }

    /**
     * Parse a list like "0,2,4-7" into an int[]. The substring 'n' is
     * interpreted to be the value of specified 'maxValue'. If the list is equal
     * to 'all', then values from 0 to maxValue are returned.
     * If list is equal to none, empty array is returned.
     * 
     * @param stringList
     * @param maxValue
     * @return parsed list.
     * @throws Exception
     */
    public int[] parseList(String stringList, int maxValue)
	    throws Exception
    {
	ArrayListInt list = new ArrayListInt();
	if (stringList.toLowerCase().equals("all"))
	{
	    for (int i = 0; i <= maxValue; ++i)
		list.add(i);
	}
	else if (stringList.toLowerCase().equals("none"))
	    return new int[0];
	else
	{
	    stringList = stringList.replace("n", Integer.toString(maxValue));
	    for (String subList : stringList.replaceAll(" ", "").split(","))
	    {
		String[] s = subList.split("-");
		if (s.length == 1) 
		{
		    if (Integer.parseInt(s[0]) <= maxValue)
			list.add(Integer.parseInt(s[0]));
		    else
			list.add(maxValue);
		}
		else if (s.length == 2)
		{
		    for (int i = Integer.parseInt(s[0]); i <= Integer
			    .parseInt(s[1]); ++i)
			if (i <= maxValue)
			    list.add(i); 
		}
		else
		    throw new Exception(String.format(
			    "%nCannot interpret substring %s%n%n", subList));
	    }
	}
	// System.out.println("GeoTessExplorer.parseList = "+Arrays.toString(list.toArray()));
	return list.toArray();
    }

    /**
     * Output to standard out an array of equally spaced latitude values. User
     * specifies first latitude, last latitude and either step size or number of
     * values. If the last parameter does not contain a '.' then it is
     * interpreted as an integer number of values. If it contains a '.' then it
     * is interpreted as the step size (a floating point value). If the step
     * size is specified then the actual step size of generated latitude values
     * may be somewhat smaller than the requested value so that an integral
     * number of equally spaced values will be returned.
     * 
     * @param args
     *            <ol>
     *            <li>getLatitudes
     *            <li>first latitude, arbitrary units
     *            <li>last latitude, arbitrary units
     *            <li>either the number of latitude values (if can be parsed as an integer), 
     *            or the spacing of latitude values (if can be parsed as a double).
     *            </ol>
     * @throws Exception
     */
    public void getLatitudes(String[] args) throws Exception
    {
	if (args.length < 4)
	{
	    System.out .println(
		    String.format("%n%nMust specify first latitude, last latitude and a third value%n"
			    +           "that is either the number of latitude values (if the value can be%n"
			    + "interpreted as an integer), or the spacing of the latitude values%n"
			    + "(if the value can be interpreted as a double)%n"));
	    System.exit(0);
	}
	for (double lat : GeoTessModelUtils.getLatitudes(args[1], args[2],
		args[3]))
	    System.out.printf(" %1.7g%n", lat);
    }

    /**
     * Output to standard out an array of equally spaced longitude values. User
     * specifies first longitude, last longitude and either step size or number
     * of values. If the last parameter does not contain a '.' then it is
     * interpreted as an integer number of values. If it contains a '.' then it
     * is interpreted as the step size (a floating point value). If the step
     * size is specified then the actual step size of generated longitude values
     * may be somewhat smaller than the requested value so that an integral
     * number of equally spaced values will be returned.
     * <p>
     * If the first longitude is greater than the last longitude, (170 and -170,
     * for example), then 360 is added to the last longitude.
     * 
     * @param args
     *            <ol>
     *            <li>getLongitudes 
     *            <li>first longitude, degrees 
     *            <li>last longitude, degrees 
     *            <li>either the number of longitude values (if can be parsed as an integer), 
     *            or the spacing of longitude values in degrees (if can be parsed as a double).
     *            </ol>
     * @throws Exception
     */
    public void getLongitudes(String[] args) throws Exception
    {
	if (args.length < 4)
	{
	    System.out .println(
		    String.format("%n%nMust specify first longitude, last longitude and a third value%n"
			    + "that is either the number of longitude values (if the value can be%n"
			    + "interpreted as an integer), or the spacing of the longitude values%n"
			    + "(if the value can be interpreted as a double)%n"));
	    System.exit(0);
	}
	for (double lon : GeoTessModelUtils.getLongitudes(args[1], args[2],
		args[3], "true"))
	    System.out.printf(" %1.7g%n", lon);
    }

    /**
     * Output to standard out an array of equally spaced distance values, in
     * degrees. User specifies the lat, lon positions of two points, and the
     * number number of points, which are assumed to be equally spaced between
     * the first and last point, inclusive. This routine otuputs the distance
     * between the first point and each subsequent point. First value will be
     * zero.
     * 
     * @param args
     *            <ol>
     *            <li>getDistanceDegrees
     *            <li>lat1, degrees
     *            <li>lon1, degrees
     *            <li>lat2, degrees
     *            <li>lon2, degrees
     *            <li>nPoints
     *            </ol>
     * @throws Exception
     */
    public void getDistanceDegrees(String[] args) throws Exception
    {
	if (args.length < 6)
	{
	    System.out .println(
		    String.format("%n%nMust specify lat1 lon1 lat2 lon2 and nPoints%n"));
	    System.exit(0);
	}
	int arg = 1;
	double lat1 = Double.parseDouble(args[arg++]);
	double lon1 = Double.parseDouble(args[arg++]);
	double lat2 = Double.parseDouble(args[arg++]);
	double lon2 = Double.parseDouble(args[arg++]);
	int nx = Integer.parseInt(args[arg++]);

	double dx = VectorUnit.angleDegrees(
		EarthShape.WGS84.getVectorDegrees(lat1, lon1),
		EarthShape.WGS84.getVectorDegrees(lat2, lon2))
		/ (nx - 1);

	for (int i = 0; i < nx; ++i)
	    System.out.printf(" %1.7g%n", i * dx);

    }

    protected String parseFunctionList()
    {
	StringBuffer buf = new StringBuffer();
	for (Map.Entry<String, String> f : functionMap.entrySet())
	{
	    if (f.getValue().length() == 0)
		buf.append(String.format("%n%s%n", f.getKey()));
	    else
		buf.append(String.format("%-23s -- %s%n", f.getKey(), f.getValue()));
	}
	return buf.toString();
    }

    static String getFileSize(File f)
    {
	double kb = 1024.;
	double mb = kb * 1024.;
	double gb = mb * 1024.;
	double tb = gb * 1024.;
	if (f.length() >= tb)
	    return String.format("%1.3f TB", f.length()/tb);
	if (f.length() >= gb)
	    return String.format("%1.3f GB", f.length()/gb);
	if (f.length() >= mb)
	    return String.format("%1.3f MB", f.length()/mb);
	if (f.length() >= kb)
	    return String.format("%1.3f KB", f.length()/kb);
	return String.format("%d bytes", f.length());
    }

}
