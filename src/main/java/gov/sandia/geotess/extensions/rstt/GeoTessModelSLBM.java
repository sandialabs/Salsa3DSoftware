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
import java.util.Scanner;

import gov.sandia.geotess.Data;
import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessUtils;
import gov.sandia.gmp.util.globals.Globals;

public class GeoTessModelSLBM extends GeoTessModel {

	private double[] averageMantelVelocity;

	/**
	 * path independent uncertainty[phase][attribute] for 4 phases = (Pn, Sn, Pg,
	 * Lg), and 3 attributes = (TT, SH, AZ))
	 */
	private UncertaintyPIU[][] piu;

	/**
	 * path dependent uncertainty for 4 phases = (Pn, Sn, Pg, Lg)
	 */
	private UncertaintyPDU[] pdu;

	public GeoTessModelSLBM() {
		super();
	}

	public GeoTessModelSLBM(File modelPath) throws IOException {
		super(modelPath);
	}

	public GeoTessModelSLBM(String modelPath) throws IOException {
		super(modelPath);
	}

	public GeoTessModelSLBM(File input, String pathToGridDir) throws IOException {
		super(input, pathToGridDir);
	}

	public GeoTessModelSLBM(String modelPath, String relGridPath) throws IOException {
		super(modelPath, relGridPath);
	}

	public GeoTessModelSLBM(File gridFileName, GeoTessMetaData metadata) throws IOException {
		super(gridFileName, metadata);
	}

	public GeoTessModelSLBM(String gridFileName, GeoTessMetaData metadata) throws IOException {
		super(gridFileName, metadata);
	}

	public GeoTessModelSLBM(GeoTessGrid grid, GeoTessMetaData metadata) throws GeoTessException {
		super(grid, metadata);
	}

	public double getAverageMantleVelocity(int index) {
		if (averageMantelVelocity == null)
			averageMantelVelocity = new double[2];
		return averageMantelVelocity[index];
	}

	public void setAverageMantleVelocity(int index, double velocity) {
		if (averageMantelVelocity == null)
			averageMantelVelocity = new double[2];
		averageMantelVelocity[index] = velocity;
	}

	public boolean isPDUSupported() {
		return pdu.length > 0;
	}

	public boolean isPIUSupported() {
		return piu.length > 0;
	}

	public UncertaintyPIU[][] getPathIndependentUncertainty() {
		return piu;
	}

	public void setPathIndependentUncertainty(UncertaintyPIU[][] piu) {
		if (piu == null)
			this.piu = new UncertaintyPIU[0][0];
		else
			this.piu = piu;
	}

	public UncertaintyPDU[] getPathDependentUncertainty() {
		return pdu;
	}

	public void setPathDependentUncertainty(UncertaintyPDU[] pdu) throws Exception {
		if (pdu == null)
			this.pdu = new UncertaintyPDU[0];
		else
			this.pdu = pdu;

		for (UncertaintyPDU u : pdu)
			u.setGrid(this.getGrid());
	}

	public UncertaintyPDU getPathDependentUncertainty(String phase) {
		return getPathDependentUncertainty(Uncertainty.getPhase(phase));
	}

	public UncertaintyPDU getPathDependentUncertainty(int phase) {
		return phase >= pdu.length ? null : pdu[phase];
	}

	public void setPathDependentUncertainty(UncertaintyPDU pdu, String phase) throws Exception {
		setPathDependentUncertainty(pdu, Uncertainty.getPhase(phase));
	}

	public void clearPathDependentUncertainty()
	{
		this.pdu = new UncertaintyPDU[0];
	}

	public void setPathDependentUncertainty(UncertaintyPDU pdu, int phase) throws Exception {
		if (this.pdu == null || this.pdu.length == 0)
			this.pdu = new UncertaintyPDU[4];
		this.pdu[phase] = pdu;
		pdu.setGrid(this.getGrid());
	}

