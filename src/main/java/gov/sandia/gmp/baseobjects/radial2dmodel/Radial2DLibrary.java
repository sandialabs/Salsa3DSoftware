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
package gov.sandia.gmp.baseobjects.radial2dmodel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.util.containers.Tuple;
import gov.sandia.gmp.util.globals.GMTFormat;

/**
 * Class manages a whole directory full of Radial2DModels.  
 * Maintains a map from timeintervals (seasons) -> station names -> Radial2DModel.
 * Includes method to retrieve the right Radial2DModel for a given jdate, station name.
 * 
 * <p>There are two implementations of Radial2DModelInterface: Radial2DModelLegacy and Radial2DModelImproved.
 * Legacy implements code that produces the same results as was produced by old C code written by SAIC
 * and was in use at the IDC as of August, 2024.  It suffers from several shortcomings which are rectified
 * in Radial2DModelImproved, which was written by S. Ballard in August 2024.
 */
public class Radial2DLibrary {

	/**
	 * There are two implementations of Radial2DModelInterface: Radial2DModelLegacy and Radial2DModelImproved.
	 * Legacy implements code that produces the same results as was produced by old C code written by SAIC
	 * and was in use at the IDC as of August, 2024.  It suffers from several shortcomings which are rectified
	 * in Radial2DModelImproved, which was written by S. Ballard in August 2024.
	 */
	public static String RADIAL2D_MODEL_CLASS = "Radial2DModelImproved";

	/**
	 * The names of all the models in the library.  These are typically station names.
	 * They are loaded from the file in library directory called 'stations'
	 */
	public ArrayList<String> modelNames;

	/**
	 * The names of all the seasons and the day of year when the season ends.
	 */
	public ArrayList<Tuple<String, Integer>> seasons;

	/**
	 * Map from season -> stationName(modelName) -> Radial2DModel
	 */
	public Map<String, Map<String, Radial2DModel>> models;

	/**
	 * Only constructor is private so forced to use static method getLibrary() to get one.
	 */
	private Radial2DLibrary() { };

	/**
	 * Map from a canonical directory name to a Radial2DLibrary
	 */
	static private Map<String, Radial2DLibrary> libraryMap;

	synchronized static public Map<String, Radial2DLibrary> getLibraryMap() { 
		return libraryMap; 
	}

	/**
	 * Retrieve a Radial2DLibrary object that contains all the models in the specified modelDirectory.
	 * A Radial2DLibrary that corresponds to a particular modelDirectory is only loaded into memory
	 * the first time this method is called with a particular modelDirectory.  Subsequent calls with 
	 * the same modelDirectory return a reference to the previously loaded library.
	 * 
	 * @param modelDirectory
	 * @return a Radial2DLibrary object.
	 * @throws IOException
	 */
	synchronized static public Radial2DLibrary getLibrary(File modelDirectory) throws Exception {
		if (libraryMap == null)
			libraryMap = new ConcurrentHashMap<String, Radial2DLibrary>();

		Radial2DLibrary library = libraryMap.get(modelDirectory.getCanonicalPath());

		if (library == null)
		{
			library = new Radial2DLibrary();

			File stationFile = new File(modelDirectory, "stations.txt");
			if (!stationFile.exists()) 
				stationFile = new File(modelDirectory, "stations");
			if (!stationFile.exists()) 
				stationFile = new File(modelDirectory, "infra_stas");
			if (!stationFile.exists()) 
				stationFile = new File(modelDirectory, "stations.txt");

			// read in list of station names
			library.modelNames = new ArrayList<String>(100);
			Scanner input = new Scanner(stationFile);
			while(input.hasNext())
				library.modelNames.add(input.nextLine());
			input.close();

			// read in time intervals, e.g., WINTER -> 80, or JANUARY -> 31
			File timeGuideFile = new File(modelDirectory, "time_guide.txt");
			if (!timeGuideFile.exists())
				timeGuideFile = new File(modelDirectory, "time_guide");
			if (!timeGuideFile.exists())
				timeGuideFile = new File(modelDirectory, "time_guide.txt");

			library.seasons = new ArrayList<Tuple<String,Integer>>();
			input = new Scanner(timeGuideFile);
			double htConvert = Double.parseDouble(input.nextLine());
			while(input.hasNext()) {
				String[] tokens = input.nextLine().trim().split("\\s+");
				if (tokens.length == 2)
					library.seasons.add(new Tuple<>(tokens[0], Integer.parseInt(tokens[1])));
				else if (tokens.length == 1 && library.seasons.get(library.seasons.size()-1).second < 365)
					library.seasons.add(new Tuple<>(tokens[0], 365));
			}
			input.close();

			// map from season -> modelName -> Radial2DModel
			library.models = new TreeMap<>();

			for (Tuple<String, Integer> tuple : library.seasons) {
				String season = tuple.first;
				Map<String, Radial2DModel> modelMap = new TreeMap<>();
				library.models.put(season, modelMap);
				File seasonDirectory = new File(modelDirectory, season);
				for (String modelName : library.modelNames) {
					File modelFile = new File(seasonDirectory, modelName);
					File cannonicalFile = modelFile.getCanonicalFile();
					Radial2DModel model = modelMap.get(cannonicalFile.getName());
					if (model == null && modelFile.exists()) {
						if (RADIAL2D_MODEL_CLASS.equals("Radial2DModelImproved"))
							model = new Radial2DModelImproved(cannonicalFile);
						else if (RADIAL2D_MODEL_CLASS.equals("Radial2DModelLegacy"))
							model = new Radial2DModelLegacy(cannonicalFile);
						else
							throw new Exception("public static variable Radial2DLibrary.RADIAL2D_MODEL_CLASS must equal "
									+ "either Radial2DModelImproved or Radial2DModelLegacy but is currently equal to "
									+Radial2DLibrary.RADIAL2D_MODEL_CLASS);
						model.htConvert(htConvert);
						modelMap.put(cannonicalFile.getName(), model);
					}
					modelMap.put(modelFile.getName(), model);
				}
			}
			libraryMap.put(modelDirectory.getCanonicalPath(), library);
		}
		return library;
	}

