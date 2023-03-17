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
import java.util.Collection;
import java.util.List;

import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotessbuilder.GeoTessBuilderMain;
import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.util.numerical.simplex.Simplex;
import gov.sandia.gmp.util.numerical.simplex.SimplexFunction;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

/**
 * <p>
 * Title: LocOOJava
 * </p>
 * 
 * A class to manage a 3D ellipsoid
 * 
 * The class keeps track of 3 things: center: a 4-element vector that contains
 * the position of the center of the ellipsoid. The components are LAT, LON,
 * DEPTH, TIME in the order specified in LocooDefines.h coeff: a 7-element
 * vector that contains the coefficients of the equation that defines the
 * ellipse. The equation is c0*x*x + c1*x*y + c2*x*z + c3*y*y + c4*y*z + c5*z*z
 * = c6 where x is latitude, y is longitude, z is depth. For projected
 * ellipsoids, c6 = 1 defines the ellipsoid that corresponds to the contour in
 * location space where delta chi square = 1. The ellipsoid can be scaled to
 * reflect current statistical parameters. Scaling simply sets the value of the
 * c6 coefficient to a specified value.
 * 
 * principal axes: a 3x3 matrix, each row of which contains the trend, plunge
 * and length of one of the principle axes of the ellipsoid. The first row
 * contains the major axis, the second row the intermediate axis and the third
 * row contains the minor axis. The trend is in radians clockwise from the x
 * axis, plunge is relative to the x,y plane, positive in the direction of
 * decreasing z.
 * 
 * 
 * The class is initialized by specifying the center and equation coefficients.
 * The principal axes are calculated internally using the Simplex minimization
 * algorithm as described in Press, et. al, 2002, Numercial Recipes in C++, 2nd
 * edition, page 413.
 * 
 * The class has a public method to calculate the distance from the center of
 * the ellipsoid to its perimenter in a direction specified by 3 direction
 * cosines ralative to the x, y and z axes.
 * 
 * Written By: Sandy Ballard Sandia National Laboratories November 2002
 * 
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
public class Ellipsoid implements SimplexFunction, Serializable
{
	private static final long serialVersionUID = 8743338800329104574L;
	
	private double[] coeff;
	private Location center;
	private double[][] principal_axes;
	
	private int maximize;

	/**
	 * Default constructor. Does nothing.
	 */
	public Ellipsoid()
	{
	}

	/**
	 * Initialize the ellipsoid by specifying the location of its center and the 
	 * coefficients of it equation.
	 * @param center Location object that manages the position of the center.
	 * @param coeff 7-element array containing equation of the ellipsoid.
	 */
	public void initialize(Location center, double[] coeff)
	{
		clear();
		this.center = center;
		this.coeff = coeff;
	}

	/**
	 * Sets everything to null.
	 */
	public void clear()
	{
		coeff = null;
		center = null;
		principal_axes = null;
	}

	/**
	 * Returns true if the coefficients of the equation of the ellipsoid
	 * are valid.
	 * @return true if the coefficients of the equation of the ellipsoid
	 * are valid.
	 */
	public boolean isValid()
	{
		return (coeff != null && coeff.length == 7 && coeff[6] > 0.0);
	}

	/**
	 * Retrieve the Location of the center of the ellipsoid
	 * @return the Location of the center of the ellipsoid
	 */
	public Location getCenter() { return center; }

	/**
	 * Specify the scale factor of the ellipsoid.
	 * @param kappa_sqr
	 */
	public void setScaleFactor(double kappa_sqr)
	{
		if (isValid())
			coeff[6] = kappa_sqr;
	}
	
	public double[] getCoeff() { return coeff; }

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
		double scale_factor = sqrt(coeff[6]);
		for (int i = 0; i < 3; ++i)
		{
			p[i][0] = principal_axes[i][0];
			p[i][1] = principal_axes[i][1];
			p[i][2] = principal_axes[i][2]*scale_factor;
		}
		return p;
	}

	/**
	 * Retrieve the trend of the major axis of the ellipsoid
	 * in radians. This is the angle in the horizontal plane 
	 * measured east of due north.  
	 * Range is 0 to 2*PI.
	 * @return the trend of the major axis of the ellipsoid
	 * in radians
	 * @throws Exception
	 */
	public double getMajaxTrend() throws Exception
	{
		if (principal_axes == null)
			find_principal_axes();
		return principal_axes[0][0];
	}

	/**
	 * Retrieve the plunge of the major axis of the ellipsoid
	 * in radians.  Plunge is the angle between the axis and 
	 * the horizontal plane; positive is down from the horizontal.
	 * Range is -PI/2 to PI/2.
	 * @return the plunge of the major axis of the ellipsoid
	 * in radians; positive is down.
	 * @throws Exception
	 */
	public double getMajaxPlunge() throws Exception
	{
		if (principal_axes == null)
			find_principal_axes();
		return principal_axes[0][1];
	}

	/**
	 * Retrieve the length of the major axis of the ellipsoid
	 * in km.
	 * @return the length of the major axis of the ellipsoid
	 * in km.
	 * @throws Exception
	 */
	public double getMajaxLength() throws Exception
	{
		if (principal_axes == null)
			find_principal_axes();
		return principal_axes[0][2] * sqrt(coeff[6]);
	}

	/**
	 * Retrieve the trend of the intermediate axis of the ellipsoid
	 * in radians. This is the angle in the horizontal plane 
	 * measured east of due north.
	 * Range is 0 to 2*PI.
	 * @return the trend of the intermediate axis of the ellipsoid
	 * in radians
	 * @throws Exception
	 */
	public double getIntaxTrend() throws Exception
	{
		if (principal_axes == null)
			find_principal_axes();
		return principal_axes[1][0];
	}

	/**
	 * Retrieve the plunge of the intermediate axis of the ellipsoid
	 * in radians.  Plunge is the angle between the axis and 
	 * the horizontal plane; positive is down from the horizontal.
	 * Range is -PI/2 to PI/2.
	 * @return the plunge of the intermediate axis of the ellipsoid
	 * in radians; positive is down.
	 * @throws Exception
	 */
	public double getIntaxPlunge() throws Exception
	{
		if (principal_axes == null)
			find_principal_axes();
		return principal_axes[1][1];
	}

	/**
	 * Retrieve the length of the intermediate axis of the ellipsoid
	 * in km.
	 * @return the length of the intermediate axis of the ellipsoid
	 * in km.
	 * @throws Exception
	 */
	public double getIntaxLength() throws Exception
	{
		if (principal_axes == null)
			find_principal_axes();
		return principal_axes[1][2] * sqrt(coeff[6]);
	}

	/**
	 * Retrieve the trend of the minor axis of the ellipsoid
	 * in radians. This is the angle in the horizontal plane 
	 * measured east of due north.
	 * Range is 0 to 2*PI.
	 * @return the trend of the minor axis of the ellipsoid
	 * in radians
	 * @throws Exception
	 */
	public double getMinaxTrend() throws Exception
	{
		if (principal_axes == null)
			find_principal_axes();
		return principal_axes[2][0];
	}

	/**
	 * Retrieve the plunge of the minor axis of the ellipsoid
	 * in radians.  Plunge is the angle between the axis and 
	 * the horizontal plane; positive is down from the horizontal.
	 * Range is -PI/2 to PI/2.
	 * @return the plunge of the minor axis of the ellipsoid
	 * in radians; positive is down.
	 * @throws Exception
	 */
	public double getMinaxPlunge() throws Exception
	{
		if (principal_axes == null)
			find_principal_axes();
		return principal_axes[2][1];
	}

	/**
	 * Retrieve the length of the minor axis of the ellipsoid
	 * in km.
	 * @return the length of the minor axis of the ellipsoid
	 * in km.
	 * @throws Exception
	 */
	public double getMinaxLength() throws Exception
	{
		if (principal_axes == null)
			find_principal_axes();
		return principal_axes[2][2] * sqrt(coeff[6]);
	}

	/**
	 * Retrieve the horizontal ellipse that represents the intersection of the
	 * ellipsoid with a horizontal plane at the specified depth.
	 * @param depth in km.
	 * @return
	 */
	public Ellipse getHorizontalEllipse(double depth)
	{
		Ellipse e = new Ellipse();
		double[] eq = new double[4];
		Location newcenter = center.clone();
		newcenter.setDepth(depth);

		// initialize the new ellipse with center at same epicenter, time but
		// with new depth and invalid coefficients.
		e.initialize(newcenter, eq);

		// if the ellipsoid is not valid, the ellipse won't be either.
		if (!isValid())
			return e;

		double z = (depth - center.getDepth());
		double denom = coeff[1] * coeff[1] - 4 * coeff[0] * coeff[3];
		double k = z * (2 * coeff[0] * coeff[4] - coeff[1] * coeff[2]) / denom;
		double h = -(coeff[4] * z + 2 * coeff[3] * k) / coeff[1];

		// initialize the new center to the old center
		// newcenter.copy(center);

		// move the new center to correct location.
		// move_location(newcenter, dc);
		newcenter = center.move(new double[] { h, k, z, 0. });

		// initialize the ellipse with new center but invalid coefficients.
		e.initialize(newcenter, eq);

		// calculate the right hand side of the ellipse equation.
		double rhs = coeff[6] - coeff[0] * h * h - coeff[1] * h * k - coeff[2]
				* h * z - coeff[3] * k * k - coeff[4] * k * z - coeff[5] * z
				* z;

		// right hand side less than zero indicates that plane where z == depth
		// does not intersect the ellipsoid. Return ellipse with center
		// extrapolated to horizontal position where intersection would be
		// and with invalid coefficients.
		if (rhs < 0.)
			return e;

		// right hand side equals zero indicates that plane where z == depth is
		// tangent to the ellipsoid. Return ellipse with right hand side = 0.
		if (rhs == 0.)
			for (int i = 0; i < 4; i++)
				eq[i] = 0.;
		else
		{
			// calculate the equation of the new ellipse
			eq[0] = coeff[0] / rhs;
			eq[1] = coeff[1] / rhs;
			eq[2] = coeff[3] / rhs;
			eq[3] = 1.0;
		}

		// initialize the ellipse with new center and equation coefficients.
		e.initialize(newcenter, eq);
		return e;
	}

	/**
	 * Use the simplex algorithm to find the major, intermediate and
	 * minor axes of the uncertainty ellipoid.  
	 * @throws Exception
	 */
	private void find_principal_axes() throws Exception
	{
		principal_axes = new double[3][];
		
		try
		{
			// set maximize negative to find the major principal axis
			maximize = -1;
			
			// find trend, plunge and length of major axis
			principal_axes[0] = find_extreme();

			if (principal_axes[0].length == 0)
			{
				clear();
				return;
			}

			// set maximize positive to find the minor axis.
			maximize = 1;
			
			// find trend, plunge and length of minor axis
			principal_axes[2] = find_extreme();

			if (principal_axes[2].length == 0)
			{
				clear();
				return;
			}
		}
		catch (Exception e)
		{
			throw new Exception(e);
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
		while (principal_axes[1][0] < 0.)
			principal_axes[1][0] += 2 * PI;
		while (principal_axes[1][0] > 2 * PI)
			principal_axes[1][0] -= 2 * PI;
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
		
		// at this point, p[0] holds the trend and plunge of the principle axis
		// and y[0] holds the length of the axis.  Move trend, plunge and length
		// into y.
		y[2] = abs(y[0]);  // length
		y[0] = p[0][0];  // trend
		y[1] = p[0][1];  // plunge
		
		// ensure that the plunge is in range -PI/2 to PI/2.
		while (y[1] < -PI / 2)
			y[1] += PI;
		while (y[1] > PI / 2)
			y[1] -= PI;
		
		// ensure that the plunge is positive (down from horizontal)
		if (y[1] < 0.)
		{
			y[1] = -y[1];
			y[0] += PI;
		}
		
		// ensure that the trend is in range 0 to 2*PI.
		while (y[0] < 0.)
			y[0] += 2 * PI;
		while (y[0] >= 2 * PI)
			y[0] -= 2 * PI;

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
		tp[1] = atan(-v[2] / sqrt(v[0] * v[0] +v[1] * v[1]));
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
		v[2] = -sin(tp[1]);
	}

	public void writeVTK(File f) throws Exception
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

	/**
	 * This main() program will load a bunch of *_ellipsoid.vtk and *_axes.vtk files, compute
	 * the centroid of their centers and then translate the contents of each file away from the centroid
	 * by the appropriate distance.  Input files are overwriiten but can be restored by 
	 * running this app with only a single command line parameter equal to the file to be restored.
	 * <p>Command line arguments are the names of vtk files and/or directories containing vtk files.
	 * <p>This main() can be run by executing:
	 * <br>java -cp locoo3d.jar gov.sandia.gmp.locoo3d.Ellipsoid <list of vtk files/directories> 
	 * @param args
	 */
	static public void main(String[] args)
	{
	    try {
		if (args.length ==0)
		{
		    throw new Exception("Specify namea of vtk files and/or directories containing vtk files which are "
		    	+ "to  be translated away from their centroid.");
		}
		List<File> vtkFiles = new ArrayList<>();
		for (String arg : args)
		    findVTKFiles(new File(arg), vtkFiles);

		// list of vtk datasets loaded from vtk files.
		List<VTKDataSet> dataSets = new ArrayList<>(vtkFiles.size());
		
		// centroid will be the unit vector equal to the normalized vector 
		// sum of all the ellipsoid centers.
		double[] centroid = new double[3];
		// centroidDepth will be the average depth of all the centers.
		double centroidDepth = 0;
		
		int nameLength = 0;
		for (File f : vtkFiles)
		{
		    // load a dataset from the vtk file and save it
		    VTKDataSet dataSet = new VTKDataSet(f); 
		    dataSets.add(dataSet);
		    
		    // get a reference to the list of points in the dataset
		    List<double[]> points = dataSet.getPoints();
		    // get a copy of the center.
		    double[] center = points.get(points.size()-2).clone();
		    // normalize center to a unit vector and save the length of the 
		    // vector before it was normalized
		    double rCenter = VectorUnit.normalize(center);
		    
		    // accumulate the sum of the center depths
		    centroidDepth += VectorGeo.getEarthRadius(center)-rCenter;
		    // accumulate vector sum of all the centers.
		    for (int i=0; i<3; ++i)
			centroid[i] += center[i];
		    
		    nameLength = Math.max(nameLength, f.getName().length());
		}

		// normalize centroid to unit vector and find average depth
		VectorUnit.normalize(centroid);
		centroidDepth /= vtkFiles.size();

		// find the earth radius at location of centroid
		double earthRadiusCentroid = VectorGeo.getEarthRadius(centroid);

		System.out.printf("Centroid lat, lon, depth: %s %1.3f%n%n", 
			VectorGeo.getLatLonString(centroid), centroidDepth);

		String format = String.format("%%-%ds %%s %%8.3f %%9.3f %%9.3f %%9.3f%n", nameLength);

		String s = "                                                                             ".substring(0, nameLength);
		System.out.printf(s+"       Lat        Lon    Depth     X(km)     Y(km)     Z(km)%n");

		// iterate over all the vtk datasets
		for (int i=0; i<dataSets.size(); ++i)
		{
		    // get a reference to the points in the dataSet
		    List<double[]> points = dataSets.get(i).getPoints();

		    // get a copy of the center of ellipsoid
		    double[] center = points.get(points.size()-2).clone();

		    // normalize center to unit vector and save teh radius.
		    double centerRadius = VectorUnit.normalize(center);
		    // convert the centerRadius to centerDepth
		    double centerDepth = VectorGeo.getEarthRadius(center)-centerRadius;

		    // get reference to old offset (usually [0,0,0])
		    double[] old_offset = points.get(points.size()-1).clone();

		    // initialize the new offset to zero
		    double[] new_offset = new double[3];

		    // find azimuth from centroid to center. Will be nan if they are colocated.
		    double azimuth = VectorGeo.azimuth(centroid, center, Double.NaN);

		    if (!Double.isNaN(azimuth))
		    {
			double distanceKm = VectorGeo.angle(centroid, center)*earthRadiusCentroid;
			new_offset[0] = distanceKm * Math.sin(azimuth);
			new_offset[1] = distanceKm * Math.cos(azimuth);
			new_offset[2] = centroidDepth-centerDepth;
		    }

		    System.out.printf(format, vtkFiles.get(i).getName(), 
			    VectorGeo.getLatLonString(center), centerDepth,
			    new_offset[0], new_offset[1], new_offset[2]);

		    // translate the ellipsoid by new_offset minus old_offset
		    for (int j=0; j<points.size()-2; ++j)
		    {
			double[] point = points.get(j);
			for (int k=0; k<3; ++k)
			    point[k] += new_offset[k]-old_offset[k];
		    }

		    // set offset to new offset
		    points.set(points.size()-1, new_offset);

		    // write the dataset out to the same file that it was read from.
		    dataSets.get(i).write(vtkFiles.get(i));
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	}

	private static void findVTKFiles(File file, Collection<File> files)
	{
	    if (file.isDirectory())
		for (File f : file.listFiles())
		    findVTKFiles(f, files);
	    else if (file.getName().toLowerCase().endsWith("vtk"))
		files.add(file);
	}
}
