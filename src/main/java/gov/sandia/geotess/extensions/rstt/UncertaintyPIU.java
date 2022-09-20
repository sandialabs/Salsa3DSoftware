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
import java.util.Arrays;
import java.util.Scanner;

/**
 * Class to manage path independent model uncertainty for 
 * travel time, slowness, and azimuth.
 * TT in seconds,
 * Slowness in sec/radian
 * Azimuth in radians.

 * @author jrhipp, sballar
 *
 */
public class UncertaintyPIU extends Uncertainty
{
	/**
	 * The Attribute number (TT, Sh, Az) for which this UncertaintyPIU
	 * object is defined.
	 */
	private int attributeNum;

	/**
	 * Distance values (degrees) at which the uncertainty is defined.
	 */
	private double[] errDistances;

	/**
	 * Depth values (km) at which the uncertainty is defined.
	 */
	private double[] errDepths;

	/**
	 * Uncertainty values [depth][distance]).
	 * TT in seconds,
	 * Slowness in sec/radian
	 * Azimuth in radians.
	 */
	private double[][] errVal;

	/**
	 * An array of doubles representing the angular distances (in degrees)
	 * for which the model errors are defined.
	 *
	 * @return The array of distances
	 */
	public double[] getDistances()
	{
		return errDistances;
	}

	/**
	 * An array of doubles representing the depths (in km)
	 * for which the model errors are defined. If empty there is no depth
	 * dependence
	 *
	 * @return The array of depths
	 */
	public double[] getDepths()
	{
		return errDepths;
	}

	/**
	 * A array of double[nDepths][nDistances] representing the model uncertainties.
	 * TT in seconds,
	 * Slowness in sec/radian
	 * Azimuth in radians.
	 *
	 * @return The array of model errors[nDepths][nDistances].
	 */
	public double[][] getValues()
	{
		return errVal;
	}

	/**
	 * Default constructor
	 */
	public UncertaintyPIU()
	{
		super();
		attributeNum = -1;
	}

	/**
	 * Parameterized Uncertainty Constructor used by SLBM.  phase is one of
	 * "Pn", "Sn", "Pg", "Lg". attribute is one of "TT", "Sh", "Az"
	 *
	 * @param phase input phase number.
	 * @param attribute input attribute number.
	 */
	public UncertaintyPIU(int phase, int attribute)
	{
		super(phase);
		attributeNum = attribute;
	}

	/**
	 * Parameterized Uncertainty Constructor used by SLBM.  phase is one of
	 * "Pn", "Sn", "Pg", "Lg". attribute is one of "TT", "Sh", "Az"
	 *
	 * @param phase input phase string.
	 * @param attribute input attribute string.
	 */
	public UncertaintyPIU(String phase, String attribute)
	{
		super(phase);
		attributeNum = getAttribute(attribute);
	}

	/**
	 * Standard constructor that reads the objects contents from the input file
	 * path.
	 *
	 * @param fileName The file from which the object is read.
	 * @param phase The objects phase number.
	 * @param attribute The objects attribute number.
	 * @param readBinary Reads binary format if true (else ascii format is read).
	 * @throws IOException
	 */
	public UncertaintyPIU(File fileName, String phase, String attribute,
						  boolean readBinary) throws IOException
	{
		this(fileName, getPhase(phase), getAttribute(attribute), readBinary);
	}

	/**
	 * Standard constructor that reads the objects contents from the input file
	 * path.
	 *
	 * @param fileName The file from which the object is read.
	 * @param phase The objects phase number.
	 * @param attribute The objects attribute number.
	 * @param readBinary Reads binary format if true (else ascii format is read).
	 * @throws IOException
	 */
	public UncertaintyPIU(File fileName, int phase, int attribute,
						  boolean readBinary) throws IOException
	{
		super(phase);
		attributeNum = attribute;
		readFile(fileName, readBinary);
	}

	/**
	 * Static factory method to load an existing UncertaintyPIU from a file.
	 * @param input
	 * @param phase The phase of the uncertainty file to be loaded.
	 * @param attribute The attribute of the uncertainty file to be loaded.
	 * @return The new UncertaintyPIU object if successful, or null otherwise.
	 * @throws IOException
	 */
	public static UncertaintyPIU getUncertainty(Scanner input,
												int phase, int attribute) throws IOException
	{
		UncertaintyPIU uncertainty = new UncertaintyPIU(phase, attribute);
		uncertainty.readFileAscii(input);
		if (uncertainty.getDistances().length == 0)
			return null;
		return uncertainty;
	}

