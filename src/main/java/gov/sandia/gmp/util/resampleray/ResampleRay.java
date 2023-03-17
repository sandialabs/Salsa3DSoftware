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
package gov.sandia.gmp.util.resampleray;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;

import java.util.ArrayList;
import java.util.ListIterator;

import gov.sandia.gmp.util.numerical.vector.Vector3D;

public class ResampleRay {

	/**
	 * Given a list of points (3D vectors, not unit vectors) that define a ray path, resample the path
	 * so that the new points are separated by approximately dkm (in km).  Note that dkm will be adjusted downward such
	 * that the spacing between adjacent samples is constant.  
	 * <p>ASSUMPTION: the distance from the first point to each other point is monotonically increasing.
	 * @param points
	 * @param dkm desired separation of samples along the ray, in km.  Actual value will be less so that
	 * that spacing of samples will be approximately constant.
	 * @param testResults if true, a test is run to ensure that samples are equally spaced and that
	 * every sample resides between two points in a colinear manner.  This test is expensive and should only
	 * be run when developing an algorithm that uses this resample method to gain confidence that the algorithm
	 * is producing valid results.
	 * @return a list of equally spaced 3D vectors that lie on the path.
	 * @throws Exception 
	 */
	public static ArrayList<double[]> resample(ArrayList<double[]> points, double dkm, boolean testResults) throws Exception {

	    //System.out.println();
	    //System.out.println(dkm);

	    // On first call to resample_private, maxsamples is unlimited.
	    // On subsequent calls, maxsamples will be limited to the number of samples
	    // retrieved on this first call.
	    ArrayList<double[]> samples = resample_private(points, dkm, Integer.MAX_VALUE);
	    if (samples.size() > 2)
	    {
		double dkm_error = Vector3D.distance3D(samples.get(samples.size()-2), samples.get(samples.size()-1))-dkm;
		//System.out.printf("%6d %10.4f %12.6f%n", samples.size(), dkm, dkm_error);
		
		// While the length of the last interval is not right, adjust dkm and call resample_private again.
		while (Math.abs(dkm_error/dkm) > 1e-3)
		{
		    dkm += dkm_error / (samples.size()-1);
		    samples = resample_private(points, dkm, samples.size());
		    dkm_error = Vector3D.distance3D(samples.get(samples.size()-2), samples.get(samples.size()-1))-dkm;
		    //System.out.printf("%6d %10.4f %12.6f%n", samples.size(), dkm, dkm_error);
		}
	    }
	    
	    // test the samples to ensure that they are equally spaced and that each sample resides
	    // between two points in a colinear manner.
	    // TODO: this test is pretty expensice.  It should only be performed during development of an 
	    // an algorithm that uses resample() method to ensure that valid samples are computed.
	    if (testResults) testSamples(points, samples);
	    return samples;
	}

