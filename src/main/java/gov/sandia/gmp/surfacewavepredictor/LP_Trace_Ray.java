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
package gov.sandia.gmp.surfacewavepredictor;

import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import java.io.File;
import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.EarthShape;

public class LP_Trace_Ray extends SurfaceWaveModel {

	public LP_Trace_Ray(File directory, SeismicPhase phase) throws Exception {
		super(directory, phase);
	}

	@Override
	public double[] pathIntegral(GreatCircle path, double[] periods, ArrayList<double[]> points,
			ArrayList<double[]> pathVelocities) throws Exception {

		if (points != null) 
			points.clear();

		if (pathVelocities != null) 
			pathVelocities.clear();

		return LP_trace_ray (
				EarthShape.WGS84.getLatDegrees(path.getFirst()),
				EarthShape.WGS84.getLonDegrees(path.getFirst()),
				EarthShape.WGS84.getLatDegrees(path.getLast()),
				EarthShape.WGS84.getLonDegrees(path.getLast()),
				periods,
				points, pathVelocities
				);
	}

	public static boolean DEBUG = false;

	public boolean usequadinterp = false;

	/*
	 * Copyright (c) 1995-1996 Science Applications International Corporation.
	 *

	 * NAME
	 *	read_LP_info -- Read long-period (LR & LQ) grid and phase velocity info
	 *	get_LP_velocity -- Get LP phase velocity at a given period
	 *	LP_trace_ray -- Do actual tracing of long-period rays thru grid
	 *	get_LP_grid_index -- Get LP grid indice (not currently called anywhere)

	 * FILE
	 *	LP_trace_ray.c

	 * SYNOPSIS
	 *	int
	 *	read_LP_info (lp_pathway)
	 *	char	*lp_pathway;	(i) Directory pathway containing LP tables

	 *	double
	 *	get_LP_velocity (ilat, ilon, period, ph_index)
	 *	int	ilat;		(i) Latitude index
	 *	int	ilon;		(i) Longitude index
	 *	double	period;		(i) Period for which to get velocity (sec.)
	 *	int	ph_index;	(i) LP phase index (0: LR; 1: LQ)

	 *	int
	 *	LP_trace_ray (ev_lat, ev_lon, sta_lat, sta_lon, ph_index, period,
	 *		      num_periods, total_trv_time)
	 *	double	ev_lat;		(i) Geographic event latitude (deg.)
	 *	double	ev_lon;		(i) Geographic event longitude (deg.)
	 *	double	sta_lat;	(i) Geographic station latitude (deg.)
	 *	double	sta_lon;	(i) Geographic station longitude (deg.)
	 *	int	ph_index;	(i) LP phase index (0: LR; 1: LQ)
	 *	double	*period;	(i) Array of period values (sec.)
	 *	int	num_periods;	(i) Number of period samples in period array
	 *	double	*total_trv_time;(o) Array of total path travel times (sec.)

	 *	int
	 *	get_LP_grid_index (geoc_co_lat, east_lon, ph_index, azimuth)
	 *	double	*geoc_co_lat;		(i/o) Geocentric co-latitude (deg.)
	 *	double	east_lon;		(i) East longitude (deg.)
	 *	int	ph_index;		(i) LP phase index (0: LR; 1: LQ)
	 *	double	azimuth;		(i) Forward azimuth (rad.)

	 * DESCRIPTION
	 *	Functions.  All handling of long-period (LP) grid and phase velocity
	 *	tables is handled here.  Specifically, all functionality local to the 
	 *	structure, lp_data, is handled within this file.

	 *	-- read_LP_info() reads long-period (LR & LQ) grid and phase velocity
	 *	tables.  Files "must" be specified (named) as follows:
	 *	    LR grid file:	'LP_grid.LR'
	 *	    LQ grid file:	'LP_grid.LQ'
	 *	    LR velocity file:	'LP_vel.LR'
	 *	    LQ velocity file:	'LP_vel.LQ'

	 *	-- get_LP_velocity() interogates the lp_data structures for the 
	 *	long-period phase velocity for a given input period.

	 *	-- LP_trace_ray() traces a long-period ray thru grid of phase
	 *	velocities and periods (as read by, function, read_LP_info(), 
	 *	mapped on a sphere, to obtain the travel-time (sec.).  See 
	 *	ALGORITHM section below for details.

	 *	-- get_LP_grid_index() interogates the lp_data structures for the
	 *	grid index for an input geocentric co-latitude and east longitude.
	 *	Input geocentric co-latitude can only be updated if, and only if,
	 *	sample latitude and longitude fall right on a grid corner.

	 * ALGORITHM
	 *	This section provides a general algorithmic description for tracing
	 *	long-period (LP) rays over a grid mapped onto the Earth's surface 
	 *	containing different phase velocity vs. period measures.  The main
	 *	guts of this algorithm are contained in function, LP_trace_ray(),
	 *	while phase velocity data is extracted from a world-wide grid of
	 *	indexes to phases velocity, via function, get_LP_velocity().  The
	 *	general ray tracing task is accomplished as follows:

	 *	   1.	Given an event and station location (lat/lon; deg.) along
	 *		with the desired period (sec.), we determine which point
	 *		is the westernmost so that we always trace from west-to-
	 *		east.  This simplifies the overall approach, since we know
	 *		rays can never exit from the west side of a grid boundary.

	 *	   2.	Our first ray will almost always emanate from "within" a
	 *		grid cell.  Calculate distance and azimuth from westernmost
	 *		to easternmost ray points.  This is defined as the "current"
	 *		distance and azimuth.

	 *		Start of main iterative loop over all grid cells between
	 *		station and event or vice-a-versa.
	 *	   3.	We begin ray tracing from current starting point by first
	 *		calculating the distance and azimuth from this point to 
	 *		both the NE and SE corners of the current grid cell.  

	 *	   4.	If the current azimuth lies between the computed NE and SE 
	 *		azimuths, then we know the ray exits from the eastern 
	 *		boundary.  We then calculate the distance and new position 
	 *		to the east longitude grid boundary based on its latitude 
	 *		intersection.  This is our new starting point for the ray.  
	 *		Go to 6.

	 *	   5.	If the current azimuth lies between the SE corner and 180
	 *		deg. (i.e., directly due south), then our exit point will
	 *		be defined along the southern boundary of the current
	 *		grid cell.  Elsewise, the exit point will be the northern
	 *		boundary.  We then calculate the distance and new position
	 *		to the northern or southern latitude grid boundary based 
	 *		its longitude intersection.  Note, since we insist that the 
	 *		ray be traced from west-to-east, it cannot exit via the 
	 *		western boundary of the grid cell.

	 *	   6.	If the distance to the exit boundary is greater than the
	 *		"current" distance, then the final grid cell has been 
	 *		found.

	 *	   7.	Compute travel-time spent within this segment (grid cell)
	 *		of ray path.  The velocity is obtained by a call to 
	 *		function, get_LP_velocity () for the given input period.
	 *		Add this travel-time to the overall travel-time for the 
	 *		path computed thus far.  If this is the final grid cell, 
	 *		then go to 9.

	 *	   8.	Calculate a new "current" azimuth and distance from our
	 *		new starting point to the final (easternmost) point.  This 
	 *		will provide the distance for testing step 7 during the
	 *		next iteration.  Go to 3.

	 *	   9.	Ray path completely traced.  Check that sum of distances
	 *		for each individual grid cell is nearly equal (within 0.1%) 
	 *		to the original distance computed between station and event.
	 *		This check is just a verification that the ray was traced 
	 *		properly.  Return travel-time (sec.).

	 * DIAGNOSTICS
	 *	-- read_LP_info() returns an integer argument to inform the user
	 *	about local conditions encountered while attempting to read LP
	 *	tables (if they are available):

	 *	    0:	LP table were successfully read.
	 *	    1:	No LP tables available.
	 *	    2:	LP directory is empty.
	 *	   -1:	Bogus input format encountered.
	 *	   -2:	Problems encountered trying to open file.
	 *	   -3:	Error allocating memory for given structure or array.

	 *	A negative return code indicates a clear error condition.  A
	 *	positive return code simply indicates that no LP grid and velocity
	 *	files could be found, so that the default tables found in the 
	 *	normal T-T directory area will be used instead.

	 *	-- get_LP_velocity() will return with a negative velocity (namely,
	 *	-1.0) if an invalid period or latitude/longitude index is specified.

	 *	-- LP_trace_ray() will return an error code of ERR (-1) if serious
	 *	problem is encountered (e.g., a period out of range).  If everything
	 *	works as intended, this function will return a code of OK (0).

	 * FILES
	 *	-- read_LP_info() reads LP grid and phase velocity files.

	 * NOTES
	 *	A file must exists within the main travel-time table directory 
	 *	of the form, 'tab.lp_dir', where tab represents the travel-time 
	 *	table prefix name.  This pointer file specifies the directory
	 *	where the actual table are located.  get_LP_grid_index() is not
	 *	currently called anywhere, but might it be useful in the future.

	 * SEE ALSO
	 *	None.

	 * AUTHOR
	 *	Walter Nagy,  6/22/95,	Created.
	 *	Walter Nagy,  3/25/96,	LP_trace_ray() will now handle multiple periods
	 *				on a single call.  Note change to interface.
	 *
	 *  Sandy Ballard, 2/21/2025, Translated from c to java. 
	 */

