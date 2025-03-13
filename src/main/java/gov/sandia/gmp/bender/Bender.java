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
package gov.sandia.gmp.bender;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ChangeListener;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.geotess.GradientCalculator;
import gov.sandia.geotess.extensions.siteterms.GeoTessModelSiteData;
import gov.sandia.gmp.baseobjects.EllipticityCorrections;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.EarthInterface;
import gov.sandia.gmp.baseobjects.globals.EarthInterfaceGroup;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintyAzimuth;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintyDistanceDependent;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintyNAValue;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintySlowness;
import gov.sandia.gmp.baseobjects.uncertainty.UncertaintySourceDependent;
import gov.sandia.gmp.bender.BenderConstants.GradientCalculationMode;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;
import gov.sandia.gmp.bender.BenderConstants.RayStatus;
import gov.sandia.gmp.bender.BenderConstants.SearchMethod;
import gov.sandia.gmp.bender.BenderException.ErrorCode;
import gov.sandia.gmp.bender.phase.PhaseLayerLevelDefinition;
import gov.sandia.gmp.bender.phase.PhaseRayBranchModel;
import gov.sandia.gmp.bender.phase.PhaseWaveTypeModel;
import gov.sandia.gmp.bender.phase.TauPPhaseBottomSideReflection;
import gov.sandia.gmp.bender.ray.Ray;
import gov.sandia.gmp.bender.ray.RayBranch;
import gov.sandia.gmp.bender.ray.RayInfo;
import gov.sandia.gmp.bender.ray.RaySegment;
import gov.sandia.gmp.bender.ray.RaySegmentBottom;
import gov.sandia.gmp.util.changenotifier.ChangeNotifier;
import gov.sandia.gmp.util.containers.Tuple;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.io.FileAttributes;
import gov.sandia.gmp.util.io.GlobalInputStreamProvider;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.brents.Brents;
import gov.sandia.gmp.util.numerical.brents.BrentsFunction;
import gov.sandia.gmp.util.numerical.simplex.Simplex;
import gov.sandia.gmp.util.numerical.simplex.SimplexFunction;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gmp.util.propertiesplus.Property;

/**
 * <p>
 * Seismic ray tracing using the bending algorithm of Um and Thurber (1987)
 * modified to enforce Snell's Law at velocity discontinuities as described
 * by Zhao et al. (2004) but modified somewhat as described in Ballard et al.
 * SRL, in press.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
public class Bender extends Predictor implements BrentsFunction, SimplexFunction
{
  /**
	 * Return the current version number.
	 * 
	 * @return String
	 */
	public static String getVersion()
	{
		return Utils.getVersion("bender");

		// 4.2.2 2021-2-9 Fixed bug causing Bender to returning OUT_OF_PLANE values
		// with no units (unit vectors).
		//
		// 4.2.1 2020-9-5 Primarily updates tot he pom file.
		//
		// 4.1.0 2016-11-7 Finalized Under-side Reflection optimization methods
		// that robustly discover XX and xX phase bounce point locations. Also, have
		// run a predictions test that categorizes error types for each phase along
		// with travel time residual histograms that give us significant confidence
		// on the overall result.
		//
		// 4.0.1 2016-6-1 Added Underside Reflection ability to join multiple Ray
		// Branches together to form a single Ray. This feature allows Bender to
		// trace complex reflections (e.g. pP, PP, PPP, etc.). In addition, Bender
		// Earth model dependence was converted from GeoModel to GeoTessModel.
		//
		// 3.8.1 2014-12-10 Added ability to apply multiplier and offset to 
		// tt model uncertainty .  
		//		
		// 3.8.0  Added ability to change the size of the tetrahedron used
		// to compute velocity gradients.  
		//		
		// 3.7.0 2011-08-20 switched to using precomputed velocity gradients
		// stored on geomodel instead of velocity gradients computed on-the-fly
		// by Bender.  Added phase Pg based on MIDDLE_CRUST_G layer copied from
		// SLBM.
		//		
		// 3.6.8 2011-06-22 bender did not return GeoAttributes.BACKAZIMUTH_DEGREES
		//		
		// 3.6.7 2011-06-08 changed ray path calculations in RayInfo to enforce
		// points on major layer boundaries
		// 
		// 3.6.6 2011-05-02 changed it so expects sedimentary layer
		// to have sedimentary sized pslowness values.
		//		
		// 3.6.5 2011-04-21 fixed bug where allowCMBDiffraction was not being
		// copied in getCopy() which means it was not set properly when 
		// Prediction was running in concurrent mode.
		//
		// also fixed bug where GeoAttributes.DSH_DX was not being recognized.
		//
		// 3.6.4 2011-02-18  minor changes RaySegment.doubleNodes(threshold) to
		// improve robustness.  Also in RaySegment.checkReflection().
		//
		// 3.6.3 2011-01-25  in Ray.SnellsLaw() now assume that zero thickness
		// layers have a valid slowness value in them.  This was done to prevent
		// the ray from jumping around in rare situations when the ray moved
		// between horizontal positions where the top layer was 'thin' and 
		// 'not thin'.
		//
		// 3.6.2 2010-06-14 added a check in RaySegment.bend3nodes() to ensure 
		// that middle node of bottom raysegment is at least as deep as the top 
		// of the layer in which it is ultimately going to end up.
		//
		// 3.6.1 2010-06-08
		//   Changes to support modifications in GeoModelUUL where velocity
		//   gradient information is precomputed and stored in the model.
		//
		// 3.6.0  2010-02-05  
		//    added SearchMethod.AUTO so that during Snell's Law, bender will 
		//    check to see what the angle is between normal to interface and 
		//    normal to great circle plane and use simplex if angle is big
		//    or brents is angle is small.  Always uses brents when bottom
		//    layer is M410 or below.
	}
	
	/**
	 * A map of File name to GeoTessModel. 
	 */
	private static Map<String, GeoTessModel> geotessModels = new LinkedHashMap<>();

	/**
	 * Return the name of this algorithm: Bender.
	 * 
	 * @return String
	 */
	public static final String getAlgorithmName()
	{
		return "Bender";
	}
	
	public void  close() throws Exception {
		super.close();
	}

	
//
//	private HashSet<Integer> bouncePointIgnoreSnellsLawLayers =
//					new HashSet<Integer>();
//
//	private void addBouncePointIgnoreSnellsLawLayer(int layer)
//	{
//		bouncePointIgnoreSnellsLawLayers.add(layer);
//	}
//
//	public boolean isLayerBouncePointIgnoreSnellsLaw(int layer)
//	{
//		return bouncePointIgnoreSnellsLawLayers.contains(layer);
//	}

	private class PhaseLayerChange {
		private ArrayList<Tuple<RayDirection, EarthInterface>> rayBrnchDirChngList;
		private ArrayList<Tuple<EarthInterface, Integer>> waveSpeedInterfaceChngList;
		
		private PhaseLayerChange() {
			this.rayBrnchDirChngList = new ArrayList<Tuple<RayDirection, EarthInterface>>();
			this.waveSpeedInterfaceChngList = new ArrayList<Tuple<EarthInterface, Integer>>();
		}
	}
	
	/**
	 * Map of seisimic phase to PhaseLayerChange object. Used to avoid doing this more than once for
	 * each phase.
	 */
	private HashMap<SeismicPhase, PhaseLayerChange> phaseLayerChange =
			new HashMap<SeismicPhase, PhaseLayerChange>(); 

	/**
	 * The model interfaces validation object that validates the layer
	 * description for the GeoTessModel assigned to this Bender predictor.
	 */
	private BenderModelInterfaces benderModelInterfaces = null;
	
	/**
	 * If true then any layer through which the layer passes that is thinner than
	 * the minimum allowed layer thickness (BenderConstants.MIN_LAYER_THICKNESS) is
	 * removed from the ray segment definition.
	 */
	private boolean        removeThinLayers = false;

	/**
	 * Sets the remove thin layers flag to the input value rtl.
	 * 
	 * @param rtl The new removeThinLayers setting.
	 */
	public void setRemoveThinLayers(boolean rtl)
	{
		removeThinLayers = rtl;
	}

	/**
	 * Returns the setting of the removeThinLayers flag.
	 *  
	 * @return The setting of the removeThinLayers flag.
	 */
	public boolean removeThinLayers()
	{
		return removeThinLayers;
	}

	/**
	 * If true then any layer that has been removed by either of the above two
	 * booleans will be added back into the layer segment definition if the point
	 * where the ray transits the layer is thicker than 2 * the minimum allowed
	 * layer thickness (2 * BenderConstants.MIN_LAYER_THICKNESS). This can
	 * happen for cases where the ray moves enough (typically with bounce points)
	 * that it begins to transit a portion of a layer, that was thin at the
	 * onset of optimization, but becomes thicker as the ray moves away from its
	 * starting location. If both removeSuperCrustalLayers and removeThinLayers
	 * are set to false then no layers are ever removed and thus this flag will
	 * have no affect. Likewise, if either of the two removal flags are set to
	 * true (or both) and this flag is set to false, then once removed the layer
	 * stays out of the ray definition for the remainder of the optimization
	 * calculation.
	 */
	private boolean        addThickLayers = false;

	/**
	 * Sets the add thick layers flag to the input value atl.
	 * 
	 * @param rtl The new addThickLayers setting.
	 */
	public void setAddThickLayers(boolean atl)
	{
		addThickLayers = atl;
	}

	public double getMinOuterIterSegmentAddThickness(int outerIter)
	{
		switch (outerIter)
		{
		  case 1:
		  	return 100.0 * BenderConstants.MIN_LAYER_THICKNESS;
		  case 2:
		  	return 10.0 * BenderConstants.MIN_LAYER_THICKNESS;
		  default:
		  	return 2.0 * BenderConstants.MIN_LAYER_THICKNESS;
		}
	}

	/**
	 * Returns the setting of the addThickLayers flag.
	 *  
	 * @return The setting of the addThickLayers flag.
	 */
	public boolean addThickLayers()
	{
		return addThickLayers;
	}

//	/**
//	 * Defines the top layer of the input model below which all layers are
//	 * considered to be thick, and therefore, not subject to removal. This
//	 * interface can be set using the setMaximumEarthInterfaceNotThinLayer(...)
//	 * method below. The value default to the crust top.
//	 */
//	private EarthInterface maxEarthInterfaceNotThinLayer = EarthInterface.CRUST_TOP;
//
//	/**
//	 * The maximum model layer below which the model is termed to possess only
//	 * thick regions that are not subject to removal. This value corresponds to
//	 * the model index for the layer and is set automatically at the start of
//	 * each computeFastRays(...) calculation.
//	 */
//	private int					 maxModelNotThinLayer = -1;
//
//	/**
//	 * Retrieve the maximum GeoTessModel layer below which the model is termed to
//	 * possess only thick regions that are not subject to removal.
//	 * 
//	 * @return The maximum model not-thin layer index.
//	 */
//	public int getMaximumModelNotThinLayer()
//	{
//		return maxModelNotThinLayer;
//	}

//	/**
//	 * Sets the top EarthInterface layer below which all layers are
//	 * considered to be thick, and therefore, not subject to removal.
//	 * @param layer The new maximum EarthInterface not-thin layer.
//	 */
//	public void setMaximumEarthInterfaceNotThinLayer(EarthInterface layer)
//	{
//		maxEarthInterfaceNotThinLayer = layer;
//	}
////
//	public void addBouncePointIgnoreSnellsLawLayers(GeoTessMetaData md) throws GMPException
//	{
//		bouncePointIgnoreSnellsLawLayers.clear();
//		for (int i = 0; i < md.getNLayers(); ++i)
//		{
//			// get valid EarthInterface layer name
//
//			String name = md.getLayerName(i);
//			String validName = EarthInterface.getRepairedEarthInterfaceName(name);
//			if (validName == null)
//				throw new GMPException("Invalid Non-Repairable GeoTessMetaData Layer Name: " + name + " ...");
//
//			// if the input model interface name ordinal exceeds the maximum defined
//			// EarthInterface not-thin layer ordinal and the current model not-thin
//			// layer setting is greater than the ith layer being tested then set
//			// the max model not-thin layer to i
//
//			if (EarthInterface.valueOf(validName).ordinal() >
//					 maxEarthInterfaceNotThinLayer.ordinal())
//				bouncePointIgnoreSnellsLawLayers.add(i);
//		}		
//	}
//
//	/**
//	 * Sets the maximum GeoTessModel not thin layer index given the input
//	 * GeoTessMetaData object that exceeds the input EarthInterface layer name
//	 * designated as the maximum layer that is never pinched out in the model.
//	 * Typically in an EarthInterface specification layers from CRUST_TOP and
//	 * lower are never pinched out. Higher layers such as the SEDIMENTARY_LAYER_*,
//	 * SURFACE, ICE_TOP, and WATER_TOP, can all have regions where they have zero
//	 * thickness. If no layer can be pinched out then md.getNLayers() is returned.
//	 * 
//	 * @param md The GeoTessMetaData object of some GeoTessModel whose minimum
//	 *           layer index larger than or equal to the defined maximum
//	 *           EarthInterface not-thin layer (maxEarthInterfaceNotThinLayer)
//	 *           will be returned as the minimum model not-thin layer index.
//	 * @return The maximum not-thin layer id of the input model metadata that is
//	 * 				 equal to or greater than the maximum non-thin EarthInterface layer.
//	 * 				 If no layer can be pinched out then md.getNLayers() is returned.
//	 * @throws IOException
//	 */
//	public void setMaximumModelNotThinLayer(GeoTessMetaData md) throws GMPException
//	{
//		// set max model not-thin layer to getNLayers() and loop over all layers
//		// to find max model not-thin layer.
//
//		maxModelNotThinLayer = md.getNLayers();
//		for (int i = 0; i < md.getNLayers(); ++i)
//		{
//			// get valid EarthInterface layer name
//
//			String name = md.getLayerName(i);
//			String validName = EarthInterface.getRepairedEarthInterfaceName(name);
//			if (validName == null)
//				throw new GMPException("Invalid Non-Repairable GeoTessMetaData Layer Name: " + name + " ...");
//
//			// if the input model interface name ordinal exceeds the maximum defined
//			// EarthInterface not-thin layer ordinal and the current model not-thin
//			// layer setting is greater than the ith layer being tested then set
//			// the max model not-thin layer to i
//
//			if ((EarthInterface.valueOf(validName).ordinal() >
//					 maxEarthInterfaceNotThinLayer.ordinal()) && (maxModelNotThinLayer > i ))
//				maxModelNotThinLayer = i;
//		}
//	}

	/**
	 * The GeoModel from which to interpolate nodes
	 */
  protected GeoTessModel geoTessModel;
  
  // Sets of Bender supported phases separated by P only, S only, and P and S.
	EnumSet<SeismicPhase> supportedPPhases = EnumSet.of(SeismicPhase.P, SeismicPhase.Pn, 
			SeismicPhase.Pdiff, SeismicPhase.Pg, SeismicPhase.pP, SeismicPhase.PP,
			SeismicPhase.PcP, SeismicPhase.PKPbc, SeismicPhase.PKPdf,
			SeismicPhase.PKP, SeismicPhase.PKiKP, SeismicPhase.Pdif);
	EnumSet<SeismicPhase> supportedSPhases = EnumSet.of(SeismicPhase.S, SeismicPhase.Sn, 
			SeismicPhase.Sdiff, SeismicPhase.Sg, SeismicPhase.Lg, SeismicPhase.sS,
			SeismicPhase.SS, SeismicPhase.ScS, SeismicPhase.SKSbc, SeismicPhase.SKSdf,
			SeismicPhase.SKS, SeismicPhase.SKiKS, SeismicPhase.Sdif);
	EnumSet<SeismicPhase> supportedPSPhases = EnumSet.of(SeismicPhase.ScP,
			SeismicPhase.PcS, SeismicPhase.sP, SeismicPhase.SP, SeismicPhase.pS,
			SeismicPhase.PS, SeismicPhase.SKPdf, SeismicPhase.SKP, SeismicPhase.SKiKP,
			SeismicPhase.PKSdf, SeismicPhase.PKS, SeismicPhase.PKiKS,
			SeismicPhase.SKPbc, SeismicPhase.PKSbc );
	
	private int verbosity = 0;

	protected ChangeNotifier changeNotifier;

	public ChangeNotifier getChangeNotifier()
	{
		return changeNotifier;
	}
	
	protected boolean accumulateNodeMovementStatistics = false;
  public boolean isNodeMovementStatisticsOn()
  {
  	return accumulateNodeMovementStatistics;
  }

	/**
	 * The RayInfo object calculalted by the last call to computeRayInfo().
	 */
	//X protected RayInfo[] rayInfo;
	protected RayInfo[] rayInfo;

	private boolean computingDerivative = false;

	/**
	 * Used to compute velocity gradients.
	 */
	//X private SmallTet tetrahedron;
	private GradientCalculator gradientCalculator;

	/**
	 * The size of the tetrahedron usd to compute velocity gradient, in km.
	 * Default value is 10 km.
	 */
	private double tetSize = 10.0;

	protected GradientCalculationMode gradientCalculatorMode = GradientCalculationMode.PRECOMPUTED;

	protected double  depthPhaseBottomTolerance = 0.0;
	
	public double getDepthPhaseBottomTolerance()
	{
		return depthPhaseBottomTolerance;
	}

	protected boolean precomputeGradients = true;

	protected double[] undersideReflectionFractions = null;
  protected double[] undersideReflectionLatLonvector = null;
  protected boolean  updateFromUndersideReflectionLatLonvector = false;

  protected boolean  mapUndersideReflections = false;
  
  public boolean updateUndersideReflections()
  {
  	return updateFromUndersideReflectionLatLonvector;
  }

  public double[] undersideReflectionVector()
  {
  	return undersideReflectionLatLonvector;
  }

	protected StringBuilder errorMessages = null;
	public StringBuilder getErrorMessages()
	{
		return errorMessages;
	}

	protected GeoVector currentSource = null;
	protected GeoVector currentReceiver = null;

	protected GeoTessPosition currentSourceProfile = null;
	protected GeoTessPosition currentReceiverProfile = null;
	protected double          sourceToReceiverDistance = -1.0;

	public GeoVector getSource()
	{
		return currentSource;
	}
	
	public GeoVector getReceiver()
	{
		return currentReceiver;
	}
	
	public GeoTessPosition getSourceProfile()
	{
		return currentSourceProfile;
	}
	
	public GeoTessPosition getReceiverProfile()
	{
		return currentReceiverProfile;
	}

	public PhaseLayerLevelDefinition getPhaseLayerLevelDefinition()
	{
		return phaseRayBranchModel.getFirstPhaseLayerLevelDefinition();
	}

	/**
	 * StringBuffer for storing log information.
	 */
	protected StringBuilder logBuffer = new StringBuilder();

	/**
	 * If the derivative of travel time with respect to slowness for each active
	 * node touched by a Ray object is to be computed, the calculation will be
	 * performed numerically by modifying the slowness of each active node, one
	 * at a time, by the specified slowness perturbation (in sec/km) and
	 * recalculating the travel time.
	 */
	public static final double slownessPerturbation = 1e-7;

	protected long calcOriginTime, timeToAbort;
	private static final long defaultMaxEllapsedTime = Math.round(120. * 1e3);
	protected long maxEllapsedTime = defaultMaxEllapsedTime;

	public long getMaxEllapsedTime()
	{
		return maxEllapsedTime;
	}

	public long getTimeToAbort()
	{
		return timeToAbort;
	}

	//X protected ArrayList<Ray> rays = new ArrayList<Ray>();
	protected ArrayList<Ray> rays = new ArrayList<Ray>();

	/**
	 * The outer loop convergence criteria used in Ray.optimize() An n x 3
	 * array. First number is source receiver separation in deg. Second number
	 * is convergence criteria (in seconds) for all distances less than first
	 * number. Third number is minimum point spacing on the ray (in km).
	 */
//	protected double[][] convergenceCriteria = new double[][] { { 180., 0.002,
//		30. } };

	protected double[] defaultConvgncCriteria = new double[] {0.002, 30.};
	protected double[] currentRayConvgncCriteria = defaultConvgncCriteria;
  protected HashMap<SeismicPhase, double[]> phaseSpecificConvgncCriteria =
  					new HashMap<SeismicPhase, double[]>();

  private double phaseLayerLevelThickness = 30.0;

  private SearchMethod searchMethod = SearchMethod.AUTO; 

	/**
	 * If this parameter is false, then RayType will be set to INVALID
	 * when type is DIFFRACTION and interface is ICB.
	 */
	private boolean allowICBDiffraction = false;

	/**
	 * If this parameter is false, then RayType will be set to INVALID
	 * when type is DIFFRACTION and interface is CMB.
	 */
	private boolean allowCMBDiffraction = false;

	/**
	 * If this parameter is false, then RayType will be set to INVALID
	 * when type is DIFFRACTION and the interface is MOHO.
	 */
  private boolean allowMOHODiffraction = false;

	long modelId = -1;
	long algorithmId = -1;

	protected EllipticityCorrections ellipticityCorrections = null;
	
	private boolean useTTSiteCorrections = true;
	//private boolean useAZSiteCorrections = true;
	//private boolean useSHSiteCorrections = true;
	
	/**
	 * The current seismic phase ray branch model.
	 */
	protected PhaseRayBranchModel    phaseRayBranchModel = null;

	public PhaseRayBranchModel getPhaseRayBranchModel()
	{
		return phaseRayBranchModel;
	}

	/**
	 * The current seismic phase wave type interface conversion model.
	 */
	protected PhaseWaveTypeModel phaseWaveTypeModel  = null;

	public PhaseWaveTypeModel getPhaseWaveTypeModel()
	{
		return phaseWaveTypeModel;
	}
	
	/**
	 * Map from station name to boolean.  This map overrides the setting of
	 * useTTSiteCorrections.  if a station appears in this list with value 
	 * true, site corrections will be applied if available.  Otherwise,
	 * if a station appears in this list with value false, station corrections
	 * will not be applied.  If a station does not appear in this map then
	 * useTTSiteCorrections will apply.
	 */
	private HashMap<String, Boolean> useTTSiteCorrectionsStationList = new HashMap<String, Boolean>();

	public static final int debugBranchOutputALL = -1;
	public static final int debugBranchBottomLevelFast = -2;

	/**
	 * The number of output lines appearing before the debug header information
	 * is output. This is controlled by RayBranch.getOptimizeIterationString().
	 */
	public int debugHeaderOutputCount = RayBranch.debugHeaderOutputLimit;

  /**
   * Defines which ray branches (by index) to output, and be set to ALL, or some
   * integer between 0 and the number of ray branches - 1. If set to a number
   * larger than the number of available branches no output will appear.
   */
	public int debugBranchIndexOutput = debugBranchOutputALL;
 
  /**
   * Defines which RayBranchBottom levels are output. This is ignored if the
   * output RayBranch is not a RayBranchBottom. Can be set to ALL, LevelFast,
   * or the index of some level. If set to an index larger than the number of
   * levels available no output will appear for RayBranchBottoms. If more than
   * one RayBranchBottoms are defined then the same level number will be output
   * for both. Usually, this is set in conjunction with debugBranchIndexOuput
   * so that only one level of one branch is output.
   */
	public int debugRayBranchBottomLevelOutput = debugBranchOutputALL; 
	//public int debugRayBranchBottomLevelOutput = 14;
	
	public Bender(PropertiesPlus properties) throws Exception {
		this(properties, null);
	}

	public Bender(PropertiesPlus properties, ScreenWriterOutput logger) throws Exception {
		super(properties);
		
		predictionsPerTask = properties.getInt("benderPredictionsPerTask", 50);

		File benderModelFile = properties.getFile(PROP_MODEL);
		
		geoTessModel = getGeoTessModel (benderModelFile);

		File polygonFile = properties.getFile("benderModelActiveNodePolygon");
		if (polygonFile != null) {
		  geoTessModel.setActiveRegion(polygonFile);
		}

		// validate the GeoTessModel layer interfaces for compatibility with Bender

		String modelInterfaceRemap = properties.getProperty(PROP_MODEL_LAYER_TO_EARTH_IFACE_MAP, "");
		benderModelInterfaces = new BenderModelInterfaces(geoTessModel.getMetaData(),
				modelInterfaceRemap);

		VectorGeo.setEarthShape(geoTessModel.getEarthShape());

		setProperties(properties);
		changeNotifier = new ChangeNotifier(this);

		String type = properties.getProperty(PROP_UNCERTAINTY_TYPE, 
				properties.getProperty("benderUncertaintyType", "DistanceDependent")).replaceAll("_", "");
		
		if (type.equalsIgnoreCase("SourceDependent")) {
		    super.getUncertaintyInterface().put(GeoAttributes.TRAVEL_TIME, 
			    new UncertaintySourceDependent(properties, "bender"));
		}
		else if (type.equalsIgnoreCase("DistanceDependent"))
		{
			if (!properties.containsKey(PROP_UNCERTAINTY_DIR) && benderModelFile.isDirectory())
				properties.setProperty(PROP_UNCERTAINTY_DIR, benderModelFile.getCanonicalPath());
			super.getUncertaintyInterface().put(GeoAttributes.TRAVEL_TIME,  
				new UncertaintyDistanceDependent(properties, "bender"));
		}
		else
		    super.getUncertaintyInterface().put(GeoAttributes.TRAVEL_TIME, new UncertaintyNAValue());
		
		
		if (!properties.containsKey("benderAzSloUncertaintyFile") &&
			benderModelFile != null && benderModelFile.exists() && benderModelFile.isDirectory()
			&& new File(benderModelFile, "azimuth_slowness_uncertainty.dat").exists()) {
		    File uncertaintyFile = new File(getModelFile(), "azimuth_slowness_uncertainty.dat");
		    uncertaintyInterfaces.put(GeoAttributes.AZIMUTH, new UncertaintyAzimuth(uncertaintyFile));
		    uncertaintyInterfaces.put(GeoAttributes.SLOWNESS, new UncertaintySlowness(uncertaintyFile));
		}	
		
		if (logger != null && logger.getVerbosity() > 0)
			logger.writef(getPredictorName()+" Predictor instantiated in %s%n", Globals.elapsedTime(constructorTimer));
	}

	private void setProperties(PropertiesPlus properties)
					throws Exception
	{
		gradientCalculatorMode = GradientCalculationMode.valueOf(properties.getProperty(
				PROP_GRADIENT_CALCULATOR, "PRECOMPUTED").toUpperCase());

		setGradientCalculator(gradientCalculatorMode);
		
		useTTSiteCorrections = properties.getBoolean(
				PROP_USE_TT_SITE_CORRECTIONS, true);

		clearUseTTSiteCorrectionsStationList();

		String stationList = properties.getProperty(PROP_USE_TT_SITE_CORRECTIONS_TRUE);
		if (stationList != null)
			setUseTTSiteCorrectionsStationList(true, stationList);

		stationList = properties.getProperty(PROP_USE_TT_SITE_CORRECTIONS_FALSE);
		if (stationList != null)
			setUseTTSiteCorrectionsStationList(false, stationList);

		tetSize = properties.getDouble(PROP_TET_SIZE, tetSize);

		if (gradientCalculatorMode == GradientCalculationMode.PRECOMPUTED
				&& properties.getProperty(PROP_PRECOMPUTE_GRADIENTS) != null)
			precomputeGradients = properties.getBoolean(
					PROP_PRECOMPUTE_GRADIENTS, false);

		if (properties.getProperty(PROP_ELLIPTICITY_CORR_DIR) != null)
			ellipticityCorrections = new EllipticityCorrections(
					properties.getFile(PROP_ELLIPTICITY_CORR_DIR));

		allowICBDiffraction = properties.getBoolean("allowICBDiffraction", 
			properties.getBoolean("benderAllowICBDiffraction", false));
			
		allowCMBDiffraction = properties.getBoolean("allowCMBDiffraction", 
			properties.getBoolean("benderAllowCMBDiffraction", false));
			
		allowMOHODiffraction = properties.getBoolean("allowMOHODiffraction", 
			properties.getBoolean("benderAllowMOHODiffraction", false));
			
		if (properties.getProperty(PROP_DEFAULT_TT_TOL) != null)
			defaultConvgncCriteria[0] = properties.getDouble(PROP_DEFAULT_TT_TOL);

		if (properties.getProperty(PROP_DEFAULT_MIN_NODE_SPACING) != null)
			defaultConvgncCriteria[1] = properties.getDouble(PROP_DEFAULT_MIN_NODE_SPACING);

		if (properties.getProperty(PROP_DEFAULT_CONVERGENCE_CRITERIA) != null)
		{
			String[] cc = Globals.getTokens(properties.getProperty(PROP_DEFAULT_CONVERGENCE_CRITERIA), "\t ,");
			if (cc.length != 2)
				throw new IOException(Globals.NL + "Error: Bender property " +
			                        "\"benderDefaultConvergenceCriteria = " +
			                        properties.getProperty(PROP_DEFAULT_CONVERGENCE_CRITERIA) +
			                        "\" must have two entries: " + Globals.NL +
			                        "       Travel Time Convergence Tolerance & " +
			                        "Minimum Node Spacing");
			defaultConvgncCriteria[0] = Double.valueOf(cc[0]);
			defaultConvgncCriteria[1] = Double.valueOf(cc[1]);
		}

		if (properties.getProperty(PROP_PHASE_SPECIFIC_CONV_CRIT) != null)
		{
			String[] phccEntries = Globals.getTokens(properties.getProperty(PROP_PHASE_SPECIFIC_CONV_CRIT), ";");
			for (String phcc: phccEntries)
			{
				String[] cc = Globals.getTokens(phcc, "\t ,");
				if (cc.length != 3)
					throw new IOException(Globals.NL + "Error: Bender property " +
              "\"benderPhaseSpecificConvergenceCriteria = " +
              properties.getProperty(PROP_PHASE_SPECIFIC_CONV_CRIT) +
              "\" must have three entries per phase: " + Globals.NL +
              "       \"Phase name\" \"Travel Time Convergence Tolerance\"" +
              " \"Minimum Node Spacing\"");

				double[] phaseCC = new double [2];
				phaseCC[0] = Double.valueOf(cc[1]);
				phaseCC[1] = Double.valueOf(cc[2]);
				phaseSpecificConvgncCriteria.put(SeismicPhase.valueOf(cc[0]),  phaseCC);
			}
		}

		if (properties.getProperty("depthPhaseBottomTolerance") != null)
			depthPhaseBottomTolerance = properties.getDouble("depthPhaseBottomTolerance");

		if (properties.getProperty(PROP_PHASE_LEVEL_THICKNESS) != null)
			phaseLayerLevelThickness = properties.getDouble(PROP_PHASE_LEVEL_THICKNESS);
		
		setSearchMethod(SearchMethod.valueOf(
				properties.getProperty(PROP_SEARCH_METHOD, "auto").toUpperCase()));
		
		setMaxCalcTime(properties.getDouble(PROP_MAX_CALC_TIME, defaultMaxEllapsedTime));
		
//	  if (properties.getProperty("benderOptimizeWithSnellsLaw") != null)
//	  	this.optimizeSnellsLaw = properties.getBoolean("benderOptimizeWithSnellsLaw", false);
	}

	@Override
	public boolean isSupported(Receiver receiver, SeismicPhase phase,
			GeoAttributes attribute, double epochTime) 
	{
		// see if P and or S slowness are defined.
		boolean hasPSlowness = geoTessModel.getMetaData().getNodeAttributes().
				isAttributeDefined(GeoAttributes.PSLOWNESS.name());
		boolean hasSSlowness = geoTessModel.getMetaData().getNodeAttributes().
				isAttributeDefined(GeoAttributes.SSLOWNESS.name());
		
		// return true if the phase is defined in a supported list with the necessary
		// model slowness present.
		return (hasPSlowness && supportedPPhases.contains(phase)) ||
				(hasSSlowness && supportedSPhases.contains(phase)) ||
				(hasPSlowness && hasSSlowness && supportedPSPhases.contains(phase));
//		AttributeDataDefinitions attrDataDef = geoTessModel.getMetaData().
//				                                                getNodeAttributes();
//		switch (phase)
//		{
//		case P:
//			return attrDataDef.isAttributeDefined(GeoAttributes.PSLOWNESS.name());
//		case Pn:
//			return attrDataDef.isAttributeDefined(GeoAttributes.PSLOWNESS.name());
//		case Pg:
//			return attrDataDef.isAttributeDefined(GeoAttributes.PSLOWNESS.name());
//		case Pdiff:
//			return attrDataDef.isAttributeDefined(GeoAttributes.PSLOWNESS.name());
//		case S:
//			return attrDataDef.isAttributeDefined(GeoAttributes.SSLOWNESS.name());
//		case Sn:
//			return attrDataDef.isAttributeDefined(GeoAttributes.SSLOWNESS.name());
//		case Lg:
//			return attrDataDef.isAttributeDefined(GeoAttributes.SSLOWNESS.name());
//		case Sdiff:
//			return attrDataDef.isAttributeDefined(GeoAttributes.SSLOWNESS.name());
//		default:
//			return false;
//		}
	}

	/**
	 * Add a visualization object that wants to be notified when the bender's
	 * Ray object changes state.
	 * 
	 * @param listener
	 *            ChangeListener
	 */
	public void addListener(ChangeListener listener)
	{
		changeNotifier.addListener(listener);
	}

	/**
	 * Set the verbosity level.
	 * 
	 * @param verbosity
	 *            int
	 *            <ol>
	 *            <li value="0">No output.
	 *            <li>Summary information after each Ray calculation
	 *            <li>Summary information at the conclusion of each outer loop.
	 *            <li>Summary information at the conclusion of each inner loop.
	 *            </ol>
	 */
	public void setVerbosity(int verbosity)
	{
		this.verbosity = verbosity;
	}

	public int getVerbosity()
	{
		if (computingDerivative)
			return 0;
		return verbosity;
	}

	/**
	 * Retrieve a reference to the GeoTessModelSiteData that supports this Bender.
	 * New function during transition to GeoTess.
	 * 
	 * @return GeoModelUUL
	 */
	public GeoTessModel getGeoTessModel()
	{
		return geoTessModel;
	}

	/**
	 * Set the maximum time time in seconds that a ray calculation is allowed to
	 * take. If a ray calculation takes longer than this, then the calculation
	 * is aborted and a BenderExceptionFatal is thrown. Default is 120 seconds.
	 * <p>
	 * <ul>
	 * <li>If maxCalcTime == null, then maxCalcTime is set to the default value.
	 * <li>If maxCalcTime < 0 or == Double.POSITIVE_INFINITY, then maxCalcTime
	 * is set to infinity.
	 * </ul>
	 * 
	 * @param maxCalcTime
	 *            Double maximum time calculation is allowed to take, in
	 *            seconds.
	 */
	public void setMaxCalcTime(Double maxCalcTime)
	{
		if (maxCalcTime == null)
			this.maxEllapsedTime = defaultMaxEllapsedTime;
		else if (maxCalcTime < 0. || maxCalcTime.isInfinite())
			this.maxEllapsedTime = -1;
		else
			this.maxEllapsedTime = (long) (maxCalcTime * 1e3);
	}

	/**
	 * @throws Exception 
	 * Find the fastest ray for the specified receiver-source pair that honors
	 * the specified phase. Returns the fastest refracted/diffracted/reflected
	 * ray. If all rays are invalid, returns a RayInfo object whose status is
	 * INVALID.
	 * 
	 * <p>
	 * This method throws no exceptions. All Exceptions are caught and their
	 * error messages accumulated into the errorMessage attribute of the RayInfo
	 * object that is returned.
	 * 
	 * @param request PredictionRequest
	 *            specifies receiver, source, phase, requesteAttributes,
	 *            etc.
	 * @return RayInfo
	 * @throws GMPException 
	 * @throws  
	 */
	@Override
	public Prediction getPrediction(PredictionRequest request) 
				 throws Exception
	{
		return computeFastRays(new BenderPredictionRequest(request))[0];
	}

  public Prediction getPrediction(PredictionRequest request,
  		                                     double[] undersideReflctnFrcnts) 
	       throws Exception
	{
		undersideReflectionFractions = undersideReflctnFrcnts;
		return computeFastRays(new BenderPredictionRequest(request))[0];
	}

