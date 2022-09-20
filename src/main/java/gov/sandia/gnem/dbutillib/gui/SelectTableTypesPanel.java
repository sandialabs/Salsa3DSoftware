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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.TableDefinition;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Util;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.TableStruct;

/**
 * This class displays a panel where a user can select table types from a list of available table
 * types.  Once table types are selected, the calling SchemaPanel's list of table types will be set
 * to the selected table types.
 */

/*
 * General design notes:
 * The SchemaPanel that contains an object of type SelectTableTypesPanel does not necessarily intend
 * to use the functionality this class provides.  Thus, when this class's constructor is called,
 * very little is done.  It's not until setVisible(true) is called that any sort of table type
 * information is retrieved based on the Table Definition Table settings in the calling SchemaPanel.
 *
 * Since those Table Definition Table settings can change, each time setVisible(true) is called, a
 * check is performed to see if the Table Definition Table information in the calling SchemaPanel
 * has changed.  If it has not changed, the existing information is displayed.  If it has changed,
 * new information is retrieved using the new Table Definition Table information from the calling
 * SchemaPanel.
 */
@SuppressWarnings("serial")
public class SelectTableTypesPanel extends JPanel {
    /**
     * The calling SchemaPanel that created an instance of this class.
     */
    private SchemaPanel schemaPanel;

    /**
     * Keep track of the Table Definition Table that was used to retrieve the list of available
     * tables currently being displayed.
     */
    private TableDefinition currentTableDefinition;

    /**
     * List of available table types label.
     */
    private JLabel availableTablesLabel;

    /**
     * List of available table types.
     */
    private JList availableTablesList;

    /**
     * List of selected table types label.
     */
    private JLabel selectedTablesLabel;

    /**
     * List of selected table types.
     */
    private JList selectedTablesList;

    /**
     * Button to move available tables to selected tables.
     */
    private JButton availableToSelected;

    /**
     * Button to move selected tables to available tables.
     */
    private JButton selectedToAvailable;

    /**
     * Panel for the buttons that move tables from list to another
     */
    private JPanel buttonsPanel;

    /**
     * Button to add selected tables to the calling Schema
     */
    private JButton addSelectedTables;

    /**
     * Track whether or not the unchanging components of this object have been initialized or not
     */
    private boolean staticComponentsInitialized = false;

    /**
     * Constructor.  Actual creation of this JPanel's components occurs when
     * {@link #setVisible setVisible(true)} is called
     *
     * @param schemaPanel the calling SchemaPanel that created an instance of this class
     */
    public SelectTableTypesPanel(SchemaPanel schemaPanel) {
        this.schemaPanel = schemaPanel;
        super.setVisible(false);
    }

    /**
     * Display this SelectTableTypesPanel object.  If the Table Definition Table information in the
     * calling SchemaPanel has changed, updated table type information will be read in.
     *
     * @param visible whether this object should be visible or not
     */
    @Override
    public void setVisible(boolean visible) {
        // Do nothing if the panel simply needs to be hid
        if (!visible) {
            super.setVisible(visible);
            return;
        }

        // Initialize the static components if they have not yet been initialized
        if (!staticComponentsInitialized)
            initializeStaticComponents();

        // Populate and show this object
        populateGui();
        super.setVisible(visible);
    }

