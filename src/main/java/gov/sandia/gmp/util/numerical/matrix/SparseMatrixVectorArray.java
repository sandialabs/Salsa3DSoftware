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
import java.util.ArrayList;

import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.numerical.intrinsicsort.IntrinsicSort;

@SuppressWarnings("serial")
public class SparseMatrixVectorArray implements Serializable
{
  private final static int         SINT       = Integer.SIZE / 8;
  private final static int         SDBL       = Double.SIZE / 8;

  /**
   * The row or column index to which each entry in aValue belongs.
   */
  private ArrayListInt             aIndex     = null;

  /**
   * The sparse matrix entry vector.
   */
  private ArrayListDouble          aValue     = null;

  /**
   * Maximum index stored in aIndex for each row. Set to -1 if no indices have
   * been added to aIndex. All indices added to aIndex are assumed to be
   * equal to or larger than 0 (positive).
   */
  private ArrayListInt             aMaxIndx   = null;

  /**
   * The row or column index to which each entry in aValue belongs.
   */
  private ArrayListInt             aRowStrt   = null;

  private transient int            aRow       = -1;
  
  private transient int            aRowOffst  = -1;

  private transient int            aEntry     = -1;

  //***************************************************************************

  public SparseMatrixVectorArray(int nrows)
  {
    aIndex   = new ArrayListInt(nrows * 8);
    aValue   = new ArrayListDouble(nrows * 8);
    aMaxIndx = new ArrayListInt(nrows);
    aRowStrt = new ArrayListInt(nrows + 1);
  }

  public SparseMatrixVectorArray()
  {
    aIndex   = new ArrayListInt(16 * 8);
    aValue   = new ArrayListDouble(16 * 8);
    aMaxIndx = new ArrayListInt(16);
    aRowStrt = new ArrayListInt(17);
  }

  //***************************************************************************

  /**
   * Returns the current size of the sparse vector.
   * 
   * @return The current size of the sparse vector.
   */
  public int rowCount()
  {
    return aMaxIndx.size();
  }

  public int entryCount()
  {
    return aIndex.size();
  }

  //***************************************************************************

  public void clear()
  {
    aIndex.clear();
    aValue.clear();
    aMaxIndx.clear();
    aRowStrt.clear();
    aRow = aRowOffst = aEntry = -1;    
  }

  public void add(int[] indx, double[] valu)
  {
    add(indx.length, indx, valu);
  }
  
  public void add(int n, int[] indx, double[] valu)
  {
    int maxindx = -1;
    for (int i = 0; i < n; ++i)
    {
      if (maxindx < indx[i]) maxindx = indx[i];
      aIndex.add(indx[i]);
      aValue.add(valu[i]);
    }
    aMaxIndx.add(maxindx);
    aRowStrt.add(aIndex.size());
  }

  public void add(SparseMatrixVector smv)
  {
    int[]    indx = smv.getIndexArray();
    double[] valu = smv.getValueArray();
    for (int i = 0; i < smv.size(); ++i)
    {
      aIndex.add(indx[i]);
      aValue.add(valu[i]);
    }
    aMaxIndx.add(smv.getMaxIndex());
    aRowStrt.add(aIndex.size());
  }

  public void add(ArrayList<SparseMatrixVector> smvList)
  {
    for (int i = 0; i < smvList.size(); ++i) add(smvList.get(i));
  }

  //***************************************************************************

  /**
   * Reads this sparse vector array from the file at pth.
   * 
   * @param pth The path/file name from which this sparse vector array is
   *            read.
   * @throws IOException
   */
  public void readVector(String pth) throws IOException
  {
    FileInputBuffer fib = new FileInputBuffer(pth);
    readVector(fib);
    fib.close();
  }

  /**
   * Reads this sparse vector array from the supplied FileInputBuffer fib.
   * 
   * @param fib The file input buffer from which this sparse vector array is
   *            read.
   * @throws IOException
   */
  public void readVector(FileInputBuffer fib) throws IOException
  {
    int n = fib.readInt();
    aMaxIndx = new ArrayListInt(n);
    aRowStrt = new ArrayListInt(n+1);
    for (int i = 0; i < n + 1; ++i)
      aRowStrt.add(fib.readInt());

    n = fib.readInt();
    aIndex = new ArrayListInt(n);
    aValue = new ArrayListDouble(n);
    int[] rs = aRowStrt.getArray();
    int r = 0;
    int mxindx = -1;
    for (int i = 0; i < n; ++i)
    {
      int indx = fib.readInt();
      if (i == rs[r])
      {
        if (i > 0) aMaxIndx.add(mxindx);
        mxindx = indx;
        ++r;
      }
      else
        if (mxindx < indx) mxindx = indx;

      aIndex.add(indx);
      aValue.add(fib.readDouble());
    }
  }

  /**
   * Writes out this sparse vector array into a file at pth.
   * 
   * @param pth The path/file name into which this sparse vector array is
   *            written.
   * @throws IOException
   */
  public void writeVector(String pth) throws IOException
  {
    FileOutputBuffer fob = new FileOutputBuffer(pth);
    writeVector(fob);
    fob.close();
  }

