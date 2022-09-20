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
package gov.sandia.gmp.baseobjects.tttables;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import gov.sandia.gmp.util.exceptions.GMPException;

public abstract class Table {
	protected File file;

	/**
	 * distances, in degrees, at which values are stored in tables
	 */
	protected double[] distances;

	/**
	 * depths, in km, at which values are stored in tables
	 */
	protected double[] depths;

	/**
	 * values at various distances and depths.
	 */
	protected double[][] values;

	public Table() {
	}

	abstract public Table read(File inputFile) throws GMPException, IOException;

	abstract public double interpolate(double distance, double depth) throws OutOfRangeException;

	protected int hunt(double[] values, double x) {
		if (x == values[values.length - 1])
			return values.length - 2;

		int i;
		int bot = -1;
		int top = values.length;
		while (top - bot > 1) {
			i = (top + bot) / 2;
			if (x >= values[i])
				bot = i;
			else
				top = i;
		}
		return bot;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @return the distances
	 */
	public double[] getDistances() {
		return distances;
	}

	public void setDistances(double[] distances) {
		this.distances = distances;
	}

	public void setDepths(double[] depths) {
		this.depths = depths;
	}

	public void setValues(double[][] values) {
		this.values = values;
	}

	/**
	 * @return the depths
	 */
	public double[] getDepths() {
		return depths;
	}

	/**
	 * @return the values (nDistances x nDepths)
	 */
	public double[][] getValues() {
		return values;
	}

	public String staticInit() {
		StringBuffer buf = new StringBuffer();
		buf.append("double[] distances = new double[] {\n");
		String sep = "";
		for (double d : distances) {
			buf.append(String.format("%s%1.2f", sep, d));
			sep = ", ";
		}
		buf.append("};\n\n");

		buf.append("double[] depths = new double[] {\n");
		sep = "";
		for (double d : depths) {
			buf.append(String.format("%s%1.2f", sep, d));
			sep = ", ";
		}
		buf.append("};\n\n");

		buf.append("double[][] values = new double[][] {\n");

		for (int i = 0; i < depths.length; ++i) {
			sep = "";
			buf.append("{");
			for (int j = 0; j < distances.length; ++j) {
				buf.append(String.format("%s%1.3f", sep, values[i][j]));
				sep = ", ";
			}
			if (i < depths.length - 1)
				buf.append("},\n");
			else
				buf.append("}\n");
		}
		buf.append("};\n");

		return buf.toString();
	}

	public void vtk(File outputFile) throws Exception {
		double[] x = getDistances();
		double[] z = getDepths();
		double[][] v = getValues();
		int npoints = x.length * z.length;

		// System.out.printf("nx = %d, nz = %d, npoints = %d %n%n", x.length, z.length,
		// npoints);

		DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("TableValues%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

		output.writeBytes(String.format("POINTS %d double%n", npoints));

		// iterate over all the grid vertices and write out their position
		for (int j = 0; j < z.length; ++j)
			for (int i = 0; i < x.length; ++i) {
				output.writeDouble(x[i]);
				output.writeDouble(-z[j]);
				output.writeDouble(0.);
			}

		// write out node connectivity
		int nquads = (x.length - 1) * (z.length - 1);
		output.writeBytes(String.format("CELLS %d %d%n", nquads, nquads * 5));

		for (int j = 0; j < z.length - 1; ++j)
			for (int i = 0; i < x.length - 1; ++i) {
				output.writeInt(4);

				int ll = j * x.length + i;
				int lr = j * x.length + i + 1;
				int ur = (j + 1) * x.length + i + 1;
				int ul = (j + 1) * x.length + i;
				output.writeInt(ll);
				output.writeInt(lr);
				output.writeInt(ur);
				output.writeInt(ul);
			}

		output.writeBytes(String.format("CELL_TYPES %d%n", nquads));
		for (int t = 0; t < nquads; ++t)
			output.writeInt(9); // vtk_quad

		output.writeBytes(String.format("POINT_DATA %d%n", npoints));

		output.writeBytes(String.format("SCALARS %s float 1%n", "travel_time_(sec)"));
		output.writeBytes(String.format("LOOKUP_TABLE default%n"));

		for (int j = 0; j < z.length; ++j)
			for (int i = 0; i < x.length; ++i)
				output.writeFloat(v[j][i] < 0 ? Float.NaN : (float) v[j][i]);

		output.close();
	}

}
