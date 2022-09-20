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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.RelationshipStruct;

/**
 * This class defines some Relationships between tables that are commonly
 * used in the context of Gnem-based work.
 */

public class GnemRelationships {
    // HashMap for retrieving relationship information.  The key into
    // this HashMap is a source table name which points to a value that is
    // itself a HashMap with a key that is the target table name and a
    // value that is the RelationshipStruct containing relationship information
    // for the source and target table.
    private static HashMap<String, HashMap<String, RelationshipStruct>> relationships;

    /**
     * Constructor.  Read in the relationships file.
     */
    public GnemRelationships() {
        relationships = new HashMap<String, HashMap<String, RelationshipStruct>>();

        // Read in relationships from the Gnem Relationships file
        InputStream inputStream = ClassLoader.getSystemResourceAsStream
                ("src/gov/sandia/gnem/dbutillib/gnem/GnemRelationships.txt");

        try {
            if (inputStream == null)
                return;
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = reader.readLine();
            while (line != null) {
                line = line.trim();
                // Ignore comments and blank lines
                if (line.length() == 0 || line.startsWith("//")) {
                    line = reader.readLine();
                    continue;
                }

                // Get rid of tabs and double spaces.
                line = line.replaceAll("\t", " ").replaceAll("  *", " ");

                String[] relInfo = line.split(" ");
                // relInfo[0]: source table
                // relInfo[1]: target table
                // relInfo[2] - relInfo[relInfo.length - 2]: equality where clause
                // relInfo[relInfo.length - 1]: constraint
                int length = relInfo.length;
                String sourceTable = relInfo[0];
                String targetTable = relInfo[1];
                String constraint = relInfo[length - 1];
                StringBuilder clause = new StringBuilder();
                for (int i = 2; i < length - 1; i++)
                    clause.append(relInfo[i] + " ");

                // If relationships does not already have an entry for source
                // table, add one.  An entry looks something like:
                // source_table   ->  target_table_1 -> relStruct for source_table/target_table_1 relationship
                //                ->  target_table_2 -> relStruct for source table/target_table_2 relationship
                if (relationships.get(sourceTable) == null)
                    relationships.put(sourceTable, new HashMap<String, RelationshipStruct>());
                relationships.get(sourceTable)
                        .put(targetTable, new RelationshipStruct(sourceTable, targetTable, clause.toString().trim(), constraint));
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            DBDefines.ERROR_LOG.add("Error in GnemRelationships.populateRelationships." +
                    "\n" + e.getMessage());
        }
    }

    /**
     * Return the relationship between a source table and target table.  If no
     * such Relationship exists, null is returned.
     *
     * @param sourceTable the source table in the desired Relationship
     * @param targetTable the target table in the desired Relationship
     * @return Relationship between sourceTable and targetTable (null if none
     * exists)
     */
    public RelationshipStruct get(String sourceTable, String targetTable) {
        sourceTable = sourceTable.trim().toLowerCase();
        targetTable = targetTable.trim().toLowerCase();

        if (relationships.get(sourceTable) == null)
            return null;
        return relationships.get(sourceTable).get(targetTable);
    }

    /**
     * Return all known relationships
     *
     * @return array of RelationshipStructs representing all known relationships
     */
    public RelationshipStruct[] getAllRelationships() {
        ArrayList<RelationshipStruct> relStructList = new ArrayList<RelationshipStruct>();

        for (String sourceTable : relationships.keySet()) {
            for (String targetTable : relationships.get(sourceTable).keySet()) {
                relStructList.add(relationships.get(sourceTable).get(targetTable));
            }
        }

        return relStructList.toArray(new RelationshipStruct[]{});
    }
}
