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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.gui.util.DBUtilLibDocumentListener;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Constants;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Util;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;
import gov.sandia.gnem.dbutillib.util.RelationshipStruct;
import gov.sandia.gnem.dbutillib.util.TableStruct;

// Date of last review: 9/27/07 -- JEL

/**
 * This class represents a SchemaPanel with a Flat file DAO.
 */
@SuppressWarnings("serial")
public class SchemaPanelFF extends SchemaPanel {
    /**
     * Handle to this class's DAOPanel
     */
    private DAOPanelFF daoPanelFF;

    /**
     * Prefixes for flatfile configuration items in CONFIGURATION_PARINFO (a
     * {@link ParInfo ParInfo} object).  (See SchemaPanel's
     * {@link SchemaPanel#CONFIGURATION_PARINFO CONFIGURATION_PARINFO} comments
     * for more information.)
     * configurationPrefixes contains the items as they are displayed in the
     * configurationComboBox which are also used to access the information
     * related to that item from the {@link SchemaPanel#CONFIGURATION_PARINFO
     * CONFIGURATION_PARINFO} object.
     */
    private TreeSet<String> configurationPrefixes;

    /**
     * Create a SchemaPanelFF.  This constructor invokes the {@link SchemaPanel#SchemaPanel(int)
     * SchemaPanel(schemaPanelType)} constructor.  Please see that constructor's comments for more information.
     *
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(int) SchemaPanel(schemaPanelType)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelFF(int schemaPanelType)
            throws FatalDBUtilLibException {
        this(true, schemaPanelType, new ArrayList<TableStruct>(), new ArrayList<RelationshipStruct>());
    }

    /**
     * Create a SchemaPanelFF.  This constructor invokes the {@link SchemaPanel#SchemaPanel(boolean, int)
     * SchemaPanel(modifiable, schemaPanelType)} constructor.  Please see that constructor's comments for more information.
     *
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(boolean, int) SchemaPanel(modifiable, schemaPanelType)}
     * @param modifiable      See {@link SchemaPanel#SchemaPanel(boolean, int) SchemaPanel(modifiable, schemaPanelType)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelFF(boolean modifiable, int schemaPanelType)
            throws FatalDBUtilLibException {
        this(modifiable, schemaPanelType, new ArrayList<TableStruct>(), new ArrayList<RelationshipStruct>());
    }

    /**
     * Create a SchemaPanelFF.  This constructor invokes the {@link SchemaPanel#SchemaPanel(int, ArrayList)
     * SchemaPanel(schemaPanelType, tables)} constructor.  Please see that constructor's comments for more information.
     *
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(int, ArrayList) SchemaPanel(schemaPanelType, tables)}
     * @param tables          See {@link SchemaPanel#SchemaPanel(int, ArrayList) SchemaPanel(schemaPanelType, tables)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelFF(int schemaPanelType, ArrayList<TableStruct> tables)
            throws FatalDBUtilLibException {
        this(true, schemaPanelType, tables, new ArrayList<RelationshipStruct>());
    }

    /**
     * Create a SchemaPanelFF.  This constructor invokes the {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList)
     * SchemaPanel(modifiable, schemaPanelType, tables)} constructor.  Please see that constructor's comments for more
     * information.
     *
     * @param modifiable      See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList)
     *                        SchemaPanel(modifiable, schemaPanelType, tables)}
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList)
     *                        SchemaPanel(modifiable, schemaPanelType, tables)}
     * @param tables          See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList)
     *                        SchemaPanel(modifiable, schemaPanelType, tables)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelFF(boolean modifiable, int schemaPanelType, ArrayList<TableStruct> tables)
            throws FatalDBUtilLibException {
        this(modifiable, schemaPanelType, tables, new ArrayList<RelationshipStruct>());
    }

    /**
     * Create a SchemaPanelFF.  This constructor invokes the
     * {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList, ArrayList)
     * SchemaPanel(modifiable, schemaPanelType, tables, relationships)} constructor.  Please see that constructor's
     * comments for more information.
     *
     * @param modifiable      See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList, ArrayList)
     *                        SchemaPanel(modifiable, schemaPanelType, tables, relationships)}
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList, ArrayList)
     *                        SchemaPanel(modifiable, schemaPanelType, tables, relationships)}
     * @param tables          See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList, ArrayList)
     *                        SchemaPanel(modifiable, schemaPanelType, tables, relationships)}
     * @param relationships   See {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList, ArrayList)
     *                        SchemaPanel(modifiable, schemaPanelType, tables, relationships)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelFF(boolean modifiable, int schemaPanelType,
                         ArrayList<TableStruct> tables, ArrayList<RelationshipStruct> relationships)
            throws FatalDBUtilLibException {
        super(modifiable, schemaPanelType, tables, relationships);
        this.configurationPrefixes = new TreeSet<String>();
    }

    /**
     * Create a SchemaPanelFF.  This constructor invokes the {@link SchemaPanel#SchemaPanel(ParInfo)
     * SchemaPanel(parInfo)} constructor.  Please see that constructor's comments for more information.
     *
     * @param parInfo See {@link SchemaPanel#SchemaPanel(ParInfo) SchemaPanel(parInfo)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelFF(ParInfo parInfo)
            throws FatalDBUtilLibException {
        this(parInfo, "");
    }

    /**
     * Create a SchemaPanelFF.  This constructor invokes the {@link SchemaPanel#SchemaPanel(ParInfo, String)
     * SchemaPanel(parInfo, prefix)} constructor.  Please see that constructor's comments for more information.
     *
     * @param parInfo See {@link SchemaPanel#SchemaPanel(ParInfo, String) SchemaPanel(parInfo, prefix)}
     * @param prefix  See {@link SchemaPanel#SchemaPanel(ParInfo, String) SchemaPanel(parInfo, prefix)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelFF(ParInfo parInfo, String prefix)
            throws FatalDBUtilLibException {
        super(parInfo, prefix);
        this.configurationPrefixes = new TreeSet<String>();
    }

    /**
     * Method to create a flatfile DAOPanel for this SchemaPanelFF.
     * <br> Implementation of the {@link SchemaPanel#createDAOPanel createDAOPanel}
     * method that is abstract in {@link SchemaPanel SchemaPanel}
     *
     * @return JPanel representing this SchemaPanelFF's DAO information
     */
    @Override
    protected JPanel createDAOPanel() {
        this.daoPanelFF = new DAOPanelFF();

        // Add listeners that change the ConfigurationComboBox selected item
        // to the correct one based on what is in the dao fields.
        // This listener is handled in this class instead of in DAOPanelFF
        // since the listener involves the configurationComboBox as
        // well as the daoPanelFF information, so it's wiser to control from
        // this class.
        this.daoPanelFF.addFlatFilePathListener(new ConfigFieldsListener());

        // Populate the text fields for this DAOPanel with information from the
        // parInfo object.

        return this.daoPanelFF;
    }

