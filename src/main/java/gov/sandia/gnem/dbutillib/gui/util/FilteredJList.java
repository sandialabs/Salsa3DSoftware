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
package gov.sandia.gnem.dbutillib.gui.util;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * This class implements a JList that a filter box capability. It is from the book Swing Hacks and the code was obtained
 * from: http://oreilly.com/catalog/9780596009076/. It has been modified very slightly in order to allow constructors to
 * be handed the initial list data.
 */
@SuppressWarnings("serial")
// Use this when we move to Java 1.7
// public class FilteredJList extends JList<Object>
public class FilteredJList extends JList {
    private FilterField filterField;

    public FilteredJList() {
        super();
        setModel(new FilterModel());
        this.filterField = new FilterField();
    }

    public FilteredJList(Object[] items) {
        super();
        setModel(new FilterModel(items));
        this.filterField = new FilterField();
    }

    public FilteredJList(Vector<?> items) {
        super();
        setModel(new FilterModel(items));
        this.filterField = new FilterField();
    }

    @Override
    public void setModel(ListModel m) {
        if (!(m instanceof FilterModel))
            throw new IllegalArgumentException();
        super.setModel(m);
    }

    @Override
    public void setListData(Object[] listData) {
        setModel(new FilterModel(listData));
    }

    @Override
    public void setListData(Vector listData) {
        setModel(new FilterModel(listData));
    }

    public void addItem(Object o) {
        ((FilterModel) getModel()).addElement(o);
    }

    public JTextField getFilterField() {
        return this.filterField;
    }

    public void setFilterFieldDimension(Dimension d) {

        this.filterField.setPreferredSize(d);
    }

    // // test filter list
    // public static void main(String[] args)
    // {
    // String[] listItems = { "Chris", "Joshua", "Daniel", "Michael", "Don", "Kimi", "Kelly", "Keagan" };
    // JFrame frame = new JFrame("FilteredJList");
    // frame.getContentPane().setLayout(new BorderLayout());
    // // populate list
    // FilteredJList list = new FilteredJList();
    // for (int i = 0; i < listItems.length; i++)
    // list.addItem(listItems[i]);
    // // add to gui
    // JScrollPane pane = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
    // ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    // frame.getContentPane().add(pane, BorderLayout.CENTER);
    // frame.getContentPane().add(list.getFilterField(), BorderLayout.NORTH);
    // frame.pack();
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // frame.setVisible(true);
    // }

    class FilterModel extends AbstractListModel {
        ArrayList<Object> items;
        ArrayList<Object> filterItems;

        public FilterModel() {
            super();
            this.items = new ArrayList<Object>();
            this.filterItems = new ArrayList<Object>();
        }

        public FilterModel(Object[] listItems) {
            this();
            for (Object listItem : listItems) {
                this.items.add(listItem);
                this.filterItems.add(listItem);
            }
        }

        public FilterModel(Vector<?> listItems) {
            this();
            for (Object listItem : listItems)
                this.items.add(listItem);
        }

        public Object getElementAt(int index) {
            if (index < this.filterItems.size())
                return this.filterItems.get(index);
            return null;
        }

        public int getSize() {
            return this.filterItems.size();
        }

        public void addElement(Object o) {
            this.items.add(o);
            refilter();
        }

        protected void refilter() {
            this.filterItems.clear();
            String term = getFilterField().getText();
            for (int i = 0; i < this.items.size(); i++)
                if (this.items.get(i).toString().indexOf(term, 0) != -1)
                    this.filterItems.add(this.items.get(i));
            fireContentsChanged(this, 0, getSize());
        }
    }

    // inner class provides filter-by-keystroke field
    class FilterField extends JTextField implements DocumentListener {
        public FilterField() {
            super();
            getDocument().addDocumentListener(this);
        }

        public void changedUpdate(DocumentEvent e) {
            ((FilterModel) getModel()).refilter();
        }

        public void insertUpdate(DocumentEvent e) {
            ((FilterModel) getModel()).refilter();
        }

        public void removeUpdate(DocumentEvent e) {
            ((FilterModel) getModel()).refilter();
        }
    }
}
