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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.seismicitydepth.SeismicityDepthModel;
import gov.sandia.gmp.locoo3d.EventList.CorrelationMethod;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

/**
 * @author Benjamin Lawry (bjlawry@sandia.gov) created on 07/07/2022
 *
 */
public class EventParameters implements Serializable{
  private static final long serialVersionUID = 1L;
  private PropertiesPlusGMP properties;
  private boolean debugCorrelatedObservations;
  private boolean useTTPathCorrections;
  private boolean useShPathCorrections;
  private boolean useAzPathCorrections;
  private boolean useTTModelUncertainty;
  private boolean useAzModelUncertainty;
  private boolean useShModelUncertainty;
  private boolean[] fixed;
  private double fixedDepthValue;
  private int fixedDepthIndex;
  private double[] seismicityDepthMinMax;
  private transient GeoTessPosition seismicityDepthModel;
  private int seismicityDepthMinIndex;
  private int seismicityDepthMaxIndex;
  private int splitSizeNDef;
  private double depthConstraintUncertaintyScale;
  private double depthConstraintUncertaintyOffset;
  private CorrelationMethod correlationMethod;
  private double correlationScale;
  private Map<String, Map<String, Double>> correlations;
  private boolean allowBigResiduals;
  private double bigResidualThreshold;
  private double bigResidualMaxFraction;
  private String eInitialLocationMethod;
  private EnumSet<SeismicPhase> definingPhases;
  private EnumSet<GeoAttributes> definingAttributes;
  private Location parFileLocation;
  private Set<String> definingStations;
  private ObservationFilter observationFilter;
  private boolean needDerivatives;
  private transient ExecutorService predictionsThreadPool;
  
  /**
   * The number of times an observation can change from defining to non-defining before it is set
   * non-defining permanently.
   */
  private int nObservationFlipFlops;
  private boolean allowCorePhaseRenamingP;
  private double corePhaseRenamingThresholdDistanceP;
  private boolean useSimplex;
  private ScreenWriterOutput outputLog, errorLog;
  private String predictorPrefix;
  private transient PredictorFactory predictorFactory;

  public EventParameters(PropertiesPlusGMP ps, String predictorPrefix, ExecutorService predThreads, 
      ScreenWriterOutput ol, ScreenWriterOutput el) throws Exception {
    properties = ps;
    definingPhases = EnumSet.noneOf(SeismicPhase.class);
    definingAttributes = EnumSet.noneOf(GeoAttributes.class);
    outputLog = ol;
    errorLog = el;
    predictorFactory = new PredictorFactory(ps,predictorPrefix);
    seismicityDepthModel = null;
    needDerivatives = true;
    predictionsThreadPool = predThreads;
    setup();
  }
  
  private GeoTessPosition initSeismicityDepthModel() throws GMPException{
    if(seismicityDepthModel != null) return seismicityDepthModel;
    
    synchronized(this) {
      if(seismicityDepthModel == null) {
        String gen_fix_depth = properties.getProperty("gen_fix_depth", "false").toLowerCase();
        
        if (gen_fix_depth.startsWith("topo")) {
          try {
            seismicityDepthModel =
                SeismicityDepthModel.getGeoTessPosition(properties.getProperty("seismicity_depth_model"));
          } catch (Exception e) {
            throw new GMPException(e);
          }
          
          if (seismicityDepthModel == null)
            throw new GMPException(
                "Unable to load seismicity depth model because property seismicity_depth_model = "
                    + properties.getProperty("seismicity_depth_model") + "\n"
                    + "To use the default seismicity depth model specify property: seismicity_depth_model = default");
        }
        
        // if property seismicityDepthModel is specified, then the seismicity depth model will be
        // loaded from the
        // the specified file. If seismicityDepthModel is 'default'then the default model
        // will be loaded from the internal resources directory. Otherwise, the seismicityDepthModel
        // is null

        if (seismicityDepthModel != null) {
          seismicityDepthMinIndex =
              seismicityDepthModel.getModel().getMetaData().getAttributeIndex("SEISMICITY_DEPTH_MIN");
          seismicityDepthMaxIndex =
              seismicityDepthModel.getModel().getMetaData().getAttributeIndex("SEISMICITY_DEPTH_MAX");
        }
      }
    }
    
    return seismicityDepthModel;
  }

