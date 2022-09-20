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

import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.containers.arraylisthuge.ArrayListHugeDouble;
import gov.sandia.gmp.util.containers.arraylisthuge.ArrayListHugeInt;
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
public class SparseVectorHuge extends SparseVector
{
  /**
   * The array containing the second segment (1) column (CSR) or row (CSC)
   * indexes. The first segment column or row indexes are stored in indxArray.
   */
  protected int[]    indxArray1 = null;
	
	/**
   * The array containing the second segment (1) column (CSR) or row (CSC)
   * values. The first segment column or row values are stored in valsArray.
	 */
	protected double[] valsArray1 = null;
	
	/**
	 * Protected default constructor.
	 */
	protected SparseVectorHuge()
	{
		super();
	}

	/**
	 * Factory method to create a CompressedSparseVector. If the CSV is contained
	 * entirely within one segment then a standard CSV is created and returned.
	 * However, in the rare case where the CSV spans two segments then a CSV
	 * Huge object is created and returned.
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
	public static SparseVector
	              getCompressedSparseVector(ArrayListLong ptrsX, ArrayListHugeInt x,
	              		                      ArrayListHugeDouble v, int k)
  {
		// get the standard elements of the CSV

		int vLen       = (int) (ptrsX.get(k + 1) - ptrsX.get(k));
		int offstIndx  = x.getElementIndex(ptrsX.get(k));
		int segIndx0   = x.getSegmentIndex(ptrsX.get(k));
		int[]    indxA = x.getArraySegment(segIndx0);
		double[] valsA = v.getArraySegment(segIndx0);

		// if the length is zero return a simple CSV

		if (vLen == 0)
			return new SparseVector(k, vLen, offstIndx, indxA, valsA);
		else
		{
			// otherwise get the segment of the last element and see if it equals the
			// first element segment. If it does then the vector is contained within
			// one array so return a simple CSV

			int segIndx1   = x.getSegmentIndex(ptrsX.get(k + 1) - 1);
			if (segIndx0 == segIndx1)
				return new SparseVector(k, vLen, offstIndx, indxA, valsA);
			else
			{
				// the last element is in the next segment (Were assuming that no
				// vector exceeds the total number of allocated elements in a single
				// segment ... if it does this will fail) then make a huge CSV with
				// two segments containing the data

				int[]    indxA1 = x.getArraySegment(segIndx1);
				double[] valsA1 = v.getArraySegment(segIndx1);
				return new SparseVectorHuge(k, vLen, offstIndx, indxA, valsA,
	                                            indxA1, valsA1);
			}
		}
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
	public SparseVectorHuge(int vecIndx, int vecLen, int offstIndx,
      int[] indxA, double[] valsA, int[] indxA1, double[] valsA1)
	{
		// same as standard CSV except startIndex is the beginning of the first
		// entry in segment 0 (indxArray and valsArray) and goes to the end of the
		// array(s). endIndex is the end of the last entry in segment 1 (indxArray1
		// and valsArray1) + 1. In the standard CSV the length of the vector is
		// endIndex - startIndex. For the huge case it is endIndex - startIndex +
		// indxArray.length.

		vectorIndex = vecIndx;
		startIndex  = offstIndx;
		//vectorLength0 = indxA.length - startIndex;
		endIndex    = vecLen - indxA.length + offstIndx;
		//vectorLength1 = endIndex;
		indxArray   = indxA;
		valsArray   = valsA;
		indxArray1  = indxA1;
		valsArray1  = valsA1;
	}

	/**
	 * The length of the vector (# of column entries for CSR or the # of row
	 * entries for CSC).
	 * 
	 * @return The length of the vector.
	 */
	@Override
	public int size()
	{
		return endIndex - startIndex + indxArray.length;
	}

	/**
	 * Returns the jth vector value entry.
	 * 
	 * @param j The index of the entry in the vector to return.
	 * @return The jth vector value entry.
	 */
	@Override
	public double getValue(int j)
	{
		if (j + startIndex >= valsArray.length)
		  return valsArray1[j - indxArray.length + startIndex];
		else
		  return valsArray[startIndex + j];
	}

