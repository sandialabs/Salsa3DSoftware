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
import gov.sandia.gmp.rayuncertainty.basecontainers.PhaseSiteSiteMap;


/**
 * Container that associates a returned DebugResultsSourceMap with a phase /
 * site / site type AB covariance result.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class DebugResultsPhaseSiteSiteMap
        extends PhaseSiteSiteMap<DebugResultsSourceMap> {
    /**
     * Used to add a new FinalVariancesSourceMap object to this map.
     */
    private DebugResultsSourceMap aNewObj = new DebugResultsSourceMap();

    /**
     * Default constructor.
     */
    public DebugResultsPhaseSiteSiteMap() {
        super();
    }

    /**
     * Standard constructor. Constructs itself from the definition of the file at
     * path fpth.
     *
     * @param fpth The path of the file from which this object will be
     *             constructed.
     * @throws IOException
     */
    public DebugResultsPhaseSiteSiteMap(String fpth) throws IOException {
        read(fpth);
    }

    /**
     * Reads and assigns itself to the definition contained in the file at path
     * fpth.
     *
     * @param fpth The path of the file that contains this objects new definition.
     */
    @Override
    public void read(String fpth) throws IOException {
        read(aNewObj, fpth);
    }

    /**
     * Returns the DebugResultsSourceMap associated with the input phase /
     * site A / site B key. If it does not exist a new one is added.
     *
     * @param phase   The phase of the entry to be returned / added.
     * @param siteAid The receiver A id of the entry to be returned / added.
     * @param siteBid The receiver B id of the entry to be returned / added.
     * @return The DebugResultsSourceMap associated with the input phase /
     * site A / site B key.
     */
    public DebugResultsSourceMap getSet(SeismicPhase phase,
                                        long siteAid, long siteBid) {
        // get the associated entry and see if it is new

        DebugResultsSourceMap fvsm = getSet(phase, siteAid, siteBid, aNewObj);
        if (fvsm == aNewObj) {
            // new assign phase and site and create a new entry for the next add

            fvsm.setPhaseAndSiteId(phase, siteAid);
            aNewObj = new DebugResultsSourceMap();
        }

        // return the result

        return fvsm;
    }

    /**
     * Adds the contents of the input DebugResultsPhaseSiteSiteMap to this one.
     *
     * @param drpssm The input DebugResultsPhaseSiteSiteMap.
     */
    public void add(DebugResultsPhaseSiteSiteMap drpssm) {
        // exit if it is null

        if (drpssm != null) {
            // get the list of source maps and loop over each

            ArrayList<DebugResultsSourceMap> drssmList = drpssm.getList();
            for (int i = 0; i < drssmList.size(); ++i) {
                // get the ith source map and the corresponding site ids

                DebugResultsSourceMap drsm = drssmList.get(i);
                long sAid = drpssm.getSiteAId(i);
                long sBid = drpssm.getSiteBId(i);

                // get the matching source map for this DebugResultsPhaseSiteSiteMap
                // and the list of DebugTaskResults from the input source map ...
                // loop over all entries in the results map and add to this map

                DebugResultsSourceMap thisDRSM = getSet(drsm.getPhase(), sAid, sBid);
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
