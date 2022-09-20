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
package gov.sandia.gmp.parallelutils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * The Sequential ParallelBroker turns off all concurrent or distributed
 * parallelism and submits and processes tasks in the order called.
 * This mode is generally useful for debugging or in cases where
 * concurrency or distributed options are not available.
 *
 * @author jrhipp
 *
 */
public class ParallelBrokerSequential extends ParallelBroker
implements CommunicationsManager
{
  private LinkedList<ParallelTask> aTasks = new LinkedList<ParallelTask>();
  private boolean connected = false;

  /**
   * Default constructor.
   */
  public ParallelBrokerSequential()
  {
    super();
  }
  
  @Override
  public ExecutorService getExecutorService() { return null; }
  
  @Override
  public int getHostCount() { return 1; }

  /**
   * Returns the type name of the ParallelBroker.
   * 
   * @return The type name of the ParallelBroker.
   */
  @Override
  public String getName()
  {
    return "Sequential";
  }

  /**
   * Returns the available processor count (this is always 1 for a
   * SequentialBroker).
   * 
   * @return The available processor count.
   */
  @Override
  public int getProcessorCount()
  {
    return 1;
  }
  
  /**
   * Returns the available processor count, by host (always 1 entry, with a value of 1)
   * 
   * @return The available processor count, by host.
   */
  @Override
  public Map<String, Integer> getProcessorCountByHost() {
	  Map<String, Integer> map = new HashMap<String,Integer>();
	  try {
		  map.put((InetAddress.getLocalHost()).getHostName(),1);
	  } catch (UnknownHostException e) {}
	  return map;
  }
  
  @Override
  public int getProcessorCountEstimate() { return 1; }

  /**
   * Return the next available result or null if none are pending. (Note: In
   * sequential mode only one result at a time is ever returned).
   * 
   * @return The next available result.
   */
  @Override
  public ParallelResult getResult()
  {
    // return null if no results are available or pending

    if (isEmpty()) return null;

    // remove the next task and calculate it ... return the result

    ParallelTask tsk = aTasks.remove();
    tsk.setLocalThreadCount(1);
    tsk.run();
    tsk.setLocalThreadCount(null);
    return tsk.getResultObject();
  }

  /**
   * Return the next available result or null if none are pending. (Note: In
   * sequential mode only one result at a time is ever returned).
   * 
   * @return The next available result.
   */
  @Override
  public ParallelResult getResultWait()
  {
    return getResult();
  }

  /**
   * Return the next available result or null if none are pending. (Note: In
   * sequential mode only one result at a time is ever returned).
   * 
   * @return The next available result.
   */
  @Override
  public List<ParallelResult> getResults()
  {
    // return null if no results are available or pending
    
    if (isEmpty()) return null;

    // create a list to put the task in

    ArrayList<ParallelResult> results =
              new ArrayList<ParallelResult>(1);

    // remove the next task and calculate it ... add it to the result and
    // return the result

    ParallelTask tsk = aTasks.remove();
    tsk.setLocalThreadCount(1);
    tsk.run();
    tsk.setLocalThreadCount(null);
    results.add(tsk.getResultObject());
    return results;
  }

  /**
   * Return the next available result or null if none are pending. (Note: In
   * sequential mode only one result at a time is ever returned).
   * 
   * @return The next available result.
   */
  @Override
  public List<ParallelResult> getResultsWait()
  {
    return getResults();
  }

  /**
   * Returns true if no more task results are available.
   * 
   * @return True if no more task results are available.
   */
  @Override
  public boolean isEmpty()
  {
    return (size() == 0);
  }

  /**
   * Returns the number of task results that are currently available.
   * 
   * @return The number of task results that are currently available.
   */
  @Override
  public int size()
  {
    return aTasks.size();
  }

  /**
   * Submit a list of tasks for processing. This function returns immediately.
   * 
   * @param tsks The list of all ParallelTasks to be submitted for
   *             processing.
   */
  @Override
  public void submit(List<? extends ParallelTask> tsks)
  {
    for(ParallelTask t : tsks) submit(t);
  }

  /**
   * Submit a single task for processing. This function returns immediately.
   * 
   * @param tsk The ParallelTask to be submitted for processing.
   */
  @Override
  public void submit(ParallelTask tsk)
  {
	if(ParallelTask.getCommunicationsManager() == null){
		synchronized(ParallelTask.class){
			if(ParallelTask.getCommunicationsManager() == null)
				ParallelTask.setCommunicationsManager(this);
		}
	}
	
	if(!connected) {
	  connected = true;
	  super.fireConnected();
	}
	
    aTasks.add(tsk);
  }

  @Override
  public void sendToClient(Object message){
	  super.setMessageReceived(message);
  }
  
  @Override
  public void submitStaticCleanupTask(final StaticCleanupTask tsk,
		  boolean waitFor){
    if(!connected) {
      connected = true;
      super.fireConnected();
    }
    
    if(waitFor) tsk.run();
    else new Thread(tsk).start();
  }

  @Override
  public void submitBatched(ParallelTask tsk) { submit(tsk); }

  @Override
  public void purgeBatch() {
    if(!connected) {
      connected = true;
      super.fireConnected();
    }
  }
}
