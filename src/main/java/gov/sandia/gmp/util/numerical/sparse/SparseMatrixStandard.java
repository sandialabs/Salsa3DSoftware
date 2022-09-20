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
package gov.sandia.gmp.util.numerical.sparse;

import java.io.File;
import java.io.IOException;

import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.numerical.intrinsicsort.IntrinsicSort;

/**
 * Stores a sparse matrix in one or more of three standard sparse matrix
 * representations including triplet (row, column, value arrays), Compressed
 * Sparse Column or CSC (compressed sorted column, row, value arrays), or
 * Compressed Sparse Row or CSR (compressed sorted row, column, value arrays).
 * 
 * Any one of, or all three representations may exist simultaneously. When
 * inserting new elements the triplet form is automatically created if it does
 * not exist. Elements are only inserted into triplet form and only retrieved
 * in CSC or CSR form.
 * 
 * Sparse Vector objects can wrap the CSR or CSC forms to provide
 * convenient SparseMatrix row or column access without incurring any
 * significant additional storage penalty. Methods are provided to create these
 * arrays for either CSR or CSC representations.
 * 
 * Unlike the SparseMatrixHuge container, the Standard form can only hold
 * an entry count that does not exceed the size of Integer.MAX_VALUE. Generally,
 * since most intrinsic types are more than one byte, the actual allocation
 * size is usually much less (e.g. 1/4 for ints, 1/8 for longs and doubles).
 * 
 * The following public methods are available for use by a client:
 * 
 *      // Adds a new entry to the sparse matrix
 *      add(row, col, val)
 *
 *      // Returns the length of row 'row'.
 *      getCSRVectorLength(row)
 * 
 *      // Get the compressed sparse row value at 'row' and entry 'index'.
 *      getCSRValue(row, index)
 * 
 *      // Set the compressed sparse row value to 'val' at 'row' and entry 'index'.
 *      setCSRValue(row, index, val)
 *      
 *      // Get the compressed sparse row column index at 'row' and entry 'index'.
 *      getCSRIndex(row, index)
 * 
 *      // Set the compressed sparse row column index to 'col' at 'row' and
 *      // entry 'index'.
 *      setCSRCol(row, index, col)
 *
 *      // Sorts CSR row 'row'
 *      sortCSRRow(row)
 *
 *      // Returns the length of column 'col'.
 *      getCSCVectorLength(col)
 *      
 *      // Get the compressed sparse column value at 'col' and entry 'index'.
 *      getCSCValue(col, index)
 * 
 *      // Set the compressed sparse column value to 'val' at 'col' and entry 'index'.
 *      setCSCValue(col, index, val)
 *      
 *      // Get the compressed sparse column row index at 'col' and entry 'index'.
 *      getCSCIndex(col, index)
 * 
 *      // Set the compressed sparse column row index to 'row' at 'col' and
 *      // entry 'index'.
 *      setCSCRow(col, index, row)
 *
 *      // Sorts CSC column 'col'
 *      sortCSCCol(col)
 *      
 *      // Clears the entire array and readies for triplet input
 *      clear()
 *      
 *      // Clears just the triplet storage.
 *      clearTriplet()
 * 
 *      // Clears just the Compressed Sparse Row (CSR) storage.
 *      clearCSR()
 *      
 *      // Clears just the Compressed Sparse Column (CSC) storage.
 *      clearCSC()
 *      
 *      // Creates the triplet representation if it does not exist.
 *      createTriplet()
 *      
 *      // Creates the CSR representation if it does not exist.
 *      createCSR()
 *      
 *      // Creates the CSC representation if it does not exist.
 *      createCSC()
 *      
 *      // Constructs an array of Sparse Vectors (SV) in CSR form.
 *      buildCSR_SV();
 *      
 *      // Constructs an array of Sparse Vectors (SV) in CSC form.
 *      buildCSC_SV();
 *      
 *      // Transposes the sparse matrix (swaps rows and columns)
 *      transpose()
 *      
 *      // Returns true if triplet representation is allocated.
 *      tripletExists()
 *      
 *      // Returns true if CSR representation is allocated.
 *      CSRExists()
 *      
 *      // Returns true if CSC representation is allocated.
 *      CSCExists()
 *      
 *      // Returns total entry count in the sparse matrix as a double
 *      entries()
 *      
 *      // Returns total entry count in the sparse matrix
 *      entryCount()
 *      
 *      // Returns the mean column entry count in the sparse matrix.
 *      meanColEntryCount()
 *      
 *      // Returns the mean row entry count in the sparse matrix.
 *      meanRowEntryCount()
 *      
 *      // Returns the number of rows in the sparse matrix.
 *      getMaxRow()
 *      
 *      // Returns the number of columns in the sparse matrix.
 *      getMaxCol()
 *      
 *      // Reads the sparse matrix from the input file name.
 *      read(String filename)
 *      
 *      // Writes the sparse matrix to the provided file name.
 *      write(String filename)
 * 
 * 
 * @author jrhipp
 *
 */
