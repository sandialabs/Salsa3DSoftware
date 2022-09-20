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
package gov.sandia.gmp.locoo3d;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import gov.sandia.gmp.baseobjects.globals.DBTableTypes;
import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.gmp.Observation;
import gov.sandia.gnem.dbtabledefs.gmp.Prediction;
import gov.sandia.gnem.dbtabledefs.gmp.Source;
import gov.sandia.gnem.dbtabledefs.gmp.Srcobsassoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.ArrivalExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

/**
 * Represents the results of locating a single seismic event.
 * If the result is invalid, then method isValid() returns false
 * getErrorMessage will contain some indication of what went wrong.
 * @author sballar
 *
 */
//@XmlRootElement
public class LocOOResult implements Serializable
{
	private static final long serialVersionUID = -4937797012460979171L;
	
	public OriginExtended originRow;
	
	public Source sourceRow;
	
	public ArrayList<Srcobsassoc> srcobsassocRows;
	
	public ArrayList<Observation> observationRows;
	
	public ArrayList<Prediction> predictionRows;
	
	private EnumSet<DBTableTypes> tableTypes;
	
	/**
	 * Map from arid to weightedResiduals of TT, AZ, SH, in that order.
	 * Value is Globals.NA_VALUE for invalid observations/predictions.
	 * For this to have been computed, property io_nondefining_residuals must
	 * be true, which is the default.
	 */
	private HashMap<Long, double[]> weightedResiduals;
	
	/**
	 * number of sswr function evaluations performed.
	 */
	private int nFunc;
	
	/** 
	 * number of iterations performed
	 */
	private int nIterations;
	
	/**
	 * Time in seconds to compute location
	 */
	private double calculationTime;
	
	private double predictionTime;

	/**
	 * true if a valid location was computed.
	 */
	private boolean valid;
	
	/**
	 * Contains an errorMessage.  isValid() will return false
	 */
	private String errorMessage;
	
	public LocOOResult(int index, Event event, EnumSet<DBTableTypes> tableTypes, HashMap<Long, ArrivalExtended> originalArrivals)
	{
		this.tableTypes = tableTypes;
		
		try
		{
			errorMessage = "";
			
			LocatorResults lr = event.getLocatorResults();
			
			nFunc = lr.getNFunc();
			nIterations = lr.getNIterations();
			calculationTime = event.getCalculationTime();
			predictionTime = event.getPredictionTime();
			
			valid = lr != null && lr.getConverged() && lr.getLocation() != null;
			
			if (valid)
			{
				if (tableTypes.contains(DBTableTypes.ORIGIN))
				{
					originRow = new OriginExtended(lr.getOriginRow());
					
					if(tableTypes.contains(DBTableTypes.ORIGERR))
						originRow.setOrigerr(lr.getOrigerrRow());

					if(tableTypes.contains(DBTableTypes.AZGAP))
						originRow.setAzgap(lr.getAzgapRow());

					if(tableTypes.contains(DBTableTypes.ASSOC))
					{
						for (Arrival arrival : event.getArrivals())
						{
							AssocExtended assoc = arrival.getAssocRow();
							assoc.setArrival(originalArrivals.get(assoc.getArid()));
							originRow.addAssoc(assoc);
						}
					}
				}

				if (tableTypes.contains(DBTableTypes.SOURCE))
					sourceRow = lr.getSourceRow();

				if(tableTypes.contains(DBTableTypes.SRCOBSASSOC))
				{
					srcobsassocRows = new ArrayList<Srcobsassoc>(event.getArrivals().size());
					for (Arrival arrival : event.getArrivals())
						srcobsassocRows.add(arrival.getSrcobsassocRow());
				}

				if(tableTypes.contains(DBTableTypes.OBSERVATION))
				{
					observationRows = new ArrayList<Observation>(event.getArrivals().size());
					for (Arrival arrival : event.getArrivals())
						observationRows.add(arrival.getObservationRow());
				}

				if(tableTypes.contains(DBTableTypes.PREDICTION))
				{
					predictionRows = new ArrayList<Prediction>(event.getArrivals().size());
					for (Arrival arrival : event.getArrivals())
						predictionRows.add(arrival.getPredictionRow());
				}
				
				weightedResiduals = event.getWeightedResiduals();
			}
		}
		catch (Exception e)
		{
			valid = false;
			errorMessage = e.getMessage();
		}
	}
	
	public ArrayList<BaseRow> getBaseRows()
	{
		ArrayList<BaseRow> dbRows = new ArrayList<BaseRow>();
		if (originRow != null)
		{
			dbRows.add(originRow);
			if (originRow.getOrigerr() != null)
				dbRows.add(originRow.getOrigerr());
			if (originRow.getAzgap() != null)
				dbRows.add(originRow.getAzgap());
			if (tableTypes.contains(DBTableTypes.ASSOC))
				dbRows.addAll(originRow.getAssocs().values());
			if (tableTypes.contains(DBTableTypes.ARRIVAL))
				for (AssocExtended assoc : originRow.getAssocs().values())
					if (assoc.getArrival() != null)
						dbRows.add(assoc.getArrival());
		}
		
		
		if (sourceRow != null)
			dbRows.add(sourceRow);
		if (srcobsassocRows != null)
			dbRows.addAll(srcobsassocRows);
		if (observationRows != null)
			dbRows.addAll(observationRows);
		if (predictionRows != null)
			dbRows.addAll(predictionRows);
		return dbRows;
	}
	
	public OriginExtended getOriginRow()
	{
		return originRow;
	}
	
	public Source getSourceRow()
	{
		return sourceRow;
	}
	
	public ArrayList<Srcobsassoc> getSrcObsAssocRows()
	{
		return srcobsassocRows;
	}
	
	public ArrayList<Observation> getObervationRows()
	{
		return observationRows;
	}
	
	public ArrayList<Prediction> getPredictionRows()
	{
		return predictionRows;
	}
	
	public void setOrid(long orid)
	{
		if (originRow != null)
		{
			originRow.setOrid(orid);
			if (originRow.getOrigerr() != null)
				originRow.getOrigerr().setOrid(orid);
			if (originRow.getAzgap() != null)
				originRow.getAzgap().setOrid(orid);
			if (tableTypes.contains(DBTableTypes.ASSOC))
				for (AssocExtended assoc : originRow.getAssocs().values())
					assoc.setOrid(orid);
		}
		
		
		if (sourceRow != null)
			sourceRow.setSourceid(orid);
		if (srcobsassocRows != null)
			for (Srcobsassoc soa : srcobsassocRows)
				soa.setSourceid(orid);
		if (predictionRows != null)
			for (Prediction prediction : predictionRows)
				prediction.setSourceid(orid);
	}
	
	public void setSourceId(long sourceId)
	{
		setOrid(sourceId);
	}

	/**
	 * @return false if an error occurred.  
	 */
	public boolean isValid()
	{
		return valid;
	}
	
	/**
	 * Contains error message if isValid() is false, 
	 * empty String if isValid() is true.
	 * @return
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}

	/**
	 * Map from arid to weightedResiduals of TT, AZ, SH, in that order.
	 * Value is Globals.NA_VALUE for invalid observations/predictions.
	 * For this to have been computed, property io_nondefining_residuals must
	 * be true, which is the default.
	 */
	public HashMap<Long, double[]> getWeightedResiduals() 
	{
		return weightedResiduals;
	}

	public int getnFunc() {
		return nFunc;
	}

	public int getnIterations() {
		return nIterations;
	}

	public double getCalculationTime() {
		return calculationTime;
	}

	public double getPredictionTime() {
		return predictionTime;
	}

}
