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
package gov.sandia.gmp.baseobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;

/**
 * Map is from a station name to a TreeSet of Receiver objects that should be
 * associated with that station name. The list contains Receiver objects with
 * different on-off dates. On-off dates should not overlap.
 */
public class Network extends TreeMap<String, TreeSet<Receiver>> {
	protected String networkName;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3474040940917371264L;

	public Network() {
		super();
		networkName = "NO_NAME";
	}

	public Network(String networkName) {
		super();
		this.networkName = networkName;
	}

	public Network(Collection<Site> sites, String networkName) throws GMPException {
		super();
		this.networkName = networkName;
		for (Site site : sites)
			add(new Receiver(site));
	}

	public String getName() {
		return networkName;
	}

	/**
	 * Add a single Receiver object to the network
	 * 
	 * @param receiver
	 */
	public void add(Receiver receiver) {
		TreeSet<Receiver> set = get(receiver.getSta());
		if (set == null) {
			set = new TreeSet<Receiver>();
			put(receiver.getSta(), set);
		}
		set.add(receiver);
	}

	/**
	 * Add a single Receiver object to the network
	 * 
	 * @param receiver
	 * @throws GMPException
	 */
	public void add(Site site) throws GMPException {
		add(new Receiver(site));
	}

	/**
	 * Add a Collection of Receiver objects to the network
	 * 
	 * @param receivers
	 */
	public void addAll(Collection<Receiver> receivers) {
		for (Receiver r : receivers)
			add(r);
	}

	/**
	 * 
	 * @return list of all receivers including all on/off dates.
	 */
	public ArrayList<Receiver> getReceiversAll() {
		ArrayList<Receiver> rcv = new ArrayList<Receiver>(size());
		for (TreeSet<Receiver> set : values())
			for (Receiver r : set)
				rcv.add(r);
		return rcv;
	}

	/**
	 * 
	 * @return list of receivers including only those with with latest ondate.
	 */
	public ArrayList<Receiver> getReceivers() {
		ArrayList<Receiver> rcv = new ArrayList<Receiver>(size());
		for (TreeSet<Receiver> set : values())
			rcv.add(set.last());
		return rcv;
	}

	/**
	 * Given a list of station names, return an ArrayList of Receiver objects.
	 * Station names that do not exist are simply ignored. For stations with
	 * multiple on-off dates, the one with the latest off date is returned.
	 * 
	 * @param stations
	 * @return list of receivers
	 */
	public ArrayList<Receiver> getReceivers(String... stations) {
		ArrayList<Receiver> rcv = new ArrayList<Receiver>(stations.length);
		for (String sta : stations) {
			Receiver r = getReceiver(sta);
			if (r != null)
				rcv.add(r);
		}
		return rcv;
	}

	/**
	 * Given a list of station names, return an ArrayList of Receiver objects,
	 * including only Receivers that were active on the specified date. Station
	 * names that do not exist are simply ignored.
	 * 
	 * @param date
	 * @param stations
	 * @return list of receivers
	 */
	public ArrayList<Receiver> getReceivers(Date date, String... stations) {
		ArrayList<Receiver> rcv = new ArrayList<Receiver>(stations.length);
		for (String sta : stations) {
			Receiver r = getReceiver(sta, date);
			if (r != null)
				rcv.add(r);
		}
		return rcv;
	}

	/**
	 * 
	 * @param date
	 * @return list of receivers active on the specified date
	 */
	public ArrayList<Receiver> getReceivers(Date date) {
		return getReceivers(GMTFormat.getJDate(date));
	}

	/**
	 * 
	 * @param jdate
	 * @return list of receivers active on the specified date
	 */
	public ArrayList<Receiver> getReceivers(int jdate) {
		ArrayList<Receiver> rcv = new ArrayList<Receiver>(size());
		for (TreeSet<Receiver> set : values())
			for (Receiver r : set)
				if (r.getOndate() <= jdate && r.getOffdate() >= jdate)
					rcv.add(r);
		return rcv;
	}

	/**
	 * @param sta station name
	 * @return the Receiver object with the specified station name and which has the
	 *         latest ondate.
	 */
	public Receiver getReceiver(String sta) {
		TreeSet<Receiver> values = get(sta);
		return values == null ? null : values.last();
	}