    /**
     * Initialize the unchanging components for this SelectTableTypesPanel object.  This components
     * are the ones that are not dependent on data read in from the Table Definition Table specified
     * in the calling SchemaPanel.
     */
    private void initializeStaticComponents() {
        // Available tables label 
        this.availableTablesLabel = new JLabel("Available Tables");
        this.availableTablesLabel.setFont(new Font(availableTablesLabel.getFont().getName(), Font.BOLD, 14));
        this.availableTablesLabel.setForeground(Color.BLUE);

        // Selected tables label
        this.selectedTablesLabel = new JLabel("Selected Tables");
        this.selectedTablesLabel.setFont(new Font(this.selectedTablesLabel.getFont().getName(), Font.BOLD, 14));
        this.selectedTablesLabel.setForeground(Color.BLUE);

        // Button to move tables from the available tables list to the selected tables list
        this.availableToSelected = new JButton(">");
        this.availableToSelected.setMargin(new Insets(0, 0, 0, 0));
        this.availableToSelected.addActionListener(new AvailableToSelectedListener());

        // Button to move tables from the selected tables list to the available tables list
        this.selectedToAvailable = new JButton("<");
        this.selectedToAvailable.setMargin(new Insets(0, 0, 0, 0));
        this.selectedToAvailable.addActionListener(new SelectedToAvailableListener());

        // Panel for the buttons that move tables from list to another
        this.buttonsPanel = new JPanel();
        this.buttonsPanel.setLayout(new BoxLayout(this.buttonsPanel, BoxLayout.Y_AXIS));
        this.buttonsPanel.add(availableToSelected);
        this.buttonsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        this.buttonsPanel.add(selectedToAvailable);

        // Button to add the tables in the selected tables list to the calling SchemaPanel's tables
        this.addSelectedTables = new JButton("Update Tables");
        this.addSelectedTables.addActionListener(new AddTablesToSchemaPanelListener());

        staticComponentsInitialized = true;
    }

    /**
     * This method populates the dynamic components of this panel.  If the current table
     * information being displayed was read from a different Table Definition Table than the one
     * currently specified in the calling SchemaPanel's Table Definition Table parameters, then
     * new table information is read in.  If it is the same Table Definition Table, then no new
     * table information is read in.
     */
    private void populateGui() {
        // Retrieve parInfo information to be able to create a schema to retrieve table types.
        ParInfo parInfo = schemaPanel.getParInfo();
        String parNamePrefix = schemaPanel.getParNamePrefix();

        // Retrieved table types
        String[] tableTypes = new String[]{};

        // Attempt to create a Schema object and retrieve available table types.
        try {
            // Since the state of tables and such in the SchemaPanel may be odd and/or not ready for 
            // Schema creation, remove those parameters so that we do not generate an error.
            parInfo.removeParameter(parNamePrefix + ParInfo.TABLES);
            parInfo.removeParameter(parNamePrefix + ParInfo.RELATIONSHIPS);
            Schema schema = new Schema(parInfo, parNamePrefix);

            // If this new Schema's TableDefinitionTable is the same as the one for the information
            // currently being displayed, then do nothing more since the panel already has curent
            // information
            if (this.currentTableDefinition != null &&
                    this.currentTableDefinition.equals(schema.getTableDefinition()))
                return;

            // Track what table definition information we are using
            this.currentTableDefinition = schema.getTableDefinition();
            tableTypes = schema.getTableDefinition().getTableTypes();
        } catch (DBDefines.FatalDBUtilLibException e) {
            String errorMessage = "Fatal DBUtilLib Exception when trying to " +
                    "retrieve table type information from the table definition " +
                    "table " + parInfo.getItem(parNamePrefix + ParInfo.TABLE_DEFINITION_TABLE) +
                    ".\nError: " + e.getMessage();
            GUI_Util.optionPane_error(errorMessage, "Error");
        }
        init(tableTypes);
    }

