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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.EarthInterface;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;
import gov.sandia.gmp.bender.BenderModelInterfaces;
import gov.sandia.gmp.util.containers.Tuple;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.globals.Globals;

/**
 * Defines a phase ray branch model used by Bender.Ray to construct initial
 * rays that support multiple top and bottom side fixed reflections and refractions.
 * The phase ray branch model is constructed from the comma separated branch
 * type / model interface list provided by the SeismicPhase object using the
 * method getRayBranchList(). The ray branch list defines each branch type that
 * is defined between a source and receiver which are assumed on entry. For 
 * example, the string "BOTTOM_SIDE_REFLECTION, SURFACE, TOP_SIDE_REFLECTION,
 * CMB" is the prescription for a ray leaving the source in an up-going sense
 * (a depth phase) before reflecting off the bottom side of the Earth's free
 * surface and then traveling in a down-going manner before reflecting off the
 * top side of the core-mantle boundary, finally traveling in an up-going
 * fashion to the receiver.  
 * 
 * @author jrhipp
 *
 */
public class PhaseRayBranchModel
{
	/**
	 * The GeoTessModel metadata object from which this wavetype interface
	 * conversion specification was created.
	 */
	private GeoTessMetaData   metaData = null;

	/**
	 * The seismic phase from which this model was created.
	 */
	private SeismicPhase      seismicPhase = null;

	/**
	 * Builds all refraction phase layer level structures.
	 */
	private PhaseLayerLevelBuilder phaseLayerLevelBuilder = null;

	/**
	 * The list of all interface names associated with a direction change
	 */
	private ArrayList<String> rayBranchInterfaceNameList = null;

	/**
	 * The list of all interface indexes associated with a direction change
	 */
	private ArrayListInt      rayBranchInterfaceIndxList = null;

	/**
	 * The list of all ray branch direction changes
	 */
	private ArrayList<RayDirection> rayBranchDirectionChangeType = null;

	/**
	 * The map of all refraction phase layer definition objects associated with
	 * their refraction EarthInterface name.
	 */
	private HashMap<String, PhaseLayerLevelDefinition> levelDefinitionMap = null;

	/**
	 * A list that maps the branch model index to the Bottom-Side Reflection
	 * index.
	 */
	private ArrayListInt branchModelIndexFromBSRIndex = null;

	/**
	 * The list of all EarthInterface for the current model.
	 */
	private EarthInterface[] modelEarthInterface = null;

	/**
	 * Retrieves the Branch Model Index given an input Bottom-Side Reflection
	 * Index.
	 *  
	 * @param bsri The input Bottom-Side Reflection index.
	 * @return The associated Branch Model Index.
	 */
	public int getBMIndexFromBSRIndex(int bsri)
	{
		return branchModelIndexFromBSRIndex.get(bsri);
	}
	
	/**
	 * A list that maps the Bottom-Side Reflection index to the branch model
	 * index.
	 */
	private ArrayListInt bottomSideReflectionIndexFromBMIndex = null;

	/**
	 * Retrieves the Bottom-Side Reflection index given and input Branch Model 
	 * Index.
	 *  
	 * @param bmi The input Branch Model Index.
	 * @return The associated Bottom-Side Reflection index.
	 */
	public int getBSRIndexFromBMIndex(int bmi)
	{
		return bottomSideReflectionIndexFromBMIndex.get(bmi);
	}

	/**
	 * The number of fixed reflections defined by this model.
	 */
	private int               fixedReflectionCount = 0;

	/**
	 * The number of fixed underside reflections defined by this model.
	 */
	private int               underSideReflectionCount = 0;
	
	/**
	 * The source to receiver separation distance in degrees. Used to set the
	 * initial distance between fixed reflections.
	 */
	private double            srcRcvrDistDeg = 0.0;

	/**
	 * The initial angle from the source to the receiver for each fixed reflection
	 * direction change entry. If the entry is not a fixed reflection the angle
	 * is set to zero.
	 */
	private ArrayListDouble fixedReflInitialAngle = null;

	/**
	 * Standard toString() method that returns the content of the Phase Ray Branch
	 * Model.
	 */
	@Override
	public String toString() {
		return getPhaseRayBranchModelTable();
	}

