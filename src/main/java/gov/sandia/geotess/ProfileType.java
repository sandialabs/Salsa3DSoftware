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
package gov.sandia.geotess;

/**
 * There are 6 types of profiles:
 * <ol start="0">
 * <li>EMPTY : two radii and no Data
 * <li>THIN: one radius and one Data
 * <li>CONSTANT: two radii and one Data
 * <li>NPOINT: two or more radii and an equal number of Data
 * <li>SURFACE: no radii and one Data
 * <li>SURFACE_EMPTY: no radii and no Data
 * </ol>
 * <p>
 * 
 * @author Sandy Ballard Ballard
 * 
 */
public enum ProfileType
{

	// DO NOT CHANGE THE ORDER OF THE ProfileTypes!
	/**
	 * A profile defined by two radii and no Data
	 */
	EMPTY,

	/**
	 * A profile defined by a single radius and one Data object
	 */
	THIN,

	/**
	 * A profile defined by two radii and one Data object
	 */
	CONSTANT,

	/**
	 * A profile defined by two or more radii and an equal number of Data
	 * objects.
	 */
	NPOINT,

	/**
	 * A profile defined by no radii and one Data object
	 */
	SURFACE,

	/**
	 * A profile defined by no radii and no Data
	 */
	SURFACE_EMPTY

};
