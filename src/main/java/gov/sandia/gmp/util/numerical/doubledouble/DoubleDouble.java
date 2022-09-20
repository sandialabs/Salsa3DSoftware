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
package gov.sandia.gmp.util.numerical.doubledouble;

import java.io.Serializable;

/**
 * A DoubleDouble precision class that maintains roughly DoubleDouble precision
 * floating point in a hi,lo representation of two double intrinsic values.
 * Effectively, some real floating point number, say r, is defined such that
 * r = hi + lo where the lowest order digits of accuracy are stored in lo.
 * Operations to create, add, substract, multiply, and divide are provided.
 * These function were all taken from the C++ DoubleDouble, QuadDouble
 * computation package
 * 
 *   | QUAD-DOUBLE/DOUBLE-DOUBLE COMPUTATION PACKAGE                        |
 *   |                                                                      |
 *   | Yozo Hida        U.C. Berkeley               yozo@cs.berkeley.edu    |
 *   | Xiaoye S. Li     Lawrence Berkeley Natl Lab  xiaoye@nersc.gov        |
 *   | David H. Bailey  Lawrence Berkeley Natl Lab  dhbailey@lbl.gov        |
 *   |                                                                      |
 *   | Revised  2007-01-10  Copyright (c) 2005-2007                         |
 *   
 *   This work was supported by the Director, Office of Science, Division
 *   of Mathematical, Information, and Computational Sciences of the
 *   U.S. Department of Energy under contract number DE-AC02-05CH11231.
 *   
 * The implementation was converted to Java and provides numeric types of about
 * twice the precision of the IEEE double (106 mantissa bits, or approximately
 * 32 decimal digits)
 *     
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class DoubleDouble implements Serializable
{
  /**
   * default number splitter to split a standard double into a high and low
   * component representation.
   */
  private static double QD_SPLITTER  = 134217729.0; // 2^27 + 1

  /**
   * A large number, threshold to handle splitting differently when an
   * input double exceeds this threshold (in magnitude).
   */
  //private static double QD_THRESHOLD = 6.69692879491417e+299; // = 2^996

  /**
   * The high bit component.
   */
  public double hi = 0.0;

  /**
   * The low bit component.
   */
  public double lo = 0.0;

  /**
   * Default constructor
   */
  public DoubleDouble()
  {
    // no action
  }

  /**
   * Standard constructor builds a DoubleDouble from the input double.
   * 
   * @param a
   */
  public DoubleDouble(double a)
  {
    hi = a;
  }

  /**
   * Standard constructor builds a DoubleDouble given the high and low
   * components of another DoubleDouble.
   * 
   * @param a The high component.
   * @param b The low component.
   */
  public DoubleDouble(double a, double b)
  {
    hi = a;
    lo = b;
  }

  /**
   * Standard constructor builds a DoubleDouble copy from the input
   * DoubleDouble.
   * 
   * @param a Input DoubleDouble for which a copy will be constructed.
   */
  public DoubleDouble(DoubleDouble a)
  {
    hi = a.hi;
    lo = a.lo;
  }

  //**** Basic ****************************************************************

  private void quickTwoSum(double a, double b, DoubleDouble c)
  {
    c.hi = a + b;
    c.lo = b - (c.hi - a);
  }

  /*
  private void quickTwoDiff(double a, double b, DoubleDouble c)
  {
    c.hi = a - b;
    c.lo = (a - c.hi) - b;
  }
  */

  private void twoSum(double a, double b, DoubleDouble c)
  {
    c.hi = a + b;
    double bb = c.hi - a;
    c.lo = (a - (c.hi - bb)) + (b - bb);
  }

  private void twoDiff(double a, double b, DoubleDouble c)
  {
    c.hi = a - b;
    double bb = c.hi - a;
    c.lo = (a - (c.hi - bb)) - (b + bb);
  }

  private void split(double a, DoubleDouble b)
  {
    b.hi  = QD_SPLITTER * a;
    b.hi += a - b.hi;
    b.lo  = a - b.hi;
  }

  /*
  private void accSplit(double a, DoubleDouble b)
  {
    if ((a > QD_THRESHOLD) || (a < -QD_THRESHOLD))
    {
      a *= 3.7252902984619140625e-09;  // 2^-28
      split(a, b);
      b.hi *= 268435456.0;             // 2^28
      b.lo *= 268435456.0;             // 2^28
    }
    else
      split(a, b);
  }
  */

  private void twoProd(double a, double b, DoubleDouble c)
  {
    DoubleDouble aD = new DoubleDouble();
    DoubleDouble bD = new DoubleDouble();

    split(a, aD);
    split(b, bD);
    c.hi = a * b;
    c.lo = ((aD.hi * bD.hi - c.hi) + aD.hi * bD.lo + aD.lo * bD.hi) + aD.lo * bD.lo;
  }

  private void twoSqr(double a, DoubleDouble b)
  {
    DoubleDouble aD = new DoubleDouble();

    split(a, aD);
    b.hi = a * a;
    b.lo = ((aD.hi * aD.hi - b.hi) + 2.0 * aD.hi * aD.lo) + aD.lo * aD.lo;
  }

  //**** Simple Functions *****************************************************

  /**
   * Returns the high component. 
   * 
   * @return The High component.
   */
  public double getDouble()
  {
    return hi;
  }

  /**
   * Returns the low component.
   * 
   * @return The low component.
   */
  public double getError()
  {
    return lo;
  }

  /**
   * Negates this DoubleDouble
   * this = -this;
   */
  public void negate()
  {
    hi = -hi;
    lo = -lo;
  }

  /**
   * Equality test.
   * 
   * @return true if this.hi == a.hi and this.lo == a.lo.
   */
  public boolean equals(DoubleDouble a)
  {
    return ((hi == a.hi) && (lo == a.lo) ? true : false);
  }

  //**** Additions ************************************************************
  
  /**
   * Adds a + b and stores into this.
   * this = a + b;
   */
  public void add(double a, double b)
  {
    twoSum(a, b, this);
  }

  /**
   * Adds a + b and stores into this.
   * this = a + b;
   */
  public void add(DoubleDouble a, double b)
  {
    twoSum(a.hi, b, this);
    lo += a.lo;
    quickTwoSum(hi, lo, this);
  }

  /**
   * Adds a + b and stores into this.
   * this = a + b;
   */
  public void add(DoubleDouble a, DoubleDouble b)
  {
    DoubleDouble t = new DoubleDouble();

    twoSum(a.hi, b.hi, this);
    twoSum(a.lo, b.lo, t);
    lo += t.hi;
    quickTwoSum(hi, lo, this);
    lo += t.lo;
    quickTwoSum(hi, lo, this);
  }

  /**
   * Adds a + this and stores into this.
   * this += a;
   */
  public void add(double a)
  {
    add(new DoubleDouble(this), a);
  }

  /**
   * Adds a + this and stores into this.
   * this += a;
   */
  public void add(DoubleDouble a)
  {
    add(new DoubleDouble(this), a);
  }

  // tests
  //   add(double a, double b)                     this  = a + b
  //   add(DoubleDouble a, double b)               this  = a + b
  //   add(DoubleDouble a, DoubleDouble b)         this  = a + b 
  //   add(double a)                               this += a
  //   add(DoubleDouble a)                         this += a
  //   addFast(double[] t, double ahi, double alo) t    += a

  /**
   * Static function that performs t += a. All temporaries are removed to
   * maximize performance. This function is called by LSQRDD to perform
   * partial vector sums during the concurrent two norm calculation.
   */
  public static void addFast(double[] t, double ahi, double alo)
  {
    double chi, clo, dhi, dlo, bb, bhi, blo;

    // performs t(hi,lo) += a(hi,lo)
    // first perform twoSum on t[0], ahi then another on t[1], alo
    
    chi = t[0] + ahi;
    bb  = chi - t[0];
    clo = (t[0] - (chi - bb)) + (ahi - bb);

    dhi = t[1] + alo;
    bb  = dhi - t[1];
    dlo = (t[1] - (dhi - bb)) + (alo - bb);

    // now perform quickTwoSum on chi, clo twice to include
    // dhi and dlo and store final result in t[0], t[1]

    clo += dhi;
    bhi = chi + clo;
    blo = clo - (bhi - chi) + dlo;
    t[0] = bhi + blo;
    t[1] = blo - (t[0] - bhi);
  }

  //**** Subtractions *********************************************************

  /**
   * Subtracts a - b and stores into this.
   * this = a - b;
   */
  public void subtract(double a, double b)
  {
    twoDiff(a, b, this);
  }

  /**
   * Subtracts a - b and stores into this.
   * this = a - b;
   */
  public void subtract(DoubleDouble a, double b)
  {
    twoDiff(a.hi, b, this);
    lo += a.lo;
    quickTwoSum(hi, lo, this);
  }

  /**
   * Subtracts a - b and stores into this.
   * this = a - b;
   */
  public void subtract(double a, DoubleDouble b)
  {
    subtract(b, a);
    negate();
  }

  /**
   * Subtracts a - b and stores into this.
   * this = a - b;
   */
  public void subtract(DoubleDouble a, DoubleDouble b)
  {
    DoubleDouble t = new DoubleDouble();

    twoDiff(a.hi, b.hi, this);
    twoDiff(a.lo, b.lo, t);
    lo += t.hi;
    quickTwoSum(hi, lo, this);
    lo += t.lo;
    quickTwoSum(hi, lo, this);
  }

  /**
   * Subtracts a from this and stores into this.
   * this -= a;
   */
  public void subtract(double a)
  {
    subtract(new DoubleDouble(this), a);
  }

  /**
   * Subtracts a from this and stores into this.
   * this -= a;
   */
  public void subtract(DoubleDouble a)
  {
    subtract(new DoubleDouble(this), a);
  }

  //**** Multiplies ***********************************************************

  /**
   * Multiplies a * b and stores into this.
   * this = a * b;
   */
  public void mult(double a, double b)
  {
    twoProd(a, b, this);
  }

  /**
   * Multiplies a * b and stores into this.
   * this = a * b;
   */
  public void mult(DoubleDouble a, double b)
  {
    twoProd(a.hi, b, this);
    lo += (a.lo * b);
    quickTwoSum(hi, lo, this);
  }

  /**
   * Multiplies a * b and stores into this.
   * this = a * b;
   */
  public void mult(DoubleDouble a, DoubleDouble b)
  {
    twoProd(a.hi, b.hi, this);
    lo += (a.hi * b.lo + a.lo * b.hi);
    quickTwoSum(hi, lo, this);
  }

  /**
   * Multiplies a times this and stores into this.
   * this *= a;
   */
  public void mult(double a)
  {
    mult(new DoubleDouble(this), a);
  }

  /**
   * Multiplies a times this and stores into this.
   * this *= a;
   */
  public void mult(DoubleDouble a)
  {
    mult(new DoubleDouble(this), a);
  }

  /**
   * Static function that performs t *= a. All temporaries are removed to
   * maximize performance. This function is called by LSQRDD to perform
   * partial vector scaling during the concurrent scale calculation.
   */
  public static void multFast(double[] t, double ahi, double alo)
  {
    double chi, clo, bhi, blo, hi, lo;

    // perform y *= scl in DoubleDouble
    // perform twoProd ... first split y[0] and sclhi

    chi  = QD_SPLITTER * t[0];
    chi += t[0] - chi;
    clo  = t[0] - chi;
    bhi  = QD_SPLITTER * ahi;
    bhi += ahi - bhi;
    blo  = ahi - bhi;

    // finish twoProd storing result in hi, lo

    hi = t[0] * ahi;
    lo = ((chi * bhi - hi) + chi * blo + clo * bhi) + clo * blo +
         (t[0] * alo + t[1] * ahi);

    // perform quickTwoSum on hi,lo storing result in y

    t[0] = hi + lo;
    t[1] = lo - (t[0] - hi);
  }

  /**
   * Static function that performs t += a * b. All temporaries are removed to
   * maximize performance. This function is called by LSQRDD to perform
   * partial vector sparse matrix updates (APROD).
   */
  public static void addMultFast(double[] t, double a, double bhi, double blo)
  {
    double sahi, salo, sbhi, sblo, sthi, stlo;
    double chi, clo, dhi, dlo, bb;

    // split va[i] and hivi (first part of twoProd ... split)

    sahi  = QD_SPLITTER * a;
    sahi += a - sahi;
    salo  = a - sahi;
    sbhi  = QD_SPLITTER * bhi;
    sbhi += bhi - sbhi;
    sblo  = bhi - sbhi;

    // store twoprod results

    chi = a * bhi;
    clo = ((sahi * sbhi - chi) + sahi * sblo + salo * sbhi) +
          salo * sblo + blo * a;

    // perform quickTwoSum

    dhi = chi + clo;
    dlo = clo - (dhi - chi);

    // perform twoSum on t[0], dhi then another on t[1], dlo
    
    chi = t[0] + dhi;
    bb  = chi - t[0];
    clo = (t[0] - (chi - bb)) + (dhi - bb);

    dhi = t[1] + dlo;
    bb  = dhi - t[1];
    dlo = (t[1] - (dhi - bb)) + (dlo - bb);

    // now perform quickTwoSum on chi, clo twice to include
    // dhi and dlo and store final result in t[0], t[1]

    clo += dhi;
    sthi = chi + clo;
    stlo = clo - (sthi - chi) + dlo;
    t[0] = sthi + stlo;
    t[1] = stlo - (t[0] - sthi);
  }

  //**** Divisions ************************************************************

  /**
   * Divides a / b and stores into this.
   * this = a / b;
   */
  public void div(double a, double b)
  {
    DoubleDouble q = new DoubleDouble();
    DoubleDouble p = new DoubleDouble();

    q.hi = a / b;
    twoProd(q.hi, b, p);
    twoDiff(a, p.hi, this);
    lo -= p.lo;
    q.lo = (hi + lo) / b;
    quickTwoSum(q.hi, q.lo, this);
  }

  /**
   * Divides a / b and stores into this.
   * this = a / b;
   */
  public void div(DoubleDouble a, double b)
  {
    DoubleDouble q = new DoubleDouble();
    DoubleDouble p = new DoubleDouble();

    q.hi = a.hi / b;
    twoProd(q.hi, b, p);
    twoDiff(a.hi, p.hi, this);
    lo += a.lo - p.lo;
    q.lo = (hi + lo) / b;
    quickTwoSum(q.hi, q.lo, this);
  }

  /**
   * Divides a / b and stores into this.
   * this = a / b;
   */
  public void div(DoubleDouble a, DoubleDouble b)
  {
    DoubleDouble q = new DoubleDouble();
    DoubleDouble r = new DoubleDouble();
    double q3;

    q.hi = a.hi / b.hi;
    r.mult(b, q.hi);
    subtract(a, r);
    q.lo = hi / b.hi;
    r.mult(b, q.lo);
    subtract(r);
    q3 = hi / b.hi;
    quickTwoSum(q.hi, q.lo, this);
    add(q3);
  }

  /**
   * Divides a / b and stores into this.
   * this = a / b;
   */
  public void div(double a, DoubleDouble b)
  {
    div(new DoubleDouble(a), b);
  }

  /**
   * Inverts a (1/a) and stores into this.
   * this = 1.0 / a;
   */
  public void inverse(DoubleDouble a)
  {
    div(1.0, a);
  }

  // this = 1.0 / this
  /**
   * Inverts this and stores into this.
   * this = 1.0 / this;
   */
  public void inverse()
  {
    div(1.0, new DoubleDouble(this));
  }

  /**
   * Divides this by a and stores into this.
   * this /= a;
   */
  public void div(double a)
  {
    div(new DoubleDouble(this), a);
  }

  /**
   * Divides this by a and stores into this.
   * this /= a;
   */
  public void div(DoubleDouble a)
  {
    div(new DoubleDouble(this), a);
  }

  //**** Complex Functions ****************************************************

  /**
   * Squares a and stores into this.
   * this = a * a;
   */
  public void sqr(double a)
  {
    twoSqr(a, this);
  }

  /**
   * Squares a and stores into this.
   * this = a * a;
   */
  public void sqr(DoubleDouble a)
  {
    DoubleDouble p = new DoubleDouble();

    twoSqr(a.hi, p);
    p.lo += 2.0 * a.hi * a.lo + a.lo * a.lo;
    quickTwoSum(p.hi, p.lo, this);
  }

  /**
   * Squares this and stores into this.
   * this *= this;
   */
  public void sqr()
  {
    sqr(new DoubleDouble(this));
  }

  /**
   * Static function that performs t += a * a. All temporaries are removed to
   * maximize performance. This function is called by LSQRDD to perform
   * partial vector two norm calculations.
   */
  public static void addSqrFast(double[] t, double ahi, double alo)
  {
    double sahi, salo, bhi, blo, chi, clo, dhi, dlo, bb;

    // split vhi[i] (first part of twoSqr ... split)

    sahi  = QD_SPLITTER * ahi;
    sahi += ahi - sahi;
    salo  = ahi - sahi;

    // store twoSqr results

    chi = ahi * ahi;
    clo = ((sahi * sahi - chi) + 2.0 * sahi * salo) + salo * salo +
          2.0 * ahi * alo + alo * alo;

    // perform quickTwoSum

    dhi = chi + clo;
    dlo = clo - (dhi - chi);

    // perform twoSum on hi, dhi then another on lo, dlo
    
    chi = t[0] + dhi;
    bb  = chi - t[0];
    clo = (t[0] - (chi - bb)) + (dhi - bb);

    dhi = t[1] + dlo;
    bb  = dhi - t[1];
    dlo = (t[1] - (dhi - bb)) + (dlo - bb);

    // now perform quickTwoSum on chi, clo twice to include
    // dhi and dlo and store final result in hi, lo

    clo += dhi;
    bhi = chi + clo;
    blo = clo - (bhi - chi) + dlo;
    t[0] = bhi + blo;
    t[1] = blo - (t[0] - bhi);
  }

  /**
   * Takes the square root of a and stores into this.
   * this = sqrt(a);
   */
  public void sqrt(DoubleDouble a)
  {
    if ((a.hi == 0.0) && (a.lo == 0.0))
      hi = lo = 0.0;
    else if (a.hi < 0.0)
    {
      hi = lo = Double.NaN;
    }
    else
    {
      DoubleDouble tmp = new DoubleDouble();
      double x = 1.0 / Math.sqrt(a.hi);
      double ax = a.hi * x;
      tmp.sqr(ax);
      subtract(a, tmp);
      add(ax, hi * (x * 0.5));
    }
  }

  /**
   * Takes the square root of a and stores into this.
   * this = sqrt(a);
   */
  public void sqrt(double a)
  {
    sqrt(new DoubleDouble(a));
  }

  /**
   * Takes the square root of this and stores into this.
   * this = sqrt(this);
   */
  public void sqrt()
  {
    sqrt(new DoubleDouble(this));
  }
}
