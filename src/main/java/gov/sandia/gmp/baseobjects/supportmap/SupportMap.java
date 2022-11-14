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
package gov.sandia.gmp.baseobjects.supportmap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Site;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.Schema;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.SiteExtended;

/**
 * This class establishes an association between a set of LibCorr3D models and
 * all the site information stored in a site table. The problem being addressed
 * is that a LibCorr3D model is constructed for a specific station and the model
 * is identified by sta-ondate. When the models are going to be used by a
 * locator, the locator is working with arrivals that are joined to a specific
 * site table. It is sometimes the case that the values stored in the model can
 * be correctly be applied to site information stored the site table that has
 * different sta-ondates than the sta-ondates that represent the model. This
 * class resolves this link. Basically, it loads the station information from
 * all the models stored in a directory full of LibCorr3D models. It also loads
 * all the site information from a site table. Then it establishes a
 * relationship from every model to a set of stations to which the model
 * information can be applied. This association is made based on either the
 * refsta of the model and site entries, or based on the distance, measured in
 * km, between the station information stored in the model and the station
 * information from the site table. It writes all this information out to a flat
 * file that is written to a specified outputFile.
 * 
 * @author sballar
 * 
 */
public class SupportMap
{

    private String siteSourceInfo;

    private int verbosity;

    static private String eProp = 
	    "\n\n# Expecting a properties file in which the following properties are specified:\n"
		    + "\n"
		    + "# verbosity level.  0: no output. bigger numbers, more output\n"
		    + "verbosity = 1\n"
		    + "\n"
		    + "# the directory where the LibCorr3DModels are located\n"
		    + "libcorr3dDirectory = /Users/username/LocOO3D/libcorr3d_models_tt_delta_ak135\n"
		    + "\n"
		    + "libcorr3dRelativeGridPath = .\n"
		    + "\n"
		    + "# outputFile.  If ommitted or empty, defaults to libcorr3dDirectory/_supportMap.txt\n"
		    + "outputFile = \n"
		    + "\n"
		    + "# maxSeparationKm in km. default is 10 km\n"
		    + "maxSeparationKm = 10\n"
		    + "\n"
		    + "# associateByRefsta; if true stations with same refsta are associated regardless of maxSeparationKm\n"
		    + "associateByRefsta = false\n"
		    + "\n"
		    + "# site information can be loaded from an oracle database (oracle), or from a text file (file)\n"
		    + "dataLoaderType = file\n"
		    + "\n"
		    + "# if dataLoaderType = file, then this is the file that contains the site information\n"
		    + "dataLoaderFileInputSites = /Users/username/LocOO3D/Examples/Data/sites.txt\n"
		    + "\n"
		    + "# if dataLoaderType = oracle then specify database information\n"
		    + "dbInputInstance = jdbc:oracle:thin:@domain:port:database\n"
		    + "dbInputUserName = username\n"
		    + "#dbInputPassword = \n"
		    + "\n"
		    + "dbInputSiteTable = schema.site_table\n"
		    + "\n"
		    + "#where clause to limit the sites returned from the site table\n"
		    + "# e.e. where sta in ('STA1', 'STA2')\n"
		    + "dbInputSiteWhereClause = \n";