	/**
	 * Static factory method to load an existing UncertaintyPIU from a file.
	 * @param input
	 * @param phase The phase of the uncertainty file to be loaded.
	 * @param attribute The attribute of the uncertainty file to be loaded.
	 * @return The new UncertaintyPIU object if successful, or null otherwise.
	 * @throws IOException
	 */
	public static UncertaintyPIU getUncertainty(DataInputStream input,
												int phase, int attribute) throws IOException
	{
		UncertaintyPIU uncertainty = new UncertaintyPIU(phase, attribute);
		uncertainty.readFileBinary(input);
		if (uncertainty.getDistances().length == 0)
			return null;
		return uncertainty;
	}

	public int getAttribute()
	{
		return attributeNum;
	}

	public String getAttributeStr()
	{
		return getAttribute(attributeNum);
	}

	public double getUncertainty(double f, int idist, int idepth)
	{
		return (f * (errVal[idepth][idist+1] - errVal[idepth][idist]) +
				errVal[idepth][idist]);
	}

	public double getVariance(double f, int idist, int idepth)
	{
		return (f * (errVal[idepth][idist+1] * errVal[idepth][idist+1] -
				errVal[idepth][idist] * errVal[idepth][idist]) +
				errVal[idepth][idist] * errVal[idepth][idist]);
	}

	public double getUncertainty(double distance, double depth)
	{
		// Convert to degrees since model errors are defined in degrees.
		double distanceDeg = Math.toDegrees(distance);

		// initialize indices and interpolation weights
		Integer idist = 0;
		Integer idepth = 0;
		double wdist, wdepth;
		wdist = wdepth = 0.0;

		if ((errVal.length == 1) || (depth >= errDepths[0]))
		{
			// if more than one depth and depth exceeds last entry set idepth
			if (errVal.length > 1) idepth = errDepths.length - 1;

			// if distance in question is greater than the max defined distance, return
			// uncertainty defined for max distance.  Otherwise, interpolate between the
			// two bracketing distances.
			if( distanceDeg >= errDistances[errDistances.length - 1])
				return errVal[idepth][errDistances.length - 1];
			else
			{
				// get distance interpolation index and weight
				wdist = getIndex(distanceDeg, errDistances, idist);
				return getUncertainty(wdist, idist, idepth);
			}
		}
		else
		{
			// get depth interpolation index and weight
			wdepth = getIndex(depth, errDepths, idepth);

			// if distance in question is greater than the max defined distance, return
			// uncertainty defined for max distance.  Otherwise, interpolate between the
			// two bracketing distances.
			if( distanceDeg >= errDistances[errDistances.length - 1])
				return wdepth * (errVal[idepth+1][errDistances.length - 1] -
						errVal[idepth][errDistances.length - 1]) +
						errVal[idepth][errDistances.length - 1];
			else
			{
				// get distance interpolation index and weight
				wdist = getIndex(distanceDeg, errDistances, idist);
				return wdepth * (getUncertainty(wdist, idist, idepth+1) -
						getUncertainty(wdist, idist, idepth)) +
						getUncertainty(wdist, idist, idepth);
			}
		}
	}

	public double getVariance(double distance, double depth)
	{
		// Convert to degrees since model errors are defined in degrees.
		double distanceDeg = Math.toDegrees(distance);

		// initialize indices and interpolation weights
		Integer idist = 0;
		Integer idepth = 0;
		double wdist, wdepth;
		wdist = wdepth = 0.0;

		if ((errVal.length == 1) || (depth >= errDepths[errDepths.length -1]))
		{
			// if more than one depth and depth exceeds last entry set idepth
			if (errVal.length > 1) idepth = errDepths.length - 1;

			// if distance in question is greater than the max defined distance, return
			// uncertainty defined for max distance.  Otherwise, interpolate between the
			// two bracketing distances.
			if( distanceDeg >= errDistances[errDistances.length - 1])
				return errVal[idepth][errDistances.length - 1];
			else
			{
				// get distance interpolation index and weight
				wdist = getIndex(distanceDeg, errDistances, idist);
				return getVariance(wdist, idist, idepth);
			}
		}
		else
		{
			// get depth interpolation index and weight
			wdepth = getIndex(depth, errDepths, idepth);

			// if distance in question is greater than the max defined distance, return
			// uncertainty defined for max distance.  Otherwise, interpolate between the
			// two bracketing distances.
			if( distanceDeg >= errDistances[errDistances.length - 1])
				return wdepth * (errVal[idepth+1][errDistances.length - 1] -
						errVal[idepth][errDistances.length - 1]) +
						errVal[idepth][errDistances.length - 1];
			else
			{
				// get distance interpolation index and weight
				wdist = getIndex(distanceDeg, errDistances, idist);
				return wdepth * (getVariance(wdist, idist, idepth+1) -
						getVariance(wdist, idist, idepth)) +
						getVariance(wdist, idist, idepth);
			}
		}
	}

