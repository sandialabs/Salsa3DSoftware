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
package gov.sandia.gnem.dbutillib.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import gov.sandia.gnem.dbutillib.Column;
import gov.sandia.gnem.dbutillib.IDGapsTable;
import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Relationship;
import gov.sandia.gnem.dbutillib.Row;
import gov.sandia.gnem.dbutillib.RowGraph;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.Table;
import gov.sandia.gnem.dbutillib.TableDefinition;
import gov.sandia.gnem.dbutillib.jaxb.KBInfo;
import gov.sandia.gnem.dbutillib.jaxb.ObjectFactory;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * <p>
 * Title: DAOFlatFile
 * <p>
 * Description:
 */

public class DAOFlatFile extends DAO {
    // daoType is set to either FF or XML in constructor.
    private String daoType = null;

    private String name = null;

    private Schema schema = null;

    private HashMap<String, String> uri = new HashMap<String, String>();

    private String uriXML;

    // a rowgraph of Rows where the Vertex objects have no children or parents.
    private RowGraph dataGraph;

    // a list of rows that have been modified since the last commit(). Used by
    // rollback() to undo modifications.
    private LinkedList<Row> modifiedRows = new LinkedList<Row>();

    // when a connection is created to an xml file, this map is populated with
    // tableType -> tableName information. This information reflects what is
    // available in the xml file, regardless of what types of tables are actually
    // implemented in this schema.
    protected TreeSet<String> availableTables = new TreeSet<String>();

    protected HashSet<Relationship> availableRelationships = new HashSet<Relationship>();

    /**
     * The Set of all File objects that have already been read and their contents added to dataGraph.
     */
    private HashSet<File> filesRead = new HashSet<File>();

    final static Byte UNMODIFIED = new Byte((byte) 0);
    final static Byte MODIFIED = new Byte((byte) 1);
    final static Byte DROPPED = new Byte((byte) 2);

    /**
     * Map from (Table)table -> (Byte)status where status is one of the static Bytes defined above.
     */
    private HashMap<Table, Byte> tableStatus = new HashMap<Table, Byte>();

    private File xmlInputFile = null, xmlOutputFile = null;

    /**
     * Whether or not to include column information in xml output files.
     */
    private boolean outputXMLColumnInfo = false;

    protected DAOFlatFile(Schema schema, ParInfo configInfo, String prefix) throws DBDefines.FatalDBUtilLibException {
        name = prefix;
        this.schema = schema;
        dataGraph = new RowGraph();
        daoType = configInfo.getItem(name + "DAOType").toUpperCase();
        if (daoType.equals("XML")) {
            String fileName = configInfo.getItem(name + "XMLInputFile", null);
            if (fileName != null) {
                xmlInputFile = new File(fileName);
                uriXML = xmlInputFile.toURI().toString();
            }
            fileName = configInfo.getItem(name + "XMLOutputFile", null);
            if (fileName != null) {
                xmlOutputFile = new File(fileName);
                uriXML = xmlOutputFile.toURI().toString();
            }
        } else if (daoType.equals("FF")) {
            tableStatus.clear();
            modifiedRows.clear();
        }
    }

    @Override
    public ParInfo getParInfo() {
        ParInfo parInfo = new ParInfo();
        parInfo.addParameter(name + "DAOType", daoType);
        if (xmlInputFile != null)
            parInfo.addParameter(name + "_xml_input_file", xmlInputFile.getName());
        if (xmlOutputFile != null)
            parInfo.addParameter(name + "_xml_output_file", xmlOutputFile.getName());
        return parInfo;
    }

    @Override
    public String getType() {
        return daoType;
    }

    /**
     * Returns the URI of this database account. This will be something like: 'database://username@instance'.
     *
     * @return URI of this database account.
     */
    public String getURI() throws DBDefines.FatalDBUtilLibException {
        if (daoType.equals("XML"))
            return uriXML;
        return "";
    }

    /**
     * Returns the URI of a particular table in this database. This will be something like:
     * 'database://username@instance/table'.
     *
     * @param table the Table object whose URI is requested.
     * @return the URI of a particular table.
     */
    @Override
    public String getURI(Table table) {
        if (daoType.equals(DBDefines.XML_DAO))
            return uriXML;
        return uri.get(table.getName());
    }

