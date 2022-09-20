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

/**
 *
 * A java version of the LAPACK 3.0 Bunch-Kaufman Symmetric Indefinite
 * solver.
 *
 * This object contains routines solve(...) that finds the solution of an
 * input matrix (assumed to be symmetric indefinite) for a series of RHS
 * vectors. Two forms of the solver exist such that the matrix and solution
 * vectors may be chosen to be preserved or not. If the matrix was not
 * solvable a flag is set indicating such (isSolvable() is false).
 *
 * Copyright (c) 1992-2008 The University of Tennessee.  All rights reserved.
 *
 */
public final class LDLTDecomposition implements MatrixSolver {

	// The current matrix row/column size.
	private int sidmN;

    // The current number of RHS to be solved for.
	private int sidmNRHS;

   // The current matrix is-solved state.
	private boolean sidmIsSolvable;

	private boolean valid = false;

	private double[] matrixCache = null;

	private int[] ipivCache = null;

	  // Initialize ALPHA for use in choosing pivot block size.
	private static final double ALPHA = (Math.sqrt(17.) + 1.) / 8.;

	/**
	 *
	 * Default constructor.
	 *
	 */
	public LDLTDecomposition() {
		sidmN = 0;
		sidmNRHS = 0;
        sidmIsSolvable = false;
	}

	/**
	 * Copy constructor.
	 *
	 * @param sidm {@link MatrixContainer} to copy state from.
	 */
	public LDLTDecomposition(LDLTDecomposition ldlt) {
		set(ldlt);
	}

	/**
	 * Assignment method.
	 *
	 *
	 * @param sidm {@link MatrixContainer} to copy state from.
	 * @throws Throws a {@link NullPointerException} on null input.
	 */
	public void set(LDLTDecomposition ldlt) {
		sidmN = ldlt.sidmN;
		sidmNRHS = ldlt.sidmNRHS;
        sidmIsSolvable = ldlt.sidmIsSolvable;
	}

