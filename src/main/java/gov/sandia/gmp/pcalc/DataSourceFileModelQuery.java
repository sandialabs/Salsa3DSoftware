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
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.exceptions.GMPException;

public class DataSourceFileModelQuery extends DataSourceFile
{
	private int latIndex=-1, lonIndex=-1, depthIndex=-1;

	public DataSourceFileModelQuery(PCalc pcalc) throws GMPException
	{
		super(pcalc);

        batchSize = properties.getInt("batchSize", 10000);

		try
		{
			// deal with inputAttributes

			String line;
			if (properties.getBoolean("inputHeaderRow", false))
			{
				// inputHeaderRow = true which means that input header information is to be 
				// read from the input file
				line = input.nextLine().trim();
				while (line.startsWith("#") || line.length()==0)
				{
					comments.add(line);
					line = input.nextLine().trim();
				}
				inputHeader = line;
				if (log.isOutputOn())
					log.write(String.format("inputAttributes read from first line of input file:%n%s%n%n", inputHeader));

				if (inputHeader.toLowerCase().indexOf("latitude") < 0)
					throw new GMPException(String.format(
							"%ninputHeader = %s%ndoes not contain required elements. Should property inputHeaderRow = false?", 
							inputHeader));
			}
			else
			{
				// inputHeaderRow = false which means that input header information is to be 
				// read from the property file
				line = properties.getProperty("inputAttributes", "longitude latitude depth");
				if (log.isOutputOn())
					log.write(String.format("inputAttributes read from property file:%n%s%n%n", line));
			}

			// extract the names of all the columns from the header row.
			Scanner scanner = new Scanner(line.replaceAll(",", " "));
			while (scanner.hasNext())
				bucket.inputAttributes.add(scanner.next().trim().toLowerCase());

			// build the input header from the list of column names
			if (inputHeader == null || inputHeader.length() == 0)
				setInputHeader();

			if (log.isOutputOn())
				log.writeln("parsed inputAttributes = "+inputHeader);

			// build a map from the name of an input attribute to the index of the attribute in each input record.
			for (int i=0; i<bucket.inputAttributes.size(); ++i)
				inputMap.put(bucket.inputAttributes.get(i).trim(), i);

			// ensure that the inputAttributes contains all the required elements.
			for (String s : new String[] {"latitude", "longitude"})
				if (!bucket.inputAttributes.contains(s))
					throw new GMPException(String.format("%ninputAttributes does not contain required attribute %s%n", s));

			latIndex = inputMap.get("latitude");
			lonIndex = inputMap.get("longitude");
			
			if (bucket.inputAttributes.contains("depth")) 
				depthIndex = inputMap.get("depth");
			else if (pcalc.inputType != IOType.DATABASE)
			{
				pcalc.extractDepthInfo(bucket);
				inputHeader += separator + "depth";
			}
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    @Override
    public boolean hasNext() {
      return input.hasNext();
    }

	@Override
	public Bucket next()
	{
		Bucket newBucket = new Bucket(bucket);
		newBucket.inputType = IOType.FILE;
		newBucket.points = new ArrayList<GeoVector>(batchSize);
		newBucket.records = new ArrayList<String>(batchSize);
		newBucket.recordMap = new ArrayListInt(batchSize); 
		
		if (depthIndex < 0)
			newBucket.receivers = bucket.receivers;

		GeoVector point;
		double lat, lon;

		String line;
		Scanner scanner;
		String[] columns = new String[bucket.inputAttributes.size()];
		int count = 0;
		while (input.hasNext() && ++count <= batchSize)
			try
		{
				line = input.nextLine();
				newBucket.records.add(line);

				scanner = new Scanner(line.trim().replaceAll(",", " "));

				for (int i=0; i<bucket.inputAttributes.size(); ++i)
					columns[i] = scanner.next().trim();
				
				lat = Double.parseDouble(columns[latIndex]); 
				lon = Double.parseDouble(columns[lonIndex]);
				
				if (Math.abs(lat) > 90.)
					throw new GMPException(String.format("\nLatitude %1.6f is out of range\n", lat));

				point = new GeoVector(lat, lon,
						depthIndex < 0 ? Double.NaN : Double.parseDouble(columns[depthIndex]),
						true);

				newBucket.points.add(point);

				newBucket.recordMap.add(newBucket.points.size()-1);

		} 
		catch (Exception ex)
		{
			if (ex.getMessage().contains("is out of range"))
				log.writeln(ex);
			newBucket.recordMap.add(-1);
		}

		if (log.isOutputOn())
			log.writeln(String.format("DataSourceFileModelQuery.next() produced %d valid records and %d invalid records.", 
					newBucket.points.size(), newBucket.records.size()-newBucket.points.size()));

		moreData = input.hasNext();

		return newBucket;
	}

}
