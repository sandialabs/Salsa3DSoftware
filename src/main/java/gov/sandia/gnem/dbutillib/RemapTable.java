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

import java.util.Date;
import java.util.HashMap;

import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * This class handles all functionality that interacts with the Remap table in any way. Remap tables are generated when
 * tables are being merged into each other. The concept behind a remap table is that when you are merging a source table
 * into a target table, one of two things will happen to each row being merged. The row will either be added to the
 * target table because the row contains information that is not present in the target table or the row will not be
 * added to the target table because the target table already contains the information that is contained in the row. New
 * ids are obtained from the {@link IDGapsTable IDGapsTable}.
 * <p>
 * It is often useful to know what happened to rows during the merge process. If the row was not added, then what is the
 * id of the row in the target table that was equivalent to the row not added. If the row is added, then it is helpful
 * to know what its new id is now that it is part of the target table (it cannot keep its old id since it that could
 * cause conflicts in the target table).
 * <p>
 * The remap table keeps track of what ids in the target table the ids in the source table map to.
 * <p>
 * Each Schema is expected to have its own Remap table. It is expected that all of the ids within one Schema are
 * internally consistent so that the foreign keys in rows refer to the row they are expected to refer to, both before
 * and after a merge.
 * <p>
 * The Remap table is expected to exist within the database with the names and types of the columns identified below. <BR>
 * <B>ID_NAME</B>: the name of the id that is being remapped - VARCHAR2(12) <BR>
 * <B>SOURCE</B>: identifier for sets of entries in the remap table that originate from the same place; for example, if
 * a user generates a remap table, SOURCE could be set to the user's name or the name of the account that the tables are
 * in; it could be any String the user wants to use to identify rows in the remap table that are related in some way -
 * VARCHAR2(512) <BR>
 * <B>ORIGINAL_ID</B>: the original value of the id being remapped (the value of the id in the source table) -
 * NUMBER(12) <BR>
 * <B>CURRENT_ID</B>: what ORIGINAL_ID is remapped to - whether it be an id that was already in the target table (in the
 * case of rows that are not added to the target table since equivalent data was already in it) or a new id (in the case
 * of rows that are added to the target table since equivalent data was not already in it) - NUMBER(12) <BR>
 * <B>LDDATE</B>: the date when the information was loaded into the table - DATE
 * <p>
 * When new information is added to the RemapTable, that information will be added to an in memory structure that keeps
 * track of what's been added since the RemapTable object was instantiated. The {@link #write write()} function must be
 * called to write that in memory structure to the database.
 * <p>
 * Here is an example of how to create and use a RemapTable object.
 * <p>
 * Creating:<BR>
 * <CODE>RemapTable remapTable = new RemapTable(mySchema, "remapTableName");</CODE>
 * <p>
 * Adding an entry to the RemapTable:<BR>
 * <CODE>remapTable.addCurrentId("remapSource", "idName", original_id, current_id)</CODE>
 * <p>
 * Getting an entry from the RemapTable:<BR>
 * <CODE>Long currentId = remapTable.getCurrentId("remapSource",
 * "idName", original_id);</CODE> <BR>
 * <B>-- OR --</B><BR>
 * <CODE>Long currentId = remapTable.getCurrentId("remapSource",
 * "idName", row);</CODE>
 * <p>
 * In the second version of getCurrentId where the third parameter is a Row object, then original_id value is extracted
 * out of the idName column of row.
 *
 * @author Jennifer Lewis
 * @version 1.0
 */
public class RemapTable {
    /**
     * This HashMap represents the in memory version of the Remap table. It is a bit complicated in that it is a HashMap
     * of HashMaps of HashMaps. For the following rows of information<BR>
     * SOURCE ID_NAME ORIGINAL_ID CURRENT_ID ------------------------------------------------- LANL ORID 123 456 LANL
     * EVID 789 472 LANL EVID 568 1235 LANL ARID 255 755 LLNL ARID 821 523
     * <p>
     * The remapTable would look like: LANL -> ORID -> 123 -> 456 EVID -> 789 -> 472 568 -> 1235 ARID -> 255 -> 755 LLNL
     * -> ARID -> 821 -> 523 where a->b means that a is a key and b is a value in a HashMap.
     * <p>
     * Basically, the remapTable is a HashMap where the keys are source names and the values are HashMaps. Those
     * HashMaps in turn have keys that are id names and values that are HashMaps. Those HashMaps have keys that are
     * original ids and values that are current ids.
     */
    public HashMap<String, HashMap<String, HashMap<Long, Long>>> remapTable = new HashMap<String, HashMap<String, HashMap<Long, Long>>>();

