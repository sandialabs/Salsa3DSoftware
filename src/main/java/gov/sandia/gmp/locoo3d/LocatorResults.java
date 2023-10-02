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
package gov.sandia.gmp.locoo3d;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

import java.io.File;
import java.util.Arrays;

import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.baseobjects.flinnengdahl.FlinnEngdahlCodes;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.hyperellipse.Ellipse;
import gov.sandia.gmp.baseobjects.hyperellipse.Ellipsoid;
import gov.sandia.gmp.baseobjects.hyperellipse.FStatistic;
import gov.sandia.gmp.baseobjects.hyperellipse.HyperEllipse;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gnem.dbtabledefs.gmp.Source;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origerr;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_custom.Azgap;

/**
 * <p>Title: LocOOJava</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Sandia National Laboratories</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */

public class LocatorResults
{
  private Event event;

  private Location location = null; 

  //sum squared weighted residuals of only
  // defining observations
  private double sumSQRWeightedResiduals; 

  /**
   * The standard deviation of the weighted residuals.  
   * Includes all defining tt, az and sh weighted residuals. 
   */
  private double sdobs;

  private int Nass; // number of arrivals associated with the event
  private int Ndef; // number of time-defining phases.
  private int Nobs; // number of defining observations (those used to locate event)
  private int Ndeleted; // number of observations converted to non-defining during

  //number of free parameters in the inversion
  private int M; 

  //number of time sum square weighted residuals were calculated
  // in order to compute solution.
  private int nFunc; 

  // Whether the i'th component of the solution was fixed.
  private boolean[] fixed = new boolean[4]; 

  //depth determination flag.  one of {f,d,r,g}.
  // f=free,
  // d=depth phases, (locoo will never set this)
  // r=restrained by locator (depth was free in par file)
  // g=restrained by geophysicist (depth fixed in par file)
  private String dtype; 

  // name of algorithm used to compute the location
  private String algorithm; 

  private String author;

  //number of iterations required to achieve solution
  private int niter; 

  //true if the algorithm converged
  private boolean converged; 

  private boolean gotStats = false;

  //apriori estimate of the data variance scale factor
  // s_sub_k_squared in Bratte & Bache (1988) eq. 6.
  private double apriori_variance; 

  //confidence ( 0. <= confidence <= 1.)
  private double conf; 

  //K value of Jordan and Sverdrup (1981).
  private int K; 

  // 5x4 matrix containing the principal axes of the 4D uncertainty
  // hyper ellipse.  Each column contains the components of one of the
  // principal axes.  The first 4 elements of each column are the
  // direction cosines with respect to the lat, lon, depth and time
  // axes in that order.  The 5th element must be multiplied by kappa(4)
  // to convert it to the length of the semi axis in units of km*sec.
  private HyperEllipse hyper_ellipse;

  private double[][] uncertainty;

  private boolean infiniteUncertainty;

  private double sigma;
  private double[] kappa=new double[4];
  boolean sigma_fresh;
  private boolean kappa_fresh;

  //possible existance of local minima.
  private boolean local_minima; 
  
  // if property ellipsoidVTK is set, and a 3D error ellipsoid can be calculated,
  // a vtk file is generated with an image of the ellipsoid.
  private String ellipsoidVTK;


  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // LocatorResults Constructor
  //
  //
  // INPUT ARGS:  NONE
  // OUTPUT ARGS: NONE
  // RETURN:      NONE
  //
  // *****************************************************************************
  public LocatorResults(Event event) throws Exception
  {
    this.event = event;

    PropertiesPlus p = event.getEventParameters().properties();
    
    author = event.getEventParameters().getAuthor();

    conf = p.getDouble("gen_confidence_level", 0.95);
    setConf(conf);

    String lrEllipseType = p.getProperty("gen_error_ellipse_type", "coverage");

    // The Jordan-Sverdrup K must also reflect what liEllipseType is
    if (lrEllipseType.equals("coverage"))
      setK( -1);
    else if (lrEllipseType.equals("confidence"))
      setK(0);
    else
      setK(p.getInt("gen_jordan_sverdrup_K", 8));

    setApriori_std_err(p.getDouble("gen_apriori_standard_error", 1.0));
    
    ellipsoidVTK = p.getProperty("ellipsoidVTK");
  }

