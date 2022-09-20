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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.LinkedHashMap;

import gov.sandia.gnem.dbutillib.Row;

/**
 * This class accumulates and then returns statistics about
 * the number of rows processed during a merge operation using the Merge Class.</p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */
public class MergeStatistics {
    public MergeStatistics() {
    }

    /**
     * An inner class of MergeStatistics that keeps track of the statisics for a
     * single Table object.  MergeStatistics will have a HashMap with tablenames
     * as keys and Statistics as values.
     */
    public class Statistics {
        public int read, inserted, updated, dropped, deleted;

        private Statistics() {
            read = 0;
            inserted = 0;
            updated = 0;
            dropped = 0;
            deleted = 0;
        }
    }

    /**
     * A HashMap of (String) tableName -> Statistics object.
     */
    private LinkedHashMap<String, Statistics> tableMap
            = new LinkedHashMap<String, Statistics>();

    /**
     * Clear out the statistics being tracked for this object. (This is something that
     * is probably only necessary when the same Merge object is being used for multiple Merges
     * that each need to track their statistics independently.)
     */
    protected void clear() {
        this.tableMap = new LinkedHashMap<String, Statistics>();
    }

    /**
     * Initialize the statistics objects for a Collection of rows.  This method
     * creates a statistics object for each table in the collection or rows and
     * counts how many rows are 'read' from each table.
     *
     * @param rows the Row objects to be counted.
     */
    public void addRows(Collection<Row> rows) {
        for (Row row : rows) {
            String tablename = row.getTableName();
            Statistics stats = tableMap.get(tablename);
            if (stats == null) {
                stats = new Statistics();
                tableMap.put(tablename, stats);
            }
            ++stats.read;
        }
    }

    /**
     * Add a table with no rows to the statistics being generated so that the table shows up in
     * the total stastics with 0s for all of the stats.  This way, it does not look like a table
     * is being lost.
     *
     * @param tableName name of the table
     */
    public void addTableNoRows(String tableName) {
        Statistics stats = tableMap.get(tableName);
        if (stats == null) {
            stats = new Statistics();
            tableMap.put(tableName, stats);
        }
    }

    /**
     * Analyze the statistics for each table represented in the collection of rows.
     * This method counts the number of rows in each table that have status flags
     * equal to INSERT, UPDATE, DROP, or DELETE.
     *
     * @param rows
     */
    public void analyzeRows(Collection<Row> rows) {
        for (Row row : rows) {
            String tablename = row.getTableName();
            Statistics stats = tableMap.get(tablename);
            if (stats == null) {
                stats = new Statistics();
                tableMap.put(tablename, stats);
            }
            if (row.getStatus() == DBDefines.INSERT) ++stats.inserted;
            if (row.getStatus() == DBDefines.UPDATE) ++stats.updated;
            if (row.getStatus() == DBDefines.DROP) ++stats.dropped;
            if (row.getStatus() == DBDefines.DELETE) ++stats.deleted;

        }
    }

    /**
     * Generate a table of statistics that reflects the activity of this
     * Merge instance since it's creation.
     * <p>READ is the number of rows that each table contained when the addRows()
     * method was called.
     * <p>Deleted is the number of rows from each table that had their status
     * flag equal to DELETE when the analyzeRows() method was called.
     * <p>Inserted is the number of rows from each table that had their status
     * flag equal to INSERT when the analyzeRows() method was called.
     * <p>Updated is the number of rows from each table that had their status
     * flag equal to UPDATE when the analyzeRows() method was called.
     * <p>Dropped is the number of rows from each table that had their status
     * flag equal to DROP when the analyzeRows() method was called.
     * <p>Lost is Read - (Deleted + Inserted + Updated + Dropped).  The total
     * of the Lost column should be zero.
     *
     * @return table of statistics.
     */
    public String getStatistics() {
        return formatStatistics(tableMap);
    }

