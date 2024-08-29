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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessModelUtils;
import gov.sandia.geotess.PointMap;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.geotess.extensions.siteterms.GeoTessModelSiteData;
import gov.sandia.geotess.extensions.siteterms.SiteData;
import gov.sandia.gmp.bender.Bender;
import gov.sandia.gmp.lookupdz.LookupTable;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

public class DataSinkGeoTess extends DataSink {

	private String outputFile;

	private GeoTessModel geotessModel;
	
	public DataSinkGeoTess(PCalc pcalc) throws Exception {
		super(pcalc);

		// Set up the output device.
		outputFile = properties.getProperty("outputFile");
		
		if (outputFile == null)
		    throw new GMPException("Properties file does not contain property 'outputFile'");
	}

	@Override
	public void writeData(Bucket bucket) throws Exception {
		if (bucket.geotessModel == null)
			throw new Exception("bucket.geotessModel is null");

		if (bucket.modelValues == null)
			throw new Exception("bucket.modelValues is null");

		if (bucket.modelValues.length != bucket.geotessModel.getNPoints())
			throw new Exception(String.format(
					"bucket.modelValues.size(%d) != bucket.geotessModel.getNPoints(%d)",
					bucket.modelValues.length, bucket.geotessModel.getNPoints()));

		geotessModel = bucket.geotessModel;

		outputFile = outputFile
				.replaceAll("<sta>", bucket.site.getSta())
				.replaceAll("<refsta>", bucket.site.getRefsta())
				.replaceAll("<phase>", bucket.phases.get(0).toString())
				.replaceAll("<ondate>", String.format("%d", bucket.site.getOndate()))
				.replaceAll("<offdate>", String.format("%d", bucket.site.getOffdate()));
		
		File fout = new File(outputFile);
		if (fout.getParentFile() != null && !fout.getParentFile().exists())
			fout.getParentFile().mkdirs();

		// Note that bucket.modelValues[i] is an array of length outputAttributes.size()+1.
		// The first element is the radius of the point and subsequent elements are the 
		// values of the outputAttributes.
		PointMap pm = bucket.geotessModel.getPointMap();
		for (int i=0; i<bucket.modelValues.length; ++i) {
			double[] values = bucket.modelValues[i];
			for (int j=1; j<values.length; ++j)
				pm.setPointValue(i,j-1,values[j] ==  Globals.NA_VALUE ? Double.NaN : values[j]);
		}
		
		StringBuffer d = new StringBuffer();
		
		getDescription(bucket, d);

		bucket.geotessModel.getMetaData().setDescription(d.toString());
	}

