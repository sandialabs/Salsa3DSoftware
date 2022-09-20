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
package gov.sandia.gnem.dbutillib.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import gov.sandia.gnem.dbutillib.Row;
import gov.sandia.gnem.dbutillib.RowGraph;
import gov.sandia.gnem.dbutillib.Table;

/**
 * The Unload class contains one method - deleteRows.  This method unloads
 * all of the rows in a RowGraph from the database.  Actually, it sets all
 * of the Rows' status bytes to what needs to happen to that row in the database
 * for all of the RowGraph's rows to be deleted from the database.  The calling
 * application must go through and perform the actual deletions.  If deleting a
 * row in the RowGraph will leave orphan a row in the database that is not part
 * of the database, then that row is not deleted.
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public class Unload {
    /**
     * Placeholder constructor - does nothing.
     */
    public Unload() {
    }

    /**
     * Delete the rows in rowGraph from the database.  These changes are not
     * actually committed to the database, but are done in memory.  All rows
     * that need to be deleted for the rowGraph to be unloaded have their
     * status bytes set to DBDefines.DELETE while those rows that need to be
     * updated have their status bytes set to DBDefines.UPDATE.
     *
     * @param rowGraph         RowGraph containing the rows to be deleted from the database
     * @param dontCheckColumns LinkedList<String> of foreign key columns that
     *                         should be ignored if they are part of a foreign key relationship
     * @return whether or not rows were deleted successfully
     */
    public boolean deleteRows(RowGraph rowGraph, LinkedList<String> dontCheckColumns) {
        // Iterate over the rows from idowner tables whose status flags are
        // still UNDETERMINED.  Rows whose status has already been set to
        // something that is not UNDETERMINED have probably been handled by some
        // preprocessing, and should not have their status changed.
        // The reason only idowner tables are iterated over is because the
        // getLinkedTables method from Table is used, and that only applies
        // to idowner tables.

        // This try block is to catch the exception that can potentially be 
        // thrown by the call to dao.executeSelectStatement.  It is outside
        // the loop so that we don't have to create a new try/catch block on
        // each loop iteration.
        try {
            for (Iterator i = rowGraph.iterator(); i.hasNext(); ) {
                Row idownerRow = (Row) i.next();
                if (idownerRow.getTable().getOwnedID() != null
                        && idownerRow.getStatus() == DBDefines.UNDETERMINED) {
                    // connectedRows will be a list of all the rows in the database
                    // that have direct foreign key relationships with the idownerRow.
                    HashSet<Row> connectedRows = new HashSet<Row>();

                    // allInGraph will be true if all the rows in connectedRows are
                    // members of the rowgraph.
                    boolean allInGraph = true;

                    // iterate over all the tables in the schema that have foreign key
                    // relationship with the table that idownerRow comes from.
                    // linkedTable has a foreign key that links to idownerRow's ownedID
                    for (Table linkedTable : idownerRow.getTable().getLinkedTables().keySet()) {
                        // connectedColumns is a list of the columns in linkedTable
                        // that are foreign keys linked to idownerRow's ownedID.
                        LinkedList<String> connectedColumns =
                                idownerRow.getTable().getLinkedTables().get(linkedTable);

                        // Remove columns from the connectedColumns list that do
                        // not need to be checked (in dontCheckColumns).
                        for (String col : dontCheckColumns) {
                            col = col.trim();
                            connectedColumns.remove(col);
                            connectedColumns.remove(col.toUpperCase());
                            connectedColumns.remove(col.toLowerCase());
                        }

                        // So, if the current row's table is linked to another table
                        // through any columns ...
                        if (connectedColumns != null && connectedColumns.size() > 0) {
                            // build a select statement that will get back every row in
                            // linkedTable that is linked to idownerRow (by value).
                            StringBuilder where = new StringBuilder("WHERE ");
                            String delim = "";
                            for (String column : connectedColumns) {
                                where.append(delim);
                                delim = " OR ";
                                where.append(column);
                                where.append("=");
                                where.append(idownerRow.getValueOwnedID().toString());
                            }

                            // now execute the select statement and get back the
                            // connected rows.
                            LinkedList<Row> moreRows =
                                    idownerRow.getTable().getSchema().getDAO().executeSelectStatement
                                            (linkedTable, where.toString());

                            // If there any connected rows ...
                            if (moreRows != null && moreRows.size() > 0) {
                                // If the rows recovered are all members of the
                                // rowGraph, add them to the larger set of all the
                                // connected rows.
                                if (rowGraph.containsAll(moreRows))
                                    connectedRows.addAll(moreRows);
                                else {
                                    // otherwise, idownerRow has a foriegn key
                                    // relationship with a row that is not a member
                                    // of the row graph.  All it takes is for there
                                    // to be one such row and idownerRow cannot be
                                    // deleted - nor can either of the other rows
                                    // since they are connected to idownerRow.
                                    DBDefines.STATUS_LOG.add("Cannot delete " + idownerRow + " since the following " +
                                            "rows refer to it and will not be deleted");
                                    for (Row r : moreRows)
                                        DBDefines.STATUS_LOG.add("\t" + r);
                                    allInGraph = false;
                                    break;
                                }
                            }
                        }
                    }
                    if (allInGraph) {
                        // all the rows in the schema that have foriegn key relationships
                        // with idownerRow are members of the row graph.  Therefore,
                        // idownerRow, and all the rows that are related to it by foreign
                        // key relationships, can be deleted.
                        idownerRow.setStatus(DBDefines.DELETE);
                        for (Row row : connectedRows) {
                            if (row.getTable().getOwnedID() == null)
                                rowGraph.getRow(row.getRowId()).setStatus(DBDefines.DELETE);
                        }
                    }
                }
            }
        } catch (DBDefines.FatalDBUtilLibException e) {
            String error = "Error in Unload.deleteRows.\nError"
                    + " message: " + e.getMessage();
            DBDefines.ERROR_LOG.add(error);
            return false;
        }
        return true;
    }
}
