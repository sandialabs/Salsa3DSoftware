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
package gov.sandia.gnem.slbmjni;

import java.util.ArrayList;

/**
 * <p>A Java Native Interface to the <a href="../../SLBM/doc/html/index.html">SLBM</a> C++ library, providing access to all supported functionality.</p>
 * <p>SlbmInterface (Java) manages a connection to the C++ <a href="../../SLBM/doc/html/index.html">SLBM</a> library. To make this work, 5 conditions must be met:</p>
 * <ol>
 * <li>The C++ shared objects <b>libgeotesscpp.so</b>, <b>libslbm.so</b> and <b>libslbmjni.so</b> must both be accessible via the <b>LD_LIBRARY_PATH</b>.</li>
 * <li>The java application must include the jar file <b>slbmjni.jar</b> in its classpath.</li>
 * <li>The java application must <b>import gov.sandia.gnem.slbmjni.*</b>;</li>
 * <li>The java application must execute the command: <b>System.loadLibrary("slbmjni");</b></li>
 * <li>The java applicaiton can then instantiate a new SlbmInterface object by calling <b>SlbmInterface slbm = new SlbmInterface();</b></li>
 * </ol>
 * <p>If all of these conditions are successfully met, then the the Java application can use the <b>slbm</b> attribute to access all the methods described in this document.</p>
 * <p>A SlbmInterface object maintains a C++ <a href="../../../../../../../../SLBM/doc/html/class_grid.html">Grid</a> object, which manages interaction with the Earth model.  The Earth model is loaded by calling the {@link #loadVelocityModelBinary} method, described below.  This <a href="../../../../../../../../SLBM/doc/html/class_grid.html">Grid</a> object remains in memory until the SlbmInterface object goes out of scope, or a different Earth model is loaded with another call to {@link #loadVelocityModelBinary}.</p>
 * <p>SlbmInterface also maintains a single instance of a C++ <a href="../../../../../../../../SLBM/doc/html/class_great_circle.html">GreatCircle</a> object which is instantiated with a call to <a href="#creatGreatCircleAnchor">createGreatCircle()</a>. Once instantiated, many SlbmInterface methods can retrieve information from this <a href="../../../../../../../../SLBM/doc/html/class_great_circle.html">GreatCircle</a> object, such as {@link #getTravelTime()}, {@link #getWeights()}, {@link #toString}, and more.  Once instantiated, the <a href="../../../../../../../../SLBM/doc/html/class_great_circle.html">GreatCircle</a> can be interrogated until it is replaced with another <a href="../../../../../../../../SLBM/doc/html/class_great_circle.html">GreatCircle</a> by a subsequent call to <a href="#creatGreatCircleAnchor">createGreatCircle()</a>, or is deleted by {@link #clear}.</p>
 * <p>The <a href="../../../../../../../../SLBM/doc/html/class_grid.html">Grid</a> object owned by SlbmInterface stores a vector of map objects which associates the phase and <a href="../../../../../../../../SLBM/doc/html/class_location.html">Location</a> of a <a href="../../../../../../../../SLBM/doc/html/class_crustal_profile.html">CrustalProfile</a> object with a pointer to the instance of the <a href="../../../../../../../../SLBM/doc/html/class_crustal_profile.html">CrustalProfile</a>. When <a href="#creatGreatCircleAnchor">createGreatCircle()</a> is called with a latitude, longitude and depth which has been used before, the <a href="../../../../../../../../SLBM/doc/html/class_grid.html">Grid</a> object will return a pointer to the existing <a href="../../../../../../../../SLBM/doc/html/class_crustal_profile.html">CrustalProfile</a> object, thereby enhancing performance. This vector of maps is cleared when {@link #clear()} is called.  The implications of all this is that applications that loop over many calls to <a href="#creatGreatCircleAnchor">createGreatCircle()</a> will see a performance improvement if {@link #clear()} is not called within the loop.  However, for problems where the number of sources and/or receivers is so large that memory becomes an issue, applications could call {@link #clear()} within the loop to save memory.</p>
 * <p>All calculations assume the Earth is defined by a GRS80 ellipsoid. For a description of how points along a great circle are calculated see <a href="../../doc/geovectors.pdf">SLBM_Root/doc/geovectors.pdf</a></p>
 * <hr>
 * <p>Copyright 2009 National Technology &amp; Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.</p>
 * <p>BSD Open Source License</p>
 * <p>All rights reserved.</p>
 * <p>Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:</p>
 * <ol>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.</li>
 * </ol>
 * <p>THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.</p>
 * <p>Author:  Brian A. Young et al.</p>
 * <p>Version: 3.2.0</p>
 */
public class SlbmInterface implements AutoCloseable
{
    protected int vtkId = 0;  // This is needed in order to use slbmJavaUtil

    public static final int PWAVE=0;
    public static final int SWAVE=1;

    public enum Layers
    {
        WATER, SEDIMENT1, SEDIMENT2, SEDIMENT3, UPPER_CRUST, MIDDLE_CRUST_N,
        MIDDLE_CRUST_G, LOWER_CRUST, MANTLE
    }

    /**
     * This is the constructor that should be used under normal circumstances.
     */
    public SlbmInterface ()
    {
        SlbmInterfaceNative();
    }
    private native void SlbmInterfaceNative();

    /**
     * Location::EARTH_RADIUS is a variable that controls how the radius of
     * the earth is defined.  It is initialized to -1 in SLBM::Location.cc, outside
     * the class definition. If Location::EARTH_RADIUS is less than zero, then
     * Location::getEarthRadius() will return the radius of the earth that
     * is a function of geocentric latitude (larger at the equator, smaller
     * at the poles).  If Location::EARTH_RADIUS is &gt; 0, then getEarthRadius()
     * returns Location::EARTH_RADIUS.  Applications can modify
     * Location::EARTH_RADIUS by calling this routine.
     * <p>In it's default state, SLBM will return travel times that do not
     * require application of ellipticity corrections, since the radius of
     * the elliptical earth is computed at every latitude.  However, when comparing
     * SLBM results to results obtained with radially symmetric, 1D models,
     * or any model which assumes a spherical earth, it is important to
     * fix the Earth radius to the correct value for the comparison (usually
     * 6371 km).
     * @param earthRadius double earth radius in km.
     */
    public SlbmInterface (double earthRadius)
    {
        SlbmInterfaceFixedEarthRadiusNative(earthRadius);
    }
    private native void SlbmInterfaceFixedEarthRadiusNative(double earthRadius);

    /**
     * Delete the C++ SlbmInterface object from memory.
     */
    @SuppressWarnings("deprecation")  // we're overriding this, so turn off the deprecation warning
    @Override
    public void finalize()
    {
        closeNative();
    }

    /**
     * Delete the C++ SlbmInterface object from memory.
     */
    @Override
    public void close()
    {
        closeNative();
    }
    private native void closeNative();

    /**
     * Retrieve the SLBM version number.
     * @return String
     */
    public String getVersion()
    {
        return getVersionNative();
    }
    private native String getVersionNative();

    /**
     * Load the velocity model into memory from the specified file.
     * @param modelFileName String
     * @throws SLBMException if native method fails
     */
    public void loadVelocityModel (String modelFileName)
    throws SLBMException
    {
        loadVelocityModelNative(modelFileName);
    }
    private native void loadVelocityModelNative (String modelFileName);

    /**
     * Load the velocity model into memory from the specified file.
     * @param modelDirectory String
     * @throws SLBMException if native method fails
     */
    public void loadVelocityModelBinary( String modelDirectory)
            throws SLBMException
    { loadVelocityModelNative( modelDirectory ); }

    /**
     * Save the velocity model currently in memory to the specified file.
     * @param modelFileName String
     * @throws SLBMException if native method fails
     */
    public void saveVelocityModel (String modelFileName)
    throws SLBMException
    { saveVelocityModelNative(modelFileName, 4); }

