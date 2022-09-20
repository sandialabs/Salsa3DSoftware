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

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The base class ParallelBroker object used to handle parallel task
 * submissions in an arbitrary manner. Five different parallel modes are
 * supported including: "sequential", "concurrent", "distributed",
 * "distributed_cuda_sp", and "distributed_cuda_dp". The "sequential" mode is
 * used for debugging purposes only or in rare cases where only one machine
 * with one processor is available for processing. The concurrent mode is used
 * to take advantage of several processors on a single machine. The last three
 * use the Java Parallel Processing Framework (JPPF) to handle many separate
 * nodes (processors) on many distributed machines. A static factory method
 * (create) is defined that allows a user to access any of the above brokers.
 * 
 * <P> Several functions are defined to allow the user to submit tasks, get
 * results, and check on the relative state of the broker (size, empty,
 * processor count, broker type, etc.). All submitted tasks must use the
 * interface ParallelTask. All returned results must use the interface
 * ParallelResults. Tasks must be implemented by the user to perform the
 * specific requirements of the application. Results must be defined to
 * contain the relevant results from each task for return to the application.
 * 
 * <p> Nine primary functions are supported including:
 * 
 * <ul>
 * <li> public abstract void submit(List<? extends ParallelTask> tsks); </li>
 * <li> public abstract void submit(ParallelTask tsk); </li>
 * <li> public abstract List<ParallelResult> getResults(); </li>
 * <li> public abstract List<ParallelResult> getResultsWait(); </li>
 * <li> public abstract ParallelResult getResult(); </li>
 * <li> public abstract ParallelResult getResultWait(); </li>
 * <li> public abstract int size(); </li>
 * <li> public abstract boolean isEmpty(); </li>
 * <li> public abstract void close(); </li>
 * </ul>
 * 
 * <p> The two submit functions submit an array of tasks or a single entry
 * for processing using one of the modes above. These functions are non-
 * blocking and return immediately.
 * 
 * <p> There are four result retrieval functions getResult() and
 * getResultWait() return a single result submitted by one of the first
 * two functions or null if no results are available. The returned result is
 * not necessarily in the order that it was submitted. The functions
 * getResults() and getResultsWait() return all currently available results.
 * 
 * <p> The "Wait" version will block and wait for a result if any are
 * pending. The non "Wait" version return null immediately if no results are
 * available. All four functions return null immediately if their are no
 * available results and none pending.
 * 
 * <p> The function size returns the number of results that are or will be
 * available once all results from the submit functions have been processed.
 * The function isEmpty() returns true if no results remain (none are
 * available and none are pending).
 * 
 * <p> The function close cleans up resources used by the CONCURRENT and
 * DISTRIBUTED modes. It performs no action for the SEQUENTIAL mode.
 * 
 * @author jrhipp, bjlawry
 */
public abstract class ParallelBroker
{
  /** 
   * <p>Used to set which operating systems are allowed to run tasks for
   * calling application.  If set to null (done by default), nodes running on
   * any operating system will be allowed to run tasks for the calling
   * application.
   * 
   * <p><b>Note: </b>this call only applies to distributed-mode
   * ParallelBrokers.
   */
  protected String preferredOperatingSystems;
  private LinkedBlockingQueue<Object> receivedMessages;
  private LinkedList<MessageListener> messageListeners;
  private LinkedList<ConnectionListener> connectionListeners;
  private int batchSize, maxBatches;
  private boolean forceWait;
	
  public static enum ParallelMode{
  	SEQUENTIAL,CONCURRENT,DISTRIBUTED,DISTRIBUTED_CUDA_SP,SEQUENTIAL_FABRIC,
  	DISTRIBUTED_CUDA_DP,CONCURRENT_FABRIC, DISTRIBUTED_FABRIC;
  }
  
  //Constructors: -------------------------------------------------------------
	
  /** Default constructor. */
  protected ParallelBroker(){
	  preferredOperatingSystems = null;
	  receivedMessages = new LinkedBlockingQueue<Object>();
	  messageListeners = new LinkedList<MessageListener>();
	  connectionListeners = new LinkedList<ConnectionListener>();
	  batchSize = 16;
	  maxBatches = 64;
	  forceWait = false;
  }
  
