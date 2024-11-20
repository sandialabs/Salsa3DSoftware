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
package gov.sandia.geotess.extensions.libcorr3d;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.parallelutils.ParallelBroker;
import gov.sandia.gmp.parallelutils.ParallelBroker.ParallelMode;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.globals.Site;
import gov.sandia.gmp.util.globals.SiteInterface;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;

/**
 * LibCorr3D manages a collection of LibCorr3DModels.  Its most important 
 * function is to establish a link between a Site/phase/attribute the caller is interested in  
 * and a particular LibCorr3DModel that can support that Site/phase/attribute.
 * @author sballar
 *
 */
public class LibCorr3D
{
	/**
	 * A Map from <code>configurationString</code> to LibCorr3D instance used to 
	 * prevent instantiating new instances of LibCorr3D when an identical instance
	 * is already available in memory.
	 */
	static private Map<String, LibCorr3D> libcorr3dMap;

	synchronized static public Map<String, LibCorr3D> getConfigurationMap() { 
		if (libcorr3dMap == null)
			libcorr3dMap = new ConcurrentHashMap<String, LibCorr3D>();
		return libcorr3dMap; 
	}

	static public enum ConfigurationKeyOption { ROOT_DIRECTORY_ONLY, INCLUDE_PARAMETERS }

	static private ConfigurationKeyOption currentConfigurationKeyOption;

	static synchronized public ConfigurationKeyOption getConfigurationKeyOption() { 
		if (currentConfigurationKeyOption == null)
			currentConfigurationKeyOption = ConfigurationKeyOption.ROOT_DIRECTORY_ONLY;
		return currentConfigurationKeyOption; 
	}

	static synchronized public void setConfigurationKeyOption(ConfigurationKeyOption option) {
		if (option != getConfigurationKeyOption())
			getConfigurationMap().clear();
		currentConfigurationKeyOption = option;
	}

	/**
	 * Retrieve an instance of LibCorr3D.  If an instance with the same properties
	 * is already available in memory, a reference to the existing instance is returned.
	 * Otherwise, a new instance is instantiated and returned.
	 * @param rootDirectory
	 * @param relGridPath
	 * @param interpTypeHorz
	 * @param maxSiteSeparation
	 * @param matchOnRefsta
	 * @param preloadModels
	 * @param maxModels
	 * @param logger may be null
	 * @return
	 * @throws Exception
	 */
	synchronized static public LibCorr3D getLibCorr3D(File rootDirectory, String relGridPath,
			InterpolatorType interpTypeHorz, double maxSiteSeparation, boolean matchOnRefsta, 
			boolean preloadModels, int maxModels, ScreenWriterOutput logger) throws Exception
	{
		int filesPerTask = -1;
		int maxProcessors = -1;
		return getLibCorr3D(rootDirectory, relGridPath, interpTypeHorz, maxSiteSeparation, matchOnRefsta,
				preloadModels, maxModels, filesPerTask, maxProcessors, logger);
	}

	/**
	 * Retrieve an instance of LibCorr3D.  If an instance with the same properties
	 * is already available in memory, a reference to the existing instance is returned.
	 * Otherwise, a new instance is instantiated and returned.
	 * @param rootDirectory
	 * @param relGridPath
	 * @param interpTypeHorz
	 * @param maxSiteSeparation
	 * @param matchOnRefsta
	 * @param preloadModels
	 * @param maxModels
	 * @param logger may be null
	 * @return
	 * @throws Exception
	 */
	synchronized static public LibCorr3D getLibCorr3D(File rootDirectory, String relGridPath,
			InterpolatorType interpTypeHorz, double maxSiteSeparation, boolean matchOnRefsta, 
			boolean preloadModels, int maxModels, int filesPerTask, int maxProcessors, ScreenWriterOutput logger) throws Exception
	{
		String configurationString = "libcorrRootDirectory = " + rootDirectory.getCanonicalPath();

		if (getConfigurationKeyOption() == ConfigurationKeyOption.INCLUDE_PARAMETERS)
			configurationString += String.format("; libcorrInterpolatorTypeHorizontal = %s; "
					+ "libcorrMaxSiteSeparation = %1.3f; "
					+ "libcorrMatchOnRefsta = %b; "
					+ "libcorrMaxModels = %d; "
					+ "libcorrFilesPerTask = %d; "
					+ "libcorrMaxProcessors = %d",
					interpTypeHorz.toString(), 
					maxSiteSeparation,
					matchOnRefsta,
					maxModels,
					filesPerTask,
					maxProcessors);

		LibCorr3D libcorr3d = getConfigurationMap().get(configurationString);
		
		if (libcorr3d == null)
		{
			libcorr3d = new LibCorr3D(rootDirectory, relGridPath, interpTypeHorz, maxSiteSeparation, matchOnRefsta,
					preloadModels, maxModels, filesPerTask, maxProcessors, logger);
			libcorr3d.configurationString = configurationString;
			getConfigurationMap().put(configurationString, libcorr3d);			
		}

		return libcorr3d;
	}
	
