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

import javax.xml.bind.annotation.XmlRegistry;

import gov.sandia.gnem.dbutillib.jaxb.Row.Field;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the gov.sandia.gnem.dbutillib.jaxb package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: gov.sandia.gnem.dbutillib.jaxb
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Relationship }
     */
    public Relationship createRelationship() {
        return new Relationship();
    }

    /**
     * Create an instance of {@link Schema }
     */
    public Schema createSchema() {
        return new Schema();
    }

    /**
     * Create an instance of {@link Row }
     */
    public Row createRow() {
        return new Row();
    }

    /**
     * Create an instance of {@link Column }
     */
    public Column createColumn() {
        return new Column();
    }

    /**
     * Create an instance of {@link Field }
     */
    public Field createRowField() {
        return new Field();
    }

    /**
     * Create an instance of {@link RowGraph }
     */
    public RowGraph createRowGraph() {
        return new RowGraph();
    }

    /**
     * Create an instance of {@link TableDefinition }
     */
    public TableDefinition createTableDefinition() {
        return new TableDefinition();
    }

    /**
     * Create an instance of {@link Table }
     */
    public Table createTable() {
        return new Table();
    }

    /**
     * Create an instance of {@link DAO }
     */
    public DAO createDAO() {
        return new DAO();
    }

    /**
     * Create an instance of {@link KBInfo }
     */
    public KBInfo createKBInfo() {
        return new KBInfo();
    }

}
