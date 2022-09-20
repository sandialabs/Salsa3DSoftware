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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.RowGraph;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.Table;
import gov.sandia.gnem.dbutillib.gui.util.DBUtilLibActionChangeListener;
import gov.sandia.gnem.dbutillib.gui.util.DBUtilLibActionListener;
import gov.sandia.gnem.dbutillib.gui.util.DBUtilLibDocumentListener;
import gov.sandia.gnem.dbutillib.gui.util.DBUtilLibListener;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Constants;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Util;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;
import gov.sandia.gnem.dbutillib.util.RelationshipStruct;
import gov.sandia.gnem.dbutillib.util.TableStruct;

/**
 * This class is a GUI representation of a dbutillib Schema. As an abstract class, it cannot be instantiated - use one
 * of its subclasses (such as {@link SchemaPanelDB SchemaPanelDB}, {@link SchemaPanelFF SchemaPanelFF},
 * {@link SchemaPanelXML SchemaPanelXML} instead.
 */
@SuppressWarnings("serial")
public abstract class SchemaPanel extends JPanel {
    // ---------- SCHEMA PANEL TYPES ----------
    /**
     * INPUT SchemaPanel type constant. Input schemas are for reading information in.
     */
    public final static int INPUT = 0;
    /**
     * OUTPUT SchemaPanel type constant. Output schemas are for writing information out.
     */
    public final static int OUTPUT = 1;
    /**
     * TARGET SchemaPanel type constant. Target schemas are for being able to examine data (not reading it into
     * RowGraphs or the like, not writing it out).
     */
    public final static int TARGET = 2;

    // ---------- CONFIGURATION PANEL ----------
    /**
     * Configuration panel (DAO Connectivity information)
     */
    private JPanel configurationPanel;
    /**
     * Configuration combobox label.
     */
    private JLabel configurationComboBoxLabel;
    /**
     * Configuration settings combobox (from KBDB_ACCOUNTS_FILE). The items displayed in this configurationComboBox are
     * also used to access the information related to that item from the CONFIGURATION_PARINFO object.
     */
    protected JComboBox configurationComboBox;
    /**
     * Show/Hide connection information toggle button
     */
    protected JToggleButton connectionToggle;
    /**
     * "Hide connection information" text
     */
    protected final static String HIDE_CONNECTION_TEXT = "Hide Connection Information";
    /**
     * "Show connection information" text
     */
    protected final static String SHOW_CONNECTION_TEXT = "Show Connection Information";
    /**
     * DAO Panel
     */
    private JPanel daoPanel;
    /**
     * Show/Hide Table definition table information toggle button
     */
    protected JToggleButton tableDefToggle;
    /**
     * "Hide Table Definition Table information" text
     */
    protected final static String HIDE_TABLE_DEF_TEXT = "Hide Table Definition Information";
    /**
     * "Show Table Definition Table information" text
     */
    protected final static String SHOW_TABLE_DEF_TEXT = "Show Table Definition Information";
    /**
     * Table def panel. This is a JPanel instead of a TableDefPanel so that TableDefPanelChoosers can be put into this
     * panel.
     */
    private JPanel tableDefPanel;
    /**
     * Keep track of where the tableDefPanel is located in the SchemaPanel since developers have the option of updating
     * the table def information which requires removing the component and adding a new one. Removing a component is
     * easy enough, but we have to keep track of where to put the new one.
     */
    private GridBagConstraints tableDefPanelGbc;

    // ---------- TABLE OPTIONS PANEL ----------
    /**
     * Table Options panel (Options that can be associated with the tables in the SchemaPanel.)
     */
    private JPanel tableOptionsPanel;
    /**
     * Top Level Table label.
     */
    private JLabel topLevelTableLabel;
    /**
     * Top Level Table drop down list for INPUT type SchemaPanel
     */
    private JComboBox topLevelTable;
    /**
     * The String that indicates that no top level table was selected.
     */
    public final static String NO_TOP_LEVEL_TABLE = "(none)";
    /**
     * Query Label
     */
    private JLabel queryLabel;
    /**
     * Scrollpane for query textarea
     */
    private JScrollPane queryScrollPane;
    /**
     * Query textarea for INPUT type SchemaPanel
     */
    private JTextArea query;
    /**
     * Auto Table Creation checkbox for OUTPUT type SchemaPanel
     */
    private JCheckBox autoTableCreation;
    /**
     * Clear Table Data checkbox for OUTPUT type SchemaPanel
     */
    private JCheckBox clearTableData;
    /**
     * Disable Renumbering of IDs checkbox for OUTPUT type SchemaPanel.
     */
    private JCheckBox disableRenumbering;
    /**
     * String for the idgaps table name field when the user does not want to renumber ids.
     */
    private final static String DO_NOT_RENUMBER_IDS = "(do not renumber ids)";
    /**
     * Table options panel separator. Visible globally so it can be hidden if necessary.
     */
    private JSeparator tableOptionsPanelSeparator;

    // ---------- TABLES PANEL ----------
    /**
     * Panel for tables associated with this SchemaPanel.
     */
    private JPanel tablesPanel;
    /**
     * Panel where Tables toggle button is displayed. The reason why there is a whole panel for this is because if the
     * user opts to add a mimic schema button via {@link #createMimicSchemaButton createMimicSchemaButton}, this panel
     * is where that mimic button will be placed.
     */
    private JPanel tablesTogglePanel;
    /**
     * Panel where table types can be selected.
     */
    private SelectTableTypesPanel selectTableTypesPanel;
    /**
     * Button user can click to toggle visibility on the selectTableTypesPanel.
     */
    private JToggleButton selectTableTypesPanelToggle;
    /**
     * Panel with table information in it.
     */
    private JPanel tablesInfoPanel;
    /**
     * Tables that are in the tablesPanel
     */
    protected LinkedHashMap<String, GUITableStruct> tableTypeToGUITableStruct;
    /**
     * Use table label
     */
    private JLabel useTableLabel;
    /**
     * Table type header label
     */
    private JLabel tableTypeLabel;
    /**
     * Auto-fill header label.
     */
    protected JCheckBox autoFillLabel;
    /**
     * Table name label
     */
    private JLabel tableNameLabel;
    /**
     * "Special" (idgaps, remap, and/or author rank) tables that are in the tablesPanel
     */
    protected LinkedHashMap<String, GUITableStruct> specialTableTypeToTableStruct;
    /**
     * idgaps table type
     */
    public final static String IDGAPS_TABLE_TYPE = "idgaps";
    /**
     * remap table type
     */
    public final static String REMAP_TABLE_TYPE = "remap";
    /**
     * author_rank table type
     */
    public final static String AUTHORS_RANK_TABLE_TYPE = "authors_rank";
    /**
     * Special tables separator. Visible globally so it can be hidden if necessary.
     */
    private JSeparator specialTablesSeparator;
    /**
     * Special table use table label.
     */
    private JLabel specialUseTableLabel;
    /**
     * Special table table type label
     */
    private JLabel specialTableTypeLabel;
    /**
     * Special table auto fill label
     */
    private JLabel specialAutoFillLabel;
    /**
     * Special table table name label.
     */
    private JLabel specialTableNameLabel;
    /**
     * Whether or not useTable checkboxes should be shown. This is for the case where a developer wants to turn off ALL
     * use table checkboxes, not each use table checkbox for each individual table.
     */
    private boolean useTableCheckBoxesVisible = true;

    // ---------- RELATIONSHIPS PANEL ----------
    /**
     * Relationships panel
     */
    private JPanel relationshipsPanel;
    /**
     * Button user can click to toggle visibility on the relationshipsMatrixPanel.
     */
    private JToggleButton relationshipsMatrixPanelToggle;
    /**
     * Separator between the tables and the relationships. Visible globally so it can be hidden if necessary.
     */
    private JSeparator relationshipsSeparator;
    /**
     * RelationshipsMatrixPanel
     */
    private RelationshipsMatrixPanel relationshipsMatrixPanel;
    /**
     * GridBagConstraints for where the relationshipsMatrixPanel belongs in the relationshipsPanel.
     */
    private GridBagConstraints relationshipsMatrix_gbc;

    // ---------- VISIBILITY RELATED ----------
    // Since the SchemaPanel is highly customizable, it is useful to keep track of the visibility of different
    // components so that that can be re-established if the SchemaPanel ever needs to be rebuilt or reloaded. In many
    // cases, it is easier to rebuild the SchemaPanel than to try to update all of its many components.
    /**
     * Whether or not the connection information is currently visible.
     */
    private boolean connectionVisible = true;
    /**
     * Whether or not the table definition information is currently visible.
     */
    private boolean tableDefVisible = true;
    /**
     * Whether or not the SelectTableTypes window is currently visible.
     */
    private boolean selectTableTypesVisible = false;
    /**
     * Whether or not the use table checkboxes are currently visible.
     */
    private boolean useTableCheckBoxVisible = true;
    /**
     * Whether or not the top level table drop down box is currently visible.
     */
    private boolean topLevelTableVisible = true;
    /**
     * Whether or not the Auto Table Creation checkbox is currently visible.
     */
    private boolean autoTableCreationVisible = true;
    /**
     * Whether or not the Clear Table Data checkbox is currently visible.
     */
    private boolean clearTableDataVisible = true;
    /**
     * Whether or not the Disable Renumbering of IDs checkbox is currently visible.
     */
    private boolean disableRenumberingVisible = true;
    /**
     * Whether or not the Query text area is currently visible.
     */
    private boolean queryVisible = true;
    /**
     * Whether or not the idgaps table is currently visible.
     */
    private boolean idGapsTableVisible = true;
    /**
     * Whether or not the remap table is currently visible.
     */
    private boolean remapTableVisible = true;
    /**
     * Whether or not the author rank table is currently visible.
     */
    private boolean authorRankTableVisible = true;
    /**
     * Whether or not the relationships panel is currently visible.
     */
    private boolean relationshipsVisible = true;

    // ---------- NON-SWING COMPONENTS ----------
    /**
     * Store predefined configuration information. For database connections, this will have driver, instance, username,
     * password, and tabledefinitiontable information. Since this ParInfo object can store information for many
     * different configurations, here is an example that illustrates how the different sets of information are stored in
     * the ParInfo object:
     * <p>
     * <code>Some Configuration Name           = kb.db.0</code> <br>
     * <code>kb.db.0.name                     = &lt;configuration name&gt;</code> <br>
     * <code>kb.db.0.sql.username             = &lt;configuration username&gt;</code> <br>
     * <code>kb.db.0.sql.password             = &lt;configuration password&gt;</code> <br>
     * <code>kb.db.0.sql.driver               = &lt;configuration driver&gt;</code> <br>
     * <code>kb.db.0.sql.instance             = &lt;configuration instance&gt;</code> <br>
     * <code>kb.db.0.sql.tabledefinitiontable = &lt;configuration tabledefinitiontable&gt;</code> <br>
     * <code>kb.db.0.type                     = &lt;configuration type&gt;</code>
     * <p>
     * So, to access information for a configuration named Some Configuration Name (this is the name that would show up
     * in the configuration combobox), first you must retrieve the par info prefix used for that configuration: <br>
     * <code>String prefix = CONFIGURATION_PARINFO.getItem("Some Configuration Name");</code> <br>
     * Then, the rest of the information can be retrieved like so: <br>
     * <code>String username = CONFIGURATION_PARINFO.getItem(prefix+".sql.username");</code>
     * <p>
     * This is static so that SchemaPanels created by the same application do not end up rereading configuration
     * information.
     */
    protected static ParInfo CONFIGURATION_PARINFO;
    /**
     * Keep track of which configuration files have already been read in so that they are not re-read. The map is from
     * configuration file name to prefixes used in that configuration file.
     */
    private static HashMap<String, TreeSet<String>> CONFIGURATION_FILENAMES;
    /**
     * String place holder for when the user enters in their own custom configuration information.
     */
    protected final static String CONFIGURATION_CUSTOM_ITEM = "custom";

    /**
     * Whether this schema is modifiable (users can change the table types and relationships) or not (the table types
     * and relationships are set by the developer).
     */
    private boolean modifiable;
    /**
     * This SchemaPanel's type (one of {@link #INPUT INPUT}, {@link #TARGET TARGET}, {@link #OUTPUT OUTPUT}).
     */
    protected int type;
    /**
     * Represents the association between parameter names and GUI components
     */
    protected ParInfoGui parInfoGui;
    /**
     * Prefix to be prepended to parameter names. Defaults to schema type.
     */
    protected String parNamePrefix;
    /**
     * Whether or not this SchemaPanel is part of a SchemaPanelChooser. This is a tad kludgey, but there are a couple of
     * pieces of functionality that must remain in SchemaPanel that behave differently when the SchemaPanel is part of a
     * chooser and when it is not. For example, the radio buttons that let a user move between the DB, FF, and XML Dao
     * Types only show up when the SchemaPanel is part of a chooser. However, developers want those radio buttons to be
     * able to be hidden when the user hides the configuration, so they must be in the panel that is declared in this
     * class and gets hidden by this class.
     */
    private boolean containedInSchemaPanelChooser = false;
    /**
     * If this SchemaPanel is part of a SchemaPanelChooser, keep track of the panel with the buttons that allow the user
     * to move between the different SchemaPanels.
     */
    private JPanel schemaPanelChooserButtonPanel;
    /**
     * Map from table type to columns that need to be fixed. This "fix foreign keys" functionality is currently only
     * supported by DTX. Basically, it allows the user to specify that some foreign keys are to be read in and left
     * alone - not remapped or set to na values or anything.
     */
    private HashMap<String, LinkedList<String>> fixedForeignKeys = new HashMap<String, LinkedList<String>>();
    /**
     * Whether or not the GUI data has been initialized
     */
    private boolean initialized = false;
    /**
     * ParInfo to use when initializing (populating with data) the SchemaPanel
     */
    protected ParInfo initializationParInfo;
    /**
     * Tables to use when initializing (populating with data) the SchemaPanel
     */
    protected ArrayList<TableStruct> initializationTables;
    /**
     * Relationships to use when initializing (populating with data) the SchemaPanel
     */
    protected ArrayList<RelationshipStruct> initializationRelationships;

    // ---------- CONSTRUCTORS / INITIALIZATION TYPE THINGS ----------

    /**
     * Create a modifiable (users can modify table types and relationships) SchemaPanel of the designated type - either
     * {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT}, or {@link #TARGET TARGET}. This SchemaPanel is created without any
     * initial table information.
     * <p>
     * If schemaPanelType is {@link #INPUT INPUT}, the SchemaPanel will be created with a top level combobox and a query
     * text field. (These can both be hidden.) <br>
     * If schemaPanelType is {@link #OUTPUT OUTPUT}, the SchemaPanel will be created with a place for users to specify a
     * remap table, an idgaps table, and an author rank table. (These can all be hidden.) This SchemaPanel will not be
     * initialized with its starting data until {@link #setVisible(boolean) setVisible} is called.
     *
     * @param schemaPanelType this SchemaPanel's type - either {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT}, or
     *                        {@link #TARGET TARGET}
     * @throws FatalDBUtilLibException if an error occurs when creating the SchemaPanel
     */
    public SchemaPanel(int schemaPanelType) throws FatalDBUtilLibException {
        this(true, schemaPanelType, new ArrayList<TableStruct>());
    }

    /**
     * Create a SchemaPanel of the designated type - either {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT}, or
     * {@link #TARGET TARGET}- with the specified modifiability. If the SchemaPanel is modifiable, the user can modify
     * table types and relationships. The SchemaPanel is created without any initial table information.
     * <p>
     * If schemaPanelType is {@link #INPUT INPUT}, the SchemaPanel will be created with a top level combobox and a query
     * text field. (These can both be hidden.) <br>
     * If schemaPanelType is {@link #OUTPUT OUTPUT}, the SchemaPanel will be created with a place for users to specify a
     * remap table, an idgaps table, and an author rank table. (These can all be hidden.) This SchemaPanel will not be
     * initialized with its starting data until {@link #setVisible(boolean) setVisible} is called.
     *
     * @param modifiable      whether or not this schema is modifiable
     * @param schemaPanelType this SchemaPanel's type - either {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT}, or
     *                        {@link #TARGET TARGET}
     * @throws FatalDBUtilLibException if an error occurs when creating the SchemaPanel
     */
    public SchemaPanel(boolean modifiable, int schemaPanelType) throws FatalDBUtilLibException {
        this(modifiable, schemaPanelType, new ArrayList<TableStruct>());
    }

    /**
     * Create a modifiable (users can modify table types and relationships) SchemaPanel of the designated type - either
     * {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT}, or {@link #TARGET TARGET} - with the specified initial tables.
     * <p>
     * If schemaPanelType is {@link #INPUT INPUT}, the SchemaPanel will be created with a top level combobox and a query
     * text field. (These can both be hidden.) <br>
     * If schemaPanelType is {@link #OUTPUT OUTPUT}, the SchemaPanel will be created with a place for users to specify a
     * remap table, an idgaps table, and an author rank table. (These can all be hidden.) This SchemaPanel will not be
     * initialized with its starting data until {@link #setVisible(boolean) setVisible} is called.
     *
     * @param schemaPanelType this SchemaPanel's type - either {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT}, or
     *                        {@link #TARGET TARGET}
     * @param tables          tables initially present when the SchemaPanel is created
     * @throws FatalDBUtilLibException if an error occurs when creating the SchemaPanel
     */
    public SchemaPanel(int schemaPanelType, ArrayList<TableStruct> tables) throws FatalDBUtilLibException {
        this(true, schemaPanelType, tables);
    }

    /**
     * Create a SchemaPanel of the designated type - either {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT}, or
     * {@link #TARGET TARGET} - with the specified modifiability and initial table information. If the SchemaPanel is
     * modifiable, the user can modify table types and relationships.
     * <p>
     * If schemaPanelType is {@link #INPUT INPUT}, the SchemaPanel will be created with a top level combobox and a query
     * text field. (These can both be hidden.) <br>
     * If schemaPanelType is {@link #OUTPUT OUTPUT}, the SchemaPanel will be created with a place for users to specify a
     * remap table, an idgaps table, and an author rank table. (These can all be hidden.) This SchemaPanel will not be
     * initialized with its starting data until {@link #setVisible(boolean) setVisible} is called.
     *
     * @param modifiable      whether or not this schema is modifiable
     * @param schemaPanelType this SchemaPanel's type - either {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT}, or
     *                        {@link #TARGET TARGET}
     * @throws FatalDBUtilLibException if an error occurs when creating the SchemaPanel
     */
    public SchemaPanel(boolean modifiable, int schemaPanelType, ArrayList<TableStruct> tables)
            throws FatalDBUtilLibException {
        initSchemaPanel(modifiable, schemaPanelType, tables, new ArrayList<RelationshipStruct>(), null);
    }

    /**
     * Create a SchemaPanel of the designated type - either {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT}, or
     * {@link #TARGET TARGET} - with the specified modifiability and initial tables and relationships. If the
     * SchemaPanel is modifiable, the user can modify table types and relationships.
     * <p>
     * If schemaPanelType is {@link #INPUT INPUT}, the SchemaPanel will be created with a top level combobox and a query
     * text field. (These can both be hidden.) <br>
     * If schemaPanelType is {@link #OUTPUT OUTPUT}, the SchemaPanel will be created with a place for users to specify a
     * remap table, an idgaps table, and an author rank table. (These can all be hidden.) This SchemaPanel will not be
     * initialized with its starting data until {@link #setVisible(boolean) setVisible} is called.
     *
     * @param modifiable      whether or not this schema is modifiable
     * @param schemaPanelType this SchemaPanel's type - either {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT}, or
     *                        {@link #TARGET TARGET}
     * @param tables          tables initially present in the SchemaPanel
     * @param relationships   relationships initially present in the SchemaPanel
     * @throws FatalDBUtilLibException if an error occurs when creating the SchemaPanel
     */
    public SchemaPanel(boolean modifiable, int schemaPanelType, ArrayList<TableStruct> tables,
                       ArrayList<RelationshipStruct> relationships) throws FatalDBUtilLibException {
        initSchemaPanel(modifiable, schemaPanelType, tables, relationships, null);
    }

    /**
     * Constructor that creates a SchemaPanel with information from a ParInfo object; see comments with
     * {@link #SchemaPanel(ParInfo, String) SchemaPanel(parInfo, prefix)} constructor since this constructor calls that
     * constructor with "" for the prefix value. This SchemaPanel will not be initialized with its starting data until
     * {@link #setVisible(boolean) setVisible} is called.
     *
     * @param parInfo parInfo object containing information for creating a SchemaPanel.
     * @throws FatalDBUtilLibException if an error occurs when creating the SchemaPanel
     */
    public SchemaPanel(ParInfo parInfo) throws FatalDBUtilLibException {
        this(parInfo, "");
    }

