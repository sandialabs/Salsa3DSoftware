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
package gov.sandia.gmp.util.numerical.matrix;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

import javax.swing.JProgressBar;

import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.nio.NioUtils;
import gov.sandia.gmp.util.numerical.doubledouble.DoubleDouble;
import gov.sandia.gmp.util.numerical.intrinsicsort.IntrinsicSort;

/**
 * Holds a single sparse matrix row or column vector and the indirect index
 * array (row or column) to which the entry belongs. This object is used by
 * the SparseMatrix container in tomography to define a row-ordered, and/or
 * column-ordered, list of vectors that define an entire sparse matrix.
 * 
 * <p> Functionality exists to construct, add, clear, and retrieve the internal
 * data. A trimToSize() function is provided to resize to size() to free
 * memory that is not required. A memoryEstimate() function is provided to
 * estimate the approximate amount of memory used by this SparseMatrixVector.
 * Finally, update functions are provided to multiply the sparse matrix vector
 * by an input vector and return the summed results. Both a double precision
 * and DoubleDouble precision update(...) function is provided.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class SparseMatrixVector implements Serializable
{
  /**
   * Maximum index stored in aIndex. Set to -1 if no indices have been
   * added to aIndex. All indices added to aIndex are assumed to be
   * equal to or larger than 0 (positive).
   */
  private int             aMaxIndx = -1;

  /**
   * The row or column index to which each entry in aValue belongs.
   */
  private ArrayListInt    aIndex   = null;

  /**
   * The sparse matrix entry vector.
   */
  private ArrayListDouble aValue   = null;

  /**
   * Default constructor defines initial storage for aIndex and aValue.
   */
  public SparseMatrixVector()
  {
    aIndex = new ArrayListInt(16);
    aValue = new ArrayListDouble(16);
  }

  /**
   * Standard constructor that defined initial storage of upto "capacity"
   * entries for aIndex and aValue.
   * 
   * @param capacity The initial capacity of aIndex and aValue.
   */
  public SparseMatrixVector(int capacity)
  {
    aIndex = new ArrayListInt(capacity);
    aValue = new ArrayListDouble(capacity);
  }

  /**
   * Adds (appends) the contents of the input Sparse Matrix Vector to this one.
   *  
   * @param smv The input Sparse Matrix Vector whose contents will be added to
   *            this one.
   */
  public void add(SparseMatrixVector smv)
  {
    int[]    indx = smv.getIndexArray();
    double[] valu = smv.getValueArray();
    aIndex.ensureCapacity(size() + smv.size());
    aValue.ensureCapacity(size() + smv.size());
    for (int i = 0; i < smv.size(); ++i) add(indx[i], valu[i]);
  }

  /**
   * Adds a new entry to the sparse vector.
   * 
   * @param index The row or column index of the associated value.
   * @param value The sparse entry value.
   */
  public void add(int index, double value)
  {
    if (index > aMaxIndx) aMaxIndx = index;
    aIndex.add(index);
    aValue.add(value);
  }

  /**
   * Clears the sparse vector of all entries.
   */
  public void clear()
  {
    aMaxIndx = -1;
    aIndex.clear();
    aValue.clear();
  }

  /**
   * Returns the sparse vector index array.
   * 
   * @return The sparse vector index array.
   */
  public final int[] getIndexArray()
  {
    return aIndex.getArray();
  }

  /**
   * Returns the sparse vector value array.
   * 
   * @return The sparse vector value array.
   */
  public final double[] getValueArray()
  {
    return aValue.getArray();
  }

  /**
   * Trims the index and value arrays allocated capacity to their current size.
   */
  public void trimToSize()
  {
    aIndex.trimToSize();
    aValue.trimToSize();
  }

  /**
   * Returns the current size of the sparse vector.
   * 
   * @return The current size of the sparse vector.
   */
  public int size()
  {
    return aValue.size();
  }

  /**
   * Returns an estimate of the intrinsic memory required to store this
   * sparse vector.
   * 
   * @return An estimate of the intrinsic memory required to store this
   *         sparse vector.
   */
  public long memoryEstimate()
  {
    return (long) aIndex.capacity() * Integer.SIZE / 8 +
           (long) aValue.capacity() * Double.SIZE / 8 +
           baseMemoryEstimate();
  }

  public static long baseMemoryEstimate()
  {
    // this reference                8
    // max index storage             4
    // 2 array list references      16
    // 2 container array references 16
    // 2 array list capacity stores  8
    // 2 array size stores           8
    return 60;
  }
  /**
   * Sets all entries in trnsSprs from this sparse matrix vector which
   * occupies row i in some other sparse matrix definition. The input
   * sparse matrix must have rows defined that encompass the range of
   * values in aIndex. Each entry in aIndex is a row in the input
   * sparse matrix and will have aValue entries copied into them set
   * to index i.
   * 
   * @param i The row at which this sparse matrix vector is set in some
   *          other sparse matrix definition that is used to build the
   *          input transpose.
   * @param trnsSprs A transpose definition from the one within which
   *                 this sparse matrix vector is the ith row member.
   */
  public void setTranspose(int i, ArrayList<SparseMatrixVector> trnsSprs)
  {
    int[]              indx = getIndexArray();
    double[]           va   = getValueArray();
    for (int j = 0; j < aIndex.size(); ++j) trnsSprs.get(indx[j]).add(i, va[j]);
  }

  /**
   * Normalizes each value in aValue by the column entry in aColNorm whose
   * column is selected using the entry in aIndex.
   * 
   * @param aColNorm The column normalization array.
   */
  public void normalize(double[] aColNorm)
  {
    int[]              indx = getIndexArray();
    double[]           va   = getValueArray();
    for (int j = 0; j < aValue.size(); ++j) va[j] /= aColNorm[indx[j]]; 
  }

  /**
   * Normalizes each value in aValue by the inverse sum of entries in
   * the vector.
   * 
   * @return The sum of the entries in the sparse vector before scaling
   */
  public double normalizeWithVectorSum()
  {
         double sumvec = sum();
         scale(1.0/sumvec);
         
         return sumvec;
  }

  /**
   * Normalizes each value in aValue by the sum of the absolute value of
   * the entries in the vector.
   * 
   * @return The sum of the absolute value of entries in the sparse vector
   *           before scaling.
   */
  public double normalizeWithVector1Norm()
  {
         double sumvec = sumOfAbsoluteValues();
         scale(1.0/sumvec);
         
         return sumvec;
  }

  /**
   * Normalizes each value in aValue by the inverse square root sum of squares of
   * the entries in the vector.
   * 
   * @return The square root sum of squares of entries in the sparse
   *         vector before scaling.
   */
  public double normalizeWithVector2Norm()
  {
         double sumvec = Math.sqrt(sumOfSquares());
         scale(1.0/sumvec);
         
         return sumvec;
  }

  /**
   * Scales each value in aValue by the input scale value.
   * 
   * @param scale The scale factor by which each entry in aValue is scaled
   */
  public void scale(double scale)
  {
    double[]           va   = getValueArray();
    for (int j = 0; j < aValue.size(); ++j) va[j] *= scale; 
  }

  /**
   * Peforms a double precision update of this sparse vector with the input
   * vector vec. The results are summed to the input value (val) and returned
   * on completion.
   * 
   * @param val The initial value of the returned result.
   * @param vec The vector that will multiply this sparse vector. This vector
   *            is addressed only at locations defined in aIndex.
   *       
   * @return The final vector product
   *           val += sum(aValue[i] * vec[aIndex[i]]; i = 0; i < aValue.size());
   */
  public double update(double val, double[] vec)
  {
    int[]    ia = aIndex.getArray();
    double[] va = aValue.getArray();
    for (int i = 0; i < aValue.size(); ++i) val += va[i] * vec[ia[i]];
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
    int[]    ia = aIndex.getArray();
    double[] va = aValue.getArray();
    for (int i = 0; i < aValue.size(); ++i)
    {
      int j = ia[i];
      DoubleDouble.addMultFast(val, va[i], vhi[j], vlo[j]);
    }
  }

  /**
   * Returns the maximum index stored in aIndex.
   * 
   * @return The maximum index stored in aIndex.
   */
  public int getMaxIndex()
  {
    return aMaxIndx;
  }

  /**
   * Writes out this sparse vector to the supplied FileOutputBuffer fob.
   * 
   * @param fob The file output buffer into which this sparse vector is
   *            written.
   * @throws IOException
   */
  public void writeVector(FileOutputBuffer fob) throws IOException
  {
    // write out the number of entries and each value contained in
    // aIndex and aValue.

    fob.writeInt(aValue.size());
    for (int i = 0; i < aIndex.size(); ++i) fob.writeInt(aIndex.get(i));
    for (int i = 0; i < aValue.size(); ++i) fob.writeDouble(aValue.get(i));
  }
  
  /**
   * Writes this vector to the specified channel. The channel will need to be flushed via call to
   * NioUtils.flushBufferToChannel() after this call is made to ensure the entire contents of the
   * buffer are written.
   * @param dest
   * @param buffer
   * @throws IOException
   */
  public void writeVector(WritableByteChannel dest, ByteBuffer buffer) throws IOException{
    NioUtils.writeInts(dest, buffer, aValue.size());
    NioUtils.writeInts(dest, buffer, aIndex.getArray(),0,aIndex.size());
    NioUtils.writeDoubles(dest, buffer, aValue.getArray(),0,aValue.size());
  }
  
  public boolean readVector(ReadableByteChannel src, ByteBuffer buffer) throws IOException{
    int n = NioUtils.readInt(src, buffer);
    aIndex = new ArrayListInt(NioUtils.readInts(src, buffer, new int[n], 0, n));
    aValue = new ArrayListDouble(NioUtils.readDoubles(src,buffer,new double[n],0,n));

    boolean ordered = true;
    int lastIdx = -1;
    for(int i : aIndex.getArray()) {
      if(i > aMaxIndx) aMaxIndx = i;
      if(ordered) {
        if (i > lastIdx) lastIdx = i;
        else ordered = false;
      }
    }
    
    return ordered;
  }

  /**
   * Reads a sparse vector definition from the supplied FileInputBuffer (fib)
   * and populates this SparseMatrixVector with the input information. The
   * maximum index (aMaxIndx) is also set. If the index array is ordered in an
   * increasing fashion (aIndex.get(i+1) > aIndex.get(i)) then true is returned
   * to indicate an ordered index array. Otherwise false is returned.
   * 
   * @param fib The file input buffer from which this sparse matrix vectors
   *            information is read.
   * @returns True if the index array is ordered in an ascending fashion. 
   * @throws IOException
   */
  public boolean readVector(FileInputBuffer fib) throws IOException
  {
    // read the total number of entries and initialize the lists

    int n = fib.readInt();
    clear();
    aIndex.ensureCapacity(n);
    aValue.ensureCapacity(n);

    // loop over each entry and read the index and value and store into
    // aIndex and aValue. Adjust the maximum discovered index during the read.
    // test for an ordered index array

    boolean ordered = true;
    int lastindex = -1;
    for (int i = 0; i < n; ++i)
    {
      int indx = fib.readInt();
      if (indx > aMaxIndx) aMaxIndx = indx;
      aIndex.add(indx);
      if (ordered)
      {
        if (indx > lastindex)
          lastindex = indx;
        else
          ordered = false;
      }
    }
    for (int i = 0; i < n; ++i) aValue.add(fib.readDouble());
    return ordered;
  }

  /**
   * Returns the sum-of-squares of all elements int the value array.
   * 
   * @return The sum-of-squares of all elements int the value array.
   */
  public double sum()
  {
    // get the value array and sum all element squares to rslt and
    // return

    double[] val = getValueArray();
    double rslt = 0.0;
    for (int i = 0; i < val.length; ++i) rslt += val[i];
    return rslt;
  }

  /**
   * Returns the sum-of-absolute value of all elements in the value array.
   * 
   * @return The sum-of-absolute value of all elements in the value array.
   */
  public double sumOfAbsoluteValues()
  {
    // get the value array and sum all element squares to rslt and
    // return

    double[] val = getValueArray();
    double rslt = 0.0;
    for (int i = 0; i < val.length; ++i) rslt += Math.abs(val[i]);
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

    double[] val = getValueArray();
    double rslt = 0.0;
    for (int i = 0; i < val.length; ++i) rslt += val[i] * val[i];
    return rslt;
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

    int[]   indx = getIndexArray();
    int mi;
    for (mi = 0; mi < indx.length; ++mi) if (indx[mi] >= maxVecIndex) break;

    // get the value array and sum all element squares to rslt for indices
    // 0 to i < mi. Return the result.

    double[] val = getValueArray();
    double rslt = 0.0;
    for (int i = 0; i < mi; ++i) rslt += val[i] * val[i];
    return rslt;
  }

  /**
   * Given the input sparse matrix representation, inSprs, the transpose
   * ordered sparse matrix, trnsSprs, is constructed. That is, if the
   * input sparse matrix is row ordered then the output sparse matrix
   * is column ordered and vice-versa. The transpose is constructed where
   * each row in inSprs becomes a column in trnsSprs and each column in
   * inSprs becomes a row in trnsSprs.
   */
  public static ArrayList<SparseMatrixVector>
         buildTransposeSparseMatrix(ArrayList<SparseMatrixVector> inSprs,
         JProgressBar pb)
  {
    ArrayList<SparseMatrixVector> trnsSprs = null;

    // find the largest entry index in the input sparse matrix and create
    // a transpose-ordered sparse matrix with that many entries (nentries).

    int nentries = 0;
    for (int i = 0; i < inSprs.size(); ++i)
    {
      int maxindx = inSprs.get(i).getMaxIndex();
      if (nentries < maxindx) nentries = maxindx;
    }
    ++nentries;
    trnsSprs = new ArrayList<SparseMatrixVector>(nentries);
    for (int i = 0; i < nentries; ++i) trnsSprs.add(new SparseMatrixVector());

    // loop over all entries in inSprs and assign them to trnsSprs.

    if (pb != null) pb.setMaximum(inSprs.size());
    for (int i = 0; i < inSprs.size(); ++i)
    {
      inSprs.get(i).setTranspose(i, trnsSprs);
      if (pb != null) pb.setValue(i);
    }

    // trim each vector to size

    for (int i = 0; i < nentries; ++i) trnsSprs.get(i).trimToSize();
    
    // return transpose-ordered sparse matrix

    return trnsSprs;
  }

  /**
   * Multiplies the two input sparse matrix vectors (smv1 and smv2) together
   * and returns the result to the caller. Only entries in smv1 and smv2 whose
   * index values are equivalent are multiplied and summed to the final result.
   * 
   * <p> Note that the matrix M = G^T * G can be assembled. The i,j element of
   * M is given by M(i,j) = G^T(i) * G^T(j)
   * 
   * @param smv1 The first sparse matrix vector to be multiplied by smv2 and
   *             returned.
   * @param smv2 The second sparse matrix vector to be multiplied by smv1 and
   *             returned.
   * @return The result of multiplying smv1 * smv2.
   */
  public static double multiplyVectors(SparseMatrixVector smv1,
                                       SparseMatrixVector smv2)
  {
    // if smv1 and smv2 are equivalent then return the sum of squares

    if (smv1 == smv2) return smv1.sumOfSquares();

    // get index and value arrays from sparse matrix vectors smv1 and smv2

    int[]    i1 = smv1.getIndexArray();
    double[] v1 = smv1.getValueArray();
    int[]    i2 = smv2.getIndexArray();
    double[] v2 = smv2.getValueArray();

    // initialize result and index counters to 0 and loop over all entries

    double rslt = 0.0;
    int k1 = 0, k2 = 0;
    while ((k1 < i1.length) && (k2 < i2.length))
    {
      // see if index values for smv1 and smv2 are equivalent ... if they are
      // then sum the equivalent value product to the result and increment both
      // counters. If not and the current index of smv1 is larger than the
      // current index for smv2 then increment the smv2 counter. Otherwise,
      // increment the smv1 counter. Continue until one or both counters
      // traverses its entire array

      if (i1[k1] == i2[k2])
        rslt += v1[k1++] * v2[k2++];
      else if (i1[k1] > i2[k2])
        ++k2;
      else
        ++k1;
    }

    // done ... return result

    return rslt;
  }

  /**
   * Multiplies the two input sparse matrix vectors (smv1 and smv2) together
   * and returns the result to the caller. Only entries in smv1 and smv2 whose
   * index values are equivalent are multiplied and summed to the final result.
   * Also, only entries whose index values are less than the input maximum
   * vector index limit, maxVecIndex, are summed.
   * 
   * <p> Note that the matrix M = G^T * G can be assembled. The i,j element of
   * M is given by M(i,j) = G^T(i) * G^T(j)
   * 
   * @param maxVecIndex The limiting index for which elements whose index equal
   *                    or exceed this value are not included in the multiply.
   * @param smv1 The first sparse matrix vector to be multiplied by smv2 and
   *             returned.
   * @param smv2 The second sparse matrix vector to be multiplied by smv1 and
   *             returned.
   * @return The result of multiplying smv1 * smv2.
   */
  public static double multiplyPartialVectors(int maxVecIndex,
                                              SparseMatrixVector smv1,
                                              SparseMatrixVector smv2)
  {
    // if smv1 and smv2 are equivalent then return the sum of squares

    if (smv1 == smv2) return smv1.sumOfSquaresPartial(maxVecIndex);

    // get index and value arrays from sparse matrix vectors smv1 and smv2

    int[]    i1 = smv1.getIndexArray();
    double[] v1 = smv1.getValueArray();
    int[]    i2 = smv2.getIndexArray();
    double[] v2 = smv2.getValueArray();

    // initialize result and index counters to 0 and loop over all entries
    // whose index values are less than maxVecIndex.

    double rslt = 0.0;
    int k1 = 0, k2 = 0;
    while ((k1 < i1.length) && (k2 < i2.length) &&
           (i1[k1] < maxVecIndex) && (i2[k2] < maxVecIndex))
    {
      // see if index values for smv1 and smv2 are equivalent ... if they are
      // then sum the equivalent value product to the result and increment both
      // counters. If not and the current index of smv1 is larger than the
      // current index for smv2 then increment the smv2 counter. Otherwise,
      // increment the smv1 counter. Continue until one or both counters
      // traverses its entire array

      if (i1[k1] == i2[k2])
        rslt += v1[k1++] * v2[k2++];
      else if (i1[k1] > i2[k2])
        ++k2;
      else
        ++k1;
    }

    // done ... return result

    return rslt;
  }

  /**
   * Public static function that reads and returns the sparse matrix from the
   * input file fn.
   * 
   * @param fn
   *          The file from which the sparse matrix will be read.
   * @return The sparse matrix read from file fn.
   * 
   * @throws IOException
   */
  public static ArrayList<SparseMatrixVector> readSparseMatrix(String fn,
                                                               JProgressBar pb)
      throws IOException
  {
    ArrayList<SparseMatrixVector> sprsMtrx;

    // create input stream

    FileInputBuffer fib = new FileInputBuffer(fn);

    // read in sparse matrix size and sparse matrix vectors

    int n = fib.readInt();
    sprsMtrx = new ArrayList<SparseMatrixVector>(n);
    if (pb != null) pb.setMaximum(n);
    for (int i = 0; i < n; ++i)
    {
      SparseMatrixVector smv = new SparseMatrixVector();
      smv.readVector(fib);
      sprsMtrx.add(smv);
      if (pb != null)
      {
        pb.setValue(i);
        pb.repaint();
      }
    }
    if (pb != null) pb.setValue(n);

    // done ... close file and return sparse matrix

    fib.close();
    return sprsMtrx;
  }

  /**
   * Writes the input sparse matrix sprsMtrx to the file fn.
   * 
   * @param fn
   *          The file into which sprsMtrx will be written.
   * @param sprsMtrx
   *          The sparse matrix that will be written to file fn.
   * 
   * @throws IOException
   */
  public static void writeSparseMatrix(String fn,
         ArrayList<SparseMatrixVector> sprsMtrx,
         JProgressBar pb) throws IOException
  {
    // create output stream

    FileOutputBuffer fob = new FileOutputBuffer(fn);

    // write the sparse matrix size and its SparseMatrixVectors to the file

    fob.writeInt(sprsMtrx.size());
    if (pb != null) pb.setMaximum(sprsMtrx.size());
    for (int i = 0; i < sprsMtrx.size(); ++i)
    {
      sprsMtrx.get(i).writeVector(fob);
      if (pb != null) pb.setValue(i);
    }

    // done ... close and exit

    fob.close();
  }

  /**
   * Static function that returns the maximum column index from all rows in the
   * input sparse matrix. The maximum row index is simply sprsMtrx.size().
   * 
   * @param sprsMtrx The input sparse matrix whose maximum column index will be
   *                 returned. 
   * @return The maximum column index represented in sprsMtrx.
   */
  public static int getSparseMatrixMaxIndex(ArrayList<SparseMatrixVector> sprsMtrx)
  {
    int maxIndx = -1;
    for (int i = 0; i < sprsMtrx.size(); ++i)
    {
      SparseMatrixVector smv = sprsMtrx.get(i);
      if (smv.getMaxIndex() > maxIndx) maxIndx = smv.getMaxIndex();
    }

    return maxIndx;
  }

  /**
   * Static function that returns the total number of entries in the
   * input sparse matrix.
   * 
   * @param sprsMtrx The input sparse matrix whose total entry count will be
   *                 returned. 
   * @return The total number of entries in the sparse matrix.
   */
  public static long getSparseMatrixEntries(ArrayList<SparseMatrixVector> sprsMtrx)
  {
    long nEntries = 0;
    for (int i = 0; i < sprsMtrx.size(); ++i)
      nEntries += sprsMtrx.get(i).size();

    return nEntries;
  }

  /**
   * Static function that returns the total amount of memory used by the
   * sparse matrix in bytes.
   * 
   * @param sprsMtrx The input sparse matrix whose total entry count will be
   *                 returned.
   * @return The total amount of memory used by the sparse matrix
   *         in bytes.
   */
  public static long getMemory(ArrayList<SparseMatrixVector> sprsMtrx)
  {
    long mem = 0;
    for (int i = 0; i < sprsMtrx.size(); ++i)
      mem += sprsMtrx.get(i).memoryEstimate();

    return mem;
  }

  /**
   * Orders the column index array in ascending order if it is found to be
   * out of order.
   * 
   * @param sprsMtrx The input sparse matrix to be ordered.
   */
  public static void orderIndexArray(ArrayList<SparseMatrixVector> sprsMtrx,
                                     JProgressBar pb)
  {
    int j;

    // loop over all sparse matrix vectors and order each if out of order

    if (pb != null) pb.setMaximum(sprsMtrx.size());
    for (int i = 0; i < sprsMtrx.size(); ++i)
    {
      // get sparse matrix vector index and value arrays

      SparseMatrixVector smv = sprsMtrx.get(i);
      int[]    indx = smv.getIndexArray();
      double[] valu = smv.getValueArray();

      // if out of order break early

      for (j = 1; j < indx.length; ++j)
        if (indx[j-1] > indx[j]) break;

      // if j < length of array then out of order ... sort

      if (j < indx.length) IntrinsicSort.sort(indx, valu);
      if (pb != null) pb.setValue(i);
    }
  }

  /**
   * Orders the column index array in ascending order if it is found to be
   * out of order (the value array is adjusted accordingly.
   * 
   * @param smv The input SparseMatrixVector to be ordered.
   */
  public static void orderIndexArray(SparseMatrixVector smv)
  {
    int j;
    int[]    indx = smv.getIndexArray();
    double[] valu = smv.getValueArray();

    // if out of order break early

    for (j = 1; j < smv.size(); ++j)
      if (indx[j-1] > indx[j]) break;

    // if j < length of array then out of order ... sort

    if (j < smv.size()) IntrinsicSort.sort(indx, valu, 0, smv.size());
  }
}
