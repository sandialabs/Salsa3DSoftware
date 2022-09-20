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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import gov.sandia.geotess.Data;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessUtils;
import gov.sandia.geotess.Profile;
import gov.sandia.geotess.ProfileSurface;
import gov.sandia.gmp.util.globals.DataType;

/**
 *
 * @author jrhipp, sballar
 *
 */
public class UncertaintyPDU extends Uncertainty {

	/**
	 * Required properties are: phase, gridId, nDistanceBins, nVertices, includeRandomError
	 * Other properties are allowed.
	 */
	private Map<String, String> properties;

	private String gridId;

	private GeoTessGrid grid;

	/**
	 * Distance bins (km) ... pathUncDistanceBins [nDist].
	 */
	private float[] pathUncDistanceBins;

	/**
	 * Crustal travel time error (sec) defined for each grid vertex [vertex].
	 */
	private float[] pathUncCrustError;

	/**
	 * Travel time random error (sec) defined for each distance bin at each grid
	 * vertex [dist][vertex] (optional parameter).
	 */
	private float[][] pathUncRandomError;

	/**
	 * Travel time model error (sec) defined for each distance bin at each grid
	 * vertex [dist][vertex].
	 */
	private float[][] pathUncModelError;

	/**
	 * Travel time model error (sec) defined for each distance bin at each grid
	 * vertex [dist][vertex].
	 */
	private float[][] pathUncBias;

	private int nDistanceBins;

	private int nVertices;

	private boolean includeRandomError;

	/**
	 * used to size input and output buffers for speed.
	 */
	private static final int bufferSize = 1000 * 8192;

	public static UncertaintyPDU getUncertainty(Scanner input, int phase) throws IOException {
		UncertaintyPDU uncertainty = new UncertaintyPDU(phase);

		uncertainty.readFileAscii(input);

		if (uncertainty.getDistanceBins().length == 0)
			return null;

		return uncertainty;
	}

	public static UncertaintyPDU getUncertainty(DataInputStream input) throws IOException {
		UncertaintyPDU uncertainty = new UncertaintyPDU();

		uncertainty.readFileBinary(input);

		if (uncertainty.getDistanceBins().length == 0)
			return null;

		return uncertainty;
	}

	public static UncertaintyPDU getUncertainty(GeoTessModelSLBMPDU input) throws Exception {
		UncertaintyPDU uncertainty = new UncertaintyPDU(input);

		if (uncertainty.getDistanceBins().length == 0)
			return null;

		return uncertainty;
	}

	/**
	 * Default constructor
	 */
	public UncertaintyPDU() {
		super();
		properties = new LinkedHashMap<String, String>();
	}

	/**
	 * Standard constructor that sets the phase number but nothing else.
	 *
	 * @param phase The input phase number.
	 */
	public UncertaintyPDU(int phase) {
		super(phase);
		properties = new LinkedHashMap<String, String>();
	}

	/**
	 * Standard constructor that sets the phase number but nothing else.
	 *
	 * @param phase The input phase string.
	 */
	public UncertaintyPDU(String phase) {
		this(getPhase(phase));
	}

	/**
	 * Standard constructor that reads the objects contents from input file path.
	 *
	 * @param uncertaintyPath The file from which the object is read.
	 * @param phase           The objects phase string.
	 * @param readBinary      Reads binary format if true (else ascii format is
	 *                        read).
	 * @throws Exception
	 */
	public UncertaintyPDU(File uncertaintyPath, String phase, boolean readBinary) throws Exception {
		this(uncertaintyPath, getPhase(phase), readBinary);
		properties = new LinkedHashMap<String, String>();
	}

	/**
	 * Standard constructor that reads the objects contents from input file path.
	 *
	 * @param uncertaintyPath The file from which the object is read.
	 * @param phase           The objects phase number.
	 * @param readBinary      Reads binary format if true (else ascii format is
	 *                        read).
	 * @throws Exception
	 */
	public UncertaintyPDU(File uncertaintyPath, int phase, boolean readBinary) throws Exception {
		this(phase);
		readFile(uncertaintyPath, readBinary);
	}

