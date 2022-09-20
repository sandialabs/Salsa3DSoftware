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
package gov.sandia.gmp.bender.visualize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.bender.Bender;
import gov.sandia.gmp.bender.BenderConstants.RayStatus;
import gov.sandia.gmp.bender.phase.PhaseLayerLevelDefinition;
import gov.sandia.gmp.bender.ray.Ray;
//X import gov.sandia.gmp.geomodel.GeoModelException;
//X import gov.sandia.gmp.geomodel.InterpolatedNodeLayered;
//X import gov.sandia.gmp.geomodel.Phase;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;

// Other additions would be nice
//
//    Using circular geometry (2D) to plot the points and interfaces
//
//    Turning on off markers for Source, Receiver, Interface nodes, segment
//       interior nodes, TOP and BOTTOM fixed reflection nodes.
//    Coloring a ray by segments (every other segment is different color)
//    Coloring a ray by branches (5 colors) (7 if you color refraction up and
//      down branches)
//    Coloring a ray by wave type (2 colors)
//
//    A twoD plot looking down on the source to reciever plane showing the
//    segment interface nodes (first and last) in an out-of-plane sense. This
//    would be useful for seeing how much out-of plane there is in a ray

public class RayVisualizerJFree implements ChangeListener {

	//X private RayVisualizerChartFrame view;
	private RayVisualizerChartFrame view;
	//X private Bender bender;
	private Bender bender;
	private PhaseLayerLevelDefinition pld;
	private Set<RayStatus> rayStatus;
	private int bottomLayer = Integer.MIN_VALUE;
	private int topLayer = Integer.MAX_VALUE;
	private GreatCircle greatCircle;
	private int currentDepth;
	private boolean initialAutoRange = true;

	//X public RayVisualizerJFree(Bender bender) {
	public RayVisualizerJFree(Bender bender) {
		this.bender = bender;
		this.view = new RayVisualizerChartFrame();
		this.rayStatus = new HashSet<RayStatus>();
	}

	public void open(GeoVector receiver, GeoVector source,
			PhaseLayerLevelDefinition pld, RayStatus... rayStatus)
			throws Exception {
		if (rayStatus.length <= 0)
			setRayStatusList(RayStatus.INITIAL_RAY, RayStatus.INNER_LOOP,
					RayStatus.BENT, RayStatus.SNELL, RayStatus.DOUBLED,
					RayStatus.OUTER_LOOP, RayStatus.FINAL_RAY, RayStatus.FASTEST_RAY);
		else
			setRayStatusList(rayStatus);

		this.pld = bender.getPhaseLayerLevelDefinition();
		this.greatCircle = new GreatCircle(receiver.getUnitVector(), source.getUnitVector());
		double d = receiver.distanceDegrees(source);
		int dist = Math.max((int) Math.ceil(d), 1);
		this.view.setXAxis(0, dist);
		
		this.currentDepth = 700;
		this.view.setYAxis(0, currentDepth);
		this.view.display();
		this.plotInterfaces(d);
	}

	private void plotInterfaces(double dist) throws Exception
	{
		int nPoints = Math.max(400, (int) (dist * 100.0));
		//X ArrayList<InterpolatedNodeLayered> interfaces = new ArrayList<InterpolatedNodeLayered>(nPoints);
		ArrayList<GeoTessPosition> interfaces = new ArrayList<GeoTessPosition>(nPoints);
		
		ArrayList<double[]> points = greatCircle.getPoints(nPoints, false);
		
		for (int j = 0; j < nPoints; ++j)
		{
			GeoTessPosition gtp = GeoTessPosition.getGeoTessPosition(bender.getGeoTessModel());
			gtp.set(points.get(j), VectorGeo.getEarthRadius(points.get(j)));
			interfaces.add(gtp);
		}
		
		for (int i = pld.getNInterfaces() - 1; i >= 0; i--)
		{
			for (int j = 0; j < nPoints; ++j)
			{
				GeoTessPosition node = interfaces.get(j);
				node.setRadius(pld.getInterface(i).getRadius(node));
				node.setIndex(i);
			}
			this.view.plot(interfaces, greatCircle, PlotType.LAYER, pld.getInterface(i).getName());	
		}
	}

