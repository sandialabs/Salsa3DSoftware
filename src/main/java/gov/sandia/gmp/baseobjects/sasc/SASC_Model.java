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
package gov.sandia.gmp.baseobjects.sasc;

import static java.lang.Math.*;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import gov.sandia.gmp.util.globals.Globals;

/*
 * Copyright (c) 1997-2004 Science Applications International Corporation.
 *

 * NAME
 *	read_sasc -- Read slowness/azimuth station correction (SASC) files
 *      load_single_sasc -- load single SASC file into memory
 *	read_single_sasc -- Read single SASC file (e.g., StaPro)
 *	correct_az_slow -- Correct various azimuth and slowness info
 *	get_ar_sasc -- Get a single ar_sasc structure given arid
 *	free_active_ar_sasc -- Free current array of ar_sasc structures
 *	make_sasc_adj_before_locator - Make SASC adjustments before location
 *	apply_sasc_adj_in_locator - Apply SASC adjustment in event locator?

 * FILE
 *	az_slow_corr.c

 * SYNOPSIS
 *	int
 *	read_sasc (sasc_pathway)
 *	char	*sasc_pathway;	(i) Directory pathway and filename prefix of
 *				    slowness/azimuth station correction files

 *	int
 *	load_single_sasc (sta_index)
 *	int	sta_index	(i) Station index in SASC_Tables structure

 *	int
 *	read_single_sasc (sasc_pathway, sta)
 *	char	*sasc_pathway;	(i) Directory pathway and filename prefix of
 *				    single SASC file
 *	char	*sta;		(i) Station name 

 *	void
 *	correct_az_slow (arid, sta, azimuth, slow, delaz, delslo, 
 *		 	 tot_az_err, tot_slow_err)

 *	int	arid;		(i)   Unique arrival ID
 *	char	*sta;		(i)   Station name 
 *	double	*azimuth;	(i/o) Raw azimuth (i); Corrected azimuth (o)
 *	double	*slow;		(i/o) Raw slowness (i); Corrected slowness (o)
 *	double	delaz;		(i)   Azimuth measurement error (from DFX)
 *	double	delslo;		(i)   Slowness measurement error (from DFX)
 *	double	*tot_az_err;	(o)   Total azimuth error (deg)
 *	double	*tot_slow_err;	(o)   Total slowness error (sec/deg)

 *	Ar_SASC
 *	get_ar_sasc (arid)
 *	int	arid;		(i) Unique arrival ID

 *	void
 *	free_active_ar_sasc ()

 *	void
 *	make_sasc_adj_before_locator () 

 *	Bool
 *	apply_sasc_adj_in_locator () 

 * DESCRIPTION
 *	-- read_sasc() puts paths to slowness/azimuth station correction
 *	files into SASC_Tables structure. Every path to file in the specified
 *	directory with the defined filename prefix will be put into SASC_Tables 
 *	structure.  For example, assuming sasc_pathway is,
 *	/prj/idc/ops/static/SASC/sasc , which includes SASC tables for 
 *	the stations, ASAR, NORES and GERES, then paths to 3 files would be saved:

 *	    /prj/idc/ops/static/SASC/sasc.ASAR
 *	    /prj/idc/ops/static/SASC/sasc.NORES
 *	    /prj/idc/ops/static/SASC/sasc.GERES

 *	-- load_single_sasc() reads a single slowness/azimuth station 
 *	correction (SASC) table from file into memory using path saved in
 *	SASC_Tables structure by read_sasc(). 

 *	-- read_single_sasc() reads a single slowness/azimuth station 
 *	correction (SASC) table from file into memory.  A single file in 
 *	the specified directory with the defined filename prefix will be 
 *	read.  For example, assuming we wish to only read SASC table for
 *	station, GERES, and sasc_pathway is, /prj/idc/ops/static/SASC/sasc, 
 *	then only the one file, /prj/idc/ops/static/SASC/sasc.GERES, would 
 *	be read.  This is currently only employed in StaPro.

 *	-- correct_az_slow() corrects for the station-specific slowness/
 *	azimuth field specified in the SASC table.  Unlike a source-specific 
 *	station correction (SSSC) which is applied to a theoretical 
 *	calculation, an SASC is applied to an observed slowness/azimuth 
 *	measure.  A correction is made to the raw azimuth and slowness as 
 *	well as their respective modeling errors.  An affine transform is
 *	applied prior to looking for a bin-corrected azimuth and slowness
 *	correction.  In most cases this transform will not apply any
 *	horizontal rotation, but provides a mechanism by which such as can
 *	be employed.  The affine transform contains a rotation element,
 *	defined by coefficients, a11, a12, a21 and a22, and a default sx/sy
 *	correction.  If a bin correction exists, then the coefficients and
 *	default slowness vector is combined to the value specified in the
 *	appropriate bin.

 *	-- get_ar_sasc() gets an azimuth and slowness corrected structure
 *	for a given arid from the array of actively stored Ar_SASC structures
 *	currently stored in memory.  At present this function is only
 * 	declared locally (privately) to libloc.  If external access is
 *	desired, make include file, ar_sasc.h, public in Makefile.

 *	-- free_active_ar_sasc() frees all current SASC information that was
 *	originally allocated in calls to correct_az_slow().  In general,
 *	external access to correct_az_slow() is limited a single instanti-
 *	ation when arrival records are read.  If the arrival records have 
 *	not yet been corrected, function, locate_event(), will make sure
 *	the SASCs are applied.  In this case, the static Ar_SASC structure,
 *	active_ar_sasc (local to this file), must be freed upon completion
 *	of event location (see bottom of locate_event.c).  This function is
 *	also recommended if new arrival records are introduced by a calling
 *	application/function.

 *	-- make_sasc_adj_before_locator() sets the internal static variable,
 *	make_sasc_adj_in_locator to FALSE if called; else it is initialized 
 *	to TRUE.  This is called when SASC adjustments are needed at the 
 *	start of a calling application so that they are not re-applied in 
 *	the event location process itself.  On the other hand, some 
 *	applications (e.g., ARS) use raw azimuth and slowness measures
 *	throughout their processing, but require SASC's be applied in the
 *	event location process alone.

 *	-- apply_sasc_adj_in_locator() informs the locator whether or not
 *	the SASC adjustments have already been applied.  If so, they should
 *	not be doublely applied.

 * DIAGNOSTICS
 *	-- read_sasc() will return an error code of ERR (-1) if a fatal
 *	error is encountered; else it will return OK (0).

 *	-- read_single_sasc() will return an error code of ERR (-1) if a
 *	fatal error is encountered; (1) is no SASC directory prefix is
 *	specified; (2) if no SASC info exists for input station; else it 
 *	will return OK (0).

 * FILES
 *	Function, read_sasc(), reads all station-specific SASC files.
 *	Function, read_single_sasc(), reads only a single SASC file.

 * NOTES
 *	None.

 * SEE ALSO
 *	None. 

 * AUTHOR
 *	Walter Nagy,  2/21/97,	Created.
 *	Walter Nagy,  3/10/97,	Added function, read_single_sasc().
 *	Walter Nagy,  8/26/99,	Added functionality employing full azimuth/
 *				slowness adjustments by employing a new affine
 *				transformation.
 *	Walter Nagy, 11/14/03,	Extended station/phase-dependent azimuth/slowness 
 *				capabilities, including SASCSs, in support of
 *				IDC CR P1 (AWST).  New functions, get_ar_sasc()
 *				and free_active_ar_sasc(), facilitated this
 *				upgrade.
 *  Sandy Ballard, 9/22/2025, translated from c to java.
 */
