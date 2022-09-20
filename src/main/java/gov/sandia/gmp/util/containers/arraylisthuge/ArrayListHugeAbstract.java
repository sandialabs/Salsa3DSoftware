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
package gov.sandia.gmp.util.containers.arraylisthuge;

import java.io.IOException;

import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * Base class used by all intrinsic ArrayListHugeXXX objects.
 * 
 * @author jrhipp
 *
 */
public abstract class ArrayListHugeAbstract
{
  /**
   * The size of each segment stored in the parent ArrayListHuge container.
   * This is big enough so that 64bit object references, longs, and doubles
   * can all be safely allocated within the JAVA limitation Integer.MAX_VALUE
   * bytes. 
   */
  protected static final int segArraySize = (Integer.MAX_VALUE - 16) / 8;

  /**
   * The total number of entries stored in the parent ArrayListHuge container.
   */
  protected long      aldASize          = 0;

  /**
   * Abstract function that returns the number of array segments allocated by
   * the parent ArrayListHuge object.
   * 
   * @return The number of allocated array segments.
   */
  public abstract int getSegmentCount();

  /**
   * Abstract function defined by a sub classes that gives the intrinsic
   * byte size of a single entry.
   * 
   * @return The intrinsic byte size of a single entry in the list.
   */
  public abstract int intrinsicSize();

  /**
   * Returns the segment array size (i.e. the number of entries allocated for
   * each segment created by the list).
   * 
   * @return The segment array size (i.e. the number of entries allocated for
   *         each segment created by the list).
   */
  public int getSegmentArraySize()
  {
  	return segArraySize;
  }

  /**
   * Returns true if no entries are stored in this ArrayListHuge object.
   * 
   * @return true if no entries are stored in this ArrayListHuge object.
   */
  public boolean isEmpty()
  {
    return aldASize == 0;
  }

  /**
   * Returns the number of entries stored in this ArrayListHuge object.
   * 
   * @return The number of entries stored in this ArrayListHuge object.
   */
  public long size()
  {
    return aldASize;
  }

  /**
   * Resets the list size to zero. Note: The list capacity is not changed by
   * this method ... only the list size (aldASize = 0).
   */
  protected void resetSize()
  {
    aldASize = 0;
  }

  /**
   * Resets the lists size to the index prescribed by the input size. Note:
   * The list capacity is not changed by this method ... only the list size
   * (aldASize) if the input size is smaller than the current size.
   * 
   * @param size The new list size.
   */
  public void resetSize(long size)
  {
  	if (size < aldASize) aldASize = size;
  }

  /**
   * Resets the lists size to the index prescribed by the input segment (si)
   * and element (ei) indices. Note: The list capacity is not changed by this
   * method ... only the list size (aldASize) if the input size is smaller than
   * the current size.
   * 
   * @param si The segment index.
   * @param ei The element index.
   */
  public void resetSize(int si, int ei)
  {
  	long newIndex = getIndex(si, ei);
  	if (newIndex < aldASize) aldASize = newIndex;
  }

  /**
   * The total memory size (in bytes) allocated to this object. Does not
   * include the additional memory allocated by Java to represent Array
   * objects and a single array list.
   * 
   * @return The total memory size.
   */
  public long memoryEstimate()
  {
    return capacity() * intrinsicSize();
  }

  /**
   * Returns the primary index given the input segment (si) and element indices.
   * @param si The segment index.
   * @param ei The element index.
   * @return The primary index.
   */
  protected long getIndex(int si, int ei)
  {
  	return (long) si * getSegmentArraySize() + ei;
  }

  /**
   * Returns the segment index given the index position of an element.
   * 
   * @param index The index position of an element in the huge list.
   * @return The segment index given the index position of an element.
   */
  public int getSegmentIndex(long index)
  {
  	return (int) (index / getSegmentArraySize());
  }
  
  /**
   * Returns a segments elementIndex given the index position of an element.
   * 
   * @param index The index position of an element in the huge list.
   * @return A segments elementIndex given the index position of an element.
   */
  public int getElementIndex(long index)
  {
  	return (int) (index % getSegmentArraySize());
  }

  /**
   * Returns the total element capacity of this list. The capacity is always
   * an increment of the total array segment entry size (getSegmentArraySize()).
   * 
   * @return The total element capacity of this list.
   */
  public long capacity()
  {
  	return (long) getSegmentCount() * getSegmentArraySize();
  }

  /**
   * Throws an error if the input segment index (si), element index (ei), and
   * increment (n) exceed the current list size (aldASize), or if si or ei are
   * negative.
   * 
   * @param si Segment Index.
   * @param ei Element Index.
   * @param n  Increment.
   */
  public void validateIndex(int si, int ei, int n)
  {
  	if ((si < 0) || (ei < 0) || (getIndex(si, ei) + n >= aldASize))
  	{
    	throw new IndexOutOfBoundsException(String.format(
		      "index/segment/element %d/%d/%d out of range ...",
		      getIndex(si, ei), si, ei));  		
  	}
  }

  /**
   * Fills this array list with the contents of the input file (filename).
   * 
   * @param filename The input file that will be loaded into this array list.
   * @throws IOException
   */
  public void read(String filename) throws IOException
  {
  	FileInputBuffer fib = new FileInputBuffer(filename);
  	read(fib);
  	fib.close();
  }

  /**
   * Reads data from the input file buffer and fills this ArrayList with the
   * contents.
   * 
   * @param fib The FileInputBuffer from which this array list is filled.
   * @throws IOException
   */
  public abstract void read(FileInputBuffer fib) throws IOException;

  /**
   * Writes this huge array list to the input file (filename).
   * 
   * @param filename The output file where this array list will be written.
   * @throws IOException
   */
  public void write(String filename) throws IOException
  {
    FileOutputBuffer fob = new FileOutputBuffer(filename);
    write(fob);
    fob.close();
  }

  /**
   * Writes this array list to the input file output buffer.
   * 
   * @param fob The output file buffer containing the location where this 
   *        array list will be written.
   * @throws IOException
   */
  public abstract void write(FileOutputBuffer fob) throws IOException;
}
