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

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.JTextComponent;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.gui.util.DBUtilLibActionListener;
import gov.sandia.gnem.dbutillib.gui.util.DBUtilLibDocumentListener;
import gov.sandia.gnem.dbutillib.gui.util.DBUtilLibItemListener;
import gov.sandia.gnem.dbutillib.gui.util.DBUtilLibPropertyChangeListener;

/**
 * This class extends ParInfo to add an association between a ParInfo element and a GUI component. If a GUI component
 * changes, the ParInfo element it is associated with will be updated to reflect this. If a ParInfo element changes, the
 * GUI component it is associated with will be updated to reflect this.
 *
 * @author Sandy Ballard
 */
public class ParInfoGui extends ParInfo {
    /**
     * If this is true, debug information will be printed to System.out.
     */
    public final static boolean DEBUG_MODE = false;

    /**
     * A map from a JComponent to the name of the parameter that that JComponent is associated with. The parameter name
     * is not reduced. It has the form originally assigned to it by the developer that made the association between the
     * component and the parameter name.
     */
    private HashMap<JComponent, String> componentToParameter = new HashMap<JComponent, String>();

    /**
     * A map from a reduced parameter name to the JComponents that are associated with it. Reduced parameter names are
     * the original names but are case insensitive and '_' (underscore characters) are ignored. All JComponent except
     * JRadioButtons have only a single JComponent for each parameter name. For JRadioButtons, all the JRadioButtons
     * that are members of the same ButtonGroup have the same parameter name and the HashSet in the parameterToComponent
     * HashMap contains all the JRadioButton objects that share the same parameter name (ie. are in the same
     * ButtonGroup).
     */
    private HashMap<String, HashSet<JComponent>> parameterToComponent = new HashMap<String, HashSet<JComponent>>();

    /**
     * Default constructor. Essenially sets up the default parameters obtained from the system/user's environment.
     */
    public ParInfoGui() {
        super();
    }

    /**
     * Constructor that takes a String as a parameter. If the string contains a newline character (system dependent),
     * then the string is interpreted to be a list of parameter name=value pairs (contents of a par file) and is
     * processed accordingly. If the string does not contain a newline character, it is interpreted to be a file name
     * and the contents of the file are processed.
     *
     * @param init either a file name or name=value pairs separated by newlines
     */
    public ParInfoGui(String init) {
        super(init);
    }

    /**
     * Add a parameter (specified as a name-value pair) to the ParInfo item. This method calls
     * {@link ParInfo#addParameter ParInfo.addParameter} and then ensures that any JComponent objects connected to this
     * parameter name get updated to reflect the new value.
     *
     * @param parName  the name of the parameter
     * @param parValue the value to associate with the name.
     */
    @Override
    public void addParameter(String parName, String parValue) {
        super.addParameter(parName, parValue);
        synchComponents(parameterToComponent.get(reduceParName(parName)));
    }

    /**
     * Append a parameter (specified as a name-value pair) to the ParInfo item. This method calls
     * {@link ParInfo#addParameter ParInfo.appendParameter} and then ensures that any JComponent objects connected to
     * this parameter name get updated to reflect the new value.
     *
     * @param parName  the name of the parameter
     * @param parValue the value to associate with the name.
     */
    @Override
    public void appendParameter(String parName, String parValue) {
        super.appendParameter(parName, parValue);
        synchComponents(parameterToComponent.get(reduceParName(parName)));
    }

    /**
     * Identifies the set consisting of the union of the parameters in this ParInfoGUI object and the parameters
     * associated with the JComponents registered with this ParInfoGUI object. It then takes the intersection of that
     * union with the parameters in the input ParInfo object. All of those parameters (and associated values) are added
     * to this ParInfoGUI object with the addParameter() method. Also ensures that the displays of all registered
     * JComponent objects are updated.
     *
     * @param parInfo a ParInfo object whose parameter name->value pairs are to be added to this ParInfoGui object.
     */
    @Override
    public void updateParameters(ParInfo parInfo) {
        for (JComponent component : componentToParameter.keySet()) {
            String parName = componentToParameter.get(component);
            if (parName != null) {
                String newValue = parInfo.getItem(parName);
                if (newValue != null)
                    addParameter(parName, newValue);
            }
        }
    }

