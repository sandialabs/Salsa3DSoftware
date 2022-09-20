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
package gov.sandia.gmp.baseobjects.globals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import gov.sandia.gmp.util.globals.Globals;

/**
 * An Enum class that contains official designations for Earth layer interfaces
 * used by GNEM ray tracers (e.g. Bender). Each layer interface has an
 * enumerated name designation, a corresponding layer name, a description, and
 * a EarthInterfaceGroup designation. A set of getters to access the layer name,
 * description, and interface group are provided.
 * </p>
 * 
 * This class also supports a common interface name mapping of typical interface
 * names found in existing GeoTessModels back to a proper EarthInterface name.
 * The method addCommonInterfaceName(String otherName, EarthInterface interfaceName)
 * can be used to add additional names if a GeoTessModel does not use any of the
 * EarthInterface or common interface names.
 * </p>
 * 
 * A set of static public methods are defined to allow interface name validation
 * including:
 * 
 * 		void validateInterfaceOrder(EarthInterface[] interfaces);
 * 		String getValidEarthInterfaceName(String name);
 * 		boolean isValidEarthInterfaceName(String name)
 * 
 * </p>
 */
public enum EarthInterface {
	ICB("Inner Core", "Inner Core Boundary", EarthInterfaceGroup.CORE),
	CMB("Outer Core", "Core-Mantle Boundary", EarthInterfaceGroup.CORE),
	M660("Lower Mantle", "Lower mantle boundary generally located at approximately 660 km depth.", EarthInterfaceGroup.MANTLE),
	M410("Mantle Transition Zone", "Middle mantle boundary generally located at approximately 410 km depth.", EarthInterfaceGroup.MANTLE),
	M210("Athenosphere", "Upper mantle boundary generally located at approximately 210 km depth.", EarthInterfaceGroup.MANTLE),
	MOHO("Lower Lithosphere", "Mohorovicic discontinuity or the boundary between the Earth's crust and mantle.", EarthInterfaceGroup.MANTLE),
	LOWER_CRUST_TOP("Lower Crust", "Lower Crust Top boundary.", EarthInterfaceGroup.CRUST),
	MIDDLE_CRUST_TOP("Middle Crust", "Middle Crust Top boundary.", EarthInterfaceGroup.CRUST),
	UPPER_CRUST_TOP("Upper Crust", "Upper Crust Top boundary.", EarthInterfaceGroup.CRUST),
	SEDIMENTARY_LAYER_5_TOP("Sedimentary Layer 5", "Sedimentary Layer 5 Top Boundary.", EarthInterfaceGroup.CRUST),
	SEDIMENTARY_LAYER_4_TOP("Sedimentary Layer 4", "Sedimentary Layer 4 Top Boundary.", EarthInterfaceGroup.CRUST),
	SEDIMENTARY_LAYER_3_TOP("Sedimentary Layer 3", "Sedimentary Layer 3 Top Boundary.", EarthInterfaceGroup.CRUST),
	SEDIMENTARY_LAYER_2_TOP("Sedimentary Layer 2", "Sedimentary Layer 2 Top Boundary.", EarthInterfaceGroup.CRUST),
	SEDIMENTARY_LAYER_1_TOP("Sedimentary Layer 1", "Sedimentary Layer 1 Top Boundary.", EarthInterfaceGroup.CRUST),
	SURFACE("Surface", "Top Layer of Earth (Topography)", EarthInterfaceGroup.CRUST),
	ICE_TOP("Ice", "Ice Surface", EarthInterfaceGroup.WATER) {
		/**
		 * ICE_TOP and WATER_TOP are homogeneous constant velocity layers.
		 * 
		 * @return True always.
		 */
		@Override
		public boolean isHomogeneousConstantVelocityLayer() {
			return true;
		}
	},
	WATER_TOP("Water", "Water Surface", EarthInterfaceGroup.WATER) {
		/**
		 * ICE_TOP and WATER_TOP are homogeneous constant velocity layers.
		 * 
		 * @return True always.
		 */
		@Override
		public boolean isHomogeneousConstantVelocityLayer() {
			return true;
		}
	},
	NONE("None", "Not an Interface", EarthInterfaceGroup.NOT_DEFINED) {
		/**
		 * NONE is not a layer.
		 * 
		 * @return True always.
		 */
		@Override
		public boolean isHomogeneousConstantVelocityLayer() {
			return true;
		}
	};

	/**
	 * Descriptive String.
	 */
	private String description;

	/**
	 * Default layer name associated with each boundary.
	 */
	private String defaultLayerName;

