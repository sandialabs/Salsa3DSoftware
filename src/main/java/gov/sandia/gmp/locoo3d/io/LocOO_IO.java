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
package gov.sandia.gmp.locoo3d.io;

import java.util.Collection;
import java.util.Map;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.testingbuffer.TestBuffer;

/**
 * DataIO manages a NativeInput class and a NativeOutput class.
 * @author sballar
 *
 */
public class LocOO_IO {

	protected NativeInput dataInput;

	protected NativeOutput dataOutput;

	/**
	 * Constructs new NativeInput and NativeOutput classes based on properties specified in
	 * the properties file.  The following properties control the types of dataInput and
	 * dataOutput classes created:
	 * <ul>
	 * <li>dataLoaderInputType must be one of [ file, database, application ]
	 * <li>dataLoaderInputFormat must be one of [ kb, gmp, native ]
	 * <li>dataLoaderOutputType must be one of [ file, database, application ]
	 * <li>dataLoaderOutputFormat must be one of [ kb, gmp, native ]
	 * </ul>
	 * 
	 * @param properties
	 * @throws Exception
	 */
	public LocOO_IO(PropertiesPlusGMP properties) throws Exception {
		VectorGeo.setEarthShape(properties);	
		dataInput = createInput(properties);
		dataOutput = createOutput(properties, dataInput);
	}

	public LocOO_IO() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Factory method to return a concrete DataInput based on the properties
	 * file setting "dataLoaderType". Current valid types include "file", "database",
	 * and "application".  "oracle" can be specified in place of "database".
	 * 
	 * @param properties Input LocOO3D Properties object.
	 * @param errorlog 
	 * @param logger 
	 * @return The new concrete DataLoader.
	 * @throws Exception 
	 */
	static public NativeInput createInput(PropertiesPlusGMP properties) throws Exception
	{
		/**
		 * One of file, database, application
		 */
		String type = properties.getProperty("dataLoaderInputType", 
				properties.getProperty("dataLoaderType", "")).toLowerCase();

		if (type.equalsIgnoreCase("oracle")) type = "database";

		/**
		 * format is one of kb, gmp, native
		 */
		String format = properties.getProperty("dataLoaderInputFormat", "kb").toLowerCase();

		if (format.equals("native"))
			return new NativeInput(properties);

		if (format.equals("kb") && type.equals("file"))
			return new KBFileInput(properties);

		if (format.equals("kb") && type.equals("database"))
			return new KBDBInput(properties);

		if (format.equals("kb") && type.equals("application"))
			return new KBInput(properties);


		if (format.equals("gmp") && type.equals("file"))
			return new GMPFileInput(properties);

		if (format.equals("gmp") && type.equals("database"))
			return new GMPDBInput(properties);

		if (format.equals("gmp") && type.equals("application"))
			return new GMPInput(properties);


		// deal with legacy property definitions.

		if (type.equals("file")) {

			if (properties.containsKey("dataLoaderFileInputOrigins"))    
				return new KBFileInput(properties);

			if (properties.containsKey("dataLoaderFileInputSources"))    
				return new GMPFileInput(properties);

			throw new Exception("dataLoaderInputType = "+type+",\n"
					+ "but neither dataLoaderFileInputOrigins nor dataLoaderFileInputSources is specified.");

		}
		else if (type.equals("database")){

			if ((properties.containsKey("dbInputTableTypes") &&
					properties.getProperty("dbInputTableTypes").toLowerCase().contains("origin"))
					|| properties.containsKey("dbInputOriginTable"))
				return new KBDBInput(properties);

			if ((properties.containsKey("dbInputTableTypes") &&
					properties.getProperty("dbInputTableTypes").toLowerCase().contains("source"))
					|| properties.containsKey("dbInputSourceTable"))
				return new GMPDBInput(properties);

			throw new Exception("dataLoaderInputType = "+type+",\n"
					+ "but neither dbInputTableTypes nor dbInputOriginTable nor dbInputSourceTable is specified.");

		}
		else if (type.toUpperCase().equals("application")){

			String inputApplication = properties.getProperty("dataLoaderInputApplication", "?");

			if (inputApplication.equalsIgnoreCase("KB"))
				return new KBInput(properties);

			if (inputApplication.equalsIgnoreCase("GMP"))
				return new GMPInput(properties);

			return new NativeInput(properties);

		}

		throw new Exception("Must specify property dataLoaderInputType = file or database");
	}