	/**
	 * Returns the phase layer level builder for this branch model.
	 * 
	 * @return The phase layer level builder for this branch model.
	 */
	public PhaseLayerLevelBuilder getPhaseLayerLevelBuilder() {
		return phaseLayerLevelBuilder;
	}

	/**
	 * Returns the EarthInterface associated with the input major layer index.
	 * 
	 * @param majorLayerIndex The major layer index.
	 * @return The EarthInterface associated with the input major layer index.
	 */
	public EarthInterface getModelEarthInterface(int majorLayerIndex) {
		return modelEarthInterface[majorLayerIndex];
	}

	/**
	 * Builds a new RayBranchDirectionChangeModel from the input GeoTessModel
	 * metadata object and the input seismic phase.
	 * 
	 * @param md                       The GeoTessModel metadata object.
	 * @param sp                       The seismic phase object.
	 * @param phaseModelInterfaceRemap A mapping of phase EarthInterfaces defined
	 *                                 and used by SeismicPhase to those actually
	 *                                 defined in the input GeoTessModel (model).
	 * @param srcRcvrDistDeg           The source to receiver distance in degrees.
	 * @param plldw                    The phase layer level depth width which
	 *                                 defines the thickness (km) of Level
	 *                                 definitions inserted within various Earth
	 *                                 model Layers.
	 * @throws IOException
	 * @throws GeoTessException
	 */
	public PhaseRayBranchModel(GeoTessPosition depthProfile, SeismicPhase sp,
			BenderModelInterfaces benderModelInterfaces,
			ArrayList<Tuple<RayDirection, EarthInterface>> rayBrnchDirChngList,
			double srcRcvrDistDeg, double plldw)
			throws IOException, GeoTessException {

		// set the seismic phase

		seismicPhase = sp;

		// get valid interface names and the model EarthInterface list

		HashMap<String, Integer> validInterfaceNames = benderModelInterfaces.getValidInterfaceNameIndexMap();
		modelEarthInterface = benderModelInterfaces.getModelValidInterfaces();

		// make the phase layer level builder

		phaseLayerLevelBuilder = new PhaseLayerLevelBuilder(depthProfile, 0, modelEarthInterface, validInterfaceNames);
		phaseLayerLevelBuilder.setPhaseLayerLevelDepthWidth(plldw);

		// create the interface name and index lists and the direction change list

		rayBranchDirectionChangeType = new ArrayList<RayDirection>(rayBrnchDirChngList.size() + 2);
		rayBranchInterfaceNameList = new ArrayList<String>(rayBrnchDirChngList.size() + 2);
		rayBranchInterfaceIndxList = new ArrayListInt(rayBrnchDirChngList.size() + 2);
		levelDefinitionMap = new HashMap<String, PhaseLayerLevelDefinition>();

		rayBranchDirectionChangeType.add(RayDirection.SOURCE);
		rayBranchInterfaceNameList.add("SOURCE");
		rayBranchInterfaceIndxList.add(-1);

		// loop over all branch type entries

		int minTopSideInterfaceIndex = -1;
		RayDirection lastRayBrnchDirChngType = null;
		for (int i = 0; i < rayBrnchDirChngList.size(); ++i) {
			// get next branch change

			RayDirection rayBrnchDirChngType = rayBrnchDirChngList.get(i).first;

			// check for invalid branch transition adjacent pairs (bottom -> bottom,
			// top -> top, top -> refraction, refraction -> top, and refraction ->
			// refraction).

			if (lastRayBrnchDirChngType != null) {
				if (lastRayBrnchDirChngType == RayDirection.BOTTOM_SIDE_REFLECTION) {
					if (rayBrnchDirChngType == RayDirection.BOTTOM_SIDE_REFLECTION)
						throw new IOException("Error: Two Consecutive Bottom Side Fixed Reflections were found ...");
				} else {
					if (lastRayBrnchDirChngType == RayDirection.TOP_SIDE_REFLECTION) {
						if (rayBrnchDirChngType == RayDirection.TOP_SIDE_REFLECTION)
							throw new IOException("Error: Two Consecutive Top Side Fixed Reflection were found ...");
						else if (rayBrnchDirChngType == RayDirection.BOTTOM)
							throw new IOException(
									"Error: A Top Side Fixed Reflection followed by a Refraction was found ...");
					} else if (lastRayBrnchDirChngType == RayDirection.BOTTOM) {
						if (rayBrnchDirChngType == RayDirection.TOP_SIDE_REFLECTION)
							throw new IOException(
									"Error: A Refraction followed by a Top Side Fixed Reflection was found ...");
						else if (rayBrnchDirChngType == RayDirection.BOTTOM)
							throw new IOException("Error: Two Consecutive Refractions were found ...");
					}
				}
			}

			// add ray branch direction type and get associated interface
			// index, and check for refraction

			rayBranchDirectionChangeType.add(rayBrnchDirChngType);
			EarthInterface earthInterface = rayBrnchDirChngList.get(i).second;
			int indx = validInterfaceNames.get(earthInterface.name());
			if (rayBrnchDirChngType == RayDirection.BOTTOM) {
				// refraction ... get level definition and assign interface name ...

				levelDefinitionMap.put(earthInterface.name(),
						phaseLayerLevelBuilder.getPhaseLayerLevelDefinition(earthInterface.name()));
				rayBranchInterfaceNameList.add(earthInterface.name());
				rayBranchInterfaceIndxList.add(indx);
			}

			else {
				// not refraction ... must be valid fixed reflection ... add associated interface
				// name and index and increment fixed reflection count

				rayBranchInterfaceNameList.add(earthInterface.name());
				rayBranchInterfaceIndxList.add(indx);
				++fixedReflectionCount;
				if ((rayBrnchDirChngType == RayDirection.TOP_SIDE_REFLECTION)
						&& ((indx < minTopSideInterfaceIndex) || (minTopSideInterfaceIndex == -1)))
					minTopSideInterfaceIndex = indx;
			}

			// set last branch type to current branch type and continue loop

			lastRayBrnchDirChngType = rayBrnchDirChngType;
		}

		// add "RECEIVER" as an end point and create radian angle distances
		// between fixed reflection points

		rayBranchDirectionChangeType.add(RayDirection.RECEIVER);
		rayBranchInterfaceNameList.add("RECEIVER");
		rayBranchInterfaceIndxList.add(-1);
		setSourceReceiverDistance(srcRcvrDistDeg);

		// build default layer level here from minTopSideInterfaceIndex if no
		// refraction branches were defined.

		if (levelDefinitionMap.size() == 0) {
			String name = modelEarthInterface[minTopSideInterfaceIndex].name();
			levelDefinitionMap.put(name, phaseLayerLevelBuilder.getPhaseLayerLevelDefinition(name));
		}
	}
	