    /**
     * Create the GUI components for this panel.  They are more or less laid
     * out as follows:
     * Available Tables                                     Selected Tables
     * move buttons
     * available tables list                               selected tables list
     * Update Tables button
     *
     * @param tableTypes table types to be displayed in the "Available Tables" list
     */
    private void init(String[] tableTypes) {
        // Remove everything in this panel since this method gets called every time the calling
        // SchemaPanel's table definition information changes.  It's fairly unlovely to display
        // old panels on top of new ones. :)
        this.removeAll();

        // Determine the longest table name.  This table name will be used as the "prototype cell 
        // value" for the tables lists to ensure that both the selected tables list and the 
        // available tables list are the same size.
        String longestTableType = "longest table type";
        if (tableTypes != null)
            for (String t : tableTypes)
                if (t.length() > longestTableType.length())
                    longestTableType = t;

        // Available tables list
        this.availableTablesList = new JList(tableTypes);
        this.availableTablesList.setPrototypeCellValue(longestTableType);
        this.availableTablesList.setVisibleRowCount(10);

        // Selected tables list
        this.selectedTablesList = new JList();
        this.selectedTablesList.setVisibleRowCount(10);
        this.selectedTablesList.setPrototypeCellValue(longestTableType);

        // Starting adding components to the panel ...
        setLayout(new GridBagLayout());

        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.01, 0.01,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 0, 10), 0, 0);

        // Available tables label
        add(this.availableTablesLabel, gbc);
        // Leave an empty cell where the buttons will be
        gbc.gridx++;
        // Selected tables label
        gbc.gridx++;
        add(this.selectedTablesLabel, gbc);

        // Lists & move buttons
        gbc.gridx = 0;
        gbc.gridy++;
        add(new JScrollPane(this.availableTablesList), gbc);
        gbc.gridx++;
        add(this.buttonsPanel, gbc);
        gbc.gridx++;
        add(new JScrollPane(this.selectedTablesList), gbc);

        // Button to update tables in SchemaPanel and hide this panel
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets.top = 10;
        gbc.insets.bottom = 10;
        add(this.addSelectedTables, gbc);

        // Put whatever tables are already in the schema panel in the
        // selected tables list.
        GUI_Util.moveListItems(this.schemaPanel.getTableTypes().toArray(), this.availableTablesList, this.selectedTablesList);

        // set default tool tips
        setToolTips();

        // Seems kludgey
        super.revalidate();
        super.setVisible(true);
    }

    /**
     * Set default tool tips
     */
    private void setToolTips() {
        // Use a string to keep track of the tool tip text so that we don't have typos
        // showing up in tool tips for components that are supposed to have the same tool tips.

        // available tables
        String toolTipText = "Available table types from the table definition table";
        this.availableTablesLabel.setToolTipText(toolTipText);
        this.availableTablesList.setToolTipText(toolTipText);

        // selected tables
        toolTipText = "Selected table types";
        this.selectedTablesLabel.setToolTipText(toolTipText);
        this.selectedTablesList.setToolTipText(toolTipText);

        // move from available to selected
        toolTipText = "Move highlighted available tables to the selected tables box";
        this.availableToSelected.setToolTipText(toolTipText);

        // move from selected to available
        toolTipText = "Move highlighted selected tables to the available tables box";
        this.selectedToAvailable.setToolTipText(toolTipText);

        // update tables
        toolTipText = "Add selected table types to the schema";
        this.addSelectedTables.setToolTipText(toolTipText);
    }

    /**
     * Listener that responds to the user clicking on the "Add Tables to Schema" button.  Adds the
     * selected tables to the calling Schema and hide this panel.
     */
    private class AddTablesToSchemaPanelListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Create a String[] of table types
            ListModel model = selectedTablesList.getModel();
            int count = model.getSize();
            ArrayList<TableStruct> selectedTables = new ArrayList<TableStruct>();
            for (int i = 0; i < count; i++)
                selectedTables.add(new TableStruct(model.getElementAt(i).toString()));

            try {
                // Show a busy cursor while setting the tables
                SelectTableTypesPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                // Add tables to the SchemaPanel
                schemaPanel.setTables(selectedTables);

                // Hide this window
                schemaPanel.setSelectTableTypesVisible(false);
            } finally {
                // Show non-busy cursor after the table have been set.  This is in the finally in case
                // an unanticipated exception gets thrown during openProject
                SelectTableTypesPanel.this.setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    /**
     * Listener that responds to the user clicking on the button to move tables
     * from the Selected Tables to the Available Tables.
     */
    private class SelectedToAvailableListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            GUI_Util.moveListItems(selectedTablesList.getSelectedValues(), selectedTablesList, availableTablesList);
        }
    }

    /**
     * Listener that responds to the user clicking on the button to move tables
     * from the Available Tables to the Selected Tables.
     */
    private class AvailableToSelectedListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            GUI_Util.moveListItems(availableTablesList.getSelectedValues(), availableTablesList, selectedTablesList);
        }
    }
}