	/**
	 * Retrieve the models for a particular season.
	 * @param season
	 * @return Map from modelName -> Radial2DModel.
	 */
	public Map<String, Radial2DModel> getModels(String season) { return models.get(season); }

	/**
	 * Retrieve the models for a particular season.
	 * @param season
	 * @return Map from modelName -> Radial2DModel.
	 */
	public Map<String, Radial2DModel> getUniqueModels(String season) { 
		Map<String, Radial2DModel> map = new TreeMap<>();
		Map<String, Radial2DModel> seasonModels = models.get(season);
		if (seasonModels != null)
			for (Radial2DModel model : seasonModels.values()) {
				map.put(model.name(), model);
			}
		return map;
	}

	/**
	 * Return Map modelName -> Radial2DModel valid on specified jdate.
	 * Note that sometimes there are softlinks in a modelDirectory where multiple
	 * modelNames point to the same Radial2DModel.  This method will return an entry 
	 * for each softlink.  See also getUniqueModels(jdate).
	 * @return
	 */
	public Map<String, Radial2DModel> getModels(long jdate) { return models.get(getSeason(jdate)); }

	/**
	 * Return Map station name -> Radial2DModel valid on specified jdate.
	 * Note that sometimes there are softlinks in a modelDirectory where multiple
	 * modelNames point to the same Radial2DModel.  This method will return an entry 
	 * for only the canonical modelName.  See also getModels(jdate).
	 * @return
	 */
	public Map<String, Radial2DModel> getUniqueModels(long jdate) { 
		return getUniqueModels(getSeason(jdate));
	}

	/**
	 * Retrieve the Radial2DModel for specified station on specified jdate.
	 * @param jdate
	 * @param sta
	 * @return
	 */
	public Radial2DModel getModel(long jdate, String sta) {
		Map<String, Radial2DModel> seasonModels = models.get(getSeason(jdate));
		return seasonModels == null ? null : seasonModels.get(sta);
	}

	/**
	 * Retrieve the model that supports the specified PredictionRequest.  This is 
	 * equivalent to requesting the model for the request.source.jdate and request.receiver.sta.
	 * @param request
	 * @return
	 */
	public Radial2DModel getModel(PredictionRequest request) {
		return getModel(request.getSource().getJDate(),  request.getReceiver().getSta());
	}

	public Set<String> getSeasons() { return models.keySet(); } 

	/**
	 * Retrieve the season for a particular jdate.
	 * Leapyears are handled appropriately.
	 * @param jdate
	 * @return
	 */
	public String getSeason(long jdate) {
		int doy = GMTFormat.getCommonDOY((int)jdate);
		for (Tuple<String, Integer> tuple : seasons)
			if (doy <= tuple.second)
				return tuple.first;
		// invalid jdate!
		return seasons.get(0).first;
	}

	public void close() {
		models = null;
		modelNames = null;
		seasons = null;
	}

}
