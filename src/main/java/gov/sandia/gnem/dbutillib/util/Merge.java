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

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import gov.sandia.gnem.dbutillib.Column;
import gov.sandia.gnem.dbutillib.IDGapsTable;
import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Relationship;
import gov.sandia.gnem.dbutillib.RemapTable;
import gov.sandia.gnem.dbutillib.Row;
import gov.sandia.gnem.dbutillib.RowGraph;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.Table;
import gov.sandia.gnem.dbutillib.UndoSqlTable;
import gov.sandia.gnem.dbutillib.dao.DAODatabase;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;
import gov.sandia.gnem.dbutillib.util.MergeStatistics.Statistics;

/**
 * <p>
 * This class implements functionality to merge rows from a set of source tables that are part of a source schema, into
 * a set of corresponding target tables that are part of a target schema.
 * <p>
 * During a merge: <br>
 * a) All foreign keys are remapped to ensure that primary key constraints are not violated in the target tables. New ID
 * values are obtained from an IDGaps table associated with the target schema. (If the IDGaps table is set to null, then
 * no merge will take place and data from the source will simply be inserted into the target.) <br>
 * b) Information is added to the target schema's Remap table which remaps the old value of an ID (the value in the
 * source table) to its new value (the value in the target table). <br>
 * c) Source rows are set to either have copies inserted into the target, to have their data update target data, or to
 * be ignored and not represented in the target in any way (typically because their data is already in the target). Rows
 * are copied from the source tables to the target tables only if the unique key information contained in the source row
 * does not already exist in the target tables.
 * <p>
 * See {@link #mergeRows mergeRows} method for more information on how the actual merge is performed or
 * {@link #execute(RowGraph, MergeStatistics, boolean, RemapTable, IDGapsTable, UndoSqlTable, boolean) execute} for a
 * method that calls {@link #mergeRows mergeRows} and then inserts or updates the rows in the target schema.
 * <p>
 * <i>Note: If the user wishes to simply insert data, not merge it, into the target schema, this can be accomplished by
 * setting the idgaps table in the target schema to null and calling
 * {@link #execute(RowGraph, MergeStatistics, boolean, RemapTable, IDGapsTable, UndoSqlTable, boolean) execute} . In so
 * doing, {@link #mergeRows mergeRows} will not be called.</i>
 * <p>
 * The class requires the following 3 pieces of information:
 * <p>
 * 1) The source {@link Schema schema}.
 * <p>
 * 2) The target {@link Schema schema}.
 * <p>
 * 3) An optional Set<{@link Table Tables}> that includes Table objects from the source schema for which there is no
 * corresponding Table of the same type in target schema. This class assumes the reader is familiar with {@link Schema
 * Schema}, {@link RowGraph RowGraph}, and {@link Row Row} objects. In order to get a decent understanding of what this
 * class's purpose is, it's best to peruse and understand the {@link #mergeRows mergeRows} method.
 * <p>
 * Copyright (c) 2004
 * </p>
 * <p>
 * Sandia National Laboratories
 * </p>
 *
 * @author Sandy Ballard
 */

/*
 * General design notes: Most of the design of this class is described above since it is useful for people wishing to
 * understand the class as well as those who might need to update it.
 */
public class Merge {
    /**
     * The Schema from which information is to be extracted during the merge.
     */
    private Schema sourceSchema = null;

    /**
     * The Schema into which information is to be inserted during the merge.
     */
    private Schema targetSchema = null;

    /**
     * Tables in the source that have no table of the same type in the target. This is not deduced - the calling
     * application must specify this using one of the Merge constructors. If no tables are specified, then for each
     * table type represented in the source schema there must be a table of the same type in the target schema.
     * Otherwise, if the source schema has a table type that is not represented in the target schema, and that table
     * type is in missingTargetTables, then Merge will treat that source table as being in both the source and target
     * schemas. This is typically used when there is a table in the source schema that data in the source AND the target
     * schema refers to.
     */
    private Set<Table> missingTargetTables = null;

    /**
     * A HashMap from a sourceTableName -> uniqueKeyEquality {@link Relationship Relationship}. <br>
     * During a merge, it is necessary to determine if a source row contains new information or if the information it
     * contains already exists in the target table. Each table has its own unique key equality test that is represented
     * by a {@link Relationship Relationship} object, which is essentially a SQL SELECT statement. If the select
     * statement, once populated with data from the source row for which we are trying to determine if there is an equal
     * row in the target, does not return any rows, the source row is new and can be inserted into the target.
     * Otherwise, it is not new and cannot be inserted. Table name is used as the key into this HashMap since a
     * {@link Row Row} object knows what table it is a member of; this HashMap can then be used to look up which
     * {@link Relationship Relationship} object to use to find out if the information in a given source row is new or
     * not.
     */
    private HashMap<String, Relationship> uniqueKeyEquality = new HashMap<String, Relationship>();

    /**
     * A HashMap from a sourceTableName -> foreignKeyEquality {@link Relationship Relationship}. <br>
     * During a merge, it is necessary to determine if a source row contains new information or if the information it
     * contains already exists in the target table. Each table has its own foreign key equality test that is represented
     * by a {@link Relationship Relationship} object, which is essentially a SQL SELECT statement. If the select
     * statement, once populated with data from the source row for which we are trying to determine if there is an equal
     * row in the target, does not return any rows, the source row is new and can be inserted into the target.
     * Otherwise, it is not new and cannot be inserted. Table name is used as the key into this HashMap since a
     * {@link Row Row} object knows what table it is a member of; this HashMap can then be used to look up which
     * {@link Relationship Relationship} object to use to find out if the information in a given source row is new or
     * not.
     */
    private HashMap<String, Relationship> foreignKeyEquality = new HashMap<String, Relationship>();

    /**
     * A HashMap from sourceTableName -> boolean which will be true when the foreignKeyEqualityTest and the
     * uniqueKeyEqualityTest for a given source table are identical. This is only relevant for rows from non-idowner
     * source tables. This HashMap is created in {@link #createEqualityTests createEqualityTests} and is used by
     * {@link #mergeNonIdOwnerRows mergeNonIdOwnerRows}. This is useful because if the two are identical, there is no
     * need to evaluate them both since they will return the same rows.
     */
    private HashMap<String, Boolean> identicalEqualityTests = new HashMap<String, Boolean>();

    /**
     * Some tables have unique keys which include foreign keys. Given a source row from such a table, when the target
     * table is queried for a row which is equal, we must first remap the source foreign key to corresponding target
     * values. In order for this to happen, the source rows from the table that owns that id the foreign key refers to
     * must be mapped first (during {@link #mergeIdOwnerRows mergeIdOwnerRows}). This means that there is a dependency
     * where merging rows from table B, which depends on Table A, should not take place until all the rows from table A
     * have been traversed and appropriate entries made in the remap table. {@link #sourceIdOwnerTables
     * sourceIdOwnerTables} is created such that tables that have rows that need to be remapped before other table rows
     * are processed before those other tables. dependencyMap maps Table -> LinkedList of column indices in that table
     * that are foreign keys.
     */
    private HashMap<Table, LinkedList<Integer>> dependencyMap = new HashMap<Table, LinkedList<Integer>>();

    /**
     * Source idowner tables
     */
    private LinkedList<Table> sourceIdOwnerTables = new LinkedList<Table>();

    /**
     * Source non-idowner tables
     */
    private LinkedList<Table> sourceNonIdOwnerTables = new LinkedList<Table>();

    /**
     * {@link Merge.Priority Priority} used to determine when source row information should replace target row
     * information due to the source row's priority being higher.
     * <p>
     * See {@link #setPriorities setPriorities()} and {@link Merge.Priority Priority class} comments for more
     * information.
     */
    protected Priority priority = null;

    /**
     * New rows added to the target will have their LDDATE column set to this date.
     */
    private Date ldDate = new Date();

    /**
     * If fixLdDate is false, then the LDDATE column of all rows added to the target will be set to {@link #ldDate
     * ldDate} to reflect the date/time data was loaded. If fixLdDate is true, then the LDDATE columns' values will not
     * be changed.
     */
    private boolean fixLdDate = false;

    /**
     * The {@link UndoSqlTable undoSqlTable} for the merge. An undosql table contains sql statements to undo what the
     * merge does. When executed from highest to lowest id, the statements revert the database to the state it was in
     * before merge was called.
     */
    // private UndoSqlTable undoSqlTable = null;
    /**
     * If this is false, then no row history information will be stored since it takes up so much room in memory and
     * generates voluminous output in the log files. If this is true, row history information will be stored - memory
     * usage be damned!
     */
    private boolean recordRowHistories = false;

    /**
     * This constructor calls {@link #Merge(Schema, Schema, Set) Merge} with the given source and target schemas and an
     * empty Set for the missingTargetTables parameter. Please see {@link #Merge(Schema, Schema, Set) Merge} for more
     * information.
     *
     * @param sourceSchema the schema with data to be merged into the target schema
     * @param targetSchema the schema into which rows are to be merged
     * @throws FatalDBUtilLibException if an error occurs when creating the merge object
     */
    public Merge(Schema sourceSchema, Schema targetSchema) throws FatalDBUtilLibException {
        this(sourceSchema, targetSchema, new HashSet<Table>());
    }

    /**
     * This constructor calls {@link #Merge(Schema, Schema, Set) Merge} with the given schema as the source and target
     * schema and with an empty Set for the missingTargetTables parameter. Please see {@link #Merge(Schema, Schema, Set)
     * Merge} for more information.
     *
     * @param targetSchema the schema with data to be merged into itself
     * @throws FatalDBUtilLibException if an error occurs when creating the merge object
     */
    public Merge(Schema targetSchema) throws FatalDBUtilLibException {
        this(targetSchema, targetSchema, new HashSet<Table>());
    }

    /**
     * Constructor specifying the source schema with data to be merged into the target schema. After the Merge object is
     * created, it will be ready to merge Row objects using {@link #mergeRows mergeRows()} or {@link #execute execute()}
     * . This constructor is primarily responsible for determining which tables have dependencies on other tables. A
     * dependency is when a particular table must be processed before a given table can be processed. This typically
     * occurs when a table contains foreign keys to another table's id, and that id needs to be remapped before the
     * foreign keys that refer to that id can be processed.
     * <p>
     * <i>Note on LDDATEs:</i> <br>
     * Schemas support a couple of different options for handling LDDATES. One option that merge recognizes (that
     * applies to ALL tables in the source schema), is the fix_lddate option where lddates from the source are not
     * updated to the current date before being placed in the target. In order to use this option, the source schema's
     * {@link Schema#getLddateOption getLddateOption} method must be equal to {@link DBDefines#FIX_LDDATE
     * DBDefines.FIX_LDDATE}. See {@link Schema#Schema(ParInfo, String) Schema constructor} for more information.
     * <p>
     * <i>Note on {@link IDGapsTable IDGapsTables} and inserting instead of merging:</i> <br>
     * If no {@link IDGapsTable IDGapsTable} is found within the target schema, new id numbers will be assigned by
     * locating the highest id currently in use and incrementing it by one. <br>
     * If the target schema's idgaps table is set to null, then no merge will take place and data from the source will
     * simply be inserted into the target.)
     *
     * @param sourceSchema        the schema with data to be merged into the target schema
     * @param targetSchema        the schema into which rows are to be merged
     * @param missingTargetTables an optional set of Tables from the source schema that are referred to by the target
     *                            schema. If no tables are specified, then for each table type represented in the source schema there must be a
     *                            table of the same type in the target schema. Otherwise, if the source schema has a table type that is not
     *                            represented in the target schema, and that table type is in missingTargetTables, then Merge will treat that
     *                            source table as being in both the source and target schemas. This is typically used when there is a table in the
     *                            source schema that data in the source AND the target schema refer to.
     * @throws FatalDBUtilLibException if an error occurs when creating the merge object
     */
    public Merge(Schema sourceSchema, Schema targetSchema, Set<Table> missingTargetTables)
            throws FatalDBUtilLibException {
        if (sourceSchema == null || targetSchema == null) {
            StringBuilder error = new StringBuilder("Error in Merge constructor.\n");
            if (sourceSchema == null)
                error.append("Source Schema is null\n");
            if (targetSchema == null)
                error.append("Target Schema is null\n");
            error.append("No null schema values are acceptable as parameters.");
            throw new FatalDBUtilLibException(error.toString());
        }

        this.sourceSchema = sourceSchema;
        this.targetSchema = targetSchema;

        // Determine if lddates are allowed to be changed or not
        if (this.sourceSchema.getLddateOption().equals(DBDefines.FIX_LDDATE))
            this.fixLdDate = true;

        if (missingTargetTables == null)
            this.missingTargetTables = new HashSet<Table>();
        else
            this.missingTargetTables = missingTargetTables;

        // this.undoSqlTable = new UndoSqlTable(this.targetSchema, this.ldDate);

        // If the target idgaps table is null, no "merging" will happen - more of "inserting". So,
        // the rest of the constructor can be ignored
        if (this.targetSchema.getIdGapsTable() != null)
            // populate sourceIdOwnerTables and sourceNonIdOwnerTables
            createSourceTablesLists();
    }