public class SparseMatrixStandard extends SparseMatrix
{
	/**
	 * The triplet row list.
	 */
	private ArrayListInt    rowsTRP     = null;

	/**
	 * The triplet column list.
	 */
	private ArrayListInt    colsTRP     = null;

	/**
	 * The triplet value list.
	 */
	private ArrayListDouble valsTRP     = null;

	/**
	 * The CSR compressed row list.
	 */
	private ArrayListInt    rowsPtrsCSR = null;

	/**
	 * The CSR column list.
	 */
	private ArrayListInt    colsCSR     = null; // = colsTRP with row/column sort

	/**
	 * The CSR value list.
	 */
	private ArrayListDouble valsCSR     = null; // = valsTRP with row/column sort

	/**
	 * The CSC compressed column list.
	 */
	private ArrayListInt    colsPtrsCSC = null;

	/**
	 * The CSC row list.
	 */
	private ArrayListInt    rowsCSC     = null; // = rowsTRP with column/row sort

	/**
	 * The CSC value list.
	 */
	private ArrayListDouble valsCSC     = null; // = valsTRP with column/row sort

	/**
	 * Standard constructor. Creates a new empty sparse matrix in triplet form.
	 */
  public SparseMatrixStandard()
  {
  	resetTRPEmpty();
  }

  /**
   * Reads a sparse matrix from the file stored by the input name.
   * 
   * @param filename The input file to read.
   * @throws IOException
   */
  public SparseMatrixStandard(String filename) throws IOException
  {
  	read(filename);
  }

  /**
   * Builds an array of SparseVector objects, one for each row in
   * the sparse matrix.
   * 
   * @return The array of SparseVector objects.
   * @throws IOException
   */
  @Override
	public SparseVector[] buildCSR_SV() throws IOException
	{
		// make sure the CSR representation exists and create the compressed sparse
		// vector array
		
		createCSR();
		SparseVector[] sv = new SparseVector [maxRowIndex];

		// fill the sparse vector array

		for (int row = 0; row < maxRowIndex; ++row)
			sv[row] = new SparseVector(rowsPtrsCSR, colsCSR, valsCSR, row);

		return sv;
	}

  /**
   * Builds an array of SparseVector objects, one for each column in
   * the sparse matrix.
   * 
   * @return The array of SparseVector objects.
   * @throws IOException
   */
  @Override
	public SparseVector[] buildCSC_SV() throws IOException
	{
		// make sure the CSC representation exists and create the compressed sparse
		// vector array

		createCSC();
		SparseVector[] sv = new SparseVector [maxColIndex];

		// fill the sparse vector array

		for (int col = 0; col < maxColIndex; ++col)
			sv[col] = new SparseVector(colsPtrsCSC, rowsCSC, valsCSC, col);
		
		return sv;
	}

	/**
	 * Adds a new entry to the sparse matrix at the input row and column. If the
	 * triplet form does not exist it is recreated before the element is added.
	 * Also, the CSR and CSC forms are destroyed, as they are no longer valid.
	 * The triplet form is set to unsorted after the insertion.
	 * 
	 * @param row   The row index of the new entry.
	 * @param col   The column index of the new entry.
	 * @param value The new entries value.
	 * @throws IOException
	 */
  @Override
  public void add(int row, int col, double value) throws IOException
  {
  	if (!tripletExists()) createTriplet();

  	addedInput = true;
  	if (maxRowIndex < row + 1) maxRowIndex = row + 1;
  	if (maxColIndex < col + 1) maxColIndex = col + 1;

  	rowsTRP.add(row);
  	colsTRP.add(col);
  	valsTRP.add(value);
  }

  /**
   * Returns the length of the vector assigned to input CSR 'row'.
   * 
   * @param row The row for which the vector length will be returned.
   * @return The length of the vector assigned to input CSR 'row'.
   * @throws IOException
   */
  @Override
  public int getCSRVectorLength(int row) throws IOException
  {
  	createCSR();
  	return rowsPtrsCSR.get(row + 1) - rowsPtrsCSR.get(row);
  }

