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
package gov.sandia.gmp.bender.level;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;

/**
 * <p>A LevelEllipsoid object represents a Level within a GeoTessModel that
 * computes radii that are all on the same ellipsoidal surface, regardless of 
 * the position of the input GeoTessPosition argument. 
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * <p>Converted for use with GeoTessModel/GeoTessPosition Nov, 2014.</p>
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 2.0
 */
@SuppressWarnings("serial")
public class LevelEllipsoid extends Level
{
	/**
	 * The ellipsoids equatorial radius.
	 */
	private double equatorialRadius;
	
	/**
	 * Standard constructor.
	 * 
	 * @param majorLayerIndex The major layer index of this Level.
	 * @param name The Levels name.
	 */
	public LevelEllipsoid(int majorLayerIndex, String name)
	{
		super(majorLayerIndex, name);
	}

	/**
	 * Constructor specifying a major layer index, an equatorial radius, and the
	 * name of the Radius.
	 * 
	 * @param layer
	 * @param equatorialRadius
	 * @param name
	 */
	public LevelEllipsoid(int majorLayerIndex, String name, double equatorialRadius)
	{
		super(majorLayerIndex, name);
		this.equatorialRadius = equatorialRadius;
	}

	/**
	 * Constructor specifying a major layer index and an equatorial radius. The
	 * name of the layer will set to Rx, where x is the specified equatorial
	 * radius .
	 * 
	 * @param layer
	 *            int
	 * @param equatorialRadius
	 *            double
	 */
	public LevelEllipsoid(int majorLayerIndex, double equatorialRadius)
	{
		this(majorLayerIndex, String.format("R%1.0f", equatorialRadius), equatorialRadius);
	}

	/**
	 * Retrieve the actual radius of this Radius or Depth object. For Radius
	 * objects, the equatorial radius specified in the constructor will be
	 * converted to a different radius using the Earth eccentricity and the
	 * latitude of the supplied Profile object. For Depth objects, the depth
	 * specified in the constructor will be converted to radius using the earth
	 * radius at the latitude of the supplied Profile. In both cases, the radius
	 * is constrained to reside in the layer specified in the constructor. For
	 * Radius/Depth objects constructed with LayerSide objects, the radius of
	 * the major layer boundary at the position of the supplied Profile is
	 * returned.
	 * 
	 * @param profile
	 *            Profile
	 * @return double
	 * @throws GeoTessException
	 */
	@Override
	public double getRadius(GeoTessPosition profile) throws GeoTessException
	{
		return constrainRadius(profile, equatorialRadius * 
				profile.getEarthShape().getSquashFactor(profile.getVector()));
	}

	/**
	 * Override of abstract method that retrieves the content of this level object
	 * and writes it to the supplied StringBuffer.
	 * 
	 * @param buf       The buffer within which the toString() is written.
	 * @param profile   The profile defining where Level radii are extracted.
	 * @param attribute The attributes to be output at the radius location.
	 */
	@Override
	public void toString(StringBuffer buf, GeoTessPosition profile,
			                 GeoAttributes attribute) 
	       throws GeoTessException
	{
		//GeoTessPosition gtp = GeoTessPosition.getGeoTessPosition(profile);
		buf.append(String.format(" %4d, %12s", majorLayerIndex, name));

		double r = getRadius(profile);
		profile.setRadius(majorLayerIndex, r);
		int attrIndx = profile.getModel().getMetaData().getAttributeIndex(attribute.name());
		buf.append(String.format(" z=%10.4f  r=%10.4f  d=%10.4f  v=%10.4f",
				                     equatorialRadius, r, profile.getEarthRadius() - r,
				                     1. / profile.getValue(attrIndx)));
	}

	/**
	 * Returns the content of this Level object.
	 */
	@Override
	public String toString()
	{
		return String.format(
				"Ellipsoid:  majorLayerIndex = %3d, equatorialRadius = %10.4f%n",
				majorLayerIndex, equatorialRadius);
	}
}