  private void setup() throws Exception {
    splitSizeNDef = properties.getInt(PROP_SPLIT_SIZE,Integer.MAX_VALUE);
    
    fixed = new boolean[] {properties.getBoolean("gen_fix_lat_lon", false),
        properties.getBoolean("gen_fix_lat_lon", false), false,
        properties.getBoolean("gen_fix_origin_time", false),};

    // property gen_fix_depth can be true, false, 'topo', or a double
    // if double, the value is depth to which event depths are to be fixed.
    // if topo, load the seismicity depth model and fix depth to topo surface.
    String gen_fix_depth = properties.getProperty("gen_fix_depth", "false").toLowerCase();

    fixedDepthValue = Double.NaN;
    fixedDepthIndex = -1;
    depthConstraintUncertaintyScale = 0.;
    depthConstraintUncertaintyOffset = 0.;

    if (gen_fix_depth.startsWith("topo")) {
      fixedDepthIndex = 0;
      fixed[GMPGlobals.DEPTH] = true;
    } else {
      try {
        // try and parse gen_fix_depth as a double
        fixedDepthValue = Double.parseDouble(gen_fix_depth);
        fixed[GMPGlobals.DEPTH] = true;
      } catch (NumberFormatException ex) {
        if (gen_fix_depth.equals("true"))
          fixed[GMPGlobals.DEPTH] = true;
        else if (gen_fix_depth.equals("false"))
          fixed[GMPGlobals.DEPTH] = false;
        else
          throw new Exception("gen_fix_depth = " + gen_fix_depth
              + " is not a recognized value.  Must equal false, true, topography, or a floating point value.");
      }
    }

    if (!fixed[GMPGlobals.DEPTH]) {
      // free depth solutions will be computed. Determine the depth range to which
      // computed depths will be constrained.

      // default depth range is 0 to 700 km
      seismicityDepthMinMax = new double[] {properties.getDouble("gen_min_depth", 0.),
          properties.getDouble("gen_max_depth", 700.)};

      depthConstraintUncertaintyScale = properties.getDouble("depthConstraintUncertainyScale", 0.);
      depthConstraintUncertaintyOffset = properties.getDouble("depthConstraintUncertainyOffset", 0.);
    }

    eInitialLocationMethod =
        properties.getProperty("gen_initial_location_method", "data_file").toLowerCase();

    if (eInitialLocationMethod.toLowerCase().startsWith("properties")) {
      parFileLocation = null;
      double lat = properties.getDouble("gen_lat_init", Globals.NA_VALUE);
      if (lat >= -90.) {
        double lon = properties.getDouble("gen_lon_init", Globals.NA_VALUE);
        if (lon > -360.) {
          double depth = properties.getDouble("gen_depth_init", Globals.NA_VALUE);

          if (depth != Globals.NA_VALUE) {
            double etime = properties.getDouble("gen_origin_time_init", Globals.NA_VALUE);

            if (etime != Globals.NA_VALUE) {
              parFileLocation = new Location(lat, lon, depth, true, etime);
            }
          }
        }
      }

      if (parFileLocation == null) {
        StringBuffer buf = new StringBuffer();
        buf.append(String.format("Unable to read initial location from properties file.%n"));
        buf.append(String.format("%s = %s%n", "gen_lat_init",
            properties.getProperty("gen_lat_init", "-999.")));
        buf.append(String.format("%s = %s%n", "gen_lon_init",
            properties.getProperty("gen_lon_init", "-999.")));
        buf.append(String.format("%s = %s%n", "gen_depth_init",
            properties.getProperty("gen_depth_init", "-999.")));
        buf.append(String.format("%s = %s%n", "gen_origin_time_init",
            properties.getProperty("gen_origin_time_init", "-999999.")));

        throw new LocOOException(buf.toString());
      }
    }

    correlationMethod = CorrelationMethod.valueOf(
        properties.getProperty("gen_correlation_matrix_method", "uncorrelated").toUpperCase());
    if (correlationMethod == CorrelationMethod.FUNCTION) {
      correlationScale = properties.getDouble("gen_correlation_scale", 10.);
      if (correlationScale < 1e-30)
        correlationMethod = CorrelationMethod.UNCORRELATED;
    } else
      correlationScale = Globals.NA_VALUE;

    if (correlationMethod == CorrelationMethod.FILE)
      readCorrelationData();

    if (properties.containsKey("debugCorrelatedObservations"))
      debugCorrelatedObservations = properties.getBoolean("debugCorrelatedObservations", false);

    allowBigResiduals = properties.getBoolean("gen_allow_big_residuals", true);
    bigResidualThreshold = properties.getDouble("gen_big_residual_threshold", 3.);
    bigResidualMaxFraction = properties.getDouble("gen_big_residual_max_fraction", 0.2);

    nObservationFlipFlops = properties.getInt("nObservationFlipFlops", 10);

    observationFilter =
        new ObservationFilter(properties.getProperty("gen_defining_observations_filter"));

    // if definingStations is empty, it means accept all stations.
    // otherwise accept only stations in the Set.
    definingStations = new TreeSet<String>();
    String defining = properties.getProperty("gen_defining_stations");
    if (defining != null)
      for (String sta : defining.split(","))
        definingStations.add(sta.trim());

    defining = properties.getProperty("gen_defining_phases");
    if (defining == null)
      definingPhases = EnumSet.allOf(SeismicPhase.class);
    else {
      definingPhases = EnumSet.noneOf(SeismicPhase.class);
      for (String phase : defining.split(",")) {
        SeismicPhase ph = SeismicPhase.valueOf(phase.trim());
        if (ph == null)
          errorLog.write(String.format("WARNING: property gen_defining_phases = %s includes "
              + "unrecognized phase %s.  It is being ignored.", defining, phase));
        else
          definingPhases.add(ph);
      }
    }

    defining = properties.getProperty("gen_defining_attributes");
    if ((defining == null) || (defining.toLowerCase().trim().equals("all")))
      definingAttributes =
          EnumSet.of(GeoAttributes.TRAVEL_TIME, GeoAttributes.AZIMUTH, GeoAttributes.SLOWNESS);
    else {
      definingAttributes = EnumSet.noneOf(GeoAttributes.class);
      for (String attribute : defining.replaceAll(",", " ").split("\\s+"))
        if (attribute.toLowerCase().trim().startsWith("t"))
          definingAttributes.add(GeoAttributes.TRAVEL_TIME);
        else if (attribute.toLowerCase().trim().startsWith("a"))
          definingAttributes.add(GeoAttributes.AZIMUTH);
        else if (attribute.toLowerCase().trim().startsWith("s"))
          definingAttributes.add(GeoAttributes.SLOWNESS);
        else
          errorLog.write(String.format("WARNING: property gen_defining_attributes = %s includes "
              + "unrecognized attribute %s.  It is being ignored.  "
              + "Attributes must start with 't', 'a', or 's'", defining, attribute));

      if (definingAttributes.isEmpty())
        throw new LocOOException("Property gen_defining_attributes = " + defining
            + " resulted in 0 defining attributes.");
    }

    useTTPathCorrections = properties.getBoolean("use_tt_path_corrections", true);
    useShPathCorrections = properties.getBoolean("use_sh_path_corrections", true);
    useAzPathCorrections = properties.getBoolean("use_az_path_corrections", true);

    useTTModelUncertainty = properties.getBoolean("use_tt_model_uncertainty", true);
    useAzModelUncertainty = properties.getBoolean("use_az_model_uncertainty", true);
    useShModelUncertainty = properties.getBoolean("use_sh_model_uncertainty", true);

    allowCorePhaseRenamingP = properties.getBoolean("allowCorePhaseRenamingP", false);
    if (allowCorePhaseRenamingP)
      corePhaseRenamingThresholdDistanceP =
          properties.getDouble("corePhaseRenamingThresholdDistanceP", 110.);

    useSimplex = properties.getBoolean("useSimplex", false);
  }

