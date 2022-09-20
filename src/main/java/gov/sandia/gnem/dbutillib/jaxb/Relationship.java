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
package gov.sandia.gnem.dbutillib.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Relationship
 *
 * <p>Java class for Relationship element declaration.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;element name="Relationship">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="sourceTable" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="targetTable" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="whereClause" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="constraint" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "id",
        "sourceTable",
        "targetTable",
        "whereClause",
        "constraint"
})
@XmlRootElement(name = "Relationship")
public class Relationship {

    protected String id;
    protected String sourceTable;
    protected String targetTable;
    protected String whereClause;
    protected String constraint;

    /**
     * Gets the value of the id property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the sourceTable property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getSourceTable() {
        return sourceTable;
    }

    /**
     * Sets the value of the sourceTable property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSourceTable(String value) {
        this.sourceTable = value;
    }

    /**
     * Gets the value of the targetTable property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getTargetTable() {
        return targetTable;
    }

    /**
     * Sets the value of the targetTable property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTargetTable(String value) {
        this.targetTable = value;
    }

    /**
     * Gets the value of the whereClause property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getWhereClause() {
        return whereClause;
    }

    /**
     * Sets the value of the whereClause property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setWhereClause(String value) {
        this.whereClause = value;
    }

    /**
     * Gets the value of the constraint property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getConstraint() {
        return constraint;
    }

    /**
     * Sets the value of the constraint property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConstraint(String value) {
        this.constraint = value;
    }

}
