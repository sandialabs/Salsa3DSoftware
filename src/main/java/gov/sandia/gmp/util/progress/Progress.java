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
package gov.sandia.gmp.util.progress;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Simple functional interface for handling progress updates.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov) created on 09/02/2022
 */
@FunctionalInterface
public interface Progress {
  /**
   * @param done work done so far
   * @param total total work steps
   * @param msg optional message (null permitted)
   */
  void update(int done, int total, String msg);

  /**
   * Convenience method, has same effect as calling update(done,total,null);
   * 
   * @param done work done so far
   * @param total total work steps
   */
  default void update(int done, int total) {
    update(done, total, null);
  }
  
  /**
   * @param generator
   * @return a Progress instance where all String messages are supplied by the specified generator
   */
  default Progress withMessageGenerator(Supplier<String> generator) {
    return (done,total,msg) -> update(done, total, generator.get());
  }
  
  /**
   * 
   * @param generator
   * @return a Progress instance where the generator uses "done" and "total" to produce the
   * String message.
   */
  default Progress withMessageGenerator(BiFunction<Integer,Integer,String> generator) {
    return (done,total,msg) -> update(done,total,generator.apply(done,total));
  }

  /**
   * Combines all of the Progress instances in the specified Collection into one.
   * 
   * @param c collection of Progress intances to aggregate
   * @return single, combined Progress interface
   */
  static Progress aggregate(Collection<Progress> c) {
    return (done, total, msg) -> c.forEach(p -> p.update(done, total, msg));
  }

  /**
   * Combines all of the Progress instances in the specified array into one.
   * 
   * @param c collection of Progress intances to aggregate
   * @return single, combined Progress interface
   */
  static Progress aggregate(Progress... p) {
    return aggregate(Arrays.asList(p));
  }
}
