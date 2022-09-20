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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gov.sandia.gnem.dbutillib.gnem.GnemRelationships;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Constants;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Util;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.RelationshipStruct;
import gov.sandia.gnem.dbutillib.util.TableStruct;

/**
 * This panel contains a matrix that displays what relationships exist between tables. It also has an interface for
 * modifying/adding/deleting individual relationships.
 */

/*
 * General design notes: This panel displays a grid with source table types running down the left of the grid and target
 * table types running across the top. The interior of the grid displays color coded boxes that convey information about
 * the type of relationship (if any) between a source and a target table. The color codes currently used represent that:
 * a) no relationship exists, b) a relationship exists, and c) a relationship exists and it is the default gnem
 * relationship. In addition to displaying this information, this panel has a class defining ways for users to
 * add/delete individual relationship information. Clicking the Add button displays a template with comboboxes where the
 * users can select source and target table types from the available tables types (defined in the SchemaPanel that
 * contains this RelationshipsMatrixPanel) as well as where they can enter a relationship where clause and select the
 * constraint for the relationship. This relationship template also contains a Delete button where the user can delete
 * the relationship. For more information on relationships, please see the {@link Relationship Relationship} class.
 */
@SuppressWarnings("serial")
public class RelationshipsMatrixPanel extends JPanel {
    // -------------------- RELATIONSHIP TYPES --------------------
    /**
     * Color for relationships that are not defined
     */
    protected final static Color NO_RELATIONSHIP_COLOR = Color.LIGHT_GRAY;

    /**
     * Color for relationships that are defined (that are not gnem default relationships)
     */
    protected final static Color YES_RELATIONSHIP_COLOR = new Color(155, 155, 225);

    /**
     * Color for relationships that are the default gnem relationships
     */
    protected final static Color DEFAULT_RELATIONSHIP_COLOR = new Color(155, 200, 155);

    /**
     * Textfield color coded to represent that no relationship exists between two tables (used in matrix legend).
     */
    private JTextField noRelationship;

    /**
     * noRelationship textfield label (used in matrix legend)
     */
    private JLabel noRelationshipLabel;

    /**
     * Textfield color coded to represent that a relationship exists between two tables (used in matrix legend).
     */
    private JTextField yesRelationship;

    /**
     * yesRelationship textfield label (used in matrix legend)
     */
    private JLabel yesRelationshipLabel;

    /**
     * Textfield color coded to represent that a relationship exists between two tables and that it is the gnem default
     * relationship between those tables (used in matrix legend)
     */
    private JTextField defaultRelationship;

    /**
     * defaultRelationship textfield label (used in matrix legend)
     */
    private JLabel defaultRelationshipLabel;

    /**
     * Button to load default relationships for all of the tables in the schema panel.
     */
    private JButton loadDefaultRelationshipsButton;

    /**
     * Button to add a new relationship panel
     */
    private JButton addButton;

    /**
     * Default GNEM relationship information
     */
    protected GnemRelationships gnemRelationships;

    // -------------------- CONSTRAINTS --------------------
    /**
     * Constraint definitions displayed in each {@link RelationshipsMatrixPanel.RelationshipPanel RelationshipPanel}'s
     * constraint combobox.
     */
    protected final static String[] COMBOBOX_CONSTRAINTS = new String[]{
            DBDefines.CONSTRAINT_0_N + ": Any Number of Rows", DBDefines.CONSTRAINT_0_1 + ": 0 or 1 Row",
            DBDefines.CONSTRAINT_1 + ": Exactly One Row", DBDefines.CONSTRAINT_N + ": At Least One Row"};

    /**
     * Maps an index into the {@link #COMBOBOX_CONSTRAINTS COMBOBOX_CONSTRAINTS} array to the DBDefines constant
     * constraint definition.
     */
    protected static HashMap<Integer, String> CONSTRAINT_INDEX_TO_CONSTANT;

    /**
     * Maps the DBDefines constant constraint definition to an index into the {@link #COMBOBOX_CONSTRAINTS
     * COMBOBOX_CONSTRAINTS} array.
     */
    protected static HashMap<String, Integer> CONSTRAINT_CONSTANT_TO_INDEX;

    // -------------------- INNER PANELS --------------------
    /**
     * Displays relationship matrix information
     */
    private JPanel matrixPanel;

    /**
     * {@link #matrixPanel matrixPanel}'s legend
     */
    private JPanel matrixLegendPanel;

    /**
     * {@link #matrixPanel matrixPanel}'s GridBagConstraints
     */
    private GridBagConstraints matrixPanel_gbc;

    /**
     * Holds
     *
     * @link RelationshipsMatrixPanel.RelationshipPanel RelationshipPanel} objects
     */
    protected JPanel modifyRelationshipsPanel;

    /**
     * All relationshipPanels
     */
    protected ArrayList<RelationshipPanel> relationshipPanels;

    /**
     * Tables that can have relationships defined between them
     */
    protected String[] tables;

