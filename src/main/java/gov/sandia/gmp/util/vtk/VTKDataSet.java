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
package gov.sandia.gmp.util.vtk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * This class facilitates writing a VTK file.  It is assumed the caller has a set of points
 * (each point is a 3-element vector).  The caller also supplies a list of VTKCell
 * objects, each of which specifies a vtk cell that includes the cell type (triangle, quad, etc.)
 * and the indices of the points that define the cell.  The caller can optionally specify a bunch of 
 * attribute data to attach to each point.  Given all this information, this class will write the 
 * information to an output file in vtk format suitable for viewing with ParaView.
 * <p>
 * Here a code snippet that writes some data from GeoTessModel to a vtk file:
 * <p>
 * <br>GeoTessModel model = new GeoTessModel("C:\\Users\\sballar\\git\\geo-tess-java\\resources\\permanent_files\\crust20.geotess");
 * <br>System.out.println(model);
 * <br>
 * <br>GeoTessGrid grid = model.getGrid();
 * <br>
 * <br>ArrayList<VTKCell> cells = new ArrayList<VTKCell>(10000);
 * <br>int tessId= 0;
 * <br>int level = grid.getLastLevel(tessId);
 * <br>for (int t=grid.getFirstTriangle(tessId, level); t <= grid.getLastTriangle(tessId, level); ++t)
 * <br>    cells.add(new VTKCell(VTKCellType.VTK_TRIANGLE, grid.getTriangleVertexIndexes(t)));
 * <br>
 * <br>float[][] data = new float[grid.getNVertices()][model.getNAttributes()];
 * <br>for (int i=0; i<grid.getNVertices(); ++i)
 * <br>	for (int j=0; j<model.getNAttributes(); ++j)
 * <br>		data[i][j] = model.getProfile(i, 0).getDataTop().getFloat(j);
 * <br>
 * <br>VTKDataSet.write(new File("C:\\Users\\sballar\\Desktop\\deleteme.vtk"), grid.getVertices(), cells, 
 * <br>		model.getMetaData().getAttributeNames(), data);
 * <br>
 * @author sballar
 */
public class VTKDataSet 
{
	private List<VTKCell> cells;
	private List<double[]> points;
	private List<String> attributeNames;
	private List<float[]> attributes;
	
	public VTKDataSet(File f) throws Exception	{
		read(f);
	}
	
