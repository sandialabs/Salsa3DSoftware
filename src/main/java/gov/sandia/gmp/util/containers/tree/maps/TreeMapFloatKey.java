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
package gov.sandia.gmp.util.containers.tree.maps;

import gov.sandia.gmp.util.containers.tree.TreeIntrinsic;

/**
 * A Red-Black tree based intrinsic map implementation that maps an intrinsic
 * Float to an associated arbitrary object value. This map is sorted on the
 * intrinsic key from smallest to largest in the tree.
 * 
 * <p>
 * This implementation provides guaranteed log(n) time cost for the
 * <tt>containsKey</tt>, <tt>get</tt>, <tt>put</tt> and <tt>remove</tt>
 * operations. Algorithms are adaptations of those in Cormen, Leiserson, and
 * Rivest's <I>Introduction to Algorithms</I>.
 * 
 * <p>
 * <strong> Note that this implementation is not synchronized.</strong> If
 * multiple threads access a map concurrently, and at least one of the threads
 * modifies the map structurally, it <i>must</i> be synchronized externally. (A
 * structural modification is any operation that adds or deletes one or more
 * mappings; merely changing the value associated with an existing key is not a
 * structural modification.) This is typically accomplished by synchronizing on
 * some object that naturally encapsulates the map.
 * 
 * @author jrhipp
 * 
 */
@SuppressWarnings("serial")
public class TreeMapFloatKey<V> extends TreeIntrinsic
{
  /**
   * The entry pair for this map containing the intrinsic Float key and its
   * associated object value. This object extends the base class
   * TreeIntrinsic.Entry which contains the left and right binary tree
   * references, as-well-as a reference to the parent element. This class
   * provides mechanisms to retrieve the key and the value, a function to set
   * the value and a means to test for object equality.
   * 
   * @author jrhipp
   * 
   */
  public final static class Entry<V> extends TreeIntrinsic.Entry
  {
    /**
     * The key for this entry.
     */
    private float key;

    /**
     * The value for this entry.
     */
    private V    value;

    /**
     * Standard constructor that sets the key, value, and the parent reference.
     * 
     * @param k
     *          The key.
     * @param v
     *          The value.
     * @param parent
     *          The parent reference.
     */
    Entry(float k, V v, Entry<V> parent)
    {
      super(parent);

      key = k;
      value = v;
    }

    /**
     * Returns the key.
     * 
     * @return The key.
     */
    public float getKey()
    {
      return key;
    }

    /**
     * Returns the value.
     * 
     * @return The value.
     */
    public V getValue()
    {
      return value;
    }

    /**
     * Replaces the value currently associated with the key with the given
     * value.
     * 
     * @return The value associated with the key before this method was called
     */
    protected V setValue(V value)
    {
      V oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    /**
     * Returns true if the input object has the same key and value as this
     * object.
     * 
     * @param o
     *          The input object.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o)
    {
      if (!(o instanceof Entry)) return false;
      Entry<V> e = (Entry<V>) o;
      return ((key == e.key) && (value.equals(e.value)));
    }

    /**
     * Returns the pair as a string "key = value".
     */
    @Override
    public String toString()
    {
      return key + " = " + value;
    }
  }

  /**
   * Returns <tt>true</tt> if this map contains a mapping for the specified key.
   * 
   * @param key
   *          The input key whose presence in this map is to be tested.
   * @return <tt>true</tt> if this map contains a mapping for the specified key.
   */
  public boolean containsKey(float key)
  {
    return getEntry(key) != null;
  }

  /**
   * Returns <tt>true</tt> if this map maps one or more keys to the specified
   * value. More formally, returns <tt>true</tt> if and only if this map
   * contains at least one mapping to a value <tt>v</tt> such that
   * <tt>(value == v)</tt>. This operation will require time linear in the map
   * size for most implementations.
   * 
   * @param value
   *          value whose presence in this map is to be tested.
   * @return <tt>true</tt> if a mapping to <tt>value</tt> exists; <tt>false</tt>
   *         otherwise.
   */
  @SuppressWarnings("unchecked")
  public boolean containsValue(V value)
  {
    for (Entry<V> e = (Entry<V>) getFirstEntry(); e != null; e = (Entry<V>) successor(e))
      if (value == e.value) return true;

    return false;
  }