    /**
     * Populate sourceIdOwnerTables and sourceNonIdOwnerTables. Some idowner tables may have to be processed in a
     * particular order if they have foreign key related dependencies on other idowner tables. This method determines
     * those dependencies and populates {@link #dependencyMap dependencyMap} accordingly and orders
     * {@link #sourceIdOwnerTables sourceIdOwnerTables} such that tables are processed in the appropriate order.
     *
     * @throws FatalDBUtilLibException if an error occurs
     */
    private void createSourceTablesLists() throws FatalDBUtilLibException {
        // HashMap from source idowner tables to the tables that the source idowner foreign keys
        // refer to.
        // This HashMap's keys are the source idowner tables. For every column in those tables that
        // is both a unique and a foreign key, the HashMap's value HashSet will contain the table
        // that owns the id that the foreign key points to. In other words, the HashSet values
        // contains the tables that the source idowner table is dependent on. These tables have to
        // be remapped before the source table can be remapped since the source idowner table's
        // unique keys include foreign keys.
        LinkedHashMap<Table, HashSet<Table>> ufKeyTables = new LinkedHashMap<Table, HashSet<Table>>();

        // Keep track of if any unique keys that are foreign keys that are allowed to be modified
        // (fixedFK for that key's column is false) refer to columns in an idowner table that is not
        // present in the schema. This flag is used so that if there are multiple errors of this
        // type, they can all be output to the error log before an exception is thrown.
        boolean nullIdOwnerTableFound = false;

        // Iterate over all the source tables and check that tables in the source have a
        // corresponding table in either the target schema or missingTargetTables while also
        // populating ufKeyTables.
        for (Table sourceTable : this.sourceSchema.getTables()) {
            // idowner table
            if (sourceTable.isIdOwner())
                ufKeyTables.put(sourceTable, new HashSet<Table>());
                // constructed later when the dependencies are worked out.
            else
                this.sourceNonIdOwnerTables.add(sourceTable);

            // Check that source tables have a corresponding table in the target
            // schema or in missing tables.
            Table targetTable = this.targetSchema.getTableOfType(sourceTable.getTableType());
            if (targetTable == null) {
                if (this.missingTargetTables.contains(sourceTable))
                    continue;
                throw new FatalDBUtilLibException("Error in Merge constructor: The source schema has a table named: "
                        + sourceTable.getName() + " of type " + sourceTable.getTableType()
                        + " but there is no table of that type in the target schema.\n");
            }

            // createSourceToTargetRelationships returns true if an idowner table is found that
            // has a foreign key that refers to a table that is not in the schema when that foreign
            // key's navalue is set to not allowed
            nullIdOwnerTableFound = nullIdOwnerTableFound
                    || createSourceToTargetRelationships(sourceTable, targetTable, ufKeyTables);
        }

        // Throw this error here so that all of the "Can't find the idowner table in the source
        // schema" errors can be accumulated instead of forcing the user to re-run merge after each
        // fix to find the next error.
        if (nullIdOwnerTableFound)
            throw new FatalDBUtilLibException("Error creating Merge object");

        populateSourceIdOwnerTables(ufKeyTables);
    }

    /**
     * Set up all of equality test information necessary to merge rows from sourceTable into targetTable.
     *
     * @param sourceTable the source table with data to be merged into targetTable
     * @param targetTable the target table that will have sourceTable's data merged into it
     * @param ufKeyTables HashMap from source idowner tables to the tables that the source idowner foreign keys refer
     *                    to.
     * @return whether or not a null idowner table was found when processing these source and target tables. This occurs
     * when a table has a foreign key that refers to another table that is not in the schema. If the foreign key's
     * navalue is not allowed, then Merge cannot continue. If the foreign key is allowed to be set to an na value, then
     * Merge can continue, and this method will return true
     * @throws FatalDBUtilLibException if an error occurs
     */
    private boolean createSourceToTargetRelationships(Table sourceTable, Table targetTable,
                                                      LinkedHashMap<Table, HashSet<Table>> ufKeyTables) throws FatalDBUtilLibException {
        // Track whether or not a table's foreign refers to a table that is not in the schema and
        // that foreign key's navalue is set to not allowed
        boolean nullIdOwnerTableFound = false;

        Column[] targetColumns = targetTable.getColumns();
        int[] targetUniqueKeyIndexes = targetTable.getUniqueKeyColumnIndexes();
        LinkedHashSet<String> targetUniqueKeys = new LinkedHashSet<String>();

        // This for loop populates ufKeyTables, finds the unique keys on the target table, and
        // populates dependencyMap. (This loop loops over the target table since that is where
        // the unique keys are set.)
        for (int targetUniqueKeyIndex : targetUniqueKeyIndexes) {
            // The name of the column in the target table that is unique key
            String targetUniqueKey = targetColumns[targetUniqueKeyIndex].getName();

            // If includeColumn is true, then the current target column should be included in
            // the target columns to check when determining if there are rows equal to a source
            // in the target.
            // Do not include any target columns that are not in the source table.
            boolean includeColumn = sourceTable.getColumn(targetUniqueKey) != null;

            // If the target table is an idowner table, and the targetUniqueKey is the ownedid,
            // do not include it in the unique field equality test.
            if (targetTable.isIdOwner() && targetUniqueKey.equals(targetTable.getOwnedID()))
                includeColumn = false;

            // If this column name can be included, then an entry will be added to the where
            // clause for this column. Otherwise, continue to the next column.
            if (!includeColumn)
                continue;

            // Unique keys on the target table (used by createSourceToTargetRelationships)
            targetUniqueKeys.add(targetUniqueKey);

            // If this unique key is also a foreign key, and the foreign key is allowed to be
            // modified (the column's fixedFK is not true) then save an association from the
            // sourceTable to the index of the foreign key column in the table it refers to.
            // Otherwise, continue to the next column.
            String fkey = sourceTable.getColumn(targetUniqueKey).getForeignKey();
            if (fkey == null || targetColumns[targetUniqueKeyIndex].getFixedFK())
                continue;

            // See if the dependency map has an entry for this sourceTable & if not.
            if (this.dependencyMap.get(sourceTable) == null)
                this.dependencyMap.put(sourceTable, new LinkedList<Integer>());

            // add the index of the column that is a foreign key to the list of such column
            // indexes for this sourceTable.
            this.dependencyMap.get(sourceTable).add(Integer.valueOf(sourceTable.getColumnIndex(targetUniqueKey)));

            // Make sure the the Table that owns the id that corresponds to this foreign key is
            // also in the source schema and add the table dependency to ufKeyTables
            Table idOwner = this.sourceSchema.getOwnedIDTable(fkey);
            if (idOwner != null)
                ufKeyTables.get(sourceTable).add(idOwner);
            else {
                String errorMessage = "";
                // Only need this "error in merge constructor" message once in the output
                if (nullIdOwnerTableFound == false)
                    errorMessage = "Error in merge constructor\n";
                errorMessage += "\t" + sourceTable.getName() + "'s " + targetUniqueKey
                        + " column is both a unique key and a foreign key.\n\tThis means that the"
                        + " table that owns " + targetUniqueKey + "\n\tmust be included in the"
                        + " source schema, but it is not included.";
                DBDefines.ERROR_LOG.add(errorMessage);

                // Set this to true since we have an unworkable situation, but continue
                // the loop so we can accumulate all of the null idowner table errors
                // instead of stopping at the first one found.
                nullIdOwnerTableFound = true;
            }
        }
        // Only add entries if the Merge constructor is going to create a viable Merge object.
        // If nullIdOwnerTableFound is true, then we can't create a usable Merge object.
        if (!nullIdOwnerTableFound) {
            checkTargetColumns(targetTable, sourceTable);
            createEqualityTests(sourceTable, targetTable, targetUniqueKeys);
        }
        return nullIdOwnerTableFound;
    }

    /**
     * Check for issues with the target table columns. <br>
     * For target columns that have no corresponding column in the source table, check that the target column is allowed
     * to have NAValues since that's what the field will have to be set to since no value for that column will be
     * available from the source table. (Ignore target columns that start with {@link DBDefines#VALUEOF
     * DBDefines.VALUEOF} since those can only be checked when actual values from a Row are available). <br>
     * Check if there is a foreign key that refers to an idowner table that is not part of the schema. If so, and that
     * foreign key is allowed to be set to an navalue, issue a warning that that foreign key will be set to its navalue.
     * If so, and that foreign key is not allowed to be set to an navalue, throw an exception
     *
     * @param targetTable target table whose columns are being checked
     * @param sourceTable target table's corresponding table type in the source schema
     * @throws FatalDBUtilLibException if an error occurs
     */
    private void checkTargetColumns(Table targetTable, Table sourceTable) throws FatalDBUtilLibException {
        Column[] targetTableColumns = targetTable.getColumns();
        for (Column targetCol : targetTableColumns) {
            // Make sure that if the target table has a column that is not in the source table, Merge
            // can use the NA_Value for that column.
            boolean colPresentInSource = sourceTable.getColumn(targetCol.getName()) != null;
            if (!colPresentInSource && !targetCol.NAValueAllowed())
                throw new FatalDBUtilLibException("ERROR in Merge.checkTargetColumns." + "  Target table "
                        + targetTable.getName() + "'s " + targetCol.getName()
                        + " column's NA_VALUE is NOT_ALLOWED and source table " + sourceTable.getName()
                        + " does not have a column with the same name.  There is not enough information"
                        + " to determine what value to insert into new target " + targetTable.getName() + " rows\n");

            // Check if there is a foreign key that refers to an idowner table is not part of the
            // schema or within the missing target tables. If the foreign key has an allowable
            // navalue, issue a warning that this field will be set to its navalue in all new target
            // rows. If the foreign key does not have an acceptable navalue, throw an exception
            // since if the table the foreign key refers to is absent, and the foreign key can't be
            // set to an navalue, then the foreign key cannot be set.
            if (targetCol.getForeignKey() != null && !targetCol.getForeignKey().startsWith(DBDefines.VALUEOF)
                    && !idOwnerTableInMissingTargetTables(targetCol)
                    && targetTable.getSchema().getOwnedIDTable(targetCol.getForeignKey()) == null) {
                // If the foreign key is fixed, then it's not going to be changed.
                if (targetCol.getFixedFK())
                    break;

                if (targetCol.NAValueAllowed())
                    DBDefines.WARNING_LOG.add("WARNING in Merge.checkTargetColumns. " + " Target table "
                            + targetTable.getName() + " has foreign key " + targetCol.getName()
                            + " but the table that owns that key is not a member of the"
                            + " target schema.  The value of " + targetCol.getName() + " will be set to the NA_VALUE ("
                            + targetCol.getNAValue() + ") in all target Rows.\n");
                else
                    throw new FatalDBUtilLibException("Error in Merge.checkTargetColumns.  " + targetTable.getName()
                            + "'s foreign key column " + targetCol.getName()
                            + " refers to a table that is not in the schema and this column is not"
                            + " allowed to be set to an na_value.  Therefore, the value of this column"
                            + " is unable to be determined.");
            }
        }
    }

    /**
     * Check if an the table that owns the id in column is part of the {@link #missingTargetTables missingTargetTables}.
     *
     * @param column {@link #missingTargetTables missingTargetTables} will be checked to see if the table that owns the
     *               id in column is present
     * @return true if the table that owns the id in column is present in {@link #missingTargetTables
     * missingTargetTables}; false otherwise
     */
    private boolean idOwnerTableInMissingTargetTables(Column column) {
        if (this.missingTargetTables == null)
            return false;
        for (Table table : this.missingTargetTables)
            if (table.getOwnedID() != null && table.getOwnedID().equals(column.getName()))
                return true;

        return false;
    }

    /**
     * Create the foreign key and unique key equality tests that check for rows in the target table that are equal to a
     * row in the source table.
     *
     * @param sourceTable the source table with data to be merged into targetTable
     * @param targetTable the target table that will have sourceTable's data merged into it
     * @param uniqueKeys  target table's unique keys
     * @throws FatalDBUtilLibException if an error occurs
     */
    private void createEqualityTests(Table sourceTable, Table targetTable, LinkedHashSet<String> uniqueKeys)
            throws FatalDBUtilLibException {
        LinkedHashSet<String> foreignKeys = new LinkedHashSet<String>();

        // if the source table is a non-idowner table, then its unique key equality test will
        // incorporate foreign keys (see findNonIdownerForeignKeys method comments for more
        // information). Find those foreign keys and add them to the unique keys.
        if (!sourceTable.isIdOwner()) {
            foreignKeys = findNonIdOwnerForeignKeys(sourceTable, targetTable);

            // Combining the uniqueKeyEqualityTest and the foreignKeyEqualityTest yields an all
            // encompassing equality test that can be used to see if there is a row in the target
            // table that is the same as a row being merged from the source table.
            uniqueKeys.addAll(foreignKeys);
        }

        String uniqueKeyEqualityTest = createUniqueKeyEqualityTest(targetTable, uniqueKeys);
        String foreignKeyEqualityTest = createForeignKeyEqualityTest(targetTable, foreignKeys);

        // Non-idowner tables combine unique and foreign key equality tests
        if (!sourceTable.isIdOwner()) {
            // if there are any unique keys, put a "true" entry into ideniticalEqualityTests for
            // this table since an all encompassing equality test incorporating both unique and
            // foreign keys is used for nonidowner tables.
            boolean uniqueKeysExist = (uniqueKeys.size() > 0);
            this.identicalEqualityTests.put(sourceTable.getName(), Boolean.valueOf(uniqueKeysExist));
        }

        // Add the foreign and unique key equality tests to this.foreignKeyEquality and
        // this.uniqueKeyEquality
        if (foreignKeyEqualityTest.length() > 0)
            this.foreignKeyEquality.put(sourceTable.getName(), new Relationship("ForeignKeyEqualityTest_"
                    + sourceTable.getTableType(), sourceTable, targetTable, foreignKeyEqualityTest,
                    DBDefines.CONSTRAINT_0_1));

        if (uniqueKeyEqualityTest.length() > 0)
            this.uniqueKeyEquality.put(sourceTable.getName(), new Relationship("UniqueFieldEqualityTest_"
                    + sourceTable.getTableType(), sourceTable, targetTable, uniqueKeyEqualityTest,
                    DBDefines.CONSTRAINT_0_1));
    }

