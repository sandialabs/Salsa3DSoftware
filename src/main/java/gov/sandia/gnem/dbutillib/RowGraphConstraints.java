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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import gov.sandia.gnem.dbutillib.util.DBDefines;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */

/*
 * Constraint violation legend - see getLegend()
 *
 */
public class RowGraphConstraints {
    RowGraph rowGraph = null;

    Violations violations = new Violations();

    public RowGraphConstraints(RowGraph rowGraph) {
        this.rowGraph = rowGraph;
    }

    protected boolean checkConstraints(Schema schema, String allowedErrors) {
        violations.clear();
        violations.setAllowedErrors(allowedErrors);

        // ownedID name -> original value -> new value
        HashMap<String, HashMap<Long, Long>> remap = new HashMap<String, HashMap<Long, Long>>();

        // populate remap with a link from ownedID to a new HashMap(), including
        // every ownedID in the entire schema (represented in rowGraph or not).
        for (Table table : schema.tableNameToTable.values()) {
            if (table.ownedID != null) {
                HashMap<Long, Long> idMap = new HashMap<Long, Long>(); // original value -> new value
                remap.put(table.ownedID, idMap);
            }
        }

        HashSet<Row> rowsToRemove = new HashSet<Row>();
        ArrayList<Row> rows = null;

        // iterate over all the table types that are represented in the rowgraph
        for (Table table : rowGraph.getRowMap().keySet()) {
            // consider only id owner tables
            if (table.ownedID != null) {
                int[] idx = new int[]{table.getColumnIndex(table.ownedID)};

                HashMap<Long, Long> idMap = remap.get(table.ownedID);

                // get the list of rows in the rowgraph that come from this idowner table,
                rows = new ArrayList<Row>(rowGraph.getRowsOfType(table));
                // iterate over all of them
                for (int j = 0; j < rows.size(); j++) {
                    Row row = rows.get(j);

//          if (row.getTableType().equals("REMARK"))
//            System.out.println(row);

                    // owned id values are put into the remap table, so if this one is
                    // already there, it means a row from this table with this owned id value
                    // has already been visited.
                    Long targetValue = idMap.get(row.getValueOwnedID());

                    // visit only rows that have not been visited before.
                    if (targetValue == null) {
                        // add an entry in the remap table that points from this id to itself.
                        idMap.put(row.getValueOwnedID(), row.getValueOwnedID());

                        // now check to see if there are any other rows in the row graph
                        // that have the same ownedID value as this row.  If there are,
                        // visit them all and resolve the conflict.
                        ArrayList<Row> equalRows = findEqualRows(row, idx, j);
                        for (Row otherRow : equalRows) {
                            // otherRow has the same ownedid value as row.

                            if (!row.equals(otherRow, table.primaryKeys)) {
                                // this row is not in error.  otherRow has the same ownedId value as
                                // row but their primary keys are not equal.  This means that the
                                // table that row comes from has a compound primary key and the
                                // owned id is not unique.  The example is the Remark table which
                                // has ownedId commid and primary key [commid, lineno].
                            } else if (row.equals(otherRow)) {
                                // if the two rows are equal on the basis of all columns excepth
                                // the last column (assumed to be load date), drop otherRow
                                violations.addViolation(10,
                                        "Row " + otherRow.toString() +
                                                " is being removed from the graph because there is " + "another row in table " + table.name +
                                                " that has owned ID " + table.ownedID + " = " + row.getValueOwnedID() +
                                                ".  Since the row is strictly equal to the first row, all of its" + " child/parent relationships are being transferred " + " to that row.");

                                rowGraph.getGraph().get(otherRow.getRowId()).
                                        transferRelationships(rowGraph.getGraph().get(row.getRowId()));
                                rowsToRemove.add(otherRow);
                            } else if (row.equals(otherRow, table.uniqueKeys)) {
                                // if the two rows are equal on the basis of unique keys, drop otherRow
                                violations.addViolation(30,
                                        "Row " + otherRow.toString() +
                                                " is being removed from the graph because there is " + "another row in table " + table.name +
                                                " that has owned ID " + table.ownedID + " = " + row.getValueOwnedID() + ".  Since the row is equal to the first row on the basis of its unique keys, all of its" + " child/parent relationships are being transferred " + " to that row.");

                                rowGraph.getGraph().get(otherRow.getRowId()).
                                        transferRelationships(rowGraph.getGraph().get(row.getRowId()));
                                rowsToRemove.add(otherRow);
                            } else {
                                // there is already a row from this table with this ownedID value.
                                violations.addViolation(60,
                                        "Row " + otherRow.toString() +
                                                " is being removed from the graph because there is " + "another row from table " + row.getTableName() +
                                                " that has owned ID " + row.getTable().getOwnedID() + " = " + row.getValueOwnedID() +
                                                ".  Since its unique keys are NOT equal to the other row, its" + " child/parent relationships are NOT being transferred " + " to that row.");
                                rowsToRemove.add(otherRow);
                            }
                        }
                    }
                }
            }
        }
        rowGraph.removeAll(rowsToRemove);
        rowsToRemove.clear();

        // iterate over all the table types that are represented in the rowgraph
        for (Table table : rowGraph.getRowMap().keySet()) {
            // consider only id owner tables
            if (table.ownedID != null) {
                HashMap<Long, Long> idMap = remap.get(table.ownedID);

                // for every row in the rowgraph that comes from this idowner table,
                // check for equal unique key values
                if (table.uniqueKeys.length > 0) {
                    // Find the indexes of all columns that are not the ownedID or
                    // the load date since those are not taken into account when
                    // we compare for equality.
                    ArrayList<Integer> colsList = new ArrayList<Integer>();
                    for (int j = 0; j < table.columns.length; j++) {
                        if (!table.columns[j].ownedID && !table.columns[j].getName().equalsIgnoreCase("lddate"))
                            colsList.add(j);
                    }

                    int[] cols = new int[colsList.size()];
                    for (int j = 0; j < cols.length; j++)
                        cols[j] = colsList.get(j);

                    // get a list of all the rows in this idOwner table.
                    //rows =  (ArrayList) rowGraph.rowMap.get(table);
                    rows = new ArrayList<Row>(rowGraph.getRowsOfType(table));
                    for (int j = 0; j < rows.size(); j++) {
                        Row row = rows.get(j);

                        // get the list of all the other rows in the table that are equal to
                        // this row, based on unique keys.
                        ArrayList<Row> equalRows = findEqualRows(row, table.uniqueKeys, j);
                        for (Row otherRow : equalRows) {
                            // if the two rows are equal based on all columns except ownedID and
                            // load date.
                            if (row.equals(otherRow, cols)) {
                                // if the two rows are equal on the basis of all columns except
                                // ownedID and
                                // the last column (assumed to be load date), drop otherRow
                                violations.addViolation(11,
                                        "Row " + otherRow.toString() +
                                                " is being removed from the graph because there is " + "another row " + row.toString() + " that is strictly equal to this row. " + " All of this row's child/parent relationships are being transferred " + " to the other row.");
                            }
                            // if the two rows are strictly equal,
                            else {
                                // if the two rows are equal on the basis of unique keys but are not
                                // strictly equal, do the same thing, but with higher error code.
                                violations.addViolation(31,
                                        "Row " + otherRow.toString() + " is being removed from the graph because there is another row " + row.toString() + " that is equal to this row on the basis of their unique keys. " + " The two rows are not strictly equal, however. " + " All of this row's child/parent relationships are being transferred " + " to the other row.");
                            }

                            rowGraph.getGraph().get(otherRow.getRowId()).
                                    transferRelationships(rowGraph.getGraph().get(row.getRowId()));

                            idMap.put(otherRow.getValueOwnedID(), row.getValueOwnedID());

                            rowsToRemove.add(otherRow);
                        }
                    }
                }
            }
        }
        rowGraph.removeAll(rowsToRemove);
        rowsToRemove.clear();

        boolean again = false;

        // remap foreignKeys.  Iterate over all the rows in the rowGraph, regardless
        // of table type.
        for (Vertex vertex : rowGraph.getGraph().values()) {
            Row row = vertex.getRow();

            // iterate over all the foreign keys in this row.  Note that this will
            // include ownedIDs in idOwner tables.
            for (int k = 0; k < row.getTable().foreignKeys.length; k++) {
                int i = row.getTable().foreignKeys[k];
                // column[i] in this table is a foreign key.

                // fkValue is the value of foreign key in the current row.
                Long fkValue = (Long) row.getValue(i);

                // if this foreign key is already equal to this column's navalue,
                // there is no problem.
                //if (row.table.columns[i].NAValue != null && fkValue.equals((Long) row.table.columns[i].NAValue))
                if (row.getTable().columns[i].NAValueAllowed() &&
                        ((fkValue == null && row.getTable().columns[i].NAValue == null) || fkValue.equals((Long) row.getTable().columns[i].NAValue)))
                    continue;


                // ownedID is the ownedID this row's foreign key is related to.
                String ownedID = row.getTable().columns[i].foreignKey;

                if (ownedID.startsWith("VALUEOF:")) {
                    ownedID = (String) row.getValue(ownedID.substring(8));
                    if (DBDefines.convertToUpperCase) {
                        ownedID = ownedID.toUpperCase();
                    }
                }

                // idOwner is the Table object that owns the ownedID.  null indicates
                // that the table that owns this id is not represented in the current
                // rowGraph.schema.
                Table idowner = schema.ownedIDToTable.get(ownedID);

                // set idMap to a map from current value -> new value for this value of
                // ownedID, if there is one.
                HashMap<Long, Long> idMap = remap.get(ownedID);

                Long targetValue = null;

                // get the value that this foreign key maps to from the remap table.
                // Most of the time, values will map back to themselves.
                if (idMap != null)
                    targetValue = (Long) idMap.get(fkValue);

                if (targetValue != null)
                    row.updateField(i, targetValue);
                else {
                    // this is a broken foreign key.

                    // if this column is allowed to have an NAValue, set the value to the
                    // navalue.
                    //if (row.table.columns[i].NAValue != null)
                    if (row.getTable().columns[i].NAValueAllowed()) {
                        row.updateField(i, row.getTable().columns[i].NAValue);
                        if (idowner == null)
                            violations.addViolation(20,
                                    "Row " + row.toString() + " has foreign key " +
                                            row.getTable().columns[i].name + " which is related to " + row.getTable().columns[i].foreignKey + " in a table that is not represented in the rowGraph.  " + row.getTable().columns[i].name + " is being set to NAValue.");
                        else
                            violations.addViolation(20,
                                    "Row " + row.toString() + " has foreign key " +
                                            row.getTable().columns[i].name + " but " + idowner.name + " does not contain a row where " +
                                            ownedID + "=" + fkValue + ".  " + row.getTable().columns[i].name +
                                            " is being set to NAValue.");
                    } else if (row.getTable().columns[i].getFixedFK()) {
                        DBDefines.WARNING_LOG.add("Column: " +
                                row.getTable().columns[i].getName() + " in " +
                                row.getTable().getName() + " is a foreign key that " +
                                "refers to a table that is not present " +
                                "in the Schema.  However, this column's fixedFK " +
                                "value is set to true, and it will remain in the " +
                                "rowGraph");
                    } else {

                        // If this column's navalue is "not allowed" (null) then this row is
                        // invalid.
                        if (idowner == null) {
                            violations.addViolation(50,
                                    "Row " + row.toString() + " has foreign key " +
                                            row.getTable().columns[i].name + " which is linked to " + ownedID +
                                            " in a table that is not represented" + " in this schema.  " + row.toString() +
                                            " is being removed from the row graph.");
                        } else {
                            violations.addViolation(50,
                                    "Row " + row.toString() + " has foreign key " +
                                            row.getTable().columns[i].name + " but " + idowner.name + " does not contain a row where " +
                                            ownedID + "=" + fkValue + ".  " + row.toString() +
                                            " is being removed from the row graph.");
                        }

                        // if this row comes from an idOwner table, then we have to remove the
                        // ownedID value from remap.  Then we have to iterate
                        // over all the rows in the row graph again to ensure that no rows
                        // have foreign links to the ownedID value that is being removed.
                        if (row.getTable().ownedID != null) {
                            HashMap<Long, Long> rowsMap = remap.get(row.getTable().ownedID);
                            if (rowsMap != null && rowsMap.remove(row.getValueOwnedID()) != null)
                                again = true;
                        }

                        rowsToRemove.add(row);
                    }
                }
            } // end of iteration over foreign keys in a row.
        } // end of iteration over rows.

        rowGraph.removeAll(rowsToRemove);
        rowsToRemove.clear();

        // check for unique key violations in non-idowner tables.
        // iterate over all the table types that are represented in the rowgraph
        for (Table table : rowGraph.getRowMap().keySet()) {
            // consider only non id owner tables
            if (table.ownedID == null) {
                // for every row in the rowgraph that comes from this table,
                // check for equal unique key values
                if (table.uniqueKeys.length > 0) {
                    rows = new ArrayList<Row>(rowGraph.getRowsOfType(table));
                    for (int j = 0; j < rows.size(); j++) {
                        Row row = rows.get(j);
                        ArrayList<Row> equalRows = findEqualRows(row, table.uniqueKeys, j);
                        for (Row otherRow : equalRows) {
                            // if the two rows are strictly equal,
                            if (row.equals(otherRow)) {
                                // if the two rows are equal on the basis of all columns except
                                // the last column (assumed to be load date), drop otherRow
                                violations.addViolation(10,
                                        "Row " + otherRow.toString() + " is being removed from the graph because there is" + " another row " + row.toString() + " that is strictly equal to this row. " + " All of this row's child/parent relationships are being transferred " + " to the other row.");
                            } else {
                                violations.addViolation(30,
                                        "Row " + otherRow.toString() + " is being removed from the graph because there is another row " + row.toString() + " that has equal unique keys.  " +
                                                "Its child/parent and foreign key relationships are being transferred.");
                            }

                            rowGraph.getGraph().get(otherRow.getRowId()).
                                    transferRelationships(rowGraph.getGraph().get(row.getRowId()));

                            rowsToRemove.add(otherRow);
                        }
                    }
                    rowGraph.removeAll(rowsToRemove);
                    rowsToRemove.clear();
                }
            }
        }
        rowGraph.removeAll(rowsToRemove);
        rowsToRemove.clear();

        DBDefines.STATUS_LOG.add("RowGraphConstraints.checkConstraints() violation counts: " + violations.getViolationCounts());

        return violations.valid;
    }

