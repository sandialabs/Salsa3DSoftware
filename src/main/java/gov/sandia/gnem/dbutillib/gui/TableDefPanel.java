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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import gov.sandia.gnem.dbutillib.ParInfo;

/**
 * This class is a GUI representation of Table Definition Table information.
 * As an abstract class, it cannot be instantiated - use one of its subclasses
 * instead.
 * <p>This class is intended to be contained within a
 * {@link SchemaPanel SchemaPanel}.
 */
public abstract class TableDefPanel extends JPanel {
    /**
     * Table Definition Table Name label
     */
    private JLabel tableDefNameLabel;
    /**
     * Table Definition Table Name
     */
    protected JTextField tableDefName;

    /**
     * Constructor.  This constructor creates the Table Definition Table Label
     * and text field and populates the dao information via a method
     * implemented by subclasses.
     */
    public TableDefPanel() {
        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.WEST, 0, new Insets(0, 0, 5, 0), 0, 0);
        setLayout(new GridBagLayout());

        this.tableDefNameLabel = new JLabel("Table Definition Table:");
        this.tableDefName = new JTextField();

        // Create this here so that we can access its width when setting the
        // size for the table definition table name text field.
        JPanel daoPanel = createDAOPanel();

        // We want the table definition table name text field to line up with
        // whatever is in the dao panel below it.  So, leave its height the same,
        // but set its width to the width of the dao panel - the width of the
        // table definition table label - 10 (for the rigid area).
        int width = 455;
        if (!daoPanel.getPreferredSize().equals(new Dimension(0, 0)))
            width = daoPanel.getPreferredSize().width - tableDefNameLabel.getPreferredSize().width - 10;
        tableDefName.setPreferredSize(new Dimension(width, tableDefName.getPreferredSize().height));

        // Add the table definition table label and text field to an inner panel
        JPanel tableDefNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tableDefNamePanel.add(tableDefNameLabel);
        tableDefNamePanel.add(Box.createRigidArea(new Dimension(10, 0)));
        tableDefNamePanel.add(tableDefName);

        add(tableDefNamePanel, gbc);
        gbc.gridy++;
        add(daoPanel, gbc);

        // set default tool tips
        setToolTips();
    }

    /**
     * Set default tool tips
     */
    private void setToolTips() {
        String toolTip = "Table definition table name";
        this.tableDefNameLabel.setToolTipText(toolTip);
        this.tableDefName.setToolTipText(toolTip);
    }

    /**
     * Return this TableDefPanel's DAO Type.  This is not implemented here since it
     * must be implemented by the subclasses so that it is specific to whatever
     * DAO the subclass represents.
     *
     * @return this TableDefPanel's DAO type
     */
    public abstract String getDAOType();

    /**
     * Get the table definition table name from this panel
     *
     * @return the table definition table name from this panel
     */
    protected String getTableDefName() {
        return this.tableDefName.getText().trim();
    }

    /**
     * Set the table definition table name in this panel
     *
     * @param tableDefName new table definition table name
     */
    protected void setTableDefName(String tableDefName) {
        if (tableDefName == null)
            this.tableDefName.setText("");
        else
            this.tableDefName.setText(tableDefName);
    }

    /**
     * Method to create the DAOPanel.  This is not implemented here since it must
     * be implemented by the subclasses so that it is specific to whatever DAO
     * the subclass represents.
     *
     * @return a JPanel representing this TableDefPanel's DAO
     */
    protected abstract JPanel createDAOPanel();

    protected void registerComponents(String parNamePrefix, ParInfoGui parInfoGui) {
        parInfoGui.registerComponent(this.tableDefName, parNamePrefix + ParInfo.TABLE_DEFINITION_TABLE);
        registerSubComponents(parNamePrefix, parInfoGui);
    }

    protected void synchParInfo(ParInfoGui parInfoGui) {
        parInfoGui.synchParInfo(this.tableDefName);
        synchSubParInfo(parInfoGui);
    }

    /**
     * Register GUI components with parInfoGui
     *
     * @param parNamePrefix prefix to be prepend to parameter names in parInfoGui
     * @param parInfoGui    ParInfoGUI object to register GUI components with
     */
    protected abstract void registerSubComponents(String parNamePrefix, ParInfoGui parInfoGui);

    /**
     * This method updates the ParInfoGui object with information from the gui components in the instantiating class.
     *
     * @param parInfoGui ParInfoGUI object to synch GUI components with
     */
    protected abstract void synchSubParInfo(ParInfoGui parInfoGui);
}