  //Factory methods: ----------------------------------------------------------
  
  /**
   * Creates a new ParallelBroker of kind = parallelMode
   * The result is returned as a base class (ParallelBroker) reference.
   * 
   * @return The new ParallelBroker object.  If the developer selects
   * DISTRIBUTED mode, both NRM and JPPF libraries must be on the classpath.
   * If they are not, then an error will be printed to the screen and either
   * Sequential (processors == 1) or Concurrent (processors > 1) mode will be
   * used instead.
   */
  public static ParallelBroker create(ParallelMode mode)
  {
    try {
    	Class<?> c = null;
    	boolean val = true;
    	
    	//Definitely not elegant, but using reflections is the only way to
    	//handle errors associated with not being able to load classes.  We
    	//can't directly reference the distributed version of ParallelBroker
    	//because doing so forces the class loader to load those classes right
    	//after loading ParallelBroker, which causes unrecoverable problems.
    	switch(mode){
    	case SEQUENTIAL: return new ParallelBrokerSequential();
    	case CONCURRENT: return new ParallelBrokerConcurrent();
    	case DISTRIBUTED:
    		c = Class.forName(
    			"gov.sandia.gmp.parallelutils.ParallelBrokerDistributedCPU");
    		return (ParallelBroker)c.getDeclaredConstructor().newInstance();
    	case DISTRIBUTED_CUDA_SP:
    		val = false; //CUDA_SP/DP use the same ParallelBroker class
    	case DISTRIBUTED_CUDA_DP:
    		c = Class.forName(
    			"gov.sandia.gmp.parallelutils.ParallelBrokerDistributedCUDA");
    		Constructor<?> cons = c.getConstructor(Boolean.class);
        	return (ParallelBroker)cons.newInstance(val);
    	case SEQUENTIAL_FABRIC:
    		c = Class.forName("gov.sandia.gmp.parallelutils.fabric." +
    				"FabricParallelBrokerSequential");
    		cons = c.getConstructor((Class<?>[])null);
    		return (ParallelBroker)cons.newInstance((Object[])null);
    	case DISTRIBUTED_FABRIC:
    		c = Class.forName("gov.sandia.gmp.parallelutils.fabric." +
    				"FabricParallelBrokerDistributed");
    		cons = c.getConstructor((Class<?>[])null);
    		return (ParallelBroker)cons.newInstance((Object[])null);  		
    	case CONCURRENT_FABRIC:
    		c = Class.forName("gov.sandia.gmp.parallelutils.fabric." +
    				"FabricParallelBrokerConcurrent");
    		cons = c.getConstructor((Class<?>[])null);
    		return (ParallelBroker)cons.newInstance((Object[])null);
    	default:
    		ParallelBroker broker = null;
    		if(Runtime.getRuntime().availableProcessors() > 1)
    			broker = new ParallelBrokerSequential();
    		else broker = new ParallelBrokerConcurrent();
    		
    		System.err.println("Invalid ParallelMode ("+mode+"), reverting to "
    				+broker.getName());
    		
    		return broker;
    	}
	} catch(Throwable t) { return handleDistributedInitError(mode); }
  }
  
  /**
   * Creates a new ParallelBroker of kind = "type". The value of "type"
   * must be "sequential", "concurrent", "distributed_fabric" (case 
   * insensitive). The result is returned as a base class (ParallelBroker)
   * reference.
   * 
   * @return The new ParallelBroker object.
   */
  public static ParallelBroker create(String type){
	return create(ParallelMode.valueOf(type.toUpperCase()));
  }
  
  //Private static helper methods: --------------------------------------------
  
  /**
   * This method is used to handle problems in instantiating distributed
   * versions of ParallelBroker.
   * @param m ParallelMode that failed to be instantiated
   * @return CONCURRENT- or SEQUENTIAL-mode ParallelBroker, depending on
   * whether the machine has more than one processor or not.
   */
  private static ParallelBroker handleDistributedInitError(ParallelMode m){
    System.err.print("Selected mode "+m+" unavailable, switching to ");
    if(Runtime.getRuntime().availableProcessors() > 1){
	  System.err.println(ParallelMode.CONCURRENT+" mode.");
	  return new ParallelBrokerConcurrent();
    }

    System.err.println(ParallelMode.SEQUENTIAL+" mode.");
    return new ParallelBrokerSequential();
  }
  