    /**
     * Map that maintains an association between a source and target table and the JTextField being used to represent
     * the type of relationship between those tables (no relationship, yes relationship, default relationship). Here is
     * an example: origin -> arrival -> No relationship color coded JTextField -> assoc -> Default relationship color
     * coded JTextField arrival -> assoc -> Yes relationship color coded JTextField -> origin -> No relationship color
     * coded JTextField assoc -> arrival -> Default relationship color coded JTextField -> origin -> No relationship
     * color coded JTextField
     */
    protected HashMap<String, HashMap<String, JTextField>> matrixMap;

    /**
     * Constructor.
     *
     * @param tableTypes          tables that can have relationships defined between them
     * @param relationshipStructs initial relationships that are present for this relationship panel
     */
    public RelationshipsMatrixPanel(ArrayList<String> tableTypes, ArrayList<RelationshipStruct> relationshipStructs) {
        init(tableTypes, relationshipStructs);
    }

    protected void init(ArrayList<String> tableTypes, ArrayList<RelationshipStruct> relationshipStructs) {
        this.removeAll();
        this.gnemRelationships = new GnemRelationships();
        this.relationshipPanels = new ArrayList<RelationshipPanel>();
        this.matrixMap = new HashMap<String, HashMap<String, JTextField>>();
        if (this.matrixPanel != null)
            this.matrixPanel.removeAll();
        this.matrixPanel = new JPanel(new GridBagLayout());
        this.matrixPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        if (this.modifyRelationshipsPanel != null)
            this.modifyRelationshipsPanel.removeAll();
        this.modifyRelationshipsPanel = new JPanel(new GridBagLayout());
        setDetailedRelationshipsVisible(false);

        createConstraintMaps();
        createMatrixLegendPanel();

        this.tables = tableTypes.toArray(new String[]{});
        Arrays.sort(this.tables);

        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        this.matrixPanel_gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0);

        populateMatrixMap(this.tables);
        populateRelationshipsColors(relationshipStructs);

        // createMatrixPanel() adds the matrix panel to the main panel since it is called from more
        // places than just the constructor
        populateMatrixPanel();
        populateModifyRelationshipsPanel(relationshipStructs);

        // Move down a row under the matrix panel
        GUI_Util.gridBag_newRow(this.matrixPanel_gbc);
        add(this.modifyRelationshipsPanel, this.matrixPanel_gbc);

        // Move this back up one row to accurately track where the matrix panel is
        GUI_Util.gridBag_up(this.matrixPanel_gbc);