	static final double M_PI = Math.PI;
	static final double M_PI_2 = Math.PI/2;
	static final double RAD_TO_DEG = (180.0/Math.PI);
	static final double DEG_TO_RAD = (Math.PI/180.0);
	static final double KM_TO_DEG = (1.0/111.195);
	static final double RADIUS_EARTH = 6371.0;
	static final double ONE_KM_IN_RAD = (1.0/RADIUS_EARTH);
	static final double SMALL_DIST = (0.01*ONE_KM_IN_RAD) /* 10 meters (radians) */;
	static final int LR = 0;
	static final int LQ = 1;
	static final int INSIDE = 1;
	static final int EAST = 2;
	static final int NORTH = 3;
	static final int SOUTH = 4;
	static final int NE	= 0;		/* Northeast corner of grid (NE) */
	static final int SE	= 1;		/* Southeast corner of grid (SE) */
	static final int WNS = 2;		/* West side of grid (NW or SW)  */

	static final double DBL_EPSILON = 1e-15;

	public double[] LP_trace_ray (double ev_lat, double ev_lon, double sta_lat, double sta_lon,
			double[] input_periods,
			ArrayList<double[]> pathPoints,
			ArrayList<double[]> velocities_by_period) throws Exception

	{

		boolean	final_grid_found = false;
		int	ilat, ilon;
		int	grid_position;
		double	co_lat, lon;
		double	lon_360;
		double	start_lon, end_lon;
		double	start_co_lat, end_co_lat;
		double	ev_co_lat = 0, sta_co_lat = 0;
		double	remainder_lat, remainder_lon;
		double[] cur_lat_bounds = new double[2];
		double[] cur_lon_bounds = new double[2];
		double cur_azi = 0.0, cur_baz, cur_delta = 0.0 ;
		double[] azi = new double[3];
		double[] baz = new double[3];
		double[] delta = new double[3];
		double	spacing, tmp;
		double	delta_to_edge, new_co_lat, new_lon;
		double	delta_diff, orig_delta;
		double	sum_of_deltas = 0.0;
		int num_periods = input_periods.length;
		double angle_wrt_azi;
		/*
		 * Added by S. Ballard, February 2025.  Java cannot pass arguments to 
		 * functions by reference, only by value.  We will use this array to get
		 * results back from functions when necessary.
		 */
		double[] output_array = new double[3];

		double[] vel = new double[num_periods];

		/*
		 * Initialize travel times to zero.
		 */
		double[] total_trv_time = new double[num_periods];

		/*
		 * To simplify matters we will always trace from west to east.
		 * Therefore, set western and eastern longitude bounds here, 
		 * start_lon and end_lon, respectively.  First, convert 
		 * geographic event and station latitudes (deg.) into geocentric 
		 * co-latitude (deg.).
		 */
		ev_co_lat = lat_conv (ev_lat, true, true, true, true, false);
		sta_co_lat = lat_conv (sta_lat, true, true, true, true, false);

		/*
		 * To avoid crossing exactly across North or South Poles add a
		 * small ammount to one of the longitudes.  If ray passes exactly
		 * thru one of the poles, then this can create some numerical 
		 * and book-keeping headaches which are not worth the trouble.
		 */

		if (ev_lon != sta_lon && abs (ev_lon - sta_lon) == 180.0)
			sta_lon += 0.01;

		/*
		 * Compute distance, azimuth and back-azimuth starting with event
		 * and ending with station.  This will be re-ordered if the
		 * station is found to be west of the event.
		 */

		geoc_distaz( ev_co_lat * DEG_TO_RAD, ev_lon * DEG_TO_RAD, sta_co_lat * DEG_TO_RAD,
				sta_lon * DEG_TO_RAD, output_array);
		cur_delta = output_array[0]; cur_azi = output_array[1]; cur_baz = output_array[2];

		orig_delta = cur_delta;

		if (cur_azi > M_PI)
		{
			start_co_lat = sta_co_lat;
			start_lon = sta_lon;
			end_co_lat = ev_co_lat;
			end_lon = ev_lon;
			tmp = cur_azi;
			cur_azi = cur_baz;
			cur_baz = tmp;
		}
		else
		{
			start_co_lat = ev_co_lat;
			start_lon = ev_lon;
			end_co_lat = sta_co_lat;
			end_lon = sta_lon;
		}

		//				spacing = lp_data[ph_index].latlon_spacing;
		//				nlat = (int) 180.0/spacing + DBL_EPSILON;	/* Avoid roundoff */
		//				nlon = (int) 360.0/spacing + DBL_EPSILON;
		spacing = super.spacing*RAD_TO_DEG;

		/*
		 * Get LP grid index for first grid cell (starting point).
		 * Start by represeting all longitude information from
		 * 0 to 360 deg.
		 */

		if (start_lon < 0.0)
			lon_360 = start_lon + 360.0;
		else
			lon_360 = start_lon;

		/*
		 * Set latitude and longitude indexes, that is, ilat and ilon, 
		 * respectively.  Note that input co-latitudes and longitudes 
		 * which fall along a boundary will be put into the next 
		 * co-latitude or longitude grid cell.  
		 */

		modf ((start_co_lat/spacing), output_array);
		remainder_lat = output_array[0]; co_lat = output_array[1];
		ilat = (int) co_lat;

		modf ((lon_360/spacing), output_array);
		remainder_lon = output_array[0]; lon = output_array[1];
		ilon = (int) lon;


		/*
		 * Special case: If both co-latitude and longitude fall on a 
		 * boundary (i.e., a corner), determine whether ray is moving
		 * into southerly (will stay in prescribed grid) or northerly
		 * (will move into latitude grid immediately above).  For stability,
		 * either add or subtract a small distance (10 m) to or from the 
		 * starting co-latitude.
		 */

		if (remainder_lat < DBL_EPSILON && remainder_lon < DBL_EPSILON)
		{
			if (cur_azi < M_PI_2)
			{
				--ilat;
				start_co_lat -= 0.0001;
				co_lat -= spacing;
			}
			else
				start_co_lat += 0.0001;
		}

		/*
		 * Save current lower (north; 0) and upper (south; 1) co-latitude
		 * and lower (west; 0) and upper (east; 1) longitude values of grid
		 * in static arrays, cur_lat_bounds and cur_lon_bounds (in radians).
		 * Also convert spacing into radians for the rest of this routine.
		 */

		cur_lat_bounds[0] = co_lat * spacing * DEG_TO_RAD;
		cur_lon_bounds[0] = lon * spacing * DEG_TO_RAD;

		spacing *= DEG_TO_RAD;

		cur_lat_bounds[1] = cur_lat_bounds[0] + spacing;
		cur_lon_bounds[1] = cur_lon_bounds[0] + spacing;
		if (cur_lon_bounds[0] > M_PI)
			cur_lon_bounds[0] -= TWO_PI;
		if (cur_lon_bounds[1] > M_PI)
			cur_lon_bounds[1] -= TWO_PI;


		/*
		 * Main iterative loop.  Rays will be trace from grid cell boundary
		 * to grid cell boundary until the complete ray path is traversed.
		 * Since the eastern boundary (EAST) will almost always be longer
		 * than the northern (NORTH) or southern (SOUTH) boundaries, we will
		 * check for it first.  Fortunately, some economies can be made
		 * here as well.  We will always start at the NE corner and inspect
		 * the SE second.  This will tells us quickly whether or not our
		 * exit ray will least the eastern boundary.  Please note that all
		 * directions are given relative to the "previous" grid cell exit
		 * points.  Therefore, an EAST grid position is actually emanating
		 * from the western edge for the current cell.  When a new edge is
		 * determined, then the direction is strictly true.  One way to 
		 * understand this choice is by realizing the direction indicates
		 * the general direction of the ray itself (specifically, whether
		 * it is more NORTH than SOUTH).  All rays are more EAST than WEST,
		 * by definition.  Please try to keep aware of this fact!
		 */


		/* Initialize starting point. */

		grid_position = INSIDE;
		co_lat = start_co_lat*DEG_TO_RAD;
		lon    = start_lon*DEG_TO_RAD;
		end_co_lat = end_co_lat*DEG_TO_RAD;
		end_lon = end_lon*DEG_TO_RAD;

		if (pathPoints != null)
			pathPoints.add(EarthShape.SPHERE.getVector(M_PI_2-co_lat, lon));

		while (final_grid_found != true)
		{
			/* Always start with NE corner, then SE */
			geoc_distaz (co_lat, lon, cur_lat_bounds[0], cur_lon_bounds[1],	output_array);
			delta[NE] = output_array[0]; azi[NE] = output_array[1]; baz[NE] = output_array[2];

			if (azi[NE] > M_PI)
			{
				azi[NE] = 0.0;
				baz[NE] = M_PI;
			}

			geoc_distaz (co_lat, lon, cur_lat_bounds[1], cur_lon_bounds[1], output_array);
			delta[SE] = output_array[0]; azi[SE] = output_array[1]; baz[SE] = output_array[2];

			if (azi[SE] > M_PI)
			{
				azi[SE] = M_PI;
				baz[SE] = 0.0;
			}
			if (cur_azi > azi[NE] && cur_azi <= azi[SE])
			{
				grid_position = EAST;

				/*
				 * Since the eastern longitude boundary defines a great
				 * circle, we only need subtract exactly 180 deg. from
				 * the back-azimuth (baz).  Note that we could avoid a 
				 * call to geoc_lat_lon() by just computing the side 
				 * coincident with the longitude great circle at the 
				 * eastern boundary.  Unfortunately, we still need to 
				 * calculate the distance from our current working 
				 * point to the edge to test if this is the final grid 
				 * cell.
				 */

				delta_to_edge = dist_given_2angles_plus_side( baz[ NE ] - M_PI, cur_azi - azi[ NE ], delta[ NE ] ) ;

				if ((cur_delta - delta_to_edge) < SMALL_DIST)
				{
					/*
					 * Final cell found!!!  We only need to compute
					 * the final travel-time segment.
					 */
					for ( int i = 0; i < num_periods ; i++ )
					{
						vel[i] = get_LP_velocity( ilat, ilon, input_periods[i]);
						total_trv_time[ i ] += ( cur_delta * RAD_TO_DEG ) / ( vel[i] * KM_TO_DEG ) ;
					}

					sum_of_deltas += cur_delta ;

					if (pathPoints != null)
						pathPoints.add(EarthShape.SPHERE.getVector(M_PI_2-end_co_lat, end_lon));
					if (velocities_by_period != null)
						velocities_by_period.add(vel.clone());

					if (DEBUG) 
						System.out.printf("LAST:   %12.6f %12.6f %12.6f %12.6f %12.6f %3d %3d%n", 
								90.0-(co_lat*RAD_TO_DEG), lon*RAD_TO_DEG, 
								90.0-(end_co_lat*RAD_TO_DEG), end_lon*RAD_TO_DEG, 
								cur_delta*RAD_TO_DEG, ilat, ilon);

					break ;
				}

				geoc_lat_lon (co_lat, lon, delta_to_edge, cur_azi, output_array);
				new_co_lat = output_array[0]; new_lon = output_array[1];

				/*
				 * Add travel-time for this segment
				 */
				for (int i = 0; i < num_periods ; i++ )
				{
					vel[i] = get_LP_velocity( ilat, ilon, input_periods[i]);
					total_trv_time[ i ] += ( delta_to_edge * RAD_TO_DEG ) / ( vel[i] * KM_TO_DEG ) ;
				}
				sum_of_deltas += delta_to_edge;

				if (pathPoints != null)
					pathPoints.add(EarthShape.SPHERE.getVector(M_PI_2-new_co_lat, new_lon));
				if (velocities_by_period != null)
					velocities_by_period.add(vel.clone());

				if (DEBUG) 
					System.out.printf("EAST:   %12.6f %12.6f %12.6f %12.6f %12.6f %3d %3d%n", 
							90.0-(co_lat*RAD_TO_DEG), lon*RAD_TO_DEG, 
							90.0-(new_co_lat*RAD_TO_DEG), new_lon*RAD_TO_DEG, 
							delta_to_edge*RAD_TO_DEG, ilat, ilon);

				/*
				 * Always re-compute new forward azimuth (cur_azi) for each
				 * new starting point.
				 */

				geoc_distaz (new_co_lat, new_lon, end_co_lat, end_lon, output_array);
				cur_delta = output_array[0]; cur_azi = output_array[1]; cur_baz = output_array[2];

				if (cur_delta < SMALL_DIST)
					break;	/* Special case: distance < 10 meters */

				co_lat = new_co_lat;
				lon = new_lon;
				cur_lon_bounds[0] = new_lon;
				cur_lon_bounds[1] = new_lon + spacing;
				if (cur_lon_bounds[1] > M_PI)
					cur_lon_bounds[1] -= TWO_PI;
				++ilon;
				if (ilon >= nlon)
					ilon -= nlon;
				continue;
			}

			/*
			 * Ray must exit from NORTH or SOUTH co-latitude boundaries!

			 * If emanating from INSIDE or EAST boundary, then determine
			 * whether exit ray will leave NORTH or SOUTH boundary.  Note
			 * that EAST exit test has already been done.
			 */

			if (grid_position == INSIDE || grid_position == EAST)
			{
				if (cur_azi > azi[SE] && cur_azi <= M_PI)
					grid_position = SOUTH;		/* Will exit to SOUTH */
				else
					grid_position = NORTH;		/* Will exit to NORTH */
			}

			if (grid_position == SOUTH)
			{
				geoc_distaz (cur_lat_bounds[1], cur_lon_bounds[1], cur_lat_bounds[1], lon, output_array);
				delta[WNS] = output_array[0]; azi[WNS] = output_array[1]; baz[WNS] = output_array[2];

				/*
				 * Protect against this ray being close to a pole and perhaps turning
				 * slightly west (negative).  If so, set to a very small number.
				 * 
				 * SB; 3/2025: The original comment is wrong.  (angle_wrt_azi <= 0) happens when the ray is 
				 * heading almost directly due east and it just grazes the top/bottom of a grid cell.  
				 * Setting angle_wrt_azi = SMALL_DIST makes the travel time prediction error smaller but does 
				 * not produce the right answer.
				 * 
				 * Here is an example ray that causes this problem:
				 * stalat=-40.732733, stalon=-70.550835, originlat=-35.839978,  originlon=179.245040
				 * Error message = Bad ray heading south. cur_azi=90.0495 azi[SE]=173.0106 azi[NE]=90.0865 angle_wrt_azi=-82.9610
				 */
				angle_wrt_azi = cur_azi - azi[ SE ];
				if (angle_wrt_azi <= 0)
					//angle_wrt_azi = SMALL_DIST;
					throw new Exception(String.format("Bad ray exiting south. cur_azi=%1.4f azi[SE]=%1.4f azi[NE]=%1.4f angle_wrt_azi=%1.4f",
						Math.toDegrees(cur_azi), Math.toDegrees(azi[SE]), Math.toDegrees(azi[NE]), Math.toDegrees(angle_wrt_azi)));

				delta_to_edge = dist_given_2angles_plus_side (baz[SE]-azi[WNS], angle_wrt_azi, delta[SE]);

				if ((cur_delta - delta_to_edge) < SMALL_DIST)
				{
					/*
					 * Final cell found!!!  We only need to compute
					 * the final travel-time segment.
					 */
					for ( int i = 0; i < num_periods ; i++ )
					{
						vel[i] = get_LP_velocity( ilat, ilon, input_periods[i]);
						total_trv_time[ i ] += ( cur_delta * RAD_TO_DEG ) / ( vel[i] * KM_TO_DEG ) ;
					}

					sum_of_deltas += cur_delta ;

					if (pathPoints != null)
						pathPoints.add(EarthShape.SPHERE.getVector(M_PI_2-end_co_lat, end_lon));
					if (velocities_by_period != null)
						velocities_by_period.add(vel.clone());

					if (DEBUG) 
						System.out.printf("LAST:    %12.6f %12.6f %12.6f %12.6f %3d %3d%n", 
								90.0-(co_lat*RAD_TO_DEG), lon*RAD_TO_DEG, 
								90.0-(end_co_lat*RAD_TO_DEG), end_lon*RAD_TO_DEG, 
								ilat, ilon);

					break ;
				}

				geoc_lat_lon (co_lat, lon, delta_to_edge, cur_azi, output_array);
				new_co_lat = output_array[0]; new_lon = output_array[1];

				/*
				 * Add travel-time for this segment
				 */
				for (int i = 0; i < num_periods ; i++ )
				{
					vel[i] = get_LP_velocity( ilat, ilon, input_periods[i]);
					total_trv_time[ i ] += ( delta_to_edge * RAD_TO_DEG ) / ( vel[i] * KM_TO_DEG ) ;
				}
				sum_of_deltas += delta_to_edge;

				if (pathPoints != null)
					pathPoints.add(EarthShape.SPHERE.getVector(M_PI_2-new_co_lat, new_lon));
				if (velocities_by_period != null)
					velocities_by_period.add(vel.clone());

				if (DEBUG) 
					System.out.printf("SOUTH:  %12.6f %12.6f %12.6f %12.6f %12.6f %3d %3d%n", 
							90.0-(co_lat*RAD_TO_DEG), lon*RAD_TO_DEG, 
							90.0-(new_co_lat*RAD_TO_DEG), new_lon*RAD_TO_DEG, 
							delta_to_edge*RAD_TO_DEG, ilat, ilon);


				/*
				 * Always re-compute new forward azimuth (cur_azi) for each
				 * new starting point.
				 */

				geoc_distaz (new_co_lat, new_lon, end_co_lat, end_lon, output_array);
				cur_delta = output_array[0]; cur_azi = output_array[1]; cur_baz = output_array[2];

				if (cur_delta < SMALL_DIST)
					break;	/* Special case: distance < 10 meters */

				co_lat = new_co_lat;
				lon = new_lon;
				cur_lat_bounds[0] = new_co_lat;
				cur_lat_bounds[1] = new_co_lat + spacing;
				++ilat;
				if (cur_lat_bounds[1] > M_PI)
				{
					cur_lat_bounds[0] = M_PI - spacing;
					cur_lat_bounds[1] = M_PI;
					if (ilat > nlat-1)
						ilat = nlat-1;		/* Wrap around S. Pole */
				}
				continue;
			}
			else	/* Exits to NORTH */
			{
				geoc_distaz (cur_lat_bounds[0], cur_lon_bounds[1], cur_lat_bounds[0], lon, output_array);
				delta[WNS] = output_array[0]; azi[WNS] = output_array[1]; baz[WNS] = output_array[2];

				/*
				 * Protect against this ray being close to a pole and perhaps turning
				 * slightly west (negative).  If so, set to a very small number.
				 * 
				 * SB; 3/2025: The original comment is wrong.  (angle_wrt_azi <= 0) happens when the ray is 
				 * heading almost directly due east and it just grazes the top/bottom of a grid cell.  
				 * Setting angle_wrt_azi = SMALL_DIST makes the travel time prediction error smaller but does 
				 * not produce the right answer.
				 * 
				 * Here is an example ray that causes this problem:
				 * stalat=33.015180, stalon=35.403110, originlat=39.912762, originlon=141.640110
				 * Error message = Bad ray heading north. cur_azi=89.9524 azi[SE]=89.9399 azi[NE]=5.4382 angle_wrt_azi=-84.5142
				 */
				angle_wrt_azi = azi[ NE ] - cur_azi;
				if (angle_wrt_azi <= 0)
					//angle_wrt_azi = SMALL_DIST;
					throw new Exception(String.format("Bad ray exiting north. cur_azi=%1.4f azi[SE]=%1.4f azi[NE]=%1.4f angle_wrt_azi=%1.4f",
						Math.toDegrees(cur_azi), Math.toDegrees(azi[SE]), Math.toDegrees(azi[NE]), Math.toDegrees(angle_wrt_azi)));


				delta_to_edge = dist_given_2angles_plus_side (azi[WNS]-baz[NE], angle_wrt_azi, delta[NE]);

				if ((cur_delta - delta_to_edge) < SMALL_DIST)
				{
					/*
					 * Final cell found!!!  We only need to compute
					 * the final travel-time segment.
					 */
					for ( int i = 0; i < num_periods ; i++ )
					{
						vel[i] = get_LP_velocity( ilat, ilon, input_periods[i]);
						total_trv_time[ i ] += ( cur_delta * RAD_TO_DEG ) / ( vel[i] * KM_TO_DEG ) ;
					}

					sum_of_deltas += cur_delta ;

					if (pathPoints != null)
						pathPoints.add(EarthShape.SPHERE.getVector(M_PI_2-end_co_lat, end_lon));
					if (velocities_by_period != null)
						velocities_by_period.add(vel.clone());

					if (DEBUG) 
						System.out.printf("LAST:    %12.6f %12.6f %12.6f %12.6f %3d %3d%n", 
								90.0-(co_lat*RAD_TO_DEG), lon*RAD_TO_DEG, 
								90.0-(end_co_lat*RAD_TO_DEG), end_lon*RAD_TO_DEG, 
								ilat, ilon);

					break ;
				}

				geoc_lat_lon (co_lat, lon, delta_to_edge, cur_azi, output_array);
				new_co_lat = output_array[0]; new_lon = output_array[1];

				/*
				 * Add travel-time for this segment
				 */
				for (int i = 0; i < num_periods ; i++ )
				{
					vel[i] = get_LP_velocity( ilat, ilon, input_periods[i]);
					total_trv_time[ i ] += ( delta_to_edge * RAD_TO_DEG ) / ( vel[i] * KM_TO_DEG ) ;
				}
				sum_of_deltas += delta_to_edge;

				if (pathPoints != null)
					pathPoints.add(EarthShape.SPHERE.getVector(M_PI_2-new_co_lat, new_lon));
				if (velocities_by_period != null)
					velocities_by_period.add(vel.clone());

				if (DEBUG) 
					System.out.printf("NORTH:  %12.6f %12.6f %12.6f %12.6f %12.6f %3d %3d%n", 
							90.0-(co_lat*RAD_TO_DEG), lon*RAD_TO_DEG, 
							90.0-(new_co_lat*RAD_TO_DEG), new_lon*RAD_TO_DEG, 
							delta_to_edge*RAD_TO_DEG, ilat, ilon);

				/*
				 * Always re-compute new forward azimuth (cur_azi) for each
				 * new starting point.
				 */

				geoc_distaz (new_co_lat, new_lon, end_co_lat, end_lon, output_array);
				cur_delta = output_array[0]; cur_azi = output_array[1]; cur_baz = output_array[2];

				if (cur_delta < SMALL_DIST)
					break;	/* Special case: distance < 10 meters */

				co_lat = new_co_lat;
				lon = new_lon;
				cur_lat_bounds[1] = new_co_lat;
				cur_lat_bounds[0] = new_co_lat - spacing;
				--ilat;
				if (cur_lat_bounds[0] < 0.0)
				{
					cur_lat_bounds[0] = 0.0;
					cur_lat_bounds[1] = spacing;
					if (ilat > 0)
						ilat = 0;		/* Wrap around N. Pole */
				}
				continue;
			}

		}	/* End of main iterative loop over grid cells */


		/*
		 * If sum of all individual distance segments is different from
		 * original distance calculation by more than 0.1%, then print
		 * an warning message.
		 */

		delta_diff = abs (orig_delta - sum_of_deltas);

		if (delta_diff > 0.001 * orig_delta)
			throw new Exception("Sum of individual LP segments differ by > 0.1% from original distance!");

		return (total_trv_time);
	}

