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
package gov.sandia.gmp.util.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Simple interface that allows for the transformation of different types of objects into
 * OutputStreams. This is particularly useful in cases where distributed applications need to
 * stream data to remote file system.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov)
 * created on 08/04/2023
 */
public interface OutputStreamProvider<D> extends Serializable{
  /**
   * 
   * @param dataObject
   * @return
   * @throws IOException
   */
  OutputStream newStream(D dataObject) throws IOException;
  
  /**
   * Creates a new provider, based on this one, that also buffers underlying created streams with
   * the specified buffer length.
   * @param bufLen length in bytes (must be greater than 1 to have any effect)
   * @return new OutputStreamProvider that buffers created OutputStream instances
   */
  default OutputStreamProvider<D> buffered(int bufLen){
    return new OutputStreamProvider<>() {
      private static final long serialVersionUID = 1L;

      @Override
      public OutputStream newStream(D dataObject) throws IOException {
        OutputStream os = OutputStreamProvider.this.newStream(dataObject);
        if(bufLen < 2) return os; 
        return new BufferedOutputStream(os,bufLen);
      }
    };
  }
  
  static interface FileOutputStreamProvider extends FileStreamSupport, OutputStreamProvider<File>{
    /**
     * @param directory folder path to be created
     * @return true if and only if the directory and all necessary non-existent parent directories
     * were created as a result of this call
     * @throws IOException may be thrown if in the event of a network error during the call
     */
    boolean mkdirs(File directory) throws IOException;
  }
}
