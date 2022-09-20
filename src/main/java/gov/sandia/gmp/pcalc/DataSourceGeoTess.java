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
package gov.sandia.gmp.pcalc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;

import gov.sandia.geotess.Data;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessModelUtils;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.geotess.PointMap;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.geotessbuilder.GeoTessBuilderMain;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.seismicitydepth.SeismicityDepthModel;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.globals.DataType;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.Polygon;
import gov.sandia.gmp.util.numerical.polygon.PolygonGlobal;
import gov.sandia.gmp.util.numerical.polygon.PolygonSmallCircles;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class DataSourceGeoTess extends DataSource
{
    public DataSourceGeoTess(PCalc pcalc) throws Exception
    {
	super(pcalc);

	bucket.inputType = IOType.GEOTESS;

	pcalc.extractStaPhaseInfo(bucket, true);
	Receiver receiver = bucket.receivers.get(0);

	int modelDimensions = properties.containsKey("geotessDepthSpacing")  
		|| properties.containsKey("geotessDepths") ? 3 : 2;

	GeoTessPosition seismicity_depth = null;
	int seismicityDepthMinIndex = -1;
	int seismicityDepthMaxIndex = -1;

	double depthSpacing = Double.NaN;
	double[] depths = null;
	boolean spanSeismicityDepth = false;

	if (modelDimensions == 3)
	{
	    if (properties.containsKey("geotessDepthSpacing")  
		    && properties.containsKey("geotessDepths"))
		throw new Exception("Cannot specify both geotessDepthSpacing and geotessDepths");

	    if (properties.containsKey("geotessDepths"))
	    {
		depths = properties.getDoubleArray("geotessDepths");
		if (depths.length < 2)
		    throw new Exception(String.format("property geotessDepths = %s but must specify at least two depths",
			    properties.getProperty("geotessDepths")));
		for (int i=1; i<depths.length; ++i)
		    if (depths[i] <= depths[i-1])
			throw new Exception(String.format("property geotessDepths = %s.  Depths are not monotonically increasing.",
				properties.getProperty("geotessDepths")));
	    }

	    if (properties.containsKey("geotessDepthSpacing"))
	    {
		depthSpacing = properties.getDouble("geotessDepthSpacing");
		if (depthSpacing <= 0.)
		    throw new Exception(String.format("property geotessDepthSpacing = %1.3f but must be > 0. when geotessModelDimensions = 3",
			    depthSpacing));
	    }

	    spanSeismicityDepth = properties.getBoolean("spanSeismicityDepth", true);

	    // if property seismicityDepthModel is specified, then the seismicity depth model will be loaded from the
	    // the specified file.  If seismicityDepthModel is 'default', or is not specified, then the default model
	    // will be loaded from the internal resources directory.
	    seismicity_depth = SeismicityDepthModel.getGeoTessPosition(properties.getProperty("seismicityDepthModel", "default"));

	    seismicityDepthMinIndex = seismicity_depth.getModel().getMetaData().getAttributeIndex("SEISMICITY_DEPTH_MIN");
	    seismicityDepthMaxIndex = seismicity_depth.getModel().getMetaData().getAttributeIndex("SEISMICITY_DEPTH_MAX");

	    if (log.isOutputOn())
	    {
		log.writeln("Seismicity Depth Model: \n");
		//log.writeln(seismicity_depth.getModel());
		log.writeln(GeoTessModelUtils.statistics(seismicity_depth.getModel()));
		log.writeln();
	    }

	    if (spanSeismicityDepth && depths != null)
	    {
		// find min and max seismicityDepth;
		double smin = Double.POSITIVE_INFINITY;
		double smax = Double.NEGATIVE_INFINITY;
		PointMap m = seismicity_depth.getModel().getPointMap();
		for (int i=0; i<m.size(); ++i)
		{
		    smin = Math.min(smin, m.getPointValueDouble(i, seismicityDepthMinIndex));
		    smax = Math.max(smax, m.getPointValueDouble(i, seismicityDepthMaxIndex));
		}

		if (depths[0] > smin || depths[depths.length-1] < Math.min(700., smax))
		    throw new Exception(String.format(
			    "geotessDepths range does not span seismicityDepth range.\n"
				    + "geotessDepths range   = [%1.3f, %1.3f]\n"
				    + "seismicityDepth range = [%1.3f, %1.3f]%n",
				    depths[0], depths[depths.length-1], smin, smax
			    ));
	    }
	}

	// Create a MetaData object in which we can specify information
	// needed for model construction.
	GeoTessMetaData metaData = new GeoTessMetaData();

	// will repopulate the description in DataSinkGeoTess before writing to output file.
	metaData.setDescription("");

	// Specify a list of layer names delimited by semi-colons
	if (modelDimensions == 2)
	    metaData.setLayerNames("surface");
	else
	    metaData.setLayerNames("sesimicity_depth");

	String[] attributes = new String[pcalc.outputAttributes.size()];
	String[] units = new String[pcalc.outputAttributes.size()];
	for (int i=0; i< pcalc.outputAttributes.size(); ++i)
	{
	    attributes[i] = pcalc.outputAttributes.get(i).toString();
	    units[i] = pcalc.outputAttributes.get(i).getUnits();
	}
	metaData.setAttributes(attributes, units);

	// specify the DataType for the data. All attributes, in all
	// profiles, will have the same data type.  Note that this
	// applies only to the data; radii are always stored as floats.
	metaData.setDataType(DataType.valueOf(
		properties.getProperty("geotessDataType", "FLOAT").toUpperCase()));

	// specify the name of the software that is going to generate
	// the model.  This gets stored in the model for future reference.
	metaData.setModelSoftwareVersion("PCalc "+PCalc.getVersion());

	// specify the date when the model was generated.  This gets
	// stored in the model for future reference.
	metaData.setModelGenerationDate(new Date().toString());

	metaData.setEarthShape(VectorGeo.earthShape);

	GeoTessGrid grid = null;

	if (pcalc.properties.containsKey("geotessInputGridFile"))
	{
	    grid = new GeoTessGrid(pcalc.properties.getFile("geotessInputGridFile"));
	    // if grid vertex[0] is located at the north pole, 
	    // and the current station is not located at the north pole,
	    // and property geotessRotateGridToStation is true, 
	    // then set euler rotation angles in the model.
	    double[] station = receiver.getUnitVector();
	    if (VectorUnit.parallel(grid.getVertex(0), new double[] {0., 0., 1.})
		    && !VectorUnit.parallel(station, new double[] {0., 0., 1.})
		    && pcalc.properties.getBoolean("geotessRotateGridToStation", false))
		metaData.setEulerRotationAngles(VectorUnit.getEulerRotationAnglesDegrees(
			station));
	}
	else
	{
	    PropertiesPlus gridProperties = new PropertiesPlus();
	    gridProperties.setProperty("verbosity = 0");
	    gridProperties.setProperty("gridConstructionMode = scratch");
	    gridProperties.setProperty("nTessellations = 1");
	    gridProperties.setProperty("baseEdgeLengths = 64");
	    	    
	    for (Entry<Object, Object> p : properties.entrySet())
	    {
		String key = (String) p.getKey();
		if (key.startsWith("geotess") && !key.equals("geotessGridFile"))
		{
		    key = key.substring(7,8).toLowerCase()+key.substring(8);
		    gridProperties.setProperty(key,  (String) p.getValue());
		}
	    }

	    if (gridProperties.containsKey("polygons"))
	    {
		String property = properties.getProperty("geotessPolygons");
		property = property.replaceAll("<site.lat>", String.format("%1.6f", receiver.getLatDegrees()))
			.replaceAll("<site.lon>", String.format("%1.6f", receiver.getLonDegrees()));
		gridProperties.setProperty("polygons", property);
	    }

	    // apply rotation that will place grid vertex 0 at the station location.
	    if (pcalc.properties.getBoolean("geotessRotateGridToStation", true))
	    {
		gridProperties.setProperty("rotateGrid", String.format("%1.6f %1.6f",
				receiver.getLatDegrees(), receiver.getLonDegrees()));
//		double[] v = VectorGeo.getVectorDegrees(receiver.getLatDegrees(), receiver.getLonDegrees()); 
//		double[] e = new double[] {atan2(v[1], v[0]) + Math.PI/2., acos(v[2]), PI/2.};
//		gridProperties.setProperty(String.format("eulerRotationAngles = %1.6f, %1.6f %1.6f",
//			Math.toDegrees(e[0]), Math.toDegrees(e[1]), Math.toDegrees(e[2])));
	    }

	    grid = (GeoTessGrid) GeoTessBuilderMain.run(gridProperties);
	}

	String outputType = properties.getProperty("outputType", "geotess");

	if (outputType.equalsIgnoreCase("libcorr3d"))
	{
	    boolean ok = pcalc.outputAttributes.size() == 2 
		    && pcalc.outputAttributes.get(1) == GeoAttributes.TT_MODEL_UNCERTAINTY
		    && (pcalc.outputAttributes.get(0) == GeoAttributes.TT_DELTA_AK135 
		    || pcalc.outputAttributes.get(0) == GeoAttributes.TT_PATH_CORRECTION);

	    if (!ok)
	    {
		String outputAttributes = "";
		for (GeoAttributes a : pcalc.outputAttributes)
		    outputAttributes = outputAttributes + ", "+a.toString().toLowerCase();
		if (outputAttributes.length() > 2)
		    outputAttributes = outputAttributes.substring(2);

		throw new Exception(String.format("When outputType is %s outputAttributes must \n"
			+ "equal tt_delta_ak135, tt_model_uncertainty but that is not the case.\n"
			+ "outputAttributes = %s",
			outputType, outputAttributes));
	    }

	    LibCorr3DModel model = new LibCorr3DModel(grid, metaData);
	    Receiver r = bucket.receivers.get(0);
	    model.setSite(r.getSta(), r.getOndate(), r.getOffdate(), r.getLatDegrees(),
		    r.getLonDegrees(), -r.getDepth(), r.getStaName(),
		    r.getStaTypeString(), r.getRefsta(), r.getDnorth(), r.getDeast());

	    model.setPhase(bucket.phases.get(0).toString());
	    model.setSupportedPhases(bucket.supportedPhases);

	    model.setBaseModel(properties.getProperty("lookup2dModel", "AK135"));
	    model.setBaseModelVersion("-");
	    model.setComments("-");
	    
	    model.setParameters(metaData.getAttributeNamesString());

	    bucket.geotessModel = model;
	}
	else
	    bucket.geotessModel = new GeoTessModel(grid, metaData);

	double geotessActiveNodeRadius = properties.getDouble("geotessActiveNodeRadius", -1.);
	Polygon polygon;
	if (geotessActiveNodeRadius > 0.)
	    polygon = new PolygonSmallCircles(receiver.getUnitVector(), true,
		    Math.toRadians(geotessActiveNodeRadius));
	else
	    polygon = new PolygonGlobal(true);

	double[] dataD = new double[bucket.geotessModel.getNAttributes()];
	Arrays.fill(dataD, Double.NaN);

	float[] dataF = new float[bucket.geotessModel.getNAttributes()];
	Arrays.fill(dataF, Float.NaN);

	// Populate the model with profiles.
	if (modelDimensions == 2)
	{
	    // this is a 2D model with data only at the surface of the earth
	    for (int vtx = 0; vtx < bucket.geotessModel.getNVertices(); ++vtx)
		if (polygon.contains(bucket.geotessModel.getVertex(vtx)))
		    bucket.geotessModel.setProfile(vtx,
			    metaData.getDataType() == DataType.DOUBLE ?
				    Data.getDataDouble(dataD.clone()) :
					Data.getDataFloat(dataF.clone()));
		else
		    bucket.geotessModel.setProfile(vtx);
	}
	else if (depths != null)
	{
	    double minDepth, maxDepth;

	    // user specified a list of depths where radial nodes are to be deployed.
	    for (int vtx = 0; vtx < bucket.geotessModel.getNVertices(); ++vtx)
	    {
		// retrieve the unit vector corresponding to the current vertex
		double[] vertex = bucket.geotessModel.getVertex(vtx);

		double earthRadius = VectorGeo.getEarthRadius(vertex);

		double[] z = depths.clone();

		if (spanSeismicityDepth)
		{
		    // user specified that only depths that span the seismicity depth range
		    // are to be included.  So, all depths > seismicity_depth_min and
		    // < seismicity_depth_max, plus one depth <= seismicity_depth_min and
		    // one depth >= seismicity_depth_max

		    seismicity_depth.set(vertex, earthRadius);

		    minDepth = seismicity_depth.getValue(seismicityDepthMinIndex);

		    // maxDepth can be no less than minDepth and no greater than 700 km.
		    maxDepth = Math.min(700., Math.max(minDepth, seismicity_depth.getValue(seismicityDepthMaxIndex)));

		    if (Math.abs(minDepth - maxDepth) < 1e-3)
			z = new double[] {maxDepth};
		    else
		    {
			// find index of depth such that depth[zfirst] <= minDepth
			int zfirst = Globals.hunt(depths, minDepth, true, true);

			// find index of depth such that depth[zlast] >= maxDepth
			int zlast = depths.length-1;                	
			for (int i=depths.length-1; i >= 0; --i)
			    if (depths[i] >= maxDepth)
				zlast = i;  
			    else
				break;

			// copy depths between zfirst and zlast inclusive from depths[] to a[].
			// There is guaranteed to be at least two of them.
			ArrayListDouble a = new ArrayListDouble(zlast-zfirst+1);
			for (int i=zfirst; i<=zlast; ++i)
			    a.add(depths[i]);

			if (a.size() > 2)
			{
			    // now set a[0]=minDepth and a[a.size()-1]=maxDepth
			    double r1 = (minDepth-a.get(0))/(a.get(1)-a.get(0));
			    double r2 = (maxDepth-a.get(a.size()-2))/(a.get(a.size()-1)-a.get(a.size()-2)); 
			    if (a.size() == 3)
			    {
				if (r1 > 0.9 || r2 < 0.1)
				    a.remove(1);
			    }
			    else
			    {
				if (r1 > 0.9)
				    a.remove(1);

				if (r2 < 0.1)
				    a.remove(a.size()-2);
			    }
			}
			a.set(0, minDepth);
			a.set(a.size()-1, maxDepth);

			z = a.toArray();
		    }
		}

		float[] radii = new float[z.length];
		for (int i=0; i<z.length; ++i)
		    radii[i] = (float) (earthRadius-z[z.length-1-i]);

		if (polygon.contains(vertex))
		{
		    if (metaData.getDataType() == DataType.DOUBLE)
		    {
			double[][] rawData = new double[radii.length][];
			for (int i = 0; i < radii.length; ++i)
			    rawData[i] = dataD.clone();
			bucket.geotessModel.setProfile(vtx, 0, radii, rawData);
		    }
		    else if (metaData.getDataType() == DataType.FLOAT)
		    {
			float[][] rawData = new float[radii.length][];
			for (int i = 0; i < radii.length; ++i)
			    rawData[i] = dataF.clone();
			bucket.geotessModel.setProfile(vtx, 0, radii, rawData);
		    }
		}
		else
		    bucket.geotessModel.setProfile(vtx, 0, radii); 
	    }
	}
	else
	{
	    for (int vtx = 0; vtx < bucket.geotessModel.getNVertices(); ++vtx)
	    {
		// retrieve the unit vector corresponding to the current vertex
		double[] vertex = bucket.geotessModel.getVertex(vtx);

		seismicity_depth.set(vertex, 1e4);
		double minDepth = seismicity_depth.getValue(seismicityDepthMinIndex);

		// maxDepth can be no less than minDepth and no greater than 700 km.
		double maxDepth = Math.min(700., Math.max(minDepth, seismicity_depth.getValue(seismicityDepthMaxIndex)));

		double earthRadius = VectorGeo.getEarthRadius(vertex);

		float[] radii = Globals.getArrayFloat(earthRadius-maxDepth, earthRadius-minDepth, depthSpacing);

		if (polygon.contains(vertex))
		{
		    if (metaData.getDataType() == DataType.DOUBLE)
		    {
			double[][] rawData = new double[radii.length][];
			for (int i = 0; i < radii.length; ++i)
			    rawData[i] = dataD.clone();
			bucket.geotessModel.setProfile(vtx, 0, radii, rawData);
		    }
		    else if (metaData.getDataType() == DataType.FLOAT)
		    {
			float[][] rawData = new float[radii.length][];
			for (int i = 0; i < radii.length; ++i)
			    rawData[i] = dataF.clone();
			bucket.geotessModel.setProfile(vtx, 0, radii, rawData);
		    }
		}
		else
		    bucket.geotessModel.setProfile(vtx, 0, radii);
	    }
	}

	PointMap pm = bucket.geotessModel.getPointMap();
	bucket.points = new ArrayList<GeoVector>(pm.size());

	if (bucket.geotessModel.is3D())
	    for (int i=0; i<pm.size(); ++i)
		bucket.points.add(new GeoVector(pm.getPointUnitVector(i), pm.getPointRadius(i)));
	else
	    for (int i=0; i<pm.size(); ++i)
		bucket.points.add(new GeoVector(pm.getPointUnitVector(i),
			VectorGeo.getEarthRadius(pm.getPointUnitVector(i))));


    }

}
