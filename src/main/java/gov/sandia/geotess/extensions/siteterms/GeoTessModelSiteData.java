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
package gov.sandia.geotess.extensions.siteterms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import gov.sandia.geotess.AttributeDataDefinitions;
import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessUtils;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.DataType;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Site;
import gov.sandia.gmp.util.numerical.vector.EarthShape;

/**
 * An extended GeoTessModel that contains station site terms.
 * 
 * @author jrhipp
 *
 */
public class GeoTessModelSiteData extends GeoTessModel
{
	/**
	 * The map of receiver names mapped to a list of ReceiverSiteTerms objects.
	 */
	private Map<String, ArrayList<SiteData>> rcvrSiteTerms = 
			new TreeMap<String, ArrayList<SiteData>>();

	/**
	 * Total number of site term entries in the map.
	 */
	private int nSiteTerms = 0;

	protected AttributeDataDefinitions siteDataAttributes = new AttributeDataDefinitions();

    /**
     * Classes that extend GeoTessModel must override this method
     * and populate their 'extra' data with shallow copies from the model 
     * specified in the parameter list.  
     * @param other the other model from which to copy extra data
     * @throws Exception 
     */
	@Override
	public void copyDerivedClassData(GeoTessModel other) throws Exception
	{
		super.copyDerivedClassData(other);
		this.rcvrSiteTerms = ((GeoTessModelSiteData)other).rcvrSiteTerms;    
		this.nSiteTerms = ((GeoTessModelSiteData)other).nSiteTerms;
		this.siteDataAttributes = ((GeoTessModelSiteData)other).siteDataAttributes;
	}

