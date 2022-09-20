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

import java.io.Serializable;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.bender.BenderConstants.LayerSide;

/**
 * <p>A Level object represents a radius level within a GeoTessModel (not to be
 * confused with tessellation levels which are different). A Level is something
 * that one would use to generate a map of some quantity represented in a 
 * model.  The primary purpose of a Level object is to implement the
 * getRadius(GeoTessPosition) method.
 * <p>Level is abstract. Currently implemented derived classes include:
 * <ul>
 * <li>LevelDepth -      Returns radii that are all at a constant depth.
 * <li>LevelEllipsoid -  Returns radii that are all on the same ellipsoid.
 * <li>LevelFraction -   Returns radii that are all at some constant fractional 
 *                       depth within a layer.
 * <li>LevelMajorLayer - Returns radii that are all on the same major layer
 *                       interface. 
 * </ul>
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
public abstract class Level implements Serializable
{
	/**
	 * Used as the previous layer assignment in RayBranchBottom when the
	 * active layer level is assigned to the inner core boundary (ICB).
	 */
	public static final Level	earthCenter		= new LevelMajorLayer(-1, "Center", LayerSide.TOP);

	/**
	 * The major layer in which the level is constrained to reside.
	 */
	final protected int majorLayerIndex;

	/**
	 * The name of the layer. For Level/Depth objects constructed on a major
	 * layer boundary, this will equal the name of the interface as stored in
	 * GeoTessModel.GeoMetaData object. For other layers, it will be some other
	 * name.
	 */
	protected String name;

	/**
	 * In the most recent call to getRadius(), if the radius value returned was
	 * exactly on a major layer boundary, then layerBoundary will equal the
	 * index of that major layer boundary. For LevelMajorLayer objects, 
	 * layerBoundary will have a constant
	 * value equal to the major layer index specified at construction. For
	 * all other Level objects, layerBoundary will equal -1 if the radius computed 
	 * by getRadius() was internal to the major layer (not on the top or bottom of the layer).
	 * If the radius computed in getRadius() was constrained to reside on one of
	 * the major layer boundaries at the top or bottom of the layer, then
	 * layerBoundary will be equal to the major layer index on which the
	 * computed radius resides.
	 */
	protected int layerBoundary;

	/**
	 * Classes that use a Level object can get/set this index as they see fit.
	 * Never modified by any methods in Level or derived classes.
	 */
	protected int index = -1;

	/**
	 * An abstract method supplied by derived concrete classes that retrieves the
	 * radius of this Level object, as determined at the position of the specified
	 * Profile.

	 * @param profile The input profile used to retrieve the radius at this Level.
	 * @return The radius at this Level.
	 * @throws GeoTessException
	 */
	abstract public double getRadius(GeoTessPosition profile) throws GeoTessException;

	/**
	 * Abstract toString method defined by each derived concrete type.
	 * 
	 * @param buf       The buffer within which the toString() is written.
	 * @param profile   The profile defining where Level radii are extracted.
	 * @param attribute The attributes to be output at the radius location.
	 * @throws GeoTessException
	 */
	abstract public void toString(StringBuffer buf, GeoTessPosition profile,
			GeoAttributes attribute) throws GeoTessException;
	
	/**
	 * Standard constructor.
	 * 
	 * @param majorLayerIndex The major layer within which this Level resides.
	 * @param name The name of the Level.
	 */
	public Level(int majorLayerIndex, String name)
	{
		this.majorLayerIndex = majorLayerIndex;
		this.name = name;
		this.layerBoundary = -1;
	}

	/**
	 * Retrieve the index of the major layer within which the radius is
	 * constrained.
	 * 
	 * @return The major layer index of this Level.
	 */
	public int getMajorLayerIndex()
	{
		return majorLayerIndex;
	}

	/**
	 * Returns true if the most recent call to getRadius() returned a radius
	 * that was on a major layer boundary. Always returns true if this Level or
	 * Depth object was constructed with a LayerSide object. For Level/Depth
	 * objects constructed with specific equatorialRadius/depth values, returns
	 * true if on the last call to getRadius() the radius value returned was
	 * constrained to lie exactly on a major layer boundary.
	 * 
	 * @return True if the Level is a major layer boundary.
	 */
	public boolean isOnLayerBoundary()
	{
		return layerBoundary >= 0;
	}

	/**
	 * If this Level is located on a major layer boundary, this method returns
	 * the index of the major layer it is positioned on, which will equal either
	 * layer or layer-1. If this Level is not on a major layer boundary, then
	 * -1 is returned.
	 * 
	 * @return -1 if isOnLayerBoundary is false. Else the major layer boundary is
	 *         returned.
	 */
	public int getLayerBoundary()
	{
		return layerBoundary;
	}

	/**
	 * Retrieve the name of this Level object.
	 * 
	 * @return The name of this Level object.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Set the name of this Level object.
	 * 
	 * @param name The new name of this Level object.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Retrieve the depth of this Level object, as determined at the position
	 * of the specified Profile (Profile is an interface implemented by both
	 * LayereData and InterpolatedNodeLayered).
	 * @param profile
	 * @return
	 * @throws GeoTessException
	 */
	public double getDepth(GeoTessPosition profile) throws GeoTessException
	{
		return profile.getEarthRadius() - getRadius(profile);
	}

	/**
	 * If this Level object is an instance of LevelMajorLayer, then return 
	 * the LayerSide (TOP or BOTTOM).  Otherwise, return null.
	 * @return
	 */
	public LayerSide getLayerSide()
	{
		return null;
	}

	/**
	 * Returns true if this Level object is an instance of LevelMajorLayer,
	 * false otherwise.
	 * @return
	 */
	public boolean isMajorInterface()
	{
		return getLayerSide() != null;
	}

	/**
	 * Returns this Levels index.
	 * 
	 * @return The Levels index.
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * Sets this Levels index.
	 * 
	 * @param index The new Level index.
	 */
	public void setIndex(int index)
	{
		this.index = index;
	}

	/**
	 * Returns a vector containing the outward pointing normal vector of this
	 * Levels top position.
	 *  
	 * @param profile The profile within which the normal vector is determined.
	 * @return
	 * @throws GeoTessException
	 */
	public double[] getNormal(GeoTessPosition profile) throws GeoTessException
	{
		// call getRadius() to ensure that layerBoundary is set correctly
		getRadius(profile);
		if (layerBoundary >= 0)
		{
			//profile.setRadius(layerBoundary, profile.getRadiusTop(layerBoundary));
			return profile.getLayerNormal(layerBoundary);
		}
		
		// if the current radius is not exactly on a major layer boundary, then
		// assume that outward pointing normal is equal to the unit vector that
		// points from center of earth to position of profile. In an ellipsoid,
		// this is not exactly true, but the error is very small. Also, the
		// normal is only going to be used for reflections which are not valid
		// for rays that reflect off of interface which are not major layer
		// boundaries. Not worth doing extra work here.
		return profile.getVector().clone();
	}

	/**
	 * Retrieve a radius value that will be equal to the supplied 
	 * radius if the supplied radius is within the boundaries of the 
	 * major layer specified at construction.  If the supplied radius
	 * is outside the range of the major layer index specified at 
	 * construction, then the radius of the top or bottom of the layer
	 * is returned.
	 * @param profile
	 * @param r
	 * @return
	 * @throws GeoTessException
	 */
	protected double constrainRadius(GeoTessPosition profile, double r)
			      throws GeoTessException 
	{
		double rt = profile.getRadiusTop(majorLayerIndex - 1);
		if (r <= rt)
		{
			layerBoundary = majorLayerIndex - 1;
			return rt;
		}
		
		rt = profile.getRadiusTop(majorLayerIndex);
		if (r >= rt)
		{
			layerBoundary = majorLayerIndex;
			if (majorLayerIndex == profile.getNLayers() - 1)
				return r;

			return rt;
		}

		layerBoundary = -1;
		return r;		
	}
}