//	private LBFGS lBFGS = new LBFGS();

//Bender operation:
//  Basic initialized ray built from scratch ... refractions levels are set to deepest levels
//    ray = new Ray(this (Bender), receiver, source, phaseBranchList, waveTypeInterfaceList)
//
//  New ray built from scratch but with refraction level assignments, branch definitions, and wave type definitions
//  made from the input previous ray replacing source and receiver position only (used to tweak source and receiver
//  locations for purposes of evaluating derivatives)
//    ray = new Ray(prevRay, receiver, source)
//
//  New ray built from previous ray refraction interface intersections/branch structure/wave type definitions but
//  but with the same refraction level settings as before (like a do over method for adding nodes to refraction     segment)
//    ray = new Ray(prevRay, incrementLevel = false)
//
//  New ray built from previous ray refraction interface intersections/branch structure/wave type definitions but
//  with an incremented refraction level setting (used by the main loop to advance over each level ... note that
//  after each ray the method ray.hasAnotherLevel() is called. If true the loop continues. If false the last level
//  was processed and the loop exits)
//    ray = new Ray(prevRay, incrementLevel = true)
	/**
	 * Find the fastest ray, and all other rays whose travel times differ from
	 * the travel time of the fastest ray by less than
	 * FAST_TRAVEL_TIME_TOLERANCE, for the specified receiver-source pair that
	 * honors the specified phase. Returns the fastest
	 * refracted/diffracted/reflected ray. If all rays are invalid, returns a
	 * RayInfo object whose status is INVALID.
	 * 
	 * <p>
	 * This method throws no exceptions. All Exceptions are caught and their
	 * error messages accumulated into the errorMessage attribute of the RayInfo
	 * object that is returned.
	 * 
	 * @param  request specifies receiver, source, phase, requesteAttributes,
	 *            etc.
	 * @return RayInfo[]
	 */

  
	private void setCurrentRayConvergenceCriteria(SeismicPhase phase) {
		currentRayConvgncCriteria = phaseSpecificConvgncCriteria.get(phase);
		if (currentRayConvgncCriteria == null)
			currentRayConvgncCriteria = defaultConvgncCriteria;
	}

	/**
	 * Validate the input SeismicPhase (sp) ray branch change list. The GeoTessModel
	 * assigned to Bender for purposes of ray-tracing the input phase may not define
	 * the interfaces at which the ray changes direction. These direction changes
	 * can be of three types including: TOP_SIDE_REFLECTION, BOTTOM_SIDE_REFLECTION,
	 * and BOTTOM, the last of which corresponds to a refracting ray whose bottom
	 * represents a direction change. The reflections can occur at specific
	 * interfaces, while the "BOTTOM" change refers to the deepest layer at which a
	 * ray bottoms and becomes an up-going ray. If one or more of these interfaces
	 * are not supported in the current model, then alternate valid interfaces that
	 * work as well are attempted to be discovered. If no valid alternate is found,
	 * then this method throws an exception, as the assigned GeoTessModel cannot
	 * support the phase. If a valid alternate is found then the interface is
	 * re-mapped to the alternate and a log message is output to indicate that an
	 * interface re-mapping has occurred.
	 * <p>
	 * If the phase direction change prescription is a "BOTTOM" refraction and its
	 * prescribed deepest layer is not defined in the assigned GeoTessModel, then we
	 * find the closest layer definition above, that is still in the same
	 * EarthInterfaceGroup as the layer assigned to "BOTTOM" and use it as the
	 * deepest layer for refraction.
	 * <p>
	 * If the phase prescription is a TOP_SIDE_REFLECTION, which are usually defined
	 * at major interfaces within the Earth layer structure, then we require that
	 * the requested interface be defined. Otherwise an error is thrown.
	 * <p>
	 * Lastly, for a BOTOM_SIDE_REFLECTION we first check to see if the ray
	 * direction change interface is defined as "FREE_SURFACE". If so then we assign
	 * the direction change interface to the top-most CRUST interface defined by the
	 * model. Otherwise, if the interface is not "FREE_SURFACE", and is NOT a valid
	 * EarthInterface defined by the assigned GeoTessModel, then we perform a
	 * validation as follows:
	 * 
	 * If the requested interface is in the MANTLE or CORE, we throw an error. Deep
	 * BOTOM_SIDE_REFLECTION interfaces must be defined for Bender to function
	 * properly.
	 * 
	 * Similarly, if the requested layer is an undefined water interface, we also
	 * throw an error.
	 * 
	 * Finally, if the undefined interface is in the crust, then we find the next
	 * closest interface above the requested interface that still belongs to the
	 * CRUST group. If one above is not defined then we assign the interface to the
	 * very top crust interface that is defined, which will be the closest to the
	 * requested interface.
	 * 
	 * @param sp                  The SeismicPhase whose ray branch direction change
	 *                            list is checked for validity.
	 * @param rayBrnchDirChngList The output list of tuple pairs (<RayDirection,
	 *                            EarthInterface >) for each ray direction change
	 *                            assigned to this phase (cleared on entry).
	 * @throws IOException
	 */
	private void checkPhaseRayBranchDirectionChangeInterfaceRemap(SeismicPhase sp,
			ArrayList<Tuple<RayDirection, EarthInterface>> rayBrnchDirChngList) throws IOException {

		// get valid interface names for the assigned GeoTessModel

		HashMap<String, Integer> validInterfaceNames = 
				benderModelInterfaces.getValidInterfaceNameIndexMap();
		
		// clear the list and make a pair element to store the matching
		// RayDirection -> EarthInterface objects. Loop over all phase branch list pairs
		
		rayBrnchDirChngList.clear();
		Tuple<RayDirection, EarthInterface> dirChngEntry;
		String[] entries = sp.getRayBranchList().split(",");
		for (int i = 0; i < entries.length; i += 2) {
			entries[i] = entries[i].trim();
			entries[i+1] = entries[i+1].trim();
			
			// phase prescriptions for bottom refractions, and top and bottom reflections, define
			// specific EarthInterface names for their occurrence. However, not all input models will
			// define those interfaces. If the phase prescription is a "BOTTOM" refraction and its
			// deepest layer is not defined, then we find the closest layer definition above, that is
			// still in the same EarthInterfaceGroup as the layer assigned to "BOTTOM" and use it as
			// the deepest layer for refraction.
			//
			// If the phase prescription is a TOP_SIDE_REFLECTION, which are usually defined at
			// major interfaces within the Earth layer structure, then we require that the
			// requested interface be defined. Otherwise an error is thrown.
			//
			// Lastly, for a BOTOM_SIDE_REFLECTION we first check to see if the requested interface
			// is given as "FREE_SURFACE". If so, then we map the requested surface to the top-most
			// CRUST interface defined by the model. If the requested interface is not "FREE_SURFACE",
			// and is not a valid EarthInterface defined by the asssigned GeoTessModel, then we
			// attempt to discover a valid alternative as follows: First, if the requested interface is in the MANTLE or
			// CORE we throw an error. Deep BOTOM_SIDE_REFLECTION interfaces
			// must be defined for Bender to function properly. Similarly, if the requested layer is
			// an undefined water interface, we also throw an error. Finally, if the undefined
			// interface is in the crust, then we find the next closest interface above the requested
			// interface that still belongs to the CRUST group. If one above is not defined then we 
			// assign the interface to the very top crust interface that is defined, which will be
			// the closest to the requested interface.
			
			// Summary of if-else structure below:
			// if bottom reflection and requested interface is FREE_SURFACE then map to top crust interface (M)
			// else if bottom reflection and requested interface is not defined,
			//    if requested interface is at or below MOHO, then throw error (E)
			//    else if requested interface is in crust, then map to next defined crust layer above interface, or top crust interface if none are above. (M)
			//    else if requested interface is in water, then throw error (E)
			// if top reflection interface is not defined then throw error (E)
			// if bottom refraction layer is not defined, then map to closest interface above requested interface in group, or throw error if none. (M, E)

			EarthInterface rmap = null;
			int remapCase = -1;
			if (entries[i].equalsIgnoreCase("BOTTOM_SIDE_REFLECTION")) {
				// The ray direction change is a BOTTOM_SIDE_REFLECTION: test for FREE_SURFACE
				
				if (entries[i+1].equalsIgnoreCase("FREE_SURFACE")) {
					// Is FREE_SURFACE. Map to top most crust interface
					
					rmap = benderModelInterfaces.getModelValidInterfaces()
							[benderModelInterfaces.getTopMostEarthInterface(EarthInterfaceGroup.CRUST)];
					remapCase = 1;
				} else if (!validInterfaceNames.containsKey(entries[i+1])) {
					// not FREE_SURFACE and interface is not defined. check to see if interface
					// is the MOHO or lower
					
					if (EarthInterface.valueOf(entries[i+1]).ordinal() <=
							EarthInterface.MOHO.ordinal()) {
						// interface is <= MOHO and must be defined. Throw error.
						
						throw new IOException("\nError: Phase \"" + sp.name()
						+ "\" with ray direction change \"" + entries[i]
						+ "\", and an associated EarthInterface specification \""
						+ entries[i + 1] + "\",\n       is not defined in the current "
						+ "input model. Bender requires all MANTLE or CORE phase "
						+ "\n       " + entries[i] + " to be defined in the input"
						+ " GeoTessModel for proper ray tracing execution ...\n");

					} else if (EarthInterface.valueOf(entries[i+1]).getInterfaceGroup() ==
							EarthInterfaceGroup.CRUST) {
						// interface is above MOHO, and is a member of the CRUST group.
						// find the lowest defined layer above the requested
						// interface within the CRUST group.
						
						rmap = EarthInterface.findLowestDefinedLayerAboveInGroup(entries[i + 1], validInterfaceNames);
						remapCase = 2;
						if (rmap == null) {
							// No interface in the CRUST group lies above the 
							// requested interface entries[i+1], so map to the
							// top most crust interface.
							
							rmap = benderModelInterfaces.getModelValidInterfaces()
									[benderModelInterfaces.getTopMostEarthInterface(EarthInterfaceGroup.CRUST)];
							remapCase = 3;
						}
					} else {
						// interface entries[i+1] must be in the water group. But the
						// requested interface does not exist so throw error.
						
						throw new IOException("\nError: Phase \"" + sp.name()
						+ "\" with ray direction change \"" + entries[i]
						+ "\", and an associated EarthInterface specification \""
						+ entries[i + 1] + "\",\n       is not defined in the current "
						+ "input model. Bender requires all WATER phase "
						+ "\n       " + entries[i] + " interfaces to be defined in the input"
						+ " GeoTessModel for proper ray tracing execution ...\n");

					}
				} else {
					// requested interface is defined ... assign to rmap
					
					rmap = EarthInterface.valueOf(entries[i+1]);
				}
			}
			else if (entries[i].equalsIgnoreCase("TOP_SIDE_REFLECTION")) {
				// The ray direction change is a TOP_SIDE_REFLECTION: test for interface existence
				
				if (!validInterfaceNames.containsKey(entries[i+1]))
					// interface does not exist. throw an error
					
					throw new IOException("\nError: Phase \"" + sp.name()
					+ "\" with ray direction change \"" + entries[i]
					+ "\", and an associated EarthInterface specification \""
					+ entries[i + 1] + "\",\n       is not defined in the current "
					+ "input model. Bender requires all " + entries[i]
					+ "\n       interfaces to be defined in the input GeoTessModel"
					+ " for proper ray tracing execution ...\n");
				
				else
					// requested interface is defined ... assign to rmap
					
					rmap = EarthInterface.valueOf(entries[i+1]);
			}
			else if (entries[i].equalsIgnoreCase("BOTTOM")) {
				// The ray direction change is a BOTTOM (a refraction): test for interface existence
				
				if (!validInterfaceNames.containsKey(entries[i+1])) {
					// interface does not exist. find the lowest defined layer above the
					// interface within the interface group.

					rmap = EarthInterface.findLowestDefinedLayerAboveInGroup(entries[i + 1], validInterfaceNames);
					remapCase = 4;
					if (rmap == null)
						// No interface in the interface group lies above the interface entries[i+1],
						// throw an error
						throw new IOException("\nError: Phase \"" + sp.name()
						+ "\" with ray direction change \"" + entries[i]
						+ "\", and an associated EarthInterface specification \""
						+ entries[i + 1] + "\",\n       is not defined in the current "
						+ "input model. no valid model interface within the same \n       " 
						+ "EarthInterfaceGroup (\""
						+ EarthInterface.valueOf(entries[i+1]).getInterfaceGroup().name()
						+ "\" was found that could be mapped as an alternative \n       "
						+ "for this seismic phase using the input GeoTessModel ...\n");
				}
				else
					// requested interface is defined ... assign to rmap
					
					rmap = EarthInterface.valueOf(entries[i+1]);
			}
			
			// test that the direction change interface specification is defined. If it is not defined
			// and no valid interface could be mapped to the direction change then throw an error.
			// Given the if-else structure above this should never happen. The rmap==null error
			// message is just a precaution.
			
			// If rmap is defined but is different than the requested interface (a re-map),
			// then log a message and set the new re-mapped interface.
			
			if (rmap == null) {
				
				// the ray direction change interface is not defined in the model, and no valid
				// interface could be mapped. Throw an exception.
				
				throw new IOException("\nError: Phase \"" + sp.name()
						+ "\" with ray direction change \"" + entries[i]
						+ "\", and an associated EarthInterface specification \""
						+ entries[i + 1] + "\",\n       is not defined in the current "
						+ "input model. Additionally, no valid model interface within "
						+ "the same EarthInterfaceGroup (\""
						+ EarthInterface.valueOf(entries[i+1]).getInterfaceGroup().name()
						+ "\")\n       was found that could be mapped as an alternative "
						+ "for this seismic phase using the input GeoTessModel ...\n");
			} else if (!entries[i+1].equalsIgnoreCase(rmap.name())) {
				
				// a ray direction change interface was re-mapped. Log and output the layer re-map
				// for this phase
				
				String s = "\nThe input phase \"" + sp.name() + "\""
						+ " with ray direction change \"" + entries[i]
						+ "\",\n" + "and an associated EarthInterface specification \""
						+ entries[i + 1] + "\",\n" + "has been remapped to layer \""
						+ rmap.name() + "\".\n\n";
				switch (remapCase) {
				case 1:
					s += "This is the top most EarthInterface that is defined in the\n"
					   + "CRUST by the assigned GeoTessModel ...\n\n";
					break;
				case 2:
					s += "This is the closest \"" + rmap.getInterfaceGroup().name() + "\" group interface defined "
					   + "in the input\nGeoTessModel that lies above the requested EarthInterface ...\n\n";
					break;
				case 3:
					s += "No interface in the CRUST group was discovered above the requested interface,\n"
					   + "so the requested interface was mapped to the top most CRUST group interface\n"
					   + "defined in the GeoTessModel ...\n\n";
					break;
				case 4:
					s += "This is the closest \"" + rmap.getInterfaceGroup().name() + "\" group interface defined "
							   + "in the input\nGeoTessModel that lies above the requested EarthInterface ...\n\n";
					break;
				default:
				
				}

				// log the message
				
				print(s);
				
				// assign re-mapped name to entries[i+ 1]
				
				entries[i + 1] = rmap.name();
			}

			// add next ray branch direction change and EarthInterface to the output list.
			
			RayDirection rayBrnchDirChngType = RayDirection.valueOf(entries[i]);
			dirChngEntry = new Tuple<RayDirection, EarthInterface>(rayBrnchDirChngType,
					EarthInterface.valueOf(entries[i + 1]));
			rayBrnchDirChngList.add(dirChngEntry);

		}
	}
	
	/**
	 * Validate the input SeismicPhase (sp) wave speed type change list. The
	 * GeoTessModel assigned to Bender for purposes of ray-tracing the input phase
	 * may not define the interfaces at which the ray changes wave speed type (P to
	 * S or S to P).
	 * 
	 * This method first validates the requested wave speed change types (P or S) to
	 * ensure they are defined by the assigned GeoTessModel. Next, it validates the
	 * EarthInterfaces where the wave speed type changes. If the EarthInterface is
	 * defined as "FREE_SURFACE" then the method maps the top-most CRUST interface
	 * as the requested EarthInterface.
	 * <p>
	 * If the requested EarthInterface is not "FREE_SURFACE", and is NOT a valid
	 * defined interface supported by the assigned GeoTessModel, then requested
	 * interface is checked to see if it is a CRUST group EarthInterface. If NOT
	 * then an error is thrown, as all non-CRUST requested interfaces must be
	 * defined for Bender to function properly.
	 * <p>
	 * Finally,If the requested interface is not a valid defined interface, but is a
	 * CRUST group interface, then the requested interface is mapped to first valid
	 * interface that lies above the requested interface, that still resides in the
	 * CRUST group. If no layer is found then the requested interface is mapped to
	 * the top-most CRUST layer, which will be the closest layer to the requested
	 * interface.
	 * 
	 * @param sp                         The SeismicPhase whose ray branch direction
	 *                                   change list is checked for validity.
	 * @param waveSpeedInterfaceChngList The output list of tuple pairs
	 *                                   (<EarthInterface, Integer>) for each ray
	 *                                   wave speed type change (P to S or S to P)
	 *                                   assigned to this phase (cleared on entry).
	 * 
	 * @throws IOException
	 */
	private void checkPhaseWaveSpeedInterfaceRemap(SeismicPhase sp,
		ArrayList<Tuple<EarthInterface, Integer>> waveSpeedInterfaceChngList) throws IOException {

		// get valid interface names for the assigned GeoTessModel

		HashMap<String, Integer> validInterfaceNames = 
				benderModelInterfaces.getValidInterfaceNameIndexMap();
		
		// get the wave speed interface type list from the phase. Check the first
		// wave speed entry to ensure it is defined in the assigned GeoTessModel
		
		String[] entries = sp.getRayInterfaceWaveTypeList().replaceAll(",", " ").split("\\s+");
		entries[0] = entries[0].trim();
		
		for (int i=0; i<entries.length; i+=2)
		    if (entries[i].equals("P"))
			entries[i] = "PSLOWNESS";
		    else if (entries[i].equals("S"))
			entries[i] = "SSLOWNESS";
		
//		if(WaveType.P.name().equals(entries[0])) entries[0] = GeoAttributes.PSLOWNESS.name();
//		else if(WaveType.S.name().equals(entries[0])) entries[0] = GeoAttributes.SSLOWNESS.name();
		
		int waveSpeedIndx = this.geoTessModel.getMetaData().getAttributeIndex(entries[0]);
		if (waveSpeedIndx == -1)
			throw new IOException("\nError: The assigned Bender GeoTessModel does not support"
					+ " the requested phase \"" + sp + "\" slowness attribute \""
					+ entries[0] + "\" ...\n");

		// make the temporary interface/wave speed index tuple and variable. Add the starting
		// wave speed to the wave speed interface change list associated with a null interface
		// (this is the starting slowness type at the source).
		
		Tuple<EarthInterface, Integer> wavSpdChngEntry;
		EarthInterface interfaceWaveTypeChng = null;
		wavSpdChngEntry = new Tuple<EarthInterface, Integer>(null, waveSpeedIndx);
		waveSpeedInterfaceChngList.add(wavSpdChngEntry);
		
		// loop over all remaining entries, which if they exist, will be in pairs
		// defining the EarthInterface and the new slowness type that changes when
		// the ray crosses that interface.
		
		for (int i = 1; i < entries.length; i += 2) {
			entries[i] = entries[i].trim();
			entries[i+1] = entries[i+1].trim();

			// first check the new slowness (entries[i+1]) to ensure it is an attribute of the
			// assigned GeoTessModel. throw an error it it isn't supported.
			
			waveSpeedIndx = this.geoTessModel.getMetaData().getAttributeIndex(entries[i+1]);
			if (waveSpeedIndx == -1)
				throw new IOException("\nError: The assigned Bender GeoTessModel does not support"
						+ " the requested phase \"" + sp + "\" slowness attribute \""
						+ entries[i + 1] + "\" ...\n");
			
			// now check the interface where the wave speed changes type. First check for a
			// "FREE_SURFACE" entry
			
			if (entries[i].equalsIgnoreCase("FREE_SURFACE")) {
				
				// "FREE_SURFACE". Map the interface to the top level CRUST interface
			
				interfaceWaveTypeChng = benderModelInterfaces.getModelValidInterfaces()
						[benderModelInterfaces.getTopMostEarthInterface(EarthInterfaceGroup.CRUST)];
				
			} else if (!validInterfaceNames.containsKey(entries[i])) {
				
				// not FREE_SURFACE and requested interface is not defined. Check to see if
				// interface is part of the CRUST
				
				if (EarthInterface.valueOf(entries[i]).getInterfaceGroup() !=
						EarthInterfaceGroup.CRUST) {
					
					// interface is not in the CRUST so throw an error, as non-CRUST interaces
					// must exist for proper Bender Execution.
					
					throw new IOException("\nError: Phase \"" + sp.name()
					+ "\" with  wave speed EarthInterface change \"" + entries[i]
					+ "\"\n       is not defined in the current "
					+ "input model. Bender requires all WATER, MANTLE, or CORE phase "
					+ "\n       wave speed change EarthInterfaces to be defined in the input"
					+ " GeoTessModel for proper ray tracing execution ...\n");

				} else {
					
					// interface is in the crust. map to next defined crust layer above interface,
					// or the top crust interface if none are above.
					
					interfaceWaveTypeChng = EarthInterface.findLowestDefinedLayerAboveInGroup(
							entries[i], validInterfaceNames);
					if (interfaceWaveTypeChng == null) {
						
						// No interface in the CRUST group lies above the 
						// requested interface entries[i], so map to the
						// top most crust interface.
						
						interfaceWaveTypeChng = benderModelInterfaces.getModelValidInterfaces()
								[benderModelInterfaces.getTopMostEarthInterface(EarthInterfaceGroup.CRUST)];
					}
				}
				
			}

			// check to see if the requested interface has been re-mapped. If so, then log the
			// change in the Bender log file .
			
			if (!interfaceWaveTypeChng.name().equalsIgnoreCase(entries[i])) {
				
				// a ray direction change interface was re-mapped. Log and output the layer re-map
				// for this phase
				
				String s = "\nThe input phase \"" + sp.name() + "\""
						+ " with wave speed EarthInterface change \"" + entries[i]
						+ "\",\n" + "has been remapped to layer \""
						+ interfaceWaveTypeChng.name() + "\".\n\n"
						+ "This is the closest interface defined "
						+ "in the input GeoTessModel ...\n\n";

				// log the message
				
				print(s);
				
				// assign re-mapped name to entries[i]
				
				entries[i] = interfaceWaveTypeChng.name();
			}
			
			// save the wave speed and the interface where the wave speed changes in the list
			// and continue to the next entry pair, if any.
			
			wavSpdChngEntry = new Tuple<EarthInterface, Integer>(interfaceWaveTypeChng, waveSpeedIndx);
			waveSpeedInterfaceChngList.add(wavSpdChngEntry);
  		}
	}
	
	/**
	 * Validates that the input SeismicPhase (sp) is supported by the assigned GeoTessModel.
	 * By definition all crustal phases are supported by the model, which is validated when
	 * the BenderModelInterfaces object is constructed. This includes a definition for the
	 * MOHO layer. However, phases requiring a water or ice layer, or phases whose rays
	 * penetrate the mantle or core have not been validated. This is done here in this method.
	 * 
	 * @param sp                  The SeismicPhase whose model support is validated.
	 * @param rayBrnchDirChngList The list of tuple pairs (<RayDirection,
	 *                            EarthInterface >) for each ray direction change
	 *                            assigned to this phase.
	 * @throws IOException
	 */
	private void checkPhaseModelSupport(SeismicPhase sp,
			ArrayList<Tuple<RayDirection, EarthInterface>> rayBrnchDirChngList) throws IOException {

		// loop over all ray branch direction changes for this phase

		for (int i = 0; i < rayBrnchDirChngList.size(); ++i) {

			// get the ray direction and the interface/layer assigned to it

			RayDirection rayDirection = rayBrnchDirChngList.get(i).first;
			EarthInterface earthInterface = rayBrnchDirChngList.get(i).second;

			// jump to the EarthInterfaceGroup for this interface

			switch (earthInterface.getInterfaceGroup()) {
			case WATER:
				// just check to make sure a water or ice layer was defined to properly handle
				// water phases

				if (!benderModelInterfaces.isEarthInterfaceGroupDefined(EarthInterfaceGroup.WATER)) {
					throw new IOException("\nError: A water layer (WATER or ICE) must be defined in "
							+ "the assigned GeoTessModel to properly support this phase (" + sp + ") ...\n");
				}
				break;
			case CRUST:
				// just return here as the construction of the BenderModelInterfaces object
				// required that a valid CRUST layer be defined along with the definition of
				// the MOHO

				break;
			case MANTLE:
				// Interface group is MANTLE. if this is a top side reflection and the interface
				// is below the MOHO, or if this is a bottom refraction or bottom side reflection,
				// then the CMB is required by the model for Bender to perform proper ray tracing.
				// If the CMB is not defined throw an error.
				if ((rayDirection == RayDirection.TOP_SIDE_REFLECTION)
						&& (earthInterface.ordinal() < EarthInterface.MOHO.ordinal())
						&& !benderModelInterfaces.isValidInterfaceNameContained(earthInterface.name())) {
					throw new IOException("\nError: The \"" + earthInterface.name() 
							+ "\" must be defined in the assigned GeoTessModel to properly "
							+ "support the TOP_SIDE_REFLECTION for this phase (" + sp + ") ...\n");
				} else if ((rayDirection == RayDirection.BOTTOM_SIDE_REFLECTION)
						|| (rayDirection == RayDirection.BOTTOM)) {
					if (!benderModelInterfaces.isValidInterfaceNameContained(EarthInterface.CMB.name()))
						throw new IOException("\nError: The CMB must be defined in the assigned "
								+ "GeoTessModel to properly support this phase (" + sp + ") ...\n"
								+ "       Phases whose rays penetrate below the MOHO cannot be"
								+ " processed properly without a defined CMB ...\n");
				}
				break;
			case CORE:
				// Interface group is CORE. if this is a top side reflection and the interface
				// is below the CMB, or if this is a bottom refraction or bottom side reflection,
				// then both the CMB and ICB are required by the model for Bender to perform
				// proper ray tracing. If one or both are not defined then throw an error.

				// first check CMB which must be defined for any core phase

				if (!benderModelInterfaces.isValidInterfaceNameContained(EarthInterface.CMB.name()))
					throw new IOException("\nError: The CMB must be defined in the assigned "
							+ "GeoTessModel to properly support a core phase (" + sp + ") ...\n");

				// next check for rays that penetrate the CMB. If they do then the ICB must be
				// defined
				if ((rayDirection == RayDirection.TOP_SIDE_REFLECTION)
						&& (earthInterface == EarthInterface.ICB)
						&& !benderModelInterfaces.isValidInterfaceNameContained("ICB")) {
					throw new IOException("\nError: The \"ICB\" must be defined in the "
							+ "assigned GeoTessModel to properly support the "
							+ "TOP_SIDE_REFLECTION for this phase (" + sp + ") ...\n");
				} else if ((rayDirection == RayDirection.BOTTOM_SIDE_REFLECTION)
						|| (rayDirection == RayDirection.BOTTOM)) {
					if (!benderModelInterfaces.isValidInterfaceNameContained(EarthInterface.ICB.name()))
						throw new IOException("\nError: The ICB must be defined in the assigned "
								+ "GeoTessModel to properly support this phase (" + sp + ") ...\n"
								+ "       Phases whose rays penetrate below the CMB cannot be"
								+ " processed properly without a defined ICB ...\n");
				}
				break;
			default:
				// can't get here, but throw an error just-in-case

				throw new IOException("\nError: The NOT_DEFINED EarthInterfaceGroup was encountered "
				+ "for phase ("	+ sp + ") ...\n");
			}
		}
	}
	
	private PhaseLayerChange getPhaseLayerChange(SeismicPhase phase) throws IOException {
		
		PhaseLayerChange phaseLayerChangeEntry = phaseLayerChange.get(phase); 
		if (phaseLayerChangeEntry == null) {
			phaseLayerChangeEntry = new PhaseLayerChange();
			phaseLayerChange.put(phase, phaseLayerChangeEntry);

			// validate the phase branch direction and wave speed interface changes. also ensure
			// that the model supports the interfaces required by the phase
			
			checkPhaseRayBranchDirectionChangeInterfaceRemap(phase, phaseLayerChangeEntry.rayBrnchDirChngList);
			checkPhaseWaveSpeedInterfaceRemap(phase, phaseLayerChangeEntry.waveSpeedInterfaceChngList);
			checkPhaseModelSupport(phase, phaseLayerChangeEntry.rayBrnchDirChngList);
		}
		
		return phaseLayerChangeEntry;
	}

	public RayInfo[] computeFastRays(PredictionRequest request) throws Exception
	{
		if (!request.isDefining())
			return new RayInfo[] { 
					new RayInfo(request, this, "PredictionRequest submitted Bender was non-defining") 
					};

		// time when calculation starts
		calcOriginTime = System.currentTimeMillis();
		//setMaximumModelNotThinLayer(getGeoTessModel().getMetaData());
		
		// time by which calculation must end
		if (maxEllapsedTime >= 0)
			timeToAbort = calcOriginTime + maxEllapsedTime;
		else
			timeToAbort = Long.MAX_VALUE;
		GeoVector source = request.getSource();
		GeoVector receiver = request.getReceiver();

		currentSource = source;
		currentReceiver = receiver;

		// buffer to accumulate error messages from caught exceptions. These
		// will be added to the RayInfo object that is returned by this method.
		
		errorMessages = new StringBuilder();

		// make sure the phase ray branch list and wave speed interface list are defined.
		
		if (request.getPhase().getRayBranchList() == null)
			return new RayInfo[] { new RayInfo(request, this,
					"\nError: Seismic Phase Ray Branch List for "
					+ request.getPhase().name() + " was Not Defined ...\n") };

		if (request.getPhase().getRayInterfaceWaveTypeList() == null)
			return new RayInfo[] {
					new RayInfo(request, this,
							"\n Error: Seismic Phase Ray Wave Type Interface Conversion List for "
							+ request.getPhase().name() + " was Not Defined ...\n") };

		setCurrentRayConvergenceCriteria(request.getPhase());
		try
		{
			// build source, receiver, branch model, wave type conversion model

			currentSourceProfile = GeoTessPosition.getGeoTessPosition(geoTessModel);
			currentSourceProfile.set(source.getUnitVector(),  source.getRadius());

			currentReceiverProfile = GeoTessPosition.getGeoTessPosition(geoTessModel);
			currentReceiverProfile.set(receiver.getUnitVector(),  receiver.getRadius());
			sourceToReceiverDistance = currentSourceProfile.distanceDegrees(currentReceiverProfile);

			GeoTessPosition depthProfile = GeoTessPosition.getGeoTessPosition(geoTessModel);
			depthProfile.set(receiver.getUnitVector(),  receiver.getRadius());
			depthProfile.setIntermediatePosition(currentSourceProfile , currentReceiverProfile, 0.5);
			depthProfile.setRadius(depthProfile.getEarthRadius());

			// create the phase ray branch direction change list and wave speed interface branch
			// change list. This call also validates any remappings if they occur.
			
			PhaseLayerChange plc = getPhaseLayerChange(request.getPhase());
			
			// make the phase ray branch and wave type models
			
			phaseRayBranchModel = new PhaseRayBranchModel(depthProfile, request.getPhase(),
					benderModelInterfaces, plc.rayBrnchDirChngList, sourceToReceiverDistance, phaseLayerLevelThickness);
			phaseWaveTypeModel = new PhaseWaveTypeModel(getGeoTessModel().getMetaData(),
					request.getPhase(), benderModelInterfaces, plc.waveSpeedInterfaceChngList);

			// check co-positional source/receiver rays

			String phasename = request.getPhase().name();
			if (currentSourceProfile.getDistance3D(currentReceiverProfile) == 0.0)
			{
				// if more than 1 branch or not a reflection then make a zero length/
				// travel time ray

				if ((phaseRayBranchModel.getUndersideReflectionCount() > 0) ||
						((phasename.indexOf('i') == -1) && (phasename.indexOf('c') == -1) && (phasename.indexOf('m') == -1)))
				{
					rayInfo = new RayInfo[1];
					rayInfo[0] = new RayInfo(request, this, "Zero Path Length Ray");
					rayInfo[0].setErrorMessage(errorMessages.toString());
					rayInfo[0].setStatusLog(logBuffer.toString());
					rayInfo[0].setAttribute(GeoAttributes.CALCULATION_TIME, 
							(System.currentTimeMillis() - calcOriginTime) * 1e-3);
					rayInfo[0].setZeroLengthRay();
					rayInfo[0].setRayType(RayType.VALID);
					rayInfo[0].setAttribute(GeoAttributes.TRAVEL_TIME, 0.0);
					rayInfo[0].setAttribute(GeoAttributes.TURNING_DEPTH, 0.0);
					logBuffer.setLength(0);
					
					rayInfo[0].getAttribute(GeoAttributes.TRAVEL_TIME);
					rayInfo[0].getAttribute(GeoAttributes.TURNING_DEPTH);
					return rayInfo;
				}
			}

//			if (undersideReflectionFractions != null)
//			{
//				for (int i = 0; i < undersideReflectionFractions.length; ++i)
//				{
//				  double ang = Math.toRadians(distDeg) * undersideReflectionFractions[i];
//				  phaseRayBranchModel.setFixedReflectionInitialAngle(i+2, ang);
//				}
//			}

			if (getVerbosity() > 0)
			{
				print(String.format(
						"ObservationId: %d%nSource:   %s%nReceiver: %s%n", request.getObservationId(),
						source.toString(), receiver.toString()));
				print(String.format("dist=%1.4f  seaz=%1.4f  esaz=%1.4f%n%n",
						receiver.distanceDegrees(source), receiver
						.azimuthDegrees(source, 0.),
						source.azimuthDegrees(receiver, 0.)));

				println("Seismic Phase: " + request.getPhase().name());
				println("Receiver:");
				println(profileString(currentReceiverProfile));
				println();
				println("Source:");
				println(profileString(currentSourceProfile));
			}

			// allow listeners to initialize

			initializeListeners();

			// calculate fastest ray

//			long aProfilerSamplePeriod = 1;
//	    Profiler profiler = null;
//	    if (aProfilerSamplePeriod > 0)
//	    {
//	      profiler = new Profiler(Thread.currentThread(), aProfilerSamplePeriod,
//	                              "Bender");
//	      profiler.setTopClass("gov.sandia.gmp.bender.ray.RayBranch");
//	      profiler.setTopMethod("optimizeLoop");
//	      profiler.accumulateOn();
//	    }

			fastRay = null;
    	try
			{
//    		double[] testf = {0.0, 0.0};
//				lBFGS.setLBFGSFunction(this);
//				lBFGS.setOutputAmountOff();
//				lBFGS.lbfgs(testf);
//				double[] testGrad = {0.0, 0.0};
//				double testVal = this.setFunctionAndGradient(testf, testGrad);

				if (phaseRayBranchModel.getUndersideReflectionCount() > 0)
				{
					// bottom-side reflections exist ... see if this is an optimal ray
					// calculation (using lBFGS) or a mapping calculation

					mapUndersideReflections = false;
					if (mapUndersideReflections)
					{
						mapBottomSideReflections();
						return null;
					}
					else
					{
						// this is an under side reflected phase get it's definition object
						// and test to see if the bounce point prediction request is for
						// a fixed or variable position.
						
						UndersideReflectedPhaseBouncePoint undersideReflectedPhaseBouncePoint =
								((BenderPredictionRequest)request).getUndersideReflectedPhaseBouncePoint();
						if (undersideReflectedPhaseBouncePoint.isBouncePointFixed())
						{
							// bounce point is to remain fixed. See if it position has already
							// ben set or if Bender is to initialize it using a TauP map.
							
						  updateFromUndersideReflectionLatLonvector = true;
						  if (undersideReflectedPhaseBouncePoint.isFixedBouncePointPositionSet())
						  {
						  	// It has already been set ... extract it from the
						  	// undersideReflectedPhaseBouncePoint
						  	
							  if (undersideReflectionLatLonvector == null)
							  	undersideReflectionLatLonvector = new double [2];
	
							  undersideReflectionLatLonvector[0] = Math.toRadians(undersideReflectedPhaseBouncePoint.getFixedBouncePointLatitudeDeg());
								undersideReflectionLatLonvector[1] = Math.toRadians(undersideReflectedPhaseBouncePoint.getFixedBouncePointLongitudeDeg());
						  }
						  else
						  	// has not been set ... set it with TauP
						  	
							  setInitialBottomSideReflections(source, receiver);

						  // evaluate the ray using a fixed bounce point position
						  
							fastRay = new Ray(this, currentReceiverProfile, currentSourceProfile, true);
						}
						else
						{
							// bounce point position is variable. Calculate ray while
							// optimizing for the bounce point position

						  setInitialBottomSideReflections(source, receiver);
						  
							// if phase is a depth phase get bounce point layer
							if (phaseRayBranchModel.isDepthPhase())
							{
								int layer = phaseRayBranchModel.getRayBranchInterfaceIndex(1);
								GeoTessPosition gtp = GeoTessPosition.getGeoTessPosition(currentSourceProfile);
								gtp.setTop(layer, currentSourceProfile.getVector());
								double d = gtp.getDistance3D(currentSourceProfile);
								if (d < .02)
								{
									if (request.getPhase() == SeismicPhase.pP)
									{
										plc = this.getPhaseLayerChange(SeismicPhase.P);
										phaseRayBranchModel = new PhaseRayBranchModel(depthProfile,
												SeismicPhase.P, benderModelInterfaces,
												plc.rayBrnchDirChngList, sourceToReceiverDistance,
												phaseLayerLevelThickness);
										phaseWaveTypeModel = new PhaseWaveTypeModel(
												getGeoTessModel().getMetaData(), SeismicPhase.P,
												benderModelInterfaces, plc.waveSpeedInterfaceChngList);
									}
									else if (request.getPhase() == SeismicPhase.sP)
									{
										plc = this.getPhaseLayerChange(SeismicPhase.S);
										phaseRayBranchModel = new PhaseRayBranchModel(depthProfile,
												SeismicPhase.S, benderModelInterfaces,
												plc.rayBrnchDirChngList, sourceToReceiverDistance,
												phaseLayerLevelThickness);
										phaseWaveTypeModel = new PhaseWaveTypeModel(getGeoTessModel().getMetaData(),
												SeismicPhase.S,benderModelInterfaces,
												plc.waveSpeedInterfaceChngList);
									}

									fastRay = new Ray(this, currentReceiverProfile, currentSourceProfile, true);
								}
								else
								  optimizeBouncePoints();
							}
							else
  						  optimizeBouncePoints();
						}
					}
				}
				else // simple 1 branch ray
					fastRay = new Ray(this, currentReceiverProfile, currentSourceProfile, true);
			}
			catch(BenderException rayEx)
			{
				if (rayEx.getErrorCode() == ErrorCode.FATAL)
					throw rayEx;

				// record error and keep going. these are innocuous errors
				errorMessages.append(rayEx.getMessage()
						+GMPException.getStackTraceAsString(rayEx)+"\n");
			}

	  	rayInfo = new RayInfo[1];

	  	// if fastRay is null or invalid or an error than return an informative
	  	// rayInfo with the error message.

	  	if ((fastRay == null) ||
			    (fastRay.getRayType() == RayType.ERROR ||
					 fastRay.getRayType() == RayType.INVALID) ||
					 rayContainsUnrequestedDiffractions())
			{
				rayInfo = new RayInfo[1];
				rayInfo[0] = new RayInfo(request, this, "INVALID");
				rayInfo[0].setErrorMessage(errorMessages.toString());
				rayInfo[0].setStatusLog(logBuffer.toString());
				rayInfo[0].setAttribute(GeoAttributes.CALCULATION_TIME, 
						(System.currentTimeMillis() - calcOriginTime) * 1e-3);
				if ((fastRay != null) && (phaseRayBranchModel.getUndersideReflectionCount() > 0))
 				{
 					rayInfo[0].setAttribute(GeoAttributes.BOUNCE_POINT_LATITUDE_DEGREES,
 																  fastRay.getBranches().get(0).
 																  				getNextDirectionChangeSegment().
 																  				getMiddleNode().getLatitudeDegrees());
 					rayInfo[0].setAttribute(GeoAttributes.BOUNCE_POINT_LONGITUDE_DEGREES,
 						  										fastRay.getBranches().get(0).
 						  														getNextDirectionChangeSegment().
 						  														getMiddleNode().getLongitudeDegrees());
 					rayInfo[0].setAttribute(GeoAttributes.BOUNCE_POINT_SNELLS_LAW,
																	fastRay.getBouncePointFitness(0));
 					rayInfo[0].setAttribute(GeoAttributes.DISTANCE_DEGREES,
 																	fastRay.getBranches().get(0).angleDegrees() +
 																	fastRay.getBranches().get(1).angleDegrees());
 				}
				logBuffer.setLength(0);
				return rayInfo;
			}

			changeNotifier.setSource(fastRay);
			fastRay.setStatus(RayStatus.FASTEST_RAY);

			//X rayInfo[i] = new RayInfo(request, this, rays.get(i));
			rayInfo[0] = new RayInfo(request, fastRay);
//
//	    if (profiler != null)
//	    {
//	      profiler.stop();
//	      profiler.printAccumulationString();
//	      profiler = null;
//	    }

    	Ray ray = null;
			GeoTessPosition src = GeoTessPosition.getGeoTessPosition(currentSourceProfile);
			double[] vtp = {0.0, 0.0, 0.0};
			
			if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLAT))
			{
				// move source north
				//X GeoVector src = source.moveNorth(BenderConstants.deriv_dx).setDepth(source.getDepth());
				currentSourceProfile.move_north(BenderConstants.deriv_dx, vtp);
				src.set(vtp, currentSourceProfile.getRadius());
				src.setDepth(currentSourceProfile.getDepth());
				computingDerivative = true;
				//ray = new Ray(fastRay, src, currentReceiverProfile);
				ray = new Ray(this, currentReceiverProfile, src, true);
				computingDerivative = false;
				rayInfo[0].setAttribute(GeoAttributes.DTT_DLAT, 
						(ray.getTravelTime()-rayInfo[0].getTravelTime())/BenderConstants.deriv_dx);
			}

			if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLON))
			{
				// move source east along a great circle (not small circle)
				//X GeoVector src = source.move(PI/2, BenderConstants.deriv_dx).setDepth(source.getDepth());
				VectorUnit.move(currentSourceProfile.getVector(), BenderConstants.deriv_dx, PI/2, vtp);
				src.set(vtp, currentSourceProfile.getRadius());
				src.setDepth(currentSourceProfile.getDepth());
				computingDerivative = true;
				//ray = new Ray(fastRay, src, currentReceiverProfile);
				ray = new Ray(this, currentReceiverProfile, src, true);
				computingDerivative = false;
				rayInfo[0].setAttribute(GeoAttributes.DTT_DLON, 
						(ray.getTravelTime()-rayInfo[0].getTravelTime())/BenderConstants.deriv_dx);
			}

			double slowness = Globals.NA_VALUE;
			if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS) || 
					request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES))
			{
				// move source away from receiver
				//X GeoVector src = source.move(rayInfo[i].getBackAzimuth()+PI, BenderConstants.deriv_dx).setDepth(source.getDepth());
				VectorUnit.move(currentSourceProfile.getVector(), BenderConstants.deriv_dx, rayInfo[0].getBackAzimuth() + PI, vtp);
				src.set(vtp, currentSourceProfile.getRadius());
				src.setDepth(currentSourceProfile.getDepth());
				computingDerivative = true;
				//ray = new Ray(fastRay, src, currentReceiverProfile);
				ray = new Ray(this, currentReceiverProfile, src, true);
				computingDerivative = false;
				slowness = (ray.getTravelTime()-rayInfo[0].getTravelTime())/BenderConstants.deriv_dx;
			}

			double dtt_dr = Globals.NA_VALUE;
			if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DR))
			{
				// move source up (increase radius)
				//XGeoVector src = source.clone().setRadius(source.getRadius() + BenderConstants.deriv_dr);
				src.set(currentSourceProfile.getVector(), currentSourceProfile.getRadius() + BenderConstants.deriv_dr);
				computingDerivative = true;
				//ray = new Ray(fastRay, src, currentReceiverProfile);
				ray = new Ray(this, currentReceiverProfile, src, true);
				computingDerivative = false;
				//System.out.println((ray.getTravelTime()-rayInfo[0].getTravelTime())/BenderConstants.deriv_dr);
				dtt_dr =  (ray.getTravelTime()-rayInfo[0].getTravelTime())/BenderConstants.deriv_dr;
			}

			Prediction result = rayInfo[0];
			
			if(rayInfo[0] == null)
			  System.out.println("THIS IS ALSO TERRIBLE");
			double tt = rayInfo[0].getTravelTime();
			result.setAttribute(GeoAttributes.TT_BASEMODEL, tt);
			
			if (ellipticityCorrections != null)
			{
				double ellipCorr = ellipticityCorrections.getEllipCorr(request.getPhase(), 
						request.getReceiver(), request.getSource());
				rayInfo[0].setAttribute(GeoAttributes.TT_ELLIPTICITY_CORRECTION, ellipCorr);
				if (ellipCorr != Globals.NA_VALUE)
					tt += ellipCorr;
			}

			// if the site term was requested then try to retrieve it and set the value 
			// for the tt_site_correction attribute in the rayInfo object.  If the site term
			// is not na value, and predicted travel time is not na value, add the site
			// term to the total travel time.
			Boolean useST = useTTSiteCorrectionsStationList.get(rayInfo[0].getReceiver().getSta());
			if (useST == null) useST = useTTSiteCorrections;
			if (useST)
			{
				double st = Globals.NA_VALUE;
				if (geoTessModel instanceof GeoTessModelSiteData)
				{
					if (tt != Globals.NA_VALUE)
					{
						String staname = rayInfo[0].getReceiver().getSta();
						double origTime = rayInfo[0].getSource().getOriginTime();
						int attrIndx = geoTessModel.getMetaData().getAttributeIndex(
							rayInfo[0].getWaveType().getAttribute().name());
						st = ((GeoTessModelSiteData) geoTessModel).getSiteTerm(
								   attrIndx, staname, tt, origTime);
						if (st != Globals.NA_VALUE)
							tt += st;
					}
				}
				rayInfo[0].setAttribute(GeoAttributes.TT_SITE_CORRECTION, st);
			}

			setGeoAttributes(result, tt, rayInfo[0].getAzimuth(), slowness,
					dtt_dr, Globals.NA_VALUE, Globals.NA_VALUE);

			if (request.getRequestedAttributes().contains(GeoAttributes.DISTANCE))
				result.setAttribute(GeoAttributes.DISTANCE, rayInfo[0].getDistance());

			if (request.getRequestedAttributes().contains(GeoAttributes.DISTANCE_DEGREES))
				result.setAttribute(GeoAttributes.DISTANCE_DEGREES, rayInfo[0].getDistanceDegrees());

