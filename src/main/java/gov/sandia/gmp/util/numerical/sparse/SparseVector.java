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

import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.numerical.doubledouble.DoubleDouble;

/**
 * Compressed Sparse Vector object that represents a single row (if created
 * from CSR) or column (if created from CSC) of the sparse matrix. The row or
 * column from which it was created is stored in the internal attribute
 * vectorIndex. The length of the vector is stored in the internal attribute
 * vectorLength. The value and index (column or row for CSR or CSC,
 * respectively) can be returned for the jth entry in the vector from the
 * methods getValue(j) and getIndex(j).
 * 
 * If the backing arrays from a CSR or CSC represented SparseMatrix are
 * destroyed then the arrays defined here may no longer be valid.
 * 
 * @author jrhipp
 *
 */
public class SparseVector
{
	/**
	 * The offset into indxArray and valsArray at which this compressed sparse
	 * vector begins.
	 */
	protected int  startIndex    = -1;
	/**
	 * The offset into indxArray and valsArray at which this compressed sparse
	 * vector ends + 1.
	 */
	protected int  endIndex      = -1;

	/**
	 * The row or column index represented by this vector.
	 */
	protected int  vectorIndex   = -1;

  /**
   * The array containing the column (CSR) or row (CSC) indexes.
   */
  protected int[]    indxArray = null;
	
	/**
	 * The array containing the values.
	 */
	protected double[] valsArray = null;

	protected SparseVector()
	{
		
	}

	/**
	 * Standard constructor.
	 * 
	 * @param ptrsX The compressed row (CSR) or column (CSC) list for which
	 *              the kth entry will be selected to populate this compressed
	 *              sparse vector. 
	 * @param x     The sorted column (CSR) or row (CSC) list that will supply
	 *              the requested indices from the getIndex(j) method.
	 * @param v     The sorted value list that will supply the requested values
	 *              from the getValue(j) method.
	 * @param k     The row (CSR) or column (CSC) index represented by this
	 *              compressed sparse vector.
	 */
	public SparseVector(ArrayListInt ptrsX, ArrayListInt x, ArrayListDouble v, int k)
	{
		indxArray     = x.getArray();
		valsArray     = v.getArray();
		vectorIndex   = k;
		startIndex    = ptrsX.get(vectorIndex);
		endIndex      = ptrsX.get(vectorIndex + 1);
	}

	protected SparseVector(int vecIndx, int vecLen, int strtIndx,
			                             int[] indxA, double[] valsA)
	{
		vectorIndex   = vecIndx;
		startIndex    = strtIndx;
		endIndex      = strtIndx + vecLen;
		indxArray     = indxA;
		valsArray     = valsA;
	}

	/**
	 * The vector index for which this CSV was created (the row index for CSR
	 * or the column index for CSC).
	 * 
	 * @return The vector index.
	 */
	public int getVectorIndex()
	{
		return vectorIndex;
	}

	/**
	 * The length of the vector (# of column entries for CSR or the # of row
	 * entries for CSC).
	 * 
	 * @return The length of the vector.
	 */
	public int size()
	{
		return endIndex - startIndex;
	}

	/**
	 * Returns the jth vector value entry.
	 * 
	 * @param j The index of the entry in the vector to return.
	 * @return The jth vector value entry.
	 */
	public double getValue(int j)
	{
		return valsArray[startIndex + j];
	}

	/**
	 * Returns the jth vector index entry (a column index if CSR or a row index
	 * if CSC).
	 * 
	 * @param j The index of the entry in the vector to return.
	 * @return The jth vector index entry.
	 */
	public int getIndex(int j)
	{
		return indxArray[startIndex + j];
	}

	/**
	 * Performs a binary search to locate the input key i within the index array.
	 * This method assumes the SparseVector has been sorted. The outcome is
	 * undefined if the SparseVector is not sorted. If the input key is not
	 * contained in the SparseVector then its insertion point is returned. The
	 * actual index should be checked by validating that
	 * 
	 *   getIndex(findIndex(int i)) == i.
	 *   
	 * @param i The index searched for in the index array of this SparseVector.
	 * @return The index containin i, or its insertion point where i would be
	 *         inserted.
	 */
	public int findIndex(int i)
	{
		return java.util.Arrays.binarySearch(indxArray, startIndex, endIndex, i) -
				   startIndex;
	}

  /**
   * Normalizes each value in aValue by the column entry in aColNorm whose
   * column is selected using the entry in aIndex.
   * 
   * @param aColNorm The column normalization array.
   */
	public void normalizeIndex(double[] aNorm)
  {
    for (int j = startIndex; j < endIndex; ++j)
    	valsArray[j] /= aNorm[indxArray[j]];
  }

