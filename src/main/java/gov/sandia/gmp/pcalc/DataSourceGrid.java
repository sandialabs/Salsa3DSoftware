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
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

public class DataSourceGrid extends DataSource
{
	public DataSourceGrid(PCalc pcalc) throws Exception
	{
		super(pcalc);

		bucket.inputType = IOType.GRID;

		pcalc.extractDepthInfo(bucket);

		if (pcalc.application == Application.PREDICTIONS)
			pcalc.extractStaPhaseInfo(bucket, true);

		GeoVector[][] grid = null;

		if (properties.getProperty("gridRangeLat") != null 
				|| properties.getProperty("gridRangeLon") != null)
		{
			double[] latRange = properties.getDoubleArray("gridRangeLat");
			if (latRange == null)
				throw new GMPException(String.format("%ngridRangeLon is specified but gridRangeLat is not"));
			if (latRange.length != 3)
				throw new GMPException(String.format("%ngridRangeLat = %s%nbut must specify 3 values: lat1, lat2, nlat%n",
						properties.getProperty("gridRangeLat")));

			double[] lonRange = properties.getDoubleArray("gridRangeLon");
			if (lonRange == null)
				throw new GMPException(String.format("%ngridRangeLat is specified but gridRangeLon is not"));
			if (lonRange.length != 3)
				throw new GMPException(String.format("%ngridRangeLon = %s%nbut must specify 3 values: lon1, lon2, nlon%n",
						properties.getProperty("gridRangeLon")));

			double lat1 = latRange[0];
			double lat2 = latRange[1];
			int nLat = (int)Math.round(latRange[2]);
			double latSpacing = (lat2-lat1)/(nLat-1.);


			double lon1 = lonRange[0];
			double lon2 = lonRange[1];
			int nLon  = (int)Math.round(lonRange[2]);
			double lonSpacing = (lon2-lon1)/(nLon-1.);

			if (nLat < 2)
				throw new GMPException(String.format("%ngridRangeLat value of npoints must be > 1"));

			if (nLon < 2)
				throw new GMPException(String.format("%ngridRangeLon value of npoints must be > 1"));

			grid = new GeoVector[nLat][nLon];
			for (int i=0; i<nLat; ++i)
				for (int j=0; j<nLon; ++j)
					grid[i][j] = new GeoVector(lat1+i*latSpacing, lon1+j*lonSpacing, 0., true);
		}
		else
		{
			GeoVector gridCenter = properties.getGeoVector("gridCenter");

			double[] gridHeight = properties.getDoubleArray("gridHeight");
			if (gridHeight == null)
				throw new GMPException(String.format("%ngridHeight is not specified in property file"));
			if (gridHeight.length != 2)
				throw new GMPException(String.format("%ngridHeight = %s%nbut must specify 2 values: height of grid and nPoints%n",
						properties.getProperty("gridHeight")));

			double[] gridWidth = properties.getDoubleArray("gridWidth");
			if (gridWidth == null)
				throw new GMPException(String.format("%ngridWidth is not specified in property file"));
			if (gridWidth.length != 2)
				throw new GMPException(String.format("%ngridWidth = %s%nbut must specify 2 values: width of grid and nPoints%n",
						properties.getProperty("gridWidth")));


			int npointsLat = (int)Math.round(gridHeight[1]);
			if (npointsLat <= 0)
				throw new GMPException("\ngridHeight npoints must be > 0");
			double latSpacing = gridHeight[0]/(npointsLat-1);

			int npointsLon = (int)Math.round(gridWidth[1]);
			if (npointsLon <= 0)
				throw new GMPException("\ngridWidth npoints must be > 0");
			double lonSpacing = gridWidth[0]/(npointsLon-1);

			String sPole = properties.getProperty("gridPole", "90DegreesNorth");
			GeoVector pole;
			if (sPole.equalsIgnoreCase("northPole"))
				pole = new GeoVector(new double[] { 0., 0., 1. }, 1.);
			else if (sPole.equalsIgnoreCase("90DegreesNorth"))
				pole = gridCenter.moveNorth(Math.PI/2.);
			else 
				try
			{
					pole = properties.getGeoVector("gridPole");
			}
			catch (PropertiesPlusException ex)
			{
				throw new GMPException(String.format("%nUnable to parse property gridPole = %s%n"
						+"gridPole must equal [northPole | 90DegreesNorth | valid lat lon values]%n",
						sPole));
			}

			if (log.isOutputOn())
				log.writeln(String.format("lat,lon of pole of rotation = %1.4f, %1.4f%n",
						pole.getLatDegrees(), pole.getLonDegrees()));

			grid = gridCenter.getGrid(pole, npointsLat, Math.toRadians(latSpacing), 
					npointsLon, Math.toRadians(lonSpacing));
		}

		bucket.points = new ArrayList<GeoVector>(grid.length*grid[0].length);
		if (pcalc.yFast)
			for (int i=0; i<grid.length; ++i)
				for (int j=0; j<grid[i].length; ++j)
					bucket.points.add(grid[i][j].setDepth(Double.NaN));
		else
			for (int j=0; j<grid[0].length; ++j)
				for (int i=0; i<grid.length; ++i)
					bucket.points.add(grid[i][j].setDepth(Double.NaN));

		String[] parameters = properties.getProperty("gridPositionParameters", "longitude latitude depth")
		.replaceAll(",", " ").replaceAll("  ", " ").split(" ");

		bucket.positionParameters = new ArrayList<GeoAttributes>();

		for (String attribute : parameters)
		{
			attribute = attribute.trim();
			if (attribute.length() > 0)
			{
				try
				{
					GeoAttributes geoAttribute = GeoAttributes.valueOf(attribute.toUpperCase());
					if (geoAttribute == GeoAttributes.LATITUDE 
							|| geoAttribute == GeoAttributes.LONGITUDE 
							|| geoAttribute == GeoAttributes.DEPTH)
					{
						bucket.inputAttributes.add(attribute.trim().toLowerCase());
						bucket.positionParameters.add(geoAttribute);
					}
				} 
				catch (java.lang.IllegalArgumentException ex1)
				{
					throw new GMPException(String.format("Property gcPositionParameters contains invalid parameter %s%n", attribute));
				}
			}
		}

		// build the input header from the list of column names
		setInputHeader();

	}

}
