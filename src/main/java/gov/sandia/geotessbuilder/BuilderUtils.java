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
package gov.sandia.geotessbuilder;

import java.util.Arrays;
import java.util.EnumSet;

import gov.sandia.geotess.Data;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.PointMap;
import gov.sandia.geotess.Profile;
import gov.sandia.geotess.ProfileType;
import gov.sandia.gmp.util.containers.hash.sets.HashSetInteger;
import gov.sandia.gmp.util.containers.hash.sets.HashSetInteger.Iterator;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.numerical.vector.EarthShape;

/**
 * A collection of static methods that operate on GeoTessModels to modify their
 * structure in various ways. There are methods to:
 * <ul>
 * <li>changeAttributes - Downselect the set of attributes supported by the
 * model
 * <li>changeEarthShape - Change the ellipsoid that controls the radial
 * distribution of nodes.
 * <li>combineMajorLayers - Combine multiple layers of type CONSTANT or THIN
 * into a single layer.
 * <li>removeLayer - remove a layer from the model.
 * <li>resampleProfile -
 * <li>checkZeroThicknessLayers -
 * </ul>
 * 
 * @author sballar
 *
 */
public class BuilderUtils {

	/**
	 * Resample the data at the specified radii.
	 * <p>
	 * Only works on profiles of type NPOINT. All others ignored.
	 * 
	 * @param model
	 * @param vertex
	 * @param layer
	 * @param radii
	 * @throws Exception
	 */
	static public void resampleProfile(GeoTessModel model, int vertex, int layer, float[] radii) throws Exception {
		Profile profile = model.getProfile(vertex, layer);
		if (profile.getType() == ProfileType.NPOINT) {
			Data[] data = new Data[radii.length];

			switch (model.getMetaData().getDataType()) {
			case FLOAT:
				for (int i = 0; i < radii.length; ++i) {
					float[] values = new float[model.getNAttributes()];
					for (int j = 0; j < model.getNAttributes(); ++j)
						values[j] = (float) profile.getValue(InterpolatorType.LINEAR, j, radii[i], true);
					data[i] = Data.getDataFloat(values);
				}
				break;
			case DOUBLE:
				for (int i = 0; i < radii.length; ++i) {
					double[] values = new double[model.getNAttributes()];
					for (int j = 0; j < model.getNAttributes(); ++j)
						values[j] = profile.getValue(InterpolatorType.LINEAR, j, radii[i], true);
					data[i] = Data.getDataDouble(values);
				}
				break;
			case LONG:
				for (int i = 0; i < radii.length; ++i) {
					long[] values = new long[model.getNAttributes()];
					for (int j = 0; j < model.getNAttributes(); ++j)
						values[j] = Math.round(profile.getValue(InterpolatorType.LINEAR, j, radii[i], true));
					data[i] = Data.getDataLong(values);
				}
				break;
			case INT:
				for (int i = 0; i < radii.length; ++i) {
					int[] values = new int[model.getNAttributes()];
					for (int j = 0; j < model.getNAttributes(); ++j)
						values[j] = (int) Math.round(profile.getValue(InterpolatorType.LINEAR, j, radii[i], true));
					data[i] = Data.getDataInt(values);
				}
				break;
			case SHORT:
				for (int i = 0; i < radii.length; ++i) {
					short[] values = new short[model.getNAttributes()];
					for (int j = 0; j < model.getNAttributes(); ++j)
						values[j] = (short) Math.round(profile.getValue(InterpolatorType.LINEAR, j, radii[i], true));
					data[i] = Data.getDataShort(values);
				}
				break;
			case BYTE:
				for (int i = 0; i < radii.length; ++i) {
					byte[] values = new byte[model.getNAttributes()];
					for (int j = 0; j < model.getNAttributes(); ++j)
						values[j] = (byte) Math.round(profile.getValue(InterpolatorType.LINEAR, j, radii[i], true));
					data[i] = Data.getDataByte(values);
				}
				break;
			case CUSTOM:
				throw new Exception("Cannot changeAttributes when DataType == DataType.CUSTOM");
			default:
				throw new Exception("Unsupported DataType");
			}
			model.setProfile(vertex, layer, radii, data);
		}
	}