	private double getIndex(double x, double[] v, Integer index)
	{
		// if v consists of only 2 entries then set index to 0 ... otherwise,
		// find bracketing index value
		if (v.length == 2)
			index = 0;
		else
		{
			// set up interpolation increment and index start
			int inc = (int) v.length;
			inc >>= 1;
			index = inc;

			// perform binary search to find index.
			do
			{
				if (inc > 1) inc >>= 1;
				if (v[index + 1] <= x)
					index += inc;
				else if (v[index] > x)
					index -= inc;
				else
					break;
			} while (true);
		}

		// calculate weight fraction
		double w = (x - v[index]) / (v[index+1] - v[index]);

		return w;
	}

	/**
	 * Sets this objects data arrays.
	 *
	 * @param distances Distance array [distance].
	 * @param depths Depth array [depth].
	 * @param values Error value array [depth][distance].
	 */
	public void setData(double[] distances, double[] depths, double[][] values)
	{
		errDistances = distances;
		errDepths = depths;
		errVal = values;
	}

	/**
	 * Reads this SLBM uncertainty object from a file.
	 *
	 * @param fileName
	 * @param readBinary reads binary or ascii format
	 * @throws IOException
	 */
	@Override
	public void readFile(File fileName, boolean readBinary) throws IOException
	{
		if (readBinary)
			readFileBinary(fileName);
		else
			readFileAscii(fileName);
	}

	/**
	 * Reads this SLBM uncertainty object from an ascii file.
	 * File stores tt uncertainty in sec, slowness uncertainty in sec/degree,
	 * and azimuth uncertainty in degrees.
	 * Slowness and azimuth are converted to sec/radian and radians, respectively.
	 *
	 * @param fileName
	 * @throws IOException
	 */
	@Override
	public void readFileAscii(File fileName) throws IOException
	{
		Scanner input = new Scanner(fileName);
		readFileAscii(input);
		input.close();
	}

	/**
	 * Reads this SLBM uncertainty object from a binary file.
	 * File stores tt uncertainty in sec, slowness uncertainty in sec/degree,
	 * and azimuth uncertainty in degrees.
	 * Slowness and azimuth are converted to sec/radian and radians, respectively.
	 *
	 * @param fileName
	 * @throws IOException
	 */
	@Override
	public void readFileBinary(File fileName) throws IOException
	{
		DataInputStream input = new DataInputStream(new BufferedInputStream(
				new FileInputStream(fileName)));
		readFileBinary(input);
		input.close();
	}

	/**
	 * Reads this SLBM uncertainty object from an ascii file.
	 * File stores tt uncertainty in sec, slowness uncertainty in sec/degree,
	 * and azimuth uncertainty in degrees.
	 * Slowness and azimuth are converted to sec/radian and radians, respectively.
	 *
	 * @param input The input BufferedReader.
	 * @throws IOException
	 */
	protected void readFileAscii(Scanner input) throws IOException {
		// The number of depths can be zero and still have uncertainty values as
		// a function of distance.
		int numdistances = input.nextInt();
		int numdepths = input.nextInt();
		input.nextLine();

		if (numdistances > 0) {
			errDistances = new double[numdistances];
			for (int i = 0; i < numdistances; i++)
				errDistances[i] = input.nextDouble();
			input.nextLine();

			if (numdepths > 0) {
				errDepths = new double[numdepths];
				for (int i = 0; i < numdepths; i++)
					errDepths[i] = input.nextDouble();
				input.nextLine();
			}
			else
			{
				errDepths = new double[0];
				numdepths = 1;
			}

			// if attribute is 2:AZ, convert degrees to radians, 
			// if 1:SH convert sec/degree to sec/radian
			// if 0:TT no conversion
			double convert = attributeConversionFactor();

			errVal = new double[numdepths][];
			for (int j = 0; j < numdepths; ++j) {
				errVal[j] = new double[numdistances];
				input.nextLine(); // this is a '#' character.
				for (int i = 0; i < numdistances; i++)
					errVal[j][i] = input.nextDouble() * convert;
				input.nextLine();
			}
		}
		else
		{
			errDistances = new double[0];
			errDepths = new double[0];
			errVal = new double[0][];
		}
	}

