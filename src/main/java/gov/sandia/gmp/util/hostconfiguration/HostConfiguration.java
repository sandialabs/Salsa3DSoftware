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
package gov.sandia.gmp.util.hostconfiguration;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import gov.sandia.gmp.util.globals.Globals;

/**
 * Defines all Servers used in the GMP distributed memory parallel cluster.
 * Each machine is defined within an internal Machine class. A map of all host
 * names associated with their Machine class is maintained for inspection and
 * manipulation.
 * 
 * @author jrhipp
 *
 */
public final class HostConfiguration
{

  /**
   * An enum designation of each parameter type in the Machine class.
   * 
   * @author jrhipp
   *
   */
  public enum HostConfigurationCategory
  {
    HOST_NAME,
    HOST_ALIAS,
    XEON_NUM,
    ARCHITECTURE,
    ARCHITECTURE_TICK,
    CPU_GHZ,
    MEM_GB,
    CORES,
    THREADS,
    DIE_NM,
    DEVL_HIST,
    PERFORMANCE;
  }

  /**
   * The map that associates each host name with a Machine class description.
   */
  private HashMap<String, Machine>  aMachineMap = null;

  /**
   * Inner class that descriptively defines a server. The information includes
   * the host name, host alias, Xeon number, Intel architecture, architecture
   * tick, cpu GHZ, memory (GB), cores, threads, DIE size, development history
   * index, and a performance factor.
   * 
   * @author jrhipp
   *
   */
  public class Machine
  {
    /**
     * The host name.
     */
    public String aHostName;
    
    /**
     * The alias name of the host.
     */
    public String aHostAlias;
    
    /**
     * The Intel Xeon number. 
     */
    public String aXeonNum;
    
    /**
     * The Intel Architecture description.
     */
    public String aArchitecture;
    
    /**
     * The Intel Architecture Tick description.
     */
    public String aArchitectureTick;
    
    /**
     * The CPU cycle speed (GHz).
     */
    public double aCPU_GHZ;
    
    /**
     * The total memory available on the host (GB).
     */
    public int    aMem_GB;
    
    /**
     * The number of cores on the host.
     */
    public int    aCores;
    
    /**
     * The number of threads on the host.
     */
    public int    aThreads;
    
    /**
     * The CPU DIE size (nm).
     */
    public int    aDIE_nm;
    
    /**
     * The development history index (1=oldest, n(>1)=newest).
     */
    public int    aDevlHist;
    
    /**
     * A performance weighting factor.
     */
    public double aPerformance;

    /**
     * The Machine constructor.
     * 
     * @param hostname    The host name.
     * @param alias       The host alias.
     * @param xeonname    The Intel Xeon number.
     * @param cpuspd      The CPU cycle speed (GHz).
     * @param mem         The total available memory (GB).
     * @param cores       The number of cores.
     * @param threads     The number of threads.
     * @param die         The CPU DIE size (nm).
     * @param arch        The architecture description.
     * @param archtick    The architecture tick description.
     * @param devlhist    The development history index.
     * @param performance The performance factor.
     */
    public Machine(String hostname, String alias, String xeonname,
                   double cpuspd, int mem, int cores, int threads,
                   int die, String arch, String archtick, int devlhist,
                   double performance)
    {
      aHostName         = hostname;
      aHostAlias        = alias;
      aXeonNum          = xeonname;
      aArchitecture     = arch;
      aArchitectureTick = archtick;
      aCPU_GHZ          = cpuspd;
      aMem_GB           = mem;
      aCores            = cores;
      aThreads          = threads;
      aDIE_nm           = die;
      aDevlHist         = devlhist;
      aPerformance      = performance;
    }

    /**
     * Returns the amount of memory per thread on this host.
     * 
     * @return The amount of memory per thread on this host.
     */
    public String MemPerThread()
    {
      return String.format("%6.2f", (double) aMem_GB / aThreads);
    }

    /**
     * Returns the amount of memory per core on this host.
     * 
     * @return The amount of memory per core on this host.
     */
    public String MemPerCore()
    {
      return String.format("%6.2f", (double) aMem_GB / aCores);
    }
    
    /**
     * Returns the host name.
     * 
     * @return The host name.
     */
    public String getHostName()
    {
      return aHostName;
    }

