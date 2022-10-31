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
package gov.sandia.gmp.baseobjects.supportmap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.gmp.util.globals.Site;

/**
 * Stores basic information about a LibCorr3D model such as
 * filename, station position, supported phase and supported attributes.
 */
public class SupportMapModel implements Comparable<SupportMapModel>
{
    File file;

    int index;

    Site station;

    /**
     * Phase name extracted from the model file.
     */
    List<String> supportedPhases;

    /**
     * Names of the attributes stored in the model.
     */
    String[] attributes;

    public SupportMapModel(int index, File file, String relativeGridPath) throws IOException
    {
	this.index = index;
	this.file = file.getCanonicalFile();

	GeoTessMetaData md = GeoTessMetaData.getMetaData(file);

	// get the model attributes from the metadata;
	attributes = md.getAttributeNames();

	// try and get site and supportedPhases from metadata properties.
	String s = md.getProperties().get("site");
	if (s != null)
	    station = Site.getSite(s.trim());

	s = md.getProperties().get("supportedPhases");
	if (s != null)
	    supportedPhases = Arrays.asList(s.trim().replaceAll(",", " ").split("\\s+"));

	// if still unsuccessful, load the entire model and get the info.
	if (station == null || supportedPhases == null)
	{
	    LibCorr3DModel lcm = new LibCorr3DModel(file, relativeGridPath);
	    station = lcm.getSite();
	    supportedPhases = lcm.getSupportedPhases();

	    GeoTessModel.getGridMap().remove(lcm.getGrid().getGridID());
	}
    }

    public File getRelativePath(Path directory) throws IOException {
	return directory.relativize(Paths.get(file.getCanonicalPath())).toFile();
    }

    @Override
    public String toString()
    {
	return String.format("%3d %-6s %-6s %7d %7d %10.6f %11.6f %6.3f %s %s", 
		index, station.getSta(), station.getRefsta(), station.getOndate(),
		station.getOffdate(), station.getLat(), station.getLon(),
		station.getElev(), supportedPhases.toString(),
		Arrays.toString(attributes));
    }
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((file == null) ? 0 : file.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	SupportMapModel other = (SupportMapModel) obj;
	if (file == null) {
	    if (other.file != null)
		return false;
	} else if (!file.equals(other.file))
	    return false;
	return true;
    }
    /**
     * Two Models are compared based on their file names.
     */
    @Override
    public int compareTo(SupportMapModel other)
    {
	return file.getPath().compareTo(other.file.getPath());
    }

}