public class SASC_Model {

	private static final double RAD_TO_DEG = 180./PI;
	private static final double DEG_TO_RAD = PI/180.;
	private static final int SLOW = 0;
	private static final int AZ = 1;

	/**
	 * Default x-vector slowness (sx) corr
	 */
	private double	def_sx_corr;	  

	/**
	 * Default y-vector slowness (sy) corr
	 */
	private double	def_sy_corr;	  

	/**
	 * Default slowness modeling error
	 */
	double	def_slow_mdl_err; 

	/**
	 * A11 x-vector affine slowness coefficient
	 */
	private double	a11; 		  

	/**
	 * A12 y-vector affine slowness coefficient
	 */
	private double	a12; 		  

	/**
	 * A21 x-vector affine slowness coefficient
	 */
	private double	a21; 		  

	/**
	 * A22 y-vector affine slowness coefficient
	 */
	private double	a22; 	

	/**
	 * Number of slowness/azimuth bin pairs
	 */
	private int	num_bins;	 

	/**
	 * Lower slowness/azimuth bound on bin
	 */
	private double[][] bin_lb;	

	/**
	 * Upper slowness/azimuth bound on bin
	 */
	private double[][] bin_ub;	 

	/**
	 * Binned slowness/azimuth correction
	 */
	private double[][] corr;		

