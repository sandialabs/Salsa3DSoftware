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
package gov.sandia.geotess.examples.customdata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import gov.sandia.geotess.Data;
import gov.sandia.geotess.DataCustom;
import gov.sandia.geotess.GeoTessUtils;
import gov.sandia.gmp.util.globals.DataType;

/**
 * GridPointData manages the information associated with a single point in 
 * a GeoTessModel.  GridPointData has information about GeoAttributes at a
 * bunch of stations and phases.  It is stored in a map 
 * station -&gt; phase -&gt; GeoAttributes -&gt; value.
 * @author sandy
 */
public class GridPointData extends DataCustom implements Map<String, StationData>
{
	/**
	 * Horizontal radius of the cell centered on the grid point, in degrees or maybe km(?)
	 */
	double cellRadius;

	/**
	 * Upper limit of the depth range that encompasses the grid point, in km.
	 */
	double minDepth;

	/**
	 * Lower limit of the depth range that encompasses the grid point, in km.
	 */
	double maxDepth;

	/**
	 * Map from Station Name -&gt; Phase Name -&gt; GeoAttribute -&gt; dataValue
	 */
	private Map<String, StationData> dataValues;
	
	/**
	 * A list of all the attributes supported by this model.
	 * All of these attributes will have values in each PhaseData map and will
	 * be read from, and written to the output files.
	 */
	private GeoAttributes[] attributes;

	/**
	 * Map from attribute index -&gt; [Station Name, Phase Name, Attribute Name]
	 */
	private List<Triple> indexList;

	/**
	 * 
	 */
	public GridPointData() 
	{
	}
	
	/**
	 * 
	 */
	public GridPointData(GeoTessModelGA model) 
	{
		this.dataValues = new HashMap<String, StationData>(model.stationNames.length);
		this.attributes = model.getAttributes();
		this.indexList = model.getIndexList();
	}
	
	public GridPointData(GeoTessModelGA model,
			double cellRadius,
			double minDepth,
			double maxDepth) 
	{
		this(model);
		this.cellRadius = cellRadius;
		this.minDepth = minDepth;
		this.maxDepth = maxDepth;
	}

	/**
	 * 
	 * @param input
	 * @param model
	 */
	protected GridPointData(Scanner input, GeoTessModelGA model) 
	{
		this(model);
		cellRadius = input.nextDouble();
		minDepth = input.nextDouble();
		maxDepth = input.nextDouble();
		int nStations = input.nextInt();
		for (int i=0; i<nStations; ++i)
		{
			String station = input.next();
			StationData stationData = new StationData(input, attributes);
			dataValues.put(station, stationData);
		}
	}

	/**
	 * 
	 * @param input
	 * @param model
	 * @throws IOException
	 */
	protected GridPointData(DataInputStream input, GeoTessModelGA model) throws IOException
	{
		this(model);

		cellRadius = input.readDouble();
		minDepth = input.readDouble();
		maxDepth = input.readDouble();
		int nStations = input.readInt();
		for (int i=0; i<nStations; ++i)
		{
			String station = GeoTessUtils.readString(input);
			StationData stationData = new StationData(input, attributes);
			dataValues.put(station, stationData);
		}
	}

	@Override
	public void write(DataOutputStream output) throws IOException {
		output.writeDouble(cellRadius);
		output.writeDouble(minDepth);
		output.writeDouble(maxDepth);
		output.writeInt(dataValues.size()); // number of stations
		for (Entry<String, StationData> entry : dataValues.entrySet())
		{
			GeoTessUtils.writeString(output, entry.getKey()); // station name
			entry.getValue().write(output, attributes); // StationData
		}
	}

	/**
	 * toString() is the format of the output to ascii files when
	 * a model is written to ascii file.  This text will be used to 
	 * construct a new GridPointData object from a Scanner.
	 */
	@Override
	public String toString()  {
		StringBuffer output = new StringBuffer();
		output.append(String.format("%1.6f %1.6f %1.6f%n", cellRadius, minDepth, maxDepth));
		output.append(String.format("%d%n", dataValues.size())); // number of stations
		for (Entry<String, StationData> e1 : dataValues.entrySet())
		{
			output.append(e1.getKey()+'\n'); // station name
			e1.getValue().write(output, attributes); // Station Data
		}
		return output.toString();
	}