	/**
	 * Reads this SLBM uncertainty object from a binary file.
	 * File stores tt uncertainty in sec, slowness uncertainty in sec/degree,
	 * and azimuth uncertainty in degrees.
	 * Slowness and azimuth are converted to sec/radian and radians, respectively.
	 *
	 * @param input
	 * @throws IOException
	 */
	@Override
	protected void readFileBinary(DataInputStream input) throws IOException
	{
		int numDistances = input.readInt();
		int numDepths = input.readInt();

		errDistances = new double [numDistances];
		errDepths = new double [numDepths];
		for (int i=0; i<errDistances.length; ++i)
			errDistances[i] = input.readDouble();
		for (int j=0; j<errDepths.length; ++j)
			errDepths[j] = input.readDouble();

		// if attribute is 2:AZ, convert degrees to radians, 
		// if 1:SH convert sec/degree to sec/radian
		// if 0:TT no conversion
		double convert = attributeConversionFactor();

		if (numDepths == 0) numDepths = 1;
		errVal = new double [numDepths][];
		for (int i=0; i < errVal.length; ++i)
		{
			errVal[i] = new double [numDistances];
			for (int j=0; j<errDistances.length; ++j)
				errVal[i][j] = input.readDouble() * convert;
		}
	}

	/**
	 * Writes this SLBM uncertainty object to a file.
	 * File stores tt uncertainty in sec, slowness uncertainty in sec/degree,
	 * and azimuth uncertainty in degrees.
	 * Slowness and azimuth in memory have units of sec/radian and radians, respectively.
	 *
	 * @param fileName
	 * @throws IOException
	 */
	@Override
	public void writeFile(File fileName, boolean writeBinary) throws IOException
	{
		if (writeBinary)
			writeFileBinary(fileName);
		else
			writeFileAscii(fileName);
	}

	/**
	 * Writes this SLBM uncertainty object to a file.
	 * File stores tt uncertainty in sec, slowness uncertainty in sec/degree,
	 * and azimuth uncertainty in degrees.
	 * Slowness and azimuth in memory have units of sec/radian and radians, respectively.
	 *
	 * @param fileName
	 * @throws IOException
	 */
	@Override
	public void writeFileAscii(File fileName) throws IOException
	{
		BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
		writeFileAscii(output);
		output.close();
	}

	/**
	 * Writes this SLBM uncertainty object to a file.
	 * File stores tt uncertainty in sec, slowness uncertainty in sec/degree,
	 * and azimuth uncertainty in degrees.
	 * Slowness and azimuth in memory have units of sec/radian and radians, respectively.
	 *
	 * @param output
	 * @throws IOException
	 */
	@Override
	protected void writeFileAscii(Writer output) throws IOException
	{
		int nlCount = 8;
		int nDistances = errDistances.length;
		int nDepths = errVal.length > 1 ? errVal.length : 0;

		output.write(String.format("%d %d%n", nDistances, nDepths));

		for (int i = 0; i < nDistances; i++)
		{
			output.write(String.format(" %5.1f", errDistances[i]));
			if (((i + 1) % nlCount == 0) ||
					(i == errDistances.length - 1))
				output.write("\n");
		}

		for (int i = 0; i < nDepths; i++)
		{
			output.write(String.format(" %5.1f", errDepths[i]));
			if (((i + 1) % nlCount == 0) ||
					(i == errDepths.length - 1))
				output.write("\n");
		}

		// if attribute is 2:AZ, convert radians to degrees, 
		// if 1:SH convert sec/radian to sec/degree
		// if 0:TT no conversion
		double convert = 1./attributeConversionFactor();

		String format = "%6.4f%n";

		for (int i = 0; i < errVal.length; i++)
		{
			output.write("#\n");
			for (int j=0; j < nDistances; ++j)
				output.write(String.format(format, errVal[i][j]*convert));
		}
	}

