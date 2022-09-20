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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.gmp.baseobjects.globals.EarthInterface;
import gov.sandia.gmp.baseobjects.globals.EarthInterfaceGroup;
import gov.sandia.gmp.util.globals.Globals;

/**
 * This class is used strictly by Bender at construction to validate the layer
 * description of the input GeoTessModel to ensure that it's layers are mapped
 * to valid EarthInterface layer names, which Bender understands.
 * <p>
 * The purpose of the class is to 1) validate the input GeoTessModels layer
 * description; 2) if necessary, map any of the models layer names to an
 * equivalent valid EarthInterface name; 3) ensure all GeoTessModel layer names
 * have an equivalent EarthInterface name; 4) build internal maps and arrays
 * containing the valid mappings; 5) build top and bottom interfaces in the
 * model that are associated with all EarthInterfaceGroups contained in the
 * layer description; 6) finally, ensure that at least one interface from the
 * "CRUST" and "MANTLE" groups are defined.; and 7) provide an easily accessible
 * interface to the constructed fields for Bender to use during processing.
 * 
 * 
 * @author jrhipp
 *
 */
public class BenderModelInterfaces {
	
	/**
	 * Contains any string model layer names that are mapped to
	 * valid EarthInterface names. If no mapping are requested then
	 * this container will be empty. The mapping are built from the
	 * contents of the bender property "benderModelLayerToEarthInterfaceMap".
	 */
	private HashMap<String, String> benderInstanceInterfaceNameMap;
	
	/**
	 * After construction this map contains all valid mapped EarthInterface
	 * names associated with their layer index in the input GeoTessModel
	 * layer name description.
	 */
	private HashMap<String, Integer> modelValidInterfaceNameIndexMap;
	
	/**
	 * After construction this map contains all original GeoTessModel layer names
	 * associated with their layer index in the input GeoTessModel description.
	 */
	private HashMap<String, Integer> modelLayerNameIndexMap;
	
	/**
	 * After construction this array contains all valid EarthInterface objects
	 * stored in order of their layer index (least to greatest).
	 */
	private EarthInterface[] modelValidInterfaces;
	
	/**
	 * After construction this array contains all original model layer names
	 * stored in order of their layer index (least to greatest).
	 */
	private String[] modelLayerNames;

	/**
	 * After construction this map contains all discovered interface groups
	 * associated with their top-most EarthInterface layer index defined in
	 * the model.
	 */
	private HashMap<EarthInterfaceGroup, Integer> interfaceGroupTopMap; 
	
	/**
	 * After construction this map contains all discovered interface groups
	 * associated with their bottom-most EarthInterface layer index defined
	 * in the model.
	 */
	private HashMap<EarthInterfaceGroup, Integer> interfaceGroupBottomMap;

	/**
	 * Creates a Bender GeoTessModel interface prescription used by all rays
	 * processed in Bender with the input GeoTessModel meta data (GeoTessMetaData).
	 * The purpose of the class is to 1) validate the input GeoTessModels layer
	 * description; 2) if necessary, map any of the models layer names to an
	 * equivalent valid EarthInterface name; 3) ensure all GeoTessModel layer names
	 * have an equivalent EarthInterface name; 4) build internal maps and arrays
	 * containing the valid mappings; 5) build top and bottom interfaces in the
	 * model that are associated with all EarthInterfaceGroups contained in the
	 * layer description; 6) finally, ensure that at least one interface from the
	 * "CRUST" and "MANTLE" groups are defined.
	 * 
	 * @param md                          The input models meta data containing the
	 *                                    models layer definition.
	 * @param modelInterfaceRemapProperty The contents of the bender property
	 *                                    "benderModelLayerToEarthInterfaceMap"
	 * @throws IOException
	 */
	public BenderModelInterfaces(GeoTessMetaData md, String modelInterfaceRemapProperty)
			throws IOException {

		// map model names to valid EarthInterface names if the remap
		// property is defined
		
		if ((modelInterfaceRemapProperty != null) && !modelInterfaceRemapProperty.isEmpty()) {
			mapModelNames(md, modelInterfaceRemapProperty);
		}
		
		// now validate all model layer names to ensure they are completely mapped to a valid
		// EarthInterface.
		
		validateModelLayerNames(md);

		// validate interface name groups to ensure a layer in the "CRUST" and "MANTLE" groups
		// are defined.
		
		validateInterfaceGroups();
	}
	