    /**
     * Find the foreign keys for non-idowner tables. This method is useful since non-idowner tables need to incorporate
     * foreign keys when checking if the target table already has a row equal to the source row.
     *
     * @param sourceTable the source table with data to be merged into targetTable
     * @param targetTable the target table that will have sourceTable's data merged into it
     * @return the foreign keys for sourceTable (provided it is a non-idowner table); null if sourceTable is an idowner
     * table
     */
    private LinkedHashSet<String> findNonIdOwnerForeignKeys(Table sourceTable, Table targetTable) {
        // only for non-idowner tables
        if (sourceTable.isIdOwner())
            return null;

        LinkedHashSet<String> foreignKeys = new LinkedHashSet<String>();

        // Add all the foreign keys in the source table that are involved in relationships (not
        // deduced from the table definition table) to the list of foreign keys
        int[] sourceTableIdLinks = sourceTable.getIdLinks();
        Column[] sourceTableColumns = sourceTable.getColumns();
        for (int i : sourceTableIdLinks)
            foreignKeys.add(sourceTableColumns[i].getName());

        // Check to see if the application fixed any of the foreign keys in the source or
        // target tables by calling Column.fixId(true). The calling application would normally
        // only do this if the target schema did not contain the idowner table for the foreign
        // key in question. It is safe to assume that if the application fixed a foreign key,
        // then they meant for it to be a foreign key. Add it to the list of foreign keys.
        int[] sourceTableForeignKeys = sourceTable.getForeignKeyColumnIndexes();
        for (int i : sourceTableForeignKeys)
            if (sourceTableColumns[i].getFixedFK())
                foreignKeys.add(sourceTableColumns[i].getName());

        Column[] targetTableColumns = targetTable.getColumns();
        int[] targetTableForeignKeys = targetTable.getForeignKeyColumnIndexes();
        for (int i : targetTableForeignKeys)
            if (targetTableColumns[i].getFixedFK())
                foreignKeys.add(targetTableColumns[i].getName());

        return foreignKeys;
    }

    /**
     * Create the where clause used by the {@link Relationship Relationship} that is executed to determine if the target
     * table has a row that is equal to a certain source table row
     *
     * @param targetTable target table that the unique key equality where clause will be executed against
     * @param uniqueKeys  unique keys used to generate the where clause
     * @return where clause used by the {@link Relationship Relationship} that is executed to determine if the target
     * table has a row that is equal to a certain source table row
     */
    private String createUniqueKeyEqualityTest(Table targetTable, LinkedHashSet<String> uniqueKeys) {
        StringBuilder uniqueKeyEqualityTest = new StringBuilder();

        if (uniqueKeys.size() == 0)
            uniqueKeyEqualityTest.append("WHERE FALSE");
        else {
            String delim = "WHERE ";
            for (String s : uniqueKeys) {
                if (s.startsWith(DBDefines.VALUEOF))
                    s += "=#" + s + "#";
                else
                    s = targetTable.getColumn(s).getEqualityRelation();

                uniqueKeyEqualityTest.append(delim);
                uniqueKeyEqualityTest.append(s);
                delim = " AND ";
            }
        }
        return uniqueKeyEqualityTest.toString();
    }

    /**
     * Create the where clause used by the {@link Relationship Relationship} that is executed to determine if the target
     * table has a row that is equal to a certain source table row
     *
     * @param targetTable target table that the foreign key equality where clause will be executed against
     * @param foreignKeys foreign keys used to generate the where clause
     * @return where clause used by the {@link Relationship Relationship} that is executed to determine if the target
     * table has a row that is equal to a certain source table row
     */
    private String createForeignKeyEqualityTest(Table targetTable, LinkedHashSet<String> foreignKeys) {
        StringBuilder foreignKeyEqualityTest = new StringBuilder();
        if (foreignKeys.size() > 0) {
            String delim = "WHERE ";
            for (String s : foreignKeys) {
                if (s.startsWith(DBDefines.VALUEOF))
                    s += "=#" + s + "#";
                else
                    s = targetTable.getColumn(s).getEqualityRelation();

                foreignKeyEqualityTest.append(delim);
                foreignKeyEqualityTest.append(s);
                delim = " AND ";
            }
        }
        return foreignKeyEqualityTest.toString();
    }

    /**
     * Populate {@link #sourceIdOwnerTables sourceIdOwnerTables}. This method iterates over all of the idowner tables in
     * the source schema and determines which tables have dependencies on other tables. A dependency is when a
     * particular table must be processed before a given table since the given table's foreign keys refer to this other
     * table's id. This method populates {@link #sourceIdOwnerTables sourceIdOwnerTables} such that the tables with
     * dependencies get processed after the tables they depend on. This is done by using the information in ufKeyTables
     * (created in {@link #createSourceTablesLists createSourceTablesLists}).
     *
     * @param ufKeyTables HashMap from source idowner tables to the tables that the source idowner foreign keys refer
     *                    to; populated in {@link #createSourceTablesLists createSourceTablesLists}. For every column in the source idowner
     *                    tables that is both a unique and a foreign key, the HashMap's value HashSet will contain the table that owns the
     *                    id that the foreign key points to. In other words, the HashSet values contains the tables that the source idowner
     *                    table is dependent on. These tables have to be remapped before the source table can be remapped since the source
     *                    idowner table's unique keys include foreign keys.
     * @throws FatalDBUtilLibException if an error occurs
     */
    private void populateSourceIdOwnerTables(LinkedHashMap<Table, HashSet<Table>> ufKeyTables)
            throws FatalDBUtilLibException {
        // Iterate over all the source idowner tables that are in ufKeyTables and populate
        // sourceIdOwnerTables so that tables that are dependent on other tables get remapped after
        // the tables they are dependent on
        for (Table sourceTable : ufKeyTables.keySet()) {
            // source table dependencies
            HashSet<Table> dependencies = ufKeyTables.get(sourceTable);

            // if this source idowner table is not dependent on any other tables, add it to the
            // front of the list
            if (dependencies.size() == 0)
                this.sourceIdOwnerTables.addFirst(sourceTable);

                // This source idowner table is dependent on some other tables. Start at the end of
                // sourceIdOwnerTables and work backwards searching for tables that either a) have no
                // dependencies or b)depend on this table. Add this table to the list immediately
                // following the identified table.
            else {
                // start the iterator at the end ... this.sourceIdownerTable.size() as index
                ListIterator<Table> it = this.sourceIdOwnerTables.listIterator(this.sourceIdOwnerTables.size());
                while (it.hasPrevious()) {
                    // Table already in sourceIdOwnerTables list
                    Table table = it.previous();
                    // If ufKeyTables does not have table in it, then condition a) is satisfied;
                    // if the current source table depends on table (dependencies has table in it),
                    // the condition b) is satisfied - put sourceTable immediately after the table
                    // that met the condition
                    if (ufKeyTables.get(table).size() == 0 || dependencies.contains(table)) {
                        it.next();
                        break;
                    }
                }
                it.add(sourceTable);
            }
        }

        // All of the source idowner tables have been added to sourceIdOwnerTables. Now, check for
        // dependency conflicts. Note that this process will destroy ufKeyTables.
        for (ListIterator<Table> it = this.sourceIdOwnerTables.listIterator(); it.hasNext(); ) {
            Table sourceTable = it.next();
            // Find the set of tables that this source idowner table depends on.
            HashSet<Table> dependencies = ufKeyTables.get(sourceTable);

            // Start from the position before this table and iterate backwards toward the beginning
            // of sourceIdOwnerTables. For every table that is in sourceIdOwnerTables before the
            // current table, remove it from this table's dependency set.
            ListIterator<Table> pos = this.sourceIdOwnerTables.listIterator(it.nextIndex());
            while (pos.hasPrevious())
                dependencies.remove(pos.previous());

            // If the current table's dependency map is not empty, it means that the current table
            // has a dependency on a table that will not have been processed by the time the current
            // table gets processed. This is an unresolved dependency - also known as an error.
            if (dependencies.size() != 0) {
                StringBuilder msg = new StringBuilder("\n\nERROR in Merge constructor.  Source " + "table "
                        + sourceTable.getName() + " has an unresolved dependency on the " + "following table(s):\n\t");
                for (Table table : dependencies)
                    msg.append(table.getName() + " ");
                msg.append("\nSource IdOwner tables are set to be merged in the following order:\n");
                for (Table table : this.sourceIdOwnerTables)
                    msg.append(table.getName() + " ");
                msg.append("\n\n");
                throw new FatalDBUtilLibException(msg.toString());
            }
        }
    }

    /**
     * This method simply calls {@link #mergeRows(RowGraph, MergeStatistics, RemapTable, IDGapsTable)
     * mergeRows(rowGraph, new MergeStatistics(), this.targetSchema.getRemapTable(),
     * this.targetSchema.getIdGapsTable())}
     *
     * @param rowGraph the collection of related source rows to transform into Row objects that are members of the
     *                 target schema
     * @return the RowGraph of Row objects that have had their status determined regarding being merged into the target
     * schema; the returned rows belong to the target Schema, not the source Schema.
     * @throws FatalDBUtilLibException if an error occurs
     */
    public RowGraph mergeRows(RowGraph rowGraph) throws FatalDBUtilLibException {
        return mergeRows(rowGraph, new MergeStatistics(), this.targetSchema.getRemapTable(), this.targetSchema
                .getIdGapsTable());
    }

    /**
     * This method transforms a collection of Row objects from the source schema into a collection of Row objects that
     * are members of the target schema.
     * <p>
     * During the merge:
     * <p>
     * a) All foreign keys are remapped to ensure that primary key constraints are not violated in the target tables.
     * New ID values are obtained from an IDGaps table associated with the target schema. (If the IDGaps table is set to
     * null, then no merge will take place and data from the source will simply be inserted into the target.)
     * <p>
     * b) Information is added to the target schema's Remap table which remaps the old value of an ID (the value in the
     * source table) to its new value (the value in the target table).
     * <p>
     * c) Source rows are set to either have copies inserted into the target, to have their data update target data, or
     * to be ignored and not represented in the target in any way (typically because their data is already in the
     * target). Rows are copied from the source tables to the target tables only if the unique key information contained
     * in the source row does not already exist in the target tables.
     * <p>
     * <b>Important Note</b> The Row objects returned by this method have not been inserted into the target schema. It
     * is the responsibility of the calling routine to perform that function, if desired. (See {@link #execute execute}
     * for a method that executes mergeRows and then writes the data to the target schema.)
     * <p>
     * The RowGraph returned by this method has the same size as the input RowGraph. Each Row has a status variable that
     * will be set by this method to either <br>
     * {@link DBDefines#DROP DBDefines.DROP} <br>
     * {@link DBDefines#INSERT DBDefines.INSERT} <br>
     * {@link DBDefines#UPDATE DBDefines.UPDATE}
     * <p>
     * The calling routine should use this variable to act appropriately. <br>
     * status = {@link DBDefines#DROP DBDefines.DROP}: do nothing <br>
     * status = {@link DBDefines#INSERT DBDefines.INSERT}: use {@link Row#insertIntoDB Row.insertIntoDB()} <br>
     * status = {@link DBDefines#UPDATE DBDefines.UPDATE}: {@link Row#updateInDB Row.updateInDB()}
     * <p>
     * This method traverses the entire RowGraph four times.
     * <p>
     * 1st traversal: Visit all the idowner table rows. Add appropriate RemapTable information to the local remap table.
     * Set the row's status variable.
     * <p>
     * 2nd traversal: At this point, all source rows from idOwner tables have been examined and remap information for
     * their owned ids has been added to the remap table. No actual id values have been changed yet. Source rows from
     * non-idOwner tables are now considered. <br>
     * The target is NOT examined to see if it contains rows equal to source non-idowner rows if <br>
     * a) all of the idowner rows were merged <br>
     * b) all of the idowner rows from tables that the non-idowner row refers to were merged
     * <p>
     * 3rd traversal: Iterate through all of the rows to ensure that every rows' status is set. If the status is not
     * set, throw an exception. <br>
     * If the row's status is DBDefines.INSERT (this row is to be added to the target schema), then the remapTable will
     * be used to remap the IDs. If remapping is unsuccessful, (perhaps the row contains foreign key values that do not
     * have entries in the remap table), an error is generated. 4th traversal: Accumulate statistics
     *
     * @param rowGraph        the collection of related source rows to transform into Row objects that are members of the
     *                        target schema
     * @param mergeStatistics object to track statistics about this merge; no statistics will be accumulated if this is
     *                        null
     * @param remapTable      table to write remap information to
     * @param idGapsTable     table to get new id information from
     * @return the RowGraph of Row objects that have had their status determined regarding being merged into the target
     * schema; the returned rows belong to the target Schema, not the source Schema.
     * @throws FatalDBUtilLibException if an error occurs
     */
    public RowGraph mergeRows(RowGraph rowGraph, MergeStatistics mergeStatistics, RemapTable remapTable,
                              IDGapsTable idGapsTable) throws FatalDBUtilLibException {
        // Keep track of values obtained from the idgaps table in case an error occurs and they need to be restored
        // to the idgaps table
        HashMap<String, ArrayList<Long>> usedIdGapsValues = new HashMap<String, ArrayList<Long>>();

        try {
            if (rowGraph == null || rowGraph.size() == 0) {
                DBDefines.ERROR_LOG.add("ERROR in Merge.mergeRows().  There is no rowGraph to merge.");
                return new RowGraph();
            }
            // Set up some merging statistics information
            if (mergeStatistics != null)
                mergeStatistics.addRows(rowGraph);

            // Makes all rows histories (if they are turned on) the same length
            DBDefines.evenLength(rowGraph);

            // IDGapsTable targetIdGapsTable = this.targetSchema.getIdGapsTable();

            // If there are foreign keys in the target schema that are "fixed" (they will not be
            // remapped), then, due to how non-idowner table remapping is handled, those foreign key
            // columns need to be "fixed" in the source as well. (Since we do not want to permanently
            // change the user's source schema, this is undone later with undoFixSourceForeignKeys.)
            HashMap<Table, LinkedList<Column>> sourceFixedForeignKeys = fixSourceForeignKeys();

            // Merge idowner rows
            // (idsForRowsNotMerged tracks ids involved in decisions to NOT merge an idOwner row.)
            // HashSet<String> idsForRowsNotMerged = mergeIdOwnerRows(rowGraph, targetRemapTable, targetIdGapsTable);
            HashSet<String> idsForRowsNotMerged = mergeIdOwnerRows(rowGraph, remapTable, idGapsTable, usedIdGapsValues);

            // Merge non-idowner rows
            mergeNonIdOwnerRows(idsForRowsNotMerged, rowGraph, remapTable);

            // Check that every row's status is set.
            RowGraph rowsToBeMerged = checkAllRowsStatus(rowGraph, remapTable);

            // Accumulate statistics
            accumulateStatistics(rowsToBeMerged);

            // Undo the foreign key "fixing" that was done to the source with the call to
            // fixSourceForeignKeys above.
            undoFixSourceForeignKeys(sourceFixedForeignKeys);

            if (mergeStatistics != null)
                mergeStatistics.analyzeRows(rowGraph);
            return rowsToBeMerged;
        } catch (FatalDBUtilLibException e) {
            idGapsTable.returnUnusedValues(usedIdGapsValues);
            throw e;
        }
    }