    /**
     * \brief Save the velocity model currently in memory to
     * the specified file.
     *
     * Save the velocity model currently in memory to
     * the specified file or directory.
     *
     * <p>The following formats are supported:
     * <ol>
     * <li>SLBM version 1 ascii file.  All model information is
     * output to a single file in ascii format.
     * This format was available in SLBM version 2, but never used.
     *
     * <li>SLBM version 2 directory format. Model information is
     * output to a number of different files and directories, mostly
     * in binary format.  This format was used almost exclusively in
     * SLBM version 2.
     *
     * <li>SLBM version 3 directory format. Model information is
     * output to a number of different files and directories, mostly
     * in binary format.  This format is very similar to format 2
     * with the difference being that the model tessellation and values
     * are stored in GeoTess format instead of the custom SLBM format.
     *
     * <li>SLBM version 3 single-file format.  This is the default+preferred
     * format.  All model information is written to a single file.
     * If the modelFileName extension is '.ascii' the file is written
     * in ascii format, otherwise it is written in binary format.
     * </ol>
     *
     * See SLBM_Design.pdf in the main documentation directory
     * for detailed information about model output formats.
     *
     * <p>Models stored in SLBM version 1 and 2 formats (formats 1 and 2)
     * only support linear interpolation.  Models stored in SLBM
     * version 3 formats (formats 3 and 4) support both linear
     * and natural neighbor interpolation.
     *
     * @param modelFileName the full or relative path to the
     * file or directory to which the earth model is to
     * be written.  For formats 2 and 3, the directory will be
     * created if it does not exist.
     * @param format the desired format of the output.
     * If omitted, defaults to 4: all model information written to a single
     * file.
     * @throws SLBMException if native method fails
     */
    public void saveVelocityModel (String modelFileName, int format)
            throws SLBMException
            {
        try
        {
            saveVelocityModelNative(modelFileName, format);
        }
        catch(NullPointerException npe) { /* ignore */ }
            }
    private native String saveVelocityModelNative (String modelFileName, int format);

    /**
     * Specify the directory where the model that is currently in memory should be
     * written to the next time that saveVelocityModelBinary() is called. A call to
     * specifyOutputDirectory() will check to make sure that the specified
     * directory exists. If it does not exist, slbm will attempt to create it. Then
     * slbm will write a very small file called deleteme.buf to the specified
     * directory and then delete that file. All of this is done to ensure that when
     * the model is actually written to the specified directory, the probability of
     * error is minimized. Call method saveVelocityModelBinary() to actually write
     * the model to the specified directory.
     *
     * <p>Note that specified directory name is associated with the Grid object
     * currently in memory. If the current Grid object is deleted and a different
     * Grid object loaded, the directoryName will be reset to "".
     *
     * @param directoryName String
     */
    public void specifyOutputDirectory( String directoryName )
    { specifyOutputDirectoryNative( directoryName ); }
    private native void specifyOutputDirectoryNative( String directoryName );

    /**
     * Write the model currently in memory out to files.
     * The model is written to a directory which was previously
     * specified with a call to specifyOutputDirectory().
     */
    public void  saveVelocityModelBinary() { saveVelocityModelBinaryNative(); }
    private native void saveVelocityModelBinaryNative();

    /**
     * <div id="creatGreatCircleAnchor"></div>
     * Instantiate a new GreatCircle object between two locations.
     * @param phase the phase that this GreatCircle is to
     * support.  Recognized phases are Pn, Sn, Pg and Lg.
     * @param sourceLat the geographic latitude of the source
     * in radians.
     * @param sourceLon the geographic longitude of source in radians.
     * @param sourceDepth the depth of the source in km.
     * @param receiverLat the geographic latitude of the receiver
     * in radians.
     * @param receiverLon the geographic longitude of the receiver in radians.
     * @param receiverDepth the depth of the receiver in km.
     * along the head wave interface, in radians.
     * @throws SLBMException if native method fails
     */
    public void createGreatCircle(
            String phase,
            double sourceLat,
            double sourceLon,
            double sourceDepth,
            double receiverLat,
            double receiverLon,
            double receiverDepth)
    throws SLBMException
    {
        createGreatCircleNative(
                phase.equals("Pn") ? 0 : phase.equals("Sn") ? 1 :
                    phase.equals("Pg") ? 2 : phase.equals("Lg") ? 3 : -1,
                            sourceLat,
                            sourceLon,
                            sourceDepth,
                            receiverLat,
                            receiverLon,
                            receiverDepth);
    }
    private native void createGreatCircleNative(
            int phase,
            double sourceLat,
            double sourceLon,
            double sourceDepth,
            double receiverLat,
            double receiverLon,
            double receiverDepth);

    /**
     * Delete the current
     * <a href="../../../../../../../../SLBM/doc/html/class_great_circle.html">GreatCircle</a>
     * object from memory and clear the pool of stored
     * <a href="../../../../../../../../SLBM/doc/html/class_crustal_profile.html">CrustalProfile</a>
     * objects.
     * The model
     * <a href="../../../../../../../../SLBM/doc/html/class_grid.html">Grid</a>
     * is not deleted and remains accessible.
     *
     * The
     * <a href="../../../../../../../../SLBM/doc/html/class_grid.html">Grid</a>
     * object owned by SlbmInterface stores a vector of map objects which associates
     * the phase and
     * <a href="../../../../../../../../SLBM/doc/html/class_location.html">Location</a>
     * of a
     * <a href="../../../../../../../../SLBM/doc/html/class_crustal_profile.html">CrustalProfile</a>
     * object with a pointer to the instance
     * of the
     * <a href="../../../../../../../../SLBM/doc/html/class_crustal_profile.html">CrustalProfile</a>
     * .  When
     * {@link #createGreatCircle}
     * is called with a latitude,
     * longitude and depth which has been used before, the
     * <a href="../../../../../../../../SLBM/doc/html/class_grid.html">Grid</a>
     * object will return a
     * pointer to the existing
     * <a href="../../../../../../../../SLBM/doc/html/class_crustal_profile.html">CrustalProfile</a>
     * object, thereby enhancing performance.
     * This vector of maps is cleared when {@link #clear()} is called.  The
     * implications of all this is that applications that loop over many calls to
     * {@link #createGreatCircle} will see a performance improvement if {@link #clear()} is not called
     * within the loop.  However, for problems with a huge number of sources and or receivers,
     * if memory becomes an issue, applications could call {@link #clear()} within the loop
     * to save memory.
     */
    public void clear()
    {
        clearNative();
    }
    private native void clearNative();

    /**
     * Retrieve a formatted String representation of the results computed by a
     * GreatCircle object.
     * @param verbosity specifies the amount of information
     * that is to be included in the return string.  Each
     * verbosity level includes all information in preceeding
     * verbosity levels.<br>
     * - 0 : nothing.  An empty string is returned.<br>
     * - 1 : total distance and travel time summary<br>
     * - 2 : gradient correction information for Pn/Sn. Nothing for Pg/Lg<br>
     * - 3 : Source and receiver profiles<br>
     * - 4 : Grid node weights.<br>
     * - 5 : Head wave interface profiles<br>
     * - 6 : Interpolation coefficients for great circle nodes on the head wave interface.<br>
     * @return String a formatted String representation of the results computed by a
     * GreatCircle object.
     * @throws SLBMException if native method fails
     */
    public String toString(int verbosity)
    throws SLBMException
    {
        return toStringNative(verbosity);
    }
    private native String toStringNative(int verbosity);

