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

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.geovector.GeoVectorLayer;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.bender.BenderConstants.LayerSide;
import gov.sandia.gmp.util.containers.Tuple;
//import gov.sandia.gmp.pcalc.PCalc.GraphicsFormat;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;

/**
 * Abstract class that defines a generic source of InputData.
 * Derived classes might read InputData from a file, might generated
 * data along a great circle path, might define InputData on a 
 * grid pattern, or build a geotess model.
 * 
 * @author sballar
 *
 */
public abstract class DataSource implements Iterator<Bucket>
{
    protected PropertiesPlusGMP properties;

    protected ScreenWriterOutput log; 

    /**
     * Container class
     */
    protected Bucket bucket;

    protected Application application;

    //protected GraphicsFormat graphicsFormat;

    /**
     * The record that contains the names of the recognized columns in the data file.
     * Recognized columns are site_lat, origin_lon, phase, etc.
     */
    protected String inputHeader;

    /**
     * This is the set of GeoAttributes that will be sent to the Predictor.  Does
     * not accurately reflect input or output attributes.
     */
    protected EnumSet<GeoAttributes> requestedAttributes;

    /**
     * A String of length 1, either tab, comma or space 
     */
    protected String separator;

    protected int batchSize;

    /**
     * True when there is more data that has not yet been processed.
     */
    protected boolean moreData;

    /**
     * Comments and empty records read from the top of the input file.
     */
    protected ArrayList<String> comments = new ArrayList<String>();

    /**
     * Static factory method that queries the Properties object owned by PCalc
     * and constructs the correct type of DataSource object. 
     * @param pcalc
     * @return a DataSource object.
     * @throws GMPException
     * @throws FatalDBUtilLibException
     * @throws IOException
     */
    public static DataSource getDataSource(PCalc pcalc) 
	    throws Exception
    {
	switch (pcalc.inputType)
	{
	case FILE:
	    if (pcalc.application == Application.MODEL_QUERY)
		return new DataSourceFileModelQuery(pcalc);
	    else
		return new DataSourceFilePredictions(pcalc);
	case DATABASE:
	    return new DataSourceDB(pcalc);
	case GREATCIRCLE:
	    return new DataSourceGC(pcalc);
	case GRID:
	    return new DataSourceGrid(pcalc);
	case GEOTESS:
	    return new DataSourceGeoTess(pcalc);
	default:
	    throw new Exception("Missing enum");
	}
    }

    protected DataSource(PCalc pcalc) throws GMPException
    {
	this.properties = pcalc.properties;
	this.log = pcalc.log;

	this.application = pcalc.application;

	//this.graphicsFormat = pcalc.graphicsFormat;

	//    VectorGeo.earthShape = EarthShape.valueOf(
	//        properties.getProperty("earthShape", "WGS84"));

	this.requestedAttributes = pcalc.predictionAttributes;

	separator = properties.getProperty("separator", "space");
	if (separator.equals("tab"))
	    separator = new String(new byte[] {9});
	else if (separator.equals("comma"))
	    separator = ",";
	else
	    separator = " ";

	moreData = true;

	bucket = pcalc.bucket;

	bucket.inputAttributes = new ArrayList<String>();
    }

    /**
     * @return a single String containing list of input attributes
     * (column headings), separated by the specified separator 
     * (space, comma or tab).
     */
    public String getInputHeader()
    {
	return inputHeader;
    }

    /**
     * Every DataSource class should call this method
     * after it has populated inputAttributes in order
     * to populate inputHeader.
     */
    protected void setInputHeader()
    {
	StringBuffer buf = new StringBuffer();
	{
	    for (String s : bucket.inputAttributes)
		buf.append(s).append(separator);
	    if (buf.length() > 0)
		buf.setLength(buf.length()-1);
	}
	inputHeader = buf.toString();		
    }

    /**
     * @return Comments and empty records read from the top of the input file.	 
     */
    protected ArrayList<String> getComments()
    {
	return comments;
    }

    @Override
    public boolean hasNext() {
	return moreData;
    }	

    @Override
    public Bucket next()
    {
	moreData = false;
	return bucket;
    }

    @Override
    public void remove() { /* do nothing */ }