	/**
	 * The interface group (EarthInterfaceGroup) for this interface which can only be
	 * WATER, CRUST, MANTLE, or CORE.
	 */
	private EarthInterfaceGroup interfaceGroup;
	
	/**
	 * HashMap containing an association of common interface name keys associated with
	 * the matching EarthInterface name. This map is used to help convert existing
	 * GeoTessMetaData layer names to the equivalent EarthInterface name.
	 */
	private static HashMap<String, String> commonInterfaceNameMap = new HashMap<String, String>();
	static {
		commonInterfaceNameMap.put("INNER_CORE", EarthInterface.ICB.name());
		commonInterfaceNameMap.put("OUTER_CORE", EarthInterface.CMB.name());
		commonInterfaceNameMap.put("CORE", EarthInterface.CMB.name());
		commonInterfaceNameMap.put("MANTLE_TOP", EarthInterface.MOHO.name());
		commonInterfaceNameMap.put("MANTLE", EarthInterface.MOHO.name());
		commonInterfaceNameMap.put("LOWER_CRUST", EarthInterface.LOWER_CRUST_TOP.name());
		commonInterfaceNameMap.put("MIDDLE_CRUST", EarthInterface.MIDDLE_CRUST_TOP.name());
		commonInterfaceNameMap.put("UPPER_CRUST", EarthInterface.UPPER_CRUST_TOP.name());
		commonInterfaceNameMap.put("CRUST_TOP", EarthInterface.UPPER_CRUST_TOP.name());
		commonInterfaceNameMap.put("CRUST", EarthInterface.UPPER_CRUST_TOP.name());
		commonInterfaceNameMap.put("SEDIMENTARY_LAYER_1", EarthInterface.SEDIMENTARY_LAYER_1_TOP.name());
		commonInterfaceNameMap.put("SEDIMENTARY_LAYER_TOP", EarthInterface.SEDIMENTARY_LAYER_1_TOP.name());
		commonInterfaceNameMap.put("SEDIMENTARY_LAYER", EarthInterface.SEDIMENTARY_LAYER_1_TOP.name());
		commonInterfaceNameMap.put("SEDIMENTS", EarthInterface.SEDIMENTARY_LAYER_1_TOP.name());
		commonInterfaceNameMap.put("SEDIMENTARY_LAYER_2", EarthInterface.SEDIMENTARY_LAYER_2_TOP.name());
		commonInterfaceNameMap.put("SEDIMENTARY_LAYER_3", EarthInterface.SEDIMENTARY_LAYER_3_TOP.name());
		commonInterfaceNameMap.put("SEDIMENTARY_LAYER_4", EarthInterface.SEDIMENTARY_LAYER_4_TOP.name());
		commonInterfaceNameMap.put("SEDIMENTARY_LAYER_5", EarthInterface.SEDIMENTARY_LAYER_5_TOP.name());
		commonInterfaceNameMap.put("ICE", EarthInterface.ICE_TOP.name());
		commonInterfaceNameMap.put("WATER", EarthInterface.WATER_TOP.name());
	}

	/**
	 * Standard constructor that sets the layer name and description associated with
	 * a layer interface.
	 * 
	 * @param layerName   The layer name of the interface.
	 * @param description The layer description.
	 */
	private EarthInterface(String layerName, String description, EarthInterfaceGroup group) {
		this.description = description;
		this.defaultLayerName = layerName;
		this.interfaceGroup = group;
	}

	/**
	 * Returns the layer interface description.
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the default layer name of the interface.
	 * 
	 * @return The default layer name of the interface.
	 */
	public String getDefaultLayerName() {
		return defaultLayerName;
	}

	/**
	 * Returns the EarthInterfaceGroup of this interface.
	 * 
	 * @return The EarthInterfaceGroup of this interface.
	 */
	public EarthInterfaceGroup getInterfaceGroup() {
		return interfaceGroup;
	}
	
	/**
	 * Returns true if the layer is a homogeneous constant velocity layer. Defaults
	 * to false.
	 * 
	 * @return True if the layer is a homogeneous constant velocity layer.
	 */
	public boolean isHomogeneousConstantVelocityLayer() {
		return false;
	}

	/**
	 * Throws an error if the input EarthInterface array is out of order (must be
	 * deepest as the first entry and the most shallow as the last entry).
	 * 
	 * @param interfaces The array of EarthInterface's to be order tested.
	 * @throws IOException
	 */
	static public void validateInterfaceOrder(EarthInterface[] interfaces) throws IOException {
		for (int i = 1; i < interfaces.length; ++i) {
			if (interfaces[i].ordinal() <= interfaces[i - 1].ordinal())
				throw new IOException(Globals.NL
						+ "Error: EarthInterface Input Order was INVALID between" + Globals.NL
						+ "       interface " + interfaces[i].name() + " (later) and interface "
						+ interfaces[i - 1].name() + " (earlier) ...." + Globals.NL);
		}
	}

