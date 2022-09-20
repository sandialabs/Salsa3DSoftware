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
package gov.sandia.gmp.util.containers.multilevelmap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.md5.MD5Hash;

/**
 * Manager for the relationship between a list of Objects and an index into 
 * an array of values. This class provides a set of maps that relate a list of
 * objects, which act as keys, to an Integer index into the array of floats
 * attached to each node.
 *   
 * @author sballar
 *
 */
public class MultiLevelMap
{
  /**
   * Map of depth 1
   */
  protected HashMap<Object, Integer> map0;

  /**
   * Map of depth 2
   */
  protected HashMap<Object, HashMap<Object, Integer>> map1;

  /**
   * Map of depth 3
   */
  protected HashMap<Object, HashMap<Object, HashMap<Object, Integer>>> map2;

  /**
   * Map of depth 4
   */
  protected HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>> map3;

  /**
   * Map of depth 5
   */
  protected HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>>> map4;

  /**
   * The number of elements in the arrays of attribute values stored on each node of the model
   */
  private int size;

  /**
   * Default constructor
   */
  public MultiLevelMap()
  {
    //
  }
  
  /**
   * Add a single entry into the set of maps.
   * @param keys 1 to 5 objects that will act as keys into the 
   * map whose value is the Integer index into the array of 
   * attribute values stored on each node in the model.
   * The Integer index is incremented automatically.
   * @return the index of the set keys added to the map.
   * @throws IOException
   */
  public int addEntry(Object ... keys) throws IOException 
  {
    HashMap<Object, Integer> v1;
    HashMap<Object, HashMap<Object, Integer>> v2;
    HashMap<Object, HashMap<Object, HashMap<Object, Integer>>> v3;
    HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>> v4;

    switch (keys.length)
    {
    case 0 : 
      // do nothing. Not clear why an application would call this.
      return size-1;
    case 1 :
      if (map0 == null)
        map0 = new HashMap<Object, Integer>();
      if (map0.containsKey(keys[0]))
    	  return map0.get(keys[0]);
      map0.put(keys[0], size);
      return size++;

    case 2 :
      if (map1 == null)
        map1 = new HashMap<Object, HashMap<Object,Integer>>();

      v1 = map1.get(keys[0]);
      if (v1 == null)
      {
        v1 = new HashMap<Object, Integer>();
        map1.put(keys[0], v1);
      }
      if (v1.containsKey(keys[1]))
    	  return v1.get(keys[1]);
      v1.put(keys[1], size);
      return size++;

    case 3 :
      if (map2 == null)
        map2 = new HashMap<Object, HashMap<Object, HashMap<Object,Integer>>>();

      v2 = map2.get(keys[0]);
      if (v2 == null)
      {
        v2 = new HashMap<Object, HashMap<Object, Integer>>();
        map2.put(keys[0], v2);
      }

      v1 = v2.get(keys[1]);
      if (v1 == null)
      {
        v1 = new HashMap<Object, Integer>();
        v2.put(keys[1], v1);
      }
      if (v1.containsKey(keys[2]))
    	  return v1.get(keys[2]);
      v1.put(keys[2], size);
      return size++;
    case 4:
      if (map3 == null)
        map3 = new HashMap<Object, HashMap<Object,HashMap<Object,HashMap<Object,Integer>>>>();

      v3 = map3.get(keys[0]);
      if (v3 == null)
      {
        v3 = new HashMap<Object, HashMap<Object, HashMap<Object,Integer>>>();
        map3.put(keys[0], v3);
      }

      v2 = v3.get(keys[1]);
      if (v2 == null)
      {
        v2 = new HashMap<Object, HashMap<Object, Integer>>();
        v3.put(keys[1], v2);
      }

      v1 = v2.get(keys[2]);
      if (v1 == null)
      {
        v1 = new HashMap<Object, Integer>();
        v2.put(keys[2], v1);
      }
      if (v1.containsKey(keys[3]))
    	  return v1.get(keys[3]);
      v1.put(keys[3], size);
      return size++;
    case 5:
      if (map4 == null)
        map4 = new HashMap<Object, HashMap<Object,HashMap<Object,HashMap<Object,HashMap<Object,Integer>>>>>();

      v4 = map4.get(keys[0]);
      if (v4 == null)
      {
        v4 = new HashMap<Object, HashMap<Object,HashMap<Object,HashMap<Object,Integer>>>>();
        map4.put(keys[0], v4);
      }

      v3 = v4.get(keys[1]);
      if (v3 == null)
      {
        v3 = new HashMap<Object, HashMap<Object, HashMap<Object,Integer>>>();
        v4.put(keys[1], v3);
      }

      v2 = v3.get(keys[2]);
      if (v2 == null)
      {
        v2 = new HashMap<Object, HashMap<Object, Integer>>();
        v3.put(keys[2], v2);
      }

      v1 = v2.get(keys[3]);
      if (v1 == null)
      {
        v1 = new HashMap<Object, Integer>();
        v2.put(keys[3], v1);
      }

      if (v1.containsKey(keys[4]))
    	  return v1.get(keys[4]);
      v1.put(keys[4], size);
      return size++;
    default:
      throw new IOException("Cannot support more than 5 keys. keys.length="+keys.length);
    }
  }

