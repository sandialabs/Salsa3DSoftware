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

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.gui.util.DBUtilLibDocumentListener;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;
import gov.sandia.gnem.dbutillib.util.RelationshipStruct;
import gov.sandia.gnem.dbutillib.util.TableStruct;

/**
 * This class represents a SchemaPanel with a Database DAO.
 */
@SuppressWarnings("serial")
public class SchemaPanelDB extends SchemaPanel {
    /**
     * Handle to this class' DAOPanel
     */
    protected DAOPanelDB daoPanelDB;

    /**
     * Prefixes for configurationParInfo items that are for DB configurations.
     */
    private TreeSet<String> configurationPrefixes = new TreeSet<String>();
    ;

    /**
     * Database table chooser.
     */
    private DBTableChooser tableChooserDialog;

    /**
     * Create a SchemaPanelDB. This constructor invokes the {@link SchemaPanel#SchemaPanel(int)
     * SchemaPanel(schemaPanelType)} constructor. Please see that constructor's comments for more information.
     *
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(int) SchemaPanel(schemaPanelType)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelDB(int schemaPanelType) throws FatalDBUtilLibException {
        this(true, schemaPanelType, new ArrayList<TableStruct>(), new ArrayList<RelationshipStruct>());
    }

    /**
     * Create a SchemaPanelDB. This constructor invokes the {@link SchemaPanel#SchemaPanel(boolean, int)
     * SchemaPanel(modifiable, schemaPanelType)} constructor. Please see that constructor's comments for more
     * information.
     *
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(boolean, int) SchemaPanel(modifiable, schemaPanelType)}
     * @param modifiable      See {@link SchemaPanel#SchemaPanel(boolean, int) SchemaPanel(modifiable, schemaPanelType)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelDB(boolean modifiable, int schemaPanelType) throws FatalDBUtilLibException {
        this(modifiable, schemaPanelType, new ArrayList<TableStruct>(), new ArrayList<RelationshipStruct>());
    }

    /**
     * Create a SchemaPanelDB. This constructor invokes the {@link SchemaPanel#SchemaPanel(int, ArrayList)
     * SchemaPanel(schemaPanelType, tables)} constructor. Please see that constructor's comments for more information.
     *
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(int, ArrayList) SchemaPanel(schemaPanelType, tables)}
     * @param tables          See {@link SchemaPanel#SchemaPanel(int, ArrayList) SchemaPanel(schemaPanelType, tables)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelDB(int schemaPanelType, ArrayList<TableStruct> tables) throws FatalDBUtilLibException {
        this(true, schemaPanelType, tables, new ArrayList<RelationshipStruct>());
    }

    /**
     * Create a SchemaPanelDB. This constructor invokes the {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList)
     * SchemaPanel(modifiable, schemaPanelType, tables)} constructor. Please see that constructor's comments for more
     * information.
     *
     * @param modifiable      See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList)  SchemaPanel(modifiable,
     *                        schemaPanelType, tables)}
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList)  SchemaPanel(modifiable,
     *                        schemaPanelType, tables)}
     * @param tables          See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList)  SchemaPanel(modifiable,
     *                        schemaPanelType, tables)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelDB(boolean modifiable, int schemaPanelType, ArrayList<TableStruct> tables)
            throws FatalDBUtilLibException {
        this(modifiable, schemaPanelType, tables, new ArrayList<RelationshipStruct>());
    }

    /**
     * Create a SchemaPanelDB. This constructor invokes the
     * {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList, ArrayList)  SchemaPanel(modifiable, schemaPanelType,
     * tables, relationships)} constructor. Please see that constructor's comments for more information.
     *
     * @param modifiable      See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList, ArrayList)  SchemaPanel(modifiable,
     *                        schemaPanelType, tables, relationships)}
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList, ArrayList)
     *                        SchemaPanel(modifiable, schemaPanelType, tables, relationships)}
     * @param tables          See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList, ArrayList)  SchemaPanel(modifiable,
     *                        schemaPanelType, tables, relationships)}
     * @param relationships   See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList, ArrayList)
     *                        SchemaPanel(modifiable, schemaPanelType, tables, relationships)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelDB(boolean modifiable, int schemaPanelType, ArrayList<TableStruct> tables,
                         ArrayList<RelationshipStruct> relationships) throws FatalDBUtilLibException {
        super(modifiable, schemaPanelType, tables, relationships);
    }

    /**
     * Create a SchemaPanelDB. This constructor invokes the {@link SchemaPanel#SchemaPanel(ParInfo)
     * SchemaPanel(parInfo)} constructor. Please see that constructor's comments as well as the
     * {@link SchemaPanelDB#SchemaPanelDB(ParInfo, String) SchemaPanelDB constructor} comments for more information.
     *
     * @param parInfo See {@link SchemaPanel#SchemaPanel(ParInfo) SchemaPanel(parInfo)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelDB(ParInfo parInfo) throws FatalDBUtilLibException {
        this(parInfo, "");
    }

    /**
     * Create a SchemaPanelDB. This constructor invokes the {@link SchemaPanel#SchemaPanel(ParInfo, String)
     * SchemaPanel(parInfo, prefix)} constructor. Please see that constructor's comments for more information. In
     * addition to the parameters handled by the {@link SchemaPanel#SchemaPanel(ParInfo, String)  SchemaPanel(parInfo,
     * prefix)} constructor, the following parameters are also handled:
     * <p>
     * <b>{@link ParInfo#USERNAME Username}</b> Database username
     * <p>
     * <b>{@link ParInfo#PASSWORD Password}</b> Database password
     * <p>
     * <b>{@link ParInfo#INSTANCE Instance}</b> Database Instance
     * <p>
     * <b>{@link ParInfo#DRIVER Driver}</b> Database Driver
     *
     * @param parInfo See {@link SchemaPanel#SchemaPanel(ParInfo, String) SchemaPanel(parInfo, prefix)}
     * @param prefix  See {@link SchemaPanel#SchemaPanel(ParInfo, String) SchemaPanel(parInfo, prefix)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelDB(ParInfo parInfo, String prefix) throws FatalDBUtilLibException {
        super(parInfo, prefix);
    }

    /**
     * Method to create a database DAOPanel for this SchemaPanelDB. <br>
     * Implementation of the {@link SchemaPanel#createDAOPanel createDAOPanel} method that is abstract in
     * {@link SchemaPanel SchemaPanel}
     *
     * @return JPanel representing this SchemaPanelDB's DAO information
     */
    @Override
    protected JPanel createDAOPanel() {
        daoPanelDB = new DAOPanelDB();

        JTextField tempTextField = getTableDefPanel().tableDefName;

        // Add listeners that change the ConfigurationComboBox selected item
        // to the correct one based on what is in the dao fields.
        daoPanelDB.addUsernameListener(new ConfigFieldsListener());
        daoPanelDB.addPasswordListener(new ConfigFieldsListener());
        daoPanelDB.addInstanceListener(new ConfigFieldsListener());
        daoPanelDB.addDriverListener(new ConfigFieldsListener());
        tempTextField.getDocument().addDocumentListener(new ConfigFieldsListener());

        return daoPanelDB;
    }

