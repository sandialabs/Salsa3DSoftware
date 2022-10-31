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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.globals.Site;
import gov.sandia.gmp.util.globals.SiteInterface;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * LibCorr3D manages a collection of LibCorr3DModels.  Its most important 
 * function is to establish a link between a Site/phase/attribute the caller in interested in  
 * and a particular LibCorr3DModel that can support that Site/phase/attribute.
 * @author sballar
 *
 */
public class LibCorr3D
{
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
    private ArrayList<File> modelFiles;

    /**
     * An array of all the unique models that are available in <code>rootDirectory</code>.  
     * The dimensions of the <code>models</code> array and the <code>modelFiles</code> array
     * are the same.  Not all models necessarily reside in memory at once.  Models that have
     * not been recently referenced may be deleted from memory to satisfy the <code>maxModels</code>
     * constraint.
     */
    private ArrayList<LibCorr3DModel> models;

    /**
     * Map from SiteInterface -> phase -> attribute -> model index.
     * There is an entry in this map for all unique site-phase-attribute in the _supportMap file. 
     */
    private Map<SiteInterface, Map<String, Map<String, Integer>>> supportMap;

    /**
     * A stack containing the indices of the libcorr models, in order of
     * how recently the model was requested by a call to method <code>getModel(modelIndex)</code>.  
     * The first element is the index of the model most recently requested
     * by a call to method <code>getModel(modelIndex)</code>.  
     * When the size of the stack exceeds <code>maxModels</code>, the last modelIndex in <code>modelStack</code> is
     * popped off the end of the stack and the corresponding model in the <code>models</code> 
     * array is set to null (removed from memory). 
     */
    private LinkedList<Integer> modelStack;

    /**
     * Maximum number of models that can be in memory at one time.  
     */
    private int maxModels;

    private StringBuffer logger;

    /**
     * If the model grids reside outside the models, then this is the 
     * relative path from the model directory to the grid directory
     */
    private String relGridPath;

    /**
     * InterpolatorType.LINEAR or InterpolatorType.NATURAL_NEIGHBOR.
     */
    private InterpolatorType interpTypeHorz;

    /**
     * InterpolatorType.LINEAR.
     */
    private InterpolatorType interpTypeRadial;

