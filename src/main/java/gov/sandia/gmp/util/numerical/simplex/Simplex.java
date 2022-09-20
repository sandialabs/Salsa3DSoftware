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
package gov.sandia.gmp.util.numerical.simplex;

import static java.lang.Math.abs;

import java.io.Serializable;

import javax.swing.event.ChangeListener;

import gov.sandia.gmp.util.changenotifier.ChangeNotifier;

/**
 * <p>Simplex multi-dimensional minimization algorith which was
 * originally proposed by Nelder, J.A. and Mead, R., 1965, Computer Journal, v.
 * 7, pp. 308-313. This implementation was translated from a C++ version
 * published by Press, W. H., S. A. Teukolsky, W. T. Vetterling and B. P.
 * Flannery (2002), Numerical Recipes in C++, The Art of Scientific Computing,
 * 2nd Edition, Cambridge University Press. </p>
 *
 * * <p>This version performs function evaluations in parallel model.  There is
 * also a parallel version that performs multiple function evaluations sequentially.
 * </p>
</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Simplex implements Serializable 
{
	private SimplexFunction  function = null;
	
	/**
	 * If parallelMode is true then the (ndim+1) test points of the amoeba
	 * are all evaluated in parallel.  Otherwise they are evaluated sequentially.
	 */
	private boolean parallelMode;

	private int maxFunk;
	private int nFunk;
	private double tolerance;
	
	private static final int REFLECTION = 0;
	private static final int EXPANSION = 1;
	private static final int CONTRACTION = 2;

	protected ChangeNotifier changeNotifier;

	/**
	 * 
	 */
	public Simplex() { changeNotifier = new ChangeNotifier(this); }

	/**
	 *   
	 * @param function an instance of a class that implements the SimplexFunction interface.
	 * The SimplexFunction interface specified a method double simplexFunction(double[] x)
	 * that computes the function that is to be minimized.
	 * @param tolerance
	 * @param maxFunctionCalls
	 */
	public Simplex(SimplexFunction function, double tolerance, int maxFunctionCalls)
	{
		this.function = function;
		this.tolerance = tolerance;
		this.nFunk = 0;
		this.maxFunk = maxFunctionCalls;
		changeNotifier = new ChangeNotifier(this);
	}

	/**
	 * Add a visualization object that wants to be notified each time 
	 * a new simplex is available
	 * 
	 * @param listener ChangeListener
	 */
	public void addListener(ChangeListener listener) { changeNotifier.addListener(listener); }

	/**
	 * Multi-dimensional minimization of the function
	 * simlexFunction(double[] x) where x[0..ndim-1] is a vector in ndim dimensions.
	 *
	 *<p>This method added by Sandy Ballard 03/20/2015
	 *
	 * @param x on input: an initial guess at the ndim model parameters. 
	 * On output: contains the ndim model parameters at the minimum point.
	 * @param dx on input: the size of initial simplex in each of the ndim dimensions.
	 * On output: the size of the final simplex in each of the ndim dimensions.
	 * @return double the value of simplexFuntion(x) evaluated at the minimum point.
	 * @throws Exception
	 */
	public double search(double[] x, double[] dx, boolean restart) throws Exception
	{
		Amoeba simplex = new Amoeba(x, dx);
		changeNotifier.setSource(simplex);

		if (parallelMode)
		{
			// evaluate the points of the amoeba in parallel.
			simplexFunctionParallel(simplex.p, simplex.y);
			amoebaParallel(simplex);
		}
		else
		{
			// evaluate the poins of the ameoba sequentially.
			for (int i=0; i<simplex.p.length; ++i)
				simplex.y[i] = function.simplexFunction(simplex.p[i]);
			amoebaSequential(simplex);
		}
		nFunk += simplex.p.length;

		if (restart)
		{
			// restart at the minimum location
			for (int j=0; j<x.length; ++j)
				x[j] = simplex.p[0][j];

			simplex.set(x, dx);

			if (parallelMode)
			{
				simplexFunctionParallel(simplex.p, simplex.y);
				amoebaParallel(simplex);
			}
			else
			{
				for (int i=0; i<simplex.p.length; ++i)
					simplex.y[i] = function.simplexFunction(simplex.p[i]);
				amoebaSequential(simplex);
			}
			nFunk += simplex.p.length;

		}

		// replace x with best simplex point and 
		// dx misfit of each parameter relative to best parameter.
		for (int i=0; i<simplex.nparameters(); ++i)
		{
			x[i] = simplex.p[0][i];
			dx[i] = 0.;
			for (int j=0; j<simplex.nparameters(); ++j)
				dx[i] += abs(simplex.p[i+1][j]-simplex.p[0][j]);
		}

		return simplex.y[0];
	}

	/**
	 * Multi-dimensional minimization of the function
	 * simlexFunction(double[] x) where x[0..ndim-1] is a vector in ndim dimensions.
	 * The matrix p[0..ndim][0..ndim-1] is input.  Its ndim+1 rows are
	 * ndim-dimensional vectors that are the vertices of the starting simplex.  On
	 * output, p will have been reset to the ndim+1 new points all within tolerance
	 * of the a minimum function value.  Returns the value of the fitness
	 * function at each of the vertices of the simplex.
	 * <p>p and y are reordered such that y[0] holds the smallest function value
	 * and p[0] holds the corresponding set of parameters.
	 *
	 * @param p double[][]
	 * @return double[]
	 * @throws Exception
	 */
	public double[] search(double[][] p) throws Exception
	{
		Amoeba simplex = new Amoeba(p);

		if (parallelMode)
			simplexFunctionParallel(simplex.p, simplex.y);
		else
			for (int i=0; i<simplex.p.length; ++i)
				simplex.y[i] = function.simplexFunction(simplex.p[i]);
		return search(simplex);
	}

	/**
	 * Multi-dimensional minimization of the function
	 * simlexFunction(double[] x) where x[0..ndim-1] is a vector in ndim dimensions.
	 * The matrix p[0..ndim][0..ndim-1] is input.  Its ndim+1 rows are
	 * ndim-dimensional vectors that are the vertices of the starting simplex.  On
	 * output, p will have been reset to the ndim+1 new points all within tolerance
	 * of the a minimum function value.  Returns the value of the fitness
	 * function at each of the vertices of the simplex.
	 * <p>p and y are reordered such that y[0] holds the smallest function value
	 * and p[0] holds the corresponding set of parameters.
	 *
	 * @param p double[][]
	 * @return double[]
	 * @throws Exception
	 */
	public double[] search(Amoeba amoeba) throws Exception
	{
		changeNotifier.setSource(amoeba);

		if (parallelMode)
			amoebaParallel(amoeba);
		else
			amoebaSequential(amoeba);
		nFunk += amoeba.p.length;

		return amoeba.y;
	}

	/**
	 * See Press, et al., Numerical Recipes in C++, 2nd edition.
	 * @param p simplex
	 * @throws Exception 
	 */
	private void amoebaSequential(Amoeba simplex) throws Exception
	{
		while (true)
		{
			// extended types use this method to redefine the simplex
			simplex.redefine();
			
			// sort the points of the simplex into order of increasing y value.
			simplex.sort();
			double yBest = simplex.y[0];
			double yWorst = simplex.y[simplex.y.length-1];
			double ySecondWorst = simplex.y[simplex.y.length-2];
			
			// tell any listeners that there is a new simplex available for plotting.
			changeNotifier.fireStateChanged();

			// check for convergence
			//rtol = 2.0 * abs(y[ihi] - y[ilo]) / (abs(y[ihi]) + abs(y[ilo]) + 1.0e-10);
			if (simplex.isConverged(tolerance)) return;

			if (nFunk >= maxFunk)
				throw new SimplexException(
						String.format("ERROR in Simplex.amoeba(). NMAX=%d exceeded.", maxFunk));

			double[] ptry = new double[simplex.ndim];
			simplex.getTestPoint(1., ptry); // reflection
			double ytry = function.simplexFunction(ptry);
			++nFunk; 
			if (ytry < yBest)
			{
				// reflection produced a y value smaller than current smallest value, try expansion
				double[] pexpand = new double[simplex.ndim];
				simplex.getTestPoint(2., pexpand); // expansion
				double yexpand = function.simplexFunction(pexpand);
				++nFunk;
				if (yexpand < ytry)
					// expansion also produced a lower value.  keep it.
					simplex.replaceHighPoint(pexpand, yexpand);
				else
					// keep the reflection
					simplex.replaceHighPoint(ptry, ytry);
			}
			else if (ytry < ySecondWorst)
				// if reflection is smaller than the second worst point, 
				// keep the reflection
				simplex.replaceHighPoint(ptry, ytry);
			else
			{
				// try a 1D contraction along the line through the centroid and the worst point
				simplex.getTestPoint(-0.5, ptry); // contraction
				ytry = function.simplexFunction(ptry);
				++nFunk;
				if (ytry < yWorst)
					// contraction found a lower point; keep it
					simplex.replaceHighPoint(ptry, ytry);
				else
				{
					// still no lower value.  contract all points toward lo point.
					for (int i=1; i<simplex.npoints(); ++i)
					{
						for (int j = 0; j < simplex.ndim; j++)
							ptry[j] = simplex.plo[j] + 0.5 * (simplex.p[i][j] - simplex.plo[j]);
						
						simplex.replace(i, ptry, function.simplexFunction(ptry));
						nFunk+=simplex.ndim;
					}
				}
			}
			++simplex.index;
		}
	}
	
	/**
	 * See Press, et al., Numerical Recipes in C++, 2nd edition.
	 * @param p simplex
	 * @throws Exception 
	 */
	private void amoebaParallel(Amoeba simplex) throws Exception
	{
		while (true)
		{
			// sort the points of the simplex into order of increasing y value.
			simplex.sort();
			double yBest = simplex.y[0];
			double yWorst = simplex.y[simplex.y.length-1];
			double ySecondWorst = simplex.y[simplex.y.length-2];
			
			// tell any listeners that there is a new simplex available for plotting.
			changeNotifier.fireStateChanged();

			// check for convergence
			//rtol = 2.0 * abs(y[ihi] - y[ilo]) / (abs(y[ihi]) + abs(y[ilo]) + 1.0e-10);
			double rtol = simplex.y[simplex.y.length-1] - simplex.y[0];
			if (rtol < tolerance) return;

			if (nFunk >= maxFunk)
				throw new SimplexException(
						String.format("ERROR in Simplex.amoeba(). NMAX=%d exceeded.", maxFunk));

			// get a bunch of new points in model parameter space that need to be evaluated.
			// The number of points generated will equal 3 + n model parameters, representing 
			// all the possible outcomes of one iteration of the simplex algorithm.  
			double[][] testPoints = simplex.getTestPoints();
			double[] y = new double[testPoints.length];
			
			// perform function evaluations for all the test points in parallel.
			simplexFunctionParallel(testPoints, y);
			nFunk += testPoints.length;

			if (y[REFLECTION] < yBest)
			{
				// reflection produced a y value smaller than current smallest value
				if (y[EXPANSION] < y[REFLECTION])
					// expansion also produced a lower value.  keep it.
					simplex.replaceHighPoint(testPoints[EXPANSION], y[EXPANSION]);
				else
					// keep the reflection
					simplex.replaceHighPoint(testPoints[REFLECTION], y[REFLECTION]);
			}
			else if (y[REFLECTION] < ySecondWorst)
				simplex.replaceHighPoint(testPoints[REFLECTION], y[REFLECTION]);
			else if (y[CONTRACTION] < yWorst)
				simplex.replaceHighPoint(testPoints[CONTRACTION], y[CONTRACTION]);
			else
				// still no lower value.  contract all dimensions toward lo point.
				for (int i=1; i<simplex.npoints(); ++i)
					simplex.replace(i, testPoints[i+CONTRACTION], y[i+CONTRACTION]);
			
			++simplex.index;
		}
	}
	
	/**
	 * Evaluate the (ndim+1) test points of the amoeba in parallel.
	 * @param x
	 * @param y
	 * @throws Exception
	 */
	private void simplexFunctionParallel(final double[][] x, final double[] y) throws Exception
	{
		final Exception[] exceptions = new Exception[x.length];
		Thread[] threads = new Thread[x.length]; 
		for(int i = 0; i < x.length; i++)
		{
			final int index = i;
			threads[i] = new Thread()
			{
				@Override
				public void run()
				{
					try 
					{
						y[index] = function.simplexFunction(x[index]);
					} 
					catch (Exception e) 
					{
						exceptions[index] = e;
					}
				}
			};
		}
		
		for(Thread t : threads) t.start();
		for(Thread t : threads) t.join();
		
		for (int i=0; i<exceptions.length; ++i)
			if (exceptions[i] != null)
				exceptions[i].printStackTrace();
	}

	/**
	 * If parallelMode is true then the (ndim+1) test points of the amoeba
	 * are all evaluated in parallel.  Otherwise they are evaluated sequentially.
	 */
	public boolean isParallelMode() {
		return parallelMode;
	}

	/**
	 * If parallelMode is true then the (ndim+1) test points of the amoeba
	 * are all evaluated in parallel.  Otherwise they are evaluated sequentially.
	 */
	public void setParallelMode(boolean parallelMode) {
		this.parallelMode = parallelMode;
	}

	public double getTolerance() { return tolerance; }
	
	public void reset() {nFunk = 0;}
	
	public int getNumFunctionEvals() {return nFunk;}
}