    /**
     * Returns the host alias name.
     * 
     * @return The host alias name.
     */
    public String getAliasName()
    {
      return aHostAlias;
    }

    /**
     * Returns the alias name if defined else the host name is returned.
     * 
     * @return The alias name if defined else the host name is returned.
     */
    public String getName()
    {
      if (aHostAlias.equals(""))
        return aHostName;
      else
        return aHostAlias;
    }
  } // end Machine definition

  /**
   * Default constructor.
   */
  public HostConfiguration()
  {
    aMachineMap = new HashMap<String, Machine>();
    defineHosts();
  }

  /**
   * Defines the current set of hosts in the GMP distributed memory cluster.
   * Add new ones and remove old ones from this list, or modify existing host
   * information after upgrades or redefinitions. This function is called once
   * by the HostConfiguration constructor.
   * 
   * The developmentHistoryIndex is an integer associated with the architecture
   * tick time line as follows:
   * 
   *   Tick      Index           Release
   *   Core        1              2006
   *   Penryn      2              2007
   *   Nehalem     3              2008
   *   Westmere    4              2010
   *   Sandybridge 5              2011
   *   Ivybridge   6              2012
   *   Haswell     7  (future)    2013
   *   Broadwell   8  (future)
   *   Skylake     9  (future)
   *   Skymont     10 (future)
   */
  private void defineHosts()
  {
    //addMachine(hostName, alias, xeonName, cpuSpeed, memory, cores, threads,
    //           dieSize, architecture, architectureTick,
    //           developmentHistoryIndex, performanceFactor);

    addMachine("crunk", "", "Xeon x5355", 2.66, 48, 8, 8, 65, "Core", "Core", 1, 1.0);

    addMachine("newton1", "", "Xeon x7350", 2.93, 64, 16, 16, 65, "Core", "Core", 1, 1.0);
    addMachine("newton2", "", "Xeon x7350", 2.93, 64, 16, 16, 65, "Core", "Core", 1, 1.0);
    addMachine("newton3", "", "Xeon x7350", 2.93, 64, 16, 16, 65, "Core", "Core", 1, 1.0);
    addMachine("newton4", "", "Xeon x7350", 2.93, 64, 16, 16, 65, "Core", "Core", 1, 1.0);
    addMachine("fignewton", "", "Xeon x7350", 2.93, 64, 16, 16, 65, "Core", "Core", 1, 1.0);
    addMachine("newton6", "", "Xeon x7350", 2.93, 64, 16, 16, 65, "Core", "Core", 1, 1.0);
    addMachine("newton7", "", "Xeon x7350", 2.93, 64, 16, 16, 65, "Core", "Core", 1, 1.0);

    addMachine("fig1", "", "Xeon x7350", 2.93, 128, 16, 16, 65, "Core", "Core", 1, 1.0);
    addMachine("fig2", "", "Xeon x7350", 2.93, 128, 16, 16, 65, "Core", "Core", 1, 1.0);

    addMachine("oreo1", "", "Xeon x5420", 2.50, 8, 8, 8, 45, "Core", "Penryn", 2, 1.0);
    addMachine("oreo2", "", "Xeon x5420", 2.50, 8, 8, 8, 45, "Core", "Penryn", 2, 1.0);

    addMachine("s907750", "jrhipp  DTOld", "Xeon x5472", 3.00, 16, 8, 8, 45, "Core", "Penryn", 2, 1.0);
    addMachine("s906383", "sheck   DT", "Xeon x5472", 3.00, 16, 8, 8, 45, "Core", "Penryn", 2, 1.0);
    addMachine("s907752", "sballar DT", "Xeon x5472", 3.00, 16, 8, 8, 45, "Core", "Penryn", 2, 1.0);
    addMachine("s909456", "margonz DT", "Xeon x5472", 3.00, 16, 8, 8, 45, "Core", "Penryn", 2, 1.0);
    addMachine("s907751", "mchang  DT", "Xeon x5472", 3.00, 16, 8, 8, 45, "Core", "Penryn", 2, 1.0);

    addMachine("fignewton1", "", "Xeon x5460", 3.16, 32, 8, 8, 45, "Core", "Penryn", 2, 1.0);

    addMachine("oreo3", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    addMachine("oreo4", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    addMachine("oreo5", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    addMachine("oreo6", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    addMachine("oreo7", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    addMachine("oreo8", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    
    addMachine("dblstuff1", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    addMachine("dblstuff2", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    addMachine("dblstuff3", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    addMachine("dblstuff4", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    addMachine("dblstuff5", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    addMachine("dblstuff6", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);
    addMachine("dblstuff7", "", "Xeon x5570", 2.93, 48, 8, 16, 45, "Nehalem", "Nehalem", 3, 1.0);

    addMachine("biscochito", "", "Xeon E7-4870", 2.40, 256, 40, 80, 32, "Nehalem", "Westmere", 4, 1.0);

    addMachine("s952797", "brhamle DT", "Xeon E5-2620", 3.60, 32, 8, 16, 32, "SandyBridge", "SandyBridge", 5, 1.0);
    addMachine("s952798", "tjdrael DT", "Xeon E5-2620", 3.60, 32, 8, 16, 32, "SandyBridge", "SandyBridge", 5, 1.0);
    addMachine("s952799", "avencar DT", "Xeon E5-2620", 3.60, 32, 8, 16, 32, "SandyBridge", "SandyBridge", 5, 1.0);
    addMachine("s952800", "bjlawry DT", "Xeon E5-2620", 3.60, 32, 8, 16, 32, "SandyBridge", "SandyBridge", 5, 1.0);
    addMachine("s952801", "dbcarr  DT", "Xeon E5-2620", 3.60, 32, 8, 16, 32, "SandyBridge", "SandyBridge", 5, 1.0);
    addMachine("s952802", "cjyoung DT", "Xeon E5-2620", 3.60, 32, 8, 16, 32, "SandyBridge", "SandyBridge", 5, 1.0);
    addMachine("s952820", "lewisje DT", "Xeon E5-2620", 3.60, 32, 8, 16, 32, "SandyBridge", "SandyBridge", 5, 1.0);
    addMachine("s957873", "jrhipp  DT", "Xeon E5-2620", 3.60, 64, 8, 16, 32, "SandyBridge", "SandyBridge", 5, 1.0);
  }

  /**
   * Add the new host to the internal map of host name associated with Machine.
   * 
   * @param hostname    The host name.
   * @param alias       The host alias.
   * @param xeonname    The Intel Xeon number.
   * @param cpuspd      The CPU cycle speed (GHz).
   * @param mem         The total available memory (GB).
   * @param cores       The number of cores.
   * @param threads     The number of threads.
   * @param die         The CPU DIE size (nm).
   * @param arch        The architecture description.
   * @param archtick    The architecture tick description.
   * @param devlhist    The development history index.
   * @param performance The performance factor.
   */
  private void addMachine(String hostname, String alias, String xeonname,
                          double cpuspd, int mem, int cores, int threads,
                          int die, String arch, String archtick, int devlhist,
                          double performance)
  {
    Machine mach = new Machine(hostname, alias, xeonname, cpuspd, mem, cores,
                               threads, die, arch, archtick, devlhist,
                               performance);
    aMachineMap.put(hostname,  mach);
  }

  /**
   * Returns a map of all unique Xeon numbers associated with a set of host
   * names that have that Xeon number.
   *  
   * @return A map of all unique Xeon numbers associated with a set of host
   *         names that have that Xeon number.
   */
  public HashMap<String, HashSet<String>> getXeonSet()
  {
    // create the set to be returned and loop over all Machine map entries

    HashMap<String, HashSet<String>> xeonMapSet;
    xeonMapSet = new HashMap<String, HashSet<String>>();
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
    {
      // get Xeon number and see if map contains it

      String xeon = e.getValue().aXeonNum;
      HashSet<String> hs = xeonMapSet.get(xeon);
      if (hs == null)
      {
        // not defined create a new host set and add to map associated with
        // Xeon number

        hs = new HashSet<String>();
        xeonMapSet.put(xeon,  hs);
      }

      // add host name to host set and continue

      hs.add(e.getKey());
    }

    // return Xeon map of associated hosts

    return xeonMapSet;
  }

  /**
   * Returns a map of all unique Xeon numbers associated with a set of
   * Machines (hosts) that have that Xeon number.
   *  
   * @return A map of all unique Xeon numbers associated with a set of
   *         Machines (hosts) that have that Xeon number.
   */
  public HashMap<String, HashSet<Machine>> getMapOfXeonType()
  {
    // create the map to be returned and loop over all Machine map entries

    HashMap<String, HashSet<Machine>> xeonTypeMap;
    xeonTypeMap = new HashMap<String, HashSet<Machine>>();
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
    {
      // get machine and xeon number and see if map contains the number

      Machine mach = e.getValue();
      String xeontype = mach.aXeonNum;
      HashSet<Machine> xeonMachSet = xeonTypeMap.get(xeontype);
      if (xeonMachSet == null)
      {
        // not defined create a new machine set and add to map associated with
        // Xeon number

        xeonMachSet = new HashSet<Machine>();
        xeonTypeMap.put(xeontype, xeonMachSet);
      }

      // add machine to machine set and continue

      xeonMachSet.add(mach);
    }

    // return Xeon number map of associated machines
    
    return xeonTypeMap;
  }

  /**
   * Returns a map of all unique architecture tick entries associated with a
   * set of host names that have that architecture tick.
   *  
   * @return A map of all unique architecture tick entries associated with a
   *         set of host names that have that architecture tick.
   */
  public HashMap<String, HashSet<String>> getArchitectureTickSet()
  {
    // create the set to be returned and loop over all Machine map entries

    HashMap<String, HashSet<String>> archTickMapSet;
    archTickMapSet = new HashMap<String, HashSet<String>>();
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
    {
      // get architecture tick and see if map contains it

      String archTick = e.getValue().aArchitectureTick;
      HashSet<String> hs = archTickMapSet.get(archTick);
      if (hs == null)
      {
        // not defined create a new host set and add to map associated with
        // architecture tick

        hs = new HashSet<String>();
        archTickMapSet.put(archTick,  hs);
      }

      // add host name to host set and continue

      hs.add(e.getKey());
    }

    // return architecture tick map of associated hosts
    
    return archTickMapSet;    
  }

  /**
   * Returns a map of all unique architecture tick entries associated with a
   * set of Machines that have that architecture tick.
   *  
   * @return A map of all unique architecture tick entries associated with a
   *         set of Machines that have that architecture tick.
   */
  public HashMap<String, HashSet<Machine>> getMapOfArchitecture()
  {
    // create the set to be returned and loop over all Machine map entries

    HashMap<String, HashSet<Machine>> archTypeMap;
    archTypeMap = new HashMap<String, HashSet<Machine>>();
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
    {
      // get architecture tick and Machine and see if map contains it

      Machine mach = e.getValue();
      String archtype = mach.aArchitectureTick;
      HashSet<Machine> archMachSet = archTypeMap.get(archtype);
      if (archMachSet == null)
      {
        // not defined create a new Machine set and add to map associated with
        // architecture tick

        archMachSet = new HashSet<Machine>();
        archTypeMap.put(archtype, archMachSet);
      }

      // add Machine to Machine set and continue

      archMachSet.add(mach);
    }

    // return architecture tick map of associated Machines
    
    return archTypeMap;
  }

  /**
   * Returns the average CPU cycle speed (GHz) for all machines in the GMP
   * cluster.
   * 
   * @return The average CPU cycle speed (GHz) for all machines in the GMP
   *         cluster
   */
  public double avgCPUSpeedPerHost()
  {
    double cpuspd = 0.0;
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
      cpuspd += e.getValue().aCPU_GHZ;
    
    return cpuspd / aMachineMap.size();
  }

  /**
   * Returns the average CPU cycle speed (GHz) per core from all machines in
   * the GMP cluster.
   * 
   * @return The average CPU cycle speed (GHz) per core from all machines in
   *         the GMP cluster.
   */
  public double avgCPUSpeedPerCore()
  {
    int cores = 0;
    double cpuspd = 0.0;
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
    {
      cpuspd += e.getValue().aCores * e.getValue().aCPU_GHZ;
      cores  += e.getValue().aCores;
    }
    
    return cpuspd / cores;
  }

  /**
   * Returns the average CPU cycle speed (GHz) per thread from all machines in
   * the GMP cluster.
   * 
   * @return The average CPU cycle speed (GHz) per thread from all machines in
   *         the GMP cluster.
   */
  public double avgCPUSpeedPerThread()
  {
    int threads = 0;
    double cpuspd = 0.0;
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
    {
      cpuspd  += e.getValue().aThreads * e.getValue().aCPU_GHZ;
      threads += e.getValue().aThreads;
    }
    
    return cpuspd / threads;
  }

  /**
   * Returns the average total memory available per host in the GMP cluster.
   * 
   * @return The average total memory available per host in the GMP cluster.
   */
  public double avgMemoryPerHost()
  {
    int mem = 0;
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
      mem += e.getValue().aMem_GB;
    
    return (double) mem / aMachineMap.size();
  }

  /**
   * Returns the average total memory available per core in the GMP cluster.
   * 
   * @return The average total memory available per core in the GMP cluster.
   */
  public double avgMemoryPerCore()
  {
    int cores = 0;
    int mem = 0;
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
    {
      mem += e.getValue().aMem_GB;
      cores += e.getValue().aCores;
    }
    
    return (double) mem / cores;
  }

  /**
   * Returns the average total memory available per thread in the GMP cluster.
   * 
   * @return The average total memory available per thread in the GMP cluster.
   */
  public double avgMemoryPerThread()
  {
    int threads = 0;
    int mem = 0;
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
    {
      mem += e.getValue().aMem_GB;
      threads += e.getValue().aThreads;
    }
    
    return (double) mem / threads;
  }

  /**
   * Returns the average number of threads per core in the GMP cluster.
   * 
   * @return The average number of threads per core in the GMP cluster.
   */
  public double avgThreadsPerCore()
  {
    int threads = 0;
    int cores = 0;
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
    {
      cores   += e.getValue().aCores;
      threads += e.getValue().aThreads;
    }
    
    return (double) threads / cores;
  }

  /**
   * Returns the total number of hosts in the GMP cluster.
   * 
   * @return The total number of hosts in the GMP cluster.
   */
  public int totalHosts()
  {
    return aMachineMap.size();
  }

  /**
   * Returns the total number of hosts in the GMP cluster.
   * 
   * @return The total number of hosts in the GMP cluster.
   */
  public int totalMemory()
  {
    int mem = 0;
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
      mem += e.getValue().aMem_GB;
    return mem;
  }

  /**
   * Returns the total number of cores in the GMP cluster.
   * 
   * @return The total number of cores in the GMP cluster.
   */
  public int totalCores()
  {
    int cores = 0;
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
      cores += e.getValue().aCores;
    return cores;
  }

  /**
   * Returns the total number of threads in the GMP cluster.
   * 
   * @return The total number of threads in the GMP cluster.
   */
  public int totalThreads()
  {
    int threads = 0;
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
      threads += e.getValue().aThreads;
    return threads;
  }

  /**
   * Returns the ith Machine parameter type name.
   * 
   * @param i The parameter index to be returned.
   * @return The ith Machine parameter type name.
   */
  public String getParamDescription(int i)
  {
    switch (i)
    {
      case 0:
        return "Host Name";
      case 1:
        return "Host Alias";
      case 2:
        return "Xeon Number";
      case 3:
        return "Intel Architecture";
      case 4:
        return "Intel Architecture (Tick)";
      case 5:
        return "CPU Speed (GHz)";
      case 6:
        return "Memory (GB)";
      case 7:
        return "Cores";
      case 8:
        return "Threads";
      case 9:
        return "Die Size (nm)";
      case 10:
        return "Architecture Development Sequence";
      case 11:
        return "Performance Rating";
      default:
        return "";
    }
  }

  /**
   * Returns the total number of descriptive machine parameters.
   * 
   * @return The total number of descriptive machine parameters.
   */
  public int getParamCount()
  {
    return 12;
  }

  /**
   * Returns the parameter value (as a string) from the input host for the
   * parameter defined by the input enum HostConfigurationCategory.
   * 
   * @param hostname The host name for which the parameter value will be
   *                 returned.
   * @param hcc      The HostConfigurationCategory defining the parameter to be
   *                 returned.
   * @return The parameter value (as a string) from the input host for the
   *         parameter defined by the input enum HostConfigurationCategory.
   */
  public String getParameter(String hostname, HostConfigurationCategory hcc)
  {
    // get the host name short form

    int istrt = hostname.indexOf(".");
    if (istrt > -1)
      hostname = hostname.substring(0, istrt);

    // if the host name is defined return the requested parameter ... otherwise
    // return an empty string.

    Machine mach = aMachineMap.get(hostname);
    if (mach != null)
      return getParam(mach, hcc.ordinal());
    else    
      return "";
  }

  /**
   * Returns the ith parameter of the input Machine.
   * 
   * @param mach The machine for which the parameter will be returned.
   * @param i    The parameter index.
   * @return     The ith parameter of the input Machine.
   */
  public String getParam(Machine mach, int i)
  {
    switch (i)
    {
      case 0:
        return mach.aHostName;
      case 1:
        return mach.aHostAlias;
      case 2:
        return mach.aXeonNum;
      case 3:
        return mach.aArchitecture;
      case 4:
        return mach.aArchitectureTick;
      case 5:
        return Double.toString(mach.aCPU_GHZ);
      case 6:
        return Integer.toString(mach.aMem_GB);
      case 7:
        return Integer.toString(mach.aCores);
      case 8:
        return Integer.toString(mach.aThreads);
      case 9:
        return Integer.toString(mach.aDIE_nm);
      case 10:
        return Integer.toString(mach.aDevlHist);
      case 11:
        return Double.toString(mach.aPerformance);
      default:
        return "";
    }
  }

  /**
   * Returns a summary of the host configuration information.
   * 
   * @return A summary of the host configuration information.
   * 
   */
  @Override
  public String toString()
  {
    return toString("");
  }

  public Machine getMachine(String hostName)
  {
  	return aMachineMap.get(hostName);
  }

  /**
   * Returns a summary of the host configuration information.
   * 
   * @param hdr A header appended to the beginning of each line.
   * @return A summary of the host configuration information.
   */
  public String toString(String hdr)
  {
    String s = "";

    s += hdr + "Host Machine Configuration Information:" + NL + NL;

    s += hdr + "    Total Hosts                         = " +
         totalHosts() + NL;
    s += hdr + "    Average CPU Cycles Per Host   (GHz) = " +
         String.format("%6.2f", avgCPUSpeedPerHost()) + NL;
    s += hdr + "    Average Memory     Per Host   (GB)  = " +
         String.format("%6.2f", avgMemoryPerHost()) + NL + NL;
    
    s += hdr + "    Total Cores                         = " + 
         totalCores() + NL;
    s += hdr + "    Average CPU Cycles Per Core   (GHz) = " +
         String.format("%6.2f", avgCPUSpeedPerCore()) + NL;
    s += hdr + "    Average Memory     Per Core   (GB)  = " +
         String.format("%6.2f", avgMemoryPerCore()) + NL + NL;

    s += hdr + "    Total Threads                       = " + 
         totalThreads() + NL;
    s += hdr + "    Average CPU Cycles Per Thread (GHz) = " +
         String.format("%6.2f", avgCPUSpeedPerThread()) + NL;
    s += hdr + "    Average Memory     Per Thread (GB)  = " +
         String.format("%6.2f", avgMemoryPerThread()) + NL + NL;

    s += hdr + "    Total Memory                  (GB)  = " + 
         totalMemory() + NL;
    s += hdr + "    Average Threads    Per Core         = " +
         String.format("%6.2f", avgThreadsPerCore()) + NL + NL;
    
    s += dumpHostMachineInfo(hdr + "    ", 2);

    return s;
  }

  /**
   * Returns a string table of all host machine information.
   * 
   * @param hdr    A header appended to every line.
   * @param colSpc The inter-column spacing.
   * @return       A string table of all host machine information.
   */
  public String dumpHostMachineInfo(String hdr, int colSpc)
  {
    // define title, column headers, and row and column alignment

    String title = "Host Machine Configuration Table";

    int ncols = getParamCount() + 1;
    String[][] colHdr = {{"", "Host", "Host", "Xeon", "", "Tick", "CPU",
                          "Memory",
                          "", "", "Die Size", "Development", ""},
                         {"Index", "Name", "Alias", "Number", "Architecture",
                          "Architecture", "(GHz)", "(GB)", "Cores", "Threads",
                          "(nm)", "History", "Performance"}};
 
    Globals.TableAlignment ta         = Globals.TableAlignment.LEFT;
    Globals.TableAlignment rowAlign   = ta;
    Globals.TableAlignment[] colAlign = {ta, ta, ta, ta, ta, ta, ta,
                                         ta, ta, ta, ta, ta, ta};

    // Build the data table from an ordered machine list

    int colspc = 2;
    String[][] data = new String [aMachineMap.size()][ncols];
    ArrayList<Machine> machList = orderedMachineList();

    // build the data table

    for (int i = 0; i < machList.size(); ++i)
    {
      Machine mach = machList.get(i);
      String[] dataRow = data[i];
      dataRow[0] = Integer.toString(i+1);
      for (int j = 0; j < getParamCount(); ++j)
        dataRow[j+1] = getParam(mach, j);
    }

    // output the table to the string s.

    String s = "";
    s += Globals.makeTable(hdr, title, "", colHdr, colAlign, null,
                           rowAlign, data, colspc);

    // return the table.
 
    return s;
  }

  /**
   * Comparator of machine host names.
   */
  public class compareMachineHostName implements Comparator<Machine>
  {
    @Override
    public int compare(Machine m1, Machine m2)
    {
      int scmp = m1.aHostName.compareTo(m2.aHostName);
      if (scmp > 0)
        return 1;
      else if (scmp < 0)
        return -1;
      else
      return 0;
    }
  }

  /**
   * Comparator of machine development history.
   */
  public class compareMachineDevlHistory implements Comparator<Machine>
  {
    @Override
    public int compare(Machine m1, Machine m2)
    {
      if (m1.aDevlHist < m2.aDevlHist)
        return 1;
      else if (m1.aDevlHist > m2.aDevlHist)
        return -1;
      else
      return 0;
    }
  }

  /**
   * Comparator of machine core count.
   */
  public class compareMachineCores implements Comparator<Machine>
  {
    @Override
    public int compare(Machine m1, Machine m2)
    {
      if (m1.aCores < m2.aCores)
        return 1;
      else if (m1.aCores > m2.aCores)
        return -1;
      else
      return 0;
    }
  }

  /**
   * Comparator of machine CPU cycle speed.
   */
  public class compareMachineCPU implements Comparator<Machine>
  {
    @Override
    public int compare(Machine m1, Machine m2)
    {
      if (m1.aCPU_GHZ < m2.aCPU_GHZ)
        return 1;
      else if (m1.aCPU_GHZ > m2.aCPU_GHZ)
        return -1;
      else
      return 0;
    }
  }

  /**
   * Comparator of machine total available memory.
   */
  public class compareMachineMemory implements Comparator<Machine>
  {
    @Override
    public int compare(Machine m1, Machine m2)
    {
      if (m1.aMem_GB < m2.aMem_GB)
        return 1;
      else if (m1.aMem_GB > m2.aMem_GB)
        return -1;
      else
      return 0;
    }
  }

  /**
   * Returns an ordered list of all machines in the GMP distributed
   * memory cluster. The list is ordered first by machine development history,
   * then by number of cores, then by CPU cycle speed, then by total
   * available memory, and finally by host name.
   * 
   * @return An ordered list of all machines in the GMP distributed
   *         memory cluster.
   */
  public ArrayList<Machine> orderedMachineList()
  {
    // get list of all machines
    
    ArrayList<Machine> machList = new ArrayList<Machine>(aMachineMap.size());
    for (Map.Entry<String, Machine> e: aMachineMap.entrySet())
      machList.add(e.getValue());

    // ordered first by machine development history,
    // then by number of cores, then by CPU cycle speed, then by total
    // available memory, and finally by host name.

    Collections.sort(machList, new compareMachineHostName());
    Collections.sort(machList, new compareMachineMemory());
    Collections.sort(machList, new compareMachineCPU());
    Collections.sort(machList, new compareMachineCores());
    Collections.sort(machList, new compareMachineDevlHistory());

    // return ordered list

    return machList;
  }

  /**
   * Outputs a string table of machine types ordered by the input hash map.
   * The hash map contains a set of host entries for some key (e.g. Xeon
   * number).
   * 
   * @param hdr        The header appended to every line ouput.
   * @param category   The input maps category name (used as a title).
   * @param machineMap The input map that will be output.
   * 
   * @return A string table of machine types ordered by the input hash map.
   *         The hash map contains a set of host entries for some key (e.g. Xeon
   *         number).
   */
  public String toString(String hdr, String category,
                         HashMap<String, HashSet<Machine>> machineMap)
  {
    // initialize the parameter and header

    boolean[] noChange = new boolean [12];
    String[]  param    = new String [12];
    String hdrMach = hdr + "        ";
    String s = "";
    
    // create a sorted map and populate in order of machine development history

    TreeMap<Integer, HashSet<String>> sortedMap;
    sortedMap = new TreeMap<Integer, HashSet<String>>();
    for (Map.Entry<String, HashSet<Machine>> e: machineMap.entrySet())
    {
      int si = Integer.valueOf(getParam(e.getValue().iterator().next(), 10));
      HashSet<String> valMap = sortedMap.get(si);
      if (valMap == null)
      {
        valMap = new HashSet<String>();
        sortedMap.put(si, valMap);
      }
      valMap.add(e.getKey());
    }

    // output title and loop over all sorted entries

    s += hdr + category + NL + NL;
    for (Map.Entry<Integer, HashSet<String>> e: sortedMap.entrySet())
    {
      // get set associated with current sorted entry and loop over each entry

      HashSet<String> valSet = e.getValue();
      for (String key: valSet)
      {
        // get set of machines associated with current group name ... output
        // group name, initialize counters, and loop over all machine entries
        // in the set
     
        HashSet<Machine> machineSet = machineMap.get(key);
        s += hdr + "    Group Name: " + key + NL;
        boolean firstEntry = true;
        int colwidth = 0;
        int catIndex = 0;
        for (Machine mach: machineSet)
        {
          // output first entry with all parameters that are the same for all
          // entries in the set. Ouput subsequent entries with just parameter
          // differences

          if (firstEntry)
          {
            // first entry ... output all common parameter values.

            firstEntry = false;
            for (int i = 2; i < 12; ++i)
            {
              noChange[i] = true;
              param[i] = getParam(mach, i);
              if (colwidth < getParamDescription(i).length())
                colwidth = getParamDescription(i).length();
              if (param[i].equals(e.getKey())) catIndex = i;
            }
          }
          else
          {
            // output only parameter differences
            colwidth = 0;
            for (int i = 2; i < 12; ++i)
            {
              if (!param[i].equals(getParam(mach, i)))
                noChange[i] = false;
              else
                if (colwidth < getParamDescription(i).length())
                  colwidth = getParamDescription(i).length();
            }
          }
        }
  
        // add parameter descriptions to string

        for (int i = 2; i < 12; ++i)
        {
          if (noChange[i] && (i != catIndex))
          {
            s += hdrMach +
                 Globals.leftJustifyString(getParamDescription(i), colwidth) +
                 " = " + param[i] + NL;
          }
        }
        
        // write out differences
  
        ArrayList<Integer> noChgList = new ArrayList<Integer>();
        for (int i = 2; i < 12; ++i)
        {
          if (!noChange[i]) noChgList.add(i);
        }

        // output machine entries

        s += hdrMach + "Machines:" + NL;
        for (Machine mach: machineSet)
        {
          s += hdrMach + "    " + mach.getName();
          if (noChgList.size() > 0)
          {
            for (int i = 0; i < noChgList.size(); ++i)
            {
              int k = noChgList.get(i);
              if (i == 0)
                s += " (";
              else
                s += ", ";
              s += getParamDescription(k) + ": " +
                   getParam(mach,  k);
            }
            s += ")";
          }
          s += NL;
        }
        s += NL;
      } // end for (String key: valSet)
    } // end for (Map.Entry<Integer, HashSet<String>> e: sortedMap.entrySet())

    // return string

    return s;
  }
}