    protected void populate(ParInfo parInfo) {
        this.daoPanelFF.setDateFormat(parInfo.getItem(parNamePrefix + "DateFormat"));
        if (this.daoPanelFF.getDateFormat().length() == 0)
            this.daoPanelFF.setDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * Return this SchemaPanelFF's DAO Type.
     * <br>Implementation of the {@link SchemaPanel#getDAOType getDAOType} method
     * that is abstract in {@link SchemaPanel SchemaPanel}
     *
     * @return this SchemaPanelFF's DAO type
     */
    @Override
    public String getDAOType() {
        return DBDefines.FF_DAO;
    }

    /**
     * Add items to the configuration combobox that are of type
     * {@link GUI_Constants#CONFIG_FLATFILE flatfile}. This method also adds a
     * configuration combobox listener.
     * <br>Implementation of the
     * {@link SchemaPanel#populateConfigurationComboBox populateConfigurationComboBox}
     * method that is abstract in {@link SchemaPanel SchemaPanel}
     *
     * @param prefixes the prefixes of the different configurations listed in
     *                 configuration file
     */
    @Override
    protected void populateConfigurationComboBox(TreeSet<String> prefixes) {
        this.configurationPrefixes.clear();

        // Add flatfile configuration items to the configurationComboBox
        for (String prefix : prefixes) {
            // If the type specified in the config file for this prefix is
            // flatfile, add the prefix to configurationPrefixes
            String prefixType = GUI_Util.getConfigType(CONFIGURATION_PARINFO, prefix);

            if (prefixType.equals(GUI_Constants.CONFIG_FLATFILE))
                configurationPrefixes.add(prefix);
        }

        // Add flat file items to the configurationCombobox
        for (String prefix : configurationPrefixes)
            configurationComboBox.addItem(prefix);

        // Set up the configurationComboBox with a listener.
        configurationComboBox.addActionListener(new ConfigurationComboBoxListener());
    }

    /**
     * Return a button that looks for flatfile table names as files in the file system.
     * <br>Implementation of the
     * {@link SchemaPanel#getTableBrowseButton getTableBrowseButton} method in
     * SchemaPanel
     *
     * @param tableType table type to browse for
     * @return a button that looks for flatfile table names as files in the file system.
     */
    @Override
    protected JButton getTableBrowseButton(String tableType) {
        JButton button = GUI_Util.button_noInsets("Browse");
        button.addActionListener(new BrowseFileSystemListener(tableType));
        button.setToolTipText("Browse for " + tableType + " tables in the file system");
        return button;
    }

    /**
     * Set the current directory that should be prepended to flat file names
     *
     * @param currentDirectory directory that should be prepended to flat file names
     */
    @Override
    public void setDirectory(String currentDirectory) {
        if (!this.daoPanelFF.getFlatFilePath().contains(currentDirectory))
            this.daoPanelFF.setFlatFilePath(currentDirectory);
        for (GUITableStruct tableStruct : getTables()) {
            JTextField tableField = tableStruct.tableName;
            String tableName = tableField.getText();
            if (!tableName.contains(currentDirectory))
                tableStruct.tableName.setText(currentDirectory + DBDefines.PATH_SEPARATOR + tableName);
        }
    }

    /**
     * Register GUI components specific to this SchemaPanelFF with the ParInfoGui
     * object.  (The main GUI components for this SchemaPanelFF are primarily
     * registered in {@link SchemaPanel SchemaPanel}.)
     * <br>Implementation of the
     * {@link SchemaPanel#registerSubComponents registerSubComponents} method.
     */
    @Override
    protected void registerSubComponents() {
        // The components that must be registered are all in the DAOPanelFF
        this.daoPanelFF.registerComponents(parInfoGui, parNamePrefix);
    }

    /**
     * Update the ParInfoGui object with information from the gui components.
     * <br>Implementation of the {@link SchemaPanel#synchSubParInfo synchSubParInfo} method.
     */
    @Override
    protected void synchSubParInfo() {
        // The components that must be synchronized are all in the DAOPanelFF
        this.daoPanelFF.synchParInfo(parInfoGui);
    }

    /**
     * Refresh visibility on components specific to this class.  See
     * {@link SchemaPanel#refreshVisibility SchemaPanel.refreshVisibility} for
     * more information.
     * <br>Implementation of the
     * {@link SchemaPanel#refreshSubVisibility refreshSubVisibility} method.
     */
    @Override
    protected void refreshSubVisibility() {
    }

    ;

    /**
     * Flag used to indicate when code inside the configurationComboBoxActionPerformed
     * is being executed.  Having this flag prevents:
     * a) the configurationComboBox from activating itself or
     * b) the ConfigFieldsListener from activating configurationComboBoxActionPerformed
     * while things are being initialized.
     */
    private boolean inConfigurationComboBoxListener = false;

    /**
     * This code is called when the configurationComboBox listener is triggered by
     * the user clicking on the configuration that they want.  It populates the dao
     * panel with information read in from the configuration file for the specified
     * configuration.  This code is called by the listener, but should remain outside
     * of the listener since it is also called from other places.
     * <br> Implementation of the {@link SchemaPanel#configurationComboBoxActionPerformed
     * configurationComboBoxActionPerformed} method that is abstract in
     * {@link SchemaPanel SchemaPanel}
     */
    @Override
    protected void configurationComboBoxActionPerformed(JComboBox source) {
        // The ConfigFieldsListener changes the selected item in the
        // configurationComboBox based on changes the user makes to the dao
        // fields, which is the activity this listener listens for.  If this
        // listener is triggered by the ConfigFieldsListener, return.  Failing to
        // return in this scenario causes an IllegalStateException to be generated
        // since the ConfigFieldsListener triggers this method which triggers the
        // ConfigFieldsListener and so on ...
        if (this.inConfigFieldsListener)
            return;

        // This flag is checked by the ConfigFieldsListener.
        this.inConfigurationComboBoxListener = true;

        // No items - nothing selected - nothing to do.
        if (source.getItemCount() == 0 || this.daoPanelFF == null)
            return;

        // Retrieve the flatfile path from the CONFIGURATION_PARINFO that was
        // selected and set the daoPanelFF flatfile path value to that.
        // (source.getSelectedItem() returns the prefix used to access the
        // information in the CONFIGURATION_PARINFO object for the selected
        // configuration.)
        String selectedConfigPrefix = source.getSelectedItem().toString();
        String path = GUI_Util.getConfigFlatFilePath(CONFIGURATION_PARINFO, selectedConfigPrefix);
        this.daoPanelFF.setFlatFilePath(path);

        this.inConfigurationComboBoxListener = false;
    }

    /**
     * Listener triggered when the configurationComboBox listener is triggered by
     * the user clicking on the configuration that they want.  It populates the dao
     * panel with information read in from the configuration file for the specified
     * configuration.
     */
    private class ConfigurationComboBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            configurationComboBoxActionPerformed((JComboBox) e.getSource());
        }
    }

