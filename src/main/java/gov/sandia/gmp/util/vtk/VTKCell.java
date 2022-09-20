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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;

public class VTKCell 
{
	private VTKCellType vtkType;
	private int[] pointIndices;
	
	/**
	 * Create a new VTKCell of the specified type. The assumption is that the caller
	 * is maintaining a list of points and this cell consists of a subset of those
	 * points.  The list of pointIndices maintained by this VTKCell are indices into
	 * the caller's list of points.
	 * @param vtkType the VTKCellType.  For definitions see 
	 * http://www.vtk.org/wp-content/uploads/2015/04/file-formats.pdf 
	 * figures 2 and 3 
	 * @param pointIndices this constructor keeps a reference to the supplied array.
	 * @throws Exception if the VTKCellType requires a specific number of pointIndices,
	 * and the number of pointIndices is incorrect, this constructor throws an exception.
	 */
	public VTKCell(VTKCellType vtkType, int[] pointIndices) throws Exception
	{
		if (vtkType.getNPoints() > 0 && pointIndices.length != vtkType.getNPoints())
			throw new Exception(String.format("%nA VTKCell of type %s expects %d pointIndices but pointIndices.length = %d%n",
					vtkType.toString(), vtkType.getNPoints(), pointIndices.length));
		
		this.vtkType = vtkType;
		this.pointIndices = pointIndices;
	}
	
	/**
	 * Create a new VTKCell of the specified type. The assumption is that the caller
	 * is maintaining a list of points and this cell consists of a subset of those
	 * points.  The list of pointIndices maintained by this VTKCell are indices into
	 * the caller's list of points.
	 * @param vtkType the VTKCellType.  For definitions see 
	 * http://www.vtk.org/wp-content/uploads/2015/04/file-formats.pdf 
	 * figures 2 and 3 
	 * @param pointIndices this constructor makes a copy of the supplied list
	 * @throws Exception if the VTKCellType requires a specific number of pointIndices,
	 * and the number of pointIndices is incorrect, this constructor throws an exception.
	 */
	public VTKCell(VTKCellType vtkType, ArrayListInt pointIndices) throws Exception
	{ this(vtkType, pointIndices.toArray());	}
	
	/**
	 * Create a new VTKCell of the specified type. The assumption is that the caller
	 * is maintaining a list of points and this cell consists of a subset of those
	 * points.  The list of pointIndices maintained by this VTKCell are indices into
	 * the caller's list of points.
	 * @param vtkType the VTKCellType.  For definitions see 
	 * http://www.vtk.org/wp-content/uploads/2015/04/file-formats.pdf 
	 * figures 2 and 3 
	 * @param pointIndices this constructor makes a copy of the supplied list
	 * @throws Exception if the VTKCellType requires a specific number of pointIndices,
	 * and the number of pointIndices is incorrect, this constructor throws an exception.
	 */
	public VTKCell(VTKCellType vtkType, ArrayList<Integer> pointIndices) throws Exception
	{ 
		this.pointIndices = new int[pointIndices.size()];
		for (int i=0; i<pointIndices.size(); ++i)
			this.pointIndices[i] = pointIndices.get(i);
		
		if (vtkType.getNPoints() > 0 && this.pointIndices.length != vtkType.getNPoints())
			throw new Exception(String.format("%nA VTKCell of type %s expects %d pointIndices but pointIndices.length = %d%n",
					vtkType.toString(), vtkType.getNPoints(), this.pointIndices.length));
		
		this.vtkType = vtkType;
	}
	
	/**
	 * The number of pointIndices that comprise this VTKCell object.
	 * @return
	 */
	public int size() { return pointIndices.length; }
	
	/**
	 * Write the contents of this VTKCell to the output stream
	 * @param output
	 * @throws IOException
	 */
	protected void writeCell(DataOutputStream output) throws IOException
	{
		output.writeInt(pointIndices.length);
		for (int pointIndex : pointIndices) output.writeInt(pointIndex);
	}

	/**
	 * Write the index of the vtk type to the output stream.
	 * @param output
	 * @throws IOException
	 */
	protected void writeCellType(DataOutputStream output) throws IOException
	{ output.writeInt(vtkType.getIndex()); }
	
	@Override
	public String toString() {
	  return String.format("%s %n", vtkType.toString(), pointIndices.length);
	}
}
