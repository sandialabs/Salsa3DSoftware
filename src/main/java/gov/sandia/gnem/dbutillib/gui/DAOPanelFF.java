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
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Util;

/**
 * This class is a JPanel representation of a Flat File DAO (Data Access Object).
 */
@SuppressWarnings("serial")
public class DAOPanelFF extends JPanel {
    /**
     * Flat File Path label
     */
    private JLabel flatFilePathLabel;
    /**
     * Flat File Path
     */
    private JTextField flatFilePath;
    /**
     * Flat File Path Browse button
     */
    private JButton browseButton;

    /**
     * Flat File Date Format Label
     */
    private JLabel dateFormatLabel;
    /**
     * Flat File Date Format
     */
    private JTextField dateFormat;

    /**
     * Constructor that creates a JPanel representing a Flat File DAO (Data Access Object).
     */
    public DAOPanelFF() {
        this(true);
    }

    /**
     * Constructor that creates a JPanel representing a Flat File DAO (Data Access Object).
     *
     * @param showDateFormat whether or not to show the date format label and text field
     */
    public DAOPanelFF(boolean showDateFormat) {
        this.flatFilePathLabel = GUI_Util.label_plain("Flat File Path:");
        this.flatFilePath = GUI_Util.textField_long();

        this.browseButton = GUI_Util.button_noInsets("Browse");
        this.browseButton.addActionListener(new FFBrowseListener());

        // gbc param order: gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
                GridBagConstraints.NONE, new Insets(0, 0, 2, 10), 0, 0);
        setLayout(new GridBagLayout());

        // Add components to the panel
        add(this.flatFilePathLabel, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.flatFilePath, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.browseButton, gbc);
        GUI_Util.gridBag_newRow(gbc);

        this.dateFormatLabel = GUI_Util.label_plain("Date Format:");
        this.dateFormat = GUI_Util.textField_medium();

        if (showDateFormat) {
            add(this.dateFormatLabel, gbc);
            GUI_Util.gridBag_right(gbc);
            add(this.dateFormat, gbc);
        }

        // set default tool tips
        setToolTips();
    }

    /**
     * Register GUI components specific to this DAOPanelFF with a ParInfoGui object.
     *
     * @param parInfoGui    ParInfoGui object to register components with. See {@link ParInfoGui#registerComponent
     *                      ParInfoGui.registerComponent} for more information.
     * @param parNamePrefix the prefix to prepend to parameter names before registering GUI components with parInfoGui
     */
    protected void registerComponents(ParInfoGui parInfoGui, String parNamePrefix) {
        parInfoGui.registerComponent(this.dateFormat, parNamePrefix + ParInfo.DATE_FORMAT);
    }

    /**
     * Update the ParInfoGui object with information from the gui components.
     *
     * @param parInfoGui ParInfoGui object with components to be synchronized. See {@link ParInfoGui#synchParInfo
     *                   ParInfoGui.synchParInfo} for more information.
     */
    protected void synchParInfo(ParInfoGui parInfoGui) {
        parInfoGui.synchParInfo(this.dateFormat);
    }

    /**
     * Set default tool tips
     */
    protected void setToolTips() {
        setFlatFilePathToolTipText("Flat file path directory");
        this.browseButton.setToolTipText("Browse for flat file path directory");
        setDateFormatToolTipText("Date format of flat file data (e.g. yy/MM/dd hh:mm:ss)");
    }

    /**
     * Set the flat file path in this panel
     *
     * @param flatFilePath new flat file path
     */
    protected void setFlatFilePath(String flatFilePath) {
        if (flatFilePath == null)
            this.flatFilePath.setText("");
        else
            this.flatFilePath.setText(flatFilePath);
    }

    /**
     * Get the flat file path from this panel
     *
     * @return the flat file path from this panel
     */
    protected String getFlatFilePath() {
        if (this.flatFilePath.getText().length() == 0)
            return System.getProperty("user.dir");
        return this.flatFilePath.getText().trim();
    }

    /**
     * Set flat file path tool tip text
     *
     * @param toolTipText flat file path tool tip text
     */
    protected void setFlatFilePathToolTipText(String toolTipText) {
        this.flatFilePathLabel.setToolTipText(toolTipText);
        this.flatFilePath.setToolTipText(toolTipText);
    }

    /**
     * Set the date format in this panel
     *
     * @param dateFormat new date format
     */
    protected void setDateFormat(String dateFormat) {
        if (dateFormat == null)
            this.dateFormat.setText("");
        else
            this.dateFormat.setText(dateFormat);
    }

    /**
     * Get the date format from this panel
     *
     * @return the date format from this panel
     */
    protected String getDateFormat() {
        // dateFormat might not always be initialized ... don't want an error
        // on the getText call.
        if (this.dateFormat == null)
            return null;
        return this.dateFormat.getText().trim();
    }

    /**
     * Set date format tool tip text
     *
     * @param toolTipText date format tool tip text
     */
    protected void setDateFormatToolTipText(String toolTipText) {
        if (this.dateFormatLabel != null)
            this.dateFormatLabel.setToolTipText(toolTipText);
        if (this.dateFormat != null)
            this.dateFormat.setToolTipText(toolTipText);
    }

    /**
     * Add a listener to the flat file path in this panel
     *
     * @param listener listener for flat file path in this panel
     */
    protected void addFlatFilePathListener(DocumentListener listener) {
        // It may seem to violate encapsulation to allow external code to add
        // a listener on a field in this class. However, SchemaPanelFF has a
        // configurationComboBox that must listen to this field and then
        // update the configuration accordingly. Thus, it seemed appropriate.
        this.flatFilePath.getDocument().addDocumentListener(listener);
    }

    /**
     * This listener is activated when the user clicks on the Browse button. It opens a window where the user can browse
     * to a file path.
     */
    protected class FFBrowseListener implements ActionListener {
        /**
         * Respond to the user clicking on the browse button
         *
         * @param e event that triggered this listener
         */
        public void actionPerformed(ActionEvent e) {
            // Open the chooser in the directory specified in filePath
            JFileChooser chooser = new JFileChooser(new File(getFlatFilePath()));

            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Browse for Flat File Path");
            int returnVal = chooser.showOpenDialog(null);
            if (returnVal != JFileChooser.APPROVE_OPTION)
                return;

            File file = chooser.getSelectedFile();
            if (file != null)
                setFlatFilePath(file.toString());
        }
    }
}
