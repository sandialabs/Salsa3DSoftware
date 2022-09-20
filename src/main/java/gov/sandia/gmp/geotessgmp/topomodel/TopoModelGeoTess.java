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
package gov.sandia.gmp.geotessgmp.topomodel;

import java.io.File;

import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class TopoModelGeoTess 
{
  private GeoTessPosition topoModel;

  private int attributeIndex;

  private double unitConversionFactor;

  private File modelFile;

  public TopoModelGeoTess() {}

  public TopoModelGeoTess(PropertiesPlusGMP properties) throws Exception {
    loadTopoModel(properties);
  }

  public void loadTopoModel(File topoModelFile) throws Exception 
  {
    topoModel = new GeoTessModel(topoModelFile).getGeoTessPosition(); 

    attributeIndex = -1;
    unitConversionFactor = Double.NaN;

    determineAttributeIndex(topoModel.getModel().getMetaData());

    this.modelFile = topoModelFile;
  }

  
  public void loadTopoModel(PropertiesPlus properties) throws Exception 
  {
	  String topo_model = properties.getProperty("topo_model", "");
	  File topoModelFile = new File(topo_model);  
	  if (!topoModelFile.exists())
		  throw new GMPException(String.format("%nCannot load a topography model because %n"
				  + "%s%ndoes not exist.", topoModelFile.getCanonicalPath()));

	  topoModel = new GeoTessModel(topoModelFile).getGeoTessPosition(); 

	  this.modelFile = topoModelFile;

	  attributeIndex = properties.getInt("topo_model_attribute_index", -1);
	  unitConversionFactor = properties.getDouble("topo_model_unit_conversion_factor", Double.NaN);

	  determineAttributeIndex(topoModel.getModel().getMetaData());
  }

  
  public double getTopoKm(double[] unitVector) throws Exception 
  { return topoModel.set(0, unitVector, 6371.).getValue(attributeIndex) * unitConversionFactor; }


  private void determineAttributeIndex(GeoTessMetaData md) throws Exception
  {
    if (attributeIndex < 0)
      attributeIndex = topoModel.getModel().getMetaData().getAttributeIndex("Topography");
    if (attributeIndex < 0)
      attributeIndex = topoModel.getModel().getMetaData().getAttributeIndex("topography");
    if (attributeIndex < 0)
      attributeIndex = topoModel.getModel().getMetaData().getAttributeIndex("TOPOGRAPHY");
    if (attributeIndex < 0)
      throw new Exception(String.format("%nThe specified model does not have an attribute called 'topography'%n%s",
          md.toString()));

    if (Double.isNaN(unitConversionFactor))
    {
      String units = md.getAttributeUnit(attributeIndex).toLowerCase();
      if (units.equals("km")) unitConversionFactor = 1.;
      else if (units.equals("meters")) unitConversionFactor = 1e-3;
      else 
        throw new Exception(String.format("%nThe units of attribute %s are %s but must be one of [ km | meters ]%n",
            md.getAttributeName(attributeIndex), md.getAttributeUnit(attributeIndex)));
    }
  }

  
  public File getModelFile() { return modelFile; }

  
  public boolean isValid() { return modelFile != null; }

}
