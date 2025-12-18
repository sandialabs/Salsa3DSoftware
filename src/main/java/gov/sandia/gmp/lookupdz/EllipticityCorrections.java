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
package gov.sandia.gmp.lookupdz;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.io.GlobalInputStreamProvider;
import gov.sandia.gmp.util.io.InputStreamProvider.FileInputStreamProvider;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;

/**
 * References: Dziewonski, A. M. and F. Gilbert, 1976, The effect of small,
 * aspherical perturbations on travel times and a re-examination of the
 * corrections for ellipticity, Geophys. J. R. Astr. Soc., 44, 7-17.
 * 
 * Kennett, B. L. N. and O. Gudmundsson, 1996, Ellipticity corrections for
 * seismic phases, Geophys. J. Int., 127, p. 40-48.
 * 
 * @author sballar
 */

public class EllipticityCorrections {

	private EnumMap<SeismicPhase, Tau> tauMap;

	private final static double SQRT3_OVER2 = sqrt(3.) / 2.;

	/**
	 * 
	 * @param tableFile
	 * @param logger 
	 * @throws IOException
	 */
	public EllipticityCorrections(File tableFile, ScreenWriterOutput logger) throws IOException {
		tauMap = new EnumMap<SeismicPhase, Tau>(SeismicPhase.class);
		long timer = System.currentTimeMillis();

		String modelName = tableFile.getName();
		String phase = "-";

		FileInputStreamProvider fisp = GlobalInputStreamProvider.forFiles();

		if (fisp.isDirectory(tableFile)) {
			// if tableName refers to a directory, read the lookupTable files from the directory
			for (File f : fisp.listFiles(tableFile)) {
				int idx = f.getName().indexOf('.');
				phase = idx >= 0 ? f.getName().substring(idx+1) : f.getName();
				try (InputStream inputStream = new FileInputStream(f);) {
					tauMap.put(SeismicPhase.valueOf(phase), new Tau(inputStream, tableFile, phase));
				} catch (IllegalArgumentException e) {
				}
			}
		}
		else {
			// boolean jar will be true if the user requested tt models from a jar file or from the 
			// /src/main/resources directory if running from an IDE.
			boolean jar = tableFile.toPath().getName(0).toString().toLowerCase().equals("jar");

			try (ZipInputStream zipInputStream = new ZipInputStream( 
					jar ? EllipticityCorrections.class.getClassLoader().getResourceAsStream(tableFile.getName())
							: fisp.newStream(tableFile));) {
				ZipEntry zipEntry;
				while ((zipEntry = zipInputStream.getNextEntry()) != null) {
					// To extract the phase from the entry name, we have to ignore any path information
					// and keep only the extension if name includes the model name.
					String entryName = new File(zipEntry.getName()).getName(); // ignore path information
					int idx = entryName.lastIndexOf('.');
					phase = idx >= 0 ? entryName.substring(idx+1) : entryName;	// extract extension if name includes one.
					if (!zipEntry.isDirectory() && !phase.startsWith("_") && !phase.equals(modelName)) {
						try {
							tauMap.put(SeismicPhase.valueOf(phase), new Tau(zipInputStream, tableFile, phase));
						} catch (IllegalArgumentException e) {
						}
					}
					zipInputStream.closeEntry();
				}
			}
		}
		if (logger != null && logger.getVerbosity() > 0) 
			logger.writef("Loaded %3d ec models from %s in %s%n", tauMap.size(), tableFile, Globals.elapsedTime(timer));
	}

	public EnumMap<SeismicPhase, Tau> getTauMap() {
		return tauMap;
	}

	public boolean isSupported(SeismicPhase phase) {
		return tauMap.containsKey(phase);
	}

	public Set<SeismicPhase> getSupportedPhases() { 
		return EnumSet.copyOf(tauMap.keySet()); 
	}

	/**
	 * EllipTable interpolate member function. Interpolates an ellipticity
	 * correction from the associated lookup table. Based On the Function
	 * "get_ec_from_table" Walter Nagy, March 1993. Copyright (c) 1993-1996 Science
	 * Applications International Corporation.
	 * 
	 * @param phase
	 * @param event
	 * @param receiver
	 * @return double the ellipticity correction in seconds.  NaN if phase not supported.
	 */
	public double getEllipCorr(SeismicPhase phase, GeoVector receiver, GeoVector event) {
		Tau tau = tauMap.get(phase);

		if (tau == null)
			return Double.NaN;

		return tau.get_correction(phase, receiver, event);

	} // END table_correction

}
