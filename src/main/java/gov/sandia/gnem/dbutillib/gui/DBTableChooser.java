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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.dao.DAODatabase;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Util;
import gov.sandia.gnem.dbutillib.util.DBDefines;

/**
 * Class that allows the user to select a database table name from existing tables in the database.
 * <p>
 * An object of this class's type gets created when a user clicks on the "Browse Database" button associated with a
 * table type/name in a SchemaPanelDB. (See {@link SchemaPanelDB#getTableBrowseButton
 * SchemaPanelDB.getTableBrowseButton}).
 */
@SuppressWarnings("serial")
public class DBTableChooser extends JDialog {
    /**
     * Database username.
     */
    private String username;

    /**
     * Database username when an object of this class's type is first created.
     */
    private String originalUsername;

    /**
     * Database password.
     */
    private String password;

    /**
     * Database instance.
     */
    private String instance;

    /**
     * Database driver.
     */
    private String driver;

    /**
     * Textfield in {@link SchemaPanelDB SchemaPanelDB} where the selected table name will be placed.
     */
    private JTextField tableTextField;

    /**
     * Table type associated with the "Browse Database" button in {@link SchemaPanelDB SchemaPanelDB}.
     */
    private String tableType;

    /**
     * {@link #dbAccountsList dbAccountsList} label.
     */
    private JLabel dbAccountsListLabel;

    /**
     * List of available database accounts.
     */
    private JList dbAccountsList;

    /**
     * List of available database tables.
     */
    private JList tablesList;

    /**
     * {@link #tablesList tablesList} label
     */
    private JLabel tablesListLabel;

    /**
     * Combobox where users can filter tables based on table type.
     */
    private JComboBox filterComboBox;

    /**
     * {@link #filterComboBox filterComboBox} label
     */
    private JLabel filterLabel;

    /**
     * Whether or not the listeners should respond to events.
     */
    private boolean listenersOn = true;

    /**
     * Table types users can filter available tables by.
     */
    private TreeSet<String> tableTypes;

    /**
     * Map from database account name to list of table names in that account.
     */
    private TreeMap<String, TreeSet<String>> dbAccountToTableNames;

    /**
     * OK button
     */
    private JButton okButton;

    /**
     * Cancel button
     */
    private JButton cancelButton;

    /**
     * Refresh button
     */
    private JButton refreshButton;

    private boolean initializationError = false;