  /**
   * Return the value stored at the input 'row' index and the column offset
   * 'index' from a CSR representation.
   * 
   * @param row    The matrix row index,
   * @param index  The Sparse Vector (SV) column index from a CSR
   *               sorted representation.
   * @return       The value stored at the input 'row' index and the column
   *               offset 'index'.
   * @throws IOException
   */
  @Override
  public double getCSRValue(int row, int index) throws IOException
  {
  	createCSR();
  	int i = getCSElementIndex(rowsPtrsCSR, colsCSR, row, index);
  	return valsCSR.get(i);
  }

  /**
   * Sets the input 'val' into the location at the input 'row' index and the
   * column offset 'index' for a CSR representation.
   * 
   * @param row    The matrix row index,
   * @param index  The Sparse Vector (SV) column index from a CSR
   *               sorted representation.
   * @param val    The new value to be set.
   * @return       The previous value stored at the input 'row' index and the
   *               column offset 'index'.
   * @throws IOException
   */
  @Override
  public double setCSRValue(int row, int index, double val) throws IOException
  {
  	createCSR();
  	int i = getCSElementIndex(rowsPtrsCSR, colsCSR, row, index);
  	double tmp = valsCSR.get(i);
  	valsCSR.set(index,  val);
  	return tmp;
  }

  /**
   * Return the column stored at the input 'row' index and the column offset
   * 'index' from a CSR representation.
   * 
   * @param row    The matrix row index,
   * @param index  The Sparse Vector (SV) column index from a CSR
   *               sorted representation.
   * @return       The column index stored at the input 'row' index and the
   *               the column offset 'index'.
   * @throws IOException
   */
  @Override
  public double getCSRColumn(int row, int index) throws IOException
  {
  	createCSR();
  	int i = getCSElementIndex(rowsPtrsCSR, colsCSR, row, index);
  	return colsCSR.get(i);
  }

  /**
   * Sets the column index stored at the input 'row' index and the column offset
   * 'index' for a CSR representation. Note: This destroys the sort order of the
   * Sparse Vector (SV) for the input 'row'. Call sortCSRRow(int row) after
   * finishing to restore the SV for the input 'row' to sorted order.
   * 
   * @param row    The matrix row index,
   * @param index  The Sparse Vector (SV) column index from a CSR
   *               sorted representation.
   * @param column The column index to be set.
   * @return       The previous column index stored at the input 'row' index
   *               and the column offset 'index'.
   * @throws IOException
   */
  @Override
  public int setCSRColumn(int row, int index, int column) throws IOException
  {
  	createCSR();
  	int i = getCSElementIndex(rowsPtrsCSR, colsCSR, row, index);
  	int tmp = colsCSR.get(i);
  	colsCSR.set(index,  column);
  	return tmp;
  }

  /**
   * Sorts the sparse vector associated with the input row. If CSR does not
   * exist it is created (which automatically sorts at creation).
   * 
   * @param row The CSR row to be sorted.
   * @throws IOException
   */
  @Override
  public void sortCSRRow(int row) throws IOException
  {
  	if (!CSRExists())
  	{
  		// Doesn't exist ... create

  		createCSR();
  		return;
  	}
  	else
  	{
  		// CSR already exists ... the input row will be sorted even if it is not
  		// out of sort order (no harm).

  		int len = (int) (rowsPtrsCSR.get(row + 1) - rowsPtrsCSR.get(row));
  		IntrinsicSort.sort(colsCSR.getArray(), valsCSR.getArray(),
  				               rowsPtrsCSR.get(row), len);
  	}
  }

  /**
   * Returns the length of the vector assigned to input CSC 'col'.
   * 
   * @param col The column for which the vector length will be returned.
   * @return The length of the vector assigned to input CSC 'col'.
   * @throws IOException
   */
  @Override
  public int getCSCVectorLength(int col) throws IOException
  {
  	createCSC();
  	return colsPtrsCSC.get(col + 1) - colsPtrsCSC.get(col);
  }

  /**
   * Return the value stored at the input 'col' index and the row offset
   * 'index' from a CSC representation.
   * 
   * @param col    The matrix column index,
   * @param index  The Sparse Vector (SV) row index from a CSC
   *               sorted representation.
   * @return       The value stored at the input 'col' index and the row
   *               offset 'index'.
   * @throws IOException
   */
  @Override
  public double getCSCValue(int col, int index) throws IOException
  {
  	createCSR();
  	int i = getCSElementIndex(colsPtrsCSC, rowsCSC, col, index);
  	return valsCSC.get(i);
  }

