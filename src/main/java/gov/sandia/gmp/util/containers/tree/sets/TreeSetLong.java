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
package gov.sandia.gmp.util.containers.tree.sets;

import gov.sandia.gmp.util.containers.tree.TreeIntrinsic;

/**
 * A Red-Black tree based intrinsic Long set implementation. This set is
 * sorted on the intrinsic key from smallest to largest in the tree.
 * 
 * <p>
 * This implementation provides guaranteed log(n) time cost for the
 * <tt>contains</tt>, <tt>get</tt>, <tt>add</tt> and <tt>remove</tt> operations.
 * Algorithms are adaptations of those in Cormen, Leiserson, and Rivest's
 * <I>Introduction to Algorithms</I>.
 * 
 * <p>
 * <strong> Note that this implementation is not synchronized.</strong> If
 * multiple threads access a map concurrently, and at least one of the threads
 * modifies the map structurally, it <i>must</i> be synchronized externally. (A
 * structural modification is any operation that adds or deletes one or more
 * mappings; merely changing the value associated with an existing key is not a
 * structural modification.) This is typically accomplished by synchronizing on
 * some object that naturally encapsulates the set.
 * 
 * @author jrhipp
 * 
 */
@SuppressWarnings("serial")
public class TreeSetLong extends TreeIntrinsic
{
  /**
   * The entry pair for this set containing the intrinsic Long key. This object
   * extends the base class TreeIntrinsic.Entry which contains the left and
   * right binary tree references, as-well-as a reference to the parent element.
   * This class provides mechanisms to retrieve the key and a means to test for
   * object equality.
   * 
   * @author jrhipp
   * 
   */
  final static class Entry extends TreeIntrinsic.Entry
  {
    /**
     * The key for this entry.
     */
    private long key;

    /**
     * Standard constructor that sets the key and the parent reference.
     * 
     * @param k
     *          The key.
     * @param parent
     *          The parent reference.
     */
    Entry(long k, Entry parent)
    {
      super(parent);
      key = k;
    }

    /**
     * Returns the key.
     * 
     * @return The key.
     */
    public long getKey()
    {
      return key;
    }

