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
package gov.sandia.geotess.extensions.rstt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessUtils;

public class GeoTessModelSLBMPDU extends GeoTessModel {

	public static String getVersion() {
		return "1.0.0";
	}

	/**
	 * Distance bins (km)
	 */
	private float[] distanceBins;

	/**
	 * Phase supported by this model.
	 */
	private String phase;

	private Map<String, String> properties;

	/**
	 * This is the index into the geotess data array that contains crustalError
	 * value. This value is deduced from the geotess attribute names in
	 * loadModelAscii and loadModelBinary
	 */
	private int crustalErrorIndex;

	/**
	 * This is the index into the geotess data array that contains first modelError
	 * value. This value is deduced from the geotess attribute names in
	 * loadModelAscii and loadModelBinary
	 */
	private int modelErrorIndex;

	/**
	 * This is the index into the geotess data array that contains first bias value.
	 * This value is deduced from the geotess attribute names in loadModelAscii and
	 * loadModelBinary
	 */
	private int biasIndex;

	/**
	 * This is the index into the geotess data array that contains first randomError
	 * value. This value is deduced from the geotess attribute names in
	 * loadModelAscii and loadModelBinary. If this value is -1 it means that
	 * randomError was not stored in the model.
	 */
	private int randomErrorIndex;

	/**
	 * Classes that extend GeoTessModel must override this method
	 * and populate their 'extra' data with shallow copies from the model
	 * specified in the parameter list.
	 * @param other the other model from which to copy extra data
	 * @throws Exception
	 */
	@Override
	public void copyDerivedClassData(GeoTessModel other) throws Exception {
		super.copyDerivedClassData(other);
		this.distanceBins = ((GeoTessModelSLBMPDU) other).distanceBins;
		this.phase = ((GeoTessModelSLBMPDU) other).phase;
		this.properties = ((GeoTessModelSLBMPDU) other).properties;
		this.crustalErrorIndex = ((GeoTessModelSLBMPDU) other).crustalErrorIndex;
		this.modelErrorIndex = ((GeoTessModelSLBMPDU) other).modelErrorIndex;
		this.biasIndex = ((GeoTessModelSLBMPDU) other).biasIndex;
		this.randomErrorIndex = ((GeoTessModelSLBMPDU) other).randomErrorIndex;
	}

	public GeoTessModelSLBMPDU setDistanceBins(float[] fs) {
		this.distanceBins = fs;
		return this;
	}

	public GeoTessModelSLBMPDU setProperties(Map<String, String> properties)
	{
		this.properties = properties;
		return this;
	}

	public Map<String, String> getProperties()
	{
		return properties;
	}