	/**
	 * DSPTRF computes the factorization of a real symmetric matrix A stored
	 * in packed format using the Bunch-Kaufman diagonal pivoting method:
	 *
	 *        A = L*D*L**T
	 *
	 * where L is a product of permutation and unit lower
	 * triangular matrices, and D is symmetric and block diagonal with
	 * 1-by-1 and 2-by-2 diagonal blocks.
	 *
	 * Based on modifications by J. Lewis, Boeing Computer Services Company in
	 * May of 1996.
	 *
	 *     with A = L*D*L', where
	 *        L = P(1)*L(1)* ... *P(k)*L(k)* ...,
	 *     i.e., L is a product of terms P(k)*L(k), where k increases from 1 to
	 *     n in steps of 1 or 2, and D is a block diagonal matrix with 1-by-1
	 *     and 2-by-2 diagonal blocks D(k).  P(k) is a permutation matrix as
	 *     defined by IPIV(k), and L(k) is a unit lower triangular matrix, such
	 *     that if the diagonal block D(k) is of order s (s = 1 or 2), then
	 *
	 *                (   I    0     0   )  k-1
	 *        L(k) =  (   0    I     0   )  s
	 *                (   0    v     I   )  n-k-s+1
	 *                   k-1   s  n-k-s+1
	 *
	 *     If s = 1, D(k) overwrites A(k,k), and v overwrites A(k+1:n,k).
	 *     If s = 2, the lower triangle of D(k) overwrites A(k,k), A(k+1,k),
	 *     and A(k+1,k+1), and v overwrites A(k+2:n,k:k+1).
	 *
	 * LAPACK routine (version 3.0) --
	 * Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,
	 * Courant Institute, Argonne National Lab, and Rice University
	 * March 31, 1993.
	 *
	 * Converted to C++ by Jim Hipp April 22, 2004.
	 *
	 * @param n	The order of the matrix a (n >= 0).
	 * @param ap the lower triangle of the symmetric matrix a, packed
	 * 	columnwise in a linear array. The j-th column of a is stored in the
	 * 	array ap as follows:
	 *
	 *   <code> ap(i + (j-1)*(2n-j)/2) = a(i,j) for j<=i<=n. </code>
	 *
	 * 	On exit, the block diagonal matrix D and the multipliers used to obtain
	 * 	the factor L, are stored as a packed triangular matrix overwriting a.
	 * @param ipiv Details of the interchanges and the block structure of D.
	 * 	If IPIV(k) > 0, then rows and columns k and IPIV(k) were interchanged
	 * 	and D(k,k) is a 1-by-1 diagonal block. If IPIV(k) = IPIV(k+1) < 0, then
	 * 	rows and columns k+1 and -IPIV(k) were interchanged and D(k:k+1,k:k+1)
	 * 	is a 2-by-2 diagonal block.
	 * @return Returns info integer.
	 * @throws Throws an {@link IllegalArgumentException} with a description of the problem for any bad args.
	 */
	private static final int dsptrf(int n, double[] ap, int[] ipiv) {
		int imax = 0, jmax;
		double t;
		int kstep;
		double r1, d11, d21, d22;
		int kk, kp;
		double absakk, wk;
		int kx;
		double colmax, rowmax;
		int knc, kpc = 0, npp;
		double wkp1;

		//make sure matrix order is valid
		if (n < 0) {
			throw new IllegalArgumentException("Error in DSPTRF ... Matrix order less than 0 (" + n + ") ...");
		}

		// Factorize A as L*D*L using the lower triangle of A
		// K is the main loop index, increasing from 1 to N in steps of 1 or 2
		int info = 0;
		int k = 1;
		int kc = 1;
		npp = n * (n + 1) / 2;

		do {
			knc = kc;

			// If K > N, exit from loop
			if (k > n)
				return info;

			kstep = 1;

			// Determine rows and columns to be interchanged and whether
			// a 1-by-1 or 2-by-2 pivot block will be used
			absakk = Math.abs(ap[kc-1]);

			// IMAX is the row-index of the largest off-diagonal element in
			// column K, and COLMAX is its absolute value
			if (k < n) {
				imax = k + idamax(n - k, ap, kc, 1);
				colmax = Math.abs(ap[kc + imax - k - 1]);
			} else
				colmax = 0.;

			if (Math.max(absakk, colmax) == 0.) {
				// Column K is zero: set INFO and continue
				if (info == 0) info = k;
				kp = k;
			} else {
				if (absakk >= ALPHA * colmax) {
					// no interchange, use 1-by-1 pivot block
					kp = k;
				} else {
					// JMAX is the column-index of the largest off-diagonal
					// element in row IMAX, and ROWMAX is its absolute value
					rowmax = 0.;
					kx = kc + imax - k;
					for (int j = k; j <= imax - 1; ++j) {
						if (Math.abs(ap[kx-1]) > rowmax) {
							rowmax = Math.abs(ap[kx-1]);
							jmax = j;
						}
						kx += n - j;
					}

					// Computing MAX
					kpc = npp - (n - imax + 1) * (n - imax + 2) / 2 + 1;
					if (imax < n) {
						jmax = imax + idamax(n - imax, ap, kpc, 1);
						rowmax = Math.max(rowmax, Math.abs(ap[kpc + jmax - imax - 1]));
					}

					if (absakk >= ALPHA * colmax * (colmax / rowmax)) {
						// no interchange, use 1-by-1 pivot block

						kp = k;
					} else if (Math.abs(ap[kpc-1]) >= ALPHA * rowmax) {
						// interchange rows and columns K and IMAX, use 1-by-1
						// pivot block
						kp = imax;
					} else {
						// interchange rows and columns K+1 and IMAX, use 2-by-2
						// pivot block
						kp = imax;
						kstep = 2;
					}
				}

				kk = k + kstep - 1;
				if (kstep == 2)
					knc += n - k + 1;
				if (kp != kk) {
					// Interchange rows and columns KK and KP in the trailing
					// submatrix A(k:n,k:n)

					if (kp < n)
						dswap(n - kp, ap, knc + kp - kk, 1, ap, kpc, 1);

					kx = knc + kp - kk;

					for (int j = kk; j < kp - 1; ++j) {
						kx += n - j;
						t = ap[knc - kk + j];
						ap[knc - kk + j] = ap[kx - 1];
						ap[kx - 1] = t;
					}

					t = ap[knc - 1];
					ap[knc - 1] = ap[kpc - 1];
					ap[kpc - 1] = t;
					if (kstep == 2) {
						t = ap[kc];
						ap[kc] = ap[kc + kp - k - 1];
						ap[kc + kp - k - 1] = t;
					}
				}

				// Update the trailing submatrix

				if (kstep == 1) {
					// 1-by-1 pivot block D(k): column k now holds
					// W(k) = L(k)*D(k)
					// where L(k) is the k-th column of L

					if (k < n) {
						// Perform a rank-1 update of A(k+1:n,k+1:n) as
						// A := A - L(k)*D(k)*L(k)' = A - W(k)*(1/D(k))*W(k)'

						r1 = 1. / ap[kc - 1];
						dspr(n - k, -r1, ap, kc, ap, kc + n  - k);

						// Store L(k) in column K

						dscal(n - k, r1, ap, kc, 1);
					}
				} else {
					// 2-by-2 pivot block D(k): columns K and K+1 now hold
					// ( W(k) W(k+1) ) = ( L(k) L(k+1) )*D(k)
					// where L(k) and L(k+1) are the k-th and (k+1)-th columns of L

					if (k < n - 1) {
						// Perform a rank-2 update of A(k+2:n,k+2:n) as
						//       A := A - ( L(k) L(k+1) )*D(k)*( L(k) L(k+1) )'
						//          = A - ( W(k) W(k+1) )*inv(D(k))*( W(k) W(k+1) )'

						kk  = (k - 1) * ((n + 1) - k) / 2;
						int kk1 = k * ((n + 1) - k - 1) / 2;

						d21 = ap[kk-1+k + 1];
						double d21Inv = 1.0/d21;
						d11 = ap[kk1-1+k + 1] * d21Inv;
						d22 = ap[kk-1+k] * d21Inv;
						t = 1. / (d11 * d22 - 1.);
						d21 = t * d21Inv;

						for (int j = k + 1; j < n; ++j) {
							wk   = d21 * (d11 * ap[kk+j] - ap[kk1+j]);
							wkp1 = d21 * (d22 * ap[kk1+j] - ap[kk+j]);

							int ii = (j) * ((n + 1) - j - 1) / 2;

							for (int i = j; i < n; ++i) {
								ap[ii+i] -= ap[kk+i] * wk + ap[kk1+i] * wkp1;
							}

							ap[kk-1+j]  = wk;
							ap[kk1-1+j] = wkp1;

						}
					}
				}
			}

			// Store details of the interchanges in IPIV
			if (kstep == 1)
				ipiv[k-1] = kp;
			else {
				ipiv[k-1] = -kp;
				ipiv[k] = -kp;
			}

			// Increase K and return to the start of the main loop
			k += kstep;
			kc = knc + n - k + 2;

		} while(true); // end do
	}


