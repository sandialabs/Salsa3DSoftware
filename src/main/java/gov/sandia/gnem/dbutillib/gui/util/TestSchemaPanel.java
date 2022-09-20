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
package gov.sandia.gnem.dbutillib.gui.util;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.gui.ParInfoGui;
import gov.sandia.gnem.dbutillib.gui.SchemaPanel;
import gov.sandia.gnem.dbutillib.gui.SchemaPanelDB;

public class TestSchemaPanel {
    public static void main(String[] args) {
        try {
            // Tool Tip Checking - create schemas with all sorts of options
            ParInfo parInfo = new ParInfo();
            // Type
            parInfo.addParameter(ParInfoGui.SCHEMA_PANEL_TYPE, String.valueOf(SchemaPanel.OUTPUT));
            parInfo.addParameter(ParInfoGui.MODIFIABLE, "true");
            parInfo.addParameter(ParInfoGui.TABLES, "origin \narrival\nassoc\nevent");
            // Create the schemaPanel
            SchemaPanel schemaPanel = new SchemaPanelDB(parInfo);
            schemaPanel.setVisible(true);
            //schemaPanel.setIDGapsTableVisible(false);

            JFrame frame = new JFrame("Testing SchemaPanel");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new JScrollPane(schemaPanel));
            frame.setSize(800, 800);
            frame.setVisible(true);
        } catch (Exception e) {
            System.err.println("ERROR");
            e.printStackTrace(System.err);
        }
    }
}