    /**
     * If there are foreign keys in the target schema that are "fixed" (they will not be remapped), then, due to how
     * non-idowner table remapping is handled, those foreign key columns need to be "fixed" in the source schema as
     * well. This method calls {@link Column#setFixId Column.setFixId(true)} for each of the source schema columns that
     * have been "fixed" in the target schema. If a source column is already "fixed" in the source schema, this method
     * will not "re-fix". This is because this method returns information about what columns in the source had to be
     * fixed so that they can be reverted back to their normal state once merging is complete since we do not want to
     * permanently change the user's source schema.
     *
     * @return a map from a source table to a list of the columns in that source table that had to be fixed
     */
    private HashMap<Table, LinkedList<Column>> fixSourceForeignKeys() {
        // Source foreign keys that will be "fixed"
        HashMap<Table, LinkedList<Column>> sourceFixedForeignKeys = new HashMap<Table, LinkedList<Column>>();

        // Go through all of the target tables and their columns looking for columns that are fixed
        for (Table targetTable : this.targetSchema.getTables()) {
            Table sourceTable = this.sourceSchema.getTableOfType(targetTable.getTableType());
            for (Column targetCol : targetTable.getColumns()) {
                // Found a target column that is fixed
                if (targetCol.getFixedFK()) {
                    // Determine if the source table even has this column
                    Column sourceCol = sourceTable.getColumn(targetCol.getName());

                    // If the source table has this column and it's not already fixed in the source,
                    // fix it. Keep track of the fact that it was fixed so that can be undone later.
                    if (sourceCol != null && !sourceCol.getFixedFK()) {
                        sourceCol.setFixId(true);

                        // tracking ...
                        LinkedList<Column> fks = sourceFixedForeignKeys.get(sourceTable);
                        if (fks == null) {
                            fks = new LinkedList<Column>();
                            sourceFixedForeignKeys.put(sourceTable, fks);
                        }
                        fks.add(sourceCol);
                    }
                }
            }
        }
        return sourceFixedForeignKeys;
    }

    /**
     * Undo any changes to the source schema made by {@link #fixSourceForeignKeys fixSourceForeignKeys}.
     *
     * @param sourceFixedForeignKeys results from {@link #fixSourceForeignKeys fixSourceForeignKeys}.
     */
    private void undoFixSourceForeignKeys(HashMap<Table, LinkedList<Column>> sourceFixedForeignKeys) {
        for (Table sourceTable : sourceFixedForeignKeys.keySet()) {
            for (Column sourceCol : sourceFixedForeignKeys.get(sourceTable))
                sourceCol.setFixId(false);
        }
    }

    /**
     * This method visits all idowner table rows in rowGraph and determines if those rows should/can be merged into the
     * target. A row's status is set to indicate whether or not the row should be inserted into the target schema (as is
     * the case when there are no "equal" rows in the target) or if the target schema should be updated with information
     * from the source row. Remap information relating the source id from each of the source rows to the id they had in
     * the target table is added to remapTable
     *
     * @param rowGraph          the collection of related source rows to transform into Row objects that are members of the
     *                          target schema
     * @param remapTable        target schema's remap table
     * @param targetIdGapsTable target schema's id gaps table
     * @param usedIdGapsValues  HashMap from idname to values obtained from the idgaps table populated here in case we
     *                          need to return those idgaps values as part of handling an error
     * @throws FatalDBUtilLibException if an error occurs
     */
    private HashSet<String> mergeIdOwnerRows(RowGraph rowGraph, RemapTable remapTable, IDGapsTable targetIdGapsTable,
                                             HashMap<String, ArrayList<Long>> usedIdGapsValues) throws FatalDBUtilLibException {
        // idsForRowsNotMerged tracks what IDs were involved in a decision to NOT merge a row
        HashSet<String> idsForRowsNotMerged = new HashSet<String>();

        for (Table sourceTable : this.sourceIdOwnerTables) {
            String ownedIDColumnName = sourceTable.getOwnedID();
            String remapSource = sourceTable.getSchema().getRemapSource(ownedIDColumnName);
            Set<Row> sourceRows = rowGraph.getRowsOfType(sourceTable);

            // If there are no rows for this source table, add its ownedID column name to
            // idsForRowsNotMerged. When an idowner table has rows involved in a decision NOT to
            // merge a row, then non-idowner rows will be checked to make sure that they are not in
            // the target table before they are merged in. If all idowner rows merge all of their
            // rows, then non-idowner rows are not checked before they are merged into the target.
            // If an idowner table has no rows, then it's not really possible to say that it merged
            // all its rows since it didn't, and non-idowner rows that refer to it need to be checked
            // before they are added to the target.
            if (sourceRows.size() == 0)
                idsForRowsNotMerged.add(ownedIDColumnName);

            for (Row sourceRow : sourceRows) {
                // See if odd handling needs to be done ... if so, do it, and continue on
                boolean oddIdOwnerCases = handleOddIdOwnerCases(sourceRow, targetIdGapsTable, remapTable, remapSource);
                if (oddIdOwnerCases)
                    continue;

                // With the odd cases out of the way, we can now see if there is a row in the target
                // table that is "uniquely equal" to the current source row
                LinkedList<Row> targetRows = evaluateUniqueKeyEquality(sourceRow, remapTable);

                // get the value of the owned id in this row.
                Long ownedID = sourceRow.getValueOwnedID();
                // >1 target rows is an error - there can't be >1 unique things
                if (targetRows.size() > 1) {
                    StringBuilder error = new StringBuilder("ERROR in Merge.mergeIdOwnerRows().  "
                            + "While seeing if the target schema has a row equal to " + sourceRow + ", "
                            + targetRows.size() + " rows were determined to be equal to " + sourceRow
                            + ".  This indicates that the target schema has a unique key violation.\n"
                            + "Unique key equality relationship used:\n\t"
                            + this.uniqueKeyEquality.get(sourceRow.getTableName()) + "\nRows returned: ");
                    for (Row targetRow : targetRows)
                        error.append("\t" + targetRow.toString() + "\n");
                    error.append("Merging of rowGraph aborted.\n");
                    throw new FatalDBUtilLibException(error.toString());
                }
                // There is one row in the target table that enjoys unique key equality with the
                // row currently being considered for merging.
                else if (targetRows.size() == 1) {
                    Row targetRow = targetRows.getFirst();

                    // determine if this row should replace the target row that it is equal to.
                    // (This is not usually the case - usually if the target has a row equal to the
                    // source, then the target row is kept. Occasionally, the source row's
                    // information should replace the target row's information.) If the source row
                    // should update the target row, the source row's status is set accordingly.
                    replaceTargetIdOwnerRow(sourceRow, targetRow);

                    // Add remap information from the source row's ownedID to the target row's
                    // ownedID
                    remapTable.addCurrentId(remapSource, ownedIDColumnName, ownedID, targetRow.getValueOwnedID());

                    // record the fact that this ownedID was involved in a decision to NOT merge a
                    // row from an idowner table.
                    idsForRowsNotMerged.add(ownedIDColumnName);
                }

                // This is a new row that has not been seen before and there is no row in the target
                // tables that is "equal" it. This one must be merged, but the calling routine set
                // it's status to FIX_ID, indicating that it's id value should not be changed.
                // Set its status to insert and add remap table information
                else if (sourceRow.getStatus() == DBDefines.FIX_ID) {
                    remapTable.addCurrentId(remapSource, ownedIDColumnName, ownedID, ownedID);
                    sourceRow.setStatus(DBDefines.INSERT);
                    sourceRow.addToHistory(" 14");
                }

                // This is a new row that has not been seen before and there is no row in the target
                // tables that is "equal" it. This one must be merged, with a new id value.
                else {
                    // Get new id
                    Long targetValueFromIdGaps = targetIdGapsTable.getNextId(ownedIDColumnName);

                    if (usedIdGapsValues.get(ownedIDColumnName) == null)
                        usedIdGapsValues.put(ownedIDColumnName, new ArrayList<Long>());
                    usedIdGapsValues.get(ownedIDColumnName).add(targetValueFromIdGaps);

                    // add remap information
                    // (Note that later, when the remapTable is added to the global RemapTable,
                    // there may be more than one ownedID abc -> ??? entries (e.g. arid 123->345 and
                    // arid 123->678). Identical RemapTable entries, will not be introduced (such
                    // as OwnedID 123->345 if ownedID 123 -> 345 already exists), but different
                    // remap entries with the same original_id and different current_ids will be
                    // introduced (such as ownedID 123->345 if ownedID 123->678 already exists).
                    remapTable.addCurrentId(remapSource, ownedIDColumnName, ownedID, targetValueFromIdGaps);

                    sourceRow.setStatus(DBDefines.INSERT);
                    sourceRow.addToHistory(" 13");
                }
            }
        }
        return idsForRowsNotMerged;
    }

    /**
     * Handle the odd idowner cases. These include cases such as: <br>
     * When a target table is not in the target schema since it's in {@link #missingTargetTables missingTargetTables} <br>
     * When the calling routine indicated that a new ownedID value should be retrieved without testing anything <br>
     * When the source table has a compound primary key that's been remapped already
     *
     * @param sourceRow         source row that is being assessed to see if it should be inserted into the target or not
     * @param targetIdGapsTable target idgaps table for retrieving new ids
     * @param remapTable        target remap table that remap information is being read from and written to
     * @param remapSource       value for the source column in the remapTable
     * @return true if an odd idowner case was encountered and handled; false otherwise
     */
    private boolean handleOddIdOwnerCases(Row sourceRow, IDGapsTable targetIdGapsTable, RemapTable remapTable,
                                          String remapSource) {
        Table sourceTable = sourceRow.getTable();
        String ownedIDColumnName = sourceTable.getOwnedID();
        Long ownedID = sourceRow.getValueOwnedID();

        // If this source table is specified as a "missing table", then tables in the target
        // schema will refer to this source schema table. Add remap table information for
        // the ownedids and continue on since nothing else needs to be done.
        if (this.missingTargetTables.contains(sourceTable)) {
            // It won't be added to the target schema since there's no corresponding target table.
            remapTable.addCurrentId(remapSource, ownedIDColumnName, ownedID, ownedID);
            sourceRow.setStatus(DBDefines.DROP);
            sourceRow.addToHistory(" 17");
            return true;
        }

        // Determine if this id has already been remapped (has an entry in the remap table)
        Long targetValueFromRemap = remapTable.getCurrentId(remapSource, ownedIDColumnName, ownedID);

        // The calling routine said to get a new ownedID value for this row without
        // testing anything.
        if (((sourceRow.getStatus() & DBDefines.FORCE_NEW_ID) != 0)) {
            Long targetValueFromIdGaps = targetIdGapsTable.getNextId(ownedIDColumnName);

            // add remap entry to the remap table
            remapTable.addCurrentId(remapSource, ownedIDColumnName, ownedID, targetValueFromIdGaps);
            // set mergeRow = yes
            sourceRow.setStatus(DBDefines.INSERT);
            sourceRow.addToHistory(" 15");
            return true;
        }

        // If this is a row from an idOwner table that has a compound primary key
        // (example: remark table has primary keys [commid, lineno]), and its ownedID has
        // already been remapped, then insert it and continue on.
        else if (sourceTable.hasCompoundPrimaryKey() && targetValueFromRemap != null) {
            sourceRow.setStatus(DBDefines.INSERT);
            sourceRow.addToHistory(" 16");
            return true;
        }
        return false;
    }

