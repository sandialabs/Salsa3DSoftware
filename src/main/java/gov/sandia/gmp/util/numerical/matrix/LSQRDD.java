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
package gov.sandia.gmp.util.numerical.matrix;

import static gov.sandia.gmp.util.globals.Globals.NL;
import static gov.sandia.gmp.util.numerical.machine.DhbMath.getMachinePrecision;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
//import gov.sandia.gmp.util.statistics.Statistic;

import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.doubledouble.DoubleDouble;

/**
* <p> LSQR least-squares matrix solution algorithm as originally defined by
* for ACM TOMS as Algorithm 583 in 1982. This algorithm has undergone
* several modifications over the years including this translation to the
* Java programming language which includes a multi-threaded solution of
* the bidiagonalization operation (primarily function aProd).
*
* <p> Besides a general clean-up of the syntax this rendition also added a
* different way of outputting information. The new method allows the user to
* define a BufferedWriter into which LSQR output is written. This object
* must be set (see public void setWriter(BufferedWriter writer)) if that
* option is selected or an exception will be thrown. Output options include
* "None", "Screen", "Writer", and "Screen & Writer". The default is "Screen"
* but each can be selected using one of the following functions:
* 
*   public void setOutputNone();
*   public void setOutputScreen();
*   public void setOutputWriter();
*   public void setOutputScreenAndWriter();
* 
* <p> In addition to output all of the standard output normalization, condition
* numbers, and limits can be retrieved following the solution. These retrieval
* functions include:
*
*   public double aNorm();
*   public double arNorm();
*   public double bNorm();
*   public double xNorm();
*   public double aCond();
*   public double systemCompatibilityMeasure();
*   public double LeastSquaresAccuracyMeasure();
*   public double aTol();
*   public double bTol();
*   public double damp();
*   
*   public int stopCondition();
*   public int iterationLimit();
*   public int conditionLimit();
*
*   public int iterationCount();
*   public int matrixRows();
*   public int matrixCols();
*
* <p> On input aTol, bTol, damp, iterationLimit, and conditionLimit can all
* be set before calling solve.
*
* <p> The primary function call is:
* 
* <p>     public int solve(ArrayList<SparseMatrixVector> sprsRow,
*                          ArrayList<SparseMatrixVector> sprsCol,
*                          ArrayListDouble bRHS, double[] x, double[] se)
*                    throws IOException
*
* <p> The vector bRHS is the rhs vector of the problem definition, x is the
* solution vector and se is the solution error. Both x and se are solved for and
* filled on return. The lists sprsRow and sprsCol are lists of row-ordered and
* column-ordered sparse matrix vectors, respectively. The primary solution
* calculation can be performed entirely are partly in a concurrent parallel
* mode. The SolveType setting for parallel concurrency can take on any of the
* eight values, NONE, SCALE, NORM, APROD, SCALE_NORM, SCALE_APROD, NORM_APROD,
* or SCALE_NORM_APROD. The setting determines which part, if any, is performed
* using parallel concurrency.
* 
* <p> Similarly, the DoubleDouble SolveType setting can be set in the same
* fashion as the parallel concurrency setting, to perform any part of the
* calculation using DoubleDouble precision.
* 
* <p> The default parallel and DoubleDouble calculation settings are
*     aUseParallel     = SolveType.NORM_SCALE_APROD;
*     aUseDoubleDouble = SolveType.NORM_SCALE;
* 
* <p> The original analytic description of the solution methodology follows:
* ****************************************************************************
*
* <p> LSQR  finds a solution x to the following problems:
*
* <p> 1. Unsymmetric equations --    solve  A*x = b
*
* <p> 2. Linear least squares  --    solve  A*x = b
*                                    in the least-squares sense
*
* <p> 3. Damped least squares  --    solve  (   A    )*x = ( b )
*                                           ( damp*I )     ( 0 )
*                                    in the least-squares sense
*
* <p> where A is a matrix with m rows and n columns, b is an
*     m-vector, and damp is a scalar.  (All quantities are real.)
*     The matrix A is intended to be large and sparse.  It is used
*     in function aprod to update v and u as
*     
* <p>            u[sprsRowIndx[i]] += aSprs[i] * v[sprsColIndx[i]];
*                
* <p>            (effectively y = y + A * x) or as

* <p>            v[sprsColIndx[i]] += aSprs[i] * u[sprsRowIndx[i]];
*                
* <p>            (which is x = x + A' * y).
*
* <p> The rhs vector b is input via u, and subsequently overwritten.
*
* <p> Note:  LSQR uses an iterative method to approximate the solution.
*     The number of iterations required to reach a certain accuracy
*     depends strongly on the scaling of the problem.  Poor scaling of
*     the rows or columns of A should therefore be avoided where
*     possible.
*
* <p> For example, in problem 1 the solution is unaltered by
*     row-scaling.  If a row of A is very small or large compared to
*     the other rows of A, the corresponding row of ( A  b ) should be
*     scaled up or down.
*
* <p> In problems 1 and 2, the solution x is easily recovered
*     following column-scaling.  Unless better information is known,
*     the nonzero columns of A should be scaled so that they all have
*     the same Euclidean norm (e.g., 1.0).
*
* <p> In problem 3, there is no freedom to re-scale if damp is
*     nonzero.  However, the value of damp should be assigned only
*     after attention has been paid to the scaling of A.
*
* <p> The parameter damp is intended to help regularize
*     ill-conditioned systems, by preventing the true solution from
*     being very large.  Another aid to regularization is provided by
*     the parameter lsqrACond, which may be used to terminate iterations
*     before the computed solution becomes very large.
*
*
* <p> Notation
*     --------
*
* <p> The following quantities are used in discussing the subroutine
*     parameters:
*
* <p> Abar   =  (   A    ),          bbar  =  ( b )
*               ( damp*I )                    ( 0 )
*
* <p> r      =  b  -  A*x,           rbar  =  bbar  -  Abar*x
*
* <p> rnorm  =  sqrt( norm(r)**2  +  damp**2 * norm(x)**2 )
*            =  norm( rbar )
*
* <p> RELPR  =  the relative precision of floating-point arithmetic
*               on the machine being used. (typically near 1.0e-16
*               on modern machines in double precision).
*
* <p> LSQR  minimizes the function rnorm with respect to x.
*
*
* <p> Parameters
*     ----------
*
* <p> lsqrRows input      The number of rows in A. Taken from u.length.
*
* <p> lsqrCols input      The number of columns in A. Taken from x.length.
*
* <p> damp     input      The damping parameter for problem 3 above.
*                         (damp should be 0.0 for problems 1 and 2.)
*                         If the system A*x = b is incompatible, values
*                         of damp in the range 0 to sqrt(RELPR)*norm(A)
*                         will probably have a negligible effect.
*                         Larger values of damp will tend to decrease
*                         the norm of x and reduce the number of
*                         iterations required by LSQR.
*
* <p>                     The work per iteration and the storage needed
*                         by LSQR are the same for all values of damp.
*
* <p> u(nrow)  input      The rhs vector b.  Beware that u is
*                         over-written by LSQR.
*
* <p> x(ncol)  output     Returns the computed solution x.
*
* <p> se(ncol) output     Returns standard error estimates for the
*                         components of X.  For each i, SE(i) is set
*                         to the value  rnorm * sqrt( sigma(i,i) / T ),
*                         where sigma(i,i) is an estimate of the i-th
*                         diagonal of the inverse of Abar(transpose)*Abar
*                         and  T = 1      if  m .le. n,
*                              T = m - n  if  m .gt. n  and  damp = 0,
*                              T = m      if  damp .ne. 0.
*
* <p> lsqrATol  input     An estimate of the relative error in the data
*                         defining the matrix A.  For example,
*                         if A is accurate to about 6 digits, set
*                         lsqrATol = 1.0e-6 (default). Set / returned
*                         with function aTol().
*
* <p> lsqrBTol  input     An extimate of the relative error in the data
*                         defining the rhs vector b.  For example,
*                         if b is accurate to about 6 digits, set
*                         lsqrBTol = 1.0e-6 (default). Set / returned
*                         with function bTol().
*
* <p> lsqrConLim  input   An upper limit on cond(Abar), the apparent
*                         condition number of the matrix Abar.
*                         Iterations will be terminated if a computed
*                         estimate of cond(Abar) exceeds conLim.
*                         This is intended to prevent certain small or
*                         zero singular values of A or Abar from
*                         coming into effect and causing unwanted growth
*                         in the computed solution.
*
* <p>                     conLim and damp may be used separately or
*                         together to regularize ill-conditioned systems.
*
* <p>                     Normally, conLim should be in the range
*                         1000 to 1/RELPR.
*                         Suggested value:
*                         conLim = 1/(100*RELPR)  for compatible systems,
*                         conLim = 1/(10*sqrt(RELPR)) for least squares.
*
* <p>                     Set / returned with function condLimit().
*
* <p>          Note:  If the user is not concerned about the parameters
*              lsqrATol, lsqrBTol and conLim, any or all of them may be set
*              to zero.  The effect will be the same as the values
*              RELPR, RELPR and 1/RELPR respectively.
*
* <p> lsqrItnLim  input   An upper limit on the number of iterations.
*                         Suggested value:
*                         lsqrItnLim = n/2  for well-conditioned systems
*                                           with clustered singular values,
*                         lsqrItnLim = 4*n  otherwise.
*                         Set / returned with function iterationLimit().
*
* <p> lsqrIStop  output   An integer giving the reason for termination
*                         (returned by function solve(...) and function
*                          stopCondition()):
*
* <p>             0       x = 0  is the exact solution.
*                         No iterations were performed.
*
* <p>             1       The equations A*x = b are probably
*                         compatible.  Norm(A*x - b) is sufficiently
*                         small, given the values of lsqrATol and lsqrBTol.
*
* <p>             2       The system A*x = b is probably not
*                         compatible.  A least-squares solution has
*                         been obtained that is sufficiently accurate,
*                         given the value of lsqrATol.
*
* <p>             3       An estimate of cond(Abar) has exceeded
*                         conLim.  The system A*x = b appears to be
*                         ill-conditioned. There could be an inconsistency
*                         in the sparse matrix or indirect index arrays
*                         (aSprs, indxMSprs, indxDSprs).
*
* <p>             4       The equations A*x = b are probably
*                         compatible.  Norm(A*x - b) is as small as
*                         seems reasonable on this machine.
*
* <p>             5       The system A*x = b is probably not
*                         compatible.  A least-squares solution has
*                         been obtained that is as accurate as seems
*                         reasonable on this machine.
*
* <p>             6       Cond(Abar) seems to be so large that there is
*                         no point in doing further iterations,
*                         given the precision of this machine. There could
*                         be an inconsistency in the sparse matrix or
*                         indirect index arrays (aSprs, indxMSprs, indxDSprs).
*
* <p>             7       The iteration limit lsqrItnLim was reached.
* 
* <p> lsqrIter    output  The number of iterations performed. Accessed
*                         from function iterationCount().
*
* <p> lsqrANorm   output  An estimate of the Frobenius norm of  Abar.
*                         This is the square-root of the sum of squares
*                         of the elements of Abar.
*                         If "damp" is small and if the columns of A
*                         have all been scaled to have length 1.0,
*                         lsqrANorm should increase to roughly sqrt(n).
*                         A radically different value for lsqrANorm may
*                         indicate an error in in the sparse
*                         matrix or indirect index arrays (aSprs,
*                         indxMSprs, indxDSprs). Accessed from function
*                         aNorm().
*
* <p> lsqrACond   output  An estimate of cond(Abar), the condition
*                         number of Abar.  A very high value of lsqrACond
*                         may indicate an inconsistency in the sparse
*                         matrix or indirect index arrays (aSprs,
*                         indxMSprs, indxDSprs). Accessed from function
*                         aCond().
*
* <p> lsqrRNorm   output  An estimate of the final value of norm(rbar),
*                         the function being minimized (see notation
*                         above).  This will be small if A*x = b has
*                         a solution. Accessed from function rNorm().
*
* <p> lsqrARNorm  output  An estimate of the final value of
*                         norm( Abar(transpose)*rbar ), the norm of
*                         the residual for the usual normal equations.
*                         This should be small in all cases.  (lsqrARNorm
*                         will often be smaller than the true value
*                         computed from the output vector X.) Accessed
*                         from function arNorm().
*
* <p> lsqrXNorm   output  An estimate of the norm of the final
*                         solution vector X. Accessed from function
*                         xNorm().
*
*
* <p> References
*     ----------
*
* <p> C.C. Paige and M.A. Saunders,  LSQR: An algorithm for sparse
*          linear equations and sparse least squares,
*          ACM Transactions on Mathematical Software 8, 1 (March 1982),
*          pp. 43-71.
*
* <p> C.C. Paige and M.A. Saunders,  Algorithm 583, LSQR: Sparse
*          linear equations and least-squares problems,
*          ACM Transactions on Mathematical Software 8, 2 (June 1982),
*          pp. 195-209.
*
* <p> C.L. Lawson, R.J. Hanson, D.R. Kincaid and F.T. Krogh,
*          Basic linear algebra subprograms for Fortran usage,
*          ACM Transactions on Mathematical Software 5, 3 (Sept 1979),
*          pp. 308-323 and 324-325.
*-----------------------------------------------------------------------
*
* <p> LSQR development:
*     22 Feb 1982: LSQR sent to ACM TOMS to become Algorithm 583.
*     15 Sep 1985: Final F66 version.  LSQR sent to "misc" in netlib.
*     13 Oct 1987: Bug (Robert Davies, DSIR).  Have to delete
*                     IF ( (1.0 + DABS(T)) .LE. 1.0) GO TO 200
*                  from loop 200.  The test was an attempt to reduce
*                  underflows, but caused W(I) not to be updated.
*     17 Mar 1989: First F77 version.
*     04 May 1989: Bug (David Gay, AT&T).  When the second beta is zero,
*                  lsqrRNorm = 0 and
*                  TEST2 = lsqrARNorm / (lsqrANorm * lsqrRNorm) overflows.
*                  Fixed by testing for lsqrRNorm = 0.
*     05 May 1989: Sent to "misc" in netlib.
*     24 Mar 2008: Converted to Java (James R Hipp, Sandia National
*                  Laboratories).
*     28 Aug 2009: Added multi-threaded solution of aProd (James R Hipp,
*                  Sandia National Laboratories).
*
* <p> Michael A. Saunders            (na.saunders @ NA-net.stanford.edu)
*     Department of Operations Research
*     Stanford University
*     Stanford, CA 94305-4022.
*-----------------------------------------------------------------------
*/
@SuppressWarnings("serial")
public class LSQRDD implements Serializable
{

