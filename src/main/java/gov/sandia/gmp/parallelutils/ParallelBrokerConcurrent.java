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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The concurrent ParallelBroker uses the Java completion service and a ThreadPoolExecutor to submit
 * tasks as threads on the current executing platform. The thread pool uses 2 times the number of
 * available processors defined on the current machine.
 * 
 * @author jrhipp
 *
 */
public class ParallelBrokerConcurrent extends ParallelBroker {
  /**
   * Number of processor threads to use (number of available processors, by default).
   */
  private int aNProcessors = Runtime.getRuntime().availableProcessors();

  /**
   * The local thread pool on which the processes will execute.
   */
  private ThreadPoolExecutor aThreadPool =
      (ThreadPoolExecutor) Executors.newFixedThreadPool(aNProcessors);

  /**
   * The completion service results queue used to save the results as they return from the thread
   * pool.
   */
  private CompletionService<ParallelResult> aQueue =
      new ExecutorCompletionService<ParallelResult>(aThreadPool);

  /**
   * The current number of tasks for which results are or will be available. This number is simply
   * the total number of submitted tasks minus those that have been retrieved with a call to
   * getResults().
   */
  private AtomicInteger aTaskCount = new AtomicInteger(0);

  /**
   * Used to limit the number of tasks that can be submitted simultaneously. This prevents too many
   * tasks from being created at a time during a tight for loop where <code>submitBatched()</code>
   * is called with instances of newly-created tasks (prevents OOMEs).
   */
  private Semaphore batchLimiter = new Semaphore(1);
  private volatile boolean startedBatching = false;
  private boolean connected = false;

  /**
   * A simple inner class required to implement the concurrent Callable interface. This object wraps
   * the BenderResultBundle call with a try/catch exception block.
   * 
   * @author jrhipp
   *
   */
  final class ConcurrentTask implements Callable<ParallelResult>, CommunicationsManager {
    /**
     * The task to be executed concurrently
     */
    ParallelTask pt = null;

    /**
     * Flag to release a permit from the limiter, if batched, upon completion.
     */
    boolean batched = false;

    /**
     * Standard constructor that saves the input task.
     * 
     * @param pt - The input task to be executed concurrently.
     */
    public ConcurrentTask(ParallelTask pt, boolean batched) {
      this.pt = pt;
      this.batched = batched;
    }

    /**
     * Execute the task saved in bob. This function will execute the task in a concurrent fashion by
     * calling the tasks run() function.
     */
    @Override
    public ParallelResult call() throws Exception {
      // call the task run function, retrieve and return the result

      try {
        if (ParallelTask.getCommunicationsManager() == null) {
          synchronized (ParallelTask.class) {
            if (ParallelTask.getCommunicationsManager() == null)
              ParallelTask.setCommunicationsManager(this);
          }
        }

        pt.setLocalThreadCount(aNProcessors);
        pt.run();
        pt.setLocalThreadCount(null);
        if (batched)
          batchLimiter.release();
        return pt.getResultObject();
      } catch (Exception ex) {
        // return null if an error occurs

        ex.printStackTrace();
        return (ParallelResult) null;
      }
    }

    @Override
    public void sendToClient(Object message) throws IOException {
      ParallelBrokerConcurrent.this.setMessageReceived(message);
    }
  }

  /**
   * Default constructor.
   */
  public ParallelBrokerConcurrent() {
    super();
  }

  /**
   * Sets the processor count to the input value instead of the one returned by
   * Runtime.getRuntime().availableProcessors().
   * 
   * @param procCount The concurrent processor count to use.
   * 
   */
  @Override
  public void setProcessorCount(int procCount) {
    if (procCount > 0) {
      aNProcessors = procCount;
      aThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(aNProcessors);
      aQueue = new ExecutorCompletionService<ParallelResult>(aThreadPool);
    }
    // System.out.println("Number of processors to be used: " + aNProcessors);
  }

  @Override
  public void close() {
    aThreadPool.shutdown();
  }

  @Override
  public ExecutorService getExecutorService() {
    return aThreadPool;
  }

  @Override
  public int getHostCount() {
    return 1;
  }

  /**
   * Returns the type name of the ParallelBroker.
   * 
   * @return The type name of the ParallelBroker.
   */
  @Override
  public String getName() {
    return "Concurrent";
  }

  /**
   * Returns the available processor count.
   * 
   * @return The available processor count.
   */
  @Override
  public int getProcessorCount() {
    return aNProcessors;
  }

  /**
   * Returns the available processor count, by host.
   * 
   * @return The available processor count, by host.
   */
  @Override
  public Map<String, Integer> getProcessorCountByHost() {
    Map<String, Integer> map = new HashMap<String, Integer>();
    try {
      map.put((InetAddress.getLocalHost()).getHostName(), aNProcessors);
    } catch (UnknownHostException e) {
    }
    return map;
  }

  @Override
  public int getProcessorCountEstimate() {
    return aNProcessors;
  }

