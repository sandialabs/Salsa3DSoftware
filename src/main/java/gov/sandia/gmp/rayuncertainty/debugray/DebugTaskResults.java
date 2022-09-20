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
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * A debug task result returned to the client for processing. Defines the
 * taskid, hostname, block row and column indices, phase, receiver A and
 * B id (receiver B id may be -1 for type AA rays), and the source id.
 * Additionally, a list of all DebugTaskResultsEntry objects associated with
 * this DebugTaskResults is defined. All entries in the list correspond to
 * the processing of a single block of the covariance matrix.
 *
 * @author jrhipp
 */
public class DebugTaskResults {
    /**
     * The task id of the task that created these debug results.
     */
    private int aTaskId = -1;

    /**
     * The host name of the server within which these results were generated.
     */
    private String aHostName = "";

    /**
     * The block row index used to create these debug results.
     */
    private int aRowBlock = -1;         // aBlockRow

    /**
     * The block column index used to create these debug results.
     */
    private int aColBlock = -1;         // aBlockCol

    /**
     * The seismic phase of all the debug results stored in this object.
     */
    private SeismicPhase aPhase = null;

    /**
     * The receiver A id of all debug results stored in this object.
     */
    private long aRcvrAId = -1;

    /**
     * The receiver B id of all debug results stored in this object. Note that
     * for type AA rays the B receiver is not defined and this id remains -1.
     */
    private long aRcvrBId = -1;

    /**
     * The source id of all debug results stored in this object.
     */
    private long aSrcId = -1;

    /**
     * The array list of all result entries.
     */
    private ArrayList<DebugTaskResultsEntry> aDbgRsltList = null;

    /**
     * Default constructor.
     */
    public DebugTaskResults() {
        aDbgRsltList = new ArrayList<DebugTaskResultsEntry>();
    }

    /**
     * Standard constructor that reads the contents of this results object from
     * the provided file input buffer.
     *
     * @param fib The file input buffer from which this results object will be
     *            read.
     * @throws IOException
     */
    public DebugTaskResults(FileInputBuffer fib) throws IOException {
        read(fib);
    }

    /**
     * Standard constructor that sets this results object with the input type AA
     * ray definition.
     *
     * @param taskid   The task id that processed this result set.
     * @param hostname The host upon which this result set was processed.
     * @param br       The block row index.
     * @param bc       The block column index.
     * @param ph       The seismic phase.
     * @param rcvrAId  The receiver A id.
     * @param srcId    The source id.
     */
    public DebugTaskResults(int taskid, String hostname, int br, int bc,
                            SeismicPhase ph, long rcvrAId, long srcId) {
        aDbgRsltList = new ArrayList<DebugTaskResultsEntry>();
        set(taskid, hostname, br, bc, ph, rcvrAId, srcId);
    }

    /**
     * Standard constructor that sets this results object with the input type AB
     *
     * @param taskid   The task id that processed this result set.
     * @param hostname The host upon which this result set was processed.
     * @param br       The block row index.
     * @param bc       The block column index.
     * @param ph       The seismic phase.
     * @param rcvrAId  The receiver A id.
     * @param rcvrBId  The receiver B id.
     * @param srcId    The source id.
     */
    public DebugTaskResults(int taskid, String hostname, int br, int bc,
                            SeismicPhase ph, long rcvrAId, long rcvrBId,
                            long srcId) {
        aDbgRsltList = new ArrayList<DebugTaskResultsEntry>();
        set(taskid, hostname, br, bc, ph, rcvrAId, rcvrBId, srcId);
    }

    /**
     * Sets this results object with the input type AA ray definition.
     *
     * @param taskid   The task id that processed this result set.
     * @param hostname The host upon which this result set was processed.
     * @param br       The block row index.
     * @param bc       The block column index.
     * @param ph       The seismic phase.
     * @param rcvrAId  The receiver A id.
     * @param srcId    The source id.
     */
    public void set(int taskid, String hostname, int br, int bc,
                    SeismicPhase ph, long rcvrAId, long srcId) {
        aTaskId = taskid;
        aHostName = hostname;
        aRowBlock = br;
        aColBlock = bc;

        aPhase = ph;
        aRcvrAId = rcvrAId;
        aSrcId = srcId;
    }