	/**
	 * Writes this SLBM uncertainty object to a file.
	 * File stores tt uncertainty in sec, slowness uncertainty in sec/degree,
	 * and azimuth uncertainty in degrees.
	 * Slowness and azimuth in memory have units of sec/radian and radians, respectively.
	 *
	 * @param fileName
	 * @throws IOException
	 */
	@Override
	public void writeFileBinary(File fileName) throws IOException
	{
		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(fileName)));
		writeFileBinary(output);
		output.close();
	}

	/**
	 * Writes this SLBM uncertainty object to a file.
	 * File stores tt uncertainty in sec, slowness uncertainty in sec/degree,
	 * and azimuth uncertainty in degrees.
	 * Slowness and azimuth in memory have units of sec/radian and radians, respectively.
	 *
	 * @param output
	 * @throws IOException
	 */
	@Override
	protected void writeFileBinary(DataOutputStream output) throws IOException
	{
		// if attribute is 2:AZ, convert radians to degrees, 
		// if 1:SH convert sec/radian to sec/degree
		// if 0:TT no conversion
		double convert = 1./attributeConversionFactor();

		// The number of depths can be zero and still have uncertainty values as
		// a function of distance.

		output.writeInt(errDistances.length);
		output.writeInt(errDepths.length);
		for (int i=0; i<errDistances.length; ++i)
			output.writeDouble(errDistances[i]);
		for (int j=0; j<errDepths.length; ++j)
			output.writeDouble(errDepths[j]);
		for (int i=0; i<errVal.length; ++i)
			for (int j=0; j<errDistances.length; ++j)
				output.writeDouble(errVal[i][j] * convert);
	}

	/**
	 * Conversion factors for tt, sh, az
	 *
	 * 	 0 = travel time = 1.0
	 *   1 = slowness    = 57.29577951 (convert sec/degree to sec/radian)
	 *   2 = azimuth     = 0.017453293 (convert degrees to radians)
	 *
	 * @return the conversion factor based on attribute number.
	 */
	private double attributeConversionFactor()
	{
		return (attributeNum == 2) ? Math.toRadians(1.0) :
				(attributeNum == 1) ? Math.toDegrees(1.0) : 1.;
	}


	/**
	 * Object equals test.
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (this == obj)
			return true;

		if (obj == null || obj.getClass() != this.getClass())
			return false;

		UncertaintyPIU UncertaintyPIU = (UncertaintyPIU) obj;

		//if (!this.fileName.equals(uncertaintyPathDep.fileName))
		//	return false;

		if (this.phaseNum != UncertaintyPIU.phaseNum)
			return false;

		if (this.attributeNum != UncertaintyPIU.attributeNum)
			return false;

		if ((this.errDistances != null) && (UncertaintyPIU.errDistances != null))
		{
			if (this.errDistances.length != UncertaintyPIU.errDistances.length)
				return false;

			for (int i = 0; i < this.errDistances.length; ++i)
				if (this.errDistances[i] != UncertaintyPIU.errDistances[i])
					return false;
		}

		if ((this.errDepths != null) && (UncertaintyPIU.errDepths != null))
		{
			if (this.errDepths.length != UncertaintyPIU.errDepths.length)
				return false;

			for (int i = 0; i < this.errDepths.length; ++i)
				if (this.errDepths[i] != UncertaintyPIU.errDepths[i])
					return false;
		}

		if ((this.errVal != null) && (UncertaintyPIU.errVal != null))
		{
			if (this.errVal.length != UncertaintyPIU.errVal.length)
				return false;

			for (int i = 0; i < this.errVal.length; ++i)
			{
				double[] thisError = this.errVal[i];
				double[] otherError = UncertaintyPIU.errVal[i];
				if (thisError.length != otherError.length)
					return false;

				for (int j = 0; j < thisError.length; ++j)
					if (Math.abs(thisError[j]/otherError[j] - 1.) > 1e-7)
						return false;
			}
		}

		return true;
	}

	@Override
	public boolean isPathDependent() {
		return false;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer("UncertaintyPIU Data:\n");
		if (errDistances.length >0)
			buf.append(String.format("Distances[%d] = %s%n", errDistances.length,
					Arrays.toString(errDistances)));
		if (errDepths.length >0)
			buf.append(String.format("Depths[%d] = %s%n", errDepths.length,
					Arrays.toString(errDepths)));
		for (int i=0; i<errVal.length; ++i)
			buf.append(String.format("Error Values[%d][%d] = %s%n", i, errVal[i].length,
					Arrays.toString(errVal[i])));

		return buf.toString();
	}
}