	static public LibCorr3D getLibCorr3D(File rootDirectory) throws Exception {
		return getLibCorr3D(rootDirectory, ".", InterpolatorType.LINEAR, 10., false, false, -1, -1, -1, null);
	}

	/**
	 * Retrieve an empty instance of LibCorr3D. All requests submitted to this instance
	 * will return NA values.
	 * @return an empty instance of LibCorr3D
	 */
	synchronized static public LibCorr3D getLibCorr3D()
	{
		String configurationString = "empty";

		LibCorr3D libcorr3d = getConfigurationMap().get(configurationString);

		if (libcorr3d != null)
			return libcorr3d;

		libcorr3d = new LibCorr3D();

		libcorr3d.configurationString = configurationString;

		getConfigurationMap().put(configurationString, libcorr3d);

		return libcorr3d;
	}

	/**
	 * Directory from which the libcorr models were loaded.
	 */
	private File rootDirectory;

	/**
	 * An array of all the names of the files for the models 
	 * available in <code>rootDirectory</code>.  All the elements of the <code>modelFiles</code> array
	 * are not null and all the files actually exist in <code>rootDirectory</code>.
	 * The dimensions of the <code>modelFiles</code> array and the <code>models</code> array
	 * are the same.  <code>modelFiles</code> is populated in the constructor and never modified.
	 */
	ArrayList<File> modelFiles;

	/**
	 * An array of all the unique models that are available in <code>rootDirectory</code>.  
	 * The dimensions of the <code>models</code> array and the <code>modelFiles</code> array
	 * are the same.  Not all models necessarily reside in memory at once. Models that are not 
	 * in memory will have a null entry in the <code>models</code> array.
	 * Models that have not been recently referenced may be deleted from memory to satisfy 
	 * the <code>maxModels</code> constraint.
	 */
	ArrayList<LibCorr3DModel> models;
	protected ArrayList<LibCorr3DModel> getModels() { return models; }

	/**
	 * Map from SiteInterface -> phase -> attribute (TT | AZ | SH) ->  model index (handle).
	 * The SiteInterface object is one the user is interested in, which is not necessarily
	 * equal to any included in a libcorr3d model.  Some of the SiteInterface objects will be close
	 * enough to ones in the model that an association exists.  For others, there may be 
	 * no association with a libcorr3d model.
	 */
	private Map<SiteInterface, Map<String, Map<String, Integer>>> supportMap;

	/**
	 * Map from SiteInterface -> phase -> attribute (TT | AZ | SH) -> handle (modelIndex)
	 * The SiteInterface object in this case is one of the sites represented in a
	 * libcorr3d model.
	 */
	Map<SiteInterface, Map<String, Map<String, Integer>>> modelInfoMap;

	/**
	 * A stack containing the indices of the libcorr models, in order of
	 * how recently the model was requested by a call to method <code>getModel(handle)</code>.  
	 * The first element is the index of the model most recently requested
	 * by a call to method <code>getModel(handle)</code>.  
	 * When the size of the stack exceeds <code>maxModels</code>, the last handle in <code>modelStack</code> is
	 * popped off the end of the stack and the corresponding model in the <code>models</code> 
	 * array is removed from memory (set to null). 
	 */
	LinkedList<Integer> modelStack;

	/**
	 * Angular separation in km of two sites such that if they are separated
	 * by less than this, they can be considered a match.
	 */
	private double maxSiteSeparationKm;

	/**
	 * If true, and two Sites have the same refsta, their separation is deemed to be zero.
	 */
	private boolean matchOnRefsta;

	/**
	 * Maximum number of models that can be in memory at one time.  
	 */
	private int maxModels;

	/**
	 * If the model grids reside outside the models, then this is the 
	 * relative path from the model directory to the grid directory
	 */
	String relGridPath;

	/**
	 * InterpolatorType.LINEAR or InterpolatorType.NATURAL_NEIGHBOR.
	 */
	private InterpolatorType interpTypeHorz;

	/**
	 * InterpolatorType.LINEAR.
	 */
	private final InterpolatorType interpTypeRadial = InterpolatorType.LINEAR;

