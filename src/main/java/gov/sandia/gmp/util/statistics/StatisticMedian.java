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

import java.util.Arrays;

import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;

/**
 * An extension of Statistic that stores input values for Statistic in a
 * sorted manner form which the median can be extracted. The object currently
 * overrides add(double value) and reset(). It should also override read() and
 * write() but as yet does not.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class StatisticMedian extends Statistic
{ 
	/**
	 * True if additions to values0 have been made since the last call to
	 * getMedian.
	 */
	private boolean changed = true;

	/**
	 * If true values are set into storage as abs(value).
	 */
	private boolean setAbsolute = false;
	
	/**
	 * The set of values saved in an ArrayListDouble
	 */
  ArrayListDouble values = new ArrayListDouble();

  /**
   * Standard constructor.
   * 
   * @param absolute Sets setAbsolute with this value.
   */
  public StatisticMedian(boolean absolute)
  {
  	super();
  	setAbsolute = absolute;
  }

  /**
   * Adds the input value into the Statistic and into the sorted values map.
   * If setAbsolute is true the values are added into the map as the absolute
   * value of val.
   */
  @Override
  public void add(double val)
  {
  	super.add(val);
  	changed = true;
  	if (setAbsolute)
  		values.add(Math.abs(val));
  	else
  		values.add(val);
  }

  /**
   * Returns the median of the input values.
   * 
   * @return The median of the input values.
   */
  public double getMedian()
  {
  	if (getCount() == 0.0)
  		return 0.0;
  	else
  	{
  		// sort if changed is true

  		if (changed)
  		  Arrays.sort(values.getArray(), 0, values.size());

	  	changed = false;
  		int midIndex = (int) (values.size() / 2);
	  	boolean even = (values.size() % 2 == 0);

	  	if (even)
	  		return (values.get(midIndex-1) + values.get(midIndex)) / 2.0;
	  	else
	  		return values.get(midIndex);
  	}
  }

  /**
   * Resets the Statistic to empty.
   */
  @Override
  public void reset()
  {
  	super.reset();
  	values.clear();
  	changed = true;
  }
}