	/**
	 * Maps all model layer name to EarthInterface name pairs contained in the input
	 * bender property string modelInterfaceRemapProperty into the the map
	 * benderInstanceInterfaceNameMap. The property is not empty on entry and the
	 * model GeoTessMetaData object, md, is defined. The property string is assigned
	 * from the Bender property "benderModelLayerToEarthInterfaceMap".
	 * 
	 * @param md                          The GeoTessMetaData object containing the
	 *                                    model layer names.
	 * @param modelInterfaceRemapProperty The contents of the bender property
	 *                                    "benderModelLayerToEarthInterfaceMap"
	 * @throws IOException
	 */
	private void mapModelNames(GeoTessMetaData md, String modelInterfaceRemapProperty)
			throws IOException {

		// create storage for the benderInstanceInterfaceNameMap and pull all mappings separated
		// by ";" from the remap property
		
		benderInstanceInterfaceNameMap = new HashMap<String, String>();
		String[] tokens = modelInterfaceRemapProperty.split(";");
		
		// loop over all token pairs. Each token must be defined as
		// 		(model layer name, valid EarthInterface name)
		for (String token : tokens) {
			
			// get token pair as string array. Must be two. If not throw error.
			
			String[] names = Globals.getTokens(token, "\t ,");
			if (names.length != 2) {
				throw new IOException("\nError: Property "
						+ "benderModelLayerToEarthInterfaceMap = \""
								+ modelInterfaceRemapProperty + "\"\n"
								+ "       Expects two entries per remap separated by \";\"\n"
								+ "       (e.g. "
								+ "\" modelLayer1 earthInterface1; modelLayer2 earthInterface2\")"
								+ " ...\n");
			}

			// test the model layer name to ensure it is contained by the model.
			
			if (md.getLayerIndex(names[0].toUpperCase()) == -1) {
				throw new IOException("\nError: GeoTessModel layer name \"" + names[0]
						+ "\" read from Property\n       benderModelLayerToEarthInterfaceMap = \""
						+ modelInterfaceRemapProperty + "\"\n"
						+ "       is not defined in the input GeoTessModel layer entries ...\n");
			}

			// now test the model layer name to ensure it is not already a valid or mapped
			// EarthInterface name.

			if (EarthInterface.isValidMappedEarthInterfaceName(names[0])) {
				throw new IOException("\nError: GeoTessModel layer name \"" + names[0]
						+ "\" read from Property\n       benderModelLayerToEarthInterfaceMap = \""
						+ modelInterfaceRemapProperty + "\"\n"
						+ "       is a valid EarthInterface specification and does "
						+ "not need remapping ...\n");
			}

			// finally, test the EarthInterface name to which the model layer name is to be mapped
			// and verify it is a valid or commonly mapped EarthInterface name.
			
			if (!EarthInterface.isValidMappedEarthInterfaceName(names[1])) {
				throw new IOException("\nError: EarthInterface name \"" + names[1]
						+ "\" read from Property\n"
						+ "       benderModelLayerToEarthInterfaceMap = \""
						+ modelInterfaceRemapProperty + "\"\n"
						+ "       is not a defined EarthInterface entry as required ...\n");
			}

			// valid model name to EarthInterface name remap ... add it to the map

			benderInstanceInterfaceNameMap.put(names[0].toUpperCase(), names[1].toUpperCase());
		}
	}
	
