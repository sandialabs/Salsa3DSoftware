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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * This class is used to abstract away how ParallelUtils loads Java resources (e.g. files embedded
 * within jars or on the class path) and Files from the file system onto Task processing nodes.
 * Extensions of ParallelBroker that support distributed computation may choose fall back to
 * loading files and resources from the ParallelBroker client if they can't be found on the local
 * processing node's file system or class path.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov) created on 04/10/2023
 */
public class LocalClientStreamSupport implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Creates an input stream corresponding to the specified File location.
   * @param clientPath
   * @return
   * @throws IOException
   */
  public InputStream getClientFileAsStream(File clientPath) throws IOException {
    return new BufferedInputStream(new FileInputStream(clientPath), 65536);
  }

  /**
   * Creates an input stream corresponding to the specified resource name on the Java class path.
   * @param resourceName
   * @return
   * @throws IOException
   */
  public InputStream getClientResourceAsStream(String resourceName) throws IOException {
    return ParallelTask.class.getResourceAsStream(resourceName);
  }

  /**
   * Encodes six booleans into the first six bits of a bytes, corresponding to File.exists(), 
   * File.isFile(), File.isDirectory(), File.canRead(), File.canWrite(), and File.canExecute(), in
   * that order.
   * 
   * @param clientPath
   * @return
   * @throws IOException
   */
  public byte getClientFileMetadata(File clientPath) throws IOException {
    ArrayList<Predicate<File>> funcs = new ArrayList<>();
    funcs.add(File::exists);
    funcs.add(File::isFile);
    funcs.add(File::isDirectory);
    funcs.add(File::canRead);
    funcs.add(File::canWrite);
    funcs.add(File::canExecute);
    funcs.add(f -> false);
    funcs.add(f -> false);

    byte b = 0;
    for (int i = 0; i < funcs.size(); i++)
      if (funcs.get(i).test(clientPath))
        b |= 1 << i % 8;
    return b;
  }
  
  public File[] listFiles(File clientDirectory) throws IOException{
    return clientDirectory.listFiles();
  }
}
