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

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;

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
* <p> The primary functions are the sequential solve:
*
* <p>     public int solve(ArrayListDouble sprsMatrix,
*                          ArrayListInt sprsColIndx, ArrayListInt sprsRowIndx,
*                          ArrayListDouble bRHS, double[] x, double[] se)
*                    throws IOException
* 
* <p> And the concurrent solve:
* 
* <p>     public int solve(ArrayListDouble sprsRowMtrx, ArrayListDouble sprsColMtrx,
*                          ArrayListInt sprsRowIndx, ArrayListInt sprsColIndx,
*                          ArrayListInt sprsRowPtr, ArrayListInt sprsColPtr,
*                          ArrayListDouble bRHS, double[] x, double[] se)
*                    throws IOException
*
* <p> In both cases the bRHS is the rhs vector of the problem definition, x is the
* solution vector and se is the solution error. Both x and se are solved for and
* filled on return. The vectors sprsMatrx, in the first case, and sprsRowMtrx in
* the second are the row-ordered sparse matrices used in the solution. The vector
* sprsColMtrx, in the second case, is the column-ordered sparse matrix which is
* required by the concurrent solver.
* 
* <p> The vector sprsRowIndx is the row-ordered index vector into "u", in the
* first case, and the column-ordered index vector into "u" in the second case
* (concurrent). The vector sprsColIndx is the row-ordered index vector into "v",
* in both cases. The vector sprsRowPtr is the The row start index of each new
* row in the row-ordered sparse matrix. This vector is only used by the
* concurrent solution. The vector sprsColPtr is the column start index of each
* new column in the column-ordered sparse matrix. This vector is also only used
* by the concurrent solution.
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
public class LSQR
{
  /**
   * The row-ordered sparse matrix. This is used by both the sequential solver
   * aProd and the concurrent solver submitTasks(SolveAProd[] tasks).
   */
  private double[] aSprsRow = null;

  /**
   * The row-ordered u vector (aU) index vector if this is a concurrent
   * solution, or the column-ordered u vector index vector if this is a
   * sequential solution.
   */
  private int[]    aRowIndx = null;

  /**
   * The row start index of each new row in the row-ordered sparse matrix
   * (aSprsRow). This vector is only used by the concurrent solution.
   */
  private int[]    aRowPtr  = null;

  /**
   * The column-ordered sparse matrix. This matrix is only used by the
   * concurrent solution.
   */
  private double[] aSprsCol = null;

  /**
   * The row-ordered v vector (aV) index vector if this is a concurrent
   * solution for aU tasks and both sequential aProd solutions for either
   * aU or aV.
   */
  private int[]    aColIndx = null;

  /**
   * The column start index of each new column in the column-ordered sparse
   * matrix (aSprsCol). This vector is only used in the concurrent solution.
   */
  private int[]    aColPtr  = null;

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
   * same size (the number of rows in the problem definition).
   */
  private double[] aU       = null;

  /**
   * The LSQR "v" vector which is initialized to 0 and has the same size as
   * the solution vector (aX.length which is the number of columns in the
   * problem definition). 
   */
  private double[] aV       = null;

  /**
   * The number of entries in the sparse matrices. aSprsRow, aSprsCol,
   * aRowIndx, and aColIndx all have this same size.
   */
  private int      aNSprs   = 0;

  /**
   * The size of aRowPTr.
   */
  private int      aNRowPtr = 0;

  /**
   * The size of aColPtr.
   */
  private int      aNColPtr = 0;
  
