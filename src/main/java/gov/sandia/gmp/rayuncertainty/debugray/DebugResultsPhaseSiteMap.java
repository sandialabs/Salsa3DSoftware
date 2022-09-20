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
package gov.sandia.gmp.rayuncertainty.debugray;

import java.io.IOException;
import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.rayuncertainty.basecontainers.PhaseSiteMap;


/**
 * Container of DebugResultsSourceMap objects associated with a specific
 * phase / site pair.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class DebugResultsPhaseSiteMap
        extends PhaseSiteMap<DebugResultsSourceMap> {
    /**
     * Used to add a new DebugResultsSourceMap.
     */
    private DebugResultsSourceMap aNewObj = new DebugResultsSourceMap();

    /**
     * Default constructor.
     */
    public DebugResultsPhaseSiteMap() {
        super();
    }

    /**
     * Standard constructor. Builds itself from the input file path.
     *
     * @param fpth The path of the file from which this object will be
     *             constructed.
     * @throws IOException
     */
    public DebugResultsPhaseSiteMap(String fpth) throws IOException {
        read(fpth);
    }

    /**
     * Reads and reassigns itself to the definition at the input file path.
     */
    @Override
    public void read(String fpth) throws IOException {
        read(aNewObj, fpth);
    }

    /**
     * Returns the DebugResultsSourceMap associated with the input phase /
     * site pair, or creates and adds a new one if one does not yet exist.
     */
    public DebugResultsSourceMap getSet(SeismicPhase phase, long siteid) {
        // get the current entry and see if a new one was added

        DebugResultsSourceMap fvsm = getSet(phase, siteid, aNewObj);
        if (fvsm == aNewObj) {
            // a new one was added ... set the phase and site id and create a new
            // one for the next add

            fvsm.setPhaseAndSiteId(phase, siteid);
            aNewObj = new DebugResultsSourceMap();
        }

        // return the associated DebugResultsSourceMap object.

        return fvsm;
    }

    /**
     * Adds the contents of the input DebugResultsPhaseSiteMap to this one.
     *
     * @param drpsm The input DebugResultsPhaseSiteMap.
     */
    public void add(DebugResultsPhaseSiteMap drpsm) {
        // exit if it is null

        if (drpsm != null) {
            // get the list of source maps and loop over each

            ArrayList<DebugResultsSourceMap> drsmList = drpsm.getList();
            for (int i = 0; i < drsmList.size(); ++i) {
                // get the ith source map and the corresponding site id

                DebugResultsSourceMap drsm = drsmList.get(i);
                long sAid = drpsm.getSiteId(i);

                // get the matching source map for this DebugResultsPhaseSiteMap
                // and the list of DebugTaskResults from the input source map ...
                // loop over all entries in the results map and add to this map

                DebugResultsSourceMap thisDRSM = getSet(drsm.getPhase(), sAid);
                ArrayList<DebugTaskResults> dtrList = drsm.getList();
                for (int j = 0; j < dtrList.size(); ++j) {
                    // get the jth results entry and corresponding source id ... find
                    // ths same entry in this one and add the results to this one

                    DebugTaskResults dtr = dtrList.get(j);
                    long sourceId = drsm.getSourceId(j);
                    DebugTaskResults thisDTR = thisDRSM.getSet(sourceId);
                    thisDTR.add(dtr);
                }
            }
        }
    }
}
