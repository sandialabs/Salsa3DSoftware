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
package gov.sandia.gmp.util.numerical.lbfgs;

import static gov.sandia.gmp.util.globals.Globals.NL;
import static gov.sandia.gmp.util.numerical.machine.DhbMath.getMachinePrecision;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

import java.io.Serializable;

/**
 * Limited-Memory Quasi-Newton Approximation employing the limited-memory
 * Broyden-Fletcher-Goldfarb-Shanno (LBFGS) algorithm for large-scale
 * multidimensional unconstrained minimization problems.
 * 
 * <p> This file is a translation of Fortran code written by Jorge Nocedal.
 *
 * <p> LBFGS is distributed as part of the RISO project. Following is a message from Jorge Nocedal:
 * <pre>
 *   From: Jorge Nocedal [mailto:nocedal@dario.ece.nwu.edu]
 *   Sent: Friday, August 17, 2001 9:09 AM
 *   To: Robert Dodier
 *   Subject: Re: Commercial licensing terms for LBFGS?
 *   
 *   Robert:
 *   The code L-BFGS (for unconstrained problems) is in the public domain.
 *   It can be used in any commercial application.
 *   
 *   The code L-BFGS-B (for bound constrained problems) belongs to
 *   ACM. You need to contact them for a commercial license. It is
 *   algorithm 778.
 *   
 *   Jorge
 * </pre>
 * 
 * This code is derived from the Fortran program <code>lbfgs.f</code>.
 * The Java translation was effected mostly mechanically, with some
 * manual clean-up; in particular, array indices start at 0 instead of 1.
 * Most of the comments from the Fortran code have been pasted in here
 * as well.
 *
 * <p> The most recent version removes the necessity to exit the function
 * to call the function optimization and gradient evaluation, or the inverse
 * Hessian diagonal estimation function. These are now called via an
 * interface (LBFGSFunction) which can be used to define the function
 * evaluations externally without the need of recalling this function many
 * times. Additionally, the code has been restructured to place many,
 * otherwise non-static variables into a local functional context when
 * possible. If variables could not be defined locally they were changed
 * to private non-static values as were the functions so that many of the
 * LBFGS objects could be made to operate simultaneously in a distributed
 * parallel environment. Finally, some code clean-up was performed to remove
 * the extraneous "-1" from array indices and to change the accompanying
 * "for" loop index to run from 0 to n-1 instead of 1 to n.
 * 
 * <p> Here's some information on the original LBFGS Fortran source code,
 * available at <a href="http://www.netlib.org/opt/lbfgs_um.shar">
 * http://www.netlib.org/opt/lbfgs_um.shar</a>. This info is taken
 * verbatim from the Netlib blurb on the Fortran source.</p>
 *
 * <pre>
 * 	file    opt/lbfgs_um.shar
 * 	for     unconstrained optimization problems
 * 	alg     limited memory BFGS method
 * 	by      J. Nocedal
 * 	contact nocedal@eecs.nwu.edu
 * 	ref     D. C. Liu and J. Nocedal, ``On the limited memory BFGS method for
 * 	,       large scale optimization methods'' Mathematical Programming 45
 * 	,       (1989), pp. 503-528.
 * 	,       (Postscript file of this paper is available via anonymous ftp
 * 	,       to eecs.nwu.edu in the directory pub/lbfgs/lbfgs_um.)
 * </pre>
 *
 * @author Jorge Nocedal: original Fortran version, including comments
 * (July 1990). Robert Dodier: Java translation, August 1997. Jim Hipp:
 * Java functional interface for optimization function and gradient
 * evaluation and overall code restructuring to remove the necessity
 * to exit the function to call optimization and diagonal set functions,
 * March 2008.
 */
@SuppressWarnings("serial")
public class LBFGS implements Serializable
{
	/**
	 * Specify lower bound for the step in the line search.
	 * The default value is 1e-20. This value need not be modified unless
	 * the exponent is too large for the machine being used, or unless
	 * the problem is extremely badly scaled (in which case the exponent
	 * should be increased). This must be done manually as it is a very
	 * rare circumstance.
	 */
	private static final double stpmin = 1e-20;

	/**
	 * Specify upper bound for the step in the line search.
	 * The default value is 1e20. This value need not be modified unless
	 * the exponent is too large for the machine being used, or unless
	 * the problem is extremely badly scaled (in which case the exponent
	 * should be increased). This must be done manually as it is a very
   * rare circumstance.
	 */
	private static final double stpmax = 1e20;

	/**
	 * Machine precision.
	 */
  private static final double EPSILON = getMachinePrecision();

	/**
	 * Norm of the gradient at the current solution <code>x</code>.
	 */
	private double gnorm = 0.0;

	/**
	 * Internal iteration counter for each estimate of the inverse
	 * Hessian.
	 */
  private int iter = 0;
  
  /**
   * Internal parameter that tracks the total number of objective
   * function evaluations for each iteration of the inverse
   * Hessian estimate.
   */  
  private int nfev = 0;
  
  /**
   * An internal flag used by the line search function (msrch) to set its state
   * following a call to the function. The following values are possible:
   *
   *   <ul>
   *   <li><code>info = 0</code> Improper input parameters.
   *   <li><code>info = -1</code> A return is made to compute the function and gradient.
   *   <li><code>info = 1</code> The sufficient decrease condition and
   *     the directional derivative condition hold.
   *   <li><code>info = 2</code> Relative width of the interval of uncertainty
   *     is at most <code>EPSILON</code>.
   *   <li><code>info = 3</code> Number of function evaluations has reached <code>maxfev</code>.
   *   <li><code>info = 4</code> The step is at the lower bound <code>stpmin</code>.
   *   <li><code>info = 5</code> The step is at the upper bound <code>stpmax</code>.
   *   <li><code>info = 6</code> Rounding errors prevent further progress.
   *     There may not be a step which satisfies the
   *     sufficient decrease and curvature conditions.
   *     Tolerances may be too small.
   *   </ul>
   */
  private int info = 0;

  /**
   * An internal flag used by the line search function (msrch) that contains
   * the current search step size.
   */
  private double stp = 0.0;

  /**
   * Internal parameter that is set to true when convergence is attained.
   */
  private boolean finish = false;

  /**
   * An internal parameter used by function <code>mcstep</code> to indicate
   * internal state. The value can be set as follows:
   * 
   * <p>  If <code>infoc</code> is 1, 2, 3, or 4, then the step has been
   *      computed successfully. Otherwise <code>infoc</code> = 0, and this
   *      indicates improper input parameters.
   * 
   */
  private int infoc = 1;