  /**
   * Returns the value to which the specified key is mapped, or {@code null} if
   * this map contains no mapping for the key.
   * 
   * <p>
   * More formally, if this map contains a mapping from a key {@code k} to a
   * value {@code v} such that {@code key} compares equal to {@code k} according
   * to the map's ordering, then this method returns {@code v}; otherwise it
   * returns {@code null}. (There can be at most one such mapping.)
   * 
   * <p>
   * A return value of {@code null} does not <i>necessarily</i> indicate that
   * the map contains no mapping for the key; it's also possible that the map
   * explicitly maps the key to {@code null}. The {@link #containsKey
   * containsKey} operation may be used to distinguish these two cases.
   * 
   */
  public V get(float key)
  {
    Entry<V> p = getEntry(key);
    return (p == null ? null : p.value);
  }

  /**
   * Returns this map's entry for the given key, or <tt>null</tt> if the map
   * does not contain an entry for the key.
   * 
   * @return This map's entry for the given key, or <tt>null</tt> if the map
   *         does not contain an entry for the key
   */
  @SuppressWarnings("unchecked")
  public final Entry<V> getEntry(float key)
  {
    // start at the root and traverse the tree to find the containing entry

    Entry<V> p = (Entry<V>) root;
    while (p != null)
    {
      float cmp = key - p.key;
      if (cmp < 0)
        p = (Entry<V>) p.left;
      else if (cmp > 0)
        p = (Entry<V>) p.right;
      else
        return p;
    }

    // entry not found ... return null

    return null;
  }

  /**
   * Associates the specified value with the specified key in this map. If the
   * map previously contained a mapping for the key, the old value is replaced.
   * 
   * @param key
   *          The key with which the specified value is to be associated.
   * @param value
   *          The value to be associated with the specified key.
   * 
   * @return The previous value associated with <tt>key</tt>, or <tt>null</tt>
   *         if there was no mapping for <tt>key</tt>. (A <tt>null</tt> return
   *         can also indicate that the map previously associated <tt>null</tt>
   *         with <tt>key</tt>.)
   */
  @SuppressWarnings("unchecked")
  public V put(float key, V value)
  {
    // if no entries exist add this association as the first entry.

    if (root == null)
    {
      root = new Entry<V>(key, value, null);
      size = 1;
      return null;
    }

    // start at the root and find an existing key. If found set this new
    // value associated with the key

    float cmp;
    Entry<V> parent;
    Entry<V> t = (Entry<V>) root;
    do
    {
      parent = t;
      cmp = key - t.key;
      if (cmp < 0)
        t = (Entry<V>) t.left;
      else if (cmp > 0)
        t = (Entry<V>) t.right;
      else
        return t.setValue(value);
    } while (t != null);

    // no existing key found ... insert new key/value pair on the left or
    // right parent node.

    Entry<V> e = new Entry<V>(key, value, parent);
    if (cmp < 0)
      parent.left = e;
    else
      parent.right = e;

    // re-balance the tree, increment the count, and return the null

    fixAfterInsertion(e);
    size++;
    return null;
  }

  /**
   * Removes the mapping for this key from this TreeMap if present.
   * 
   * @param key
   *          The key for which mapping should be removed.
   * @return the previous value associated with <tt>key</tt>, or <tt>null</tt>
   *         if there was no mapping for <tt>key</tt>. (A <tt>null</tt> return
   *         can also indicate that the map previously associated <tt>null</tt>
   *         with <tt>key</tt>.)
   */
  public V remove(float key)
  {
    Entry<V> p = getEntry(key);
    if (p == null) return null;

    V oldValue = p.value;
    deleteEntry(p);
    return oldValue;
  }

  /**
   * Private function called by remove functions to delete the input pair p, and
   * then re-balance the tree.
   */
  @SuppressWarnings("unchecked")
  private void deleteEntry(Entry<V> p)
  {
    size--;

    // If strictly internal, copy successor's element to p and then make p
    // point to successor.

    if (p.left != null && p.right != null)
    {
      Entry<V> s = (Entry<V>) successor(p);
      p.key = s.key;
      p.value = s.value;
      p = s;
    } // p has 2 children
    deleteEntryLocal(p);
  }

