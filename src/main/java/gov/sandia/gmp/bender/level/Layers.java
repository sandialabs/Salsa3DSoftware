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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.EarthInterface;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.bender.BenderConstants.LayerSide;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;

/**
 * <p>
 * Title:
 * </p>
 *
 * <p>
 * Description:
* <p>Converted for use with GeoTessModel/GeoTessPosition Nov, 2014.</p>
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 *
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Layers implements Serializable
{

  /**
   * The Level objects that define the levels of the interfaces.  These
   * are the interfaces at the tops of layers.  It is assumed that the
   * bottom of layer 0 is the center of the earth.  In other words, all
   * major layer boundaries will be included in this list, along with a
   * few more interfaces of interest.
   */
  protected ArrayList<Level> interfaces = new ArrayList<Level> ();

  /**
   * Map from major layer index to interfaces index.  N elements =
   * number of major layers in the model.
   */
  protected Level[] majorLayerIndexMap = null;
  
  /**
   * Flag set to true when the majorLayerIndexMap needs updating.
   */
  protected boolean mapUpToDate;

  /**
   * Used during construction of a Layers object to keep all the Level objects
   * sorted by radius.  The structure maps radius -> majorLayerIndex -> Level object.
   * The reason there are two maps is because some major layers have zero
   * thickness so that 2 major layers will have the same radius.
   */
  protected TreeMap<Double,TreeMap<Integer, Level>> radii =
            new TreeMap<Double,TreeMap<Integer, Level>>();

  /**
   * Map to facilitate finding an interface by name.
   */
  protected HashMap<String, Level> layerNames = new HashMap<String,Level>();

  /**
   * Default constructor.
   */
  public Layers()
  {
	  super();
  }

  /**
   * Creates a new identical copy of this Layers object.
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object clone()
  {
    Layers newLayers = new Layers();
    newLayers.interfaces = (ArrayList<Level>)this.interfaces.clone();
    newLayers.layerNames = (HashMap<String, Level>)this.layerNames.clone();
    newLayers.radii = (TreeMap<Double,TreeMap<Integer, Level>>)this.radii.clone();
    newLayers.majorLayerIndexMap = (Level[])this.majorLayerIndexMap.clone(); 
    newLayers.mapUpToDate = this.mapUpToDate;
    return newLayers;
  }

  /**
   * Adds a new Level object to this layers description.
   * 
   * @param radiusObject The new Level object to be added.
   * @param profile      The radius of the new Level object is defined along
   *                     the profile described by this GeoTessPosition.
   * @throws GeoTessException
   */
  public void addRadius(Level radiusObject, GeoTessPosition profile)
          throws GeoTessException
  {
    double r = radiusObject.getRadius(profile);
    layerNames.put(radiusObject.getName(), radiusObject);

    // radii maps radius -> majorLayerIndex -> Level object.
    TreeMap<Integer, Level> entry = radii.get(r);
    if (entry == null)
    {
      entry = new TreeMap<Integer, Level> ();
      radii.put(r, entry);
    }
    entry.put(radiusObject.getMajorLayerIndex(), radiusObject);

    // add all the Level objects to interfaces arraylist, sorted by
    // radius as determined at the Profile where the Layers were constructed.
    interfaces.clear();
    for (TreeMap<Integer, Level> m : radii.values())
      interfaces.addAll(m.values());

    for (int i = 0; i < interfaces.size(); ++i)
      interfaces.get(i).setIndex(i);

    mapUpToDate = false;
    if (majorLayerIndexMap == null)
      majorLayerIndexMap = new Level[profile.getNLayers()];
  }

  /**
   * Adds a major layer index level to the layer definition
   * 
   * @param profile The model profile from which the radius is taken
   * @param majorLayerIndex The major layer index.
   * @param topBottom The top or bottom of the layer.
   * 
   * @throws GeoTessException
   */
  public void addRadius(GeoTessPosition profile, int majorLayerIndex, EarthInterface earthInterface,
  		                  LayerSide topBottom)
         throws GeoTessException 
  {
    addRadius(new LevelMajorLayer(majorLayerIndex, earthInterface.name(), topBottom), profile);
  }

  /**
   * Add an interface to the array of interfaces supported by this Layers
   * object. The major layers as determined at Profile profile are scanned and
   * the one containing radius is identified. Also, the equatorial radius that
   * corresponds to radius at the latitude of profile is determined and saved.
   *
   * @param profile Profile
   * @param radius double
   * @throws GeoTessException
   */
  public void addRadius(GeoTessPosition profile, double radius)
         throws GeoTessException
  {
    radius -= 1e-6;
    addRadius(new LevelEllipsoid(profile.getInterfaceIndex(radius),
          radius / profile.getEarthShape().getSquashFactor(profile.getVector())),
              profile);
  }

  /**
   * Add an interface to the array of interfaces supported by this Layers
   * object. The major layers as determined at Profile profile are scanned and
   * the one containing depth is identified. depth is saved for use later.
   *
   * @param profile Profile
   * @param depth double
   * @throws GeoTessException
   */
  public void addDepth(GeoTessPosition profile, double depth)
         throws GeoTessException
  {
    depth += 1e-6;
    addRadius(new LevelDepth(profile.getInterfaceIndex(
              profile.getEarthRadius() - depth), depth), profile);
  }

  /**
   * Add an interface to the array of interfaces supported by this Layers
   * object. The major layers as determined at Profile profile are scanned and
   * the one containing depth is identified. depth is saved for use later.
   *
   * @param profile Profile
   * @param depth double
   * @throws GeoTessException
   */

  /**
   * Retrieve the number of interfaces managed by this Layers object.  This
   * is generally larger than the number of major layers in the model because
   * it includes extra Level objects distributed within major layers.
   *
   * @return int
   */
  public int getNInterfaces()
  {
    return interfaces.size();
  }

  /**
   * Retrieve a reference to the Level object that has the specified index.
   * @param i int
   * @return Level
   */
  public Level getInterface(int i)
  {
    if (i < 0)
        return null;
    return interfaces.get(i);
  }

  /**
   * Retrieve a reference to the Level object that has the specified name.
   * @param interfaceName String
   * @return Level
   */
  public Level getInterface(String interfaceName)
  {
    return layerNames.get(interfaceName);
  }

  /**
   * Retrieve index of the Level object that has the specified name.  If
   * more than one name is specified, then the first one that is found
   * in currently defind interfaces is returned.
   * @param interfaceName String
   * @return int
   */
  public int getInterfaceIndex(String... interfaceName)
  {
    for (String name : interfaceName)
    {
      Level r = layerNames.get(name);
      if (r != null)
        return r.getIndex();
    }
    return -1;
  }

  /**
   * Find index i such that radius resides in the i'th layer, i.e., r >
   * interface i and r <= interface i+1.Returns -1 if radius <= bottom of
   * layer[0] and size() if radius > top of top layer.
   *
   * @param profile Profile
   * @param radius double
   * @return int
   * @throws GeoTessException
   */
  public Level getInterface(GeoTessPosition profile, double radius)
         throws GeoTessException
  {
    int i, bot = -1, top = interfaces.size();
    while (top - bot > 1)
    {
      i = (top + bot) / 2;
      if (radius > interfaces.get(i).getRadius(profile))
        bot = i;
      else
        top = i;
    }
    
    if (top >= 0 && top < interfaces.size())
      return interfaces.get(top);
    return null;
  }

	/**
	 * Find index i such that radius resides in the i'th layer, i.e., r >
	 * interface i and r <= interface i+1.Returns -1 if radius <= bottom
	 * oflayer[0] and size() if radius > top of top layer.
	 *
	 * @param profile Profile
	 * @return int
	 * @throws GeoTessException
	 */
	public Level getInterface(GeoTessPosition profile)
	       throws GeoTessException
	{
    return getInterface(profile, profile.getRadius());
  }

  /**
   * Retrieve the Level object that corresponds to the
   * specified majorLayerIndex.
   * @param majorLayerIndex int
   * @return Level
   */
  public Level getMajorLayer(int majorLayerIndex)
  {
    if (!mapUpToDate)
    {
      for (Level i : interfaces)
        if (i.getLayerSide() == LayerSide.TOP)
          majorLayerIndexMap[i.getMajorLayerIndex()] = i;
      mapUpToDate = true;
    }

    return majorLayerIndexMap[majorLayerIndex];
  }

  /**
   * Find which major layer profile is located in and return the Level
   * object of the top of that layer.  The Level object returned will
   * be a major layer interface.
   * @param profile Profile
   * @return Level
   * @throws GeoTessException
   */
  public Level getMajorLayer(GeoTessPosition profile)
         throws GeoTessException
  {
    return getMajorLayer(getInterface(profile).getIndex());
  }

  /**
   * Retrieve the thickness of the i'th layer.  This is the thickness
   * between two Level objects not the thickness of a major layer.
   *
   * @param profile Profile
   * @param i int
   * @return double
   * @throws GeoTessException
   */
  public double getLayerThickness(GeoTessPosition profile, int i)
  		   throws GeoTessException
  {
    if (i == 0)
      return interfaces.get(0).getRadius(profile);
    return interfaces.get(i).getRadius(profile)
        - interfaces.get(i - 1).getRadius(profile);
  }

	/**
	 * Retrieve the thickness of the specified layer.  This is the thickness
	   * between two Level objects not the thickness of a major layer.
	 *
	 * @param profile Profile
	 * @param layer int
	 * @return double
	 * @throws GeoTessException
	 */
  public double getLayerThickness(GeoTessPosition profile, Level layer)
  		   throws GeoTessException
  {
    return getLayerThickness(profile, layer.getIndex());
  }

  /**
   * Find the next layer above the specified layer, whose thickness is greater
   * than specified minimum value. If no such layer exists, returns null.
   *
   * @param profile Profile
   * @param layer int
   * @param minThickness double
   * @return int
   * @throws GeoTessException
   */
  public Level getNextInterface(GeoTessPosition profile, Level layer,
                                 double minThickness)
         throws GeoTessException
  {
    int i = layer.getIndex()+1;
    while (i < getNInterfaces() && getLayerThickness(profile, i) < minThickness)
        ++i;
    if (i < getNInterfaces())
        return getInterface(i);
    return null;
  }

  /**
   * Find the layer at or below the specified layer that is thicker than the
   * specified minimum. If no such layer exists, returns null.
   *
   * @param profile Profile
   * @param layer int
   * @param minThickness double
   * @return int
   * @throws GeoTessException
   */
  public Level getPreviousInterface(GeoTessPosition profile, Level layer,
                                     double minThickness)
         throws GeoTessException
  {
    int i = layer.getIndex();
    while (i > 0 && getLayerThickness(profile, i) < minThickness)
        --i;
    if (i >= 0)
        return getInterface(i);
    return null;
  }

  /**
   * Given the input level index find the next lower level whose major layer
   * index is 1 smaller than the major layer index of the input level. Return
   * that index.
   *  
   * @param levelIndx The level index to begin with.
   * @return The next lower level whose major layer index is 1 smaller than the
   *         major layer index of the input level.
   */
  public int getPreviousMajorLayerLevelIndex(int currentLevelIndx)
  {
  	int i = currentLevelIndx - 1;
  	int mli = interfaces.get(currentLevelIndx).getMajorLayerIndex();
  	while ((i > 0) && (interfaces.get(i).getMajorLayerIndex() == mli)) --i;
  	
  	return i;
  }

  /**
   * Retrieve the radius of profile and constrain it to be between the top of
   * the specfied layer and the top of the next layer down from the specified
   * layer.
   *
   * @param profile Profile
   * @param layer int
   * @throws GeoTessException
   */
  public void constrainRadius(GeoTessPosition profile, int layer)
         throws GeoTessException
  {
    double pr = profile.getRadius();
    double r = interfaces.get(layer).getRadius(profile);
    if (pr > r)
        profile.setRadius(r);
    else if (layer > 0)
    {
        r = interfaces.get(layer-1).getRadius(profile);
        if (pr <= r)
            profile.setRadius(r);
    }
  }

  /**
   * Retrieve a string representation of the information content of this Layers
   * object. Includes some velocity information from the specified profile.
   *
   * @param buf StringBuffer
   * @param profile Profile
   * @param attribute GeoAttributes
   * @throws GeoTessException
   */
  public void toString(StringBuffer buf, GeoTessPosition profile,
		                   GeoAttributes attribute)
         throws GeoTessException
  {
		GeoTessPosition gtp = GeoTessPosition.getGeoTessPosition(profile);
		gtp.set(profile.getVector(), profile.getRadius());
	  for (int i = getNInterfaces() - 1; i >= 0; --i)
	  {
		  buf.append(String.format("%4d  ", getInterface(i).getIndex()));
	
		  getInterface(i).toString(buf, gtp, attribute);
	
		  buf.append(String.format(" th=%10.4f", getLayerThickness(gtp, i)));
	
		  double[] normal = getInterface(i).getNormal(gtp);
	
		  buf.append(String.format(" nrml=%5.2f az_nrml=%6.2f%n",
				  Math.toDegrees(VectorUnit.angle(gtp.getVector(), normal)),
				  Math.toDegrees(VectorUnit.azimuth(gtp.getVector(), normal,
						  Assoc.AZRES_NA))));
	  }
  }

  /**
   * Retrieve a string representation of the information content of this
   * Layers object.
   *
   * @return String
   */
  @Override
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    for (int i = interfaces.size() - 1; i >= 0; --i)
    {
        buf.append(
            String.format("%4d  ",
                          interfaces.get(i).getIndex()));

        buf.append(interfaces.get(i).toString());
    }

    return buf.toString();
  }
}
