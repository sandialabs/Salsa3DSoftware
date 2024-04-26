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
package gov.sandia.gmp.predictorfactory;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.extensions.siteterms.GeoTessModelSiteData;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.bender.Bender;
import gov.sandia.gmp.benderlibcorr3d.BenderLibCorr3D;
import gov.sandia.gmp.hydroradial2d.HydroRadial2D;
import gov.sandia.gmp.infrasoundpredictor.InfrasoundPredictor;
import gov.sandia.gmp.infrasoundradial2d.InfrasoundRadial2D;
import gov.sandia.gmp.lookupdz.LookupTablesGMP;
import gov.sandia.gmp.slbmwrapper.SLBMWrapper;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.io.GlobalInputStreamProvider;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * Utility to help manage Predictor objects such as Bender, SLBM, TaupToolkit, 
 * PGL, etc.  An application can construct a PredictorFactory object, passing it a PropertiesPlus
 * object and String indicating the name of the property that specifies the list of 
 * Predictors that are to be used.  For example, if property 
 * <BR>myFavoritePredictors = lookup2d, SLBM(Pn, Pg), bender(Pn, Sn)<BR>
 * then lookup2d will be be used for all phases not specified later in the list,
 * SLBM will be used for phase Pg and Bender will be used for phase Pn and Sn.  
 * 
 * <p>PredictorFactory is not thread-safe because it supports collections of Predictor
 * objects which are not thread-safe.
 * 
 * <p>A queue is implemented such that applications can add PredictionRequest objects
 * to the queue using addPredictionRequest(), which stores them by Predictor -> Set<PredictionRequest>
 * where the correct Predictor is chosen based on the SeismicPhase of the PredictionRequest.  Then
 * the application can call computePredictions which will compute the PredictionRequests in 
 * parallel, returning all the Prediction objects in an ArrayList.  Applications 
 * should remember to call clearPredictionRequestQueue() in order to clear the queue
 * after predictions have been retrieved.
 * 
 * @author sballar
 *
 */
public class PredictorFactory 
{
    public static final String PROP_LOG_ALL_REQUESTS = "predictorFactory.logPredictionRequests";
    public static final String LOG_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";
    
	public static String getVersion() 	{ 
		return Utils.getVersion("predictor-factory");
	}

	private EnumSet<PredictorType> supportedPredictors = EnumSet.of(
			PredictorType.AK135RAYS,
			PredictorType.BENDER, PredictorType.LOOKUP2D, PredictorType.BENDERLIBCORR3D,  
			PredictorType.SLBM, PredictorType.INFRASOUND, PredictorType.INFRASOUND_RADIAL2D, 
			PredictorType.HYDRO_RADIAL2D);
	
	/**
	 * Map from a SeismicPhase to the appropriate PredictorType object.
	 */
	private Map<SeismicPhase, PredictorType> phaseToPredictorType;
	
	private EnumSet<PredictorType> instantiatedPredictorTypes = EnumSet.noneOf(PredictorType.class);

	private PropertiesPlusGMP properties;
	
	private ScreenWriterOutput logger;

	private String name;
	
	private boolean logAllRequests;

	/**
	 * Default PredictorFactory implements the lookup2d predictor
	 * using the default seismicBaseData stored in the project/jar file.
	 * @throws Exception
	 */
	public PredictorFactory() throws Exception
	{
	    this.properties = new PropertiesPlusGMP();
	    this.properties.setProperty("earthShape", VectorGeo.getEarthShape().toString());
	    this.name = "predictors";
	    this.properties.setProperty(this.name, "lookup2d");
	    this.logAllRequests = false;
	    parsePredictorMap("predictors");
	}

