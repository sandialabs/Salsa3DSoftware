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

import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

@SuppressWarnings("serial")
public class SparseMatrixVectorArrayFixed implements Serializable
{
  //private static SMVEntry aSMV0 = null;
  private static final int SINT = Integer.SIZE / 8;
  private static final int SDBL = Double.SIZE / 8;

  private int[]             aIndexArray = null;
  private double[]          aValueArray = null;
  private int[]             aRowStart   = null;
  
  public SparseMatrixVectorArrayFixed(int nrows, int nentries)
  {
    aIndexArray = new int    [nentries];
    aValueArray = new double [nentries];
    aRowStart   = new int    [nrows + 1];
  }

  public void setEntry(int i, int j, int index, double value)
  {
    int p = aRowStart[i] + j;
    aIndexArray[p] = index;
    aValueArray[p] = value;
  }

  public void setRowStart(int i, int rs)
  {
    aRowStart[i] = rs;
  }

  public int getRowStart(int i)
  {
    return aRowStart[i];
  }

  public long getMemory()
  {
    return aRowStart.length * SINT + aIndexArray.length * (SINT + SDBL) + 32;
  }

  public int size()
  {
    return aRowStart.length - 1;
  }
  
  public int getSize(int i)
  {
    return aRowStart[i+1] - aRowStart[i];
  }

  public int getIndex(int i, int j)
  {
    return aIndexArray[aRowStart[i] + j];
  }

  public int[] getIndexArray()
  {
    return aIndexArray;
  }

  public double getValue(int i, int j)
  {
    return aValueArray[aRowStart[i] + j];
  }
  
  public double[] getValueArray()
  {
    return aValueArray;
  }

  public long getEntryCount()
  {
    return aIndexArray.length;
  }

  public void clear()
  {
    aIndexArray = null;
    aValueArray = null;
  }
//
//  public class SMVEntry
//  {
//    private int      aMaxIndex = -1;
//    private int[]    aIndex    = null;
//    private double[] aValue    = null;
//
//    public SMVEntry(int n)
//    {
//      aIndex = new int [n];
//      aValue = new double [n];
//    }
//
//    public SMVEntry(FileInputBuffer fib) throws IOException
//    {
//      read(fib);
//    }
//
//    public int size()
//    {
//      return aIndex.length;
//    }
//
//    public void setEntry(int j, int index, double value)
//    {
//      if (aMaxIndex < index) aMaxIndex = index;
//      aIndex[j] = index;
//      aValue[j] = value;
//    }
//
//    public void read(FileInputBuffer fib) throws IOException
//    {
//      int n = fib.readInt();
//      aIndex = new int [n];
//
//      aMaxIndex = -1;
//      for (int i = 0; i < n; ++i)
//      {
//        int indx = fib.readInt();
//        if (aMaxIndex < indx) aMaxIndex = indx;
//        aIndex[i] = indx;
//      }
//      if (n > 0)
//        aValue = fib.readDoubles();
//      else
//        aValue = new double [0];
//    }
//
//    public void write(FileOutputBuffer fob) throws IOException
//    {
//      fob.writeInt(aIndex.length);
//      if (aIndex.length > 0)
//      {
//        for (int i = 0; i < aIndex.length; ++i) fob.writeInt(aIndex[i]);
//        fob.writeDoubles(aValue);
//      }
//    }
//
//    public long getMemory()
//    {
//      return (aIndex.length + 3) * SINT + aValue.length * SDBL + 24;
//    }
//  }

  //private long       aEntryCount = 0;
  //private SMVEntry[] aSMVArray   = null;
  
  public SparseMatrixVectorArrayFixed(int nrows)
  {
    aRowStart = new int [nrows + 1];
  }

  public SparseMatrixVectorArrayFixed(FileInputBuffer fib) throws IOException
  {
    read(fib);
  }
  