    /**
     * Sets this results object with the input type AB ray definition.
     *
     * @param taskid   The task id that processed this result set.
     * @param hostname The host upon which this result set was processed.
     * @param br       The block row index.
     * @param bc       The block column index.
     * @param ph       The seismic phase.
     * @param rcvrAId  The receiver A id.
     * @param rcvrBId  The receiver B id.
     * @param srcId    The source id.
     */
    public void set(int taskid, String hostname, int br, int bc,
                    SeismicPhase ph, long rcvrAId, long rcvrBId, long srcId) {
        set(taskid, hostname, br, bc, ph, rcvrAId, srcId);
        aRcvrBId = rcvrBId;
    }

    /**
     * Returns the entry count stored in this results object.
     *
     * @return The entry count stored in this results object.
     */
    public int size() {
        return aDbgRsltList.size();
    }

    /**
     * Returns the ith entry.
     *
     * @param i The index of the entry to be returned.
     * @return The ith entry.
     */
    public DebugTaskResultsEntry get(int i) {
        return aDbgRsltList.get(i);
    }

    /**
     * Returns the list of entries.
     *
     * @return The list of entries.
     */
    public ArrayList<DebugTaskResultsEntry> getList() {
        return aDbgRsltList;
    }

    /**
     * Adds a new entry.
     *
     * @param dtre The new entry to be added.
     */
    public void add(DebugTaskResultsEntry dtre) {
        aDbgRsltList.add(dtre);
    }

    /**
     * Adds all of the result entries from the input DebugTaskResults object
     * (dtr) into this one.
     *
     * @param dtr The input DebugTaskResults object whose entries will be added
     *            into this one.
     */
    public void add(DebugTaskResults dtr) {
        for (DebugTaskResultsEntry dtre : dtr.aDbgRsltList)
            aDbgRsltList.add(dtre);
    }

    /**
     * Returns the task id for which these results were processed.
     *
     * @return The task id for which these results were processed.
     */
    public int getTaskId() {
        return aTaskId;
    }

    /**
     * Returns the host name for which these results were processed.
     *
     * @return The host name for which these results were processed.
     */
    public String getHostName() {
        return aHostName;
    }

    /**
     * Returns the block row index from which these results were accumulated.
     *
     * @return The block row index from which these results were accumulated.
     */
    public int getRowBlock() {
        return aRowBlock;
    }

    /**
     * Returns the block column index from which these results were accumulated.
     *
     * @return The block column index from which these results were accumulated.
     */
    public int getColBlock() {
        return aColBlock;
    }

    /**
     * Returns the seismic phase that defines this debug task result.
     *
     * @return The seismic phase id that defines this debug task result.
     */
    public SeismicPhase getPhase() {
        return aPhase;
    }

    /**
     * Returns the receiver A id that defines this debug task result.
     *
     * @return The receiver A id that defines this debug task result.
     */
    public long getReceiverAId() {
        return aRcvrAId;
    }

    /**
     * Returns the receiver B id that defines this debug task result.
     * (if not defined -1 is returned).
     *
     * @return The receiver B id that defines this debug task result.
     */
    public long getReceiverBId() {
        return aRcvrBId;
    }

    /**
     * Returns the source id that defines this debug task result.
     *
     * @return The source id that defines this debug task result.
     */
    public long getSourceId() {
        return aSrcId;
    }

    /**
     * Reads this result from the input file input buffer.
     *
     * @param fib The file input buffer from which this result is read.
     * @throws IOException
     */
    public void read(FileInputBuffer fib) throws IOException {
        aTaskId = fib.readInt();
        aHostName = fib.readString();
        aRowBlock = fib.readInt();
        aColBlock = fib.readInt();

        int n = fib.readInt();
        aDbgRsltList = new ArrayList<DebugTaskResultsEntry>();
        for (int i = 0; i < n; ++i)
            aDbgRsltList.add(new DebugTaskResultsEntry(fib));
    }

    /**
     * Writes this result to the input file output buffer.
     *
     * @param fob The file output buffer into which this result is written.
     * @throws IOException
     */
    public void write(FileOutputBuffer fob) throws IOException {
        fob.writeInt(aTaskId);
        fob.writeString(aHostName);
        fob.writeInt(aRowBlock);
        fob.writeInt(aColBlock);

        fob.writeInt(aDbgRsltList.size());
        for (int i = 0; i < aDbgRsltList.size(); ++i)
            aDbgRsltList.get(i).write(fob);
    }
}
