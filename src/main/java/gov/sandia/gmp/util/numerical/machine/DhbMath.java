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
package gov.sandia.gmp.util.numerical.machine;

import static java.lang.Math.sqrt;

/**
 * This class determines the limits of the hardware floating point
 * representation. The machine radix, precision and the default
 * numerical precision (sqrt(machine precision)) are evaluated.
 * It is a final static representation and will only be evaluated
 * once per application regardless of the number of times it is
 * used within the application.
 *
 * @author Didier H. Besset
 */
public final class DhbMath
 {
  /** Radix used by floating-point numbers. */
  private final static int radix = computeRadix();
  /** Largest positive value which, when added to 1.0, yields 1.0 */
  private final static double machinePrecision = computeMachinePrecision();
  /** Typical meaningful precision for numerical calculations. */
  private final static double defaultNumPrecision = sqrt(machinePrecision);

  /** Calculate the machine radix */
  private static int computeRadix()
  {
    int r = 0;
    double a = 1.0d;
    double tmp1, tmp2;
    do
    {
       a += a;
       tmp1 = a + 1.0d;
       tmp2 = tmp1 - a;
    } while (tmp2 - 1.0d != 0.0d);

    double b = 1.0d;
    while (r == 0)
    {
      b += b;
      tmp1 = a + b;
      r = (int)(tmp1 - a);
    }

    return r;
  }

  /** Calculate the machine precision */
  private static double computeMachinePrecision()
  {
    double floatingRadix = getRadix();
    double inverseRadix = 1.0d / floatingRadix;
    double machPrec = 1.0d;
    double tmp = 1.0d + machPrec;

    while (tmp - 1.0d != 0.0d)
    {
      machPrec *= inverseRadix;
      tmp = 1.0d + machPrec;
    }

    return machPrec;
  }

  /**
   * @return machine radix.
   */
  public static int getRadix()
  {
    return radix;
  }

  /**
   * @return machine precision.
   */
  public static double getMachinePrecision()
  {
    return machinePrecision;
  }

  /**
   * @return machine default precision.
   */
  public static double defaultNumericalPrecision()
  {
    return defaultNumPrecision;
  }

  /**
   * @return true if the difference between a and b is less than
   * the default numerical precision
   */
  public static boolean equals(double a, double b)
  {
    return equals(a, b, defaultNumericalPrecision());
  }

  /**
   * @return true if the relative difference between a and b is
   * less than precision
   */
  public static boolean equals(double a, double b, double precision)
  {
    double norm = Math.max(Math.abs(a), Math.abs(b));
    return norm < precision || Math.abs(a - b) < precision * norm;
  }
}