    @Override
    public boolean equals(Object otherDAO) {
        // Can't be equal - not even from the same class!
        if (!otherDAO.getClass().toString().equals(this.getClass().toString()))
            return false;
        if (!((DAO) otherDAO).getType().equals(daoType))
            return false;
        DAOFlatFile other = (DAOFlatFile) otherDAO;
        if (other.getType().equals("XML")
                && (!xmlInputFile.equals(other.xmlInputFile) || !xmlOutputFile.equals(other.xmlOutputFile)))
            return false;
        return true;
    }

    @Override
    public void createConnection() throws DBDefines.FatalDBUtilLibException {
        // Reread everything to catch any changes that have been made on disk
        dataGraph.clear();
        if (getType().equals(DBDefines.XML_DAO))
            createConnectionXML();
        else if (getType().equals(DBDefines.FF_DAO))
            createConnectionFF();
    }

    public void createConnectionXML() throws DBDefines.FatalDBUtilLibException {
        if (xmlInputFile == null || !xmlInputFile.exists())
            return;
        try {
            // Create a JAXBContext capable of handling classes generated into the
            // gnemJax package.
            JAXBContext jc = JAXBContext.newInstance("gov.sandia.gnem.dbutillib.jaxb");

            // Create an ObjectFactory instance for creating JAXB objects.
            ObjectFactory of = new ObjectFactory();

            // Create KBInfo root element.
            KBInfo jaxbKBInfo = of.createKBInfo();

            // Take an XML stream and turn it into JAXB
            Unmarshaller u = jc.createUnmarshaller();

            // Validate the XML document while unmarshalling it.
            // u.setValidating(true);

            try {
                uriXML = xmlInputFile.toURI().toString();
                // Unmarshal - read XML from stream into JAXB content tree.
                jaxbKBInfo = (KBInfo) u.unmarshal(new FileInputStream(xmlInputFile));
            } catch (FileNotFoundException e) {
                DBDefines.ERROR_LOG.add("ERROR in DAOFlatFile.createConnectionXML(). "
                        + " FileNotFoundException for file:" + xmlInputFile.getName());
                return;
            }

            // Reset rowGraphTypes list to point to the new kbInfo information.
            RowGraph xmlGraph = RowGraph.fromJaxb(jaxbKBInfo.getRowGraph(), this.schema.getIncludeXMLDAO());

            // iterate over all the schemas in the rowgraph.
            for (Schema xmlSchema : xmlGraph.getSchemas()) {
                // iterate over all the rows in the rowgraph that come from this schema.
                for (Row xmlRow : xmlGraph.getRowsOfType(xmlSchema)) {
                    // see if the schema contains of Table of this type.
                    Table table = schema.getTableOfType(xmlRow.getTableType());
                    // if the schema does contain a table of this type, then add the
                    // row to the data graph.
                    if (table != null) {
                        Row row = new Row(xmlRow, table);
                        dataGraph.add(row, false);
                    }
                }
                for (Table table : xmlSchema.getTables())
                    availableTables.add(table.getTableType() + ":" + table.getName());
                for (Relationship relationships : xmlSchema.getRelationships())
                    availableRelationships.add(relationships);
            }
        } catch (JAXBException je) {
            DBDefines.ERROR_LOG.add("ERROR in DAOFlatFile.createConnectionXML(). JAXBException " + je.getMessage());
        } catch (NullPointerException e) {
            DBDefines.ERROR_LOG.add("ERROR in DAOFlatFile.createConnectionXML().  " + "NullPointerException");
        }
    }

