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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import gov.sandia.gnem.dbutillib.dao.Parser;
import gov.sandia.gnem.dbutillib.jaxb.ObjectFactory;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * <p>
 * The Relationship Class encapsulates the information and behavior of a SQL SELECT statement. It's primary fields are a
 * starting Table object, an ending Table object, and the WHERE clause of the select statement. The WHERE clause may
 * contain expressions like
 * <p>
 * WHERE END_FIELD = #START_FIELD#
 * </p>
 * <p>
 * where END_FIELD is the name of a column in the ending Table and START_FIELD is the name of a column in the starting
 * Table.<p/>
 * <p>
 * In its constructor, a PreparedStatement is created that represents the Select statement. Each occurrence of
 * #START_FIELD# is replaced with a '?' in the PreparedStatement. The SELECT Prepared Statement is executed using the
 * execute(Row startingRow) method. Execute() finds the values of all the START_FIELDs in the startingRow and populates
 * the appropriate fields of the PreparedStatement with those values. The PreparedStatement is then executed and the
 * LinkedList of Row objects from the ending Table are returned.
 * </p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public class Relationship {
    protected String id = null;

    protected Table sourceTable = null;

    protected Table targetTable = null;

    protected String relationship = "";

    protected String constraint = "";

    Parser parser;

    private RelationshipCondition[] conditions;

    protected boolean preconditions;

    private String whereTemplate = "";

    protected Object[] sourceVariables;

    protected String preparedStatementKey;

    protected Row lastSourceRow = null;

    /**
     * isLumpable is true for where clauses like 'targetColumn = #sourceColumn#', and is false for all others.
     */
    private boolean isLumpable = false;

    private String targetColumn, sourceColumn;

    byte sourceType = DBDefines.UNKNOWN_TYPE;

    private static String log = "RELATIONSHIP";

    private DTimer timer = new DTimer();

    /**
     * Constructor where the id, sourceTable, targetTable, relationship, and constraint are specified.
     *
     * @param id           an identifier for this relationship. This string is used in some output situations to identify the
     *                     relationship. It does not influence the behavior of the Relationship in any way.
     * @param sourceTable  the Table from which the Relationship originates.
     * @param targetTable  the Table where the Relationship terminates.
     * @param relationship a SQL WHERE clause that defines the relationship between the sourceTable and the targetTable.
     * @param constraint   the number of Row objects the Relationship should return. Valid constriants are:
     *                     <p>
     *                     0/N : any number of Rows (no constraint)
     *                     <p>
     *                     1/N : at least one Row.
     *                     <p>
     *                     1 : exactly one Row.
     *                     <p>
     *                     0/1 : no more than 1 Row.
     * @throws FatalDBUtilLibException if an error occurs
     */
    public Relationship(String id, Table sourceTable, Table targetTable, String relationship, String constraint)
            throws FatalDBUtilLibException {
        constraint = constraint.trim();
        if (!(constraint.equals(DBDefines.CONSTRAINT_1) || constraint.equals(DBDefines.CONSTRAINT_0_1)
                || constraint.equals(DBDefines.CONSTRAINT_N) || constraint.equals(DBDefines.CONSTRAINT_0_N))) {
            String error = "Error in Relationship constructor.  constraint = " + constraint
                    + ".  This is not allowed.  The constraint must equal one " + "of the following: "
                    + DBDefines.CONSTRAINT_1 + ", " + DBDefines.CONSTRAINT_0_1 + ", " + DBDefines.CONSTRAINT_N + ", "
                    + DBDefines.CONSTRAINT_0_N;
            DBDefines.ERROR_LOG.add(error);
            throw new FatalDBUtilLibException(error);
        }

        this.id = id.trim();
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        this.relationship = relationship.trim();
        this.constraint = constraint.trim();
        if (relationship.equalsIgnoreCase("null"))
            return;
        if (DBDefines.convertToUpperCase)
            this.constraint = this.constraint.toUpperCase();

        generatePreparedStatementKey();
        try {
            parser = new Parser(this.relationship, sourceTable, targetTable);
            isLumpable = parser.isLumpable();
        } catch (Exception ex) {
            // Parser was unable to be created - typically this is due to the
            // where clause having more Oracle based SQL in it than the parser
            // can handle. Parsers generally serve to handle "lumpable" type
            // statements to speed up execution (see Parser for more information
            // about what it means to be "lumpable").
            // If this parser is being created for a non DB DAO, then it is
            // the only way to execute sql (DB DAOs typically have Oracle), so
            // generate a warning if the parser could not be created for a
            // non database dao.
            if (!sourceTable.getSchema().getDAOType().equals(DBDefines.DATABASE_DAO)
                    && !this.relationship.toLowerCase().equals("where false"))
                DBDefines.WARNING_LOG.add("WARNING in Relationship constructor.  "
                        + "Parser is invalid.  Relationship = " + this.relationship + '\n');
            parser = null;
            isLumpable = false;
        }

        if (isLumpable) {
            // need to discover the names of the source and target columns
            // that appear in the where clause.
            String[] parts = this.relationship.split("=");
            if (parts.length != 2)
                isLumpable = false;
            else {
                // parts.length should be 2
                if (parts[0].indexOf("#") > -1) {
                    // first part has # characters, it is the source
                    sourceColumn = parts[0].replaceAll("#", "").trim();
                    targetColumn = parts[1].trim();
                } else if (parts[1].indexOf("#") > -1) {
                    // second part has # characters, it is the source
                    sourceColumn = parts[1].replaceAll("#", "").trim();
                    targetColumn = parts[0].trim();
                }

                // strip off word 'where' if it is present.
                if (sourceColumn.toLowerCase().startsWith("where "))
                    sourceColumn = sourceColumn.substring(6).trim();
                if (targetColumn.toLowerCase().startsWith("where "))
                    targetColumn = targetColumn.substring(6).trim();

                // strip off 'order by' if it is present
                int i = sourceColumn.toLowerCase().indexOf(" order by ");
                if (i > -1)
                    sourceColumn = sourceColumn.substring(0, i).trim();
                i = targetColumn.toLowerCase().indexOf(" order by ");
                if (i > -1)
                    targetColumn = targetColumn.substring(0, i).trim();

                if (sourceColumn.length() == 0 || targetColumn.length() == 0
                        || sourceTable.getColumnIndex(sourceColumn) < 0)
                    isLumpable = false;
                else
                    // discover the type of the sourceColumn (Long, String, etc.)
                    sourceType = sourceTable.getColumn(sourceColumn).javaType;

                // if sourceType is neither Long nor String, set to false.
                if (sourceType != DBDefines.LONG && sourceType != DBDefines.STRING)
                    isLumpable = false;
            }
        }
    }

    /**
     * Close the PreparedStatement owned by this Relationship object, if it is currently open. This frees the database
     * cursor owned by the PreparedStatement.
     */
    public void close() {
        for (ArrayList<PreparedStatement> preparedStatements : this.preparedStatementPool.values()) {
            for (PreparedStatement preparedStatement : preparedStatements) {
                try {
                    preparedStatement.close();
                } catch (SQLException ex) {
                    // Ignore closing errors
                }
            }
        }
    }

    /**
     * Given a Row object from the source schema, this method populates the Prepared Select Statement managed by this
     * Relationship, executes the select statement and returns the list of Row objects that results. This method does
     * not check to see if a constraint violation occurred.
     *
     * @param sourceRow The source row from which field values will be extracted to populate the Prepared Select
     *                  Statement.
     * @return the list of Row objects from the target table that result from execution of the prepared select
     * statement.
     * @throws FatalDBUtilLibException if an error occurs
     */
    // This method does not check to see if a constraint violation occurred. When I added the check at the end, all of
    // the tests failed. I believe this is used by other methods for retrieving rows with relationships that were not
    // defined in the user relationships but are instead internally managed relationships where the constraints might
    // not need to be adhered to. JEL Jan 2009
    public LinkedList<Row> execute(Row sourceRow) throws FatalDBUtilLibException {
        LinkedList<Row> rows;
        if (targetTable == null || relationship.equals("WHERE FALSE"))
            rows = new LinkedList<Row>();
        else {
            if (sourceTable.schema.SQLTimer || targetTable.schema.SQLTimer)
                timer.getDt();
            setSourceRow(sourceRow);
            try {
                rows = targetTable.schema.dao.executeSelectStatement(this);
            } catch (FatalDBUtilLibException e) {
                String error = "Error in Relationship.execute for row: " + sourceRow + ".\nError message: "
                        + e.getMessage();
                throw new FatalDBUtilLibException(error);
            }
            if (sourceTable.schema.SQLTimer || targetTable.schema.SQLTimer)
                DBDefines.STATUS_LOG.add("Timer " + id + ": " + timer.getDt() + " ms");
        }
        return rows;
    }

    /**
     * Given a set of verteces that are all from the sourceTable, execute the sql this relationship object represents
     * for all of them, make vertex objects out of the rows that are returned, establish child<->parent relationships,
     * and return all the child verteces. If this relationship is a simple select statement (ie. is lumpable, ie. is of
     * the form xxx = #yyy#, where xxx is the name of a column in the target table and yyy is the name of a column in
     * the source table), then conbine all the selects into a single select statement of the form 'xxx in
     * (yyy1,yyy2,yyy3,...)' and execute that.
     *
     * @param verteces Collection
     * @return Collection
     * @throws FatalDBUtilLibException if an error occurs
     */
    protected Collection<Vertex> execute(Collection<Vertex> verteces) throws FatalDBUtilLibException {
        // this is the list of children that will be returned.
        LinkedList<Vertex> children = new LinkedList<Vertex>();

        // if passed an empty collection of verteces, pass empty list of children back.
        if (verteces.size() == 0)
            return children;

        try {
            // if this relationship is lumpable and the javatype of the source column
            // is either Long or String then don't execute a separate sql statement
            // for each vertex. Combine them into groups of 1000 (the max number
            // allowed by Oracle).
            if (isLumpable && verteces.size() > 1) {
                // the where clause of the form 'where targetColumn in (..)'.
                StringBuilder where = new StringBuilder();

                if (sourceType == DBDefines.LONG) {
                    // make a map with keys equal to the unique values of sourceColumn in
                    // the input verteces. For each key the corresponding value is the
                    // list of verteces that have that value in their sourceColumn
                    Map<Long, LinkedList<Vertex>> parentsOfValue = new TreeMap<Long, LinkedList<Vertex>>();

                    // iterate over all the verteces
                    for (Iterator<Vertex> i = verteces.iterator(); i.hasNext(); ) {
                        Vertex vertex = i.next();
                        // extract the value in the sourceColumn from the vertex.
                        Long sourceValue = (Long) vertex.getRow().getValue(sourceColumn);

                        // see if the value is already represented in the map of parents.
                        LinkedList<Vertex> parents = parentsOfValue.get(sourceValue);

                        // if not, create a new list of parents and put it in the map
                        if (parents == null) {
                            parents = new LinkedList<Vertex>();
                            parentsOfValue.put(sourceValue, parents);
                        }
                        // add the current vertex to the list of parents of this source value.
                        parents.add(vertex);

                        // if we have accumulated 1000 unique sourcevalues (the max number
                        // allowed by oracle), or if this is the last vertex, then build
                        // the select statement and execute it.
                        if (parentsOfValue.size() == 1000 || !i.hasNext()) {
                            // make a string out of all the source values.
                            for (Long value : parentsOfValue.keySet())
                                where.append(',').append(value.toString());

                            // turn the string into a where clause.
                            where = where.deleteCharAt(0).insert(0, ("WHERE " + targetColumn + " IN (")).append(')');

                            // execute the select statement and process each of the Rows that
                            // is returned.
                            for (Row row : targetTable.schema.dao.executeSelectStatement(targetTable, where.toString())) {
                                // make a vertex out of it.
                                Vertex child = new Vertex(row);
                                // for each of the input verteces that had this value in the
                                // sourceColumn, create a child<->parent relationship linking
                                // the input vertex (parent) to the new vertex (child).
                                for (Vertex parent : parentsOfValue.get((Long) row.getValue(targetColumn))) {
                                    parent.addChild(child);
                                    child.addParent(parent);
                                }
                                // add this new vertex to the list of verteces to be returned
                                // by this method.
                                children.add(child);
                            }
                            // clear the working containers for the next batch, if there is one.
                            parentsOfValue.clear();
                            where = new StringBuilder();
                        }
                    }
                } else if (sourceType == DBDefines.STRING) {
                    // do the same thing that was done for longs, only for strings.
                    // Only difference is types of the containers and the delimiter
                    // in the where clause.
                    Map<String, LinkedList<Vertex>> parentsOfValue = new TreeMap<String, LinkedList<Vertex>>();

                    for (Iterator<Vertex> i = verteces.iterator(); i.hasNext(); ) {
                        Vertex vertex = i.next();
                        String sourceValue = (String) vertex.getRow().getValue(sourceColumn);
                        LinkedList<Vertex> parents = parentsOfValue.get(sourceValue);
                        if (parents == null) {
                            parents = new LinkedList<Vertex>();
                            parentsOfValue.put(sourceValue, parents);
                        }
                        parents.add(vertex);

                        if (parentsOfValue.size() == 1000 || !i.hasNext()) {
                            for (String value : parentsOfValue.keySet())
                                where.append("','").append(value.toString());

                            where = where.deleteCharAt(0).deleteCharAt(0)
                                    .insert(0, ("WHERE " + targetColumn + " IN (")).append("')");

                            for (Row row : targetTable.schema.dao.executeSelectStatement(targetTable, where.toString())) {
                                Vertex child = new Vertex(row);
                                for (Vertex parent : parentsOfValue.get((String) row.getValue(targetColumn))) {
                                    parent.addChild(child);
                                    child.addParent(parent);
                                }
                                children.add(child);
                            }
                            parentsOfValue.clear();
                            where = new StringBuilder();
                        }
                    }
                }
            } else
                // if this relationship is not lumpable (i.e., has a complicated where
                // clause), then execute a distinct sql statement for each input row.
                for (Vertex vertex : verteces)
                    for (Row row : execute(vertex.getRow()))
                        children.add(new Vertex(row, vertex));
        } catch (Exception ex) {
            String error = "ERROR in Relationship.execute(Collection<Vertex> verteces).  " + "Relationship = "
                    + relationship + "\nError message: " + ex.getMessage();
            throw new FatalDBUtilLibException(error);
        }
        return children;
    }

    protected boolean evaluate(Row sourceRow, Row targetRow) {
        if (parser != null)
            try {
                return parser.evaluate(sourceRow, targetRow);
            } catch (FatalDBUtilLibException ex) {
                System.out
                        .println("ERROR in DBUtilLib.Relationship.evaluate(sourceRow, targetRow)\n" + ex.getMessage());
            }
        return false;
    }

    public boolean evaluate(Row targetRow) {
        return evaluate(lastSourceRow, targetRow);
    }

    /**
     * Whenever a where clause contains a sub string like '#tagname#'='evid', there is a bunch of processing to do. This
     * class facilitates that. buildWhereTemplate (which is called by the Relationship constructor), creates one or more
     * RelationshipCondition objects each time it finds a substring like '#tagname#'='evid'. It records the starting and
     * ending position of the substring within the whereTemplate. It also records the target Column name (the part
     * between # signs), and the testValue (the part on the other side of the = sign). When passed in a row, it knows
     * how to evaluate whether or not the condition is true, and can pass back a string where the target Column name is
     * replaced with the value of that item extracted from the Row object (eg. 'evid'='evid').
     */
    protected class RelationshipCondition {
        // targetColName is the name of the column in the targetTable whose
        // value is to be compared to testValue.
        private String targetColName, testValue;

        private int first, last;

        private RelationshipCondition(int first, int last, String targetColName, String testValue) {
            this.first = first;
            this.last = last;
            this.targetColName = targetColName;
            this.testValue = testValue; // .toUpperCase();
        }

        // find value of targetColName in row. return true if it was found and
        // was == to testValue.
        private boolean test(Row row) {
            String value = (String) row.getValue(targetColName);
            return (value != null && value.equalsIgnoreCase(testValue));
        }

        // test row as above and return a string like 'evid'='evid'.
        private String testString(Row row) {
            // return ("'"+((String)row.getValue(targetColName)).toUpperCase()+"'='"+testValue+"'");
            return ("'" + (String) row.getValue(targetColName) + "'='" + testValue + "'");
        }
    }

    /**
     * Check to see if a particular rowCount violates this Relationship's contraint. If it does, a message is passed to
     * DBDefines.ERROR_LOG with details.
     *
     * @param rowCount number of rows returned by selectStatement.
     * @return true if rowCount violated the constraint. false if rowCount did not violate the constraint.
     */
    public boolean constraintViolated(int rowCount) {
        if (targetTable == null)
            return false;
        boolean violated = false;
        if ((this.constraint.equals(DBDefines.CONSTRAINT_1) && rowCount != 1)
                || (this.constraint.equals(DBDefines.CONSTRAINT_0_1) && rowCount > 1)
                || (this.constraint.equals(DBDefines.CONSTRAINT_N) && rowCount == 0)) {
            DBDefines.ERROR_LOG.add(log + " CONSTRAINT VIOLATION in Relationship.constraintViolated(): rowCount="
                    + rowCount + " violates constraint " + this.constraint + ".  \nSourceTable = "
                    + sourceTable.getName() + " select statement = " + getSelectStatement() + "\n");
            violated = true;
        }
        return violated;
    }

    /**
     * Return this Relationship's ID string.
     *
     * @return this Relationship's ID string.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Return this Relationship's source Table object.
     *
     * @return this Relationship's source Table object.
     */
    public Table getSourceTable() {
        return this.sourceTable;
    }

    /**
     * Return this Relationship's target Table object.
     *
     * @return this Relationship's target Table object.
     */
    public Table getTargetTable() {
        return this.targetTable;
    }

    /**
     * Return this Relationship's select where clause.
     *
     * @return this Relationship's select where clause.
     */
    public String getRelationship() {
        return this.relationship;
    }

    /**
     * Return this Relationship's constraint specification.
     *
     * @return this Relationship's constraint specification.
     */
    public String getConstraint() {
        return this.constraint;
    }

    // Create a select statement template from the information in the
    // relationship implied by this relationship entry. Basically, it will be of the
    // form SELECT * FROM targetTable WHERE targetKey=#sourceKey#. There
    // are complicating circumstances, however, that arise from compound
    // keys, special cases, etc.
    private void generatePreparedStatementKey() {
        this.whereTemplate = "";
        if (this.targetTable == null)
            return;
        String where = relationship.trim();

        if (where.length() > 0 && where.toLowerCase().indexOf("where") != 0)
            where = "WHERE " + where;

        // clear spaces that surround '=' signs, eg., change tagname = 'evid' to
        // tagname='evid'.
        where.replaceAll("' =", "'=");
        where.replaceAll("= '", "='");

        // scan the where clause looking for substrings like '#tagname#'='evid'.
        // Process them in reverse order (i.e., start from end of string).
        // Processing involves creation of an RelationshipCondition object that can be
        // evaluated in the getSelectStatement method.
        ArrayList<RelationshipCondition> c = new ArrayList<RelationshipCondition>();
        int first, last, pos = where.lastIndexOf("'='");
        while (pos >= 0) {
            // find the start and end of the substring '#tagname#'='evid'
            first = where.lastIndexOf("'", pos - 2);
            last = where.indexOf("'", pos + 3) + 1;
            // extract the substring.
            // String junk = where.substring(first, last);
            // split it into 3 parts: #tagname#, =, and evid.
            String[] sub = where.substring(first + 1, last).split("'");
            sub[0] = sub[0].trim();
            sub[2] = sub[2].trim();
            if (sub[0].charAt(0) == '#')
                c.add(new RelationshipCondition(first, last, sub[0].substring(1, sub[0].length() - 1), sub[2]));
            else if (sub[2].charAt(0) == '#')
                c.add(new RelationshipCondition(first, last, sub[2].substring(1, sub[2].length() - 1), sub[0]));
            pos = where.lastIndexOf("'='", pos - 1);
        }
        // convert the conditions from an arraylist to an array.
        conditions = new RelationshipCondition[c.size()];
        for (int i = 0; i < conditions.length; i++)
            conditions[i] = c.get(i);

        if (where.length() == 0)
            where = "WHERE 1=1";

        whereTemplate = where;

        // now create the PreparedStatement that will implement the relationship.
        // The PreparedStatement is created using the where clause but is not
        // populated with any real variables.

        for (int i = 0; i < conditions.length; i++) {
            String part1, part3 = "";
            part1 = where.substring(0, conditions[i].first);
            // chop off "AND"
            if (part1.trim().endsWith(" AND"))
                part1 = part1.substring(0, part1.lastIndexOf(" AND"));
            if (conditions[i].last < where.length())
                part3 = where.substring(conditions[i].last + 1);
            where = part1 + part3;
        }

        String[] parts = where.split("#");
        StringBuilder statement = new StringBuilder("SELECT ");
        // Handle hints (if there are any).
        String hint = targetTable.schema.tableTypeToHint.get(targetTable.getTableType());
        if (hint != null) {
            // Hints are required to have table types in them surrounded by
            // ##. This split extracts the table name out of the hint
            String[] sub = hint.split("#");
            // Surround the hint with /*+ */ which is notation that Oracle needs.
            if (sub.length == 3 && sub[1].equalsIgnoreCase(targetTable.getTableType()))
                statement.append("/*+ ").append(sub[0]).append(targetTable.name).append(sub[2]).append(" */ ");
        }
        statement.append("* FROM ");
        statement.append(targetTable.getName());
        statement.append(' ');
        for (int i = 0; i < parts.length; i++)
            if (i % 2 == 1)
                statement.append('?');
            else
                statement.append(parts[i]);
        preparedStatementKey = statement.toString();

        // System.out.println("Relationship.generatePreparedStatement: "+ preparedStatementKey);
    }

    /**
     * Maintain some PreparedStatements usable by this object for use in multi-threaded environments.
     */
    private HashMap<Connection, ArrayList<PreparedStatement>> preparedStatementPool = new HashMap<Connection, ArrayList<PreparedStatement>>();

    /**
     * Retrieve a PreparedStatement for this object.
     *
     * @param connection Connection object used to create PreparedStatement objects
     * @return a PreparedStatement for this object
     */
    public PreparedStatement getPreparedStatement(Connection connection) throws SQLException {
        synchronized (this.preparedStatementPool) {
            if (this.preparedStatementPool.get(connection) == null)
                this.preparedStatementPool.put(connection, new ArrayList<PreparedStatement>());
            if (this.preparedStatementPool.get(connection).size() == 0)
                this.preparedStatementPool.get(connection).add(connection.prepareStatement(this.preparedStatementKey));
            return this.preparedStatementPool.get(connection).remove(0);
        }
    }

    /**
     * Release a PreparedStatement for future use by this object.
     *
     * @param connection        Connection preparedStatement is associated with
     * @param preparedStatement PreparedStatement to release for future use by this object.
     */
    public void releasePreparedStatement(Connection connection, PreparedStatement preparedStatement) {
        if (preparedStatement != null)
            synchronized (this.preparedStatementPool) {
                this.preparedStatementPool.get(connection).add(preparedStatement);
            }
    }

    /**
     * Return the key used to create PreparedStatments for this Relationship object.
     *
     * @return the key used to create PreparedStatments for this Relationship object.
     */
    public String getPreparedStatementKey() {
        return this.preparedStatementKey;
    }

    /**
     * Populate a where clause template with actual values from a Row object. A where clause template is stored with
     * each Relationship object (there is one associated with each row of the Relationship table). An example is "WHERE
     * orid=#orid#. This method will replace #orid# with the value of orid in the row that is passed in.
     *
     * @param sourceRow The Row object from which values are to be extracted and placed in the template
     * @return true if no errors were encountered, false otherwise
     * @throws FatalDBUtilLibException if the target table is null or if the source table's name does not equal the
     *                                 source row's table name
     */
    public boolean setSourceRow(Row sourceRow) throws FatalDBUtilLibException {
        if (this.targetTable == null)
            throw new FatalDBUtilLibException("Error in Relationship.setSourceRow(" + sourceRow
                    + ") for the relationship named " + this.id + ". targetTable == null.");

        // check that sourceRow is a row from the table for which this relationship
        // table entry is the sourceRow. If not, return false. Should also
        // add error to messageQ.
        if (!sourceRow.getTable().getName().equalsIgnoreCase(this.sourceTable.name))
            throw new FatalDBUtilLibException("Error in Relationship.setSourceRow(" + sourceRow
                    + ") for the relationship named " + this.id + ". The sourceRow's table name is "
                    + sourceRow.getTableName() + " but the name of the source table for this relationship is "
                    + this.sourceTable.getName() + ". These must be the same.");

        // String colName;
        StringBuilder selectStmt = new StringBuilder(this.whereTemplate);
        Object value;

        // test all the conditions associated with this where clause. This will
        // replace substrings like '#tagname#'='evid' with 'evid'='evid' or
        // 'orid'='evid'.

        preconditions = true;
        for (int i = 0; i < conditions.length; i++) {
            String part1, part2, part3 = "";
            part1 = selectStmt.substring(0, conditions[i].first);
            part2 = conditions[i].testString(sourceRow);
            if (conditions[i].last < selectStmt.length())
                part3 = selectStmt.substring(conditions[i].last + 1);
            selectStmt.setLength(0);
            selectStmt.append(part1);
            selectStmt.append(part2);
            selectStmt.append(part3);
            if (!conditions[i].test(sourceRow))
                preconditions = false;
        }

        String[] parts = selectStmt.toString().split("#");
        sourceVariables = new Object[parts.length / 2];
        String colName;
        for (int i = 1; i < parts.length; i += 2) {
            if (parts[i].equalsIgnoreCase("(time)"))
                colName = "time";
            else
                colName = parts[i];
            value = sourceRow.getValue(colName);
            if (value == null) {
                DBDefines.ERROR_LOG.add("ERROR in Relationship.setSourceRow(" + sourceRow + ").  " + "getValue("
                        + colName + ") returned null.  sourceTable=" + sourceTable.getName() + " template="
                        + whereTemplate);
                return false;
            }
            sourceVariables[i / 2] = value;
        }
        lastSourceRow = sourceRow;
        return true;
    }

    /**
     * Return a String representation of this Relationship's PreparedStatement, populated with the values from the
     * sourceRow that was specified in the last call to execute().
     *
     * @return the SELECT statement last executed by a call to execute().
     */
    public String getSelectStatement() {
        if (targetTable == null || lastSourceRow == null)
            return null;
        // check that sourceRow is a row from the table for which this relationship
        // table entry is the sourceRow. If not, return false. Should also
        // add error to messageQ.
        if (!lastSourceRow.getTable().getName().equalsIgnoreCase(sourceTable.name))
            return "";

        String selectStmt = whereTemplate;
        Object value;

        // test all the conditions associated with this where clause. This will
        // replace substrings like '#tagname#'='evid' with 'evid'='evid' or
        // 'orid'='evid'.
        preconditions = true;
        for (int i = 0; i < conditions.length; i++) {
            String part1, part2, part3 = "";
            part1 = selectStmt.substring(0, conditions[i].first);
            part2 = conditions[i].testString(lastSourceRow);
            if (conditions[i].last < selectStmt.length())
                part3 = selectStmt.substring(conditions[i].last + 1);
            selectStmt = part1 + part2 + part3;
            if (!conditions[i].test(lastSourceRow))
                preconditions = false;
        }

        String[] parts = selectStmt.split("#");
        String colName;
        sourceVariables = new Object[parts.length / 2];
        StringBuilder stmt = new StringBuilder("SELECT * FROM ");
        stmt.append(targetTable.name);
        stmt.append(" ");
        for (int i = 0; i < parts.length; i++) {
            if (i % 2 == 1) {
                if (parts[i].equalsIgnoreCase("(time)"))
                    colName = "time";
                else
                    colName = parts[i];
                value = lastSourceRow.getValue(colName);
                if (value == null) {
                    DBDefines.ERROR_LOG.add("ERROR in Relationship.getSelectStatement().  " + "lastSourceRow = "
                            + lastSourceRow + "  " + "getValue(" + colName + ") returned null.  sourceTable="
                            + sourceTable.getName() + " template=" + whereTemplate);
                    return null;
                }
                sourceVariables[i / 2] = value;
                if (lastSourceRow.getTable().getColumn(colName).javaType == DBDefines.STRING) {
                    stmt.append("'");
                    stmt.append(value.toString());
                    stmt.append("'");
                } else
                    stmt.append(" " + value.toString());
            } else
                stmt.append(parts[i]);
        }

        return stmt.toString();
    }

    /**
     * This method scans the whereTemplate and looks at the id relationships. For each link from an ownedID in an
     * IDowner table to a column in a non-IDowner table, set the idLink field in the column of the non-IDowner table to
     * the index of the ID in the IDowner table. For example, set the idLink field in the ARID column of the ASSOC table
     * to the index of ARID in the ARRIVAL table.
     *
     * @throws FatalDBUtilLibException if an error occurs
     */
    protected void setIdLinks() throws FatalDBUtilLibException {
        if (whereTemplate.length() < 6 || whereTemplate.indexOf('#') < 0)
            return;
        String sourceCol = "", targetCol = "";
        int s, t;
        // Get rid of ORDER BY if it exists
        int pos = whereTemplate.toUpperCase().indexOf(" ORDER BY ");
        if (pos < 0)
            pos = whereTemplate.length();
        // split the where template on the " AND ".
        String[] andParts = whereTemplate.substring(6, pos).split(" AND ");

        for (int i = 0; i < andParts.length; i++) {
            // Split it on '='
            String[] eqParts = andParts[i].split("=");
            if (eqParts.length == 2) {
                // remove all the '#' symbols from the source side.
                if (eqParts[0].indexOf('#') >= 0) {
                    s = 0;
                    t = 1;
                } else {
                    s = 1;
                    t = 0;
                }
                sourceCol = eqParts[s].trim().replaceAll("#", "").trim();
                targetCol = eqParts[t].trim();

                if ((sourceTable.ownedID == null || !sourceTable.ownedID.equals(sourceCol))
                        && targetTable.ownedID != null && targetCol.equals(targetTable.ownedID)) {
                    if (sourceTable.getColumn(sourceCol) == null) {
                        throw new FatalDBUtilLibException("ERROR in Relationship.setIdLinks(). " + "Source table "
                                + sourceTable.name + " does not contain column " + sourceCol);
                    }

                    sourceTable.getColumn(sourceCol).idLink = targetCol;
                }

                // if targetCol is not the ownedID column in the target table, and
                // sourceCol is the ownedID column in the source table, then set
                // targetTable's idLink field to point to sourceTable's column
                if ((targetTable.ownedID == null || !targetTable.ownedID.equals(targetCol))
                        && sourceTable.ownedID != null && sourceCol.equals(sourceTable.ownedID)) {
                    if (targetTable.getColumn(targetCol) == null) {
                        throw new FatalDBUtilLibException("ERROR in Relationship.setIdLinks(). " + "target table "
                                + targetTable.name + " does not contain column " + targetCol);
                    }
                    targetTable.getColumn(targetCol).idLink = sourceCol;
                }
            }
        }
    }

    private class DTimer {
        Date time0 = new Date();

        protected DTimer() {
        }

        private long getDt() {
            Date current = new Date();
            long dt = current.getTime() - time0.getTime();
            time0 = current;
            return dt;
        }
    }

    /**********************************************************************
     * Format conversion functions
     **********************************************************************/
    /**
     * Return a String representation of this Relationship object.
     *
     * @return a String representation of this Relationship object.
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(id).append(" ").append(sourceTable.name).append(" -> ");
        if (targetTable == null)
            str.append("NULL");
        else
            str.append("SELECT * FROM ").append(targetTable.name).append(" ").append(whereTemplate);
        return str.toString();
    }

    public gov.sandia.gnem.dbutillib.jaxb.Relationship toJaxb() throws JAXBException {
        gov.sandia.gnem.dbutillib.jaxb.Relationship jaxbRelationship = new ObjectFactory().createRelationship();

        jaxbRelationship.setConstraint(this.constraint);
        jaxbRelationship.setId(this.id);
        jaxbRelationship.setSourceTable(this.sourceTable.getTableType());
        jaxbRelationship.setTargetTable(this.targetTable.getTableType());
        jaxbRelationship.setWhereClause(this.relationship);

        return jaxbRelationship;
    }

    public static Relationship fromJaxb(gov.sandia.gnem.dbutillib.jaxb.Relationship jaxbRelationship, Schema schema)
            throws FatalDBUtilLibException {
        Table sourceTable = schema.getTableOfType(jaxbRelationship.getSourceTable());
        Table targetTable = schema.getTableOfType(jaxbRelationship.getTargetTable());
        if ((sourceTable == null) || (targetTable == null)) {
            DBDefines.ERROR_LOG.add(log + " fromJAXB jaxb relationship has a "
                    + " source table and/or a target table that is not in the "
                    + " associated schema. JAXB sourceTable: " + jaxbRelationship.getSourceTable() + " targetTable: "
                    + jaxbRelationship.getTargetTable() + ". Returning null.");
            return null;
        }
        Relationship relationship = new Relationship(jaxbRelationship.getId(), sourceTable, targetTable,
                jaxbRelationship.getWhereClause(), jaxbRelationship.getConstraint());

        return relationship;
    }

    public Object[] getSourceVariables() {
        return this.sourceVariables;
    }
}