  /**
   * Internal parameter used by function <code>msrch</code> to store the
   * initial gradient calculation.
   */
  private double dginit = 0.0;
  
  /**
   * An internal parameter containing the initial function value at the
   * first line search in <code>msrch</code>.
   */
  private double finit = 0.0;

  /**
   * Internal parameter used by function <code>msrch</code> to store the
   * initial gradient * ftol.
   */
  private double dgtest = 0.0;

  /**
   * The Derivative at the best step obtained so far in the line search
   * function <code>msrch</code>. The derivative must be negative in the
   * direction of the step, that is, <code>dgx</code> and
   * <code>stp - stx</code> must have opposite signs. This variable is
   * modified by <code>mcstep</code>.
   */
  private double dgx[] = new double[1];

  /**
   * The slightly modified derivative at the best step obtained so far in
   * the line search function <code>msrch</code>. The derivative must be
   * negative in the direction of the step, that is, <code>dgx</code> and
   * <code>stp - stx</code> must have opposite signs. This variable is
   * modified by <code>mcstep</code>.
   */
  private double dgxm[] = new double[1];
  
  /**
   * The derivative at the other endpoint of the interval of
   * uncertainty (opposite dgx). This variable is modified by
   * <code>mcstep</code>.
   */  
  private double dgy[] = new double[1];
  
  /**
   * The slightly modified derivative at the other endpoint of the interval
   * of uncertainty (opposite dgx). This variable is modified by
   * <code>mcstep</code>.
   */  
  private double dgym[] = new double[1];

  /**
   * The function value at the best step obtained so far. 
   * This variable is modified by <code>mcstep</code>.
   */  
  private double fx[] = new double[1];

  /**
   * The slightly modified function value at the best step obtained so far. 
   * This variable is modified by <code>mcstep</code>.
   */  
  private double fxm[] = new double[1];
  
  /**
   * The function value at the other endpoint of the interval of uncertainty
   * (opposite fx). This variable is modified by <code>mcstep</code>.
   */
  private double fy[] = new double[1];
  
  /**
   * The slightly modifed function value at the other endpoint of the interval
   * of uncertainty (opposite fx). This variable is modified by
   * <code>mcstep</code>.
   */
  private double fym[] = new double[1];

  /**
   * The step size for the best step obtained so far. 
   * This variable is modified by <code>mcstep</code>.
   */
  private double stx = 0.0;

  /**
   * The step size at the other endpoint of the interval of uncertainty for
   * the best step obtained so far. This variable is modified by
   * <code>mcstep</code>.
   */
  private double sty = 0.0;
  
  /**
   * Lower bound of the current step size.
   */
  private double stmin = 0.0;
  
  /**
   * Upper bound of the current step size.
   */
  private double stmax = 0.0;
  
  /**
   * The interval of uncertainty.
   */
  private double width = 0.0;
  
  /**
   * The interval of uncertainty in the previous line search.
   */
  private double width1 = 0.0;
  
  /**
   * Tells whether a minimizer has been bracketed. If the minimizer has
   * not been bracketed, then on input to <code>mcstep</code> this variable
   * must be set to <code>false</code>. If the minimizer has been
   * bracketed, then on output this variable is <code>true</code>.

   */  
  private boolean brackt = false;
  
  /**
   * An internal flag indicating if the first stage of step prediction is
   * processing.
   */
  private boolean stage1 = false;

  /**
   * Tracks the total number of objective function evaluations.
   * This value can be returned following lbfgs completion with
   * the function {@link #nfevaluations() nfevaluations}.
   */
  private int nfun = 0;
  
  /**
   * The returned error code following execution of the primary function
   * (lbfgs). A return with <code>errCode < 0</code> indicates an error,
   * and <code>errCode = 0</code> indicates that the routine has
   * terminated without detecting errors.
   *
   * <p> The following negative values of <code>errCode</code>, detecting an error,
   *     are possible:
   *    <ul>
   *    <li> <code>errCode = -1</code> The line search routine
   *      <code>mcsrch</code> failed. One of the following messages
   *      is printed:
   *      <ul>
   *      <li> Improper input parameters.
   *      <li> Relative width of the interval of uncertainty is at
   *        most <code>EPSILON</code>.
   *      <li> More than 20 function evaluations were required at the
   *        present iteration.
   *      <li> The step is too small.
   *      <li> The step is too large.
   *      <li> Rounding errors prevent further progress. There may not
   *        be  a step which satisfies the sufficient decrease and
   *        curvature conditions. Tolerances may be too small.
   *      </ul>
   *    <li><code>errCode = -2</code> The i-th diagonal element of the diagonal inverse
   *      Hessian approximation, given in DIAG, is not positive.
   *    <li><code>errCode = -3</code> Improper input parameters for LBFGS
   *      (<code>n(length of x)</code> or <code>corrKept</code> are not positive).
   *    </ul>
   *    
   * <p> The value for the errCode can be returned following execution of any
   * lbfgs function with the function {@link #getErrorCode() getErrorCode}.    
   */
  private int errCode = 0;
	
	/**
	 * Determines the accuracy with which the solution is to be found
	 * (defaults to 1.0e-6). The subroutine terminates when
	 * <pre>
   *            ||G|| < EPS * max(1,||X||),
   * </pre>
   * where <code>||.||</code> denotes the Euclidean norm.
   * 
   * <p> The accuracy can be set with the function
   * {@link #setAccuracyTolerance(double) setAccuracyTolerance}. 
 	 */
	private double eps = 1.0e-6;

	/**
	 * The number of corrections used in the BFGS update. 
   * Values of <code>corrKept</code> less than 3 are not recommended;
   * large values of <code>corrKept</code> will result in excessive
   * computing time. <code>3 <= corrKept <= 7</code> is recommended.
   * Restriction: <code>corrKept > 0</code>.
   * 
   * <p> The kept correction count can be set with the function
   * {@link #setCorrectionCount(int) setCorrectionCount}. 
	 */
  private int    corrKept = 3;
  
  /**
   * Specifies the frequency of the output:
   * 
   * <p>
   * <ul>
   * <li> <code>outfreq < 0</code>: no output is generated,
   * <li> <code>outfreq = 0</code>: output only at first and last iteration,
   * <li> <code>outfreq > 0</code>: output every <code>outfreq</code> iterations.
   * </ul>
   * <p>
   * 
   * The output frequency can be set with the following functions
   *    {@link #setOutputCount(int) setOutputCount} for a specific count, or
   *    {@link #setOutputOff() setOutputOff} to turn off output, or
   *    {@link #setOutputFirstAndLast() setOutputFirstAndLast} to output only
   *    the first and last iterations.
   */  
  private int    outFreq = 0;
  
