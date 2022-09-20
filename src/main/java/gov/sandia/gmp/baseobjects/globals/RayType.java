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

/**
 * RayType refers to the type of ray that was calculated by a Predictor. <br>
 * ERROR, INVALID, REFLECTION, DIFFRACTION, REFRACTION.
 */
public enum RayType {
	/**
	 * Something really bad happened that should be investigated. Examples include:
	 * <p>
	 * <ul>
	 * <li>failure to converge
	 * <li>maximum number of iterations exceeded,
	 * <li>null pointer exceptions, array bounds violated, etc.
	 * </ul>
	 * <p<This implies a bug that should be reported.
	 */
	ERROR,

	/**
	 * the calculated ray is invalid for one of the following reasons:
	 * <ul>
	 * <li>the source or receiver was below specified maximum depth of ray
	 * <li>the ray reflected off of, or diffracted along, an interface that did not
	 * have a velocity discontinuity
	 * <li>calculation aborted for a ray that was diffracting a very long distance
	 * along an interface
	 * </ul>
	 */
	INVALID,

	/**
	 * The calculated ray is valid, but is of unknown type.
	 */
	VALID,

	/**
	 * The ray reflected off of a major layer interface in the model.
	 */
	REFLECTION,

	/**
	 * The ray diffracted along the top of one or more major layer interfaces in the
	 * model.
	 */
	TOP_SIDE_DIFFRACTION,

	/**
	 * The ray diffracted along the bottom of a major layer interface in the model
	 * (at least partially).
	 */
	BOTTOM_SIDE_DIFFRACTION,

	/**
	 * The ray is a refracted ray that turned at some radius in the model.
	 */
	REFRACTION,

	/**
	 * Ray has one or more top- or under-side reflections.
	 */
	FIXED_REFLECTION,

	/**
	 * The type and status of the ray is not known.
	 */
	UNKNOWN;
}