	double get_LP_velocity (int ilat, int ilon, double period)
	{
		int	i;
		int	iper;
		int	num_per;
		double	lower_period_bound, upper_period_bound;
		double	ratio, vel_1, vel_2;
		double	velocity = -1.0;

		//		/*
		//		 * If bad latitude or longitude index is entered, return a value of
		//		 * -1.0, indicating that a problem was encountered.
		//		 *
		//		 * Except if ilon=-999 and ilat>0 interpret ilat as igrid. This is
		//		 * necessary because we need a way to return a value for a specific
		//		 * model.  We will do this a better way later - JLS, 2/15/97.
		//		 */
		//
		//		if (ilon == -999 && ilat >= 0) {
		//		    igrid = ilat;
		//		} else {
		//		  if (ilat < 0 || ilon < 0 || ilat >= lp_data[ph_index].num_lat_grids ||
		//		    ilon >= lp_data[ph_index].num_lon_grids)
		//		    return (-1.0);
		//
		//		    /* Get grid index! */
		//		    igrid = (int) lp_data[ph_index].grid_indice[ilat][ilon];
		//		}

		/*
		 * Note significant change: 1/28/97
		 * Period out of range now returns the end point value.
		 * This is necessary because calculation of a full path correction
		 * at all frequencies will always exceed the data range.
		 */
		/* Change 8/7/2002 - option to use improved quadratic interpolator */

		num_per = periods.length;
		if (usequadinterp) {
			//		    if (num_per != save_num_per) {
			//			free(periods);
			//			free(vels);
			//			periods = (float *) calloc(num_per,sizeof(float));
			//			vels    = (float *) calloc(num_per,sizeof(float));
			//			save_num_per = num_per;
			//		    }
			//		    for (i = 0, iper = 0; i < num_per; i++) {
			//			periods[i] = lp_data[ph_index].period_samples[i];
			//			vels[i]    = lp_data[ph_index].velocity[igrid][i];
			//		    }
			velocity = quadinterp2 (period, velocities[ilat][ilon], periods, num_per);
		} else {

			if (period < periods[0])
				period = periods[0];
			if (period > periods[num_per-1])
				period = periods[num_per-1];

			/* Determine upper period index for interpolation purposes */
			i = 0; iper = 0;
			while (i < num_per && period >= periods[i])
				i++;
			iper = i;
			
			if (iper == periods.length)
				--iper;

			/* Interpolate period data to get phase velocity */
			if (iper > 0)
			{
				upper_period_bound = periods[iper];
				lower_period_bound = periods[iper-1];
				ratio = (period - lower_period_bound) /
						(upper_period_bound - lower_period_bound);
				vel_1 = velocities[ilat][ilon][iper-1];
				vel_2 = velocities[ilat][ilon][iper];
				velocity = vel_1 + ratio * (vel_2 - vel_1);
			} else {

				/* Special case: Input period == largest period sample */
				velocity = velocities[ilat][ilon][num_per-1];
			}
		}   /* end of option to allow quad interp */

		return (velocity); 
	}



