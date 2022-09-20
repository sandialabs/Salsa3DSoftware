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

/**
 * @author jrhipp
 *
 * An interface used by the LBGS.lbgs functions that contain the function
 * definitions setFunctionAndGradient(double[], double[]) and
 * setDiagonal(double[], double[]) to be overridden by the implementor
 * of this interface to provide appropriate gradient and inverse hessian
 * diagonal estimate return values at the input vector x.
 */
public interface LBFGSFunction
{
  /**
   * Sets the gradient at each value of x and returns the functional value
   * 
   * @param x The independent parameter vector.
   * @param g The gradient defined at each parameter entry.
   * @return The functional value to be minimized evaluted at x.
   */
  abstract double setFunctionAndGradient(double[] x, double[] g)
           throws LBFGSException;
  
  /**
   * Sets the diagonal of the inverse Hessian approximation at the current
   * value of the vector x.
   * 
   * @param x The vector of independent parameters.
   * @param diag The diagonal approximation of the inverse hessian evaluated
   *        at x.
   */
  abstract void   setDiagonal(double[] x, double[] diag);
}
