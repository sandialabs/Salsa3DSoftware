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
package gov.sandia.gmp.bender.phase;

import java.util.HashMap;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.geotess.Profile;
import gov.sandia.gmp.baseobjects.globals.EarthInterface;
import gov.sandia.gmp.baseobjects.globals.EarthInterfaceGroup;
import gov.sandia.gmp.bender.BenderConstants.LayerSide;
import gov.sandia.gmp.bender.level.LevelDepth;

/**
 * A PhaseLayerLevelDefinition builder that constructs a unique
 * PhaseLayerLevelDefinition object for each unique EarthInterface name
 * provided. These objects are stored in a map associated with the EarthInterface
 * and returned on any subsequent call to the builder using the same
 * EarthInterface name.
 *  
 * @author jrhipp
 *
 */
public class PhaseLayerLevelBuilder
{
	/**
	 * A static map used to hold all constructed PhaseLayerLevelDefinition
	 * associated with the EarthInterface name with which it was built.
	 */
	private static final HashMap<String, PhaseLayerLevelDefinition> phaseLayerLevelDefMap = new HashMap<String, PhaseLayerLevelDefinition>();

	private HashMap<String, Integer> validLayerMap = null;
	
	private EarthInterface[] validLayerArray = null;

//	/**
//	 * A specific index used as an example radial profile in the input model
//	 * for returning radii.
//	 */
//	private int             builderModelVertex = -1;

	/**
	 * The minimum thickness of a depth level just below the major layer boundary.
	 */
	private double minimumTopDepthLevelWidth = 10.0;

	/**
	 * The width between depths levels in a layer.
	 */
	private double depthLevelWidth = 30.0;

	/**
	 * The profile location at the sample index.
	 */
	private GeoTessPosition builderRadiusProfile = null;

	/**
	 * Standard constructor to set the GeoTessModel and sample vertexIndex (used to
	 * initiate profiles) for this builder.
	 * 
	 * @param model       The GeoTessModel used to construct
	 *                    PhaseLayerLevelDefinition objects.
	 * @param vertexIndex The sample vertex index in the GeoTessModel used to build
	 *                    initial radial profiles.
	 * @throws GeoTessException
	 */
	public PhaseLayerLevelBuilder(GeoTessPosition depthProfile, int vertexIndex,
			EarthInterface[] validInterfaceArray, HashMap<String, Integer> validLayerNames)
			throws GeoTessException {
		validLayerMap = validLayerNames;
		validLayerArray = validInterfaceArray;
		builderRadiusProfile = depthProfile;
	}

	protected void setPhaseLayerLevelDepthWidth(double plldw) {
		depthLevelWidth = plldw;
		minimumTopDepthLevelWidth = plldw / 3.0;
	}

	/**
	 * The only public method used to retrieve a PhaseLayerLevelDefinition defined
	 * for the input EarthInterface name. A PhaseLayerLevelDefinition object is
	 * built one time only for a specific EarthInterface. Subsequent calls to this
	 * method using the same EarthInterface will receive the same
	 * PhaseLayerLevelDefinition which is saved in a map once created.
	 * 
	 * @param phaseLayerType The input EarthInterface for which an associated
	 *                       PhaseLayerLevelDefinition object will be returned.
	 * @return A PhaseLayerLevelDefinition defined for the input EarthInterface name
	 * @throws GeoTessException
	 */
	public PhaseLayerLevelDefinition getPhaseLayerLevelDefinition(String phaseLayerType) throws GeoTessException {
		phaseLayerType = phaseLayerType.toUpperCase();
		PhaseLayerLevelDefinition pld = null;
		synchronized (this) {
			pld = phaseLayerLevelDefMap.get(phaseLayerType);
			if (pld == null) {
				EarthInterface ei = EarthInterface.valueOf(phaseLayerType);
				pld = buildPhaseLayerLevelDefinition(ei);
				phaseLayerLevelDefMap.put(phaseLayerType, pld);
			}
		}

		return pld;
	}