	/**
	 * A map from attribute names in the input libcorr3d models to the attribute
	 * names that are used by a code that uses an instance of LibCorr3D.
	 */
	public static final Map<String, String> attributeTranslationMap;
	static {
		attributeTranslationMap = new HashMap<>();
		attributeTranslationMap.put("TT_DELTA_AK135", "TT");
		attributeTranslationMap.put("TT_PATH_CORRECTION", "TT");
		attributeTranslationMap.put("TT_MODEL_UNCERTAINTY", "TT");
		attributeTranslationMap.put("AZIMUTH_PATH_CORRECTION", "AZ");
		attributeTranslationMap.put("AZIMUTH_DELTA_AK135", "AZ");
		attributeTranslationMap.put("AZIMUTH_MODEL_UNCERTAINTY", "AZ");
		attributeTranslationMap.put("SLOWNESS_PATH_CORRECTION", "SH");
		attributeTranslationMap.put("SLOWNESS_DELTA_AK135", "SH");
		attributeTranslationMap.put("TT", "TT");
		attributeTranslationMap.put("AZ", "AZ");
		attributeTranslationMap.put("SH", "SH");
	}

	/**
	 * A set containing the unique set of attributes available.  
	 * These attributes are values in <code>attributeTranslationMap</code>:
	 * (TT | AZ | SH)
	 */
	Set<String> supportedAttributes;

	/**
	 * A set containing the unique set of phases available.  
	 */
	Set<String> supportedPhases;

	/**
	 * A unique string that identifies this instance of LibCorr3D.  It is 
	 * a concatenation of all the properties used to construct this instance.
	 */
	private String configurationString;

	/**
	 * A reference to a logger supplied by calling method.  May be null.
	 */
	private ScreenWriterOutput logger;

	boolean preloadModels;

	private String stringRepresentation;

	/**
	 * Retrieve an instance of LibCorr3D.  If an instance with the same properties
	 * is already available in memory, a reference to the existing instance is returned.
	 * Otherwise, a new instance is instantiated and returned.
	 * <p>The properties that define uniqueness (that go into the libcorr3dMap key) are
	 * <ol>
	 * <li>&lt;prefix&gt;RootDirectory (no default value)
	 * <li>&lt;prefix&gt;InterpTypeHorz (defaults to linear)
	 * <li>&lt;prefix&gt;MaxSiteSeparation (defaults to 10 km)
	 * <li>&lt;prefix&gt;MatchOnRefsta (defaults to false)
	 * </ol>
	 * @param prefix
	 * @param properties
	 * @return
	 * @throws Exception
	 */
	static public LibCorr3D getLibCorr3D(String prefix, Properties properties, ScreenWriterOutput logger) throws Exception {
		return getLibCorr3D(
				new File(getProperty(properties, prefix, "PathCorrectionsRoot", null)),

				getProperty(properties, prefix, "PathCorrectionsRelativeGridPath", "."),

				InterpolatorType.valueOf(getProperty(properties, prefix, "InterpolatorTypeHorizontal", "LINEAR")),

				Double.valueOf(getProperty(properties, prefix, "MaxSiteSeparation", "10.")),

				Boolean.valueOf(getProperty(properties, prefix, "MatchOnRefsta", "false")),

				Boolean.valueOf(getProperty(properties, prefix, "PreloadModels", "true")),

				Integer.valueOf(getProperty(properties, prefix, "MaxModels", "-1")),

				Integer.valueOf(getProperty(properties, prefix, "FilesPerTask", "-1")),

				Integer.valueOf(getProperty(properties, prefix, "MaxProcessors", "1")),

				logger
				);
	}

	static private String getProperty(Properties properties, String prefix, String suffix, String defaultValue) {
		for (String s : new String[] {"LibCorr", "Libcorr"}) 
			if (properties.containsKey(prefix+s+suffix))
				return properties.getProperty(prefix+s+suffix);
		return properties.getProperty(prefix+"LibCorr3D"+suffix, defaultValue);	
	}

	/**
	 * Private constructor that returns an empty instance of LibCorr3D
	 */
	protected LibCorr3D()
	{
		this.rootDirectory = new File("");
		this.relGridPath = ".";
		this.interpTypeHorz = InterpolatorType.LINEAR;
		this.matchOnRefsta = false;
		this.maxSiteSeparationKm = 10.; // km
		this.maxModels = Integer.MAX_VALUE;

		models = new ArrayList<>();
		modelFiles = new ArrayList<File>();
		modelStack = new LinkedList<Integer>();
		supportMap = new HashMap<>();
		modelInfoMap = new HashMap<>();
		supportedAttributes = new LinkedHashSet<>();
		supportedPhases = new HashSet<>();
	}

