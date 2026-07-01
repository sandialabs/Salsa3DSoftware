/**
 * Copyright 2026 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the
 * terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this
 * software.
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

import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.DEPTH;
import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.LAT;
import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.LON;
import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.TIME;
import static gov.sandia.gmp.util.globals.Globals.sqr;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.log10;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.flinnengdahl.FlinnEngdahlCodes;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.baseobjects.observation.ObservationComponent;
import gov.sandia.gmp.locoo3d.EventList.CorrelationMethod;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.brents.Brents;
import gov.sandia.gmp.util.numerical.brents.BrentsFunction;
import gov.sandia.gmp.util.numerical.matrix.CholeskyDecomposition;
import gov.sandia.gmp.util.numerical.matrix.LUDecomposition;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.GeoMath;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origerr;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AzgapExtended;

/**
 * <p>
 * Title: LocOO
 * </p>
 * 
 * <p>
 * Description: Seismic Event Locator
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
@SuppressWarnings("serial")
public class Event implements BrentsFunction, Serializable, Cloneable {
  Source source;

  private EventParameters eventParameters;

  /**
   * logger and errorlog are shared among all events in a locooTask
   */
  protected ScreenWriterOutput logger, errorlog;

  /**
   * Used to store info about observations whose defining status is changed at the start of a
   * location calculation so it can be output right after the observation table.
   */
  private StringBuffer observationStatus = new StringBuffer();

  /**
   * inputLocation is the original location specified by the calling application.
   */
  private Location inputLocation;

  /**
   * initialLocation is the location that is to be used as the starting location for the
   * calculation.
   */
  private Location initialLocation;

  /**
   * the set of all observationComponents that are defining and have valid predictions.
   */
  private ArrayList<ObservationComponent> definingVec = new ArrayList<ObservationComponent>();

  protected double[] dloc = new double[4];

  protected double dkm;

  private StringBuffer iterationTable;

  /**
   * true if the list of defining observations (definingVec) changed the last time update was
   * called.
   */
  private boolean definingChanged;

  /**
   * gets set to false when the event location upon which predictions are based is moved. Set to
   * true in the update() method, where values that depend on the predictions are recalculated.
   */
  protected boolean positionUpToDate;

  /**
   * gets set to false when the origin time upon which time residuals are based is moved. Set to
   * true in the update() method, where values that depend on the predictions are recalculated.
   */
  private boolean originTimeUpToDate;

  /**
   * The amount of damping actually being applied during each iteration. This is lambda in
   * lsq_algorith.pdf Section 5.
   */
  protected double applied_damping;

  /**
   * The convergence value in the current iteration. This is abs(new_sswr/old_sswr - 1).
   */
  protected double lsq_convergence_value;

  /**
   * number of times sum squared weighted residuals are computed
   */
  private int nSSWR = 0;

  /**
   * Amount of time in nanoseconds spent computing predictions in method update
   */
  private long predictionTime;

  /**
   * If user specified property gen_fix_depth = floating point number fixedDepthValue is the number.
   * Otherwise NaN.
   */
  protected double fixedDepthValue;

  /**
   * If user specified property gen_fix_depth = topo, then fixedDepthIndex will be zero. If during a
   * free depth calculation depth is out of range, then fixedDepthIndex will be set to 0 if
   * event.depth < minDepth constraint, or set to 1 it event.depth > maxDepth constraint.
   */
  protected int fixedDepthIndex;

  private double depthConstraintUncertaintyScale;

  private double depthConstraintUncertaintyOffset;

  private CorrelationMethod correlationMethod;

  /**
   * Correlations specifies the correlation coefficient between two observations. Each String is
   * composed of station name/phase/attribute where attribute is one of [ TT, AZ, SH ]. An example
   * of an entry in this map would be: <br>
   * ASAR/Pg/TT -> WRA/Pg/TT -> 0.5 <br>
   * Coefficient values must be in the range [ -1 to 1 ]
   */
  private Map<String, Map<String, Double>> correlationCoefficients;

  /**
   * Used to process correlated observations
   */
  private double[][] sigma;

  /**
   * Number of defining observation components used to constrain the solution.
   */
  int N;

  /**
   * The number of free event location parameters in the inversion. Typically 4 or 3 for fixed depth
   * solutions.
   */
  int M;

  int iterationCount;

  /**
   * 
   * @param params
   * @param phasePredictorMap
   * @param source Origin
   * @param masterEventCorrections
   * @throws Exception
   * @throws LocOOException
   */
  public Event(EventParameters params, Source source) throws Exception {
    this.source = source;

    this.eventParameters = params;

    this.source.setAlgorithm(eventParameters.getAlgorithm());
    this.source.setAuthor(eventParameters.getAuthor());

    this.iterationTable = new StringBuffer();

    this.inputLocation = source.getLocation();

    if (eventParameters.initialLocationMethod().startsWith("properties"))
      initialLocation = eventParameters.parFileLocation();
    else if (eventParameters.initialLocationMethod().startsWith("data"))
      initialLocation = (Location) inputLocation.clone();

    if (eventParameters.initialLocationOffset() != null) {
      // offset[0] is azimuth in degrees, 1 is distance in degrees, 2 is depth offset in km
      // and 3 is time offset in seconds
      double[] offset = eventParameters.initialLocationOffset();
      double depth = source.getDepth();
      GeoVector moved = source.move(Math.toRadians(offset[0]), Math.toRadians(offset[1]));
      moved.setDepth(depth + offset[2]);
      initialLocation = new Location(moved, source.getTime() + offset[3]);
    }

    source.setLocation(initialLocation);

    this.logger = eventParameters.outputLog();
    this.errorlog = eventParameters.errorLog();

    if (source.getFixed() == null) {
      // get values of fixed from the properties file
      source.setFixed(eventParameters.fixed());
    }

    if (source.getCorrelationCoefficients() != null) {
      correlationMethod = CorrelationMethod.SOURCE;
      setCorrelationCoefficients(source.getCorrelationCoefficients());
    } else if (eventParameters.correlationMethod() != CorrelationMethod.UNCORRELATED) {
      setCorrelationCoefficients(eventParameters.correlations());
      correlationMethod = eventParameters.correlationMethod();
    } else
      correlationMethod = CorrelationMethod.UNCORRELATED;

    this.fixedDepthValue = eventParameters.fixedDepthValue();
    this.fixedDepthIndex = eventParameters.fixedDepthIndex();
    this.depthConstraintUncertaintyScale = eventParameters.depthConstraintUncertaintyScale();
    this.depthConstraintUncertaintyOffset = eventParameters.depthConstraintUncertaintyOffset();

    // if either lat or lon is fixed, make sure that both are fixed.
    if (source.getFixed()[LAT] || source.getFixed()[LON])
      source.getFixed()[LAT] = source.getFixed()[LON] = true;


    positionUpToDate = false;
    originTimeUpToDate = false;
    definingChanged = true;

    source.needDerivatives(eventParameters.needDerivatives());
    source.useTTModelUncertainty(eventParameters.useTTModelUncertainty());
    source.useAzModelUncertainty(eventParameters.useAzModelUncertainty());
    source.useShModelUncertainty(eventParameters.useShModelUncertainty());
    source.useTTPathCorrections(eventParameters.useTTPathCorrections());
    source.useAzPathCorrections(eventParameters.useAzPathCorrections());
    source.useShPathCorrections(eventParameters.useShPathCorrections());

    populateObservationComponents();
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    Event e = (Event) super.clone();
    // clone the source, which will also clone all the Observations and ObservationComponents.
    e.source = (Source) source.clone();
    // check the observations and populate the new event's obsComponents array.
    e.populateObservationComponents();
    e.dloc = new double[4];
    return e;
  }

  public void locate() throws Exception {

    long timer = System.currentTimeMillis();

    if (logger.getVerbosity() >= 3) {
      logger.writeln();
      logger.writeln("==========================================================================");
      logger.writeln();
      logger.writeln(getInputLocationString());
      logger.writeln(getSiteTable());
      logger.writeln(getObservationTable());
      logger.writeln(getCorrelationMatrixString(source.getObsComponents()));
      logger.writeln();
    }

    // TODO
    SolverLSQ solver = new SolverLSQ();

    ArrayList<Source> sourceList = new ArrayList<>();

    solver.locateEvent(this);

    sourceList.add(source);

    Event bestEvent = this;

    if (eventParameters.gen_search_for_local_minima() && eventParameters.sMaxIterations() > 0
        && source.isFree(LAT) && source.isFree(LON) && source.isFixed(DEPTH)
        && source.getNSta(true) >= 3
        && source.getNSta() <= eventParameters.local_minima_nsta_threshold()
        && source.getRMSWeightedResiduals() > 1e-6
        && fractionalContent(EnumSet.of(SeismicPhase.I, SeismicPhase.H, SeismicPhase.T)) == 0.) {

      Observation closest_observation = source.findClosestObservation();

      if (closest_observation != null && closest_observation.getDistanceDegrees() <= eventParameters
          .local_minima_closest_station_threshold()) {

        Event event1 = (Event) this.clone();

        // consider a great circle from source to closest receiver. Move along that great circle
        // the source receiver separation plus one degree, starting from the source location and
        // moving past the receiver location,
        double[] u = GeoMath.move(source.getLocation().getUnitVector(),
            closest_observation.getDistance() + toRadians(1.01), closest_observation.getEsaz());

        // set the location of the current event to the new location. (depth = original source
        // depth)
        event1.setLocation(
            new Location(u, GeoMath.getEarthRadius(u) - source.getLocation().getDepth(),
                source.getLocation().getTime()));

        // solve for a new event location, starting from the new starting location.
        solver.locateEvent(event1);

        Source source1 = event1.source;

        if (source1.isValid()) {

          sourceList.add(source1);

          double scaled_separation = source.getEllipse().scaledSeparation(source1.getEllipse());

          if (scaled_separation >= 0.1) {
            source.hasLocalMinima(true);
            source1.hasLocalMinima(true);
          }

          // orid separation degrees scaled separation src-rcv distance azgap1 azgap3 nFree nobs0
          // nobs1 naz nslo nsta0 nsta1 rmswr0 rmswr1 rmswr1/rmswr0
          if (logger.getVerbosity() == -68 // && scaled_separation >= 0.1
              && source1.getNobs() >= source.getNobs()) {

            // treat the location of the closest observation like an event and compute azgap3.
            // this will be a measure of how closely the defining stations are aligned on a great
            // circle.
            // small values (close to zero) and large values (close to 180) will indicate alignment.
            AzgapExtended azg =
                new AzgapExtended(-1L, closest_observation.getReceiver().getUnitVector(),
                    source.getReceivers(true).values());

            logger.writef(
                "_local_minima\t%10d\t%10.4f\t%10.4f\t%10.4f\t%10.4f\t%10.4f\t%5d\t%3d\t%3d\t%3d\t%3d\t%3d\t%3d\t%8.4f\t%8.4f\t%8.3f%n",
                getSourceId(), source.getLocation().distanceDegrees(source1.getLocation()),
                scaled_separation, closest_observation.getDistanceDegrees(),
                source.getAzgap().getAzgap1(), azg.getAzgap3(), source.nFree(), source.getNobs(),
                source1.getNobs(), source.getNaz(), source.getNslo(), source.getNSta(true),
                source1.getNSta(true), source.getRMSWeightedResiduals(),
                source1.getRMSWeightedResiduals(),
                source1.getRMSWeightedResiduals() / source.getRMSWeightedResiduals());
          }

          // if result1 is "better" than result0
          if (source1.getRMSWeightedResiduals() / source.getRMSWeightedResiduals() < .999
              && source1.getNobs() >= source.getNobs())
            bestEvent = event1;
        }
      }

    }

    bestEvent.updateAll();
    bestEvent.write_to_out_buf(LocatorStatus.FINAL);

    if (logger.getVerbosity() >= 3)
      logger.writeln(iterationTable);

    if (logger.getVerbosity() >= 2)
      logger.writeln(new LocatorResults(bestEvent));

    this.source = bestEvent.source;

    this.source.getAzgap();

    timer = System.currentTimeMillis() - timer;
    source.setCalculationTime(timer * 1e-3);

    if (logger.getVerbosity() >= 2)
      logger
          .write(String.format("Time to compute this location = %1.6f seconds%n%n", timer * 1e-3));

    griddedResiduals(sourceList);
  }

  /**
   * Write an observation+prediction tables directly to the logger and write iteration information
   * to the iterationTable StringBuffer for output to the logger later.
   * 
   * @param current status of the location calculation. one of FIRST, INTERIM, LAST, FIXED, SIMPLEX,
   *        FINAL, DAMPED
   * @throws Exception
   */
  public void write_to_out_buf(LocatorStatus status) throws Exception {
    if (logger.getVerbosity() >= 4) {
      boolean write = false;
      int nTables = eventParameters.io_observation_tables();
      switch (status) {
        case END:
          write = nTables >= 1;
          break;
        case START:
          write = nTables >= 2;
          break;
        case INTERIM:
          write = nTables >= 3;
          break;
        case FIXED:
          write = nTables >= 1;
          break;
        case SIMPLEX:
          write = nTables >= 1;
          break;
        case FINAL:
          write = nTables >= 1;
          break;
        case DAMPED:
          write = nTables >= 1;
          break;
        default:
          break;
      }

      if (write) {
        logger.writeln();
        logger
            .writeln("==========================================================================");
        logger.writeln(String.format("%nIt=%d %-7s N=%d M=%d %s%n", iterationCount,
            status.name().toLowerCase(), N, M, toString()));

        logger.writeln(getObsIterationTable(status != LocatorStatus.FINAL));
        logger.writeln(getPredictionTable(status != LocatorStatus.FINAL));
      }
    }

    if (eventParameters.io_iteration_table() && logger.getVerbosity() >= 3) {
      Location newloc = getLocation();
      newloc = newloc.move(dloc);

      if (iterationTable.length() == 0)
        iterationTable.append(String.format(
            "Iteration Table:%n%n  It  Comment    N   M     Lat      Lon        Depth      Time  rms_Trsd  rms_Wrsd    "
                + "dNorth     dEast       dZ        dT       dkm    dxStart   dzStart   dtStart   azStart   nF damp  converge%n"));

      iterationTable.append(String.format(
          "%4d  %-7s %4d %3d %9.4f %9.4f %9.3f %9.3f %9.4f %9.4f %9.3f %9.3f %9.3f %9.3f %9.4f %9.4f %9.4f %9.4f %9.4f %4d %4d %9.2e%n",
          iterationCount, status.name().toLowerCase(), N, M, source.getLatDegrees(),
          source.getLonDegrees(), source.getDepth(),
          source.getTime() - getInitialLocation().getTime(), rmsTTResiduals(),
          rmsWeightedResidual(), dloc[LAT] * source.getRadius(), dloc[LON] * source.getRadius(),
          dloc[DEPTH], dloc[TIME], dkm,
          getInitialLocation().distance(newloc) * getInitialLocation().getEarthRadius(),
          newloc.getDepth() - getInitialLocation().getDepth(),
          newloc.getTime() - getInitialLocation().getTime(),
          getInitialLocation().azimuthDegrees(newloc, Double.NaN), nSSWR,
          (int) round(log10(applied_damping)), lsq_convergence_value));
    }

  }

  public Location getInitialLocation() {
    return initialLocation;
  }

  /**
   * Checks each defining observation to see if it is to be filtered out due to settings in the
   * properties file. Relevant properties are definingAttributes, definingPhases, definingStations
   * and definingObservationFilter.
   * <p>
   * An ObservationComponent will be set non-defining if the observed value OR the observed
   * uncertainty are equal to NA_VALUE.
   * 
   * @throws Exception
   */
  private void populateObservationComponents() {
    try {
      source.getObsComponents().clear();
      for (Observation obs : source.getObservations().values()) {
        if (obs.getReceiver() == null)
          errorlog.writeln(String.format(
              "%nIgnoring observation that has no receiver associated with it. "
                  + "orid %d, evid %d, arid %d, receiverId %d%n",
              source.getSourceId(), source.getEvid(), obs.getObservationId(),
              obs.getReceiver().getReceiverId()));
        else if (obs.getPhase() == SeismicPhase.NULL)
          errorlog.writeln(String.format(
              "%nIgnoring observation with unrecognized phase %s. "
                  + "orid %d, evid %d, arid %d, receiverId %d%n",
              obs.getPhase().toString(), source.getSourceId(), source.getEvid(),
              obs.getObservationId(), obs.getReceiver().getReceiverId()));
        else {
          Predictor predictor = eventParameters.predictorFactory().getPredictor(obs.getPhase());
          if (predictor != null) {
            obs.setPredictorName(predictor.getPredictorName());

            LibCorr3DModel model = predictor.getLibcorr3d().getModel(obs.getReceiver(),
                obs.getPhase().toString(), "TT");
            if (model != null)
              obs.setModelName(model.getVmodel());
            else
              obs.setModelName(predictor.getModelName());
          }
          source.getObsComponents().addAll(obs.getObservationComponents().values());
        }
      }

      observationStatus.setLength(0);

      // check each ObservationComponent to see if its defining status needs to change
      // due to parameters specified in par file.
      for (ObservationComponent obsComponent : source.getObsComponents()) {
        if (!eventParameters.definingAttributes().contains(obsComponent.getObsType())
            || !eventParameters.definingPhases().contains(obsComponent.getPhase())
            || (!eventParameters.definingStations().isEmpty() && !eventParameters.definingStations()
                .contains(obsComponent.getObservation().getReceiver().getSta())))
          obsComponent.setDefiningOriginal(false);

        // if an observation filter wants to change status, do it.
        if (eventParameters.observationFilter().apply(obsComponent))
          observationStatus.append(String.format(
              "Observation %s defining status changed to %s by an ObservationFilter.%n",
              obsComponent.toString(), (obsComponent.isDefining() ? "defining" : "non-defining")));

        if (obsComponent.isDefining() && !obsComponent.isObservationValid()) {
          obsComponent.setDefiningOriginal(false);
          String msg = String.format(
              "Observation %s set non-defining because one or both of observed value and uncertainty are == NA_VALUE%n",
              obsComponent.toString());
          observationStatus.append(msg);
          errorlog.writeln(msg);
        }

        // if (obsComponent.isDefining() && Math.abs(obsComponent.getReceiver().getDepth()) > 700)
        // {
        // obsComponent.setDefiningOriginal(false);
        // String msg = String.format(
        // "Observation %s set non-defining because receiver elevation (%1.3f km) if out of range
        // [-10, 10] km.%n",
        // obsComponent.toString(), -obsComponent.getReceiver().getDepth());
        // observationStatus.append(msg);
        // errorlog.writeln(msg);
        // }
      }

      // sort the observation ArrayList by station, phase, type.
      // type should be in order tt, az, sh.
      Map<String, ObservationComponent> sortedSet = new TreeMap<String, ObservationComponent>();
      for (ObservationComponent obs : source.getObsComponents())
        sortedSet.put(String.format("%s_%s_%d_%012d", obs.getObservation().getReceiver().getSta(),
            obs.getPhase().name(), obs.getObsType().ordinal(), obs.getObservationid()), obs);

      // now repopulate the observation ArrayList with the sorted
      // observations.
      source.getObsComponents().clear();
      source.getObsComponents().addAll(sortedSet.values());
    } catch (Exception e1) {
      e1.printStackTrace();
      throw new UnsupportedOperationException(e1.getMessage());
    }
  }

  /**
   * If depth is a free parameter, and the depth of this event is out of range, set depth fixed and
   * return true
   * 
   * @return true if depth constraints are violated.
   * @throws Exception
   */
  protected boolean checkDepthConstraints(Source locatorResults) throws Exception {
    if (source.isFree(DEPTH)) {
      double depthUncertainty = source.getHyperEllipse().getSdepth();
      if (depthUncertainty == Origerr.SDEPTH_NA && depthConstraintUncertaintyScale > 0.)
        throw new Exception("depthUncertainty == ORIGERR_NA_VLAUE is not allowed here.");
      double[] depthRange = eventParameters.getSeismicityDepthRange(getUnitVector());
      String problem = "";
      if (source.getDepth() + (depthUncertainty * depthConstraintUncertaintyScale
          + depthConstraintUncertaintyOffset) < depthRange[0]) {
        source.getFixed()[DEPTH] = true;
        fixedDepthIndex = 0;
        problem =
            String.format("%1.3f + (%1.3f * %1.3f + %1.3f) = %1.3f < %1.3f", source.getDepth(),
                depthUncertainty, depthConstraintUncertaintyScale, depthConstraintUncertaintyOffset,
                source.getDepth() + (depthUncertainty * depthConstraintUncertaintyScale
                    + depthConstraintUncertaintyOffset),
                depthRange[0]);
      } else if (source.getDepth() - (depthUncertainty * depthConstraintUncertaintyScale
          + depthConstraintUncertaintyOffset) > depthRange[1]) {
        source.getFixed()[DEPTH] = true;
        fixedDepthIndex = 1;
        problem =
            String.format("%1.3f - (%1.3f * %1.3f + %1.3f) = %1.3f > %1.3f", source.getDepth(),
                depthUncertainty, depthConstraintUncertaintyScale, depthConstraintUncertaintyOffset,
                source.getDepth() - (depthUncertainty * depthConstraintUncertaintyScale
                    + depthConstraintUncertaintyOffset),
                depthRange[1]);
      }

      if (source.getFixed()[DEPTH]) {
        String e = String.format(
            "WARNING:  Free depth solution is out of range for orid, evid = %1d, %1d.%n"
                + "Free depth solution: %1.4f, %1.4f, %1.3f, %1.3f%n"
                + "Depth of free depth solution: %1.3f +/- %1.3f%n"
                + "depthConstraintUncertainy scale, offset: %1.3f, %1.3f%n"
                + "Acceptable depth range: %1.3f to %1.3f km%n" + "%s%n"
                + "A fixed depth solution will be computed at %1.3f km%n" + "%n",
            source.getSourceId(), source.getEvid(), GeoMath.getLatDegrees(getUnitVector()),
            GeoMath.getLonDegrees(getUnitVector()), source.getDepth(), source.getOriginTime(),
            source.getDepth(), depthUncertainty, eventParameters.depthConstraintUncertaintyScale(),
            eventParameters.depthConstraintUncertaintyOffset(), depthRange[0], depthRange[1],
            problem, depthRange[fixedDepthIndex]);

        e = String.format(
            "WARNING: Free-depth solution at %1.3f km is out of range [%1.3f, %1.3f].  "
                + "Fixed-depth solution will be computed at %1.3f km. orid=%d, evid=%d.",
            source.getDepth(), depthRange[0], depthRange[1], depthRange[fixedDepthIndex],
            source.getSourceId(), source.getEvid());
        if (logger.getVerbosity() >= 3)
          logger.write(e);

        errorlog.write(e);

        source.appendErrorMessage(e);

        return true;
      }

    }
    return false;
  }

  /**
   * If upToDate is false, Event.update() is called which will update all the predictions. Then
   * upToDate is set to true.
   * 
   * @throws Exception
   */
  void checkStatus() throws Exception {
    if (!positionUpToDate || !originTimeUpToDate)
      update();
  }

  /**
   * Returns the sum of the squared weighted residuals for defining observations.
   * 
   * @return
   * @throws Exception
   */
  double sumSqrWeightedResiduals() throws Exception {
    checkStatus();
    return source.getSumSQRWeightedResiduals();
  }

  // Returns the root-mean-squared weighted residuals for defining
  // observations.
  double rmsWeightedResidual() throws Exception {
    checkStatus();
    if (definingVec.size() == 0)
      return -1.;
    return sqrt(source.getSumSQRWeightedResiduals() / definingVec.size());
  }

  public ArrayList<ObservationComponent> getDefiningVec() throws Exception {
    checkStatus();
    return definingVec;
  }

  boolean definingListChanged() {
    return definingChanged;
  }

  /**
   * Update each observation and the sum squared weighted residuals.
   * 
   * @throws Exception
   */
  private void update() throws Exception {
    long timer = System.nanoTime();

    definingChanged = false;

    if (!positionUpToDate) {
      // if any observations were originally defining but are now non-defining,
      // and the number of times their status has been changed here from non-defining
      // to defining is less than 10, change their status to defining.
      // In other words, if a defining observation gets set to non-defining 10 times
      // don't change it from non-defining to defining any more.
      for (ObservationComponent obs : source.getObsComponents())
        if (obs.isDefiningOriginal() && !obs.isDefining()
            && obs.incFlipFlop() < eventParameters.observationFlipFlops()) {
          obs.setDefining(true);
          definingChanged = true;
        }

      // ensure that requested attributes is accurate for each observation
      for (Observation obs : source.getObservations().values())
        obs.setRequestedAttributes(getEventParameters().needDerivatives());

      // Update predictions.
      // Note that all observations are being sent to their predictors,
      // but predictors will only compute predictions for those that are defining.
      ArrayList<Prediction> predictions = eventParameters.predictorFactory().computePredictions(
          source.getObservations().values(), eventParameters.predictionsThreadPool());

      // for each supported observation call setPredictions. That will update residuals.
      // Then for each ObservationComponent owned by the Observation, the observation will
      // call ObservationComponent.setPrediction() which will compute weights, weightedResiduals,
      // weightedDerivatives etc.
      for (Prediction p : predictions)
        source.getObservation(p.getObservationId()).setPrediction(p);

      // check for invalid observations
      for (ObservationComponent obs : source.getObsComponents())
        if (obs.isDefining() && !obs.predictionValid()) {
          // this observation is supposed to be defining but its prediction is invalid
          obs.setDefining(false);
          definingChanged = true;

          String emsg = String.format("Iteration %d. %s set non-defining because %s%n",
              iterationCount, obs.getSPA(), obs.getErrorMessage());

          source.appendErrorMessage(emsg);

          errorlog.write(emsg);
          if (logger.getVerbosity() >= 4)
            logger.write(emsg);

        }

      definingVec.clear();
      for (ObservationComponent obs : source.getObsComponents())
        if (obs.isDefining())
          definingVec.add(obs);

    } else
      // position of this Event did not change. Only the origin time changed.
      // No need for new Predictions but must update timeres and weighted residuals
      // and weighted derivatives.
      for (Observation obs : source.getObservations().values())
        obs.updateResiduals();


    // now deal with correlated observations, if necessary
    if (correlationMethod != CorrelationMethod.UNCORRELATED)
      correlatedObservations(definingVec);

    // finally, recompute the sum squared weighted residuals.
    source.setSumSQRWeightedResiduals(0.);
    for (int i = 0; i < definingVec.size(); i++)
      source.setSumSQRWeightedResiduals(source.getSumSQRWeightedResiduals()
          + Math.pow(definingVec.get(i).getWeightedResidual(), 2.));

    // increment the counter that keeps track of how many times sswr is computed.
    ++nSSWR;

    positionUpToDate = true;
    originTimeUpToDate = true;

    predictionTime += System.nanoTime() - timer;
  }

  /**
   * Update each observation including defining and non defining observations.
   * 
   * @throws Exception
   */
  void updateAll() throws Exception {
    long timer = System.nanoTime();

    // for all observation components, save the current defining status and set
    // defining status to true
    for (Observation obs : source.getObservations().values())
      for (ObservationComponent component : obs.getObservationComponents().values()) {
        component.setDefiningTemp(component.isDefining());
        component.setDefining(true);
      }

    // set up requested attributes
    for (Observation obs : source.getObservations().values())
      obs.setRequestedAttributes(getEventParameters().needDerivatives());

    // update predictions
    ArrayList<Prediction> predictions = eventParameters.predictorFactory().computePredictions(
        source.getObservations().values(), eventParameters.predictionsThreadPool());

    // for each supported observation call setPredictions. That will update residuals.
    // Then for each ObservationComponent owned by the Observation, the observation will
    // call ObservationComponent.setPrediction() which will compute weights, weightedResiduals,
    // weightedDerivatives etc.
    for (Prediction p : predictions)
      source.getObservation(p.getObservationId()).setPrediction(p);

    // now deal with correlated observations, if necessary
    if (correlationMethod != CorrelationMethod.UNCORRELATED) {
      ArrayList<ObservationComponent> definingVecTemp =
          new ArrayList<ObservationComponent>(3 * source.getObservations().size());

      for (Observation obs : source.getObservations().values())
        for (ObservationComponent component : obs.getObservationComponents().values())
          if (component.isObservationValid())
            definingVecTemp.add(component);
      correlatedObservations(definingVecTemp);
    }

    // for all observation components restore defining status to previous value
    for (Observation obs : source.getObservations().values())
      for (ObservationComponent component : obs.getObservationComponents().values())
        component.setDefining(component.isDefiningTemp());

    // finally, recompute the sum squared weighted residuals.
    source.setSumSQRWeightedResiduals(0.);
    for (int i = 0; i < definingVec.size(); i++)
      source.setSumSQRWeightedResiduals(source.getSumSQRWeightedResiduals()
          + Math.pow(definingVec.get(i).getWeightedResidual(), 2.));

    // source.setRMSWeightedResiduals();

    // increment the counter that keeps track of how many times sswr is computed.
    ++nSSWR;

    positionUpToDate = true;
    originTimeUpToDate = true;

    predictionTime += System.nanoTime() - timer;
  }

  private void correlatedObservations(ArrayList<ObservationComponent> definingVec)
      throws Exception {

    // to see voluminous output of the matrices that support correlated observations
    // set property debugCorrelatedObservations = true. The matrices are output to the log file
    // in a format that allows cutting and pasting into other programs like Matlab, etc.

    sigma = getCorrelationMatrix(definingVec);

    // pre and post multiply the correlation matrix by a diagonal matrix containing the total
    // uncertainy
    for (int i = 0; i < definingVec.size(); i++)
      for (int j = 0; j < definingVec.size(); j++)
        sigma[i][j] *=
            definingVec.get(i).getTotalUncertainty() * definingVec.get(j).getTotalUncertainty();

    if (eventParameters.debugCorrelatedObservations())
      logger.write(String.format("sigma=%n%s%n", printMatrix(sigma, " %23.16e")));

    // find the cholesky decomposition of sigma
    CholeskyDecomposition chol = new CholeskyDecomposition(sigma);

    if (!chol.isSPD())
      throw new LocOOException(String.format(
          "ERROR while trying to compute correlated observations. Cholesky decomposition of sigma failed "
              + "because sigma is not positive definite.%n"
              + "This often indicates that the supplied correlation scale is too large."));

    sigma = chol.getDecomposedMatrix();

    if (eventParameters.debugCorrelatedObservations())
      logger.write(
          String.format("Cholesky decomposition of sigma =%n%s%n", printMatrix(sigma, " %23.16e")));

    // find the inverse of the cholesky decomposition of sigma.
    sigma = new LUDecomposition(sigma).inverse();

    if (eventParameters.debugCorrelatedObservations())
      logger.write(String.format("The inverse of the cholesky decomposition of sigma =%n%s%n",
          printMatrix(sigma, " %23.16e")));


    // now multiply sigma times the residuals and put results in weighted residuals.
    double wr;
    for (int i = 0; i < definingVec.size(); i++) {
      wr = 0;
      for (int j = 0; j < definingVec.size(); j++)
        wr += sigma[i][j] * definingVec.get(j).getResidual();

      definingVec.get(i).setWeightedResidual(wr);
    }

    for (ObservationComponent obs : definingVec)
      obs.setWeight(obs.getWeightedResidual() / obs.getResidual());

    // multiply the sigma times the derivatives and put results in weighted derivatives.
    if (getEventParameters().needDerivatives())
      for (int k = 0; k < 4; k++)
        if (source.isFree(k))
          for (int i = 0; i < definingVec.size(); i++) {
            wr = 0;
            for (int j = 0; j < definingVec.size(); j++)
              wr += sigma[i][j] * definingVec.get(j).getDerivatives()[k];

            definingVec.get(i).getWeightedDerivatives()[k] = wr;
          }
  }

  /**
   * Given a List of ObservationComponents (definingVec), build a N x N matrix of correlation
   * coefficients where N is the size of the list. The matrix will have 1's on the diagonal and
   * value <= 1 on off-diagonal. The values will either be generated using the function exp(-dx^2)
   * where dx is the scaled distance between the two stations, or the values will have been read
   * from a file.
   * 
   * @param list
   * @return N x N matrix of correlation coefficients
   */
  private double[][] getCorrelationMatrix(List<ObservationComponent> list) {
    int i, j, n = list.size();

    double[][] c = new double[n][n];

    for (i = 0; i < n; i++)
      c[i][i] = 1.;

    if (correlationMethod == CorrelationMethod.FUNCTION1) {
      double dsta;
      for (i = 0; i < n - 1; i++) {
        // location of receiver i
        double[] ri = list.get(i).getObservation().getReceiver().getUnitVector();
        for (j = i + 1; j < n; j++)
          if (list.get(i).getObsType() == list.get(j).getObsType()
              && list.get(i).getPhase() == list.get(j).getPhase()) {
            // location of receiver j
            double[] rj = list.get(j).getObservation().getReceiver().getUnitVector();
            // distance from station j to station i in degrees divided by correlation scale
            dsta = GeoMath.angleDegrees(rj, ri) / eventParameters.correlationScale();

            c[i][j] = c[j][i] = exp(-sqr(dsta));

          }
      }
    } else if (correlationMethod == CorrelationMethod.FUNCTION2) {
      double scale = eventParameters.correlationScale();
      for (i = 0; i < n - 1; i++) {
        // location of receiver i
        double[] ri = list.get(i).getObservation().getReceiver().getUnitVector();
        // distance from source to receiver i
        double deltai = GeoMath.angleDegrees(ri, source.getUnitVector());
        for (j = i + 1; j < n; j++) {
          if (list.get(i).getObsType() == list.get(j).getObsType()
              && list.get(i).getPhase() == list.get(j).getPhase()) {
            // location of receiver j
            double[] rj = list.get(j).getObservation().getReceiver().getUnitVector();
            // distance from source to receiver j
            double deltaj = GeoMath.angleDegrees(rj, source.getUnitVector());
            double dsta = GeoMath.angleDegrees(ri, rj);
            double dmin = min(dsta / deltai, dsta / deltaj);
            c[i][j] = c[j][i] = exp(-sqr(dmin / scale));

            // System.out.printf("%-6s %-6s %6.4f%n",
            // observations.get(i).getObservation().getReceiver().getSta(),
            // observations.get(j).getObservation().getReceiver().getSta(),
            // c[i][j]);
          }
        }
      }
    } else if (correlationCoefficients != null) {
      for (i = 0; i < n; i++) {
        Map<String, Double> corr = correlationCoefficients.get(list.get(i).getStaPhaseType());
        if (corr != null)
          for (j = i + 1; j < n; ++j) {
            Double correlation = corr.get(list.get(j).getStaPhaseType());
            if (correlation != null)
              c[i][j] = c[j][i] = correlation;
          }
      }
    }
    return c;
  }

  /**
   * Retrieve a String representation of the observation covariance matrix.
   * 
   * @return String
   */
  public String getCorrelationMatrixString(List<ObservationComponent> list) {
    if (correlationMethod == CorrelationMethod.UNCORRELATED)
      return "Correlated observation option is not active.";

    StringBuffer cout = new StringBuffer();
    cout.append(String.format("Observation Correlation Coefficients:%n"));
    double[][] m = getCorrelationMatrix(list);
    for (int i = 0; i < list.size(); i++)
      if (list.get(i).isDefining())
        for (int j = i + 1; j < list.size(); ++j)
          if (list.get(j).isDefining() && m[i][j] > 1e-6)
            cout.append(String.format("%-16s %-16s %8.6f%n", list.get(i).getStaPhaseType(),
                list.get(j).getStaPhaseType(), m[i][j]));
    return cout.toString();
  }

  /**
   * Checks whether any defining observations have residuals that exceed the sBigResidualThreshold.
   * 
   * @return
   * @throws Exception
   */
  boolean areThereBigResiduals() throws Exception {
    if (eventParameters.allowBigResiduals())
      return false;
    checkStatus();
    for (ObservationComponent obs : definingVec)
      if (abs(obs.getWeightedResidual()) > eventParameters.bigResidualThreshold())
        return true;
    return false;

  }

  /**
   * Check for observations that have large weighted residuals. Large means >
   * gen_big_residual_threshold (default = 3). The number of large residuals identified will not
   * exceed gen_big_residual_max_fraction*(N-M) where N is the number of defining observations and M
   * is the number of free parameters in the location (4 for free depth, 3 for fixed depth, etc).
   * All observations thus identified are set non-defining.
   * 
   * @return true if any observations were set non-defining, false if none were set non-defining.
   */
  protected boolean checkBigResiduals() {
    if (eventParameters.allowBigResiduals())
      return false;

    TreeMap<Double, ObservationComponent> bigResiduals =
        new TreeMap<Double, ObservationComponent>();

    int n = 0;
    // add observations with weighted residuals greater than bigResidualThreshold to
    // the map. The map key is -abs(residual) so that entries are ordered from
    // largest to smallest absolute value.
    for (ObservationComponent obs : source.getObsComponents())
      if (obs.isDefining()) {
        ++n;
        if (abs(obs.getWeightedResidual()) > eventParameters.bigResidualThreshold())
          bigResiduals.put(-abs(obs.getWeightedResidual()), obs);
      }

    if (bigResiduals.size() == 0)
      return false;

    // n is number of defining observations. Subtract M to make it
    // number that can be set non-defining without making the problem
    // ill posed.
    n -= source.nFree();

    // can't set any to non-defining without making the problem ill-posed.
    if (n <= 0)
      return false;

    // determine how many to set non-defining. minimum number is one.
    // maximum is a percentage of the number of degrees of freedom.
    int nmax = Math.max(1, (int) Math.floor(n * eventParameters.bigResidualMaxFraction()));

    n = 0;
    for (ObservationComponent obs : bigResiduals.values()) {
      if (n++ == nmax)
        break;

      obs.setDefining(false);
      if (logger.getVerbosity() >= 4)
        logger.write(String.format(
            "Setting observation %s non-defining because weighted residual = %1.3f is greater than %1.3f%n",
            obs.toString(), obs.getWeightedResidual(), eventParameters.bigResidualThreshold()));

      // 10/09/2012, sb mod. if travel time is set to non-defining then
      // set az and sh non-defining as well.
      if (obs.getObsType() == GeoAttributes.TRAVEL_TIME)
        for (ObservationComponent ob : obs.getSiblings().values())
          ob.setDefining(false);
    }

    return true;
  }

  /**
   * Returns the root mean squared residuals for defining Travel Time Observations only.
   * 
   * @return
   * @throws Exception
   */
  double rmsTTResiduals() throws Exception {
    double sum = 0;
    int n = 0;

    checkStatus();
    for (int i = 0; i < definingVec.size(); i++)
      if (definingVec.get(i).getObsType() == GeoAttributes.TRAVEL_TIME) {
        sum += pow(definingVec.get(i).getResidual(), 2);
        n++;
      }
    if (n > 0)
      sum = sqrt(sum / n);

    return sum;

  } // END sumSQRTTResidual


  /**
   * Counts the number of observations associated with this origin. This is the number of unique
   * arids. Includes both defining and non-defining observations.
   * 
   * @return
   */
  int countObservations() {
    return source.getObservations().size();
  }

  /**
   * Returns the count of either DEFINING or NON-DEFINING observations
   * 
   * @param defining if true, count defining observations, false, non-defining
   * @return
   * @throws Exception
   */
  int countObservations(boolean defining) throws Exception {
    checkStatus();
    if (defining)
      return definingVec.size();
    return source.getObsComponents().size() - definingVec.size();
  } // END countObservations

  /**
   * Returns the count of either DEFINING or NON-DEFINING TT observations
   * 
   * @param defining
   * @return
   * @throws Exception
   */
  int countTTObservations(boolean defining) throws Exception {
    checkStatus();
    int count = 0;
    for (ObservationComponent obs : source.getObsComponents())
      if (obs.getObsType() == GeoAttributes.TRAVEL_TIME && obs.isDefining() == defining)
        count++;
    return count;
  } // END countTTObservations

  /**
   * Returns the count of either DEFINING or NON-DEFINING Az observations
   * 
   * @param defining
   * @return
   * @throws Exception
   */
  int countAzObservations(boolean defining) throws Exception

  {
    checkStatus();
    int count = 0;
    for (ObservationComponent obs : source.getObsComponents())
      if (obs.getObsType() == GeoAttributes.AZIMUTH && obs.isDefining() == defining)
        count++;
    return count;
  } // END countAzObservations

  /**
   * Returns the count of either DEFINING or NON-DEFINING Sh observations
   * 
   * @param defining
   * @return
   * @throws Exception
   */
  int countShObservations(boolean defining) throws Exception

  {
    checkStatus();
    int count = 0;
    for (ObservationComponent obs : source.getObsComponents())
      if (obs.getObsType() == GeoAttributes.SLOWNESS && obs.isDefining() == defining)
        count++;
    return count;
  } // END countShObservations

  /**
   * Counts the number of defining observations that are of the specified type (TT/Az/Sh) and phase.
   * 
   * @param phase
   * @param type
   * @return
   * @throws Exception
   */
  int countPhaseAndType(SeismicPhase phase, GeoAttributes type) throws Exception {
    checkStatus();
    int count = 0;
    for (int i = 0; i < definingVec.size(); i++)
      if (source.getObsComponents().get(i).getPhase() == phase
          && source.getObsComponents().get(i).getObsType() == type)
        ++count;
    return count;
  }

  /**
   * Counts the number of defining observations of crustal phases (Pn, Sn, Lg, Pg).
   * 
   * @return
   * @throws Exception
   */
  int countCrustalPhases() throws Exception {
    checkStatus();
    int count = 0;
    for (ObservationComponent obs : definingVec)
      if (obs.getPhase() == SeismicPhase.Pn || obs.getPhase() == SeismicPhase.Sn
          || obs.getPhase() == SeismicPhase.Pg || obs.getPhase() == SeismicPhase.Sg
          || obs.getPhase() == SeismicPhase.Lg)
        ++count;
    return count;
  }

  /**
   * Counts the number of stations that are either defining or nondefining and are within a
   * specified distance (in radians) from the event epicenter.
   * 
   * @param defining
   * @param dist in radians
   * @return
   * @throws Exception
   */
  Set<Receiver> countStations(boolean defining, double dist) throws Exception

  {
    checkStatus();
    Set<Receiver> in = new HashSet<Receiver>();
    for (ObservationComponent ob : source.getObsComponents())
      if (ob.isDefining() == defining && source.distance(ob.getObservation().getReceiver()) <= dist)
        in.add(ob.getObservation().getReceiver());

    return in;

  } // END countStations

  /**
   * Calculate an initial estimate of the event location.
   * 
   * @return
   * @throws Exception
   */
  Location calculateInitialLocation() throws Exception {
    Location loc = null;

    // find the earliest arrival time. That should be the closest station.
    // set the event location to the station location and the origin time
    // to 100 seconds before the arrival time.
    // If there are no travel time data then don't modify the origin time
    // and the locator should fix the origin time.
    for (ObservationComponent ob : source.getObsComponents())
      if (ob.getObsType() == GeoAttributes.TRAVEL_TIME && ob.isDefining()
          && (loc == null || ob.getObserved() < loc.getTime()))
        loc = new Location(ob.getObservation().getReceiver(), ob.getObserved());

    if (loc != null)
      loc.setTime(loc.getTime() - 100.);

    // make an index to the defining azimuth observations
    ArrayList<ObservationComponent> azObs = new ArrayList<ObservationComponent>();
    for (ObservationComponent ob : definingVec)
      if (ob.getObsType() == GeoAttributes.AZIMUTH)
        azObs.add(ob);

    if (azObs.size() == 1) {
      // if there is only one azimuth observation, set the initial event
      // location to 10 degrees away from the station location, in the direction of
      // the observed azimuth
      loc = new Location(azObs.get(0).getObservation().getReceiver()
          .move(azObs.get(0).getObserved(), toRadians(10.)), Globals.NA_VALUE);
    } else if (azObs.size() > 1) {
      try {
        // There are two or more azimuth observations, so use the great
        // circle intersections to estimate the event location.
        ArrayList<GreatCircle> gc = new ArrayList<GreatCircle>();
        for (ObservationComponent ob : azObs) {
          gc.add(new GreatCircle(ob.getObservation().getReceiver().getUnitVector(),
              ob.getObservation().getReceiver().move(ob.getObserved(), PI / 2).getUnitVector()));
        }

        double[] vector = new double[3];
        double[] cross = new double[3];

        for (GreatCircle gc1 : gc)
          for (GreatCircle gc2 : gc)
            if (gc1 != gc2) {
              VectorUnit.cross(gc1.getNormal(), gc2.getNormal(), cross);
              if (VectorUnit.dot(cross, gc1.getLast()) < 0.)
                Vector3D.negate(cross);
              Vector3D.increment(vector, cross);
            }

        loc = new Location(cross, VectorUnit.normalize(cross), Globals.NA_VALUE);
        loc.setDepth(0);
      } catch (Exception e) {
        throw new LocOOException(e.getCause());
      }
    }

    return loc;

  } // END calculateInitialLocation

  /**
   * Check whether the problem is properly constrained. In the case of a constrained problem, but
   * with improper observations relative to the free parameter set, constrain time or depth as
   * appropriate.
   * 
   * @throws Exception
   */
  void checkConstraints() throws Exception {
    int M = 0; // Number of free solution parameter (<=4)
    for (int j = 0; j < 4; j++)
      if (source.isFree(j))
        M++;

    int nD = countObservations(true); // number of defining observations
    // ------------------------------------------------------------------------
    // Must Have Defining Observations
    // ------------------------------------------------------------------------
    if (nD == 0)
      throw new LocOOException(
          String.format("ERROR:  No defining observations for evid=%d, orid=%d", source.getEvid(),
              source.getSourceId()));

    int nAz = countAzObservations(true); // number of az defining observations
    // ------------------------------------------------------------------------
    // Azimuth Only Check: Cannot Solve for Depth With Only Azimuth Observations.
    // Do not throw exception. Set depth fixed.
    // ------------------------------------------------------------------------
    if ((nD == nAz) && source.isFree(DEPTH)) {
      source.getFixed()[DEPTH] = true;
      --M;

      String emsg = String.format(
          "Fixing depth because there are only azimuth observations. " + "Evid=%d Orid=%d%n",
          source.getEvid(), source.getSourceId());

      errorlog.write(emsg);

      if (logger.getVerbosity() >= 2)
        logger.write(emsg);
    }

    // ------------------------------------------------------------------------
    // Basic Check: Need at Least as Many Defining Observations as Free Parameters
    // ------------------------------------------------------------------------
    if (nD < M)
      throw new LocOOException(String.format(
          "ERROR: Fewer defining observations (%d) than free parameters (%d) for evid=%d, orid=%d",
          nD, M, source.getEvid(), source.getSourceId()));


    // If TIME is Free, then we need at least 1 TT Observation
    int nTT = countTTObservations(true); // number of time defining observations
    if (source.isFree(TIME) && nTT == 0)
      throw new LocOOException(String.format(
          "ERROR: When time is a free parameter must have at least 1 travel time observation.  evid=%d, orid=%d",
          source.getEvid(), source.getSourceId()));

    // If there is only one station and LAT / LON are unconstrained then we need at least 1 azimuth
    if (nAz == 0 && (source.isFree(LAT) || source.isFree(LON))
        && countStations(true, 1e30).size() == 1)
      throw new LocOOException(String.format(
          "ERROR: With only one station, and epicenter is unconstrained, must have at least one defining azimuth observation.  evid=%d, orid=%d",
          source.getEvid(), source.getSourceId()));


  } // END isConstrained

  public Map<GeoAttributes, ObservationComponent> getObservations(Observation observation) {
    return observation.getObservationComponents();
  }

  public Map<GeoAttributes, ObservationComponent> getObservations(Long arid) {
    return source.getObservations().get(arid).getObservationComponents();
  }

  public int countRemovedObservations() {
    int n = 0;
    for (Observation observation : source.getObservations().values())
      for (ObservationComponent obs : observation.getObservationComponents().values())
        if (obs.isDefiningOriginal() && !obs.isDefining())
          ++n;
    return n;
  }

  public String getInputLocationString() {
    return String.format(
        "Input location:%n%n      Orid      Evid         Lat         Lon     Depth             Time                Date (GMT)     JDate%n"
            + "%10d %9d %11.6f %11.6f %9.3f %16.4f %25s  %8d%n%n"
            + "Geographic region: %s (%d)    Seismic region %s (%d)%n",
        source.getSourceId(), source.getEvid(), inputLocation.getLatDegrees(),
        inputLocation.getLonDegrees(), inputLocation.getDepth(), inputLocation.getTime(),
        GMTFormat.GMT_MS.format(GMTFormat.getDate(inputLocation.getTime())),
        GMTFormat.getJDate(inputLocation.getTime()),
        FlinnEngdahlCodes.getGeoRegionName(source.getLatDegrees(), source.getLonDegrees()),
        FlinnEngdahlCodes.getGeoRegionIndex(source.getLatDegrees(), source.getLonDegrees()),
        FlinnEngdahlCodes.getSeismicRegionName(source.getLatDegrees(), source.getLonDegrees()),
        FlinnEngdahlCodes.getSeismicRegionIndex(source.getLatDegrees(), source.getLonDegrees()));

  }

  public String getSiteTable() {
    TreeMap<String, Receiver> siteMap = new TreeMap<String, Receiver>();

    for (Observation observation : source.getObservations().values())
      siteMap.put(String.format("%s_%d", observation.getReceiver().getSta(),
          observation.getReceiver().getOndate()), observation.getReceiver());

    StringBuffer cout = new StringBuffer(String.format(
        "Site Table:%n%nSta      OnDate   OffDate      Lat         Lon       Elev    StaName%n"));

    for (Receiver site : siteMap.values())
      cout.append(String.format("%-8s %7d  %7d  %10.6f %11.6f %8.4f   %-1s%n", site.getSta(),
          site.getOndate(), site.getOffdate(), site.getLatDegrees(), site.getLonDegrees(),
          -site.getDepth(), site.getStaName()));
    return cout.toString();
  }

  /**
   * Retrieve a String representation of the observation table printed at the start of the location
   * calculation.
   * 
   * @return observation table
   * @throws Exception
   */
  public String getObservationTable() throws Exception {
    StringBuffer cout = new StringBuffer(String.format(
        "Observation Table:%n%n       Arid  Sta    Phase   Typ Def      Dist          Obs      Obs_err    Predictor%n"));

    for (ObservationComponent obs : sortedObservations(false)) {
      Predictor p = eventParameters.predictorFactory().getPredictor(obs.getPhase());
      if (p == null)
        throw new Exception("No Predictor for phase " + obs.getPhase().toString());
      p = p.getPredictor(obs.getObservation());
      cout.append(obs.observationString(p)).append(Globals.NL);
    }

    cout.append(Globals.NL).append(observationStatus.toString());
    return cout.toString();
  }

  /**
   * Get String representation of observation table that is output each iteration.
   * 
   * @return observation table
   */
  public String getObsIterationTable(boolean defining_only) {
    StringBuffer cout = new StringBuffer(
        "     Arid  Sta    Phase   Typ Def  Predictor                Obs      Obs_err         Pred    Total_err       Weight     Residual      W_Resid         Dist      ES_Azim      SE_Azim");
    cout.append(Globals.NL);

    for (ObservationComponent obs : sortedObservations(defining_only))
      cout.append(obs.obsIterationString()).append(Globals.NL);
    return cout.toString();
  }

  public String getPredictionTable(boolean defining_only) {
    StringBuffer cout = new StringBuffer(
        "     Arid  Sta    Phase   Typ Def  Model           Model_uncert   Base_model    Ellip_corr    Elev_rcvr     Elev_src    Site_corr  Source_corr    Path_corr      ME_corr       d_dLat       d_dLon         d_dZ         d_dT");
    cout.append(Globals.NL);

    for (ObservationComponent obs : sortedObservations(defining_only))
      cout.append(obs.predictionString()).append(Globals.NL);
    return cout.toString();
  }

  /**
   * Get the current set of ObservationComponents, sorted in a manner proscribed by property
   * io_observation_sort_order defined in the properties file.
   * 
   * @param definingOnly
   * @return
   */
  private ArrayList<ObservationComponent> sortedObservations(boolean definingOnly) {
    ArrayList<ObservationComponent> list =
        new ArrayList<ObservationComponent>(source.getObsComponents().size());
    for (ObservationComponent obs : source.getObsComponents())
      if (!definingOnly || obs.isDefining())
        list.add(obs);

    String sortOrder = eventParameters.observation_sort_order().toLowerCase().replaceAll("_", "");

    if (sortOrder.equalsIgnoreCase("distance")) {
      Collections.sort(list, new Comparator<ObservationComponent>() {
        @Override
        public int compare(ObservationComponent o1, ObservationComponent o2) {
          int compare = (int) Math
              .signum(o1.getObservation().getDistance() - o2.getObservation().getDistance());
          if (compare != 0)
            return compare;
          if (o1.getSta().equals(o2.getSta()))
            if (o1.getPhase() == o2.getPhase())
              return o2.getObsTypeShort().compareTo(o1.getObsTypeShort());
            else
              return o1.getPhase().toString().compareTo(o2.getPhase().toString());
          return o1.getSta().compareTo(o2.getSta());
        }
      });
    } else if (sortOrder.contains("weightedresidual")) {

      Collections.sort(list, new Comparator<ObservationComponent>() {

        @Override
        public int compare(ObservationComponent o1, ObservationComponent o2) {
          int compare = (int) Math
              .signum(Math.abs(o1.getWeightedResidual()) - Math.abs(o2.getWeightedResidual()));
          if (compare != 0)
            return compare;
          if (o1.getSta().equals(o2.getSta()))
            if (o1.getPhase() == o2.getPhase())
              return o2.getObsTypeShort().compareTo(o1.getObsTypeShort());
            else
              return o1.getPhase().toString().compareTo(o2.getPhase().toString());
          return o1.getSta().compareTo(o2.getSta());
        }

      });
    } else if (sortOrder.equals("observationid")) {

      Collections.sort(list, new Comparator<ObservationComponent>() {

        @Override
        public int compare(ObservationComponent o1, ObservationComponent o2) {
          int compare =
              (int) Math.signum(Math.abs(o1.getObservationid()) - Math.abs(o2.getObservationid()));
          if (compare != 0)
            return compare;
          if (o1.getSta().equals(o2.getSta()))
            if (o1.getPhase() == o2.getPhase())
              return o2.getObsTypeShort().compareTo(o1.getObsTypeShort());
            else
              return o1.getPhase().toString().compareTo(o2.getPhase().toString());
          return o1.getSta().compareTo(o2.getSta());
        }

      });
    } else // if (sortOrder.equals("station_phase"))
    {
      Collections.sort(list, new Comparator<ObservationComponent>() {

        @Override
        public int compare(ObservationComponent o1, ObservationComponent o2) {
          if (o1.getSta().equals(o2.getSta()))
            if (o1.getPhase() == o2.getPhase())
              return o2.getObsTypeShort().compareTo(o1.getObsTypeShort());
            else
              return o1.getPhase().toString().compareTo(o2.getPhase().toString());
          return o1.getSta().compareTo(o2.getSta());
        }

      });
    }
    return list;
  }

  @Override
  public String toString() {
    try {
      return String.format(
          "Lat=%8.4f  lon=%9.4f  z=%7.3f t0=%7.3f rms_Trsd=%7.4f rms_Wrsd=%7.4f dNorth=%8.3f dEast=%8.3f dZ=%7.3f dT=%8.4f dkm=%8.3f nf=%3d damp=%3d conv=%1.2e",
          source.getLatDegrees(), source.getLonDegrees(), source.getDepth(),
          source.getTime() - getInitialLocation().getTime(), rmsTTResiduals(),
          rmsWeightedResidual(), dloc[LAT] * source.getRadius(), dloc[LON] * source.getRadius(),
          dloc[DEPTH], dloc[TIME], dkm, nSSWR, (int) round(log10(applied_damping)),
          lsq_convergence_value);
    } catch (Exception e) {
      return e.getStackTrace().toString();
    }
  }

  /**
   * Returns a deep copy of the Location object that this Event extends.
   * 
   * @return Location
   * @throws Exception
   */
  public Location getLocation() throws Exception {
    return new Location(source.getUnitVector().clone(), source.getRadius(), source.getTime());
  }

  /**
   * setLocation
   * 
   * @param location new Location
   */
  public void setLocation(Location location) {
    source.setLocation(location);
    setPositionUpToDateFalse();
  }

  /**
   * Sets the working location of this Event to a new Location which is offset from some old
   * location by dloc. Returns the distance in km from old location to new location. This distance
   * is equal to GeoVector.distance3D() plus difference in time * 8 km/sec.
   * 
   * @param dloc double[] change in lat (radians), lon (radians), depth (km) and time (sec). Change
   *        in lon is measures in great circle sense, not small circle.
   * @return double distance moved in km.
   */
  public double moveLocation(Location oldLocation, double[] dloc) throws Exception {
    double dkm = sqrt(pow(dloc[0] * oldLocation.getRadius(), 2)
        + pow(dloc[1] * oldLocation.getRadius(), 2) + pow(dloc[2], 2) + pow(dloc[3] * 8., 2));

    // compute a new Location
    setLocation(oldLocation.move(dloc));
    return dkm;
  }

  /**
   * If false, modelUncertainty will always be zero. Default is true.
   * 
   * @return useModelUncertainty
   */
  public boolean useTTModelUncertainty() {
    return eventParameters.useTTModelUncertainty();
  }

  /**
   * If false, modelUncertainty will always be zero. Default is true.
   * 
   * @return useModelUncertainty
   */
  public boolean useAzModelUncertainty() {
    return eventParameters.useAzModelUncertainty();
  }

  /**
   * If false, modelUncertainty will always be zero. Default is true.
   * 
   * @return useModelUncertainty
   */
  public boolean useShModelUncertainty() {
    return eventParameters.useShModelUncertainty();
  }

  public void setPositionUpToDateFalse() {
    if (positionUpToDate) {
      positionUpToDate = false;
      for (Observation observation : source.getObservations().values())
        observation.predictionUpToDate(false);
    }
  }

  public void setOriginTimeUpToDateFalse() {
    originTimeUpToDate = false;
  }

  public void change(double dlat, double dlon, double ddepth, double dtime) {
    source.change(dlat, dlon, ddepth, dtime);
    setPositionUpToDateFalse();
  }

  public void change(double[] dloc) {
    source.change(dloc);
    setPositionUpToDateFalse();
  }

  public void setTime(double time) {
    source.setTime(time);
    setOriginTimeUpToDateFalse();
  }

  public void add(double[] u) {
    source.add(u);
    setPositionUpToDateFalse();
  }

  public void flip() {
    source.flip();
    setPositionUpToDateFalse();
  }

  public double[] getUnitVector() {
    return source.getUnitVector();
  }

  public void interpolate(GeoVector g1, GeoVector g2, double fraction) {
    source.interpolate(g1, g2, fraction);
    setPositionUpToDateFalse();
  }

  public void mean(Collection<GeoVector> geoVectors) {
    source.mean(geoVectors);
    setPositionUpToDateFalse();
  }

  public void mean(Object[] geoVectors) {
    source.mean(geoVectors);
    setPositionUpToDateFalse();
  }

  public void midpoint(GeoVector g1, GeoVector g2) {
    source.midpoint(g1, g2);
    setPositionUpToDateFalse();
  }

  public boolean move_north(double distance) {
    boolean m = source.move_north(distance);
    setPositionUpToDateFalse();
    return m;
  }

  public void rotateThis(double[] eulerRotationMatrix) {
    source.rotateThis(eulerRotationMatrix);
    setPositionUpToDateFalse();
  }

  /**
   * Rotate this location around pole by angle. Positive rotation is clockwise when looking in
   * direction of pole (right hand rule).
   * 
   * @param pole
   * @param angle in radians
   */
  public void rotateThis(GeoVector pole, double angle) {
    source.rotateThis(pole, angle);
    setPositionUpToDateFalse();
  }

  public GeoVector setDepth(double depth) {
    GeoVector g = source.setDepth(depth);
    setPositionUpToDateFalse();
    return g;
  }

  public void setGeoVector(double lat, double lon, double depth, boolean inDegrees) {
    source.setGeoVector(lat, lon, depth, inDegrees);
    setPositionUpToDateFalse();
  }

  public void setGeoVector(double[] v, double radius) {
    source.setGeoVector(v, radius);
    setPositionUpToDateFalse();
  }

  public void setGeoVector(double[] v) {
    source.setGeoVector(v);
    setPositionUpToDateFalse();
  }

  public GeoVector setRadius(double r) {
    GeoVector g = source.setRadius(r);
    setPositionUpToDateFalse();
    return g;
  }

  public void setUnitVector(double[] u) {
    source.setUnitVector(u);
    setPositionUpToDateFalse();
  }

  @SuppressWarnings("unused")
  private String printMatrix(double[][] x) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < x.length; ++i) {
      for (int j = 0; j < x[i].length; ++j)
        buf.append("  ").append(Double.toString(x[i][j]));
      buf.append(Globals.NL);
    }
    return buf.toString();
  }

  private String printMatrix(double[][] x, String format) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < x.length; ++i) {
      for (int j = 0; j < x[i].length; ++j)
        buf.append(String.format(format, x[i][j]));
      buf.append(Globals.NL);
    }
    return buf.toString();
  }

  private double time0;

  /**
   * Generates a grid of sum squared weighted residuals.
   * <p>
   * Required parameters: <br>
   * grid_output_file_name = output file <br>
   * grid_output_file_format = vtk <br>
   * grid_origin_source = epicenter <br>
   * grid_origin_lat <br>
   * grid_origin_lon <br>
   * grid_origin_depth <br>
   * grid_map_nwidth = 21 <br>
   * grid_map_nheight = 23 <br>
   * grid_map_ndepth = 1 <br>
   * grid_map_width = 20 <br>
   * grid_map_height = 22 <br>
   * grid_map_depth_range = 0 <br>
   * grid_units = radians | degrees | km | meters
   * 
   * @throws IOException
   * @throws Exception
   */
  protected void griddedResiduals(ArrayList<Source> sourceList) throws Exception {
    if (!eventParameters.computeGriddedResiduals())
      return;

    if (sourceList == null || sourceList.isEmpty())
      return;

    String outputFileName = eventParameters.properties().getProperty("grid_output_file_name");

    outputFileName = outputFileName.replace("<orid>", Long.toString(source.getSourceId()));
    outputFileName = outputFileName.replace("<sourceid>", Long.toString(source.getSourceId()));

    File outputFile = new File(outputFileName);
    outputFile.getParentFile().mkdir();

    String gridFileFormat =
        eventParameters.properties().getProperty("grid_output_file_format", "vtk").toLowerCase();

    int nObservationFlipFlops = eventParameters.observationFlipFlops();

    eventParameters.observationFlipFlops(Integer.MAX_VALUE);

    time0 = source.getTime();
    double sswr_minimum = sourceList.get(0).getSumSQRWeightedResiduals();

    Location center = null;
    String grid_origin_center =
        eventParameters.properties().getProperty("grid_origin_source", "epicenter");
    if (eventParameters.properties().containsKey("grid_origin_lat")) {
      center = new Location(eventParameters.properties().getDouble("grid_origin_lat"),
          eventParameters.properties().getDouble("grid_origin_lon"),
          eventParameters.properties().getDouble("grid_origin_depth", 0.), true, 0.);
    } else if (grid_origin_center.equals("epicenter")) {
      center = sourceList.get(0).getLocation();
      center.setDepth(0.);
    } else if (grid_origin_center.equals("hypocenter"))
      center = sourceList.get(0).getLocation();
    else if (grid_origin_center.equals("centroid")) {
      double[] u = new double[3];
      double depth = 0;
      for (Source lr : sourceList) {
        depth += lr.getLocation().getDepth();
        for (int i = 0; i < 3; ++i)
          u[i] += lr.getLocation().getUnitVector()[i];
      }
      GeoMath.normalize(u);
      depth /= sourceList.size();

      center = new Location(new GeoVector(u, GeoMath.getEarthRadius(u) - depth), 0.);
    }

    double earthRadius = GeoMath.getEarthRadius(center.getUnitVector());

    int nx = eventParameters.properties().getInt("grid_map_nwidth");
    int ny = eventParameters.properties().getInt("grid_map_nheight");
    int nz = eventParameters.properties().getInt("grid_map_ndepth", 1);

    double width = eventParameters.properties().getDouble("grid_map_width");
    double height = eventParameters.properties().getDouble("grid_map_height");

    double depth0 = center.getDepth();
    double ddepth = 0.;
    if (nz > 1) {
      double[] depthRange = eventParameters.properties().getDoubleArray("grid_map_depth_range");
      // depthRange is 2-element array with depth of top and bottom of block.
      if (depthRange.length != 2)
        throw new LocOOException("\nProperty grid_map_depth_range must have two "
            + "elements for minimum and maximum depth in km");

      depth0 = depthRange[0];
      ddepth = (depthRange[1] - depthRange[0]) / (nz - 1);
    }

    String gridUnits = eventParameters.properties().getProperty("grid_units", "degrees");
    // convert to radians
    double convert = 1.;
    GeoVector pole = null;

    if (gridUnits.equals("degrees")) {
      convert = Math.PI / 180.;
      pole = new GeoVector(new double[] {0., 0., 1.}, 1.);
    } else if (gridUnits.equals("km")) {
      convert = 1. / earthRadius;
      pole = center.moveNorth(Math.PI / 2);
    } else
      throw new LocOOException(
          String.format("Property grid_units = %s but must be one of [degrees | km ]", gridUnits));

    /////////////////////////////////////////////////////////////////////////////////////////
    // plot ellipses and ellipse centers
    int pointPerEllipse = 500;
    ArrayList<double[]> points = new ArrayList<>(sourceList.size() * pointPerEllipse);
    ArrayList<VTKCell> cells = new ArrayList<>(sourceList.size());

    for (Source lr : sourceList) {
      cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, points.size(), pointPerEllipse));
      points.addAll(lr.getEllipse().getPointsKm(center.getUnitVector(), pointPerEllipse));
    }
    VTKDataSet.write(new File(outputFile.getParentFile(), "ellipses.vtk"), points, cells);

    points.clear();
    for (Source lr : sourceList) {
      double dkm =
          new GreatCircle(center.getUnitVector(), lr.getLocation().getUnitVector()).getDistanceKm();
      double az = GeoMath.azimuth(center.getUnitVector(), lr.getLocation().getUnitVector(), 0.);
      points.add(new double[] {dkm * sin(az), dkm * cos(az), 0.});
    }
    VTKDataSet.write(new File(outputFile.getParentFile(), "ellipse_centers.vtk"), points,
        VTKCellType.VTK_POLY_VERTEX);

    /////////////////////////////////////////////////////////////////////////////////////////
    // plot receiver locations
    points.clear();
    for (Observation obs : source.getObservations().values())
      if (obs.isTimedef()) {
        double[] r = obs.getReceiver().getUnitVector();
        double az = VectorUnit.azimuth(center.getUnitVector(), r, 0.);
        double dist = VectorUnit.angle(center.getUnitVector(), r) * earthRadius;
        double x = dist * sin(az);
        double y = dist * cos(az);
        if (abs(x) <= width && abs(y) <= height)
          points.add(new double[] {x, y, 0.});
      }
    VTKDataSet.write(new File(outputFile.getParentFile(), "receivers.vtk"), points,
        VTKCellType.VTK_POLY_VERTEX);

    /////////////////////////////////////////////////////////////////////////////////////////
    // plot location tracks
    points.clear();
    ArrayList<VTKCell> polypoints = new ArrayList<>();
    ArrayList<VTKCell> polylines = new ArrayList<>();
    for (Source lr : sourceList) {
      if (!lr.getLocationTrack().isEmpty()) {
        ArrayList<Integer> pid = new ArrayList<Integer>(lr.getLocationTrack().size());
        for (Location location : lr.getLocationTrack()) {
          double[] r = location.getUnitVector();
          double az = VectorUnit.azimuth(center.getUnitVector(), r, 0.);
          double dist = VectorUnit.angle(center.getUnitVector(), r) * earthRadius;
          pid.add(points.size());
          points.add(new double[] {dist * sin(az), dist * cos(az), 0.});
        }
        polypoints.add(new VTKCell(VTKCellType.VTK_POLY_VERTEX, pid));
        polylines.add(new VTKCell(VTKCellType.VTK_POLY_LINE, pid));
      }
    }
    if (points.size() > 0) {
      VTKDataSet.write(new File(outputFile.getParentFile(), "locationTracks.vtk"), points,
          polylines);
      VTKDataSet.write(new File(outputFile.getParentFile(), "locationTrack_points.vtk"), points,
          polypoints);
    }


    /////////////////////////////////////////////////////////////////////////////////////////


    GeoVector[][] grid =
        center.getGrid(pole, nx, width * convert / (nx - 1), ny, height * convert / (ny - 1));

    double[][][] sswr = new double[ny][nx][nz];
    double[][][] rmswr = new double[ny][nx][nz];

    if (logger.getVerbosity() > 0)
      logger.writeln("Computing gridded residuals");

    double min_rmswr = Double.POSITIVE_INFINITY;
    Location min_location = null;

    int nObs = sourceList.get(0).getNobs();
    double r, az, x, y;
    Brents brents = new Brents();
    double[] xbrack = new double[] {-5, 5};

    long timer = System.currentTimeMillis();
    for (int i = 0; i < ny; ++i) {
      System.out.println("gridded residuals :" + (ny - i));
      for (int j = 0; j < nx; ++j)
        for (int k = 0; k < nz; ++k) {

          setLocation(new Location(grid[i][j].setDepth(depth0 + k * ddepth), source.getTime()));

          xbrack = mnbrak(-20., 20.);

          sswr[i][j][k] = brents.minF(xbrack[0], xbrack[1], this);

          double t = brents.getExtremaAbscissa();
          double xx = (t - xbrack[0]) / (xbrack[1] - xbrack[0]);
          boolean ok = xx > 0. && xx < 1.;

          if (!ok)
            System.out.printf("mnbrak: %10.3f %10.3f %10.3f %b %d%n", xbrack[0], t, xbrack[1], ok,
                getnSSWR());


          rmswr[i][j][k] = Math.sqrt(sswr[i][j][k] / nObs);

          // System.out.println("DEBUG in Event.gridded_residuals "+rmswr[i][j][k]);

          if (rmswr[i][j][k] < min_rmswr) {
            min_rmswr = rmswr[i][j][k];
            min_location = getLocation();
          }
        }
    }

    timer = System.currentTimeMillis() - timer;

    if (logger.getVerbosity() >= 1) {
      logger.writef(String.format("Center of grid = %s%n", center.toString()));

      logger.writef("Mimimum rms_wr = %1.6f found at %s %1.3f%n", min_rmswr,
          min_location.toString(), min_location.getTime() - time0);

    }

    if (gridFileFormat.equals("vtk")) {
      if (logger.getVerbosity() >= 1)
        logger.write(
            String.format("Writing gridded residuals to file %s%n", outputFile.getCanonicalPath()));

      DataOutputStream output =
          new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));

      output.writeBytes(String.format("# vtk DataFile Version 2.0%n"));
      output.writeBytes(String.format("LocOO3D_Gridded_Residuals%n"));
      output.writeBytes(String.format("BINARY%n"));

      output.writeBytes(String.format("DATASET UNSTRUCTURED_GRID%n"));

      output.writeBytes(String.format("POINTS %d double%n", nx * ny * nz));

      if (gridUnits.equals("km"))
        for (int k = 0; k < nz; ++k)
          for (int i = 0; i < ny; ++i)
            for (int j = 0; j < nx; ++j) {
              r = center.distance(grid[i][j]) / convert;
              az = center.azimuth(grid[i][j], 0.);
              x = r * Math.sin(az);
              y = r * Math.cos(az);
              output.writeDouble(x);
              output.writeDouble(y);
              output.writeDouble(-depth0 - k * ddepth);
            }
      else if (gridUnits.equals("degrees"))
        for (int k = 0; k < nz; ++k)
          for (int i = 0; i < ny; ++i)
            for (int j = 0; j < nx; ++j) {
              x = grid[i][j].getLonDegrees();
              y = grid[i][j].getLatDegrees();
              output.writeDouble(x);
              output.writeDouble(y);
              output.writeDouble(depth0 + k * ddepth);
            }

      if (nz == 1) {
        int nCells = (nx - 1) * (ny - 1);
        output.writeBytes(String.format("CELLS %d %d%n", nCells, nCells * 5));

        for (int i = 0; i < ny - 1; ++i)
          for (int j = 0; j < nx - 1; ++j) {
            output.writeInt(4);
            output.writeInt(i * nx + j);
            output.writeInt(i * nx + j + 1);
            output.writeInt((i + 1) * nx + j + 1);
            output.writeInt((i + 1) * nx + j);
          }

        output.writeBytes(String.format("CELL_TYPES %d%n", nCells));
        for (int t = 0; t < nCells; ++t)
          output.writeInt(9); // vtk_quad
      } else {
        int nCells = (nx - 1) * (ny - 1) * (nz - 1);
        output.writeBytes(String.format("CELLS %d %d%n", nCells, nCells * 9));

        for (int k = 0; k < nz - 1; ++k)
          for (int i = 0; i < ny - 1; ++i)
            for (int j = 0; j < nx - 1; ++j) {
              output.writeInt(8);
              output.writeInt(k * nx * ny + i * nx + j);
              output.writeInt(k * nx * ny + i * nx + j + 1);
              output.writeInt(k * nx * ny + (i + 1) * nx + j + 1);
              output.writeInt(k * nx * ny + (i + 1) * nx + j);
              output.writeInt((k + 1) * nx * ny + i * nx + j);
              output.writeInt((k + 1) * nx * ny + i * nx + j + 1);
              output.writeInt((k + 1) * nx * ny + (i + 1) * nx + j + 1);
              output.writeInt((k + 1) * nx * ny + (i + 1) * nx + j);
            }

        output.writeBytes(String.format("CELL_TYPES %d%n", nCells));
        for (int t = 0; t < nCells; ++t)
          output.writeInt(12); // vtk_hexahedron
      }

      output.writeBytes(String.format("POINT_DATA %d%n", ny * nx * nz));

      output.writeBytes("SCALARS Sum_Squared_Weighted_Residuals float 1\n");
      output.writeBytes("LOOKUP_TABLE default\n");

      for (int k = 0; k < nz; ++k)
        for (int i = 0; i < ny; ++i)
          for (int j = 0; j < nx; ++j)
            output.writeFloat((float) sswr[i][j][k]);

      output.writeBytes("SCALARS Delta_Sum_Squared_Weighted_Residuals float 1\n");
      output.writeBytes("LOOKUP_TABLE default\n");

      for (int k = 0; k < nz; ++k)
        for (int i = 0; i < ny; ++i)
          for (int j = 0; j < nx; ++j)
            output.writeFloat((float) (sswr[i][j][k] - sswr_minimum));

      output.writeBytes("SCALARS Root_Mean_Squared_Weighted_Residuals float 1\n");
      output.writeBytes("LOOKUP_TABLE default\n");

      for (int k = 0; k < nz; ++k)
        for (int i = 0; i < ny; ++i)
          for (int j = 0; j < nx; ++j)
            output.writeFloat((float) rmswr[i][j][k]);

      output.close();

      if (logger != null && logger.getVerbosity() > 0)
        logger.writeln(
            "Time to compute gridded residuals = " + GMPGlobals.ellapsedTime(timer * .001));

    }

    eventParameters.observationFlipFlops(nObservationFlipFlops);

    // restore the best fit location and all predictions, residuals, etc.
    update();
  }

  @Override
  public double bFunc(double originTime) throws Exception {
    source.setTime(originTime + time0);
    positionUpToDate = false;
    return sumSqrWeightedResiduals();
  }

  /**
   * Bracket a minimum of function bFunc
   * 
   * @param x1 initial estimate of left abscissa
   * @param x2 initial estimate of right abscissa
   * @return two abscissa values that bracket the minimum.
   * @throws Exception
   */
  private double[] mnbrak(double x1, double x2) throws Exception {
    double GOLD = 1.618034, GLIMIT = 100.0, TINY = 1.0e-20;
    double ulim, r, q;

    double[] f = new double[4];
    double[] x = new double[] {x1, x2, 0., 0.};

    f[0] = bFunc(x[0]);
    f[1] = bFunc(x[1]);
    if (f[1] > f[0]) {
      x[2] = x[0];
      x[0] = x[1];
      x[1] = x[2];
      f[2] = f[0];
      f[0] = f[1];
      f[1] = f[2];
    }
    x[2] = x[1] + GOLD * (x[1] - x[0]);
    f[2] = bFunc(x[2]);
    while (f[1] > f[2]) {
      r = (x[1] - x[0]) * (f[1] - f[2]);
      q = (x[1] - x[2]) * (f[1] - f[0]);
      x[3] = x[1] - ((x[1] - x[2]) * q - (x[1] - x[0]) * r)
          / (2.0 * nr_sign(Math.max(abs(q - r), TINY), q - r));
      ulim = x[1] + GLIMIT * (x[2] - x[1]);
      if ((x[1] - x[3]) * (x[3] - x[2]) > 0.0) {
        f[3] = bFunc(x[3]);
        if (f[3] < f[2]) {
          x[0] = x[1];
          x[1] = x[3];
          f[0] = f[1];
          f[1] = f[3];
          return new double[] {x[0], x[2]};
        } else if (f[3] > f[1]) {
          x[2] = x[3];
          f[2] = f[3];
          return new double[] {x[0], x[2]};
        }
        x[3] = x[2] + GOLD * (x[2] - x[1]);
        f[3] = bFunc(x[3]);
      } else if ((x[2] - x[3]) * (x[3] - ulim) > 0.0) {
        f[3] = bFunc(x[3]);
        if (f[3] < f[2]) {
          nr_shft3(x, x[3] + GOLD * (x[3] - x[2]));
          nr_shft3(f, bFunc(x[3]));
        }
      } else if ((x[3] - ulim) * (ulim - x[2]) >= 0.0) {
        x[3] = ulim;
        f[3] = bFunc(x[3]);
      } else {
        x[3] = x[2] + GOLD * (x[2] - x[1]);
        f[3] = bFunc(x[3]);
      }
      nr_shft3(x);
      nr_shft3(f);
    }
    return new double[] {x[0], x[2]};
  }

  private double nr_sign(double a, double b) {
    return b >= 0 ? (a >= 0 ? a : -a) : (a >= 0 ? -a : a);
  }

  private void nr_shft3(double[] a) {
    a[0] = a[1];
    a[1] = a[2];
    a[2] = a[3];
  }

  private void nr_shft3(double[] x, double y) {
    x[1] = x[2];
    x[2] = x[3];
    x[3] = y;
  }

  public void setInitialLocation(Location initialLocation) {
    this.initialLocation = initialLocation;
  }

  /**
   * Get the depth of the upper or lower depth constraint at the current location.
   * 
   * @return depth of constraint, in km
   * @throws Exception
   */
  protected double getSeismicityDepthRange() throws Exception {
    return eventParameters.getSeismicityDepthRange(getUnitVector())[fixedDepthIndex];
  }

  /**
   * Number of times sum squared weighted residuals are compute. Value is updated in method
   * update().
   * 
   * @return
   */
  public int getnSSWR() {
    return nSSWR;
  }

  public EventParameters getEventParameters() {
    return eventParameters;
  }

  public Source getSource() {
    return source;
  }

  /**
   * Correlations specifies the correlation coefficient between two observations. Each String is
   * composed of station name/phase/attribute where attribute is one of [ TT, AZ, SH ]. An example
   * of an entry in this map would be: <br>
   * ASAR/Pg/TT -> WRA/Pg/TT -> 0.5 <br>
   * Coefficient values must be in the range [ -1 to 1 ]
   * 
   * @return correlation map
   */
  public Map<String, Map<String, Double>> getCorrelationCoefficients() {
    return correlationCoefficients;
  }

  /**
   * Correlations specifies the correlation coefficient between two observations. Each String is
   * composed of station name/phase/attribute where attribute is one of [ TT, AZ, SH ]. An example
   * of an entry in this map would be: <br>
   * ASAR/Pg/TT -> WRA/Pg/TT -> 0.5 <br>
   * Coefficient values must be in the range [ -1 to 1 ]
   * 
   * @return correlation map
   */
  public void setCorrelationCoefficients(Map<String, Map<String, Double>> correlationCoefficients) {
    this.correlationCoefficients = correlationCoefficients;
  }

  /**
   * One of FIRST, INTERIM, LAST, FIXED, SIMPLEX, FINAL, DAMPED
   */
  enum LocatorStatus {
    START, INTERIM, END, FIXED, SIMPLEX, FINAL, DAMPED
  }

  /**
   * Return fraction of the defining observations that have phase included in the specified
   * collection of phases.
   * 
   * @param phases
   * @return
   */
  double fractionalContent(EnumSet<SeismicPhase> phases) {
    int n = 0, ndef = 0;
    for (Observation obs : source.getObservations().values())
      if (obs.isDefining()) {
        ++ndef;
        if (phases.contains(obs.getPhase()))
          ++n;
      }
    return n == 0 ? 0. : n == ndef ? 1. : (double) n / (double) ndef;
  }

  /**
   * Returns true if all defining observations have phase = I
   * 
   * @return
   */
  boolean isInfrasountEvent() {
    for (Observation obs : source.getObservations().values())
      if (obs.isDefining() && obs.getPhase() != SeismicPhase.I)
        return false;
    return true;
  }

  void resetFlipFlop() {
    for (Observation obs : source.getObservations().values())
      obs.resetFlipFlop();
  }

  public long getSourceId() {
    return source.getSourceId();
  }

  /**
   * Amount of time in nanoseconds spent computing predictions in method update
   */
  public long getPredictionTime() {
    return predictionTime;
  }
}
