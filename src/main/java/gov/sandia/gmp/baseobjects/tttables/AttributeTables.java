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
package gov.sandia.gmp.baseobjects.tttables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.util.exceptions.GMPException;

public class AttributeTables {

	/**
	 * The File that corresponds to the seismicBaseData directory.
	 */
	private final File seismicBaseData;

	/**
	 * The name of the directory in seismicBaseData/tt , az, sh, etc
	 */
	private final String modelName;

	/**
	 * A map from travel_time, tt_uncertainty, azimuth, azimuth_uncertainty,
	 * slowness and slowness_uncertainty to the correct directory where the tables
	 * are found. Directories will be seismicBaseData/[tt|az|sh]/modelName
	 */
	private final HashMap<GeoAttributes, File> directoryMap = new HashMap<GeoAttributes, File>(8);

	/**
	 * Set of SeismicPhases that are supported by these Attribute tables. In order
	 * to make it into this list, a file with the name of the phase has to exist in
	 * the right directory.
	 */
	//protected static EnumSet<SeismicPhase> supportedPhases = EnumSet.noneOf(SeismicPhase.class);

	/**
	 * These are phase names that are available from the file system
	 * (seismicBaseData?) but which are not specified in the enum SeismicPhases
	 */
	protected ArrayList<String> unrecognizedPhaseNames = new ArrayList<String>();

	/**
	 * Map from GeoAttributes->SeismicPhase->Table
	 */
	private TreeMap<GeoAttributes, TreeMap<SeismicPhase, Table>> tables = new TreeMap<GeoAttributes, TreeMap<SeismicPhase, Table>>();

	/**
	 * Constructor
	 * 
	 * @param seismicBaseData the File objects that corresponds to siesmicBaseData
	 *                        directory
	 * @param modelName       the name of a 1D model. Tables will be read from
	 *                        seismicBaseData/[tt|az|sh]/modelName/<phaseName>
	 * @throws FileNotFoundException if seismicBaseData does not exist, or if
	 *                               seismicBaseData/tt/modelName does not exist.
	 */
	public AttributeTables(File seismicBaseData, String modelName) throws FileNotFoundException {
		super();
		this.seismicBaseData = seismicBaseData;
		this.modelName = modelName;

		directoryMap.put(GeoAttributes.TRAVEL_TIME, new File(new File(seismicBaseData, "tt"), modelName));
		directoryMap.put(GeoAttributes.TT_MODEL_UNCERTAINTY, new File(new File(seismicBaseData, "tt"), modelName));
		directoryMap.put(GeoAttributes.AZIMUTH, new File(new File(seismicBaseData, "az"), modelName));
		directoryMap.put(GeoAttributes.AZIMUTH_DEGREES, new File(new File(seismicBaseData, "az"), modelName));
		directoryMap.put(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY, new File(new File(seismicBaseData, "az"), modelName));
		directoryMap.put(GeoAttributes.SLOWNESS, new File(new File(seismicBaseData, "sh"), modelName));
		directoryMap.put(GeoAttributes.SLOWNESS_DEGREES, new File(new File(seismicBaseData, "sh"), modelName));
		directoryMap.put(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY, new File(new File(seismicBaseData, "sh"), modelName));

//		File file = new File(directoryMap.get(GeoAttributes.TRAVEL_TIME), "P");
//		SeismicBaseData sbd = new SeismicBaseData(file);
//		if (!sbd.exists())
//			throw new FileNotFoundException(
//					"Directory " + directoryMap.get(GeoAttributes.TRAVEL_TIME).getPath() + 
//					" does not exist, couldn't find file "+file+", alt="+sbd.getAlternate()+
//					", ide="+sbd.getIde()+", resourceName = "+sbd.getResourceName()+", ispType="+
//					GlobalInputStreamProvider.forFiles().getClass().getCanonicalName()+
//					", isp="+GlobalInputStreamProvider.forFiles());
	}
	
	/**
	 * if directory dir/subdir exists, return dir/subdir/model
	 * else return dir/model.
	 * @param dir e.g., seismicBaseData
	 * @param subDir e.g., tt, or az, or sh
	 * @param model e.g., ak135
	 * @return
	 */
	private File getDir(File dir, String subDir, String model) {
		if (dir.getName().equals("seismic-base-data.jar") || new File(dir, subDir).exists())
			return new File(new File(dir, subDir), model);
		
		System.out.println("/Users/sballar/Documents/infrasound/distance_dependent_uncertainty/infrasound_uncertainty_model");
		System.out.println(new File(dir, model).getAbsolutePath());
		return new File(dir, model);
	}

	/**
	 * Retrieve an interpolated value for the specified attribute and prediction.
	 * Source-receiver distance and source depth are retrieved from the prediction
	 * object. Returns Globals.NA_VALUE if anything goes wrong.
	 * 
	 * @param attribute
	 * @param prediction
	 * @return
	 * @throws GMPException          if model file exists but does not contain model
	 *                               uncertainty information. This should only
	 *                               happen for uncertainty tables, never for
	 *                               attribute tables.
	 * @throws FileNotFoundException if travel time table does not exist for the
	 *                               specified attribute/model/phase.
	 * @throws OutOfRangeException   if source-receiver distance or source depth is
	 *                               out of range. This should only happen for
	 *                               attribute tables, never for uncertainty tables.
	 */
	public double getValue(GeoAttributes attribute, Prediction prediction)
			throws OutOfRangeException, IOException, GMPException {
		return getValue(attribute, prediction.getPhase(), prediction.getReceiver(),
				prediction.getSource());
	}