    /**
     * Constructor that creates a SchemaPanel with information from a ParInfo object; see the description of the parInfo
     * parameter for more information on what parameters SchemaPanel extracts from this parInfo object and what the
     * default values are when no parameter is specified. All parameters retrieved will be prefixed with this
     * SchemaPanel's {@link #getParNamePrefix() parNamePrefix}.
     *
     * @param parInfo parInfo object containing information for creating a SchemaPanel. The following parameters are
     *                retrieved from the parInfo object:
     *                <p>
     *                <hr>
     *                <u>Parameters applicable to all Schema Types</u>
     *                <p>
     *                <b>{@link ParInfoGui#SCHEMA_PANEL_TYPE SchemaPanelType}</b>: this SchemaPanel's type <br>
     *                Acceptable values are: {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT}, or {@link #TARGET TARGET} <br>
     *                <i>defaults to {@link #INPUT INPUT}</i>
     *                <p>
     *                <b>{@link ParInfoGui#MODIFIABLE Modifiable}</b>: this SchemaPanel's modifiability (whether or not the user can
     *                modify the tables/relationships in the SchemaPanel) <br>
     *                Acceptable values are true or false <br>
     *                <i>defaults to true</i>
     *                <p>
     *                <b>{@link ParInfo#TABLES Tables}</b>: Initial tables for this SchemaPanel <br>
     *                Example of acceptable values for Tables (table_name table_type): <br>
     *                &nbsp;&nbsp;&nbsp;&nbsp;origin_name origin <br>
     *                &nbsp;&nbsp;&nbsp;&nbsp;assoc_name assoc <br>
     *                &nbsp;&nbsp;&nbsp;&nbsp;arrival_name arrival <br>
     *                <i>defaults to no tables</i>
     *                <p>
     *                <b>{@link ParInfo#USE_TABLE_TYPES UseTableTypes}</b> The table types that should have their "Use Table" checkbox
     *                selected (comma separated list, whitespace is ignored) <br>
     *                Example: <br>
     *                &nbsp;&nbsp;&nbsp;&nbsp;UseTableTypes = origin,assoc <br>
     *                <i>defaults to all tables having their Use Table checkbox selected</i>
     *                <p>
     *                <b>{@link ParInfoGui#AUTO_FILL_TABLE_TYPES AutoFillTableTypes}</b> The table types that should have their
     *                Auto-Fill checkbox selected (comma separated list, whitespace is ignored) <br>
     *                Example: <br>
     *                &nbsp;&nbsp;&nbsp;&nbsp;AutoFillTableTypes = origin,assoc <br>
     *                <i>defaults to all tables having their Auto Fill checkboxes selected</i>
     *                <p>
     *                <b>{@link ParInfo#RELATIONSHIPS Relationships}</b> Initial relationships for this SchemaPanel <br>
     *                Example of acceptable values for Relationships (id source_table target_table where_clause constraint): <br>
     *                &nbsp;&nbsp;&nbsp;&nbsp;rel_id origin assoc orid=#orid# N <br>
     *                &nbsp;&nbsp;&nbsp;&nbsp;rel_id assoc arrival arid=#arid# 1 <br>
     *                <i>defaults to no relationships</i>
     *                <p>
     *                <hr>
     *                <p>
     *                <u>Parameters applicable to just the {@link #INPUT INPUT} Schema Type</u>
     *                <p>
     *                <b>{@link ParInfo#TOP_LEVEL_TABLE TopLevelTable}</b> TopLevelTable - this could be where RowGraphs with tables in
     *                the SchemaPanel originate or the table that the query executes against; this must be a table that is in the
     *                schema <br>
     *                <i>defaults to no selected table</i>
     *                <p>
     *                <b>{@link ParInfo#QUERY Query}</b> Query to execute against the tables. <br>
     *                <i>defaults to an empty string</i>
     *                <p>
     *                <hr>
     *                <p>
     *                <u>Parameters applicable to just the {@link #OUTPUT OUTPUT} Schema</u>
     *                <p>
     *                <b>{@link ParInfo#AUTO_TABLE_CREATION AutoTableCreation}</b> Whether or not the auto table creation checkbox
     *                should be selected <br>
     *                <i>defaults to not selected</i>
     *                <p>
     *                <b>{@link ParInfo#TRUNCATE_TABLES TruncateTables}</b> Whether or not the Clear Table Data checkbox should be
     *                selected <br>
     *                <i>defaults to not selected</i>
     *                <p>
     *                <b>{@link ParInfo#IDGAPS_TABLE IdGapsTable}</b> IdGaps table name for this SchemaPanel; if this is null, the
     *                Disable Renumbering of ids checkbox will be selected <br>
     *                <i>defaults to an empty string </i>
     *                <p>
     *                <b>{@link ParInfo#IDGAPS_TABLE_USE_TABLE IdGapsTableUseTable}</b> Whether or not the IdGaps table should have its
     *                use table checkbox selected <br>
     *                <i>defaults to a selected checkbox </i>
     *                <p>
     *                <b>{@link ParInfoGui#IDGAPS_TABLE_AUTO_FILL IdGapsTableAutoFill}</b> Whether or not the IdGaps table should have
     *                its auto fill checkbox selected <br>
     *                <i>defaults to a non-selected checkbox</i>
     *                <p>
     *                <b>{@link ParInfo#REMAP_TABLE RemapTable}</b> Remap table name for this SchemaPanel <br>
     *                <i>defaults to an empty string </i>
     *                <p>
     *                <b>{@link ParInfo#REMAP_TABLE_USE_TABLE RemapTableUseTable}</b> Whether or not the Remap table should have its
     *                use table checkbox selected <br>
     *                <i>defaults to a selected checkbox</i>
     *                <p>
     *                <b>{@link ParInfoGui#REMAP_TABLE_AUTO_FILL RemapTableAutoFill}</b> Whether or not the Remap table should have its
     *                auto fill checkbox selected <br>
     *                <i>defaults to a non-selected checkbox</i>
     *                <p>
     *                <b>{@link ParInfo#REMAP_SOURCE RemapSource}</b> Value to use in source column of Remap table for this SchemaPanel
     *                <br>
     *                <i>defaults to an empty string </i>
     *                <p>
     *                <b>{@link ParInfo#REMAP_LDDATE RemapLddate}</b> Value to use in lddate column of Remap table for this SchemaPanel
     *                <br>
     *                <i>defaults to today's date</i>
     *                <p>
     *                <b>{@link ParInfo#RANKING_TABLE AuthorRankTable}</b> Author rank table name for this SchemaPanel <br>
     *                <i>defauls to an empty string</i>
     *                <p>
     *                <b>{@link ParInfo#RANKING_TABLE_USE_TABLE RankingTableUseTable}</b> Whether or not the Ranking table should have
     *                its use table checkbox selected <br>
     *                <i>defaults to a selected checkbox</i>
     *                <p>
     *                <b>{@link ParInfoGui#RANKING_TABLE_AUTO_FILL RankingTableAutoFill}</b> Whether or not the Ranking table should
     *                have its auto fill checkbox selected <br>
     *                <i>defaults to a non-selected checkbox</i>
     * @param prefix  prefix to prepend to the parameter names listed above when accessing parameters from the ParInfo
     *                object
     * @throws FatalDBUtilLibException if an error occurs when creating the SchemaPanel
     */
    public SchemaPanel(ParInfo parInfo, String prefix) throws FatalDBUtilLibException {
        setParNamePrefix(prefix);

        // Determine SchemaPanel type - INPUT is the default
        int schemaPanelType = INPUT;
        String schemaPanelTypeString = parInfo.getItem(prefix + ParInfoGui.SCHEMA_PANEL_TYPE);
        if (schemaPanelTypeString != null)
            schemaPanelType = Integer.valueOf(schemaPanelTypeString).intValue();

        // Determine whether or not the SchemaPanel is modifiable
        boolean canModify = true;
        String modStr = parInfo.getItem(prefix + ParInfoGui.MODIFIABLE, "");
        if (modStr.equalsIgnoreCase("false") || modStr.equalsIgnoreCase("f"))
            canModify = false;

        // Extract table name and type information from the tables parameter
        ArrayList<TableStruct> tables = createTableStructs(parInfo);

        // Extract relationship information
        ArrayList<RelationshipStruct> relationships = createRelationshipStructs(parInfo);

        // Create the SchemaPanel!
        initSchemaPanel(canModify, schemaPanelType, tables, relationships, parInfo);
    }

    /**
     * Create {@link TableStruct TableStructs} with table information from the parInfo object. Table information is
     * retrieved from the parameter named {@link #parNamePrefix parNamePrefix}+{@link ParInfo#TABLES ParInfo.TABLES}.
     * The table information associated with this parameter is then split based on new lines, which results in an array
     * where each array slot contains a line of table information. Each line can specify table information in one of the
     * following ways: <br>
     * table_type <br>
     * table_name table_type <br>
     * table_type selected auto-fill unique_key<i>(The "old" way - supported by the previous SchemaPanel version)</i>
     *
     * @param parInfo ParInfo object to extract table information from
     * @return ArrayList of TableStructs representing the table information found in ParInfo
     */
    private ArrayList<TableStruct> createTableStructs(ParInfo parInfo) {
        ArrayList<TableStruct> tables = new ArrayList<TableStruct>();

        String parInfoTables = parInfo.getItem(this.parNamePrefix + ParInfo.TABLES, null);
        if (parInfoTables == null)
            return tables;

        // Get each line of table information (name and type)
        String[] tableInfo = DBDefines.splitOnNewLine(parInfoTables);

        // Extract table type and name
        // t: current line of table information (name, type, unique key setting)
        for (String t : tableInfo) {
            // Get rid of all double spaces and tabs
            // t = t.trim().replaceAll("\t", " ").replaceAll(" *", " ");
            t = DBDefines.removeExtraSpaces(t.trim());

            // String[] split = t.split(" ");
            String[] split = ParInfo.handleParametersWithSpaces(t);

            // split.length == 1: Only table type specified
            // -- OR --
            // split.length == 4: Tables specified the "old" way (table type,
            // selected, auto-fill, unique key)
            if (split.length == 1 || split.length == 4)
                tables.add(new TableStruct(split[0]));
                // -- OTHERWISE ... --
                // Table name and table type specified
            else
                tables.add(new TableStruct(split[1], split[0]));
        }
        return tables;
    }

    /**
     * Create {@link RelationshipStruct RelationshipStructs} with relationship information from the parInfo object.
     * Relationship information is retrieved from the parameter named {@link #parNamePrefix parNamePrefix}+
     * {@link ParInfo#RELATIONSHIPS ParInfo.RELATIONSHIPS}. The relationship information associated with this parameter
     * is then split based on new lines, which results in an array where each array slot contains a line of relationship
     * information. Each line must specify relationship information as follows: <br>
     * id source_table_type target_table_type where_clause constraint
     *
     * @param parInfo ParInfo object to extract relationship information from
     * @return ArrayList of RelationshipStructs representing the relationship information found in ParInfo
     */
    private ArrayList<RelationshipStruct> createRelationshipStructs(ParInfo parInfo) {
        ArrayList<RelationshipStruct> relationships = new ArrayList<RelationshipStruct>();

        String parInfoRelationships = parInfo.getItem(this.parNamePrefix + ParInfo.RELATIONSHIPS, null);
        if (parInfoRelationships == null)
            return relationships;

        // Get each line of relationship information
        String[] relInfo = DBDefines.splitOnNewLine(parInfoRelationships);

        // Extract relationship information
        // r: current line of relationship information (id, source table type, target table type, where clause,
        // and constraint).
        for (String r : relInfo) {
            // Get rid of all spaces and tabs
            // r = r.trim().replaceAll("\t", " ").replaceAll(" *", " ");
            r = DBDefines.removeExtraSpaces(r.trim());

            String[] split = r.split(" ");
            String id = split[0];
            String sourceTable = split[1];
            String targetTable = split[2];

            // The relationship's where clause is of variable length. It starts after the 3rd word (the target
            // table) and includes everything but the last word (the constraints).
            StringBuilder whereClause = new StringBuilder();
            for (int j = 3; j < split.length - 1; j++)
                whereClause.append(split[j] + " ");

            String constraint = split[split.length - 1];
            relationships.add(new RelationshipStruct(id, sourceTable, targetTable, whereClause.toString(), constraint));
        }
        return relationships;
    }

