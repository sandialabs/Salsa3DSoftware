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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/*
 * This is Marcus's code swiped from the gov.sandia.gnem.kbcit.gui package.
 * <p>
 * This helper class is a JFrame that displays
 * a progress bar. This can be used to display progress during long operations. This is different from ProgressMonitor
 * in that it is always displayed immediately, and does not have a Cancel button. Also, due to the Synthetica Look &
 * Feel not supporting strings being painted within the progress bar, this actually uses a label to paint the string
 * above the progress bar.
 */
@SuppressWarnings("serial")
public class JProgressFrame extends JFrame {
    private JProgressBar progBar;

    private JLabel progLabel;

    private JButton cancelButton;

    private SwingWorker<?, ?> swingWorker;

    /**
     * Create a new frame with a progress bar in it. This version has no Cancel button.
     *
     * @param parentComponent the parent component (generally a JFrameApplication)
     * @param progressString  string to display for progress bar, or null for default
     * @param indeterminate   true if indeterminate progress bar
     */
    public JProgressFrame(Component parentComponent, String progressString, boolean indeterminate) {
        this(parentComponent, progressString, indeterminate, null); // do NOT use SwingWorker (null)

    }

    /**
     * DO NOT USE DIRECTLY - NOT FULLY WORKING YET IN TERMS OF CALLER RECOGNIZING INTERRUPTS. Create a new frame with a
     * progress bar in it, and also add a Cancel button that will interrupt the given SwingWorker if it is canceled. The
     * caller worker is responsible for checking whether the thread has been interrupted, and also calling finished() if
     * necessary.
     *
     * @param parentComponent the parent component (generally a JFrameApplication)
     * @param progressString  string to display for progress bar, or null for default
     * @param indeterminate   true if indeterminate progress bar
     * @param worker          SwingWorker to interrupt if canceled, or null to have no cancel option
     */
    private JProgressFrame(Component parentComponent, String progressString, boolean indeterminate, SwingWorker<?, ?> worker) {

        // create progress frame for holding progress bar
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Progress Bar");

        // save worker
        swingWorker = worker;

        // disable resizing and set to undecorated so there is no minimize, maximize, X buttons
        setVisible(false);
        setResizable(false);
        setUndecorated(true);
        setAlwaysOnTop(true);

        // set location based on parent's location (50, 50 below top left corner)
        Point p = parentComponent.getLocationOnScreen();
        if (p != null)
            setLocation(p.x + 50, p.y + 50);
        else
            setLocation(50, 50);

        // create progress bar
        progBar = new JProgressBar();
        progBar.setIndeterminate(indeterminate);
        progBar.setValue(0);
        progBar.setMinimum(0);
        progBar.setMaximum(100);
        progBar.setPreferredSize(new Dimension(411, 15));
        progBar.setOpaque(true);
        progBar.setBackground(new Color(225, 235, 255)); // very light blue

        progBar.setForeground(new Color(112, 159, 255)); // medium blue

        // create progress label
        progLabel = new JLabel(progressString);

        // create a panel for the progress bar with a string on top of it
        JPanel progPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = createGbc();
        c.insets = new Insets(0, 5, 3, 5);
        addGbc(progPanel, progLabel, c, 0, 0);
        addGbc(progPanel, progBar, c, 0, 1);

        // if SwingWorker is not null, then add a cancel button
        // below the progress bar
        if (swingWorker != null) {
            cancelButton = new JButton("Cancel");
            Dimension defaultSize = cancelButton.getSize();
            cancelButton.setSize(defaultSize.width, defaultSize.height - 5);
            cancelButton.setToolTipText("Cancel the operation");
            cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // interrupt the swing worker
                    if (swingWorker != null) {
                        // System.out.println("Interrupting worker");
                        // swingWorker.interrupt();
                        System.out.println("Canceling worker");
                        swingWorker.cancel(true);
                    }
                }
            });

            c.anchor = GridBagConstraints.CENTER;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(7, 10, 0, 10);
            addGbc(progPanel, cancelButton, c, 0, 2);
        }

        progPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(darkBlue, 2),
                BorderFactory.createEmptyBorder(7, 5, 7, 5)));

        // set background to white
        progPanel.setBackground(Color.WHITE);

        // add the progress panel to the frame
        add(progPanel);
        pack();
        setVisible(true);
    }

    /**
     * Get the progress bar in order to update its value. Note that the string is NOT painted, so calling setString()
     * has no effect.
     *
     * @return progress bar
     */
    public JProgressBar getProgBar() {
        return progBar;
    }

    /**
     * Update the progress string displayed above the progress bar. Note that the string is NOT painted, so calling
     * setString() on the progress bar has no effect. This function should be used instead.
     *
     * @param progString new progress string to display above progress bar
     */
    public void setProgressString(final String progString) {
        // run on EDT if not already on EDT
        if (SwingUtilities.isEventDispatchThread()) {
            progLabel.setText(progString);
            pack();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progLabel.setText(progString);
                    pack();
                }
            });
        }
    }

    /**
     * Set the progress bar's value.
     *
     * @param n new value
     */
    public void setProgressValue(final int n) {
        // run on EDT if not already on EDT
        if (SwingUtilities.isEventDispatchThread()) {
            progBar.setValue(n);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progBar.setValue(n);
                }
            });
        }
    }

    /**
     * Set the progress bar's maximum value.
     *
     * @param max new max value
     */
    public void setProgressMaximum(final int max) {
        // run on EDT if not already on EDT
        if (SwingUtilities.isEventDispatchThread()) {
            progBar.setMaximum(max);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progBar.setMaximum(max);
                }
            });
        }
    }

    /**
     * If there was a cancel button added to the progress bar, this will set its visibility
     *
     * @param b true for visible
     */
    public void setCancelVisible(final boolean b) {

        if (cancelButton == null)
            return;

        // run on EDT if not already on EDT
        if (SwingUtilities.isEventDispatchThread()) {
            cancelButton.setVisible(b);
            pack();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    cancelButton.setVisible(b);
                    pack();
                }
            });
        }
    }

    /**
     * Creates a default GridBagConstraints object with anchor set to FIRST_LINE_START, fill set to HORIZONTAL, insets
     * set to 5 all around, and weightx/weighty set to 0.
     *
     * @return a new GridBagConstraints object
     */
    public static GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
    }

    /**
     * Sets gridx and gridy into the given GridBagContraints object, and then adds the component to the given container.
     */
    public static void addGbc(Container addToContainer, Component component, GridBagConstraints gbc, int gridx,
                              int gridy) {
        // set gridX and gridY to input settings
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        addToContainer.add(component, gbc);
    }

    /**
     * A dark blue color.
     */
    private static final Color darkBlue = new Color(0, 0, 170);

}
