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
package gov.sandia.gmp.rayuncertainty.basecontainers;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * A map of PhaseSiteBase type objects associated with a SeismicPhase, and
 * two unique receiver id's.
 *
 * @param <T>
 * @author jrhipp
 */
@SuppressWarnings("serial")
public abstract class PhaseSiteSiteMap<T extends PhaseSiteBase>
        implements Serializable {
    /**
     * The map of derived type PhaseSiteBase object (T) associated with a unique
     * phase and two unique receiver id's.
     */
    private HashMap<SeismicPhase, HashMap<Long, HashMap<Long, T>>>
            aMap = null;

    /**
     * The list of all entries in aMap.
     */
    private ArrayList<T> aList = null;

    /**
     * A list of all SeismicPhase entries associated with their corresponding
     * entry in aList.
     */
    private ArrayList<SeismicPhase> aPhaseList = null;

    /**
     * A list of all receiver A id entries associated with their corresponding
     * entry in aList.
     */
    private ArrayListLong aSiteAIdList = null;

    /**
     * A list of all receiver B id entries associated with their corresponding
     * entry in aList.
     */
    private ArrayListLong aSiteBIdList = null;

    /**
     * The array of all unique phases input into this map.
     */
    private HashSet<SeismicPhase> aUniquePhases = null;

    /**
     * The array of all unique A receiver id's input into this map.
     */
    private HashSet<Long> aUniqueASites = null;

    /**
     * The array of all unique B receiver id's input into this map.
     */
    private HashSet<Long> aUniqueBSites = null;

    /**
     * The total number of phase/site/site entries input into this map
     */
    private int aPhaseSiteSiteCount = 0;

    /**
     * An abstract function defined by a derived class that reads this
     * PhaseSiteSiteMap object from a file defined by the input path string fpth.
     *
     * @param fpth The input Path string defining the file from which this object
     *             will be read.
     * @throws IOException
     */
    public abstract void read(String fpth) throws IOException;

    /**
     * An abstract function used to get the type T object contained by this map
     * associated with the input phase, receiver A id, and receiver B id.
     *
     * @param phase   The phase for which an associated T object will be returned.
     * @param siteAid The receiver A id for which an associated T object will be
     *                returned.
     * @param siteBid The receiver B id for which an associated T object will be
     *                returned.
     * @return
     */
    public abstract T getSet(SeismicPhase phase, long siteAid, long siteBid);

    /**
     * Default constructor.
     */
    public PhaseSiteSiteMap() {
        createStorage(0);
    }

    /**
     * Clears this PhaseSiteSiteMap and reinitializes all containers back to an
     * empty state.
     */
    public void clear() {
        aMap.clear();
        aList.clear();
        aPhaseList.clear();
        aSiteAIdList.clear();
        aSiteBIdList.clear();
        aUniquePhases.clear();
        aUniqueASites.clear();
        aUniqueBSites.clear();
        aPhaseSiteSiteCount = 0;
    }

    /**
     * Returns the total number of phase/site/site entries input into this map.
     *
     * @return The total number of phase/site/site entries input into this map.
     */
    public int getPhaseSiteSiteCount() {
        return aPhaseSiteSiteCount;
    }

    /**
     * Returns the total number of phases input into this map.
     *
     * @return The total number of phases input into this map.
     */
    public int getUniquePhaseCount() {
        return aUniquePhases.size();
    }

    /**
     * Returns the total number of A site id's input into this map.
     *
     * @return The total number of A site id's input into this map.
     */
    public int getUniqueASiteCount() {
        return aUniqueASites.size();
    }

    /**
     * Returns the total number of B site id's input into this map.
     *
     * @return The total number of B site id's input into this map.
     */
    public int getUniqueBSiteCount() {
        return aUniqueBSites.size();
    }

    /**
     * Returns the unique array of phases contained in this map.
     *
     * @return The unique array of phases contained in this map.
     */
    public SeismicPhase[] getPhases() {
        // create array

        SeismicPhase[] phases = new SeismicPhase[aUniquePhases.size()];

        // loop over all unique phases in the set and add to array

        int i = 0;
        for (SeismicPhase phase : aUniquePhases) phases[i++] = phase;

        // return array

        return phases;
    }

    /**
     * Returns the unique array of A site id's contained in this map.
     *
     * @return The unique array of A site id's contained in this map.
     */
    public Long[] getSiteAIds() {
        return (Long[]) aUniqueASites.toArray();
    }

    /**
     * Returns the unique array of B site id's contained in this map.
     *
     * @return The unique array of B site id's contained in this map.
     */
    public Long[] getSiteBIds() {
        return (Long[]) aUniqueBSites.toArray();
    }

    /**
     * Returns the entire set of objects in the map as a list.
     *
     * @return The entire set of objects in the map as a list.
     */
    public ArrayList<T> getList() {
        return aList;
    }

    /**
     * Returns the SeismicPhase associated with the object in the list at index
     * listIndex.
     *
     * @param listIndex The index of the object in the list (aList) whose
     *                  SeismicPhase will be returned.
     * @return
     */
    public SeismicPhase getPhase(int listIndex) {
        return aPhaseList.get(listIndex);
    }

    /**
     * Returns the receiver A id associated with the object in the list at index
     * listIndex.
     *
     * @param listIndex The index of the object in the list (aList) whose
     *                  receiver A id will be returned.
     * @return
     */
    public long getSiteAId(int listIndex) {
        return aSiteAIdList.get(listIndex);
    }

    /**
     * Returns the receiver B id associated with the object in the list at index
     * listIndex.
     *
     * @param listIndex The index of the object in the list (aList) whose
     *                  receiver B id will be returned.
     * @return
     */
    public long getSiteBId(int listIndex) {
        return aSiteBIdList.get(listIndex);
    }

    /**
     * Creates a new storage allocation for this PhaseSiteSiteMap
     *
     * @param n The allocation size for all containers. If zero or less default
     *          instantiation is used.
     */
    private void createStorage(int n) {
        // use defaults if n <= 0

        if (n <= 0) {
            aMap = new HashMap<SeismicPhase,
                    HashMap<Long, HashMap<Long, T>>>();
            aList = new ArrayList<T>();
            aPhaseList = new ArrayList<SeismicPhase>();
            aSiteAIdList = new ArrayListLong();
            aSiteBIdList = new ArrayListLong();
            aUniquePhases = new HashSet<SeismicPhase>();
            aUniqueASites = new HashSet<Long>();
            aUniqueBSites = new HashSet<Long>();
        } else {
            int sze = 4 * n / 3;
            aMap = new HashMap<SeismicPhase,
                    HashMap<Long, HashMap<Long, T>>>(sze);
            aList = new ArrayList<T>(n);
            aPhaseList = new ArrayList<SeismicPhase>(n);
            aSiteAIdList = new ArrayListLong(n);
            aSiteBIdList = new ArrayListLong(n);
            aUniquePhases = new HashSet<SeismicPhase>(sze);
            aUniqueASites = new HashSet<Long>(sze);
            aUniqueBSites = new HashSet<Long>(sze);
        }
    }

    /**
     * Returns the T object associated with the input seismic phase, receiver A
     * id, and receiver B id, or null if one has not been associated with the
     * keys.
     *
     * @param phase   The input phase for which an associated T object will be
     *                returned.
     * @param siteAid The input receiver A id for which an associated T object
     *                will be returned.
     * @param siteAid The input receiver B id for which an associated T object
     *                will be returned.
     * @return The associated T object or null if one is not found.
     */
    public T get(SeismicPhase phase, long siteAid, long siteBid) {
        HashMap<Long, HashMap<Long, T>> siteAMap;
        HashMap<Long, T> siteBMap;

        // get site A map ... if null return null ... otherwise get site B map ...
        // if null return null ... otherwise get associated T object and return

        siteAMap = aMap.get(phase);
        if (siteAMap == null) return null;
        siteBMap = siteAMap.get(siteAid);
        if (siteBMap == null) return null;
        return siteBMap.get(siteBid);
    }

    /**
     * Adds a new phase/site A id/site B id entry to the ray weight map and
     * returns the existing entry. If the phase/site A id/site B id entry is
     * already present the existing entry is returned.
     *
     * @param phase   The phase for which the new/existing entry is to be added/
     *                retrieved.
     * @param siteAid The receiver A id for which the new/existing entry is to be
     *                added/retrieved.
     * @param siteBid The receiver B id for which the new/existing entry is to be
     *                added/retrieved.
     * @param newObj  If a new object is to be added then use this default entry.
     * @return The contained T object, or the input newObj if one is not yet
     * associated with the input keys.
     */
    protected T getSet(SeismicPhase phase, long siteAid, long siteBid, T newObj) {
        HashMap<Long, HashMap<Long, T>> siteAMap;
        HashMap<Long, T> siteBMap;

        // get the site A map ... if it was not present add a new one

        siteAMap = aMap.get(phase);
        if (siteAMap == null) {
            // make the new site A map and add it to the ray weight map ... also
            // add the phase to the unique phase set

            siteAMap = new HashMap<Long, HashMap<Long, T>>();
            aMap.put(phase, siteAMap);
            aUniquePhases.add(phase);
        }

        siteBMap = siteAMap.get(siteAid);
        if (siteBMap == null) {
            // make the new site B map and add it to the ray weight map ... also add
            // the phase to the unique phase set

            siteBMap = new HashMap<Long, T>();
            siteAMap.put(siteAid, siteBMap);
            aUniqueASites.add(siteAid);
        }

        // get the T object ... if it is not present add a new one

        T obj = siteBMap.get(siteBid);
        if (obj == null) {
            // set the new T object as the input newObje and and add it to the
            // site B map ... also add to the unique site B id set and increment the
            // phase/site/site entry count.

            obj = newObj;
            siteBMap.put(siteBid, obj);
            aList.add(obj);
            aPhaseList.add(phase);
            aSiteAIdList.add(siteAid);
            aSiteBIdList.add(siteBid);
            aUniqueBSites.add(siteBid);
            ++aPhaseSiteSiteCount;
        }

        // returns the new or existing T object associated with
        // the input phase and site id.

        return obj;
    }

    /**
     * Adds the input entry to the internal map and list associated with the
     * input phase, receiver A id, and receiver B id.
     *
     * @param phase   The phase associated with the input object (obj).
     * @param siteAid The receiver A id associated with the input object (obj).
     * @param siteBid The receiver B id associated with the input object (obj).
     * @param obj     The object to be added.
     */
    public void add(SeismicPhase phase, long siteAid, long siteBid, T obj) {
        // see if the phase is is the map

        HashMap<Long, HashMap<Long, T>> siteAMap = aMap.get(phase);
        if (siteAMap == null) {
            // new phase ... make the new receiver A map and add it to the phase map
            // associated with the phase ... also add the phase to the unique phase
            // set

            siteAMap = new HashMap<Long, HashMap<Long, T>>();
            aMap.put(phase, siteAMap);
            aUniquePhases.add(phase);
        }

        // get the site B map and see if the receiver A id is in the map

        HashMap<Long, T> siteBMap = siteAMap.get(siteAid);
        if (siteBMap == null) {
            // new receiver A id ... make the receiver B map and add it to the
            // receiver A map associated with the receiver A id ... add the receiver
            // A id to the unique receiver id set

            siteBMap = new HashMap<Long, T>();
            siteAMap.put(siteAid, siteBMap);
            aUniqueASites.add(siteAid);
        }

        // get the current T object if it exists

        T currObj = siteBMap.get(siteBid);
        if (currObj == null) {
            // new T object ... add the input object to the list, its phase to the
            // phase list, its receiver A id to the receiver A list, and its receiver
            // B id to the receiver B list. Also, add the receiver B id to the unique
            // receiver B set and increment the number of phase/site/site entries.

            aList.add(obj);
            aPhaseList.add(phase);
            aSiteAIdList.add(siteAid);
            aSiteBIdList.add(siteBid);
            aUniqueBSites.add(siteBid);
            ++aPhaseSiteSiteCount;
        } else {
            // object exists so replace current one with the input one ... first find
            // the existing one in the list so that it can be replaced and then set
            // it with the new one

            int indx = aList.size();
            for (indx = 0; indx < aList.size(); ++indx)
                if (aList.get(indx) == currObj) break;
            aList.set(indx, obj);
        }

        // add or replace current object with new one in the map

        siteBMap.put(siteBid, obj);
    }

    /**
     * Reads / reinitializes this object from the definition contained in the
     * input file.
     *
     * @param fpth Path to where the new definition for this PhaseSiteRayWeightMap
     *             resides.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    protected void read(T readerObj, String fpth) throws IOException {
        // create the filename and a new file input buffer

        FileInputBuffer fib = new FileInputBuffer(fpth);

        // read the number of phase entries and create a new map of phase
        // associated with a site / site / T object map ... loop over all phases

        int n = fib.readInt();
        if (aMap == null)
            createStorage(n);
        else
            clear();

        // loop over all entries and read in each new T object

        for (int i = 0; i < n; ++i) {
            // get the phase name, receiver A id, receiver B id, and read in the
            // associated T object ... add to the maps and lists

            SeismicPhase phase = SeismicPhase.valueOf(fib.readString());
            long siteAid = fib.readLong();
            long siteBid = fib.readLong();
            T obj = (T) readerObj.readNew(fib);
            add(phase, siteAid, siteBid, obj);
        }

        // done ... close file and return map

        fib.close();
    }

    /**
     * Writes this PhaseSiteSiteMap object to the file defined by the input path.
     *
     * @param fpth Path to where the this T object will be
     *             written.
     * @throws IOException
     */
    public void write(String fpth) throws IOException {
        // create the file output buffer

        FileOutputBuffer fob = new FileOutputBuffer(fpth);

        // write out the number of PhaseSiteObservations entries and loop over each
        // unique phase

        fob.writeInt(aPhaseSiteSiteCount);
        for (Map.Entry<SeismicPhase, HashMap<Long, HashMap<Long, T>>> e :
                aMap.entrySet()) {
            // get the phase and site A map and loop over all entries in site A map

            SeismicPhase ph = e.getKey();
            HashMap<Long, HashMap<Long, T>> siteAMap = e.getValue();
            for (Map.Entry<Long, HashMap<Long, T>> esiteA : siteAMap.entrySet()) {
                // get site A id, site B map and loop over all entries in site B map

                long siteAid = esiteA.getKey();
                HashMap<Long, T> siteBMap = esiteA.getValue();
                for (Map.Entry<Long, T> esiteB : siteBMap.entrySet()) {
                    // get the site B id and the T object and write the
                    // phase, site A id, site B id, and T object to disk

                    long siteBid = esiteB.getKey();
                    T obj = esiteB.getValue();
                    fob.writeString(ph.name());
                    fob.writeLong(siteAid);
                    fob.writeLong(siteBid);
                    obj.write(fob);
                }
            }
        }

        // done ... close file and return

        fob.close();
    }
}