	/**
	 * Returns the jth vector index entry (a column index if CSR or a row index
	 * if CSC).
	 * 
	 * @param j The index of the entry in the vector to return.
	 * @return The jth vector index entry.
	 */
	@Override
	public int getIndex(int j)
	{
		if (j + startIndex >= indxArray.length)
		  return indxArray1[j - indxArray.length + startIndex];
		else
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
	@Override
	public int findIndex(int i)
	{
		if (i > indxArray[indxArray.length - 1])
		  return java.util.Arrays.binarySearch(indxArray1, 0, endIndex, i) +
		  		   indxArray.length - startIndex;
		else
		  return java.util.Arrays.binarySearch(indxArray, startIndex, indxArray.length, i) -
		  		   startIndex;
	}

  /**
   * Normalizes each value in aValue by the column entry in aColNorm whose
   * column is selected using the entry in aIndex.
   * 
   * @param aColNorm The column normalization array.
   */
	@Override
	public void normalizeIndex(double[] aNorm)
  {
    for (int j = startIndex; j < indxArray.length; ++j)
    	valsArray[j] /= aNorm[indxArray[j]];
    for (int j = 0; j < endIndex; ++j)
    	valsArray1[j] /= aNorm[indxArray1[j]];
  }

  /**
   * Normalizes each value in aValue by the column entry in aColNorm whose
   * column is selected using the entry in aIndex.
   * 
   * @param aColNorm The column normalization array.
   */
	@Override
	public void normalize(double aNorm)
  {
    for (int j = startIndex; j < indxArray.length; ++j) valsArray[j] /= aNorm;
    for (int j = 0; j < endIndex; ++j) valsArray1[j] /= aNorm;
  }

  /**
   * Returns the sum of all elements in the value array.
   * 
   * @return The sum of all elements in the value array.
   */
	@Override
  public double sum()
  {
    // get the value array and sum all elements to rslt and
    // return

    double rslt = 0.0;
    for (int i = startIndex; i < indxArray.length; ++i) rslt += valsArray[i];
    for (int i = 0; i < endIndex; ++i) rslt += valsArray1[i];
    return rslt;
  }

  /**
   * Returns the sum-of-squares of all elements in the value array.
   * 
   * @return The sum-of-squares of all elements in the value array.
   */
	@Override
  public double sumOfSquares()
  {
    // get the value array and sum all element squares to rslt and
    // return

    double rslt = 0.0;
    for (int i = startIndex; i < indxArray.length; ++i)
    	rslt += valsArray[i] * valsArray[i];
    for (int i = 0; i < endIndex; ++i)
  	  rslt += valsArray1[i] * valsArray1[i];
    return rslt;
  }

  /**
   * Scales all elements in the value array for this CSV.
   * 
   * @param scl The value by which each element is scaled.
   */
	@Override
  public void  scale(double scl)
  {
    // scale each entry in the vector.

    for (int i = startIndex; i < indxArray.length; ++i) valsArray[i] *= scl;
    for (int i = 0; i < endIndex; ++i) valsArray1[i] *= scl;
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
	@Override
  public double sumOfSquaresPartial(int maxVecIndex)
  {
    // get the index array and find the first index, mi, such that
    // the value of the index array at mi is equal to or larger than
    // the input limit maxVecIndex.

    int mi;
    for (mi = startIndex; mi < indxArray.length; ++mi)
    	if (indxArray[mi] >= maxVecIndex) break;

    // get the value array and sum all element squares to rslt for indices
    // 0 to i < mi. Return the result.

    double rslt = 0.0;
    for (int i = startIndex; i < mi; ++i)
    	rslt += valsArray[i] * valsArray[i];
    
    if (mi == indxArray.length)
    {
      for (mi = 0; mi < endIndex; ++mi)
      	if (indxArray1[mi] >= maxVecIndex) break;

      for (int i = 0; i < mi; ++i)
      	rslt += valsArray1[i] * valsArray1[i];
    }

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
	@Override
  public double update(double val, double[] vec)
  {
    for (int i = startIndex; i < indxArray.length; ++i)
    	val += valsArray[i] * vec[indxArray[i]];
    for (int i = 0; i < endIndex; ++i)
    	val += valsArray1[i] * vec[indxArray1[i]];
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
	@Override
  public void update(double[] val, double[] vhi, double[] vlo)
  {
    for (int i = startIndex; i < indxArray.length; ++i)
    {
      int j = indxArray[i];
      DoubleDouble.addMultFast(val, valsArray[i], vhi[j], vlo[j]);
    }
    for (int i = 0; i < endIndex; ++i)
    {
      int j = indxArray1[i];
      DoubleDouble.addMultFast(val, valsArray1[i], vhi[j], vlo[j]);
    }
  }

  /**
   * Returns the maximum index in the index arrays (sorted so last position).
   * 
   * @return The maximum index in the index arrays (sorted so last position).
   */
	@Override
  public int getMaxIndex()
  {
  	return indxArray1[endIndex - 1];
  }
}