	/**
	 * Private constructor
	 * @param aRootDirectory
	 * @param aRelGridPath
	 * @param aInterpTypeHorz
	 * @param maxSiteSeparation
	 * @param matchOnRefsta
	 * @param preloadModels
	 * @param nModels
	 * @param logger
	 * @throws Exception
	 */
	protected LibCorr3D(File aRootDirectory, String aRelGridPath,
			InterpolatorType aInterpTypeHorz, double maxSiteSeparation, boolean matchOnRefsta,
			boolean preloadModels, int nModels, int filesPerTask, int _maxProcessors, ScreenWriterOutput logger) throws Exception
	{
		this();

		long timer = System.currentTimeMillis();

		this.rootDirectory = checkRootDirectory(aRootDirectory);
		this.relGridPath = aRelGridPath;

		// loop over all the LibCorr3DModels in rootDirectory and all of its sub directories.
		modelFiles = getFiles(this.rootDirectory, this.relGridPath);
		Collections.sort(modelFiles);

		this.logger = logger;
		this.interpTypeHorz = aInterpTypeHorz;
		this.maxSiteSeparationKm = maxSiteSeparation;
		this.matchOnRefsta = matchOnRefsta;
		this.preloadModels = preloadModels;

		int maxProcessors = _maxProcessors >=  1 ? _maxProcessors : Runtime.getRuntime().availableProcessors();

		this.maxModels = nModels < 0 ? Integer.MAX_VALUE : nModels;

		models.ensureCapacity(modelFiles.size());
		for (int handle=0; handle < modelFiles.size(); ++handle) models.add(null);

		if (maxProcessors == 1) {
			filesPerTask = modelFiles.size();
		}
		else if (filesPerTask <= 0) {
			// choose a batch size that will result in number of tasks = 10 times the number of processors,
			// but no less than the number of processors

			// minFilesPerTask is task size that results in number of tasks equal to number of processors
			int minFilesPerTask = (int) Math.ceil((double)modelFiles.size()/(double)maxProcessors);
			// targetFilesPerTask will produce number of tasks = 10 * number of processors
			int targetFilesPerTask = minFilesPerTask / 10;  
			filesPerTask = (int) Math.max(minFilesPerTask, targetFilesPerTask);
		}

		ArrayList<Integer> handles = new ArrayList<Integer>(filesPerTask);
		ArrayList<File> files = new ArrayList<File>(filesPerTask);
		ArrayList<Boolean> returnModels = new ArrayList<Boolean>(filesPerTask); 

		ArrayList<ReadModelTask> tasks = new ArrayList<>(modelFiles.size()/filesPerTask+1);
		for (int handle=0; handle < modelFiles.size(); ++handle)  {
			handles.add(handle);
			files.add(modelFiles.get(handle));
			returnModels.add(preloadModels && handle < maxModels);

			if (handles.size() == filesPerTask || handle == modelFiles.size()-1) {
				tasks.add(new ReadModelTask(handles, files, aRelGridPath, returnModels));

				handles = new ArrayList<Integer>(filesPerTask);
				files = new ArrayList<File>(filesPerTask);
				returnModels = new ArrayList<Boolean>(filesPerTask); 
			}
		}

		ParallelBroker parallelBroker = ParallelBroker.create
				(maxProcessors == 1 ? ParallelMode.SEQUENTIAL : ParallelMode.CONCURRENT);

		parallelBroker.setProcessorCount(maxProcessors);
		parallelBroker.submit(tasks);

		double sumTaskTimes = 0;
		ReadModelTaskResult taskResult = null;
		while ((taskResult = (ReadModelTaskResult) parallelBroker.getResultWait()) != null) {
			for (int i=0; i<taskResult.size(); ++i) {
				int handle = taskResult.getHandle(i);
				if (taskResult.getModel(i) != null) {
					modelStack.add(handle);
					models.set(handle, taskResult.getModel(i));
				}

				// add the required model into to the modelInfoMap, which is a map
				// from modelSite -> modelPhase -> modelAttribute -> handle
				Map<String, Map<String, Integer>> siteMap = modelInfoMap.get(taskResult.getSite(i));
				if (siteMap == null)
					modelInfoMap.put(taskResult.getSite(i), siteMap = new HashMap<>());
				for (String phase : taskResult.getSupportedPhases(i)) {
					Map<String, Integer> phaseMap = siteMap.get(phase);
					if (phaseMap == null)
						siteMap.put(phase, phaseMap = new HashMap<>());
					phaseMap.put(taskResult.getAttribute(i), handle);
				}

				// add all the phases to list of all supported phases
				for (String phase : taskResult.getSupportedPhases(i))
					supportedPhases.add(phase);
				// add the modelAttribute to the list of all supported attributes.
				supportedAttributes.add(taskResult.getAttribute(i));
			}
			sumTaskTimes += taskResult.getEllapsedTime()/1000.;
		}
		parallelBroker.close();

		// ellapsed time in seconds
		double ellapsedTime = (System.currentTimeMillis()-timer)*0.001;

		// save info for the toString() function
		double efficiency = ellapsedTime/sumTaskTimes;
		String timeToReadOneFile = Globals.elapsedTime(sumTaskTimes/modelFiles.size());
		String averageTimePerTask = Globals.elapsedTime(ellapsedTime/tasks.size());
		
		this.stringRepresentation = String.format("LibCorr3D.%s loaded library from directory %n"
				+ "%s%n"
				+ "Supported phases = %s%n"
				+ "Supported attributes = %s%n"
				+ "maxSiteSeparation = %1.3f km%n"
				+ "matchOnRefsta = %b%n"
				+ "preloadModels = %b%n"
				+ "maxModels = %d%n"
				+ "number of models = %d%n"
				+ "maxProcessors = %d%n"
				+ "number of tasks = %d%n"
				+ "files per task = %d%n"
				+ "read efficiency = %1.4f%n"
				+ "time to read one file = %s%n"
				+ "average time per task = %s%n"
				+ "time to load models = %s%n", 
				getVersion(), 
				rootDirectory.getAbsolutePath(), 
				supportedPhases.toString(), 
				supportedAttributes.toString(),
				maxSiteSeparationKm,
				matchOnRefsta,
				preloadModels,
				maxModels,
				modelFiles.size(), 
				maxProcessors,
				tasks.size(),
				filesPerTask,
				efficiency,
				timeToReadOneFile,
				averageTimePerTask,
				Globals.elapsedTime(ellapsedTime)
				);
		
		if (this.logger != null && logger.getVerbosity() > 0)
			this.logger.writeln(stringRepresentation);

	}