	/**
	 * IDAMAX finds the index of element having max. absolute value.
	 *
	 * Written for Linpack on 11-March-1978 by Jack Dongarra
	 * modified 3/93 to return if incx .le. 0.
	 * modified 12/3/93, array(1) declarations changed to array(*)
	 *
	 * BLAS routine (version 3.0) --
	 * Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,
	 * Courant Institute, Argonne National Lab, and Rice University
	 * December 3, 1993.
	 *
	 * Converted to C++ by Jim Hipp April 22, 2004.
	 *
	 * @param n  The number of elements along x for which the maximum will be
	 * 	found.
	 * @param dx The x vector for which the maximum element will be determined.
	 * @param dxStart The starting index for dx.
	 * @param incx The increment along dx. Only dx[dxStart+i*incx] elements are checked
	 * 	for i=1 to n.
	 * @return Index of max absolute value.
	 */
	private final static int idamax(int n, double[] dx, int dxStart, int incx) {
		int i, ix;

		// Function Body ... check for simple inputs of n and incx ... and return
		// 0 or 1 appropriately

		if (n < 1 || incx <= 0)
			return 0;
		if (n == 1) return 1;

		// initialize ret_val and find the max index ... use faster loop if
		// increment is 1

		int returnValue = 0;
	    ix = 1;
	    double dmax = Math.abs(dx[dxStart]);
	    ix += incx;
	    for (i = 2; i <= n; ++i) {
	    	if (Math.abs(dx[dxStart+ix-1]) > dmax) {
	    		returnValue = i;
	    		dmax = Math.abs(dx[dxStart+ix-1]);
	    	}
	    	ix += incx;
	    }

	    // return result and exit
	    return returnValue;
	}


	/**
	 * DSWAP interchanges two vectors. Uses unrolled loops for increments
	 * equal one.
	 *
	 * Written for Linpack on 11-March-1978 by Jack Dongarra
	 * modified 12/3/93, array(1) declarations changed to array(*)
	 *
	 * BLAS routine (version 3.0) --
	 * Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,
	 * Courant Institute, Argonne National Lab, and Rice University
	 * December 3, 1993.
	 *
	 * Converted to C++ by Jim Hipp April 22, 2004.
	 *
	 * @param n The number of elements to be swapped between dx and dy.
	 * @param dx The x vector to be swapped with y.
	 * @param dxStart The starting index of dx.
	 * @param incx The increment along dx. Only dx[i*incx] elements are swapped
	 * 	for i=1 to n.
	 * @param dy The y vector to be swapped with x.
	 * @param dyStart The starting index of dy.
	 * @param incy The increment along dy. Only dy[i*incy] elements are swapped
	 * 	for i=1 to n.
	 */
	private final static void dswap(int n, double[] dx, int dxStart, int incx, double[] dy, int dyStart, int incy) {
		//exit if n is trivial
		if (n <= 0)
			return;

		int ix = 0;
		int iy = 0;
		if (incx < 0)
			ix = (-n + 1) * incx;
		if (incy < 0)
			iy = (-n + 1) * incy;

		for (int i = 0; i < n; ++i) {
			double dtemp = dx[dxStart+ix];
			dx[dxStart+ix] = dy[dyStart+iy];
			dy[dyStart+iy] = dtemp;
			ix += incx;
			iy += incy;
		}
	}


	/**
	 * DSPR performs the symmetric rank 1 operation
	 *        A := alpha*x*x' + A,
	 * where <code>alpha</code> is a real scalar, <code>x</code> is an <code>n</code> element vector and
	 * <code>a</code> is an <code>n</code> by <code>n</code> symmetric matrix, supplied in lower triangular
	 * packed form.
	 *
	 * Level 2 Blas routine.
	 * Written on 22-October-1986.
	 *     Jack Dongarra, Argonne National Lab.
	 *     Jeremy Du Croz, Nag Central Office.
	 *     Sven Hammarling, Nag Central Office.
	 *     Richard Hanson, Sandia National Labs.
	 *
	 * BLAS routine (version 3.0) --
	 * Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,
	 * Courant Institute, Argonne National Lab, and Rice University
	 * December 3, 1993.
	 *
	 * Converted to C++ by Jim Hipp April 22, 2004.
	 *
	 * Note: incx parameter removed since the one place this is called in the code it was
	 * 	equal to 1.
	 *
	 * @param n The order of matrix a.
	 * @param alpha The matrix multiplicative scalar for the rank-1 operation.
	 * @param x The n element vector in the rank-1 matrix operation.
	 * @param xStart Starting index of x.
	 * @param ap Matrix of dimension at least n * (n + 1) / 2. Must contain
	 * 	the lower triangular part of the symmetric matrix packed sequentially,
	 * 	column by column, so that ap(1) contains a(1, 1), ap(2) and ap(3) contain
	 * 	a(2, 1) and a(3, 1) respectively, and so on. On exit, the array ap is
	 * 	overwritten by the lower triangular part of the updated matrix.
	 * @param apStart Starting index of ap.
	 * @throws Throws an {@link IllegalArgumentException} with a description of the problem for any bad args.
	 */
	private final static void dspr(int n, double alpha, double[] x, int xStart, double[] ap, int apStart) {
		//make sure n and incx are valid
		if (n < 0) {
			throw new IllegalArgumentException("Error in DSPR ... Matrix order less than 0 (" + n + ") ...");
		}

		// Quick return if possible.
		if (n == 0 || alpha == 0.)
			return;

		// Start the operations. In this version the elements of the array AP
		// are accessed sequentially with one pass through AP (in lower triangular
		// packed form

		int kk = 0;
		for (int j = 0; j < n; ++j) {
			if (x[xStart+j] != 0.) {
				double temp = alpha * x[xStart+j];
				int k = kk;
				for (int i = j; i < n; ++i) {
					ap[apStart+k] += x[xStart+i] * temp;
					k++;
				}
			}
			kk += n - j;
		}

	}

