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
package gov.sandia.gmp.util.numerical.simplex;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Scanner;

public class Amoeba
{
	public int index;
	public double[][] p;
	public double[] y;
	
	double[] phi;
	double[] plo;
	
	/**
	 * The centroid of all points except the one with
	 * the highest y value (phi).
	 */
	double[] centroid;
	
	int ndim;
	int npoints;
	
	/**
	 * 
	 * @param p0
	 * @param dp
	 */
	public Amoeba(double[] p0, double[] dp)
	{
		npoints = p0.length+1;
		ndim = p0.length;
		
		index = 0;
		p = new double[npoints][ndim];
		y = new double[npoints];
		centroid = new double[ndim];
		set(p0, dp);
	}
	
	/**
	 * Construct an Amoeba specifying the coordinates of the corners of the initial simplex.
	 * @param p0 an ndim+1 x ndim array containing the coordinates of the initial simplex.
	 * @throws Exception if (p0.length != p0[0].length)
	 */
	public Amoeba(double[][] p0) throws Exception
	{
		index = 0;
		npoints = p0.length;
		ndim = p0[0].length;
		if (npoints != ndim+1)
			throw new Exception(String.format("npoints != ndim+1.  npoints=%d, ndim=%d", npoints, ndim));
		this.p = p0;
		y = new double[p.length];
		centroid = new double[ndim];
	}
	
	public Amoeba(String shape)
	{
		// 0 [90.0, 10.0] [91.0, 10.0] [90.0, 11.0] [90.55385138137417, 91.54780172128658, 90.6697303403953]

		Scanner scanner = new Scanner(shape);
		index = scanner.nextInt();
		String line = scanner.nextLine().trim();
		scanner.close();
		
		line = line.substring(1, line.length()-1);
		String[] arrays = line.split("\\] \\[");
		
		int j;
		npoints = arrays.length-1;
		ndim = arrays.length-2;
		p = new double[npoints][ndim];
		for (int i=0; i<p.length; ++i)
		{
			j=0;
			for (String s : arrays[i].split(", "))
				p[i][j++] = Double.parseDouble(s);
		}
		y = new double[npoints];
		j=0;
		for (String s : arrays[arrays.length-1].split(", "))
			y[j++] = Double.parseDouble(s);
		centroid = new double[ndim];
	}
	
	public void redefine()
	{
		// defined by extended Amoeba types.
	}

	/**
	 * Set up an initial amoeba.  Assume we want to optimize
	 * a system consisting of ndim parameters.  The amoeba for 
	 * such a system will consist of ndim+1 points.  This method
	 * takes an initial point in the ndim parameter space (p0) and 
	 * a step size in each of the ndim dimensions (dp), and builds 
	 * an amoeba with ndim+1 points.  The first point will be a copy
	 * of p0.  Each of the other points will be a copy of p0 with 
	 * the value of dp[i] added to the i'th dimension of p0[i+1].
	 * @param p0 a 1D array with ndim elements representing one
	 * point in the ndim parameter space.
	 * @param dp a 1D array with ndim elements specifying a step
	 * size in each of the ndim dimensions of parameter space.
	 */
	public void set(double[] p0, double[] dp) 
	{
		for (int i=0; i<npoints; ++i)
			for (int j=0; j<ndim; ++j)
				p[i][j] = p0[j];

		for (int i=0; i<ndim; ++i)
			p[i+1][i] += dp[i];
	}
	
	/**
	 * The number of points that define the simplex.
	 * Equals number of model paramters + 1
	 * @return number of points that define the simplex.
	 */
	public int npoints() { return npoints; }
	
	public int nparameters() { return ndim; }
	
	/**
	 */
	double[][] getTestPoints() 
	{
		double[][] newPoints = new double[3+ndim][ndim];
		
		// reflection
		getTestPoint(1, newPoints[0]);
		
		// expansion
		getTestPoint(2, newPoints[1]);
		
		// 1D contraction
		getTestPoint(-.5, newPoints[2]);

		// Try a contraction along all dimensions toward the low point.
		for (int i = 1; i < npoints; i++)
		{
			double[] newPoint = new double[ndim];
			for (int j = 0; j < ndim; j++)
				newPoint[j] = plo[j] + 0.5 * (p[i][j] - plo[j]);
			newPoints[i+2] = newPoint;
		}

		return newPoints;
	}
	
