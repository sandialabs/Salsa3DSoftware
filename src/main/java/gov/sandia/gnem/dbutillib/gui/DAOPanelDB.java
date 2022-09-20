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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Util;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * This class is a JPanel representation of a Database DAO (Data Access Object).
 */
@SuppressWarnings("serial")
public class DAOPanelDB extends JPanel {
    /**
     * Database Username label.
     */
    private JLabel usernameLabel;
    /**
     * Database Username
     */
    private JTextField username;

    /**
     * Database Password label.
     */
    private JLabel passwordLabel;
    /**
     * Database Password
     */
    private JPasswordField password;

    /**
     * Database Instance label.
     */
    private JLabel instanceLabel;
    /**
     * Database Instance
     */
    private JTextField instance;

    /**
     * Database Driver
     */
    private JTextField driver;
    /**
     * Database Driver label.
     */
    private JLabel driverLabel;

    /**
     * Database Index tablespace label
     */
    private JLabel indexTablespaceLabel;
    /**
     * Database Index tablespace
     */
    private JTextField indexTablespace;

    /**
     * Database Table tablespace label
     */
    private JLabel tableTablespaceLabel;
    /**
     * Database Table tablespace
     */
    private JTextField tableTablespace;

    /**
     * Test button to test the connection to the database.
     */
    protected JButton testButton;

    /**
     * Constructor that creates the JPanel representing a Database DAO (Data Access Object).
     */
    public DAOPanelDB() {
        // Create labels and initialize JTextFields
        this.usernameLabel = GUI_Util.label_plain("Username:");
        this.username = GUI_Util.textField_medium();

        this.passwordLabel = GUI_Util.label_plain("Password:");
        this.password = GUI_Util.passwordField_short();

        this.instanceLabel = GUI_Util.label_plain("Instance:");
        this.instance = GUI_Util.textField_medium();

        this.driverLabel = GUI_Util.label_plain("Driver:");
        this.driver = GUI_Util.textField_medium();

        this.indexTablespaceLabel = GUI_Util.label_plain("Index Tablespace:");
        this.indexTablespace = GUI_Util.textField_medium();

        this.tableTablespaceLabel = GUI_Util.label_plain("Table Tablespace:");
        this.tableTablespace = GUI_Util.textField_medium();

        this.testButton = GUI_Util.button_noInsets("Test");
        this.testButton.addActionListener(new TestActionListener());

        setLayout(new GridBagLayout());
        // gbc param order: gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.1, 0.1, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 2, 10), 0, 0);

        // Add components to the panel
        // Username & instance
        add(this.usernameLabel, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.username, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.instanceLabel, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.instance, gbc);

        GUI_Util.gridBag_right(gbc);
        gbc.gridheight = 2;
        GUI_Util.gridBag_centerAnchor(gbc);
        add(this.testButton, gbc);
        GUI_Util.gridBag_northwestAnchor(gbc);
        gbc.gridheight = 1;
        GUI_Util.gridBag_newRow(gbc);

        // Password & driver
        add(this.passwordLabel, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.password, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.driverLabel, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.driver, gbc);
        GUI_Util.gridBag_newRow(gbc);

        // Tablespace information
        add(this.indexTablespaceLabel, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.indexTablespace, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.tableTablespaceLabel, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.tableTablespace, gbc);

        // These are only shown if the user specifically requests it
        setTablespaceFieldsVisible(false);

        // set default tool tips
        setToolTips();
    }

    /**
     * Set whether or not the index and table tablespace fields are visible.
     *
     * @param visible whether or not the index and table tablespace fields are visible
     */
    protected void setTablespaceFieldsVisible(boolean visible) {
        this.indexTablespaceLabel.setVisible(visible);
        this.indexTablespace.setVisible(visible);
        this.tableTablespaceLabel.setVisible(visible);
        this.tableTablespace.setVisible(visible);
    }

