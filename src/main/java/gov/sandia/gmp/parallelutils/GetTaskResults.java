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


import java.util.Date;
import java.util.LinkedList;

/**
 * A runnable designed to retrieve all returned tasks immediately upon
 * return form the ParallelBroker object so that their time stamp can
 * be set. This ensures their is no bias in the parallel transfer
 * overhead time estimate. This class simply starts running in a new
 * thread and continually updates a list of returned task results
 * (LSINVTaskResult). The list is updated by polling the ParallelBroker
 * and returning any task results that it may have, time stamping the
 * result, and storing it in the list. This thread continues until
 * shutdown by the client after all tasks have been processed and
 * received.
 * 
 * @author jrhipp
 *
 */
public class GetTaskResults<T extends ParallelResult> implements Runnable
{
	/**
	 * The parallel broker from which task results are returned.
	 */
  private ParallelBroker aParallelBrkr   = null;

	/**
	 * Number of task results returned
	 */
  private int            taskCount       = 0;

  /**
   * The list of all returned results from the ParallelBroker.
   */
  private LinkedList<ParallelResult>  returnedResults = new LinkedList<ParallelResult>();

  /**
   * The run condition. Defaults to true until stopped by the client using the function
   * stop().
   */
  private boolean        runCondition    = true;

  /**
   * The runnable thread for this class.
   */
  private Thread         runThread       = null;

  /**
   * Default constructor. Creates a new thread and starts it. 
   */
  public GetTaskResults(ParallelBroker pb)
  {
  	aParallelBrkr = pb;
    runThread = new Thread(this, "GetResults");
    runThread.start();
  }

  /**
   * The run method called by the new thread. It simply polls the
   * ParallelBroker to see if any new task results have been
   * returned. If any are found they are time stamped and added to
   * the internal list (returnedResults).
   */
  @Override
  public void run()
  {
    // clear the list and enter the perpetual while loop ... this loop
    // exits when the boolean runCondition is set to false by calling
    // function stop() below.

    returnedResults.clear();
    while (runCondition)
    {
      // see if a new task result is available

			ParallelResult tskrslt = aParallelBrkr.getResult();

      if (tskrslt != null)
      {
        // found task result ... add it to the list

        tskrslt.setTaskReturnTime((new Date()).getTime());

        synchronized(this)
        {
        	++taskCount;
          returnedResults.add(tskrslt);
        }
      }
    }
  }

  /**
   * Stop this thread.
   */
	public synchronized void stop()
	{
	  runCondition = false;
	}
	
	/**
	 * Get the next available returned task result. Returns null if not tasks
	 * are present.
	 * 
	 * @return The next available returned task result.
	 */
	@SuppressWarnings("unchecked")
	public synchronized T getNextResult()
	{
    if (returnedResults.size() > 0)
    	return (T) returnedResults.pollFirst();
    else
    	return null;
	}

	/**
	 * Returns the current stored task count.
	 * 
	 * @return The current stored task count.
	 */
	public synchronized int getCurrentCount()
	{
		return returnedResults.size();
	}

	/**
	 * Returns the total task count added to this container over its lifetime.
	 * 
	 * @return The total task count added to this container over its lifetime.
	 */
	public synchronized int getTotalTaskCount()
	{
		return taskCount;
	}
}
