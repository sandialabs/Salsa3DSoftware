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
package gov.sandia.gmp.rayuncertainty.containers;

import java.io.IOException;
import java.util.HashSet;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.numerical.matrixblock.MatrixBlockDefinition;

/**
 * Maintains the RayWeightSet entries that avoid overloading the
 * RayUncertaintyTask objects during the ray uncertainty calculation. One of
 * these objects is created and maintained by the RayUncertainty object.
 * <p>
 * This object creates new RayWeightSet objects as needed and, when complete
 * with adding predicted ray weight entries, writes the RayWeightSet to disk as
 * a set of block row specific ray weight files that will be read by the
 * RayUncertaintyTask objects during the ray uncertainty calculation. This
 * object also maintains a separate RayWeightSet for type AB ray weight
 * covariance calculations. It is composed of entries from all other
 * RayWeightSet objects of just those sites that are in the input site pair set
 * (aSitePairSet).
 *
 * @author jrhipp
 */
public class RayWeightSetList {
    /**
     * The total number of ray weights set objects created.
     */
    private static int aRayWeightSetsCreated = 0;

    /**
     * The total number of phase / site entries maintained by all RayWeightSets
     * in this object.
     */
    private int aPhaseSiteCount = 0;

    /**
     * A set of RayWeightSets that are locked (can't add any more phase / site
     * entries).
     */
    private HashSet<RayWeightSet> aLockedSets = null;

    /**
     * The current RayWeightSet that is not locked and free to add more phase /
     * site entries.
     */
    private RayWeightSet aCurrentRayWghtSet = null;

    /**
     * The type AB pair RayWeightSet that is only instantiated if the site pair
     * set (aSitePairSet below). is instantiated. It is used to write out entries
     * specifically required to perform type AB ray covariance calculations.
     */
    private RayWeightSet aPairRayWghtSet = null;

    /**
     * The current set id count. Assigned to new RayWeightSets as they are
     * created and then incremented.
     */
    private int aSetId = 0;

    /**
     * The set of all site ids to be included in the type AB pair RayWeightSet.
     */
    private HashSet<Long> aSitePairSet = null;

    /**
     * Default constructor.
     */
    public RayWeightSetList() {
        aLockedSets = new HashSet<RayWeightSet>();
    }

    /**
     * Returns the number of RayWeightSets created.
     *
     * @return The number of RayWeightSets created.
     */
    public static int getRayWeightSetsCreated() {
        return aRayWeightSetsCreated;
    }

    /**
     * Initializes this object statically with the MatrixBlockInfo object that
     * describes the covariance matrix structure and the RayWeightSet object with
     * its static parameters.
     *
     * @param elemLimit The RayWeightSet element limit above which the
     *                  RayWeightSet becomes locked to any future phase / site
     *                  entry additions.
     * @param pth       The path to where all ray weight block row files are
     *                  stored.
     * @param mbd       The MatrixBlockInfo object that describes the structure
     *                  of the covariance matrix.
     * @param procmap   The static ObservationsPhaseSiteMap that is created and
     *                  maintained by the RayUncertainty object but used by the
     *                  RayWeightSet object to fetch references of previously
     *                  instantiated ObservationSourceMap objects.
     */
    public static void initialize(long elemLimit, String pth,
                                  MatrixBlockDefinition mbd,
                                  ObservationsPhaseSiteMap procmap) {
        RayWeightSet.initialize(elemLimit, pth, procmap);
        ObservationsSourceMap.setMatrixDefn(mbd);
    }

    /**
     * Returns the total number of unique phase / site entries processed thus
     * far by this ray weight set list
     *
     * @return The total number of unique phase / site entries processed thus
     * far by this ray weight set list
     */
    public int getPhaseSiteCount() {
        if (aCurrentRayWghtSet != null)
            return aPhaseSiteCount + aCurrentRayWghtSet.getPhaseSiteCount();
        else
            return aPhaseSiteCount;
    }

    /**
     * Adds a new site pair to the site pair set used by the type AB pair
     * RayWeightSet.
     *
     * @param siteIdA The receiver A id to add to the set.
     * @param siteIdB The receiver B id to add to the set.
     */
    public void addSitePair(long siteIdA, long siteIdB) {
        if (aSitePairSet == null) aSitePairSet = new HashSet<Long>();
        aSitePairSet.add(siteIdA);
        aSitePairSet.add(siteIdB);
    }