  /**
   * Specifies the amount of output generated:
   * 
   * <p>
   * <ul>
   * <li> <code>outAmount = 0</code>: iteration count, number of function 
   *      evaluations, function value, norm of the gradient, and steplength,
   * <li> <code>outAmount = 1</code>: same as <code>outAmount=0</code>, plus vector of
   *      variables and  gradient vector at the initial point,
   * <li> <code>outAmount = 2</code>: same as <code>outAmount=1</code>, plus vector of
   *      variables,
   * <li> <code>outAmount = 3</code>: same as <code>outAmount=2</code>, plus gradient vector.
   * </ul>
   * <p>
   * 
   * The amount of output can be set with the following functions.
   * {@link #setOutputAmount(int) setOutputAmount} to set any amount from the above list, or
   * {@link #setOutputAmountBasic() setOutputAmountBasic} to set the basic amount
   * of output.
   */
  private int    outAmount = 0;
  
  /** The LBFGSFunction definition that provides the updated, gradient,
   *  optimization function value, and optionally, the Hessian diagonal
   *  estimate at the current solution vector value (x). This parameter
   *  is used in all lbfgs functions that do not take a LBFGSFunction
   *  arugment. The value can be set with the function
   *  {@link #setLBFGSFunction(LBFGSFunction) setLBFGSFunction}.
   */
  private LBFGSFunction lbfgsfunc = null;
  
  /**
   * Controls the accuracy of the line search <code>mcsrch</code>
   * curvature condition. If the function and gradient evaluations are
   * inexpensive with respect to the cost of the iteration (which is
   * sometimes the case when solving very large problems) it may be
   * advantageous to set <code>gtol</code> to a small value. A typical
   * small value is 0.1. Restriction: <code>gtol</code> should be
   * greater than 1e-4 (ftol). This parameter can be set with a call to
   * function
   * {@link #setSearchCurvatureTolerance(double) setSearchCurvatureTolerance} 
   */
  private double gtol = 0.9;
  
  /**
   * Internal tolerance for the sufficient decrease condition. Used
   * by the the line search routine, msrch, which attempts to
   * minimize the function along a search direction. This parameter
   * can be set to a different value but should be larger than the
   * requested accuracy (eps) and smaller than the line search
   * curvature tolerance (gtol). The value can be set with the
   * function 
   * {@link #setSearchDecreaseTolerance(double) setSearchDecreaseTolerance}
   */
  private double ftol = 0.0001;

  /**
   * Defines the maximum number of function evaluations allowed in
   * a single outer iteration (for the Hessian estimate) before
   * throwing an error. This value can be set by calling function
   * {@link #setMaxFEVPerIteration(int) setMaxFEVPerIteration}
   */
  private int maxfev = 20;

  /**
   * Default constructor.
   */
  public LBFGS()
  {
  }
  
  /**
   * Sets the object that implements the function and gradient, and
   * diagonal Hessian estimate functions.
   * 
   * @param li The implementor of the function and gradient, and if
   * defined, the diagonal Hessian estimate functions. This object
   * is used by the lbfgs functions that do not explicitly define
   * a LBFGSFunction object as an input argument.
   */
  public void setLBFGSFunction(LBFGSFunction li)
  {
    lbfgsfunc = li;
  }

  /**
   * Returns the error code at the completion of the lbfgs call.
   * 
   * @return The error code.
   */
  public int getErrorCode()
  {
    return errCode;
  }

  /**
   * Sets the number of kept corrections to be used by the lbfgs.
   *  
   * @param m The number of kept corrections.
   */
  public void setCorrectionCount(int m)
  {
    corrKept = m;
  }

  /**
   * Turns off all output.
   */
  public void setOutputOff()
  {
    outFreq = -1;
  }
  
  /**
   * Outputs only the first and last iteration results.
   */
  public void setOutputFirstAndLast()
  {
    outFreq = 0;
  }
  
  /**
   * Sets the output frequency to every <code>cnt</code> iterations.
   */ 
  public void setOutputCount(int cnt)
  {
    if (cnt > 0)
      outFreq = cnt;
    else
      outFreq = 0;
  }

  /**
   * Turns off all output
   */
  public void setOutputAmountOff()
  {
    outAmount = -1;
  }

  /**
   * Sets the amount of output to the basic amount
   */
  public void setOutputAmountBasic()
  {
    outAmount = 0;
  }

  /**
   * 
   * Specifies the amount of output generated:
   * 
   * <p>
   * <ul>
   * <li> <code>amt = 0</code>: iteration count, number of function 
   *      evaluations, function value, norm of the gradient, and steplength,
   * <li> <code>amt = 1</code>: same as <code>amt=0</code>, plus vector of
   *      variables and  gradient vector at the initial point,
   * <li> <code>amt = 2</code>: same as <code>amt=1</code>, plus vector of
   *      variables,
   * <li> <code>amt = 3</code>: same as <code>amt=2</code>, plus gradient vector.
   * </ul>
   * <p>
   */
  public void setOutputAmount(int amt)
  {
    if (amt < 0)
      outAmount = 0;
    else if (amt > 3)
      outAmount = 3;
    else
      outAmount = amt;
  }

	/**
	 *  This method returns the total number of evaluations of the objective
   * function since the last time LBFGS was restarted. The total number of function
   * evaluations increases by the number of evaluations required for the
   * line search; the total is only increased after a successful line search.
   */
	public int nfevaluations()
	{
	  return nfun;
	}

	/**
   * Sets the accuracy with which the solution is to be found
   * (defaults to 1.0e-6). The subroutine terminates when
   * <pre>
   *            ||G|| < tol * max(1,||X||),
   * </pre>
   * where <code>||.||</code> denotes the Euclidean norm.
	 * @param tol Input accuracy.
	 */
	public void setAccuracyTolerance(double tol)
	{
	  eps = tol;
	}

	
  public double getSearchCurvatureTolerance()
  {
    return gtol;
  }

  /**
   * Controls the accuracy of the line search <code>mcsrch</code>
   * curvature condition. If the function and gradient evaluations are
   * inexpensive with respect to the cost of the iteration (which is
   * sometimes the case when solving very large problems) it may be
   * advantageous to set <code>gtol</code> to a small value. A typical
   * small value is 0.1. Restriction: <code>gtol</code> should be
   * greater than 1e-4 (ftol).
   *
   * @param lscrvtol Line search curvature tolerance condition.
   */
  public void setSearchCurvatureTolerance(double lscrvtol)
  {
    if (lscrvtol < 1.0e-4)
    {
      System.err.println( "LBFGS.lbfgs: gtol is less than or equal to 0.0001. It has been reset to 0.9." );
      gtol = 0.9;
    }
    else
      gtol = lscrvtol;
  }

