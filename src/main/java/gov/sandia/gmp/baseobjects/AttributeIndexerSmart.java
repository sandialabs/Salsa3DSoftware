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
package gov.sandia.gmp.baseobjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.containers.multilevelmap.MultiLevelMap;

public class AttributeIndexerSmart extends MultiLevelMap {
	/**
	 * The unique set of Receivers that have been set in this model
	 */
	private Network network;

	/**
	 * Unique set of SeismicPhase objects that have specified in this model
	 */
	private EnumSet<SeismicPhase> supportedPhases;

	/**
	 * Unique set of GeoAttributes that have specified in this model
	 */
	private EnumSet<GeoAttributes> supportedAttributes;

	/**
	 * An array of length = size where each element is final key in the list of keys
	 * specified for the corresponding entry. Assumption is that the final key is a
	 * GeoAttribute.
	 */
	private ArrayList<GeoAttributes> geoAttributes;

	public AttributeIndexerSmart() {
		super();

		geoAttributes = new ArrayList<GeoAttributes>();
		network = new Network();
		supportedPhases = EnumSet.noneOf(SeismicPhase.class);
		supportedAttributes = EnumSet.noneOf(GeoAttributes.class);
	}

	public AttributeIndexerSmart(String[] attributeNames) throws IOException {
		this();

		for (String record : attributeNames) {
			String[] keyStrings = record.split("->");
			Object[] keys = new Object[keyStrings.length];
			for (int i = 0; i < keyStrings.length; ++i) {
				Scanner scanner = new Scanner(keyStrings[i].trim());
				String firstString = scanner.next().trim();
				if (firstString.equals("Receiver"))
					keys[i] = new Receiver(scanner);
				else if (firstString.equals("SeismicPhase"))
					keys[i] = SeismicPhase.valueOf(scanner.next().trim());
				else if (firstString.equals("GeoAttributes"))
					keys[i] = GeoAttributes.valueOf(scanner.next().trim());
				else
					try {
						// attempt to convert the string to a GeoAttribute
						// object.
						keys[i] = GeoAttributes.valueOf(firstString.toUpperCase());
					} catch (java.lang.IllegalArgumentException ex) {
						// if the attributeName is not a GeoAttributes
						// object, add it as a String
						keys[i] = firstString;
					}
			}
			addEntry(keys);
		}
	}

	@Override
	public int addEntry(Object... keys) throws IOException {
		int index = super.addEntry(keys);

		// keep track of the Receivers, SeismicPhases and GeoAttributes that
		// are added, independent of the other keys.
		for (Object key : keys)
			if (key instanceof Receiver)
				network.add((Receiver) key);
			else if (key instanceof SeismicPhase)
				supportedPhases.add((SeismicPhase) key);
			else if (key instanceof GeoAttributes)
				supportedAttributes.add((GeoAttributes) key);

		// if the last key is GeoAttribute, add it to list of GeoAttributes
		// otherwise add null. geoAttributes will end up being of length
		// size() with entries that are the last key in the list of keys
		// if the last key was a GeoAttribute, or null otherwise.
		if (keys.length > 0) {
			while (geoAttributes.size() < size())
				geoAttributes.add(null);
			if (keys[keys.length - 1] instanceof GeoAttributes)
				geoAttributes.set(size() - 1, (GeoAttributes) keys[keys.length - 1]);
		}

		return index;
	}

	/**
	 * Assumes that every time a set of keys was entered using the addEntry method
	 * the last key was a GeoAttribute. If that is true, then geoAttributes is
	 * 
	 * @return
	 */
	public ArrayList<GeoAttributes> getGeoAttributes() {
		return geoAttributes;
	}

	/**
	 * @param index
	 * @return the GeoAttribute of element i in the attribute array. Might be null.
	 */
	public GeoAttributes getAttribute(int index) {
		return geoAttributes.get(index);
	}

	/**
	 * @return the network of Receivers managed by this model.
	 */
	public Network getNetwork() {
		return network;
	}

	/**
	 * @return the Set of SeismicPhases supported by this model.
	 */
	public EnumSet<SeismicPhase> getSupportedPhases() {
		return supportedPhases;
	}

	/**
	 * @return the Set of GeoAttributes supported by this model.
	 */
	public EnumSet<GeoAttributes> getSupportedAttributes() {
		return supportedAttributes;
	}
}