	void getTestPoint(double factor, double[] point)
	{
		for (int j = 0; j < ndim; j++)
			point[j] = centroid[j] + (centroid[j]-phi[j]) * factor;
	}
	
	void replaceHighPoint(double[] pnew, double ynew) throws Exception
	{ replace(p.length-1, pnew, ynew); }

	protected void replace(int i, double[] pnew, double ynew) throws Exception
	{
		for (int j=0; j<ndim; ++j)
			p[i][j] = pnew[j];
		y[i] = ynew;
	}

	/**
	 * Sort the points of the simplex into order of 
	 * increasing y value.
	 * @throws Exception 
	 */
	void sort() throws Exception
	{
		int jmin;
		for (int i=0; i<y.length-1; ++i)
		{
			jmin = i;
			for (int j=i+1; j<y.length; ++j)
				if (y[j] < y[jmin]) jmin=j;
			if (jmin != i)
				swap(i,jmin);
		}
		
		phi = p[p.length-1];
		plo = p[0];
		
		// compute the centroid of all the points except
		// the one with highest y value
		for (int j = 0; j < ndim; j++)
		{
			centroid[j] = 0.0;
			for (int i = 0; i < p.length-1; i++)
				centroid[j] += p[i][j];
			centroid[j] /= p.length-1;
		}		
	}
	
	protected void swap(int i, int j)
	{
		double yi = y[i];
		y[i] = y[j];
		y[j]= yi;
		double[] pi = p[i];
		p[i] = p[j];
		p[j] = pi;
	}
	
	/**
	 * Returns true if the current sorted worst - best is < tolerance.
	 * 
	 * @param tolerance Input covergence tolerance.
	 * @return True if the current sorted worst - best is < tolerance.
	 */
	public boolean isConverged(double tolerance)
	{
		if (y[y.length-1] - y[0] < tolerance)
			return true;
		else
			return false;
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(String.format("%d", index));
		for (int i=0; i<p.length; ++i)
			buf.append(" ").append(Arrays.toString(p[i]));
		buf.append(" ").append(Arrays.toString(y));
		return buf.toString();
	}
	
	public void vtk(File dir, String fileName) throws Exception
	{
		int npoints = p.length;
		int ndim = p.length-1;
		
		if (fileName.contains("<index>"))
			fileName = fileName.replace("<index>", String.valueOf(index));
		
		DataOutputStream output = new DataOutputStream(
				new BufferedOutputStream(new FileOutputStream(
						new File(dir, fileName))));

		output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
		output.writeBytes(String.format("SimplexShape%n"));
		output.writeBytes(String.format("BINARY%n"));

		output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

		output.writeBytes(String.format("POINTS %d double%n", npoints));

		for (int i = 0; i < npoints; ++i)
		{
			output.writeDouble(p[i][0]);
			output.writeDouble(ndim > 1 ? p[i][1] : 0.);
			output.writeDouble(ndim > 2 ? p[i][2] : 0.);
		}
		
		if (npoints == 3)
		{
			// make a vtk triangle
			output.writeBytes("CELLS 1 4\n");
			output.writeInt(3);
			output.writeInt(0);
			output.writeInt(1);
			output.writeInt(2);
			output.writeBytes("CELL_TYPES 1\n");
			output.writeInt(5); // vtk_triangle
		}
		else if (npoints == 4)
		{
			// make a vtk tetrahedron
			output.writeBytes("CELLS 1 5\n");
			output.writeInt(4);
			output.writeInt(0);
			output.writeInt(1);
			output.writeInt(2);
			output.writeInt(3);
			output.writeBytes("CELL_TYPES 1\n");
			output.writeInt(10); // vtk_tetra
		}
		else if (npoints > 4)
		{
			// make a whole bunch of line segments
			int nlines = 0;
			for (int i=0; i<npoints-1; ++i)
				for (int j=i+1; j<npoints; ++j)
					++nlines;
			output.writeBytes(String.format("CELLS %d %d%n", nlines, nlines*3));
			
			for (int i=0; i<npoints-1; ++i)
				for (int j=i+1; j<npoints; ++j)
				{
					output.writeInt(2);
					output.writeInt(i);
					output.writeInt(j);
				}
			output.writeBytes(String.format("CELL_TYPES %d%n", nlines));
			for (int i=0; i<nlines; ++i) output.writeInt(3); // vtk_line
			
		}
		output.close();
	}

}