  /**
   *
   * @param convergence_flag boolean
   * @param algorithm String
   * @param evid long
   * @param orid long
   * @param location Location
   * @param uncertainty double[][]
   * @param sumSQRWeightedResiduals double
   * @param rmsTTResiduals double
   * @param nFunc long
   * @param <any> unknown
   * @return boolean  true if an error occurred, false if everything valid.
 * @throws Exception 
 * @throws Exception 
   */
  protected boolean setLocation(
      boolean convergence_flag, // Convergence indicator
      long evid, // event id number
      long orid, // origin id number

      Location location,

      double[][] uncertainty,

      double sumSQRWeightedResiduals,
      //sum squared weighted residuals of only
      // defining observations

      double rmsTTResiduals,
      // sum squared residuals of only
      // defining travel time observations

      // The standard deviation of the weighted residuals.  
      // Includes all defining tt, az and sh weighted residuals. 
      double sdobs,

      int nFunc, // number of times sswr was computed
      int Nass, // number of arrivals associated with the event
      int Ndef, // number of time-defining phases.
      int Nobs, // number of defining observations (those used to locate event)
      int Ndeleted, // number of observations converted to non-defining during
      int M,

      boolean[] fixed, // Whether the solution used a fixed longitude

      String dtype, // depth determination type
      // f=free, d=depth phases, r=fixed by code, g=fixed in par file
      int niterations, // number of iterations required to achieve solution
      boolean local_minima // possible existance of local minima
      ) throws Exception
  {
    this.converged = convergence_flag;
    this.algorithm = algorithm;
    this.location = location;
    this.uncertainty = uncertainty;
    gotStats = uncertainty != null;
    this.sumSQRWeightedResiduals = sumSQRWeightedResiduals;
    //this.rmsTTResiduals = rmsTTResiduals;
    this.sdobs = sdobs;
    this.nFunc = nFunc;
    this.Nass = Nass;
    this.Ndef = Ndef;
    this.Nobs = Nobs;
    this.Ndeleted = Ndeleted;
    this.fixed = fixed.clone();
    this.dtype = dtype;
    this.M = M;
    this.Nass = Nass;
    this.niter = niterations;
    this.local_minima = local_minima;

    sigma_fresh = false;
    kappa_fresh = false;
    
    if (ellipsoidVTK != null && gotStats && !fixed[GMPGlobals.DEPTH] && !fixed[GMPGlobals.LAT] && !fixed[GMPGlobals.LON])
    {
    	if (ellipsoidVTK.contains("%d"))
    		getEllipsoid().writeVTK(new File(String.format(ellipsoidVTK, orid)));
    	else 
    		getEllipsoid().writeVTK(new File(ellipsoidVTK));
    }



    return true;
  }

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // Query:  Did the algorithm converge?
  //
  // INPUT ARGS:  NONE
  // OUTPUT ARGS: NONE
  // RETURN:
  //   const boolean                          Convergence flag
  //	                                       true  = algorithm converged
  //	                                       false = algorithm failded to converge
  //
  // *****************************************************************************
  public boolean getConverged()
  {
    return converged;
  } // END Converged

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // Mutator functions.  These allow calling routines to
  // change the values of statistical parameters:
  // uncertainty matrix, data variance scale factor,
  // confidence level and Jordan-Sverdrup K parameter.
  //
  // *****************************************************************************
  public void setConfidenceEllipse()
  {
    K = 0;
    sigma_fresh = false;
    kappa_fresh = false;
  }

  public void setCoverageEllipse()
  {
    K = -1;
    sigma_fresh = false;
    kappa_fresh = false;
  }

  public boolean setJordanSverdrupEllipse(int k)
  {
    return setK(k);
  }

  public boolean setK(int k)
  {
    sigma_fresh = false;
    kappa_fresh = false;
    if (k < -1)
    {
      gotStats = false;
      return true;
    }
    K = k;
    return false;
  }

  public boolean setApriori_std_err(double v)
  {
    sigma_fresh = false;
    kappa_fresh = false;
    if (v < 0)
    {
      gotStats = false;
      return true;
    }
    apriori_variance = v*v;
    return false;
  }