  /**
   * Return the next available result or null if none are available or ever will be.
   * 
   * @return The next available result.
   */
  @Override
  public ParallelResult getResult() {
    // return null if no tasks are pending

    if (aTaskCount.get() == 0)
      return null;

    // get the most recent Future if any. If none are found return null

    Future<ParallelResult> f = aQueue.poll();
    if (f != null) {
      // found a future ... decrement the task count and return the result.
      // if an error occurs return null.

      try {
        aTaskCount.decrementAndGet();
        // System.out.println("Remaining Tasks " + aTaskCount);
        return f.get();
      } catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
    } else
      // none are available ... return null

      return null;
  }

  /**
   * Return the next available result or null if none are available or ever will be. This function
   * waits (blocks) until a task is available before returning.
   * 
   * @return The next available result.
   */
  @Override
  public ParallelResult getResultWait() {
    // return null if no tasks are pending

    if (!isForceWaitEnabled() && aTaskCount.get() == 0)
      return null;

    // get another task

    try {
      ParallelResult pr = aQueue.take().get();
      aTaskCount.decrementAndGet();
      // System.out.println("Remaining Tasks " + aTaskCount);
      return pr;
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Return all available results or null if none are available or ever will be.
   * 
   * @return List of all available results.
   */
  @Override
  public List<ParallelResult> getResults() {
    // return null if no tasks are pending

    if (aTaskCount.get() == 0)
      return null;

    // create a results vector to add any entries into

    ArrayList<ParallelResult> results = new ArrayList<ParallelResult>(aTaskCount.get() + 1);

    // loop over all returned results

    Future<ParallelResult> f;
    while ((f = aQueue.poll()) != null) {
      // found a future ... decrement the task count and add the result
      // into the list. if an error occurs return null.

      try {
        aTaskCount.decrementAndGet();
        results.add(f.get());
      } catch (Exception ex) {
        ex.printStackTrace();
        return null;
      }
    }

    // if no results are available return null ... otherwise return the
    // results

    if (results.size() == 0)
      return null;
    else
      return results;
  }

  /**
   * Return all available results or null if none are available or ever will be. This function waits
   * (blocks) until tasks are available before returning.
   * 
   * @return List of all available results.
   */
  @Override
  public List<ParallelResult> getResultsWait() {
    // return null if no tasks are pending

    if (aTaskCount.get() == 0)
      return null;

    // wait for results and return

    List<ParallelResult> result;
    while ((result = getResults()) == null);
    return result;
  }

  /**
   * Returns true if no more task results are available.
   * 
   * @return True if no more task results are available.
   */
  @Override
  public boolean isEmpty() {
    return (aTaskCount.get() == 0);
  }

  private void resetBatchLimiter() {
    // Simulates batching behavior of distributed versions ParallelBroker:
    batchLimiter.drainPermits();
    batchLimiter.release(Math.max(1, getBatchSize() * getMaxBatches()));
  }

  @Override
  public void setBatchSize(int s) {
    if (startedBatching)
      return;
    super.setBatchSize(s);
    resetBatchLimiter();
  }

  @Override
  public void setMaxBatches(int m) {
    if (startedBatching)
      return;
    super.setMaxBatches(m);
    resetBatchLimiter();
  }

  /**
   * Returns the number of task results that are currently available.
   * 
   * @return The number of task results that are currently available.
   */
  @Override
  public int size() {
    return aTaskCount.get();
  }

  /**
   * Submit a list of tasks for concurrent parallel processing. This function returns immediately.
   * 
   * @param tsks The list of all ParallelTasks to be submitted for processing.
   */
  @Override
  public void submit(List<? extends ParallelTask> tsks) {
    // loop over each task and submit

    for (int i = 0; i < tsks.size(); ++i)
      submit(tsks.get(i));
  }

  /**
   * Submit a single task for concurrent parallel processing. This function returns immediately.
   * 
   * @param tsk The task to be processed.
   */
  @Override
  public void submit(ParallelTask tsk) {
    if (!connected) {
      connected = true;
      super.fireConnected();
    }

    // increment task count, wrap the task in a Callable, and submit it to
    // the completion service for processing.

    aTaskCount.incrementAndGet();
    ConcurrentTask ct = new ConcurrentTask(tsk, false);
    aQueue.submit(ct);
  }

  @Override
  public void submitStaticCleanupTask(StaticCleanupTask tsk, boolean waitFor) {
    if (!connected) {
      connected = true;
      super.fireConnected();
    }

    Future<?> f = aThreadPool.submit(tsk);
    if (waitFor) {
      try {
        f.get();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void submitBatched(final ParallelTask tsk) {
    if (!connected) {
      connected = true;
      super.fireConnected();
    }

    startedBatching = true;

    try {
      batchLimiter.acquire();
      aTaskCount.incrementAndGet();
      aQueue.submit(new ConcurrentTask(tsk, true));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void purgeBatch() {
    if (!connected) {
      connected = true;
      super.fireConnected();
    }

    // No effect as tasks are not actually batched, but submitted immediately
  }
}