    public void createConnectionFF() throws DBDefines.FatalDBUtilLibException {
        try {
            if (schema != null)
                for (Table table : schema.getTables()) {
                    String line = null;
                    FileReader fr = null;
                    BufferedReader br = null;
                    File f = new File(table.getName());
                    if (!f.exists() && !this.schema.getAutoTableCreation())
                        System.err.println("File " + table.getName() + " specified for table of type "
                                + table.getTableType() + " not found");

                    if (f.exists() && !filesRead.contains(f)) {
                        filesRead.add(f);
                        tableStatus.put(table, UNMODIFIED);
                        // flatFiles and tables have the same indexing. So, flatFiles[i]
                        // refers to a flat file that is of type tables[i].
                        // Set up input file.
                        uri.put(table.getName(), f.toURI().toString());
                        fr = new FileReader(f);
                        // BufferedReader has the lovely readLine method.
                        br = new BufferedReader(fr);
                        // read the first line.
                        line = br.readLine();
                        int linesRead = 1;

                        // Columns should be returned in the order they appear
                        // in the flat file from getColumns. (getColumns builds
                        // column information based on the table definition
                        // table.)
                        Column[] columns = table.getColumns();

                        // start is an int[] that records where on each line each field begins.
                        // There is one extra on the end that should be 1 past the end of the line.
                        int[] start = new int[columns.length + 1];
                        start[0] = 0; // first field starts at position 0 on each line of data.

                        // build the the start int[] from the column widths for this table.
                        for (int j = 0; j < columns.length; j++) {
                            start[j + 1] = start[j] + columns[j].getExternalWidth();
                            if (j != columns.length - 1)
                                start[j + 1] += 1;
                        }

                        // Read until we hit the end of file.
                        while (line != null) {
                            if (linesRead % 1000 == 0)
                                System.out.println("Processed " + linesRead + " rows");
                            // extract the data from the current line of info read from the file.
                            Object[] values = new Object[columns.length];
                            for (int j = 0; j < columns.length; j++) {
                                if (j < columns.length - 1 && line.charAt(start[j + 1] - 1) != ' ') {
                                    StringBuilder msg = new StringBuilder(
                                            "ERROR in DAOFlatFile.createConnectionFF() while trying to read from file "
                                                    + table.getName() + ".  Expecting space character at position "
                                                    + start[j + 1] + " but '" + line.charAt(start[j + 1] - 1)
                                                    + "' found instead." + "  Trying to read column " + j + " ("
                                                    + columns[j].getName() + ")  using format "
                                                    + columns[j].getExternalFormat()
                                                    + ".  No rows from this file added to row graph.\n\n");
                                    br.close();

                                    int k = 0;
                                    String col = columns[k].getName();
                                    for (int l = 0; l < line.length(); l++) {
                                        if (k < start.length - 1 && l == start[k + 1] - 1) {
                                            col = "--------------------";
                                            ++k;
                                        } else if (k < columns.length)
                                            col = columns[k].getName();

                                        msg.append(String.format("%30s   %1s %4d%n", col, line.substring(l, l + 1),
                                                l + 1));
                                    }
                                    throw new DBDefines.FatalDBUtilLibException(msg.toString());

                                }

                                if (j == columns.length - 1 && columns[j].getName().equals("LDDATE")
                                        && schema.getLddateOption().equals(DBDefines.IGNORE_FF_LDDATE))
                                    values[j] = columns[j].valueToString(new Date(0), false);
                                else
                                    values[j] = line.substring(start[j], start[j + 1]).trim();
                            }
                            // Start a new row.
                            Row row = new Row(table, values, true);

                            // add the row to the graph.
                            dataGraph.add(row, false);
                            // read the next line of information.
                            line = br.readLine();
                            linesRead++;
                        }
                        br.close();
                    }

                }
        } catch (java.io.FileNotFoundException e) {
            System.out.println("FileNotFoundException?");
            String error = "ERROR in DAOFlatFile.createConnectionFF().  " + "FileNotFoundException message: "
                    + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(error);
        } catch (java.io.IOException e) {
            String error = "ERROR in DAOFlatFile.createConnectionFF().  " + "IOException message: " + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(error);
        }
    }

    @Override
    public void outputColumnInfo(boolean yesNo) {
        outputXMLColumnInfo = yesNo;
    }

    @Override
    public void commit() {
        if (getType().equals(DBDefines.XML_DAO))
            commitXML();
        else if (getType().equals(DBDefines.FF_DAO))
            commitFF();
    }