  public boolean setConf(double c)
  {
    kappa_fresh = false;
    if (c <= 0.0 || c >= 1.0)
    {
      gotStats = false;
      return true;
    }
    conf = c;
    return false;
  }

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // Accessor functions.  These allow calling routines to
  // access the values of statistical parameters:
  // data variance scale factor,
  // confidence level and Jordan-Sverdrup K parameter.
  // See lsq_algorithm Section 6.2.
  //
  // *****************************************************************************

  public String getEllipseType()
  {
    String s = "";
    if (K == 0)
      s += "confidence (K=0)";
    else if (K == -1)
      s += "coverage (K=infinity)";
    else
      s += "mixed (K=" + K + ")";
    return s; //.str();
  }

  int getK()
  {
    return K;
  }

  double getApriori_std_err()
  {
    if (getApriori_variance() < 0.)
      return -1;
    return sqrt(getApriori_variance());
  }

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // Calculate Sigma, if necessary.
  //
  // *****************************************************************************
  public double Sigma()
  {
    // See svd_algorithm.pdf eq. 6.16
    if (!sigma_fresh)
    {
      if (K < 0)
        // K < 0 is interpreted as K = infinity. coverage uncertainty
        sigma = sqrt(getApriori_variance());
      else if (K + Nobs - M > 0)
	  // if K=0, confidence uncertainty, otherwise, K-weighted
        sigma = sqrt( (K * getApriori_variance() + sumSQRWeightedResiduals) /
            (K + Nobs - M));
      else
        sigma = 0.;

      sigma_fresh = true;
    }
    return sigma;
  }

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // Calculate Kappa, if necessary.  There are four different kappa values, one
  // for each number of free parameters (1 through 4).  If current values are
  // up to date, they are not recalculated.  If they are recalculated, then the
  // current uncertainty limits (hyper_ellipse, hypocentral_ellipsoid and
  // epicentral_ellipsoid) are automatically rescaled with recalculated values.
  //
  // *****************************************************************************

  public double Kappa(int m) throws Exception
  {
    // See lsq_algorithm.pdf eq. 6.15
    if (!kappa_fresh)
    {
      if (Nobs - M >= 0 && conf > 0. && conf < .9991)
        for (int i = 0; i < 4; i++)
          kappa[i] = Sigma() * sqrt(FStatistic.f_statistic(i+1, Nobs - M, K, conf));
      else
        Arrays.fill(kappa, 0.);

      kappa_fresh = true;
    }
    if (m >= 1 && m <= 4)
      return kappa[m - 1];
    return Double.POSITIVE_INFINITY;
  }

  public void setLocalMinimumFlag(boolean flag)
  {
    local_minima = flag;
  }

  public boolean getLocalMinima()
  {
    return local_minima;
  }

  public int getNFunc()
  {
    return nFunc;
  }

  public int getNIterations()
  {
    return niter;
  } //number of iterations to find  solution

  public double getSumSQRWeightedResiduals()
  {
    return sumSQRWeightedResiduals;
  }

  public int getM()
  {
    return M;
  }

  public boolean statsValid()
  {
    return gotStats;
  }

  public double getOrigLat()
  {
    return location.getLatDegrees();
  }

  public double getOrigLon()
  {
    return location.getLonDegrees();
  }

  public double getOrigDepth()
  {
    return location.getDepth();
  }

  public double getOrigTime()
  {
    return location.getTime();
  }

  public long getOrigOrid()
  {
    return event.getSource().getSourceId();
  }

  public long getOrigEvid()
  {
    return event.getSource().getEvid();
  }

  public long getOrigJdate()
  {
    return GMTFormat.getJDate(location.getTime());
  }

  public int getOrigNass()
  {
    return Nass;
  }

  public int getOrigNdef()
  {
    return Ndef;
  }

  public int getOrigNdp()
  {
    return -1;
  }

  public int getOrigGrn()
  {
    return FlinnEngdahlCodes.getGeoRegionIndex(getOrigLat(), getOrigLon());
  }

  public int getOrigSrn()
  {
    return FlinnEngdahlCodes.getSeismicRegionIndex(
        FlinnEngdahlCodes.getGeoRegionIndex(getOrigLat(), getOrigLon()));
  }

  public String getOrigEtype()
  {
    return "-";
  }

  public double getOrigDepdp()
  {
    return -999.;
  }

  public String getOrigDtype()
  {
    return dtype;
  }

  public double getOrigMb()
  {
    return -999.;
  }

  public int getOrigMbid()
  {
    return -1;
  }