    /**
     * Constructor. Opens up a dialog box where users can select a table name to populate a table name text field in the
     * calling {@link SchemaPanelDB SchemaPanelDB}.
     *
     * @param username       database username
     * @param password       database password
     * @param instance       database instance
     * @param driver         database driver
     * @param tableTypes     available table types to filter on
     * @param tableType      initial table type to filter on
     * @param tableTextField textfield where the selected table name will be placed
     */
    public DBTableChooser(String username, String password, String instance, String driver,
                          ArrayList<String> tableTypes, String tableType, JTextField tableTextField) {

        if (username == null || username.length() == 0 || password == null || password.length() == 0
                || instance == null || instance.length() == 0 || driver == null || driver.length() == 0) {
            GUI_Util.optionPane_error("A database username, password, instance, and driver must be provided in order "
                    + "to browse for a database table.", "Database Table Chooser Error");
            dispose();

            this.initializationError = true;
            return;
        }

        setTitle("Select " + tableType + " Table");

        this.username = username.toLowerCase();
        this.password = password.toLowerCase();
        this.instance = instance;
        this.driver = driver;

        this.tableTextField = tableTextField;
        this.tableType = tableType;
        this.tableTypes = new TreeSet<String>(tableTypes);

        originalUsername = username.toLowerCase();

        // Create list that displays available database accounts
        this.dbAccountsList = new JList();
        this.dbAccountsList.addListSelectionListener(new DBAccountsListener());
        this.dbAccountsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.dbAccountToTableNames = new TreeMap<String, TreeSet<String>>();

        // Create list that displays available tables within the selected
        // database account
        this.tablesList = new JList();
        this.tablesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Filter combobox
        this.filterLabel = new JLabel("Filter:");
        this.filterComboBox = new JComboBox();
        this.filterComboBox.addItemListener(new FilterComboBoxListener());

        // Ok, Cancel, and Refresh buttons
        this.okButton = new JButton("OK");
        okButton.addActionListener(new OkButtonListener());
        this.cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new CancelButtonListener());
        this.refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new RefreshButtonListener());

        // Panel with Ok, Cancel, and Refresh buttons as well as the Filter drop
        // down box
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridBagLayout());

        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill,
        // insets, ipadx, ipady
        GridBagConstraints optionsGbc = new GridBagConstraints(0, 0, 1, 1, 0.01, 0.01, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);

        optionsPanel.add(this.filterLabel, optionsGbc);
        optionsGbc.gridx++;
        optionsPanel.add(this.filterComboBox, optionsGbc);

        optionsGbc.gridy++;
        optionsGbc.gridx = 0;
        optionsPanel.add(this.okButton, optionsGbc);
        optionsGbc.gridx++;
        optionsPanel.add(this.cancelButton, optionsGbc);
        optionsGbc.gridx++;
        optionsPanel.add(this.refreshButton, optionsGbc);

        // DB Accounts panel
        JPanel dbAccountsPanel = new JPanel();
        dbAccountsPanel.setLayout(new BoxLayout(dbAccountsPanel, BoxLayout.Y_AXIS));
        this.dbAccountsListLabel = new JLabel("Accounts");
        dbAccountsPanel.add(this.dbAccountsListLabel);
        dbAccountsPanel.add(new JScrollPane(this.dbAccountsList));

        // Tables Panel
        JPanel tablesPanel = new JPanel();
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));
        this.tablesListLabel = new JLabel("Tables");
        tablesPanel.add(this.tablesListLabel);
        tablesPanel.add(new JScrollPane(this.tablesList));

        // SplitPane - dbAccounts panel on the left, tablesPanel on the right
        JSplitPane splitPane = new JSplitPane();
        splitPane.add(dbAccountsPanel, JSplitPane.LEFT);
        splitPane.add(tablesPanel, JSplitPane.RIGHT);

        setLayout(new BorderLayout());
        add(optionsPanel, BorderLayout.SOUTH);
        add(splitPane, BorderLayout.CENTER);

        // Retrieve available database tables for the given database account
        updateDbAccountToTableNames();

        // Update database usernames list, available tables list, and filter/
        // combobox.
        updateLists();

        // set default tool tips
        setToolTips();

        // This dialog being modal means that it blocks user input to all other
        // windows in the program
        setModal(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();
    }

    @Override
    public void setVisible(boolean visible) {
        if (this.initializationError)
            return;
        else
            super.setVisible(visible);
    }

    /**
     * Update {@link #dbAccountToTableNames dbAccountToTableNames} based on the currently selected database account.
     * This will retrieve all of the tables available from the selected database account and add an entry from database
     * account -> list of available tables in that account to {@link #dbAccountToTableNames dbAccountToTableNames}.
     */
    protected void updateDbAccountToTableNames() {
        // Get database information
        ParInfo schemaPar = new ParInfo();
        schemaPar.addParameter("username", username);
        schemaPar.addParameter("password", password);
        schemaPar.addParameter("instance", instance);
        schemaPar.addParameter("driver", driver);

        DAODatabase dao = null;
        try {
            dao = (DAODatabase) new Schema(schemaPar, "").getDAO();
        } catch (DBDefines.FatalDBUtilLibException ex) {
            String error = "Error in DBTableChooserDialog.updateTableList().\n" + ex.getMessage();
            ex.printStackTrace();
            DBDefines.ERROR_LOG.add(error);
            System.err.println(error);
            return;
        }

        this.dbAccountToTableNames.clear();

        // Get all available tables for the selected database account (username)
        TreeSet<String> list = dao.getAvailableTables(false, null);
        for (String accountAndTableName : list) {
            // Extract username and table name
            int i = accountAndTableName.indexOf('.');
            String accountName = accountAndTableName.substring(0, i).toLowerCase();
            String tableName = accountAndTableName.substring(i + 1).toLowerCase();

            // Add tableName to the list of tables associated with accountName
            if (this.dbAccountToTableNames.get(accountName) == null)
                this.dbAccountToTableNames.put(accountName, new TreeSet<String>());
            this.dbAccountToTableNames.get(accountName).add(tableName);
        }
    }

    /**
     * Update database accounts lists, available tables list, and filter combobox.
     */
    protected void updateLists() {
        // Don't let listeners respond to events while manually updating these
        // gui components.
        this.listenersOn = false;

        // Clear out the filterComboBox and add in tables available for use as
        // table name filters
        this.filterComboBox.removeAllItems();
        this.filterComboBox.addItem("<all>");
        for (String tab : tableTypes)
            this.filterComboBox.addItem(tab);
        // Set the default selected table type
        this.filterComboBox.setSelectedItem(this.tableType);

        // Add all of the available database accounts to the dbAccountsList
        if (this.dbAccountToTableNames.size() > 0)
            this.dbAccountsList.setListData(this.dbAccountToTableNames.keySet().toArray());
        // Select the database account based on user name
        this.dbAccountsList.setSelectedValue(this.originalUsername, true);

        this.listenersOn = true;
    }

    /**
     * Update the tables in the tablesList based on the selected database account.
     */
    private void updateTablesList() {
        String selectedUserName;
        if (this.dbAccountsList.getSelectedValue() != null)
            selectedUserName = this.dbAccountsList.getSelectedValue().toString();
        else
            selectedUserName = this.originalUsername;
        String currentFilter = this.filterComboBox.getSelectedItem().toString();

        // Update what tables are shown based on what table type is selected
        // in the filterComboBox.
        this.tablesList.setListData(filter(this.dbAccountToTableNames.get(selectedUserName), currentFilter).toArray());

        // Update the tool tip for the tables
        updateTablesToolTipText(selectedUserName);
    }

    /**
     * Reopens this JDialog window. If the database connection information is the same, the table names do not need to
     * be retrieved again.
     *
     * @param username       database username
     * @param password       database password
     * @param instance       database instance
     * @param driver         database driver
     * @param tableTypes     available table types to filter on
     * @param tableType      initial table type to filter on
     * @param tableTextField textfield where the selected table name will be placed
     */
    public void reOpen(String username, String password, String instance, String driver, ArrayList<String> tableTypes,
                       String tableType, JTextField tableTextField) {
        try {
            setTitle("Select " + tableType + " Table");

            this.tableTextField = tableTextField;
            this.tableType = tableType;
            this.originalUsername = username.toLowerCase();
            this.tableTypes = new TreeSet<String>(tableTypes);

            boolean same = this.username.equalsIgnoreCase(username) && this.password.equalsIgnoreCase(password)
                    && this.instance.equalsIgnoreCase(instance) && this.driver.equalsIgnoreCase(driver);
            if (!same) {
                this.username = username;
                this.password = password;
                this.instance = instance;
                this.driver = driver;

                // Retrieve available database tables for the given database
                // account
                updateDbAccountToTableNames();

                // Update database usernames list, available tables list, and
                // filter/ combobox.
                updateLists();
            }
            this.filterComboBox.setSelectedItem(tableType);
            pack();
        } catch (Exception e) {
            String error = "Error in TableChooserDialog.reOpen.\n" + e.getMessage();
            e.printStackTrace();
            DBDefines.ERROR_LOG.add(error);
            System.err.println(error);
        }
    }

    /**
     * Update what tables are shown in the tables list based on what table type is selected in the filterComboBox.
     *
     * @param list         list of tables being shown in the tablesList
     * @param filterString string used to filter the table names shown in the tablesList; if filterString is <all> or
     *                     null, list will be returned unchanged
     * @return a filtered list of tables; for example, if list contains: my_origin my_assoc my_arrival your_origin
     * your_assoc your_arrival and filterString is "origin", then this method would return the list: my_origin
     * your_origin
     */
    private TreeSet<String> filter(TreeSet<String> list, String filterString) {
        if (filterString == null || filterString.equals("<all>"))
            return list;
        TreeSet<String> filteredList = new TreeSet<String>();
        for (String item : list)
            if (item.contains(filterString))
                filteredList.add(item);
        return filteredList;
    }

    /**
     * Set default tool tips
     */
    private void setToolTips() {
        // Use a string to keep track of the tool tip text so that we don't have
        // typos
        // showing up in tool tips for components that are supposed to have the
        // same tool tips.

        // accounts
        String toolTipText = "Database accounts to choose tables from";
        this.dbAccountsListLabel.setToolTipText(toolTipText);
        this.dbAccountsList.setToolTipText(toolTipText);

        // update the tool tips for the available tables with the initial
        // username
        updateTablesToolTipText(this.username);

        // filter
        toolTipText = "Filter tables based on table name";
        this.filterLabel.setToolTipText(toolTipText);
        this.filterComboBox.setToolTipText(toolTipText);

        // ok button
        toolTipText = "Update table names";
        this.okButton.setToolTipText(toolTipText);

        // cancel button
        toolTipText = "Exit without updating table names";
        this.cancelButton.setToolTipText(toolTipText);

        // refresh button
        toolTipText = "Refresh table listing";
        this.refreshButton.setToolTipText(toolTipText);
    }

    /**
     * Update the tool tip text for the tables list to include the name of the account the tables are in.
     *
     * @param account database account the tables in the table list are from
     */
    private void updateTablesToolTipText(String account) {
        String toolTipText = "Tables in the " + account + " account";
        this.tablesListLabel.setToolTipText(toolTipText);
        this.tablesList.setToolTipText(toolTipText);
    }

    /**
     * Listener that responds to changes in the database accounts list. When the user selects a different account, this
     * listener updates the tables list to display the tables in that selected database account.
     */
    protected class DBAccountsListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent listSelectionEvent) {
            updateTablesList();
        }
    }

    /**
     * Listener that responds to the user clicking on the Cancel button. Closes the dialog and does not update anything
     * in the calling SchemaPanel.
     */
    protected class CancelButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    /**
     * Listener that responds to the user clicking on the OK button. Closes the dialog and sets the text of the
     * tableTextField to the selected table text.
     */
    protected class OkButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            if (tablesList.getSelectedValue() != null) {
                String selectedTableName = tablesList.getSelectedValue().toString();
                String selectedUserName = dbAccountsList.getSelectedValue().toString();

                // If the selected database account is different than the one in
                // the
                // calling SchemaPanel, prepend the database account name to the
                // table name.
                if (selectedUserName.equalsIgnoreCase(originalUsername))
                    tableTextField.setText(selectedTableName);
                else
                    tableTextField.setText(selectedUserName + "." + selectedTableName);
            }
            dispose();
        }
    }

    /**
     * Listener that responds to the user clicking on the Refresh button. Just updates the tablesList.
     */
    protected class RefreshButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent actionEvent) {
            updateDbAccountToTableNames();
            updateTablesList();
        }
    }

    /**
     * Responds to the user selecting a table type to filter on.
     */
    private class FilterComboBoxListener implements ItemListener {
        public void itemStateChanged(ItemEvent itemEvent) {
            if (listenersOn)
                updateTablesList();
        }
    }
}