    /**
     * Determine if the target row should be replaced with the source row. Either
     * <p>
     * a) the calling routine set a flag specifying that this source row must UPDATE a target row with which it enjoys
     * unique key equality, or <br>
     * b) the source row has higher priority than the target row so values in target row will be replaced with values
     * from source row. The ownedID value will not be changed by the update but all other values could potentially be
     * changed. (Remember: a low priority value indicates high priority. A priority of Integer.MIN_VALUE has infinitely
     * high priority.)
     *
     * @param sourceRow source row whose information might need to be used to update targetRow
     * @param targetRow target row whose information might need to be updated from sourceRow
     */
    private void replaceTargetIdOwnerRow(Row sourceRow, Row targetRow) {
        if ((sourceRow.getStatus() & DBDefines.FORCE_UPDATE) != 0) {
            sourceRow.setStatusUpdate(targetRow);
            sourceRow.addToHistory(" 12");
        } else if (this.priority != null
                && (this.priority.getPriority(sourceRow) == Integer.MIN_VALUE || this.priority.getPriority(sourceRow) < this.priority
                .getPriority(targetRow))) {
            sourceRow.setStatusUpdate(targetRow);
            sourceRow.addToHistory(" 12");
        }
        // source row's priority is less than target row's, so threre's no need to merge this row.
        else {
            sourceRow.setStatus(DBDefines.DROP);
            sourceRow.addToHistory(" 11");
        }
    }

    /**
     * This method visits all non-idowner table rows in rowGraph and determines if those rows should/can be merged into
     * the target.
     * <p>
     * The target is not examined to see if it contains rows equal to source non-idowner rows if <br>
     * a) all of the idowner rows were merged <br>
     * b) all of the idowner rows from tables that the non-idowner row refers to were merged.
     *
     * @param idsForRowsNotMerged set of the ids for idowner rows that were not merged
     * @param rowGraph            rowGraph containing the source rows to be merged
     * @throws FatalDBUtilLibException if an error occurs
     */
    private void mergeNonIdOwnerRows(HashSet<String> idsForRowsNotMerged, RowGraph rowGraph, RemapTable remapTable)
            throws FatalDBUtilLibException {
        // If idsForRowsNotMerged is empty, it means that all the rows in all of the idowner rows
        // are going to be merged into target tables. (idsForRowsNotMerged contains IDs used in a
        // decision to NOT merge a row, so if it's empty, no decisions were made to not merge a row
        // and all rows were merged.) This means that all non-idowner rows will be merged as well
        // and there is no need to check the target tables for rows that are in any way "equal" to
        // non-idOwner rows in the current rowGraph.
        if (idsForRowsNotMerged.size() == 0) {
            allIdOwnerRowsMerged(rowGraph);
            return;
        }

        // Create a HashSet containing the names of all the non-idowner tables that have all of
        // their foreign keys as members of idsForRowsNotMerged. When all of a non-idowner table's
        // foreign keys refer to ids for tables whose rows were no merged, then that non-idowner row
        // needs to be further examined in order to determine what to do with it.
        HashSet<String> tablesThatNeedToBeAnalyzed = new HashSet<String>();

        // iterate over all the non-idowner tables in the source schema.
        for (Table table : this.sourceNonIdOwnerTables) {
            // Iterate over the foreign keys that this non-idowner table contains. If all of the
            // foreign keys are members of idsForRowsNotMerged, then rows from this table need to
            // have their status determined -> add the name of the table to the set of table names
            // that need to be analyzed.

            Column[] columns = table.getColumns();
            HashSet<String> foreignKeyColumns = new HashSet<String>();
            for (int fk : table.getIdLinks())
                foreignKeyColumns.add(columns[fk].getForeignKey());

            if (idsForRowsNotMerged.containsAll(foreignKeyColumns))
                tablesThatNeedToBeAnalyzed.add(table.getName());
        }

        // Traverse the RowGraph again and check the rows from non-idowner tables to see if there
        // are rows in the target table that are "equal".
        for (Table table : this.sourceNonIdOwnerTables) {
            for (Row row : rowGraph.getRowsOfType(table.getTableType())) {
                // See if odd handling needs to be done ... if so, do it, and continue on
                boolean oddNonIdOwnerCases = handleOddNonIdOwnerCases(row);
                if (oddNonIdOwnerCases)
                    continue;

                // At least one of the foreign keys that this non-idOwner row contains is owned by
                // an IDOwner table that always merged its rows (the ID is not present in
                // idsForRowsNotMerged). Therefore, this row has to be merged.
                if (!tablesThatNeedToBeAnalyzed.contains(table.getName())) {
                    boolean nonIdOwnerRowMerged = insertNonIdOwnerRowNoCheck(row, rowGraph);
                    if (nonIdOwnerRowMerged)
                        continue;
                }

                // At this point, this row comes from a non-idOwner table AND it wasn't a weird case
                // AND all of its foreign keys were previously involved in a decision to not merge
                // an idOwner row. We must Remap all its foreign keys using remapTable and
                // then check the associated target table to see if the target table contains any
                // rows that have 'equal' foreign keys, and 'equal' unique fields.

                // Create a copy of row with all its foreign keys remapped using remapTable
                Row tempRow = null;
                try {
                    tempRow = new Row(row, remapTable);
                } catch (Row.DanglingForeignKeyPointerException e) {

                    String errorMessage = processDanglingForeignKeyError(e, rowGraph);
                    throw new FatalDBUtilLibException(errorMessage);
                } catch (FatalDBUtilLibException e) {
                    throw new FatalDBUtilLibException("Error in Merge.mergeNonIdOwnerRows"
                            + " when creating a new row from: " + row + " using a remap table.\nException"
                            + " message: " + e.getMessage());
                }

                // Check to see if any rows in the target table have the same foreign keys (with the
                // same values) as tempRow. (Looking for the same value since tempRow has been
                // remapped).
                LinkedList<Row> targetRows = evaluateForeignKeyEquality(tempRow);

                // There are no rows in the target table that have foreign keys that have the same
                // values as this row. Merge this row.
                if (targetRows.size() == 0) {
                    row.setStatus(DBDefines.INSERT);
                    row.addToHistory(" 22");
                }

                // If there are equal target rows, but the calling method has designated that this
                // row should be updated no matter what - update it!
                else if (row.getStatus(DBDefines.FORCE_UPDATE)) {
                    row.setStatusUpdate(targetRows.getFirst());
                    row.addToHistory(" 29");
                }

                // One or more rows were found in the target table that have the same foreign keys
                // and, since the uniqueKeyTest will return the same rows as the foreignKey test,
                // then the unique keys are equal as well. No need to insert this row.
                else if (this.identicalEqualityTests.get(tempRow.getTable().getName()).booleanValue()) {
                    row.setStatus(DBDefines.DROP);
                    row.addToHistory(" 23");
                }

                // One or more rows were found in the target table that have the same foreign keys
                // as tempRow. See if any of those rows also enjoy "unique key equality" with
                // tempRow. This involves executing a select statement against the associated
                // target table. If the target table is large, this might be expensive. It may be
                // extremely expensive since some of the fields involved may not be indexed. Much
                // of the code complexity above (idsForRowsNotMerged, tablesThatNeedToBeAnalyzed,
                // etc.) was motivated by the desire to avoid the potentially significant expense
                // associated with this statement.
                else {
                    targetRows = evaluateUniqueKeyEquality(tempRow, remapTable);

                    // Foreign and unique keys are equal. Do not merge; no negative consequences.
                    if (targetRows.size() > 0) {
                        row.setStatus(DBDefines.DROP);
                        row.addToHistory(" 24");
                    }
                    // There is a target row in the target table with the same foreign key values as
                    // tempRow, but with different unique keys. Insert.
                    else if (targetRows.size() == 0) {
                        row.setStatus(DBDefines.INSERT);
                        row.addToHistory(" 27");
                    }
                }
            }
        }
    }

    /**
     * It has been determined that all of the idowner rows always merged all of their rows. Thus, the non-idowner rows
     * can be merged in without checking anything.
     *
     * @param rowGraph RowGraph containing the non-idowner rows
     */
    private void allIdOwnerRowsMerged(RowGraph rowGraph) {
        // Keep the log files from being pumped full of "POTENTIAL ISSUE" text by only displaying it
        // once for each time this method is called
        boolean potentialIssueStarted = false;

        // Only look at non-idowner rows
        for (Table table : this.sourceNonIdOwnerTables) {
            for (Row row : rowGraph.getRowsOfType(table.getTableType())) {
                // This source row belongs to a non-idowner table that is a "missing table"; there
                // is no corresponding table of the same type in the target. Drop this row.
                if (this.missingTargetTables.contains(table)) {
                    row.setStatus(DBDefines.DROP);
                    row.addToHistory(" 28");
                } else {
                    row.setStatus(DBDefines.INSERT);
                    row.addToHistory(" 20");
                    // When setting a row's status to INSERT without checking to see if the
                    // target has a matching row, there's the potential for primary/unique
                    // key violations.
                    if (!potentialIssueStarted) {
                        DBDefines.STATUS_LOG.add("\nPOTENTIAL ISSUE -- rows that will be inserted "
                                + "without checking to see if an \"equal\" row exists\nin the target "
                                + "database since all of the idowner rows in the RowGraph will be inserted:\n");
                        potentialIssueStarted = true;
                    }
                    DBDefines.STATUS_LOG.add(row.toString());
                }
            }
        }
    }

    /**
     * Handle the odd non-idowner cases. These include cases such as: <br>
     * When a row belongs to a different schema <br>
     * When a target table is not in the target schema since it's in {@link #missingTargetTables missingTargetTables} <br>
     * When a row has had its status preset to {@link DBDefines#INSERT DBDefines.INSERT}
     *
     * @param row source row that merge is assessing to see if t should be inserted into the target or not
     * @return true if an odd case was encountered and handled; false otherwise
     */
    private boolean handleOddNonIdOwnerCases(Row row) {
        Table table = row.getTable();
        // skip rows that are not members of sourceSchema;
        if (table.getSchema() != this.sourceSchema)
            return true;
            // This row comes from a table that is a member of 'missing tables'. Drop this row since
            // there is no target table for it to go into.
        else if (this.missingTargetTables.contains(table)) {
            row.setStatus(DBDefines.DROP);
            row.addToHistory(" 28");
            return true;
        }
        // once it has been decided to merge a non-idOwner row, the decision will never be reversed
        // so no need to revisit.
        else if (row.getStatus() == DBDefines.INSERT)
            return true;
        else
            return false;
    }

    /**
     * Keep track of whether or not "POTENTIAL ISSUE" messages have been issued already so that each subsequent message
     * doesn't have to re-issue all the background text.
     */
    private boolean nonIdownerNoCheckPotentialIssue = false;

    /**
     * This method handles the case where non-idowner rows are set to be inserted without checking to see if equal rows
     * exist in the target since there was a condition with the idowner rows these rows refer to that has merited them
     * being inserted without checking.
     *
     * @param row      row to be inserted
     * @param rowGraph RowGraph row is a member of
     * @return true if the the non-idowner row should be inserted without checking (the method just checks that if the
     * row is being inserted because of a situation with the idowner rows it references that it does indeed actually
     * reference some idowner rows); false otherwise
     */
    private boolean insertNonIdOwnerRowNoCheck(Row row, RowGraph rowGraph) {
        // When setting a row's status to INSERT without checking to see if the target has a
        // matching row, there's the potential for primary/unique key violations. Figure out if
        // this row has parents/children that are being inserted, and let the user know about those
        // in case errors occur when inserting the non-idowner row.
        HashSet<Row> rowsToBeInserted = new HashSet<Row>();
        for (Row parent : rowGraph.getParents(row))
            if (parent.getStatus() == DBDefines.INSERT)
                rowsToBeInserted.add(parent);
        for (Row child : rowGraph.getChildren(row))
            if (child.getStatus() == DBDefines.INSERT)
                rowsToBeInserted.add(child);

        if (rowsToBeInserted.size() != 0) {
            if (!this.nonIdownerNoCheckPotentialIssue) {
                DBDefines.STATUS_LOG.add("\nPOTENTIAL ISSUE -- rows that will be inserted without "
                        + "checking to see if an \"equal\" row exists in the target database since one "
                        + "of this row's\nforeign keys points to an idowner row that WILL be inserted."
                        + "Thus, the row must be inserted to preserve that foreign key relationship " + "information.");
                DBDefines.STATUS_LOG.add(String
                        .format("%-50s%-50s", "Row:", "Related rows that " + "WILL be inserted:"));
                this.nonIdownerNoCheckPotentialIssue = true;
            }

            DBDefines.STATUS_LOG.add(String.format("%-50s", row.toString())
                    + (rowsToBeInserted.size() > 0 ? rowsToBeInserted : "[none]"));

            row.setStatus(DBDefines.INSERT);
            if (row.history != null)
                row.history.append(" 21");
            return true;
        }
        return false;
    }