    protected void populate(ParInfo parInfo) {
        // Populate the text fields for this DAOPanel with information from the
        // parInfo object.
        daoPanelDB.setUsername(parInfo.getItem(parNamePrefix + "Username"));
        daoPanelDB.setPassword(parInfo.getItem(parNamePrefix + "Password"));
        daoPanelDB.setInstance(parInfo.getItem(parNamePrefix + "Instance"));
        daoPanelDB.setDriver(parInfo.getItem(parNamePrefix + "Driver"));
        daoPanelDB.setIndexTablespace(parInfo.getItem(parNamePrefix + "IndexTablespace"));
        daoPanelDB.setTableTablespace(parInfo.getItem(parNamePrefix + "TableTablespace"));
    }

    /**
     * Return this SchemaPanelDB's DAO Type. <br>
     * Implementation of the {@link SchemaPanel#getDAOType getDAOType} method that is abstract in
     * {@link SchemaPanel SchemaPanel}
     *
     * @return this SchemaPanelDB's DAO type
     */
    @Override
    public String getDAOType() {
        return DBDefines.DATABASE_DAO;
    }

    /**
     * Add items to the configuration combobox that are of type "sql". (The configuration file defines dao type "DB" as
     * "sql".) This method also adds a configuration combo box listener. <br>
     * Implementation of the {@link SchemaPanel#populateConfigurationComboBox populateConfigurationComboBox} method that
     * is abstract in {@link SchemaPanel SchemaPanel}
     *
     * @param prefixes the prefixes of the different configurations listed in configuration file
     */
    @Override
    protected void populateConfigurationComboBox(TreeSet<String> prefixes) {
        configurationPrefixes.clear();

        // Add DB configuration items to the configurationComboBox
        for (String prefix : prefixes) {
            if (CONFIGURATION_PARINFO.getItem(prefix + ".type", "").equals("sql"))
                configurationPrefixes.add(prefix);
        }

        // Add environment information to the configurationComboBox
        if (DBDefines.DEFAULT_DRIVER != null && DBDefines.DEFAULT_INSTANCE != null
                && DBDefines.DEFAULT_PASSWORD != null && DBDefines.DEFAULT_TABLEDEF != null
                && DBDefines.DEFAULT_USERNAME != null) {
            CONFIGURATION_PARINFO.addParameter("<environment>.sql.driver", DBDefines.DEFAULT_DRIVER);
            CONFIGURATION_PARINFO.addParameter("<environment>.sql.instance", DBDefines.DEFAULT_INSTANCE);
            CONFIGURATION_PARINFO.addParameter("<environment>.sql.password", DBDefines.DEFAULT_PASSWORD);
            CONFIGURATION_PARINFO.addParameter("<environment>.sql.tabledefinitiontable", DBDefines.DEFAULT_TABLEDEF);
            CONFIGURATION_PARINFO.addParameter("<environment>.sql.username", DBDefines.DEFAULT_USERNAME);
            CONFIGURATION_PARINFO.addParameter("<environment>.type", "sql");

            configurationPrefixes.add("<environment>");
        }

        for (String prefix : configurationPrefixes)
            configurationComboBox.addItem(prefix);

        // Set up the configurationComboBox with a listener.
        configurationComboBox.addActionListener(new ConfigurationComboBoxListener());
    }

