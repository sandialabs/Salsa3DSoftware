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
package gov.sandia.geotess.extensions.amplitude;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessUtils;
import gov.sandia.geotess.PointMap;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerDouble;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerDouble.Iterator;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;

/**
 * This class extends GeoTessModel 
 * @author sballar
 *
 */
public class GeoTessModelAmplitude extends GeoTessModel 
{
	/**
	 * The seismic phase supported by this model.
	 */
	private String phase;

	/**
	 * Map from a String representation of a frequency band, to the 
	 * same information stored as a 2 element array of doubles.  
	 * For example, key = '1.0_2.0'; value = [1.0, 2.0]
	 */
	private LinkedHashMap<String, float[]> frequencyMap;

	/**
	 * Map from station -&gt; channel -&gt; band -&gt; siteTran
	 */
	private Map<String, Map<String, Map<String, Float>>> siteTrans;

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
		GeoTessModelAmplitude o = (GeoTessModelAmplitude)other;
		this.phase = o.phase;    
		this.frequencyMap = o.frequencyMap;
		this.siteTrans = o.siteTrans;
	}
    
	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
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
	public GeoTessModelAmplitude(File modelInputFile, String relativeGridPath) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, relativeGridPath);
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
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
	public GeoTessModelAmplitude(File modelInputFile) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, "");
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
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
	public GeoTessModelAmplitude(String modelInputFile, String relativeGridPath) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, relativeGridPath);
	}

	/**
	 * Construct a new GeoTessModel object and populate it with information from
	 * the specified file.
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
	public GeoTessModelAmplitude(String modelInputFile) throws IOException
	{ 
		super(); 
		loadModel(modelInputFile, "");
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
	 * </ul>
	 * 
	 * @param gridFileName
	 *            name of file from which to load the grid.
	 * @param metaData
	 *            MetaData the new GeoTessModel instantiates a reference to the
	 *            supplied metaData. No copy is made.
	 * @throws IOException
	 */
	public GeoTessModelAmplitude(String gridFileName, GeoTessMetaData metaData) throws IOException
	{ 
		super(gridFileName, metaData); 
		initializeData();
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
	public GeoTessModelAmplitude(GeoTessGrid grid, GeoTessMetaData metaData) throws GeoTessException, IOException
	{ 
		super(grid, metaData); 
		initializeData();
	}

	/**
	 * Construct a new AmpTomoModel with all the structures from the supplied
	 * baseModel.  The new AmpTomoModel will be built with references to the 
	 * GeoTessMetaData, GeoTessGrid and all the Profiles in the baseModel.  
	 * No copies are made. Changes to one will be reflected in the other.  
	 * All of the extraData will be set to default values.
	 * @param baseModel
	 * @throws GeoTessException
	 */
	public GeoTessModelAmplitude(GeoTessModel baseModel) throws GeoTessException
	{
		super(baseModel.getGrid(), baseModel.getMetaData());
		for (int i = 0; i < baseModel.getNVertices(); ++i)
			for (int j=0; j<baseModel.getNLayers(); ++j)
				setProfile(i,j,baseModel.getProfile(i, j));

		initializeData();
	}

	/**
	 * Protected method to initialize siteTrans.  
	 */
	private void initializeData()
	{
		phase = "?";
		siteTrans = new HashMap<String, Map<String, Map<String, Float>>>();
		frequencyMap = new LinkedHashMap<String, float[]>(20);
		
		refreshFrequencyMap();
	}
	
	/**
	 * Get the current version number
	 * @return the current version number
	 */
	public static String getVersion() { return "1.1.1"; }

	/**
	 * Retrieve the value of effective Q for the specified frequency band,
	 * integrated along the great circle path from pointA to pointB
	 * @param latA geographic latitude of the start of the great circle path.  Units depend on the value of inDegrees.
	 * @param lonA longitude of the start of the great circle path.  Units depend on the value of inDegrees.
	 * @param latB geographic latitude of the end of the great circle path.  Units depend on the value of inDegrees.
	 * @param lonB longitude of the end of the great circle path.  Units depend on the value of inDegrees.
	 * @param inDegrees if true, lats and lons are assumed to be in degrees, otherwise radians.
	 * @param band the frequency band, eg., "1.0-2.0"
	 * @return the value of effective Q for the specified frequency band
	 * @throws GeoTessException
	 */
	public double getPathQ(double latA, double lonA, double latB, double lonB, boolean inDegrees, String band) 
			throws GeoTessException
	{
		double[] pointA, pointB;
		if (inDegrees)
		{
			pointA = getEarthShape().getVectorDegrees(latA,lonA);
			pointB = getEarthShape().getVectorDegrees(latB,lonB);
		}
		else
		{
			pointA = getEarthShape().getVector(latA,lonA);
			pointB = getEarthShape().getVector(latB,lonB);
		}
		return getPathQ(new GreatCircle(pointA, pointB), band, InterpolatorType.LINEAR);
	}

	/**
	 * Retrieve the value of effective Q for the specified frequency band,
	 * integrated along the great circle path from pointA to pointB
	 * @param path great circle path along which to compute Q
	 * @param band the frequency band, e.g., "1.0_2.0"
	 * @param interpolatorType either InterpolatorType.LINEAR or InterpolatorType.NATURAL_NEIGHBOR.
	 * @return the value of effective Q for the specified frequency band
	 * @throws GeoTessException
	 */
	public double getPathQ(GreatCircle path, String band, InterpolatorType interpolatorType) 
			throws GeoTessException
	{
		int level = getGrid().getNLevels(0)-1;
		// convert level index into grid spacing in degrees.
		double gridSpacing = GeoTessUtils.getEdgeLength(level); //exp(log(64)-level*log(2));
		// set the integration interval to 1/10th the grid spacing, in radians
		double integrationInterval = Math.toRadians(gridSpacing/10.);

		HashMapIntegerDouble weights = new HashMapIntegerDouble(1000);
		getWeights(path, integrationInterval, -1., interpolatorType, weights);
		int pointIndex;
		Iterator it = weights.iterator();
		double weight, integral = 0, pathLength=0.;
		PointMap pm = getPointMap();

		int bandIndex = getMetaData().getAttributeIndex(String.format("Q[%s]", band));
		if (bandIndex >= 0)
		{
			while(it.hasNext())
			{
				gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerDouble.Entry entry = it.nextEntry();
				pointIndex = entry.getKey();
				weight = entry.getValue();
				integral += weight/pm.getPointValueFloat(pointIndex, bandIndex);
				pathLength += weight;
			}
			return pathLength/integral;
		}
		else 
		{
			int q0Index = getMetaData().getAttributeIndex("Q0");
			int etaIndex = getMetaData().getAttributeIndex("ETA");
			if (q0Index < 0 || etaIndex < 0)
				throw new GeoTessException(String.format("Model does not support attributes Q0/ETA or %s. Supported bands include:%n%s", 
						band, getMetaData().getAttributeNamesString()));

			float[] freq = frequencyMap.get(band);
			if (freq == null)
				throw new GeoTessException(String.format("%n%s is not a supported frequency band.%nSupported bands include:%n%s", 
						band, getMetaData().getAttributeNamesString()));

			double q0, eta, centerFrequency = Math.sqrt(freq[0]*freq[1]);

			while(it.hasNext())
			{
				gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerDouble.Entry entry = it.nextEntry();
				pointIndex = entry.getKey();
				weight = entry.getValue();
				q0 = pm.getPointValueFloat(pointIndex, q0Index);
				eta = pm.getPointValueFloat(pointIndex, etaIndex);
				integral += weight/(q0 * Math.pow(centerFrequency, eta));
				pathLength += weight;
			}
			return pathLength/integral;
		}
	}

	/**
	 * Retrieve the phase supported by this model
	 * @return the phase supported by this model
	 */
	public String getPhase() { return phase; }

	/**
	 * Specify the phase supported by this model
	 * @param phase the phase supported by this model
	 */
	public void setPhase(String phase) { this.phase = phase; }

	/**
	 * Setter specifying a map from station -&gt; channel -&gt; band -&gt; siteTran
	 * @param siteTrans
	 */
	public void setSiteTrans(Map<String, Map<String, Map<String, Float>>> siteTrans)
	{ this.siteTrans = siteTrans; }

	/**
	 * Get map from station -&gt; channel -&gt; band -&gt; siteTran
	 * @return extraData
	 */
	public Map<String, Map<String, Map<String, Float>>> getSiteTrans()
	{ return siteTrans; }

	/**
	 * Retrieve the site term for the specified station/channel/band 
	 * or NaN if not supported.
	 * @param station
	 * @param channel
	 * @param band
	 * @return the site term for the specified station/channel/band 
	 * or NaN if not supported.
	 */
	public float getSiteTrans(String station, String channel, String band)
	{
		if (station != null && channel != null && band != null)
		{
			Map<String, Map<String, Float>> m1 = siteTrans.get(station);
			if (m1 != null)
			{
				Map<String, Float> m2 = m1.get(channel);
				if (m2 != null)
				{
					Float value = m2.get(band);
					if (value != null)
						return value;
				}
			}
		}
		return Float.NaN;
	}
	
	/**
	 * Get the number of stations that have site term values
	 * @return the number of stations that have site term values
	 */
	public int getNStations() { return siteTrans.size(); }
	
	/**
	 * Return a reference to the set of stations that have site term values.
	 * @return a reference to the set of stations that have site term values.
	 */
	public Set<String> getStations() { return siteTrans.keySet(); }
	
	/**
	 * Determine whether or not the specified station has any site term values
	 * @param station
	 * @return whether or not the specified station has any site term values
	 */
	public boolean isSupportedStation(String station) { return siteTrans.containsKey(station); }

	/**
	 * Retrieve the number of channels that have site terms for the specified station
	 * @param station
	 * @return the number of channels that have site terms for the specified station
	 */
	public int getNChannels(String station) 
	{ 
		Map<String, Map<String, Float>> m = siteTrans.get(station);
		return m == null ? 0 : m.size();
	}
	
	/**
	 * Retrieve the set of channels supported by the specified station.
	 * @param station
	 * @return the set of channels supported by the specified station.
	 */
	public Set<String> getChannels(String station)
	{
		Map<String, Map<String, Float>> m = siteTrans.get(station);
		return m == null ? new HashSet<String>() : m.keySet();
	}
	
	/**
	 * Return true if the specified station-channel has any site term values
	 * @param station
	 * @param channel
	 * @return true if the specified station-channel has any site term values
	 */
	public boolean isSupportedChannel(String station, String channel) 
	{ 
		Map<String, Map<String, Float>> m = siteTrans.get(station);
		return m != null && m.containsKey(channel);
	}

	/**
	 * Return the number of frequency bands have site term values for the 
	 * specified station-channel
	 * @param station
	 * @param channel
	 * @return the number of frequency bands have site term values for the 
	 * specified station-channel
	 */
	public int getNBands(String station, String channel) 
	{ 
		Map<String, Map<String, Float>> m = siteTrans.get(station);
		if (m != null)
		{
			Map<String, Float> n = m.get(channel);
			return n == null ? 0 : n.size();
		}
		return 0;
	}
	
	/**
	 * Retrieve the set of bands supported by the specified station-channel.
	 * @param station
	 * @param channel
	 * @return the set of bands supported by the specified station-channel.
	 */
	public Set<String> getBands(String station, String channel)
	{
		Map<String, Map<String, Float>> m = siteTrans.get(station);
		if (m != null)
		{
			Map<String, Float> n = m.get(channel);
			if (n != null)
				return n.keySet();
		}
		return new HashSet<String>();
	}
	
	/**
	 * Return true if there is a site term value available for the specified
	 * station-channel-band
	 * @param station
	 * @param channel
	 * @param band
	 * @return true if there is a site term value available for the specified
	 * station-channel-band
	 */
	public boolean isSupportedBand(String station, String channel, String band) 
	{ 
		Map<String, Map<String, Float>> m = siteTrans.get(station);
		if (m != null)
		{
			Map<String, Float> n = m.get(channel);
			if (n != null)
				return n.containsKey(band);
		}
		return false;
	}

	/**
	 * Map from a String representation of a frequency band, to the 
	 * same information stored as a 2 element array of doubles.  
	 * For example, key = '1.0_2.0'; value = [1.0, 2.0]
	 * @return the frequencyMap
	 */
	public LinkedHashMap<String, float[]> getFrequencyMap() { return frequencyMap; }

	/**
	 * Clear and repopulate the frequencyMap, which is a 
	 * Map from a String representation of a frequency band, to the 
	 * same information stored as a 2 element array of doubles.  
	 * For example, key = '1.0_2.0'; value = [1.0, 2.0]
	 * <p>
	 * This method should only be called after changes have been made to the 
	 * siteTrans container that would modify the set of frequency bands supported
	 * by this model.
	 * @return the refreshed frequencyMap
	 */
	public LinkedHashMap<String, float[]> refreshFrequencyMap() 
	{ 
		frequencyMap.clear();
		
		for (String attribute : getMetaData().getAttributeNames())
			if (attribute.startsWith("Q[") && attribute.endsWith("]"))
			{
				String band = attribute.substring(2, attribute.length()-1);
				String[] s = band.split("_");
				if (s.length == 2)
					frequencyMap.put(band, new float[] {Float.parseFloat(s[0]), Float.parseFloat(s[1])});
			}
		
		for (Entry<String, Map<String, Map<String, Float>>> m1 : siteTrans.entrySet())
			for (Entry<String, Map<String, Float>> m2 : m1.getValue().entrySet())
				for (String band : m2.getValue().keySet())
					if (!frequencyMap.containsKey(band))
					{
						String[] f = band.split("_");
						frequencyMap.put(band, new float[] {Float.parseFloat(f[0]), Float.parseFloat(f[1])});
					}
		return frequencyMap; 
	}

	/**
	 * Get a deep copy of the siteTrans.
	 * @return
	 */
	private Map<String, Map<String, Map<String, Float>>> copySiteTrans()
	{
//		Map from station -> channel -> band -> siteTran
//		HashMap<String, LinkedHashMap<String, LinkedHashMap<String, Float>>> siteTrans;
		
		Map<String, Map<String, Map<String, Float>>> copy = 
				new HashMap<String, Map<String, Map<String, Float>>>(siteTrans.size());
		
		for (Entry<String, Map<String, Map<String, Float>>> e1 : siteTrans.entrySet())
		{
			Map<String, Map<String, Float>> m1 = new LinkedHashMap<>(e1.getValue().size());
			this.siteTrans.put(e1.getKey(), m1);
			for (Entry<String, Map<String, Float>> e2 : e1.getValue().entrySet())
				m1.put(e2.getKey(), new LinkedHashMap<String, Float>(e2.getValue()));
		}
		return copy;
	}
	
	/**
	 * Retrieve basic information about this GeoTessModelAmplitude object.
	 * @return  basic information about this GeoTessModelAmplitude object.
	 */
	@Override
	public String toString()
	{
		StringBuffer s = new StringBuffer();
		s.append(super.toString());
		
		s.append("\nAvailable frequency bands:\n");
		for (String band : frequencyMap.keySet())
			s.append(band).append("\n");
		s.append("\n");
		
		s.append("Stations with siteTran values:\n");
		ArrayList<String> stations = new ArrayList<String>(siteTrans.keySet());
		Collections.sort(stations);
		int len=0;
		for (String station : stations)
		{
			len += station.length()+1;
			if (len > 110) { s.append('\n'); len = 0; }
			s.append(station).append(" ");
		}
		s.append("\n\n");
		
		return s.toString();
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

		// it is good practice, but not required, to store the class
		// name as the first thing added by the extending class.
		String className = GeoTessUtils.readString(input);
		if (!className.equals(this.getClass().getSimpleName()))
			throw new IOException("Found class name "+className
					+" but expecting "
					+this.getClass().getSimpleName());

		// it is good practice, but not required, to store a format 
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		int formatVersion = input.readInt();

		initializeData();
			
		if (formatVersion == 1)
		{
			phase = GeoTessUtils.readString(input);
			int ni = input.readInt();
			for (int i=0; i<ni; ++i)
			{
				String station = GeoTessUtils.readString(input);
				Map<String, Map<String, Float>> m1 = new LinkedHashMap<String, Map<String,Float>>();
				siteTrans.put(station, m1);

				int nj = input.readInt();
				for (int j=0; j<nj; ++j)
				{
					String channel = GeoTessUtils.readString(input);
					Map<String, Float> m2 = new LinkedHashMap<String, Float>();
					m1.put(channel, m2);

					int nk = input.readInt();
					for (int k=0; k<nk; ++k)
					{
						String band = GeoTessUtils.readString(input);
						m2.put(band, input.readFloat());
					}
				}
			}
			refreshFrequencyMap();
		}
		else
			throw new IOException("Format version "+formatVersion+" is not supported.");
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
		output.writeInt(1);

		GeoTessUtils.writeString(output, phase);
		output.writeInt(siteTrans.size());
		for (Entry<String, Map<String, Map<String, Float>>> m1 : siteTrans.entrySet())
		{
			GeoTessUtils.writeString(output, m1.getKey());
			output.writeInt(m1.getValue().size());
			for (Entry<String, Map<String, Float>> m2 : m1.getValue().entrySet())
			{
				GeoTessUtils.writeString(output, m2.getKey());
				output.writeInt(m2.getValue().size());
				for (Entry<String, Float> m3 : m2.getValue().entrySet())
				{
					GeoTessUtils.writeString(output, m3.getKey());
					output.writeFloat(m3.getValue());
				}
			}
		}
	}

	/**
	 * Overridden IO method.
	 */
	@Override
	protected void loadModelAscii(Scanner input, String inputDirectory,
			String relGridFilePath) throws GeoTessException, IOException
	{
		super.loadModelAscii(input, inputDirectory, relGridFilePath);

		// it is good practice, but not required, to store the class
		// name as the first thing added by the extending class.
		String className = input.nextLine();
		if (!className.equals(this.getClass().getSimpleName()))
			throw new IOException("Found class name "+className
					+" but expecting "
					+this.getClass().getSimpleName());

		// it is good practice, but not required, to store a format 
		// version number as the second thing added by the extending class.
		// With this information, if the format changes in a future release
		// it may be possible to make the class backward compatible.
		int formatVersion = input.nextInt();
		input.nextLine();

		initializeData();

		if (formatVersion == 1)
		{
			String station, channel, band;
			float value;
			Scanner scn;

			phase = input.nextLine();
			int count = input.nextInt();
			input.nextLine();
			for (int i=0; i<count; ++i)
			{
				scn = new Scanner(input.nextLine());
				station = scn.next();
				channel = scn.next();
				band = scn.next();
				value = scn.nextFloat();
				Map<String, Map<String, Float>> channelMap = siteTrans.get(station);
				if (channelMap == null)
				{
					channelMap = new LinkedHashMap<String, Map<String,Float>>();
					siteTrans.put(station, channelMap);
				}
				Map<String, Float> bandMap = channelMap.get(channel);
				if (bandMap == null)
				{
					bandMap = new LinkedHashMap<String, Float>();
					channelMap.put(channel, bandMap);
				}
				bandMap.put(band, value);
			}
			refreshFrequencyMap();
		}
		else
			throw new IOException("Format version "+formatVersion+" is not supported.");
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
		output.write(String.format("%s%n%d%n", this.getClass().getSimpleName(), 1));

		// write application specific data in ascii format.
		output.write(String.format("%s%n", phase));
		output.write(String.format("%d%n", getNStations()));
		for (Entry<String, Map<String, Map<String, Float>>> m1 : siteTrans.entrySet())
		{
			String station = m1.getKey();
			for (Entry<String, Map<String, Float>> m2 : m1.getValue().entrySet())
			{
				String channel = m2.getKey();
				for (Entry<String, Float> m3 : m2.getValue().entrySet())
				{
					String band = m3.getKey();
					float siteTran = m3.getValue();
					output.write(String.format("%-6s %-6s %-12s %s%n",
							station, channel, band, Float.toString(siteTran)));
				}
			}
		}
	}

}
