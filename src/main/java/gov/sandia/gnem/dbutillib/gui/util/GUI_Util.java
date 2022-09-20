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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListModel;
import javax.swing.SwingConstants;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.util.DBDefines;

public class GUI_Util {
    public GUI_Util() {
    }

    /**
     * Keep track of whether or not the "Cannot find kbdb accounts file" error has been issued. This way, it doesn't
     * need to be issued over and over and over.
     */
    private static boolean accountsFileErrorIssued = false;

    /**
     * Return the location(s) of the KBDB accounts file(s). This method looks for the file in three different places -
     * the location defined in the KBDB_ACCOUNTS_FILE environment variable first, the user's home directory, and the
     * current working directory - in that order. If a file is found in any of those locations, the path to that file is
     * added to the list of files to be returned.
     *
     * @return a list of the locations of KBDB accounts file(s)
     */
    public static ArrayList<String> findKBDB_ACCOUNTS_FILE() {
        ArrayList<String> kbdbfiles = new ArrayList<String>();

        // Look in the area defined in the KBDB_ACCOUNTS_FILE variable
        String filePath = DBDefines.KBDB_ACCOUNTS_FILE;
        if (filePath != null && new File(filePath).exists())
            kbdbfiles.add(filePath);

        // Extract what file name to look for
        String fileName = "";

        // If no KBDB_ACCOUNTS_FILE is defined, look for a file named kbdb.cfg
        if (filePath == null)
            fileName = "kbdb.cfg";
            // If KBDB_ACCOUNTS_FILE is defined, strip the file name out of it and
            // look for files by that name.
        else
            fileName = filePath.substring(filePath.lastIndexOf(DBDefines.PATH_SEPARATOR) + 1);

        // Look in the user's home directory.
        filePath = System.getProperty("user.home") + DBDefines.PATH_SEPARATOR + fileName;
        if (new File(filePath).exists())
            kbdbfiles.add(filePath);

        // Look in the current working directory
        filePath = System.getProperty("user.dir") + DBDefines.PATH_SEPARATOR + fileName;
        if (new File(filePath).exists())
            kbdbfiles.add(filePath);

        // If no files were found, issue a warning ...
        if (!accountsFileErrorIssued && kbdbfiles.size() == 0) {
            String msg = "WARNING in GUI_Constants.findKBDB_ACCOUNTS_FILE().  "
                    + "Cannot find kbdb configuration file.\nNo KBDB_ACCOUNTS_FILE "
                    + " environment variable is defined and there are no configuration" + "\nfiles named " + fileName
                    + " in the current working directory\n\t(" + System.getProperty("user.dir")
                    + ")\nor the user's home directory\n\t(" + System.getProperty("user.home") + ").";

            if (GUI_Constants.STATUS_BAR != null) {
                String shortMsg = "No kbdb config file found in KBDB_ACCOUNTS_FILE, current dir, or " + "user home.";
                GUI_Constants.setStatusBarText(shortMsg);
            } else
                System.out.println(msg);

            DBDefines.WARNING_LOG.add(msg);
            accountsFileErrorIssued = true;
        }

        return kbdbfiles;
    }

    public static String getConfigType(ParInfo configurationParInfo, String prefix) {
        String configType = prefix + GUI_Constants.CONFIG_SEPARATOR + GUI_Constants.CONFIG_TYPE;

        String type = configurationParInfo.getItem(configType, "");
        return type;
    }

    public static String getConfigFlatFilePath(ParInfo configurationParInfo, String prefix) {
        String configPath = prefix + GUI_Constants.CONFIG_SEPARATOR + GUI_Constants.CONFIG_FLATFILE
                + GUI_Constants.CONFIG_SEPARATOR + GUI_Constants.CONFIG_PATH;

        String path = configurationParInfo.getItem(configPath, "");
        return path;
    }

    public static JTextField textField_tiny(String initialText) {
        JTextField textField = new JTextField(GUI_Constants.tinyTextColumns);
        textField.setMinimumSize(textField.getPreferredSize());
        textField.setText(initialText);
        return textField;
    }

    public static JTextField textField_tiny() {
        return textField_tiny("");
    }

    public static JTextField textField_short(String initialText) {
        JTextField textField = new JTextField(GUI_Constants.shortTextColumns);
        textField.setMinimumSize(textField.getPreferredSize());
        textField.setText(initialText);
        return textField;
    }

    public static JTextField textField_short() {
        return textField_short("");
    }

    public static JTextField textField_medium(String initialText) {
        JTextField textField = new JTextField(GUI_Constants.mediumTextColumns);
        textField.setMinimumSize(textField.getPreferredSize());
        textField.setText(initialText);
        return textField;
    }