  /**
   * Sets the input 'val' into the location at the input 'col' index and the
   * row offset 'index' for a CSC representation.
   * 
   * @param col    The matrix column index,
   * @param index  The Sparse Vector (SV) row index from a CSC
   *               sorted representation.
   * @param val    The new value to be set.
   * @return       The previous value stored at the input 'col' index and the
   *               row offset 'index'.
   * @throws IOException
   */
  @Override
  public double setCSCValue(int col, int index, double val) throws IOException
  {
  	createCSC();
  	int i = getCSElementIndex(colsPtrsCSC, rowsCSC, col, index);
  	double tmp = valsCSC.get(i);
  	valsCSC.set(index,  val);
  	return tmp;
  }

  /**
   * Return the row stored at the input 'col' index and the row offset
   * 'index' from a CSC representation.
   * 
   * @param col    The matrix column index,
   * @param index  The Sparse Vector (SV) row index from a CSC
   *               sorted representation.
   * @return       The row index stored at the input 'col' index and the
   *               the column offset 'index'.
   * @throws IOException
   */
  @Override
  public double getCSCRow(int col, int index) throws IOException
  {
  	createCSR();
  	int i = getCSElementIndex(colsPtrsCSC, rowsCSC, col, index);
  	return rowsCSC.get(i);
  }

  /**
   * Sets the row index stored at the input 'col' index and the row offset
   * 'index' for a CSC representation. Note: This destroys the sort order of the
   * Sparse Vector (SV) for the input 'col'. Call sortCSCCol(int col) after
   * finishing to restore the SV for the input 'col' to sorted order.
   * 
   * @param col    The matrix column index,
   * @param index  The Sparse Vector (SV) row index from a CSC
   *               sorted representation.
   * @param row    The new row index to be set.
   * @return       The previous row index stored at the input 'col' index and
   *               the row offset 'index'.
   * @throws IOException
   */
  @Override
  public int setCSCRow(int col, int index, int row) throws IOException
  {
  	createCSC();
  	int i = getCSElementIndex(colsPtrsCSC, rowsCSC, col, index);
  	int tmp = rowsCSC.get(i);
  	rowsCSC.set(index, row);
  	return tmp;
  }

  /**
   * Sorts the sparse vector associated with the input column. If CSC does not
   * exist it is created (which automatically sorts at creation).
   * 
   * @param col The CSC column to be sorted.
   * @throws IOException
   */
  @Override
  public void sortCSCCol(int col) throws IOException
  {
  	if (!CSCExists())
  	{
  		// Doesn't exist ... create

  		createCSC();
  		return;
  	}
  	else
  	{
  		// CSC already exists ... the input column will be sorted even if it is not
  		// out of sort order (no harm).

  		int len = (int) (colsPtrsCSC.get(col + 1) - colsPtrsCSC.get(col));
  		IntrinsicSort.sort(rowsCSC.getArray(), valsCSC.getArray(),
  				               colsPtrsCSC.get(col), len);
  	}
  }

  /**
   * Retrieves the element index in array list x that was sorted by x1 first
   * and x2 second. The start of each x1 in x is given by the array list xPtrs.
   * 
   * If CSC format was used then xPtrs is colsPtrsCSC, x is rowsCSC, x1 is the
   * column, and index is the offset entry for column x1. If CSR format was
   * used then xPtrs is rowsPtrsCSR, x is colsCSR, x1 is row, and index is the
   * offset entry for row x1.
   * 
   * @param xPtrs The compressed sorted column (CSC) or row (CSR) array list.
   * @param x     The array list of sorted row entries by column (CSC) or
   *              column entries by row (CSR).
   * @param x1    The column (CSC) or row (CSR) index.
   * @param x2    The offset row entry for the specified column index (CSC), or
   *              the offset column entry for the specified row index (CSR).
   * @return The index in array list x containing the result associated with
   *         column x1 (CSC) or row x1 (CSR) and the offset index.
   */
  private int getCSElementIndex(ArrayListInt xPtrs, ArrayListInt x, int x1, int x2)
  {
  	// make sure x1 is valid
  	
		if (x1 < xPtrs.size())
		{
			// get the start of the row or column entry for x1 in x, and the number
			// of entries for row or column entry x1.
			
			int istrt = xPtrs.get(x1);
			int icnt  = xPtrs.get(x1+1) - istrt;
			
			// make sure index is valid and return the offset + index location in x.
			if (x2 < icnt)
				return istrt + x2;
		}
		
		// invalid x, or index ... return -1.
		
  	return -1;
  }

