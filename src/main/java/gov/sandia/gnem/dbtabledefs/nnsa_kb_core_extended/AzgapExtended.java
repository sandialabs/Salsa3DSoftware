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
package gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import gov.sandia.gmp.util.globals.SiteInterface;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_custom.Azgap;

/**
 * This class extends Azgap and adds only a single new constructor that takes 
 * an event location and a list of sites.  From that it is able to compute all
 * the fields of the base class, Azgap.
 * @author sballar
 *
 */
public class AzgapExtended extends Azgap {

    private static final long serialVersionUID = 2788187273069123925L;

    /**
     * Computes all the fields of the base class
     * @param orid 
     * @param event unit vector of the event location
     * @param sites 
     * @throws Exception
     */
    public AzgapExtended(long orid, double[] event, Collection<? extends SiteInterface> sites) throws Exception
    {
	// set all default values
	super();

	// inner class to manage a sta-esaz pair in a sorted list of Esaz objects.
	class Esaz implements Comparable<Esaz>
	{
	    String sta;
	    double esaz; // event-station azimuth in degrees

	    public Esaz(double esaz, String sta) { this.esaz=esaz; this.sta=sta; }
	    
	    /**
	     * Add a copy of the specified Esaz with the esaz value incremented by 360 degrees
	     * @param other
	     */
	    public Esaz(Esaz other) { this.esaz = other.esaz+360.; this.sta=other.sta; }

	    @Override
	    public int compareTo(Esaz o) { 
		int d = (int) Math.signum(this.esaz-o.esaz);
		if (d == 0)
		    d = this.sta.compareTo(o.sta);
		return d;
	    }

	    @Override
	    public String toString() { return String.format("%-6s %5.2f", sta, esaz); }
	}

	setOrid(orid);

	if (sites == null || sites.isEmpty())
	    // all default values (-1) except orid.
	    return;

	int nsta = 0;
	int nsta30 = 0;
	int nsta250 = 0;

	// set of unique station names.  Each station is only considered once.
	Set<String> stas = new HashSet<String>();

	ArrayList<Esaz> esaz = new ArrayList<Esaz>();

	double[] station;
	double backAz;

	// if the event is at one of the poles, then it is not possible to 
	// compute event-to-station azimuth.  Station longitude will be used instead.
	boolean eventAtPole = VectorGeo.isPole(event);

	for (SiteInterface site : sites)
	{
	    if (!stas.contains(site.getSta()))
	    {
		station = VectorGeo.getVectorDegrees(site.getLat(), site.getLon());

		if (eventAtPole)
		    // if the event is located at north or south pole, then backAzimuth is indeterminant
		    // but we can use the site longitude instead.
		    backAz = (site.getLon()+360.) % 360.;
		else
		    backAz = (VectorGeo.azimuthDegrees(event, station, Double.NaN)+360.) % 360.;

		if (!Double.isNaN(backAz)) // ignore stations 0 or 180 degrees from event
		{
		    stas.add(site.getSta());

		    double dkm = VectorGeo.angle(station, event) * 6371.;
		    if (dkm <= 30)
			++nsta30;
		    if (dkm <= 250)
			++nsta250;
		    ++nsta;

		    esaz.add(new Esaz(backAz, site.getSta()));
		}
	    }
	}

	if (esaz.size() == 0)
	    // this can happen if all stations are 0 or 180 deg from event.
	    // Return an Azgap object with all default values except orid.
	    return; 
	else if (esaz.size() == 1)
	{
	    setAzgap1(360.);
	    setAzgap2(360.);
	    setSta(esaz.get(0).sta);
	    setNsta(nsta);
	    setNsta30(nsta30);
	    setNsta250(nsta250);
	    return;
	}

	setNsta(nsta);
	setNsta30(nsta30);
	setNsta250(nsta250);

	// sort the sta-esaz objects in order of increasing esaz
	Collections.sort(esaz);

	// add a copy of the first esaz incremented by 360
	esaz.add(new Esaz(esaz.get(0)));

	// add a copy of the second esaz incremented by 360
	esaz.add(new Esaz(esaz.get(1)));

	double daz; 

	// find the largest azgap and azgap2
	for (int i = 1; i < esaz.size(); ++i)
	{
	    daz = esaz.get(i).esaz - esaz.get(i - 1).esaz;
	    if (daz > getAzgap1())
		setAzgap1(daz);
	    if (i > 1)
	    {
		daz = esaz.get(i).esaz - esaz.get(i-2).esaz;
		if (daz > getAzgap2())
		{
		    setAzgap2(daz);
		    setSta(esaz.get(i - 1).sta);
		}
	    }
	}
    }
}
