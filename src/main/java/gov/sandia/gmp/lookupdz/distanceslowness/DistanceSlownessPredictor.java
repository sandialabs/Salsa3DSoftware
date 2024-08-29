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
package gov.sandia.gmp.lookupdz.distanceslowness;

import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.seismicbasedata.SeismicBaseData;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

public class DistanceSlownessPredictor {

	public static void main(String[] args) {

		try {
			DistanceSlownessPredictor dsp = null;
			
			DistanceSlownessModel.SURFACE_ONLY = false;

			if (args.length == 0)
				// use the ak135 model stored in internal resources
				dsp = new DistanceSlownessPredictor();
			if (args.length == 1) 
				// args[0] is name of a properties file
				dsp = new DistanceSlownessPredictor(new PropertiesPlusGMP(new File(args[0])));
			if (args.length == 2) 
				// args are name of file model directory and name of model
				dsp = new DistanceSlownessPredictor(new File(args[0]), args[1]);

			for (SeismicPhase phase : new SeismicPhase[] {SeismicPhase.PP,SeismicPhase.PS,SeismicPhase.SP,SeismicPhase.SS,SeismicPhase.Lg,SeismicPhase.pP,SeismicPhase.pS,SeismicPhase.sP,SeismicPhase.sS,SeismicPhase.nNL,SeismicPhase.nP,SeismicPhase.P,SeismicPhase.Pb,SeismicPhase.PcP,SeismicPhase.PcS,SeismicPhase.Pdiff,SeismicPhase.Pg,SeismicPhase.PKiKP,SeismicPhase.PKKP,SeismicPhase.PKKPab,SeismicPhase.PKKPbc,SeismicPhase.PKKPdf,SeismicPhase.PKKS,SeismicPhase.PKKSab,SeismicPhase.PKKSbc,SeismicPhase.PKKSdf,SeismicPhase.PKP,SeismicPhase.PKP2,SeismicPhase.PKP2df,SeismicPhase.PKPab,SeismicPhase.PKPbc,SeismicPhase.PKPdf,SeismicPhase.PKPPKP,SeismicPhase.PKS,SeismicPhase.PKSab,SeismicPhase.PKSbc,SeismicPhase.PKSdf,SeismicPhase.Pn,SeismicPhase.PnPn,SeismicPhase.pPdiff,SeismicPhase.pPKiKP,SeismicPhase.pPKP,SeismicPhase.pPKPab,SeismicPhase.pPKPbc,SeismicPhase.pPKPdf,SeismicPhase.PPP,SeismicPhase.pSKS,SeismicPhase.pSKSac,SeismicPhase.pSKSdf,SeismicPhase.S,SeismicPhase.Sb,SeismicPhase.ScP,SeismicPhase.ScS,SeismicPhase.Sdiff,SeismicPhase.Sg,SeismicPhase.SKiKP,SeismicPhase.SKKP,SeismicPhase.SKKPab,SeismicPhase.SKKPbc,SeismicPhase.SKKPdf,SeismicPhase.SKKS,SeismicPhase.SKKSac,SeismicPhase.SKKSdf,SeismicPhase.SKP,SeismicPhase.SKPab,SeismicPhase.SKPbc,SeismicPhase.SKPdf,SeismicPhase.SKS,SeismicPhase.SKS2,SeismicPhase.SKS2df,SeismicPhase.SKSac,SeismicPhase.SKSdf,SeismicPhase.SKSSKS,SeismicPhase.Sn,SeismicPhase.SnSn,SeismicPhase.sPdiff,SeismicPhase.sPKiKP,SeismicPhase.sPKP,SeismicPhase.sPKPab,SeismicPhase.sPKPbc,SeismicPhase.sPKPdf,SeismicPhase.sSKS,SeismicPhase.sSKSac,SeismicPhase.sSKSdf,SeismicPhase.SSS,}) {
				DistanceSlownessModel model = dsp.getModel(phase);
				for (int z=0; z<model.depths.length; ++z) {
					double[][] ds = model.getDistanceSlownessCurve(model.depths[z]);
					double[] d = ds[0];
					double[] s = ds[1];

					System.out.printf("%-8s\t\t%3d\t%8.4f\t%8.4f\t%8.4f\t%8.4f\t%8.4f\t%8.4f\t%8.4f%n", 
							phase.name(), z,
							d[0], d[d.length-1], d[d.length-1]-d[0], 
							s[0], s[s.length-1], s[s.length-1]-s[0], model.getRMS(0));
					break;
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static final Map<File, DistanceSlownessModel> modelMap = new LinkedHashMap<>();

	private static DistanceSlownessModel getDistanceSlownessModel(File modelFile) throws Exception {
		synchronized (modelMap) {
			DistanceSlownessModel model = modelMap.get(modelFile);
			if (model == null) 
				modelMap.put(modelFile, model = new DistanceSlownessModel(modelFile));
			return model;
		}
	}

	private File modelDirectory;

	private String modelName;

	public DistanceSlownessPredictor() throws PropertiesPlusException {
		this(Paths.get("seismic-base-data.jar", "tt", "ak135").toFile(), "ak135");
	}

	public DistanceSlownessPredictor(File modelDirectory, String modelName) throws PropertiesPlusException {
		this.modelDirectory = modelDirectory;
		this.modelName = modelName;
	}

	public DistanceSlownessPredictor(PropertiesPlusGMP properties) throws PropertiesPlusException {
		modelName = properties.getProperty("distanceSlownessModelName", "ak135");
		File siesmicBaseData = properties.getFile("seismicBaseData", new File("seismic-base-data.jar"));
		modelDirectory = new File(new File(siesmicBaseData, "tt"), modelName);
	}

	public double getDistance(SeismicPhase phase, double slowness, double depth) throws Exception {
		DistanceSlownessModel model = getDistanceSlownessModel(getFile(phase));
		return model.getDistance(slowness, depth);
	}

	private File getFile(SeismicPhase phase) {
		File f = new File(modelDirectory, phase.getFileName());
		if (new SeismicBaseData(f).exists())
			return f;
		f = new File(modelDirectory, modelName+"."+phase.getFileName());
		if (f.exists())
			return f;
		return null;
	}

	public File getModelDirectory() {
		return modelDirectory;
	}

	public String getModelName() {
		return modelName;
	}

	public DistanceSlownessModel getModel(SeismicPhase phase) throws Exception {
		return getDistanceSlownessModel(getFile(phase));
	}

}
