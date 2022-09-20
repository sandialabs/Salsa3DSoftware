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

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

import gov.sandia.gmp.util.globals.InterpolatorType;

//import ucar.ma2.IndexIterator;

/**
 * A Profile defined by zero radii and 1 Data object. This is useful for
 * representing 2D models.
 * 
 * @author Sandy Ballard
 * 
 */
public class ProfileSurface extends Profile
{
	private Data data;

	private int pointIndex = -1;

	/**
	 * Parameterized constructor that takes a single Data object and no radii.
	 * This object keeps a reference to the supplied Data object (no copy is made).
	 * 
	 * @param data
	 *            a single Data object
	 */
	public ProfileSurface(Data data)
	{
		this.data = data;
	}

	/**
	 * Constructor that loads required information from an ascii file.
	 * 
	 * @param input
	 * @param metaData
	 * @throws GeoTessException
	 */
	protected ProfileSurface(Scanner input, GeoTessMetaData metaData)
			throws GeoTessException, IOException
	{
		data = Data.getData(input, metaData);
	}

//	/**
//	 * Constructor that loads required information from netcdf Iterator objects.
//	 * 
//	 * @param itValues
//	 * @param metaData
//	 * @throws GeoTessException
//	 */
//	protected ProfileSurface(IndexIterator itValues, GeoTessMetaData metaData)
//			throws GeoTessException
//	{
//		data = Data.getData(itValues, metaData);
//	}

	/**
	 * Constructor that loads required information from a binary file.
	 * 
	 * @param input
	 * @param metaData
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected ProfileSurface(DataInputStream input, GeoTessMetaData metaData)
			throws GeoTessException, IOException
	{
		data = Data.getData(input, metaData);
	}

	@Override
	protected void write(Writer output) throws IOException
	{
		output.write(String.format("%d %s%n", getType().ordinal(),
				data.toString()));
	}

	@Override
	protected void write(DataOutputStream output) throws IOException
	{
		output.writeByte((byte) getType().ordinal());
		data.write(output);
	}

//	@Override
//	protected void write(IndexIterator nPoints, IndexIterator radii,
//			IndexIterator values)
//	{
//		data.write(values);
//	}

	@Override
	public ProfileType getType()
	{
		return ProfileType.SURFACE;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof ProfileSurface))
			return false;
		
		return this.data.equals(((ProfileSurface)other).getData(0));
		
	}

	/**
	 * Retrieve the value of the specified attributes at the specified
	 * radius index.
	 * @param attributeIndex
	 * @param nodeIndex
	 * @return the value of the specified attributes at the specified
	 * radius index.
	 */
	@Override
	public double getValue(int attributeIndex, int nodeIndex)
	{
		return nodeIndex == 0 ? data.getDouble(attributeIndex)
				: Double.NaN;
	}

	/**
	 * Return true if the specified Data value is NaN.  
	 * For doubles and floats, this means not NaN.
	 * For bytes, shorts, ints and longs, always returns false
	 * since there is no value that is NaN
	 * @param nodeIndex
	 * @param attributeIndex
	 * @return true if the specified Data value is valid.  
	 */
	public boolean isNaN(int nodeIndex, int attributeIndex)
	{
		return nodeIndex == 0 ? data.isNaN(attributeIndex) : true;
	}

	/**
	 * Retrieve the value of the specified attribute at the top 
	 * of the layer.
	 * @param attributeIndex
	 * @return the value of the specified attribute at the top 
	 * of the layer.
	 */
	public double getValueTop(int attributeIndex)
	{ return data.getDouble(attributeIndex); }
	
	@Override
	public double getValue(InterpolatorType interpType, int attributeIndex,
			double radius, boolean allowRadiusOutOfRange)
	{
		return data.getDouble(attributeIndex);
	}
	
	@Override
	public double getRadius(int node)
	{
		return Double.NaN;
	}

	@Override
	public void setRadius(int node, float radius) { /* do nothing */ }
	
	@Override
	public Data[] getData()
	{
		return new Data[] { data };
	}

	@Override
	public Data getData(int node)
	{
		return data;
	}

	@Override
	public void setData(Data... data)
	{
		this.data = data[0];
	}

	/**
	 * Replace one of the Data objects currently associated with this Profile
	 * 
	 * @param index
	 * @param data
	 * @throws ArrayIndexOutOfBoundsException
	 */
	@Override
	public void setData(int index, Data data)
	{
		if (index == 0)
			this.data = data;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}

	@Override
	public double getRadiusTop()
	{
		return Double.NaN;
	}

	@Override
	public Data getDataTop()
	{
		return data;
	}

	@Override
	public double getRadiusBottom()
	{
		return Double.NaN;
	}
	
	@Override
	public int getRadiusIndex(double radius)
	{
		return -1;
	}

	@Override
	public Data getDataBottom()
	{
		return data;
	}

	@Override
	public int getNRadii()
	{
		return 0;
	}

	@Override
	public int getNData()
	{
		return 1;
	}

	@Override
	public float[] getRadii()
	{
		return new float[0];
	}

	/**
	 * Find the index of the node in this Profile that has radius closest to the
	 * supplied radius.
	 * 
	 * @param radius in km
	 * @return the index of the node in this Profile that has radius closest to the
	 * supplied radius.
	 */
	public int findClosestRadiusIndex(double radius)
	{
		return -1;
	}

	/**
	 * Set the pointIndex that corresponds to the supplied nodeIndex.  
	 * <p>There is a node index for each Data object in a profile and they are indexed from 0 to 
	 * the number of Data objects managed by a Profile.  There is a pointIndex for every 
	 * Data object in the entire model, indexed from 0 to the number of Data objects in the 
	 * model. 
	 * @param nodeIndex
	 * @param pointIndex
	 */
	public void setPointIndex(int nodeIndex, int pointIndex)
	{
		this.pointIndex = pointIndex;
	}
	
	@Override
	public void resetPointIndices()
	{ this.pointIndex = -1; }

	/**
	 * Get the pointIndex that corresponds to the supplied nodeIndex.  
	 * <p>There is a node index for each Data object in a profile and they are indexed from 0 to 
	 * the number of Data objects managed by a Profile.  There is a pointIndex for every 
	 * Data object in the entire model, indexed from 0 to the number of Data objects in the 
	 * model. 
	 * @param nodeIndex
	 * @return poitnIndex
	 */
	public int getPointIndex(int nodeIndex)
	{
		if (nodeIndex == 0)
			return pointIndex;
		throw new ArrayIndexOutOfBoundsException();
	}

	/**
	 * Outputs this Profile as a formatted string.
	 */
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
    buf.append("  Type: " + getType().name() + NL);
    buf.append("    Point Index: " + pointIndex + NL);
    buf.append("    Data: " + data.toString() + NL);
	  return buf.toString();
	}

	/**
	 * Returns an independent deep copy of this profile.
	 */
	@Override
	public Profile copy() throws GeoTessException
	{
		ProfileSurface ps = new ProfileSurface(data.copy());
		ps.pointIndex = pointIndex;
		return ps;
	}

}
