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

import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;
import gov.sandia.gnem.dbutillib.util.RelationshipStruct;
import gov.sandia.gnem.dbutillib.util.TableStruct;

/**
 * This class represents a SchemaPanel with an XMLDAO.
 */
@SuppressWarnings("serial")
public class SchemaPanelXML extends SchemaPanel {
    /**
     * Handle to this class' DAOPanel
     */
    private DAOPanelXML daoPanelXML;

    /**
     * Create a SchemaPanelXML.  This constructor invokes the {@link SchemaPanel#SchemaPanel(int)
     * SchemaPanel(schemaPanelType)} constructor.  Please see that constructor's comments for more information.
     *
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(int) SchemaPanel(schemaPanelType)}
     * @throws FatalDBUtilLibException if an error occurs
     */

    public SchemaPanelXML(int schemaPanelType)
            throws FatalDBUtilLibException {
        super(schemaPanelType);
    }

    /**
     * Create a SchemaPanelXML.  This constructor invokes the {@link SchemaPanel#SchemaPanel(boolean, int)
     * SchemaPanel(modifiable, schemaPanelType)} constructor.  Please see that constructor's comments for more information.
     *
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(boolean, int) SchemaPanel(modifiable, schemaPanelType)}
     * @param modifiable      See {@link SchemaPanel#SchemaPanel(boolean, int) SchemaPanel(modifiable, schemaPanelType)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelXML(boolean modifiable, int schemaPanelType)
            throws FatalDBUtilLibException {
        super(modifiable, schemaPanelType);
    }

    /**
     * Create a SchemaPanelXML.  This constructor invokes the {@link SchemaPanel#SchemaPanel(int, ArrayList)
     * SchemaPanel(schemaPanelType, tables)} constructor.  Please see that constructor's comments for more information.
     *
     * @param schemaPanelType See {@link SchemaPanel#SchemaPanel(int, ArrayList) SchemaPanel(schemaPanelType, tables)}
     * @param tables          See {@link SchemaPanel#SchemaPanel(int, ArrayList) SchemaPanel(schemaPanelType, tables)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelXML(int schemaPanelType, ArrayList<TableStruct> tables)
            throws FatalDBUtilLibException {
        super(schemaPanelType, tables);
    }

    /**
     * Create a SchemaPanelXML.  This constructor invokes the {@link SchemaPanel#SchemaPanel(boolean, int, ArrayList)
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
    public SchemaPanelXML(boolean modifiable, int schemaPanelType, ArrayList<TableStruct> tables)
            throws FatalDBUtilLibException {
        super(modifiable, schemaPanelType, tables);
    }

    /**
     * Create a SchemaPanelXML.  This constructor invokes the
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
    public SchemaPanelXML(boolean modifiable, int schemaPanelType,
                          ArrayList<TableStruct> tables, ArrayList<RelationshipStruct> relationships)
            throws FatalDBUtilLibException {
        super(modifiable, schemaPanelType, tables, relationships);
    }

    /**
     * Create a SchemaPanelXML.  This constructor invokes the {@link SchemaPanel#SchemaPanel(ParInfo)
     * SchemaPanel(parInfo)} constructor.  Please see that constructor's comments for more information.
     *
     * @param parInfo See {@link SchemaPanel#SchemaPanel(ParInfo) SchemaPanel(parInfo)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelXML(ParInfo parInfo)
            throws FatalDBUtilLibException {
        super(parInfo);
    }

    /**
     * Create a SchemaPanelXML.  This constructor invokes the {@link SchemaPanel#SchemaPanel(ParInfo, String)
     * SchemaPanel(parInfo, prefix)} constructor.  Please see that constructor's comments for more information.
     *
     * @param parInfo See {@link SchemaPanel#SchemaPanel(ParInfo, String) SchemaPanel(parInfo, prefix)}
     * @param prefix  See {@link SchemaPanel#SchemaPanel(ParInfo, String) SchemaPanel(parInfo, prefix)}
     * @throws FatalDBUtilLibException if an error occurs
     */
    public SchemaPanelXML(ParInfo parInfo, String prefix)
            throws FatalDBUtilLibException {
        super(parInfo, prefix);
    }