  //Protected helper methods: -------------------------------------------------
  
  protected void fireConnected() {
    synchronized(connectionListeners) {
      for(ConnectionListener c : connectionListeners) c.connected();
    }
  }
  
  protected void setMessageReceived(Object message){
	  try {
		receivedMessages.put(message);
		synchronized(messageListeners){
			for(MessageListener ml : messageListeners)
				ml.messageReceived(message);
		}
	} catch (InterruptedException e) {
		//This should never happen because the queue is unbounded:
		e.printStackTrace();
	}
  }
  
  //Public instance methods: --------------------------------------------------
  
  public void addConnectionListener(ConnectionListener cl) {
    synchronized(connectionListeners) {
      connectionListeners.addLast(cl);
    }
  }
  
  public void addMessageListener(MessageListener ml){
	  synchronized(messageListeners){
		  messageListeners.addLast(ml);
	  }
  }
  
  /**
   * @return null if this broker is sequential, otherwise a reference to an executor service that
   * represents the threads being used under the hood.
   */
  public abstract ExecutorService getExecutorService();
  
  /**
   * @return the number of tasks that must be cached prior to auto-submitting
   * the next batch of tasks to the task driver.
   */
  public int getBatchSize() { return batchSize; }
  
  /**
   * @return the maximum number of batches allowed to be "in process" at any
   * given time.
   */
  public int getMaxBatches() { return maxBatches; }
  
  /**
   * @return true if forced waiting for results is enabled (set to
   * <code>false</code> by default).
   */
  public boolean isForceWaitEnabled() { return forceWait; }
  
  public void removeConnectionListener(ConnectionListener cl) {
    synchronized(connectionListeners) {
      connectionListeners.remove(cl);
    }
  }
  
  public void removeMessageListener(MessageListener ml){
	  synchronized(messageListeners){
		  messageListeners.remove(ml);
	  }
  }
  
  /**
   * Sets the number of tasks to be cached internally prior to submission of a
   * batch of tasks during calls to <code>submitBatched(tsk)</code>.
   * @param s
   */
  public void setBatchSize(int s) { batchSize = s; }
  
  /**
   * Toggles whether calls to getResultWait() should always wait, even if no
   * further tasks are known to be submitted at the time of the call. This
   * should be set to true whenever the calling application intends to use
   * <code>submitBatched(ParallelTask)</code>, prior to making batched calls.
   * This call has no effect in Sequential mode.
   * @param force toggles whether forced waiting on results is enabled
   */
  public void setForceWaitEnabled(boolean force) { forceWait = force; }
  
  /**
   * Sets the maximum number of batches allowed to be "in process" at any given
   * time during calls to <code>submitBatched(task)</code>
   * @param m
   */
  public void setMaxBatches(int m) { maxBatches = m ; }
  
  /**
   * <p>Takes a comma-separated String, defining operating system names (case-
   * insensitive) that the underlying distributed task management system is
   * allowed to start nodes on.  For example, the String "Windows 7, linux"
   * will allow nodes to run on any machine whose lower-cased OS name starts
   * with "windows 7" or "linux".   In order for this method to have any
   * effect, it must be called prior to making the first call to submit(),
   * preferably immediately after calling ParallelBroker.create(mode).
   * 
   * <p><b>Warning: </b>
   * Depending on what resources are available on the distributed system,
   * limiting the operating systems that your application may use can cause
   * some undesirable effects.  If another application is currently utilizing
   * resources on the network, limiting the operating systems may increase the
   * amount of time your application must wait for resources.  This is due to
   * the randomized nature of how often nodes check back with the server for
   * reassignment.  In the case that no resources exist with the desired
   * operating system(s) installed, the calling application may wait
   * indefinitely.
   * 
   * <p><b>Note: </b>
   * If the driver's default configuration file contains a listing of preferred
   * operating system names under the property defined in
   * Properties.NRM_PREFERRED_OPERATING_SYSTEMS, this function call will
   * override the default list in the driver's configuration file.
   * 
   * @param osNames
   */
  public void setPreferredOperatingSystems(String osNames){
	  preferredOperatingSystems = osNames;
  }
  