	/**
	 * Validates the Bender GeoTessModel layer names. The method fills the class fields
	 * modelValidInterfaceNameIndexMap, modelLayerNameIndexMap, modelValidInterfaces, and
	 * modelLayerNames on exit. If the model contains a layer name that does not match an
	 * EarthInterface entry, or an EarthInterface::commonInterfaceNameMap entry, or finally
	 * a benderInstanceInterfaceNameMap, then an error is thrown. If all the layer names are
	 * found to be valid then an order check of the models layer names is made to ensure
	 * it corresponds to the order specified in EarthInterface. If not an error is thrown.
	 * 
	 * @param md The GeoTessMetaData of the current Bender GeoTessModel.
	 */
	private void validateModelLayerNames(GeoTessMetaData md) throws IOException {
		
		// initialize class fields
		
		modelValidInterfaceNameIndexMap = new HashMap<String, Integer>();
		modelLayerNameIndexMap = new HashMap<String, Integer>();
		modelValidInterfaces = new EarthInterface [md.getNLayers()];
		modelLayerNames = md.getLayerNames();
		
		// loop over all GeoTessModel layer names
		
		for (int i = 0; i < md.getNLayers(); ++i) {
		
			// get the next layername
			
			String layerName = md.getLayerName(i).toUpperCase();
			
			// see if the layer name is defined as an EarthInterface entry or if it is mapped
			// EarthInterface::commonInterfaceNameMap entry.
			if (EarthInterface.isValidMappedEarthInterfaceName(layerName)) {
				
				// entry is defined in EarthInterface. Throw an error if the interface is defined
				// twice. Otherwise, set the fields and continue to the next layer name
				
				String validInterfaceName = EarthInterface.getValidMappedEarthInterfaceName(layerName);
				if (modelValidInterfaceNameIndexMap.get(validInterfaceName) != null) {
					throw new IOException("\nError: EarthInterface name \""
							+ validInterfaceName + "\" cannot be defined twice\n"
							+ "       (layer interface # " 
							+ modelValidInterfaceNameIndexMap.get(validInterfaceName) + " and # "
							+ i + " ...\n");
				}
				modelValidInterfaceNameIndexMap.put(validInterfaceName, i);
				modelLayerNameIndexMap.put(layerName,  i);
				modelValidInterfaces[i] = EarthInterface.getValidMappedEarthInterface(layerName);
			} else {
				
				// entry was not defined in EarthInterface. See if the entry is defined in the local
				// Bender instance (benderInstanceInterfaceNameMap) that is using this GeoTessModel.
				
				if ((benderInstanceInterfaceNameMap != null) &&
						benderInstanceInterfaceNameMap.containsKey(layerName)) {
					
					// entry is mapped in this bender instance interface map. Throw an error if
					// the interface is defined twice. Otherwise, set the fields and continue
					// to the next layer name
					
					String validInterfaceName = benderInstanceInterfaceNameMap.get(layerName);
					if (modelValidInterfaceNameIndexMap.get(validInterfaceName) != null) {
						throw new IOException("\nError: EarthInterface name \""
								+ validInterfaceName + "\" cannot be defined twice\n"
								+ "       (layer interface # " 
								+ modelValidInterfaceNameIndexMap.get(validInterfaceName) + " and # "
								+ i + " ...\n");
					}
					modelValidInterfaceNameIndexMap.put(validInterfaceName, i);
					modelLayerNameIndexMap.put(layerName,  i);
					modelValidInterfaces[i] = EarthInterface.getValidMappedEarthInterface(validInterfaceName);
				} else {
					
					// entry was not defined in bender instance interface map. Throw an error.
					
					throw new IOException("\nError: GeoTessModel layer name \"" + layerName
							+ "\" (layer index = " + i + ") is not a valid EarthInterface name,\n"
							+ "       and has not been mapped to a valid EarthInterface"
							+ " name ...\n       Use bender property "
							+ "\"benderModelLayerToEarthInterfaceMap\"\n"
							+ "       to map the model layer name to a valid EarthInterface"
							+ " name ...\n");
				}
			}
		}

		// validate the order of the layer names for consistency. If an error is thrown during
		// evaluation append the error to a new descriptive error and throw again.
		
		try {
			EarthInterface.validateInterfaceOrder(modelValidInterfaces);	
		} catch (Exception ex) {
			throw new IOException("\nError: The ordering of the Bender input GeoTessModel"
					+ " layers were found\n       to be invalid relative to the "
					+ "EarthInterface ordering: \n\n" + ex.getMessage() + "\n");
		}
	}