	/**
	 * Given a list of points (3D vectors, not unit vectors) that define a ray path, resample the path
	 * so that the new points are separated by dkm (in km).  The separation of the last two samples will
	 * be arbitrarily small but > 0 and < dkm.
	 * <p>ASSUMPTION: the distance from the first point to each other point is monotonically increasing.
	 * @param points
	 * @param dkm
	 * @return a list of 3D vectors that lie on the path and are dkm km apart (except the last pair).
	 * @throws Exception 
	 */
	private static ArrayList<double[]> resample_private(ArrayList<double[]> points, double dkm, int maxsamples) throws Exception {
	    
	    // test assumption that distance from first point is monotonically increasing
	    double d = 0, dp;
	    for (int i=1; i<points.size(); ++i)
	    {
		dp = Vector3D.distance3D(points.get(0), points.get(i));
		if (dp <= d)
		    throw new Exception("Points are not monotonically increasing in distance from first point.");
		d = dp;
	    }
	    
	    ArrayList<double[]> samples = new ArrayList<double[]>(2);
	    
	    // if separation of first and last points is <= dkm, add first and last points to samples and return
	    if (Vector3D.distance3D(points.get(points.size()-1), points.get(0)) <= dkm)
	    {
		samples.add(points.get(0));
		samples.add(points.get(points.size()-1));
		return samples;
	    }
	    
	    // ensure capacity of samples is big enough to hold all the samples that will be generated.
	    if (maxsamples < Integer.MAX_VALUE)
		samples.ensureCapacity(maxsamples);
	    else
	    {
		double pathlength = 0;
		for (int i=1; i<points.size(); ++i)
		    pathlength += Vector3D.distance3D(points.get(i-1), points.get(i));

		samples.ensureCapacity((int)Math.ceil(pathlength/dkm)+1);
	    }

	    ListIterator<double[]> iterator = points.listIterator();

	    // get 3 3D vectors.  u is a new sample along the ray, 
	    // p is the previous point on the ray and n is the next point on the ray
	    double[] u, p, n;

	    u = p = iterator.next();
	    samples.add(u);

	    // distance in km from u to p.
	    double dup = 0;

	    n = iterator.next();
	    
	    // distance in km from u to n
	    double dun = Vector3D.distance3D(u, n);

	    boolean done = false;

	    while (!done)
	    {
		// increment p and n until dun is >= dkm
		while (dun < dkm)
		{
		    // check is we have reached the end of the path.  If so, its time to quit.
		    if (!iterator.hasNext())
		    {
			// while number of samples it too large, delete the last sample.
			while (samples.size() > maxsamples-1)
			    samples.remove(samples.size()-1);
			// add the last point.
			samples.add(n);			
			done = true;
			break;
		    }
		    p = n;
		    dup = dun; 
		    n = iterator.next();
		    dun = Vector3D.distance3D(u, n);
		}
		if (done) break;
		
		double dpn = Vector3D.distance3D(p, n);
		
		// find angle an using law of cosines
		double an = acos(min(1., max(-1., (dpn*dpn + dun*dun - dup*dup) / (2.*dpn*dun))));
		
		// find distance from v (next sample on the ray) to n
		double dvn = 0;
		if (an == 0.)
		{
		    // u, p and n are colinear
		    // distance from v to n is distance from u to n - dkm
		    dvn = dun - dkm;
		}
		else
		{
		    // u, p and n are not colinear
		    // use law of sines to find obtuse angle av.
		    double av = PI - asin(dun * sin(an) / dkm);
		    // use the fact that sum of angles in a triable is equal to PI to find au
		    double au = PI - an - av;
		    // use law of sines to find length of side from v to n.
		    dvn = dkm * sin(au)/sin(an);
		}
		
		// find ratio of distance from (v to n) to (p to n) 
		double f = max(0., min(1., dvn / dpn));
		
		// find 3D vector v located fractional distance between p and n
		double[] v = new double[] {p[0]*f+n[0]*(1-f), p[1]*f+n[1]*(1-f), p[2]*f+n[2]*(1-f)};

		samples.add(v);
		p = u = v;
		dup = 0;
		dun = dvn;
	    }
	    return samples;
	}
	
	/**
	 * Test the samples to ensure that they are equally spaced and that each sample resides between two points 
	 * in a colinear manner.  
	 * This test is pretty expensive.  It should only be performed during development of an algorithm 
	 * that uses the resample() method to ensure that valid samples are computed.
	 * @param points
	 * @param samples
	 * @throws Exception
	 */
	private static void testSamples(ArrayList<double[]> points, ArrayList<double[]> samples) throws Exception
	{
	    double dkm = Vector3D.distance3D(samples.get(0), samples.get(1));

	    // assert that the separation of every pair of adjacent samples is equal to dkm;
	    for (int i=1; i<samples.size(); ++i)
	    {
		double d = Vector3D.distance3D(samples.get(i-1), samples.get(i));
		if (abs(1.-d/dkm) > 1e-2)
		    throw new Exception("samples are not equally spaced. "+d+" != "+dkm);
	    }

	    // make sure that every sample is either colocated with one of the points,
	    // or is located on the line connecting a pair of adjacent points.
	    for (int i=0; i<samples.size(); ++i)
	    {
		boolean onray = false;
		for (int j=1; j<points.size()-1; ++j)
		    if (inline(points.get(j-1), samples.get(i), points.get(j))) {
			onray = true;
			break;
		    }
		if (!onray) 
		    throw new Exception("sample not colinear with points");
	    }
	}

	/**
	 * returns true iff v is on the straight line connecting u and w.
	 * u, v and w are full 3D vectors, not unit vectors.
	 * @param u
	 * @param v
	 * @param w
	 * @return
	 */
	private static boolean inline(double[] u, double[] v, double[] w) {
	    double[] x = Vector3D.subtract(u,v);
	    if (Vector3D.normalize(x) < 1e7)
		return true;
	    double[] y = Vector3D.subtract(w, v);
	    if (Vector3D.normalize(y) < 1e7)
		return true;
	    return Vector3D.dot(x, y) < -0.999999;
	}



}
