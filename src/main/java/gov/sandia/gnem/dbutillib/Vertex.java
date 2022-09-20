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
package gov.sandia.gnem.dbutillib;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import gov.sandia.gnem.dbutillib.util.DBDefines;

/**
 * This class encompasses the properties and behavior of a single vertex in a graph. It is assumed that any instance of
 * Vertex is an element of a collection of Vertex objects which, collectively define the graph. Each Vertex object has a
 * node comprised of a Row object, and two LinkedHashMaps that define the edges of the graph (one for children and one
 * for parents of the Vertex). The parent and children LinkedHashMaps associate table -> LinkedList of Vertex objects
 * that belong to that table.
 *
 * @author Sandy Ballard
 */
public class Vertex implements Cloneable {
    /**
     * The Row object that is encapsulated by this Vertex.
     */
    private Row row;

    /**
     * The Vertex objects that are immediate descendants of this Vertex. The children are maintained in a map from Table ->
     * list of Vertex objects. This was done this way since children are frequently requested on the basis of which
     * table they are in (such as in
     * {@link RowGraph#getChildrenOfType(java.util.Collection, String) getChildrenOfType(rows, tableType)} and
     * {@link RowGraph#getChildrenOfType(Row, String) getChildrenOfType(row, tableType)}).
     */
    private LinkedHashMap<Table, LinkedHashSet<Vertex>> children;

    /**
     * The Vertex objects that are immediate parents of this Vertex. The parents are maintained in a map from Table ->
     * list of Vertex objects. This was done this way since parents are frequently requested on the basis of which table
     * they are in (such as in
     * {@link RowGraph#getParentsOfType(java.util.Collection, String) getParentsOfType(rows, tableType)} and
     * {@link RowGraph#getParentsOfType(Row, String) getParentsOfType(row, tableType)}).
     */
    private LinkedHashMap<Table, LinkedHashSet<Vertex>> parents;

    /**
     * Constructor that encapsulates a new Row object into a Vertex object with empty parent and child connection lists.
     *
     * @param row Row that is to be encapsulated in the new Vertex.
     */
    protected Vertex(Row row) {
        this.row = row;
        this.children = new LinkedHashMap<Table, LinkedHashSet<Vertex>>();
        this.parents = new LinkedHashMap<Table, LinkedHashSet<Vertex>>();
    }

    /**
     * Constructor that encapsulates a new Row object into a Vertex object and establishes child<->parent connections
     * between the new Vertex object and the Vertex object that is the immediate parent of the new Vertex object.
     *
     * @param row    Row that is to be encapsulated in the new Vertex.
     * @param parent Vertex that is the immediate parent of the Vertex being constructed. The new vertex is added to the
     *               list of children of the parent, and the parent is added to the list of parents of the new vertex.
     */
    protected Vertex(Row row, Vertex parent) {
        this(row);
        addParent(parent);
        parent.addChild(this);
    }

    /**
     * Add a child vertex to this vertex.
     *
     * @param child vertex to be added to this vertex's children
     */
    protected void addChild(Vertex child) {
        LinkedHashSet<Vertex> set = this.children.get(child.getRow().getTable());
        // If this is the first child, create the LinkedHashSet
        if (set == null) {
            set = new LinkedHashSet<Vertex>();
            this.children.put(child.row.getTable(), set);
        }
        set.add(child);
    }

    /**
     * Return all of the children for this vertex
     *
     * @return all of the children for this vertex
     */
    protected LinkedHashSet<Vertex> getChildren() {
        LinkedHashSet<Vertex> vertexes = new LinkedHashSet<Vertex>();
        for (LinkedHashSet<Vertex> childVertexList : this.children.values())
            vertexes.addAll(childVertexList);
        return vertexes;
    }

    /**
     * Remove the specified vertex from this vertex's list of children.
     *
     * @param child vertex to be removed from this vertex's list of children
     * @return whether or not the child was removed (false if the child did not exist)
     */
    protected boolean removeChild(Vertex child) {
        boolean changed = false;
        LinkedHashSet<Vertex> set = this.children.get(child.getRow().getTable());
        if (set == null)
            return changed;

        changed = set.remove(child);
        // If that was the last child for the table, remove the table->Vertex mapping from children
        if (set.isEmpty())
            this.children.remove(child.row.getTable());

        return changed;
    }