    /**
     * DataSources that need to close something, such as 
     * a file, or database connection, should override this method.
     */
    public void close()
    {
	// do nothing by default.
    }


    /**
     * Extract depth specification from properties file.
     * Searches for the following parameters in specified order:
     * <ol>
     * <li>depths = comma or space delimited list of depth values.
     * <li>depthRange = first depth, last depth, number of depths
     * <li>depthLevels = a string like 'top of upper_crust' or 'below moho'
     * <li>maxDepthSpacing = maximum depth spacing in each layer of the model
     * </ol>
     * @param bucket
     * @throws GMPException
     * @throws IOException 
     */
    protected ArrayList<GeoVectorLayer> expandPointList(GeoTessModel model, ArrayList<GeoVectorLayer> points) 
	    throws Exception
    {

	String depthSpecificationMethod = properties.getProperty("depthSpecificationMethod");

	boolean depthFast = properties.getBoolean("depthFast", true);

	if (depthSpecificationMethod == null)
	    throw new GMPException("\nproperties file must specify depthSpecificationMethod = one of [ depths | depthRange | maxDepthSpacing | depthLevels ]\n");

	if (depthSpecificationMethod.equalsIgnoreCase("depths"))
	{
	    if (!properties.containsKey("depths"))
		throw new GMPException ("Must specify property 'depths' because depthSpecificationMethod = 'depths'");

	    double[] depths = properties.getDoubleArray("depths");

	    ArrayList<GeoVectorLayer> results = new ArrayList<>(points.size()*depths.length);

	    if (depthFast) {
		for (int i=0; i<points.size(); ++i)
		    for (int k=0; k<depths.length; ++k)
			results.add(new GeoVectorLayer(points.get(i).getLat(), points.get(i).getLon(), depths[k], false));
	    }
	    else {
		for (int k=0; k<depths.length; ++k)
		    for (int i=0; i<points.size(); ++i)
			results.add(new GeoVectorLayer(points.get(i).getLat(), points.get(i).getLon(), depths[k], false));
	    }

	    return results;
	}
	else if (depthSpecificationMethod.equalsIgnoreCase("depthRange"))
	{
	    if (!properties.containsKey("depthRange"))
		throw new GMPException ("Must specify property 'depthRange'");

	    double[] range = properties.getDoubleArray("depthRange");

	    if (range.length != 3)
		throw new GMPException(String.format("%ndepthRange = %s%nbut must specify 3 values: depth1, depth2 and n depths%n",
			properties.getProperty("depthRange")));

	    int nz = (int)Math.round(range[2]);

	    if (nz < 2)
		throw new GMPException("Number of depths specified in depth range must be >= 2");

	    double[] depths = new double[nz];
	    double z1 = range[0];
	    double dz = (range[1]-range[0])/(nz-1);
	    for (int i=0; i<nz; ++i)
		depths[i] = z1 + i*dz;

	    ArrayList<GeoVectorLayer> results = new ArrayList<>(points.size()*depths.length);

	    if (depthFast) {
		for (int i=0; i<points.size(); ++i)
		    for (int k=0; k<depths.length; ++k)
			results.add(new GeoVectorLayer(points.get(i).getLat(), points.get(i).getLon(), depths[k], false));
	    }
	    else {
		for (int k=0; k<depths.length; ++k)
		    for (int i=0; i<points.size(); ++i)
			results.add(new GeoVectorLayer(points.get(i).getLat(), points.get(i).getLon(), depths[k], false));
	    }

	    return results;
	}
	else if (depthSpecificationMethod.equalsIgnoreCase("depthLevels"))
	{
	    if (model == null)
		throw new GMPException("Trying to set depthLevels but geoModel is null");

	    if (!properties.containsKey("depthLevels"))
		throw new GMPException ("Must specify property 'depthLevels'");

	    String[] depthLevels = properties.getProperty("depthLevels").split(",");

	    double[][] depths = new double[points.size()][depthLevels.length];
	    int[][] majorLayerIndex  = new int[points.size()][depthLevels.length];

	    GeoTessPosition pos = model.getGeoTessPosition();
	    for (int i=0; i<points.size(); ++i) {
		pos.set(points.get(i).getLatDegrees(), points.get(i).getLonDegrees(), 1e4);
		for (int k=0; k<depthLevels.length; ++k)
		{
		    Tuple<Integer, LayerSide> horizon = getLevel(model, depthLevels[k]);
		    majorLayerIndex[i][k] = horizon.first.intValue();
		    LayerSide layerSide = horizon.second;

		    if (layerSide == LayerSide.TOP) {
			depths[i][k] = pos.getDepthTop(majorLayerIndex[i][k]);
		    }
		    else {
			depths[i][k] = pos.getDepthBottom(majorLayerIndex[i][k]);
		    }
		}

	    }

	    ArrayList<GeoVectorLayer> results = new ArrayList<>(points.size()*depthLevels.length);

	    if (depthFast) {
		for (int i=0; i<points.size(); ++i)
		    for (int k=0; k<depthLevels.length; ++k)
			results.add(new GeoVectorLayer(majorLayerIndex[i][k], points.get(i).getLat(), points.get(i).getLon(), depths[i][k], false));
	    }
	    else {
		for (int k=0; k<depthLevels.length; ++k)
		    for (int i=0; i<points.size(); ++i)
			results.add(new GeoVectorLayer(majorLayerIndex[i][k], points.get(i).getLat(), points.get(i).getLon(), depths[i][k], false));
	    }

	    return results;

	}
	else if (depthSpecificationMethod.equalsIgnoreCase("maxDepthSpacing"))
	{
	    if (model == null)
		throw new GMPException("Trying to set maxDepthSpacing but geoModel is null");

	    if (!properties.containsKey("maxDepthSpacing"))
		throw new GMPException ("Must specify property 'maxDepthSpacing'");

	    double maxDepthSpacing = properties.getDouble("maxDepthSpacing");

	    String maxDepthString = properties.getProperty("maxDepth");

	    double maxDepth = Double.POSITIVE_INFINITY;
	    Tuple<Integer, LayerSide> iface = null;  // major layer index and TOP or BOTTOM
	    if (maxDepthString != null) {
		try
		{
		    maxDepth = Double.parseDouble(maxDepthString);
		}
		catch (NumberFormatException e)
		{
		    iface = getLevel(model, maxDepthString);  // major layer index and TOP or BOTTOM
		    //minRadius.add(pos.getRadiusTop(iface.first.intValue()+(iface.second==LayerSide.TOP ? 0 : -1)));
		}
	    }


	    int[] pointsPerLayer = new int[model.getNLayers()];

	    GeoTessPosition pos = model.getGeoTessPosition();

	    // at this point we are iterating over the points along the great circle without considering depth.
	    for (int i=0; i<points.size(); ++i)
	    {

		pos.set(points.get(i).getLatDegrees(), points.get(i).getLonDegrees(), points.get(i).getDepth());

		if (iface != null) {
		    maxDepth = iface.second == LayerSide.TOP ? pos.getDepthTop(iface.first) : pos.getDepthBottom(iface.first);
		}

		int[] n = pos.getInterfacesPerLayer(pos.getEarthRadius()-maxDepth + 1e-6, maxDepthSpacing);
		for (int j=0; j<model.getNLayers(); ++j)
		    if (n[j] > pointsPerLayer[j])
			pointsPerLayer[j] = n[j];
	    }

	    int nDepths = 0;
	    for (int n : pointsPerLayer) nDepths += n;

	    double zBottom, zTop, dz, depth;

	    ArrayList<GeoVectorLayer> results = new ArrayList<>(points.size()*nDepths);

	    for (GeoVectorLayer point : points)
	    {
		pos.set(point.getLatDegrees(), point.getLonDegrees(), point.getDepth());

		if (iface != null)
		    maxDepth = iface.second == LayerSide.TOP ? pos.getDepthTop(iface.first) 
			    : pos.getDepthBottom(iface.first);

		for (int layer=model.getNLayers()-1; layer >= 0; --layer)
		{
		    if (pointsPerLayer[layer] > 1)
		    {
			zTop = pos.getDepthTop(layer);
			if (zTop > maxDepth)
			{
			    zTop = zBottom = maxDepth;
			}
			else 
			{
			    zBottom = pos.getDepthBottom(layer);
			    if (zBottom > maxDepth)
				zBottom = maxDepth;
			}

			dz = (zBottom - zTop) / (pointsPerLayer[layer]-1);
			for (int k=0; k < pointsPerLayer[layer]; ++k) {
			    depth = zTop + k * dz;

			    GeoVectorLayer g = new GeoVectorLayer(layer, point.getLat(), point.getLon(), depth, false);
			    results.add(g);
			}
		    }
		}

	    }
	    return results;
	}
	else
	    throw new GMPException("\nproperties file must specify depthSpecificationMethod = one of [ depths | depthRange | maxDepthSpacing | depthLevels ]\n");
    }