	static public void changeAttributes(GeoTessModel model, int[] attributeMap) throws Exception {
		PointMap pm = model.getPointMap();

		switch (model.getMetaData().getDataType()) {
		case FLOAT:
			for (int pointIndex = 0; pointIndex < pm.size(); ++pointIndex) {
				Data data = pm.getPointData(pointIndex);
				float[] floats = new float[attributeMap.length];
				for (int i = 0; i < floats.length; ++i)
					floats[i] = data.getFloat(attributeMap[i]);
				pm.setPointData(pointIndex, Data.getDataFloat(floats));
			}
			break;
		case DOUBLE:
			for (int pointIndex = 0; pointIndex < pm.size(); ++pointIndex) {
				Data data = pm.getPointData(pointIndex);
				double[] doubles = new double[attributeMap.length];
				for (int i = 0; i < doubles.length; ++i)
					doubles[i] = data.getDouble(attributeMap[i]);
				pm.setPointData(pointIndex, Data.getDataDouble(doubles));
			}
			break;
		case LONG:
			for (int pointIndex = 0; pointIndex < pm.size(); ++pointIndex) {
				Data data = pm.getPointData(pointIndex);
				long[] longs = new long[attributeMap.length];
				for (int i = 0; i < longs.length; ++i)
					longs[i] = data.getLong(attributeMap[i]);
				pm.setPointData(pointIndex, Data.getDataLong(longs));
			}
			break;
		case INT:
			for (int pointIndex = 0; pointIndex < pm.size(); ++pointIndex) {
				Data data = pm.getPointData(pointIndex);
				int[] ints = new int[attributeMap.length];
				for (int i = 0; i < ints.length; ++i)
					ints[i] = data.getInt(attributeMap[i]);
				pm.setPointData(pointIndex, Data.getDataInt(ints));
			}
			break;
		case SHORT:
			for (int pointIndex = 0; pointIndex < pm.size(); ++pointIndex) {
				Data data = pm.getPointData(pointIndex);
				short[] shorts = new short[attributeMap.length];
				for (int i = 0; i < shorts.length; ++i)
					shorts[i] = data.getShort(attributeMap[i]);
				pm.setPointData(pointIndex, Data.getDataShort(shorts));
			}
			break;
		case BYTE:
			for (int pointIndex = 0; pointIndex < pm.size(); ++pointIndex) {
				Data data = pm.getPointData(pointIndex);
				byte[] bytes = new byte[attributeMap.length];
				for (int i = 0; i < bytes.length; ++i)
					bytes[i] = data.getByte(attributeMap[i]);
				pm.setPointData(pointIndex, Data.getDataByte(bytes));
			}
			break;
		case CUSTOM:
			throw new Exception("Cannot changeAttributes when DataType == DataType.CUSTOM");
		default:
			throw new Exception("Unsupported DataType");
		}
		String[] attributes = new String[attributeMap.length];
		String[] units = new String[attributeMap.length];
		for (int i = 0; i < attributeMap.length; ++i) {
			attributes[i] = model.getMetaData().getAttributeName(attributeMap[i]);
			units[i] = model.getMetaData().getAttributeUnit(attributeMap[i]);
		}
		model.getMetaData().setAttributes(attributes, units);
	}

	/**
	 * 
	 * @param model
	 * @param newShape
	 * @throws Exception
	 */
	static public void changeEarthShape(GeoTessModel model, EarthShape newShape) throws Exception {
		double stretch;
		for (int vertex = 0; vertex < model.getNVertices(); ++vertex) {
			stretch = newShape.getEarthRadius(model.getVertex(vertex))
					/ model.getEarthShape().getEarthRadius(model.getVertex(vertex));

			for (Profile p : model.getProfiles(vertex))
				for (int r = 0; r < p.getNRadii(); ++r)
					p.setRadius(r, (float) (p.getRadius(r) * stretch));
		}
		model.setEarthShape(newShape);
	}