  private void readCorrelationData() throws LocOOException, PropertiesPlusException, IOException {
    if (!properties.containsKey("gen_correlation_matrix_file"))
      throw new LocOOException(
          "gen_correlation_matrix_method = file but gen_correlation_matrix_file is not specified in properties file.");

    String staPhaseType1, staPhaseType2;
    Double correlation;
    Map<String, Double> values;

    correlations = new LinkedHashMap<String, Map<String, Double>>();

    Scanner input = new Scanner(properties.getFile("gen_correlation_matrix_file"));
    while (input.hasNext()) {
      staPhaseType1 = input.next();
      staPhaseType2 = input.next();
      correlation = input.nextDouble();

      // ensure that last two characters are upper case
      staPhaseType1 = staPhaseType1.substring(0, staPhaseType1.length() - 2)
          + staPhaseType1.substring(staPhaseType1.length() - 2).toUpperCase();
      staPhaseType2 = staPhaseType2.substring(0, staPhaseType2.length() - 2)
          + staPhaseType2.substring(staPhaseType2.length() - 2).toUpperCase();

      if (Math.abs(correlation) > 1)
        throw new LocOOException(String.format(
            "%nError reading correlation data from file "
                + properties.getFile("gen_correlation_matrix_file").getCanonicalPath()
                + "%nAttempting to set %s -> %s = %1.6f%n"
                + "but correlation coefficients must be between -1 and 1.%n",
            staPhaseType1, staPhaseType2, correlation));

      values = correlations.computeIfAbsent(staPhaseType1, k -> new LinkedHashMap<>());
      values.put(staPhaseType2, correlation);

      values = correlations.get(staPhaseType2);
      if (values != null && values.get(staPhaseType1) != null
          && !values.get(staPhaseType1).equals(correlation))
        throw new LocOOException(String.format(
            "%nError reading correlation data from file "
                + properties.getFile("gen_correlation_matrix_file").getCanonicalPath()
                + "%nAttempting to set %s -> %s = %1.6f and %s -> %s = %1.6f%n"
                + "but matrix of correlation coefficients must be symmetric.%n",
            staPhaseType1, staPhaseType2, correlation, staPhaseType2, staPhaseType1,
            values.get(staPhaseType1)));

    }
    input.close();
  }
  