	void modf(double x, double[] output_array) {  
		double sign = x < 0 ? -1 : 1;  
		x = abs(x); 
		output_array[0] = sign*(x-floor(x)); 
		output_array[1] = sign*floor(x);
	}

	void geoc_lat_lon (double alat1, double alon1, double delta, double azi, double[] output_array)
	{
		double	alat, alon, b, c, coslat, dlon;
		double	r13, r13sq, sinlat, x1, x2, x3;
		//double	atan2(), cos(), sin();


		/*
		 * Convert a geographical location to geocentric cartesian 
		 * coordinates, assuming a spherical Earth.
		 */

		alat = M_PI_2 - delta;
		alon = M_PI - azi;
		r13  = cos(alat);

		/*
		 * x1:	Axis 1 intersects equator at  0 deg longitude  
		 * x2:	Axis 2 intersects equator at 90 deg longitude  
		 * x3:	Axis 3 intersects north pole
		 */

		x1 = r13*sin(alon);
		x2 = sin(alat);
		x3 = r13*cos(alon);

		/*
		 * Rotate in cartesian coordinates.  The cartesian coordinate system 
		 * is most easily described in geocentric terms.  The origin is at 
		 * the Earth's center.  Rotation by alat1 degrees southward, about 
		 * the 1-axis.
		 */

		sinlat = sin(alat1);
		coslat = cos(alat1);
		b      = x2;
		c      = x3;
		x2     = b*coslat - c*sinlat;
		x3     = b*sinlat + c*coslat;

		r13sq  = x3*x3 + x1*x1;
		r13    = sqrt(r13sq);
		//r123   = sqrt(r13sq + x2*x2);
		dlon   = atan2(x1, x3);
		double alat2 = M_PI_2 - atan2(x2, r13);
		double alon2 = alon1 + dlon;
		if (alat2 > M_PI)
			alat2 = TWO_PI - alat2;
		if (alat2 < 0.0)
			alat2 = -(alat2);
		if (abs(alon2) > M_PI)
			alon2 = SIGN((TWO_PI-abs(alon2)), alon2);

		output_array[0] = alat2;
		output_array[1] = alon2;
	}

