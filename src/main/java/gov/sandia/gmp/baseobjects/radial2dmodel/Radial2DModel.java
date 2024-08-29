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
package gov.sandia.gmp.baseobjects.radial2dmodel;

import java.io.File;
import java.util.EnumMap;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;

/**
 * There are two implementations of Radial2DModel interface: Radial2DModelLegacy and Radial2DModelImproved. 
 * Legacy implements code that produces the same results as was produced by old C code written by SAIC and 
 * was in use at the IDC as of August, 2024. It suffers from several shortcomings which are rectified 
 * in Radial2DModelImproved, which was written by S. Ballard in August 2024.
 */
public interface Radial2DModel {

	/**
	 * Retrieve a map of attribute values.  The map will always contain
	 * distance, distance_degrees, azimuth and azimuth_degrees.
	 * If not blocked, map will also contain travel_time, tt_model_uncertainty,
	 * slowness and slowness_degrees.  
	 * @param lat geographic latitude in degrees
	 * @param lon longitude in degrees
	 * @return map of attribute values.  If map does not include travel_time then 
	 * ray path was blocked.
	 * @throws Exception
	 */
	EnumMap<GeoAttributes, Double> interpolate(double lat, double lon) throws Exception;

	/**
	 * Retrieve a map of attribute values.  The map will always contain
	 * distance, distance_degrees, azimuth and azimuth_degrees.
	 * If not blocked, values will also contain travel_time, tt_model_uncertainty,
	 * slowness and slowness_degrees.  
	 * @param v unit vector of location where interpolation should be calculated.
	 * @return map of attribute values.  If map does not include travel_time then 
	 * ray path was blocked.
	 * @throws Exception 
	 */
	EnumMap<GeoAttributes, Double> interpolate(double[] v) throws Exception;

	/**
	 * Return the maximum distance in degrees from the center of the model moving in the direction 
	 * given by azimuth in degrees.  For hydroacoustics, this is blockage.
	 * @param az in degrees
	 * @return max distance in degrees
	 */
	double getMaxDistance(double az);

	/**
	 * Retrieve the canonical File from which this model was loaded
	 */
	File inputFile();

	/**
	 * Station name. Same as the canonical file name.
	 * @return
	 */
	String name();

	/**
	 * The season.  Returns the name of the directory from which this model was read.
	 * Will be the name of a month, or of a season.
	 * @return
	 */
	String season();

	/**
	 * Value of <i>period</i> read from the input file.  This is 
	 * supposed to be the <i>season</i>, but is not (at least in some models).
	 * @return
	 */
	String period();

	/**
	 * Latitude of the center of the model in degrees.  
	 * Computed using the ellipsoid currently in use by the user
	 * @return
	 */
	double lat();

	/**
	 * Longitude of the center of the model in degrees.
	 * @return
	 */
	double lon();

	/**
	 * Earth-centered unit vector of the center of the model.
	 * @return
	 */
	double[] center();

	/**
	 * The travel time at each grid point, in seconds.
	 * Dimensions are nAzimuths x nRadii. 
	 * @return
	 */
	float[][] tt();

	/**
	 * The travel time uncertainty at each grid point, in seconds.
	 * Dimensions are nAzimuths x nRadii. 
	 * @return
	 */
	float[][] uncertainty();

	/**
	 * returns the value of travel time uncertainty that should be added to the
	 * normal values of tt_uncertainty stored in this model.  This constant value
	 * is retrieved from file <code>time_guide</code> in the directory from which
	 * this model was loaded.  It should only be used for hydroacoustic T phases.
	 * @return
	 */
	double htConvert();

	/**
	 * set the value of travel time uncertainty that should be added to the
	 * normal values of tt_uncertainty stored in this model.  This constant value
	 * is retrieved from file <code>time_guide</code> in the directory from which
	 * this model was loaded.  It should only be used for hydroacoustic T phases.
	 * @param htConvert
	 */
	void htConvert(double htConvert);

	void vtk(File outputFile) throws Exception;

	String toString();

	/**
	 * Retrieve a reference to a Receiver object constructed using only information 
	 * retrieved from this Radial2D object;
	 * @return
	 * @throws Exception
	 */
	Receiver getReceiver() throws Exception;

	/**
	 * Retrieve the spacing of grid nodes in the radial direction.
	 * Assumed constant at all azimuths.
	 * @return
	 */
	double delta();

	/**
	 * Retrieve the spacing of grid nodes in the azimuthal direction.
	 * Assumed constant.
	 * @return
	 */
	double dazimuth();

	/**
	 * hashCode is based on the hashCode of the canonical input File.
	 * @return
	 */
	int hashCode();

	/**
	 * Equality is based on the equality of the canonical input File.
	 * @param obj
	 * @return
	 */
	boolean equals(Object obj);

}