	//	private void processFile(int handle) throws Exception {
	//		// for each model in the library, must discover the following 3 pieces of information:
	//		Site modelSite = null;
	//		List<String> modelPhaseList = new ArrayList<String>();
	//		String modelAttribute = null;
	//
	//		if (preloadModels && modelStack.size() < maxModels)
	//		{
	//			models.add(new LibCorr3DModel(modelFiles.get(handle), relGridPath));
	//
	//			modelStack.add(handle);
	//
	//			// the whole model has been loaded. Get required model info:
	//			modelSite = models.get(handle).getSite();
	//			modelAttribute = attributeTranslationMap.get(models.get(handle).getMetaData().getAttributeName(0));
	//			modelPhaseList.addAll(models.get(handle).getSupportedPhases());
	//		}
	//		else
	//		{
	//			models.add(null);
	//
	//			// Read just the metadata object from the model file.  Newer models have the 
	//			// required model info in the metatdata properties object.  But older models do not.
	//			// Reading just the metatdata from the model file is MUCH faster than reading the whole file.
	//			GeoTessMetaData md = GeoTessMetaData.getMetaData(modelFiles.get(handle));
	//
	//			if (md.getProperties().containsKey("site"))
	//				modelSite = new Site(md.getProperties().get("site"));
	//
	//			if (md.getProperties().containsKey("supportedPhases"))
	//				for (String s : md.getProperties().get("supportedPhases").trim().replaceAll(",", " ").split("\\s+"))
	//					modelPhaseList.add(s);
	//
	//			modelAttribute = attributeTranslationMap.get(md.getAttributeName(0));
	//
	//			// if unsuccessful, then must read in the entire model (slow!)
	//			// this will only happen with models written by old versions of code.
	//			if (modelSite == null || modelPhaseList.isEmpty() || modelAttribute == null )
	//			{
	//				// load the model
	//				LibCorr3DModel model = new LibCorr3DModel(modelFiles.get(handle), relGridPath);
	//
	//				modelSite = model.getSite();
	//				modelAttribute = attributeTranslationMap.get(model.getMetaData().getAttributeName(0));
	//				modelPhaseList.clear();
	//				modelPhaseList.addAll(model.getSupportedPhases());
	//				model.close();
	//			}
	//		}
	//
	//		// add the required model into to the modelInfoMap, which is a map
	//		// from modelSite -> modelPhase -> modelAttribute -> handle
	//		Map<String, Map<String, Integer>> siteMap = modelInfoMap.get(modelSite);
	//		if (siteMap == null)
	//			modelInfoMap.put(modelSite, siteMap = new HashMap<>());
	//		for (String phase : modelPhaseList) {
	//			Map<String, Integer> phaseMap = siteMap.get(phase);
	//			if (phaseMap == null)
	//				siteMap.put(phase, phaseMap = new HashMap<>());
	//			phaseMap.put(modelAttribute, handle);
	//		}
	//
	//		// add all the phases to list of all supported phases
	//		for (String phase : modelPhaseList)
	//			supportedPhases.add(phase);
	//		// add the modelAttribute to the list of all supported attributes.
	//		supportedAttributes.add(modelAttribute);
	//
	//	}

