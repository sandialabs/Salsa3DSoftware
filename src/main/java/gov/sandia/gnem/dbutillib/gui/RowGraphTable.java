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
package gov.sandia.gnem.dbutillib.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import gov.sandia.gnem.dbutillib.util.DBDefines;

/**
 * <p>
 * Title: RowGraphTable
 * </p>
 * <p>
 * This class
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */
@SuppressWarnings("serial")
public class RowGraphTable extends JTable {
    /**
     * Constructor.
     */
    public RowGraphTable() {
        tableHeader.addMouseListener(new RowGraphTable_tableHeader_mouseAdapter(this));
    }

    /**
     * Populate the RowGraphTable with user supplied data. The user's calling method should generated a
     * LinkedsList<String> of columnNames and a LinkedList of Object[] arrays where each Object[] is the same length as
     * the list of columnNames.
     *
     * @param columnNames LinkedList
     * @param data        LinkedList
     */
    public void populate(LinkedList<String> columnNames, LinkedList<Object[]> data) {
        rowGraphTableModel = new RowGraphTableModel(columnNames, data);
        setModel(rowGraphTableModel);
    }

    public Object[] getRow(int row) {
        if (row >= 0 && row < rowGraphTableModel.data.length)
            return rowGraphTableModel.data[row];
        return null;
    }

    public Object getCell(int row, int col) {
        if (row >= 0 && row < rowGraphTableModel.data.length && col >= 0 && col < rowGraphTableModel.data[row].length)
            return rowGraphTableModel.data[row][col];
        return null;
    }

    public int getPreferredHeight() {
        return tableHeader.getPreferredSize().height + getPreferredSize().height;
    }

    public HashSet<String> editableColumns = new HashSet<String>();

    /**
     * inputDataTableModel is the data model for this rowGraph. It's class definition appears in this file.
     */
    protected RowGraphTableModel rowGraphTableModel;

    /**
     * sortList contains the indeces of the columns (in order) that are to be used to sort the data.
     */
    private ArrayList<Integer> sortList = new ArrayList<Integer>(); // Integer column indeces.