	/**
	 * DSCAL scales a vector by a constant. Uses unrolled loops when
	 * increment is equal to one.
	 *
	 * Written for Linpack on 11-March-1978 by Jack Dongarra
	 * modified 3/93 to return if incx .le. 0.
	 * modified 12/3/93, array(1) declarations changed to array(*)
	 *
	 * BLAS routine (version 3.0) --
	 * Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,
	 * Courant Institute, Argonne National Lab, and Rice University
	 * December 3, 1993.
	 *
	 * Converted to C++ by Jim Hipp April 22, 2004.
	 *
	 * @param n The length of the vector dx to be scaled.
	 * @param da The scalar by which the elements of dx are to be scaled.
	 * @param dx The vector to be scaled.
	 * @param dxStart Starting index of dx.
	 * @param incx  The increment along dx. Only dx[i*incx] elements are scaled
	 * 	for i=1 to n.
	 *
	 * @throws Throws an {@link IllegalArgumentException} with a description of the problem for any bad args.
	 */
	private final static void dscal(int n, double da, double[] dx, int dxStart, int incx) {
		//exit if n or incx are trivial
		if (n <= 0 || incx <= 0)
			return;

		int nincx = n * incx;
		for (int i = 0; i < nincx; i += incx)
			dx[dxStart+i] *= da;
	}

	/**
	 * DSPTRS solves a system of linear equations A*X = B with a real
	 * symmetric matrix A stored in packed format using the factorization
	 * A = L*D*L**T computed by DSPTRF.
	 *
	 * LAPACK routine (version 3.0) --
	 * Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,
	 * Courant Institute, Argonne National Lab, and Rice University
	 * March 31, 1993.
	 *
	 * Converted to C++ by Jim Hipp April 22, 2004.
	 *
	 * @param n - The order of the matrix a (n >= 0).
	 * @param nrhs - The number of right hand sides, i.e., the number of columns
	 * 	of the matrix b (nrhs >= 0).
	 * @param ap - The block diagonal matrix D and the multipliers used to
	 * 	obtain the factor L as computed by DSPTRF, stored as a packed triangular
	 * 	matrix.
	 * @param ipiv - Details of the interchanges and the block structure of D
	 * 	as determined by DSPTRF.
	 * @param b - The RHS matrix b. On exit b contains the solution matrix x.
	 * @param ldb - The leading dimension of the array b (ldb >= max(1,n)).
	 * @return info - Successful if 0 on exit.
	 * @throws Throws an {@link IllegalArgumentException} with a description of the problem for any bad args.
	 */
	private final static int dsptrs(int n, int nrhs, double[] ap, int[] ipiv,
            double[] b, int ldb) {
		final double c_b7 = -1.;
		final double c_b19 = 1.;

		int j, k, kc, kp, ii;
		double akm1k, denom, ak, bk, akm1, bkm1;
		int bp;

		// reset ap and ipiv back one location ... I guess for zero based arrays?

//		--ap;
//		--ipiv;

		// offset b by 1 and ldb

//		b -= (ldb + 1);

		// Function Body ... check for errors

		int info = 0;
		if (n < 0) {
			throw new IllegalArgumentException("Error in DSPTRS ... Matrix order less than 0 (" + n + ") ...");
		} else if (nrhs < 0) {
			throw new IllegalArgumentException("Error in DSPTRS ... The Number of RHS columns cannot be "
	         + "less than 0 (" + nrhs + ") ...");
		} else if (ldb < Math.max(1,n)) {
			throw new IllegalArgumentException("Error in DSPTRS ... LDB (=" + ldb + ") Must be at least "
	         + Math.max(1, n) + " ...");
		}

		// Quick return if possible

		if (n == 0 || nrhs == 0)
			return info;

		// Solve A*X = B, where A = L*D*L'.

		// First solve L*D*X = B, overwriting B with X.

		// K is the main loop index, increasing from 1 to N in steps of
		// 1 or 2, depending on the size of the diagonal blocks. */

		k = 0;
		kc = 1;

		do {
			// If K > N, exit from loop. */

			if (k + 1 > n)
				break;


			if (ipiv[k] > 0) {
				// 1 x 1 diagonal block
				// Interchange rows K and IPIV(K).

				kp = ipiv[k];
				if (kp != k + 1)
					dswap(nrhs, b, k, ldb, b, kp - 1, ldb);

				// Multiply by inv(L(K)), where L(K) is the transformation
				// stored in column K of A.

				if (k < n)
					dger(n - k - 1, nrhs, c_b7, ap, kc, 1, b, ldb + k - (ldb),
							ldb, b, ldb + k + 1 - (ldb), ldb);

				// Multiply by the inverse of the diagonal block.

				dscal(nrhs, 1. / ap[kc - 1], b, k, ldb);
				kc += n - k;
				k++;
			} else {
				// 2 x 2 diagonal block
				// Interchange rows K+1 and -IPIV(K).

				kp = -ipiv[k];
				if (kp != k + 2)
					dswap(nrhs, b, k + 1, ldb, b, kp - 1, ldb);

				// Multiply by inv(L(K)), where L(K) is the transformation
				// stored in columns K and K+1 of A.

				if (k < n - 2) {
					ii = n - k;
					dger(ii, nrhs, c_b7, ap, kc + 1, 1, b, ldb + k - (ldb),
							ldb, b, ldb + k + 2 - (ldb), ldb);
					dger(ii, nrhs, c_b7, ap, kc + n - k, 1, b, ldb + k + 1 - (ldb),
							ldb, b, ldb + k + 2 - (ldb), ldb);
				}

				// Multiply by the inverse of the diagonal block.

				akm1k = ap[kc];
				akm1 = ap[kc - 1] / akm1k;
				ak = ap[kc + n - k - 1] / akm1k;
				denom = akm1 * ak - 1.;
				bp = k;
				for (j = 1; j <= nrhs; ++j) {
					bkm1 = b[bp] / akm1k;
					bk = b[bp + 1] / akm1k;
					b[bp] = (ak * bkm1 - bk) / denom;
					b[bp + 1] = (akm1 * bk - bkm1) / denom;
					bp += ldb;
				}
				kc += (n - (k + 1) + 1) + 1;
				k += 2;
			}
		} while(true);

		// Next solve L'*X = B, overwriting B with X.
		// K is the main loop index, decreasing from N to 1 in steps of
		// 1 or 2, depending on the size of the diagonal blocks.

		k = n;
		kc = n * (n + 1) / 2 + 1;
		do {
			// If K < 1, exit from loop.

			if (k < 1)
				break;


			kc -= n - k + 1;
			if (ipiv[k - 1] > 0) {
				// 1 x 1 diagonal block
				// Multiply by inv(L'(K)), where L(K) is the transformation
				// stored in column K of A.

				if (k < n)
					dgemv(n - k, nrhs, c_b7, b, ldb + k + 1 - (ldb + 1), ldb,
							ap, kc, 1, c_b19, b, ldb + k - (ldb + 1), ldb);

				// Interchange rows K and IPIV(K).

				kp = ipiv[k - 1];
				if (kp != k)
					dswap(nrhs, b, k - 1, ldb, b, kp - 1, ldb);
				k--;
			} else {
				// 2 x 2 diagonal block
				// Multiply by inv(L'(K-1)), where L(K-1) is the transformation
				// stored in columns K-1 and K of A.

				if (k < n) {
					dgemv(n - k, nrhs, c_b7, b, ldb + k + 1 - (ldb + 1), ldb,
							ap, kc, 1, c_b19, b, ldb + k - (ldb + 1), ldb);
					dgemv(n - k, nrhs, c_b7, b, ldb + k + 1 - (ldb + 1), ldb,
							ap, kc - (n - k) - 1, 1, c_b19, b, ldb + k - 1 - (ldb + 1), ldb);
				}

				// Interchange rows K and -IPIV(K).

				kp = -ipiv[k - 1];
				if (kp != k)
					dswap(nrhs, b, k - 1, ldb, b, kp - 1, ldb);
				kc -= n - k + 2;
				k -= 2;
			}
		} while(true);

		return info;
	} // end dsptrs

