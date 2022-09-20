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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

public class PropertiesPlusGMP extends PropertiesPlus implements Serializable {
	private static final long serialVersionUID = -1913389127820892882L;

	public PropertiesPlusGMP() {
		super();
	}

	public PropertiesPlusGMP(String inputString) {
		super(inputString);
	}

	public PropertiesPlusGMP(File file) throws IOException {
		super(file);
	}

	public PropertiesPlusGMP(InputStream inputStream) {
		super(inputStream);
	}

	public PropertiesPlusGMP(PropertiesPlus properties) {
		super(properties);
	}

	/**
	 * Retrieve the value of specified property as a GeoVector. Input 2, 3 or 4
	 * values: latitude (required), longitude (required), depth (optional, defaults
	 * to 0), inDegrees (optional, defaults to true). If inDegrees is true, then
	 * latitude and longitude are assumed to be degrees. If false, they are assumed
	 * to be in radians.
	 * <p>
	 * The shape of the earth must be specified with property EarthShape in this
	 * property file.
	 * 
	 * @param property String latitude (required), longitude (required), depth
	 *                 (optional, defaults to 0), inDegrees (optional, defaults to
	 *                 true).
	 * @return GeoVector
	 * @throws PropertiesPlusException if EarthShape not defined or value cannot be
	 *                                 converted to a GeoVector
	 * @throws PropertiesPlusException
	 */
	public GeoVector getGeoVector(String property) throws PropertiesPlusException {
		String value = getProperty(property);

		if (value == null)
			throw new PropertiesPlusException(property + " is not defined.");
		try {
			Scanner str = new Scanner(value.replaceAll(",", " "));
			GeoVector gv = new GeoVector(str.nextDouble(), // lat
					str.nextDouble(), // lon
					(str.hasNext() ? str.nextDouble() : 0.), // depth
					(str.hasNext() ? str.nextBoolean() : true)); // inDegrees
			str.close();
			return gv;
		} catch (Exception ex) {
			throw new PropertiesPlusException(
					String.format("%s = %s cannot be converted to type GeoVector", property, value));
		}
	}

	/**
	 * Parse a list of SeismicPhase names and return ArrayList of SeismicPhase
	 * enums.
	 * 
	 * @param property
	 * @return ArrayList of SeismicPhase enums.
	 * @throws PropertiesPlusException
	 */
	public ArrayList<SeismicPhase> getSeismicPhases(String property) throws PropertiesPlusException {
		String value = getProperty(property);
		if (value == null)
			return new ArrayList<SeismicPhase>();
		String[] phaseNames = value.replaceAll(",", " ").split(" ");
		ArrayList<SeismicPhase> phases = new ArrayList<SeismicPhase>(phaseNames.length);
		for (String n : phaseNames)
			if (SeismicPhase.valueOf(n) != null)
				phases.add(SeismicPhase.valueOf(n));
		return phases;
	}

	/**
	 * Return the value of the specified property as a SeismicPhase enum.
	 * 
	 * @param property
	 * @return a SeismicPhase enum.
	 * @throws PropertiesPlusException
	 */
	public SeismicPhase getSeismicPhase(String property) throws PropertiesPlusException {
		return getSeismicPhase(property, null);
	}

	/**
	 * Return the value of the specified property as a SeismicPhase enum.
	 * 
	 * @param property
	 * @return a SeismicPhase enum.
	 * @throws PropertiesPlusException
	 */
	public SeismicPhase getSeismicPhase(String property, SeismicPhase defaultValue) throws PropertiesPlusException {
		String value = getProperty(property);

		if (value == null) {
			if (defaultValue == null)
				throw new PropertiesPlusException(property + " is not defined.");

			addRequestedProperty(property, defaultValue.toString());
			return defaultValue;
		}

		return SeismicPhase.valueOf(value);

	}

	/**
	 * Retrieve a ArrayList<GeoAttributes> from the properties file.
	 * 
	 * @param property
	 * @return ArrayList<GeoAttributes>
	 * @throws PropertiesPlusException
	 */
	public ArrayList<GeoAttributes> getGeoAttributes(String property) throws PropertiesPlusException {
		String value = getProperty(property);

		if (value == null)
			throw new PropertiesPlusException(property + " is not defined.");

		ArrayList<GeoAttributes> attributes = new ArrayList<GeoAttributes>();
		try {
			Scanner str = new Scanner(value.replaceAll(",", " "));
			while (str.hasNext())
				attributes.add(GeoAttributes.valueOf(str.next().toUpperCase()));
			str.close();
		} catch (Exception ex) {
			throw new PropertiesPlusException(
					String.format("%s = %s cannot be converted to type EnumSet<GeoAttributes>", property, value));
		}
		return attributes;
	}

	/**
	 * Retrieve a ArrayList<GeoAttributes> from the properties file.
	 * 
	 * @param property
	 * @return ArrayList<GeoAttributes>
	 * @throws PropertiesPlusException
	 */
	public ArrayList<GeoAttributes> getGeoAttributes(String property, String defaultValue)
			throws PropertiesPlusException {
		String value = getProperty(property);

		if (value == null) {
			if (defaultValue == null)
				throw new PropertiesPlusException(property + " is not defined.");

			addRequestedProperty(property, defaultValue);
			value = defaultValue;
		}

		ArrayList<GeoAttributes> attributes = new ArrayList<GeoAttributes>();
		try {
			Scanner str = new Scanner(value.replaceAll(",", " "));
			while (str.hasNext())
				attributes.add(GeoAttributes.valueOf(str.next().toUpperCase()));
			str.close();
		} catch (Exception ex) {
			throw new PropertiesPlusException(
					String.format("%s = %s cannot be converted to type EnumSet<GeoAttributes>", property, value));
		}
		return attributes;

	}

}