  /**
   * Sets any relevant properties found in the input Properties object
   * for this ParallelBroker. Relevant properties include the following
   * 
   *   clientThreadPoolSize
   *   driverMaxMemory
   *   nodeMaxMemory
   *   taskTimout
   *   preferredOperatingSystems
   * 
   * See each of their corresponding set functions for more information
   * about valid settings. If the property is not defined in the input
   * Properties file then the property is not set.
   * 
   * @param prop The properties file from which relevant ParallelBroker
   *             properties will be set.
   */
  public void setProperties(Properties prop)
  {
    String p;
    long v;
    int i;

    p = prop.getProperty("clientThreadPoolSize", "-1").trim();
    v = Long.valueOf(p);
    if (v != -1) setClientThreadPoolSize(v);

    p = prop.getProperty("driverMaxMemory", "-1").trim();
    v = Long.valueOf(p);
    if (v != -1) setDriverMaxMemory(v);

    p = prop.getProperty("nodeMaxMemory", "-1").trim();
    v = Long.valueOf(p);
    if (v != -1) setNodeMaxMemory(v);

    p = prop.getProperty("taskTimeout", "-1").trim();
    v = Long.valueOf(p);
    if (v != -1) setTaskTimeout(v);
    
    p = prop.getProperty("maxProcessors", "-1").trim();
    i = Integer.valueOf(p);
    if (i != -1) setProcessorCount(i);

    p = prop.getProperty("preferredOperatingSystems", "").trim();
    if (!p.equals("")) setPreferredOperatingSystems(p);
    
    // --- Fabric-specific properties ---
    
    p = prop.getProperty("fabricApplicationName", "").trim();
    if (!p.equals("")) setFabricApplicationName(p);
    
    p = prop.getProperty("fabricRequiredOperatingSystems", "").trim();
    if (!p.equals("")) setFabricRequiredOperatingSystems(p.split("\\s+"));
    
    p = prop.getProperty("fabricLibraryPath", "").trim();
    if (!p.equals("")) setFabricLibraryPath(p);
    
    p = prop.getProperty("fabricRelativeClasspath", "").trim();
    if (!p.equals("")) setFabricRelativeClasspath(p);
    
    p = prop.getProperty("fabricNodeVMArgs", "").trim();
    if (!p.equals("")) setFabricNodeVMArgs(p);

    p = prop.getProperty("fabricMaxThreadsPerNode", "1").trim();
    i = Integer.valueOf(p);
    if (i != -1) setFabricMaxThreadsPerNode(i);
    
    p = prop.getProperty("fabricMaxThreadQueueSize", "-1").trim();
    i = Integer.valueOf(p);
    if (i != -1) setFabricMaxThreadQueueSize(i);
    
    p = prop.getProperty("fabricSocketBufferSize", "65536").trim(); // 64K is what JPPF uses for receive buffer size
    i = Integer.valueOf(p);
    if (i != -1) setFabricSocketBufferSize(i);
    
    p = prop.getProperty("fabricBaselineNodeMemory", "-1").trim();
    v = Long.valueOf(p);
    if (v != -1) setFabricBaselineNodeMemory(v);
    
  }

  //Abstract methods: ---------------------------------------------------------
  
  /**
   * Primary function to execute the input list of tasks. This function
   * returns immediately (non-blocking).
   * 
   * @param tsks Array list of input tasks to be executed.
   */
  public abstract void submit(List<? extends ParallelTask> tsks);

  /**
   * Primary function to execute the input list of tasks. This function
   * returns immediately (non-blocking).
   * 
   * @param tsk Input task to be executed.
   */
  public abstract void submit(ParallelTask tsk);
  
