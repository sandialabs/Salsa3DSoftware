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

import java.io.Serializable;
import java.util.Date;

/**
 * The ParallelResult class allows any application that uses ParallelUtils to define result
 * instances that are compatible with both local and distributed versions of ParallelUtils.
 * 
 * @author jrhipp, bjlawry
 */
public abstract class ParallelResult implements Serializable {
  private static final long serialVersionUID = 1L;
  /**
   * The process host node name upon which the task that owns this result was processed.
   */
  private String aHostName;
  /** Used to sync indexing with a matching ParallelTaskDistributed object. */
  private int aIndex = -1;
  /** The task result calculation time in msec. */
  protected long aTaskCalcTime;
  /** The task result submit time in msec. Set from the owning task. */
  protected long aTaskSubmitTime = 0;;
  /** The task result return time in msec. Set by the client on return. */
  protected long aTaskReturnTime = 0;
  /** Automatically set by ParallelUtils as tasks come back. */
  private Exception exception;

  // Constructors: -----------------------------------------------------------

  /** Initializes all private fields. */
  protected ParallelResult() {
    aHostName = null;
    aIndex = -1;
    aTaskCalcTime = 0L;
  }

  /**
   * Sets this task's internal index to indx.
   * 
   * @param indx The task sync index used to match a result with a task.
   */
  public void setIndex(int indx) {
    aIndex = indx;
  }

  /** @return this task's index. */
  public int getIndex() {
    return aIndex;
  }

  /**
   * Sets the process node host name.
   * 
   * @param hostname The process node host name.
   */
  public void setHostName(String hostname) {
    aHostName = hostname;
  }

  /** @return The processing node's host name. */
  public String getHostName() {
    return aHostName;
  }

  /**
   * Sets the task result run time given the input task start time (in msec).
   * 
   * @param startTime
   */
  public void setCalculationTime(long startTime) {
    aTaskCalcTime = (new Date()).getTime() - startTime;
  }

  /** Get Calculation time in msec. */
  public long getCalculationTimeMSec() {
    return aTaskCalcTime;
  }

  /** Get Calculation time in msec. */
  public double getCalculationTimeMinutes() {
    return (double) aTaskCalcTime / 1000.0 / 60.0;
  }

  /** Set task return time in msec. (set by the client on task return). */
  public void setTaskReturnTime(long tskRetTime) {
    aTaskReturnTime = tskRetTime;
  }

  /** @return The task return time (set by the client on task return). */
  public long getTaskReturnTime() {
    return aTaskReturnTime;
  }

  /** @return Sets task submit time (set from the task at instantiation if desired). */
  public void setTaskSubmitTime(long tskSbmtTime) {
    aTaskSubmitTime = tskSbmtTime;
  }

  /** @return The task submit time (set from the task at instantiation if desired). */
  public long getTaskSubmitTime() {
    return aTaskReturnTime;
  }

  /**
   * The total time the task was out for processing (out in control by the parallel task manager).
   * 
   * @return The total time the task was out for processing.
   */
  public long getTaskOutForProcessTime() {
    return aTaskReturnTime - aTaskSubmitTime;
  }

  /**
   * The total parallel overhead of the task (time out in control by the parallel task manager - the
   * total process time where it was in control of a parallel processing node). This represents
   * shipping time from the client to the parallel task manager, and then on to a parallel
   * processing node, and finally the trip back as result to the parallel task manager, and lastly
   * from the task manager to the client.
   * 
   * @return The total parallel overhead of the task.
   */
  public long getTaskManagerOverhead() {
    long tmohd = aTaskReturnTime - aTaskSubmitTime - aTaskCalcTime;
    return (tmohd > 0) ? tmohd : 0;
  }

  /**
   * Allows the developer to store any Exception that may have occured when computing this result
   * and send it back to the Client.
   * 
   * @param e
   */
  public void setException(Exception e) {
    exception = e;
  }

  /** @return the Exception stored by setException(Exception e). */
  public Exception getException() {
    return exception;
  }
}
