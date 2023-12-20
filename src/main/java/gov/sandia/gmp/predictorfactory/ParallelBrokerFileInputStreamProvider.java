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
package gov.sandia.gmp.predictorfactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import gov.sandia.gmp.parallelutils.ParallelTask;
import gov.sandia.gmp.util.io.InputStreamProvider.FileInputStreamProvider;

/**
 * Defers FileInputStream creating to ParallelBroker, which may choose to load a file from the
 * ParallelBroker client application if the requested file cannot be found on the local file
 * system.
 * @author Benjamin Lawry (bjlawry@sandia.gov)
 * created on 05/11/2023
 *
 */
public class ParallelBrokerFileInputStreamProvider implements FileInputStreamProvider{
  private static final long serialVersionUID = 1L;

  @Override
  public InputStream newStream(File file) throws IOException {
    return ParallelTask.getClientStreamSupport().getClientFileAsStream(file);
  }

  @Override
  public byte getMetadata(File file) throws IOException {
    return ParallelTask.getClientStreamSupport().getClientFileMetadata(file);
  }

  @Override
  public File[] listFiles(File directory) throws IOException {
    return ParallelTask.getClientStreamSupport().listFiles(directory);
  }
  
  @Override
  public String toString() {
    return new StringBuilder(super.toString()).append("[clientStreamSupport=")
        .append(ParallelTask.getClientStreamSupport()).append("]").toString();
  }
}