	public double getCellRadius() {
		return cellRadius;
	}

	public void setCellRadius(double cellRadius) {
		this.cellRadius = cellRadius;
	}

	public double getMinDepth() {
		return minDepth;
	}

	public void setMinDepth(double minDepth) {
		this.minDepth = minDepth;
	}

	public double getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(double maxDepth) {
		this.maxDepth = maxDepth;
	}

	public Map<String, StationData> getDataValues() {
		return dataValues;
	}

	public void setDataValues(Map<String, StationData> dataValues) {
		this.dataValues = dataValues;
	}

	public List<Triple> getindexList() {
		return indexList;
	}

	public void setindexList(List<Triple> indexList) {
		this.indexList = indexList;
	}
	
	/**
	 * Returns true if this and other are of the same DataType, both have a
	 * single element and those elements are == (or both values are NaN).
	 * 
	 * @param other
	 * @return true if this and other are of the same DataType, both have a
	 *         single element and those elements are == (or both values are NaN).
	 */
	@Override
	public boolean equals(Object other)
	{
		if (other == null || !(other instanceof GridPointData))
			return false;

		// TODO have to write code here to check that every value and every 
		// index in every map is equal.
		return true;
	}

	@Override
	public Data getNew() {
		return new GridPointData();
	}

	@Override
	public Data[] getNew(int n) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getDataTypeString() {
		return this.getClass().getSimpleName();
	}

	@Override
	public DataType getDataType() {
		return DataType.DOUBLE;
	}

	@Override
	public int size() {
		return indexList.size(); 
	}

	/**
	 * Get the value of specified attributeIndex, 
	 * or NaN if does not exist.
	 */
	@Override
	public double getDouble(int attributeIndex) {
		Triple map = indexList.get(attributeIndex);
		StationData s = dataValues.get(map.station);
		if (s == null) return Double.NaN;
		PhaseData p = s.get(map.phase);
		if (p == null) return Double.NaN;
		Double a = p.get(map.attribute);
		if (a == null) return Double.NaN;
		return a.doubleValue();
	}

	@Override
	public Data setValue(int attributeIndex, double value) {
		// as written, this only allows modification of 
		// existing values, not addition of new values.
		Triple map = indexList.get(attributeIndex);
		StationData phaseMap = dataValues.get(map.station);
		if (phaseMap != null)
		{
			PhaseData attributeMap = phaseMap.get(map.phase);
			if (attributeMap != null)
				attributeMap.put(map.attribute, value);
		}
		return this;
	}

	/**
	 * Return a deep copy of this.
	 */
	@Override
	public Data copy() {
		// TODO: add code to return a deep copy of this
		return new GridPointData();
	}

	@Override
	public float getFloat(int attributeIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLong(int attributeIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getInt(int attributeIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public short getShort(int attributeIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte getByte(int attributeIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Data setValue(int attributeIndex, float value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Data setValue(int attributeIndex, long value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Data setValue(int attributeIndex, int value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Data setValue(int attributeIndex, short value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Data setValue(int attributeIndex, byte value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Data fill(Number fillValue) {
		throw new UnsupportedOperationException();
	}
	
	// Methods that override methods in Map interface

	@Override
	public boolean isEmpty() {
		return dataValues.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return dataValues.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return dataValues.containsValue(value); 
	}

	@Override
	public StationData get(Object key) {
		return dataValues.get(key);
	}

	@Override
	public StationData put(String key, StationData value) {
		return dataValues.put((String)key, (StationData)value); 
	}

	@Override
	public StationData remove(Object key) {
		return dataValues.remove(key);
	}

	@Override
	public void clear() {
		dataValues.clear();
	}

	@Override
	public Set<String> keySet() {
		return dataValues.keySet();
	}

	@Override
	public Collection<StationData> values() {
		return dataValues.values();
	}

	@Override
	public Set<Entry<String, StationData>> entrySet() {
		return dataValues.entrySet();
	}

	@Override
	public void putAll(Map<? extends String, ? extends StationData> m) {
		dataValues.putAll(m);
	}

}
