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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import gov.sandia.gnem.dbutillib.gui.SchemaPanel.GUITableStruct;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;
import gov.sandia.gnem.dbutillib.util.RelationshipStruct;

/**
 * This class is a panel with radio buttons representing different types of Schemas. When a user clicks on a radio
 * button, the related schema will be made visible.
 * <p>
 * This class currently can only display up to three different types of SchemaPanels (typically: DB, FF, and XML). All
 * DAO types represented within a SchemaPanelChooser must be unique
 */
@SuppressWarnings("serial")
public class SchemaPanelChooser extends JPanel implements ActionListener {
    /**
     * Association between radio button names and the SchemaPanel that should be displayed when that radio button is
     * clicked.
     */
    private LinkedHashMap<String, SchemaPanel> buttonNameToPanel;
    /**
     * Keep track of the buttons that are on each SchemaPanel so that we can retrieve them and do things like call
     * doClick() or setSelected().
     */
    private HashMap<SchemaPanel, HashMap<String, JRadioButton>> panelToButtons;
    /**
     * The currently selected SchemaPanel.
     */
    private SchemaPanel selectedSchemaPanel;

    /**
     * Constructor. Radio buttons are created and associated with each of the SchemaPanels. Note that the only DAOTypes
     * that are recognized are the ones defined in DBDefines - other DAO Types and their corresponding SchemaPanels will
     * be ignored. The order the SchemaPanels are in in the schemaPanels array is the order their corresponding buttons
     * will be displayed. DB is the default selected panel type - if it exists.
     *
     * @param schemaPanels an array of SchemaPanels to be shown in this SchemaPanel chooser - up to three different
     *                     SchemaPanels may be specified, and all of the schemaPanels must have different DAO types
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelChooser(SchemaPanel[] schemaPanels) throws FatalDBUtilLibException {
        // Check that only a max of 3 panels are specified
        if (schemaPanels.length > 3)
            throw new DBDefines.FatalDBUtilLibException("SchemaPanelChooser " + "Error - too many ("
                    + schemaPanels.length + ") SchemaPanels " + "specified.  A maximum of 3 is allowed");

        this.buttonNameToPanel = new LinkedHashMap<String, SchemaPanel>();
        this.panelToButtons = new HashMap<SchemaPanel, HashMap<String, JRadioButton>>();

        // Check that DAO Types are valid and create associations between button names and the schemaPanel
        // they correspond to.
        ArrayList<String> daoTypes = new ArrayList<String>();
        for (SchemaPanel sp : schemaPanels) {
            sp.setContainedInSchemaPanelChooser();
            String daoType = sp.getDAOType();
            if (!daoType.equals(DBDefines.DATABASE_DAO) && !daoType.equals(DBDefines.FF_DAO)
                    && !daoType.equals(DBDefines.XML_DAO))
                throw new DBDefines.FatalDBUtilLibException("SchemaPanelChooser Error - a SchemaPanel with a "
                        + "DAOType of " + daoType + " was specified.  Only " + DBDefines.DATABASE_DAO + ", "
                        + DBDefines.FF_DAO + ", and " + DBDefines.XML_DAO + " DAOTypes are allowed.");

            daoTypes.add(daoType);
            this.buttonNameToPanel.put(daoType, sp);

            // Remove the tableDefToggle listeners so that the SchemaPanelChooser can handle events on these
            // buttons. The events are handled essentially the same way that they are handled in SchemaPanel.
            // The difference with SchemaPanelChoosers is that when one table definition panel is set to visible,
            // all table definition table panels for all SchemaPanels are set to visible. It seems like if
            // the user wants that area visible, they want it visible. Users don't know about the different
            // SchemaPanels in the background - they just see one GUI where they expect uniformity.
            for (ActionListener actionListener : sp.tableDefToggle.getActionListeners())
                sp.tableDefToggle.removeActionListener(actionListener);
            sp.tableDefToggle.addActionListener(new TableDefToggleListener());
        }

        if (daoTypes.size() != schemaPanels.length)
            throw new DBDefines.FatalDBUtilLibException("SchemaPanelChooser Error - duplicate daoTypes specified "
                    + "in the SchemaPanels.  DAOTypes specified:\n\t" + daoTypes);

        // Add the button panels to the SchemaPanels. Done here when all SchemaPanels are known about in
        // buttonNameToPanel
        for (SchemaPanel sp : schemaPanels)
            sp.setSchemaPanelChooserButtonPanel(createButtonPanel(sp));

        // All of the configuration comboBoxes for all of the SchemaPanels in this SchemaPanelChooser will have the same
        // items.
        // updateConfigurationComboBoxItems();

        this.selectedSchemaPanel = schemaPanels[0];
        setLayout(new BorderLayout());
        setToolTips();
    }

    /**
     * Create the JPanel that has the radio buttons that when clicked will switch out which SchemaPanel is displayed.
     * Each SchemaPanel must have its own buttons and panel since different panels can't share the same component.
     *
     * @param sp the SchemaPanel that this method is creating the buttonPanel for. Needed so that we can add information
     *           to panelToButtons
     * @return JPanel with radio buttons, that, when clicked, will update this panel to display the related SchemaPanel
     */
    private JPanel createButtonPanel(SchemaPanel sp) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        ButtonGroup buttonGroup = new ButtonGroup();
        HashMap<String, JRadioButton> buttonNameToButton = new HashMap<String, JRadioButton>();

