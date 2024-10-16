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
package gov.sandia.geotess.extensions.libcorr3d;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.gmp.parallelutils.ParallelTask;
import gov.sandia.gmp.util.globals.Site;

public class ReadModelTask extends ParallelTask {

	private static final long serialVersionUID = 1L;
	private ArrayList<Integer> handles;
	private ArrayList<File> files;
	private ArrayList<Boolean> returnModels;
	private String relGridPath;

	public ReadModelTask(ArrayList<Integer> handles, ArrayList<File> files, String relGridPath,
			ArrayList<Boolean> returnModels) {
		this.handles = handles;
		this.files = files;
		this.relGridPath = relGridPath;
		this.returnModels = returnModels;
	}

	@Override
	public void run() {
		long timer = System.currentTimeMillis();
		
		ReadModelTaskResult result = new ReadModelTaskResult(handles.size());

		for (int i=0; i<handles.size(); ++i) {
			result.setHandle(i, handles.get(i));
			try {
				if (returnModels.get(i)) {
					// load the model and save it in result.
					LibCorr3DModel model = new LibCorr3DModel(files.get(i), relGridPath);
					result.setModel(i, model);;
					result.setSite(i, model.getSite());
					result.setAttribute(i, LibCorr3D.attributeTranslationMap.get(model.getMetaData().getAttributeName(0)));
					result.setSupportedPhases(i, model.getSupportedPhases());
				}
				else {
					// Read just the metadata object from the model file.  Newer models have the 
					// required model info in the metatdata properties object.  But older models do not.
					// Reading just the metatdata from the model file is MUCH faster than reading the whole file.
					GeoTessMetaData md = GeoTessMetaData.getMetaData(files.get(i));

					if (md.getProperties().containsKey("site"))
						result.setSite(i, new Site(md.getProperties().get("site")));

					if (md.getProperties().containsKey("supportedPhases"))
						result.setSupportedPhases(i, Arrays.asList(md.getProperties().get("supportedPhases").split(",")));

					result.setAttribute(i, LibCorr3D.attributeTranslationMap.get(md.getAttributeName(0)));

					// if unsuccessful, then must read in the entire model (slow!)
					// this will only happen with models written by old versions of code.
					if (result.getSite(i) == null || result.getSupportedPhases(i) == null || result.getAttribute(i) == null )
					{
						// load the model
						LibCorr3DModel model = new LibCorr3DModel(files.get(i), relGridPath);
						result.setSite(i, model.getSite());
						result.setAttribute(i, LibCorr3D.attributeTranslationMap.get(model.getMetaData().getAttributeName(0)));
						result.setSupportedPhases(i, model.getSupportedPhases());
						model.close();
					}
				}
			} catch (Exception e) {
				result.setErrorMessage(i, e.getMessage());
			}
		}
		result.setEllapsedTime(System.currentTimeMillis()-timer);
		setResult(result);
	}
}
