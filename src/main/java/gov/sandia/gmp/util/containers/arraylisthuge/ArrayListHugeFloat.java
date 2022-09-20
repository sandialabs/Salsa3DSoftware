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
import java.util.ArrayList;

import gov.sandia.gmp.util.containers.arraylist.ArrayListFloat;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * Container to hold intrinsic "float" types where the number of entries may
 * exceed the Integer.MAX_VALUE limitation of Java arrays. One should not use
 * this object to store small number of entries as the efficiency is not quite
 * as good as standard array lists. Unlike standard array lists no facility is
 * provided to remove entries (other than the last entry) as this can be very
 * inefficient. Primary operational functions supported include:
 * 
 *    void   add(float val);
 *    float  get(long i);
 *    float  getLast();
 *    float  removeLast();
 *    Object clone();
 *    float  set(long i, float val);
 *    void   setVector(long i, float val, int n);
 *    float  increment(long i, float inc);
 *    void   incrementVector(long i, float inc, int n);
 *    float  scale(long i, float scl);
 *    void   scaleVector(long i, float scl, int n);
 *    void   swap(long i1, long i2);
 *    void   swapVector(long i1, long i2, int n);
 *    void   clear();
 *    void   resetSize();
 *    void   ensureCapacity(long capacity);
 *    void   trimToSegmentSize();
 *    
 * @author jrhipp
 *
 */
public class ArrayListHugeFloat extends ArrayListHugeAbstract
{
	/**
	 * The intrinsic size of this huge float list.
	 */
	private static final int elemIntrinsicSize = Float.SIZE / 8;

  /**
   * Primary storage array list of all huge segment arrays. Each segment
   * is allocated with segArraySize entries so that the total byte storage
   * of a single segment never exceeds segArrayByteSize.
   */
  private ArrayList<float[]>     aldA    = null;

  /**
   * Return the current number of segments stored by this ArrayListHuge object.
   * 
   * @return The current number of segments stored by this ArrayListHuge
   *         object.
   */
  @Override
  public int getSegmentCount()
  {
    return aldA.size();
  }

  /**
   * Returns this object array element intrinsic size (bytes).
   * 
   * @return This object array element intrinsic size (bytes).
   */
  @Override
  public int intrinsicSize()
  {
    return elemIntrinsicSize;
  }

  /**
   * Default constructor.
   */
  public ArrayListHugeFloat()
  {
    aldA = new ArrayList<float[]>();
  }

  /**
   * Constructs a list from the input file name (filename).
   */
  public ArrayListHugeFloat(String filename) throws IOException
  {
    read(filename);
  }

  /**
   * Constructs a list from the input FileInputBuffer.
   */
  public ArrayListHugeFloat(FileInputBuffer fib) throws IOException
  {
  	read(fib);
  }

  /**
   * Standard constructor that ensures the list capacity is at least as big as
   * the input request.
   * 
   * @param capacity The minimum set capacity of the list at construction.
   */
  public ArrayListHugeFloat(long capacity)
  {
  	this();
  	ensureCapacity(capacity);
  }

  /**
   * Standard constructor initializes the list with the values stored in the
   * input array a.
   * 
   * @param a The input array with which the list is initialized.
   */
  public ArrayListHugeFloat(float[] a)
  {
  	aldA = new ArrayList<float []>();
  	aldA.add(allocateSegment());
  	if (a.length <= segArraySize)
    	for (int i = 0; i < a.length; ++i) aldA.get(0)[i] = a[i];
  	else
  	{
    	for (int i = 0; i < segArraySize; ++i) aldA.get(0)[i] = a[i];
    	aldA.add(allocateSegment());
  		for (int i = segArraySize; i < a.length; ++i) aldA.get(1)[i - segArraySize] = a[i];
  	}
    aldASize = a.length;
  }

  /**
   * Standard constructor initializes the list with the values stored in the
   * input list a.
   * 
   * @param a The input list with which this list is initialized.
   */
  public ArrayListHugeFloat(ArrayListFloat a)
  {
  	aldA = new ArrayList<float []>();
  	aldA.add(allocateSegment());
  	if (a.size() <= segArraySize)
    	for (int i = 0; i < a.size(); ++i) aldA.get(0)[i] = a.get(i);
  	else
  	{
    	for (int i = 0; i < segArraySize; ++i) aldA.get(0)[i] = a.get(i);
    	aldA.add(allocateSegment());
  		for (int i = segArraySize; i < a.size(); ++i) aldA.get(1)[i - segArraySize] = a.get(i);
  	}
    aldASize = a.size();
  }

