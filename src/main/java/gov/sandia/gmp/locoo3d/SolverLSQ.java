/**
 * Copyright 2009 Sandia Corporation. Under the terms of Contract DE-AC04-94AL85000 with Sandia
 * Corporation, the U.S. Government retains certain rights in this software.
 * 
 * BSD Open Source License.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * 
 * - Neither the name of Sandia National Laboratories nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.sandia.gmp.locoo3d;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.io.File;
import java.util.Arrays;
import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.hyperellipse.HyperEllipse;
import gov.sandia.gmp.baseobjects.observation.ObservationComponent;
import gov.sandia.gmp.locoo3d.Event.LocatorStatus;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.matrix.Matrix;
import gov.sandia.gmp.util.numerical.matrix.SingularValueDecomposition;

/**
 * <p>
 * Title: LocOO3D
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 *
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public class SolverLSQ {
  protected EventParameters eventParameters;

  /**
   * Weighted residuals. (Observed - predicted)/uncertainty. Unitless. Length N. See
   * lsq_algorithm.pdf equation 2.3.
   */
  protected ArrayListDouble r = new ArrayListDouble();

  protected double[][] U, V;
  protected Matrix A = new Matrix(1, 1);
  protected double[] W = new double[4];

  /**
   * The change in event location imposed each iteration.
   */
  protected ArrayListDouble X = new ArrayListDouble(4);

  /**
   * Data resolution matrix. Only calculated and printed when lsq_print_data_resolution_matrix is
   * true.
   */
  protected double[][] data_resolution_matrix;;

  /**
   * Diagonal elements of data resolution matrix.
   */
  protected ArrayListDouble data_importance = new ArrayListDouble();

  /**
   * The indices of the event location parameters that are not held fixed.
   */
  protected ArrayListInt locPar = new ArrayListInt(4);

  /**
   * Number of times in a row that convergence test must be passed for final convergence to be
   * declared.
   */
  protected int lsq_convergence_satisfied;

  /**
   * The sum squared weighted residuals at the start of the current iteration.
   */
  protected double old_sswr;

  /**
   * The sum squared weighted residuals at the end of the current iteration.
   */
  protected double new_sswr;

  /**
   * The ratio of the largest to the smallest singular value.
   */
  protected double lsq_condition_number;

  /**
   * True when all conditions necessary to stop iterating have been achieved.
   */
  protected boolean done;

  /**
   * Iteration Count for the Current locate() Attempt
   */
  protected int sIterationCount;

  public SolverLSQ() throws Exception {
    super();
  }

  /**
   * Solves for the seismic event location given the vector of observations. Upon completion, the
   * results container object should be updated.
   * 
   * @param event
   * @return
   * @throws Exception
   */
  public void locateEvent(Event event) throws Exception {
    eventParameters = event.getEventParameters();

    eventParameters.needDerivatives(true);

    if (eventParameters.computeGriddedResiduals())
      event.source.getLocationTrack().add(event.getLocation());


    event.source.setValid(false);

    event.iterationCount = 0;

    event.resetFlipFlop();

    if (eventParameters.sMaxIterations() == 0)
      eventLocation(event);
    else {
      try {
        // ------------------------------------------------------------------------
        // While the set of defining observations and depth are in a state of flux,
        // continue to relocate the event with the new set. During location, the
        // defining/non-defining status of observations can change if they
        // involve predictions that are extrapolated, either horizontally or
        // vertically, or if they have big residuals.
        // ------------------------------------------------------------------------
        do {
          event.positionUpToDate = false;

          // check if problem is contrained
          event.checkConstraints();

          // locate the event
          eventLocation(event);

          // ------------------------------------------------------------------------
          // Check the set of observations and see if any should be reset to
          // non-defining. Also check to see if depth is out of user-specified range.
          // ------------------------------------------------------------------------

        } while (event.checkBigResiduals() || event.checkDepthConstraints(event.source));
      } catch (Exception e) {
        event.getSource().appendErrorMessage(e.getMessage() + "\n");
        if (event.logger.getVerbosity() >= 0)
          event.logger.writeln(e);
        event.errorlog.writeln(e);
        event.source.setValid(false);
      }
    }
  }


  /**
   * Locate the seismic event and populate a LocatorResults object containing the location and
   * associated uncertainties.
   * 
   * @param event
   * @return
   * @throws Exception
   */
  private void eventLocation(Event event) throws Exception {
    event.N = event.getDefiningVec().size(); // number of defining observations
    event.M = event.source.nFree();

    if (event.M == 0)
      throw new Exception("Cannot relocate event when lat, lon, depth, time are all fixed. \n"
          + "Set lsq_max_iterations = 0 instead.");

    if (eventParameters.sMaxIterations() == 0) {
      initialization(event);
      event.dkm = 0.;
      event.source.setValid(true);
      event.write_to_out_buf(LocatorStatus.FIXED);
    } else
      locate(event);

    double[][] covarianceMatrix = calculateCovarianceMatrix(event);

    HyperEllipse hyper_ellipse = new HyperEllipse(event.getLocation(), covarianceMatrix, event.M,
        event.N, event.sumSqrWeightedResiduals(), event.getEventParameters().getUncertaintyK(),
        event.getEventParameters().getAprioriVariance(),
        event.getEventParameters().getConfidenceLevel());

    event.source.setHyperEllipse(hyper_ellipse);

    String ellipsoidVTK = event.getEventParameters().getEllipsoidVTK();

    if (ellipsoidVTK != null && event.source.isFree(GMPGlobals.DEPTH)
        && event.source.isFree(GMPGlobals.LAT) && event.source.isFree(GMPGlobals.LON)) {
      if (ellipsoidVTK.contains("%d"))
        hyper_ellipse.getEllipsoid().writeVTK(
            new File(String.format(ellipsoidVTK, event.getSourceId())), event.getLocation());
      else
        hyper_ellipse.getEllipsoid().writeVTK(new File(ellipsoidVTK), event.getLocation());
    }

    // event.locatorResults.setResults(event);
    // event.setLocatorResults(event.getLocatorResults());
  }

  /**
   * Locate a single seismic event. Returns true if no errors encountered.
   * 
   * @param event
   * @return
   * @throws Exception
   */
  private void locate(Event event) throws Exception {
    if (event.source.depthFixed()) {
      // if user specified a specific depth in the properties file, this.fixedDepthValue =
      // eventParameters.fixedDepthValue();
      // set the event depth to that value. Otherwise, if event.fixedDepthIndex is
      // set to 0 or 1, then set depth to either top or bottom of the
      // seismicityDepthRange.
      if (!Double.isNaN(event.fixedDepthValue))
        event.setDepth(event.fixedDepthValue);
      else if (event.fixedDepthIndex == 0 || event.fixedDepthIndex == 1)
        event.setDepth(event.getSeismicityDepthRange());
    }

    LocatorStatus status = LocatorStatus.START;

    initialization(event);

    do // this is the start of the main location iteration loop
    {
      ++sIterationCount; // increment the iteration counter

      ++event.iterationCount;

      // Save a deep copy of the working location at the start of the iteration.
      Location sOldLocation = event.getLocation();

      // populate the local VectorMods and matrices for the lsq solver
      // (weighted residuals in R, and weighted derivatives in A).
      if (!populateNRContainers(event))
        throw new LocOOException("ERROR: A matrix contains huge values.");

      // cannot locate if the number of free parameters is > the number of defining observations.
      if (event.M > event.N)
        throw new LocOOException(String.format(
            "Cannotlocate event because number of observation %d is less than number of location parameters %d",
            event.N, event.M));

      SingularValueDecomposition svd = new SingularValueDecomposition(A);

      U = svd.getU().getArray();
      V = svd.getV().getArray();
      W = svd.getSingularValues();

      // find Wmax, calculate the condition number.
      double Wmax = processSingularValues(W);

      // Solve A' * A * dm = A' * r for dm.
      svdlm(U, W, V, eventParameters.lsq_singular_value_cutoff() * Wmax, event.applied_damping, r,
          X);

      // Expand the new solution vector to full size (4 elements).
      // Event location components that are held fixed will be zero.
      // Units of dm will be [radians, radians, km, seconds].
      // Then add the change in location to the previous location.
      // dkm will be the amount that the location moved, in km.
      expand_dm(event, sOldLocation);

      event.write_to_out_buf(status);
      status = LocatorStatus.INTERIM;

      event.dkm = event.moveLocation(sOldLocation, event.dloc);

      // if depth is fixed to top or bottom of seismicityDepthRange then
      // reset the depth to be consistent with the new lat, lon location.
      if (event.fixedDepthIndex == 0 || event.fixedDepthIndex == 1)
        event.setDepth(event.getSeismicityDepthRange());

      // calculate the sum squared weighted residuals at the new location
      new_sswr = event.sumSqrWeightedResiduals();

      if (event.definingListChanged()) {
        lsq_convergence_satisfied = 0;
        if (eventParameters.lsq_damping_factor() < 0.)
          event.applied_damping = eventParameters.lsq_initial_applied_damping();
      }

      // Levenberg-Marquardt loop. Perform the following if automatic damping is in effect.
      if (eventParameters.lsq_damping_factor() < 0.) {
        // if the sum squared weighted residuals has increased, then increase the
        // amount of damping and relocate starting from the location at the begining
        // of this iteration. Don't do this if the change in location is insignificant.
        while (new_sswr >= old_sswr && event.dkm >= eventParameters.lsq_damping_dkm_threshold()) {
          // increase the applied damping
          event.applied_damping *= eventParameters.lsq_applied_damping_multiplier();

          // Solve A' * A * dm = A' * r for dm.
          svdlm(U, W, V, eventParameters.lsq_singular_value_cutoff() * Wmax, event.applied_damping,
              r, X);

          // Add the change in location to the location that was valid at the begining
          // of the iteration.
          expand_dm(event, sOldLocation);

          event.dkm = event.moveLocation(sOldLocation, event.dloc);

          // if depth is fixed to top or bottom of seismicityDepthRange then
          // reset the depth to be consistent with the new lat, lon location.
          if (event.fixedDepthIndex == 0 || event.fixedDepthIndex == 1)
            event.setDepth(event.getSeismicityDepthRange());

          // calculate the sum squared weighted residuals at the new location
          new_sswr = event.sumSqrWeightedResiduals();

          if (event.definingListChanged()) {
            lsq_convergence_satisfied = 0;
            if (eventParameters.lsq_damping_factor() < 0.)
              event.applied_damping = eventParameters.lsq_initial_applied_damping();

          }
        }

        if (event.dkm < eventParameters.lsq_damping_dkm_threshold()) {
          // cout + "dkm tiny -------------------------------------------------------" + endl;
          // We were unsuccessful in finding a solution with sswr lower than the
          // sswr of the solution we started the iteration with. This despite the
          // application of sufficient damping to reduce the step size (dkm) to a very small
          // distance. We can conclude that we are very near the minimum sswr and can
          // declare convergence. See lsq_algorithm.pdf Section 5.
          event.setLocation(sOldLocation);
          Arrays.fill(event.dloc, 0.);
          event.dkm = 0.;
          new_sswr = old_sswr;
          event.source.setValid(true);
          status = LocatorStatus.DAMPED;
        }
      } // End of Levenberg-Marquardt loop.

      // converged = TestConvergence(event) || converged; //check for convergence
      event.source.setValid(TestConvergence(event) || event.source.isValid()); // check for
                                                                               // convergence

      old_sswr = event.sumSqrWeightedResiduals();

      done = (event.source.isValid() && sIterationCount > 1)
          || sIterationCount >= eventParameters.sMaxIterations();

      // If automatic damping is in effect, and the applied damping factor
      // is greater than the base value, decrease the applied damping factor
      // by the damping multiplier. This applies the policy that the damping
      // factor should decrease at the conclusion of each iteration, down to
      // some minimum value.
      if (!done && eventParameters.lsq_damping_factor() < 0.
          && event.applied_damping > eventParameters.lsq_initial_applied_damping())
        event.applied_damping /= eventParameters.lsq_applied_damping_multiplier();

      if (eventParameters.computeGriddedResiduals())
        event.source.getLocationTrack().add(event.getLocation());

    } while (!done); // end of main iteration loop

    event.write_to_out_buf(LocatorStatus.END);

    if (event.getEventParameters().useSimplex()) {
      Location old_location = (Location) event.getLocation().clone();
      double old_rms = event.rmsWeightedResidual();

      SolverSimplex simplex = new SolverSimplex();
      event.dkm = simplex.locate(event);
      event.write_to_out_buf(LocatorStatus.SIMPLEX);

      if (event.dkm > 1 || event.rmsWeightedResidual() < old_rms - 0.01) {
        double delta = old_location.distance(event.getLocation()) * old_location.getRadius();
        double azimuth = old_location.azimuthDegrees(event.getLocation(), Globals.NA_VALUE);
        // double depth_diff = event.getLocation().getDepth()-old_location.getDepth();
        event.logger.writeln(String.format(
            "%nSimplex moved location %5.1f km in direction %1.3f, "
                + "depth changed from %1.3f km to %1.3f, rms reduced by %6.4f%n",
            delta, azimuth, old_location.getDepth(), event.getLocation().getDepth(),
            old_rms - event.rmsWeightedResidual()));
      }
    }
  }

  /**
   * Perform initialization activities for locate()
   * 
   * @param event
   * @throws Exception
   */
  public void initialization(Event event) throws Exception {

    event.N = event.getDefiningVec().size(); // number of defining observations

    event.M = event.source.nFree(); // number of free location parameters (0 to 4)

    lsq_condition_number = 0.;
    event.applied_damping = 0.;
    event.lsq_convergence_value = 0.;

    // the following call is going to check that
    // all predictions are upToDate before calculating sswr.
    old_sswr = event.sumSqrWeightedResiduals();

    locPar.clear();

    // set the initial applied damping parameter
    if (eventParameters.lsq_damping_factor() < 0.)
      event.applied_damping = eventParameters.lsq_initial_applied_damping();

    // number of consecutive iterations that convergence has been satisfied.
    lsq_convergence_satisfied = 0;
    event.source.setValid(false);

    event.dkm = -999;
    sIterationCount = 0;
  }

  /**
   * Perform initialization activities for locate()
   * 
   * @param event
   * @return
   * @throws Exception
   */
  private boolean populateNRContainers(Event event) throws Exception {
    int i, j;

    // save the current depth status
    boolean was_fixed = event.source.isFixed(GMPGlobals.DEPTH);

    // if this is first iteration, or if depth status has changed, then figure out
    // which parameters we are solving for.
    if (sIterationCount < 1 || locPar.isEmpty()
        || (event.source.isFixed(GMPGlobals.DEPTH)) != was_fixed) {
      // step through the parameters and see which to solve for.
      locPar.clear();
      for (i = 0; i < 4; i++)
        // if depth is not fixed permanently and not fixed temporarily, then add it
        if (i == GMPGlobals.DEPTH && event.source.isFree(GMPGlobals.DEPTH))
          locPar.add(i);
        else if (i != GMPGlobals.DEPTH && event.source.isFree(i))
          locPar.add(i);

      lsq_convergence_satisfied = 0;
      if (eventParameters.lsq_damping_factor() < 0.)
        event.applied_damping = eventParameters.lsq_initial_applied_damping();
    }

    // obs.getDefiningVec() returns a const VectorMod<int>& containing a list of the indeces
    // of the defining observations. This list will include only observations that
    // have valid predictions so that the sum squared weighted residuals for this set
    // of observations will be valid. During the course of a location computation
    // the list of defining observations will shrink when the predictions associated
    // with defining observations become invalid, or the list may expand when
    // observations which are non-defining only because their predictions are invalid,
    // move to a location where their predictions become valid.

    event.N = event.getDefiningVec().size(); // number of defining observations
    event.M = locPar.size(); // the number of free parameters
    r.setSize(event.N);
    if (A.getRowDimension() != event.N || A.getColumnDimension() != event.M)
      A = new Matrix(event.N, event.M);
    X.setSize(event.M);

    // Fill the residual VectorMod and derivative matrix.
    // See lsq_algorithm.pdf equations 2.3 and 2.10.
    boolean ok = true;
    ObservationComponent obs;
    for (i = 0; i < event.N; i++) // step through defining observations
    {
      obs = event.getDefiningVec().get(i);
      // get the weighted residuals
      r.set(i, obs.getWeightedResidual());

      // get the elements of the weighted derivatives matrix
      for (j = 0; j < event.M; j++) {
        A.set(i, j, obs.getWeightedDerivatives()[locPar.get(j)]);
        if (obs.getWeightedDerivatives()[locPar.get(j)] > 1e12)
          ok = false;
        if (Double.isNaN(A.get(i, j))) {
          throw new LocOOException("ERROR: Matrix element (" + i + ", " + j + ") from observation "
              + obs + " contains a NaN");
        }
      }
    }

    // if the list of defining observations changed (see comment above),
    // then set old_sswr = big which will ensure that the solution cannot
    // converge on this iteration. Also reset the damping factor to the
    // initial value.
    if (event.definingListChanged()) {
      lsq_convergence_satisfied = 0;
      if (eventParameters.lsq_damping_factor() < 0.)
        event.applied_damping = eventParameters.lsq_initial_applied_damping();
    }

    // System.out.println("populateNRContainers");
    // System.out.printf("A=%n%s%n", A.toString());
    // System.out.printf("R=%n%s%n", r.toString("%20.8f%n"));

    return ok;
  }

  /**
   * Identify the largest singular value (Wmax) and compute lsq_condition_number.
   * 
   * @param Wmax
   * @return
   */
  public double processSingularValues(double[] w) {
    double Wmin = w[0], Wmax = w[0];
    for (int i = 1; i < W.length; i++) {
      if (W[i] < Wmin)
        Wmin = W[i];
      if (W[i] > Wmax)
        Wmax = W[i];
    }

    // calculate the condition number
    if (Wmin < Wmax * eventParameters.lsq_singular_value_cutoff())
      lsq_condition_number = 1. / eventParameters.lsq_singular_value_cutoff();
    else
      lsq_condition_number = Wmax / Wmin;

    return Wmax;
  }

  /**
   * Massage the solution VectorMod. When X comes back from svdlm, it has length M. // Expand it to
   * length 4, filling elements corresponding to fixed parameters to // zero. Units of dloc will be
   * [radians, radians, km, seconds].
   * 
   * @param event
   * @param oldLocation
   */
  public void expand_dm(Event event, Location oldLocation) {
    // on entry, X is the distance the location should move, in [km, km, km, sec].
    Arrays.fill(event.dloc, 0.);
    for (int i = 0; i < event.M; i++)
      event.dloc[locPar.get(i)] = X.get(i);

    // event.dkm is approx distance moved, in km.
    event.dkm = sqrt(pow(event.dloc[GMPGlobals.LAT], 2) + pow(event.dloc[GMPGlobals.LON], 2)
        + pow(event.dloc[GMPGlobals.DEPTH], 2) + pow(event.dloc[GMPGlobals.TIME] * 8., 2));

    // convert units of event.dloc to [radians, radians, km, sec]
    event.dloc[GMPGlobals.LAT] /= oldLocation.getRadius();
    event.dloc[GMPGlobals.LON] /= oldLocation.getRadius();

  }

  /**
   * Calculate the data resolution matrix.
   * <p>
   * See Menke, W. (1985), Geophysical Data Analysis: Discrete Inverse Theory, Revised Edition,
   * Academic Press.
   * 
   * @param event
   */
  public void dataResolutionMatrix(Event event) {
    int i, j, k, l;
    double sum;

    if (data_resolution_matrix == null || data_resolution_matrix.length != event.N)
      data_resolution_matrix = new double[event.N][event.N];

    double[] winv = new double[event.M];
    for (i = 0; i < event.M; i++)
      if (W[i] < 1e-60)
        winv[i] = 0.;
      else
        winv[i] = 1. / W[i];

    for (i = 0; i < event.N; i++)
      for (j = 0; j < event.N; j++)
        for (k = 0; k < event.M; k++) {
          sum = 0;
          for (l = 0; l < event.M; l++)
            sum += A.get(i, l) * V[l][k];
          data_resolution_matrix[i][j] = sum * winv[k] * U[j][k];
        }
  }

  /**
   * Calculate the data importance VectorMod.
   * <p>
   * The N-element data importance VectorMod. These are the diagonal elements of the data resolution
   * matrix. See Menke, W. (1985), Geophysical Data Analysis: Discrete Inverse Theory, Revised
   * Edition, Academic Press.
   * 
   * @param event
   */
  public void dataImportance(Event event) {
    data_importance.setSize(event.N);
    double[] I = data_importance.getArray();

    if (eventParameters.lsq_print_data_resolution_matrix())
      for (int i = 0; i < event.N; i++)
        I[i] = data_resolution_matrix[i][i];
    else {
      double sum;

      double[] winv = new double[event.M];
      for (int i = 0; i < event.M; i++)
        if (W[i] < 1e-60)
          winv[i] = 0.;
        else
          winv[i] = 1. / W[i];

      for (int i = 0; i < event.N; i++) {
        I[i] = 0.;
        for (int k = 0; k < event.M; k++) {
          sum = 0;
          for (int l = 0; l < event.M; l++)
            sum += A.get(i, l) * V[l][k];
          I[i] += sum * winv[k] * U[i][k];
        }
      }
    }
  }

  /**
   * See lsq_algorithm.pdf Section 3.
   * 
   * @param event
   * @return
   */
  boolean TestConvergence(Event event) {
    event.lsq_convergence_value = abs(new_sswr / old_sswr - 1.0);
    if (event.lsq_convergence_value < eventParameters.lsq_convergence_criterion())
      ++lsq_convergence_satisfied;
    else
      lsq_convergence_satisfied = 0;
    return lsq_convergence_satisfied >= eventParameters.lsq_convergence_n();
  }

  /**
   * Calculate the covariance matrix.
   * 
   * @param event
   * @return
   * @throws Exception
   */
  protected double[][] calculateCovarianceMatrix(Event event) throws Exception {
    if (event.source.isValid()) {
      populateNRContainers(event);

      if (A.getColumnDimension() == 0 || A.getRowDimension() == 0) {
        throw new Exception("A matrix is empty.");
      }

      // Compute the singular value decomposition of matrix A
      // without application of the levenberg-marquardt algorithm
      SingularValueDecomposition svd = new SingularValueDecomposition(A);

      // compute the covariance matrix using equation 6.1 in the locoo sand report
      Matrix w = svd.getS();

      double wmax = processSingularValues(svd.getSingularValues());

      double wmin = wmax * eventParameters.lsq_singular_value_cutoff();
      // for all elements of w less than wmin, set the value to wmin
      for (int i = 0; i < w.getRowDimension(); ++i)
        if (w.get(i, i) < wmin)
          w.set(i, i, wmin);

      w = w.inverse();


      Matrix v = svd.getV();
      double[][] covariance = v.times(w.times(w)).times(v.transpose()).getArray();

      // if any location components were held fixed, expand the covariance matrix
      // to 4 x 4, with elements corresponding to fixed components equal to zero.
      if (covariance.length != 4) {
        double[][] cov_4x4 = new double[4][4];
        for (int i = 0; i < locPar.size(); ++i)
          for (int j = 0; j < locPar.size(); ++j)
            cov_4x4[locPar.get(i)][locPar.get(j)] = covariance[i][j];
        return cov_4x4;
      }

      return covariance;
    }

    return null;
  }

  /**
   * Solve system dm = V.W.(W^2+lambda.I)^(-1).U'.r
   * <p>
   * Basic routine comes from Numerical Recipes in C++, Press, et al., 2002
   * <p>
   * Modified here to implement Levenberg-Marquardt damping. See lsq_algorithm.pdf Section 5.ß
   * 
   * @param u
   * @param w
   * @param v
   * @param sv_cutoff
   * @param lambda
   * @param b
   * @param x
   */
  void svdlm(double[][] u, double[] w, double[][] v, double sv_cutoff, double lambda,
      ArrayListDouble b, ArrayListDouble x) {
    int jj, j, i;
    double s, wj;
    int n = u.length;
    int m = u[0].length;
    double[] tmp = new double[m];
    x.setSize(m);

    for (j = 0; j < m; j++) {
      s = 0.0;
      wj = w[j];
      if (wj > sv_cutoff) {
        for (i = 0; i < n; i++)
          s += u[i][j] * b.get(i);
        if (lambda > 0.)
          s *= wj / (wj * wj + lambda);
        else
          s /= wj;
      }
      tmp[j] = s;
    }
    for (j = 0; j < m; j++) {
      s = 0.0;
      for (jj = 0; jj < m; jj++)
        s += v[j][jj] * tmp[jj];
      x.set(j, s);
    }
  }

  @SuppressWarnings("unused")
  private String printMatrix(Matrix x, String format) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < x.getRowDimension(); ++i) {
      for (int j = 0; j < x.getColumnDimension(); ++j)
        buf.append(String.format(format, x.get(i, j)));
      buf.append(Globals.NL);
    }
    return buf.toString();
  }

}