	/**
	 * DGER   performs the rank 1 operation
	 * \code       A := alpha*x*y' + A, \endcode
	 * where \em alpha is a scalar, \em x is an \em m element vector, \em y is
	 * an \em n element vector and \em a is an \em m by \em n matrix.
	 *
	 * Level 2 Blas routine.
	 * Written on 22-October-1986.
	 *     Jack Dongarra, Argonne National Lab.
	 *     Jeremy Du Croz, Nag Central Office.
	 *     Sven Hammarling, Nag Central Office.
	 *     Richard Hanson, Sandia National Labs.
	 *
	 * BLAS routine (version 3.0) --
	 * Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,
	 * Courant Institute, Argonne National Lab, and Rice University
	 * December 3, 1993.
	 *
	 * Converted to C++ by Jim Hipp April 22, 2004.
	 *
	 * @param m - The number of rows of matrix a. Must be at least 0.
	 * @param n - The number of columns of matrix a. Must be at least 0.
	 * @param alpha - Multiplicative scalar for the rank-1 operation.
	 * @param x - The m element column vector in the rank-1 operation.
	 * @param xStart Starting index for x
	 * @param incx - The increment value along the elements of x. Must be > 0.
	 * @param y - The n element row vector in the rank-1 operation.
	 * @param yStart Starting index for y
	 * @param incy - The increment value along the elements of y. Must be > 0.
	 * @param a - The leading m by n part of the array a must contain the matrix
	 * 	of coefficients. On exit, a is overwritten by the updated matrix.
	 * @param aStart Starting index for a
	 * @param lda - Specifies the first dimension of a as declared in the
	 * 	calling function. Must be at least max(1, m).
	 *
	 * @throws Throws an {@link IllegalArgumentException} with a description of the problem for any bad args.
	 */
	private static final void dger(int m, int n, double alpha, double[] x, int xStart, int incx,
            double[] y, int yStart, int incy, double[] a, int aStart, int lda) {
		double temp;
		int ix, jy, kx;
		int aj;

		if (m < 0) {
			throw new IllegalArgumentException("Error in DGER ... Matrix row count is less than 0 (" + m
	          + ") ...");
		} else if (n < 0) {
			throw new IllegalArgumentException("Error in DGER ... Matrix column count is less than 0 (" + n
	          + ") ...");
		} else if (incx == 0) {
			throw new IllegalArgumentException("Error in DGER ... X element increment is zero ...");
		} else if (incy == 0) {
			throw new IllegalArgumentException("Error in DGER ... Y element increment is zero ...");
		} else if (lda < Math.max(1, m)) {
			throw new IllegalArgumentException("Error in DGER ... LDA (=" + lda + ") Must be at least "
	         + Math.max(1, m) + " ...");
		}

		// Quick return if possible.

		if (m == 0 || n == 0 || alpha == 0.)
			return;

		// Start the operations. In this version the elements of a are
		// accessed sequentially with one pass through a.

		if (incy > 0)
			jy = yStart;
		else
			jy = yStart - (n - 1) * incy;

		if (incx == 1) {
			for (int j = 0; j < n; ++j) {
				if (y[jy] != 0.) {
					temp = alpha * y[jy];
					aj = j * lda + aStart;
					for (int i = 0; i < m; ++i)
						a[aj + i] += x[i + xStart] * temp;
				}
				jy += incy;
			}
		} else {
			if (incx > 0)
				kx = 1;
			else
				kx = 1 - (m - 1) * incx;

			for (int j = 0; j < n; ++j) {
				if (y[jy] != 0.) {
					temp = alpha * y[jy];
					ix = kx + xStart;
					aj = j * lda + aStart;
					for (int i = 0; i < m; ++i) {
						a[aj + i] += x[ix] * temp;
						ix += incx;
					}
				}
				jy += incy;
			}
		}
	}