	/**
	 *  Binned slowness/azimuth modeling error
	 */
	private double[][] mdl_err;	

	/**
	 * True if either affine or default slowness vector corrections need to be applied.
	 */
	private boolean flag;

	public SASC_Model(File fileName) throws Exception {
		try (Scanner input = new Scanner(fileName);) {

			String line = input.nextLine().trim();
			while (line.startsWith("#") || line.length() == 0)
				line = input.nextLine();
			String[] tokens = line.trim().split("\\s+");
			int i=0;
			def_sx_corr = Double.parseDouble(tokens[i++]);
			def_sy_corr = Double.parseDouble(tokens[i++]);
			def_slow_mdl_err = Double.parseDouble(tokens[i++]);
			a11 = a22 = 1.;
			a12 = a21 = 0.;
			if (tokens.length > 3) {
				a11  = Double.parseDouble(tokens[i++]);
				a12  = Double.parseDouble(tokens[i++]);
				a21  = Double.parseDouble(tokens[i++]);
				a22  = Double.parseDouble(tokens[i++]);
			}
			if (input.hasNext()) {
				input.nextLine(); // skip comment
				num_bins = input.nextInt();
				bin_lb = new double[num_bins][2];
				bin_ub = new double[num_bins][2];
				corr = new double[num_bins][2];
				mdl_err = new double[num_bins][2];
				input.nextLine(); // skip end-of-line
				input.nextLine(); // skip comment
				for (i=0; i<num_bins; ++i) {
					bin_lb[i][SLOW] = input.nextDouble();  // bottom of slowness interval (sec/deg)
					bin_ub[i][SLOW] = input.nextDouble();  // top of slowness interval (sec/deg)
					bin_lb[i][AZ] = input.nextDouble();  // bottom of azimuth interval (deg)
					bin_ub[i][AZ] = input.nextDouble();  // top of azimuth interval (deg)
					corr[i][SLOW] = input.nextDouble();    // slowness correction (sec/deg)
					corr[i][AZ] = input.nextDouble();    // azimuth correction (deg)
					mdl_err[i][SLOW] = input.nextDouble(); // slowness modeling error (sec/deg)
					mdl_err[i][AZ] = input.nextDouble(); // azimuth modeling error (deg)
				}
			}
			input.close();
			
			flag = def_sx_corr != 0. || def_sy_corr != 0. || a11 != 1. || a12 != 0. || a21 != 0. || a22 != 1.;
		} 
	}