  /**
   * Internal tolerance for the sufficient decrease condition. Used
   * by the the line search routine, msrch, which attempts to
   * minimize the function along a search direction. This parameter
   * can be set to a different value but should be larger than the
   * requested accuracy (eps) and smaller than the line search
   * curvature tolerance (gtol).
   *
   * @param dtol Line search decrease tolerance condition.
   */
  public void setSearchDecreaseTolerance(double dtol)
  {
    ftol = dtol;
  }

  /**
   * Sets the maximum allowable function evaluations per inverse
   * Hessian estimate. If this value is exceeded an error is thrown.
   * 
   * @param mfev Maximum allowable function evaluations per inverse
   *             Hessian estimate.
   */
  public void setMaxFEVPerIteration(int mfev)
  {
    maxfev = mfev;
  }
	
  /**
   * Standard call to the lbfgs function. The LBFGSFunction
   * setDiagonal is not used with this call. The internal LBFGSFunction
   * function must have been set with a call to setLBFGSFunction or an
   * error will be thrown
   *
   * @param x The initial input vector for which a minimum will be sought.
   * @throws LBFGSException
   */
  public void lbfgs(double[] x) throws LBFGSException
  {
    lbfgs(x, false, lbfgsfunc);
  }

  /**
   * Standard call to the lbfgs function. The LBFGSFunction
   * setDiagonal is not used with this call.
   *
   * @param x The initial input vector for which a minimum will be sought.
   * @param fi The LBFGSFunction to be used for the object function call.
   * @throws LBFGSException
   */
  public void lbfgs(double[] x, LBFGSFunction fi) throws LBFGSException
  {
    lbfgs(x, false, fi);
  }

  /**
   * Standard call to the lbfgs function that uses a user defined guess
   * to the inverse Hessian diagonal at each iteration. The diagonal must
   * be supplied by the LBFGSFunction setDiagonal function. The internal
   * LBFGSFunction function must have been set with a call to setLBFGSFunction
   * or an error will be thrown
   *
   * @param x The initial input vector for which a minimum will be sought.
   * @throws LBFGSException
   */
  public void lbfgsEstDiag(double[] x) throws LBFGSException
  {
    lbfgs(x, true, lbfgsfunc);
  }

  /**
   * Standard call to the lbfgs function that uses a user defined guess
   * to the inverse Hessian diagonal at each iteration. The diagonal must
   * be supplied by the LBFGSFunction objects (fi) setDiagonal function.
   *
   * @param x The initial input vector for which a minimum will be sought.
   * @param fi The LBFGSFunction to be used for the object function and
   *        diagonal calls.
   * @throws LBFGSException
   */
  public void lbfgsEstDiag(double[] x, LBFGSFunction fi) throws LBFGSException
  {
    lbfgs(x, true, fi);
  }