	/**
	 * Builds a unique or default PhaseLayerLevelDefinition for the input
	 * EarthInterface.
	 * 
	 * @param type The input EarthInterface for which the layer level definition is
	 *             constructed.
	 * @return The new PhaseLayerLevelDefinition for the input EarthInterface.
	 * @throws GeoTessException
	 */
	private PhaseLayerLevelDefinition buildPhaseLayerLevelDefinition(EarthInterface type) throws GeoTessException {
		if (type.getInterfaceGroup() == EarthInterfaceGroup.CRUST)
			return buildCrustPhaseLayerLevelDefintion();
		else if (type == EarthInterface.M410)
			return build410PhaseLayerLevelDefintion();
		else if (type == EarthInterface.M660)
			return build660PhaseLayerLevelDefintion();
		else if (type == EarthInterface.CMB)
			return buildCMBPhaseLayerLevelDefintion();
		else if (type == EarthInterface.ICB)
			return buildICBPhaseLayerLevelDefintion();
		else
			return buildDefaultPhaseLayerLevelDefintion(type);
	}

	/**
	 * Retrieve the index of the interface with the specified name. If
	 * majorInterfaces can be parsed to an integer, then that value is returned. If
	 * not, then the index of the interface with the specified name is returned.
	 * <p>
	 * If more than one name is supplied, the first one that can be successfully
	 * interpreted as either an integer index or a valid interface name is returned.
	 * <p>
	 * The ability to supply alternative names is useful. For example, some models
	 * call the top of the crust "CRUST" while in other models the top of the crust
	 * is called "UPPER_CRUST". If both are requested using this method, the correct
	 * index will be returned.
	 * 
	 * @param majorInterfaces String
	 * @return int
	 */
	public int getInterfaceIndex(String... majorInterfaces) {
		Integer index = null;
		for (String majorInterface : majorInterfaces) {
			index = validLayerMap.get(majorInterface.trim());
			if (index != null)
				return index.intValue();
		}

		return -1;
	}

	/**
	 * Builds a unique PhaseLayerLevelDefinition object for refraction rays that can
	 * bottom as deep as the Moho layer (e.g. Pg).
	 * 
	 * @return The PhaseLayerLevelDefinition object for the Crust.
	 * @throws GeoTessException
	 */
	private PhaseLayerLevelDefinition buildCrustPhaseLayerLevelDefintion() throws GeoTessException {
		
		PhaseLayerLevelDefinition pld = new PhaseLayerLevelDefinition(EarthInterface.UPPER_CRUST_TOP);
		addMajorLayerLevels(pld);

		//int iCrust = getInterfaceIndex("UPPER_CRUST_TOP", "CRUST_TOP", "SEDIMENTARY_LAYER_1_TOP",
		//		"SEDIMENTARY_LAYER_TOP", "SURFACE");

		//pld.addBottomLayer(181, "UPPER_CRUST_TOP", "CRUST_TOP", "SURFACE");
		pld.addTopLayer(181, "SURFACE", "SEDIMENTARY_LAYER_1_TOP", "UPPER_CRUST_TOP", "MIDDLE_CRUST_TOP", "LOWER_CRUST_TOP");

		pld.addBottomLayer(181, "LOWER_CRUST_TOP", "MIDDLE_CRUST_TOP", "UPPER_CRUST_TOP", "SEDIMENTARY_LAYER_1_TOP", "SURFACE");

		//pld.addTopLayer(5, "UPPER_CRUST_TOP", "CRUST_TOP");
		//pld.addTopLayer(30, "MIDDLE_CRUST_TOP", "CRUST_TOP");
		//pld.addTopLayer(181, "LOWER_CRUST_TOP", "CRUST_TOP");

		return pld;
	}