//          commented out because these values are computed and set in request.setGeoAttributes().
//			//if (request.getRequestedAttributes().contains(GeoAttributes.TRAVEL_TIME))
//			result.setAttribute(GeoAttributes.TRAVEL_TIME, rayInfo[0].getTravelTime());
//
//			if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH))
//				result.setAttribute(GeoAttributes.AZIMUTH, rayInfo[0].getAzimuth());
//
//			if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_DEGREES))
//				result.setAttribute(GeoAttributes.AZIMUTH_DEGREES, rayInfo[0].getAzimuthDegrees());
//
			if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DLAT))
				result.setAttribute(GeoAttributes.DAZ_DLAT, sin(rayInfo[0].getBackAzimuth()) / sin(rayInfo[0].getDistance()));

			if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DLON))
				result.setAttribute(GeoAttributes.DAZ_DLON, cos(rayInfo[0].getBackAzimuth()) / sin(rayInfo[0].getDistance()));
//
//			if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DR))
//				result.setAttribute(GeoAttributes.DAZ_DR, 0.);
//
			if (request.getRequestedAttributes().contains(GeoAttributes.BACKAZIMUTH))
				result.setAttribute(GeoAttributes.BACKAZIMUTH, ((rayInfo[0].getBackAzimuth() + 2*PI) % (2*PI)));

			if (request.getRequestedAttributes().contains(GeoAttributes.BACKAZIMUTH_DEGREES))
				result.setAttribute(GeoAttributes.BACKAZIMUTH_DEGREES, ((rayInfo[0].getBackAzimuthDegrees()+360.) % 360.));

//			if (request.getRequestedAttributes().contains(GeoAttributes.DISTANCE))
//				result.setAttribute(GeoAttributes.DISTANCE, rayInfo[0].getDistance());
//
//			if (request.getRequestedAttributes().contains(GeoAttributes.DISTANCE_DEGREES))
//				result.setAttribute(GeoAttributes.DISTANCE_DEGREES, rayInfo[0].getDistanceDegrees());
//
//			if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DTIME))
//				result.setAttribute(GeoAttributes.DTT_DTIME, 1.);
//
//			if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DTIME))
//				result.setAttribute(GeoAttributes.DAZ_DTIME, 0.);
//
//			if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DTIME))
//				result.setAttribute(GeoAttributes.DSH_DTIME, 0.);

			if (getVerbosity() > 0)
			{
				println("fastest ray = " + rayInfo[0].toString());
			}

			if(rayInfo[0] == null)
			  System.out.println("THIS IS TERRIBLE");
			rayInfo[0].setStatusLog(logBuffer.toString());
			rayInfo[0].setErrorMessage(errorMessages.toString());
			rayInfo[0].setAttribute(GeoAttributes.CALCULATION_TIME, 
					                   (System.currentTimeMillis() - calcOriginTime) * 1e-3);
			if (phaseRayBranchModel.getUndersideReflectionCount() > 0)
			{
				rayInfo[0].setAttribute(GeoAttributes.BOUNCE_POINT_LATITUDE_DEGREES,
															  fastRay.getBranches().get(0).
															  				getNextDirectionChangeSegment().
															  				getMiddleNode().getLatitudeDegrees());
				rayInfo[0].setAttribute(GeoAttributes.BOUNCE_POINT_LONGITUDE_DEGREES,
					  										fastRay.getBranches().get(0).
					  														getNextDirectionChangeSegment().
					  														getMiddleNode().getLongitudeDegrees());
				rayInfo[0].setAttribute(GeoAttributes.BOUNCE_POINT_SNELLS_LAW,
																fastRay.getBouncePointFitness(0));
				rayInfo[0].setAttribute(GeoAttributes.BOUNCE_POINT_OUTOFPLANE_DEGREES,
																fastRay.getBouncePointOutOfPlane(0));
				rayInfo[0].setAttribute(GeoAttributes.DISTANCE_DEGREES,
																fastRay.getBranches().get(0).angleDegrees() +
																fastRay.getBranches().get(1).angleDegrees());
			}
			logBuffer.setLength(0);
			return rayInfo;
		}
		catch(Exception ex)
		{
			if (ex.getMessage() == null)
				errorMessages.append("null pointer exception");
			else
				errorMessages.append(ex.getMessage());

			errorMessages.append(System.getProperty("line.separator"));
			errorMessages.append(String.format("Version = %s%n", getVersion()));

			errorMessages.append(String.format("Receiver = %1.8f, %1.8f, %1.6f%n",
					receiver.getLatDegrees(), receiver.getLonDegrees(),
					receiver.getDepth()));

			errorMessages.append(String.format("Source = %1.8f, %1.8f, %1.6f%n",
					source.getLatDegrees(), source.getLonDegrees(),
					source.getDepth()));

			errorMessages.append(String.format("Phase = %s%n", request
					.getPhase().toString()));

			errorMessages.append(String.format("distance = %1.6f%n",
					receiver.distanceDegrees(source)));

			// Recreate the stack trace into the error String.
			for (int i = 0; i < ex.getStackTrace().length; i++)
				errorMessages.append(ex.getStackTrace()[i].toString()).append(
				"\n");

			//X rayInfo=new RayInfo[1];
			//X rayInfo[0] = new RayInfo(request, this, "ERROR");
			rayInfo = new RayInfo[1];
			rayInfo[0] = new RayInfo(request, this, "ERROR");
			
			ex.printStackTrace();
			try {
			  rayInfo[0].setErrorMessage(errorMessages.toString());
			} catch (NullPointerException x) {
			  x.printStackTrace();
			}
			rayInfo[0].setStatusLog(logBuffer.toString());
			rayInfo[0].setAttribute(GeoAttributes.CALCULATION_TIME, 
					(System.currentTimeMillis() - calcOriginTime) * 1e-3);
			if(logBuffer != null) logBuffer.setLength(0);
			return rayInfo;
		}
	}

	private boolean rayContainsUnrequestedDiffractions()
	{
		// invalidate ICB diffractions if not requested.

		if (!isICBDiffractionAllowed())
		{
			for (int i = 0; i < fastRay.getBranchBottoms().size(); ++i)
			{
				RaySegmentBottom bseg = fastRay.getBranchBottoms().get(i).getBottomSegment();
				
				if (bseg.getRayInterfaceName().equals("ICB")
						&& bseg.getRayTypeSetting() == RayType.TOP_SIDE_DIFFRACTION)
				{
					String msg = "Ray is invalid because property allowICBDiffraction is " +
											 "false and the ray diffracts along the ICB.%n";
					appendErrorMessage(msg, bseg.getActiveLayerName());
					return true;
				}
			}
		}

		// invalidate CMB diffractions if not requested.

		if (!isCMBDiffractionAllowed())
		{
			for (int i = 0; i < fastRay.getBranchBottoms().size(); ++i)
			{
				RaySegmentBottom bseg = fastRay.getBranchBottoms().get(i).getBottomSegment();
				
				if (bseg.getRayInterfaceName().equals("CMB")
						&& bseg.getRayTypeSetting() == RayType.TOP_SIDE_DIFFRACTION)
				{
					String msg = "Ray is invalid because property benderAllowCMBDiffraction is " +
											 "false and the ray diffracts along the CMB.%n";
					appendErrorMessage(msg, bseg.getActiveLayerName());
					return true;
				}
			}
		}

		// invalidate MOHO diffractions if not requested.

		if (!isMOHODiffractionAllowed())
		{
			for (int i = 0; i < fastRay.getBranchBottoms().size(); ++i)
			{
				RaySegmentBottom bseg = fastRay.getBranchBottoms().get(i).getBottomSegment();
				
				if (bseg.getRayInterfaceName().equals("MOHO")
						&& bseg.getRayTypeSetting() == RayType.TOP_SIDE_DIFFRACTION)
				{
					String msg = "Ray is invalid because property benderAllowMOHODiffraction is " +
											 "false and the ray diffracts along the MOHO.%n";
					appendErrorMessage(msg, bseg.getActiveLayerName());
					return true;
				}
			}
		}

		return false;
	}

	private void appendErrorMessage(String msg, String activeLayerName)
	{
		getErrorMessages().append(String.format(msg));
		getErrorMessages().append(String.format("Version = %s%n", Bender.getVersion()));

		getErrorMessages().append(String.format("Receiver = %1.8f, %1.8f, %1.6f%n",
				currentReceiverProfile.getLatitudeDegrees(), currentReceiverProfile.getLongitudeDegrees(),
				currentReceiverProfile.getDepth()));

		getErrorMessages().append(String.format("Source = %1.8f, %1.8f, %1.6f%n",
				currentSourceProfile.getLatitudeDegrees(), currentSourceProfile.getLongitudeDegrees(),
				currentSourceProfile.getDepth()));

		getErrorMessages().append(String.format("Phase = %s%n",
					getPhaseRayBranchModel().getSeismicPhase().toString()));

		getErrorMessages().append(String.format("layer = %s%n", activeLayerName));

		getErrorMessages().append(String.format("distance = %1.6f%n",
				currentReceiverProfile.distanceDegrees(currentSourceProfile)));
	}

	private void mapBottomSideReflections() throws Exception
	{
//		boolean useDist = false;

	  updateFromUndersideReflectionLatLonvector = true;
	  undersideReflectionLatLonvector = new double [2*phaseRayBranchModel.getUndersideReflectionCount()];
	  
	  
	  boolean useDistDepthMap = false;
	  if (useDistDepthMap)
	  {
		  String outFmt = "%5d  %5d  %14.6f  %14.6f  %14.6f  %14.6f  %14.6f  %14.6f\n";
			String outputFile = "//old_computer/gnem/devlpool/jrhipp/UnderSideReflectionTests/SALSA3D/PP/TransvereSouthAmerica_Dist25.txt";
			BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
	  	output.write("SALSA3D BP Distance/Source Depth Map for 25 deg src/rcvr Separation Transverse Nazca/South American Plate Crossing ... " + Globals.getTimeStamp() + "\n");
	  	
	  	int    nDepth   = 50;
	  	double depthMin = 0.0;
	  	double depthMax = 700.0;
	  	
	  	int    nDist    = 100;
	  	double distMinF = .1;
	  	double distMaxF = .9;
	  	
//	  	int    nDepth   = 50;
//	  	double depthMin = 1.0;
//	  	double depthMax = 700.0;
//	  	
//	  	int    nDist    = 100;
//	  	double distMinF = .03/15;
//	  	double distMaxF = .2;
	  	
	  	double distMin  = distMinF * sourceToReceiverDistance;
	  	double distMax  = distMaxF * sourceToReceiverDistance;
	  	
      output.write(String.format("Source (lat, lon) = %14.6f, %14.6f\n",
      														currentSourceProfile.getLatitudeDegrees(),
      														currentSourceProfile.getLongitudeDegrees()));
      output.write(String.format("Receiver (lat, lon, depth) = %14.6f, %14.6f, %14.6f\n",
      														currentSourceProfile.getLatitudeDegrees(),
      														currentSourceProfile.getLongitudeDegrees(),
      														currentSourceProfile.getDepth()));
      output.write(String.format("Phase = %s\n",
      														phaseRayBranchModel.getSeismicPhase().name()));
      output.write(String.format("Source-Receiver Distance = %14.6f\n",
      														sourceToReceiverDistance));

      output.write(String.format("DistMinFraction,  DistMaxFraction = %14.6f,  %14.6f\n", distMinF,  distMaxF));
      output.write(String.format("DistMin,  DistMax,  NDist  = %14.6f,  %14.6f,  %5d\n", distMin,  distMax,  nDist));
      output.write(String.format("DepthMin, DepthMax, NDepth = %14.6f,  %14.6f,  %5d\n", depthMin, depthMax, nDepth));
      output.write("Data Format: idepth, idist, depth, dist, lat, lon, ttDepth[idist], slDepth[idist] \n");

      double[][] tt = new double [nDepth][nDist];
      double[][] sl = new double [nDepth][nDist];
      double delDepth = (depthMax - depthMin) / (nDepth - 1);
    	double delDist = (distMaxF - distMinF) / (nDist - 1);

    	for (int idepth = 0; idepth < nDepth; ++idepth)
      {
      	double depth = delDepth * idepth + depthMin;
      	currentSource.setDepth(depth);
      	currentSourceProfile.setDepth(depth);
  		  GeoTessPosition gtp = currentSourceProfile.deepClone();
      
  		  double[] ttDepth = tt[idepth];
  		  double[] slDepth = sl[idepth];
      	for (int idist = 0; idist < nDist; ++idist)
      	{
      		double distF = delDist * idist + distMinF;
      		double dist  = distF * sourceToReceiverDistance;

  				gtp.setIntermediatePosition(currentSourceProfile, currentReceiverProfile, distF);
  				double lat = gtp.getLatitude();
  				double lon = gtp.getLongitude();
  				undersideReflectionLatLonvector[0] = lat;
  				undersideReflectionLatLonvector[1] = lon;

  				// calculate ray

  				Ray ray = new Ray(this, getReceiverProfile(), getSourceProfile(), true);
  			  ttDepth[idist] = ray.getTravelTime();
          slDepth[idist] = ray.getBouncePointFitness(0);

          output.write(String.format(outFmt, idepth, idist, depth, dist, lat, lon, ttDepth[idist], slDepth[idist]));
      	}
      }
			output.close();

	  	return;
	  }
	  else
	  {
	  	// loop over distance along separate off-GC paths to source receiver.
	  	// let lon lat = gc normal (n) rotated by a small angle (+-)
	  	// bsr = vdist * cos ang + n * sin ang
	  	// n = |src x rcvr|
	  	
	  	// get vector along gc at some distance i
	  	// calculate bsr(i,j) = vdist(i) * cos(ang(j)) + n * sin(ang(j))

	  	int    nDist    = 51;
	  	double distMinF = .25;
	  	double distMaxF = .75;
	  	//int    nDist    = 51;
	  	//double distMinF = .25;
	  	//double distMaxF = .75;
    	double delDist = (distMaxF - distMinF) / (nDist - 1);

    	double distMin  = distMinF * sourceToReceiverDistance;
	  	double distMax  = distMaxF * sourceToReceiverDistance;

    	int    nOffGC   = 11;
    	double offGCMin = -0.1;
    	double offGCMax =  0.1;
    	//int    nOffGC   = 26;
    	//double offGCMin = -0.45;
    	//double offGCMax =  0.55;
    	double delOffGC = (offGCMax - offGCMin) / (nOffGC - 1);

    	String outFmt = "%5d  %5d  %14.6f  %14.6f  %14.6f  %14.6f  %14.6f  %14.6f\n";
			//String outputFile = "//old_computer/gnem/devlpool/jrhipp/UnderSideReflectionTests/SALSA3D/PP/badRay_Dist20_Depth1_offGC.txt";
			String outputFile = "//old_computer/gnem/devlpool/jrhipp/UnderSideReflectionTests/SALSA3D/PP/badPP_Dist14_Depth43_offGC_v2.txt";
			BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
	  	output.write("SALSA3D BP Distance/OffGC Ang Map for 14 deg src/rcvr Separation, 43.7 km src depth  ... " + Globals.getTimeStamp() + "\n");

      output.write(String.format("Source (lat, lon) = %14.6f, %14.6f\n",
      														currentSourceProfile.getLatitudeDegrees(),
      														currentSourceProfile.getLongitudeDegrees()));
      output.write(String.format("Receiver (lat, lon, depth) = %14.6f, %14.6f, %14.6f\n",
      														currentSourceProfile.getLatitudeDegrees(),
      														currentSourceProfile.getLongitudeDegrees(),
      														currentSourceProfile.getDepth()));
      output.write(String.format("Phase = %s\n",
      														phaseRayBranchModel.getSeismicPhase().name()));
      output.write(String.format("Source-Receiver Distance = %14.6f\n",
      														sourceToReceiverDistance));

      output.write(String.format("DistMinFraction,  DistMaxFraction = %14.6f,  %14.6f\n", distMinF,  distMaxF));
      output.write(String.format("DistMin,  DistMax,  NDist  = %14.6f,  %14.6f,  %5d\n", distMin,  distMax,  nDist));
      output.write(String.format("OffAngGCMin, OffAngGCMax, NOffGC= %14.6f,  %14.6f,  %5d\n", offGCMin, offGCMax, nOffGC));
      output.write("Data Format: idepth, idist, depth, dist, lat, lon, ttDepth[idist], slDepth[idist] \n");

	  	double[] n = new double [3];
	  	VectorUnit.crossNormal(currentSourceProfile.getVector(),
	  												 currentReceiverProfile.getVector(), n);
	  	double[] bsr = new double [3];
		  GeoTessPosition gtp = currentSourceProfile.deepClone();

      double[][] tt = new double [nOffGC][nDist];
      double[][] sl = new double [nOffGC][nDist];
		  for (int jOffGC = 0; jOffGC < nOffGC; ++jOffGC)
		  {
		  	double angOffGC = delOffGC * jOffGC + offGCMin;

  		  double[] ttDepth = tt[jOffGC];
  		  double[] slDepth = sl[jOffGC];
	    	for (int idist = 0; idist < nDist; ++idist)
	    	{
	    		double distF = delDist * idist + distMinF;
	    		double dist  = distF * sourceToReceiverDistance;

	    		// set gtp to the position along the source to receiver GC at a
	    		// distance fraction of the total distance equal to distF. Then rotate
	    		// that position toward the normal by an angle of angOffGC. Finally,
	    		// retrieve the latitude/longitude position of the bounce point and
	    		// set undersideReflectionLatLonvector.

					gtp.setIntermediatePosition(currentSourceProfile, currentReceiverProfile, distF);
			  	Vector3D.addMult(bsr, Math.cos(Math.toRadians(angOffGC)),
			  									 gtp.getVector(), Math.sin(Math.toRadians(angOffGC)),
			  									 n);
			  	double lat = geoTessModel.getEarthShape().getLat(bsr);
			  	double lon = geoTessModel.getEarthShape().getLon(bsr);
  				undersideReflectionLatLonvector[0] = lat;
  				undersideReflectionLatLonvector[1] = lon;

  				// calculate ray

				  Ray ray = new Ray(this, getReceiverProfile(), getSourceProfile(), true);
  			  ttDepth[idist] = ray.getTravelTime();
          slDepth[idist] = ray.getBouncePointFitness(0);
    		  fastRay = ray;

          output.write(String.format(outFmt, jOffGC, idist, angOffGC, dist, lat, lon, ttDepth[idist], slDepth[idist]));
	    	}
		  }

		  output.close();
  	  return;
	  }

	  // new map function as of 9/27/2016
	  // given input source/receiver position
	  //   open output file
	  //     write descriptor
	  //     write depthmin, depthmax, ndepth
	  //     write distmin, distmax, ndist
	  //   for source depth from 0 to maxSrcDepth using ndepth depths
	  //     set source depth
	  //     for bounce point distance from distmin to distmax using ndist distances
	  //       set bounce point position
	  //       calculate ray
	  //       save idepth, depth, fdepth, idist, dist, fdist, lat, lon, tt, sl
	  //   close file
	  // exit

//		if (useDist)
//		{
//		  String outFmt = "%14.6f  %14.6f  %14.6f  %14.6f\n";
//			String outputFile = "//old_computer/gnem/devlpool/jrhipp/BottomSideReflectionProfileMap.txt";
//			BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
//
//			int nDist = 500;
//			double distmin = .1;
//			double distmax = .9;
//		  double deldst = (distmax - distmin) / (nDist - 1);
//		  GeoTessPosition gtp = currentSourceProfile.deepClone();
//		  double[] tt = new double [nDist];
//		  double[] sl = new double [nDist];
//		  double[] dist = new double [nDist];
//			for (int i = 0; i < nDist; ++i)
//			{
//				dist[i] = deldst * i + distmin;
//				gtp.setIntermediatePosition(currentSourceProfile, currentReceiverProfile, dist[i]);
//				double lat = gtp.getLatitude();
//				double lon = gtp.getLongitude();
//				undersideReflectionLatLonvector[0] = lat;
//				undersideReflectionLatLonvector[1] = lon;
//
//				// calculate ray
//
//				Ray ray = new Ray(this, getReceiverProfile(), getSourceProfile(), true);
//			  tt[i] = ray.getTravelTime();
//        sl[i] = ray.getBouncePointFitness(0);
//
//        output.write(String.format(outFmt, lat, lon, tt[i], sl[i]));
//			}
//			output.close();
//			return;
//		}
//		else
//		{
//		}

		// vary each bsr from closest grid point to maxgapdeg+ to maxgapdeg- from
		// next bsr or receiver
		// dist src->rcvr
		// divide by n to get del
		// for i=0; i<n; ++i
		//   pt = del * i;
		// bsr[0] = first i such that pt[i] >= maxgapdeg;
		// bsr[1] = first i such that pt[i] >= maxgapdeg + pt[bsr[0]]
		// ...
		
		// increment bsr[n-1] from i= bsr[n-1] to pt[i] <= dist - maxgapdeg
		// increment bsr[n-2] 
		// when last hits end
		// while next to last can move over
		//   increment next to last
		//   else go to one before next to last
		// k = j-1
		// if bsr[k] + 1
		
		// let Ni = number of points
		// let Nbsr = numbr of BSRs
		// let Del  = min gap between BSRs
		// let B[k] = kth BSR point index
		// ...
		// while (true)
		// {
		//   while B[0] < Ni - Del
		//   {
		//     solve for ray with current bsr defintion
		//     save tt and bsr indexes
		//     ++B[0];
		//   }
		// 
		//   k = 1;
		//   while B[k] = Ni - (k+1) * Del && k < Nbsr-1
		//     ++k;
		// 
		//   if (k == Nbsr-1 && B[k] = Ni - (k+1) * Del)
		//     break;
		//   else
		//     ++B[k]
		//     for (j = k-1; j >= 0; --j)
		//       B[j] = B[j-1] + Del;
		//
		// }
//		
//		int Ni = 26;
//		int Nbsr = 3;
//		int Del  = 3;
//		int[] B = new int [Nbsr];
//		B[0] = Del;
//		for (int j = 1; j < Nbsr; ++j) B[j] = B[j-1] + Del;
//
//		boolean cont = true;
//		while (cont)
//		{
//			 while (B[Nbsr-1] < Ni - Del)
//			 {
//		     // solve for ray with current bsr defintion
//				 for (int k = 0; k < Nbsr; ++k)
//				   if (k < Nbsr-1)
//					    System.out.print(B[k] + ",");
//				   else
//					    System.out.println(B[k]);
//		     ++B[Nbsr-1];
//			 }
//		   int k = Nbsr - 2;
//		   while ((B[k] == Ni - (Nbsr - k - 1) * Del) && (k >= 0)) --k;
//
//		   if ((k == 0) && (B[0] == Ni - (Nbsr - 1) * Del))
//		  	 cont = false;
//		   else
//		   {
//		  	 ++B[k];
//		 		 for (int j = k+1; j < Nbsr; ++j)
//		 		   B[j] = B[j-1] + Del;
//		   }
//		}

	}
	
