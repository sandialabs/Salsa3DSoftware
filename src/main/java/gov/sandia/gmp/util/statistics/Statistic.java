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
package gov.sandia.gmp.util.statistics;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import gov.sandia.gmp.util.containers.arraylist.ArrayListByte;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListFloat;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.containers.arraylist.ArrayListShort;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * Private container class to store residual statistics. Values include the
 * count, minimum, maximum, mean, and standard deviation, rms, and
 * relative standard deviation.
 * 
 * @author jrhipp
 * 
 */
@SuppressWarnings("serial")
public class Statistic implements Serializable
{
  /**
   * Enum of the various available Statistic types.
   * 
   * @author jrhipp
   * 
   */
  public enum StatType
  {
    MINIMUM, MAXIMUM, ABS_MAXIMUM, MEAN, STANDARD_DEVIATION,
    MEAN_DEVIATION, RELATIVE_STANDARD_DEVIATION, RELATIVE_MEAN_DEVIATION,
    RMS, RELATIVE_STANDARD_VARIANCE, RELATIVE_MEAN_VARIANCE, SUM;
  }

  /**
   * A boolean that is true if the mean and standard deviation have been
   * finalized.
   */
  private boolean aChanged = false;

  /**
   * The number of updates since the last reset.
   */
  private int aCount = 0;

  /**
   * The minimum found thus far since the last reset.
   */
  private double aMinimum = Double.MAX_VALUE;

  /**
   * The maximum found thus far since the last reset.
   */
  private double aMaximum = -Double.MAX_VALUE;

  /**
   * The mean of all input data thus far.
   */
  private double aMean = 0.0;

  /**
   * The standard deviation of all input data thus far.
   */
  private double aStdDev = 0.0;

  /**
   * The root mean square of all input data thus far.
   */
  private double aRMS = 0.0;

  /**
   * The sum of all input data thus far.
   */
  private double aSum = 0.0;

  /**
   * The sum of the squares all input data thus far.
   */
  private double aSumSqr = 0.0;

  /**
   * The mean deviation. Only calculated on array adds when aCount is zero
   * to begin with or when explicitly called using the array input functions
   * of getMeanDeviation(array v).
   */
  private double aMeanDev = 0.0;

  /**
   * Default constructor.
   */
  public Statistic()
  {
    // performs no action
  }

  /**
   * Defines a new Statistic from existing data.
   * 
   * @param count   The number of entries.
   * @param min     The minimum value input.
   * @param max     The maximum value input.
   * @param sum     The sum of all input values.
   * @param sumsqr  The sum of squares of all input values.
   * @param meanDev The mean deviation.
   */
  public Statistic(int count, double min, double max, double sum, double sumsqr,
                   double meanDev)
  {
    aChanged = true;
    aCount   = count;
    aMinimum = min;
    aMaximum = max;
    aSum     = sum;
    aSumSqr  = sumsqr;
    aMeanDev = meanDev;
  }

  /**
   * Sets this Statistic with the values of the input Statistic.
   * 
   * @param s
   *          The input Statistic object that will be set into this one.
   */
  public void set(Statistic s)
  {
    aChanged = s.aChanged;
    aCount   = s.aCount;
    aMinimum = s.aMinimum;
    aMaximum = s.aMaximum;
    aMean    = s.aMean;
    aStdDev  = s.aStdDev;
    aMeanDev = s.aMeanDev;
    aSum     = s.aSum;
    aSumSqr  = s.aSumSqr;
    aRMS     = s.aRMS;
  }

  /**
   * Writes this statistic to the file output buffer.
   * 
   * @param fob The file output buffer into which this statistic is written.
   * @throws IOException
   */
  public void write(FileOutputBuffer fob) throws IOException
  {
    fob.writeBoolean(aChanged);
    fob.writeInt(aCount);
    fob.writeDouble(aMinimum);
    fob.writeDouble(aMaximum);
    fob.writeDouble(aMean);
    fob.writeDouble(aStdDev);
    fob.writeDouble(aMeanDev);
    fob.writeDouble(aSum);
    fob.writeDouble(aSumSqr);
    fob.writeDouble(aRMS);
  }