    /**
     * Initialize the components of the SchemaPanel GUI. (Create the schema panel itself and do initial layout.) This
     * SchemaPanel will not be initialized with its starting data until {@link #setVisible(boolean) setVisible} is
     * called.
     *
     * @param modifiability   whether or not users can modify tables and relationships in this SchemaPanel
     * @param schemaPanelType what type of schema panel this is - one of: {@link #INPUT INPUT}, {@link #OUTPUT OUTPUT},
     *                        {@link #TARGET TARGET}
     * @param tables          the types of tables initially included in this SchemaPanel
     * @param relationships   the relationships initially included in this SchemaPanel
     * @param parInfo         object associated with this SchemaPanel (null if not applicable).
     * @throws DBDefines.FatalDBUtilLibException if an error occurs when initializing the SchemaPanel
     */
    private void initSchemaPanel(boolean modifiability, int schemaPanelType, ArrayList<TableStruct> tables,
                                 ArrayList<RelationshipStruct> relationships, ParInfo parInfo) throws DBDefines.FatalDBUtilLibException {
        DBUtilLibListener.setListenersOn(false);

        // Do not show GUI until user calls setVisible(true) which will handle initializing actual data in the
        // GUI if it hasn't been initialized already
        super.setVisible(false);

        // Check for valid types
        if (schemaPanelType != INPUT && schemaPanelType != OUTPUT && schemaPanelType != TARGET)
            throw new DBDefines.FatalDBUtilLibException("Error in SchemaPanel.initSchemaPanel: schemaPanelType = "
                    + schemaPanelType + ". It must be set to one of the following:\n\t" + INPUT
                    + " (SchemaPanel.INPUT),\n\t" + OUTPUT + " (SchemaPanel.OUTPUT),\n\t" + TARGET
                    + " (SchemaPanel.TARGET)\n");

        // Start over with SchemaPanel creation when this method is called
        this.removeAll();

        // Set SchemaPanel type and modifiability
        this.type = schemaPanelType;
        this.modifiable = modifiability;

        // Initialize special tables HashMap (these tables are handled differently than ordinary tables)
        this.specialTableTypeToTableStruct = new LinkedHashMap<String, GUITableStruct>();

        // Create the ParInfoGui object that will handle synching up GUI components with parInfo components
        this.parInfoGui = new ParInfoGui();

        setLayout(new GridBagLayout());
        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0.1, 0.1, GridBagConstraints.NORTHWEST,
                GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 0), 0, 0);

        // Create the configuration panel and add it to the main panel
        createConfigurationPanel();
        add(this.configurationPanel, gbc);

        // If this SchemaPanel is contained in a SchemaPanelChooser, set up a place to to put the chooser buttons
        if (this.containedInSchemaPanelChooser && this.schemaPanelChooserButtonPanel != null)
            setSchemaPanelChooserButtonPanel(this.schemaPanelChooserButtonPanel);

        // Separator
        GUI_Util.gridBag_newRow(gbc);
        add(new JSeparator(), gbc);

        // Create the table options panel and add it to the main panel
        GUI_Util.gridBag_newRow(gbc);
        createTableOptionsPanel();
        add(this.tableOptionsPanel, gbc);

        // Add a separator between the table options panel and the tables panel if the schemaPanel type is not TARGET
        // (TARGET does not have a table options panel so it does not need a separator)
        if (this.type != TARGET) {
            GUI_Util.gridBag_newRow(gbc);
            this.tableOptionsPanelSeparator = new JSeparator();
            add(this.tableOptionsPanelSeparator, gbc);
        }

        // Create the tables panel and add it to the main panel
        GUI_Util.gridBag_newRow(gbc);
        createTablesPanel();
        add(this.tablesPanel, gbc);

        // Add a separator
        GUI_Util.gridBag_newRow(gbc);
        this.relationshipsSeparator = new JSeparator();
        add(this.relationshipsSeparator, gbc);

        // Create the relationships panel and add it to the main panel
        GUI_Util.gridBag_newRow(gbc);
        createRelationshipsPanel();
        add(this.relationshipsPanel, gbc);

        setToolTips();

        // Associate GUI components with corresponding parameters in this.parInfoGui
        registerComponents();

        // Store information used to populate the SchemaPanel with data at a later time
        this.initialized = false;
        if (parInfo == null)
            this.initializationParInfo = new ParInfo();
        else
            this.initializationParInfo = parInfo;
        this.initializationTables = tables;
        this.initializationRelationships = relationships;

        DBUtilLibListener.setListenersOn(true);
        // GUITableStructs start with their use table checkbox selected, but this is not the desired state for special
        // tables.
        deselectSpecialTablesUseTables();
    }

    /**
     * Set SchemaPanel visibility. Note that if the data in the SchemaPanel data has not been initialized, it will be
     * initialized by this method.
     *
     * @param visible whether this SchemaPanel is visible or not
     */
    @Override
    public void setVisible(final boolean visible) {
        if (!this.initialized) {
            SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>() {
                @Override
                protected Object doInBackground() throws Exception {
                    // This involves IO and can take awhile. Send it off in a SwingWorker thread and update
                    // the GUI once it returns.
                    initializeConfigurationComboBox(SchemaPanel.this.initializationParInfo);
                    return null;
                }

                @Override
                protected void done() {
                    // set tables and relationships first since if there are also tables and table settings in the
                    // ParInfo
                    // object, those take precedence
                    setTables(SchemaPanel.this.initializationTables);
                    setRelationships(SchemaPanel.this.initializationRelationships);

                    updateParameters(SchemaPanel.this.initializationParInfo, getParNamePrefix());

                    // We no longer need these ...
                    SchemaPanel.this.initializationParInfo = null;
                    SchemaPanel.this.initializationTables = null;
                    SchemaPanel.this.initializationRelationships = null;

                    SchemaPanel.super.setVisible(visible);
                    validate();
                    repaint();
                }
            };
            worker.execute();
        } else
            super.setVisible(visible);
    }

    /** *********************** PANEL CREATION METHODS ************************ */
    /**
     * Create the configuration panel that has the Schema's DAO information as well as the dao information for the table
     * definition table.
     * <p>
     * This panel contains:
     * <p>
     * 1st Row: <br>
     * - Configuration label and drop down box <br>
     * - Hide/Show connection information toggle button
     * <p>
     * 2nd Row: <br>
     * - Radio buttons if this SchemaPanel is part of a SchemaPanelChooser. Population of this panel is done by
     * SchemaPanelChooser.
     * <p>
     * 3rd Row: <br>
     * - DAO connection information (this will be provided by a subclass of this SchemaPanel class)
     * <p>
     * 4th Row: <br>
     * - Table Definition Table toggle button
     * <p>
     * 5th Row: <br>
     * - Table Definition Table information
     */
    private void createConfigurationPanel() {
        // Configuration label and combobox
        this.configurationComboBoxLabel = new JLabel("Configuration:");
        this.configurationComboBox = new JComboBox();

        // table definition toggle button
        // Initially create this button with the "longer" text (showTableDef) so that we can set the preferred size
        // to the bigger size. Then set the text to the "shorter" text.
        this.tableDefToggle = GUI_Util.toggleButton_leftAligned(SHOW_TABLE_DEF_TEXT, GUI_Constants.showIcon);
        this.tableDefToggle.setPreferredSize(this.tableDefToggle.getPreferredSize());
        this.tableDefToggle.addActionListener(new TableDefToggleListener());
        this.tableDefToggle.setText(HIDE_TABLE_DEF_TEXT);

        // Table Definition Panel - create before the dao panel since the dao panel listeners handle the table def name.
        this.tableDefPanel = new TableDefPanelDB();

        // dao toggle button
        // Initially create this button with the "longer" text (SHOW_CONNECTION_TEXT) so that we can set the preferred
        // size to the bigger size. Then set the text to the "shorter" text.
        this.connectionToggle = GUI_Util.toggleButton_leftAligned(SHOW_CONNECTION_TEXT, GUI_Constants.showIcon);
        this.connectionToggle.setText(HIDE_CONNECTION_TEXT);
        this.connectionToggle.setIcon(GUI_Constants.hideIcon);
        this.connectionToggle.addActionListener(new ConnectionToggleListener());

        // dao panel
        this.daoPanel = new JPanel(new BorderLayout());
        this.daoPanel.add(createDAOPanel());

        // Make the combobox the same height as the toggle button - looks nicer.
        this.configurationComboBox
                .setPreferredSize(new Dimension(150, this.connectionToggle.getPreferredSize().height));

        // Start adding components to the configurationPanel
        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);

        this.configurationPanel = new JPanel(new GridBagLayout());

        // First Row: configuration label/comboBox and DAO display toggle button.

        // Create the JPanel that will hold this first row
        Box configPanel = Box.createHorizontalBox();
        configPanel.add(this.configurationComboBoxLabel);
        configPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        configPanel.add(this.configurationComboBox);
        configPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        configPanel.add(this.connectionToggle);

        // Add this first row to the configuration panel
        this.configurationPanel.add(configPanel, gbc);

        // 2nd row will contain a panel that can hold radio buttons if this
        // SchemaPanel is part of a SchemaPanelChooser. This only gets added by the SchemaPanelChooser, so leave a place
        // for it.
        GUI_Util.gridBag_newRow(gbc);

        // 3rd row will contain DAO information provided by the subclass
        GUI_Util.gridBag_newRow(gbc);
        this.configurationPanel.add(this.daoPanel, gbc);

        // 4th row will contain the table definition table toggle information Table Definition Label & TextField
        GUI_Util.gridBag_noFill(gbc);
        GUI_Util.gridBag_newRow(gbc);

        this.configurationPanel.add(this.tableDefToggle, gbc);

        // 5th row will contain the Table Definition Table Panel Add TableDefPanel - default is DB
        GUI_Util.gridBag_newRow(gbc);
        this.tableDefPanelGbc = gbc;
        this.configurationPanel.add(this.tableDefPanel, gbc);
    }

    /**
     * This method creates the Table Options Panel. Since this panel looks differently based on whether the Schema is
     * modifiable or not, this method just calls the appropriate method based on what the schema panel's type is.
     * <p> {@link #INPUT INPUT}: {@link #createTableOptionsPanelINPUT createTableOptionsPanelINPUT()} <br> {@link #OUTPUT
     * OUTPUT}: {@link #createTableOptionsPanelINPUT createTableOptionsPanelOUTPUT()} <br> {@link #TARGET TARGET}:
     * {@link #createTableOptionsPanelINPUT createTableOptionsPanelTARGET()}
     */
    private void createTableOptionsPanel() {
        this.tableOptionsPanel = new JPanel();
        this.tableOptionsPanel.setLayout(new BoxLayout(this.tableOptionsPanel, BoxLayout.Y_AXIS));

        switch (this.type) {
            case INPUT:
                createTableOptionsPanelINPUT();
                break;
            case OUTPUT:
                createTableOptionsPanelOUTPUT();
                break;
            case TARGET:
                createTableOptionsPanelTARGET();
                break;
        }
    }

    /**
     * This method creates the table options panel when the schema panel type = {@link #INPUT INPUT}. If the schema is
     * modifiable, this panel will have a Top Level Table drop down box and a Query text area If the schema is not
     * modifiable, it will only have the Query text area.
     */
    private void createTableOptionsPanelINPUT() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        this.queryLabel = new JLabel("Query");

        this.query = new JTextArea(1, GUI_Constants.longTextColumns);
        this.query.setLineWrap(true);

        // Only have the option to specify a top level table if the SchemaPanel
        // is modifiable
        if (this.modifiable) {
            this.topLevelTableLabel = new JLabel("Top Level Table");
            this.topLevelTable = new JComboBox();
            this.topLevelTable.addItem(NO_TOP_LEVEL_TABLE);

            this.topLevelTable.setPreferredSize(new Dimension(140, 20));

            panel.add(this.topLevelTableLabel);
            panel.add(this.topLevelTable);
        } else
            this.query.setColumns(GUI_Constants.longTextColumns + GUI_Constants.mediumTextColumns);

        this.queryScrollPane = new JScrollPane(this.query);
        panel.add(this.queryLabel);
        panel.add(this.queryScrollPane);

        this.tableOptionsPanel.add(panel);
    }

    /**
     * This method creates the table options panel when the schema panel type = {@link #OUTPUT OUTPUT}. This panel will
     * be the same whether the schema panel is modifiable or not and contains the following checkboxes: Auto Table
     * Creation, Clear Table Data, and Disable Renumbering of IDs.
     */
    private void createTableOptionsPanelOUTPUT() {
        // OUTPUT SchemaPanels are the only ones that have special table types, so initialize the special table type
        // information here.

        // Add these tables to the special tables struct. If the user doesn't want one or more of them, they can call
        // the setRemapTableVisible, setIdGapsTableVisible, and/or setAuthorRankTableVisible methods

        // -- IDGAPS
        this.specialTableTypeToTableStruct.put(IDGAPS_TABLE_TYPE, new IdGapsGUITableStruct(new JCheckBox(), new JLabel(
                IDGAPS_TABLE_TYPE), new JCheckBox(), new JTextField(GUI_Constants.longTextColumns),
                getTableBrowseButton(IDGAPS_TABLE_TYPE)));

        // -- REMAP
        this.specialTableTypeToTableStruct.put(REMAP_TABLE_TYPE, new RemapGUITableStruct(new JCheckBox(), new JLabel(
                REMAP_TABLE_TYPE), new JCheckBox(), new JTextField(GUI_Constants.longTextColumns),
                getTableBrowseButton(REMAP_TABLE_TYPE)));

        // -- AUTHOR RANK
        this.specialTableTypeToTableStruct.put(AUTHORS_RANK_TABLE_TYPE, new GUITableStruct(new JCheckBox(), new JLabel(
                AUTHORS_RANK_TABLE_TYPE), new JCheckBox(), new JTextField(GUI_Constants.longTextColumns),
                getTableBrowseButton(AUTHORS_RANK_TABLE_TYPE)));

        // Create and add Auto Table Creation, Clear Existing Table Data, and
        // Disable Renumbering of IDs check boxes
        this.autoTableCreation = new JCheckBox("Auto Table Creation");
        this.autoTableCreation.setMargin(new Insets(0, 0, 0, 0));

        this.clearTableData = new JCheckBox("Clear Existing Table Data");
        this.clearTableData.setMargin(new Insets(0, 0, 0, 0));

        this.disableRenumbering = new JCheckBox("Disable Renumbering of IDs");
        this.disableRenumbering.addActionListener(new DisableRenumberingListener());
        this.disableRenumbering.setMargin(new Insets(0, 0, 0, 0));

        // Add checkboxes to tableOptionsPanel
        JPanel checkboxesPanel = new JPanel();
        checkboxesPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        checkboxesPanel.add(this.autoTableCreation);
        checkboxesPanel.add(this.clearTableData);
        checkboxesPanel.add(this.disableRenumbering);

        this.tableOptionsPanel.add(checkboxesPanel);
    }

    /**
     * This method creates the table options panel when the schema panel type = {@link #TARGET TARGET}. This panel looks
     * the same whether the schema panel is modifiable or not (empty in both cases).
     */
    private void createTableOptionsPanelTARGET() {
        // Nothing in the table options panel for the TARGET SchemaPanel type
        return;
    }

    /**
     * Create the tables panel. Different subclasses will have different ways of implementing how users specify the
     * table names for the given table types (see the {@link #getTableBrowseButton getTableBrowseButton} method) so the
     * button and its listeners must be defined in a subclass.
     */
    private void createTablesPanel() {
        this.tableTypeToGUITableStruct = new LinkedHashMap<String, GUITableStruct>();

        // Panel for tables information
        this.tablesPanel = new JPanel(new GridBagLayout());

        // Panel for information that applies to all tables
        this.tablesInfoPanel = new JPanel(new GridBagLayout());

        // Panel where table types can be selected if the schema is modifiable - can be toggled to be visible or not
        this.tablesTogglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1, 0.1, 0.1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0);

        // Make the Tables label clickable if the SchemaPanel is modifiable.
        if (this.modifiable) {
            // Toggle button for showing/hiding the tablesTogglePanel
            this.selectTableTypesPanelToggle = GUI_Util.toggleButton_centerAligned("Tables", null);
            this.selectTableTypesPanelToggle.setForeground(Color.BLUE);
            Font selectTablesFont = new Font(this.selectTableTypesPanelToggle.getFont().getName(), Font.BOLD, 14);
            this.selectTableTypesPanelToggle.setFont(selectTablesFont);

            this.tablesTogglePanel.add(this.selectTableTypesPanelToggle);

            this.selectTableTypesPanelToggle.addActionListener(new SelectTableTypesListener());
            this.selectTableTypesPanel = new SelectTableTypesPanel(this);

            this.tablesPanel.add(this.tablesTogglePanel, gbc);
            GUI_Util.gridBag_newRow(gbc);
            this.tablesPanel.add(this.selectTableTypesPanel, gbc);
            GUI_Util.gridBag_newRow(gbc);
        } else {
            JLabel tablesLabel = new JLabel("Tables");
            tablesLabel.setForeground(Color.BLUE);
            Font tablesFont = new Font(tablesLabel.getFont().getName(), Font.BOLD, 14);
            tablesLabel.setFont(tablesFont);

            this.tablesTogglePanel.add(tablesLabel);

            this.tablesPanel.add(this.tablesTogglePanel, gbc);
            GUI_Util.gridBag_newRow(gbc);
        }

        // Specify an invisible table panel that has a defined width to force the rest of the gridbaglayout
        // components to stay somewhat centered.
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new Dimension(650, 0));
        emptyPanel.setMinimumSize(emptyPanel.getPreferredSize());
        this.tablesPanel.add(emptyPanel, gbc);
        GUI_Util.gridBag_newRow(gbc);

        GUI_Util.gridBag_northwestAnchor(gbc);
        gbc.gridwidth = 1;
        GUI_Util.gridBag_newRow(gbc);

        this.tablesPanel.add(this.tablesInfoPanel, gbc);

        // These will be used by the headers created for the different types of tables that go into
        // this tables panel
        this.useTableLabel = GUI_Util.label_plain("Use");
        this.tableTypeLabel = GUI_Util.label_plain("Table Type");
        this.autoFillLabel = GUI_Util.checkBox_noMargin("Auto-fill  ");
        this.autoFillLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        this.autoFillLabel.addActionListener(new AutoFillLabelListener());
        this.tableNameLabel = GUI_Util.label_plain("Table Name");
        this.specialUseTableLabel = GUI_Util.label_plain("Use");
        this.specialTableTypeLabel = GUI_Util.label_plain("Table Type");
        this.specialAutoFillLabel = GUI_Util.label_plain("Auto-fill");
        this.specialTableNameLabel = GUI_Util.label_plain("Table Name");
        this.specialTablesSeparator = new JSeparator();
    }

    /**
     * Create the relationships panel.
     */
    private void createRelationshipsPanel() {
        // Panel for relationships information
        this.relationshipsPanel = new JPanel(new GridBagLayout());

        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);

        // Create a panel with nothing in it for spacing reasons - it will take up a certain amount of space so that
        // other items in the GridBagLayout below it won't bunch up on the left if the modifyRelationshipsPanel
        // of the RelationshipsMatrixPanel is not visible.
        JPanel emptyPanel = new JPanel();
        emptyPanel.setPreferredSize(new Dimension(650, 0));
        emptyPanel.setMinimumSize(emptyPanel.getPreferredSize());
        this.relationshipsPanel.add(emptyPanel, gbc);

        GUI_Util.gridBag_newRow(gbc);
        gbc.insets = new Insets(10, 0, 5, 0);

        // Make the Relationships label clickable if the SchemaPanel is modifiable.
        if (this.modifiable) {
            this.relationshipsMatrixPanelToggle = GUI_Util.toggleButton_centerAligned("Relationships", null);
            Font relationshipsFont = new Font(this.relationshipsMatrixPanelToggle.getFont().getName(), Font.BOLD, 14);
            this.relationshipsMatrixPanelToggle.setFont(relationshipsFont);
            this.relationshipsMatrixPanelToggle.setForeground(Color.BLUE);
            this.relationshipsMatrixPanelToggle.addActionListener(new ShowModifyRelationshipsListener());
            this.relationshipsPanel.add(this.relationshipsMatrixPanelToggle, gbc);
        } else {
            JLabel relationshipsLabel = new JLabel("Relationships");
            relationshipsLabel.setForeground(Color.BLUE);
            Font relationshipsFont = new Font(relationshipsLabel.getFont().getName(), Font.BOLD, 14);
            relationshipsLabel.setFont(relationshipsFont);
            this.relationshipsPanel.add(relationshipsLabel, gbc);
        }

        // Relationships Matrix Panel placement is ready to go
        GUI_Util.gridBag_newRow(gbc);
        this.relationshipsMatrix_gbc = gbc;
        this.relationshipsMatrixPanel = new RelationshipsMatrixPanel(new ArrayList<String>(),
                new ArrayList<RelationshipStruct>());
    }

    /** ************************** ABSTRACT METHODS *************************** */
    /**
     * Method to create the DAOPanel. This is not implemented here since it must be implemented by the subclasses so
     * that it is specific to whatever DAO the subclass represents.
     *
     * @return JPanel representing this SchemaPanel's DAO information
     */
    protected abstract JPanel createDAOPanel();

    /**
     * Return this SchemaPanel's DAO Type. This is not implemented here since it must be implemented by the subclsses so
     * that it is specific to whatever DAO the subclass represents.
     *
     * @return this SchemaPanel's DAO type
     */
    public abstract String getDAOType();

    /**
     * Method that adds items to the configuration combobox that are of the same DAO type as the SchemaPanel.
     *
     * @param prefixes the prefixes of the different configurations listed in configuration file
     */
    protected abstract void populateConfigurationComboBox(TreeSet<String> prefixes);

    /**
     * Method that is called when the configurationComboBox listener is triggered. It populates the dao panel text
     * fields with information read in from the kbdb.cfg file when the user changes what's selected in the
     * configurationComboBox.
     *
     * @param source combobox that is the source of the event that triggers this method
     */
    protected abstract void configurationComboBoxActionPerformed(JComboBox source);

    /**
     * This method returns the button used to browse for a table name. Since each sublcass will implement this
     * differently (e.g. browsing the database for a SchemaPanelDB, browsing the file system for a SchemaPanelFF, etc),
     * this must be implemented in the subclasses.
     *
     * @param tableType table type to browse for
     * @return JButton used to browse for a table name
     */
    protected abstract JButton getTableBrowseButton(String tableType);

    /**
     * Set the current directory that should be prepended to file names
     *
     * @param currentDirectory directory that should be prepended to file names
     */
    public abstract void setDirectory(String currentDirectory);

    /**
     * This method registers the gui components in the instantiating class.
     */
    protected abstract void registerSubComponents();

    /**
     * This method updates the ParInfoGui object with information from the gui components in the instantiating class.
     */
    protected abstract void synchSubParInfo();

    /**
     * Refresh the visibility status of instantiating class components.
     */
    abstract protected void refreshSubVisibility();

    /** ******************** SET VISIBLE/ENABLED METHODS ********************** */

    /**
     * Show/Hide the configuration panel
     *
     * @param visible if true - show the configuration panel; if false - hide the configuration panel
     */
    public void setConnectionVisible(boolean visible) {
        if (visible) {
            this.connectionToggle.setText(HIDE_CONNECTION_TEXT);
            this.connectionToggle.setIcon(GUI_Constants.hideIcon);
            this.daoPanel.setVisible(true);
            this.connectionToggle
                    .setToolTipText("Click here to hide the connection information details (username, password, instance, driver).");
        } else {
            this.connectionToggle.setText(SHOW_CONNECTION_TEXT);
            this.connectionToggle.setIcon(GUI_Constants.showIcon);
            this.daoPanel.setVisible(false);
            this.connectionToggle
                    .setToolTipText("Click here to show the connection information details (username, password, instance, driver).");
        }
        this.connectionVisible = visible;
    }

    /**
     * Show/Hide the Table Definition Table panel
     *
     * @param visible if true - show the Table Definition panel; if false - hide the Table Definition panel
     */
    public void setTableDefVisible(boolean visible) {
        if (visible) {
            this.tableDefToggle.setText(HIDE_TABLE_DEF_TEXT);
            this.tableDefToggle.setIcon(GUI_Constants.hideIcon);
            this.tableDefPanel.setVisible(true);
            this.tableDefToggle
                    .setToolTipText("<html>Click here to hide the table definition table connection information details (username, "
                            + "password, instance, driver).<br>The table definition table is a view into the schema schema tables "
                            + "and is used to define the structure of tables in the schema.</html>");
        } else {
            this.tableDefToggle.setText(SHOW_TABLE_DEF_TEXT);
            this.tableDefToggle.setIcon(GUI_Constants.showIcon);
            this.tableDefPanel.setVisible(false);
            this.tableDefToggle
                    .setToolTipText("<html>Click here to show the table definition table connection information details (username, "
                            + "password, instance, driver).<br>The table definition table is a view into the schema schema tables "
                            + "and is used to define the structure of tables in the schema.</html>");
        }
        this.tableDefVisible = visible;
    }

    /**
     * Set whether or not the panel where users can select/deselect table types is visible or not. This can only be done
     * for SchemaPanels that are modifiable.
     *
     * @param visible whether or not the panel where users can select/deselect table types is visible or not
     */
    public void setSelectTableTypesVisible(boolean visible) {
        // This panel does not exist for a non-modifiable SchemaPanel
        if (!this.modifiable)
            return;
        try {
            // Show a busy cursor while loading the table types
            SchemaPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            this.selectTableTypesPanel.setVisible(visible);
            this.selectTableTypesPanelToggle.setSelected(visible);
            this.selectTableTypesVisible = visible;

            updateSelectTablesToolTipText();
        } finally {
            // Show non-busy cursor after table types have loaded. This is in the finally in
            // case an unanticipated exception gets thrown
            SchemaPanel.this.setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Set whether or not the use table checkbox is visible or not for all tables
     *
     * @param visible if true - show the use table checkboxes; if false - hide the use table checkboxes
     */
    public void setUseTableCheckBoxVisible(boolean visible) {
        // Handle the normal tables' useTable label and all of the tables' checkboxes
        for (GUITableStruct ts : this.tableTypeToGUITableStruct.values()) {
            if (ts.isVisible) {
                // Only show the use table label once we've confirmed that a table will be showing
                this.useTableLabel.setVisible(visible);
                ts.useTable.setVisible(visible);
            }
        }

        // Handle the special tables' useTable label and all of the special tables' checkboxes
        for (GUITableStruct ts : this.specialTableTypeToTableStruct.values()) {
            if (ts.isVisible) {
                // Only show the use table label once we've confirmed that a table will be showing
                this.specialUseTableLabel.setVisible(visible);
                ts.useTable.setVisible(visible);
            }
        }
        this.useTableCheckBoxVisible = visible;
    }

    /**
     * Set the visibility for the top level table.
     *
     * @param visible if true, display the top level table information; if false, do not display it
     */
    public void setTopLevelTableVisible(boolean visible) {
        if (this.topLevelTable == null)
            return;

        this.topLevelTable.setVisible(visible);
        this.topLevelTableLabel.setVisible(visible);

        // If the top level table is not visible and neither is the query, then make the separator for
        // the top options panel that contains both of those items invisible as well.
        if (!visible && !this.query.isVisible())
            this.tableOptionsPanelSeparator.setVisible(false);
        else
            this.tableOptionsPanelSeparator.setVisible(true);
        this.topLevelTableVisible = visible;
    }

    /**
     * Set the visibility for the Auto Table Creation checkbox.
     *
     * @param visible if true, display the auto table creation checkbox; if false, do not display it
     */
    public void setAutoTableCreationVisible(boolean visible) {
        if (this.autoTableCreation != null)
            this.autoTableCreation.setVisible(visible);

        // If the Auto Table Creation checkbox is not visible and neither is the Clear Table Data or
        // Disable Renumbering of IDs checkbox, then make the separator for the panel containing those
        // items invisible as well.
        if (!visible && !this.clearTableDataVisible && !this.disableRenumberingVisible)
            this.tableOptionsPanelSeparator.setVisible(false);
        else
            this.tableOptionsPanelSeparator.setVisible(true);

        this.autoTableCreationVisible = visible;
    }

    /**
     * Set the visibility for the Clear Table Data checkbox.
     *
     * @param visible if true, display the Clear Table Data checkbox; if false, do not display it
     */
    public void setClearTableDataVisible(boolean visible) {
        if (this.clearTableData != null)
            this.clearTableData.setVisible(visible);

        // If the Clear Table Data checkbox is not visible and neither is the Auto Table Creation or
        // the Disable Renumbering of IDs checkbox, then make the separator for the panel containing those
        // items invisible as well.
        if (!visible && !this.autoTableCreationVisible && !this.disableRenumberingVisible)
            this.tableOptionsPanelSeparator.setVisible(false);
        else
            this.tableOptionsPanelSeparator.setVisible(true);
        this.clearTableDataVisible = visible;
    }

    /**
     * Set the visibility for the Disable Renumbering of IDs checkbox.
     *
     * @param visible if true, display the Disable Renumbering of IDs checkbox; if false, do not display it
     */
    public void setDisableRenumberingVisible(boolean visible) {
        if (this.disableRenumbering != null)
            this.disableRenumbering.setVisible(visible);

        // If the Disable Renumbering of IDs checkbox is not visible and neither is the Clear Table Data or
        // the Auto Table Creation checkbox, then make the separator for the panel containing those
        // items invisible as well.
        if (!visible && !this.clearTableDataVisible && !this.autoTableCreationVisible)
            this.tableOptionsPanelSeparator.setVisible(false);
        else
            this.tableOptionsPanelSeparator.setVisible(true);

        this.disableRenumberingVisible = visible;
    }

    /**
     * Set the visibility for the query.
     *
     * @param visible if true, display the query information; if false, do not display it
     */
    public void setQueryVisible(boolean visible) {
        if (this.query == null)
            return;

        this.query.setVisible(visible);
        this.queryLabel.setVisible(visible);
        this.queryScrollPane.setVisible(visible);

        // If the query is not visible and neither is the top level table, then make the separator for the
        // panel containing those items invisible as well.
        if (!visible && !this.query.isVisible())
            this.tableOptionsPanelSeparator.setVisible(false);
        else
            this.tableOptionsPanelSeparator.setVisible(true);
        this.queryVisible = visible;
    }

    /**
     * Enable/Disable whether or not users can enter text in the query box.
     *
     * @param enabled true: the user can enter text in the query box; false: the user can not enter text in the query
     *                box
     */
    public void setQueryEnabled(boolean enabled) {
        if (this.query != null)
            this.query.setEnabled(enabled);
    }

    /**
     * Set whether or not the idgaps table name text field is visible
     *
     * @param visible whether or not the idgaps table name text field is visible
     */
    public void setIDGapsTableVisible(boolean visible) {
        setSpecialTableVisible(visible, IDGAPS_TABLE_TYPE);
        this.idGapsTableVisible = visible;
    }

    /**
     * Set whether or not the remap table name text field is visible
     *
     * @param visible whether or not the remap table name text field is visible
     */
    public void setRemapTableVisible(boolean visible) {
        setSpecialTableVisible(visible, REMAP_TABLE_TYPE);
        this.remapTableVisible = visible;
    }

    /**
     * Set whether or not the author rank table name text field is visible
     *
     * @param visible whether or not the author rank table name text field is visible
     */
    public void setAuthorRankTableVisible(boolean visible) {
        setSpecialTableVisible(visible, AUTHORS_RANK_TABLE_TYPE);
        this.authorRankTableVisible = visible;
    }

    /**
     * Generalized method to handle setIDGapsTableVisible, setRemapTableVisible, and setAuthorRankTableVisible methods
     * since they essentially require the same processing
     *
     * @param visible          whether or not the specified specialTableType is visible or not
     * @param specialTableType the special table type that is being set to visible or not
     */
    private void setSpecialTableVisible(boolean visible, String specialTableType) {
        String otherTable1;
        String otherTable2;

        // Determine which tables are not the tables that visibility is being set for.
        if (specialTableType.equals(IDGAPS_TABLE_TYPE)) {
            otherTable1 = REMAP_TABLE_TYPE;
            otherTable2 = AUTHORS_RANK_TABLE_TYPE;
        } else if (specialTableType.equals(REMAP_TABLE_TYPE)) {
            otherTable1 = IDGAPS_TABLE_TYPE;
            otherTable2 = AUTHORS_RANK_TABLE_TYPE;
        } else {
            otherTable1 = IDGAPS_TABLE_TYPE;
            otherTable2 = REMAP_TABLE_TYPE;
        }

        if (this.specialTableTypeToTableStruct.get(specialTableType) != null)
            this.specialTableTypeToTableStruct.get(specialTableType).setVisible(visible);

        // If this table is the last of the special tables to be set to not visible, remove the separator
        // between the normal tables and the special tables since it looks silly if it's not separating anything
        // as well as the special tables labels.
        if (!visible && this.type == OUTPUT && !this.specialTableTypeToTableStruct.get(otherTable1).isVisible
                && !this.specialTableTypeToTableStruct.get(otherTable2).isVisible) {
            this.specialUseTableLabel.setVisible(false);
            this.specialTableTypeLabel.setVisible(false);
            this.specialAutoFillLabel.setVisible(false);
            this.specialTableNameLabel.setVisible(false);
            this.specialTablesSeparator.setVisible(false);
        }

        // If the separator and labels had been set to not visible and this is now being set to visible, show
        // the separator and labels again
        if (visible) {
            if (this.useTableCheckBoxesVisible)
                this.specialUseTableLabel.setVisible(true);
            this.specialTableTypeLabel.setVisible(true);
            this.specialAutoFillLabel.setVisible(true);
            this.specialTableNameLabel.setVisible(true);
            this.specialTablesSeparator.setVisible(true);
        }
    }

    /**
     * Set whether or not the Relationships are visible
     *
     * @param visible whether or not the Relationships are visible
     */
    public void setRelationshipsVisible(boolean visible) {
        if (this.relationshipsPanel == null)
            return;
        this.relationshipsPanel.setVisible(visible);
        this.relationshipsSeparator.setVisible(visible);
        this.relationshipsVisible = visible;
    }

    /**
     * "Refresh" the visibility status of the different components in this GUI. Over time, the visibility of each
     * component can change. If the SchemaPanel has to be rebuilt, that visibility needs to stay the same, even if the
     * components themselves have changed. This method can be called whenever the SchemaPanel has been rebuilt to
     * reestablish correct visibility settings.
     */
    protected void refreshVisibility() {
        // Components of instantiating class
        refreshSubVisibility();
        setConnectionVisible(this.connectionVisible);
        setTableDefVisible(this.tableDefVisible);
        setSelectTableTypesVisible(this.selectTableTypesVisible);
        setUseTableCheckBoxVisible(this.useTableCheckBoxVisible);
        setTopLevelTableVisible(this.topLevelTableVisible);
        setAutoTableCreationVisible(this.autoTableCreationVisible);
        setClearTableDataVisible(this.clearTableDataVisible);
        setDisableRenumberingVisible(this.disableRenumberingVisible);
        setQueryVisible(this.queryVisible);
        setIDGapsTableVisible(this.idGapsTableVisible);
        setRemapTableVisible(this.remapTableVisible);
        setAuthorRankTableVisible(this.authorRankTableVisible);
        setRelationshipsVisible(this.relationshipsVisible);
    }

    /** ************************** GET/SET METHODS **************************** */

    /**
     * Returns the Schema representing the current state of this SchemaPanel.
     *
     * @return the Schema representing the current state of this SchemaPanel
     * @throws FatalDBUtilLibException if an error occurs when retrieving the Schema
     */
    public Schema getSchema() throws FatalDBUtilLibException {
        Schema schema = new Schema(getParInfo(), this.parNamePrefix);
        for (String tableName : this.fixedForeignKeys.keySet()) {
            for (String columnName : this.fixedForeignKeys.get(tableName)) {
                Table table = schema.getTableOfType(tableName.toUpperCase());
                if (table != null) {
                    if (table.getColumn(columnName) != null)
                        table.getColumn(columnName).setFixId(true);
                    else
                        System.err.println("Error fixing foreign key " + columnName + " (length: "
                                + columnName.length() + ") in the " + tableName
                                + " table. That column name does not appear to exist in the " + tableName + " table.");
                } else
                    System.err.println("Error fixing foreign key " + columnName + " (length: " + columnName.length()
                            + ") in the " + tableName + " table. That table does not appear to exist in the schema.");
            }
        }
        return schema;
    }

    /**
     * If this SchemaPanel is an {@link #INPUT INPUT} SchemaPanel, return a populated RowGraph that reflects the current
     * status of this SchemaPanel;otherwise, returns null.
     *
     * @return populated RowGraph that reflects the current status of this SchemaPanel if this SchemaPanel is an
     * {@link #INPUT INPUT} SchemaPanel; otherwise, returns null.
     */
    public RowGraph getRowGraph() {
        if (this.type != INPUT) {
            JOptionPane.showMessageDialog(this, "ERROR in SchemaPanel.getRowGraph().\nCannot create a RowGraph because"
                    + "\nthe schema type is not 'INPUT'.", "ERROR", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Schema schema = null;
        try {
            schema = new Schema(getParInfo(), this.parNamePrefix);
        } catch (DBDefines.FatalDBUtilLibException e) {
            JOptionPane.showMessageDialog(this,
                    "ERROR in SchemaPanel.getRowGraph().\nCannot create a RowGraph because "
                            + "there was an error creating the input schema.\nError: " + e.getMessage(), "ERROR",
                    JOptionPane.ERROR_MESSAGE);
        }
        if (schema == null) {
            JOptionPane.showMessageDialog(this,
                    "ERROR in SchemaPanel.getRowGraph().\nCannot create a RowGraph because the "
                            + "input schema is null.", "ERROR", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        String queryText = this.query.getText();
        if (queryText == null || queryText.length() == 0) {
            JOptionPane.showMessageDialog(this,
                    "ERROR in SchemaPanel.getRowGraph().\nCannot create a RowGraph because no query"
                            + "\nspecified in " + "the Input Schema", "ERROR", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        String topTable = getTableTypes().get(0);
        if (this.modifiable)
            topTable = this.topLevelTable.getSelectedItem().toString();

        Table table = schema.getTableOfType(topTable);

        try {
            return new RowGraph(table, queryText);
        } catch (DBDefines.FatalDBUtilLibException e) {
            System.err.println("Error in SchemaPanel.getRowGraph.\nError message: " + e.getMessage());
            return new RowGraph();
        }
    }

    /**
     * Return {@link GUITableStruct GUITableStruct} objects for the "special" (idgaps, remap, author_rank) tables
     * represented in this SchemaPanel
     *
     * @return {@link GUITableStruct GUITableStruct} objects for the "special" (idgaps, remap, author_rank) tables
     * represented in this SchemaPanel
     */
    public ArrayList<GUITableStruct> getSpecialTables() {
        return new ArrayList<GUITableStruct>(this.specialTableTypeToTableStruct.values());
    }

    /**
     * Return the selected table types represented in this SchemaPanel.
     *
     * @return the selected table types represented in this SchemaPanel
     */
    public ArrayList<String> getSelectedTableTypes() {
        ArrayList<String> tableTypes = new ArrayList<String>();
        for (GUITableStruct ts : this.tableTypeToGUITableStruct.values())
            if (ts.useTable.isSelected())
                tableTypes.add(ts.tableType.getText());
        return tableTypes;
    }

    /**
     * Return the table types represented in this SchemaPanel.
     *
     * @return the table types represented in this SchemaPanel
     */
    public ArrayList<String> getTableTypes() {
        ArrayList<String> tableTypes = new ArrayList<String>();
        for (GUITableStruct ts : this.tableTypeToGUITableStruct.values())
            tableTypes.add(ts.tableType.getText());
        return tableTypes;
    }

    /**
     * Return this SchemaPanel's parNamePrefix (prefix to be prepended to parameter names).
     *
     * @return this SchemaPanel's parNamePrefix (prefix to be prepended to parameter names)
     */
    public String getParNamePrefix() {
        return this.parNamePrefix;
    }

    /**
     * Set the value for this SchemaPanel's parameter name prefix.
     *
     * @param prefix the value for this SchemaPanel's parameter name prefix
     */
    public void setParNamePrefix(String prefix) {
        this.parNamePrefix = prefix.trim();
        if (this.parNamePrefix.endsWith("_"))
            this.parNamePrefix = this.parNamePrefix.substring(0, this.parNamePrefix.length() - 1);
    }

    /**
     * Return whether or not this SchemaPanel's Clear Table Data checkbox isselected or not. (If this SchemaPanel's type
     * is not {@link #OUTPUT OUTPUT}, this method will return false.)
     *
     * @return whether or not this SchemaPanel's Clear Table Data checkbox is selected or not. (If this SchemaPanel's
     * type is not {@link #OUTPUT OUTPUT}, this method will return false.)
     */
    public boolean getClearTableDataSelected() {
        if (this.type != OUTPUT)
            return false;
        return this.clearTableData.isSelected();
    }

    /**
     * Set whether or not the Clear Table Data checkbox is selected or not.
     *
     * @param selected whether or not the Clear Table Data checkbox is selected or not.
     */
    public void setClearTableDataSelected(boolean selected) {
        if (this.clearTableData != null)
            this.clearTableData.setSelected(selected);
    }

    /**
     * Return {@link GUITableStruct GUITableStruct} objects for the tables represented in this SchemaPanel
     *
     * @return {@link GUITableStruct GUITableStruct} objects for the tables represented in this SchemaPanel
     */
    public ArrayList<GUITableStruct> getTables() {
        return new ArrayList<GUITableStruct>(this.tableTypeToGUITableStruct.values());
    }

    /**
     * Set table types in the tables panel. Existing table types are removed and the new table types are added.
     *
     * @param tableStructs table types to display in the tablesPanel.
     */
    public void setTables(ArrayList<TableStruct> tableStructs) {
        // This methods removes all of the table panels and adds new ones for the tables in tableStructs.
        // It preserves things like selected top level table and existing table names when possible
        doAutoFill = false;

        // Sort the tables by table type.
        TableStruct[] temp = tableStructs.toArray(new TableStruct[]{});
        Arrays.sort(temp, new TableStructComparator());
        ArrayList<TableStruct> sortedTableStructs = new ArrayList<TableStruct>(Arrays.asList(temp));

        // Hold on to old table structs so we can retrieve existing table name information from them since
        // we will be recreating all of the table structs
        LinkedHashMap<String, GUITableStruct> oldTableStructs = new LinkedHashMap<String, GUITableStruct>();
        // Non-shallow copy
        for (String key : this.tableTypeToGUITableStruct.keySet())
            oldTableStructs.put(key, this.tableTypeToGUITableStruct.get(key));

        // Remove all existing table panels
        this.tablesInfoPanel.removeAll();
        this.tableTypeToGUITableStruct.clear();

        // Keep track of what top level table was originally selected
        Object selectedTopLevelTable = null;
        if (this.topLevelTable != null) {
            selectedTopLevelTable = this.topLevelTable.getSelectedItem();
            this.topLevelTable.removeAllItems();
            this.topLevelTable.addItem(NO_TOP_LEVEL_TABLE);
        }
        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 5, 10, 10), 0, 0);

        if (sortedTableStructs.size() > 0)
            createNormalTablesHeader(gbc);

        // Display table information
        for (TableStruct tableStruct : sortedTableStructs) {
            GUI_Util.gridBag_newRow(gbc);

            String tableType = tableStruct.type;
            GUITableStruct guiTableStruct = oldTableStructs.get(tableType);

            // If this table wasn't in the panel before, create a new one.
            if (guiTableStruct == null)
                guiTableStruct = new GUITableStruct(new JCheckBox(), new JLabel(tableType.toLowerCase()),
                        new JCheckBox(), new JTextField(tableStruct.name, GUI_Constants.longTextColumns),
                        getTableBrowseButton(tableType));
                // Table was originally in the SchemaPanel with no name specified. Now a name is being specified - use it.
            else if (tableStruct.name.length() > 0)
                guiTableStruct.tableName.setText(tableStruct.name);

            // Add this table to tableTypeToGUITableStruct
            this.tableTypeToGUITableStruct.put(tableType, guiTableStruct);

            // Add this table to the list of tables that can be selected as a top level table
            if (this.topLevelTable != null)
                this.topLevelTable.addItem(tableType);

            // Add this GUITableStruct information to the tablesInfoPanel
            guiTableStruct.addToPanel(this.tablesInfoPanel, gbc);

            // Only set selected to true if this is a new table. Pre-existing tables already have their use table
            // and auto-fill selectiveness set.
            if (oldTableStructs.get(tableType) == null) {
                guiTableStruct.useTable.setSelected(true);
                // guiTableStruct.autoFill.setSelected(true);
            }
        }

        // Auto-fill new table names if there are old table names to use for auto-filling. Do this after the main loop
        // since if a new table gets added that is alphabetically before a table that had been there before, then, at
        // the time of trying to auto-fill that table, the table to auto-fill from will not yet be present.
        // for (TableStruct tableStruct : tableStructs)
        // {
        // String tableType = tableStruct.type;
        // if (oldTableStructs.get(tableType) != null)
        // {
        // GUITableStruct guiTableStruct = this.tableTypeToGUITableStruct.get(tableType);
        //
        // // Little bit of trickery to trigger the auto-fill listener. Just doing setSelected(true) doesn't work.
        // guiTableStruct.autoFill.setSelected(false);
        // guiTableStruct.autoFill.doClick();
        // }
        // }

        // Update topLevelTable to what was selected before
        if (selectedTopLevelTable != null)
            this.topLevelTable.setSelectedItem(selectedTopLevelTable);

        if (this.relationshipsMatrixPanel != null)
            // Update the relationships to reflect any changes to the table types
            this.relationshipsMatrixPanel.updateTableTypes(sortedTableStructs);

        // If the type is INPUT or TARGET, then there are no "special" tables to handle. If the type is OUTPUT and no
        // "special" tables are visible, there's no special table handling. Just return.
        if (this.type != OUTPUT
                || (!this.specialTableTypeToTableStruct.get(IDGAPS_TABLE_TYPE).isVisible
                && !this.specialTableTypeToTableStruct.get(REMAP_TABLE_TYPE).isVisible && !this.specialTableTypeToTableStruct
                .get(AUTHORS_RANK_TABLE_TYPE).isVisible)) {
            repaint();
            revalidate();
            doAutoFill = true;
            return;
        }

        // Handle special tables
        // Add a separator
        GUI_Util.gridBag_newRow(gbc);
        GUI_Util.gridBag_useWholeLine(gbc);
        this.tablesInfoPanel.add(this.specialTablesSeparator, gbc);
        gbc.gridwidth = 1;

        createSpecialTablesHeader(gbc);

        // Display "special table information
        for (String tableType : this.specialTableTypeToTableStruct.keySet()) {
            GUI_Util.gridBag_newRow(gbc);
            GUITableStruct guiTableStruct = this.specialTableTypeToTableStruct.get(tableType);

            // Add this GUITableStruct information to the tablesInfoPanel
            guiTableStruct.addToPanel(this.tablesInfoPanel, gbc);
        }

        repaint();
        revalidate();
        doAutoFill = true;
    }

    /**
     * Return {@link RelationshipStruct RelationshipStruct} objects for the relationships represented in this
     * SchemaPanel
     *
     * @return {@link RelationshipStruct RelationshipStruct} objects for the relationships represented in this
     * SchemaPanel
     */
    public ArrayList<RelationshipStruct> getRelationships() {
        ArrayList<RelationshipStruct> relationshipStructs = new ArrayList<RelationshipStruct>();
        for (RelationshipStruct rs : this.relationshipsMatrixPanel.getRelationshipStructs())
            relationshipStructs.add(rs);
        return relationshipStructs;
    }

    /**
     * Set relationships in the relationships panel. Existing relationships are removed and the new relationships are
     * added.
     *
     * @param relationships relationships to display in the relationshipsPanel
     */
    public void setRelationships(ArrayList<RelationshipStruct> relationships) {
        // make sure things are in the correct case to avoid case sensitivity issues
        for (RelationshipStruct rel : relationships) {
            rel.id = rel.id.toLowerCase();
            rel.sourceTableType = rel.sourceTableType.toLowerCase();
            rel.targetTableType = rel.targetTableType.toLowerCase();
            rel.whereClause = rel.whereClause.toLowerCase();
            rel.constraint = rel.constraint.toUpperCase();
        }

        this.relationshipsPanel.remove(this.relationshipsMatrixPanel);

        boolean detailedRelationshipsVisible = false;
        if (this.relationshipsMatrixPanel != null)
            detailedRelationshipsVisible = this.relationshipsMatrixPanel.detailedRelationshipsVisible();

        this.relationshipsMatrixPanel = new RelationshipsMatrixPanel(getTableTypes(), relationships);
        this.relationshipsMatrixPanel.setDetailedRelationshipsVisible(detailedRelationshipsVisible);
        this.relationshipsPanel.add(this.relationshipsMatrixPanel, this.relationshipsMatrix_gbc);

        this.relationshipsPanel.revalidate();
    }

    /**
     * Method that sets tables, special tables, and relationships in this SchemaPanel.
     *
     * @param tables        tables to have in this SchemaPanel
     * @param specialTables "special" tables (e.g. idgaps, remap, author_rank) tables to have in this SchemaPanel; if
     *                      this is null, then the special tables will be unchanged from their current state
     * @param relationships relationships to have in this SchemaPanel
     */
    protected void setAllTablesRelationships(ArrayList<GUITableStruct> tables, ArrayList<GUITableStruct> specialTables,
                                             ArrayList<RelationshipStruct> relationships) {
        if (specialTables != null) {
            this.specialTableTypeToTableStruct.clear();
            for (GUITableStruct table : specialTables) {
                String tableType = table.tableType.getText();
                this.specialTableTypeToTableStruct.put(tableType, table);
            }
        }

        // Make TableStructs
        ArrayList<TableStruct> tableStructs = new ArrayList<TableStruct>();
        for (GUITableStruct gts : tables)
            tableStructs.add(new TableStruct(gts.tableType.getText(), gts.tableName.getText()));
        setTables(tableStructs);

        setRelationships(relationships);
    }

    /**
     * Returns this SchemaPanel's tableDefPanel. If this SchemaPanel's tableDefPanel is a TableDefPanelChooser, returns
     * the currently "chosen" tableDefPanel.
     *
     * @return this SchemaPanel's tableDefPanel
     */
    protected TableDefPanel getTableDefPanel() {
        String className = this.tableDefPanel.getClass().toString();
        TableDefPanel returnPanel;
        if (className.endsWith("TableDefPanelChooser"))
            returnPanel = ((TableDefPanelChooser) this.tableDefPanel).getSelectedTableDefPanel();
        else
            returnPanel = (TableDefPanel) this.tableDefPanel;

        return returnPanel;
    }

    /**
     * Set the Table Definition Panel to be newTableDefPanel.
     *
     * @param newTableDefPanel TableDefPanel to set the Table Defintion Panel to
     */
    public void setTableDefinition(TableDefPanel newTableDefPanel) {
        setTableDefPanel(newTableDefPanel);
    }

    /**
     * Set the Table Definition Panel to be newTableDefPanel.
     *
     * @param newTableDefPanel TableDefPanelChooser to set the Table Defintion Panel to
     */
    public void setTableDefinition(TableDefPanelChooser newTableDefPanel) {
        setTableDefPanel(newTableDefPanel);
    }

    /**
     * Retrieve which foreign keys are currently set to "fixed" (the values in these columns are not to be changed.
     *
     * @return HashMap from table type to LinkedList of fixed foreign key columns
     */
    protected HashMap<String, LinkedList<String>> getFixedForeignKeys() {
        return this.fixedForeignKeys;
    }

    /**
     * Set which foreign keys are to remain fixed
     *
     * @param tableToFkColumns HashMap from table type to list of columns that are to remain fixed (the values in these
     *                         columns are not changed)
     */
    protected void setFixedForeignKeys(HashMap<String, LinkedList<String>> tableToFkColumns) {
        this.fixedForeignKeys = tableToFkColumns;
    }

    /**
     * Get the currently selected top level table (null if not applicable)
     *
     * @return the currently selected top level table (null if not applicable)
     */
    public String getTopLevelTable() {
        if (this.topLevelTable == null || !this.topLevelTableVisible || this.topLevelTable.getSelectedIndex() == -1)
            return null;
        return this.topLevelTable.getSelectedItem().toString();
    }

    /**
     * Set whether or not the Auto Table Creation checkbox is selected or not.
     *
     * @param selected whether or not the Auto Table Creation checkbox is selected or not.
     */
    public void setAutoTableCreationSelected(boolean selected) {
        if (this.autoTableCreation != null)
            this.autoTableCreation.setSelected(selected);
    }

    /**
     * Set whether or not the Disable Renumbering of IDs checkbox is selected or not.
     *
     * @param selected whether or not the Disable Renumbering of IDs checkbox is selected or not.
     */
    public void setDisableRenumberingSelected(boolean selected) {
        if (this.disableRenumbering != null)
            this.disableRenumbering.setSelected(selected);
    }

    /**
     * Set the tableDefPanel to be newTableDefPanel. The two public versions of this method ensure that developers pass
     * in acceptable values for the table defintion panel (either a TableDefPanel or a TableDefPanelChooser). This
     * method does the actual setting of tableDefPanel to newTableDefPanel
     *
     * @param newTableDefPanel panl to set the Table Definition Panel to
     */
    private void setTableDefPanel(JPanel newTableDefPanel) {
        boolean origVisibility = this.tableDefPanel.isVisible();

        this.configurationPanel.remove(this.tableDefPanel);
        this.tableDefPanel = newTableDefPanel;

        // Maintain the original visibility
        newTableDefPanel.setVisible(origVisibility);

        this.configurationPanel.add(this.tableDefPanel, this.tableDefPanelGbc);

        this.configurationPanel.repaint();
        this.configurationPanel.revalidate();

        registerTableDefComponents();
    }

    /**
     * Set AutoFill state (true or false) for a particular table
     *
     * @param tableType table type to set autofill use policy for
     * @param autoFill  whether or not to use autofill
     */
    public void setAutoFill(String tableType, boolean autoFill) {
        GUITableStruct tableStruct = this.tableTypeToGUITableStruct.get(tableType);
        if (tableStruct == null)
            System.err.println("Error in setAutoFill(" + tableType + ", " + autoFill + "). The SchemaPanel "
                    + "does not have a " + tableType + " table.");
        else
            tableStruct.autoFill.setSelected(autoFill);
    }

    /**
     * Set UseTable state (true or false) for a particular table
     *
     * @param tableType table type to set autofill use policy for
     * @param useTable  whether or not to use autofill
     */
    public void setUseTable(String tableType, boolean useTable) {
        GUITableStruct tableStruct = this.tableTypeToGUITableStruct.get(tableType);
        if (tableStruct == null)
            System.err.println("Error in setUseTable(" + tableType + ", " + useTable + "). The SchemaPanel does "
                    + "not have a " + tableType + " table.");
        else
            tableStruct.useTable.setSelected(useTable);
    }

    /**
     * Set the text in the query box.
     *
     * @param text text to use to populate the query box
     */
    public void setQueryText(String text) {
        this.query.setText(text);
    }

    /**
     * Set the text used to label the query.
     *
     * @param label text used to label the query
     */
    public void setQueryLabelText(String label) {
        this.queryLabel.setText(label);
    }

    /**
     * Set the table name for a specific table type
     *
     * @param tableType the table type to set the table name for
     * @param tableName the table name to set for tableType
     */
    public void setTableName(String tableType, String tableName) {
        GUITableStruct tableStruct = this.tableTypeToGUITableStruct.get(tableType);
        if (tableStruct == null)
            System.err.println("Error in setTableName(" + tableType + ", " + tableName + "). The SchemaPanel "
                    + "does not have a " + tableType + " table.");
        else
            tableStruct.tableName.setText(tableName);
    }

    /**
     * Set the IDGaps Table name
     *
     * @param tableName the idgaps table name
     */
    public void setIDGapsTableName(String tableName) {
        if (this.specialTableTypeToTableStruct.get(IDGAPS_TABLE_TYPE) != null)
            this.specialTableTypeToTableStruct.get(IDGAPS_TABLE_TYPE).tableName.setText(tableName);
    }

    /**
     * Set the Remap Table name
     *
     * @param tableName the remap table name
     */
    public void setRemapTableName(String tableName) {
        if (this.specialTableTypeToTableStruct.get(REMAP_TABLE_TYPE) != null)
            this.specialTableTypeToTableStruct.get(REMAP_TABLE_TYPE).tableName.setText(tableName);
    }

    /**
     * Set the Author Rank Table name
     *
     * @param tableName the remap table name
     */
    public void setAuthorRankTableName(String tableName) {
        if (this.specialTableTypeToTableStruct.get(AUTHORS_RANK_TABLE_TYPE) != null)
            this.specialTableTypeToTableStruct.get(AUTHORS_RANK_TABLE_TYPE).tableName.setText(tableName);
    }

    /**
     * Add the schemaPanelChooserButtonPanel panel if this SchemaPanel is part of a SchemaPanelChooser.
     *
     * @param buttonPanel panel with radio buttons to placed in the schemaPanelChooserButtonPanel
     */
    protected void setSchemaPanelChooserButtonPanel(JPanel buttonPanel) {
        if (!this.containedInSchemaPanelChooser)
            return;
        this.schemaPanelChooserButtonPanel = buttonPanel;
        this.daoPanel.add(buttonPanel, BorderLayout.NORTH);
    }

    /**
     * Specifies that this SchemaPanel is contained in a SchemaPanelChooser. See comments accompanying
     * containedInSchemaPanelChooser declaration for more information.
     */
    protected void setContainedInSchemaPanelChooser() {
        this.containedInSchemaPanelChooser = true;
    }

    /** *************************** TOOLTIP METHODS ***************************** */
    /**
     * Set default tool tips. These are all set in one method instead of in each of the individual classes/creation
     * methods since it seems more logical to have all tooltip related information in one place.
     */
    private void setToolTips() {
        // Use a string to keep track of the tool tip text so that we don't have typos or differences
        // showing up in tool tips for components that are supposed to have the same tool tips.

        // connection toggle
        String toolTipText = "<html>Select a configuration. These configurations are loaded from "
                + "the following locations (in order):<br>(1) A file specified in the KBDB_ACCOUNTS_FILE "
                + "environment parameter,<br>(2) A file specified in the user's home directory with the same "
                + "name as the file in the KBDB_ACCOUNTS_FILE parameter or kbdb.cfg if that parameter does "
                + "not exist,<br>(3) a file in the current working directory using the criteria for file names "
                + "outlined in (2).<br>If configuration information by the same name exists in more than "
                + "one file, the most recently read file's information will replace the previous file's "
                + "information.<br>The only table definition table information read from these configuration "
                + "files is the table definition table <b>name</b> (no connection or file information).</html>";
        this.configurationComboBoxLabel.setToolTipText(toolTipText);
        this.configurationComboBox.setToolTipText(toolTipText);

        toolTipText = "Click here to hide the connection information details "
                + "(username, password, instance, driver).";
        this.connectionToggle.setToolTipText(toolTipText);

        toolTipText = "<html>Click here to hide the table definition table connection information "
                + "details (username, password, instance, driver).<br>The table definition table is "
                + "a view into the schema schema tables and is used to define the structure of "
                + "tables in the schema.</html>";
        this.tableDefToggle.setToolTipText(toolTipText);
        if (this.type == OUTPUT) {
            // auto table creation
            toolTipText = "Create tables in the database if they do not already exist";
            this.autoTableCreation.setToolTipText(toolTipText);

            // clear table data
            toolTipText = "Delete data from tables that are presently in the database and are included in the schema";
            this.clearTableData.setToolTipText(toolTipText);

            // disable renumbering of ids
            toolTipText = "Do not renumber ids when they are added to this schema";
            this.disableRenumbering.setToolTipText(toolTipText);
        }

        this.autoFillLabel.setToolTipText("Select Auto-Fill checkboxes for all tables");
        if (this.modifiable) {
            if (this.type == INPUT) {
                // top level table
                toolTipText = "Table that RowGraphs should start from";
                this.topLevelTableLabel.setToolTipText(toolTipText);
                this.topLevelTable.setToolTipText(toolTipText);

                // query
                toolTipText = "Restrict what data is read in (SQL query)";
                this.queryLabel.setToolTipText(toolTipText);
                this.query.setToolTipText(toolTipText);
            }

            // tables toggle button
            updateSelectTablesToolTipText();

            // relationships toggle button
            updateRelationshipsToolTipText();
        }

        // idgaps
        GUITableStruct idgapsTable = this.specialTableTypeToTableStruct.get(IDGAPS_TABLE_TYPE);
        if (idgapsTable != null) {
            toolTipText = "Table to retrieve new ids from";
            idgapsTable.tableType.setToolTipText(toolTipText);
            idgapsTable.tableName.setToolTipText(toolTipText);

            ((IdGapsGUITableStruct) idgapsTable).useSequencesToggle
                    .setToolTipText("Click in order to specify sequences " + "to use instead of an idgaps table.");
        }

        // remap
        RemapGUITableStruct remapTable = (RemapGUITableStruct) this.specialTableTypeToTableStruct.get(REMAP_TABLE_TYPE);
        if (remapTable != null) {
            toolTipText = "Table to write remap information to";
            remapTable.tableType.setToolTipText(toolTipText);
            remapTable.tableName.setToolTipText(toolTipText);
            toolTipText = "<html>Value used to populate the SOURCE field of the remap table. If the value contains "
                    + "the substring #ACCOUNT#, that substring will be replaced with<br>the name of the account that is "
                    + "associated with each source table. If Remap Source contains the substring #TABLE#, then that "
                    + "substring will be<br>replaced with the name of the table that the row came from. Then the resulting "
                    + "string is used to populate the source column of the remap table.</html>";
            remapTable.remapSourceLabel.setToolTipText(toolTipText);
            remapTable.remapSource.setToolTipText(toolTipText);

            toolTipText = "<html>The lddate to use for the remap table. Remap table's unique key is source, id_name, "
                    + "original_id, current_id, and lddate.<br>By manually dictating what the lddate will be across "
                    + "multiple runs of / EvLoader, the user can in effect cause this change in unique key behavior.<br>"
                    + "This parameter must be in yyyy-MM-dd HH:mm:ss format.</html>";
            remapTable.remapLddateLabel.setToolTipText(toolTipText);
            remapTable.remapLddate.setToolTipText(toolTipText);
        }

        // author rank
        GUITableStruct authorRankTable = this.specialTableTypeToTableStruct.get(AUTHORS_RANK_TABLE_TYPE);
        if (authorRankTable != null) {
            toolTipText = "Table to retrieve new author ranking information from";
            authorRankTable.tableType.setToolTipText(toolTipText);
            authorRankTable.tableName.setToolTipText(toolTipText);
        }

        // Table header labels
        // use table
        toolTipText = "When checked for a table, that table will be included in the schema";
        this.useTableLabel.setToolTipText(toolTipText);
        this.specialUseTableLabel.setToolTipText(toolTipText);

        // table type
        toolTipText = "Table type";
        this.tableTypeLabel.setToolTipText(toolTipText);
        this.specialTableTypeLabel.setToolTipText(toolTipText);

        // auto fill
        toolTipText = "<html>When checked for a table, that table's name will auto fill based<br>"
                + "on other table names. Click here to select/deselect all tables";
        this.autoFillLabel.setToolTipText(toolTipText);
        this.specialAutoFillLabel.setToolTipText(toolTipText);

        // table name
        toolTipText = "Table name";
        this.tableNameLabel.setToolTipText(toolTipText);
        this.specialTableNameLabel.setToolTipText(toolTipText);
    }

    /**
     * Set query tool tip text
     *
     * @param toolTipText tool tip text to use for the query label and query textarea
     */
    public void setQueryToolTipText(String toolTipText) {
        if (this.queryLabel != null)
            this.queryLabel.setToolTipText(toolTipText);
        if (this.query != null)
            this.query.setToolTipText(toolTipText);
    }

    /**
     * Update the tool tip text for the Relationships button (if the SchemaPanel is modifiable) that shows/hides
     * relationship information based on what state the button is in It might seem silly to have a whole method for
     * this, but these tool tips need to be set from multiple places.
     */
    private void updateRelationshipsToolTipText() {
        if (this.relationshipsMatrixPanel.detailedRelationshipsVisible())
            this.relationshipsMatrixPanelToggle.setToolTipText("Hide add / remove relationships");
        else
            this.relationshipsMatrixPanelToggle.setToolTipText("Show add / remove relationships");
    }

    /**
     * Update the tool tip text for the SelectTableTypes "Tables" button (if the SchemaPanel is modifiable) that
     * shows/hides Table information based on what state the button is in. It might seem silly to have a whole method
     * for this, but these tool tips need to be set from multiple places.
     */
    private void updateSelectTablesToolTipText() {
        if (this.selectTableTypesVisible)
            this.selectTableTypesPanelToggle
                    .setToolTipText("<html>Hide selected/available table types<br>Refresh available tables list using current Table Definition "
                            + "Table information</html>");
        else
            this.selectTableTypesPanelToggle.setToolTipText("Show selected/available table types");
    }

    /** ************************** PARAMETER RELATED METHODS **************************** */
    /**
     * Register GUI components with the ParInfoGui object. This keeps this ParInfoGui object up to date whenever the
     * corresponding GUI component changes and it provides a way to update all of the GUI componenets from a ParInfoGui
     * object.
     */
    private void registerComponents() {
        if (this.parNamePrefix == null)
            this.parNamePrefix = typeToString() + "_";

        if (this.type == INPUT) {
            this.parInfoGui.registerComponent(this.topLevelTable, this.parNamePrefix + ParInfo.TOP_LEVEL_TABLE);
            this.parInfoGui.registerComponent(this.query, this.parNamePrefix + ParInfo.QUERY);
        }
        if (this.type == OUTPUT) {
            this.parInfoGui.registerComponent(this.autoTableCreation, this.parNamePrefix + ParInfo.AUTO_TABLE_CREATION);
            this.parInfoGui.registerComponent(this.clearTableData, this.parNamePrefix + ParInfo.TRUNCATE_TABLES);
            // Disable renumbering of ids is not present here since it doesn't correspond directly to a parameter.
            // Instead, it dictates what the idgaps table parameter gets set to. If it is checked, the idgaps table
            // name gets set to DO_NOT_RENUMBER_IDS. If the idgaps table name coming in from a parameter file is
            // null, then this box gets checked
        }
        // Register GUI components speciic to instantiating class. This handles the DAO information.
        registerSubComponents();
        registerTableDefComponents();
    }

    /**
     * Register the Table Definition Table components - either for this SchemaPanel's TableDefPanel or for the currently
     * selected TableDefPanel in a TableDefPanelChooser. TableDefPanel's inside the SchemaPanel have to be handled a tad
     * differently since they are a removable component that can be removed from and/or replaced within the SchemaPanel.
     * This register method gets called more often than SchemaPanel's registerComponents due to the weird nature of
     * TableDefPanelChoosers. The craziest example being when this there is a SchemaPanelChooser with three
     * SchemaPanel's in it and each of those SchemaPanel's has a TableDefChooser in them. That's 3 TableDefChooers, each
     * with up to two TableDefPanel's in them for a total of 6 TableDefPanels. So, each TableDefPanelDB in each
     * SchemaPanel has its components synchronized to the same parameter name, and all 6 TableDefPanels synchornize
     * their Table Definition Table name to the same parameter name. This causes issues when it comes time to update
     * that panel with information from a ParInfo object or to synchronize a ParInfoGui object with the components in
     * that panel since only one association can exist. We get around this by re-registering the TableDefPanel
     * components any time the TableDefPanel gets switched out or when information is read in.
     */
    private void registerTableDefComponents() {
        getTableDefPanel().registerComponents(this.parNamePrefix, this.parInfoGui);
    }

    /**
     * Return a {@link ParInfo ParInfo} object representing the information currently contained in this SchemaPanel.
     * Please see comments for the {@link #SchemaPanel(ParInfo, String) SchemaPanel(ParInfo,String)} constructor since
     * the parameters that it accepts are the same parameters that this method returns. Note that a SchemaPanel cannot
     * be instantiated since it is abstract, so more parameters might be returned than those listed based on the type of
     * SchemaPanel that was created and depending on whether or not this SchemaPanel has a table definition table or
     * not.
     *
     * @return a ParInfo object representing the information contained in this SchemaPanel. Any parameters that have
     * been added with "" values are removed.
     */
    public ParInfo getParInfo() {
        // Update this.parInfoGui to be synchronized with the corresponding GUI
        // component values
        if (this.type == INPUT) {
            this.parInfoGui.synchParInfo(this.topLevelTable);
            this.parInfoGui.synchParInfo(this.query);
        }
        if (this.type == OUTPUT) {
            this.parInfoGui.synchParInfo(this.autoTableCreation);
            this.parInfoGui.synchParInfo(this.clearTableData);
            // Disable renumbering of ids is not present here since it doesn't correspond directly to a parameter.
            // Instead, it dictates what the idgaps table parameter gets set to.
        }

        // Update this.parInfoGui to be synchronized with the corresponding GUI component values in instantiating
        // classes
        synchSubParInfo();

        // Update this.parInfoGui to be synchronized with the corresponding GUI component values in the table definition
        // table
        getTableDefPanel().synchParInfo(this.parInfoGui);

        // Create the parInfo object to be returned.
        ParInfo parInfo = this.parInfoGui.clone();

        // Add the DAO type (synchSubParInfo handles the non-type DAO info)
        parInfo.addParameter(this.parNamePrefix + ParInfo.DAO_TYPE, getDAOType());
        parInfo.addParameter(this.parNamePrefix + ParInfo.TABLE_DEFINITION_TABLE + ParInfo.DAO_TYPE, getTableDefPanel()
                .getDAOType());

        // Make sure that prompting before truncating is turned off since the prompt goes to the console, not the GUI.
        // (This results in the GUI looking locked if the users don't know to look at the console.)
        parInfo.addParameter(this.parNamePrefix + ParInfo.PROMPT_BEFORE_TRUNCATE, "false");

        addTablesToParInfo(parInfo);
        addRelationshipsToParInfo(parInfo);
        addSpecialTablesToParInfo(parInfo);
        addFixForeignKeysToParInfo(parInfo);

        // If no top level table is specified, then don't leave the top level table in parInfo
        if (parInfo.getItem(this.parNamePrefix + ParInfo.TOP_LEVEL_TABLE, "").equals(NO_TOP_LEVEL_TABLE))
            parInfo.removeParameter(this.parNamePrefix + ParInfo.TOP_LEVEL_TABLE);

        // Remove parameters that have been added with "" values
        ArrayList<String> parametersToRemove = new ArrayList<String>();
        for (String parName : parInfo.keySet())
            if (parInfo.getItem(parName).length() == 0)
                parametersToRemove.add(parName);
        for (String parName : parametersToRemove)
            parInfo.removeParameter(parName);

        return parInfo;
    }

    /**
     * Add the tables currently represented in the SchemaPanel to the ParInfo object as a {@link ParInfo#TABLES
     * ParInfo.Tables} parameter, prefixed by this SchemaPanel's parNamePrefix. This will add the following information
     * about the tables: <br>
     * Whether or not use table is selected <br>
     * Table Type <br>
     * Whether or not auto fill is selected <br>
     * Table Name
     *
     * @param parInfo ParInfo object to add tables information to.
     */
    private void addTablesToParInfo(ParInfo parInfo) {
        // ----- TABLES -----
        // Populate the Tables, UseTableTypes, and AutoFillTableTypes parameters
        parInfo.removeParameter(this.parNamePrefix + ParInfo.TABLES);
        parInfo.removeParameter(this.parNamePrefix + ParInfo.USE_TABLE_TYPES);
        parInfo.removeParameter(this.parNamePrefix + ParInfoGui.AUTO_FILL_TABLE_TYPES);

        // Table related information
        StringBuilder tables = new StringBuilder();
        StringBuilder useTableTypes = new StringBuilder();
        StringBuilder autoFillTableTypes = new StringBuilder();

        for (GUITableStruct tableStruct : this.tableTypeToGUITableStruct.values()) {
            // See if the table is selected
            if (tableStruct.useTable.isSelected())
                useTableTypes.append(tableStruct.tableType.getText() + ",");

            // See if the table's Auto-Fill checkbox is selected.
            if (tableStruct.autoFill.isSelected())
                autoFillTableTypes.append(tableStruct.tableType.getText() + ",");

            // See if the table has a name ... if it doesn't, add a null table
            // name parameter
            if (tableStruct.tableName.getText().trim().length() == 0)
                tables.append("null " + tableStruct.tableType.getText() + DBDefines.EOLN);
                // See if the table name has spaces in it. If so, surrounded it with quotes
            else if (tableStruct.tableName.getText().trim().contains(" "))
                tables.append("\"" + tableStruct.tableName.getText() + "\" " + tableStruct.tableType.getText()
                        + DBDefines.EOLN);
            else
                tables.append(tableStruct.tableName.getText() + " " + tableStruct.tableType.getText() + DBDefines.EOLN);
        }

        // If the tables parameter has anything in it, add it to the ParInfo
        // object.
        if (tables.length() > 0)
            parInfo.addParameter(this.parNamePrefix + ParInfo.TABLES, tables.toString());

        // If the useTableTypes parameter is non-empty, add it to the parInfo
        // object after removing the last comma
        if (useTableTypes.length() > 0)
            parInfo.addParameter(this.parNamePrefix + ParInfo.USE_TABLE_TYPES, useTableTypes.substring(0, useTableTypes
                    .length() - 1));

        // If the autoFillTableTypes parameter is non-empty, add it to the
        // parInfo object after removing the last comma
        if (autoFillTableTypes.length() > 0)
            parInfo.addParameter(this.parNamePrefix + ParInfoGui.AUTO_FILL_TABLE_TYPES, autoFillTableTypes.substring(0,
                    autoFillTableTypes.length() - 1));
    }

    /**
     * Add the relationships currently represented in the SchemaPanel to the ParInfo object as a
     * {@link ParInfo#RELATIONSHIPS ParInfo.Relationships} parameter, prefixed by this SchemaPanel's parNamePrefix
     *
     * @param parInfo ParInfo object to add relationships information to.
     */
    private void addRelationshipsToParInfo(ParInfo parInfo) {
        // Populate the relationships parameter
        parInfo.removeParameter(this.parNamePrefix + ParInfo.RELATIONSHIPS);

        StringBuilder relationshipsParameter = new StringBuilder();
        for (RelationshipStruct relationshipStruct : this.relationshipsMatrixPanel.getRelationshipStructs())
            relationshipsParameter.append(relationshipStruct.id + " " + relationshipStruct.sourceTableType + " "
                    + relationshipStruct.targetTableType + " " + relationshipStruct.whereClause + " "
                    + relationshipStruct.constraint + DBDefines.EOLN);

        // If the relationships parameter is non-empty, add it to the parInfo object.
        if (relationshipsParameter.length() > 0)
            parInfo.addParameter(this.parNamePrefix + ParInfo.RELATIONSHIPS, relationshipsParameter.toString());
    }

    /**
     * Add information about the "special" tables (remap, idgaps, author rank) to the ParInfo object. This adds the
     * following parameters to the ParInfo object, prefixed by this SchemaPanel's parNamePrefix:<br>
     * {@link ParInfo#REMAP_TABLE_USE_TABLE ParInfo.REMAP_TABLE_USE_TABLE} <br> {@link ParInfoGui#REMAP_TABLE_AUTO_FILL
     * ParInfoGui.REMAP_TABLE_AUTO_FILL} <br> {@link ParInfo#REMAP_TABLE ParInfo.REMAP_TABLE} <br> {@link ParInfo#REMAP_SOURCE
     * ParInfo.REMAP_TABLE_SOURCE} <br> {@link ParInfo#REMAP_LDDATE ParInfo.REMAP_TABLE_LDDATE} <br>
     * TODO: Add sequence information {@link ParInfo#IDGAPS_TABLE_USE_TABLE ParInfo.IDGAPS_TABLE_USE_TABLE} <br>
     * {@link ParInfoGui#IDGAPS_TABLE_AUTO_FILL ParInfoGui.IDGAPS_TABLE_AUTO_FILL} <br> {@link ParInfo#IDGAPS_TABLE
     * ParInfo.IDGAPS_TABLE} <br> {@link ParInfo#RANKING_TABLE_USE_TABLE ParInfo.RANKING_TABLE_USE_TABLE} <br>
     * {@link ParInfoGui#RANKING_TABLE_AUTO_FILL ParInfoGui.RANKING_TABLE_AUTO_FILL} <br> {@link ParInfo#RANKING_TABLE
     * ParInfo.RANKING_TABLE} <br>
     *
     * @param parInfo
     */
    private void addSpecialTablesToParInfo(ParInfo parInfo) {
        // Set idgaps, remap, author_rank table information for the special tables that exist/are visible

        // -- REMAP
        GUITableStruct tableStruct = this.specialTableTypeToTableStruct.get(REMAP_TABLE_TYPE);
        parInfo.removeParameter(this.parNamePrefix + ParInfo.REMAP_TABLE);
        parInfo.removeParameter(this.parNamePrefix + ParInfo.REMAP_TABLE_USE_TABLE);
        parInfo.removeParameter(this.parNamePrefix + ParInfoGui.REMAP_TABLE_AUTO_FILL);
        parInfo.removeParameter(this.parNamePrefix + ParInfo.REMAP_SOURCE);
        parInfo.removeParameter(this.parNamePrefix + ParInfo.REMAP_LDDATE);

        // Only add the remap table information if this SchemaPanel was constructed with a remap table
        if (tableStruct != null) {
            if (tableStruct.useTable.isSelected())
                parInfo.addParameter(this.parNamePrefix + ParInfo.REMAP_TABLE_USE_TABLE, "true");
            else
                parInfo.addParameter(this.parNamePrefix + ParInfo.REMAP_TABLE_USE_TABLE, "false");
            if (tableStruct.autoFill.isSelected())
                parInfo.addParameter(this.parNamePrefix + ParInfoGui.REMAP_TABLE_AUTO_FILL, "true");
            else
                parInfo.addParameter(this.parNamePrefix + ParInfoGui.REMAP_TABLE_AUTO_FILL, "false");

            RemapGUITableStruct remapTableStruct = (RemapGUITableStruct) tableStruct;
            if (remapTableStruct.tableName.getText().length() > 0)
                parInfo.addParameter(this.parNamePrefix + ParInfo.REMAP_TABLE, remapTableStruct.tableName.getText());
            if (remapTableStruct.remapSource.getText().length() > 0)
                parInfo.addParameter(this.parNamePrefix + ParInfo.REMAP_SOURCE, remapTableStruct.remapSource.getText());
            if (remapTableStruct.remapLddate.getText().length() > 0)
                parInfo.addParameter(this.parNamePrefix + ParInfo.REMAP_LDDATE, remapTableStruct.remapLddate.getText());
        }

        // -- IDGaps
        tableStruct = this.specialTableTypeToTableStruct.get(IDGAPS_TABLE_TYPE);
        parInfo.removeParameter(this.parNamePrefix + ParInfo.IDGAPS_TABLE);
        parInfo.removeParameter(this.parNamePrefix + ParInfo.IDGAPS_TABLE_USE_TABLE);
        parInfo.removeParameter(this.parNamePrefix + ParInfoGui.IDGAPS_TABLE_AUTO_FILL);
        parInfo.removeParameter(this.parNamePrefix + ParInfo.NEXT_ID_SEQUENCES);

        // Only add the idgaps table information if this SchemaPanel was constructed with an idgaps table
        if (tableStruct != null) {
            if (tableStruct.useTable.isSelected())
                parInfo.addParameter(this.parNamePrefix + ParInfo.IDGAPS_TABLE_USE_TABLE, "true");
            else
                parInfo.addParameter(this.parNamePrefix + ParInfo.IDGAPS_TABLE_USE_TABLE, "false");

            if (tableStruct.autoFill.isSelected())
                parInfo.addParameter(this.parNamePrefix + ParInfoGui.IDGAPS_TABLE_AUTO_FILL, "true");
            else
                parInfo.addParameter(this.parNamePrefix + ParInfoGui.IDGAPS_TABLE_AUTO_FILL, "false");

            // IdGapsGUITableStruct idGapsTableStruct = (IdGapsGUITableStruct) tableStruct;
            if (tableStruct.tableName.getText().length() > 0)
                if (tableStruct.tableName.getText().equals(DO_NOT_RENUMBER_IDS))
                    parInfo.addParameter(this.parNamePrefix + ParInfo.IDGAPS_TABLE, "null");
                else
                    parInfo.addParameter(this.parNamePrefix + ParInfo.IDGAPS_TABLE, tableStruct.tableName.getText());

            IdGapsGUITableStruct idGapsTable = (IdGapsGUITableStruct) tableStruct;
            if (idGapsTable.useSequencesToggle.getText().equals(IdGapsGUITableStruct.HIDE_SEQUENCES_TEXT)) {
                StringBuilder sequenceInfo = new StringBuilder();
                for (String idName : idGapsTable.idNameToTextFields.keySet()) {
                    JTextField[] idNameInfo = idGapsTable.idNameToTextFields.get(idName);
                    sequenceInfo.append(idNameInfo[0].getText() + " " + idNameInfo[1].getText() + DBDefines.EOLN);
                }
                if (sequenceInfo.length() > 0)
                    parInfo.addParameter(this.parNamePrefix + ParInfo.NEXT_ID_SEQUENCES, sequenceInfo.toString());
            }
        }

        // -- Author Rank
        tableStruct = this.specialTableTypeToTableStruct.get(AUTHORS_RANK_TABLE_TYPE);
        parInfo.removeParameter(this.parNamePrefix + ParInfo.RANKING_TABLE);
        parInfo.removeParameter(this.parNamePrefix + ParInfo.RANKING_TABLE_USE_TABLE);
        parInfo.removeParameter(this.parNamePrefix + ParInfoGui.RANKING_TABLE_AUTO_FILL);

        // Only add the author rank table information if this SchemaPanel was constructed with an author rank table
        if (tableStruct != null) {
            if (tableStruct.useTable.isSelected())
                parInfo.addParameter(this.parNamePrefix + ParInfo.RANKING_TABLE_USE_TABLE, "true");
            else
                parInfo.addParameter(this.parNamePrefix + ParInfo.RANKING_TABLE_USE_TABLE, "false");

            if (tableStruct.autoFill.isSelected())
                parInfo.addParameter(this.parNamePrefix + ParInfoGui.RANKING_TABLE_AUTO_FILL, "true");
            else
                parInfo.addParameter(this.parNamePrefix + ParInfoGui.RANKING_TABLE_AUTO_FILL, "false");
        }
    }

    /**
     * Add information about foreign keys that need to be fixed to the ParInfo object. This adds the following
     * parameter, prefixed by this SchemaPanel's parNamePrefix: <br> {@link ParInfo#FIX_FOREIGN_KEYS ParInfo.FixForeignKeys}
     *
     * @param parInfo
     */
    private void addFixForeignKeysToParInfo(ParInfo parInfo) {
        parInfo.removeParameter(this.parNamePrefix + ParInfo.FIX_FOREIGN_KEYS);

        // Add fix foreign keys information (if applicable)
        if (this.type == INPUT) {
            StringBuilder parameter = new StringBuilder();
            for (String tableName : this.fixedForeignKeys.keySet()) {
                parameter.append(tableName + " ");
                StringBuilder currentTablesFKs = new StringBuilder();
                for (String columnName : this.fixedForeignKeys.get(tableName))
                    currentTablesFKs.append(columnName + ",");

                // Remove last comma at the end, add an EOLN to indicate the end
                // of FKs for the current table, and continue on!
                String temp = currentTablesFKs.toString().substring(0, currentTablesFKs.length() - 1) + DBDefines.EOLN;
                parameter.append(temp);
            }

            if (parameter.length() > 0)
                parInfo.addParameter(this.parNamePrefix + ParInfo.FIX_FOREIGN_KEYS, parameter.toString());
        }
    }

    /**
     * Update the parameters in the SchemaPanel based on the information in the ParInfo object. This method calls
     * {@link #updateParameters(ParInfo, String) updateParameters} with an empty string prefix. Please see that method
     * for more information.
     *
     * @param parInfo ParInfo object to use to update the SchemaPanel
     */
    public void updateParameters(ParInfo parInfo) {
        updateParameters(parInfo, "");
    }

    /**
     * Update the parameters in the SchemaPanel based on the information in the ParInfo object. Please see comments for
     * the {@link #SchemaPanel(ParInfo, String) SchemaPanel(ParInfo,String)} constructor since the parameters that it
     * accepts are the same parameters that this method updates.
     *
     * @param parInfo ParInfo object to use to update the SchemaPanel
     * @param prefix  prefix to prepend to the parameter names listed above when accessing parameters from the ParInfo
     *                object
     */
    public void updateParameters(ParInfo parInfo, String prefix) {
        DBUtilLibListener.setListenersOn(false);

        // TODO: Pull sequence information out of ParInfo
        this.parNamePrefix = prefix;

        restoreDefaults();

        ArrayList<TableStruct> tableStructs = createTableStructs(parInfo);
        ArrayList<RelationshipStruct> relationshipStructs = createRelationshipStructs(parInfo);

        setTables(tableStructs);
        updateAutoFillFromParInfo(parInfo);
        updateUseTableFromParInfo(parInfo);

        setRelationships(relationshipStructs);
        updateSpecialTablesFromParInfo(parInfo);
        updateForeignKeysFromParInfo(parInfo);

        // Remove existing parameters so that there's no carry over. This method should use only the
        // parameters specified. This way, if a parameter is not specified, then the old value won't
        // hang on.
        ArrayList<String> parametersToRemove = new ArrayList<String>();
        for (String parName : this.parInfoGui.keySet())
            parametersToRemove.add(parName);
        for (String parName : parametersToRemove)
            this.parInfoGui.removeParameter(parName);

        this.parInfoGui.updateAndAddParameters(parInfo);

        // See comments above this methods as to why this is here.
        registerTableDefComponents();

        // Remove parameters not specific to this SchemaPanel since it reduces confusion
        parametersToRemove.clear();
        for (String parName : this.parInfoGui.keySet())
            if (!parName.toLowerCase().startsWith(this.parNamePrefix.toLowerCase()))
                parametersToRemove.add(parName);
        for (String parName : parametersToRemove)
            this.parInfoGui.removeParameter(parName);

        // Make sure everything retains its proper visibility
        refreshVisibility();

        DBUtilLibListener.setListenersOn(true);
    }

    /**
     * Restore the "default" state of GUI components. This method is needed because when a parameter is removed via
     * {@link ParInfoGui#removeParameter(String) ParInfoGui.removeParameter(String)}, this removal does not reset the
     * gui component to a non-selected state. So, some of the SchemaPanel components need to be set to an initial state.
     */
    private void restoreDefaults() {
        if (this.type == OUTPUT) {
            this.autoTableCreation.setSelected(false);
            this.disableRenumbering.setSelected(false);
            this.clearTableData.setSelected(false);
        }
    }

    /**
     * GUITableStructs start with their use table checkbox selected, but this is not the desired state for special
     * tables.
     */
    private void deselectSpecialTablesUseTables() {
        // In order to trigger the change listener, there must be a "change". Set to selected and then not selected for
        // the change listener to kick in and disable the special table gui struct information.
        if (this.specialTableTypeToTableStruct.get(IDGAPS_TABLE_TYPE) != null) {
            this.specialTableTypeToTableStruct.get(IDGAPS_TABLE_TYPE).useTable.setSelected(true);
            this.specialTableTypeToTableStruct.get(IDGAPS_TABLE_TYPE).useTable.setSelected(false);
        }
        if (this.specialTableTypeToTableStruct.get(REMAP_TABLE_TYPE) != null) {
            this.specialTableTypeToTableStruct.get(REMAP_TABLE_TYPE).useTable.setSelected(true);
            this.specialTableTypeToTableStruct.get(REMAP_TABLE_TYPE).useTable.setSelected(false);
        }
        if (this.specialTableTypeToTableStruct.get(AUTHORS_RANK_TABLE_TYPE) != null) {
            this.specialTableTypeToTableStruct.get(AUTHORS_RANK_TABLE_TYPE).useTable.setSelected(true);
            this.specialTableTypeToTableStruct.get(AUTHORS_RANK_TABLE_TYPE).useTable.setSelected(false);
        }
    }

    /**
     * Update the auto-fill status of tables in the SchemaPanel with information from the
     * {@link ParInfoGui#AUTO_FILL_TABLE_TYPES ParInfoGui.AUTO_FILL_TABLE_TYPES} parameter, prefixed by this
     * SchemaPanel's parNamePrefix, in parInfo.
     *
     * @param parInfo ParInfo object to extract {@link ParInfoGui#AUTO_FILL_TABLE_TYPES
     *                ParInfoGui.AUTO_FILL_TABLE_TYPES} parameter, prefixed by this SchemaPanel's parNamePrefix, from
     */
    private void updateAutoFillFromParInfo(ParInfo parInfo) {
        String autoFillTableTypes = parInfo.getItem(this.parNamePrefix + ParInfoGui.AUTO_FILL_TABLE_TYPES, null);
        if (autoFillTableTypes == null)
            return;
        for (String tableType : this.tableTypeToGUITableStruct.keySet())
            if (!autoFillTableTypes.contains(tableType))
                this.tableTypeToGUITableStruct.get(tableType).autoFill.setSelected(false);
            else
                this.tableTypeToGUITableStruct.get(tableType).autoFill.setSelected(true);
    }

    /**
     * Update the use table status of tables in the SchemaPanel with information from the
     * {@link ParInfo#USE_TABLE_TYPES ParInfo.USE_TABLE_TYPES} parameter, prefixed by this SchemaPanel's parNamePrefix,
     * in parInfo.
     *
     * @param parInfo ParInfo object to extract {@link ParInfo#USE_TABLE_TYPES ParInfo.UUSE_TABLE_TYPES} parameter,
     *                prefixed by this SchemaPanel's parNamePrefix, from
     */
    private void updateUseTableFromParInfo(ParInfo parInfo) {
        String useTableTypes = parInfo.getItem(this.parNamePrefix + ParInfo.USE_TABLE_TYPES, null);
        if (useTableTypes == null)
            return;
        for (String tableType : this.tableTypeToGUITableStruct.keySet())
            if (!useTableTypes.contains(tableType))
                this.tableTypeToGUITableStruct.get(tableType).useTable.setSelected(false);
            else
                this.tableTypeToGUITableStruct.get(tableType).useTable.setSelected(true);
    }

    /**
     * Update the special table information to reflect what's in the parInfo object.
     *
     * @param parInfo ParInfo object to use to update the special table information
     */
    private void updateSpecialTablesFromParInfo(ParInfo parInfo) {
        updateRemapTableFromParInfo(parInfo);
        updateIdGapsTableFromParInfo(parInfo);
        updateRankingTableFromParInfo(parInfo);
    }

    /**
     * Update the remap table information to reflect what's in the parInfo object.
     *
     * @param parInfo ParInfo object to use to update the remap table information
     */
    private void updateRemapTableFromParInfo(ParInfo parInfo) {
        // No corresponding remap object to update
        if (this.specialTableTypeToTableStruct.get(REMAP_TABLE_TYPE) == null)
            return;

        RemapGUITableStruct remapTable = (RemapGUITableStruct) this.specialTableTypeToTableStruct.get(REMAP_TABLE_TYPE);

        // update table name
        String remapTableName = parInfo.getItem(this.parNamePrefix + ParInfo.REMAP_TABLE, "");
        remapTable.tableName.setText(remapTableName);

        // update use table checkbox -- if no remap use table parameter exists, but there is a remap table name, set the
        // remap use table checkbox to selected. This can be the case if the SchemaPanel is reading a parameter file
        // created from a command line application.
        String remapUseTable = parInfo.getItem(this.parNamePrefix + ParInfo.REMAP_TABLE_USE_TABLE, "");
        if (remapUseTable.startsWith("t") || (remapUseTable.length() == 0 && remapTableName.length() > 0))
            remapTable.useTable.setSelected(true);
        else
            remapTable.useTable.setSelected(false);

        // update auto fill checkbox
        boolean remapAutoFill = parInfo.getItem(this.parNamePrefix + ParInfoGui.REMAP_TABLE_AUTO_FILL, "false")
                .startsWith("t");
        remapTable.autoFill.setSelected(remapAutoFill);

        // update lddate and source
        remapTable.remapLddate.setText(parInfo.getItem(this.parNamePrefix + ParInfo.REMAP_LDDATE));
        remapTable.remapSource.setText(parInfo.getItem(this.parNamePrefix + ParInfo.REMAP_SOURCE));
    }

    /**
     * Update the idgaps table information to reflect what's in the parInfo object.
     *
     * @param parInfo ParInfo object to use to update the idgaps table information
     */
    private void updateIdGapsTableFromParInfo(ParInfo parInfo) {
        // No corresponding idgaps object to update
        if (this.specialTableTypeToTableStruct.get(IDGAPS_TABLE_TYPE) == null)
            return;

        // TODO: Deal with sequences
        IdGapsGUITableStruct idGapsTable = (IdGapsGUITableStruct) this.specialTableTypeToTableStruct
                .get(IDGAPS_TABLE_TYPE);

        // update table name
        String idGapsTableName = parInfo.getItem(this.parNamePrefix + ParInfo.IDGAPS_TABLE, "");
        idGapsTable.tableName.setText(idGapsTableName);

        // update use table checkbox -- if no idgaps use table parameter exists, but there is a idgaps table name, set
        // the idgaps use table checkbox to selected. This can be the case if the SchemaPanel is reading a parameter
        // file/ created from a command line application.
        String idGapsUseTable = parInfo.getItem(this.parNamePrefix + ParInfo.IDGAPS_TABLE_USE_TABLE, "");
        if (idGapsUseTable.startsWith("t") || (idGapsUseTable.length() == 0 && idGapsTableName.length() > 0))
            idGapsTable.useTable.setSelected(true);
        else
            idGapsTable.useTable.setSelected(false);

        // update auto fill checkbox
        boolean idGapsAutoFill = parInfo.getItem(this.parNamePrefix + ParInfoGui.IDGAPS_TABLE_AUTO_FILL, "false")
                .startsWith("t");
        idGapsTable.autoFill.setSelected(idGapsAutoFill);

        // If the parameter file has an idgaps table set to null, that indicates that ids should not be renumbered
        if (parInfo.getItem(this.parNamePrefix + ParInfo.IDGAPS_TABLE, "").toLowerCase().equals("null")) {
            this.disableRenumbering.setSelected(true);
            idGapsTable.tableName.setText(DO_NOT_RENUMBER_IDS);
            idGapsTable.setEnabled(false);
        } else
            idGapsTable.tableName.setText(parInfo.getItem(this.parNamePrefix + ParInfo.IDGAPS_TABLE, ""));

        // Sequences
        String sequenceInformation = parInfo.getItem(this.parNamePrefix + ParInfo.NEXT_ID_SEQUENCES, null);
        if (sequenceInformation != null) {
            String[] sequences = DBDefines.splitOnNewLine(sequenceInformation);
            for (String sInfo : sequences) {
                String[] s = DBDefines.removeExtraSpaces(sInfo).split(" ");
                String idName = s[0];
                String sequenceName = s[1];
                idGapsTable.idNameToTextFields.put(idName, new JTextField[]{GUI_Util.textField_short(idName),
                        GUI_Util.textField_medium(sequenceName)});
            }
            idGapsTable.useSequencesToggle.setSelected(true);
            idGapsTable.showSequences(false);
        }
    }

    /**
     * Update the ranking table information to reflect what's in the parInfo object.
     *
     * @param parInfo ParInfo object to use to update the ranking table information
     */
    private void updateRankingTableFromParInfo(ParInfo parInfo) {
        // No corresponding ranking object to update
        if (this.specialTableTypeToTableStruct.get(AUTHORS_RANK_TABLE_TYPE) == null)
            return;

        GUITableStruct authorRankTable = this.specialTableTypeToTableStruct.get(AUTHORS_RANK_TABLE_TYPE);

        // update table name
        String authorRankTableName = parInfo.getItem(this.parNamePrefix + ParInfo.RANKING_TABLE, "");
        authorRankTable.tableName.setText(authorRankTableName);

        // update use table checkbox -- if no author rank use table parameter exists, but there is a author rank table
        // name, set the author rank use table checkbox to selected. This can be the case if the SchemaPanel is reading
        // a parameter file created from a command line application.
        String authorRankUseTable = parInfo.getItem(this.parNamePrefix + ParInfo.RANKING_TABLE_USE_TABLE, "");
        if (authorRankUseTable.startsWith("t")
                || (authorRankUseTable.length() == 0 && authorRankTableName.length() > 0))
            authorRankTable.useTable.setSelected(true);
        else
            authorRankTable.useTable.setSelected(false);

        // update auto fill checkbox
        boolean authorRankAutoFill = parInfo.getItem(this.parNamePrefix + ParInfoGui.RANKING_TABLE_AUTO_FILL, "false")
                .startsWith("t");
        authorRankTable.autoFill.setSelected(authorRankAutoFill);
    }

    /**
     * Update the fixed foreign keys in the SchemaPanel with information from the {@link ParInfo#FIX_FOREIGN_KEYS
     * ParInfo.FIX_FOREIGN_KEYS} parameter, prefixed by this SchemaPanel's parNamePrefix, in parInfo.
     *
     * @param parInfo ParInfo object to extract {@link ParInfo#FIX_FOREIGN_KEYS ParInfo.FIX_FOREIGN_KEYS} parameter,
     *                prefixed by this SchemaPanel's parNamePrefix, from
     */
    private void updateForeignKeysFromParInfo(ParInfo parInfo) {
        String foreignKeys = parInfo.getItem(this.parNamePrefix + ParInfo.FIX_FOREIGN_KEYS, null);
        if (foreignKeys == null)
            return;

        // foreignKeys = foreignKeys.replaceAll("\t", " ").replaceAll(" *", " ").replaceAll(", ", ",");
        foreignKeys = DBDefines.removeExtraSpaces(foreignKeys.replaceAll("\t", " ").replaceAll("  *", " ")).replaceAll(
                ", ", ",");

        // Get each line of table information (name and type)
        String[] tableFksToFix = DBDefines.splitOnNewLine(foreignKeys);

        HashMap<String, LinkedList<String>> newForeignKeys = new HashMap<String, LinkedList<String>>();
        for (int i = 0; i < tableFksToFix.length; i++) {
            String[] temp = tableFksToFix[i].split(" ");
            String tableName = temp[0];
            String[] colsToFix = temp[1].split(",");

            newForeignKeys.put(tableName, new LinkedList<String>());

            for (int j = 0; j < colsToFix.length; j++) {
                String columnName = colsToFix[j];
                newForeignKeys.get(tableName).add(columnName);
            }
        }

        setFixedForeignKeys(newForeignKeys);
    }

    /** *************************** OTHER METHODS ***************************** */
    /**
     * Create header for normal table information in the {@link #tablesInfoPanel tablesInfoPanel}
     *
     * @param gbc GridBagConstraints grid bag constraints to use when adding information to {@link #tablesInfoPanel
     *            tablesInfoPanel}
     */
    private void createNormalTablesHeader(GridBagConstraints gbc) {
        // Use Table
        this.tablesInfoPanel.add(this.useTableLabel, gbc);
        // Don't show the Use label if all of the use table checkboxes are set to not visible.
        if (!this.useTableCheckBoxesVisible)
            this.useTableLabel.setVisible(false);

        // Table Type
        GUI_Util.gridBag_right(gbc);
        this.tablesInfoPanel.add(this.tableTypeLabel, gbc);

        // Auto Fill
        GUI_Util.gridBag_right(gbc);
        this.tablesInfoPanel.add(this.autoFillLabel, gbc);

        // Table Name
        GUI_Util.gridBag_right(gbc);
        this.tablesInfoPanel.add(this.tableNameLabel, gbc);
    }

    /**
     * Create header for special table information in the {@link #tablesInfoPanel tablesInfoPanel}
     *
     * @param gbc GridBagConstraints grid bag constraints to use when adding information to {@link #tablesInfoPanel
     *            tablesInfoPanel}
     */
    private void createSpecialTablesHeader(GridBagConstraints gbc) {
        GUI_Util.gridBag_newRow(gbc);

        // Use Table
        this.tablesInfoPanel.add(this.specialUseTableLabel, gbc);
        GUI_Util.gridBag_right(gbc);
        // Table Type
        this.tablesInfoPanel.add(this.specialTableTypeLabel, gbc);

        // Auto Fill
        GUI_Util.gridBag_right(gbc);
        this.tablesInfoPanel.add(this.specialAutoFillLabel, gbc);

        // Table Name
        GUI_Util.gridBag_right(gbc);
        this.tablesInfoPanel.add(this.specialTableNameLabel, gbc);
    }

    /**
     * Removes all table information from this SchemaPanel
     */
    public void removeAllTables() {
        this.tableTypeToGUITableStruct.clear();
        this.tablesInfoPanel.removeAll();
    }

    /**
     * Create a configuration combo box with items that have the same DAO as this SchemaPanel. If this SchemaPanel is
     * part of a SchemaPanelChooser, then all DAO types are acceptable. (All DAO types would be added in the
     * {@link SchemaPanelChooser SchemaPanelChooser} class.) Configuration information can be obtained from the
     * following sources: <br>
     * 1) A file specified in the KBDB_ACCOUNTS_FILE environment parameter. <br>
     * 2) A file in the user's home directory. This file must have the same name as the file specified in the
     * KBDB_ACCOUNTS_FILE environment parameter. If no KBDB_ACCOUNTS_FILE parameter has been specified, the file must be
     * named kbdb.cfg. <br>
     * 3) A file in the current working directory using the criteria listed in #2 to determine the file name to look
     * for. <br>
     * 4) A file specified in the parameter file using the AlternateConfigFile parameter.
     * <p>
     * The files are examined in the order listed above. If configuration information by the same name exists in more
     * than one file, the most recently read file's information will replace the previous file's information.
     *
     * @param parInfo ParInfo object used when creating the SchemaPanel. This method only uses the ParInfo object to
     *                obtain alternate places to look for configuration files.
     */
    protected void initializeConfigurationComboBox(ParInfo parInfo) {
        // Find the KBDB accounts file(s)
        ArrayList<String> filenames = GUI_Util.findKBDB_ACCOUNTS_FILE();

        // See if an alternate configuration file was specified.
        String alternateConfigFile = parInfo.getItem(this.parNamePrefix + ParInfo.ALTERNATE_CONFIG_FILE, "");

        // Add the alternate configuration file information if available
        if (alternateConfigFile.length() > 0) {
            filenames.add(alternateConfigFile);
            this.parInfoGui.addParameter(this.parNamePrefix + ParInfo.ALTERNATE_CONFIG_FILE, alternateConfigFile);
        }

        if (CONFIGURATION_FILENAMES == null)
            CONFIGURATION_FILENAMES = new HashMap<String, TreeSet<String>>();

        // Temporary holding place for config file contents
        Properties properties = new Properties();

        // Initialize configuration ParInfo object
        if (CONFIGURATION_PARINFO == null)
            CONFIGURATION_PARINFO = new ParInfo();

        // Store all of the prefixes from found in the kbdb accounts file
        TreeSet<String> prefixes = new TreeSet<String>();

        // Retrieve all of the configuration information in a config file. Each set of configuration information by
        // the same name as configuration information from a previous file overwrites the existing information.
        for (String filename : filenames) {
            // Don't read in configuration information if it has already been read in - this can happen if two
            // different ways of specifying configuration information specify the same filename.
            if (CONFIGURATION_FILENAMES.get(filename) != null) {
                prefixes.addAll(CONFIGURATION_FILENAMES.get(filename));
                continue;
            }

            properties.clear();
            try {
                properties.load(new FileInputStream(new File(filename)));
            } catch (Exception e) {
                System.err.println("Unable to load file: " + filename);
            }

            // Load information in!
            Enumeration<?> propertyNames = properties.propertyNames();
            CONFIGURATION_FILENAMES.put(filename, new TreeSet<String>());

            while (propertyNames.hasMoreElements()) {
                // Configuration name properties end with .name
                String propertyName = propertyNames.nextElement().toString();
                if (!propertyName.endsWith(".name"))
                    continue;

                // Get the name of the configuration (the value of the propery
                // that ends with .name)
                String name = properties.get(propertyName).toString();
                String prefix = propertyName.substring(0, propertyName.length() - "name".length());

                // Issue warning if we are about to overwrite information.
                if (prefixes.contains(name))
                    System.out.println("\t" + name + " configuration information from filename is overwriting "
                            + "previous " + name + " configuration information");

                // Keep track of the current configuration file name
                CONFIGURATION_FILENAMES.get(filename).add(name);
                // Keep track of the prefix that will be used to access properties related to this configuration
                prefixes.add(name);

                // Extract configuration information and add it to the configurationParInfo - this information will
                // be accessed using the prefix (so, two different "type" properties can exist, but if they have
                // different prefixes, they can both be stored in the same ParIno object)
                // Configuration information being extracted for each configuration: type
                // Information extracted for database configuration: driver, instance, username, password, table
                // definition table

                // type
                String currentProperty = properties.getProperty(prefix + "type");
                if (currentProperty != null)
                    CONFIGURATION_PARINFO.addParameter(name + ".type", currentProperty.trim());
                // driver
                currentProperty = properties.getProperty(prefix + "sql.driver");
                if (currentProperty != null)
                    CONFIGURATION_PARINFO.addParameter(name + ".sql.driver", currentProperty.trim());
                // instance
                currentProperty = properties.getProperty(prefix + "sql.instance");
                if (currentProperty != null)
                    CONFIGURATION_PARINFO.addParameter(name + ".sql.instance", currentProperty.trim());
                // username
                currentProperty = properties.getProperty(prefix + "sql.username");
                if (currentProperty != null)
                    CONFIGURATION_PARINFO.addParameter(name + ".sql.username", currentProperty.trim());
                // password
                currentProperty = properties.getProperty(prefix + "sql.password");
                if (currentProperty != null)
                    CONFIGURATION_PARINFO.addParameter(name + ".sql.password", currentProperty.trim());
                // tabledefinitiontable
                currentProperty = properties.getProperty(prefix + "sql.tabledefinitiontable");
                if (currentProperty != null)
                    CONFIGURATION_PARINFO.addParameter(name + ".sql.tabledefinitiontable", currentProperty.trim());
                // flatfile path
                currentProperty = properties.getProperty(prefix + "flatfile.path");
                if (currentProperty != null)
                    CONFIGURATION_PARINFO.addParameter(name + ".flatfile.path", currentProperty.trim());
            }
        }
        // Add items to the configuration combo box based on this SchemaPanel's daotype.
        populateConfigurationComboBox(prefixes);

        int count = this.configurationComboBox.getItemCount();
        boolean customAdded = false;

        // put custom in the right alpahbetical place
        for (int j = 0; j < count; j++) {
            if (CONFIGURATION_CUSTOM_ITEM.compareTo(this.configurationComboBox.getItemAt(j).toString()) < 0) {
                // Add a placeholder for user's custom settings - when they type in their own settings.
                this.configurationComboBox.insertItemAt(CONFIGURATION_CUSTOM_ITEM, j);
                customAdded = true;
                break;
            }
        }

        // If, after the above loop, the custom item has not been added, it must come alphabetically after everything
        // else. Add it to the end of the list of items in the configurationComboBox.
        if (!customAdded)
            this.configurationComboBox.addItem(CONFIGURATION_CUSTOM_ITEM);

        this.configurationComboBox.setSelectedIndex(0);
    }

    /**
     * Do an "accuracy check" of everything in the panel before it is actually used. These checks include: <br>
     * If the schema type is {@link #INPUT INPUT}, then the tables in the schema must exist. <br>
     * If the schema type is {@link #OUTPUT OUTPUT} or {@link #TARGET TARGET} and the Auto Table Creation box is not
     * checked, then the tables in the schema must exist.
     *
     * @return whether or not the check succeeded (true) or failed (false)
     * @throws FatalDBUtilLibException if an error occurs when checking the Schema
     */
    public boolean check() throws FatalDBUtilLibException {
        boolean status = true;

        // If the type is Database and INPUT, then the tables in that schema should exist. If the type is OUTPUT or
        // TARGET and the autoTableCreation box is not checked, then those tables must exist. Check for this.
        if (getDAOType().equals(DBDefines.DATABASE_DAO)
                && (this.type == INPUT || ((this.type == OUTPUT || this.type == TARGET) && !this.autoTableCreation
                .isSelected()))) {
            ArrayList<String> nonExistentTables = new ArrayList<String>();

            // This crazy schema creation must be this way in order to check for existence of tables. Otherwise,
            // errors are thrown about tables not existing when the schema is created instead of in a place
            // where the error can be displayed back to the user.
            Schema schema = null;
            try {
                schema = new Schema(getParInfo(), this.parNamePrefix);
            } catch (DBDefines.FatalDBUtilLibException e) {
                ParInfo parInfo = getParInfo();
                ArrayList<String> parametersToRemove = new ArrayList<String>();
                for (String parameterName : parInfo.keySet()) {
                    // Since the XML file parameter name is different for input or output, extract
                    // it here for the comparison below
                    String xmlFileParam = "";
                    if (this.type == INPUT)
                        xmlFileParam = this.parNamePrefix + ParInfo.XML_INPUT_FILE;
                    else
                        xmlFileParam = this.parNamePrefix + ParInfo.XML_OUTPUT_FILE;

                    if (!parameterName.equalsIgnoreCase(this.parNamePrefix + ParInfo.DAO_TYPE)
                            && !parameterName.equalsIgnoreCase(this.parNamePrefix + ParInfo.USERNAME)
                            && !parameterName.equalsIgnoreCase(this.parNamePrefix + ParInfo.PASSWORD)
                            && !parameterName.equalsIgnoreCase(this.parNamePrefix + ParInfo.INSTANCE)
                            && !parameterName.equalsIgnoreCase(this.parNamePrefix + ParInfo.DRIVER)
                            && !parameterName.equalsIgnoreCase(xmlFileParam))
                        parametersToRemove.add(parameterName);
                }
                for (String param : parametersToRemove)
                    parInfo.removeParameter(param);

                schema = new Schema(parInfo);
            }

            // Loop over the tables and check for the existence of each table specified.
            for (GUITableStruct tableStruct : getTables()) {
                String tableName = tableStruct.tableName.getText();
                boolean useTable = tableStruct.useTable.isSelected();

                if (useTable && tableName.length() > 0 && !schema.getDAO().tableExists(tableName))
                    nonExistentTables.add(tableName);
            }

            // Loop over special tables and check for existence as well
            for (GUITableStruct specialTableStruct : this.specialTableTypeToTableStruct.values()) {
                String tableName = specialTableStruct.tableName.getText();
                boolean useTable = specialTableStruct.useTable.isSelected();
                if (useTable && tableName.length() > 0 && !schema.getDAO().tableExists(tableName))
                    nonExistentTables.add(tableName);
            }

            // If there are tables that need to be created in order to continue, issue an appropriate message.
            if (nonExistentTables.size() > 0) {
                StringBuilder message = new StringBuilder();
                if (this.type == OUTPUT)
                    message.append("\nThese  tables must either be created before continuing \nor the Auto Table "
                            + "Creation box must be checked:");
                else
                    message.append("These tables must be created before continuing:");

                for (String table : nonExistentTables)
                    message.append("\n    " + table);

                JOptionPane.showMessageDialog(null, message, "Schema Error", JOptionPane.ERROR_MESSAGE);
                status = false;
            }
        }
        return status;
    }

    /**
     * Add a button to this SchemaPanel (next to the Tables button) that will allow the user to mimic the tables and
     * relationships in otherSchemaPanel. If the panel that contains the tables panel does not exist, then this button
     * will not be created.
     *
     * @param otherSchemaPanel the other SchemaPanel whose relationships and tables can be mimiced in this schemaPanel
     * @param buttonText       button text
     */
    public void addMimicSchemaButton(final SchemaPanel otherSchemaPanel, String buttonText) {
        // If there is not a tablesLabelPanel, then there is nowhere to put this button; return.
        if (this.tablesTogglePanel == null)
            return;

        // Create the mimic button
        JButton mimicButton = createMimicSchemaButton(buttonText);

        // Add the listener
        mimicButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mimicSchema(otherSchemaPanel);
            }
        });

        // Add the button
        this.tablesTogglePanel.add(mimicButton);
    }

    /**
     * Add a button to this SchemaPanel (next to the Tables button) that will allow the user to mimic the tables and
     * relationships in the active SchemaPanel in schemaPanelChooser. If the panel that contains the tables panel does
     * not exist, then this button will not be created.
     *
     * @param schemaPanelChooser the SchemaPanelChooser whose active SchemaPanel's relationships and tables can be
     *                           mimiced in this schemaPanel
     * @param buttonText         button text
     */
    public void addMimicSchemaButton(final SchemaPanelChooser schemaPanelChooser, String buttonText) {
        // This method is very similar to the addMimicSchemaButton method that just takes a
        // SchemaPanel (not a SchemaPanelChooser) as a parameter. The reason there is a separate
        // method for SchemaPanelChoosers is because the active SchemaPanel with the information
        // to mimic must be determined at run time.

        // If there is not a tablesLabelPanel, then there is nowhere to put this button; return.
        if (this.tablesTogglePanel == null)
            return;

        // Create the mimic button
        JButton mimicButton = createMimicSchemaButton(buttonText);

        // Add the listener.
        mimicButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Select the active SchemaPanel
                SchemaPanel otherSchemaPanel = schemaPanelChooser.getSelectedSchemaPanel();
                mimicSchema(otherSchemaPanel);
            }
        });

        // Add the button
        this.tablesTogglePanel.add(mimicButton);
    }

    /**
     * Create the mimic schema panel button used by {@link #addMimicSchemaButton addMimicSchemaButton}
     *
     * @param buttonText button's text
     * @return the mimic schema panel button used by {@link #addMimicSchemaButton addMimicSchemaButton}
     */
    private JButton createMimicSchemaButton(String buttonText) {
        JButton mimicButton = GUI_Util.button_noInsets(buttonText);
        mimicButton.setFont(new Font(mimicButton.getFont().getName(), Font.BOLD, 14));
        mimicButton.setForeground(Color.BLUE);
        return mimicButton;
    }

    /**
     * Mimic the tables and relationships in otherSchemaPanel in this SchemaPanel
     *
     * @param otherSchemaPanel SchemaPanel to retrieve tables and relationships from to display in the current
     *                         SchemaPanel
     */
    protected void mimicSchema(SchemaPanel otherSchemaPanel) {
        ArrayList<GUITableStruct> otherTables = new ArrayList<GUITableStruct>();
        ArrayList<RelationshipStruct> otherRelationships = new ArrayList<RelationshipStruct>();

        for (GUITableStruct gts : otherSchemaPanel.getTables())
            otherTables.add(gts);
        otherRelationships = otherSchemaPanel.getRelationships();

        setAllTablesRelationships(otherTables, null, otherRelationships);
    }

    /**
     * Returns a String representation of this Schema's type.
     *
     * @return a String representation of this Schema's type.
     */
    public String typeToString() {
        switch (this.type) {
            case (INPUT):
                return "Input";
            case (OUTPUT):
                return "Output";
            case (TARGET):
                return "Target";
            default:
                return "Unknown type";
        }
    }

    /** ***************************** LISTENERS ******************************* */
    /**
     * Listener for when the user selects or deselects the Auto-Fill Checkbox
     */
    protected class AutoFillLabelListener extends DBUtilLibActionListener {
        @Override
        public void listenersOnActionPerformed(ActionEvent e) {
            ArrayList<GUITableStruct> tables = getTables();
            boolean selected = SchemaPanel.this.autoFillLabel.isSelected();
            for (GUITableStruct table : tables)
                table.autoFill.setSelected(selected);
        }
    }

    /**
     * Listener for when the user clicks on the Show/Hide connection toggle button. Shows the configPanel if it is
     * hidden, hides it if it's showing.
     */
    protected class ConnectionToggleListener implements ActionListener {
        /**
         * Shows/Hides DAO information based on what the DAO toggle button says (if it says "Hide", then the information
         * is hidden ...)
         *
         * @param e event that triggered a call to this listener
         */
        public void actionPerformed(ActionEvent e) {
            // The configToggle has the hide text on it. The user clicked it to hide connection information.
            if (SchemaPanel.this.connectionToggle.getText().equals(HIDE_CONNECTION_TEXT))
                setConnectionVisible(false);
                // The connection toggle has the show text on it. The user clicked it to show connection information.
            else
                setConnectionVisible(true);
        }
    }

    /**
     * Listener for when the user clicks on the Show/Hide Table Definition Table information toggle button.
     */
    protected class TableDefToggleListener implements ActionListener {
        /**
         * Shows/Hides Table Definition Table information based on what the Table Definition Table toggle button says
         * (it if says "Hide", then the information is not hidden ...)
         *
         * @param e event that triggered the call to this listener
         */
        public void actionPerformed(ActionEvent e) {
            // The tableDefToggle has the hide text on it. The user clicked it to hide connection information.
            if (SchemaPanel.this.tableDefToggle.getText().equals(HIDE_TABLE_DEF_TEXT))
                setTableDefVisible(false);
                // The tableDefToggle has the show text on it. The user clicked it to show connection information.
            else
                setTableDefVisible(true);
        }
    }

    /**
     * Listener for when the user clicks on the "Tables" button to add table types when the SchemaPanel is modifiable.
     * This will cause a window to open where the user can select the table types of interest.
     */
    protected class SelectTableTypesListener implements ActionListener {
        /**
         * Open a window where the user can select the table types they wish to see in the SchemaPanel.
         */
        public void actionPerformed(ActionEvent e) {
            JToggleButton tablesButton = (JToggleButton) e.getSource();
            setSelectTableTypesVisible(tablesButton.isSelected());
            repaint();
            revalidate();
        }
    }

    /**
     * Listener for when the user clicks on the "Relationships" button to show relationships when the SchemaPanel is
     * modifiable.
     */
    protected class ShowModifyRelationshipsListener implements ActionListener {
        /**
         * Open a window where the user can see the Relationships.
         */
        public void actionPerformed(ActionEvent e) {
            JToggleButton source = (JToggleButton) e.getSource();
            if (source.isSelected())
                SchemaPanel.this.relationshipsMatrixPanel.setDetailedRelationshipsVisible(true);
            else
                SchemaPanel.this.relationshipsMatrixPanel.setDetailedRelationshipsVisible(false);
            updateRelationshipsToolTipText();
        }
    }

    /**
     * This class handles when the user clicks on the Disable Renumbering of IDs checkbox. If they are selecting the
     * checkbox, then the idgaps table field has its table name set to {@link #DO_NOT_RENUMBER_IDS DO_NOT_RENUMBER_IDS}.
     * If they are deselecting it, the {@link #DO_NOT_RENUMBER_IDS DO_NOT_RENUMBER_IDS} is removed from the idgaps table
     * name. This listener also keeps track of what the idgaps table name was (if anything) before the user clicks on
     * the Disable Renumbering of IDs checkbox. This way, if the user chooses enters and igaps table name, selects the
     * disable renumbering checkbox, and then de-selects the disable renumbering checkbox, the original idgaps table
     * name can be restored.
     */
    protected class DisableRenumberingListener implements ActionListener {
        /**
         * If the user had an idgaps table before they clicked on Disable Renumbering, then that table name should show
         * up again when they deselect Disable Renumbering.
         */
        private String oldIdgapsTable = "";

        public void actionPerformed(ActionEvent e) {
            boolean selected = SchemaPanel.this.disableRenumbering.isSelected();
            GUITableStruct tableStruct = SchemaPanel.this.specialTableTypeToTableStruct.get(IDGAPS_TABLE_TYPE);
            if (tableStruct == null)
                return;

            IdGapsGUITableStruct idgapsTable = (IdGapsGUITableStruct) tableStruct;

            // Disable the idgaps table and add DO_NOT_RENUMBER_IDS to the table name
            if (selected) {
                oldIdgapsTable = idgapsTable.tableName.getText();
                idgapsTable.tableName.setText(DO_NOT_RENUMBER_IDS);
                idgapsTable.setEnabled(false);
            }
            // Enable the idgaps table and remove DO_NOT_RENUMBER_IDS from the table name
            else {
                idgapsTable.tableName.setText(oldIdgapsTable);
                idgapsTable.setEnabled(true);
            }
        }
    }

    /**
     * A way to keep track of when code should trigger the AutoFillListener (doAutoFill == true) and when it should not
     * (doAutoFill == false). This has to be outside of the TableStruct class since it needs to be static, and
     * apparently statics are not allowed in inner classes.
     */
    protected static boolean doAutoFill = true;

    /** **************************** SUBCLASSES ****************************** */
    /**
     * TableStruct class. This class defines the GUI components used to display information for one table. It also has
     * handlers for the different button and checkbox GUI components.
     */
    public class GUITableStruct implements Cloneable {
        /**
         * Whether or not to "use" (include in the Schema) this table
         */
        public JCheckBox useTable;

        /**
         * This table's type
         */
        public JLabel tableType;

        /**
         * Whether or not to "autofill" the table names. If this is selected, then when the users types in one table
         * name, the prefix (anything before the table type) and suffix (anything after the table type) will be
         * retrieved. The other table name boxes will then be populated with prefix+table type+suffix values.
         */
        public JCheckBox autoFill;

        /**
         * This table's name.
         */
        public JTextField tableName;

        /**
         * Browse button for this table. Used to locate a table name.
         */
        public JButton browseButton;

        /**
         * Whether or not this TableStruct is visible.
         */
        public boolean isVisible = true;

        /**
         * Constructor.
         *
         * @param useTable     whether or not to "use" (include in the Schema) this table
         * @param tableType    this table's type
         * @param autoFill     whether or not to "autofill" the table names. If this is selected, then when the user types
         *                     in one table name, the prefix (anything before the table type) and suffix (anything after the table type)
         *                     will be retrieved. The other table name boxes will then be populated with prefix+table type+suffix values.
         * @param tableName    this table's name; if this parameter is null (either a null type or the string "null" - case
         *                     insensitive - the table's name will be set to the empty string)
         * @param browseButton browse button for this table. Used to locate a table name.
         */
        public GUITableStruct(JCheckBox useTable, JLabel tableType, JCheckBox autoFill, JTextField tableName,
                              JButton browseButton) {
            this.useTable = useTable;
            if (!SchemaPanel.this.useTableCheckBoxesVisible)
                this.useTable.setVisible(false);
            this.tableType = tableType;
            this.autoFill = autoFill;
            this.tableName = tableName;

            if (this.tableName == null || this.tableName.getText().equalsIgnoreCase("null"))
                this.tableName.setText("");

            this.browseButton = browseButton;

            this.useTable.addActionListener(new UseTableListener(this));
            this.autoFill.addActionListener(new AutoFillActionListener(tableType.getText()));
            this.autoFill.addChangeListener(new AutoFillActionListener(tableType.getText()));
            this.tableName.getDocument().addDocumentListener(new AutoFillDocumentListener(tableType.getText()));
        }

        /**
         * Returns a String representation of this GUITableStruct object
         *
         * @return a String representation of this GUITableStruct object
         */
        @Override
        public String toString() {
            return DBDefines.EOLN + "\tUse Table: " + useTable.isSelected() + DBDefines.EOLN + "\tTable Type: "
                    + tableType.getText() + DBDefines.EOLN + "\tAuto-Fill: " + autoFill.isSelected() + DBDefines.EOLN
                    + "\tTable Name: " + tableName.getText();
        }

        /*
         * Set the visibility on all of the GUI components for this TableStruct @param visible if true, all components
         * for this TableStruct are visible; if false, all components for this TableStruct are not visible
         */
        public void setVisible(boolean visible) {
            isVisible = visible;
            // Only show the checkboxes if the SchemaPanel is an "acceptable to show use table checkboxes" state.
            if (SchemaPanel.this.useTableCheckBoxesVisible)
                useTable.setVisible(visible);
            tableType.setVisible(visible);
            autoFill.setVisible(visible);
            tableName.setVisible(visible);
            browseButton.setVisible(visible);
        }

        /**
         * Add this table struct information to the specified panel
         *
         * @param panel panel to add table struct information to
         * @param gbc   GridBagConstraints to use when adding this table struct information to panel
         */
        public void addToPanel(JPanel panel, GridBagConstraints gbc) {
            // Use Table
            panel.add(useTable, gbc);
            GUI_Util.gridBag_right(gbc);

            // TableType
            GUI_Util.gridBag_northwestAnchor(gbc);
            panel.add(tableType, gbc);
            GUI_Util.gridBag_centerAnchor(gbc);
            GUI_Util.gridBag_right(gbc);

            // Auto fill checkbox
            panel.add(autoFill, gbc);
            GUI_Util.gridBag_right(gbc);

            // Table Name
            panel.add(tableName, gbc);
            GUI_Util.gridBag_right(gbc);

            // Table Name Browse Button
            browseButton.setPreferredSize(new Dimension(browseButton.getPreferredSize().width, tableName
                    .getPreferredSize().height));
            panel.add(browseButton, gbc);
        }

        /**
         * Create a clone of this GUITableStruct.
         */
        @Override
        public Object clone() {
            JCheckBox useTableCopy = new JCheckBox(this.useTable.getText());
            useTableCopy.setPreferredSize(this.useTable.getPreferredSize());
            useTableCopy.setSelected(this.useTable.isSelected());
            useTableCopy.setEnabled(this.useTable.isEnabled());

            JLabel tableTypeCopy = new JLabel(this.tableType.getText());
            tableTypeCopy.setPreferredSize(this.tableType.getPreferredSize());
            tableTypeCopy.setEnabled(this.tableType.isEnabled());

            JCheckBox autoFillCopy = new JCheckBox(this.autoFill.getText());
            autoFillCopy.setPreferredSize(this.autoFill.getPreferredSize());
            autoFillCopy.setSelected(this.autoFill.isSelected());
            autoFillCopy.setEnabled(this.autoFill.isEnabled());

            JTextField tableNameCopy = new JTextField(this.tableName.getText());
            tableNameCopy.setPreferredSize(this.tableName.getPreferredSize());
            tableNameCopy.setEnabled(this.tableName.isEnabled());

            JButton browseButtonCopy = new JButton(this.browseButton.getText());
            browseButtonCopy.setPreferredSize(this.browseButton.getPreferredSize());
            browseButtonCopy.setMargin(this.browseButton.getMargin());
            browseButtonCopy.setEnabled(this.browseButton.isEnabled());

            GUITableStruct guiTableStructClone = new GUITableStruct(useTableCopy, tableTypeCopy, autoFillCopy,
                    tableNameCopy, browseButtonCopy);
            guiTableStructClone.setVisible(this.isVisible);

            return guiTableStructClone;
        }

        /**
         * Method called when the user clicks on the Use Table checkbox. If they are selecting the checkbox, then all
         * the GUITableStruct components will be enabled and the table type will be added to the top level table drop
         * down box; otherwise, they will be disabled and the table type will be removed from the top level table drop
         * down box.
         *
         * @param event event that triggered this listener call to this method
         */
        protected void processUseTableEvent(ActionEvent event) {
            JCheckBox eventSource = (JCheckBox) event.getSource();
            boolean enabled = true;
            if (!eventSource.isSelected())
                enabled = false;

            setEnabled(enabled);

            // If the user has unchecked the use checkbox, remove the unused table type from the
            // top level table box. If the user has checked the use checkbox, add the table
            // type to the top level table box if it is not already there.
            if (SchemaPanel.this.topLevelTable == null)
                return;

            String tableTypeText = this.tableType.getText();
            // Update top level table information
            if (enabled) {
                Object selected = SchemaPanel.this.topLevelTable.getSelectedItem();

                // Use a tree set so that the table types are alphabetical
                TreeSet<String> tableTypes = new TreeSet<String>();
                tableTypes.add(tableTypeText);
                for (int i = 0; i < SchemaPanel.this.topLevelTable.getItemCount(); i++)
                    tableTypes.add(SchemaPanel.this.topLevelTable.getItemAt(i).toString());

                SchemaPanel.this.topLevelTable.removeAllItems();
                for (String type : tableTypes)
                    SchemaPanel.this.topLevelTable.addItem(type);
                SchemaPanel.this.topLevelTable.setSelectedItem(selected);
            } else
                SchemaPanel.this.topLevelTable.removeItem(tableTypeText);
        }

        /**
         * Set whether or not the components of this GUITableStruct are enabled.
         *
         * @param enabled whether or not the components of this GUITableStruct are enabled
         */
        protected void setEnabled(boolean enabled) {
            this.tableType.setEnabled(enabled);
            this.autoFill.setEnabled(enabled);
            this.tableName.setEnabled(enabled);
            this.browseButton.setEnabled(enabled);
        }

        /**
         * Listener that is called when the user clicks on the "Use Table" checkbox.
         */
        protected class UseTableListener extends DBUtilLibActionListener {
            /**
             * GUITableStruct this listener is for.
             */
            private GUITableStruct guiTableStruct;

            /**
             * Constructor.
             *
             * @param guiTableStruct the GUITableStruct the useTable JCheckBox is associated with
             */
            public UseTableListener(GUITableStruct guiTableStruct) {
                this.guiTableStruct = guiTableStruct;
            }

            /**
             * Handles when the user clicks on the Use Table checkbox. See
             * {@link SchemaPanel.GUITableStruct#processUseTableEvent(ActionEvent) processUseTableEvent} for more
             * information.
             */
            @Override
            public void listenersOnActionPerformed(ActionEvent event) {
                this.guiTableStruct.processUseTableEvent(event);
            }
        }

        /**
         * Listener that is called when the user clicks on the Auto Fill button or when the user changes text in the
         * table name field. This listener manages the "synching" of table names. Basically, synched table names have
         * the same prefix and suffix with the middle of the table name being the table type.
         */
        private class AutoFillActionListener extends DBUtilLibActionChangeListener {
            /**
             * Table type of the component that triggered this listener.
             */
            private String tableType;

            /**
             * Constructor.
             *
             * @param tableType table type the useTable JCheckBox is associated with
             */
            public AutoFillActionListener(String tableType) {
                this.tableType = tableType;
            }

            /**
             * If the user clicks on a table's "Auto Fill" checkbox when it was already selected, nothing is done. If
             * the table was not selected, and the user then selects it, then the table name corresponding to the
             * selected checkbox will be synched with the other table names.
             */
            @Override
            public void listenersOnActionPerformed(ActionEvent e) {
                // Return if the user is deselecting the checkbox
                if (!((JCheckBox) e.getSource()).isSelected() || !doAutoFill)
                    return;
                handleListenerEvent();
            }

            @Override
            public void listenersOnStateChanged(ChangeEvent e) {
                handleListenerEvent();
            }

            private void handleListenerEvent() {
                String prefix = "";
                String suffix = "";

                // If the checkbox got checked, make sure the table name associated with that checkbox has a synched
                // name
                for (String type : SchemaPanel.this.tableTypeToGUITableStruct.keySet()) {
                    // Don't check against the table that triggered this
                    if (type.equals(tableType))
                        continue;
                    GUITableStruct tableStruct = SchemaPanel.this.tableTypeToGUITableStruct.get(type);

                    // Ignore tables that do not have their "Use Table" and "Auto Fill" checkboxes checked.
                    if (!tableStruct.useTable.isSelected())
                        continue;
                    if (!tableStruct.autoFill.isSelected())
                        continue;

                    // Retrieve the prefix and suffix
                    String tableName = tableStruct.tableName.getText();
                    if (!tableName.contains(type))
                        return;
                    prefix = tableName.substring(0, tableName.indexOf(type));
                    suffix = tableName.substring(tableName.indexOf(type) + type.length());
                    break;
                }
                if (prefix.length() != 0 || suffix.length() != 0) {
                    if (SchemaPanel.this.tableTypeToGUITableStruct.get(tableType) != null)
                        SchemaPanel.this.tableTypeToGUITableStruct.get(tableType).tableName.setText(prefix + tableType
                                + suffix);
                    if (SchemaPanel.this.specialTableTypeToTableStruct.get(tableType) != null)
                        SchemaPanel.this.specialTableTypeToTableStruct.get(tableType).tableName.setText(prefix
                                + tableType + suffix);
                }
            }
        }

        /**
         * Listener that is called when the user modifies a table name that Auto-Fills / synchs the other table names.
         * Basically, synched table names have the same prefix and suffix with the middle of the table name being the
         * table type.
         */
        private class AutoFillDocumentListener extends DBUtilLibDocumentListener {
            /**
             * Table type of the component that triggered this listener.
             */
            private String tableType;

            /**
             * Constructor.
             *
             * @param tableType table type that will be used as the basis for auto-filling other table types
             */
            public AutoFillDocumentListener(String tableType) {
                this.tableType = tableType;
            }

            /**
             * Gives notification that an attribute or set of attributes changed.
             *
             * @param e the document event
             */
            @Override
            public void listenersOnChangedUpdate(DocumentEvent e) {
                synchTables();
            }

            /**
             * Gives notification that there was an insert into the document.
             *
             * @param e the document event
             */
            @Override
            public void listenersOnInsertUpdate(DocumentEvent e) {
                synchTables();
            }

            /**
             * Gives notification that a portion of the document has been removed.
             *
             * @param e the document event
             */
            @Override
            public void listenersOnRemoveUpdate(DocumentEvent e) {
                synchTables();
            }

            /**
             * Synchronize the tables that have their "Auto Fill" and "Use Table" boxes checked with the name of the
             * table that triggered this.
             */
            private void synchTables() {
                // If code is already executing in this method from somewhere else, return. This is because when
                // a table name changes, it synchs the other table names. When changing the text of the other table
                // names, this listener gets triggered again, but we only care about the initial changes made by
                // the user, not the changes the code is making.
                if (!doAutoFill)
                    return;

                GUITableStruct tableStruct = SchemaPanel.this.tableTypeToGUITableStruct.get(tableType);
                if (tableStruct == null || !tableStruct.autoFill.isSelected()) {
                    tableStruct = SchemaPanel.this.specialTableTypeToTableStruct.get(tableType);
                    if (tableStruct == null || !tableStruct.autoFill.isSelected())
                        return;
                }

                String lowerCaseType = tableType.toLowerCase();
                String lowerCaseName = tableStruct.tableName.getText().trim().toLowerCase();

                String tableName = tableStruct.tableName.getText().trim();

                if (lowerCaseName.length() == 0)
                    return;
                if (lowerCaseName.indexOf(lowerCaseType) < 0)
                    return;

                doAutoFill = false;

                // Retrieve prefix and suffix for the table whose table name modification triggered this event
                String prefix = tableName.substring(0, lowerCaseName.indexOf(lowerCaseType));
                String suffix = tableName.substring(lowerCaseName.indexOf(lowerCaseType) + lowerCaseType.length());

                // Update all other tables with new prefix and suffix.
                for (GUITableStruct ts : SchemaPanel.this.tableTypeToGUITableStruct.values()) {
                    if (ts.tableType.getText().equals(tableType))
                        continue;
                    else if (ts.useTable.isSelected() && ts.autoFill.isSelected())
                        ts.tableName.setText(prefix + ts.tableType.getText() + suffix);
                }
                // Update all other tables with new prefix and suffix.
                for (GUITableStruct ts : SchemaPanel.this.specialTableTypeToTableStruct.values()) {
                    if (ts.tableType.getText().equals(tableType))
                        continue;
                    else if (ts.useTable.isSelected() && ts.autoFill.isSelected())
                        ts.tableName.setText(prefix + ts.tableType.getText() + suffix);
                }
                doAutoFill = true;
            }
        }
    }

    /**
     * RemapGUITableStruct class. This class extends GUITableStruct for the Remap table case to include the ability to <br>
     * a) enter remap source information <br>
     * b) enter a remap lddate that all remap entries will use
     */
    private class RemapGUITableStruct extends GUITableStruct {
        /**
         * Remap source label.
         */
        protected JLabel remapSourceLabel;

        /**
         * Remap source
         */
        protected JTextField remapSource;

        /**
         * Remap lddate label.
         */
        protected JLabel remapLddateLabel;

        /**
         * Remap lddate
         */
        protected JTextField remapLddate;

        /**
         * Constructor. Calls
         * {@link SchemaPanel.GUITableStruct#SchemaPanel.GUITableStruct(JCheckBox, JLabel, JCheckBox, JTextField, JButton)
         * GUITableStruct constructor} and sets up the remap source and lddate.
         *
         * @param useTable     whether or not to "use" (include in the Schema) this table
         * @param tableType    this table's type
         * @param autoFill     whether or not to "autofill" the table names. If this is selected, then when the user types
         *                     in one table name, the prefix (anything before the table type) and suffix (anything after the table type)
         *                     will be retrieved. The other table name boxes will then be populated with prefix+table type+suffix values.
         * @param tableName    this table's name; if this parameter is null (either a null type or the string "null" - case
         *                     insensitive - the table's name will be set to the empty string)
         * @param browseButton browse button for this table. Used to locate a table name.
         */
        public RemapGUITableStruct(JCheckBox useTable, JLabel tableType, JCheckBox autoFill, JTextField tableName,
                                   JButton browseButton) {
            super(useTable, tableType, autoFill, tableName, browseButton);

            this.remapSourceLabel = GUI_Util.label_plain("Remap Source");
            this.remapSource = GUI_Util.textField_medium();

            this.remapLddateLabel = GUI_Util.label_plain("Remap LDDATE");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.remapLddate = GUI_Util.textField_medium(dateFormat.format(new Date()));
        }

        /**
         * Add remap related information to the specified panel
         *
         * @param panel panel to add remap related information to
         * @param gbc   GridBagConstraints to use when adding things to the panel being handed in
         */
        @Override
        public void addToPanel(JPanel panel, GridBagConstraints gbc) {
            super.addToPanel(panel, gbc);

            // Get spacing right ... move down a new row and start remap information under the table name text field
            GUI_Util.gridBag_newRow(gbc);
            // Use Table
            GUI_Util.gridBag_right(gbc);
            // Table Type
            GUI_Util.gridBag_right(gbc);
            // Auto Fill
            GUI_Util.gridBag_right(gbc);

            JPanel remapSourcePanel = new JPanel(new GridBagLayout());
            // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
            GridBagConstraints localGbc = new GridBagConstraints(0, 0, 1, 1, 0.1, 0.1, GridBagConstraints.NORTHWEST,
                    GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0);

            remapSourcePanel.add(this.remapSourceLabel, localGbc);
            GUI_Util.gridBag_right(localGbc);
            remapSourcePanel.add(this.remapSource, localGbc);

            GUI_Util.gridBag_newRow(localGbc);
            remapSourcePanel.add(this.remapLddateLabel, localGbc);
            GUI_Util.gridBag_right(localGbc);
            remapSourcePanel.add(this.remapLddate, localGbc);

            panel.add(remapSourcePanel, gbc);
        }

        /**
         * Sets the visibility for this RemaapGUITableStruct
         */
        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            this.remapSourceLabel.setVisible(visible);
            this.remapSource.setVisible(visible);
            this.remapLddateLabel.setVisible(visible);
            this.remapLddate.setVisible(visible);
        }

        /**
         * Handles when the user clicks on the Use Table checkbox for the RemapGUITableStruct. This calls
         * {@link GUITableStruct#processUseTableEvent(ActionEvent) GUITableStruct.processUseTableEvent} and then handles
         * enabling/disabling the remap source and lddate information.
         */
        @Override
        protected void processUseTableEvent(ActionEvent event) {
            super.processUseTableEvent(event);
            JCheckBox eventSource = (JCheckBox) event.getSource();
            boolean enabled = true;
            if (!eventSource.isSelected())
                enabled = false;
            this.remapSourceLabel.setEnabled(enabled);
            this.remapSource.setEnabled(enabled);
            this.remapLddateLabel.setEnabled(enabled);
            this.remapLddate.setEnabled(enabled);
        }
    }

    /**
     * IdGapsGUITableStruct class. This class extends GUITableStruct for the IdGaps table case to include the ability to
     * handle either idgaps tables or sequence information.
     */
    private class IdGapsGUITableStruct extends GUITableStruct {
        /**
         * Panel where sequence related information will go
         */
        protected JPanel sequencesPanel;

        /**
         * GridBagConstraints for placing things onto the sequencesPanel
         */
        private GridBagConstraints sequences_gbc;

        /**
         * The button that will show/hide the panel with sequence information
         */
        protected JToggleButton useSequencesToggle;

        /**
         * HashMap from ownedid name to corresponding id name (array position 0) and sequence name (array position 1)
         * text fields. This can be populated with information from a ParInfo object and it can be used to write
         * information out to a ParInfo object.
         */
        protected HashMap<String, JTextField[]> idNameToTextFields = new HashMap<String, JTextField[]>();
        /**
         * "Hide sequences information" text
         */
        protected final static String HIDE_SEQUENCES_TEXT = "Do not use sequences";
        /**
         * "Show sequences information" text
         */
        protected final static String SHOW_SEQUENCES_TEXT = "Use sequences";

        /**
         * Constructor. Calls
         * {@link SchemaPanel.GUITableStruct#SchemaPanel.GUITableStruct(JCheckBox, JLabel, JCheckBox, JTextField, JButton)
         * GUITableStruct constructor} and sets up the sequence handling information.
         *
         * @param useTable     whether or not to "use" (include in the Schema) this table
         * @param tableType    this table's type
         * @param autoFill     whether or not to "autofill" the table names. If this is selected, then when the user types
         *                     in one table name, the prefix (anything before the table type) and suffix (anything after the table type)
         *                     will be retrieved. The other table name boxes will then be populated with prefix+table type+suffix values.
         * @param tableName    this table's name; if this parameter is null (either a null type or the string "null" - case
         *                     insensitive - the table's name will be set to the empty string)
         * @param browseButton browse button for this table. Used to locate a table name.
         */
        public IdGapsGUITableStruct(JCheckBox useTable, JLabel tableType, JCheckBox autoFill, JTextField tableName,
                                    JButton browseButton) {
            super(useTable, tableType, autoFill, tableName, browseButton);

            // Allow users to specify sequences to use instead of idgaps tables or max(id) + 1 values
            this.useSequencesToggle = GUI_Util.toggleButton_leftAligned(SHOW_SEQUENCES_TEXT, GUI_Constants.showIcon);
            this.useSequencesToggle.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    useSequencesToggled(e);
                }
            });

            this.sequencesPanel = new JPanel(new GridBagLayout());
            this.sequences_gbc = new GridBagConstraints(0, 0, 1, 1, 0.1, 0.1, GridBagConstraints.NORTHWEST,
                    GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0);
        }

        /**
         * Add this table struct information to the specified panel
         *
         * @param panel panel to add table struct information to
         * @param gbc   GridBagConstraints to use when adding this table struct information to panel
         */
        @Override
        public void addToPanel(JPanel panel, GridBagConstraints gbc) {
            super.addToPanel(panel, gbc);
            int oldFill = gbc.fill;
            GUI_Util.gridBag_noFill(gbc);

            // Get spacing right before adding the Use Sequences toggle button ... move down a new row and put
            // sequences toggle under the table name text field. Must move past Use Table, Table Type, Auto Fill
            GUI_Util.gridBag_newRow(gbc);
            GUI_Util.gridBag_right(gbc);
            GUI_Util.gridBag_right(gbc);
            GUI_Util.gridBag_right(gbc);
            panel.add(this.useSequencesToggle, gbc);

            // Spacing again for sequencesPanel placement
            GUI_Util.gridBag_newRow(gbc);
            GUI_Util.gridBag_right(gbc);
            GUI_Util.gridBag_right(gbc);
            GUI_Util.gridBag_right(gbc);
            panel.add(this.sequencesPanel, gbc);

            gbc.fill = oldFill;
        }

        /**
         * Set all of this idgapsTableStruct information visible or not
         *
         * @param visible whether or that all of this idgapsTableStruct information should be visible or not
         */
        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            this.sequencesPanel.setVisible(visible);
        }

        /**
         * Handles when the user clicks on the UseSequencesToggle button. If the sequences panel is showing, hide it and
         * restore the idgaps area to the state prior to clicking on the Use Sequences button. If the sequences panel is
         * if it was hidden, show it and disable the idgaps area.
         *
         * @param e event that registered the user's click on the toggle button
         */
        protected void useSequencesToggled(ActionEvent e) {
            // Whether or not the use sequences toggle button is selected
            boolean useSequencesSelected = this.useSequencesToggle.isSelected();

            // If useSequencesSelected is true, then the useSequencesSelected button was just selected.
            // Disable the area that are not sequences related
            if (useSequencesSelected) {
                super.setEnabled(false);
                // If there are no tables in the SchemaPanel, we cannot deduce which tables are idowner tables
                if (getTables().size() == 0) {
                    GUI_Util
                            .optionPane_error("No tables exist in the schema that sequences could be used for", "Error");
                    this.useSequencesToggle.doClick();
                } else
                    showSequences(true);
            }
            // Use Sequences button was de-selected - reset the idgaps stuff to what it was before
            else {
                super.setEnabled(true);
                this.useSequencesToggle.setText(SHOW_SEQUENCES_TEXT);
                this.useSequencesToggle.setIcon(GUI_Constants.showIcon);
            }

            this.sequencesPanel.setVisible(useSequencesSelected);
        }

        private void showSequences(boolean findOwnedIds) {
            this.sequencesPanel.removeAll();
            this.useSequencesToggle.setText(HIDE_SEQUENCES_TEXT);
            this.useSequencesToggle.setIcon(GUI_Constants.hideIcon);

            try {
                ParInfo parInfo = getParInfo();
                parInfo.removeParameter(SchemaPanel.this.parNamePrefix + ParInfo.AUTO_TABLE_CREATION);
                parInfo.removeParameter(SchemaPanel.this.parNamePrefix + ParInfo.TRUNCATE_TABLES);
                parInfo.addParameter(SchemaPanel.this.parNamePrefix + ParInfo.DAO_TYPE, DBDefines.DATABASE_DAO);
                Schema schema = new Schema(parInfo, SchemaPanel.this.parNamePrefix);

                if (findOwnedIds) {
                    for (Table table : schema.getTables()) {
                        String ownedId = table.getOwnedID();
                        if (ownedId == null || this.idNameToTextFields.get(ownedId) != null)
                            continue;

                        JTextField ownedIdTextField = GUI_Util.textField_short(ownedId);
                        JTextField sequenceNameTextField = GUI_Util.textField_medium(ownedId + "_SEQ");
                        this.idNameToTextFields.put(ownedId,
                                new JTextField[]{ownedIdTextField, sequenceNameTextField});
                    }
                }
            } catch (FatalDBUtilLibException e) {
                System.err
                        .println("Error retrieving table information in order to determine idowner tables to use sequeneces for.");
                e.printStackTrace(System.err);
            }

            JLabel idNameLabel = GUI_Util.label_plain("ID name");
            JLabel sequenceNameLabel = GUI_Util.label_plain("Sequence name");
            GUI_Util.gridBag_centerAnchor(this.sequences_gbc);
            this.sequencesPanel.add(idNameLabel, this.sequences_gbc);
            GUI_Util.gridBag_right(this.sequences_gbc);
            this.sequencesPanel.add(sequenceNameLabel, this.sequences_gbc);
            GUI_Util.gridBag_newRow(this.sequences_gbc);
            GUI_Util.gridBag_northwestAnchor(this.sequences_gbc);

            for (String ownedId : this.idNameToTextFields.keySet()) {
                JTextField ownedIdTextField = this.idNameToTextFields.get(ownedId)[0];
                JTextField sequenceNameTextField = this.idNameToTextFields.get(ownedId)[1];
                this.sequencesPanel.add(ownedIdTextField, this.sequences_gbc);
                GUI_Util.gridBag_right(this.sequences_gbc);
                this.sequencesPanel.add(sequenceNameTextField, this.sequences_gbc);
                GUI_Util.gridBag_newRow(this.sequences_gbc);
            }
            this.sequencesPanel.revalidate();
        }
    }

    /**
     * Comparator implementation for sorting TableStructs alphabetically by table type.
     */
    protected class TableStructComparator implements Comparator<TableStruct> {
        public int compare(TableStruct o1, TableStruct o2) {
            return o1.type.compareTo(o2.type);
        }
    }
}
