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
package gov.sandia.gmp.baseobjects.hyperellipse;

import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.DEPTH;
import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.LAT;
import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.LON;
import static gov.sandia.gmp.util.globals.Globals.TWO_PI;
import static gov.sandia.gmp.util.globals.Globals.PI_OVR_TWO;
import static gov.sandia.gmp.util.globals.Globals.NA_VALUE;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotessbuilder.GeoTessBuilderMain;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.util.numerical.simplex.Simplex;
import gov.sandia.gmp.util.numerical.simplex.SimplexFunction;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.testingbuffer.Buff;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

public class Ellipsoid implements SimplexFunction, Serializable {

    private static final long serialVersionUID = 5712937458441860265L;

    private HyperEllipse hyperEllipse;
    private double[] coeff;
    private double[][] principal_axes;

    private int maximize;


    Ellipsoid(HyperEllipse hyperEllipse) throws Exception {
	this.hyperEllipse = hyperEllipse;
	this.coeff = this.hyperEllipse.uncertainty_equation_coefficients(
		new int[] {LAT, LON, DEPTH});
	if (isValid())
	    find_principal_axes();
    }

    public boolean isValid() { return coeff != null; }

    /**
     * Retrieve the trend of the major axis of the ellipsoid
     * in degrees. This is the angle in the horizontal plane 
     * measured east of due north.  
     * Range is 0 to 360. 
     * A value of -1 implies invalid uncertainty information.
     * @return the trend of the major axis of the ellipsoid
     * in degrees.  -999999. implies invalid uncertainty information.
     * @throws Exception
     */
    public double getMajaxTrend() throws Exception {
	return isValid() ? principal_axes[0][0] : NA_VALUE;
    }

    /**
     * Retrieve the plunge of the major axis of the ellipsoid
     * in degrees.  Plunge is the angle between the axis and 
     * the horizontal plane; positive is down from the horizontal.
     * Range is -90 to 90.
     * @return the plunge of the major axis of the ellipsoid
     * in degrees; positive is down.
     * -999999. implies invalid uncertainty information.
     * @throws Exception
     */
    public double getMajaxPlunge() throws Exception {
	return isValid() ? principal_axes[0][1] : NA_VALUE;
    }

    /**
     * Retrieve the length of the major axis of the ellipsoid
     * in km.
     * @return the length of the major axis of the ellipsoid
     * in km.
     * -999999. implies invalid uncertainty information.
     * @throws Exception
     */
    public double getMajaxLength() throws Exception {
	return isValid() ? principal_axes[0][2] * hyperEllipse.getKappa(3) : NA_VALUE;
    }

    /**
     * Retrieve the trend of the intermediate axis of the ellipsoid
     * in degrees. This is the angle in the horizontal plane 
     * measured east of due north.
     * Range is 0 to 360.
     * @return the trend of the intermediate axis of the ellipsoid
     * in degrees
     * -999999. implies invalid uncertainty information.
     * @throws Exception
     */
    public double getIntaxTrend() throws Exception {		
	return isValid() ? principal_axes[1][0] : NA_VALUE;
    }

    /**
     * Retrieve the plunge of the intermediate axis of the ellipsoid
     * in degrees.  Plunge is the angle between the axis and 
     * the horizontal plane; positive is down from the horizontal.
     * Range is -PI/2 to 90.
     * @return the plunge of the intermediate axis of the ellipsoid
     * in degrees; positive is down.
     * -999999. implies invalid uncertainty information.
     * @throws Exception
     */
    public double getIntaxPlunge() throws Exception {		
	return isValid() ? principal_axes[1][1] : NA_VALUE;
    }

    /**
     * Retrieve the length of the intermediate axis of the ellipsoid
     * in km.
     * @return the length of the intermediate axis of the ellipsoid
     * in km.
     * -999999. implies invalid uncertainty information.
     * @throws Exception
     */
    public double getIntaxLength() throws Exception {		
	return isValid() ? principal_axes[1][2] * hyperEllipse.getKappa(3) : NA_VALUE;
    }

    /**
     * Retrieve the trend of the minor axis of the ellipsoid
     * in degrees. This is the angle in the horizontal plane 
     * measured east of due north.
     * Range is 0 to 360.
     * @return the trend of the minor axis of the ellipsoid
     * in degrees
     * -999999. implies invalid uncertainty information.
     * @throws Exception
     */
    public double getMinaxTrend() throws Exception {		
	return isValid() ? principal_axes[2][0] : NA_VALUE;
    }