  /**
   * Returns a new allocated list segment to be inserted into the primary list
   * container aldA.
   * 
   * @return A new allocated list segment to be inserted into the primary list
   *         container aldA.
   */
  private float[] allocateSegment()
  {
  	return new float [segArraySize];
  }

  /**
   * Returns a copy of this <tt>ArrayListHuge</tt> instance.
   *
   * @return a clone of this <tt>ArrayListHuge</tt> instance
   */
  @Override
  public Object clone()
  {
  	ArrayListHugeFloat v = new ArrayListHugeFloat();
    for (int i = 0; i < aldA.size(); ++i)
    	v.aldA.add((float []) aldA.get(i).clone());
    v.aldASize = aldASize;
  	return v;
  }

  /**
   * Ensures that the capacity of this list is as least as big as the input
   * capacity. The list capacity will be set to the nearest increment of the
   * segment array element size (segArraySize) that is big enough to hold the
   * input capacity.
   *  
   * @param capacity The new capacity of this list which will be at least as
   *                 big as the input capacity.
   */
  public void ensureCapacity(long capacity)
  {
    if (capacity() < capacity)
    {
      int si = getSegmentIndex(capacity);
      if (getElementIndex(capacity) != 0) ++si;
      for (int i = aldA.size(); i < si; ++i) aldA.add(allocateSegment());
    }
  }

  /**
   * Trims the excess capacity to the nearest segment allocation that can
   * safely contain the current size.
   */
  public void trimToSegmentSize()
  {
  	int si = getSegmentIndex(aldASize);
    if (getElementIndex(aldASize) != 0) ++si;
    while (aldA.size() > si) aldA.remove(aldA.size() - 1);  	
  }

  /**
   * Returns an array containing all of the elements in this list
   * in proper sequence (from first to last element).
   *
   * <p> The returned array is not safe in the sense that it is the true
   * array container of this list and should only be used but not modified.
   *
   * <p>This method acts as bridge between array-based and collection-based
   * APIs.
   *
   * @return The backing array of a single segment of this list.
   */
  public float[] getArraySegment(int seg)
  {
    return aldA.get(seg);
  }

  /**
   * Clears the list and removes all segment allocations.
   */
  public void clear()
  {
    aldA.clear();
    resetSize();
  }
  
  /**
   * Appends the specified element to the end of this list.
   *
   * @param val element to be appended to this list
   */
  public void add(float val)
  {
    // see if a new segment is required and add it if so

    int si = getSegmentIndex(aldASize);
    if (si == aldA.size()) aldA.add(allocateSegment());

   	// add element to segment and increment size

    int ei = getElementIndex(aldASize);
    aldA.get(si)[ei] = val;
    ++aldASize;
  }

  /**
   * Returns the value stored at the list index i.
   * 
   * @param i The index of the value to be returned.
   * @return The value stored at the list index i.
   */
  public float get(long i)
  {
    if (i >= aldASize)
    	throw new IndexOutOfBoundsException("Input index (" + i +
    			                                ") exceeds size (" + aldASize +
    			                                ") ...");

    int si = getSegmentIndex(i);
    int ei = getElementIndex(i);
    return get(si, ei);
  }

  /**
   * Returns the value stored at segment (si) and element (ei) indices.
   * 
   * @param si The segment index.
   * @param ei The element index.
   * @return The value stored at index si,ei.
   */
  public float get(int si, int ei)
  {
    return aldA.get(si)[ei];
  }

  /**
   * Returns the element stored as the last entry in the list.
   * 
   * @return The element stored as the last entry in the list.
   */
  public float getLast()
  {
  	return get(aldASize - 1);
  }

  /**
   * Returns the element stored as the first entry in the list.
   * 
   * @return The element stored as the first entry in the list.
   */
  public float getFirst()
  {
  	return aldA.get(0)[0];
  }

  /**
   * Removes and returns the last element stored in the list.
   * 
   * @return The last element stored in the list.
   */
  public float removeLast()
  {
  	float tmp = get(aldASize - 1);
  	--aldASize;
  	return tmp;
  }

  /**
  /**
   * Replaces the element at the specified index position in this list with
   * the specified element.
   *
   * @param  index Index of the element to replaced.
   * @param  val   Element to be stored at the specified index position.
   * @return The element previously stored at the specified position.
   */
  public float set(long index, float val)
  {
    int si = getSegmentIndex(index);
    int ei = getElementIndex(index);

    return set(si, ei, val);
  }