  /**
   * Machine precision.
   */
  public static final double RELPR = getMachinePrecision();

  /**
   * Output format version
   */
  private static int                aFormatVersion = 2;

  /**
   * A enum used to set the aUseParallel and aUseDoubleDouble flags. These
   * flags the manner in which the three major calculational components 
   * (normalization, scaling, and aProd solution) are evaluated.
   * 
   * @author jrhipp
   *
   */
  public enum SolveType
  {
    NONE
    {
      @Override
      public boolean isNone() {return true;};
      @Override
      public boolean isAProd() {return false;};
      @Override
      public boolean isScale() {return false;};
      @Override
      public boolean isNorm() {return false;};
    },
    NORM
    {
      @Override
      public boolean isNone() {return false;};
      @Override
      public boolean isAProd() {return false;};
      @Override
      public boolean isScale() {return false;};
      @Override
      public boolean isNorm() {return true;};
    },
    SCALE
    {
      @Override
      public boolean isNone() {return false;};
      @Override
      public boolean isAProd() {return false;};
      @Override
      public boolean isScale() {return true;};
      @Override
      public boolean isNorm() {return false;};
    },
    APROD
    {
      @Override
      public boolean isNone() {return false;};
      @Override
      public boolean isAProd() {return true;};
      @Override
      public boolean isScale() {return false;};
      @Override
      public boolean isNorm() {return false;};
    },
    NORM_SCALE
    {
      @Override
      public boolean isNone() {return false;};
      @Override
      public boolean isAProd() {return false;};
      @Override
      public boolean isScale() {return true;};
      @Override
      public boolean isNorm() {return true;};
    },
    NORM_APROD
    {
      @Override
      public boolean isNone() {return false;};
      @Override
      public boolean isAProd() {return true;};
      @Override
      public boolean isScale() {return false;};
      @Override
      public boolean isNorm() {return true;};
    },
    SCALE_APROD
    {
      @Override
      public boolean isNone() {return false;};
      @Override
      public boolean isAProd() {return true;};
      @Override
      public boolean isScale() {return true;};
      @Override
      public boolean isNorm() {return false;};
    },
    NORM_SCALE_APROD
    {
      @Override
      public boolean isNone() {return false;};
      @Override
      public boolean isAProd() {return true;};
      @Override
      public boolean isScale() {return true;};
      @Override
      public boolean isNorm() {return true;};
    };

    public abstract boolean isNone();
    public abstract boolean isAProd();
    public abstract boolean isScale();
    public abstract boolean isNorm();
  }

  //private HashMapIntegerKey<Statistic> ddStat = new HashMapIntegerKey<Statistic>(8);

  /**
   * Format version of the input data. This value is initialized to the current
   * formt version and may be changed if readSolution(String filename) is called.
   */
  private int       aFrmtVrsn        = aFormatVersion;

  /**
   * Defines the solution components that are performed in a concurrent
   * parallel fashion.
   */
  private SolveType aUseParallel     = SolveType.NORM_SCALE_APROD;

  /**
   * Defines the solution components that are performed using DoubleDouble
   * precision.
   */
  private SolveType aUseDoubleDouble = SolveType.NORM_SCALE;

  /**
   * The row-ordered sparse matrix.
   */
  private ArrayList<SparseMatrixVector> aRowSprs = null;

  /**
   * The column-ordered sparse matrix.
   */
  private ArrayList<SparseMatrixVector> aColSprs = null;

  /**
   * The RHS vector which must have the same number of entries as aRowSprs.
   * This vector is unchanged after solution.
   */
  private ArrayListDouble  aRHS  = null;

  /**
   * The solution result vector used by both concurrent and sequential solvers.
   * This vector is the same size as aV.
   */
  private double[] aX       = null;

  /**
   * The solution result error vector used by both concurrent and sequential
   * solvers. This vector is the same size as aV.
   */
  private double[] aSE      = null;

  /**
   * The LSQR "u" vector which is initialized to the rhs vector and has the
   * same size (the number of rows in the problem definition). This vector
   * contains the high component if DoubleDouble calculations are performed,
   * and the double result if not.
   */
  private double[] aU       = null;

  /**
   * The LSQR "u" vector low component when DoubleDouble precision is used.
   * This vector is empty or set to zero if double precision is used.
   */
  private double[] aULo     = null;

  /**
   * The LSQR "v" vector which is initialized to 0 and has the same size as
   * the solution vector (aX.length which is the number of columns in the
   * problem definition). This vector contains the high component if
   * DoubleDouble calculations are performed, and the double result if not.
   */
  private double[] aV       = null;

  /**
   * The LSQR "v" vector low component when DoubleDouble precision is used.
   * This vector is empty or set to zero if double precision is used.
   */
  private double[] aVLo     = null;

  // Intrinsics and local variables

  /**
   * Stop condition on return from solve. These include one of eight
   * options as described above.
   */
  private int    lsqrIStop  = 0;      // ISTOP
  
  /**
   * The norm of the input matrix on return from solve.
   */
  private double lsqrANorm  = 0.0;    // ANORM
  
  /**
   * The condition number of the input matrix on return from solve.
   */
  private double lsqrACond  = 0.0;    // ACOND
  
  /**
   * The norm of the residual matrix on return from solve.
   */
  private double lsqrRNorm  = 0.0;    // RNORM
  
  /**
   * The norm of the matrix A^T * r on return from solve.
   */
  private double lsqrARNorm = 0.0;    // ARNORM
  
  /**
   * The norm of the solution vector on return from solve.
   */
  private double lsqrXNorm  = 0.0;    // XNORM
  
  /**
   * The norm of the rhs input vector on return from solve.
   */
  private double lsqrBNorm  = 0.0;    // BNORM
  
  /**
   * The last value of the system compatibility test on
   * return from solve.
   */
  private double lsqrCmptbl = 0.0;    // TEST1
  
  /**
   * The last value of the least-squares accuracy test on
   * return from solve.
   */
  private double lsqrLSAcc  = 0.0;    // TEST2

  /**
   * The total iteration count on return from solve.
   */
  private int    lsqrIter   = 0;      // ITN

  /**
   * The input least-squares damping factor.
   */
  private double lsqrDamp   = 0.0;    // DAMP
  
  /**
   * An estimate of the input matrix data relative error tolerance.
   */
  private double lsqrATol   = 1.0e-6; // ATOL
  
  /**
   * An estimate of the input rhs vector data relative error tolerance.
   */
  private double lsqrBTol   = 1.0e-6; // BTOL

  /**
   * The input matrix ill-conditioned limit.
   */
  private double lsqrConLim = 1.0 / sqrt(RELPR) / 10.0;   // CONLIM
  
  /**
   * The input maximum number of allowed iterations.
   */
  private int    lsqrItnLim = 0;      // ITNLIM

  /**
   * The input number of matrix rows (rhs vector size).
   */
  private int    lsqrRows   = 0;      // M
  
  /**
   * The input number of matrix columns (solution vector size).
   */
  private int    lsqrCols   = 0;      // N
  
  /**
   * Total time to execute the solve function (msec).
   */
  private long   lsqrExTime = 0;      // execution time
  
  /**
   * The final value of the first solution vector entry.
   */
  private double lsqrX0     = 0.0;    // x[0] ... used for output

  /**
   * Array of eight exit messages corresponding to the value of lsqrIStop
   * on exit from solve. 
   */
  private String[][] lsqrExitMsg =
          {{"x = 0 is the exact solution." + NL,
            "No iterations were performed." + NL},
          
           {"The equations A*x = b are probably" + NL,
            "compatible. Norm(A*x = b) is sufficiently" + NL,
            "small, given the values of ATol and BTol." + NL},
          
           {"The system A*x = b is probably not" + NL,
            "compatible. A least-squares solution has" + NL,
            "been obtained that is sufficiently accurate," + NL,
            "given the value of ATol." + NL},
          
           {"An estimate of the cond(Abar) has exceeded" + NL,
            "CONLIM. The system A*x = b appears to be" + NL,
            "ill-conditioned." + NL},

           {"The equations A*x = b are probably" + NL,
            "compatible. Norm(A*x - b) is as small as" + NL,
            "seems reasonable on this machine." + NL},
 
           {"The system A*x = b is probably not" + NL,
            "compatible. A Least-Squares solution has" + NL,
            "been obtained that is as accurate as seems" + NL,
            "reasonable on this machine." + NL},

           {"Cond(Abar) seems to be so large that there is" + NL,
            "no point in doing further iterations," + NL,
            "given the precision of this machine." + NL},
          
           {"The iteration limit lsqrItnLim was reached." + NL}};

