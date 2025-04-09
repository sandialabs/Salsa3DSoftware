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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.baseobjects.observation.Observation;
import gov.sandia.gmp.locoo3d.LocOO;
import gov.sandia.gmp.locoo3d.LocOOTask;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;

public class NativeInput {

	protected PropertiesPlusGMP mainProperties;
	protected PropertiesPlusGMP taskProperties;
	protected ScreenWriterOutput logger;
	protected ScreenWriterOutput errorlog;

	/**
	 * Map from sourceId to baseobjects.Source object containing all the input sources.
	 * Note that Source contains a reference to a Collection of baseobjects.Observations
	 * and each Observation has a reference to a baseobjects.Receiver object.
	 * 
	 */
	protected Map<Long, Source> sources;

	public NativeInput() {
	}

	/**
	 * Protected so it can only be referenced by classes that extend DataInput
	 * @param properties
	 * @throws Exception
	 */
	public NativeInput(PropertiesPlusGMP properties) throws Exception {
		this();
		this.mainProperties = properties;
		this.taskProperties = (PropertiesPlusGMP) this.mainProperties.clone();
		VectorGeo.setEarthShape(properties);	
		setupLoggers();
	}

	/**
	 * This method is called by LocOO to retrieve input data.  Applications should
	 * not call this.
	 * Given a list of sourceids, retrieve a LocOOTask that includes those 
	 * sources.  The data includes the sources as well as the associated observations
	 * and receivers.
	 * @param sourceids
	 * @return
	 * @throws Exception
	 */
	public LocOOTask getLocOOTask(ArrayListLong sourceids) throws Exception {
		Collection<Source> taskOriginSet = new LinkedHashSet<Source>(sourceids.size());
		for (int i = 0; i < sourceids.size(); ++i)
			taskOriginSet.add(sources.get(sourceids.get(i)));
		LocOOTask task = new LocOOTask(taskProperties, taskOriginSet);
		return task;
	}

	/**
	 * This method is called by LocOO to retrieve input data.  Applications should
	 * not call this.
	 * Query the data and retrieve batches of sourceid such that each batch has
	 * less than some number of time defining phases.  Returns a 2D ragged array of longs.
	 * The first index spans the batches and the second spans the orids|sourceids in each
	 * batch.
	 * <p>The maximum number of time defining phases in a batch is retrieve from the 
	 * input property file with property batchSizeNdef, which defaults to 100.  
	 */
	public ArrayList<ArrayListLong> readTaskSourceIds() throws Exception {
		if (sources == null || sources.size() == 0) return new ArrayList<>();

		// sort the sources by ndef decreasing and populate batches.
		List<Source>list = new ArrayList<>(sources.values());
		Source.sortByNdefDescending(list);

		int batchSizeNdef = (int) list.get((int)(list.size()*0.75)).getNdef();

		batchSizeNdef = taskProperties.getInt("batchSizeNdef", batchSizeNdef);

		ArrayList<ArrayListLong> batches = new ArrayList<>(sources.size());
		ArrayListLong batch = new ArrayListLong();
		int n = 0;
		for (Source source : list) {
			batch.add(source.getSourceId());
			n += source.getNdef();
			if (n >= batchSizeNdef) {
				batches.add(batch);
				batch = new ArrayListLong();
				n = 0;
			}
		}
		if (batch.size() > 0)
			batches.add(batch);
		return batches;
	}

	/**
	 * This method is called by LocOO when it has finished retrieving data.  Applications should
	 * not call this.
	 * Perform any operations required to close a a data source (files, database connections,
	 * etc.).  
	 * @throws Exception
	 */
	public void close() throws Exception {
	}

	public void setSources(Collection<Source> sources) throws Exception {
		setSources(sources, null, null);
	}

	public void setSources(Collection<Source> sources, PropertiesPlusGMP changedProperties) throws Exception {
		setSources(sources, null, changedProperties);
	}

	public void setSources(Collection<Source> sources, Source masterEvent) throws Exception {
		setSources(sources, masterEvent, null);
	}

	public void setSources(Collection<Source> sources, Source masterEvent, PropertiesPlusGMP changedProperties) throws Exception {
		Map<String, double[]> masterEventCorrections = getMasterEventCorrections(masterEvent, "");
		this.sources = new LinkedHashMap<>(sources.size());
		for (Source s : sources) {
			this.sources.put(s.getSourceId(), s);
			applyMasterEventCorrections(s, masterEventCorrections);
		}

		if (changedProperties != null) {
			this.taskProperties = (PropertiesPlusGMP) this.mainProperties.clone();
			for (Entry<Object, Object> property : changedProperties.entrySet())
				taskProperties.put(property.getKey(), property.getValue());
		}
	}

