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
import gov.sandia.gmp.bender.BenderConstants.LayerSide;

/**
 * <p>A LevelMajorLayer object represents a Level within a GeoTessModel that
 * computes radii that are all within the same major layer index, regardless of 
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
public class LevelMajorLayer extends Level
{
	/**
	 * The top or bottom of the major layer that this Level represents.
	 */
	private LayerSide layerSide;

	/**
	 * Standard constructor.
	 * 
	 * @param majorLayerIndex Major layer index of this level.
	 * @param name            Level name.
	 * @param topOrBottom     Top of layer or bottom of layer level.
	 */
	public LevelMajorLayer(int majorLayerIndex, String name, LayerSide topOrBottom)
	{
		super(majorLayerIndex, name);
		this.layerSide = topOrBottom;
		if (layerSide == LayerSide.TOP)
			layerBoundary = majorLayerIndex;
		else
			layerBoundary = majorLayerIndex-1;
	}

	/**
	 * Constructor specifying major layer index and layerSide. 
	 *
	 * @param majorLayerIndex Major layer index of this level.
	 * @param layerSide       Top of layer or bottom of layer level.
	 */
	public LevelMajorLayer(int majorLayerIndex, LayerSide topOrBottom)
	{
		this(majorLayerIndex, String.format("L%d_%s", majorLayerIndex, topOrBottom.toString()), 
				topOrBottom);
	}

	/**
	 * Override of an abstract method that retrieves the radius of this Level
	 * object, as determined at the position of the specified input profile.

	 * @param profile The input profile used to retrieve the radius at this Level.
	 * @return The radius at this Level.
	 * @throws GeoTessException
	 */
	@Override
	public double getRadius(GeoTessPosition profile) throws GeoTessException
	{
		if (layerBoundary == -1)
			return 0.0;  // Earth Center layer.
		else
		  return profile.getRadiusTop(layerBoundary);
	}

	/**
	 * Returns the BenderConstant LayerSide (TOP or BOTTOM) of this level.
	 */
	@Override
	public LayerSide getLayerSide()
	{
		return layerSide;
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
		//gtp.set(profile.getVector(), profile.getRadius());
		buf.append(String.format(" %4d, %12s", majorLayerIndex, name));
		int attrIndx = profile.getModel().getMetaData().getAttributeIndex(attribute.name());

		if (layerSide == LayerSide.TOP)
		{
			double r = profile.getRadiusTop(majorLayerIndex);
			profile.setRadius(majorLayerIndex, r);
			buf.append(String.format(" %12s  r=%10.4f  d=%10.4f  v=%10.4f",
					layerSide, r, profile.getEarthRadius() - r,
					1./profile.getValue(attrIndx)));
		}
		else if (layerSide == LayerSide.BOTTOM)
		{
			double r = profile.getRadiusTop(majorLayerIndex-1);
			profile.setRadius(majorLayerIndex-1, r);
			buf.append(String.format(" %12s  r=%10.4f  d=%10.4f  v=%10.4f",
					layerSide, r, profile.getEarthRadius() - r,
					1./profile.getValue(attrIndx)));
		}
	}

	/**
	 * Returns the content of this Level object.
	 */
	@Override
	public String toString()
	{
		return String.format(
				"MajorLayer:  majorLayerIndex = %3d, LayerSide = %6s%n",
				majorLayerIndex, layerSide);
	}
}