	/**
	 * Validates the Bender GeoTessModel interface groups. For Bender to properly execute
	 * it requires at least one valid EarthInterface from both the "CRUST" and "MANTLE"
	 * EarthInterfaceGroup. This enables proper crustal phase evaluation. Later, if a 
	 * mantle or core phase is requested then one valid EarthInterface from the "CORE" group
	 * is also required. Those phases are checked before the ray is executed.
	 */
	private void validateInterfaceGroups() throws IOException {
		
		// Set up group name top and bottom layers
		
		interfaceGroupTopMap = new HashMap<EarthInterfaceGroup, Integer>();
		interfaceGroupBottomMap = new HashMap<EarthInterfaceGroup, Integer>();
		EarthInterfaceGroup.getEarthInterfaceGroupTop(modelValidInterfaces, interfaceGroupTopMap);
		EarthInterfaceGroup.getEarthInterfaceGroupBottom(modelValidInterfaces, interfaceGroupBottomMap);
		
		// Test to ensure that a "CRUST" and "MANTLE" group are defined
		
		if (!interfaceGroupTopMap.containsKey(EarthInterfaceGroup.CRUST)) {
			throw new IOException("\nError: Input GeoTessModel does not contain"
					+ " an interface assigned to the EarthInterfaceGroup \"CRUST\" grouping,\n"
					+ "       which is necessary to properly execute any phase in Bender ...\n");
		} else if (!interfaceGroupTopMap.containsKey(EarthInterfaceGroup.MANTLE)) {
			throw new IOException("\nError: Input GeoTessModel does not contain"
					+ " an interface assigned to the EarthInterfaceGroup \"MANTLE\" grouping,\n"
					+ "       which is necessary to properly execute any phase in Bender ...\n");
		}
	}

	/**
	 * Returns the valid interface name index map.
	 * 
	 * @return The valid interface name index map.
	 */
	public HashMap<String, Integer> getValidInterfaceNameIndexMap() {
		return modelValidInterfaceNameIndexMap;
	}
	
	/**
	 * Returns true if the input name is a valid EarthInterface name defined by the
	 * GeoTessModel.
	 * 
	 * @param name The valid EarthInterface name to be tested for containment.
	 * @return True if the input EarthInterface name is contained by the assigned
	 *         GeoTessModel layer description.
	 */
	public boolean isValidInterfaceNameContained(String name) {
		return modelValidInterfaceNameIndexMap.containsKey(name);
	}
	
	/**
	 * Returns the layer index of the layer assigned to the input valid
	 * EarthInterface name. If the name is not used in the model -1 is returned.
	 * Otherwise, the layer index is returned.
	 * 
	 * @param name The valid EarthInterface name for which the layer index will be
	 *             returned.
	 * @return The layer index associated with the input EarthInterface name, if it
	 *         is defined, or -1 if it is not defined.
	 */
	public int getValidInterfaceNameLayerIndex(String name) {
		if (modelValidInterfaceNameIndexMap.containsKey(name))
			return modelValidInterfaceNameIndexMap.get(name);
		else
			return -1;
	}

	/**
	 * Returns true if the input name is an original model layer name defined by the
	 * GeoTessModel.
	 * 
	 * @param name The original model layer name to be tested for containment.
	 * @return True if the input model layer name is contained by the assigned
	 *         GeoTessModel layer description.
	 */
	public boolean isModelLayerNameContained(String name) {
		return modelLayerNameIndexMap.containsKey(name);
	}
	
	/**
	 * Returns the layer index of the layer assigned to the input original
	 * model layer name. If the name is not used in the model -1 is returned.
	 * Otherwise, the layer index is returned.
	 * 
	 * @param name The original model layer name for which the layer index will be
	 *             returned.
	 * @return The layer index associated with the input original model layer name,
	 *         if it is defined, or -1 if it is not defined.
	 */
	public int getModelLayerNameLayerIndex(String name) {
		if (modelLayerNameIndexMap.containsKey(name))
			return modelLayerNameIndexMap.get(name);
		else
			return -1;
	}
	
	/**
	 * Returns the array of valid model EarthInterfaces, which are mapped
	 * to the original GeoTessModel.
	 */
	public EarthInterface[] getModelValidInterfaces() {
		return modelValidInterfaces;
	}
	
	/**
	 * Returns the ith layer valid model EarthInterface, which was mapped
	 * to the original GeoTessModel layer.
	 * 
	 * @param i The index of the valid model EarthInterface to be returned.
	 */
	public EarthInterface getModelValidInterface(int i) {
		return modelValidInterfaces[i];
	}
	
	/**
	 * Returns the array of original model layer names specified in the
	 * GeoTessModel.
	 */
	public String[] getModelLayerNames() {
		return modelLayerNames;
	}
	
	/**
	 * Returns the ith model layer name. This is the original layer name specified in the
	 * GeoTessModel.
	 * 
	 * @param i The index of the model layer to be returned.
	 */
	public String getModelLayerName(int i) {
		return modelLayerNames[i];
	}

