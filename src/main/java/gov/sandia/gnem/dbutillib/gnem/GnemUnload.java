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
package gov.sandia.gnem.dbutillib.gnem;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import gov.sandia.gnem.dbutillib.RankTable;
import gov.sandia.gnem.dbutillib.Row;
import gov.sandia.gnem.dbutillib.RowGraph;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.Table;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.Unload;

/**
 * The GnemUnload class does some special "GNEM specific" processing of rows
 * in a RowGraph before those rows are deleted/unloaded from the database.
 * This "special processing" applies entirely to rows from event tables.
 * Event tables are an odd case since they serve in a sort of collection
 * capacity.  One event groups together a Collection of origin rows, with
 * one of those origin rows being the preferred origin for the event.
 * <p>
 * There are three different scenarios for how events and some or all of their
 * origins can get added to a RowGraph:
 * 1 - All origins associated with an event are present in the RowGraph.
 * In this case, the event and all of the origins can be deleted.
 * This case is handled by {@link Unload#deleteRows Unload.deleteRows()}
 * 2 - The preferred origin associated with an event is not present in the
 * RowGraph.
 * In this case, the event cannot be deleted.  However, its prefor that
 * points to the preferred origin does not need to be changed since the
 * preferred origin is not in the RowGraph of things to delete.
 * This case is handled by {@link Unload#deleteRows Unload.deleteRows()}
 * 3 - The preferred origin associated with an event is present in the RowGraph.
 * This is the case that requires special handling.  The event cannot be
 * deleted because there are other origins associated with it that are not
 * part of the RowGraph (if those origins were part of the RowGraph, then we
 * would be dealing with the first case mentioned above).  However, since the
 * event's prefor is being deleted, then the event needs a new preferred
 * origin chosen from the origins that are outside of the RowGraph.  The
 * author ranking table indicates how to chose among several of such origins
 * based on author priorities.
 * <p>
 * Since the first two cases are covered by the generic
 * {@link Unload#deleteRows deleteRows} method in Unload, GnemUnload only
 * needs to handle the third case in its own deleteRows method.
 *
 * @author Sandy Ballard
 * @version 1.0
 */

public class GnemUnload extends Unload {
    /**
     * Ranking table to use to determine what priority different authors have.
     */
    RankTable authorRank = null;

    /**
     * Constructor.
     *
     * @param authorRank ranking table to use to determine what priority
     *                   different authors have
     */
    public GnemUnload(RankTable authorRank) {
        this.authorRank = authorRank;
    }

    /**
     * Delete the rows in rowGraph from the database.  These changes are not
     * actually committed to the database, but are done in memory.  All rows
     * that need to be deleted for the rowGraph to be unloaded have their
     * status bytes set to DBDefines.DELETE while those rows that need to be
     * updated have their status bytes set to DBDefines.UPDATE.
     * This function does some special handling of event rows before calling
     * {@link Unload#deleteRows Unload.deleteRows}; see comments at the
     * beginning of this class.
     *
     * @param rowGraph         RowGraph containing the rows to be deleted from the database
     * @param schema           schema the rows in rowGraph belong to
     * @param dontCheckColumns LinkedList<String> of foreign key columns that
     *                         should be ignored if they are part of a foreign key relationship
     * @return true if rows were deleted successfully; false otherwise
     */
    public boolean deleteRows(RowGraph rowGraph, Schema schema,
                              LinkedList<String> dontCheckColumns) {
        // Rows from the event table are a special case because of the
        // prefor.  Get all the event rows that are members of the rowgraph.
        Set<Row> events = rowGraph.getRowsOfType(schema.getTableOfType("EVENT"));
        if (events.size() > 0) {
            Table originTable = schema.getTableOfType("ORIGIN");
            String where = "WHERE EVID=";

            // extract the set of all the orids represented in the rowgraph
            HashSet<Long> orids = new HashSet<Long>();
            for (Row origin : rowGraph.getRowsOfType(originTable))
                orids.add(origin.getValueOwnedID());

            try {
                // Iterate over each of the event rows in the rowgraph
                for (Row event : events) {
                    // Extract the prefor for this event.
                    Long prefor = (Long) event.getValue("PREFOR");

                    // See if the preferred origin of this event is a member of the
                    // rowgraph.  The case where the preferred origin is not part
                    // of the rowgraph will be handled by the generic unload.
                    if (orids.contains(prefor)) {
                        // This event has a prefor that relates to an orid that is
                        // a member of this rowGraph, and thus needs to be deleted.
                        // If there are other origins in the database which belong to
                        // the same event but which are not members of the rowgraph,
                        // then the prefor needs to be updated to point to the one
                        // with the highest author rank.  The case where the prefor
                        // is in the RowGraph but there are not other origins in the
                        // database will be handled by the generic unload.
                        LinkedList<Row> origins = schema.getDAO().executeSelectStatement
                                (originTable, where + event.getValueOwnedID().toString());

                        // Iterate over the list of origins and find the one that is
                        // not a member of the rowgraph and which has the highesAO
                        // author rank.
                        Row newPrefor = null;
                        int bestRank = Integer.MAX_VALUE;

                        // Find the origin with the best priority (lowest rank).
                        for (Row origin : origins) {
                            if (!orids.contains(origin.getValueOwnedID())) {
                                int rank = authorRank.get((String) origin.getValue("AUTH"));
                                if (newPrefor == null || rank < bestRank) {
                                    newPrefor = origin;
                                    bestRank = rank;
                                }
                            }
                        }

                        // newPrefor is a row from the origin table which has the
                        // same evid as the event row that is being considered from
                        // deletion, but it is not a member of the rowgraph.  Of all
                        // such rows, it is the one with the highest author rank.
                        // Update the prefor field of the event row.
                        if (newPrefor != null) {
                            // call this first to preserve undo info.
                            event.setStatus(DBDefines.UPDATE);
                            event.updateField("PREFOR", newPrefor.getValueOwnedID());
                            event.updateField("AUTH", newPrefor.getValue("AUTH"));
                        }
                    }
                }
            } catch (DBDefines.FatalDBUtilLibException e) {
                String msg = "Error in GnemUnload.deleteRows.\nException "
                        + "message: " + e.getMessage();
                DBDefines.ERROR_LOG.add(msg);
                return false;
            }
        }
        super.deleteRows(rowGraph, dontCheckColumns);
        return true;
    }
}