	/**
	 * Returns the number of ray branch direction change entries.
	 * 
	 * @return The number of ray branch direction change entries.
	 */
	public int size()
	{
		return rayBranchDirectionChangeType.size();
	}

	/**
	 * Sets the initial angular distances between the fixed reflection points based
	 * on the input source-receiver separation distance in degrees
	 * 
	 * @param srcRcvrDistDeg The source-receiver separation distance in degrees.
	 */
	public void setSourceReceiverDistance(double distDeg) {
		srcRcvrDistDeg = distDeg;

		// create a temporary array to hold distances between fixed reflections
		// if this is a depth phase then make the first entry different (closer to
		// the source) than the other fixed reflection sources.

		double dist = Math.toRadians(srcRcvrDistDeg);
		double[] angFrac = null;
		angFrac = new double[fixedReflectionCount + 2];
		angFrac[0] = 0.0;
		if (isDepthPhase()) {
			double bsr = 1.0;
			if (srcRcvrDistDeg >= 90.0)
				bsr = 0.05;
			else if (srcRcvrDistDeg <= 30.0)
				bsr = .30;
			else
				bsr = .05 * (6.0 - (srcRcvrDistDeg - 30.0) / 12.0);

			for (int i = 1; i <= fixedReflectionCount; ++i)
				angFrac[i] = dist * (bsr + (1.0 - bsr) * (i - 1) / fixedReflectionCount);
		} else {
			for (int i = 1; i <= fixedReflectionCount; ++i)
				angFrac[i] = dist * i / (fixedReflectionCount + 1);
		}
		angFrac[fixedReflectionCount + 1] = dist;

		// now save the entries into the fixedReflectionInitialAngle list ...
		// use 0.0 for non-fixed reflection entries.

		fixedReflInitialAngle = new ArrayListDouble(rayBranchDirectionChangeType.size());
		int refl = 1;
		bottomSideReflectionIndexFromBMIndex = new ArrayListInt(fixedReflectionCount + 2);
		branchModelIndexFromBSRIndex = new ArrayListInt(fixedReflectionCount);
		underSideReflectionCount = 0;
		for (int i = 0; i < rayBranchDirectionChangeType.size(); ++i) {
			if (isFixedReflection(i)) {
				fixedReflInitialAngle.add(angFrac[refl++]);
				if (rayBranchDirectionChangeType.get(i) == RayDirection.BOTTOM_SIDE_REFLECTION) {
					bottomSideReflectionIndexFromBMIndex.add(underSideReflectionCount++);
					branchModelIndexFromBSRIndex.add(i);
				} else
					bottomSideReflectionIndexFromBMIndex.add(-1);
			} else {
				fixedReflInitialAngle.add(0.0);
				bottomSideReflectionIndexFromBMIndex.add(-1);
			}
		}
	}

