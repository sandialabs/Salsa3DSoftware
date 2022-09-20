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
import java.util.LinkedHashMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import gov.sandia.gnem.dbutillib.util.DBDefines;

/**
 * This class is a panel with radio buttons represnenting different types of
 * Table Defintion Tables.  When a user clicks on a radio button, the related
 * TableDefPanel will be made visible.
 * <p>
 * For example, say the following associations exists:
 * <br>"DB" radio button -> TableDefPanelDB
 * <br>"FF" radio button -> TableDefPanelFF
 *
 * <p>This class would make a panel that looks as follows:
 * <br><i>(Not functional ...)</i>
 * <table border="1"><tr><td>
 * <table><tr>
 * <td><input type="radio" name="group1" checked>DB</td>
 * <td><input type="radio" name="group1">FF</td>
 * </tr><tr align="center" height="75">
 * <td colspan="3"><i>TableDefPanelDB</i></td>
 * </tr></table>
 * </td></tr></table>
 * <p>Then, if the user clicked on the FF radio button, the TableDefPanelDB
 * would go away and the TableDefPanelFF would be shown instead.
 *
 * <p>This class currently can only display up to two different types of
 * TableDefPanels (typically DB and FF).  These TableDefPanels must all
 * have different DAO types.
 */
@SuppressWarnings("serial")
public class TableDefPanelChooser extends JPanel implements ActionListener {
    /**
     * Association between radio buttons and TableDefPanels.
     */
    private LinkedHashMap<JRadioButton, TableDefPanel> buttonToPanel;

    /**
     * JPanel that has all of the radio buttons in it
     */
    private JPanel buttonPanel;

    /**
     * DB radio button  - used for the selectDB() method.  May remain null.
     */
    private JRadioButton dbButton = null;
    /**
     * FF radio button - used for the selectFF() method.  May remain null.
     */
    private JRadioButton ffButton = null;

    /**
     * Keep track of which TableDefPanel is selected.
     */
    private TableDefPanel selectedTableDefPanel;

    /**
     * Constructor.  Radio buttons are created and associated with each
     * of the TableDefPanels - the radio button names are the associated
     * TableDefPanel's DAO Type.  Note that the only DAOTypes that are
     * recognized are the ones defined in DBDefines - other DAO Types and their
     * corresponding TableDefPanels, as well as XML DAO types (since XML
     * Table Definition Tables are no supported) will be ignored.  The order the
     * TableDefPanels are in in the tableDefPanels array is the order their
     * corresponding buttons will be displayed.
     * DB is the default selected panel type - if it exists.
     *
     * @param tableDefPanels an array of tableDefPanels to be shown in this
     *                       TableDefPanel chooser - up to two different TableDefPanels may be
     *                       specified and all of the TableDefPanels must have diffeerent DAO types
     */
    public TableDefPanelChooser(TableDefPanel[] tableDefPanels)
            throws DBDefines.FatalDBUtilLibException {
        String db = DBDefines.DATABASE_DAO;
        String ff = DBDefines.FF_DAO;

        // A LinkedHashMap preserves the order
        buttonToPanel = new LinkedHashMap<JRadioButton, TableDefPanel>();

        if (tableDefPanels.length > 2)
            throw new DBDefines.FatalDBUtilLibException("TableDefPanelChooser " +
                    "Error - too many (" + tableDefPanels.length + ") TableDefPanels " +
                    "specified.  A maximum of 2 is allowed");

        // Create associations between radio buttons and TableDefPanels
        for (TableDefPanel tdp : tableDefPanels) {
            String daoType = tdp.getDAOType();

            if (!daoType.equals(db) && !daoType.equals(ff))
                throw new DBDefines.FatalDBUtilLibException("TableDefPanelChooser " +
                        "Error - a TableDefPanel with a DAOType of " + daoType +
                        " was specified.  Only " + db + " and " + ff + " DAOTypes " +
                        "are allowed.");

            // Create the button
            JRadioButton button = new JRadioButton(tdp.getDAOType());
            // Associate the button with the TableDefPanel
            buttonToPanel.put(button, tdp);

            // Keep track of which dao type is represented by which button
            // so that we can provide selectDB() and selectFF() methods
            if (daoType.equals(DBDefines.DATABASE_DAO))
                dbButton = button;
            else if (daoType.equals(DBDefines.FF_DAO))
                ffButton = button;
        }

        this.selectedTableDefPanel = tableDefPanels[0];

        setLayout(new BorderLayout());

        // Add buttons
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        ButtonGroup buttonGroup = new ButtonGroup();

        for (JRadioButton button : buttonToPanel.keySet()) {
            buttonGroup.add(button);
            buttonPanel.add(button);
            button.addActionListener(this);
        }
        add(buttonPanel, BorderLayout.NORTH);


        // DB is the default selected.
        selectDB();

        // set default tool tips
        setToolTips();
    }

    /**
     * Select the radio button corresponding to the specified daoType. If the daoType specified is
     * not present in the TableDefPanelChooser, nothing is selected.
     *
     * @param daoType specifies which dao type radio button to select
     */
    public void select(String daoType) {
        if (daoType.equalsIgnoreCase(DBDefines.DATABASE_DAO))
            selectDB();
        else if (daoType.equalsIgnoreCase(DBDefines.FF_DAO))
            selectFF();
    }

    /**
     * Select the DB radio button, if there is one.
     */
    public void selectDB() {
        if (dbButton != null)
            dbButton.doClick();
    }

    /**
     * Select the FF radio button, if there is one.
     */
    public void selectFF() {
        if (ffButton != null)
            ffButton.doClick();
    }

    /**
     * Set default tool tips
     */
    private void setToolTips() {
        // database dao
        this.dbButton.setToolTipText("Select database table definition table");

        // flat file dao
        this.ffButton.setToolTipText("Select flat file table definition table");
    }

    /**
     * Return the currently selected TableDefPanel
     *
     * @return the currently selected TableDefPanel
     */
    public TableDefPanel getSelectedTableDefPanel() {
        return selectedTableDefPanel;
    }

    /**
     * Call registerComponents for each of the table def panels.
     *
     * @param parNamePrefix prefix to be prepend to parameter names in parInfoGui
     * @param parInfoGui    ParInfoGUI object to register GUI components with
     */
    public void registerComponents(String parNamePrefix, ParInfoGui parInfoGui) {
        for (TableDefPanel panel : buttonToPanel.values())
            panel.registerComponents(parNamePrefix, parInfoGui);
    }

    /**
     * When a radio button in this panel is clicked, display the related panel.
     *
     * @param e event that triggered the call to this method
     */
    public void actionPerformed(ActionEvent e) {
        String selected = ((JRadioButton) e.getSource()).getText();
        String tableDefName = this.selectedTableDefPanel.getTableDefName();

        // Remove the panels in order to add the new one.
        for (TableDefPanel panel : this.buttonToPanel.values())
            remove(panel);

        for (JRadioButton button : this.buttonToPanel.keySet()) {
            if (selected.equals(button.getText())) {
                // Add TableDefPanel associated with the button to the main panel.
                add(this.buttonToPanel.get(button), BorderLayout.CENTER);
                this.selectedTableDefPanel = this.buttonToPanel.get(button);
                this.selectedTableDefPanel.setTableDefName(tableDefName);
                break;
            }
        }
        repaint();
        revalidate();
    }
}