  /**
   * Create the CSC representation if it does not exist.
   * 
   * @throws IOException
   */
  @Override
  public void createCSC() throws IOException
	{
  	// if it already exists exit.
  	
	  if (CSCExists())  return;

	  // otherwise make sure the triplet form exists and is sorted as CSC

		if (CSRExists())
		{
			colsTRP = (ArrayListInt) colsCSR.clone();
			valsTRP = (ArrayListDouble) valsCSR.clone();
			rowsTRP = decompressCSToTriplet(rowsPtrsCSR, valsTRP.size());
		}
		else
		{
			if (!tripletExists() || (valsTRP.size() == 0))
				throw new IOException("Error: Can't create CSC from empty SparseMatrix ...");
		}
	  
		sortTriplet(colsTRP, rowsTRP, valsTRP, addedInput);
  	addedInput = false;

	  // Clone the triplet rows and values array lists and compress the column
	  // array list. Store in CSC rows, vals, and colsPtrs respectively.
	  
	  rowsCSC = rowsTRP;
    valsCSC = valsTRP;
    colsPtrsCSC = compressTripletToCS(colsTRP);

    rowsTRP = colsTRP = null;
    valsTRP = null;
	}

  /**
   * Create the CSR representation if it does not exist.
   * 
   * @throws IOException
   */
  @Override
	public void createCSR() throws IOException
	{
  	// if it already exists exit.

		if (CSRExists())  return;

	  // otherwise make sure the triplet form exists and is sorted as CSR

		if (CSCExists())
		{
			rowsTRP = (ArrayListInt) rowsCSC.clone();
			valsTRP = (ArrayListDouble) valsCSC.clone();
			colsTRP = decompressCSToTriplet(colsPtrsCSC, valsTRP.size());
		}
		else
		{
			if (!tripletExists() || (valsTRP.size() == 0))
				throw new IOException("Error: Can't create CSR from empty SparseMatrix ...");
		}

	  sortTriplet(rowsTRP, colsTRP, valsTRP, addedInput);
  	addedInput = false;

	  // Clone the triplet cols and values array lists and compress the row
	  // array list. Store in CSR cols, vals, and rowsPtrs respectively.

	  colsCSR = colsTRP;
    valsCSR = valsTRP;
    rowsPtrsCSR = compressTripletToCS(rowsTRP );

    rowsTRP = colsTRP = null;
    valsTRP = null;
	}

  /**
   * Create the Triplet representation if it does not exist.
   * 
   * @throws IOException
   */
  @Override
	protected void createTriplet()
	{
		// exit if it already exists.

	  if (tripletExists())
	    return;
	  else if (CSCExists())
	  {
	  	// triplet does not exist but CSC does ... form from CSC
	  	
	    rowsTRP = rowsCSC;
	    valsTRP = valsCSC;
	    colsTRP = decompressCSToTriplet(colsPtrsCSC, valsTRP.size());
	  }
	  else if (CSRExists())
	  {
	  	// triplet does not exist but CSR does ... form from CSR
	  	
	    colsTRP = colsCSR;
	    valsTRP = valsCSR;
	    rowsTRP = decompressCSToTriplet(rowsPtrsCSR, valsTRP.size());
	  }
	  else
	  {
	  	// create a new empty triplet form

	    resetTRPEmpty();
	  }
	  
	  // clear CSR and CSC

	  rowsCSC = colsCSR = rowsPtrsCSR = colsPtrsCSC = null;
	  valsCSC = valsCSR = null;
	}

  /**
   * Resets the sparse matrix to an empty TRP representation.
   */
  @Override
  protected void resetTRPEmpty()
  {
  	rowsTRP = new ArrayListInt();
  	colsTRP = new ArrayListInt();
  	valsTRP = new ArrayListDouble();

  	maxColIndex = maxRowIndex = 0;
		transposed  = addedInput  = false;
  }

  /**
   * Validity Checked
   * Decompresses the input CSR or CSC compressed vector into its decompressed
   * form.
   * 
   * @param xPtrs Input compressed vector.
   * @return Decompressed vector.
   */
  private static ArrayListInt decompressCSToTriplet(ArrayListInt xPtrs, int decompressSize)
  {
  	ArrayListInt x = new ArrayListInt(decompressSize);
  	for (int ix = 0; ix < xPtrs.size() - 1; ++ix)
  	{
  		int ixstrt   = xPtrs.get(ix);
  		int ixstrtp1 = xPtrs.get(ix+1);
      for (int j = ixstrt; j < ixstrtp1; ++j)	x.add(ix);
  	}
  	return x;
  }