	double SIGN(double a1, double a2) { return ((a2) >= 0 ? -(a1) : (a1)); }

	/**
	 * 
	 * @param angle_wrt_to_baz in radians
	 * @param angle_wrt_to_azi in radians
	 * @param side_between_angles in radians
	 * @return delta_to_edge in radians
	 * @throws Exception if distance to edge is negative.
	 */
	double dist_given_2angles_plus_side (double angle_wrt_to_baz, double angle_wrt_to_azi,
			double side_between_angles) throws Exception
	{

		double	A_minus_B_over_2, A_plus_B_over_2, delta_over_2;
		double	a_plus_b, a_minus_b;
		double	epsilon = DBL_EPSILON;

		A_minus_B_over_2 = (angle_wrt_to_baz - angle_wrt_to_azi) / 2.0;
		A_plus_B_over_2 = (angle_wrt_to_baz + angle_wrt_to_azi) / 2.0;
		delta_over_2 = side_between_angles / 2.0;

		if (abs(A_plus_B_over_2) < epsilon || 
				abs(A_plus_B_over_2 - M_PI_2) < epsilon)
			A_plus_B_over_2 += epsilon;

		a_plus_b = atan ((cos(A_minus_B_over_2)*tan(delta_over_2))/
				cos(A_plus_B_over_2));
		a_minus_b = atan ((sin(A_minus_B_over_2)*tan(delta_over_2))/
				sin(A_plus_B_over_2));

		return a_plus_b + a_minus_b;
	}