  public double getOrigMs()
  {
    return -999.;
  }

  public int getOrigMsid()
  {
    return -1;
  }

  public double getOrigMl()
  {
    return -999.;
  }

  public int getOrigMlid()
  {
    return -1;
  }

  public String getOrigAlgorithm()
  {
    return algorithm;
  }

  public String getOrigAuthor()
  {
    return author;
  }

  public int getOrigCommid()
  {
    return -1;
  }

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // Accessor functions for elements of the Schema Origerr Table
  //
  // *****************************************************************************

  public long getOrigErrOrid()
  {
    return event.getSource().getSourceId();
  }

  public double getOrigErrSxx() throws Exception
  {
    return getOrigErrS(GMPGlobals.LAT, GMPGlobals.LAT);
  }

  public double getOrigErrSyy() throws Exception
  {
    return getOrigErrS(GMPGlobals.LON, GMPGlobals.LON);
  }

  public double getOrigErrSzz() throws Exception
  {
    return getOrigErrS(GMPGlobals.DEPTH, GMPGlobals.DEPTH);
  }

  public double getOrigErrStt() throws Exception
  {
    return getOrigErrS(GMPGlobals.TIME, GMPGlobals.TIME);
  }

  public double getOrigErrSxy() throws Exception
  {
    return getOrigErrS(GMPGlobals.LAT, GMPGlobals.LON);
  }

  public double getOrigErrSxz() throws Exception
  {
    return getOrigErrS(GMPGlobals.LAT, GMPGlobals.DEPTH);
  }

  public double getOrigErrSyz() throws Exception
  {
    return getOrigErrS(GMPGlobals.LON, GMPGlobals.DEPTH);
  }

  public double getOrigErrStx() throws Exception
  {
    return getOrigErrS(GMPGlobals.LAT, GMPGlobals.TIME);
  }

  public double getOrigErrSty() throws Exception
  {
    return getOrigErrS(GMPGlobals.LON, GMPGlobals.TIME);
  }

  public double getOrigErrStz()
      throws Exception
  {
    return getOrigErrS(GMPGlobals.DEPTH, GMPGlobals.TIME);
  }

  public double getOrigErrS(int comp1, int comp2) throws Exception
  {
    if (!gotStats || infiniteUncertainty || fixed[comp1] || fixed[comp2])
      return Origerr.SXX_NA;
    return getCovariance()[comp1][comp2];

  }

  public double getOrigErrSdepth() throws Exception
  {
    if (!gotStats || infiniteUncertainty || event.isFixed(GMPGlobals.DEPTH))
      return Origerr.SDEPTH_NA;
    else
      return sqrt(getCovariance()[GMPGlobals.DEPTH][GMPGlobals.DEPTH]) * Kappa(1);
  }

  public double getOrigErrStime() throws Exception
  {
    if (!gotStats || infiniteUncertainty || event.isFixed(GMPGlobals.TIME))
      return Origerr.STIME_NA;
    else
      return sqrt(getCovariance()[GMPGlobals.TIME][GMPGlobals.TIME]) * Kappa(1);
  }

  public double getOrigErrSmajax()
      throws Exception
  {
    if (!gotStats || infiniteUncertainty || event.isFixed(GMPGlobals.LAT) || event.isFixed(GMPGlobals.LON))
      return Origerr.SMAJAX_NA;
    else
      return getEllipse().getMajaxLength();
  }

  public double getOrigErrSminax()throws Exception
  {
    if (!gotStats || infiniteUncertainty || event.isFixed(GMPGlobals.LAT) || event.isFixed(GMPGlobals.LON))
      return Origerr.SMINAX_NA;
    else
      return getEllipse().getMinaxLength();
  }

  public double getOrigErrStrike()throws Exception
  {
    if (!gotStats || infiniteUncertainty || event.isFixed(GMPGlobals.LAT) || event.isFixed(GMPGlobals.LON))
      return Origerr.STRIKE_NA;
    else
      return toDegrees(getEllipse().getMajaxTrend());
  }

  public double getOrigErrConf()
  {
    return conf;
  }

  public int getOrigErrCommid()
  {
    return -1;
  }

  public double getOrigErrSdobs()
  {
    return sdobs;
  }

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // Accessor functions for a couple of values that are not in the Database
  // but ought to be.
  //
  // *****************************************************************************

