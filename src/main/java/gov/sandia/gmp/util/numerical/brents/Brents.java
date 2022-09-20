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
package gov.sandia.gmp.util.numerical.brents;

import static gov.sandia.gmp.util.numerical.machine.DhbMath.getMachinePrecision;
import static java.lang.Math.abs;

import java.io.Serializable;

/**
 * <p>Title: Brents function zero and minimum/maximum searcher.</p>
 *
 * <p> Brents defines the standard brents algorithm (aka zeroin) that finds
 * functional zeros between bracketing abscissas. It also defines the standard
 * extrema search functions minF and maxF that finds associated minima and
 * maxima between input bracketing abscissas. These functions are essentially
 * those provided by Numerical Recipes (Press, Flannery, Teukolsky, and
 * Vetterling), namely, zBrent and Brent. The functions have been converted to
 * Java and polished numerically to eliminate small performance degradations.
 *
 * <p> The Methods can be used by defining some object that contains the
 * functional definition requiring zeros or minima or maxima to be discovered.
 * By implementing the function bFunc(double x) in the object, and by using the
 * interface BrentsFunction, the object can utilize the Brents functions to
 * find the required zero(s) or extema value(s).</p>
 *
 * <p> For example suppose some function is defined in object AObj for which
 * zeros are desired. Simply define AObj as implementing interface
 * BrentsFunction and add the function double bFunc(double x) to wrap the
 * desired function definition or implement it directly within. Then
 * create a Brents object (B) and set the desired accuracy tolerance for
 * converging on the zero. Finally, call B.zeroF(ax, bx, AObj) which
 * returns the value x between ax and bx that zeros the function AObj.bFunc(x).
 * If many calls using the same AObj are to be made the function can be
 * set directly within the Brents object using the call B.setFunction(AObj).
 * Then the function call B.zeroF(ax, bx) will return the zero.
 *
 * <p> Similar functionality exists for finding minimum and maximum values
 * between two input abscissas. These functions are defined by
 * B.minF(ax, bx, AObj), B.minF(ax, bx), B.maxF(ax, bx, AObj), and
 * B.maxF(ax, bx). Here the search limits are again defined by ax and bx.
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Sandia National Laboratories</p>
 *
 * <p>Author: Jim Hipp
 * <p>Version 1.0.0
 *
 * @see numerical.brents.BrentsTest
 */
@SuppressWarnings("serial")
public class Brents implements Serializable
{
  /**
   * BrentsFunction object containing the definition function bFunc(x)
   * used by the zero-in and extrema search processes.
   */
  private BrentsFunction  bF = null;

  /**
   * The search accuracy tolerance used by the zero-in and extrema search
   * processes. This value can be zero in which case machine precision
   * zeros are returned. The value should be no smaller than
   * DhbMath.defaultNumericalPrecision() for minF and maxF functions.
   */
  private double          bTol = 1.0e-6;

  /**
   * Flag used to indicate if a minimum or maximum will be found in the
   * private extrema search function extremaF. If set to 1.0 a minimum is
   * discovered while a -1.0 setting causes a maximum to be found. If
   * an extrema does not exist the smallest (minimum search) or largest
   * (maximum search) function value at the range extremities will be
   * returned.
   */
  private double          bMinMaxFlg = 1.0;

  /**
   * Maximum number of allowed iterations in extremaF before throwing
   * a BrentsException.
   */
  private int             bITMAX = 100;

  /**
   * Contains the extrema abscissa following a call to minF or maxF.
   */
  private double          bExAbs = 0.0;

  /**
   * Constant used by extremaF (Golden ratio).
   */
  private final double    bCGOLD = 0.3819660112501051;

  /**
   * Minimum machine tolerance used by extremaF.
   */
  private final double    bZEPS  = 1.0e-12;

  /**
   * <p> Obtains a function zero within the given range ax to bx. The object to
   * be zeroed is set into the internal object bF which defines the implements
   * the function bFunc(double x).
   *
   * <p> The function uses the user defined tolerance bTol for root accuracy.
   * If set to zero the root is found to near machine precision.
   *
   * @see #zeroF(double, double, BrentsFunction)
   *
   * @param ax Left hand range limit.
   * @param bx Right hand range limit.
   * @return Root estimate with an accuracy of 4*EPSILON*abs(x) + tol.
   *
   */
  public double zeroF(double ax, double bx)
        throws Exception
    {
    return zeroF(ax, bx, bF);
  }