	/**
	 * Builds a unique PhaseLayerLevelDefinition object for refraction rays that can
	 * bottom as deep as the 410 layer but generally only penetrate into the moho
	 * layer (e.g. Pn).
	 * 
	 * @return The PhaseLayerLevelDefinition object for the 410.
	 * @throws GeoTessException
	 */
	private PhaseLayerLevelDefinition build410PhaseLayerLevelDefintion() throws GeoTessException {

		PhaseLayerLevelDefinition pld = new PhaseLayerLevelDefinition(EarthInterface.M410);
		addMajorLayerLevels(pld);

		// add all the sublayer interfaces between the 410 and the Moho,
		// as specified at the current GridNode, to the Layers object.
		// The interfaces are added to activeInterfaces as Depth objects.
		int iMoho = getInterfaceIndex("MOHO", "MANTLE_TOP");
		addSubLayers(pld, iMoho);

		// if source-receiver separation <= 10 degrees, deepest layer tested
		// is M410. Otherwise, deepest level tested is M660.
		pld.addBottomLayer(10, "M410");
		pld.addBottomLayer(181, "M660");

		// if source-receiver separation <= 24 shallowest level tested is MOHO.
		// if <= 30 degrees shallowest level tested is M410.
		// Otherwise, shallowest level tested is M660

		// pld.addTopLayer(8, "CRUST_TOP", "UPPER_CRUST_TOP", "SURFACE");
		pld.addTopLayer(24, "MOHO");
		pld.addTopLayer(30, "M410");
		pld.addTopLayer(181, "M660");

		// in summary: < 10 degrees test from M410 up to MOHO.
		// 10 to 24 degrees test from M660 to MOHO
		// 24 to 30 degrees test from M660 to M410
		// > 30 degrees test M660 only

		return pld;
	}

	/**
	 * Builds a unique PhaseLayerLevelDefinition object for refraction rays that can
	 * bottom as deep as the 660 layer (eg. P).
	 * 
	 * @return The PhaseLayerLevelDefinition object for the 660.
	 * @throws GeoTessException
	 */
	private PhaseLayerLevelDefinition build660PhaseLayerLevelDefintion() throws GeoTessException {
		PhaseLayerLevelDefinition pld = new PhaseLayerLevelDefinition(EarthInterface.M660);
		addMajorLayerLevels(pld);

		// add all the sublayer interfaces between the MOHO and the top of the CRUST
		// as specified at the current GridNode, to the Layers object.
		// The interfaces are added to activeInterfaces as Depth objects.
		//int i = getInterfaceIndex("MOHO");
		//addSubLayers(pld, i);

		// add all the sublayer interfaces between the 410 and the Moho,
		// as specified at the current GridNode, to the Layers object.
		// The interfaces are added to activeInterfaces as Depth objects.
		int i = getInterfaceIndex("MOHO");
		addSubLayers(pld, i);

		pld.addBottomLayer(10, "M410");
		pld.addBottomLayer(181, "M660");

		// pld.addTopLayer(8, "CRUST_TOP", "UPPER_CRUST_TOP", "SURFACE");
		pld.addTopLayer(24, "MOHO");
		pld.addTopLayer(30, "M410");
		pld.addTopLayer(181, "M660");

		return pld;
	}

	/**
	 * Builds a unique PhaseLayerLevelDefinition object for refraction rays that can
	 * bottom as deep as the outer core.
	 * 
	 * @return The PhaseLayerLevelDefinition object for the CMB.
	 * @throws GeoTessException
	 */
	private PhaseLayerLevelDefinition buildCMBPhaseLayerLevelDefintion() throws GeoTessException {
		PhaseLayerLevelDefinition pld = new PhaseLayerLevelDefinition(EarthInterface.CMB);
		addMajorLayerLevels(pld);

		// add top layer to be tested as CMB for any ray
		pld.addTopLayer(181, "CMB");

		// add bottom layer to be tested as CMB for any ray
		pld.addBottomLayer(181, "CMB");

		return pld;
	}

	/**
	 * Builds a unique PhaseLayerLevelDefinition object for refraction rays that can
	 * bottom as deep as the inner core.
	 * 
	 * @return The PhaseLayerLevelDefinition object for the ICB.
	 * @throws GeoTessException
	 */
	private PhaseLayerLevelDefinition buildICBPhaseLayerLevelDefintion() throws GeoTessException {
		PhaseLayerLevelDefinition pld = new PhaseLayerLevelDefinition(EarthInterface.ICB);
		addMajorLayerLevels(pld);

		// add top layer to be tested as ICB for any ray
		// modified by sballar, rwporri and kdavenp on 5/12/2022.  changed 'CMB' to 'ICB'
		pld.addTopLayer(181, "ICB");

		// add top layer to be tested as ICB for any ray
		pld.addBottomLayer(181, "ICB");

		return pld;
	}