  /**
   * Asynchronously submits the specified task for execution. Tasks submitted
   * via this method are cached internally until the cache size equals the
   * number returned by <code>getBatchSize()</code>. When this happens, the
   * whole batch is submitted to the task driver for execution.
   * <br>
   * Only <code>getBatchSize()*getMaxBatches()</code> tasks are allowed to be
   * submitted at a time. When this limit is reached, the next call to
   * <code>submitBatched(tsk)</code> will block until a batch of results are
   * returned by the task driver.
   * <br>
   * Due to the nature of how batched submits work, it's best to submit batched
   * one-by-one in in a tight loop, then call <code>purgeBatch()</code> to
   * force all remaining cached tasks to be submitted.
   * <br>
   * Results of batched tasks can be obtained using any of the public
   * <code>getResult*()</code> functions.
   * <br>
   * <b>Note:</b> in Sequential mode, this method behaves in exactly the same
   * manner as the normal <code>submit(tsk)</code> method.
   * @param tsk task to be queued for batched submission
   */
  public abstract void submitBatched(ParallelTask tsk);

  /**
   * Forces all remaining tasks cached by <code>submitBatched(tsk)</code> to be
   * immediately sent to the driver for execution. This method should always be
   * called after the final call to <code>submitBatched(tsk)</code> has been
   * made.
   * <br>
   * <b>Note:</b> in Sequential mode, this method does nothing.
   */
  public abstract void purgeBatch();
  
  /**
   * Duplicates the specified task and sends one for every remote JVM on the
   * ParallelUtils network.  The task's run method may be used to clean up
   * static resources that may be shared across threads and is guaranteed to
   * run only once on each remote JVM.
   * @param tsk
   */
  public abstract void submitStaticCleanupTask(StaticCleanupTask tsk,
		  boolean waitFor);

  /**
   * Returns a result object from some previous call to submit. If the current
   * mode is CONCURRENCY or DISTRIBUTED, then the result may not be from the
   * last call to submit and may even be null indicating no task was ever
   * submitted or no tasks have yet completed processing. The user is
   * responsible for aligning returned concurrent results with their
   * corresponding input task.
   *  
   * @return The most recent result on the queue or null.
   */
  public abstract ParallelResult getResult();

  /**
   * Same as getResult() except this function blocks (waits) for a result to
   * be completed if none are currently available. If all results have been
   * returned this function returns null.
   * 
   * <p> If the mode is SEQUENTIAL this function will evaluate the next
   * submission before returning with its matching result.
   * 
   * @return The most recent result or null if no further results are
   *         available.
   */
  public abstract ParallelResult getResultWait();

  /**
   * Returns all available result objects currently available from a set of
   * previous calls to submit. If the current mode is CONCURRENCY or
   * DISTRIBUTED, then the results may not be from the last calls to submit
   * and may even be null indicating no task was ever submitted or no tasks
   * have yet completed processing. The user is responsible for aligning
   * returned concurrent results with their corresponding input task.
   *  
   * @return The set of all available results on the queue or null if none
   *         are available.
   */
  public abstract List<ParallelResult> getResults();

  /**
   * Same as getResults() except this function blocks (waits) for one or more
   * results to complete if none are currently available. If all results have
   * been returned this function returns null.
   * 
   * <p> If the mode is SEQUENTIAL this function will evaluate the next
   * submission before returning with its matching result (always one entry).
   * 
   * @return The most recent set of results or null if no further results are
   *         available.
   */
  public abstract List<ParallelResult> getResultsWait();

  /**
   * Returns the number of results that are or will be available for access.
   * 
   * @return The number of results that are or will be available for access.
   */
  public abstract int size();

  /**
   * Returns true if there are no more results.
   * 
   * @return True if there are no more results.
   */
  public abstract boolean isEmpty();
  
  /**
   * @return the number of host machines making threads available to this ParallelBroker
   */
  public abstract int getHostCount();
  
  /**
   * Returns the number of parallel processors currently in use.
   * 
   * @return The number of parallel processors currently in use.
   */
  public abstract int getProcessorCount();
  
  /**
   * Returns a map of the number of parallel processors currently in use, by host.
   * 
   * @return Map of the number of parallel processors currently in use, by host.
   */
  public abstract Map<String,Integer> getProcessorCountByHost();
  