	/**
	 * 
	 * @param model
	 * @param newShape
	 * @param iCMB
	 * @param i660
	 * @throws Exception
	 */
	static public void changeEarthShape(GeoTessModel model, EarthShape newShape, int iCMB, int i660) throws Exception {
		if (newShape == model.getEarthShape())
			return;

		if (iCMB < 0 || i660 < 0)
			throw new Exception("both iCMB and i660 must be >= 0");

		double stretch;
		for (int vertex = 0; vertex < model.getNVertices(); ++vertex) {
			double originalEarthRadius = model.getEarthShape().getEarthRadius(model.getVertex(vertex));
			double newEarthRadius = newShape.getEarthRadius(model.getVertex(vertex));

			double dr = newEarthRadius - originalEarthRadius;

			Profile[] newProfiles = new Profile[model.getNLayers()];
			for (int layer = 0; layer < model.getNLayers(); ++layer)
				newProfiles[layer] = model.getProfile(vertex, layer).copy();

			stretch = newEarthRadius / originalEarthRadius;

			// ellipsoidal radius of the core-mantle boundary is stretched/compressed
			// using the new flattening parameter.
			double rCMBOriginal = newProfiles[iCMB].getRadiusTop();
			double rCMBNew = rCMBOriginal * stretch;

			// radius of the discontinuity at ~660 km depth is set to
			// be at same depth in new model as it was in original model.
			double depth_660 = originalEarthRadius - newProfiles[i660].getRadiusTop();
			double r660New = newEarthRadius - depth_660;

			double mfactor = (r660New - rCMBNew) / (newProfiles[i660].getRadiusTop() - rCMBOriginal);

			// for layers from center of earth to CMB, stretch/compress
			// the radii using ratio of earth flattening parameters
			for (int i = 0; i <= iCMB; ++i)
				for (int j = 0; j < newProfiles[i].getNRadii(); ++j)
					newProfiles[i].setRadius(j, (float) (newProfiles[i].getRadius(j) * stretch));

			// for layers from CMB to M660, stretch/compress linearly
			// between radii at CMB and M660.
			for (int i = iCMB + 1; i <= i660; ++i) {
				newProfiles[i].setRadius(0, (float) newProfiles[i - 1].getRadiusTop());
				for (int j = 1; j < newProfiles[i].getNRadii(); ++j)
					newProfiles[i].setRadius(j,
							(float) (rCMBNew + (newProfiles[i].getRadius(j) - rCMBOriginal) * mfactor));
			}

			// for interfaces from M660 to surface, preserve depth of the interfaces.
			for (int i = i660 + 1; i < newProfiles.length; ++i) {
				newProfiles[i].setRadius(0, (float) newProfiles[i - 1].getRadiusTop());
				for (int j = 1; j < newProfiles[i].getNRadii(); ++j)
					newProfiles[i].setRadius(j, (float) (newProfiles[i].getRadius(j) + dr));
			}

			model.getProfiles()[vertex] = newProfiles;
		}
		model.setEarthShape(newShape);
	}

	/**
	 * Remove the specified layer from the model.
	 * 
	 * @param model
	 * @param layerName
	 * @throws Exception
	 */
	static public void removeLayer(GeoTessModel model, String layerName) throws Exception {
		int layer = model.getMetaData().getLayerIndex(layerName);
		if (layer < 0)
			throw new Exception(layerName + " is not a recognized interface in this model");

		int nLayersOld = model.getNLayers();
		int nLayersNew = nLayersOld - 1;

		for (int vertex = 0; vertex < model.getNVertices(); ++vertex) {
			Profile[] oldProfiles = model.getProfiles(vertex);

			Profile[] newProfiles = new Profile[nLayersNew];
			int idx = 0;
			for (int i = 0; i < nLayersOld; ++i)
				if (i != layer)
					newProfiles[idx++] = oldProfiles[i];

			model.getProfiles()[vertex] = newProfiles;
		}

		String[] layerNames = new String[nLayersNew];
		int idx = 0;
		for (int i = 0; i < nLayersOld; ++i)
			if (i != layer)
				layerNames[idx++] = model.getMetaData().getLayerName(i);

		model.getMetaData().setLayerNames(layerNames);

		model.setActiveRegion();
	}

