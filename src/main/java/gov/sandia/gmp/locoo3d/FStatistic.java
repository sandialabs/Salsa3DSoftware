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
package gov.sandia.gmp.locoo3d;

import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.log;

import gov.sandia.gmp.util.numerical.machine.DhbMath;

public class FStatistic
{
  private static double f_n, f_m, f_k, f_p;

  private static final double cof[] =
      {76.18009172947146, -86.50532032941677, 24.01409824083091, 
	  -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5};

  private static final int ITMAX = 100;
  private static final double EPS = DhbMath.defaultNumericalPrecision();
  private static final double FPMIN = Double.MIN_VALUE / EPS;

  private static double gammln(final double xx)
  {
    int j;
    double x, y, tmp, ser;

    y = x = xx;
    tmp = x + 5.5;
    tmp -= (x + 0.5) * log(tmp);
    ser = 1.000000000190015;
    for (j = 0; j < 6; j++)
      ser += cof[j] / ++y;
    return -tmp + log(2.5066282746310005 * ser / x);
  }

  private static double gcf(final double a, final double x)
      throws LocOOException
  {
    int i;
    double an, b, c, d, del, h;

    double gln = gammln(a);
    b = x + 1.0 - a;
    c = 1.0 / FPMIN;
    d = 1.0 / b;
    h = d;
    for (i = 1; i <= ITMAX; i++)
    {
      an = -i * (i - a);
      b += 2.0;
      d = an * d + b;
      if (abs(d) < FPMIN)
        d = FPMIN;
      c = b + an / c;
      if (abs(c) < FPMIN)
        c = FPMIN;
      d = 1.0 / d;
      del = d * c;
      h *= del;
      if (abs(del - 1.0) <= EPS)
        break;
    }
    if (i > ITMAX)
      throw new LocOOException("a too large, ITMAX too small in gcf\n");

    return exp( -x + a * log(x) - gln) * h;
  }

  private static double gser(final double a, final double x)
      throws LocOOException
  {
    int n;
    double gamser=0., sum, del, ap;

    if (x <= 0.0)
    {
      if (x < 0.0)
        throw new LocOOException("x less than 0 in routine gser\n");
      return gamser;
    }
    else
    {
      double gln = gammln(a);
      ap = a;
      del = sum = 1.0 / a;
      for (n = 0; n < ITMAX; n++)
      {
        ++ap;
        del *= x / ap;
        sum += del;
        if (abs(del) < abs(sum) * EPS)
          return sum * exp( -x + a * log(x) - gln);
      }
      throw new LocOOException("a too large, ITMAX too small in routine gser\n");
    }
  }

  private static double gammq(final double a, final double x) throws
      LocOOException
  {
    if (x < 0.0 || a <= 0.0)
      throw new LocOOException("Invalid arguments in routine gammq\n");

    if (x < a + 1.0)
      return 1.0 - gser(a, x);
    else
      return gcf(a, x);
  }

  private static double betacf(final double a, final double b, final double x) 
  throws LocOOException
  {
    final int MAXIT = 10000;
    int m, m2;
    double aa, c, d, del, h, qab, qam, qap;

    qab = a + b;
    qap = a + 1.0;
    qam = a - 1.0;
    c = 1.0;
    d = 1.0 - qab * x / qap;
    if (abs(d) < FPMIN)
      d = FPMIN;
    d = 1.0 / d;
    h = d;
    for (m = 1; m <= MAXIT; m++)
    {
      m2 = 2 * m;
      aa = m * (b - m) * x / ( (qam + m2) * (a + m2));
      d = 1.0 + aa * d;
      if (abs(d) < FPMIN)
        d = FPMIN;
      c = 1.0 + aa / c;
      if (abs(c) < FPMIN)
        c = FPMIN;
      d = 1.0 / d;
      h *= d * c;
      aa = - (a + m) * (qab + m) * x / ( (a + m2) * (qap + m2));
      d = 1.0 + aa * d;
      if (abs(d) < FPMIN)
        d = FPMIN;
      c = 1.0 + aa / c;
      if (abs(c) < FPMIN)
        c = FPMIN;
      d = 1.0 / d;
      del = d * c;
      h *= del;
      if (abs(del - 1.0) <= EPS)
        break;
    }
    if (m > MAXIT)
      throw new LocOOException("a or b too big, or MAXIT too small in betacf\n");
    return h;
  }

  private static double betai(final double a, final double b, final double x) throws
      LocOOException
  {
    double bt;

    if (x < 0.0 || x > 1.0)
      throw new LocOOException("Bad x in routine betai\n");
    if (x == 0.0 || x == 1.0)
      bt = 0.0;
    else
      bt = exp(gammln(a + b) - gammln(a) - gammln(b) + a * log(x) +
               b * log(1.0 - x));
    if (x < (a + 1.0) / (a + b + 2.0))
      return bt * betacf(a, b, x) / a;
    else
      return 1.0 - bt * betacf(b, a, 1.0 - x) / b;
  }

