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

import java.util.ArrayList;
import java.util.EnumSet;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;

public abstract class DataSink
{
	protected PropertiesPlusGMP properties;
	
	protected ScreenWriterOutput log;
	
	protected ArrayList<GeoAttributes> outputAttributes;
	
	protected EnumSet<GeoAttributes> requestedAttributes;
	
	/**
	 * The record that contains the names of the recognized columns in the input
	 * dataSource.  Recognized columns are site_lat, origin_lon, phase, etc.
	 * Copied from input dataSource.
	 */
	protected String inputHeader;

	protected Application application;
	
	/**
	 * True when there is more data that has not yet been processed.
	 */
	protected boolean moreData;

	protected PredictorFactory predictors;

	public static DataSink getDataSink(PCalc pcalc) 
	throws Exception
	{
      if (pcalc.inputType == IOType.DATABASE)
        return new DataSinkDB(pcalc);
    
      if (pcalc.inputType == IOType.GEOTESS)
        return new DataSinkGeoTess(pcalc);
    
      return new DataSinkFile(pcalc);
	}
	
	public DataSink(PCalc pcalc)
	{
		this.properties = pcalc.properties;
		this.outputAttributes = pcalc.outputAttributes;
		this.requestedAttributes = pcalc.predictionAttributes;
		this.log = pcalc.log;
		
		this.predictors = pcalc.predictors;
		
		this.application = pcalc.application;
		
		//this.graphicsFormat = pcalc.graphicsFormat;
		
		this.inputHeader = pcalc.dataSource.getInputHeader();		
	}
	
	abstract public void writeData(Bucket data) throws Exception;
	
	abstract public void close() throws Exception;

	static public String formatRequest(PredictionRequest request, String separator)
	{
		StringBuffer record = new StringBuffer();
		if (separator.equals(" "))
		{
			if (!request.getReceiver().getSta().equals("-"))
				record.append(String.format("%8s%s%7d%s",
						request.getReceiver().getSta(), separator, 
						GMTFormat.getJDate(request.getSource().getOriginTime()), separator));

			record.append(String.format("%10.6f%s%11.6f%s%7.3f%s%10.6f%s%11.6f%s%7.3f%s%6s",
					request.getReceiver().getLatDegrees(), separator,
					request.getReceiver().getLonDegrees(), separator,
					-request.getReceiver().getDepth(), separator,
					request.getSource().getLatDegrees(), separator,
					request.getSource().getLonDegrees(), separator,
					request.getSource().getDepth(), separator,
					request.getPhase().toString()));
		}
		else
		{
			if (!request.getReceiver().getSta().equals("-"))
				record.append(String.format("%1s%s%1d%s",
						request.getReceiver().getSta(), separator, 
						GMTFormat.getJDate(request.getSource().getOriginTime()), separator));

			record.append(String.format("%1.6f%s%1.6f%s%1.3f%s%1.6f%s%1.6f%s%1.3f%s%1s",
					request.getReceiver().getLatDegrees(), separator,
					request.getReceiver().getLonDegrees(), separator,
					-request.getReceiver().getDepth(), separator,
					request.getSource().getLatDegrees(), separator,
					request.getSource().getLonDegrees(), separator,
					request.getSource().getDepth(), separator,
					request.getPhase().toString()));
		}
		
		return record.toString();
	}
	
}