    /**
     * Clear the ParInfo object and set the enabled flags of all associated JComponent objects (except TextFields and
     * TextAreas) to false.
     */
    @Override
    public void clear() {
        clear(false);
    }

    /**
     * Clear the ParInfo object.
     *
     * @param enabled what to set the enabled flags of all associated JComponent objects (except TextFields and
     *                TextAreas) to.
     */
    public void clear(boolean enabled) {
        super.clear();
        for (JComponent component : componentToParameter.keySet())
            if (!className(component).endsWith("TextField") && !className(component).endsWith("TextArea"))
                component.setEnabled(enabled);
    }

    /**
     * Add a JComponent object to the set of JComponent objects that are to be monitored. The current list of supported
     * JComponents includes: <br>
     * JCheckBox <br>
     * JRadioButton <br>
     * JComboBox <br>
     * JTextField <br>
     * JTextArea <br>
     * JPasswordField
     *
     * @param component     the component to be monitored.
     * @param parameterName the parameter name to associate with the JComponent object. For all JComponent types except
     *                      JRadioButton, parameterName should consist of just the parameter name. For JRadioButtons, parameterName should
     *                      consist of 'parameter_name=parameter_value'. Every JRadioButton in the same ButtonGroup should have the same
     *                      'parameter_name' but different 'parameter_value'.
     */
    public void registerComponent(JComponent component, String parameterName) {
        if (component == null)
            return;

        // Handle the special case that is JRadioButtons since it can have
        // multiple JComponents associated with one parameter.
        if (className(component).contains("JRadioButton")) {
            String[] parts = parameterName.split("=");
            if (parts.length != 2) {
                System.out.println("ERROR in " + className(this)
                        + ".registerComponent().  Passed a JRadioButton that does not "
                        + "contain an = sign.  For JRadioButtons, parameterName "
                        + "should consist of 'parameter_name=parameter_value'.  "
                        + "Every JRadioButton in the same ButtonGroup should have "
                        + "the same 'parameter_name' but different 'parameter_value'." + "  parName=" + parameterName);
                // System.exit(1);
            }

            component.putClientProperty("parValue", parts[1].trim());
            componentToParameter.put(component, parts[0].trim());

            String reducedParName = reduceParName(parts[0].trim());

            HashSet<JComponent> components = parameterToComponent.get(reducedParName);
            if (components == null) {
                components = new HashSet<JComponent>();
                parameterToComponent.put(reducedParName, components);
            }
            components.add(component);
        } else {
            componentToParameter.put(component, parameterName);
            HashSet<JComponent> components = new HashSet<JComponent>();
            components.add(component);
            parameterToComponent.put(reduceParName(parameterName), components);
        }

        // Add a PropertyChangeListener to this component. Whenever the
        // component's isEnabled() status changes, synch the parInfo object.
        // This yields a behavior such that when a component becomes
        // disabled, the corresponding parameter is removed from the parInfo
        // object. Whenever the component becomes enabled, the parameter is
        // added to the parInfo object.
        component.addPropertyChangeListener(new JComponentPropertyChangeListener());

        // Deal with action events. Different types of components have different
        // behavior and must be dealt with individually.

        // JTextField and JPasswordField
        if (className(component).endsWith("JTextField") || className(component).endsWith("JPasswordField")) {
            // add an event to synch the parInfo object whenever the text in a jtextfield
            // is changed in any way.
            ((JTextField) component).getDocument().addDocumentListener(new TextListener((JTextField) component));
        }

        // JTextArea
        else if (className(component).endsWith("JTextArea")) {
            // add an event to synch the parInfo object whenever the text in a jtextarea
            // is changed in any way.
            ((JTextArea) component).getDocument().addDocumentListener(new TextListener((JTextArea) component));
        }

        // JComboBox
        else if (className(component).endsWith("JComboBox")) {
            // add an event to synch the parInfo object whenever the text in the
            // JComboBox changes.
            ((JComboBox) component).addActionListener(new JComboBoxActionListener());
        }

        // JCheckBox
        else if (className(component).endsWith("JCheckBox")) {
            // add an event to synch the parInfo object whenever a jcheckbox
            // gets checked or unchecked.
            // add an event to synch the parInfo object whenever a jradiobutton
            // has its status changed.
            ((AbstractButton) component).addItemListener(new JCheckBoxItemListener());
        }

        // JRadioButton
        else if (className(component).endsWith("JRadioButton")) {
            // add an event to synch the parInfo object whenever a jradiobutton
            // has its status changed.
            ((AbstractButton) component).addItemListener(new JRadioButtonItemListener());
        }

        synchParInfo(component);
    }