	/**
	 * DGEMV  performs the matrix-vector operation
	 *        y := alpha*A'*x + beta*y,
	 * where \em alpha and \em beta are scalars, \em x and \em y are vectors
	 * and \em a is an \em m by \em n matrix.
	 *
	 * Level 2 Blas routine.
	 * Written on 22-October-1986.
	 *     Jack Dongarra, Argonne National Lab.
	 *     Jeremy Du Croz, Nag Central Office.
	 *     Sven Hammarling, Nag Central Office.
	 *     Richard Hanson, Sandia National Labs.
	 *
	 * BLAS routine (version 3.0) --
	 * Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,
	 * Courant Institute, Argonne National Lab, and Rice University
	 * December 3, 1993.
	 *
	 * Converted to C++ by Jim Hipp April 22, 2004.
	 *
	 * @param m The number of rows of matrix a. Must be at least 0.
	 * @param n The number of columns of matrix a. Must be at least 0.
	 * @param alpha Multiplicative scalar for the rank-1 matrix operation.
	 * @param a  The leading m by n part of the array a must contain the matrix
	 * 	of coefficients. On exit, a is overwritten by the updated matrix.
	 * @param aStart Starting index of a.
	 * @param lda Specifies the first dimension of a as declared in the
	 * 	calling function. Must be at least max(1, m).
	 * @param x The m element col. vector in the rank-1 matrix/vector operation.
	 * @param xStart Starting index of x.
	 * @param incx The increment value along the elements of x. Must be > 0.
	 * @param beta Multiplicative scalar for the rank-1 vector operation.
	 * @param y The n element row vector in the rank-1 matrix/vector operation.
	 * @param yStart Starting index of y.
	 * @param incy  The increment value along the elements of y. Must be > 0.
	 *
	 * @throws Throws an {@link IllegalArgumentException} with a description of the problem for any bad args.
	 */
	private static final void dgemv(int m, int n, double alpha, double[] a, int aStart, int lda,
            double[] x, int xStart, int incx, double beta, double[] y, int yStart, int incy) {
		double temp;
		int lenx, leny;
		int ix, iy, jy, kx, ky;
		int aj;

		if (m < 0) {
			throw new IllegalArgumentException("Error in DGEMV ... Matrix row count is less than 0 (" + m
					+ ") ...");
		} else if (n < 0) {
			throw new IllegalArgumentException("Error in DGEMV ... Matrix column count is less than 0 (" + n
					+ ") ...");
		} else if (incx == 0) {
			throw new IllegalArgumentException("Error in DGEMV ... X element increment is zero ...");
		} else if (incy == 0) {
			throw new IllegalArgumentException("Error in DGEMV ... Y element increment is zero ...");
		} else if (lda < Math.max(1, m)) {
			throw new IllegalArgumentException("Error in DGEMV ... LDA (=" + lda + ") Must be at least "
					+ Math.max(1, m) + " ...");
		}

		// Quick return if possible.

		if (m == 0 || n == 0 || alpha == 0. && beta == 1.)
			return;


		// Set lenx and leny, the lengths of the vectors x and y, and set
		// up the start points in x and y. (NOTE: in this rendition TRANS is
		// "T" or "C" only ... NOT N).

		lenx = m;
		leny = n;

		if (incx > 0)
			kx = 0;
		else
			kx = -(lenx - 1) * incx;

		if (incy > 0)
			ky = 0;
		else
			ky = -(leny - 1) * incy;

		// Start the operations. In this version the elements of a are
		// accessed sequentially with one pass through a. First form  y := beta*y.

		if (beta != 1.) {
			if (incy == 1) {
				if (beta == 0.) {
					for (int i = 0; i < leny; ++i)
						y[yStart + i] = 0.;
				} else {
					for (int i = 0; i < leny; ++i)
						y[yStart + i] *= beta;
				}
			} else {
				iy = ky;
				if (beta == 0.) {
					for (int i = 0; i < leny; ++i) {
						y[yStart + iy] = 0.;
						iy += incy;
					}
				} else {
					for (int i = 0; i < leny; ++i) {
						y[yStart + iy] *= beta;
						iy += incy;
					}
				}
			}
		}

		if (alpha == 0.)
			return;

		// Form  y := alpha*A'*x + y.
		jy = ky;
		if (incx == 1) {
			for (int j = 0; j < n; ++j) {
				temp = 0.;
				aj = j * lda;
				for (int i = 0; i < m; ++i)
					temp += a[aStart + aj+i] * x[xStart + i];
				y[yStart + jy] += alpha * temp;
				jy += incy;
			}
		} else {
			for (int j = 0; j < n; ++j) {
				temp = 0.;
				ix = kx;
				aj = j * lda;
				for (int i = 0; i < m; ++i) {
					temp += a[aStart + aj+i] * x[xStart + ix];
					ix += incx;
				}
				y[yStart + jy] += alpha * temp;
				jy += incy;
			}
		}
	}