	/**
	 * Retrieve an interpolated value for the specified attribute, phase, source and
	 * receiver. Returns Globals.NA_VALUE if anything goes wrong.
	 * 
	 * @param attribute
	 * @param phase
	 * @param receiver
	 * @param event
	 * @return
	 * @throws GMPException          if model file exists but does not contain model
	 *                               uncertainty information. This should only
	 *                               happen for uncertainty tables, never for
	 *                               attribute tables.
	 * @throws FileNotFoundException if travel time table does not exist for the
	 *                               specified attribute/model/phase.
	 * @throws OutOfRangeException   if source-receiver distance or source depth is
	 *                               out of range. This should only happen for
	 *                               attribute tables, never for uncertainty tables.
	 */
	public double getValue(GeoAttributes attribute, SeismicPhase phase, GeoVector receiver, GeoVector event)
			throws OutOfRangeException, IOException, GMPException {
		return getValue(attribute, phase, event.distanceDegrees(receiver), event.getDepth());

	}

	/**
	 * Retrieve an interpolated value for the specified attribute, phase,
	 * source-receiver distance in degrees, and source depth in km. Returns
	 * Globals.NA_VALUE if anything goes wrong.
	 * 
	 * @param attribute
	 * @param phase
	 * @param distanceDegrees
	 * @param sourceDepth
	 * @return
	 * @throws GMPException          if model file exists but does not contain model
	 *                               uncertainty information. This should only
	 *                               happen for uncertainty tables, never for
	 *                               attribute tables.
	 * @throws FileNotFoundException if travel time table does not exist for the
	 *                               specified attribute/model/phase.
	 * @throws OutOfRangeException   if source-receiver distance or source depth is
	 *                               out of range. This should only happen for
	 *                               attribute tables, never for uncertainty tables.
	 */
	public double getValue(GeoAttributes attribute, SeismicPhase phase, double distanceDegrees, double sourceDepth)
			throws OutOfRangeException, IOException, GMPException {
		return getTable(attribute, phase).interpolate(distanceDegrees, Math.max(0., sourceDepth));
	}

	/**
	 * Retrieve the table for the specified attribute/phase. Tables are read from
	 * files as necessary and stored internally.
	 * 
	 * @param attribute
	 * @param phase
	 * @return
	 * @throws GMPException          if model file exists but does not contain model
	 *                               uncertainty information.
	 * @throws FileNotFoundException
	 */
	public synchronized Table getTable(GeoAttributes attribute, SeismicPhase phase) throws IOException, GMPException {
		TreeMap<SeismicPhase, Table> tableSet = tables.get(attribute);
		if (tableSet == null) {
			tableSet = new TreeMap<SeismicPhase, Table>();
			tables.put(attribute, tableSet);
		}

		Table table = tableSet.get(phase);
		if (table == null) {
			if (attribute.toString().endsWith("UNCERTAINTY"))
				table = new TableOfUncertainties().read(getFile(attribute, phase));
			else
				table = new TableOfObservables().read(getFile(attribute, phase));
			tableSet.put(phase, table);
		}

		return table;
	}

	/**
	 * Retrieve the File object that would be used to retrieve information about a
	 * potential attribute, phase combination.
	 * 
	 * @param attribute
	 * @param phase
	 * @return
	 */
	public File getFile(GeoAttributes attribute, SeismicPhase phase) {
		return new File(directoryMap.get(attribute), phase.toString());
	}

	/**
	 * Find out if a table exists for a specified GeoAttributes, SeismicPhase
	 * combination.
	 * 
	 * @param attribute
	 * @param phase
	 * @return
	 */
	public boolean isSupported(GeoAttributes attribute, SeismicPhase phase) {
		return isSupported(seismicBaseData, modelName, attribute, phase);
	}

	/**
	 * Find out if a table exists in seismicBaseData for a specified modelName,
	 * GeoAttributes, SeismicPhase combination.
	 * 
	 * @param modelName
	 * @param attribute
	 * @param phase
	 * @return
	 */
	public boolean isSupported(String modelName, GeoAttributes attribute, SeismicPhase phase) {
		return isSupported(seismicBaseData, modelName, attribute, phase);
	}

	/**
	 * Find out if a table exists for a specified modelName, GeoAttributes,
	 * SeismicPhase combination.
	 * 
	 * @param attribute
	 * @param phase
	 * @return
	 */
	static public boolean isSupported(File seismicBaseData, String modelName,
	        GeoAttributes attribute, SeismicPhase phase) {
		File f = new File(seismicBaseData, subdir(attribute));
		if (!f.exists())
			return false;
		f = new File(f, modelName);
		if (!f.exists())
			return false;
		f = new File(f, phase.toString());
		if (!f.exists())
			return false;
		if (attribute.toString().endsWith("UNCERTAINTY"))
			try {
				new TableOfUncertainties().read(f);
			} catch (Exception e) {
				return false;
			}
		return true;
	}

	/**
	 * Retrieve current modelName
	 * 
	 * @return
	 */
	public String getModelName() {
		return modelName;
	}

	private static String subdir(GeoAttributes attribute) {
		if (attribute == GeoAttributes.TRAVEL_TIME || attribute == GeoAttributes.TT_MODEL_UNCERTAINTY)
			return "tt";
		if (attribute.toString().startsWith("AZIMUTH"))
			return "az";
		if (attribute.toString().startsWith("SLOWNESS"))
			return "sh";

		return attribute.toString();
	}

	/**
	 * Phase names that are available from the file system (seismicBaseData?) but
	 * which are not specified in the enum SeismicPhases
	 * 
	 * @return the unrecognizedPhaseNames
	 */
	public ArrayList<String> getUnrecognizedPhaseNames() {
		return unrecognizedPhaseNames;
	}

	/**
	 * @return the seismicBaseData
	 */
	public File getSeismicBaseData() {
		return seismicBaseData;
	}

}