 /**
  * <p> Obtains a function zero within the given range ax to bx. The function
  * to be zeroed is defined by the input object f that inherits the interface
  * BrentsInterface. The object implements the function bFunc(double x)
  * which is called to perform the zero in procedure.
  *
  * <p> The function uses the user defined tolerance bTol for root accuracy.
  * If set to zero the root is found to near machine precision.
  *
  * <p> The function makes use of a bisection procedure combined with
  * linear or quadric inverse interpolation. At every step the program
  * operates on three abscissa - a, b, and c, defined as
  *
  * <p>b - the last and the best approximation to the root
  * <p>a - the last but one approximation
  * <p>c - the last but one or even earlier approximation than a such that
  *        1) |f(b)| <= |f(c)|
  *        2) f(b) and f(c) have opposite signs, i.e. b and c confine
  *           the root
  *
  * <p> At every step zeroF selects one of the two new approximations, the
  * former being obtained by the bisection procedure and the latter
  * resulting in the interpolation (if a,b, and c are all different
  * the quadric interpolation is utilized, otherwise the linear one is
  * used). If the latter (i.e. obtained by the interpolation) point is
  * reasonable (i.e. lies within the current interval [b,c] not being
  * too close to the boundaries) it is accepted. The bisection result
  * is used in the other case. Therefore, the range of uncertainty is
  * ensured to be reduced at least by the factor 1.6
  *
  * <p> Original Algorithm
  *   G.Forsythe, M.Malcolm, C.Moler, Computer methods for mathematical
  *   computations. M., Mir, 1980, p.180 of the Russian edition
  *
  * @param ax Left hand range limit.
  * @param bx Right hand range limit.
  * @param f The object implementing the function bFunc(x).
  * @return Root estimate with an accuracy of 4*EPSILON*abs(x) + tol.
  */
  public double zeroF(double ax, double bx, BrentsFunction f)
         throws Exception
  {
    double a, b, c, fa, fb, fc, p, q, cb, new_step, prev_step, tol_act;
    final double EPSILON = getMachinePrecision();
    final double TwoEPSILON = 2.0 * EPSILON;
    final double HalfbTol   = 0.5 * bTol;

    // initialize limits

    a = ax; b = bx;
    fa = f.bFunc(a);
    fb = f.bFunc(b);
    c = a; fc = fa;

    // enter the primary loop

    while(true)
    {
      // set previous step size and test interval for data swap

      prev_step = b - a;
      if (abs(fc) < abs(fb))
      {
        // set b as the best guess so far

        a  = b;  b  = c;  c  = a;
        fa = fb;  fb = fc;  fc = fa;
      }

      // calculate current error and create a new step size

      cb = c - b;
      tol_act = TwoEPSILON * abs(b) + HalfbTol;
      new_step = 0.5 * cb;

      // if tolerance is acceptable then return b

      if ((abs(new_step) <= tol_act) || (fb == 0.0)) return b;

      // Perform the quadric interpolation if the previous step was both
      // large enough and in the true direction

      if ((abs(prev_step) >= tol_act) && (abs(fa) > abs(fb)))
      {
        double t1, t2, t1m1;

        // apply linear interpolation if only two distinct points ...
        // otherwise perform quadric inverse interpolation

        if (a == c)
        {
          t1 = fb / fa;
          p  = cb * t1;
          q  = 1.0 - t1;
        }
        else
        {
          q = fa / fc;  t1 = fb / fc;  t2 = fb / fa;
          t1m1 = t1 - 1.0;
          p = t2 * (cb * q * (q - t1) - (b - a) * t1m1);
          q = (q - 1.0) * t1m1 * (t2 - 1.0);
        }

        // p was calculated with the opposite sign ... if negative make p
        // positive ... if positive change q sign

        if (p > 0.0)
          q = -q;
        else
          p = -p;

        // if b + p / q falls in range [b, c] and is not too large then
        // accept it ... if p / q is too large then the bisection procedure
        // can reduce the range [b, c]

        if ((p < (0.75 * cb * q - abs(tol_act * q) / 2.0)) &&
            (p < abs(prev_step * q / 2.0)))
          new_step = p / q;
      }

      // adjust the step size to be not less than the tolerance

      if (abs(new_step) < tol_act)
      {
        if (new_step > 0.0)
          new_step = tol_act;
        else
          new_step = -tol_act;
      }

      // save the previous approximation and calculate a new function evaluation
      // at the new b

      a  = b;
      fa = fb;
      b += new_step;
      fb = f.bFunc(b);

      // adjust c to have opposite sign of b if fb and fc are of the same sign

      //if (fb * fc > 0.0)
      if ( (fb > 0 && fc > 0) || (fb < 0 && fc < 0) )
      {
        c  = a;
        fc = fa;
      }
    }
  }