  /**
   * Used to provide Screen or BufferedWriter Output.
   */
  private ScreenWriterOutput aScrnWrtr = new ScreenWriterOutput();

  /**
   * Output indention string. All output lines are pre-pended with this string. 
   */
  private String             aOutputIndent = "";

  /**
   * If true only the beginning 10 lines and ending 15 lines of the convergence
   * output table are written. Otherwise, all lines are written.
   */
  private boolean            aTruncateOutTable = false;

  /**
   * If aTruncateOutTable is true then this list stores the last 15 output lines
   * of the convergence table.
   */
  LinkedList<String>         aEndOfOutTable    = null;

  /**
   * Default constructor.
   */
  public LSQRDD()
  {
  }

  /**
   * Set the parallel mode.
   * 
   * @param pm The new parallel mode.
   */
  public void parallelMode(SolveType pm)
  {
    aUseParallel = pm;
  }

  /**
   * Set the precision mode.
   * 
   * @param pm The new precision mode.
   */
  public void precisionMode(SolveType pm)
  {
    aUseDoubleDouble = pm;
  }

  /**
   * Returns the ScreenWriterOutput object so that its properties can
   * be set.
   * 
   * @return The owned ScreenWriterOutput object of this LSQR object.
   */
  public ScreenWriterOutput getScreenWriterOutput()
  {
    return aScrnWrtr;
  }

  /**
   * Returns the stop condition following execution.
   * 
   * @return The stop condition on return from solve.
   */
  public int stopCondition()
  {
    return lsqrIStop;
  }

  /**
   * Returns the stop condition message following execution.
   * 
   * @return The stop condition message on return from solve.
   */
  public String[] stopConditionMessage()
  {
    return lsqrExitMsg[lsqrIStop];
  }

  /**
   * Returns the matrix norm following execution.
   * 
   * @return The matrix norm on return from solve.
   */
  public double aNorm()
  {
    return lsqrANorm;
  }

  /**
   * Returns the matrix condition number estimate following execution.
   * 
   * @return The matrix condition number estimate on return from solve.
   */
  public double aCond()
  {
    return lsqrACond;
  }

  /**
   * Returns the residual vector norm following execution.
   * 
   * @return The residual vector norm on return from solve.
   */
  public double rNorm()
  {
    return lsqrRNorm;
  }

  /**
   * Returns the norm of A^T * r following exectuion.
   * 
   * @return The norm of A^T * r on return from solve.
   */
  public double arNorm()
  {
    return lsqrARNorm;
  }

  /**
   * Returns the solution vector norm following execution.
   * 
   * @return The solution vector norm on return from solve.
   */
  public double xNorm()
  {
    return lsqrXNorm;
  }

  /**
   * Returns the rhs vector norm following execution.
   * 
   * @return The rhs vector norm on return from solve.
   */
  public double bNorm()
  {
    return lsqrBNorm;
  }

  /**
   * Returns the last value of the system compatibility test following
   * execution.
   * 
   * @return The last value of the system compatibility test on return
   *         from solve.
   */
  public double systemCompatibilityMeasure()
  {
    return lsqrCmptbl;
  }

  /**
   * Returns the last value of the least-squares accuracy test following
   * exectuion.
   * 
   * @return The last value of the least-squares accuracy test on return
   *         from solve.
   */
  public double LeastSquaresAccuracyMeasure()
  {
    return lsqrLSAcc;
  }

  /**
   * Returns the solution iteration count following execution.
   * 
   * @return the iteration count on return from solve.
   */
  public int iterationCount()
  {
    return lsqrIter;
  }

  /**
   * Returns the number of matrix rows (size of rhs input vector).
   * 
   * @return The number of matrix rows.
   */
  public int matrixRows()
  {
    return lsqrRows;
  }

  /**
   * Returns the number of matrix columns (size of solution vector).
   * 
   * @return The number of matrix columns.
   */
  public int matrixColumns()
  {
    return lsqrCols;
  }

  /**
   * Sets the maximum allowed iteration limit to il.
   * 
   * @param il The maximum allowed iteration limit.
   */
  public void iterationLimit(int il)
  {
    lsqrItnLim = il;
  }

  /**
   * Returns the maximum allowed iteration limit.
   * 
   * @return The maximum allowed iteration limit.
   */
  public int iterationLimit()
  {
    return lsqrItnLim;
  }

  /**
   * Sets the damping factor to damp.
   * 
   * @param damp The least-squares solution damping factor.
   */
  public void damp(double damp)
  {
    lsqrDamp = damp;
  }

  /**
   * Returns the least-squares solution damping factor.
   * 
   * @return The least-squares solution damping factor.
   */
  public double damp()
  {
    return lsqrDamp;
  }

  /**
   * Sets the input matrix data relative error estimate to atol.
   * 
   * @param atol The input matrix data relatvie error estimate.
   */
  public void aTol(double atol)
  {
    lsqrATol = atol;
  }

  /**
   * Returns the input matrix data relative error estimate.
   * 
   * @return The input matrix data relative error estimate.
   */
  public double aTol()
  {
    return lsqrATol;
  }

  /**
   * Sets the input rhs vector data relative error estimate to atol.
   * 
   * @param atol The input rhs vector data relatvie error estimate.
   */
  public void bTol(double btol)
  {
    lsqrBTol = btol;
  }

  /**
   * Returns the input rhs vector data relative error estimate.
   * 
   * @return The input rhs vector data relative error estimate.
   */
  public double bTol()
  {
    return lsqrBTol;
  }

  /**
   * Sets the matrix condition number limit to cl.
   * 
   * @param cl The matrix condition number limit.
   */
  public void condLimit(double cl)
  {
    lsqrConLim = cl;
  }

  /**
   * Returns the matrix condition number limit.
   * 
   * @return The matirx condition number limit.
   */
  public double condLimit()
  {
    return lsqrConLim;
  }

  /**
   * Process execution time in seconds.
   * 
   * @return Process execution time returned in smallest units (days, hours, min,
   *         sec, msec).
   */
  public String processExecutionTime()
  {
    return Globals.timeString(lsqrExTime);
  }

  /**
   * The public interface to perform the LSQR solution. This function sets up
   * the inputs, creates local vectors, builds any parallel tasks and then calls
   * solve() to perform the LSQR solution.
   *  
   * @param rowSprs The input row oriented sparse matrix. The size of this list
   *                is the number of rows in the solution. The total number of
   *                sparse entries (sum of sprsRow.get(i).size() over all i)
   *                must be equal to the sum of sprsCol.get(j).size() over all
   *                j.
   * @param colSprs The input column oriented sparse matrix. The size of this list
   *                is the number of columns in the solution. The total number of
   *                sparse entries (sum of rowSprs.get(i).size() over all i)
   *                must be equal to the sum of colSprs.get(j).size() over all
   *                j.
   * @param bRHS The RHS input matrix. Must have the same number of entries as
   *             rowSprs (i.e. rowSprs.size() == bRHS.size()).
   * @param x The output solution vector.
   * @param se The output solution error vector.
   * 
   * @return The LSQR stopping criteria.
   * 
   * @throws IOException
   */
  public int solve(ArrayList<SparseMatrixVector> rowSprs,
                   ArrayList<SparseMatrixVector> colSprs,
                   ArrayListDouble bRHS, double[] x, double[] se)
         throws IOException
  {
    // assign inputs

    aRowSprs = rowSprs;
    aColSprs = colSprs;
    aX       = x;
    aSE      = se;
    aRHS     = bRHS;

    // calculate solution

    return resolve();
  }

  /**
   * Performs the LSQR solution given the settings in aRowSprs, aX, aSE, and
   * aRHS. This function creates a transpose sparse matrix and necessary local
   * vectors, builds any parallel tasks, and then calls solve() to perform the
   * LSQR solution.
   * 
   * @return The LSQR stopping criteria.
   * 
   * @throws IOException
   */
  public int resolve() throws IOException
  {
    // verify inputs have been set and that aRowSprs and aRHS sizes are
    // consistent

    if ((aRowSprs == null) || (aRHS == null) || (aX == null) || (aSE == null))
    {
      String s = "Input row-ordered sparse matrix, RHS vector, " +
                 "solution vector, and solution error vector, " +
                 "have not been assigned (null) ...";
      throw new IOException(s);
    }
    if (aRowSprs.size() != aRHS.size())
    {
      String s = "Sparse matrix size (" + aRowSprs.size() +
                 ") and RHS vector size (" + aRHS.size() +
                 ") must be the same ...";
      throw new IOException(s);
    }

    if (aColSprs.size() != aX.length)
    {
      String s = "Column-ordered sparse matrix size (" + aColSprs.size() +
                 ") and solution vector size (" + aX.length +
                 ") must be the same ...";
      throw new IOException(s);
    }

    // set matrix dimensions

    lsqrRows = aRHS.size(); // M
    lsqrCols = aX.length; // N

    // create bidiagonalization V and U arrays. Initialize U with bRHS.

    aV   = new double [lsqrCols];
    aVLo = new double [lsqrCols];
    aU   = aRHS.toArray();
    aULo = new double [aU.length];

    // start threads, create task queue, build tasks if any parallel
    // calculations are requested ... if parallel is not requested build a
    // single U task and a single V task to hold the solution components

    if (!aUseParallel.isNone())
    {
      aThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(aNThreads);
      aQueue = new ExecutorCompletionService<PartialResult>(aThreadPool);
      buildParallelTasks();
    }
    else
    {
      uTasks = new SolveAProd [1];
      uTasks[0] = new SolveAProd(0, aRowSprs.size(), 0, aRowSprs.size(),
                                 aU, aULo, aV, aVLo, aRowSprs);
      vTasks = new SolveAProd [1];
      vTasks[0] = new SolveAProd(0, aColSprs.size(), 0, aColSprs.size(),
                                 aV, aVLo, aU, aULo, aColSprs);
    }

    // solve

    //ddStat.clear();

    int solvestatus = solve();
    /*
    HashMapIntegerKey<Statistic>.Iterator it = ddStat.iterator();
    while (it.hasNext())
    {
      HashMapIntegerKey.Entry<Statistic> e = it.nextEntry();
      int vlen = e.getKey();
      Statistic s = e.getValue();
      System.out.println(NL + aOutputIndent +
                         "DoubleDouble - double Normalization Error:" +
                         NL + aOutputIndent +
                         "  Vector Size    = " + vlen +
                         NL + aOutputIndent +
                         "  Count          = " + s.getCount() +
                         NL + aOutputIndent +
                         "  Abs. Maximum   = " + s.getAbsMaximum() +
                         NL + aOutputIndent +
                         "  Mean           = " + s.getMean() +
                         NL + aOutputIndent +
                         "  Std. Deviation = " + s.getStdDev() +
                         NL + aOutputIndent +
                         "  RMS            = " + s.getRMS());
    }
    */

    // shutdown

    if (!aUseParallel.isNone())
    {
      aThreadPool.shutdown();
      aThreadPool = null;
      aQueue = null;
    }

    // exit

    uTasks = vTasks = null;
    aU = aULo = aV = aVLo = null;
    return solvestatus;    
  }

