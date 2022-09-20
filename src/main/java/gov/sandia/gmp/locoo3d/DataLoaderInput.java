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

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.TreeSet;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

public interface DataLoaderInput
{

    ArrayList<ArrayListLong> readTaskSourceIds() throws SQLException, GMPException;

    LocOOTask readTaskObservations(ArrayListLong orids) throws Exception;

    void close() throws Exception;

    /**
     * Factory method to return a concrete DataLoader based on the properties
     * file setting "dataLoaderType". Current valid types include "file" and
     * "oracle".
     * 
     * @param properties Input LocOO3D Properties object.
     * @param errorlog 
     * @param logger 
     * @return The new concrete DataLoader.
     * @throws Exception 
     */
    public static DataLoaderInput create(PropertiesPlusGMP properties, ScreenWriterOutput logger, 
	    ScreenWriterOutput errorlog) throws Exception
    {
	String dataTypeProperty = properties.getProperty("dataLoaderInputType", 
		properties.getProperty("dataLoaderType", "not specified"));
	if (dataTypeProperty.toUpperCase().equals("FILE"))
	    return new DataLoaderInputFile(properties, logger, errorlog);
	else if (dataTypeProperty.toUpperCase().equals("ORACLE"))
	    return new DataLoaderInputOracle(properties, logger, errorlog);
	else
	    throw new IOException("Error: Property \"dataLoaderInputType = " +
		    dataTypeProperty + "\" must be one of [ file, oracle ]");
    }

    public static HashMap<String, double[]> getMasterEventCorrections(OriginExtended masterEvent, PredictorFactory predictors, 
	    ScreenWriterOutput logger, String loggerHeader) throws Exception
    {
	HashMap<String, double[]> masterEventCorrections =
		new HashMap<String, double[]>(masterEvent.getAssocs().size());

		PredictionRequest request = new PredictionRequest();
		request.setDefining(true);
		request.setSource(new Source(masterEvent));
		request.setRequestedAttributes(
			EnumSet.of(GeoAttributes.TRAVEL_TIME, GeoAttributes.AZIMUTH_DEGREES, GeoAttributes.SLOWNESS_DEGREES));

		double TIME_NA = gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival.TIME_NA;
		double AZIMUTH_NA = gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival.AZIMUTH_NA;
		double SLOW_NA = gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival.SLOW_NA;

		for (AssocExtended assoc : masterEvent.getAssocs().values())
		{
		    if (assoc.isTimedef() || assoc.isAzdef() || assoc.isSlodef())
		    {
			SeismicPhase phase = SeismicPhase.valueOf(assoc.getPhase());
			Predictor predictor = predictors.getPredictor(phase);
			if (predictor != null)
			{
			    request.setReceiver(new Receiver(assoc.getSite()));
			    request.setPhase(phase);

			    Prediction prediction = predictor.getPrediction(request);
			    if (prediction.isValid())
			    {
				double[] corr = new double[] {Assoc.TIMERES_NA, Assoc.AZRES_NA, Assoc.SLORES_NA};

				if (assoc.isTimedef() && assoc.getArrival().getTime() != TIME_NA
					&& prediction.getAttribute(GeoAttributes.TRAVEL_TIME) != Globals.NA_VALUE)
				    corr[0] = assoc.getArrival().getTime()-masterEvent.getTime()-
				    prediction.getAttribute(GeoAttributes.TRAVEL_TIME);

				if (assoc.isAzdef() && assoc.getArrival().getAzimuth() != AZIMUTH_NA
					&& prediction.getAttribute(GeoAttributes.AZIMUTH_DEGREES) != AZIMUTH_NA)
				{
				    corr[1] = Math.toRadians(assoc.getArrival().getAzimuth()
					    -prediction.getAttribute(GeoAttributes.AZIMUTH_DEGREES));
				    if (corr[1] < -Math.PI) 
					corr[1] += 2*Math.PI;
				    else if (corr[1] > Math.PI) 
					corr[1] -= 2*Math.PI;
				}

				if (assoc.isSlodef() && assoc.getArrival().getSlow() != SLOW_NA
					&& prediction.getAttribute(GeoAttributes.SLOWNESS_DEGREES) != SLOW_NA)
				    corr[2] = Math.toDegrees(assoc.getArrival().getSlow()
					    -prediction.getAttribute(GeoAttributes.SLOWNESS_DEGREES));

				masterEventCorrections.put(String.format("%s/%s", assoc.getSta(), assoc.getPhase()), corr);
			    }
			}
		    }
		}

		String format = "  %-6s %-6s %2s %8.3f%n";
		if (logger.getVerbosity() > 0)
		{
		    logger.writeln(loggerHeader);
		    logger.write(String.format("masterEvent loaded:%n"
			    + "  Evid    = %d%n"
			    + "  Orid    = %d%n"
			    + "  Lat     = %11.5f%n"
			    + "  Lon     = %11.5f%n"
			    + "  Depth   = %8.3f%n"
			    + "  Time    = %15.3f%n"
			    + "  Jdate   = %d%n"
			    + "  NAssocs = %d%n%n",
			    masterEvent.getEvid(),
			    masterEvent.getOrid(),
			    masterEvent.getLat(),
			    masterEvent.getLon(),
			    masterEvent.getDepth(),
			    masterEvent.getTime(),
			    masterEvent.getJdate(),
			    masterEvent.getAssocs().size()
			    ));
		    for (String mec : new TreeSet<String>(masterEventCorrections.keySet()))
		    {
			double[] corr = masterEventCorrections.get(mec);
			String[] staPhase = mec.split("/");
			if (corr[0] != Assoc.TIMERES_NA)
			    logger.write(String.format(format, staPhase[0],staPhase[1], "tt", corr[0]));
			if (corr[1] != Assoc.AZRES_NA)
			    logger.write(String.format(format, staPhase[0], staPhase[1], "az", corr[1]));
			if (corr[2] != Assoc.SLORES_NA)
			    logger.write(String.format(format, staPhase[0], staPhase[1], "sh", corr[2]));
		    }
		    logger.writeln();
		}

		return masterEventCorrections;
    }

}
