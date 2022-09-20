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

import java.io.IOException;
import java.io.Serializable;

import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

@SuppressWarnings("serial")
public abstract class ArrayListIntrinsic implements Serializable
{

  /**
   * The size of the ArrayList (the number of elements it contains).
   */
  protected int       aldSize = 0;

  /**
   * Increases the capacity of this <tt>ArrayList</tt> instance, if
   * necessary, to ensure that it can hold at least the number of elements
   * specified by the minimum capacity argument.
   *
   * @param   minCapacity   the desired minimum capacity
   */
  public void ensureCapacity(int minCapacity)
  {
    if (minCapacity > capacity())
    {
      int newCapacity = (int) (((long) capacity() * 3)/2 + 1);
      if (newCapacity < minCapacity) newCapacity = minCapacity;
      copyOf(newCapacity);
    }
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

  public abstract int capacity();
  protected abstract void copyOf(int newCapacity);
  protected abstract int intrinsicSize();

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