  /**
   * Returns a best-guess estimate on the number of processors that will be
   * used on the next call to submit().  For Sequential and Concurrent
   * ParallelBrokers, this will always return 1 and 2*Runtime.getRuntime().
   * availableProcessors(), respectively.  For Distributed ParallelBrokers,
   * this method will return 2*(# of available cores)/(# of running apps + 1).
   * Depending on each machine's processor performance rating, this number may
   * be off by a small percentage.
   * 
   * @return estimate of how many processors will be used on the next call to
   * submit(task-list).
   */
  public abstract int getProcessorCountEstimate();

  /**
   * Returns the name string of the ParallelBroker type.
   * 
   * @return The name string of the ParallelBroker type.
   */
  public abstract String getName();
  
  //Adapter methods (non-abstract, but also non-functional): ------------------
  
  /**
   * Adds a data object (obj) that can be used by all tasks submitted by
   * the same broker. The data object is associated with the string key
   * so that it can be accessed within a task when it is running on a
   * remote node.
   * 
   * @param key The key string associated with the data object obj.
   * @param obj The data object.
   */
  public void addSharedData(String key, Object obj){
    // performs no action ... implemented by extending classes
  }

  /**
   * Removes all shared data managed by this broker.
   */
  public void clearSharedData(){
    // performs no action ... implemented by extending classes
  }
  
  /**
   * Frees resources used by the CONCURRENT and DISTRIBUTED parallel brokers.
   */
  public void close(){
    // performs no action ... implemented by extending classes
  }
  
  /**
   * Overrides the amount of memory defined in the driver's properties file, if
   * this method is called prior to calling submit().  If submit() has already
   * been called, then close() must be called, then submit() must be called
   * again before the change will take effect.
   */
  public void setDriverMaxMemory(long megs){
	// performs no action ... implemented by extending classes
  }
  
  /**
   * Overrides the amount of memory defined in the node's properties file, if
   * this method is called prior to calling submit().  If submit() has already
   * been called, then close() must be called, then submit() must be called
   * again before the change will take effect.
   */
  public void setNodeMaxMemory(long megs){
	// performs no action ... implemented by extending classes
  }
  
  /**
   * Note: Used only by the DISTRIBUTED parallel broker.
   * Sets a timeout value (in seconds) that a user is willing to wait inside
   * a getResult blocking call (getResultWait and getResultsWait). This
   * specifies a maximum time between receiving two successive task results.
   */
  public void setTaskTimeout(long timeoutInSecs) {
	// performs no action ... implemented by ParallelBrokerDistributed
  }
  
  /**
   * Note: Used only by the CONCURRENT parallel broker.
   * Sets that max # of processors to use.
   */
  public void setProcessorCount(int processorCount) {
	// performs no action ... implemented by ParallelBrokerConcurrent
  }
  
  /**
   * Note: Used only by the DISTRIBUTED parallel broker.
   * Sets the JPPF client thread pool size, used by JPPF in order
   * to process task submissions concurrently.
   */
  public void setClientThreadPoolSize(long numClientThreads) {
	// performs no action ... implemented by ParallelBrokerDistributed
  }
  
  /**
   * Note: Used only by the FABRIC parallel broker.
   * Sets the application name in ApplicationSettings
   */
  public void setFabricApplicationName(String appName) {
	// performs no action ... implemented by FabricParallelBroker
  }
  
  /**
   * Note: Used only by the FABRIC parallel broker.
   * Sets the library path in ApplicationSettings
   */
  public void setFabricLibraryPath(String classpath) {
	// performs no action ... implemented by FabricParallelBroker
  }
  
  /**
   * Note: Used only by the FABRIC parallel broker.
   * Sets the relative classpath in ApplicationSettings
   */
  public void setFabricRelativeClasspath(String classpath) {
	// performs no action ... implemented by FabricParallelBroker
  }
  
  /**
   * Note: Used only by the FABRIC parallel broker.
   * Sets the VM node args in ApplicationSettings
   */
  public void setFabricNodeVMArgs(String args) {
	// performs no action ... implemented by FabricParallelBroker
  }
  