//	private class AmoebaBouncePoint extends Amoeba
//	{
//		private double[]  travelTime = null;
//		private double[]  snellsLawMisfit = null;
//		private GeoTessPosition[] simplexPos = null;
//
//		private double    currentTravelTime = 0.0;
//		private double    currentSnellsLawMisfit = 0.0;
//
//		private double    minTTDiff = 0.001;
//		private double    minBPMove = 1.0;
//
//		private boolean   redefineSimplex = false;
//
//		public void setMinTTDiffAndBPMove(double mttd, double mbpm)
//		{
//			minTTDiff = mttd;
//			minBPMove = mbpm;
//		}
//
//		// override swap, replace, replaceHighPoint and initial setup in simplex
//		public AmoebaBouncePoint(double[][] simplexPoints) throws Exception
//		{
//			super(simplexPoints);
//			travelTime = new double [simplexPoints.length];
//			for (int i = 0; i < simplexPoints.length; ++i)
//				travelTime[i] = Globals.NA_VALUE;
//			snellsLawMisfit = new double [simplexPoints.length];
//			simplexPos = new GeoTessPosition [simplexPoints.length];
//		}
//
//		@Override
//		public void redefine()
//		{
//			// redefine point 3 of the simplex using so that it is a well-formed 
//		  // triangle again
//			if (redefineSimplex)
//			{
//				try
//				{
//				  redefineSimplex();
//				}
//				catch (Exception ex)
//				{
//					ex.printStackTrace();
//				}
//				redefineSimplex = false;
//			}
//		}
//
//		public void redefineSimplexPoints()
//		{
//			redefineSimplex = true;
//		}
//
//		@Override
//		public boolean isConverged(double tolerance)
//		{
//			if (isTravelTimeToleranceMinimum() && (super.isConverged(tolerance) ||
//					isTravelTimeDifferenceMinimum(minTTDiff) ||
//					isBadBouncePointSeparationMinimum(minBPMove / 2.0)))
////				  isBouncePointSeparationMinimum(minBPMove))
//				return true;
//			else
//				return false;
//		}
//
//		public boolean isTravelTimeDifferenceMinimum(double minTTDiff)
//		{
//			if ((Math.abs(travelTime[1] - travelTime[0]) < minTTDiff) && 
//					(Math.abs(travelTime[2] - travelTime[1]) < minTTDiff) &&
//					(Math.abs(travelTime[0] - travelTime[2]) < minTTDiff))
//				return true;
//			else
//				return false;
//		}
//
//		public boolean isBouncePointSeparationMinimum(double minBPMove)
//		{
//			if ((Vector3D.distance3D(simplexPos[0].get3DVector(),
//					 simplexPos[1].get3DVector()) < minBPMove) &&
//					(Vector3D.distance3D(simplexPos[1].get3DVector(),
//					 simplexPos[2].get3DVector()) < minBPMove) &&
//					(Vector3D.distance3D(simplexPos[2].get3DVector(),
//					 simplexPos[0].get3DVector()) < minBPMove))
//				return true;
//			else
//				return false;
//		}
//
//		public boolean isBadBouncePointSeparationMinimum(double minBPMove)
//		{
//			if (Vector3D.distance3D(simplexPos[1].get3DVector(),
//					 simplexPos[2].get3DVector()) < minBPMove)
//				return true;
//			else
//				return false;
//		}
//
//		/**
//		 * Returns true if all travel time values for the simplex have been
//		 * initialized;
//		 * @return True if all travel time values for the simplex have been
//		 * 				 initialized;
//		 */
//		public boolean isInitialized()
//		{
//			return ((travelTime[0] != Globals.NA_VALUE) &&
//							(travelTime[1] != Globals.NA_VALUE) &&
//							(travelTime[2] != Globals.NA_VALUE));
//		}
//
//		public double getMeanTravelTimeDifference()
//		{
//			 return (Math.abs(travelTime[0] - travelTime[1]) +
//							 Math.abs(travelTime[1] - travelTime[2]) +
//							 Math.abs(travelTime[2] - travelTime[0])) / 3.0;
//		}
//
//		public double getMeanBouncePointMovement()
//		{
//			 return (Vector3D.distance3D(simplexPos[0].get3DVector(),
//					 													simplexPos[1].get3DVector()) +
//							 Vector3D.distance3D(simplexPos[1].get3DVector(),
//									 									simplexPos[2].get3DVector()) +
//							 Vector3D.distance3D(simplexPos[2].get3DVector(),
//									 									simplexPos[0].get3DVector())) / 3.0;
//		}
//
//		// when a simplex point is set/replaced the y (dtt) and p (lat, lon) of the
//		// point is modified. At that time the traveltime[], snellsLawMisfit[], and
//		// simplexPos[] must be set from the current equivalents.
//		// 
//
//		@Override
//		protected void replace(int i, double[] pnew, double ynew) throws Exception
//		{
//			super.replace(i, pnew, ynew);
//			travelTime[i] = currentTravelTime;
//			snellsLawMisfit[i] = currentSnellsLawMisfit;
//			simplexPos[i] = currentBPPosition.deepClone();
//		}
//
//		@Override
//		protected void swap(int i, int j)
//		{
//			super.swap(i, j);
//			double tt = travelTime[i];
//			travelTime[i] = travelTime[j];
//			travelTime[j] = tt;
//			double slmf = snellsLawMisfit[i];
//			snellsLawMisfit[i] = snellsLawMisfit[j];
//			snellsLawMisfit[j] = slmf;
//			GeoTessPosition p = simplexPos[i];
//			simplexPos[i] = simplexPos[j];
//			simplexPos[j] = p;
//		}
//
//		// create a new AmoebaBouncePoint abp
//		//
//		//			for (int i=0; i<simplex.p.length; ++i)
//		//        simplex.y[i] = function.simplexFunction(simplex.p[i]);
//		//        set traveltime and snellslawmisfit
//    //
//		// call simplex.search(abp)
//	}

	// Tolerance parameters
	// 
	//    Start tolerances:
	// 	    currentTTTol = .1;
	// 	    currentMinNodeSpacing = 90;
	//    End Tolerances:
	//      convergenceCriteria[0][1] // travel time
	//      convergenceCriteria[0][2] // min node spacing
	//
 	// 	  nTolReductions = 6 // number of tt/minNodeSpc reductions from start to
	//                          end
	//
	//    simplexAngleDelta = 0.5 // delta angle separation from the last Brents
	//                               point to each of the initial simplex triangle
	//                               vertices in degrees.
	//                               Could set as f * sourceToReceiverDistance with
	//                               f = 0.02 for 25 degree source to receiver
	//                               separation.
	//
	//     minBPMove = 1.0 // minimum bounce point move distance (km).
	
	private void optimizeBouncePoints() throws Exception
	{
		nResetRay = nNewRay = 0;
		//optimizeSnellsLaw = true;

		bpStart = currentSourceProfile.deepClone();
		bpEnd   = currentReceiverProfile.deepClone();
		ngc = VectorUnit.crossNormal(currentSourceProfile.getVector(),
																 currentReceiverProfile.getVector());

		// define starting tt tolerance and min node spacing. Then define the
		// number of reductions that can occur before reaching the predefined
		// Bender convergence criteria.

		currentTTTol = .25;
		currentMinNodeSpacing = 100;
		nTolReductions = 6;

		// calculate the tt and min node spacing reduction factors.

	  ttTolReduction = Math.pow(currentTTTol / currentRayConvgncCriteria[0], 1.0 / nTolReductions);
	  minNodeSpcReduction = (currentMinNodeSpacing - currentRayConvgncCriteria[1]) / nTolReductions;

	  // initialize tt and bp movement parameters.

		nextToLastTT = 1000 * currentTTTol;
		lastTT = 100 * currentTTTol;
		//nextToLastBPMove = 10000.0;
		//lastBPMove = 1000.0;

		// setup brents only search to about 1/4 of the simplexAngleDelta range
		// over the source to receiver distance. This ensures that Brents doesn't
		// waste any time trying to find an accurate min/max that simplex is going
		// to enlarge.

		double simplexAngleDelta = 0.5;
    Brents brents = new Brents();
    brents.setTolerance(simplexAngleDelta / sourceToReceiverDistance / 4.0);

    for (int i = 0; i < phaseRayBranchModel.size(); ++i)
    {
  		if (phaseRayBranchModel.getRayBranchDirectionChangeType(i) == RayDirection.BOTTOM_SIDE_REFLECTION)
  		{
  			bpLayer = phaseRayBranchModel.getRayBranchInterfaceIndex(i);
  			break;
  		}
    }

		bpStart.setTop(bpLayer);
		bpEnd.setTop(bpLayer);
		currentBPPosition = bpStart.deepClone();
		lastBPPosition = bpStart.deepClone();

    // call maxF if phase is XX and minF if phase is xX
    // bpRay has the optimal point on exit.

		bpRay = null;
    if (phaseRayBranchModel.isDepthPhase())
    {
      brents.minF(0.0,  0.5, this);
      //simplexTTDepthPhaseScale = 1.0 / lastTT; 
      if (lastValidXBrentsxX >= 0.0)
      	bFunc(lastValidXBrentsxX);
    }
    else
      brents.maxF(0.0,  1.0, this);

    // initialize for simplex

    //double dist = Vector3D.distance3D(currentBPPosition.get3DVector(),
    //																	lastBPPosition.get3DVector());
		nextToLastTT = lastTT;
		lastBPPosition = currentBPPosition.deepClone();

    // Setup simplex point:
		//   a) Given current bp position rotate along +gc to a point
		//      simplexAngleDelta degrees ahead from the current bp and
		//      then rotate toward the normal (ngc) by same distance.
		//   b) Get lat and lon and set that as first simplex point.
		//   c) Then from the point simplexAngleDelta ahead of the current bounce
		//      point rotate toward -ngc by same distance.
		//   d) Get lat and lon and set that as second simplex point
		//   e) Finally, rotate backward along -gc from the current bounce point
		//      to a point simplexAngleDelta degrees behind it.
		//   f) Get lat and lon and set that as third simplex point

		double[][] simplexPoints = new double [3][2];
		amoeba = new AmoebaBouncePoint(this, simplexPoints);
		amoeba.setMinTTDiffAndBPMove(currentRayConvgncCriteria[0], minBPMove);

		// get angle from start position to curretn bounce point and add simplex
		// angle delta to it and scale by source to receiver distance to get
		// fraction ahead of bounce point.
		
		double a = VectorUnit.angleDegrees(bpStart.getVector(),
																			 currentBPPosition.getVector());
		if (simplexAngleDelta > a) simplexAngleDelta = a;

		double f = (a + simplexAngleDelta) / sourceToReceiverDistance;

		// if the fraction is >= than 1.0 then adjust the simplex angle delta to a
		// smaller value and recalculate the fraction.

		if (f >= 1.0)
		{
			simplexAngleDelta = (sourceToReceiverDistance - a) / 2.0;
			f = (a + simplexAngleDelta) / sourceToReceiverDistance;
		}

		// get position ahead of current bounce point along +gc into v1 ... then
		// rotate v1 toward +ngc by simplexAngleDelta and save into v2
		double[] v1 = {0.0, 0.0, 0.0};
		double[] v2 = {0.0, 0.0, 0.0};

		//currentBPPosition.setIntermediatePosition(bpStart, bpEnd, x, bpLayer);
		VectorUnit.rotatePlane(bpStart.getVector(), bpEnd.getVector(), f, v1);
		VectorUnit.rotateVector(v1, ngc, Math.toRadians(simplexAngleDelta), v2);
		
		// set v2 into first amoeba simplex point
		setBouncePointInitialSimplexPoint(0, v2, simplexPoints);
//		amoeba.simplexPos[0] = bpStart.deepClone();
//		amoeba.simplexPos[0].setTop(bpLayer, v2);
//		simplexPoints[0][0] =  amoeba.simplexPos[0].getLatitude();
//		simplexPoints[0][1] =  amoeba.simplexPos[0].getLongitude();
		
		// now rotate v1 toward -ngc by simplexAngleDelta and save into v2
		VectorUnit.rotateVector(v1, ngc, Math.toRadians(-simplexAngleDelta), v2);

		// set v2 into second amoeba simplex point
		setBouncePointInitialSimplexPoint(1, v2, simplexPoints);
//		amoeba.simplexPos[1] = bpStart.deepClone();
//		amoeba.simplexPos[1].setTop(bpLayer, v2);
//		simplexPoints[1][0] =  amoeba.simplexPos[1].getLatitude();
//		simplexPoints[1][1] =  amoeba.simplexPos[1].getLongitude();
		
		// finally rotate current bounce point in the negative gc direction

		f = (a - simplexAngleDelta) / sourceToReceiverDistance;

		// if the fraction is <= than 0.0 then adjust the simplex angle delta to a
		// smaller value and recalculate the fraction.

		if (f <= 0.0)
		{
			f = a / sourceToReceiverDistance / 2.0;
		}

		// get position behind current bounce point along +gc into v1
		VectorUnit.rotatePlane(bpStart.getVector(), bpEnd.getVector(), f, v1);

		// set v1 into third amoeba simplex point
		setBouncePointInitialSimplexPoint(2, v1, simplexPoints);
//		amoeba.simplexPos[2] = bpStart.deepClone();
//		amoeba.simplexPos[2].setTop(bpLayer, v1);
//		simplexPoints[2][0] =  amoeba.simplexPos[2].getLatitude();
//		simplexPoints[2][1] =  amoeba.simplexPos[2].getLongitude();

		// set initial simplex travel time tolerance and min node spacing to their
		// mid level tolerance settings
		
		currentTTTol = currentRayConvgncCriteria[0] *
									 Math.pow(ttTolReduction, nTolReductions/2);
		currentMinNodeSpacing = minNodeSpcReduction * nTolReductions / 2 +
														currentRayConvgncCriteria[1];

		// create simplex and amoeba
		//Simplex simplex = new Simplex(this, 5e-4, 2000);
		Simplex simplex = new Simplex(this, 5e-3, 2000);
		//Simplex simplex = new Simplex(this, 1e-6, 2000);
		
		amoeba.setSimplex(simplex);

		//nextToLastBPMove = 0.0;

		//set lstBPMove to approximate between 1st and 2nd simplexPoints
		//lastBPMove = Vector3D.distance3D(amoeba.simplexPos[0].get3DVector(),
		//																 amoeba.simplexPos[2].get3DVector()) ;


		// set initial simplex values
		for (int i = 0; i < simplexPoints.length; ++i)
		{
			amoeba.y[i] = simplexFunction(simplexPoints[i]);
			amoeba.replace(i, simplexPoints[i], lastTT);
      if ((i == 2) && (amoeba.currentValidResult != 0))
      {
    		f -= .01;

    		// if the fraction is <= than 0.0 then adjust the simplex angle delta to a
    		// smaller value and recalculate the fraction.

    		if (f <= 0.0)
    		{
    			f += .01;
    			f /= 2;
    		}

    		// get position behind current bounce point along +gc into v1
    		VectorUnit.rotatePlane(bpStart.getVector(), bpEnd.getVector(), f, v1);
    		setBouncePointInitialSimplexPoint(2, v1, simplexPoints);
    		--i;
      }
		}

		// perform search ... on exit bpRay has the ray with the optimized bounce
		// point

		simplex.search(amoeba);

		// make sure last point is best point (simplex point 0) which is typically
		// not the case. If not recalculate the ray at the best point

		if (!Vector3D.equals(amoeba.simplexPos[0].get3DVector(),
												 currentBPPosition.get3DVector()))
		{
//			calculateBouncePointRay(amoeba.simplexPos[2], currentBPPosition);
//			double tt = bpRay.getTravelTime();
//			double sl = bpRay.getBouncePointFitness(0);
//			calculateBouncePointRay(amoeba.simplexPos[1], currentBPPosition);
//			tt = bpRay.getTravelTime();
//			sl = bpRay.getBouncePointFitness(0);
			calculateBouncePointRay(amoeba.simplexPos[0], currentBPPosition);
//			tt = bpRay.getTravelTime();
//			sl = bpRay.getBouncePointFitness(0);
//			int iii = 0;
//			++iii;
		}
		fastRay = bpRay;

		// if ((amoeba.ttTol[0] != convergenceCriteria[0][1]) ||
		//     (amoeba.ttTol[1] != convergenceCriteria[0][1]) ||
		//     (amoeba.ttTol[2] != convergenceCriteria[0][1]))
		
		
//		double sl = bpRay.getBouncePointFitness(0);
//		//if (Math.abs(sl) >= 0.1)
//		if (Math.abs(sl) >= 0.4)
//		{
//        fastRay.setInvalid();
//			throw new BenderException(ErrorCode.NONFATAL, "Error: Invalid Bounce Point Snells Law Result = " +
//																sl + "\n");
//		}
		
		
		if ((amoeba.simplexValidPoint[0] == 2) || (amoeba.simplexValidPoint[1] == 2) ||
				(amoeba.simplexValidPoint[2] == 2))
		{
        fastRay.setInvalid();
	    if (phaseRayBranchModel.isDepthPhase())
	    {
	    	throw new BenderException(ErrorCode.NONFATAL, "Error: Bounce Point Solution is near an invalid depth phase boundary \n");
	    }
	    else 
	    	throw new BenderException(ErrorCode.NONFATAL, "Error: Bounce Point Solution is near an invalid ray bottom boundary \n");
		}
	}

	private void setBouncePointInitialSimplexPoint(int i, double[] v,
																								 double[][] smplxPoints)
					throws GeoTessException
	{
		// set v1 into third amoeba simplex point

		//amoeba.simplexPos[i] = bpStart.deepClone();
		amoeba.simplexPos[i] = GeoTessPosition.getGeoTessPosition(bpStart);
		amoeba.simplexPos[i].setTop(bpLayer, v);
		smplxPoints[i][0] =  amoeba.simplexPos[i].getLatitude();
		smplxPoints[i][1] =  amoeba.simplexPos[i].getLongitude();
	}

	protected boolean isTravelTimeToleranceMinimum()
	{
		return (currentTTTol == currentRayConvgncCriteria[0]) ? true : false;
	}

//
//	private boolean hasInvalidBranch(Ray ray)
//	{
//	  if (ray.getRayType() == RayType.ERROR ||
//				ray.getRayType() == RayType.INVALID)
//	  	return true;
//	  else
//	  	return false;
//	}
//	
//	private boolean hasDiffractedBranch(Ray ray)
//	{
//		for (int i = 0; i < ray.getBranches().size(); ++i)
//		{
//			RayBranch rb = ray.getBranches().get(i);
//			if (rb.getRayTypeName().equals("DIFFRACTION"))
//				return true;
//		}
//		return false;
//	}
//	
//	private boolean hasReflectedBranch(Ray ray)
//	{
//		for (int i = 0; i < ray.getBranches().size(); ++i)
//		{//			RayBranch rb = ray.getBranches().get(i);
//			if (rb.getRayTypeName().equals("REFLECTION"))
//				return true;
//		}
//		return false;
//	}
//
//	/**
//	 * Performs the lBFGS optimization to find the maximum travel time value of a
//	 * multi-branch ray separated by bottom-side reflections between each branch.
//	 * 
//	 * @param source    The input ray source position.
//	 * @param receiver  The input ray receiver position.
//	 * @return fastest ray optimized on bottom-side reflections.
//	 * 
//	 * @throws LBFGSException
//	 */
//	private Ray optimizeBottomSideReflections(GeoVector source, GeoVector receiver) throws LBFGSException
//	{
//	  // set bottom side vector
//		
//	  undersideReflectionLatLonvector = new double [2*phaseRayBranchModel.getUndersideReflectionCount()];
//		double[] z = {0.0, 0.0, 0.0};
//	  for (int i = 0; i < phaseRayBranchModel.getUndersideReflectionCount(); ++i)
//	  {
//	  	int uri = phaseRayBranchModel.getBMIndexFromBSRIndex(i);
//			double f = phaseRayBranchModel.getFixedReflectionInitialAngleFraction(uri);
//			VectorUnit.rotatePlane(source.getUnitVector(), receiver.getUnitVector(), f, z);
//			undersideReflectionLatLonvector[2*i]   = getGeoTessModel().getEarthShape().getLat(z);
//			undersideReflectionLatLonvector[2*i+1] = getGeoTessModel().getEarthShape().getLon(z);
//	  }
//
//	  // Output initial bottom-side reflection vector
//
//		if (getVerbosity() > 0)
//		{
//			println();
//			println("BSROPT  LBFGS BSR Optimization Start " + Globals.repeat("*", 120));
//			print("BSROPT    Initial Bottom-Side Vector (lat, lon ... in deg) = ");
//		  for (int i = 0; i < undersideReflectionLatLonvector.length; ++i)
//		  {
//		  	print(String.format("%8.3f", Math.toDegrees(undersideReflectionLatLonvector[i])));
//		  	if (i < undersideReflectionLatLonvector.length-1)
//		  			print(", ");
//		  }
//		  println(); println();
//		}
//
//	  // perform optimization
//
//	  updateFromUndersideReflectionLatLonvector = true;
//
//	  lBFGSFunctionMethodCallCount = 0;
//	  lBFGS.setLBFGSFunction(this);
//	  lBFGS.lbfgs(undersideReflectionLatLonvector);
//
//	  // Output final bottom-side reflection vector
//
//		if (getVerbosity() > 0)
//		{
//		  println();
//		  print("BSROPT    Final Bottom-Side Vector (lat, lon ... in deg) = ");
//		  for (int i = 0; i < undersideReflectionLatLonvector.length; ++i)
//		  {
//		  	print(String.format("%8.3f", Math.toDegrees(undersideReflectionLatLonvector[i])));
//		  	if (i < undersideReflectionLatLonvector.length-1)
//		  			print(", ");
//		  }
//		  println(); println();
//		  println("BSROPT  LBFGS BSR Optimization End   " + Globals.repeat("*", 120));
//		  println(); println();
//		}
//
//	  // done return fast ray
//
//		return fastRay;
//	}
//
//	private int simplexFunctionMethodCallCount = 0;
//	private Simplex					simplex;

	/**
	 * Used in simplex algorithm. The x and y coordinates of the three corners of
	 * the simplex.
	 */
//	private double[][]						simplex_p												= new double[3][2];
	//private double								simplexFitness = 0.0;

//	private HashMap<double[], Ray> simplexRayMap = null;

	private void setInitialBottomSideReflections(GeoVector source, GeoVector receiver) throws IOException
	{
	  undersideReflectionLatLonvector = new double [2*phaseRayBranchModel.getUndersideReflectionCount()];
		TauPPhaseBottomSideReflection bsrPoints = TauPPhaseBottomSideReflection.
				               												getTauPPhaseBSR(phaseRayBranchModel.getSeismicPhase());
		double[] z = {0.0, 0.0, 0.0};
		if (bsrPoints == null)
		{
		  // set bottom side vector from the Phase-Ray Branch Model initial guess.
		  for (int i = 0; i < phaseRayBranchModel.getUndersideReflectionCount(); ++i)
		  {
		  	int uri = phaseRayBranchModel.getBMIndexFromBSRIndex(i);
				double f = phaseRayBranchModel.getFixedReflectionInitialAngleFraction(uri);
				VectorUnit.rotatePlane(source.getUnitVector(), receiver.getUnitVector(), f, z);
				undersideReflectionLatLonvector[2*i]   = getGeoTessModel().getEarthShape().getLat(z);
				undersideReflectionLatLonvector[2*i+1] = getGeoTessModel().getEarthShape().getLon(z);
		  }
		}
		else
		{
			double f = bsrPoints.getBSRPoint(currentSourceProfile.getDepth(),
																			 sourceToReceiverDistance) /
																			 sourceToReceiverDistance;
			VectorUnit.rotatePlane(source.getUnitVector(), receiver.getUnitVector(), f, z);
			undersideReflectionLatLonvector[0] = getGeoTessModel().getEarthShape().getLat(z);
			undersideReflectionLatLonvector[1] = getGeoTessModel().getEarthShape().getLon(z);
		}
		
	  // Output initial bottom-side reflection vector
	  //xxx undersideReflectionLatLonvector[1] = Math.toRadians(22.974);
		if (getVerbosity() > 0)
		{
			println();
			println("BSROPT  SIMPLEX BSR Optimization Start " + Globals.repeat("*", 118));
			print("BSROPT    Initial Bottom-Side Vector (lat, lon ... in deg) = ");
		  for (int i = 0; i < undersideReflectionLatLonvector.length; ++i)
		  {
		  	print(String.format("%8.3f", Math.toDegrees(undersideReflectionLatLonvector[i])));
		  	if (i < undersideReflectionLatLonvector.length-1)
		  			print(", ");
		  }
		  println(); println();
		}

	  // set initial counters and Simplex method
	  updateFromUndersideReflectionLatLonvector = true;		
	}
