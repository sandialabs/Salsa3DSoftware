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
package gov.sandia.gmp.pcalc;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.Schema;


public class DataSinkDB extends DataSink
{
	private Schema outputSchema;

	public DataSinkDB(PCalc pcalc) 
			throws Exception
	{
		super(pcalc);

		outputSchema = new Schema("dbOutput", properties, true);
	}

	@Override
	public void close() throws SQLException
	{
		if (outputSchema != null)
			outputSchema.close();
	}

	@Override
	public void writeData(Bucket bucket) throws Exception
	{
		// definition of the columns in an assoc row:
		//		values[ 0] = new Long(arid);
		//		values[ 1] = new Long(orid);
		//		values[ 2] = sta;
		//		values[ 3] = phase;
		//		values[ 4] = new Double(belief);
		//		values[ 5] = new Double(delta);
		//		values[ 6] = new Double(seaz);
		//		values[ 7] = new Double(esaz);
		//		values[ 8] = new Double(timeres);
		//		values[ 9] = timedef;
		//		values[10] = new Double(azres);
		//		values[11] = azdef;
		//		values[12] = new Double(slores);
		//		values[13] = slodef;
		//		values[14] = new Double(emares);
		//		values[15] = new Double(wgt);
		//		values[16] = vmodel;
		//		values[17] = new Long(commid);
		//		values[18] = new Date();

		if (bucket.predictions.size() > 0)
		{
			double predicted, residual;

			ArrayList<Assoc> rows = new ArrayList<>(bucket.predictions.size());

			for (int i=0; i<bucket.predictions.size(); ++i)
			{
				Prediction p = bucket.predictions.get(i);
				ArrivalInfo arrival = bucket.assocRows.get((int)p.getObservationId());
				Assoc assoc = new Assoc();

				assoc.setArid(arrival.arid);
				assoc.setTimedef(String.valueOf(arrival.timedef));
				assoc.setAzdef(String.valueOf(arrival.azdef));
				assoc.setSlodef(String.valueOf(arrival.slodef));

				assoc.setOrid(p.getSource().getSourceId());
				assoc.setSta(p.getReceiver().getSta());
				assoc.setPhase(p.getPhase().toString());
				assoc.setDelta(p.getSource().distanceDegrees(p.getReceiver()));
				
				assoc.setEsaz(p.getSource().getEsaz(p.getReceiver()));
				assoc.setSeaz(p.getReceiver().getSeaz(p.getSource()));
				
				double precision = 1e6;

				residual = Assoc.TIMERES_NA;
				if (arrival.time != Arrival.TIME_NA)
				{
					predicted = p.getAttribute(GeoAttributes.TRAVEL_TIME);
					if (predicted != Globals.NA_VALUE)
						residual = arrival.time-p.getSource().getOriginTime()-predicted;
				}
				residual = Math.round(residual*precision)/precision;
				assoc.setTimeres(residual);

				residual = Assoc.AZRES_NA;
				if (arrival.azimuth != Arrival.AZIMUTH_NA)
				{
					predicted = p.getAttribute(GeoAttributes.AZIMUTH_DEGREES);
					if (predicted != Globals.NA_VALUE)
					{
						residual = arrival.azimuth-predicted;
						if (residual < -180.)
							residual += 360.;
						else if (residual > 180.)
							residual -= 360.;
					}
				}
				residual = Math.round(residual*precision)/precision;
				assoc.setAzres(residual);

				residual = Assoc.SLORES_NA;
				if (arrival.slow != Arrival.SLOW_NA)
				{
					predicted = p.getAttribute(GeoAttributes.SLOWNESS_DEGREES);
					if (predicted != Globals.NA_VALUE)
						residual = arrival.slow-predicted;
				}
				residual = Math.round(residual*precision)/precision;
				assoc.setSlores(residual);

				assoc.setVmodel(p.getModelName().length() <= 15 ? p.getModelName() 
						: p.getModelName().substring(0,15));

				rows.add(assoc);
			}

			try
			{
				Assoc.write(outputSchema.getConnection(), outputSchema.getTableName("Assoc"), rows, 
						new Timestamp(System.currentTimeMillis()), true);

				if (log.isOutputOn())
					log.writeln(String.format("%d new rows committed to %s",
							rows.size(), outputSchema.getTableName("Assoc")));
			} 
			catch (SQLException e)
			{
				log.writeln(e);
				outputSchema.rollback();

			}
		}

	}

}