	/**
	 * Construct a new GeoTessModelSiteTerms object and populate it with
	 * information from the specified file.
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
	public GeoTessModelSiteData(File modelInputFile, String relativeGridPath) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, relativeGridPath);
	}

	/**
	 * Construct a new GeoTessModelSiteTerms object.  Populate it with
	 * base class information from the specified file.  No derived class data
	 * is loaded.
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
	public GeoTessModelSiteData(File modelInputFile, String relativeGridPath,
			AttributeDataDefinitions siteDataAttributes) throws IOException
	{ 
		super(modelInputFile, relativeGridPath); 
		this.siteDataAttributes = siteDataAttributes;;
	}

	/**
	 * Construct a new GeoTessModelSiteTerms object and populate it with
	 * information from the specified file.
	 * 
	 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as the
	 * model file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @throws IOException
	 */
	public GeoTessModelSiteData(File modelInputFile) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, "");
	}

	/**
	 * Construct a new GeoTessModelSiteTerms object.  Populate it with
	 * base class information from the specified file.  No derived class data
	 * is loaded.
	 * 
	 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as the
	 * model file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @throws IOException
	 */
	public GeoTessModelSiteData(File modelInputFile,
			AttributeDataDefinitions siteDataAttributes) throws IOException
	{ 
		super(modelInputFile); 
		this.siteDataAttributes = siteDataAttributes;;
	}

	/**
	 * Construct a new GeoTessModelSiteTerms object and populate it with
	 * information from the specified file.
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
	public GeoTessModelSiteData(String modelInputFile, String relativeGridPath) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, relativeGridPath);
	}

	/**
	 * Construct a new GeoTessModelSiteTerms object and populate it with
	 * information from the specified file.
	 * 
	 * <p>relativeGridPath is assumed to be "" (empty string), which is appropriate
	 * when the grid information is stored in the same file as the model or when
	 * the grid is stored in a separate file located in the same directory as the
	 * model file.
	 * 
	 * @param modelInputFile
	 *            name of file containing the model.
	 * @throws IOException
	 */
	public GeoTessModelSiteData(String modelInputFile) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, "");
	}

	/**
	 * Construct a new GeoTessModelSiteData object and populate it with information from
	 * the specified DataInputStream.  The GeoTessGrid will be read directly from
	 * the inputStream as well.
	 * @param inputStream
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public GeoTessModelSiteData(DataInputStream inputStream) throws GeoTessException, IOException
	{
		super();
		loadModelBinary(inputStream, null, "*");	
	}

	/**
	 * Construct a new GeoTessModelSiteData object and populate it with information from
	 * the specified Scanner.  The GeoTessGrid will be read directly from
	 * the inputScanner as well.
	 * @param inputScanner
	 * @throws GeoTessException
	 * @throws IOException
	 */
	public GeoTessModelSiteData(Scanner inputScanner) throws GeoTessException, IOException
	{
		super();
		loadModelAscii(inputScanner, null, "*");
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
	 * <li>setLayerNames()
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
	 *            MetaData the new GeoTessModel instantiates a reference to the
	 *            supplied metaData. No copy is made.
	 * @throws IOException
	 */
	public GeoTessModelSiteData(String gridFileName, GeoTessMetaData metaData, AttributeDataDefinitions attributes) throws IOException
	{ 
		super(gridFileName, metaData); 
		this.siteDataAttributes = attributes;
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
	 * <li>setLayerNames()
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
	 *            MetaData the new GeoTessModel instantiates a reference to the
	 *            supplied metaData. No copy is made.
	 * @throws IOException
	 */
	public GeoTessModelSiteData(File gridFileName, GeoTessMetaData metaData, AttributeDataDefinitions attributes) throws IOException
	{ 
		super(gridFileName, metaData); 
		this.siteDataAttributes = attributes;
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
	 * <li>setLayerNames()
	 * <li>setAttributes()
	 * <li>setDataType()
	 * <li>setLayerTessIds() (only required if grid has more than one
	 * multi-level tessellation)
	 * <li>setSoftwareVersion()
	 * <li>setGenerationDate()
	 * </ul>
	 * 
	 * @param grid
	 *            a reference to the GeoTessGrid that will support this
	 *            GeoTessModel.
	 * @param metaData
	 *            MetaData the new GeoTessModel instantiates a reference to the
	 *            supplied metaData. No copy is made.
	 * @throws GeoTessException
	 *             if metadata is incomplete.
	 */
	public GeoTessModelSiteData(GeoTessGrid grid, GeoTessMetaData metaData) throws Exception
	{ 
		super(grid, metaData); 
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
	 * <li>setLayerNames()
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
	 *            MetaData the new GeoTessModel instantiates a reference to the
	 *            supplied metaData. No copy is made.
	 * @throws GeoTessException
	 *             if metadata is incomplete.
	 */
	public GeoTessModelSiteData(GeoTessGrid grid, GeoTessMetaData metaData, AttributeDataDefinitions attributes) throws GeoTessException, IOException
	{ 
		super(grid, metaData); 
		this.siteDataAttributes = attributes;
	}

	/**
	 * Construct a new GeoTessModelSiteTerms with all the structures from the supplied
	 * baseModel.  The new GeoTessModelSiteTerms will be built with references to the 
	 * GeoTessMetaData, GeoTessGrid and all the Profiles in the baseModel.  
	 * No copies are made. Changes to one will be reflected in the other.  
	 * All of the extraData will be set to default values.
	 * 
	 * @param baseModel
	 * @throws GeoTessException
	 */
	public GeoTessModelSiteData(GeoTessModel baseModel, AttributeDataDefinitions attributes) throws GeoTessException
	{
		super(baseModel.getGrid(), baseModel.getMetaData());
		for (int i = 0; i < baseModel.getNVertices(); ++i)
			for (int j = 0; j < baseModel.getNLayers(); ++j)
				setProfile(i,j,baseModel.getProfile(i, j));
		this.siteDataAttributes = attributes;
	}

	/**
	 * Overridden IO method.
	 */
	@Override
	protected void loadModelBinary(DataInputStream input,
			String inputDirectory, String relGridFilePath)
					throws GeoTessException, IOException
	{
		// call super class to load model data from binary file.
		super.loadModelBinary(input, inputDirectory, relGridFilePath);

		if (input.available() == 0)
			throw new IOException(String.format("End of file encountered before reading SiteTerm data.  "
					+ "%s is a GeoTessModel but is not a %s", getMetaData().getInputModelFile().getName(), this.getClass().getSimpleName()));

		// it is good practice, but not required, to store the class
		// name as the first thing added by the extending class.
		String className = GeoTessUtils.readString(input);
		if (!className.equals(this.getClass().getSimpleName()))
			throw new IOException("Found class name " + className +
					" but expecting " +
					this.getClass().getSimpleName());

		// it is good practice, but not required, to store a format 
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		int formatVersion = input.readInt();

		if (formatVersion > 2)
			throw new IOException("Format version "+formatVersion+" is not supported.  "
					+ "It is likely that there is a later version of GeoTessModelSiteData that does support this version.");

		// read the site terms into a map of (String)sta -> ArrayList<SiteTerm>
		// site terms exist in the file if the parameter nSiteTerms = -1 and it
		// made it to this point without throwing an error. Otherwise nSiteTerms
		// will be zero and the input file will be a GeoTessModel which contains
		// no site terms

		String sta;
		ArrayList<SiteData> st;
		siteDataAttributes.readAll(input);	
		nSiteTerms = input.readInt();
		for (int i = 0; i < nSiteTerms; ++i)
		{
			sta = SiteData.readStationName(input);
			st = rcvrSiteTerms.get(sta);
			if (st == null)
			{
				st = new ArrayList<SiteData>();
				rcvrSiteTerms.put(sta, st);
			}
			st.add(new SiteData(input, getMetaData().getEarthShape(), siteDataAttributes, formatVersion));
		}
	}

	/**
	 * Output method to dump site terms for each supported attribute (column) and
	 * for all supported receivers (rows).
	 * 
	 * @return String formatted table of site terms.
	 */
	public String outputSiteTerms()
	{
		String s = "";
		s = "Site Name " + Globals.repeat(" ",  10);
		int nattr = siteDataAttributes.getNAttributes();
		for (int i = 0; i < nattr; ++i)
		{
			s += siteDataAttributes.getAttributeName(i);
			if (i < nattr-1) s += Globals.repeat(" ", 15);
		}
		s += Globals.NL + Globals.repeat("-", 60) + Globals.NL + Globals.NL;

		for (Map.Entry<String, ArrayList<SiteData>> e: rcvrSiteTerms.entrySet())
		{
			s += String.format("%8s   ", e.getKey());
			SiteData sd = e.getValue().get(0);
			for (int i = 0; i < nattr; ++i)
				s += String.format("%20.5e   ", sd.getSiteTerm(i));
			s += Globals.NL;
		}
		return s;
	}

	/**
	 * Overridden IO method.
	 */
	@Override
	protected void writeModelBinary(DataOutputStream output, String gridFileName)
			throws IOException
	{
		// call super class to write standard model information to binary file.
		super.writeModelBinary(output, gridFileName);

		// it is good practice, but not required, to store the class
		// name as the first thing added by the extending class.
		GeoTessUtils.writeString(output, this.getClass().getSimpleName());

		// it is good practice, but not required, to store a format 
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		output.writeInt(2);

		// write application specific data in binary format.
		siteDataAttributes.writeAll(output);
		output.writeInt(nSiteTerms);
		for (Map.Entry<String, ArrayList<SiteData>> entry : rcvrSiteTerms.entrySet())
			for (SiteData siteTerm : entry.getValue())
				siteTerm.write(output, entry.getKey(), getMetaData().getEarthShape());

		output.flush();
	}

	/**
	 * Overridden IO method.
	 */
	@Override
	protected void loadModelAscii(Scanner input, String inputDirectory,
			String relGridFilePath) throws GeoTessException, IOException
	{
		super.loadModelAscii(input, inputDirectory, relGridFilePath);

		if (!input.hasNext())
			throw new IOException("End of file encountered before reading SiteTerm data.  "
					+ "This GeoTessModel is not a "+this.getClass().getSimpleName());

		// it is good practice, but not required, to store the class
		// name as the first thing added by the extending class.
		String className = input.nextLine();
		if (!className.equals(this.getClass().getSimpleName()))
			throw new IOException("Found class name " + className +
					" but expecting " +
					this.getClass().getSimpleName());

		// it is good practice, but not required, to store a format 
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		int formatVersion = input.nextInt();
		input.nextLine();

		if (formatVersion > 2)
			throw new IOException("Format version "+formatVersion+" is not supported.");

		// read the site terms into a map of (String)sta -> ArrayList<SiteTerm>
		// site terms exist in the file if the parameter nSiteTerms = -1 and it
		// made it to this point without throwing an error. Otherwise nSiteTerms
		// will be zero and the input file will be a GeoTessModel which contains
		// no site terms

		String sta;
		ArrayList<SiteData> st;
		siteDataAttributes.readAll(input);
		nSiteTerms = input.nextInt();
		for (int i=0; i<nSiteTerms; ++i)
		{
			sta = SiteData.readStationName(input);
			st = rcvrSiteTerms.get(sta);
			if (st == null)
			{
				st = new ArrayList<SiteData>();
				rcvrSiteTerms.put(sta, st);
			}
			st.add(new SiteData(input, getMetaData().getEarthShape(), siteDataAttributes, formatVersion));
		}		
	}

	/**
	 * Overridden IO method.
	 */
	@Override
	protected void writeModelAscii(Writer output, String gridFileName)
			throws IOException
	{
		// call super class to write standard model information to ascii file.
		super.writeModelAscii(output, gridFileName);

		// it is good practice, but not required, to store the class
		// name and a format version number as the first things added 
		// by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		output.write(String.format("%s%n%d%n", this.getClass().getSimpleName(), 2));

		// write application specific data in ascii format.
		siteDataAttributes.writeAll(output);
		output.write(String.format("%d%n", nSiteTerms));
		for (Map.Entry<String, ArrayList<SiteData>> entry : rcvrSiteTerms.entrySet())
			for (SiteData siteTerm : entry.getValue())
				siteTerm.write(output, entry.getKey(), getMetaData().getEarthShape());
		output.flush();
	}


	/**
	 * Returns the total number of site terms in the model, including
	 * multiple site terms from the same station that has more than one
	 * ondate/offdate time range.
	 * 
	 * @return The total number of site terms in the model, including
	 *         multiple site terms from the same station that has more than one
	 *         ondate/offdate time range.
	 */
	public int getNSiteTerms()
	{
		return nSiteTerms;
	}

	/**
	 * Return the number of stations that have site terms stored in the model.
	 * 
	 * @return The number of stations that have site terms stored in the model.
	 */
	public int getNSiteTermStations()
	{
		return rcvrSiteTerms.size();
	}

	/**
	 * Clear all the site terms stored in this GeoModel
	 */
	public void clearSiteTerms()
	{
		setSiteTerms(new TreeMap<String, ArrayList<SiteData>>());
	}

	/**
	 * Clear all the site terms associated with station sta.
	 * 
	 * @param sta The station name whose site terms will be cleared.
	 */
	public void clearSiteTerms(String sta)
	{
		rcvrSiteTerms.remove(sta);
	}

	/**
	 * Specify a set of site terms to attach to this GeoTessModelSiteTerms.  
	 * Map is from a station name to a list of SiteTerm objects that 
	 * should be associated with that station name.  The list contains
	 * SiteTerm objects with different on-off dates.  On-off dates 
	 * should not overlap.
	 * 
	 * @param siteTerms The site terms to be attached to this GeoTessModelSiteTerms
	 *                  object.
	 */
	public void setSiteTerms(Map<String, ArrayList<SiteData>> siteTerms)
	{
		rcvrSiteTerms = siteTerms;
		nSiteTerms = 0;
		for (ArrayList<SiteData> list : siteTerms.values())
			nSiteTerms += list.size();
	}

	/**
	 * Replace the site terms currently stored in the model with new values
	 * read in from a file.  File format is:
	 * 
	 * <br> sta lat(deg) lon(deg) elev(km) ondate offdate siteterm(sec)
	 * 
	 * <p>ondate and offdate are treated specially.  If the string can
	 * be parsed to an integer, it is assumed to be a jdate. Otherwise,
	 * if it can be parsed to a double, it is interpreted as an epoch time.
	 * Otherwise, an attempt is made to parse it as a datetime string with
	 * format 'yyyy-MM-dd hh:mm:ss'.
	 * 
	 * Here is a sample db query to retrieve this stuff:
	 * 
	 * select sta, lat, lon, elevation, ts2jdate(starttime) as ondate,
	 *        ts2jdate(endtime) as offdate, sterm from
	 *        gmpreadonly.locdb_200905_receiver r, locdb0905_90b_siteterm sterm
	 *        where sterm.RECEIVERID=r.receiverid and r.receiverid in 
	 *        (select unique(receiverid) from gmpreadonly.locdb_200905_receiver)
	 *        order by sta, starttime;
	 * 
	 * 
	 * @param input The ASCII file containing the site terms.
	 * @throws GMPException 
	 * @throws IOException 
	 */
	public void setSiteTerms(Scanner input) throws GMPException, IOException
	{
		rcvrSiteTerms.clear();
		nSiteTerms = 0;

		EarthShape es = getMetaData().getEarthShape();

		while (input.hasNext())
		{
			String sta = input.next().trim();
			SiteData siteTerm = new SiteData(input, es, siteDataAttributes);

			ArrayList<SiteData> list = rcvrSiteTerms.get(sta);
			if (list == null)
			{
				list = new ArrayList<SiteData>();
				rcvrSiteTerms.put(sta, list);
			}
			list.add(siteTerm);
			++nSiteTerms;
			//System.out.printf("%6d %8s %s%n", nSiteTerms, sta, siteTerm);
		}
		input.close();
	}

	/**
	 * Add a Site with an associated siteTerm to this GeoModel. The site
	 * object must be capable of supplying valid sta, position, onDate and
	 * offDate information.
	 * 
	 * @param site The input site to be added to this model.
	 * @param siteTerm Array of site terms (seconds).
	 * @throws IOException
	 */
	public void addSiteTerm(Site site, double[] siteTerm)
			throws IOException
	{
		if (siteDataAttributes.getNAttributes() != siteTerm.length)
			throw new IOException("Attribute storage size (" +
					siteDataAttributes.getNAttributes() + ") is not " +
					" equal to input site term array length (" +
					siteTerm.length + ") ...");

		ArrayList<SiteData> list = rcvrSiteTerms.get(site.getSta());
		if (list == null)
		{
			list = new ArrayList<SiteData>();
			rcvrSiteTerms.put(site.getSta(), list);
		}
		SiteData sd = new SiteData(site, siteDataAttributes);
		for (int i = 0; i < siteTerm.length; ++i) sd.setSiteTerm(i, siteTerm[i]);
		list.add(sd);
		++nSiteTerms;
	}

	/**
	 * Add a Site definition with an associated siteTerm to this GeoModel.
	 *  
	 * @param sta           The site name.
	 * @param staUnitVector The lateral unit vector position of the site.
	 * @param staRadius     The site radius (km).
	 * @param onDate     The site's onDate (jdate).
	 * @param offDate    The site's offDate (jdate).
	 * @param siteTerm      Array of site terms (seconds).
	 */
	public void addSiteTerm(String sta, double[] staUnitVector, double staRadius,
			int onDate, int offDate, double[] siteTerm)
					throws IOException
	{
		if (siteDataAttributes.getNAttributes() != siteTerm.length)
			throw new IOException("Attribute storage size (" +
					siteDataAttributes.getNAttributes() + ") is not " +
					" equal to input site term array length (" +
					siteTerm.length + ") ...");

		ArrayList<SiteData> list = rcvrSiteTerms.get(sta);
		if (list == null)
		{
			list = new ArrayList<SiteData>();
			rcvrSiteTerms.put(sta, list);
		}
		SiteData sd = new SiteData(staUnitVector, staRadius, onDate, 
				offDate, siteDataAttributes);
		for (int i = 0; i < siteTerm.length; ++i) sd.setSiteTerm(i, siteTerm[i]);
		list.add(sd);
		++nSiteTerms;
	}

	/**
	 * Returns this models site term map.
	 * 
	 * @return This models site term map.
	 */
	public Map<String, ArrayList<SiteData>> getSiteTermMap()
	{
		return rcvrSiteTerms;
	}

	/**
	 * Returns the site term associated with the input station name and epoch
	 * time (seconds). The input epoch time lies within the sites on time and
	 * off time.
	 * 
	 * @param station   The name of the station whose site term is returned.
	 * @param epochTime The epoch time that lies between the sites on time and
	 *                  off time.
	 * @return The site term (in seconds) for the specified station at the 
	 *         specified epoch time.
	 */
	public double getSiteTerm(int attributeIndex, String station, double epochTime)
	{
		// find the set of SiteTerm objects for the station.
		ArrayList<SiteData> list = rcvrSiteTerms.get(station);
		if (list != null)
		{
			// if one is found that has on/off times that includes the 
			// arrival time at the station, return the site term value.
			for (SiteData st : list)
				if (st.inRange(GMTFormat.getJDate(epochTime)))
					return st.getSiteTerm(attributeIndex);

		}
		return Globals.NA_VALUE;
	}

	/**
	 * Returns the site term (in seconds) for the specified station name (sta),
	 * travel time (tt in seconds), and origin time (originTime in seconds).
	 * These values can be readily extracted from a PredictionInterface object
	 * for input in this method.  If anything goes wrong, returns Globals.NA_VALUE
	 * (-999999.)
	 * 
	 * @return The site term (in seconds) for the specified station name (sta),
	 *         travel time (tt in seconds), and origin time (originTime in
	 *         seconds).
	 */
	public double getSiteTerm(int attributeIndex, String sta, double tt,
			double originTime)
	{
		// if the predicted travel time is na value, return na value.
		//double tt = prediction.getAttribute(GeoAttributes.TRAVEL_TIME); //PredictionInterface
		if (tt == Globals.NA_VALUE)
			return Globals.NA_VALUE;

		return getSiteTerm(attributeIndex, sta, tt+originTime);
	}

	/**
	 * Returns a String containing this models site term history for the input
	 * station name.
	 * 
	 * @param station The station name whose site term history is returned.
	 * @return A String containing this models station specific site term history.
	 */
	public String getSiteTermHistory(String station)
	{
		StringBuffer buf = new StringBuffer();
		if (rcvrSiteTerms.get(station) != null)
			for (SiteData sterm : rcvrSiteTerms.get(station))
				buf.append(String.format("%-8s %s%n", station, sterm));
		else 
			buf.append(String.format("%-8s no information%n", station));
		return buf.toString();
	}

	/**
	 * Returns a String containing this models site term history.
	 * 
	 * @return A String containing this models site term history.
	 */
	public String getSiteTermHistory()
	{
		StringBuffer buf = new StringBuffer();
		if (rcvrSiteTerms.size() == 0)
			buf.append(String.format("Model contains no site term information%n"));
		else
			for (String station : rcvrSiteTerms.keySet())
				for (SiteData sterm : rcvrSiteTerms.get(station))
					buf.append(String.format("%-8s %s%n", station, sterm));
		return buf.toString();
	}

	/**
	 * Compares this GeoTessModelSiteTerms object with the input object and
	 * returns true if they are identical.
	 * 
	 * @return True if the input model and this model are identical.
	 */
	@Override
	public boolean equals (Object other)
	{
		// return false if the input object is null or not a SiteData
		// object
		if ((other == null) || !(other instanceof GeoTessModelSiteData))
			return false;

		// compare GeoTessModel base class for equivalency.
		if (!super.equals(other)) return false;

		// get input models site term map and compare against this models site
		// term map.
		Map<String, ArrayList<SiteData>> otherSiteTerms;
		otherSiteTerms = ((GeoTessModelSiteData) other).rcvrSiteTerms;
		if (!equals(otherSiteTerms)) return false;

		// models are identical ... return true.
		return true;
	}

	/**
	 * Private method used compare the site term map from this object
	 * (rcvrSiteTerms) with the input map. If identical true is returned.
	 * 
	 * @param otherSiteTerms The site term map to be compared with this
	 *                       site term map.
	 * @return True if the input site term map and this site term map are
	 *         identical.
	 */
	private boolean equals(Map<String, ArrayList<SiteData>> otherSiteTerms)
	{
		// if the input map is null or has a different number of entries
		// return false
		if ((otherSiteTerms == null) ||
				(otherSiteTerms.size() != rcvrSiteTerms.size())) return false;

		// loop over each entry and check that they are identical
		for (Map.Entry<String, ArrayList<SiteData>> lst: rcvrSiteTerms.entrySet())
		{
			// get other list and this list
			ArrayList<SiteData> otherList = otherSiteTerms.get(lst.getKey());
			ArrayList<SiteData> thisList  = lst.getValue();

			// if other list is null or its entry count is a different size
			// return false
			if ((otherList == null) ||
					(otherList.size() != thisList.size())) return false;

			// loop over each entry of the current list and make sure the
			// other and this ReceiverSiteTerms are identical
			for (int i = 0; i < thisList.size(); ++i)
			{
				// get the next ReceiverSiteTerms object from this and the other list
				// and check that they are identical
				SiteData thisRST  = thisList.get(i);
				SiteData otherRST = otherList.get(i);
				if (!thisRST.equals(otherRST))
					return false;
			}
		}

		// site term maps are identical ... return true
		return true;
	}

	public int getNSiteTermAttributes()
	{
		return siteDataAttributes.getNAttributes();
	}

	public AttributeDataDefinitions getSiteTermAttributes()
	{
		return siteDataAttributes;
	}

	/**
	 * Defines the names of the types of site terms stored by this model (usually
	 * wave velocity specific). Data are created and initialized (0) for each
	 * specific attribute name. They must be set by calling the method
	 * setSiteTerms(attrIndex, siteTerm) for each attribute.
	 *  
	 * @param names Array of attribute names.
	 * @param units Array of attribute units.
	 */
	public void setSiteAttributes(String[] names, String[] units, DataType dataType)
	{
		siteDataAttributes = new AttributeDataDefinitions(names, units, dataType);
	}

	/**
	 * Defines the names of the types of site terms stored by this model (usually
	 * wave velocity specific). Data are created and initialized (0) for each
	 * specific attribute name. They must be set by calling the method
	 * setSiteTerms(attrIndex, siteTerm) for each attribute.
	 *  
	 * @param names List of attribute names separated by ";".
	 * @param units List of attribute units separated by ";".
	 */
	public void setSiteAttributes(String names, String units, DataType dataType)
	{
		siteDataAttributes = new AttributeDataDefinitions(names, units, dataType);
	}

	@Override
	public String toString()
	{
		StringBuffer s = new StringBuffer(super.toString());
		s.append(String.format("getNSiteTermAttributes()() = %d%n", getNSiteTermAttributes()));
		s.append(siteDataAttributes.getAttributeNamesString()+"\n");
		s.append(String.format("getNSiteTermStations() = %d%n", getNSiteTermStations()));
		return s.toString();
	}

}