//
//	/**
//	 * Performs the Nelder-Mead Simplex optimization on a ray containing one or
//	 * more Bottom-Side Reflections (BSRs) to find the BSR settings that maximize
//	 * the ray travel time.
//	 * 
//	 * @param source    The input ray source position.
//	 * @param receiver  The input ray receiver position.
//	 * @return fastest ray optimized on bottom-side reflections.
//	 * 
//	 * @throws LBFGSException
//	 */
//	private Ray optimizeBottomSideReflectionsSimplex(GeoVector source, GeoVector receiver)
//			        throws LBFGSException, IOException
//	{
//	  undersideReflectionLatLonvector = new double [2*phaseRayBranchModel.getUndersideReflectionCount()];
//		TauPPhaseBottomSideReflection bsrPoints = TauPPhaseBottomSideReflection.
//				               												getTauPPhaseBSR(phaseRayBranchModel.getSeismicPhase());
//		double[] z = {0.0, 0.0, 0.0};
//		if (bsrPoints == null)
//		{
//		  // set bottom side vector from the Phase-Ray Branch Model initial guess.
//		  for (int i = 0; i < phaseRayBranchModel.getUndersideReflectionCount(); ++i)
//		  {
//		  	int uri = phaseRayBranchModel.getBMIndexFromBSRIndex(i);
//				double f = phaseRayBranchModel.getFixedReflectionInitialAngleFraction(uri);
//				VectorUnit.rotatePlane(source.getUnitVector(), receiver.getUnitVector(), f, z);
//				undersideReflectionLatLonvector[2*i]   = getGeoTessModel().getEarthShape().getLat(z);
//				undersideReflectionLatLonvector[2*i+1] = getGeoTessModel().getEarthShape().getLon(z);
//		  }
//		}
//		else
//		{
//			double f = bsrPoints.getBSRPoint(currentSourceProfile.getDepth(),
//																			 sourceToReceiverDistance) /
//																			 sourceToReceiverDistance;
//			VectorUnit.rotatePlane(source.getUnitVector(), receiver.getUnitVector(), f, z);
//			undersideReflectionLatLonvector[0] = getGeoTessModel().getEarthShape().getLat(z);
//			undersideReflectionLatLonvector[1] = getGeoTessModel().getEarthShape().getLon(z);
//		}
//		
//	  // Output initial bottom-side reflection vector
//	  //*** undersideReflectionLatLonvector[1] = Math.toRadians(22.974);
//		if (getVerbosity() > 0)
//		{
//			println();
//			println("BSROPT  SIMPLEX BSR Optimization Start " + Globals.repeat("*", 118));
//			print("BSROPT    Initial Bottom-Side Vector (lat, lon ... in deg) = ");
//		  for (int i = 0; i < undersideReflectionLatLonvector.length; ++i)
//		  {
//		  	print(String.format("%8.3f", Math.toDegrees(undersideReflectionLatLonvector[i])));
//		  	if (i < undersideReflectionLatLonvector.length-1)
//		  			print(", ");
//		  }
//		  println(); println();
//		}
//
//	  // set initial counters and Simplex method
//	  updateFromUndersideReflectionLatLonvector = true;
//	  int nBSR = phaseRayBranchModel.getUndersideReflectionCount();
//		boolean done = false;
//		int ntries = 0, maxtries = 100;
//	  //simplexFunctionMethodCallCount = 0;
////		simplex = new Simplex(this, 1e-4, 2000);
//
//		// build simplex basis points (simplex_p)
//		simplex_p = new double [undersideReflectionLatLonvector.length + 1]
//				                   [undersideReflectionLatLonvector.length];
//		for (int i = 0; i < 2*nBSR+1; ++i)
//		{
//			double[] pi = simplex_p[i];
//			for (int j = 0; j < undersideReflectionLatLonvector.length; ++j)
//				pi[j] = undersideReflectionLatLonvector[j];
//		}
//
//    double[] uv1 = {0.0, 0.0, 0.0};
//    double[] uv2 = {0.0, 0.0, 0.0};
//    double[] uvN = {0.0, 0.0, 0.0};
//    VectorUnit.crossNormal(source.getUnitVector(), receiver.getUnitVector(), uvN);
//
//    // now offset the ith BSR twice, once toward Rcvr and once toward Normal of
//    // S x R (by a smaller amount). This forms two new points (2i and 2i+1) in
//    // the simplex_p array. initialize 2 * nBSR vectors (2 for each BSR)
//    
//    //*** new offset: the ith BSR three times, once toward Rcvr by 0.025, once
//    //*** back toward source by 0.025 and toward normal of S x R by 0.005, and
//    //*** once back toward source by 0.025 and toward normal of S x $ by -0.005.
//    for (int i = 0; i < nBSR; ++i)
//    {
//    	int i0 = 2*i;
//    	int i1 = i0+1;
//    	int i2 = i1+1;
//
//    	// get BSR[i] lat, lon as a unit vector
//    	double lat = undersideReflectionLatLonvector[i0];
//      double lon = undersideReflectionLatLonvector[i1];
//      getGeoTessModel().getEarthShape().getVector(lat, lon, uv1);
//
//      // rotate BSR[i] toward receiver by a small amount and save into uv2
//      // replace lat, lon elements (positions 2i and 2i+1) of vector 2i+1 with
//      // new position
//      VectorUnit.rotatePlane(uv1, receiver.getUnitVector(), .05, uv2);
//      simplex_p[i1][i0]   = getGeoTessModel().getEarthShape().getLat(uv2);
//      simplex_p[i1][i1] = getGeoTessModel().getEarthShape().getLon(uv2);
////	  	print(String.format("%8.3f", Math.toDegrees(simplex_p[i1][i0])));
////			print(", ");
////	  	print(String.format("%8.3f", Math.toDegrees(simplex_p[i1][i1])));
////		  println();
//
//      // rotate BSR[i] toward S x R  by a small amount and save into uv2
//      // replace lat, lon elements (positions 2i and 2i+1) of vector 2i+2 with
//      // new position
//      VectorUnit.rotatePlane(uv1, uvN, .01, uv2);
//      simplex_p[i2][i0]   = getGeoTessModel().getEarthShape().getLat(uv2);
//      simplex_p[i2][i1] = getGeoTessModel().getEarthShape().getLon(uv2);
////	  	print(String.format("%8.3f", Math.toDegrees(simplex_p[i2][i0])));
////			print(", ");
////	  	print(String.format("%8.3f", Math.toDegrees(simplex_p[i2][i1])));
////		  println();
//    }
////
////		// perform simplex optimization. Moves BSR points around to maximize
////		// travel time.
////    fastRay = null;
////		while (!done && ++ntries <= maxtries)
////		{
////			try
////			{
////				simplexRayMap = new HashMap<double[], Ray>();
////				simplex.search(simplex_p);
////		    fastRay = null;
////				double sf = simplexFunction(simplex_p[0]);
////	
////				done = sf < 10. * simplex.getTolerance();
////				// System.out.println("Simplex Fitness= " + fitness);
////			}
////			catch (Exception ex)
////			{
////				done = false;
////			}
////		}
//
//	  // Output final bottom-side reflection vector
//
//		if (getVerbosity() > 0)
//		{
//		  println();
//		  print("BSROPT    Final Bottom-Side Vector (lat, lon ... in deg) = ");
//		  for (int i = 0; i < undersideReflectionLatLonvector.length; ++i)
//		  {
//		  	print(String.format("%8.3f", Math.toDegrees(undersideReflectionLatLonvector[i])));
//		  	if (i < undersideReflectionLatLonvector.length-1)
//		  			print(", ");
//		  }
//		  println(); println();
//		  println("BSROPT  SIMPLEX BSR Optimization End   " + Globals.repeat("*", 118));
//		  println(); println();
//		}
//
//	  // done return fast ray
//
//		return fastRay;
//	}
//
//	/**
//	 * Find the fastest ray, and all other rays whose travel times differ from
//	 * the travel time of the fastest ray by less than
//	 * FAST_TRAVEL_TIME_TOLERANCE, for the specified receiver-source pair that
//	 * honors the specified phase. Returns the fastest
//	 * refracted/diffracted/reflected ray. If all rays are invalid, returns a
//	 * RayInfo object whose status is INVALID.
//	 * 
//	 * <p>
//	 * This method throws no exceptions. All Exceptions are caught and their
//	 * error messages accumulated into the errorMessage attribute of the RayInfo
//	 * object that is returned.
//	 * 
//	 * @param  request specifies receiver, source, phase, requesteAttributes,
//	 *            etc.
//	 * @return RayInfo[]
//	 */
//	//X public RayInfo[] computeFastRays(PredictionRequest request)
//	public RayInfo[] computeFastRaysNewOld(PredictionRequest request)
//	{
//		if (!request.isDefining())
//			return new RayInfo[] { new RayInfo(request, this, "PredictionRequest was non-defining") };
//
//		// time when calculation starts
//		calcOriginTime = System.currentTimeMillis();
//
//		// time by which calculation must end
//		if (maxEllapsedTime >= 0)
//			timeToAbort = calcOriginTime + maxEllapsedTime;
//		else
//			timeToAbort = Long.MAX_VALUE;
//
//		GeoVector source = request.getSource();
//		GeoVector receiver = request.getReceiver();
//
//		currentSource = source;
//		currentReceiver = receiver;
//
//		// buffer to accumulate error messages from caught exceptions. These
//		// will be added to the RayInfo object that is returned by this method.
//		errorMessages = new StringBuffer();
//
//		//Phase phase = null;
//		//int layer = -1;
//		//String layerName = "";
//
//		if (request.getPhase().getRayBranchList() == null)
//			return new RayInfo[] { new RayInfo(request, this, "Seismic Phase Ray Branch List was Not Defined") };
//
//		if (request.getPhase().getRayInterfaceWaveTypeList() == null)
//			return new RayInfo[] { new RayInfo(request, this, "Seismic Phase Ray Wave Type Interface Conversion List was Not Defined") };
//
//		try
//		{
//			// build source, receiver, branch model, wave type conversion model
//
//			GeoTessPosition sourceProfile = GeoTessPosition.getGeoTessPosition(geoTessModel);
//			sourceProfile.set(source.getUnitVector(),  source.getRadius());
//
//			GeoTessPosition receiverProfile = GeoTessPosition.getGeoTessPosition(geoTessModel);
//			receiverProfile.set(receiver.getUnitVector(),  receiver.getRadius());
//			double distDeg = sourceProfile.distanceDegrees(receiverProfile);
//
//			// if the source and receiver position are the same return a zero length ray
//
//			if (distDeg == 0.0)
//			{
//				//X rayInfo=new RayInfo[1];
//				//X rayInfo[0] = new RayInfo(request, this, "INVALID");
//				rayInfo = new RayInfo[1];
//				rayInfo[0] = new RayInfo(request, this, "Zero Path Length Ray");
//				rayInfo[0].setErrorMessage(errorMessages.toString());
//				rayInfo[0].setStatusLog(logBuffer.toString());
//				rayInfo[0].setAttribute(GeoAttributes.CALCULATION_TIME, 
//						(System.currentTimeMillis() - calcOriginTime) * 1e-3);
//				rayInfo[0].setZeroLengthRay();
//				rayInfo[0].setRayType(RayType.VALID);
//				logBuffer.setLength(0);
//				return rayInfo;
//			}
//
//			// make the phase ray branch and wave type models
//
//			phaseRayBranchModel = 
//					new PhaseRayBranchModel(getGeoTessModel(),
//							                              request.getPhase(), distDeg);
//			phaseWaveTypeModel =
//					new PhaseWaveTypeModel(getGeoTessModel().getMetaData(),
//                                               request.getPhase());
//
////			if (undersideReflectionFractions != null)
////			{
////				for (int i = 0; i < undersideReflectionFractions.length; ++i)
////				{
////				  double ang = Math.toRadians(distDeg) * undersideReflectionFractions[i];
////				  phaseRayBranchModel.setFixedReflectionInitialAngle(i+2, ang);
////				}
////			}
//
//			if (getVerbosity() > 0)
//			{
//				print(String.format(
//						"ObservationId: %d%nSource:   %s%nReceiver: %s%n", request.getObservationId(),
//						source.toString(), receiver.toString()));
//				print(String.format("dist=%1.4f  seaz=%1.4f  esaz=%1.4f%n%n",
//						receiver.distanceDegrees(source), receiver
//						.azimuthDegrees(source, 0.),
//						source.azimuthDegrees(receiver, 0.)));
//
//				println("Seismic Phase: " + request.getPhase().name());
//				println("Receiver:");
//				println(profileString(receiverProfile));
//				println();
//				println("Source:");
//				println(profileString(sourceProfile));
//			}
//
//			// allow listeners to initialize
//
//			initializeListeners();
//
//			// loop over all levels until finished
//
//			//accumulateNodeMovementStatistics = true;
//			rays.clear();
//    	Ray ray = null, ray0 = null;
//    	int maxLayerLevel = 1;
//    	int layerLevel = 0;
//      do
//      {
//				try
//				{
//					if (phaseRayBranchModel.getUndersideReflectionCount() > 0)
//					{
//						lBFGS.setLBFGSFunction(this);
//
//						ray = new Ray(this, receiverProfile, sourceProfile, "");
//					}
//					else
//						ray = new Ray(this, receiverProfile, sourceProfile, "");
//					
//					
//        	if (ray0 == null)
//        	{
//        		// build the first initial ray ... if 1 or more RayBranchBottom
//        		// branches are present then multiple levels may exist ... get the
//        		// maximum level ... if larger than 1 fill the pierce point list
//
//						ray = new Ray(this, receiverProfile, sourceProfile, "");
//						maxLayerLevel = ray.getMaxLayerLevelCount();
//						ray0 = ray;
//						//buildMasterLevelHeirarchy(ray0);
//        	}
//					else
//						ray = new Ray(ray0, "", false);
//
//					rays.add(ray);
//				}
//				catch(BenderException rayEx)
//				{
//					if (rayEx.getErrorCode() == ErrorCode.FATAL)
//						throw rayEx;
//
//					// record error and keep going. these are innocuous errors
//					errorMessages.append(rayEx.getMessage()
//							+GMPException.getStackTraceAsString(rayEx)+"\n");
//				}
//				//layerLevel = this.getNextValidMasterLevel(layerLevel, maxLayerLevel);
//				layerLevel = 0;
//      } while (layerLevel < maxLayerLevel);
//
//			// look for cases where a reflection is followed immediately by
//			// a diffraction along the same interface. Convert the faster one
//			// to a refraction.
//			for (int i = 1; i < rays.size(); ++i)
//			{
//				if (rays.get(i - 1).isType(RayType.REFLECTION)
//						&& rays.get(i).isType(RayType.DIFFRACTION)
//						&& rays.get(i - 1).getRayInterface() == rays.get(i)
//						.getRayInterface())
//				{
//					if (rays.get(i - 1).getTravelTime() <= rays.get(i)
//							.getTravelTime())
//						rays.get(i - 1).setRayType(RayType.REFRACTION);
//					else
//						rays.get(i).setRayType(RayType.REFRACTION);
//				}
//			}
//
//			// remove all invalid rays
//			for (int i = rays.size() - 1; i >= 0; --i)
//			{
//				boolean invalid = rays.get(i).getRayType() == RayType.ERROR
//				|| rays.get(i).getRayType() == RayType.INVALID;
//
//				//if (!rays.get(i).getRayInterface().isMajorInterface() && (rays
//				//		.get(i).getRayType() == RayType.DIFFRACTION || rays
//				//		.get(i).getRayType() == RayType.REFLECTION))
//				//	invalid = true;
//
//				// dissallow Pdiff!
////				if (!allowCMBDiffraction && rays.get(i).getRayInterface().getName().equals("CMB")
////						&& rays.get(i).getRayType() == RayType.DIFFRACTION)
////				{
////					invalid = true;
////					errorMessages.append(String.format(
////							"Ray is invalid because property allowCMBDiffraction is false and "
////							+"the ray diffracts along the CMB.%n"));
////					errorMessages.append(String.format("Version = %s%n", getVersion()));
////
////					errorMessages.append(String.format("Receiver = %1.8f, %1.8f, %1.6f%n",
////							receiver.getLatDegrees(), receiver.getLonDegrees(),
////							receiver.getDepth()));
////
////					errorMessages.append(String.format("Source = %1.8f, %1.8f, %1.6f%n",
////							source.getLatDegrees(), source.getLonDegrees(),
////							source.getDepth()));
////
////					errorMessages.append(String.format("Phase = %s%n", request
////							.getPhase().toString()));
////
////					errorMessages.append(String.format("distance = %1.6f%n",
////							receiver.distanceDegrees(source)));
////				}
//
//				if (invalid)
//					rays.remove(i);
//			}
//
//			// order the rays by increasing travel time.
//			if (rays.size() > 1)
//				Collections.sort(rays);
//
//			// remove all slow rays
//			for (int i = rays.size() - 1; i > 0; --i)
//				if (rays.get(i).getTravelTime() - rays.get(0).getTravelTime() > BenderConstants.FAST_TRAVELTIME_TOLERANCE)
//					rays.remove(i);
//
//			if (rays.size() == 0)
//			{
//				//X rayInfo=new RayInfo[1];
//				//X rayInfo[0] = new RayInfo(request, this, "INVALID");
//				rayInfo = new RayInfo[1];
//				rayInfo[0] = new RayInfo(request, this, "INVALID");
//				rayInfo[0].setErrorMessage(errorMessages.toString());
//				rayInfo[0].setStatusLog(logBuffer.toString());
//				rayInfo[0].setAttribute(GeoAttributes.CALCULATION_TIME, 
//						(System.currentTimeMillis() - calcOriginTime) * 1e-3);
//				logBuffer.setLength(0);
//				return rayInfo;
//			}
//
//	  	rayInfo = new RayInfo[rays.size()];
//
//			for (int i = 0; i < rays.size(); ++i)
//			{
//				changeNotifier.setSource(rays.get(i));
//				if (i == 0)
//					rays.get(i).setStatus(RayStatus.FASTEST_RAY);
//				else
//					rays.get(i).setStatus(RayStatus.FAST_RAY);
//
//				//X rayInfo[i] = new RayInfo(request, this, rays.get(i));
//				rayInfo[i] = new RayInfo(request, this, rays.get(i));
//
//				GeoTessPosition src = GeoTessPosition.getGeoTessPosition(sourceProfile);
//				double[] vtp = {0.0, 0.0, 0.0};
//				if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLAT))
//				{
//					// move source north
//					//X GeoVector src = source.moveNorth(BenderConstants.deriv_dx).setDepth(source.getDepth());
//					sourceProfile.move_north(BenderConstants.deriv_dx, vtp);
//					src.set(vtp, sourceProfile.getRadius());
//					src.setDepth(sourceProfile.getDepth());
//					computingDerivative = true;
//					//X Ray ray = new Ray(this, receiver, src, phase, rays.get(i).getBottomLayer(), null);
//					ray = new Ray(rays.get(i), src, receiverProfile, "");
//					computingDerivative = false;
//					rayInfo[i].setAttribute(GeoAttributes.DTT_DLAT, 
//							(ray.getTravelTime()-rayInfo[i].getTravelTime())/BenderConstants.deriv_dx);
//				}
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLON))
//				{
//					// move source east along a great circle (not small circle)
//					//X GeoVector src = source.move(PI/2, BenderConstants.deriv_dx).setDepth(source.getDepth());
//					VectorUnit.move(sourceProfile.getVector(), BenderConstants.deriv_dx, PI/2, vtp);
//					src.set(vtp, sourceProfile.getRadius());
//					src.setDepth(sourceProfile.getDepth());
//					computingDerivative = true;
//					//X Ray ray = new Ray(this, receiver, src, phase, rays.get(i).getBottomLayer(), null);
//					ray = new Ray(rays.get(i), src, receiverProfile, "");
//					computingDerivative = false;
//					rayInfo[i].setAttribute(GeoAttributes.DTT_DLON, 
//							(ray.getTravelTime()-rayInfo[i].getTravelTime())/BenderConstants.deriv_dx);
//				}
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS) || 
//						request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES))
//				{
//					// move source away from receiver
//					//X GeoVector src = source.move(rayInfo[i].getBackAzimuth()+PI, BenderConstants.deriv_dx).setDepth(source.getDepth());
//					VectorUnit.move(sourceProfile.getVector(), BenderConstants.deriv_dx, rayInfo[i].getBackAzimuth() + PI, vtp);
//					src.set(vtp, sourceProfile.getRadius());
//					src.setDepth(sourceProfile.getDepth());
//					computingDerivative = true;
//					//X Ray ray = new Ray(this, receiver, src, phase, rays.get(i).getBottomLayer(), null);
//					ray = new Ray(rays.get(i), src, receiverProfile, "");
//					computingDerivative = false;
//					double slowness = (ray.getTravelTime()-rayInfo[i].getTravelTime())/BenderConstants.deriv_dx;
//					if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS))
//						rayInfo[i].setAttribute(GeoAttributes.SLOWNESS, slowness);
//					if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES))
//						rayInfo[i].setAttribute(GeoAttributes.SLOWNESS_DEGREES, toRadians(slowness));
//				}
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DR))
//				{
//					// move source up (increase radius)
//					//XGeoVector src = source.clone().setRadius(source.getRadius() + BenderConstants.deriv_dr);
//					src.set(sourceProfile.getVector(), sourceProfile.getRadius() + BenderConstants.deriv_dr);
//					computingDerivative = true;
//					//X Ray ray = new Ray(this, receiver, src, phase, rays.get(i).getBottomLayer(), null);
//					ray = new Ray(rays.get(i), src, receiverProfile, "");
//					computingDerivative = false;
//					rayInfo[i].setAttribute(GeoAttributes.DTT_DR, 
//							(ray.getTravelTime()-rayInfo[i].getTravelTime())/BenderConstants.deriv_dr);
//				}
//
//				//				// see if we need to compute any derivatives wrt source position.
//				//				boolean d2tdxdr = request.getRequestedAttributes().contains(GeoAttributes.DSH_DR);
//				//				boolean d2tdx2 = request.getRequestedAttributes().contains(GeoAttributes.DSH_DLAT)
//				//				|| request.getRequestedAttributes().contains(GeoAttributes.DSH_DX);
//				//				boolean dtdx = request.getRequestedAttributes().contains(GeoAttributes.DTT_DLAT)
//				//				|| request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS)
//				//				|| request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES);
//				//				boolean dtdr = request.getRequestedAttributes().contains(GeoAttributes.DTT_DR);
//				//
//				//				if (d2tdxdr || d2tdx2 || dtdx || dtdr)
//				//				{
//				//					// if we need to compute derivatives of tt, az, or sh
//				//					// with respect to source position, then we need a place to
//				//					// store
//				//					// intermediate values of tt and az. First dimension is
//				//					// 0:tt, 1:az.
//				//					// Second dimension is horizontal changes in source position
//				//					// used to compute dt/dx and dt2/dx2. Third dimension is
//				//					// radial
//				//					// changes in source position used to compute dt/dz and
//				//					// dt2/dxdz
//				//					// 10 11 12
//				//					// 00 01 02
//				//					//
//				//					double[][] data = new double[2][3];
//				//					data[0][1] = rayInfo[i].getTravelTime();
//				//					//double dx = 1e-3; // horizontal spacing in radians
//				//					//double dr = 1e-1; // radial spacing in km
//				//					GeoVector src;
//				//					Ray ray;
//				//					double backAz = rayInfo[i].getBackAzimuth();
//				//
//				//					computingDerivative = true;
//				//
//				//					if (dtdx || d2tdx2 || d2tdxdr)
//				//					{
//				//						// move source away from receiver
//				//						src = source.move(backAz + PI, BenderConstants.deriv_dx);
//				//						src.setDepth(source.getDepth());
//				//						ray = new Ray(this, receiver, src, phase, rays.get(i)
//				//								.getBottomLayer(), null);
//				//						data[0][2] = ray.getTravelTime();
//				//						// set derivative of travel time wrt distance
//				//						// (slowness).
//				//						double slowness = (data[0][2] - data[0][1]) / BenderConstants.deriv_dx;
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS))
//				//							rayInfo[i].setAttribute(GeoAttributes.SLOWNESS, slowness);
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES))
//				//							rayInfo[i].setAttribute(GeoAttributes.SLOWNESS_DEGREES, toRadians(slowness));
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLAT))
//				//							rayInfo[i].setAttribute(GeoAttributes.DTT_DLAT, 
//				//									-slowness * Math.cos(backAz));
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLON))
//				//							rayInfo[i].setAttribute(GeoAttributes.DTT_DLON, 
//				//									-slowness * Math.sin(backAz));
//				//
//				//					}
//				//
//				//					if (d2tdx2)
//				//					{
//				//						// move source away toward receiver
//				//						src = source.move(backAz, BenderConstants.deriv_dx);
//				//						src.setDepth(source.getDepth());
//				//						ray = new Ray(this, receiver, src, phase, rays.get(i)
//				//								.getBottomLayer(), null);
//				//						data[0][0] = ray.getTravelTime();
//				//						// set derivative of slowness with respect to distance
//				//						double dsh_dx = (data[0][2] - 2 * data[0][1] + data[0][0]) 
//				//						            / (BenderConstants.deriv_dx * BenderConstants.deriv_dx);
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DX))
//				//							rayInfo[i].setAttribute(GeoAttributes.DSH_DX, dsh_dx);
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DLAT))
//				//							rayInfo[i].setAttribute(GeoAttributes.DSH_DLAT, -dsh_dx*cos(backAz));
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DLON))
//				//							rayInfo[i].setAttribute(GeoAttributes.DSH_DLON, -dsh_dx*sin(backAz));
//				//					}
//				//
//				//					if (dtdr || d2tdxdr)
//				//					{
//				//						src = source.clone();
//				//						src.setRadius(source.getRadius() + BenderConstants.deriv_dr);
//				//						ray = new Ray(this, receiver, src, phase, rays.get(i)
//				//								.getBottomLayer(), null);
//				//						data[1][1] = ray.getTravelTime();
//				//						// set derivative of travel time wrt radius
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DR))
//				//							rayInfo[i].setAttribute(GeoAttributes.DTT_DR, 
//				//									(data[1][1] - data[0][1]) / BenderConstants.deriv_dr);
//				//					}
//				//
//				//					if (d2tdxdr)
//				//					{
//				//						// move source away from receiver
//				//						src = source.move(backAz + PI, BenderConstants.deriv_dx);
//				//						src.setDepth(source.getDepth() - BenderConstants.deriv_dr);
//				//						ray = new Ray(this, receiver, src, phase, rays.get(i)
//				//								.getBottomLayer(), null);
//				//						data[1][2] = ray.getTravelTime();
//				//						// set derivative of slowness wrt radius
//				//						rayInfo[i].setAttribute(GeoAttributes.DSH_DR, 
//				//								(data[1][2] - data[1][1] - data[0][2] + data[0][1])
//				//								/ (BenderConstants.deriv_dx * BenderConstants.deriv_dr));
//				//					}
//				//					computingDerivative = false;
//				//				}
//				//
//				//				// see if we need to compute derivatives of travel time
//				//				// wrt slowness of active nodes.
//				//				if (request.getRequestedAttributes().contains(
//				//						GeoAttributes.DTT_DSLOW)
//				//						&& geoModel.getNActiveNodes() > 0)
//				//				{
//				//					int[] activeNodes = rayInfo[i].getActiveNodeIndexes();
//				//					double[] dtds = new double[activeNodes.length];
//				//					double oldSlow;
//				//					computingDerivative = true;
//				//
//				//					piercePoints = null; // fastestRay.piercePointsForThisRay();
//				//
//				//					for (int j = 0; j < activeNodes.length; ++j)
//				//					{
//				//						oldSlow = geoModel.getValue(activeNodes[j]);
//				//						geoModel.setValue(activeNodes[j], oldSlow
//				//								+ slownessPerturbation);
//				//
//				//						Ray tray = new Ray(this, receiver, source, phase, rays
//				//								.get(i).getBottomLayer(), piercePoints);
//				//
//				//						dtds[j] = (tray.getTravelTime() - rayInfo[i]
//				//						                                          .getAttribute(GeoAttributes.TRAVEL_TIME))
//				//						                                          / slownessPerturbation;
//				//
//				//						geoModel.setValue(activeNodes[j], oldSlow);
//				//					}
//				//
//				//					rayInfo[i].setActiveNodeDerivs(dtds);
//				//					computingDerivative = false;
//				//				}
//
//				Prediction result = rayInfo[i];
//
//				//if (request.getRequestedAttributes().contains(GeoAttributes.TRAVEL_TIME))
//				result.setAttribute(GeoAttributes.TRAVEL_TIME, rayInfo[i].getTravelTime());
//				result.setAttribute(GeoAttributes.TT_BASEMODEL, rayInfo[i].getTravelTime());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH))
//					result.setAttribute(GeoAttributes.AZIMUTH, rayInfo[i].getAzimuth());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_DEGREES))
//					result.setAttribute(GeoAttributes.AZIMUTH_DEGREES, rayInfo[i].getAzimuthDegrees());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DLAT))
//					result.setAttribute(GeoAttributes.DAZ_DLAT, sin(rayInfo[i].getBackAzimuth()) / sin(rayInfo[i].getDistance()));
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DLON))
//					result.setAttribute(GeoAttributes.DAZ_DLON, cos(rayInfo[i].getBackAzimuth()) / sin(rayInfo[i].getDistance()));
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DR))
//					result.setAttribute(GeoAttributes.DAZ_DR, 0.);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.BACKAZIMUTH))
//					result.setAttribute(GeoAttributes.BACKAZIMUTH, rayInfo[i].getBackAzimuth());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.BACKAZIMUTH_DEGREES))
//					result.setAttribute(GeoAttributes.BACKAZIMUTH_DEGREES, rayInfo[i].getBackAzimuthDegrees());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DISTANCE))
//					result.setAttribute(GeoAttributes.DISTANCE, rayInfo[i].getDistance());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DISTANCE_DEGREES))
//					result.setAttribute(GeoAttributes.DISTANCE_DEGREES, rayInfo[i].getDistanceDegrees());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DTIME))
//					result.setAttribute(GeoAttributes.DTT_DTIME, 1.);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DTIME))
//					result.setAttribute(GeoAttributes.DAZ_DTIME, 0.);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DTIME))
//					result.setAttribute(GeoAttributes.DSH_DTIME, 0.);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.TT_MODEL_UNCERTAINTY))
//				{
//					uncertaintyInterface.setUncertainty(result, GeoAttributes.TT_MODEL_UNCERTAINTY);
//					if (ttModelUncertaintyScale != null && ttModelUncertaintyScale.length > 0)
//					{
//						double ttuncertainty = result.getAttribute(GeoAttributes.TT_MODEL_UNCERTAINTY);
//						if (ttuncertainty != Globals.NA_VALUE)
//						{
//							ttuncertainty *= ttModelUncertaintyScale[0];
//							if (ttModelUncertaintyScale.length > 1)
//								ttuncertainty += ttModelUncertaintyScale[1];
//							result.setAttribute(GeoAttributes.TT_MODEL_UNCERTAINTY, ttuncertainty);
//						}
//					}
//				}
//				
//				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY))
//					uncertaintyInterface.setUncertainty(result, GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES))
//					uncertaintyInterface.setUncertainty(result, GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY))
//					uncertaintyInterface.setUncertainty(result, GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES))
//					uncertaintyInterface.setUncertainty(result, GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES);
//
//				// if the site term was requested then try to retrieve it and set the value 
//				// for the tt_site_correction attribute in the rayInfo object.  If the site term
//				// is not na value, and predicted travel time is not na value, add the site
//				// term to the total travel time.
//				Boolean useST = useTTSiteCorrectionsStationList.get(rayInfo[i].getReceiver().getSta());
//				if (useST == null) useST = useTTSiteCorrections;
//				if (useST)
//				{
////X					double siteTerm = geoModel.getSiteTerm(rayInfo[i]);
////X					rayInfo[i].setAttribute(GeoAttributes.TT_SITE_CORRECTION, siteTerm);
////X					if (siteTerm != Globals.NA_VALUE)
////X					{
////X						double tt = rayInfo[i].getAttribute(GeoAttributes.TRAVEL_TIME);
////X						if (tt != Globals.NA_VALUE)
////X							rayInfo[i].setAttribute(GeoAttributes.TRAVEL_TIME, tt+siteTerm);
////X					}
//					
//					double st = Globals.NA_VALUE;
//					if (geoTessModel instanceof GeoTessModelSiteData)
//					{
//						double tt = rayInfo[i].getAttribute(GeoAttributes.TRAVEL_TIME);
//						if (tt != Globals.NA_VALUE)
//						{
//							String staname = rayInfo[i].getReceiver().getSta();
//							double origTime = rayInfo[i].getSource().getOriginTime();
//							int attrIndx = geoTessModel.getMetaData().getAttributeIndex(rayInfo[i].getWaveType().name());
//							st = ((GeoTessModelSiteData) geoTessModel).getSiteTerm(
//									   attrIndx, staname, tt, origTime);
//							rayInfo[i].setAttribute(GeoAttributes.TRAVEL_TIME, tt + st);
//						}
//					}
//					rayInfo[i].setAttribute(GeoAttributes.TT_SITE_CORRECTION, st);
//				}
//
//				if (ellipticityCorrections != null)
//				{
//					double ellipCorr = ellipticityCorrections.getEllipCorr(request.getPhase(), 
//							request.getReceiver(), request.getSource());
//					rayInfo[i].setAttribute(GeoAttributes.TT_ELLIPTICITY_CORRECTION, ellipCorr);
//					double tt = rayInfo[i].getAttribute(GeoAttributes.TRAVEL_TIME);
//					if (tt != Globals.NA_VALUE)
//						rayInfo[i].setAttribute(GeoAttributes.TRAVEL_TIME, tt+ellipCorr);
//				}
//
//				if (getVerbosity() > 0)
//				{
//					if (i == 0)
//						println("fastest ray = " + rayInfo[i].toString());
//					else
//						println("fast    ray = " + rayInfo[i].toString());
//				}
//
//			}
//
//			//Xfor (RayInfo aRay : rayInfo)
//			for (RayInfo aRay : rayInfo)
//			{
//				aRay.setStatusLog(logBuffer.toString());
//				aRay.setErrorMessage(errorMessages.toString());
//				aRay.setAttribute(GeoAttributes.CALCULATION_TIME, 
//						(System.currentTimeMillis() - calcOriginTime) * 1e-3);
//			}
//			logBuffer.setLength(0);
//			return rayInfo;
//		}
//		catch(Exception ex)
//		{
//			if (ex.getMessage() == null)
//				errorMessages.append("null pointer exception");
//			else
//				errorMessages.append(ex.getMessage());
//
//			errorMessages.append(System.getProperty("line.separator"));
//			errorMessages.append(String.format("Version = %s%n", getVersion()));
//
//			errorMessages.append(String.format("Receiver = %1.8f, %1.8f, %1.6f%n",
//					receiver.getLatDegrees(), receiver.getLonDegrees(),
//					receiver.getDepth()));
//
//			errorMessages.append(String.format("Source = %1.8f, %1.8f, %1.6f%n",
//					source.getLatDegrees(), source.getLonDegrees(),
//					source.getDepth()));
//
//			errorMessages.append(String.format("Phase = %s%n", request
//					.getPhase().toString()));
//
//			errorMessages.append(String.format("distance = %1.6f%n",
//					receiver.distanceDegrees(source)));
//
//			// Recreate the stack trace into the error String.
//			for (int i = 0; i < ex.getStackTrace().length; i++)
//				errorMessages.append(ex.getStackTrace()[i].toString()).append(
//				"\n");
//
//			//X rayInfo=new RayInfo[1];
//			//X rayInfo[0] = new RayInfo(request, this, "ERROR");
//			rayInfo = new RayInfo[1];
//			rayInfo[0] = new RayInfo(request, this, "ERROR");
//			rayInfo[0].setErrorMessage(errorMessages.toString());
//			rayInfo[0].setStatusLog(logBuffer.toString());
//			rayInfo[0].setAttribute(GeoAttributes.CALCULATION_TIME, 
//					(System.currentTimeMillis() - calcOriginTime) * 1e-3);
//			logBuffer.setLength(0);
//			return rayInfo;
//		}
//	}
//
//	public double getTravelTimeConvergenceErrorCriteria()
//	{
//		return 0.0015;
//	}

	private void initializeListeners()
	{
		changeNotifier.setSource(this);
		changeNotifier.fireStateChanged();		
	}

//	private ArrayList<RayBranchBottom> ray0BottomBranchList = null;
//	private HashMap<RayBranchBottom, Integer> bottomCurrentLayerLevel = null;
//	private HashMap<String, HashMap<String, ArrayList<RayBranchBottom>>> duplicateBottomMap = null;
//	private boolean nextValidLayerLevelEntryAlwaysTrue = true;
//	
//	private void buildMasterLevelHeirarchy(Ray ray0)
//	{
//		nextValidLayerLevelEntryAlwaysTrue = true;
//		bottomCurrentLayerLevel = new HashMap<RayBranchBottom, Integer>();
//		duplicateBottomMap = new HashMap<String, HashMap<String, ArrayList<RayBranchBottom>>>();
//		ray0BottomBranchList = ray0.getBranchBottoms();
//		for (int i = 0; i < ray0BottomBranchList.size(); ++i)
//		{
//			RayBranchBottom branchBottom = ray0BottomBranchList.get(i);
//			RaySegmentBottom bottomSegment = branchBottom.getBottomSegment();
//			String plldname = bottomSegment.getPhaseLayerLevelDefinition().getInterfaceLayerTypeName();
//			bottomCurrentLayerLevel.put(branchBottom,  branchBottom.getBottomLayerLevel());
//			HashMap<String, ArrayList<RayBranchBottom>> waveTypeBottomMap = duplicateBottomMap.get(plldname);
//			if (waveTypeBottomMap == null)
//			{
//				waveTypeBottomMap = new HashMap<String, ArrayList<RayBranchBottom>>();
//				duplicateBottomMap.put(plldname,  waveTypeBottomMap);
//			}
//			
//			String waveTypeName = bottomSegment.getWaveTypeName();
//			ArrayList<RayBranchBottom> phaseLayerWaveTypeBottomList = waveTypeBottomMap.get(waveTypeName);
//			if (phaseLayerWaveTypeBottomList == null)
//			{
//				phaseLayerWaveTypeBottomList = new ArrayList<RayBranchBottom>();
//				waveTypeBottomMap.put(waveTypeName, phaseLayerWaveTypeBottomList);
//			}
//			
//			phaseLayerWaveTypeBottomList.add(branchBottom);
//			if (phaseLayerWaveTypeBottomList.size() > 1)
//				nextValidLayerLevelEntryAlwaysTrue = false;
//		}
//	}
//
//	private int getNextValidMasterLevel(int currentMasterLevel,
//			                                int maxMasterLevel)
//	{
//		int nextValidMasterLevel = currentMasterLevel + 1;
//		if (nextValidLayerLevelEntryAlwaysTrue)
//			return nextValidMasterLevel;
//
//		while (nextValidMasterLevel < maxMasterLevel)
//		{
//			int masterLevel = nextValidMasterLevel;
//			for (int i = ray0BottomBranchList.size() - 1; i >= 0; --i)
//			{
//				RayBranchBottom branchBottom = ray0BottomBranchList.get(i);
//				int currentLayerLevel = branchBottom.getLayerLevel(masterLevel);
//				masterLevel = branchBottom.getRemainingMasterLayerLevelAllocation(masterLevel);
//				bottomCurrentLayerLevel.put(branchBottom, currentLayerLevel);
//			}
//
//			if (nextLayerLevelValid())
//				return nextValidMasterLevel;
//
//			++nextValidMasterLevel;
//		}
//
//		return nextValidMasterLevel;
//	}
//
//	private boolean nextLayerLevelValid()
//	{
//		HashMap<String, ArrayList<RayBranchBottom>> waveTypeBottomMap;
//		for (Entry<String, HashMap<String, ArrayList<RayBranchBottom>>> e:
//			   duplicateBottomMap.entrySet())
//		{
//			waveTypeBottomMap = e.getValue();
//			for (Entry<String, ArrayList<RayBranchBottom>> eWaveType: waveTypeBottomMap.entrySet())
//			{
//				ArrayList<RayBranchBottom> bottomList = eWaveType.getValue();
//				RayBranchBottom primaryBottom = bottomList.get(0);
//				int primaryLayerLevel = bottomCurrentLayerLevel.get(primaryBottom);
//				for (int i = 1; i < bottomList.size(); ++i)
//				{
//					int secondaryLayerLevel = bottomCurrentLayerLevel.get(bottomList.get(i));
//					if (Math.abs(primaryLayerLevel - secondaryLayerLevel) > 1)
//					{
//						return false;
//					}
//				}
//			}
//		}
//		
//		return true;
//	}