	void geoc_distaz (double alat1, double alon1, double alat2, double alon2, double[] output_array) {
		{
			double	clat1, cdlon, cdel, rdlon;
			double	slat1, sdlon, xazi, xbaz, yazi, ybaz;
			double	clat2, slat2;
			double	azi, baz, delta;

			/*
			 * Simple case when both sets of lat/lons are the same.
			 */

			if ((alat1 == alat2) && (alon1 == alon2))
			{
				delta = 0.0;
				azi = 0.0;
				baz = M_PI;
				output_array[0] = delta;
				output_array[1] = azi;
				output_array[2] = baz;
				return;
			}

			clat2 = cos(alat2);
			slat2 = sin(alat2);

			rdlon = alon2 - alon1;

			clat1 = cos(alat1);
			slat1 = sin(alat1);
			cdlon = cos(rdlon);
			sdlon = sin(rdlon);

			cdel = clat1*clat2 + slat1*slat2*cdlon;
			cdel = (cdel <  1.0) ? cdel :  1.0;
			cdel = (cdel > -1.0) ? cdel : -1.0;
			yazi = sdlon * slat2;
			xazi = slat1*clat2 - clat1*slat2*cdlon;
			ybaz = -sdlon * slat1;
			xbaz = slat2*clat1 - clat2*slat1*cdlon;

			delta = acos(cdel);
			azi   = atan2(yazi, xazi);
			baz   = atan2(ybaz, xbaz);

			if (azi < 0.0)
				azi += 2.0*M_PI;
			if (baz < 0.0)
				baz += 2.0*M_PI;

			output_array[0] = delta;
			output_array[1] = azi;
			output_array[2] = baz;
		}
	}



