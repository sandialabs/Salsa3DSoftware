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

import java.io.IOException;
import java.io.Serializable;

/**
 * A read/write wrapper function used frequently by distributed processing nodes when significant
 * read/write file traffic can lead to read/write timeout errors. To use the task simply implements
 * the the interface ReadWriteCatch which requires the functions readCatch(String fp),
 * writeCatch(String fp), and catchExceptionString(int ecnt, Exception ex, String fp) to be defined.
 * If only a subset of these functions is desired the others can be defined to do nothing. The
 * intent of this object is to give read/write functions more than one chance to succeed, thus
 * avoiding a FileNotFoundException which can happen if a timeout is exceeded during file reads and
 * writes. This can happens more frequently if many tasks, on many processing nodes, are executing
 * reads and writes simultaneously.
 * 
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class CatchRWException implements Serializable {
  /**
   * Thread sleep time in milliseconds. This setting is used after each read/write fail to give the
   * file system a chance to clear before the read/write is tried again.
   */
  private long aThreadSleep = 3000;

  /**
   * Read/Write fail limit after which an error is thrown. After this many read/write failures an
   * error is thrown back to the caller.
   */
  private int aReadWriteFailLimit = 5;

  /**
   * Default constructor.
   */
  public CatchRWException() {
    // use defaults.
  }

  /**
   * Standard constructor. Sets the thread sleep time and the read/write fail limit.
   * 
   * @param threadSleepTime The thread sleep time.
   * @param readwriteFailLimit The read/write fail limit.
   */
  public CatchRWException(long threadSleepTime, int readwriteFailLimit) {
    aThreadSleep = threadSleepTime;
    aReadWriteFailLimit = readwriteFailLimit;
  }

  /**
   * Sets the thread sleep time to tst.
   * 
   * @param tst The new thread sleep time setting.
   */
  public void setThreadSleepTime(long tst) {
    aThreadSleep = tst;
  }

  /**
   * Returns the current thread sleep time setting.
   * 
   * @return The current thread sleep time setting.
   */
  public long getThreadSleepTime() {
    return aThreadSleep;
  }

  /**
   * Sets the Read/Write fail limit to rwfl.
   * 
   * @param rwfl The new Read/Write fail limit setting.
   */
  public void setReadWriteFailLimit(int rwfl) {
    aReadWriteFailLimit = rwfl;
  }

  /**
   * Returns the current setting of the Read/Write fail limit.
   * 
   * @return The current setting of the Read/Write fail limit.
   */
  public int getReadWriteFailLimit() {
    return aReadWriteFailLimit;
  }

  /**
   * Calls the input ReadWriteCatch objects (rwc) readCatch function. If it is successful this
   * function simply returns. If an error is encountered the read is retried. It continues to retry
   * up to aReadWriteFailLimit times after which the last error is thrown back to the caller. This
   * function is used primarily by distributed task nodes where significant read/writes are
   * performed and timeouts can occur regularly.
   * 
   * @param fp The file path passed to the readCatch() function.
   * @param rwc The ReadWriteCatch object whose readCatch() function will be called.
   * @return A string containing the errors from any failures that resulted in a retry. Empty if
   *         successful the first time.
   * 
   * @throws IOException
   */
  public String read(String fp, ReadWriteCatch rwc) throws IOException {
    // try to read the input file name

    String readWriteFail = "";
    int ecnt = 0;
    while (true) {
      try {
        // call the read function ... exit if it succeeds.

        rwc.readCatch(fp);
        break;
      } catch (Exception ex) {
        // unsuccessful ... increment count and try again ... after
        // aReadWriteFailLimit tries throw error

        ++ecnt;
        readWriteFail += rwc.catchExceptionString(ecnt, ex, fp);
        if (ecnt == aReadWriteFailLimit)
          throw new IOException(ex);

        // sleep to give the file system a chance to clear

        try {
          Thread.sleep(aThreadSleep);
        } catch (InterruptedException e) {
        }
      }
    }

    // return read/write fail string

    return readWriteFail;
  }

  /**
   * Calls the input ReadWriteCatch objects (rwc) writeCatch function. If it is successful this
   * function simply returns. If an error is encountered the write is retried. It continues to retry
   * up to aReadWriteFailLimit times after which the last error is thrown back to the caller. This
   * function is used primarily by distributed task nodes where significant read/writes are
   * performed and timeouts can occur regularly.
   * 
   * @param fp The file path passed to the writeCatch() function.
   * @param rwc The ReadWriteCatch object whose writeCatch() function will be called.
   * @throws IOException
   */
  public String write(String fp, ReadWriteCatch rwc) throws IOException {
    // try to write the input file name

    String readWriteFail = "";
    int ecnt = 0;
    while (true) {
      try {
        // call the write function ... exit if it succeeds.

        rwc.writeCatch(fp);
        break;
      } catch (Exception ex) {
        // unsuccessful ... increment count and try again ... after
        // aReadWriteFailLimit tries throw error

        ++ecnt;
        readWriteFail += rwc.catchExceptionString(ecnt, ex, fp);
        if (ecnt == aReadWriteFailLimit)
          throw new IOException(ex);

        // sleep to give the file system a chance to clear

        try {
          Thread.sleep(aThreadSleep);
        } catch (InterruptedException e) {
        }
      }
    }

    // return read/write fail string

    return readWriteFail;
  }
}
