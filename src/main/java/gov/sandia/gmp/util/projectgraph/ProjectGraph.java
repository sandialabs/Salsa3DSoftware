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
package gov.sandia.gmp.util.projectgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProjectGraph {

	private ArrayList<ProjectNode> projectNodes;

	public static void main(String[] args) {
		try {

			if (args.length == 0) {
				System.out.println("Must specify two arguments: path to git directory containing maven projects, \n"
						+ "and a comma delimited list of projects in the git directory to analyze.\n"
						+ "Optional third argument: includeTestScope.  If present test scope dependencies will be included in the analysis.");
			}

			File gitDirectory = new File(args[0]);

			Set<String> projectNames = new LinkedHashSet<>(Arrays.asList(args[1].split(",")));
			
			boolean includeTestScope = (args.length >= 3 && args[2].equalsIgnoreCase("includeTestScope"));

			ProjectGraph graph = new ProjectGraph(gitDirectory, projectNames, includeTestScope);

			System.out.println(graph.toString());

			System.out.println(graph.check_for_redundant_dependencies());
			System.out.println(graph.check_for_version_conflicts(projectNames));
			System.out.println(graph.check_build_order(projectNames));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Given a git directory full of maven projects and a list of maven project names, build a 
	 * directed acyclical graph of the specified projects plus unspecified dependents.  
	 * The nodes of the graph consist of the 
	 * groupId, artifactId and version of each maven project, extracted from the pom.xml files of the specified
	 * projects.
	 * The directed edges point from project nodes to all of their direct dependents.  The graph will include 
	 * nodes for all the projects included in the specified set of project names, but may also include
	 * additional nodes for dependents of the specified projects and their dependents and so on.
	 * <p>Method getProjectNodes() returns a list of ProjectNodes that includes only the projects 
	 * in the specified Set of projectNames.
	 * @throws Exception
	 */
	public ProjectGraph(File gitDirectory, Set<String> projectNames, boolean includeTestScope) throws Exception {

		// keys will be groupId/artifactId/version; values will be ProjectNodes.
		// includes all projects, including those not in projectNames.
		Map<String, ProjectNode> allNodes = new TreeMap<>();

		// build the project graph by parsing the pom.xml files of all the projects 
		// and all of their dependents. Includes all projects, including those not in projectNames
		for (String projectName : projectNames) {
			File f = new File(new File(gitDirectory, projectName), "pom.xml");
			InputStream stream = new FileInputStream(f.getPath());
			parsePom(allNodes, stream, includeTestScope);
		}

		// execute the topological sort algorithm to find the list of
		// project nodes in build order.
		// Stack will Include projects not included in projectNames.
		Stack<ProjectNode> stack = new Stack<>();
		for (ProjectNode projectNode : allNodes.values())
			if (!projectNode.visited())
				topologicalSort(projectNode, stack);

		// find the subset of all project nodes that includes only projects
		// that are in the input set of projectNames.  The list will be in build order.
		projectNodes = new ArrayList<>(stack.size());
		while (!stack.isEmpty()) {
			ProjectNode pn = stack.pop();
			if (projectNames.contains(pn.getProjectId())) {
				projectNodes.add(pn);
			}
		}
	}

	public String check_for_redundant_dependencies() {
		Set<String> discards = new LinkedHashSet<>();
		for (ProjectNode node : projectNodes) 
			node.seartchForDiscards(discards);
		String s = discards.isEmpty() ? "No redundant dependencies" : "";
		for (String d : discards)
			s += d;
		return s;
	}

	public String check_build_order(Set<String> projects) {
		// check to see if the input project names are in proper build order.
		// If not, propose a valid sort order and throw exception.
		Map<String, ProjectNode> nodes = new LinkedHashMap<>();
		for (ProjectNode node : projectNodes)
			nodes.put(node.getProjectId(), node);

		String errors = "";
		Set<String> compiled = new LinkedHashSet<>(projects.size());
		for (String project : projects) {
			ProjectNode projectNode = nodes.get(project);
			for (ProjectNode dependent : projectNode.getDependents()) {
				if (projects.contains(dependent.getProjectId()) && !compiled.contains(dependent.getProjectId())) {
					errors += String.format("%s must be compiled before %s%n", dependent.getProjectId(), project);
				}
			}
			compiled.add(project);
		}

		if (errors.length() > 0) {
			String buildOrder = "";
			for (ProjectNode n : projectNodes) 
				buildOrder += n.getProjectId()+"\n";
			return "The input project names are not in build order.\n"
			+errors+ "A valid build order is:\n"+buildOrder;
		}

		return "No build order violations.";
	}

	public String check_for_version_conflicts(Set<String> projects) {
		// look for conflicts, ie, two nodes with same groupid and artifactid but different versions
		Set<String> conflicts = new LinkedHashSet<>();

		// make a list of all the nodes and sort alphabetically
		ArrayList<ProjectNode> nodeList = new ArrayList<>(projectNodes);
		Collections.sort(nodeList);
		// check for project with the same artifactid but different versions.
		// if found, add to Set of artifactIds that have conflicts
		for (int i=1; i<nodeList.size(); ++i)
			if (nodeList.get(i).getProjectId().equals(nodeList.get(i-1).getProjectId())) {
				conflicts.add(nodeList.get(i).getProjectId());
			}

		if (conflicts.isEmpty())
			return "No conflicting version numbers.";

		// build a map called version_projects, from version to list of projects with that version
		String out = "There are conflicting version numbers:\n";
		for (String projectId : conflicts) {
			TreeMap<String, Set<String>> version_projects = new TreeMap<>(); 
			for (ProjectNode node : nodeList) {
				for (ProjectNode dependent : node.getDependents()) {
					if (dependent.getProjectId().equals(projectId)) {
						Set<String> projectSet = version_projects.get(dependent.getVersion());
						if (projectSet == null)
							version_projects.put(dependent.getVersion(), projectSet=new TreeSet<>());
						projectSet.add(node.getProjectId());
					}
				}
			}

			// print the version_projects to standard out
			for (Entry<String, Set<String>> entry : version_projects.entrySet()) {
				String version = entry.getKey();
				Set<String> projs = entry.getValue();
				out += String.format("%s   is referenced in projects ", projectId+"/"+version);
				String s = ""; for (String p : projs) s += ", "+p;
				out += s.substring(2)+"\n";
			}
		}
		return out;
	}

	private void topologicalSort(ProjectNode projectNode, Stack<ProjectNode> stack) {
		projectNode.visited(true);
		for (ProjectNode parent : projectNode.getParents()) 
			if (!parent.visited())
				topologicalSort(parent, stack);
		stack.push(projectNode);
	}

	/**
	 * Extract information for ProjectNode from a pom file.
	 * @param input a stream pointing to the contents of a pom file.
	 * @return
	 * @throws Exception
	 */
	private ProjectNode parsePom(Map<String, ProjectNode> allNodes, InputStream input, boolean includeTestScope) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
		DocumentBuilder db = dbf.newDocumentBuilder();  
		Document doc = db.parse(input);  
		doc.getDocumentElement().normalize();  

		// get a reference to the root element, which is 'project'
		Element root = (Element)doc.getElementsByTagName("project").item(0); 

		// get the groupId, artifactId and version of this pom file and 
		// populate a new ProjectNode object, except for the dependents.
		String groupId = ((Element)root.getElementsByTagName("groupId").item(0)).getTextContent();
		String artifactId = ((Element)root.getElementsByTagName("artifactId").item(0)).getTextContent();
		String version = ((Element)root.getElementsByTagName("version").item(0)).getTextContent();
		ProjectNode projectNode = getProjectNode(groupId, artifactId, version, allNodes);

		// get a reference to the dependencies node.
		NodeList dependencies = root.getElementsByTagName("dependencies");

		// if there are any dependencies, iterate over all of them
		if (dependencies.getLength() > 0)
		{
			// get a list of all the <dependency> nodes and iterate over them
			NodeList dependencyList = ((Element)dependencies.item(0)).getElementsByTagName("dependency");
			for (int itr = 0; itr < dependencyList.getLength(); itr++)   
			{  
				Node dependency = dependencyList.item(itr);  
				if (dependency.getNodeType() == Node.ELEMENT_NODE)   
				{  
					Element eElement = (Element) dependency; 

					groupId = ((Element)eElement.getElementsByTagName("groupId").item(0)).getTextContent();
					artifactId = ((Element)eElement.getElementsByTagName("artifactId").item(0)).getTextContent();
					version = ((Element)eElement.getElementsByTagName("version").item(0)).getTextContent();
					Element scope = ((Element)eElement.getElementsByTagName("scope").item(0));

					if (includeTestScope || scope == null) {
						ProjectNode dependent = getProjectNode(groupId, artifactId, version, allNodes);
						dependent.addParent(projectNode);
						projectNode.addDependent(dependent);
					}
				}  
			}
		}
		return projectNode;  
	}

	private String getKey(String groupId, String artifactId, String version) {
		return groupId+"/"+artifactId+"/"+version;
	}

	private ProjectNode getProjectNode(String groupId, String artifactId, String version, 
			Map<String, ProjectNode> allNodes) throws Exception {
		String key = getKey(groupId, artifactId, version);
		ProjectNode projectNode = allNodes.get(key);
		if (projectNode == null)
			allNodes.put(key, projectNode = new ProjectNode(groupId, artifactId, version));
		return projectNode;
	}

	public ArrayList<ProjectNode> getProjectNodes() {
		return projectNodes;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (ProjectNode node : projectNodes) {
			buf.append(node.toString()+"\n");
			buf.append("    Parents: ");
			String s = "";
			for (ProjectNode parent : node.getParents())
				s += ", "+parent.toString();
			if (s.length() > 0)
				buf.append(s.substring(2));
			buf.append("\n");
			buf.append("    Dependents: ");
			s = "";
			for (ProjectNode dependent : node.getDependents())
				s += ", "+dependent.toString();
			if (s.length() > 0)
				buf.append(s.substring(2));
			buf.append("\n");
			buf.append("\n");
		}
		return buf.toString();
	}

}