  /**
   * Normalizes each value in aValue by the column entry in aColNorm whose
   * column is selected using the entry in aIndex.
   * 
   * @param aColNorm The column normalization array.
   */
	public void normalize(double aNorm)
  {
    for (int j = startIndex; j < endIndex; ++j) valsArray[j] /= aNorm;
  }

  /**
   * Returns the sum of all elements in the value array.
   * 
   * @return The sum of all elements in the value array.
   */
  public double sum()
  {
    // get the value array and sum all elements to rslt and
    // return

    double rslt = 0.0;
    for (int i = startIndex; i < endIndex; ++i) rslt += valsArray[i];
    return rslt;
  }

  /**
   * Returns the sum-of-squares of all elements in the value array.
   * 
   * @return The sum-of-squares of all elements in the value array.
   */
  public double sumOfSquares()
  {
    // get the value array and sum all element squares to rslt and
    // return

    double rslt = 0.0;
    for (int i = startIndex; i < endIndex; ++i)
    	rslt += valsArray[i] * valsArray[i];
    return rslt;
  }

  /**
   * Scales all elements in the value array for this CSV.
   * 
   * @param scl The value by which each element is scaled.
   */
  public void  scale(double scl)
  {
    // scale each entry in the vector.

    for (int i = startIndex; i < endIndex; ++i) valsArray[i] *= scl;
  }
  
  /**
   * Returns the sum-of-squares of all elements in the value array
   * whose index is less than the input maximum maxVecIndex.
   * 
   * @param maxVecIndex The maximum index value for which elements
   *                    whose index equals or exceeds this value are
   *                    not included in the result.
   * 
   * @return The sum-of-squares of all elements in the value array
   *         whose index is less than the input maximum maxVecIndex.
   */
  public double sumOfSquaresPartial(int maxVecIndex)
  {
    // get the index array and find the first index, mi, such that
    // the value of the index array at mi is equal to or larger than
    // the input limit maxVecIndex.

    int mi;
    for (mi = startIndex; mi < endIndex; ++mi)
    	if (indxArray[mi] >= maxVecIndex) break;

    // get the value array and sum all element squares to rslt for indices
    // 0 to i < mi. Return the result.

    double rslt = 0.0;
    for (int i = startIndex; i < mi; ++i)
    	rslt += valsArray[i] * valsArray[i];
    return rslt;
  }

  /**
   * Performs a double precision update of this sparse vector with the input
   * vector vec. The results are summed to the input value (val) and returned
   * on completion.
   * 
   * @param val The initial value of the returned result.
   * @param vec The vector that will multiply this sparse vector. This vector
   *            is addressed only at locations defined in indxArray.
   *       
   * @return The final vector product
   *           val += sum(valsArray[i] * vec[indxArray[i]]; i = 0; i < vectorLength);
   */
  public double update(double val, double[] vec)
  {
    for (int i = startIndex; i < endIndex; ++i)
    	val += valsArray[i] * vec[indxArray[i]];
    return val;
  }

  /**
   * Peforms a DoubleDouble precision update of this sparse vector with the
   * input vector v(hi,lo). The results are summed to the input value
   * (val(hi,lo)) and returned on completion.
   * 
   * @param val The initial value of the returned result (hi,lo).
   * @param vhi The DoubleDouble high component of the vector that will
   *            multiply this sparse vector. This vector is addressed
   *            only at locations defined in aIndex.
   * @param vlo The DoubleDouble low component of the vector that will
   *            multiply this sparse vector. This vector is addressed
   *            only at locations defined in aIndex.
   * @return The final vector product
   *           val(hi,lo) += sum(aValue[i] * v[aIndex[i]](hi,lo);
   *                             i = 0; i < aValue.size());
   */
  public void update(double[] val, double[] vhi, double[] vlo)
  {
    for (int i = startIndex; i < endIndex; ++i)
    {
      int j = indxArray[i];
      DoubleDouble.addMultFast(val, valsArray[i], vhi[j], vlo[j]);
    }
  }

  /**
   * Returns the maximum index in the index arrays (sorted so last position).
   * 
   * @return The maximum index in the index arrays (sorted so last position).
   */
  public int getMaxIndex()
  {
  	return indxArray[endIndex - 1];
  }

  /**
   * Returns the minimum index in the index arrays (sorted so first position).
   * 
   * @return The minimum index in the index arrays (sorted so first position).
   */
  public int getMinIndex()
  {
  	return indxArray[startIndex];
  }
}