  /**
   * The low-level function responsible for performing the LSQR solution.
   * 
   * @return The final stop condition.
   * 
   * @throws IOException
   */
  private int solve() throws IOException
  {
    int    i, nconv, nstop;
    double[] alfa = {0.0, 0.0};
    double[] beta = {0.0, 0.0};
    double bbnorm, cs, cs1, cs2, cTol, dampSq, ddnorm,
           delta, gamma, gamBar, phi, phiBar, psi, res1, res2, rho, rhoBar,
           rhBar1, rhBar2, rhs, rTol, sn, sn1, sn2, t, tau, test3,
           theta, t1, t2, t3, t3sq, xxnorm, z, zBar;

    // create temporary arrays

    double[] w = new double [lsqrCols];

    // output header if requested

    long start = (new Date()).getTime();
    if (aScrnWrtr.isOutputOn()) writeOutput("Initialization");

    // initialize.
    
    dampSq =   lsqrDamp * lsqrDamp;
    lsqrIter = lsqrIStop = nstop = 0;
    
    lsqrANorm  = lsqrACond = lsqrXNorm = lsqrBNorm = 0.0;
    bbnorm = ddnorm = xxnorm = res2 = sn2 = z = 0.0;
    cs2    = - 1.0;
    
    // set max iteration limit to 4 * lsqrCols if <= 0
  
    if (lsqrItnLim <= 0) lsqrItnLim = 4 * lsqrCols;

    // set cTol to 1.0 / lsqrConLim if it is defined
    
    cTol   =   0.0;
    if (lsqrConLim > 0.0) cTol = 1.0 / lsqrConLim;

    // initialize work arrays and output solution vector.
    
    for (i = 0; i < lsqrCols; ++i) aV[i] = aX[i] = aSE[i] = 0.0;

    // set up the first vectors u and v for the bidiagonalization.
    // these satisfy  beta * u = b,  alfa * v = A' * U.

    twoNormVector(beta, uTasks);
    
    if (beta[0] > 0.0)
    {
      inverseScaleVector(beta, uTasks);  
      updateVector(vTasks);
      twoNormVector(alfa, vTasks);
    }

    if (alfa[0] > 0.0)
    {
      inverseScaleVector(alfa, vTasks);
      for (i = 0; i < lsqrCols; ++i) w[i] = aV[i];
    }

    // finish initialization

    rhoBar = alfa[0];
    phiBar = beta[0];
    lsqrBNorm  = beta[0];
    lsqrRNorm  = beta[0];

    // test for x = 0 as an exact solution ... exit if true

    lsqrARNorm = alfa[0] * beta[0];
    if (lsqrARNorm == 0.0)
    {
      // output x = 0 solution if requested

      lsqrExTime = (new Date()).getTime() - start;
      if (aScrnWrtr.isOutputOn()) writeOutput("Exit");

      resetLargeReferences();
      return lsqrIStop;
    }

    // ready to enter iteration loop ... output data header

    if (aScrnWrtr.isOutputOn()) 
    {
      lsqrCmptbl = 1.0;
      lsqrLSAcc  = alfa[0] / beta[0];
      lsqrX0 = aX[0];
      writeOutput("Output Header");
    }

    // ------------------------------------------------------------------
    // Main iteration loop.
    // ------------------------------------------------------------------
    
    do
    {
      ++lsqrIter;

      // Perform the next step of the bidiagonalization to obtain the
      // next  beta, u, alfa, v.  These satisfy the relations
      //       beta * u  =  A  * v - alfa * u,
      //       alfa * v  =  A' * u - beta * v.

      // perform scale/norm/update

      negativeScaleVector(alfa, uTasks);
      updateVector(uTasks);
      twoNormVector(beta, uTasks);
      bbnorm += alfa[0] * alfa[0] + beta[0] * beta[0] + dampSq;

      if (beta[0] > 0.0)
      {
        inverseScaleVector(beta, uTasks);
        negativeScaleVector(beta, vTasks);
        updateVector(vTasks);
        twoNormVector(alfa, vTasks);
        if (alfa[0] > 0.0) inverseScaleVector(alfa, vTasks);
      }

      // Use a plane rotation to eliminate the damping parameter.
      // This alters the diagonal (rhoBar) of the lower-bidiagonal matrix.

      rhBar2  = rhoBar * rhoBar + dampSq;
      rhBar1  = sqrt(rhBar2);
      cs1     = rhoBar   / rhBar1;
      sn1     = lsqrDamp / rhBar1;
      psi     = sn1 * phiBar;
      phiBar *= cs1;

      // Use a plane rotation to eliminate the subdiagonal element (beta)
      // of the lower-bidiagonal matrix, giving an upper-bidiagonal matrix.

      rho     =  sqrt(rhBar2 + beta[0] * beta[0]);
      cs      =  rhBar1  / rho;
      sn      =  beta[0] / rho;
      theta   =  sn * alfa[0];
      rhoBar  = -cs * alfa[0];
      phi     =  cs * phiBar;
      phiBar *=  sn;
      tau     =  sn * phi;

      // Update  x, w  and the standard error estimates.

      t1   =  phi   / rho;
      t2   = -theta / rho;
      t3   =  1.0   / rho;
      t3sq =  t3    * t3;
      for (i = 0; i < lsqrCols; ++i)
      {
        t       = w[i];
        aX[i]  += t1 * t;
        w[i]    = t2 * t + aV[i];
        t      *= t3sq * t;
        aSE[i] += t;
        ddnorm += t;
      }

      // Use a plane rotation on the right to eliminate the
      // super-diagonal element (theta) of the upper-bidiagonal matrix.
      // Then use the result to estimate norm(x).

      delta     =  sn2 * rho;
      gamBar    = -cs2 * rho;
      rhs       =  phi - delta * z;
      zBar      =  rhs / gamBar;
      lsqrXNorm =  sqrt(xxnorm + zBar * zBar);
      gamma     =  sqrt(gamBar * gamBar + theta * theta);
      cs2       =  gamBar / gamma;
      sn2       =  theta  / gamma;
      z         =  rhs    / gamma;
      xxnorm   +=  z * z;

      // Test for convergence.
      // First, estimate the norm and condition of the matrix  Abar,
      // and the norms of  rbar  and  Abar(transpose)*rbar.

      lsqrANorm  = sqrt(bbnorm);
      lsqrACond  = lsqrANorm * sqrt(ddnorm);
      res1       = phiBar * phiBar;
      res2      += psi * psi;
      lsqrRNorm  = sqrt(res1 + res2);
      lsqrARNorm = alfa[0] * abs(tau);

      // Now use these norms to estimate certain other quantities,
      // some of which will be small near a solution.

      lsqrCmptbl = lsqrRNorm / lsqrBNorm;
      lsqrLSAcc  = 0.0;
      if (lsqrRNorm > 0.0) lsqrLSAcc = lsqrARNorm / (lsqrANorm * lsqrRNorm);
      test3  = 1.0 / lsqrACond;
      t1     = lsqrCmptbl / (1.0 + lsqrANorm * lsqrXNorm / lsqrBNorm);
      rTol   = lsqrBTol + lsqrATol * lsqrANorm * lsqrXNorm / lsqrBNorm;

      // The following tests guard against extremely small values of
      // lsqrATol, lsqrBTol  or  cTol.  (The user may have set any or all of
      // the parameters  lsqrATol, lsqrBTol, lsqrConLim  to 0.0.)
      // The effect is equivalent to the normal tests using
      // lsqrATol = RELPR,  lsqrBTol = RELPR,  lsqrConLim = 1/RELPR.

      t3 = 1.0 + test3;
      t2 = 1.0 + lsqrLSAcc;
      t1 = 1.0 + t1;
      if (lsqrIter >= lsqrItnLim) lsqrIStop = 7;
      if (t3 <= 1.0) lsqrIStop = 6;
      if (t2 <= 1.0) lsqrIStop = 5;
      if (t1 <= 1.0) lsqrIStop = 4;

      // Allow for tolerances set by the user.

      if (test3      <= cTol)     lsqrIStop = 3;
      if (lsqrLSAcc  <= lsqrATol) lsqrIStop = 2;
      if (lsqrCmptbl <= rTol)     lsqrIStop = 1;

      // See if it is time to print something.

      if (aScrnWrtr.isOutputOn() &&
          ((lsqrCols <= 40) || (lsqrIter <= 10) ||
           (lsqrIter >= lsqrItnLim - 10) ||
           (lsqrIter % 10 == 0) || (test3 <= 2.0 * cTol) ||
           (lsqrLSAcc <= 10.0 * lsqrATol) || (lsqrCmptbl <= 10.0 * rTol) ||
           (lsqrIStop != 0)))
      {
        lsqrX0 = aX[0];
        writeOutput("Output");
      }

      // Stop if appropriate.
      // The convergence criteria are required to be met on nconv
      // consecutive iterations, where nconv is set below.
      // Suggested value:  nconv = 1, 2  or  3.

      if (lsqrIStop == 0)
        nstop = 0;
      else
      {
        nconv = 1;
        ++nstop;
        if ((nstop < nconv) && (lsqrIter < lsqrItnLim)) lsqrIStop = 0;
      }
    } while (lsqrIStop == 0);

    // ------------------------------------------------------------------
    // End of main iteration loop
    // ------------------------------------------------------------------

    // Finish off the standard error estimates.

    t = 1.0;
    if (lsqrRows > lsqrCols) t = lsqrRows - lsqrCols;
    if (dampSq > 0.0) t = lsqrRows;
    t = lsqrRNorm / sqrt(t);

    for (i = 0; i < lsqrCols; ++i) aSE[i] = t * sqrt(aSE[i]);

    // Print the stopping condition.

    lsqrExTime = (new Date()).getTime() - start;
    if (aScrnWrtr.isOutputOn()) writeOutput("Exit");

    // exit
 
    return lsqrIStop;
  }

  /**
   * Performs an inverse scaling of the Y vector defined in the input set of
   * tasks. The Y vector is scaled by the inverse of the input scale factor
   * scl.
   *  
   * @param scl The input scale factor which eill be inverted and used to
   *            scale the Y vector in the input task list.
   * @param tasks The set of tasks to be solved in parallel, or if sequential
   *              mode is on, a single task containing the sequential solution
   *              inputs.
   */
  private void inverseScaleVector(double[] scl, SolveAProd[] tasks)
  {
    double[] inv = {0.0, 0.0};
    invertScale(inv, scl);
    scaleVector(inv[0], inv[1], tasks);
  }

  /**
   * Peforms a negative scaling of the Y vector defined in the input set of
   * tasks. The Y vector is scaled by the negative of the input scale factor
   * scl.
   * 
   * @param scl The input scale factor which will be negated and used to
   *            scale the Y vector in the input task list.
   * @param tasks The set of tasks to be solved in parallel, or if sequential
   *              mode is on, a single task containing the sequential solution
   *              inputs.
   */
  private void negativeScaleVector(double[] scl, SolveAProd[] tasks)
  {
    double[] neg = {-scl[0], -scl[1]};
    scaleVector(neg[0], neg[1], tasks);
  }