    /**
     * The Table object that represents the instance of this remap table in the database. If this RemapTable is not
     * associated with an actual remap table in the database, this Table object should remain null.
     */
    protected Table table = null;

    /**
     * Since the number of entries into the remap table is so hard to calculate with the HashMap of HashMaps of
     * HashMaps, just keep track of how many there are currently stored in memory. This is helpful to know so that users
     * can commit the remap table information to the database when it gets large instead of using up all of their memory
     * to store it!
     */
    private int numberEntries = 0;

    /**
     * Constructor - creates the remap table if it does not already exist.
     *
     * @param schema Schema object this RemapTable is associated with
     * @param name   name of the remap table in the database
     * @throws FatalDBUtilLibException if an error occurs when creating the remap table
     */
    public RemapTable(Schema schema, String name) throws FatalDBUtilLibException {
        if (schema != null && name != null && name.trim().length() > 0) {
            try {
                if (schema.getRemapTable() == null)
                    this.table = new Table(name, "REMAPID", "on", schema);
                else
                    this.table = schema.getRemapTable().getTable();

                // table = new Table(name, "REMAPID", "on", schema);
                // table.setRowNameComponents("source,id_name,original_id,current_id");

                if (!schema.dao.tableExists(this.table))
                    schema.dao.createTable(this.table);
            } catch (FatalDBUtilLibException ex) {
                String error = "ERROR in RemapTable constructor when " + "trying to create Table object. \n"
                        + "Schema = " + schema.name + ", Name = " + name
                        + ". No Table object created.\nError message: " + ex.getMessage();
                throw new FatalDBUtilLibException(error);
            }
        }
        // If schema does = null or name is not set to anything, then this
        // constructor will do nothing (similar to the RemapTable(Schema) and
        // RemapTable() constructors).
    }

    /**
     * Constructor - creates the remap table if it does not already exist.
     *
     * @param table the remap table
     * @throws FatalDBUtilLibException if an error occurs when creating the remap table
     */
    public RemapTable(Table table) throws FatalDBUtilLibException {
        this.table = table;
        table.setRowNameComponents("source,id_name,original_id,current_id");

        if (!table.schema.dao.tableExists(table))
            table.schema.dao.createTable(table);
    }

    /**
     * Constructor for a Remap object that is not associated with an actual REMAP table in the database.
     *
     * @param schema Schema object this Remap table is associated with (ignored). Could call RemapTable constructor with
     *               no parameters at all.
     */
    public RemapTable(Schema schema) {
    }

    /**
     * Constructor for a Remap object that is not associated with an actual REMAP table in the database.
     */
    public RemapTable() {
    }

    /**
     * Calls the close() method for this RemapTable's Table object. See Table's {@link Table#close close()} method for
     * more information.
     */
    public void close() {
        if (table != null)
            table.close();
    }

    public void clear() {
        this.numberEntries = 0;
        this.remapTable.clear();
    }

    /**
     * Find out if remapTable contains any remap entries.
     *
     * @return true if the remap table is empty; false otherwise.
     */
    public boolean isEmpty() {
        return numberEntries == 0;
    }

    /**
     * Return the Table object that represents the instance of this remap table in one of the following formats:
     * database, flatfile, or xml file.
     *
     * @return Table object or null if remapTable is not associated with an actual REMAP table
     */
    public Table getTable() {
        return table;
    }

    /**
     * Returns the current id that the original id has been remapped to. The original id is the value in row's idName
     * column.
     *
     * @param source the source used when the original id and current ids were originally added
     * @param idName the name of the column that has had values remapped
     * @param row    Row object that contains the column idName which contains the original id that was remapped to a
     *               different value
     * @return current id that the original id in the idName column of row was remapped to; null if none available
     */
    public Long getCurrentId(String source, String idName, Row row) {
        return getCurrentId(source, idName, (Long) row.getValue(idName));
    }

