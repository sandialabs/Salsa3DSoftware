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
package gov.sandia.gmp.util.profiler;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.Serializable;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import gov.sandia.gmp.util.globals.Globals;

/**
 * A basic profiler that samples code from a single thread and
 * accumulates the samples in a map for merger with other
 * profilers or for output as a formatted string. This object
 * accumulates samples of the program stack of its assigned
 * thread at time intervals set by the client at construction.
 * The samples include the class name, method name, and line
 * number of the program at which each sample is taken. The
 * samples are recorded for each member of the stack trace of
 * the current sample. The actual content of the samples are
 * stored in a separate object (a ProfilerContent object) to
 * remove the content from the control so that it can be
 * returned compactly to a caller in a distributed or concurrent
 * parallel operation.
 * 
 * @author Jim Hipp
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Profiler implements Serializable
{
  /**
   * Used to name profilers ("ProfilerInstance_#" where # is taken from the
   * value of aProfilerCount at creation.
   */
  private static int       aProfilerCount = 0;

  /**
   * An separate output thread to output the profiler content as a formatted
   * string.
   * 
   * @author jrhipp
   *
   */
  private class ProfilerOutputThread implements Runnable
  {
    /**
     * The runnable thread for this class.
     */
    private Thread        runThread = null;

    /**
     * Default Constructor. Create and start thread.
     */
    public ProfilerOutputThread()
    {
      runThread = new Thread(this, "ProfilerOutputThread");
      runThread.start();
    }

    /**
     * Run method executed at start of thread that dumps the profiler
     * content as a formatted string to standard output.
     */
    @Override
    public void run()
    {
      try
      {
        printAccumulationString();
      }
      catch(Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  /**
   * The profiler content output task called by the profiler timer
   * (aTimer) to output content as a formatted string if the function
   * outputOnTimer(...) was called.
   * 
   * @author jrhipp
   *
   */
  private class ProfilerOutputTask extends TimerTask
  {
    @Override
    public void run()
    {
      // create a new profiler output thread to output the profilers
      // content and see if the repeat flag is false.

      new ProfilerOutputThread();
      if (!aOutputRepeatFlg)
      {
        // don't repeat .. cancel this task and set it to null.

        cancel();
        aOutputTask = null;
      }
    }
  }

  /**
   * The profiler sample accumulation task called by the profiler timer
   * (aTimer) to accumulate samples.
   * 
   * @author jrhipp
   *
   */
  private class ProfilerSampleTask extends TimerTask
  {
    /**
     * The sample accumulation function.
     */
    @Override
    public void run()
    {
      if (aTaskThread == null) aTaskThread = Thread.currentThread();

      // only accumulate if requested and the thread is alive.

      if (aAccumulate)
      {
        // set the accumulating flag to true and get the array of
        // StackTraceElements from the thread

        aAccumulating = true;
        StackTraceElement[] ste = aThread.getStackTrace();

        // initialize the ProfilerContent accumulation and the found class and
        // found method flags

        aProfilerContent.initializeSampleAccumulation();
        boolean fndClass  = aFoundClass;
        boolean fndMethod = aFoundMethod;

        // loop from last to first (top to bottom) of the stack and process each
        // StackTraceElement

        for (int i = ste.length - 1; i > -1; --i)
        {
          // get the next StackTraceElement ... if top class or top method have
          // not been found then check

          StackTraceElement stei = ste[i];
          if (!fndClass  && stei.getClassName().equals(aTopClass))
            fndClass  = true;
          if (!fndMethod && stei.getMethodName().equals(aTopMethod))
            fndMethod = true;

          // if top class and top method have both been found then accumulate
          // stack trace to map

          if (fndClass && fndMethod)
            aProfilerContent.accumulateSample(stei.getClassName(), stei.getMethodName(),
                                              stei.getLineNumber());
        }

        // done ... reset accumulating flag

        aAccumulating = false;

        // check for sample interval profile output

        if (aOutSmplCount > -1)
        {
          if (aProfilerContent.getSampleCount() % aOutSmplCount == 0)
          {
            // output sample interval by issuing a new ProfilerOutputThread
            // reset sample interval period to -1 if output repeat flag is -1

            new ProfilerOutputThread();
            if (!aOutputRepeatFlg) aOutSmplCount = -1;
          }
        }
      }
    } 
  }

  /**
   * The name of this profiler assigned by the caller at construction.
   */
  private String             aName            = "";

  /**
   * The name assigned to the timer thread.
   */
  private String             aTimerName       = "";

  /**
   * The thread that is profiled by this profiler.
   */
  private Thread             aThread          = null;

  private Thread             aTaskThread      = null;

  /**
   * The timer used by the profiler to call the profiling task
   * (aProfilerTask), and optionally, the output task (aOutputTask).
   */
	private Timer              aTimer           = null;

	/**
	 * The actual sampling task whose run method is called by the profiler to
	 * gather profile samples.
	 */
  private ProfilerSampleTask aProfilerTask    = null;

  /**
   * A threaded output task to assemble and dump the profiler content as a
   * separate thread. This object is null unless function outputOnTimer(...)
   * has been called. 
   */
  private ProfilerOutputTask aOutputTask      = null;

  /**
   * The accumulation flag set by accumulateOn() and accumulateOff(). Samples
   * are only recorded when this flag is true (on).
   */
  private boolean            aAccumulate      = false;

  /**
   * Set to true when a sample is being accumulated (inside the function
   * ProfilerSampleTask.run()).
   */
  private boolean            aAccumulating    = false;

  /**
   * Used to select the first class in a call stack to include in the
   * profile. Only classes at or below this value are recorded. Others
   * are ignored.
   */
  private String             aTopClass        = "";

  /**
   * Used to select the first method in a call stack to include in the
   * profile. Only methods at or below this value are recorded. Others
   * are ignored.
   */
  private String             aTopMethod       = "";

  /**
   * A line pad prepended to each line of the profilers formatted output
   * content string.
   */
  private String             aPrePend         = "    ";

  /**
   * Used in conjunction with aTopClass to initialize the search
   * in a call stack for the first occurrence of aTopClass. If
   * aTopClass = "" then this value is true. Otherwise it is false.
   */
  private boolean            aFoundClass      = true;

  /**
   * Used in conjunction with aTopMethod to initialize the search
   * in a call stack for the first occurrence of aTopMethod. If
   * aTopMethod = "" then this value is true. Otherwise it is false.
   */
  private boolean            aFoundMethod     = true;

  /**
   * The time at which the last call to accumulateOn() was made.
   */
  private long               aStartTime       = 0;

  /**
   * Used by outputOnSampleCount(...) settings to store the sample count at
   * which the content of the profiler is dumped to standard output. If sample
   * count output is not selected this value is set to -1.
   */
  private int                aOutSmplCount    = -1;

  /**
   * The output repeat flag. Used when outputOnSampleCount(...) or
   * outputOnTimer(...) is called to set the repeat flag. If true then output
   * is dumped at consecutive time or sample count intervals. If false, output
   * is dumped only once.
   */
  private boolean            aOutputRepeatFlg = false;

  /**
   * The content of this profilers operation.
   */
  private ProfilerContent    aProfilerContent = null;

	/**
	 * Standard Constructor. Creates a new profiler assigning the name, thread,
	 * and sample period.
	 * 
	 * @param thrd The thread from which profiler will record samples.
	 * @param period The time (in milliseconds) between each consecutive
	 *               sample. Note: If period is too small then the
	 *               program may spend all of its time in the sampler
	 *               and no time in the actual program.
	 * @param name The user assigned name of this profiler.
	 */
	public Profiler(Thread thrd, long period, String name)
	{
	  aName            = name;
	  aThread          = thrd;
	  aProfilerContent = new ProfilerContent();

	  aTimerName       = "ProfilerInstance_" + aProfilerCount++;
    aTimer           = new Timer(aTimerName);
    aProfilerTask    = new ProfilerSampleTask();
	  aTimer.schedule(aProfilerTask, period, period);
	}

	/**
	 * Returns the ProfilerSampleTaskThread of this Profiler so that it can
	 * be profiled. There is never any other reason to call this function.
	 * 
	 * @return The ProfilerSampleTaskThread of this Profiler so that it can
   *         be profiled.
	 */
	public Thread getProfilerSampleTaskThread()
	{
	  if (aTaskThread == null)
	  {
	    // find task thread ... first find top level group

	    ThreadGroup currentThreadGroup;
	    ThreadGroup rootThreadGroup;
	    ThreadGroup parent;

	    // Get the current thread group

	    currentThreadGroup = Thread.currentThread().getThreadGroup();

	    // Now go find the root thread group

	    rootThreadGroup = currentThreadGroup;
	    parent = rootThreadGroup.getParent();
	    while (parent != null)
	    {
	      rootThreadGroup = parent;
	      parent = parent.getParent();
	    }

	    // get all thread under root group ... make sure array is large enough
	    // to hold group ... if returned count fills array then size twice as
	    // large and try again.
	    
	    int cnt = 0;
	    int tc = rootThreadGroup.activeCount();
	    tc *= 2;
	    Thread[] threads = new Thread [tc];
	    while ((cnt = rootThreadGroup.enumerate(threads)) == tc)
	    {
	      tc *= 2;
	      threads = new Thread [tc];
	    }

	    // cnt threads were found ... loop through all threads and find thread
	    // with name = aTimerName and assign to aTaskThread. If not found then
	    // return null

	    for (int i = 0; i < cnt; ++i)
	    {
	      if (threads[i].getName().equals(aTimerName)) aTaskThread = threads[i];
	    }
	  }

	  // return timer thread

	  return aTaskThread;
	}

	/**
	 * Sets the content output and repeat flag based on the current sample
	 * count. If repeat is true then the content is dumped to standard
	 * output after each passing of outsmplecnt samples. If repeat is false
	 * then output is dumped only once.
	 * 
	 * @param outsmplecnt The number of samples after which output will be
	 *                    sent to the standard output device.
	 * @param repeat Repeat flag. If true output is sent after each consecutive
   *               set of outsmplecnt samples are reached.
	 */
	public void outputOnSampleCount(int outsmplecnt, boolean repeat)
	{
	  aOutSmplCount  = outsmplecnt;
	  aOutputRepeatFlg = repeat;
	  if (aOutputTask != null)
	  {
	    aOutputTask.cancel();
	    aOutputTask = null;
	  }
	}

	/**
	 * Sets the content output timer period and repeat flag. If repeat is
	 * true then the content is dumped to standard output after each the
	 * passing of each output period. If repeat is false then output is dumped
	 * only once.
	 *  
	 * @param period The period of time (milliseconds) after which output will
	 *               be sent to the standard output device.
	 * @param repeat Repeat flag. If true output is sent after each consecutive
	 *               "period".
	 */
	public void outputOnTimer(long period, boolean repeat)
	{
    aOutSmplCount  = -1;
    aOutputRepeatFlg = repeat;
    aOutputTask = new ProfilerOutputTask();
    aTimer.schedule(aOutputTask, 0, period);
	}

	/**
	 * Sets the top class, above which, no other classes are
	 * recored in any sample operations by this profiler. In other
	 * words, no classes above this class in a sample call stack
	 * are recorded in the profiler content.
	 * 
	 * @param topClass The new top class definition.
	 */
	public void setTopClass(String topClass)
	{
	  aTopClass = topClass;
	  if (aTopClass.equals(""))
	    aFoundClass = true;
	  else
	    aFoundClass = false;
	}

	/**
	 * Sets the top method, above which, no other methods are
	 * recorded in any sample operations by this profiler. In other
   * words, no methods above this method in a sample call stack
   * are recorded in the profiler content.
	 * 
	 * @param topMethod The new top method definition.
	 */
  public void setTopMethod(String topMethod)
  {
    aTopMethod = topMethod;
    if (aTopMethod.equals(""))
      aFoundMethod = true;
    else
      aFoundMethod = false;
  }

  /**
   * Turn on accumulation. Used by the client to control where in a
   * program sample accumulation occurs.
   */
	public void accumulateOn()
	{
	  if (aTimer != null)
	  {
	    aAccumulate = true;
	    aStartTime  = (new Date()).getTime();
	  }
	}

	/**
	 * Turn off accumulation. Used by the client to control where in a
   * program sample accumulation occurs.
	 */
  public void accumulateOff()
  {
    aAccumulate = false;
    aProfilerContent.incrementAccumulationTime((new Date()).getTime() - aStartTime);
  }

  /**
   * Returns the total sample accumulation time as a formatted string.
   * 
   * @return The total sample accumulation time as a formatted string.
   */
  public String getAccumulationTime()
  {
    if (aAccumulate)
      return Globals.elapsedTimeString2(0, (new Date()).getTime() - aStartTime +
                                        aProfilerContent.getAccumulationTime());
    else
      return Globals.elapsedTimeString2(0, aProfilerContent.getAccumulationTime());
  }

  /**
   * Stops this profiler from accumulating any more samples.
   */
  public void stop()
  {
    // cancel timer and turn off accumulation. wait until current
    // accumulation finishes if one is in progress.

    aTimer.cancel();
    accumulateOff();
    while (aAccumulating);

    // set task and timer to null, and output control to off

    aOutputTask      = null;
    aTimer           = null;
    aOutSmplCount    = -1;
    aOutputRepeatFlg = false;
  }

  /**
   * Outputs the profilers content as a formatted string.
   */
  @Override
  public String toString()
  {
    return getAccumulationString(aPrePend);
  }

  /**
   * Clears the profiler contents accumulation making ready to
   * performa a new accumulation.
   */
  public void clearAccumulation()
  {
    aProfilerContent.clearAccumulation();
  }

  /**
   * Prints the profiler content formatted string to standard output.
   */
  public void printAccumulationString()
  {
    System.out.println(getAccumulationString(aPrePend));
  }

  /**
   * Build and return profiler content as a formatted string. Each line is
   * prepended with prepnd.
   * 
   * @param prepnd Buffer string prepended to each line.
   * @return The formatted profiler content string. 
   */
  public String getAccumulationString(String prepnd)
  {
    if (aProfilerContent.getSampleCount() == 0) return "";

    // create buffer to hold profiler content

    StringBuffer sb = new StringBuffer(4096);

    // add header to buffer

    sb.append(NL + prepnd + "Profiler \"" + aName + "\" Output ..." + NL);
    sb.append(prepnd + "    Current Time = " + Globals.getTimeStamp() + NL);
    sb.append(prepnd + "    Elapsed Time = " + getAccumulationTime() + NL);
    sb.append(prepnd + "    Thread Name  = " + aThread.getName() + NL);

    // save accumulation flag to reset at return ... set to false and wait
    // until current accumulation finishes if one is underway

    boolean currentFlag = aAccumulate;
    aAccumulate = false;
    while (aAccumulating);

    // add content

    sb.append(aProfilerContent.getAccumulationString(prepnd));

    // done reset flag and return formatted string

    aAccumulate = currentFlag;
    return sb.toString();
  }

  /**
   * Add the input profilers content to this one.
   * 
   * @param p The input Profiler whose content will be added to this one.
   */
  public void addProfilerContent(Profiler p)
  {
    aProfilerContent.addProfilerContent(p.aProfilerContent);
  }

  /**
   * Add the input ProfilerContent to the one maintained by this Profiler.
   * 
   * @param pc The input ProfilerContent whose content will be added to this
   *           one.
   */
  public void addProfilerContent(ProfilerContent pc)
  {
    aProfilerContent.addProfilerContent(pc);
  }

  /**
   * Returns the Profilers content.
   * 
   * @return The Profilers content.
   */
  public ProfilerContent getProfilerContent()
  {
    return aProfilerContent;
  }
}
