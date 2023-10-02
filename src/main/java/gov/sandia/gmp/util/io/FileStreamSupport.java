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

import java.io.File;
import java.io.IOException;

/**
 * Defines all file/directory accessibility tests one may wish to perform when reading from or
 * writing to files or folders. 
 * @author Benjamin Lawry (bjlawry@sandia.gov)
 * created on 08/04/2023
 */
public interface FileStreamSupport {
  /**
   * @param file to get metadata for
   * @return a byte that can be decoded using the FileAttributes enum type
   * @throws IOException may be thrown in the event of a network problem during the call
   */
  default byte getMetadata(File file) throws IOException { return FileAttributes.encode(file); }

  /**
   * @param directory folder to list first-level contents for
   * @return an array of all first-level files contained by the specified directory (non-recursive)
   * @throws IOException may be thrown in the event of a network problem during the call
   */
  default File[] listFiles(File directory) throws IOException { return directory.listFiles(); }
  
  /**
   * Equivalent to calling FileAttributes.EXISTS.test(getMetadata(file)).
   * @param file file to test existence of
   * @return true if the file exists
   * @throws IOException
   */
  default boolean exists(File file) throws IOException{
    return file.exists() || FileAttributes.EXISTS.test(getMetadata(file));
  }
  
  /**
   * Equivalent to calling FileAttributes.IS_FILE.test(getMetadata(file))
   * @param file reference to test
   * @return true if the file exists and is not a directory
   * @throws IOException
   */
  default boolean isFile(File file) throws IOException{
    return file.isFile() || FileAttributes.IS_FILE.test(getMetadata(file));
  }
  
  /**
   * Equivalent to calling FileAttributes.IS_DIRECTORY.test(getMetadata(file))
   * @param file reference to test
   * @return true if the file exists and is a directory (not a file)
   * @throws IOException
   */
  default boolean isDirectory(File file) throws IOException{
    return file.isDirectory() || FileAttributes.IS_DIRECTORY.test(getMetadata(file));
  }
  
  /**
   * Equivalent to calling FileAttributes.READABLE.test(getMetadata(file))
   * @param file reference to test
   * @return true if the file/directory is readable with the current user permissions
   * @throws IOException
   */
  default boolean canRead(File file) throws IOException{
    return file.canRead() || FileAttributes.READABLE.test(getMetadata(file));
  }
  
  /**
   * Equivalent to calling FileAttributes.WRITEABLE.test(getMetadata(file))
   * @param file reference to test
   * @return true if the file/directory is writeable with the current user permissions
   * @throws IOException
   */
  default boolean canWrite(File file) throws IOException{
    return file.canWrite() || FileAttributes.WRITEABLE.test(getMetadata(file));
  }
  
  /**
   * Equivalent to calling FileAttributes.EXECUTABLE.test(getMetadata(file))
   * @param file reference to test
   * @return true if the file/directory is executable
   * @throws IOException
   */
  default boolean canExecute(File file) throws IOException{
    return file.canExecute() || FileAttributes.EXECUTABLE.test(getMetadata(file));
  }
}