	/**
	 * Solves the system <code>m</code> x = <code>b</code> where <code>m</code> is an <code>n</code> x <code>n</code>
	 * symmetric indefinite lower triangular matrix and <code>b</code> is a set of <code>nrhs</code>
	 * RHS solution vectors of size <code>n</code> .
	 * The matrix <code>m</code> must contain at least <code>n</code>  * (<code>n</code> + 1) / 2 elements
	 * and b must have at least <code>n</code> * <code>nrhs</code> elements. Extra elements are
	 * ignored. On exit the matrix is overwritten with the bunch-kaufman LDLT
	 * decomposition and <code>b</code> is overwritten with x. This function calls the
	 * LAPACK 3.0 dspsv routine.
	 *
	 * @param n - The size of the square symmetric indefinite lower triangular
	 * 	matrix m.
	 * @param m - The n x n symmetric indefinitie lower triangular matrix in
	 * 	packed form (a[0][0], a[1][0], a[1][0], ..., a[n-1][0], a[1][1], a[1][2],
	 * 	..., a[n-1][n-1]). On output m is overwritten by the bunch-kaufman LDLT
	 * 	decomposition.
	 * @param nrhs - The number of RHS vectors to be solved for.
	 * @param b - The RHS solution vectors. On output b is overwritten with the
	 * 	solution x.
	 *
	 */
	public final void solve(int n, double[] m, int nrhs, double[] b) {
		int[] ipiv;

		// set solvable flag to false

		sidmIsSolvable = false;

		// calculate matrix size and verify it is valid

		sidmN = n;
		if (sidmN * (sidmN + 1) > 2 * m.length) {
			// Error m must be a lower triangular n x n square matrix such that
			// m.size() >= n * (n + 1) / 2

			throw new IllegalArgumentException("Symmetric Indefinite Matrix Sizing Error ...\n"
	       + "Input Matrix Size (" + m.length
	       + ") Must Be Equal To Or Larger Than N * (N + 1) / 2 ("
	       + sidmN * (sidmN + 1) / 2 + ") ...");
		}

		// calculate number of nrhs entries and verigy it is valid

		sidmNRHS = nrhs;
		if ((sidmNRHS < 1) || (sidmNRHS * sidmN > b.length)) {
			// Error: b.size() must be an integer multiple of n

			throw new IllegalArgumentException("RHS Column Vector Sizing Error ...\n"
		       + "Input Column Vector Size (" + b.length
		       + ") Must Be Equal To Or Larger Than NRHS * N (" + sidmNRHS * sidmN
		       + ") ...");
		}

		// resize row permutation vector if necessary

		ipiv = new int[sidmN + 1];

		// solve system

		dspsv(sidmN, sidmNRHS, m, ipiv, b, sidmN);

		//completed
		sidmIsSolvable = true;
	}