    /**
     * Flag used to indicate when code inside the ConfigFieldsListener
     * is being executed.  Having this flag prevents:
     * a) the ConfigFieldsListener from activating itself or
     * b) the configurationComboBoxActionPerformed method from calling it while
     * things are being initialized.
     */
    private boolean inConfigFieldsListener = false;

    /**
     * Listener that checks to see if the values being entered/modified in the
     * DAO Panel fields (flat file path) correspond to a configuration defined
     * in the CONFIGURATION_PARINFO.  If they do, then show that configuration
     * name in the configurationComboBox.
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
     * Check the configuration fields to see if their settings equal some
     * settings in the configuration file.  If they are, update the
     * configurationComboBox to show that setting as the selected setting
     */
    protected void checkConfigFields() {
        // When the user selects a configuration from the
        // ConfigurationComboBox, the ConfigurationComboBoxListener sets the
        // dao fields equal to the information in the configuration.  If
        // this listener gets triggered when those fields are being set,
        // return since that listener needs to have the power to set those
        // fields uninterrupted.
        if (this.inConfigurationComboBoxListener)
            return;

        // This flag is checked by the configurationComboBoxActionPerformed.
        this.inConfigFieldsListener = true;

        // Flag to track if the custom item should be displayed in the configurationComboBox
        boolean useCustomItem = true;

        // For each configurationPrefix, check if the values associated with
        // that prefix equal the values in the dao fields.  If so, set
        // the configurationComboBox's selected item equal to that
        // configuration's name.  Otherwise, set it to "Custom".
        // configurationPrefixes is actually the items displayed in the
        // configurationComboBox in addition to being used to access the
        // information related to that item
        for (String configPrefix : configurationPrefixes) {
            // configurationPrefixes contains the items as they are displayed in the
            // configurationComboBox which are also used to access the information
            // related to that item from the CONFIGURATION_PARINFO object

            // Create the string necessary to access the flatfile path information
            // associated with the current configPrefix
            String configFlatFilePath = GUI_Util.getConfigFlatFilePath(CONFIGURATION_PARINFO, parNamePrefix);

            // If the flatfile matches a configuration in CONFIGURATION_PARINFO,
            // update the configuration combobox to reflect this.
            if (configFlatFilePath.equalsIgnoreCase(this.daoPanelFF.getFlatFilePath())) {
                configurationComboBox.setSelectedItem(configPrefix);
                useCustomItem = false;
                break;
            }
        }

        // What has been entered in the fields does not match a pre-set configuration.
        // Set the configurationComboBox selection to the custom.
        if (useCustomItem)
            configurationComboBox.setSelectedItem(CONFIGURATION_CUSTOM_ITEM);

        this.inConfigFieldsListener = false;
    }

    /**
     * Listener that is activated when the user clicks on the "Browse" button.
     * The user does this to specify a file as the "table name" for a flatfile
     * table type.  This listener opens a file browser window open to the path
     * specified in the daoPanelFF's flatFilePath field.
     */
    private class BrowseFileSystemListener implements ActionListener {
        /**
         * The table type being browsed for
         */
        private String tableType;

        /**
         * Constructor.
         *
         * @param tableType tableType for the flat file being browsed for
         */
        public BrowseFileSystemListener(String tableType) {
            this.tableType = tableType;
        }

        /**
         * Activated when the user clicks on the "Browse" button for a particular
         * table type.  Open's up a file browser window where the user can navigate
         * to the flat file for the specified table type.
         *
         * @param e triggering action event
         */
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser(new File(daoPanelFF.getFlatFilePath()));

            chooser.setDialogTitle("Browse for " + tableType + " table");
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal != JFileChooser.APPROVE_OPTION)
                return;

            File file = chooser.getSelectedFile();
            // By selecting this file, the user is specifying the "table name" 
            // for a flat file table type.  
            if (file != null)
                setTableName(tableType, file.toString());
        }
    }
}
