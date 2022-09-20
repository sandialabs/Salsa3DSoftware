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

import java.io.Serializable;
import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.containers.hash.maps.HashMapLongKey;

/**
 * A container associating source id with parameterized T type objects. This
 * map extends PhaseSiteBase objects which contain a phase and receiver id that
 * uniquely identifies this map for some phase / site pair. For RayUncertainty
 * the T type objects can be Observation, SparseMatrixVector, or
 * PartialVariance.
 *
 * @param <T>
 * @author jrhipp
 */
@SuppressWarnings("serial")
public abstract class SourceMap<T> extends PhaseSiteBase implements Serializable {
    /**
     * List of all T objects stored in the map.
     */
    private ArrayList<T> aList = null;

    /**
     * List of all source id's corresponding to T objects in aList above.
     */
    private ArrayListLong aSrcList = null;

    /**
     * Map containing source id -> T associations
     */
    private HashMapLongKey<T> aMap = null;

    /**
     * Map iterator used by the getNext() function
     * to iterate over the map.
     */
    private HashMapLongKey<T>.Iterator itRay = null;

    /**
     * Map entry containing the current entry in the map when
     * iterated over in function getNext().
     */
    private HashMapLongKey.Entry<T> eRay = null;

    /**
     * Current value retrieved from the map during iterated calls
     * using function getNext().
     */
    private T val = null;

    /**
     * Default constructor.
     */
    public SourceMap() {
        super(null, -1);
        newMap(0);
    }

    /**
     * Standard constructor.
     */
    public SourceMap(SeismicPhase ph, long siteid) {
        super(ph, siteid);
        newMap(0);
    }

    /**
     * Standard Constructor.
     *
     * @param n Initial capacity.
     */
    public SourceMap(int n) {
        super(null, -1);
        newMap(n);
    }

    /**
     * Creates a new map with the input initial capacity (default if 0).
     *
     * @param n Initial capacity.
     */
    protected void newMap(int n) {
        if (n == 0) {
            aMap = new HashMapLongKey<T>();
            aList = new ArrayList<T>();
            aSrcList = new ArrayListLong();
        } else {
            aMap = new HashMapLongKey<T>(n);
            aList = new ArrayList<T>(n);
            aSrcList = new ArrayListLong(n);

        }
    }

    /**
     * Returns the element associated with the input source id, if it exists in
     * the map, or adds and returns the input element (newElement) if the entry
     * does not exist.
     *
     * @param srcid      The input source id.
     * @param newElement The input element to be added if one does not exist
     *                   associated with the input source id.
     * @return The element associated with the input source id, or the
     * input element if srcid is not yet present in the
     * map.
     */
    public T getSet(long srcid, T newElement) {
        // get the ray entry associated with source id and see if it is defined

        eRay = aMap.getEntry(srcid);
        if (eRay == null) {
            // not defined ... assign val to input element and add it to the map
            // associated with srcid and to the list ... also add the source id to
            // the source list.

            val = newElement;
            aMap.put(srcid, val);
            aList.add(val);
            aSrcList.add(srcid);
        } else
            // defined ... get value and assign to val

            val = eRay.getValue();

        // return val

        return val;
    }

    /**
     * Returns the element associated with the input source id (srcid) if
     * defined. Otherwise, null is returned.
     *
     * @param srcid The source id for which the associated element will be
     *              returned.
     * @return The element associated with the input source id (srcid) if
     * defined. Otherwise, null is returned.
     */
    public T get(long srcid) {
        eRay = aMap.getEntry(srcid);
        if (eRay == null)
            return null;
        else
            return eRay.getValue();
    }

    /**
     * Returns each element in the map in succession until no others remain,
     * after which null is returned. To restart the iteration call function
     * resetIterator(). Functions getCurrentSourceId() can be called to
     * retrieve the current source id associated with the last getNext()
     * call.
     *
     * @return The next element in the map. Null if none remain.
     */
    public T getNext() {
        // see if the column iterator is defined

        if (itRay != null) {
            // column iterator is defined  ... get next column entry (eCol) and see
            // if it is defined

            if (itRay.hasNext()) {
                // have a new eRay ... return eRay.getValue()

                eRay = itRay.nextEntry();
                return eRay.getValue();
            } else {
                // eRay is not defined ... so nothing is left ... return null

                eRay = null;
                return null;
            }
        } else if (aMap.isEmpty()) {
            // no entries ... return null

            return null;
        } else {
            // iterator not defined ... get iterator and return first element

            itRay = aMap.iterator();
            eRay = itRay.nextEntry();
            return eRay.getValue();
        }
    }

    /**
     * Resets the iterator reinitializing returns from function getNext()
     * to the beginning of the map.
     */
    public void resetIterator() {
        itRay = null;
    }

    /**
     * Returns the current source id associated with the last element returned
     * from the function getNext().
     *
     * @return The current source id associated with the last element returned
     * from the function getNext().
     */
    public long getCurrentSourceId() {
        if (eRay == null)
            return Long.MIN_VALUE;
        else
            return eRay.getKey();
    }

    /**
     * Empties the map and list, and resets to initial conditions.
     */
    public void clear() {
        aMap.clear();
        aList.clear();
        aSrcList.clear();

        itRay = null;
        eRay = null;
        val = null;
    }

    /**
     * Returns true if the map is empty.
     *
     * @return True if the map is empty.
     */
    public boolean isEmpty() {
        return aMap.isEmpty();
    }

    /**
     * Returns the number of the source id entries in the map (same as getRayIndexCount()).
     *
     * @return The number of the source id entries in the map (same as getRayIndexCount()).
     */
    public int size() {
        return aMap.size();
    }

    /**
     * Returns the list of Source objects.
     *
     * @return The list of Source objects.
     */
    public ArrayList<T> getList() {
        return aList;
    }

    /**
     * Returns the source id associated with the ith entry in aList.
     *
     * @param i The index of the entry in aList whose associated source id will be
     *          returned
     * @return The source id associated with the ith entry in aList.
     */
    public long getSourceId(int i) {
        return aSrcList.get(i);
    }

    /**
     * Returns an estimate of the amount of memory used by this ray map. The
     * memory estimate does not include the memory used by the T objects
     * stored by the map. Only the storage structure is estimated. One must loop
     * over the entries stored within exterior to this object to determine their
     * size.
     *
     * @return The estimated memory used by this RayMap container.
     */
    public long getMemory() {
        return aMap.memoryEstimate(8) + 16 * aList.size() + 36;
    }
}