  /**
   * Get the index mapped by the supplied set of keys.
   * @param keys
   * @return index, or -1 if supplied set of keys is not supported.
   */
  public int getIndex(Object ... keys)
  {
    try
    {
        switch (keys.length)
        {
        case 1 : return map0.get(keys[0]);
        case 2 : return map1.get(keys[0]).get(keys[1]);
        case 3 : return map2.get(keys[0]).get(keys[1]).get(keys[2]);
        case 4 : return map3.get(keys[0]).get(keys[1]).get(keys[2]).get(keys[3]);
        case 5 : return map4.get(keys[0]).get(keys[1]).get(keys[2]).get(keys[3]).get(keys[4]);
        default: return -1;
        }
    }
    catch (NullPointerException ex)
    {
      return -1;
    }
  }

  /**
   * 
   * @param keys
   * @return true if the supplied list of keys is supported, false if not supported.
   */
  public boolean isSupported(Object ... keys)
  {
    return getIndex(keys) >= 0;
  }

  /**
   * @return The size of the attribute array.
   */
  public int size()
  {
    return size;
  }

  /**
   * Returns the array of keys for a given index. If no match is found null is
   * returned.
   * 
   * @param index The index for which a match is sought.
   * 
   * @return The array of keys for a given index. If no match is found null is
   *         returned.
   */
  public Object[] getKeys(int index)
  {
    Object[] array = null;

    // check map0 for match

    if (map0 != null)
    {
      for (Map.Entry<Object, Integer> e0: map0.entrySet())
      {
        if (e0.getValue().intValue() == index)
        {
          array = new Object [1];
          array[0] = e0.getKey();
          return array;
        }
      }
    }

    // check map1 for match

    if (map1 != null)
    {
      for (Map.Entry<Object, HashMap<Object, Integer>> e1: map1.entrySet())
      {
        for (Map.Entry<Object, Integer> e0: e1.getValue().entrySet())
        {
          if (e0.getValue().intValue() == index)
          {
            array = new Object [2];
            array[0] = e1.getKey();
            array[1] = e0.getKey();
            return array;
          }
        }
      }
    }

    // check map2 for match

    if (map2 != null)
    {
      for (Map.Entry<Object, HashMap<Object, HashMap<Object, Integer>>> e2: map2.entrySet())
      {
        for (Map.Entry<Object, HashMap<Object, Integer>> e1: e2.getValue().entrySet())
        {
          for (Map.Entry<Object, Integer> e0: e1.getValue().entrySet())
          {
            if (e0.getValue().intValue() == index)
            {
              array = new Object [3];
              array[0] = e2.getKey();
              array[1] = e1.getKey();
              array[2] = e0.getKey();
              return array;
            }
          }
        }
      }
    }

    // check map3 for match

    if (map3 != null)
    {
      for (Map.Entry<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>> e3: map3.entrySet())
      {
        for (Map.Entry<Object, HashMap<Object, HashMap<Object, Integer>>> e2: e3.getValue().entrySet())
        {
          for (Map.Entry<Object, HashMap<Object, Integer>> e1: e2.getValue().entrySet())
          {
            for (Map.Entry<Object, Integer> e0: e1.getValue().entrySet())
            {
              if (e0.getValue().intValue() == index)
              {
                array = new Object [4];
                array[0] = e3.getKey();
                array[1] = e2.getKey();
                array[2] = e1.getKey();
                array[3] = e0.getKey();
                return array;
              }
            }
          }
        }
      }
    }

    // check map4 for match

    if (map4 != null)
    {
      for (Map.Entry<Object, HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>>> e4: map4.entrySet())
      {
        for (Map.Entry<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>> e3: e4.getValue().entrySet())
        {
          for (Map.Entry<Object, HashMap<Object, HashMap<Object, Integer>>> e2: e3.getValue().entrySet())
          {
            for (Map.Entry<Object, HashMap<Object, Integer>> e1: e2.getValue().entrySet())
            {
              for (Map.Entry<Object, Integer> e0: e1.getValue().entrySet())
              {
                if (e0.getValue().intValue() == index)
                {
                  array = new Object [5];
                  array[0] = e4.getKey();
                  array[1] = e3.getKey();
                  array[2] = e2.getKey();
                  array[3] = e1.getKey();
                  array[4] = e0.getKey();
                  return array;
                }
              }
            }
          }
        }
      }
    }

    // no match ... return null

    return null;
  }
  
