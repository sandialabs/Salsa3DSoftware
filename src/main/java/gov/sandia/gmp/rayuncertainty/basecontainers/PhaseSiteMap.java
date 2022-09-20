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
 * A map of PhaseSiteBase type objects associated with a SeismicPhase and a
 * unique receiver id.
 *
 * @param <T>
 * @author jrhipp
 */
@SuppressWarnings("serial")
public abstract class PhaseSiteMap<T extends PhaseSiteBase>
        implements Serializable {
    /**
     * The map of derived type PhaseSiteBase object (T) associated with a unique
     * phase and receiver id.
     */
    private HashMap<SeismicPhase, HashMap<Long, T>>
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
     * A list of all receiver id entries associated with their corresponding
     * entry in aList.
     */
    private ArrayListLong aSiteIdList = null;

    /**
     * The array of all unique phases input into aMap.
     */
    private HashSet<SeismicPhase> aUniquePhases = null;

    /**
     * The array of all unique receiver id's input into aMap.
     */
    private HashSet<Long> aUniqueSites = null;

    /**
     * The total number of phase/site pairs input into this map
     */
    private int aPhaseSiteCount = 0;

    /**
     * An abstract function defined by a derived class that reads this
     * PhaseSiteMap object from a file defined by the input path string fpth.
     *
     * @param fpth The input Path string defining the file from which this object
     *             will be read.
     * @throws IOException
     */
    public abstract void read(String fpth) throws IOException;

    /**
     * An abstract function used to get the type T object contained by this map
     * associated with the input phase and receiver id.
     *
     * @param phase  The phase for which an associated T object will be returned.
     * @param siteid The receiver id for which an associated T object will be
     *               returned.
     * @return
     */
    public abstract T getSet(SeismicPhase phase, long siteid);

    /**
     * Default constructor.
     */
    public PhaseSiteMap() {
        createStorage(0);
    }

    /**
     * Clears this PhaseSiteMap and reinitializes all containers back to an empty
     * state.
     */
    public void clear() {
        aMap.clear();
        aList.clear();
        aPhaseList.clear();
        aSiteIdList.clear();
        aUniquePhases.clear();
        aUniqueSites.clear();
        aPhaseSiteCount = 0;
    }

    /**
     * Returns the total number of phase/site pairs input into this map.
     *
     * @return The total number of phase/site pairs input into this map.
     */
    public int getPhaseSiteCount() {
        return aPhaseSiteCount;
    }

    /**
     * Returns the total number of unique phases input into this map.
     *
     * @return The total number of unique phases input into this map.
     */
    public int getUniquePhaseCount() {
        return aUniquePhases.size();
    }

    /**
     * Returns the total number of unique receiver id's input into this map.
     *
     * @return The total number of unique receiver id's input into this map.
     */
    public int getUniqueSiteCount() {
        return aUniqueSites.size();
    }

    /**
     * Returns the unique phases contained in this map as an array.
     *
     * @return The unique phases contained in this map as an array.
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
     * Returns the unique receiver id's contained in this map as an array.
     *
     * @return The unique receiver id's contained in this map as an array.
     */
    public Long[] getSiteIds() {
        return (Long[]) aUniqueSites.toArray();
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
     * Returns the site id map associated with the input phase. If not found null
     * is returned.
     *
     * @param phase The input phase for which the site id map will be returned.
     * @return The site id map associated with the input phase. If not found null
     * is returned.
     */
    public HashMap<Long, T> getSiteIdMap(SeismicPhase phase) {
        return aMap.get(phase);
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
     * Returns the receiver id associated with the object in the list at index
     * listIndex.
     *
     * @param listIndex The index of the object in the list (aList) whose
     *                  receiver id will be returned.
     * @return
     */
    public long getSiteId(int listIndex) {
        return aSiteIdList.get(listIndex);
    }

    /**
     * Creates a new storage allocation for this PhaseSiteMap
     *
     * @param n The allocation size for all containers. If zero or less default
     *          instantiation is used.
     */
    private void createStorage(int n) {
        // use defaults if n <= 0

        if (n <= 0) {
            aMap = new HashMap<SeismicPhase, HashMap<Long, T>>();
            aList = new ArrayList<T>();
            aPhaseList = new ArrayList<SeismicPhase>();
            aSiteIdList = new ArrayListLong();
            aUniquePhases = new HashSet<SeismicPhase>();
            aUniqueSites = new HashSet<Long>();
        } else {
            int sze = 4 * n / 3;
            aMap = new HashMap<SeismicPhase, HashMap<Long, T>>(sze);
            aList = new ArrayList<T>(n);
            aPhaseList = new ArrayList<SeismicPhase>(n);
            aSiteIdList = new ArrayListLong(n);
            aUniquePhases = new HashSet<SeismicPhase>(sze);
            aUniqueSites = new HashSet<Long>(sze);
        }
    }

    /**
     * Returns the T object associated with the input seismic phase and receiver
     * id, or null if one has not been associated with the pair.
     *
     * @param phase  The input phase for which an associated T object will be
     *               returned.
     * @param siteid The input receiver id for which an associated T object will
     *               be returned.
     * @return The associated T object or null if one is not found.
     */
    public T get(SeismicPhase phase, long siteid) {
        HashMap<Long, T> siteMap;

        // get site map ... if null return null ... otherwise get associated
        // PhaseSiteObservations object (on null if one is not found)

        siteMap = aMap.get(phase);
        if (siteMap == null) return null;
        return siteMap.get(siteid);
    }

    /**
     * Adds a new phase/site id pair to the ray weight map and return the new/
     * existing entry. If the phase/site id pair is already present the existing
     * entry is returned.
     *
     * @param phase  The phase for which the new/existing entry is to be added/
     *               retrieved.
     * @param siteid The site id for which the new/existing entry is to be added/
     *               retrieved.
     * @param newObj If a new object is to be added then use this default entry.
     * @return The contained T object, or the input newObj if one is not yet
     * associated with the input keys.
     */
    protected T getSet(SeismicPhase phase, long siteid, T newObj) {
        HashMap<Long, T> siteMap;

        // get the site map ... if it was not present add a new one

        siteMap = aMap.get(phase);
        if (siteMap == null) {
            // make the new site map and add it to the ray weight map ... also add the
            // phase to the unique phase set

            siteMap = new HashMap<Long, T>();
            aMap.put(phase, siteMap);
            aUniquePhases.add(phase);
        }

        // get the T object ... if it is not present add a new one

        T obj = siteMap.get(siteid);
        if (obj == null) {
            // T object not contained ... use the input newObj and add it to the site
            // map ... also add to the unique site id and increment the phase/site
            // entry count.

            obj = newObj;
            siteMap.put(siteid, obj);
            aList.add(obj);
            aPhaseList.add(phase);
            aSiteIdList.add(siteid);
            aUniqueSites.add(siteid);
            ++aPhaseSiteCount;
        }

        // returns the new or existing T object associated with
        // the input phase and receiver id.

        return obj;
    }

    /**
     * Adds the input entry to the internal map and list associated with the
     * input phase and receiver id.
     *
     * @param phase  The phase associated with the input object (obj).
     * @param siteid The receiver id associated with the input object (obj).
     * @param obj    The object to be added.
     */
    public void add(SeismicPhase phase, long siteid, T obj) {
        // see if the phase is is the map

        HashMap<Long, T> siteMap = aMap.get(phase);
        if (siteMap == null) {
            // new phase ... make the new site map and add it to the ray weight
            // map ... also add the phase to the unique phase set

            siteMap = new HashMap<Long, T>();
            aMap.put(phase, siteMap);
            aUniquePhases.add(phase);
        }

        // get the current T object if it exists

        T currObj = siteMap.get(siteid);
        if (currObj == null) {
            // new object ... receiver id to unique site set, increment the phase /
            // site count, add the input object to the list and add its associated
            // phase and receiver id to their lists.

            aList.add(obj);
            aPhaseList.add(phase);
            aSiteIdList.add(siteid);
            aUniqueSites.add(siteid);
            ++aPhaseSiteCount;
        } else {
            // object already exists ... replace existing object with new one for the
            // same phase / site

            int indx = aList.size();
            for (indx = 0; indx < aList.size(); ++indx)
                if (aList.get(indx) == currObj) break;
            aList.set(indx, obj);
        }

        // put the object into the map associated with the input phase and reciever
        // id

        siteMap.put(siteid, obj);
    }

    /**
     * Reads / reinitializes this object from the definition contained in the
     * input file.
     *
     * @param fpth Path to where the new definition for this T object
     *             resides.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    protected void read(T readerObj, String fpth) throws IOException {
        // create the filename and a new file input buffer

        FileInputBuffer fib = new FileInputBuffer(fpth);

        // read the number of phase entries and create a new map of phase
        // associated with a site / T object map ... loop over all phases

        int n = fib.readInt();
        if (aMap == null)
            createStorage(n);
        else
            clear();

        // loop over all entries and read in each new T object

        for (int i = 0; i < n; ++i) {
            // get the phase name and receiver id and read in the
            // associated T object ... add to the maps and lists

            SeismicPhase phase = SeismicPhase.valueOf(fib.readString());
            //fib.readString();
            //SeismicPhase phase  = SeismicPhase.P;

            long siteid = fib.readLong();
            T obj = (T) readerObj.readNew(fib);
            add(phase, siteid, obj);
        }

        // done ... close file and return map

        fib.close();
    }

    /**
     * Writes this T object to the file defined by the input
     * path.
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

        fob.writeInt(aPhaseSiteCount);
        for (Map.Entry<SeismicPhase, HashMap<Long, T>> e : aMap.entrySet()) {
            // get the phase and site map and loop over each unique site for the
            // current phase

            SeismicPhase ph = e.getKey();
            HashMap<Long, T> siteMap = e.getValue();
            for (Map.Entry<Long, T> esite : siteMap.entrySet()) {
                // get the site id and the T object and write the
                // phase, site id, and T object to disk

                long siteid = esite.getKey();
                T obj = esite.getValue();
                fob.writeString(ph.name());
                fob.writeLong(siteid);
                obj.write(fob);
            }
        }

        // done ... close file and return

        fob.close();
    }
}