	/**
	 * Write a vtk dataset to an output file.
	 * @param outputFile name of the file to receive the vtk output
	 * @param points the list of 3-component points to be written to the output file
	 * @param cells a List of VTKCell objects that define the indices of the points that comprise each cell.
	 * @param attributeNames (optional - may be null) the names of the data attributes
	 * @param attributes (optional - may be null) the 2D array of attribute values.  
	 * The first dimension must be equal to the  size of the points array.  
	 * The second dimension must be equal to the number of attributeNames.
	 * @throws IOException
	 */
	static public void write(File outputFile, List<double[]> points, Collection<VTKCell> cells,
			List<String> attributeNames, List<float[]> attributes) throws IOException
	{
		DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));

		writeBytes(output,String.format("# vtk DataFile Version 2.0%n"));
		writeBytes(output,String.format("VTK DataSet%n"));
		writeBytes(output,String.format("BINARY%n"));
		writeBytes(output,String.format("DATASET UNSTRUCTURED_GRID%n"));
		writeBytes(output,String.format("POINTS %d double%n", points.size()));

		for (double[] point : points)
		{
			output.writeDouble(point[0]);
			output.writeDouble(point[1]);
			output.writeDouble(point[2]);
		}

		int totalPoints = 0;
		for (VTKCell cell : cells)
			totalPoints += cell.size();

		writeBytes(output,String.format("CELLS %d %d%n", cells.size(), totalPoints+cells.size()));
		for (VTKCell cell : cells) cell.writeCell(output);

		writeBytes(output,String.format("CELL_TYPES %d%n", cells.size()));
		for (VTKCell cell : cells) 
		  cell.writeCellType(output);

		if (attributeNames != null)
		{
			writeBytes(output,String.format("POINT_DATA %d%n", points.size()));

			for (int a = 0; a < attributeNames.size(); ++a)
			{
				writeBytes(output, String.format("SCALARS %s float 1%nLOOKUP_TABLE default%n",
						attributeNames.get(a).replaceAll(" ", "_")));

				for (int i = 0; i < attributes.size(); ++i)
					output.writeFloat(attributes.get(i)[a]);
				
			}
		}
		
		output.close();
	}
	
	/**
	 * Write a vtk dataset to an output file.
	 * @param outputFile name of the file to receive the vtk output
	 * @param points the array of 3-component points to be written to the output file
	 * @param cells a List of VTKCell objects that define the indices of the points that comprise each cell.
	 * @param attributeNames (optional - may be null) the names of the data attributes
	 * @param attributes (optional - may be null) the 2D array of attribute values.  
	 * The first dimension must be equal to the  size of the points array.  
	 * The second dimension must be equal to the number of attributeNames.
	 * @throws IOException
	 */
	public static void write(File outputFile, double[][] points, VTKCell[] cells, 
			String[] attributeNames, float[][] attributes) throws IOException 
	{ write(outputFile, Arrays.asList(points), Arrays.asList(cells), Arrays.asList(attributeNames), Arrays.asList(attributes)); }

	/**
	 * Write a vtk dataset to an output file.
	 * @param outputFile name of the file to receive the vtk output
	 * @param points the list of 3-component points to be written to the output file
	 * @param cells a List of VTKCell objects that define the indices of the points that comprise each cell.
	 * @throws IOException
	 */
	static public void write(File outputFile, List<double[]> points, Collection<VTKCell> cells) throws IOException
	{ write(outputFile, points, cells, null, null);	}

	/**
	 * Write a vtk dataset to an output file.
	 * @param outputFile name of the file to receive the vtk output
	 * @param points the array of 3-component points to be written to the output file
	 * @param cells a List of VTKCell objects that define the indices of the points that comprise each cell.
	 * @throws IOException
	 */
	public static void write(File outputFile, double[][] points, VTKCell[] cells) throws IOException 
	{ write(outputFile, Arrays.asList(points), Arrays.asList(cells), null, null); }


	private static void writeBytes(DataOutputStream output, String s) throws IOException
	{ output.writeBytes(s); }

	public List<VTKCell> getCells() {
		return cells;
	}

	public void setCells(List<VTKCell> cells) {
		this.cells = cells;
	}

	public List<String> getAttributeNames() {
		return attributeNames;
	}

	public void setAttributeNames(List<String> attributeNames) {
		this.attributeNames = attributeNames;
	}

	public List<float[]> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<float[]> attributes) {
		this.attributes = attributes;
	}

	public List<double[]> getPoints() {
		return points;
	}

	public void setPoints(List<double[]> points) {
		this.points = points;
	}

	public void write(File file) throws Exception {
		if (attributeNames != null)
			VTKDataSet.write(file, points, cells, attributeNames, attributes);
		else
			VTKDataSet.write(file, points, cells);
	}

	@SuppressWarnings("deprecation")
	public void read(File f) throws Exception
	{
		DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		String line = input.readLine();
		while (!line.startsWith("POINTS"))
			line = input.readLine();
		Scanner scnr = new Scanner(line);
		scnr.next();
		int npoints = scnr.nextInt();
		scnr.close();
		
		points = new ArrayList<>(npoints);
		for (int i=0; i<npoints; ++i)
			points.add(new double[] {input.readDouble(), input.readDouble(), input.readDouble()});
		
		line = input.readLine();
		if (!line.startsWith("CELLS"))
		{
			input.close();
			throw new Exception("Expected to find line starting with 'CELLS'");
		}
		
		scnr = new Scanner(line);
		scnr.next();
		int ncells = scnr.nextInt();
		scnr.close();
		int[][] pointIndeces = new int[ncells][];
		for (int i=0; i<ncells; ++i)
		{
			int[] idx = new int[input.readInt()];
			for (int j=0; j<idx.length; ++j)
				idx[j] = input.readInt();
			pointIndeces[i] = idx;
		}
		
		line = input.readLine();
		if (!line.startsWith("CELL_TYPES"))
		{
			input.close();
			throw new Exception("Expected to find line starting with 'CELL_TYPES'");
		}
		
		cells = new ArrayList<>(ncells);
		for (int i=0; i<ncells; ++i)
		{
		    int idx = input.readInt();
			VTKCellType cellType = VTKCellType.values()[idx-1];
			VTKCell cell = new VTKCell(cellType, pointIndeces[i]);
			cells.add(cell);
		}
		
		if (input.available() > 0)
		{
			line = input.readLine();
			if (!line.startsWith("POINT_DATA"))
			{
				input.close();
				throw new Exception("Expected to find line starting with 'POINT_DATA'");
			}
			attributeNames = new ArrayList<>();
			attributes = new ArrayList<>();
			while (input.available() > 0)
			{
				scnr = new Scanner(input.readLine());
				if (!scnr.next().equals("SCALARS"))
				{
					scnr.close();
					input.close();
					throw new Exception("Expected to find line starting with 'SCALARS'");
				}
				attributeNames.add(scnr.next());
				String type = scnr.next();
				input.readLine();
				float[] floats = new float[npoints];
				if (type.equalsIgnoreCase("float"))
					for (int i=0; i<npoints; ++i)
						floats[i] = input.readFloat();
				attributes.add(floats);
			}
		
		}
		input.close();
	}
	
}