	/**
	 * Constructor.  Instantiates and configures a set of Predictor objects
	 * for use by the calling application.
	 * @param properties
	 * @param propertyName  The name of the property in supplied properties object that
	 * identifies the list of Predictors that are to be instantiated.  For example, if 
	 * properties contains property with key myFavoritePredictors: 
	 * <BR>myFavoritePredictors = lookup2d, slbm(Pn, Pg), bender(Pn, Sn)<BR>
	 * then lookup2d will be be used for all phases not specified later in the list,
	 * SLBM will be used for phase Pg and Bender will be used for phase Pn and Sn.  
	 * @throws GMPException
	 * @throws IOException 
	 * @throws GeoTessException 
	 */
	public PredictorFactory(PropertiesPlusGMP properties, String propertyName) 
			throws Exception
	{ this(properties, propertyName, null);	}
	
	/**
	 * Constructor.  Instantiates and configures a set of Predictor objects
	 * for use by the calling application.
	 * @param properties
	 * @param propertyName  The name of the property in supplied properties object that
	 * identifies the list of Predictors that are to be instantiated.  For example, if 
	 * properties contains property with key myFavoritePredictors: 
	 * <BR>myFavoritePredictors = lookup2d, slbm(Pn, Pg), bender(Pn, Sn)<BR>
	 * then lookup2d will be be used for all phases not specified later in the list,
	 * SLBM will be used for phase Pg and Bender will be used for phase Pn and Sn.  
	 * @throws GMPException
	 * @throws IOException 
	 * @throws GeoTessException 
	 */
	public PredictorFactory(PropertiesPlusGMP properties, String propertyName, 
		    ScreenWriterOutput logger) throws Exception
		{
		    this.logger = logger;
			this.properties = properties;
			this.name = propertyName;
			this.logAllRequests = properties.getBoolean(PROP_LOG_ALL_REQUESTS, false);
			parsePredictorMap(propertyName);
		}
		
	public PredictorFactory(PropertiesPlusGMP properties, Map<SeismicPhase, PredictorType> predictors, 
		    ScreenWriterOutput logger) throws Exception
		{
		    this.logger = logger;
			this.properties = properties;
			this.name = "";
			this.logAllRequests = properties.getBoolean(PROP_LOG_ALL_REQUESTS, false);
			this.phaseToPredictorType = predictors;
		}
		
	/**
	 * Retrieve the PredictorType that is assigned to the specified 
	 * phase.  Can return null is no PredictorType was specified for the 
	 * specified phase.
	 * @param phase
	 * @return
	 */
	public PredictorType getPredictorType(SeismicPhase phase) {
		PredictorType t = phaseToPredictorType.get(phase);
		if (t == null)
			t = phaseToPredictorType.get(SeismicPhase.NULL);
		return t;
	}

	/**
	 * If the phase is one of the supported phases, then check to see 
	 * if the predictor for that phase has already been instantiated.
	 * If it has, then return a reference to it.  If it has not, instantiate
	 * a new instance, store a reference to it internally, and return 
	 * a reference. 
	 * 
	 *  <p>Caution: Many predictors are not thread-safe so only call this
	 *  method on PredictorFactory objects instantiated in a single task
	 *  of a multi-threaded application.
	 * 
	 * <p>If a new instance of a Predictor object is instantiated, and
	 * the Predictor uses a model of some sort, then that model will be
	 * loaded from file if necessary and a copy stored in a static map.  
	 * If the model has been loaded previously,
	 * then the new Predictor that is constructed will include a reference
	 * to the existing model (assumption: models are thread safe!).
	 * @param predictorType
	 * @return
	 * @throws Exception
	 */
	public Predictor getPredictor(SeismicPhase phase) throws Exception {
		return getPredictor(getPredictorType(phase));
	}

