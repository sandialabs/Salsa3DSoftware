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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import gov.sandia.gmp.util.exceptions.GMPException;

public abstract class DataSourceFile extends DataSource
{
	protected Scanner input;

	/**
	 * Map from recognized column names like sta_lat, etc., to the index
	 * of the column that contains that element.  For example, if 
	 * sta_lat is the third value read in a record of data, then
	 * map entry is from "sta_lat" -> 2.
	 */
	protected Map<String, Integer> inputMap = new HashMap<String, Integer>();

	public DataSourceFile(PCalc pcalc) throws GMPException
	{
		super(pcalc);

		bucket.inputType = IOType.FILE;

		try
		{
			String inputFileName = properties.getProperty("inputFile");
			if (inputFileName == null)
			{
				throw new GMPException("Property 'inputFile' is not specified.");
			}
			else if (inputFileName.trim().toLowerCase().equals("stdin"))
			{
				input = new Scanner(System.in);
				if (log.isOutputOn())
				log.writeln("Reading input data from stdin");
			}
			else
			{
				File inputFile = properties.getFile("inputFile");

				if (log.isOutputOn())
					log.write(String.format("Input file %s%n", inputFile.getCanonicalPath()));

				input = new Scanner(inputFile);
			}

		} 
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void close()
	{
		input.close();
	}

}
