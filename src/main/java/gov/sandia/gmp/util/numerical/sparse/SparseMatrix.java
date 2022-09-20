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

import java.io.IOException;

/**
 * SparseMatrix and SparseMatrixHuge both inherit this interface. Either
 * concrete class can be instantiated and used in code that manipulates a
 * SparseMatrix object.
 * 
 * Concrete classes store a sparse matrix in one or more of three standard
 * sparse matrix representations including triplet (row, column, value arrays),
 * Compressed Sparse Column or CSC (compressed sorted column, row, value arrays),
 * or Compressed Sparse Row or CSR (compressed sorted row, column, value arrays).
 * 
 * Any one of, or all three representations may exist simultaneously. When
 * inserting new elements the triplet form is automatically created if it does
 * not exist. Elements are only inserted into triplet form and only retrieved
 * in CSC or CSR form.
 * 
 * Sparse Vector (SV) objects can wrap the CSR or CSC forms to provide
 * convenient sparse matrix row or column access without incurring any
 * significant additional storage penalty. This is because the SV object is a
 * lightweight container that uses the SparseMatrix object as the actual
 * container (no copying) and references the contents based on SV size and
 * start locations within the SparseMatrix. Methods are provided to create
 * arrays of SV objects for either CSR or CSC representations.
 * 
 * @author jrhipp
 *
 */
public abstract class SparseMatrix
{
  /**
   * The maximum row index for this sparse matrix.
   */
	protected int             maxRowIndex = -1;
  
  /**
   * The maximum column index for this sparse matrix.
   */
	protected int             maxColIndex = -1;
	
	/**
	 * Transposed flag.
	 */
	protected boolean         transposed  = false;

	/**
	 * Set to true if input has been added until a CSC or CSR is created
	 */
	protected boolean         addedInput  = false;

  /**
   * Create the CSC representation if it does not exist.
   * 
   * @throws IOException
   */
	public abstract void createCSC() throws IOException;

  /**
   * Create the CSR representation if it does not exist.
   * 
   * @throws IOException
   */
	public abstract void createCSR() throws IOException;

	/**
   * Create the Triplet representation if it does not exist.
   * 
   * @throws IOException
   */
	protected abstract void createTriplet();

	/**
   * Transpose the matrix.
   */
	public abstract void transpose();

	/**
	 * Clears the entire sparse matrix (all representations) to an empty state.
	 */
  public void clear()
  {
  	clearCSR();
  	clearCSC();
  }

  public SparseMatrix()
  {
  	
  }

	/**
   * Clears the CSC representation. If this was the last representation the
   * matrix is reset to an empty state.
   */
  public abstract void clearCSC();

	/**
   * Clears the CSR representation. If this was the last representation the
   * matrix is reset to an empty state.
   */
  public abstract void clearCSR();

  /**
   * Resets the sparse matrix to an empty TRP representation.
   */
  protected abstract void resetTRPEmpty();

  /**
   * Returns true if the triplet form exists.
   * 
   * @return True if the triplet form exists.
   */
	public abstract boolean tripletExists();

	/**
   * Returns true if the CSR form exists.
   * 
   * @return True if the CSR form exists.
   */
	public abstract boolean CSRExists();

	/**
   * Returns true if the CSC form exists.
   * 
   * @return True if the CSC form exists.
   */
	public abstract boolean CSCExists();

	/**
   * Builds an array of SparseVector objects, one for each row in
   * the sparse matrix.
   * 
   * @return The array of SparseVector objects.
   * @throws IOException
   */
	public abstract SparseVector[] buildCSR_SV() throws IOException;

  /**
   * Builds an array of SparseVector objects, one for each column in
   * the sparse matrix.
   * 
   * @return The array of SparseVector objects.
   * @throws IOException
   */
	public abstract SparseVector[] buildCSC_SV() throws IOException;

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
  public abstract void add(int row, int col, double value) throws IOException;

