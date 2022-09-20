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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Schema
 *
 * <p>Java class for Schema element declaration.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;element name="Schema">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element ref="{}DAO"/>
 *           &lt;element ref="{}TableDefinition" minOccurs="0"/>
 *           &lt;element ref="{}Table" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{}Relationship" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element name="RemapTable" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="IDGapsTable" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "name",
        "dao",
        "tableDefinition",
        "table",
        "relationship",
        "remapTable",
        "idGapsTable"
})
@XmlRootElement(name = "Schema")
public class Schema {

    protected String name;
    @XmlElement(name = "DAO")
    protected DAO dao;
    @XmlElement(name = "TableDefinition")
    protected TableDefinition tableDefinition;
    @XmlElement(name = "Table")
    protected List<Table> table;
    @XmlElement(name = "Relationship")
    protected List<Relationship> relationship;
    @XmlElement(name = "RemapTable")
    protected String remapTable;
    @XmlElement(name = "IDGapsTable")
    protected String idGapsTable;

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the dao property.
     *
     * @return possible object is
     * {@link DAO }
     */
    public DAO getDAO() {
        return dao;
    }

    /**
     * Sets the value of the dao property.
     *
     * @param value allowed object is
     *              {@link DAO }
     */
    public void setDAO(DAO value) {
        this.dao = value;
    }

    /**
     * Gets the value of the tableDefinition property.
     *
     * @return possible object is
     * {@link TableDefinition }
     */
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    /**
     * Sets the value of the tableDefinition property.
     *
     * @param value allowed object is
     *              {@link TableDefinition }
     */
    public void setTableDefinition(TableDefinition value) {
        this.tableDefinition = value;
    }

    /**
     * Gets the value of the table property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the table property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTable().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Table }
     */
    public List<Table> getTable() {
        if (table == null) {
            table = new ArrayList<Table>();
        }
        return this.table;
    }

    /**
     * Gets the value of the relationship property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the relationship property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRelationship().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Relationship }
     */
    public List<Relationship> getRelationship() {
        if (relationship == null) {
            relationship = new ArrayList<Relationship>();
        }
        return this.relationship;
    }

    /**
     * Gets the value of the remapTable property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRemapTable() {
        return remapTable;
    }

    /**
     * Sets the value of the remapTable property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRemapTable(String value) {
        this.remapTable = value;
    }

    /**
     * Gets the value of the idGapsTable property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getIDGapsTable() {
        return idGapsTable;
    }

    /**
     * Sets the value of the idGapsTable property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIDGapsTable(String value) {
        this.idGapsTable = value;
    }

}
