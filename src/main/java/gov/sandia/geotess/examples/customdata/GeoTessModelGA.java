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
package gov.sandia.geotess.examples.customdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessUtils;

public class GeoTessModelGA extends GeoTessModel {

	/**
	 * A list of all the station names supported by this model
	 */
	protected String[] stationNames;

	/**
	 * A list of all the phase names supported by this model.
	 */
	private SeismicPhase[] phaseNames;
	
	/**
	 * A list of all the attributes supported by this model.
	 * All of these attributes will have values in each PhaseData map and will
	 * be read from, and written to the output files.
	 */
	private GeoAttributes[] attributes;
	
	/**
	 * Map from index -> [Station Name, Phase Name, Attribute Name]
	 * Includes every combination of station - phase - attribute.
	 * Size equals nStations * nPhases * nAttributes.
	 */
	private List<Triple> indexList;
	
	/**
	 * Map from station -> phase -> attribute -> attribute index
	 * Includes every combination of station - phase - attribute.
	 * Maximum index equals nStations * nPhases * nAttributes -1.
	 */
	private Map<String, Map<SeismicPhase,Map<GeoAttributes, Integer>>> indexMap;

    /**
     * Classes that extend GeoTessModel must override this method
     * and populate their 'extra' data with shallow copies from the model 
     * specified in the parameter list.  
     * @param other the other model from which to copy extra data
     * @throws Exception 
     */
	@Override
	public void copyDerivedClassData(GeoTessModel other) throws Exception
	{
		super.copyDerivedClassData(other);
		this.stationNames = ((GeoTessModelGA)other).stationNames;    
		this.phaseNames = ((GeoTessModelGA)other).phaseNames;
		this.attributes = ((GeoTessModelGA)other).attributes;
		this.indexList = ((GeoTessModelGA)other).indexList;
		this.indexMap = ((GeoTessModelGA)other).indexMap;
	}
    
	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param relativeGridPath
	 *            the relative path from the directory where the model is stored
	 *            to the directory where the grid is stored. Often, the model
	 *            and grid are stored together in the same file in which case
	 *            this parameter is ignored. Sometimes, however, the grid is
	 *            stored in a separate file and only the name of the grid file
	 *            (without path information) is stored in the model file. In
	 *            this case, the code needs to know which directory to search
	 *            for the grid file. The default is "" (empty string), which
	 *            will cause the code to search for the grid file in the same
	 *            directory in which the model file resides. Bottom line is that
	 *            the default value is appropriate when the grid is stored in
	 *            the same file as the model, or the model file is in the same
	 *            directory as the model file.
	 * @throws IOException
	 */
	public GeoTessModelGA(File modelInputFile, String relativeGridPath) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, relativeGridPath);
	}
	
	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
	 * 
	 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as the
	 * model file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @throws IOException
	 */
	public GeoTessModelGA(File modelInputFile) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, "");
	}
	
	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param relativeGridPath
	 *            the relative path from the directory where the model is stored
	 *            to the directory where the grid is stored. Often, the model
	 *            and grid are stored together in the same file in which case
	 *            this parameter is ignored. Sometimes, however, the grid is
	 *            stored in a separate file and only the name of the grid file
	 *            (without path information) is stored in the model file. In
	 *            this case, the code needs to know which directory to search
	 *            for the grid file. The default is "" (empty string), which
	 *            will cause the code to search for the grid file in the same
	 *            directory in which the model file resides. Bottom line is that
	 *            the default value is appropriate when the grid is stored in
	 *            the same file as the model, or the model file is in the same
	 *            directory as the model file.
	 * @throws IOException
	 */
	public GeoTessModelGA(String modelInputFile, String relativeGridPath) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, relativeGridPath);
	}
	
	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
	 * 
	 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as the
	 * model file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @throws IOException
	 */
	public GeoTessModelGA(String modelInputFile) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, "");
	}
	
	/**
	 * Parameterized constructor, specifying the grid and metadata for the
	 * model. The grid is constructed and the data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * </ul>
	 * 
	 * @param gridFileName
	 *            name of file from which to load the grid.
	 * @param metaData
	 *            MetaData the new GeoTessModel instantiates a reference to the
	 *            supplied metaData. No copy is made.
	 * @throws IOException
	 */
	public GeoTessModelGA(String gridFileName, GeoTessMetaData metaData) throws IOException
	{ 
		super(gridFileName, metaData); 
	}

	/**
	 * Parameterized constructor, specifying the grid and metadata for the
	 * model. The grid is constructed and the data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * <li>setSoftwareVersion()
	 * <li>setGenerationDate()
	 * </ul>
	 * 
	 * @param grid
	 *            a reference to the GeoTessGrid that will support this
	 *            GeoTessModel.
	 * @param metaData
	 *            MetaData the new GeoTessModel instantiates a reference to the
	 *            supplied metaData. No copy is made.
	 * @throws GeoTessException
	 *             if metadata is incomplete.
	 */
	public GeoTessModelGA(GeoTessGrid grid, GeoTessMetaData metaData) throws GeoTessException, IOException
	{ 
		super(grid, metaData); 
	}
	
	/**
	 * Construct a new GAModel with all the structures from the supplied
	 * baseModel.  The new GAModel will be built with references to the 
	 * GeoTessMetaData, GeoTessGrid and all the Profiles in the baseModel.  
	 * No copies are made. Changes to one will be reflected in the other.  
	 * All of the extraData will be set to default values.
	 * @param baseModel
	 * @throws GeoTessException
	 */
	public GeoTessModelGA(GeoTessModel baseModel) throws GeoTessException
	{
		super(baseModel.getGrid(), baseModel.getMetaData());
		for (int i = 0; i < baseModel.getNVertices(); ++i)
			for (int j=0; j<baseModel.getNLayers(); ++j)
				setProfile(i,j,baseModel.getProfile(i, j));
	}
	
	public String[] getStationNames() {
		return stationNames;
	}

	public SeismicPhase[] getPhaseNames() {
		return phaseNames;
	}

	public GeoAttributes[] getAttributes() {
		return attributes;
	}

	public List<Triple> getIndexList() {
		return indexList;
	}
	
	public int getAttributeIndex(String station, SeismicPhase phase, GeoAttributes attribute)
	{ return indexMap.get(station).get(phase).get(attribute); }

	/**
	 * Overridden IO method.
	 */
	@Override
	protected void loadModelBinary(DataInputStream input,
			String inputDirectory, String relGridFilePath)
			throws GeoTessException, IOException
	{
		// call super class to load model data from binary file.
		super.loadModelBinary(input, inputDirectory, relGridFilePath);
		
		// it is good practice, but not required, to store the class
		// name as the first thing added by the extending class.
		String className = GeoTessUtils.readString(input);
		if (!className.equals(this.getClass().getSimpleName()))
			throw new IOException("Found class name "+className
					+" but expecting "
		+this.getClass().getSimpleName());
		
		// it is good practice, but not required, to store a format 
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		int formatVersion = input.readInt();
		
		if (formatVersion != 1)
			throw new IOException("GAModel Format version "+formatVersion+" is not supported.");
		
		// Read in list of station names supported by this model
		int nStations = input.readInt();
		String[] stations = new String[nStations];
		for (int i=0; i<nStations; ++i)
			stations[i] = GeoTessUtils.readString(input);

		// Read in list of phase names supported by this model
		int nPhases = input.readInt();
		SeismicPhase[] phases = new SeismicPhase[nPhases];
		for (int i=0; i<nPhases; ++i)
			phases[i] = SeismicPhase.valueOf(GeoTessUtils.readString(input));

		// Read in list of GeoAttributes supported by this model.
		int nAttributes = input.readInt();
		GeoAttributes[] attributes = new GeoAttributes[nAttributes];
		for (int i=0; i<nAttributes; ++i)
			attributes[i] = GeoAttributes.valueOf(GeoTessUtils.readString(input));
		
		setStationsPhasesAttributes(stations, phases, attributes);
	}

	/**
	 * Overridden IO method.
	 */
	@Override
	protected void writeModelBinary(DataOutputStream output, String gridFileName)
			throws IOException
	{
		// call super class to write standard model information to binary file.
		super.writeModelBinary(output, gridFileName);

		// it is good practice, but not required, to store the class
		// name as the first thing added by the extending class.
		GeoTessUtils.writeString(output, this.getClass().getSimpleName());
		
		// it is good practice, but not required, to store a format 
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		output.writeInt(1);
		
		// now output the extraData
		output.writeInt(stationNames.length);
		for (String station : stationNames)
			GeoTessUtils.writeString(output, station);
		
		output.writeInt(phaseNames.length);
		for (SeismicPhase phase : phaseNames)
			GeoTessUtils.writeString(output, phase.toString());
		
		output.writeInt(attributes.length);
		for (GeoAttributes a : attributes)
			GeoTessUtils.writeString(output, a.toString());
	}

	/**
	 * Overridden IO method.
	 */
	@Override
	protected void loadModelAscii(Scanner input, String inputDirectory,
			String relGridFilePath) throws GeoTessException, IOException
	{
		super.loadModelAscii(input, inputDirectory, relGridFilePath);
		
		// it is good practice, but not required, to store the class
		// name as the first thing added by the extending class.
		String className = input.nextLine();
		if (!className.equals(this.getClass().getSimpleName()))
			throw new IOException("Found class name "+className
					+" but expecting "
		+this.getClass().getSimpleName());
		
		// it is good practice, but not required, to store a format 
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		int formatVersion = input.nextInt();
		input.nextLine();
		
		if (formatVersion != 1)
			throw new IOException("GAModel Format version "+formatVersion+" is not supported.");
		
		// Read in list of station names supported by this model
		int nStations = input.nextInt();
		String[] stations = new String[nStations];
		for (int i=0; i<nStations; ++i)
			stations[i] = input.next();

		// Read in list of phase names supported by this model
		int nPhases = input.nextInt();
		SeismicPhase[] phases = new SeismicPhase[nPhases];
		for (int i=0; i<nPhases; ++i)
			phases[i] = SeismicPhase.valueOf(input.next());

		// Read in list of GeoAttribute names supported by this model
		int nAttributes = input.nextInt();
		GeoAttributes[] attributes = new GeoAttributes[nAttributes];
		for (int i=0; i<nAttributes; ++i)
			attributes[i] = GeoAttributes.valueOf(input.next());
		
		setStationsPhasesAttributes(stations, phases, attributes);
	}
	
	public void setStationsPhasesAttributes(
			String[] stations, SeismicPhase[] phases, GeoAttributes[] attributes)
	{
		this.stationNames = stations;
		this.phaseNames = phases;
		this.attributes = attributes;
		
		indexList = new ArrayList<>(stationNames.length*phaseNames.length*attributes.length);
		indexMap = new HashMap<String, Map<SeismicPhase,Map<GeoAttributes,Integer>>>(stations.length);
		
		for (String s : stationNames)
		{
			Map<SeismicPhase, Map<GeoAttributes, Integer>> m2 = 
					new HashMap<SeismicPhase, Map<GeoAttributes,Integer>>(phases.length);
			indexMap.put(s, m2);
			for (SeismicPhase p : phaseNames)
			{
				Map<GeoAttributes, Integer> m3 = new HashMap<GeoAttributes, Integer>(attributes.length);
				m2.put(p, m3);
				for (GeoAttributes a : attributes)
				{
					m3.put(a, indexList.size());
					indexList.add(new Triple(s, p, a));
				}
			}
		}
	}

	/**
	 * Overridden IO method.
	 */
	@Override
	protected void writeModelAscii(Writer output, String gridFileName)
			throws IOException
	{
		// call super class to write standard model information to ascii file.
		super.writeModelAscii(output, gridFileName);

		// it is good practice, but not required, to store the class
		// name and a format version number as the first things added 
		// by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		output.write(String.format("%s%n%d%n", this.getClass().getSimpleName(), 1));
		
		// now output the extraData
		output.write(stationNames.length);
		for (String station : stationNames)
			output.write(' '+station);
		output.write("\n");
		
		output.write(phaseNames.length);
		for (SeismicPhase phase : phaseNames)
			output.write(' '+phase.toString());
		output.write("\n");
		
		output.write(attributes.length);
		for (GeoAttributes a : attributes)
			output.write(' '+a.toString());
		output.write("\n");
	}

}
