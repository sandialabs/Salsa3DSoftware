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
package gov.sandia.gmp.baseobjects.sasc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages a collection of SASC_Models stored in a specified directory.
 * It is primarily intended to be used as a library of models that will be accessed
 * by an application.
 * <p>It includes a main method that can be used to generate linux commands to 
 * convert duplicate SASC models into softlinks.
 */
public class SASC_Library {

	/**
	 * Map from a canonical directory name to a SASC_Library
	 */
	static private Map<String, SASC_Library> libraryMap;

	synchronized static public Map<String, SASC_Library> getLibraryMap() { 
		return libraryMap; 
	}

	/**
	 * This application reads in all the SASC models in a specified directory and generates 
	 * a set of linux commands that will replace duplicate models with softlinks.
	 * The commands are not executed by this application and no changes are made to the SASC model directory.
	 * <p>The linux commands can be copied to the clipboard and pasted directly into 
	 * a terminal window where the current directory is the specified SASC directory.
	 * All the commands will be executed, permanently altering the contents of the directory.
	 * A backup of the directory should be created before executing these commands.
	 * @param args path to the SASC directory.
	 */
	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				throw new Exception("\nThis application reads in all the SASC models in a specified directory and generates \n"
						+"a set of linux commands that will replace duplicate models with softlinks. \n"
						+"The commands are not executed by this application and no changes are made to the SASCmodel directory. \n"
						+"<p>The linux commands can be copied to the clipboard and pasted directly into  \n"
						+"a terminal window where the current directory is the specified SASC directory. \n"
						+"All the commands will be executed, permanently altering the contents of the directory. \n"
						+"A backup of the directory should be created before executing these commands. \n"
						+"\nPlease specify the path to the directory containing SASC models. \n"
						);
			}

			File modelDirectory = new File(args[0]);

			//			SASC_Library library = getLibrary(modelDirectory);
			//
			//			Map<String, SASC_Model> map = library.getModelMap();
			//			
			//			for (String sta : map.keySet())
			//				System.out.println(sta);
			//			
			//			System.out.println(map.size());
			//
			//			Set<SASC_Model> set = new HashSet<>(map.values());
			//			System.out.println(set.size());

			// The following code generates commands that convert files to soft links.

			Map<SASC_Model, List<String>> stationsByModel = new HashMap<>();
			Map<String, File> fileByStation = new HashMap<>();

			// load sasc models. Station names are extracted from the file names.
			// Expecting names like sasc.sta or sasc.sta.???, where the station name
			// is the first substring following 'sasc.'
			for (File f : modelDirectory.listFiles()) 
				if (f.getName().startsWith("sasc") && !f.getName().contains("dummy")) 
				{
					try {
						SASC_Model model = new SASC_Model(f);
						String sta = f.getName().split("\\.")[1];
						fileByStation.put(sta, f);
						List<String> stations = stationsByModel.get(model);
						if (stations == null)
							stationsByModel.put(model, stations=new ArrayList<>());
						stations.add(sta);
					} catch (Exception e) {
						System.err.println(f.getName());
						e.printStackTrace();
					}
				}

			Map<SASC_Model, File> dummies = new LinkedHashMap<>();
			File dummyFile=null;
			for (Entry<SASC_Model, List<String>> entry : stationsByModel.entrySet()) {
				SASC_Model uniqueModel = entry.getKey();
				List<String> stations = entry.getValue();
				if (stations.size() > 1) {
					if (uniqueModel.def_slow_mdl_err == 2.5)
						dummyFile = new File(modelDirectory, "sasc.default.seismic");
					else if (uniqueModel.def_slow_mdl_err == 30.)
						dummyFile = new File(modelDirectory, "sasc.default.infrasound");
					else if (uniqueModel.def_slow_mdl_err == 0.)
						dummyFile = new File(modelDirectory, "sasc.default.zero");
					else 
						dummyFile = new File(modelDirectory, String.format("sasc.default.%1.0f", uniqueModel.def_slow_mdl_err));
					dummies.put(uniqueModel, dummyFile);
					System.out.printf("cp %s %s%n", fileByStation.get(stations.get(0)).getName(),
							dummyFile.getName());

					for (String sta : stations)
						System.out.printf("rm %s; ln -s %s %s%n", fileByStation.get(sta).getName(), dummyFile.getName(), fileByStation.get(sta).getName());
				}
			}
			File sasc_dummy = new File(modelDirectory, "sasc.dummy");
			if (sasc_dummy.exists())
				System.out.println("rm sasc.dummy");


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieve a SASC_Library object that contains all the models in the specified modelDirectory.
	 * A SASC_Library that corresponds to a particular modelDirectory is only loaded into memory
	 * the first time this method is called with a particular modelDirectory.  Subsequent calls with 
	 * the same modelDirectory return a reference to the previously loaded library.
	 * 
	 * @param modelDirectory
	 * @return a SASC_Library object.
	 * @throws IOException
	 */
	synchronized static public SASC_Library getLibrary(File modelDirectory) throws Exception {
		if (libraryMap == null) libraryMap = new ConcurrentHashMap<String, SASC_Library>();

		SASC_Library library = libraryMap.get(modelDirectory.getCanonicalPath());

		if (library == null)
		{
			library = new SASC_Library();
			libraryMap.put(modelDirectory.getCanonicalPath(), library);

			library.modelMap = new TreeMap<>();

			// load sasc models. Station names are extracted from the file names.
			// Expecting names like sasc.sta.yyy or sasc.sta
			for (File f : modelDirectory.listFiles()) 
				if (f.getName().startsWith("sasc") && !f.getName().contains("default")) {
					try {
						SASC_Model model = new SASC_Model(f);
						String sta = f.getName().split("\\.")[1];
						for (Entry<String, SASC_Model> entry : library.modelMap.entrySet()) {
							if (model.equals(entry.getValue())) {
								model = entry.getValue();
								break;
							}
						}
						library.modelMap.put(sta, model);
					} catch (Exception e) {
					}
				}
		}
		return library;
	}

	/**
	 * Close will delete all the SASC_Libraries currently in memory and set the 
	 * libraryMap object = null.  All future requests for a SASC_Library will require reloading
	 * the library from memory.
	 */
	public void close() {
		libraryMap = null;
	}

	/**
	 * TreeMap from station name to SASC_Model.
	 */
	private Map<String, SASC_Model> modelMap;

	/**
	 * Only constructor is private so forced to use static method getLibrary() to get one.
	 */
	private SASC_Library() { }

	public SASC_Model getModel(String sta) {
		return modelMap.get(sta);
	}

	/**
	 * Get map from sta to SASC_Model
	 * @return
	 */
	public Map<String, SASC_Model> getModelMap() {
		return modelMap;
	};


}