    /**
     * If <code>preloadModels</code> is true, then all the LibCorr3D models in <code>rootDirectory</code> will
     * be loaded into memory in the LibCorr3D constructor, subject to the limitations
     * imposed by <code>maxModels</code>.
     */
    private boolean preloadModels;

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
	attributeTranslationMap.put("SLOWNESS_MODEL_UNCERTAINTY", "SH");
    }

    /**
     * Map from station name to a list of sites that have that name but
     * differ by ondate.  The keys in the map are in alphabetical order
     * and the List of Sites is sorted by decreasing ondate.
     */
    private Map<String, List<SiteInterface>> supportedSites;

    /**
     * A set containing the unique set of attributes available.  
     * The attributes in this set are attributes that the user might call, not
     * the attributes specified in the _supportMap File. I.e., the attributes that
     * are values in the <code>attributeTranslationMap</code>
     */
    private Set<String> supportedAttributes;

    /**
     * A unique string that identifies this instance of LibCorr3D.  It is 
     * a concatenation of all the properties used to construct this instance.
     */
    private String configurationString;

    /**
     * A Map from <code>configurationString</code> to LibCorr3D instance used to 
     * prevent instantiating new instances of LibCorr3D when an identical instance
     * is already availble in memory.
     */
    static private Map<String, LibCorr3D> libcorr3dMap = 
	    new ConcurrentHashMap<String, LibCorr3D>();

    static public Map<String, LibCorr3D> getConfigurationMap() { return libcorr3dMap; }

    /**
     * Map from gridID to number of models currently in memory that reference the 
     * corresponding grid.  When models are loaded from file, the gridCount of their
     * grid is incremented.  When models are removed from memory, the gridCound is
     * decremented.
     * 
     * <p>There is always an entry for every grid referenced by any model supported by
     * this libcorr3d library, some of which may be zero.
     */
    private Map<String, Integer> gridCount;
    
    public static boolean TESTING = false;

    /**
     * Retrieve an instance of LibCorr3D.  If an instance with the same properties
     * is already available in memory, a reference to the existing instance is returned.
     * Otherwise, a new instance is instantiated and returned.
     * @param rootDirectory
     * @param supportMapFile
     * @param relGridPath
     * @param interpTypeHorz
     * @param interpTypeRadial
     * @param preloadModels
     * @param maxModels
     * @return
     * @throws Exception
     */
    static public LibCorr3D getLibCorr3D(File rootDirectory, File supportMapFile, String relGridPath,
	    InterpolatorType interpTypeHorz, InterpolatorType interpTypeRadial, 
	    boolean preloadModels, int maxModels) throws Exception
    {
	if (supportMapFile == null)
	    supportMapFile = new File(rootDirectory, "_supportMap.txt");
	String configurationString = String.format(
		"libcorrRootDirectory = %s; "
			+ "libcorrSupportMapFile = %s; "
			+ "libcorrRelativeGridPath = %s; "
			+ "libcorrInterpolatorTypeHorizontal = %s; "
			+ "libcorrInterpolatorTypeRadial = %s; "
			+ "libcorrPreloadModels = %b; "
			+ "libcorrMaxModels = %d",
			rootDirectory.getCanonicalPath(),
			supportMapFile.getCanonicalPath(),
			relGridPath,
			interpTypeHorz.toString(), interpTypeRadial.toString(),
			preloadModels, (maxModels <= 0 ? Integer.MAX_VALUE : maxModels));

	LibCorr3D model = libcorr3dMap.get(configurationString);
	if (model == null)
	{
	    model = new LibCorr3D(rootDirectory, supportMapFile, relGridPath, interpTypeHorz, interpTypeRadial, preloadModels, maxModels);
	    model.configurationString = configurationString;
	    libcorr3dMap.put(configurationString, model);
	}

	return model;
    }

    /**
     * Retrieve an empty instance of LibCorr3D. All requests submitted to this instance
     * will return NA values.
     * @return
     */
    static public LibCorr3D getLibCorr3D()
    {
	String configurationString = "empty";

	LibCorr3D model = libcorr3dMap.get(configurationString);

	if (model != null)
	    return model;

	model = new LibCorr3D();

	model.configurationString = configurationString;

	libcorr3dMap.put(configurationString, model);

	return model;
    }

    /**
     * Retrieve an instance of LibCorr3D.  If an instance with the same properties
     * is already available in memory, a reference to the existing instance is returned.
     * Otherwise, a new instance is instantiated and returned.
     * @param properties
     * @return
     * @throws Exception
     */
    static public LibCorr3D getLibCorr3D(PropertiesPlus properties) throws Exception
    {
	return getLibCorr3D(properties.getFile("libcorrRootDirectory"),
		properties.getFile("libcorrSupportMapFile", 
			new File(properties.getFile("libcorrRootDirectory"), "_supportMap.txt")),
		properties.getProperty("libcorrRelativeGridPath"),
		InterpolatorType.valueOf(properties.getProperty("libcorrInterpolatorTypeHorizontal", "LINEAR")),
		InterpolatorType.valueOf(properties.getProperty("libcorrInterpolatorTypeRadial", "LINEAR")),
		properties.getBoolean("libcorrPreloadModels", Boolean.TRUE),
		properties.getInt("libcorrMaxModels", Integer.MAX_VALUE)
		);
    }

    /**
     * Retrieve an instance of LibCorr3D.  If an instance with the same properties
     * is already available in memory, a reference to the existing instance is returned.
     * Otherwise, a new instance is instantiated and returned.
     * @param prefix
     * @param properties
     * @return
     * @throws Exception
     */
    static public LibCorr3D getLibCorr3D(String prefix, PropertiesPlus properties) throws Exception {
	return getLibCorr3D(properties.getFile(prefix+"LibCorrPathCorrectionsRoot"),
		properties.getFile(prefix+"LibCorrPathCorrectionsSupportMapFile", 
			new File(properties.getFile(prefix+"LibCorrPathCorrectionsRoot"), "_supportMap.txt")),
		properties.getProperty(prefix+"LibCorrPathCorrectionsRelativeGridPath", "."),
		InterpolatorType.valueOf(properties.getProperty(prefix+"LibcorrInterpolatorTypeHorizontal", "LINEAR")),
		InterpolatorType.valueOf(properties.getProperty(prefix+"LibcorrInterpolatorTypeRadial", "LINEAR")),
		properties.getBoolean(prefix+"LibcorrPreloadModels", Boolean.TRUE),
		properties.getInt(prefix+"LibcorrMaxModels", Integer.MAX_VALUE)
		);
    }

    /**
     * Private constructor that returns an empty instance of LibCorr3D
     */
    private LibCorr3D()
    {
	this.rootDirectory = new File("");
	this.relGridPath = ".";
	this.interpTypeHorz = InterpolatorType.LINEAR;
	this.interpTypeRadial = InterpolatorType.LINEAR;
	this.preloadModels = false;
	this.maxModels = Integer.MAX_VALUE;

	models = new ArrayList<>();
	modelFiles = new ArrayList<File>();
	modelStack = new LinkedList<Integer>();
	gridCount = new HashMap<>(); 
	supportMap = new HashMap<>();
	logger = new StringBuffer();
	supportedSites = new TreeMap<>();
	supportedAttributes = new LinkedHashSet<>();;
    }

    /**
     * Private constructor
     * @param rootDirectory
     * @param supportMapFile
     * @param relGridPath
     * @param interpTypeHorz
     * @param interpTypeRadial
     * @param preloadModels
     * @param maxModels
     * @throws Exception
     */
    private LibCorr3D(File aRootDirectory, File supportMapFile, String aRelGridPath,
	    InterpolatorType aInterpTypeHorz, InterpolatorType aInterpTypeRadial, 
	    boolean preload, int nModels) throws Exception
    {
	this();

	this.rootDirectory = aRootDirectory;
	this.relGridPath = aRelGridPath;
	this.interpTypeHorz = aInterpTypeHorz;
	this.interpTypeRadial = aInterpTypeRadial;
	this.preloadModels = preload;
	if (nModels <= 0) nModels = Integer.MAX_VALUE;
	this.maxModels = nModels;

	if (!rootDirectory.exists())
	    throw new IOException(String.format(
		    "%nrootPath does not exist%n%s%n", rootDirectory.getPath()));

	long timer = System.nanoTime();

	Path dirPath = Paths.get(rootDirectory.getCanonicalPath());

	if (!supportMapFile.exists())
	{
	    logger.append("LibCorr3DModels is generating default _supportMap.txt file.\n");

	    // load all the LibCorr3DModel files and extract station, phase, attribute
	    // information
	    ArrayList<File> files = new ArrayList<File>(500);
	    discoverFiles(rootDirectory, files);
	    for (File modelFile : files)
	    {
		LibCorr3DModel model = new LibCorr3DModel(modelFile, relGridPath);

		String gridID = model.getGrid().getGridID();

		if (!gridCount.containsKey(gridID))
		    gridCount.put(gridID, 0);


		// must ensure that models array and modelFiles array are always
		// the same size and that the range 'index' is zero to models.size()-1
		int modelIndex = modelFiles.size();

		modelFiles.add(modelFile);

		if (preloadModels && modelStack.size() < this.maxModels)
		{
		    modelStack.addLast(modelIndex);
		    models.add(model);
		    gridCount.put(gridID, gridCount.get(gridID)+1);
		}
		else
		    models.add(null);

		String attribute = model.getMetaData().getAttributeName(0);

		if (attributeTranslationMap.containsKey(attribute))
		    attribute = attributeTranslationMap.get(attribute);

		supportedAttributes.add(attribute);

		// populate supportEntries with the modelIndex and attributeIndex 
		// that supports each station-phase-attribute
		// Also populate supportMap with the supporEntry that will support
		// the station-phase-attribute
		for (String phase : model.getSupportedPhases())
		    if (getHandle(model.getSite(), phase, attribute) < 0)
			setHandle(model.getSite(), phase, attribute, modelIndex);

	    }

	    timer = System.nanoTime() - timer;

	    if (preloadModels)
		logger.append(String.format("LibCorr3DModels constructor: loaded %d models in %1.3f seconds%n",
			models.size(), timer * 1e-9));
	    else
		logger.append(String.format("LibCorr3DModels constructor: analyzed %d models in %1.3f seconds%n",
			models.size(), timer * 1e-9));

	    // write the supportMap.txt file
	    try
	    {
		Writer output = new BufferedWriter(new FileWriter(supportMapFile));
		output.write(String.format("# Generated automatically by LibCorr3DModels.java %s %s\n",
			System.getProperty("user.name", "???"), GMTFormat.getNow()));
		output.write(String.format("# LibCorr3D model directory: %s\n", rootDirectory.getCanonicalPath()));
		output.write("# Every LibCorr3D model is associated only to a station corresponding to the station used to generate the model. \n");
		output.write(String.format("%-29s %-6s %-6s %7s %7s %13s %14s %9s %-9s %s%n",
			"# model", "sta", "refsta", "ondate", "offdate", "lat", "lon", "elev", "ph",
			"attribute"));

		ArrayList<String> records = new ArrayList<String>();

		Map<String, String> reverseMap = new HashMap<>();
		for (Entry<String, String> entry : attributeTranslationMap.entrySet())
		    reverseMap.put(entry.getValue(), entry.getKey());

		for (Entry<SiteInterface, Map<String, Map<String, Integer>>> e1 : supportMap.entrySet())
		{
		    SiteInterface site = e1.getKey();
		    for (Entry<String, Map<String, Integer>> e2 : e1.getValue().entrySet())
		    {
			String phase = e2.getKey();
			for (Entry<String, Integer> e3 : e2.getValue().entrySet())
			{
			    Integer modelIndex = e3.getValue();
			    Path modelPath = Paths.get(modelFiles.get(modelIndex).getAbsolutePath());

			    String[] attributes;
			    if (models.get(modelIndex) != null)
				attributes = models.get(modelIndex).getMetaData().getAttributeNames();
			    else
				attributes = new GeoTessMetaData(modelPath.toFile()).getAttributeNames();

			    for (String attribute : attributes)
				records.add(String.format("%-29s %-6s %-6s %7d %7d %13.6f %14.6f %9.3f %-6s %s%n",
					dirPath.relativize(modelPath), 
					site.getSta(), site.getRefsta(), site.getOndate(), site.getOffdate(), site.getLat(),
					site.getLon(), site.getElev(),
					phase, attribute));
			}
		    }
		}
		Collections.sort(records);

		for (String record : records)
		    output.write(record);

		output.close();

		logger.append(String.format("LibCorr3DModels constructor wrote file _supportMap.txt with %d stations and %d records%n",
			supportMap.size(), records.size()));
	    }
	    catch (Exception ex)
	    {
		System.out.println("LibCorr3DModels constructor: writing file _supportMap.txt failed");
	    }
	}
	else
	{
	    // supportFile.exists.

	    File modelFile;
	    SiteInterface site;
	    String phase;

	    // missing models are models that are specified in the
	    // supportMap.txt file but which do not exist in the file system.
	    HashSet<File> missingModels = new HashSet<File>();

	    Scanner input = new Scanner(supportMapFile);

	    while (input.hasNext())
	    {
		String line = input.nextLine().trim();
		if (line.startsWith("#"))
		    continue;
		Scanner record = new Scanner(line);
		//              # model                       sta    refsta  ondate offdate           lat            lon      elev ph        attribute
		//              AAK_P_salsa3_10km.geotess     AAK    AAK       2364 2286324     42.633300      74.494400     1.680 P      TT_DELTA_AK135
		//              AAK_P_salsa3_10km.geotess     AAK    AAK       2364 2286324     42.633300      74.494400     1.680 P      TT_MODEL_UNCERTAINTY
		modelFile = dirPath.resolve(record.next()).toFile();

		String sta = record.next();
		String refsta = record.next();
		long ondate = record.nextLong();
		long offdate = record.nextLong();
		double lat = record.nextDouble();
		double lon = record.nextDouble();
		double elev = record.nextDouble();
		site = new Site(sta, ondate, offdate, lat, lon, elev, Site.STANAME_NA,
			Site.STATYPE_NA, refsta, Site.DNORTH_NA, Site.DEAST_NA);

		phase = record.next().trim();

		String attribute = record.next().trim();
		if (attributeTranslationMap.containsKey(attribute))
		    attribute = attributeTranslationMap.get(attribute);

		supportedAttributes.add(attribute);

		record.close();

		if (!modelFile.exists())
		    missingModels.add(modelFile);
		else // model file exists
		{
		    int modelIndex = modelFiles.indexOf(modelFile);

		    if (modelIndex < 0)
		    {
			modelIndex = modelFiles.size();
			modelFiles.add(modelFile);

			if (preloadModels && modelStack.size() < maxModels)
			{
			    LibCorr3DModel model = new LibCorr3DModel(modelFile, relGridPath);										
			    models.add(model);

			    // if new gridID, put 1 in gridCount, otherwise increment gridCount.
			    // the reason is that this model will remain in memory
			    Integer n = gridCount.get(model.getGridID());
			    gridCount.put(model.getGridID(), 
				    n == null ? 1 : n+1);

			    modelStack.addLast(modelIndex);
			}
			else
			{
			    models.add(null);

			    String gridID = GeoTessModel.getGridID(modelFile);

			    // if new gridID, put 0 in gridCount.  this model will not remain in memory
			    if (!gridCount.containsKey(gridID))
				gridCount.put(gridID, 0);
			}
		    }

		    if (getHandle(site, phase, attribute) < 0)
			setHandle(site, phase, attribute, modelIndex);
		}
	    }
	    input.close();


	    if (missingModels.size() > 0)
	    {
		System.out.println();
		System.out.printf("Problem in LibCorr3DModels.  The following LibCorr3D models were specified in %s%nbut do not exist in the file system:%n",
			supportMapFile.getCanonicalPath());
		for (File f : missingModels)
		    System.out.printf("   %s%n", f.getCanonicalPath());
		System.out.println();
		System.out.println();
	    }

	    logger.append(String.format("Loaded info about %d LibCorr3DModels%n"
		    + "associated with %d sites in the site table%n"
		    + "from file %s%n"
		    + "in %1.3f seconds%n%n",
		    modelFiles.size(), supportMap.size(),
		    supportMapFile.getCanonicalPath(),
		    timer * 1e-9));

	}

	for (SiteInterface site : supportMap.keySet())
	{
	    List<SiteInterface> s = supportedSites.get(site.getSta());
	    if (s == null)
		supportedSites.put(site.getSta(), s = new ArrayList<>());
	    s.add(site);
	}

	for (List<SiteInterface> sites : supportedSites.values())
	    Collections.sort(sites, new Comparator<SiteInterface>() {
		@Override public int compare(SiteInterface o1, SiteInterface o2) {
		    return (int) Math.signum(o2.getOndate() - o1.getOndate());
		}});
    }

    /**
     * Add site->phase->attribute->index to supportMap.  
     * Returns the previous index for site->phase->attribute if there was one
     * otherwise returns null.
     * @param site
     * @param phase
     * @param attribute
     * @param index
     * @return the previous index for site->phase->attribute if there was one
     * otherwise returns null.
     * @throws Exception if attempt is made to add index == null.
     */
    private void setHandle(SiteInterface site, String phase, String attribute, Integer index) throws Exception
    {
	if (index == null)
	    throw new Exception("Cannot add index == null to support map");

	Map<String, Map<String, Integer>> phaseMap = supportMap.get(site);
	if (phaseMap == null)
	    supportMap.put(site, phaseMap=new HashMap<>());
	Map<String, Integer> attributeMap = phaseMap.get(phase);
	if (attributeMap == null)
	    phaseMap.put(phase, attributeMap=new HashMap<>());
	attributeMap.put(attribute, index);
    }

    /**
     * Retrieve the handle for the specified site-phase-attribute combination,
     * or -1 if site-phase-attribute is not supported
     * @param site
     * @param phase
     * @param attribute
     * @return
     */
    public int getHandle(SiteInterface site, String phase, String attribute)
    {
	Map<String, Map<String, Integer>> phaseMap = supportMap.get(site);
	if (phaseMap != null)
	{
	    Map<String, Integer> attributeMap = phaseMap.get(phase);
	    if (attributeMap != null)
	    {
		Integer handle = attributeMap.get(attribute);
		return handle == null ? -1 : handle.intValue();
	    }
	}
	return -1;
    }

    /**
     * Retrieve the model with the specified index (handle).  If the requested
     * model is not currently loaded in memory, it is loaded from disk.  
     * A check is performed to ensure that the number of models currently in 
     * memory does not exceed <code>maxModels</code> and the least-recently used
     * model is deleted if it is.
     * @param modelIndex
     * @return
     * @throws Exception
     */
    synchronized public LibCorr3DModel getModel(int modelIndex) throws Exception
    {
	if (modelIndex < 0 || modelIndex >= models.size())
	    return null;

	LibCorr3DModel model = models.get(modelIndex);

	// if the requested model is not currently loaded in memory, load it.
	if (model == null) {
	    model = new LibCorr3DModel(modelFiles.get(modelIndex), relGridPath);
	    models.set(modelIndex, model);
	    gridCount.put(model.getGridID(), gridCount.get(model.getGridID())+1);
	    if (TESTING) test();
	}

	// now check to see if maximum allowed number of models are currently in memory.
	// If maxModels is >= models.size() then it is permissible for all available models to 
	// to be loaded at the same time and therefore no checks are necessary
	if (maxModels < models.size()) 
	{
	    // remove modelIndex from the stack (if currently there) and push it onto the front
	    Integer i = Integer.valueOf(modelIndex);
	    modelStack.remove(i);
	    modelStack.addFirst(i);
	    // if the stack is full, pop the oldest, least recently used, model 
	    // off the end of the stack and remove the corresponding model from memory.
	    if (modelStack.size() > maxModels) {
		// get index of least recently used model
		i = modelStack.removeLast();
		// find the gridID of least-recently-used model
		String gridID = models.get(i).getGridID();
		// if that grid is not shared with any other models then delete it
		// from GeoTessModel's reuseGridMap
		gridCount.put(gridID, gridCount.get(gridID)-1);
		if (gridCount.get(gridID) == 0)
		    GeoTessModel.getGridMap().remove(gridID);
		// and finally delete the model from memory.
		models.set(i, null);
		if (TESTING) test();
	    }
	}
	return models.get(modelIndex);
    }

    /**
     * Retrieve a GeoTessPosition object for the LibCorr3DModel with the specified
     * handle (modelIndex).  Returns null if handle < 0 or >- <code>models.size()</code>.
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
     * Find all the GeoTessModel Files in the specified directory, and all of its
     * subdirectories. Return all the Files in the supplied array of Files.
     * 
     * @param directory
     * @param files
     * @throws IOException 
     */
    private void discoverFiles(File directory, List<File> files) throws Exception
    {
	if (!directory.exists())
	    throw new IOException(directory.getAbsolutePath()+" does not exist.");

	if (!directory.isDirectory())
	    throw new IOException(directory.getAbsolutePath()+" is not a directory.");

	for (File file : directory.listFiles())
	    if (file.isDirectory())
		discoverFiles(file, files);
	    else if (GeoTessModel.isGeoTessModel(file))
		files.add(file);
    }

    /**
     * Map from SiteInterface -> phase -> index.
     * The index in the models array and the modelFiles array
     * of the model that supports the specified site/phase.
     * @return supportMap
     */
    public Map<SiteInterface, Map<String, Map<String, Integer>>> getSupportMap() {
	return supportMap;
    }

    /**
     * Map from station name to a list of sites that have that name but
     * differ by ondate.  The keys in the map are in alphabetical order
     * and values are lists of Sites sorted by decreasing ondate.
     */
    public Map<String, List<SiteInterface>> getSupportedSites() {
	return supportedSites;
    }

    /**
     * A Set<String> containing the names of all the unique attributes
     * supported by this instance of LibCorr3D.
     * @return
     */
    public Set<String> getSupportedAttributes() {
	return supportedAttributes;
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
    public String toString() {
	return configurationString;
    }

    public boolean isEmpty() {
	return supportMap.size() == 0;
    }

    public String getRelGridPath() {
	return relGridPath;
    }

    public Map<String, Integer> getGridCount() {
	return gridCount;
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
    
    private void test() throws Exception {
	// the sum of all the values in gridCount should equal the number of models in memory.
	int count = 0;
	for (Integer n : gridCount.values()) count += n;
	if (count != getNModelsInMemory())
	    throw new Exception(String.format("sum of gridCount values (%d) != number of models in memory (%d)%n",
		    count, getNModelsInMemory()));
	
	// the number of unique gridIDs should equal GeoTessModel.gridMap .size()
	int nUniqueGridIDs = 0;
	for (Integer n : gridCount.values())
	    if (n > 0) ++nUniqueGridIDs;
	if (nUniqueGridIDs != GeoTessModel.getReuseGridMapSize())
	    throw new Exception(String.format("number of unique gridIDs in gridCount (%d) != "
	    	+ "GeoTessModel.getReuseGridMapSize() (%d)%n",
	    	nUniqueGridIDs, GeoTessModel.getReuseGridMapSize()));
    }

}
