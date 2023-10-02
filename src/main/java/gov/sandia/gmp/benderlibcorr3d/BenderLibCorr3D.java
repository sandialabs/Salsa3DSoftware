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
package gov.sandia.gmp.benderlibcorr3d;

import java.io.File;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.UncertaintyInterface;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.bender.Bender;
import gov.sandia.gmp.lookupdz.LookupTablesGMP;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class BenderLibCorr3D extends Predictor {

	private Bender bender;

	private LookupTablesGMP lookup2d;

	public BenderLibCorr3D(PropertiesPlus properties) throws Exception 
	{
		super(properties);

		predictionsPerTask = properties.getInt(getPredictorName()
			+"PredictionsPerTask", 500);

		File benderModelFile = properties.getFile("benderModel");

		if (benderModelFile == null)
			throw new Exception("Property benderModel is not specified in the properties file.");

		if (!benderModelFile.exists())
			throw new Exception("benderModel "+benderModelFile+" does not exist.");

		if (!benderModelFile.isDirectory())
			throw new Exception("Property benderModel does not specify a directory.");

		File libcorrDir = new File(benderModelFile, "libcorr3d_delta_ak135");

		if (!libcorrDir.exists())
			new Exception("Directory " +libcorrDir+" does not exist.");

		bender = new Bender(properties);

		PropertiesPlusGMP p = new PropertiesPlusGMP();
		p.setProperty("lookup2dPathCorrectionsType = libcorr");
		p.setProperty("lookup2dLibCorrPathCorrectionsRoot", libcorrDir.getCanonicalPath());

		String prefix = "benderlibcorr3d";
		for (String key : properties.stringPropertyNames())
		{
			if (key.startsWith(prefix))
			{
				String value = properties.getProperty(key);
				p.setProperty("lookup2d"+key.substring(key.length()), value);
			}
		}
		
		lookup2d = new LookupTablesGMP(p);
	}
	
	/**
	 * Return the current version number.
	 * 
	 * @return String
	 */
	public static String getVersion() { return Utils.getVersion("bender-libcorr3d"); }

	public Predictor getPredictor(Receiver receiver, SeismicPhase phase) throws Exception { 
		if (lookup2d.getLibcorr3d().isSupported(receiver, phase.toString(), "TT"))
			return lookup2d;
		else
			return bender;
	}

	@Override
	public Predictor getPredictor(PredictionRequest request) throws Exception { 
		return getPredictor(request.getReceiver(), request.getPhase());
	}

	@Override
	public Prediction getPrediction(PredictionRequest request) throws Exception {
		return getPredictor(request).getPrediction(request);
	}

	@Override
	public Prediction getNewPrediction(PredictionRequest request, String msg) {
		try {
			return getPredictor(request).getNewPrediction(request, msg);
		} catch (Exception e) {
			return new Prediction(request, this, msg);
		}
	}

	@Override
	public Prediction getNewPrediction(PredictionRequest request, Exception ex) {
		try {
			return getPredictor(request).getNewPrediction(request, ex);
		} catch (Exception e) {
			return new Prediction(request, this, ex);
		}
	}

	@Override
	public String getModelDescription() throws Exception {
		return bender.getModelDescription();
	}

	@Override
	public String getModelName() {
		return bender.getModelName();
	}

	@Override
	public String getPredictorName() {
		return this.getClass().getSimpleName().toLowerCase();
	}

	@Override
	public PredictorType getPredictorType() {
		return PredictorType.BENDERLIBCORR3D;
	}

	@Override
	public File getModelFile() {
		return bender.getModelFile();
	}

	@Override
	public boolean isSupported(Receiver receiver, SeismicPhase phase, GeoAttributes attribute, double originTime) {
		try {
			Predictor predictor = getPredictor(receiver, phase);
			return predictor.getSupportedPhases().contains(phase) && 
					predictor.getSupportedAttributes().contains(attribute);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getPredictorVersion() {
	    return getVersion();
	}

	@Override
	public EnumSet<GeoAttributes> getSupportedAttributes() {
	    return bender.getSupportedAttributes();
	}

	@Override
	public EnumSet<SeismicPhase> getSupportedPhases() {
	    return bender.getSupportedPhases();
	}

	@Override
	public Object getEarthModel() {
	    return bender.getEarthModel();
	}

	@Override
	public UncertaintyInterface getUncertaintyInterface() {
	    return bender.getUncertaintyInterface();
	}

}
