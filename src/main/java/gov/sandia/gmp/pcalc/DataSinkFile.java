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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayPath;
//import gov.sandia.gmp.pcalc.PCalc.GraphicsFormat;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

public class DataSinkFile extends DataSink
{
	private File outputFile;

	private BufferedWriter output;

	/**
	 * The character(s) that will be output in between the 
	 * requested output values.  Space, comma or tab.
	 */
	protected String separator;

	protected String outputFormat;


	public DataSinkFile(PCalc pcalc) 
			throws IOException, GMPException
	{
		super(pcalc);

		// Set up the output device.
		outputFile = properties.getFile("outputFile");

		if (outputFile == null)
			throw new GMPException("Properties file does not contain property 'outputFile'");

		if (log.isOutputOn())
			log.write(String.format("Sending output to %s%n", outputFile.getCanonicalPath()));

		output = new BufferedWriter(new FileWriter(outputFile));

		for (String comment : pcalc.dataSource.getComments())
		{
			output.write(comment);
			output.newLine();
		}

		separator = properties.getProperty("separator", "space");
		if (separator.equals("tab"))
			separator = new String(new byte[] {9});
		else if (separator.equals("comma"))
			separator = ",";
		else
			separator = " ";

		outputFormat = "%s"+properties.getProperty("outputFormat", "%1.4f");

		if (properties.getBoolean("outputHeader", properties.getBoolean("inputHeaderRow", false)))
		{
			output.write(getOutputHeader());
			output.newLine();
		}
		output.flush();
	}

	@Override
	public void close() throws IOException
	{
		if (output != null)
			output.close();
	}

	public String getOutputHeader() throws PropertiesPlusException, IOException
	{
		if (outputAttributes.contains(GeoAttributes.RAY_PATH))
			return properties.getProperty("gcPositionParameters", "");

		StringBuffer outputHeader = new StringBuffer();
		outputHeader.append(inputHeader);
		for (GeoAttributes attribute : outputAttributes)
			if (attribute != GeoAttributes.RAY_PATH)
				outputHeader.append(separator).append(attribute.toString().toLowerCase());
		return outputHeader.toString();
	}

