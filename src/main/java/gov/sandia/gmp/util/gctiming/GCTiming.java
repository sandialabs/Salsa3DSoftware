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
package gov.sandia.gmp.util.gctiming;

import java.io.IOException;
import java.io.Serializable;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;

/**
* Garbage Collector timing information. Returns the number of collections and
* the total collection time (msec) from each type of garbage collector used 
 * in the application that defines this GCTiming class. The update() function
* should be called before accessing any information. 
 * 
 * @author jrhipp
*
*/
@SuppressWarnings("serial")
public final class GCTiming implements Serializable
{
  /**
   * An inner class to hold the count and time for each type of garbage
   * collector encountered in an application. This class is not accessible
   * outside of GCTiming.
   * 
   * @author jrhipp
   *
   */
  
  private final class d implements Serializable
  {
    /**
     * Collection count.
     */
    public long   count;
    
    /**
     * Collection time (milliseconds).
     */
    public long   tim;

    /**
     * Constructor that sets the count and time for this collector.
     * 
     * @param c Count.
     * @param t Time (milliseconds).
     */
    public d(long c, long t)
    {
      count = c;
      tim   = t;
    }
  }

  /**
   * The internal map associating garbage collector names with their call count
   * and elapsed time information.
   */
  private HashMap<String, d> aGCTiming = null;

  /**
   * Time at instantiation.
   */
  private long               aStrtTime = -1;

  /**
   * Time at last call to update.
   */
  private long               aLastTime = -1;

  /**
   * Default constructor.
   */
  public GCTiming()
  {
    aGCTiming = new HashMap<String, d>();
    aStrtTime = (new Date()).getTime();
    aLastTime = aStrtTime;
  }

  /**
   * Standard copy constructor called used to create a synchronized safe copy
   * of the input GCTiming for serialization and transport across the network.
   * Note: The update function should be called before performing the
   *       construction to obtain an identical copy.
   * 
   * @param gct The input (and actively updated) GCTiming object that
   *            will be copied into this one. 
   */
  public GCTiming(GCTiming gct)
  {
    // copy intrinsics

    synchronized (gct)
    {
      aStrtTime          = gct.aStrtTime;
      aLastTime          = gct.aLastTime;
    }

    // create timing hash map and add input GCTiming to this one

    aGCTiming = new HashMap<String, d>();
    add(gct);
  }

  /**
   * Returns the start time of this GC Timing object.
   * 
   * @return The start time of this GC Timing object.
   */
  public long getStartTime()
  {
    return aStrtTime;
  }

  /**
   * Returns the last update time of this GC Timing object.
   * 
   * @return The last update time of this GC Timing object.
   */
  public long getLastUpdateTime()
  {
    return aLastTime;
  }

  /**
   * Returns the elapsed time since the instantiation of this GCTiming object
   * and its last call to update.
   * 
   * @return The elapsed time since the instantiation of this GCTiming object
   *         and its last call to update.
   */
  public String getElapsedTime()
  {
    return Globals.elapsedTimeString(aStrtTime, aLastTime);
  }

  /**
   * Writes this GCTiming to the input file output buffer.
   * 
   * @param fob The file output buffer into which this GCTiming is written.
   * @throws IOException
   */
  public void write(FileOutputBuffer fob) throws IOException
  {
    // write start and last time and size of map

    fob.writeLong(aStrtTime);
    fob.writeLong(aLastTime);
    fob.writeInt(aGCTiming.size());
    
    // if any entries loop over gc map and output name, count and time.

    if (aGCTiming.size() > 0)
    {
      for (Map.Entry<String, d> e: aGCTiming.entrySet())
      {
        fob.writeString(e.getKey());
        fob.writeLong(e.getValue().count);
        fob.writeLong(e.getValue().tim);
      }
    }
  }

  /**
   * Reads this GCTiming from the input file input buffer.
   * 
   * @param fib The file input buffer from which this GCTiming is initialized.
   * @throws IOException
   */
  public void read(FileInputBuffer fib) throws IOException
  {
    // initialize start and last times and size of GC map

    aStrtTime          = fib.readLong();
    aLastTime          = fib.readLong();
    int n = fib.readInt();
    aGCTiming = new HashMap<String, d>();
    
    // if any map entries populate map

    for (int i = 0; i < n; ++i)
    {
      String name = fib.readString();
      long count  = fib.readLong();
      long tim    = fib.readLong();
      d newd      = new d(count, tim);
      aGCTiming.put(name,  newd);
    }
  }

  /**
   * Removes all entries from the map.
   */
  public void clear()
  {
    aGCTiming.clear();
  }