    /**
     * Format the statistics represents in a HashMap from table name to
     * Statistics object for that table into some readable output.
     * <p>READ is the number of rows that each table contained when the addRows()
     * method was called.
     * <p>Deleted is the number of rows from each table that had their status
     * flag equal to DELETE when the analyzeRows() method was called.
     * <p>Inserted is the number of rows from each table that had their status
     * flag equal to INSERT when the analyzeRows() method was called.
     * <p>Updated is the number of rows from each table that had their status
     * flag equal to UPDATE when the analyzeRows() method was called.
     * <p>Dropped is the number of rows from each table that had their status
     * flag equal to DROP when the analyzeRows() method was called.
     * <p>Lost is Read - (Deleted + Inserted + Updated + Dropped).  The total
     * of the Lost column should be zero.
     *
     * @return table of statistics.
     */
    public String formatStatistics(LinkedHashMap<String, Statistics> stats) {
        // n is the number of tables (stats.keySet() has table names) + 1
        // since we are going to have a "new" table named "Total"
        int n = stats.keySet().size() + 1;

        // These arrays will be populated with statistics information for each
        // table.  The information corresponding to each table will be in the
        // same array slot number across all arrays.  For example, the information
        // for the table in tables[1] will be found in read[1], inserted[1], 
        // updated[1], dropped[1], and deleted[1].
        String[] tables = new String[n];
        int[] read = new int[n];
        int[] inserted = new int[n];
        int[] updated = new int[n];
        int[] dropped = new int[n];
        int[] deleted = new int[n];

        // Populate the above arrays and keep some totals information.
        int j = 0;
        int totalRead = 0, totalinserted = 0, totalupdated = 0,
                totaldropped = 0, totaldeleted = 0;
        for (String tableName : stats.keySet()) {
            Statistics st = stats.get(tableName);
            if (st != null) {
                totalRead += st.read;
                totalinserted += st.inserted;
                totalupdated += st.updated;
                totaldropped += st.dropped;
                totaldeleted += st.deleted;

                tables[j] = tableName;
                read[j] = st.read;
                inserted[j] = st.inserted;
                updated[j] = st.updated;
                dropped[j] = st.dropped;
                deleted[j] = st.deleted;

                ++j;
            }
        }
        // Populate the "Total" table information.
        tables[j] = "Total";
        read[j] = totalRead;
        inserted[j] = totalinserted;
        updated[j] = totalupdated;
        dropped[j] = totaldropped;
        deleted[j] = totaldeleted;

        // a default double formatter.
        NumberFormat nformat = DecimalFormat.getInstance();
        // set up the number formatter to output 3 digits to right of
        // decimal point and to not use groupings (i.e., commas between
        // every third digit).
        nformat.setMinimumFractionDigits(1);
        nformat.setMaximumFractionDigits(1);
        nformat.setGroupingUsed(false);
        String delim = "   ";

        String[] list = new String[tables.length + 1];

        // Generate the String of output with headings across the top.
        list[0] = "Table";
        for (int i = 1; i < list.length; i++)
            list[i] = tables[i - 1];
        DBDefines.evenLength(list);

        list[0] += delim + "Read";
        for (int i = 1; i < list.length; i++)
            list[i] += delim + read[i - 1];
        DBDefines.evenLength(list);

        list[0] += delim + "Deleted";
        for (int i = 1; i < list.length; i++)
            list[i] += delim + deleted[i - 1];
        DBDefines.evenLength(list);

        list[0] += delim + "Inserted";
        for (int i = 1; i < list.length; i++)
            list[i] += delim + inserted[i - 1];
        DBDefines.evenLength(list);

        list[0] += delim + "Updated";
        for (int i = 1; i < list.length; i++)
            list[i] += delim + updated[i - 1];
        DBDefines.evenLength(list);

        list[0] += delim + "Dropped";
        for (int i = 1; i < list.length; i++)
            list[i] += delim + dropped[i - 1];
        DBDefines.evenLength(list);

        list[0] += delim + "Lost";
        for (int i = 1; i < list.length; i++)
            list[i] += delim + (read[i - 1] - inserted[i - 1] - updated[i - 1] - dropped[i - 1] - deleted[i - 1]);

        StringBuilder s = new StringBuilder();
        for (int i = 0; i < list.length; i++)
            s.append(list[i] + DBDefines.EOLN);
        return s.toString();
    }

    /**
     * Returns the HashMap from table name to the table's corresponding
     * Statistics object.
     *
     * @return the HashMap from table name to the table's corresponding
     * Statistics object
     */
    public LinkedHashMap<String, Statistics> getStatisticsMap() {
        return tableMap;
    }
}
