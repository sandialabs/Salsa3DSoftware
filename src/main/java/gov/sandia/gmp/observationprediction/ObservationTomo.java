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
package gov.sandia.gmp.observationprediction;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SimpleTimeZone;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.DBTableTypes;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.globals.WaveType;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.bender.UndersideReflectedPhaseBouncePoint;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerDouble;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.polygon.Polygon3D;

/**
 *
 * <p>Title: Observation</p>
 *
 * <p>Description: This class represents the input and output data associated
 * with a single source-receiver pair and the ray path in between.
 * </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ObservationTomo extends PredictionRequest
       implements Serializable, Cloneable
{
  /**
   * Standard observation status settings.
   * 
   * @author jrhipp
   *
   */
  public enum ObservationStatus
  {
    NO_STATUS
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return false;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return false;
    	}
    },

    // observation status is set to this if used in a tomography calculation

    USE_TOMOGRAPHY
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return false;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return false;
    	}
    },


    // observation status is set to one of these if removed before tomography/
    // prediction calculation begins

    REMOVE_INVALID_TRAVEL_TIME
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return false;
    	}
    },

    REMOVE_NON_DEFINING
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return false;
    	}
    },

    REMOVE_LOMO
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return false;
    	}
    },


    REMOVE_LEAVE_OUT_EVENT
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return false;
    	}
    },

    REMOVE_LEAVE_OUT_SITE
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return false;
    	}
    },

    REMOVE_POOR_EVENT_SUPPORT
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return false;
    	}
    },

    REMOVE_POOR_SITE_SUPPORT
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return false;
    	}
    },


    // observation status is set to one these if considered at each tomography
    // iteration but left out after prediction because of one of the following

    REMOVE_ITERATION_ERRONEOUS_PREDICTION
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return true;
    	}
    },

    REMOVE_ITERATION_INVALID_PREDICTION
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return true;
    	}
    },

    REMOVE_ITERATION_BAD_RESIDUAL
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return true;
    	}
    },

    REMOVE_ITERATION_BAD_NORM_RESIDUAL
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return true;
    	}
    },

    REMOVE_ITERATION_INACTIVE
    {
    	@Override
    	public boolean isRemoved()
    	{
    		return true;
    	}
    	
    	@Override
    	public boolean isRemovedIteration()
    	{
    		return true;
    	}
    };

    public boolean isValidForTomography()
    {
      return (((isPoorlyRepresented() && aUsePoorlyRepresented) ||
              !isPoorlyRepresented()) && !isPermanentlyRemoved()) ? true : false;
    }
    
    /**
     * Returns true if this status is any "removed" state except
     * "REMOVE_ITERATION_*"
     * 
     * @return True if this status is any "removed" state except
     * 				 "REMOVE_ITERATION_*"
     */
    public boolean isPermanentlyRemoved()
    {
    	return (isRemoved() && !isRemovedIteration()) ? true : false;
    }

    /**
     * Returns true if this status is a poorly represented event or site.
     * 
     * @return True if this status is a poorly represented event or site.
     */
    public boolean isPoorlyRepresented()
    {
    	return ((this == REMOVE_POOR_EVENT_SUPPORT) ||
    					(this == REMOVE_POOR_SITE_SUPPORT)) ? true : false;
    }
    
    public abstract boolean isRemoved();
    public abstract boolean isRemovedIteration();
  }

  /**
   * Set to true if poorly represented (event or site) observations are to be
   * used in tomography
   */
  private static boolean             aUsePoorlyRepresented = false;

  private long predictionId;

  /**
   * Origin time, in seconds
   */
  protected double originTime;

  /**
   * The observed arrival time in seconds.
   */
  private double arrivalTime;

  /**
   * The tomography event term
   */
  private double eventTerm=0;

  /**
   * True if observation event term was calculated.
   */
  private boolean evalEventTerm = false;

  /**
   * True if observation site term was calculated.
   */
  private boolean evalSiteTerm  = false;

  /**
   * The tomography site term
   */
  private double siteTerm=0;

  /**
   * The uncertainty of the observed travel time.  Obtained from arrival.deltim.
   * Units of seconds.
   */
  private double deltim;

  /**
   * observed slowness in sec/radian
   */
  private double observedSlowness = -999.;


  /**
   * slowness uncertainty in sec/radian
   */
  private double delslo = -999.;

  /**
   * observed azimuth in radians.
   */
  private double observedAzimuth = -999.;

  /**
   * azimuth uncertainty in radians
   */
  private double delaz = -999.;

  /**
   * The GT level in km of this observation.  Comes from a gt_epi.gtlevel_km.
   */
  private double gtlevel;

  /**
   * The unique sources will be uniquely, consecutively indexed starting from
   * zero.  Origin uniqueness is based on orid.  Indexing is managed in
   * ObservationList.
   */
  private int originIndex;

  /**
   * The unique receivers will be uniquely, consecutively indexed starting from
   * zero.  Uniqueness is based on the string stored in uniqueSta.
   * Indexing is managed in ObservationList.
   */
  private int siteIndex;

  /**
   * The unique receivers refsta index consecutively indexed from zero.
   * Uniqueness is based on the refsta name of the receiver. Indexing is
   * managed in ObservationList.
   */
  private int refstaIndex;

  /**
   * Observation objects each have a unique, consecutive index starting from
   * zero.  Indexing is managed in ObservationList.
   */
  private int index;

  /**
   * Whether or not this observation is defining.  If there is an
   * assoc table, this would correspond to assoc.timedef.
   */
  private boolean timedef;

  /**
   * Travel-Time Uncertainty (=sqrt((GT/avgVel)^2 + deltim^2)) 
   */
  private double ttUncertainty;

  /**
   * The current iteration residual travel time.
   */
  private double ttCurrResidual = 0.0;

  /**
   * The last iteration residual travel time. 
   */
  private double ttLastResidual = -1.0;

  /**
   * The first iteration residual travel time. 
   */
  private double ttFirstResidual = -1.0;

  /**
   * The inactive travel time. Used by tomography as a baseline
   * when the constant ray-path option is selected.
   */
  private double ttInActive = 0.0;
  
  /**
   * Used to mark an observation
   */
  private boolean mark = false;

  private double crustalThickness = -1.0;

  private boolean sourceInPolygon   = false;
  private boolean receiverInPolygon = false;

  static protected String auth = System.getProperty("user.name");
  static protected Date lddate = new Date();  

  /**
   * Default observation status.
   */
  private ObservationStatus status = ObservationStatus.NO_STATUS;
	
	/**
	 * Used by predictors that understand the behavior of reflective phases that
	 * are defined with under side reflective bounce points. If the under side
	 * reflected phase bounce point object is requested but not defined it is first created and
	 * then returned.
	 */
	private UndersideReflectedPhaseBouncePoint undersideReflectedPhaseBouncePoint = null;

	private int    skipRayTrace = -1;
	private int    skipBouncePointOptimization = -1;

	private Map<WaveType, HashMapIntegerDouble> rayWeights;

	private double rayWeightMovement   = -1.0;
	private double bouncePointMovement = -1.0;

	private Prediction prediction;

	public void resetRayWeightMovement()
	{
		skipRayTrace = -1;
		rayWeightMovement = -1.0;
	}

	public void resetBouncePointMovement()
	{
		skipBouncePointOptimization = -1;
		bouncePointMovement = -1.0;
	}

	public void setRayWeightMovement(double rwm)
	{
		rayWeightMovement = rwm;
	}

	public void setBouncePointMovement(double bpm)
	{
		bouncePointMovement = bpm;
	}

	public boolean isRayWeightMovementDefined()
	{
		return (rayWeightMovement != -1.0);
	}

	public double getRayWeightMovement()
	{
		return rayWeightMovement;
	}

	public boolean isBouncePointMovementDefined()
	{
		return (bouncePointMovement != -1.0);
	}

	public double getBouncePointMovement()
	{
		return bouncePointMovement;
	}

	public void setPredictorResults(PredictorResult pr)
	{
	    this.prediction = pr.getPrediction();
	    rayWeights = prediction.getRayWeights();
	}

	/**
	 * Get the tomography weights for this observation, if they were computed.
	 * RayWeights is a map from WaveType (either WaveType.P of WaveType.S) to 
	 * a Tuple<int[], double[]>.  The ints are point indexes in a GeoTessModel
	 * that includes all the points in the model that were touched by the ray.
	 * The doubles are the weights (sum of interpolation coefficients * interval 
	 * length) that correspond to the point indexes.  The int[] and the double[]
	 * within a single Tuple are guaranteed to have the same length.
	 * <p>For phases that comprise of only one wavetype (P, pP, PKP, S,sS, ScS, etc), 
	 * the map will only have one key (WaveType.P or WaveType.S) and the entries of 
	 * the double[] of the corresponding Tuple will sum to the length of the raypath.
	 * <p>For phases comprised of segments of two different wavetypes (PS, sP, PcS,
	 * SKS, etc), there will be two keys in the map (WaveType.P and WaveType.S) and 
	 * the doubles of the corresponding Tuples will sum to the length
	 * of the segments of the ray that traveled through the different parts of the ray.
	 * The sum of all the doubles[] in both map.values() will still sum to the length
	 * of the overall ray. 
	 * @return rayWeights, which will be empty if the tomography weights were not computed. 
	 * computed.
	 */
	public Map<WaveType, HashMapIntegerDouble> getRayWeights()
	{
	    return rayWeights;
	}

	public boolean isSkipRayTrace()
	{
		return (skipRayTrace != -1);
	}
	
	public boolean isSkipBouncePointOptimization()
	{
		return (skipBouncePointOptimization != -1);
	}

	public void incrementSkipRayTrace(int maxCount)
	{
		skipRayTrace++;
		if (skipRayTrace >= maxCount)
			skipRayTrace = -1;
	}

	public void incrementSkipBouncePointOptimization(int maxCount)
	{
		skipBouncePointOptimization++;
		if (skipBouncePointOptimization >= maxCount)
			skipBouncePointOptimization = -1;
	}

	public void resetRaySkipCounts()
	{
		skipRayTrace = skipBouncePointOptimization = -1;
	}

  /**
   * 
   * @param sourceId
   * @param observationId
   * @param predictionId
   * @param receiverId
   * @param sta
   * @param uniqueSta
   * @param phase
   * @param source
   * @param receiver
   * @param originTime
   * @param arrivalTime
   * @param deltim
   * @param gtlevel
   * @param defining
   * @param index
 * @throws Exception 
   */
  public ObservationTomo(
      long observationId,
      long predictionId,
      SeismicPhase phase,
      Source source,
      Receiver receiver,
      EnumSet<GeoAttributes> requesteAttributes,
      double originTime,
      double arrivalTime,
      double deltim,
      double gtlevel,
      boolean defining,
      int index) throws Exception
  {
	  super(observationId, receiver, source, phase, requesteAttributes, defining);
	  
    this.predictionId = predictionId;
    
    this.originTime = originTime;
    this.arrivalTime = arrivalTime;
    this.deltim = deltim;
    this.gtlevel = gtlevel;
    this.timedef = defining;
    this.index = index;
  }

  public Prediction getPrediction() {
      return prediction;
  }
  
  public ObservationTomo setPrediction(Prediction prediction) {
      this.prediction = prediction;
      return this;
  }
  
  /**
   *
   * @param arrivalTime double
   */
  public void setArrivalTime(double arrivalTime)
  {
    this.arrivalTime = arrivalTime;
  }

  /**
   *
   * @return double
   */
  public double getArrivalTime()
  {
    return arrivalTime;
  }

  /**
   *
   * @return double
   */
  public double getOriginTime()
  {
    //return source.getOriginTime();
    return originTime;
  }

  /**
   * Corrects this observations origin time by adding the event term to the
   * origin time and zeroing the event term. Only do this if the source is NOT
   * a GTTime event.
   */
  public void correctOriginTime()
  {
    if (!source.isGTTime())
    {
      //source.setTime(source.getOriginTime() + eventTerm);
      originTime += eventTerm;
      eventTerm = 0.0;
    }
  }

  /**
   *
   * @param eventTerm double
   */
  public void setEventTerm(double eventTerm)
  {
    this.eventTerm = eventTerm;
  }

  /**
   *
   * @return double
   */
  public double getEventTerm()
  {
    return eventTerm;
  }

  /**
   *
   * @param eventTerm double
   */
  public void setSiteTerm(double siteTerm)
  {
    this.siteTerm = siteTerm;
  }

  /**
   *
   * @return double
   */
  public double getSiteTerm()
  {
    return siteTerm;
  }

  /**
   *
   * @return double
   */
  public double getTraveltime()
  {
    //return arrivalTime - source.getOriginTime();
    return arrivalTime-originTime;
  }

  /**
   *
   * @return double
   */
  public double getDeltim()
  {
    return deltim;
  }

  /**
   *
   * @return GtEpi
   */
  public double getGtLevel()
  {
    return gtlevel;
  }

  /**
   * @return defining
   */
  public boolean isDefining()
  {
    return timedef;
  }

  /**
   * Sets this observation as non-defining
   */
  public void setAsNonDefining()
  {
    timedef = false;
  }

  /**
   *
   * @return long
   */
  public long getEventId()
  {
	  return source.getEvid();
  }

  /**
   * Sets the observation index
   *
   * @param index int
   */
  public void setIndex(int index)
  {
    this.index = index;
  }

  /**
   * Returns the observation index
   *
   * @return The observation index.
   */
  public int getIndex()
  {
    return index;
  }

  /**
   * Sets the site index
   *
   * @param siteIndex int
   */
  public void setSiteIndex(int siteIndex)
  {
    this.siteIndex = siteIndex;
  }

  /**
   * Returns the site index
   *
   * @return The observation index.
   */
  public int getSiteIndex()
  {
    return siteIndex;
  }

  /**
   * Sets the refsta index
   *
   * @param refstaIndex int
   */
  public void setRefStaIndex(int refstaIndex)
  {
    this.refstaIndex = refstaIndex;
  }

  /**
   * Returns the refsta index
   *
   * @return The refsta index.
   */
  public int getRefStaIndex()
  {
    return refstaIndex;
  }

  /**
   * Returns the refstaIndex if the input argument is true, or the siteIndex
   * otherwise.
   * @param useRefSta Flag, which if true, uses the refsta unique index.
   *                  Otherwise the site unique index is returned.
   * @return The refsta or site unique index.
   */
  public int getSiteIndex(boolean useRefSta)
  {
  	if (useRefSta)
  		return refstaIndex;
  	else
  		return siteIndex;
  }
 
  /**
   * Sets the origin index
   *
   * @param originIndex int
   */
  public void setOriginIndex(int originIndex)
  {
    this.originIndex = originIndex;
  }

  /**
   * Returns the origin index
   *
   * @return The origin index.
   */
  public int getOriginIndex()
  {
    return originIndex;
  }

  /**
   *
   * @return double
   */
  public double getObservedSlowness()
  {
    return observedSlowness;
  }

  /**
   *
   * @return double
   */
  public double getDelslo()
  {
    return delslo;
  }

  /**
   *
   * @return double
   */
  public double getObservedAzimuth()
  {
    return observedAzimuth;
  }

  /**
   *
   * @return double
   */
  public double getDelaz()
  {
    return delaz;
  }

  /**
   * Returns the predicted travel time from the rayInfo object.
   * 
   * @return The predicted travel time from the rayInfo object.
   */
  public double getPredictedTravelTime()
  {
    return prediction.getAttribute(GeoAttributes.TRAVEL_TIME);    
  }

  /**
   * Returns the average ray velocity.
   * 
   * @return The average ray velocity.
   */
  public double getAverageRayVelocity()
  {
    return prediction.getAttribute(GeoAttributes.AVERAGE_RAY_VELOCITY);    
  }

  /**
   * Returns the travel time residual.
   * 
   * @return The travel time residual.
   */
  public double getTravelTimeResidual()
  {
    return getTraveltime() - getPredictedTravelTime();
  }

  /**
   * Returns the initial travel time residual.
   * 
   * @return The initial travel time residual.
   */
  public double getInitialTravelTimeResidual()
  {
    return getTravelTimeResidual() - getFirstResidual();
  }

  /**
   * Returns the delta travel time residual.
   * 
   * @return The delta travel time residual.
   */
  public double getDeltaTravelTimeResidual()
  {
    return getTravelTimeResidual() - getLastResidual();
  }

  /**
   * Returns the total delta travel time residual.
   * 
   * @return The total delta travel time residual.
   */
  public double getTotalDeltaTravelTimeResidual()
  {
    return getTravelTimeResidual() - getFirstResidual();
  }

  /**
   * Returns the travel time residual.
   * 
   * @return The travel time residual.
   */
  public double getTravelTimeWeightedResidual()
  {
    return getTravelTimeResidual() / getTravelTimeUncertainty();
  }

  /**
   * Returns the initial travel time weighted residual.
   * 
   * @return The initial travel time weighted residual.
   */
  public double getInitialTravelTimeWeightedResidual()
  {
    return getFirstResidual() / getTravelTimeUncertainty();
  }

  /**
   * Returns the delta travel time weighted residual.
   * 
   * @return The delta travel time weighted residual.
   */
  public double getDeltaTravelTimeWeightedResidual()
  {
    return getDeltaTravelTimeResidual() / getTravelTimeUncertainty();
  }

  /**
   * Returns the total delta travel time weighted residual.
   * 
   * @return The total delta travel time weighted residual.
   */
  public double getTotalDeltaTravelTimeWeightedResidual()
  {
    return getTotalDeltaTravelTimeResidual() / getTravelTimeUncertainty();
  }

  /**
   *
   * @return BenderObservation
   */
  public PredictorObservation getPredictorObservation(boolean saveRayPath)
  	throws Exception
  {
    PredictorObservation bobs = new PredictorObservation(source, receiver,
                                    phase, requestedAttributes, timedef, index);
    if (isSkipBouncePointOptimization())
    	bobs.getUndersideReflectedPhaseBouncePoint().setFixedBouncePointPositionDeg(prediction.getAttribute(GeoAttributes.BOUNCE_POINT_LATITUDE_DEGREES),
    																			prediction.getAttribute(GeoAttributes.BOUNCE_POINT_LONGITUDE_DEGREES));
    if (saveRayPath) bobs.setRayPathReturn(receiver.getReceiverId(), source.getSourceId());
    return bobs;
  }

  /**
   *
   * @param phase Phase
   */
  public void setPhase(SeismicPhase phase) {
    this.phase = phase;
  }

  /**
   *
   * @param observedSlowness double
   */
  public void setObservedSlowness(double observedSlowness)
  {
    this.observedSlowness = observedSlowness;
  }

  /**
   *
   * @param delslow double
   */
  public void setDelslow(double delslow)
  {
    this.delslo = delslow;
  }

  /**
   *
   * @param observedAzimuth double
   */
  public void setObservedAzimuth(double observedAzimuth)
  {
    this.observedAzimuth = observedAzimuth;
  }

  /**
   *
   * @param delaz double
   */
  public void setDelaz(double delaz)
  {
    this.delaz = delaz;
  }

  /**
   * Sets the total travel time uncertainty (=sqrt((GT/avgVel)^2 + deltim^2))
   *  
   * @param ttUncertainty Total travel time uncertainty.
   */
  public void setTravelTimeUncertainty(double ttUncertainty)
  {
    this.ttUncertainty = ttUncertainty;
  }

  /**
   * Returns the total travel time uncertainty.
   * 
   * @return The total travel time uncertainty.
   */
  public double getTravelTimeUncertainty()
  {
    return ttUncertainty;
  }

  /**
   * Sets the current iteration travel time residual. If this is the first call
   * to this function the first residual is set. The last residual is set to the
   * current residual before setting the current residual. If the first iteration
   * adaption level residual is set to -1.0 then it is also set to the residual. 
   * 
   * @param residual The current iteration travel time residual.
   */
  public void setResidual(double residual)
  {
    if (ttFirstResidual == -1.0)
    {
      ttFirstResidual = residual;
      ttLastResidual  = 0.0;
    }
    else
      ttLastResidual = ttCurrResidual;

//    if (ttFirstAdaptResidual == -1.0) ttFirstAdaptResidual = residual;

    ttCurrResidual = residual;
  }

  /**
   * Returns the last iteration residual travel time.
   * 
   * @return The last iteration residual travel time.
   */
  public double getLastResidual()
  {
    return ttLastResidual;
  }

  /**
   * Returns the first iteration residual travel time.
   * 
   * @return The first  iteration residual travel time.
   */
  public double getFirstResidual()
  {
    return ttFirstResidual;
  }