	static public NativeOutput createOutput(PropertiesPlusGMP properties, NativeInput dataInput) throws Exception {
		/**
		 * One of file, database, application
		 */
		String type = properties.getProperty("dataLoaderOutputType", 
				properties.getProperty("dataLoaderType", "application")).toLowerCase();

		if (type.equalsIgnoreCase("oracle")) type = "database";

		/**
		 * format is one of kb, gmp, native
		 */
		String format = properties.getProperty("dataLoaderOutputFormat", "-").toLowerCase();

		if (format.equals("native"))
			return new NativeOutput(properties, dataInput);

		if (format.equals("kb") && type.equals("file"))
			return new KBFileOutput(properties, dataInput);

		if (format.equals("kb") && type.equals("database"))
			return new KBDBOutput(properties, dataInput);

		if (format.equals("kb") && type.equals("application"))
			return new KBOutput(properties, dataInput);


		if (format.equals("gmp") && type.equals("file"))
			return new GMPFileOutput(properties, dataInput);

		if (format.equals("gmp") && type.equals("database"))
			return new GMPDBOutput(properties, dataInput);

		if (format.equals("gmp") && type.equals("application"))
			return new GMPOutput(properties, dataInput);


		// deal with legacy property definitions.

		String dataTypeProperty = properties.getProperty("dataLoaderOutputType", 
				properties.getProperty("dataLoaderType", "application"));

		if (dataTypeProperty.toLowerCase().equals("file")) {

			if (properties.containsKey("dataLoaderFileOutputOrigins"))    
				return new KBFileOutput(properties, dataInput);

			if (properties.containsKey("dataLoaderFileOutputGMPSources"))    
				return new GMPFileOutput(properties, dataInput);

			throw new Exception("dataLoaderOutputType = "+dataTypeProperty+",\n"
					+ "but neither dataLoaderFileOutputOrigins nor dataLoaderFileOutputSources is specified.");

		}
		else if (dataTypeProperty.toLowerCase().equals("oracle") || 
				dataTypeProperty.toLowerCase().equals("database")){

			if ((properties.containsKey("dbOutputTableTypes") &&
					properties.getProperty("dbOutputTableTypes").toLowerCase().contains("origin"))
					|| properties.getProperty("dbOutputOriginTable") != null)
				return new KBDBOutput(properties, dataInput);

			if ((properties.containsKey("dbOutputTableTypes") &&
					properties.getProperty("dbOutputTableTypes").toLowerCase().contains("source"))
					|| properties.getProperty("dbOutputSourceTable") != null)
				return new GMPDBOutput(properties, dataInput);

			throw new Exception("dataLoaderOutputType = "+dataTypeProperty+" but dbOutputTableTypes is not specified.");

		}

		if (properties.containsKey("dataLoaderFileOutputOrigins"))    
			return new KBFileOutput(properties, dataInput);

		if (properties.containsKey("dataLoaderFileOutputGMPSources"))    
			return new GMPFileOutput(properties, dataInput);

		if (properties.containsKey("outputTableTypes") && properties.getProperty("outputTableTypes").contains("origin"))
			return new KBOutput(properties, dataInput);

		return new NativeOutput(properties, dataInput);
	}

	public NativeInput getDataInput() { return dataInput; }

	public NativeOutput getDataOutput() { return dataOutput; }

	public LocOO_IO setInputSources(Collection<Source> inputSources) 
	{ dataInput.setSources(inputSources); return this; }

	public LocOO_IO setInputSources(Collection<Source> inputSources, PropertiesPlusGMP changedProperties) 
	{ dataInput.setSources(inputSources, changedProperties); return this; }

	public Map<Long, Source> getOutputSources() { return dataOutput.getOutputSources(); }

	public ScreenWriterOutput getLogger() { return dataInput.getLogger(); }

	public ScreenWriterOutput getErrorlog() { return dataInput.getErrorlog(); }

	public TestBuffer getTestBuffer() throws Exception { return dataOutput.getTestBuffer(); }

	public void close() throws Exception {
		dataInput.close();
		if (dataOutput != null) 
			dataOutput.close();
	}

//	public static Buff getBuff(File f) throws Exception {
//		if (!f.exists()) return new Buff("LocOO_IO");
//		return NativeOutput.getBuff(new Scanner(f));
//	}

}