	/**
	 * Returns true if the input EarthInterfaceGroup is defined in the model.
	 * 
	 * @param eig The EarthInterfaceGroup to be tested for definition in the model.
	 * @return True if eig is defined in the model.
	 */
	public boolean isEarthInterfaceGroupDefined(EarthInterfaceGroup eig) {
		return interfaceGroupTopMap.containsKey(eig);
	}

	/**
	 * Returns the top most EarthInterface layer index for the specific input
	 * EarthInterfaceGroup eig. If the EarthInterfaceGroup is not defined in the
	 * model then -1 is returned.
	 * 
	 * @param eig The EarthInterfaceGroup to be tested.
	 * @return The top most EarthInterface layer index of the group eig if that
	 *         group is defined in the model. Otherwise, -1 is returned.
	 */
	public int getTopMostEarthInterface(EarthInterfaceGroup eig) {
		if (interfaceGroupTopMap.containsKey(eig))
			return interfaceGroupTopMap.get(eig);
		else
			return -1;
	}

	/**
	 * Returns the bottom most EarthInterface layer index for the specific input
	 * EarthInterfaceGroup eig. If the EarthInterfaceGroup is not defined in the
	 * model then -1 is returned.
	 * 
	 * @param eig The EarthInterfaceGroup to be tested.
	 * @return The bottom most EarthInterface layer index of the group eig if that
	 *         group is defined in the model. Otherwise, -1 is returned.
	 */
	public int getBottomMostEarthInterface(EarthInterfaceGroup eig) {
		if (interfaceGroupBottomMap.containsKey(eig))
			return interfaceGroupBottomMap.get(eig);
		else
			return -1;
	}

	/**
	 * Standard toString dumps this objects information.
	 */
	@Override
	public String toString() {
		String s = toStringBenderInstanceEarthInterfaceNameMappings();
		s += "\n\n\n";
		s += toStringModelLayerToEarthInterfaceNameMappings();
		return s;
	}
	
	/**
	 * The toString method that outputs the Bender instance EarthInterface name
	 * mapping, which maps the names some or all of the names in the input
	 * GeoTessModel layer name description to valid EarthInterface names. The
	 * names are mapped based on the prescription of the Bender property
	 * "benderModelLayerToEarthInterfaceMap".
	 */
	public String toStringBenderInstanceEarthInterfaceNameMappings() {

		// If the bender instance interface name map is null or empty then
		// return an empty string.
		
		if ((benderInstanceInterfaceNameMap == null) || benderInstanceInterfaceNameMap.isEmpty())
			return "";
		
		// set up the header to output the common built-in interface name mappings.
		
		String s = "";
		s += "  Bender Instance Interface Name Mappings\n\n";
		s += "  (GeoTessModel Layers using an unknown interface\n" +
		     "   layer name are mapped to a corresponding\n" +
			 "   EarthInterface.)\n\n";
		s += "  Input Model Name    -->   EarthInterface\n\n";
		
		// loop over each Bender Instance model layer name mapping and show the
		// model layer name and it's associated EarthInterface name.
		
		for (Map.Entry<String, String> entry: benderInstanceInterfaceNameMap.entrySet()) {
			s += String.format("  %-20s      %-23s\n", entry.getKey(), entry.getValue());
		}

		return s;
	}
	
	/**
	 * The toString method that outputs the Bender instance EarthInterface name
	 * mapping, which maps the names some or all of the names in the input
	 * GeoTessModel layer name description to valid EarthInterface names. The
	 * names are mapped based on the prescription of the Bender property
	 * "benderModelLayerToEarthInterfaceMap".
	 */
	public String toStringModelLayerToEarthInterfaceNameMappings() {

		// set up the header to output the table.
		
		String s = "";
		s += "  Model layer names and associated EarthInterface names\n\n";
		s += "  Layer Index      Input Model Name        -->  EarthInterface\n\n";
		
		// loop over all model layer names and output the layer index, model layer name,
		// and associated valid EarthInterface name.
	
		for (int i = 0; i < modelLayerNames.length; ++i) {
			s += String.format("  %-11d      %-23s      %-23s\n", i, modelLayerNames[i],
					modelValidInterfaces[i].name());
		}

		return s;
	}
}