  /**
   * Calls the vector scale operation for all input tasks using concurrency
   * if aUseParallel is set for scale evaluation. Otheriwise, sequential
   * scaling is performed.
   * 
   * @param sclhi The double precision scale factor or the high component
   *              of the scale factor if DoubleDouble precision scaling
   *              is performed.
   * @param scllo The low component of the scale factor if DoubleDouble
   *              precision scaling is performed. This value is not
   *              used if double precision scaling is performed.
   * @param tasks The set of tasks to be solved in parallel, or if sequential
   *              mode is on, a single task containing the sequential solution
   *              inputs.
   */
  private void scaleVector(double sclhi, double scllo, SolveAProd[]tasks)
  {
    if (aUseParallel.isScale())
      concurrentScale(sclhi, scllo, tasks);
    else
      dscl(sclhi, scllo, tasks[0].aY, tasks[0].aYLo);
  }

  /**
   * Calls the vector two norm calculation for all input tasks using
   * concurrency if aUseParallel is set for normalization evaluation.
   * Otherwise, it uses a sequential calculation.
   * 
   * @param twoNorm The two norm result (twoNorm[1] is zero if double
   *                precision was requested).
   * @param tasks The set of tasks to be solved in parallel, or if sequential
   *              mode is on, a single task containing the sequential solution
   *              inputs.
   */
  private void twoNormVector(double[] twoNorm, SolveAProd[] tasks)
  {
    if (aUseParallel.isNorm())
      concurrent2Norm(twoNorm, tasks);
    else
      dnrm2(twoNorm, tasks[0].aY, tasks[0].aYLo);

    // debug line to output difference between DoubleDouble and double
    // 2 norm calculation

    /*
    if (aUseDoubleDouble.isNorm())
    {
      Statistic s = ddStat.get(tasks[0].aY.length);
      if (s == null)
      {
        s = new Statistic();
        ddStat.put(tasks[0].aY.length, s);
      }
      s.add(twoNorm[0] - dnrm2Old(tasks[0].aY));
    }
    */
  }

  /**
   * Calls the vector update method for all input tasks using concurrency if
   * aUseParallel is set for AProd evaluation. Otherwise, it uses a sequential
   * calculation.
   * 
   * @param tasks The set of tasks to be solved in parallel, or if sequential
   *              mode is on, a single task containing the sequential solution
   *              inputs.
   */
  private void updateVector(SolveAProd[] tasks)
  {
    if (aUseParallel.isAProd())
      concurrentUpdate(tasks);
    else
    {
      SolveAProd t = tasks[0];
      aprod(t.aSpM, t.aY, t.aYLo, t.aX, t.aXLo);    
    }
  }

  /**
   * Sequential Sparse matrix multiply. Multiplies Asprs[i] * x[ix[i]](hi,lo)
   * and sums the result to y[i](hi,lo). If Doubledouble is used both hi and
   * lo are evaluted (expensive), otherwise, only hi is evaluated.
   * 
   * @param aSprs The sparse matrix.
   * @param y The vector to be updated (high component if DoubleDouble
   *          precision is requested).
   * @param ylo The low component of the DoubleDouble precision vector to be
   *          updated (set to 0.0 for each entry if double precision is used).
   * @param x The vector multiplying aSprs to update y (high component if
   *          DoubleDouble precision is requested).
   * @param xlo The low component of the DoubleDouble precision vector used
   *            to multiply aSprs. (not used for double precision calculation).
   */
  private void aprod(ArrayList<SparseMatrixVector> aSprs,
                     double[] yhi, double[] ylo, double[] xhi, double[] xlo)
  {
    if (aUseDoubleDouble.isAProd())
    {
      // use DoubleDouble precision

      double[] t = {0.0, 0.0};
      for (int i = 0; i < aSprs.size(); ++i)
      {
        t[0] = yhi[i];
        t[1] = ylo[i];
        aSprs.get(i).update(t, xhi, xlo);
        yhi[i] = t[0];
        ylo[i] = t[1];
      }
    }
    else
    {
      // use double precision

      for (int i = 0; i < aSprs.size(); ++i)
      {
        yhi[i] = aSprs.get(i).update(yhi[i], xhi);
        ylo[i] = 0.0;
      }
    }
  }

  /**
   * Sequential vector 2 norm. Finds the 2 norm of the input vector x(hi, lo)
   * and returns the result in n(hi, lo). If Doubledouble is used both hi and
   * lo are evaluted (expensive but numerically a better choice if the vector
   * xhi is large), otherwise, only hi is evaluated.
   * 
   * @param n The output two norm result (hi, lo). If DoubleDouble is not used
   *          then n[0] has the sqrt() of the sum of the squares of xhi and
   *          n[1] is zero.
   * @param xhi The vector whose two norm will be found if non-DoubleDouble is
   *            calculated, or the high half of the two norm if DoubleDouble is
   *            used.
   * @param xlo The low half of the vector whose two norm will be calculated
   *            for a DoubleDouble calcualtion (not used for non-DoubleDouble).
   */
  private void dnrm2(double[] n, double[] xhi, double[] xlo)
  {
    if (aUseDoubleDouble.isNorm())
    {
      n[0] = n[1] = 0.0;
      for (int i = 0; i < xhi.length; ++i)
        DoubleDouble.addSqrFast(n, xhi[i], xlo[i]);
      DoubleDouble sq = new DoubleDouble(n[0], n[1]);
      sq.sqrt();
      n[0] = sq.hi;
      n[1] = sq.lo;
    }
    else
    {
      double d = 0.0;
      for (int i = 0; i < xhi.length; ++i) d += xhi[i] * xhi[i];
      n[0] = sqrt(d);
      n[1] = 0.0;
    }
  }

  /*
  private double dnrm2Old(double[] x)
  {
    double d = 0.0;
    for (int i = 0; i < x.length; ++i) d += x[i] * x[i];
    return sqrt(d);
  }
  */

  /**
   * Sequential vector scale. Scales the input vector x(hi,lo) by scl(hi,lo).
   * If Doubledouble is used both hi and lo are evaluted (expensive),
   * otherwise, only hi is evaluated.
   * 
   * @param sclhi The scaling factor for non-DoubleDouble or the high half for
   *              a DoubleDouble evaluation
   * @param scllo The low half of the scaling factor for a DoubleDouble
   *              calculation (not used for non-DoubleDouble).
   * @param xhi The vector to be scaled for non-DoubleDouble or the high half
   *            to be scaled for a DoubleDouble evaluation.
   * @param xlo The low half of the vector to be scaled for a DoubleDouble
   *            calculation (not used for non-DoubleDouble).
   */
  private void dscl(double sclhi, double scllo, double[] xhi, double[] xlo)
  {
    if (aUseDoubleDouble.isScale())
    {
      double[] y = {0.0, 0.0};
      for (int i = 0; i < xhi.length; ++i)
      {
        y[0] = xhi[i];
        y[1] = xlo[i];
        DoubleDouble.multFast(y, sclhi, scllo);
        xhi[i] = y[0];
        xlo[i] = y[1];
      }
    }
    else
    {
      for (int i = 0; i < xhi.length; ++i)
      {
        xhi[i] *= sclhi;
        xlo[i]  = 0.0;
      }
    }
  }

  /**
   * Primary output function. This function writes all LSQR solution messages
   * given the type string which can be "Initialization" (called once at the
   * beginning of the solution process), "Output Header" (called once before
   * entering the main iteration loop), "Output" (called many times during
   * the solution process, and "Exit" (called once before exiting the solve
   * function).
   *
   * @param type Type of message to be written. Can be "Initialization",
   *             "Output Header", "Output", or "Exit".
   *
   * @throws IOException
   */
  private void writeOutput(String type) throws IOException
  {
    String s = "";
    if (type.equals("Output"))
    {
      s = aOutputIndent +
          String.format("%6d  %16.9e  %16.9e  %8.2e  %8.2e  %8.2e  %8.2e",
                        lsqrIter, lsqrX0, lsqrRNorm, lsqrCmptbl, lsqrLSAcc,
                        lsqrANorm, lsqrACond) + NL;
      if (lsqrIter % 10 == 0) s += NL;
      
      if (aTruncateOutTable && (lsqrIter > 10))
      {
        aEndOfOutTable.add(s);
        if (aEndOfOutTable.size() > 15) aEndOfOutTable.removeFirst();

        s  = "";
        if (aEndOfOutTable.size() == 1) s += NL + aOutputIndent;
        if (lsqrIter % 10 == 0) s += ".";
        if (lsqrIter % 100 == 0) s += NL + aOutputIndent;
      }
    }
    else if (type.equals("Initialization"))
    {
      s = NL + aOutputIndent +
          "Begin LSQR Least-Squares solution of A*x = b" +
          NL + aOutputIndent +
          "Start Time: " + Globals.getTimeStamp() +
          NL + aOutputIndent +
          "The matrix A has " + lsqrRows + " rows and " + lsqrCols +
          " columns" + NL + aOutputIndent +
          "The damping parameter is          DAMP   = " +
          String.format("%10.4e", lsqrDamp) +
          NL + aOutputIndent +
          "      ATOL   = " + String.format("%10.4e", lsqrATol) +
          "         CONLIM = " + String.format("%10.4e", lsqrConLim) +
          NL + aOutputIndent +
          "      BTOL   = " + String.format("%10.4e", lsqrBTol) +
          "         ITNLIM = " + String.format("%10d", lsqrItnLim) + NL;
    }
    else if (type.equals("Output Header"))
    {
      s  = NL + aOutputIndent +
           "Itrtn        x(1)           Function     Compatible  Lst-Sq" +
           "    Norm A    Cond A" + NL + aOutputIndent;
      if (lsqrDamp == 0.0)
        s += "Count                        (RNorm)       System   Accuracy" + NL;
      else
        s += "Count                        (RNorm)       System   Accuracy" +
             "  (damped)  (damped)" + NL;
      s += aOutputIndent +
           String.format("%6d  %16.9e  %16.9e  %8.2e  %8.2e  %8.2e  %8.2e",
                         lsqrIter, lsqrX0, lsqrRNorm, lsqrCmptbl, lsqrLSAcc,
                         0.0, 0.0) + NL;
      if (aEndOfOutTable != null) aEndOfOutTable.clear();
    }
    else if (type.equals("Exit"))
    {
      if (aTruncateOutTable)
      {
        aScrnWrtr.write(NL + NL);
        for (String ss: aEndOfOutTable) aScrnWrtr.write(ss);
      }

      s = NL + aOutputIndent + "Exiting LSQR ..." + NL + aOutputIndent +
          "Process Execution Time                   = " +
          processExecutionTime() +
          NL + aOutputIndent +
          "      ISTOP  = " + String.format("%3d", lsqrIStop) +
          "                ITN    = " + lsqrIter +
          NL + aOutputIndent +
          "      ANORM  = " + String.format("%13.7e", lsqrANorm) +
          "      ACOND  = " + String.format("%13.7e", lsqrACond) +
          NL + aOutputIndent +
          "      RNORM  = " + String.format("%13.7e", lsqrRNorm) +
          "      ARNORM = " + String.format("%13.7e", lsqrARNorm) +
          NL + aOutputIndent +
          "      BNORM  = " + String.format("%13.7e", lsqrBNorm) +
          "      XNORM  = " + String.format("%13.7e", lsqrXNorm) + NL + NL;

      for (int i = 0; i < lsqrExitMsg[lsqrIStop].length; ++i)
        s += aOutputIndent + lsqrExitMsg[lsqrIStop][i];
    }

    // write string

    aScrnWrtr.write(s); 
  }