	/**
	 * 
	 * @param sta
	 * @param date
	 * @return the receiver with specified name that was active on specified date.
	 */
	public Receiver getReceiver(String sta, Date date) {
		return getReceiver(sta, GMTFormat.getJDate(date));
	}

	/**
	 * 
	 * @param sta
	 * @param jdate
	 * @return the receiver with specified name that was active on specified date.
	 */
	public Receiver getReceiver(String sta, int jdate) {
		Set<Receiver> set = get(sta);
		if (set != null)
			for (Receiver r : set)
				if (r.validJDate(jdate))
					return r;
		return null;
	}

	public boolean active(String sta, int jdate) {
		return getReceiver(sta, jdate) != null;
	}

	/**
	 * 
	 * @return number of defined Receivers including all station names and all
	 *         on-off dates.
	 */
	public int getNReceivers() {
		int n = 0;
		for (TreeSet<Receiver> values : values())
			n += values.size();
		return n;
	}

	/**
	 * 
	 * @param sta station name
	 * @return the number of Receivers with the specified station name.
	 */
	public int getNReceivers(String sta) {
		TreeSet<Receiver> values = get(sta);
		return values == null ? 0 : values.size();
	}

	/**
	 * 
	 * @param date
	 * @return number of Receivers that were active on teh specified date.
	 */
	public int getNReceivers(Date date) {
		return getNReceivers(GMTFormat.getJDate(date));
	}

	/**
	 * 
	 * @param jdate
	 * @return number of Receivers that were active on teh specified date.
	 */
	public int getNReceivers(int jdate) {
		int n = 0;
		for (TreeSet<Receiver> values : values())
			for (Receiver r : values)
				if (r.validJDate(jdate)) {
					++n;
					break;
				}
		return n;
	}

	/**
	 * Parse a list of receiver names in to an ArrayList of Receivers retaining only
	 * those that were valid on the specified Date.
	 * 
	 * @param receiverList
	 * @param date
	 * @return list of receivers
	 */
	public ArrayList<Receiver> parseReceivers(String receiverList, Date date) {
		ArrayList<Receiver> receivers = new ArrayList<Receiver>();
		for (String r : receiverList.replaceAll(",", " ").split(" "))
			if (r.trim().length() > 0)
				receivers.add(getReceiver(r.trim(), date));

		return receivers;
	}

	/**
	 * Parse a list of receiver names in to an ArrayList of Receivers retaining only
	 * the one with the latest ondate.
	 * 
	 * @param receiverList
	 * @return list of receivers
	 */
	public ArrayList<Receiver> parseReceivers(String receiverList) {
		if (receiverList.equals("all"))
			return getReceivers();

		ArrayList<Receiver> receivers = new ArrayList<Receiver>();
		for (String r : receiverList.replaceAll(",", " ").split(" "))
			if (r.trim().length() > 0)
				receivers.add(getReceiver(r.trim()));

		return receivers;
	}

	/**
	 * Search the network for receivers that have more than one entry and which have
	 * overlapping on-off dates, which is illegal. If any are found, then
	 * information is entered into the String that gets returned. If no overlapping
	 * on-off dates are found an empty string is returned.
	 * 
	 * @return
	 */
	public String analyzeOnOffDates() {
		StringBuffer buf = new StringBuffer();
		ArrayList<Receiver> r = new ArrayList<Receiver>();
		for (String sta : keySet())
			if (get(sta).size() > 1) {
				r.clear();
				r.addAll(get(sta));
				boolean overlap = false;
				for (int i = 0; i < r.size() - 1; ++i)
					for (int j = i + 1; j < r.size(); ++j)
						if (r.get(j).getOndate() >= r.get(i).getOndate()
								&& r.get(j).getOndate() <= r.get(i).getOffdate())
							overlap = true;

				if (overlap) {
					for (Receiver rr : get(sta))
						buf.append(rr.toString()).append(Globals.NL);
					buf.append(Globals.NL);
				}
			}
		return buf.toString();
	}

	/**
	 * Retrieve Site objects for all of the Receivers in this Network
	 * 
	 * @return
	 */
	public ArrayList<Site> getSites() {
		ArrayList<Site> sites = new ArrayList<Site>(2 * size());
		for (Set<Receiver> receivers : values())
			for (Receiver receiver : receivers)
				sites.add(receiver.getSiteRow());
		return sites;
	}

}