  public SparseMatrixVectorArrayFixed(String pathFile) throws IOException
  {
    read(pathFile);
  }

//  public long getMemory()
//  {
//    long mem = 0;
//    for (int i = 0; i < aSMVArray.length; ++i) mem += aSMVArray[i].getMemory();
//    return mem + (aSMVArray.length + 1) * SDBL + SINT;
//  }

//  public long getEntryMemory(int i)
//  {
//    return aSMVArray[i].getMemory();
//  }
//
//  public long getEntryCount()
//  {
//    return aEntryCount;
//  }
//
//  public void clear()
//  {
//    for (int i = 0; i < aSMVArray.length; ++i) aSMVArray[i] = null;
//    aEntryCount = 0;
//  }

  public static SparseMatrixVectorArrayFixed
         createTransposedFixedSMVArray(ArrayList<SparseMatrixVector> smvArray,
                                       int colStart, int nTrnspRows)
  {
    SparseMatrixVectorArrayFixed smvf = null;

    int nentries = 0;
    int[] count = new int [nTrnspRows];
    for (int i = 0; i < smvArray.size(); ++i)
    {
      SparseMatrixVector smv = smvArray.get(i);
      nentries += smv.size();
      int[] indx = smv.getIndexArray();
      for (int j = 0; j < smv.size(); ++j) ++count[indx[j]];
    }
    
    // set row starts and re-zero count array
    
    smvf = new SparseMatrixVectorArrayFixed(nTrnspRows, nentries);
    //for (int i = 0; i < smvf.size(); ++i) smvf.setEntrySize(i, count[i]);
    int rowStart = 0;
    for (int i = 0; i < count.length; ++i)
    {
      smvf.setRowStart(i, rowStart);
      rowStart += count[i];
      count[i] = 0;
    }
    smvf.setRowStart(count.length, rowStart);

    // loop again over input SMV array and build fixed SMV array

    for (int i = 0; i < smvArray.size(); ++i)
    {
      // get SMV entry and its index and value arrays

      SparseMatrixVector smv = smvArray.get(i);
      int[]    indx = smv.getIndexArray();
      double[] valu = smv.getValueArray();
      
      // loop over each index and value and set into the fixed SMV

      for (int j = 0; j < smv.size(); ++j)
      {
        int ii = indx[j];
        smvf.setEntry(ii, count[ii]++, i + colStart, valu[j]);
      }
    }

    // done

    return smvf;
  }

//  public int size()
//  {
//    return aSMVArray.length;
//  }
//  
//  public int getSize(int i)
//  {
//    return aSMVArray[i].aIndex.length;
//  }
//  
//  public int getMaxIndex(int i)
//  {
//    return aSMVArray[i].aMaxIndex;
//  }
//
//  public int getIndex(int i, int j)
//  {
//    return aSMVArray[i].aIndex[j];
//  }
//
//  public int[] getIndexArray(int i)
//  {
//    return aSMVArray[i].aIndex;
//  }
//
//  public double getValue(int i, int j)
//  {
//    return aSMVArray[i].aValue[j];
//  }
//  
//  public double[] getValueArray(int i)
//  {
//    return aSMVArray[i].aValue;
//  }
//  
//  public void setEntrySize(int i, int entrySize)
//  {
//    if (aSMVArray[i] != null) aEntryCount -= aSMVArray[i].size();
//    aSMVArray[i] = new SMVEntry(entrySize);
//    aEntryCount += entrySize;
//  }

//  public void setEntry(int i, int j, int index, double value)
//  {
//    aSMVArray[i].setEntry(j, index, value);
//  }

  public void read(String pathFile) throws IOException
  {
    FileInputBuffer fib = new FileInputBuffer(pathFile);
    read(fib);
    fib.close();
  }
  
  public void read(FileInputBuffer fib) throws IOException
  {
    aRowStart = fib.readInts();
    aIndexArray = fib.readInts();
    aValueArray = fib.readDoubles();
  }

  public void write(String pathFile) throws IOException
  {
    FileOutputBuffer fob = new FileOutputBuffer(pathFile);
    write(fob);
    fob.close();
  }
  
  public void write(FileOutputBuffer fob) throws IOException
  {
    fob.writeInts(aRowStart);
    fob.writeInts(aIndexArray);
    fob.writeDoubles(aValueArray);
  }
}