	/**
	 * Returns a valid EarthInterface name matching the input name if there is one
	 * defined. Otherwise null is returned.
	 * 
	 * @param name The name for which a valid EarthInterface name is returned.
	 * @return If the name is already valid it is returned. If not valid and a
	 *         matching repair name is found the valid name is returned. Otherwise,
	 *         null is returned.
	 */
	static public String getValidMappedEarthInterfaceName(String name) {
		if (isValidEarthInterfaceName(name))
			return name.toUpperCase();
		else
			return commonInterfaceNameMap.get(name.toUpperCase());
	}

	/**
	 * Returns a valid EarthInterface matching the input name if there is one
	 * defined. Otherwise null is returned.
	 * 
	 * @param name The name for which a valid EarthInterface is returned.
	 * @return If the name is does not equal and existing EarthInterface name, or a
	 *         name mapped to an existing EarthInterface, then null is returned.
	 */
	static public EarthInterface getValidMappedEarthInterface(String name) {
		if (isValidEarthInterfaceName(name))
			return EarthInterface.valueOf(name.toUpperCase());
		else if (isValidMappedEarthInterfaceName(name))
			return EarthInterface.valueOf(commonInterfaceNameMap.get(name.toUpperCase()));
		else
			return null;
	}

	/**
	 * Returns true if the input name is a valid EarthInterface name.
	 * 
	 * @param name The name to be tested.
	 * @return True if the input name is a valid EarthInterface name.
	 */
	static public boolean isValidEarthInterfaceName(String name) {
		try {
			// if name is valid return it

			EarthInterface.valueOf(name.toUpperCase());
		} catch (Exception ex) {
			return false;
		}

		return true;
	}

	/**
	 * Returns true if the input name is a valid (i.e. existing) EarthInterface name
	 * or a common interface name mapping.
	 * 
	 * @param name The name to be tested.
	 * @return True if the input name is a valid EarthInterface name or a common
	 *         interface name mapping.
	 */
	static public boolean isValidMappedEarthInterfaceName(String name) {
		if (isValidEarthInterfaceName(name))
			return true;
		else if (commonInterfaceNameMap.containsKey(name.toUpperCase()))
			return true;
		else
			return false;
	}

	/**
	 * Finds the first defined interface in the input map of model interface names
	 * (modelInterfaces) beginning with the interface with the name
	 * startInterfaceName. Only interfaces in the same EarthInterfaceGroup as
	 * startInterfaceName are checked. If the interface associated with
	 * startInterfaceName is defined in modelInterfaces, then that interface is
	 * returned. If not, then the next interface above the start interface, if it is
	 * in the same EarthInterfaceGroup, is checked for containment. If it is in
	 * the same group, and it is contained, then it is returned. This process
	 * continues until a contained interface is found, or until the next interface
	 * to be checked is out of the group of the interface associated with
	 * startInterfaceName. If this happens then null is returned, indicating no
	 * interfaces are defined in the model that lie above the interface associated
	 * with startInterfacename that have the same group as startInterface.
	 * 
	 * @param startInterfaceName The name of the first interface to be checked for
	 *                           definition within the map of model interfaces
	 *                           (modelInterfaces).
	 * @param modelInterfaces    The map of valid model interfaces to be checked for
	 *                           the lowest index interface, beginning with the
	 *                           interface associated with startInterfaceName, that
	 *                           is also in the same EarthInterfaceGroup as
	 *                           startInterfaceName.
	 * @return The lowest discovered interface defined in the map of input valid
	 *         model interfaces (modelInterfaces), beginning with the interface
	 *         associated with startInterfaceName. If no defined interfaces with the
	 *         same EarthInterfaceGroup as startInterfaceName are discovered then
	 *         null is returned.
	 */
	static public EarthInterface findLowestDefinedLayerAboveInGroup(String startInterfaceName,
			HashMap<String, Integer> modelInterfaces) throws IOException {

		// get the interface associated with the input name startInterfaceName. If not defined
		// throw an error.
		
		EarthInterface startInterface = null;
		try {
			startInterface = EarthInterface.valueOf(startInterfaceName.toUpperCase());
		} catch (Exception ex) {
			throw new IOException("\nError: The input interface name \"" + startInterfaceName
					+ "\" is not a valid EarthInterface Name ...\n");
		}
		
		// find the lowest defined layer in the group shared with startInterface
		
		return findLowestDefinedLayerAboveInGroup(startInterface, modelInterfaces);
	}

