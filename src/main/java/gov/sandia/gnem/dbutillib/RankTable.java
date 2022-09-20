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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * This class handles interactions with ranking tables in the database. Ranking tables simply contain items that need to
 * be ranked in some way or another and their numerical ranking. For example, say an application needed to know which
 * row to insert into a given table when multiple rows must be chosen from to determine which one to insert. One way to
 * decide that would be to examine the author fields of each row (provided each row has an author field) and pick the
 * row whose author field has a higher priority. To determine which author field has the higher priority, the ranking
 * table would be queried for both authors, and both authors' priorities would be returned. Thus, ranking tables can
 * have very few columns - as few as two: whatever needs to be ranked and its rank. The values in the rank column are
 * expected to be integers.
 */
public class RankTable {
    /**
     * This HashMap keeps track of the item to be ranked and its rank. The keys into the HashMap are the items to be
     * ranked and the values in the HashMap are the ranks themselves.
     */
    private HashMap<String, Integer> rank = new HashMap<String, Integer>();

    /**
     * priorityList is a convenience list that just keeps all of the items to be ranked in the order they are ranked in.
     * So, to know which of two items has the higher ranking, find out the index of both of those items within the
     * priorityList LinkedList and compare the indexes. This way there is no need to actually think about the rank
     * itself, just the relative rank of two ranked objects.
     */
    private LinkedList<String> priorityList = null;

    /**
     * This ranking table's name
     */
    private String rankingTableName = null;

    /**
     * Default constructor that reads in all ranking table information from the ranking table. Reading the whole table
     * in speeds up look up times so that the table doesn't need to be queried every time {@link #get get()} is called.
     *
     * @param rankingTableName the name of the table containing ranking information
     * @param rankedColumnName the name of the column in tableName that contains what is ranked
     * @param rankedColumn     the name of the column in tableName that contains actual numeric ranking values
     * @param schema           Schema object this RankTable is associated with
     * @throws FatalDBUtilLibException if ranking information cannot be extracted successfully
     */
    public RankTable(String rankingTableName, String rankedColumnName, String rankedColumn, Schema schema)
            throws FatalDBUtilLibException {
        // Check that parameters are good.
        if ((rankingTableName == null) || (rankedColumnName == null) || (rankedColumn == null) || (schema == null)
                || (rankingTableName.length() == 0) || (rankedColumnName.length() == 0) || (rankedColumn.length() == 0)) {
            DBDefines.ERROR_LOG.add("RankTable constructor received an"
                    + " invalid parameter within the following parameters - tableName: " + rankingTableName
                    + ", rankedCol: " + rankedColumnName + ", rankCol: " + rankedColumn + ", schema: " + schema
                    + ". Throwing DBDefines." + "FatalDBUtilLibException.");
            throw new FatalDBUtilLibException("Invalid parameter passed to RankTable constructor.");
        }

        this.rankingTableName = rankingTableName.toUpperCase().trim();
        String rankedCol = rankedColumnName.toUpperCase().trim();
        String rankCol = rankedColumn.toUpperCase().trim();

        this.priorityList = new LinkedList<String>();
        // priorityList.add(rankedCol);

        // Create LinkedList of column names that are the columns to select
        // out of the ranking table.
        LinkedList<String> columns = new LinkedList<String>();
        columns.add(rankedCol);
        columns.add(rankCol);

        // Get everything out of the ranking table. This version of the
        // executeSelectStatement function returns a LinkedList of LinkedLists.
        // The LinkedLists within the greater LinkedList are the row values,
        // where each inner LinkedList's contents are values. Those values are
        // ordered in the same order as the columns that are handed to the
        // executeSelectStatement function. For example, say the ranking table
        // ranks authors, and has two columns in it - AUTH, RANK - and two rows
        // in it as follows:
        // AUTH RANK
        // AUTH1 5
        // AUTH2 10
        // executeSelectStatement would return
        // [[AUTH1, 5], [AUTH2, 10]]
        // Where [AUTH1, 5] is a LinkedList of length 2 where AUTH1 is what was
        // in the AUTH column and 5 is what was in the RANK column
        LinkedList<LinkedList<Object>> rows = schema.dao.executeSelectStatement(columns, this.rankingTableName, "1=1", "ORDER BY "
                + rankCol);

        // Ranking table empty - not a fatal error, but this will probably cause
        // problems later, so generate a warning.
        if (rows.size() == 0) {
            DBDefines.ERROR_LOG.add("Error in RankTable constructor. Ranking table " + this.rankingTableName + " empty.");
            return;
        }

        synchronized (this.priorityList) {
            // Walk through the rows returned from the ranking table, and get the
            // column to be ranked and the rank information itself out of them
            // and put that information in the rank HashMap.
            for (LinkedList<Object> row : rows) {
                String column = row.getFirst().toString();
                // row's first value is what needs to be ranked, and its second
                // value is the rank itself. Before adding these to the rank
                // HashMap, check to see if the first value is already in the
                // HashMap - that would be an error since the values should be unique.
                // if (rank.containsKey(column.toUpperCase()))
                if (this.rank.containsKey(column))
                    throw new FatalDBUtilLibException("Error in RankTable constructor. Ranking table"
                            + " contains more than one rank for " + ((String) row.getFirst()).toUpperCase()
                            + ". Only one rank" + " allowed.");

                int columnRank = ((BigDecimal) row.getLast()).intValue();
                this.rank.put(column, Integer.valueOf(columnRank));
                // rank.put(column.toUpperCase(), columnRank);

                // Add the item being ranked to the priorityList. Since the select
                // statement above order the ranked items when they were returned,
                // just adding them to the list in the order they are viewed by the
                // rows iterator will put them into the priorityList in ranked order.
                this.priorityList.add(column);
            }

            // Let users know if there's not any ranking information in the table
            // for the rankedCol column. We don't check to see if the table was
            // empty because the rankedCol column may not be this table's only column.
            if (this.priorityList.size() < 1)
                DBDefines.WARNING_LOG.add("Error in RankTable constructor: unable to successfully " + "extract "
                        + rankedCol + " ranking information from table " + this.rankingTableName + ".\n");
        }
    }