	/**
	 * This function is the primary call to solve the unconstrained minimization
	 * problem
	 * 
   * <pre>
	 *     min f(x),    x = (x1,x2,...,x_n),
	 * </pre>
	 * 
	 * <p> using the limited-memory BFGS method. The routine is especially
	 * effective on problems involving a large number of variables. In
	 * a typical iteration of this method an approximation <code>Hk</code> to the
	 * inverse of the Hessian is obtained by applying <code>m</code> BFGS updates to
	 * a diagonal matrix <code>Hk0</code>, using information from the previous M steps.
	 * The user specifies the number <code>m</code>, which determines the amount of
	 * storage required by the routine. The user may also provide the
	 * diagonal matrices <code>Hk0</code> if not satisfied with the default choice.
	 * The algorithm is described in "On the limited memory BFGS method
	 * for large scale optimization", by D. Liu and J. Nocedal,
	 * Mathematical Programming B 45 (1989) 503-528.
	 *
	 * The user is required to calculate the function value <code>f</code> and its
	 * gradient <code>g</code> using the interface LBFGSFunction object fi. The
	 * objective function must be supplied by a function call to
	 * setFunctionAndGradient(x, g).
	 *
	 * The steplength is determined at each iteration by means of the
	 * line search routine <code>mcsrch</code>, which is a slight modification of
	 * the routine <code>CSRCH</code> written by More' and Thuente.
	 *
	 * The only variables that are machine-dependent are <code>EPSILON</code>,
	 * <code>stpmin</code> and <code>stpmax</code>.
	 *
	 * Progress messages are printed to <code>System.out</code>, and
	 * non-fatal error messages are printed to <code>System.err</code>.
	 * Fatal errors cause exception to be thrown, as listed below.
	 *
	 * @param x On initial entry this must be set by the user to the values
	 *	      	of the initial estimate of the solution vector. On exit with
	 *		      <code>errCode = 0</code>, it contains the values of the variables
	 *		      at the best point found (usually a solution).
	 *
	 * @param diagco Set this to <code>true</code> if the user  wishes to
	 *		           provide the diagonal matrix <code>Hk0</code> at each
	 *               iteration.	Otherwise it should be set to <code>false</code>
	 *               in which case <code>lbfgs</code> will use a default value
	 *               described below. If <code>diagco</code> is set to
	 *               <code>true</code> the interface function
	 *               fi.setDiagonal(x, diag) will be called at each iteration
	 *               of the algorithm. 
	 *
	 * @param fi The LBFGSFunction interface that contains the objective function
	 *           and diagonal matrix implementations. If this object is null an
	 *           error will be thrown
	 *
	 *	@throws LBFGSException 
	 */
	private void lbfgs(double[] x, boolean diagco, LBFGSFunction fi)
	             throws LBFGSException
	{
	  int i;
    int cp, inmc, iycn, iscn, ispt, iypt, npt, bound, point;
    cp = inmc = iycn = iscn = ispt = iypt = npt = bound = point = 0;
	  double sq, yy, ys, yr, stp1, beta, xnorm;
	  sq = ys = yy = yr = stp1 = beta = xnorm = 0.0;

	  if (fi == null)
	  {
      errCode = -3;
      String s = "LBFGSFunction object fi is null.";
      throw new LBFGSException(errCode, s);
	  }

	  // create storage for the gradient and evaluate along with
	  // function value at input vector x.
	  
    int n = x.length;
	  double[] g = new double [n]; 
	  double f = fi.setFunctionAndGradient(x, g);
	  
	  // if the diagonal is to be provided retrieve it
	  
	  double[] diag = new double [n];
	  if (diagco)
	  {
	    fi.setDiagonal(x, diag);
	  }
	  
	  // create temporary workspace array
	  
		int sze = n * (2 * corrKept + 1) + 2 * corrKept; // 2 * corrKept * (n + 1) + n
		double [] w = new double[sze];

  	// Initialize.

		iter = 0;

		if ((n <= 0) || (corrKept <= 0))
		{
		  errCode = -3;
			String s = "Improper input parameters (n or m are not positive.)";
			throw new LBFGSException(errCode, s);
		}

		nfun   = 1;
		point  = 0;
		finish = false;

		if (diagco)
		{
			for (i = 0; i < n; ++i)
			{
				if (diag[i] <= 0)
				{
				  errCode = -2;
					String s = "The " + i + "-th diagonal element of the inverse " +
					           "hessian approximation is not positive.";
					throw new LBFGSException(errCode, s);
				}
			}
		}
		else
		{
			for (i = 0; i < n; ++i) diag [i] = 1.0;
		}
		ispt = 2 * corrKept + n;
		iypt = n * corrKept + ispt;

		for (i = 0 ; i < n; ++i) w[ispt + i] = - g[i] * diag[i];

		gnorm = sqrt(ddot(n, g, 0, g, 0));
		stp1 = 1.0 / gnorm;

		if (outFreq >= 0) lb1(n, x, f, g);

		while (true)
		{
			++iter;
			info = 0;
			bound = iter - 1;
			if (iter != 1)
			{
				if (iter > corrKept) bound = corrKept;
				ys = ddot(n, w, iypt + npt, w, ispt + npt);
				if (!diagco)
				{
					yy = ddot(n, w, iypt + npt, w, iypt + npt);
					for (i = 0; i < n; ++i) diag[i] = ys / yy;
				}
				else
				{
				  fi.setDiagonal(x, diag);
				}
			}

      if (iter != 1)
			{
				if (diagco)
				{
					for (i = 0; i < n; ++i)
					{
						if (diag[i] <= 0.0)
						{
						  errCode = -2;
							String s = "The " + i + "-th diagonal element of the inverse " +
							           "hessian approximation is not positive.";
							throw new LBFGSException(errCode, s);
						}
					}
				}
				cp = point;
				if (point == 0) cp = corrKept;
				w[n + cp - 1] = 1.0 / ys;

				for (i = 0; i < n; ++i) w[i] = -g[i];

				cp = point;

				for (i = 0; i < bound; ++i)
				{
					--cp;
					if (cp == -1) cp = corrKept - 1;
					sq = ddot(n, w, ispt + cp * n, w, 0);
					inmc = n + corrKept + cp;
					iycn = iypt + cp * n;
					w[inmc] = w[n + cp] * sq;
					daxpy(n, -w[inmc], w, iycn, w);
				}

				for (i = 0; i < n; ++i) w[i] = diag[i] * w[i];

				for (i = 0; i < bound; ++i)
				{
					yr = ddot(n, w, iypt + cp * n, w, 0);
					beta = w[n + cp] * yr;
					inmc = n + corrKept + cp;
					beta = w[inmc] - beta;
					iscn = ispt + cp * n;
					daxpy(n, beta, w, iscn, w);
					++cp;
					if (cp == corrKept) cp = 0;
				}

				iscn = ispt + point * n;
				for (i = 0; i < n ; ++i) w[iscn + i] = w[i];
			}

			nfev = 0;
			stp = 1;
			if (iter == 1) stp = stp1;

			for (i = 0 ; i < n; ++i) w[i] = g[i];

			while (mcsrch(x, f, g, w, ispt + point * n, diag) == -1)
			{
			  f = fi.setFunctionAndGradient(x, g);
			} 
			errCode = 1;

			if (info != 1)
			{
			  errCode = -1;
				String s = "Line search failed. See documentation of routine mcsrch." +
				           NL + "Error return of line search: info = " + info + NL +
				           " Possible causes: function or gradient are incorrect, " +
				           "or incorrect tolerances."; 
				throw new LBFGSException(errCode, s);
			}

			nfun += nfev;
			npt  = point * n;

			iscn = ispt + npt;
			iycn = iypt + npt;
			for (i = 0; i < n; ++i)
			{
				w[iscn + i] *= stp;
				w[iycn + i] = g[i] - w[i];
			}

			++point;
			if (point == corrKept) point = 0;

			gnorm = sqrt(ddot(n, g, 0, g, 0));
			xnorm = sqrt(ddot(n, x, 0, x, 0));
			xnorm = max(1.0, xnorm);

			if (gnorm / xnorm <= eps) finish = true;

			if (outFreq >= 0) lb1(n, x, f, g);

			if (finish)
			{
				// complete ... exit
			  errCode = 0;
				return;
			}
		}
	}

	/**
   * Print debugging and status messages for <code>lbfgs</code>.
   * Depending on the parameter <code>outFreq</code> and <code>outAmount</code>,
   * variables. This can include number of function evaluations, current
   * function value, etc. The messages are output to <code>System.out</code>.
   * 
   * <p>
   *		<code>outfreq</code> specifies the frequency of the output:
   *		<ul>
   *		<li> <code>outfreq &lt; 0</code>: no output is generated,
   *		<li> <code>outfreq = 0</code>: output only at first and last iteration,
   *		<li> <code>outfreq &gt; 0</code>: output every <code>outfreq</code> iterations.
   *		</ul>
   *
   * <p>
   *		<code>outAmount</code> specifies the type of output generated:
   *		<ul>
   *		<li> <code>outAmount = 0</code>: iteration count, number of function 
   *			evaluations, function value, norm of the gradient, and steplength,
   *		<li> <code>outAmount = 1</code>: same as <code>outAmount=0</code>, plus vector of
   *			variables and  gradient vector at the initial point,
   *		<li> <code>outAmount = 2</code>: same as <code>outAmount=1</code>, plus vector of
   *			variables,
   *		<li> <code>outAmount = 3</code>: same as <code>outAmount=2</code>, plus gradient vector.
   *		</ul>
   *
   * @param n Number of free parameters.
   * @param x Current solution.
   * @param f Function value at current solution.
   * @param g Gradient at current solution <code>x</code>.
   */
	private void lb1(int n, double[] x, double f, double[] g)
	{
		int i;

		if (outAmount > 0)
		{
			if (iter == 0)
			{
				System.out.println("*************************************************");
				System.out.println("  n = " + n + "   number of corrections = " + corrKept +
					               "\n       initial values");
				System.out.println(" f =  " + f + "   gnorm =  " + gnorm);
				if (outAmount >= 1)
				{
					System.out.print(" vector x = ");
					for (i = 0; i < n; ++i) System.out.print("  " + x[i]);
					System.out.println("");
	
					System.out.print(" gradient vector g = ");
					for (i = 0; i < n; ++i) System.out.print("  " + g[i]);
					System.out.println("");
				}
				System.out.println("*************************************************");
				System.out.println("\ti\tnfn\tfunc\tgnorm\tsteplength");
			}
			else
			{
				if ((outFreq == 0 ) && ((iter != 1) && !finish)) return;
				if (outFreq != 0 )
				{
					if ((iter - 1) % outFreq == 0 || finish)
					{
						if ((outAmount > 1) && (iter > 1))
							System.out.println("\ti\tnfn\tfunc\tgnorm\tsteplength");
						System.out.println("\t" + iter + "\t" + nfun + "\t" + f +
							               "\t" + gnorm + "\t" + stp);
					}
					else
					{
						return;
					}
				}
				else
				{
					if ((outAmount > 1) && finish)
						System.out.println("\ti\tnfn\tfunc\tgnorm\tsteplength");
					System.out.println("\t" + iter + "\t" + nfun + "\t" + f + "\t" + gnorm +
						               "\t" + stp);
				}
				if ((outAmount == 2) || (outAmount == 3))
				{
					if (finish)
					{
						System.out.print(" final point x = ");
					}
					else
					{
						System.out.print(" vector x = ");
					}
					for (i = 0; i < n; ++i) System.out.print("  " + x[i]);
					System.out.println("");
					if (outAmount == 3)
					{
						System.out.print(" gradient vector g = ");
						for (i = 0; i < n; ++i) System.out.print("  " + g[i]);
						System.out.println("");
					}
				}
				if (finish)
					System.out.println("The minimization terminated without " +
					                   "detecting errors." + NL + "(errCode = 0)");
			}
		}
		return;
	}