  /**
   * Returns the length of the vector assigned to input CSR 'row'.
   * 
   * @param row The row for which the vector length will be returned.
   * @return The length of the vector assigned to input CSR 'row'.
   * @throws IOException
   */
  public abstract int getCSRVectorLength(int row) throws IOException;

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
  public abstract double getCSRValue(int row, int index) throws IOException;

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
  public abstract double setCSRValue(int row, int index, double val) throws IOException;

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
  public abstract double getCSRColumn(int row, int index) throws IOException;

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
  public abstract int setCSRColumn(int row, int index, int column) throws IOException;

  /**
   * Sorts the sparse vector associated with the input row. If CSR does not
   * exist it is created (which automatically sorts at creation).
   * 
   * @param row The CSR row to be sorted.
   * @throws IOException
   */
  public abstract void sortCSRRow(int row) throws IOException;

  /**
   * Returns the length of the vector assigned to input CSC 'col'.
   * 
   * @param col The column for which the vector length will be returned.
   * @return The length of the vector assigned to input CSC 'col'.
   * @throws IOException
   */
  public abstract int getCSCVectorLength(int col) throws IOException;

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
  public abstract double getCSCValue(int col, int index) throws IOException;

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
  public abstract double setCSCValue(int col, int index, double val) throws IOException;

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
  public abstract double getCSCRow(int col, int index) throws IOException;

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
  public abstract int setCSCRow(int col, int index, int row) throws IOException;

  /**
   * Sorts the sparse vector associated with the input column. If CSC does not
   * exist it is created (which automatically sorts at creation).
   * 
   * @param col The CSC column to be sorted.
   * @throws IOException
   */
  public abstract void sortCSCCol(int col) throws IOException;

  /**
   * Reads the sparse matrix from the input file filename.
   * 
   * @param filename
   * @throws IOException
   */
	public abstract void read(String filename) throws IOException;

  /**
   * Reads an old style tomography sparseMatrix of the form
   *      SparseMatrix sm = ArrayList<SparseMatrixVector>();
   * 
   * @param filepath The path to where the "sparseMatrix" file name and its
   *                 size array ("sparseMatrixSize") are written.
   * @throws IOException
   */
  public abstract void readOldSparseMatrix(String filepath) throws IOException;

  /**
   * Writes the sparse matrix to the file given by filename.
   * 
   * @param filename The name of the file into which the sparse matrix is
   *                 written.
   * @throws IOException
   */
  public abstract void write(String filename) throws IOException;

  /**
   * Returns the total number of entries as a double.
   * 
   * @return The total number of entries as a double.
   */
  public abstract double entries();

  /**
   * Returns the total number of unique entries in the sparse matrix.
   * 
   * @return The total number of unique entries in the sparse matrix.
   */
  public abstract long entryCount();

  /**
   * Returns the mean row entry count (normalized by columns).
   * 
   * @return The mean row entry count (normalized by columns).
   */
  public double meanRowEntryCount()
  {
  	return (double) entries() / (maxColIndex - 1);
  }

  /**
   * Returns the mean column entry count (normalized by rows).
   * 
   * @return The mean column entry count (normalized by rows).
   */
  public double meanColEntryCount()
  {
  	return (double) entries() / (maxRowIndex - 1);
  }

  /**
   * Returns the maximum row index of the sparse matrix.
   * 
   * @return The maximum row index of the sparse matrix.
   */
  public int getMaxRow()
  {
  	return maxRowIndex;
  }

  /**
   * Returns the maximum column index of the sparse matrix.
   * 
   * @return The maximum column index of the sparse matrix.
   */
  public int getMaxCol()
  {
  	return maxColIndex;
  }

  /**
   * Returns true if transposed.
   * 
   * @return True if transposed.
   */
  public boolean isTransposed()
  {
  	return transposed;
  }

  /**
   * Returns the total memory allocation size in bytes.
   * 
   * @return The total memory allocation size in bytes.
   */
  public abstract long memoryAllocationSize();
}