  private static boolean zbrac(double[] x) throws LocOOException
  {
    final int NTRY = 50;
    final double FACTOR = 1.6;
    int j;
    double f1, f2;

    if (x[0] == x[1])
      throw new LocOOException("Bad initial range in zbrac\n");
    f1 = func(x[0]);
    f2 = func(x[1]);
    for (j = 0; j < NTRY; j++)
    {
      if (f1 * f2 < 0.0)
        return true;
      if (abs(f1) < abs(f2))
        f1 = func(x[0] += FACTOR * (x[0] - x[1]));
      else
        f2 = func(x[1] += FACTOR * (x[1] - x[0]));
    }
    return false;
  }

  private static double zbrent(double[] x, final double tol) 
  throws LocOOException
  {
    final int ITMAX = 100;
    //final double EPS=numeric_limits<double>::epsilon();
    final double EPS = 2.220446e-16;
    int iter;
    double a = x[0], b = x[1], c = x[1], d = 0, e = 0, min1, min2;
    double fa = func(a), fb = func(b), fc, p, q, r, s, tol1, xm;

    if ( (fa > 0.0 && fb > 0.0) || (fa < 0.0 && fb < 0.0))
      throw new LocOOException("Root must be bracketed in zbrent\n");
    fc = fb;
    for (iter = 0; iter < ITMAX; iter++)
    {
      if ( (fb > 0.0 && fc > 0.0) || (fb < 0.0 && fc < 0.0))
      {
        c = a;
        fc = fa;
        e = d = b - a;
      }
      if (abs(fc) < abs(fb))
      {
        a = b;
        b = c;
        c = a;
        fa = fb;
        fb = fc;
        fc = fa;
      }
      tol1 = 2.0 * EPS * abs(b) + 0.5 * tol;
      xm = 0.5 * (c - b);
      if (abs(xm) <= tol1 || fb == 0.0)
        return b;
      if (abs(e) >= tol1 && abs(fa) > abs(fb))
      {
        s = fb / fa;
        if (a == c)
        {
          p = 2.0 * xm * s;
          q = 1.0 - s;
        }
        else
        {
          q = fa / fc;
          r = fb / fc;
          p = s * (2.0 * xm * q * (q - r) - (b - a) * (r - 1.0));
          q = (q - 1.0) * (r - 1.0) * (s - 1.0);
        }
        if (p > 0.0)
          q = -q;
        p = abs(p);
        min1 = 3.0 * xm * q - abs(tol1 * q);
        min2 = abs(e * q);
        if (2.0 * p < (min1 < min2 ? min1 : min2))
        {
          e = d;
          d = p / q;
        }
        else
        {
          d = xm;
          e = d;
        }
      }
      else
      {
        d = xm;
        e = d;
      }
      a = b;
      fa = fb;
      if (abs(d) > tol1)
        b += d;
      else
        b += SIGN(tol1, xm);
      fb = func(b);
    }

    throw new LocOOException("Maximum number of iterations exceeded in zbrent\n");
  }

  private static double func(final double f) throws LocOOException
  {
    return probability(f) - f_p;
  }

  private static double probability(final double f) throws LocOOException
  {
    //if (f == 0.) return 0.;
    if (f_k >= 0)
      return betai(f_n * 0.5, f_m * 0.5, (f_n / (f_n + f_m * f)));
    else
      return gammq(f_m * 0.5, f * 0.5);
  }

  public static double probability(final int m, final long n, final int k,
                                   final double chi_sqr) throws LocOOException
  {
    if (m < 1 || chi_sqr < 0.)
      return -1.;
    //if (chi_sqr == 0.) return 0.;
    if (k < 0)
      return 1. - gammq(m * 0.5, chi_sqr * 0.5);
    f_n = n + k;
    if (f_n <= 0)
      return 1.;
    return 1. - betai(f_n * 0.5, m * 0.5, (f_n / (f_n + chi_sqr)));
  }

  public static double f_statistic(final int m, final int n, final int k,
                                   final double p) throws LocOOException //throws LocOOException
  {
    f_n = n + k;
    f_m = m;
    f_k = k;
    f_p = 1. - p;

    double[] x = new double[] {0., 1.4};

    if (m < 0 || p >= 1.0)
      return -1.;

    if (k >= 0)
    {
      if (f_n == 0)
        return 1e100;
      if (zbrac(x))
        return f_m * zbrent(x, 1e-6);
    }
    else
    {
      if (zbrac(x))
        return zbrent(x, 1e-6);
    }
    return -1.;
  }

  private static double SIGN(double a, double b)
  {
    return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
  }
}