	public void setRayStatusList(RayStatus... rayStatus) {
		this.rayStatus.clear();
		for (RayStatus status : rayStatus)
			this.rayStatus.add(status);
	}

	private void constructRayToPlot(Ray ray, GreatCircle greatCircle, int layer) {
		String layerName = pld.getInterface(layer).getName();
		
		//Used to round deepestRayPoint to nearest hundred
		//X int	deepestRayPoint = (int) Math.ceil(getDeepestRayPoint(ray));
		//int	deepestRayPoint = (int) Math.ceil(ray.getTurningPoint().getDepth());
		int	deepestRayPoint = (int) Math.ceil(ray.getMaxRayDepth());
		int mod = deepestRayPoint % 100;
		int difference = 100 - mod;
		deepestRayPoint += difference;
		if(this.currentDepth > deepestRayPoint) {
			this.currentDepth = deepestRayPoint;
			//System.out.println("currentDepth = " + currentDepth + ", deepestRayPoint = " + deepestRayPoint);
			this.view.setYAxis(0, this.currentDepth);
			
		}
		
		if (ray.getStatus() == RayStatus.INITIAL_RAY)
		{
			this.view.plot(ray.getNodes(false), greatCircle, PlotType.RAY,
					String.format("initial %1d %s", layer, layerName));
			if (initialAutoRange)
			{
			  this.view.setAutoRange();
			  initialAutoRange = false;
			}
		}

		else if (ray.getStatus() == RayStatus.INNER_LOOP)
			this.view.plot(
					ray.getNodes(false),
					greatCircle,PlotType.RAY,
					String.format("inner %s", layerName));
		
		else if (ray.getStatus() == RayStatus.OUTER_LOOP)
			this.view.plot(ray.getNodes(false), greatCircle, PlotType.RAY, String
					.format("outer %s", layerName));

		else if (ray.getStatus() == RayStatus.SNELL)
			this.view.plot(
					ray.getNodes(false),
					greatCircle, PlotType.RAY, 
					String.format("snells %s", layerName));

		else if (ray.getStatus() == RayStatus.BENT)

			this.view.plot(
					ray.getNodes(false),
					greatCircle, PlotType.RAY, 
					String.format("bend %s", layerName));

		else if (ray.getStatus() == RayStatus.DOUBLED)
			this.view.plot(ray.getNodes(false), greatCircle, PlotType.RAY, 
					String.format("doubled n=%1d", ray.getNPoints()));
		
		if (ray.getStatus() == RayStatus.FINAL_RAY)
			this.view.plot(ray.getNodes(false), greatCircle, PlotType.RAY,
					String.format("final %s", layerName));

		if (ray.getStatus() == RayStatus.FASTEST_RAY)
			this.view
					.plot(ray.getNodes(false),
							greatCircle, PlotType.RAY, 
							String.format("fastest %s", layerName));
	}

//	private double getDeepestRayPoint(Ray ray)
//	{
//		double max = Double.MIN_VALUE;
//		ray.getNodes(false);
//		//X for(InterpolatedNodeLayered node : ray.getNodes(false))
//		for(GeoTessPosition node : ray.getNodes(false))
//			if (node.getDepth() > max) max = node.getDepth();
//		return max;
//	}
	
	@Override
	public void stateChanged(ChangeEvent event) {
		//X Ray ray = (Ray) event.getSource();
		if (event.getSource() == bender)
		{
			try
			{
			  open(bender.getSource(), bender.getReceiver(), bender.getPhaseLayerLevelDefinition());
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else
		{
			Ray ray = (Ray) event.getSource();
		  int layer = ray.getBottomLayer();
		  if (rayStatus.contains(ray.getStatus()) && layer >= bottomLayer && layer <= topLayer)
			  constructRayToPlot(ray, greatCircle, layer);
		}
	}
}
