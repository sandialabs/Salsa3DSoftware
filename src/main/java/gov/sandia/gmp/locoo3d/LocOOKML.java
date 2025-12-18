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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import gov.sandia.gmp.util.numerical.vector.GeoMath;

public class LocOOKML {

	static public void run(double[] location, double[] ellipse, String title, File outputFile) throws Exception
	{
		@SuppressWarnings("unchecked")
		ArrayList<String> k = (ArrayList<String>) kml.clone();
		for (int j=0; j<k.size(); ++j)
		{
			String s = k.get(j);
			if (s.contains("$location_title"))
				k.set(j, s.replace("$location_title", title));
			else if (s.contains("$location_point"))
				k.set(j, s.replace("$location_point", getLatLonString(location)));
			else if (s.contains("$ellipse_points"))
				k.set(j, s.replace("$ellipse_points", getEllipse(location, ellipse)));
		}
		BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
		for (String s : k)
		{
			output.write(s);
			output.newLine();
		}
		output.close();
	}

	private static String getLatLonString(double[] u) {
		return String.format("%1.6f,%1.6f,0", GeoMath.getLonDegrees(u), GeoMath.getLatDegrees(u));
	}

	private static String getEllipse(double[] location, double[] ellipse)
	{
		StringBuffer buf = new StringBuffer();
		double r = GeoMath.getEarthRadius(location);
		for (double[] point : GeoMath.getEllipse(location, ellipse[0]/r, ellipse[1]/r, Math.toRadians(ellipse[2]), 101))
			buf.append(String.format("%1.6f,%1.6f,0 ", GeoMath.getLonDegrees(point), GeoMath.getLatDegrees(point)));
		return buf.toString();
	}

	private static ArrayList<String> kml;
	static {
		kml = new ArrayList<String>();

		kml.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		kml.add("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">");
		kml.add("<Document>");
		kml.add("	<name>LocOO3D</name>");
		kml.add("	<open>1</open>");
		kml.add("	<Style id=\"error-ellipse-line\">");
		kml.add("		<LineStyle>");
		kml.add("			<color>640000ff</color>");
		kml.add("			<width>3</width>");
		kml.add("		</LineStyle>");
		kml.add("	</Style>");
		kml.add("	<Style id=\"location-star\">");
		kml.add("		<IconStyle>");
		kml.add("			<color>640000ff</color>");
		kml.add("			<scale>1.0</scale>");
		kml.add("			<Icon>");
		kml.add("				<href>http://maps.google.com/mapfiles/kml/shapes/star.png</href>");
		kml.add("			</Icon>");
		kml.add("		</IconStyle>");
		kml.add("		<LabelStyle>");
		kml.add("			<color>640000ff</color>");
		kml.add("			<scale>1.2</scale>");
		kml.add("		</LabelStyle>");
		kml.add("	</Style>");
		kml.add("	<Folder>");
		kml.add("		<name>$location_title</name>");
		kml.add("		<open>1</open>");
		kml.add("		<Placemark>");
		kml.add("			<name>$location_title</name>");
		kml.add("			<styleUrl>#location-star</styleUrl>");
		kml.add("			<Point>");
		kml.add("				<coordinates>$location_point</coordinates>");
		kml.add("			</Point>");
		kml.add("		</Placemark>");
		kml.add("		<Placemark>");
		kml.add("			<name>error ellipse</name>");
		kml.add("			<styleUrl>#error-ellipse-line</styleUrl>");
		kml.add("			<LineString>");
		kml.add("				<coordinates>");
		kml.add("					$ellipse_points");
		kml.add("				</coordinates>");
		kml.add("			</LineString>");
		kml.add("		</Placemark>");
		kml.add("	</Folder>");
		kml.add("</Document>");
		kml.add("</kml>");

	}
}
