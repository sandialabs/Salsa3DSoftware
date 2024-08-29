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
package gov.sandia.gmp.observationprediction;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
//import gov.sandia.gmp.geomodel.GeoModelUUL;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ObservationList
    extends ArrayList<ObservationTomo> implements Cloneable, Externalizable
{

  private GeoTessModel geoModel = null;

  private HashMap<Long, Integer> origins = null;
  private HashMap<Long, Integer> sites = null;
  private HashMap<Long, String> receiverNames = null;

  /**
   * Used to associate all unique refsta names with a unique refsta index
   */
  private HashMap<String, Integer> refStas = null;

  private ArrayListLong uniqueOrids = null;
  private ArrayListLong uniqueSites = null;
  private ArrayList<String> uniqueRefStas = null;

  /**
   * True if origins or sites need to be updated.  Set to true
   * when Observations are added or removed from the List.  Set to false
   * in updateIndexes().
   */
  private boolean stateChanged;

  /**
   * The set of GeoAttributes that Bender is supposed to compute. 
   * Bender will always compute things like TRAVEL_TIME, AZIMUTH, etc.
   * but it only computes SLOWNESS, DTT_DSLOW, RAY_PATH, and various
   * derivatives, on request.  To request something, add the 
   * GeoAttribute to this set.  
   * BenderObservation objects have a reference to this Set, which
   * they carry with them into Bender, which will then compute 
   * requested items.
   */
  private EnumSet<GeoAttributes> requestedAttributes = EnumSet.noneOf(GeoAttributes.class);

  /**
   *
   */
  public ObservationList()
  {
    super();
    stateChanged = true;
  }

  /**
   *
   * @param geoModel GeoModelUUL
   */
  public ObservationList(GeoTessModel geoModel)
  {
    super();
    this.geoModel = geoModel;
    stateChanged = true;
  }

  /**
   *
   * @param collection Collection
   */
  public ObservationList(Collection<? extends ObservationTomo> collection)
  {
    super(collection);
    stateChanged = true;
  }

  /**
   *
   * @param _int int
   */
  public ObservationList(int _int)
  {
    super(_int);
    stateChanged = true;
  }

  /**
   * Retreive a refence to the GeoModelUUL object stored
   * in this ObservationList
   * @return GeoModelUUL
   */
  public GeoTessModel getGeoModel()
  {
    return geoModel;
  }

  /**
   * Set the a GeoModelUUL object reference.
   * 
   * @param geomodel The GeoModelUUL object reference.
   */
  public void setGeoModel(GeoTessModel geomodel)
  {
    this.geoModel = geomodel;
  }

  /**
   *  Add an Observation.
   * @param obs Object
   * @return boolean
   */
  @Override
  public boolean add(ObservationTomo obs)
  {
    boolean changed = super.add(obs);
    stateChanged = stateChanged || changed;
    return changed;
  }

  /**
   * Remove an Observation.
   * @param obs Object
   * @return boolean
   */
  public boolean remove(ObservationTomo obs)
  {
    boolean changed = super.contains(obs);
    
    stateChanged = stateChanged || changed;

    ObservationTomo last = get(size()-1);
    set(obs.getIndex(), last);
    last.setIndex(obs.getIndex());
    super.remove(size()-1);
    return changed;
  }

  /**
   * Remove an Observation rapidly by changing the
   * list order
   * 
   * @param int i
   * @return boolean
   */
  public ObservationTomo removeUnordered(int i)
  {
    // get the ith and last observations

	ObservationTomo obs = get(i);
	ObservationTomo last = get(size()-1);

    // set the last observation into index i
    // and set the last observations index to i

    set(i, last);
    last.setIndex(i);

    // remove the last entry and set the change flag

    super.remove(size()-1);
    stateChanged = true;

    // exit and return the removed observation

    return obs;
  }

  /**
   * For each unique Origin and each unique Site in the set of observations,
   * give each Origin a unique, consecutively numbered index (starting with
   * zero), and each Site a a unique, consecutively numbered index (starting
   * with zero)
   */
  public void updateIndexes()
  {
    origins = new HashMap<Long, Integer>(size()/2);
    sites   = new HashMap<Long, Integer> (size()/2);
    refStas = new HashMap<String, Integer> (size()/2);

    // build map of receiver id to station name for output purposes
    receiverNames = new HashMap<Long, String>(size()/2);
    
    int index = 0;
    Integer i;
    for (ObservationTomo obs : this)
    {
      obs.setIndex(index++);

      i = origins.get(obs.getSourceId());
      if (i == null)
      {
        i=origins.size();
        origins.put(obs.getSourceId(), i);
      }
      obs.setOriginIndex(i);

      i = sites.get(obs.getReceiverId());
      if (i == null)
      {
        i=sites.size();
        sites.put(obs.getReceiverId(), i);
      }
      obs.setSiteIndex(i);

      String rs = obs.getReceiver().getRefsta();

      i = refStas.get(obs.getReceiver().getRefsta());
      if (i == null)
      {
        i=refStas.size();
        refStas.put(rs, i);
      }
      obs.setRefStaIndex(i);

      // save mapping from receiverId to receiver name
      receiverNames.put(obs.getReceiverId(), obs.getReceiverName());
    }

    // build array lists of unique orids and unique sites

    if (uniqueOrids == null)   uniqueOrids   = new ArrayListLong();
    if (uniqueSites == null)   uniqueSites   = new ArrayListLong();
    if (uniqueRefStas == null) uniqueRefStas = new ArrayList<String>();

    uniqueOrids.clear();
    uniqueOrids.setSize(origins.size());
    for (Map.Entry<Long, Integer> pair: origins.entrySet())
      uniqueOrids.set(pair.getValue().intValue(), pair.getKey().longValue());

    uniqueSites.clear();
    for (int j = 0; j < sites.size(); ++j) uniqueSites.add(-1); //TODO is this still needed now that we use receiverid?
    for (Map.Entry<Long, Integer> pair: sites.entrySet())
      uniqueSites.set(pair.getValue().intValue(), pair.getKey().longValue());

    uniqueRefStas.clear();
    for (Map.Entry<String, Integer> pair: refStas.entrySet())
    	uniqueRefStas.add(pair.getKey());

    //System.out.printf("ObservationList.updateIndexes(). NObservations=%d, NOrigins=%d, NSites=%d%n",
    //                  size(), origins.size(), sites.size());
    stateChanged = false;
  }

  /**
   * Returns map of unique origins associated with their index.
   *
   * @return Map of unique origins associated with their index.
   */
  public HashMap<Long, Integer> getOridIndexMap()
  {
    if (stateChanged) updateIndexes();
    return origins;
  }

  /**
   * Returns map of unique sites associated with their index.
   *
   * @return Map of unique sites associated with their index.
   */
  public HashMap<Long, Integer> getSiteIndexMap()
  {
    if (stateChanged) updateIndexes();
    return sites;
  }

  /**
   * Returns the unique refsta map of refsta names associated with their
   * sequentially incremented index.
   * 
   * @return The unique refsta map of refsta names associated with their
   *         sequentially incremented index.
   */
  public HashMap<String, Integer> getRefStaIndexMap()
  {
  	if (stateChanged) updateIndexes();
  	return refStas;
  }

  /**
   * Returns unique ORID list.
   *
   * @return Unique ORID list.
   */
  public ArrayListLong getUniqueOrids()
  {
    if (stateChanged) updateIndexes();
    return uniqueOrids;
  }

  /**
   * Returns the unique ORID count.
   * 
   * @return The unique ORID count.
   */
  public int getUniqueOridCount()
  {
    if (stateChanged) updateIndexes();
    return uniqueOrids.size();
  }

  /**
   * Returns unique SITE list.
   *
   * @return Unique SITE list.
   */
  public ArrayListLong getUniqueSites()
  {
    if (stateChanged) updateIndexes();
    return uniqueSites;
  }

  /**
   * Returns the unique site count.
   * 
   * @return The unique site count.
   */
  public int getUniqueSiteCount()
  {
    if (stateChanged) updateIndexes();
    return uniqueSites.size();
  }

  /**
   * Returns unique refsta list.
   *
   * @return Unique refsta list.
   */
  public ArrayList<String> getUniqueRefStas()
  {
    if (stateChanged) updateIndexes();
    return uniqueRefStas;
  }

  /**
   * Returns the unique refsta count.
   * 
   * @return The unique refsta count.
   */
  public int getUniqueRefStaCount()
  {
    if (stateChanged) updateIndexes();
    return uniqueRefStas.size();
  }

  /**
   * Returns the unique site count ... unique refsta count if input boolean
   * is true.
   * 
   * @return The unique site count.
   */
  public int getUniqueSiteCount(boolean useRefSta)
  {
    if (stateChanged) updateIndexes();
    
    if (useRefSta)
      return uniqueRefStas.size();
    else
      return uniqueSites.size();
  }

  /**
   * Returns map of receiver id's associated with their names.
   *
   * @return map of receiver id's associated with their names
   */
  public HashMap<Long, String> getReceiverNames()
  {
    if (stateChanged) updateIndexes();
    return receiverNames;
  }
//
//  /**
//   * Resets the adaption level residual to -1 (initial value).
//   */
//  public void resetAdaptionLevelResidual()
//  {
//    for (int i = 0; i < size(); ++i) get(i).resetAdaptionLevelResidual();
//  }

  /**
   * Removes all marked observations from the list.
   */
  public int removeMarked()
  {
    int count = 0;
    for (int i = 0; i < size(); ++i)
    {
      // get the ith observation and see if it is marked

      ObservationTomo obs = get(i);
      if (obs.isMarked())
      {
        // it is marked ... remove it and decrement i so that the new
        // observation at that position (was last entry) can be checked ...
        // also set the state change flag

        ++count;
        removeUnordered(obs.getIndex());
        --i;
        stateChanged = true;
      }
    }

    // done return removal count

    return count;
  }

  /**
   * Remove all input Observations.
   * 
   * @param obslst The input list of observations to be removed.
   */
  public void remove(ArrayList<ObservationTomo> obslst)
  {
    if (obslst.size() > 0)
    {
      for (int i = 0; i < obslst.size(); ++i)
      {
        ObservationTomo obs = obslst.get(i);
        removeUnordered(obs.getIndex());
      }
      stateChanged = true;
    }
  }

  /**
   * Returns a list of cloned observations, where each observation
   * is cloned with a shallow copy.
   * @return
   */
  @Override
  public Object clone()
  {
	  ObservationList newList = new ObservationList(this.size());
	  try
	  {
		  for(ObservationTomo ob: this)
			  newList.add((ObservationTomo) ob.clone());
	  }
	  catch (CloneNotSupportedException e)
	  {
		  e.printStackTrace();
	  }
	  newList.setGeoModel(this.getGeoModel());
	  newList.updateIndexes();
	  return newList;
  }

  /**
   * The set of GeoAttributes that Bender is supposed to compute.  
   * Might include TRAVEL_TIME, DTT_DSLOW, RAY_PATH, etc.
   */
  public EnumSet<GeoAttributes> getRequestedAttributes() 
  {
  	return requestedAttributes;
  }

  /**
   * Corrects origin times for all observations in this list by adding the
   * event term to the origin time and zeroing the event term. 
   */
  public void correctOriginTime()
  {
    for (ObservationTomo obs: this) obs.correctOriginTime();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    // TODO Auto-generated method stub
    
  }
}