  /**
   * Validity Checked
   * Compresses the input sorted triplet row or column into its decompressed
   * CSR or CSC form.
   * 
   * @param x The input sorted triplet row or column.
   * @return The compressed CSR or CSC vector.
   */
  private static ArrayListInt compressTripletToCS(ArrayListInt x)
  {
  	int i = 0;
  	int cnt = 0;
  	ArrayListInt xPtrs = new ArrayListInt();
  	xPtrs.add(0);
  	for (int rowEntry = 0; rowEntry < x.size(); ++rowEntry)
  	{
  		int irow = x.get(rowEntry);
  		if (i < irow)
  		{
  			for (int ii = 0; ii < irow-i; ++ii) xPtrs.add(cnt);
  			i = irow;
  		}
 			++cnt;
  	}
  	xPtrs.add(cnt);
  	return xPtrs;
  }

  /**
   * Transpose the matrix.
   */
  @Override
	public void transpose()
	{
		ArrayListInt    tmpI = null;
		ArrayListDouble tmpV = null;
		
		tmpI    = rowsTRP;
		rowsTRP = colsTRP;
		colsTRP = tmpI;
		
		tmpI    = rowsPtrsCSR;
		rowsPtrsCSR = colsPtrsCSC;
		colsPtrsCSC = tmpI;
		
		tmpI    = colsCSR;
		colsCSR = rowsCSC;
		rowsCSC = tmpI;
		
		tmpV    = valsCSR;
		valsCSR = valsCSC;
		valsCSC = tmpV;

		int tmp = maxRowIndex;
		maxRowIndex = maxColIndex;
		maxColIndex = tmp;

		transposed = !transposed;
	}

  /**
   * Clears the CSR representation. If this was the last representation the
   * matrix is reset to an empty state.
   */
  @Override
  public void clearCSR()
  {
  	rowsPtrsCSR = colsCSR = null;
  	valsCSR = null;
  }

  /**
   * Clears the CSC representation. If this was the last representation the
   * matrix is reset to an empty state.
   */
  @Override
  public void clearCSC()
  {
  	colsPtrsCSC = rowsCSC = null;
  	valsCSC = null;
  }

  /**
   * Returns true if the triplet form exists.
   * 
   * @return True if the triplet form exists.
   */
  @Override
	public boolean tripletExists()
	{
		return (valsTRP != null);
	}

  /**
   * Returns true if the CSR form exists.
   * 
   * @return True if the CSR form exists.
   */
  @Override
	public boolean CSRExists()
	{
		return (valsCSR != null);
	}

  /**
   * Returns true if the CSC form exists.
   * 
   * @return True if the CSC form exists.
   */
  @Override
	public boolean CSCExists()
	{
		return (valsCSC != null);
	}

	/**
   * Validity Checked
   * 
   * Sorts the triplet sparse array in indexA order, and for each indexA entry,
   * sorts in indexB order such that two consecutive entries k and k+1 are
   * defined
   * 
   *    sort such that indexA[k+1] >= indexA[k],
   *    and if indexA[k+1] == indexA[k] then these entries are the same row so
   *    indexB[k+1] > indexB[k] for uniqueness;
   *    otherwise if indexA[k+1] > indexA[k] then row k+1 is the next row and
   *    indexB[k+1] and indexB[k] are arbitrary as they exist on different rows.
   * 
   * On exit indexB should be replaced with newIndexB, and v with newV. If
   * indexA is rowsTRP then set tripletSortType = CSR_SORTED, else if indexA is
   * colsTRP then set tripletSortType = CSC_SORTED. Finally, build rowsPtrsCSR
   * if indexA is rowsTRP, or colsPtrsCSC if indexA is colsTRP.
   * 
   * @throws IOException
   */
  private static void sortTriplet(ArrayListInt indexA, ArrayListInt indexB,
  		                            ArrayListDouble v, boolean checkInput)
  		           throws IOException
  {
  	// sort on indexA and accompanying arrays indexB and v in place

 	  IntrinsicSort.sort(indexA.getArray(), indexB.getArray(), v.getArray(),
 	  		               0, indexA.size());

 	  // now sort each entry of indexB for each entry of indexA

 	  int istrt = 0;
 	  int i = 1;
 	  while (i < indexA.size())
 	  {
 	  	// find each indexA entry containing 2 or more indexB entries and sort them

	 	  while ((i < indexA.size()) && (indexA.get(i) == indexA.get(istrt))) ++i;
	 	  int icnt = i - istrt;
	 	  if (icnt > 1)
	 	  {
	 	    IntrinsicSort.sort(indexB.getArray(), v.getArray(), istrt, icnt);

	 	    // check for double entries (same indexA and indexB index) and throw
	 	    // error if discovered

	 	    if (checkInput)
	 	    {
	 	      for (int j = istrt; j < i-1; ++j)
	 	  	    if (indexB.get(j) == indexB.get(j+1))
	 	  		    throw new IOException("Error: Double Entry for row,column: " +
	 	  				                      indexA.get(istrt) + ", " + indexB.get(j) +
	 	  				                      "...");
	 	    }
	 	  }

	 	  // set istrt to i and increment i ... continue to next indexA entry

	 	  istrt = i++;
 	  }
  }