    private void commitXML() {
        try {
            // Create a JAXBContext capable of handling classes generated into the
            // gnemJax package.
            JAXBContext jc = JAXBContext.newInstance("gov.sandia.gnem.dbutillib.jaxb");

            // Create an ObjectFactory instance for creating JAXB objects.
            ObjectFactory of = new ObjectFactory();

            // Create KBInfo root element.
            gov.sandia.gnem.dbutillib.jaxb.KBInfo jaxbKBInfo = of.createKBInfo();

            // Create JAXB objects.

            // Create a jaxb version of dataGraph and make it the RowGraph
            // associated with jaxbKBInfo.
            jaxbKBInfo.setRowGraph(dataGraph.toJaxb(outputXMLColumnInfo));

            // Now, kbInfo, the root node for this JAXB content tree is
            // associated with a RowGraph.

            // Write it out as XML to a file.

            // Create a marshaller to marshal above out to xml.
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            try {
                if (xmlOutputFile.toString().contains(DBDefines.PATH_SEPARATOR)) {
                    File path = new File(xmlOutputFile.toString().substring(0,
                            xmlOutputFile.toString().lastIndexOf(DBDefines.PATH_SEPARATOR)));

                    path.mkdirs();
                    if (!path.exists())
                        DBDefines.ERROR_LOG.add("Error in DAOFlatFile.commitXML.  " + path
                                + " does not exist/cannot be created.");
                }
                m.marshal(jaxbKBInfo, new FileOutputStream(xmlOutputFile));
            } catch (FileNotFoundException e) {
                String error = "ERROR in DAOFlatFile.commitXML(). " + "FileNotFoundException for file:" + xmlOutputFile;

                DBDefines.ERROR_LOG.add(error);
            }
        } catch (JAXBException je) {
            String error = "ERROR in DAOFlatFile.commitXML().  " + "JAXBException: " + je.getMessage();
            DBDefines.ERROR_LOG.add(error);
        }
    }

    private void commitFF() {
        try {
            for (Table table : tableStatus.keySet()) {
                String tablename = table.getName();
                if (tableStatus.get(table).equals(DROPPED)) {
                    // delete existing file, it it exists.
                    File f = new File(tablename);
                    if (f.exists() && !f.delete()) {
                        DBDefines.ERROR_LOG.add("ERROR in DAOFlatFile.commitFF(). " + "Unable to delete file "
                                + tablename);
                    }
                } else if (tableStatus.get(table).equals(MODIFIED)) {
                    File path = null;
                    if (tablename.contains(DBDefines.PATH_SEPARATOR)) {
                        path = new File(tablename.substring(0, tablename.lastIndexOf(DBDefines.PATH_SEPARATOR)));

                        path.mkdirs();
                        if (!path.exists())
                            DBDefines.ERROR_LOG.add("Error in DAOFlatFile.commitFF.  " + path
                                    + " does not exist/cannot be created.");
                    }

                    BufferedWriter bw = new BufferedWriter(new FileWriter(tablename));
                    for (Row row : dataGraph.getRowsOfType(table.getTableType())) {
                        bw.write(row.valuesToString(true, ' '));
                        bw.newLine();
                        row.setStatus(DBDefines.UNDETERMINED);
                    }
                    bw.flush();
                    bw.close();
                }
            }
            modifiedRows.clear();
        } catch (IOException e) {
            String error = "Error in DAOFlatFile.commitFF().  IOException " + e.getMessage();
            DBDefines.ERROR_LOG.add(error);
        }
    }

    @Override
    public void closeConnection() {
        dataGraph.clear();
        tableStatus.clear();
        modifiedRows.clear();
    }

    @Override
    public TreeSet<String> getAvailableTables(boolean thisUserOnly, String tableNameFilter) {
        return availableTables;
    }

    public HashSet<Relationship> getAvailableRelationships() {
        return availableRelationships;
    }

    @Override
    public long getMaxID(String idName, String tableName) {
        long maxid = -1;

        for (Iterator<Row> it = dataGraph.iterator(); it.hasNext(); ) {
            Row row = it.next();
            if (row.getTableName().equals(tableName)) {
                Long id = (Long) row.getValue(idName);
                if (id != null && id.longValue() > maxid)
                    maxid = id.longValue();
            }
        }
        return maxid;
    }

    /**
     * Get the next value of an id from an oracle sequence.
     *
     * @param sequenceName String Name of the sequence from which a value is to be extracted.
     * @return long The value extracted from the sequence. Returns -1 if the sequence does not exist.
     */
    @Override
    public long executeSelectFromSequence(String sequenceName) {
        /** TODO: Implement this gov.sandia.gnem.dbutillib.DAO method */
        throw new java.lang.UnsupportedOperationException(
                "Method DAOFlatFile.executeSelectFromSequence(String sequenceName) not implemented.");
    }