        // set default tool tips
        setToolTips();
        revalidate();
        setVisible(true);
    }

    /**
     * Initialize {@link #CONSTRAINT_INDEX_TO_CONSTANT CONSTRAINT_INDEX_TO_CONSTANT} and
     * {@link #CONSTRAINT_CONSTANT_TO_INDEX CONSTRAINT_CONSTANT_TO_INDEX}.
     */
    private void createConstraintMaps() {
        // Initialize the CONSTRAINT_INDEX_TO_CONSTANT.
        CONSTRAINT_INDEX_TO_CONSTANT = new HashMap<Integer, String>();
        CONSTRAINT_INDEX_TO_CONSTANT.put(new Integer(0), DBDefines.CONSTRAINT_0_N);
        CONSTRAINT_INDEX_TO_CONSTANT.put(new Integer(1), DBDefines.CONSTRAINT_0_1);
        CONSTRAINT_INDEX_TO_CONSTANT.put(new Integer(2), DBDefines.CONSTRAINT_1);
        CONSTRAINT_INDEX_TO_CONSTANT.put(new Integer(3), DBDefines.CONSTRAINT_N);

        // Initialize the CONSTRAINT_CONSTANT_TO_INDEX
        CONSTRAINT_CONSTANT_TO_INDEX = new HashMap<String, Integer>();
        CONSTRAINT_CONSTANT_TO_INDEX.put(DBDefines.CONSTRAINT_0_N, new Integer(0));
        CONSTRAINT_CONSTANT_TO_INDEX.put(DBDefines.CONSTRAINT_0_1, new Integer(1));
        CONSTRAINT_CONSTANT_TO_INDEX.put(DBDefines.CONSTRAINT_1, new Integer(2));
        CONSTRAINT_CONSTANT_TO_INDEX.put(DBDefines.CONSTRAINT_N, new Integer(3));
    }

    /**
     * Initialize {@link #matrixMap matrixMap}. See{@link #matrixMap matrixMap} comments for more information
     */
    private void populateMatrixMap(String[] matrixTables) {
        this.matrixMap.clear();

        // Add source table -> target table -> JTextField to the matrix map
        for (String sourceTable : matrixTables) {
            // If there is no entry in matrixMap for the sourceTable, add one
            if (this.matrixMap.get(sourceTable) == null)
                this.matrixMap.put(sourceTable, new HashMap<String, JTextField>());

            // Add target table information for source table
            for (String targetTable : matrixTables) {
                // No relationships between a table and itself
                if (sourceTable.equals(targetTable))
                    continue;
                // Create NO_RELATIONSHIP color coded text field to initially represent the
                // relationship since this is the default state.
                JTextField textField = new JTextField("     ");
                textField.setBackground(NO_RELATIONSHIP_COLOR);
                textField.setEditable(false);
                this.matrixMap.get(sourceTable).put(targetTable, textField);
            }
        }
    }

    /**
     * For relationships that are defined at the time this class is created, change their textfield color to indicate
     * that a relationship (either the default GNEMRelationship or other) exists.
     *
     * @param relationshipStructs relationships at the time this class was created
     */
    private void populateRelationshipsColors(ArrayList<RelationshipStruct> relationshipStructs) {
        // Reset all colors
        for (String source : this.matrixMap.keySet()) {
            HashMap<String, JTextField> targets = this.matrixMap.get(source);
            for (String target : targets.keySet())
                targets.get(target).setForeground(DEFAULT_RELATIONSHIP_COLOR);
        }

        for (RelationshipStruct rel : relationshipStructs) {
            // Check to see if a relationship is being defined between tables
            // that do not exist in the SchemaPanel.
            if (this.matrixMap.get(rel.sourceTableType) == null) {
                System.err.println(rel.sourceTableType + " is the source table" + " in the relationship:\n\t" + rel
                        + "\nThis table is not present" + " in the schemaPanel. This relationship will be ignored.");
                continue;
            }
            if (this.matrixMap.get(rel.sourceTableType).get(rel.targetTableType) == null) {
                System.err.println(rel.targetTableType + " is the target table" + " in the relationship:\n\t" + rel
                        + "\nThis table is not present" + " in the schemaPanel. This relationship will be ignored.");
                continue;
            }

            // Determine if it is a default gnem relationship being used
            RelationshipStruct gnemRel = this.gnemRelationships.get(rel.sourceTableType, rel.targetTableType);
            boolean useGnemRel = sameWhereClauseAndConstraint(gnemRel, rel);

            JTextField relationshipColorField = this.matrixMap.get(rel.sourceTableType).get(rel.targetTableType);
            if (useGnemRel)
                relationshipColorField.setBackground(DEFAULT_RELATIONSHIP_COLOR);
            else
                relationshipColorField.setBackground(YES_RELATIONSHIP_COLOR);
        }
    }

    /**
     * Create the legend explaining the matrix panel.
     */
    private void createMatrixLegendPanel() {
        if (this.matrixLegendPanel != null)
            this.matrixLegendPanel.removeAll();
        // Panel for displaying the "legend" indicating what the color coded boxes represent.
        this.matrixLegendPanel = new JPanel(new FlowLayout());

        // No relationship defined.
        this.noRelationshipLabel = new JLabel("None");
        this.noRelationship = new JTextField("     ");
        this.noRelationship.setBackground(NO_RELATIONSHIP_COLOR);
        this.noRelationship.setEditable(false);

        // Relationship defined.
        this.yesRelationshipLabel = new JLabel("Exists");
        this.yesRelationship = new JTextField("     ");
        this.yesRelationship.setBackground(YES_RELATIONSHIP_COLOR);
        this.yesRelationship.setEditable(false);

        // Relationship is the default relationship.
        this.defaultRelationshipLabel = new JLabel("Default");
        this.defaultRelationship = new JTextField("     ");
        this.defaultRelationship.setBackground(DEFAULT_RELATIONSHIP_COLOR);
        this.defaultRelationship.setEditable(false);

        // Add legend items to legendPanel
        this.matrixLegendPanel.add(this.noRelationshipLabel);
        this.matrixLegendPanel.add(this.noRelationship);
        this.matrixLegendPanel.add(this.yesRelationshipLabel);
        this.matrixLegendPanel.add(this.yesRelationship);
        this.matrixLegendPanel.add(this.defaultRelationshipLabel);
        this.matrixLegendPanel.add(this.defaultRelationship);
    }

    /**
     * Create the matrix panel. This looks something like this: target_table_1 target_table_2 target_table_3
     * target_table_4 source_table_1 ______________ ______________ ______________ source_table_2 ______________
     * ______________ ______________ source_table_3 ______________ ______________ ______________ source_table_4
     * ______________ ______________ ______________ The source and target tables are JLabels. Each ______________ is a
     * JTextField that has no text and is uneditable. The text fields exist solely for color coding purposes - to
     * indicate if there is a relationship between a source table in the column and a target table in the row. If a
     * relationship exists, the color indicates what sort of relationship it is (default GNEM relationship or not).
     * {@link #matrixPanel_gbc matrixPanel_gbc} must be initialized before calling this method.
     */
    private void populateMatrixPanel() {
        this.matrixPanel.removeAll();

        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, this.tables.length + 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0);

        // Add legendPanel to matrixPanel
        this.matrixPanel.add(this.matrixLegendPanel, gbc);

        // redefine gbc since the above definition was to space the legend out properly over the matrix
        gbc = new GridBagConstraints(1, 1, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
                1, 1, 1, 1), 0, 0);

        // table types across the top of the matrix
        for (String table : this.tables) {
            this.matrixPanel.add(new JLabel(table), gbc);
            GUI_Util.gridBag_right(gbc);
        }

        // Populate the type of relationship that exists for each pair of tables.
        for (String sourceTable : this.tables) {
            // Start a new row, right align and pad the source table label
            GUI_Util.gridBag_newRow(gbc);
            GUI_Util.gridBag_eastAnchor(gbc);
            gbc.insets.right = 10;
            this.matrixPanel.add(new JLabel(sourceTable), gbc);

            // Move over and display the color coded text fields for each of the target tables associated with the
            // current source table
            GUI_Util.gridBag_right(gbc);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.insets.left = 3;

            for (String targetTable : this.tables) {
                // If the sourceTable != the targetTable (a table cannot have a relationship with itself), then display
                // what type of relationship it is.
                if (!sourceTable.equals(targetTable))
                    this.matrixPanel.add(this.matrixMap.get(sourceTable).get(targetTable), gbc);

                GUI_Util.gridBag_right(gbc);
            }
        }
        add(this.matrixPanel, this.matrixPanel_gbc);
    }

    /**
     * Create the {@link #modifyRelationshipsPanel modifyRelationshipsPanel} where relationships can be
     * added/deleted/modified
     *
     * @param relationshipStructs relationships at the time this class was created
     */
    protected void populateModifyRelationshipsPanel(ArrayList<RelationshipStruct> relationshipStructs) {
        this.modifyRelationshipsPanel.removeAll();

        setLayout(new GridBagLayout());
        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0);

        // Create a button where users can load default relationships for the tables currently selected in the
        // schema panel
        this.loadDefaultRelationshipsButton = GUI_Util.button_noInsets("Load Default Relationships");
        this.loadDefaultRelationshipsButton.addActionListener(new LoadDefaultRelationshipsListener());

        this.modifyRelationshipsPanel.add(this.loadDefaultRelationshipsButton, gbc);

        GUI_Util.gridBag_newRow(gbc);
        // Add RelationshipPanel information for each relationship defined
        for (RelationshipStruct struct : relationshipStructs) {
            this.modifyRelationshipsPanel.add(new RelationshipPanel(struct), gbc);
            GUI_Util.gridBag_newRow(gbc);
        }

        // Create a button where users can add new relationship information
        this.addButton = new JButton("Add");

        // The AddRelationshipListener needs the gbc used to add things to the modifyRelationshipsPanel so that when
        // users click add, the add button can be removed from the panel, the new RelationshipPanel added where the add
        // button was, and the add button added beneath the new RelationshipPanel
        this.addButton.addActionListener(new AddRelationshipListener(this.addButton, gbc));
        this.modifyRelationshipsPanel.add(this.addButton, gbc);
    }

    /**
     * Set default tool tips
     */
    private void setToolTips() {
        // Use a string to keep track of the tool tip text so that we don't have typos showing up in tool tips for
        // components that are supposed to have the same tool tips.
        this.loadDefaultRelationshipsButton.setToolTipText("Load default relationships for the tables selected above");

        // no relationship defined
        String toolTipText = "No relationship defined";
        this.noRelationshipLabel.setToolTipText(toolTipText);
        this.noRelationship.setToolTipText(toolTipText);

        // some relationship defined
        toolTipText = "Relationship defined";
        this.yesRelationshipLabel.setToolTipText(toolTipText);
        this.yesRelationship.setToolTipText(toolTipText);

        // default relationship defined
        toolTipText = "Default relationship defined";
        this.defaultRelationshipLabel.setToolTipText(toolTipText);
        this.defaultRelationship.setToolTipText(toolTipText);

        // add relationship button
        toolTipText = "Add a relationship";
        this.addButton.setToolTipText(toolTipText);
    }

    /**
     * Set the visibility for the panel where users can add/delete/modify relationship information.
     *
     * @param visible if true, the detailed relationships will be visible; if false, they will not
     */
    protected void setDetailedRelationshipsVisible(boolean visible) {
        this.modifyRelationshipsPanel.setVisible(visible);
    }

    /**
     * Whether or not the detailed relationships panel is visible
     *
     * @return whether or not the detailed relationships panel is visible
     */
    protected boolean detailedRelationshipsVisible() {
        return this.modifyRelationshipsPanel.isVisible();
    }

    /**
     * Return the relationships represented in this RelationshipsMatrixPanel
     *
     * @return the relationships represented in this RelationshipsMatrixPanel
     */
    protected ArrayList<RelationshipStruct> getRelationshipStructs() {
        ArrayList<RelationshipStruct> relationshipStructs = new ArrayList<RelationshipStruct>();

        // Create RelationshipStructs for the relationships represented in this RelationshipsMatrixPanel
        for (RelationshipPanel rp : this.relationshipPanels) {
            // if the source or target combobox are empty, continue on
            if (rp.sourceComboBox.getSelectedItem() == null || rp.targetComboBox.getSelectedItem() == null)
                continue;
            relationshipStructs.add(new RelationshipStruct(rp.sourceComboBox.getSelectedItem().toString(),
                    rp.targetComboBox.getSelectedItem().toString(), rp.whereClause.getText(),
                    CONSTRAINT_INDEX_TO_CONSTANT.get(new Integer(rp.constraintsComboBox.getSelectedIndex()))));
        }
        return relationshipStructs;
    }

    /**
     * Update the available tables that can have relationships defined between them. This is typically called when the
     * user changes the tables types defined within a {@link SchemaPanel SchemaPanel}. This involves removing
     * relationships involving tables not represented in tableStructs, removing tables not in tableStructs from the
     * matrix panel and the source/target table comboboxes, and adding new tables found in tableStructs to the matrix
     * panel and the source/target table comboboxes.
     *
     * @param tableStructs the tables that can have relationships defined between them
     */
    protected void updateTableTypes(ArrayList<TableStruct> tableStructs) {
        // Retrieve just the table types from the tableStructs
        HashSet<String> tableTypes = new HashSet<String>();
        for (TableStruct struct : tableStructs)
            tableTypes.add(struct.type);
        this.tables = tableTypes.toArray(new String[]{});
        Arrays.sort(this.tables);

        // Determine which RelationshipPanels need to be removed due to containing a source and/or target table that
        // is not present in tableTypes. Accumulate the ones to be removed and then remove them later to avoid a
        // ConcurrentModificationException
        ArrayList<RelationshipPanel> relationshipPanelsToRemove = new ArrayList<RelationshipPanel>();
        for (RelationshipPanel rp : this.relationshipPanels) {
            if (rp.sourceComboBox.getSelectedItem() == null || rp.targetComboBox.getSelectedItem() == null)
                continue;
            if (!tableTypes.contains(rp.sourceComboBox.getSelectedItem().toString())
                    || !tableTypes.contains(rp.targetComboBox.getSelectedItem().toString()))
                relationshipPanelsToRemove.add(rp);
        }
        for (RelationshipPanel rp : relationshipPanelsToRemove)
            rp.removeButton.doClick();

        // recreate these with new table information
        populateMatrixMap(this.tables);
        remove(this.matrixPanel);
        populateMatrixPanel();

        // Set this while making these changes so that there are not all of these updates flying all
        // over while items are being updated in the source and target comboboxes
        this.ignoreRelationshipPanelChange = true;
        // Update the relationship panels' comboboxes to have the new tables
        for (RelationshipPanel rp : this.relationshipPanels) {
            // Keep track of what table was originally selected
            Object sourceSelected = rp.sourceComboBox.getSelectedItem();
            Object targetSelected = rp.targetComboBox.getSelectedItem();

            // Since the original combobox sizes may not accommodate any longer table names that
            // are now being added, create a new combobox object with all of the tables. That new
            // combobox will not have any old sizing information associated with it, and its sizing
            // information can be used to size the existing source and target comboboxes
            JComboBox sizingComboBox = new JComboBox();
            rp.sourceComboBox.removeAllItems();
            rp.targetComboBox.removeAllItems();

            for (String table : this.tables) {
                rp.sourceComboBox.addItem(table);
                rp.targetComboBox.addItem(table);
                sizingComboBox.addItem(table);
            }
            if (sourceSelected != null)
                rp.sourceComboBox.setSelectedItem(sourceSelected);
            if (targetSelected != null)
                rp.targetComboBox.setSelectedItem(targetSelected);

            // Make sizing nice
            rp.sourceComboBox.setPreferredSize(new Dimension(sizingComboBox.getPreferredSize().width, rp.sourceComboBox
                    .getPreferredSize().height));
            rp.targetComboBox.setPreferredSize(new Dimension(sizingComboBox.getPreferredSize().width, rp.targetComboBox
                    .getPreferredSize().height));
        }
        this.ignoreRelationshipPanelChange = false;

        // Kludgey trigger to get the panel to update to the correct colors again by triggering the change listener
        for (RelationshipPanel rp : this.relationshipPanels)
            rp.sourceComboBox.setSelectedIndex(rp.sourceComboBox.getSelectedIndex());
    }

    /**
     * Return if the two specified relationships have the same where clause and constraint are equal.
     *
     * @param rel1 relationship with the where clause and constraint to compare to rel2's where clause and constraint
     * @param rel2 relationship with the where clause and constraint to compare to rel1's where clause and constraint
     * @return true if rel1 and rel2 both have the same where clause and constraint; false otherwise
     */
    protected boolean sameWhereClauseAndConstraint(RelationshipStruct rel1, RelationshipStruct rel2) {
        return (rel1 != null && rel2 != null
                && rel1.whereClause.trim().equalsIgnoreCase(rel2.whereClause.trim().replaceAll(" = ", "=")) && rel1.constraint
                .trim().equals(rel2.constraint.trim()));
    }

    /**
     * Inner class that defines the components in a RelationshipPanel where users and modify/delete relationship
     * information. This includes: a combobox where the user can select a source table, a combobox where the user can
     * select a target table, a text field where the user can enter a where clause, a combobox where the user can select
     * a relationship constraint, and a remove button where the user can remove the RelationshipPanel. When users click
     * on the "Add" button, a new RelationshipPanel is added to the addRelationshipsPanel.
     */
    private class RelationshipPanel extends JPanel {
        /**
         * Source table combobox
         */
        protected JComboBox sourceComboBox;

        /**
         * Target table combobox
         */
        protected JComboBox targetComboBox;

        /**
         * Where clause text field
         */
        protected JTextField whereClause;

        /**
         * Constraints combobox
         */
        protected JComboBox constraintsComboBox;

        /**
         * Button to remove this RelationshipPanel
         */
        protected JButton removeButton;

        /**
         * Source table that was selected before a new source table is selected. Keep track of this so that previous
         * relationships that existed can be removed in the matrix since by the time the listeners get the information,
         * they will only have the currently selected table.
         */
        protected String oldSourceTable;

        /**
         * Target table that was selected before a new source table is selected. Keep track of this so that previous
         * relationships that existed can be removed in the matrix since by the time the listeners get the information,
         * they will only have the currently selected table.
         */
        protected String oldTargetTable;

        /**
         * Constructor.
         *
         * @param struct RelationshipStruct with information for initial settings for this panel's components (if this
         *               is null, default values will be used for the components)
         */
        public RelationshipPanel(RelationshipStruct struct) {
            // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
            GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                    GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0);

            // Listener components will use to handle changes to the RelationshipPanel
            RelationshipChangeListener relationshipChangeListener = new RelationshipChangeListener(this);

            // Initialization ...

            // Source table combobox
            this.sourceComboBox = new JComboBox(RelationshipsMatrixPanel.this.tables);
            if (struct != null) {
                this.sourceComboBox.setSelectedItem(struct.sourceTableType);
                this.oldSourceTable = struct.sourceTableType;
            }
            this.sourceComboBox.setPreferredSize(new Dimension(this.sourceComboBox.getPreferredSize().width + 10, 20));
            this.sourceComboBox.addActionListener(relationshipChangeListener);
            add(this.sourceComboBox, gbc);
            GUI_Util.gridBag_right(gbc);

            // Target table combobox
            this.targetComboBox = new JComboBox(RelationshipsMatrixPanel.this.tables);
            if (struct != null) {
                this.targetComboBox.setSelectedItem(struct.targetTableType);
                this.oldTargetTable = struct.targetTableType;
            }
            this.targetComboBox.setPreferredSize(new Dimension(this.targetComboBox.getPreferredSize().width, 20));
            this.targetComboBox.addActionListener(relationshipChangeListener);
            add(this.targetComboBox, gbc);
            GUI_Util.gridBag_right(gbc);

            // Where clause
            this.whereClause = new JTextField(GUI_Constants.mediumTextColumns);
            this.whereClause.setPreferredSize(new Dimension(this.whereClause.getPreferredSize().width,
                    this.sourceComboBox.getPreferredSize().height));
            if (struct != null)
                this.whereClause.setText(struct.whereClause);
            this.whereClause.addMouseListener(relationshipChangeListener);
            this.whereClause.getDocument().addDocumentListener(relationshipChangeListener);
            add(this.whereClause, gbc);
            GUI_Util.gridBag_right(gbc);

            // Constraints combobox
            this.constraintsComboBox = new JComboBox(COMBOBOX_CONSTRAINTS);
            this.constraintsComboBox.setPreferredSize(new Dimension(150, 20));
            if (struct != null)
                this.constraintsComboBox.setSelectedIndex(CONSTRAINT_CONSTANT_TO_INDEX.get(struct.constraint)
                        .intValue());
            this.constraintsComboBox.addActionListener(relationshipChangeListener);
            add(this.constraintsComboBox, gbc);
            GUI_Util.gridBag_right(gbc);

            // Remove button
            this.removeButton = new JButton("Remove");
            this.removeButton.setMargin(new Insets(0, 0, 0, 0));
            this.removeButton.addActionListener(new RemoveRelationshipListener(this));
            add(this.removeButton, gbc);

            // Add this panel to relationshipPanels - keeps track of all of the relationshipPanels
            RelationshipsMatrixPanel.this.relationshipPanels.add(this);

            // set default tool tips
            setToolTips();
        }

        /**
         * Set default tool tips
         */
        private void setToolTips() {
            this.sourceComboBox.setToolTipText("Relationship source table");
            this.targetComboBox.setToolTipText("Relationshp target table");
            this.whereClause.setToolTipText("Relationship where clause (right click for default)");
            this.constraintsComboBox.setToolTipText("Relationship constraint");
            this.removeButton.setToolTipText("Remove relationship");
        }
    }

    /**
     * Listener that handles when the user clicks on the "Add" button to add a new Relationship.
     */
    private class AddRelationshipListener implements ActionListener {
        /**
         * GridBagConstraints for where the add button is currently located.
         */
        private GridBagConstraints gbc;

        /**
         * RelationshipPanel Add button that triggers this listener.
         */
        private JButton relPanelAddButton;

        /**
         * Constructor
         *
         * @param addButton RelationshipPanel Add button that triggers this listener
         * @param gbc       GridBagConstraints for where the add button is currently located
         */
        public AddRelationshipListener(JButton addButton, GridBagConstraints gbc) {
            this.relPanelAddButton = addButton;
            this.gbc = gbc;
        }

        /**
         * Handle when the user clicks on the add button
         */
        public void actionPerformed(ActionEvent e) {
            // Remove the add button
            RelationshipsMatrixPanel.this.modifyRelationshipsPanel.remove(this.relPanelAddButton);
            // Add a new "blank" RelationshipPanel
            RelationshipsMatrixPanel.this.modifyRelationshipsPanel.add(new RelationshipPanel(null), this.gbc);
            // Add the add button back under the new RelationshipPanel
            GUI_Util.gridBag_newRow(this.gbc);
            RelationshipsMatrixPanel.this.modifyRelationshipsPanel.add(this.relPanelAddButton, this.gbc);

            repaint();
            revalidate();
        }
    }

    /**
     * Listener that handles when a user clicks on the Remove button to remove a RelationshipPanel
     */
    private class RemoveRelationshipListener implements ActionListener {
        /**
         * RelationshipPanel to be removed.
         */
        private RelationshipPanel relationshipPanel;

        /**
         * Constructor.
         *
         * @param panel RelationshipPanel to be removed
         */
        public RemoveRelationshipListener(RelationshipPanel panel) {
            this.relationshipPanel = panel;
        }

        /**
         * Handle when the user clicks on the remove button to remove the RelationshipPanel
         */
        public void actionPerformed(ActionEvent e) {
            // Currently selected source table
            String sourceTable = null;
            if (this.relationshipPanel.sourceComboBox.getSelectedItem() != null)
                sourceTable = this.relationshipPanel.sourceComboBox.getSelectedItem().toString();

            // Currently selected target table
            String targetTable = null;
            if (this.relationshipPanel.targetComboBox.getSelectedItem() != null)
                targetTable = this.relationshipPanel.targetComboBox.getSelectedItem().toString();

            // Set the source table -> target table textfield to no relationship in the matrix.
            if (sourceTable != null && targetTable != null
                    && RelationshipsMatrixPanel.this.matrixMap.get(sourceTable).get(targetTable) != null)
                RelationshipsMatrixPanel.this.matrixMap.get(sourceTable).get(targetTable).setBackground(
                        NO_RELATIONSHIP_COLOR);

            // Remove from the gui
            RelationshipsMatrixPanel.this.modifyRelationshipsPanel.remove(this.relationshipPanel);

            // Remove from the list of what's in the gui
            RelationshipsMatrixPanel.this.relationshipPanels.remove(this.relationshipPanel);

            repaint();
            revalidate();
        }
    }

    /**
     * Flag that indicates whether RelationshipPanel changes should be ignored or not.
     */
    protected boolean ignoreRelationshipPanelChange = false;

    /**
     * Listener that updates the matrix when a relationship is changed to reflect those changes.
     */
    private class RelationshipChangeListener extends MouseAdapter implements ActionListener, DocumentListener {
        /**
         * RelationshipPanel that triggered the call to this listener when some change occurred in it
         */
        protected RelationshipPanel relationshipPanel;

        /**
         * Constructor.
         *
         * @param relationshipPanel RelationshipPanel that triggered the call to this listener when some change occurred
         *                          in it
         */
        public RelationshipChangeListener(RelationshipPanel relationshipPanel) {
            this.relationshipPanel = relationshipPanel;
        }

        /**
         * This method updates the colors in the matrix panel that indicate the type of relationship.
         */
        private void handleChange() {
            // If ignoreRelationshipChange is set to true, then this listener will do nothing.
            if (RelationshipsMatrixPanel.this.ignoreRelationshipPanelChange)
                return;

            // if the source or target combobox is empty, do nothing
            if (this.relationshipPanel.sourceComboBox.getSelectedItem() == null
                    || this.relationshipPanel.targetComboBox.getSelectedItem() == null)
                return;

            // Currently selected source and target tables
            String sourceTable = this.relationshipPanel.sourceComboBox.getSelectedItem().toString();
            String targetTable = this.relationshipPanel.targetComboBox.getSelectedItem().toString();

            // Previously selected source and target tables
            String oldSourceTable = this.relationshipPanel.oldSourceTable;
            String oldTargetTable = this.relationshipPanel.oldTargetTable;

            // Change the old relationships to a noRelationship relationship in the matrix panel.
            if (oldSourceTable != null && !oldSourceTable.equals(targetTable))
                RelationshipsMatrixPanel.this.matrixMap.get(oldSourceTable).get(targetTable).setBackground(
                        NO_RELATIONSHIP_COLOR);
            if (oldTargetTable != null && !oldTargetTable.equals(sourceTable))
                RelationshipsMatrixPanel.this.matrixMap.get(sourceTable).get(oldTargetTable).setBackground(
                        NO_RELATIONSHIP_COLOR);

            // Update the new source table to target table relationship in the matrix panel to indicate that either a
            // relationship exists or that the default relationship exists.
            if (!sourceTable.equals(targetTable) && this.relationshipPanel.whereClause.getText().length() > 0) {
                // Get the current where clause and constraint
                String whereClause = this.relationshipPanel.whereClause.getText().trim();
                String constraint = CONSTRAINT_INDEX_TO_CONSTANT.get(new Integer(
                        this.relationshipPanel.constraintsComboBox.getSelectedIndex()));
                JTextField relationshipTextField = RelationshipsMatrixPanel.this.matrixMap.get(sourceTable).get(
                        targetTable);

                // See if there is a gnem relationship defined between these two tables
                RelationshipStruct gnemRel = RelationshipsMatrixPanel.this.gnemRelationships.get(sourceTable,
                        targetTable);

                // Determine if it is a default gnem relationship being used
                boolean useGnemRel = sameWhereClauseAndConstraint(gnemRel, new RelationshipStruct("temp", sourceTable,
                        targetTable, whereClause, constraint));

                // Set to the appropriate color
                if (useGnemRel)
                    relationshipTextField.setBackground(DEFAULT_RELATIONSHIP_COLOR);
                else
                    relationshipTextField.setBackground(YES_RELATIONSHIP_COLOR);
            }

            // Set the previously selected source and target table to the currently selected source and target tables
            this.relationshipPanel.oldSourceTable = sourceTable;
            this.relationshipPanel.oldTargetTable = targetTable;
        }

        /** ****************** MOUSE ADAPTER METHODS ******************* */

        /**
         * Respond to the user clicking the mouse in the where clause text field. Only respond to right clicks. If the
         * user right clicks in the where clause text field, pop up a menu that has only one item - Use Default - in it.
         * If the user selects "Use Default", then populate the where clause text field with the default GNEM
         * relationship.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            // Ignore anything but right clicks
            if (e.getButton() != MouseEvent.BUTTON3)
                return;

            // Create the popup menu
            final JPopupMenu popupMenu = new JPopupMenu();
            // Only one item goes into the popup menu -> "Use default"
            JMenuItem menuItem = new JMenuItem("Use default");

            // Listener for if the user selects the "Use default" menu item from the popup menu. Selecting this would
            // populate the where clause text field with the default GNEM relationship
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    // if the source or the target combobox fields are empty, ignore this action
                    if (RelationshipChangeListener.this.relationshipPanel.sourceComboBox.getSelectedItem() == null
                            || RelationshipChangeListener.this.relationshipPanel.targetComboBox.getSelectedItem() == null)
                        return;

                    String sourceTable = RelationshipChangeListener.this.relationshipPanel.sourceComboBox
                            .getSelectedItem().toString();
                    String targetTable = RelationshipChangeListener.this.relationshipPanel.targetComboBox
                            .getSelectedItem().toString();
                    RelationshipStruct struct = RelationshipsMatrixPanel.this.gnemRelationships.get(sourceTable,
                            targetTable);

                    // Set to the default relationship information
                    if (struct != null) {
                        // where clause
                        RelationshipChangeListener.this.relationshipPanel.whereClause.setText(struct.whereClause);
                        // constraint
                        RelationshipChangeListener.this.relationshipPanel.constraintsComboBox
                                .setSelectedItem(COMBOBOX_CONSTRAINTS[CONSTRAINT_CONSTANT_TO_INDEX.get(
                                        struct.constraint).intValue()]);
                    } else
                        RelationshipChangeListener.this.relationshipPanel.whereClause
                                .setText("(no default relationship available)");

                    popupMenu.setVisible(false);
                }
            });

            // Populate the popup menu
            popupMenu.add(menuItem);
            this.relationshipPanel.whereClause.setComponentPopupMenu(popupMenu);
            popupMenu.setLocation(this.relationshipPanel.whereClause.getLocationOnScreen());
            popupMenu.setVisible(true);
        }

        /** ****************** ACTION LISTENER METHODS ******************* */
        /**
         * Handle a change in the relationship in relationshipPanel. Calls {@link #handleChange handleChange}.
         */
        public void actionPerformed(ActionEvent e) {
            handleChange();
        }

        /** ****************** DOCUMENT LISTENER METHODS ******************* */
        /**
         * Gives notification that an attribute or set of attributes changed; calls {@link #handleChange handleChange}.
         *
         * @param e the document event
         */
        public void changedUpdate(DocumentEvent e) {
            handleChange();
        }

        /**
         * Gives notification that there was an insert into the document; calls {@link #handleChange handleChange}.
         *
         * @param e the document event
         */
        public void insertUpdate(DocumentEvent e) {
            handleChange();
        }

        /**
         * Gives notification that a portion of the document has been removed; calls {@link #handleChange handleChange}.
         *
         * @param e the document event
         */
        public void removeUpdate(DocumentEvent e) {
            handleChange();

        }
    }

    /**
     * Loads the default (GNEM) relationships for the tables currently in the SchemaPanel.
     */
    protected class LoadDefaultRelationshipsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Get current relationships. Use a HashSet to avoid duplicate relationships.
            TreeSet<RelationshipStruct> currentRelationships = new TreeSet<RelationshipStruct>(getRelationshipStructs());

            // Retrieve the tables currently selected in the schema panel
            String[] schemaTables = RelationshipsMatrixPanel.this.matrixMap.keySet().toArray(new String[]{});

            for (int i = 0; i < schemaTables.length; i++) {
                String sourceTable = schemaTables[i];
                for (int j = 0; j < schemaTables.length; j++) {
                    if (i == j)
                        continue;
                    String targetTable = schemaTables[j];

                    // Determine if a default relationship exists
                    RelationshipStruct gnemRel = RelationshipsMatrixPanel.this.gnemRelationships.get(sourceTable,
                            targetTable);

                    if (gnemRel != null)
                        currentRelationships.add(gnemRel);
                }
            }

            init(new ArrayList<String>(RelationshipsMatrixPanel.this.matrixMap.keySet()),
                    new ArrayList<RelationshipStruct>(currentRelationships));
            setDetailedRelationshipsVisible(true);
        }
    }
}