  /**
   * Return an array of Strings, one for each index supported by this MultiLevelMap.
   * Each String is a concatenation of the classnames and toString() outputs for each
   * key in the map.
   * @return an array of Strings, one for each index supported by this MultiLevelMap.
   * Each String is a concatenation of the classnames and toString() outputs for each
   * key in the map.
   */
  public String[] getKeyStrings()
  {
	    String[] records = new String[size];

	    if (map0 != null)
	        for (Map.Entry<Object, Integer> e0 : map0.entrySet())
	          records[e0.getValue().intValue()] = String.format("%s %s", 
	              e0.getKey().getClass().getSimpleName(), e0.getKey().toString());
	    if (map1 != null)
	        for (Map.Entry<Object, HashMap<Object, Integer>> e1 : map1.entrySet())
	          for (Map.Entry<Object, Integer> e0 : e1.getValue().entrySet())
	            records[e0.getValue().intValue()] = String.format("%s %s -> %s %s", 
	                e1.getKey().getClass().getSimpleName(), e1.getKey().toString(),
	                e0.getKey().getClass().getSimpleName(), e0.getKey().toString());
	    if (map2 != null)
	      for (Map.Entry<Object, HashMap<Object, HashMap<Object, Integer>>> e2 : map2.entrySet())
	        for (Map.Entry<Object, HashMap<Object, Integer>> e1 : e2.getValue().entrySet())
	          for (Map.Entry<Object, Integer> e0 : e1.getValue().entrySet())
	            records[e0.getValue().intValue()] = String.format("%s %s -> %s %s -> %s %s", 
	                e2.getKey().getClass().getSimpleName(), e2.getKey().toString(),
	                e1.getKey().getClass().getSimpleName(), e1.getKey().toString(),
	                e0.getKey().getClass().getSimpleName(), e0.getKey().toString());
	    if (map3 != null)
	      for (Map.Entry<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>> e3 : map3.entrySet())
	        for (Map.Entry<Object, HashMap<Object, HashMap<Object, Integer>>> e2 : e3.getValue().entrySet())
	          for (Map.Entry<Object, HashMap<Object, Integer>> e1 : e2.getValue().entrySet())
	            for (Map.Entry<Object, Integer> e0 : e1.getValue().entrySet())
	              records[e0.getValue().intValue()] = String.format("%s %s -> %s %s -> %s %s -> %s %s", 
	                  e3.getKey().getClass().getSimpleName(), e3.getKey().toString(),
	                  e2.getKey().getClass().getSimpleName(), e2.getKey().toString(),
	                  e1.getKey().getClass().getSimpleName(), e1.getKey().toString(),
	                  e0.getKey().getClass().getSimpleName(), e0.getKey().toString());
	    if (map4 != null)
	      for (Map.Entry<Object, HashMap<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>>> e4 : map4.entrySet())
	        for (Map.Entry<Object, HashMap<Object, HashMap<Object, HashMap<Object, Integer>>>> e3 : e4.getValue().entrySet())
	          for (Map.Entry<Object, HashMap<Object, HashMap<Object, Integer>>> e2 : e3.getValue().entrySet())
	            for (Map.Entry<Object, HashMap<Object, Integer>> e1 : e2.getValue().entrySet())
	              for (Map.Entry<Object, Integer> e0 : e1.getValue().entrySet())
	                records[e0.getValue().intValue()] = String.format("%s %s -> %s %s -> %s %s -> %s %s -> %s %s", 
	                    e4.getKey().getClass().getSimpleName(), e4.getKey().toString(),
	                    e3.getKey().getClass().getSimpleName(), e3.getKey().toString(),
	                    e2.getKey().getClass().getSimpleName(), e2.getKey().toString(),
	                    e1.getKey().getClass().getSimpleName(), e1.getKey().toString(),
	                    e0.getKey().getClass().getSimpleName(), e0.getKey().toString());

	  return records;
  }

  /**
   * @return String representation of the all the key lists that support this object. 
   */
  @Override
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    for (String record : getKeyStrings())
      buf.append(record).append(Globals.NL);

    return buf.toString();
  }

  /**
   * Add to the supplied hash information that uniquely identifies the lists of
   * keys that support this MultiLevelMap
   * 
   * @param hash
   */
  public void update(MD5Hash hash, int[] modelVersion)
  {
    if (map0 != null)
    {
      // backward compatibility with version 6 where
      // attributes was a simple list of GeoAttributes
      StringBuffer buf = new StringBuffer();
      for (Object attribute : map0.keySet())
        buf.append(attribute.toString()).append(' ');
      hash.update(buf.toString().trim());
    }
    else
      hash.update(toString());
  }

}