	/*
	 * Copyright (c) 1994-1997 Science Applications International Corporation.
	 *

	 * NAME
	 *	lat_conv -- Convert from/to geographic to/from geocentric latitude.

	 * FILE
	 *	lat_conv.c

	 * SYNOPSIS
	 *	double
	 *	lat_conv (double lat, Bool geog_to_geoc, Bool in_deg, Bool out_deg, 
	 *		  Bool in_lat, Bool out_lat)

	 * DESCRIPTION
	 *	Functions.  This routine is used to convert between geographic and 
	 *	geocentric latitudes.  Input and output latitude information can be
	 *	geographic or geocentric, in radians or degrees and/or represented 
	 *	as latitude or co-latitude.

	 *	---- On entry ----
	 *	lat:		Input latitude (deg/rad; lat/colat)
	 *	geog_to_geoc:	Convert from geographic to geocentric latitude (TRUE)
	 *			or geocentric to geographic latitude (FALSE)?
	 *	in_deg:		Is input latitude in degrees (TRUE) or radians (FALSE)?
	 *	out_deg:	Should output latitude be in degrees (TRUE) or 
	 *			radians (FALSE)?
	 *	in_lat:		Is input latitude represented as latitude (TRUE) or
	 *			co-latitude (FALSE).
	 *	out_lat:	Should returned latitude be represented as latitude 
	 *			(TRUE) or co-latitude (FALSE).

	 *	---- On return ----
	 *	Return value:	Output latitude (geog/geoc; deg/rad; lat/colat)

	 * DIAGNOSTICS
	 *	None.

	 * FILES
	 *	None.

	 * NOTES
	 *	Normal latitude is zero at equator and positive North.  Co-latitude 
	 *	is zero at North Pole and positive to the south.

	 * SEE ALSO
	 *	None.

	 * AUTHOR
	 *	Walter Nagy, June 1994.
	 */


