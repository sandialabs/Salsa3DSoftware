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

/**
 * This interface provides ParallelUtils a way to get around dependencies that Distributed
 * ParallelTasks have that are not required by applications that don't require JPPF/NRM
 * functionality.
 * 
 * Also provides a means for remotely loading resources and files that reside on the Client machine
 * as InputStreams at the Nodes.
 * 
 * @author bjlawry
 * 
 */
public interface SharedObjectProvider extends Serializable {
  /**
   * This method may only be called from distributed versions of ParallelTasks and returns what a
   * JPPFTask instance would return when calling getDataProvider().getValue(key). This method always
   * returns null if used in a non-distributed sense.
   * 
   * @param key the key that is mapped to the desired shared object.
   * @return the shared object associated with the given key, provided that A) the key is valid and
   *         B) the calling task implementation is being used in the distributed sense.
   */
  public Object getSharedObject(String key) throws Exception;

  /**
   * Sets this SharedObjectProvider's result instance.
   * 
   * @param result
   */
  public void setResult(Object result);

  /** @return this SharedObjectProvider's result instance. */
  public Object getResult();

  /**
   * Allows the developer to store a unique ID for this task.
   * 
   * @param id
   */
  public void setId(String id);

  /** @return the unique ID set by calling setID(String id). */
  public String getId();
}