    /**
     * Method to create the DAOPanel.
     * <br> Implementation of the {@link SchemaPanel#createDAOPanel createDAOPanel}
     * method that is abstract in {@link SchemaPanel SchemaPanel}
     *
     * @return JPanel representing this SchemaPanelXML's DAO information
     */
    @Override
    protected JPanel createDAOPanel() {
        daoPanelXML = new DAOPanelXML();

        return daoPanelXML;
    }

    protected void populate(ParInfo parInfo) {
        // Populate the text fields for this DAOPanel with information from the
        // parInfo object.
        if (type == INPUT)
            daoPanelXML.setXMLFile(parInfo.getItem(parNamePrefix + "XMLInputFile"));
        else
            daoPanelXML.setXMLFile(parInfo.getItem(parNamePrefix + "XMLOutputFile"));
    }

    /**
     * Return this SchemaPanelXML's DAO Type.
     * <br>Implementation of the {@link SchemaPanel#getDAOType getDAOType} method
     * that is abstract in {@link SchemaPanel SchemaPanel}
     *
     * @return this SchemaPanelXML's DAO type
     */
    @Override
    public String getDAOType() {
        return DBDefines.XML_DAO;
    }

    /**
     * Add items to the configuration combobox that are of type "xml"
     * (which is currently not a supported type for the configuration file).
     * <br>Implementation of the
     * {@link SchemaPanel#populateConfigurationComboBox populateConfigurationComboBox}
     * method that is abstract in {@link SchemaPanel SchemaPanel}
     *
     * @param prefixes the prefixes of the different configurations listed in
     *                 configuration file
     */
    @Override
    protected void populateConfigurationComboBox(TreeSet<String> prefixes) {
        // XML is not something representable in the configuration file
    }

    /**
     * Return an invisible button - xml files are not browsed for on a table
     * by table basis.  One xml file (located from the config panel, not from
     * the tables panel) contains information for all of the files.
     * <br>Implementation of the
     * {@link SchemaPanel#getTableBrowseButton getTableBrowseButton} method in
     * SchemaPanel
     *
     * @param tableType table type to browse for (ignored)
     * @return an invisible button
     */
    @Override
    protected JButton getTableBrowseButton(String tableType) {
        JButton button = new JButton();
        button.setVisible(false);
        return button;
    }

    /**
     * Set the current directory that should be prepended to the xml file name
     * <br>Implementation of the {@link SchemaPanel#setDirectory setDirectory} method;
     * for SchemaPanelDB, does nothing.
     *
     * @param currentDirectory directory that should be prepended to the xml file name
     */
    @Override
    public void setDirectory(String currentDirectory) {
        if (!this.daoPanelXML.getXMLFile().contains(currentDirectory))
            this.daoPanelXML.setXMLFile(currentDirectory + DBDefines.PATH_SEPARATOR +
                    this.daoPanelXML.getXMLFile());
    }

    /**
     * Register GUI components specific to this SchemaPanelXML with the ParInfoGui
     * object.  (The main GUI components for this SchemaPanelXML are primarily
     * handled in {@link SchemaPanel SchemaPanel}.  This method typically
     * handles the DAO GUI components.)
     * <br>Implementation of the
     * {@link SchemaPanel#registerSubComponents registerSubComponents} method.
     */
    @Override
    protected void registerSubComponents() {
        daoPanelXML.registerComponents(parInfoGui, parNamePrefix, type);
    }

    /**
     * Update the ParInfoGui object with information from the gui components.
     * <br>Implementation of the {@link SchemaPanel#synchSubParInfo synchSubParInfo} method.
     */
    @Override
    protected void synchSubParInfo() {
        // The components that must be synchronized are all in the DAOPanelDB
        daoPanelXML.synchParInfo(parInfoGui);
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
     * This code is called when the configurationComboBox listener is triggered.
     * It populates the dao panel text fields with information read
     * in from the kbdb.cfg file when the user changes what's selected in the
     * configurationComboBox.
     * This code is outside of the listener so that it can be called from
     * outside of the listener.
     * <br> Implementation of the {@link SchemaPanel#configurationComboBoxActionPerformed
     * configurationComboBoxActionPerformed} method that is abstract in
     * {@link SchemaPanel SchemaPanel}
     */
    @Override
    protected void configurationComboBoxActionPerformed(JComboBox source) {
        // Do nothing since the configuration files do not have an xml type
    }

}
