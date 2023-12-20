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
package gov.sandia.gmp.parallelutils.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This interface allows ParallelBroker to customize access to a file system. In some cases, it may
 * be necessary to defer file system access to another process outside the one that runs a
 * ParallelBroker instance (e.g. if the broker runs on a system without direct access to required
 * file resources). Applications that access files via this interface may ultimately read and write
 * files to another machine on the local network.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov)
 * created on 08/07/2023
 */
public interface FileBroker {
  /**
   * @param file path to test existence of
   * @return true if the path represents a valid file or directory
   * @throws IOException may be thrown if a network- or disk-read-error occurs during the call
   */
  boolean exists(File file) throws IOException;
  
  /**
   * @param file path to test
   * @return true if and only if exists(File) also returns true and the path points to a File and
   * not a directory.
   * @throws IOException may be thrown if a network- or disk-read-error occurs during the call
   */
  boolean isFile(File file) throws IOException;
  
  /**
   * @param dir path to test
   * @return true if and only if exists(File) also returns true and the path points to a directory
   * and not a file
   * @throws IOException may be thrown if a network- or disk-read-error occurs during the call
   */
  boolean isDirectory(File dir) throws IOException;
  
  /**
   * @param dir structure of directories to create
   * @return true if and only if the specified directory did not exist before and if it and all
   * non-existent parent directories were created as a result of this call
   * @throws IOException may be thrown if a network- or disk-read-error occurs during the call
   */
  boolean mkdirs(File dir) throws IOException;
  
  /**
   * @param dir directory to provide first-level content list for (non-recursive)
   * @return a list of all first-level files (and folders) contained by the specified directory
   * @throws IOException may be thrown if a network- or disk-read-error occurs during the call
   */
  File[] listFiles(File dir) throws IOException;
  
  /**
   * Creates a new InputStream corresponding to the specified File reference
   * @param file file to be read
   * @return an InputStream corresponding to the specified file
   * @throws IOException may be thrown if a network- or disk-read-error occurs during the call
   */
  InputStream newInputStream(File file) throws IOException;
  
  /**
   * Creates a new OutputStream corresponding the the specified File path. If the File does not
   * exist, a new one will be created. If it does exist, the File at the specified path will be
   * overwritten.
   * @param file path to open an OutputStream to
   * @return new OutputStream corresponding to the specified file
   * @throws IOException may be thrown if a network- or disk-read-error occurs during the call
   */
  OutputStream newOutputStream(File file) throws IOException;
}
