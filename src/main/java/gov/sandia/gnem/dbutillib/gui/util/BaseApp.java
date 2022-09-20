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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.gui.ParInfoGui;
import gov.sandia.gnem.dbutillib.gui.SchemaPanel;
import gov.sandia.gnem.dbutillib.gui.SchemaPanel.GUITableStruct;
import gov.sandia.gnem.dbutillib.gui.SchemaPanelChooser;
import gov.sandia.gnem.dbutillib.gui.SchemaPanelDB;
import gov.sandia.gnem.dbutillib.gui.SchemaPanelFF;
import gov.sandia.gnem.dbutillib.gui.SchemaPanelXML;
import gov.sandia.gnem.dbutillib.gui.TableDefPanel;
import gov.sandia.gnem.dbutillib.gui.TableDefPanelChooser;
import gov.sandia.gnem.dbutillib.gui.TableDefPanelDB;
import gov.sandia.gnem.dbutillib.gui.TableDefPanelFF;
import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * This class is a base class that provides what many DBTools applications seem to all have in common. It creates a
 * JFrame with a {@link SchemaPanelChooser Source SchemaPanelChooser} on the left and a
 * {@link SchemaPanelChooser target SchemaPanelChooser} on the right.
 * <p>
 * The source SchemaPanelChooser has schema panels for the following DAO types: DB, FF, and XML. All three schema panels
 * are of of type {@link SchemaPanel#INPUT INPUT} and are modifiable.
 * <p>
 * The target SchemaPanelChooser has schema panels for the following DAO types: DB, FF, and XML. All three schema panels
 * are of of type {@link SchemaPanel#OUTPUT OUTPUT} and are modifiable.
 * <p>
 * This class essentially represents a glorified parameter file creation mechanism for the typical scenarios DBTools
 * applications tend to use. It cannot be instantiated since each class must populate certain specific methods
 * particular to their own class.
 */

/*
 * General design notes: There are not many design notes since this class just lays out a GUI. The most complicated
 * objects are the SchemaPanelChoosers since they each contain 3 SchemaPanels - DB, FF, and XML SchemaPanels. Other than
 * that, the class is laid out mostly in the order that methods are used during the creation of the GUI, so starting
 * with the constructor and examining each new method as it is encountered should provide a decently clear vision of how
 * this class is set up.
 */
@SuppressWarnings("serial")
public abstract class BaseApp extends JFrame {
    /**
     * The name of the application extending this class.
     */
    protected String applicationName;

    /**
     * The ParInfoGui object keeps {@link ParInfo ParInfo} parameters in synch with their corresponding GUI components.
     */
    protected ParInfoGui parInfoGui;

    /**
     * {@link JPanel JPanel} version of the source {@link Schema schema}. This SchemaPanelChooser contains three
     * SchemaPanels - a {@link SchemaPanelDB SchemaPanelDB}, a {@link SchemaPanelFF SchemaPanelFF}, and a
     * {@link SchemaPanelXML SchemaPanelXML}.
     */
    protected SchemaPanelChooser sourceSchemaPanel;

    /**
     * {@link JPanel JPanel} version of the target {@link Schema schema}. This SchemaPanelChooser contains three
     * SchemaPanels - a {@link SchemaPanelDB SchemaPanelDB}, a {@link SchemaPanelFF SchemaPanelFF}, and a
     * {@link SchemaPanelXML SchemaPanelXML}.
     */
    protected SchemaPanelChooser targetSchemaPanel;

    /**
     * Keep track of the TableDefPanelChoosers in each SchemaPanel since we don't have access to them through the
     * SchemaPanels.
     */
    private HashMap<String, TableDefPanelChooser> tableDefPanelChoosers;

    /**
     * JSplitPane with {@link #sourceSchemaPanel sourceSchemaPanel} on the left and
     * {@link #targetSchemaPanel targetSchemaPanel} on the right.
     */
    private JSplitPane schemaPanelSplitPane;

    /**
     * JSplitPane with {@link #schemaPanelSplitPane schemaPanelSplitPane} on the top and {@link #appPanel appPanel} on
     * the bottom.
     */
    private JSplitPane mainSplitPane;

    /**
     * Keep track of the most recently "browsed to" directory.
     */
    protected String currentDirectory = "";

    /**
     * Keep track of the most recently "saved to" filename
     */
    private String currentFilename = "";

    /**
     * Panel with application specific (non {@link SchemaPanel SchemaPanel} type) components.
     */
    protected JPanel appPanel;

    /**
     * Whether or not the GUI has been initialized/populated with data.
     */
    private boolean initialized = false;

    /**
     * Denotes a "Source" schema
     */
    protected final static String SOURCE = "Source";

    /**
     * Denotes a "Target" schema
     */
    protected final static String TARGET = "Target";

    /**
     * Status log label
     */
    protected JLabel statusLogLabel;

    /**
     * Status log parameter
     */
    protected JTextField statusLog;

    /**
     * Error log label
     */
    protected JLabel errorLogLabel;

    /**
     * Error log parameter
     */
    protected JTextField errorLog;

    /**
     * Warning log label
     */
    protected JLabel warningLogLabel;

    /**
     * Warning log parameter
     */
    protected JTextField warningLog;

    /**
     * Constructor. Creates the base GUI which consists of three panels representing the Source Schema parameters,
     * Target Schema parameters, and the app specific parameters.
     *
     * @param applicationName the name of the application extending this class
     * @throws FatalDBUtilLibException if an error occurs when creating the GUI
     */
    public BaseApp(String applicationName) throws FatalDBUtilLibException {
        this.applicationName = applicationName;
        setTitle(this.applicationName);

        // Source Schema on left, Target Schema on right
        this.schemaPanelSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        // Schemas on top, application specific stuff on bottom
        this.mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.mainSplitPane.setTopComponent(this.schemaPanelSplitPane);

        add(this.mainSplitPane, BorderLayout.CENTER);

        // Initialize the current working directory to the user's current directory
        this.currentDirectory = System.getProperty("user.dir");

        // handles synching up GUI components with parInfo components
        this.parInfoGui = new ParInfoGui();

        createSchemaPanels();
        createAppPanel();
        this.mainSplitPane.setBottomComponent(new JScrollPane(this.appPanel));

        createMenus();
        createToolTips();

        registerComponents();
    }

    /**
     * Register application specific components with the ParInfoGui object so that parameters and gui components are
     * kept in sync.
     */
    protected abstract void registerComponents();

    /**
     * Synch application specific components with the ParInfoGui object so that the gui components reflect what's in
     * this.parInfoGui.
     */
    protected abstract void synchComponents();

    /**
     * Create the application specific parameters panel.
     */
    protected abstract void createAppPanel();

    /**
     * Create the source and target schema panels.
     *
     * @throws DBDefines.FatalDBUtilLibException if an error occurs while creating these schema panels
     */
    private void createSchemaPanels() throws DBDefines.FatalDBUtilLibException {
        // TODO: Test both modifiable and not
        // Delete existing SchemaPanels if they exist to make way for new ones!
        if (this.sourceSchemaPanel != null)
            remove(this.sourceSchemaPanel);
        if (this.targetSchemaPanel != null)
            remove(this.targetSchemaPanel);

        this.tableDefPanelChoosers = new HashMap<String, TableDefPanelChooser>();

        // ---- SOURCE SCHEMA ----
        this.parInfoGui.addParameter(SOURCE + ParInfoGui.SCHEMA_PANEL_TYPE, String.valueOf(SchemaPanel.INPUT));
        this.parInfoGui.addParameter(SOURCE + ParInfoGui.MODIFIABLE, "true");

        // Source DB SchemaPanel and TableDefChooser
        TableDefPanelChooser sourceTableDefChooser = createTableDefPanelChooser();
        this.tableDefPanelChoosers.put(SOURCE + DBDefines.DATABASE_DAO, sourceTableDefChooser);
        SchemaPanelDB sourceSchemaPanelDB = new SchemaPanelDB(this.parInfoGui, SOURCE);
        sourceSchemaPanelDB.setTableDefinition(sourceTableDefChooser);
        sourceSchemaHandling(sourceSchemaPanelDB);

        // Source FF SchemaPanel and TableDefChooser
        sourceTableDefChooser = createTableDefPanelChooser();
        this.tableDefPanelChoosers.put(SOURCE + DBDefines.FF_DAO, sourceTableDefChooser);
        SchemaPanelFF sourceSchemaPanelFF = new SchemaPanelFF(this.parInfoGui, SOURCE);
        sourceSchemaPanelFF.setTableDefinition(sourceTableDefChooser);
        sourceSchemaHandling(sourceSchemaPanelFF);

        // Source XML SchemaPanel and TableDefChooser
        sourceTableDefChooser = createTableDefPanelChooser();
        this.tableDefPanelChoosers.put(SOURCE + DBDefines.XML_DAO, sourceTableDefChooser);
        SchemaPanelXML sourceSchemaPanelXML = new SchemaPanelXML(this.parInfoGui, SOURCE);
        sourceSchemaPanelXML.setTableDefinition(sourceTableDefChooser);
        sourceSchemaHandling(sourceSchemaPanelXML);

        // Source SchemaPanelChooser with the DB, FF, and XML source schema panels
        this.sourceSchemaPanel = new SchemaPanelChooser(new SchemaPanel[]{sourceSchemaPanelDB, sourceSchemaPanelFF,
                sourceSchemaPanelXML});
        this.sourceSchemaPanel.setBorder(BorderFactory.createTitledBorder("Source Schema"));

        // ---- TARGET SCHEMA ----
        this.parInfoGui.addParameter(TARGET + ParInfoGui.SCHEMA_PANEL_TYPE, String.valueOf(SchemaPanel.OUTPUT));
        this.parInfoGui.addParameter(TARGET + ParInfoGui.MODIFIABLE, "true");

        // Target DB SchemaPanel and TableDefChooser.
        TableDefPanelChooser targetTableDefChooser = createTableDefPanelChooser();
        this.tableDefPanelChoosers.put(TARGET + DBDefines.DATABASE_DAO, targetTableDefChooser);
        SchemaPanelDB targetSchemaPanelDB = new SchemaPanelDB(this.parInfoGui, TARGET);
        targetSchemaPanelDB.setTableDefinition(targetTableDefChooser);
        targetSchemaHandling(targetSchemaPanelDB);
        targetSchemaPanelDB.addMimicSchemaButton(this.sourceSchemaPanel, "Mimic Input Schema");

        // Target FF SchemaPanel and TableDefChooser.
        targetTableDefChooser = createTableDefPanelChooser();
        this.tableDefPanelChoosers.put(TARGET + DBDefines.FF_DAO, targetTableDefChooser);
        SchemaPanelFF targetSchemaPanelFF = new SchemaPanelFF(this.parInfoGui, TARGET);
        targetSchemaPanelFF.setTableDefinition(targetTableDefChooser);
        targetSchemaHandling(targetSchemaPanelFF);
        targetSchemaPanelFF.addMimicSchemaButton(this.sourceSchemaPanel, "Mimic Input Schema");

        // Target XML SchemaPanel and TableDefChooser.
        targetTableDefChooser = createTableDefPanelChooser();
        this.tableDefPanelChoosers.put(TARGET + DBDefines.XML_DAO, targetTableDefChooser);
        SchemaPanelXML targetSchemaPanelXML = new SchemaPanelXML(this.parInfoGui, TARGET);
        targetSchemaPanelXML.setTableDefinition(targetTableDefChooser);
        targetSchemaHandling(targetSchemaPanelXML);
        targetSchemaPanelXML.addMimicSchemaButton(this.sourceSchemaPanel, "Mimic Input Schema");

        // Target SchemaPanelChooser with the DB, FF, and XML source schema panels
        this.targetSchemaPanel = new SchemaPanelChooser(new SchemaPanel[]{targetSchemaPanelDB, targetSchemaPanelFF,
                targetSchemaPanelXML});
        this.targetSchemaPanel.setBorder(BorderFactory.createTitledBorder("Target Schema"));

        // Put these source and target schema panels in the correct places in the schema panel split pane
        this.schemaPanelSplitPane.setLeftComponent(new JScrollPane(this.sourceSchemaPanel));
        this.schemaPanelSplitPane.setRightComponent(new JScrollPane(this.targetSchemaPanel));

        // Ensure the correct SchemaPanel and TableDefinitionTable types are selected. (Since these are choosers, they
        // are not registered with parInfoGui.)
        String sourceSchemaDAOType = this.parInfoGui.getItem(SOURCE + ParInfo.DAO_TYPE, DBDefines.DATABASE_DAO);
        this.sourceSchemaPanel.select(sourceSchemaDAOType);
        String sourceTableDefDAOType = this.parInfoGui.getItem(SOURCE + ParInfo.TABLE_DEFINITION_TABLE
                + ParInfo.DAO_TYPE, DBDefines.DATABASE_DAO);
        this.tableDefPanelChoosers.get(SOURCE + sourceSchemaDAOType).select(sourceTableDefDAOType);

        String targetSchemaDAOType = this.parInfoGui.getItem(TARGET + ParInfo.DAO_TYPE, DBDefines.DATABASE_DAO);
        this.targetSchemaPanel.select(targetSchemaDAOType);
        String targetTableDefDAOType = this.parInfoGui.getItem(TARGET + ParInfo.TABLE_DEFINITION_TABLE
                + ParInfo.DAO_TYPE, DBDefines.DATABASE_DAO);
        this.tableDefPanelChoosers.get(TARGET + targetSchemaDAOType).select(targetTableDefDAOType);

        this.schemaPanelSplitPane.setPreferredSize(new Dimension(1400, 700));
        this.schemaPanelSplitPane.setDividerLocation(700);
    }

    /**
     * Create a TableDefPanelChooser with a {@link TableDefPanelDB TableDefPanelDB} and a
     * {@link TableDefPanelFF TableDefPanelFF}.
     */
    private TableDefPanelChooser createTableDefPanelChooser() throws DBDefines.FatalDBUtilLibException {
        TableDefPanelDB tableDefPanelDB = new TableDefPanelDB();
        TableDefPanelFF tableDefPanelFF = new TableDefPanelFF();

        TableDefPanel[] tableDefPanels = new TableDefPanel[]{tableDefPanelDB, tableDefPanelFF};
        return new TableDefPanelChooser(tableDefPanels);
    }

    /**
     * Handle source schema panel specific issues.
     *
     * @param sourceSchema source schema panel to perform the above operations on
     */
    protected abstract void sourceSchemaHandling(SchemaPanel sourceSchema);

    /**
     * Handle target schema panel specific issues.
     *
     * @param targetSchema target schema panel to perform the above operations on
     */
    protected abstract void targetSchemaHandling(SchemaPanel targetSchema);

    /**
     * Show the application's GUI. This also populates the GUI with any data it needs to be initialized with.
     */
    @Override
    public void setVisible(boolean visible) {
        if (!this.initialized) {
            for (SchemaPanel sp : this.sourceSchemaPanel.getSchemaPanels())
                sp.setVisible(true);
            for (SchemaPanel tp : this.targetSchemaPanel.getSchemaPanels())
                tp.setVisible(true);
            this.initialized = true;
        }
        super.setVisible(visible);
    }

    /**
     * Load parameters from a parameter file into the GUI.
     */
    private void openProject() {
        final File file = browseToFile("Browse for " + this.applicationName + " parameter file",
                GUI_Constants.OPEN_DIALOG);

        // No file chosen
        if (file == null)
            return;

        SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>() {
            private JProgressFrame progressFrame;

            @Override
            protected Object doInBackground() throws Exception {
                setEnabled(false);
                this.progressFrame = new JProgressFrame(BaseApp.this, "Loading project " + file, true);

                String parFileName = file.toString();
                ParInfo parInfo = new ParInfo();
                parInfo.readParFile(parFileName);

                // Remove old parameters
                ArrayList<String> parametersToRemove = new ArrayList<String>();
                for (String parameter : BaseApp.this.parInfoGui.keySet())
                    parametersToRemove.add(parameter);
                for (String parameter : parametersToRemove)
                    BaseApp.this.parInfoGui.removeParameter(parameter);

                BaseApp.this.parInfoGui.updateAndAddParameters(parInfo);

                appOpenProjectTasks();

                synchComponents();

                return null;
            }

            @Override
            protected void done() {
                for (SchemaPanel sp : BaseApp.this.sourceSchemaPanel.getSchemaPanels()) {
                    sp.setDirectory(BaseApp.this.currentDirectory);
                    sp.updateParameters(BaseApp.this.parInfoGui, SOURCE);
                }

                for (SchemaPanel tp : targetSchemaPanel.getSchemaPanels()) {
                    tp.setDirectory(currentDirectory);
                    tp.updateParameters(parInfoGui, TARGET);
                }

                // Ensure the correct SchemaPanel and TableDefinitionTable types are selected. (Since these are
                // choosers, they are not registered with parInfoGui.)
                String sourceSchemaDAOType = parInfoGui.getItem(SOURCE + ParInfo.DAO_TYPE, DBDefines.DATABASE_DAO);
                sourceSchemaPanel.select(sourceSchemaDAOType);
                String sourceTableDefDAOType = parInfoGui.getItem(SOURCE + ParInfo.TABLE_DEFINITION_TABLE
                        + ParInfo.DAO_TYPE, DBDefines.DATABASE_DAO);
                tableDefPanelChoosers.get(SOURCE + sourceSchemaDAOType).select(sourceTableDefDAOType);

                String targetSchemaDAOType = parInfoGui.getItem(TARGET + ParInfo.DAO_TYPE, DBDefines.DATABASE_DAO);
                targetSchemaPanel.select(targetSchemaDAOType);
                String targetTableDefDAOType = parInfoGui.getItem(TARGET + ParInfo.TABLE_DEFINITION_TABLE
                        + ParInfo.DAO_TYPE, DBDefines.DATABASE_DAO);
                tableDefPanelChoosers.get(TARGET + targetSchemaDAOType).select(targetTableDefDAOType);

                validate();
                repaint();

                setEnabled(true);

                progressFrame.setVisible(false);
                progressFrame.dispose();
            }
        };
        worker.execute();
    }

    /**
     * Do the application specific tasks that must be done when opening a project file.
     */
    protected abstract void appOpenProjectTasks();

    /**
     * Save the state of the gui to a parameter file.
     *
     * @param checkPrompt whether or not the user should be prompted to see if they would like to check if the gui is in
     *                    a state such that the saved project file will be runnable by the application.
     * @param savePrompt  whether or not to prompt the user for which file to save to; if true, function as a "Save As",
     *                    if false function as a "Save" and just save it to the default filename
     * @return whether or not the project was saved; if the user clicked cancel or an error was encountered, the project
     * will not be saved
     */
    private boolean saveProject(boolean checkPrompt, boolean savePrompt) {
        if (checkPrompt) {
            Object[] options = {"Yes", "No", "Cancel"};
            int n = JOptionPane.showOptionDialog(this,
                    "Would you like to check if the current state being saved to a project file is " + "runnable by "
                            + this.applicationName + "?", "Save Project", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            // user clicked Cancel -> return to the applicationGui.
            if (n == 2)
                return false;
            // If n is 0, then the user clicked on Yes - they want to check the state.
            if (n == 0) {
                if (!runCheck()) {
                    GUI_Util.optionPane_error("The application is not in a state runnable by " + this.applicationName,
                            "Save Project Error");
                    return false;
                }
            }
        }

        File projectFile = null;
        // If there's no file to use in a "Save" function, force the user to browse for a file
        if (this.currentFilename == null || this.currentFilename.length() == 0 || savePrompt) {
            projectFile = browseToFile("Save to " + this.applicationName + " parameter file", GUI_Constants.SAVE_DIALOG);

            // No file browsed to
            if (projectFile == null)
                return false;
        }
        // Just save to the current project file
        else
            projectFile = new File(this.currentDirectory + DBDefines.PATH_SEPARATOR + this.currentFilename);

        // Do the actual saving
        try {
            ParInfo currentParInfo = getCurrentParInfo();
            currentParInfo.writeParFile(projectFile, true);
        } catch (IOException e) {
            GUI_Util.optionPane_error("Error in BaseApp(" + this.applicationName + ").saveProject.\n" + e.getMessage(),
                    this.applicationName + " Error");
            return false;
        }
        return true;
    }

    /**
     * Display a pop up file browser to the user and return the file they have browsed to
     *
     * @param title      File chooser window title
     * @param dialogType type of dialog window to pop up; currently only
     *                   {@link GUI_Constants#SAVE_DIALOG GUI_Constants.SAVE_DIALOG} and
     *                   {@link GUI_Constants#OPEN_DIALOG GUI_Constants.OPEN_DIALOG} are supported
     * @return file the user has browsed to
     */
    private File browseToFile(String title, String dialogType) {
        File file = null;
        try {
            JFileChooser chooser = new JFileChooser(new File(this.currentDirectory));
            chooser.setDialogTitle(title);

            FileNameExtensionFilter filter = new FileNameExtensionFilter("Parameter Files (*.par *.txt)", "par", "txt");
            chooser.setFileFilter(filter);

            int returnVal = -1;
            if (dialogType.equals(GUI_Constants.SAVE_DIALOG))
                returnVal = chooser.showSaveDialog(null);
            else if (dialogType.equals(GUI_Constants.OPEN_DIALOG))
                returnVal = chooser.showOpenDialog(null);

            if (returnVal != JFileChooser.APPROVE_OPTION)
                return file;

            file = chooser.getSelectedFile();
            // Track the directory that the user most recently navigated to
            if (file != null) {
                this.currentDirectory = file.getParent();
                this.currentFilename = file.getName();
            }
        } catch (NullPointerException e) {
            GUI_Util.optionPane_error("Error in BaseApp (" + this.applicationName + ").browseToFile.\n"
                    + e.getMessage(), this.applicationName + " Error");
            return null;
        }
        return file;
    }

    /**
     * Return a {@link ParInfoGui ParInfoGui} object that reflects the current state of the GUI. This
     * {@link ParInfoGui ParInfoGui} object should be in a state where the application is ready to be run with the
     * information contained in that object.
     *
     * @return a {@link ParInfoGui ParInfoGui} object that reflects the current state of the GUI.
     */
    protected ParInfo getCurrentParInfo() {
        // Get the most current source and target parameters);
        synchComponents();

        ParInfo currentParInfo = this.parInfoGui.clone();
        currentParInfo.updateAndAddParameters(this.sourceSchemaPanel.getSelectedSchemaPanel().getParInfo());
        currentParInfo.updateAndAddParameters(this.targetSchemaPanel.getSelectedSchemaPanel().getParInfo());

        // Set the application
        currentParInfo.addParameter(ParInfo.APPLICATION, this.applicationName);

        appGetCurrentParInfoTasks(currentParInfo);
        return currentParInfo;
    }

    /**
     * Do the application specific tasks that must be done when retrieving the current ParInfo object.
     *
     * @param currentParInfo ParInfo object currently being prepared to be returned as the current ParInfo
     *                       representation of what's in this gui
     */
    protected abstract void appGetCurrentParInfoTasks(ParInfo currentParInfo);

    /**
     * Create menus
     */
    private void createMenus() {
        // File Menu
        JMenu fileMenu = new JMenu("File");

        // Open Project Menu Item
        JMenuItem openProjectMenuItem = new JMenuItem("Open Project");
        openProjectMenuItem.addActionListener(new OpenProjectActionListener());

        // Save Project Menu Item
        JMenuItem saveProjectMenuItem = new JMenuItem("Save Project");
        saveProjectMenuItem.addActionListener(new SaveProjectActionListener(false));

        // Save Project Menu Item
        JMenuItem saveProjectAsMenuItem = new JMenuItem("Save Project As");
        saveProjectAsMenuItem.addActionListener(new SaveProjectActionListener(true));

        ArrayList<JMenu> appFileMenuItems = appFileMenuItems();

        // Exit Menu Item
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ExitActionListener());

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
//        new Documentation(helpMenu, this.applicationName);

        // Add items to the file menu
        fileMenu.add(openProjectMenuItem);
        fileMenu.add(saveProjectMenuItem);
        fileMenu.add(saveProjectAsMenuItem);

        // Add app specific File menu items
        if (appFileMenuItems.size() > 0)
            fileMenu.add(new JSeparator());
        for (JMenu jmenu : appFileMenuItems)
            fileMenu.add(jmenu);

        fileMenu.add(new JSeparator());
        fileMenu.add(exitMenuItem);

        // Add items to the menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    /**
     * Create application specific JMenu items for the File menu
     *
     * @return application specific JMenu items for the File menu
     */
    protected abstract ArrayList<JMenu> appFileMenuItems();

    /**
     * Create application specific tool tips.
     */
    protected abstract void createToolTips();

    /**
     * Code performed when the user clicks on the "Run [application]" button; this runs the application with a parInfo
     * object populated from the GUI. This presents the user with 5 options: <br>
     * Run and Close: close the GUI and run with the current parameters in the GUI <br>
     * Run: run with the current parameters in the GUI (disable the GUI until the run is done) <br>
     * Save Project then Close and Run: save the current state of the GUI to the specified parameter file, close the GUI
     * and run with the current parameters in the GUI <br>
     * Save Project then Run: save the current state of the GUI to the specified parameter file, then run with the
     * current parameters in the GUI (disable the GUI until the run is done) <br>
     * Cancel: cancel run action
     */
    protected void run() {
        if (!runCheck())
            return;
        Object[] options = new Object[5];
        int closeAndRunIndex = 0;
        options[closeAndRunIndex] = "<html><center>Close<br>and Run</center></html>";
        int runIndex = 1;
        options[runIndex] = "Run";
        int saveCloseRunIndex = 2;
        options[saveCloseRunIndex] = "<html><center>Save Project then<br>Close and Run</center></html>";
        int saveRunIndex = 3;
        options[saveRunIndex] = "<html><center>Save Project<br>then Run</center></html>";
        int cancelIndex = 4;
        options[cancelIndex] = "Cancel";

        int selectedIndex = JOptionPane.showOptionDialog(this, "", "Launch " + this.applicationName,
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[runIndex]);

        boolean continueRun = true;

        // user clicked Cancel -> return to the gui.
        if (selectedIndex == cancelIndex)
            return;
        // user clicked on "Save Project then Close and Run" or "Save Project then Run" -- save the project to a file
        if (selectedIndex == saveCloseRunIndex || selectedIndex == saveRunIndex)
            continueRun = saveProject(false, false);

        // error saving
        if (!continueRun)
            return;

        // user opted to close down the application
        if (selectedIndex == closeAndRunIndex || selectedIndex == saveCloseRunIndex) {
            dispose();
            runApp(true);
            System.exit(0);
        }
        // user wants gui to remain open -- disable gui while application is running

        else {
            SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>() {
                private JProgressFrame progressFrame;

                @Override
                protected Object doInBackground() throws Exception {
                    BaseApp.this.setEnabled(false);
                    progressFrame = new JProgressFrame(BaseApp.this, "Running " + applicationName, true);
                    runApp(false);
                    return null;
                }

                @Override
                protected void done() {
                    progressFrame.setVisible(false);
                    progressFrame.dispose();
                    BaseApp.this.setEnabled(true);
                }
            };
            worker.execute();
        }
    }

    /**
     * Call and run the application
     *
     * @param closeApp whether or not to close the application down after running
     */
    protected abstract void runApp(boolean closeApp);

    /**
     * Check state of parameters before running.
     *
     * @return whether or not the information in the GUI is adequate to run the application
     */
    private boolean runCheck() {
        try {
            // Show a busy cursor while doing the checks
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SchemaPanel source = this.sourceSchemaPanel.getSelectedSchemaPanel();
            SchemaPanel target = this.targetSchemaPanel.getSelectedSchemaPanel();

            // Create a temporary ParInfo object that can be mangled some while we check things
            ParInfo runParInfo = new ParInfoGui();
            runParInfo.addParameters(source.getParInfo());
            runParInfo.addParameters(target.getParInfo());

            ArrayList<SchemaPanel.GUITableStruct> sourceTables = source.getTables();
            ArrayList<SchemaPanel.GUITableStruct> targetTables = target.getTables();

            if (sourceTables.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please specify source tables.", this.applicationName + " Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (targetTables.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please specify target tables.", this.applicationName + " Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // If there is a SourceTopLevelTable specified in the source schema, then SourceDBQuery must be specified.
            if (runParInfo.getItem("SourceTopLevelTable", null) != null
                    && !runParInfo.getItem("SourceTopLevelTable", null).equals(SchemaPanel.NO_TOP_LEVEL_TABLE)
                    && runParInfo.getItem("SourceQuery", null) == null) {
                JOptionPane.showMessageDialog(this, "If a Top Level Table is specified in the source"
                                + " schema, \nthen a DB Query must be specified as well.", this.applicationName + " Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // If there are tables in the source schema that do not have a corresponding table in the target schema,
            // alert users to this fact, and ask if it's okay to make the missing tables in the target linked to the
            // source tables of the same type.
            HashMap<String, SchemaPanel.GUITableStruct> sourceTableTypeToTableStruct = new HashMap<String, SchemaPanel.GUITableStruct>();
            Set<String> targetTableTypes = new HashSet<String>();

            // source tables whose use table box is selected
            for (SchemaPanel.GUITableStruct table : sourceTables)
                if (table.useTable.isSelected())
                    sourceTableTypeToTableStruct.put(table.tableType.getText(), table);
            // target tables whose use table box is selected
            for (SchemaPanel.GUITableStruct table : targetTables)
                if (table.useTable.isSelected())
                    targetTableTypes.add(table.tableType.getText());
            // missing tables
            Set<String> missingTargetTables = new HashSet<String>();
            for (String sourceTableType : sourceTableTypeToTableStruct.keySet())
                if (!targetTableTypes.contains(sourceTableType))
                    missingTargetTables.add(sourceTableType);

            if (!missingTargetTables.isEmpty()) {
                Object[] options = {"OK", "Cancel"};
                StringBuilder msgStr = new StringBuilder();
                msgStr
                        .append("There are tables in the source schema that are not\nin the target schema. Would you like to link\n");
                for (String missingTargetTable : missingTargetTables)
                    msgStr.append("\tTarget Table " + missingTargetTable + " to "
                            + sourceTableTypeToTableStruct.get(missingTargetTable).tableName.getText() + "\n");
                String msg = msgStr.substring(0, msgStr.length() - 1) + "?";

                int n = JOptionPane.showOptionDialog(this, msg.toString(), this.applicationName + " Question",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (n == 1)
                    return false;

                for (String mt : missingTargetTables)
                    runParInfo.appendParameter("TargetReferencedTables", mt);
            }

            // If the target schema's Clear Table Data checkbox is selected, double check that users really and truly
            // want
            // to clear out table data for tables that exist in the database. This was requested by Jorge.
            if (target.getClearTableDataSelected()) {
                StringBuilder tables = new StringBuilder();
                Schema targetSchema = null;
                try {
                    ParInfo parInfo = target.getParInfo();

                    // Remove the auto table creation option if it is checked. This is because if this is turned on,
                    // creating the schema will create those tables, and then the user will get asked if they want to
                    // delete the data in those tables. This is confusing since the user will not know about those
                    // tables since schema created them.
                    parInfo.removeParameter(target.getParNamePrefix() + ParInfo.AUTO_TABLE_CREATION);

                    // Remove the Truncate tables option since that's what we are asking the user about and we shouldn't
                    // just do it and then ask the user if it's okay. :)
                    parInfo.removeParameter(target.getParNamePrefix() + ParInfo.TRUNCATE_TABLES);

                    // We don't want the schema to do any of its remap or idgaps table processing
                    parInfo.removeParameter(target.getParNamePrefix() + ParInfo.IDGAPS_TABLE);
                    parInfo.removeParameter(target.getParNamePrefix() + ParInfo.REMAP_TABLE);

                    targetSchema = new Schema(parInfo, target.getParNamePrefix());
                } catch (DBDefines.FatalDBUtilLibException e) {
                    GUI_Util.optionPane_error("Error creating target schema in order to check if "
                            + "target tables exist in the target schema since the Clear Table Data checkbox "
                            + "is selected.\n" + e.getMessage(), this.applicationName + " Error");
                    e.printStackTrace(System.err);
                }
                // These tables already exist ... accumulate them for the prompt
                for (GUITableStruct guiTable : target.getTables())
                    if (targetSchema.getDAO().tableExists(guiTable.tableName.getText()))
                        tables.append(guiTable.tableName.getText() + "\n");

                if (tables.length() > 0) {
                    StringBuilder msg = new StringBuilder("The Clear Table Data checkbox"
                            + " in the Target Schema is selected. Are you sure you wish to"
                            + " delete all of the data in the\n" + tables + "tables?");

                    Object[] options = {"Continue", "Cancel"};
                    int n = JOptionPane.showOptionDialog(this, msg.toString(), this.applicationName + " Question",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                    if (n == 1)
                        return false;
                }
            }

            // If the source and target DAO are both FF, and the flat files have the same name, then the user is going
            // to overwrite existing flat files. Double check that they really want to do that.
            if (source.getDAOType().equals(DBDefines.FF_DAO) && target.getDAOType().equals(DBDefines.FF_DAO)) {
                // Accumulate the filenames in the source and the target
                HashSet<String> sourceFilenames = new HashSet<String>();
                HashSet<String> targetFilenames = new HashSet<String>();
                for (GUITableStruct table : sourceTables)
                    sourceFilenames.add(table.tableName.getText().trim());
                for (GUITableStruct table : targetTables)
                    targetFilenames.add(table.tableName.getText().trim());

                // For all of the source files, see if any target files share the same name.
                ArrayList<String> problemFiles = new ArrayList<String>();
                for (String filename : sourceFilenames)
                    if (targetFilenames.contains(filename))
                        problemFiles.add(filename);

                // some source and target files share the same name
                if (problemFiles.size() > 0) {
                    StringBuilder message = new StringBuilder("These files are in the source schema "
                            + "as well as the target schema.\n");
                    for (String filename : problemFiles)
                        message.append(filename + "\n");
                    message.append("The source version of these files will be overwritten.\nDo you "
                            + "wish to continue?");
                    String title = this.applicationName + " Message";
                    String[] options = new String[]{"Yes", "Cancel"};

                    int selectedOption = GUI_Util.optionPane_yesNo(message.toString(), title, options, 1);

                    if (selectedOption == 1)
                        return false;
                }
            }

            // Run SchemaPanel checks
            try {
                if (this.sourceSchemaPanel.getSelectedSchemaPanel().check() == false)
                    return false;
                if (this.targetSchemaPanel.getSelectedSchemaPanel().check() == false)
                    return false;
            } catch (DBDefines.FatalDBUtilLibException e) {
                System.err.println("Error checking source and/or target schema.\n" + e.getMessage());
                return false;
            }

            return appRunCheck();
        } finally {
            // Show non-busy cursor after the check is done. This is in the finally in case an unanticipated
            // exception gets thrown
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Application specific checks that must be performed before the application can be run.
     *
     * @return whether or not the application can be run
     */
    protected abstract boolean appRunCheck();

    // ~~~~~~~~~~~~~~~~~~~~~~~~~ LISTENERS ~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * Listener that responds to the user choosing to open a new project
     */
    protected class OpenProjectActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            openProject();
        }
    }

    /**
     * Listener that responds to the user choosing to save this current state of the gui to a parameter file.
     */
    private class SaveProjectActionListener implements ActionListener {
        // Whether or not to prompt the user for a filename to save to or to just save to the "current" filename (if
        // there is one)
        private boolean savePrompt;

        public SaveProjectActionListener(boolean savePrompt) {
            this.savePrompt = savePrompt;
        }

        public void actionPerformed(ActionEvent e) {
            saveProject(true, this.savePrompt);
        }
    }

    /**
     * Listener that responds to the user choosing to exit the gui. This asks the user if they wish to save the state of
     * the GUI before exiting or not.
     */
    protected class ExitActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Determine if they want to Exit, Save and then exit, or do not want to exit at all.
            String[] options = {"Exit", "Save Project and Exit", "Cancel"};
            int n = GUI_Util.optionPane_yesNo("Exit " + applicationName + "?", "Exit " + applicationName, options, 1);

            if (n == 0) {
                DBDefines.outputLogs();
                System.exit(0);
            } else if (n == 1) {
                saveProject(true, false);
                DBDefines.outputLogs();
                System.exit(0);
            }
        }
    }
}
