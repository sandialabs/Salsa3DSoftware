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
package gov.sandia.gmp.rayuncertainty;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.gui.MultiValueProgressBar;
import gov.sandia.gmp.util.numerical.matrixblock.MatrixBlockDefinition;

/**
 * Builds the RayUncertainty User Interface (UI) that allows a user to track
 * the progress of the RayUncertainty solution. The UI consists of 3 panels
 * including a status panel at the top of the window that describes the
 * problem and the solution elapsed time and current process state, and 2
 * other panels in a tabbed pane format which includes ray prediction and
 * the ray uncertainty calculation. Depending on which solutions are requested
 * one or two panes will be present. If only one is present the tabbed pane does
 * not appear.
 */
@SuppressWarnings("serial")
public class RayUncertaintyUI extends JFrame
        implements WindowListener {
    /**
     * The label array for the initial status panel.
     */
    private JLabel[] aInitLabels = null;

    /**
     * The initial panel Status progress bar.
     */
    private MultiValueProgressBar aInitProgBar = null;

    /**
     * The status progress bar values.
     */
    private int[] aInitProgValue = {1, 1};

    /**
     * The start time of the problem.
     */
    private long aInitStartTime = 0;

    /**
     * The label array for the ray prediction panel.
     */
    private JLabel[] aPredLabels = null;

    /**
     * The prediction panel progress bar.
     */
    private MultiValueProgressBar aPredProgBar = null;

    /**
     * The prediction panel start time.
     */
    private long aPredStartTime = 0;

    /**
     * Used to update processor count by calculating delta time.
     */
    private long aPredLastCurrTime = 0;

    /**
     * The prediction panel processor count time summation.
     */
    private long aPredProcCountTime = 0;

    /**
     * The total node time for performing ray predictions.
     */
    private long aPredNodeTime = 0;

    /**
     * The current ray prediction panel process task count.
     */
    private int aPredTasks = 0;

    /**
     * The total number of ray prediction panel tasks to perform.
     */
    private int aTotalPredTasks = 0;

    /**
     * The label array for the ray uncertainty panel.
     */
    private JLabel[] aRayUncLabels = null;

    /**
     * The ray uncertainty progress bar.
     */
    private MultiValueProgressBar aRayUncProgBar = null;

    /**
     * The ray uncertainty work fraction bar.
     */
    private MultiValueProgressBar aRayUncWork = null;

    /**
     * The ray uncertainty panel start time.
     */
    private long aRayUncStartTime = 0;

    /**
     * Used to update processor count by calculating delta time.
     */
    private long aRayUncLastCurrTime = 0;

    /**
     * The ray uncertainty panel processor count time summation.
     */
    private long aRayUncProcCountTime = 0;

    /**
     * The ray uncertainty panel process task count.
     */
    private int aRayUncTasks = 0;

    /**
     * The total number of ray uncertainty panel tasks to perform.
     */
    private int aTotalRayUncTasks = 0;

    /**
     * The ray uncertainty work fraction progress bar values.
     */
    private double[] aRayUncWorkFracs =
            {0.0, 0.0, 0.0, 0.0};

    /**
     * Default constructor sets the window listener to this JFrame.
     */
    public RayUncertaintyUI() {
        addWindowListener(this);
    }

    /**
     * Builds the UI GUI. This includes populating calling the function populate
     * the four sub-panels as well as setting them up to fit in the main UI
     * window.
     */
    public void makeGUI(RayUncertainty.SolutionPhase[] stage) {
        // make some borders for the spacer panel (spc_pnl)

        Border ebl = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder tbstatus, tbperformance;

        tbstatus = BorderFactory.createTitledBorder(ebl, "LSINV Status");
        tbstatus.setTitleJustification(TitledBorder.CENTER);
        tbstatus.setTitlePosition(TitledBorder.CENTER);

        tbperformance = BorderFactory.createTitledBorder(ebl,
                "Ray Uncertainty Stage Execution");
        tbperformance.setTitleJustification(TitledBorder.CENTER);
        tbperformance.setTitlePosition(TitledBorder.CENTER);

        // create main panel and add upper (north) status panel

        JPanel mainPnl = new JPanel();
        mainPnl.setLayout(new BorderLayout());
        JPanel initialStatus = new JPanel();
        initialStatus.setLayout(new BoxLayout(initialStatus, BoxLayout.Y_AXIS));
        mainPnl.add(initialStatus, BorderLayout.NORTH);

        // build initialStatus panel

        buildInitialStatusPanel(initialStatus, stage);

        // build tabbed pane panel

        JPanel aStageCntnr = new JPanel();
        aStageCntnr.setBorder(tbperformance);
        aStageCntnr.setLayout(new BorderLayout());
        mainPnl.add(aStageCntnr, BorderLayout.CENTER);

        // create ray prediction, block map, and ray uncertainty panels

        JPanel pnlPrediction = new JPanel();
        JPanel pnlRayUncertainty = new JPanel();
        JTabbedPane aTabbedPane = null;

        // populate tabbed pane if more than 1 stage is to be evaluated

        if (stage.length > 1) {
            // more than 1 stage ... create tabbed pane and add to window.

            aTabbedPane = new JTabbedPane();
            aStageCntnr.add(aTabbedPane, BorderLayout.CENTER);

            // loop over each stage and build requested panels ... add each to the
            // tabbed pane.

            for (RayUncertainty.SolutionPhase sp : stage) {
                if (sp == RayUncertainty.SolutionPhase.PREDICTION) {
                    buildPredictionPanel(pnlPrediction);
                    aTabbedPane.add("Ray Prediction", pnlPrediction);
                } else if (sp == RayUncertainty.SolutionPhase.RAY_UNCERTAINTY) {
                    buildRayUncertaintyPanel(pnlRayUncertainty);
                    aTabbedPane.add("Ray Uncertainty", pnlRayUncertainty);
                }
            }
        } else {
            // add single JPanel for one stage

            if (stage[0] == RayUncertainty.SolutionPhase.PREDICTION) {
                buildPredictionPanel(pnlPrediction);
                aStageCntnr.add(pnlPrediction, BorderLayout.CENTER);
            } else if (stage[0] == RayUncertainty.SolutionPhase.RAY_UNCERTAINTY) {
                buildRayUncertaintyPanel(pnlRayUncertainty);
                aStageCntnr.add(pnlRayUncertainty, BorderLayout.CENTER);
            }
        }

        // create the control display frame and add the main panel to it ...

        JScrollPane scrollBar = new JScrollPane(mainPnl);
        scrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.
                VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.
                HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollBar.getHorizontalScrollBar().setUnitIncrement(100);
        scrollBar.getVerticalScrollBar().setUnitIncrement(100);
        setTitle("  Ray Uncertainty Solution  ");
        add(scrollBar);
        pack();
        setVisible(true);

        // set the size (vertical) to some reasonable number and validate

        Dimension d = getSize();
        Dimension scrnsize = Toolkit.getDefaultToolkit().getScreenSize();
        if (d.height > scrnsize.height) setSize(d.width,
                (int) (0.9 * scrnsize.height));
        validate();
    }

    /**
     * Sets the initial status panel values.
     *
     * @param srcType       The source type string (DATABASE, GEOMODEL, or
     *                      PROPERTIESFILE).
     * @param filPath       The path to where the source is defined.
     * @param phaseCount    The number of phased to be processed.
     * @param siteCount     The number of sites to be processed.
     * @param sitePairCount The number of site pairs to be processed.
     * @param mbd           The Matrix Block Information object.
     */
    public void setInitialStatus(String srcType, String filPath, int phaseCount,
                                 int sourceCount, int siteCount,
                                 int sitePairCount, MatrixBlockDefinition mbd) {
        // set information and exit

        setVisible(true);
        aInitStartTime = (new Date()).getTime();
        aInitLabels[0].setText(Globals.getTimeStamp(aInitStartTime));
        aInitLabels[3].setText(" " + srcType + " ");
        aInitLabels[4].setText(" " + filPath + " ");
        aInitLabels[5].setText(" " + phaseCount + " ");
        aInitLabels[6].setText(" " + sourceCount + " ");
        aInitLabels[7].setText(" " + siteCount + " ");
        aInitLabels[8].setText(" " + sitePairCount + " ");
        aInitLabels[9].setText(" " + mbd.size() + " x " + mbd.size() + " ");
        aInitLabels[10].setText(" " + mbd.blocks() + " x " + mbd.blocks() + " ");
        aInitLabels[11].setText(" " + mbd.blockSize() + " x " +
                mbd.blockSize() + " ");
        aInitLabels[12].setText(" " + mbd.symmMatrixBlockCount() +
                " {Nb(Nb+1)/2} ");
    }

    /**
     * Updates the initial status panel with the current solution phase.
     *
     * @param stg The current solution phase.
     */
    public void updateInitialStatus(RayUncertainty.SolutionPhase stg) {
        // set the current and elapsed time.

        long currTime = (new Date()).getTime();
        aInitLabels[1].setText(Globals.getTimeStamp(currTime));
        aInitLabels[2].setText(Globals.elapsedTimeString2(aInitStartTime,
                currTime));

        // set progress bar colors for the stage progress bar

        Color c0r = new Color(255, 224, 224); // red
        Color c1r = new Color(128, 96, 96);
        Color c0g = new Color(224, 255, 224); // green
        Color c1g = new Color(96, 128, 96);
        Color c0b = new Color(224, 224, 255); // blue
        Color c1b = new Color(96, 96, 128);

        // Set the stage progress bar based on the input stage. If the input stage
        // is null then all are complete.

        if (stg == RayUncertainty.SolutionPhase.PREDICTION) {
            // in predicition stage ... set as processing and downstream stages as
            // queued.

            aInitProgBar.getRectangle(0).setFillPaint(0.5, 0.05, c0r, 0.5, 0.5,
                    c1r, true);
            aInitProgBar.setText(0, String.format("  Ray Prediction: PROCESSING  ",
                    aInitProgValue[0]));
            aInitProgBar.getRectangle(1).setFillPaint(0.5, 0.05, c0b, 0.5, 0.5,
                    c1b, true);
            aInitProgBar.setText(1, String.format("  Ray Uncertainty: QUEUED  ",
                    aInitProgValue[1]));
        } else if (stg == RayUncertainty.SolutionPhase.RAY_UNCERTAINTY) {
            // in ray uncertainty stage ... set as processing and previous stages as
            // complete

            aInitProgBar.getRectangle(0).setFillPaint(0.5, 0.05, c0g, 0.5, 0.5,
                    c1g, true);
            aInitProgBar.setText(0, String.format("  Ray Prediction: COMPLETED  ",
                    aInitProgValue[0]));
            aInitProgBar.getRectangle(1).setFillPaint(0.5, 0.05, c0r, 0.5, 0.5,
                    c1r, true);
            aInitProgBar.setText(1, String.format("  Ray Uncertainty: PROCESSING  ",
                    aInitProgValue[1]));
        } else if (stg == null) {
            // null ... set all stages as complete

            aInitProgBar.getRectangle(0).setFillPaint(0.5, 0.05, c0g, 0.5, 0.5,
                    c1g, true);
            aInitProgBar.setText(0, String.format("  Ray Prediction: COMPLETED  ",
                    aInitProgValue[0]));
            aInitProgBar.getRectangle(1).setFillPaint(0.5, 0.05, c0g, 0.5, 0.5,
                    c1g, true);
            aInitProgBar.setText(1, String.format("  Ray Uncertainty: COMPLETED  ",
                    aInitProgValue[1]));
        }

        // repaint progress bar and exit

        aInitProgBar.repaint();
    }

    /**
     * Lays out the initialization status panel to the following specification:
     * <p>
     * Start Time                                [lbl0]
     * Current Time                              [lbl1]
     * Elapsed Time                              [lbl2]
     * <p>
     * Source Definition: Database or GeoModel   [lbl3]
     * Source Path: "file"                       [lbl4]
     * <p>
     * Phase Count:                              [lbl5]
     * Receiver Count:                           [lbl6]
     * Receiver Pair Count (Site Covariance):    [lbl7]
     * <p>
     * Covariance Matrix:
     * Size:                                 [lbl8]
     * Blocks:                               [lbl9]
     * Block Size:                           [lbl10]
     * Total Blocks:                         [lbl11]
     * <p>
     * Process Status: [--- Queued: Ray Prediction ---|
     * --- Queued: Ray Uncertainty ---] [pb]
     *
     * @param initStatPnl The UI initialization status panel.
     * @param stage       The set of solution phases that will be solved for.
     */
    private void buildInitialStatusPanel(JPanel initStatPnl,
                                         RayUncertainty.SolutionPhase[] stage) {
        JPanel pnl0, pnl1, pnl2;
        JLabel lbl;

        // create and initialize labels

        aInitLabels = new JLabel[13];
        for (int i = 0; i < aInitLabels.length; ++i) {
            lbl = new JLabel("  0  ", SwingConstants.LEFT);
            aInitLabels[i] = lbl;
        }

        // set solution phase progress bar values equal

        for (int i = 0; i < stage.length; ++i) {
            if (stage[i] == RayUncertainty.SolutionPhase.PREDICTION)
                aInitProgValue[0] = 1;
            else if (stage[i] == RayUncertainty.SolutionPhase.RAY_UNCERTAINTY)
                aInitProgValue[1] = 1;
        }

        // build initial status panel

        pnl0 = new JPanel();
        initStatPnl.add(pnl0);
        pnl0.setLayout(new BoxLayout(pnl0, BoxLayout.X_AXIS));
        pnl1 = new JPanel();
        pnl0.add(pnl1);
        pnl1.setLayout(new GridLayout(21, 1, 2, 2));
        pnl2 = new JPanel();
        pnl0.add(pnl2);
        pnl2.setLayout(new GridLayout(21, 1, 2, 2));

        // add left side labels

        pnl1.add(new JPanel());
        pnl1.add(new JPanel());
        pnl1.add(new JLabel("      Start Time  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Current Time  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Elapsed Time  ", SwingConstants.LEFT));
        pnl1.add(new JPanel());
        pnl1.add(new JLabel("      Source Definition  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Source Path  ", SwingConstants.LEFT));
        pnl1.add(new JPanel());
        pnl1.add(new JLabel("      Phase Count  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Source Count  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Receiver Count  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Receiver Pair Count  ", SwingConstants.LEFT));
        pnl1.add(new JPanel());
        pnl1.add(new JLabel("      Covariance Matrix:  "));
        pnl1.add(new JLabel("         Size  "));
        pnl1.add(new JLabel("         Blocks  "));
        pnl1.add(new JLabel("         Block Size  "));
        pnl1.add(new JLabel("         Total Blocks  "));
        pnl1.add(new JPanel());
        pnl1.add(new JPanel());

        // add right side value labels

        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(aInitLabels[0]);
        pnl2.add(aInitLabels[1]);
        pnl2.add(aInitLabels[2]);
        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(aInitLabels[3]);
        pnl2.add(aInitLabels[4]);
        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(aInitLabels[5]);
        pnl2.add(aInitLabels[6]);
        pnl2.add(aInitLabels[7]);
        pnl2.add(aInitLabels[8]);
        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(aInitLabels[9]);
        pnl2.add(aInitLabels[10]);
        pnl2.add(aInitLabels[11]);
        pnl2.add(aInitLabels[12]);
        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(new JLabel("", SwingConstants.LEFT));

        // set solution phase progress bar

        pnl0 = new JPanel();
        initStatPnl.add(pnl0);
        pnl0.setLayout(new BoxLayout(pnl0, BoxLayout.X_AXIS));
        pnl0.add(new JLabel("    Process Status: ", SwingConstants.LEFT));
        MultiValueProgressBar mvpb = new MultiValueProgressBar();
        aInitProgBar = mvpb;
        pnl0.add(aInitProgBar);
        pnl0.add(new JLabel("  ", SwingConstants.LEFT));
        pnl0 = new JPanel();
        initStatPnl.add(pnl0);

        mvpb.setPreferredSize(new Dimension(768, 24));
        mvpb.setBarCount(2);
        mvpb.setUseFullPanelDisplay(true);
        mvpb.setProgress(1.0, 1.0);
        setInitProgressBar(mvpb);
    }

    /**
     * Initialize ray prediction panel.
     *
     * @param tptsks Total number of tasks to process.
     */
    public void setTotalRayPredictionTasks(int tptsks) {
        // set total tasks

        aTotalPredTasks = tptsks;
        aPredLabels[1].setText(tptsks + " ");

        // set start time

        aPredStartTime = (new Date()).getTime();
        aPredLastCurrTime = aPredStartTime;
        aPredLabels[2].setText(Globals.getTimeStamp(aPredStartTime));
    }

    /**
     * Update the ray weight sets and blocks created and written.
     *
     * @param rayWeightSetsToWrite   Number of ray weight sets created.
     * @param rayWeightBlocksToWrite Number of ray weight blocks created.
     * @param rayWeightSetsWritten   Number of ray weight sets written.
     * @param rayWeightBlocksWritten Number of ray weight blocks written.
     */
    public void updateRayWeightFilesWritten(int rayWeightSetsToWrite,
                                            int rayWeightBlocksToWrite,
                                            int rayWeightSetsWritten,
                                            int rayWeightBlocksWritten) {
        // output ray weight file progress

        aPredLabels[21].setText(rayWeightSetsToWrite + " ");
        aPredLabels[22].setText(rayWeightBlocksToWrite + " ");
        aPredLabels[23].setText(rayWeightSetsWritten + " ");
        aPredLabels[24].setText(rayWeightBlocksWritten + " ");
        aPredLabels[21].repaint();
        aPredLabels[22].repaint();
        aPredLabels[23].repaint();
        aPredLabels[24].repaint();
    }

    /**
     * Update the ray prediction panel.
     *
     * @param nodeTime        The total processor node calculation time.
     * @param procCount       The total number of processors at the time of this call.
     * @param rayPredCount    The total number of ray predictions performed.
     * @param siteCount       The total number of sites processed.
     * @param rayCount        The total number of rays processed.
     * @param rayElementCount The total number of ray elements evaluated.
     * @param minRayElemCount The minimum ray elements per ray thus far.
     * @param maxRayElemCount The maximum ray elements per ray thus far.
     */
    public void updateRayPrediction(long nodeTime, int procCount,
                                    int rayPredCount, int siteCount, int rayCount,
                                    long rayElementCount, int minRayElemCount,
                                    int maxRayElemCount) {
        // increment task count

        ++aPredTasks;
        aPredLabels[0].setText(aPredTasks + " ");

        // set progress bar

        aPredProgBar.setProgress((double) aPredTasks,
                (double) (aTotalPredTasks - aPredTasks));
        aPredProgBar.setText(0, String.format("  Completed: %.2f  ",
                100.0 * aPredTasks / aTotalPredTasks));
        aPredProgBar.setText(1, String.format("  Remaining: %.2f  ",
                100.0 * (aTotalPredTasks - aPredTasks) /
                        aTotalPredTasks));
        aPredProgBar.repaint();

        // set current and elapsed time

        long currTime = (new Date()).getTime();
        long elpsdTime = currTime - aPredStartTime;
        aPredLabels[3].setText(Globals.getTimeStamp(currTime));
        aPredLabels[4].setText(Globals.elapsedTimeString2(aPredStartTime,
                currTime));

        // determine total processor node calculation time

        aPredNodeTime += nodeTime;
        aPredLabels[5].setText(Globals.elapsedTimeString2(0, aPredNodeTime));

        // calculate mean processor count (average over time)

        if (currTime > aPredLastCurrTime) {
            long dtime = currTime - aPredLastCurrTime;
            aPredProcCountTime += procCount * dtime;
            aPredLastCurrTime = currTime;
        }

        // output performance

        double meanProcCount = (double) aPredProcCountTime / elpsdTime;
        aPredLabels[6].setText(String.format("%.2f  ", meanProcCount));
        double acclRatio = (double) aPredNodeTime / elpsdTime;
        aPredLabels[7].setText(String.format("%.2f  ", acclRatio));
        double eff = 100.0 * acclRatio / meanProcCount;
        aPredLabels[8].setText(String.format("%.2f  ", eff));

        // output mean ray calculation time

        aPredLabels[9].setText(rayPredCount + "  ");
        if (rayPredCount > 0)
            aPredLabels[10].setText(Globals.timeStringAbbrvUnits(
                    (double) aPredNodeTime / rayPredCount));
        else
            aPredLabels[10].setText("----");

        // output site, ray set, ray, and ray element counts

        aPredLabels[11].setText(siteCount + " ");
        aPredLabels[13].setText(rayCount + " ");
        aPredLabels[14].setText(rayElementCount + " ");

        // output ray set, ray, and ray element counts per site

        if (siteCount > 0) {
            aPredLabels[15].setText(String.format("%.2f  ",
                    (double) rayElementCount / siteCount));
            aPredLabels[20].setText(String.format("%.2f  ",
                    (double) rayCount / siteCount));
        } else {
            aPredLabels[15].setText("----");
            aPredLabels[20].setText("----");
        }

        // output ray element count per rays processed.

        if (rayCount > 0)
            aPredLabels[17].setText(String.format("%.2f  ",
                    (double) rayElementCount / rayCount));
        else
            aPredLabels[17].setText("----");

        // set min and max ray element count and exit

        aPredLabels[18].setText(maxRayElemCount + " ");
        aPredLabels[19].setText(minRayElemCount + " ");
    }

    /**
     * Lays out the Ray Prediction panel to the following specification:
     * <p>
     * # Tasks [lbl0] Processed of Total Tasks [lbl1]
     * [------------------------------------------------------------] sbpred
     * <p>
     * Start time                            lbl2
     * Current(End) time                     lbl3
     * elapsed time                          lbl4
     * total node ray prediction time        lbl5
     * mean processor count                  lbl6
     * acceleration ratio                    lbl7
     * parallel efficiency                   lbl8
     * <p>
     * rays predicted                        lbl9
     * mean ray calculation time             lbl10
     * <p>
     * total sites                           lbl11
     * total rays                            lbl13
     * total ray elements                    lbl14
     * mean ray elements per site            lbl15
     * mean ray elements per ray             lbl17
     * max ray elements per ray              lbl18
     * min ray elements per ray              lbl19
     * mean rays per site                    lbl20
     * <p>
     * Total Block Array Sets To Write       lbl21
     * Total Blocks To Write                 lbl22
     * Total Block Array Sets Written        lbl23
     * Total Blocks Written                  lbl24
     *
     * @param predPnl The ray prediction UI panel.
     */
    private void buildPredictionPanel(JPanel predPnl) {
        JPanel pnl0, pnl1, pnl2;
        JLabel lbl;

        // create prediction panel container and add to predPnl

        JPanel mainPnl = new JPanel();
        mainPnl.setLayout(new BoxLayout(mainPnl, BoxLayout.Y_AXIS));
        predPnl.add(mainPnl);

        // create and initialize ray prediction labels.

        aPredLabels = new JLabel[25];
        for (int i = 0; i < aPredLabels.length; ++i) {
            lbl = new JLabel("  0  ", SwingConstants.LEFT);
            aPredLabels[i] = lbl;
        }

        // create ray prediction panel layout

        pnl0 = new JPanel();
        mainPnl.add(pnl0);
        pnl0.setLayout(new BoxLayout(pnl0, BoxLayout.Y_AXIS));
        pnl1 = new JPanel();
        pnl1.setLayout(new BoxLayout(pnl1, BoxLayout.X_AXIS));
        pnl0.add(pnl1);

        // create progress bar labels

        pnl1.add(new JLabel("              Completed ", SwingConstants.LEFT));
        pnl1.add(aPredLabels[0]);
        pnl1.add(new JLabel(" Tasks of ", SwingConstants.LEFT));
        pnl1.add(aPredLabels[1]);
        pnl1 = new JPanel();
        pnl1.setLayout(new BoxLayout(pnl1, BoxLayout.X_AXIS));
        pnl0.add(pnl1);
        pnl1.add(new JLabel("    Progess: ", SwingConstants.LEFT));

        // create progress bar

        MultiValueProgressBar mvpb = new MultiValueProgressBar();
        aPredProgBar = mvpb;
        pnl1.add(aPredProgBar);
        pnl1.add(new JLabel("  ", SwingConstants.LEFT));

        mvpb.setPreferredSize(new Dimension(768, 24));
        mvpb.setBarCount(2);
        mvpb.setUseFullPanelDisplay(true);
        mvpb.setProgress(0.0, 1.0);
        setProgressBar(mvpb);

        // create label layout

        pnl0 = new JPanel();
        mainPnl.add(pnl0);
        pnl0.setLayout(new BoxLayout(pnl0, BoxLayout.X_AXIS));
        pnl1 = new JPanel();
        pnl0.add(pnl1);
        pnl1.setLayout(new GridLayout(26, 1, 2, 2));
        pnl2 = new JPanel();
        pnl0.add(pnl2);
        pnl2.setLayout(new GridLayout(26, 1, 2, 2));

        // add left side description labels.

        pnl1.add(new JPanel());
        pnl1.add(new JLabel("      Start Time  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Current Time  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Elapsed Time  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Total Node Ray Prediction Task Time  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Mean Processor Count  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Parallel Acceleration Level  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Parallel Efficiency  ", SwingConstants.LEFT));
        pnl1.add(new JPanel());
        pnl1.add(new JLabel("      Total Rays Predicted  "));
        pnl1.add(new JLabel("      Mean Ray Prediction Time  "));
        pnl1.add(new JPanel());
        pnl1.add(new JLabel("      Total Phase/Sites Evaluated  "));
        pnl1.add(new JLabel("      Total Valid Rays Processed  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Total Ray Elements Processed  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Mean Ray Elements per Phase/Site  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Mean Ray Elements per Ray  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Max Ray Elements per Ray  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Min Ray Elements per Ray  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Mean Rays per Phase/Site  ",
                SwingConstants.LEFT));
        pnl1.add(new JPanel());
        pnl1.add(new JLabel("      Total Ray Weight Sets Created ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Total Ray Weight Blocks Created ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Total Ray Weight Sets Written ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Total Ray Weight Blocks Written ",
                SwingConstants.LEFT));
        pnl1.add(new JPanel());

        // add right side result labels

        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(aPredLabels[2]);
        pnl2.add(aPredLabels[3]);
        pnl2.add(aPredLabels[4]);
        pnl2.add(aPredLabels[5]);
        pnl2.add(aPredLabels[6]);
        pnl2.add(aPredLabels[7]);
        pnl2.add(aPredLabels[8]);
        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(aPredLabels[9]);
        pnl2.add(aPredLabels[10]);
        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(aPredLabels[11]);
        pnl2.add(aPredLabels[13]);
        pnl2.add(aPredLabels[14]);
        pnl2.add(aPredLabels[15]);
        pnl2.add(aPredLabels[17]);
        pnl2.add(aPredLabels[18]);
        pnl2.add(aPredLabels[19]);
        pnl2.add(aPredLabels[20]);
        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(aPredLabels[21]);
        pnl2.add(aPredLabels[22]);
        pnl2.add(aPredLabels[23]);
        pnl2.add(aPredLabels[24]);
    }

    /**
     * Set up and initialize the input progress bar (standard progress bar).
     *
     * @param mvpb The progress bar to be initialized.
     */
    public void setProgressBar(MultiValueProgressBar mvpb) {
        // make color

        Color c0 = new Color(224, 255, 224); // green
        Color c1 = new Color(96, 128, 96);

        // create each progress bar rectangle for the number of available solution
        // types

        mvpb.getRectangle(0).setFillPaint(0.5, 0.05, c0, 0.5, 0.5, c1, true);
        mvpb.getRectangle(0).getLabel().setForeground(Color.white);
        mvpb.getRectangle(0).getLabel().setText("Completed");

        // create the "Remaining" rectangle

        mvpb.getRectangle(1).setFillPaint(0.5, 0.05, new Color(192, 192, 192),
                0.5, 0.5, new Color(64, 64, 64), true);
        mvpb.getRectangle(1).getLabel().setForeground(Color.white);
        mvpb.getRectangle(1).getLabel().setText("Remaining");
    }

    /**
     * Set up the initial panel status progress bar.
     *
     * @param mvpb The progress bar to be initialized.
     */
    public void setInitProgressBar(MultiValueProgressBar mvpb) {
        // make color

        Color c0 = new Color(224, 224, 255); // blue
        Color c1 = new Color(96, 96, 128);

        // create each progress bar rectangle for the number of available solution
        // types

        mvpb.getRectangle(0).setFillPaint(0.5, 0.05, c0, 0.5, 0.5, c1, true);
        mvpb.getRectangle(0).getLabel().setForeground(Color.white);
        mvpb.getRectangle(0).getLabel().setText("  Ray Prediction: QUEUED  ");

        mvpb.getRectangle(1).setFillPaint(0.5, 0.05, c0, 0.5, 0.5, c1, true);
        mvpb.getRectangle(1).getLabel().setForeground(Color.white);
        mvpb.getRectangle(1).getLabel().setText("  Ray Uncertainty: QUEUED  ");
    }

    /**
     * Set the total number of ray uncertainty tasks.
     *
     * @param tptsks The total number of ray uncertainty tasks.
     */
    public void setTotalRayUncertaintyTasks(int tptsks) {
        // set the total number of tasks.

        aTotalRayUncTasks = tptsks;
        aRayUncLabels[1].setText(tptsks + " ");

        // set the initial start time

        aRayUncStartTime = (new Date()).getTime();
        aRayUncLastCurrTime = aRayUncStartTime;
        aRayUncLabels[2].setText(Globals.getTimeStamp(aRayUncStartTime));
    }

    /**
     * Update the ray uncertainty UI panel.
     *
     * @param nodeTime        The total processor node ray uncertainty calculation time.
     * @param procCount       The current number of processor nodes.
     * @param proctime        The total ray uncertainty algorithm time.
     * @param gcProcTime      The total garbage collection time.
     * @param raysetreadtime  The total ray set block map read time.
     * @param raysetreadcount The total number of ray set block maps read.
     * @param blkreadtime     The total ray uncertainty covariance block read time.
     * @param ovrhdtime       The total ray uncertainty overhead time.
     */
    public void updateRayUncertainty(long nodeTime, int procCount, long proctime,
                                     long gcProcTime, long raysetreadtime,
                                     int raysetreadcount, long blkreadtime,
                                     long ovrhdtime) {
        // Update the current process task

        ++aRayUncTasks;
        aRayUncLabels[0].setText(aRayUncTasks + " ");

        // update the progress bar

        aRayUncProgBar.setProgress((double) aRayUncTasks,
                (double) (aTotalRayUncTasks - aRayUncTasks));
        aRayUncProgBar.setText(0, String.format("  Completed: %.2f  ",
                100.0 * aRayUncTasks / aTotalRayUncTasks));
        aRayUncProgBar.setText(1, String.format("  Remaining: %.2f  ",
                100.0 * (aTotalRayUncTasks - aRayUncTasks) /
                        aTotalRayUncTasks));
        aRayUncProgBar.repaint();

        // update the current and elapsed time

        long currTime = (new Date()).getTime();
        long elpsdTime = currTime - aRayUncStartTime;
        aRayUncLabels[3].setText(Globals.getTimeStamp(currTime));
        aRayUncLabels[4].setText(Globals.elapsedTimeString2(aRayUncStartTime,
                currTime));

        // update the total processor node calculation time

        aRayUncLabels[5].setText(Globals.elapsedTimeString2(0, nodeTime));

        // calculate the mean processor count (time averaged)

        if (currTime > aRayUncLastCurrTime) {
            long dtime = currTime - aRayUncLastCurrTime;
            aRayUncProcCountTime += procCount * dtime;
            aRayUncLastCurrTime = currTime;
        }

        // set performance updates

        double meanProcCount = (double) aRayUncProcCountTime / elpsdTime;
        aRayUncLabels[6].setText(String.format("%.2f  ", meanProcCount));
        double acclRatio = (double) nodeTime / elpsdTime;
        aRayUncLabels[7].setText(String.format("%.2f  ", acclRatio));
        double eff = 100.0 * acclRatio / meanProcCount;
        aRayUncLabels[8].setText(String.format("%.2f  ", eff));

        // wet task work fraction breakdown

        aRayUncWorkFracs[0] = (double) raysetreadtime / aRayUncTasks;
        aRayUncWorkFracs[1] = (double) blkreadtime / aRayUncTasks;
        aRayUncWorkFracs[2] = (double) proctime / aRayUncTasks;
        aRayUncWorkFracs[3] = (double) ovrhdtime / aRayUncTasks;

        // update work fraction progress bar

        aRayUncWork.setProgress(aRayUncWorkFracs);
        double sum = aRayUncWork.getSum();
        for (int i = 0; i < aRayUncWorkFracs.length; ++i)
            aRayUncWork.setText(i, String.format("%.2f  ",
                    100.0 * aRayUncWorkFracs[i] / sum));
        aRayUncWork.repaint();

        // update label information

        aRayUncLabels[9].setText(Globals.
                timeStringAbbrvUnits((double) nodeTime / aRayUncTasks));
        aRayUncLabels[10].setText(Globals.
                timeStringAbbrvUnits(aRayUncWorkFracs[2]) +
                String.format("  (%.2f",
                        100.0 * aRayUncWorkFracs[2] / sum) + " %)");
        aRayUncLabels[11].setText(Globals.
                timeStringAbbrvUnits(aRayUncWorkFracs[0]) +
                String.format("  (%.2f",
                        100.0 * aRayUncWorkFracs[0] / sum) + " %)");
        aRayUncLabels[12].setText(String.format("%.2f  ",
                (double) raysetreadcount / aRayUncTasks));
        aRayUncLabels[13].setText(Globals.
                timeStringAbbrvUnits((double) raysetreadtime /
                        raysetreadcount));
        aRayUncLabels[14].setText(Globals.
                timeStringAbbrvUnits(aRayUncWorkFracs[1]) +
                String.format("  (%.2f",
                        100.0 * aRayUncWorkFracs[1] / sum) + " %)");
        aRayUncLabels[15].setText(Globals.
                timeStringAbbrvUnits(aRayUncWorkFracs[3]) +
                String.format("  (%.2f",
                        100.0 * aRayUncWorkFracs[3] / sum) + " %)");

        // repaint labels and exit

        for (int i = 0; i < aRayUncLabels.length; ++i) aRayUncLabels[i].repaint();
    }

    /**
     * Lays out the Block Map panel to the following specification:
     * <p>
     * Processing Task [lbl0] of [lbl1]
     * Progress: [-----------------------------------------------------------]
     * [pb1]
     * <p>
     * Start time                                                    [lbl2]
     * Current time                                                  [lbl3]
     * Elapsed time                                                  [lbl4]
     * Total node block map determination time                       [lbl5]
     * Mean processor count                                          [lbl6]
     * Acceleration ratio                                            [lbl7]
     * Parallel efficiency                                           [lbl8]
     * <p>
     * [--Ray Set Read Time--|--Block Read Time--|
     * --Algorithm--|--Overhead Time--] [pb2]
     * Task Time Fractions: [-------------------|-----------------------|
     * ----|--------------|---------] [pb3]
     * <p>
     * Mean task total time                                          [lbl9]
     * Mean task process time                                        [lbl10]
     * Mean task ray set read time                                   [lbl11]
     * Mean task ray set read count                                  [lbl12]
     * Mean ray set read time                                        [lbl13]
     * Mean task block read time                                     [lbl14]
     * Mean task overhead time                                       [lbl15]
     *
     * @param rayUncPnl
     */
    private void buildRayUncertaintyPanel(JPanel rayUncPnl) {
        JPanel pnl0, pnl1, pnl2;
        JLabel lbl;

        // create ray uncertainty panel container and initialize

        aRayUncLabels = new JLabel[16];
        for (int i = 0; i < aRayUncLabels.length; ++i) {
            lbl = new JLabel("  0  ", SwingConstants.LEFT);
            aRayUncLabels[i] = lbl;
        }

        // create ray uncertainty panel container and add to predPnl

        JPanel mainPnl = new JPanel();
        mainPnl.setLayout(new BoxLayout(mainPnl, BoxLayout.Y_AXIS));
        rayUncPnl.add(mainPnl);

        // set main panel layout

        pnl0 = new JPanel();
        mainPnl.add(pnl0);
        pnl0.setLayout(new BoxLayout(pnl0, BoxLayout.Y_AXIS));
        pnl1 = new JPanel();
        pnl1.setLayout(new BoxLayout(pnl1, BoxLayout.X_AXIS));
        pnl0.add(pnl1);

        // set progress bar labels

        pnl1.add(new JLabel("              Completed ", SwingConstants.LEFT));
        pnl1.add(aRayUncLabels[0]);
        pnl1.add(new JLabel(" Tasks of ", SwingConstants.LEFT));
        pnl1.add(aRayUncLabels[1]);
        pnl1 = new JPanel();
        pnl1.setLayout(new BoxLayout(pnl1, BoxLayout.X_AXIS));
        pnl0.add(pnl1);
        pnl1.add(new JLabel("    Progess: ", SwingConstants.LEFT));

        // set progress bar

        MultiValueProgressBar mvpb = new MultiValueProgressBar();
        aRayUncProgBar = mvpb;
        pnl1.add(aRayUncProgBar);
        pnl1.add(new JLabel("  ", SwingConstants.LEFT));

        mvpb.setPreferredSize(new Dimension(768, 24));
        mvpb.setBarCount(2);
        mvpb.setUseFullPanelDisplay(true);
        mvpb.setProgress(0.0, 1.0);
        setProgressBar(mvpb);

        // add start of status information

        pnl0 = new JPanel();
        mainPnl.add(pnl0);
        pnl0.setLayout(new BoxLayout(pnl0, BoxLayout.X_AXIS));
        pnl1 = new JPanel();
        pnl0.add(pnl1);
        pnl1.setLayout(new GridLayout(9, 1, 2, 2));
        pnl2 = new JPanel();
        pnl0.add(pnl2);
        pnl2.setLayout(new GridLayout(9, 1, 2, 2));

        // add left side description labels

        pnl1.add(new JPanel());
        pnl1.add(new JLabel("      Start Time  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Current Time  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Elapsed Time  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Total Node Ray Uncertainty Task Time  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Mean Processor Count  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Parallel Acceleration Level  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Parallel Efficiency  ", SwingConstants.LEFT));
        pnl1.add(new JPanel());

        // add right side results labels

        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(aRayUncLabels[2]);
        pnl2.add(aRayUncLabels[3]);
        pnl2.add(aRayUncLabels[4]);
        pnl2.add(aRayUncLabels[5]);
        pnl2.add(aRayUncLabels[6]);
        pnl2.add(aRayUncLabels[7]);
        pnl2.add(aRayUncLabels[8]);
        pnl2.add(new JLabel("", SwingConstants.LEFT));

        // add work fraction progress bars here

        // *                          [--Ray Set Read Time--|--Block Read Time--|
        // *                           --Algorithm--|--Overhead Time--] [pb2]
        // *     Task Time Fractions: [----------------------------|
        // *                           --------------------------------|----|
        // *                           ------] [pb3]

        // add process progress bar

        mainPnl.add(new JLabel("", SwingConstants.CENTER));
        JPanel pnlProcBar = new JPanel();
        mainPnl.add(pnlProcBar);

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();

        pnlProcBar.setLayout(gbl);

        gc.gridwidth = 1;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.gridheight = 1;
        gc.insets = new Insets(2, 2, 2, 2);

        // add "Work Split" header MultiValueProgressBar in row 10 col 2

        gc.gridy = 0;
        gc.gridx = 1;
        gc.gridwidth = 1;
        mvpb = new MultiValueProgressBar();
        mvpb.setPreferredSize(new Dimension(768, 24));
        mvpb.setBarCount(4);
        mvpb.setUseFullPanelDisplay(true);
        double[] v = new double[4];
        v[0] = v[1] = v[2] = v[3] = 1.0 / 4.0;
        mvpb.setProgress(v);
        setProcWorkRayUncPaint(mvpb, null);
        gbl.setConstraints(mvpb, gc);
        pnlProcBar.add(mvpb);

        // add black spacer panel across col 3 cell

        gc.gridy = 1;
        gc.gridx = 1;
        gc.gridwidth = 1;
        pnl0 = new JPanel();
        pnl0.setPreferredSize(new Dimension(768, 2));
        pnl0.setBackground(Color.black);
        gbl.setConstraints(pnl0, gc);
        pnlProcBar.add(pnl0);

        // add "Work Split" labels and progress bar

        gc.gridy = 2;
        gc.gridx = 0;
        lbl = new JLabel("      Per Task Time Split  ", SwingConstants.LEFT);
        gbl.setConstraints(lbl, gc);
        pnlProcBar.add(lbl);

        gc.gridx = 1;
        mvpb = new MultiValueProgressBar();
        aRayUncWork = mvpb;
        mvpb.setPreferredSize(new Dimension(768, 24));
        mvpb.setBarCount(4);
        mvpb.setUseFullPanelDisplay(true);
        v[0] = v[1] = v[2] = 0.0;
        v[3] = 100.0;
        mvpb.setProgress(v);
        setProcWorkRayUncPaint(mvpb, v);
        gbl.setConstraints(mvpb, gc);
        pnlProcBar.add(mvpb);

        // add spacer panel across all 3 grid cells

        gc.gridy = 3;
        gc.gridx = 0;
        gc.gridwidth = 2;
        pnl0 = new JPanel();
        pnl0.setPreferredSize(new Dimension(1, 12));
        gbl.setConstraints(pnl0, gc);
        pnlProcBar.add(pnl0);

        // add final label output

        pnl0 = new JPanel();
        mainPnl.add(pnl0);
        pnl0.setLayout(new BoxLayout(pnl0, BoxLayout.X_AXIS));
        pnl1 = new JPanel();
        pnl0.add(pnl1);
        pnl1.setLayout(new GridLayout(24, 1, 2, 2));
        pnl2 = new JPanel();
        pnl0.add(pnl2);
        pnl2.setLayout(new GridLayout(24, 1, 2, 2));

        // add left side description labels

        pnl1.add(new JPanel());
        pnl1.add(new JLabel("      Mean Task Total Time  ", SwingConstants.LEFT));
        pnl1.add(new JLabel("      Mean Task Algorithm Time  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Mean Task Block Ray Weight Read Time  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Mean Task Block Ray Weight Read Count  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Mean Block Ray Weight Read Time  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Mean Task Cov. Block Read Time  ",
                SwingConstants.LEFT));
        pnl1.add(new JLabel("      Mean Task Overhead Time  ",
                SwingConstants.LEFT));
        pnl1.add(new JPanel());

        // add right side results labels

        pnl2.add(new JLabel("", SwingConstants.LEFT));
        pnl2.add(aRayUncLabels[9]);
        pnl2.add(aRayUncLabels[10]);
        pnl2.add(aRayUncLabels[11]);
        pnl2.add(aRayUncLabels[12]);
        pnl2.add(aRayUncLabels[13]);
        pnl2.add(aRayUncLabels[14]);
        pnl2.add(aRayUncLabels[15]);
        pnl2.add(new JLabel("", SwingConstants.LEFT));
    }

    /**
     * Defines the process work progress bar layout.
     *
     * @param mvpb The input process work progress bar to be modified.
     * @param v    The array of values for the individual progress bar components.
     */
    private void setProcWorkRayUncPaint(MultiValueProgressBar mvpb, double[] v) {
        // define colors for each rectangle

        Color read1C0 = new Color(128, 128, 255); // blue
        Color read1C1 = new Color(64, 64, 128);
        Color read2C0 = new Color(128, 255, 255); // cyan
        Color read2C1 = new Color(64, 128, 128);
        Color processC0 = new Color(128, 255, 128); // green
        Color processC1 = new Color(64, 128, 64);
        Color ovrhdC0 = new Color(255, 128, 255); // magenta
        Color ovrhdC1 = new Color(128, 64, 128);

        // set the individual rectangle fill and label foreground colors

        mvpb.getRectangle(0).setFillPaint(0.5, 0.05, read1C0,
                0.5, 0.5, read1C1, true);
        mvpb.getRectangle(0).getLabel().setForeground(Color.white);
        mvpb.getRectangle(1).setFillPaint(0.5, 0.05, read2C0,
                0.5, 0.5, read2C1, true);
        mvpb.getRectangle(1).getLabel().setForeground(Color.white);
        mvpb.getRectangle(2).setFillPaint(0.5, 0.05, processC0,
                0.5, 0.5, processC1, true);
        mvpb.getRectangle(2).getLabel().setForeground(Color.white);
        mvpb.getRectangle(3).setFillPaint(0.5, 0.05, ovrhdC0,
                0.5, 0.5, ovrhdC1, true);
        mvpb.getRectangle(3).getLabel().setForeground(Color.white);

        // set the individual rectangle label strings.

        if (v == null) {
            mvpb.getRectangle(0).getLabel().setText("Read Block Ray Weights");
            mvpb.getRectangle(1).getLabel().setText("Read COV Blocks");
            mvpb.getRectangle(2).getLabel().setText("Algorithm");
            mvpb.getRectangle(3).getLabel().setText("Overhead");
        } else {
            double sum = v[0] + v[1] + v[2] + v[3];
            mvpb.getRectangle(0).getLabel().setText(String.format("%.2f",
                    100.0 * v[0] / sum));
            mvpb.getRectangle(1).getLabel().setText(String.format("%.2f",
                    100.0 * v[1] / sum));
            mvpb.getRectangle(2).getLabel().setText(String.format("%.2f",
                    100.0 * v[2] / sum));
            mvpb.getRectangle(3).getLabel().setText(String.format("%.2f",
                    100.0 * v[3] / sum));
        }
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // TODO Auto-generated method stub
    }

    /**
     * Closes the GUI and exits.
     */
    @Override
    public void windowClosing(WindowEvent e) {
        System.out.println("RayUncertaintyUI Exiting ...");
        System.exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub
    }
}