    /**
     * Add a parent vertex to this vertex.
     *
     * @param parent vertex to be added to this vertex's parent
     */
    protected void addParent(Vertex parent) {
        LinkedHashSet<Vertex> set = this.parents.get(parent.getRow().getTable());
        // If this is the first parent, create the LinkedHashSet
        if (set == null) {
            set = new LinkedHashSet<Vertex>();
            this.parents.put(parent.row.getTable(), set);
        }
        set.add(parent);
    }

    /**
     * Return all of the parents for this vertex
     *
     * @return all of the parents for this vertex
     */
    protected LinkedHashSet<Vertex> getParents() {
        LinkedHashSet<Vertex> vertexes = new LinkedHashSet<Vertex>();
        for (LinkedHashSet<Vertex> parentVertexList : this.parents.values())
            vertexes.addAll(parentVertexList);
        return vertexes;
    }

    /**
     * Remove the specified vertex from this vertex's list of parents.
     *
     * @param parent vertex to be removed from this vertex's list of parent
     * @return whether or not the parent was removed (false if the parent did not exist)
     */
    protected boolean removeParent(Vertex parent) {
        boolean changed = false;
        LinkedHashSet<Vertex> set = this.parents.get(parent.getRow().getTable());
        if (set == null)
            return changed;

        changed = set.remove(parent);
        // If that was the last parent for the table, remove the table->Vertex mapping from parents
        if (set.isEmpty())
            this.parents.remove(parent.row.getTable());

        return changed;
    }

    /**
     * Transfer the child/parent relationships from this vertex to another vertex.
     *
     * @param toVertex vertex to transfer child/parent relationships to
     */
    protected void transferRelationships(Vertex toVertex) {
        // for all parent vertexes: remove the current vertex from the parent's child list, add toVertex to the parent's
        // child list, remove parent from current vertex's parent list, add parent to toVertex's parent list
        for (Vertex parent : getParents()) {
            parent.removeChild(this);
            parent.addChild(toVertex);

            removeParent(parent);
            toVertex.addParent(parent);
        }

        // for all children vertexes: remove the current vertex from the child's parent list, add toVertex to the
        // child's parent list, remove child from current vertex's child list, add child to toVertex's child list
        for (Vertex child : getChildren()) {
            child.removeParent(this);
            child.addParent(toVertex);

            removeChild(child);
            toVertex.addChild(child);
        }

    }

    /**
     * Return a String representation of this Vertex. This will output this Vertex's row's table type and rowId and then
     * the row.toString() output for each of the children and parents of this Vertex.
     *
     * @return a String representation of this Vertex
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.row + "   " + this.row.getTableType() + "  ");
        buf.append((this.row.getRowId().getRowIDHex()) + DBDefines.EOLN);
        buf.append("    Children:" + DBDefines.EOLN);
        for (Vertex child : getChildren())
            buf.append("      " + child.row + DBDefines.EOLN);

        buf.append("    Parents:" + DBDefines.EOLN);
        for (Vertex parent : getParents())
            buf.append("      " + parent.row + DBDefines.EOLN);

        buf.append(DBDefines.EOLN);
        return buf.toString();
    }

    /**
     * Return this vertex's row.
     *
     * @return this vertex's row.
     */
    protected Row getRow() {
        return this.row;
    }

    /**
     * Return this vertex's children that belong to the specified table type
     *
     * @param table specifies which table the returned children should belong to
     * @return this vertex's children that belong to the specified table type
     */
    protected LinkedHashSet<Vertex> getChildren(Table table) {
        return this.children.get(table);
    }

    /**
     * Return this vertex's parents that belong to the specified table type
     *
     * @param table specifies which table the returned parents should belong to
     * @return this vertex's parents that belong to the specified table type
     */
    protected LinkedHashSet<Vertex> getParents(Table table) {
        return this.parents.get(table);
    }
}