	/**
	 * Retrieve the handle for the specified site-phase-attribute combination,
	 * or -1 if site-phase-attribute is not supported
	 * @param userSite
	 * @param phase
	 * @param attribute
	 * @return
	 */
	synchronized public int getHandle(SiteInterface userSite, String phase, String attribute)
	{
		// see if site is already supported
		Map<String, Map<String, Integer>> siteMap = supportMap.get(userSite);
		if (siteMap == null) 
			// site has never been evaluated.  do so now.
			supportMap.put(userSite, siteMap = searchModels(userSite));
		Map<String, Integer> phaseMap = siteMap.get(phase);
		if (phaseMap != null) {
			Integer modelInfo = phaseMap.get(attributeTranslationMap.get(attribute));
			if (modelInfo != null)
				return modelInfo;
		} 
		return -1;
	}

	/**
	 * Empty map from phase -> attribute -> handle shared by all unsupported.
	 * No information should ever be added to this map.
	 * user sites.
	 */
	private final Map<String, Map<String, Integer>> emptyMap = new HashMap<>();

	/**
	 * If the specified site is a supported model site, return info about that site.
	 * If the specified site is not a supported model site, search through the model
	 * sites and see if there is one that is 'close'.  If so, return info about that
	 * model site.
	 * If there is no 'close' modelSite, return an empty map.
	 * @param site
	 * @return
	 */
	private Map<String, Map<String, Integer>> searchModels(SiteInterface site)
	{
		// if site is a model site, it will have an entry in modelInfoMap.  Retrieve that
		// model info and return it.
		Map<String, Map<String, Integer>> map = modelInfoMap.get(site);
		if (map != null)
			return map;

		// if site is not a model site then see if there is a model site that is 'close'.  If there
		// is, then return that.
		for (Entry<SiteInterface, Map<String, Map<String, Integer>>> entry : modelInfoMap.entrySet()) {
			SiteInterface modelSite = entry.getKey();
			if (getSiteSeparation(modelSite, site) <= maxSiteSeparationKm) {
//				if (logger != null && logger.getVerbosity() >= 4) {
//					logger.writeln(String.format("LibCorr3D associated model site %-6s "
//							+ "and user site %-6s %d", modelSite.getSta(), site.getSta(), site.getOndate()));
//				}
				return entry.getValue();
			}
		}
		return emptyMap;
	}

	/**
	 * Retrieve the angular separation of two sites, in km
	 * @param modelSite
	 * @param userSite
	 * @return
	 */
	private double getSiteSeparation(SiteInterface modelSite, SiteInterface userSite) {
		if (matchOnRefsta && !modelSite.getRefsta().equals(Site.REFSTA_NA) 
				&& modelSite.getRefsta().equals(userSite.getRefsta()))
			return 0.;
		return VectorGeo.angle(modelSite.getUnitVector(), userSite.getUnitVector())*modelSite.getRadius();
	}

	/**
	 * Retrieve the model with the specified index (handle).  If the requested
	 * model is not currently loaded in memory, it is loaded from disk.  
	 * A check is performed to ensure that the number of models currently in 
	 * memory does not exceed <code>maxModels</code> and the least-recently used
	 * model is deleted if it is.
	 * @param handle
	 * @return
	 * @throws Exception
	 */
	synchronized public LibCorr3DModel getModel(int handle) throws Exception
	{
		if (handle < 0 || handle >= models.size())
			return null;

		LibCorr3DModel model = models.get(handle);

		// if the requested model is not currently loaded in memory, load it.
		if (model == null) {
			model = new LibCorr3DModel(modelFiles.get(handle), relGridPath);

			models.set(handle, model);
		}

		// now check to see if maximum allowed number of models are currently in memory.
		// If maxModels is >= models.size() then it is permissible for all available models to 
		// to be loaded at the same time and therefore no checks are necessary
		if (maxModels < models.size()) 
		{
			// remove handle from the stack (if currently there) and push it onto the front
			Integer i = Integer.valueOf(handle);
			modelStack.remove(i);
			modelStack.addFirst(i);
			// if the stack is full, pop the oldest, least recently used, model 
			// off the end of the stack and remove the corresponding model from memory.
			while (modelStack.size() > maxModels) {
				// get index of least recently used model
				i = modelStack.removeLast();
				models.get(i).close();
				models.set(i, null);
			}
		}
		return models.get(handle);
	}

