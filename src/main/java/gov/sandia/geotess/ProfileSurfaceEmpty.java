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

public class ProfileSurfaceEmpty extends Profile
{

	/**
	 * Default constructor
	 */
	public ProfileSurfaceEmpty()
	{
	}

	/**
	 * Constructor that loads required information from an ascii file.
	 * 
	 * @param input
	 * @throws GeoTessException
	 * @throws GeoTessException
	 */
	protected ProfileSurfaceEmpty(Scanner input) throws GeoTessException
	{
		// do nothing
	}

//	/**
//	 * Constructor that loads required information from netcdf Iterator objects.
//	 * 
//	 * @param itRadii
//	 * @throws GeoTessException
//	 */
//	protected ProfileEmpty(IndexIterator itRadii) throws GeoTessException
//	{
//		this(itRadii.getFloatNext(), itRadii.getFloatNext());
//	}

	/**
	 * Constructor that loads required information from a binary file.
	 * 
	 * @param input
	 * @throws GeoTessException
	 * @throws IOException
	 */
	protected ProfileSurfaceEmpty(DataInputStream input) throws GeoTessException,
			IOException
	{
		// do nothing
	}

	@Override
	public ProfileType getType()
	{
		return ProfileType.SURFACE_EMPTY;
	}

	@Override
	public boolean equals(Object other)
	{
		return other != null && other instanceof ProfileSurfaceEmpty;
	}

	@Override
	public boolean isNaN(int nodeIndex, int attributeIndex)
	{
		return true;
	}

	@Override
	public double getValue(InterpolatorType rInterpType, int attributeIndex,
			double radius, boolean allowRadiusOutOfRange)
	{
		return Double.NaN;
	}

	@Override
	public double getValue(int attributeIndex, int nodeIndex)
	{
		return Double.NaN;
	}

	@Override
	public double getValueTop(int attributeIndex)
	{
		return Double.NaN;
	}

	@Override
	public double getRadius(int i)
	{
		return Double.NaN;
	}

	@Override
	public void setRadius(int node, float radius) { /* do nothing */ }
	
	@Override
	public Data[] getData()
	{
		return new Data[0];
	}

	@Override
	public Data getData(int i)
	{
		return null;
	}

	@Override
	public void setData(Data... data)
	{
		// do nothing
	}

	@Override
	public void setData(int index, Data data)
	{
		// do nothing
	}

	@Override
	public double getRadiusTop()
	{
		return Double.NaN;
	}

	@Override
	public Data getDataTop()
	{
		return null;
	}

	@Override
	public double getRadiusBottom()
	{
		return Double.NaN;
	}

	@Override
	public Data getDataBottom()
	{
		return null;
	}

	@Override
	public int getNRadii()
	{
		return 0;
	}

	@Override
	public int getNData()
	{
		return 0;
	}

	@Override
	public float[] getRadii()
	{
		return new float[0];
	}

	@Override
	protected void write(Writer output) throws IOException
	{
		output.write(String.format("%d%n", getType().ordinal()));
	}

	@Override
	protected void write(DataOutputStream output) throws IOException
	{
		output.writeByte((byte) getType().ordinal());
	}

	@Override
	public int findClosestRadiusIndex(double radius)
	{
		return -1;
	}

	@Override
	public void setPointIndex(int nodeIndex, int pointIndex)
	{
	}

	@Override
	public int getPointIndex(int nodeIndex)
	{
		return -1;
	}

	/**
	 * Returns an independent deep copy of this profile.
	 */
	@Override
	public Profile copy() throws GeoTessException
	{
		return new ProfileSurfaceEmpty();
	}

	@Override
	public void resetPointIndices()
	{
	}

	/**
	 * Outputs this Profile as a formatted string.
	 */
	@Override
	public String toString()
	{
		return "  Type: "  + getType().name() + NL;
	}

}