	/**
	 * Finds the first defined interface in the input map of model interface names
	 * (modelInterfaces) beginning with the interface startInterface. Only
	 * interfaces in the same EarthInterfaceGroup as startInterface are checked. If
	 * startInterface is defined in modelInterfaces, then startInterface is
	 * returned. If not, then the next interface above startInterface, if it is in
	 * the same EarthInterfaceGroup, is checked for containment. If it is in
	 * the same group, and it is contained, then it is returned. This process
	 * continues until a contained interface is found, or until the next interface
	 * to be checked is out of startInterface group. If this happens then null is
	 * returned, indicating no interfaces are defined in the model that lie above
	 * startInterface that have the same group as startInterface.
	 * 
	 * @param startInterface  The first interface to be checked for definition
	 *                        within the map of model interfaces (modelInterfaces).
	 * @param modelInterfaces The map of valid model interfaces to be checked for
	 *                        the lowest index interface, beginning with
	 *                        startInterface, that is also in the same
	 *                        EarthInterfaceGroup as startInterface.
	 * @return The lowest discovered interface defined in the map of input valid
	 *         model interfaces (modelInterfaces), beginning with startInterface. If
	 *         no defined interfaces with the same EarthInterfaceGroup as
	 *         startInterface are discovered then null is returned.
	 */
	static public EarthInterface findLowestDefinedLayerAboveInGroup(EarthInterface startInterface,
			HashMap<String, Integer> modelInterfaces) {

		// get the EarthInterfaceGroup of startInterface and start with that interface
		// looking for one defined in the input model interface map. Continue in an upwards
		// direction searching for the next contained interface. Stop if the next
		// interface is not of the same group
		
		EarthInterfaceGroup startInterfaceGroup = startInterface.interfaceGroup;
		for (int i = startInterface.ordinal();
				EarthInterface.values()[i].getInterfaceGroup() == startInterfaceGroup; ++i) {
			if (modelInterfaces.containsKey(EarthInterface.values()[i].name()))
				return EarthInterface.values()[i];
		}

		// no interfaces at or above startInterface in the same group as startInterface were
		// discovered. return null
		
		return null;
	}

	/**
	 * Finds the first defined interface in the input map of model interface names
	 * (modelInterfaces) beginning with the interface with the name
	 * startInterfaceName. Only interfaces in the same EarthInterfaceGroup as
	 * startInterfaceName are checked. If the interface associated with
	 * startInterfaceName is defined in modelInterfaces, then that interface is
	 * returned. If not, then the next interface below the start interface, if it is
	 * in the same EarthInterfaceGroup, is checked for containment. If it is in the
	 * same group, and it is contained, then it is returned. This process continues
	 * until a contained interface is found, or until the next interface to be
	 * checked is out of the group of the interface associated with
	 * startInterfaceName or no more interfaces remain to be checked. If this
	 * happens then null is returned, indicating no interfaces are defined in the
	 * model that lie below the interface associated with startInterfacename that
	 * have the same group as startInterface.
	 * 
	 * @param startInterfaceName The name of the first interface to be checked for
	 *                           definition within the map of model interfaces
	 *                           (modelInterfaces).
	 * @param modelInterfaces    The map of valid model interfaces to be checked for
	 *                           the lowest index interface, beginning with the
	 *                           interface associated with startInterfaceName, that
	 *                           is also in the same EarthInterfaceGroup as
	 *                           startInterfaceName.
	 * @return The lowest discovered interface defined in the map of input valid
	 *         model interfaces (modelInterfaces), beginning with the interface
	 *         associated with startInterfaceName. If no defined interfaces with the
	 *         same EarthInterfaceGroup as startInterfaceName are discovered then
	 *         null is returned.
	 */
	static public EarthInterface findHighestDefinedLayerBelowInGroup(String startInterfaceName,
			HashMap<String, Integer> modelInterfaces) throws IOException {

		// get the interface associated with the input name startInterfaceName. If not defined
		// throw an error.
		
		EarthInterface startInterface = null;
		try {
			startInterface = EarthInterface.valueOf(startInterfaceName.toUpperCase());
		} catch (Exception ex) {
			throw new IOException("\nError: The input interface name \"" + startInterfaceName
					+ "\" is not a valid EarthInterface Name ...\n");
		}
		
		// find the highest defined layer in the group shared with startInterface
		
		return findHighestDefinedLayerBelowInGroup(startInterface, modelInterfaces);
	}

