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
package gov.sandia.gmp.util.projecttree;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import gov.sandia.gmp.util.containers.Tuple;

/**
 * VersionControl parses a collection of pom.xml files from a group of related
 * maven projects and builds an acyclic graph of the information they contain.
 * Each node of the graph contains the groudId, artifactid and version number of
 * a single project, extracted from the pom file. The unidirectional edges of
 * the graph are defined by the dependencies between projects. With this
 * information, the VersionConrol application can perform the following tasks:
 * 
 * CHECK_STATUS – Checks for the following issues: 
 * • While loading the graph, if any cycles are discovered, VersionControl will 
 * throw an exception specifying which two projects produced the cycle. 
 * • After loading the graph, VersionControl will traverse the graph and identify 
 * any project dependencies that can be deleted from the pom files without causing problems. 
 * If project A depends on project B, and B depends on C, and A depends on C, then the
 * dependence of A on C can be deleted in project A because the dependence will
 * be satisfied by the other dependences in the graph. 
 * • Version control will identify nodes in the graph that have the same artifactId 
 * but different version numbers. 
 * POM.XML FILES IN USER'S GIT DIRECTORY ARE _NOT_ MODIFIED BY THIS PROCESS.
 * 
 * REMOVE_SNAPSHOT / ADD_SNAPSHOT – For every project in the specified set of
 * projects, modify the version number by either removing or adding ‘-SNAPSHOT’
 * to the end of the version number. 
 * POM.XML FILES IN USER'S GIT DIRECTORY ARE MODIFIED BY THIS PROCESS.
 * 
 * INCREMENT / DECREMENT – For every project in the specified set of projects,
 * either increment or decrement the version number. Version numbers are
 * expected to be of the form xx.yy.zz or xx.yy.zz-SNAPSHOT. The number zz is
 * incremented or decremented. 
 * POM.XML FILES IN USER'S GIT DIRECTORY ARE MODIFIED BY THIS PROCESS.
 * 
 * @author sballar
 *
 */
public class VersionControl {

    private enum Application { CHECK_STATUS, INCREMENT, DECREMENT, REMOVE_SNAPSHOT, ADD_SNAPSHOT};