    /**
     * Retrieve the total travel time for the GreatCircle, in seconds.
     * @return double the total travel time for the GreatCircle, in seconds.
     * @throws SLBMException if native method fails
     */
    public double getTravelTime()
    throws SLBMException
    {
        double travelTime = getTravelTimeNative();
        return travelTime;
    }
    private native double getTravelTimeNative();

    /**
     * Retrieve phase specified in last call to createGreatCircle()
     * @return String phase
     * @throws SLBMException if native method fails
     */
    public String getPhase() throws SLBMException  { return getPhaseNative(); }
    private native String getPhaseNative();

    /**
     * Retrieve the source-receiver separation, in radians.
     * @return double the source-receiver separation, in radians.
     * @throws SLBMException if native method fails
     */
    public double getDistance() throws SLBMException  { return getDistanceNative(); }
    private native double getDistanceNative();

    /**
     *
     * Retrieve horizontal offset below the source, in radians.
     * This is the angular distance between the location of the
     * source and the source pierce point where the ray impinged
     * on the headwave interface.
     * @return the horizontal offset below the source, in radians.
     * @throws SLBMException if native method fails
     */
    public double getSourceDistance() throws SLBMException  { return getSourceDistanceNative(); }
    private native double getSourceDistanceNative();

    /**
     * Retrieve horizontal offset below the receiver, in radians.
     * This is the angular distance between the location of the
     * receiver and the receiver pierce point where the ray impinged
     * on the headwave interface.
     * @return the horizontal offset below the receiver, in radians.
     * @throws SLBMException if native method fails
     */
    public double getReceiverDistance() throws SLBMException  { return getReceiverDistanceNative(); }
    private native double getReceiverDistanceNative();

    /**
     * Retrieve the angular distance traveled by the ray
     * below the headwave interface, in radians.
     * This is the total distance minus the horizontal offsets
     * below the source and receiver.  getSourceDistance() +
     * getReceiverDistance() + getHeadwaveDistance() =
     * getDistance().
     * @return the angular distance traveled by the ray
     * below the headwave interface, in radians.
     * @throws SLBMException if native method fails
     */
    public double getHeadwaveDistance() throws SLBMException  { return getHeadwaveDistanceNative(); }
    private native double getHeadwaveDistanceNative();

    /**
     * Retrieve horizontal distance traveled by the ray
     * below the headwave interface, in km.
     * This is the sum of actual_path_increment(i) * R(i) where actual_path_increment(i) is the
     * angular distance traveled by the ray in each angular
     * distance increment along the head wave interface, and R(i)
     * is the radius of the head wave interface in that same
     * horizontal increment.
     * @return the horizontal distance traveled by the ray
     * below the headwave interface, in km.
     * @throws SLBMException if native method fails
     */
    public double getHeadwaveDistanceKm() throws SLBMException  { return getHeadwaveDistanceKmNative(); }
    private native double getHeadwaveDistanceKmNative();

    /**
     * Retrieve the total travel time and all the components that
     * contributed to the total.  All values are in seconds.
     * @return double[]<br>
     * t[0] : total travel time <br>
     * t[1] : crustal travel time below the source<br>
     * t[2] : crustal travel time below the receiver<br>
     * t[3] : travel time along the head wave interface<br>
     * t[4] : gradient correction term. Zero for Pg/Lg<br>
     * @throws SLBMException if native method fails
     */
    public double[] getTravelTimeComponents()
    throws SLBMException
    {
        return getTravelTimeComponentsNative();
    }
    private native double[] getTravelTimeComponentsNative();

    /**
     * Retrieve the weight assigned to each grid node that
     * was touched by the GreatCircle that is currently in memory.
     * <p>A map which associates an instance of a
     * GridProfile object with a double <i>weight</i> is initialized.
     * Then every LayerProfile on the head wave interface between the source
     * and receiver is visited and the angular distance, <i>d</i>, that the ray
     * traveled in the horizontal segment is retreived.  If <i>d</i> &gt; 0,
     * then the neighboring GridProfile objects that contributed to
     * the interpolated value of the LayerProfile are visited.
     * The product of <i>d * R * C</i>  is added to the weight associated
     * with that GridProfile object, where <i>R</i> is the radius of the
     * head wave interface for the LayerProfile object being evaluated,
     * and <i>C</i> is the interpolation coefficient for the
     * GridProfile - LayerProfile pair under consideration.
     * Then, all the GridProfile objects in the map are visited, the
     * grid node IDs extracted into int array <i>node</i>, and the
     * <i>weight</i> extracted into double array <i>weight</i>.
     *
     * <p>Note: Only grid nodes touched by this GreatCircle are included in the
     * output.  Each grid node is included only once, even though more than
     * one LayerProfile object may have contributed some weight to it.
     * The sum of all the weights will equal the horizontal distance
     * traveled by the ray along the head wave interface, from the source
     * pierce point to the receiver pierce point, in km.
     * @return GridWeight a storage container for the results.
     * @throws SLBMException if native method fails
     */
    public GridWeight getWeights()
    throws SLBMException
    {
        GridWeight weights = new GridWeight();
        computeWeightsNative();
        weights.node = getWeightNodesNative();
        weights.weight = getWeightsNative();
        deleteWeightsNative();

        return weights;
    }
    private native void computeWeightsNative();
    private native int[] getWeightNodesNative();
    private native double[] getWeightsNative();
    private native void deleteWeightsNative();


    public GridWeight getWeightsSource()
    throws SLBMException
    {
        GridWeight weights = new GridWeight();
        computeWeightsSourceNative();
        weights.node = getWeightNodesNative();
        weights.weight = getWeightsNative();
        deleteWeightsNative();

        return weights;
    }
    private native void computeWeightsSourceNative();


    public GridWeight getActiveNodeWeightsSource()
    throws SLBMException
    {
        GridWeight weights = new GridWeight();
        computeWeightsSourceNative();
        weights.node = getWeightNodesNative();
        for (int i = 0; i < weights.node.length; ++i)
        {
            weights.node[i] = getActiveNodeId(weights.node[i]);
        }
        weights.weight = getWeightsNative();
        deleteWeightsNative();

        return weights;
    }


    public GridWeight getWeightsReceiver()
    throws SLBMException
    {
        GridWeight weights = new GridWeight();
        computeWeightsReceiverNative();
        weights.node = getWeightNodesNative();
        weights.weight = getWeightsNative();
        deleteWeightsNative();

        return weights;
    }
    private native void computeWeightsReceiverNative();


    public GridWeight getActiveNodeWeightsReceiver()
    throws SLBMException
    {
        GridWeight weights = new GridWeight();
        computeWeightsReceiverNative();
        weights.node = getWeightNodesNative();
        for (int i = 0; i < weights.node.length; ++i)
        {
            weights.node[i] = getActiveNodeId(weights.node[i]);
        }
        weights.weight = getWeightsNative();
        deleteWeightsNative();

        return weights;
    }



    /**
     * Retrieve the node IDs for grid nodes that contributed to the
     * interpolation of values at the source location.  There should be 4
     * of these.
     * @return int[] source node ids.
     */
    public int[] getSourceNodeIds()
    {
        return getSourceNodeIdsNative();
    }
    private native int[] getSourceNodeIdsNative();

    /**
     * Retrieve the interpolation coefficients for grid nodes that contributed to the
     * interpolation of values at the source location.  There should be 4 of these
     * and they should sum to one.
     * @return int[] source interpolation coefficients
     */
    public double[] getSourceCoefficients()
    {
        return getSourceCoefficientsNative();
    }
    private native double[] getSourceCoefficientsNative();

    /**
     * Retrieve the node IDs for grid nodes that contributed to the
     * interpolation of values at the receiver location.  There should be 4 of these.
     * @return int[] receiver node ids.
     */
    public int[] getReceiverNodeIds()
    {
        return getReceiverNodeIdsNative();
    }
    private native int[] getReceiverNodeIdsNative();