	public PropertiesPlusGMP getProperties() {
		return taskProperties;
	}

	public ScreenWriterOutput getLogger() {
		return logger;
	}

	public ScreenWriterOutput getErrorlog() {
		return errorlog;
	}

	/**
	 * Retrieve masterEventCorrections for a specified masterEvent. Returns null if masterEvent is null.
	 *   
	 * <p>This method will compute residuals for all the defining observations in the master event using the
	 * predictors and earth models specified in the properties object. The master event is not relocated.  
	 * Those residuals will be used as mater event corrections for all the sources that are relocated.
	 * @param masterEvent
	 * @param loggerHeader
	 * @return a Map of sta/phase -> double[3] where the the doubles are tt, az, sh corrections.
	 * @throws Exception
	 */
	protected Map<String, double[]> getMasterEventCorrections(Source masterEvent, String loggerHeader) throws Exception
	{
		if (masterEvent == null)
			return null;

		Map<String, double[]> masterEventCorrections = new TreeMap<String, double[]>();
		// Create the predictors, using the PredictorFactory
		PredictorFactory predictors = new PredictorFactory(taskProperties,"loc_predictor_type", logger);

		PredictionRequest request = new PredictionRequest();
		request.setDefining(true);
		request.setSource(new Source(masterEvent));
		request.setRequestedAttributes(
				EnumSet.of(GeoAttributes.TRAVEL_TIME, GeoAttributes.AZIMUTH, GeoAttributes.SLOWNESS));

		for (Observation observation : masterEvent.getObservations().values())
		{
			SeismicPhase phase = SeismicPhase.valueOf(observation.getPhase());
			Predictor predictor = predictors.getPredictor(phase);
			if (predictor != null)
			{
				request.setReceiver(observation.getReceiver());
				request.setPhase(phase);

				Prediction prediction = predictor.getPrediction(request);
				if (prediction.isValid())
				{
					// master event corrections for tt, az, sh
					double[] corr = new double[3];

					if (observation.getTime() != Globals.NA_VALUE
							&& prediction.getAttribute(GeoAttributes.TRAVEL_TIME) != Globals.NA_VALUE)
						corr[0] = observation.getTime()-masterEvent.getTime()-
						prediction.getAttribute(GeoAttributes.TRAVEL_TIME);

					if (observation.isAzdef() && observation.getAzimuth() != Globals.NA_VALUE
							&& prediction.getAttribute(GeoAttributes.AZIMUTH) != Globals.NA_VALUE)
					{
						// everything in radians
						corr[1] = observation.getAzimuth()-prediction.getAttribute(GeoAttributes.AZIMUTH);
						if (corr[1] < -Math.PI) 
							corr[1] += 2*Math.PI;
						else if (corr[1] > Math.PI) 
							corr[1] -= 2*Math.PI;
					}

					// everything in sec/radian
					if (observation.getSlow() != Globals.NA_VALUE
							&& prediction.getAttribute(GeoAttributes.SLOWNESS) != Globals.NA_VALUE)
						corr[2] = observation.getSlow()-prediction.getAttribute(GeoAttributes.SLOWNESS);

					masterEventCorrections.put(String.format("%s/%s", observation.getReceiver().getSta(), observation.getPhase()), corr);
				}
			}
		}

		if (logger.getVerbosity() > 0)
		{
			logger.writeln(loggerHeader);
			logger.write(String.format("masterEvent loaded:%n"
					+ "  Evid    = %d%n"
					+ "  Orid    = %d%n"
					+ "  Lat     = %11.5f%n"
					+ "  Lon     = %11.5f%n"
					+ "  Depth   = %9.3f%n"
					+ "  Time    = %15.3f%n"
					+ "  Jdate   = %d%n"
					+ "  NAssocs = %d%n%n",
					masterEvent.getEvid(),
					masterEvent.getSourceId(),
					masterEvent.getLat(),
					masterEvent.getLon(),
					masterEvent.getDepth(),
					masterEvent.getTime(),
					GMTFormat.getJDate(masterEvent.getTime()),
					masterEvent.getObservations().size()
					));
			for (String mec : new TreeSet<String>(masterEventCorrections.keySet()))
			{
				double[] corr = masterEventCorrections.get(mec);
				String[] staPhase = mec.split("/");
				if (corr[0] != Assoc.TIMERES_NA)
					logger.write(String.format("  %-6s %-6s %2s %8.3f seconds%n", staPhase[0],staPhase[1], "tt", corr[0]));
				if (corr[1] != Assoc.AZRES_NA)
					logger.write(String.format("  %-6s %-6s %2s %8.3f degrees%n", staPhase[0], staPhase[1], "az", Math.toDegrees(corr[1])));
				if (corr[2] != Assoc.SLORES_NA)
					logger.write(String.format("  %-6s %-6s %2s %8.3f sec/deg%n", staPhase[0], staPhase[1], "sh", Math.toRadians(corr[2])));
			}
			logger.writeln();
		}
		predictors.close();
		return masterEventCorrections;
	}