    /**
     * Extract information from a JComponent and update the appropriate parameter value in the ParInfo object.
     *
     * @param component JComponent whose information is to be used to update the ParInfo object.
     */
    public void synchParInfo(JComponent component) {
        if (className(component).contains("JCheckBox"))
            synchParInfo((JCheckBox) component);
        else if (className(component).contains("JTextField") || className(component).endsWith("JPasswordField"))
            synchParInfo((JTextField) component);
        else if (className(component).contains("JTextArea"))
            synchParInfo((JTextArea) component);
        else if (className(component).contains("JComboBox"))
            synchParInfo((JComboBox) component);
        else if (className(component).contains("JRadioButton"))
            synchParInfo((JRadioButton) component);
    }

    /**
     * Extract information from a JCheckBox and update the appropriate parameter value in the ParInfo object.
     *
     * @param component JCheckBox whose information is to be used to update the ParInfo object.
     */
    private void synchParInfo(JCheckBox component) {
        String parName = componentToParameter.get(component);
        if (parName == null) {
            System.out.println("ERROR in " + this.getClass().getName()
                    + ".synchParInfo().  parName not set in JComponent " + component.getName());
        }
        if (!component.isEnabled())
            super.removeParameter(parName);
        else if (component.isSelected())
            super.addParameter(parName, "true");
        else
            super.addParameter(parName, "false");
    }

    /**
     * Extract information from a JRadioButton and update the appropriate parameter value in the ParInfo object.
     *
     * @param component JRadioButton whose information is to be used to update the ParInfo object.
     */
    private void synchParInfo(JRadioButton component) {
        String parName = componentToParameter.get(component);
        if (parName == null) {
            System.out.println("ERROR in " + this.getClass().getName()
                    + ".synchParInfo().  parName not set in JComponent " + component.getName());
            // System.exit(1);
        }
        String parValue = (String) component.getClientProperty("parValue");
        if (component.isEnabled() && component.isSelected()) {
            if (DEBUG_MODE)
                System.out.println("ParInfoGui.synchParInfo(" + className(component) + ")  " + parName + " = "
                        + parValue + " being added");
            super.addParameter(parName, parValue);
        } else {
            if (DEBUG_MODE)
                System.out.println("ParInfoGui.synchParInfo(" + className(component) + ")  " + parName + " = "
                        + parValue + " being removed");
            super.removeParameter(parName);
        }
    }

    /**
     * Extract information from a JComboBox and update the appropriate parameter value in the ParInfo object.
     *
     * @param component JComboBox whose information is to be used to update the ParInfo object.
     */
    private void synchParInfo(JComboBox component) {
        String parName = componentToParameter.get(component);
        if (parName == null) {
            System.out.println("ERROR in " + this.getClass().getName()
                    + ".synchParInfo().  parName not set in JComponent " + component.getName());
            // System.exit(1);
        }
        if (!component.isEnabled())
            super.removeParameter(parName);

        if (component.getSelectedItem() == null)
            super.removeParameter(parName);
        else {
            String parValue = ((String) component.getSelectedItem()).trim();
            if (parValue.length() > 0)
                super.addParameter(parName, parValue);
            else
                super.removeParameter(parName);
        }
    }