	/**
	 * If the specified predictorType is one of the supported types, then check to see 
	 * if the predictor for that type has already been instantiated.
	 * If it has, then return a reference to it.  If it has not, instantiate
	 * a new instance, store a reference to it internally, and return 
	 * a reference. 
	 * 
	 *  <p>Caution: Many predictors are not thread-safe so only call this
	 *  method on PredictorFactory objects instantiated in a single task
	 *  of a multi-threaded application.
	 * 
	 * <p>If a new instance of a Predictor object is instantiated, and
	 * the Predictor uses a model of some sort, then that model will be
	 * loaded from file if necessary and a copy stored in a static map.  
	 * If the model has been loaded previously,
	 * then the new Predictor that is constructed will include a reference
	 * to the existing model (assumption: models are thread safe!).
	 * @param predictorType
	 * @return
	 * @throws Exception
	 */
	public Predictor getPredictor(PredictorType pType) throws Exception {
		if (pType == null) return null;
		return getNewPredictor(pType);
	}

	public boolean isSupported(PredictorType pType) {
		return supportedPredictors.contains(pType);
	}
	
	public EnumSet<PredictorType> getInstantiatedPredictorTypes() {
	   return instantiatedPredictorTypes;
	}

	public List<String> getInstantiatedPredictorNames() {
	    List<String> ptypes = new ArrayList<>();
	    for (PredictorType pt : getInstantiatedPredictorTypes())
		ptypes.add(pt.toString());
	    return ptypes;
	}

	/**
	 * If the predictorType is one of the supported predictorTypes, then 
	 * a new instance of a Predictor object is instantiated.  If
	 * the Predictor uses a model of some sort, then that model will be
	 * loaded from file if necessary and a copy stored in a static map.  
	 * If the model has been loaded previously,
	 * then the new Predictor that is constructed will include a reference
	 * to the existing model (assumption: models are thread safe!).
	 * @param predictorType
	 * @return
	 * @throws GeoTessException 
	 * @throws IOException 
	 * @throws Exception
	 */
	private Predictor getNewPredictor(PredictorType predictorType) throws Exception
	{
		if (predictorType == null) return null;
		Predictor newPredictor = null;

		switch (predictorType)
		{
		case BENDER:
			newPredictor = new Bender(properties); break;
			//		case TAUPTOOLKIT:
			//			return new TaupToolkitWrapper(properties, getLibCorr("tauptoolkit"));
		case LOOKUP2D:
		    newPredictor =  new LookupTablesGMP(properties, logger); break;
		case INFRASOUND:
		    newPredictor =  new InfrasoundPredictor(properties); break;
		case SLBM:
		    newPredictor =  new SLBMWrapper(properties); break;
		case RSTT:
		    newPredictor =  new SLBMWrapper(properties); break;
		case BENDERLIBCORR3D:
		    newPredictor =  new BenderLibCorr3D(properties); break;
		case INFRASOUND_RADIAL2D:
		    newPredictor =  new InfrasoundRadial2D(properties); break;
		case HYDRO_RADIAL2D:
		    newPredictor =  new HydroRadial2D(properties); break;
		case AK135RAYS:
		    //Use Reflection initialize to avoid a direct dependency on ak135-Rays
		    //Currently, ak135 rays is only used in the testing of Tomography and we aren't ready
		    //to release it yet.
		    newPredictor =  (Predictor)
		          Class.forName("gov.sandia.gmp.ak135rays.AK135Rays")
	              .getConstructor(PropertiesPlus.class)
	              .newInstance(properties); break;
		default:
			throw new GMPException(predictorType.toString()+" is not a supported PredictorType.");
		}
		
		if (newPredictor != null)
		    instantiatedPredictorTypes.add(predictorType);
		
		return newPredictor;
	}