	/**
	 * Returns the fixed reflection initial angle (between source and receiver in
	 * radians) for the ith entry. If the ith entry is not a fixed reflection
	 * 0.0 is returned.
	 * 
	 * @param i The entry for which the fixed reflection initial angle is returned.
	 * @return The fixed reflection initial angle (between source and receiver in
	 *         radians) for the ith entry.
	 */
	public double getFixedReflectionInitialAngle(int i)
	{
		return fixedReflInitialAngle.get(i);
	}

	/**
	 * Used to override the automatically determined fixed reflection initial
	 * angle (between source and receiver in radians) for the ith branch segment
	 * entry.
	 * @param i The branch segment entry.
	 * @param angle The angle for the ith branch segment.
	 */
	public void setFixedReflectionInitialAngle(int i, double angle)
	{
		fixedReflInitialAngle.set(i, angle);
	}

	/**
	 * Returns the fixed reflection initial angle (between source and receiver in
	 * radians) for the ith entry. If the ith entry is not a fixed reflection
	 * 0.0 is returned.
	 * 
	 * @param i The entry for which the fixed reflection initial angle is returned.
	 * @return The fixed reflection initial angle (between source and receiver in
	 *         radians) for the ith entry.
	 */
	public double getFixedReflectionInitialAngleFraction(int i)
	{
		return fixedReflInitialAngle.get(i) / Math.toRadians(srcRcvrDistDeg);
	}

	/**
	 * Returns true if the ith ray branch change direction is a fixed reflection
	 * (top or bottom).
	 * 
	 * @param i The entry whose ray branch change is compared for a fixed
	 *          reflection.
	 * @return True if the ith ray branch change direction is a fixed reflection
	 *         (top or bottom).
	 */
	public boolean isFixedReflection(int i)
	{
		return ((rayBranchDirectionChangeType.get(i) == RayDirection.BOTTOM_SIDE_REFLECTION) ||
				    (rayBranchDirectionChangeType.get(i) == RayDirection.TOP_SIDE_REFLECTION));
	}

	/**
	 * Returns the number of fixed reflections associated with this ray direction
	 * change model.
	 * 
	 * @return The number of fixed reflections associated with this ray direction
	 *         change model.
	 */
	public int getFixedReflectionCount()
	{
		return fixedReflectionCount;
	}

	/**
	 * Returns the number of fixed under side reflections associated with this ray
	 * direction change model.
	 * 
	 * @return The number of fixed under side reflections associated with this ray
	 *         direction change model.
	 */
	public int getUndersideReflectionCount()
	{
		return underSideReflectionCount;
	}

