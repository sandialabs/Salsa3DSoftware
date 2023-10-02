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

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

import java.io.Serializable;
import java.util.Arrays;

import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.matrix.LUDecomposition;
import gov.sandia.gmp.util.numerical.matrix.Matrix;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origerr;

/**
 * <p>Title: LocOOJava</p>
 *
 * A class to manage a 4D hyper-ellipse
 *
 * The class keeps track of 3 things:
 *   center:  a 4-element vector that contains the position of the center of the
 *            hyper-ellipse.  The components are LAT, LON, DEPTH, TIME in the order
 *            specified in LocooDefines.h
 *
 *   coeff:   an 11-element vector that contains the coefficients of the equation
 *            that defines the ellipse.  The equation is
 *            c0*x*x + c1*x*y + c2*x*z + c3*x*t + c4*y*y + c5*y*z + c6*y*t
 *               + c7*z*z + c8*z*t + c9*t*t = c10
 *            where x is latitude, y is longitude, z is depth, t is time.
 *            c10 = 1 defines the hyper-ellipse that corresponds
 *            to the contour in location space where delta chi square = 1.  The
 *            hyper-ellipse can be scaled to reflect current statistical parameters.
 *            Scaling simply sets the value of the c10 coefficient to a specified
 *            value.
 *
 *   principal axes:  a 5x4 matrix.  Each of the 4 columns contains one of the
 *            principal axes.  The first 4 elements of each column contain the
 *            components of a unit vector in each of the directions LAT, LON
 *            DEPTH, TIME, with the order being that specified in LocooDefines.h.
 *            The 5th element of each column contains the length of that
 *            principal axis.
 *
 *
 * The class is initialized by specifying the center and the uncertainty matrix
 * that the hyper-ellipse describes.  The principal axes and the coefficients
 * of the hyper-ellipse equation are calculated internally.
 *
 * The class has a public method to calculate the distance from the center of
 * the ellipsoid to its perimenter in a direction specified by 4 direction cosines
 * ralative to the x, y, z and t axes.
 *
 * The class has several public methods that return various 3D ellipsoids and
 * 2D ellipses that can be generated from the hyper-ellipse.  In particular,
 * getProjectedEllipsoid() returns the 3D ellipsoid that results from the projection of
 * the hyperellipse onto a plane of constant time and getProjectedEllipse() returns
 * the ellipse that results from the projection of the hyper-ellipse onto
 * planes of constant depth and time.  getIntersectionEllipsoid(time) returns the ellipsoid
 * that represents the intersection of the hyper-ellipse with the plane where
 * t=time.
 *
 * There is also a public method that returns the covariance matrix represented
 * by the uncertainty matrix.
 *
 * Written By:  Sandy Ballard
 *              Sandia National Laboratories
 *              November 2002
 *
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Sandia National Laboratories</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public class HyperEllipse implements Serializable
{
    private static final long serialVersionUID = -57778419195285669L;

    private double[][] principal_axes; // 5x4 uncertainty matrix

    private Location center;

    private double[] coeff; // 11 coefficients of ellipoid equation

    private double[][] covariance; // 4x4 covariance matrix

    private Ellipsoid ellipsoid; // the projected hypocentral ellipsoid

    private Ellipse ellipse; // the projected epicentral ellipsoid

    private boolean gotStats;

    private boolean infiniteUncertainty;

    // Whether the i'th component of the solution was fixed.
    private boolean[] fixed; 

    private double[] kappa;

    private int Nobs;

    private int M;

    private double sigma = Double.NaN;

    //apriori estimate of the data variance scale factor
    // s_sub_k_squared in Bratte & Bache (1988) eq. 6.
    private double apriori_variance; 

    //confidence ( 0. <= confidence <= 1.)
    private double conf; 
    public double getConf() {return conf;}

    //K value of Jordan and Sverdrup (1981).
    private int K;
    
    private double sdobs;
    
    /**
     * The value of K in the Jordan and Sverdrup (1981) statistical formulation.
     * See lsq_algrorithm.pdf for more information.
     * K == -1 implies that K is infinite.
     * @return
     */
    public int getKWeight() { return K; }

    double sumSQRWeightedResiduals;

    // **** _FUNCTION DESCRIPTION_ *************************************************
    //
    // Initialize a HyperEllipse by specifying the 4 components of its
    // center, and its principal axes, and its scale factor (kappa squared).
    //
    // INPUT ARGS:  c: 4 components of the center of the hyperellipse. Units are
    //	              LAT:radians, LON:radians, DEPTH:km, TIME:sec.  Order of
    //	              components is specified in LocooDefines.h
    //	              uu: 5x4 uncertainty matrix. first 4 rows describe unit Vectors
    //	              in principal directions.  Last row contains lengths of principal
    //	              semi-axes. All spatial components have units of km, time is sec.
    //	              Order components is as specified in LocooDefines.h
    //	              kappa_sqr: the scale factor for this hyper-ellipse
    // OUTPUT ARGS: NONE
    // RETURN:      NONE
    //
    // *****************************************************************************
    public HyperEllipse(Location c, boolean[] fixed, int Nobs, double[][] uu, 
	    int K, double apriori_variance, double sumSQRWeightedResiduals, double conf, double sdobs) throws Exception
    {
	center = c;
	this.fixed = fixed;
	this.Nobs = Nobs;
	this.K = K;
	this.apriori_variance = apriori_variance;
	this.conf = conf;
	this.sumSQRWeightedResiduals = sumSQRWeightedResiduals;
	if (uu != null && uu.length == 5) {
	    // set the principal axes = to uncertainty matrix.
	    principal_axes = uu;
	    gotStats = true;
	}

	this.M = 4; for (boolean b : fixed) if (b) --this.M;
	
	getSigma();
	
	this.sdobs = sdobs;

	//printMatrix("initialize()  principal_axes", principal_axes);
    }

    public void setScaleFactor(double kappa_sqr) 
	    throws Exception
    {
	if (isValid())
	    coeff[10] = kappa_sqr;
    }

    public boolean isValid() 
	    throws Exception
    {
	// if the coefficients have not yet been calculated, do so.  This will
	// also calculate the covariance_matrix.
	if (coeff == null)
	{
	    // calculate the coefficients of the equation describing the uncertainty
	    // hyper-ellipse.  Note that the order of the coordinates is
	    // specified such that x=LAT, y=LON, z=DEPTH, t=TIME which is a
	    // right handed coordinate system.
	    int[] par = new int[] {GMPGlobals.LAT, GMPGlobals.LON, 
		    GMPGlobals.DEPTH, GMPGlobals.TIME};
	    coeff = uncertainty_equation_coefficients(par);
	    setScaleFactor(pow(getKappa(4),2));
	}
	return coeff != null && coeff[10] >= 0.;
    }

    // **** _FUNCTION DESCRIPTION_ *************************************************
    //
    // clear all result structures except principle axes.
    //
    // *****************************************************************************
    public HyperEllipse clear()
    {
	coeff = null;
	covariance = null;
	ellipse = null;
	ellipsoid = null;
	return this;
    }

    // returns the 4x4 covariance matrix.  It is not scaled by the scale factor.
    public double[][] getCovariance() 
	    throws Exception
    {
	if (covariance == null)
	    calculate_covariance_matrix();
	return covariance;
    }

    // return the 11 coefficients of the hyperellipse equation.
    public double[] getCoefficients() 
	    throws Exception
    {
	isValid(); // called to make sure coeff has been calculated.
	return coeff;
    }

    // **** _FUNCTION DESCRIPTION_ *************************************************
    //
    // scale the lengths of the principal axes to reflect current statistical
    // parameters and return.
    //
    // *****************************************************************************
    public double[][] getPrincipalAxes() 
	    throws Exception
    {
	double[][] a = principal_axes.clone();
	if (isValid())
	{
	    double scale_factor = sqrt(coeff[10]);
	    for (int i = 0; i < 4; i++)
		a[4][i] *= scale_factor;
	}
	return a;
    }

    // **** _FUNCTION DESCRIPTION_ *************************************************
    //
    // get the ellipse that results from the projection of the hyperellipse onto
    // a surface of constant depth and time.
    //
    // *****************************************************************************
    public Ellipse getProjectedEllipse(double kappa_sqr) 
	    throws Exception
    {
	if (ellipse == null)
	    ellipse = new Ellipse();

	if (!ellipse.isValid())
	{
	    // calculate the projection of the uncertainty hyper-ellipse onto planes
	    // of constant depth and time.  Note that the order of the coordinates is
	    // specified such that in the ellipse x=LAT, y=LON.
	    try
	    {
		ellipse.initialize(center, uncertainty_equation_coefficients(
			new int[] {GMPGlobals.LAT, GMPGlobals.LON}));
	    }
	    catch (Exception e)
	    {

	    }
	}
	ellipse.setScaleFactor(kappa_sqr);
	return ellipse;
    }

    // **** _FUNCTION DESCRIPTION_ *************************************************
    //
    // get an ellipsoid that results from the projection of the hyperellipse onto
    // a surface of constant time t.
    //
    // *****************************************************************************
    public Ellipsoid getProjectedEllipsoid(double kappa_sqr) 
	    throws Exception
    {
	if (ellipsoid == null)
	    ellipsoid = new Ellipsoid();

	if (!ellipsoid.isValid())
	{
	    // calculate the projection of the uncertainty hyper-ellipse onto plane
	    // of constant time.  Note that the order of the coordinates is
	    // specified such that in the ellipsoid x=LAT, y=LON, z=DEPTH which is a
	    // right handed coordinate system.
	    int[] par = new int[] {GMPGlobals.LAT, GMPGlobals.LON, GMPGlobals.DEPTH};
	    ellipsoid.initialize(center, uncertainty_equation_coefficients(par));
	}
	ellipsoid.setScaleFactor(kappa_sqr);
	return ellipsoid;
    }

    // **** _FUNCTION DESCRIPTION_ *************************************************
    //
    // get an ellipsoid that results from the intersection of the hyperellipse with
    // a surface of constant time t.
    //
    // *****************************************************************************
    public Ellipsoid getIntersectionEllipsoid(double t) throws Exception
    {
	Ellipsoid e = new Ellipsoid();
	double[] uu = new double[0];

	double tmax = sqrt(covariance[GMPGlobals.TIME][GMPGlobals.TIME] * coeff[10]);

	if (!isValid() || abs(t - center.getTime()) > tmax)
	{
	    e.initialize(center, uu);
	    return e;
	}

	double[][] h = new double[3][3];

	h[0][0] = 2 * coeff[0];
	h[0][1] = coeff[1];
	h[0][2] = coeff[2];
	h[1][0] = coeff[1];
	h[1][1] = 2 * coeff[4];
	h[1][2] = coeff[5];
	h[2][0] = coeff[2];
	h[2][1] = coeff[5];
	h[2][2] = 2 * coeff[7];

	LUDecomposition lu = new LUDecomposition(h);
	if (lu.isNonsingular())
	    h = lu.solve(Matrix.identity(3,3)).getArray();
	else
	    throw new Exception("ERROR: Singular matrix in  getEllipsoid()%n");

	double[] r = new double[3];
	double[] c = new double[4];
	c[3] = t - center.getTime();
	r[0] = -coeff[3] * c[3];
	r[1] = -coeff[6] * c[3];
	r[2] = -coeff[8] * c[3];

	for (int i = 0; i < 3; i++)
	    for (int j = 0; j < 3; j++)
		c[i] += h[i][j] * r[j];

	double scale = (coeff[10] - coeff[0] * c[0] * c[0] - coeff[1] * c[0] * c[1] - coeff[2] * c[0] * c[2] 
		- coeff[3] * c[0] * c[3] - coeff[4] * c[1] * c[1] - coeff[5] * c[1] * c[2] - coeff[6] * c[1] * c[3] 
			- coeff[7] * c[2] * c[2] - coeff[8] * c[2] * c[3] - coeff[9] * c[3] * c[3]);

	if (scale > 0.0)
	{
	    uu = new double[7];
	    uu[0] = coeff[0] / scale;
	    uu[1] = coeff[1] / scale;
	    uu[2] = coeff[2] / scale;
	    uu[3] = coeff[4] / scale;
	    uu[4] = coeff[5] / scale;
	    uu[5] = coeff[7] / scale;
	    uu[6] = 1.0;
	}

	e.initialize(center.move(c), uu);
	return e;
    }

    // **** _FUNCTION DESCRIPTION_ *************************************************
    //
    // compute the distance from the center of the hyperellipse to its perimeter, in
    // the direction specified by the 4-component unit VectorMod v.
    //
    //
    // *****************************************************************************
    public double distance_to_perimeter(double[] v, boolean scaled) 
	    throws Exception
    {
	// calculate distance from center of HyperEllipse to its perimeter, in
	// the direction specified by VectorMod x.
	// v is a 4-component unit VectorMod specifying direction in which to look.
	if (!isValid())
	    return 0.;
	if (v.length != 4)
	    return -1.;
	double scale = 1;
	if (scaled)
	    scale = coeff[10];
	return sqrt(scale /
		(coeff[0] * v[0] * v[0] +
			coeff[1] * v[0] * v[1] +
			coeff[2] * v[0] * v[2]
				+ coeff[3] * v[0] * v[3] +
				coeff[4] * v[1] * v[1] +
				coeff[5] * v[1] * v[2] +
				coeff[6] * v[1] * v[3]
					+ coeff[7] * v[2] * v[2] +
					coeff[8] * v[2] * v[3] +
					coeff[9] * v[3] * v[3]));
    }

    // **** _FUNCTION DESCRIPTION_ *************************************************
    //
    // Calculate the covariance matrix.
    //
    // INPUT ARGS:  5x4 uncertainty matrix. first 4 rows describe unit VectorMods
    //	              in principal directions.  Last row contains lengths of principal
    //	              semi-axes.
    // OUTPUT ARGS: NONE
    //
    // See lsq_algorithm.pdf eq 6.1.
    //
    // *****************************************************************************
    public void calculate_covariance_matrix() 
	    throws Exception
    {
	if (principal_axes == null)
	    throw new Exception("principle axes are not defined.");

	//resize the covariance matrix to 4x4 and initialize to zero
	covariance = new double[4][4];

	for (int row = 0; row < 4; row++)
	    for (int col = 0; col < 4; col++)
		for (int i = 0; i < 4; i++)
		    covariance[row][col] += principal_axes[row][i] *
		    principal_axes[col][i] * pow(principal_axes[4][i], 2);

	//printMatrix("calculate_covariance_matrix()", covariance);
    }

    // **** _FUNCTION DESCRIPTION_ *************************************************
    //
    // Calculate the coefficients of the equation describing the uncertainty region.
    // In 2D, this will be the equation of the uncertainty ellipse.
    //	    c1*x*x + c2*x*y + c3*y*y = 1  where  x=latitude, y=longitude
    // In 3D, this will be the equation of an ellipsoid:
    //	    c1*x*x + c2*x*y + c3*x*z + c4*y*y + c5*y*z + c6*z*z = 1  where z=depth
    // In 4D, this will be the equation of a hyper-ellipse (t=time):
    //	    c1*x*x + c2*x*y + c3*x*z + c4*x*t + c5*y*y + c6*y*z + c7*y*t + c8*z*z + c9*z*t + c10*t*t = 1
    //
    // See discussion in Press, et. al, 2002, Numercial Recipes in C++, 2nd edition, page 702-703.
    //
    // INPUT ARGS:  parameter: a VectorMod of the parameters that are included.  Order should
    //	                define a right handed coordinate system.
    // OUTPUT ARGS: None.
    // RETURN:      the VectorMod of equation coefficients as described above.  If
    //	          a singular matrix was encountered, no valid ellipse exists and
    //	          an empty VectorMod is returned.
    //
    // *****************************************************************************
    public double[] uncertainty_equation_coefficients(int[] parameter) 
	    throws Exception
    {
	// call this to ensure covariance has been calculated
	getCovariance();

	int n = parameter.length; //number of dimensions.
	ArrayListDouble c = new ArrayListDouble(n*n+1);

	//extract the desired rows and columns from the covariance matrix.
	Matrix A = new Matrix(n, n);
	for (int i = 0; i < n; i++)
	    for (int j = 0; j < n; j++)
		A.set(i,j, covariance[parameter[i]][parameter[j]]);

	//printMatrix("uncertainty_equation_coefficients(), A", covariance);

	//invert the submatrix.  
	try
	{
	    A = A.inverse();
	}
	catch (Exception e)
	{
	    throw new Exception(String.format("Covariance matrix is singular.%n%s%n", 
		    getMatrixString("uncertainty_equation_coefficients(), A", covariance)));
	}

	//push on the coefficients on the left hand side, in order
	for (int j = 0; j < n; j++)
	    for (int i = j; i < n; i++)
		if (i == j)
		    c.add(A.get(i,j));
		else
		    c.add(2 * A.get(i,j));
	//push on the right hand side.
	c.add(1.0);
	return c.toArray();
    }

    @SuppressWarnings("unused")
    private void printMatrix(String title, double[][] m)
    {
	System.out.print(getMatrixString(title, m));
    }

    private String getMatrixString(String title, double[][] m)
    {
	StringBuffer buf = new StringBuffer();
	buf.append(String.format("%nHyperEllipse.%s  %d x %d%n", title, m.length, m[0].length));
	for (int i=0; i<m.length; ++i)
	{
	    for (int j=0; j<m[i].length; ++j)
		buf.append(String.format("  %20.8f", m[i][j]));
	    buf.append(Globals.NL);
	}
	buf.append(Globals.NL);
	return buf.toString();
    }

    public double getSxx() throws Exception
    {
	return getS(GMPGlobals.LAT, GMPGlobals.LAT);
    }

    public double getSyy() throws Exception
    {
	return getS(GMPGlobals.LON, GMPGlobals.LON);
    }

    public double getSzz() throws Exception
    {
	return getS(GMPGlobals.DEPTH, GMPGlobals.DEPTH);
    }

    public double getStt() throws Exception
    {
	return getS(GMPGlobals.TIME, GMPGlobals.TIME);
    }

    public double getSxy() throws Exception
    {
	return getS(GMPGlobals.LAT, GMPGlobals.LON);
    }

    public double getSxz() throws Exception
    {
	return getS(GMPGlobals.LAT, GMPGlobals.DEPTH);
    }

    public double getSyz() throws Exception
    {
	return getS(GMPGlobals.LON, GMPGlobals.DEPTH);
    }

    public double getStx() throws Exception
    {
	return getS(GMPGlobals.LAT, GMPGlobals.TIME);
    }

    public double getSty() throws Exception
    {
	return getS(GMPGlobals.LON, GMPGlobals.TIME);
    }

    public double getStz() throws Exception
    {
	return getS(GMPGlobals.DEPTH, GMPGlobals.TIME);
    }

    private double getS(int comp1, int comp2) throws Exception
    {
	if (!gotStats || infiniteUncertainty || fixed[comp1] || fixed[comp2])
	    return Origerr.SXX_NA;
	return getCovariance()[comp1][comp2];

    }

    public double getSdepth() throws Exception
    {
	if (!gotStats || infiniteUncertainty || fixed[GMPGlobals.DEPTH])
	    return Origerr.SDEPTH_NA;
	else
	    return sqrt(getCovariance()[GMPGlobals.DEPTH][GMPGlobals.DEPTH]) * getKappa(1);
    }

    public double getStime() throws Exception
    {
	if (!gotStats || infiniteUncertainty || fixed[GMPGlobals.TIME])
	    return Origerr.STIME_NA;
	else
	    return sqrt(getCovariance()[GMPGlobals.TIME][GMPGlobals.TIME]) * getKappa(1);
    }

    public double getSmajax()
	    throws Exception
    {
	if (!gotStats || infiniteUncertainty || fixed[GMPGlobals.LAT] || fixed[GMPGlobals.LON])
	    return Origerr.SMAJAX_NA;
	else
	    return getEllipse().getMajaxLength();
    }

    private Ellipse getEllipse() throws Exception {
	if (ellipse == null)
	    ellipse = getProjectedEllipse(pow(getKappa(2),2));
	return ellipse;
    }

    private Ellipsoid getEllipsoid() throws Exception {
	if (ellipsoid == null)
	    ellipsoid = getProjectedEllipsoid(pow(getKappa(3),2));
	return ellipsoid;
    }

    public double getSminax()throws Exception
    {
	if (!gotStats || infiniteUncertainty || fixed[GMPGlobals.LAT] || fixed[GMPGlobals.LON])
	    return Origerr.SMINAX_NA;
	else
	    return getEllipse().getMinaxLength();
    }

    public double getStrike()throws Exception
    {
	if (!gotStats || infiniteUncertainty || fixed[GMPGlobals.LAT] || fixed[GMPGlobals.LON])
	    return Origerr.STRIKE_NA;
	else
	    return toDegrees(getEllipse().getMajaxTrend());
    }

    // **** _FUNCTION DESCRIPTION_ *************************************************
    //
    // Calculate Kappa, if necessary.  There are four different kappa values, one
    // for each number of free parameters (1 through 4).  If current values are
    // up to date, they are not recalculated.  If they are recalculated, then the
    // current uncertainty limits (hyper_ellipse, hypocentral_ellipsoid and
    // epicentral_ellipsoid) are automaticallly rescaled with recalculated values.
    //
    // *****************************************************************************

    public double getKappa(int m) throws Exception
    {
	// See lsq_algorithm.pdf eq. 6.15
	if (kappa == null)
	{
	    if (Nobs - M >= 0 && conf > 0. && conf < .9991) {
		kappa = new double[4];
		for (int i = 0; i < 4; i++)
		    kappa[i] = sigma * sqrt(FStatistic.f_statistic(i+1, Nobs - M, K, conf));
	    }
	    else
		Arrays.fill(kappa, 0.);
	}
	if (m >= 1 && m <= 4)
	    return kappa[m - 1];
	return Double.POSITIVE_INFINITY;
    }

    // **** _FUNCTION DESCRIPTION_ *************************************************
    //
    // Calculate Sigma, if necessary.
    //
    // *****************************************************************************
    public double getSigma()
    {
	// See svd_algorithm.pdf eq. 6.16
	if (Double.isNaN(sigma))
	{
	    if (K < 0)
		// K < 0 is interpreted as K = infinity.
		sigma = sqrt(apriori_variance);
	    else if (K + Nobs - M > 0)
		sigma = sqrt( (K * apriori_variance + sumSQRWeightedResiduals) /
			(K + Nobs - M));
	    else
		sigma = 0.;
	}
	return sigma;
    }

    public double getSdobs() {
        return sdobs;
    }

}