	/**
	 * The default PhaseLayerLevelDefinition contains all major layers as levels and
	 * no additional intermediate levels. The top and bottom layer for which
	 * refraction bottoms are considered is set to the input EarthInterface.
	 * 
	 * @param ei The EarthInterface for which the default layer level definition is
	 *           constructed.
	 * @return The default PhaseLayerLevelDefinition for the input EarthInterface.
	 * @throws GeoTessException
	 */
	private PhaseLayerLevelDefinition buildDefaultPhaseLayerLevelDefintion(EarthInterface ei) throws GeoTessException {
		PhaseLayerLevelDefinition pld = new PhaseLayerLevelDefinition(ei);
		addMajorLayerLevels(pld);

		// add top layer to be tested as ICB for any ray
		pld.addTopLayer(181, ei.name());

		// add top layer to be tested as ICB for any ray
		pld.addBottomLayer(181, ei.name());

		return pld;
	}

	/**
	 * Adds the GeoTessModel major layers as levels in the new input
	 * PhaseLayerLevelDefinition object.
	 * 
	 * @param pld The new PhaseLayerLevelDefinition object to be initialized.
	 * @throws GeoTessException
	 */
	private void addMajorLayerLevels(PhaseLayerLevelDefinition pld) throws GeoTessException {
		for (int majorLayerIndex = 0; majorLayerIndex < validLayerArray.length; ++majorLayerIndex)

			pld.addRadius(builderRadiusProfile, majorLayerIndex, validLayerArray[majorLayerIndex], LayerSide.TOP);
	}

	/**
	 * Adds additional intermediate levels (between major layer intervals) to the
	 * input PhaseLayerLevelDefinition object within the input layer index.
	 * 
	 * @param pld   The PhaseLayerLevelDefinition object that will have additional
	 *              levels added within the input layer.
	 * @param layer The layer index within which the new intermediate levels are
	 *              added.
	 * @throws GeoTessException
	 */
	private void addSubLayers(PhaseLayerLevelDefinition pld, int layer) throws GeoTessException {
		double[] depths = findLayerDepths(layer);
		for (int i = 0; i < depths.length; ++i) {
			pld.addRadius(new LevelDepth(layer, depths[i]), builderRadiusProfile);
		}
	}

	/**
	 * Finds a depth discretization for the input layer such that the first and last
	 * entry are below the mean top of the layer and above the mean bottom of the
	 * layer. If the mean thickness of the layer is < depthLevelWidth then an empty
	 * depth array is returned.
	 * 
	 * @param layer The major layer index for which additional depth levels are
	 *              calculated.
	 *              
	 * @return The array of depths lying between the top of the layer and the bottom
	 *         of layer.
	 * @throws GeoTessException
	 */
	private double[] findLayerDepths(int layer) throws GeoTessException {

		double dpth = 0.0;
		double maxDepth = Double.MAX_VALUE;
		double minDepth = 0.0;

		GeoTessModel mdl = builderRadiusProfile.getModel();
		for (int i = 0; i < mdl.getNVertices(); ++i) {
			Profile layerProfile = mdl.getProfile(i, layer);
			if (layerProfile.getNRadii() >= 2) {
				double[] uv = mdl.getVertex(i);

				// find minimum max depth

				builderRadiusProfile.set(uv, layerProfile.getRadiusBottom());
				dpth = builderRadiusProfile.getDepth();
				if (dpth < maxDepth)
					maxDepth = dpth;

				// find maximum min depth

				builderRadiusProfile.setRadius(layerProfile.getRadiusTop());
				dpth = builderRadiusProfile.getDepth();
				if (dpth > minDepth)
					minDepth = dpth;
			}
		}

		// add a start delta so that the first entry is below the major layer
		// top and calculate the number of depths levels. If the thickness of the
		// layer is less than the value of depthLevelWidth make sure the number
		// of depths is zero.
		
		minDepth += minimumTopDepthLevelWidth;
		int nDepths = (int) ((maxDepth - minDepth) / depthLevelWidth);
		double delDepth = (maxDepth - minDepth) / nDepths;
		if (nDepths < 0) nDepths = 0;

		double[] depths = new double[nDepths];
		for (int i = 0; i < nDepths; ++i)
			depths[i] = delDepth * i + minDepth;

		return depths;
	}
}
