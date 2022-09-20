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
package gov.sandia.gmp.util.globals;

import java.util.EnumSet;

/**
 * An Enum of DOUBLE, FLOAT, LONG, INT, SHORT and BYTE
 * 
 * @author sballar
 * 
 */
public enum DataType
{
	/**
	 * A single value of type DOUBLE
	 */
	DOUBLE(8),

	/**
	 * A single value of type FLOAT
	 */
	FLOAT(4),

	/**
	 * A single value of type LONG
	 */
	LONG(8),

	/**
	 * A single value of type INT
	 */
	INT(4),

	/**
	 * A single value of type SHORT
	 */
	SHORT(2),

	/**
	 * A single value of type BYTE
	 */
	BYTE(1),

	/**
	 * A custom object of arbitrary size and configuration
	 */
	CUSTOM(-1);
	
	public static final EnumSet<DataType> floatingPointTypes = EnumSet.of(DataType.FLOAT, DataType.DOUBLE);
	
	public static final EnumSet<DataType> integerTypes = EnumSet.of(DataType.LONG, DataType.INT, 
			DataType.SHORT, DataType.BYTE);

	/**
	 * Number of bytes required to store a value 
	 * of the corresponding type.
	 */
	public int nbytes;
	
	DataType(int nbytes)
	{ this.nbytes = nbytes; }

	/**
	 * Returns the DataType of the input number or throws an exception if it is not
	 * one of the supported types.
	 * @param val The number whose DataType will be returned.
	 * @return The DataType of the input number val.
	 */
	public static DataType getDataType(Number val)
	{
		// return data type

		if (val.getClass() == Double.class)
			return DataType.DOUBLE;
		else if (val.getClass() == Float.class)
			return DataType.FLOAT;
		else if (val.getClass() == Long.class)
			return DataType.LONG;
		else if (val.getClass() == Integer.class)
			return DataType.INT;
		else if (val.getClass() == Short.class)
			return DataType.SHORT;
		else if (val.getClass() == Byte.class)
			return DataType.BYTE;
		else
			throw new IllegalArgumentException("\nthe type of fillValue ("
					+ val.getClass().getSimpleName()
					+ ") is not one of the data types \n"
					+ "double, float, long, int, short, byte\n");
	}
}