    public static String getVersion() {
	return Utils.getVersion("base-objects");
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
	System.out.printf("SupportMap %s%n%n", getVersion());

	if (args.length != 1 || args[0].startsWith("-"))
	{
	    System.out.println(eProp);
	    System.exit(1);
	}

	try
	{
	    run(args[0]);
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
    }

    static public void run(String propertiesFile) throws Exception {
	(new SupportMap()).run (new PropertiesPlusGMP(new File(propertiesFile)));
    }

    public void run(PropertiesPlusGMP properties) throws Exception
    {
	verbosity = properties.getInt("verbosity", 1);

	if (verbosity > 1)
	    System.out.println(properties.toString());

	ArrayList<Site> stations = loadSites(properties);

	ArrayList<SupportMapModel> models = loadModels(properties);

	double maxSeparationKm = properties.getDouble("maxSeparationKm", 10);

	boolean associateByRefsta = properties.getBoolean("associateByRefsta",
		true);

	if (verbosity > 1)
	    System.out.printf(
		    "associateByRefsta = %b%nmaxSeparationKm = %1.2f%n%n",
		    associateByRefsta, maxSeparationKm);

	// map from Site -> phase -> attribute -> Model
	TreeMap<Site, TreeMap<String, TreeMap<String, SupportMapModel>>> stationMap = 
		new TreeMap<Site, TreeMap<String, TreeMap<String, SupportMapModel>>>();

	// map from phase -> attribute -> Model
	TreeMap<String, TreeMap<String, SupportMapModel>> phaseMap;
	// map from attribute -> Model
	TreeMap<String, SupportMapModel> attributeMap;

	TreeSet<String> siteSta = new TreeSet<String>();
	TreeSet<String> modelSta = new TreeSet<String>();

	TreeSet<SupportMapModel> includedModels = new TreeSet<>();

	// map from Site -> phase -> attribute -> Model
	for (Site station : stations)
	{
	    phaseMap = stationMap.get(station);
	    if (phaseMap == null)
		stationMap.put(station, phaseMap = new TreeMap<String, TreeMap<String, SupportMapModel>>());

	    for (SupportMapModel model : models)
	    {
		if (getDistance(model, station, associateByRefsta) < maxSeparationKm)
		{
		    for (String phase : model.supportedPhases)
		    {
			attributeMap = phaseMap.get(phase);
			if (attributeMap == null)
			    phaseMap.put(phase, attributeMap = new TreeMap<String,SupportMapModel>());

			for (String attribute : model.attributes)
			{
			    SupportMapModel m = attributeMap.get(attribute);
			    if (m == null || getDistance(model, station, false) < getDistance(m, station, false))
			    {
				attributeMap.put(attribute, model);
				siteSta.add(station.getSta());
				modelSta.add(model.station.getSta());
				includedModels.add(model);
			    }
			}
		    }
		}
	    }
	}

	File modelDir = properties.getFile("libcorr3dDirectory");
	Path modelPath = Paths.get(modelDir.getCanonicalPath());

	File outputFile = new File(modelDir, "_supportMap.txt");

	String property = properties.getProperty("outputFile");

	if (property != null && property.length() > 0)
	    outputFile = properties.getFile("outputFile");

	if (verbosity > 1) 
	    System.out.printf("Writing output file %s%n", outputFile);


	BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
	output.write(String.format("# Generated by SupportMap.%s %s %s%n",
		getVersion(), System.getProperty("user.name", "???"), GMTFormat.getNow()));
	
	output.write(String.format("# LibCorr3D model directory: %s%n",
		modelDir.getCanonicalPath()));

	output.write(siteSourceInfo);

	output.write(String.format("# associateByRefsta = %b%n"
		+ "# maxSeparationKm = %1.2f%n",
		associateByRefsta, maxSeparationKm));

	output.write(String.format("# %d of %d sites included in map (%1.2f%%)%n", siteSta.size(), stations.size(),
		100.0*siteSta.size()/stations.size()));

	output.write(String.format("# %d of %d models included in map (%1.2f%%)%n", modelSta.size(), includedModels.size(),
		100.0*modelSta.size()/includedModels.size()));

	StringBuffer buf = new StringBuffer();
	for (String sta : siteSta)
	    buf.append(","+sta);
	output.write(String.format("# Supported Sites: %s%n", (buf.length() > 1 ? buf.toString().substring(1) : "")));

	buf.setLength(0);
	for (String sta : modelSta)
	    buf.append(","+sta);
	output.write(String.format("# Supported Model Sites: %s%n", (buf.length() > 1 ? buf.toString().substring(1) : "")));

	output.write(String.format(
		"%-29s %-6s %-6s %7s %7s %13s %14s %9s %-6s %s%n", "# model",
		"sta", "refsta", "ondate", "offdate", "lat", "lon", "elev",
		"ph", "attribute"));

	// map from Site -> phase -> attribute -> Model
	for (Entry<Site, TreeMap<String, TreeMap<String, SupportMapModel>>> stationEntry : stationMap.entrySet())
	{
	    Site site = stationEntry.getKey();
	    phaseMap = stationEntry.getValue();
	    for (Entry<String, TreeMap<String, SupportMapModel>> phaseEntry : phaseMap.entrySet())
	    {
		String phase = phaseEntry.getKey();
		attributeMap = phaseEntry.getValue();
		for (Entry<String, SupportMapModel> attributeEntry : attributeMap.entrySet())
		{
		    String attribute = attributeEntry.getKey();
		    SupportMapModel model = attributeEntry.getValue();

		    // model sta refsta ondate offdate lat lon elev ph attribute
		    output.write(String.format("%-29s %-6s %-6s %7d %7d %13.6f %14.6f %9.3f %-6s %s%n", 
			    model.getRelativePath(modelPath), 
			    site.getSta(), site.getRefsta(), site.getOndate(), site.getOffdate(), site.getLat(),
			    site.getLon(), site.getElev(), 
			    phase, attribute));
		}
	    }
	}


	output.close();

	if (verbosity > 0) {
	    System.out.print(String.format("%d of %d sites included in support map (%1.2f%%)%n", siteSta.size(), stations.size(),
		    100.0*siteSta.size()/stations.size()));

	    System.out.print(String.format("%d of %d models included in support map (%1.2f%%)%n", modelSta.size(), includedModels.size(),
		    100.0*modelSta.size()/includedModels.size()));

	    System.out.printf("Output written to file %s%n%n",
		    outputFile.getCanonicalPath());
	}

	if (verbosity > 1)
	    System.out.println(properties.getRequestedPropertiesString(false));

	if (verbosity > 0) 
	    System.out.println("Done.");

    }

    /**
     * How close a model station and another station are to each other.
     * If associateByRefsta is true and the stations have the same refsta, returns 0
     * Otherwise, returns the epicentral separation of the stations in km.
     * @param model
     * @param station
     * @param associateByRefsta
     * @return
     */
    private double getDistance(SupportMapModel model, Site station,
	    boolean associateByRefsta)
    {
	// compute distance from this station to this model.
	if (associateByRefsta && !station.getRefsta().equals(Site.REFSTA_NA)
		&& !model.station.getRefsta().equals(Site.REFSTA_NA)
		&& station.getRefsta().equals(model.station.getRefsta()))
	    return 0.;
	else
	    return VectorUnit.angle(model.station.getUnitVector(),
		    station.getUnitVector())
		    * station.getRadius();
    }

    /**
     * Interrogate all the models in the libcorr3d directory and extract
     * site, phase and attribute information. 
     * @param properties
     * @return
     * @throws Exception
     */
    private ArrayList<SupportMapModel> loadModels(PropertiesPlus properties)
	    throws Exception
    {
	File modelDir = properties.getFile("libcorr3dDirectory");
	if (modelDir == null)
	    throw new PropertiesPlusException(
		    "\nProperty libcorr3dDirectory is not specified in the property file.\n");

	if (!modelDir.exists())
	    throw new PropertiesPlusException(String.format(
		    "\nDirectory %s does not exist.\n",
		    modelDir.getCanonicalPath()));

	if (!modelDir.isDirectory())
	    throw new PropertiesPlusException(String.format(
		    "\nProperty libcorr3dDirectory = %s is not a directory.\n",
		    modelDir.getCanonicalPath()));
	
	// see if this is a model definition directory
	if (new File(modelDir, "prediction_model.geotess").exists()) {
	    File mdir = new File(modelDir, "libcorr3d_delta_ak135");
	    if (!mdir.exists())
		throw new Exception(modelDir.getAbsolutePath()+"\nappears to be a model definition directory \n"
			+ "but does not contain a libcorr3d_delta_ak135 subdirectory.");
	    modelDir = mdir;
	}

	String relativeGridPath = properties.getProperty("libcorr3dRelativeGridPath", ".");

	if (verbosity > 1)
	    System.out.printf("%nExtracting information about the available models from %s:%n", modelDir);

	ArrayList<File> files = new ArrayList<File>();
	getFiles(modelDir, files);
	Collections.sort(files);

	ArrayList<SupportMapModel> models = new ArrayList<SupportMapModel>();

	for (File file : files)
	    if (GeoTessModel.getClassName(file).equals("LibCorr3DModel"))
		models.add(new SupportMapModel(models.size(), file, relativeGridPath));

	if (verbosity > 0)
	    System.out.printf(
		    "Loaded information about %d LibCorr3D models from %s%n",
		    models.size(), modelDir.getCanonicalPath());

	return models;
    }

    /**
     * Find all the files in the specified directory and all subdirectories
     * and add them to the list of files.
     * @param dir
     * @param files
     */
    private void getFiles(File dir, List<File> files)
    {
	if (dir.isDirectory())
	    for (File f : dir.listFiles())
		getFiles(f, files);
	else if (dir.isFile())
	    files.add(dir);

    }

    private ArrayList<Site> loadSites(PropertiesPlusGMP properties)
	    throws Exception
    {
	String dataLoaderType = properties.getProperty("dataLoaderType", "oracle");

	if (dataLoaderType.equalsIgnoreCase("oracle"))
	    return loadSitesDB(properties);
	else if (dataLoaderType.equalsIgnoreCase("file"))
	    return loadSitesFile(properties);
	else
	    throw new Exception("Property dataLoaderType = "+dataLoaderType
		    +" but must be one of [ oracle | file ]");

    }

    private ArrayList<Site> loadSitesFile(PropertiesPlusGMP properties) throws Exception {

	gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site.getColumns().setColumnRequiredStatus(
		gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site.getColumns().getColumnNames(), false);

	String tokenDelimiter = properties.getProperty("dataLoaderFileInputTokenDelimiter", "");
	if (!tokenDelimiter.equals(" "))
	    BaseRow.setTokenDelimiter(tokenDelimiter);

	File siteFile = properties.getFile("dataLoaderFileInputSites");

	siteSourceInfo = String.format("# Site File = %s%n", siteFile.getCanonicalPath());

	Set<gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site> sites = 
		gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site.readSites(siteFile);

	ArrayList<Site> stations = new ArrayList<Site>(sites.size());

	for (gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site site : sites)
	    stations.add(new Site(site.getSta(), site.getOndate(), site.getOffdate(), site.getLat(), site.getLon(), site.getElev(),
		    site.getStaname(), site.getStatype(), site.getRefsta(),site.getDnorth(), site.getDeast()));

	if (verbosity > 0)
	    System.out.printf("Loaded information about %d site rows from file %s%n",
		    stations.size(), siteFile.getCanonicalPath());

	return stations;
    }

    private ArrayList<Site> loadSitesDB(PropertiesPlusGMP properties)
	    throws Exception
    {
	ArrayList<Site> stations = new ArrayList<Site>(200);

	Schema schema = new Schema("dbInput", properties, false);

	String siteTableQuery = properties.getProperty("dbInputSiteSelectStatement");

	if (siteTableQuery == null)
	{

	    siteTableQuery = "select * from " + schema.getTableName("site");

	    String where = properties.getProperty("dbInputSiteWhereClause");
	    if (where != null && where.length() > 0)
	    {
		if (!where.toLowerCase().startsWith("where "))
		    siteTableQuery += " where " + where;
		else
		    siteTableQuery += " " + where;
	    }
	}

	siteSourceInfo = String.format("# database = %s%n"
		+ "# site table = %s%n"
		+ "# site table query: %s%n", schema.getInstance(), schema.getTableName("site"), siteTableQuery);

	for (gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site site : SiteExtended.readSites(schema.getConnection(), siteTableQuery))
	    stations.add(new Site(site.getSta(), site.getOndate(), site.getOffdate(), site.getLat(), 
		    site.getLon(), site.getElev(), site.getStaname(), site.getStatype(), site.getRefsta(),
		    site.getDnorth(), site.getDeast()));


	if (verbosity > 0)
	    System.out.printf("Loaded information about %d site rows from database%n",
		    stations.size());

	return stations;
    }

    //	/**
    //	 * Returns a huge string where each record specifies: <br>
    //	 * model file name, station information, phase, attribute name.
    //	 * 
    //	 * @param modelMap
    //	 *            map from model object -> set of stations associated with that
    //	 *            model.
    //	 * @return
    //	 */
    //	private String toString(MultiLevelMap modelMap, ArrayList<Model> models, ArrayList<Site> stations)
    //	{
    //		TreeSet<String> records = new TreeSet<String>();
    //		
    //		for (Site station : stations)
    //			for (Model model : models)
    //				for (String attribute : model.attributes)
    //				{
    //					Integer index = modelMap.getIndex(station, model.phase, attribute);
    //					if (index > 0)
    //						records.add(String.format("%-29s %s %-3s %s%n",
    //									model.file.getName(), station, model.phase,
    //									attribute));
    //				}
    //		
    //		StringBuffer buf = new StringBuffer();
    //		for (String record : records)
    //			buf.append(record);
    //
    //		return buf.toString();
    //	}

}