    /**
     * Search the row graph for Rows that are 'equal' to based on an int[] of
     * column indexes (but not including the row itself).  Row values of types
     * double and float are evaluated including a tolerance based on the
     * external format specifier that is specified in the table definition.
     *
     * @param row the row for which equal rows are being sought.
     * @return a list of rows that are equal to the input row.  This list does
     * not include the original row itself.
     */
    private ArrayList<Row> findEqualRows(Row row, int[] indexes, int rowIndex) {
        ArrayList<Row> equalRows = new ArrayList<Row>();

        ArrayList<Row> rows = new ArrayList<Row>(rowGraph.getRowsOfType(row.getTable()));

        for (int i = rowIndex + 1; i < rows.size(); i++) {
            Row previous = rows.get(i);
            if (!row.getRowId().equals(previous.getRowId()) && row.equals(previous, indexes))
                equalRows.add(previous);
        }

        return equalRows;
    }

    protected class Violations {
        private int maxErrorCode = 0;

        private TreeMap<Integer, Integer> codes = new TreeMap<Integer, Integer>();

        private HashSet<Integer> allowedErrors = new HashSet<Integer>();

        private int maxAllowedError = -1;

        boolean valid = true;

        protected void setAllowedErrors(String errs) {
            allowedErrors.clear();
            maxAllowedError = -1;
            if (errs.startsWith("<="))
                maxAllowedError = Integer.parseInt(errs.substring(2).trim());
            else if (errs.trim().length() > 0) {
                // String[] e = errs.replaceAll(",", " ").replaceAll("  ", " ").split(" ");
                String[] e = DBDefines.removeExtraSpaces(errs.replaceAll(",", " ")).split(" ");
                for (int i = 0; i < e.length; i++)
                    allowedErrors.add(new Integer(e[i]));
            }
        }