    /**
     * Retrieve the interpolation coefficients for grid nodes that contributed to the
     * interpolation of values at the receiver location.  There should be 4 of these
     * and they should sum to one.
     * @return int[] receiver interpolation coefficients
     */
    public double[] getReceiverCoefficients()
    {
        return getReceiverCoefficientsNative();
    }
    private native double[] getReceiverCoefficientsNative();

    /**
     * Retrieve data from a single grid node in the earth model.
     * @param nodeId the grid node ID number.
     * @return GridProfile
     * @throws SLBMException if native method fails
     */
    public GridProfile getGridData(int nodeId)
    throws SLBMException
    {
        GridProfile gp = new GridProfile();
        accessGridProfileNative(nodeId);
        gp.nodeId = nodeId;
        gp.lat = getGridLatNative();
        gp.lon = getGridLonNative();
        gp.depth = getGridDepthNative();
        gp.velocity[0] = getGridVelocityNative(0);
        gp.velocity[1] = getGridVelocityNative(1);
        gp.gradient = getGridGradientNative();
        return gp;
    }
    private native void accessGridProfileNative(int nodeId);
    private native double getGridLatNative();
    private native double getGridLonNative();
    private native double[] getGridDepthNative();
    private native double[] getGridVelocityNative(int waveType);
    private native double[] getGridGradientNative();
    private native double[] setGridDepthNative(double[] depth);
    private native double[] setGridVelocityNative(int waveType, double[] velocity);
    private native double[] setGridGradientNative(double[] gradient);

    /**
     * Modify the velocity and gradient information for a single
     * grid node in the earth model currently in memory.  The
     * Earth model in the input file is not modified.  To save the
     * Earth model that is currently in memory to the file system
     * call method {@link #saveVelocityModel(String)}
     * @param gridProfile GridProfile
     * containing the nodeId, velocity and gradient information that
     * is to replace the information currently stored in the
     * Earth model in memory.
     * @throws SLBMException if native method fails
     */
    public void setGridData(GridProfile gridProfile)
    throws SLBMException
    {
//		accessGridProfileNative(gridProfile.nodeId);
//		setGridDepthNative(gridProfile.depth);
//		setGridVelocityNative(0, gridProfile.velocity[0]);
//		setGridVelocityNative(1, gridProfile.velocity[1]);
//		setGridGradientNative(gridProfile.gradient);
        setGridDataNative(gridProfile.nodeId, gridProfile.depth, gridProfile.velocity[0],
                gridProfile.velocity[1], gridProfile.gradient);
    }
    private native void setGridDataNative(int nodeId, double[] depths,
            double[] pvelocities, double[] svelocities, double[] gradients);

    /**
     * Modify the velocity and gradient information associated with a specified
     * active node in the Grid.
     *
     * @param gridProfile the node number of the grid point in the model. (zero
     *   based index).
     * @throws SLBMException if native method fails
     */
    public void setActiveNodeData(GridProfile gridProfile)
    throws SLBMException
    {
        setGridDataNative(getGridNodeId(gridProfile.nodeId), gridProfile.depth, gridProfile.velocity[0],
                gridProfile.velocity[1], gridProfile.gradient);
    }


    /**
     * Retrieve interpolated data from the earth model at a single
     * specified latitude, longitude.
     * @param lat the latitude where information is to be interpolated,
     * in radians.
     * @param lon the longitude where information is to be interpolated,
     * in radians.
     * @return QueryProfile
     * @throws SLBMException if native method fails
     */
    public QueryProfile getInterpolatedPoint(double lat, double lon)
    throws SLBMException
    {
        QueryProfile qp = new QueryProfile();
        createQueryProfileNative(lat, lon);
        qp.lat = lat;
        qp.lon = lon;
        qp.nodeId = getQueryNodeIdNative();
        qp.depth = getQueryDepthNative();
        qp.coefficient = getQueryCoefficientNative();
        qp.velocity[0] = getQueryVelocityNative(0);
        qp.velocity[1] = getQueryVelocityNative(1);
        qp.gradient = getQueryGradientNative();
        deleteQueryProfileNative();
        return qp;
    }
    private native void createQueryProfileNative(double lat, double lon);
    private native void deleteQueryProfileNative();
    private native int[] getQueryNodeIdNative();
    private native double[] getQueryDepthNative();
    private native double[] getQueryCoefficientNative();
    private native double[] getQueryVelocityNative(int waveType);
    private native double[] getQueryGradientNative();

    /**
     * Retrieve interpolated data from the earth model along a 1 dimensional
     * transect across the model.
     * @param lat the latitudes where information is to be interpolated,
     * in radians.
     * @param lon the longitudes where information is to be interpolated,
     * in radians.
     * @return QueryProfile
     * @throws SLBMException if native method fails
     */
    public ArrayList<QueryProfile> getInterpolatedTransect(ArrayList<Double> lat,
            ArrayList<Double> lon)
            throws SLBMException
            {
        ArrayList<QueryProfile> qp = new ArrayList<QueryProfile>(lat.size());
        for (int i=0; i<lat.size(); i++)
            qp.add(getInterpolatedPoint(lat.get(i), lon.get(i)));
        return qp;
            }

    /**
     * Retrieve the information required for input to the travel time
     * calculation, for the GreatCircle object currently in memory.
     * @return GreatCircleData a storage container for the results.
     * @throws SLBMException if native method fails
     */
    public GreatCircleData getGreatCircleData()
    throws SLBMException
    {
        GreatCircleData gcd = new GreatCircleData();
        gcd.phase = getGreatCirclePhaseNative();
        gcd.actualPathIncrement = getActualPathIncrementNative();
        gcd.sourceDepths = getGreatCircleSourceDepthNative();
        gcd.receiverDepths = getGreatCircleReceiverDepthNative();
        gcd.sourceVelocitites = getGreatCircleSourceVelocityNative();
        gcd.receiverVelocities = getGreatCircleReceiverVelocityNative();
        gcd.headWaveVelocities = getGreatCircleHeadwaveVelocityNative();
        gcd.mantleGradients = getGreatCircleHeadwaveGradientNative();
        int n = gcd.headWaveVelocities.length;
        gcd.neighbors = new int[n][0];
        gcd.coefficients = new double[n][0];
        for (int i=0; i<n; i++)
        {
            gcd.neighbors[i] = getGreatCircleNeighborsNative(i);
            gcd.coefficients[i] = getGreatCircleCoefficientsNative(i);
        }

        return gcd;
    }
    private native String getGreatCirclePhaseNative();
    private native double getActualPathIncrementNative();
    private native int[] getGreatCircleNeighborsNative(int i);
    private native double[] getGreatCircleCoefficientsNative(int i);
    private native double[] getGreatCircleSourceDepthNative();
    private native double[] getGreatCircleReceiverDepthNative();
    private native double[] getGreatCircleSourceVelocityNative();
    private native double[] getGreatCircleReceiverVelocityNative();
    private native double[] getGreatCircleHeadwaveVelocityNative();
    private native double[] getGreatCircleHeadwaveGradientNative();



    /**
     * Retrieve the latitudes, longitudes and depths of all the profile positions
     * along the moho.  Profile positions are located at the center of each segment
     * of the head wave interface between the source and receiver.  The first position
     * is located actualPathIncrement/2 radians from the source, the last profile position is located
     * actualPathIncrement/2 radians from the receiver, and the others are spaced actualPathIncrement radians apart.
     * @return double[][] a 3 x npoints array containing the latitudes,
     * longitudes and depths of the points along the great circle.
     * Latitudes and longitudes are in radians.  Depths are in km.
     * @throws SLBMException if native method fails
     */
    public double[][] getGreatCircleLocations()
    throws SLBMException
    {
        double[][] locations = new double[3][0];
        computeGreatCircleLocationsNative();
        locations[0] = getGreatCirclePointsLatNative();
        locations[1] = getGreatCirclePointsLonNative();
        locations[2] = getGreatCirclePointsDepthNative();
        deleteGreatCirclePointsNative();
        return locations;
    }
    private native void computeGreatCircleLocationsNative();

