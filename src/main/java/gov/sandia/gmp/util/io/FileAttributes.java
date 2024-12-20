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
import java.util.Arrays;
import java.util.BitSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Enumerates the six standard queries one might make concerning the accessibility of a File
 * object in Java: EXISTS, IS_FILE, IS_DIRECTORY, READABLE, WRITEABLE, and EXECUTABLE. Encodes all
 * of these into a single byte for the purpose of testing accessibility of Files on a remote
 * StreamSource instance.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov)
 * created on 04/06/2023
 */
public enum FileAttributes implements Predicate<File>{
  EXISTS(File::exists),
  IS_FILE(File::isFile),
  IS_DIRECTORY(File::isDirectory),
  READABLE(File::canRead),
  WRITEABLE(File::canWrite),
  EXECUTABLE(File::canExecute),
  RESERVED1,
  RESERVED2;
  
  private final Predicate<File> pred;
  
  private FileAttributes(Predicate<File> p) { pred = p; }
  private FileAttributes() { this(null); }
  
  public boolean test(byte encoded) { return BitSet.valueOf(new byte[] {encoded}).get(ordinal()); }
  
  @Override
  public boolean test(File t) { return pred == null ? false : pred.test(t); }
  
  public static byte encode(File f) {
    byte b = 0;
    for(int i = 0; i < values().length; i++)
      if(values()[i].test(f))
        b |= 1<<i%8;
    return b;
  }
  
  /** @return an array of all non-reserved FileAttributes enums */
  public static FileAttributes[] standardValues() {
    int[] ct = new int[] {0};
    return Arrays.asList(FileAttributes.values()).stream()
      .filter(a -> a.pred != null)
      .peek(a -> ct[0]++)
      .collect(Collectors.toList())
      .toArray(new FileAttributes[ct[0]]);
  }
}
