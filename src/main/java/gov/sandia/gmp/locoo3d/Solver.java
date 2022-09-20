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

import java.io.IOException;

import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

/**
 * <p>Title: LocOO</p>
 *
 * <p>Description: Seismic Event Locator</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: Sandia National Laboratories</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public abstract class Solver
{
// **** _PROTECTED DATA_ ***************************************************

  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // Parameterized Solver Base Class Constructor
  //
  // INPUT ARGS:
  //   LocatorInterface&       loc_handle      Handle to the Locator Interface
  //   kbat::ModuleConfigInfo& config          Handle to the Configuration Container
  //   WorkingLocation&        location        Handle to the Working Loction
//                                                 Container
  //   ObservationVec&         observations    Handle to the Observation Container
  // OUTPUT ARGS: NONE
  // RETURN:      NONE
  //
  // *****************************************************************************
  protected Solver()
      throws PropertiesPlusException, LocOOException, GMPException
  {
  } // END Solver Default Constructor

  /**
   * locateEvents
   *
   * @param events EventList
 * @throws IOException 
 * @throws Exception 
   */
  public abstract void locateEvents(EventList events)
      throws Exception;


  // **** _FUNCTION DESCRIPTION_ *************************************************
  //
  // Extract configuration parameters from locoo.configInfo(), a TConfigInfo
  // object that contains parameter information normally retrieved from a parameter
  // file.
  //
  // INPUT ARGS:  NONE
  // OUTPUT ARGS: NONE
  // RETURN:      NONE
  //
  // *****************************************************************************
//  public abstract void setup(PropertiesPlusGMP properties)
//      throws LocOOException, PropertiesPlusException;

}