	public LibCorr3DModel getModel(SiteInterface station, String phase, String attribute) throws Exception {
		return getModel(getHandle(station, phase, attribute));
	}

	/**
	 * Retrieve a GeoTessPosition object for the LibCorr3DModel with the specified
	 * handle (modelIndex).  Returns null if handle &lt; 0 or &ge; <code>models.size()</code>.
	 * @param handle
	 * @return
	 * @throws Exception
	 */
	public GeoTessPosition getGeoTessPosition(int handle) throws Exception
	{
		LibCorr3DModel model = getModel(handle);

		if (model == null)
			return null;

		return model.getGeoTessPosition(interpTypeHorz, interpTypeRadial);
	}

	/**
	 * Retrieve a GeoTessPosition object for the LibCorr3DModel that supports the specified
	 * Site-phase-attribute, or null if the site-phase-attribute is not supported.
	 * @param site
	 * @param phase
	 * @param attribute
	 * @return
	 * @throws Exception
	 */
	public GeoTessPosition getGeoTessPosition(SiteInterface site, String phase, String attribute) throws Exception {
		return getGeoTessPosition(getHandle(site, phase, attribute));
	}


	public InterpolatorType getPathCorrInterpolatorTypeHorizontal() {
		return interpTypeHorz;
	}

	public InterpolatorType getPathCorrInterpolatorTypeRadial() {
		return interpTypeRadial;
	}

	public String getPathCorrRootDirectory() throws IOException {
		return rootDirectory.getCanonicalPath();
	}

	public File getModelFile(SiteInterface station, String phase, String attribute) throws IOException {
		return getModelFile(getHandle(station, phase, attribute));
	}

	public File getModelFile(int handle) throws IOException {
		return handle < 0 ? null : modelFiles.get(handle);
	}

	public ArrayList<File> getModelFiles() throws IOException {
		return modelFiles;
	}

	static public String getVersion() {
		return Utils.getVersion("geo-tess-java");
	}

	/**
	 * Map from SiteInterface -> phase -> attribute -> ModelInfo object.
	 * @return supportMap
	 */
	public Map<SiteInterface, Map<String, Map<String, Integer>>> getSupportMap() {
		return supportMap;
	}

	/**
	 * A Set&lt;String&gt; containing the names of all the unique attributes
	 * supported by this instance of LibCorr3D.
	 * @return
	 */
	public Set<String> getSupportedAttributes() {
		return supportedAttributes;
	}

	/**
	 * A Set containing the names of all the unique phases
	 * supported by this instance of LibCorr3D.
	 * @return
	 */
	public Set<String> getSupportedPhases() {
		return supportedPhases;
	}

