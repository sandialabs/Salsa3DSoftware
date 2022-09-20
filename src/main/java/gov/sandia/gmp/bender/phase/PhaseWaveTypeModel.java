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
package gov.sandia.gmp.bender.phase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.gmp.baseobjects.globals.EarthInterface;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.bender.BenderModelInterfaces;
import gov.sandia.gmp.util.containers.Tuple;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.globals.Globals;

/**
 * Defines the wave type across GeoTessModel boundary interfaces. The phase
 * wave type model is constructed from the comma separated wave type / model
 * interface list provided by the SeismicPhase object using the method
 * getRayInterfaceWaveTypeList(). The list begins with the starting ray wave
 * type name (PSLOWNESS or SSLOWNESS) and then, if wave type conversion occurs
 * at one or more model boundary interfaces, one or more pairs containing the
 * model interface name (e.g. "CMB", "M660", ect.) are associated with a
 * wave type name as the ray crosses each respective boundary. For example, the
 * phase "PcS" is defined by the string "PSLOWNESS, CMB, SSLOWNESS".
 * 
 * @author jrhipp
 *
 */
public class PhaseWaveTypeModel
{
	/**
	 * The GeoTessModel metadata object used to output slowness attribute names.
	 */
	private GeoTessMetaData   metaData = null;

	/**
	 * The seismic phase from which this model was created.
	 */
	private SeismicPhase      seismicPhase = null;

	/**
	 * The list of model interfaces where the wave type changes. The first entry
	 * is "SOURCE" and the last entry is "RECEIVER". All other entries are valid
	 * model interface names.
	 */
	private ArrayList<String> waveSpeedInterfaceNameList = null;
	
	/**
	 * The list of model interfaces layer name indexes where the wave type changes.
	 * The first and last entries are -1 (corresponding to "SOURCE" and "RECEIVER").
	 * All other entries are the indexes of the valid model interface names.
	 */
	private ArrayListInt      waveSpeedInterfaceIndxList = null;
	
	/**
	 * the wave speed index defined in the model and specified for each entry in
	 * the wave speed interface list above. The first entry is the wave speed
	 * defined at the source. The last entry is simply a copy of the 2nd to last
	 * wave speed index which finishes at the receiver.
	 */
	private ArrayListInt      waveSpeedAttributeIndxList = null;

	/**
	 * Creates a new seismic phase wave type interface conversion model object using
	 * the interface layer description provided by a GeoTessModel metadata object
	 * and an input seismic phase ray interface wave type list.
	 * 
	 * @param md The input GeoTessModel meta data object.
	 * @param sp The input seismic phase.
	 * @throws IOException
	 */
	public PhaseWaveTypeModel(GeoTessMetaData md, SeismicPhase sp,
			BenderModelInterfaces benderModelInterfaces,
			ArrayList<Tuple<EarthInterface, Integer>> waveSpeedInterfaceChngList)
					throws IOException {

		// set the metaData (output use only) and seismic phase

		metaData = md;
		seismicPhase = sp;

		// get valid interface names for the assigned GeoTessModel

		HashMap<String, Integer> validInterfaceNames = 
				benderModelInterfaces.getValidInterfaceNameIndexMap();

		// initialize the interface name, interface index, and slowness attribute index lists
		
		waveSpeedInterfaceNameList = new ArrayList<String>(waveSpeedInterfaceChngList.size() + 1);
		waveSpeedInterfaceIndxList = new ArrayListInt(waveSpeedInterfaceChngList.size() + 1);
		waveSpeedAttributeIndxList = new ArrayListInt(waveSpeedInterfaceChngList.size() + 1);

		// The first entry is simply the "SOURCE" associated with the first
		// slowness type index

		int waveSpeedAttrIndex = waveSpeedInterfaceChngList.get(0).second;
		waveSpeedInterfaceNameList.add("SOURCE");
		waveSpeedInterfaceIndxList.add(-1);
		waveSpeedAttributeIndxList.add(waveSpeedAttrIndex);

		// loop over all interface changes and add their contents to their respective lists

		for (int i = 1; i < waveSpeedInterfaceChngList.size(); i++) {

			waveSpeedAttrIndex = waveSpeedInterfaceChngList.get(i).second;
			String interfaceName = waveSpeedInterfaceChngList.get(i).first.name();
			waveSpeedInterfaceNameList.add(interfaceName);
			waveSpeedInterfaceIndxList.add(validInterfaceNames.get(interfaceName));
			waveSpeedAttributeIndxList.add(waveSpeedAttrIndex);
		}

		// add the Receiver as a final entry with the last stored wave speed

		waveSpeedInterfaceNameList.add("RECEIVER");
		waveSpeedInterfaceIndxList.add(-1);
		waveSpeedAttributeIndxList.add(waveSpeedAttrIndex);
	}

	public int size()
	{
		return waveSpeedInterfaceNameList.size() - 2;
	}

	/**
	 * Returns the wave speed model interface name for entry i.
	 * 
	 * @return The wave speed model interface name for entry i.
	 */
	public String getWaveSpeedInterfaceName(int i)
	{
		return waveSpeedInterfaceNameList.get(i);
	}

	/**
	 * Returns the wave speed model interface index for entry i.
	 * 
	 * @return The wave speed model interface index for entry i.
	 */
	public int getWaveSpeedInterfaceIndex(int i)
	{
		return waveSpeedInterfaceIndxList.get(i);
	}

	/**
	 * Returns the wave speed model attribute index for entry i.
	 * 
	 * @return The wave speed model attribute index for entry i.
	 */
	public int getWaveSpeedAttributeIndex(int i)
	{
		return waveSpeedAttributeIndxList.get(i);
	}

//	/**
//	 * Returns the metadata object used to create this wave type conversion model.
//	 *  
//	 * @return The metadata object used to create this wave type conversion model.
//	 */
//	public GeoTessMetaData getMetaData()
//	{
//		return metaData;
//	}
	
	/**
	 * Returns the seismic phase object used to create this wave type conversion model.
	 *  
	 * @return The seismic phase object used to create this wave type conversion model.
	 */
	public SeismicPhase getSeismicPhase()
	{
		return seismicPhase;
	}

	/**
	 * Standard toString() override.
	 */
	@Override
	public String toString()
	{
    return getPhaseWaveTypeModelTable();				
	}

	/**
	 * Builds a table of interface wave speed conversion information.
	 * 
	 * @return A table of interface wave speed conversion information.
	 */
	public String getPhaseWaveTypeModelTable()
	{
		String hdr = "    ";
		String title = "Phase Wave Speed Model: " + seismicPhase.name();

		String rowColHdr = "";
		String[][] colHdr =
		{
		  { "Entry", "Interface", "Interface", "Wave Speed" },
		  { "Index", "Name",      "Index",     "Name"       }
		};

		Globals.TableAlignment algn = Globals.TableAlignment.CENTER;
		Globals.TableAlignment[] colAlign = { algn, algn, algn, algn };

		String[][] data = new String[waveSpeedInterfaceNameList.size()][];
		for (int i = 0; i < waveSpeedInterfaceNameList.size(); ++i)
		{
			String[] rowData = new String[colAlign.length];
			rowData[0] = Integer.toString(i);
			rowData[1] = waveSpeedInterfaceNameList.get(i);
			rowData[2] = Integer.toString(waveSpeedInterfaceIndxList.get(i));
			rowData[3] = metaData.getAttributeName(waveSpeedAttributeIndxList.get(i));
			data[i] = rowData;
		}

		return Globals.makeTable(hdr, title, rowColHdr, colHdr, colAlign, null,
				                     algn, data, 2);
	}
}
