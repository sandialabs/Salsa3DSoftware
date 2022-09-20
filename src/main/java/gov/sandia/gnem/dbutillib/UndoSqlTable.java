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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * This class handles adding sql statements to a table of type UNDOSQL. These statements represent how to undo what an
 * application has done. Note that in order for anything to be written to an undosqltable, the table must be defined in
 * the schema specified in the constructor (a table of type undosql must have been added to the schema with the
 * xxxTables parameter).
 * <p>
 * In order to actually perform the undo, spool the results of executing the SQL below to a file: SELECT statement FROM
 * undoSqlTable ORDER BY undoid DESC; (Substitute the actual name of the undoSqlTable in the SQL above.) Then, execute
 * the SQL in that file.
 * <p>
 * The columns in a table of type UNDOSQL are <BR>
 * UNDOID NUMBER(9) <BR>
 * STATEMENT VARCHAR2(4000) <BR>
 * LDDATE DATE
 */
public class UndoSqlTable {
    /**
     * Table representing the UndoSqlTable.
     */
    private Table undoSqlTable;

    /**
     * Unique id that can be used for each UndoSqlTable row when the schema's idgaps table is null.
     */
    private long undoId;

    /**
     * Idgaps table containing id information for the undosql table. If this is null, then undoId will be the
     * max(undoid) + 1 from the undosql table.
     */
    private IDGapsTable idGapsTable;

    /**
     * Load date to be used for rows inserted into this table.
     */
    private Date ldDate;

    /**
     * Constructor. Retrieves the table of type undosql from the schema. If that table does not exist in the schema,
     * then nothing is done.
     *
     * @param schema schema this table is associated with; this schema must contain a table of type "UNDOSQL" which will
     *               be retrieved by the constructor
     * @param ldDate load date to be used for rows inserted into this table
     * @throws FatalDBUtilLibException if an error occurs
     */
    public UndoSqlTable(Schema schema, Date ldDate) throws FatalDBUtilLibException {
        this.ldDate = ldDate;
        this.undoSqlTable = schema.getTableOfType("UNDOSQL");

        if (this.undoSqlTable != null) {
            // Create the table if it does not exist.
            if (!schema.dao.tableExists(this.undoSqlTable)) {
                if (!schema.dao.createTable(this.undoSqlTable)) {
                    String msg = "Error in UndoSqlTable constructor.  Unable to " + "create undosql table.\n";
                    throw new FatalDBUtilLibException(msg);
                }
            }

            // Get the max id of the undosql table if the schema does not
            // (Note: addSqlStatement handles incrementing undoId.)
            this.idGapsTable = schema.idGapsTable;
            if (this.idGapsTable == null)
                this.undoId = schema.dao.getMaxID("UNDOID", this.undoSqlTable.getName());
        }
    }

    /**
     * If the target schema that was specified when this UndoSqlTable object was constructed contained a table of type
     * UNDOSQL, then this object is active and will record undo information that will be stored in the undosql table. If
     * the target schema does not contain a table of type undosql, then this object is not active, and it will basically
     * do nothing (all of its methods return without doing anything).
     *
     * @return boolean whether or not this object is active.
     */
    public boolean isActive() {
        return this.undoSqlTable != null;
    }

    /**
     * Adds statement to the undoSqlTable.
     *
     * @param statement SQL statement to be added to the undo table
     */
    public synchronized void addSqlStatement(String statement) {
        if (this.undoSqlTable == null || statement == null)
            return;
        Object[] rowValues;
        Long id;
        StringBuilder error = new StringBuilder();
        if (this.idGapsTable != null) {
            id = this.idGapsTable.getNextId("UNDOID");
            if (id == null) {
                id = Long.valueOf(++this.undoId);
                error.append("Unable to retrieve UNDOID value from idgaps table. Using " + id);
            }
        } else
            id = Long.valueOf(++this.undoId);

        rowValues = new Object[]{id, statement, this.ldDate};
        try {
            Row sqlRow = new Row(this.undoSqlTable, rowValues, false);
            sqlRow.insertIntoDB();
        } catch (FatalDBUtilLibException e) {
            if (e.getMessage().contains("unique constraint")) {
                if (this.idGapsTable != null) {
                    HashMap<String, ArrayList<Long>> unusedValues = new HashMap<String, ArrayList<Long>>();
                    ArrayList<Long> unusedUndoId = new ArrayList<Long>();
                    unusedUndoId.add(id);
                    unusedValues.put("UNDOID", unusedUndoId);
                    this.idGapsTable.returnUnusedValues(unusedValues);
                } else
                    this.undoId--;
            } else {
                error.append("Unable to insert row into undoSQLTable: " + this.undoSqlTable.getName()
                        + " with values: ");

                for (Object i : rowValues)
                    error.append(i.toString() + " ");
            }
        }
        if (error.length() > 0)
            DBDefines.ERROR_LOG.add("Error in UndoSqlTable.addSqlStatement.  " + error.toString());
    }

    /**
     * Adds statements to the undoSqlTable.
     *
     * @param statements LinkedList of SQL statements to be added to the undo table; the last item in the list will be
     *                   the first item written to the table since the last item in the last should be the first thing to be undone
     */
    public void addSqlStatement(LinkedList<String> statements) {
        if (this.undoSqlTable == null)
            return;
        while (statements.size() > 0)
            addSqlStatement(statements.removeLast());
    }

    /**
     * Adds undo statement for a row to the undoSqlTable and removes the undo sql statement from undoRow (to free up
     * memory).
     *
     * @param undoRow row with undo SQL statement to be added to the undo table; once the SQL statement has been added,
     *                it is cleared out of the undoRow object (frees up memory)
     */
    public void addSqlStatement(Row undoRow) {
        if (this.undoSqlTable == null)
            return;
        addSqlStatement(undoRow.getSqlUndo());
        undoRow.clearSqlUndo();
    }
}