  /**
   * Gets the entry corresponding to the specified key; if no such entry exists,
   * returns the entry for the least key greater than the specified key; if no
   * such entry exists (i.e., the greatest key in the Tree is less than the
   * specified key), returns <tt>null</tt>.
   * 
   * @param key
   *          The input key for which the ceiling is found.
   */
  @SuppressWarnings("unchecked")
  public final Entry<V> ceilingEntry(float key)
  {
    // start at the root

    Entry<V> p = (Entry<V>) root;
    while (p != null)
    {
      // search left if the input key is less than the current entry key

      float cmp = key - p.key;
      if (cmp < 0)
      {
        if (p.left != null)
          p = (Entry<V>) p.left;
        else
          return p;
      }
      else if (cmp > 0)
      {
        // otherwise search right and find the right-most key

        if (p.right != null)
          p = (Entry<V>) p.right;
        else
          return (Entry<V>) findParentRight(p);
      }
      else
        return p;
    }
    return null;
  }

  /**
   * Gets the entries key corresponding to the input key; if no such entry
   * exists, returns the entry for the least key greater than the specified key;
   * if no such entry exists (i.e., the greatest key in the Tree is less than
   * the specified key), returns <tt>Float.MIN_VALUE</tt>.
   * 
   * @param key
   *          The input key for which the ceiling key is found.
   */
  public float ceilingKey(float key)
  {
    Entry<V> p = ceilingEntry(key);
    if (p == null)
      return Float.MIN_VALUE;
    else
      return p.key;
  }

  /**
   * Gets the entry corresponding to the specified key; if no such entry exists,
   * returns the entry for the greatest key less than the specified key; if no
   * such entry exists, returns <tt>null</tt>.
   * 
   * @param key
   *          The input key for which the floor is found.
   */
  @SuppressWarnings("unchecked")
  public final Entry<V> floorEntry(float key)
  {
    // start at the root

    Entry<V> p = (Entry<V>) root;
    while (p != null)
    {
      // search right if the input key exceeds the current entry key

      float cmp = key - p.key;
      if (cmp > 0)
      {
        if (p.right != null)
          p = (Entry<V>) p.right;
        else
          return p;
      }
      else if (cmp < 0)
      {
        // otherwise search left and find the left-most key

        if (p.left != null)
          p = (Entry<V>) p.left;
        else
          return (Entry<V>) findParentLeft(p);
      }
      else
        return p;

    }
    return null;
  }

  /**
   * Gets the entries key corresponding to the input key; if no such entry
   * exists, returns the entry for the greatest key less than the specified key;
   * if no such entry exists, returns <tt>Float.MIN_VALUE</tt>.
   * 
   * @param key
   *          The input key for which the floor key is found.
   */
  public float floorKey(float key)
  {
    Entry<V> p = floorEntry(key);
    if (p == null)
      return Float.MIN_VALUE;
    else
      return p.key;
  }

  /**
   * Gets the entry for the least key greater than the specified key; if no such
   * entry exists, returns the entry for the least key greater than the
   * specified key; if no such entry exists returns <tt>null</tt>.
   * 
   * @param key
   *          The input key for which the higher entry is found.
   */
  @SuppressWarnings("unchecked")
  public final Entry<V> higherEntry(float key)
  {
    // start at the root

    Entry<V> p = (Entry<V>) root;
    while (p != null)
    {
      // search left if the input key is less than the current entry key

      float cmp = key - p.key;
      if (cmp < 0)
      {
        if (p.left != null)
          p = (Entry<V>) p.left;
        else
          return p;
      }
      else
      {
        // otherwise search right and find the right-most key

        if (p.right != null)
        {
          p = (Entry<V>) p.right;
        }
        else
        {
          return (Entry<V>) findParentRight(p);
        }
      }
    }
    return null;
  }

  /**
   * Gets the entries key for the least key greater than the specified key; if
   * no such entry exists, returns the entry for the least key greater than the
   * specified key; if no such entry exists returns <tt>Float.MIN_VALUE</tt>.
   * 
   * @param key
   *          The input key for which the higher entry key is found.
   */
  public float higherKey(float key)
  {
    Entry<V> p = higherEntry(key);
    if (p == null)
      return Float.MIN_VALUE;
    else
      return p.key;
  }

