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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

import gov.sandia.gmp.util.globals.DataType;

/**
 * An array of DataCustom objects with nAttributes elements will be associated
 * with each node of the model.
 *
 * @author jrhipp
 *
 */
public class DataArrayOfCustoms extends Data
{
	Data[] values;

	/**
	 * Constructor.
	 * 
	 * @param values
	 */
	public DataArrayOfCustoms(Data... values)
	{
		this.values = values;
	}

	public void setValues(Data[] vals)
	{
		values = vals;
	}

	/**
	 * Constructor that reads the array of values from an ascii file given a
	 * GeoTessMetaData data description (Legacy).
	 * 
	 * @param input    Scanner from which the data array of custom objects is read.
	 * @param metaData GeoTessMetaData describing the attributes.
	 */
	protected DataArrayOfCustoms(Scanner input, GeoTessMetaData metaData)
	{
		this(input, metaData.getNodeAttributes());
	}

	/**
	 * Constructor that reads the array of values from an ascii file given a
	 * AttributeDataDefinitions data description.
	 * 
	 * @param input   Scanner from which the data array of custom objects is read.
	 * @param attrDef AttributeDataDefinitions describing the attributes.
	 */
	protected DataArrayOfCustoms(Scanner input, AttributeDataDefinitions attrDef)
	{
		values = new Data[attrDef.getNAttributes()];
		for (int i = 0; i < values.length; ++i)
			values[i] = (Data) attrDef.getCustomDataType().read(input, attrDef);
	}

	/**
	 * Constructor that reads the array of values from a binary file given a
	 * GeoTessMetaData data description (Legacy).
	 * 
	 * @param input    DataInputStream from which the data array of custom objects
	 *                 are read.
	 * @param metaData GeoTessMetaData describing the attributes.
	 */
	protected DataArrayOfCustoms(DataInputStream input, GeoTessMetaData metaData)
			throws IOException
	{
		this(input, metaData.getNodeAttributes());
	}

	/**
	 * Constructor that reads the array of values from a binary file given a
	 * AttributeDataDefinitions data description.
	 * 
	 * @param input   DataInputStream from which the data array of custom objects
	 *                are read.
	 * @param attrDef AttributeDataDefinitions describing the attributes.
	 */
	protected DataArrayOfCustoms(DataInputStream input, AttributeDataDefinitions attrDef)
			throws IOException
	{
		values = new Data[attrDef.getNAttributes()];
		for (int i = 0; i < values.length; ++i)
			values[i] = (Data) attrDef.getCustomDataType().read(input, attrDef);
	}

	/**
	 * Returns DataType.CUSTOM
	 * 
	 * @return DataType.CUSTOM
	 */
	@Override
	public DataType getDataType()
	{
		return DataType.CUSTOM;
	}

	/**
	 * Returns true if this and other are of the same DataType, both have a
	 * single element and those elements are == (or both values are NaN).
	 * 
	 * @param other
	 * @return true if this and other are of the same DataType, both have a
	 *         single element and those elements are == (or both values are NaN).
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof DataArrayOfCustoms))
			return false;
		
		for (int i = 0; i < values.length; ++i)
			if (!values[i].equals(other)) return false;
		return true;
	}

	public Data getData(int attributeIndex)
	{
		return values[attributeIndex];
	}

	@Override
	public int size()
	{
		return values.length;
	}

	@Override
	public double getDouble(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getFloat(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLong(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getInt(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public short getShort(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte getByte(int attributeIndex)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Data setValue(int attributeIndex, double value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data setValue(int attributeIndex, float value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data setValue(int attributeIndex, long value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data setValue(int attributeIndex, int value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data setValue(int attributeIndex, short value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data setValue(int attributeIndex, byte value)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Data fill(Number fillValue)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void write(Writer output) throws IOException
	{
		for (int i = 0; i < values.length; ++i)
			values[i].write(output);
	}

	@Override
	public void write(DataOutputStream output) throws IOException
	{
		for (int i = 0; i < values.length; ++i)
			values[i].write(output);
	}
    @Override
    public String toString()
    {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < values.length; ++i)
			buf.append(values[i].toString());
		return buf.toString();
    }

	@Override
	public String toString(int attributeIndex)
	{
		return values[attributeIndex].toString();
	}

	@Override
	public Data copy()
	{
		Data[] newValues = new Data[values.length];
		for (int i = 0; i < values.length; ++i)
			newValues[i] = values[i].copy();
		return new DataArrayOfCustoms(newValues);
	}
}