        protected void clear() {
            codes.clear();
            maxErrorCode = 0;
        }

        protected Set<Integer> getViolations() {
            return codes.keySet();
        }

        protected int getMaxErrorCode() {
            return maxErrorCode;
        }

        protected int getViolationCount(int i) {
            Integer N = codes.get(new Integer(i));
            if (N == null)
                return 0;
            return N.intValue();
        }

        protected int getViolationCount() {
            int sum = 0;
            for (Integer code : codes.values())
                sum += code;
            return sum;
        }

        protected String getViolationCounts() {
            StringBuilder s = new StringBuilder();
            for (Integer code : codes.keySet()) {
                Integer N = codes.get(code);
                s.append(code + ":" + N.intValue() + "  ");
            }

            // No error codes
            if (s.length() == 0)
                s.append("none");
            return s.toString().trim();
        }

        private int addViolation(int i, String msg) {

            maxErrorCode = Math.max(maxErrorCode, i);
            Integer I = new Integer(i);
            Integer N = codes.get(I);
            if (N == null)
                N = new Integer(1);
            else
                N = new Integer(N.intValue() + 1);
            codes.put(I, N);

            msg = "ERROR Level " + I.toString() + " in RowGraph.checkConstraints().  " + msg;
            if (i <= maxAllowedError || allowedErrors.contains(I))
                DBDefines.WARNING_LOG.add(msg);
            else {
                DBDefines.ERROR_LOG.add(msg);
                valid = false;
            }

            return i;
        }
    }

