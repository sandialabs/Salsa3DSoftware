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
package gov.sandia.gmp.baseobjects.uncertainty;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.io.GlobalInputStreamProvider;

public class UncertaintyAzimuthSlowness
{
	//This map equals the parsed contents of azimuth_slowness_uncertainty.dat, which will never change
	//from one LookupTable run to the next. This means every time we call
	//"new UncertaintyAzimuthSlowness()", we don't need to re-read this file:
	private static Map<String, Map<String, Map<String, double[]>>> UNCERTAINTY_MAP = new LinkedHashMap<>();

	// # station phase slo_unc (s/dg) az_unc (dg)
	// * * 0.31 2.1
	// * P 0.30 2.0
	// QSPA * 0.50 5.0
	// WRA P 0.55 5.5

	/**
	 * map from station name -> seismic phase -> [slowness uncertainty, azimuth uncertainty] in
	 * seconds/degree and degrees
	 */
	private Map<String, Map<String, double[]>> uncertainty;

	private String modelFile;
	
	static private final String modelFileDefault = "internal_resources.azimuth_slowness_uncertainty.dat";

	public UncertaintyAzimuthSlowness() throws Exception {
		modelFile = modelFileDefault;
		uncertainty = UNCERTAINTY_MAP.get(modelFile);
		if (uncertainty == null) {
			InputStream stream = Utils.getResourceAsStream("azimuth_slowness_uncertainty.dat");
			if (stream == null) {
				stream = new FileInputStream(new File("../base-objects/src/main/resources/azimuth_slowness_uncertainty.dat"));
			}
			uncertainty = read(stream);
			UNCERTAINTY_MAP.put(modelFile, uncertainty);
		}
	}

	public UncertaintyAzimuthSlowness(File f) throws Exception {
		modelFile = f.getCanonicalPath();
		uncertainty = UNCERTAINTY_MAP.get(modelFile);
		if (uncertainty == null) {
			UNCERTAINTY_MAP.put(modelFile, uncertainty = read(GlobalInputStreamProvider.forFiles().newStream(
					new File(modelFile))));			
		}
	}

	public UncertaintyAzimuthSlowness(PropertiesPlusGMP properties, String prefix) throws Exception {
		File f = properties.getFile(prefix+"AzSloUncertaintyFile");
		if (f == null) {
			modelFile = modelFileDefault;
			uncertainty = UNCERTAINTY_MAP.get(modelFile);
			if (uncertainty == null) 
				UNCERTAINTY_MAP.put(modelFile, uncertainty = read(Utils.getResourceAsStream(
						"azimuth_slowness_uncertainty.dat")));
		}
		else {
			modelFile = f.getCanonicalPath();
			uncertainty = UNCERTAINTY_MAP.get(modelFile);
			if (uncertainty == null) 
				UNCERTAINTY_MAP.put(modelFile, uncertainty = read(GlobalInputStreamProvider.forFiles().newStream(new File(modelFile))));			
		}
	}

	/**
	 * Used for junit testing.  Uncertainty will not be stored in static map.
	 * @param records
	 * @throws Exception
	 */
	public UncertaintyAzimuthSlowness(String records) throws Exception {
		modelFile = "input_records";
		uncertainty = read(records);
	}

	/**
	 * get slowness uncertainty in seconds/radian
	 * 
	 * @param sta
	 * @param phase
	 * @return
	 * @throws Exception
	 */
	public double getSloUncertainty(String sta, String phase) throws Exception {
		return getUncertainty(sta, phase)[0];
	}

	/**
	 * get azimuth uncertainty in radians
	 * 
	 * @param sta
	 * @param phase
	 * @return
	 * @throws Exception
	 */
	public double getAzUncertainty(String sta, String phase) throws Exception {
		return getUncertainty(sta, phase)[1];
	}

	/**
	 * get slowness and azimuth uncertainties in seconds/radian and radians
	 * 
	 * @param sta
	 * @param phase
	 * @return
	 * @throws Exception
	 */
	public double[] getUncertainty(String sta, String phase) throws Exception {
		Map<String, double[]> phMap = uncertainty.get(sta);
		if (phMap == null)
			phMap = uncertainty.get("*");
		double[] u = phMap.get(phase);
		if (u == null)
			u = phMap.get("*");
		if (u == null)
			u = uncertainty.get("*").get(phase);
		if (u == null)
			u = uncertainty.get("*").get("*");
		if (u == null)
			throw new Exception(
					"azimuth_slowness_uncertainty table does not have a default entry for sta=* and phase=*");

		return new double[] {Math.toDegrees(u[0]), Math.toRadians(u[1])};
	}

	private Map<String, Map<String, double[]>> read(InputStream stream) throws Exception {
		String s = "";
		Scanner input = new Scanner(stream);
		while (input.hasNextLine())
			s += input.nextLine() + "\n";
		input.close();
		return read(s);
	}

	private Map<String, Map<String, double[]>> read(String string) throws Exception {
		Map<String, Map<String, double[]>> uncertainty = new LinkedHashMap<>();
		Scanner input = new Scanner(string);
		while (input.hasNext()) {
			String line = input.nextLine().trim();
			if (!line.startsWith("#")) {
				String[] tokens = line.split("\\s+");
				// tokens are: 0:sta, 1:phase, 2:slo uncertainty, 3:az uncertainty
				if (tokens.length == 4) {
					Map<String, double[]> phMap = uncertainty.get(tokens[0]);
					if (phMap == null)
						uncertainty.put(tokens[0], phMap = new LinkedHashMap<>());
					phMap.put(tokens[1], new double[] {Double.valueOf(tokens[2]), Double.valueOf(tokens[3])});
				}
			}
		}
		input.close();

		if (uncertainty.get("*") == null || uncertainty.get("*").get("*") == null) {
			throw new Exception(
					"Default values are not specified (no line like \" * * 0.5, 5.0\")\n" );
		}

		return uncertainty;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer("# station phase slo_unc (s/dg) az_unc (dg)\n");
		for (Entry<String, Map<String, double[]>> e1 : uncertainty.entrySet()) {
			String sta = e1.getKey();
			for (Entry<String, double[]> e2 : e1.getValue().entrySet()) {
				String phase = e2.getKey();
				double uslo = e2.getValue()[0];
				double uaz = e2.getValue()[1];
				buf.append(String.format("%-6s %-6s %6.2f %6.2f%n", sta, phase, uslo, uaz));
			}
		}
		return buf.toString();
	}

	static public String getVersion() {
		return Utils.getVersion("base-objects");
	}

	public String getUncertaintyModelFile() {
		return modelFile;
	}

}
