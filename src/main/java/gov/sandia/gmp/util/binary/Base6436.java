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
package gov.sandia.gmp.util.binary;

import java.math.BigInteger;
import java.util.Base64;

/**
 * Piggybacks a Base36 encoding/decoding implementation on top of Base64 to guarantee the following:
 * 
 * <ol>
 * <li>Arbitrary byte arrays can be encoded into lower-case alphanumeric strings.</li>
 * <li>Encoding is non-numeric (in other words, leading zeros in the data are preserved)</li>
 * </ol>
 * 
 * The intention of these constraints is to produce Strings compatible with both Linux and Windows
 * file-systems so that hashed query strings can be converted into valid directory names.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov) created on 04/07/2022
 */
public class Base6436 {
  /**
   * @param bytes to be encoded into text
   * @return text representation of the specified bytes in Base6436 encoding
   */
  public static String encode(byte[] bytes) {
    return new BigInteger(1, Base64.getEncoder().encode(bytes)).toString(36);
  }

  /**
   * 
   * @param encoded assumes the String has been encoded in the same manner as Base6436.encode().
   * @return decoded byte[]
   */
  public static byte[] decode(String encoded) {
    return Base64.getDecoder().decode(new BigInteger(encoded, 36).toByteArray());
  }
}