  public double getEllipseArea()throws Exception
  {
    if (event.isFixed(GMPGlobals.LAT) || event.isFixed(GMPGlobals.LON))
      return 0.;
    if (!gotStats)
      return -1.;
    else if (infiniteUncertainty)
      return Globals.NA_VALUE;
    else
      return getEllipse().getArea();
  }

  public double getMajaxTrend() throws Exception
  {
    return toDegrees(getEllipsoid().getMajaxTrend());
  }

  public double getMajaxPlunge() throws Exception
  {
    return toDegrees(getEllipsoid().getMajaxPlunge());
  }

  public double getMajaxLength() throws Exception
  {
    return getEllipsoid().getMajaxLength();
  }

  public double getIntaxTrend() throws Exception
  {
    return toDegrees(getEllipsoid().getIntaxTrend());
  }

  public double getIntaxPlunge() throws Exception
  {
    return toDegrees(getEllipsoid().getIntaxPlunge());
  }

  public double getIntaxLength() throws Exception
  {
    return getEllipsoid().getIntaxLength();//*location.getEarthRadius();
  }

  public double getMinaxTrend() throws Exception
  {
    return toDegrees(getEllipsoid().getMinaxTrend());
  }

  public double getMinaxPlunge() throws Exception
  {
    return toDegrees( getEllipsoid().getMinaxPlunge());
  }

  public double getMinaxLength() throws Exception
  {
    return getEllipsoid().getMinaxLength();//*location.getEarthRadius();
  }

  public double getSLat() // independent uncertainty in latitude in km
      throws Exception
  {
    if (!gotStats)
      return -1.;
    else
      return sqrt(getCovariance()[GMPGlobals.LAT][GMPGlobals.LAT]) * Kappa(1);
  }

  public double getSLon() // independent uncertainty in longitude in km
      throws Exception
  {
    if (!gotStats)
      return -1.;
    else
      return sqrt(getCovariance()[GMPGlobals.LON][GMPGlobals.LON]) * Kappa(1);
  }

  public double getSigma()
  {
    if (!gotStats)
      return -1.;
    else
      return Sigma();
  }

  public double getKappa(int M_) throws Exception
  {
    if (!gotStats)
      return -1.;
    else
      return Kappa(M_);
  }

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // return the final, calculated event location in a 4-element VectorMod with
  // elements LAT, LON, DEPTH and GMPGlobals.TIME.  Units are radians, radians, km and
  // seconds since 1970.
  //
  // INPUT ARGS:  NONE
  // OUTPUT ARGS: NONE
  // RETURN:      4-element VectorMod
  //
  //
  // *****************************************************************************
  public Location getLocation()
  {
    return location;

  } // END getFixLon

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // return the final, calculated event epicenter in a 4-element VectorMod with
  // elements LAT, LON, DEPTH(=0) and GMPGlobals.TIME.  Units are radians, radians, km and
  // seconds since 1970.
  //
  // INPUT ARGS:  NONE
  // OUTPUT ARGS: NONE
  // RETURN:      4-element VectorMod
  //
  //
  // *****************************************************************************
  public Location epicenter()
  {
    Location epicenter = location.clone();
    epicenter.setDepth(0.);
    return epicenter;
  } // END getFixLon

  public Ellipse getEllipse() throws Exception
  {
    return hyper_ellipse.getProjectedEllipse(pow(Kappa(2),2));
  }

  public Ellipsoid getEllipsoid() throws Exception
  {
    return hyper_ellipse.getProjectedEllipsoid(pow(Kappa(3),2));
  }

  public HyperEllipse getHyperEllipse() throws Exception
  {
      if (hyper_ellipse == null) {
	    hyper_ellipse = new HyperEllipse(location, fixed, Nobs, uncertainty, K, getApriori_variance(), 
		    sumSQRWeightedResiduals, conf, sdobs);
      }
    // setScaleFactor initializes the hyper_ellipse, if necessary.
    //hyper_ellipse.setScaleFactor(pow(Kappa(4),2));
    return hyper_ellipse;
  }

  public double[][] getCovariance() throws Exception
  {
    return hyper_ellipse.getCovariance();
  }

