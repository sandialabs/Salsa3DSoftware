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
package gov.sandia.gmp.util.containers;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A multi-map implementation that uses a base container Treemap to associate
 * an arbitrary key with a multiple values stored in a TreeSet.
 *
 * <p> MultiMapTT is a multi-map that uses a base container Treemap to
 * associate an arbitrary key with a TreeSet of values ... i.e. more than one value
 * per key. It contains very basic functionality found in a standard Map.
 * 
 * @author Jim Hipp
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public class MultiMapTT<K, V>
{
  /**
   * The base container representing the multi-map.
   */
  private TreeMap<K, TreeSet<V>> m;

  /**
   * Total entry count.
   */
  private int entrySize = 0;

  /**
   * Standard constructor.
   */
  public MultiMapTT()
  {
    m = new TreeMap<K, TreeSet<V>>();
  }

  /**
   * Clears the map.
   */
  public void clear()
  {
    m.clear();
    entrySize = 0;
  }
  
  /**
   * Returns true if the input key is found in the map.
   * 
   * @param key The key to be found in the map.
   * @return True if the input key is found in the map.
   */
  public boolean containsKey(K key)
  {
    return m.containsKey(key);
  }
  
  /**
   * Returns the maps Entry Set for purposes of iteration.
   * 
   * @return The maps entry Set.
   */
  public  Set<Entry<K,TreeSet<V>>> entrySet()
  {
    return m.entrySet();
  }

  /**
   * Returns the maps set (TreeSet) of values associated with the input key.
   * 
   * @param key The key for which the associated set of values will be
   *        returned.
   * @return The set of values associated with the input key.
   */
  public TreeSet<V> get(K key)
  {
    return m.get(key);
  }

  /**
   * Returns true if the map is empty.
   * 
   * @return True if the map is empty.
   */
  public boolean isEmpty()
  {
    return m.isEmpty();
  }

  /**
   * Returns the maps key Set.
   * 
   * @return The maps key Set.
   */
  public Set<K> keySet()
  {
    return m.keySet();
  }

  /**
   * Adds a new key / value pair to the map. If the key has already
   * been added to the map then the value is added to the set associated
   * with the key. Otherwise a new key / value set association pair is
   * add to the map and the value is added to the value set.
   * 
   * @param key The key of the new key / value pair.
   * @param value the value of the new key / value pair.
   * @return The value added to the map.
   */
  public V put(K key, V value)
  {
    TreeSet<V> nhs = m.get(key);
    if (nhs == null)
    {
      nhs = new TreeSet<V>();
      m.put(key, nhs);
    }
    
    nhs.add(value);
    ++entrySize;

    return value;
  }

  /**
   * Removes all values associated with the input key.
   * 
   * @param key The key for which all associated values will be removed.
   * @return The set of all values associated with the input key.
   */
  public TreeSet<V> remove(K key)
  {
    TreeSet<V> nhs = m.get(key);
    if (nhs != null) entrySize -= nhs.size();
    
    return m.remove(key);
  }

  /**
   * Removes the input value associated with the input key from the map.
   * 
   * @param key The key associated with the input value to be removed.
   * @param value The value to be removed from the map.
   */
  public void remove(K key, V value)
  {
   TreeSet<V> nhs = m.get(key);
   if (nhs != null)
   {
     nhs.remove(value);
     if (nhs.isEmpty()) m.remove(key);
     --entrySize;
   }
  }

  /**
   * Removes and returns the first value in the multimap. I no values remain
   * null is returned.
   * 
   * @return Removes and returns the first value in the multimap.
   */
  public V pollFirstEntry()
  {
    if (m.size() == 0) return null;
    Entry<K, TreeSet<V>> e = m.firstEntry();
    TreeSet<V> tset = e.getValue();
    V val = tset.pollFirst();
    if (tset.size() == 0) m.pollFirstEntry();
    --entrySize;
    return val;
  }

  /**
   * Removes and returns the last value in the multimap. I no values remain
   * null is returned.
   * 
   * @return Removes and returns the last value in the multimap.
   */
  public V pollLastValue()
  {
    if (m.size() == 0) return null;
    Entry<K, TreeSet<V>> e = m.lastEntry();
    TreeSet<V> tset = e.getValue();
    V val = tset.pollFirst();
    if (tset.size() == 0) m.pollLastEntry();
    --entrySize;
    return val;
  }

  public K lastKey()
  {
    return m.lastKey();
  }

  public K firstKey()
  {
    return m.firstKey();
  }

  /**
   * Returns the size of the map (number of unique key entries).
   * 
   * @return The size of the map.
   */
  public int size()
  {
    return m.size();
  }

  public int entryCount()
  {
    return entrySize;
  }
}
