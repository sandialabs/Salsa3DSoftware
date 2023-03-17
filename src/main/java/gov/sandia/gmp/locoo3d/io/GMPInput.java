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
package gov.sandia.gmp.locoo3d.io;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gnem.dbtabledefs.gmp.Source;

public class GMPInput extends NativeInput {

    /**
     * Map from receiverid to dbtabledefs.Receiver.
     */
    protected Map<Long, Receiver> receivers = new HashMap<Long, Receiver>();

    /**
     * Map from sourceId to dbtabledefs.Source object, which has references to associated
     * dbtabledefs.Srcobsassoc, dbtabledefs.Observation and dbtabledefs.Receiver.  
     * These sources can be specified in constructors
     * defined below, or can be retrieved from files or databases.
     */
    Map<Long, Source> inputSources;

    //boolean uncertaintyRequested;

    //boolean azgapRequested;

    boolean srcobsassocsRequested;

    boolean observationsRequested;

    public GMPInput() {
	super();
    }

    public GMPInput(PropertiesPlusGMP properties) throws Exception {
	super(properties);
	
	String tableTypes = properties.getProperty("dbOutputTableTypes", " ").toLowerCase();

	srcobsassocsRequested = tableTypes.contains("srcobsassoc") || properties.containsKey("dataLoaderFileOutputSrcobsassocs")
		|| properties.containsKey("dbOutputSrcobsassocTable");

	observationsRequested = tableTypes.contains("observation") || properties.containsKey("dataLoaderFileOutputObservations")
		|| properties.containsKey("dbOutputObservationTable");

	if (observationsRequested)
	    inputSources = new TreeMap<>();

    }

    @Override
    public void close() throws Exception {

    }

}