    /**
     * Retrieve the plunge of the minor axis of the ellipsoid
     * in degrees.  Plunge is the angle between the axis and 
     * the horizontal plane; positive is down from the horizontal.
     * Range is -90 to 90.
     * @return the plunge of the minor axis of the ellipsoid
     * in degrees; positive is down.
     * -999999. implies invalid uncertainty information.
     * @throws Exception
     */
    public double getMinaxPlunge() throws Exception {		
	return isValid() ? principal_axes[2][1] : NA_VALUE;
    }

    /**
     * Retrieve the length of the minor axis of the ellipsoid
     * in km.
     * @return the length of the minor axis of the ellipsoid
     * in km.
     * -999999. implies invalid uncertainty information.
     * @throws Exception
     */
    public double getMinaxLength() throws Exception {		
	return isValid() ? principal_axes[2][2] * hyperEllipse.getKappa(3) : NA_VALUE;
    }

    /**
     * Use the simplex algorithm to find the major, intermediate and
     * minor axes of the uncertainty ellipoid.  
     * @throws Exception
     */
    private void find_principal_axes() throws Exception
    {
	principal_axes = new double[3][];

	// set maximize negative to find the major principal axis
	maximize = -1;

	// find trend, plunge and length of major axis
	principal_axes[0] = find_extreme();

	if (principal_axes[0].length == 0)
	{
	    coeff = null;
	    principal_axes = null;
	    return;
	}

	// set maximize positive to find the minor axis.
	maximize = 1;

	// find trend, plunge and length of minor axis
	principal_axes[2] = find_extreme();

	if (principal_axes[2].length == 0)
	{
	    coeff = null;
	    principal_axes = null;
	    return;
	}

	// the intermediate axis is found by taking the cross product of unit
	// vectors in the directions of the major and minor axes.
	double[] u = new double[3];
	double[] v = new double[3];
	double[] w = new double[3];

	// convert major and minor axes from trend/plunge to unit vectors.
	unit_vector(principal_axes[0], u);
	unit_vector(principal_axes[2], w);

	VectorUnit.cross(u, w, v);

	// at this point, v is a unit vector that points in direction of intermediate axis.

	// check that the length of the intermediate axis is 1. This indicates
	// that the major and minor axes are in fact perpendicular to each other.
	double len = sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);

	if (abs(len - 1.) > 1e-7)
	    throw new Exception(
		    ("\nMajor and minor axes are not mutually perpendicular.\nLength of cross product = " 
			    + len + "\n"));

	principal_axes[1] = new double[3];

	// convert v to trend and plunge
	trendPlunge(v, principal_axes[1]);

	// find the distance to the ellipsoid perimeter in the direction of the
	// intermediate axis.
	principal_axes[1][2] = distance_to_perimeter(v);

	// ensure that the trend is between 0 and 2PI
	principal_axes[1][0] = (principal_axes[1][0] + TWO_PI) % TWO_PI;
	