//	/**
//	 * Find the fastest ray, and all other rays whose travel times differ from
//	 * the travel time of the fastest ray by less than
//	 * FAST_TRAVEL_TIME_TOLERANCE, for the specified receiver-source pair that
//	 * honors the specified phase. Returns the fastest
//	 * refracted/diffracted/reflected ray. If all rays are invalid, returns a
//	 * RayInfo object whose status is INVALID.
//	 * 
//	 * <p>
//	 * This method throws no exceptions. All Exceptions are caught and their
//	 * error messages accumulated into the errorMessage attribute of the RayInfo
//	 * object that is returned.
//	 * 
//	 * @param  request specifies receiver, source, phase, requesteAttributes,
//	 *            etc.
//	 * @return RayInfo[]
//	 * @throws GeoTessException 
//	 */
//	//X public RayInfo[] computeFastRays(PredictionRequest request)
//	public RayInfo[] computeFastRaysOld(PredictionRequest request) throws GeoTessException
//	{
//		if (!request.isDefining())
//			//X return new RayInfo[] { new RayInfo(request, this, "PredictionRequest was non-defining") };
//			return new RayInfo[] { new RayInfo(request, this, "PredictionRequest was non-defining") };
//
//		// time when calculation starts
//		calcOriginTime = System.currentTimeMillis();
//
//		// time by which calculation must end
//		if (maxEllapsedTime >= 0)
//			timeToAbort = calcOriginTime + maxEllapsedTime;
//		else
//			timeToAbort = Long.MAX_VALUE;
//
//		GeoVector source = request.getSource();
//		GeoVector receiver = request.getReceiver();
//
//		currentSource = source;
//		currentReceiver = receiver;
//
//		// buffer to accumulate error messages from caught exceptions. These
//		// will be added to the RayInfo object that is returned by this method.
//		StringBuffer errorMessages = new StringBuffer();
//
//		int layer = -1;
//		String layerName = "";
//
//		PhaseLayerLevelBuilder phaseBuilder = new PhaseLayerLevelBuilder(geoTessModel, 0);
//		try
//		{
//			//PhaseLayerLevelDefinition phase = phaseBuilder.getPhaseLayerLevelDefinition(request.getPhase().name());
//			PhaseLayerLevelDefinition phase = phaseBuilder.getPhaseLayerLevelDefinition("M660");
//
//			//X InterpolatedNodeLayered sourceProfile = geoModel
//			//X .getInterpolatedNodeLayered(source);
//			GeoTessPosition sourceProfile = GeoTessPosition.getGeoTessPosition(geoTessModel);
//			sourceProfile.set(source.getUnitVector(),  source.getRadius());
//
//			GeoTessPosition receiverProfile = GeoTessPosition.getGeoTessPosition(geoTessModel);
//			receiverProfile.set(receiver.getUnitVector(),  receiver.getRadius());
//			double distDeg = sourceProfile.distanceDegrees(receiverProfile);
//			
//			phaseRayBranchModel = 
//					new PhaseRayBranchModel(getGeoTessModel(), request.getPhase(), distDeg);
//			phaseWaveTypeModel =
//					new PhaseWaveTypeModel(getGeoTessModel().getMetaData(),
//	                               request.getPhase());
//
//			if (getVerbosity() > 0)
//			{
//				print(String.format(
//						"ObservationId: %d%nSource:   %s%nReceiver: %s%n", request.getObservationId(),
//						source.toString(), receiver.toString()));
//				print(String.format("dist=%1.4f  seaz=%1.4f  esaz=%1.4f%n%n",
//						receiver.distanceDegrees(source), receiver
//						.azimuthDegrees(source, 0.),
//						source.azimuthDegrees(receiver, 0.)));
//
//				println("Receiver:");
//				//X println(phase.toString(geoModel.getInterpolatedNodeLayered(receiver)));
//				//println(phase.toString(receiverProfile));
//				println();
//				println("Source:");
//				//println(phase.toString(sourceProfile));
//			}
//			
//			initializeListeners();
//			
//			//x List<InterpolatedNodeLayered> piercePoints = null;
//			List<GeoTessPosition> piercePoints = null;
//			ArrayListInt segmentWaveType = null;
//			int topLayer = phase.getTopLayer(source.distanceDegrees(receiver));
//			int prevTopLayer = phase.getPreviousMajorLayerLevelIndex(topLayer) + 1;
//			int bottomLayer = min(prevTopLayer, sourceProfile.getInterfaceIndex());
//			bottomLayer = min(phase.getBottomLayer(source.
//					              distanceDegrees(receiver)), bottomLayer);
//
////			if ((phaseRayBranchModel.getSeismicPhase().name().length() == 5) &&
////					(phaseRayBranchModel.getSeismicPhase().name().substring(3, 5).equals("ab")))
////			{
////				++bottomLayer;
////			}
////			else if ((phase.getPhaseName().name().length() == 5) &&
////					     (phase.getPhaseName().name().substring(3, 5).equals("bc")))
////			{
////				--topLayer;				
////			}
//			rays.clear();
//			// rays.ensureCapacity(topLayer - bottomLayer + 1);
//
//			for (layer = bottomLayer; layer <= topLayer; ++layer)
//			{
//				layerName = phase.getInterface(layer).getName();
//				
//				boolean done = false;
//				double sourceRadius = source.getRadius();
//				while (!done) {
//					try
//					{
//						source.setRadius(sourceRadius);
//						Ray ray = new Ray(this, receiverProfile, sourceProfile,
//								                    phase, layer, piercePoints, segmentWaveType);
//						rays.add(ray);
//						piercePoints = ray.piercePoints();
//						segmentWaveType = ray.getSegmentWaveTypeList();
//						done = true;
//					} 
//					catch (BenderException ex)
//					{
//						if (ex.getMessage().equals("Failure to converge in Ray.optimize().") 
//								&& Math.abs(source.getRadius()-sourceRadius) < 1e-3)
//						{
//							source.setRadius(source.getRadius()+1e-4);
//						}
//						else
//						{
//							if (ex.getErrorCode() == ErrorCode.FATAL)
//								throw ex;
//
//							// record error and keep going. these are innocuous errors
//							errorMessages.append(ex.getMessage()
//									+GMPException.getStackTraceAsString(ex)+"\n");
//							piercePoints = null;
//							segmentWaveType = null;
//							done = true;
//						}
//					}
//				}
//			} // end loop over layers
//
//			// look for cases where a reflection is followed immediately by
//			// a diffraction along the same interface. Convert the faster one
//			// to a refraction.
//			for (int i = 1; i < rays.size(); ++i)
//			{
//				if (rays.get(i - 1).isType(RayType.REFLECTION)
//						&& rays.get(i).isType(RayType.DIFFRACTION)
//						&& rays.get(i - 1).getRayInterface() == rays.get(i)
//						.getRayInterface())
//				{
//					if (rays.get(i - 1).getTravelTime() <= rays.get(i)
//							.getTravelTime())
//						rays.get(i - 1).setRayType(RayType.REFRACTION);
//					else
//						rays.get(i).setRayType(RayType.REFRACTION);
//				}
//			}
//
//			// remove all invalid rays
//			for (int i = rays.size() - 1; i >= 0; --i)
//			{
//				boolean invalid = rays.get(i).getRayType() == RayType.ERROR
//				|| rays.get(i).getRayType() == RayType.INVALID;
//
//				if (!rays.get(i).getRayInterface().isMajorInterface() && (rays
//						.get(i).getRayType() == RayType.DIFFRACTION || rays
//						.get(i).getRayType() == RayType.REFLECTION))
//					invalid = true;
//
//				// dissallow Pdiff!
//				if (!allowCMBDiffraction && rays.get(i).getRayInterface().getName().equals("CMB")
//						&& rays.get(i).getRayType() == RayType.DIFFRACTION)
//				{
//					invalid = true;
//					errorMessages.append(String.format(
//							"Ray is invalid because property allowCMBDiffraction is false and "
//							+"the ray diffracts along the CMB.%n"));
//					errorMessages.append(String.format("Version = %s%n", getVersion()));
//
//					errorMessages.append(String.format("Receiver = %1.8f, %1.8f, %1.6f%n",
//							receiver.getLatDegrees(), receiver.getLonDegrees(),
//							receiver.getDepth()));
//
//					errorMessages.append(String.format("Source = %1.8f, %1.8f, %1.6f%n",
//							source.getLatDegrees(), source.getLonDegrees(),
//							source.getDepth()));
//
//					errorMessages.append(String.format("Phase = %s%n", request
//							.getPhase().toString()));
//
//					errorMessages.append(String.format("layer = %s%n", layerName));
//
//					errorMessages.append(String.format("distance = %1.6f%n",
//							receiver.distanceDegrees(source)));
//
//				}
//
//				if (invalid)
//					rays.remove(i);
//			}
//
//			// special case for Pg when there is an extra, thin, middle crustal layer
//			if (phaseRayBranchModel.getSeismicPhase() == SeismicPhase.Pg)
//			{
//				//X int middle_crust_g = geoModel.getInterfaces().getInterfaceIndex("MIDDLE_CRUST_G");
//				int middle_crust_g = geoTessModel.getMetaData().getInterfaceIndex("MIDDLE_CRUST_G");
//				if (middle_crust_g >= 0)
//				{
//					//					// if there is a refraction, keep it.  If there are only diffractions, keep the one that bottoms in middle_crust_g
//					//					Ray refraction = null;
//					//					Ray diffraction_g = null;
//					//					for (Ray ray : rays)
//					//					{
//					//						if (ray.getRayType() == RayType.REFRACTION 
//					//								&& (refraction == null || ray.getTravelTime() < refraction.getTravelTime()))
//					//							refraction = ray;
//					//
//					//						if (ray.getBottomLayer() == middle_crust_g)
//					//							diffraction_g = ray;
//					//					}
//					//					if (refraction != null)
//					//					{
//					//						rays.clear();
//					//						rays.add(refraction);
//					//					}
//					//					else if (diffraction_g != null)
//					//					{
//					//						rays.clear();
//					//						rays.add(diffraction_g);
//					//					}
//					// remove all rays that are diffractions that do not bottom in the middle_crust_g layer
//					//X ArrayList<Ray> removeRays = new ArrayList<Ray>();
//					ArrayList<Ray> removeRays = new ArrayList<Ray>();
//					for (Ray ray : rays)
//						if (ray.getRayType() == RayType.DIFFRACTION && ray.getBottomLayer() != middle_crust_g)
//							removeRays.add(ray);
//					//X for (Ray ray : removeRays)
//					for (Ray ray : removeRays)
//						rays.remove(ray);
//				}
//			}
//
//			// order the rays by increasing travel time.
//			if (rays.size() > 1)
//				Collections.sort(rays);
//
//			// remove all slow rays
//			for (int i = rays.size() - 1; i > 0; --i)
//				if (rays.get(i).getTravelTime() - rays.get(0).getTravelTime() > BenderConstants.FAST_TRAVELTIME_TOLERANCE)
//					rays.remove(i);
//
//			if (rays.size() == 0)
//			{
//				//X rayInfo=new RayInfo[1];
//				//X rayInfo[0] = new RayInfo(request, this, "INVALID");
//				rayInfo = new RayInfo[1];
//				rayInfo[0] = new RayInfo(request, this, "INVALID");
//				rayInfo[0].setErrorMessage(errorMessages.toString());
//				rayInfo[0].setStatusLog(logBuffer.toString());
//				rayInfo[0].setAttribute(GeoAttributes.CALCULATION_TIME, 
//						(System.currentTimeMillis() - calcOriginTime) * 1e-3);
//				logBuffer.setLength(0);
//				return rayInfo;
//			}
//
//			//X rayInfo = new RayInfo[rays.size()];
//			rayInfo = new RayInfo[rays.size()];
//
//			for (int i = 0; i < rays.size(); ++i)
//			{
//				changeNotifier.setSource(rays.get(i));
//				if (i == 0)
//					rays.get(i).setStatus(RayStatus.FASTEST_RAY);
//				else
//					rays.get(i).setStatus(RayStatus.FAST_RAY);
//
//				//X rayInfo[i] = new RayInfo(request, this, rays.get(i));
//				rayInfo[i] = new RayInfo(request, this, rays.get(i));
//
//				GeoTessPosition src = GeoTessPosition.getGeoTessPosition(sourceProfile);
//				double[] vtp = {0.0, 0.0, 0.0};
//				if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLAT))
//				{
//					// move source north
//					//X GeoVector src = source.moveNorth(BenderConstants.deriv_dx).setDepth(source.getDepth());
//					sourceProfile.move_north(BenderConstants.deriv_dx, vtp);
//					src.set(vtp, sourceProfile.getRadius());
//					src.setDepth(sourceProfile.getDepth());
//					computingDerivative = true;
//					//X Ray ray = new Ray(this, receiver, src, phase, rays.get(i).getBottomLayer(), null);
//					Ray ray = new Ray(this, receiverProfile, src, phase, rays.get(i).getBottomLayer(), null, null);
//					computingDerivative = false;
//					rayInfo[i].setAttribute(GeoAttributes.DTT_DLAT, 
//							(ray.getTravelTime()-rayInfo[i].getTravelTime())/BenderConstants.deriv_dx);
//				}
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLON))
//				{
//					// move source east along a great circle (not small circle)
//					//X GeoVector src = source.move(PI/2, BenderConstants.deriv_dx).setDepth(source.getDepth());
//					VectorUnit.move(sourceProfile.getVector(), BenderConstants.deriv_dx, PI/2, vtp);
//					src.set(vtp, sourceProfile.getRadius());
//					src.setDepth(sourceProfile.getDepth());
//					computingDerivative = true;
//					//X Ray ray = new Ray(this, receiver, src, phase, rays.get(i).getBottomLayer(), null);
//					Ray ray = new Ray(this, receiverProfile, src, phase, rays.get(i).getBottomLayer(), null, null);
//					computingDerivative = false;
//					rayInfo[i].setAttribute(GeoAttributes.DTT_DLON, 
//							(ray.getTravelTime()-rayInfo[i].getTravelTime())/BenderConstants.deriv_dx);
//				}
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS) || 
//						request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES))
//				{
//					// move source away from receiver
//					//X GeoVector src = source.move(rayInfo[i].getBackAzimuth()+PI, BenderConstants.deriv_dx).setDepth(source.getDepth());
//					VectorUnit.move(sourceProfile.getVector(), BenderConstants.deriv_dx, rayInfo[i].getBackAzimuth() + PI, vtp);
//					src.set(vtp, sourceProfile.getRadius());
//					src.setDepth(sourceProfile.getDepth());
//					computingDerivative = true;
//					//X Ray ray = new Ray(this, receiver, src, phase, rays.get(i).getBottomLayer(), null);
//					Ray ray = new Ray(this, receiverProfile, src, phase, rays.get(i).getBottomLayer(), null, null);
//					computingDerivative = false;
//					double slowness = (ray.getTravelTime()-rayInfo[i].getTravelTime())/BenderConstants.deriv_dx;
//					if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS))
//						rayInfo[i].setAttribute(GeoAttributes.SLOWNESS, slowness);
//					if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES))
//						rayInfo[i].setAttribute(GeoAttributes.SLOWNESS_DEGREES, toRadians(slowness));
//				}
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DR))
//				{
//					// move source up (increase radius)
//					//XGeoVector src = source.clone().setRadius(source.getRadius() + BenderConstants.deriv_dr);
//					src.set(sourceProfile.getVector(), sourceProfile.getRadius() + BenderConstants.deriv_dr);
//					computingDerivative = true;
//					//X Ray ray = new Ray(this, receiver, src, phase, rays.get(i).getBottomLayer(), null);
//					Ray ray = new Ray(this, receiverProfile, src, phase, rays.get(i).getBottomLayer(), null, null);
//					computingDerivative = false;
//					rayInfo[i].setAttribute(GeoAttributes.DTT_DR, 
//							(ray.getTravelTime()-rayInfo[i].getTravelTime())/BenderConstants.deriv_dr);
//				}
//
//				//				// see if we need to compute any derivatives wrt source position.
//				//				boolean d2tdxdr = request.getRequestedAttributes().contains(GeoAttributes.DSH_DR);
//				//				boolean d2tdx2 = request.getRequestedAttributes().contains(GeoAttributes.DSH_DLAT)
//				//				|| request.getRequestedAttributes().contains(GeoAttributes.DSH_DX);
//				//				boolean dtdx = request.getRequestedAttributes().contains(GeoAttributes.DTT_DLAT)
//				//				|| request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS)
//				//				|| request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES);
//				//				boolean dtdr = request.getRequestedAttributes().contains(GeoAttributes.DTT_DR);
//				//
//				//				if (d2tdxdr || d2tdx2 || dtdx || dtdr)
//				//				{
//				//					// if we need to compute derivatives of tt, az, or sh
//				//					// with respect to source position, then we need a place to
//				//					// store
//				//					// intermediate values of tt and az. First dimension is
//				//					// 0:tt, 1:az.
//				//					// Second dimension is horizontal changes in source position
//				//					// used to compute dt/dx and dt2/dx2. Third dimension is
//				//					// radial
//				//					// changes in source position used to compute dt/dz and
//				//					// dt2/dxdz
//				//					// 10 11 12
//				//					// 00 01 02
//				//					//
//				//					double[][] data = new double[2][3];
//				//					data[0][1] = rayInfo[i].getTravelTime();
//				//					//double dx = 1e-3; // horizontal spacing in radians
//				//					//double dr = 1e-1; // radial spacing in km
//				//					GeoVector src;
//				//					Ray ray;
//				//					double backAz = rayInfo[i].getBackAzimuth();
//				//
//				//					computingDerivative = true;
//				//
//				//					if (dtdx || d2tdx2 || d2tdxdr)
//				//					{
//				//						// move source away from receiver
//				//						src = source.move(backAz + PI, BenderConstants.deriv_dx);
//				//						src.setDepth(source.getDepth());
//				//						ray = new Ray(this, receiver, src, phase, rays.get(i)
//				//								.getBottomLayer(), null);
//				//						data[0][2] = ray.getTravelTime();
//				//						// set derivative of travel time wrt distance
//				//						// (slowness).
//				//						double slowness = (data[0][2] - data[0][1]) / BenderConstants.deriv_dx;
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS))
//				//							rayInfo[i].setAttribute(GeoAttributes.SLOWNESS, slowness);
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_DEGREES))
//				//							rayInfo[i].setAttribute(GeoAttributes.SLOWNESS_DEGREES, toRadians(slowness));
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLAT))
//				//							rayInfo[i].setAttribute(GeoAttributes.DTT_DLAT, 
//				//									-slowness * Math.cos(backAz));
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DLON))
//				//							rayInfo[i].setAttribute(GeoAttributes.DTT_DLON, 
//				//									-slowness * Math.sin(backAz));
//				//
//				//					}
//				//
//				//					if (d2tdx2)
//				//					{
//				//						// move source away toward receiver
//				//						src = source.move(backAz, BenderConstants.deriv_dx);
//				//						src.setDepth(source.getDepth());
//				//						ray = new Ray(this, receiver, src, phase, rays.get(i)
//				//								.getBottomLayer(), null);
//				//						data[0][0] = ray.getTravelTime();
//				//						// set derivative of slowness with respect to distance
//				//						double dsh_dx = (data[0][2] - 2 * data[0][1] + data[0][0]) 
//				//						            / (BenderConstants.deriv_dx * BenderConstants.deriv_dx);
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DX))
//				//							rayInfo[i].setAttribute(GeoAttributes.DSH_DX, dsh_dx);
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DLAT))
//				//							rayInfo[i].setAttribute(GeoAttributes.DSH_DLAT, -dsh_dx*cos(backAz));
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DLON))
//				//							rayInfo[i].setAttribute(GeoAttributes.DSH_DLON, -dsh_dx*sin(backAz));
//				//					}
//				//
//				//					if (dtdr || d2tdxdr)
//				//					{
//				//						src = source.clone();
//				//						src.setRadius(source.getRadius() + BenderConstants.deriv_dr);
//				//						ray = new Ray(this, receiver, src, phase, rays.get(i)
//				//								.getBottomLayer(), null);
//				//						data[1][1] = ray.getTravelTime();
//				//						// set derivative of travel time wrt radius
//				//						if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DR))
//				//							rayInfo[i].setAttribute(GeoAttributes.DTT_DR, 
//				//									(data[1][1] - data[0][1]) / BenderConstants.deriv_dr);
//				//					}
//				//
//				//					if (d2tdxdr)
//				//					{
//				//						// move source away from receiver
//				//						src = source.move(backAz + PI, BenderConstants.deriv_dx);
//				//						src.setDepth(source.getDepth() - BenderConstants.deriv_dr);
//				//						ray = new Ray(this, receiver, src, phase, rays.get(i)
//				//								.getBottomLayer(), null);
//				//						data[1][2] = ray.getTravelTime();
//				//						// set derivative of slowness wrt radius
//				//						rayInfo[i].setAttribute(GeoAttributes.DSH_DR, 
//				//								(data[1][2] - data[1][1] - data[0][2] + data[0][1])
//				//								/ (BenderConstants.deriv_dx * BenderConstants.deriv_dr));
//				//					}
//				//					computingDerivative = false;
//				//				}
//				//
//				//				// see if we need to compute derivatives of travel time
//				//				// wrt slowness of active nodes.
//				//				if (request.getRequestedAttributes().contains(
//				//						GeoAttributes.DTT_DSLOW)
//				//						&& geoModel.getNActiveNodes() > 0)
//				//				{
//				//					int[] activeNodes = rayInfo[i].getActiveNodeIndexes();
//				//					double[] dtds = new double[activeNodes.length];
//				//					double oldSlow;
//				//					computingDerivative = true;
//				//
//				//					piercePoints = null; // fastestRay.piercePointsForThisRay();
//				//
//				//					for (int j = 0; j < activeNodes.length; ++j)
//				//					{
//				//						oldSlow = geoModel.getValue(activeNodes[j]);
//				//						geoModel.setValue(activeNodes[j], oldSlow
//				//								+ slownessPerturbation);
//				//
//				//						Ray tray = new Ray(this, receiver, source, phase, rays
//				//								.get(i).getBottomLayer(), piercePoints);
//				//
//				//						dtds[j] = (tray.getTravelTime() - rayInfo[i]
//				//						                                          .getAttribute(GeoAttributes.TRAVEL_TIME))
//				//						                                          / slownessPerturbation;
//				//
//				//						geoModel.setValue(activeNodes[j], oldSlow);
//				//					}
//				//
//				//					rayInfo[i].setActiveNodeDerivs(dtds);
//				//					computingDerivative = false;
//				//				}
//
//				Prediction result = rayInfo[i];
//
//				//if (request.getRequestedAttributes().contains(GeoAttributes.TRAVEL_TIME))
//				result.setAttribute(GeoAttributes.TRAVEL_TIME, rayInfo[i].getTravelTime());
//				result.setAttribute(GeoAttributes.TT_BASEMODEL, rayInfo[i].getTravelTime());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH))
//					result.setAttribute(GeoAttributes.AZIMUTH, rayInfo[i].getAzimuth());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_DEGREES))
//					result.setAttribute(GeoAttributes.AZIMUTH_DEGREES, rayInfo[i].getAzimuthDegrees());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DLAT))
//					result.setAttribute(GeoAttributes.DAZ_DLAT, sin(rayInfo[i].getBackAzimuth()) / sin(rayInfo[i].getDistance()));
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DLON))
//					result.setAttribute(GeoAttributes.DAZ_DLON, cos(rayInfo[i].getBackAzimuth()) / sin(rayInfo[i].getDistance()));
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DR))
//					result.setAttribute(GeoAttributes.DAZ_DR, 0.);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.BACKAZIMUTH))
//					result.setAttribute(GeoAttributes.BACKAZIMUTH, rayInfo[i].getBackAzimuth());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.BACKAZIMUTH_DEGREES))
//					result.setAttribute(GeoAttributes.BACKAZIMUTH_DEGREES, rayInfo[i].getBackAzimuthDegrees());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DISTANCE))
//					result.setAttribute(GeoAttributes.DISTANCE, rayInfo[i].getDistance());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DISTANCE_DEGREES))
//					result.setAttribute(GeoAttributes.DISTANCE_DEGREES, rayInfo[i].getDistanceDegrees());
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DTT_DTIME))
//					result.setAttribute(GeoAttributes.DTT_DTIME, 1.);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DAZ_DTIME))
//					result.setAttribute(GeoAttributes.DAZ_DTIME, 0.);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.DSH_DTIME))
//					result.setAttribute(GeoAttributes.DSH_DTIME, 0.);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.TT_MODEL_UNCERTAINTY))
//				{
//					uncertaintyInterface.setUncertainty(result, GeoAttributes.TT_MODEL_UNCERTAINTY);
//					if (ttModelUncertaintyScale != null && ttModelUncertaintyScale.length > 0)
//					{
//						double ttuncertainty = result.getAttribute(GeoAttributes.TT_MODEL_UNCERTAINTY);
//						if (ttuncertainty != Globals.NA_VALUE)
//						{
//							ttuncertainty *= ttModelUncertaintyScale[0];
//							if (ttModelUncertaintyScale.length > 1)
//								ttuncertainty += ttModelUncertaintyScale[1];
//							result.setAttribute(GeoAttributes.TT_MODEL_UNCERTAINTY, ttuncertainty);
//						}
//					}
//				}
//				
//				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY))
//					uncertaintyInterface.setUncertainty(result, GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES))
//					uncertaintyInterface.setUncertainty(result, GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY))
//					uncertaintyInterface.setUncertainty(result, GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY);
//
//				if (request.getRequestedAttributes().contains(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES))
//					uncertaintyInterface.setUncertainty(result, GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES);
//
//				// if the site term was requested then try to retrieve it and set the value 
//				// for the tt_site_correction attribute in the rayInfo object.  If the site term
//				// is not na value, and predicted travel time is not na value, add the site
//				// term to the total travel time.
//				Boolean useST = useTTSiteCorrectionsStationList.get(rayInfo[i].getReceiver().getSta());
//				if (useST == null) useST = useTTSiteCorrections;
//				if (useST)
//				{
////X					double siteTerm = geoModel.getSiteTerm(rayInfo[i]);
////X					rayInfo[i].setAttribute(GeoAttributes.TT_SITE_CORRECTION, siteTerm);
////X					if (siteTerm != Globals.NA_VALUE)
////X					{
////X						double tt = rayInfo[i].getAttribute(GeoAttributes.TRAVEL_TIME);
////X						if (tt != Globals.NA_VALUE)
////X							rayInfo[i].setAttribute(GeoAttributes.TRAVEL_TIME, tt+siteTerm);
////X					}
//					
//					double st = Globals.NA_VALUE;
//					if (geoTessModel instanceof GeoTessModelSiteData)
//					{
//						double tt = rayInfo[i].getAttribute(GeoAttributes.TRAVEL_TIME);
//						if (tt != Globals.NA_VALUE)
//						{
//							String staname = rayInfo[i].getReceiver().getSta();
//							double origTime = rayInfo[i].getSource().getOriginTime();
//							int attrIndx = geoTessModel.getMetaData().getAttributeIndex(rayInfo[i].getWaveType().name());
//							st = ((GeoTessModelSiteData) geoTessModel).getSiteTerm(
//									   attrIndx, staname, tt, origTime);
//							rayInfo[i].setAttribute(GeoAttributes.TRAVEL_TIME, tt + st);
//						}
//					}
//					rayInfo[i].setAttribute(GeoAttributes.TT_SITE_CORRECTION, st);
//				}
//
//				if (ellipticityCorrections != null)
//				{
//					double ellipCorr = ellipticityCorrections.getEllipCorr(request.getPhase(), 
//							request.getReceiver(), request.getSource());
//					rayInfo[i].setAttribute(GeoAttributes.TT_ELLIPTICITY_CORRECTION, ellipCorr);
//					double tt = rayInfo[i].getAttribute(GeoAttributes.TRAVEL_TIME);
//					if (tt != Globals.NA_VALUE)
//						rayInfo[i].setAttribute(GeoAttributes.TRAVEL_TIME, tt+ellipCorr);
//				}
//
//				if (getVerbosity() > 0)
//				{
//					if (i == 0)
//						println("fastest ray = " + rayInfo[i].toString());
//					else
//						println("fast    ray = " + rayInfo[i].toString());
//				}
//
//			}
//
//			//Xfor (RayInfo aRay : rayInfo)
//			for (RayInfo aRay : rayInfo)
//			{
//				aRay.setStatusLog(logBuffer.toString());
//				aRay.setErrorMessage(errorMessages.toString());
//				aRay.setAttribute(GeoAttributes.CALCULATION_TIME, 
//						(System.currentTimeMillis() - calcOriginTime) * 1e-3);
//			}
//			logBuffer.setLength(0);
//			return rayInfo;
//		}
//		catch (Exception ex)
//		{
//			if (ex.getMessage() == null)
//				errorMessages.append("null pointer exception");
//			else
//				errorMessages.append(ex.getMessage());
//
//			errorMessages.append(System.getProperty("line.separator"));
//			errorMessages.append(String.format("Version = %s%n", getVersion()));
//
//			errorMessages.append(String.format("Receiver = %1.8f, %1.8f, %1.6f%n",
//					receiver.getLatDegrees(), receiver.getLonDegrees(),
//					receiver.getDepth()));
//
//			errorMessages.append(String.format("Source = %1.8f, %1.8f, %1.6f%n",
//					source.getLatDegrees(), source.getLonDegrees(),
//					source.getDepth()));
//
//			errorMessages.append(String.format("Phase = %s%n", request
//					.getPhase().toString()));
//
//			errorMessages.append(String.format("layer = %s%n", layerName));
//
//			errorMessages.append(String.format("distance = %1.6f%n",
//					receiver.distanceDegrees(source)));
//
//			// Recreate the stack trace into the error String.
//			for (int i = 0; i < ex.getStackTrace().length; i++)
//				errorMessages.append(ex.getStackTrace()[i].toString()).append(
//				"\n");
//
//			//X rayInfo=new RayInfo[1];
//			//X rayInfo[0] = new RayInfo(request, this, "ERROR");
//			rayInfo = new RayInfo[1];
//			rayInfo[0] = new RayInfo(request, this, "ERROR");
//			rayInfo[0].setErrorMessage(errorMessages.toString());
//			rayInfo[0].setStatusLog(logBuffer.toString());
//			rayInfo[0].setAttribute(GeoAttributes.CALCULATION_TIME, 
//					(System.currentTimeMillis() - calcOriginTime) * 1e-3);
//			logBuffer.setLength(0);
//			return rayInfo;
//		}
//	}

	//	public void getVelocityGradient(InterpolatedNodeLayered center,
	//			GeoAttributes waveType, int majorLayerIndex, double[] gradient)
	//	throws GeoModelException
	//	{
	//		getTetrahedron().getVelocityGradient(center, waveType, majorLayerIndex,
	//				gradient);
	//	}
	//
	//	protected double getRadialVelocityGradient(InterpolatedNodeLayered center,
	//			GeoAttributes waveType, int majorLayerIndex)
	//	throws GeoModelException
	//	{
	//		double[] gradient = new double[3];
	//		getTetrahedron().getVelocityGradient(center, waveType, majorLayerIndex,
	//				gradient);
	//		double magnitude = GeoVector.normalize(gradient);
	//		double angle = GeoVector.angle(gradient, center.getUnitVector());
	//		return magnitude * cos(angle);
	//	}

	public void print(String s)
	{
		logBuffer.append(s);
		System.out.print(s);
	}

	public void println(String s)
	{
		logBuffer.append(s).append(Globals.NL);
		System.out.println(s);
	}

	public void println()
	{
		logBuffer.append(Globals.NL);
		System.out.println();
	}

	/**
	 * 
	 * @return String: bender
	 */
	@Override
	public String getPredictorName() {
		return getAlgorithmName();
	}

	/**
	 * 
	 * @return String
	 */
	@Override
	public String getPredictorVersion()
	{
		return getVersion();
	}
//
//	/**
//	 * The outer loop convergence criteria used in Ray.optimize() An n x 3
//	 * array. First number is source receiver separation in deg. Second number
//	 * is convergence criteria (in seconds) for all distances less than first
//	 * number. Third number is minimum point spacing on the ray (in km).
//	 * 
//	 * @param convergenceCriteria  double[][]
//	 * @throws BenderException
//	 */
//	public void setConvergenceCriteria(double[][] convergenceCriteria)
//	throws BenderException
//	{
//		if (convergenceCriteria[0].length != 2)
//			throw new BenderException(ErrorCode.FATAL, 
//			"convergenceCriteria must be N x 2 array. \n");
//		for (int i = 1; i < convergenceCriteria.length; ++i)
//			if (convergenceCriteria[i][0] < convergenceCriteria[i - 1][0])
//				throw new BenderException(ErrorCode.FATAL, 
//				"convergenceCriteria[i][0] (distance in deg) must be monotonically decreasing \n");
//		this.convergenceCriteria = convergenceCriteria;
//	}
//
//	/**
//	 * The outer loop convergence criteria used in Ray.optimize() An n x 3
//	 * array. First number is source receiver separation in deg. Second number
//	 * is convergence criteria (in seconds) for all distances less than first
//	 * number. Third number is minimum point spacing on the ray (in km).
//	 * 
//	 * @throws BenderException
//	 * @return double[][]
//	 */
//	public double[][] getConvergenceCriteria()
//	{
//		return convergenceCriteria;
//	}

	/**
	 * Rays with travel times that are within this tolerance level of the travel
	 * time of the fastest ray are included in the list or rays returned by
	 * Bender. In seconds. Default is zero.
	 * 
	 * @return double
	 */
	public double getFastTravelTimeTolerance()
	{
		return BenderConstants.FAST_TRAVELTIME_TOLERANCE;
	}

	/**
	 * Rays with travel times that are within this tolerance level of the travel
	 * time of the fastest ray are included in the list or rays returned by
	 * Bender. In seconds. Default is zero.
	 * 
	 * @param fastTravelTimeTolerance
	 *            double
	 */
	public void setFastTravelTimeTolerance(double fastTravelTimeTolerance)
	{
		BenderConstants.FAST_TRAVELTIME_TOLERANCE = fastTravelTimeTolerance;
	}

	/**
 	 * Returns the travel time tolerance required for a ray to converge.
 	 * 
 	 * @return The travel time tolerance required for a ray to converge.
 	 */
 	public double getTravelTimeConvergenceTolerance()
 	{
 		return currentRayConvgncCriteria[0];
 	}
// 
// 	/**
// 	 * Sets the travel time tolerance required for a ray to converge.
// 	 * @param travelTimeTolerance The travel time tolerance required for a ray
// 	 * 														to converge.
// 	 */
// 	public void setTravelTimeConvergenceTolerance(double travelTimeTolerance)
// 	{
// 		convergenceCriteria[0][1] = travelTimeTolerance;
// 	}
 
 	/**
 	 * Returns the minimum node spacing on a ray required for convergence in km.
 	 * 
 	 * @return The minimum node spacing on a ray required for convergence in km.
 	 */
 	public double getMinimumRayNodeSpacing()
 	{
 		return currentRayConvgncCriteria[1];
 	}
