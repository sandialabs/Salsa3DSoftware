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
package gov.sandia.gmp.lookupdz.distanceslowness;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import gov.sandia.gmp.lookupdz.LookupTable;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.brents.Brents;

/**
 * A model that manages a 2D lookup table of slowness for a single phase, as a function of distance and depth.
 * The purpose of the model is to be able to extract a curve of slowness as a function of distance
 * at a single depth and use that curve to determine source-receiver distance from a single slowness
 * value using Brents Method.
 */
public class DistanceSlownessModel {

	static public void main(String[] args) {
		try {
			File dir = new File("/Users/sballar/git/seismic-base-data/src/main/resources");

			DistanceSlownessModel.SURFACE_ONLY = false;

			DistanceSlownessModel dsm = new DistanceSlownessModel(new File(dir, "tt_ak135_Sn"));

			int z = 0;

			for (int i=0; i<dsm.distances.length; ++i)
				System.out.printf("%3d\t %10.2f\t %10.4f%n", i, dsm.distances[i], dsm.slowness[z][i]);

			System.out.println ();

			double[][] ds = dsm.getDistanceSlownessCurve(dsm.depths[z]);

			for (int i=0; i<ds[0].length; ++i)
				System.out.printf("%3d\t %10.2f\t %10.4f%n", i, ds[0][i], ds[1][i]); 


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * If SURFACE_ONLY is false, then the entire 2D slowness model (function of distance and depth)
	 * as well as the distance and depth arrays are retained after the constructor is called.  
	 * This allows distance as a function of slowness to be computed for any depth in the depth range.
	 * It also allows calculation of RMS difference between original slowness values and processed 
	 * slowness values, as a function of depth (see method <code>getRMS()</code>).
	 * <p>If SURFACE_ONLY is true, then a processed slowness vs distance curve for zero depth is computed and a 
	 * reference to it saved in this model.  All the rest of the information is discarded.
	 * This is very efficient in terms of memory.
	 */
	public static boolean SURFACE_ONLY = true;

	/**
	 * If this is false, then input slowness values that are out of range of the slowness curve
	 * will throw an exception.  If true, then returned distance will be set to distance range 
	 * limit when input slowness is outside slowness range limits.
	 */
	public static boolean ALLOW_SLOWNESS_OUT_OF_RANGE = true;

	/**
	 * distances, in degrees, at which values are stored in tables
	 */
	protected double[] distances;

	/**
	 * depths, in km, at which values are stored in tables
	 */
	protected double[] depths;

	/**
	 * slowness values at various distances and depths, in seconds/degree.
	 * First index spans depths, second index spans distances.
	 */
	protected double[][] slowness;

	/**
	 * true indicates that the original slowness is negative.  This indicates that the ray approached the station 
	 * from a source more than 180 degrees away.  When true, distance is changed to 360-distance
	 * and slowness is set to positive.
	 */
	private boolean negative_slowness;

	/**
	 * Two arrays, distance in degrees and slowness in seconds/degree computed at depth = 0 km.
	 * The slowness values are guaranteed to be not-NaN and monotonically either increasing or decreasing.
	 */
	protected double[][] distance_slowness_0;

	/**
	 * Brents object used to find distance as a function of slowness.
	 */
	private Brents brents;

	public DistanceSlownessModel(File modelFile) throws Exception {
		LookupTable ttTable = new LookupTable(modelFile);
		if (SURFACE_ONLY)
			this.depths = new double[] {ttTable.getDepths()[0]};
		else
			this.depths = ttTable.getDepths();

		this.distances = ttTable.getDistances();
		this.slowness = new double[depths.length][distances.length];
		double[] results = new double[6];

		// iterate over all depths and distances and compute slowness in sec/deg at the 
		// same locations as the traveltime values.  Use NaN for all invalid values.
		for (int i=0; i<depths.length; ++i) {
			for (int j=0; j<distances.length; ++j) {
				if (ttTable.getValues()[i][j] >= 0) {
					int code = ttTable.interpolate(distances[j], depths[i], false, false, false, results);
					slowness[i][j] = code == 0 ? results[1] : Double.NaN;
				}
				else
					slowness[i][j] = Double.NaN;
			}
		}

		// precompute and cache distance as a function of slowness for depth 0 km
		// because it is used most (maybe even exclusively).
		distance_slowness_0 = getDistanceSlownessCurve(0.);

		if (SURFACE_ONLY) {
			this.distances = null;
			this.depths = null;
			this.slowness = null;
		}

		brents = new Brents();
		brents.setTolerance(1e-3);
	}

	/**
	 * Retrieve the distance in degrees as a function of slowness for depth = 0.
	 * @param slowness in seconds/degree
	 * @return distance in degrees.
	 * @throws Exception 
	 */
	public double getDistance(double slowness) throws Exception {
		return getDistance(slowness, 0.);
	}

	/**
	 * Retrieve the distance in degrees as a function of slowness and event depth
	 * @param slowness in seconds/degree
	 * @param depth event depth in km.
	 * @return distance in degrees.
	 * @throws Exception 
	 */
	public double getDistance(double slowness, double depth) throws Exception {
		double[][] curve = getDistanceSlownessCurve(depth);

		if (ALLOW_SLOWNESS_OUT_OF_RANGE) {
			double[] dist = curve[0];
			double[] slow = curve[1];
			int n = dist.length-1;

			if (slow[0] >= slow[n]) {
				if (slowness >= slow[0])
					return dist[0];
				if (slowness <= slow[n])
					return dist[n];
			}
			else  {
				if (slowness <= slow[0])
					return dist[0];
				if (slowness >= slow[n])
					return dist[n];
			}
		}

		BrentsObject bObj = new BrentsObject(curve, slowness);
		return brents.zeroF(bObj.getMinDistance(), bObj.getMaxDistance(), bObj);
	}

	/**
	 * Get Slowness as a function of distance at specified depth
	 * @param depth
	 * @return 2 arrays, the first is distance in degrees and the second is slowness
	 * in seconds/degree.  All slowness values are guaranteed to be valid and to be
	 * monotonically increasing or decreasing.
	 * @throws Exception 
	 */
	public double[][] getDistanceSlownessCurve(double depth) throws Exception {
		if (Math.abs(depth) < 1e-6 && distance_slowness_0 != null)
			return distance_slowness_0;

		// find all the valid (not NaN) slowness values and associated distances
		// at the specified depth.
		List<Double> dist = new LinkedList<Double>();
		List<Double> slow = new LinkedList<Double>();
		int z = Globals.hunt(depths, depth);
		if (z >= 0 && (z < depths.length-1) || SURFACE_ONLY) {
			ArrayListDouble d = new ArrayListDouble(distances.length);
			ArrayListDouble s = new ArrayListDouble(distances.length);
			if (Math.abs(depth-depths[z]) < 1e-6) {
				for (int i=0; i<distances.length; ++i) 
					if (!Double.isNaN(slowness[z][i]))
					{
						d.add(distances[i]);
						s.add(slowness[z][i]);
					}
			}
			else if (Math.abs(depth-depths[depths.length-1]) < 1e-6) {
				for (int i=0; i<distances.length; ++i) 
					if (!Double.isNaN(slowness[depths.length-1][i]))
					{
						d.add(distances[i]);
						s.add(slowness[depths.length-1][i]);
					}
			}
			else {
				double c = (depth-depths[z])/(depths[z+1]-depths[z]);
				for (int i=0; i<distances.length; ++i) {
					double x = slowness[z][i]*(1-c)+slowness[z+1][i]*c;
					if (!Double.isNaN(x)) {
						d.add(distances[i]);
						s.add(x);
					}
				}
			}

			if (s.size() < 2)
				throw new Exception("Insufficient valid slowness values");

			// increasing will = 1 when slowness decreases as a function of distance.
			int increasing = s.get(0) > s.getLast() ? 1 : -1;

			// add the last valid distance and slowness to the lists.
			dist.add(d.getLast());
			slow.add(s.getLast());

			// iterate over all valid slowness data in reverse order of distance.
			// If slowness increases, add distance and slowness to the beginning of the list
			// Skip slowness and distance values where slowness decreases as a function of 
			// decreasing distance.
			for (int i=d.size()-2; i >= 0; --i) {
				if (increasing*(s.get(i)-slow.get(0)) > 0) {
					dist.add(0, d.get(i));
					slow.add(0, s.get(i));
				}
			}

			dist.set(0, d.get(0));

			// copy the data from LinkedLists to double arrays
			double[] dd = new double[dist.size()];
			double[] ss = new double[dist.size()];

			if (slow.get(slow.size()/2) >= 0) {
				// since slownesses are all positive, the ray traveled less than 180 degrees
				negative_slowness = false;
				for (int i=0; i<dist.size(); ++i) {
					dd[i] = dist.get(i);
					ss[i] = slow.get(i);
				}
			}
			else {
				// since slownesses are all negative, the ray traveled more than 180 degrees.
				// We need to set distances to 360 - distance and set slowness positive
				negative_slowness = true;
				for (int i=0; i<dist.size(); ++i) {
					dd[i] = 360-dist.get(dist.size()-1-i);
					ss[i] = -slow.get(slow.size()-1-i);
				}
			}

			return new double[][] {dd, ss}; 
		}
		throw new Exception(String.format("Depth %1.3f is out of range [%1.3f - %1.3f]",
				depth, depths[0], depths[depths.length-1]));
	}

	/**
	 * get the root-mean-squared difference between the original slowness values computed in the 
	 * constructor at the same points as sthe traveltimes, and slowness values contained the distance-slowness
	 * array.  The latter differs from the former in that in the latter all distance-slowness pairs where 
	 * slowness decreases as a function of decreasing distance are omitted.
	 * @param z integer index of the depth in the depths array where the rms is to be computed.
	 * @return
	 * @throws Exception
	 */
	public double getRMS(int z) throws Exception {

		// if the slowness values computed in the constructor have been discarded, then 
		// throw an exception.  To prevent this from happening set the static boolean value
		// SURFACE_ONLY = false prior to calling the constructor.
		if (slowness == null)
			throw new Exception("Cannot compute RMS because double[][] slowness is null.  To overcome this\n"
					+ "limitation, set public static boolean SURFACE_ONLY = false before calling the "
					+ "constructor.");

		double[][] ds = getDistanceSlownessCurve(depths[z]);

		double rms = 0;
		int n = 0;
		for (int i=0; i<distances.length; ++i) {
			double d = negative_slowness ? 360.-distances[i] : distances[i];
			double s = negative_slowness ? -slowness[z][i] : slowness[z][i];
			if (!Double.isNaN(s)) {
				rms += Globals.sqr(getSlow(ds, d) - s); 
				++n;
			}
		}
		return Math.sqrt(rms/n);
	}

	/**
	 * Interpolate the value of slowness at the specified distance.
	 * @param ds a 2D array [2][distance.length].  The first element includes distance values in km,
	 * and the second element includes slowness values in sec/deg.
	 * @param dist
	 * @return
	 * @throws Exception
	 */
	public double getSlow(double[][] ds, double dist) throws Exception {
		int i=Globals.hunt(ds[0], dist, false, false);
		if (i < 0 || i >= ds[0].length-1)
			return Double.NaN;
		double c = (dist-ds[0][i])/(ds[0][i+1]-ds[0][i]);
		return ds[1][i]*(1.-c) + ds[1][i+1]*c;
	}

}