        // Add buttons to button group and buttons panel
        for (String buttonName : buttonNameToPanel.keySet()) {
            JRadioButton button = new JRadioButton(buttonName);
            buttonGroup.add(button);
            buttonPanel.add(button);
            button.addActionListener(this);
            buttonNameToButton.put(buttonName, button);
        }

        this.panelToButtons.put(sp, buttonNameToButton);
        return buttonPanel;
    }

    /**
     * Select the panel with the specified DAO.
     *
     * @param daoType dao type of the panel to be selected
     */
    public void select(String daoType) {
        if (buttonNameToPanel.get(daoType) != null)
            panelToButtons.get(buttonNameToPanel.get(daoType)).get(daoType).doClick();
    }

    /**
     * Set default tool tips
     */
    private void setToolTips() {
        // Retrieve the actual buttons
        for (SchemaPanel sp : panelToButtons.keySet()) {
            for (JRadioButton button : panelToButtons.get(sp).values()) {
                if (button.getText().equals(DBDefines.DATABASE_DAO))
                    button.setToolTipText("Select database configuration");
                else if (button.getText().equals(DBDefines.FF_DAO))
                    button.setToolTipText("Select flat file configuration");
                else
                    button.setToolTipText("Select XML configuration");
            }
        }
    }

    /**
     * Return the currently selected SchemaPanel.
     *
     * @return the currently selected SchemaPanel
     */
    public SchemaPanel getSelectedSchemaPanel() {
        return selectedSchemaPanel;
    }

    /**
     * Return the SchemaPanels in this SchemaPanelChooser.
     *
     * @return SchemaPanels in this SchemaPanelChooser
     */
    public ArrayList<SchemaPanel> getSchemaPanels() {
        ArrayList<SchemaPanel> schemaPanels = new ArrayList<SchemaPanel>();
        for (SchemaPanel sp : panelToButtons.keySet())
            schemaPanels.add(sp);
        return schemaPanels;
    }

    /**
     * When a radio button in this panel is clicked, display the related panel.
     *
     * @param e event that triggered the call to this method
     */
    public void actionPerformed(ActionEvent e) {
        String selected = ((JRadioButton) e.getSource()).getText();
        ArrayList<GUITableStruct> tables = new ArrayList<GUITableStruct>();
        ArrayList<GUITableStruct> specialTables = new ArrayList<GUITableStruct>();
        ArrayList<RelationshipStruct> relationships = new ArrayList<RelationshipStruct>();

        if (selectedSchemaPanel != null) {
            for (GUITableStruct gts : selectedSchemaPanel.getTables())
                tables.add(gts);
            relationships = selectedSchemaPanel.getRelationships();
            for (GUITableStruct gts : selectedSchemaPanel.getSpecialTables())
                specialTables.add(gts);
        }

        // Remove the panels in order to add the new one.
        for (JPanel panel : buttonNameToPanel.values())
            remove(panel);

        for (String buttonName : buttonNameToPanel.keySet()) {
            if (!selected.equals(buttonName))
                continue;

            SchemaPanel schemaPanel = buttonNameToPanel.get(buttonName);
            selectedSchemaPanel = buttonNameToPanel.get(buttonName);
            selectedSchemaPanel.setAllTablesRelationships(tables, specialTables, relationships);

            add(schemaPanel, BorderLayout.CENTER);

            // Make sure the button that was just selected shows up as
            // selected in the buttons panel of the newly selected
            // SchemaPanel.
            panelToButtons.get(schemaPanel).get(buttonName).setSelected(true);

            // See if the change to the dao panel triggers a change in the
            // configuration combobox
            if (schemaPanel.getDAOType().equals(DBDefines.DATABASE_DAO))
                ((SchemaPanelDB) schemaPanel).checkConfigFields();

            else if (schemaPanel.getDAOType().equals(DBDefines.FF_DAO))
                ((SchemaPanelFF) schemaPanel).checkConfigFields();

            // If a user clicks on a different DAO, then they should see
            // right away that they got the right one.
            schemaPanel.setConnectionVisible(true);

            break;
        }
        repaint();
        revalidate();
    }

    /**
     * When one table definition panel is set to visible, all table definition table panels for all SchemaPanels are set
     * to visible. It seems like if the user wants that area visible, they want it visible. Users don't know about the
     * different SchemaPanels in the background - they just see one GUI where they expect uniformity.
     */
    private class TableDefToggleListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JToggleButton toggleButton = (JToggleButton) e.getSource();

            // The daoToggle has the hide text on it. The user clicked
            // it to hide connection information.
            if (toggleButton.getText().equals(SchemaPanel.HIDE_TABLE_DEF_TEXT))
                for (SchemaPanel sp : buttonNameToPanel.values())
                    sp.setTableDefVisible(false);
            else
                // The daoToggle has the show text on it. The user clicked
                // it to show connection information.
                for (SchemaPanel sp : buttonNameToPanel.values())
                    sp.setTableDefVisible(true);
        }
    }
}
// /**
// * All of the schema panels in this SchemaPanelChooser should have the same configuration combobox items in
// * their respective configurationComboBoxes.
// */
// protected void populateConfigurationComboBoxItems()
// {
// // Create a set of all of the configuration items from all of the SchemaPanels and add those items to configParInfo.
// // Also remove configurationComboBox listeners.
// TreeSet<Object> allItems = new TreeSet<Object>();
// ParInfo configParInfo = null;
//    
// for (SchemaPanel sp : buttonNameToPanel.values())
// {
// for (int i = 0; i < sp.configurationComboBox.getItemCount(); i++)
// allItems.add(sp.configurationComboBox.getItemAt(i));
//
// if (configParInfo == null)
// configParInfo = SchemaPanel.CONFIGURATION_PARINFO;
// else
// // Add any new parameters from the current schemaPanel
// configParInfo.updateParameters(SchemaPanel.CONFIGURATION_PARINFO);
//
// // Remove the configurationComboBox ActionListeners so that this class can handle the listening
// // instead. That way, when configurations are selected that are different than the currently
// // being displayed dao type, the switching between panel types will be handled properly.
// for (ActionListener al : sp.configurationComboBox.getActionListeners())
// sp.configurationComboBox.removeActionListener(al);
// }
//    
// // Add missing items to the configurationComboBoxes of all of the SchemaPanels. Set all SchemaPanel's
// // configurationParInfos to configParInfo which has the union of all of the SchemaPanel's configurationParInfos.
// // Add configurationComboBox listeners. Do this after adding items so that there aren't events firing left and right.
// Object[] allItemsArray = allItems.toArray(new Object[]{});
//    
// for (SchemaPanel sp : buttonNameToPanel.values())
// {
// SchemaPanel.CONFIGURATION_PARINFO = configParInfo;
//        
// // This SchemaPanel's items.
// TreeSet<Object> items = new TreeSet<Object>();
// for (int i = 0; i < sp.configurationComboBox.getItemCount(); i++)
// items.add(sp.configurationComboBox.getItemAt(i));
//
// // Add items that are in other SchemaPanels not yet present in
// // the current SchemaPanel
// for (int i = 0; i < allItemsArray.length; i++)
// {
// if (items.contains(allItemsArray[i]))
// continue;
// sp.configurationComboBox.insertItemAt(allItemsArray[i], i);
// }
// sp.configurationComboBox.addActionListener(new ConfigurationComboBoxListener());
// }
// }
//
// /**
// * Whether or not the code is currently in the ConfigurationComboBoxListener
// * This is handy to know since sometimes the code in the listener causes the
// * listener to be called again, which is not the desired behavior
// */
// boolean inListener = false;
//
// /**
// * Listener that populates the dao panel text fields with information read
// * in from the kbdb.cfg file when the user changes what's selected in that
// * ComboBox.
// */
// private class ConfigurationComboBoxListener implements ActionListener
// {
// public void actionPerformed(ActionEvent e)
// {
// // If this listener was triggered by itself, then return.
// if (inListener)
// return;
//
// inListener = true;
//        
// JComboBox source = (JComboBox) e.getSource();
// // No items - nothing selected - nothing to do.
// if (source.getItemCount() == 0)
// return;
//
// // Retrieve the correct configuration information from the
// // CONFIGURATION_PARINFO.
// ParInfo configParInfo = selectedSchemaPanel.CONFIGURATION_PARINFO;
// String selectedItem = source.getSelectedItem().toString();
//        
// String type = configParInfo.getItem(selectedItem + ".type");
// String prefix = selectedItem + "." + type + ".";
//        
// // Select the correct configuration
// if (type != null)
// {
// if (type.equals("sql"))
// select(DBDefines.DATABASE_DAO);
// else
// select(DBDefines.FF_DAO);
// selectedSchemaPanel.configurationComboBox.setSelectedItem(selectedItem);
// selectedSchemaPanel.configurationComboBoxActionPerformed(selectedSchemaPanel.configurationComboBox);
// }
// inListener = false;
// }
// }
