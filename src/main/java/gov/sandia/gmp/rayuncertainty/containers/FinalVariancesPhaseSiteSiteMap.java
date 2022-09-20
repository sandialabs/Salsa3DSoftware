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
import gov.sandia.gmp.rayuncertainty.basecontainers.PhaseSiteSiteMap;

/**
 * Container that associates a returned FinalVariancesSourceMap with a phase /
 * site / site type AB covariance result.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class FinalVariancesPhaseSiteSiteMap
        extends PhaseSiteSiteMap<FinalVariancesSourceMap> {
    /**
     * Used to add a new FinalVariancesSourceMap object to this map.
     */
    private FinalVariancesSourceMap aNewObj = new FinalVariancesSourceMap();

    /**
     * Default constructor.
     */
    public FinalVariancesPhaseSiteSiteMap() {
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
    public FinalVariancesPhaseSiteSiteMap(String fpth) throws IOException {
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
     * Returns the FinalVariancesSourceMap associated with the input phase /
     * site A / site B key. If it does not exist a new one is added.
     *
     * @param phase   The phase of the entry to be returned / added.
     * @param siteAid The receiver A id of the entry to be returned / added.
     * @param siteBid The receiver B id of the entry to be returned / added.
     * @return The FinalVariancesSourceMap associated with the input phase /
     * site A / site B key.
     */
    public FinalVariancesSourceMap getSet(SeismicPhase phase,
                                          long siteAid, long siteBid) {
        // get the associated entry and see if it is new

        FinalVariancesSourceMap fvsm = getSet(phase, siteAid, siteBid, aNewObj);
        if (fvsm == aNewObj) {
            // new assign phase and site and create a new entry for the next add

            fvsm.setPhaseAndSiteId(phase, siteAid);
            aNewObj = new FinalVariancesSourceMap();
        }

        // return the result

        return fvsm;
    }

    /**
     * Adds the contents of the input variance phase site site map to this one.
     *
     * @param fvpssm The variance phase site site map that will be added to this
     *               one.
     */
    public void add(FinalVariancesPhaseSiteSiteMap fvpssm) {
        ArrayList<FinalVariancesSourceMap> fvsmList;
        FinalVariancesSourceMap fvsm, thisFVSM;
        SeismicPhase ph;
        long siteAId, siteBId;

        // get the variance component list from the input variance phase site map
        // and loop over each entry

        fvsmList = fvpssm.getList();
        for (int i = 0; i < fvsmList.size(); ++i) {
            // get the ith variance source map and its phase and site id and get
            // same entry from this variance phase site map

            fvsm = fvsmList.get(i);
            ph = fvsm.getPhase();
            siteAId = fvpssm.getSiteAId(i);
            siteBId = fvpssm.getSiteBId(i);
            thisFVSM = getSet(ph, siteAId, siteBId);

            // add the ith variance source map to thisFVSM

            thisFVSM.add(fvsm);
        }
    }

    /**
     * Returns the total number of source entries in the map.
     *
     * @return The total number of source entries in the map.
     */
    public int getTotalSourceEntryCount() {
        int srcEntryCnt = 0;
        ArrayList<FinalVariancesSourceMap> fvsmList;

        // get the variance source map list and loop over each entry and sum the
        // total number of source entries for the entire map

        fvsmList = getList();
        for (int ik = 0; ik < fvsmList.size(); ++ik)
            srcEntryCnt += fvsmList.get(ik).getTotalRayCount();

        // return result

        return srcEntryCnt;
    }
}
