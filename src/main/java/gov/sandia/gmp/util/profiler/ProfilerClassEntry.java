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
import java.util.HashMap;
import java.util.Map;

/**
 * Used by a ProfilerContent object to maintain a map of all such
 * entries discovered during Profiler sampling operations. Each
 * class entry maintains a sample count of how many times this
 * class was sampled, a class name, and a map of all method
 * entries sampled.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class ProfilerClassEntry implements Serializable
{
  /**
   * The number of times this class is counted in a set of
   * samples produced by the profiler.
   */
  public int                          aCount = 0;

  /**
   * A flag used to mark this entry as having its count incremented
   * during sampling.
   */
  public boolean                      aIncr  = false;

  /**
   * The class name associated with this class entry.
   */
  public String                       aName  = "";

  /**
   * The map of all method entries associated with this class
   * entry.
   */
  public HashMap<String, ProfilerMethodEntry> aMethodMap = null;

  /**
   * Standard constructor. Creates a new ProfilerClassEntry with the
   * input name.
   * 
   * @param name Input class name.
   */
  public ProfilerClassEntry(String name)
  {
    aName = name;
    aMethodMap = new HashMap<String, ProfilerMethodEntry>(512);
  }

  /**
   * Standard constructor. Creates a new ProfilerClassEntry with
   * the input name and a capacity for the method entry map.
   * 
   * @param name Input class name.
   * @param cap Method map capacity.
   */
  public ProfilerClassEntry(String name, int cap)
  {
    aName = name;
    aMethodMap = new HashMap<String, ProfilerMethodEntry>(cap);
  }

  /**
   * Makes and returns a copy of this ProfilerClassEntry. Called by the
   * ProfilerContent object when adding one ProfierContent object to
   * another.
   * 
   * @return A deep copy of this object.
   */
  public ProfilerClassEntry copy()
  {
    // make the new class entry and set the count ... copy all method entries to
    // the method map

    ProfilerClassEntry pce = new ProfilerClassEntry(aName, 2 * aMethodMap.size());
    pce.aCount = aCount;
    for (Map.Entry<String, ProfilerMethodEntry> e: aMethodMap.entrySet())
    {
      pce.aMethodMap.put(e.getKey(), e.getValue().copy());
    }

    // return the new copy

    return pce;
  }
}