  /**
   * Sets the output indentation string.
   * 
   * @param indent The output indentation string.
   */
  public void setOutputIndent(String indent)
  {
    aOutputIndent = indent;
  }

  /**
   * Reads a solution back into the LSQRDD object for further analysis. All
   * files are read from the path fPath and include files:
   * 
   *   sparseMatrixExcecutionControl - LSQR state information
   *   sparseMatrixRHS               - Right-hand-side vector
   *   sparseMatrix                  - Sparse matrix
   *   sparseMatrixTranspose         - Transpose of the sparse matrix
   *   sparseMatrixSolution          - The solution vector
   *   sparseMatrixSolutionError     - The associated solution error vector
   * 
   * @param fPath The path containing the LSQRDD definition to be loaded
   * 
   * @throws IOException
   */
  public void readSolution(String fPath) throws IOException
  {
    // create the base file name string

    String f = fPath + File.separator + "sparseMatrix";

    // read each component file into this LSQRDD object

    readSolutionExecutionControl(f + "ExecutionControl");
    readSolutionRHS(f + "RHS");
    aRowSprs = readSolutionSparseMatrix(f);
    aColSprs = readSolutionSparseMatrix(f + "Transpose");
    aX  = readSolutionVector(f + "Solution");
    aSE = readSolutionVector(f + "SolutionError");
  }

  /**
   * Writes this LSQRDD objects current definition to a set of files in the
   * directory pointed to by the input path fPath. The files written to
   * fPath include:
   * 
   *   sparseMatrixExcecutionControl - LSQR state information
   *   sparseMatrixRHS               - Right-hand-side vector
   *   sparseMatrix                  - Sparse matrix
   *   sparseMatrixTranspose         - Transpose of the sparse matrix
   *   sparseMatrixSolution          - The solution vector
   *   sparseMatrixSolutionError     - The associated solution error vector
   * 
   * @param fPath The path that will contain the files describing the
   *              current state of this LSQRDD object.
   * 
   * @throws IOException
   */
  public void writeSolution(String fPath) throws IOException
  {
    // create the base filename to be written to fPath

    String f = fPath + File.separator + "sparseMatrix";

    // write each component file to fPath

    writeSolutionExecutionControl(f + "ExecutionControl");
    writeSolutionRHS(f + "RHS");
    writeSolutionSparseMatrix(f, aRowSprs);
    writeSolutionSparseMatrix(f + "Transpose", aColSprs);
    writeSolutionVector(f + "Solution", aX);
    writeSolutionVector(f + "SolutionError", aSE);
  }

  /**
   * Reads LSQRDD execution and control information from the input file fn
   * and assigns it to this LSQRDD object.
   * 
   * @param fn The file from which LSQRDD execution and control information
   *           will be read and assigned to this LSQRDD object.
   * 
   * @throws IOException
   */
  private void readSolutionExecutionControl(String fn) throws IOException
  {
    // create input stream
    
    FileInputBuffer fib = new FileInputBuffer(fn);

    // read in format version for future input control

    aFrmtVrsn  = fib.readInt();

    // read in all execution control and result information and assign
    // to LSQRDD parameters

    lsqrANorm  = fib.readDouble();
    lsqrACond  = fib.readDouble();
    lsqrRNorm  = fib.readDouble();
    lsqrARNorm = fib.readDouble();
    lsqrBNorm  = fib.readDouble();
    lsqrXNorm  = fib.readDouble();
    lsqrCmptbl = fib.readDouble();
    lsqrLSAcc  = fib.readDouble();
    lsqrDamp   = fib.readDouble();
    lsqrATol   = fib.readDouble();
    lsqrBTol   = fib.readDouble();
    lsqrConLim = fib.readDouble();
    lsqrX0     = fib.readDouble();
    setThreadCountMultiplier(fib.readDouble());
    aUseParallel     = SolveType.values()[fib.readInt()];
    aUseDoubleDouble = SolveType.values()[fib.readInt()];
    lsqrIStop  = fib.readInt();
    lsqrIter   = fib.readInt();
    lsqrItnLim = fib.readInt();
    lsqrRows   = fib.readInt();
    lsqrCols   = fib.readInt();
    lsqrExTime = fib.readLong();
    aOutputIndent = fib.readString();

    // done ... close the file and exit

    fib.close();
  }

  /**
   * Writes this LSQRDD objects execution and control information to the
   * input file fn.
   * 
   * @param fn The file into which this LSQRDD objects execution and control
   *           information will be written.
   * 
   * @throws IOException
   */
  private void writeSolutionExecutionControl(String fn) throws IOException
  {
    // create output stream
    
    FileOutputBuffer fob = new FileOutputBuffer(fn);

    // write out format version for input control

    fob.writeInt(aFrmtVrsn);

    // write out all execution control and result information

    fob.writeDouble(lsqrANorm);
    fob.writeDouble(lsqrACond);
    fob.writeDouble(lsqrRNorm);
    fob.writeDouble(lsqrARNorm);
    fob.writeDouble(lsqrBNorm);
    fob.writeDouble(lsqrXNorm);
    fob.writeDouble(lsqrCmptbl);
    fob.writeDouble(lsqrLSAcc);
    fob.writeDouble(lsqrDamp);
    fob.writeDouble(lsqrATol);
    fob.writeDouble(lsqrBTol);
    fob.writeDouble(lsqrConLim);
    fob.writeDouble(lsqrX0);
    fob.writeDouble(aProcMult);
    fob.writeInt(aUseParallel.ordinal());
    fob.writeInt(aUseDoubleDouble.ordinal());
    fob.writeInt(lsqrIStop);
    fob.writeInt(lsqrIter);
    fob.writeInt(lsqrItnLim);
    fob.writeInt(lsqrRows);
    fob.writeInt(lsqrCols);
    fob.writeLong(lsqrExTime);
    fob.writeString(aOutputIndent);
    
    // done ... close the file and exit

    fob.close();    
  }

  /**
   * Reads in the right-hand-side vector from the input file fn and assign
   * to aRHS.
   *  
   * @param fn The file from which the right-hand-side vector is read.
   * 
   * @throws IOException
   */
  private void readSolutionRHS(String fn) throws IOException
  {
    // create input stream
    
    FileInputBuffer fib = new FileInputBuffer(fn);

    // read size of RHS vector and create a new array list to hold the
    // entries ... read the entries into aRHS

    int n = fib.readInt();
    aRHS = new ArrayListDouble(n);
    for (int i = 0; i < n; ++i) aRHS.add(fib.readDouble());

    // done ... close the file and exit

    fib.close();
  }

  /**
   * Writes the the right-hand-side vector (aRHS) to the input file fn.
   *  
   * @param fn The file into which the right-hand-side vector will be written.
   * 
   * @throws IOException
   */
  private void writeSolutionRHS(String fn) throws IOException
  {
    // create output stream
    
    FileOutputBuffer fob = new FileOutputBuffer(fn);

    // write out size and each entry into file

    fob.writeInt(aRHS.size());
    for (int i = 0; i < aRHS.size(); ++i)
      fob.writeDouble(aRHS.get(i));

    // done ... close file and exit

    fob.close();
  }

  /**
   * Read and return a sparse matrix to the LSQRDD caller (usually
   * function readSolution(...)). The sparse matrix is read from the input
   * file fn.
   * 
   * @param fn The input file from which the sparse matrix will be read.
   * @return The sparse matrix read from the input file fn.
   * 
   * @throws IOException
   */
  private ArrayList<SparseMatrixVector> readSolutionSparseMatrix(String fn)
          throws IOException
  {
    ArrayList<SparseMatrixVector> sm;

    // create input stream
    
    FileInputBuffer fib = new FileInputBuffer(fn);
    
    // read in sparse matrix size and and create a new sparse matrix
    // array list container ... read in all SparseMatrixVectors into the
    // new array list
    
    int n = fib.readInt();
    sm = new ArrayList<SparseMatrixVector>(n);
    for (int i = 0; i < n; ++i)
    {
      SparseMatrixVector smv = new SparseMatrixVector();
      smv.readVector(fib);
      sm.add(smv);
    }

    // done ... close the input file and return the sparse matrix

    fib.close();
    return sm;
  }

  /**
   * Writes the input sparse matrix into the file fn.
   * 
   * @param fn The file that will contain the input sparse matrix sm.
   * @param sm The sparse matrix that is written into file fn.
   * 
   * @throws IOException
   */
  private void writeSolutionSparseMatrix(String fn,
                                         ArrayList<SparseMatrixVector> sm)
          throws IOException
  {
    // create output stream
    
    FileOutputBuffer fob = new FileOutputBuffer(fn);

    // write out the size of the sparse matrix and each SparseMatrixVector
    // contained within

    fob.writeInt(sm.size());
    for (int i = 0; i < sm.size(); ++i)
      sm.get(i).writeVector(fob);

    // done ... close the file and exit

    fob.close();
  }

  /**
   * Reads and returns a double array read from the input file fn. If no data
   * was written null is returned.
   * 
   * @param fn The input file from which the double array will be read.
   * @return The double array read from file fn.
   * 
   * @throws IOException
   */
  private double[] readSolutionVector(String fn) throws IOException
  {
    double[] x = null;

    // create input stream
    
    FileInputBuffer fib = new FileInputBuffer(fn);

    // read the size of the vector ... read the vector data if size > 0

    int n = fib.readInt();
    if (n > 0) x = fib.readDoubles();

    // close the file and return the vector (null if no data was read)

    fib.close();
    return x;
  }

  /**
   * Writes a double array to the output file buffer fn. If the array is null
   * a size of zero is output.
   * 
   * @param fn The file into which the array x is written.
   * @param x The array to be written into file x.
   * 
   * @throws IOException
   */
  private void writeSolutionVector(String fn, double[] x) throws IOException
  {
    // create the output file buffer

    FileOutputBuffer fob = new FileOutputBuffer(fn);

    // if x is null write a size of 0 ... otherwise write the size of x and the
    // vector x to fob

    if (x == null)
      fob.writeInt(0);
    else
    {
      fob.writeInt(x.length);
      fob.writeDoubles(x);
    }

    // done ... close the file and exit

    fob.close();
  }

  /**
   * Returns the sparse matrix used to solve this LSQRDD definition.
   * 
   * @return The sparse matrix used to solve this LSQRDD definition.
   */
  public ArrayList<SparseMatrixVector> getSparseMatrix()
  {
    return aRowSprs;
  }

  /**
   * Returns the RHS vector used to solve this LSQRDD definition.
   * 
   * @return The RHS vector used to solve this LSQRDD definition.
   */
  public ArrayListDouble getRHS()
  {
    return aRHS;
  }

  /**
   * Returns the solution vector solved for by this LSQRDD definition.
   * 
   * @return The solution vector solved for by this LSQRDD definition.
   */
  public double[] getSolution()
  {
    return aX;
  }