	/**
	 *
	 * DSPSV computes the solution to a real system of linear equations
	 *        A * X = B,
	 * where A is an N-by-N symmetric matrix stored in packed format and X
	 * and B are N-by-NRHS matrices.
	 *
	 * The diagonal pivoting method is used to factor A as
	 *        A = L * D * L**T,
	 * where L is a product of permutation and unit lower
	 * triangular matrices, D is symmetric and block diagonal with 1-by-1
	 * and 2-by-2 diagonal blocks.  The factored form of A is then used to
	 * solve the system of equations A * X = B.
	 *
	 * The packed storage scheme is illustrated by the following example
	 * when n = 4:
	 *
	 *     Two-dimensional storage of the symmetric matrix a:
	 *
	 *        a11
	 *        a21 a22
	 *        a31 a32 a33         (aij = aji)
	 *        a41 a42 a43 a44
	 *
	 *     Packed storage of the lower triangle of A:
	 *
	 *     ap = [a11, a21, a31, a41, a22, a32, a42, a33, a43, a44]
	 *
	 * LAPACK driver routine (version 3.0) --
	 * Univ. of Tennessee, Univ. of California Berkeley, NAG Ltd.,
	 * Courant Institute, Argonne National Lab, and Rice University
	 * March 31, 1993.
	 *
	 * Converted to C++ by Jim Hipp April 22, 2004.
	 *
	 * @param n - The order of the matrix a (n >= 0).
	 * @param nrhs - The number of right hand sides, i.e., the number of columns
	 * 	of the matrix b (nrhs >= 0)
	 * @param ap - the lower triangle of the symmetric matrix a, packed
	 * 	columnwise in a linear array. The j-th column of a is stored in the
	 * 	array ap as follows:
	 *
	 *   <code> ap(i + (j-1)*(2n-j)/2) = a(i,j) for j<=i<=n. </code>
	 *
	 * 	On exit, the block diagonal matrix D and the multipliers used to obtain
	 * 	the factor L in DSPTRF, are stored as a packed triangular matrix
	 * 	overwriting a.
	 * @param ipiv - Details of the interchanges and the block structure of D.
	 * 	If IPIV(k) > 0, then rows and columns k and IPIV(k) were interchanged
	 * 	and D(k,k) is a 1-by-1 diagonal block. If IPIV(k) = IPIV(k+1) < 0, then
	 * 	rows and columns k+1 and -IPIV(k) were interchanged and D(k:k+1,k:k+1)
	 * 	is a 2-by-2 diagonal block.
	 * @param b - The n-by-nrhs RHS matrix b. On exit, if info = 0, the n-by-nrhs
	 * 	solution matrix x.
	 * @param ldb - The leading dimension of the array b (ldb >= max(1,n)).
	 * @throws Throws an {@link IllegalArgumentException} with a description of the problem for any bad args.
	 */
	private static final void dspsv(int n, int nrhs, double[] ap, int[] ipiv,
            double[] b, int ldb) {
		// Function Body
		if (n < 0) {
			throw new IllegalArgumentException("Error in DSPSV ... Matrix order less than 0 (" + n + ") ...");
		} else if (nrhs < 0) {
			throw new IllegalArgumentException("Error in DSPSV ... The Number of RHS columns cannot be "
	         	+ "less than 0 (" + nrhs + "))");
		} else if (ldb < Math.max(1,n)) {
			throw new IllegalArgumentException("Error in DSPSV ... LDB (=" + ldb + ") Must be at least "
				+ Math.max(1, n) + " ...");
		}

		// Compute the factorization A = U*D*U' or A = L*D*L'. */

		dsptrf(n, ap, ipiv);

		dsptrs(n, nrhs, ap, ipiv, b, ldb);

	}

	/**
	 *
	 * Returns true if the matrix is solvable.
	 *
	 * @return
	 */
	public boolean isSolvable()	{
		return sidmIsSolvable;
	}

	/**
	 * Flattens a 2d array of doubles.
	 * Note: assumes an even (non-ragged) array.
	 *
	 * @param d Array to flatten.
	 * @return Flattened array or an empty array for null arrays.
	 */
	public final static double[] flatten(double[][] d) {
		if(d == null || d.length < 1) {
			return new double[] {};
		}

		double[] output = new double[d.length*d[0].length];

		int index = 0;

		int length = d[0].length;

		for(int n = 0; n < d.length; n++) {
			System.arraycopy(d[n], 0, output, index, length);
			index += length;
		}

		return output;
	}

	/**
	 * Expands a flattened array of doubles.
	 *
	 *
	 * @param to
	 * @param from
	 */
	public final static void expand(double[][] to, double[] from) {
		if(to.length*to[0].length != from.length) {
			throw new IllegalArgumentException("\"to\" array isn't the right size: " + to.length*to[0].length + " != " + from.length);
		}

		int length = to[0].length;

		int index = 0;

		for(int n = 0; n < to.length; n++) {
			System.arraycopy(from, index, to[n], 0, length);

			index += length;
		}
	}

	/**
	 * Flattens and compresses a triangular matrix array of doubles.
	 *
	 *
	 * @param d
	 * @return
	 */
	public final static double[] flattenAndCompressSymmetric(double[][] d) {
		if(d == null || d.length < 1) {
			return new double[] {};
		}

		int length = d.length;

		int copyLength = length;

		int index = 0;

		double[] output = new double[(length*length+length)/2];

		for(int n = 0; n < length; n++) {
			System.arraycopy(d[n], 0, output, index, copyLength);

			index += copyLength;
			copyLength--;
		}


		return output;
	}

	/**
	 * Expands and decompresses an array of doubles.
	 *
	 * @param to
	 * @param from
	 */
	public final static void expandAndDecompress(double[][] to, double[] from) {
		int length = to.length;

		int copyLength = length;

		int index = 0;

		for(int n = 0; n < length; n++) {
			System.arraycopy(from, index, to[n], 0, copyLength);

			index += copyLength;
			copyLength--;
		}
	}

	/**
	 * Decomposes matrix into DSPTRF form, only converted back into 2D.
	 *
	 *
	 */
	//@Override
	public void decompose(double[][] A) {
		valid = false;

		matrixCache = flattenAndCompressSymmetric(A);
		ipivCache = new int[A.length];

		sidmN = A.length;

		dsptrf(sidmN, matrixCache, ipivCache);

		expandAndDecompress(A, matrixCache);

		valid = true;	//will only get here if no errors
	}

	//@Override
	public boolean isValid() {
		return valid;
	}

	/**
	 * Uses the LDLT decomposition to solve for a.
	 *
	 *
	 */
	//@Override
	public void solve(double[][] a, double[][] b) {
		if(isValid()) {

			sidmNRHS = a.length;

			double[] b_ = flatten(a);

			dsptrs(sidmN, sidmNRHS, matrixCache, ipivCache, b_, sidmN);

			expand(a, b_);
		}
	}

	/**
	 * Uses the LDLT decomposition to solve for a.
	 *
	 *
	 */
	//@Override
	public void solve(double[][] a) {
		if(isValid()) {
			sidmNRHS = a.length;

			double[] b = flatten(a);

			dsptrs(sidmN, sidmNRHS, matrixCache, ipivCache, b, sidmN);

			expand(a, b);
		}
	}
}
