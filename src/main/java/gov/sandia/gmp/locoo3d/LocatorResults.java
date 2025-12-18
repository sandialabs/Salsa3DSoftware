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

import static java.lang.Math.sqrt;

import java.io.File;

import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.baseobjects.flinnengdahl.FlinnEngdahlCodes;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.hyperellipse.Ellipse;
import gov.sandia.gmp.baseobjects.hyperellipse.Ellipsoid;
import gov.sandia.gmp.baseobjects.hyperellipse.HyperEllipse;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gnem.dbtabledefs.gmp.Source;
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

    /**
     * number of arrivals associated with the event
     */
    private int Nass; 
    
    /**
     * number of time-defining phases.
     */
    private int Ndef; 
    
    /**
     * number of defining observations (those used to locate event)
     */
    private int Nobs; 
    
    /**
     * 
     */
    private int Ndeleted; 

    //number of free parameters in the inversion
    private int M; 

    //number of time sum square weighted residuals were calculated
    // in order to compute solution.
    private int nFunc; 

    // Whether the i'th component of the solution was fixed.
    private boolean[] fixed; 

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

    // 5x4 matrix containing the principal axes of the 4D uncertainty
    // hyper ellipse.  Each column contains the components of one of the
    // principal axes.  The first 4 elements of each column are the
    // direction cosines with respect to the lat, lon, depth and time
    // axes in that order.  The 5th element must be multiplied by kappa(4)
    // to convert it to the length of the semi axis in units of km*sec.
    private HyperEllipse hyper_ellipse;

    //possible existance of local minima.
    private boolean local_minima; 

    // if property ellipsoidVTK is set, and a 3D error ellipsoid can be calculated,
    // a vtk file is generated with an image of the ellipsoid.
    private String ellipsoidVTK;


    public LocatorResults(Event event, 
	    boolean converged, 
	    long evid, long orid, 
	    Location location,
	    double[][] covarianceMatrix, 
	    double sumSQRWeightedResiduals, 
	    double rmsTTResiduals, 
	    double sdobs, 
	    int nFunc, 
	    int nass,
	    int ndef, 
	    int nobs, 
	    int ndel, 
	    int M, 
	    boolean[] fixed, 
	    String dtype, 
	    int niterations,
	    boolean sLocalMinima) throws Exception {

	this.converged = converged;
	this.location = location;
	this.sumSQRWeightedResiduals = sumSQRWeightedResiduals;
	this.sdobs = sdobs;
	this.nFunc = nFunc;
	this.Nass = nass;
	this.Ndef = ndef;
	this.Nobs = nobs;
	this.Ndeleted = ndel;
	this.fixed = fixed.clone();
	this.dtype = dtype;
	this.M = M;
	this.niter = niterations;

	this.event = event;

	author = event.getEventParameters().getAuthor();

	hyper_ellipse = new HyperEllipse(location, covarianceMatrix, this.M, this.Nobs, 
		this.sumSQRWeightedResiduals, 
		event.getEventParameters().getUncertaintyK(), 
		event.getEventParameters().getAprioriVariance(), 
		event.getEventParameters().getConfidenceLevel());

	ellipsoidVTK = event.getEventParameters().getEllipsoidVTK();
	
	if (ellipsoidVTK != null && !fixed[GMPGlobals.DEPTH] && !fixed[GMPGlobals.LAT] && !fixed[GMPGlobals.LON])
	{
	    // TODO: specifying location may not be right
	    if (ellipsoidVTK.contains("%d"))
		getEllipsoid().writeVTK(new File(String.format(ellipsoidVTK, orid)), location);
	    else 
		getEllipsoid().writeVTK(new File(ellipsoidVTK), location);
	}
    }

     public int getNFunc()
    {
	return nFunc;
    }

    public int getNIterations()
    {
	return niter;
    } 

    public double getSumSQRWeightedResiduals()
    {
	return sumSQRWeightedResiduals;
    }

    public int getM()
    {
	return M;
    }