    @Override
    public boolean sequenceExists(String sequenceName) {
        return false;
    }

    @Override
    public boolean tableExists(Table table) {
        return tableExists(table.getName());
    }

    @Override
    public boolean tableExists(String tableName) {
        return (schema.getTable(tableName) != null);
    }

    @Override
    public boolean tableIsEmpty(String tableName) {
        Table table = schema.getTable(tableName);
        return table == null || dataGraph.getRowsOfType(table.getTableType()).size() == 0;
    }

    @Override
    public void emptyTable(String tableName) {
        Table table = schema.getTable(tableName);
        if (table != null) {
            Set<Row> rows = dataGraph.getRowsOfType(table.getTableType());
            if (rows.size() > 0) {
                dataGraph.removeAll(rows);
                tableStatus.put(table, MODIFIED);
                for (Row row : rows) {
                    row.setStatus(DBDefines.DELETE);
                    modifiedRows.add(row);
                }
            }
        }
    }

    /**
     * Gets rid of the table named tableName.
     *
     * @param tableName name of the table to be gotten rid of
     * @return whether or not table was dropped successfully or not
     */
    @Override
    public boolean dropTable(String tableName) {
        Table table = schema.getTable(tableName);
        if (table != null) {
            emptyTable(table.getName());
            tableStatus.put(table, DROPPED);
        }
        return true;
    }

    @Override
    public boolean truncateTable(Table table) {
        if (!tableExists(table.getName()))
            return false;
        dropTable(table.getName());
        return true;
    }

    @Override
    public boolean createTable(Table table) {
        // no need to do anything. The file will be opened/created when necessary.
        return true;
    }

    @Override
    public boolean createTable(Table table, boolean setPrimaryKeys, boolean setUniqueKeys) {
        // no need to do anything. The file will be opened/created when necessary.
        return true;
    }

    /**
     * Set a foreign key on a table.
     *
     * @param table             String the name of the table containing the foreign key to be set
     * @param columns           String a comma-separated list of column names that constitute the foreign key
     * @param referencedTable   String the name of the table that the foreign key in table references.
     * @param referencedColumns String the primary key in referencedTable
     * @param onDeleteCascade   boolean see Oracle documentation
     * @param onDeleteSetNull   boolean
     * @param deferrable        boolean
     * @param initiallyDeferred boolean
     * @param enabled           boolean
     * @param validate          boolean
     * @param printSql          boolean dbutillib will print to the screen the sql statement that generates the foreign key.
     * @throws FatalDBUtilLibException
     */
    @Override
    public void setForeignKeys(String table, String columns, String referencedTable, String referencedColumns,
                               boolean onDeleteCascade, boolean onDeleteSetNull, boolean deferrable, boolean initiallyDeferred,
                               boolean enabled, boolean validate, boolean printSql) throws DBDefines.FatalDBUtilLibException {
        // Can't set foreign keys on flat files.
    }

    @Override
    public void setForeignKeys(Table table, boolean onDeleteCascade, boolean onDeleteSetNull, boolean deferrable,
                               boolean initiallyDeferred, boolean enabled, boolean validate, boolean printSql) {
        // Can't set foreign keys on flat files.
    }

    /**
     * Turn the constraints on a table on (contraintsOn = true) or off (constraintsOn = false).
     *
     * @param table         table that needs to have constraints turned on or off
     * @param constraintsOn if true, turn the constraints on the specified table on; if false, turn the constraints on
     *                      the specified table off
     * @return whether or not the changes to this table's constraints was successful
     */
    @Override
    public boolean setConstraints(Table table, boolean constraintsOn) {
        // Can't turn constraints on or off for flat files
        return true;
    }

    /**
     * Set the primary key on a Table. Primary key definition is retrieved from the table definition table. Only applies
     * to database DAOs.
     *
     * @param table Table
     */
    @Override
    public String setPrimaryKey(Table table) throws DBDefines.FatalDBUtilLibException {
        // Can't set primary keys on flat files.
        return "";
    }

    /**
     * Set the unique key on a Table. Unique key definition is retrieved from the table definition table. Only applies
     * to database DAOs.
     *
     * @param table Table
     */
    @Override
    public String setUniqueKey(Table table) throws DBDefines.FatalDBUtilLibException {
        // Can't set unique keys on flat files.
        return "";
    }