// 
// 	/**
// 	 * Sets the minimum node spacing on a ray required for convergence in km.
// 	 * 
// 	 * @param minimumNodeSpacing The minimum node spacing on a ray required for
// 	 * 													 convergence in km.
// 	 */
// 	public void setMinimumRayNodeSpacing(double minimumNodeSpacing)
// 	{
// 		convergenceCriteria[0][2] = minimumNodeSpacing;
// 	}

	@Override
	public String getModelDescription() throws GMPException
	{
		if (geoTessModel == null)
			return "No model loaded by Bender";
		return geoTessModel.toString();
	}

	/**
	 * getModelInfo
	 * 
	 * @return String
	 * @throws GMPException
	 */
	public String getModelInfo() throws GMPException
	{
		//X return geoModel.toString();
		return geoTessModel.toString();
	}

	/**
	 * Retrieve a brief name for the model loaded into Bender.
	 * @throws IOException 
	 */
	@Override
	public String getModelName()
	{
		if (geoTessModel == null)
			return "NULL";
		try
		{
		  return geoTessModel.getMetaData().getInputModelName();
		}
		catch (Exception ex)
		{
			return ex.getMessage();
		}
	}

	/**
	 * @return the allowICBDiffraction
	 */
	public boolean isICBDiffractionAllowed()
	{
		return allowICBDiffraction;
	}

	/**
	 * @param allowICBDiffraction  allowICBDiffraction to set
	 */
	public void setAllowICBDiffraction(boolean allowICBDiffraction)
	{
		this.allowICBDiffraction = allowICBDiffraction;
	}

	/**
	 * @return the allowCMBDiffraction
	 */
	public boolean isCMBDiffractionAllowed()
	{
		return allowCMBDiffraction;
	}

	/**
	 * @param allowCMBDiffraction  allowCMBDiffraction to set
	 */
	public void setAllowCMBDiffraction(boolean allowCMBDiffraction)
	{
		this.allowCMBDiffraction = allowCMBDiffraction;
	}

	/**
	 * @return the allowMOHODiffraction
	 */
	public boolean isMOHODiffractionAllowed()
	{
		return allowMOHODiffraction;
	}

	/**
	 * @param allowMOHODiffraction  allowMOHODiffraction to set
	 */
	public void setAllowMOHODiffraction(boolean allowMOHODiffraction)
	{
		this.allowMOHODiffraction = allowMOHODiffraction;
	}

	@Override
	public long getAlgorithmId()
	{
		return algorithmId;
	}

	@Override
	public File getModelFile() {
	    return geoTessModel == null ? null : geoTessModel.getMetaData().getInputModelFile();
	}

	@Override
	public long getModelId()
	{
		return modelId;
	}

	@Override
	public void setAlgorithmId(long algorithmId)
	{
		this.algorithmId = algorithmId;
	}

	@Override
	public void setModelId(long modelId)
	{
		this.modelId = modelId;
	}

	@Override
	public Prediction getNewPrediction(PredictionRequest predictionRequest, String msg) {
		return new RayInfo(predictionRequest, this, msg);
	}

	@Override
	public Prediction getNewPrediction( PredictionRequest predictionRequest, Exception ex) {
		return new RayInfo(predictionRequest, this, ex);
	}

	/**
	 * Retrieve an enum value that specifies which algorithm was used 
	 * to search for the positions on discontinuities where Snell's Law is 
	 * satisfied.  Possibilities are SIMPLEX (default) or BRENTS.
	 * @return SearchMethod.SIMPLEX or SearchMethod.BRENTS.
	 */
	public SearchMethod getSearchMethod()
	{
		return searchMethod;
	}

	/**
	 * Specify which algorithm to use to search for the positions on
	 * discontinuities where Snell's Law is satisfied.  
	 * Possibilities are AUTO, SIMPLEX or BRENTS.
	 * Default is AUTO.
	 */
	public void setSearchMethod(SearchMethod searchMethod)
	{
		this.searchMethod = searchMethod;
	}

	@Override
	public Object getEarthModel() {
		return geoTessModel;
	}

	//@Override
	public double getSurfaceDepth(GeoVector position) throws GMPException 
	{
		//X return geoModel.getInterpolatedNodeLayered(position).getSurfaceDepth();
		try
		{
		  GeoTessPosition pstn = GeoTessPosition.getGeoTessPosition(geoTessModel);
		  pstn.set(position.getUnitVector(), position.getRadius());
		  return pstn.getSurfaceDepth();
		}
		catch (Exception ex)
		{
			throw new GMPException(ex.getMessage());
		}
	}

	//@Override
	public double getSurfaceRadius(GeoVector position) throws GMPException 
	{
		//X return geoModel.getInterpolatedNodeLayered(position).getSurfaceRadius();
		try
		{
		  GeoTessPosition pstn = GeoTessPosition.getGeoTessPosition(geoTessModel);
		  pstn.set(position.getUnitVector(), position.getRadius());
		  return pstn.getSurfaceRadius();
		}
		catch (Exception ex)
		{
			throw new GMPException(ex.getMessage());
		}
	}

	public GradientCalculator getGradientCalculator()
	{
		return gradientCalculator;
	}

	/**
	 * Retrieve the travel time between the specified source and receiver, in seconds.
	 * <p>If the GeoModel that supports this Bender object contains a site corretion for
	 * the specified station at the time of the origin time, then the travel time will
	 * include the site correction.
	 * @param sta  station name
	 * @param staLat station latitude in degrees
	 * @param staLon  station longitude in degrees
	 * @param staElev station elevation in km above sea level
	 * @param originLat origin latitude in degrees
	 * @param originLon origin longitude in degrees
	 * @param originDepth origin depth in km below sea level
	 * @param originTime origin epoch time in seconds since 1/1/1970
	 * @param phase seismic phase (P, Pn or Pg)
	 * @return travel time in seconds
	 * @throws GMPException
	 */
	public double getTravelTime(String sta, double staLat, double staLon, double staElev,
			double originLat, double originLon, double originDepth, double originTime, 
			String phase) 
	throws Exception
	{
		Receiver receiver = new Receiver(1, sta, -1e9, 1e9, 
				new GeoVector(staLat, staLon, -staElev, true));

		Source source = new Source(new GeoVector(originLat, originLon, originDepth, true),
				                       originTime);

		SeismicPhase seismicPhase = SeismicPhase.valueOf(phase.trim());

		PredictionRequest request = new PredictionRequest(1, receiver, source, seismicPhase,
				EnumSet.of(GeoAttributes.TRAVEL_TIME, GeoAttributes.TT_SITE_CORRECTION), true);

		return getPrediction(request).getAttribute(GeoAttributes.TRAVEL_TIME);
	}

//	/**
//	 * Retrieve the travel time between the specified source and receiver, in seconds.
//	 * Only works for IMS primary and auxiliary stations.  Convenient but inefficient relative to 
//	 * the other getTravelTime() function because a new IMS_Stations object has to be
//	 * constructed for each call.
//	 * 
//	 * <p>If the GeoModel that supports this Bender object contains a site corretion for
//	 * the specified station at the time of the origin time, then the travel time will
//	 * include the site correction.
//	 * @param sta  station name (must be an IMS station).
//	 * @param originLat origin latitude in degrees
//	 * @param originLon origin longitude in degrees
//	 * @param originDepth origin depth in km below sea level
//	 * @param originTime origin epoch time in seconds since 1/1/1970
//	 * @param phase seismic phase (P, Pn or Pg)
//	 * @return travel time in seconds
//	 * @throws GMPException
//	 */
//	public double getTravelTime(String sta, double originLat, double originLon, double originDepth, 
//			double originTime, String phase) 
//	throws GMPException
//	{
//		Receiver receiver = (IMSNetwork.primary.getReceiver(sta, GMTFormat.getJDate(originTime)));
//
//		if (receiver == null)
//			return Globals.NA_VALUE;
//
//		Source source = new Source(new GeoVector(originLat, originLon, originDepth, true),
//				                       originTime);
//
//		SeismicPhase seismicPhase = SeismicPhase.valueOf(phase.trim());
//
//		PredictionRequest request = new PredictionRequest(1L, receiver, source, seismicPhase,
//				EnumSet.of(GeoAttributes.TRAVEL_TIME, GeoAttributes.TT_SITE_CORRECTION), true);
//
//		return getPrediction(request).getAttribute(GeoAttributes.TRAVEL_TIME);
//
//	}

	/**
	 * If value is ON_THE_FLY then velocity gradient calculations are performed
	 * on-the-fly by Bender.  If value is PRECOMPUTED, the velocity gradient is
	 * pre-computed, stored on the grid in geomodel, and values are interpolated
	 * upon request.
	 * @return the gradientCalculator
	 */
	public GradientCalculationMode getGradientCalculatorMode()
	{
		return gradientCalculatorMode;
	}

	/**
		/**
	 * If value is BENDER then velocity gradient calculations are performed
	 * on-the-fly by Bender.  If value is GEOMODEL, the velocity gradient is
	 * pre-computed, stored on the grid in geomodel, and values are interpolated
	 * upon request.
	 * @param gradientCalculator the gradientCalculator to set
	 * @throws GeoTessException 
	 */
	public void setGradientCalculator(GradientCalculationMode gradientCalculator) throws GeoTessException
	{
		this.gradientCalculatorMode = gradientCalculator;

		if (gradientCalculator == GradientCalculationMode.PRECOMPUTED)
		{
			if (precomputeGradients)
			{
				geoTessModel.getMetaData().setGradientCalculatorTetSize(tetSize);
				int lastGradientLayer = geoTessModel.getMetaData().getInterfaceIndex("M660");
				if (lastGradientLayer == -1) lastGradientLayer = 0; 
//				if (lastGradientLayer < 0)
//					throw new GeoTessException("\nThere is no layer with name M660");
				int[] layers = new int[geoTessModel.getNLayers() - lastGradientLayer];
				for (int i=0; i<layers.length; ++i)
					layers[i] = lastGradientLayer + i;

				String[] attrNames = geoTessModel.getMetaData().getAttributeNames();
				for (int a=0; a < attrNames.length; ++a)
				  if (attrNames[a].equals(GeoAttributes.PSLOWNESS.name()) ||
				      attrNames[a].equals(GeoAttributes.SSLOWNESS.name()))
					  geoTessModel.computeGradients(a, true, layers);
			}
		}
		else if (gradientCalculator == GradientCalculationMode.ON_THE_FLY)
		{		
			this.gradientCalculator = new GradientCalculator(this.geoTessModel, tetSize);  // slow: compute gradients on the fly
		}
		else
			throw new GeoTessException("gradientCalculator equal to " +
		                              gradientCalculator.toString() +
					                        " but only GEOTESSMODEL and BENDER were tested.");

	}
	
	public void setGradientCalculator(GradientCalculationMode gradientCalculator,
			                              double tetSize) throws GeoTessException
	//X  throws GeoModelException
	{
		this.tetSize = tetSize;
		setGradientCalculator(gradientCalculator);
	}

	public static final List<String> getRecognizedProperties()
	{
		return Arrays.asList(new String[] {PROP_MAX_PROCS,
				PROP_GRADIENT_CALCULATOR,
				PROP_PRECOMPUTE_GRADIENTS,
				"benderAllowCMBDiffraction",
				PROP_MODEL,
				PROP_UNCERTAINTY_TYPE,
				PROP_UNCERTAINTY_DIR,
				PROP_UNCERTAINTY_MODEL
		});
	}

	public boolean isUseTTSiteCorrections()
	{
		return useTTSiteCorrections;
	}

	public void setUseTTSiteCorrections(boolean useTTSiteCorrections)
	{
		this.useTTSiteCorrections = useTTSiteCorrections;
	}

	public void clearUseTTSiteCorrectionsStationList()
	{
		this.useTTSiteCorrectionsStationList.clear();
	}

	public void setUseTTSiteCorrectionsStationList(boolean useTTSiteCorr, String stationList)
	{
		String[] stations = stationList.replaceAll(",", " ").replaceAll("'", "").replaceAll("\"", "").split(" ");
		for (String sta : stations)
			this.useTTSiteCorrectionsStationList.put(sta, useTTSiteCorr);
	}

	@Override
	public PredictorType getPredictorType()
	{
		return PredictorType.BENDER;
	}

	@Override
	public EnumSet<GeoAttributes> getSupportedAttributes()
	{
		return BenderConstants.supportedAttributes;
	}

	@Override
	public EnumSet<SeismicPhase> getSupportedPhases()
	{
		// see if P and or S slowness are defined.
		boolean hasPSlowness = geoTessModel.getMetaData().getNodeAttributes().
				isAttributeDefined(GeoAttributes.PSLOWNESS.name());
		boolean hasSSlowness = geoTessModel.getMetaData().getNodeAttributes().
				isAttributeDefined(GeoAttributes.SSLOWNESS.name());

		// build a set of all supported phases based on P and S slowness availability
		// int the model
		Set<SeismicPhase> enumSet = new HashSet<>();
		if (hasPSlowness)
		{
			enumSet.addAll(supportedPPhases);
			if (hasSSlowness)
			{
				enumSet.addAll(supportedSPhases);
				enumSet.addAll(supportedPSPhases);
			}
		}
		else if (hasSSlowness)
			enumSet.addAll(supportedSPhases);
		
		// return total supported set as and enum set
		return EnumSet.copyOf(enumSet);
	}

	public String profileString(GeoTessPosition profile)
	{
  	profile.getEarthShape().getLatDegrees(profile.getVector());
    StringBuffer buf = new StringBuffer(String.format(
        "lat,lon = %1.4f, %1.4f%n",
        profile.getEarthShape().getLatDegrees(profile.getVector()),
        profile.getEarthShape().getLonDegrees(profile.getVector())));

    return buf.toString();
	}
//
//	@Override
//	public void   setDiagonal(double[] x, double[] diag)
//	{
//		// LBFGS Method stub
//	}

	//***
	// Make computeLevelRays() the call to optimize the ray for a set of fixed
	// underside reflection locations (0 to nrefl). If zero then on return
	// from this call the container "rays" contains all valid rays and fastestRay
	// has the fastest. Same is true for 1 or more underside reflections except
	// this call is made many times by LBFGS.setFunctionAndGradient(x, g) to find
	// the optimum location of the underside reflections.
	// 
	// Must move the outer ray optimize iteration from the ray constructor down to
	// the ray branch level. Each RayBranch finds the optimum setting given fixed
	// end points which may be source/USR as the first point or receiver/USR as
	// second point. Master level is now unessessary as the level is iterated over
	// in each RayBranch to find the minimum.
	// 
	// Fastest ray in a RayBranchBottom needs to be a refraction, if one exists;
	// a diffraction, if requested; or a reflection, if neither of the others
	// conditions are met.
	
	// Each branch can be optimized with fixed end-points (min travel time). Then
	// USR can be optimized using LBFGS. Level loop can be moved to RayBranchBottom,
	// since that is the only branch type that needs a level definition. Additionally,
	// the coupled level loop can be eliminated as the LBFGS and RayBranch
	// optimizations are now decoupled. Each RayBranchBottom will will find several
	// rays but only the fastest is kept for the LBFGS processing. The old concept
	// of keeping multiple rays and associated RayInfo objects may be obsolete.

	// 
	// Move outer iteration of optimize down to each ray branch
	// Move layer level iteration down to RayBranchBottom
	// Have Ray optimize simply call optimize for each RayBranch
	// 
  
	
	protected Ray fastRay = null;
  protected double fastRayTravelTime = 0.0;
//
//  private void computeLevelRays() throws BenderException
//  {
//		rays.clear();
//  	Ray ray = null, ray0 = fastRay;
//  	int maxLayerLevel = 1;
//  	int layerLevel = 0;
//    do
//    {
//			try
//			{
//      	if (ray0 == null)
//      	{
//      		// build the first initial ray ... if 1 or more RayBranchBottom
//      		// branches are present then multiple levels may exist ... get the
//      		// maximum level ... if larger than 1 fill the pierce point list
//
//					ray = new Ray(this, getReceiverProfile(), getSourceProfile(), "");
//					maxLayerLevel = ray.getMaxLayerLevelCount();
//					ray0 = ray;
//					fastRay = ray0;
//					//buildMasterLevelHeirarchy(ray0);
//      	}
//				else
//				{
//					ray = new Ray(ray0, "", false);
//				}
//
//				if (updateFastRay(ray)) rays.add(ray);
//				//layerLevel = this.getNextValidMasterLevel(layerLevel, maxLayerLevel);
//				layerLevel = 0;
//			}
//			catch(BenderException rayEx)
//			{
//				if (rayEx.getErrorCode() == ErrorCode.FATAL)
//					throw rayEx;
//
//				// record error and keep going. these are innocuous errors
//				errorMessages.append(rayEx.getMessage()
//						+GMPException.getStackTraceAsString(rayEx)+"\n");
//			}
//			catch(Exception ex)
//			{
//				// record error and keep going. these are innocuous errors
//				errorMessages.append(ex.getMessage()
//						+GMPException.getStackTraceAsString(ex)+"\n");
//			}
//    } while (layerLevel < maxLayerLevel);
//  }
//
//  protected int lBFGSFunctionMethodCallCount = 0;
//	@Override
//	public double setFunctionAndGradient(double[] x, double[] g)
//	       throws LBFGSException
//	{
//	  // Output initial bottom-side reflection vector
//
//		lBFGSFunctionMethodCallCount++;
//		double del_lat = Math.toRadians(0.01);
//		double del_lon = Math.toRadians(0.01);
//    if (lBFGSFunctionMethodCallCount == 1)
//    	x[1] = .08727;
//
//		if (getVerbosity() > 0)
//		{
//		  println("BSROPT.F  LBFGS Optimization Function Value/Gradient Method Start " + Globals.repeat("F", 91));
//		  println("BSROPT.F    lBFGS Function Value/Gradient Method Call Count = " +
//		  		    lBFGSFunctionMethodCallCount);
//		  print("BSROPT.F    Input Bottom-Side Reflection Vector (lat, lon ... in deg) = ");
//		  for (int i = 0; i < x.length; ++i)
//		  {
//		  	print(String.format("%8.3f", Math.toDegrees(x[i])));
//		  	if (i < x.length-1)
//		  			print(", ");
//		  }
//		  println(); println();
//		}
//
//		try
//		{
//			if (getVerbosity() > 0)
//		    println("BSROPT.F    Solve for Input Position BSR Input Ray ..");
//
//			fastRay = new Ray(this, getReceiverProfile(), getSourceProfile());
//		  fastRayTravelTime = fastRay.getTravelTime();
//		  // get vector of radii at each BSR position
//		  double[] r = new double [x.length/2];
//		  for (int i = 0; i < r.length; ++i)
//		  {
//		  	r[i] = fastRay.getBottomSideReflectionRadius(i);
//		  }
//
//			if (getVerbosity() > 0)
//			{
//			  println("BSROPT.F    Input Position BSR Input Ray Travel Time (sec) = " +
//			  		    fastRayTravelTime);
//			  println();
//			}
//
//		  // loop over all bottom side reflections
//
//		  double dlat = 0.0, dlon = 0.0;
//		  Ray ray = null;
//		  for (int i = 0; i < x.length / 2; i += 2)
//		  {
//				if (getVerbosity() > 0)
//				{
//				  println("BSROPT.F    Solve for Bottom-Side Reflection Derivative " + i + " ...");
//	        println();
//				}
//
//				// calculate ray travel time for lat+del_lat and subtract ray travel time
//				// for ray lat-del_lat (central difference) ... reset under side reflection
//				// back to input latitude (x[i]) when complete
//
//				undersideReflectionLatLonvector[i] = x[i] + del_lat;
//				ray = new Ray(this, getReceiverProfile(), getSourceProfile());
//			  dlat  = ray.getTravelTime();
//				undersideReflectionLatLonvector[i] = x[i] - del_lat;
//				ray = new Ray(this, getReceiverProfile(), getSourceProfile());
//			  dlat -= ray.getTravelTime();
//				undersideReflectionLatLonvector[i] = x[i];
//        g[i] = dlat;
//        
//			  if (getVerbosity() > 0)
//				{
//			    println("BSROPT.F    Latitude Derivative Travel Time Difference for " +
//			            "BSR " + i/2 + "  = " + dlat);
//				  println();
//				}
//
//				// calculate ray travel time for lon+del_lon and subtract ray travel time
//				// for ray lon-del_lon (central difference) ... reset under side reflection
//				// back to input longitude (x[i+1]) when complete
//
//				undersideReflectionLatLonvector[i+1] = x[i+1] + del_lon;
//				ray = new Ray(this, getReceiverProfile(), getSourceProfile());
//			  dlon  = ray.getTravelTime();
//				undersideReflectionLatLonvector[i+1] = x[i+1] - del_lon;
//				ray = new Ray(this, getReceiverProfile(), getSourceProfile());
//			  dlon -= ray.getTravelTime();
//				undersideReflectionLatLonvector[i+1] = x[i+1];
//        g[i+1] = dlon;
//
//			  if (getVerbosity() > 0)
//				{
//			    println("BSROPT.F    Longitude Derivative Travel Time Difference for " +
//			            "BSR " + i/2 + "  = " + dlat);
//				  println();
//				}
//		  }
//
//			if (getVerbosity() > 0)
//			{
//			  print("BSROPT.F    Bottom-Side Reflection Gradient Vector = ");
//			  for (int i = 0; i < x.length; ++i)
//			  {
//			  	print(String.format("%14.6e", g[i]));
//			  	if (i < x.length-1)
//			  			print(", ");
//			  }
//			  println(); println();
//			}
//		}
//		catch (Exception ex)
//		{
//			if ((ex instanceof BenderException) &&
//			    (((BenderException) ex).getErrorCode() == ErrorCode.FATAL))
//				throw new LBFGSException(ex.getMessage());
//		}
//
//    // Done ... return new travel time result
//
//		if (getVerbosity() > 0)
//		{
//			println();
//		  println("BSROPT.F  LBFGS Optimization Function Value/Gradient Method End   " + Globals.repeat("F", 91));
//		  println();
//		}
//
//    return fastRayTravelTime;
//	}
//
//	private boolean updateFastRay(Ray ray) throws BenderException, GeoTessException
//	{
//		boolean invalid = (ray.getRayType() == RayType.ERROR) ||
//		                  (ray.getRayType() == RayType.INVALID);
//		if (!invalid)
//		{
//			double tt = ray.getTravelTime();
//			if ((fastRay == null) || (fastRay.getTravelTime() > tt))
//			{
//				fastRay = ray;
//				fastRayTravelTime = tt;
//			}
//		}
//
//		return !invalid;
//	}
//	
//	// test of bfgs
//	// f = e^(x1-1) + e^(-x2+1) + (x1 - x2)^2
//	// df/dx1 = e^(x1-1) + 2(x1 - x2)
//	// df/dx2 = -e^(-x2+1) - 2(x1 - x2)
//	
//	//
//
//	@Override
//	public double setFunctionAndGradient(double[] x, double[] grad)
//	       throws LBFGSException
//	{
//		double f = Math.exp(x[0]-1) + Math.exp(-x[1]+1) + (x[0] - x[1]) * (x[0] - x[1]);
//		grad[0] = Math.exp(x[0]-1) + 2 * (x[0] - x[1]);
//		grad[1] = -Math.exp(-x[1]+1) - 2 * (x[0] - x[1]);
//
//		return f;
//	}


  private Ray bpRay = null;
  
  private double currentTTTol						= 0.0;
  private double ttTolReduction              = 0.0;

  private double currentMinNodeSpacing	= 0.0;
  private double minNodeSpcReduction    = 0.0;
  private int    nTolReductions         = 1;

  //private double lastBPMove							= 0.0;
  private double lastTT									= 0.0;
  
  //private double nextToLastBPMove				= 0.0;
  private double nextToLastTT						= 0.0;

  @SuppressWarnings("unused")
private double bpAboveBelowSep				= 0.0;

  private GeoTessPosition bpStart       = null;
  private GeoTessPosition bpEnd         = null;
  private double[]        ngc           = null;

  protected GeoTessPosition currentBPPosition = null;
  private GeoTessPosition lastBPPosition = null;
  
  private int             bpLayer       = -1;
  private AmoebaBouncePoint amoeba      = null;

  private double          minBPMove     = 1.0; // km

  //private double          simplexTTDepthPhaseScale = 1.0;
  private double          lastValidXBrentsxX   = -1.0;
  private double          lastValidXBrentsStep = -1.0;

  @Override
	public double bFunc(double x) throws Exception
	{
		// swap current BP position with previous one

		GeoTessPosition tmp = lastBPPosition;
		lastBPPosition = currentBPPosition;
		currentBPPosition = tmp;

		// calculate a new position from the input Brents fraction

		currentBPPosition.setIntermediatePosition(bpStart, bpEnd, x, bpLayer);

	  // set ray accuracy tolerances and calculate the bounce point segment
		// midpoint separation

		if (Math.abs(nextToLastTT - lastTT) < ttTolReduction * currentTTTol)
			reduceRayAccuracyTolerances();
		setBouncePointSegmentSeparation();

		// calculate the ray at the input position and save its previous and
		// current travel time

		calculateBouncePointRay(currentBPPosition, lastBPPosition);
		nextToLastTT = lastTT;
		lastTT = bpRay.getTravelTime();
		if (phaseRayBranchModel.isDepthPhase())
		{
			if (!bpRay.getBranches().get(0).isValidDepthPhase())
			{
				lastTT += 200.0;
			}
			else
			{
				lastValidXBrentsxX = x;
				lastValidXBrentsStep = Math.abs(x - lastValidXBrentsStep);
			}
		}
		else
		{
			if (!bpRay.getBranches().get(0).isValidRayBottomPhase())
			{
				lastTT -= 200.0;
			}
			else
			{
				lastValidXBrentsxX = x;
				lastValidXBrentsStep = Math.abs(x - lastValidXBrentsStep);
			}
		}

		// return the travel time

		return lastTT;
	}

	private int nResetRay = 0;
	private int nNewRay   = 0;

	public int getBouncePointRayEvaluationCount()
	{
		return nResetRay + nNewRay;
	}

	public int getBouncePointNewRayEvaluationCount()
	{
		return nNewRay;
	}

	public int getBouncePointResetRayEvaluationCount()
	{
		return nResetRay;
	}

	private void calculateBouncePointRay(GeoTessPosition newPos,
																			 GeoTessPosition lastPos) throws Exception
	{
		//if (Math.abs(nextToLastBPMove - lastBPMove) < 2.0 * bpAboveBelowSep)
//		if (Vector3D.distance3D(newPos.get3DVector(),
//													  lastPos.get3DVector()) < 5.0 * bpAboveBelowSep)	
//		{
//	    // coarsen the current ray and reset the bounce point position and then
//			// reoptimize.
//
//			bpRay.resetToInitialNodeDensity();
//			
//			// set bp position in bp segement
//			setFixedReflectionMiddleNode(newPos);
//      bpRay.setStatus(RayStatus.INITIALIZED);
//			bpRay.optimizeBranches(currentTTTol, currentMinNodeSpacing);
//			++nResetRay;
//		}
//		else
//		{
			// set the input bounce point as a fraction of the source receiver
			// point positions then build a new ray

			// set bp position into initial array undersideReflectionLatLonvector

			double lat = newPos.getLatitude();
			double lon = newPos.getLongitude();
			undersideReflectionLatLonvector[0] = lat;
			undersideReflectionLatLonvector[1] = lon;
			bpRay = new Ray(this, getReceiverProfile(), getSourceProfile(),
											currentTTTol, currentMinNodeSpacing);
			++nNewRay;
//		}
	}

	private void reduceRayAccuracyTolerances()
	{
		currentTTTol /= ttTolReduction;
		if (currentTTTol < currentRayConvgncCriteria[0])
			currentTTTol = currentRayConvgncCriteria[0];
		
		currentMinNodeSpacing -= minNodeSpcReduction;
		if (currentMinNodeSpacing < currentRayConvgncCriteria[1])
			currentMinNodeSpacing = currentRayConvgncCriteria[1];
	}

	private void setBouncePointSegmentSeparation()
	{
		if (bpRay != null)
		{
			GeoTessPosition node0 = bpRay.getBranches().get(0).getLastActiveSegment().getMiddleNode();
			GeoTessPosition node1 = bpRay.getBranches().get(1).getFirstActiveSegment().getMiddleNode();
			bpAboveBelowSep = Vector3D.distance3D(node0.get3DVector(), node1.get3DVector());
		}
		else
			bpAboveBelowSep = -1.0;
	}

	@SuppressWarnings("unused")
	private void setFixedReflectionMiddleNode(GeoTessPosition gtp) throws Exception
	{
		RaySegment rs = bpRay.getBranches().get(0).getNextDirectionChangeSegment();
		GeoTessPosition node = rs.getMiddleNode();

		node.setTop(node.getIndex(), gtp.getVector());

		// whether or not the ray interacts with this interface as a
		// reflection was determined early in this method by calling
		// RaySegement.checkReflection(). If we get here, then bottom segment
		// only has 3 nodes and current node is a reference to the middle node.
		// Set the other two nodes equal to copies of node. Using copy(node)
		// ensures that their references in the previous and next segments are
		// preserved. Only their contents are updated to the contents of node.
		rs.getNodes().getFirst().copy(node);
		rs.getNodes().getLast().copy(node);
	}

	private double[][] nBPSet = {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}};
	//private double snellsLawSimplexOptimizationSwitchSize = 10.0;
	
	public double simplexFunction(double[] x) throws Exception
	{
		// swap current BP position with previous one

		GeoTessPosition tmp = lastBPPosition;
		lastBPPosition = currentBPPosition;
		currentBPPosition = tmp;

		// calculate a new position from the input lat, lon

		currentBPPosition.set(bpLayer, Math.toDegrees(x[0]), Math.toDegrees(x[1]), 0.0);
		currentBPPosition.setTop(bpLayer);

	  // set ray accuracy tolerances and calculate the bounce point segment
		// midpoint separation

		if (amoeba.isInitialized() && 
				((amoeba.getMeanTravelTimeDifference() < ttTolReduction * currentTTTol) ||
						(amoeba.getMeanSimplexPointSeparation() < 10.0 * minBPMove))) 
			reduceRayAccuracyTolerances();
		setBouncePointSegmentSeparation();

		// calculate the ray at the input position and save its travel time

		calculateBouncePointRay(currentBPPosition, lastBPPosition);
		nextToLastTT = lastTT;
		lastTT = bpRay.getTravelTime();

		// if the phase is a depth phase return the travel time. Otherwise,
		// calculate the gradient of travel time at the current BP position

		// save the current ray state into the amoeba
		amoeba.setCurrentExtendedData(lastTT, bpRay.getBouncePointFitness(0),
																	currentBPPosition, x, currentTTTol);

		// check to see if optimization should switch to snells law (instead of
		// travel time or travel time gradient).
		
//		if (amoeba.isInitialized() &&
//				amoeba.getMaximumSimplexPointSeparation() <
//				snellsLawSimplexOptimizationSwitchSize)
//			optimizeSnellsLaw = true;

		// return depth phase or XX phase optimization parameter

		if (phaseRayBranchModel.isDepthPhase())
		{
			// check for valid depth phase here ... update travel time and snells
			// law misfit with a wall value if the depth phase is not valid
			if (Math.abs(amoeba.currentSnellsLawMisfit) >= 2.0)
				invalidateSimplexResult(200.0, 1);
			else if (!bpRay.getBranches().get(0).isValidDepthPhase())
				invalidateSimplexResult(200.0, 2);

			// if the closest two points are within 1/2 of minBPMove then
			// redefine the simplex about the best point (point 0)

			if (amoeba.isInitialized() &&
					(amoeba.getMinMaxSimplexPointSeparationRatio() < .01))
					redefineSimplex();

		  return getDepthPhaseSimplexReturn();
		}
		else
		{
			if (Math.abs(amoeba.currentSnellsLawMisfit) >= 2.0)
				invalidateSimplexResult(-200.0, 1);
			else if (!bpRay.getBranches().get(0).isValidRayBottomPhase())
				invalidateSimplexResult(-200.0, 2);

			return getXXSimplexReturn();
		}
	}

	private void invalidateSimplexResult(double invalidValue, int type)
	{
		lastTT += invalidValue;
		amoeba.currentTravelTime += invalidValue;
		//amoeba.currentSnellsLawMisfit += 2.0;
		amoeba.currentValidResult = type;
	}

	//boolean optimizeSnellsLaw = false;
	protected double getDepthPhaseSimplexReturn()
	{
//		if (amoeba.swapSnellsLaw)
//		{
//			return amoeba.currentSnellsLawMisfit;
//		}
//		else
			return lastTT;
	}

	protected double getXXSimplexReturn() throws Exception
	{
//		if (amoeba.swapSnellsLaw)
//			return amoeba.currentSnellsLawMisfit;
//		else
			return estimateCurrentBPPositionGradient();
	}

	private double estimateCurrentBPPositionGradient() throws Exception
	{
		double[] dtt = {0.0, 0.0, 0.0};
		bpOrthogonalSet(currentBPPosition);
		if (amoeba.isInitialized())
		{
			// attempt to calculate the average gradient at the current BP position
			// using each simplex point. If the distance to the current BP position
			// is < than 1/4 of the minBPMove criteria then don't use it in the
			// calculation

			int cnt = 0;
		  cnt += estimateSimplexGradient(lastTT, dtt, 0);
		  cnt += estimateSimplexGradient(lastTT, dtt, 1);
		  cnt += estimateSimplexGradient(lastTT, dtt, 2);
		  if (cnt < 2)
		  {
		  	// if one or all simplex points lie within 1/4 of the minBPMove
		  	// criteria then we can't get a reliable gradient. If all points
		  	// violated the condition then reduce the accuracy and return a
		  	// large gradient. If only one point satisfied the condition then
		  	// redefine the simplex centered around the current best point.

			  if (cnt == 0)
			  {
			  	reduceRayAccuracyTolerances();
			  	dtt[0] = dtt[1] = dtt[2] = 1.0;
			  	cnt = 1;
			  }
			  else
		  	  amoeba.redefineSimplexPoints();
		  }
  		dtt[0] /= cnt;
	  	dtt[1] /= cnt;
		  dtt[2] /= cnt;
		}
		else if (amoeba.travelTime[0] == Globals.NA_VALUE)
		{
			// this is called once to initialize point 0 at the beginning of the
			// simplex calculation

		  estimateSimplexGradient(lastTT, dtt, 0);
		}
		else if (amoeba.travelTime[1] == Globals.NA_VALUE)
		{
			// this is called once to initialize point 1 at the beginning of the
			// simplex calculation

		  estimateSimplexGradient(lastTT, dtt, 1);
		}
		else if (amoeba.travelTime[2] == Globals.NA_VALUE)
		{
			// this is called once to initialize point 2 at the beginning of the
			// simplex calculation

		  estimateSimplexGradient(lastTT, dtt, 2);
		}

		// return the gradient

		return Math.sqrt(dtt[0] * dtt[0] + dtt[1] * dtt[1] + dtt[2] * dtt[2]);
		//return (dtt[0] * dtt[0] + dtt[1] * dtt[1] + dtt[2] * dtt[2] +
		//		    amoeba.currentSnellsLawMisfit * amoeba.currentSnellsLawMisfit);
	}

	protected void redefineSimplex() throws Exception
	{
		// newPos 0,1
		//   let inode = intermediate node between pos0 and pos1 at .25 the distance from 0 to 1
		//   let n     = pos0 x pos1 normalized
		//   let ang   = angle between pos0 and inode
		//   let newpos0 = rotate inode in direction of n by ang
		//   let newpos1 = rotate inode in direction of n by -ang
		//   let newpos2 = intermediate node between pos0 and pos1 at -.25 the distance from 0 to 1
		//   calculate travel times and set simplexPos, p, travelTime, snellsLawMisfit for all nodes
		//   calculate gradient to each new node using old pos0 results

		// save the old position 0 and 1 and the position 0 travel time

		double          tt0  = amoeba.travelTime[0];
		GeoTessPosition pos0 = amoeba.simplexPos[0];
		GeoTessPosition pos1 = amoeba.simplexPos[1];

		// calculate the normal of the plane containing position 1 and 0 and
		// oriented as pos1 crossing into pos0

    double[] n = {0.0, 0.0, 0.0};
    Vector3D.crossNormal(pos1.getVector(), pos0.getVector(), n);

    // get the position of a node (inode) that is located 25% of the way from
    // position 0 toward position 1 on the bouncepoint layer boundary

    double[] pos = {0.0, 0.0, 0.0};
		//GeoTessPosition inode = pos0.deepClone();
		GeoTessPosition inode = GeoTessPosition.getGeoTessPosition(pos0);
		inode.setIntermediatePosition(pos0, pos1, .25, bpLayer);

		// get 25% of the angle from position 0 to position 1, and rotate inode
		// toward n by that angle. Save the result in pos

		double ang01 = 0.25 * VectorUnit.angle(pos1.getVector(), pos0.getVector());
    Vector3D.rotateVector(inode.getVector(), n, ang01, pos);

    // make a new simplex point 0 from the position vector saved in pos

    //GeoTessPosition newPos0 = pos0.deepClone();
		GeoTessPosition newPos0 = GeoTessPosition.getGeoTessPosition(pos0);
    newPos0.setTop(bpLayer, pos);

    // rotate inode toward n by -ang01 and save the result in pos

    Vector3D.rotateVector(inode.getVector(), n, -ang01, pos);

    // make a new simplex point 1 from the position vector saved in pos

    //GeoTessPosition newPos1 = pos0.deepClone();
		GeoTessPosition newPos1 = GeoTessPosition.getGeoTessPosition(pos0);
    newPos1.setTop(bpLayer, pos);

    // now set inode to a position that is 1/4 the distance from pos0 as before
    // but in the opposite direction. Save that position as the new simplex
    // point 2.

    inode.setIntermediatePosition(pos0, pos1, -.25, bpLayer);
    GeoTessPosition newPos2 = inode;

    // reduce the ray accuracy tolerances

    reduceRayAccuracyTolerances();

    // redefine the simplex at the new positions.

    redefineSimplexPoint(newPos0, pos0, tt0, 0);
    redefineSimplexPoint(newPos1, pos0, tt0, 1);
    redefineSimplexPoint(newPos2, pos0, tt0, 2);
	}

	private void redefineSimplexPoint(GeoTessPosition newPos,
																		GeoTessPosition oldPos0,
																		double oldPos0TT, int i) throws Exception
	{
		// calculate the ray at the new bounce point (newPos)

    calculateBouncePointRay(newPos, oldPos0);

    // Set the result into the simplex position index = i

    amoeba.simplexPos[i] = newPos;
    amoeba.p[i][0] = newPos.getLatitude();
    amoeba.p[i][1] = newPos.getLongitude();
		amoeba.travelTime[i] = bpRay.getTravelTime();
		amoeba.snellsLawMisfit[i] = bpRay.getBouncePointFitness(0);

//		if (amoeba.swapSnellsLaw)
//			amoeba.y[i] = amoeba.snellsLawMisfit[i];
//		else
		{
			// calculate the magnitude of the gradient and its direction vector
	
			double[] lpq = Vector3D.subtract(newPos.get3DVector(), oldPos0.get3DVector());
			double L = Vector3D.normalize(lpq);
	    double gmag = (amoeba.travelTime[i] - oldPos0TT) / L;
	
	    // find the local orthogonal coordinate system centered on newPos and
	    // aligned with the source-receiver great circle and its normal ... then
	    // calculate the gradient
	
	    bpOrthogonalSet(newPos);
			double g0 = gmag * VectorUnit.dot(lpq, nBPSet[0]);
			double g1 = gmag * VectorUnit.dot(lpq, nBPSet[1]);
			double g2 = gmag * VectorUnit.dot(lpq, nBPSet[2]);
	
			// get the gradient magnitude and set it into amoeba index = i
	
			amoeba.y[i] = Math.sqrt(g0*g0 + g1*g1 + g2*g2);
		}
	}

	private double estimateSimplexGradient(double tt, double[] g, int i)
	{
		// if amoeba.travelTime[i] == Globals.NA then must use lastBPPosition
		// instead of amoeba.simplexPos[i], and nextToLastTT instead of 
		// amoeba.travelTime[i].
		// This means that this is one of the first three calls to simplexFunction
		// where the currentBPPosition is a simplex point and the nextToLastTT and
		// lastBPPosition have been set to the final Brents travel time and location.
		// These are used to intialize the gradient at each simplex point.

		double ttl = amoeba.travelTime[i];
		GeoTessPosition qi = amoeba.simplexPos[i];
		if (ttl == Globals.NA_VALUE)
		{
			ttl = nextToLastTT;
			qi  = lastBPPosition;
		}

		// get the vector (lpq) and its length (L) from qi to the currentBPPosition.
		double[] lpq = Vector3D.subtract(currentBPPosition.get3DVector(), qi.get3DVector());
		double L = Vector3D.normalize(lpq);
    if (L < minBPMove / 4.0)
    	return 0;

    // calculate the magnitude of the gradient
		double gmag = (tt - ttl) / L;
		
		// Dot lpq into each orthognal component about the current BP posiiton and
		// scale by gmag to obtain the gradient components in nx, ny, and nz. These
		// are added to the current contents of g and then averaged in
		// simplexFunction on return.
		g[0] += gmag * VectorUnit.dot(lpq, nBPSet[0]);
		g[1] += gmag * VectorUnit.dot(lpq, nBPSet[1]);
		g[2] += gmag * VectorUnit.dot(lpq, nBPSet[2]);
		return 1;
	}

	private void bpOrthogonalSet(GeoTessPosition pos)
	{
		// determine the orthogonal system nx, ny, nz where nz is the normal vector
		// along the current BP position, nx is the vector orthogonal to the plane
		// containing ngc crossed into the current BP position, and ny is the
		// plane that contains ngc formed by crossing nz into nx.
		nBPSet[2][0] = pos.getVector()[0];
		nBPSet[2][1] = pos.getVector()[1];
		nBPSet[2][2] = pos.getVector()[2];
		VectorUnit.crossNormal(ngc, nBPSet[2], nBPSet[0]);
		VectorUnit.crossNormal(nBPSet[2],  nBPSet[0], nBPSet[1]);
	}