	/**
	 * Compute the sum of a vector times a scalara plus another vector.
	 * Adapted from the subroutine <code>daxpy</code> in <code>lbfgs.f</code>.
	 */ 
	private void daxpy(int n, double da, double[] dx, int ix0, double[] dy)
	{
		int i;

		if (da == 0.0) return;
		for (i = 0; i < n; ++i) dy[i] += da * dx[ix0 + i];
	}

	/**
	 * Compute the dot product of two vectors.
   * Adapted from the subroutine <code>ddot</code> in <code>lbfgs.f</code>.
	 */ 
	private double ddot(int n, double[] dx, int ix0, double[] dy, int iy0)
	{
		int i;
		double dtemp = 0.0;

		for (i = 0; i < n; ++i) dtemp += dx[ix0 + i] * dy[iy0 + i];
		return dtemp;
	}

  /**
   * Minimize a function along a search direction. This code is
   * a Java translation of the function <code>MCSRCH</code> from
   * <code>lbfgs.f</code>, which in turn is a slight modification of
   * the subroutine <code>CSRCH</code> of More' and Thuente.
   * This function, in turn, calls <code>mcstep</code>.<p>
   *
   * <p> The Java translation was effected mostly mechanically, with some
   * manual clean-up; in particular, array indices start at 0 instead of 1.
   * Most of the comments from the Fortran code have been pasted in here
   * as well.
   *
   * <p> The purpose of <code>mcsrch</code> is to find a step which satisfies
   * a sufficient decrease condition and a curvature condition.<p>
   *
   * <p> At each stage this function updates an interval of uncertainty with
   * endpoints <code>stx</code> and <code>sty</code>. The interval of
   * uncertainty is initially chosen so that it contains a
   * minimizer of the modified function
   * 
   * <pre>
   *      f(x+stp*s) - f(x) - ftol*stp*(gradf(x)'s).
   * </pre>
   * 
   * <p> If a step is obtained for which the modified function
   * has a nonpositive function value and nonnegative derivative,
   * then the interval of uncertainty is chosen so that it
   * contains a minimizer of <code>f(x+stp*s)</code>.<p>
   * 
   * <p> The line search routine will terminate if the
   * relative width of the interval of uncertainty is less than
   * <code>EPSILON</code>.
   * 
   * <p> The algorithm is designed to find a step which satisfies
   * the sufficient decrease condition
   * 
   * <pre>
   *       f(x+stp*s) <= f(X) + ftol * stp * (gradf(x)'s),
   * </pre>
   * 
   * and the curvature condition
   * 
   * <pre>
   *       abs(gradf(x + stp * s)'s)) <= gtol * abs(gradf(x)'s).
   * </pre>
   * 
   * <p> If <code>ftol</code> is less than <code>gtol</code> and if, for
   * example, the function is bounded below, then there is always a step which
   * satisfies both conditions. If no step can be found which satisfies both
   * conditions, then the algorithm usually stops when rounding
   * errors prevent further progress. In this case <code>stp</code> only
   * satisfies the sufficient decrease condition.<p>
   *
   * @author Original Fortran version by Jorge J. More' and David J. Thuente
   *   as part of the Minpack project, June 1983, Argonne National 
   *   Laboratory. Java translation by Robert Dodier, August 1997. Java
   *   Streamlining by Jim Hipp, March 2008.
   *
   * @param x On entry this contains the base point for the line search.
   *          On exit it contains <code>x + stp*s</code>.
   * @param f On entry this contains the value of the objective function
   *          at <code>x</code>. On exit it contains the value of the objective
   *          function at <code>x + stp*s</code>.
   * @param g On entry this contains the gradient of the objective function
   *          at <code>x</code>. On exit it contains the gradient at
   *          <code>x + stp*s</code>.
   * @param s The search direction.
   * @param is0 Offset into s.
   * @param wa Temporary storage array, of length <code>n</code>.
   * @return The variable info. This is an output variable, which can have
   *         these values:
   *         
   *   <ul>
   *   <li><code>info = 0</code> Improper input parameters.
   *   <li><code>info = -1</code> A return is made to compute the function and
   *                              gradient.
   *   <li><code>info = 1</code> The sufficient decrease condition and
   *                             the directional derivative condition hold.
   *   <li><code>info = 2</code> Relative width of the interval of uncertainty
   *                             is at most <code>EPSILON</code>.
   *   <li><code>info = 3</code> Number of function evaluations has reached
   *                             <code>maxfev</code>.
   *   <li><code>info = 4</code> The step is at the lower bound
   *                             <code>stpmin</code>.
   *   <li><code>info = 5</code> The step is at the upper bound
   *                             <code>stpmax</code>.
   *   <li><code>info = 6</code> Rounding errors prevent further progress.
   *                             There may not be a step which satisfies the
   *                             sufficient decrease and curvature conditions.
   *                             Tolerances may be too small.
   *   </ul>
   */
  private int mcsrch(double[] x, double f, double[] g, double[] s,
                     int is0, double[] wa)
  {
    int j;
    
    double dg, dgm, fm, ftest1;
    dg = dgm = fm = ftest1 = 0.0;
    
    int n = x.length;
    
    double p66 = 0.66;
    double xtrapf = 4.0;

    if (info != -1)
    {
      infoc = 1;
      if ((n <= 0) || (stp <= 0) || (ftol < 0) || (gtol < 0) ||
          (stpmin < 0) || (stpmax) < (stpmin) || (maxfev <= 0)) return info;

      // Compute the initial gradient in the search direction
      // and check that s is a descent direction.

      dginit = 0;

      for (j = 0; j < n; ++j) dginit += g[j] * s[is0 + j];

      if (dginit >= 0)
      {
        System.out.println("The search direction is not a descent direction.");
        return info;
      }

      brackt = false;
      stage1 = true;
      nfev = 0;
      finit = f;
      dgtest = ftol * dginit;
      width = stpmax - stpmin;
      width1 = 2.0 * width;

      for (j = 0; j < n; ++j) wa[j] = x[j];

      // The variables stx, fx, dgx contain the values of the step,
      // function, and directional derivative at the best step.
      // The variables sty, fy, dgy contain the value of the step,
      // function, and derivative at the other endpoint of
      // the interval of uncertainty.
      // The variables stp, f, dg contain the values of the step,
      // function, and derivative at the current step.

      stx = 0.0;
      fx[0] = finit;
      dgx[0] = dginit;
      sty = 0.0;
      fy[0] = finit;
      dgy[0] = dginit;
    }

    while (true)
    {
      if (info != -1)
      {
        // Set the minimum and maximum steps to correspond
        // to the present interval of uncertainty.

        if (brackt)
        {
          stmin = min(stx, sty);
          stmax = max(stx, sty);
        }
        else
        {
          stmin = stx;
          stmax = stp + xtrapf * (stp - stx);
        }

        // Force the step to be within the bounds stpmax and stpmin.

        stp = max(stp, stpmin);
        stp = min(stp, stpmax);

        // If an unusual termination is to occur then let
        // stp be the lowest point obtained so far.

        if ((brackt && ((stp <= stmin) || (stp >= stmax))) ||
          (nfev >= maxfev - 1) || (infoc == 0) ||
          (brackt && (stmax - stmin <= EPSILON * stmax))) stp = stx;

        // Evaluate the function and gradient at stp
        // and compute the directional derivative.
        // We return to main program to obtain F and G.

        for (j = 0; j < n ; ++j) x[j] = wa[j] + stp * s[is0 + j];

        info = -1;
        return info;
      }

      ++nfev;
      info = 0;
      dg = 0;

      for (j = 0; j < n ; ++j)  dg += g[j] * s[is0 + j];

      ftest1 = finit + stp * dgtest;

      // Test for convergence.

      if (( brackt && ((stp <= stmin) || (stp >= stmax))) ||
        (infoc == 0)) info = 6;

      if ((stp == stpmax) && (f <= ftest1) &&
        (dg <= dgtest)) info = 5;

      if ((stp == stpmin) && ((f > ftest1) ||
        (dg >= dgtest))) info = 4;

      if (nfev >= maxfev) info = 3;

      if (brackt && (stmax - stmin <= EPSILON * stmax)) info = 2;

      if ((f <= ftest1) && (abs(dg) <= -dginit * gtol)) info = 1;

      // Check for termination.

      if (info != 0) return info;

      // In the first stage we seek a step for which the modified
      // function has a nonpositive value and nonnegative derivative.

      if (stage1 && (f <= ftest1) &&
        (dg >= min(ftol , gtol) * dginit)) stage1 = false;

      // A modified function is used to predict the step only if
      // we have not obtained a step for which the modified
      // function has a nonpositive function value and nonnegative
      // derivative, and if a lower function value has been
      // obtained but the decrease is not sufficient.

      if (stage1 && (f <= fx[0]) && (f > ftest1))
      {
        // Define the modified function and derivative values.

        fm = f - stp * dgtest;
        fxm[0] = fx[0] - stx * dgtest;
        fym[0] = fy[0] - sty * dgtest;
        dgm = dg - dgtest;
        dgxm[0] = dgx[0] - dgtest;
        dgym[0] = dgy[0] - dgtest;

        // Call mcstep to update the interval of uncertainty
        // and to compute the new step.

        mcstep(fxm, dgxm, fym, dgym, fm, dgm);

        // Reset the function and gradient values for f.

        fx[0] = fxm[0] + stx * dgtest;
        fy[0] = fym[0] + sty * dgtest;
        dgx[0] = dgxm[0] + dgtest;
        dgy[0] = dgym[0] + dgtest;
      }
      else
      {
        // Call mcstep to update the interval of uncertainty
        // and to compute the new step.

        mcstep(fx, dgx, fy, dgy, f, dg);
      }

      // Force a sufficient decrease in the size of the
      // interval of uncertainty.

      if (brackt)
      {
        if (abs(sty - stx) >= p66 * width1)
          stp = stx + 0.5 * (sty - stx);
        width1 = width;
        width = abs(sty - stx);
      }
    }
  }
  