    protected Tuple<Integer, LayerSide> getLevel(GeoTessModel model, String level) throws Exception 
    {
	int majorLayerIndex = -1;
	LayerSide layerSide = null;

	level = level.trim().toUpperCase();
	if (level.equalsIgnoreCase("topography"))
	{
	    majorLayerIndex = Integer.MAX_VALUE;
	    layerSide = LayerSide.TOP;

	    if (application == Application.MODEL_QUERY)
		throw new GMPException(String.format(
			"%n'depthLevels = topography' %nis invalid for model queries.  %nUse 'depthLevels = below surface' instead.%n"));
	}
	else
	{
	    if (model == null)
		throw new GMPException("Trying to interpret level = '"+level
			+"' but geoTessModel is null");

	    String[] horizon = level.split(" ");

	    majorLayerIndex = model.getMetaData().getInterfaceIndex(horizon[horizon.length-1]);
	    if (majorLayerIndex < 0)
		throw new GMPException(String.format("%nlevel = '%s' but geoModel has no layer with name %s%n"
			+ "valid layer names are %s%n", 
			level, horizon[horizon.length-1],
			model.getMetaData().getLayerNamesString()));

	    if (horizon.length == 1 || horizon[0].equals("TOP"))
		layerSide = LayerSide.TOP;
	    else if (horizon[0].equals("BOTTOM"))
		layerSide = LayerSide.BOTTOM;
	    else if (horizon[0].equals("BELOW"))
		layerSide = LayerSide.TOP;
	    else if (horizon[0].equals("ABOVE"))
	    {
		layerSide = LayerSide.BOTTOM;
		++majorLayerIndex;
		if (majorLayerIndex >= model.getNLayers())
		    throw new GMPException("property depthLevels = '"+properties.getProperty("depthLevels")
		    +"' which is out of range");
	    }
	    else
		throw new GMPException("property depthLevels = '"+properties.getProperty("depthLevels")
		+"' but must be something like 'top of moho' or 'bottom of lower_crust'");
	}

	return new Tuple<Integer, LayerSide>(majorLayerIndex, layerSide);
    }