    /**
     * Register GUI components specific to this DAOPanelDB with a ParInfoGui object.
     *
     * @param parInfoGui    ParInfoGui object to register components with. See
     *                      {@link ParInfoGui#registerComponent ParInfoGui.registerComponent} for more information.
     * @param parNamePrefix the prefix to prepend to parameter names before registering GUI components with parInfoGui
     */
    protected void registerComponents(ParInfoGui parInfoGui, String parNamePrefix) {
        parInfoGui.registerComponent(this.username, parNamePrefix + ParInfo.USERNAME);
        parInfoGui.registerComponent(this.password, parNamePrefix + ParInfo.PASSWORD);
        parInfoGui.registerComponent(this.instance, parNamePrefix + ParInfo.INSTANCE);
        parInfoGui.registerComponent(this.driver, parNamePrefix + ParInfo.DRIVER);
        parInfoGui.registerComponent(this.indexTablespace, parNamePrefix + ParInfo.INDEX_TABLESPACE);
        parInfoGui.registerComponent(this.tableTablespace, parNamePrefix + ParInfo.TABLE_TABLESPACE);
    }

    /**
     * Update the ParInfoGui object with information from the gui components.
     *
     * @param parInfoGui ParInfoGui object with components to be synchronized. See
     *                   {@link ParInfoGui#synchParInfo ParInfoGui.synchParInfo} for more information.
     */
    protected void synchParInfo(ParInfoGui parInfoGui) {
        parInfoGui.synchParInfo(this.username);
        parInfoGui.synchParInfo(this.password);
        parInfoGui.synchParInfo(this.instance);
        parInfoGui.synchParInfo(this.driver);
        parInfoGui.synchParInfo(this.indexTablespace);
        parInfoGui.synchParInfo(this.tableTablespace);
    }

    /**
     * Set default tool tips.
     */
    protected void setToolTips() {
        setUsernameToolTipText("Database username");
        setPasswordToolTipText("Database password");
        setInstanceToolTipText("Database instance");
        setDriverToolTipText("Database driver");
        setIndexTablespaceToolTipText("Database index tablespace");
        setTableTablespaceToolTipText("Database table tablespace");

        this.testButton.setToolTipText("Test database connection");
    }

    /**
     * Set the database username in this panel
     *
     * @param username new database username
     */
    protected void setUsername(String username) {
        if (username == null)
            this.username.setText("");
        else
            this.username.setText(username);
    }

    /**
     * Get the database username from this panel
     *
     * @return the database username from this panel
     */
    protected String getUsername() {
        return this.username.getText().trim();
    }

    /**
     * Set the database username tool tip.
     *
     * @param toolTipText database username tool tip text
     */
    protected void setUsernameToolTipText(String toolTipText) {
        this.usernameLabel.setToolTipText(toolTipText);
        this.username.setToolTipText(toolTipText);
    }

    /**
     * Set the database password in this panel
     *
     * @param password new database password
     */
    protected void setPassword(String password) {
        if (password == null)
            this.password.setText("");
        else
            this.password.setText(password);
    }

    /**
     * Get the database password from this panel
     *
     * @return the database password from this panel
     */
    protected String getPassword() {
        return String.valueOf(this.password.getPassword()).trim();
    }

    /**
     * Set the database password tool tip
     *
     * @param toolTipText database password tool tip text
     */
    protected void setPasswordToolTipText(String toolTipText) {
        this.passwordLabel.setToolTipText(toolTipText);
        this.password.setToolTipText(toolTipText);
    }

    /**
     * Set the database instance in this panel
     *
     * @param instance new database instance
     */
    protected void setInstance(String instance) {
        if (instance == null)
            this.instance.setText("");
        else
            this.instance.setText(instance);
    }

    /**
     * Get the database instance from this panel
     *
     * @return the database instance from this panel
     */
    protected String getInstance() {
        return this.instance.getText().trim();
    }

    /**
     * Set the database instance tool tip
     *
     * @param toolTipText database instance tool tip text
     */
    protected void setInstanceToolTipText(String toolTipText) {
        this.instanceLabel.setToolTipText(toolTipText);
        this.instance.setToolTipText(toolTipText);
    }

    /**
     * Set the database driver in this panel
     *
     * @param driver new database driver
     */
    protected void setDriver(String driver) {
        if (driver == null)
            this.driver.setText("");
        else
            this.driver.setText(driver);
    }