  /**
   * The purpose of this function is to compute a safeguarded step for
   * a linesearch and to update an interval of uncertainty for
   * a minimizer of the function.
   * 
   * <p> The parameter <code>stx</code> contains the step with the least
   * function value. The parameter <code>stp</code> contains the current step.
   * It is assumed that the derivative at <code>stx</code> is negative in the
   * direction of the step. If <code>brackt[0]</code> is <code>true</code> 
   * when <code>mcstep</code> returns then a minimizer has been bracketed in
   * an interval of uncertainty with endpoints <code>stx</code> and
   * <code>sty</code>.
   * 
   * <p> Variables that must be modified by <code>mcstep</code> are 
   * implemented as 1-element arrays.
   * 
   * <p> On return infoc from <code>mcstep</code>, this is set as follows:
   *     If <code>infoc</code> is 1, 2, 3, or 4, then the step has been
   *     computed successfully. Otherwise <code>infoc</code> = 0, and this
   *     indicates improper input parameters.
   *
   * @param fx Function value at the best step obtained so far. 
   *   This variable is modified by <code>mcstep</code>.
   * @param dx Derivative at the best step obtained so far. The derivative
   *   must be negative in the direction of the step, that is, <code>dx</code>
   *   and <code>stp-stx</code> must have opposite signs. 
   *   This variable is modified by <code>mcstep</code>.
   * @param fy Function value at the other endpoint of the interval of uncertainty.
   *   This variable is modified by <code>mcstep</code>.
   * @param dy Derivative at the other endpoint of the interval of
   *   uncertainty. This variable is modified by <code>mcstep</code>.
   * @param fp Function value at the current step.
   * @param dp Derivative at the current step.
   *
   * @author Jorge J. More, David J. Thuente: original Fortran version,
   *         as part of Minpack project. Argonne Nat'l Laboratory, June 1983.
   *         Robert Dodier: Java translation, August 1997. Jim Hipp: Java
   *         streamlining, March 2008.
   */
  private void mcstep(double[] fx, double[] dx,
                      double[] fy, double[] dy,
                      double fp, double dp)
  {
    boolean bound;
    double gamma, p, q, r, s, sgnd, stpc, stpf, stpq, theta;

    infoc = 0;

    if ((brackt && ((stp <= min(stx, sty)) || (stp >= max(stx, sty)))) ||
      (dx[0] * (stp - stx) >= 0.0) || (stmax < stmin)) return;

    // Determine if the derivatives have opposite sign.

    sgnd = dp * (dx[0] / abs(dx[0]));

    if (fp > fx[0])
    {
      // First case. A higher function value.
      // The minimum is bracketed. If the cubic step is closer
      // to stx than the quadratic step, the cubic step is taken,
      // else the average of the cubic and quadratic steps is taken.

      infoc = 1;
      bound = true;
      theta = 3.0 * (fx[0] - fp) / (stp - stx) + dx[0] + dp;
      s = max(abs(theta), max(abs(dx[0]), abs(dp)));
      gamma = s * sqrt((theta / s) * (theta / s) - (dx[0] / s) * (dp / s));
      if (stp < stx) gamma = -gamma;
      p = (gamma - dx[0]) + theta;
      q = ((gamma - dx[0]) + gamma) + dp;
      r = p / q;
      stpc = stx + r * (stp - stx);
      stpq = stx +
           ((dx[0] / ((fx[0] - fp) / (stp - stx) + dx[0])) / 2.0) *
           (stp - stx);
      if (abs(stpc - stx) < abs(stpq - stx))
      {
        stpf = stpc;
      }
      else
      {
        stpf = stpc + (stpq - stpc) / 2.0;
      }
      brackt = true;
    }
    else if (sgnd < 0.0)
    {
      // Second case. A lower function value and derivatives of
      // opposite sign. The minimum is bracketed. If the cubic
      // step is closer to stx than the quadratic (secant) step,
      // the cubic step is taken, else the quadratic step is taken.

      infoc = 2;
      bound = false;
      theta = 3.0 * (fx[0] - fp) / (stp - stx) + dx[0] + dp;
      s = max(abs(theta), max(abs(dx[0]), abs(dp)));
      gamma = s * sqrt((theta / s) * (theta / s) - (dx[0] / s) * (dp / s));
      if (stp > stx) gamma = -gamma;
      p = (gamma - dp) + theta;
      q = ((gamma - dp) + gamma) + dx[0];
      r = p/q;
      stpc = stp + r * (stx - stp);
      stpq = stp + (dp / (dp - dx[0])) * (stx - stp);
      if (abs(stpc - stp) > abs(stpq - stp))
      {
        stpf = stpc;
      }
      else
      {
        stpf = stpq;
      }
      brackt = true;
    }
    else if (abs(dp) < abs(dx[0]))
    {
      // Third case. A lower function value, derivatives of the
      // same sign, and the magnitude of the derivative decreases.
      // The cubic step is only used if the cubic tends to infinity
      // in the direction of the step or if the minimum of the cubic
      // is beyond stp. Otherwise the cubic step is defined to be
      // either stmin or stmax. The quadratic (secant) step is also
      // computed and if the minimum is bracketed then the the step
      // closest to stx is taken, else the step farthest away is taken.

      infoc = 3;
      bound = true;
      theta = 3.0 * (fx[0] - fp) / (stp - stx) + dx[0] + dp;
      s = max(abs(theta), max(abs(dx[0]), abs(dp)));
      gamma = s * sqrt(max(0.0, (theta / s) * (theta / s) -
                                (dx[0] / s) * (dp / s)));
      if (stp > stx) gamma = -gamma;
      p = (gamma - dp) + theta;
      q = (gamma + (dx[0] - dp)) + gamma;
      r = p / q;
      if ((r < 0.0) && (gamma != 0.0))
      {
        stpc = stp + r * (stx - stp);
      }
      else if (stp > stx)
      {
        stpc = stmax;
      }
      else
      {
        stpc = stmin;
      }
      stpq = stp + (dp / (dp - dx[0])) * (stx - stp);

      if (brackt)
      {
        if (abs(stp - stpc) < abs(stp - stpq))
        {
          stpf = stpc;
        }
        else
        {
          stpf = stpq;
        }
      }
      else
      {
        if (abs(stp - stpc) > abs(stp - stpq))
        {
          stpf = stpc;
        }
        else
        {
          stpf = stpq;
        }
      }
    }
    else
    {
      // Fourth case. A lower function value, derivatives of the
      // same sign, and the magnitude of the derivative does
      // not decrease. If the minimum is not bracketed, the step
      // is either stmin or stmax, else the cubic step is taken.

      infoc = 4;
      bound = false;
      if (brackt)
      {
        theta = 3.0 * (fp - fy[0]) / (sty - stp) + dy[0] + dp;
        s = max(abs(theta), max(abs(dy[0]), abs(dp)));
        gamma = s * sqrt((theta / s) * (theta / s) - (dy[0] / s) * (dp / s));
        if (stp > sty) gamma = -gamma;
        p = (gamma - dp) + theta;
        q = ((gamma - dp) + gamma) + dy[0];
        r = p / q;
        stpc = stp + r * (sty - stp);
        stpf = stpc;
      }
      else if (stp > stx)
      {
        stpf = stmax;
      }
      else
      {
        stpf = stmin;
      }
    }

    // Update the interval of uncertainty. This update does not
    // depend on the new step or the case analysis above.

    if (fp > fx[0])
    {
      sty = stp;
      fy[0] = fp;
      dy[0] = dp;
    }
    else
    {
      if (sgnd < 0.0)
      {
        sty = stx;
        fy[0] = fx[0];
        dy[0] = dx[0];
      }
      stx = stp;
      fx[0] = fp;
      dx[0] = dp;
    }

    // Compute the new step and safeguard it.

    stpf = min(stmax , stpf);
    stpf = max(stmin , stpf);
    stp = stpf;

    if (brackt && bound)
    {
      if (sty > stx)
      {
        stp = min(stx + 0.66 * (sty - stx), stp);
      }
      else
      {
        stp = max(stx + 0.66 * (sty - stx), stp);
      }
    }

    return;
  }
  
}
