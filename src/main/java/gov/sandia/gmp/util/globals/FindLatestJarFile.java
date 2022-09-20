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
import java.util.Comparator;
import java.util.TreeSet;

/**
 * Class to search the project directory in a maven repo and return the latest 
 * executable jar file (either release or snapshot).  "Latest" means the jar file
 * with the largest version number.
 * @author sballar
 *
 */
public class FindLatestJarFile {

	public static void main(String[] args) {

		try {
			if (args.length != 2)
			{
				throw new Exception("\nFindLatestJarFile will search the maven-repo for a specific project\n"
						+ "and find the latest snapshot or release executable jar.\n"
						+"Must specify two command line arguments:\n"
						+ "   1 - The path to the directory in the maven-repo that contains the project files\n"
						+ "   2 - [ snapshot | release ] to specify which jar file is to be returned.\n");
			}

			// repoDir is the path to <maven-repo>/<project>
			File repoDir = new File(args[0]);

			if (!repoDir.exists())
				throw new Exception("The directory "+args[0]+" does not exist.");

			if (!repoDir.isDirectory())
				throw new Exception("The File "+args[0]+" exists but is not a directory.");

			if (!args[1].equals("release") && !args[1].equals("snapshot"))
				throw new Exception("Second command line argument must equal either snapshot or release");

			// Container for version directories sorted by version number, largest version number first
			TreeSet<File> versionDirectories = new TreeSet<>(new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					// split the directory names on '.'
					String[] version1 = o1.getName().replace("-SNAPSHOT", "").split("\\.");
					String[] version2 = o2.getName().replace("-SNAPSHOT", "").split("\\.");
					
					// convert the components of the version numbers from strings to integers.
					int[] v1 = new int[version1.length];
					for (int i=0; i<version1.length; ++i) v1[i] = Integer.parseInt(version1[i]);
					int[] v2 = new int[version2.length];
					for (int i=0; i<version2.length; ++i) v2[i] = Integer.parseInt(version2[i]);
					
					//System.out.println(Arrays.toString(v1)+"  "+Arrays.toString(v2));

					for (int i=0; i<Math.min(v1.length, v2.length); ++i)
						if (v2[i] != v1[i])
							return Integer.compare(v2[i], v1[i]);
					return 0;
				} });

			// traverse all the release or snapshot directories in repoDir and add them to versionDirectories
			// sorted in order of decreasing version number (largest version number first).
			for (File d : repoDir.listFiles())
				if (d.isDirectory() && (d.getName().contains("SNAPSHOT") ^ args[1].equals("release")))
					versionDirectories.add(d);

			if (versionDirectories.isEmpty())
				throw new Exception("Directory "+args[0]+"\n"
						+ "does not contain any version directories");

			// Container for jar files sorted by modification date (most recent modification date first)
			TreeSet<File> jarFiles = new TreeSet<>(new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					return Long.compare(o2.lastModified(), o1.lastModified());
				} });

			for (File f : versionDirectories.first().listFiles())
				if (f.getName().endsWith("-jar-with-dependencies.jar"))
					jarFiles.add(f);

			if (jarFiles.isEmpty())
				throw new Exception("Directory "+versionDirectories.first().getAbsolutePath()+"\n"
						+ "does not contain any jar files that end with 'jar-with-dependencies.jar'");

			System.out.println(jarFiles.first().getAbsolutePath());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