  /**
   * Adds the input GCTiming results to this one. This is used simply to 
   * agglomerate several different host results into a single result.
   *  
   * @param a The GCTiming whose results will be added to this one.
   */
  public synchronized void add(GCTiming a)
  {
    // added synchronized statement for concurrent mode when a.aGCTiming is
    // still being modified by task threads at the same time that the client is
    // updating the cummulative host state

    synchronized (a.aGCTiming)
    {
      for (Map.Entry<String, d> e: a.aGCTiming.entrySet())
      {
        // see if this GCTiming has an entry matching e.getKey()
  
        d thisd = aGCTiming.get(e.getKey());
        if (thisd == null)
        {
          // no entry ... add one
  
          thisd = new d(0,0);
          aGCTiming.put(e.getKey(), thisd);
        }
        
        // update this entry with the values in e
  
        thisd.count += e.getValue().count;
        thisd.tim   += e.getValue().tim;
      }
    }
  }

  /**
   * Removes the input GCTiming results from this one. This is used simply to 
   * agglomerate several different host results into a single result.
   *  
   * @param a The GCTiming whose results will be removed from this one.
   */
  public synchronized void remove(GCTiming a)
  {
    // loop over each result in the input GCTiming

    synchronized (a.aGCTiming)
    {
      for (Map.Entry<String, d> e: a.aGCTiming.entrySet())
      {
        // see if this GCTiming has an entry matching e.getKey()
  
        d thisd = aGCTiming.get(e.getKey());
        if (thisd != null)
        {
          // matching entry ... decrement count and time
  
          thisd.count -= e.getValue().count;
          thisd.tim   -= e.getValue().tim;
          
          // if count is empty remove it from the list
  
          if (thisd.count == 0)
            aGCTiming.remove(e.getKey());
        }
      }
    }
  }

  /**
   * Updates the garbage collection information. This function should be called
   * before accessing any getters to ensure that the information is up to date.
   */
  public synchronized void update()
  {
    d total = new d(0, 0);

    // get the new list and update the access time

    List<GarbageCollectorMXBean> mmList = ManagementFactory.getGarbageCollectorMXBeans();
    aLastTime = (new Date()).getTime();
    
    // clear the current map and loop over each entry in the list and add
    // the entry to the map
    
    synchronized (aGCTiming)
    {
       aGCTiming.clear();
       for (GarbageCollectorMXBean mm: mmList)
       {
             long c = mm.getCollectionCount();
             long t = mm.getCollectionTime();
             total.count += c;
             total.tim   += t;
             aGCTiming.put(mm.getName(), new d(c, t));
       }

       // add the total entry and exit

       aGCTiming.put("Total", total);
    }
  }

  /**
   * Returns the start time for this GCTiming object.
   * 
   * @return The start time for this GCTiming object.
   */
  public long getGCCollectorStartTime()
  {
    return aStrtTime;
  }

  /**
   * Returns the last update time for this GCTiming object.
   * 
   * @return The last update time for this GCTiming object.
   */
  public long getGCCollectorLastUpdateTime()
  {
    return aLastTime;
  }

  /**
   * Returns the total elapsed time for this GCTiming object.
   * 
   * @return The total elapsed time for this GCTiming object.
   */
  public long getGCCollectorElapsedTime()
  {
    return aLastTime - aStrtTime;
  }

  /**
   * Returns the total number of garbage collection types.
   * 
   * @return The total number of garbage collection types.
   */
  public int getCollectorCount()
  {
    return aGCTiming.size() - 1;
  }

  /**
   * Returns the garbage collector run count for the input named garbage
   * collector. If not found -1 is returned.
   * 
   * @return The garbage collector run count for the input named garbage
   *         collector.
   */
  public long getCollectionCount(String name)
  {
    d cd = aGCTiming.get(name);
    if (cd == null)
      return -1;
    else
      return cd.count;
  }

  /**
   * Returns the total garbage collector run count for all garbage collectors.
   * 
   * @return The total garbage collector run count for all garbage collectors.
   */
  public long getTotalCollectionCount()
  {
    d cd = aGCTiming.get("Total");
    if (cd == null)
      return -1;
    else
      return cd.count;
  }

  /**
   * Returns the garbage collector elapsed time (milliseconds) for the input
   * named garbage collector. If not found -1 is returned.
   * 
   * @return The garbage collector elapsed time (milliseconds) for the input
   *         named garbage collector. If not found -1 is returned.
   */
  public long getCollectionTime(String name)
  {
    d cd = aGCTiming.get(name);
    if (cd == null)
      return -1;
    else
      return cd.tim;
  }

  /**
   * Returns the total garbage collector run time for all garbage collectors in
   * milliseconds.
   * 
   * @return The total garbage collector run time for all garbage collectors in
   *         milliseconds.
   */
  public long getTotalCollectionTime()
  {
    d cd = aGCTiming.get("Total");
    if (cd == null)
      return -1;
    else
      return cd.tim;
  }

  /**
   * Returns the set of garbage collector names in the internal map.
   * 
   * @return The set of garbage collector names in the internal map.
   */
  public Set<String> getNames()
  {
    return aGCTiming.keySet();
  }
}
