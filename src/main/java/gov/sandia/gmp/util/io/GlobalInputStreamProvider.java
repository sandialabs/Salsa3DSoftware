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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import gov.sandia.gmp.util.io.InputStreamProvider.FileInputStreamProvider;

/**
 * Allows custom InputStream initialization for Files, depending on what context a GMP/SALSA3D
 * application may be running in (e.g. local-only or distributed). By default, all File accesses
 * are handled directly on the local file system. However, when running ParallelTasks in a
 * distributed environment, not all compute Nodes will have access to these read-only resources on
 * their local file systems. GlobalInputStreamProvider allows file/resource access to fall back or
 * even defer to the remote Client application for file-based resources needed at the Nodes.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov)
 * created on 04/10/2023
 */
public class GlobalInputStreamProvider {
  private static FileInputStreamProvider files = new DefaultFileInputStreamProvider();
  
  /**
   * @return the stream provider responsible for handling all File references.
   */
  public static FileInputStreamProvider forFiles(){ return files; }
  
  /**
   * Sets the global singleton FileInputStreamProvider. In distributing computing environments,
   * this method should typically be called in a static initialization block of the class
   * responsible for representing a unit of distributed computation work, or "Task". The Task class
   * is always loaded first before any actual work is performed, so initializing the stream provider
   * in a static initialization block within the Task class guarantees all submitted Tasks will use
   * the desired stream provider.
   * @param fs stream provider instance to set
   */
  public static void forFiles(FileInputStreamProvider fs) {
    if(fs == null) throw new NullPointerException("null FileInputStreamProvider not permitted!");
    files = fs;
  }
  
  /**
   * Simple, local-only implementation of a FileInputStreamProvider. All InputStreams created by
   * this provider correspond to what is available on the local file system, and no alternates are
   * used when attempts to open local FileInputStreams fail (e.g. when the file isn't found).
   * 
   * @author Benjamin Lawry (bjlawry@sandia.gov)
   * created on 04/10/2023
   */
  private static class DefaultFileInputStreamProvider implements FileInputStreamProvider{
    private static final long serialVersionUID = 1L;

    @Override
    public InputStream newStream(File dataObject) throws IOException {
      return new BufferedInputStream(new FileInputStream(dataObject),65536);
    }
  }
}