    @Override
    public RowGraph getAllData() throws DBDefines.FatalDBUtilLibException {
        return dataGraph.clone();
    }

    /**
     * Get a LinkedList of Row objects that come from tables of a specified type. There is a subtle difference between
     * this method and the version of executeSelectStatement that takes a Table object as a parameter. If there are two
     * different Table objects that are of the same type but come from different schemas, this method will return rows
     * from all the tables of type tableType (from all the schemas). The version of executeSelectStatement that takes a
     * table object will only return rows that belong to that particular table (the one that belongs the schema). If a
     * RowGraph contains only rows from a single schema, this distinction is immaterial.
     *
     * @param tableType   the type of the tables from which Rows are to be retrieved.
     * @param whereClause a select statement. Currently, the parser only supports statements of the form 'where xyz <op>
     *                    123' where xyz is any column name that corresponds to a column of type Long (all ID's are of type Long), 123 is
     *                    any valid long, and <op> is any of the following operators: =, >, <, >=, <=, !=.
     * @return LinkedList of Row objects that result from executing the select statement against the specified Table.
     * @throws DBDefines.FatalDBUtilLibException if an error occurs
     */
    @Override
    public LinkedList<Row> executeSelectStatement(String tableType, String whereClause)
            throws DBDefines.FatalDBUtilLibException {
        LinkedList<Row> rows = new LinkedList<Row>();
        try {
            for (Schema s : dataGraph.getSchemas()) {
                rows.addAll(executeSelectStatement(s.getTableOfType(tableType), whereClause));
            }
        } catch (DBDefines.FatalDBUtilLibException e) {
            String msg = "Error in DAOFlatFile.executeSelectStatement(String,String)." + "\nException message: "
                    + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(msg);
        }
        return rows;
    }

    /**
     * Get a LinkedList of Row objects from a specified Table.
     *
     * @param table       the Table object from which Rows are to be retrieved.
     * @param whereClause a select statement. Currently, the parser only supports statements of the form 'where xyz <op>
     *                    123' where xyz is any column name that corresponds to a column of type Long (all ID's are of type Long), 123 is
     *                    any valid long, and <op> is any of the following operators: =, >, <, >=, <=, !=.
     * @return LinkedList of Row objects that result from executing the select statement against the specified Table.
     * @throws DBDefines.FatalDBUtilLibException if an error occurs
     */
    @Override
    public LinkedList<Row> executeSelectStatement(Table table, String whereClause)
            throws DBDefines.FatalDBUtilLibException {
        if (whereClause == null || whereClause.length() == 0 || whereClause.toUpperCase().equals("WHERE 1=1"))
            return new LinkedList<Row>(dataGraph.getRowsOfType(table.getTableType()));

        // System.out.println("DAOFlatFile.executeSelectStatement(Table, String)\n"
        // +"SELECT * FROM "+table.getName()+" WHERE "+whereClause);

        LinkedList<Row> rows = new LinkedList<Row>();
        try {
            Parser parser = new Parser(whereClause, table);
            for (Row row : dataGraph.getRowsOfType(table.getTableType()))
                if (parser.evaluate(row))
                    rows.add(row);
        } catch (DBDefines.FatalDBUtilLibException e) {
            String msg = "Error in DAOFlatFile.executeSelectStatement(Table,String)." + "\nException message: "
                    + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(msg);
        }

        return rows;
    }

    /**
     * Retrieve all the ownedID values from an idOwner table that satisfy some condition specified with a where clause.
     *
     * @param table       the idowner table against which the sql statement should be executed
     * @param whereClause the where clause
     * @return set of ownedIds
     * @throws DBDefines.FatalDBUtilLibException if an error occurs
     */
    @Override
    public TreeSet<Long> selectOwnedIds(Table table, String whereClause) throws DBDefines.FatalDBUtilLibException {
        if (whereClause == null || whereClause.length() == 0 || whereClause.toUpperCase().equals("WHERE 1=1"))
            return new TreeSet(dataGraph.getRowsOfType(table.getTableType()));

        TreeSet<Long> ids = new TreeSet<Long>();
        Parser parser = null;
        try {
            parser = new Parser(whereClause, table);
            for (Row row : dataGraph.getRowsOfType(table.getTableType()))
                if (parser.evaluate(row))
                    ids.add(row.getValueOwnedID());
        } catch (DBDefines.FatalDBUtilLibException e) {
            String msg = "Error in DAOFlatFile.selectOwnedIds(Table,String)." + "\nException message: "
                    + e.getMessage();
            throw new DBDefines.FatalDBUtilLibException(msg);
        }

        return ids;
    }

