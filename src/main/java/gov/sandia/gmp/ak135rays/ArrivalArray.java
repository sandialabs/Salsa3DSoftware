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
package gov.sandia.gmp.ak135rays;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TimeDist;
import gov.sandia.gmp.util.globals.Globals;

/**
 * Contains methods used to determine which taup arrival corresponds with the user requested arrival
 * based on phase requested and which taupArrival is fastest and/or deepest and/or furthest
 * Original author sballard; modified by acconle
 * @author sballar
 */
public class ArrivalArray implements List<Arrival>
{
	/**
	 * All the arrivals
	 */
	Arrival[] arrivals;

	double[] turn_depth;

	/**
	 * arrivals with turning depths greater than this will be ignored
	 */
	double minDepth;

	/**
	 * arrivals with turning depths greater than this will be ignored
	 */
	double maxDepth;

	/**
	 * Constructor
	 * 
	 * @param arrivals
	 */
	ArrivalArray(Arrival[] arrivals, double minDepth, double maxDepth)
	{
		this.arrivals = arrivals;
		this.minDepth = minDepth;
		this.maxDepth = maxDepth;

		turn_depth = new double[arrivals.length];
		Arrays.fill(turn_depth, -1.);
	}

	public ArrivalArray(List<Arrival> arrivals, double minDepth, double maxDepth) 
	{
		this(arrivals.toArray(new Arrival[arrivals.size()]), minDepth, maxDepth);
	}

	public int size()
	{
		return arrivals.length;
	}

	public Arrival get(int index)
	{
		return arrivals[index];
	}

	/**
	 * Retrieve reference to the fastest arrival. There is one complication
	 * which is that if the fastest is Pdiff or Sdiff and there are any core
	 * phases (PKxxx or SKxxx) then the core phases will trump the
	 * diffraction.
	 * 
	 * @return
	 */
	int getFastest()
	{
		int ifastest = -1;
		for (int j = 0; j < arrivals.length; ++j)
			if (getTurnDepth(j) >= minDepth && getTurnDepth(j) <= maxDepth)
			{
				if (ifastest < 0
						|| arrivals[ifastest].getName().equals("Pdiff")
						&& arrivals[j].getName().startsWith("PK")
						|| arrivals[ifastest].getName().equals("Sdiff")
						&& arrivals[j].getName().startsWith("SK")
						|| arrivals[j].getTime() < arrivals[ifastest]
								.getTime())
					
					ifastest = j;
			}
		return ifastest;
	}

	/**
	 * Retrieve reference to the slowest arrival.
	 * 
	 * @return
	 */
	int getSlowest()
	{
		int islowest = -1;
		for (int j = 0; j < arrivals.length; ++j)
			if (getTurnDepth(j) >= minDepth && getTurnDepth(j) <= maxDepth)
			{
				if (islowest < 0
						|| arrivals[j].getTime() > arrivals[islowest]
								.getTime())
					islowest = j;
			}
		return islowest;
	}

	/**
	 * Get reference to arrival with deepest turning point.
	 * 
	 * @return
	 */
	int getDeepest()
	{
		int ideepest = -1;
		double maxturndepth = 0;
		for (int j = 0; j < arrivals.length; ++j)
		{
			if (getTurnDepth(j) >= minDepth && getTurnDepth(j) <= maxDepth)
			{
				if (ideepest < 0)
					ideepest = j;
				else if (getTurnDepth(j) > maxturndepth)

				{
					maxturndepth = getTurnDepth(j);
					ideepest = j;
				}
			}
		}
		return ideepest;
	}

	double getTurnDepth(int index)
	{
		if (turn_depth[index] < 0.)
			for (int i = 0; i < arrivals[index].getNumPiercePoints(); ++i)
				if (arrivals[index].getPiercePoint(i).getDepth() > turn_depth[index])
					turn_depth[index] = arrivals[index].getPiercePoint(i).getDepth();
		return turn_depth[index];
	}

	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < arrivals.length; ++i)
		{
			buf.append(String.format("Arrival %3d %s:%n", i,
					arrivals[i].getName()));
			for (int j = 0; j < arrivals[i].getNumPiercePoints(); ++j)
				buf.append(pierceToString(arrivals[i].getPiercePoint(j)))
						.append(Globals.NL);
			buf.append(String.format("%n"));
		}
		return buf.toString();
	}

	String toString(int i)
	{
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < arrivals[i].getNumPiercePoints(); ++j)
			buf.append(pierceToString(arrivals[i].getPiercePoint(j)))
					.append(Globals.NL);

		return buf.toString();
	}

	//Print out distance increments in degrees, depth in km, ray parameter in (s/radian), and time increments in seconds
	String pierceToString(TimeDist td)
	{
		return String.format("dist=%9.3f z=%8.3f p=%8.3f t=%8.3f",
				Math.toDegrees(td.getDistDeg()), td.getDepth(), td.getP(), td.getTime());

	}

	@Override
	public boolean add(Arrival e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void add(int arg0, Arrival arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addAll(Collection<? extends Arrival> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends Arrival> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Iterator<Arrival> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<Arrival> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<Arrival> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Arrival remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Arrival set(int arg0, Arrival arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Arrival> subList(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}
}