	double lat_conv (double lat, boolean geog_to_geoc, boolean in_deg, boolean out_deg, 
			boolean in_lat, boolean out_lat)
	{
		double	glat, glat_gc;


		/*
		 * Is input latitude in degrees?  If so, convert to radians.
		 */

		if (in_deg)
			glat = lat*DEG_TO_RAD; /* Latitude (radians) */
		else
			glat = lat;	       /* Latitude (radians) */

		/*
		 * Is input geographic latitude or co-latitude?  Make sure it is
		 * converted to geocentric co-latitude (in radians), if not already
		 * true.
		 */

		if (in_lat)
			glat = M_PI_2 - glat;	/* Co-latitude (radians) */

		/*
		 * Do geographic to geocentric co-latitude conversion if geog_to_geoc 
		 * is set TRUE; else do geocentric to geographic co-latitude conversion.
		 */

		if (geog_to_geoc)
		{
			glat_gc	= GEOCENTRIC_COLAT(glat);
			glat	= glat_gc;	/* Geocentric co-latitude (radians) */
		}
		else
		{
			glat_gc	= GEOGRAPHIC_COLAT(glat);
			glat	= glat_gc;	/* Geographic co-latitude (radians) */
		}

		if (out_lat)
			glat = M_PI_2 - glat;  	/* Converted latitude (radians) */

		if (out_deg)
			glat = glat*RAD_TO_DEG;	/* Converted lat/co-lat (radians) */

		return (glat);
	}

	double GEOCENTRIC_COLAT(double x) { return (x + (((0.192436*sin(x+x)) + (0.000323*sin(4.0*x)))*DEG_TO_RAD)); }
	double GEOGRAPHIC_COLAT(double x) {return ( x - (((0.192436*sin(x+x)) - (0.000323*sin(4.0*x)))*DEG_TO_RAD)); }


	double quadinterp2 (double x, double[] y0, double[] x0, int n0)
	{
		// S. Ballard 2/2025, changed floats to doubles.
		int    i;
		double  x1, x2, x3;
		double  y1, y2, y3;
		double  y;
		double ylower,yupper;
		double dfrac;

		if (x <= x0[0]) 
			return y0[0];
		if (x >= x0[n0-1]) 
			return y0[n0-1];
		for (i = 0; i < n0; i++) 
			if (x <= x0[i]) 
				break;
		ylower = 0.0;
		yupper = 0.0;

		/* i is next point past x */
		if (i != n0-1) {
			/* First use upper points */
			x1 = x0[i-1]; 
			y1 = y0[i-1];
			x2 = x0[i]; 
			y2 = y0[i];
			x3 = x0[i+1]; 
			y3 = y0[i+1];
			yupper = ycalc (x, x1, x2, x3, y1, y2, y3);
		}
		if (i != 1) {
			/* Second use lower points */
			x1 = x0[i-2]; 
			y1 = y0[i-2];
			x2 = x0[i-1]; 
			y2 = y0[i-1];
			x3 = x0[i]; 
			y3 = y0[i];
			ylower = ycalc (x, x1, x2, x3, y1, y2, y3);
		}

		/* Then do a weighted average of the two */
		if (i == 1) {
			y = yupper;
		} else if (i == (n0-1)) {
			y = ylower;
		} else {
			dfrac = (x-x0[i-1])/(x0[i]-x0[i-1]);
			y = yupper*dfrac+ylower*(1.0-dfrac);
		}

		return (y);
	}

	static double ycalc (double x, double x1, double x2, double x3, 
			double y1, double y2, double y3)
	{
		double y;
		double dx, dy;
		double dx32, dx12;
		double dy32, dy12;
		double a, b, denom;

		dx    = x-x2;
		dx12  = x1-x2;
		dx32  = x3-x2;
		//dx31  = x3-x1;
		denom = dx32*dx12*dx12-dx12*dx32*dx32;
		dy12  = y1-y2;
		//dy31  = y3-y1;
		dy32  = y3-y2;
		a     = (dy12*dx32-dx12*dy32)/denom;
		b     = (dy32*dx12*dx12-dy12*dx32*dx32)/denom;
		dy    = a*dx*dx+b*dx;
		y     = (y2+dy);

		return(y);
	}
}
