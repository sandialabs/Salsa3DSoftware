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
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.Arrays;

import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.observation.ObservationComponent;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.matrix.Matrix;
import gov.sandia.gmp.util.numerical.matrix.SingularValueDecomposition;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

/**
 * <p>Title: LocOO3D</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Sandia National Laboratories</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public class SolverLSQ
extends Solver
{
	protected PropertiesPlusGMP properties;

	/**
	 * total number of tables that are to be written
	 */
	protected int io_observation_tables;
	
	/**
	 * number of tables that have already been written
	 */
	protected int n_observation_tables;

	protected boolean io_iteration_table;

	int N;
	// Number of defining observation used to constrain the
	// solution.

	int M;
	// The number of free event location parameters in the
	// inversion.

	protected ArrayListDouble r = new ArrayListDouble();
	// Weighted residuals.  (Observed - predicted)/uncertainty.
	// Unitless.  Length N. See lsq_algorithm.pdf equation 2.3.

	protected double[][] U, V;
	protected Matrix A = new Matrix(1, 1);
	protected double[] W = new double[4];

	protected ArrayListDouble X = new ArrayListDouble(4);
	// The change in event location imposed each iteration.

	protected String comment; // a short comment that gets output in the iteration table.

	protected boolean generate_output;
	// determines whether or not to send information to output buffers.

	protected double[][] data_resolution_matrix;;
	// Data resolution matrix.  Only calculated and printed
	// when lsq_print_data_resolution_matrix is true.

	protected ArrayListDouble data_importance = new ArrayListDouble();
	// Diagonal elements of data resolution matrix.

	protected ArrayListInt locPar = new ArrayListInt(4);
	// The indices of the event location parameters that are not
	// held fixed.

	protected int lsq_maxiterations;
	// Maximum number of iterations that will performed before
	// the algorithm stops iterating.

	protected int K; // Jordan-Sverdrup K parameter.  See lsq_algorithm.pdf
	// Section 6.2.3

	protected boolean lsq_print_data_resolution_matrix;
	// Controls printing of entire data resolution matrix.

	protected int lsq_convergence_satisfied;
	// Number of times in a row that convergence test must be
	// passed for final convergence to be declared.

	protected int lsq_convergence_n;
	// Number of times in a row that convergence test has
	// actually been passed.

	protected double old_sswr, new_sswr;
	// The sum squared weighted residuals at the start and end
	// of the current iteration, respectively.

	protected double lsq_convergence_criterion;
	// When lsq_convergence_value is less than this value, the
	// convergence test is passed.

	protected double lsq_condition_number;
	// The ratio of the largest to the smallest singular value.

	protected double lsq_singular_value_cutoff;
	// Singular values less than this value times the largest
	// singular value are set equal to zero.

	protected double lsq_damping_factor;
	// Damping factor to apply to singular values.  If less than
	// zero, automatic damping is implemented (strongly
	// recommended).  See lsq_algorithm.pdf Section 5.

	protected double lsq_initial_applied_damping;
	// When AUTOMATIC DAMPING is being applied, this is the
	// initial damping factor applied when an increase in the
	// sum squared weighted residuals is observed.

	protected double lsq_applied_damping_multiplier;
	// If the initial applied damping does not reduce the sum
	// squared weighted residuals to a level below that observed
	// in the previous iteration, keep multiplying the applied
	// damping by this factor until it does, or untiil the
	// damping is so large that the solution stops moving
	// (see lsq_damping_dkm_threshold).

	protected double lsq_damping_dkm_threshold;
	// During automatic damping, the applied damping will
	// continue to increase until either the sum squared weighted
	// residual is reduced to a level less than that observed
	// in the previous iteration, or until the applied damping
	// is so large that the solution stops moving.  The quantity
	// dkm is the amount, in km, that the solution will move
	// during an iteration.  When dkm becomes less than
	// lsq_damping_dkm_threshold, it can be concluded that the
	// sum squared weighted residuals cannot be further reduced
	// and convergence can be declared.

	//protected double sswr_gradient, sswr_gradient_threshold;

	protected boolean done; // True when all conditions necessary to stop iterating have
	// been achieved.

	protected boolean converged; // True when solution has converged. See lsq_algorithm.pdf
	// Section 3.

	protected int sIterationCount; // Iteration Count for the Current
	// locate() Attempt

	protected int sMaxIterations; // Maximum Iteration Count

	protected int sBaseIteration; // Iteration Count at the Solver
	// Level.  This is initialized to zero
	// at the entrance to locateEvent

	protected boolean sLocalMinima; // Flag to indicate likely existance
	// of local minima.  Should be
	// initialized to false but set true
	// by a concrete solver if the solver
	// detects anything that might indicate
	// existance of local minima.

	protected double gen_confidence_level, gen_apriori_standard_error;
	
	public SolverLSQ(PropertiesPlusGMP properties)
	throws PropertiesPlusException, LocOOException, GMPException
	{
		super();
		this.properties = properties;
		setup(this.properties);
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Extract configuration parameters from locoo.configInfo(), a TConfigInfo
	// object that contains parameter information normally retrieved from a parameter
	// file.
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN:      NONE
	//
	// *****************************************************************************
	//@Override
	public void setup(PropertiesPlusGMP properties)
	throws LocOOException, PropertiesPlusException
	{
		sMaxIterations = properties.getInt(
				"lsq_max_iterations", 100);

		gen_confidence_level = properties.getDouble(
				"gen_confidence_level", 0.95);

		gen_apriori_standard_error = properties.getDouble(
				"gen_apriori_standard_error",
				1.);

		lsq_convergence_criterion = properties.getDouble(
				"lsq_convergence_criterion",
				0.0001);

		lsq_convergence_n = properties.getInt(
				"lsq_convergence_n", 2);

		lsq_print_data_resolution_matrix = properties.getBoolean(
				"lsq_print_data_resolution_matrix", false);

		lsq_singular_value_cutoff = properties.getDouble(
				"lsq_singular_value_cutoff",
				1e-5);

		lsq_damping_factor = properties.getDouble(
				"lsq_damping_factor", -1.);

		lsq_damping_dkm_threshold = properties.getDouble(
				"lsq_damping_dkm_threshold",
				0.01);

		lsq_applied_damping_multiplier = properties.getDouble(
				"lsq_applied_damping_multiplier", 10);

		lsq_initial_applied_damping = properties.getDouble(
				"lsq_initial_applied_damping", 1e-8);

		io_observation_tables = properties.getInt("io_observation_tables", 
			properties.getInt("io_max_obs_tables", 2));

		io_iteration_table = properties.getBoolean("io_iteration_table", true);
		
		generate_output = true;

	} // END setup

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Solves for the seismic event location given the vector of observations.
	// Upon completion, the results container object should be updated.
	//
	// INPUT ARGS:
	//   LocatorResults&   results    Reference to a LocatorResults container to
	//	                                 return with the final event location results
	// OUTPUT ARGS: NONE
	// RETURN:
	//   bool                         Cancellation Flag
	//	                                 true  = Cancel the event location
	//
	//	                                 false = No error condition, the event
	//	                                           location ran to conclusion
	//
	// *****************************************************************************
	@Override
	public void locateEvents(EventList events) throws Exception
	{
		// Iterate over each of the events in memory.
		for (Event event : events.values())
		{
			long timer = System.nanoTime();

			if (events.parameters.initialLocationMethod().equals("internal"))
				event.setInitialLocation(event.calculateInitialLocation());
				
			event.getEventParameters().needDerivatives(true);

			// this call sends the current location of this event to the predictor
			// to make sure that event and the predictor start out on the same page.
			event.setLocation(event.getInitialLocation());

			event.locationTrack = new ArrayList<Location>();
			event.locationTrack.add(event.getLocation());

			if (event.logger.getVerbosity() >= 3)
			{
				event.logger.writeln();
				event.logger.writeln("==========================================================================");
				event.logger.writeln();
				event.logger.writeln(event.getInputLocationString());
				event.logger.writeln(event.getSiteTable());
				event.logger.writeln(event.getObservationTable());
				event.logger.writeln(event.getCorrelationMatrixString(event.getObsComponents()));
				event.logger.writeln();
			}

			// initialize iteration counters.
			sBaseIteration = 0;
			sIterationCount = 0;

			converged = false;

			if (sMaxIterations == 0)
				eventLocation(event);
			else
				try
			{
					//------------------------------------------------------------------------
					// While the set of defining observations and depth are in a state of flux,
					// continue to relocate the event with the new set.  During location, the
					// defining/non-defining status of observations can change if they
					// involve predictions that are extrapolated, either horizontally or
					// vertically, or if they have big residuals.
					//------------------------------------------------------------------------
					do
					{
						sBaseIteration += sIterationCount;
						sIterationCount = 0;

						event.positionUpToDate = false;

						// check if problem is contrained
						event.checkConstraints();
						
						// locate the event
						eventLocation(event);

						//------------------------------------------------------------------------
						// Check the set of observations and see if any should be reset to
						// non-defining.  Also check to see if depth is out of user-specified range.
						//------------------------------------------------------------------------

					}
					while (event.checkBigResiduals() || event.checkDepthConstraints());
			} 
			catch (Exception e)
			{
			    	event.errorMessages.append(e.getMessage()+"\n");
				event.logger.writeln(e);
				event.errorlog.writeln(e);
				converged = false;
				event.setLocatorResults(null);
			}

			event.updateAll();
			write_to_out_buf(event, true); 

			if (event.logger.getVerbosity() >= 3)
				event.logger.writeln(event.getIterationTable());

			if (event.logger.getVerbosity() >= 2)
			{
				if (event.getLocatorResults() == null)
				{
					//event.logger.writeln(event.errorlog.getStringBuffer().toString());
					event.logger.writeln(String.format(
							"No results available for event orid=%d evid=%d%n%n",
							event.getSource().getSourceId(), event.getSource().getEvid()));
				}
				else
					event.logger.writeln(event.getLocatorResults().toString());
				
			}

			//------------------------------------------------------------------------
			// generate grid of residuals, if requested by user in par file.
			//------------------------------------------------------------------------
			event.griddedResiduals();

			timer = System.nanoTime()-timer;
			event.getSource().setCalculationTime(timer*1e-9);
			
			if (event.logger.getVerbosity() >= 2)
				event.logger.write(String.format("Time to compute this location = %1.6f seconds%n%n", 
						timer*1e-9));
		}
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Locate the seismic event and return a LocatorResults object containing the
	// location and associated uncertainties.
	//
	// *****************************************************************************
	private boolean eventLocation(Event event)
	throws Exception
	{
		// initialize variables needed to detect local minima
		sLocalMinima = false;

		N = event.getDefiningVec().size(); //number of defining observations
		M = event.nFree();
		
		if (M == 0) 
		    throw new Exception("Cannot relocate event when lat, lon, depth, time are all fixed");
		
		if (sMaxIterations <= 1)
		{
			comment = "fixed";
			sMaxIterations = 0;
			initialization(event);
			event.dkm = 0.;
			converged = true;
			write_to_out_buf(event, false);
			event.setLocatorResults(getLocatorResults(event));
		}
		else 
		{
			comment = "start";
			if (!locate(event))
				return false;
			event.setLocatorResults(getLocatorResults(event));
		}
		return true;
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Locate a single seismic event. Returns true if no errors encountered.
	//
	// *****************************************************************************
	private boolean locate(Event event) throws Exception
	{
		if (event.depthFixed())
		{
			// if user specified a specific depth in the properties file,
			// set the event depth to that value.  Otherwise, if event.fixedDepthIndex is 
			// set to 0 or 1, then set depth to either top or bottom of the 
			// seismicityDepthRange.
			if (!Double.isNaN(event.fixedDepthValue))
				event.setDepth(event.fixedDepthValue);
			else if (event.fixedDepthIndex == 0 || event.fixedDepthIndex == 1)
				event.setDepth(event.getSeismicityDepthRange());
		}

		initialization(event);

		do // this is the start of the main location iteration loop
		{
			++sIterationCount; //increment the iteration counter
			
			event.iterationCount = sBaseIteration+sIterationCount;

			//Save a deep copy of the working location at the start of the iteration.
			Location sOldLocation = event.getLocation();

			// populate the local VectorMods and matrices for the lsq solver
			// (weighted residuals in R, and weighted derivatives in A).
			if (!populateNRContainers(event))
				throw new LocOOException("ERROR: A matrix contains huge values.");

			// cannot locate if the number of free parameters is > the number of defining observations.
			if (M > N)
			{
//				event.errorlog.writeln(String.format(
//						"Unable to locate event because N < M in locate(). SourceId = %d  N=%d M=%d", 
//						event.getSource().getSourceId(), N, M));
//				return false;
			    throw new LocOOException(String.format(
						"ERROR: Unable to locate event because N < M in locate(). SourceId = %d  N=%d M=%d", 
						event.getSource().getSourceId(), N, M));
			}

			SingularValueDecomposition svd = new SingularValueDecomposition(A);

			U = svd.getU().getArray();
			V = svd.getV().getArray();
			W = svd.getSingularValues();

			//find Wmax, calculate the condition number.
			double Wmax = processSingularValues(W);

			//Solve A' * A * dm = A' * r for dm.
			svdlm(U, W, V, lsq_singular_value_cutoff * Wmax, event.applied_damping, r, X);

			// Expand the new solution vector to full size (4 elements).
			// Event location components that are held fixed will be zero.
			// Units of dm will be [radians, radians, km, seconds].
			// Then add the change in location to the previous location.
			// dkm will be the amount that the location moved, in km.
			expand_dm(event, sOldLocation);

			write_to_out_buf(event, false); 

			event.dkm = event.moveLocation(sOldLocation, event.dloc);

			// if depth is fixed to top or bottom of seismicityDepthRange then
			// reset the depth to be consistent with the new lat, lon location.
			if (event.fixedDepthIndex == 0 || event.fixedDepthIndex == 1)
				event.setDepth(event.getSeismicityDepthRange());

			// calculate the sum squared weighted residuals at the new location
			new_sswr = event.getSumSqrWeightedResiduals();

			if (event.definingListChanged())
			{
				lsq_convergence_satisfied = 0;
				if (lsq_damping_factor < 0.)
					event.applied_damping = lsq_initial_applied_damping;
			}

			// Levenberg-Marquardt loop. Perform the following if automatic damping is in effect.
			if (lsq_damping_factor < 0.)
			{
				// if the sum squared weighted residuals has increased, then increase the
				// amount of damping and relocate starting from the location at the begining
				// of this iteration.  Don't do this if the change in location is insignificant.
				while (new_sswr >= old_sswr && event.dkm >= lsq_damping_dkm_threshold)
				{
					// increase the applied damping
					event.applied_damping *= lsq_applied_damping_multiplier;

					//Solve A' * A * dm = A' * r for dm.
					svdlm(U, W, V, lsq_singular_value_cutoff * Wmax, event.applied_damping, r, X);

					//Add the change in location to the location that was valid at the begining
					//of the iteration.
					expand_dm(event, sOldLocation);

					event.dkm = event.moveLocation(sOldLocation, event.dloc);

					// if depth is fixed to top or bottom of seismicityDepthRange then
					// reset the depth to be consistent with the new lat, lon location.
					if (event.fixedDepthIndex == 0 || event.fixedDepthIndex == 1)
						event.setDepth(event.getSeismicityDepthRange());

					// calculate the sum squared weighted residuals at the new location
					new_sswr = event.getSumSqrWeightedResiduals();

					if (event.definingListChanged())
					{
						lsq_convergence_satisfied = 0;
						if (lsq_damping_factor < 0.)
							event.applied_damping = lsq_initial_applied_damping;

					}
				}

				if (event.dkm < lsq_damping_dkm_threshold)
				{
					//cout + "dkm tiny -------------------------------------------------------" + endl;
					//We were unsuccessful in finding a solution with sswr lower than the
					//sswr of the solution we started the iteration with.  This despite the
					//application of sufficient damping to reduce the step size (dkm) to a very small
					//distance.  We can conclude that we are very near the minimum sswr and can
					//declare convergence.  See lsq_algorithm.pdf Section 5.
					event.setLocation(sOldLocation);
					Arrays.fill(event.dloc, 0.);
					event.dkm = 0.;
					new_sswr = old_sswr;
					converged = true;
					//algorithm = "LocOO3D";
					comment = "damped";
				}
			} // End of Levenberg-Marquardt loop.

			converged = TestConvergence(event) || converged; //check for convergence

			old_sswr = event.getSumSqrWeightedResiduals();

			done = (converged && sIterationCount > 1) || sIterationCount >= sMaxIterations;

			// If automatic damping is in effect, and the applied damping factor
			// is greater than the base value, decrease the applied damping factor
			// by the damping multiplier.  This applies the policy that the damping
			// factor should decrease at the conclusion of each iteration, down to
			// some minimum value.
			if (!done && lsq_damping_factor < 0. && event.applied_damping > lsq_initial_applied_damping)
				event.applied_damping /= lsq_applied_damping_multiplier;
			
			event.locationTrack.add(event.getLocation());

		}
		while (!done); // end of main iteration loop

		write_to_out_buf(event, false);

		if (event.getEventParameters().useSimplex())
		{
			double old_rms = event.rmsWeightedResidual();
			
			SolverSimplex simplex = new SolverSimplex();
			event.dkm = simplex.locate(event);
			comment = "simplex";
			write_to_out_buf(event, false);
			
			if (event.dkm > 1 || event.rmsWeightedResidual() < old_rms -0.01)
				   event.logger.writeln(String.format("%nSimplex moved location %5.1f km, rms reduced by %6.4f, orid %12d%n", 
						   event.dkm, old_rms-event.rmsWeightedResidual(), event.getSource().getSourceId()));

		}

		sBaseIteration += sIterationCount;
		sIterationCount = 0;
		
		return true;

	} // END locate

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Perform initialization activities for locate()
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	//
	// *****************************************************************************
	public void initialization(Event event) throws Exception
	{

		N = event.getDefiningVec().size(); //number of defining observations

		M = event.nFree(); // number of free location parameters (0 to 4)

		lsq_condition_number = 0.;
		event.applied_damping = 0.;
		event.lsq_convergence_value = 0.;

		// the following call is going to check that 
		// all predictions are upToDate before calculating sswr.
		old_sswr = event.getSumSqrWeightedResiduals();

		locPar.clear();

		// set the initial applied damping parameter
		if (lsq_damping_factor < 0.)
			event.applied_damping = lsq_initial_applied_damping;

		//number of consecutive iterations that convergence has been satisfied.
		lsq_convergence_satisfied = 0;
		converged = false;

		event.dkm = -999;
		sIterationCount = 0;
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Populate the vector of residuals and the matrix of derivatives.
	//
	// INPUT ARGS:  A, the N x M matrix of derivatives
	//                  r, the N-element vector of weighted residuals
	// OUTPUT ARGS: NONE
	//
	// *****************************************************************************
	boolean populateNRContainers(Event event) throws Exception
	{
		int i, j;

		//save the current depth status
		boolean was_fixed = event.isFixed(GMPGlobals.DEPTH);

		// if this is first iteration, or if depth status has changed, then figure out
		// which parameters we are solving for.
		if (sIterationCount < 1 || locPar.isEmpty() ||
				(event.isFixed(GMPGlobals.DEPTH)) != was_fixed)
		{
			//step through the parameters and see which to solve for.
			locPar.clear();
			for (i = 0; i < 4; i++)
				//if depth is not fixed permanently and not fixed temporarily, then add it
				if (i == GMPGlobals.DEPTH && event.isFree(GMPGlobals.DEPTH))
					locPar.add(i);
				else if (i != GMPGlobals.DEPTH && event.isFree(i))
					locPar.add(i);

			lsq_convergence_satisfied = 0;
			if (lsq_damping_factor < 0.)
				event.applied_damping = lsq_initial_applied_damping;
		}

		// obs.getDefiningVec() returns a const VectorMod<int>& containing a list of the indeces
		// of the defining observations.  This list will include only observations that
		// have valid predictions so that the sum squared weighted residuals for this set
		// of observations will be valid.  During the course of a location computation
		// the list of defining observations will shrink when the predictions associated
		// with defining observations become invalid, or the list may expand when
		// observations which are non-defining only because their predictions are invalid,
		// move to a location where their predictions become valid.

		N = event.getDefiningVec().size(); //number of defining observations
		M = locPar.size(); // the number of free parameters
		r.setSize(N);
		if (A.getRowDimension() != N || A.getColumnDimension() != M)
			A = new Matrix(N, M);
		X.setSize(M);

		//Fill the residual VectorMod and derivative matrix.
		//See lsq_algorithm.pdf equations 2.3 and 2.10.
		boolean ok = true;
		ObservationComponent obs;
		for (i = 0; i < N; i++) //step through defining observations
		{
			obs = event.getDefiningVec().get(i);
			//get the weighted residuals
			r.set(i, obs.getWeightedResidual());
			
			//get the elements of the weighted derivatives matrix
			for (j = 0; j < M; j++)
			{
				A.set(i,j, obs.getWeightedDerivatives()[locPar.get(j)]);
				if (obs.getWeightedDerivatives()[locPar.get(j)] > 1e12)
					ok = false;
				if (Double.isNaN(A.get(i, j)))
				{
					throw new LocOOException("ERROR: Matrix element (" + i + ", " + j +
							") from observation " + obs + " contains a NaN");
				}
			}
		}

		// if the list of defining observations changed (see comment above),
		// then set old_sswr = big which will ensure that the solution cannot
		// converge on this iteration.  Also reset the damping factor to the
		// initial value.
		if (event.definingListChanged())
		{
			lsq_convergence_satisfied = 0;
			if (lsq_damping_factor < 0.)
				event.applied_damping = lsq_initial_applied_damping;
		}

		//    System.out.println("populateNRContainers");
		//    System.out.printf("A=%n%s%n", A.toString());
		//    System.out.printf("R=%n%s%n", r.toString("%20.8f%n"));
		
		return ok;
	}

	/**
	 * Identify the largest singular value (Wmax) and compute lsq_condition_number.
	 * @param Wmax
	 * @return 
	 */
	public double processSingularValues(double[] w)
	{
		double Wmin=w[0], Wmax=w[0];
		for (int i = 1; i < W.length; i++)
		{
			if (W[i] < Wmin)
				Wmin = W[i];
			if (W[i] > Wmax)
				Wmax = W[i];
		}

		//calculate the condition number
		if (Wmin < Wmax * lsq_singular_value_cutoff)
			lsq_condition_number = 1./lsq_singular_value_cutoff;
		else
			lsq_condition_number = Wmax / Wmin;
		
		return Wmax;
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Massage the solution VectorMod.  When X comes back from svdlm, it has length M.
	// Expand it to length 4, filling elements corresponding to fixed parameters to
	// zero.  Units of dloc will be [radians, radians, km, seconds].
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN:      NONE
	//
	// *****************************************************************************
	public void expand_dm(Event event, Location oldLocation)
	{
		// on entry, X is the distance the location should move, in [km, km, km, sec].
		Arrays.fill(event.dloc, 0.);
		for (int i = 0; i < M; i++)
			event.dloc[locPar.get(i)] = X.get(i);

		// event.dkm is approx distance moved, in km.
		event.dkm = sqrt(pow(event.dloc[GMPGlobals.LAT], 2)
				+ pow(event.dloc[GMPGlobals.LON], 2)
				+ pow(event.dloc[GMPGlobals.DEPTH], 2)
				+ pow(event.dloc[GMPGlobals.TIME]*8., 2));

		// convert units of event.dloc to [radians, radians, km, sec]
		event.dloc[GMPGlobals.LAT] /= oldLocation.getRadius();
		event.dloc[GMPGlobals.LON] /= oldLocation.getRadius();

	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Calculate the data resolution matrix.
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN: The N x N data resolution matrix.  See
	//             Menke, W. (1985), Geophysical Data Analysis: Discrete Inverse Theory,
	//             Revised Edition, Academic Press.
	//
	// *****************************************************************************
	public void dataResolutionMatrix(Event event)
	{
		int i, j, k, l;
		double sum;

		if (data_resolution_matrix == null || data_resolution_matrix.length != N)
			data_resolution_matrix = new double[N][N];

		double[] winv = new double[M];
		for (i = 0; i < M; i++)
			if (W[i] < 1e-60)
				winv[i] = 0.;
			else
				winv[i] = 1. / W[i];

		for (i = 0; i < N; i++)
			for (j = 0; j < N; j++)
				for (k = 0; k < M; k++)
				{
					sum = 0;
					for (l = 0; l < M; l++)
						sum += A.get(i, l) * V[l][k];
					data_resolution_matrix[i][j] = sum * winv[k] * U[j][k];
				}
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Calculate the data importance VectorMod.
	//
	// INPUT ARGS:  NONE
	// OUTPUT ARGS: NONE
	// RETURN: The N-element data importance VectorMod.  These are the diagonal elements
	//             of the data resolution matrix.
	//             See Menke, W. (1985), Geophysical Data Analysis: Discrete Inverse
	//             Theory, Revised Edition, Academic Press.
	//
	// *****************************************************************************
	void dataImportance(Event event)
	{
		data_importance.setSize(N);
		double[] I = data_importance.getArray();

		if (lsq_print_data_resolution_matrix)
			for (int i = 0; i < N; i++)
				I[i] = data_resolution_matrix[i][i];
		else
		{
			double sum;

			double[] winv = new double[M];
			for (int i = 0; i < M; i++)
				if (W[i] < 1e-60)
					winv[i] = 0.;
				else
					winv[i] = 1. / W[i];

			for (int i = 0; i < N; i++)
			{
				I[i] = 0.;
				for (int k = 0; k < M; k++)
				{
					sum = 0;
					for (int l = 0; l < M; l++)
						sum += A.get(i, l) * V[l][k];
					I[i] += sum * winv[k] * U[i][k];
				}
			}
		}
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Convergence Test
	//
	// INPUT ARGS:  None
	// OUTPUT ARGS: None
	// RETURN: a booleanean value indicating whether or not convergence has been achieved.
	//
	//   See lsq_algorithm.pdf Section 3.
	//
	// *****************************************************************************
	boolean TestConvergence(Event event)
	{
		event.lsq_convergence_value = abs(new_sswr / old_sswr - 1.0);
		if (event.lsq_convergence_value < lsq_convergence_criterion)
			++lsq_convergence_satisfied;
		else
			lsq_convergence_satisfied = 0;
		return lsq_convergence_satisfied >= lsq_convergence_n;
	}

	/**
	 * Calculate the covariance matrix.  
	 * @param event
	 * @return
	 * @throws Exception
	 */
	protected double[][] calculateCovarianceMatrix(Event event)
		throws Exception
	{
	    if (converged)
	    {
		populateNRContainers(event);

		// Compute the singular value decomposition of matrix A
		// without application of the levenberg-marquardt algorithm
		SingularValueDecomposition svd = new SingularValueDecomposition(A);

		// compute the covariance matrix using equation 6.1 in the locoo sand report
		Matrix w = svd.getS();
		
		double wmax = processSingularValues(svd.getSingularValues());
		
		double wmin = wmax*lsq_singular_value_cutoff; 
		// for all elements of w less than wmin, set the value to wmin
		for (int i=0; i<w.getRowDimension(); ++i)
		    if (w.get(i, i) < wmin)
			w.set(i, i, wmin);

		w = w.inverse();
		
		
		Matrix v = svd.getV();
		double[][] covariance = v.times(w.times(w)).times(v.transpose()).getArray();

		// if any location components were held fixed, expand the covariance matrix
		// to 4 x 4, with elements corresponding to fixed components equal to zero.
		if (covariance.length != 4) {
		    double[][] cov_4x4 = new double[4][4];
		    for (int i=0; i<locPar.size(); ++i)
			for (int j=0; j<locPar.size(); ++j)
			    cov_4x4[locPar.get(i)][locPar.get(j)] = covariance[i][j];
		    return cov_4x4;
		}

		return covariance;				
	    }

	    return null;
	}

	/**
	 * 
	 * @param event
	 * @param isFinalTable true if this is the final table.
	 * @throws Exception
	 */
	public void write_to_out_buf(Event event, boolean isFinalTable) throws Exception
	{
		if (event.logger.getVerbosity() >= 4)
		{	  
			if (n_observation_tables < io_observation_tables-1 || (isFinalTable && io_observation_tables > 0))
			{
				++n_observation_tables;
				
				event.logger.writeln();
				event.logger.writeln("==========================================================================");
				event.logger.writeln(String.format("%nItt=%d It=%d N=%d M=%d %s%n", 
						sBaseIteration+sIterationCount, 
						sIterationCount,
						N, M, 
						event.toString()));
				event.logger.writeln(event.getObsIterationTable(!isFinalTable));
				event.logger.writeln(event.getPredictionTable(!isFinalTable));
			}

			if (io_iteration_table)
				event.submitIterationTableEntry(sBaseIteration+sIterationCount, 
						sIterationCount, comment, N, M);
		}
	}

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Fill a LocatorResults container with the results of the location
	//
	// INPUT ARGS:  a reference to a LocatorResults object to be filled.
	// OUTPUT ARGS: a reference to a filled LocatorResults object.
	// RETURN:      NONE
	//
	// *****************************************************************************
	private LocatorResults getLocatorResults(Event event)  throws Exception
	{
		LocatorResults locatorResults = new LocatorResults(event,
			converged, // Convergence flag

				event.getSource().getEvid(), // Event id  (evid)
				event.getSource().getSourceId(), // Origin id (orid)

				event.getLocation(), // Final Event Location Vector

				//calculateUncertainties(event),
				calculateCovarianceMatrix(event),

				(converged ? event.getSumSqrWeightedResiduals() : -1.),	
				// Sum squared weighted residuals of only defining observations

				(converged ? event.rmsTTResiduals() : -1.),
				// Root mean squared residuals of defining travel time observations

				(converged ? event.sdobs() : -1.),
				// the standard deviation of the weighted residuals.
				// Includes all defining tt, az and sh weighted residuals.

				event.getnSSWR(), 
				// Number of times sum squared weighted residuals were calculated.

				event.countObservations(),
				// Number of observations associated with this event.

				event.countTTObservations(true),
				// Number of time-defining observations

				event.getDefiningVec().size(), // Number of defining observations

				event.countRemovedObservations(), 
				// Number of defining observations converted to non-defining
				// during event location.

				M,

				event.getFixed(), // Whether the solution used a fixed longitude

				(event.isFixed(GMPGlobals.DEPTH) ? "g" : "f"), 
				// who fixed depth? g=geophysicist, r=code, f=free

				sIterationCount + sBaseIteration, // Number of iterations required to achieve solution
				sLocalMinima // booleanean indicating possible existance of local minima.

		);

		return locatorResults;

	} // END recordResults

	// **** _FUNCTION DESCRIPTION_ *************************************************
	//
	// Solve system dm = V.W.(W^2+lambda.I)^(-1).U'.r
	// Basic routine comes from Numerical Recipes in C++, Press, et al., 2002
	// Modified here to implement Levenberg-Marquardt damping.  
	// See lsq_algorithm.pdf Section 5.
	//
	//
	// *****************************************************************************
	void svdlm(double[][] u, double[] w, double[][] v,
			double sv_cutoff, double lambda, ArrayListDouble b, ArrayListDouble x)
	{
		int jj,j,i;
		double s, wj;
		int n = u.length;
		int m = u[0].length;
		double[] tmp = new double[m];
		x.setSize(m);

		for (j=0;j<m;j++)
		{
			s=0.0;
			wj=w[j];
			if (wj > sv_cutoff)
			{
				for (i=0;i<n;i++) s += u[i][j]*b.get(i);
				if (lambda > 0.) s *= wj/(wj*wj+lambda);
				else s /= wj;
			}
			tmp[j]=s;
		}
		for (j=0;j<m;j++)
		{
			s=0.0;
			for (jj=0;jj<m;jj++) s += v[j][jj]*tmp[jj];
			x.set(j, s);
		}  
	}

	private String printMatrix(Matrix x, String format)
	{
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<x.getRowDimension(); ++i)
		{
			for (int j=0; j<x.getColumnDimension(); ++j)
				buf.append(String.format(format, x.get(i, j)));
			buf.append(Globals.NL);
		}
		return buf.toString();
	}

}