	// convert all trend and plunge values to degrees
	for (int i=0; i<3; ++i) 
	    for (int j=0; j<2; ++j) 
		principal_axes[i][j] = Math.toDegrees(principal_axes[i][j]);
    }

    /**
     * find major or minor axis of the ellipsoid, 
     * depending on setting of maximize private member.
     * All angle are in radians.
     * 
     * @return trend, plunge and length of major or minor axis.
     * @throws Exception
     */
    private double[] find_extreme() throws Exception
    {
	Simplex simplex = new Simplex(this, 1e-8, 1000);

	// declare variables needed by amoeba.
	// p is 3 initial estimates of trend and plunge, in radians.
	double[][] p = new double[][] { {0., 0.7}, {0., 0.8}, {0.1, 0.75} };

	// y will initially hold the three distances from the center of the ellipsoid
	// to its perimeter.  When searching for the major axis of the ellipsoid,
	// the y values will be negative because Simplex does minimization, not 
	// maximization.
	double[] y = new double[3]; 

	// find the trend and plunge (p) and the length (y) of the principal axis in question
	y = simplex.search(p);

	// at this point, p[0] holds the trend and plunge of the principal axis
	// and y[0] holds the length of the axis.  Move trend, plunge and length
	// into y.
	y[2] = abs(y[0]);  // length
	y[0] = p[0][0];  // trend
	y[1] = p[0][1];  // plunge

	// ensure that the plunge is in range -PI/2 to PI/2.
	while (y[1] < -PI_OVR_TWO)
	    y[1] += PI;
	while (y[1] > PI_OVR_TWO)
	    y[1] -= PI;

	// ensure that the plunge is positive (down from horizontal)
	if (y[1] < 0.)
	{
	    y[1] = -y[1];
	    y[0] += PI;
	}

	// ensure that the trend is in range 0 to 2*PI.
	y[0] = (y[0] + TWO_PI) % TWO_PI;

	return y;
    }

    /**
     * Find the distance from the center of the ellipsoid to the perimeter of the
     * ellipsoid when moving in the specified direction.
     * 
     * @param v unit vector in local, lat-lon-depth coordinate system, that 
     * specifies the direction in which to move. 
     */
    private double distance_to_perimeter(double[] v)
    {
	// calculate distance from center of ellipsoid to perimeter of
	// ellipsoid, in the direction specified by vector x.
	// v is a 3-component unit vector specifying direction in which to
	// look.
	return sqrt( 1. / (coeff[0] * v[0] * v[0] + coeff[1] * v[0] * v[1] + coeff[2]
		* v[0] * v[2] + coeff[3] * v[1] * v[1] + coeff[4]
			* v[1] * v[2] + coeff[5] * v[2] * v[2]));
    }

    /**
     * Find the distance from the center of the ellipsoid to the perimeter of the
     * ellipsoid when moving in the specified direction.
     * <p>Set global parameter 'maximize' to -1 to search for the major axis of the 
     * ellipsoid and set it to 1 to find the minor axis of the ellipsoid.
     * 
     * @param tp trend and plunge in radians.  Trend is angle east of north measured
     * in horizontal plane.  Plunge is angle down from the horizontal.
     * @return distance to the perimeter of the ellipsoid, multiplied by 'maximize'.
     */
    public double simplexFunction(double[] tp)
    {
	// tp is a 2-element array containing trend and plunge.
	// find the distance to the perimeter of the ellipsoid in the
	// direction specified by tp.

	// first, convert v to a 3-component unit vector, then find the
	// distance
	double[] u = new double[3];		
	unit_vector(tp, u);

	// if we are looking for the major axis of the ellipsoid, return -d
	// since simplex does minimization.
	return maximize * distance_to_perimeter(u);
    }

    /**
     * Convert a 3 component unit vector into trend, plunge.
     * @param (input) u unit vector
     * @param tp (output) trend and plunge in radians.  Trend is direction
     * east of north and plunge is angle down from horizontal,
     * in radians.
     */
    private void trendPlunge(double[] v, double[] tp)
    {
	tp[0] = atan2(v[1], v[0]);
	tp[1] = atan(v[2] / sqrt(v[0] * v[0] +v[1] * v[1]));
    }

    /**
     * Convert a trend and plunge (in radians) into a unit vector.
     * The length of the input vector is ignored if present.
     * @param tp (input) 2-element array with trend and plunge in radians.
     * @param v (output) unit vector.
     */
    private void unit_vector(double[] tp, double[] v)
    {
	v[0] = cos(tp[0]) * cos(tp[1]);
	v[1] = sin(tp[0]) * cos(tp[1]);
	v[2] = sin(tp[1]);
    }

	/**
	 * Retrieve the principal axes of the ellipoid.  The are contained in a 
	 * 3 x 3 array.  First row contains the major axis, second row contains the 
	 * intermediate axis, and third row contains the minor axis. 
	 * First column is the trend of the axis in radians, second column is plunge
	 * of the axis in radians (positive down from the horizontal), and the third 
	 * column is the length of the axis in km.
	 * 
	 * @return the principal axes of the ellipsoid
	 * @throws Exception
	 */
	public double[][] getPrincipalAxes() throws Exception
	{
		if (principal_axes == null)
			find_principal_axes();
		double[][] p = new double[3][3];
		for (int i = 0; i < 3; ++i)
		{
			p[i][0] = principal_axes[i][0];
			p[i][1] = principal_axes[i][1];
			p[i][2] = principal_axes[i][2]*hyperEllipse.getKappa(3);
		}
		return p;
	}

	public void writeVTK(File f, GeoVector center) throws Exception
	{
	    if (!f.getName().endsWith(".vtk"))
		throw new Exception("file name must end with 'vtk'");
	    
	    // get a GeoTessGrid object that will be shaped into an ellipsoid by 
	    // setting the radius of each vertex to a unique value.
	    GeoTessGrid grid = (GeoTessGrid)GeoTessBuilderMain.getGrid(1.); 
	    
	    // make a list of unit vectors with capacity of n vertices + 2
	    List<double[]> points = new ArrayList<>(grid.getNVertices()+2);
	    // for each vertex of the grid, compute the distance to the perimeter
	    // of the ellipsoid and add a point to the list
	    for (double[] v : grid.getVertices())
	    {
		double l = distance_to_perimeter(v) * sqrt(coeff[6]);
		points.add(new double[] {v[1]*l, v[0]*l, v[2]*l});
	    }
	    
	    // after all the grid vertices have been added to the list, add two more vectors.
	    
	    // write the full vector of the location of the center of the ellipsoid (not a unit vector).
	    points.add(center.getVector());
	    // write the offset of the center of the ellipsoid
	    // from the origin of the vtk reference frame.
	    points.add(new double[] {0,0,0});

	    // build the connectivity of the vtk dataset.
	    List<VTKCell> cells = new ArrayList<>();

	    for (int t = grid.getFirstTriangle(0); t <= grid.getLastTriangle(0); ++t)
	    {
		int[] indices = grid.getTriangleVertexIndexes(t);
		VTKCell cell = new VTKCell(VTKCellType.VTK_TRIANGLE, indices);
		cells.add(cell);
	    }

	    // write the vtk dataset to output file.  Add string '_ellipsoid' to the file name.
	    String name = f.getName();
	    int idx = name.indexOf('.');
	    String ext = name.substring(idx);
	    name = name.substring(0, idx)+"_ellipsoid"+ext;
	    VTKDataSet.write(new File(f.getParent(), name), points, cells);

	    // done writing the ellipsoid. Now write another vtk file for the primary axes of the 
	    // ellipsoid.
	    points.clear();
	    cells.clear();

	    // get the principal axes of the ellipsoid.  3x3 array. Each row contains
	    // the trend in radians, plunge in radians and length in km of one of the axes.
	    double[][] axes = getPrincipalAxes();

	    double[] u = new double[3];
	    for (int i=0; i<3; ++i)
	    {
		// convert trend, plunge, length to vector
		unit_vector(axes[i], u);
		for (int j=0; j<3; ++j) 
		    u[j] *= axes[i][2];
		// add a point at one end of the axis
		points.add(new double[] {u[1], u[0], u[2]});
		// add another point at other end of axis.
		points.add(new double[] {-u[1], -u[0], -u[2]});

		// add a vtk_line for each axis
		cells.add(new VTKCell(VTKCellType.VTK_LINE, new int[] {i*2, i*2+1}));
	    }
	    
	    // write the full vector of the location of the center of the ellipsoid.
	    points.add(center.getVector());
	    
	    // write the offset of the center of the ellipsoid
	    // from the origin of the vtk reference frame.
	    points.add(new double[] {0,0,0});

	    // write the 3 axes to a vtk file with the string '_axes' inserted before the extension.
	    name = f.getName();
	    idx = name.indexOf('.');
	    ext = name.substring(idx);
	    name = name.substring(0, idx)+"_axes"+ext;
	    VTKDataSet.write(new File(f.getParent(), name), points, cells);

	}

    public Buff getBuff() {

	Buff buffer = new Buff(getClass().getSimpleName());
	buffer.add("format", 1);
	try {
	    buffer.add("majax_length", getMajaxLength()); 
	    buffer.add("majax_trend", getMajaxTrend()); 
	    buffer.add("majax_plunge", getMajaxPlunge()); 
	    buffer.add("intax_length", getIntaxLength()); 
	    buffer.add("intax_trend", getIntaxTrend()); 
	    buffer.add("intax_plunge", getIntaxPlunge()); 
	    buffer.add("minax_length", getMinaxLength()); 
	    buffer.add("minax_trend", getMinaxTrend()); 
	    buffer.add("minax_plunge", getMinaxPlunge());
	} catch (Exception e) {
	    buffer.add("ellipsoid", e.getMessage());
	} 
	return buffer;
    }

    static public Buff getBuff(Scanner input) {
	Buff buf = new Buff(input);
	return buf;

    }

}