  /**
   * Returns the entry for the greatest key less than the specified key; if no
   * such entry exists (i.e., the least key in the Tree is greater than the
   * specified key), returns <tt>null</tt>.
   * 
   * @param key
   *          The input key for which the lower entry key is found.
   */
  @SuppressWarnings("unchecked")
  public final Entry<V> lowerEntry(float key)
  {
    // start at the root

    Entry<V> p = (Entry<V>) root;
    while (p != null)
    {
      // search right if the input key exceeds the current key

      float cmp = key - p.key;
      if (cmp > 0)
      {
        if (p.right != null)
          p = (Entry<V>) p.right;
        else
          return p;
      }
      else
      {
        // otherwise, search left and find the left most parent.

        if (p.left != null)
          p = (Entry<V>) p.left;
        else
          return (Entry<V>) findParentLeft(p);
      }
    }
    return null;
  }

  /**
   * Returns the entries key for the greatest key less than the specified key;
   * if no such entry exists (i.e., the least key in the Tree is greater than
   * the specified key), returns <tt>Float.MIN_VALUE</tt>.
   * 
   * @param key
   *          The input key for which the lower entry key is found.
   */
  public float lowerKey(float key)
  {
    Entry<V> p = lowerEntry(key);
    if (p == null)
      return Float.MIN_VALUE;
    else
      return p.key;
  }

  /**
   * Returns the first Entry in the TreeMap. Returns null if the TreeMap is
   * empty.
   */
  @SuppressWarnings("unchecked")
  public final Entry<V> firstEntry()
  {
    return (Entry<V>) getFirstEntry();
  }

  /**
   * Returns the first Entry in the TreeMap. Returns Float.MIN_VALUE if the
   * TreeMap is empty.
   */
  @SuppressWarnings("unchecked")
  public final float firstKey()
  {
    Entry<V> p = (Entry<V>) getFirstEntry();
    if (p == null)
      return Float.MIN_VALUE;
    else
      return p.key;
  }

  /**
   * Returns the last Entry in the TreeMap. Returns null if the TreeMap is
   * empty.
   */
  @SuppressWarnings("unchecked")
  public final Entry<V> lastEntry()
  {
    return (Entry<V>) getLastEntry();
  }

  /**
   * Returns the last Entry in the TreeMap. Returns Float.MIN_VALUE if the
   * TreeMap is empty.
   */
  @SuppressWarnings("unchecked")
  public final float lastKey()
  {
    Entry<V> p = (Entry<V>) getLastEntry();
    if (p == null)
      return Float.MIN_VALUE;
    else
      return p.key;
  }

  /**
   * Returns and removes the first entry from this map. If the map is empty null
   * is returned.
   * 
   * @return The first entry in this map.
   */
  @SuppressWarnings("unchecked")
  public Entry<V> pollFirstEntry()
  {
    Entry<V> p = (Entry<V>) getFirstEntry();
    if (p != null) deleteEntry(p);
    return p;
  }

  /**
   * Returns and removes the last entry from this map. If the map is empty null
   * is returned.
   * 
   * @return The last entry in this map.
   */
  @SuppressWarnings("unchecked")
  public Entry<V> pollLastEntry()
  {
    Entry<V> p = (Entry<V>) getLastEntry();
    if (p != null) deleteEntry(p);
    return p;
  }

  /**
   * Returns an estimate of the bulk memory size used by this object. The
   * input pointer size (ptrsize) should be 8 for 64-bit and 4 for 32-bit.
   * Note that the size of V is not included in the estimate. This would
   * have to be added, times the size of this map, to the total.
   * 
   * @param ptrsize The pointer size set to 8 for 64-bit and 4 for 32-bit.
   * @return The bulk memory estimate in bytes.
   */
  @Override
  public long memoryEstimate(int ptrsize)
  {
    return super.memoryEstimate(ptrsize) +
                  size * (ptrsize + Float.SIZE / 8);
  }

