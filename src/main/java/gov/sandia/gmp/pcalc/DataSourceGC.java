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
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.geovector.GeoVectorLayer;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;

public class DataSourceGC extends DataSource
{
    public DataSourceGC(PCalc pcalc) throws Exception
    {
	super(pcalc);

	bucket.inputType = IOType.GREATCIRCLE;

	if (pcalc.application == Application.PREDICTIONS)
	    pcalc.extractStaPhaseInfo(bucket, true);

	GeoVector gcStart = properties.getGeoVector("gcStart");

	bucket.greatCircle = null;

	if (properties.containsKey("gcEnd"))
	    bucket.greatCircle = new GreatCircle(gcStart.getUnitVector(), properties.getGeoVector("gcEnd").getUnitVector());
	else if (properties.containsKey("gcAzimuth") && properties.containsKey("gcDistance"))
	{
	    double azimuth = properties.getDouble("gcAzimuth", Globals.NA_VALUE);
	    double distance = properties.getDouble("gcDistance", Globals.NA_VALUE);
	    if (azimuth != Globals.NA_VALUE && distance != Globals.NA_VALUE)
		bucket.greatCircle = new GreatCircle(gcStart.getUnitVector(), 
			VectorUnit.move(gcStart.getUnitVector(), Math.toRadians(distance), Math.toRadians(azimuth)));
	}

	if (bucket.greatCircle == null)
	    throw new GMPException("\nNot enough information in property file to create great circle. \n"
		    +"Must specify gcStart and either gcEnd, or gcDistance and gcAzimuth \n");

	double gcFirstDistance = properties.getDouble("gcFirstDistance", 0.);
	double gcLastDistance = properties.getDouble("gcLastDistance", bucket.greatCircle.getDistanceDegrees());
	int nPoints;
	boolean gcOnCenters = properties.getBoolean("gcOnCenters", false);
	double spacing;

	if (properties.containsKey("gcNpoints")) {
	    nPoints = properties.getInt("gcNpoints");
	}
	else if (properties.containsKey("gcSpacing")) {
	    nPoints = (int)Math.ceil((gcLastDistance-gcFirstDistance)/properties.getDouble("gcSpacing")) 
		    + (gcOnCenters ? 0 : 1);
	}
	else
	    throw new GMPException(String.format("%nMust specify either gcSpacing or gcNpoints%n"));

	spacing = (gcLastDistance-gcFirstDistance)/Math.max(1, (gcOnCenters ? nPoints : nPoints-1));

	ArrayList<GeoVectorLayer> points = new ArrayList<>(nPoints);
	for (int i=0; i<nPoints; ++i)
	{
	    double d = gcFirstDistance + (i + (gcOnCenters ? 0.5 : 0.)) *spacing;
	    points.add(new GeoVectorLayer(bucket.greatCircle.getPoint(Math.toRadians(d)), 1e4));
	}

	bucket.points = expandPointList(pcalc.getGeoTessModel(), points);

	String[] parameters = properties.getProperty("gcPositionParameters", "")
		.replaceAll(",", " ").split("\\s+");

	bucket.positionParameters = new ArrayList<GeoAttributes>();

	for (String attribute : parameters)
	{
	    attribute = attribute.trim();
	    if (attribute.length() > 0)
		try
	    {
		    bucket.inputAttributes.add(attribute.trim().toLowerCase());
		    bucket.positionParameters.add(GeoAttributes.valueOf(attribute.toUpperCase()));
	    } 
	    catch (java.lang.IllegalArgumentException ex1)
	    {
		throw new GMPException(String.format("%nProperty gcPositionParameters contains invalid parameter %s%n", attribute));
	    }
	}

	// build the input header from the list of column names
	setInputHeader();

    }

}
