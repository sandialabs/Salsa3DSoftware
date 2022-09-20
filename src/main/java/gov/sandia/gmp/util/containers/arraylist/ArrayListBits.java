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
package gov.sandia.gmp.util.containers.arraylist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.BitSet;
import java.util.Scanner;

/**
 * <p>ArrayListBits</p>
 *
 * <p>ArrayListBits extends java.util.BitSet by adding methods that read/write
 * the bits to/from Scanners and DataInputStreams as a series of bytes.
 * The number of bytes read/written is given by (int)Math.ceil(length()/8.0)
 * where length() is the index of the last bit that is set on, which may be
 * larger or smaller than the number of boolean type values an application
 * may wish manage.
 * </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Sandia National Laboratories</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */

@SuppressWarnings("serial")
public class ArrayListBits extends BitSet
{
  /**
   * Calls BitSet default constructor.
   */
  public ArrayListBits()
  {
    super();
  }

  /**
   * Creates a bit set whose initial size is large enough to explicitly
   * represent bits with indices in the range 0 through nbits-1.
   * All bits are initially false.
   */
  public ArrayListBits(int nbits)
  {
    super(nbits);
  }

  /**
   * Create a bit set initialized with the bits from the supplied byte[].
   * @param byteCode byte[]
   */
  public ArrayListBits(byte[] byteCode)
  {
    super(byteCode.length*8);
    for (int i=0; i<byteCode.length; ++i)
      for (int j=0; j<8; ++j)
        if ((byteCode[i] & (1<<j)) > 0)
          set(i*8+j);
  }

  /**
   * Read the bits from the specified DataInputStream
   * @param input DataInputStream
   * @throws IOException
   */
  public ArrayListBits(DataInputStream input)
  throws IOException
  {
    int n = input.readInt();
    byte b;
    for (int i=0; i<n; ++i)
    {
      b = input.readByte();
      for (int j = 0; j < 8; ++j)
        if ( (b & (1<<j)) > 0)
          set(i * 8 + j);
    }
  }

  /**
   * Read the bits from the specified Scanner
   * @param input Scanner
   * @throws IOException
   */
  public ArrayListBits(Scanner input)
      throws IOException
  {
    int n = input.nextInt();
    byte b;
    for (int i=0; i<n; ++i)
    {
      b = input.nextByte();
      for (int j = 0; j < 8; ++j)
        if ( (b & (1<<j)) > 0)
          set(i * 8 + j);
    }
  }

  /**
   * Retrieve a byte[] just big enough to hold the last bit that is set to on.
   * @return byte[]
   */
  public byte[] getByteCode()
  {
    byte[] byteCode = new byte[(int)Math.ceil(length()/8.0)];
    for(int i=nextSetBit(0); i>=0; i=nextSetBit(i+1))
      byteCode[i/8] += (1<<(i%8));
    return byteCode;
  }

  /**
   * Write the bits out to the specified DataOutputStream as bytes.  The number
   * of bytes written will be just enough to include the last bit that is set
   * to on.
   */
  public void write(DataOutputStream output)
      throws IOException
  {
    int nbytes = (int)Math.ceil(length()/8.0);
    output.writeInt(nbytes);
    byte b;
    for (int i=0; i<nbytes; ++i)
    {
      b = 0;
      for (int j=0; j<8; j++)
        if (get(i*8+j))
          b += (1<<j);
      output.writeByte(b);
    }
  }

  /**
   * Write the bits out to the specified BufferedWriter as bytes.
   * The number of bytes written will be just enough to include the last bit
   * that is set to on.
   */
  public void write(Writer output)
      throws IOException
  {
    int nbytes = (int)Math.ceil(length()/8.0);
    output .write(String.format("%d%n", nbytes));
    byte b;
    for (int i=0; i<nbytes; ++i)
    {
      b = 0;
      for (int j=0; j<8; j++)
        if (get(i*8+j))
          b += (1<<j);
      if (i % 20 != 19)
        output.write(String.format("%d ",b));
      else
        output.write(String.format("%d%n",b));

    }
    if ((nbytes-1) % 20 != 19)
    output.write(System.getProperty("line.separator"));
  }
}