  /**
   * Machine precision.
   */
  public static final double RELPR = getMachinePrecision();

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
   * Total time to execute the solve function (seconds).
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
   * Default constructor.
   */
  public LSQR()
  {
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
   * @return Process execution time in seconds.
   */
  public double processExecutionTime()
  {
    return (double) lsqrExTime / 1000000000;
  }

  /**
   * The primary function responsible for performing the sequential LSQR
   * solution.
   * 
   * @param sprsMatrix The lhs sparse input matrix.
   * @param sprsColIndex The indirectly addressed set of column numbers that
   *                     address locations into the aV vector.
   * @param sprsRowIndex The indirectly addressed set of row numbers that
   *                     address locations into the aU vector.
   * @param bRHS The input rhs input vector.
   * @param x The output solution vector result.
   * @param se The output solution vector error estimate.
   * 
   * @return The final stop condition.
   * 
   * @throws IOException
   */
  public int solve(ArrayListDouble sprsMatrix,
                   ArrayListInt sprsColIndex, ArrayListInt sprsRowIndex,
                   ArrayListDouble bRHS, double[] x, double[] se)
             throws IOException
  {
    // get row, column, and sparse matrix size and set arrays from lists

    aSprsRow = sprsMatrix.getArray();
    aColIndx = sprsColIndex.getArray();
    aRowIndx = sprsRowIndex.getArray();
    aX       = x;
    aSE      = se;
    aNSprs   = sprsMatrix.size();
    lsqrRows = bRHS.size(); // M
    lsqrCols = x.length; // N

    // create temporary arrays

    aV = new double [lsqrCols];

    // copy bRHS into u

    aU = bRHS.toArray();
    for (int i = 0; i < lsqrRows; ++i) aU[i] = bRHS.get(i);

    // solve and return

    return solve();
  }

  /**
  /**
   * The primary function responsible for performing the concurrent LSQR
   * solution.
   * 
   * @param sprsRowMtrx The lhs row-ordered sparse input matrix.
   * @param sprsColMtrx The lhs column-ordered sparse input matrix.
   * @param sprsRowIndxMtrx The row index into the aU vector when solving
   *                        for aV using the column-ordered sparse matrix.
   * @param sprsColIndxMtrx The column index into the aV vector when solving
   *                        for aU using the row-ordered sparse matrix.
   * @param sprsRowPtr The entry offset start into the row-ordered sparse
   *                   matrix for each matrix row.
   * @param sprsColPtr The entry offset start into the column-ordered sparse
   *                   matrix for each matrix column.
   * @param bRHS The rhs residual vector.
   * @param x The solution vector.
   * @param se The solution error vector.
   * @return
   * @throws IOException
   */
  public int solve(ArrayListDouble sprsRowMtrx, ArrayListDouble sprsColMtrx,
                   ArrayListInt sprsRowIndxMtrx, ArrayListInt sprsColIndxMtrx,
                   ArrayListInt sprsRowPtr, ArrayListInt sprsColPtr,
                   ArrayListDouble bRHS, double[] x, double[] se)
             throws IOException
  {
    // get row, column, and sparse matrix size and set arrays from lists

    aSprsRow = sprsRowMtrx.getArray();
    aSprsCol = sprsColMtrx.getArray();
    aRowIndx = sprsRowIndxMtrx.getArray();
    aColIndx = sprsColIndxMtrx.getArray();
    aRowPtr  = sprsRowPtr.getArray();
    aColPtr  = sprsColPtr.getArray();
    aNRowPtr = sprsRowPtr.size();
    aNColPtr = sprsColPtr.size();

    aX       = x;
    aSE      = se;
    aNSprs   = sprsRowMtrx.size();
    lsqrRows = bRHS.size(); // M
    lsqrCols = x.length; // N

    // create temporary arrays

    aV = new double [lsqrCols];

    // copy bRHS into u

    aU = bRHS.toArray();
    for (int i = 0; i < lsqrRows; ++i) aU[i] = bRHS.get(i);

    // start threads, create task queue, build tasks

    aThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(aNThreads);
    aQueue = new ExecutorCompletionService<PartialResult>(aThreadPool);
    buildParallelTasks();

    // solve

    int solvestatus = solve();

    // shutdown and return

    aThreadPool.shutdown();
    aThreadPool = null;
    aQueue = null;
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
    double alfa, bbnorm, beta, cs, cs1, cs2, cTol, dampSq, ddnorm,
           delta, gamma, gamBar, phi, phiBar, psi, res1, res2, rho, rhoBar,
           rhBar1, rhBar2, rhs, rTol, sn, sn1, sn2, t, tau, test3,
           theta, t1, t2, t3, t3sq, xxnorm, z, zBar;

    // create temporary arrays

    double[] w = new double [lsqrCols];

    // output header if requested

    long start = System.nanoTime();
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

    alfa = 0.0;
    if (aSprsCol == null)
    {
      beta = dnrm2(aU);
  
      if (beta > 0.0)
      {
        dscl(1.0 / beta, aU);
  
        // use parallel solve if aSprsCol is not null
  
        aprod(aNSprs, aSprsRow, aV, aColIndx, aU, aRowIndx);
        alfa = dnrm2(aV);
      }
  
      if (alfa > 0.0)
      {
        dscl(1.0 / alfa, aV);
        for (i = 0; i < lsqrCols; ++i) w[i] = aV[i];
      }
    }
    else
    {
      //beta = dnrm2(aU);
      beta = concurrent2Norm(uTasks);
      
      if (beta > 0.0)
      {
        //dscl(1.0 / beta, aU);
        concurrentScale(1.0 / beta, uTasks);  
        concurrentUpdate(vTasks);
        //alfa = dnrm2(aV);
        alfa = concurrent2Norm(vTasks);
      }
  
      if (alfa > 0.0)
      {
        //dscl(1.0 / alfa, aV);
        concurrentScale(1.0 / alfa, vTasks);
        for (i = 0; i < lsqrCols; ++i) w[i] = aV[i];
      }
    }

    // finish initialization

    rhoBar = alfa;
    phiBar = beta;
    lsqrBNorm  = beta;
    lsqrRNorm  = beta;

    // test for x = 0 as an exact solution ... exit if true

    lsqrARNorm = alfa * beta;
    if (lsqrARNorm == 0.0)
    {
      // output x = 0 solution if requested

      lsqrExTime = System.nanoTime() - start;
      if (aScrnWrtr.isOutputOn()) writeOutput("Exit");

      resetLargeReferences();
      return lsqrIStop;
    }

    // ready to enter iteration loop ... output data header

    if (aScrnWrtr.isOutputOn()) 
    {
      lsqrCmptbl = 1.0;
      lsqrLSAcc  = alfa / beta;
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
      //       beta * u  =  A * v  -  alfa * u,
      //       alfa * v  =  A' * u - beta * v.

      if (aSprsCol == null)
      {
        // perform sequential scale/update

        dscl(-alfa, aU);
        aprod(aNSprs, aSprsRow, aU, aRowIndx, aV, aColIndx);
        beta = dnrm2(aU);
        bbnorm += alfa * alfa + beta * beta + dampSq;
  
        if (beta > 0.0)
        {
          dscl(1.0 / beta, aU);
          dscl(-beta, aV);
          aprod(aNSprs, aSprsRow, aV, aColIndx, aU, aRowIndx);
          alfa = dnrm2(aV);
          if (alfa > 0.0) dscl(1.0 / alfa, aV);
        }
      }
      else
      {
        // perform concurrent scale/update

        //dscl(-alfa, aU);
        concurrentScale(-alfa, uTasks);
        concurrentUpdate(uTasks);
        //beta = dnrm2(aU);
        beta = concurrent2Norm(uTasks);
        bbnorm += alfa * alfa + beta * beta + dampSq;

        if (beta > 0.0)
        {
          //dscl(1.0 / beta, aU);
          //dscl(-beta, aV);
          concurrentScale(1.0 / beta, uTasks);
          concurrentScale(-beta, vTasks);
          concurrentUpdate(vTasks);
          //alfa = dnrm2(aV);
          alfa = concurrent2Norm(vTasks);
          //if (alfa > 0.0) dscl(1.0 / alfa, aV);
          if (alfa > 0.0) concurrentScale(1.0 / alfa, vTasks);
        }
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

      rho     =  sqrt(rhBar2 + beta * beta);
      cs      =  rhBar1 / rho;
      sn      =  beta   / rho;
      theta   =  sn * alfa;
      rhoBar  = -cs * alfa;
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

      delta   =  sn2 * rho;
      gamBar  = -cs2 * rho;
      rhs     =  phi - delta * z;
      zBar    =  rhs / gamBar;
      lsqrXNorm   =  sqrt(xxnorm + zBar * zBar);
      gamma   =  sqrt(gamBar * gamBar + theta * theta);
      cs2     =  gamBar / gamma;
      sn2     =  theta  / gamma;
      z       =  rhs    / gamma;
      xxnorm +=  z * z;

      // Test for convergence.
      // First, estimate the norm and condition of the matrix  Abar,
      // and the norms of  rbar  and  Abar(transpose)*rbar.

      lsqrANorm  = sqrt(bbnorm);
      lsqrACond  = lsqrANorm * sqrt(ddnorm);
      res1   = phiBar * phiBar;
      res2  += psi * psi;
      lsqrRNorm  = sqrt(res1 + res2);
      lsqrARNorm = alfa * abs(tau);

      // Now use these norms to estimate certain other quantities,
      // some of which will be small near a solution.

      lsqrCmptbl = lsqrRNorm / lsqrBNorm;
      lsqrLSAcc  = 0.0;
      if (lsqrRNorm > 0.0) lsqrLSAcc = lsqrARNorm / (lsqrANorm * lsqrRNorm);
      test3  = 1.0   /  lsqrACond;
      t1     = lsqrCmptbl / (1.0  +  lsqrANorm * lsqrXNorm / lsqrBNorm);
      rTol   = lsqrBTol  +  lsqrATol *  lsqrANorm * lsqrXNorm / lsqrBNorm;

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

    lsqrExTime = System.nanoTime() - start;
    if (aScrnWrtr.isOutputOn()) writeOutput("Exit");

    // exit
 
    resetLargeReferences();
    return lsqrIStop;
  }

  /**
   * Sequential Sparse matrix multiply. Multiplies Asprs[i] * y[iysprs[i]] and
   * sums the result to x[ixsprs[i]].
   * 
   * @param nsprs The number of entries in Asprs.
   * @param Asprs The sparse matrix.
   * @param y The vector to be updated.
   * @param iysprs The indirect address vector into y for each entry, i, in
   *               Asprs.
   * @param x The vector multiplying Asprs to update y.
   * @param ixsprs The indirect address vector into x for each entry, i, in
   *               Asprs.
   */
  private void aprod(int nsprs, double[] Asprs,
                     double[] y, int[] iysprs,
                     double[] x, int[] ixsprs)
  {
    //for (int i = 0; i < nsprs; ++i) y[iysprs[i]] += Asprs[i] * x[ixsprs[i]];
    System.out.println("");
    System.out.println("i, ixsprs[i], x[ixsprs[i]], Asprs[i], iysprs[i], y[iysprs[i]], yafter");
    for (int i = 0; i < nsprs; ++i)
    {
      if (iysprs[i] == 0)
        System.out.print(i + ", " + ixsprs[i] + ", " + x[ixsprs[i]] + ", " + Asprs[i] + ", " +
                         iysprs[i] + ", " + y[iysprs[i]]);

      y[iysprs[i]] += Asprs[i] * x[ixsprs[i]];

      if (iysprs[i] == 0)
        System.out.println(", " + y[iysprs[i]]);
    }
  }

  /**
   * Finds the 2 norm of the input vector.
   * 
   * @param x The input vector whose 2 norm will be returned.
   * 
   * @return The input vectors 2 norm.
   */
  private double dnrm2(double[] x)
  {
    double d = 0.0;
    int n = x.length;
    for (int i = 0; i < n; ++i) d += x[i] * x[i];
    //if (true)
    //{
    //  BigDecimal dBD = new BigDecimal(0.0, MathContext.DECIMAL128);
    //  for (int i = 0; i < n; ++i)
    //  {
    //    BigDecimal bd = new BigDecimal(x[i], MathContext.DECIMAL128);
    //    dBD = dBD.add(bd.pow(2, MathContext.DECIMAL128), MathContext.DECIMAL128);
    //  }
    //  System.out.println(dBD.doubleValue() + ", " + (dBD.doubleValue() - d) +
    //                     ", " + (sqrt(dBD.doubleValue()) - sqrt(d)));
    //}
    return sqrt(d);
  }

  /**
   * Scales the input vector x by scl.
   * 
   * @param scl The scaling factor.
   * @param x the vector to be scaled.
   */
  private void dscl(double scl, double[] x)
  {
    int n = x.length;
    for (int i = 0; i < n; ++i) x[i] *= scl;
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
    }
    else if (type.equals("Exit"))
    {
      s = NL + aOutputIndent + "Exiting LSQR ..." + NL + aOutputIndent +
          "Process Execution Time (sec) = " + processExecutionTime() +
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
     * Row/column start index.
     */
    public int startIndex = 0;

    /**
     * Row/column end index.
     */
    public int stopIndex  = 0;

    /**
     * Used to return 2 norm and solution norm results. 
     */
    public double rslt    = 0.0;

    /**
     * Standard constructor set the row/column start and stop indices.
     * 
     * @param strt The row/column start index.
     * @param stop The row/column stop index.
     */
    public PartialResult(int strt, int stop)
    {
      startIndex = strt;
      stopIndex = stop;
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

    /**
     * The vector multiplying the sparse matrix to update aY (aV for uTasks
     * and aU for vTasks).
     */
    double[] aX     = null; //  aV            aU

    /**
     * The sparse matrix row or column start index vector (aRowPtr for uTasks
     * and aColPtr for vTasks). This vector contains the start position in
     * the sparse matrix for each row or column. For example if this is a
     * uTask and aPtr[5] = aRowPtr[5] = 2156. Then aSpM[2156] = aSprsRow[2156]
     * is the first entry for row 5. The last entry for row 5 is given by
     * aPtr[6]-1. 
     */
    int[]    aPtr   = null; //  aRowPtr       aColPtr

    /**
     * The sparse matrix that multiplies aX to update aY (aSprsRow for uTasks
     * and aSprsCol for vTasks).
     */
    double[] aSpM   = null; //  aSprsRow      aSprsCol

    /**
     * The index vector into aX for each entry in aSpM (aColIndex for uTasks
     * and aRowIndex for vTasks)
     */
    int[]    aXIndx = null; //  aColIndex     aRowIndex

    /**
     * Used by the scaling operation to scale the aY vector.
     */
    double   aScale = 0.0;

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
     * @param x The vector multiplying the sparse matrix to update y
     *          (aV for uTasks or aU for vTasks).
     * @param ptr The row or column pointer vector (aRowPtr for uTasks or
     *            aColPtr for vTasks).
     * @param sprs The row oriented or column oriented sparse matrix (aRowSprs
     *             for uTasks or aColSprs for vTasks).
     * @param xIndx The column index or row index vector to indirectly address
     *              into x (aColIndx for uTasks or aRowIndx for vTasks).
     */
    public SolveAProd(int strt, int stop, double[] y, double[] x,
                      int[] ptr, double[] sprs, int[] xIndx)
    {
      // set input values into class variables

      aPtr   = ptr;
      aY     = y;
      aX     = x;
      aSpM   = sprs;
      aXIndx = xIndx;

      aPR = new PartialResult(strt, stop);
    }

    /**
     * Puts the task into scale mode. When the concurrent submission calls
     * the function call() it will calle scale() to scale the aY vector by
     * the input scale value (scl).
     * 
     * @param scl The factor by aY will be scaled.
     */
    public void setScale(double scl)
    {
      aScale = scl;
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
     */
    private void update()
    {
      // loop over all rows or columns from startIndex to stopIndex-1

      for (int i = aPR.startIndex; i < aPR.stopIndex; ++i)
      {
        // get start and stop location in aSpM and aXIndx for all entries
        // between aPtr[i] (inclusive) and aPtr[i+1] (exclusive).

        int start = aPtr[i];
        int stop  = aPtr[i+1];

        // loop over all entries between start and stop-1 and calculate the
        // result r ... save the result into aY at index i and continue

        double r = aY[i];
        for (int j = start; j < stop; ++j) r += aSpM[j] * aX[aXIndx[j]];
        aY[i] = r;
      }
    }

    /**
     * The parallel call that will perform the dscl multiply of the aY
     * times the input scale factor aScale. This function is called by
     * each thread processing one of the tasks in uTasks or vTasks.
     */
    private void scale()
    {
      // scale each aY entry ... reset scale flag on exit

      for (int i = aPR.startIndex; i < aPR.stopIndex; ++i) aY[i] *= aScale;
      aScaleFlg = false;
    }

    /**
     * The parallel call that will perform the dscl multiply of the aY
     * times the input scale factor aScale. This function is called by
     * each thread processing one of the tasks in uTasks or vTasks.
     */
    private void twoNorm()
    {
      // sum the square of each aY entry ... place result in aPR.rslt for
      // return ... reset 2 norm flag on exit

      double d = 0.0;
      for (int i = aPR.startIndex; i < aPR.stopIndex; ++i) d += aY[i] * aY[i];
      aPR.rslt = d;

      a2NormFlg = false;
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
   * Used to execute the concurrent tasks in a sequential manner. This is
   * purely a debug flag that allows the concurrent tasks to be executed
   * sequentially for debugging purposes. Normally, it is off. It can be
   * set/reset using the functions setSequentialUpdateOn() and
   * setSequentialUpdateOff(). This flag has no effect when running in
   * sequential mode to begin with. 
   */
  private boolean      seqUpdateFlg = false;

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
   * to obtain aU or aV in a parallel fashion. If the boolean seqUpdateFlg is
   * true the tasks are processed in a sequential manner (useful for debugging).
   * 
   * @param tasks The tasks to be processed (uTasks or vTasks).
   */
  private void concurrentUpdate(SolveAProd[] tasks)
  {
    if (seqUpdateFlg)
    {
      // execute each task sequentially

      for (int i = 0; i < tasks.length; ++i) tasks[i].call();
    }
    else
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
  }

  /**
   * The parallel equivalent of dscl which is used to scale the aU or aV
   * vector by the input scl. If the boolean seqUpdateFlg is true the tasks
   * are processed in a sequential manner (useful for debugging).
   * 
   * @param tasks The tasks to be processed (uTasks or vTasks).
   */
  private void concurrentScale(double scl, SolveAProd[] tasks)
  {
    if (seqUpdateFlg)
    {
      // execute each task sequentially

      for (int i = 0; i < tasks.length; ++i)
      {
        tasks[i].setScale(scl);
        tasks[i].call();
      }
    }
    else
    {
      // submit all tasks in concurrent mode
  
      for (int i = 0; i < tasks.length; ++i)
      {
        tasks[i].setScale(scl);
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
  }

  /**
   * The parallel equivalent of dscl which is used to scale the aU or aV
   * vector by the input scl. If the boolean seqUpdateFlg is true the tasks
   * are processed in a sequential manner (useful for debugging).
   * 
   * @param tasks The tasks to be processed (uTasks or vTasks).
   */
  private double concurrent2Norm(SolveAProd[] tasks)
  {
    double d = 0.0;
    if (seqUpdateFlg)
    {
      // execute each task sequentially

      for (int i = 0; i < tasks.length; ++i)
      {
        tasks[i].set2Norm();
        d += tasks[i].call().rslt;
      }
    }
    else
    {
      // submit all tasks in concurrent mode
  
      for (int i = 0; i < tasks.length; ++i)
      {
        tasks[i].set2Norm();
        aQueue.submit(tasks[i]);
      }
  
      // sum result from each task to d
  
      int cnt = 0; 
      try
      {
        while (cnt < tasks.length)
        {
          Future<PartialResult> pr = aQueue.poll();
          if (pr != null)
          {
            cnt++;
            d += pr.get().rslt;          
          }
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }

    // return sqrt(d)

    return sqrt(d);
  }

  /**
   * Builds the parallel U and V tasks (uTasks, and vTasks) which will be
   * submitted to multiple threads for parallel solution.
   */
  private void buildParallelTasks()
  {
    // use number of processors as target number of tasks to create ...
    // nEntsPerTask is the rough number of entries in the sparse matrix that
    // will be assigned to an individual task. It is usually slightly larger
    // or smaller than this number as row and column boundaries are where the
    // tasks actually stop which don't usually coincide with nEntsPerTask
    // exactly

    int trgtTaskCnt = aNThreads;
    int nEntsPerTask = aNSprs / trgtTaskCnt;
    if (nEntsPerTask == 0) nEntsPerTask = 1;
    if (nEntsPerTask * trgtTaskCnt < aNSprs) ++nEntsPerTask;

    // create a temporary start / stop count vector to hold the task stop and
    // start entries. These are row start and stop points for each uTask and
    // column start and stop points for each vTask ... set the first entry to
    // zero and set the entry cutoff (entStep) to nEntsPerTask ... loop over
    // all row pointer entries.

    ArrayListInt cnt = new ArrayListInt(trgtTaskCnt);
    cnt.add(0);
    int entStep = nEntsPerTask;
    for (int i = 0; i < aNRowPtr; ++i)
    {
      // if aRowPtr[i] is equal or exceeds the current entry cutoff then
      // save index i as a task cutoff point and increment the cutoff
      // count until it exceeds the current entry index in aRowPtr[i].

      if (aRowPtr[i] >= entStep)
      {
        cnt.add(i);
        while (entStep <= aRowPtr[i]) entStep += nEntsPerTask;
      }
    }

    // done ... if the cutoff vector does not have aNRowPtr-1 as its last entry
    // then add it

    if (cnt.getLast() < aNRowPtr-1) cnt.add(aNRowPtr-1);

    // create the uTasks array and fill it with tasks

    uTasks = new SolveAProd [cnt.size() - 1];
    for (int i = 0; i < cnt.size() - 1; ++i)
    {
      uTasks[i] = new SolveAProd(cnt.get(i), cnt.get(i+1), aU, aV,
                                 aRowPtr, aSprsRow, aColIndx);
    }

    // clear the temporary curoff vector to hold the task stop and start
    // entries ... set the first entry to zero and set the entry cutoff
    // (entStep) to nEntsPerTask ... loop over all column pointer entries.

    cnt.clear();
    cnt.add(0);
    entStep = nEntsPerTask;
    for (int i = 0; i < aNColPtr; ++i)
    {
      // if aColPtr[i] is equal or exceeds the current entry cutoff then
      // save index i as a task cutoff point and increment the cutoff
      // count until it exceeds the current entry index in aColPtr[i].

      if (aColPtr[i] >= entStep)
      {
        cnt.add(i);
        while (entStep <= aColPtr[i]) entStep += nEntsPerTask;
      }
    }

    // done ... if the cutoff vector does not have aNColPtr-1 as its last entry
    // then add it

    if (cnt.getLast() < aNColPtr-1) cnt.add(aNColPtr-1);

    // create the vTasks array and fill it with tasks

    vTasks = new SolveAProd [cnt.size() - 1];
    for (int i = 0; i < cnt.size() - 1; ++i)
    {
      vTasks[i] = new SolveAProd(cnt.get(i), cnt.get(i+1), aV, aU,
                                 aColPtr, aSprsCol, aRowIndx);
    }
  }

  /**
   * Resets all large matrix/vector assignments to null to free memory.
   */
  private void resetLargeReferences()
  {
    uTasks   = null;
    vTasks   = null;
    aU       = null;
    aV       = null;

    aSprsRow = null;
    aRowIndx = null;
    aRowPtr  = null;
  
    aSprsCol = null;
    aColIndx = null;
    aColPtr  = null;
  }

  /**
   * Sets the concurrent sequential flag to true. This means that the
   * concurrent tasks will be executed in sequential order. This is
   * primarily a debugging flag to aide debugging the concurrent tasks.
   * Note that this flag has no effect if the sequential solve(...)
   * function was called.
   */
  public void setSequentialUpdateOn()
  {
    seqUpdateFlg = true;
  }

  /**
   * Sets the concurrent sequential flag to false. This means that the
   * concurrent tasks will be executed concurrently. This is primarily
   * a debugging flag to aide debugging the concurrent tasks. Note that
   * this flag has no effect if the sequential solve(...) function was
   * called.
   */
  public void setSequentialUpdateOff()
  {
    seqUpdateFlg = false;
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