	/**
	 * Standard constructor that reads the objects contents from a
	 * GeoTessModelSLBMPDU.
	 *
	 * @param model
	 * @throws Exception
	 */
	public UncertaintyPDU(GeoTessModelSLBMPDU model) throws Exception {
		this();
		readModel(model);
	}

	/**
	 * Try to read the specified File as a binary file, ascii file and
	 * GeoTessModelSLBM file.
	 *
	 * @param inputFile
	 * @throws IOException
	 */
	public UncertaintyPDU(File inputFile) throws IOException {
		this();
		readFile(inputFile);
//		if (!inputFile.exists())
//			throw new FileNotFoundException("File " + inputFile.getCanonicalPath() + " not found.");
//
//		fileName = inputFile.getCanonicalPath();
//
//		boolean failed = true;
//
//		if (failed) {
//			// try to read binary file.
//			DataInputStream dis = null;
//			try {
//				dis = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile), bufferSize));
//				readFileBinary(dis);
//				failed = false;
//			} catch (Exception ex) {
//				// ex.printStackTrace();
//			} finally {
//				if (dis != null)
//					dis.close();
//			}
//		}
//
//		if (failed) {
//			// try to read ascii file.
//			BufferedReader br = null;
//			try {
//				br = new BufferedReader(new FileReader(inputFile), bufferSize);
//				readFileAscii(br);
//				failed = false;
//			} catch (Exception ex) {
//				// ex.printStackTrace();
//			} finally {
//				if (br != null)
//					br.close();
//			}
//		}
//
//		if (failed) {
//			// try to read GeoTessModelSLBM file.
//			try {
//				GeoTessModelSLBMPDU gt = new GeoTessModelSLBMPDU(inputFile);
//				readModel(gt);
//				failed = false;
//			} catch (Exception ex) {
//				// ex.printStackTrace();
//			}
//		}
//
//		if (failed)
//			throw new IOException(String.format("Unable to read file %s", inputFile.getAbsolutePath()));
//
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setGridId(String gridId) {
		this.gridId = gridId;
	}

	public String getGridId() {
		return gridId;
	}

	public int getNVertices() {
		return nVertices;
	}

	public float[] getDistanceBins() {
		return pathUncDistanceBins;
	}

	public float[] getCrustalError() {
		return pathUncCrustError;
	}

	public float[][] getRandomError() {
		return pathUncRandomError;
	}

	public float[][] getModelError() {
		return pathUncModelError;
	}

	public float[][] getBias() {
		return pathUncBias;
	}

	/**
	 * Sets this objects data arrays.
	 *
	 * @param distBins    Distance bin array [dist].
	 * @param crustError  Crust error array [vertex].
	 * @param randomError Random error array [dist][vertex].
	 * @param modelError  Random error array [dist][vertex].
	 * @param bias        Random error array [dist][vertex].
	 * @param modelError
	 * @param bias
	 */
	public void setData(float[] distBins, float[] crustError, float[][] randomError, float[][] modelError,
						float[][] bias) {
		pathUncDistanceBins = distBins;
		pathUncCrustError = crustError;
		pathUncRandomError = new float[0][0];
		if (randomError != null)
			pathUncRandomError = randomError;
		pathUncModelError = modelError;
		pathUncBias = bias;
	}

	/**
	 * Reads this path dependent uncertainty object from a file.
	 * The format of the file is deduced by reading a handful of bytes from the
	 * beginning of the file.
	 *
	 * @param fileName  name of the File
	 * @throws IOException
	 */
	public void readFile(File fileName) throws IOException {
		String className = this.getClass().getSimpleName();
		int nBytes = Math.max(className.length(), "GEOTESSMODEL".length());
		DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName), bufferSize));
		byte[] b = new byte[nBytes];
		input.read(b);
		input.close();

		// convert the bytes to a string.
		String s = new String(b);

		if (s.startsWith(className))
			readFileBinary(fileName);
		else if (s.startsWith("GEOTESSMODEL"))
			readFileGeoTess(fileName);
		else if (s.startsWith("#"))
			readFileAscii(fileName);
		else
			throw new IOException("Cannot read file that starts with: "+s);
	}

	/**
	 * Read path dependent uncertainty parameters from a File.
	 * @param geotessFile name of the File
	 * @throws IOException
	 */
	public void readFileGeoTess(File geotessFile) throws IOException {
		GeoTessModelSLBMPDU model = new GeoTessModelSLBMPDU(geotessFile);
		readModel(model);
	}

	/**
	 * Write path dependent uncertainty parameters to a File.
	 * @param binaryFile name of the File
	 * @throws IOException
	 */
	public void writeFileBinary(File binaryFile) throws IOException {
		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(binaryFile), bufferSize));
		writeFileBinary(output);
		output.close();
	}

	/**
	 * Write path dependent uncertainty parameters to a File.
	 * @param asciiFile name of the File
	 * @throws IOException
	 */
	public void writeFileAscii(File asciiFile) throws IOException {
		BufferedWriter output = new BufferedWriter(new FileWriter(asciiFile), bufferSize);
		writeFileAscii(output);
		output.close();
	}

	/**
	 * Write path dependent uncertainty parameters to a File.
	 * @param geotessFile name of the File
	 * @throws Exception
	 */
	public void writeFileGeoTess(File geotessFile) throws Exception {
		GeoTessModelSLBMPDU model = getGeoTessModelSLBMPDU();
		model.getMetaData().setModelFileFormat(3);
		model.writeModel(geotessFile);
	}

	/**
	 * Object equals test.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || obj.getClass() != this.getClass())
			return false;

		UncertaintyPDU uncertaintyPathDep = (UncertaintyPDU) obj;

		// if (!this.fileName.equals(uncertaintyPathDep.fileName))
		// return false;

		if (this.phaseNum != uncertaintyPathDep.phaseNum)
			return false;

		if (!this.gridId.equals(uncertaintyPathDep.gridId))
			return false;

		if ((this.pathUncDistanceBins != null) && (uncertaintyPathDep.pathUncDistanceBins != null)) {
			if (this.pathUncDistanceBins.length != uncertaintyPathDep.pathUncDistanceBins.length)
				return false;

			for (int i = 0; i < this.pathUncDistanceBins.length; ++i)
				if (this.pathUncDistanceBins[i] != uncertaintyPathDep.pathUncDistanceBins[i])
					return false;
		}

		if ((this.pathUncCrustError != null) && (uncertaintyPathDep.pathUncCrustError != null)) {
			if (this.pathUncCrustError.length != uncertaintyPathDep.pathUncCrustError.length)
				return false;

			for (int i = 0; i < this.pathUncCrustError.length; ++i)
				if (this.pathUncCrustError[i] != uncertaintyPathDep.pathUncCrustError[i])
					return false;
		}

		if ((this.pathUncRandomError != null) && (uncertaintyPathDep.pathUncRandomError != null)) {
			if (this.pathUncRandomError.length != uncertaintyPathDep.pathUncRandomError.length)
				return false;

			for (int i = 0; i < this.pathUncRandomError.length; ++i) {
				float[] thisError = this.pathUncRandomError[i];
				float[] otherError = uncertaintyPathDep.pathUncRandomError[i];
				if (thisError.length != otherError.length)
					return false;

				for (int j = 0; j < thisError.length; ++j)
					if (thisError[j] != otherError[j])
						return false;
			}
		}

		if ((this.pathUncModelError != null) && (uncertaintyPathDep.pathUncModelError != null)) {
			if (this.pathUncModelError.length != uncertaintyPathDep.pathUncModelError.length)
				return false;

			for (int i = 0; i < this.pathUncModelError.length; ++i) {
				float[] thisError = this.pathUncModelError[i];
				float[] otherError = uncertaintyPathDep.pathUncModelError[i];
				if (thisError.length != otherError.length)
					return false;

				for (int j = 0; j < thisError.length; ++j)
					if (thisError[j] != otherError[j])
						return false;
			}
		}

		if ((this.pathUncBias != null) && (uncertaintyPathDep.pathUncBias != null)) {
			if (this.pathUncBias.length != uncertaintyPathDep.pathUncBias.length)
				return false;

			for (int i = 0; i < this.pathUncBias.length; ++i) {
				float[] thisBias = this.pathUncBias[i];
				float[] otherBias = uncertaintyPathDep.pathUncBias[i];
				if (thisBias.length != otherBias.length)
					return false;

				for (int j = 0; j < thisBias.length; ++j)
					if (thisBias[j] != otherBias[j])
						return false;
			}
		}

		return true;
	}

	public GeoTessModelSLBMPDU getGeoTessModelSLBMPDU() throws Exception {
		// Create a MetaData object in which we can specify information
		// needed for model construction.
		GeoTessMetaData metaData = new GeoTessMetaData();

		// Specify a description of the model. This information is not
		// processed in any way by GeoTess. It is carried around for
		// information purposes.
		metaData.setDescription("This is a GeoTessModelSLBMPDU model containing parameters\n"
				+ "for the path dependent uncertainty model for phase " + getPhaseStr());

		// specify the name of the software that is generating
		// the model. This gets stored in the model for informational purposes.
		metaData.setModelSoftwareVersion(this.getClass().getSimpleName());

		// specify the date when the model was generated. This gets
		// stored in the model for informational purposes.
		metaData.setModelGenerationDate(new Date().toString());

		// Specify a list of layer names. This is a 2D model (geographic dimensions).
		metaData.setLayerNames("surface");

		// specify the DataType for the data. All attributes, in all
		// profiles, will have the same data type.
		metaData.setDataType(DataType.FLOAT);

		boolean writeRandomError = getRandomError().length > 0;

		// The model will have an attribute for the CrustalError and
		// then three attributes for each distance bin.
		int nAttributes = 2 * getDistanceBins().length + 1;
		if (writeRandomError)
			nAttributes = 3 * getDistanceBins().length + 1;

		// GeoTessModel requires that units be specified for each attribute.

		String[] attributes = new String[nAttributes];
		String[] units = new String[nAttributes];

		int a = 0;

		attributes[a] = "CrustalError";
		units[a++] = "seconds";

		for (int bin = 0; bin < getDistanceBins().length; ++bin) {
			attributes[a] = String.format("ModelError[%d]", bin);
			units[a++] = "seconds";
		}

		for (int bin = 0; bin < getDistanceBins().length; ++bin) {
			attributes[a] = String.format("Bias[%d]", bin);
			units[a++] = "seconds";
		}

		if (writeRandomError)
			for (int bin = 0; bin < getDistanceBins().length; ++bin) {
				attributes[a] = String.format("RandomError[%d]", bin);
				units[a++] = "seconds";
			}

		// specify the names of the attributes and the units of the
		// attributes in two String arrays.
		metaData.setAttributes(attributes, units);

		GeoTessModelSLBMPDU model = new GeoTessModelSLBMPDU(grid, metaData);

		// populate the 2D model with attribute data
		for (int vertex = 0; vertex < grid.getNVertices(); ++vertex) {
			// the data array associated with this vertex will include 1 value for
			// crustal error, plus [random error, model error and bias] for each distance
			// bin.
			float[] dataArray = new float[nAttributes];

			int i = 0;
			dataArray[i++] = getCrustalError()[vertex];
			for (int bin = 0; bin < getDistanceBins().length; ++bin)
				dataArray[i++] = getModelError()[bin][vertex];

			for (int bin = 0; bin < getDistanceBins().length; ++bin)
				dataArray[i++] = getBias()[bin][vertex];

			if (writeRandomError)
				for (int bin = 0; bin < getDistanceBins().length; ++bin)
					dataArray[i++] = getRandomError()[bin][vertex];

			// Construct a new Data object that holds the array of attribute values.
			Data data = Data.getDataFloat(dataArray);

			// construct a ProfileSurface with the data. A ProfileSurface has data
			// but no radii and hence can only be added to a model that is a 2D model
			// (geographic dimensions only; no radii).
			Profile profile = new ProfileSurface(data);

			// associate the Profile object with the specified vertex and layer of the
			// model.
			// This model has no layers, so specify layer index = 0
			model.setProfile(vertex, 0, profile);
		}

		// that's it for the base class GeoTessModel information.

		// now add the information for the derived class, GeoTessModelSLBMPDU.

		model.setProperties(properties);

		model.setPhase(getPhaseStr());

		model.setDistanceBins(getDistanceBins());

		// At this point, we have a fully functional GeoTessModelSLBMPDU object
		// that we can work with.
		return model;
	}

	public void readModel(GeoTessModelSLBMPDU model) {

		grid = model.getGrid();

		gridId = model.getGrid().getGridID();

		String phase = model.getPhase();

		phaseNum = getPhase(phase);

		pathUncDistanceBins = model.getDistanceBins();

		properties = model.getProperties();

		nDistanceBins = model.getDistanceBins().length;

		nVertices = model.getGrid().getNVertices();

		includeRandomError = model.getNAttributes() == 3 * nDistanceBins + 1;

		pathUncCrustError = new float[nVertices];

		if (includeRandomError)
			pathUncRandomError = new float[nDistanceBins][nVertices];
		else
			pathUncRandomError = new float[0][0];

		pathUncModelError = new float[nDistanceBins][nVertices];
		pathUncBias = new float[nDistanceBins][nVertices];

		for (int vertex = 0; vertex < nVertices; ++vertex) {
			int a = 0;
			pathUncCrustError[vertex] = model.getValueFloat(vertex, a++);
			for (int bin = 0; bin < nDistanceBins; ++bin)
				pathUncModelError[bin][vertex] = model.getValueFloat(vertex, a++);
			for (int bin = 0; bin < nDistanceBins; ++bin)
				pathUncBias[bin][vertex] = model.getValueFloat(vertex, a++);
			if (includeRandomError)
				for (int bin = 0; bin < nDistanceBins; ++bin)
					pathUncRandomError[bin][vertex] = model.getValueFloat(vertex, a++);
		}

		updateProperties();

	}

	/**
	 * Resample all the grid-dependent data structures (crustalError, modelError, randomError and bias)
	 * onto a new grid.
	 * @param newGrid
	 * @return a reference to this.
	 * @throws Exception
	 */
	protected UncertaintyPDU resample(GeoTessGrid newGrid) throws Exception {
		// if newGrid == oldGrid, don't need to do anything.
		if (!newGrid.getGridID().equals(gridId)) {
			// construct a GeoTessModelSLBMPDU with the crustalError, modelError,
			// randomError, bias, distanceBins from this UncertaintyPDU object.
			// oldModel will contain all the information of this UncertaintyPDU object,
			// but held in a different format (a GeoTessModelSLBMPDU object).
			GeoTessModelSLBMPDU pduModel = getGeoTessModelSLBMPDU();

			// resample the old GeoTessModelSLBMPDU model on the new grid.
			GeoTessModelSLBMPDU newModel = (GeoTessModelSLBMPDU)pduModel.resample(newGrid);

			// replace all the UncertaintyPDU information with info from the new, resampled model.
			readModel(newModel);
		}
		return this;
	}

	/**
	 * Writes this path dependent uncertainty file in binary format.
	 *
	 * @param output The output DataOutputStream.
	 * @throws IOException
	 */
	@Override
	protected void writeFileBinary(DataOutputStream output) throws IOException {

		int nDistanceBins = pathUncDistanceBins.length;
		int nVertices = pathUncCrustError.length;
		boolean includeRandomError = pathUncRandomError.length > 0;

		// make sure the required properties are up to date.
		updateProperties();

		output.write(this.getClass().getSimpleName().getBytes());
		output.writeInt(1); // file format version number

		output.writeInt(properties.size());
		for (Entry<String, String> entry : properties.entrySet()) {
			GeoTessUtils.writeString(output, entry.getKey());
			GeoTessUtils.writeString(output, entry.getValue().replaceAll("\n", "<NEWLINE>"));
		}

		for (int i = 0; i < nDistanceBins; ++i)
			output.writeFloat(pathUncDistanceBins[i]);

		for (int i = 0; i < nVertices; ++i)
			output.writeFloat(pathUncCrustError[i]);

		if (includeRandomError) {
			for (int i = 0; i < nDistanceBins; ++i)
				for (int j = 0; j < nVertices; ++j)
					output.writeFloat(pathUncRandomError[i][j]);
		}

		for (int i = 0; i < nDistanceBins; ++i)
			for (int j = 0; j < nVertices; ++j)
				output.writeFloat(pathUncModelError[i][j]);

		for (int i = 0; i < nDistanceBins; ++i)
			for (int j = 0; j < nVertices; ++j)
				output.writeFloat(pathUncBias[i][j]);
	}

	/**
	 * Writes this path dependent uncertainty file in ascii format.
	 *
	 * @param output The output Writer.
	 * @throws IOException
	 */
	@Override
	protected void writeFileAscii(Writer output) throws IOException {

		nDistanceBins = pathUncDistanceBins.length;
		nVertices = pathUncCrustError.length;
		includeRandomError = pathUncRandomError.length > 0;

		// First line of the file must be "# RSTT Path Dependent Uncertainty".
		output.write("# RSTT Path Dependent Uncertainty\n");

		// write the fileFormatVersion.  The currently supported version is 1 but
		// could change in the future if it becomes necessary to add additional information.
		output.write("FileFormatVersion 1\n");

		// write out required and optional properties.  Required properties will be 
		// loaded here and may be modified if this model is later written to output.
		// Optional properties will not be modified by this code and will be output 
		// to new files and by the toString function.

		// first make sure the required properties in the properties map are up to date.
		updateProperties();

		output.write("# Properties: (phase, gridId, nDistanceBins, nVertices, includeRandomError are required; others optional)\n");
		//output.write(String.format("%d\n", properties.size()));
		for (Entry<String, String> entry : properties.entrySet())
			output.write(String.format("%s = %s%n", entry.getKey(), entry.getValue().replaceAll("\n", "<NEWLINE>")));
		output.write("\n");

		String formatter = " %13.7e";

		output.write(String.format("# Distance Bins\n"));
		for (int i = 0; i < nDistanceBins; ++i) {
			output.write(String.format("%5.2f", pathUncDistanceBins[i]));
			output.write(i == nDistanceBins - 1 ? "\n" : " ");
		}
		output.write("\n");

		output.write(String.format("# Crustal Error\n"));
		for (int i = 0; i < nVertices; ++i)
			output.write(String.format(formatter + "%n", pathUncCrustError[i]));
		output.write("\n");

		if (includeRandomError) {
			output.write(String.format("# Random Error\n"));
			for (int j = 0; j < nVertices; ++j)
				for (int i = 0; i < nDistanceBins; ++i) {
					output.write(String.format(formatter, pathUncRandomError[i][j]));
					output.write(i == nDistanceBins - 1 ? "\n" : " ");
				}
			output.write("\n");
		}

		output.write(String.format("# Model Error\n"));
		for (int j = 0; j < nVertices; ++j)
			for (int i = 0; i < nDistanceBins; ++i) {
				output.write(String.format(formatter, pathUncModelError[i][j]));
				output.write(i == nDistanceBins - 1 ? "\n" : " ");
			}
		output.write("\n");

		output.write(String.format("# Bias\n"));
		for (int j = 0; j < nVertices; ++j)
			for (int i = 0; i < nDistanceBins; ++i) {
				output.write(String.format(formatter, pathUncBias[i][j]));
				output.write(i == nDistanceBins - 1 ? "\n" : " ");
			}
	}

	/**
	 * Reads this path dependent uncertainty object from an ascii file.
	 *
	 * @param input The input BufferedReader.
	 * @throws IOException
	 */
	protected void readFileAscii(Scanner input) throws IOException {
		String line = input.nextLine();
		if (!line.startsWith("# RSTT Path Dependent Uncertainty"))
			throw new IOException("Expected file to start with '# UncertaintyPDU' but found: " + line);

		line = input.nextLine();
		if (!line.startsWith("FileFormatVersion "))
			throw new IOException("Expected to find 'FileFormatVersion int' but found: " + line);

		int fileFormatVersion = Integer.parseInt(line.split("\\s+")[1].trim());
		if (fileFormatVersion < 1 || fileFormatVersion > 999)
			throw new IOException(String.format("FileFormatVersion = %d is not recognized.", fileFormatVersion));

		line = input.nextLine();
		if (!line.startsWith("# Properties"))
			throw new IOException("Expected to find '# Properties' but found: " + line);

		properties = new LinkedHashMap<String, String>();
		line = input.nextLine().trim();
		while (line.length() > 0) {
			int p = line.indexOf("=");
			if (p > 0)
			{
				String key = line.substring(0, p).trim();
				String value = line.substring(p + 1, line.length()).trim().replaceAll("<NEWLINE>", "\n");
				properties.put(key, value);
			}
			line = input.nextLine().trim();
		}

		ingestProperties();

		line = input.nextLine();
		while (!line.startsWith("# Distance Bins"))
			line = input.nextLine();

		pathUncDistanceBins = new float[nDistanceBins];
		for (int i = 0; i < nDistanceBins; ++i)
			pathUncDistanceBins[i] = input.nextFloat();

		line = input.nextLine();
		while (!line.startsWith("# Crustal Error"))
			line = input.nextLine();
		pathUncCrustError = new float[nVertices];
		for (int i = 0; i < nVertices; ++i)
			pathUncCrustError[i] = input.nextFloat();

		if (includeRandomError) {
			line = input.nextLine();
			while (!line.startsWith("# Random Error"))
				line = input.nextLine();

			pathUncRandomError = new float[nDistanceBins][nVertices];
			for (int j = 0; j < nVertices; ++j)
				for (int i = 0; i < nDistanceBins; ++i)
					pathUncRandomError[i][j] = input.nextFloat();
		}
		else pathUncRandomError = new float[0][];

		line = input.nextLine();
		while (!line.startsWith("# Model Error"))
			line = input.nextLine();
		pathUncModelError = new float[nDistanceBins][nVertices];
		for (int j = 0; j < nVertices; ++j)
			for (int i = 0; i < nDistanceBins; ++i)
				pathUncModelError[i][j] = input.nextFloat();

		line = input.nextLine();
		while (!line.startsWith("# Bias"))
			line = input.nextLine();
		pathUncBias = new float[nDistanceBins][nVertices];
		for (int j = 0; j < nVertices; ++j)
			for (int i = 0; i < nDistanceBins; ++i)
				pathUncBias[i][j] = input.nextFloat();
		input.nextLine();
	}

	/**
	 * Reads this path dependent uncertainty object from a binary file.
	 *
	 * @param input The input DataInputStream.
	 * @throws IOException
	 */
	@Override
	protected void readFileBinary(DataInputStream input) throws IOException {

		String className = this.getClass().getSimpleName();
		byte[] bytes = new byte[className.length()];
		input.read(bytes);
		String s = new String(bytes);
		if (!s.equals(className))
			throw new IOException(String.format("Expected file to start with characters "
					+ "'%s', but found: " + s));

		int format = input.readInt();

		if (format != 1)
			throw new IOException("File format " + format + " is not recognized.");

		properties.clear();
		int nProperties = input.readInt();
		for (int i = 0; i < nProperties; ++i)
			properties.put(GeoTessUtils.readString(input),
					GeoTessUtils.readString(input).replaceAll("<NEWLINE>", "\n"));

		ingestProperties();

		pathUncDistanceBins = new float[nDistanceBins];
		for (int i = 0; i < nDistanceBins; ++i)
			pathUncDistanceBins[i] = input.readFloat();

		pathUncCrustError = new float[nVertices];
		for (int i = 0; i < nVertices; ++i)
			pathUncCrustError[i] = input.readFloat();

		if (includeRandomError) {
			pathUncRandomError = new float[nDistanceBins][nVertices];
			for (int i = 0; i < nDistanceBins; ++i)
				for (int j = 0; j < nVertices; ++j)
					pathUncRandomError[i][j] = input.readFloat();
		} else
			pathUncRandomError = new float[0][0];

		pathUncModelError = new float[nDistanceBins][nVertices];
		for (int i = 0; i < nDistanceBins; ++i)
			for (int j = 0; j < nVertices; ++j)
				pathUncModelError[i][j] = input.readFloat();

		pathUncBias = new float[nDistanceBins][nVertices];
		for (int i = 0; i < nDistanceBins; ++i)
			for (int j = 0; j < nVertices; ++j)
				pathUncBias[i][j] = input.readFloat();

	}

	/**
	 * make sure the required properties are up to date.
	 * @return
	 */
	private UncertaintyPDU updateProperties()
	{
		properties.put("phase", getPhaseStr());
		properties.put("gridId", gridId);
		properties.put("nDistanceBins", Integer.toString(nDistanceBins));
		properties.put("nVertices", Integer.toString(nVertices));
		properties.put("includeRandomError", Boolean.toString(includeRandomError));
		return this;
	}

	/**
	 * extract required properties: phase, gridId, nDistanceBins, nVertices and
	 * includeRandomError
	 *
	 * @throws IOException
	 */
	private void ingestProperties() throws IOException {
		// extract required properties: phase, gridId, nDistanceBins, nVertices and
		// includeRandomError
		if (properties.containsKey("phase"))
			phaseNum = Uncertainty.getPhase(properties.get("phase"));
		else
			throw new IOException("properties map does not contain entry for string phase");

		if (properties.containsKey("gridId"))
			gridId = properties.get("gridId");
		else
			throw new IOException("properties map does not contain entry for string gridId");

		if (properties.containsKey("nDistanceBins"))
			nDistanceBins = Integer.parseInt(properties.get("nDistanceBins"));
		else
			throw new IOException("properties map does not contain entry for int nDistanceBins");

		if (properties.containsKey("nVertices"))
			nVertices = Integer.parseInt(properties.get("nVertices"));
		else
			throw new IOException("properties map does not contain entry for int nVertices");

		if (properties.containsKey("includeRandomError"))
			includeRandomError = Boolean.parseBoolean(properties.get("includeRandomError"));
		else
			throw new IOException("properties map does not contain entry for boolean includeRandomError");
	}

	/**
	 * Reads this path dependent uncertainty object from a binary file.
	 *
	 * @param fileName
	 * @throws IOException
	 */
	@Override
	public void readFileBinary(File fileName) throws IOException {
		DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName), bufferSize));
		readFileBinary(input);
		input.close();
	}

	/**
	 * Reads this path dependent uncertainty object from an ascii file.
	 *
	 * @param fileName
	 * @throws IOException
	 */
	@Override
	public void readFileAscii(File fileName) throws IOException {
		Scanner input = new Scanner(fileName);
		readFileAscii(input);
		input.close();
	}

	/**
	 * Reads this path dependent uncertainty object from a file.
	 *
	 * @param fileName
	 * @param readBinary reads binary format if true (else ascii format is read).
	 * @throws IOException
	 */
	@Override
	public void readFile(File fileName, boolean readBinary) throws IOException {
		if (readBinary)
			readFileBinary(fileName);
		else
			readFileAscii(fileName);
	}

	/**
	 * Writes this path dependent uncertainty file.
	 *
	 * @param outputFile  The directory path to write this file.
	 * @param writeBinary writes binary format if true (else ascii format is
	 *                    written).
	 * @throws IOException
	 */
	@Override
	public void writeFile(File outputFile, boolean writeBinary) throws IOException {
		if (writeBinary)
			writeFileBinary(outputFile);
		else
			writeFileAscii(outputFile);
	}

	@Override
	public boolean isPathDependent() {
		return true;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer("UncertaintyPDU:\n");
		for (Entry<String, String> e : properties.entrySet())
			buf.append(String.format("%s = %s%n", e.getKey(), e.getValue().replaceAll("<NEWLINE>", "\n")));
		buf.append("\n");
		return buf.toString();
	}

	public GeoTessGrid getGrid() {
		return grid;
	}

	public void setGrid(GeoTessGrid grid) throws IOException {
		if (!grid.getGridID().equals(this.gridId))
			throw new IOException("Trying to assign a GeoTessGrid to this UncertaintyPDU object "
					+ "but the gridIds are not equal");
		if (grid.getNVertices() != this.nVertices)
			throw new IOException("Trying to assign a GeoTessGrid to this UncertaintyPDU object "
					+ "but the number of vertices are not equal");
		this.grid = grid;
	}

}
