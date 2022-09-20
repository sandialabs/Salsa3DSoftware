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
package gov.sandia.gmp.util.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * The MultiValueProgressBar allows simultaneous viewing of a tasks components
 * as a multi-level progress bar or stepped histogram. The bar is composed of one
 * or more internal bars, or rectangles. If only one internal rectangle is
 * is represented then this object is like a traditional progress bar. If more
 * than one internal rectangles are represented the progress bar can be used as
 * a stepped histogram or as a percentage of 
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class MultiValueProgressBar extends JPanel
{
  /**
   * Internal object used to represent each of the internal Rectangles. The
   * internal rectangles have their own borders, border color, fill colors (or
   * paints) and a contained label.
   * 
   * @author jrhipp
   *
   */
  public class Rectangle3D extends JPanel
  {
    /**
     * The raised border flag. If false the border is inset.
     */
    private boolean aRaisedBorder    = true;

    /**
     * The contained label. This label is null until first requested or set.
     */
    private JLabel  aLabel           = null;

    /**
     * The rectangle border color. All rectangle border are 1 pixel thick.
     */
    private Color   aBorderColor     = Color.GRAY;

    /**
     * The rectangle fill color.
     */
    private Color   aFillColor       = Color.RED;

    /**
     * The rectangle fill flag. If true paint is used.
     */
    private boolean aUseFillPaint    = false;

    /**
     * The rectangle gradient paint start x location.
     */
    private double  aFillPaintX0     = 0.0;

    /**
     * The rectangle gradient paint start y location.
     */
    private double  aFillPaintY0     = 0.0;

    /**
     * The rectangle gradient paint start color.
     */
    private Color   aFillPaint0      = null;

    /**
     * The rectangle gradient paint end x location.
     */
    private double  aFillPaintX1     = 1.0;

    /**
     * The rectangle gradient paint end y location.
     */
    private double  aFillPaintY1     = 1.0;

    /**
     * The rectangle gradient paint end color.
     */
    private Color   aFillPaint1      = null;

    /**
     * The rectangle gradient paint cyclic flag.
     */
    private boolean aFillPaintCyclic = false;

    /**
     * Sets the Rectangle3D raised border flag. If false the
     * border is inset.
     * 
     * @param rb The Rectangle3D raised border flag.
     */
    public void setRaisedBorder(boolean rb)
    {
      aRaisedBorder = rb;
    }

    /**
     * Sets the Rectangle3D border color.
     * 
     * @param bc The Rectangle3D border color.
     */
    public void setBorderColor(Color bc)
    {
      aBorderColor = bc;
    }

    /**
     * Sets the Rectangle3D fill color.
     * 
     * @param fc The Rectangle3D fill color.
     */
    public void setFillColor(Color fc)
    {
      aUseFillPaint = false;
      aFillColor = fc;
    }

    /**
     * Sets the Rectangle3D paint values.
     * 
     * @param x0 The fractional x relative start in the Rectangle3D where the
     *           gradient paint is defined. 
     * @param y0 The fractional y relative start in the Rectangle3D where the
     *           gradient paint is defined.
     * @param fc0 The color defined at the start coordinates.
     * @param x1 The fractional x relative end in the Rectangle3D where the
     *           gradient paint is defined.
     * @param y1 The fractional y relative end in the Rectangle3D where the
     *           gradient paint is defined.
     * @param fc1 The color defined at the end coordinates.
     * @param cyclic True if the gradient paint is cyclic.
     */
    public void setFillPaint(double x0, double y0, Color fc0,
                             double x1, double y1, Color fc1,
                             boolean cyclic)
    {
      aUseFillPaint    = true;
      aFillPaintX0     = x0;
      aFillPaintY0     = y0;
      aFillPaint0      = fc0;
      aFillPaintX1     = x1;
      aFillPaintY1     = y1;
      aFillPaint1      = fc1;
      aFillPaintCyclic = cyclic;
    }

    /**
     * Creates a JLabel and adds it to this Rectangle3D. The label can be used
     * to write text on top of the progress bar. All standard JLabel properties
     * are set-able.
     * 
     * @return The JLabel written with this ProgressPanel.
     */
    public JLabel getLabel()
    {
      if (aLabel == null)
      {
        aLabel = new JLabel("", SwingConstants.CENTER);
        setLayout(new BorderLayout());
        add(aLabel, BorderLayout.CENTER);
      }

      return aLabel;
    }

    /**
     * Override the paintComponent function to repaint this progress bar as a
     * gradient color rectangle.
     */
    @Override
    public void paintComponent(Graphics g)
    {
      Graphics2D gc = (Graphics2D) g;

      // make sure lightweight children paint

      super.paintComponent(g);

      // get size and clip

      int w = (int) this.getWidth();
      int h = (int) this.getHeight();

      gc.clipRect(0, 0, w, h);

      // fill rectangle

      if (aUseFillPaint)
      {
        // find points x0,y0,x1,y1 from input paint coordinates and size of
        // rectangle

        int x0 = (int) (aFillPaintX0 * w);
        int x1 = (int) (aFillPaintX1 * w);
        int y0 = (int) (aFillPaintY0 * h);
        int y1 = (int) (aFillPaintY1 * h);
        GradientPaint gp = new GradientPaint(x0, y0, aFillPaint0,
                                             x1, y1, aFillPaint1,
                                             aFillPaintCyclic);
        gc.setPaint(gp);
      }
      else
        gc.setColor(aFillColor);

      gc.fill(new Rectangle(0, 0, w, h));

      // draw raised or inset border

      gc.setColor(aBorderColor);
      gc.draw3DRect(0, 0, w-1, h-1, aRaisedBorder);
    }
  }

  //***************************************************************************
  // MultiValueProgressBar attributes

  /**
   * The minimum range of the progress entries (only used if the number of
   * internal progress bars is 1).
   */
  private double  aProgMin         = 0.0;

  /**
   * The maximum range of the progress entries (ignored if the use full panel
   * flag is set (aUseFullPanel)). 
   */
  private double  aProgMax         = 100.0;

  /**
   * The sum of all rectangle progress entries.
   */
  private double  aProgSum         = 0.0;

  /**
   * The input progress entries for all rectangles.
   */
  private ArrayList<Double>      aProgress = new ArrayList<Double>();

  /**
   * The calculated progress fraction for all rectangles.
   */
  private ArrayList<Double>      aFrac     = new ArrayList<Double>();

  /**
   * The progress rectangles.
   */
  private ArrayList<Rectangle3D> aRects    = new ArrayList<Rectangle3D>();

  /**
   * The number of internal bars (or rectangles).
   */
  private int     aBarCount        = 1;

  /**
   * The array of calculated integer bar sizes.
   */
  private int[]   aBarSize         = null;

  /**
   * The raised border flag. If false the border is inset.
   */
  private boolean aRaisedBorder    = true;

  /**
   * The border color.
   */
  private Color   aBorderColor     = Color.GRAY;

  /**
   * The border width (in pixels).
   */
  private int     aBorderWidth     = 2;

  /**
   * The "use full panel" flag. If true, all rectangle widths
   * (heights) represent a fraction entry given by their progress
   * entry over the sum of all progress entries. Otherwise, all
   * progress bar widths (heights) are scaled to lie between 0 and
   * the maximum setting (aProgMax).
   */
  private boolean aUseFullPanel    = false;

  /**
   * If true internal rectangles are laid out left to right. The left-most
   * rectangle is index 0. If false the internal rectangles are laid out
   * bottom to top. The bottom-most rectangle is index 0.
   */
  private boolean aUseLeftToRight  = true;

  /**
   * The multi-value progress bar  fill color.
   */
  private Color   aFillColor       = Color.RED;

  /**
   * The multi-value progress bar fill flag. If true paint is used.
   */
  private boolean aUseFillPaint    = false;

  /**
   * The multi-value progress bar gradient paint start x location.
   */
  private double  aFillPaintX0     = 0.0;

  /**
   * The multi-value progress bar gradient paint start y location.
   */
  private double  aFillPaintY0     = 0.0;

  /**
   * The multi-value progress bar gradient paint start color.
   */
  private Color   aFillPaint0      = null;

  /**
   * The multi-value progress bar gradient paint end x location.
   */
  private double  aFillPaintX1     = 1.0;

  /**
   * The multi-value progress bar gradient paint end y location.
   */
  private double  aFillPaintY1     = 1.0;

  /**
   * The multi-value progress bar gradient paint end color.
   */
  private Color   aFillPaint1      = null;

  /**
   * The multi-value progress bar gradient paint cyclic flag.
   */
  private boolean aFillPaintCyclic = false;

  /**
   * Default constructor. Creates a default MultiValueProgressBar.
   */
  public MultiValueProgressBar()
  {
    super.setLayout(null);
    setBarCount(1);
  }
//
//  @Override
//  public Dimension getPreferredSize() {
//      return new Dimension(768, 24);
//  }
//
//  @Override
//  public Dimension getMinimumSize() {
//      return new Dimension(384, 24);
//  }

  @Override
  public Dimension getMaximumSize() {
      return getPreferredSize();
  }

  /**
   * Set the progress bar minimum limit to pl.
   * 
   * @param pl The new progress bar minimum limit.
   */
  public void setProgressMinimumLimit(double pl) throws IOException
  {
    if (pl >= aProgMax)
    {
      String s = "  Progress Bar Maximum (" + aProgMax +
                 ") is <= Minimum (" + pl + ") ...";
      throw new IOException(s);
    }

    // set the new minimum limit

    aProgMin = pl;
  }

  /**
   * Set the progress bar maximum limit to pl.
   * 
   * @param pl The new progress bar maximum limit.
   */
  public void setProgressMaximumLimit(double pl) throws IOException
  {
    if (pl <= aProgMin)
    {
      String s = "  Progress Bar Minimum (" + aProgMin +
                 ") is >= Maximum (" + pl + ") ...";
      throw new IOException(s);
    }

    // set the new maximum limit

    aProgMax = pl;
  }

  /**
   * Sets the "use full panel" flag. If true a multi-valued progress bar
   * values all sum to the length of the total progress bar.
   * 
   * @param fpd "use full panel" flag.
   */
  public void setUseFullPanelDisplay(boolean fpd)
  {
    aUseFullPanel = fpd;
  }

  /**
   * Sets the outer border width of the progress bar.
   * 
   * @param w The outer border width of the progress bar.
   */
  public void setBorderWidth(int w)
  {
    aBorderWidth = w;
  }

  /**
   * Sets the raised border if the input flag is true, otherwise the
   * border is inset. The sub-Rectangle3D borders are set to the same
   * value.
   * 
   * @param rb The raised border flag.
   */
  public void setRaisedBorder(boolean rb)
  {
    aRaisedBorder = rb;
    for (int i = 0; i < aRects.size(); ++i)
      aRects.get(i).setRaisedBorder(aRaisedBorder);
  }

  /**
   * Sets the raised border if the input flag is true, otherwise the
   * border is inset. The sub-Rectangle3D borders are set to inverse
   * of this setting.
   * 
   * @param rb The raised border flag.
   */
  public void setRaisedBorderSubInvert(boolean rb)
  {
    aRaisedBorder = rb;
    for (int i = 0; i < aRects.size(); ++i)
      aRects.get(i).setRaisedBorder(!aRaisedBorder);
  }

  /**
   * Sets the border color to bc.
   * 
   * @param bc The border color.
   */
  public void setBorderColor(Color bc)
  {
    aBorderColor = bc;
    for (int i = 0; i < aRects.size(); ++i)
      aRects.get(i).setBorderColor(aBorderColor);
  }

  /**
   * Sets the background fill color.
   * 
   * @param bgc The background fill color.
   */
  public void setBackgroundColor(Color bgc)
  {
    aUseFillPaint = false;
    aFillColor    = bgc;
  }

  /**
   * Sets the background gradient paint color.
   * 
   * @param x0 The fractional x relative start in the Rectangle3D where the
   *           gradient paint is defined. 
   * @param y0 The fractional y relative start in the Rectangle3D where the
   *           gradient paint is defined.
   * @param bgc0 The color defined at the start coordinates.
   * @param x1 The fractional x relative end in the Rectangle3D where the
   *           gradient paint is defined.
   * @param y1 The fractional y relative end in the Rectangle3D where the
   *           gradient paint is defined.
   * @param bgc1 The color defined at the end coordinates.
   * @param cyclic True if the gradient paint is cyclic.
   */
  public void setBackgroundPaint(double x0, double y0, Color bgc0,
                                 double x1, double y1, Color bgc1,
                                 boolean cyclic)
  {
    aUseFillPaint    = true;
    aFillPaintX0     = x0;
    aFillPaintY0     = y0;
    aFillPaint0      = bgc0;
    aFillPaintX1     = x1;
    aFillPaintY1     = y1;
    aFillPaint1      = bgc1;
    aFillPaintCyclic = cyclic;
  }

  /**
   * Sets the number of internal bars or Rectangl3D objects.
   * 
   * @param bc The number of internal bars or Rectangl3D objects.
   */
  public void setBarCount(int bc)
  {
    if (bc > 0)
    {
      // set count and clear arrays

      aBarCount = bc;
      aBarSize = new int [bc];
      aRects.clear();
      aFrac.clear();
      aProgress.clear();
      removeAll();

      // create bc new bars

      for (int i = 0; i < bc; ++i)
      {
        Rectangle3D r3d = new Rectangle3D();
        r3d.setBorderColor(aBorderColor);
        r3d.setRaisedBorder(aRaisedBorder);
        add(r3d);
        aRects.add(r3d);
        aFrac.add(0.0);
        aProgress.add(0.0);
      }
    }
  }

  /**
   * Sets the bars layout to increase from left to right. Bar 0 is
   * furtherest left. 
   */
  public void setLeftToRight()
  {
    aUseLeftToRight = true;
  }

  /**
   * Sets the bars layout to increase from bottom to top. Bar 0 is
   * at the bottom. 
   */
  public void setTopToBottom()
  {
    aUseLeftToRight = false;
  }

  /**
   * Override the paintComponent function to repaint this progress bar as a
   * gradient color rectangle.
   */
  @Override
  public void paintComponent(Graphics g)
  {
    Graphics2D gc = (Graphics2D) g;

    // make sure lightweight children paint

    super.paintComponent(g);
    //super.paintBorder(g);

    // reposition all rectangles based on fractions
    // draw raised or inset border

    // get size and clip

    int w = (int) this.getWidth();
    int h = (int) this.getHeight();

    gc.clipRect(0, 0, w, h);

    // fill background rectangle

    if (aUseFillPaint)
    {
      // find points x0,y0,x1,y1 from input paint coordinates and size of
      // rectangle

      int x0 = (int) (aFillPaintX0 * w);
      int x1 = (int) (aFillPaintX1 * w);
      int y0 = (int) (aFillPaintY0 * h);
      int y1 = (int) (aFillPaintY1 * h);
      GradientPaint gp = new GradientPaint(x0, y0, aFillPaint0,
                                           x1, y1, aFillPaint1, aFillPaintCyclic);
      gc.setPaint(gp);
    }
    else
      gc.setColor(aFillColor);

    gc.fill(new Rectangle(0, 0, w, h));

    // draw raised or inset border

    gc.setColor(aBorderColor);
    for (int i = 0; i < aBorderWidth; ++i)
    {
      int twoi = 2 * i+1;
      gc.draw3DRect(i, i, w - twoi, h - twoi, aRaisedBorder);
    }

    // reposition and repaint all Rectangle3D objects

    int dd = 2 * aBorderWidth;
    if (aBarCount == 1)
    {
      if (aUseLeftToRight)
      {
        int w0 = (int) Math.round(aFrac.get(0) * (w - dd));
        aRects.get(0).setBounds(aBorderWidth, aBorderWidth, w0, h - dd);
      }
      else
      {
        int h0 = (int) Math.round(aFrac.get(0) * (h - dd));
        aRects.get(0).setBounds(aBorderWidth, h - aBorderWidth - h0, w - dd, h0);
      }
      aRects.get(0).getLabel().revalidate();
    }
    else
    {
      // if use full panel or aprogsum >= aprogmax then
      // all bars must add up to w-dd
      // otherwise all bars must add up to (aprogsum / aprogmax) (w-dd)

      int x0 = aBorderWidth;
      int y0 = aBorderWidth;
      int hd = h - dd;
      int wd = w - dd;
      if (aUseLeftToRight)
      {
        setBarSize(wd);
        for (int i = 0; i < aBarCount; ++i)
        {
          aRects.get(i).setBounds(x0, y0, aBarSize[i], hd);
          x0 += aBarSize[i];
          aRects.get(i).getLabel().revalidate();
        }
      }
      else
      {
        y0 = h - aBorderWidth;
        setBarSize(hd);
        for (int i = 0; i < aBarCount; ++i)
        {
          y0 -= aBarSize[i];
          aRects.get(i).setBounds(x0, y0, wd, aBarSize[i]);
          aRects.get(i).getLabel().revalidate();
        }
      }
    }
  }

  /**
   * Used by the paint component function to set the bar size of all
   * internal rectangles so that they all sum to xd.
   * 
   * @param xd The value that all internal bar sizes sum to.
   */
  private void setBarSize(int xd)
  {
    int sum = 0;
    for (int i = 0; i < aBarCount; ++i)
    {
      aBarSize[i] = (int) Math.round(aFrac.get(i) * xd);
      sum += aBarSize[i];
    }
    int stp = 0;
    if (sum < xd)
      stp = 1;
    else if (sum > xd)
      stp = -1;
    int i = 0;
    while (sum != xd)
    {
      aBarSize[i] += stp;
      sum += stp;
    }
  }

  /**
   * Sets the progress bar progress value to p.
   * 
   * @param p The new progress bar progress value.
   */
  public void setProgress(double p)
  {
    // set aProgress to p ... ensure p is bounded between 0 and aProgLimit

    if (aBarCount == 1)
    {
      if (p < aProgMin)
        aProgress.set(0, aProgMin);
      else if (p > aProgMax)
        aProgress.set(0, aProgMax);
      else
        aProgress.set(0, p);

      // calculate the color bar fraction

      aFrac.set(0, (aProgress.get(0) - aProgMin) / (aProgMax - aProgMin));
      repaint();
    }
    else // aBarCount > 1
    {
      aProgress.set(0, p);
      updateProgressFractions();
    }
  }

  /**
   * returns the ith internal rectangle.
   * 
   * @param i The index of the internal rectangle to be returned.
   * @return The ith internal rectangle.
   */
  public Rectangle3D getRectangle(int i)
  {
    return aRects.get(i);
  }

  /**
   * Sets the first two progress entries. Convenience function for when only
   * two internal rectangles are represented.
   * 
   * @param p0 The progress of the first internal rectangle.
   * @param p1 The progress of the second internal rectangle.
   */
  public void setProgress(double p0, double p1)
  {
    if (aBarCount < 2) return;
    aProgress.set(0, p0);
    aProgress.set(1, p1);
    updateProgressFractions();
  }

  /**
   * Sets the first three progress entries. Convenience function for when only
   * three internal rectangles are represented.
   * 
   * @param p0 The progress of the first internal rectangle.
   * @param p1 The progress of the second internal rectangle.
   * @param p2 The progress of the third internal rectangle.
   */
  public void setProgress(double p0, double p1, double p2)
  {
    if (aBarCount < 3) return;
    aProgress.set(0, p0);
    aProgress.set(1, p1);
    aProgress.set(2, p2);
    updateProgressFractions();
  }

  /**
   * Returns the ith progress bar entry.
   * 
   * @param i The entry of the ith progress bar.
   * @return The ith progress bar entry.
   */
  public double getProgress(int i)
  {
    return aProgress.get(i);
  }

  /**
   * Sets the ith rectangle progress entry to p.
   * 
   * @param i The ith rectangle whose progress will be set.
   * @param p The value that the ith rectangles progress will be set to.
   */
  public void setProgress(int i, double p)
  {
    if ((aBarCount == 1) && (i == 0))
      setProgress(p);
    else if (i < aBarCount)
    {
      aProgress.set(i, p);
      updateProgressFractions();
    }
  }

  /**
   * Sets all rectangle progress fractions to their respective values in the
   * input array p.
   * 
   * @param p The array from which progress fractions will be set.
   */
  public void setProgress(double[] p)
  {
    if (p.length == 1)
      setProgress(p[0]);
    else
    {
      int n = p.length;
      if (n > aBarCount) n = aBarCount;
      for (int i = 0; i < n; ++i) aProgress.set(i, p[i]);
      updateProgressFractions();
    }
  }

  /**
   * Sets all rectangle progress fractions to their respective values in the
   * input list p.
   * 
   * @param p The list from which progress fractions will be set.
   */
  public void setProgress(ArrayList<Double> p)
  {
    if (p.size() == 1)
      setProgress(p.get(0));
    else
    {
      int n = p.size();
      if (n > aBarCount) n = aBarCount;
      for (int i = 0; i < n; ++i) aProgress.set(i, p.get(i));
      updateProgressFractions();
    }
  }

  /**
   * Updates all progress fractions to a unit normal sum if
   * their input sum exceeds the maximum allowed setting (aProgMax), or
   * if the flag aUseFullPanel is true.
   */
  private void updateProgressFractions()
  {
    aProgSum = 0.0;
    for (int i = 0; i < aBarCount; ++i) aProgSum += aProgress.get(i);

    double s = aProgMax;
    if (aUseFullPanel || (aProgSum >= aProgMax)) s = aProgSum;
    for (int i = 0; i < aBarCount; ++i) aFrac.set(i, aProgress.get(i) / s);
  }

  /**
   * Sets the ith rectangle label text to s.
   * 
   * @param i The index of the rectangle whose label text will be set.
   * @param s The text the ith label is set to.
   */
  public void setText(int i, String s)
  {
    aRects.get(i).getLabel().setText(s);
    aRects.get(i).repaint();
    aRects.get(i).getLabel().repaint();
  }

  /**
   * returns the ith rectangle label.
   * 
   * @param i The index of the rectangle label to return.
   * @return The ith rectangle label.
   */
  public JLabel getLabel(int i)
  {
    return aRects.get(i).getLabel();
  }

  /**
   * Returns the total progress sum from all bars.
   * 
   * @return The total progress sum from all bars.
   */
  public double getSum()
  {
    return aProgSum;
  }

  /**
   * Returns the progress fraction of the ith rectangle.
   * 
   * @param i The index of the rectangle for which the progress will be returned.
   * @return The progress fraction of the ith rectangle.
   */
  public double getFraction(int i)
  {
    return aFrac.get(i);
  }
}