	/**
	 * 
	 * @param azimuth observed azimuth
	 * @param delaz azimuth observation uncertainty (delaz)
	 * @param slow observed slowness
	 * @param delslo slowness observation uncertainty (delslo)
	 * @param inDegrees if true, then all azimuth values are in degrees and slowness values are in sec/degree;
	 * if false values are in radians and sec/radian
	 * @return azimuth_corr, azimuth_mdl_err, slow_corr, slow_mdl_err; if anything goes wrong all return values are
	 * Globals.NA_VALUE
	 */
	public double[] correct_az_slow (double azimuth, double delaz, double slow, double delslo, boolean inDegrees) 
	{
		/*
		 * If no valid azimuth or slow, simply return
		 */

		if (azimuth < 0.0 || slow < 0.0 ||delaz < 0.0 || delslo < 0.0)
			return new double[] {Globals.NA_VALUE, Globals.NA_VALUE, Globals.NA_VALUE, Globals.NA_VALUE};

		if (!inDegrees) {
			azimuth = toDegrees(azimuth); // convert radians to degrees
			delaz = toDegrees(delaz); // convert radians to degrees
			slow = toRadians(slow); // convert sec/radian to sec/degree
			delslo = toRadians(delslo); // convert sec/radian to sec/degree
		}

		double raw_slow = slow;
		double raw_azimuth = azimuth;

		double azimuth_corr = 0.;
		double slow_corr = 0.;

		double slow_mdl_err = def_slow_mdl_err;

		/*
		 * Determine default modeling error for azimuth as a function of
		 * the default slowness modeling error.
		 */
		double azimuth_mdl_err;
		double slowness_ratio = slow_mdl_err / (2.0 * slow);
		if (slowness_ratio < 1.0)
			azimuth_mdl_err =  2.0 * asin (slowness_ratio) * RAD_TO_DEG;
		else
			azimuth_mdl_err = 180.0;

		/*
		 * Loop over all bins for this station to determine if there is a 
		 * bin corrected slowness/azimuth correction that needs to be applied.
		 * Also obtain bin-dependent modeling error.
		 */

		for (int i = 0; i < num_bins; i++)
		{
			if (slow < bin_ub[i][SLOW] &&
					slow >= bin_lb[i][SLOW] &&
					azimuth < bin_ub[i][AZ] &&
					azimuth >= bin_lb[i][AZ])
			{
				azimuth_mdl_err = mdl_err[i][AZ];
				slow_mdl_err = mdl_err[i][SLOW];
				azimuth_corr = corr[i][AZ];
				slow_corr = corr[i][SLOW];

				/* 
				 * Update azimuth and slowness with corrected values. 
				 */

				azimuth = (azimuth-azimuth_corr + 720.) % 360.;
				slow -= slow_corr;

				break;
			}
		}

		/*
		 * Apply affine and default slowness vector corrections here.  First 
		 * decompose the original azimuth and slowness into vector slowness 
		 * componenets (sx, sy), and then, apply affine transform.  Then 
		 * apply default slowness vector corrections in x- and y-directions.
		 * Finally, adjust input azimuth and slow based on these updated
		 * slowness vector component adjustments.

		 * Affine transform: corrected_sx = (a11*sx + a12*sy) - def_sx_corr
		 *                   corrected_sy = (a21*sx + a22*sy) - def_sy_corr
		 * where,
		 *	sx and sy have already been bin corrected.
		 */

		if (flag && slow > 0.0)
		{
			double azr = azimuth*DEG_TO_RAD;
			double sx = slow * sin (azr);
			double sy = slow * cos (azr);
			double adj_sx = a11*sx + a12*sy;
			double adj_sy = a21*sx + a22*sy;
			sx = adj_sx;
			sy = adj_sy;

			/*
			 * Apply default sx and sy corrections here to get the total affine
			 * transformed slowness vector positions.
			 */

			sx -= def_sx_corr; /* Apply default sx corr. here */
			sy -= def_sy_corr; /* Apply default sy corr. here */

			/*
			 * Revert back to azimuth/slowness space
			 */

			azimuth = (atan2 (sx, sy) * RAD_TO_DEG +360.) % 360.;

			slow = sqrt(sx*sx+sy*sy);
			
			azimuth_corr = (raw_azimuth - azimuth + 3600.) % 360.;
			if (azimuth_corr > 180.0)
				azimuth_corr -= 360.;

			slow_corr = raw_slow - slow;

		}

		if (inDegrees)
			return new double[] {azimuth_corr, azimuth_mdl_err, slow_corr, slow_mdl_err};

		return new double[] {toRadians(azimuth_corr), toRadians(azimuth_mdl_err), 
				toDegrees(slow_corr), toDegrees(slow_mdl_err)};
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(bin_lb);
		result = prime * result + Arrays.deepHashCode(bin_ub);
		result = prime * result + Arrays.deepHashCode(corr);
		result = prime * result + Arrays.deepHashCode(mdl_err);
		result = prime * result
				+ Objects.hash(a11, a12, a21, a22, def_slow_mdl_err, def_sx_corr, def_sy_corr, flag, num_bins);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SASC_Model other = (SASC_Model) obj;
		return  num_bins == other.num_bins 
				&& Double.doubleToLongBits(def_slow_mdl_err) == Double.doubleToLongBits(other.def_slow_mdl_err)
				&& Double.doubleToLongBits(def_sx_corr) == Double.doubleToLongBits(other.def_sx_corr)
				&& Double.doubleToLongBits(def_sy_corr) == Double.doubleToLongBits(other.def_sy_corr)
				&& Double.doubleToLongBits(a11) == Double.doubleToLongBits(other.a11)
				&& Double.doubleToLongBits(a12) == Double.doubleToLongBits(other.a12)
				&& Double.doubleToLongBits(a21) == Double.doubleToLongBits(other.a21)
				&& Double.doubleToLongBits(a22) == Double.doubleToLongBits(other.a22)
				&& Arrays.deepEquals(bin_lb, other.bin_lb) 
				&& Arrays.deepEquals(bin_ub, other.bin_ub)
				&& Arrays.deepEquals(corr, other.corr)
				&& Arrays.deepEquals(mdl_err, other.mdl_err);
	}

}