    /**
     * Retrieve an array of lat, lon points along a great circle path
     * between two points, a and b. The great circle path between a and b is
     * divided into npoints-1 equal size cells and the computed points are
     * located at the boundaries of those cells.  First point will
     * coincide with point a and last point with point b.
     * @param aLat double the latitude of the first point in radians.
     * @param aLon double the longitude of the first point in radians.
     * @param bLat double the latitude of the second point in radians.
     * @param bLon double the longitude of the second point in radians.
     * @param npoints int the number of points desired.
     * @return double[][] a 2 x npoints array containing the latitudes and
     * longitudes of the points along the great circle, in radians.
     * @throws SLBMException if native method fails
     */
    public double[][] getGreatCirclePoints(double aLat,	double aLon,
            double bLat, double bLon, int npoints)
    throws SLBMException
    {
        double[][] latlon = new double[2][0];
        computeGreatCirclePointsNative(aLat, aLon, bLat, bLon, npoints, false);
        latlon[0] = getGreatCirclePointsLatNative();
        latlon[1] = getGreatCirclePointsLonNative();
        deleteGreatCirclePointsNative();
        return latlon;
    }

    /**
     * Retrieve an array of lat, lon points along a great circle path
     * between two specified points. The great circle path between a and b is
     * divided into npoints equal size cells and the computed points are
     * located at the centers of those cells.
     * @param aLat double the latitude of the first point in radians.
     * @param aLon double the longitude of the first point in radians.
     * @param bLat double the latitude of the second point in radians.
     * @param bLon double the longitude of the second point in radians.
     * @param npoints int the number of points desired.
     * @return double[][] a 2 x npoints array containing the latitudes and
     * longitudes of the points along the great circle, in radians.
     * @throws SLBMException if native method fails
     */
    public double[][] getGreatCirclePointsOnCenters(double aLat, double aLon,
            double bLat, double bLon, int npoints)
    throws SLBMException
    {
        double[][] latlon = new double[2][0];
        computeGreatCirclePointsNative(aLat, aLon, bLat, bLon, npoints, true);
        latlon[0] = getGreatCirclePointsLatNative();
        latlon[1] = getGreatCirclePointsLonNative();
        deleteGreatCirclePointsNative();
        return latlon;
    }
    private native void computeGreatCirclePointsNative(double aLat,
            double aLon, double bLat, double bLon, int npoints, boolean onCenters);
    private native void deleteGreatCirclePointsNative();
    private native double[] getGreatCirclePointsLatNative();
    private native double[] getGreatCirclePointsLonNative();
    private native double[] getGreatCirclePointsDepthNative();

    /**
     * Calculate an uncertainty value (seconds) resulting from last call to
     * createGreatCircle()
     * @return the uncertainty in seconds
     * @throws SLBMException if native method fails
     */
    public double getTravelTimeUncertainty()
    throws SLBMException
    {
//      return getTravelTimeUncertainty(getPhaseNative(),
//              getDistanceNative());
        return getTravelTimeUncertaintyNative(false);
    }
    private native double getTravelTimeUncertaintyNative(boolean calcRandomError);

    /**
     * Calculate an uncertainty value (seconds) resulting from last call to
     * createGreatCircle(). Uses Random Error in the computation.
     * @param calcRandomError Boolean describing whether or not to calculate random error (default: false)
     * @return the uncertainty in seconds
     * @throws SLBMException if native method fails
     */
    public double getTravelTimeUncertainty(boolean calcRandomError)
    throws SLBMException
    {
        return getTravelTimeUncertaintyNative(calcRandomError);
    }

    /**
     * Calculate an uncertainty value (seconds) resulting from last call to
     * createGreatCircle(). Computes the 1D-model uncertainties.
     * @return the uncertainty in seconds
     * @throws SLBMException if native method fails
     */
    public double getTravelTimeUncertainty1D()
    throws SLBMException
    {
        return getTravelTimeUncertainty1DNative();
    }

    /**
     * Calculate an uncertainty value (seconds) as a function of distance
     * in radians for a supported seismic phase (Pn, Sn, Pg, Lg).
     * @param phase String one of Pn, Sn, Pg, Lg
     * @param distance the angular distance in radians
     * @return the uncertainty in seconds
     * @throws SLBMException if native method fails
     */
    public double getTravelTimeUncertainty(String phase, double distance)
    throws SLBMException
    {
        return getTravelTimeUncertaintyPhaseDistanceNative(
                (phase.equals("Pn") ? 0 : phase.equals("Sn") ? 1 :
                    phase.equals("Pg") ? 2 : phase.equals("Lg") ? 3 : -1),
                    distance);
    }
    private native double getTravelTimeUncertaintyPhaseDistanceNative( int phase, double distance);
    private native double getTravelTimeUncertaintyNative();
    private native double getTravelTimeUncertainty1DNative();

    /**
     * Retrieve some of the parameters that contribute to the calculation of
     * of total travel time using the Zhao algorithm.  This method only returns
     * meaningful results for phases Pn and Sn.  For Pg and Lg, all the parameters
     * of type double are returned with values BaseObject::NA_VALUE and udSign is
     * returned with value of -999.
     * @return ZhaoParameters
     * @throws SLBMException if native method fails
     */
    public ZhaoParameters getZhaoParameters()
    throws SLBMException
    {
        ZhaoParameters zpar = new ZhaoParameters();
        double[] zhao = getZhaoParametersNative();
        zpar.Vm = zhao[0];
        zpar.Gm = zhao[1];
        zpar.H = zhao[2];
        zpar.C = zhao[3];
        zpar.Cm = zhao[4];
        zpar.udSign = getZhaoUdSignNative();
        return zpar;
    }
    private native double[] getZhaoParametersNative();
    private native int getZhaoUdSignNative();

    /**
     * Retrieve information about Pg/Lg travel time calculations.  This method
     * only returns useful information when the phase is Pg or Lg.  For Pn and
     * Sn, all information is returned as SLBMGlobals::NA_VALUE.
     * <p>This method returns a 7 element array of doubles.  The values have
     * the following interpretation:
     * <p>0: total travel time, sec
     * <br>1: TauP travel time, sec
     * <br>2: Headwave travel time, sec
     * <br>3: TauP ray parameter, seconds/radian
     * <br>4: Headwave ray parameter, seconds/km
     * <br>5: TauP Turning radius, km
     * <br>6: Headwave turning radius, km
     * @return double[]<br>
     * @throws SLBMException if native method fails
     */
    public double[] getPgLgComponents()
    throws SLBMException
    {
        return getPgLgComponentsNative();
    }
    private native double[] getPgLgComponentsNative();

    /**
     * Retrieve the node IDs of the nodes that surround the specified node.
     * @param nodeId int
     * @return int[]
     */
    public int[] getNodeNeighbors(int nodeId)
    {
        return getNodeNeighborsNative(nodeId);
    }
    private native int[] getNodeNeighborsNative(int nodeId);

    /**
     * Retrieve the active node IDs of the nodes that surround the specified active node.
     * @param nodeId int
     * @return int[]
     */
    public int[] getActiveNodeNeighbors(int nodeId)
    {
        return getActiveNodeNeighborsNative(nodeId);
    }
    private native int[] getActiveNodeNeighborsNative(int nodeId);

    /**
     * Retrieve the angular separation of two grid nodes, in radians.
     * @param node1 int
     * @param node2 int
     * @return double
     */
    public double getNodeSeparation(int node1, int node2)
    {
        return getNodeSeparationNative(node1, node2);
    }
    private native double getNodeSeparationNative(int node1, int node2);