    /**
     * This method simply checks that every row in rowGraph has its status set.
     *
     * @param rowGraph   RowGraph with rows to be checked
     * @param remapTable target remap table generated when preparing rowGraph for being merged into the target schema
     * @return RowGraph of rows that are ready for processing (performing the actual merge into the target)
     * @throws FatalDBUtilLibException if an error (such as a row's status not being set) occurs
     */
    private RowGraph checkAllRowsStatus(RowGraph rowGraph, RemapTable remapTable) throws FatalDBUtilLibException {
        RowGraph rowsToBeMerged = new RowGraph();

        // If the row's status is DBDefines.DROP (this row is not to be added to the target schema),
        // remove the row from the list.
        // If the row's status is DBDefines.INSERT, (this row is to be added to the target schema),
        // then apply the remapTable to all of its IDs (remap them). If applying the
        // RemapTable is unsuccessful (like when the row contains foreign key values that do not
        // have entries in remapTable), throw an exception.
        for (Row row : rowGraph) {
            Table table = row.getTable();

            // No processing of row necessary - row not from this schema.
            if (table.getSchema() != this.sourceSchema || row.getStatus() == DBDefines.DROP)
                rowsToBeMerged.add(row);

                // Remap all the foreign keys in this row.
            else if (row.getStatus() == DBDefines.UPDATE || row.getStatus() == DBDefines.INSERT) {
                Row newRow = null;
                try {
                    newRow = new Row(row, getTargetTable(table.getName()), remapTable);
                } catch (Row.DanglingForeignKeyPointerException e) {
                    String errorMessage = processDanglingForeignKeyError(e, rowGraph);
                    throw new FatalDBUtilLibException(errorMessage);
                } catch (FatalDBUtilLibException e) {
                    throw new FatalDBUtilLibException("Error in Merge.checkAllRowsStatus()"
                            + " when creating a new target row from row: " + row + " from table: " + table.getName()
                            + " when using a remap table.\n" + "Exception message: " + e.getMessage());
                }

                if (newRow.valid())
                    rowsToBeMerged.add(newRow);
                else
                    throw new FatalDBUtilLibException("ERROR in Merge.mergeRow()." + "  Remapping of Row "
                            + row.toString() + " failed.\nMerging of rowGraph" + " aborted.\n");
            }

            // Unknown fate!
            else
                throw new FatalDBUtilLibException("ERROR in Merge.mergeRow().  The fate" + " of Row " + row.toString()
                        + " still undecided in third traversal of rowGraph.");
        }
        return rowsToBeMerged;
    }

    /**
     * This method accumulates statistics about what happened to each row during its time with Merge.
     *
     * @param rowsToBeMerged rows to accumulate statistics about
     */
    private void accumulateStatistics(RowGraph rowsToBeMerged) {
        if (this.recordRowHistories)
            return;

        for (Row row : rowsToBeMerged) {
            if (row.getStatus() == DBDefines.INSERT)
                row.addToHistory(" inserted as ");
            else if (row.getStatus() == DBDefines.UPDATE)
                row.addToHistory(" updating    ");
            else if (row.getStatus() == DBDefines.DELETE)
                row.addToHistory(" deleted.    ");
            else if (row.getStatus() == DBDefines.DROP)
                row.addToHistory(" dropped.    ");
            else
                row.addToHistory(" fate unresolved.");

            if (row.getStatus() == DBDefines.UNDETERMINED) {
                DBDefines.STATUS_LOG.add("ERROR in Merge.mergeRow().  Row " + row.toString() + "'s"
                        + " status is undetermined");
                System.out.println(row + " " + row.getStatusString());
            }
        }
    }

    /**
     * This method simply calls {@link #execute(RowGraph, MergeStatistics, UndoSqlTable) execute(rowGraph, new
     * MergeStatistics(), new UndoSqlTable(this.targetSchema, this.ldDate))}
     *
     * @throws FatalDBUtilLibException if an error occurs
     */
    public void execute(RowGraph rowGraph) throws FatalDBUtilLibException {
        execute(rowGraph, new MergeStatistics(), new UndoSqlTable(this.targetSchema, this.ldDate));
    }

    /**
     * This method calls
     * {@link #execute(RowGraph, MergeStatistics, boolean, RemapTable, IDGapsTable, UndoSqlTable, boolean)
     * execute(rowGraph, mergeStatistics, false, targetSchema.getRemapTable, targetSchema.getIdGapsTable,
     * targetSchema.getTableOfType("undosql"), false)}
     *
     * @param rowGraph        the RowGraph containing the source rows to be merged into the database
     * @param mergeStatistics object to track statistics about this merge; no statistics will be accumulated if this is
     *                        null
     * @param undoSqlTable    table where statements to undo this merge will be written
     * @throws FatalDBUtilLibException if an error occurs
     */
    public void execute(RowGraph rowGraph, MergeStatistics mergeStatistics, UndoSqlTable undoSqlTable)
            throws FatalDBUtilLibException {
        execute(rowGraph, mergeStatistics, false, this.targetSchema.getRemapTable(),
                this.targetSchema.getIdGapsTable(), undoSqlTable, false);
    }

    /**
     * This method encapsulates calling {@link #mergeRows mergeRows} and then traversing the returned RowGraph and
     * performing the correct action on each row based on the row's status.
     * <p>
     * Each Row has a status variable that will is set by {@link #mergeRows mergeRows} to either <br>
     * {@link DBDefines#DROP DBDefines.DROP} <br>
     * {@link DBDefines#INSERT DBDefines.INSERT} <br>
     * {@link DBDefines#UPDATE DBDefines.UPDATE}
     * <p>
     * This method uses this status variable to act appropriately: <br>
     * status = {@link DBDefines#DROP DBDefines.DROP}: do nothing <br>
     * status = {@link DBDefines#INSERT DBDefines.INSERT}: call {@link Row#insertIntoDB Row.insertIntoDB()} <br>
     * status = {@link DBDefines#UPDATE DBDefines.UPDATE}: call {@link Row#updateInDB Row.updateInDB()}
     * <p>
     * <i>Note: If the targetIdGapsTable is set to null, this is the user's way of indicating that no merge is to take
     * place - all of the rows should simply be inserted.</i>
     * <h3>IMPORTANT NOTE! If no errors are generated and commit is true, the changes to the database are committed. If
     * errors are generated, the changes to the database are rolled back.</h3> <i>Note on {@link UndoSqlTable
     * UndoSqlTables}:</i> <br>
     * It is possible to reverse the changes Merge makes to the database through the use of an {@link UndoSqlTable
     * UndoSqlTable}. An undosql table contains sql statements to undo what the merge does. When executed from highest
     * to lowest id, the statements revert the database to the state it was in before merge was called. To utilize this
     * capability, an {@link UndoSqlTable UndoSqlTable} must be included in the target schema's tables.
     * <p>
     *
     * @param rowGraph        the RowGraph containing the source rows to be merged into the database
     * @param mergeStatistics object to track statistics about this merge; no statistics will be accumulated if this is
     *                        null
     * @param commit          whether or not to commit the results when done
     * @param remapTable      where mappings that should be used during the merge will be read from and new mappings needed
     *                        during the merge will be written to
     * @param idGapsTable     table to get new id values from
     * @param undoSqlTable    table to write undo sql statements to
     * @param threaded        whether or not the call to this method if from a threaded environment
     * @throws FatalDBUtilLibException if an error occurs
     */
    public void execute(RowGraph rowGraph, MergeStatistics mergeStatistics, boolean commit, RemapTable remapTable,
                        IDGapsTable idGapsTable, UndoSqlTable undoSqlTable, boolean threaded) throws FatalDBUtilLibException {
        // Initialize the undoSqlTable -- this is only done here and not in other merge methods since
        // this is the only method that actually performs the modifications to the target
        initializeUndoSqlTable(undoSqlTable);

        Connection connection = null;
        try {
            if (threaded)
                connection = ((DAODatabase) this.targetSchema.getDAO()).createConnectionForThreads();

            // Initialize row histories if the user wants them
            if (this.recordRowHistories) {
                for (Row row : rowGraph)
                    row.history = new StringBuffer(row.toString());
                DBDefines.evenLength(rowGraph);
            }

            // If idGaps is not null, merging occurs
            if (this.targetSchema.getIdGapsTable() != null)
                performMerge(rowGraph, mergeStatistics, remapTable, idGapsTable, undoSqlTable, connection);
                // If idgaps is null, rows just get inserted into the target table without renumbering ids
                // (no merging)
            else
                performInserts(rowGraph, connection, undoSqlTable);

            if (commit)
                if (threaded && connection != null)
                    connection.commit();
                else
                    this.targetSchema.getDAO().commit();
        } catch (Exception e) {
            try {
                if (threaded && connection != null)
                    connection.rollback();
                else
                    this.targetSchema.getDAO().rollback();
            } catch (Exception ex) {
                throw new FatalDBUtilLibException("Error in Merge.execute.\n" + ex.getMessage());
            }
            throw new FatalDBUtilLibException("Error in Merge.execute.\n" + e.getMessage());
        } finally {
            try {
                if (threaded)
                    ((DAODatabase) this.targetSchema.getDAO()).closeConnectionForThreads(connection);
            } catch (SQLException e) {
                throw new FatalDBUtilLibException("Error in Merge.execute.\n" + e.getMessage());
            }
        }
    }

    /**
     * This method is called by
     * {@link #execute(RowGraph, MergeStatistics, boolean, RemapTable, IDGapsTable, UndoSqlTable, boolean) execute} to
     * make the call to {@link #mergeRows mergeRows}, and then perform the appropriate action for each of the rows in
     * the returned RowGraph.
     * <p>
     * Each Row has a status variable that will is set by {@link #mergeRows mergeRows} to either <br>
     * {@link DBDefines#DROP DBDefines.DROP} <br>
     * {@link DBDefines#INSERT DBDefines.INSERT} <br>
     * {@link DBDefines#UPDATE DBDefines.UPDATE}
     * <p>
     * This method uses this variable to act appropriately. <br>
     * status = {@link DBDefines#DROP DBDefines.DROP}: do nothing <br>
     * status = {@link DBDefines#INSERT DBDefines.INSERT}: call {@link Row#insertIntoDB Row.insertIntoDB()} <br>
     * status = {@link DBDefines#UPDATE DBDefines.UPDATE}: call {@link Row#updateInDB Row.updateInDB()}
     *
     * @param rowGraph        RowGraph with data to be merged into the target schema
     * @param mergeStatistics object object to track statistics about this merge; no statistics will be accumulated if
     *                        this is null
     * @param remapTable      the remapTable to add remap information to
     * @param idGapsTable     idgaps table to get new ids from
     * @param undoSqlTable    table to write undoSql statements to
     * @param connection      database connection to use; if null, the default will be used
     * @return whether or not the merge was successful
     * @throws FatalDBUtilLibException if an error occurs
     */
    private boolean performMerge(RowGraph rowGraph, MergeStatistics mergeStatistics, RemapTable remapTable,
                                 IDGapsTable idGapsTable, UndoSqlTable undoSqlTable, Connection connection) throws FatalDBUtilLibException {
        // Perform merging - this is done in memory and the status of what should happen to each row
        // is set in the row's status
        RowGraph targetRows = mergeRows(rowGraph, mergeStatistics, remapTable, idGapsTable);
        if (targetRows == null)
            return false;

        // Keep track of id values for rows that could not be inserted so they can be returned to the idgaps table for
        // future use
        HashMap<String, ArrayList<Long>> usedIdGapsValues = new HashMap<String, ArrayList<Long>>();

        // If a row causes an error in the following try block, we will need a reference to the row
        // in the catch block
        Row errorRow = null;

        // Now that merging is done, process all of the rows in targetRows based on each row's status
        try {
            // Output the history before anything is done so that errors related to decisions made
            // to the source as a whole are easier to trace when you have the whole RowGraph history
            if (this.recordRowHistories) {
                DBDefines.STATUS_LOG.add("Plan for the rows in the current RowGraph\n");
                for (Row row : targetRows)
                    DBDefines.STATUS_LOG.add(row.getHistory());
            }

            for (Row row : targetRows) {
                // Ignore rows not in the target schema
                if (row.getSchema() != this.targetSchema)
                    continue;

                // Save a reference to this row in case it is needed in the catch block.
                errorRow = row;

                // Only change the lddate to the current date/time if fixLDDate is false.
                if (!this.fixLdDate)
                    row.setLDDATE(this.ldDate);

                // INSERT
                if (row.getStatus() == DBDefines.INSERT) {
                    row.addToHistory(row.toString());
                    boolean inserted;
                    if (connection == null)
                        inserted = row.insertIntoDB();
                    else
                        inserted = row.insertIntoDB(connection);
                    if (!inserted) {
                        String mergeStatsTableName = this.sourceSchema.getTableOfType(row.getTableType()).getName();
                        Statistics stats = mergeStatistics.getStatisticsMap().get(mergeStatsTableName);
                        stats.inserted--;
                        stats.dropped++;
                        String ownedId = row.getTable().getOwnedID();
                        if (ownedId != null) {
                            if (usedIdGapsValues.get(ownedId) == null)
                                usedIdGapsValues.put(ownedId, new ArrayList<Long>());
                            usedIdGapsValues.get(ownedId).add(row.getValueOwnedID());
                        }
                    }

                    // If lddate is fixed, then undo statements cannot be based on the current
                    // lddate. Individual undo statements are needed for each row inserted.
                    if (this.fixLdDate)
                        undoSqlTable.addSqlStatement(row.undoInsert());
                }

                // UPDATE
                else if (row.getStatus() == DBDefines.UPDATE) {
                    row.addToHistory(row.toString());
                    if (connection == null)
                        row.updateInDB();
                    else
                        row.updateInDB(connection);
                    // add an undoSql statement that will restore the original row.
                    undoSqlTable.addSqlStatement(row);
                }

                // DELETE
                else if (row.getStatus() == DBDefines.DELETE) {
                    if (connection == null)
                        row.deleteFromDB();
                    else
                        row.deleteFromDB(connection);
                    // add an undoSql statement that will restore the original row.
                    undoSqlTable.addSqlStatement(row);
                }

                // ??
                else if (row.getStatus() != DBDefines.DROP)
                    DBDefines.ERROR_LOG.add("ERROR in Merge.performMerge().  Status of target row " + row
                            + " is not one of INSERT, UPDATE, DROP, DELETE.\n");
            }
        } catch (FatalDBUtilLibException e) {
            StringBuilder msg = new StringBuilder("ERROR in Merge.performMerge().  ");
            if (errorRow == null)
                msg.append("Output Row is null.  ");
            else if (errorRow.getSchema() == this.targetSchema)
                if (errorRow.getStatus() == DBDefines.INSERT)
                    msg.append("Unable to insert row: " + errorRow);
                else if (errorRow.getStatus() == DBDefines.UPDATE)
                    msg.append("Unable to update row: " + errorRow);
                else if (errorRow.getStatus() == DBDefines.DELETE)
                    msg.append("Unable to delete row: " + errorRow);
                else if (errorRow.getStatus() != DBDefines.DROP)
                    msg.append("Status of targetRow " + errorRow + " is not one of INSERT, UPDATE," + " DROP, DELETE.");
                else
                    msg.append("Unidentified error processing row " + errorRow);
            msg.append('\n').append(e.getMessage());
            throw new FatalDBUtilLibException(msg.toString());
        } finally {
            if (usedIdGapsValues.size() > 0)
                idGapsTable.returnUnusedValues(usedIdGapsValues);
        }
        return true;
    }

