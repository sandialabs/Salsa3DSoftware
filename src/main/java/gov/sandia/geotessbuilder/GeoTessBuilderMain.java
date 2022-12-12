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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.ZipFile;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessJava;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessModelUtils;
import gov.sandia.geotess.GeoTessUtils;
import gov.sandia.geotess.PointMap;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.globals.DataType;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.platonicsolid.PlatonicSolid;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle.GreatCircleException;
import gov.sandia.gmp.util.numerical.polygon.Polygon;
import gov.sandia.gmp.util.numerical.polygon.Polygon2D;
import gov.sandia.gmp.util.numerical.polygon.PolygonSmallCircles;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

/**
 * Class provides a main() method to implement construction of a new GeoTessGrid or 
 * resolution refinement of an existing GeoTessModel based on a specified Properties file.
 * @author sballar
 *
 */
public class GeoTessBuilderMain {

    /**
     * @param args
     */
    public static void main(String[] args) {
	try {
	    if (args.length == 0) {
		System.out.printf(
			"%nGeoTessBuilder %s. ERROR: Must specify property file as command line argument.%n%n",
			GeoTessJava.getVersion());
		System.exit(1);
	    }

	    if (!args[0].endsWith(".properties"))
		args[0] += ".properties";

	    if (!(new File(args[0]).exists())) {
		System.out.printf("%nGeoTessBuilder %s. ERROR: Property file %s does not exist.%n%n", GeoTessJava.getVersion(),
			args[0]);
		System.exit(1);
	    }

	    GeoTessBuilderMain.run(new PropertiesPlus(new File(args[0])));

	    System.out.println("Done.");
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * 
     * @param properties
     * @return either a GeoTessModel or GeoTessGrid, depending on whether
     *         gridConstructionMode is 'model refinement' or 'scratch'.
     * @throws PropertiesPlusException
     * @throws GeoTessException
     * @throws IOException
     * @throws GreatCircleException
     */
    static public Object run(PropertiesPlus properties) throws Exception {
	return run(properties, null);
    }

    /**
     * 
     * @param properties
     * @param modelToRefine can be null
     * @return either a GeoTessModel or GeoTessGrid, depending on whether
     *         gridConstructionMode is 'model refinement' or 'scratch'.
     * @throws PropertiesPlusException
     * @throws GeoTessException
     * @throws IOException
     * @throws GreatCircleException
     */
    static public Object run(PropertiesPlus properties, GeoTessModel modelToRefine) throws Exception {
	int verbosity = properties.getInt("verbosity", 1);
	if (verbosity > 0) {
	    System.out.println("GeoTessBuilder " + GeoTessJava.getVersion());
	}

	String gridConstructionMode = properties.getProperty("gridConstructionMode");
	if (gridConstructionMode == null)
	    throw new GeoTessException(
		    String.format("%n%nProperty gridConstructionMode is not specified in the properties file.%n"
			    + "Execting one of the following values:%n" + "gridConstructionMode = model refinement%n"
			    + "gridConstructionMode = scratch%n"));

	if (gridConstructionMode.toLowerCase().contains("model")
		&& gridConstructionMode.toLowerCase().contains("refine")) {
	    if (modelToRefine == null) {
		String model = properties.getProperty("modelToRefine");
		if (model == null)
		    throw new GeoTessException("\nProperty modelToRefine is not specififed.\n"
			    + "modelToRefine is required when running in 'model refinement' mode.\n");

		modelToRefine = new GeoTessModel(model);
	    }

	    ArrayListInt pointsToRefine = null;
	    if (properties.containsKey("pointsToRefine"))
		pointsToRefine = new ArrayListInt(properties.getIntArray("pointsToRefine"));
	    else if (properties.containsKey("fileOfPointsToRefine")) {
		File f = properties.getFile("fileOfPointsToRefine");
		if (!f.exists())
		    throw new IOException(
			    "File " + properties.getProperty("fileOfPointsToRefine") + " does not exist.");
		Scanner input = new Scanner(f);
		pointsToRefine = new ArrayListInt(1000);
		while (input.hasNext())
		    pointsToRefine.add(input.nextInt());
		input.close();
	    } else if (properties.containsKey("polygonToRefine")) {
		pointsToRefine = new ArrayListInt(1000);
		for (Polygon polygon : Polygon.getPolygonList(properties.getFile("polygonToRefine")))
		    for (int point = 0; point < modelToRefine.getNPoints(); ++point)
			if (polygon.contains(modelToRefine.getPointMap().getPointUnitVector(point)))
			    pointsToRefine.add(point);
	    } else if (properties.containsKey("threshold")) {
		// expect property 'threshold' to be something like
		// attribute > 0.5
		// where first token is attributeName,
		// second is one of <=, <, !=, ==, =, >, >=
		// and third can be parsed to double or long.
		String[] parts = properties.getProperty("threshold").trim().replaceAll(",", " ").replaceAll("  ", " ")
			.split(" ");

		if (parts.length < 3)
		    throw new IOException(String.format("%nproperty 'threshold' is invalid.%n"
			    + "Must contain at least 3 substrings:%nattribute name, comparison operator, and threshold value"));

		String attribute = parts[0];
		for (int i = 1; i < parts.length - 2; ++i)
		    attribute += String.format(" %s", parts[i]);

		int attributeIndex = modelToRefine.getMetaData().getAttributeIndex(attribute);
		if (attributeIndex < 0) {
		    throw new IOException(
			    String.format("model does not contain attribute %s.%n" + "Valid attributes are %s%n",
				    attribute, modelToRefine.getMetaData().getAttributeNamesString()));
		}
		String comparison = parts[parts.length - 2];
		String threshold = parts[parts.length - 1];

		pointsToRefine = getThresholdPoints(modelToRefine, attributeIndex, comparison, threshold);
	    }

	    if (pointsToRefine == null)
		throw new IOException("\ninternal variable pointsToRefine is null.  \n"
			+ "One of the properties 'pointsToRefine', 'fileOfPointsToRefine' or "
			+ "'threshold' must be set, but none are set.");

	    int[] maxTriangleEdgeLevel;

	    if (properties.containsKey("minTriangleSize")) {
		double[] minTriangleSize = properties.getDoubleArray("minTriangleSize");

		if (minTriangleSize.length != modelToRefine.getGrid().getNTessellations())
		    throw new PropertiesPlusException(String.format(
			    "%nThe number of elements in minTriangleSize and number of tessellations in modelToRefine do not agree.%n"
				    + "There are %d tessellations in modelToRefine and %d elements in property minTriangleSize.",
				    modelToRefine.getGrid().getNTessellations(), minTriangleSize.length));

		maxTriangleEdgeLevel = new int[modelToRefine.getGrid().getNTessellations()];
		for (int tessid = 0; tessid < modelToRefine.getGrid().getNTessellations(); ++tessid) {
		    maxTriangleEdgeLevel[tessid] = GeoTessUtils.getTessLevel(minTriangleSize[tessid]);
		    if (maxTriangleEdgeLevel[tessid] < modelToRefine.getGrid().getNLevels(tessid) - 1)
			throw new PropertiesPlusException(String.format("%nminTriangleSize[%d] is %1.3f "
				+ "which is greater than the size of the triangles in tessellation[%d] of modelToRefine which is %1.3f%n",
				tessid, minTriangleSize[tessid], tessid,
				GeoTessUtils.getEdgeLength(modelToRefine.getGrid().getNLevels(tessid) - 1)));

		}
	    } else {
		maxTriangleEdgeLevel = new int[modelToRefine.getGrid().getNTessellations()];
		Arrays.fill(maxTriangleEdgeLevel, Integer.MAX_VALUE);
	    }

	    int minCorners = properties.getInt("minCorners", 1);

	    if (minCorners < 1 || minCorners > 3)
		throw new PropertiesPlusException(
			String.format("%nProperty minCorners = %d but must be between 1 and 3 inclusive.%n"
				+ "Property minCorners specifies the number of corners of a triangle that must satisfy the 'threshold' requirement%n"
				+ "in order for that triangle to be subdivided.", minCorners));

	    GeoTessModel newModel = RefineModel.refineModel(modelToRefine, pointsToRefine, maxTriangleEdgeLevel,
		    minCorners, properties.getInt("verbosity", 1), properties.getFile("vtkDir"));

	    // should already be a delaunay tessellation, but just to make sure...
	    newModel.getGrid().delaunay();

	    String outputFile = properties.getProperty("outputModelFile");

	    if (outputFile != null) {

		if (outputFile.contains("<gridID>"))
		    outputFile = outputFile.replace("<gridID>", newModel.getGridID());

		newModel.writeModel(outputFile);

		if (verbosity > 0)
		    System.out.println("Refined model written to output file " + outputFile);
	    }

	    plotFile(properties, newModel.getGrid());

	    return newModel;
	} 
	else 
	{
	    // build grid from scratch
	    InitialSolid initialSolid = new InitialSolid(
		    PlatonicSolid.valueOf(properties.getProperty("initialSolid", "ICOSAHEDRON").toUpperCase()));

	    if (properties.getProperty("rotateGrid") != null) {
		// user supplies a lat, lon position. Euler rotation will rotate the grid
		// such that grid vertex 0 is located at that position.
		double[] latlon = properties.getDoubleArray("rotateGrid");
		initialSolid.rotateAngles(latlon[0], latlon[1], true);
	    }
	    else if (properties.getProperty("eulerRotationAngles") != null) {
		double[] eulerRotationAngles = properties.getDoubleArray("eulerRotationAngles");
		if (eulerRotationAngles.length != 3)
		    throw new GeoTessException(
			    "If eulerRotationAngles are specified, then 3 angles (in degrees) must be specified.");
		eulerRotationAngles[0] = Math.toRadians(eulerRotationAngles[0]);
		eulerRotationAngles[1] = Math.toRadians(eulerRotationAngles[1]);
		eulerRotationAngles[2] = Math.toRadians(eulerRotationAngles[2]);
		initialSolid.rotateAngles(eulerRotationAngles);
	    } 

	    int maxProcessors = properties.getInt("maxProcessors", Runtime.getRuntime().availableProcessors());

	    int ntess = properties.getInt("nTessellations", 1);
	    if (ntess < 1)
		throw new GeoTessException("\nnTessellations must be > 0");

	    if (properties.getProperty("baseEdgeLengths") == null)
		throw new GeoTessException("\nbaseEdgeLengths must defined in the properties file");

	    int[] baseTessLevels = getTessLevels(properties.getDoubleArray("baseEdgeLengths"));

	    if (baseTessLevels.length != ntess)
		throw new GeoTessException(
			String.format("\nbaseEdgeLengths.length=%d is not equal to nTessellations=%d\n"));

	    ArrayList<Tessellation> tessellations = new ArrayList<Tessellation>(ntess);
	    for (int i = 0; i < ntess; ++i)
		tessellations.add(new Tessellation(initialSolid, baseTessLevels[i], maxProcessors));

	    if (properties.containsKey("polygons"))
	    {
		for (String s : properties.getProperty("polygons").split(";"))
		{
		    if (s.trim().length() > 0) 
		    {
			ArrayList<String> p = parse(s);
			if (p.size() == 0)
			    continue;

			if (p.get(0).equalsIgnoreCase("PolygonSmallCircles") 
				|| p.get(0).equalsIgnoreCase("small_circles")) 
			{
			    // expecting tokens:
			    // 1. center lat in degrees
			    // 2. center lon in degrees
			    // 3. in or out
			    // 4+ radii in degrees
			    // n-2 tessid
			    // n-1 grid resolution in degrees
			    double[] center = VectorGeo.getVectorDegrees(Double.parseDouble(p.get(1)),
				    Double.parseDouble(p.get(2)));

			    if (!p.get(3).equalsIgnoreCase("in") && !p.get(3).equalsIgnoreCase("out"))
				throw new Exception("Parameter 3 must be either 'in' or 'out' but found: "+p.get(3));

			    boolean referenceIn = p.get(3).equalsIgnoreCase("in");

			    double[] radii = new double[p.size()-6];
			    for (int i=4; i<p.size()-2; ++i)
				radii[i-4] = Math.toRadians(Double.parseDouble(p.get(i)));

			    int tessid = Integer.parseInt(p.get(p.size()-2));
			    int tessLevel = getTessLevel(p.get(p.size()-1));

			    if (tessid >= ntess)
				throw new GeoTessException(String.format(
					"%n%s%ntessellation index %d must be < nTessellations %d%n", s, tessid, ntess));

			    Polygon2D polygon = new PolygonSmallCircles(center, referenceIn, radii);
			    polygon.attachment = tessLevel;
			    tessellations.get(tessid).addPolygon(polygon);

			}
			else if (p.get(0).equalsIgnoreCase("spherical_cap")) 
			{
			    double[] center = VectorGeo.getVectorDegrees(Double.parseDouble(p.get(1)),
				    Double.parseDouble(p.get(2)));
			    double radius = Math.toRadians(Double.parseDouble(p.get(3)));

			    Integer tessid = Integer.parseInt(p.get(4));
			    Integer tessLevel = getTessLevel(p.get(5));

			    if (tessid >= ntess)
				throw new GeoTessException(String.format(
					"%n%s%ntessellation index %d must be < nTessellations %d%n", s, tessid, ntess));

			    Polygon2D polygon = new PolygonSmallCircles(center, true, radius);
			    polygon.attachment = tessLevel;
			    tessellations.get(tessid).addPolygon(polygon);
			} 
			else 
			{
			    if (p.size() != 3)
				throw new GeoTessException("\nError parsing property \npolygons = " + s
					+ "\nExpecting 3 substrings: fileName, tessIndex, levelIndex");

			    File file = new File(p.get(0));
			    Integer tessid = Integer.parseInt(p.get(1));
			    double edgeLengthDegrees = Double.parseDouble(p.get(2));
			    Integer tessLevel = GeoTessUtils.getTessLevel(edgeLengthDegrees);

			    if (tessid >= ntess)
				throw new GeoTessException(String.format(
					"%n%s%ntessellation index %d must be < nTessellations %d%n", s, tessid, ntess));

			    for (Polygon polygon : Polygon.getPolygonList(file)) {
				Polygon2D p2d = (Polygon2D)polygon;
				//p2d.densifyEdges(Math.toRadians(edgeLengthDegrees));
				p2d.attachment = tessLevel;
				tessellations.get(tessid).addPolygon(p2d);
			    }
			}
		    }
		}
	    }

	    if (properties.containsKey("paths"))
		for (String s : properties.getProperty("paths").split(";"))
		    if (s.trim().length() > 0) {
			String[] p = s.trim().split(",");
			if (p.length == 0)
			    continue;
			if (p.length != 3)
			    throw new GeoTessException("\nError parsing property \npaths = " + s
				    + "\nExpecting 3 comma-separated substrings: fileName, tessIndex, levelIndex");

			File file = new File(p[0].trim());
			Integer tessid = Integer.parseInt(p[1].trim());
			Integer tessLevel = getTessLevel(p[2].trim());

			if (tessid >= ntess)
			    throw new GeoTessException(String.format(
				    "%n%s%ntessellation index %d must be < nTessellations %d%n", s, tessid, ntess));

			ArrayList<double[]> points = readFile(file);
			expandPath(points, tessellations.get(tessid).getInitialSolid().getEdgeLength(tessLevel) * 0.1);
			tessellations.get(tessid).addPoints(points, tessLevel);
		    }

	    if (properties.containsKey("points"))
		for (String s : properties.getProperty("points").split(";"))
		    if (s.trim().length() > 0) {
			String[] p = s.trim().split(",");
			if (p.length == 0)
			    continue;
			if (p.length == 3) {
			    File file = new File(p[0].trim());
			    Integer tessid = Integer.parseInt(p[1].trim());
			    Integer tessLevel = getTessLevel(p[2].trim());

			    if (tessid >= ntess)
				throw new GeoTessException(String.format(
					"%n%s%ntessellation index %d must be < nTessellations %d%n", s, tessid, ntess));

			    ArrayList<double[]> points = readFile(file);
			    tessellations.get(tessid).addPoints(points, tessLevel);
			} else if (p.length == 5) {
			    double[] point;
			    p[0] = p[0].trim().toLowerCase();
			    if (p[0].startsWith("lat") && p[0].endsWith("lon"))
				point = VectorGeo.getVectorDegrees(Double.parseDouble(p[3]), Double.parseDouble(p[4]));
			    else if (p[0].startsWith("lon") && p[0].endsWith("lat"))
				point = VectorGeo.getVectorDegrees(Double.parseDouble(p[4]), Double.parseDouble(p[3]));
			    else
				throw new GeoTessException(
					p[0] + " is not recognized.  Must be either lat-lon or lon-lat");

			    Integer tessid = Integer.parseInt(p[1].trim());
			    Integer tessLevel = getTessLevel(p[2].trim());

			    if (tessid >= ntess)
				throw new GeoTessException(String.format(
					"%n%s%ntessellation index %d must be < nTessellations %d%n", s, tessid, ntess));

			    tessellations.get(tessid).addPoint(point, tessLevel);
			} else
			    throw new GeoTessException("\nCould not parse point definition: " + s);
		    }

	    long timer = System.currentTimeMillis();
	    for (Tessellation t : tessellations)
		t.build();

	    GridBuilder grid = new GridBuilder(tessellations);

	    // should already be a delaunay tessellation, but just to make sure...
	    grid.delaunay();

	    timer = System.currentTimeMillis() - timer;

	    if (verbosity > 0)
		System.out.println(Globals.elapsedTime(timer * 1e-3));

	    // this complicated looking line will get the name of the output file using
	    // a number of possible property names in order of priority.
	    String outputFile = properties.getProperty("outputGridFile");
	    if (outputFile == null)
		outputFile = properties.getProperty("outputModelFile");
	    if (outputFile == null)
		outputFile = properties.getProperty("outputFile", "");

	    if (outputFile.length() > 0) {
		outputFile = outputFile.replace("<gridID>", grid.getGridID());
		File f = new File(outputFile);
		if (f.getParentFile() != null)
		    f.getParentFile().mkdirs();
		grid.writeGrid(outputFile);
		if (verbosity > 0)
		    System.out.println("\nGeoTessGrid written to output file " + outputFile);
	    } else if (verbosity > 0)
		System.out.println("\nGrid not written to file because propery outputGridFile is undefined.");

	    plotFile(properties, grid);

	    if (verbosity > 0) {
		System.out.println();
		System.out.println(grid);
	    }

	    return grid;

	}
    }

    /**
     * Convenience method that returns a GeoTessGrid with approximately uniform
     * triangle edge lengths.
     * 
     * @param triangleEdgeLength in degrees. Should be a power or 2, i.e.: 
     * 64, 32, 16, 8, 4, 2, 1, 0.5, 0.25, etc.
     * @return GeoTessGrid
     * @throws Exception
     */
    public static GeoTessGrid getGrid(double triangleEdgeLength) throws Exception {
	PropertiesPlus gridProperties = new PropertiesPlus();
	gridProperties.setProperty("gridConstructionMode", "scratch");
	gridProperties.setProperty("nTessellations", "1");
	gridProperties.setProperty("baseEdgeLengths", triangleEdgeLength);
	gridProperties.setProperty("verbosity", 0);
	return (GeoTessGrid) GeoTessBuilderMain.run(gridProperties);
    }

    /**
     * Given a bunch of points, add enough additional points in between them to
     * ensure that the point spacing is smaller than maxSpacing (radians)
     * 
     * @param points
     * @param maxSpacing maximum spacing between points in radians.
     * @throws GreatCircleException
     */
    private static void expandPath(ArrayList<double[]> points, double maxSpacing) throws GreatCircleException {
	double tolerance = Math.cos(maxSpacing);
	ArrayList<double[]> p = new ArrayList<double[]>(points.size() * 2);
	GreatCircle gc;
	int n;
	double dx;
	p.add(points.get(0));

	for (int i = 0; i < points.size() - 1; ++i) {
	    if (VectorUnit.dot(points.get(i), points.get(i + 1)) < tolerance) {
		gc = new GreatCircle(points.get(i), points.get(i + 1));
		n = (int) Math.ceil(gc.getDistance() / maxSpacing);
		dx = gc.getDistance() / (n - 1);
		for (int j = 1; j < n; ++j)
		    p.add(gc.getPoint(j * dx));
	    } else
		p.add(points.get(i + 1));

	}
	points.clear();
	points.addAll(p);
    }

    /**
     * 
     * @param file
     * @throws GeoTessException
     * @throws FileNotFoundException
     */
    private static ArrayList<double[]> readFile(File file) throws IOException, GeoTessException {
	ArrayList<double[]> points = new ArrayList<double[]>();

	if (file.getName().toLowerCase().endsWith("kml")) {

	    Scanner input = new Scanner(file);
	    StringBuffer contents = new StringBuffer();
	    while (input.hasNext())
		contents.append(input.next().toLowerCase()).append(" ");
	    input.close();

	    int i1 = contents.indexOf("<coordinates>");
	    if (i1 < 0)
		throw new GeoTessException("String <coordinates> not found in kml file.");
	    int i2 = contents.indexOf("</coordinates>");
	    if (i2 < 0)
		throw new GeoTessException("String </coordinates> not found in kml file.");

	    input = new Scanner(contents.toString().substring(i1 + 13, i2).replaceAll(",", " "));

	    double lat, lon;
	    while (input.hasNextDouble()) {
		lon = input.nextDouble();
		lat = input.nextDouble();
		input.nextDouble();
		points.add(VectorGeo.getVectorDegrees(lat, lon));
	    }
	    input.close();
	} else if (file.getName().toLowerCase().endsWith("kmz")) {
	    ZipFile zip = new ZipFile(file);
	    Scanner input = new Scanner(zip.getInputStream(zip.entries().nextElement()));

	    StringBuffer contents = new StringBuffer();
	    while (input.hasNext())
		contents.append(input.next().toLowerCase()).append(" ");
	    input.close();
	    zip.close();

	    int i1 = contents.indexOf("<coordinates>");
	    if (i1 < 0)
		throw new GeoTessException("String <coordinates> not found in kmz file.");
	    int i2 = contents.indexOf("</coordinates>");
	    if (i2 < 0)
		throw new GeoTessException("String </coordinates> not found in kmz file.");

	    input = new Scanner(contents.toString().substring(i1 + 13, i2).replaceAll(",", " "));

	    double lat, lon;
	    while (input.hasNextDouble()) {
		lon = input.nextDouble();
		lat = input.nextDouble();
		input.nextDouble();
		points.add(VectorGeo.getVectorDegrees(lat, lon));
	    }
	    input.close();
	} else {
	    double[] u;
	    Scanner input = new Scanner(file);
	    ArrayList<String> records = new ArrayList<String>(1000);
	    boolean latFirst = true;
	    String line;
	    while (input.hasNext()) {
		line = input.nextLine().trim().toLowerCase();

		if (line.startsWith("lat"))
		    latFirst = true;
		else if (line.startsWith("lon"))
		    latFirst = false;
		else if (line.length() > 0 && !line.startsWith("#"))
		    records.add(line);
	    }
	    input.close();

	    double lat, lon;
	    for (String record : records) {
		input = new Scanner(record);
		try {
		    if (latFirst) {
			lat = input.nextDouble();
			lon = input.nextDouble();
		    } else {
			lat = input.nextDouble();
			lon = input.nextDouble();
		    }
		    u = VectorGeo.getVectorDegrees(lat, lon);
		    points.add(u);
		} catch (java.util.InputMismatchException ex) {
		    /* ignore errors */ }
		input.close();
	    }
	}
	return points;
    }

    private static ArrayList<String> parse(String s) {
	ArrayList<String> list = new ArrayList<String>();
	s = s.trim();
	while (s.startsWith("\"")) {
	    int i = s.indexOf("\"", 1);
	    list.add(s.substring(1, i));
	    s = s.substring(i + 1).trim();
	}
	for (String ss : s.split(","))
	    if (ss.trim().length() > 0)
		list.add(ss.trim());

	//		for (String ss : list)
	//			System.out.println(ss);
	//		System.out.println();

	return list;
    }

    /**
     * Convert edgeLength in degrees to tessellation level. Returns
     * round(log_2(64./edgeLength)
     * 
     * @param edgeLength
     * @return
     */
    public static int getTessLevel(String edgeLength) {
	return GeoTessUtils.getTessLevel(Double.parseDouble(edgeLength));
    }

    /**
     * Convert triangle edge length to tessellation level. Returns
     * round(log_2(64./edgeLength)
     * 
     * @param edgeLengths
     * @return tessellation levels
     */
    public static int[] getTessLevels(double[] edgeLengths) {
	int[] tessLevels = new int[edgeLengths.length];
	for (int i = 0; i < edgeLengths.length; ++i)
	    tessLevels[i] = GeoTessUtils.getTessLevel(edgeLengths[i]);
	return tessLevels;
    }

    private static ArrayListInt getThresholdPoints(GeoTessModel oldModel, int attribute, String comparison,
	    String thresholdString) throws IOException {
	ArrayListInt pointsToRefine = new ArrayListInt(oldModel.getNPoints() / 2);

	PointMap pm = oldModel.getPointMap();
	if (oldModel.getMetaData().getDataType() == DataType.DOUBLE
		|| oldModel.getMetaData().getDataType() == DataType.FLOAT) {
	    double threshold = Double.NaN;
	    try {
		threshold = Double.parseDouble(thresholdString);
	    } catch (Exception e) {
		throw new IOException(String.format(
			"%n%s%n%nProperty 'thresholds' is invalid.%n"
				+ "%s could not be parsed to a value of type double%n",
				e.getMessage(), thresholdString));

	    }

	    if (comparison.equals("<")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueDouble(i, attribute) < threshold)
			pointsToRefine.add(i);
	    } else if (comparison.equals("<=")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueDouble(i, attribute) <= threshold)
			pointsToRefine.add(i);
	    } else if (comparison.equals("==")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueDouble(i, attribute) == threshold)
			pointsToRefine.add(i);
	    } else if (comparison.equals("!=")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueDouble(i, attribute) != threshold)
			pointsToRefine.add(i);
	    } else if (comparison.equals(">=")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueDouble(i, attribute) >= threshold)
			pointsToRefine.add(i);
	    } else if (comparison.equals(">")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueDouble(i, attribute) > threshold)
			pointsToRefine.add(i);
	    } else
		throw new IOException(String.format("%nProperty 'thresholds' is invalid.%n"
			+ "%s is not a valid comparison operator.%n" + "Expected one of <=, <, !=, ==, =, >, >=%n",
			comparison));
	} else {
	    long threshold = 0;
	    try {
		threshold = Long.parseLong(thresholdString);
	    } catch (Exception e) {
		throw new IOException(String.format("%n%s%n%nProperty 'thresholds' is invalid.%n"
			+ "%s could not be parsed to a value of type long%n", e.getMessage(), thresholdString));
	    }

	    if (comparison.equals("<")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueLong(i, attribute) < threshold)
			pointsToRefine.add(i);
	    } else if (comparison.equals("<=")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueLong(i, attribute) <= threshold)
			pointsToRefine.add(i);
	    } else if (comparison.startsWith("=")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueLong(i, attribute) == threshold)
			pointsToRefine.add(i);
	    } else if (comparison.equals("!=")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueLong(i, attribute) != threshold)
			pointsToRefine.add(i);
	    } else if (comparison.equals(">=")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueLong(i, attribute) >= threshold)
			pointsToRefine.add(i);
	    } else if (comparison.equals(">")) {
		for (int i = 0; i < pm.size(); ++i)
		    if (pm.getPointValueLong(i, attribute) > threshold)
			pointsToRefine.add(i);
	    } else
		throw new IOException(String.format("%nProperty 'thresholds' is invalid.%n"
			+ "%s is not a valid comparison operator.%n" + "Expected one of <=, <, !=, ==, =, >, >=%n",
			comparison));
	}
	return pointsToRefine;
    }

    private static void plotFile(PropertiesPlus properties, GeoTessGrid grid) throws Exception {
	int tessId = properties.getInt("plotTess", -1);
	if (tessId < 0 && grid.getNTessellations() == 1)
	    tessId = 0;

	String output;
	if ((output = properties.getProperty("vtkFile")) != null) {

	    output = output.replace("<gridID>", grid.getGridID());
	    
	    File dir = new File(output).getParentFile();
	    if (dir != null)
		dir.mkdirs();

	    if (tessId < 0) {
		if (!output.contains("%"))
		    output = expandFileName(output, "_tess_%d");

		for (int i = 0; i < grid.getNTessellations(); ++i) {
		    File fout = new File(String.format(output, i));
		    GeoTessModelUtils.vtkTriangleSize(grid, fout, i);
		    if (properties.getInt("verbosity", 1) > 0)
			System.out.println(fout);
		}
	    } else
		GeoTessModelUtils.vtkTriangleSize(grid, new File(output), tessId);

	    File outputDirectory = new File(output).getParentFile();
	    if (outputDirectory == null)
		outputDirectory = new File(".");

	    if (!(new File(outputDirectory, "continent_boundaries.vtk")).exists())
		GeoTessModelUtils.copyContinentBoundaries(outputDirectory);
	}
	
	if ((output = properties.getProperty("vtkRobinsonFile")) != null) {
	    output = output.replace("<gridID>", grid.getGridID());
	    
	    File dir = new File(output).getParentFile();
	    if (dir != null)
		dir.mkdirs();

	    if (tessId < 0 && grid.getNTessellations() == 1)
		tessId = 0;
	    if (tessId < 0) {
		if (!output.contains("%"))
		    output = expandFileName(output, "_tess_%d");

		for (int i = 0; i < grid.getNTessellations(); ++i) {
		    File fout = new File(String.format(output, i));
		    // GeoTessModelUtils.vtkTriangleSize(grid, fout, i);
		    GeoTessModelUtils.vtkRobinsonTriangleSize(grid, fout, properties.getDouble("centerLon", 12), i);
		    System.out.println(fout);
		}
	    } else
		GeoTessModelUtils.vtkRobinsonTriangleSize(grid, new File(output), properties.getDouble("centerLon", 12), tessId);

	}
	if ((output = properties.getProperty("kmlFile")) != null) {
	    output = output.replace("<gridID>", grid.getGridID());
	    
	    File dir = new File(output).getParentFile();
	    if (dir != null)
		dir.mkdirs();

	    if (tessId < 0) {
		if (!output.contains("%"))
		    output = expandFileName(output, "_tess_%d");

		for (int i = 0; i < grid.getNTessellations(); ++i) {
		    File fout = new File(String.format(output, i));
		    grid.writeGridKML(fout, i);
		    System.out.println(fout);
		}
	    } else
		grid.writeGridKML(new File(output), tessId);
	}
	if ((output = properties.getProperty("kmzFile")) != null) {
	    output = output.replace("<gridID>", grid.getGridID());
	    
	    File dir = new File(output).getParentFile();
	    if (dir != null)
		dir.mkdirs();

	    if (tessId < 0) {
		if (!output.contains("%"))
		    output = expandFileName(output, "_tess_%d");

		for (int i = 0; i < grid.getNTessellations(); ++i) {
		    File fout = new File(String.format(output, i));
		    grid.writeGridKML(fout, i);
		    System.out.println(fout);
		}
	    } else
		grid.writeGridKML(new File(output), tessId);
	}
	if ((output = properties.getProperty("gmtFile")) != null) {
	    output = output.replace("<gridID>", grid.getGridID());
	    
	    File dir = new File(output).getParentFile();
	    if (dir != null)
		dir.mkdirs();

	    if (tessId < 0) {
		if (!output.contains("%"))
		    output = expandFileName(output, "_tess_%d");

		for (int i = 0; i < grid.getNTessellations(); ++i) {
		    File fout = new File(String.format(output, i));
		    PrintStream ps = new PrintStream(fout);
		    for (int[] edge : grid.getEdges(i))
			ps.printf("%1.6f %1.6f %1.6f %1.6f%n", VectorGeo.getLatDegrees(grid.getVertex(edge[0])),
				VectorGeo.getLonDegrees(grid.getVertex(edge[0])),
				VectorGeo.getLatDegrees(grid.getVertex(edge[1])),
				VectorGeo.getLonDegrees(grid.getVertex(edge[1])));
		    ps.close();
		    System.out.println(fout);
		}
	    } else {
		File fout = new File(String.format(output, tessId));
		PrintStream ps = new PrintStream(fout);
		for (int[] edge : grid.getEdges(tessId))
		    ps.printf("%1.6f %1.6f %1.6f %1.6f%n", VectorGeo.getLatDegrees(grid.getVertex(edge[0])),
			    VectorGeo.getLonDegrees(grid.getVertex(edge[0])),
			    VectorGeo.getLatDegrees(grid.getVertex(edge[1])),
			    VectorGeo.getLonDegrees(grid.getVertex(edge[1])));
		ps.close();
		System.out.println(fout);
	    }
	}
    }

    static protected String expandFileName(String fileName, String subString) {
	int i = fileName.lastIndexOf('.');
	return i < 0 ? fileName : fileName.substring(0, i) + subString + fileName.substring(i);
    }

}