	public GeoTessModelSLBMPDU() {
		super();
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from the
	 * specified file.
	 *
	 * @param modelInputFile   name of file containing the model.
	 * @param relativeGridPath the relative path from the directory where the model
	 *                         is stored to the directory where the grid is stored.
	 *                         Often, the model and grid are stored together in the
	 *                         same file in which case this parameter is ignored.
	 *                         Sometimes, however, the grid is stored in a separate
	 *                         file and only the name of the grid file (without path
	 *                         information) is stored in the model file. In this
	 *                         case, the code needs to know which directory to
	 *                         search for the grid file. The default is "" (empty
	 *                         string), which will cause the code to search for the
	 *                         grid file in the same directory in which the model
	 *                         file resides. Bottom line is that the default value
	 *                         is appropriate when the grid is stored in the same
	 *                         file as the model, or the model file is in the same
	 *                         directory as the model file.
	 * @throws IOException
	 */
	public GeoTessModelSLBMPDU(File modelInputFile, String relativeGridPath) throws IOException {
		this();
		loadModel(modelInputFile, relativeGridPath);
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from the
	 * specified file.
	 *
	 * <p>
	 * relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when the
	 * grid is stored in a separate file located in the same directory as the model
	 * file.
	 *
	 * @param modelInputFile name of file containing the model.
	 * @throws IOException
	 */
	public GeoTessModelSLBMPDU(File modelInputFile) throws IOException {
		this();
		loadModel(modelInputFile, "");
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from the
	 * specified file.
	 *
	 * @param modelInputFile   name of file containing the model.
	 * @param relativeGridPath the relative path from the directory where the model
	 *                         is stored to the directory where the grid is stored.
	 *                         Often, the model and grid are stored together in the
	 *                         same file in which case this parameter is ignored.
	 *                         Sometimes, however, the grid is stored in a separate
	 *                         file and only the name of the grid file (without path
	 *                         information) is stored in the model file. In this
	 *                         case, the code needs to know which directory to
	 *                         search for the grid file. The default is "" (empty
	 *                         string), which will cause the code to search for the
	 *                         grid file in the same directory in which the model
	 *                         file resides. Bottom line is that the default value
	 *                         is appropriate when the grid is stored in the same
	 *                         file as the model, or the model file is in the same
	 *                         directory as the model file.
	 * @throws IOException
	 */
	public GeoTessModelSLBMPDU(String modelInputFile, String relativeGridPath) throws IOException {
		this();
		loadModel(modelInputFile, relativeGridPath);
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from the
	 * specified file.
	 *
	 * <p>
	 * relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when the
	 * grid is stored in a separate file located in the same directory as the model
	 * file.
	 *
	 * @param modelInputFile name of file containing the model.
	 * @throws IOException
	 */
	public GeoTessModelSLBMPDU(String modelInputFile) throws IOException {
		this();
		loadModel(modelInputFile, "");
	}

	/**
	 * Construct a new GeoTessModelSLBMPDU object and populate it with information
	 * from the specified DataInputStream. The GeoTessGrid will be read directly
	 * from the inputStream as well.
	 *
	 * @param inputStream
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public GeoTessModelSLBMPDU(DataInputStream inputStream) throws GeoTessException, IOException {
		this();
		loadModelBinary(inputStream, null, "*");
	}

	/**
	 * Construct a new GeoTessModelSLBMPDU object and populate it with information
	 * from the specified Scanner. The GeoTessGrid will be read directly from the
	 * inputScanner as well.
	 *
	 * @param inputScanner
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public GeoTessModelSLBMPDU(Scanner inputScanner) throws GeoTessException, IOException {
		this();
		loadModelAscii(inputScanner, null, "*");
	}

	/**
	 * Parameterized constructor, specifying the grid and metadata for the model.
	 * The grid is constructed and the data structures are initialized based on
	 * information supplied in metadata. The data structures are not populated with
	 * any information however (all Profiles are null). The application should
	 * populate the new model's Profiles after this constructor completes.
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
	 * <li>setLayerTessIds() (only required if grid has more than one multi-level
	 * tessellation)
	 * </ul>
	 *
	 * @param gridFileName name of file from which to load the grid.
	 * @param metaData     MetaData the new GeoTessModel instantiates a reference to
	 *                     the supplied metaData. No copy is made.
	 * @throws IOException
	 */
	public GeoTessModelSLBMPDU(String gridFileName, GeoTessMetaData metaData) throws IOException {
		super(gridFileName, metaData);
	}

	/**
	 * Parameterized constructor, specifying the grid and metadata for the model.
	 * The grid is constructed and the data structures are initialized based on
	 * information supplied in metadata. The data structures are not populated with
	 * any information however (all Profiles are null). The application should
	 * populate the new model's Profiles after this constructor completes.
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
	 * <li>setLayerTessIds() (only required if grid has more than one multi-level
	 * tessellation)
	 * <li>setSoftwareVersion()
	 * <li>setGenerationDate()
	 * </ul>
	 *
	 * @param grid     a reference to the GeoTessGrid that will support this
	 *                 GeoTessModel.
	 * @param metaData MetaData the new GeoTessModel instantiates a reference to the
	 *                 supplied metaData. No copy is made.
	 * @throws GeoTessException if metadata is incomplete.
	 */
	public GeoTessModelSLBMPDU(GeoTessGrid grid, GeoTessMetaData metaData) throws GeoTessException, IOException {
		super(grid, metaData);
	}

	/**
	 * Construct a new GeoTessModelSLBMPDU with all the structures from the supplied
	 * baseModel. The new GeoTessModelSLBMPDU will be built with references to the
	 * GeoTessMetaData, GeoTessGrid and all the Profiles in the baseModel. No copies
	 * are made. Changes to one will be reflected in the other. All of the extraData
	 * will be set to default values.
	 *
	 * @param baseModel
	 * @throws GeoTessException
	 */
	public GeoTessModelSLBMPDU(GeoTessModel baseModel) throws GeoTessException {
		super(baseModel.getGrid(), baseModel.getMetaData());
		for (int i = 0; i < baseModel.getNVertices(); ++i)
			for (int j = 0; j < baseModel.getNLayers(); ++j)
				setProfile(i, j, baseModel.getProfile(i, j));
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public float[] getDistanceBins() {
		return distanceBins;
	}

	public double getCrustalError(int vertex) {
		return getValueDouble(vertex, crustalErrorIndex);
	}

	public void setCrustalError(int vertex, double value) {
		setValue(vertex, crustalErrorIndex, value);
	}

	public double getModelError(int distanceBin, int vertex) {
		return getValueDouble(vertex, modelErrorIndex + distanceBin);
	}

	public void setModelError(int distanceBin, int vertex, double value) {
		setValue(vertex, modelErrorIndex + distanceBin, value);
	}

	public double getBias(int distanceBin, int vertex) {
		return getValueDouble(vertex, biasIndex + distanceBin);
	}

	public void setBias(int distanceBin, int vertex, double value) {
		setValue(vertex, biasIndex + distanceBin, value);
	}

	public double getRandomError(int distanceBin, int vertex) {
		if (randomErrorIndex >= 0)
			return getValueDouble(vertex, randomErrorIndex + distanceBin);
		return Double.NaN;
	}

	public void setRandomError(int distanceBin, int vertex, double value) {
		if (randomErrorIndex >= 0)
			setValue(vertex, randomErrorIndex + distanceBin, value);
	}

	@Override
	protected void loadModelAscii(Scanner input, String inputDirectory, String relGridFilePath)
			throws GeoTessException, IOException {
		// call super class to load standard GeoTessModel information from ascii file.
		super.loadModelAscii(input, inputDirectory, relGridFilePath);

		// load GeoTessModelPathUnc specific data

		// it is good practice, but not required, to store the class
		// name as the first thing added by the extending class.
		String className = input.nextLine();
		if (!className.equals(this.getClass().getSimpleName()))
			throw new IOException(
					"Found class name " + className + " but expecting " + this.getClass().getSimpleName());

		// it is good practice, but not required, to store a format
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		int formatVersion = input.nextInt(); input.nextLine();

		if (formatVersion != 1)
			throw new IOException(
					this.getClass().getSimpleName() + " Format version " + formatVersion + " is not supported.");

		properties = new LinkedHashMap<>();
		String line = input.nextLine().trim();
		while (line.length() > 0)
		{
			int p=line.indexOf("=");
			if (p > 0)
				properties.put(line.substring(0,p).trim(),
						line.substring(p+1, line.length()).trim().replaceAll("<NEWLINE>", "\n"));
			line = input.nextLine().trim();
		}

		phase = properties.get("phase");

		while (!line.equals("# Distance Bins"))
			line = input.nextLine();

		line = input.nextLine();
		String[] tokens = line.trim().replaceAll(",", " ").trim().split("\\s+");
		this.distanceBins = new float[tokens.length];
		for (int i = 0; i < tokens.length; ++i)
			this.distanceBins[i] = Float.parseFloat(tokens[i]);

		crustalErrorIndex = getMetaData().getAttributeIndex("CrustalError");
		modelErrorIndex = getMetaData().getAttributeIndex("ModelError[0]");
		biasIndex = getMetaData().getAttributeIndex("Bias[0]");
		randomErrorIndex = getMetaData().getAttributeIndex("RandomError[0]");
	}

	@Override
	protected void loadModelBinary(DataInputStream input, String inputDirectory, String relGridFilePath)
			throws GeoTessException, IOException {
		// call super class to load standard GeoTessModel information from binary file.
		super.loadModelBinary(input, inputDirectory, relGridFilePath);

		// it is good practice, but not required, to store the class
		// name as the first thing added by the extending class.
		String className = GeoTessUtils.readString(input);
		if (!className.equals(this.getClass().getSimpleName()))
			throw new IOException(
					"Found class name " + className + " but expecting " + this.getClass().getSimpleName());

		// it is good practice, but not required, to store a format
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		int formatVersion = input.readInt();

		if (formatVersion != 1)
			throw new IOException(
					this.getClass().getSimpleName() + " Format version " + formatVersion + " is not supported.");

		int nProperties = input.readInt();
		properties = new LinkedHashMap<>(nProperties);
		for (int i=0; i<nProperties; ++i)
			properties.put(GeoTessUtils.readString(input),
					GeoTessUtils.readString(input).replaceAll("<NEWLINE>", "\n"));

		phase = properties.get("phase");

		int nBins = input.readInt();
		this.distanceBins = new float[nBins];
		for (int i = 0; i < nBins; ++i)
			this.distanceBins[i] = input.readFloat();

		crustalErrorIndex = getMetaData().getAttributeIndex("CrustalError");
		modelErrorIndex = getMetaData().getAttributeIndex("ModelError[0]");
		biasIndex = getMetaData().getAttributeIndex("Bias[0]");
		randomErrorIndex = getMetaData().getAttributeIndex("RandomError[0]");

	}

	@Override
	protected void writeModelAscii(Writer output, String gridFileName) throws IOException {
		// call super class to write standard GeoTessModel information to ascii file.
		super.writeModelAscii(output, gridFileName);

		// it is good practice, but not required, to store the class
		// name and a format version number as the first things added
		// by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		output.write(this.getClass().getSimpleName() + GeoTessUtils.NL);
		output.write(String.format("%d%n", 1));

		//output.write(String.format("# Properties:%n%d%n", properties.size()));
		for (Entry<String, String> entry : properties.entrySet())
			output.write(String.format("%s = %s%n", entry.getKey(),
					entry.getValue().replaceAll("\n", "<NEWLINE>")));

		output.write("\n");

		output.write(String.format("# Distance Bins%n%s%n",
				Arrays.toString(distanceBins).replace("[", "").replace("]", "")));
	}

	@Override
	protected void writeModelBinary(DataOutputStream output, String gridFileName) throws IOException {
		// call super class to write standard model information to binary file.
		super.writeModelBinary(output, gridFileName);

		// derived classes must store the class
		// name as the first thing added by the extending class.
		GeoTessUtils.writeString(output, this.getClass().getSimpleName());

		// it is good practice, but not required, to store a format
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		output.writeInt(1);

		output.writeInt(properties.size());
		for (Entry<String, String> entry : properties.entrySet())
		{
			GeoTessUtils.writeString(output, entry.getKey());
			GeoTessUtils.writeString(output, entry.getValue().replaceAll("\n", "<NEWLINE>"));
		}

		output.writeInt(distanceBins.length);
		for (int i = 0; i < distanceBins.length; ++i)
			output.writeFloat(distanceBins[i]);
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer(super.toString());

		buf.append(String.format("%n%s data:%n%n", this.getClass().getSimpleName()));
		for (Entry<String, String> entry : properties.entrySet())
			buf.append(String.format("%s = %s%n", entry.getKey(), entry.getValue()));
		buf.append("\n");
		buf.append("Distance bins = " + Arrays.toString(distanceBins) + "\n");

		return buf.toString();
	}

}
