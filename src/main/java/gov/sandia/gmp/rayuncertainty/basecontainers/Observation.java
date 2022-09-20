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

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * Simple observation container that holds
 * 1) The observation Id (from a database this is an ARID. From a GeoModel
 * source or the properties file this is the same as the observation index.
 * 2) The observation index (0 to number of observations - 1).
 * 3) The observation ray index (0 to number of observations -1 for the
 * unique pair of phase / receiver).
 * 4) The observation phase.
 * 5) The observation source id.
 * 6) The observation receiver id.
 * 7) An array of uncertainty components (Diagonal, Off-Diagonal, and
 * Non-Represented).
 * <p>
 * The observation can write itself to a FileOutputBuffer and read itself from
 * a FileInputBuffer.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class Observation implements Serializable {
    /**
     * Observation Id. If -1 it is assigned to the observation index.
     */
    private long aObsId = -1;

    /**
     * Observation index. 0 to number of observations -1.
     */
    private int aObsIndx = -1;

    /**
     * Ray index. For each unique phase/receiver pair defined as 0 to number of
     * observations - 1 defined in the RayUncertainty phase/receiver pairs list.
     */
    private int aRayIndx = -1;

    /**
     * Observation phase.
     */
    private SeismicPhase aPhase = null;

    /**
     * Observation receiver id.
     */
    private long aObsRcvrId = -1;

    /**
     * Observation source id.
     */
    private long aObsSrcId = -1;

    /**
     * Standard constructor. Reads object from the supplied FileInputBuffer.
     *
     * @param fib The FileInputBuffer from which this object is read.
     * @throws IOException
     */
    public Observation(FileInputBuffer fib) throws IOException {
        read(fib);
    }

    /**
     * Standard constructor.
     *
     * @param obsid   Observation id.
     * @param obsindx Observation index.
     * @param rayindx Observation ray index.
     * @param ph      Observation phase.
     * @param rcvrid  Observation receiver id.
     * @param srcid   Observation source id.
     */
    public Observation(long obsid, int obsindx, int rayindx, SeismicPhase ph,
                       long rcvrid, long srcid) {
        // if Observation id is -1 then assign observation index as observation id.

        aObsId = obsid;
        if (aObsId == -1) aObsId = obsindx;

        // assign other inputs and create storage for uncertainty

        aObsIndx = obsindx;
        aRayIndx = rayindx;
        aPhase = ph;
        aObsRcvrId = rcvrid;
        aObsSrcId = srcid;
    }

    /**
     * Writes this observation out to the input FileOutputBuffer.
     *
     * @param fob The FileOutputBuffer into which this observations will be
     *            written.
     * @throws IOException
     */
    public void write(FileOutputBuffer fob) throws IOException {
        // write primary information

        fob.writeLong(aObsId);
        fob.writeInt(aObsIndx);
        fob.writeInt(aRayIndx);
        fob.writeString(aPhase.name());
        fob.writeLong(aObsRcvrId);
        fob.writeLong(aObsSrcId);
    }

    /**
     * Reads the contents of this Observation from the supplied FileInputBuffer.
     *
     * @param fib The FileInputBuffer from which this observation will be read.
     * @throws IOException
     */
    public void read(FileInputBuffer fib) throws IOException {
        // read primary information

        aObsId = fib.readLong();
        aObsIndx = fib.readInt();
        aRayIndx = fib.readInt();
        aPhase = SeismicPhase.valueOf(fib.readString());
        aObsRcvrId = fib.readLong();
        aObsSrcId = fib.readLong();
    }

    /**
     * Get observation id.
     *
     * @return Observation id.
     */
    public long getId() {
        return aObsId;
    }

    /**
     * Get observation index.
     *
     * @return Observation index.
     */
    public int getIndex() {
        return aObsIndx;
    }

    /**
     * Get observation ray index.
     *
     * @return Observation ray index.
     */
    public int getRayIndex() {
        return aRayIndx;
    }

    /**
     * Get observation phase.
     *
     * @return Observation phase.
     */
    public SeismicPhase getPhase() {
        return aPhase;
    }

    /**
     * Get observation source id.
     *
     * @return Observation source id.
     */
    public long getSourceId() {
        return aObsSrcId;
    }

    /**
     * Get observation receiver id.
     *
     * @return Observation receiver id.
     */
    public long getReceiverId() {
        return aObsRcvrId;
    }
}
