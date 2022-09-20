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
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import gov.sandia.gnem.dbutillib.Column;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.TableDefinition;
import gov.sandia.gnem.dbutillib.util.DBDefines;

/**
 * This class displays a window where the user can "fix" foreign keys for tables
 * in the calling SchemaPanel.  This "fix foreign keys" functionality is
 * currently only supported by DTX.  Basically, it allows the user to specify
 * that some foreign keys are to be read in and left alone - not remapped or
 * set to na values or anything.
 */
@SuppressWarnings("serial")
public class FixForeignKeysWindow extends JFrame {
    // The calling SchemaPanel that causes this window to be created.
    private SchemaPanel schemaPanel;

    // HashMap from table type to the list of checkboxes for the foreign key
    // columns for that table
    private HashMap<String, LinkedList<JCheckBox>> checkBoxes =
            new HashMap<String, LinkedList<JCheckBox>>();

    /**
     * Constructor! For each table in the calling SchemaPanel, display that
     * table's foreign key columns that could be "fixed".
     */
    public FixForeignKeysWindow(SchemaPanel sp) {
        super("Fix Foreign Keys");
        schemaPanel = sp;

        try {
            JPanel mainPanel = new JPanel(new GridBagLayout());
            Font font = new Font("Monospaced", Font.BOLD, 16);

            // Alert user that they are doing something wild and crazy!
            JLabel warningLabel = new JLabel("Warning!");
            JLabel warningLabel2 = new JLabel("<html><center>Fixing foreign " +
                    "keys could lead to dangling<br>foreign keys in the database.  " +
                    "</center></html>");
            warningLabel.setFont(font);
            warningLabel.setForeground(Color.RED);
            warningLabel2.setFont(font);

            // int gridx, int gridy, int gridwidth, int gridheight,
            // double weightx, double weighty, int anchor,
            // int fill, Insets insets, int ipadx, int ipady
            GridBagConstraints gbc = new GridBagConstraints
                    (0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
                            GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);

            mainPanel.add(warningLabel, gbc);
            gbc.gridy++;
            mainPanel.add(warningLabel2, gbc);

            gbc.gridy++;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            mainPanel.add(new JSeparator(), gbc);
            gbc.fill = GridBagConstraints.NONE;

            // If there are no table types in the calling SchemaPanel, let the user
            // know there are no table types to fix foreign keys for
            if (schemaPanel.getTableTypes().size() == 0) {
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.gridy++;
                mainPanel.add(new JLabel("(No tables have been added to the schema ...)"), gbc);
            }

            // For each table in the calling SchemaPanel, display the foreign keys
            // in that table that could be set.
            JPanel tablesPanel = new JPanel(new GridBagLayout());
            // int gridx, int gridy, int gridwidth, int gridheight,
            // double weightx, double weighty, int anchor,
            // int fill, Insets insets, int ipadx, int ipady
            GridBagConstraints tables_gbc = new GridBagConstraints
                    (-1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
                            GridBagConstraints.NONE, new Insets(10, 10, 15, 15), 0, 0);

            // If you do TableDefintion tableDef = schemaPanel.getSchema().getTableDefinition(), 
            // you can't actually use the tableDef object to hit the database
            // for information since the schema's finalize method gets called 
            // which closes the database connections to all of the schema's
            // objects such as the schema's table definition table.
            Schema schema = schemaPanel.getSchema();
            TableDefinition tableDef = schema.getTableDefinition();

            // The foreign keys that are already fixed for this SchemaPanel
            HashMap<String, LinkedList<String>> fixedForeignKeys =
                    schemaPanel.getFixedForeignKeys();

            for (String tType : schemaPanel.getTableTypes()) {
                String tableType = tType.toUpperCase();
                JPanel tablePanel = new JPanel();
                tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));

                JLabel tableLabel = new JLabel(tableType);
                tablePanel.add(tableLabel);

                for (Column c : tableDef.getColumns(tableType)) {
                    if (c.getForeignKey() != null) {
                        JCheckBox checkbox = new JCheckBox(c.getName());
                        if (checkBoxes.get(tableType) == null)
                            checkBoxes.put(tableType, new LinkedList<JCheckBox>());
                        checkBoxes.get(tableType).add(checkbox);

                        tablePanel.add(checkbox);
                        if (fixedForeignKeys.get(tableType) != null &&
                                fixedForeignKeys.get(tableType).contains(c.getName()))
                            checkbox.setSelected(true);
                    }
                }
                if (checkBoxes.get(tableType) == null)
                    continue;

                // start a new row every 4 columns
                if (tables_gbc.gridx == 3) {
                    tables_gbc.gridx = 0;
                    tables_gbc.gridy++;
                    tables_gbc.fill = GridBagConstraints.HORIZONTAL;
                    tables_gbc.gridwidth = GridBagConstraints.REMAINDER;
                    tablesPanel.add(new JSeparator(), tables_gbc);
                    tables_gbc.gridy++;
                    tables_gbc.fill = GridBagConstraints.NONE;
                    tables_gbc.gridwidth = 1;
                } else
                    tables_gbc.gridx++;

                tablesPanel.add(tablePanel, tables_gbc);
            }

            gbc.gridy++;
            mainPanel.add(tablesPanel, gbc);

            // Fix Selected Foreign Keys and Cancel buttons
            JPanel buttonsPanel = new JPanel();
            JButton fixButton = new JButton("Fix Selected Foreign Keys");
            fixButton.setMargin(new Insets(0, 0, 0, 0));
            fixButton.addActionListener(new FixForeignKeysListener());

            JButton cancelButton = new JButton("Cancel");
            cancelButton.setMargin(new Insets(0, 0, 0, 0));
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });

            // Only show the fix button if there are foreign keys to be fixed
            if (schemaPanel.getTableTypes().size() > 0)
                buttonsPanel.add(fixButton);
            buttonsPanel.add(cancelButton);

            gbc.gridx = 0;
            gbc.gridy++;
            gbc.anchor = GridBagConstraints.CENTER;
            mainPanel.add(buttonsPanel, gbc);

            add(new JScrollPane(mainPanel));
        } catch (DBDefines.FatalDBUtilLibException e) {
            Font errorFont = new Font("Monospaced", Font.BOLD, 16);
            JLabel label = new JLabel("Error Creating Fix Foreign Keys Window");
            label.setFont(errorFont);
            System.err.println(e.getMessage());
            add(label, BorderLayout.NORTH);
        }

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 500);
        setVisible(true);
    }

    private class FixForeignKeysListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            HashMap<String, LinkedList<String>> tableToFkColumns =
                    new HashMap<String, LinkedList<String>>();
            for (String tableName : checkBoxes.keySet()) {
                for (JCheckBox checkbox : checkBoxes.get(tableName)) {
                    if (checkbox.isSelected()) {
                        if (tableToFkColumns.get(tableName.toUpperCase()) == null)
                            tableToFkColumns.put(tableName.toUpperCase(), new LinkedList<String>());
                        tableToFkColumns.get(tableName.toUpperCase()).add(checkbox.getText());
                    }
                }
            }
            schemaPanel.setFixedForeignKeys(tableToFkColumns);
            dispose();
        }
    }
}
