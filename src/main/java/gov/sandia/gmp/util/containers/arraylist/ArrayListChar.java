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
package gov.sandia.gmp.util.containers.arraylist;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Intrinsic "char" resizable-array list implementation.  Implements
 * all optional list operations (This class is roughly equivalent to
 * <tt>Vector</tt>, except that it is unsynchronized.)<p>
 *
 * The <tt>size</tt>, <tt>isEmpty</tt>, <tt>get</tt>, and <tt>set</tt>,
 * operations run in constant time.  The <tt>add</tt> operation runs in
 * <i>amortized constant time</i>, that is, adding n elements requires O(n) time.
 * All of the other operations run in linear time (roughly speaking).
 * The constant factor is low compared to that for the
 * <tt>LinkedList</tt> implementation.<p>
 *
 * Each <tt>ArrayList</tt> instance has a <i>capacity</i>.  The capacity is
 * the size of the array used to store the elements in the list.  It is always
 * at least as large as the list size.  As elements are added to an ArrayList,
 * its capacity grows automatically.  The details of the growth policy are not
 * specified beyond the fact that adding an element has constant amortized
 * time cost.<p>
 *
 * An application can increase the capacity of an <tt>ArrayList</tt> instance
 * before adding a large number of elements using the <tt>ensureCapacity</tt>
 * operation.  This may reduce the amount of incremental reallocation.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access an <tt>ArrayList</tt> instance concurrently,
 * and at least one of the threads modifies the list structurally, it
 * <i>must</i> be synchronized externally.  (A structural modification is
 * any operation that adds or deletes one or more elements, or explicitly
 * resizes the backing array; merely setting the value of an element is not
 * a structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the list.
 *
 * If no such object exists, the list should be "wrapped" using the
 * {@link Collections#synchronizedList Collections.synchronizedList}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the list:<pre>
 *   List list = Collections.synchronizedList(new ArrayList(...));</pre>
 *
 * @author  Jim Hipp
 * @see     java.util.ArrayList
 */
@SuppressWarnings("serial")
public class ArrayListChar implements Serializable
{
  /**
   * The array buffer into which the elements of the ArrayList are stored.
   * The capacity of the ArrayList is the length of this array buffer.
   */
  private char [] aldA = null;

  /**
   * The size of the ArrayList (the number of elements it contains).
   */
  private int       aldSize = 0;

  /**
   * Constructs an empty list with an initial capacity of eight.
   */
  public ArrayListChar()
  {
    this(8);
  }

  /**
   * Constructs an empty list with the specified initial capacity.
   *
   * @param   initialCapacity   the initial capacity of the list
   * @exception IllegalArgumentException if the specified initial capacity
   *            is negative
   */
  public ArrayListChar(int initialCapacity)
  {
    if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal Capacity: " +
                                         initialCapacity);
    this.aldA = new char[initialCapacity];
  }

  /**
   * Constructs a list containing the input array values..
   *
   * @param a The array of elements to be copied into this list.
   */
  public ArrayListChar(char[] a)
  {
    aldA = Arrays.copyOf(a, a.length);
    aldSize = aldA.length;
  }

  /**
   * Returns the current list capacity.
   * 
   * @return The current list capacity.
   */
  public int capacity()
  {
    return aldA.length;
  }
  
  /**
   * Increases the capacity of this <tt>ArrayListD</tt> instance, if
   * necessary, to ensure that it can hold at least the number of elements
   * specified by the minimum capacity argument.
   *
   * @param   minCapacity   the desired minimum capacity
   */
  public void ensureCapacity(int minCapacity)
  {
    if (minCapacity > aldA.length)
    {
      int newCapacity = (aldA.length * 3)/2 + 1;
      if (newCapacity < minCapacity) newCapacity = minCapacity;
      aldA = Arrays.copyOf(aldA, newCapacity);
    }
  }

  /**
   * Replaces the element at the specified position in this list with
   * the specified element.
   *
   * @param  index index of the element to replace
   * @param  val element to be stored at the specified position
   * @return the element previously at the specified position
   * @throws java.lang.IndexOutOfBoundsException.IndexOutOfBoundsException
   */
  public char set(int index, char val) // replace
  {
    if ((index < 0) || (index >= aldSize)) throw new IndexOutOfBoundsException();
    char tmp = aldA[index];
    aldA[index] = val;
    return tmp;
  }

  /**
   * Returns the number of elements in this list.
   *
   * @return the number of elements in this list
   */
  public int size()
  {
    return aldSize;
  }

  /**
   * Appends the specified element to the end of this list.
   *
   * @param val element to be appended to this list
   * @return <tt>true</tt> (as specified by {@link Collection#add})
   */
  public boolean add(char val) // append
  {
    ensureCapacity(aldSize+1);
    aldA[aldSize++] = val;
    return true;
  }

  /**
   * Inserts the specified element at the specified position in this
   * list. Shifts the element currently at that position (if any) and
   * any subsequent elements to the right (adds one to their indices).
   *
   * @param  index index at which the specified element is to be inserted
   * @param  val element to be inserted
   * @throws java.lang.IndexOutOfBoundsException.IndexOutOfBoundsException
   */
  public void add(int index, char val) // insert
  {
    if ((index < 0) || (index > aldSize)) throw new IndexOutOfBoundsException();
    ensureCapacity(aldSize+1);
    System.arraycopy(aldA, index, aldA, index + 1, aldSize - index);
    aldA[index] = val;
    ++aldSize;
  }

  /**
   * Returns the element at the specified position in this list.
   *
   * @param  i index of the element to return
   * @return the element at the specified position in this list
   * @throws java.lang.IndexOutOfBoundsException.IndexOutOfBoundsException
   */
  public char get(int i)
  {
    return aldA[i];
  }

  /**
   * Returns the element at the end of the list.
   *
   * @return the element at the end of this list
   * @throws java.lang.IndexOutOfBoundsException.IndexOutOfBoundsException
   */
  public char getLast()
  {
    return aldA[aldSize-1];
  }

  /**
   * Removes all of the elements from this list.  The list will
   * be empty after this call returns.
   */
  public void clear()
  {
    aldSize = 0;
  }

  /**
   * Returns <tt>true</tt> if this list contains no elements.
   *
   * @return <tt>true</tt> if this list contains no elements
   */
  public boolean isEmpty()
  {
    return (aldSize == 0);
  }

  /**
   * Returns <tt>true</tt> if this list contains the specified element.
   * More formally, returns <tt>true</tt> if and only if this list contains
   * at least one element <tt>e</tt> such that
   * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
   *
   * @param  val element whose presence in this list is to be tested
   * @return <tt>true</tt> if this list contains the specified element
   */
  public boolean contains(char val)
  {
    return (indexOf(val) >= 0);
  }

  /**
   * Returns the index of the first occurrence of the specified element
   * in this list, or -1 if this list does not contain the element.
   * More formally, returns the lowest index <tt>i</tt> such that
   * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
   * or -1 if there is no such index.
   */
  public int indexOf(char val)
  {
    int i;
    
    for (i = 0; i < aldSize; ++i) if (aldA[i] == val) return i;
    
    return -1;
  }

  /**
   * Returns the index of the last occurrence of the specified element
   * in this list, or -1 if this list does not contain the element.
   * More formally, returns the highest index <tt>i</tt> such that
   * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
   * or -1 if there is no such index.
   */
  public int lastIndexOf(char val)
  {
    int i;
    
    for (i = aldSize-1; i > -1; --i)
    {
      if (aldA[i] == val) return i;
    }
    return -1;
  }

  /**
   * Removes the element at the specified position in this list.
   * Shifts any subsequent elements to the left (subtracts one from their
   * indices).
   *
   * @param  index the index of the element to be removed
   * @return the element that was removed from the list
   * @throws java.lang.IndexOutOfBoundsException.IndexOutOfBoundsException
   */
  public char remove(int index)
  {
    if ((index < 0) || (index >= aldSize)) throw new IndexOutOfBoundsException();

    char oldval = aldA[index];
    if (index < aldSize - 1)
      System.arraycopy(aldA, index+1, aldA, index, aldSize - index - 1);
    --aldSize;

    return oldval;
  }

  /**
   * Removes the element at the specified position in this list. The removed
   * element is replaced with the item in the last position of the list.
   * This changes the order but is a very fast way to remove elements if the
   * order is not important.
   * 
   * @param  index the index of the element to be removed
   * @return the element that was removed from the list
   * @throws java.lang.IndexOutOfBoundsException.IndexOutOfBoundsException
   */
  public char removeUnordered(int index)
  {
    if ((index < 0) || (index >= aldSize)) throw new IndexOutOfBoundsException();

    char tmp = aldA[index];
    aldA[index] = aldA[--aldSize];
    return tmp;
  }

  /**
   * Removes the first occurrence of the specified element from this list,
   * if it is present.  If the list does not contain the element, it is
   * unchanged.  More formally, removes the element with the lowest index
   * <tt>i</tt> such that
   * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
   * (if such an element exists).  Returns <tt>true</tt> if this list
   * contained the specified element (or equivalently, if this list
   * changed as a result of the call).
   *
   * @param  val element to be removed from this list, if present
   * @return <tt>true</tt> if this list contained the specified element
   */
  public boolean removeValue(char val)
  {
    int i;

    for (i = 0; i < aldSize; ++i)
    {
      if (aldA[i] == val)
      {
        remove(i);
        return true;
      }
    }
    
    return false;
  }

  /**
   * Removes the last element in the list. Fast way to do a stack
   * 
   * @return the last element in the list
   * @throws java.lang.IndexOutOfBoundsException.IndexOutOfBoundsException
   */
  public char removeLast()
  {
    if (aldSize == 0) throw new IndexOutOfBoundsException();
    return aldA[--aldSize];
  }

  /**
   * Returns a copy of this <tt>ArrayListD</tt> instance.
   *
   * @return a clone of this <tt>ArrayListD</tt> instance
   */
  @Override
public Object clone()
  {
    ArrayListChar v =  new ArrayListChar();
    v.aldA = Arrays.copyOf(aldA, aldSize);
    v.aldSize = aldSize;
    return v;
  }

  /**
   * Returns an array containing all of the elements in this list
   * in proper sequence (from first to last element).
   *
   * <p>The returned array will be "safe" in that no references to it are
   * maintained by this list.  (In other words, this method must allocate
   * a new array).  The caller is thus free to modify the returned array.
   *
   * <p>This method acts as bridge between array-based and collection-based
   * APIs.
   *
   * @return an array containing all of the elements in this list in
   *         proper sequence
   */
  public char [] toArray()
  {
    return Arrays.copyOf(aldA, aldSize);
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
   * @return The backing array of this list.
   */
  public char [] getArray()
  {
    return aldA;
  }

  /**
   * Sets the input array as the new container.
   *
   * <p> The set array is not safe in the sense that it has become the true
   * array container of this list and should only be used or modified by
   * any other outside references.
   *
   * <p>This method acts as bridge between array-based and collection-based
   * APIs.
   *
   * @param a The new backing array of this list
   */
  public void setArray(char[] a)
  {
    aldA = a;
    aldSize = a.length;
  }

  /**
   * Trims the capacity of this <tt>ArrayListD</tt> instance to be the
   * list's current size.  An application can use this operation to minimize
   * the storage of an <tt>ArrayListD</tt> instance.
   */
  public void trimToSize()
  {
    if (aldSize < aldA.length) aldA = Arrays.copyOf(aldA, aldSize);
  }

  /**
   * Sets the size equal to the input value sze. The array is ensured to be of
   * the proper capacity before setting the size. The new elements (if any)
   * are not initialized.
   */
  public void setSize(int sze)
  {
    if (sze > 0)
    {
      ensureCapacity(sze);
      aldSize = sze;
    }
  }
}
