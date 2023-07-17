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

import java.io.Serializable;
import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.observation.ObservationComponent;

public class ObservationFilter implements Serializable
{
  /**
	 * A single entry in the filter.  Example is 
	 * +/-ORID/STATION/PHASE/ATTRIBUTE
	 * @author sballar
	 *
	 */
	private static class FilterEntry implements Serializable
	{
	    private static final long serialVersionUID = 1L;
		boolean anyOrid = true;
		long orid = -1;
		String station = "";
		SeismicPhase phase = null;
		GeoAttributes type = null;
		boolean state = true;

		FilterEntry(String entry) throws LocOOException
		{
			String[] s = entry.split("/");

			if (s.length != 4)
				throw new LocOOException("gen_defining_observations_filter "
						+"subString "+entry+" does not contain 4 components.  "
						+"Should be +/-ORID/STATION/PHASE/ATTRIBUTE"
						);

			for (int i=0; i<4; ++i)
				s[i] = s[i].trim();
			// if the first String starts with '-' then state will be false, indicating
			// that the filter should turn off the observation.  Any other character will
			// indicate turn on the observation.
			state = s[0].charAt(0) != '-';
			// delete the first character if is either '-' or '+'.
			if (s[0].charAt(0) == '-' || s[0].charAt(0) == '+')
				s[0] = s[0].substring(1);

			// if the first String is "*" then apply filter to any orid.
			anyOrid = s[0].equals("*");
			if (!anyOrid)
				orid = Long.parseLong(s[0]);

			station = s[1];

			try
			{
				phase = s[2].equals("*") ? null : SeismicPhase.valueOf(s[2]);
			}
			catch (java.lang.IllegalArgumentException e)
			{
				phase = SeismicPhase.NULL;
			}

			if (!s[3].equals("*"))
			{
				char ch = s[3].toUpperCase().charAt(0);
				if (ch == 'T')
					type = GeoAttributes.TRAVEL_TIME;
				else if (ch == 'A')
					type = GeoAttributes.AZIMUTH;
				else if (ch == 'S')
					type = GeoAttributes.SLOWNESS;

			}
		}

		@Override
		public String toString()
		{
			return String.format("%s%s/%s/%s/%s",
					state ? "+" : "-",
							anyOrid ? "*" : orid,
									station.length() == 0 ? "*" : station,
											phase == null ? "*" : phase,
													type == null ? "*" : type
					);
		}
	} // end of FilterEntry
	
	private static final long serialVersionUID = 1L;

	private ArrayList<FilterEntry> filterComponents = new ArrayList<FilterEntry>();

	private boolean match;

	/**
	 * Constructor that takes a String comprised of a number of
	 * comma delimited filter definitions.  Each filter definition
	 * is of the form +/-ORID/STATION/PHASE/ATTRIBUTE. Each component
	 * of a filter definition can be '*' which means accept anything
	 * for that component.  For Attribute, any string that start with
	 * letter 't' is TRAVELTIME, 'a' is AZIMUTH and 's' is 
	 * SLOWNESS.
	 * @param filter
	 * @throws LocOOException
	 */
	public ObservationFilter(String filter) throws LocOOException
	{
		if (filter != null && filter.trim().length() > 0)
			for (String entry : filter.split(","))
				filterComponents.add(new FilterEntry(entry));
	} // END Observation Default Constructor

	/**
	 * returns defining status of an observation after application of the 
	 * filter.
	 * @param orid
	 * @param station
	 * @param phase
	 * @param obsType
	 * @param defining
	 * @return
	 */
	public boolean apply(long orid, String station, SeismicPhase phase,
			GeoAttributes obsType, boolean defining)
	{
		for (int j = 0; j < filterComponents.size(); j++)
		{
			match = (filterComponents.get(j).anyOrid ||
					filterComponents.get(j).orid == orid)
					&&
					(filterComponents.get(j).station.equals("*") ||
							filterComponents.get(j).station.equals(station))
					&&
					(filterComponents.get(j).phase == null ||
							filterComponents.get(j).phase == phase)
					&&
					(filterComponents.get(j).type == null ||
							filterComponents.get(j).type == obsType);

			if (match)
				defining = filterComponents.get(j).state;
		}
		return defining;
	}


	/**
	 * If the characteristics of the specified observation match any of 
	 * the filters, set the defining status of the observation accordingly.
	 * Returns true if the defining status of the observation changed.
	 * @param obs
	 * @return true if the defining status changed.
	 */
	public boolean apply(ObservationComponent obs)
	{
		boolean defining = apply(obs.getSourceid(), obs.getReceiver().getSta(), obs.getPhase(),
				obs.getObsType(), obs.isDefining());
		
		boolean changed = obs.isDefining() != defining;

		obs.setDefiningOriginal(defining);

		return changed;
	}

}