	private void getDescription(Bucket bucket, StringBuffer d)
	{
		try {
			// Specify a description of the model.
			d.append(String.format("%s containing model predictions computed by PCalc version %s%n", 
					bucket.geotessModel.getMetaData().getModelClassName(), PCalc.getVersion()));
			try {
				d.append("receiver = ").append(bucket.receivers.get(0).getSiteRow().toString()).append("\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
			d.append("grid rotation angles = "+bucket.geotessModel.getMetaData().getEulerRotationAnglesString()+"\n");
			d.append("phase = ").append(bucket.phases.get(0).toString()).append("\n");
			
			if (properties.containsKey("geotessDepthSpacing"))
				d.append("geotessDepthSpacing = "+properties.getProperty("geotessDepthSpacing", "")+"\n");
			if (properties.containsKey("geotessDepths"))
			{
				d.append("geotessDepths = "+properties.getProperty("geotessDepths", "")+"\n");
				d.append("spanSeismicityDepth = "+properties.getProperty("spanSeismicityDepth", "true")+"\n");
			}
			
			String predictor = properties.getProperty("predictors", "?");
			if (predictor.equalsIgnoreCase("bender"))
			{
				//GeoTessModel benderModel = predictors.getGeoTessModel("bender");
				GeoTessModel benderModel = Bender.getGeoTessModel(
						properties.getFile("benderModel"));

				d.append(String.format("predictor = bender version %s%n"
						+ "benderModel = %s%n"
						+ "benderModel generation date = %s%n" 
						+ "benderModel GridID = %s%n",
						Bender.getVersion(),
						properties.getProperty(predictor+"Model", "?"),
						(benderModel == null ? "?" : benderModel.getMetaData().getModelGenerationDate()),
						(benderModel == null ? "?" : benderModel.getGrid().getGridID())
						));
				getSiteTerms(benderModel, d);
			}
			else if (predictor.equalsIgnoreCase("lookup2d"))
			{
				d.append(String.format("predictor = lookup2d version %s%n"
						+ "lookup2dModel = %s%n", LookupTable.getVersion(),
						properties.getProperty(predictor+"Model", "?")));
			}
			
			String seismicBaseData = properties.getProperty("seismicBaseData", "seismic-base-data.jar");
			
			d.append(String.format("seismicBaseData = %s%n", seismicBaseData));

			if (geotessModel.getMetaData().getAttributeIndex("TT_MODEL_UNCERTAINTY") >= 0)
			{
				String uncertaintyType = properties.getProperty(predictor+"UncertaintyType", "null");

				if (uncertaintyType.toLowerCase().startsWith("distance"))
				{
					
					String uncertaintyModel = properties.getProperty(predictor+"UncertaintyModel","?");
					if (predictor.equalsIgnoreCase("bender"))
					{
						File benderModel = properties.getFile("benderModel");
						if (benderModel != null && benderModel.isDirectory())
						{
							File ttDir = new File(new File(benderModel, "distance_dependent_uncertainty"), "tt");
							String[] files = ttDir.list(new FilenameFilter() {
								@Override
								public boolean accept(File dir, String name) { return !name.startsWith("."); }
							});

							if (files.length == 1)
								uncertaintyModel = new File(files[0]).getName();
						}
					}
					
					d.append(String.format("reporting %s uncertainty%n"
							+ "%sUncertaintyModel = %s%n"
							+ "%sUncertaintyDirectory = %s%n", 
							uncertaintyType,
							predictor, uncertaintyModel,
							predictor, properties.getProperty(predictor+"UncertaintyDirectory", seismicBaseData)
							));
				}
				else if (uncertaintyType.toLowerCase().startsWith("path"))
				{
					d.append(String.format("reporting %s uncertainty%n"
							+ "slownessLayerStandardDeviation_P = %s%n"
							+ "slownessLayerStandardDeviation_S = %s%n",
							uncertaintyType,
							properties.getProperty("slownessLayerStandardDeviation_P", ""),
							properties.getProperty("slownessLayerStandardDeviation_S", "")
							));
				}
				else
					d.append("Cannot interpret the uncertaintyType from "
							+ predictor+"UncertaintyType = "+uncertaintyType);
			}
		} catch (Exception e) {
			d.append("\nPopulation of description field failsed because "+e.getMessage()+"\n");
			e.printStackTrace();
		}
	}

	private void getSiteTerms(GeoTessModel benderModel, StringBuffer d) throws Exception
	{
		try {
			if (!benderModel.getClass().getSimpleName().equals("GeoTessModelSiteData"))
				d.append("No site terms are available because benderModel is not a GeoTessModelSiteData.\n");
			else
			{
				String sta = properties.getProperty("sta");
				if (sta == null)
					d.append("No site terms are availabe because property 'sta' is not defined.\n");
				else
				{
					ArrayList<SiteData> siteDataList = ((GeoTessModelSiteData)benderModel).getSiteTermMap().get(sta);
					if (siteDataList == null)
						d.append(String.format("benderModel does not contain any site terms for station %s%n",sta));
					else
					{
						int jdate = properties.getInt("jdate", 2286324);
						SiteData siteData = null;
						for (SiteData sd : siteDataList)
							if (sd.inRange(jdate))
								siteData = sd;
						if (siteData == null)
							d.append(String.format("benderModel does not contain any site terms for station %s on jdate %d%n",
									sta, jdate));
						else
						{
							d.append(String.format("site terms from benderModel for %s on jdate %d: %s%n", sta, jdate,
									siteData.getDataString()));
							boolean use_tt_site_terms = properties.getBoolean("use_tt_site_terms", true);
							if (!use_tt_site_terms)
								d.append("site terms not applied because property use_tt_site_terms = false\n");
						}
					}
				}
			}
		} catch (PropertiesPlusException e) {
			d.append("\nRetrieval of site terms failed because "+e.getMessage()+"\n");
		}
	}

	@Override
	public void close() throws Exception {
	    if(geotessModel != null) {
		if (log.isOutputOn())
		    log.write(String.format("Sending output to %s%n", new File(outputFile).getCanonicalPath()));

		int geotessFileFormat = properties.getInt("geotessFileFormat", -1);
		if (geotessFileFormat > -1)
		    geotessModel.getMetaData().setModelFileFormat(geotessFileFormat);

		if (geotessModel instanceof LibCorr3DModel)
		{
		    int libcorr3dFileFormat = properties.getInt("libcorr3dFileFormat", -1);
		    if (libcorr3dFileFormat > -1)
			((LibCorr3DModel)geotessModel).setFormatVersion(libcorr3dFileFormat);
		}

		File f = new File(outputFile);
		if (f.getParentFile() != null)
		    f.getParentFile().mkdirs();

		geotessModel.writeModel(outputFile, this.properties.getProperty("geotessOutputGridFile", 
			this.properties.getProperty("geotessInputGridFile")));

		if (log.isOutputOn())
		    log.write("\nFinal Model:\n"+geotessModel.toString()+"\n"+GeoTessModelUtils.statistics(geotessModel));

		try {
		    // deal with vtk files
		    File vtkFile = properties.getFile("vtkFile");
		    File vtkTrianglesFile = properties.getFile("vtkTrianglesFile");

		    if (vtkFile != null || vtkTrianglesFile != null)
		    {
			double centerLon = Double.NaN;
			boolean robinson = properties.getBoolean("vtkRobinson", false);
			if (robinson) {
			    centerLon = properties.getDouble("vtkCenterLon", Double.NaN);
			    if (Double.isNaN(centerLon) && geotessModel instanceof LibCorr3DModel)
				centerLon = ((LibCorr3DModel)geotessModel).getSite().getLon();
			    else if (Double.isNaN(centerLon))
				centerLon = 162;
			}
			
			if (vtkFile != null)
			{
			    int layerid = geotessModel.getMetaData().getNLayers()-1;

			    if (vtkFile.getParentFile() != null)
				vtkFile.getParentFile().mkdirs();
			    if (robinson)
				GeoTessModelUtils.vtkRobinson(geotessModel, vtkFile, centerLon, 1e4, layerid, true, 
					InterpolatorType.LINEAR, false, null);
			    else
				GeoTessModelUtils.vtk(geotessModel, vtkFile.getAbsolutePath(), layerid, false, null);
			}

			if (vtkTrianglesFile != null)
			{
			    int tessid = geotessModel.getGrid().getNTessellations()-1;
			    if (vtkTrianglesFile.getParentFile() != null)
				vtkTrianglesFile.getParentFile().mkdirs();
			    if (robinson)
				GeoTessModelUtils.vtkRobinsonTriangleSize(geotessModel.getGridRotated(), 
					vtkTrianglesFile.getAbsoluteFile(),  centerLon, tessid);
			    else
				GeoTessModelUtils.vtkTriangleSize(geotessModel.getGridRotated(), vtkTrianglesFile, tessid);
			}
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	    else {
		log.write("DataSinkGeoTessModel.close(): No geotess model to write.");
	    }
	}
}