  /**
   * Returns the functional minimum between the input abscissas
   * \em ax and \em bx at the interval defined by the function object set in
   * setFunction(BrentsFunction f). The function returns the minimum value
   * on exit. The minimum abscissa can be retrieved from the function
   * getExtremaAbscissa().
   *
   * @see #extremaF(double, double, BrentsFunction)
   *
   * @param ax Left hand range limit for the extrema search.
   * @param bx Right hand range limit for the extrema search.
   * @return The extrema functional value f.bFunc(bExAbs).
   */
  public double minF(double ax, double bx) throws Exception
  {
    bMinMaxFlg = 1.0;
    return extremaF(ax, bx, bF);
  }

  /**
   * Returns the functional minimum between the input abscissas
   * \em ax and \em bx at the interval defined by the function object f.
   * The function returns the minimum value on exit. The minimum abscissa
   * can be retrieved from the function getExtremaAbscissa().
   *
   * @see #extremaF(double, double, BrentsFunction)
   *
   * @param ax Left hand range limit for the extrema search.
   * @param bx Right hand range limit for the extrema search.
   * @param f The BrentsFunction whose bFunc implementation will be used to
   *        perform the extrema search.
   * @return The extrema functional value f.bFunc(bExAbs).
   */
  public double minF(double ax, double bx, BrentsFunction f)
                throws Exception
  {
    bMinMaxFlg = 1.0;
    return extremaF(ax, bx, f);
  }

  /**
   * Returns the functional maximum between the input abscissas
   * \em ax and \em bx at the interval defined by the function object set in
   * setFunction(BrentsFunction f). The function returns the maximum value
   * on exit. The maximum abscissa can be retrieved from the function
   * getExtremaAbscissa().
   *
   * @see #extremaF(double, double, BrentsFunction)
   *
   * @param ax Left hand range limit for the extrema search.
   * @param bx Right hand range limit for the extrema search.
   * @return The extrema functional value f.bFunc(bExAbs).
   */
  public double maxF(double ax, double bx) throws Exception
  {
    bMinMaxFlg = -1.0;
    return extremaF(ax, bx, bF);
  }

  /**
   * Returns the functional maximum between the input abscissas
   * \em ax and \em bx at the interval defined by the function object f.
   * The function returns the maximum value on exit. The maximum abscissa
   * can be retrieved from the function getExtremaAbscissa().
   *
   * @see #extremaF(double, double, BrentsFunction)
   *
   * @param ax Left hand range limit for the extrema search.
   * @param bx Right hand range limit for the extrema search.
   * @param f The BrentsFunction whose bFunc implementation will be used to
   *        perform the extrema search.
   * @return The extrema functional value f.bFunc(bExAbs).
   */
  public double maxF(double ax, double bx, BrentsFunction f)
                throws Exception
  {
    bMinMaxFlg = -1.0;
    return extremaF(ax, bx, f);
  }

