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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.seismicbasedata.SeismicBaseData;

/**
 * 
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
	/**
	 * directory will be something like ../el/ak135
	 */
	private final File directory;

	/**
	 * directory will be something like .../el/ak135
	 * 
	 * @return
	 */
	public File getDirectory() {
		return directory;
	}

//	/**
//	 * Set of SeismicPhases that are supported.  In order to make
//	 * it into this list, a file with the name of the phase has to exist in the right directory.
//	 */
//	protected static  EnumSet<SeismicPhase> supportedPhases = EnumSet.noneOf(SeismicPhase.class);

	class Tau {
		/**
		 * distances, in degrees, at which ellipticity coefficients are stored in tables
		 */
		float[] distance;

		/**
		 * depths, in km, at which ellipticity coefficients are stored in tables
		 */
		float[] depth;

		/**
		 * ellipticity coefficients at various distances and depths.
		 */
		float[][][] coeff;
	}

	private Map<SeismicPhase, Tau> tauMap = new HashMap<SeismicPhase, Tau>();

	private final static double SQRT3_OVER2 = Math.sqrt(3.) / 2.;

	/**
	 * Ellipticity coefficients loaded from file stored in the jar file.
	 * 
	 * @throws FileNotFoundException
	 */
	public EllipticityCorrections() throws FileNotFoundException {
		super();
		this.directory = new File("seismic-base-data.jar/el/ak135");
		// loadSupportedPhases();
	}

	/**
	 * 
	 * @param directory
	 * @throws FileNotFoundException
	 */
	public EllipticityCorrections(File directory) throws FileNotFoundException {
		super();
		this.directory = directory;
		// loadSupportedPhases();
	}

//	private void loadSupportedPhases() throws FileNotFoundException
//	{
//		for (SeismicPhase phase : EnumSet.allOf(SeismicPhase.class))
//			if (getFile(phase).exists())
//				supportedPhases.add(phase);
//	}

	private File getFile(SeismicPhase phase) throws FileNotFoundException {
		File f = new File(directory, "ec_ak135." + phase.getFileName());

		if (!f.exists())
			f = new File(directory, phase.getFileName());

		return f;
	}

