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

import javax.swing.JPanel;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.util.DBDefines;

/**
 * This class represents a TableDefPanel with a Database DAO.
 */
@SuppressWarnings("serial")
public class TableDefPanelDB extends TableDefPanel {
    /**
     * Handle to this class' DAOPanel
     */
    protected DAOPanelDB daoPanelDB;

    /**
     * Constructor.  This constructor creates the Table Definition Table Label
     * and text field for a DB Table Definition Table and populates the dao
     * information.
     */
    public TableDefPanelDB() {
        super();
    }

    /**
     * Return this TableDefPanel's DAO Type.
     * <br>Implementation of the {@link TableDefPanel#createDAOPanel createDAOPanel}
     * method that is abstract in {@link TableDefPanel TableDefPanel}
     *
     * @return this TableDefPanel's DAO Type
     */
    @Override
    public String getDAOType() {
        return DBDefines.DATABASE_DAO;
    }

    /**
     * Creates a DB DAO Panel for this TableDefPanelDB.
     * <br>Implementation of the {@link TableDefPanel#createDAOPanel createDAOPanel}
     * method that is abstract in {@link TableDefPanel TableDefPanel}
     *
     * @return JPanel representing this TableDefPanel's DAO information
     */
    @Override
    public JPanel createDAOPanel() {
        daoPanelDB = new DAOPanelDB();

        // set default tool tips
        setToolTips();
        return daoPanelDB;
    }

    /**
     * Register GUI components with parInfoGui
     *
     * @param parNamePrefix prefix to be prepend to parameter names in parInfoGui
     * @param parInfoGui    ParInfoGUI object to register GUI components with
     */
    @Override
    protected void registerSubComponents(String parNamePrefix, ParInfoGui parInfoGui) {
        daoPanelDB.registerComponents(parInfoGui, parNamePrefix + ParInfo.TABLE_DEFINITION_TABLE);
    }

    /**
     * This method updates the ParInfoGui object with information from the gui components in the instantiating class.
     *
     * @param parInfoGui ParInfoGUI object to synch GUI components with
     */
    @Override
    protected void synchSubParInfo(ParInfoGui parInfoGui) {
        daoPanelDB.synchParInfo(parInfoGui);
    }

    /**
     * Set default tool tips.
     */
    private void setToolTips() {
        String prefix = "Table definition table database ";

        // username
        this.daoPanelDB.setUsernameToolTipText(prefix + "username");

        // password
        this.daoPanelDB.setPasswordToolTipText(prefix + "password");

        // driver
        this.daoPanelDB.setDriverToolTipText(prefix + "driver");

        // instance
        this.daoPanelDB.setInstanceToolTipText(prefix + "instance");
    }
}