    @Override
    public LinkedList<Object> executeSelectStatement(String column, String tableName, String whereClause,
                                                     String orderClause) throws DBDefines.FatalDBUtilLibException {
        LinkedList<Object> output = new LinkedList<Object>();
        Table table = schema.getTable(tableName);
        if (table != null) {
            int i = table.getColumnIndex(column);
            if (i >= 0) {
                try {
                    for (Row row : executeSelectStatement(table, whereClause))
                        output.add(row.getValue(i));
                } catch (DBDefines.FatalDBUtilLibException e) {
                    String msg = "Error in DAOFlatFile.executeSelectStatement("
                            + "String,String,String,String).\nException message: " + e.getMessage();
                    throw new DBDefines.FatalDBUtilLibException(msg);
                }
            }
        }
        return output;
    }

    @Override
    public LinkedList<LinkedList<Object>> executeSelectStatement(LinkedList<String> columns, String tableName,
                                                                 String whereClause, String orderClause) {
        /** @todo Implement this gov.sandia.gnem.dbutillib.DAO method */
        throw new java.lang.UnsupportedOperationException(
                "Method DAOFlatFile.executeSelectStatement(LinkedList<String> " + "columns, String tableName, "
                        + "String whereClause, String orderClause) not implemented.");
    }

    /**
     * Executes a SQL SELECT statement and returns an ArrayList< ArrayList<Object>>.
     *
     * @param statement String the sql statement to be executed.
     * @return ArrayList<ArrayList < Object>>
     * @throws FatalDBUtilLibException if a SQL error occurs.
     */
    @Override
    public ArrayList<Object[]> executeSelect(String statement, Class<?>[] types)
            throws DBDefines.FatalDBUtilLibException {
        /** @todo Implement this gov.sandia.gnem.dbutillib.DAO method */
        throw new java.lang.UnsupportedOperationException("Method DAOFlatFile.executeSelect(String statement) "
                + " not implemented.");
    }

    @Override
    public LinkedList<Row> executeSelectStatement(Relationship relationship)
    // throws DBDefines.FatalDBUtilLibException
    {
        LinkedList<Row> rows = new LinkedList<Row>();

        for (Row row : dataGraph.getRowsOfType(relationship.getTargetTable())) {
            if (relationship.evaluate(row))
                rows.add(row);
        }

        return rows;
    }

    @Override
    public ArrayList<HashMap<String, String>> executeSelectStatement(TableDefinition tableDef, String tableType) {
        /** @todo Implement this gov.sandia.gnem.dbutillib.DAO method */
        throw new java.lang.UnsupportedOperationException(
                "Method DAOFlatFile.executeSelectStatement(TableDefinition tableDef, String tableType) not implemented.");
    }

    @Override
    public LinkedList<DAO.RowWithoutATable> executeSelectStatement(String statement) {
        /** @todo Implement this gov.sandia.gnem.dbutillib.DAO method */
        throw new java.lang.UnsupportedOperationException(
                "Method DAOFlatFile.executeSelectStatement(String statement) not implemented.");
    }

    @Override
    public int executeUpdateStatement(String statement) {
        /** @todo Implement this gov.sandia.gnem.dbutillib.DAO method */
        throw new java.lang.UnsupportedOperationException(
                "Method DAOFlatFile.executeUpdateStatement(String statement) not implemented.");
    }

    public boolean setRowGraph(RowGraph graph) {
        dataGraph = graph;
        return true;
    }

    public String insertRowReturnErrors(Row row) {
        if (insertRow(row))
            return "";
        else
            return "Error inserting row: " + row;
    }

    @Override
    public boolean insertRow(Row row) {
        /** @todo what if row's table is not a member of schema? */
        if (dataGraph.add(row, false)) {
            tableStatus.put(row.getTable(), MODIFIED);
            row.setStatus(DBDefines.INSERT);
            modifiedRows.add(row);
            return true;
        }
        return false;
    }