  public double[] getCovarianceFlat() throws Exception
  {
    return new double[] {
        getOrigErrSxx(),
        getOrigErrSyy(),
        getOrigErrSzz(),
        getOrigErrStt(),
        getOrigErrSxy(),
        getOrigErrSxz(),
        getOrigErrSyz(),
        getOrigErrStx(),
        getOrigErrSty(),
        getOrigErrStz()
    };
  }

  public boolean[] getFixed()
  {
    return fixed;
  }

  public boolean getFixLon()
  {
    return fixed[GMPGlobals.LON];
  }

  public boolean getFixLat()
  {
    return fixed[GMPGlobals.LAT];
  }

  public boolean getFixDepth()
  {
    return fixed[GMPGlobals.DEPTH];
  }

  public boolean getFixOT()
  {
    return fixed[GMPGlobals.TIME];
  }

  public double getRMSWeightedResiduals()
  {
    if (Nobs > 0)
      return sqrt(getSumSQRWeightedResiduals() / Nobs);
    return -1.;
  }

  public Azgap getAzgapRow() throws Exception
  {
    return event.azimuthalGap();
  }

  public int getNobs()
  {
    return Nobs;
  }

  public int getNdeleted()
  {
    return Ndeleted;
  }

  public Source getSourceRow()
  {
    return new Source(
        event.getSource().getSourceId(),
        event.getSource().getEvid(), 
        location.getLatDegrees(),
        location.getLonDegrees(),
        location.getDepth(),
        location.getTime(),
        -1.,
        event.getSource().getNass(), 
        -1L, 
        author
        );
  }

  public Origin getOriginRow()
  {
    double lat = Math.round(location.getLatDegrees()*1e6)/1e6;
    double lon = Math.round(location.getLonDegrees()*1e6)/1e6;
    int grn = FlinnEngdahlCodes.getGeoRegionIndex(lat, lon);
    int srn = FlinnEngdahlCodes.getSeismicRegionIndex(grn);

    return new Origin(
        lat,
        lon,
        Math.round(location.getDepth()*1e4)/1e4,
        Math.round(location.getTime()*1e5)/1e5,
        event.getSource().getSourceId(),
        event.getSource().getEvid(),
        GMTFormat.getJDate(location.getTime()),
        Nass,
        Ndef,
        Origin.NDP_NA,
        grn,
        srn,
        Origin.ETYPE_NA,
        Origin.DEPDP_NA,
        dtype,
        Origin.MB_NA,
        Origin.MBID_NA,
        Origin.MS_NA,
        Origin.MSID_NA,
        Origin.ML_NA,
        Origin.MLID_NA,
        algorithm.length() > 15 ? algorithm.substring(0, 15) : algorithm,
        author.length() > 15 ? author.substring(0, 15) : author,
        Origin.COMMID_NA
        );

  }

  public Origerr getOrigerrRow() throws Exception
  {
    return new Origerr(
        event.getSource().getSourceId(),
        getOrigErrSxx(),
        getOrigErrSyy(),
        getOrigErrSzz(),
        getOrigErrStt(),
        getOrigErrSxy(),
        getOrigErrSxz(),
        getOrigErrSyz(),
        getOrigErrStx(),
        getOrigErrSty(),
        getOrigErrStz(),
        getOrigErrSdobs(),
        getOrigErrSmajax(),
        getOrigErrSminax(),
        getOrigErrStrike(),
        getOrigErrSdepth(),
        getOrigErrStime(),
        getOrigErrConf(),
        -1
        );

  }

