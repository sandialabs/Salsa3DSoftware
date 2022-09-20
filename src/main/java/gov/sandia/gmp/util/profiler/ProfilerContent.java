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

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerInteger;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.intrinsicsort.IntrinsicSort;

/**
 * The class, method, and line number content accumulated by some
 * Profiler object. This object contains the total number of
 * samples and their results as a map of ProfilerClassEntry objects
 * associated with their class names. The class entries contain a
 * map of all associated ProfilerMethodEntry objects for each
 * method accumulated in a class sample. And the method entries
 * contain a map of all line numbers and their counts accumulated
 * in each method sample. This function is used by the Profiler
 * to add entries and to output the content as a formatted string.
 * A user can also add other ProfilerContent objects to this one
 * to form a single ProfilerContent summary of many independent
 * samples taken of the same code base.
 *  
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class ProfilerContent implements Serializable
{
  /**
   * The map of all content for this ProfilerContent object.
   */
  private HashMap<String, ProfilerClassEntry> aClassMap        = null;

  /**
   * A class entry set used by the profiler to avoid incrementing an
   * input class more than once in a sample.
   */
  private ArrayList<ProfilerClassEntry>       aClassSampleSet  = null;

  /**
   * A method entry set used by the profiler to avoid incrementing an
   * input method more than once in a sample.
   */
  private ArrayList<ProfilerMethodEntry>      aMethodSampleSet = null;

  /**
   * The total sample count contained in this ProfilerContent object.
   */
  private int                                 aSampleCount     = 0;

  /**
   * The total sample accumulation time.
   */
  private long                                aAccumulateTime  = 0;

  /**
   * Standard constructor. Constructs a new map and a class and method sample
   * set.
   */
  public ProfilerContent()
  {
    aClassMap        = new HashMap<String, ProfilerClassEntry>(256);
    aClassSampleSet  = new ArrayList<ProfilerClassEntry>();
    aMethodSampleSet = new ArrayList<ProfilerMethodEntry>();
  }

  /**
   * Increments the accumulation time with the input time (milliseconds).
   * 
   * @param inc The input accumulation time.
   */
  public void incrementAccumulationTime(long inc)
  {
    aAccumulateTime += inc;
  }

  /**
   * Returns the accumulation time.
   * 
   * @return The accumulation time.
   */
  public long getAccumulationTime()
  {
    return aAccumulateTime;
  }

  /**
   * Returns the sample count.
   * 
   * @return The sample count.
   */
  public int getSampleCount()
  {
    return aSampleCount;
  }

  /**
   * Called by a profilers accumulate timer (Profiler.ProfilerSampleTask.run())
   * at the beginning of each new sample to increment the sample count and
   * clear the sample class and method sets.
   */
  public void initializeSampleAccumulation()
  {
    ++aSampleCount;
    for (ProfilerClassEntry ce: aClassSampleSet) ce.aIncr = false;
    aClassSampleSet.clear();
    for (ProfilerMethodEntry me: aMethodSampleSet) me.aIncr = false;
    aMethodSampleSet.clear();
  }

  /**
   * Clear accumulation.
   */
  public void clearAccumulation()
  {
    aClassMap.clear();
    aAccumulateTime = 0;
    aSampleCount = 0;
  }

  /**
   * Adds the input ProfilerContent to this ProfilerContent. This function
   * is used to add many profiles together to form a single common profile.
   * Note this is only valid when the profiles are over the same code base,
   * such as a set of distributed parallel nodes executing the same code
   * tasks.
   * 
   * @param pc The input ProfilerContent to be added to this one.
   */
  public void addProfilerContent(ProfilerContent pc)
  {
    // increment the total sample count and accumulation times.

    aSampleCount += pc.aSampleCount;
    aAccumulateTime += pc.aAccumulateTime;

    // loop over all class entries of the input ProfilerContent class map

    for (Map.Entry<String, ProfilerClassEntry> e: pc.aClassMap.entrySet())
    {
      // get the current class entry from the input map and see if it exists
      // in this content map ... if not make a copy and add it to this map

      ProfilerClassEntry cepc = e.getValue();
      ProfilerClassEntry ce = aClassMap.get(e.getKey());
      if (ce == null)
        aClassMap.put(e.getKey(), cepc.copy());
      else
      {
        // class entry already exists in this class map ... sync this class
        // entry with the input entry by adding the input class count to
        // this class count and processing all input method entries for
        // this class entry
        
        ce.aCount += cepc.aCount;
        for (Map.Entry<String, ProfilerMethodEntry> emthd:
             cepc.aMethodMap.entrySet())
        {
          // get the current method entry from the input map and see if it
          // exists in this content map ... if not make a copy and add it to
          // this map

          ProfilerMethodEntry me = ce.aMethodMap.get(emthd.getKey());
          ProfilerMethodEntry mepc = emthd.getValue();
          if (me == null)
            ce.aMethodMap.put(emthd.getKey(), mepc.copy());
          else
          {
            // method entry already exists in this method map ... sync this
            // method entry with the input entry by adding the input method
            // count to this method count and processing all input line number
            // entries for this method entry

            me.aCount += mepc.aCount;
            HashMapIntegerInteger.Iterator it = mepc.aLineMap.iterator();
            while (it.hasNext())
            {
              // get the current line number entry from the input map and see
              // if it exists in this content map ... if not make a copy and
              // add it to this map ... otherwise, increment the existing line
              // numbers count with the input count.

              HashMapIntegerInteger.Entry elnpc = it.nextEntry();
              HashMapIntegerInteger.Entry eln   = me.aLineMap.getEntry(elnpc.getKey());
              if (eln == null)
                me.aLineMap.put(elnpc.getKey(), elnpc.getValue());
              else
                eln.setValue(eln.getValue() + elnpc.getValue());
            }
          }
        }
      }
    }
  }

  /**
   * Called by a profilers accumulate timer (Profiler.ProfilerSampleTask.run())
   * to add the current class, method, and line number location to the class
   * map (aClassMap).
   * 
   * @param className A specific program stack class.
   * @param methodName A specific program stack method.
   * @param lineNumber A specific program stack line number.
   */
  public void accumulateSample(String className, String methodName,
                               int lineNumber)
  {
    // see if class entry exists in map ... if not create a new entry for this
    // class name and add it to the map

    ProfilerClassEntry ce = aClassMap.get(className);
    if (ce == null)
    {
      ce = new ProfilerClassEntry(className);
      aClassMap.put(className, ce);
    }

    // only increment class count the first time the class appears in the
    // current sample. If it is not in the current sample increment the count
    // and add it to the sample set.

    if (!ce.aIncr)
    {
      ++ce.aCount;
      ce.aIncr = true;
      aClassSampleSet.add(ce);
    }

    // see if method entry exists in map ... if not create a new entry for
    // this method name and add it to the map

    ProfilerMethodEntry me = ce.aMethodMap.get(methodName);
    if (me == null)
    {
      me = new ProfilerMethodEntry(methodName);
      ce.aMethodMap.put(methodName, me);
    }

    // only increment method count the first time the method appears in the
    // current sample. If it is not in the current sample increment the count
    // and add it to the sample set.

    if (!me.aIncr)
    {
      ++me.aCount;
      me.aIncr = true;
      aMethodSampleSet.add(me);
    }

    // see if line number entry exists in map ... if not create a new entry
    // for this line number and add it to the map with a count of 1 ...
    // otherwise increment the existing line numbers count.

    HashMapIntegerInteger.Entry le = me.aLineMap.getEntry(lineNumber);
    if (le == null)
      me.aLineMap.put(lineNumber, 1);
    else
      le.setValue(le.getValue() + 1);
  }

  /**
   * Returns the profiler content as a formatted string. Each line is
   * prepended with prepnd. The format is defined as
   * 
   *  "        Level      Count   Total%  Class%  Method%    Name/#
   *  "    CLASS:        ddddddd  ffffff  ffffff  ffffff     ssssss"
   *  "        METHOD:   ddddddd  ffffff  ffffff  ffffff     ssssss"
   *  "            LINE: ddddddd  ffffff  ffffff  ffffff     dddddd"
   * 
   * Each line is either CLASS, METHOD, or LINE. The count represents the
   * total sample count for the respective entry, Total% is the fractional
   * percentage of the entry with respect to the total sample count. Class%
   * is the fractional percentage of the entry sample count with respect to the
   * owning class sample count (only output for METHOD and LINE entries). And,
   * finally, Method% is the fractional percentage of the entry sample count
   * with respect to the method sample count (only output for LINE entries).
   * The Name/# is the class name, method name, or line # of the respective
   * entry. All outputs are sorted from the largest sample size to the smallest
   * for all entries.
   * 
   * @param prepnd Buffer string prepended to each line.
   * @return The formatted profiler content string. 
   */
  public String getAccumulationString(String prepnd)
  {
    // return if no samples have been recorded

    if (aSampleCount == 0) return "";

    // create string buffer to hold output

    StringBuffer sb = new StringBuffer(4096);

    // output sample count and table header

    sb.append(prepnd + "    Sample Count = " + aSampleCount + NL + NL);
    sb.append(prepnd + "        Level      Count   Total%  Class%  Method%" +
              "    Name/#" + NL);
    sb.append(prepnd + Globals.repeat("=", 80) + NL);

    // Define class, method, and line table format specifications
    //"        Level      Count   Total%  Class%  Method%    Name/#
    //"    CLASS:        ddddddd  ffffff  ffffff  ffffff     ssssss"
    //"        METHOD:   ddddddd  ffffff  ffffff  ffffff     ssssss"
    //"            LINE: ddddddd  ffffff  ffffff  ffffff     dddddd"

    String classFormat  = "    CLASS:        %7d  %6.2f  ------  ------     ";
    String methodFormat = "        METHOD:   %7d  %6.2f  %6.2f  ------     ";
    String lineFormat   = "            LINE: %7d  %6.2f  %6.2f  %6.2f     ";

    // create class count and class entry arrays and fill them ... sort them
    // (ascending) if more than one entry exists

    int[] cc = new int [aClassMap.size()];
    ProfilerClassEntry[] ce = new ProfilerClassEntry [aClassMap.size()];
    int i = 0;
    for (Map.Entry<String, ProfilerClassEntry> e: aClassMap.entrySet())
    {
      cc[i] = e.getValue().aCount;
      ce[i] = e.getValue();
      ++i;
    }
    if (ce.length > 1) IntrinsicSort.sort(cc, ce);

    // loop over all class entries in descending count order
    // (largest to smallest)

    for (i = ce.length - 1; i > -1; --i)
    {
      // report class name (ce[i].aName) count (ce[i].aCount)

      sb.append(prepnd + String.format(classFormat, ce[i].aCount,
                (100.0 * ce[i].aCount / aSampleCount)) + ce[i].aName + NL);

      // create method count and method entry arrays and fill them ... sort them
      // (ascending) if more than one entry exists

      int[] mc = new int [ce[i].aMethodMap.size()];
      ProfilerMethodEntry[] me = new ProfilerMethodEntry [ce[i].aMethodMap.size()];
      int j = 0;
      for (Map.Entry<String, ProfilerMethodEntry> e: ce[i].aMethodMap.entrySet())
      {
        mc[j] = e.getValue().aCount;
        me[j] = e.getValue();
        ++j;
      }
      if (me.length > 1) IntrinsicSort.sort(mc, me);

      // loop over all method entries in descending count order
      // (largest to smallest)

      for (j = me.length - 1; j > -1; --j)
      {
        // report method name (me[j].aName) count (me[j].aCount)

        double mcnt = 100.0 * me[j].aCount;
        sb.append(prepnd + String.format(methodFormat, me[j].aCount,
                  (mcnt / aSampleCount), (mcnt / ce[i].aCount)) +
                  me[j].aName + NL);

        // create line count and line number arrays and fill them ... sort them
        // (ascending) if more than one entry exists

        int[] lc = new int [me[j].aLineMap.size()];
        int[] ln = new int [me[j].aLineMap.size()];
        int k = 0;
        HashMapIntegerInteger.Iterator it = me[j].aLineMap.iterator();
        while (it.hasNext())
        {
          HashMapIntegerInteger.Entry e = it.nextEntry();
          lc[k] = e.getValue();
          ln[k] = e.getKey();
          ++k;
        }
        if (ln.length > 1) IntrinsicSort.sort(lc, ln);

        // loop over all line entries in descending count order
        // (largest to smallest)

        for (k = ln.length - 1; k > -1; --k)
        {
          // report line number (ln[k]) count (lc[k])

          double lcnt = 100.0 * lc[k];
          sb.append(prepnd + String.format(lineFormat, lc[k],
                    (lcnt / aSampleCount), (lcnt / ce[i].aCount),
                    (lcnt / me[j].aCount)) + ln[k] + NL);
        }
      }
    }

    // done reset flag and return output string

    sb.append(NL);
    return sb.toString();
  }
}