	/**
	 * Returns the number of refraction branches associated with this ray direction
	 * change model.
	 * 
	 * @return The number of refraction branches associated with this ray direction
	 *         change model.
	 */
	public int getRefractionCount()
	{
		return rayBranchDirectionChangeType.size() - fixedReflectionCount - 2;
	}

	/**
	 * Returns the ray branch model direction change type for entry i.
	 * 
	 * @return The ray branch model direction change type for entry i.
	 */
	public RayDirection getRayBranchDirectionChangeType(int i)
	{
		return rayBranchDirectionChangeType.get(i);
	}

	/**
	 * Returns the ray branch model interface name for entry i.
	 * 
	 * @return The ray branch model interface name for entry i.
	 */
	public String getRayBranchInterfaceName(int i)
	{
		return rayBranchInterfaceNameList.get(i);
	}

	/**
	 * Returns the ray branch model interface index for entry i.
	 * 
	 * @return The ray branch model interface index for entry i.
	 */
	public int getRayBranchInterfaceIndex(int i)
	{
		return rayBranchInterfaceIndxList.get(i);
	}

	/**
	 * Returns the refraction PhaseLayerDefinition object for entry i. If entry i
	 * is not a refraction then null is returned.
	 * 
	 * @return The refraction PhaseLayerDefinition object for entry i. If entry i
	 *         is not a refraction then null is returned.
	 */
	public PhaseLayerLevelDefinition getRefractionPhaseLayerDefinition(int i)
	{
		return levelDefinitionMap.get(rayBranchInterfaceNameList.get(i));
	}

	/**
	 * Returns the first PhaseLayerLevelDefinition entry in the stored map.
	 * There is always at least one (the default).
	 * 
	 * @return The first PhaseLayerLevelDefinition entry in the stored map.
	 */
	public PhaseLayerLevelDefinition getFirstPhaseLayerLevelDefinition()
	{
		return levelDefinitionMap.values().iterator().next(); 
	}

	/**
	 * Returns the metadata object used to create this ray branch direction change model.
	 *  
	 * @return The metadata object used to create this ray branch direction change model.
	 */
	public GeoTessMetaData getMetaData()
	{
		return metaData;
	}

	/**
	 * Returns the seismic phase object used to create this ray branch direction change model.
	 *  
	 * @return The seismic phase object used to create this ray branch direction change model.
	 */
	public SeismicPhase getSeismicPhase()
	{
		return seismicPhase;
	}

	/**
	 * Returns true if the ray direction change model represents a depth phase.
	 * 
	 * @return True if the ray direction change model represents a depth phase.
	 */
	public boolean isDepthPhase()
	{
		return (rayBranchDirectionChangeType.get(1) ==
				    RayDirection.BOTTOM_SIDE_REFLECTION);
	}

	/**
	 * Returns the current source to receiver distance setting.
	 * 
	 * @return The current source to receiver distance setting.
	 */
	public double getSourceToReceiverDistanceDegrees()
	{
		return srcRcvrDistDeg;
	}

	/**
	 * Builds a table of the ray branch structure.
	 * 
	 * @return A table of the ray branch structure.
	 */
	public String getPhaseRayBranchModelTable()
	{
		String hdr = "    ";
		String title = "Phase Ray Branch Model: " + seismicPhase.name();

		String rowColHdr = "";
		String[][] colHdr =
		{
		  { "Entry", "Direction Change", "Interface", "Interface"},
		  { "Index", "Type",             "Name",      "Index"}
		};

		Globals.TableAlignment algn = Globals.TableAlignment.CENTER;
		Globals.TableAlignment[] colAlign = { algn, algn, algn, algn };

		String[][] data = new String[rayBranchDirectionChangeType.size()][];
		for (int i = 0; i < rayBranchDirectionChangeType.size(); ++i)
		{
			String[] rowData = new String[colAlign.length];
			rowData[0] = Integer.toString(i);
			rowData[1] = rayBranchDirectionChangeType.get(i).name();
			rowData[2] = rayBranchInterfaceNameList.get(i);
			rowData[3] = Integer.toString(rayBranchInterfaceIndxList.get(i));
			data[i] = rowData;
		}

		return Globals.makeTable(hdr, title, rowColHdr, colHdr, colAlign, null,
				                     algn, data, 2);
	}
}