    /**
     * Retrieve the azimuth from grid node1 to grid node2, radians.
     * @param node1 int
     * @param node2 int
     * @return double
     */
    public double getNodeAzimuth(int node1, int node2)
    {
        return getNodeAzimuthNative(node1, node2);
    }
    private native double getNodeAzimuthNative(int node1, int node2);

    /**
     * Initialize the list of active nodes in the model grid.  After a call
     * to this method, active nodes can be accessed by looping over the active
     * nodes.  See getNActiveNodes() and getActiveNodeData().
     *
     * <p>Active nodes includes all grid nodes that fall within the specified
     * range of latitude and longitude.  Lats and lons must be specified
     * in radians.  Lonmin and lonmax should -PI &lt;= lon &lt;= PI.
     * @param latmin double
     * @param lonmin double
     * @param latmax double
     * @param lonmax double
     */
    public void initializeActiveNodes(double latmin, double lonmin,
            double latmax, double lonmax)
    {
        initializeActiveNodesNative(latmin, lonmin, latmax, lonmax);
    }
    private native void initializeActiveNodesNative(double latmin, double lonmin,
            double latmax, double lonmax);

    /**
     * Retrieve the number of times that the specified node has been 'touched'
     * by a GreatCircle object.  The hit count of each node is initialized in the
     * loadVelocityModel() method.  Every time the getWeights() method is called
     * for a particular GreatCircle object, all the nodeIds that contribute any
     * weight to that GreatCircle object have their hit count incremented by one.
     * @param nodeId int
     * @return int
     */
    public int getNodeHitCount(int nodeId)
    {
        return getNodeHitCountNative(nodeId);
    }
    private native int getNodeHitCountNative(int nodeId);

    /**
     * Retrieve the number of active nodes in the Grid.
     *
     * @return int
     */
    public int getNActiveNodes()
    {
        return getNActiveNodesNative();
    }
    private native int getNActiveNodesNative();


    /**
     * Retrieve the grid node ID that corresponds to a specified active node ID.
     *
     * @param activeNodeId int
     * @return int
     */
    public int getGridNodeId(int activeNodeId)
    {
        return getGridNodeIdNative(activeNodeId);
    }
    private native int getGridNodeIdNative(int activeNodeId);

    /**
     * Retrieve the active node ID that corresponds to a specified grid node ID.
     *
     * @param gridNodeId int
     * @return int
     */
    public int getActiveNodeId(int gridNodeId)
    {
        return getActiveNodeIdNative(gridNodeId);
    }
    private native int getActiveNodeIdNative(int gridNodeId);

    /**
     * Retrieve the weight assigned to each active node that
     * was touched by the GreatCircle that is currently in memory.
     * <p>A map which associates an instance of a
     * GridProfile object with a double <i>weight</i> is initialized.
     * Then every LayerProfile on the head wave interface between the source
     * and receiver is visited and the angular distance, <i>d</i>, that the ray
     * traveled in the horizontal segment is retreived.  If <i>d</i> &gt; 0,
     * then the neighboring GridProfile objects that contributed to
     * the interpolated value of the LayerProfile are visited.
     * The product of <i>d * R * C</i>  is added to the weight associated
     * with that GridProfile object, where <i>R</i> is the radius of the
     * head wave interface for the LayerProfile object being evaluated,
     * and <i>C</i> is the interpolation coefficient for the
     * GridProfile - LayerProfile pair under consideration.
     * Then, all the GridProfile objects in the map are visited, the
     * grid node IDs extracted into int array <i>node</i>, and the
     * <i>weight</i> extracted into double array <i>weight</i>.
     *
     * <p>Note: Only active nodes touched by this GreatCircle are included in the
     * output.  Each active node is included only once, even though more than
     * one LayerProfile object may have contributed some weight to it.
     * The sum of all the weights will equal the horizontal distance
     * traveled by the ray along the head wave interface, from the source
     * pierce point to the receiver pierce point, in km.
     * @return GridWeight a storage container for the results.
     * @throws SLBMException if native method fails
     */
    public GridWeight getActiveNodeWeights()
    throws SLBMException
    {
        GridWeight weights = new GridWeight();
        computeWeightsNative();
        weights.node = getWeightNodesNative();
        for (int i = 0; i < weights.node.length; ++i)
            weights.node[i] = getActiveNodeId(weights.node[i]);
        weights.weight = getWeightsNative();
        deleteWeightsNative();

        return weights;
    }

    /**
     * Retrieve the lat (radians), lon (radians), interface depths (km), P and S
     * wave interval velocities (km/sec) and P and S mantle gradient (1/sec)
     * information associated with a specified active node in the velocity grid.
     * Retrieve the interface depth, velocity and gradient information associated
     * with a specified active node in the velocity grid.
     *
     * @param nodeId the active node ID of the grid point in the model (zero
     *   based index).
     * @throws SLBMException if native method fails
     * @return GridProfile
     */
    public GridProfile getActiveNodeData(int nodeId)
    throws SLBMException
    {
        GridProfile gp = new GridProfile();
        int gridnode = getGridNodeId(nodeId);
        accessGridProfileNative(gridnode);
        gp.nodeId = nodeId;
        gp.lat = getGridLatNative();
        gp.lon = getGridLonNative();
        gp.depth = getGridDepthNative();
        gp.velocity[0] = getGridVelocityNative(0);
        gp.velocity[1] = getGridVelocityNative(1);
        gp.gradient = getGridGradientNative();
        return gp;
    }

    /**
     * Retrieve the number of Grid nodes in the Earth model.
     *
     * @return int
     */
    public int getNGridNodes()
    {
        return getNGridNodesNative();
    }
    private native int getNGridNodesNative();

    /**
     * Retrieve the number of Head Wave Points.
     *
     * @return int
     */
    public int getNHeadWavePoints()
    {
        return getNHeadWavePointsNative();
    }
    private native int getNHeadWavePointsNative();


    /**
     * Retrieve NeighborInfo
     *
     * @param nid int
     * @return QueryNeighborInfo
     */
    public QueryNeighborInfo getNodeNeighborInfo( int nid )
    {
        QueryNeighborInfo neighborinfo = new QueryNeighborInfo();
        neighborinfo.nid = nid;
        neighborinfo.neighbors = getNodeNeighborsNative(nid);
        neighborinfo.nNeighbors = neighborinfo.neighbors.length;
        double tempDist[] = new double[neighborinfo.neighbors.length];
        double tempAz[] = new double[neighborinfo.neighbors.length];

        for (int i = 0; i < neighborinfo.neighbors.length; i++)
        {
            tempDist[i] = getNodeSeparation(nid, neighborinfo.neighbors[i]);
            tempAz[i] = getNodeAzimuth(nid, neighborinfo.neighbors[i]);
        }

        neighborinfo.distance = tempDist;
        neighborinfo.azimuth = tempAz;

        return neighborinfo;
    }

    /**
     * Retrieve NeighborInfo
     *
     * @param actnid int
     * @return QueryNeighborInfo
     */
    public QueryNeighborInfo getActiveNodeNeighborInfo(int actnid)
    {
        QueryNeighborInfo neighborinfo = new QueryNeighborInfo();
        neighborinfo.nid = actnid;
        neighborinfo.neighbors = getActiveNodeNeighborsNative(actnid);
        neighborinfo.nNeighbors = neighborinfo.neighbors.length;
        double tempDist[] = new double[neighborinfo.neighbors.length];
        double tempAz[] = new double[neighborinfo.neighbors.length];

        for (int i = 0; i < neighborinfo.neighbors.length; i++)
        {
            tempDist[i] = getNodeSeparation(getGridNodeId( actnid ), getGridNodeId( neighborinfo.neighbors[i] ));
            tempAz[i] = getNodeAzimuth(getGridNodeId(actnid), getGridNodeId( neighborinfo.neighbors[i] ) );
        }

        neighborinfo.distance = tempDist;
        neighborinfo.azimuth = tempAz;

        return neighborinfo;
    }

