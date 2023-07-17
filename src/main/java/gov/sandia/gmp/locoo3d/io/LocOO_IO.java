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

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

/**
 * DataIO manages a NativeInput class and a NativeOutput class.
 * @author sballar
 *
 */
public class LocOO_IO {

    private NativeInput dataInput;

    private NativeOutput dataOutput;
    
    /**
     * Constructs new NativeInput and NativeOutput classes based on properties specified in
     * the properties file.  The following properties control the types of dataInput and
     * dataOutput classes created:
     * <ul>
     * <li>dataLoaderInputType must be one of [ file, database, application ]
     * <li>dataLoaderInputFormat must be one of [ kb, gmp, native ]
     * <li>dataLoaderOutputType must be one of [ file, database, application ]
     * <li>dataLoaderOutputFormat must be one of [ kb, gmp, native ]
     * </ul>
     * 
     * @param properties
     * @throws Exception
     */
    public LocOO_IO(PropertiesPlusGMP properties) throws Exception {
	dataInput = NativeInput.create(properties);
	dataOutput = NativeOutput.create(properties, dataInput);
    }

    public NativeInput getDataInput() { return dataInput; }

    public NativeOutput getDataOutput() { return dataOutput; }

    public LocOO_IO setInputSources(Collection<Source> inputSources) 
    { dataInput.setSources(inputSources); return this; }

    public Map<Long, Source> getOutputSources() { return dataOutput.getOutputSources(); }

    public LocOO_IO setInputOrigins(Collection<OriginExtended> inputOrigins) throws Exception { 
	if (!(dataInput instanceof KBInput))
	    throw new Exception("Cannot setInputOrigins() because DataInput is not an instance of DataInputKB");
	((KBInput)dataInput).setInputOrigins(inputOrigins); 
	return this;
    }

    public Map<Long, OriginExtended> getOutputOrigins() throws Exception { 
	if (!(dataOutput instanceof KBOutput))
	    throw new Exception("Cannot getOutputOrigins() because DataOutput is not an instance of DataOutputKB");
	return ((KBOutput)dataOutput).getOutputOrigins();
    }

    public ScreenWriterOutput getLogger() { return dataInput.getLogger(); }

    public ScreenWriterOutput getErrorlog() { return dataInput.getErrorlog(); }

    public Buff getBuff() { return dataOutput.getBuff(); }

    public void close() throws Exception {
	dataInput.close();
	if (dataOutput != null) 
	    dataOutput.close();
    }

    public static Buff getBuff(File f) throws Exception {
	if (!f.exists()) return new Buff("LocOO_IO");
	return NativeOutput.getBuff(new Scanner(f));
    }

}