    /**
     * Returns the current id that the original id has been remapped to. The original id is the value in row's idName
     * column.
     *
     * @param source     the source used when the original id and current ids were originally added
     * @param idName     the name of the column that has had values remapped
     * @param originalId original id that was remapped to a different value
     * @return current id that the original id in the idName column of row was remapped to; null if none available
     */
    public Long getCurrentId(String source, String idName, Long originalId) {
        // Walk through the remapTable and see if it has a current id
        // in it for originalId.

        // See if source is in the remapTable - if so, remapTable.get(source)
        // will return the idname HashMap that source points to.
        HashMap<String, HashMap<Long, Long>> idMap = remapTable.get(source);

        // If idMap is not null, then see if idName is in idMap - if so,
        // idMap.get(idName) will return the originalId HashMap that idName
        // points to.
        if (idMap != null) {
            HashMap<Long, Long> valueMap = idMap.get(idName);

            // If valueMap is not, then see if originalId is in valueMap - if
            // so, valueMap.get(originalId) will return the current id that
            // originalId was remapped to.
            if (valueMap != null)
                return (Long) valueMap.get(originalId);
            else
                return null;
        }
        return null;
    }

    /**
     * Adds a mapping from originalId to currentId to the in memory remap table.
     *
     * @param source     identifier for sets of entries in the remap table that originate from the same place (see this
     *                   class's header comment for more information)
     * @param idName     the name of the column that has its value remapped
     * @param originalId the value that needs to be remapped to a different value
     * @param currentId  the value that originalId needs to be remapped to
     */
    public void addCurrentId(String source, String idName, long originalId, long currentId) {
        addCurrentId(source, idName, Long.valueOf(originalId), Long.valueOf(currentId));
    }

    /**
     * Adds a mapping from originalId to currentId to the in memory remap table.
     *
     * @param source     identifier for sets of entries in the remap table that originate from the same place (see this
     *                   class's header comment for more information)
     * @param idName     the name of the column that has its value remapped
     * @param originalId the value that needs to be remapped to a different value
     * @param currentId  the value that originalId needs to be remapped to
     */
    public void addCurrentId(String source, String idName, Long originalId, Long currentId) {
        // Make sure all of the parameters are more or less what they
        // are expected to be.
        if ((source == null) || (idName == null) || (originalId == null) || (currentId == null)
                || (idName.length() == 0) || (source.length() == 0)) {
            DBDefines.ERROR_LOG.add("Error in RemapTable. addCurrentId error with" + " parameters -> source: " + source
                    + " idName: " + idName + " originalId: " + originalId + " currentId: " + currentId
                    + ".  CurrentId not added. Returning.");
            return;
        }

        // Add information to the in memory structure if it's not there already.
        addToRemapTable(source, idName, originalId, currentId);
    }

    /**
     * Remove the mapping from originalId to currentId in the in memory remap table
     *
     * @param source     identifier for sets of entries in the remap table that originate from the same place (see this
     *                   class's header comment for more information)
     * @param idName     the name of the column that has its value remapped
     * @param originalId the value that needs to be remapped to a different value
     * @param currentId  the value that originalId needs to be remapped to
     */
    public boolean removeEntry(String source, String idName, Long originalId, Long currentId) {
        if (this.remapTable.get(source) == null) {
            DBDefines.ERROR_LOG.add("Error in RemapTable.removeEntry (" + source + ", " + idName + ", " + originalId
                    + ", " + currentId + "). Remap table does not contain a source entry for " + source);
            return false;
        }
        if (this.remapTable.get(source).get(idName) == null) {
            DBDefines.ERROR_LOG.add("Error in RemapTable.removeEntry (" + source + ", " + idName + ", " + originalId
                    + ", " + currentId + "). Remap table does not contain a " + idName + " entry for " + source);
            return false;
        }
        if (this.remapTable.get(source).get(idName).get(originalId) == null) {
            DBDefines.ERROR_LOG.add("Error in RemapTable.removeEntry (" + source + ", " + idName + ", " + originalId
                    + ", " + currentId + "). Remap table does not contain a " + originalId + " entry for " + source
                    + "->" + idName);
            return false;
        }
        if (!this.remapTable.get(source).get(idName).get(originalId).equals(currentId)) {
            DBDefines.ERROR_LOG.add("Error in RemapTable.removeEntry (" + source + ", " + idName + ", " + originalId
                    + ", " + currentId + "). Remap table's " + originalId + " entry for " + source + "->" + idName
                    + " does not map to the specified currentId of " + currentId);
            return false;
        }
        this.remapTable.get(source).get(idName).remove(originalId);
        return true;
    }

