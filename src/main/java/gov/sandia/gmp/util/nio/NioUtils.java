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
package gov.sandia.gmp.util.nio;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;

import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;

/**
 * Contains static methods used for reading/writing data from/to NIO channels using a ByteBuffer.
 * These methods assume that the buffer may be left in a partially-utilized state to minimize
 * expensive underlying system calls when writing data. Read operations always reset the buffer to
 * position 0 prior to reading any data.
 * 
 * @author Benjamin Lawry (bjlawry@sandia.gov)
 * created on 07/20/2022
 */
public class NioUtils {
  /**
   * Writes all bytes left in the specified buffer to the channel and resets the buffer. The
   * channel is left open after this call.
   * @param dest
   * @param buf
   * @return number of bytes written to the channel
   * @throws IOException
   */
  public static int flushBufferToChannel(WritableByteChannel dest, ByteBuffer buf) 
      throws IOException{
    int written = 0;
    buf.flip();
    written = dest.write(buf);
    buf.compact();
    buf.rewind();
    return written;
  }
  
  /**
   * Reads up to len doubles into the specified vals[], starting at index idx.
   * @param src data source to read from
   * @param buf buffer to use for the read operation
   * @param vals arrays to store read doubles into
   * @param idx starting index for double storage
   * @param len maximum number of doubles to read
   * @return the number of doubles actually read
   * @throws IOException
   */
  public static int readDoubles(ReadableByteChannel src, ByteBuffer buf,
      double[] vals, int idx, int len)
  throws IOException{
    int doublesToRead = (len-idx);
    int cIdx = idx;
    
    while(doublesToRead > 0) {
      buf.rewind();
      int doublesToReadThisTime = Math.min(doublesToRead,buf.capacity()/Double.BYTES);
      buf.limit(doublesToReadThisTime*Double.BYTES);
      int read = src.read(buf)/Double.BYTES;
      
      for(int r = 0; r < read; r++) vals[cIdx++] = buf.getDouble(r*Double.BYTES);
      doublesToRead -= read;
    }
    
    return cIdx-idx;
  }
  
  public static <E extends Enum<E>> E readEnum(ReadableByteChannel src, ByteBuffer buf, 
      Class<E> cls) throws IOException{
    int ordinal = readInt(src,buf);
    try {
      Object array = cls.getMethod("values").invoke(null);
      return cls.cast(Array.get(array, ordinal));
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      //This should never happen as cls is concrete and must extend Enum
      e.printStackTrace();
    }
    
    return null;
  }
  
  /**
   * Reads up to len ints into the specified vals[], starting at index idx.
   * @param src data source to read ints from
   * @param buf buffer to use for the read operation
   * @param vals arrays to store read ints into
   * @param idx starting index for int storage
   * @param len maximum number of ints to read
   * @return the number of ints actually read
   * @throws IOException
   */
  public static int readInts(ReadableByteChannel src, ByteBuffer buf, int[] vals, int idx, int len)
  throws IOException{
    int intsToRead = (len-idx);
    int cIdx = idx;
    
    while(intsToRead > 0) {
      buf.rewind();
      int intsToReadThisTime = Math.min(intsToRead,buf.capacity()/Integer.BYTES);
      buf.limit(intsToReadThisTime*Integer.BYTES);
      int read = src.read(buf)/Integer.BYTES;
      
      for(int r = 0; r < read; r++) vals[cIdx++] = buf.getInt(r*Integer.BYTES);
      intsToRead -= read;
    }
    
    return cIdx-idx;
  }
  
  public static int readInt(ReadableByteChannel src, ByteBuffer buf) throws IOException{
    int[] i = new int[1];
    readInts(src,buf,i,0,i.length);
    return i[0];
  }
  
