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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.EventListener;

/**
 * @author jrhipp, bjlawry
 *         <p>
 *         This class defines functionality common to all ParallelTask implementations, distributed
 *         or otherwise. If this task implementation is to be used in the distributed sense,
 *         ParallelBroker will automatically wrap it in an instance of JPPFTask prior to sending it
 *         off to the driver. This allows ParallelUtils to be deployed in environments without JPPF
 *         and still work in Concurrent or Sequential modes.
 */
public abstract class ParallelTask implements CommunicationsManager, Comparable<ParallelTask>,
Serializable {
  private static CommunicationsManager comm = null;
  private static final PrintlnHandler DEFAULT_PRINTLN_HANDLER = new PrintlnHandler() {
    @Override
    public void stderr(String s) {
      System.err.println(s);
    }

    @Override
    public void stdout(String s) {
      System.out.println(s);
    }
  };
  private static final long serialVersionUID = 1L;
  /** Used to sync indexing with a matching ParallelResult object. */
  private int aIndex;
  /** Used to save the client submit time of this task. */
  private long aSubmitTime = 0;
  /**
   * Used by some ParallelBroker implementations to prioritize queueing of tasks. Smaller values
   * indicate higher priority. 
   */
  private double priority = 0;
  /** Acts as a go-between for ParallelBroker local and distributed modes. */
  private SharedObjectProvider sharedObjectProvider;
  private static LocalClientStreamSupport clientStreamSupport = new LocalClientStreamSupport();
  private transient String hostToExecuteOn;
  private transient PrintlnHandler handler;
  private transient Integer localThreadCount;

  // Constructors: -----------------------------------------------------------

  /** Initializes common fields, must be called by all super-classes. */
  protected ParallelTask() {
    aIndex = -1;
    priority = Double.MAX_VALUE;
    sharedObjectProvider = new DefaultSharedObjectProvider();
    handler = null;
    localThreadCount = null;
    hostToExecuteOn = null;
  }

  // Public abstract methods: ------------------------------------------------

  /** The run function defines the work to be performed by this task. */
  public abstract void run();

  // Public methods: ---------------------------------------------------------
  
  public InputStream getClientFileAsStream(File clientPath) throws IOException {
    return clientStreamSupport.getClientFileAsStream(clientPath);
  }

  public InputStream getClientResourceAsStream(String resourceName) throws IOException {
    return clientStreamSupport.getClientResourceAsStream(resourceName);
  }

  public byte getClientFileMetadata(File clientPath) throws IOException {
    return clientStreamSupport.getClientFileMetadata(clientPath);
  }

  /**
   * @return the host that the client application desired to execute this task on.
   */
  public String getExecutionHost() {
    return hostToExecuteOn;
  }

  /** @return the string id set by calling setId(String id). */
  public String getId() {
    return sharedObjectProvider.getId();
  }

  /** @return The task index. */
  public int getIndex() {
    return aIndex;
  }

  /**
   * @return the number of task execution threads running in the node JVM shared by the task
   *         execution thread that called this method.
   * @throws IllegalStateException if this call is made outside of a concrete implementation of
   *         {@link ParallelTask#run()}.
   */
  public int getLocalThreadCount() throws IllegalStateException {
    if (localThreadCount != null)
      return localThreadCount.intValue();
    throw new IllegalStateException(
        "cannot call getLocalThreadCount() " + "outside of ParallelTask.run()");
  }
  
  /** @return the priority of this task. */
  public double getPriority() { return priority; }

  /** @return The result object contained in the JPPFTask. */
  public ParallelResult getResultObject() {
    return (ParallelResult) sharedObjectProvider.getResult();
  }

  /**
   * Returns a shared data object associated with the input key. If no object is associated with the
   * input key null is returned.
   * 
   * @param key The input key for which the associated data object will be returned.
   * @return The data object associated with the input key.
   * @throws Exception
   */
  public Object getSharedObject(String key) throws Exception {
    return sharedObjectProvider.getSharedObject(key);
  }

  /** @return The submit time. */
  public long getSubmitTime() {
    return aSubmitTime;
  }
  
  /**
   * To be called only by the ParallelBroker framework, this allows distributed versions of
   * ParallelBroker to instert their own functionality for retrieving client-side files and
   * resources.
   * @param css
   */
  public static void setClientStreamSupport(LocalClientStreamSupport css) { clientStreamSupport = css; }
  
  public static LocalClientStreamSupport getClientStreamSupport() { return clientStreamSupport; }

  /**
   * Must be called prior to calling client.submit(), this method allows the client application to
   * direct this task to a specific host on the network. This call is ignored in
   * Sequential/Concurrent modes by default and is only guaranteed to take effect in Fabric versions
   * of ParallelBroker.
   * 
   * @param host
   */
  public void setExecutionHost(String host) {
    hostToExecuteOn = host;
  }

  /**
   * Allows the developer to set a unique String id for this task.
   * 
   * @param id
   */
  public void setId(String id) {
    sharedObjectProvider.setId(id);
  }

  /**
   * Sets the internal index to indx.
   * 
   * @param indx The task sync index used to match a result with a task.
   */
  public void setIndex(int indx) {
    aIndex = indx;
  }

  /**
   * This method should only ever be called by ParallelUtils framework classes. It should never be
   * called by client applications.
   * 
   * @param count
   */
  public void setLocalThreadCount(Integer count) {
    localThreadCount = count;
  }
  
  /**
   * Stores a new priority value for this task (smaller values indicate higher priority).
   * @param priority
   */
  public void setPriority(double priority) { this.priority = priority; }

  /**
   * Only the underlying ParallelUtils framework should call this method
   * 
   * @param h
   */
  public void setPrintlnHandler(PrintlnHandler h) {
    handler = h;
  }

  /** Sets the result object instance contained by this ParallelTask. */
  public void setResult(Object o) {
    sharedObjectProvider.setResult(o);
  }

  /** To be called only by distributed versions of ParallelBroker. */
  public void setSharedObjectProvider(SharedObjectProvider prov) {
    if (sharedObjectProvider != null)
      prov.setId(sharedObjectProvider.getId());
    sharedObjectProvider = prov;
  }

  /** Sets the task submit time (set by the client at submit). */
  public void setSubmitTime(long submitTime) {
    aSubmitTime = submitTime;
  }

  public void stderr(String s) {
    if (handler == null)
      DEFAULT_PRINTLN_HANDLER.stderr(s);
    else
      handler.stderr(s);
  }

  public void stdout(String s) {
    if (handler == null)
      DEFAULT_PRINTLN_HANDLER.stdout(s);
    else
      handler.stdout(s);
  }

  // Public overridden methods: ----------------------------------------------

  @Override
  public int compareTo(ParallelTask that) {
    if(that == null) return 1;
    int c = Double.compare(this.priority, that.priority);
    if(c != 0) return c;
    c = Integer.compare(this.aIndex, that.aIndex);
    if(c != 0) return c;
    return Long.compare(this.aSubmitTime, that.aSubmitTime);
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + aIndex;
    result = prime * result + (int) (aSubmitTime ^ (aSubmitTime >>> 32));
    long temp;
    temp = Double.doubleToLongBits(priority);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ParallelTask other = (ParallelTask) obj;
    if (aIndex != other.aIndex)
      return false;
    if (aSubmitTime != other.aSubmitTime)
      return false;
    if (Double.doubleToLongBits(priority) != Double.doubleToLongBits(other.priority))
      return false;
    return true;
  }

  /**
   * This method may only be called from inside this task's <code>run()</code> method, and may not
   * be supported by all versions of ParallelUtils. If this operation is supported, the underlying
   * framework will asynchronously forward the specified data to this task's corresponding client
   * application, otherwise, an {@link UnsupportedOperationException} will be thrown.
   */
  @Override
  public void sendToClient(Object data) throws IOException {
    if (comm == null)
      throw new UnsupportedOperationException(
          "node-to-client communication not supported by " + getClass());
    comm.sendToClient(data);
  }

  // Private inner classes: --------------------------------------------------

  /** Default provider that is used only in Sequential and Concurrent modes. */
  private static class DefaultSharedObjectProvider implements SharedObjectProvider {
    private static final long serialVersionUID = 1L;
    private Object result;
    private String id;

    @Override
    public String getId() {
      return id;
    }

    @Override
    public Object getResult() {
      return result;
    }

    @Override
    public Object getSharedObject(String key) {
      return null;
    }

    @Override
    public void setId(String id) {
      this.id = id;
    }

    @Override
    public void setResult(Object res) {
      result = res;
    }
  }

  // Public static interfaces: -----------------------------------------------

  public static interface ParallelTaskListener extends EventListener, Serializable {
    public void fireNotification(Serializable s);
  }

  public static interface PrintlnHandler {
    public void stderr(String s);

    public void stdout(String s);
  }

  // Public static methods: --------------------------------------------------

  /**
   * @return the singleton CommunicationsManager instance that will be used to send messages from
   *         this ParallelTask's JVM to the client JVM.
   */
  public static CommunicationsManager getCommunicationsManager() {
    return comm;
  }

  /**
   * Statically sends a message to the client without including any information pertaining to the
   * sender.
   * 
   * @param o
   */
  public static void sendToClientThreadless(Object o) throws IOException {
    if (comm == null)
      throw new UnsupportedOperationException("communications manager not set");
    comm.sendToClient(o);
  }

  /**
   * To be called only by framework code that supports various extensions of the ParallelBroker.
   */
  public static void setCommunicationsManager(CommunicationsManager c) {
    comm = c;
  }
}