    /**
     * Primary function called during ray uncertainty prediction
     * (RayUncertainty.predictRays()) to populate ray weights that will be
     * written to disk.
     *
     * @param phase  The phase of the input ray.
     * @param siteid The receiver id of the input ray.
     * @param srcid  The source id of the input ray.
     * @param indx   The array of matrix column indices of the ray.
     * @param wght   The array of matrix column weights of the ray.
     * @throws IOException
     */
    public void add(SeismicPhase phase, long siteid, long srcid,
                    int[] indx, double[] wght) throws IOException {
        // first try to add the array to any locked RayWeightSets. These are locked
        // because there ray weight count limit has been exceeded and they are no
        // longer allowed to add any more new phase / site entries ... loop over
        // all locked sets

        for (RayWeightSet bas : aLockedSets) {
            // see if the ray was added successfully to the next set (bas)

            if (bas.add(phase, siteid, srcid, indx, wght)) {
                // ray was added to bas ... see if the RayWeightSet is complete (all
                // rays have been added)

                if (bas.isComplete()) {
                    // set is complete ... remove it from the locked set and see if any
                    // of its entries should be referenced in the type AB pair
                    // RayWeightSet ... then write the RayWeightSet to disk.

                    aLockedSets.remove(bas);
                    updatePairBlockArray(bas);
                    bas.writeSet();
                }

                // ray was added successfully ... return

                return;
            }
        }

        // no locked sets or none added ray successfully ... add to the current
        // RayWeightSet ... make sure it exists or create it if not

        if (aCurrentRayWghtSet == null) {
            ++aRayWeightSetsCreated;
            aCurrentRayWghtSet = new RayWeightSet(aSetId++);
        }

        // add ray to current RayWeightSet (not locked)

        if (aCurrentRayWghtSet.add(phase, siteid, srcid, indx, wght)) {
            // successfully added ray ... see if it is now locked

            if (aCurrentRayWghtSet.isLocked()) {
                // current RayWeightSet is now locked ... see if it is complete

                if (aCurrentRayWghtSet.isComplete()) {
                    // current RayWeightSet is locked and complete ... see if any
                    // of its entries should be referenced in the type AB pair
                    // RayWeightSet ... write the current RayWeightSet to disk.

                    updatePairBlockArray(aCurrentRayWghtSet);
                    aCurrentRayWghtSet.writeSet();
                } else {
                    // not complete ... add it to the locked set

                    aLockedSets.add(aCurrentRayWghtSet);
                }

                // increment the phase site count and set the current RayWeightSet to
                // null

                aPhaseSiteCount += aCurrentRayWghtSet.getPhaseSiteCount();
                aCurrentRayWghtSet = null;

                // see if any other phase / sites entries remain to be added. If so
                // then create a new current RayWeightSet and increment the creation
                // count

                if (aPhaseSiteCount <
                        RayWeightSet.primaryObservationsPhaseSiteMap().getPhaseSiteCount()) {
                    // more to process ... create another ray weight set

                    ++aRayWeightSetsCreated;
                    aCurrentRayWghtSet = new RayWeightSet(aSetId++);
                }
            }
        } else {
            // throw an error if the ray could not be added to the current
            // RayWeightSet (this should never happen)

            throw new IOException("Error: could not add observation ...");
        }
    }

    /**
     * Returns the total number of sets created thus far.
     *
     * @return The total number of sets created thus far.
     */
    public int getSetCount() {
        return aSetId;
    }

    /**
     * Updates the type AB pair RayWeightSet with any entries in the input
     * RayWeightSet that it needs. if the site pair set is not defined then this
     * function exits without any action. Otherwise rws is checked for any site
     * entries that are also in the site pair set (aSitePairSet). If any are
     * found then they are referenced into the type AB pair RayWeightSet
     * (aPairRayWghtSet).
     *
     * @param rws The input RayWeightSet that will be checked for entries that
     *            are needed by the type AB pair RayWeightSet.
     */
    private void updatePairBlockArray(RayWeightSet rws) {
        ObservationsSourceMap pso;

        // exit if site pair covariance was not requested

        if (aSitePairSet == null) return;

        // get all unique phases and loop over all site entries in the site pair
        // set

        SeismicPhase[] phaseArray = rws.getPhases();
        for (Long siteId : aSitePairSet) {
            // try each phase with the site id

            for (int i = 0; i < phaseArray.length; ++i) {
                // get the matching ObservationsSourceMap from rws and see if it was
                // defined (not null)

                pso = rws.get(phaseArray[i], siteId);
                if (pso != null) {
                    // defined ... add it to the type AB pair RayWeightSet ... make sure
                    // it exists first ... create it if necessary

                    if (aPairRayWghtSet == null) {
                        ++aRayWeightSetsCreated;
                        aPairRayWghtSet = new RayWeightSet(-1);
                    }

                    // add pso to the pair ray weight set

                    aPairRayWghtSet.add(pso);
                }
            }
        }
    }

    /**
     * Called one time by RayUncertainty.predictRays() when all prediction has
     * completed to write out any remaining RayWeightSets. These can include the
     * current RayWeightSet, which must be complete or an error is thrown, and
     * the type AB pair RayWeightSet, if it was defined.
     *
     * @throws IOException
     */
    public void writeLast() throws IOException {
        // Throw an error if any locked sets remain (this should never happen)

        if (aLockedSets.size() > 0) {
            throw new IOException("Error: Set of RayWeightSet Objects Is Not " +
                    "Empty (" + aLockedSets.size() + ") ...");
        }

        // see if the current ray weight set is defined

        if (aCurrentRayWghtSet != null) {
            // defined ... throw an error if not complete

            if (!aCurrentRayWghtSet.isComplete()) {
                throw new IOException("Error: Current RayWeightSet Object Is Not " +
                        "Complete ...");
            }

            // update the type AB pair RayWeightSet with the current RayWeightSet and
            // write the current RayWeightSet to disk. If it has no entries throw an
            // error (this should never happen).

            if (aCurrentRayWghtSet.getPhaseSiteCount() > 0) {
                updatePairBlockArray(aCurrentRayWghtSet);
                aCurrentRayWghtSet.writeSet();
            } else {
                throw new IOException("Error: Current RayWeightSet Object Has " +
                        "0 Entries ...");
            }

            // set the current RayWeightSet to null and continue

            aCurrentRayWghtSet = null;
        }

        // check the type AB pair RayWeightSet to see if it was defined

        if (aPairRayWghtSet != null) {
            // defined ... throw an error if it is not complete

            if (!aPairRayWghtSet.isComplete()) {
                throw new IOException("Error: Pair RayWeightSet Object Is Not " +
                        "Complete ...");
            }

            // write to disk and set to null ...

            aPairRayWghtSet.writeSet();
            aPairRayWghtSet = null;
        }
    }
}