	@Override
	public void writeData(Bucket bucket) throws IOException, GMPException, Exception
	{
		if (bucket.inputType ==  IOType.DATABASE)
			throw new GMPException("InputType == DATABASE and application == MODEL_QUERY is invalid.");
		if (bucket.inputType == null)
			throw new GMPException("data.inputType == null");

		if (bucket.rayPaths == null)
		{
			if (bucket.inputType ==  IOType.FILE)
			{
				int k0 = bucket.inputAttributes.contains("depth") || bucket.inputAttributes.contains("origin_depth") ? 1 : 0;
				for (int i=0; i<bucket.recordMap.size(); ++i)
					if (bucket.recordMap.get(i) >= 0)					
					{
						int record = bucket.recordMap.get(i);

						output.write(bucket.records.get(i));
						for (int k=k0; k<bucket.modelValues[record].length; ++k)
							if (k > 0 && outputAttributes.get(k-1) == GeoAttributes.RAY_TYPE)
								output.write(separator+" "+bucket.rayTypes[record].toString());
							else
								output.write(String.format(outputFormat, separator, bucket.modelValues[record][k]));
						output.newLine();
					}
			}
			else if (bucket.inputType ==  IOType.GREATCIRCLE || bucket.inputType ==  IOType.GRID)
			{
				for (int n=0; n<bucket.points.size(); ++n)
				{
					GeoVector point = bucket.points.get(n);

					double distance = Globals.NA_VALUE;
					double latitude = Globals.NA_VALUE;
					double longitude = Globals.NA_VALUE;
					double[] v = null;

					for (GeoAttributes attribute : bucket.positionParameters)
					{
						switch (attribute)
						{
						case X:
							if (v == null)
								v = bucket.greatCircle.transform(point.getUnitVector());
							output.write(String.format("%1.9f%s", v[0], separator)); 
							break;
						case Y:
							if (v == null)
								v = bucket.greatCircle.transform(point.getUnitVector());
							output.write(String.format("%1.9f%s", v[1], separator)); 
							break;
						case Z:
							if (v == null)
								v = bucket.greatCircle.transform(point.getUnitVector());
							output.write(String.format("%1.9f%s", v[2], separator)); 
							break;
						case LATITUDE:
							if (latitude == Globals.NA_VALUE)
								latitude = point.getLatDegrees();
							output.write(String.format("%1.9f%s", latitude, separator)); 
							break;
						case LONGITUDE:
							if (longitude == Globals.NA_VALUE)
								longitude = point.getLonDegrees();
							output.write(String.format("%1.9f%s", longitude, separator)); 
							break;
						case DISTANCE:
							if (distance < 0.)
								distance = Math.toDegrees(bucket.greatCircle.getDistance(point.getUnitVector()));
							output.write(String.format("%1.9f%s", distance, separator)); 
							break;
						case RADIUS:
							output.write(String.format("%1.6f%s", point.getRadius(), separator)); 
							break;
						case DEPTH:
							output.write(String.format("%1.6f%s", point.getDepth(), separator)); 
							break;
						default:
							break;
						}
					}

					for (int k=1; k<bucket.modelValues[n].length; ++k)	
						if (outputAttributes.get(k-1) == GeoAttributes.RAY_TYPE)
							output.write(separator+" "+bucket.rayTypes[n].toString());
						else
							output.write(String.format(outputFormat, 
									k == 1 ? "" : separator, bucket.modelValues[n][k]));
					output.newLine();
				}
			}
		}
		else  // bucket.rayPaths != null
		{
		    if (outputFile.toString().toLowerCase().endsWith("vtk") && bucket.greatCircle != null)
		    {
			ArrayList<RayPath> rays = new ArrayList<>();
			    for (ArrayList<GeoVector> l2 : bucket.rayPaths)
				rays.add(new RayPath(l2));
			if (bucket.positionParameters.contains(GeoAttributes.X))
			    RayPath.toVTKSlice(rays, bucket.greatCircle, outputFile);
			else if (bucket.positionParameters.contains(GeoAttributes.DISTANCE))
			    RayPath.toVTKSlice(rays, outputFile);
			
		    }
		    else
		    {
			double[] v = null;
			//int zone = 1;
			GreatCircle greatCircle;
			ArrayListDouble geometry = new ArrayListDouble(bucket.positionParameters.size());
			for (int i=0; i<bucket.rayPaths.size(); ++i)
				{
					ArrayList<GeoVector> ray = bucket.rayPaths.get(i);
					if (ray == null)
					{
						//++zone;
						continue;
					}

					greatCircle = bucket.greatCircle;
					if (greatCircle == null)
						greatCircle = new GreatCircle(ray.get(0).getUnitVector(), ray.get(ray.size()-1).getUnitVector());

					output.write(String.format(">%n"));
					for (int p=0; p<ray.size(); ++p)
					{
						v = null;
						geometry.clear();
						for (GeoAttributes attribute : bucket.positionParameters)
						{
							switch (attribute)
							{
							case X:
								if (v == null)
									v = greatCircle.transform(ray.get(p).getVector());
								geometry.add(v[0]);
								break;
							case Y:
								if (v == null)
									v = greatCircle.transform(ray.get(p).getVector());
								geometry.add(v[1]);
								break;
							case Z:
								if (v == null)
									v = greatCircle.transform(ray.get(p).getVector());
								geometry.add(v[2]);
								break;
							case LATITUDE:
								geometry.add(ray.get(p).getLatDegrees());
								break;
							case LONGITUDE:
								geometry.add(ray.get(p).getLonDegrees());
								break;
							case DISTANCE:
							    geometry.add(Math.toDegrees(greatCircle.getDistance(ray.get(p).getUnitVector()))); 
							  break;
							case DEPTH:
								geometry.add(ray.get(p).getDepth());
								break;
							default:
								break;
							}
						}
						for (int k=0; k<geometry.size(); ++k)
							output.write(String.format("%1.6f%s", geometry.get(k), 
									k < geometry.size()-1 ? separator : Globals.NL)); 
					}
				}
		    }
		}
		output.flush();
	}

}