  /**
   * Reads this statistic from the input file input buffer.
   * 
   * @param fib The input buffer from which this statistic is initialized.
   * @throws IOException
   */
  public void read(FileInputBuffer fib) throws IOException
  {
    aChanged = fib.readBoolean();
    aCount   = fib.readInt();
    aMinimum = fib.readDouble();
    aMaximum = fib.readDouble();
    aMean    = fib.readDouble();
    aStdDev  = fib.readDouble();
    aMeanDev = fib.readDouble();
    aSum     = fib.readDouble();
    aSumSqr  = fib.readDouble();
    aRMS     = fib.readDouble();
  }

  /**
   * Resets the statistics.
   */
  public void reset()
  {
    aChanged = false;
    aCount   = 0;
    aMinimum = Double.MAX_VALUE;
    aMaximum = -Double.MAX_VALUE;
    aSumSqr  = aSum = aMean = aStdDev = aRMS = aMeanDev = 0.0;
  }

  public void set(ArrayList<Double> values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(ArrayList<Double> values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(ArrayList<Double> values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(ArrayList<Double> values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(ArrayListDouble values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(ArrayListDouble values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(ArrayListDouble values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(ArrayListDouble values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(ArrayListFloat values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(ArrayListFloat values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(ArrayListFloat values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(ArrayListFloat values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(ArrayListLong values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(ArrayListLong values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(ArrayListLong values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(ArrayListLong values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(ArrayListInt values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(ArrayListInt values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(ArrayListInt values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(ArrayListInt values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(ArrayListShort values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(ArrayListShort values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(ArrayListShort values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(ArrayListShort values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(ArrayListByte values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(ArrayListByte values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(ArrayListByte values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(ArrayListByte values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(double[] values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(double[] values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(double[] values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }
  
  public void add(Collection<Double> values) {
    values.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).forEach(this::add);
  }

  public void add(double[] values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(float[] values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(float[] values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(float[] values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(float[] values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(long[] values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(long[] values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(long[] values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(long[] values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(int[] values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(int[] values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(int[] values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(int[] values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(short[] values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(short[] values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(short[] values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(short[] values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void set(byte[] values)
  {
    set(DoubleGetTypes.newDoubleGet(values));
  }

  public void set(byte[] values, int beg, int end)
  {
    set(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  public void add(byte[] values)
  {
    add(DoubleGetTypes.newDoubleGet(values));
  }

  public void add(byte[] values, int beg, int end)
  {
    add(DoubleGetTypes.newDoubleGet(values, beg, end));
  }

  /**
   * Sets the statistics to the array defined by the input DoubleGet.
   * 
   * @param dg a DoubleGet array
   */
  public void set(DoubleGet dg)
  {
    reset();
    add(dg);
  }

  /**
   * Adds the array defined by the input DoubleGet to this statistics object.
   * The mean deviation is calculated if aCount == 0 on input.
   * 
   * @param dg a DoubleGet array
   */
  public void add(DoubleGet dg)
  {
    for (int i = 0; i < dg.size(); ++i) add(dg.get(i));
    if (aCount == dg.size())
    {
      aMeanDev = 0.0;
      getStdDev();
      for (int i = 0; i < dg.size(); ++i)
        aMeanDev += Math.abs(dg.get(i) - aMean);
      aMeanDev /= aCount;
    }
    else
      aMeanDev = 0.0;
  }

  /**
   * Increments the count, updates the minimum and maximum values against the
   * input value, and adds the input value to the mean sum and the square of
   * the input value to the standard deviation sum. Update only occurs if the
   * statistic has not been finalized. Calling getMean() or getStdDev() will
   * finalize the statistic until reset() is called.
   * 
   * @param value
   *          The value with which the statistics are updated.
   */
  public void add(double value)
  {
    ++aCount;
    if (value < aMinimum) aMinimum = value;
    if (value > aMaximum) aMaximum = value;
    aSum    += value;
    aSumSqr += value * value;
    aChanged = true;
  }
  
  /**
   * Special case function to use when known values that were previously added
   * are to be removed. Note that this function cannot identify minimum or
   * maximum anymore. Those values can only be set by function add().
   * 
   * @param value The value to be removed from the statistic.
   */
  public void remove(double value)
  {
    --aCount;
    aSum -= value;
    aSumSqr -= value * value;
    aChanged = true;
  }

  /**
   * Sums the result of the input Statistic, s, into this Statistic.
   * 
   * @param s The input Statistic to be added to this Statistic.
   */
  public void add(Statistic s)
  {
    if (s.aCount > 0)
    {
      aCount += s.aCount;
      if (s.aMinimum < aMinimum) aMinimum = s.aMinimum;
      if (s.aMaximum > aMaximum) aMaximum = s.aMaximum;
      aSum += s.aSum;
      aSumSqr += s.aSumSqr;
      aChanged = true;
    }
  }

  /**
   * Scales the current statistics by scl. The minimum, maximum, and sum are
   * multiplied by the input scale. The sum squared is multiplied by the
   * square of the input scale.
   * 
   * @param scl
   *          The value by which the statistics are scaled.
   */
  public void scale(double scl)
  {
    aMinimum *= scl;
    aMaximum *= scl;
    aSum     *= scl;
    aSumSqr  *= scl * scl;
    aChanged = true;
  }

  /**
   * Returns the input count.
   * 
   * @return The input count.
   */
  public int getCount()
  {
    return aCount;
  }

  /**
   * Return total sum.
   * 
   * @return Total sum.
   */
  public double getSum()
  {
    return aSum;
  }

  /**
   * Return total sum of squares.
   * 
   * @return Total sum of squares.
   */
  public double getSumSqr()
  {
    return aSumSqr;
  }

  /**
   * Returns the input minimum thus far from all entries using the method
   * add(double value).
   * 
   * @return The input minimum.
   */
  public double getMinimum()
  {
    if (aCount == 0)
      return 0.0;
    else
      return aMinimum;
  }

  /**
   * Returns the input maximum thus far from all entries using the method
   * add(double value).
   * 
   * @return The input maximum.
   */
  public double getMaximum()
  {
    if (aCount == 0)
      return 0.0;
    else
      return aMaximum;
  }

  /**
   * Returns the largest absolute value of the minimum and maximum.
   * 
   * @return The largest absolute value of the minimum and maximum.
   */
  public double getAbsMaximum()
  {
    if (aCount == 0)
      return 0.0;
    else
      return max(abs(aMinimum), aMaximum);
  }

  /**
   * Returns the largest absolute value from the mean.
   * 
   * @return The largest absolute value from the mean.
   */
  public double getAbsMaxFromMean()
  {
    if (aChanged) updateResult();
    if (aCount == 0.0)
      return 0.0;
    else
      return max(abs(aMinimum - aMean), abs(aMaximum - aMean));
  }

  /**
   * Returns the mean. If the object has not yet been updated the mean will be
   * evaluated before returning the result.
   * 
   * @return The mean.
   */
  public double getMean()
  {
    if (aChanged) updateResult();
    return aMean;
  }

  /**
   * Returns the standard deviation. If the object has not yet been updated
   * the standard deviation will be evaluated before returning the result.
   * 
   * @return The standard deviation
   */
  public double getStdDev()
  {
    if (aChanged) updateResult();
    return aStdDev;
  }

  public double getMeanDeviation()
  {
    return aMeanDev;
  }

  public double getRelativeMeanDeviation()
  {
    double u = getMean();
    if (u != 0.0)
      return aMeanDev / u;
    else
      return Double.MAX_VALUE;
  }

  /**
   * Returns the relative standard deviation (sigma/mean). If the mean is zero
   * then Double.MAX_VALUE is returned.
   * 
   * @return The relative standard deviation (sigma/mean).
   */
  public double getRelativeStdDev()
  {
    double u = getMean();
    if (u != 0.0)
      return getStdDev() / u;
    else
      return Double.MAX_VALUE;
  }

  /**
   * Returns the variance. If the object has not yet been updated
   * the standard deviation will be evaluated before returning the result.
   * 
   * @return The standard deviation
   */  
  public double getVariance()
  {
    return getStdDev() * getStdDev();
  }

  /**
   * Returns the relative variance (sigma^2/rms).
   * 
   * @return The relative standard variance (sigma^2/rms).
   */  
  public double getRelativeVariance()
  {
    return getVariance() / getRMS();
  }

  public double getRelativeMeanVariance()
  {
    return aMeanDev * aMeanDev / getRMS();
  }

  /**
   * Returns the rms. If the object has not yet been updated the rms result
   * will be evaluated before returning the result.
   * 
   * @return The RMS.
   */
  public double getRMS()
  {
    if (aChanged) updateResult();
    return aRMS;
  }

  /**
   * Returns the difference between this absolute maximum and the absolute
   * maximum of the input Statistic.
   * 
   * @param s
   *          The input Statistic whose absolute maximum will be differenced
   *          with this Statistics absolute maximum.
   * @return The difference between this absolute maximum and the absolute
   *         maximum of the input Statistic.
   */
  public double getDiffAbsMaximum(Statistic s)
  {
    return getAbsMaximum() - s.getAbsMaximum();
  }

  /**
   * Returns the difference between this mean and the mean of the input
   * Statistic.
   * 
   * @param s
   *          The input Statistic whose mean will be differenced with this
   *          Statistics mean.
   * @return The difference between this mean and the mean of the input
   *         Statistic.
   */
  public double getDiffMean(Statistic s)
  {
    return getMean() - s.getMean();
  }

  /**
   * Returns the difference between this standard deviation and the standard
   * deviation of the input Statistic.
   * 
   * @param s
   *          The input Statistic whose standard deviation will be differenced
   *          with this Statistics standard deviation.
   * @return The difference between this standard deviation and the standard
   *         deviation of the input Statistic.
   */
  public double getDiffStdDev(Statistic s)
  {
    return getStdDev() - s.getStdDev();
  }

  /**
   * Returns the difference between this rms and the rms of the input
   * Statistic.
   * 
   * @param s
   *          The input Statistic whose rms will be differenced with this
   *          Statistics rms.
   * @return The difference between this rms and the rms of the input
   *         Statistic.
   */
  public double getDiffRMS(Statistic s)
  {
    return getRMS() - s.getRMS();
  }

  public double getResult(StatType st)
  {
    if (st == StatType.MINIMUM)
      return getMinimum();
    else if (st == StatType.MAXIMUM)
      return getMaximum();
    else if (st == StatType.ABS_MAXIMUM)
      return getAbsMaximum();
    else if (st == StatType.MEAN)
      return getMean();
    else if (st == StatType.STANDARD_DEVIATION)
      return getStdDev();
    else if (st == StatType.RMS)
      return getRMS();
    else if (st == StatType.MEAN_DEVIATION)
      return getMeanDeviation();
    else if (st == StatType.RELATIVE_MEAN_DEVIATION)
      return getRelativeMeanDeviation();
    else if (st == StatType.RELATIVE_STANDARD_DEVIATION)
      return getRelativeStdDev();
    else if (st == StatType.RELATIVE_MEAN_VARIANCE)
      return getRelativeMeanVariance();
    else if (st == StatType.RELATIVE_STANDARD_VARIANCE)
      return getRelativeVariance();
    else if (st == StatType.SUM)
      return getSum();
    else
      return 0.0;
  }

  public double getSingleEntryResult(double v, StatType st)
  {
    if (st == StatType.MINIMUM)
      return v;
    else if (st == StatType.MAXIMUM)
      return v;
    else if (st == StatType.ABS_MAXIMUM)
      return v;
    else if (st == StatType.MEAN)
      return v;
    else if (st == StatType.STANDARD_DEVIATION)
      return 0.0;
    else if (st == StatType.RMS)
      return Math.abs(v);
    else if (st == StatType.SUM)
      return v;
    else if (st == StatType.MEAN_DEVIATION)
      return 0.0;
    else if (st == StatType.RELATIVE_MEAN_DEVIATION)
      return 0.0;
    else if (st == StatType.RELATIVE_STANDARD_DEVIATION)
      return 0.0;
    else if (st == StatType.RELATIVE_MEAN_VARIANCE)
      return 0.0;
    else if (st == StatType.RELATIVE_STANDARD_VARIANCE)
      return 0.0;
    else
      return 0.0;
  }
//  MINIMUM, MAXIMUM, ABS_MAXIMUM, MEAN, STANDARD_DEVIATION, RMS,
//  MEAN_DEVIATION, RELATIVE_MEAN_DEVIATION, RELATIVE_STANDARD_DEVIATION,
//  RELATIVE_MEAN_VARIANCE, RELATIVE_STANDARD_VARIANCE;

  /**
   * Private function to finalized the mean and standard deviation. The
   * Statistic is only finalized if at least one entry was added with the
   * function call add(double value). If aCount == 1 the standard deviation is
   * left as 0.0.
   */
  private void updateResult()
  {
    aChanged = false;
    if (aCount > 0)
    {
      aMean = aSum / aCount;
      aRMS = sqrt(aSumSqr / aCount);
      if (aCount > 1)
      {
        double d = (aSumSqr - aSum * aSum / aCount) / (aCount - 1);
        if (d > 0.0) aStdDev = sqrt(d);
      }
    }
  }
}