  /**
   * Writes out this sparse vector array to the supplied FileOutputBuffer fob.
   * 
   * @param fob The file output buffer into which this sparse vector array is
   *            written.
   * @throws IOException
   */
  public void writeVector(FileOutputBuffer fob) throws IOException
  {    
    fob.writeInt(aMaxIndx.size());
    for (int i = 0; i < aMaxIndx.size() + 1; ++i)
      fob.writeInt(aRowStrt.get(i));
    
    fob.writeInt(aIndex.size());
    for (int i = 0; i < aIndex.size(); ++i)
    {
      fob.writeInt(aIndex.get(i));
      fob.writeDouble(aValue.get(i));
    }
  }

  //***************************************************************************

  public void setRow(int row)
  {
    aRow      = row;
    aRowOffst = aRowStrt.get(row);
    aEntry    = aRowOffst;
  }

  public void next()
  {
    ++aEntry;
  }

  public boolean hasAnyNext()
  {
    return (aEntry < aRowStrt.getLast());
  }

  public boolean hasRowNext()
  {
    return (aEntry < aRowStrt.get(aRow + 1));
  }

  public int getIndex()
  {
    return aIndex.get(aEntry);
  }

  public int getNextIndex()
  {
    return aIndex.get(aEntry++);
  }

  public double getValue()
  {
    return aValue.get(aEntry);
  }

  public double getNextValue()
  {
    return aValue.get(aEntry++);
  }

  public int getIndex(int j)
  {
    return aIndex.get(aRowOffst + j);
  }

  public double getValue(int j)
  {
    return aValue.get(aRowOffst + j);
  }

  public int getIndex(int row, int j)
  {
    return aIndex.get(aRowStrt.get(row) + j);
  }

  public double getValue(int row, int j)
  {
    return aValue.get(aRowStrt.get(row) + j);
  }

  public int getRowMaxIndex(int row)
  {
    return aMaxIndx.get(row);
  }

  public int getRowStart(int row)
  {
    return aRowStrt.get(row);
  }

  public int getRowEntryCount(int row)
  {
    return aRowStrt.get(row+1) - aRowStrt.get(row);
  }

  //***************************************************************************
  
  /**
   * Normalizes each value in aValue by the column entry in aColNorm whose
   * column is selected using the entry in aIndex.
   * 
   * @param aColNorm The column normalization array.
   */
  public void normalize(double[] aColNorm)
  {
    int[]    indx = aIndex.getArray();
    double[] valu = aValue.getArray();
    for (int j = 0; j < aValue.size(); ++j) valu[j] /= aColNorm[indx[j]];
  }

  /**
   * Trims the index and value arrays allocated capacity to their current size.
   */
  public void trimToSize()
  {
    aMaxIndx.trimToSize();
    aRowStrt.trimToSize();
    aIndex.trimToSize();
    aValue.trimToSize();
  }

  /**
   * Orders the column index array in ascending order if it is found to be
   * out of order (the value array is adjusted accordingly.
   * 
   * @param smv The input SparseMatrixVector to be ordered.
   */
  public void orderIndexArray()
  {
    for (int i = 0; i < rowCount(); ++i) orderIndexArray(i);
  }

  /**
   * Orders the column index array in ascending order if it is found to be
   * out of order (the value array is adjusted accordingly.
   * 
   * @param smv The input SparseMatrixVector to be ordered.
   */
  public void orderIndexArray(int row)
  {
    int ro = aRowStrt.get(row);
    int[]    indx = aIndex.getArray();
    double[] valu = aValue.getArray();

    // if out of order break early

    int j;
    for (j = ro; j < aRowStrt.get(row+1); ++j)
      if (indx[j] > indx[j+1]) break;

    // if j < length of array then out of order ... sort

    if (j < aRowStrt.get(row+1))
      IntrinsicSort.sort(indx, valu, ro, aRowStrt.get(row+1));
  }

  //***************************************************************************

  public static long coreSizeEstimateWthEntryCount(int rowCount, long entryCount)
  {
    return (2 * rowCount + 1) * SINT + entryCount * (SINT + SDBL) + 
           baseMemoryEstimate();
  }

  public static long entryCountEstimateWthCoreSize(int rowCount, long coreMem)
  {
    // coreMem = np * ((2 * rowCount + 1) * SINT + f * entryCount * (SINT + SDBL) + BMem)
    // coreMem / np - BMem - (2 * rowCount + 1) * SINT = f * entryCount * (SINT + SDBL)
    // f * entryCount = (coreMem / np - BMem - (2 * rowCount + 1) * SINT) / (SINT + SDBL)
    return (coreMem - baseMemoryEstimate() - (2 * rowCount + 1) * SINT) /
           (SINT + SDBL);
  }

  public static long fileSizeEstimateWthEntryCount(int rowCount, long entryCount)
  {
    return (rowCount + 3) * SINT + entryCount * (SINT + SDBL);
  }
  
  public static long entryCountEstimateWthFileSize(int rowCount, long fileSize)
  {
    return (fileSize - (rowCount + 3) * SINT) / (SINT + SDBL);
  }

  public long fileSizeEstimate()
  {
    return (rowCount() + 3) * SINT + entryCount() * (SINT + SDBL);
  }

  public long memoryEstimate()
  {
    long mem = (aIndex.capacity() * aMaxIndx.capacity() + aRowStrt.capacity()) *
               SINT + aValue.capacity() * SDBL +
               baseMemoryEstimate();

    return mem;
  }
  
  public static long baseMemoryEstimate()
  {
    // this pointer                         8
    // 3 ints                              24
    // 4 pointers                          32
    // 4 container pointers                32
    // 4 * 2 ints (size and capacity ints) 32

    return 128;
  }
}