    /**
     * Returns the size of the in memory remap table.
     *
     * @return size of the in memory remap table
     */
    public int size() {
        return this.numberEntries;
    }

    /**
     * Returns the name of this remap table.
     *
     * @return name of this remap table; may be "" but will not be null.
     */
    public String getName() {
        if (table == null)
            return "";
        return this.table.name;
    }

    /**
     * Produces a readable remapTable String for output purposes.
     *
     * @return String representation of remapTable
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        if (this.table != null)
            str.append(this.table.getName() + DBDefines.EOLN);

        // Walk through the source keys.
        for (String source : remapTable.keySet()) {
            // Get source's idName map.
            HashMap<String, HashMap<Long, Long>> idMap = remapTable.get(source);

            // Make source length at least 10.
            while (source.length() < 10)
                source += " ";

            // Walk through the id name keys.
            for (String idName : idMap.keySet()) {
                // Get id's originalId map.
                HashMap<Long, Long> origIdMap = idMap.get(idName);

                // Make idname length at least 10.
                while (idName.length() < 10)
                    idName += " ";

                // Walk through the original id keys.
                for (Long originalId : origIdMap.keySet()) {
                    // Get originalId's currentId.
                    Long currentId = origIdMap.get(originalId);

                    String origStr = originalId.toString();

                    // Make origStr (original id String) length at least 10.
                    while (origStr.length() < 10)
                        origStr = " " + origStr;

                    String currStr = currentId.toString();

                    // Make currStr (current id String) length at least 10.
                    while (currStr.length() < 10)
                        currStr = " " + currStr;

                    // Append all of the current information to the
                    // growing StringBuilder.
                    str.append(source + "  " + idName + "  " + origStr + "  " + currStr + DBDefines.EOLN);
                }
            }
        }
        return str.toString();
    }

    /**
     * Drop all the entries in the in-memory remapTable that have the specified source and idName.
     *
     * @param source the source of the entries to be dropped
     * @param idName the idname of the entries to be dropped
     * @return true if any entries were dropped, false if the remap table was not modified
     */
    public boolean dropEntries(String source, String idName) {
        // See if there is a HashMap of idnames for source.
        HashMap<String, HashMap<Long, Long>> idmap = this.remapTable.get(source);

        // If idmap is null or if idName is not present in idmap, return false.
        if (idmap == null || idmap.remove(idName) == null)
            return false;
        return true;
    }