    /**
     * Extract information from a JTextComponent and update the appropriate parameter value in the ParInfo object.
     *
     * @param component JTextComponent whose information is to be used to update the ParInfo object.
     */
    private void synchParInfo(JTextComponent component) {
        String parName = componentToParameter.get(component);
        if (parName == null)
            return;
        if (!component.isEnabled()) {
            super.removeParameter(parName);
            if (DEBUG_MODE) {
                System.out.println("ParInfoGui.synchParInfo(JTextField). removing parameter " + parName
                        + " because it has been disabled.");
            }
        } else {
            String parValue = component.getText().trim();
            if (parValue.length() > 0) {
                if (DEBUG_MODE) {
                    System.out.println("ParInfoGui.synchParInfo(JTextField). adding parameter " + parName + " = "
                            + parValue);
                }
                super.addParameter(parName, parValue);
            } else {
                super.removeParameter(parName);
                if (DEBUG_MODE) {
                    System.out.println("ParInfoGui.synchParInfo(JTextField). removing parameter " + parName
                            + " because parValue =\"\"");
                }
            }
        }
    }

    /**
     * Extract information from the ParInfo object and update the information displayed in the JComponent
     *
     * @param components JComponents whose information is to be updated.
     */
    private void synchComponents(HashSet<JComponent> components) {
        if (components == null || componentToParameter == null)
            return;

        // note that only JRadioButtons will have more than one component
        for (JComponent component : components) {
            String parName = componentToParameter.get(component);
            String parValue = getItem(parName);
            String classname = className(component);

            if (DEBUG_MODE)
                System.out.println("synchComponent " + classname + "  " + parName + " = " + parValue);

            if (parValue != null) {
                if (classname.contains("JCheckBox")) {
                    // Set the check box state to the ParInfo value
                    ((JCheckBox) component).setSelected(Boolean.parseBoolean(parValue));
                } else if (classname.contains("JTextField")) {
                    // Set the text field to the value of the ParInfo value
                    ((JTextField) component).setText(parValue);
                } else if (classname.contains("JTextArea")) {
                    // Set the text area to the value of the ParInfo value
                    ((JTextArea) component).setText(parValue);
                } else if (classname.contains("JPasswordField")) {
                    // Set the text field to the value of the ParInfo value
                    ((JPasswordField) component).setText(parValue);
                } else if (classname.contains("JComboBox")) {
                    if (((JComboBox) component).isEditable())
                        // Synchronize the selected item of the combo box with the ParInfo value
                        ((JComboBox) component).setSelectedItem(parValue);
                    else {
                        // Synchronize the selected item of the combo box with the ParInfo value
                        for (int i = 0; i < ((JComboBox) component).getItemCount(); i++) {
                            if (((String) ((JComboBox) component).getItemAt(i)).equals(parValue)) {
                                ((JComboBox) component).setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                } else if (classname.contains("JRadioButton")) {
                    String storedValue = (String) component.getClientProperty("parValue");
                    if (storedValue == null) {
                        System.out
                                .println("ERROR in "
                                        + className(this)
                                        + ".synchJPanel().  "
                                        + "Found a JRadioButton whose clientProperty HashMap does not contain an entry with key='parValue'. "
                                        + "  parName=" + parName + "  componentName=" + component.getName());
                        // System.exit(1);
                    }

                    if (storedValue.equals(parValue)) {
                        if (DEBUG_MODE)
                            System.out.println("     synchJRadioButton " + parName + " is being set selected.");
                        ((JRadioButton) component).setSelected(true);
                    }
                }
            }
        }
    }

    /**
     * Returns the class name for a given object.
     *
     * @param obj object to return the class name for
     */
    private String className(Object obj) {
        String name = obj.getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    /**
     * Listener that syncs up changes to text fields with the parameter for that textfield anytime the textfield is
     * changed in any way.
     */
    public class TextListener extends DBUtilLibDocumentListener {
        javax.swing.text.JTextComponent source;

        public TextListener(javax.swing.text.JTextComponent source) {
            this.source = source;
            if (DEBUG_MODE)
                System.out.println("Creating TextListener for JComponent " + componentToParameter.get(source));
        }

        public void listenersOnUndoableEditHappened(UndoableEditEvent undoableEditEvent) {
            if (DEBUG_MODE)
                System.out.println("TextListener.undoableEditHappened() for JComponent "
                        + componentToParameter.get(source));
            synchParInfo(source);
        }

        @Override
        public void listenersOnInsertUpdate(DocumentEvent documentEvent) {
            if (DEBUG_MODE)
                System.out.println("TextListener.insertUpdate() for JComponent " + componentToParameter.get(source));
            synchParInfo(source);
        }

        @Override
        public void listenersOnRemoveUpdate(DocumentEvent documentEvent) {
            if (DEBUG_MODE)
                System.out.println("TextListener.removeUpdate() for JComponent " + componentToParameter.get(source));
            synchParInfo(source);
        }

        @Override
        public void listenersOnChangedUpdate(DocumentEvent documentEvent) {
            if (DEBUG_MODE)
                System.out.println("TextListener.changedUpdate() for JComponent " + componentToParameter.get(source));
            synchParInfo(source);
        }
    }

    public final static String AUTO_FILL_TABLE_TYPES = "AutoFillTableTypes";

    public final static String IDGAPS_TABLE_AUTO_FILL = "IdGapsTableAutoFill";

    public final static String REMAP_TABLE_AUTO_FILL = "RemapTableAutoFill";

    public final static String RANKING_TABLE_AUTO_FILL = "RankingTableAutoFill";

    public final static String SCHEMA_PANEL_TYPE = "SchemaPanelType";

    public final static String MODIFIABLE = "Modifiable";

    private class JComboBoxActionListener extends DBUtilLibActionListener {
        @Override
        public void listenersOnActionPerformed(ActionEvent actionEvent) {
            if (DEBUG_MODE) {
                System.out.println(className(actionEvent.getSource()) + ".actionPerformed() parName = "
                        + componentToParameter.get((JComponent) actionEvent.getSource()));
            }
            synchParInfo((JComboBox) actionEvent.getSource());
        }
    }

    private class JCheckBoxItemListener extends DBUtilLibItemListener {
        @Override
        public void listenersOnItemStateChanged(ItemEvent itemEvent) {
            if (DEBUG_MODE) {
                System.out.println(className(itemEvent.getSource()) + ".itemStateChanged() parName = "
                        + componentToParameter.get((JComponent) itemEvent.getSource()));
            }
            synchParInfo((JCheckBox) itemEvent.getSource());
        }
    }

    private class JRadioButtonItemListener extends DBUtilLibItemListener {
        @Override
        public void listenersOnItemStateChanged(ItemEvent itemEvent) {
            if (DEBUG_MODE) {
                System.out.println(className(itemEvent.getSource()) + ".itemStateChanged() parName = "
                        + componentToParameter.get((JComponent) itemEvent.getSource()));
            }
            synchParInfo((JRadioButton) itemEvent.getSource());
        }
    }

    private class JComponentPropertyChangeListener extends DBUtilLibPropertyChangeListener {
        @Override
        public void listenersOnPropertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (DEBUG_MODE) {
                System.out.println(className(propertyChangeEvent.getSource()) + ".propertyChange() parName = "
                        + componentToParameter.get((JComponent) propertyChangeEvent.getSource()));
            }
            synchParInfo((JComponent) propertyChangeEvent.getSource());
        }
    }
}
