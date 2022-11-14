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
package gov.sandia.gmp.bender.ray;

import java.util.ArrayList;
import java.util.Collections;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.bender.Bender;
import gov.sandia.gmp.bender.BenderConstants.RayStatus;
import gov.sandia.gmp.bender.BenderException;
import gov.sandia.gmp.bender.BenderException.ErrorCode;
import gov.sandia.gmp.bender.phase.PhaseLayerLevelDefinition;

public class RayBranchBottomLevels
{
  protected Ray                        owningRay = null;

  protected ArrayList<RayBranchBottom> rayBranchBottomLevelList = null;
  
  protected ArrayList<RayBranchBottom> validRayBranchBottomLevelList = null;

  protected RayBranchBottom            fastestRayBranchBottom = null;

  protected RayBranchBottom            currentRayBranchBottom = null;

	/**
	 * The branch PhaseLayerLevelDefinition used to acquire the Level structure
	 * required to build the RayBranch.
	 */
  protected PhaseLayerLevelDefinition phaseLayerDefn = null;

	/**
	 * The top Level of the phase definition model to be tested for a local
	 * travel time minimum.
	 */
	protected int topLayerLevel     = -1;

	/**
	 * The bottom Level of the phase definition model to be tested for a local
	 * travel time minimum.
	 */
	protected int bottomLayerLevel  = -1;

	public RayBranchBottomLevels(Ray ray, GeoTessPosition firstPnt,
															 GeoTessPosition lastPnt,
															 PhaseLayerLevelDefinition levelStructure,
															 int branchIndex, int branchBottomIndex) throws Exception
	{
	  buildBottomBranchLevelSet(ray, firstPnt, lastPnt, levelStructure,
	  													branchIndex, branchBottomIndex);
	}

	private void buildBottomBranchLevelSet(Ray owningRay, GeoTessPosition firstNode,
      																	 GeoTessPosition lastNode,
      																	 PhaseLayerLevelDefinition levelStructure,
      																	 int branchIndex,
      																	 int branchBottomIndex) throws Exception
  {
    this.owningRay = owningRay;

		// construct a ray branch bottom using the lowest level defined by the
		// input level structure. Add the branch to the list if it is valid.

		RayBranchBottom prevBottom = new RayBranchBottom(owningRay, firstNode, lastNode,
                                                     levelStructure);
		rayBranchBottomLevelList = new ArrayList<RayBranchBottom>(prevBottom.getLayerLevelCount());
		validRayBranchBottomLevelList = new ArrayList<RayBranchBottom>(prevBottom.getLayerLevelCount());
		rayBranchBottomLevelList.add(prevBottom);
		prevBottom.setBranchIndex(branchIndex);
		prevBottom.setBranchBottomIndex(branchBottomIndex);
		if (!prevBottom.isInvalid()) validRayBranchBottomLevelList.add(prevBottom);
		prevBottom.evaluateRayType();
    prevBottom.setRayBranchBottomLevels(this);
    
		// Now loop over all valid levels and construct new RayBranchBottom's for
		// each level ... add them to the list if they are valid.

    bottomLayerLevel = prevBottom.bottomLayerLevel;
    topLayerLevel		 = prevBottom.topLayerLevel;
		int currentLevel = prevBottom.currentLayerLevel;
		int lastLevel    = prevBottom.topLayerLevel;
		for (int i = currentLevel+1; i <= lastLevel; ++i)
		{
			try
			{
				//resetSegmentCount(prevBottom.firstSegmentIndex);
				prevBottom = new RayBranchBottom(owningRay, prevBottom, firstNode, lastNode, i);
				rayBranchBottomLevelList.add(prevBottom);
				prevBottom.setBranchIndex(branchIndex);
				prevBottom.setBranchBottomIndex(branchBottomIndex);
				if (!prevBottom.isInvalid()) validRayBranchBottomLevelList.add(prevBottom);
				prevBottom.evaluateRayType();
    		prevBottom.setRayBranchBottomLevels(this);
			}
			catch (BenderException bex)
			{
				if (bex.getErrorCode() != ErrorCode.NONFATAL) throw bex;
				//rayBranchBottomLevelList.add(prevBottom);
				//prevBottom.setBranchIndex(branchIndex);
				//prevBottom.setBranchBottomIndex(branchBottomIndex);
			}
		}

    validateLevelRays();
  }

  private void reloadValidLevelRayList()
  {
    validRayBranchBottomLevelList.clear();
    for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
    {
      RayBranchBottom rbbLevel = rayBranchBottomLevelList.get(i);
      if (!rbbLevel.isInvalid()) validRayBranchBottomLevelList.add(rbbLevel);
    }
  }