//	
//	/**
//	 * Simplex method function that evaluates the travel time of a ray with one or
//	 * more Bottom-Side Reflections (BSRs), where the BSR lat, lon values are
//	 * given in the input vector x. The size of x is 2 * the number of BSRs.
//	 * 
//	 * The travel time is represented as a minimum by returning 1 - tt / 10000.0
//	 * so that the largest vale of tt returns the smallest result.
//	 */
//	@Override
//	public double simplexFunction(double[] x) throws Exception
//	{
//		// increment the simplex function call count and set the BSR lat lon vector
//		// to the contents of the input x vector.
//		//simplexFunctionMethodCallCount++;
//		for (int i = 0; i < x.length; ++i) undersideReflectionLatLonvector[i] = x[i];
//
////		// get unit vectors of simplex_p
////		// calculate mean unit vector
////		// calculate angle between mean and each unit_vector
////		// output angles, and mean angle
////		
////		double[] uv  = {0.0, 0.0, 0.0};
////		double[] muv = {0.0, 0.0, 0.0};
////		for (int i = 0; i < simplex_p.length; ++i)
////		{
////			getGeoTessModel().getEarthShape().getVector(simplex_p[i][0], simplex_p[i][1], uv);
////			Vector3D.increment(muv, uv);
////		}
////    VectorUnit.normalize(muv);
////    double meanAng = 0.0;
////    String s = "Simplex Angles = ";
////    
////		for (int i = 0; i < simplex_p.length; ++i)
////		{
////			getGeoTessModel().getEarthShape().getVector(simplex_p[i][0], simplex_p[i][1], uv);
////			double ang = Math.toDegrees(VectorUnit.angle(muv,  uv));
////			s += String.format("%8.3f, ", ang);
////			meanAng += ang;
////		}
////    s += String.format("Mean = %8.3f", meanAng/simplex_p.length);
////    System.out.println(s);
//
//		// calculate ray associated with the BSR inpt vector x and get its travel time
//
//		Ray ray = new Ray(this, getReceiverProfile(), getSourceProfile());
//	  double tt = ray.getTravelTime();
//	  simplexRayMap.put(x, ray);
//
//	  fastRayTravelTime = tt;
//		fastRay = ray;
//
//		if (getVerbosity() > 0)
//		{
//		  println("BSROPT.F    Input Position BSR Input Ray Travel Time (sec) = " +
//		  		    tt);
//		  println();
//		}
//
//	  // Create empty references and a dummy Ray (derivRay) to use for
//	  // computing derivatives. Loop over all bottom-side reflections and
//	  // calculate latitude and longitude derivatives. Only the branch prior to
//	  // and following the current BSR (i) is computed in the derivative
//	  // evaluation. All other branches (if any) are not affected by the
//	  // change in the ith BSR position, and hence are not computed.
//
//		// Derivatives are evaluated (central difference) for each Bottom-Side
//		// Reflection (BSR) in addition to the travel time at the input BSR vector.
//
//		double del_lat = Math.toRadians(0.025);
//		double del_lon = Math.toRadians(0.025);
//
//	  // get total derivative
//
//	  double dtt = totalCentralDerivative(x, del_lat, del_lon);
//	  dtt += totalCentralDerivative(x, 2.0 * del_lat, 2.0 * del_lon) / 2.0;
//	  dtt /= 2.0;
//	  //double dtt = totalForwardDerivative(x, tt, del_lat, del_lon);
//	  //double dtt = totalAvgForwardDerivative(x, tt, del_lat, del_lon);
//
//	  // get total derivative (fitness)
//
//	  double fitness = Math.sqrt(dtt);
//	  if (getVerbosity() > 0)
//		{
//	    println("BSROPT.F    Total Travel Time Derivative (fittness) for " +
//	            "Input BSR Vector = " + dtt);
//		  println();
//		}
//
//  	// done ... return fitness
//		return fitness;
//	}
//
//	private boolean isSimplexAcrossLayer()
//	{
//		if ((simplexRayMap != null) && (simplexRayMap.size() == undersideReflectionLatLonvector.length))
//		{
//			Ray first = null, prev = null, next = null;
//			for (Map.Entry<double[], Ray> entry: simplexRayMap.entrySet())
//			{
//				prev = next;
//				next = entry.getValue();
//				if (first == null) first = next;
//				if (prev != null)
//				{
//					if (!Ray.areBottomLayersEquivalent(prev, next)) return true;
//				}
//			}
//			if (!Ray.areBottomLayersEquivalent(next, first)) return true;
//			
//		}
//		
//		return false;
//	}
//
//	private double bsrCentralDerivative(int i, double del) throws Exception
//	{
//    return (bsrOffsetTravelTime(i, del) - bsrOffsetTravelTime(i, -del)) / 2.0 / del;
//	}
//
//	private double bsrForwardDerivative(int i, double tt, double del) throws Exception
//	{
//    return (bsrOffsetTravelTime(i, del) - tt) / del;
//	}
//
//	/**
//	 * Increments the BSR component 'i' by del, evaluates the ray, and returns the
//	 * travel time. All other components of the BSR vector
//	 * 
//	 * 			undersideReflectionLatLonvector
//	 * 
//	 * are not modified. Component i is reset to it's entry value on exit.
//	 * 
//	 * @param i   The ith BSR component stored inthe vector
//	 * 						undersideReflectionLatLonvector.
//	 * @param del The amount by which the ith BSR component is incremented.
//	 * 
//	 * @return The travel time at the incremented component position.
//	 * @throws Exception
//	 */
//	private double bsrOffsetTravelTime(int i, double del) throws Exception
//	{
//		double c = undersideReflectionLatLonvector[i];
//		undersideReflectionLatLonvector[i] = c + del;
//		Ray ray = new Ray(this, getReceiverProfile(), getSourceProfile());
//		undersideReflectionLatLonvector[i] = c;
//	  return ray.getTravelTime();
//	}
//
//	private double totalCentralDerivative(double[] x, double del_lat, double del_lon) throws Exception
//	{
//		double dtt = 0.0, dlat = 0.0, dlon = 0.0;
//
//	  for (int i = 0; i < x.length / 2; i += 2)
//	  {
//			if (getVerbosity() > 0)
//			{
//			  println("BSROPT.F    Solve for Bottom-Side Reflection Derivative " + i + " ...");
//        println();
//			}
//
//			dlat = bsrCentralDerivative(i, del_lat);
//	
//		  if (getVerbosity() > 0)
//			{
//		    println("BSROPT.F    Latitude Derivative Travel Time Difference for " +
//		            "BSR " + i/2 + "  = " + dlat);
//			  println();
//			}
//	
//			// calculate ray travel time for lon+del_lon and subtract ray travel time
//			// for ray lon-del_lon (central difference) ... reset under side reflection
//			// back to input longitude (x[i+1]) when complete
//
//			dlon = bsrCentralDerivative(i+1, del_lon);
//	
//		  if (getVerbosity() > 0)
//			{
//		    println("BSROPT.F    Longitude Derivative Travel Time Difference for " +
//		            "BSR " + i/2 + "  = " + dlat);
//			  println();
//			}
//	
//			// add square of dlat and dlon to total derivative (dtt)
//			
//		  dtt += dlat * dlat + dlon * dlon;
//		}
//	  
//		return dtt;
//	}
//	
//	private double totalForwardDerivative(double[] x, double tt, double del_lat, double del_lon) throws Exception
//	{
//		double dtt = 0.0, dlat = 0.0, dlon = 0.0;
//
//	  for (int i = 0; i < x.length / 2; i += 2)
//	  {
//			if (getVerbosity() > 0)
//			{
//			  println("BSROPT.F    Solve for Bottom-Side Reflection Derivative " + i + " ...");
//        println();
//			}
//
//			dlat = bsrForwardDerivative(i, tt, del_lat);
//	
//		  if (getVerbosity() > 0)
//			{
//		    println("BSROPT.F    Latitude Derivative Travel Time Difference for " +
//		            "BSR " + i/2 + "  = " + dlat);
//			  println();
//			}
//	
//			// calculate ray travel time for lon+del_lon and subtract ray travel time
//			// for ray lon-del_lon (central difference) ... reset under side reflection
//			// back to input longitude (x[i+1]) when complete
//
//			dlon = bsrForwardDerivative(i+1, tt, del_lon);
//	
//		  if (getVerbosity() > 0)
//			{
//		    println("BSROPT.F    Longitude Derivative Travel Time Difference for " +
//		            "BSR " + i/2 + "  = " + dlat);
//			  println();
//			}
//	
//			// add square of dlat and dlon to total derivative (dtt)
//			
//		  dtt += dlat * dlat + dlon * dlon;
//		}
//		return dtt;
//	}
//	
//	private double totalAvgForwardDerivative(double[] x, double tt, double del_lat, double del_lon) throws Exception
//	{
//		double dtt = 0.0, dlat = 0.0, dlon = 0.0;
//
//	  for (int i = 0; i < x.length / 2; i += 2)
//	  {
//			if (getVerbosity() > 0)
//			{
//			  println("BSROPT.F    Solve for Bottom-Side Reflection Derivative " + i + " ...");
//        println();
//			}
//
//			dlat  = bsrForwardDerivative(i, tt, del_lat);
//			dlat += bsrForwardDerivative(i, tt, 2.0 * del_lat);
//			dlat /= 2.0;
//	
//		  if (getVerbosity() > 0)
//			{
//		    println("BSROPT.F    Latitude Derivative Travel Time Difference for " +
//		            "BSR " + i/2 + "  = " + dlat);
//			  println();
//			}
//	
//			// calculate ray travel time for lon+del_lon and subtract ray travel time
//			// for ray lon-del_lon (central difference) ... reset under side reflection
//			// back to input longitude (x[i+1]) when complete
//
//			dlon  = bsrForwardDerivative(i+1, tt, del_lon);
//			dlon += bsrForwardDerivative(i+1, tt, 2.0 * del_lon);
//			dlon /= 2.0;
//	
//		  if (getVerbosity() > 0)
//			{
//		    println("BSROPT.F    Longitude Derivative Travel Time Difference for " +
//		            "BSR " + i/2 + "  = " + dlat);
//			  println();
//			}
//	
//			// add square of dlat and dlon to total derivative (dtt)
//			
//		  dtt += dlat * dlat + dlon * dlon;
//		}
//		return dtt;
//	}
//
//	private void alternatingBrents(GeoVector source, GeoVector receiver) throws Exception
//	{
//	  undersideReflectionLatLonvector = new double [2*phaseRayBranchModel.getUndersideReflectionCount()];
//		TauPPhaseBottomSideReflection bsrPoints = TauPPhaseBottomSideReflection.
//				               												getTauPPhaseBSR(phaseRayBranchModel.getSeismicPhase());
//		double[] z = {0.0, 0.0, 0.0};
//		if (bsrPoints == null)
//		{
//		  // set bottom side vector from the Phase-Ray Branch Model initial guess.
//		  for (int i = 0; i < phaseRayBranchModel.getUndersideReflectionCount(); ++i)
//		  {
//		  	int uri = phaseRayBranchModel.getBMIndexFromBSRIndex(i);
//				double f = phaseRayBranchModel.getFixedReflectionInitialAngleFraction(uri);
//				VectorUnit.rotatePlane(source.getUnitVector(), receiver.getUnitVector(), f, z);
//				undersideReflectionLatLonvector[2*i]   = getGeoTessModel().getEarthShape().getLat(z);
//				undersideReflectionLatLonvector[2*i+1] = getGeoTessModel().getEarthShape().getLon(z);
//		  }
//		}
//		else
//		{
//			double f = bsrPoints.getBSRPoint(currentSourceProfile.getDepth(),
//																			 sourceToReceiverDistance) /
//																			 sourceToReceiverDistance;
//			VectorUnit.rotatePlane(source.getUnitVector(), receiver.getUnitVector(), f, z);
//			undersideReflectionLatLonvector[0] = getGeoTessModel().getEarthShape().getLat(z);
//			undersideReflectionLatLonvector[1] = getGeoTessModel().getEarthShape().getLon(z);
//		}
//		
//	  // Output initial bottom-side reflection vector
//	  //*** undersideReflectionLatLonvector[1] = Math.toRadians(22.974);
//		if (getVerbosity() > 0)
//		{
//			println();
//			println("BSROPT  SIMPLEX BSR Optimization Start " + Globals.repeat("*", 118));
//			print("BSROPT    Initial Bottom-Side Vector (lat, lon ... in deg) = ");
//		  for (int i = 0; i < undersideReflectionLatLonvector.length; ++i)
//		  {
//		  	print(String.format("%8.3f", Math.toDegrees(undersideReflectionLatLonvector[i])));
//		  	if (i < undersideReflectionLatLonvector.length-1)
//		  			print(", ");
//		  }
//		  println(); println();
//		}
//
//	  // set initial counters and Simplex method
//	  updateFromUndersideReflectionLatLonvector = true;
//
//	  // use brents to alternate between parallel and transverse directions to
//		// find the maximum/minimum travel time in each direction.
//		
//		// parallel direction is a greatCircle between source and receiver.
//		// transverse direction is a greatCircle passing through the current
//		// parallel maximum and whose normal is rotated by 90 degrees from the
//		// parallel greatCircle
//		
//		// given travel time at the input guess and some distance pre- and post- of
//		// that position along parallel GC.
//		//   Use Brents to find maximum travel time.
//		//   Save parallel position.
//		//   calculate transverse GC
//		//   set min and max away from current point on transverse GC
//		//   Solve Brents to find minimum travel time.
//		//   save transverse position.
//		//   if transverse position 3d distance to parallel position < tolerance
//		//     done.
//		//
//		//   Otherwise,
//		//     calculate new parallel GC passing through transverse minimum location
//		//     and rotated from old parallel maximum to new transverse minimum.
//		//   loop to continue
//		
//		// Parallel GC normal is S x R (pN)
//		//   input initial guess for likely saddle point (pMax)
//		//   input initial parallel and transverse step size (pStep, tStep) to
//		//   bracket minimum.
//		//
//		//   Begin:
//		//      rotate pMax about pN by +pStep (pLast) and -pStep (pFirst)
//		//      Enter brents with pFirst, pMax, pLast to find new pMax
//		//      
//		//      calculate transverse normal (tN) by rotating pN by 90 degrees
//		//      about the axis pMax.
//		//      set tMin = pMax
//		//      rotate tMin about tN by +tStep (tLast) and -tStep (tFirst)
//		//      Enter brents with tFirst, tMin, tLast to find new tMin
//		//
//		//      if 3d distance between tMin and pMax < tolerance
//		//        done ... return
//		//
//		//      rotate pN an angle from pMax to tMin about tN
//		//      cut pStep and tStep in half
//		//      set pMax = tMin
//		//      Loop
//		
//		double[] pN = {0.0, 0.0, 0.0};
//		double[] pMax = {0.0, 0.0, 0.0};
//		double[] tMin = {0.0, 0.0, 0.0};
//		double[] tN = {0.0, 0.0, 0.0};
//		
//		VectorUnit.crossNormal(source.getUnitVector(), receiver.getUnitVector(), pN);
//		getGeoTessModel().getEarthShape().getVector(undersideReflectionLatLonvector[0],
//																								undersideReflectionLatLonvector[1],
//																								pMax);
//    double dist = source.distance(receiver);
//    double pStep = dist * .05;
//    double tStep = dist * .01;
//    
//    Brents brents = new Brents();
//    BrentsTravelTimeExtrema ttExtrema = new BrentsTravelTimeExtrema();
//    
//		double tol = 1.0e-5;
//		double tt = 0.0;
//		double[] tmp = {0.0, 0.0, 0.0};
//    while (true)
//    {
//    	// initialize the extremum searcher along a GC with normal pN and centered
//    	// at the position pMax searching from pA to pB where pA is along the GC
//    	// at an angle pStep before pMax and pB is along the GC at an angle pStep
//    	// after pMax. Then perform the maximum search to find the ray with
//    	// the maximum travel time between pA and pB along the GC. Save that
//    	// position as the new pMax
//
//    	ttExtrema.initialize(pMax, pN, pStep, "BSROPT.F");
//      tt = brents.maxF(0.0,  1.0, ttExtrema);
//      pMax[0] = ttExtrema.getExtremumPosition()[0];
//      pMax[1] = ttExtrema.getExtremumPosition()[1];
//      pMax[2] = ttExtrema.getExtremumPosition()[2];
//      
//      // cross pMax into pN to get the transverse normal tN
//
//      VectorUnit.crossNormal(pMax, pN, tN);
//
//      // initialize the extremum searcher along a GC with normal tN and centered
//    	// at the position pMax searching from pA to pB where pA is along the GC
//    	// at an angle tStep before pMax and pB is along the GC at an angle tStep
//    	// after pMax. Then perform the minimum search to find the ray with
//    	// the minimum travel time between pA and pB along the GC. Save that
//    	// position as the new tMin
//
//    	ttExtrema.initialize(pMax, tN, tStep, "BSROPT.F");
//      tt = brents.minF(0.0,  1.0, ttExtrema);
//      tMin[0] = ttExtrema.getExtremumPosition()[0];
//      tMin[1] = ttExtrema.getExtremumPosition()[1];
//      tMin[2] = ttExtrema.getExtremumPosition()[2];
//
//      // exit if the distance between pMax and tMin is less than the tolerance.
//
//      double ang = VectorUnit.angle(pMax, tMin);
//      if (Math.abs(ang) < tol) break;
//
//      // rotate pN toward -pMax by the angle between pMax and tMin and use that
//      // as the new pN
//
//      Vector3D.negate(pMax);
//      ang = acos(VectorUnit.dot(pMax, tMin));
//			VectorUnit.rotateVector(pN, pMax, ang, pN);
//      
//			// half the step sizes and set pMax to tMin ... continue
//
//      pStep /= 2.0;
//      tStep /= 2.0;
//
//      pMax[0] = tMin[0];
//      pMax[1] = tMin[1];
//      pMax[2] = tMin[2];
//    }
//
//    // done ... save the fast ray and exit
//
//    fastRay = ttExtrema.getExtremumRay();
//	}
//
//	private class BrentsTravelTimeExtrema implements BrentsFunction
//	{
//		private Ray rayExt    = null;
//		private double[] pA   = {0.0, 0.0, 0.0};
//		private double[] pB   = {0.0, 0.0, 0.0};
//		private double[] pY   = {0.0, 0.0, 0.0};
//		private double[] pExt = null;
//
//		private double   pABDist = 0.0;
//		private String   bHdr = "";
//
//		public BrentsTravelTimeExtrema()
//		{
//			
//		}
//
//		public double[] getExtremumPosition()
//		{
//			return pExt;
//		}
//
//		public Ray getExtremumRay()
//		{
//			return rayExt;
//		}
//
//		/**
//		 * Sets up the path from pA to pB along the GC whose normal is n. The input
//		 * position p is located at 1/2 the distance between pA and pB. The angular
//		 * distance between p to pA and p to pB is step.
//		 * 
//		 * @param p    The input position that will be located 1/2 way between the
//		 *             GC limits of pA and pB.
//		 * @param n    The normal of the GC containin the vectors p, pA, and pB.
//		 * @param step The distance between p and pA, and p and pB.
//		 */
//		public void initialize(double[] p, double[] n, double step, String hdr)
//		{
//			pExt = p.clone();
//			
//			// pA = pExt rotated by -step about n
//			double[] y = {0.0, 0.0, 0.0};
//			VectorUnit.crossNormal(n, pExt, y);
//			VectorUnit.rotateVector(pExt, y,  step, pB);
//			VectorUnit.rotateVector(pExt, y, -step, pA);
//			
//			VectorUnit.vectorTripleProduct(pA, pB, pA, pY);
//			VectorUnit.normalize(pY);
//	
//			pABDist = acos(VectorUnit.dot(pA, pB));
//			bHdr = hdr;
//		}
//
//		@Override
//		public double bFunc(double x) throws Exception
//		{
//			VectorUnit.rotateVector(pA, pY, x * pABDist, pExt);
//			rayExt = bsrTravelTime(0, pExt);
//			return rayExt.getTravelTime();
//		}
//	}
//
//	private void getAlternatingBrentsPosition(double f, double[] n, double[] first, double a, double[] p)
//	{
//		// n is first x last
//		// f is fractional vector from first (f=0) to last (f=1)
//		
//		// a is the angle between first and last
//		// if f = 0 then p = first
//		// if f = 1 then p = last
//		VectorUnit.rotate(first,  n,  f * a, p);
//	}
//
//	/**
//	 * Calculates a ray using the BSR position settings for all entries except the
//	 * ith entry. That entry has its position changed to the lat/lon of the input
//	 * unit vector p. The ray is calculated and the original contents of the ith
//	 * position are reset. The travel time is returned.
//	 * 
//	 * @param i    = The ith BSR entry of the undersideReflectionLatLonVector
//	 *               whose lat/lon will be changed to that of the input unit
//	 *               vector p.
//	 * @param p    = The unit vector whose lat/lon will be set into the ith
//	 *               position of the undersideReflectionLatLonVector.
//	 * 
//	 * @return The travel time of the calculated ray.
//	 * @throws Exception
//	 */
//	private Ray bsrTravelTime(int i, double[] p) throws Exception
//	{
//		int latI = 2 * i;
//		int lonI = latI + 1;
//		double lat = undersideReflectionLatLonvector[latI];
//		double lon = undersideReflectionLatLonvector[lonI];
//		undersideReflectionLatLonvector[latI] = getGeoTessModel().getEarthShape().getLat(p);;
//		undersideReflectionLatLonvector[lonI] = getGeoTessModel().getEarthShape().getLon(p);;
//		Ray ray = new Ray(this, getReceiverProfile(), getSourceProfile());
//		undersideReflectionLatLonvector[latI] = lat;
//		undersideReflectionLatLonvector[lonI] = lon;
//	  return ray;
//	}

	/**
	 * Static method that extracts all bender properties from the input
	 * PropetiesPlus object and makes a new PropertiesPlusGMP object containing
	 * just he extracted Bender properties. The input propertiesPlus object is
	 * unchanged.
	 * 
	 * @param properties The input propertiesPlus object from which Bender
	 *                   properties are extracted.
	 * @return A new PropertiesPlusGMP object containing only Bender properties.
	 */
	public static PropertiesPlusGMP getBenderProperties(PropertiesPlus properties)
	{
		PropertiesPlusGMP benderProps = new PropertiesPlusGMP();

		copyProperties(benderProps, properties, PROP_PREDICTORS);
		copyProperties(benderProps, properties, PROP_MODEL_LAYER_TO_EARTH_IFACE_MAP);
		copyProperties(benderProps, properties, PROP_VERBOSITY);
		copyProperties(benderProps, properties, PROP_GRADIENT_CALCULATOR);
		copyProperties(benderProps, properties, PROP_USE_TT_SITE_CORRECTIONS);
		copyProperties(benderProps, properties, PROP_USE_TT_SITE_CORRECTIONS_TRUE);
		copyProperties(benderProps, properties, PROP_USE_TT_SITE_CORRECTIONS_FALSE);
		copyProperties(benderProps, properties, PROP_TET_SIZE);
		copyProperties(benderProps, properties, PROP_ELLIPTICITY_CORR_DIR);
		copyProperties(benderProps, properties, "benderAllowICBDiffraction");
		copyProperties(benderProps, properties, "benderAllowCMBDiffraction");
		copyProperties(benderProps, properties, "benderAllowMOHODiffraction");
		copyProperties(benderProps, properties, PROP_TT_MODEL_UNCERTAINTY_SCALE);
		copyProperties(benderProps, properties, PROP_DEFAULT_TT_TOL);
		copyProperties(benderProps, properties, PROP_DEFAULT_MIN_NODE_SPACING);
		copyProperties(benderProps, properties, PROP_DEFAULT_CONVERGENCE_CRITERIA);
		copyProperties(benderProps, properties, PROP_PHASE_SPECIFIC_CONV_CRIT);
		copyProperties(benderProps, properties, PROP_PHASE_LEVEL_THICKNESS);
		copyProperties(benderProps, properties, PROP_SEARCH_METHOD);
		copyProperties(benderProps, properties, PROP_MAX_CALC_TIME);
		copyProperties(benderProps, properties, PROP_MODEL);
		copyProperties(benderProps, properties, PROP_TAUP_TK_MODEL);

		return benderProps;
	}

	/**
	 * Static method that finds the input property "prop" in the propPlus
	 * properties file object and adds it to the propPlusGMP properties object.
	 * 
	 * @param propPlusGMP The object into which the property "prop" is added.
	 * @param propPlus    The properties object from which the property "prop" is
	 *                    extracted.
	 * @param prop        The input property to be extracted.
	 */
  private static void copyProperties(PropertiesPlusGMP propPlusGMP,
  		                          		 PropertiesPlus propPlus, String prop)
  {
    if (propPlus.getProperty(prop) != null)
    	propPlusGMP.put(prop, propPlus.getProperty(prop));  	
  }

  /**
   * Retrieve the GeoTessModel that is specified in the properties file with 
   * property <prefix>Model.  For example, if the properties file specified when
   * this PredictorFactory was constructed contains a property benderModel = xxx,
   * then the GeoTessModel that was specified will be returned.  
   * @param prefix
   * @return
   * @throws GMPException
   * @throws IOException
   */
  static public GeoTessModel getGeoTessModel(File modelFile) throws Exception
  {
	  if (modelFile == null)
		  throw new GMPException(" Property 'benderModel' is not specified in the properties file.");

	  byte attr = GlobalInputStreamProvider.forFiles().getMetadata(modelFile);
	  
	  if (!FileAttributes.EXISTS.test(attr))
		  throw new GMPException(" Property 'benderModel' specifies a File that does not exist:\""
				  + modelFile+"\" GlobalInputStreamProvider.forFiles() = "+
		      GlobalInputStreamProvider.forFiles()+", host = "+InetAddress.getLocalHost());

	  if (FileAttributes.IS_DIRECTORY.test(attr) && GlobalInputStreamProvider.forFiles().isFile(
	        new File(modelFile, "prediction_model.geotess")))
		  modelFile = (new File(modelFile, "prediction_model.geotess"));

	  String modelFileName = modelFile.getPath();

	  GeoTessModel model;
	  synchronized(geotessModels) {    
	    model = geotessModels.get(modelFileName);
	    
	    if(model != null) return model;
        
	    Exception x = null;
	    for (int ecnt = 0; ecnt < 1; ++ecnt) {
          try {
            model = GeoTessModel.getGeoTessModel(modelFile);
            
            if(model != null) {
              geotessModels.put(modelFileName, model);
              return model;
            }
          } catch (Exception ex) {
            // unsuccessful ... wait 5 seconds and try again
            x = ex;
            try {
              Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
          }
        }

	    String message = "Failed to read in bendermodel at \""+modelFileName+"\"";
	    if(x != null) throw x;
        throw new Exception(message);
	  }
  }
  
  @Property(type = PredictorType.class) 
  public static final String PROP_PREDICTORS = "predictors";
  @Property public static final String PROP_MODEL_LAYER_TO_EARTH_IFACE_MAP = "benderModelLayerToEarthInterfaceMap";
  @Property public static final String PROP_VERBOSITY = "predictorVerbosity";
  @Property public static final String PROP_GRADIENT_CALCULATOR = "benderGradientCalculator";
  @Property (type = Boolean.class)
  public static final String PROP_USE_TT_SITE_CORRECTIONS = "benderUseTTSiteCorrections";
  @Property (type = Void.class, desc = "No arguments")
  public static final String PROP_USE_TT_SITE_CORRECTIONS_TRUE = "benderUseTTSiteCorrectionsTrue";
  @Property (type = Void.class, desc = "No arguments")
  public static final String PROP_USE_TT_SITE_CORRECTIONS_FALSE = "benderUseTTSiteCorrectionsFalse";
  @Property public static final String PROP_TET_SIZE = "benderTetSize";
  @Property public static final String PROP_ELLIPTICITY_CORR_DIR = "benderEllipticityCorrectionsDirectory";
//  @Property (type = Boolean.class)
//  public static final String PROP_ALLOW_ICB_DIFF = "benderAllowICBDiffraction";
//  @Property (type = Boolean.class)
//  public static final String PROP_ALLOW_CMB_DIFF = "benderAllowCMBDiffraction";
//  @Property (type = Boolean.class)
//  public static final String PROP_ALLOW_MOHO_DIFF = "benderAllowMOHODiffraction";
  @Property public static final String PROP_TT_MODEL_UNCERTAINTY_SCALE = "benderTTModelUncertaintyScale";
  @Property public static final String PROP_DEFAULT_TT_TOL = "benderDefaultTravelTimeTolerance";
  @Property public static final String PROP_DEFAULT_MIN_NODE_SPACING = "benderDefaultMinimumNodeSpacing";
  @Property public static final String PROP_DEFAULT_CONVERGENCE_CRITERIA = "benderDefaultConvergenceCriteria";
  @Property public static final String PROP_PHASE_SPECIFIC_CONV_CRIT = "benderPhaseSpecificConvergenceCriteria";
  @Property public static final String PROP_PHASE_LEVEL_THICKNESS = "benderPhaseLevelThickness";
  @Property public static final String PROP_SEARCH_METHOD = "benderSearchMethod";
  @Property public static final String PROP_MAX_CALC_TIME = "benderMaxCalcTime";
  @Property public static final String PROP_MODEL = "benderModel";
  @Property (type = File.class)
  public static final String PROP_TAUP_TK_MODEL = "tauptoolkitModel";
  @Property public static final String PROP_MAX_PROCS = "maxProcessors";
  @Property (type = Boolean.class)
  public static final String PROP_PRECOMPUTE_GRADIENTS = "benderPrecomputeGradients";
  @Property public static final String PROP_UNCERTAINTY_TYPE = "benderTTUncertaintyType";
  @Property (type = File.class)
  public static final String PROP_UNCERTAINTY_DIR = "benderTTUncertaintyDirectory";
  @Property public static final String PROP_UNCERTAINTY_MODEL = "benderTTUncertaintyModel";
}