    /**
     * Return a button that looks for tables in the database. <br>
     * Implementation of the {@link SchemaPanel#getTableBrowseButton getTableBrowseButton} method in SchemaPanel
     *
     * @param tableType table type to browse for
     * @return a button that looks for tables in the database.
     */
    @Override
    protected JButton getTableBrowseButton(String tableType) {
        JButton button = new JButton("Browse");
        button.setMargin(new Insets(0, 0, 0, 0));

        button.addActionListener(new BrowseDatabaseListener(tableType));
        button.setToolTipText("Browse for " + tableType + " tables in the database");
        return button;
    }

    /**
     * <br>
     * Implementation of the {@link SchemaPanel#setDirectory setDirectory} method; for SchemaPanelDB, does nothing.
     *
     * @param currentDirectory directory that should be prepended to the xml file name
     */
    @Override
    public void setDirectory(String currentDirectory) {
        // no directory tracking needed
    }

    /**
     * Register GUI components specific to this SchemaPanelDB with the ParInfoGui object. (The main GUI components for
     * this SchemaPanelDB are primarily handled in {@link SchemaPanel SchemaPanel}. This method typically handles the
     * DAO GUI components.) <br>
     * Implementation of the {@link SchemaPanel#registerSubComponents registerSubComponents} method.
     */
    @Override
    protected void registerSubComponents() {
        daoPanelDB.registerComponents(parInfoGui, parNamePrefix);
    }

    /**
     * Update the ParInfoGui object with information from the gui components. <br>
     * Implementation of the {@link SchemaPanel#synchSubParInfo synchSubParInfo} method.
     */
    @Override
    protected void synchSubParInfo() {
        // The components that must be synchronized are all in the DAOPanelDB
        daoPanelDB.synchParInfo(parInfoGui);
    }

    private boolean tablespaceFieldsVisible = false;

    /**
     * Set whether or not the index and table tablespace fields are visible.
     *
     * @param visible whether or not the index and table tablespace fields are visible
     */
    public void setTablespaceFieldsVisible(boolean visible) {
        daoPanelDB.setTablespaceFieldsVisible(visible);
        tablespaceFieldsVisible = visible;
    }