    /**
     * Return this ranking table's priority list. This list is a convenience list that just keeps all of the items to be
     * ranked in the order they are ranked in. So, to know which of two items has the higher ranking, find out the index
     * of both of those items within the priorityList LinkedList and compare the indexes. This way there is no need to
     * actually think about the rank itself, just the relative rank of two ranked objects.
     *
     * @return this ranking table's priority list
     */
    public LinkedList<String> getPriorityList() {
        return this.priorityList;
    }

    /**
     * Returns the ranking value for rankedItem.
     *
     * @param rankedItem item that needs to have its rank returned
     * @return rankedItem's ranking value; (Integer.MAX_VALUE if rankedItem is not in the ranking table)
     */
    public int get(String rankedItem) {
        synchronized (this.rank) {
            if (rankedItem == null)
                return Integer.MAX_VALUE;
            Integer r = this.rank.get(rankedItem.toUpperCase());
            if (r == null)
                return Integer.MAX_VALUE;
            return r.intValue();
        }
    }

    /**
     * Given a Collection of Row objects, find the one that has the highest priority (lowest rank).
     *
     * @param rows    the Collection of Rows from which the one with the highest priority (lowest rank) is to be chosen.
     * @param colName the name of the column that is to be used as the basis of the comparison.
     * @return the Row object in rows that has the highest priority (lowest rank). NULL is returned if rows is empty or
     * if none of the rows has a column named colName.
     */
    public Row get(Collection<Row> rows, String colName) {
        if (rows == null || rows.size() == 0)
            return null;
        Row highRank = null;
        int imin = Integer.MAX_VALUE;
        for (Row row : rows) {
            String val = row.getValueString(colName);
            int i = get(val);
            if (highRank == null || i < imin) {
                highRank = row;
                imin = i;
            }
        }
        return highRank;
    }

    /**
     * Return this ranking table's name
     *
     * @return this ranking table's name
     */
    public String getName() {
        return this.rankingTableName;
    }
}