    /**
     * Get the database driver from this panel
     *
     * @return the database driver from this panel
     */
    protected String getDriver() {
        return this.driver.getText().trim();
    }

    /**
     * Set the database driver tool tip
     *
     * @param toolTipText database driver tool tip text
     */
    protected void setDriverToolTipText(String toolTipText) {
        this.driverLabel.setToolTipText(toolTipText);
        this.driver.setToolTipText(toolTipText);
    }

    /**
     * Set the database index tablespace in this panel
     *
     * @param indexTablespace new database index tablespace
     */
    protected void setIndexTablespace(String indexTablespace) {
        if (indexTablespace == null)
            this.indexTablespace.setText("");
        else
            this.indexTablespace.setText(indexTablespace);
    }

    /**
     * Get the database index tablespace from this panel
     *
     * @return the database index tablespace from this panel
     */
    protected String getIndexTablespace() {
        return this.indexTablespace.getText().trim();
    }

    /**
     * Set the database index tablespace tool tip
     *
     * @param toolTipText database index tablespace tool tip text
     */
    protected void setIndexTablespaceToolTipText(String toolTipText) {
        this.indexTablespaceLabel.setToolTipText(toolTipText);
        this.indexTablespace.setToolTipText(toolTipText);
    }

    /**
     * Set the database table tablespace in this panel
     *
     * @param tableTablespace new database table tablespace
     */
    protected void setTableTablespace(String tableTablespace) {
        if (tableTablespace == null)
            this.tableTablespace.setText("");
        else
            this.tableTablespace.setText(tableTablespace);
    }

    /**
     * Get the database table tablespace from this panel
     *
     * @return the database table tablespace from this panel
     */
    protected String getTableTablespace() {
        return this.tableTablespace.getText().trim();
    }

    /**
     * Set the database table tablespace tool tip
     *
     * @param toolTipText database table tablespace tool tip text
     */
    protected void setTableTablespaceToolTipText(String toolTipText) {
        this.tableTablespaceLabel.setToolTipText(toolTipText);
        this.tableTablespace.setToolTipText(toolTipText);
    }

    /**
     * Add a listener to the database username in this panel
     *
     * @param listener listener for the database username in this panel
     */
    protected void addUsernameListener(DocumentListener listener) {
        this.username.getDocument().addDocumentListener(listener);
    }

    /**
     * Add a listener to the database password in this panel
     *
     * @param listener listener for the database password in this panel
     */
    protected void addPasswordListener(DocumentListener listener) {
        this.password.getDocument().addDocumentListener(listener);
    }

    /**
     * Add a listener to the database instance in this panel
     *
     * @param listener listener for the database instance in this panel
     */
    protected void addInstanceListener(DocumentListener listener) {
        this.instance.getDocument().addDocumentListener(listener);
    }

    /**
     * Add a listener to the database driver in this panel
     *
     * @param listener listener for the database driver in this panel
     */
    protected void addDriverListener(DocumentListener listener) {
        this.driver.getDocument().addDocumentListener(listener);
    }

    /**
     * Listener that responds to the user clicking on the "Test" button which tests the connection to the database.
     */
    protected class TestActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ParInfo parInfo = new ParInfo();
            parInfo.addParameter(ParInfo.DAO_TYPE, DBDefines.DATABASE_DAO);
            parInfo.addParameter(ParInfo.USERNAME, getUsername());
            parInfo.addParameter(ParInfo.PASSWORD, getPassword());
            parInfo.addParameter(ParInfo.INSTANCE, getInstance());
            parInfo.addParameter(ParInfo.DRIVER, getDriver());

            try {
                new Schema(parInfo);
            } catch (FatalDBUtilLibException ex) {
                String message = "<html>Error connecting to the database<br>" +
                        ex.getMessage().replaceAll(DBDefines.EOLN, "\n").replaceAll("\n", "<br>") + "</html>";
                GUI_Util.optionPane_message(message, "Test Failed");
                return;
            }
            GUI_Util.optionPane_message("Successfully connected to the database!", "Test Passed");
        }
    }
}
