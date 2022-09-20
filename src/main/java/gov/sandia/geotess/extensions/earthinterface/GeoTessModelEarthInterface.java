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
package gov.sandia.geotess.extensions.earthinterface;

import java.io.File;
import java.io.IOException;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;

/**
 * An extended GeoTessModel that contains layer interface names in addition to
 * layer names that must be equivalent to and ordered by the layer interface
 * prescription given by the enum EarthInterface. This is used by applications
 * that require precise naming in order to determine Earth layers by name (e.g.
 * the Phase object used by Bender).
 * 
 * @author jrhipp
 *
 */
public class GeoTessModelEarthInterface extends GeoTessModel
{
	/**
	 * Simple return class that can be over-ridden by derived types to return
	 * extended meta-data objects.
	 * 
	 * @return new meta-data object
	 */
	@Override
	protected GeoTessMetaData getNewMetaData()
	{
		return new GeoTessMetaDataEarthInterface();
	}

	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * <p>OptimizationType will default to SPEED, as opposed to MEMORY.  With 
	 * OptimizationType.SPEED, the code will execute more quickly but will 
	 * require more memory to run.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param relativeGridPath
	 *            the relative path from the directory where the model is stored
	 *            to the directory where the grid is stored. Often, the model
	 *            and grid are stored together in the same file in which case
	 *            this parameter is ignored. Sometimes, however, the grid is
	 *            stored in a separate file and only the name of the grid file
	 *            (without path information) is stored in the model file. In
	 *            this case, the code needs to know which directory to search
	 *            for the grid file. The default is "" (empty string), which
	 *            will cause the code to search for the grid file in the same
	 *            directory in which the model file resides. Bottom line is that
	 *            the default value is appropriate when the grid is stored in
	 *            the same file as the model, or the model file is in the same
	 *            directory as the model file.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(File modelInputFile, String relativeGridPath) throws IOException
	{ 
		super(modelInputFile, relativeGridPath); 
	}
	
	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as the
	 * model file.
	 * 
	 * <p>OptimizationType will default to SPEED, as opposed to MEMORY.  With 
	 * OptimizationType.SPEED, the code will execute more quickly but will 
	 * require more memory to run.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @throws IOException
	 */  
  public GeoTessModelEarthInterface(File modelInputFile) throws IOException
	{ 
		super(modelInputFile); 
	}
	
	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * <p>OptimizationType will default to SPEED, as opposed to MEMORY.  With 
	 * OptimizationType.SPEED, the code will execute more quickly but will 
	 * require more memory to run.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @param relativeGridPath
	 *            the relative path from the directory where the model is stored
	 *            to the directory where the grid is stored. Often, the model
	 *            and grid are stored together in the same file in which case
	 *            this parameter is ignored. Sometimes, however, the grid is
	 *            stored in a separate file and only the name of the grid file
	 *            (without path information) is stored in the model file. In
	 *            this case, the code needs to know which directory to search
	 *            for the grid file. The default is "" (empty string), which
	 *            will cause the code to search for the grid file in the same
	 *            directory in which the model file resides. Bottom line is that
	 *            the default value is appropriate when the grid is stored in
	 *            the same file as the model, or the model file is in the same
	 *            directory as the model file.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(String modelInputFile, String relativeGridPath) throws IOException
	{ 
		super(modelInputFile, relativeGridPath); 
	}

	/**
	 * Construct a new GeoTessModelEarthInterface object and populate it with
	 * information from the specified file.
	 * 
	 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as the
	 * model file.
	 * 
	 * <p>OptimizationType will default to SPEED, as opposed to MEMORY.  With 
	 * OptimizationType.SPEED, the code will execute more quickly but will 
	 * require more memory to run.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(String modelInputFile) throws IOException
	{ 
		super(modelInputFile); 
	}
	
	/**
	 * Parameterized constructor, specifying the grid and metadata for the
	 * model. The grid is constructed and the data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setModelLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * <li>setOptimization() (optional: defaults to SPEED)
	 * </ul>
	 * 
	 * @param gridFileName
	 *            name of file from which to load the grid.
	 * @param metaData
	 *            MetaData the new GeoTessModelEarthInterface instantiates a
	 *            reference to the supplied metaData. No copy is made.
	 * @throws IOException
	 *             if metadata is incomplete.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(File gridFileName, GeoTessMetaDataEarthInterface metaData)
  		   throws IOException
	{ 
		super(gridFileName, metaData); 
	}
  
	/**
	 * Parameterized constructor, specifying the grid and metadata for the
	 * model. The grid is constructed and the data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setModelLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * <li>setOptimization() (optional: defaults to SPEED)
	 * </ul>
	 * 
	 * @param gridFileName
	 *            name of file from which to load the grid.
	 * @param metaData
	 *            MetaData the new GeoTessModelEarthInterface instantiates a
	 *            reference to the supplied metaData. No copy is made.
	 * @throws IOException
	 */
  public GeoTessModelEarthInterface(String gridFileName, GeoTessMetaDataEarthInterface metaData)
  		   throws IOException
	{ 
		super(gridFileName, metaData); 
	}
  
	/**
	 * Parameterized constructor, specifying the grid and metadata for the
	 * model. The grid is constructed and the data structures are initialized
	 * based on information supplied in metadata. The data structures are not
	 * populated with any information however (all Profiles are null). The
	 * application should populate the new model's Profiles after this
	 * constructor completes.
	 * 
	 * <p>
	 * Before calling this constructor, the supplied MetaData object must be
	 * populated with required information by calling the following MetaData
	 * methods:
	 * <ul>
	 * <li>setDescription()
	 * <li>setModelLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * <li>setOptimization() (optional: defaults to SPEED)
	 * <li>setSoftwareVersion()
	 * <li>setGenerationDate()
	 * </ul>
	 * 
	 * @param grid
	 *            a reference to the GeoTessGrid that will support this
	 *            GeoTessModel.
	 * @param metaData
	 *            MetaData the new GeoTessModelEarthInterface instantiates a
	 *            reference to the supplied metaData. No copy is made.
	 * @throws GeoTessException
	 *             if metadata is incomplete.
	 */
  public GeoTessModelEarthInterface(GeoTessGrid grid, GeoTessMetaDataEarthInterface metaData)
  		   throws GeoTessException, IOException
	{ 
		super(grid, metaData); 
	}
}
