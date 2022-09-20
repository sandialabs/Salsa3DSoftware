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

import java.util.HashMap;

/**
 * Simple struct that encapsulates the String version of the fields for a Relationship object without any sort of Schema
 * or DAO information.
 */
public class RelationshipStruct implements Comparable<RelationshipStruct> {
    /**
     * Relationship id
     */
    public String id;
    /**
     * Relationship source table type
     */
    public String sourceTableType;
    /**
     * Relationship target table type
     */
    public String targetTableType;
    /**
     * Relationship where clause
     */
    public String whereClause;
    /**
     * Relationship constraint
     */
    public String constraint;

    /**
     * Constructor.
     *
     * @param id              relationship id
     * @param sourceTableType relationship source table type
     * @param targetTableType relationship target table type
     * @param whereClause     relationship where clause
     * @param constraint      relationship constraint
     */
    public RelationshipStruct(String id, String sourceTableType, String targetTableType, String whereClause,
                              String constraint) {
        this.id = id.trim();
        this.sourceTableType = sourceTableType.trim();
        this.targetTableType = targetTableType.trim();
        this.whereClause = whereClause.trim();
        this.constraint = constraint.trim();
    }

    /**
     * Constructor.
     *
     * @param sourceTableType relationship source table type
     * @param targetTableType relationship target table type
     * @param whereClause     relationship where clause
     * @param constraint      relationship constraint
     */
    public RelationshipStruct(String sourceTableType, String targetTableType, String whereClause, String constraint) {
        this.id = sourceTableType + "->" + targetTableType;
        this.sourceTableType = sourceTableType.trim();
        this.targetTableType = targetTableType.trim();
        this.whereClause = whereClause.trim();
        this.constraint = constraint.trim();
    }

    /**
     * Returns a string representation of this RelationshipStruct.
     *
     * @return a string representation of this RelationshipStruct.
     */
    @Override
    public String toString() {
        return id + " " + sourceTableType + " " + targetTableType + " " + whereClause + " " + constraint;
    }

    /**
     * Returns a string representation of this RelationshipStruct without the id.
     *
     * @return a string representation of this RelationshipStruct.
     */
    public String toStringNoId() {
        return sourceTableType + " " + targetTableType + " " + whereClause + " " + constraint;
    }

    /**
     * Returns whether or not this RelationshipStruct and the other specified RelationshipStruct are equal.
     *
     * @return whether or not this RelationshipStruct and the other specified RelationshipStruct are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (!other.getClass().toString().endsWith("RelationshipStruct"))
            return false;
        RelationshipStruct otherRelStruct = (RelationshipStruct) other;

        return this.sourceTableType.equals(otherRelStruct.sourceTableType)
                && this.targetTableType.equals(otherRelStruct.targetTableType)
                && this.whereClause.trim().equals(otherRelStruct.whereClause.trim())
                && this.constraint.trim().equals(otherRelStruct.constraint.trim());
    }

    /**
     * Used for generating hashcodes for this object. This is necessary for getting the equals method called properly if
     * this object is part of a set.
     */
    private static int newHashCode = 0;

    /**
     * Map from string representation of a RelationshipStruct to the corresponding hashCode for that string.
     */
    public static HashMap<String, Integer> hashCodeMap = new HashMap<String, Integer>();

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hashtables such as those
     * provided by java.util.Hashtable.
     *
     * @return the hash code value for this object
     */
    @Override
    public int hashCode() {
        String relStructString = this.toString();
        if (hashCodeMap.get(relStructString) == null) {
            hashCodeMap.put(relStructString, new Integer(newHashCode));
            newHashCode++;
        }
        return hashCodeMap.get(relStructString).intValue();
    }

    /**
     * Implementation of the Comparable interface's compareTo method.
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object.
     */
    public int compareTo(RelationshipStruct otherRel) {
        // build strings that don't have the ids in them
        return this.toStringNoId().compareTo(otherRel.toStringNoId());
    }
}
