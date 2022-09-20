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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;

import gov.sandia.geotess.GeoTessPosition;
//import gov.sandia.gmp.bender.phase.Phase;
//X import gov.sandia.gmp.geomodel.InterpolatedNodeLayered;
//X import gov.sandia.gmp.geomodel.Phase;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;

public class RayVisualizerChartFrame extends JFrame {

	private static final long serialVersionUID = -8019450313480283427L;
	private XYPlot plot;
	private XYSeriesCollection layers;

	public RayVisualizerChartFrame() {
		super();
		JFreeChart dataChart = ChartFactory.createXYLineChart("","Distance (degrees)", "Depth (km)", null,PlotOrientation.VERTICAL, true, false, false);
		ChartPanel chart = new ChartPanel(dataChart);
		chart.getChart().getXYPlot().getRangeAxis().setInverted(true);

        LegendTitle legend = new LegendTitle(dataChart.getPlot());
        legend.setItemFont(new Font("Courier", Font.PLAIN, 14));       
        legend.setPosition(RectangleEdge.RIGHT);
        
        dataChart.addSubtitle(legend);
        dataChart.removeSubtitle(dataChart.getLegend());

		this.layers = new XYSeriesCollection();
		this.plot = (XYPlot) dataChart.getPlot();	
		dataChart.setPadding(new RectangleInsets(20,20,20,20));
		XYLineAndShapeRenderer layerRenderer = new XYLineAndShapeRenderer();
		XYLineAndShapeRenderer rayRenderer = new XYLineAndShapeRenderer();

		this.plot.setRenderer(PlotType.LAYER.getIndex(), layerRenderer);
		this.plot.setRenderer(PlotType.RAY.getIndex(), rayRenderer);
		this.add(chart);
	}

	//X protected void plot(List<GeoTessPosition> nodes, Phase phase,
  //X 			GreatCircle greatCircle, PlotType plotType, String s) {
	//protected void plot(List<GeoTessPosition> nodes, Phase phase,
	//		GreatCircle greatCircle, PlotType plotType, String s) {
	protected void plot(List<GeoTessPosition> nodes,
			GreatCircle greatCircle, PlotType plotType, String s) {

		//X InterpolatedNodeLayered receiver = nodes.get(nodes.size() - 1);
		GeoTessPosition receiver = nodes.get(nodes.size() - 1);
		XYSeries series = new XYSeries(String.format("%1$-"+25+"s", s), false);
		double[] xy = new double[3];
		try {
			//X for (InterpolatedNodeLayered node : nodes) {
			for (GeoTessPosition node : nodes) {
				//greatCircle.transform(node.getVector(), xy);
				double x = node.distanceDegrees(receiver);
				double y = node.getDepth();
				series.add(x, y);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (plotType == PlotType.LAYER) {
			layers.addSeries(series);
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.plot.getRenderer(plotType.getIndex());
			renderer.setSeriesShapesVisible(layers.indexOf(series), false);
			this.plot.setDataset(plotType.getIndex(), layers);
		} else {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.plot.getRenderer(plotType.getIndex());
			renderer.setSeriesShape(0, ShapeUtilities.createDiamond(2));
			renderer.setSeriesPaint(0, Color.BLACK);
			renderer.setSeriesShapesVisible(0, true);
			this.plot.setDataset(plotType.getIndex(), new XYSeriesCollection(series));
		}
	}
	
	public void display() {
		this.setPreferredSize(new Dimension(1000, 600));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setVisible(true);
	}

	public void setXAxis(int lowerBound, int upperBound) {
		this.plot.getDomainAxis().setRange(lowerBound, upperBound);
	}

	public void setYAxis(int lowerBound, int upperBound) {
		this.plot.getRangeAxis().setRange(lowerBound, upperBound);
	}
	
	public void setAutoRange()
	{
		this.plot.getRangeAxis().setAutoRange(true);
	}
}