    @Override
    protected void refreshSubVisibility() {
        setTablespaceFieldsVisible(tablespaceFieldsVisible);
    }

    /**
     * Listener that responds to the user clicking the "Browse Database" button. Opens up a dialog where users can
     * browse tables in the database.
     */
    private class BrowseDatabaseListener implements ActionListener {
        /**
         * Table type this browse button listener is for.
         */
        private String tableType;

        /**
         * Constructor.
         *
         * @param tableType table type this browse button listener is for
         */
        public BrowseDatabaseListener(String tableType) {
            this.tableType = tableType;
        }

        /**
         * Opens up a dialog where users can browse tables in the database.
         */
        public void actionPerformed(ActionEvent e) {
            // JTextField that needs its text set to the selected table name
            JTextField tableText;

            // Other table types the table chooser dialog can filter on
            ArrayList<String> tableTypes;

            // If the browse button is associated with a "special" table, find
            // that table type's table struct in specialTableTypeToTableStruct.
            // The other table types that the table chooser dialog can filter
            // on will be restricted to the "special" table types.
            if (specialTableTypeToTableStruct.get(tableType) != null) {
                tableText = specialTableTypeToTableStruct.get(tableType).tableName;
                tableTypes = new ArrayList<String>();
                for (String key : specialTableTypeToTableStruct.keySet())
                    if (specialTableTypeToTableStruct.get(key).isVisible)
                        tableTypes.add(key);
            }
            // If the browse button is associated with a "normal" table, find
            // that table type's table struct in tableTypeToTableStruct.
            // The other table types that the table chooser dialog can filter
            // on will be all other "normal" tables.
            else {
                tableText = tableTypeToGUITableStruct.get(tableType).tableName;
                tableTypes = getTableTypes();
            }

            try {
                // Show a busy cursor while loading the table chooser
                SchemaPanelDB.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // If the table chooser dialog has not been initialized, create a
                // new one
                if (tableChooserDialog == null)
                    tableChooserDialog = new DBTableChooser(daoPanelDB.getUsername(), daoPanelDB.getPassword(),
                            daoPanelDB.getInstance(), daoPanelDB.getDriver(), tableTypes, tableType, tableText);
                    // If the table chooser dialog has already been initialized, call
                    // reopen. That way, if none of the database connectivity settings
                    // have changed, it won't have to re-obtain the information and can
                    // just reopen the window. This speeds things up considerably.
                else
                    tableChooserDialog.reOpen(daoPanelDB.getUsername(), daoPanelDB.getPassword(), daoPanelDB
                            .getInstance(), daoPanelDB.getDriver(), tableTypes, tableType, tableText);
                tableChooserDialog.setVisible(true);
            } finally {
                // Show non-busy cursor after table chooser has loaded. This is in the finally in
                // case an unanticipated exception gets thrown
                SchemaPanelDB.this.setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    /**
     * Flag used to indicate when code inside the ConfigurationComboBoxListener is being executed.
     */
    private boolean inConfigurationComboBoxListener = false;

    /**
     * This code is called when the configurationComboBox listener is triggered. It populates the dao panel text fields
     * with information read in from the kbdb.cfg file when the user changes what's selected in the
     * configurationComboBox. This code is outside of the listener so that it can be called from outside of the
     * listener. <br>
     * Implementation of the {@link SchemaPanel#configurationComboBoxActionPerformed
     * configurationComboBoxActionPerformed} method that is abstract in {@link SchemaPanel SchemaPanel}
     */
    @Override
    protected void configurationComboBoxActionPerformed(JComboBox source) {
        // The ConfigFieldsListener changes the selected item in the
        // configurationComboBox based on changes the user makes to the dao
        // fields. If this listener is triggered by the ConfigFieldsListener,
        // return. Otherwise, when the ConfigFieldsListener changes the
        // configurationComboBox's selected item to a value that matches
        // what the user has typed, this method will be triggered, which
        // will then try to change the DAO Fields, and an
        // IllegalStateException will be generated.
        if (inConfigFieldsListener)
            return;

        // This flag is checked by the ConfigFieldsListener.
        inConfigurationComboBoxListener = true;

        // No items - nothing selected - nothing to do.
        if (source.getItemCount() == 0 || daoPanelDB == null)
            return;

        // Database prefix in this config file is sql
        String prefix = source.getSelectedItem() + ".sql.";
        daoPanelDB.setUsername(CONFIGURATION_PARINFO.getItem(prefix + "username", ""));
        daoPanelDB.setPassword(CONFIGURATION_PARINFO.getItem(prefix + "password", ""));
        daoPanelDB.setInstance(CONFIGURATION_PARINFO.getItem(prefix + "instance", ""));
        daoPanelDB.setDriver(CONFIGURATION_PARINFO.getItem(prefix + "driver", ""));

        // Update the table definition table information as well
        getTableDefPanel().tableDefName.setText(CONFIGURATION_PARINFO.getItem(prefix + "tabledefinitiontable", ""));

        inConfigurationComboBoxListener = false;
    }

    /**
     * Listener that populates the dao panel text fields with information read in from the kbdb.cfg file when the user
     * changes what's selected in that ComboBox.
     */
    private class ConfigurationComboBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            configurationComboBoxActionPerformed((JComboBox) e.getSource());
        }
    }

