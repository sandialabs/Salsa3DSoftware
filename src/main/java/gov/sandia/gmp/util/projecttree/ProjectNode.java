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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A single node in a ProjectTree. The root element of a ProjectTree is a
 * ProjectNode. Each ProjectNode records the groupId, artifactId, version, and a
 * Collection of other ProjectNodes that are dependencies of the ProjectNode.
 * 
 * @author sballar
 *
 */
public class ProjectNode implements Comparable<ProjectNode> {
	private String groupId;
	private String projectId;
	private String version;
	
	/**
	 * all of this nodes dependents
	 */
	protected LinkedHashSet<ProjectNode> dependents;
	
	/**
	 * all the nodes that depend directly on this node
	 */
	protected LinkedHashSet<ProjectNode> parents;
	
	/**
	 *  a list of all the nodes reaching back up the tree to root.
	 */
	protected LinkedHashSet<ProjectNode> ancestors;

	public ProjectNode() {
		dependents = new LinkedHashSet<ProjectNode>();
		ancestors = new LinkedHashSet<ProjectNode>();
		parents = new LinkedHashSet<ProjectNode>();
	}

	public ProjectNode(String groupId, String project, String version) throws Exception {
		this();
		this.groupId = groupId;
		this.projectId = project;
		this.version = version;
	}
	
	public String getGroupId() {
		return groupId;
	}

	public String getProjectId() {
		return projectId;
	}

	public String getVersion() {
		return version;
	}
	
	/**
	 * Add a node to this node's set of parents. A check is performed that parent's ancestors
	 * don't contain a reference to this node.  If that is the case, it represents a cycle in 
	 * the graph which is not allowed.
	 * @param parent
	 * @throws Exception if there is a cycle ni the graph, i.e., this node is a member of 
	 * parent's ancestors.
	 * 
	 */
	public void addParent(ProjectNode parent) throws Exception {
	    // if parent's ancesstors already contain this ProjectNode, then there is 
	    // a cycle in this graph.  Throw an exception.
	    if (parent.ancestors.contains(this)) {
		
		Set<String> err = new LinkedHashSet<>();
		// if must be true that this node has a dependency on one of its ancestors.  find the pair.
		for (ProjectNode dependent : dependents)
		    for (ProjectNode ancestor : parent.ancestors)
			if (ancestor.projectId.equals(dependent.projectId))
			    err.add(String.format("%s and %s depend on each other.%n", parent.projectId, this.projectId));
		String e2 = "";
		for (String e : err)
		    e2+=e;
		
		throw new Exception(String.format("There is a cycle in the project tree: %s", e2));
	    }
	    parents.add(parent);
	    ancestors.addAll(parent.ancestors);
	    ancestors.add(this);
	}

	public Collection<ProjectNode> getDependents() {
		return dependents;
	}

	/**
	 * Copy all the contents of node into this.
	 * 
	 * @param node the ProjectNode from which to copy information.
	 */
	protected void copy(ProjectNode node) {
		this.groupId = node.groupId;
		this.projectId = node.projectId;
		this.version = node.version;
		this.dependents.clear();
		this.dependents.addAll(node.dependents);
	}

	@Override
	public String toString() {
		return String.format("%s/%s/%s", groupId, projectId, version);
	}
	
	public String getVersionFile(String timestamp)
	{
		return String.format("group=%s%n"
				+ "artifact=%s%n"
				+ "version=%s%n"
				+ "timestamp=%s%n",
				groupId, projectId, version, timestamp);
				
	}

	/**
	 * Retrieve the set of unique ProjectNodes in the project.
	 * @param topDown
	 * @return
	 */
	public Set<ProjectNode> getSet(boolean topDown) {
		Set<ProjectNode> set = new LinkedHashSet<>();
		return this.addToSet(set, topDown);
	}

	/**
	 * Retrieve a String containing the unique set of project nodes that comprise
	 * the project tree.
	 * 
	 * @param topDown if true, the projects will be topDown order, otherwise
	 *                bottomUp.
	 * @return a String with all the project nodes.
	 */
	public String getSetString(boolean topDown) {
		StringBuffer buf = new StringBuffer();
		for (ProjectNode node : getSet(topDown))
			buf.append(node.toString() + "\n");
		return buf.toString();
	}

	public String getSetString() {
		ArrayList<String> list = new ArrayList<>();
		for (ProjectNode node : getSet(true))
			list.add(node.toString());
		Collections.sort(list);
		StringBuffer buf = new StringBuffer();
		for (String s : list)
			buf.append(s+"\n");
		return buf.toString();
	}

	private Set<ProjectNode> addToSet(Set<ProjectNode> set, boolean topDown) {
		// if topDown is true, add this to the set before the dependents
		if (topDown)
			set.add(this);

		for (ProjectNode d : dependents)
			d.addToSet(set, topDown);

		// if topDown is false, add this to the set after the dependents
		if (!topDown)
			set.add(this);

		return set;
	}

	/**
	 * For this ProjectNode, and all dependent nodes, add an entry to the buffer.
	 * @param buffer
	 * @param indentation
	 */
	protected void code(StringBuffer buffer) {
		for (ProjectNode d : dependents)
			buffer.append(String.format("%-12s %s.addDependencies(dependencies);%n", 
					projectId, d.projectId));
		buffer.append("\n");
		
		for (ProjectNode d : dependents)
			d.code(buffer);
	}

	/**
	 * For this ProjectNode, and all dependent nodes, add an entry to the buffer.
	 * @param buffer
	 * @param indentation
	 */
	protected void toString(StringBuffer buffer, String indentation) {
		buffer.append(indentation + toString() + "\n");
		indentation = indentation + "   ";
		for (ProjectNode d : dependents)
			d.toString(buffer, indentation);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || o.getClass() != this.getClass()) {
			return false;
		}
		return this.projectId.equals(((ProjectNode) o).projectId) 
				&& this.version.equals(((ProjectNode) o).version)
				&& this.groupId.equals(((ProjectNode) o).groupId);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public int compareTo(ProjectNode o) {
		return this.toString().compareTo(o.toString());
	}
	
	public void seartchForDiscards(Set<String> discards) {
	    
	    for (ProjectNode parent : parents) 
		for (ProjectNode grandparent : parent.parents)
		    grandparent.searchDependents(projectId, discards);
	}

	private void searchDependents(String projectId, Set<String> discards) {
	    for (ProjectNode dependent : dependents)
		if (dependent.projectId.equals(projectId)) 
		    discards.add(String.format("Dependency %s can be removed from project %s%n", 
			    projectId, this.projectId));

	    for (ProjectNode parent : parents) 
		parent.searchDependents(projectId, discards);
	}

}