  /**
   * Returns the entry count as a double.
   */
  @Override
  public double entries()
  {
  	return entryCount();
  }

  /**
   * Returns the total number of unique entries in the sparse matrix.
   * 
   * @return The total number of unique entries in the sparse matrix.
   */
  @Override
  public long entryCount()
  {
  	if (CSCExists())
  	  return valsCSC.size();
  	else if (CSRExists())
  	  return valsCSR.size();
  	else if (tripletExists())
  	  return valsTRP.size();
  	else
  		return 0;
  }

  /**
   * Writes the sparse matrix to the file given by filename. This method only
   * one form of the matrix. It prefers to write CSC, CSR, Triplet, in that
   * order. The first form that exists is written.
   * 
   * @param filename The name of the file into which the sparse matrix is
   *                 written.
   * @throws IOException
   */
  @Override
  public void write(String filename) throws IOException
  {
  	// create the output buffer and write the transpose flag
  	
  	FileOutputBuffer fob = new FileOutputBuffer(filename);
  	fob.writeInt(maxRowIndex);
  	fob.writeInt(maxColIndex);
		fob.writeBoolean(addedInput);
		fob.writeBoolean(transposed);
  	if (CSCExists())
  	{
  		// prefer CSC ... write matrix as CSC.
  		
  		fob.writeString("CSC");
  		colsPtrsCSC.write(fob);
  		rowsCSC.write(fob);
  		valsCSC.write(fob);
  	}
  	else if (CSRExists())
  	{
  		// CSC does not exist ... prefer CSR ... write matrix as CSR.
		  fob.writeString("CSR");
		  rowsPtrsCSR.write(fob);
		  colsCSR.write(fob);
		  valsCSR.write(fob);
  	}
  	else if (tripletExists())
  	{
  		// CSC and CSR does not exist ... write matrix as triplet.

		  fob.writeString("TRIPLET");
		  rowsTRP.write(fob);
		  colsTRP.write(fob);
		  valsTRP.write(fob);
  	}
  	fob.close();
  }

  /**
   * Reads the sparse matrix from the input file filename. It reads whatever
   * form was written. The user must check after loading the new SparseMatrix
   * and create/destroy any wanted or unwanted representations.
   * 
   * @param filename
   * @throws IOException
   */
  @Override
  public void read(String filename) throws IOException
  {
  	// make sure the current representation is cleared, create a new input
  	// buffer and read the transpose and representation type string.
  	
  	clear();
  	FileInputBuffer fib = new FileInputBuffer(filename);
  	maxRowIndex = fib.readInt();
  	maxColIndex = fib.readInt();
  	addedInput  = fib.readBoolean();
  	transposed  = fib.readBoolean();
  	String type = fib.readString();
  	if (type.equals("CSC"))
  	{
  		// type is CSC ... read in the array lists.
  		
  		colsPtrsCSC = new ArrayListInt(fib);
  		rowsCSC = new ArrayListInt(fib);
  		valsCSC = new ArrayListDouble(fib);
  	}
  	else if (type.equals("CSR"))
  	{
  		// type is CSR ... read in the array lists.
  		
  		rowsPtrsCSR = new ArrayListInt(fib);
  		colsCSR = new ArrayListInt(fib);
  		valsCSR = new ArrayListDouble(fib);
  	}
  	else if (type.equals("TRIPLET"))
  	{
  		// type is Triplet ... read in the array lists.
  		
  		rowsTRP = new ArrayListInt(fib);
  		colsTRP = new ArrayListInt(fib);
  		valsTRP = new ArrayListDouble(fib);
  	}
  	fib.close();
  }