    /**
     * This method is called by
     * {@link #execute(RowGraph, MergeStatistics, boolean, RemapTable, IDGapsTable, UndoSqlTable, boolean) execute} when
     * the idgaps table in the targetSchema is null. This indicates that no merging is to take place, but that rows are
     * to simply be inserted into the database without checking for the existence of equal rows in the target schema.
     * This, obviously, can cause all sorts of key violations to be generated.
     *
     * @param rowGraph     RowGraph of rows to be inserted into the target schema
     * @param connection   connection to use to perform the inserts; if null, the default is used
     * @param undoSqlTable table to write undo statements to
     * @throws FatalDBUtilLibException if an error occurs
     */
    private void performInserts(RowGraph rowGraph, Connection connection, UndoSqlTable undoSqlTable)
            throws FatalDBUtilLibException {
        // Create target rows for each row in the source rowgraph and insert them.
        for (Row sourceRow : rowGraph) {
            Table targetTable = this.targetSchema.getTableOfType(sourceRow.getTableType());
            if (targetTable != null) {
                Row targetRow = null;
                boolean inserted = false;
                try {
                    targetRow = new Row(sourceRow, targetTable);
                    if (!this.fixLdDate)
                        targetRow.setLDDATE(this.ldDate);
                    if (connection == null)
                        inserted = targetRow.insertIntoDB();
                    else
                        inserted = targetRow.insertIntoDB(connection);

                    // If lddate is fixed, then cannot base undo statements on the current lddate.
                    // Have to put individual undo statements for each row inserted.
                    if (this.fixLdDate)
                        undoSqlTable.addSqlStatement(targetRow.undoInsert());
                } catch (FatalDBUtilLibException e) {
                    throw new FatalDBUtilLibException("ERROR in Merge.performInserts."
                            + "  Unable to create or insert target row: " + targetRow + ".\nError" + " message: "
                            + e.getMessage());
                }

                // Update row histories
                if (this.recordRowHistories) {
                    if (inserted)
                        targetRow.history.append("  ->  " + targetRow);
                    else
                        targetRow.history.append("  ->  dropped.");
                    DBDefines.STATUS_LOG.add(targetRow.history.toString());
                }

                if (inserted)
                    sourceRow.setStatus(DBDefines.INSERT);
                else
                    sourceRow.setStatus(DBDefines.DROP);
            } else if (this.recordRowHistories)
                DBDefines.STATUS_LOG.add(sourceRow + " not written to output because target schema"
                        + " does not contain a table of type " + sourceRow.getTableType());
        }
    }

    /**
     * Initialize the specified undoSqlTable
     *
     * @param undoSqlTable undoSqlTable to initialize
     */
    private void initializeUndoSqlTable(UndoSqlTable undoSqlTable) {
        if (!undoSqlTable.isActive())
            return;

        // Create some initial undosql table entries

        // Initialize a string that will be used to create undo SQL statements that will delete
        // rows from target tables that were inserted into the target table during the merge.
        // These strings will be added to the undo script.
        String tableString = "#table#";
        String deleteStatement = "DELETE FROM " + tableString + " WHERE LDDATE = TO_DATE('"
                + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(this.ldDate) + "','yyyy-mm-dd hh24:mi:ss');";

        // Add entries to the undoSqlTable to restore the IDGaps table to its pre-merge condition
        if (this.targetSchema.getIdGapsTable() != null)
            undoSqlTable.addSqlStatement(this.targetSchema.getIdGapsTable().getUndoLog());

        // Add a line to the undoSqlTable to delete all entries from the database remap table
        // that have lddate == this run.
        if (this.targetSchema.getRemapTableName().length() > 0) {
            String deleteRemapStatement = deleteStatement.replace(tableString, this.targetSchema.getRemapTableName());
            undoSqlTable.addSqlStatement(deleteRemapStatement);
        }

        // Add an entry to delete all new rows added to target tables based on lddate
        for (Table table : this.targetSchema.getTables()) {
            String deleteTableStatement = deleteStatement.replace(tableString, table.getName());
            undoSqlTable.addSqlStatement(deleteTableStatement);
        }
    }

    /**
     * Execute the select statement managed by a {@link Relationship Relationship object}. This method is called by
     * {@link #evaluateForeignKeyEquality evaluateForeignKeyEquality} and {@link #evaluateUniqueKeyEquality
     * evaluateUniqueKeyEquality}.
     *
     * @param relationship the Relationship object containing the select statement to be executed
     * @param row          row that contains the values to provide actual data to the relationship's where clause for the select
     *                     statement to be executed
     * @return list of Row objects that resulted from execution of the Relationship object
     * @throws FatalDBUtilLibException if an error occurs
     */
    private LinkedList<Row> evaluateEquality(Relationship relationship, Row row) throws FatalDBUtilLibException {
        // No relationship, no rows
        if (relationship == null)
            return new LinkedList<Row>();

        // execute the relationship's where clause
        try {
            return relationship.execute(row);
        } catch (FatalDBUtilLibException e) {
            throw new FatalDBUtilLibException("Error in merge.evaluateEquality.\n" + "Exception message: "
                    + e.getMessage());
        }
    }

    /**
     * Evaluate the foreignKeyEquality test for a particular row from one of the source tables to see if this row's
     * foreign key values are present in the target table.
     *
     * @param row row whose foreignKeyEquality is to be evaluated
     * @return list of rows returned from executing the foreignKeyEquality test.
     * @throws FatalDBUtilLibException if an error occurs
     */
    private LinkedList<Row> evaluateForeignKeyEquality(Row row) throws FatalDBUtilLibException {
        Relationship foreignKeyRelationship = this.foreignKeyEquality.get(row.getTable().getName());
        return evaluateEquality(foreignKeyRelationship, row);
    }

    /**
     * Evaluate the uniqueKeyEquality test for a particular row from one of the source tables to see if this row's
     * unique key values are present in the target table. If row is from an idowner table, then its foreign keys are
     * remapped before checking for unique key equality in the target schema.
     *
     * @param row        row whose foreignKeyEquality is to be evaluated
     * @param remapTable target schema's remap table to use when remapping foreign keys in non-idowner table rows
     * @return list of rows returned from executing the uniqueKeyEquality test.
     * @throws FatalDBUtilLibException if an error occurs
     */
    private LinkedList<Row> evaluateUniqueKeyEquality(Row row, RemapTable remapTable) throws FatalDBUtilLibException {
        Row rowCopy = row;
        Table table = row.getTable();

        // For rows from idowner tables, it is possible that the unique keys for the row contain
        // foreign keys. (e.g., netmag's unique keys includes orid - a foreign key to origin)
        // Remap the unique keys in the temporary row rowCopy that are foreign keys before testing
        // for equality.
        Column[] columns = table.getColumns();
        LinkedList<Integer> foreignKeys = this.dependencyMap.get(table);

        if (table.isIdOwner() && foreignKeys != null) {
            rowCopy = row.clone();
            // Remap unique foreign keys
            for (Integer i : foreignKeys) {
                int index = i.intValue();
                String fkey = columns[index].getForeignKey();
                // value to be remapped
                Long originalVal = row.getValueAsLong(index);

                String remapSource = table.getSchema().getRemapSource(fkey);
                // value(s) old value should be remapped to
                Long remappedVal = remapTable.getCurrentId(remapSource, fkey, originalVal);

                // If oldVals is null, then there were no remap values available
                if (remappedVal == null) {
                    if (columns[index].NAValueAllowed() && columns[index].getNAValue().equals(originalVal))
                        remappedVal = originalVal;
                        // otherwise, this row has a foreign key that does not allow NAValues (so the
                        // foreign key MUST refer to something), but this foreign key refers to nothing.
                    else
                        throw new FatalDBUtilLibException("ERROR in " + "Merge.evaluateUniqueKeyEquality.\n" + row
                                + " contains foreign keys"
                                + " that are also unique keys.\nIn order to determine if an \"equal\""
                                + " row exists in the target table, these foreign keys must be remapped"
                                + " with values from the remap table.\nHowever, no remap table values"
                                + " were found when trying to remap the " + columns[index].getName()
                                + " column with an original_id value of " + originalVal);
                }
                rowCopy.updateField(index, remappedVal);
            }
        }
        Relationship uniqueKeyRelationship = this.uniqueKeyEquality.get(table.getName());
        return evaluateEquality(uniqueKeyRelationship, rowCopy);
    }

    /**
     * Returns the unique key equality test for a specified source table.
     *
     * @param sourceTableName the name of the table whose test is requested
     * @return the Relationship object that encapsulates the unique key equality test for a specific source Table
     */
    public Relationship getUniqueKeyEqualityTest(String sourceTableName) {
        return this.uniqueKeyEquality.get(sourceTableName);
    }

    /**
     * When a row from an idowner table is being considered for merging, and there is a row in the target table that
     * enjoys unique field equality with the source row, the default behavior is for the source row to be dropped and
     * the target row to remain unchanged. This behavior can be overridden using a priority list. If the priority of the
     * source row is greater than the priority of the target row, then the target row will be updated with all of the
     * information from the source row.
     * <p>
     * Priorities are established on the basis of one of the columns in the rows. The name of the column in the rows
     * that will be used to establish relative priority as well as the priority list are specified using this method.
     * The elements of the priority list should contain the values that might be encountered in the column used to
     * establish priority in the source and target rows, in order from highest to lowest priority.
     * <p>
     * For example, if the column used to establish priority is AUTH and the priority list is: [JOE, MIKE, BOB], then
     * the AUTH fields of the source and target rows are compared. If source.AUTH is JOE and target.auth is BOB, then
     * the target row will be updated with all of the information from the source row since the source row has a higher
     * priority. If a row does not contain an AUTH value, or its value is not in the specified priority list, the row is
     * assigned infinitely low priority. Source data only replaces target data if the source's priority is greater than
     * target's priority.
     * <p>
     * If the priority list is not null but contains only 0 or 1 elements, then all rows are given infinitely high
     * priority and source rows that are equal to target rows will always update the target rows.
     *
     * @param priorityColumn the name of the column used to establish priority
     * @param priorityList   values that might be encountered in the priorityColumn column of the source and target rows
     *                       in order from highest to lowest priority
     */
    public void setPriorities(String priorityColumn, List<String> priorityList) {
        if ((priorityList == null)) {
            this.priority = null;
            return;
        }
        this.priority = new Priority(priorityColumn, priorityList);
    }