	/**
	 * Is the specified station/phase/attribute supported by LibCorr3DModel
	 * 
	 * @param site
	 * @param phase
	 * @param attribute
	 * @return
	 * @throws Exception 
	 */
	public boolean isSupported(SiteInterface site, String phase, String attribute) throws Exception {
		return getHandle(site, phase, attribute) >= 0;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null || ! (other instanceof LibCorr3D)) return false;
		if (other == this) return true;
		return ((LibCorr3D)other).configurationString.equals(this.configurationString);
	}

	@Override
	public int hashCode() {
		return configurationString.hashCode();
	}

	@Override
	public String toString() { return stringRepresentation == null ? "" : stringRepresentation;	}

	public boolean isEmpty() {
		return models == null || models.isEmpty();
	}

	public String getRelGridPath() {
		return relGridPath;
	}

	/**
	 * Retrieve the number of models supported by this library.
	 * Includes models not currently in memory.
	 * @return
	 */
	public int getNModels() { return models.size(); }

	/**
	 * Retrieve the number of models currently loaded in memory.
	 * @return
	 */
	public int getNModelsInMemory() {
		int n=0;
		for (int i=0; i<models.size(); ++i)
			if (models.get(i) != null)
				++n;
		return n;
	}

	/**
	 * Find all the files in the specified directory and all subdirectories
	 * @param dir
	 * @return
	 * @throws Exception 
	 */
	private ArrayList<File> getFiles(File dir, String relativePathToGrid) throws Exception
	{
		ArrayList<File> files = new ArrayList<File>(200);
		getFiles(dir, relativePathToGrid, files);
		return files;
	}

	/**
	 * Find all the files in the specified directory and all subdirectories
	 * and add them to the list of files.
	 * @param f
	 * @param files
	 * @throws Exception 
	 */
	private void getFiles(File f, String relativePathToGrid, List<File> files) throws Exception
	{
		if (f.isDirectory())
			for (File ff : f.listFiles())
				getFiles(ff, relativePathToGrid, files);
		else 
			if (f.isFile() && GeoTessModel.getClassName(f, relativePathToGrid).equals("LibCorr3DModel"))
				files.add(f);
	}

	/**
	 * This map is populated with Sites for which the user has requested models,
	 * not Sites that are represented in the libcorr3d models.  For that,
	 * see getSupportedModelSites().
	 * @return
	 */
	public Map<String, List<SiteInterface>> getSupportedUserSites() {
		Map<String, List<SiteInterface>> supportedSites = new TreeMap<>();
		for (SiteInterface site : supportMap.keySet())
			if (!supportMap.get(site).isEmpty()) {
				List<SiteInterface> siteList = supportedSites.get(site.getSta());
				if (siteList == null)
					supportedSites.put(site.getSta(), siteList = new ArrayList<>());
				siteList.add(site);
			}

		// for each sta, sort the sites by ondate decreasing
		for (List<SiteInterface> sites : supportedSites.values())
			Collections.sort(sites, new Comparator<SiteInterface>() {
				@Override public int compare(SiteInterface o1, SiteInterface o2) {
					return (int) Math.signum(o2.getOndate() - o1.getOndate());
				}});
		return supportedSites;
	}

	/**
	 * This returns the Sites that are represented in the libcorr3d models, 
	 * not Sites for which user has requested models.  For that,
	 * see getSupportedUserSites().
	 * @return
	 */
	public Collection<SiteInterface> getSupportedModelSites() {
		return modelInfoMap.keySet();
	}

	/**
	 * If rootDirectory has a file called prediction_model.geotess and 
	 * a directory called libcorr3d_delta_ak135, then returns a File
	 * that points to rootDirectory/libcorr3d_delta_ak135, 
	 * otherwise returns rootDirectory.
	 * @param rootDirectory
	 * @return
	 * @throws Exception if rootDirectory does not exist or is not a directory
	 */
	private File checkRootDirectory(File rootDirectory) throws Exception {
		if (!rootDirectory.exists())
			throw new IOException(String.format(
					"%nLibCorr3D library libcorrRootDirectory does not exist%n%s%n", rootDirectory.getPath()));

		if (!rootDirectory.isDirectory())
			throw new IOException(String.format(
					"%nLibCorr3D library libcorrRootDirectory is not a directory%n%s%n", rootDirectory.getPath()));

		File prediction_model = new File(rootDirectory, "prediction_model.geotess");
		if (prediction_model.exists()) {
			rootDirectory = new File(rootDirectory, "libcorr3d_delta_ak135");
			if (!rootDirectory.exists())
				throw new IOException(String.format(
						"%nLibCorr3D library libcorrRootDirectory does not exist%n%s%n", rootDirectory.getPath()));

			if (!rootDirectory.isDirectory())
				throw new IOException(String.format(
						"%nLibCorr3D library libcorrRootDirectory is not a directory%n%s%n", rootDirectory.getPath()));
		}

		return rootDirectory;

	}

	public double getMaxSiteSeparationKm() {
		return maxSiteSeparationKm;
	}

	public void setMaxSiteSeparationKm(double maxSiteSeparationKm) {
		this.maxSiteSeparationKm = maxSiteSeparationKm;
	}

	public boolean isMatchOnRefsta() {
		return matchOnRefsta;
	}

	public void setMatchOnRefsta(boolean matchOnRefsta) {
		this.matchOnRefsta = matchOnRefsta;
	}

	public int getMaxModels() {
		return maxModels;
	}

	public void setMaxModels(int maxModels) throws Exception {
		while (modelStack.size() > this.maxModels) {
			// get index of least recently used model
			Integer i = modelStack.removeLast();
			models.get(i).close();
			models.set(i, null);
		}
		this.maxModels = maxModels;
	}

	public InterpolatorType getInterpTypeHorz() {
		return interpTypeHorz;
	}

	public void setInterpTypeHorz(InterpolatorType interpTypeHorz) {
		this.interpTypeHorz = interpTypeHorz;
	}

	/**
	 * Call close() on all the GeoTessModels currently in memory.
	 * It is important to do this so that the GeoTessGrids stored in GeoTessModel.reuseGridMap
	 * will be released for garbage collection.
	 * @throws Exception
	 */
	public void close() throws Exception {
		for (int i=0; i<models.size(); ++i) 
			if (models.get(i) != null) 
				models.get(i).close();
	}

}