  /**
   * Returns the functional minimum or maximum between the input abscissas
   * \em ax and \em bx at the interval defined by function f.
   *
   * <P>This function isolates the extrema to a fractional precision of about
   * bTol using Brent's method and returns the functional value at the extrema.
   * The abscissa of the extrema is returned from the function
   * getExtremaAbscissa() after this function returns.
   *
   * @param ax Left hand range limit for the extrema search.
   * @param bx Right hand range limit for the extrema search.
   * @param f Object containing the function bFunc(x) for which the extrema will
   *        be identified.
   * @return The extrema functional value f.bFunc(bExAbs).
   */
  private double extremaF(double ax, double bx, BrentsFunction f)
                 throws Exception
  {
    double a, b, d, e, fu, fv, fx, fw, u, v, w, x, xmv, xmw;
    double etemp, p, q, r, tol1, tol2, xm;
    int    iter;

    // initialize

    e = d = 0.0;

    a = b = ax;
    if (bx < ax)
      a = bx;
    else
      b = bx;

    v = 0.5 * (ax + bx);
    x = w = v;

    fx = bMinMaxFlg * f.bFunc(x);
    fv = fw = fx;

    // enter main loop

    for (iter = 0; iter < bITMAX; ++iter)
    {
      // evaluate mid point and convergence tolerances

      xm   = 0.5 * (a + b);
      tol1 = bTol * abs(x) + bZEPS;
      tol2 = 2.0 * tol1;

      // see if convergence has been met

      if (abs(x - xm) <= tol2 - 0.5 * (b - a)) break;

      // see if a trial parabolic fit or a golden section fit should be performed

      if (abs(e) > tol1)
      {
        //Construct a trial parabolic fit

        xmv = x - v;
        xmw = x - w;
        r = xmw * (fx - fv);
        q = xmv * (fx - fw);
        p = xmv * q - xmw * r;
        q = 2.0 * (q - r);
        if (q > 0)  p = -p;
        q = abs(q);
        etemp = e;
        e = d;

        // see if golden section or parabolic fit is required

        if ((abs(p) >= abs(0.5 * q * etemp)) ||
            (p <= q * (a - x)) || (p >= q * (b - x)))
        {
          // do golden section fit

          if (x >= xm)
            e = a - x;
          else
            e = b - x;

          d = bCGOLD * e;
        }
        else
        {
          // do parabolic fit

          d = p / q;
          u = x + d;
          if ((u - a < tol2) || (b - u < tol2))
          {
            d = abs(tol1);
            if (xm - x < 0.0) d = -d;
          }
        }
      }
      else
      {
        // do golden section fit

        if (x >= xm)
          e = a - x;
        else
          e = b - x;

        d = bCGOLD * e;
      }

      // calculate new function evaluation point from d

      if (abs(d) >= tol1)
        u = x + d;
      else
      {
        if (d < 0)
          u = x - abs(tol1);
        else
          u = x + abs(tol1);
      }
      // evaluate function and update values dependent on if fu is larger or
      // smaller then fx

      fu = bMinMaxFlg * f.bFunc(u);
      if (fu <= fx)
      {
        if (u >= x)
          a = x;
        else
          b = x;

        v = w; fv = fw;
        w = x; fw = fx;
        x = u; fx = fu;
      }
      else
      {
        if (u < x)
          a = u;
        else
          b = u;

        if (fu <= fw || w == x)
        {
          v = w; fv = fw;
          w = u; fw = fu;
        }
        else if (fu <= fv || v == x || v == w)
        {
          v = u; fv = fu;
        }
      }
    }

    if (iter == bITMAX)
    {
      throw new BrentsException("Exceeded Extrema Search Iteration Count: " +
                                bITMAX);
    }
    bExAbs = x;
    return bMinMaxFlg * fx;
  }

  /**
   * Returns the extrema abscissa if functions minF or maxF were called.
   * @return Extrema abscissa.
   */
  public double getExtremaAbscissa()
  {
    return bExAbs;
  }

  /**
   *
   * @param f The object defining function bFunc that will be used in the
   *        zero-in or extema search.
   */
  public void setFunction(BrentsFunction f)
  {
    bF = f;
  }

  /**
   * Sets the zero-in or extrema search accuracy tolerance.
   * @param tol The zero-in or extrema search accuracy tolerance.
   */
  public void setTolerance(double tol)
  {
    bTol = tol;
  }

  /**
   * Gets the zero-in or extrema search accuracy tolerance.
   */
  public double getTolerance()
  {
    return bTol;
  }

  /**
   * Sets the extrema search maximum iteration count before
   * throwing a BrentsException.
   * @param itermax The maximum allowable iterations during an extrema
   *        search before throwing a BrentsException.
   */
  public void setExtremaSrchIterCount(int itermax)
  {
    bITMAX = itermax;
  }
}
