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
package gov.sandia.gmp.util.globals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Utils {
	
	static public String getVersion() {
		return getVersion("utils");
	}

	/**
	 * Search for a resource named <project>.version.  If found, 
	 * read the resource and extract the version number.  
	 * If the version number ends with "SNAPSHOT" append the timestamp.
	 * @param project
	 * @return
	 */
	static public String getVersion(String project) {
		try {
			Map<String, String> map = getVersionMap(project);
			
			String version = map.get("version");
			if (version == null)
				version = "???";
			if (version.endsWith("SNAPSHOT"))
			{
				String timestamp = map.get("timestamp");
				if (timestamp == null)
					timestamp = "???";
				version += "-" + timestamp;
			}
			return version;
		} catch (Exception e) {
			return "null";
		}
	}
	
	/**
	 * Search for a resource named <project>.version.  If found, 
	 * read the resource and extract a map with keys 
	 * [group, artifact, version, timestamp], if they exist.
	 * @param project
	 * @return
	 */
	static public Map<String, String> getVersionMap(String project) {
		try {
			InputStream is = getResourceAsStream(project+".version");
			Map<String, String> map = new HashMap<>();
			Scanner in = new Scanner(is);
			while (in.hasNext())
			{
				String line = in.nextLine();
				if (line.contains("="))
				{
					String[] entry = line.split("=");
					map.put(entry[0].trim(), entry[1].trim());
				}
			}
			in.close();
			is.close();
			return map;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Retrieve an InputStream for the specified resource. The resource
	 * is sought in "/" first.  If it is not there then it is sought in 
	 * "/resources/".  If it is not found in either of those places, 
	 * NULL is returned
	 * @param resource
	 * @return
	 */
	static public InputStream getResourceAsStream(String resource)
	{
		InputStream is = Globals.class.getResourceAsStream("/" + resource);
		if (is == null)
			is = Globals.class.getResourceAsStream("/resources/" + resource);
		return is;
	}

	/**
	 * If a program is run from an executable jar, scan the jar file for
	 * all files that end with '.version'.  Extract the version info and
	 * return a list of all the projects with version information attached.
	 * @return
	 * @throws IOException
	 */
	static public String getDependencyVersions() throws Exception
	{
		// find the jar file that was executed to run the current program
		File exe = new java.io.File(Utils.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath());
		
		if (!exe.getName().toLowerCase().endsWith("jar"))
			throw new IOException("Program not executed from a jar file.");

		// scan the jar file for all the files that end in '.version'
		Set<String> projects = new TreeSet<>();
		try (JarFile jarFile = new JarFile(exe)) {
			Enumeration<JarEntry> e = jarFile.entries();
			while (e.hasMoreElements()) {
				JarEntry jarEntry = e.nextElement();
				if (jarEntry.getName().endsWith(".version")) {
					String project = jarEntry.getName().replace(".version", "");
					project = project+"."+Utils.getVersion(project);
					projects.add(project);
				}
			}
		}
		StringBuffer buf = new StringBuffer();
		for (String vf : projects)
			buf.append(vf).append("\n");
		return buf.toString();
	}

}