  /**
   * Returns the solution error vector solved for by this LSQRDD definition.
   * 
   * @return The solution error vector solved for by this LSQRDD definition.
   */
  public double[] getSolutionError()
  {
    return aSE;
  }

  /**
   * Turns on output table truncation so that only the first 10 and last 15
   * convergence table output lines are written.
   */
  public void truncateOutputTableOn()
  {
    aTruncateOutTable = true;
    aEndOfOutTable    = new LinkedList<String>();
  }

  /**
   * Turns off output table truncation.
   */
  public void truncateOutputTableOff()
  {
    aTruncateOutTable = false;
    aEndOfOutTable    = null;
  }

  //***************************************************************************
  //************** Concurrent definitions *************************************
  //***************************************************************************

  /**
   * A dummy CompletionService<SolveAProd> result class returned to the
   * caller after each task is finished. The result contains the row/column
   * start/stop values for each task.
   * 
   * @author jrhipp
   *
   */
  public class PartialResult
  {
    /**
     * Row/column aProd start index.
     */
    public int startIndexProd = 0;

    /**
     * Row/column aProd end index.
     */
    public int stopIndexProd  = 0;

    /**
     * Row/column start index.
     */
    public int startIndex     = 0;

    /**
     * Row/column end index.
     */
    public int stopIndex      = 0;

    /**
     * Used to return 2 norm results. 
     */
    public double rsltHi      = 0.0;

    /**
     * Used to return 2 norm Low precision result in DoubleDouble calculations. 
     */
    public double rsltLo      = 0.0;

    /**
     * Standard constructor set the row/column start and stop indices.
     * 
     * @param strt The row/column start index.
     * @param stop The row/column stop index.
     */
    public PartialResult(int strtProd, int stopProd, int strt, int stop)
    {
      startIndexProd = strtProd;
      stopIndexProd  = stopProd;
      startIndex     = strt;
      stopIndex      = stop;
    }
  }

  /**
   * Parallel task that solves aProd in the forward or backward direction for
   * the row based solution "u" or the column based solution "v". The solution
   * looks like
   * 
   *     for (i = row_start; i < row_stop; ++i)
   *     {
   *       int strt = aRowPtr[i];
   *       int stop = aRowPtr[i+1];
   *       double r = aU[i];
   *       for (int j = strt; j < stop; ++j) r += aSprsRow[j] * aV[aColIndx[j]];
   *       aU[i] = r;
   *     }
   *
   * When updating aU, and
   * 
   *     for (i = col_start; i < col_stop; ++i)
   *     {
   *       int strt = aColPtr[i];
   *       int stop = aColPtr[i+1];
   *       double r = aV[i];
   *       for (int j = strt; j < stop; ++j) r += aSprsCol[j] * aU[aRowIndx[j]];
   *       aV[i] = r;
   *     }
   * 
   * When updating aV. Note that each task only solves a subset of the rows
   * (row_start to row_stop when solving for aU) or columns (col_start to
   *  col_stop when solving for aV) in a single tasks. The sum of all tasks
   * solves for all rows or columns. Each task is processed as a thread to
   * gain parallel performance. Unfortunately, the task are heavily weighted
   * toward memory access per flop (3) which makes them band-width limited on
   * multicore computers.
   * 
   * <p> The array of tasks for "u" and "v" are contained in uTasks and vTasks
   * and are constructed in the function buildParallelTasks().
   * 
   * @author jrhipp
   *
   */
  public class SolveAProd implements Callable<PartialResult>
  {
    /**
     * Contains the start and stop indices in aPtr defined for this task.
     * These are row start and stop indices if this is a uTask or column
     * start and stop indices if this is a vTask. 
     */
    PartialResult aPR = null;

    /**
     * The vector being solved for (aU for uTasks and aV for vTasks).
     */
    double[] aY     = null; //  aU            aV
    double[] aYLo   = null; //  aULo          aVLo

    /**
     * The vector multiplying the sparse matrix to update aY (aV for uTasks
     * and aU for vTasks).
     */
    double[] aX     = null; //  aV            aU
    double[] aXLo   = null; //  aULo          aVLo

    /**
     * The sparse matrix that multiplies aX to update aY (aRowSprs for uTasks
     * and aColSprs for vTasks).
     */
    ArrayList<SparseMatrixVector> aSpM = null; //  aRowSprs      aColSprs

    /**
     * Used by the scaling operation to scale the aY vector.
     */
    double   aScale   = 0.0;
    double   aScaleLo = 0.0;
    /**
     * If true the function scale is called.
     */
    boolean aScaleFlg = false;

    /**
     * If true the function twoNorm is called.
     */
    boolean a2NormFlg = false;

    /**
     * Standard constructor for the parallel aProd task. Sets the input
     * arguments in preparation for solving for aU or aV. 
     * 
     * @param strt First row (in aRowPtr if this is a uTask) or column
     *             (in aColPtr if this is a vTask) to process by this task.
     * @param stop Last - 1 row (in aRowPtr if this is a uTask) or column
     *             (in aColPtr if this is a vTask) to process by this task.
     * @param y The vector to be updated (aU for uTasks or aV for vTasks).
     * @param ylo The low DoubleDouble vector to be updated (aULo for uTasks
     *            or aVLo for vTasks) if DoubleDouble precision is used
     *            (not used for double precision calculations).
     * @param x The vector multiplying the sparse matrix to update y
     *          (aV for uTasks or aU for vTasks).
     * @param xlo The low DoubleDouble vector to be used to update y(hi,lo)
     *            if DoubleDouble precision is used (not used for double
     *            precision calculations).
     * @param sprs The row oriented or column oriented sparse matrix (aRowSprs
     *             for uTasks or aColSprs for vTasks).
     */
    public SolveAProd(int strtProd, int stopProd, int strt, int stop,
                      double[] y, double[] ylo, double[] x, double[] xlo,
                      ArrayList<SparseMatrixVector> sprs)
    {
      // set input values into class variables

      aY     = y;
      aYLo   = ylo;
      aX     = x;
      aXLo   = xlo;
      aSpM   = sprs;

      aPR = new PartialResult(strtProd, stopProd, strt, stop);
    }

    /**
     * Puts the task into scale mode. When the concurrent submission calls
     * the function call() it will calle scale() to scale the aY vector by
     * the input scale value (scl).
     * 
     * @param scl The factor by aY(hi,lo) will be scaled in double precision,
     *            or the high component in DoubleDouble precision.
     * @param sclLo The low component by which aY(hi,lo) will be scaled in
     *              DoubleDouble precision (not used in double precision).
     */
    public void setScale(double scl, double sclLo)
    {
      aScale    = scl;
      aScaleLo  = sclLo;
      aScaleFlg = true;
    }

    /**
     * Puts the task into 2 norm mode. When the concurrent submission calls
     * the function call() it will calculate the sum of squares of aY and
     * store the result into aPR.rslt for later retrieval.
     */
    public void set2Norm()
    {
      a2NormFlg = true;
    }

    /**
     * Concurrent entry. Call scale() if aScaleFlg is set. Otherwise call
     * update.
     */
    public PartialResult call()
    {
      if (aScaleFlg)
        scale();
      else if (a2NormFlg)
        twoNorm();
      else
        update();

      // return partial result

      return aPR;
    }

    /**
     * The parallel call that will perform the aProd multiply of the sparse
     * matrix aSpM times the aX to update the aY vector. This function is
     * called by each thread processing one of the tasks in uTasks or vTasks.
     * If DoubleDouble precision is used then aY(hi,lo) is calculated from
     * aSpM and aX(hi,lo). Otherwise, aY is evaluated in double precision
     * from aSpM and aX and aYLo is set to zero.
     */
    private void update()
    {
      // loop over all rows or columns from startIndex to stopIndex-1

      if (aUseDoubleDouble.isAProd())
      {
        // use DoubleDouble precision

        double[] y = {0.0, 0.0};
        for (int i = aPR.startIndexProd; i < aPR.stopIndexProd; ++i)
        {
          y[0] = aY[i];
          y[1] = aYLo[i];
          aSpM.get(i).update(y, aX, aXLo);
          aY[i] = y[0];
          aYLo[i] = y[1];
        }
      }
      else
      {
        // use double precision

        for (int i = aPR.startIndexProd; i < aPR.stopIndexProd; ++i)
        {
          aY[i]   = aSpM.get(i).update(aY[i], aX);
          aYLo[i] = 0.0;
        }
      }
    }

    /**
     * The parallel call that will perform the dscl multiply of the aY(hi,lo)
     * vector times the input scale factor aScale(hi,lo). This function is
     * called by each thread processing one of the tasks in uTasks or vTasks.
     * If DoubleDouble precision is used then aY(hi,lo) is scaled. Otherwise,
     * aY is scaled in double precision and aYLo is set to zero.
     */
    private void scale()
    {
      // scale each aY(hi,lo) entry
      
      if (aUseDoubleDouble.isScale())
      {
        // use DoubleDouble precision

        double[] y = {0.0, 0.0};
        for (int i = aPR.startIndex; i < aPR.stopIndex; ++i)
        {
          y[0] = aY[i];
          y[1] = aYLo[i];
          DoubleDouble.multFast(y, aScale, aScaleLo);
          aY[i] = y[0];
          aYLo[i] = y[1];
        }
      }
      else
      {
        // use double precision

        for (int i = aPR.startIndex; i < aPR.stopIndex; ++i)
        {
          aY[i] *= aScale;
          aYLo[i] = 0.0;
        }
      }

      // reset the scale flag and exit

      aScaleFlg = false;
    }

    /**
     * The parallel call that will perform the dnrm2 calculation of the aY
     * vector. This function is called by each thread processing one of the
     * tasks in uTasks or vTasks. If DoubleDouble precision is used then
     * rsltHi contains the high part and rsltLo contains the low part of the
     * result. Otherwise, rsltHi contains the double precision result and
     * rsltLo is set to 0.0.
     */
    private void twoNorm()
    {
      // sum the square of each aY(hi,lo) entry ... place result in
      // aPR.rslt(hi, lo) for return

      if (aUseDoubleDouble.isNorm())
      {
        // use DoubleDouble precision

        double[] y = {0.0, 0.0};
        normDoubleDouble(y, aY, aYLo);
        aPR.rsltHi = y[0];
        aPR.rsltLo = y[1];
      }
      else
      {
        // use double precision

        double d = 0.0;
        for (int i = aPR.startIndex; i < aPR.stopIndex; ++i) d += aY[i] * aY[i];
        aPR.rsltHi = d;
        aPR.rsltLo = 0.0;
      }

      // reset the 2 norm flag and exit

      a2NormFlg = false;
    }

    /**
     * Used to calculate the two norm in DoubleDouble precision. The
     * DoubleDouble function addSqrFast is called for each entry in aY, aYLo
     * defined for this task (startIndex to stopIndex) and squared and added
     * to the DoubleDouble solution vector y.
     * 
     * @param y The solution vector containing the DoubleDouble hi (y[0]) and
     *          low (y[1]) components.
     * @param vhi The DoubleDouble high part of the vector whose two norm will
     *            be calculated.
     * @param vlo The DoubleDouble low part of the vector whose two norm will
     *            be calculated.
     */
    private void normDoubleDouble(double[] y, double[] vhi, double[] vlo)
    {
      // perform y += v*v;
      for (int i = aPR.startIndex; i < aPR.stopIndex; ++i)
        DoubleDouble.addSqrFast(y, vhi[i], vlo[i]);
    }
  }

