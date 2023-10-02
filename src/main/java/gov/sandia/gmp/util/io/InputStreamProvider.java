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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Simple interface that allows for the transformation of different types of objects into
 * InputStreams. This is particularly useful in cases where distributed applications need to
 * stream data from a file system or socket on the application client to task execution nodes.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov)
 * created on 04/05/2023
 */
public interface InputStreamProvider<D> extends Serializable{
  /**
   * Creates a new InputStream for the specified data object
   * @param dataObject
   * @return InputStream for the data represented by the specified object
   * @throws IOException may occur if a disk read error or network issue occurs during creation
   * of the new stream instance
   */
  InputStream newStream(D dataObject) throws IOException;
  
  /**
   * Creates a new InputStream and wraps it in a Scanner
   * @param dataObject
   * @return
   * @throws IOException
   */
  default Scanner newScanner(D dataObject) throws IOException{
    return new Scanner(newStream(dataObject));
  }
  
  /**
   * Creates a new provider, based on this one, that also buffers underlying created streams with
   * the specified buffer length.
   * @param bufLen length in bytes (must be greater than 1 to have any effect)
   * @return new InputStreamProvider that buffers created InputStream instances
   */
  default InputStreamProvider<D> buffered(int bufLen){
    return new InputStreamProvider<>() {
      private static final long serialVersionUID = 1L;

      @Override
      public InputStream newStream(D dataObject) throws IOException {
        InputStream is = InputStreamProvider.this.newStream(dataObject);
        if(bufLen < 2) return is; 
        return new BufferedInputStream(is,bufLen);
      }
    };
  }
  
  /**
   * Allows an outside function to receive notifications whenever newStream is called. If the
   * stream is created successfully, the exception passed to the function will be null and vice
   * versa.
   * @param ioxHandler handles the input, output, and exceptions resulting from all calls to
   * newStream()
   * @return new InputStreamProvider instance that forwards all inputs/outputs/exceptions to
   * ioxHandler
   */
  default InputStreamProvider<D> onNewStream(TriConsumer<D,InputStream,IOException> ioxHandler){
    return new InputStreamProvider<>() {
      private static final long serialVersionUID = 1L;

      @Override
      public InputStream newStream(D dataObject) throws IOException {
        try {
          InputStream is = InputStreamProvider.this.newStream(dataObject);
          ioxHandler.accept(dataObject, is, null);
          return is;
        } catch (IOException x) {
          if(ioxHandler != null) ioxHandler.accept(dataObject,null,x);
          throw x;
        }
      }
    };
  }
  
  /**
   * @return a stream provider that fully reads the underlying stream into a byte[], then uses that
   * byte[] to fulfill all subsequent calls to "newStream(dataObject)".
   */
  default InputStreamProvider<D> memoryResident(int bufLen) {
    return new InputStreamProvider<D>() {
      private static final long serialVersionUID = 1L;
      private Map<D,byte[]> cache = new HashMap<>();

      @Override
      public InputStream newStream(D dataObject) throws IOException {
        synchronized(cache) {
          if(!cache.containsKey(dataObject)) {
            InputStream in = InputStreamProvider.this.newStream(dataObject);
            if(in != null) {
              ByteArrayOutputStream out = new ByteArrayOutputStream();
              byte[] buf = new byte[bufLen < 1 ? 64*1024 : bufLen];
              int r = -1;
              int t = 0;
              while((r = in.read(buf)) > 0) {
                out.write(buf,0,r);
                t += r;
              }
              if(t > 0) cache.put(dataObject, out.toByteArray());
              else cache.put(dataObject, null);
            }
          }
          
          byte[] bytes = cache.get(dataObject);
          return bytes != null ? new ByteArrayInputStream(bytes) : null;
        }
      }
    };
  }
  
  default InputStreamProvider<D> memoryResident(){ return memoryResident(65536); }
  
  @FunctionalInterface
  static interface TriConsumer<T1,T2,T3>{
    void accept(T1 t1, T2 t2, T3 t3);
  }
  
  static interface FileInputStreamProvider extends FileStreamSupport, InputStreamProvider<File>{}
}