    /**
     * Specify the value that LDDATE columns that will be set to in the target. This results in the creation of a new
     * UndoSqlTable for this Merge object since many of the statements that go into the undoSqlTable depend on the
     * ldDate.
     *
     * @param ldDate value that LDDATE columns that will be set to in the target
     * @throws FatalDBUtilLibException
     */
    public void setLoadDate(Date ldDate) throws FatalDBUtilLibException {
        if (ldDate == null) {
            DBDefines.ERROR_LOG.add("ERROR in Merge.setLoadDate.  ldDate is null.  ldDate will be"
                    + " set to current date/time.");
            return;
        }

        this.ldDate = ldDate;

        // create a new UndoSqlTable with the new ldDate since many of the statements that go into
        // the undoSqlTable depend on the ldDate.
        // this.undoSqlTable = new UndoSqlTable(this.targetSchema, this.ldDate);
    }

    /**
     * Specify whether or not to record row histories. If recordRowHistories is true, then row history information will
     * be stored; if this is false, then row history information will not be stored. Row history recording is typically
     * turned off for large data runs since it takes up so much room in memory and generates voluminous output in the
     * log files.
     *
     * @param recordRowHistories true if row histories should be recorded; false otherwise
     */
    public void setRecordRowHistories(boolean recordRowHistories) {
        this.recordRowHistories = recordRowHistories;
    }

    /**
     * Returns the Table that is sourceTable's target Table. This target Table is where rows from the sourceTable are
     * going to be merged.
     *
     * @param sourceTableName name of the source Table that needs its target Table returned
     * @return sourceTable's target Table or null if the target table is containd in {@link #missingTargetTables
     * missingTargetTables}
     */
    private Table getTargetTable(String sourceTableName) {
        Table sourceTable = this.sourceSchema.getTable(sourceTableName);
        if (this.missingTargetTables.contains(sourceTable))
            return null;

        return this.targetSchema.getTableOfType(sourceTable.getTableType());
    }

    /**
     * A class's finalize method frees up all the database cursors held by this Merge object. If applications that use a
     * Merge object experience database "too many open cursors" errors, it may be because Merge objects are not being
     * finalized sufficiently often by the garbage collector. It is best to explicitly close() all Merge objects when
     * they are no longer needed.
     */
    @Override
    public void finalize() {
        close();
    }

    /**
     * Close this Merge object. This closes all the Relationship objects managed by this Merge object. This in turn
     * frees all the database cursors related to this Merge object.
     */
    public void close() {
        for (Relationship r : this.uniqueKeyEquality.values())
            r.close();
        for (Relationship r : this.foreignKeyEquality.values())
            r.close();
    }

    /**
     * Returns a String representation of this Merge object. This includes the following information for each of the
     * tables in the source schema:
     * <table>
     * <tr>
     * <td>Source Table: [source table]</td>
     * <td>Target Table: [target table of same type as source table]</td>
     * </tr>
     * <tr>
     * <td colspan=2>&nbsp;&nbsp; Foreign Key Equality Test: [where clause for the foreign key equality test for the
     * source table]</td>
     * </tr>
     * <tr>
     * <td colspan=2>&nbsp;&nbsp; Unique Key Equality Test: [where clause for the unique field equality test for the
     * source table]
     * </tr>
     * </table>
     *
     * @return a String representation of this Merge object
     */
    @Override
    public String toString() {
        StringBuilder mergeString = new StringBuilder("Merge Information:\n");
        // for each source table ...
        for (Table sourceTable : this.sourceSchema.getTables()) {
            // source table
            String sourceTableName = sourceTable.getName();
            mergeString.append("Source Table: " + sourceTableName);

            Table targetTable = getTargetTable(sourceTableName);
            mergeString.append("\tTarget Table: ");

            if (!this.missingTargetTables.contains(this.sourceSchema.getTable(sourceTableName)) && targetTable != null) {
                // target table
                mergeString.append(targetTable.getName() + "\n");

                // foreign key equality relationship
                Relationship rel = this.foreignKeyEquality.get(sourceTableName);
                if (rel != null)
                    mergeString.append("\tForeign Key Equality Test: " + rel.getRelationship() + "\n");

                // unique field equality relationships
                rel = this.uniqueKeyEquality.get(sourceTableName);
                if (rel != null)
                    mergeString.append("\tUnique Key Equality Test: " + rel.getRelationship() + "\n");
            } else
                mergeString.append("[none]\n");
        }
        return mergeString.toString();
    }

    /**
     * Retrieve a legend for the codes that appear in a Row's history
     *
     * @return a legend for the codes that appear in a Row's history
     */
    public static String historyCodeLegend() {
        String history = "History code legend:\n" + "11\n"
                + "There is a row in the target table that enjoys unique field equality with\n"
                + "the idowner row currently being considered for merging.  Drop this row.\n\n" + "12\n"
                + "There is a row in the target table that enjoys unique field equality with\n"
                + "the idowner row currently being considered for merging.  However, either the source\n"
                + "row has higher priority than the target row, or the source row's status was set to\n"
                + "FORCE_UPDATE in the calling routine.  The target row will be updated with the\n"
                + "information in the source row (the owned ID value will not change).\n\n" + "13\n"
                + "This is a new idowner row that has not been seen before and there is no row in the\n"
                + "target tables that is 'equal' it.  This one must be inserted, with a new id value.\n"
                + "Get a new ownedID value from the ID_GAPS table.\n\n" + "14\n"
                + "This is a new row that has not been seen before and there is no row in the target\n"
                + "tables that is 'equal' it.  This one must be inserted, with a new id value.\n"
                + "However, it's status was set to FIX_ID by the calling routine so it will\n"
                + "be inserted into the target table with its owned ID value intact.  A new value\n"
                + "will not be obtained from the ID_GAPS table.\n\n" + "15\n"
                + "The calling routine set the status of this row to FORCE_NEW_ID.  This row will\n"
                + "inserted into the target table, with a new ID value from the ID_GAPS table\n"
                + "without evaluating any field equality tests.\n\n" + "16\n"
                + "This is a row from an idowner table that has a compound primary key\n"
                + "(example: remark table has primary keys [commid, lineno])\n"
                + "and its ownedID has already been remapped.  Insert this row.\n\n" + "17\n"
                + "This source row is from an idowner table that is a member of 'referenced tables'\n"
                + "and therefore there is no target table to receive it.  It is being dropped.\n\n" + "20\n"
                + "At the conclusion of the first row graph traversal, it has been determined that\n"
                + "no rows from idowner source tables will be dropped.  It is therefore impossible\n"
                + "for any rows from non-idowner tables to be dropped.  The second row graph traversal\n"
                + "can be skipped.  Set the status of all  non-idowner to INSERT and proceed with third\n"
                + "rowGraph traversal.\n\n" + "21\n"
                + "At least one of the foreign keys that this non-idowner row contains is owned by an\n"
                + "idowner table that always inserted its rows.  Therefore, this row must be inserted.\n\n" + "22\n"
                + "There are no rows in the target table that have foreign keys that have the same\n"
                + "values as the foreign keys in this row from a non-idowner table.\n"
                + "Insert this row.  (Potential unique key violation if unique keys are equal).\n\n" + "23\n"
                + "One or more rows were found in the target table that have the same foreign keys\n"
                + "and, since the uniqueFieldTest will return the same rows as the foreignKey test,\n"
                + "then the unique keys are equal as well.  Drop this row.\n\n" + "24\n"
                + "Foreign keys are equal and unique keys are equal.  Drop this row.\n"
                + "No negative consequences.\n\n" + "27\n"
                + "There is a row in the target table with same foreign key values as a non-idowner\n"
                + "source rows with foreign keys that were all previously involved in a decision to\n"
                + "not merge an idowner row but with different unique keys.  Merge it.  It is \n"
                + "possible that this will generate a primary key violation.\n\n" + "28\n"
                + "This row from a non-idowner table comes from a table that is a member of 'referenced\n"
                + "tables'.  Drop this row.\n\n" + "29\n"
                + "One or more rows were found in the target table that have the same foreign keys\n"
                + "and the calling application specified status FORCE_UPDATE for this row.\n" + "Update this row.\n\n";
        return history;
    }

    /**
     * When a row from an idowner table is being considered for merging, and there is a row in the target table that
     * enjoys unique field equality with the source row, the default behavior is for the source row to be dropped and
     * the target row to remain unchanged. This behavior can be overriden using a priority list. If the priority of the
     * source row is greater than the priority of the target row then the target row will be updated with all the
     * information from the source row.
     * <p>
     * Priorities are established on the basis of one of the fields in the rows. The name of the column in the rows that
     * will be used to establish relative priority as well as the priority list are specified using this method. The
     * elements of the priority list should contain the values that might be encountered in the column used to establish
     * priority in the source and target rows, in order from highest to lowest priority.
     * <p>
     * For example, if the column used to establish priority is AUTH and the priority list is: [JOE, MIKE, BOB], then
     * the AUTH fields of the source and target rows are compared. If source.AUTH is JOE and target.auth is BOB, then
     * the target row will be updated with all of the information from the source row. If a row does not contain an AUTH
     * value, or its value is not in the specified priority list, the row is assigned infinitely low priority. Source
     * data only replaces target data if the source's priority is greater than target's priority.
     * <p>
     * If the priority list is not null but contains only 0 or 1 elements, then all rows are given infintely high
     * priority and source rows that are equal to target rows will always update the target rows.
     * <p>
     * Copyright: Copyright (c) 2004
     * </p>
     * <p>
     * Company: Sandia National Laboratories
     * </p>
     *
     * @author Sandy Ballard
     * @version 1.0
     */
    private class Priority {
        /**
         * The name of the column that is to used as the basis for evaluating priority.
         */
        private String priorityColumn;

        /**
         * A map from value of a row's priorityColumn value -> value's priority.
         */
        private HashMap<String, Integer> priorityValue = new HashMap<String, Integer>();

        /**
         * Constructor.
         *
         * @param priorityColumn the name of the column used to establish priority
         * @param priorityList   values that might be encountered in the priorityColumn column of the source and target
         *                       rows in order from highest to lowest priority
         */
        protected Priority(String priorityColumn, List<String> priorityList) {
            if (priorityList == null || priorityList.size() == 0)
                return;

            this.priorityColumn = priorityColumn;
            int p = 0;
            for (String field : priorityList) {
                this.priorityValue.put(field, Integer.valueOf(p));
                p++;
            }
        }

        /**
         * Returns the relative priority of a row.
         *
         * @param row the Row object whose priority is requested.
         * @return the relative priority of the row. A low value indicates high priority.
         */
        protected int getPriority(Row row) {
            if (this.priorityValue.size() == 0)
                return Integer.MIN_VALUE;

            // getValueStringNoError does not generate an error if the row does not have
            // priorityField as one of its columns. This is used since it's befuddling to users to
            // see those types of errors since they can't trace them back to trying to retrieve
            // priority related information.
            String value = row.getValueStringNoError(this.priorityColumn);
            if (value != null) {
                Integer p = this.priorityValue.get(value);
                if (p != null)
                    return p.intValue();
            }
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Take a {@link gov.sandia.gnem.dbutillib.Row.DanglingForeignKeyPointerException
     * Row.DanglingForeignKeyPointerException} and generate a useful error from it.
     *
     * @param exception dangling foreign key exception to generate a useful error from
     * @param rowGraph  rowGraph row causing the error is part of
     * @return a useful error generated from the exception
     */
    private String processDanglingForeignKeyError(Row.DanglingForeignKeyPointerException exception, RowGraph rowGraph) {
        Row errorRow = exception.getErrorRow();
        Table table = errorRow.getTable();
        Column errorColumn = exception.getErrorColumn();
        Object errorValue = exception.getErrorValue();
        Table idOwnerTable = exception.getIdOwnerTable();

        StringBuilder errorMessage = new StringBuilder("Error in "
                + "Merge.checkAllRowsStatus() when creating a new target row from row: " + errorRow + " from table: "
                + table.getName() + " when using a remap table." + "\nException message: " + exception.getMessage());

        if (idOwnerTable == null)
            errorMessage.append("\nThe table that owns " + errorColumn.getName()
                    + " is NOT a member of the source schema.  This could be why no " + errorValue + " for the "
                    + errorColumn + " is in the remap table");
        else {
            Set<Row> idownerRows = rowGraph.getRowsOfType(idOwnerTable.getTableType());

            boolean found = false;
            for (Row r : idownerRows) {
                Object val = r.getValue(errorColumn.getName());
                if (val != null && val.equals(errorValue)) {
                    found = true;
                    break;
                }
            }
            if (found)
                errorMessage
                        .append("The " + idOwnerTable.getName() + " contains a row " + "with a "
                                + errorColumn.getName() + " value of " + errorValue
                                + ".  This error should not have occurred.");
            else
                errorMessage.append("This does not necessarily mean that a " + idOwnerTable.getName() + " row with an "
                        + errorColumn.getName() + " value of " + errorValue + " does not exist in the database;\n"
                        + "it just means that that row is not present in the rowgraph currently "
                        + "being merged.\nIn order to determine why no " + idOwnerTable.getName() + " row with a "
                        + errorColumn.getName() + " value of " + errorValue
                        + " is in the RowGraph, use the source schema\nrelationships to back track "
                        + "through the RowGraph creation.  (Backtracking -- start with the "
                        + "relationship that has\n" + idOwnerTable.getTableType() + " as the target "
                        + "table and determine which rows in the rowgraph selected those rows.  "
                        + "Continue this process\nuntil there are no more rows to examine.)\n");
        }
        return errorMessage.toString();
    }
}
