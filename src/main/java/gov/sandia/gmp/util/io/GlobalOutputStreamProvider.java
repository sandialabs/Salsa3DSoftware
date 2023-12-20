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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import gov.sandia.gmp.util.io.OutputStreamProvider.FileOutputStreamProvider;

/**
 * Allows custom OutputStream initialization for Files, depending on what context a GMP/SALSA3D
 * application may be running in (e.g. local-only or distributed). By default, all File writes
 * are handled directly on the local file system. However, when running ParallelTasks in a
 * distributed environment, not all compute Nodes or application Clients will have direct access to
 * the directories they need to write to (e.g. writing data to an NFS file shares from compute
 * servers that don't have access to the share). GlobalOutputStreamProvider allows file/resource
 * writes to defer to remote processes when necessary.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov)
 * created on 08/04/2023
 */
public class GlobalOutputStreamProvider {
  private static FileOutputStreamProvider files = new DefaultFileOutputStreamProvider();
  
  /**
   * @return the stream provider responsible for handling all File writes.
   */
  public static FileOutputStreamProvider forFiles() { return files; }
  
  /**
   * Sets the global singleton FileOutputStreamProvider. In distributing computing environments,
   * this method should typically be called in a static initialization block of the class
   * responsible for representing a unit of distributed computation work, or "Task". The Task class
   * is always loaded first before any actual work is performed, so initializing the stream provider
   * in a static initialization block within the Task class guarantees all submitted Tasks will use
   * the desired stream provider.
   * @param fs stream provider instance to set
   */
  public static void forFiles(FileOutputStreamProvider fs) {
    if(fs == null) throw new NullPointerException("null FileOutputStreamProvider not permitted!");
    files = fs;
  }
  
  /**
   * Uses the local file system to handle all write requests.
   * @author Benjamin Lawry (bjlawry@sandia.gov)
   * created on 08/04/2023
   */
  private static class DefaultFileOutputStreamProvider implements FileOutputStreamProvider{
    private static final long serialVersionUID = 1L;
    
    @Override
    public OutputStream newStream(File path) throws IOException {
      return new BufferedOutputStream(new FileOutputStream(path),65536);
    }

    @Override
    public boolean mkdirs(File directory) { return directory.mkdirs(); }
  }
}