	public void setPathDependentUncertainty(GeoTessModelSLBM otherModel, int phase) throws Exception {
		if (this.pdu == null || this.pdu.length == 0)
			this.pdu = new UncertaintyPDU[4];

		if (this.getGrid().getGridID().equals(otherModel.getGrid().getGridID()))
			this.pdu[phase] = otherModel.getPathDependentUncertainty(phase);
		else
		{
			this.pdu[phase] = otherModel.getPathDependentUncertainty(phase).resample(
					otherModel.getGrid());
		}
	}

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
		GeoTessModelSLBM o = (GeoTessModelSLBM) other;
		this.averageMantelVelocity = o.averageMantelVelocity;
		this.piu = o.piu;
		if (o.pdu != null && o.pdu.length > 0)
		{
			if (getGrid().getGridID().equals(o.pdu[0].getGridId()))
				this.pdu = o.pdu;
			else
			{
				this.pdu = new UncertaintyPDU[4];
				for (int i=0; i<4; ++i)
					this.pdu[i] = o.pdu[i].resample(this.getGrid());
			}
		}
	}

	@Override
	protected void loadModelAscii(Scanner input, String inputDirectory, String relGridFilePath)
			throws IOException, GeoTessException {

		// load base class GeoTessModel information
		super.loadModelAscii(input, inputDirectory, relGridFilePath);

		checkMiddleCrustLayers();

		// now load GeoTessModelSLBM data
		if (!input.hasNext())
			throw new IOException("Unable to read this GeoTessModelSLBM.  All the GeoTessModel \n"
					+ "information was read successfully but then an end-of-file was encountered \n"
					+ "before the GeoTessModelSLBM information could be read.  This indicates that the file contains \n"
					+ "a GeoTessModel, but not a GeoTessModelSLBM.");

		// it is required to store the class name as the first thing added by the
		// extending class.

		String className = input.nextLine();
		if (!className.equals(this.getClass().getSimpleName()))
			throw new IOException("Found name " + className + " but expecting " + this.getClass().getSimpleName());

		// it is good practice, but not required, to store a format
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		int fileFormatVersion = input.nextInt();

		if (fileFormatVersion < 1 || fileFormatVersion > 3)
			throw new IOException(GeoTessModelSLBM.class.getSimpleName() + " Format version " + fileFormatVersion
					+ " is not supported.");

		// read average mantle velocities

		averageMantelVelocity = new double[2];
		averageMantelVelocity[0] = input.nextFloat();
		averageMantelVelocity[1] = input.nextFloat();

		// read number of piu objects stored
		// these are the old-school, path-independent uncertainties.

		// phases generally include Pn, Sn, Pg and Lg
		// attributes may include TT, SH and/or AZ

		int nPhases = input.nextInt();
		int nAttributes = input.nextInt();
		int phaseIndex, attributeIndex;
		String phase, attribute;

		piu = new UncertaintyPIU[nPhases][nAttributes];
		for (int i = 0; i < nPhases; ++i) {
			for (int j=0; j< nAttributes; ++j)
			{
				phase = input.next();
				attribute = input.next();
				phaseIndex = Uncertainty.getPhase(phase);
				attributeIndex = Uncertainty.getAttribute(attribute);
				// call method in UncertaintyPIU to read the data
				piu[phaseIndex][attributeIndex] = UncertaintyPIU.getUncertainty(
						input, phaseIndex, attributeIndex);
			}
		}

		// if fileFormatVersion is 3, then path dependent uncertainty is available.
		if (fileFormatVersion == 3) {
			nPhases = input.nextInt(); input.nextLine();
			pdu = new UncertaintyPDU[4];
			for (int i = 0; i < nPhases; ++i) {
				UncertaintyPDU u = new UncertaintyPDU();
				u.readFileAscii(input);
				u.setGrid(getGrid());
				pdu[u.getPhase()] = u;
			}
		} else
			pdu = new UncertaintyPDU[0];

	}

	@Override
	protected void loadModelBinary(DataInputStream input, String inputDirectory, String relGridFilePath)
			throws GeoTessException, IOException {

		// load base class GeoTessModel information
		super.loadModelBinary(input, inputDirectory, relGridFilePath);

		checkMiddleCrustLayers();

		// now load GeoTessModelSLBM data
		if (input.available() == 0)
			throw new IOException("Unable to read this GeoTessModelSLBM.  All the GeoTessModel \n"
					+ "information was read successfully but then an end-of-file was encountered \n"
					+ "before the GeoTessModelSLBM information could be read.  This indicates that the file contains \n"
					+ "a GeoTessModel, but not a GeoTessModelSLBM.");

		// it is required to store the class name as the first thing added by the
		// extending class.

		String className = GeoTessUtils.readString(input);

		// some old slbm model files had 'SLBM' at the start.
		if (className.equals("SLBM")) className = "GeoTessModelSLBM";

		if (!className.equalsIgnoreCase(this.getClass().getSimpleName()))
			throw new IOException("Found name " + className + " but expecting " + this.getClass().getSimpleName());

		// it is good practice, but not required, to store a format
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		int fileFormatVersion = input.readInt();

		if (fileFormatVersion < 1 || fileFormatVersion > 3)
			throw new IOException(GeoTessModelSLBM.class.getSimpleName() + " Format version " + fileFormatVersion
					+ " is not supported.");

		// read average mantle velocities

		averageMantelVelocity = new double[2];
		averageMantelVelocity[0] = input.readFloat();
		averageMantelVelocity[1] = input.readFloat();

		// read number of piu objects stored
		// these are the old-school, path-independent uncertainties.

		// phases generally include Pn, Sn, Pg and Lg
		// attributes may include TT, SH and/or AZ

		int nPhases = input.readInt();
		int nAttributes = input.readInt();
		piu = new UncertaintyPIU[nPhases][nAttributes];
		for (int i = 0; i < nPhases; ++i)
			for (int j = 0; j < nAttributes; ++j) {
				int phase = Uncertainty.getPhase(GeoTessUtils.readString(input));
				int attribute = Uncertainty.getAttribute(GeoTessUtils.readString(input));
				piu[phase][attribute] = UncertaintyPIU.getUncertainty(input, phase, attribute);
			}

		if (fileFormatVersion == 3) {
			nPhases = input.readInt();
			pdu = new UncertaintyPDU[4];
			for (int i = 0; i < nPhases; ++i) {
				UncertaintyPDU u = new UncertaintyPDU();
				u.readFileBinary(input);
				u.setGrid(getGrid());
				pdu[u.getPhase()] = u;
			}
		} else
			pdu = new UncertaintyPDU[0];

	}

	@Override
	protected void writeModelAscii(Writer output, String gridFileName) throws IOException {

		checkMiddleCrustLayers();

		// call super class to write standard GeoTessModel information to ascii file.
		super.writeModelAscii(output, gridFileName);

		// it is required to store the class name as the first thing added
		// by the extending class.
		output.write(this.getClass().getSimpleName() + GeoTessUtils.NL);

		// It is highly recommended, but not required, to store a format version
		// number right after the class name.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.

		int fileFormatVersion = pdu.length == 0 ? 2 : 3;
		output.write(String.format("%3d%n", fileFormatVersion));

		// write out average mantle velocities

		output.write("  " + (float) averageMantelVelocity[0] + Globals.NL);
		output.write("  " + (float) averageMantelVelocity[1] + Globals.NL);

		// write number of piu objects stored
		// these are the old-school, path-independent uncertainties.

		// phases generally include Pn, Sn, Pg and Lg
		// attributes may include TT, SH and/or AZ

		output.write(String.format("  %d %d%n", piu.length, piu[0].length));
		for (int p = 0; p < piu.length; ++p)
			for (int a = 0; a < piu[p].length; ++a)
			{
				output.write(Uncertainty.getPhase(p) + " " + Uncertainty.getAttribute(a) + Globals.NL);
				if (piu[p][a] == null)
					output.write("   0   0\n");
				else
					piu[p][a].writeFileAscii(output);
			}

		if (fileFormatVersion == 3) {
			output.write(String.format("%d%n", pdu.length));
			for (int p = 0; p < pdu.length; ++p)
				pdu[p].writeFileAscii(output);
		}
	}

	@Override
	protected void writeModelBinary(DataOutputStream output, String gridFileName) throws IOException {

		checkMiddleCrustLayers();

		// call super class to write standard model information to binary file.
		super.writeModelBinary(output, gridFileName);

		// it is required to store the class name as the first thing added
		// by the extending class.
		GeoTessUtils.writeString(output, this.getClass().getSimpleName());

		// it is good practice, but not required, to store a format
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.

		int fileFormatVersion = pdu.length == 0 ? 2 : 3;
		output.writeInt(fileFormatVersion);

		// write out average mantle velocities
		output.writeFloat((float) averageMantelVelocity[0]);
		output.writeFloat((float) averageMantelVelocity[1]);

		// write the number of uncertaintySLBM objects stored (nPhases and nAttributes)
		// these are the old-school, path-independent, 1D-distance-dependent
		// uncertainties.

		// phases generally include Pn, Sn, Pg and Lg
		// attributes may include TT, SH and/or AZ

		output.writeInt(piu.length);
		output.writeInt(piu.length > 0 ? piu[0].length : 0);

		for (int p = 0; p < piu.length; ++p)
			for (int a = 0; a < piu[p].length; ++a) {
				GeoTessUtils.writeString(output, Uncertainty.getPhase(p));
				GeoTessUtils.writeString(output, Uncertainty.getAttribute(a));
				if (piu[p][a] == null) {
					output.writeInt(0);
					output.writeInt(0);
				} else
					piu[p][a].writeFileBinary(output);
			}

		if (fileFormatVersion == 3) {
			output.writeInt(pdu.length);
			for (int p = 0; p < pdu.length; ++p)
				pdu[p].writeFileBinary(output);
		}
	}

	/**
	 * Some old versions of SLBM incorrectly wrote the model slowness values in the
	 * wrong order (middle_crust_N followed by middle_crust_G). This method checks to see if the
	 * layers are in the wrong order and, if they are, swaps them.
	 *
	 * Correct order is middle_crust_G(3) followed by middle_crust_N(4)
	 *
	 * @throws IOException
	 */
	private void checkMiddleCrustLayers() throws IOException {
		if (getMetaData().getLayerIndex("middle_crust_N") == 3
				&& getMetaData().getLayerIndex("middle_crust_G") == 4) {
			String[] layerNames = getMetaData().getLayerNames();
			layerNames[3] = "middle_crust_G";
			layerNames[4] = "middle_crust_N";
			getMetaData().setLayerNames(layerNames);
			for (int i = 0; i < getNVertices(); ++i) {
				Data dn, dg;
				dn = this.getProfile(i, 3).getData(0);
				dg = this.getProfile(i, 4).getData(0);

				float vn0 = dn.getFloat(0);
				float vn1 = dn.getFloat(1);
				float vg0 = dg.getFloat(0);
				float vg1 = dg.getFloat(1);

				dn.setValue(0, vg0);
				dn.setValue(1, vg1);
				dg.setValue(0, vn0);
				dg.setValue(1, vn1);
			}
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;

		if (other == null || other.getClass() != this.getClass())
			return false;

		GeoTessModelSLBM o = (GeoTessModelSLBM) other;

		if (o.pdu.length != pdu.length)
			return false;
		if (o.piu.length != piu.length)
			return false;

		if (o.averageMantelVelocity.length != averageMantelVelocity.length)
			return false;
		for (int i = 0; i < averageMantelVelocity.length; ++i)
			if (o.averageMantelVelocity[i] != averageMantelVelocity[i])
				return false;

		for (int i = 0; i < pdu.length; ++i)
			if (!o.pdu[i].equals(pdu[i]))
				return false;

		for (int i = 0; i < piu.length; ++i)
			for (int j = 0; j < piu[i].length; ++j) {
				// if one is null and the other not, return false
				if (o.piu[i][j] == null ^ piu[i][j] == null)
					return false;
				// if both are not null, check equality
				if (piu[i][j] != null && !o.piu[i][j].equals(piu[i][j]))
					return false;
			}

		if (!super.equals(other))
			return false;

		return true;
	}

	private String getSupportedPhasesPIU() {
		String list = "";
		if (piu != null)
			for (int i = 0; i < piu.length; ++i)
				if (piu[i] != null)
					for (int j = 0; j < piu[i].length; ++j)
						if (piu[i][j] != null) {
							list += ", " + piu[i][j].getPhaseStr();
							break;
						}
		return list.length() >= 2 ? list.substring(2) : "none";
	}

	private String getSupportedPhasesPDU() {
		String list = "";
		if (pdu != null)
			for (int i = 0; i < pdu.length; ++i)
				list += ", " + pdu[i].getPhaseStr();
		return list.length() >= 2 ? list.substring(2) : "none";
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer(super.toString() + "\nGeoTessModelSLBM data:\n");
		buf.append(String.format("%naverageMantelVelocity = %s%n", Arrays.toString(averageMantelVelocity)));

		buf.append(String.format("path independent uncertainty supported phases = %s%n", getSupportedPhasesPIU()));
		buf.append(String.format("path   dependent uncertainty supported phases = %s%n%n", getSupportedPhasesPDU()));

		for (int i=0; i<pdu.length; ++i)
			buf.append(pdu[i].toString());

		return buf.toString();
	}

}
