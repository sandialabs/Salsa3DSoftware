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

import java.util.HashMap;

/**
 * An Enum class that contains four major Earth model layer groups including:
 * WATER, CRUST, MANTLE, and CORE. These groups are assigned to each
 * EarthInterface layer definition, and used by ray tracers (e.g. Bender)
 * to help limit the search domain during ray tracing of the bottom of
 * specific phases.
 * </p>
 *
 */
public enum EarthInterfaceGroup {
	CORE,
	MANTLE,
	CRUST,
	WATER,
	NOT_DEFINED;
	
	/**
	 * Given the input interface list a map of interface groups will be returned
	 * associated with the last (top) interface index of the group. Groups not
	 * represented in the interface list will not be returned in the input map.
	 * 
	 * @param interfaces           An input array in sorted order of some
	 *                             GeoTessModels EarthInterfaces.
	 * @param interfaceGroupTopMap A map of all supported EarthInterfaceGroups
	 *                             Defined in the input interface list (interfaces)
	 *                             associated with the last (top) index of an
	 *                             interface in the input list.
	 */
	public static void getEarthInterfaceGroupTop(
			EarthInterface[] interfaces,
			HashMap<EarthInterfaceGroup, Integer> interfaceGroupTopMap) {
	
		// clear the map and loop through all interfaces from first to last
		// and store the interface group associated with the interface index.
		// The last index for all represented groups will be saved associated
		// with the group. Non represented groups in the interface list will
		// not be defined in the output map.
		
		interfaceGroupTopMap.clear();
		for (int i = 0; i < interfaces.length; ++i) {
			interfaceGroupTopMap.put(interfaces[i].getInterfaceGroup(), i);
		}
	}
	
	/**
	 * Given the input interface list a map of interface groups will be returned
	 * associated with the first (bottom) interface index of the group. Groups not
	 * represented in the interface list will not be returned in the input map.
	 * 
	 * @param interfaces              An input array in sorted order of some
	 *                                GeoTessModels EarthInterfaces.
	 * @param interfaceGroupBottomMap A map of all supported EarthInterfaceGroups
	 *                                Defined in the input interface list
	 *                                (interfaces) associated with the first (bottom)
	 *                                index of an interface in the input list.
	 */
	public static void getEarthInterfaceGroupBottom(
			EarthInterface[] interfaces,
			HashMap<EarthInterfaceGroup, Integer> interfaceGroupBottomMap) {
		
		// clear the map and loop through all interfaces from last to the first
		// and store the interface group associated with the interface index.
		// The last index for all represented groups will be saved associated
		// with the group. Non represented groups in the interface list will
		// not be defined in the output map.
		
		interfaceGroupBottomMap.clear();
		for (int i = interfaces.length-1; i >= 0; --i) {
			interfaceGroupBottomMap.put(interfaces[i].getInterfaceGroup(), i);
		}
	}
};