  /**
   * Standard iterator used to parse this map. This entry iterator derives off
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
    public ProtectedEntryIterator(Entry<V> e)
    {
      super(e);
    }

    /**
     * Removes the current iterated entry.
     */
    @SuppressWarnings("unchecked")
    public void remove()
    {
      deleteEntry((Entry<V>) removeBase());
    }
  }

  /**
   * Standard value iterator whose next function returns the next entries value.
   * 
   * @author jrhipp
   * 
   */
  public abstract class ValueIterator extends ProtectedEntryIterator
  {
    /**
     * Standard constructor to setup the iterator at the last entry.
     */
    public ValueIterator()
    {
      super();
    }

    /**
     * Standard constructor to setup the iterator at the last entry.
     */
    public ValueIterator(Entry<V> e)
    {
      super(e);
    }

    /**
     * Returns the next entries value.
     * 
     * @return The next entries value.
     */
    public abstract V next();
  }

  /**
   * Standard value iterator whose next function returns the next entries value.
   * 
   * @author jrhipp
   * 
   */
  public class AscendingValueIterator extends ValueIterator
  {
    /**
     * Returns the next entries value.
     * 
     * @return The next entries value.
     */
    @SuppressWarnings("unchecked")
    @Override
    public V next()
    {
      return ((Entry<V>) nextEntry()).value;
    }
  }

  /**
   * Creates a new value iterator.
   * 
   * @return A new value iterator.
   */
  public ValueIterator ascendingValueIterator()
  {
    return new AscendingValueIterator();
  }

  /**
   * Standard value iterator whose next function returns the next entries value.
   * 
   * @author jrhipp
   * 
   */
  public class DecendingValueIterator extends ValueIterator
  {
    /**
     * Standard constructor to setup the iterator at the last entry.
     */
    @SuppressWarnings("unchecked")
    public DecendingValueIterator()
    {
      super((Entry<V>) getLastEntry());
    }

    /**
     * Returns the next entries value.
     * 
     * @return The next entries value.
     */
    @SuppressWarnings("unchecked")
    @Override
    public V next()
    {
      return ((Entry<V>) prevEntry()).value;
    }
  }

  /**
   * Creates a new value iterator.
   * 
   * @return A new value iterator.
   */
  public ValueIterator decendingValueIterator()
  {
    return new DecendingValueIterator();
  }

  /**
   * Standard key iterator whose next function returns the next entries key.
   * 
   * @author jrhipp
   * 
   */
  public class KeyIterator extends ProtectedEntryIterator
  {
    /**
     * Returns the next entries key.
     * 
     * @return The next entries key.
     */
    @SuppressWarnings("unchecked")
    public float next()
    {
      return ((Entry<V>) nextEntry()).key;
    }
  }

  /**
   * Creates a new key iterator.
   * 
   * @return A new key iterator.
   */
  public KeyIterator iterator()
  {
    return new KeyIterator();
  }

  /**
   * Standard descending key iterator whose next function returns the previous
   * entries key.
   * 
   * @author jrhipp
   * 
   */
  public class DecendingKeyIterator extends ProtectedEntryIterator
  {
    /**
     * Standard constructor to setup the iterator at the last entry.
     */
    @SuppressWarnings("unchecked")
    public DecendingKeyIterator()
    {
      super((Entry<V>) getLastEntry());
    }

    /**
     * Returns the previous entries key.
     * 
     * @return The previous entries key.
     */
    @SuppressWarnings("unchecked")
    public float next()
    {
      return ((Entry<V>) prevEntry()).key;
    }
  }

  /**
   * Creates a descending key iterator.
   * 
   * @return A new descending key iterator.
   */
  public DecendingKeyIterator decendingKeyIterator()
  {
    return new DecendingKeyIterator();
  }

  /**
   * Standard entry iterator whose next function returns the next entry.
   * 
   * @author jrhipp
   * 
   */
  public class EntryIterator extends ProtectedEntryIterator
  {
    /**
     * Returns the next entry.
     * 
     * @return The next entry.
     */
    @SuppressWarnings("unchecked")
    public Entry<V> next()
    {
      return (Entry<V>) nextEntry();
    }
  }

  /**
   * Creates a new entry iterator.
   * 
   * @return A new entry iterator.
   */
  public EntryIterator entryIterator()
  {
    return new EntryIterator();
  }
}