	/**
	 * Finds the first defined interface in the input map of model interface names
	 * (modelInterfaces) beginning with the interface startInterface. Only
	 * interfaces in the same EarthInterfaceGroup as startInterface are checked. If
	 * startInterface is defined in modelInterfaces, then startInterface is
	 * returned. If not, then the next interface below startInterface, if it is in
	 * the same EarthInterfaceGroup, is checked for containment. If it is in the
	 * same group, and it is contained, then it is returned. This process continues
	 * until a contained interface is found, or until the next interface to be
	 * checked is out of startInterface group or no more interfaces are defined. If
	 * this happens then null is returned, indicating no interfaces are defined in
	 * the model that lie below startInterface that have the same group as
	 * startInterface.
	 * 
	 * @param startInterface  The first interface to be checked for definition
	 *                        within the map of model interfaces (modelInterfaces).
	 * @param modelInterfaces The map of valid model interfaces to be checked for
	 *                        the highest index interface, beginning with
	 *                        startInterface, that is also in the same
	 *                        EarthInterfaceGroup as startInterface.
	 * @return The highest discovered interface defined in the map of input valid
	 *         model interfaces (modelInterfaces), beginning with startInterface. If
	 *         no defined interfaces with the same EarthInterfaceGroup as
	 *         startInterface are discovered then null is returned.
	 */
	static public EarthInterface findHighestDefinedLayerBelowInGroup(EarthInterface startInterface,
			HashMap<String, Integer> modelInterfaces) {

		// get the EarthInterfaceGroup of startInterface and start with that interface
		// looking for one defined in the input model interface map. Continue in an downwards
		// direction searching for the next contained interface. Stop if the next
		// interface is not of the same group or if i < 0
		
		EarthInterfaceGroup startInterfaceGroup = startInterface.interfaceGroup;
		for (int i = startInterface.ordinal();(i >= 0) &&
				(EarthInterface.values()[i].getInterfaceGroup() == startInterfaceGroup); --i) {
			if (modelInterfaces.containsKey(EarthInterface.values()[i].name()))
				return EarthInterface.values()[i];
		}

		// no interfaces at or below startInterface in the same group as startInterface were
		// discovered. return null

		return null;
	}
	
	/**
	 * The toString method for the entire EarthInterface class. Outputs tables
	 * showing the index, name, default layer name, interface group, and description
	 * of each valid EarthInterface entry. This method also outputs the common
	 * interface name mapping, which automatically maps the names of commonly used
	 * layers in GeoTessModels to their corresponding valid EarthInterface
	 * equivalent.
	 */
	static public String toStringOut() {
		
		// set up the EarthInterface table header
		
		String s = toStringEarthInterfaceDefinitions();
		s += "\n\n\n";
		s += toStringCommonEarthInterfaceNameMappings();
		return s;
	}

	/**
	 * The toString method that outputs the EarthInterface definition. The output is
	 * in the form of a table containing the index, name, default layer name,
	 * interface group, and description of each valid EarthInterface entry.
	 */
	static public String toStringEarthInterfaceDefinitions() {
		
		// set up the EarthInterface table header
		
		String s = "                                Earth Interface Entries\n\n";
		s += " Index    Name                        LayerName                 Group     Description\n\n";
		
		// get the EarthInterface array and loop over each. Output their index, name,
		// default layer name, interface group, and description to the table.
		
		EarthInterface[] interfaces = EarthInterface.values();
		for (int i = 0; i < interfaces.length - 1; ++i) {
			EarthInterface interfacei = interfaces[i];
			s += String.format("  %-2d      %-23s     %-22s    %-6s    %-20s\n",
					i, interfacei.name(), interfacei.defaultLayerName,
					interfacei.interfaceGroup.name(), interfacei.description);
		}
		
		return s;
	}
	
	/**
	 * The toString method that outputs the common interface name mapping, which
	 * automatically maps the names of commonly used layers in GeoTessModels to
	 * their corresponding valid EarthInterface equivalent.
	 */
	static public String toStringCommonEarthInterfaceNameMappings() {
		
		// set up the header to output the common built-in interface name mappings.
		
		String s = "";
		s += "  Common Built-In Interface Name Mappings\n\n";
		s += "  (GeoTessModel Layers using a common interface\n" +
		     "   layer name are automatically mapped to the\n" +
			 "   corresponding EarthInterface.)\n\n\n";
		s += "  Common Interface   -->    EarthInterface\n\n";
		
		// loop over each common interface name mapping and show the common layer name
		// and it's associated EarthInterface name.
		
		for (Map.Entry<String, String> entry: commonInterfaceNameMap.entrySet()) {
			s += String.format("  %-20s      %-23s\n", entry.getKey(), entry.getValue());
		}

		return s;
	}
}
