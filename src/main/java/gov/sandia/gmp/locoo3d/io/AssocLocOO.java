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

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.ArrivalExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

public class AssocLocOO extends AssocExtended {

    private static final long serialVersionUID = 1L;

    private EnumMap<GeoAttributes, Double> predictions;

    public AssocLocOO(OriginExtended origin, ArrivalExtended arrival, String phase) {
	super(origin, arrival, phase);
    }

    public AssocLocOO(long arid, long orid, String sta, String phase, double belief, double delta, double seaz,
	    double esaz, double timeres, String timedef, double azres, String azdef, double slores, String slodef,
	    double emares, double wgt, String vmodel, long commid) {
	super(arid, orid, sta, phase, belief, delta, seaz, esaz, timeres, timedef, azres, azdef, slores, slodef, emares,
		wgt, vmodel, commid);
    }

    public AssocLocOO(AssocExtended other) {
	super(other);
    }

    public AssocLocOO(Assoc other) {
	super(other);
    }

    public AssocLocOO() {
    }

    public AssocLocOO(Scanner input) throws IOException {
	super(input);
    }

    public AssocLocOO(DataInputStream input) throws IOException {
	super(input);
    }

    public AssocLocOO(ByteBuffer input) {
	super(input);
    }

    public AssocLocOO(ResultSet input) throws SQLException {
	super(input);
    }

    public AssocLocOO(ResultSet input, int offset) throws SQLException {
	super(input, offset);
    }

    public EnumMap<GeoAttributes, Double> getPredictions() {
	return predictions;
    }

    public void setPredictions(EnumMap<GeoAttributes, Double> predictions) {
	this.predictions = predictions;
    }
    
    @Override
    public Buff getBuff() {
	      Buff buffer = new Buff(this.getClass().getSimpleName());
	      buffer.insert(super.getBuff());
	      
	      buffer.add("nPredictions", predictions == null ? 0 : 1);
	      if (predictions != null) buffer.add(Prediction.getBuff(predictions, "%g"));

	      buffer.add("nArrivals", arrival == null ? 0 : 1);
	      if (arrival != null) buffer.add(arrival.getBuff());

	      return buffer;
	  }

}