  /**
   * Used during concurrent solutions to define the number of threads in the
   * thread pool and the number of tasks assigned to uTasks and vTasks.
   */
  private static int aNProcessors = Runtime.getRuntime().availableProcessors();

  /**
   * Processor multiplier used to set the number of threads. This value can be
   * changed by the user.
   */
  private static double aProcMult = 2.0;

  /**
   * The number of threads to be run by the parallel solver. This value is
   * equal to the number of available processors times the processor
   * multiplier.
   */
  private static int aNThreads = (int) (aNProcessors * aProcMult);

  /**
   * The local thread pool on which the processes will execute the
   * concurrent uTasks and vTasks (SolveAProd tasks).
   */
  private ThreadPoolExecutor aThreadPool = null;

  /**
   * The completion service results queue used to save the concurrent results
   * as they return from the thread pool.
   */
  private CompletionService<PartialResult> aQueue = null;

  /**
   * The set of uTasks which solve for aU concurrently given aSprsRow,
   * aColIndx, and aV in a concurrent fashion (the concurrent version of
   * aProd).
   */
  private SolveAProd[] uTasks = null;

  /**
   * The set of vTasks which solve for aV concurrently given aSprsCol,
   * aRowIndx, and aU in a concurrent fashion (the concurrent version of
   * aProd).
   */
  private SolveAProd[] vTasks = null;

  /**
   * The parallel equivalent of aProd which is used to process the sparse matrix
   * to obtain aU or aV in a parallel fashion. If DoubleDouble precision is
   * requested then the matrix is processed in DoubleDouble precision. Otherwise,
   * the solution vector is simply calculated using double precision.
   * 
   * @param tasks The tasks to be processed (uTasks or vTasks).
   */
  private void concurrentUpdate(SolveAProd[] tasks)
  {
    // submit all tasks in concurrent mode

    for (int i = 0; i < tasks.length; ++i) aQueue.submit(tasks[i]);

    // wait until all tasks have returned before exiting.

    int cnt = 0; 
    while (cnt < tasks.length)
    {
      Future<PartialResult> pr = aQueue.poll();
      if (pr != null) cnt++;
    }
  }

  /**
   * The parallel equivalent of dscl which is used to scale the aU or aV
   * vector by the input scl. If DoubleDouble precision is
   * requested then the vector is scaled by the DoubleDouble (scl, sclLo)
   * Otherwise, the vector is scaled in double precision by scl only.
   * 
   * @param tasks The tasks to be processed (uTasks scales the aU vector while
   *              vTasks scales the aV vector).
   */
  private void concurrentScale(double scl, double sclLo, SolveAProd[] tasks)
  {
    // submit all tasks in concurrent mode

    for (int i = 0; i < tasks.length; ++i)
    {
      tasks[i].setScale(scl, sclLo);
      aQueue.submit(tasks[i]);
    }

    // wait until all tasks have returned before exiting.

    int cnt = 0; 
    while (cnt < tasks.length)
    {
      Future<PartialResult> pr = aQueue.poll();
      if (pr != null) cnt++;
    }
  }

  /**
   * The parallel equivalent of dnrm2 which is used to find the two norm of
   * the aU or aV. The result is returned in n. If DoubleDouble precision is
   * requested then n contains the DoubleDouble result. Otherwise, n[0]
   * contains the double result and n[1] = 0.
   * 
   * @param n The returned two norm of the aY vector in tasks.
   * @param tasks The tasks to be processed (uTasks finds the two norm of aU
   *              and vTasks finds the two norm of aV).
   */
  private void concurrent2Norm(double[] n, SolveAProd[] tasks)
  {
    double[] t = {0.0, 0.0};

    // submit all tasks in concurrent mode

    for (int i = 0; i < tasks.length; ++i)
    {
      tasks[i].set2Norm();
      aQueue.submit(tasks[i]);
    }

    // sum result from each task to d

    int cnt = 0; 
    if (aUseDoubleDouble.isNorm())
    {
      // use DoubleDouble precision

      try
      {
        while (cnt < tasks.length)
        {
          Future<PartialResult> pr = aQueue.poll();
          if (pr != null)
          {
            cnt++;
            PartialResult rslt = pr.get();
            DoubleDouble.addFast(t, rslt.rsltHi, rslt.rsltLo);
          }
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    else
    {
      // use double precision

      try
      {
        while (cnt < tasks.length)
        {
          Future<PartialResult> pr = aQueue.poll();
          if (pr != null)
          {
            cnt++;
            t[0] += pr.get().rsltHi;
          }
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }

    // return sqrt(d)

    if (aUseDoubleDouble.isNorm())
    {
      // use DoubleDouble precision

      DoubleDouble sq = new DoubleDouble(t[0], t[1]);
      sq.sqrt();
      n[0] = sq.hi;
      n[1] = sq.lo;
    }
    else
    {
      // use double precision

      n[0] = sqrt(t[0]);
      n[1] = 0.0;
    }
  }

  /**
   * Invert the input scale(hi, lo) and store in invScl(hi, lo). If 
   * DoubleDouble precision is requested perform the DoubleDouble
   * inverse. Otherwise set invScl[0] to 1/scale[0] and invScl[1] to 0.
   * 
   * @param invScl The output inverse of scale.
   * @param scale The input value to be inverted.
   */
  private void invertScale(double[] invScl, double[] scale)
  {
    if (aUseDoubleDouble.isScale())
    {
      // use DoubleDouble precision

      DoubleDouble inv = new DoubleDouble(scale[0], scale[1]);
      inv.inverse();
      invScl[0] = inv.hi;
      invScl[1] = inv.lo;
    }
    else
    {
      // use double precision

      invScl[0] = 1.0 / scale[0];
      invScl[1] = 0.0;
    }
  }

  /**
   * Builds the parallel U and V tasks (uTasks, and vTasks) which will be
   * submitted to multiple threads for parallel solution.
   */
  private void buildParallelTasks() throws IOException
  {
    // use number of processors as target number of tasks to create ...
    // nEntsPerTask is the rough number of entries in the sparse matrix that
    // will be assigned to an individual task. It is usually slightly larger
    // or smaller than this number as row and column boundaries are where the
    // tasks actually stop which don't usually coincide with nEntsPerTask
    // exactly

    long aNSprs   = 0;
    for (int i = 0; i < lsqrRows; ++i) aNSprs += aRowSprs.get(i).size();

    int trgtTaskCnt = aNThreads;
    int nEntsPerTaskProd = (int) (aNSprs / trgtTaskCnt);
    if (nEntsPerTaskProd < 0)
    	throw new IOException(NL + "Error: (aNSprs / trgtTaskCnt) exceeds Integer.MAX_VALUE");

    if (nEntsPerTaskProd == 0) nEntsPerTaskProd = 1;
    if (nEntsPerTaskProd * trgtTaskCnt < aNSprs) ++nEntsPerTaskProd;

    int nEntsPerTaskRow = lsqrRows / trgtTaskCnt;
    if (nEntsPerTaskRow == 0) nEntsPerTaskRow = 1;
    if (nEntsPerTaskRow * trgtTaskCnt < lsqrRows) ++nEntsPerTaskRow;

    int nEntsPerTaskCol = lsqrCols / trgtTaskCnt;
    if (nEntsPerTaskCol == 0) nEntsPerTaskCol = 1;
    if (nEntsPerTaskCol * trgtTaskCnt < lsqrCols) ++nEntsPerTaskCol;

    // create a temporary start / stop count vector to hold the task stop and
    // start entries. These are row start and stop points for each uTask and
    // column start and stop points for each vTask ... set the first entry to
    // zero and set the entry cutoff (entStep) to nEntsPerTask ... loop over
    // all row pointer entries.

    ArrayListInt cntProd = new ArrayListInt(trgtTaskCnt);
    cntProd.add(0);
    long entStep = nEntsPerTaskProd;
    long count = 0;
    for (int i = 0; i < lsqrRows; ++i)
    {
      // if count is equal or exceeds the current entry cutoff then
      // save index i as a task cutoff point and increment the cutoff
      // count until it exceeds the current entry index in count.

      count += aRowSprs.get(i).size();
      if (count >= entStep)
      {
        cntProd.add(i);
        while (entStep <= count) entStep += nEntsPerTaskProd;
      }
    }

    // done ... if the cutoff vector does not have aNRowPtr-1 as its last entry
    // then add it

    if (cntProd.getLast() < lsqrRows) cntProd.add(lsqrRows);

    // create the uTasks array and fill it with tasks

    uTasks = new SolveAProd [cntProd.size() - 1];
    int cnt0 = 0;
    int cnt1 = 0;
    for (int i = 0; i < cntProd.size() - 1; ++i)
    {
      cnt0 = cnt1;
      cnt1 += nEntsPerTaskRow;
      if (cnt1 > lsqrRows) cnt1 = lsqrRows;
      if (i == cntProd.size() - 2) cnt1 = lsqrRows;

      uTasks[i] = new SolveAProd(cntProd.get(i), cntProd.get(i+1),
                                 cnt0, cnt1, aU, aULo, aV, aVLo, aRowSprs);
    }

    // clear the temporary curoff vector to hold the task stop and start
    // entries ... set the first entry to zero and set the entry cutoff
    // (entStep) to nEntsPerTask ... loop o/ver all column pointer entries.

    cntProd.clear();
    cntProd.add(0);
    entStep = nEntsPerTaskProd;
    count = 0;
    for (int i = 0; i < lsqrCols; ++i)
    {
      // if count is equal or exceeds the current entry cutoff then
      // save index i as a task cutoff point and increment the cutoff
      // count until it exceeds the current entry index in count.

      count += aColSprs.get(i).size();
      if (count >= entStep)
      {
        cntProd.add(i);
        while (entStep <= count) entStep += nEntsPerTaskProd;
      }
    }

    // done ... if the cutoff vector does not have aNColPtr-1 as its last entry
    // then add it

    if (cntProd.getLast() < lsqrCols) cntProd.add(lsqrCols);

    // create the vTasks array and fill it with tasks

    vTasks = new SolveAProd [cntProd.size() - 1];
    cnt0 = 0;
    cnt1 = 0;
    for (int i = 0; i < cntProd.size() - 1; ++i)
    {
      cnt0 = cnt1;
      cnt1 += nEntsPerTaskCol;
      if (cnt1 > lsqrCols) cnt1 = lsqrCols;
      if (i == cntProd.size() - 2) cnt1 = lsqrCols;

      vTasks[i] = new SolveAProd(cntProd.get(i), cntProd.get(i+1),
                                 cnt0, cnt1, aV, aVLo, aU, aULo, aColSprs);
    }
  }

  /**
   * Resets all large matrix/vector assignments to null to free memory.
   */
  public void resetLargeReferences()
  {
    aRowSprs = aColSprs = null;
  }

  /**
   * Sets the thread count multiplier to tmc and updates the total number of
   * usable threads to the processor count times this multiplier.
   * 
   * @param tmc The new thread count multiplier.
   */
  public static void setThreadCountMultiplier(double tmc)
  {
    aProcMult = tmc;    
    aNThreads = (int) (aNProcessors * aProcMult);
  }
}