  private void validateLevelRays() throws BenderException, GeoTessException
  {
		// look for cases where a reflection is followed immediately by
		// a diffraction along the same interface. Convert the faster one
		// to a refraction.

		for (int i = 1; i < validRayBranchBottomLevelList.size(); ++i)
		{
			RaySegmentBottom bsegPrev = validRayBranchBottomLevelList.get(i - 1).getBottomSegment();
			RaySegmentBottom bseg     = validRayBranchBottomLevelList.get(i).getBottomSegment();
			if (bsegPrev.rayType == RayType.REFLECTION
					&& bseg.rayType == RayType.BOTTOM_SIDE_DIFFRACTION
					&& bsegPrev.getRayInterface() == bseg.getRayInterface())
			{
				// we don't think this can happen but were not sure. So were throwing an
				// error so if it does we can look at it.
				owningRay.bender.getErrorMessages().append(String.format(
						"Ray is invalid because property benderAllowCMBDiffraction is false and "
						+"the ray diffracts along the CMB.%n"));
				owningRay.bender.getErrorMessages().append(String.format("Version = %s%n", Bender.getVersion()));

				owningRay.bender.getErrorMessages().append(String.format("Receiver = %1.8f, %1.8f, %1.6f%n",
						owningRay.getReceiver().getLatitudeDegrees(),
						owningRay.getReceiver().getLongitudeDegrees(),
						owningRay.getReceiver().getDepth()));

				owningRay.bender.getErrorMessages().append(String.format("Source = %1.8f, %1.8f, %1.6f%n",
						owningRay.getSource().getLatitudeDegrees(),
						owningRay.getSource().getLongitudeDegrees(),
						owningRay.getSource().getDepth()));

				owningRay.bender.getErrorMessages().append(String.format("Phase = %s%n",
						owningRay.bender.getPhaseRayBranchModel().getSeismicPhase().toString()));

				owningRay.bender.getErrorMessages().append(String.format("layer = %s%n", bseg.activeLayer.getName()));

				owningRay.bender.getErrorMessages().append(String.format("distance = %1.6f%n",
									owningRay.getReceiver().distanceDegrees(owningRay.getSource())));
				
				throw new BenderException(ErrorCode.FATAL,
																	"Unknown Error?");
//				if (rayBranchBottomList.get(i - 1).getTravelTime() <= rayBranchBottomList.get(i)
//						.getTravelTime())
//					bsegPrev.rayType = RayType.REFRACTION;
//				else
//					bseg.rayType = RayType.REFRACTION;
			}
		}

		// check for diffraction or non-interface reflections and remove
		
		for (int i = validRayBranchBottomLevelList.size()-1; i>=0; --i)
		{
			boolean invalid = false;
			
			// remove non major interface diffraction and reflections if any

			RaySegmentBottom bseg     = validRayBranchBottomLevelList.get(i).getBottomSegment();
			if (!bseg.getRayInterface().isMajorInterface() &&
					((bseg.getRayType() == RayType.TOP_SIDE_DIFFRACTION) ||
					(bseg.getRayType() == RayType.REFLECTION)))
				invalid = true;
			
			if (invalid) validRayBranchBottomLevelList.remove(i);
		}

		// sort the list on travel time

		Collections.sort(validRayBranchBottomLevelList);
  }

	/**
	 * Returns the number of level settings for this bottom branch.
	 * 
	 * @return The number of level settings for this bottom branch.
	 */
	public int getLayerLevelCount()
	{
		return topLayerLevel - bottomLayerLevel + 1;
	}

	/**
	 * Returns the top layer level.
	 * 
	 * @return The top layer level.
	 */
	public int getTopLayerLevel()
	{
		return topLayerLevel;
	}

	/**
	 * Returns the bottom layer level.
	 * 
	 * @return The bottom layer level.
	 */
	public int getBottomLayerLevel()
	{
		return bottomLayerLevel;
	}


	/**
	 * Sets the current RayBranchBottom to the one defined at the input level index.
	 * @param levelIndex The level index of the current RayBranchBottom.
	 */
	protected void setCurrentRayBranchBottom(int levelIndex, RayStatus status)
	{
		currentRayBranchBottom = rayBranchBottomLevelList.get(levelIndex);
		currentRayBranchBottom.owningRay.rayBranches.set(currentRayBranchBottom.branchIndex,
																										 currentRayBranchBottom);
		currentRayBranchBottom.owningRay.setStatus(status);
	}

	/**
	 * Sets the fastest RayBranchBottom to the one defined at the input level index.
	 * @param levelIndex The level index of the current RayBranchBottom.
	 */
	protected void setFastestRayBranchBottom(RayStatus status)
	{
		currentRayBranchBottom = validRayBranchBottomLevelList.get(0);
		currentRayBranchBottom.owningRay.rayBranches.set(currentRayBranchBottom.branchIndex,
																										 currentRayBranchBottom);
		currentRayBranchBottom.owningRay.setStatus(status);
	}

  protected RayBranchBottom getFastestRayBranchBottom()
  {
    return validRayBranchBottomLevelList.get(0);
  }