  /**
   * Reads up to len doubles into the specified vals[], starting at index idx.
   * @param src data source to read from
   * @param buf buffer to use for the read operation
   * @param vals arrays to store read doubles into
   * @param idx starting index for double storage
   * @param len maximum number of doubles to read
   * @return the number of doubles actually read
   * @throws IOException
   */
  public static int readLongs(ReadableByteChannel src, ByteBuffer buf,
      long[] vals, int idx, int len)
  throws IOException{
    int longsToRead = (len-idx);
    int cIdx = idx;
    
    while(longsToRead > 0) {
      buf.rewind();
      int longsToReadThisTime = Math.min(longsToRead,buf.capacity()/Long.BYTES);
      buf.limit(longsToReadThisTime*Long.BYTES);
      int read = src.read(buf)/Long.BYTES;
      
      for(int r = 0; r < read; r++) vals[cIdx++] = buf.getLong(r*Long.BYTES);
      longsToRead -= read;
    }
    
    return cIdx-idx;
  }
  
  public static long readLong(ReadableByteChannel src, ByteBuffer buf) throws IOException{
    long[] l = new long[1];
    readLongs(src,buf,l,0,l.length);
    return l[0];
  }

  /**
   * Writes the specified double values to the specified ByteBuffer, then to the destination channel
   * whenever the buffer fills to capacity. Some bytes may be written to the buffer but not the
   * channel, so the buffer must be externally flushed to the channel after this call is made. The
   * channel is left open after this call.
   * @param buf
   * @param dest
   * @return number of bytes written to the channel
   * @throws IOException
   */
  public static int writeDoubles(WritableByteChannel dest, ByteBuffer buf, double...vals)
  throws IOException{
    int bytesWritten = 0;
    
    for(double v : vals) {
      //We've reached the end of the buffer: write it to the channel and reset:
      if(buf.remaining() < Double.BYTES) {
        buf.flip();
        bytesWritten += dest.write(buf);
        buf.compact();
      }
      
      buf.putDouble(v);
    }
    
    return bytesWritten;
  }
  
  public static int writeDoubles(WritableByteChannel dest, ByteBuffer buf, double[] vals,
      int off, int len) throws IOException{
    int bytesWritten = 0;
    
    for(int i = off; i < len+off; i++) {
      //We've reached the end of the buffer: write it to the channel and reset:
      if(buf.remaining() < Double.BYTES) {
        buf.flip();
        bytesWritten += dest.write(buf);
        buf.compact();
      }
      
      buf.putDouble(vals[i]);
    }
    
    return bytesWritten;
  }
  
  public static int writeDoublesFully(WritableByteChannel dest, ByteBuffer buf, double[] vals,
      int off, int len) throws IOException{
    return writeDoubles(dest,buf,vals,off,len)+
      flushBufferToChannel(dest,buf);
  }
  
  /**
   * Writes the specified double values to the specified ByteBuffer, then to the destination channel
   * whenever the buffer fills to capacity. Some bytes may be written to the buffer but not the
   * channel, so the buffer must be externally flushed to the channel after this call is made. The
   * channel is left open after this call.
   * @param buf
   * @param dest
   * @return the number of bytes actually written to the specified channel
   * @throws IOException
   */
  public static int writeDoubles(WritableByteChannel dest, ByteBuffer buf, ArrayListDouble ald)
  throws IOException{
    int bytesWritten = 0;
    
    for(int i = 0; i < ald.size(); i++) {
      //We've reached the end of the buffer: write it to the channel and reset:
      if(buf.remaining() < Double.BYTES) {
        buf.flip();
        bytesWritten += dest.write(buf);
        buf.compact();
      }
      
      buf.putDouble(ald.get(i));
    }
    
    return bytesWritten;
  }
  
  /**
   * Same effect as making a call to writeDoubles followed immediately by a call to
   * flushBufferToChannel.
   * @param buf
   * @param dest
   * @param vals
   * @throws IOException
   */
  public static void writeDoublesFully(WritableByteChannel dest, ByteBuffer buf, 
      ArrayListDouble vals) throws IOException{
    writeDoubles(dest,buf,vals);
    flushBufferToChannel(dest,buf);
  }
  
  /**
   * Same effect as making a call to writeDoubles followed immediately by a call to
   * flushBufferToChannel.
   * @param buf
   * @param dest
   * @param vals
   * @return number of bytes written to the channel
   * @throws IOException
   */
  public static int writeDoublesFully(WritableByteChannel dest, ByteBuffer buf, double...vals) 
  throws IOException{
    return writeDoubles(dest,buf,vals) +
      flushBufferToChannel(dest,buf);
  }
  