//	static public String checkFiles(PropertiesPlusGMP properties, String propertyPrefix) 
//	throws PropertiesPlusException
//	{
//		String errorMessage = "";
//		File directory = properties.getFile(propertyPrefix+"EllipticityCorrectionsDirectory");
//		if (directory == null)
//			errorMessage += "Property "+propertyPrefix+"EllipticityCorrectionsDirectory"
//					+" not specified in property file"+Globals.NL;
//		else if (!directory.exists())
//			errorMessage += "Directory "+directory+" does not exist."+Globals.NL;
//		
//		return errorMessage;
//		
//	}

	/**
	 * EllipTable interpolate member function. Interpolates an ellipticity
	 * correction from the associated lookup table. Based On the Function
	 * "get_ec_from_table" Walter Nagy, March 1993. Copyright (c) 1993-1996 Science
	 * Applications International Corporation.
	 * 
	 * @param phase
	 * @param event
	 * @param receiver
	 * @return double the ellipticity correction in seconds
	 * @throws IOException
	 */
	public double getEllipCorr(SeismicPhase phase, GeoVector receiver, GeoVector event) throws IOException {
		Tau tau = getTau(phase);

		double ev_sta_dist = event.distanceDegrees(receiver);
		double ev_sta_azim = event.azimuth(receiver, Double.NaN);
		double ev_geoc_co_lat = Math.PI / 2. - event.getGeocentricLat();
		double ev_depth = Math.max(0., event.getDepth());

		int ix, iz, n;
		int ix1, ix2, iz1, iz2;
		double ellip_corr = 0.0;
		double dist_fac, depth_fac;
		double a, b, c, d;
		double tau0, tau1, tau2;

		// --------------------------------------------------------------------
		// Reset reference constants, if event co-latitude has changed.
		// --------------------------------------------------------------------
		double etSc0 = 0.25 * (1.0 + 3.0 * Math.cos(2.0 * ev_geoc_co_lat));
		double etSc1 = SQRT3_OVER2 * Math.sin(2.0 * ev_geoc_co_lat);
		double etSc2 = SQRT3_OVER2 * Math.sin(ev_geoc_co_lat) * Math.sin(ev_geoc_co_lat);

		// --------------------------------------------------------------------
		// Find high-ends of both distance (ix2) and depth (iz2) indexes.
		// --------------------------------------------------------------------
		n = tau.distance.length;
		for (ix2 = n - 1, ix = 1; ix < n; ix++) {
			if (ev_sta_dist < tau.distance[ix]) {
				ix2 = ix;
				break;
			}
		}
		ix1 = ix2 - 1; // Low-end distance index

		n = tau.depth.length;
		for (iz2 = n - 1, iz = 1; iz < n; iz++) {
			if (ev_depth < tau.depth[iz]) {
				iz2 = iz;
				break;
			}
		}
		iz1 = iz2 - 1; // Low-end depth index

		dist_fac = (ev_sta_dist - tau.distance[ix1]) / (tau.distance[ix2] - tau.distance[ix1]);
		depth_fac = (ev_depth - tau.depth[iz1]) / (tau.depth[iz2] - tau.depth[iz1]);

		// --------------------------------------------------------------------
		// Compute tau coefficients of Dziewonski and Gilbert (1976).
		// --------------------------------------------------------------------
		// tau0
		a = tau.coeff[0][iz1][ix1];
		b = tau.coeff[0][iz2][ix1];
		c = a + (tau.coeff[0][iz1][ix2] - a) * dist_fac;
		d = b + (tau.coeff[0][iz2][ix2] - b) * dist_fac;
		tau0 = c + (d - c) * depth_fac;

		// tau1
		a = tau.coeff[1][iz1][ix1];
		b = tau.coeff[1][iz2][ix1];
		c = a + (tau.coeff[1][iz1][ix2] - a) * dist_fac;
		d = b + (tau.coeff[1][iz2][ix2] - b) * dist_fac;
		tau1 = c + (d - c) * depth_fac;

		// tau2
		a = tau.coeff[2][iz1][ix1];
		b = tau.coeff[2][iz2][ix1];
		c = a + (tau.coeff[2][iz1][ix2] - a) * dist_fac;
		d = b + (tau.coeff[2][iz2][ix2] - b) * dist_fac;
		tau2 = c + (d - c) * depth_fac;

		// --------------------------------------------------------------------
		// Compute ellipticity correction via equations (22) and (26)
		// of Dziewonski and Gilbert (1976).
		// --------------------------------------------------------------------
		ellip_corr = etSc0 * tau0 + etSc1 * Math.cos(ev_sta_azim) * tau1 + etSc2 * Math.cos(2.0 * ev_sta_azim) * tau2;

		return Double.isNaN(ellip_corr) ? 0. : ellip_corr;

	} // END table_correction

	private synchronized Tau getTau(SeismicPhase phase) throws IOException {
		if (!tauMap.containsKey(phase))
			read_table(phase);

		return tauMap.get(phase);
	}

	/**
	 * 
	 * ASCII Text File Input Function
	 * 
	 * Given the input \em filename, this function reads in the associated basemodel
	 * ellipticity correction lookup table.
	 * 
	 * @throws IOException
	 * 
	 */
	private void read_table(SeismicPhase phase) throws IOException {
		int ix, iz;
		int ntbd, ntbz;

		InputStream inputStream = new SeismicBaseData(getFile(phase)).getInputStream();
		Scanner scn = new Scanner(inputStream);

		// headers.put(phase, scn.nextLine());
		scn.nextLine();

		// get number of depth samples
		ntbz = scn.nextInt();
		scn.nextLine();

		Tau tau = new Tau();

		tauMap.put(phase, tau);

		tau.depth = new float[ntbz];

		// read depths
		for (iz = 0; iz < ntbz; iz++)
			tau.depth[iz] = scn.nextFloat();
		scn.nextLine();

		// get number of distance samples
		ntbd = scn.nextInt();
		scn.nextLine();

		tau.distance = new float[ntbd];

		// read depths
		for (int i = 0; i < ntbd; i++)
			tau.distance[i] = scn.nextFloat();
		scn.nextLine();

		// System.out.println(Arrays.toString(d));

		// Resize the tau arrays to reflect the number of depths
		// and distances

		tau.coeff = new float[3][ntbz][ntbd];

		// Input the tau values depth by depth

		for (iz = 0; iz < ntbz; iz++) {
			// Skip depth header line.
			scn.nextLine();
			// read each line for depth iz

			for (ix = 0; ix < ntbd; ix++) {
				for (int i = 0; i < 3; ++i)
					tau.coeff[i][iz][ix] = scn.nextFloat();

				// System.out.printf("%10.4f %10.4f %10.4f%n", t[0][iz][ix],
				// t[1][iz][ix], t[2][iz][ix]);
			}
			scn.nextLine();
		}

		// close stream and exit
		inputStream.close();
		scn.close();
	}

//		/**
//		 * Retrieve set of SeismicPhases that actually have files in the right directory.
//		 * @return
//		 */
//		public EnumSet<SeismicPhase> getSupportedPhases()
//		{
//			return supportedPhases;
//		}

	public static void main(String[] args) throws Exception {
		/*
		 * URL url = SeismicBaseData.class.getResource("/el_ak135_Pg");
		 * System.out.println("url  == "+url); URI uri = url.toURI();
		 * System.out.println("uri  == "+uri);
		 * System.out.println("ext  == "+url.toExternalForm());
		 * System.out.println("path == "+url.getPath());
		 * System.out.println("exists? "+new File(url.getPath()).exists());
		 */

		try {
			EllipticityCorrections ec = new EllipticityCorrections();

			System.out.println(ec.getDirectory().getPath());
			System.out.println(ec.getDirectory().getAbsolutePath());
			System.out.println(ec.getDirectory().getCanonicalPath());

//			for (SeismicPhase phase : ec.getSupportedPhases())
//				System.out.println(phase);

			System.out.println();

			System.out.println(
					ec.getEllipCorr(SeismicPhase.PP, new GeoVector(30, 30, 0, true), new GeoVector(-30, -30, 0, true)));

			System.out.println(
					ec.getEllipCorr(SeismicPhase.pP, new GeoVector(30, 30, 0, true), new GeoVector(-30, -30, 0, true)));

			System.out.println("Done.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