	/**
	 * Combine all the layers between layerName1 and layerName2, inclusive, into a
	 * single layer in which the GeoAttributes all have constant values.
	 * 
	 * <p>
	 * WARNING: this only works with layers comprised of Profiles that are of type
	 * CONSTANT or THIN.
	 * 
	 * @param model
	 * @param layerName1
	 * @param layerName2
	 * @param newLayerName
	 * @throws Exception
	 */
	static public void combineMajorLayersToConstant(GeoTessModel model, String layerName1, String layerName2,
			String newLayerName) throws Exception {
		int layer1 = model.getMetaData().getLayerIndex(layerName1);
		if (layer1 < 0)
			throw new Exception(layerName1 + " is not a recognized interface in this model");

		int layer2 = model.getMetaData().getLayerIndex(layerName2);
		if (layer2 < 0)
			throw new Exception(layerName2 + " is not a recognized interface in this model");

		int nAttributes = model.getNAttributes();
		int nLayersOld = model.getNLayers();
		int nLayersNew = nLayersOld - (layer2 - layer1);

		int nThin = 0;

		for (int vertex = 0; vertex < model.getNVertices(); ++vertex) {
			Profile[] profiles = model.getProfiles(vertex);

			// find total thickness of layers that are to be replaced.
			double totalThickness = 0;
			for (int layer = layer1; layer <= layer2; ++layer)
				totalThickness += profiles[layer].getThickness();

			float[] values = new float[nAttributes];

			if (totalThickness < 1e-4) {
				// total thickness is zero. See if any layer has non-NaN values.
				int[] count = new int[nAttributes];
				for (int layer = layer1; layer <= layer2; ++layer) {
					for (int a = 0; a < nAttributes; ++a)
						if (!Double.isNaN(profiles[layer].getValue(a, 0))) {
							values[a] += profiles[layer].getValue(a, 0);
							++count[a];
						}
				}

				for (int a = 0; a < nAttributes; ++a)
					if (count[a] == 0) {
						// if any attribute is all nans, then set all attribute values to nan.
						Arrays.fill(values, Float.NaN);
						++nThin;
						break;
					} else
						values[a] /= count[a];

				float[] radii = new float[] { (float) profiles[layer2].getRadiusTop() };

				profiles[layer1] = Profile.newProfile(radii, new float[][] { values });
			} else {
				for (int layer = layer1; layer <= layer2; ++layer)
					for (int a = 0; a < nAttributes; ++a)
						if (!Double.isNaN(profiles[layer].getValue(a, 0)))
							values[a] += profiles[layer].getThickness() * profiles[layer].getValue(a, 0);

				for (int a = 0; a < nAttributes; ++a)
					values[a] /= totalThickness;

				float[] radii = new float[] { (float) profiles[layer1].getRadiusBottom(),
						(float) profiles[layer2].getRadiusTop() };

				profiles[layer1] = Profile.newProfile(radii, new float[][] { values });
			}

			Profile[] newProfiles = new Profile[nLayersNew];
			int idx = 0;
			for (int i = 0; i < nLayersOld; ++i)
				if (i <= layer1 || i > layer2)
					newProfiles[idx++] = profiles[i];

			model.getProfiles()[vertex] = newProfiles;
		}

		System.out.println("nThin = " + nThin);

		String[] layerNames = new String[nLayersNew];
		int idx = 0;
		for (int i = 0; i < nLayersOld; ++i)
			if (i <= layer1 || i > layer2)
				layerNames[idx++] = model.getMetaData().getLayerName(i);
		layerNames[layer1] = newLayerName;

		model.getMetaData().setLayerNames(layerNames);

		model.setActiveRegion();
	}

	/**
	 * 
	 * @param model
	 * @throws Exception
	 */
	static public void checkZeroThicknessValues(GeoTessModel model) throws Exception {
		for (int layer = 0; layer < model.getNLayers(); ++layer)
			checkZeroThicknessValues(model, layer);
	}

	/**
	 * 
	 * @param model
	 * @param layerName
	 * @throws Exception
	 */
	static public void checkZeroThicknessValues(GeoTessModel model, String layerName) throws Exception {
		checkZeroThicknessValues(model, model.getMetaData().getLayerIndex(layerName));
	}

	static public void checkZeroThicknessValues(GeoTessModel model, int layer) throws Exception {
		int nAttributes = model.getNAttributes();
		;

		int[] count = new int[model.getNVertices()];
		double[][] values = new double[model.getNVertices()][nAttributes];

		GeoTessGrid grid = model.getGrid();
		int tessId = model.getMetaData().getTessellation(layer);
		int level = grid.getNLevels(tessId) - 1;
		int nhits = 1;
		HashSetInteger vertices = grid.getVertexIndices(tessId, level);
		Iterator iterator;

		EnumSet<ProfileType> profileTypes = EnumSet.of(ProfileType.CONSTANT, ProfileType.THIN, ProfileType.SURFACE);

		while (nhits > 0) {
			iterator = vertices.iterator();
			while (iterator.hasNext()) {
				int vertex = iterator.next();
				Profile profile = model.getProfile(vertex, layer);
				if (!profileTypes.contains(profile.getType()))
					throw new Exception("Can't fix data in layer of type " + profile.getType());

				if (Double.isNaN(profile.getValue(0, 0))) {
					for (int neighborVertex : grid.getVertexNeighbors(tessId, level, vertex)) {
						Profile neighbor = model.getProfile(neighborVertex, layer);
						if (!Double.isNaN(neighbor.getValue(0, 0))) {
							++count[vertex];
							for (int a = 0; a < nAttributes; ++a)
								values[vertex][a] += neighbor.getValue(a, 0);
						}
					}
				}
			}

			nhits = 0;
			for (int vertex = 0; vertex < count.length; ++vertex)
				if (count[vertex] > 0) {
					++nhits;
					Profile profile = model.getProfile(vertex, layer);
					for (int a = 0; a < nAttributes; ++a)
						profile.getData(0).setValue(a, values[vertex][a] / count[vertex]);

				}

			Arrays.fill(count, 0);
			for (int i = 0; i < values.length; ++i)
				Arrays.fill(values[i], 0.);

			// System.out.println("NHits = "+nhits);
		}
	}

}
