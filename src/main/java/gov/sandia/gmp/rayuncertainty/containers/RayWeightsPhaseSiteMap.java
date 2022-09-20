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

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.rayuncertainty.basecontainers.PhaseSiteMap;

/**
 * Container of Phase / site associated RayWeightsSourceMap objects containing
 * the covariance block row specific ray weights for the set of defined phase /
 * site entries.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class RayWeightsPhaseSiteMap extends PhaseSiteMap<RayWeightsSourceMap> {
    /**
     * Used to create a new RayweightSourceMap object when a new entry is added.
     */
    private RayWeightsSourceMap aNewObj = new RayWeightsSourceMap();

    /**
     * Default constructor.
     */
    public RayWeightsPhaseSiteMap() {
        super();
    }

    /**
     * Standard constructor. Constructs itself from an input file name.
     *
     * @param fpth The path to the new input file.
     * @throws IOException
     */
    public RayWeightsPhaseSiteMap(String fpth) throws IOException {
        read(fpth);
    }

    /**
     * Reads and reassigns it's self to the definition in the file at fpth.
     *
     * @param fpth The path to the file from which it's new definition will be
     *             read.
     */
    @Override
    public void read(String fpth) throws IOException {
        read(aNewObj, fpth);
    }

    /**
     * Retrieves an existing RayWeightsSourceMap, if defined, or sets a new one
     * if not.
     */
    public RayWeightsSourceMap getSet(SeismicPhase phase, long siteid) {
        RayWeightsSourceMap pos = getSet(phase, siteid, aNewObj);
        if (pos == aNewObj) {
            pos.setPhaseAndSiteId(phase, siteid);
            aNewObj = new RayWeightsSourceMap();
        }

        // return existing or new object.

        return pos;
    }
}