	/**
	 * If master event corrections are available, they will be set in each of the observations associated with this source.
	 * If property masterEventUseOnlyStationsWithCorrections is true, then observations that do not have master event corrections
	 * will be set to nondefining.
	 * @param source
	 * @throws Exception
	 */
	protected void applyMasterEventCorrections(Source source, Map<String, double[]> masterEventCorrections) throws Exception {
		if (masterEventCorrections != null) {
			int nChanges = 0;

			boolean masterEventUseOnlyStationsWithCorrections = taskProperties.getBoolean("masterEventUseOnlyStationsWithCorrections", false);


			for (Observation obs : source.getObservations().values()) {
				String staPhase = obs.getReceiver().getSta()+"/"+obs.getPhase().name();
				double[] mecorr = masterEventCorrections.get(staPhase);

				obs.setMasterEventCorrections(mecorr);

				if (masterEventUseOnlyStationsWithCorrections && mecorr == null) {
					obs.setTimedef(false);
					obs.setAzdef(false);
					obs.setSlodef(false);
					++nChanges;
				}
			}

			if (logger.getVerbosity() > 0 && masterEventUseOnlyStationsWithCorrections) {
				logger.write(String.format("%d observations were set to non-defining because masterEventUseOnlyStationsWithCorrections is true.%n", 
						nChanges));
			}
		}

	}

	/**
	 * Sets up status log and error log based on properties in property file:
	 * <ul>
	 * <li><b>io_verbosity</b> int
	 * <li><b>io_print_to_screen</b> boolean
	 * <li><b>io_log_file</b> String name of file to which status log will be output. Default is no
	 * output.
	 * <li><b>io_print_errors_to_screen</b> boolean defaults to true
	 * <li><b>io_error_file</b> String name of file to which error messages are written. Defaults to
	 * "locoo_errors.txt"
	 * </ul>
	 * 
	 * @param taskProperties
	 */
	private void setupLoggers() {

		try {
			errorlog = new ScreenWriterOutput();
			File errorLogFile = null;

			if (taskProperties.getBoolean("io_print_errors_to_screen", true))
				errorlog.setScreenOutputOn();
			else
				errorlog.setScreenOutputOff();

			if (taskProperties.getProperty("io_error_file") != null) {
				errorLogFile = new File(taskProperties.getProperty("io_error_file"));
				errorlog.setWriter(new BufferedWriter(new FileWriter(errorLogFile)));
				errorlog.setWriterOutputOn();
			}
			errorlog.setBufferOutputOn();

			// turn logger off and back on to ensure current status is stored.
			errorlog.turnOff();
			errorlog.restore();

			logger = new ScreenWriterOutput();
			logger.setVerbosity(taskProperties.getInt("io_verbosity", 1));

			File logfile = null;
			if (taskProperties.getBoolean("io_print_to_screen", true))
				logger.setScreenOutputOn();
			else
				logger.setScreenOutputOff();
			if (taskProperties.getProperty("io_log_file") != null) {
				logfile = new File(taskProperties.getProperty("io_log_file"));
				logger.setWriter(new BufferedWriter(new FileWriter(logfile)));
				logger.setWriterOutputOn();
			}
			logger.setBufferOutputOn();

			// turn logger off and back on to ensure current status is stored.
			logger.turnOff();
			logger.restore();

			if (!logger.isOutputOn())
				logger.setVerbosity(0);

			if (logger.getVerbosity() > 0) {
				logger.write(String.format("LocOO3D v. %s started %s%n%n", LocOO.getVersion(),
						GMTFormat.localTime.format(new Date())));

				if (taskProperties.getPropertyFile() != null)
					logger.writeln("Properties from file " + taskProperties.getPropertyFile().getCanonicalPath());
				else
					logger.writeln("Properties:");
				logger.writeln(taskProperties.toString());

			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

}