  @Override
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    try
    {
      buf.append(String.format("Final location for evid: %d   Orid: %d%n%n", getOrigEvid(), getOrigOrid()));

      //			if (!getEllipse().isValid())
      //				buf.append("Ill-posed problem.  Uncertainty ellipse is undefined.").append(Globals.NL);
      //			else 
      if (location != null)
      {

        buf.append(String.format("  latitude longitude     depth     origin_time           origin_date_gmt       origin_jdate%n"));
        buf.append(String.format("%10.4f %9.4f %9.3f %15.3f %25s %18d%n%n",
            getOrigLat(), getOrigLon(), getOrigDepth(), getOrigTime(), 
            GMTFormat.GMT_MS.format(GMTFormat.getDate(getOrigTime())),
            GMTFormat.getJDate(getOrigTime())));
        
        buf.append(String.format("  geographic region: %s    seismic region %s%n%n", 
        	FlinnEngdahlCodes.getGeoRegionName(getOrigLat(), getOrigLon()), 
        	FlinnEngdahlCodes.getSeismicRegionName(getOrigLat(), getOrigLon())));

        buf.append(String.format("  converged  loc_min   Nit Nfunc     M  Nobs  Ndel  Nass  Ndef     sdobs    rms_wr%n"));
        buf.append(String.format("%11b %8b %5d %5d %5d %5d %5d %5d %5d %9.4f %9.4f%n%n",
            getConverged(), getLocalMinima(), getNIterations(), getNFunc(), getM(), getNobs(), getNdeleted(),
            getOrigNass(), getOrigNdef(), getOrigErrSdobs(), getRMSWeightedResiduals()));


        Azgap azgap = event.azimuthalGap();
        buf.append(String.format("    az_gap  az_gap_2 station  Nsta   N30  N250%n"));
        buf.append(String.format("%10.4f %9.4f %7s %5d %5d %5d%n%n",
        		azgap.getAzgap1(), 
        		azgap.getAzgap2(), 
        		azgap.getSta(), 
        		azgap.getNsta(), azgap.getNsta30(), azgap.getNsta250()));

        if (gotStats)
        {
          buf.append(String.format("      conf        type     K   apriori     sigma   kappa_1   kappa_2   kappa_3   kappa_4%n"));
          buf.append(String.format("%10.4f %11s %5s %9.4f %9.4f %9.4f %9.4f %9.4f %9.4f%n%n",
              getOrigErrConf(), 
              (K==0 ? "confidence" : (K==-1 ? "coverage" : "mixed")), 
              (K==0 ? "0" : (K==-1 ? "Inf" : Integer.toString(K))),
              getApriori_std_err(), getSigma(), getKappa(1), getKappa(2), getKappa(3), getKappa(4)
              ));

          if (!fixed[GMPGlobals.LAT] && !fixed[GMPGlobals.LON] && !fixed[GMPGlobals.DEPTH])
            try {
              {
                buf.append(String.format("3D Hypocentral uncertainty ellipsoid:%n%n"));
                
                
//                buf.append(String.format("   Equation: %s%n%n",
//                		Arrays.toString(getEllipsoid().getCoeff())
//                		.replace("[", "").replace("]", "").replace(",", "")));
                		

                buf.append(String.format("              length      trend     plunge%n"));
                buf.append(String.format("   major: %10.4f %10.4f %10.4f%n", 
                    getMajaxLength(), getMajaxTrend(), getMajaxPlunge()
                    ));

                buf.append(String.format("   minor: %10.4f %10.4f %10.4f%n", 
                    getMinaxLength(), getMinaxTrend(), getMinaxPlunge()
                    ));

                buf.append(String.format("   inter: %10.4f %10.4f %10.4f%n%n", 
                    getIntaxLength(), getIntaxTrend(), getIntaxPlunge()
                    ));
                
              }
            } catch (Exception e) {
              buf.append("\n"+e.getMessage()+"\n");
            }

          if (!fixed[GMPGlobals.LAT] && !fixed[GMPGlobals.LON])
          {
            try {
              buf.append(String.format("2D Epicentral uncertainty ellipse:%n%n"));
              buf.append(String.format("    smajax    sminax     trend      area%n"));
              buf.append(String.format("%10.4f %9.4f %9.4f %9.2f%n%n",
                  getOrigErrSmajax(), getOrigErrSminax(), getOrigErrStrike(), getEllipseArea()
                  ));
            } catch (Exception e) {
              buf.append("\n"+e.getMessage()+"\n\n\n");
            }
          }

          try {
            buf.append(String.format("1D linear uncertainties:%n%n"));
            buf.append(String.format("  depth_se   time_se%n"));
            buf.append(String.format("%10.4f %9.4f%n%n", 
                getOrigErrSdepth(), getOrigErrStime()
                ));
          } catch (Exception e) {
            buf.append("\n"+e.getMessage()+"\n\n");
          }
        }
      }
    } 
    catch (Exception e)
    {
      e.printStackTrace();
      buf.append(String.format("%nERROR: %s%n%n", e.getMessage()));
    }

    return buf.toString();
  }


  /**
   * @return the event
   */
  public Event getEvent()
  {
    return event;
  }

public double getApriori_variance() {
    return apriori_variance;
}

}