    /**
     * Returns true if the input object has the same key as this object.
     * 
     * @param o
     *          The input object.
     */
    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof Entry)) return false;
      Entry e = (Entry) o;
      return (key == e.key);
    }

    /**
     * Returns the key as a string "key".
     */
    @Override
    public String toString()
    {
      return "" + key;
    }
  }

  /**
   * Returns <tt>true</tt> if this set contains a mapping for the specified key.
   * 
   * @param key
   *          The input key whose presence in this set is to be tested.
   * @return <tt>true</tt> if this set contains a mapping for the specified key.
   */
  public boolean contains(long key)
  {
    return getEntry(key) != null;
  }

  /**
   * Returns this map's entry for the given key, or <tt>null</tt> if the map
   * does not contain an entry for the key.
   * 
   * @return This map's entry for the given key, or <tt>null</tt> if the map
   *         does not contain an entry for the key
   */
  private final Entry getEntry(long key)
  {
    // start at the root and traverse the tree to find the containing entry

    Entry p = (Entry) root;
    while (p != null)
    {
      long cmp = key - p.key;
      if (cmp < 0)
        p = (Entry) p.left;
      else if (cmp > 0)
        p = (Entry) p.right;
      else
        return p;
    }
    return null;
  }

  /**
   * Adds the input key to this set.
   * 
   * @param key
   *          The key to be added to the set.
   */
  public void add(long key)
  {
    // if no entries exist add this key as the first entry.

    if (root == null)
    {
      root = new Entry(key, null);
      size = 1;
      return;
    }

    // start at the root and see if the key already exists in the set

    long cmp;
    Entry parent;
    Entry t = (Entry) root;
    do
    {
      parent = t;
      cmp = key - t.key;
      if (cmp < 0)
        t = (Entry) t.left;
      else if (cmp > 0)
        t = (Entry) t.right;
      else
        return;
    } while (t != null);

    // no existing key found ... insert new key on the left or
    // right parent node.

    Entry e = new Entry(key, parent);
    if (cmp < 0)
      parent.left = e;
    else
      parent.right = e;

    // re-balance the tree and increment the count

    fixAfterInsertion(e);
    size++;
  }

  /**
   * Removes the mapping for this key from this TreeSet if present.
   * 
   * @param key
   *          The key for which mapping should be removed.
   */
  public void remove(long key)
  {
    Entry p = getEntry(key);
    if (p == null) return;

    deleteEntry(p);
  }

  /**
   * Private function called by remove functions to delete the input entry p,
   * and then re-balance the tree.
   */
  private void deleteEntry(Entry p)
  {
    size--;

    // If strictly internal, copy successor's element to p and then make p
    // point to successor.

    if (p.left != null && p.right != null)
    {
      Entry s = (Entry) successor(p);
      p.key = s.key;
      p = s;
    } // p has 2 children
    deleteEntryLocal(p);
  }

  /**
   * Gets the entries key corresponding to the input key; if no such entry
   * exists, returns the entry for the least key greater than the specified key;
   * if no such entry exists (i.e., the greatest key in the Tree is less than
   * the specified key), returns <tt>Long.MIN_VALUE</tt>.
   * 
   * @param key
   *          The input key for which the ceiling key is found.
   */
  public long ceiling(long key)
  {
    // start at the root

    Entry p = (Entry) root;
    while (p != null)
    {
      // search left if the input key is less than the current entry key

      long cmp = key - p.key;
      if (cmp < 0)
      {
        if (p.left != null)
          p = (Entry) p.left;
        else
          return p.key;
      }
      else if (cmp > 0)
      {
        // otherwise search right and find the right-most key

        if (p.right != null)
          p = (Entry) p.right;
        else
          return ((Entry) findParentRight(p)).key;
      }
      else
        return p.key;
    }
    return Long.MIN_VALUE;
  }

  /**
   * Gets the entries key corresponding to the input key; if no such entry
   * exists, returns the entry for the greatest key less than the specified key;
   * if no such entry exists, returns <tt>Long.MIN_VALUE</tt>.
   * 
   * @param key
   *          The input key for which the floor key is found.
   */
  public long floor(long key)
  {
    // start at the root

    Entry p = (Entry) root;
    while (p != null)
    {
      // search right if the input key exceeds the current entry key

      long cmp = key - p.key;
      if (cmp > 0)
      {
        if (p.right != null)
          p = (Entry) p.right;
        else
          return p.key;
      }
      else if (cmp < 0)
      {
        // otherwise search left and find the left-most key

        if (p.left != null)
          p = (Entry) p.left;
        else
          return ((Entry) findParentLeft(p)).key;
      }
      else
        return p.key;

    }
    return Long.MIN_VALUE;
  }

  /**
   * Gets the entries key for the least key greater than the specified key; if
   * no such entry exists, returns the entry for the least key greater than the
   * specified key; if no such entry exists returns <tt>Long.MIN_VALUE</tt>.
   * 
   * @param key
   *          The input key for which the higher entry key is found.
   */
  public long higher(long key)
  {
    // start at the root

    Entry p = (Entry) root;
    while (p != null)
    {
      // search left if the input key is less than the current entry key

      long cmp = key - p.key;
      if (cmp < 0)
      {
        if (p.left != null)
          p = (Entry) p.left;
        else
          return p.key;
      }
      else
      {
        // otherwise search right and find the right-most key

        if (p.right != null)
          p = (Entry) p.right;
        else
        {
          p = (Entry) findParentRight(p);
          if (p != null) return p.key;
        }
      }
    }
    return Long.MIN_VALUE;
  }

  /**
   * Returns the entries key for the greatest key less than the specified key;
   * if no such entry exists (i.e., the least key in the Tree is greater than
   * the specified key), returns <tt>Long.MIN_VALUE</tt>.
   * 
   * @param key
   *          The input key for which the lower entry key is found.
   */
  public long lower(long key)
  {
    // start at the root

    Entry p = (Entry) root;
    while (p != null)
    {
      // search right if the input key exceeds the current key

      long cmp = key - p.key;
      if (cmp > 0)
      {
        if (p.right != null)
          p = (Entry) p.right;
        else
          return p.key;
      }
      else
      {
        // otherwise, search left and find the left most parent.

        if (p.left != null)
          p = (Entry) p.left;
        else
        {
          p = (Entry) findParentLeft(p);
          if (p != null) return p.key;
        }
      }
    }
    return Long.MIN_VALUE;
  }

  /**
   * Returns the first Entry in the TreeSet. Returns Long.MIN_VALUE if the
   * TreeSet is empty.
   */
  public long first()
  {
    Entry p = (Entry) getFirstEntry();
    if (p == null)
      return Long.MIN_VALUE;
    else
      return p.key;
  }

  /**
   * Returns the last Entry in the TreeSet. Returns Long.MIN_VALUE if the
   * TreeSet is empty.
   */
  public long last()
  {
    Entry p = (Entry) getLastEntry();
    if (p == null)
      return Long.MIN_VALUE;
    else
      return p.key;
  }

  /**
   * Returns and removes the first key from this map. If the map is empty
   * Long.MIN_VALUE is returned.
   * 
   * @return The first key in this map.
   */
  public long pollFirst()
  {
    Entry p = (Entry) getFirstEntry();
    if (p == null) return Long.MIN_VALUE;

    long rslt = p.key;
    deleteEntry(p);
    return rslt;
  }

  /**
   * Returns and removes the last key from this map. If the map is empty
   * Long.MIN_VALUE is returned.
   * 
   * @return The last key in this map.
   */
  public long pollLast()
  {
    Entry p = (Entry) getLastEntry();
    if (p == null) return Long.MIN_VALUE;

    long rslt = p.key;
    deleteEntry(p);
    return rslt;
  }

  /**
   * Returns an estimate of the bulk memory size used by this object. The
   * input pointer size (ptrsize) should be 8 for 64-bit and 4 for 32-bit.
   * 
   * @param ptrsize The pointer size set to 8 for 64-bit and 4 for 32-bit.
   * @return The bulk memory estimate in bytes.
   */
  @Override
  public long memoryEstimate(int ptrsize)
  {
    return super.memoryEstimate(ptrsize) + size * Long.SIZE / 8;
  }

  /**
   * Standard iterator used to parse this set. This entry iterator derives off
   * the TreeIntrinsic.PrivateEntryIterator and contains constructors and a
   * higher level remove function.
   * 
   * @author jrhipp
   * 
   */
  protected class ProtectedEntryIterator extends PrivateEntryIterator
  {
    /**
     * Default constructor.
     */
    public ProtectedEntryIterator()
    {
      // no code
    }

    /**
     * Standard constructor begins iteration at entry e.
     * 
     * @param e
     *          The entry at which iteration begins.
     */
    public ProtectedEntryIterator(Entry e)
    {
      super(e);
    }

    /**
     * Removes the current iterated entry.
     */
    public void remove()
    {
      deleteEntry((Entry) removeBase());
    }
  }

  /**
   * Standard iterator whose next function returns the next entries key.
   * 
   * @author jrhipp
   * 
   */
  public class Iterator extends ProtectedEntryIterator
  {
    /**
     * Returns the next entries key.
     * 
     * @return The next entries key.
     */
    public long next()
    {
      return ((Entry) nextEntry()).key;
    }
  }

  /**
   * Creates a new iterator.
   * 
   * @return A new iterator.
   */
  public Iterator iterator()
  {
    return new Iterator();
  }

  /**
   * Standard descending iterator whose next function returns the previous
   * entries key.
   * 
   * @author jrhipp
   * 
   */
  public class DecendingIterator extends ProtectedEntryIterator
  {
    /**
     * Standard constructor to setup the iterator at the last entry.
     */
    public DecendingIterator()
    {
      super((Entry) getLastEntry());
    }

    /**
     * Returns the previous entries key.
     * 
     * @return The previous entries key.
     */
    public long next()
    {
      return ((Entry) prevEntry()).key;
    }
  }

  /**
   * Creates a descending key iterator.
   * 
   * @return A new descending key iterator.
   */
  public DecendingIterator decendingIterator()
  {
    return new DecendingIterator();
  }
}