  /**
   * Reads an old style tomography sparseMatrix of the form
   *      SparseMatrix sm = ArrayList<SparseMatrixVector>();
   * 
   * @param filepath The path to where the "sparseMatrix" file name and its
   *                 size array ("sparseMatrixSize") are written.
   * @throws IOException
   */
  public void readOldSparseMatrix(String filepath) throws IOException
  {
    // this method reads the old style SparseMatrix of the form
  	//     SparseMatrix --> ArrayList<SparseMatrixVector>()
  	// It has an accompanying file called "sparseMatrixSize" that defines 5
  	// elements containing size information of the form:
  	//     size = [maxRowIndex, maxColIndex, entryCount,
    //             observationCount, gridNodeCount];
  	// only the first 3 are of interest here ...
  	
  	// clear any existing information and build the file name for the size array
    // open and read in the size file and close it

  	clear();
  	String fn = filepath + File.separator + "sparseMatrixSize";
  	FileInputBuffer fib = new FileInputBuffer(fn);
  	long[] sze = new long [5];
  	for (int i = 0; i < 5; ++i) sze[i] = fib.readLong();
  	fib.close();

  	// create the file name for the sparse matrix and open it

  	fn = filepath + File.separator + "sparseMatrix";
  	fib = new FileInputBuffer(fn);

  	// create the column and value array lists given the entry count from the
  	// size array

  	colsCSR = new ArrayListInt((int) sze[2]);
  	valsCSR = new ArrayListDouble((int) sze[2]);

  	// read in the number of rows ... create the rowsPtrsCSR array and add 0
  	// as the first entry ... assign maxRowIndex ... validate n equals sze[0]

    int n = fib.readInt();
    rowsPtrsCSR = new ArrayListInt(n+1);
    rowsPtrsCSR.add(0);
    maxRowIndex = n;
    if (n != (int) sze[0])
    	throw new IOException("Error: Max Row Index does not match Size[0] array ...");

    // set the entries count to 0 and loop over all rows

    long entries = 0;
    for (int i = 0; i < n; ++i)
    {
    	// read in the number of entries for the ith row ... add the start postion
    	// of the next row to rowsPtrsCSR

    	int m = fib.readInt();
    	rowsPtrsCSR.add(m + rowsPtrsCSR.get(i));
    	
    	// set the ordered flag to true (If set to false after reading the column
    	// it will be sorted) and set the previously read column (lastCol) to -1
    	// ... loop over all column entries
    	
    	boolean ordered = true;
    	int lastCol = -1;
    	for (int j = 0; j < m; ++j)
    	{
    		// read the column index and adjust the maxColIndex if necessary ...
    		// add the column index to colsCSR

    		int col = fib.readInt();
    		if (maxColIndex < col + 1) maxColIndex = col + 1;
    		colsCSR.add(col);
    		
    		// if the input column is less than the previous column set ordered to false
        // update the previous column to this column, increment the entry count,
    		// and loop to the next entry

    		if (col < lastCol) ordered = false;
    		lastCol = col;
    		++entries;
    	}

    	// now read in all value entries into valsCSR

    	for (int j = 0; j < m; ++j) valsCSR.add(fib.readDouble());

    	// done with row ... if unordered then sort the row

    	if (!ordered) sortCSRRow(i);
    }

    // done ... close input file

  	fib.close();

  	// make sure entry count and maxColIndex are the same as the values
  	// written to the Size array and throw an error if not ... otherwise exit

    if (entries != sze[2])
    	throw new IOException("Error: Entry Count does not match Size[2] ...");
    
    if (maxColIndex != (int) sze[1])
    	throw new IOException("Error: Max Column Index does not match Size[1] array ...");
  }

  /**
   * Returns the total memory allocation size in bytes. Includes all currently
   * existing representations.
   * 
   * @return The total memory allocation size in bytes.
   */
  @Override
  public long memoryAllocationSize()
  {
  	long msze = 0;
  	long mintcnt = 0;
  	if (rowsCSC != null) mintcnt += rowsCSC.capacity();
  	if (rowsTRP != null) mintcnt += rowsTRP.capacity();
  	if (colsCSR != null) mintcnt += colsCSR.capacity();
  	if (colsTRP != null) mintcnt += colsTRP.capacity();
  	if (colsPtrsCSC != null) mintcnt += colsPtrsCSC.capacity();
  	if (rowsPtrsCSR != null) mintcnt += rowsPtrsCSR.capacity();
  	
  	long mdblcnt = 0;
  	if (valsTRP != null) mdblcnt += valsTRP.capacity();
  	if (valsCSR != null) mdblcnt += valsCSR.capacity();
  	if (valsCSC != null) mdblcnt += valsCSC.capacity();
  	
  	msze = mintcnt * (Integer.SIZE / 8) + mdblcnt * (Double.SIZE / 8);
  	return msze;
  }
}