	@Override
	public String toString()
	{
		StringBuffer s = new StringBuffer();
		s.append(String.format("%s%n", this.getClass().getSimpleName()));

		try {
			for (Entry<SeismicPhase, PredictorType> entry : phaseToPredictorType.entrySet())
			{
				String phase = entry.getKey().toString();
				PredictorType predictorType = entry.getValue();
				switch (predictorType)
				{
				case BENDER:
				{
				  try {
					Predictor bender = getNewPredictor(predictorType);
					if (bender != null)
					{
						GeoTessModelSiteData model = (GeoTessModelSiteData) bender.getEarthModel();
						s.append(String.format("%-12s bender(%s)%n", 
								phase.equals("NULL") ? "all phases" : phase,
										model == null ? "" : ((GeoTessModel)model).getMetaData().getInputModelFile().getCanonicalPath()));
					}
				  } catch (Exception x) {
				  //TODO bjlawry and sballar did this on 2022/08/05
				  }
			      break;
				}
				case LOOKUP2D:
				{
					s.append(String.format("%-12s lookup2d (%s)%n", 
							phase.equals("NULL") ? "all phases" : phase, ""));
					break;
				}
				case INFRASOUND:
					s.append(String.format("%-12s infrasound(%s)%n", 
							phase.equals("NULL") ? "all phases" : phase, ""));
					break;

				case INFRASOUND_RADIAL2D:
					s.append(String.format("%-12s infrasoundradial2d(%s)%n", 
							phase.equals("NULL") ? "all phases" : phase, ""));
					break;

				case HYDRO_RADIAL2D:
					s.append(String.format("%-12s hydroradial2d(%s)%n", 
							phase.equals("NULL") ? "all phases" : phase, ""));
					break;

				case SLBM:
				{
				  try {
					Predictor slbm = getNewPredictor(predictorType);
					File f = slbm.getModelFile();
					s.append(String.format("%-12s slbm(%s)%n", 
							phase.equals("NULL") ? "all phases" : phase,
									f == null ? "" : f.getCanonicalPath()));
				  } catch (Exception x) {
				  //TODO bjlawry and sballar did this on 2022/08/05
				  }
				  break;
				}
				case AK135RAYS:
				{
					try {
						Predictor ak135rays = getNewPredictor(predictorType);
						if (ak135rays != null)
						{
							GeoTessModelSiteData model = (GeoTessModelSiteData) ak135rays.getEarthModel();
							s.append(String.format("%-12s ak135rays(%s)%n", 
									phase.equals("NULL") ? "all phases" : phase,
											model == null ? "" : ((GeoTessModel)model).getMetaData().getInputModelFile().getCanonicalPath()));
						}
					  } catch (Exception x) {
					  //TODO bjlawry and sballar did this on 2022/08/05
					  }
				      break;
				}
					
				default:
					throw new GMPException(predictorType.toString()+" is not a supported PredictorType.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s.toString();
	}

	public String getDependencyList()
	{
		StringBuffer s = new StringBuffer();
		s.append(String.format("%s %s%n", this.getClass().getSimpleName(), getVersion()));

		try {
			for (PredictorType predictorType : supportedPredictors)
			{
				switch (predictorType)
				{
				case BENDER:
				{
					s.append(String.format("Bender %s%n", Bender.getVersion()));
					break;
				}
				case LOOKUP2D:
				{
					s.append(String.format("Lookup2D %s%n", LookupTablesGMP.getVersion()));
					break;
				}
				case INFRASOUND:
					s.append(String.format("InfrasoundPredictor %s%n", InfrasoundPredictor.getVersion()));
					break;

				case INFRASOUND_RADIAL2D:
					s.append(String.format("Infrasoundradial2dPredictor %s%n", InfrasoundRadial2D.getVersion()));
					break;

				case HYDRO_RADIAL2D:
					s.append(String.format("Hydroradial2dPredictor %s%n", HydroRadial2D.getVersion()));
					break;

				case SLBM:
				{
					s.append(String.format("SLBMWrapper %s%n", SLBMWrapper.getVersion()));
					break;
				}
				case AK135RAYS:
				{
				    Class<?> ak135raysClass = Class.forName("gov.sandia.gmp.ak135rays.AK135Rays");
				    String output = ""+ak135raysClass.getMethod("getVersion").invoke(null);
					s.append(String.format("AK135Rays %s%n", output));
					break;	
				}
					
				default:
					throw new GMPException(predictorType.toString()+" is not a supported PredictorType.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s.toString();
	}

	/**
	 * Retrieve the predictor specification from the properties file and parse it 
	 * into a map of SeismicPhase -> PredictorType.  Some predictors (i.e., Bender)
	 * are able to accept references to thread-safe model objects (GeoTessModel).  
	 * Some predictors cannot.  The supplied ModelInterface object may be null.
	 * @param propertyName
	 * @throws Exception 
	 */
	private void parsePredictorMap(String propertyName) throws Exception
	{
		phaseToPredictorType =  new LinkedHashMap<SeismicPhase, PredictorType>();
		
		String predictorList = properties.getProperty(propertyName);
		if (predictorList == null)
			throw new GMPException(propertyName+" is not a  specified property in the properties file.");

		if (predictorList.indexOf("#") > 0)
			predictorList = predictorList.substring(0, predictorList.indexOf("#"));

		boolean readingPredictor = true;
		boolean readingPhase = false;
		StringBuffer predictorString = new StringBuffer();
		StringBuffer phaseString = new StringBuffer();

		// separate the predictor names from the phase lists.
		for (char ch : predictorList.trim().toCharArray())
		{
			if (readingPredictor)
			{
				if (ch == '(')
				{
					readingPredictor = false;
					readingPhase = true;
				} 
				else if (ch == ',')
				{
					predictorString.append(';');
					phaseString.append(" ; ");
				} 
				else
					predictorString.append(ch);

			} 
			else if (readingPhase)
			{
				if (ch == ')')
				{
					readingPredictor = true;
					readingPhase = false;
				} 
				else
					phaseString.append(ch);
			}
		}

		// parse the list of predictorNames
		String[] predictorNames = predictorString.toString().split(";");
		for (int i = 0; i < predictorNames.length; ++i)
			predictorNames[i] = predictorNames[i].trim();

		//System.out.println("predictorNames = " + Arrays.toString(predictorNames));

		PredictorType[] predictorTypes = new PredictorType[predictorNames.length];
		for (int i=0; i<predictorNames.length; ++i)
		{
			try
			{
				predictorTypes[i] = PredictorType.valueOf(predictorNames[i].trim().toUpperCase());
			}
			catch (java.lang.IllegalArgumentException ex)
			{
				StringBuffer buf = new StringBuffer(predictorNames[i]
						+" is not a recognized PredictorType.  Must be one of");
				for (PredictorType p : PredictorType.values())
					buf.append(' ').append(p.toString().toLowerCase());
				throw new GMPException(buf.toString());
			}
		}

		// parse the list of phaseLists
		String[] phaseLists = phaseString.toString().split(";");
		for (int i = 0; i < phaseLists.length; ++i)
			phaseLists[i] = phaseLists[i].trim();

		//System.out.println("phaseLists    = " + Arrays.toString(phaseLists));

		// populate a map from predictor type -> set of phases
		LinkedHashMap<PredictorType, HashSet<SeismicPhase>> predictorTypeToPhase = 
				new LinkedHashMap<PredictorType, HashSet<SeismicPhase>>();

		for (int i = 0; i < phaseLists.length; ++i)
		{
			HashSet<SeismicPhase> p = new HashSet<SeismicPhase>();
			predictorTypeToPhase.put(predictorTypes[i], p);

			for (String s : phaseLists[i].split(","))
				if (s.trim().length() > 0)
					p.add(SeismicPhase.valueOf(s.trim()));
		}

		for (Map.Entry<PredictorType, HashSet<SeismicPhase>> entry : predictorTypeToPhase.entrySet())
		{
			PredictorType predictorType = entry.getKey();
			HashSet<SeismicPhase> phaseList = entry.getValue();

			// if this predictor had no phase list, set the phaselist to SeismicPhase.NULL
			// and this predictorType can support all phases
			if (phaseList.isEmpty())
				phaseList.add(SeismicPhase.NULL);

			// add entry for SeismicPhase -> Predictor
			// to the main map. Note that a subsequent predictor
			// might replace this entry. Predictor order in the
			// properties file matters.
			for (SeismicPhase phase : phaseList)
				phaseToPredictorType.put(phase, predictorType);

		}
	}

	/**
	 * Retrieve a Prediction for the specified PredictionRequest.
	 * @param request
	 * @return Prediction for the specified request.
	 * @throws Exception only in the event that a Predictor constructor throws an exception. 
	 * If a Predictor throws an exception while trying to compute a prediction, or if this
	 * PredictorFactory does not support the specified request, then a new, invalid Prediction
	 * will be returned (see Prediction.getErrorMessage() to determine what happened).
	 */
	public Prediction getPrediction(PredictionRequest request) throws Exception {
		Predictor predictor = getPredictor(request.getPhase());
		if (predictor != null)
		{
			try 
			{
				//request.setPredictor(predictor);
				return predictor.getPrediction(request);
			} 
			catch (Exception e) 
			{
				return new Prediction(request, predictor, e);
			}
		}
		return new Prediction(request, null, 
				new Exception("PredictorFactory does not support predictions for phase "+request.getPhase().toString()));
	}
	
	/**
	 * Retrieves the correct Predictor based on the request's phase, then calls that predictor's
	 * getPrediction() method with the specified request.
	 * @param request request to compute a prediction for
	 * @return computed prediction
	 * @throws Exception
	 */
	public Prediction computePrediction(PredictionRequest request) throws Exception {
	  if(logAllRequests) {
	    if(logger == null) {
	      logger = new ScreenWriterOutput();
	      logger.setScreenOutputOn();
	    }
	    logger.writeln("["+new SimpleDateFormat(LOG_TIME_FORMAT).format(new Date())+"] ["+
	        Thread.currentThread().getName()+"] ["+getClass().getCanonicalName()+
	        "] Computing request: "+request.toStringOneLiner());
	  }
	  
	  Prediction p = getPredictor(request.getPhase()).getPrediction(request);
	  
	  if(logAllRequests) {
	    logger.writeln("["+new SimpleDateFormat(LOG_TIME_FORMAT).format(new Date())+"] ["+
            Thread.currentThread().getName()+"] ["+getClass().getCanonicalName()+
            "] Completed request: "+request.toStringOneLiner());
	  }
	  
	  return p;
	}

	/**
	 * Computes predictions, in parallel if the supplied ExecutorService is not null, otherwise
	 * the predictions are computed in the calling thread.
     * @param c requests to compute predictions for
	 * @param es optional service to compute predictions with (null permitted)
	 * @return list of Predictions computed
	 * @throws Exception
	 */
    public ArrayList<Prediction> computePredictions(
        Collection<? extends PredictionRequest> c, ExecutorService es,
        BiConsumer<Integer,Integer> progress) throws Exception {
      ArrayList<Prediction> predictions = new ArrayList<Prediction>();
      Map<PredictorType,List<PredictionRequest>> requestsByType =
          new EnumMap<>(PredictorType.class);
      
      for(PredictionRequest p : c) 
        requestsByType.computeIfAbsent(getPredictorType(p.getPhase()), 
            t -> new LinkedList<>()).add(p);

      // send all the Arrivals to the Predictor and get back a Collection of
      // results. The predictor may be able to compute predictions in parallel.
      Map<Future<List<Prediction>>,Task> fs = new LinkedHashMap<>();
      for(Entry<PredictorType,List<PredictionRequest>> e : requestsByType.entrySet()) {
        if (es != null) {
          int ppt = getPredictor(e.getKey()).getPredictionsPerTask();
          int qsize = Math.min(e.getValue().size(), ppt);
          ArrayList<PredictionRequest> queue = new ArrayList<>(qsize);
          for (PredictionRequest req : e.getValue()) {
            queue.add(req);
            if (queue.size() >= ppt) {
              Task t = new Task(queue, properties, name);
              fs.put(es.submit(t),t);
              queue = new ArrayList<>(qsize);
            }
          }
          
          if (!queue.isEmpty()) {
            Task t = new Task(queue, properties, name);
            fs.put(es.submit(t),t);
          }
        } else {
          for (PredictionRequest request : e.getValue())
            predictions.add(computePrediction(request));
        }
      }
      
      int total = fs.size();
      int done = 0;
      for (Future<List<Prediction>> f : fs.keySet()) {
        Task t = fs.get(f);
        
        if(progress != null) progress.accept(done++, total);
        try {
          List<Prediction> ps = f.get();
          if (ps != null && !ps.isEmpty())
            predictions.addAll(ps);
          else {
            System.err.println("task returned no predictions! ("+ps+")");
            if (t != null) {
              if (t.requests != null)
                t.requests.forEach(r -> System.err.println(" - " + r));
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          System.err.println("task threw exception!");
          if (t != null) {
            if (t.requests != null)
              t.requests.forEach(r -> System.out.println(" - " + r));
          }
        }
      }
      return predictions;
    }
    
    public ArrayList<Prediction> computePredictions(
        Collection<? extends PredictionRequest> c, ExecutorService es) throws Exception{
      return computePredictions(c,es,null);
    }
    
    /**
     * Convenience method that computes predictions in the calling threads (same effect as calling
     * <code>computePredictions(c,null)</code>
     * @param c requests to compute predictions for
     * @return predictions in a list
     * @throws Exception
     */
    public ArrayList<Prediction> computePredictions(Collection<? extends PredictionRequest> c)
        throws Exception {
      return computePredictions(c,null,null);
    }
	
    public static class Task implements Callable<List<Prediction>>, Externalizable {
      /* 2023-05-12, bjlawry:
       * 
       * This static initialization block is what allows PredictorFactory to read files and
       * resources remotely from the Fabric Client when they are not otherwise available on the
       * local file system. These files only need to be accessible at the client, eliminating the
       * need for NFS mounts at the Fabric Nodes.
       */
      static { 
        GlobalInputStreamProvider.forFiles(new ParallelBrokerFileInputStreamProvider());
      }
      private static final long serialVersionUID = 1L;
      private static final Map<Long, PredictorFactory> factory = new ConcurrentHashMap<>();
      private List<PredictionRequest> requests;
      private PropertiesPlusGMP props;
      private String propertyName;
      
      private Task(List<PredictionRequest> r, PropertiesPlusGMP p, String n) {
        if(r == null) throw new NullPointerException("null request(s)");
        if(p == null) throw new NullPointerException("null properties");
        if(n == null) throw new NullPointerException("null property name");
        
        requests = r;
        props = p;
        propertyName = n;
      }
      
      /** only to be called by the Externalizable framework */
      public Task() {
        requests = null;
        props = null;
        propertyName = null;
      }
      
      private List<Prediction> callHelper(PredictorFactory p) throws Exception{
        ArrayList<Prediction> output = new ArrayList<>(requests.size());
        output.addAll(p.computePredictions(requests,null,null));
        return output;
      }
      
      @Override
      public List<Prediction> call() throws Exception {
        try {
          PredictorFactory p = factory.get(props.getModificationId());
          if (p != null) return callHelper(p);

          // Performance optimization. This allows remote Fabric threads to initialize only one
          // factory (and corresponding predictors and models) per JVM.
          synchronized (factory) {
            if (p == null) {
              p = new PredictorFactory(props, propertyName);
              factory.put(props.getModificationId(), p);
            }
          }

          return callHelper(p);
        } catch (Exception e) {
          e.printStackTrace();
          throw e;
        }
      }

      @Override
      public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(requests);
        out.writeObject(props);
        out.writeObject(propertyName);
      }

      @SuppressWarnings("unchecked")
      @Override
      public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        requests = (List<PredictionRequest>)in.readObject();
        props = (PropertiesPlusGMP)in.readObject();
        propertyName = (String)in.readObject();
      }
    }
}