  public boolean allowBigResiduals() { return allowBigResiduals; }
  
  public double bigResidualMaxFraction() { return bigResidualMaxFraction; }
  
  public double bigResidualThreshold() { return bigResidualThreshold; }
  
  public CorrelationMethod correlationMethod() { return correlationMethod; }
  
  public Map<String,Map<String,Double>> correlations(){ return correlations; }
  
  public double correlationScale() { return correlationScale; }
  
  public double corePhaseRenamingThresholdDistanceP() {
    return corePhaseRenamingThresholdDistanceP;
  }
  
  public boolean debugCorrelatedObservations() { return debugCorrelatedObservations; }

  public ScreenWriterOutput errorLog() { return errorLog; }
  
  public EnumSet<GeoAttributes> definingAttributes(){ return definingAttributes; }
  
  public EnumSet<SeismicPhase> definingPhases(){ return definingPhases; }
  
  public Set<String> definingStations(){ return definingStations; }
  
  public double depthConstraintUncertaintyOffset() { return depthConstraintUncertaintyOffset; }
  
  public double depthConstraintUncertaintyScale() { return depthConstraintUncertaintyScale; }

  public boolean[] fixed() { return Arrays.copyOf(fixed, fixed.length); }
  
  public int fixedDepthIndex() { return fixedDepthIndex; }
  
  public double fixedDepthValue() { return fixedDepthValue; }
  
  public double[] getSeismicityDepthRange(double[] location) throws GMPException {
    GeoTessPosition seismicityDepthModel = initSeismicityDepthModel();
    
    if (seismicityDepthModel == null)
      return seismicityDepthMinMax;
    else {
      try {
        seismicityDepthModel.set(location, 6371.);
        return new double[] {seismicityDepthModel.getValue(seismicityDepthMinIndex),
            seismicityDepthModel.getValue(seismicityDepthMaxIndex)};
      } catch (GeoTessException e) {
        throw new GMPException(e);
      }
    }
  }
  
  public String initialLocationMethod() { return eInitialLocationMethod; }
  
  public boolean isFree(int i) { return !fixed[i]; }
  
  public boolean needDerivatives() { return needDerivatives; }
  
  public EventParameters needDerivatives(boolean n) { needDerivatives = n; return this; }
  
  public ObservationFilter observationFilter() { return observationFilter; }
  
  public int observationFlipFlops() { return nObservationFlipFlops; }

  public ScreenWriterOutput outputLog() { return outputLog; }
  
  public Location parFileLocation() { return parFileLocation; }
  
  public ExecutorService predictionsThreadPool() { return predictionsThreadPool; }
  
  public PredictorFactory predictorFactory() {
    if(predictorFactory != null) return predictorFactory;
    
    synchronized(this) {
      if(predictorFactory == null) {
        try {
          predictorFactory = new PredictorFactory(properties(),predictorPrefix);
        } catch (Exception e) {
          errorLog.write(e);
        }
      }
    }
    
    return predictorFactory;
  }
  
  public PropertiesPlusGMP properties() { return properties; }
  
  public int splitSizeNDef() { return splitSizeNDef; }
  
  public boolean useAzModelUncertainty() { return useAzModelUncertainty; }
  
  public boolean useAzPathCorrections() { return useAzPathCorrections; }
  
  public boolean useShModelUncertainty() { return useShModelUncertainty; }
  
  public boolean useShPathCorrections() { return useShPathCorrections; }
  
  public boolean useSimplex() { return useSimplex; }
  
  public boolean useTTModelUncertainty() { return useTTModelUncertainty; }
  
  public boolean useTTPathCorrections() { return useTTPathCorrections; }
  
  public static final String PROP_SPLIT_SIZE = "splitSizeNdef";
}