  /**
   * Replaces the element at the specified segment (si) and element (ei)
   * indices in this list with the specified element (val).
   *
   * @param  si  The segment index.
   * @param  ei  The element index.
   * @param  val Element to be stored at the specified index position.
   * @return The element previously stored at the specified position.
   */
  public float set(int si, int ei, float val)
  {
    validateIndex(si, ei, 0);
    
    float[] aldAsi = aldA.get(si);    
    float tmp = aldAsi[ei];
    aldAsi[ei] = val;
    return tmp;
  }

  /**
   * Replaces n elements beginning with the input index and progressing
   * forward with the specified element val.
   *
   * @param  index Starting index of the element replacement.
   * @param  val   Element to be stored at each index position.
   * @param  n     The number of elements to be replaced beginning with index.
   */
  public void setVector(long indx, float val, int n)
  {
  	int si = getSegmentIndex(indx);
    int ei = getElementIndex(indx);

  	setVector(si, ei, val, n);
  }

  /**
   * Replaces n elements beginning at the input segment (si) and element (ei)
   * index and progressing forward with the specified element (val).
   *
   * @param  si  The starting segment index.
   * @param  ei  The starting element index.
   * @param  val Element to be stored at each index position.
   * @param  n   The number of elements to be replaced beginning with si,ei.
   */
  public void setVector(int si, int ei, float val, int n)
  {
  	validateIndex(si, ei, n);
    for (int i = 0; i < n; ++i)
    {
    	aldA.get(si)[ei++] = val;
  		if (ei == segArraySize) {ei = 0; ++si;}
    }
  }

  /**
   * Increments the element at the specified index position in this list by
   * the amount inc.
   *
   * @param  index Index of the element to incremented.
   * @param  inc   The amount by which the element is incremented.
   * @return The previous value of the element stored at the specified position.
   */
  public float increment(long index, float inc)
  {
    int si = getSegmentIndex(index);
    int ei = getElementIndex(index);

    return increment(si, ei, inc);
  }

  /**
   * Increments the element at the specified segment (si) and element (ei)
   * indices in this list by the amount inc.
   *
   * @param  si  The segment index.
   * @param  ei  The element index.
   * @param  inc The amount by which the element is incremented.
   * @return The previous value of the element stored at the index si,ei.
   */
  public float increment(int si, int ei, float inc)
  {
    validateIndex(si, ei, 0);
    
    float[] aldAsi = aldA.get(si);    
    float tmp = aldAsi[ei];
    aldAsi[ei] += inc;
    return tmp;
  }

  /**
   * Increments n elements beginning with the input index and progressing
   * forward by the amount inc.
   *
   * @param  index Starting index of the elements to be incremented.
   * @param  inc   The amount by which the elements are to be incremented.
   * @param  n     The number of elements to be incremented beginning with index.
   */
  public void incrementVector(long indx, float inc, int n)
  {
  	int si = getSegmentIndex(indx);
    int ei = getElementIndex(indx);

  	incrementVector(si, ei, inc, n);
  }

  /**
   * Increments n elements beginning with the input segment (si) and element (ei)
   * index and progressing forward by the amount inc.
   *
   * @param  si  The starting segment index.
   * @param  ei  The starting element index.
   * @param  inc The amount by which the elements are to be incremented.
   * @param  n   The number of elements to be incremented beginning with si,ei.
   */
  public void incrementVector(int si, int ei, float inc, int n)
  {
  	validateIndex(si, ei, n);
    for (int i = 0; i < n; ++i)
    {
    	aldA.get(si)[ei++] += inc;
  		if (ei == segArraySize) {ei = 0; ++si;}
    }
  }
  
  /**
   * Scales the element at the specified index position in this list by
   * the amount scl.
   *
   * @param  index Index of the element to scaled.
   * @param  scl   The amount by which the element is scaled.
   * @return The previous value of the element stored at the specified position.
   */
  public float scale(long index, float scl)
  {
    int si = getSegmentIndex(index);
    int ei = getElementIndex(index);

    return scale(si, ei, scl);
  }

  /**
   * Scales the element at the specified segment (si) and element (ei)
   * indices in this list by the amount scl.
   *
   * @param  si  The segment index.
   * @param  ei  The element index.
   * @param  scl   The amount by which the element is scaled.
   * @return The previous value of the element stored at the index si,ei.
   */
  public float scale(int si, int ei, float scl)
  {
    validateIndex(si, ei, 0);
    
    float[] aldAsi = aldA.get(si);    
    float tmp = aldAsi[ei];
    aldAsi[ei] *= scl;
    return tmp;
  }

