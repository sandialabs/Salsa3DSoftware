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
package gov.sandia.gmp.util.profiler;

import java.io.Serializable;

import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerInteger;

/**
 * Used by a ProfilerContent object to maintain a map of all such
 * entries discovered during Profiler sampling operations. Each
 * method entry maintains a sample count of how many times this
 * method was sampled, a method name, and a map of all line number
 * entries sampled. The ProfilerMethodEntry objects are added to
 * the method maps of ProfilerClassEntry objects during profiling
 * operations.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class ProfilerMethodEntry implements Serializable
{
  /**
   * The number of times this method is counted in a set of
   * samples produced by the profiler.
   */
  public int                          aCount   = 0;

  /**
   * A flag used to mark this entry as having its count incremented
   * during sampling.
   */
  public boolean                      aIncr    = false;

  /**
   * The class name associated with this method entry.
   */
  public String                       aName    = "";

  /**
   * The map of all line number entries associated with this method
   * entry.
   */
  public HashMapIntegerInteger        aLineMap = null;

  /**
   * Standard constructor. Creates a new ProfilerMethodEntry with the
   * input name.
   * 
   * @param name Input method name.
   */
  public ProfilerMethodEntry(String name)
  {
    aName = name;
    aLineMap = new HashMapIntegerInteger(128);
  }

  /**
   * Standard constructor. Creates a new ProfilerMethodEntry with
   * the input name and a capacity for the line number entry map.
   * 
   * @param name Input class name.
   * @param cap Method map capacity.
   */
  public ProfilerMethodEntry(String name, int cap)
  {
    aName = name;
    aLineMap = new HashMapIntegerInteger(cap);
  }

  /**
   * Makes and returns a copy of this ProfilerMethodEntry. Called by the
   * ProfilerContent object when adding one ProfierContent object to
   * another.
   * 
   * @return A deep copy of this object.
   */
  public ProfilerMethodEntry copy()
  {
    // make the new method entry and set the count ... copy all line number
    // entries to the line number map

    ProfilerMethodEntry pme = new ProfilerMethodEntry(aName, 2 * aLineMap.size());
    pme.aCount = aCount;
    HashMapIntegerInteger.Iterator it = aLineMap.iterator();
    while (it.hasNext())
    {
      HashMapIntegerInteger.Entry e = it.nextEntry();
      pme.aLineMap.put(e.getKey(), e.getValue());
    }

    // return the new copy

    return pme;
  }
}
