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
package gov.sandia.gmp.bender;

import java.io.Serializable;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;

/**
 * <p>Defines several enumerations for useful quantities.
 * </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: Sandia National Laboratories</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public class BenderConstants
{
	public enum SearchMethod implements Serializable
	{
	  SIMPLEX, BRENTS, AUTO
	};

	public static final EnumSet<GeoAttributes> supportedAttributes = EnumSet.of(
			GeoAttributes.TRAVEL_TIME,
			GeoAttributes.TT_SITE_CORRECTION,
			GeoAttributes.TT_MODEL_UNCERTAINTY,
			GeoAttributes.DTT_DLAT,
			GeoAttributes.DTT_DLON,
			GeoAttributes.DTT_DR,
			GeoAttributes.AZIMUTH,
			GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
			GeoAttributes.DAZ_DLAT,
			GeoAttributes.DAZ_DLON,
			GeoAttributes.DAZ_DR,
			GeoAttributes.SLOWNESS,
			GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY,
			//GeoAttributes.DSH_DLAT,
			//GeoAttributes.DSH_DLON,
			//GeoAttributes.DSH_DR,
			GeoAttributes.BACKAZIMUTH,
			GeoAttributes.TURNING_DEPTH,
			GeoAttributes.OUT_OF_PLANE,
			GeoAttributes.ACTIVE_FRACTION,
			GeoAttributes.AVERAGE_RAY_VELOCITY,
			GeoAttributes.CALCULATION_TIME,
			GeoAttributes.DISTANCE,
			GeoAttributes.DISTANCE_DEGREES);

	/**
	 * A status flag indicating which phase of the calculation has just been
	 * completed.  Useful for Visualization objects who are handed a reference
	 * to a Ray object at various points during the calculation.  They can
	 * check this status flag to decide how to proceed.
	 */
	public enum RayStatus
	{
		/**
		 * Ray has been initialized, but the initial points on the Ray have
		 * not yet been calculated.  Only the source and recevier positions
		 * are known.
		 */
		INITIALIZED,
		/**
		 * The initial points on the ray have been determined.
		 */
		INITIAL_RAY,
		/**
		 * Points on all the interfaces have been modified to ensure that they
		 * satisfy Snell's Law.
		 */
		SNELL,
		/**
		 * Points in the interior of the layers have been bent in order to
		 * obey Fermat's Principle
		 */
		BENT,
		/**
		 * The number of points on the ray was just doubled.
		 */
		DOUBLED,
		/**
		 * An iteration of the inner loop was just completed.
		 */
		INNER_LOOP,
		/**
		 * An iteration of the outer loop was just completed, but then number
		 * of nodes has not yet been doubled.
		 */
		OUTER_LOOP,
		/**
		 * Calculation of a Ray object has been completed.  This ray may or may
		 * not be the fastest ray.  It may be INVALID.
		 */
		FINAL_RAY,

		/**
		 * This ray is not the fastest ray, but it's travel time is within
		 * some small tolerance of the fastest ray.
		 */
		FAST_RAY,

		/**
		 * Of all the rays computed for a given source-receiver pair, this is
		 * the fastest ray.
		 */
		FASTEST_RAY} ;


		/**
		 * Convert a String to a RayStatus enum object.
		 * @param rayType
		 * @return
		 */
		public static RayStatus getRayStatus(String rayStatus)
		{
			return RayStatus.valueOf(rayStatus);
		}

		/**
		 * Used by RaySegment to indicate whether the segment is part of the upgoing
		 * leg, the downgoing leg, or the bottom segment.
		 */
		public enum RayDirection
		{
		  // Used as initial start ray direction changes for branches
			SOURCE, RECEIVER,
			
 		  // Indicates RaySegmentBend segment type and general up or down going ray
			// directions
			UPGOING, DOWNGOING,
			
			// Reflections and their specific types indicating RaySegmentFixedReflection
			// segments and direction change information
			TOP_SIDE_REFLECTION, REFLECTION, BOTTOM_SIDE_REFLECTION,
			
			// Indicates RaySegmentBottom segment type and branch type
			BOTTOM;

			public boolean isReflection()
			{
				return (this == REFLECTION) || (this == TOP_SIDE_REFLECTION) ||
						   (this == BOTTOM_SIDE_REFLECTION);
			}
		}

		/**
		 * Who will compute gradient of velocity for pseudo-raytracer?
		 * @author sballar
		 *
		 */
		public enum GradientCalculationMode {
			/**
			 * Velocity gradient will be computed on-the-fly by Bender.
			 */
			ON_THE_FLY, 
			/**
			 * Velocity gradients will be pre-computed by GeoModel, stored on the
			 * grid, and then interpolated when requested. 
			 */
			PRECOMPUTED 
			}

		/**
		 * TOP and BOTTOM refer to the top and bottom of a major layer in a model
		 * (not top and bottom of an interface).
		 */
		public enum LayerSide
		{
			/**
			 * Top of a major layer in a model.
			 */
			TOP,
			/**
			 * Bottom of a major layer in a model.
			 */
			BOTTOM
		}

		/**
		 * Layers thinner than this get special processing.  Internal use.
		 */
		final static public double MIN_LAYER_THICKNESS = .01; // km

		/**
		 * In the bottom segment of the ray, if the first and last nodes get to
		 * be closer together than this distance (in km), then the ray is considered
		 * to be a reflection.
		 */
		final static public double RELECTION_DISTANCE = .1;

		/**
		 * Rays with travel times that are within this tolerance level of the
		 * travel time of the fastest ray are included in the list or rays
		 * returned by Bender.  In seconds.  Default is zero.
		 */
		static public double FAST_TRAVELTIME_TOLERANCE = 0.;
		
		/**
		 * When computing horizontal derivatives, this is the horizontal 
		 * distance in radians that the source position is moved.
		 */
		final static public double deriv_dx = 0.001;
		
		/**
		 * When computing radial derivatives, this is the radial 
		 * distance in km that the source position is moved.
		 */
		final static public double deriv_dr = 0.1;

}