    /**
     * Set the value of chMax. c is the zhao c parameter and h is the turning depth
     * of the ray below the moho. Zhao method only valid for c*h &lt;&lt; 1. When c*h &gt;
     * chMax, then slbm will throw an exception. This call modifies global
     * parameter BaseObject::ch_max
     *
     * @param chMax double
     */
    public void setCHMax(double chMax)
    {
        setCHMaxNative(chMax);
    }
    private native void setCHMaxNative( double chMax );


    /**
     * Retrieve the current value of chMax. c is the zhao c parameter and h is the
     * turning depth of the ray below the moho. Zhao method only valid for c*h &lt;&lt;
     * 1. When c*h &gt; chMax, then slbm will throw an exception. This call retrieves
     * global parameter BaseObject::ch_max
     *
     * @return double
     */
    public double getCHMax()
    {
        return getCHMaxNative();
    }
    private native double getCHMaxNative();


    /**
     * Retrieve the average P or S wave mantle velocity that is specified in the
     * model input file. This value is used in the calculation of the Zhao c
     * parameter.
     *
     * @param type specify either BaseObject::PWAVE or BaseObject::SWAVE.
     * @return double
     */
    public double getAverageMantleVelocity( int type )
    {
        return getAverageMantleVelocityNative( type );
    }
    private native double getAverageMantleVelocityNative( int type );

    /** Set the average P or S wave mantle velocity that is specified
     * in the model input file.  This value is used in the calculation of
     * the Zhao c parameter.
     * @param type specify either BaseObject::PWAVE or BaseObject::SWAVE.
     * @param velocity the P or S wave velocity that is to be set,
     * in km/sec.  This value will be stored in the model file, if the
     * model file is written to file by a call to saveVelocityModel()
     * subsequent to a call to this method.
     */
    public void setAverageMantleVelocity( int type, double velocity)
    {
        setAverageMantleVelocityNative( type, velocity );
    }
    private native void setAverageMantleVelocityNative( int type, double velocity );

    /**
     * Retrieve the tessellation ID of the model currently in memory.
     *
     * @return String
     */
    public String getTessId()
    {
        return getTessIdNative();
    }
    private native String getTessIdNative();

    /**
     * Retrieve the fraction of the path length of the current GreatCircle object
     * that is within the currently defined active region.
     *
     * @return double
     */
    public double getFractionActive()
    {
        return getFractionActiveNative();
    }
    private native double getFractionActiveNative();

    /**
     * Set the maximum source-receiver separation for Pn/Sn phase, in radians.
     * Source-receiver separations greater than the specified value will result in
     * an exception being thrown in createGreatCircle(). Default value is PI
     * radians.
     *
     * @param maxDistance double
     */
    public void setMaxDistance( double maxDistance)
    {
        setMaxDistanceNative( maxDistance );
    }
    private native void setMaxDistanceNative(double maxDistance);

    /**
     * Retrieve the current value for the maximum source-receiver separation, in
     * radians.
     *
     * @return double
     */
    public double getMaxDistance()
    {
        return getMaxDistanceNative();
    }
    private native double getMaxDistanceNative();

    /**
     * Set the maximum source depth for Pn/Sn phase, in km. Source depths greater
     * than the specified value will result in an exception being thrown in
     * createGreatCircle(). Default value is 9999 km.
     *
     * @param maxDepth double
     */
    public void setMaxDepth(double maxDepth)
    {
        setMaxDepthNative(maxDepth);
    }
    private native void setMaxDepthNative(double maxDepth);

    /**
     * Retrieve the current value for the maximum source depth, in km.
     *
     * @return double
     */
    public double getMaxDepth()
    {
        return getMaxDepthNative();
    }
    private native double getMaxDepthNative();

    /**
     * Retrieve the derivative of travel time wrt to source latitude,
     * in seconds/radian.
     * @return double the derivative of travel time wrt to source latitude,
     * in seconds/radian.
     * @throws SLBMException if native method fails
     */
    public double get_dtt_dlat() throws SLBMException  { return getDttDlatNative(); }
    private native double getDttDlatNative();

    /**
     * Retrieve the derivative of travel time wrt to source longitude,
     * in seconds/radian.
     * @return double the derivative of travel time wrt to source longitude,
     * in seconds/radian.
     * @throws SLBMException if native method fails
     */
    public double get_dtt_dlon() throws SLBMException  { return getDttDlonNative(); }
    private native double getDttDlonNative();

    /**
     * Retrieve the derivative of travel time wrt to source depth,
     * in seconds/km.
     * @return double the derivative of travel time wrt to source depth,
     * in seconds/km.
     * @throws SLBMException if native method fails
     */
    public double get_dtt_ddepth() throws SLBMException  { return getDttDdepthNative(); }
    private native double getDttDdepthNative();

//	/**
//	 * Retrieve the derivative of travel time wrt to source latitude,
//	 * in seconds/radian.
//	 * @return double the derivative of travel time wrt to source latitude,
//	 * in seconds/radian.
//	 * @throws SLBMException if native method fails
//	 */
//	public double get_dtt_dlat_fast() throws SLBMException  { return getDttDlatFastNative(); }
//	private native double getDttDlatFastNative();
//
//	/**
//	 * Retrieve the derivative of travel time wrt to source longitude,
//	 * in seconds/radian.
//	 * @return double the derivative of travel time wrt to source longitude,
//	 * in seconds/radian.
//	 * @throws SLBMException if native method fails
//	 */
//	public double get_dtt_dlon_fast() throws SLBMException  { return getDttDlonFastNative(); }
//	private native double getDttDlonFastNative();
//
//	/**
//	 * Retrieve the derivative of horizontal slowness wrt to source latitude,
//	 * in seconds/radian^2.
//	 * @return double the derivative of horizontal slowness wrt to source latitude,
//	 * in seconds/radian^2.
//	 * @throws SLBMException if native method fails
//	 */
//	public double get_dsh_ddist() throws SLBMException  { return getDshDdistNative(); }
//	private native double getDshDdistNative();
//
//	/**
//	 * Retrieve the derivative of horizontal slowness wrt to source latitude,
//	 * in seconds/radian^2.
//	 * @return double the derivative of horizontal slowness wrt to source latitude,
//	 * in seconds/radian^2.
//	 * @throws SLBMException if native method fails
//	 */
//	public double get_dsh_dlat() throws SLBMException  { return getDshDlatNative(); }
//	private native double getDshDlatNative();
//
//	/**
//	 * Retrieve the derivative of horizontal slowness wrt to source longitude,
//	 * in seconds/radian^2.
//	 * @return double the derivative of horizontal slowness wrt to source longitude,
//	 * in seconds/radian^2.
//	 * @throws SLBMException if native method fails
//	 */
//	public double get_dsh_dlon() throws SLBMException  { return getDshDlonNative(); }
//	private native double getDshDlonNative();
//
//	/**
//	 * Retrieve the derivative of horizontal slowness wrt to source depth,
//	 * in seconds/radian-km.
//	 * @return double the derivative of horizontal slowness wrt to source depth,
//	 * in seconds/radian-km.
//	 * @throws SLBMException if native method fails
//	 */
//	public double get_dsh_ddepth() throws SLBMException  { return getDshDdepthNative(); }
//	private native double getDshDdepthNative();

    /**
     * Retrieve horizontal slowness, in seconds/radian.
     * @return double the horizontal slowness, in seconds/radian.
     * @throws SLBMException if native method fails
     */
    public double getSlowness() throws SLBMException  { return getSlownessNative(); }
    private native double getSlownessNative();