//    public double getOrigLat()
//    {
//	return location.getLatDegrees();
//    }
//
//    public double getOrigLon()
//    {
//	return location.getLonDegrees();
//    }
//
//    public double getOrigDepth()
//    {
//	return location.getDepth();
//    }
//
//    public double getOrigTime()
//    {
//	return location.getTime();
//    }
//
//    public long getOrigOrid()
//    {
//	return event.getSource().getSourceId();
//    }
//
//    public long getOrigEvid()
//    {
//	return event.getSource().getEvid();
//    }
//
//    public long getOrigJdate()
//    {
//	return GMTFormat.getJDate(location.getTime());
//    }
//
//    public int getOrigNass()
//    {
//	return Nass;
//    }
//
//    public int getOrigNdef()
//    {
//	return Ndef;
//    }
//
//    public int getOrigNdp()
//    {
//	return -1;
//    }
//
//    public int getOrigGrn()
//    {
//	return FlinnEngdahlCodes.getGeoRegionIndex(getOrigLat(), getOrigLon());
//    }
//
//    public int getOrigSrn()
//    {
//	return FlinnEngdahlCodes.getSeismicRegionIndex(
//		FlinnEngdahlCodes.getGeoRegionIndex(getOrigLat(), getOrigLon()));
//    }
//
//    public String getOrigEtype()
//    {
//	return "-";
//    }
//
//    public double getOrigDepdp()
//    {
//	return -999.;
//    }
//
//    public String getOrigDtype()
//    {
//	return dtype;
//    }
//
//    public double getOrigMb()
//    {
//	return -999.;
//    }
//
//    public int getOrigMbid()
//    {
//	return -1;
//    }
//
//    public double getOrigMs()
//    {
//	return -999.;
//    }
//
//    public int getOrigMsid()
//    {
//	return -1;
//    }
//
//    public double getOrigMl()
//    {
//	return -999.;
//    }
//
//    public int getOrigMlid()
//    {
//	return -1;
//    }
//
//    public String getOrigAlgorithm()
//    {
//	return algorithm;
//    }
//
//    public String getOrigAuthor()
//    {
//	return author;
//    }
//
//    public int getOrigCommid()
//    {
//	return -1;
//    }

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
    public Location epicenter() throws Exception
    {
	Location epicenter = (Location) location.clone();
	epicenter.setDepth(0.);
	return epicenter;
    } // END getFixLon

    public Ellipse getEllipse() throws Exception
    {
	return hyper_ellipse.getEllipse();
    }

    public Ellipsoid getEllipsoid() throws Exception
    {
	return hyper_ellipse.getEllipsoid();
    }

    public HyperEllipse getHyperEllipse() throws Exception
    {
	return hyper_ellipse;
    }

    public double[][] getCovariance() throws Exception
    {
	return hyper_ellipse.getCovariance();
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

    @Override
    public String toString()
    {
    	StringBuffer buf = new StringBuffer();
    	try
    	{
    		buf.append(String.format("Final location for evid: %d   Orid: %d%n%n", 
    				event.getSource().getEvid(), event.getSource().getSourceId()));

    		//			if (!getEllipse().isValid())
    		//				buf.append("Ill-posed problem.  Uncertainty ellipse is undefined.").append(Globals.NL);
    		//			else 
    		if (location != null)
    		{

    			buf.append(String.format("  latitude longitude     depth     origin_time           origin_date_gmt       origin_jdate%n"));
    			buf.append(String.format("%10.4f %9.4f %9.3f %15.3f %25s %18d%n%n",
    					location.getLatDegrees(), location.getLonDegrees(), location.getDepth(), location.getTime(), 
    					GMTFormat.GMT_MS.format(GMTFormat.getDate(location.getTime())),
    					GMTFormat.getJDate(location.getTime())));

    			buf.append(String.format("  geographic region: %s (%d)   seismic region %s (%d)%n%n", 
						FlinnEngdahlCodes.getGeoRegionName(location.getLatDegrees(), location.getLonDegrees()), 
						FlinnEngdahlCodes.getGeoRegionIndex(location.getLatDegrees(), location.getLonDegrees()), 
						FlinnEngdahlCodes.getSeismicRegionName(location.getLatDegrees(), location.getLonDegrees()),
						FlinnEngdahlCodes.getSeismicRegionIndex(location.getLatDegrees(), location.getLonDegrees())));

    			buf.append(String.format("  converged  loc_min   Nit Nfunc     M  Nobs  Ndel  Nass  Ndef     sdobs    rms_wr%n"));
    			buf.append(String.format("%11b %8b %5d %5d %5d %5d %5d %5d %5d %9.4f %9.4f%n%n",
    					converged, local_minima, getNIterations(), getNFunc(), getM(), getNobs(), getNdeleted(),
    					Nass, Ndef, sdobs, getRMSWeightedResiduals()));


    			Azgap azgap = event.azimuthalGap();
    			buf.append(String.format("    az_gap  az_gap_2 station  Nsta   N30  N250%n"));
    			buf.append(String.format("%10.4f %9.4f %7s %5d %5d %5d%n%n",
    					azgap.getAzgap1(), 
    					azgap.getAzgap2(), 
    					azgap.getSta(), 
    					azgap.getNsta(), azgap.getNsta30(), azgap.getNsta250()));

    			buf.append(String.format("      conf        type     K   apriori     sigma   kappa_1   kappa_2   kappa_3   kappa_4%n"));
    			buf.append(String.format("%10.4f %11s %5s %9.4f %9.4f %9.4f %9.4f %9.4f %9.4f%n%n",
    					hyper_ellipse.getConfidence(), 
    					(hyper_ellipse.getK()==0 ? "confidence" : (hyper_ellipse.getK()==-1 ? "coverage" : "mixed")), 
    					(hyper_ellipse.getK()==0 ? "0" : (hyper_ellipse.getK()==-1 ? "Inf" : Integer.toString(hyper_ellipse.getK()))),
    					hyper_ellipse.getAprioriStandardError(), hyper_ellipse.getSigmaSqr(), 
    					hyper_ellipse.getKappa(1), hyper_ellipse.getKappa(2), hyper_ellipse.getKappa(3), hyper_ellipse.getKappa(4)
    					));

    			if (!fixed[GMPGlobals.LAT] && !fixed[GMPGlobals.LON] && !fixed[GMPGlobals.DEPTH])
    				try {
    						buf.append(String.format("3D Hypocentral uncertainty ellipsoid:%n%n"));

    						Ellipsoid ellipsoid = hyper_ellipse.getEllipsoid();

    						buf.append(String.format("              length      trend     plunge%n"));
    						buf.append(String.format("   major: %10.4f %10.4f %10.4f%n", 
    								ellipsoid.getMajaxLength(), ellipsoid.getMajaxTrend(), ellipsoid.getMajaxPlunge()
    								));

    						buf.append(String.format("   minor: %10.4f %10.4f %10.4f%n", 
    								ellipsoid.getMinaxLength(), ellipsoid.getMinaxTrend(), ellipsoid.getMinaxPlunge()
    								));

    						buf.append(String.format("   inter: %10.4f %10.4f %10.4f%n%n", 
    								ellipsoid.getIntaxLength(), ellipsoid.getIntaxTrend(), ellipsoid.getIntaxPlunge()
    								));
    						
  				} catch (Exception e) {
    					if (e.getMessage().startsWith("ERROR in Simplex.amoeba()")) 
    						buf.append("Calculation of ellipsoid failed.\n\n");
    					else
    						buf.append(e.getMessage()+"\n\n");
    				}

    			if (!fixed[GMPGlobals.LAT] && !fixed[GMPGlobals.LON])
    			{
    				try {
    					Ellipse ellipse = hyper_ellipse.getEllipse();
    					buf.append(String.format("2D Epicentral uncertainty ellipse:%n%n"));
    					buf.append(String.format("    smajax    sminax     trend      area%n"));
    					buf.append(String.format("%10.4f %9.4f %9.4f %9.2f%n%n",
    							ellipse.getMajaxLength(), ellipse.getMinaxLength(), 
    							ellipse.getMajaxTrend(), ellipse.getArea()
    							));
    				} catch (Exception e) {
    					buf.append("\n"+e.getMessage()+"\n\n\n");
    				}
    			}

    			try {
    				buf.append(String.format("1D linear uncertainties:%n%n"));
    				buf.append(String.format("  depth_se   time_se%n"));
    				buf.append(String.format("%10.4f %9.4f%n%n", 
    						getHyperEllipse().getSdepth(), getHyperEllipse().getStime()
    						));
    			} catch (Exception e) {
    				buf.append("\n"+e.getMessage()+"\n\n");
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

}
