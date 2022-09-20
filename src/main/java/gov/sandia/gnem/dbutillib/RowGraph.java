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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import gov.sandia.gnem.dbutillib.jaxb.ObjectFactory;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * RowGraph manages a set of connected rows from a database. Mathematically, this set of rows forms a directed graph
 * consisting of a set of vertexes (the rows) and edges (one-way connections between pairs of vertexes). Starting from
 * any row, the user can visit every other row that is connected to the starting row by traversing the edges of this
 * graph. The connections are defined by the schema relationships specified by the user.
 * <p>
 * No two rows in a RowGraph are 'equal'. Row equality is defined in two ways. First, if two rows have the same rowID,
 * they are equal. Second, if two rows come from the same table, and that table has unique keys, and the values of all
 * the unique keys in the two rows are 'equal' (as defined by {@link Column#evaluateEquality(Object, Object)
 * Column.evalateEquality(value1, value2)}), then the two rows are equal.
 * <p>
 * RowGraph implements the Set interface, which allows calling routines to iterate over the Rows in an instance of a
 * RowGraph. Many of the methods defined in the Set interface are overloaded.
 * <p>
 * RowGraph can operate in either of two modes. In the first mode, the RowGraph object goes out to the database and
 * extracts the entire graph of connected rows and stores it in memory, including both the set of vertexes and all the
 * associated connections. Once this relatively time consuming operation has been completed, the user can traverse the
 * graph as many times as desired, accessing the graph in memory, which will be faster then re-executing all the SQL
 * statements. This mode of operation is implemented when RowGraph constructors that include initialRow parameters are
 * invoked, or when calls to the assembleGraph methods ({@link #assembleGraph(Row) assembleGraph(Row)} or
 * {@link #assembleGraph(Collection) assembleGraph(Collection<Row>)}) are made.
 * <p>
 * In the second mode of operation, relatively small batches of rows are extracted from the database and made available
 * to the user one time. SELECT statements against the database are executed as the need arises. Results of select
 * statements are stored in memory only until the extracted rows have been processed, then they are released for garbage
 * collection. This mode of operation is implemented by invoking the RowGraph constructor that takes only a Schema
 * parameter. Rows are retrieved from the database with the getRows() methods ({@link #getRows() getRows()},
 * {@link #getRows(Row) getRows(Row)}, {@link #getRows(Collection) getRows(Collection<Row>)}) or Iterators (
 * {@link #iterator() iterator()}, {@link #iterator(Collection) iterator(Collection<Row>)}, {@link #iterator(Row)
 * iterator(Row)}) that take initialRow parameters.
 * <p>
 * Hybrid sequences may come up: If a graph is assembled into memory starting from row1, and then later an iterator is
 * invoked starting from row2 where row2 is not a member of the graph currently in memory, then rows will be retrieved
 * from the database, as needed, and returned. If any rows are encountered that are in the graph in memory, the
 * subsequent calls to the iterator will get rows from the graph in memory.
 * <p>
 * When a RowGraph is assembled into memory, then a number of useful methods are available for extracting Collections of
 * Rows from the RowGraph. {@link #getRows() getRows()} returns the Set of all the Rows in the graph,
 * {@link #getRowsOfType(String) getRowsOfType(tableType)} returns the Set of all the Rows in the graph that come from a
 * table of a specified type, {@link #getChildren(Row) getChildren(row)} returns the Set of all the direct descendants
 * of the specified Row, {@link #getChildrenOfType(Row, String) getChildrenOfType(row, tableType)} returns the Set of
 * all the descendants of a specified row that come from a table of the specified type, and many more.
 * <p>
 * Using the Iterator methods, RowGraphs are traversed in breadth-first order.
 * <p>
 * The assembleGraph() methods ({@link #assembleGraph(Row) assembleGraph(Row)} or {@link #assembleGraph(Collection)
 * assembleGraph(Collection<Row>)}) will add sub graphs to an existing graph. assembleGraph methods are called
 * automatically by RowGraph constructors that take initialRow parameters. When called with a collection of rows,
 * {@link #assembleGraph(Collection) assembleGraph(Collection<Row>)} assembles one graph that includes all the rows
 * connected to each of the initial rows. It is possible that a graph may have one or more disconnected sub graphs
 * (i.e., it may be impossible to reach any of the rows in one sub graph from any of the rows in any of the other
 * subgraphs). Repeated calls to assembleGraph(), without intervening calls to {@link #clear() clear()}, result in the
 * addition of more rows (without duplication) to the existing graph, not replacement of the existing graph.
 * <p>
 * RowGraphs can be disassembled and later reassembled, which is useful for passing RowGraph objects between
 * applications. See methods {@link #add(Row, Collection, Collection) add(row, children, parents)} and
 * {@link #createLinks() createLinks()} for details.
 *
 * @author Sandy Ballard
 */
public class RowGraph implements Set<Row>, Cloneable {
    /**
     * RowGraph stores a mapping from a Row object's RowID to a Vertex object. It basically defines the mathematical
     * RowGraph object since each Vertex contains a Row object as well as collections of other Vertex objects that
     * represent the parents and children of the Vertex. rowGraph is filled during construction of a graph in memory and
     * persists after graph construction.
     */
    private HashMap<RowID, Vertex> graph = new HashMap<RowID, Vertex>(4096);

    /**
     * rowMap is a LinkedHashMap from a Table -> ArrayList of Vertex objects for vertexes with Rows belonging to that
     * table. Used to allow efficient requests for all Rows from a table of a particular type.
     */
    private LinkedHashMap<Table, ArrayList<Vertex>> rowMap = new LinkedHashMap<Table, ArrayList<Vertex>>(100);

    /**
     * Set of Schema objects that are represented in this rowGraph.
     */
    private HashSet<Schema> schemas = new HashSet<Schema>();

    /**
     * This RowGraph's RowGraphConstraints. Keeps track of any violations encountered.
     */
    private RowGraphConstraints constraints = new RowGraphConstraints(this);

    /**
     * Temporary storage when a bunch of rows need to be added at once and the links between them created later. This is
     * populated by {@link #add(Row, Collection, Collection) add(Row, Collection, Collection)} and the links are created
     * for its contents with {@link #createLinks() createLinks()}.
     */
    private ArrayList<TempGraph> createLinksTempGraph = new ArrayList<TempGraph>();

    /**
     * With this constructor, no RowGraph is loaded into memory since no initialRow(s) are specified. A RowGraph
     * constructed with this constructor can still be traversed by using the iterators that take populate the RowGraph
     * based on initialRow arguments. It can also be populated using getRows() methods that take initialRow arguments.
     * Returned rows are obtained from the database. The iterator and getRows() methods that take no initialRow
     * arguments will return empty Collections since no initialRows are specified.
     */
    public RowGraph() {
        // No action needed (see comments)
    }

    /**
     * With this constructor, a RowGraph is automatically assembled into memory, starting from the specified initialRow.
     * This will retrieve all rows related to initialRow based on the relationships defined in the schema the
     * intialRow's table belongs to
     *
     * @param initialRow the Row object from which RowGraph construction should begin.
     */
    public RowGraph(Row initialRow) {
        assembleGraph(initialRow);
    }

    /**
     * With this constructor, a RowGraph is automatically assembled into memory, starting from the specified
     * initialRows. This will retrieve all rows related to initialRows based on the relationships defined in the schema
     * the intialRows' tables belong to
     *
     * @param initialRows the Row objects from which RowGraph assembly should begin.
     */
    public RowGraph(Collection<Row> initialRows) {
        assembleGraph(initialRows);
    }

    /**
     * With this constructor, a RowGraph is automatically assembled into memory, starting from the Rows returned by
     * executing 'SELECT * FROM table.name WHERE whereClause'. This constructor executes the select statement and then
     * creates a RowGraph that contains all of the Rows returned by the select statement and all the Rows that are
     * connected to those rows by all the Relationships specified in the schema that table belongs to.
     *
     * @param table       the Table object against which the select statement should be executed.
     * @param whereClause the where clause of the sql statement. This can be anything that that comes after table.name
     *                    in the select statement.
     * @throws FatalDBUtilLibException if an error occurs while retrieving the data
     */
    public RowGraph(Table table, String whereClause) throws FatalDBUtilLibException {
        try {
            assembleGraph(table.getSchema().getDAO().executeSelectStatement(table, whereClause));
        } catch (FatalDBUtilLibException e) {
            String error = "Error in RowGraph constructor.\nError" + " message: " + e.getMessage();
            throw new FatalDBUtilLibException(error);
        }
    }

    /** ~*~*~*~*~*~*~*~ START INTERFACE IMPLEMENTATION AND OVERRIDDEN METHODS ~*~*~*~*~*~*~*~ */

    /**
     * Add a Row object to the current graph. Parent/child links are established between the new row and rows that are
     * already members of the graph, using the relationships specified in the schema specified when the rowGraph was
     * constructed.
     * <p>
     * Currently, the only relationships that are recognized are relationships like "where thisID = #thatID#". The only
     * recognized operator is '=', which must appear in the relationship exactly once. If the preceding conditions are
     * met, and 'thisID' is a column in row and 'thatID' is a column in the row that is being evaluated, and
     * thisID.equals(thatID), then parent/child links are established between the two rows. Otherwise, no links are
     * established.
     * <p>
     * Note that a check is made to see if there is a row already in the graph that is 'equal' to the new row. If there
     * is a row already in the graph with the same rowID as the new row, the method returns without modifying the graph.
     * If there is a row in the graph that enjoys equality with the new row based on the values of their unique keys,
     * then the new row is not added to the graph but all of its parent/child links are determined and assigned to the
     * row that is already in the graph.
     *
     * @param r the Row object to be added to the row graph.
     * @return true if the graph was modified by the addition of row.
     */
    public boolean add(Row r) {
        return add(r, true);
    }

    /**
     * Add a Collection of Row objects to the current graph. This method just calls {@link #add(Row) add(Row)} on each
     * Row in the collection of Rows.
     *
     * @param rows the Collection of Row objects to be added to the graph.
     * @return true if the graph was modified as a result of adding any of the Rows in the Collection.
     */
    public boolean addAll(Collection<? extends Row> rows) {
        boolean any = false;
        for (Row row : rows)
            if (add(row))
                any = true;

        return any;
    }

    /**
     * Clear the contents of the current RowGraph object.
     */
    public void clear() {
        this.rowMap.clear();
        this.graph.clear();
        this.schemas.clear();
    }

    /**
     * Determine whether or not the rowgraph in memory contains a particular Row object (or a row that is 'equal' to the
     * input Row).
     *
     * @param row the Row object whose presence in the graph in memory is to be determined.
     * @return true if row is a member of the graph in memory, false otherwise.
     */
    public boolean contains(Object row) {
        // The Set interface requires that contains(Object) be implemented, so just double check that we are indeed
        // getting a Row object. If not, there's no way the RowGraph has that object!
        if (!row.getClass().getName().equals("gov.sandia.gnem.dbutillib.Row"))
            return false;
        return this.graph.containsKey(((Row) row).getRowId());
    }

    /**
     * Determine whether or not the row graph in memory contains all of the Row objects in the Collection of Row
     * objects.
     *
     * @param rows the Collection of Row objects whose presence in the graph in memory is to be determined.
     * @return true if all elements of rows are members of the graph in memory.
     */
    public boolean containsAll(Collection<?> rows) {
        for (Iterator<?> i = rows.iterator(); i.hasNext(); )
            if (!contains(i.next()))
                return false;
        return true;
    }

    /**
     * Determines whether or not the RowGraph contains any rows.
     *
     * @return true if the RowGraph does not contain any rows.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Get an Iterator over all the rows in the RowGraph in memory.
     *
     * @return an Iterator over the Rows in the RowGraph.
     */
    public Iterator<Row> iterator() {
        return new RowGraphIterator();
    }

    /**
     * Removes a Row object from the RowGraph. Removes all references to the row from the children and parents of all
     * other Rows.
     *
     * @param row the Row object to be removed from the row graph.
     * @return true if the row graph was modified.
     */
    public boolean remove(Object row) {
        return removeRow((Row) row);
    }

    /**
     * Remove a collection of Row objects from the row graph.
     *
     * @param rows the Collection of Rows to be removed from the graph.
     * @return true if the RowGraph was modified in any way.
     */
    public boolean removeAll(Collection<?> rows) {
        boolean changed = false;
        for (Iterator<?> it = rows.iterator(); it.hasNext(); ) {
            Row row = (Row) it.next();
            if (remove(row))
                changed = true;
        }
        return changed;
    }

    /**
     * Retains only the rows in this row graph that are contained in the specified collection. In other words, removes
     * from this row graph all of its rows that are not contained in the specified collection.
     *
     * @param rows a Collection of Row objects.
     * @return true if this row graph is modified during this operation.
     */
    public boolean retainAll(Collection<?> rows) {
        boolean changed = false;

        HashSet<Row> keepers = new HashSet<Row>();
        for (Object row : rows)
            keepers.add((Row) row);

        // Iterate over all rows. If a row is not in keepers, then remove it
        for (Row row : getRows()) {
            if (!keepers.contains(row) && removeRow(row))
                changed = true;
        }
        return changed;
    }

    /**
     * Return the number of rows that the graph in memory contains.
     *
     * @return the number of rows that the graph in memory contains.
     */
    public int size() {
        return this.graph.size();
    }

    /**
     * Returns all the Row objects in the row graph in memory in a Row[]
     *
     * @return all the Row objects in the row graph in memory in a Row[]
     */
    public Row[] toArray() {
        Row[] rowsArray = new Row[size()];
        int j = 0;
        for (Vertex vertex : this.graph.values())
            rowsArray[j++] = vertex.getRow();
        return rowsArray;
    }

    /**
     * The set interface defines this method as " Returns an array containing all of the elements in this set; the
     * runtime type of the returned array is that of the specified array." This is currently not supported by RowGraph.
     */
    public <T> T[] toArray(T[] a) {
        throw new java.lang.UnsupportedOperationException("RowGraph: <T> T[] toArray(T[] a)");
    }

    /**
     * Create and return a copy of this RowGraph.
     *
     * @return a copy of this RowGraph
     */
    @Override
    public RowGraph clone() {
        // Create the new RowGraph
        RowGraph newGraph = new RowGraph();

        // Add clones of the rows represented in graph to the new RowGraph being created
        for (Vertex v : this.graph.values())
            newGraph.add(v.getRow().clone(), getChildrenRowIDs(v.getRow()), getParentsRowIDs(v.getRow()));
        try {
            newGraph.createLinks();
        } catch (Exception ex) {
            DBDefines.ERROR_LOG.add("ERROR in RowGraph.clone().  Returning null.");
            return null;
        }
        return newGraph;
    }

    /**
     * Returns a String representation of this rowgraph. There is an entry for each Row in the RowGraph containing: the
     * type of the table the row comes from, the string representation of the row, the row's RowID, the children of the
     * row and the parents of the row. The rows are presented in essentially random order.
     *
     * @return String string representation of this rowgraph.
     */
    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();
        for (Vertex vertex : this.graph.values())
            returnString.append(vertex.toString());
        return returnString.toString();
    }

    /** ~*~*~*~*~*~*~*~ END INTERFACE IMPLEMENTATION AND OVERRIDDEN METHODS ~*~*~*~*~*~*~*~ */

    /** ~*~*~*~*~*~*~*~ "GRAPHY" METHODS ~*~*~*~*~*~*~*~ */

    /**
     * Add a Row object to the current graph. If connect is true, parent/child links are established between the new row
     * and rows that are already members of the graph, using the relationships specified in the schema specified when
     * the rowGraph was constructed.
     * <p>
     * Currently, the only relationships that are recognized are relationships like "where thisID = #thatID#". The only
     * recognized operator is '=', which must appear in the relationship exactly once. If the preceding conditions are
     * met, and 'thisID' is a column in row and 'thatID' is a column in the row that is being evaluated, and
     * thisID.equals(thatID), then parent/child links are established between the two rows. Otherwise, no links are
     * established.
     * <p>
     * Note that a check is made to see if there is a row already in the graph that is 'equal' to the new row. If there
     * is a row already in the graph with the same rowID as the new row, the method returns without modifying the graph.
     * If there is a row in the graph that enjoys equality with the new row based on the values of their unique keys,
     * then the new row is not added to the graph but all of its parent/child links are determined and assigned to the
     * row that is already in the graph. Just like {@link #add(Row) add(Row)} method except for the connect parameter.
     * If connect is true, then all of row's relationships are evaluated and child<->parent links created. If false, the
     * row is added to the graph with no links. If false, the vertex will have children and parents HashMaps that are
     * empty.
     *
     * @param row     the Row object to be added to the row graph.
     * @param connect whether or not to evaluate the row's relationships in order to create child<-> parent links
     * @return true if the graph was modified by the addition of row.
     */
    public boolean add(Row row, boolean connect) {
        // If this exact row is already a member of the graph, no need to add it again.
        if (this.graph.containsKey(row.getRowId()))
            return false;

        // Create the vertex object that will be added to the graph
        Vertex vertex = new Vertex(row);

        if (!connect) {
            addVertexToGraph(vertex);
            return true;
        }

        // If row's table is a target table in a schema relationship, iterate over all the rows in the rowGraph that are
        // members of the table that is the source table in the relationship to see if any of those rows are related to
        // row
        for (Relationship relationship : row.getSchema().getRelationshipsForTargetTable(row.getTableType())) {
            ArrayList<Vertex> sourceVertexes = this.rowMap.get(relationship.sourceTable);
            if (sourceVertexes == null)
                continue;

            for (Vertex sourceVertex : sourceVertexes) {
                // see if the relationship row -> sourceRow is satisfied.
                if (relationship.evaluate(sourceVertex.getRow(), row)) {
                    // add sourceRow to row's parents and row to sourceRow's children.
                    vertex.addParent(sourceVertex);
                    sourceVertex.addChild(vertex);
                }
            }
        }

        // If row's table is a source table in a schema relationship, iterate over all the rows in the rowGraph that are
        // members of the table that is the target table in the relationship to see if any of those rows are related to
        // row
        for (Relationship relationship : row.getSchema().getRelationshipsForSourceTable(row.getTable().getTableType())) {
            ArrayList<Vertex> targetVertexes = this.rowMap.get(relationship.targetTable);
            if (targetVertexes == null)
                continue;

            for (Vertex targetVertex : targetVertexes) {
                // see if the relationship row -> targetRow is satisfied.
                if (relationship.evaluate(row, targetVertex.getRow())) {
                    // add targetRow to row's children and row to targetRow's parents.
                    vertex.addChild(targetVertex);
                    targetVertex.addParent(vertex);
                }
            }
        }
        addVertexToGraph(vertex);
        return true;
    }

    /**
     * Add a Vertex object to the graph. This method adds a link from row.getRowId() -> vertex to the graph map, and
     * adds an entry to the rowMap object as well. Since these two objects must stay in sync, vertexes should only be
     * added to the graph using this method.
     *
     * @param vertex the Vertex to be added to the graph
     */
    private void addVertexToGraph(Vertex vertex) {
        // Add an entry to rowGraph where key = the row's rowID and the value is a reference to the vertex.
        this.graph.put(vertex.getRow().getRowId(), vertex);

        // Add it to rowMap which is a HashMap from row.table -> LinkedHashSet<Row>. Used to allow efficient requests
        // for all Rows from a table of a particular type.
        ArrayList<Vertex> vertexes = this.rowMap.get(vertex.getRow().getTable());
        if (vertexes == null) {
            vertexes = new ArrayList<Vertex>();
            this.rowMap.put(vertex.getRow().getTable(), vertexes);
        }
        vertexes.add(vertex);

        // add the schema that this row belongs to to the set of schemas maintained by the rowgraph.
        this.schemas.add(vertex.getRow().getSchema());
    }

    /**
     * Remove a row from the graph. Called by several other public remove functions.
     *
     * @param row the Row object to be removed.
     * @return true if the graph was modified by removal of the row, false if no changes were made to the row graph (row
     * was not a member of the graph to start with).
     */
    boolean removeRow(Row row) {
        return removeVertex(this.graph.get(row.getRowId()));
    }

    /**
     * Remove a Vertex from the graph. Called by several other public remove functions.
     *
     * @param vertex the vertex to be removed.
     * @return true if the graph was modified by removal of the vertex, false if no changes were made to the row graph
     * (vertex was not a member of the graph to start with).
     */
    private boolean removeVertex(Vertex vertex) {
        boolean changed = false;

        if (vertex == null)
            return changed;

        // remove rowID from rowGraph
        if (this.graph.remove(vertex.getRow().getRowId()) != null)
            changed = true;

        // remove the row from rowMap.
        ArrayList<Vertex> list = this.rowMap.get(vertex.getRow().getTable());
        if (list != null) {
            changed = list.remove(vertex);
            // Remove the table from rowMap if removing this row made the list of rows belonging to that table empty
            if (list.isEmpty())
                this.rowMap.remove(vertex.getRow().getTable());
        }

        // remove this row from its parents' lists of children.
        for (Vertex parent : vertex.getParents())
            parent.removeChild(vertex);

        // remove it from all of its children's lists of parents.
        for (Vertex child : vertex.getChildren())
            child.removeParent(vertex);

        return changed;
    }

    /**
     * Return the row corresponding to rowID if it is present in this RowGraph; return null if it is not present.
     *
     * @param rowID rowID for the desired row
     * @return the row corresponding to rowID if it is present in this RowGraph; return null if it is not present.
     */
    public Row getRow(RowID rowID) {
        Vertex vertex = this.graph.get(rowID);
        if (vertex == null)
            return null;
        return vertex.getRow();
    }

    /**
     * Return all of the Row objects that are in memory for this RowGraph. If the graph is composed of multiple
     * disconnected sub graphs, all rows from all subgraphs are included. If the RowGraph in memory is empty, then this
     * method returns an empty Set. All information about descendants and ancestors is lost.
     *
     * @return a LinkedHashSet containing all the Row objects in the graph in memory.
     */
    public Set<Row> getRows() {
        LinkedHashSet<Row> rows = new LinkedHashSet<Row>();
        for (ArrayList<Vertex> i : this.rowMap.values())
            rows.addAll(extractRows(i));
        return rows;
    }

    /**
     * Retrieve a RowGraph from the database as a LinkedList of Row objects, starting from a specified initial Row. If
     * there is a graph in memory, it is cleared and then initialized with the input row as the initial row. The graph
     * is traversed with Rows being retrieved from the database. The retrieved rows are returned in a LinkedList of Row
     * objects. All information about descendants and ancestors is lost. After a call to this method, the rowGraph in
     * memory will be empty.
     *
     * @param initialRow row from which graph traversal should begin
     * @return Row objects which comprise all the rows of the RowGraph
     */
    public List<Row> getRows(Row initialRow) {
        // initialize temporary collections.
        clear();
        LinkedList<Row> rows = new LinkedList<Row>();
        for (Iterator<Row> it = iterator(initialRow); it.hasNext(); )
            rows.add(it.next());
        return rows;
    }

    /**
     * Retrieve a RowGraph from the database as a LinkedList of Row objects, starting from a specified Collection of
     * initial Rows. If there is a graph in memory, it is cleared and then initialized with the input rows as the
     * initial rows. The graph is traversed with Rows being retrieved from the database. The retrieved rows are returned
     * in a LinkedList of Row objects. All information about descendants and ancestors is lost. After a call to this
     * method, the rowGraph in memory will be empty.
     *
     * @param initialRows The row from which graph traversal should begin.
     * @return a LinkedList of Row objects which comprise all the rows of the RowGraph.
     */
    public List<Row> getRows(Collection<Row> initialRows) {
        // initialize temporary collections.
        clear();

        LinkedList<Row> rows = new LinkedList<Row>();
        if (initialRows == null || initialRows.size() == 0)
            return rows;

        for (Iterator<Row> i = iterator(initialRows); i.hasNext(); )
            rows.add(i.next());
        return rows;
    }

    /**
     * Extracts Row objects from a Set of Vertex objects
     *
     * @param vertexes the Vertex objects to extract Row objects from
     * @return Row objects extracted from vertexes
     */
    private Set<Row> extractRows(Collection<Vertex> vertexes) {
        LinkedHashSet<Row> rows = new LinkedHashSet<Row>();

        if (vertexes == null)
            return rows;
        for (Vertex vertex : vertexes)
            rows.add(vertex.getRow());
        return rows;
    }

    /**
     * Return the Row objects of the specified table type that conform to the specified schema. All information about
     * descendants and ancestors is lost.
     *
     * @param rowSchema the schema from which rows are to be returned
     * @param tableType the type of the table whose rows are desired
     * @return rows of the specified table type that conform to the specified schema
     */
    public Set<Row> getRowsOfType(Schema rowSchema, String tableType) {
        LinkedHashSet<Row> rows = new LinkedHashSet<Row>();
        if (this.schemas.contains(rowSchema)) {
            Table table = rowSchema.getTableOfType(tableType);
            if (table != null)
                rows.addAll(getRowsOfType(table));
        }
        return rows;
    }

    /**
     * Return the Row objects in the graph which come from tables of a specified type. If the graph is composed of
     * multiple schemas, then rows of the specified type are retrieved from all schemas. All information about
     * descendants and ancestors is lost.
     *
     * @param tableType the type of the table whose rows are desired
     * @return rows whose table is of type tableType
     */
    public Set<Row> getRowsOfType(String tableType) {
        LinkedHashSet<Row> rows = new LinkedHashSet<Row>();
        for (Schema schema : this.schemas) {
            Table table = schema.getTableOfType(tableType);
            if (table != null)
                rows.addAll(getRowsOfType(table));
        }
        return rows;
    }

    /**
     * Return the Row objects in the graph which come from a specific Table object. All information about descendants
     * and ancestors is lost.
     *
     * @param table table whose rows are desired.
     * @return rows whose table is equal to the specified table
     */
    public Set<Row> getRowsOfType(Table table) {
        return extractRows(this.rowMap.get(table));
    }

    /**
     * Retrieve all the rows in the rowGraph that come from a particular schema.
     *
     * @param schema the Schema whose rows are being requested.
     * @return all the rows in the rowGraph that come from a particular schema.
     */
    public Set<Row> getRowsOfType(Schema schema) {
        LinkedHashSet<Row> rows = new LinkedHashSet<Row>();
        for (Table table : schema.getTables())
            rows.addAll(getRowsOfType(table));
        return rows;
    }

    /**
     * Return all the Row objects that are direct descendants of the specified row; if the specified row is not a member
     * of the graph in memory, an empty Set is returned.
     *
     * @param row row whose direct descendants are desired.
     * @return Row objects that are direct descendants of the specified row; if the specified row is not a member of the
     * graph in memory, an empty Set is returned.
     */
    public Set<Row> getChildren(Row row) {
        Vertex vertex = this.graph.get(row.getRowId());
        if (vertex == null)
            return new LinkedHashSet<Row>();
        return extractRows(vertex.getChildren());
    }

    /**
     * Return the rowIds of all the children of the specified Row object.
     *
     * @param row the Row for whom the children's rowIDs are desired.
     * @return the rowIds of all the children of the specified Row object.
     */
    public Set<String> getChildrenRowIDs(Row row) {
        // create a LinkedList of row's children's (String)rowIDs.
        LinkedHashSet<String> childrenRowIds = new LinkedHashSet<String>();
        for (Row r : getChildren(row))
            childrenRowIds.add(r.getRowId().getRowIDHex());
        return childrenRowIds;
    }

    /**
     * Return the Row objects that are direct descendants of the specified row and which come from a table of a
     * specified type.
     *
     * @param row       row whose direct descendants are desired.
     * @param tableType the type of the table whose rows are desired.
     * @return Row objects that are direct descendants of the specified row and which come from a table of a specified
     * type
     */
    public Set<Row> getChildrenOfType(Row row, String tableType) {
        Vertex vertex = this.graph.get(row.getRowId());

        if (vertex == null)
            return new LinkedHashSet<Row>();

        Table table = row.getSchema().getTableOfType(tableType);

        if (table == null)
            return new LinkedHashSet<Row>();

        return extractRows(vertex.getChildren(table));
    }

    /**
     * Return the Row objects that are direct descendants of the specified rows and which come from a table of a
     * specified type.
     *
     * @param rows      rows whose direct descendants are desired.
     * @param tableType the type of the table whose rows are desired.
     * @return Row objects that are direct descendants of the specified rows and which come from a table of a specified
     * type
     */
    public Set<Row> getChildrenOfType(Collection<Row> rows, String tableType) {
        LinkedHashSet<Row> children = new LinkedHashSet<Row>();
        for (Row row : rows)
            children.addAll(getChildrenOfType(row, tableType));
        return children;
    }

    /**
     * Return all the Row objects that are direct ancestors of the specified row. If row is not a member of the graph in
     * memory, null is returned.
     *
     * @param row row whose direct ancestors are desired.
     * @return Row objects that are direct ancestors of the specified row. If row is not a member of the graph in
     * memory, null is returned.
     */
    public Set<Row> getParents(Row row) {
        Vertex vertex = this.graph.get(row.getRowId());
        if (vertex == null)
            return new LinkedHashSet<Row>();
        return extractRows(vertex.getParents());
    }

    /**
     * Return the rowIds of all the parents of the specified Row object.
     *
     * @param row the Row for whom the parents' rowIDs are desired.
     * @return the rowIds of all the parents of the specified Row object.
     */
    public Set<String> getParentsRowIDs(Row row) {
        LinkedHashSet<String> parentRowIds = new LinkedHashSet<String>();
        for (Row r : getParents(row))
            parentRowIds.add(r.getRowId().getRowIDHex());
        return parentRowIds;
    }

    /**
     * Return the Row objects that are direct ancestors of the specified row and which come from a table of a specified
     * type.
     *
     * @param row       row whose direct ancestors are desired.
     * @param tableType the type of table whose rows are desired.
     * @return a Row objects that are direct ancestors of the specified row and which come from a table of a specified
     * type. If row is not a member of the graph in memory, an empty list is returned.
     */
    public Set<Row> getParentsOfType(Row row, String tableType) {
        Vertex vertex = this.graph.get(row.getRowId());
        if (vertex == null)
            return new LinkedHashSet<Row>();

        Table table = row.getSchema().getTableOfType(tableType);
        if (table == null)
            return new LinkedHashSet<Row>();

        return extractRows(vertex.getParents(table));
    }

    /**
     * Return the Row objects that are direct ancestors of the specified rows and which come from a table of a specified
     * type.
     *
     * @param rows      rows whose direct ancestors are desired.
     * @param tableType the type of table whose rows are desired.
     * @return a Row objects that are direct ancestors of the specified rows and which come from a table of a specified
     * type. If row is not a member of the graph in memory, an empty list is returned.
     */
    public Set<Row> getParentsOfType(Collection<Row> rows, String tableType) {
        LinkedHashSet<Row> parents = new LinkedHashSet<Row>();
        for (Row row : rows)
            parents.addAll(getParentsOfType(row, tableType));
        return parents;
    }

    /**
     * Assemble a graph from the database into memory, starting from a single initial row. If a graph is to be assembled
     * starting from multiple starting rows, it is more efficient to collect all the initialRows into a collection of
     * Row objects and send them to {@link #assembleGraph(Collection) assembleGraph(Collection)} all at once. If there
     * is no graph currently in memory then one is assembled. To empty a graph already in memory before assembling a new
     * one, use the {@link #clear() clear()} method. If there is already a graph in memory, and initialRow is a member
     * of it, this method returns immediately without doing anything. If there is a graph already in memory and
     * initialRow is a not member of it, then all rows connected to initialRow are added to the existing graph, without
     * duplication. This method is called automatically by the {@link #RowGraph(Row) RowGraph(Row)} constructor.
     *
     * @param initialRow The single row from which graph assembly should be initiated.
     */
    public void assembleGraph(Row initialRow) {
        // stack is used during graph assembly for temporary storage of vertexes that are waiting to be processed.
        VertexStack stack = new VertexStack();

        // push the initial row onto stack
        stack.push(new Vertex(initialRow));

        // load the graph, starting from the single element of stack.
        loadGraphIntoMemory(this, stack);
    }

    /**
     * Assemble a graph from the database into memory, starting from multiple initial rows. If a graph is to be
     * assembled starting from multiple starting rows, it is more efficient to collect all the initialRows into a
     * collection of Row objects and send them to {@link #assembleGraph(Collection) assembleGraph(Collection)} all at
     * once. If there is no graph currently in memory then one is assembled. To empty a graph already in memory before
     * assembling a new one, use the {@link #clear() clear()} method. If there is already a graph in memory, and all
     * rows in initialRows are members of it, this method returns immediately without doing anything. If there is a
     * graph already in memory and any elements of initialRows are not members of it, then all rows connected to unique
     * initialRows are added to the existing graph, without duplication. This method is called automatically by the
     * {@link #RowGraph(Collection) RowGraph(Collection<Row>)} constructor.
     *
     * @param initialRows rows from which graph assembly should be initiated.
     */
    public void assembleGraph(Collection<Row> initialRows) {
        if (initialRows == null || initialRows.isEmpty())
            return;

        // stack is used during graph assembly for temporary storage of vertexes that are waiting to be processed.
        VertexStack stack = new VertexStack();

        // push all the initial rows onto stack
        for (Row row : initialRows)
            stack.push(new Vertex(row));

        // load the graph into memory, starting from the elements of stack.
        loadGraphIntoMemory(this, stack);
    }

    /**
     * Extract a subgraph from this rowgraph, starting from a single initial row.
     *
     * @param initialRow row from which subgraph assembly should be initiated.
     * @return a RowGraph object that is a subgraph of this graph and which contains rows that are linked to the initial
     * row by one or more table relationships.
     */
    public RowGraph getSubGraph(Row initialRow) {
        // stack is used during graph assembly for temporary storage of vertexes that are waiting to be processed.
        VertexStack stack = new VertexStack();
        RowGraph subGraph = new RowGraph();

        // push the initialRow onto stack
        stack.push(new Vertex(initialRow));

        // load the graph, starting from the single element of stack.
        loadGraphIntoMemory(subGraph, stack);

        return subGraph;
    }

    /**
     * Extract a subgraph from this rowgraph, starting from a Collection of Row objects.
     *
     * @param initialRows rows from which subgraph extraction should be initiated.
     * @return a RowGraph object that is a subgraph of this graph and which contains rows that are linked to the initial
     * rows by one or more table relationships.
     */
    public RowGraph getSubGraph(Collection<Row> initialRows) {
        RowGraph subGraph = new RowGraph();
        if (initialRows == null)
            return subGraph;

        // stack is used during graph assembly for temporary storage of vertexes that are waiting to be processed.
        VertexStack stack = new VertexStack();

        // push all the rows onto stack
        for (Row row : initialRows)
            stack.push(new Vertex(row));

        // load the graph, starting from the elements on stack.
        loadGraphIntoMemory(subGraph, stack);

        return subGraph;
    }

    /**
     * Extract a sub graph from this row graph, starting from all the rows in this row graph that come from a table of a
     * specified type.
     *
     * @param table the table whose rows are to serve as initial rows for sub graph extraction.
     * @return a RowGraph object that is a sub graph of this graph and which contains all of the rows that are linked to
     * the rows belonging to the specified table by one or more table relationships.
     */
    public RowGraph getSubGraph(Table table) {
        return getSubGraph(getRowsOfType(table));
    }

    /**
     * Extract a sub graph from this row graph, starting from all the rows in this row graph that belong to the
     * specified schema.
     *
     * @param schema the schema whose rows are to serve as initial rows for sub graph extraction.
     * @return a RowGraph object that is a sub graph of this graph and which contains all of the rows that are linked to
     * the rows belonging to the specified schema by one or more table relationships.
     */
    public RowGraph getSubGraph(Schema schema) {
        return getSubGraph(getRowsOfType(schema));
    }

    /**
     * Remove a subgraph of rows from this rowgraph. First, the subGraph(s) that originate from the rows in roots is
     * identified. Members of this subgraph are only removed if doing so will not result in the creation of any orphans.
     * <p>
     * For example, consider an arrival row that is a member of the subgraph to be deleted but that has two assoc
     * parents. One of the assocs is a member of the subgraph that is to be removed and one is not. The assoc that is a
     * member of the subgraph will be removed but the other assoc will not and neither will the arrival since doing so
     * would break the assoc->arrival relationship of the assoc that is not being removed.
     * <p>
     * <b>Important Note</b>: all elements of the roots will be removed, even if doing so would create orphans. Also,
     * this method is not too terribly quick ...
     *
     * @param roots rows that serve as the starting point(s) for acquiring the subgraph of Rows to be deleted.
     * @return boolean true if any rows are deleted from the rowgraph, false otherwise.
     */
    public boolean removeSubGraph(Collection<Row> roots) {
        // Keep track of whether or not a row can be removed
        HashMap<RowID, Boolean> canRemove = new HashMap<RowID, Boolean>();
        // Keep track of which rows in roots are actually in the current RowGraph
        HashSet<Row> start = new HashSet<Row>();

        // For all rows in roots that are in the graph, initialize their "able to be removed" setting to true
        for (Row root : roots) {
            if (this.graph.keySet().contains(root.getRowId())) {
                canRemove.put(root.getRowId(), new Boolean(true));
                start.add(root);
            }
        }

        // subGraph originates from all the rows in roots that are represented in this rowgraph.
        RowGraph subGraph = getSubGraph(start);

        boolean dependenciesResolved = false;
        // Loop until we have succeeded in removing the subgraph
        while (!dependenciesResolved) {
            dependenciesResolved = true;

            for (Iterator<Row> it = subGraph.iterator(start); it.hasNext(); ) {
                Row row = it.next();
                RowID rowid = row.getRowId();

                // It has already been decided that this row is okay to remove (its removal won't orphan any other rows)
                if (canRemove.containsKey(rowid))
                    continue;

                // Initially, assume this vertex can be removed.
                canRemove.put(rowid, new Boolean(true));

                // Iterate over all the parents of this member of subgraph. If this vertex has a parent that is not a
                // member of the subgraph then this vertex cannot be removed from the rowgraph. Or, if this vertex has a
                // parent that cannot be removed from the rowgraph, then it can't be removed from the rowgraph either.
                for (Vertex parent : this.graph.get(rowid).getParents()) {
                    RowID parentid = parent.getRow().getRowId();

                    if (!subGraph.graph.containsKey(parent.getRow().getRowId())
                            || (canRemove.get(parentid) != null && !canRemove.get(parentid).booleanValue())) {
                        canRemove.put(rowid, new Boolean(false));
                        break;
                    }

                    // If this vertex has a parent whose canRemove status has not yet been determined, then it's
                    // canRemove status cannot be determined either. Must keep looping over all vertexes until the
                    // canRemove status of all of it's parents have been resolved.
                    if (!canRemove.containsKey(parentid)) {
                        dependenciesResolved = false;
                        canRemove.remove(rowid);
                    }
                }
            }
        }

        // Keep track of whether or not any rows have been removed
        boolean rowsRemoved = false;

        // now iterate over all the rows in the subgraph who are members of
        // canRemove, and remove them from this rowgraph.
        for (Iterator<Row> it = subGraph.iterator(start); it.hasNext(); ) {
            Row row = it.next();
            if (canRemove.get(row.getRowId()).booleanValue())
                rowsRemoved = removeRow(row) || rowsRemoved;
        }

        return rowsRemoved;
    }

    /**
     * Load a graph into memory. Graph generation starts from the row(s) that exist in the vertexes in the specified
     * stack. If there is already a graph in memory, the current contents are preserved and new elements are added to
     * them. See assembleGraph() methods ({@link #assembleGraph(Row) assembleGraph(Row)} or
     * {@link #assembleGraph(Collection) assembleGraph(Collection<Row>)}) to see how to set up to call this method.
     *
     * @param target RowGraph representing rows that are already in memory
     * @param stack  the list of Vertex objects from which RowGraph generation should begin; will be empty at the end of
     *               this method
     */
    private void loadGraphIntoMemory(RowGraph target, VertexStack stack) {
        // stack is used to store unvisited vertex objects temporarily after acquisition with a sql select statement and
        // prior to insertion into the graph. What is actually stored is a Vertex object. Should be empty at the
        // conclusion of graph construction.
        try {
            // while there are any entries left on the stack, keep processing vertexes.
            while (!stack.isEmpty()) {
                // pop a set of vertex objects off the stack and process them. All the popped vertexes will reference
                // the same Table object because of how VertexStack.pop works. If a popped vertex is not already in the
                // target graph, add it. If the vertex is already in the graph, establish parent/child relationships.
                LinkedList<Vertex> poppedVertexes = stack.pop();
                for (ListIterator<Vertex> vtx = poppedVertexes.listIterator(); vtx.hasNext(); ) {
                    Vertex poppedVertex = vtx.next();

                    // check to see if this vertex's row has already been processed. If it has, get a reference to the
                    // vertex that was generated previously.
                    Vertex member = target.graph.get(poppedVertex.getRow().getRowId());

                    if (member == null)
                        // This row has not been visited before. Add it to the target rowgraph.
                        target.addVertexToGraph(poppedVertex);
                    else {
                        // the current vertex has been visited before. Transfer child<->parent relations from the popped
                        // vertex to the previous vertex.
                        poppedVertex.transferRelationships(member);
                        // Remove the vertex from the list of popped vertexes since it is not necessary to go to the
                        // data source and retrieve it's children.
                        vtx.remove();
                    }
                }

                // poppedVertexes does not have any vertexes that children need to be retrieved for
                if (poppedVertexes.size() == 0)
                    continue;

                if (target == this) {
                    // All the new vertexes come from the same table. Loop over all the Relationship objects that
                    // originate from this table. For each one, send all the new vertexes to it where all connected rows
                    // will be retrieved from the data source. The rows will come back already converted into Vertex
                    // objects, complete with child<->parent relationships with the vertex objects that generated them.
                    // Add each of these child Vertex objects to the stack.
                    for (Relationship relationship : poppedVertexes.getFirst().getRow().getTable().getRelationships())
                        for (Vertex child : relationship.execute(poppedVertexes))
                            stack.push(child);
                } else {
                    // If the target is a subgraph of the current graph then new row information is to be obtained from
                    // the current rowgraph (i.e. from 'this'). Get the children of the current vertex from the current
                    // rowgraph and add them all to the stack.
                    for (Vertex vertex : poppedVertexes)
                        for (Vertex child : this.graph.get(vertex.getRow().getRowId()).getChildren())
                            stack.push(new Vertex(child.getRow(), vertex));
                }
            }
        } catch (FatalDBUtilLibException e) {
            String error = "Error in RowGraph.getGraph.\nError message: " + e.getMessage();
            DBDefines.ERROR_LOG.add(error);
        }
    }

    /**
     * Add a temporary Row object to the RowGraph with specified links to its children and parents. It is temporary in
     * the sense that the row will not be permanently added to the RowGraph and the links established until
     * {@link #createLinks() createLinks()} is called since {@link #createLinks() createLinks()} operates on this
     * temporary data. The children and parent links are specified via RowIds representing the row's children and
     * parents that will ultimately be members of the RowGraph.
     * <p>
     * This method is useful for reconstructing a RowGraph object. The sequence of method calls that should be used to
     * reconstruct a RowGraph object that has been disassembled is as follows:
     * <p>
     * <code> // create a new, empty RowGraph object <br>
     * RowGraph rowGraph = new RowGraph(schema); <br>
     * <br> // Add an arbitrary number of Row objects to the rowGraph, along with the lists of children and
     * parents by using <br>// repeated calls to this method. <br>
     * rowGraph.add(row, children, parents);<br>
     * <br>// After all the Rows have been added, call <br>
     * rowGraph.createLinks();
     * </code>
     *
     * @param row      the Row object to add to the RowGraph
     * @param children the List of rowIds that represent the Row objects that are children of row.
     * @param parents  the List of rowIds that represent the Row objects that are parents of row.
     */
    public void add(Row row, Collection<String> children, Collection<String> parents) {
        this.createLinksTempGraph.add(new TempGraph(row, children, parents));
    }

    /**
     * This method converts the Collection<String>s of children and parents from rowIds to actual Row references within
     * the RowGraph for the rows added via {@link #add(Row, Collection, Collection) add(Row, Collection<String>,
     * Collection<String>)}.
     * <p>
     * Here is some sample code to illustrate how to disassemble and reassemble a RowGraph:
     * <p>
     * <code>// get the RowGraph that starts from the list of initial rows. <br>
     * RowGraph rowGraph = new RowGraph(schema, initialRows); <br>
     * <br>// create an ArrayList of all the Row objects in the graph <br>
     * ArrayList<Row> rows = new ArrayList<Row>(rowGraph.getRows()); <br>
     * <br>// create an ArrayList that will hold LinkedLists of children's rowIDs. <br>
     * ArrayList<String> children = new ArrayList<String>(rows.size()); <br>
     * <br>// create an ArrayList that will hold LinkedLists of parents's rowIDs. <br>
     * ArrayList<String> parents = new ArrayList<String>(rows.size()); <br>
     * <br>// iterate over all the rows in the graph. <br>
     * for (int i=0; i&lt;rows.size(); i++) <br>{
     * <br> &nbsp;&nbsp;// add the Set of rowIDs to the ArrayList. <br>
     * &nbsp;&nbsp;children.add(i,rowGraph.getChildrenRowIDs(rows.get(i)));
     * <br> &nbsp;&nbsp;// add the Set of rowIDs to the ArrayList. <br>
     * &nbsp;&nbsp;parents.add(i,rowGraph.getParentsRowIDs(rows.get(i))); <br>} <br>
     * <br>// throw away the original rowGraph and replace it with a new, empty one. <br>
     * rowGraph = new RowGraph(schema); <br>
     * <br>// add all the original rows, children and parents to the new rowGraph. <br>
     * for (int i=0; i&lt;rows.size(); i++) <br>
     * &nbsp;&nbsp;rowGraph.add(rows.get(i), children.get(i), parents.get(i)); <br>
     * <br>// convert all the children and parent rowIDs back into Row references. <br>
     * rowGraph.createLinks();
     * </code>
     *
     * @throws FatalDBUtilLibException throws exception if any rowID's remain unresolved (refer to a row that is not in
     *                                 the graph).
     */
    public void createLinks() throws FatalDBUtilLibException {
        // Add all vertexes in this.createLinksTempGraph to the current rowGraph. Do this before creating any links so
        // that the vertex that a link refers to is present when it's time to create the link
        for (TempGraph tempGraph : this.createLinksTempGraph)
            addVertexToGraph(new Vertex(tempGraph.row));

        for (TempGraph tempGraph : this.createLinksTempGraph) {
            Vertex vertex = this.graph.get(tempGraph.row.getRowId());

            // Iterate over the vertex's children's rowIds.
            for (String hexId : tempGraph.children) {
                RowID childId = new RowID(hexId);

                // Find the vertex that has a row with the child rowid.
                Vertex childVertex = this.graph.get(childId);

                // If one is not found, we have a serious problem. The graph contained a vertex with a rowid reference
                // to a child, but the graph does not contain a vertex with that rowid. Bad.
                if (childVertex == null) {
                    String msg = "FATAL ERROR in RowGraph.createLinks().\nAttempting to reconstruct the children of Row "
                            + vertex.getRow() + ".  Cannot find Row object with rowID = " + hexId + "\n";
                    System.out.println(msg);
                    DBDefines.ERROR_LOG.add(msg);
                    throw new FatalDBUtilLibException(msg);
                }
                vertex.addChild(childVertex);
            }

            // Iterate over the vertex's parents' rowIds.
            for (String hexId : tempGraph.parents) {
                RowID parentId = new RowID(hexId);

                // Find the vertex that has a row with the parent rowid.
                Vertex parentVertex = this.graph.get(parentId);

                // If one is not found, we have a serious problem. The graph contained a vertex with a rowid reference
                // to a parent, but the graph does not contain a vertex with that rowid. Bad.
                if (parentVertex == null) {
                    String msg = "FATAL ERROR in RowGraph.createLinks().\nAttempting to reconstruct the parents of Row "
                            + vertex.getRow() + ".  Cannot find Row object with rowID = " + hexId + "\n";
                    System.out.println(msg);
                    DBDefines.ERROR_LOG.add(msg);
                    throw new FatalDBUtilLibException(msg);
                }
                vertex.addParent(parentVertex);
            }
        }

        // Done with this temporary graph - its contents have been moved to the current RowGraph
        this.createLinksTempGraph.clear();

        // now check each Vertex's rowID to make sure it did not change as a result of being converted to and from jaxb
        // (if that was how this method got called).
        HashMap<RowID, Vertex> needFixin = new HashMap<RowID, Vertex>();
        for (Map.Entry<RowID, Vertex> entry : this.graph.entrySet()) {
            RowID oldRowId = entry.getKey();
            Vertex vertex = entry.getValue();
            RowID newRowId = new RowID(vertex.getRow());
            if (!oldRowId.equals(newRowId))
                needFixin.put(oldRowId, vertex);
        }

        // This remove and re-add works since the actual parents/children are maintained in the Vertex object itself.
        for (RowID rowid : needFixin.keySet()) {
            Vertex vertex = needFixin.get(rowid);
            this.graph.remove(rowid);
            this.graph.put(vertex.getRow().resetRowID(), vertex);
        }
    }

    /**
     * Check the rowGraph for constraint violations. See the {@link RowGraphConstraints RowGraphsConstraints} class for
     * more information regarding constraints and constraint violations.
     *
     * @param allowedErrors A list of allowed error codes or the max allowed error code (the latter is specified by
     *                      '<=39'.
     * @return the maximum error code encountered.
     */
    public boolean checkConstraints(String allowedErrors) {
        boolean ok = true;
        for (Schema schema : this.schemas)
            ok = this.constraints.checkConstraints(schema, allowedErrors) && ok;
        return ok;
    }

    /** ~*~*~*~*~*~*~*~ GET/SET TYPE METHODS ~*~*~*~*~*~*~*~ */

    /**
     * Retrieve the maximum error code generated during construction of the rowGraph. See the error log for detailed
     * information about each violation. See {@link RowGraphConstraints#getLegend() RowGraphConstraints.getLegend()} for
     * information regarding the codes that can be returned.
     *
     * @return The maximum error code encountered during rowGraph construction.
     */
    public int getMaxErrorCode() {
        return this.constraints.violations.getMaxErrorCode();
    }

    /**
     * Retrieve all of the violations generated during construction of the RowGraph.
     *
     * @return all of the violations generated during construction of the RowGraph
     */
    public String getViolationCounts() {
        return this.constraints.violations.getViolationCounts();
    }

    /**
     * Retrieve the number of Schema objects that are represented by the Rows in this RowGraph.
     *
     * @return the number of Schema objects that are represented by the Rows in this RowGraph.
     */
    public int numberOfSchemas() {
        return this.schemas.size();
    }

    /**
     * Retrieve the Collection of Schema objects that are represented by the Rows in this RowGraph.
     *
     * @return Collection of Schema objects that are represented by the Rows in this RowGraph.
     */
    public Collection<Schema> getSchemas() {
        return this.schemas;
    }

    /**
     * Retrieve the set of all the Table objects represented in the row graph
     *
     * @return the set of all the Table objects represented in the row graph
     */
    public Set<Table> getTableTypes() {
        return this.rowMap.keySet();
    }

    /**
     * Return this RowGraph's rowMap (LinkedHashMap from a Table -> ArrayList of Vertex objects for vertexes with Rows
     * belonging to that table).
     *
     * @return this RowGraph's rowMap
     */
    protected LinkedHashMap<Table, ArrayList<Vertex>> getRowMap() {
        return this.rowMap;
    }

    /**
     * Return this RowGraph's graph (mapping from a Row object's RowID to a Vertex object).
     *
     * @return this RowGraph's graph
     */
    protected HashMap<RowID, Vertex> getGraph() {
        return this.graph;
    }

    /** ~*~*~*~*~*~*~*~** TO STRING METHODS ~*~*~*~*~*~*~*~ */

    /**
     * Retrieve a textual inventory of the Tables represented in the RowGraph,
     * including the table name, the table type, and the number of Row obects
     * in the row graph which belong to the Table.
     *
     * @return a textual inventory of the Tables represented in the RowGraph
     */
    public String getTableInventory() {
        Set<Table> tables = rowMap.keySet();
        String[] records = new String[tables.size()];
        int i = 0;
        for (Table table : tables)
            records[i++] = table.name;

        DBDefines.evenLength(records);
        i = 0;
        for (Table table : tables)
            records[i++] += "  " + table.tableType;

        DBDefines.evenLength(records);
        i = 0;
        for (Table table : tables)
            records[i++] += "  " + rowMap.get(table).size();

        StringBuffer buf = new StringBuffer();
        for (i = 0; i < records.length; i++)
            buf.append(records[i] + '\n');
        return buf.toString();
    }

    /**
     * Returns a string representation of this rowgraph. This method calls {@link #toString(Collection)
     * toString(Collection<Row>)} with an initialRows parameter equal to the result of executing
     * {@link #getRowsOfType(String) getRowsOfType(topLevelTable)}
     *
     * @param topLevelTable type of the table from which rowgraph traversal should begin.
     * @return String string representation of this rowgraph starting with rows whose table type equals topLevelTable
     */
    public String toString(String topLevelTable) {
        return toString(getRowsOfType(topLevelTable));
    }

    /**
     * Returns a string representation of this rowgraph. Includes only rows that are reachable from initialRows. The
     * rows are listed in order starting from initialRows and then traversing the reachable rows in breadth-first order.
     *
     * @param initialRows initial set of rows from which rowgraph traversal should begin.
     * @return String string representation of this rowgraph.
     */
    public String toString(Collection<Row> initialRows) {
        StringBuilder returnString = new StringBuilder();
        for (Iterator<Row> row = iterator(initialRows); row.hasNext(); ) {
            Vertex vertex = this.graph.get(row.next().getRowId());
            returnString.append(vertex.toString());
        }
        return returnString.toString();
    }

    /**
     * Returns a string representation of this rowgraph. Includes only rows that are reachable from initialRow. The rows
     * are listed in order starting from the initialRow and then traversing the reachable rows in breadth-first order.
     *
     * @param initialRow initial row from which rowgraph traversal should begin.
     * @return String string representation of this rowgraph.
     */
    public String toString(Row initialRow) {
        StringBuilder returnString = new StringBuilder();
        for (Iterator<Row> row = iterator(initialRow); row.hasNext(); ) {
            Vertex vertex = this.graph.get(row.next().getRowId());
            returnString.append(vertex.toString());
        }
        return returnString.toString();
    }

    /**
     * Return a single String that contains all the fields in all the rows in the row graph. Each record is prepended
     * with the table type of the table of which the row is a member. Records are delimited by the specified delimiter
     * followed by an end of line character.
     *
     * @param widen              whether or not the width of each field should be expanded to a width that equals the external field
     *                           specifier. If true, each field will be no wider or narrower than the width specified with the external format
     *                           specifier in the table definition table. Fields that had to be truncated will have errors in the ERROR LOG.
     * @param delimeter          the character that should be used to delimit the fields in each record (in addition to the end
     *                           of line characters).
     * @param scientificNotation whether or not to use scientific notation
     * @param filter             only Rows from Tables included in this set will be represented in the output string.
     * @return single String that contains all the fields in all the rows in the row graph
     */
    public String valuesToString(boolean widen, char delimeter, boolean scientificNotation, HashSet<Table> filter) {
        StringBuilder valuesString = new StringBuilder();
        // Use this as a default in case filter is null
        HashSet<Table> tablesFilter = new HashSet<Table>();
        if (filter != null)
            tablesFilter = filter;

        for (Iterator<Row> it = iterator(); it.hasNext(); ) {
            Row row = it.next();
            if (tablesFilter.contains(row.getTable())) {
                valuesString.append(row.getTableType() + delimeter);
                valuesString.append(row.valuesToString(widen, delimeter, scientificNotation));
                valuesString.append(DBDefines.EOLN);
            }
        }
        return valuesString.toString();
    }

    /**
     * Return a single String that contains all the fields in all the rows in the row graph. Each record is prepended
     * with the table type of the table of which the row is a member. Records are delimited by the specified delimiter
     * followed by an end of line character.
     *
     * @param widen              whether or not the width of each field should be expanded to a width that equals the external field
     *                           specifier. If true, each field will be no wider or narrower than the width specified with the external format
     *                           specifier in the table definition table. Fields that had to be truncated will have errors in the ERROR LOG.
     * @param delimeter          the character that should be used to delimit the fields in each record (in addition to the end
     *                           of line characters).
     * @param scientificNotation whether or not to use scientific notation
     * @return single String that contains all the fields in all the rows in the row graph
     */
    public String valuesToString(boolean widen, char delimeter, boolean scientificNotation) {
        return valuesToString(widen, delimeter, scientificNotation, new HashSet<Table>());
    }

    /**
     * Return a single String that contains all the fields in all the rows in the row graph. Each record is prepended
     * with the table type of the table of which the row is a member. Records are delimited by the specified delimiter
     * followed by an end of line character.
     *
     * @param widen     whether or not the width of each field should be expanded to a width that equals the external field
     *                  specifier. If true, each field will be no wider or narrower than the width specified with the external format
     *                  specifier in the table definition table. Fields that had to be truncated will have errors in the ERROR LOG.
     * @param delimeter the character that should be used to delimit the fields in each record (in addition to the end
     *                  of line characters).
     * @return single String that contains all the fields in all the rows in the row graph
     */
    public String valuesToString(boolean widen, char delimeter) {
        return valuesToString(widen, delimeter, false, new HashSet<Table>());
    }

    /** ~*~*~*~*~*~*~*~ TO/FROM JAXB METHODS ~*~*~*~*~*~*~*~ */

    /**
     * Create a jaxb version of this RowGraph.
     *
     * @return jaxb version of this RowGraph.
     * @throws JAXBException if an error occurs during the jaxb RowGraph object construction
     */
    public gov.sandia.gnem.dbutillib.jaxb.RowGraph toJaxb() throws JAXBException {
        return toJaxb(true);
    }

    /**
     * Create a jaxb version of this RowGraph.
     *
     * @param includeColumnInfo whether or not column information for the tables in this schema should be written out to
     *                          the xml (true) or not (false)
     * @return a jaxb version of this RowGraph.
     * @throws JAXBException if an error occurs during the jaxb RowGraph object construction
     */
    public gov.sandia.gnem.dbutillib.jaxb.RowGraph toJaxb(boolean includeColumnInfo) throws JAXBException {
        gov.sandia.gnem.dbutillib.jaxb.RowGraph jaxbRowGraph = new ObjectFactory().createRowGraph();
        List<gov.sandia.gnem.dbutillib.jaxb.Schema> jaxbSchemas = jaxbRowGraph.getSchema();

        // Loop over each schema in the RowGraph, and add all of the rows for each of those schemas separately.
        for (Schema schema : this.schemas) {
            gov.sandia.gnem.dbutillib.jaxb.Schema jaxbSchema = schema.toJaxb(includeColumnInfo);
            jaxbSchemas.add(jaxbSchema);

            // Add the rows in jaxb form for each of this schema's tables.
            for (gov.sandia.gnem.dbutillib.jaxb.Table jaxbTable : jaxbSchema.getTable()) {
                List<gov.sandia.gnem.dbutillib.jaxb.Row> jaxbRows = jaxbTable.getRow();
                Set<Row> rows = getRowsOfType(schema, jaxbTable.getType());

                for (Row row : rows) {
                    gov.sandia.gnem.dbutillib.jaxb.Row jaxbRow = row.toJaxb();

                    // Add children
                    Set<String> rowIds = getChildrenRowIDs(row);
                    StringBuilder children = new StringBuilder();
                    for (String rowId : rowIds)
                        children.append(rowId + " ");

                    // Add Parents
                    rowIds = getParentsRowIDs(row);
                    StringBuilder parents = new StringBuilder();
                    for (String rowId : rowIds)
                        parents.append(rowId + " ");

                    if (children.length() > 0)
                        jaxbRow.setChildren(children.toString());
                    if (parents.length() > 0)
                        jaxbRow.setParents(parents.toString());

                    jaxbRows.add(jaxbRow);
                }
            }
        }
        return jaxbRowGraph;
    }

    /**
     * Returns a DBUtilLib RowGraph constructed from a Jaxb RowGraph.
     *
     * @param jaxbRowGraph Jaxb RowGraph used to construct a DBUtilLib RowGraph
     * @return a DBUtilLib RowGraph constructed from jaxbRowGraph
     * @throws FatalDBUtilLibException if an error occurs during the RowGraph object construction
     */
    public static RowGraph fromJaxb(gov.sandia.gnem.dbutillib.jaxb.RowGraph jaxbRowGraph)
            throws FatalDBUtilLibException {
        return fromJaxb(jaxbRowGraph, false);
    }

    /**
     * Returns a DBUtilLib RowGraph constructed from a Jaxb RowGraph.
     *
     * @param jaxbRowGraph Jaxb RowGraph used to construct a DBUtilLib RowGraph
     * @param includeDAO   whether or not to use the dao information from the schemas in the RowGraph (true) or not
     *                     (false)
     * @return a DBUtilLib RowGraph constructed from jaxbRowGraph
     * @throws FatalDBUtilLibException if an error occurs during the RowGraph object construction
     */
    public static RowGraph fromJaxb(gov.sandia.gnem.dbutillib.jaxb.RowGraph jaxbRowGraph, boolean includeDAO)
            throws FatalDBUtilLibException {
        RowGraph rowGraph = new RowGraph();

        if (jaxbRowGraph == null)
            return rowGraph;

        // Handle Schemas.
        for (gov.sandia.gnem.dbutillib.jaxb.Schema jaxbSchema : jaxbRowGraph.getSchema()) {
            Schema schema = Schema.fromJaxb(jaxbSchema, includeDAO);

            // Get the rows out of the tables.
            for (gov.sandia.gnem.dbutillib.jaxb.Table jaxbTable : jaxbSchema.getTable()) {
                Table table = schema.getTable(jaxbTable.getName());

                for (gov.sandia.gnem.dbutillib.jaxb.Row jaxbRow : jaxbTable.getRow()) {
                    Row row = Row.fromJaxb(jaxbRow, table);

                    // Get the row's children and parents.
                    List<String> children = new LinkedList<String>();
                    List<String> parents = new LinkedList<String>();

                    String relative = jaxbRow.getChildren();
                    // null if there were no children
                    if (relative != null) {
                        String[] relativeSplit = relative.split(" ");
                        for (int l = 0; l < relativeSplit.length; l++)
                            children.add(relativeSplit[l]);
                    }

                    relative = jaxbRow.getParents();
                    // null if there were no parents
                    if (relative != null) {
                        String[] relativeSplit = relative.split(" ");

                        for (int l = 0; l < relativeSplit.length; l++)
                            parents.add(relativeSplit[l]);
                    }
                    // Add the row, its children, and its parents to rowGraph.
                    rowGraph.add(row, children, parents);
                }
            }
        }
        try {
            // Once all the rows for all the tables have been added to the rowGraph,
            // create all of the links between the rows.
            rowGraph.createLinks();
        } catch (FatalDBUtilLibException e) {
            DBDefines.ERROR_LOG.add("RowGraph.fromJAXB error when calling " + " createLinks. " + e.getMessage()
                    + "\nReturning null.");
            return null;
        }
        return rowGraph;
    }

    /** ~*~*~*~*~*~*~*~ ITERATORS ~*~*~*~*~*~*~*~ */
    /**
     * Retrieve an Iterator over a subset of the Rows in the RowGraph. The iterator will return initialRow and all other
     * rows in the rowgraph which are reachable from initialRow. Graph traversal will be in breadth-first order.
     *
     * @param initialRow the Row object from which row graph traversal should begin
     * @return an Iterator over the Rows in the RowGraph that are reachable from initialRow
     */
    public Iterator<Row> iterator(Row initialRow) {
        return new RowGraphIterator(initialRow);
    }

    /**
     * Retrieve an Iterator over a subset of the Rows in the RowGraph. The iterator will return all the initialRows and
     * all other rows in the rowgraph which are reachable from initialRows. Graph traversal will be in breadth-first
     * order.
     *
     * @param initialRows the Collection of Row objects from which row graph traversal should begin
     * @return an Iterator over the Rows in the RowGraph that are reachable from initialRows
     */
    public Iterator<Row> iterator(Collection<Row> initialRows) {
        return new RowGraphIterator(initialRows);
    }

    /**
     * An Iterator designed to iterate over the rows in a RowGraph.
     *
     * @author Sandy Ballard
     * @version 1.0
     */
    private class RowGraphIterator implements Iterator<Row> {
        /**
         * The Vertex object that was most recently popped off the stack.
         */
        private Vertex currentVertex = null;

        /**
         * The Vertex that will be returned the next time that next() is called.
         */
        private Vertex nextVertex = null;

        /**
         * This stack is used during graph traversal for temporary storage of Vertexes that are waiting to be processed.
         */
        private LinkedList<Vertex> stack = new LinkedList<Vertex>();

        /**
         * Rows that have already been visited by the iterator. Avoids rows showing up multiple times if they are
         * reachable multiple ways.
         */
        private HashSet<RowID> visitedNodesSet = new HashSet<RowID>();

        /**
         * Constructor that sets up the iterator to iterate over all rows in the RowGraph.
         */
        public RowGraphIterator() {
            this.stack.addAll(getGraph().values());
            this.nextVertex = getNext();
        }

        /**
         * Constructor that sets up the iterator to iterate over initialRow plus all the rows reachable from initialRow.
         *
         * @param initialRow row the iterator will start from
         */
        public RowGraphIterator(Row initialRow) {
            Vertex v = getGraph().get(initialRow.getRowId());
            if (v != null)
                this.stack.add(v);
            this.nextVertex = getNext();
        }

        /**
         * Constructor that sets up the iterator to iterate over initialRows plus all the rows reachable from
         * initialRows.
         *
         * @param initialRows rows the iterator will start from
         */
        public RowGraphIterator(Collection<Row> initialRows) {
            for (Row row : initialRows) {
                Vertex v = getGraph().get(row.getRowId());
                if (v != null)
                    this.stack.add(v);
            }
            this.nextVertex = getNext();
        }

        /**
         * Return whether there are more rows to iterate over
         *
         * @return whether there are more rows to iterate over
         */
        public boolean hasNext() {
            // nextVertex will be null if there are no more vertexes in the stack
            return this.nextVertex != null;
        }

        /**
         * Return the next row in the iteration sequence
         *
         * @return the next row in the iteration sequence
         */
        public Row next() {
            this.currentVertex = this.nextVertex;
            this.nextVertex = getNext();
            return this.currentVertex.getRow();
        }

        /**
         * Retrieve the next vertex in the iteration sequence. If the stack is not empty, this will be the vertex that
         * is popped off the stack. When a vertex is popped, all of the vertexes that it is related to are added to the
         * stack of vertexes to be processed if that vertex hasn't been seen already.
         *
         * @return the next vertex in the iteration sequence
         */
        private Vertex getNext() {
            while (!this.stack.isEmpty()) {
                // Pop the next node to be visited off the stack.
                Vertex vertex = this.stack.removeFirst();

                // if Vertex is not in the set of nodes that have already been processed during this traversal, push
                // it's children onto the stack and return it's Vertex.
                if (!this.visitedNodesSet.contains(vertex.getRow().getRowId())) {
                    this.stack.addAll(vertex.getChildren());
                    this.visitedNodesSet.add(vertex.getRow().getRowId());
                    return vertex;
                }
            }
            return null;
        }

        /**
         * Remove the last vertex returned by the iterator.
         */
        public void remove() {
            if (this.currentVertex == null)
                return;
            removeRow(this.currentVertex.getRow());
            this.currentVertex = null;
        }
    }

    /** ~*~*~*~*~*~*~*~ INNER CLASSES ~*~*~*~*~*~*~*~ */

    /**
     * Container for Vertex objects. They are not truly maintained in a stack. Instead an association between
     * {@link Table Table} objects and the Vertexes that have Row's of that table's type is maintained.
     */
    protected class VertexStack {
        /**
         * Map from a table to the vertexes that are of that table type.
         */
        private LinkedHashMap<Table, LinkedList<Vertex>> map = new LinkedHashMap<Table, LinkedList<Vertex>>();

        /**
         * Return whether or not the stack is empty
         *
         * @return whether or not the stack is empty
         */
        protected boolean isEmpty() {
            return this.map.isEmpty();
        }

        /**
         * Add this vertex to the list of vertexes belonging to the vertex's table type in map.
         *
         * @param vertex vertex to be added to the list of vertexes belonging to the vertex's table type in map.
         */
        protected void push(Vertex vertex) {
            Table vertexTable = vertex.getRow().getTable();

            // See if a vertex list has been started for the table type present in this vertex
            LinkedList<Vertex> vertexes = this.map.get(vertexTable);
            if (vertexes == null) {
                vertexes = new LinkedList<Vertex>();
                this.map.put(vertexTable, vertexes);
            }
            // Add this vertex to the list associated with vertexTable
            vertexes.add(vertex);
        }

        /**
         * Return the "next" Vertexes. This accesses the first table in map and returns all of its vertexes
         *
         * @return the "next" Vertexes
         */
        protected LinkedList<Vertex> pop() {
            if (this.map.isEmpty())
                return new LinkedList<Vertex>();

            Map.Entry<Table, LinkedList<Vertex>> entry = this.map.entrySet().iterator().next();
            Table poppedTable = entry.getKey();
            this.map.remove(poppedTable);
            return entry.getValue();
        }
    }

    /**
     * Class that keeps track of a row and a collection of rowids for the rows parents and children. Just a handy
     * container class that is used to hold Rows added via {@link RowGraph#add(Row, Collection, Collection) add(Row,
     * Collection, Collection)} to be used later by the {@link RowGraph#createLinks createLinks()} method.
     */
    private class TempGraph {
        /**
         * "Base" row.
         */
        protected Row row;

        /**
         * Rowids for row's children.
         */
        protected Collection<String> children;

        /**
         * Rowids for row's parents.
         */
        protected Collection<String> parents;

        /**
         * Constructor.
         *
         * @param row      base row
         * @param children rowids for row's children
         * @param parents  rowids for row's parents
         */
        protected TempGraph(Row row, Collection<String> children, Collection<String> parents) {
            this.row = row;
            this.children = children;
            this.parents = parents;
        }
    }
}