    /**
     * Flag used to indicate when code inside the ConfigFieldsListener is being executed.
     */
    private boolean inConfigFieldsListener = false;

    /**
     * Listener that checks to see if the values being entered/modified in the DAO Panel fields (username, password,
     * instance, driver) correspond to a configuration defined in the CONFIGURATION_PARINFO. If they do, then show that
     * configuration name in the configurationComboBox.
     */
    private class ConfigFieldsListener extends DBUtilLibDocumentListener {
        /**
         * Gives notification that an attribute or set of attributes changed.
         *
         * @param e the document event
         */
        @Override
        public void listenersOnChangedUpdate(DocumentEvent e) {
            checkConfigFields();
        }

        /**
         * Gives notification that there was an insert into the document.
         *
         * @param e the document event
         */
        @Override
        public void listenersOnInsertUpdate(DocumentEvent e) {
            checkConfigFields();
        }

        /**
         * Gives notification that a portion of the document has been removed.
         *
         * @param e the document event
         */
        @Override
        public void listenersOnRemoveUpdate(DocumentEvent e) {
            checkConfigFields();
        }
    }

    /**
     * Check the configuration fields to see if their settings equal some settings in the configuration file.
     */
    protected void checkConfigFields() {
        // When the user selectes a configuration from the
        // ConfigurationComboBox, the ConfigurationComboBoxListener sets the
        // dao fields equal to the information in the configuration. If
        // this listener gets triggered when those fields are being set,
        // return since that listener needs to have the power to set those
        // fields uninterrupted.
        if (inConfigurationComboBoxListener || inConfigFieldsListener)
            return;

        // This flag is checked by the ConfigurationComboBoxListener.
        inConfigFieldsListener = true;
        // For each configurationPrefix, check if the values associated with
        // that prefix equal the values in the dao fields. If so, set
        // the configurationComboBox's selected item equal to that
        // configuration's name. Otherwise, set it to "Custom".
        for (String prefix : configurationPrefixes) {
            String p = prefix + ".sql.";

            // If the username, password, instance, driver, and
            // tabledefinitiontable match a configuration in the config
            // file, update the configuration combobox to reflect this.

            if (CONFIGURATION_PARINFO.getItem(p + "username").trim().equalsIgnoreCase(daoPanelDB.getUsername())
                    && CONFIGURATION_PARINFO.getItem(p + "password").trim().equalsIgnoreCase(daoPanelDB.getPassword())
                    && CONFIGURATION_PARINFO.getItem(p + "instance").trim().equalsIgnoreCase(daoPanelDB.getInstance())
                    && CONFIGURATION_PARINFO.getItem(p + "driver").trim().equalsIgnoreCase(daoPanelDB.getDriver())
                    && CONFIGURATION_PARINFO.getItem(p + "tabledefinitiontable").trim().equalsIgnoreCase(
                    getTableDefPanel().tableDefName.getText().trim())) {
                configurationComboBox.setSelectedItem(prefix);
                inConfigFieldsListener = false;
                return;
            }
        }
        configurationComboBox.setSelectedItem(CONFIGURATION_CUSTOM_ITEM);
        inConfigFieldsListener = false;
    }
}
