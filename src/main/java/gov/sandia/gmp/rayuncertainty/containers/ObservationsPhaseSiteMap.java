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
package gov.sandia.gmp.rayuncertainty.containers;

import java.io.IOException;
import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.rayuncertainty.basecontainers.PhaseSiteMap;

/**
 * A container for all ObservationSourceMap objects associated with a specific
 * phase / site pair.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class ObservationsPhaseSiteMap extends PhaseSiteMap<ObservationsSourceMap> {
    /**
     * Used to add a new ObservationSourceMap.
     */
    private ObservationsSourceMap aNewObj = new ObservationsSourceMap();

    /**
     * The total number of rays (observations) assigned to this map.
     */
    private int aTotalRayCount = 0;

    /**
     * The maximum number of rays in any block assigned to this map.
     */
    private int aMaxBlkRayCount = 0;

    /**
     * The total number of ray elements (weights) assigned in this map.
     */
    private long aTotalElementCount = 0;

    /**
     * The maximum number of ray weights in any block assigned in this map.
     */
    private long aMaxBlkElementCount = 0;

    /**
     * Default constructor.
     */
    public ObservationsPhaseSiteMap() {
        super();
    }

    /**
     * Standard constructor reads this map from the input file path.
     *
     * @param fpth The input file path from which this map will be read.
     * @throws IOException
     */
    public ObservationsPhaseSiteMap(String fpth) throws IOException {
        read(fpth);
    }

    /**
     * Reads and instantiates this map from the file at the input file path
     *
     * @param fpth The input file path from which this map will be read.
     *             throws IOException
     */
    @Override
    public void read(String fpth) throws IOException {
        read(aNewObj, fpth);
    }

    /**
     * Returns the observation source map associated with the input phase and site
     * id. If the map is not found one is created and assigned before being
     * returned to the caller.
     *
     * @param phase  The seismic phase associated with the returned source map.
     * @param siteid The receiver id associated with the returned source map.
     * @return The observation source map associated with the input phase and site
     * id.
     */
    @Override
    public ObservationsSourceMap getSet(SeismicPhase phase, long siteid) {
        ObservationsSourceMap pos = getSet(phase, siteid, aNewObj);
        if (pos == aNewObj) {
            pos.setPhaseAndSiteId(phase, siteid);
            aNewObj = new ObservationsSourceMap();
        }

        return pos;
    }

    /**
     * Returns the total number of rays associated with this map.
     *
     * @return The total number of rays associated with this map.
     */
    public int getTotalRayCount() {
        if ((aTotalRayCount == 0) && (getPhaseSiteCount() > 0)) setCounts();
        return aTotalRayCount;
    }

    /**
     * Returns the maximum number of rays in any block assigned in this map.
     *
     * @return The maximum number of rays in any block assigned in this map.
     */
    public int getMaxBlockRayCount() {
        if ((aMaxBlkRayCount == 0) && (getPhaseSiteCount() > 0)) setCounts();
        return aMaxBlkRayCount;
    }

    /**
     * Returns the total number of ray weights stored in this map.
     *
     * @return The total number of ray weights stored in this map.
     */
    public long getTotalElementCount() {
        if ((aTotalElementCount == 0) && (getPhaseSiteCount() > 0)) setCounts();
        return aTotalElementCount;
    }

    /**
     * Returns the maximum number of ray weights in any block assigned to this
     * map.
     *
     * @return The maximum number of ray weights in any block assigned to this
     * map.
     */
    public long getMaxBlockElementCount() {
        if ((aMaxBlkElementCount == 0) && (getPhaseSiteCount() > 0)) setCounts();
        return aMaxBlkElementCount;
    }

    /**
     * Decrements all ObservationSourceMap reference counts.
     */
    public void decrementBlockWriteReferenceCount() {
        ArrayList<ObservationsSourceMap> obsList = getList();
        for (int i = 0; i < obsList.size(); ++i)
            obsList.get(i).decrementBlockWriteReference();
    }

    /**
     * Sets the total ray/element count and the maximum ray/element count found
     * in any block.
     */
    private void setCounts() {
        // loop over each source map

        ArrayList<ObservationsSourceMap> obsList = getList();
        for (int i = 0; i < obsList.size(); ++i) {
            // get the next source map

            ObservationsSourceMap pos = obsList.get(i);

            // increement total ray/element counts and max ray/element counts

            aTotalRayCount += pos.getRayCount();
            aTotalElementCount += pos.getTotalBlockElementCount();
            int maxBlkRays = pos.getMaxBlockRayCount();
            if (aMaxBlkRayCount < maxBlkRays) aMaxBlkRayCount = maxBlkRays;
            long maxBlkElems = pos.getMaxBlockElementCount();
            if (aMaxBlkElementCount < maxBlkElems) aMaxBlkElementCount = maxBlkElems;
        }
    }
}