  /**
   * Note: Used only by the FABRIC parallel broker.
   * Sets the required operating systems args in ApplicationSettings
   */
  public void setFabricRequiredOperatingSystems(String... args) {
	// performs no action ... implemented by FabricParallelBroker
  }
  
  /**
   * Note: Used only by the FABRIC parallel broker.
   * Sets the max threads per node in ApplicationSettings
   */
  public void setFabricMaxThreadsPerNode(int maxThreads) {
	// performs no action ... implemented by FabricParallelBroker
  }
  
  /**
   * Note: Used only by the FABRIC parallel broker.
   * Sets the max thread queue size in ApplicationSettings
   */
  public void setFabricMaxThreadQueueSize(int maxQueueSize) {
	// performs no action ... implemented by FabricParallelBroker
  }
  
  /**
   * Note: Used only by the FABRIC parallel broker.
   * Sets the application's socket buffer size in ApplicationSettings
   */
  public void setFabricSocketBufferSize(int bytes) {
	// performs no action ... implemented by FabricParallelBroker
  }
  
  /**
   * Note: Used only by the FABRIC parallel broker.
   * Sets the application's baseline node memory in ApplicationSettings
   */
  public void setFabricBaselineNodeMemory(long megs) {
	// performs no action ... implemented by FabricParallelBroker
  }
  
  /**
   * Returns a map of the total memory available (used+free) by host.
   * Used only by the FABRIC parallel broker.
   * 
   * @return Map of the total memory available (used+free), by host.
   */
  public Map<String,Long> getMemoryAvailableByHost() {
	  return null;
  }
  
  public long getClientThreadPoolSize() {
	  return 1;
  }
  
  /**
   * @return the most recent message received from a task execution thread, or
   * null if none have been received.
   */
  public Object getMessage(){ return receivedMessages.poll(); }
  
  /**
   * @return the most recent message received from a task execution thread,
   * blocking if necessary until one arrives.
   */
  public Object getMessageWait() throws InterruptedException{
	  return receivedMessages.take();
  }
  
  /**
   * @return all messages received since the last <code>getMessage*()</code>
   * call, or an empty list if none have been received.
   */
  public List<Object> getMessages(){
	  List<Object> l = new LinkedList<Object>();
	  receivedMessages.drainTo(l);
	  return l;
  }
  
  /**
   * @return all messages received since the last <code>getMessage*()</code>
   * call, blocking if necessary until one arrives.
   */
  public List<Object> getMessagesWait() throws InterruptedException{
	  List<Object> l = new LinkedList<Object>();
	  l.add(receivedMessages.take());
	  receivedMessages.drainTo(l);
	  return l;
  }
  
  //Finalize: -----------------------------------------------------------------
  
  /**
   * A finalize function used to release resources used by
   * any third party object (.e.g. JPPFClient ) if any were created.
   */
  @Override
  protected void finalize() throws Throwable{
      close();
  }
  
  //Public static interfaces: -------------------------------------------------
  
  /**
   * Allows outside classes to receive notifications when thread messages
   * arrive, rather than using the <code>getMessage*()</code> calls.
   * @author bjlawry
   */
  public static interface MessageListener{
	  /**
	   * Notifies the listener that the specified message has been received.
	   * It is recommended that concrete implementations of this method
	   * synchronize themselves, as this method may be called by different
	   * threads during the runtime of the notifying ParallelBroker
	   * implementation.
	   *  
	   * @param message
	   */
	  void messageReceived(Object message);
  }
  
  /**
   * Allows outside classes to receive notifications when the broker is "connected" to whatever
   * system is responsible for handling tasks. In SEQUENTIAL and all CONCURRENT modes, connection
   * notifications are only ever fired upon the first call to any of the <code>submit*()</code>
   * methods that results in tasks being passed off to the underlying completion service. In
   * DISTRIBUTED modes, this method will also be called as soon as the underlying remote
   * completion service is connected, and may be called more than once.
   * 
   * @author Benjamin Lawry (bjlawry@sandia.gov)
   * created on 02/08/2022
   *
   */
  public static interface ConnectionListener{
    void connected();
  }
}