    /**
     * Expecting arguments 1. The operation to perform. One of CHECK_STATUS,
     * INCREMENT, DECREMENT, REMOVE_SNAPSHOT, ADD_SNAPSHOT. 2. Path to the git
     * directory containing maven projects. 3. List of projects in the git directory
     * upon which to perform operation. Comma separated, no spaces. 4. Optional path
     * to a directory where changelog files will be written.
     * 
     * CHECK_STATUS – Checks for the following issues: • While loading the graph, if
     * any cycles are discovered, VersionControl will throw an exception specifying
     * which two projects produced the cycle. • After loading the graph,
     * VersionControl will traverse the graph and identify any project dependencies
     * that can be deleted from the pom files without causing problems. If project A
     * depends on project B, and B depends on C, and A depends on C, then the
     * dependence of A on C can be deleted in project A because the dependence will
     * be satisfied by the other dependences in the graph. • Version control will
     * identify nodes in the graph that have the same artifactId but different
     * version numbers. • CHECK_STATUS DOES NOT MODIFY ANY POM.XML FILES.
     * Information collected during execution is reported on the screen and the user
     * must make pom file modifications manually.
     * 
     * REMOVE_SNAPSHOTS / ADD_SNAPSHOTS – For every project in the specified set of
     * projects, modify the version number by either removing or adding ‘-SNAPSHOT’
     * to the end of the version number. Pom.xml files in the users git directory
     * are modified by this process.
     * 
     * INCREMENT / DECREMENT – For every project in the specified set of projects,
     * modify the version number by either incrementing or decrementing the version
     * number. Version numbers are expected to be of the form xx.yy.zz or
     * xx.yy.zz-SNAPSHOT. The number zz is incremented or decremented. Pom.xml files
     * in the users git directory are modified by this process.
     * 
     */
    public static void main(String[] args) {

	if (args.length == 0) {
	    System.out.println( String.format(
		    "Expecting arguments %n"
			    + "  1. The operation to perform. One of CHECK_STATUS,  INCREMENT, DECREMENT, REMOVE_SNAPSHOT, ADD_SNAPSHOT. %n"
			    + "  2. Path to the git directory containing maven projects. %n"
			    + "  3. List of projects in the git directory upon which to perform operation. Comma separated, no spaces. %n"
			    + "  4. Optional path to a directory where changelog files will be written. %n"           
			    +" %n"
			    +"CHECK_STATUS – Checks for the following issues: • While loading the graph, if %n"                                   
			    +"any cycles are discovered, VersionControl will throw an exception specifying %n"                                  
			    +"which two projects produced the cycle. • After loading the graph, %n"                       
			    +"VersionControl will traverse the graph and identify any project dependencies %n"                                  
			    +"that can be deleted from the pom files without causing problems. If project A %n"                                   
			    +"depends on project B, and B depends on C, and A depends on C, then the %n"                            
			    +"dependence of A on C can be deleted in project A because the dependence will %n"                                  
			    +"be satisfied by the other dependences in the graph. • Version control will %n"                                
			    +"identify nodes in the graph that have the same artifactId but different %n"                             
			    +"version numbers. • CHECK_STATUS DOES NOT MODIFY ANY POM.XML FILES. %n"                        
			    +"Information collected during execution is reported on the screen and the user %n"                                   
			    +"must make pom file modifications manually. %n"
			    +" %n"
			    +"REMOVE_SNAPSHOTS / ADD_SNAPSHOTS – For every project in the specified set of %n"                                  
			    +"projects, modify the version number by either removing or adding ‘-SNAPSHOT’ %n"                                  
			    +"to the end of the version number. Pom.xml files in the users git directory %n"                                
			    +"are modified by this process. %n"
			    +" %n"
			    +"INCREMENT / DECREMENT – For every project in the specified set of projects, %n"                                 
			    +"modify the version number by either incrementing or decrementing the version %n"                                  
			    +"number. Version numbers are expected to be of the form xx.yy.zz or %n"                        
			    +"xx.yy.zz-SNAPSHOT. The number zz is incremented or decremented. Pom.xml files %n"                                   
			    +"in the users git directory are modified by this process. %n"              
		    ));
	    System.exit(0);
	}

	try {

	    int a=0;
	    Application application = Application.valueOf(args[a++].toUpperCase());

	    File gitDirectory = null;

	    Set<String> projects = new LinkedHashSet<>();

	    File changeLogDirectory = null;

	    // if second argument is a directory and arg2/pom.xml exists,
	    // then gitDirectory is arg2.getParent() and project is arg2.getName().
	    // Otherwise, arg2 is gitDirectory and next arg is a comma-delimited list of projects
	    File f = new File(args[a++]);
	    if (f.isDirectory() && new File(f, "pom.xml").exists()) {
		gitDirectory = f.getParentFile();
		projects.add(f.getName());
	    }
	    else {
		gitDirectory = f;
		String[] p = args[a++].replaceAll(",", " ").trim().split("\\s+");
		projects.addAll(Arrays.asList(p));
	    }

	    // if there is another argument, then it is the directory to which to write changelogs
	    if (a < args.length-1) {
		String dirname = args[a++];
		// only accept the argument as a changeLogDirectory if it contains a File.separator character
		// otherwise it might be some random string and we could create a directory in a strange place.
		if (dirname.contains(File.separator)) {
		    changeLogDirectory = new File(args[a++]);
		    changeLogDirectory.mkdirs();
		}
	    }


	    // iterate over every project, build the project tree EXCLUDING dependencies with test scope, and 
	    // add all the ProjectNodes to a TreeSet of nodes.
	    Set<ProjectNode> nodes = new TreeSet<>();
	    for (String project : projects) {
		ProjectTree tree = new ProjectTree(new File(gitDirectory, project), false);
		for (ProjectNode node : tree.getSet(false))
		    nodes.add(node);
	    }

	    // check for cycles
	    for (ProjectNode node : nodes) {
		for (ProjectNode parent : node.parents)
		    if (parent.ancestors.contains(node)) {

			String err = "";
			//		for (ProjectNode ancestor : ancestors)
			//		    a += ", "+ancestor.getProjectId();
			for (ProjectNode dependent : node.dependents)
			    for (ProjectNode ancestor : node.ancestors)
				if (ancestor.getProjectId().equals(dependent.getProjectId()))
				    err += String.format("%s and %s depend on each other.%n", 
					    ancestor.getProjectId(), node.getProjectId());

			throw new Exception(String.format("There is a cycle in the project tree: %s", err));
		    }
	    }

	    System.out.println("VersionControl: Scanning project graph for redundant project dependencies.\n");
	    Set<String> discards = new TreeSet<>();;
	    for (ProjectNode node : nodes) 
		node.seartchForDiscards(discards);

	    if (discards.isEmpty())
		System.out.println("VersionControl: No redundant prject dependencies found.\n");
	    else
		for (String s : discards)
		    System.out.print(s);
	    System.out.println();


	    // iterate over every project, build the project tree INCLUDING dependencies with test scope, and 
	    // add all the ProjectNodes to a TreeSet of nodes.
	    nodes.clear();;
	    for (String project : projects) {
		ProjectTree tree = new ProjectTree(new File(gitDirectory, project), true);
		for (ProjectNode node : tree.getSet(false))
		    nodes.add(node);
	    }

	    System.out.println("VersionControl: Scanning project graph for conflicting version numbers.\n");
	    // look for conflicts, ie, two nodes with same groupid and artifactid but different versions
	    Set<String> conflicts = new LinkedHashSet<>();

	    // make a list of all the nodes (they are sorted alphabetically by the treeset)
	    ArrayList<ProjectNode> nodeList = new ArrayList<>(nodes);
	    // check for project with the same artifactid but different versions.
	    // if found, add to Set of artifactIds that have conflicts
	    for (int i=1; i<nodeList.size(); ++i)
		if (nodeList.get(i).getProjectId().equals(nodeList.get(i-1).getProjectId())) {
		    conflicts.add(nodeList.get(i).getProjectId());
		}

	    // build a map, version_projects, from version to list of projects with that version
	    for (String projectId : conflicts) {
		TreeMap<String, Set<String>> version_projects = new TreeMap<>(); 
		for (ProjectNode node : nodeList) {
		    f = new File(gitDirectory, node.getProjectId());
		    if (f.exists()) {
			ProjectTree tree = new ProjectTree(f, true);
			for (ProjectNode dependent : tree.getDependents()) {
			    if (dependent.getProjectId().equals(projectId)) {
				Set<String> projectSet = version_projects.get(dependent.getVersion());
				if (projectSet == null)
				    version_projects.put(dependent.getVersion(), projectSet=new TreeSet<>());
				projectSet.add(tree.getProjectId());
			    }
			}
		    }
		}


		if (version_projects.isEmpty())
		    System.out.println("VersionControl: No conflicting version numbers found.\n");
		else
		    // print the version_projects to standard out
		    for (Entry<String, Set<String>> entry : version_projects.entrySet()) {
			String version = entry.getKey();
			Set<String> projs = entry.getValue();
			System.out.printf("%s   is referenced in projects ", projectId+"/"+version);
			for (String p : projs)
			    System.out.print(", "+p);
			System.out.println();
		    }
	    }

//	    // if any of the conflicting versions involved a project in the input list of projects
//	    // throw an exception
//	    for (int i=1; i<nodeList.size(); ++i)
//		if (nodeList.get(i).getProjectId().equals(nodeList.get(i-1).getProjectId())) {
//		    if (projects.contains(nodeList.get(i).getProjectId()))
//			throw new Exception("Version clash "+nodeList.get(i).getProjectId());
//		}

	    if (application != Application.CHECK_STATUS) {

		// map from projectId to Tuple <old version, new version>.
		// here is where changes are made to version number.
		Map<String, Tuple<String, String>> versions = new HashMap<>();
		for (ProjectNode node : nodeList) 
		    if (projects.contains(node.getProjectId()))
			versions.put(node.getProjectId(), new Tuple<String, String>(
				node.getVersion(), changeVersion(node.getVersion(), application)));

		Map<String, ArrayList<String>> poms = new LinkedHashMap<>();

		Map<String, ArrayList<String>> changeLog = new LinkedHashMap<>();

		// for every project, read the pom file and make changes to the version numbers,
		for (String project : projects) {

		    ArrayList<String> changes = new ArrayList<>();
		    changeLog.put(project, changes);

		    // read the pom file into array of strings
		    Scanner input = new Scanner(new File(new File(gitDirectory, project),"pom.xml"));

		    ArrayList<String> inLines = new ArrayList<>(200);
		    while (input.hasNext())
			inLines.add(input.nextLine());
		    input.close();

		    // loop over all input lines and change the project version number
		    boolean projectIdChanged = false;		
		    boolean dependencies = false;
		    for (int i=0; i<inLines.size(); ++i) {
			String line = inLines.get(i);
			if (line.contains("<dependencies>"))
			    dependencies = true;
			if (line.contains("</dependencies>"))
			    dependencies = false;

			// if we are outside the dependencies element, identify the first
			// instance of <version> and change the project version number.
			if (!dependencies && line.contains("<version>")) {
			    if (inLines.get(i-1).contains("<artifactId>"+project+"</artifactId>")) {
				Tuple<String, String> tuple = versions.get(project);
				inLines.set(i, line.replace(tuple.first, tuple.second));

				changes.add(String.format("%4d %-8s %-8s", i, tuple.first, tuple.second));

				projectIdChanged = true;
				break;
			    }
			}
		    }
		    if (!projectIdChanged)
			throw new Exception("Failed to change the project id for for project "+project);

		    // now work on dependencies
		    ArrayList<String> outLines = new ArrayList<>(inLines.size());
		    dependencies = false;
		    ListIterator<String> it = inLines.listIterator();
		    int linenum=0;
		    while (it.hasNext()) {

			String line = it.next();

			if (line.contains("<dependencies>"))
			    dependencies = true;
			if (line.contains("</dependencies>"))
			    dependencies = false;

			if (dependencies) {
			    if (line.contains("<dependencies>")) {
				outLines.add(line);
				++linenum;
			    }
			    if (line.contains("<dependency>")) {
				String dependentProject = null;
				ArrayList<String> dlines = new ArrayList<>();
				dlines.add(line);
				while (!line.contains("</dependency>")) {
				    line = it.next();
				    dlines.add(line);
				    if (line.contains("<artifactId>"))
					dependentProject = line.replace("<artifactId>", "").replace("</artifactId>", "").trim();
				}
				if (dependentProject == null)
				    throw new Exception("Failed to find the name of the dependent project");
				if (projects.contains(dependentProject)) {
				    Tuple<String, String> tuple = versions.get(dependentProject);
				    if (tuple == null)
					throw new Exception("Failed to find tuple for dependent project "+dependentProject);
				    for (String s : dlines) {
					if (s.contains("<version>")) {
					    s = s.replace(tuple.first, tuple.second);
					    changes.add(String.format("%4d %-8s %-8s", linenum, tuple.first, tuple.second));
					}
					outLines.add(s);
					++linenum;
				    }
				}
				else
				    for (String s : dlines)  { outLines.add(s); ++linenum; }
			    }
			}
			else {
			    outLines.add(line);
			    ++linenum;
			}
		    }

		    if (linenum != outLines.size())
			throw new Exception(String.format("linenum(%d) != outLines.size(%d)", linenum, outLines.size()));

		    if (outLines.size() != inLines.size())
			throw new Exception(String.format("outLines.size(%d) != inLines.size(%d)", outLines.size(), inLines.size()));

		    poms.put(project, outLines);
		}

		if (changeLogDirectory != null)
		    // write the change logs and pom files
		    for (String project : projects) {
			FileWriter fw = new FileWriter(new File(changeLogDirectory, 
				project+"_"+application.toString().toLowerCase()+".changelog"));
			for (String s : changeLog.get(project))
			    fw.write(s+"\n");
			fw.close();

			fw = new FileWriter(new File(new File(gitDirectory, project),"pom.xml"));
			for (String s : poms.get(project))
			    fw.write(s+"\n");
			fw.close();
		    }
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

    private static void undo(File gitDirectory, Set<String> projects, Application application, File changeLogDirectory) throws Exception {
	for (String project : projects) {
	    File changeLogFile = new File(changeLogDirectory, 
		    String.format("%s_%s.changelog", project, application.toString().toLowerCase()));
	    if (changeLogFile.exists()) {
		File pomFile = new File(new File(gitDirectory, project), "pom.xml");
		ArrayList<String> pomlines = new ArrayList<>();
		Scanner in = new Scanner(pomFile);
		while (in.hasNext())
		    pomlines.add(in.nextLine());
		in.close();
		in = new Scanner(changeLogFile);
		boolean changes = false;
		while (in.hasNext()) {
		    int lineno = in.nextInt();
		    String original = in.next();
		    String changed = in.next();
		    pomlines.set(lineno, pomlines.get(lineno).replace(changed, original));
		    changes = true;
		}
		in.close();
		if (changes) {
		    FileWriter fw = new FileWriter(pomFile);
		    for (String s : pomlines)
			fw.write(s+"\n");
		    fw.close();
		}
	    }
	}
    }

    private static String changeVersion(String version, Application application) {
	switch(application) {
	case REMOVE_SNAPSHOT:
	    return version.replace("-SNAPSHOT", "");
	case ADD_SNAPSHOT:
	    return version.endsWith("-SNAPSHOT") ? version : version+"-SNAPSHOT";
	case INCREMENT:
	    return inc(version, 1);
	case DECREMENT:
	    return inc(version, -1);
	default:
	    return version;
	}
    }

    private static String inc(String version, int value) {
	String newVersion = "";
	String[] outerParts = version.split("-");
	String[] parts = outerParts[0].split("\\.");
	for (int i=0; i<parts.length-1; ++i) 
	    newVersion += parts[i]+".";
	newVersion += String.format("%d", (Integer.valueOf(parts[parts.length-1])+value));
	for (int i=1; i<outerParts.length; ++i)
	    newVersion += "-"+outerParts[i];
	return newVersion;
    }
}