    /**
     * Get the slowness uncertainty (seconds/radian) resulting from last call to
     * createGreatCircle()
     * @return the uncertainty in seconds/radian
     * @throws SLBMException if native method fails
     */
    public double getSlownessUncertainty()
    throws SLBMException
    {
        return getSlownessUncertainty(getPhaseNative(),
                getDistanceNative());
    }

    /**
     * Retrieve uncertainty of horizontal slowness, in seconds/radian-km.
     *
     * @return double the uncertainty of horizontal slowness, in
     *   seconds/radian-km.
     * @param phase String
     * @param distance double
     * @throws SLBMException if native method fails
     */
    public double getSlownessUncertainty(String phase, double distance)
    throws SLBMException
    {
        return getSlownessUncertaintyNative(
                (phase.equals("Pn") ? 0 : phase.equals("Sn") ? 1 :
                    phase.equals("Pg") ? 2 : phase.equals("Lg") ? 3 : -1),
                    distance);
    }
    private native double getSlownessUncertaintyNative(int phase,
            double distance);


    /**
     * Find geographic latitude and longitude of the point that is the specified
     * distance and azimuth from point A.
     * @param latA radians
     * @param lonA radians
     * @param distance radians
     * @param azimuth radians
     * @return [latB, lonB] in radians
     */
    public double[] movePoint(double latA, double lonA, double distance, double azimuth)
    {
        return movePointNative(latA, lonA, distance, azimuth);
    }
    private native double[] movePointNative(double latA, double lonA, double distance, double azimuth);

    /**
     * Retrieve distance and azimuth between two points, A and B
     * (all quantities are in radians). Computed distance will range
     * between 0 and PI and azimuth will range from -PI to PI.
     * If distance is zero, or if A is located at north or south
     * pole, azimuth will be set to naValue.
     * @param latA geographic latitude of point A, in radians.
     * @param lonA geographic longitude of point A, in radians.
     * @param latB geographic latitude of point B, in radians.
     * @param lonB geographic longitude of point B, in radians.
     * @param naValue if distance is zero or if A is coincident with north
     * or south pole, then azimuth is invalid and this value will be returned as the azimuth.
     * @return [distance, azimuth] in radians.
     */
    public double[] getDistAz(double latA, double lonA, double latB, double lonB,
            double naValue)
    {
        return getDistAzNative(latA, lonA, latB, lonB, naValue);
    }
    private native double[] getDistAzNative(double latA, double lonA, double latB, double lonB,
            double naValue);

    /**
     * Retrieve the geographic latitude, longitude and depth of the moho pierce point below the receiver,
     * in radians, km.  For Pg, Lg  an exception is thrown.
     * @return pierce point at the source
     */
    public double[] getPiercePointSource()
    {
        return getPiercePointSourceNative();
    }
    private native double[] getPiercePointSourceNative();

    /**
     * Retrieve the geographic latitude, longitude and depth of the moho pierce point below the receiver,
     * in radians, km.  For Pg, Lg  an exception is thrown.
     * @return the geographic latitude, longitude and depth of the moho pierce point below the receiver,
     * in radians, km
     */
    public double[] getPiercePointReceiver()
    {
        return getPiercePointReceiverNative();
    }
    private native double[] getPiercePointReceiverNative();

    /**
     * Retrieve  the horizontal separation of two
     * points used to compute horizontal slowness and derivatives of travel time
     * with respect to latitude and longitude (in radians).
     *
     * @return double del_distance, in radians
     * @throws SLBMException if native method fails
     */
    public double getDelDistance()
    throws SLBMException
    {
        return getDelDistanceNative();
    }
    private native double getDelDistanceNative();

    /**
     * Modify the horizontal separation of two
     * points used to compute horizontal slowness and derivatives of travel time
     * with respect to latitude and longitude.
     *
     * @param del_distance in radians
     */
    public void setDelDistance(double del_distance)
    {
        setDelDistanceNative(del_distance);
    }
    private native void setDelDistanceNative( double del_distance );

    /**
     * Retrieve del_depth, the vertical separation of two points used to compute
     * derivative of travel time with respect to depth (in km).
     *
     * @return del_depth, in km.
     * @throws SLBMException if native method fails
     */
    public double getDelDepth()
    throws SLBMException
    {
        return getDelDepthNative();
    }
    private native double getDelDepthNative();

    /**
     * Modify the value of del_depth, the vertical separation of two points used to compute
     * derivative of travel time with respect to depth (in km).
     *
     * @param del_depth in km.
     */
    public void setDelDepth(double del_depth)
    {
        setDelDepthNative(del_depth);
    }
    private native void setDelDepthNative( double del_depth );

    /**
     * Retrieve the ray parameter in sec/km.
     *
     * @return rayParameter in sec/km
     * @throws SLBMException if native method fails
     */
    public double getRayParameter()
    throws SLBMException
    {
        return getRayParameterNative();
    }
    private native double getRayParameterNative();

    /**
     * Retrieve turning radius in km
     *
     * @return turningRadius in km.
     * @throws SLBMException if native method fails
     */
    public double getTurningRadius()
    throws SLBMException
    {
        return getTurningRadiusNative();
    }
    private native double getTurningRadiusNative();

    /**
     * Set the desired spacing of great circle nodes
     * along the head wave interface, in radians.
     * The actual spacing will be
     * reduced from the requested value in order that an integral
     * number of equally spaced LayerProfile objects will exactly
     * span the source-receiver separation.  Defaults to
     * 0.1 degrees if not specified.
     *
     * @param pathIncrement the desired spacing of great circle nodes
     * along the head wave interface, in radians.
     */
    public void setPathIncrement(double pathIncrement)
    {
        setPathIncrementNative(pathIncrement);
    }
    private native void setPathIncrementNative(double pathIncrement);

    /**
     * Retrieve the current value of the spacing of great circle nodes
     * along the head wave interface, in radians.
     * The actual spacing will be
     * reduced from the requested value in order that an integral
     * number of equally spaced LayerProfile objects will exactly
     * span the source-receiver separation.  The default value is 0.1 degrees.
     *
     * @return the current value of the spacing of great circle nodes
     * along the head wave interface, in radians.
     */
    public double getPathIncrement()
    {
        return getPathIncrementNative();
    }
    private native double getPathIncrementNative();

    /**
     * Set the interpolatorType to either 'linear' or 'natural_neighbor'.
     * The default value is 'natural_neighbor';
     *
     * @param interpolatorType either 'linear' or 'natural_neighbor'.
     */
    public void setInterpolatorType(String interpolatorType)
    {
        setInterpolatorTypeNative(interpolatorType);
    }
    private native void setInterpolatorTypeNative(String interpolatorType);

    /**
     * Retrieve the current interpolatorType, either 'linear' or 'natural_neighbor'.
     * The default value is 'natural_neighbor';
     *
     * @return interpolatorType either 'linear' or 'natural_neighbor'.
     */
    public String getInterpolatorType()
    {
        return getInterpolatorTypeNative();
    }
    private native String getInterpolatorTypeNative();

    /**
     * Reset the hit count of every grid node to zero.
     */
    public void clearHitCount()
    {
        clearHitCountNative();
    }
    private native void clearHitCountNative();

    /**
     * Check if two models are equal
     * @param modelPath1 String
     * @param modelPath2 String
     * @return true or false, whether the models are equal, or not
     * @throws SLBMException if native method fails
     */
    public static boolean modelsEqual (String modelPath1, String modelPath2)
    throws SLBMException
    {
        return modelsEqualNative(modelPath1, modelPath2);
    }
    private static native boolean modelsEqualNative (String modelPath1, String modelPath2);

}  // END SlbmInterface