    /**
     * Adds the values in the parameters to the remapTable in memory object. See the remapTable declaration for more
     * information on how the remapTable stores information.
     *
     * @param source     source to be added to the remapTable
     * @param idName     idName to be added to the remapTable
     * @param originalId original id to be added to the remapTable
     * @param currentId  current id to be added to the remapTable
     */
    private void addToRemapTable(String source, String idName, Long originalId, Long currentId) {
        // See if source's idName HashMap is in the remapTable HashMap.
        HashMap<String, HashMap<Long, Long>> idNameMap = remapTable.get(source);

        // If idNameMap is null, then remapTable does not have a HashMap set up
        // for source. Set up a HashMap with source as the key and an empty
        // HashMap for the idName HashMap.
        if (idNameMap == null) {
            idNameMap = new HashMap<String, HashMap<Long, Long>>();
            // Add the idNameMap HashMap to the remapTable HashMap with source as
            // the key.
            remapTable.put(source, idNameMap);
        }

        // See if idName's currentId HashMap is in the idNameMap HashMap.
        HashMap<Long, Long> valueMap = idNameMap.get(idName);

        // If valueMap is null, then idNameMap does not have a HashMap set up for
        // idName. Set up an originalId HashMap with idName as the key and an
        // empty HashMap for the originalId HashMap.
        if (valueMap == null) {
            valueMap = new HashMap<Long, Long>();
            // Add the valueMap HashMap to the idMap HashMap with idName as
            // the key.
            idNameMap.put(idName, valueMap);
        }

        // See if originalId has a currentId.
        Long currentIdVal = valueMap.get(originalId);

        // If originalId has already been remapped to a currentId, then
        // generate a warning, and overwrite the existing currentId.
        if (currentIdVal != null) {
            if (!currentIdVal.equals(currentId)) {
                DBDefines.WARNING_LOG.add("RemapTable.addToRemapTable -- Attempt was made to add a remap"
                        + " entry with source: " + source + " id name: " + idName + " original id: " + originalId
                        + " current id: " + currentId
                        + " when a remap entry already exists for the same source, idname,"
                        + " and original id, yet with a different current id = " + currentIdVal
                        + ".  The existing entry will be overwritten by" + " the new entry.");
            }
        }
        // Only increment the number of entries when a new entry is being added -
        // not when an existing entry is overwritten.
        else
            this.numberEntries++;

        // This will add the currentId->originalId mapping if it is not already
        // there or will overwrite any existing mapping.
        valueMap.put(originalId, currentId);
    }

    // Written by sb 7/2005 but never tested or used.
    // /**
    // * Add the contents of one RemapTable object to another one.
    // * @param other RemapTable the 'other' RemapTable object whose contents
    // * are to be added to 'this' RemapTable object.
    // */
    // public void addToRemapTable(RemapTable other)
    // {
    // for(String source : other.remapTable.keySet())
    // {
    // HashMap<String, HashMap<Long, Long>> idNameMap =
    // other.remapTable.get(source);
    // for (String idName : idNameMap.keySet())
    // {
    // HashMap<Long, Long> originalIdMap = idNameMap.get(idName);
    // for (Long originalId : originalIdMap.keySet())
    // {
    // Long currentId = originalIdMap.get(originalId);
    // // Add information to the in memory structure if it's not there already.
    // addToRemapTable(source, idName, originalId, currentId);
    // }
    // }
    // }
    // }
    public void write(Date loadDate) throws FatalDBUtilLibException {
        // Only write it out if there is a table to write it to!
        if (table == null)
            return;

        // Walk through the HashMap of HashMaps of HashMaps and write out
        // that information.
        for (String source : this.remapTable.keySet()) {
            // Get source's idName HashMap.
            HashMap<String, HashMap<Long, Long>> idNameMap = this.remapTable.get(source);

            // Walk through the id name HashMap.
            for (String idName : idNameMap.keySet()) {
                // idName's originalId HashMap.
                HashMap<Long, Long> origIdMap = idNameMap.get(idName);

                // Walk through the originalId HashMap.
                for (Long originalId : origIdMap.keySet()) {
                    // originalId's currentId
                    Long currentId = origIdMap.get(originalId);

                    try {
                        // Create a Remap row to write to the database.
                        Row row = new Row(table, new Object[]{source, idName, originalId, currentId, loadDate},
                                false);

                        row.insertIntoDB();
                    } catch (FatalDBUtilLibException ex) {
                        String errors = ex.getMessage();
                        if (errors.indexOf("unique constraint") < 0) {
                            StringBuilder msg = new StringBuilder(
                                    "ERROR in RemapTable.write().  Cannot insert Row with");
                            msg.append(" Source = " + source);
                            msg.append(" idName = " + idName);
                            msg.append(" originalId = " + originalId);
                            msg.append(" currentId = " + currentId);
                            msg.append(" lddate = " + table.getFormatOfLDDATE().format(loadDate));
                            msg.append("\n" + errors);
                            throw new FatalDBUtilLibException(msg.toString());
                        }
                    }
                } // end origIdIterator
            } // end idNameIterator
        } // end sourceIterator

        // Wipe out the remapTable for memory usage reasons since it has been
        // written.
        clear();
    }
}