//
//  /**
//   * Returns the first iteration residual of the current adaption level.
//   * 
//   * @return The first iteration residual of the current adaption level.
//   */
//  public double getFirstAdaptionLevelResidual()
//  {
//    return ttFirstAdaptResidual;
//  }

  /**
   * Returns the current iteration residual travel time.
   * 
   * @return The current iteration residual travel time.
   */
  public double getCurrentResidual()
  {
    return ttCurrResidual;
  }
//
//  /**
//   * Resets the first iteration travel time residual of the current adaption
//   * level.
//   */
//  public void resetAdaptionLevelResidual()
//  {
//    ttFirstAdaptResidual = -1.0;
//  }

  /**
   * Sets the first iteration residual travel time.
   *  
   * @param ttFirstResidual The first iteration residual travel time.
   */
  public void setInActiveTravelTime(double ttInActive)
  {
    this.ttInActive = ttInActive;
  }

  /**
   * Returns the first iteration residual travel time.
   * 
   * @return The first  iteration residual travel time.
   */
  public double getInActiveTravelTime()
  {
    return ttInActive;
  }

  /**
   * Sets the crustal thickness below the site and event (sum).
   *  
   * @param The crustal thickness below the site and event (sum).
   */
  public void setCrustalThickness(double crstThkns)
  {
    this.crustalThickness = crstThkns;
  }

  /**
   * Returns the crustal thickness below the site and event (sum).
   * 
   * @return The crustal thickness below the site and event (sum).
   */
  public double getCrustalThickness()
  {
    return crustalThickness;
  }

  /**
   * Retrieve data needed to make a SOURCE database row.
   * @return Object[]
   */
  public Object[] getSourceForDb()
  {
	Object values[] = new Object[10];
	values[0] = source.getSourceId();   // sourceid
	values[1] = source.getEvid();   // eventid
	values[2] = source.getLatDegrees();   // lat
	values[3] = source.getLonDegrees();   // lon
	values[4] = source.getDepth();   // depth
  //values[5] = new java.sql.Timestamp((long)(source.getOriginTime()*1000));   // origintime converted to timestampe
	values[5] = new java.sql.Timestamp((long)(originTime*1000));   // origintime converted to timestampe
	values[6] = gtlevel;   // gtlevel
	values[7] = -1L;   // numassoc, set to dummy value of -1
	values[8] = auth;     // auth
	values[9] = lddate;   // lddate
	
	return values;
  }

  /**
   * Retrieve data needed to make an Observation database row.
   * @return Object[]
   */
  public Object[] getObservationForDb()
  {
    Object values[] = new Object[11];
    values[0] = observationId;   // observationid
    values[1] = receiver.getReceiverId();   // receiverid
    values[2] = phase.toString();   // iphase
    values[3] = new java.sql.Timestamp((long)(arrivalTime*1000));   // arrivalTime converted to timestamp
    values[4] = ttUncertainty;   // timeUncertainty
    
    // Note that the values for azimuth and slowness are OBSERVED values, not predicted
    values[5] = degrees(observedAzimuth);   // azimuth or degrees(rayInfo.getAttribute(GeoAttributes.AZIMUTH))
    values[6] = degrees(delaz);   // azUncertainty
    values[7] = convertSlow(observedSlowness);   // slowness or convertSlow(rayInfo.getAttribute(GeoAttributes.PHSLOWNESS))
    values[8] = convertSlow(delslo);   // slowUncertainty 
    
    values[9] = auth;   // auth
    values[10] = lddate;   // lddate
   
    return values;
  }
  


  /**
   * Retrieve data needed to make a SrcObsAssoc database row.
   * @return Object[]
   */
  public Object[] getSrcObsAssocForDb()
  {
    Object values[]  = new Object[11];
    GeoVector srcgv  = source;
    GeoVector rcvrgv = receiver;
    values[0] = source.getSourceId();   // sourceid
    values[1] = observationId;   // observationid
    values[2] = phase.toString();   // phase
    values[3] = srcgv.distanceDegrees(rcvrgv);   // delta
    values[4] = srcgv.azimuthDegrees(rcvrgv, 0.); // esaz
    values[5] = rcvrgv.azimuthDegrees(srcgv, 0.); // seaz
    values[6] = (timedef ? "d" : "n");   // timedef
    values[7] = "-";     // azdef - Note: not implemented yet
    values[8] = "-";    // slodef - Note: not implemented yet
    values[9] = auth;   // auth
    values[10] = lddate;   // lddate
    
    return values;
  }

  /**
   * Retrieve data needed to make a TTPRED_ASSOC database row.
   * @return Object[]
   */
  public Object[] getReceiverForDb()
  {
    Object values[] = new Object[9];
    values[0] = receiver.getReceiverId(); // receiverid  
    values[1] = receiver.getSta();   // sta
    values[2] = receiver.getLatDegrees();   // lat
    values[3] = receiver.getLonDegrees();   // lon
    values[4] = -receiver.getDepth();   // elevation
    values[5] = new java.sql.Timestamp((long)(receiver.getOnTime()*1000));   // starttime 
    values[6] = new java.sql.Timestamp((long)(receiver.getOffTime()*1000));   // endtime 
    values[7] = auth;     // auth
    values[8] = lddate;   // lddate
    
    return values;
  }

  /**
   * Retrieve data needed to make a PREDICTION database row.
   *
   * @return Object[]
   * @param outputPredictionId Long
   * @param geomodelid Long
   * @param algorithmid Long
   * @param polygon Polygon3D
   * @param scrnWrtr screen writer output to log error messages to, or null (Note: it is important
   * to pass in null when performing predictions, because the starting model will not have site 
   * terms, so warnings should be ignored).
   * 
 * @param  
   */
  public Object[] getPredictionForDb(Long outputPredictionId, Long geomodelid, Long algorithmid, Polygon3D polygon,
		  ScreenWriterOutput scrnWrtr)
  {
    Object values[] = new Object[20];
    predictionId = outputPredictionId;
    values[ 0] = outputPredictionId;   // predictionid
    values[ 1] = source.getSourceId();   // sourceid
    values[ 2] = receiver.getReceiverId();   // receiverid
    values[ 3] = geomodelid;   // geomodelid
    values[ 4] = algorithmid;   // ttalgoid
    values[ 5] = observationId;   // observationid
    values[ 6] = prediction.getPhase().toString();   // phase
    values[ 7] = prediction.getRayType().toString();   // raytype
    values[ 8] = prediction.getAttribute(GeoAttributes.ACTIVE_FRACTION);   // activefraction 
    
    // parse tt and site term to determine validity before output
    double travelTime = prediction.getAttribute(GeoAttributes.TRAVEL_TIME);
    double siteTerm = this.getSiteTerm();
    if(travelTime == Globals.NA_VALUE)
    	values[9] = Globals.NA_VALUE;
    else if(siteTerm == Globals.NA_VALUE) {
    	
    	// only output warning if process is "tomography"
    	// as during "predictions", there usually is not a site term
    	// in the starting model
    	if(scrnWrtr != null && scrnWrtr.isOutputOn()) {
    		scrnWrtr.writeln("Warning: Receiver " + receiver.getSta() 
    	    		+ " (receiverid = " + receiver.getReceiverId()
    	    		+ ") does not have a valid site term for observation id "
    	    		+ observationId + ".");
    	}
    	
    	// if site term is invalid, set travel-time only 
    	values[9] = travelTime;
    }
    else
    	values[9] = travelTime + siteTerm;  // traveltime (including site term if available)
    
    if((Double)values[9] == Globals.NA_VALUE)
    	values[10] = Globals.NA_VALUE;
    else
    	values[10] = getTraveltime() - ((Double)values[9]);  // ttresidual (observed-predicted)
    values[11] = degrees(prediction.getAttribute(GeoAttributes.AZIMUTH)); // azimuth
    values[12] = convertSlow(prediction.getAttribute(GeoAttributes.SLOWNESS));  // slowness
    values[13] = degrees(prediction.getAttribute(GeoAttributes.BACKAZIMUTH)); // backazimuth
    values[14] = prediction.getAttribute(GeoAttributes.TURNING_DEPTH);   // turndepth
    values[15] = prediction.getAttribute(GeoAttributes.OUT_OF_PLANE);   // maxoutplane
    values[16] = prediction.getAttribute(GeoAttributes.CALCULATION_TIME);   // calctime 
    
    // determine if source/receiver is in polygon
    StringBuffer s = new StringBuffer();
    if(polygon != null) {
    	if(polygon.contains(source.getUnitVector()))
    		s.append("s");
    	if(polygon.contains(receiver.getUnitVector()))
    		s.append("r");

    }
    // cannot insert empty string, need blank
    if(s.toString().length() == 0)
    	s.append(" ");
    values[17] = s.toString();
    
    values[18] = auth;     // auth
    values[19] = lddate;   // lddate
    
    
    return values;
  }

  public Object[] getRowInfo(String tableType)
  {
    if (tableType.equalsIgnoreCase(DBTableTypes.SOURCE.toString()))
      return getSourceForDb();
    if (tableType.equalsIgnoreCase(DBTableTypes.SRCOBSASSOC.toString()))
      return getSrcObsAssocForDb();
    if (tableType.equalsIgnoreCase(DBTableTypes.OBSERVATION.toString()))
      return getObservationForDb();
    if (tableType.equalsIgnoreCase(DBTableTypes.RECEIVER.toString()))
      return getReceiverForDb();
    
    System.err.println("Requested table type '" + tableType + "' not supported in ObservationNew.getRowInfo().");
    return null;
  }

  /**
   * Retrieve the jdate that corresponds to specified Date
   * @param date Date
   * @return long
   */
  static public long getJdate(Date date)
  {
    Calendar calendar = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
    calendar.setTime(date);
    return calendar.get(Calendar.YEAR)*1000
        +calendar.get(Calendar.DAY_OF_YEAR);
  }

  /**
   * Retrieve the jdate that corresponds to specified time
   * @param seconds double number of seconds since Jan 1, 1970
   * @return long
   */
  static public long getJdate(double seconds)
  {
    return getJdate((long)(seconds*1000.));
  }

  /**
   * Retrieve the jdate that corresponds to specified time
   * @param milliSeconds number of msec since Jan 1, 1970
   * @return long
   */
  static public long getJdate(long milliSeconds)
  {
    return getJdate(new Date(milliSeconds));
  }

  private Double degrees(double x)
  {
    if (x == Globals.NA_VALUE)
      return Globals.NA_VALUE;
    else if (x == -999.)
      return -999.;
    else
      return  Math.toDegrees(x);
  }

  /**
   * convert slowness from sec/radian to sec/degree
   *
   * @param x double
   * @return Double
   */
  private Double convertSlow(double x)
  {
    if (x == Globals.NA_VALUE)
      return Globals.NA_VALUE;
    else if (x == -999.)
      return -999.;
    else
      return  Math.toRadians(x);
  }

  /**
   * Returns true if traveltime is a valid entry (positive).
   *
   * @return True if traveltime is a valid entry (positive).
   */
  public boolean hasValidTravelTime()
  {
    return (getTraveltime() > 0.0);
  }

  /**
   * Mark this observation
   */
  public void mark()
  {
    this.mark = true;
  }

  /**
   * Unmark this observation
   */
  public void unMark()
  {
    this.mark = false;
  }

  /**
   * Returns true if this observation is marked.
   *
   * @return True if this observation is marked.
   */
  public boolean isMarked()
  {
    return mark;
  }

  public long getReceiverId()
  {
	  return receiver.getReceiverId();
  }


  /**
   * @return the receiverInPolygon
   */
  public boolean isReceiverInPolygon()
  {
	  return receiverInPolygon;
  }


  /**
   * @param receiverInPolygon the receiverInPolygon to set
   */
  public void setReceiverInPolygon(boolean receiverInPolygon)
  {
	  this.receiverInPolygon = receiverInPolygon;
  }


  /**
   * @return the sourceInPolygon
   */
  public boolean isSourceInPolygon()
  {
	  return sourceInPolygon;
  }


  /**
   * @param sourceInPolygon the sourceInPolygon to set
   */
  public void setSourceInPolygon(boolean sourceInPolygon)
  {
	  this.sourceInPolygon = sourceInPolygon;
  }

  /**
   * Get the receiver name.
   * 
   * @return receiver name 
   */
  public String getReceiverName()
  {
	  return receiver.getSta();
  }

  /**
   * Get the observation's status.
   * 
   * @return observation status string
   */
  public ObservationStatus getStatus()
  {
    return status;
  }

  /**
   * Set the observations status.
   * 
   * @param status The observations status.
   */
  public void setStatus(ObservationStatus status)
  {
    this.status = status;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() throws CloneNotSupportedException
  {
	  return super.clone();
  }

  
  public long getPredictionId() 
  {
  	return predictionId;
  }
  
    
  /**
   * Return the sum of synthetic site term, event term, and noise term.
   * Each term's value is based on normal distribution with a standard 
   * deviation (sigma), specified in the properties file. 
   * 
   * @param sigmaSiteTerm standard deviation to use for synthetic site term's normal distribution
   * @param sigmaEventTerm standard deviation to use for synthetic event term's normal distribution
   * @param sigmaNoiseTerm standard deviation to use for synthetic noise term's normal distribution
   * @param synSiteTermMap map from receiver id to synthetic site term (to allow re-use)
   * @param synEventTermMap map from event id to synthetic event term (to allow re-use)
   * @return sum of the synthetic site term, event term, and noise term.
   */
  
  
  /**
   * 
   */
  public double getSyntheticTerms(double sigmaSiteTerm, double sigmaEventTerm,
  		double sigmaNoiseTerm, HashMap<Long, Double> synSiteTermMap,
  		HashMap<Long, Double> synEventTermMap, Random random)
  {
  	double siteTerm = 0, eventTerm = 0, noiseTerm = 0;
  	
  	// get site term from map if already computed
  	Double val = synSiteTermMap.get(getReceiverId());
  	if(val != null) {
  		siteTerm = val;
  	}
  	else {
  		// compute synthetic site term from normal distribution  
  		
  		// get next Gaussian (mean 0, std dev 1), and scale it 
  		// according to sigmaSiteTerm
  		siteTerm = random.nextGaussian() * sigmaSiteTerm;
  		
  		// save new site term for this receiver into the map
  		synSiteTermMap.put(getReceiverId(), siteTerm);
  	}
  	
  	// get event term
  	val = synEventTermMap.get(getSourceId());
  	if(val != null) {
  		eventTerm = val;
  	}
  	else {
  		// compute synthetic event term from normal distribution  
  		
  		// get next Gaussian (mean 0, std dev 1), and scale it 
  		// according to sigmaSiteTerm
  		eventTerm = random.nextGaussian() * sigmaEventTerm;
  		
  		// save new event term for this sourceid into the map
  		synEventTermMap.put(getSourceId(), eventTerm);
  	}
  	
  	// get Gaussian noise term and scale by sigmaNoiseTerm
  	noiseTerm = random.nextGaussian() * sigmaNoiseTerm;
  	
  	return siteTerm + eventTerm + noiseTerm;
  }

  public void setEvaluateEventTerm(boolean eet)
  {
    evalEventTerm = eet;
  }

  public boolean getEvaluateEventTerm()
  {
    return evalEventTerm;
  }

  public void setEvaluateSiteTerm(boolean est)
  {
    evalSiteTerm = est;
  }

  public boolean getEvaluateSiteTerm()
  {
    return evalSiteTerm;
  }

  /**
   * Sets the poorly represented use flag (for tomography) to flg.
   * 
   * @param flg The flag setting that aUsePoorlyRepresented is set.
   */
  public static void setPoorlyRepresentedUseFlag(boolean flg)
  {
    aUsePoorlyRepresented = flg;
  }
//
//  /**
//   * Returns true if either event or site term evaluation is off (poorly
//   * represented) and the flag aUsePoorlyRepresented is true.
//   * @return
//   */
//  public boolean usePoorlyRepresentedInTomography()
//  {
//    return ((!evalEventTerm || !evalSiteTerm) && aUsePoorlyRepresented) ?
//           true : false;
//  }


  /**
   * Reads the prediction state from the input FileInputBuffer. On return a
   * boolean flag is returned which indicates the existence or absence of an
   * assigned prediction. If false the prediction is null. If true the caller
   * must create a new prediction from the FileInputBuffer (fib) and then
   * call setPrediction(prediction) to set it into this observation. It is up
   * to the caller to create the appropriate type of prediction (for tomography
   * it is usually a RayInfo object). The prediction on exist is set to null.
   * 
   * @param fib The FileInputBuffer.
   * @return
   * @throws IOException
   */
  public boolean readPredictionState(FileInputBuffer fib) throws IOException
  {
      evalEventTerm    = fib.readBoolean();
      eventTerm        = fib.readDouble();
      evalSiteTerm     = fib.readBoolean();
      siteTerm         = fib.readDouble();
      ttUncertainty    = fib.readDouble();
      ttCurrResidual   = fib.readDouble();
      ttLastResidual   = fib.readDouble();
      ttFirstResidual  = fib.readDouble();
      ttInActive       = fib.readDouble();
      crustalThickness = fib.readDouble();
      status           = ObservationStatus.values()[fib.readInt()];

      skipRayTrace     = fib.readInt();
      skipBouncePointOptimization = fib.readInt();

      rayWeightMovement   = fib.readDouble();
      bouncePointMovement = fib.readDouble();

      rayWeights = Prediction.readRayWeights(fib);

      prediction       = null;
      return fib.readBoolean();
  }

  /**
   * Writes the prediction state of this observation to the input FileOutpuBuffer.
   * If the prediction is defined then it is also written to the output buffer.
   * 
   * @param fob The FileOutputBuffer.
   * @throws IOException
   */
  public void writePredictionState(FileOutputBuffer fob) throws IOException
  {
      fob.writeBoolean(evalEventTerm);
      fob.writeDouble(eventTerm);
      fob.writeBoolean(evalSiteTerm);
      fob.writeDouble(siteTerm);
      fob.writeDouble(ttUncertainty);
      fob.writeDouble(ttCurrResidual);
      fob.writeDouble(ttLastResidual);
      fob.writeDouble(ttFirstResidual);
      fob.writeDouble(ttInActive);
      fob.writeDouble(crustalThickness);
      fob.writeInt(status.ordinal());				

      fob.writeInt(skipRayTrace);
      fob.writeInt(skipBouncePointOptimization);

      fob.writeDouble(rayWeightMovement);
      fob.writeDouble(bouncePointMovement);

      Prediction.writeRayWeights(rayWeights, fob);

      if (prediction == null)
	  fob.writeBoolean(false);
      else
      {
	  fob.writeBoolean(true);
	  prediction.writePrediction(fob);
      }
  }

  /**
   * Used by predictors that understand the behavior of reflective phases that
   * are defined with under side reflective bounce points. If the under side
   * reflected phase bounce point object is not defined it is first created and
   * then returned.
   * 
   * @return an UndersideReflectedPhaseBouncePoint object.
   */
  public UndersideReflectedPhaseBouncePoint getUndersideReflectedPhaseBouncePoint()
  {
      if (undersideReflectedPhaseBouncePoint == null)
	  undersideReflectedPhaseBouncePoint = new UndersideReflectedPhaseBouncePoint();

      return undersideReflectedPhaseBouncePoint;
  }

  @Override
  public String toString() {
      return String.format("%d %d %d %d %s %1.3f %1.3f %b",
	      getSource().getSourceId(), 
	      getObservationId(), 
	      getReceiver().getReceiverId(),
	      getPredictionId(),
	      getPhase().name(),
	      getArrivalTime(),
	      getDeltim(),
	      isDefining());
  }

}
