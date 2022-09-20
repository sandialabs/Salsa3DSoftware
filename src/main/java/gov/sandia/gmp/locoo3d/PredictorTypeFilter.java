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

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;

public class PredictorTypeFilter
{

   private class FilterEntry
   {
     //boolean anyOrid = true;
     long orid = -1;
     String station = null;
     SeismicPhase phase = null;
     GeoAttributes type = null;
     String predictorType = "";
   }

  private FilterEntry[] filterComponents;

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // Default PredictorTypeFilter Constructor
  //
  // INPUT ARGS:
  //   Predictor&            pred       Handle to Object That Supports Predictions
  // OUTPUT ARGS: NONE
  // RETURN:      NONE
  //
  // *****************************************************************************
  public PredictorTypeFilter()
  {
    setup();
  } // END Observation Default Constructor

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // Clear
  //
  // INPUT ARGS:  NONE
  // OUTPUT ARGS: NONE
  // RETURN:      NONE
  //
  // *****************************************************************************
  public void clear()
  {
    // deallocate memory from the VectorMod of setDefiningEntries
    filterComponents = new FilterEntry[0];
  }

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // initialize the PredictorTypeFilter object.
  //
  // INPUT ARGS:  obs
  // OUTPUT ARGS: NONE
  // RETURN:      NONE
  //
  // *****************************************************************************
  public void setup()
  {
    clear();
  }

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // initialize the PredictorTypeFilter object.
  //
  // INPUT ARGS:  obs
  // OUTPUT ARGS: NONE
  // RETURN:      NONE
  //
  // *****************************************************************************
  public void setup(String filter) throws LocOOException
  {
    if (filter.length() > 0)
    {
      String[] list = filter.split(",");
      filterComponents = new FilterEntry[list.length];
      for (int i = 0; i < list.length; i++)
      {
        String[] s = list[i].split("/");

          FilterEntry entry = new FilterEntry();
          filterComponents[i] = entry;

          if (!s[0].equals("*"))
            entry.orid = Long.parseLong(s[0]);
          if (!s[1].equals("*"))
            entry.station = s[1];
          if (!s[2].equals("*"))
            entry.phase = SeismicPhase.valueOf(s[2]);
          if (!s[3].equals("*"))
            entry.type = GeoAttributes.valueOf(s[3].toUpperCase());
          entry.predictorType = s[4].toLowerCase();
        }
      }
      else
        clear();
    }


  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // apply the PredictorTypeFilter object.
  //
  // INPUT ARGS:  obs
  // OUTPUT ARGS: NONE
  // RETURN:      NONE
  //
  // *****************************************************************************
  public String apply(long orid, String station, SeismicPhase phase,
                       GeoAttributes obsType, String initialPredictorType)
  {
    String state = initialPredictorType;
    for (int j = 0; j < filterComponents.length; j++)
    {
      if ( (filterComponents[j].orid < 0 ||
            filterComponents[j].orid == orid)
          &&
          (filterComponents[j].station == null ||
           filterComponents[j].station == station)
          &&
          (filterComponents[j].phase == null ||
           filterComponents[j].phase.equals(phase))
          &&
          (filterComponents[j].type == null ||
           filterComponents[j].type == obsType))
        state = filterComponents[j].predictorType;
    }
    return state;
  }

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // apply the PredictorTypeFilter object.
  //
  // INPUT ARGS:  obs
  // OUTPUT ARGS: NONE
  // RETURN:      NONE
  //
  // *****************************************************************************
  public String apply(ObservationComponent obs, String initialState)
  {
    return apply(obs.getSourceid(), obs.getReceiver().getSta(), obs.getPhase(),
                 obs.getObsType(), initialState);
  }

}