  /**
   * Writes the specified int values to the specified ByteBuffer, then to the destination channel
   * whenever the buffer fills to capacity. Some bytes may be written to the buffer but not the
   * channel, so the buffer must be externally flushed to the channel after this call is made. The
   * channel is left open after this call.
   * @param buf
   * @param dest
   * @throws IOException
   */
  public static int writeInts(WritableByteChannel dest, ByteBuffer buf, int...vals)
      throws IOException{
    int bytesWritten = 0;
    
    for(int v : vals) {
      //We've reached the end of the buffer: write it to the channel and reset:
      if(buf.remaining() < Integer.BYTES) {
        buf.flip();
        bytesWritten += dest.write(buf);
        buf.compact();
      }
      
      buf.putInt(v);
    }
    
    return bytesWritten;
  }
  
  public static void writeInts(WritableByteChannel dest, ByteBuffer buf, int[] vals, int off,
      int len) throws IOException{
    for(int i = off; i < len+off; i++) {
      //We've reached the end of the buffer: write it to the channel and reset:
      if(buf.remaining() < Integer.BYTES) {
        buf.flip();
        dest.write(buf);
        buf.compact();
      }
      
      buf.putInt(vals[i]);
    }
  }
  
  public static <N extends Number> void writeInts(WritableByteChannel dest, ByteBuffer buf,
      Collection<N> vals) throws IOException{
    for(N v : vals) {
      //We've reached the end of the buffer: write it to the channel and reset:
      if(buf.remaining() < Integer.BYTES) {
        buf.flip();
        dest.write(buf);
        buf.compact();
      }
      
      buf.putInt(v.intValue());
    }
  }
  
  public static <N extends Number> void writeIntsFully(WritableByteChannel dest, ByteBuffer buf,
      Collection<N> vals) throws IOException{
    writeInts(dest,buf,vals);
    flushBufferToChannel(dest,buf);
  }
  
  /**
   * Same effect as making a call to writeInts followed immediately by a call to
   * flushBufferToChannel
   * @param buf
   * @param dest
   * @param vals
   * @throws IOException
   */
  public static void writeIntsFully(WritableByteChannel dest, ByteBuffer buf, int ... vals) 
  throws IOException{
    writeInts(dest,buf,vals);
    flushBufferToChannel(dest,buf);
  }
  
  public static void writeIntsFully(WritableByteChannel dest, ByteBuffer buf, int[] vals, int off,
  int len) throws IOException{
    writeInts(dest,buf,vals,off,len);
    flushBufferToChannel(dest,buf);
  }
  
  /**
   * Writes the specified long values to the specified ByteBuffer, then to the destination channel
   * whenever the buffer fills to capacity. Some bytes may be written to the buffer but not the
   * channel, so the buffer must be externally flushed to the channel after this call is made. The
   * channel is left open after this call.
   * @param buf
   * @param dest
   * @throws IOException
   */
  public static int writeLongs(WritableByteChannel dest, ByteBuffer buf, long...vals)
  throws IOException{
    int written = 0;
    for(long v : vals) {
      //We've reached the end of the buffer: write it to the channel and reset:
      if(buf.remaining() < Long.BYTES) {
        buf.flip();
        written += dest.write(buf);
        buf.compact();
      }
      
      buf.putLong(v);
    }
    return written;
  }
  
  /**
   * Same effect as making a call to writeLongs followed immediately by a call to
   * flushBufferToChannel.
   * @param buf
   * @param dest
   * @param vals
   * @throws IOException
   */
  public static int writeLongsFully(WritableByteChannel dest, ByteBuffer buf, long...vals) 
  throws IOException{
    return writeLongs(dest,buf,vals) +
        flushBufferToChannel(dest,buf);
  }
  
  public static <E extends Enum<E>> int writeEnum(WritableByteChannel dest, ByteBuffer buf, E e)
  throws IOException{
    return writeInts(dest,buf,e.ordinal());
  }
}
