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
import javax.swing.filechooser.FileNameExtensionFilter;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Util;

/**
 * This class is a JPanel representation of an XML DAO (Data Access Object).
 */
@SuppressWarnings("serial")
public class DAOPanelXML extends JPanel {
    /**
     * XML File label
     */
    private JLabel xmlFileLabel;
    /**
     * XML File
     */
    private JTextField xmlFile;

    /**
     * XML File browse button
     */
    private JButton browseButton;

    /**
     * Constructor that creates a JPanel representing an XML DAO (Data Access Object).
     */
    public DAOPanelXML() {
        this.xmlFileLabel = new JLabel("XML File:");
        this.xmlFile = GUI_Util.textField_long();

        this.browseButton = GUI_Util.button_noInsets("Browse");
        browseButton.addActionListener(new XMLBrowseListener());

        setLayout(new GridBagLayout());
        // gbc param order: gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 2, 10), 0, 0);

        // Add components to the panel
        add(this.xmlFileLabel, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.xmlFile, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.browseButton, gbc);

        // set default tool tips
        setToolTips();
    }

    /**
     * Register GUI components specific to this DAOPanelXML with a ParInfoGui object.
     *
     * @param parInfoGui    ParInfoGui object to register components with. See
     *                      {@link ParInfoGui#registerComponent ParInfoGui.registerComponent} for more information.
     * @param parNamePrefix the prefix to prepend to parameter names before registering GUI components with parInfoGui
     * @param type          what type of {@link SchemaPanel SchemaPanel} typs this DAOPanelXML is part of.
     *                      Acceptable values are:
     *                      <br>{@link SchemaPanel#INPUT SchemaPanel.INPUT}
     *                      <br>{@link SchemaPanel#OUTPUT SchemaPanel.OUTPUT}
     *                      <br>{@link SchemaPanel#TARGET SchemaPanel.TARGET}
     */
    protected void registerComponents(ParInfoGui parInfoGui, String parNamePrefix, int type) {
        if (type == SchemaPanel.INPUT)
            parInfoGui.registerComponent(this.xmlFile, parNamePrefix + ParInfo.XML_INPUT_FILE);
        else
            parInfoGui.registerComponent(this.xmlFile, parNamePrefix + ParInfo.XML_OUTPUT_FILE);
    }

    /**
     * Update the ParInfoGui object with information from the gui components.
     *
     * @param parInfoGui ParInfoGui object with components to be synchronized.
     *                   See {@link ParInfoGui#synchParInfo ParInfoGui.synchParInfo} for more information.
     */
    protected void synchParInfo(ParInfoGui parInfoGui) {
        parInfoGui.synchParInfo(this.xmlFile);
    }

    /**
     * Set default tool tips
     */
    private void setToolTips() {
        String toolTipText = "XML file location";
        this.xmlFileLabel.setToolTipText(toolTipText);
        this.xmlFile.setToolTipText(toolTipText);

        toolTipText = "Browse for XML file location";
        this.browseButton.setToolTipText(toolTipText);
    }

    /**
     * Set the XML file in this panel
     *
     * @param xmlFile new XML File
     */
    protected void setXMLFile(String xmlFile) {
        if (xmlFile == null)
            this.xmlFile.setText("");
        else
            this.xmlFile.setText(xmlFile);
    }

    /**
     * Get the xmlFile from this panel
     *
     * @return the xmlFile from this panel
     */
    protected String getXMLFile() {
        return this.xmlFile.getText().trim();
    }

    /**
     * Add a listener to the XML file in this panel
     *
     * @param listener listener for xml file in this panel
     */
    protected void addXMLFileListener(DocumentListener listener) {
        this.xmlFile.getDocument().addDocumentListener(listener);
    }

    /**
     * This listener is activated when the user clicks on the Browse button.
     * It opens a window where the user can browse to an xml file.
     */
    protected class XMLBrowseListener implements ActionListener {
        /**
         * Respond to the user clicking on the browse button
         *
         * @param e event that triggered this listener
         */
        public void actionPerformed(ActionEvent e) {
            // Open the chooser in the same directory as whatever is in the
            // xmlfile text field
            JFileChooser chooser = new JFileChooser
                    (new File(getXMLFile()).getParent());

            // Filter to restricts the user to selecting xml files only
            FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files", "xml");
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);

            chooser.setDialogTitle("Browse for XML Files");
            chooser.showOpenDialog(null);

            File file = chooser.getSelectedFile();
            if (file != null)
                setXMLFile(file.toString());
        }
    }
}