    /*
     * Insert all of the information in row into the database. (For DAOFlatFile, this method just calls {@link
     * #insertRow insertRow}. ignoreUniqueError is not really used - this method just exists to override the method in
     * DAO.java.)
     *
     * @param row the Row whose information is to be added to the database.
     *
     * @param gnoreUniqueError whether to ignore errors (true) generated from inserting a row that results in a unique
     * key constraint violation or not (false)
     *
     * @throws DBDefines.FatalDBUtilLibException if a SQL error occurs
     */
    @SuppressWarnings("unused")
    @Override
    public boolean insertRow(Row row, boolean ignoreUniqueError) {
        return insertRow(row);
    }

    @Override
    public boolean updateRow(Row row) {
        tableStatus.put(row.getTable(), MODIFIED);
        return true;
    }

    @Override
    public boolean deleteRow(Row row) {
        if (dataGraph.remove(row)) {
            tableStatus.put(row.getTable(), MODIFIED);
            row.setStatus(DBDefines.DELETE);
            modifiedRows.add(row);
            return true;
        }
        return false;
    }

    // public String writeRemapRow(RemapTable remapTable, Object[] values)
    // {
    // /**@todo Implement this gov.sandia.gnem.dbutillib.DAO method*/
    // throw new java.lang.UnsupportedOperationException("Method writeRemapRow() not implemented.");
    // }
    @Override
    public void writeIDGapsRow(IDGapsTable idGapsTable, Object[] values) {
        /** @todo Implement this gov.sandia.gnem.dbutillib.DAO method */
        throw new java.lang.UnsupportedOperationException("Method DAOFlatFile.writeIDGapsRow() not implemented.");
    }

    @Override
    public void rollback() {
        for (Row row : modifiedRows) {
            if (row.getStatus() == DBDefines.INSERT)
                dataGraph.remove(row);
            else if (row.getStatus() == DBDefines.DELETE)
                dataGraph.add(row, false);
            row.setStatus(DBDefines.UNDETERMINED);
        }
        modifiedRows.clear();
    }

    /**
     * Get an Iterator over all the rows that result from executing a select statement against a database table.
     *
     * @param whereClause a where clause. Currently, the parser only supports statements of the form 'where xyz <op>
     *                    123' where xyz is any column name that corresponds to a column of type Long (all ID's are of type Long), 123 is
     *                    any valid long, and <op> is any of the following operators: =, >, <, >=, <=, !=.
     * @param table       the Row objects that are returned by this iterator will be members of this Table object.
     * @return an Iterator over Row objects; null if there is an error
     */
    @Override
    public Iterator<Row> iterator(Table table, String whereClause) {
        try {
            return executeSelectStatement(table, whereClause).iterator();
        } catch (DBDefines.FatalDBUtilLibException e) {
            String msg = "Error in DAOFlatFile.iterator.\nException message: " + e.getMessage();
            DBDefines.ERROR_LOG.add(msg);
            return null;
        }
    }

    /**
     * This method is not currently supported. The iterator that returns {@link DAO.RowWithoutATable
     * DAO.RowWithoutATable} objects is for iterating over large sets of data. The amount of data that in a file for
     * this type of dao is read into memory any way, so an iterator would only be able to iterate over what's in memory.
     */
    @Override
    public Iterator<DAO.RowWithoutATable> iterator(String selectStatement) {
        /** @todo Implement this gov.sandia.gnem.dbutillib.DAO method */
        throw new java.lang.UnsupportedOperationException(
                "Method DAOFlatFile.iterator(String selectStatement) not implemented.");
    }

    @Override
    public gov.sandia.gnem.dbutillib.jaxb.DAO toJaxb() throws JAXBException {
        gov.sandia.gnem.dbutillib.jaxb.DAO jaxbDao = new ObjectFactory().createDAO();
        jaxbDao.setType(this.daoType);
        if (this.xmlOutputFile != null)
            jaxbDao.setFilename(this.xmlOutputFile.getName());
        return jaxbDao;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Data Access Object:" + DBDefines.EOLN);
        s.append("Name:  ");
        s.append(name + DBDefines.EOLN);
        s.append("Type:  ");
        s.append(daoType + DBDefines.EOLN);
        return s.toString();
    }
}