  protected RayBranchBottom getRayBranchBottom(int i)
  {
    return rayBranchBottomLevelList.get(i);
  }

  protected RayBranchBottom getRayBranchBottomLevel(int level)
  {
    return rayBranchBottomLevelList.get(getLevelIndexFromLayerLevel(level));
  }

  protected boolean isFastestRayBranchBottom(RayBranchBottom rbb)
  {
    return (rbb == validRayBranchBottomLevelList.get(0)) ? true : false;
  }
  
	/**
	 * Sets the current RayBranchBottom to the one defined at the input level index.
	 * @param levelIndex The level index of the current RayBranchBottom.
	 */
	protected void setCurrentRayBranchBottomAsFastest()
	{
		currentRayBranchBottom = fastestRayBranchBottom;
	}

	protected int getLayerLevelFromLevelIndex(int levelIndex)
	{
		return levelIndex + bottomLayerLevel;
	}

	protected int getLevelIndexFromLayerLevel(int layerLevel)
	{
		return layerLevel - bottomLayerLevel;
	}
//
//	protected void inactivateDownGoingSuperCrustalSegments(int minSuperCrustalLayer)
//	{
//	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
//	    rayBranchBottomLevelList.get(i).superInactivateDownGoingSuperCrustalSegments(minSuperCrustalLayer);
//	}
//
//	protected void inactivateUpGoingSuperCrustalSegments(int minSuperCrustalLayer)
//	{
//	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
//	    rayBranchBottomLevelList.get(i).superInactivateUpGoingSuperCrustalSegments(minSuperCrustalLayer);
//	}
//
//	protected void activateDownGoingSuperCrustalSegments() throws GeoTessException
//	{
//	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
//	    rayBranchBottomLevelList.get(i).superActivateDownGoingSuperCrustalSegments();
//	}
//
//	protected void activateUpGoingSuperCrustalSegments() throws GeoTessException
//	{
//	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
//	    rayBranchBottomLevelList.get(i).superActivateUpGoingSuperCrustalSegments();
//	}

	protected void optimizeInitialize()
	{
	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
	    rayBranchBottomLevelList.get(i).superOptimizeInitialize();
	}

	protected void optimizeInitialize(double tol, double minNodeSpc)
	{
	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
	    rayBranchBottomLevelList.get(i).superOptimizeInitialize(tol, minNodeSpc);
	}
	
	protected void optimizeOuterBeforeInner() throws BenderException
	{
	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
	    rayBranchBottomLevelList.get(i).superOptimizeOuterBeforeInner();
	}

	protected void optimizeInnerInitialize()
	{
	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
	    rayBranchBottomLevelList.get(i).superOptimizeInnerInitialize();	
	}

  protected boolean optimizeInner() throws Exception
  {
	  boolean innerIterationDone = true;
	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
	  {
	    setCurrentRayBranchBottom(i, RayStatus.INNER_LOOP);
	    if (!rayBranchBottomLevelList.get(i).superOptimizeInner())
	    	innerIterationDone = false;
	  }
    reloadValidLevelRayList();
    validateLevelRays();
	  setFastestRayBranchBottom(RayStatus.INNER_LOOP);

	  return innerIterationDone;
  }

	protected boolean optimizeOuterAfterInner()
						throws GeoTessException, BenderException
	{
	  boolean outerIterationDone = true;
	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
	  {
	    setCurrentRayBranchBottom(i, RayStatus.OUTER_LOOP);
	    if (!rayBranchBottomLevelList.get(i).superOptimizeOuterAfterInner())
	      outerIterationDone = false;
    }
    reloadValidLevelRayList();
    validateLevelRays();
	  setFastestRayBranchBottom(RayStatus.OUTER_LOOP);

	  return outerIterationDone;
	}				

	protected void optimizeFinalize()
	{
	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
	     rayBranchBottomLevelList.get(i).superOptimizeFinalize();
	}

	protected void resetToInitialNodeDensity()
	{
	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
	  {
	    setCurrentRayBranchBottom(i, RayStatus.OUTER_LOOP);
	    rayBranchBottomLevelList.get(i).superResetToInitialNodeDensity();
	  }
	  setFastestRayBranchBottom(RayStatus.OUTER_LOOP);
	}
//
//	protected void checkThinLayers(boolean rmvThinLayers,
//  															 boolean addThickLayers) throws GeoTessException
//  {
//	  for (int i = 0; i < rayBranchBottomLevelList.size(); ++i)
//	  {
//	  	RayBranchBottom rbb = rayBranchBottomLevelList.get(i);
//	  	if (rbb.downGoingBranch != null)
//	  		rbb.downGoingBranch.checkThinLayers(rmvThinLayers, addThickLayers);
//	  	if (rbb.upGoingBranch != null)
//	  		rbb.upGoingBranch.checkThinLayers(rmvThinLayers, addThickLayers);
//	  }
//	}
}