    public static HashMap<String, String> getLegend() {
        HashMap<String, String> legend = new HashMap<String, String>();
        legend.put("10", "Two rows are equal on the basis of all columns except" +
                " the last column (assumed to be load date).  Remove the duplicate" +
                " row from the RowGraph.  Since the rows are strictly equal, all of" +
                " the duplicate row's child/parent relationships are being" +
                " transferred to the existing row.");

        legend.put("11", "Two rows are equal on the basis of all columns except" +
                " ownedID and the last column (assumed to be load date).  Remove the" +
                " duplicate row from the RowGraph.  Since the rows are strictly equal," +
                " all of the duplicate row's child/parent relationships are being" +
                " transferred to the existing row.");

        legend.put("20", "If a row has a column that refers to a foreign key, the" +
                " foreign key value that it refers to is not in the RowGraph, and" +
                " that column is allowed to be set to an NAValue, set that column" +
                " to its NAValue.");

        legend.put("30", "Two rows are equal on the basis of unique keys.  Remove" +
                " the duplicate row from the RowGraph.  Since the rows are strictly" +
                " equal, all of the duplicate row's child/parent relationships are" +
                " being transferred to the existing row.");

        legend.put("31", "Two rows are equal on the basis of unique keys, but are" +
                " not strictly equal.  Remove the duplicate row from the RowGraph." +
                "  Even though the rows are not strictly equal, all of the duplicate" +
                " row's child/parent relationships are being transferred to the" +
                " existing row.");

        legend.put("50", "If a row has a column that refers to a foreign key, the" +
                " foreign key value that it refers to is not in the RowGraph, and" +
                " that column is NOT allowed to be set to an NAValue, then remove" +
                " that row from the RowGraph.");

        legend.put("60", "Two rows have the same ownedID value.  Remove the " +
                " duplicate row from the RowGraph.  Since the unique keys of the" +
                " two rows are NOT equal, the duplicate row's child/parent " +
                " relationships are NOT being transferred to the existing row.");

        return legend;
    }
}
