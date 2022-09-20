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

import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * A specific debug task results entry set within the functions
 * buildRayUncertaintyAA and/or buildRayUncertaintyAB of object
 * RayUncertaintyTask. This object is returned to the client so that the
 * specifics of the ray uncetainty calculation can be output as debug
 * information.
 *
 * @author jrhipp
 */
public class DebugTaskResultsEntry {
    /**
     * The row index within a block.
     */
    private int aRowIndex = -1;         // i

    /**
     * The column index within a block.
     */
    private int aColIndex = -1;         // j

    /**
     * The matrix row index.
     */
    private int aRowMtrxIndex = -1;         // ii

    /**
     * The matrix column index.
     */
    private int aColMtrxIndex = -1;         // ij

    /**
     * The weight of the ray associated with index i (ii) above.
     */
    private double aRowWeight = -1.0;       // wi

    /**
     * The weight of the ray associated with index j (ij) above.
     */
    private double aColWeight = -1.0;       // wj

    /**
     * The row index of the block within which aRowIndex is defined.
     */
    private int aRowBlockRow = -1;         // ii-rowStrt

    /**
     * The column index of the block within which aColIndex is defined.
     */
    private int aColBlockCol = -1;         // ij-colStrt

    /**
     * The covariance of the matrix element at ii, ij.
     */
    private double aCovariance = Double.NaN; // cij

    /**
     * The process entry (counter) for this entry.
     */
    private int aPVEntry = -1;         // pv.getCount();

    /**
     * Default constructor.
     */
    public DebugTaskResultsEntry() {
        // no code
    }

    /**
     * Standard constructor that reads this entry from the input file input
     * buffer.
     *
     * @param fib The file input buffer from which this entry will be read.
     * @throws IOException
     */
    public DebugTaskResultsEntry(FileInputBuffer fib) throws IOException {
        read(fib);
    }

    /**
     * Standard constructor that sets the entry.
     *
     * @param i       The block row index.
     * @param j       The block column index.
     * @param ii      The matrix row index.
     * @param ij      The matrix column index.
     * @param wi      The ray weight at index i (ii).
     * @param wj      The ray weight at index j (ij).
     * @param brstrt  The matrix index of the blocks first row.
     * @param bcstrt  The matrix index of the blocks first column.
     * @param cov     The covariance at i,j (ii, ij).
     * @param pventry The entry count (counter) of this entry.
     */
    public DebugTaskResultsEntry(int i, int j, int ii, int ij,
                                 double wi, double wj, int brstrt, int bcstrt,
                                 double cov, int pventry) {
        set(i, j, ii, ij, wi, wj, brstrt, bcstrt, cov, pventry);
    }

    /**
     * Sets this entry to the following inputs.
     *
     * @param i       The block row index.
     * @param j       The block column index.
     * @param ii      The matrix row index.
     * @param ij      The matrix column index.
     * @param wi      The ray weight at index i (ii).
     * @param wj      The ray weight at index j (ij).
     * @param brstrt  The matrix index of the blocks first row.
     * @param bcstrt  The matrix index of the blocks first column.
     * @param cov     The covariance at i,j (ii, ij).
     * @param pventry The entry count (counter) of this entry.
     */
    public void set(int i, int j, int ii, int ij, double wi, double wj,
                    int brstrt, int bcstrt, double cov, int pventry) {
        aRowIndex = i;
        aColIndex = j;
        aRowMtrxIndex = ii;
        aColMtrxIndex = ij;
        aRowWeight = wi;
        aColWeight = wj;
        aRowBlockRow = brstrt;
        aColBlockCol = bcstrt;
        aCovariance = cov;
        aPVEntry = pventry;
    }

    /**
     * Returns the block row index.
     *
     * @return The block row index.
     */
    public int getRowIndex() {
        return aRowIndex;
    }

    /**
     * Returns the block column index.
     *
     * @return The block column index.
     */
    public int getColIndex() {
        return aColIndex;
    }

    /**
     * Returns the matrix row index.
     *
     * @return The matrix row index.
     */
    public int getRowMatrixIndex() {
        return aRowMtrxIndex;
    }

    /**
     * Returns the matrix column index.
     *
     * @return The matrix column index.
     */
    public int getColMatrixIndex() {
        return aColMtrxIndex;
    }

    /**
     * Returns the ray weight at the matrix row index.
     *
     * @return The ray weight at the matrix row index.
     */
    public double getRowWeight() {
        return aRowWeight;
    }

    /**
     * Returns the ray weight at the matrix column index.
     *
     * @return The ray weight at the matrix column index.
     */
    public double getColWeight() {
        return aColWeight;
    }

    /**
     * Returns the block row index.
     *
     * @return The block row index.
     */
    public int getRowBlockRow() {
        return aRowBlockRow;
    }

    /**
     * Returns the block column index.
     *
     * @return The block column index.
     */
    public int getColBlockCol() {
        return aColBlockCol;
    }

    /**
     * Returns the covariance at the matrix row/column index.
     *
     * @return
     */
    public double getCovariance() {
        return aCovariance;
    }

    /**
     * Returns the entry count of this entry.
     *
     * @return The entry count of this entry.
     */
    public int getPartialVarianceEntry() {
        return aPVEntry;
    }

    /**
     * Reads this entry from the input file input buffer.
     *
     * @param fib The file input buffer from which this entry is read.
     * @throws IOException
     */
    public void read(FileInputBuffer fib) throws IOException {
        aRowIndex = fib.readInt();
        aColIndex = fib.readInt();
        aRowMtrxIndex = fib.readInt();
        aColMtrxIndex = fib.readInt();
        aRowWeight = fib.readDouble();
        aColWeight = fib.readDouble();
        aRowBlockRow = fib.readInt();
        aColBlockCol = fib.readInt();
        aCovariance = fib.readDouble();
        aPVEntry = fib.readInt();
    }

    /**
     * Writes this entry to the input file output buffer.
     *
     * @param fob The file output buffer into which this entry is written.
     * @throws IOException
     */
    public void write(FileOutputBuffer fob) throws IOException {
        fob.writeInt(aRowIndex);
        fob.writeInt(aColIndex);
        fob.writeInt(aRowMtrxIndex);
        fob.writeInt(aColMtrxIndex);
        fob.writeDouble(aRowWeight);
        fob.writeDouble(aColWeight);
        fob.writeInt(aRowBlockRow);
        fob.writeInt(aColBlockCol);
        fob.writeDouble(aCovariance);
        fob.writeInt(aPVEntry);
    }
}