    /**
     * Causes rows to get sorted when user clicks on a column header.
     *
     * @param e MouseEvent
     */
    protected void tableHeader_mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1)
            sortList.clear();
        int index = tableHeader.columnAtPoint(e.getPoint());
        int realIndex = getColumnModel().getColumn(index).getModelIndex();
        sortList.add(realIndex);
        rowGraphTableModel.sort(sortList);
    }

    /**
     * <p>
     * Title: TestGUI
     * </p>
     * <p>
     * Copyright: Copyright (c) 2005
     * </p>
     * <p>
     * Company: Sandia National Laboratories
     * </p>
     * <p>
     * This class implements the data mode that underlies the JTable. It is based on a RowGraph.
     *
     * @author Sandy Ballard
     * @version 1.0
     */
    @SuppressWarnings("serial")
    public class RowGraphTableModel extends AbstractTableModel {

        /**
         * the names of the columns in the jtable.
         */
        String[] columnNames;

        /**
         * maps the jtable column names to their corresponding index. column names are case insensitive.
         */
        HashMap<String, Integer> columnIndex;

        /**
         * the index of a row in the jtable. used by the recursive sorting algorithm.
         */
        int currentDataIndex;

        /**
         * This is the data structure that actually gets displayed in the jtable.
         */
        private Object[][] data;

        /**
         * Constructor. Doesn't do much.
         *
         * @param columnNames String[] the names of the columns in the jtable.
         */
        public RowGraphTableModel(List<String> columnNames, List<Object[]> tableData) {
            this.columnNames = new String[columnNames.size()];
            int n = 0;
            for (String columnName : columnNames)
                this.columnNames[n++] = columnName;

            data = new Object[tableData.size()][columnNames.size()];
            n = 0;
            for (Object[] row : tableData)
                data[n++] = row;
            sort(sortList);
        }

        /**
         * Get the number of columns in the jtable.
         *
         * @return int the number of columns in the jtable.
         */
        public int getColumnCount() {
            return columnNames.length;
        }

        /**
         * Get the number of rows in the jtable.
         *
         * @return int the number of rows in the jtable.
         */
        public int getRowCount() {
            return data.length;
        }

        /**
         * Get the name of the i'th column in the jtable.
         *
         * @param i int
         * @return String
         */
        @Override
        public String getColumnName(int i) {
            return columnNames[i];
        }

        /**
         * Get the column index of the column that has name colName.
         *
         * @param colName String the name of the column whose index is desired.
         * @return int -1 if not found.
         */
        public int getColumnIndex(String colName) {
            if (columnIndex == null) {
                columnIndex = new HashMap<String, Integer>();
                for (int i = 0; i < columnNames.length; i++)
                    columnIndex.put(columnNames[i].toLowerCase(), new Integer(i));
            }
            Integer i = columnIndex.get(colName.toLowerCase());
            if (i == null)
                return -1;
            return i.intValue();
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's editable.
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            // Note that the data/cell address is constant,
            // no matter where the cell appears onscreen.
            return editableColumns != null && col >= 0 && col < columnNames.length
                    && editableColumns.contains(columnNames[col]);
        }

        /*
         * Don't need to implement this method unless your table's data can change.
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        /**
         * Sort the rows in the JTable based on the sortlist.
         *
         * @param sortList ArrayList contains the indeces of the columns upon which the sorting is to be based. This is
         *                 the method that should be called from outside RowGraphTableModel.
         */
        private void sort(ArrayList<Integer> sortList) {
            if (data.length > 0 && sortList.size() > 0) {
                LinkedList rows = new LinkedList();
                for (int i = 0; i < data.length; i++)
                    rows.add(data[i].clone());

                currentDataIndex = 0;
                sortChildren(sortNode(rows, sortList.get(0).intValue()), sortList, 0);

                fireTableDataChanged();
            }
        }

        /**
         * Part of the recursive sort algorithm. This is not the entry point into the algorithm.
         *
         * @param rows     LinkedList
         * @param colIndex int
         * @return TreeMap
         */
        private TreeMap sortNode(LinkedList rows, int colIndex) {
            // A node is a TreeMap where the key is the value of some cell and the
            // value is a LinkedList of Object[] which all have the key value in the
            // appropriate cell.
            Object key;
            boolean isBoolean = ((Object[]) rows.getFirst())[colIndex].getClass().getName().equals("java.lang.Boolean");
            TreeMap node = new TreeMap();
            for (Iterator it = rows.iterator(); it.hasNext(); ) {
                Object[] row = (Object[]) it.next();
                if (!isBoolean)
                    key = row[colIndex];
                else if (((Boolean) row[colIndex]).booleanValue())
                    key = "d";
                else
                    key = "n";

                LinkedList nodeEntry = (LinkedList) node.get(key);
                if (nodeEntry == null) {
                    nodeEntry = new LinkedList();
                    node.put(key, nodeEntry);
                }
                nodeEntry.add(row);
            }
            return node;
        }

        /**
         * Part of the recursive sort algorithm. This is not the entry point into the algorithm.
         *
         * @param node     TreeMap
         * @param sortList ArrayList
         * @param index    int
         */
        private void sortChildren(TreeMap node, ArrayList<Integer> sortList, int index) {
            // node is a tree map where the keys are the value in the index'th column
            // and the values are linked lists of Object[] which all have that value in
            // index'th cell.
            //
            if (index < sortList.size() - 1) {
                // Here, we will sort each of the linked lists (the values of the current
                // node), according to the values in cell sortList[index+1].
                for (Iterator it = node.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry nextNode = (Map.Entry) it.next();
                    LinkedList values = (LinkedList) nextNode.getValue();
                    TreeMap sortedNode = sortNode(values, sortList.get(index + 1).intValue());
                    nextNode.setValue(sortedNode);
                    sortChildren(sortedNode, sortList, index + 1);
                }
            } else {
                // there aren't any more indexes to sort on. We are done.
                // Repopulate the data array.
                for (Iterator it = node.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry leaf = (Map.Entry) it.next();
                    for (Iterator jt = ((LinkedList) leaf.getValue()).iterator(); jt.hasNext(); )
                        data[currentDataIndex++] = (Object[]) jt.next();
                }
            }
        }

    }

}

class RowGraphTable_tableHeader_mouseAdapter extends MouseAdapter {
    private RowGraphTable adaptee;

    RowGraphTable_tableHeader_mouseAdapter(RowGraphTable adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            adaptee.tableHeader_mouseClicked(e);
        } catch (Exception ex) {
            String error = "Error in RowGraphTable.mouseClicked.\nError message: " + ex.getMessage();
            DBDefines.ERROR_LOG.add(error);
        }
    }
}