  /**
   * Scales n elements beginning with the input index and progressing
   * forward by the amount scl.
   *
   * @param  index Starting index of the elements to be scaled.
   * @param  scl   The amount by which the elements are to be scaled.
   * @param  n     The number of elements to be scaled beginning with index.
   */
  public void scaleVector(long indx, float scl, int n)
  {
  	int si = getSegmentIndex(indx);
    int ei = getElementIndex(indx);

  	scaleVector(si, ei, scl, n);
  }

  /**
   * Scales n elements beginning with the input segment (si) and element (ei)
   * index and progressing forward by the amount scl.
   *
   * @param  si  The starting segment index.
   * @param  ei  The starting element index.
   * @param  scl   The amount by which the elements are to be scaled.
   * @param  n   The number of elements to be scaled beginning with si,ei.
   */
  public void scaleVector(int si, int ei, float scl, int n)
  {
  	validateIndex(si, ei, n);
    for (int i = 0; i < n; ++i)
    {
    	aldA.get(si)[ei++] *= scl;
  		if (ei == segArraySize) {ei = 0; ++si;}
    }
  }

  /**
   * Swaps the elements at indices indx1 and indx2.
   * 
   * @param indx1 First index to be swapped.
   * @param indx2 Second index to be swapped.
   */
  public void swap(long indx1, long indx2)
  {
  	int si1 = getSegmentIndex(indx1);
    int ei1 = getElementIndex(indx1);
  	int si2 = getSegmentIndex(indx2);
    int ei2 = getElementIndex(indx2);
    swap(si1, ei1, si2, ei2);  	
  }
  
  /**
   * Swaps the elements at segment/element indices si1/ei1 with those at
   * segment/element indices si2/ei2.
   * 
   * @param si1 First segment index to be swapped.
   * @param ei1 First element index to be swapped.
   * @param si2 Second segment index to be swapped.
   * @param ei2 Second element index to be swapped.
   */
  public void swap(int si1, int ei1, int si2, int ei2)
  {
  	float tmp = aldA.get(si1)[ei1];
		aldA.get(si1)[ei1] = aldA.get(si2)[ei2];
		aldA.get(si2)[ei2] = tmp;  	
  }

  /**
   * Swaps n elements beginning at indices indx1 and indx2.
   * 
   * @param indx1 First index to be swapped.
   * @param indx2 Second index to be swapped.
   * @param n     Number of elements to be swapped beginning with indx1 and
   *              indx2.
   */
  public void swapVector(long indx1, long indx2, int n)
  {
  	int si1 = getSegmentIndex(indx1);
    int ei1 = getElementIndex(indx1);
  	int si2 = getSegmentIndex(indx2);
    int ei2 = getElementIndex(indx2);
    swapVector(si1, ei1, si2, ei2, n);
  }

  /**
   * Swaps n elements beginning with segment/element indices si1/ei1 and
   * segment/element indices si2/ei2.
   * 
   * @param si1 First segment index to be swapped.
   * @param ei1 First element index to be swapped.
   * @param si2 Second segment index to be swapped.
   * @param ei2 Second element index to be swapped.
   * @param n   Number of elements to be swapped beginning with si1,ei1 and
   *            si2,ei2.
   */
  public void swapVector(int si1, int ei1, int si2, int ei2, int n)
  {
  	for (int i = 0; i < n; ++i)
  	{
  		swap(si1, ei1++, si2, ei2++);
  		if (ei1 == segArraySize) {ei1 = 0; ++si1;}
  		if (ei2 == segArraySize) {ei2 = 0; ++si2;}
  	}
  }

  /**
   * Reads data from the input file buffer and fills a new Huge ArrayList with
   * the contents.
   * 
   * @param fib The FileInputBuffer from which a new huge ArrayList is filled.
   * @throws IOException
   */
  @Override
  public void read(FileInputBuffer fib) throws IOException
  {
  	clear();
  	long n = fib.readLong();
  	ensureCapacity(n);
  	for (long i = 0; i < n; ++i) add(fib.readFloat());
  }

  /**
   * Writes this huge array list to the input file output buffer.
   * 
   * @param fob The output file buffer containing the location where this 
   *        huge array list will be written.
   * @throws IOException
   */
  @Override
  public void write(FileOutputBuffer fob) throws IOException
  {
	  fob.writeLong(size());
	  for (long i = 0; i < size(); ++i) fob.writeFloat(get(i));
  }
}