    public static JTextField textField_medium() {
        return textField_medium("");
    }

    public static JTextField textField_long(String initialText) {
        JTextField textField = new JTextField(GUI_Constants.longTextColumns);
        textField.setMinimumSize(textField.getPreferredSize());
        textField.setText(initialText);
        return textField;
    }

    public static JTextField textField_long() {
        return textField_long("");
    }

    public static JTextArea textArea_uneditableLineWrap(String initialText) {
        JTextArea textArea = new JTextArea(initialText);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    public static JComboBox comboBox(String[] items) {
        JComboBox comboBox = new JComboBox(items);
        return comboBox;
    }

    public static JSplitPane splitPane(Component left, Component right) {
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(new JScrollPane(left));
        splitPane.setRightComponent(new JScrollPane(right));
        splitPane.setDividerLocation(splitPane.getPreferredSize().width / 2 - 5);
        return splitPane;
    }

    public static JButton button_noInsets(String buttonText) {
        JButton button = new JButton(buttonText);
        button.setMargin(new Insets(0, 0, 0, 0));
        return button;
    }

    public static JLabel label_plain(String labelText) {
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(label.getPreferredSize().width, label.getPreferredSize().height));
        return label;
    }

    public static JLabel label_fancy(String labelText, String fontType, int fontStyle, int fontSize,
                                     Color foregroundColor) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(fontType, fontStyle, fontSize));
        if (foregroundColor != null)
            label.setForeground(foregroundColor);
        return label;
    }

    public static JLabel label_fancy(String labelText, String fontType, int fontStyle, int fontSize) {
        return label_fancy(labelText, fontType, fontStyle, fontSize, null);
    }

    public static JPasswordField passwordField_short() {
        JPasswordField passwordField = new JPasswordField(GUI_Constants.mediumTextColumns);
        passwordField.setMinimumSize(passwordField.getPreferredSize());
        return passwordField;
    }

    public static Component rigidArea_flatNarrow() {
        return Box.createRigidArea(new Dimension(10, 0));
    }

    public static void gridBag_right(GridBagConstraints gbc) {
        gbc.gridx++;
    }

    public static void gridBag_down(GridBagConstraints gbc) {
        gbc.gridy++;
    }

    public static void gridBag_up(GridBagConstraints gbc) {
        gbc.gridy--;
    }

    public static void gridBag_newColumn(GridBagConstraints gbc) {
        gbc.gridy = 0;
        gbc.gridx++;
    }

    public static void gridBag_newRow(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
    }

    public static void gridBag_centerAnchor(GridBagConstraints gbc) {
        gbc.anchor = GridBagConstraints.CENTER;
    }

    public static void gridBag_eastAnchor(GridBagConstraints gbc) {
        gbc.anchor = GridBagConstraints.EAST;
    }

    public static void gridBag_northwestAnchor(GridBagConstraints gbc) {
        gbc.anchor = GridBagConstraints.NORTHWEST;
    }

    public static void gridBag_fullWidth(GridBagConstraints gbc) {
        gbc.gridwidth = GridBagConstraints.REMAINDER;
    }

    public static void gridBag_horizontalFill(GridBagConstraints gbc) {
        gbc.fill = GridBagConstraints.HORIZONTAL;
    }

    public static void gridBag_verticalFill(GridBagConstraints gbc) {
        gbc.fill = GridBagConstraints.VERTICAL;
    }

    public static void gridBag_bothFill(GridBagConstraints gbc) {
        gbc.fill = GridBagConstraints.BOTH;
    }

    public static void gridBag_noFill(GridBagConstraints gbc) {
        gbc.fill = GridBagConstraints.NONE;
    }

    public static void gridBag_useWholeLine(GridBagConstraints gbc) {
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
    }

    public static void gridBag_noInsets(GridBagConstraints gbc) {
        gbc.insets = new Insets(0, 0, 0, 0);
    }

    public static GridBagConstraints gridBagConstraints_centered() {
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
        return gbc;
    }

    public static void gridBag_doNotDistributeSpace(GridBagConstraints gbc) {
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
    }

    public static JToggleButton toggleButton_leftAligned(String text, ImageIcon icon) {
        JToggleButton toggleButton = null;
        if (icon != null)
            toggleButton = new JToggleButton(text, icon);
        else
            toggleButton = new JToggleButton(text);

        toggleButton.setHorizontalTextPosition(SwingConstants.LEFT);
        toggleButton.setHorizontalAlignment(SwingConstants.LEFT);
        toggleButton.setMargin(new Insets(0, 0, 0, 0));
        return toggleButton;
    }

    public static JToggleButton toggleButton_centerAligned(String text, ImageIcon icon) {
        JToggleButton toggleButton = null;
        if (icon != null)
            toggleButton = new JToggleButton(text, icon);
        else
            toggleButton = new JToggleButton(text);

        toggleButton.setHorizontalTextPosition(SwingConstants.CENTER);
        toggleButton.setHorizontalAlignment(SwingConstants.CENTER);
        toggleButton.setMargin(new Insets(0, 0, 0, 0));
        return toggleButton;
    }

    public static GridBagConstraints gridBagConstraints_northwestAligned() {
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
                GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
        return gbc;
    }

    public static JCheckBox checkBox_noMargin(String checkBoxText) {
        JCheckBox checkBox = new JCheckBox(checkBoxText);
        checkBox.setMargin(new Insets(0, 0, 0, 0));
        return checkBox;
    }

    public static JMenu menu(String menuName, JComponent[] menuItems) {
        JMenu menu = new JMenu(menuName);
        for (JComponent menuItem : menuItems)
            menu.add(menuItem);
        return menu;
    }

    public static JMenuBar menubar(JMenuItem[] menuItems) {
        JMenuBar menuBar = new JMenuBar();
        for (JMenuItem menuItem : menuItems)
            menuBar.add(menuItem);
        return menuBar;
    }

    public static int optionPane_yesNo(String message, String title, String[] options, int defaultOptionIndex) {
        int selectedOptionIndex = JOptionPane.showOptionDialog(null, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[defaultOptionIndex]);
        return selectedOptionIndex;
    }

    public static void optionPane_error(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void optionPane_message(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.DEFAULT_OPTION);
    }

    public static final File fileChooser_open(String baseDirectory, String title, String approveButtonText) {
        JFileChooser chooser = new JFileChooser(new File(baseDirectory));
        if (approveButtonText != null)
            chooser.setApproveButtonText(approveButtonText);
        chooser.setDialogTitle(title);
        chooser.showOpenDialog(null);
        File file = chooser.getSelectedFile();
        return file;
    }

    public static File fileChooser_open(String baseDirectory, String title) {
        return fileChooser_open(baseDirectory, title, null);
    }

    public static String getUserDir() {
        return System.getProperty("user.dir");
    }

    /**
     * Color code a parameter label to indicate that it is an optional parameter
     *
     * @param parameterComponent parameter component to be color coded to indicate that it is an optional parameter
     */
    public static void optionalParameterColor(Component parameterComponent) {
        parameterComponent.setForeground(GUI_Constants.OPTIONAL_PARAMETER_COLOR);
    }

    /**
     * Color code a parameter label to indicate that it is a required parameter
     *
     * @param parameterComponent parameter component to be color coded to indicate that it is a required parameter
     */
    public static void requiredParameterColor(Component parameterComponent) {
        parameterComponent.setForeground(GUI_Constants.REQUIRED_PARAMETER_COLOR);
    }

    /**
     * Move items from fromList to toList. The items will be removed from the fromList and added to the toList.
     *
     * @param itemsToMove items to move from fromList to toList
     * @param fromList    list items are to be removed from
     * @param toList      list items are to be added to
     */
    public static void moveListItems(Object[] itemsToMove, JList fromList, JList toList) {
        // Retrieve ListModels
        ListModel fromModel = fromList.getModel();
        ListModel toModel = toList.getModel();

        // Build an ArrayList of items in the toList
        ArrayList<String> toValues = new ArrayList<String>();
        int toValuesLength = toModel.getSize();
        for (int i = 0; i < toValuesLength; i++)
            toValues.add(toModel.getElementAt(i).toString());

        // Build an ArrayList of items in the fromList
        ArrayList<String> fromValues = new ArrayList<String>();
        int fromValuesLength = fromModel.getSize();
        for (int i = 0; i < fromValuesLength; i++)
            fromValues.add(fromModel.getElementAt(i).toString());

        // Remove itemsToMove from fromList and add itemsToMove to toList
        for (int i = 0; i < itemsToMove.length; i++) {
            fromValues.remove(itemsToMove[i].toString());
            toValues.add(itemsToMove[i].toString());
        }

        // Sort the values before adding them to the lists
        TreeSet<String> sortedFromValues = new TreeSet<String>(fromValues);
        TreeSet<String> sortedToValues = new TreeSet<String>(toValues);

        // Add the items to the list
        fromList.setListData(sortedFromValues.toArray());
        toList.setListData(sortedToValues.toArray());
    }
}