    /**
     * Retrieve a profile of GeoAttribute values as a function of depth in 
     * the model at the position of this InterpolatedNodeLayered object.  
     * NodesPerLayer should be obtained by calling getInterfacesPerLayer().  
     * The return value is 
     * a double[n][m] where n is the number of depths along the profile
     * and m is equal to the number of specified GeoAttributes PLUS ONE.
     * For row i, the first element is the depth of the node in km 
     * below sea level.  The remaining values are the values of the
     * requested GeoAttributes.  Requests for PVELOCITY and SVELOCITY  
     * are supported.
     * @throws GeoTessException 
     */
    public double[] getProfile(GeoTessPosition position, double radius0, int[] nodesPerLayer) throws GeoTessException
    {
	int nlayers = nodesPerLayer.length;
	int size = 0;
	for (int i = 0; i < nlayers; ++i)
	    size += nodesPerLayer[i];

	double[] radii = new double[size];

	int j = 0;
	double rBottom, rTop, dr;
	for (int i = nlayers-1; i>= 0; --i)
	{
	    if (nodesPerLayer[i] > 1)
	    {
		rTop = position.getRadiusTop(i);
		if (rTop < radius0)
		{
		    rTop = rBottom = radius0;
		}
		else 
		{
		    rBottom = position.getRadiusBottom(i);
		    if (rBottom < radius0)
			rBottom = radius0;
		}

		dr = (rTop - rBottom) / (nodesPerLayer[i]-1);
		for (int k = nodesPerLayer[i]-1; k >= 0; --k)
		    radii[j++] = position.getEarthRadius() - (rBottom + k * dr);
	    }
	}
	return radii;
    }

}
