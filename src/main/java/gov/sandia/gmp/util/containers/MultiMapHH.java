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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A multi-map implementation that uses a base container Hashmap to associate
 * an arbitrary key with a multiple values stored in a HashSet.
 *
 * <p> MultiMapHH is a multi-map that uses a base container Hashmap to
 * associate an arbitrary key with a HashSet of values ... i.e. more than one value
 * per key. It contains very basic functionality found in a standard Map.
 * 
 * @author Jim Hipp
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public class MultiMapHH<K, V>
{
  /**
   * The base container representing the multi-map.
   */
  private HashMap<K, HashSet<V>> m;

  /**
   * Standard constructor.
   */
  public MultiMapHH()
  {
    m = new HashMap<K, HashSet<V>>();
  }

  /**
   * Standard constructor.
   */
  public MultiMapHH(int sze)
  {
    m = new HashMap<K, HashSet<V>>(sze);
  }

  /**
   * Clears the map.
   */
  public void clear()
  {
    m.clear();
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
  public Set<Entry<K, HashSet<V>>> entrySet()
  {
    return m.entrySet();
  }

  /**
   * Returns the maps set (HashSet) of values associated with the input key.
   * 
   * @param key The key for which the associated set of values will be
   *        returned.
   * @return The set of values associated with the input key.
   */
  public HashSet<V> get(K key)
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
    HashSet<V> nhs = m.get(key);
    if (nhs == null) m.put(key, nhs = new HashSet<V>());

    nhs.add(value);
    
    return value;
  }

  /**
   * Removes all values associated with the input key.
   * 
   * @param key The key for which all associated values will be removed.
   * @return The set of all values associated with the input key.
   */
  public HashSet<V> remove(K key)
  {
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
    HashSet<V> nhs = m.get(key);
    if (nhs != null)
    {
      nhs.remove(value);
      if (nhs.isEmpty()) m.remove(key);
    }
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
}
