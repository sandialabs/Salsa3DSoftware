/**
 * Copyright 2009 Sandia Corporation. Under the terms of Contract DE-AC04-94AL85000 with Sandia
 * Corporation, the U.S. Government retains certain rights in this software.
 * 
 * BSD Open Source License.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * 
 * - Neither the name of Sandia National Laboratories nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.sandia.gmp.baseobjects.hyperellipse;

import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.DEPTH;
import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.LAT;
import static gov.sandia.gmp.baseobjects.globals.GMPGlobals.LON;
import static gov.sandia.gmp.util.globals.Globals.PI_OVR_TWO;
import static gov.sandia.gmp.util.globals.Globals.TWO_PI;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import gov.sandia.geotess.GeoTessGrid;
import gov.sandia.geotessbuilder.GeoTessBuilderMain;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.util.numerical.simplex.Simplex;
import gov.sandia.gmp.util.numerical.simplex.SimplexFunction;
import gov.sandia.gmp.util.numerical.vector.GeoMath;
import gov.sandia.gmp.util.numerical.vector.VectorUnit;
import gov.sandia.gmp.util.testingbuffer.TestBuffer;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

public class Ellipsoid implements SimplexFunction, Serializable {

  private static final long serialVersionUID = 5712937458441860265L;

  /**
   * Equation coefficients to compute distance to perimeter given a direction specified by a unit
   * vector in north-east-depth coordinates.
   */
  private double[] coeff;

  /**
   * Scale length to apply to ellipsoid dimensions to yield km.
   */
  double kappa3;

  /**
   * 3x3 array containing orientations of the semi-major, semi-intermediate and semi-minor axes of
   * the uncertainty ellipsoid. Elements are:
   * <ol>
   * <li>Trend of the axis in horizontal plane, in degrees. Measured clockwise from north.</li>
   * <li>Plunge of the axis in degrees, positive down</li>
   * <li>Unscaled length of the axis. Multiply by kappa3 to yield km</li>
   * </ol>
   */
  private double[][] principal_axes;

  /**
   * /** 3x3 array containing orientations of the semi-major, semi-intermediate and semi-minor axes
   * of the uncertainty ellipsoid. For each axis, 3-element array is a unit vector pointing in the
   * direction of the axis. Components are 0: north, 1: east and 2: depth.
   */
  private double[][] normalVectors;

  /**
   * Attribute used by simplex algorithm. Set to -1 for maximization or 1 for minimazation.
   */
  private int maximize;

  /**
   * Constructor
   * 
   * @param hyperEllipse
   * @throws Exception
   */
  public Ellipsoid(HyperEllipse hyperEllipse) throws Exception {
    kappa3 = hyperEllipse.getKappa(3);
    this.coeff = hyperEllipse.uncertainty_equation_coefficients(new int[] {LAT, LON, DEPTH});
    find_principal_axes();
  }

  public boolean isValid() {
    return true;
  }

  /**
   * Retrieve the trend of the major axis of the ellipsoid in degrees. This is the angle in the
   * horizontal plane measured east of due north. Range is 0 to 360. A value of -1 implies invalid
   * uncertainty information.
   * 
   * @return the trend of the major axis of the ellipsoid in degrees. -999999. implies invalid
   *         uncertainty information.
   * @throws Exception
   */
  public double getMajaxTrend() {
    return toDegrees(principal_axes[0][0]);
  }

  /**
   * Retrieve the plunge of the major axis of the ellipsoid in degrees. Plunge is the angle between
   * the axis and the horizontal plane; positive is down from the horizontal. Range is -90 to 90.
   * 
   * @return the plunge of the major axis of the ellipsoid in degrees; positive is down. -999999.
   *         implies invalid uncertainty information.
   * @throws Exception
   */
  public double getMajaxPlunge() {
    return toDegrees(principal_axes[0][1]);
  }

  /**
   * Retrieve the length of the major axis of the ellipsoid in km.
   * 
   * @return the length of the major axis of the ellipsoid in km. -999999. implies invalid
   *         uncertainty information.
   * @throws Exception
   */
  public double getMajaxLength() {
    return principal_axes[0][2] * kappa3;
  }

  /**
   * Retrieve the trend of the intermediate axis of the ellipsoid in degrees. This is the angle in
   * the horizontal plane measured east of due north. Range is 0 to 360.
   * 
   * @return the trend of the intermediate axis of the ellipsoid in degrees -999999. implies invalid
   *         uncertainty information.
   * @throws Exception
   */
  public double getIntaxTrend() {
    return toDegrees(principal_axes[1][0]);
  }

  /**
   * Retrieve the plunge of the intermediate axis of the ellipsoid in degrees. Plunge is the angle
   * between the axis and the horizontal plane; positive is down from the horizontal. Range is -PI/2
   * to 90.
   * 
   * @return the plunge of the intermediate axis of the ellipsoid in degrees; positive is down.
   *         -999999. implies invalid uncertainty information.
   * @throws Exception
   */
  public double getIntaxPlunge() {
    return toDegrees(principal_axes[1][1]);
  }

  /**
   * Retrieve the length of the intermediate axis of the ellipsoid in km.
   * 
   * @return the length of the intermediate axis of the ellipsoid in km. -999999. implies invalid
   *         uncertainty information.
   * @throws Exception
   */
  public double getIntaxLength() {
    return principal_axes[1][2] * kappa3;
  }

  /**
   * Retrieve the trend of the minor axis of the ellipsoid in degrees. This is the angle in the
   * horizontal plane measured east of due north. Range is 0 to 360.
   * 
   * @return the trend of the minor axis of the ellipsoid in degrees -999999. implies invalid
   *         uncertainty information.
   * @throws Exception
   */
  public double getMinaxTrend() {
    return toDegrees(principal_axes[2][0]);
  }

  /**
   * Retrieve the plunge of the minor axis of the ellipsoid in degrees. Plunge is the angle between
   * the axis and the horizontal plane; positive is down from the horizontal. Range is -90 to 90.
   * 
   * @return the plunge of the minor axis of the ellipsoid in degrees; positive is down. -999999.
   *         implies invalid uncertainty information.
   * @throws Exception
   */
  public double getMinaxPlunge() {
    return toDegrees(principal_axes[2][1]);
  }

  /**
   * Retrieve the length of the minor axis of the ellipsoid in km.
   * 
   * @return the length of the minor axis of the ellipsoid in km. -999999. implies invalid
   *         uncertainty information.
   * @throws Exception
   */
  public double getMinaxLength() {
    return principal_axes[2][2] * kappa3;
  }

  /**
   * Use the simplex algorithm to find the unscaled major, intermediate and minor axes of the
   * uncertainty ellipoid. Multiply by kappa3 to get km.
   * 
   * @throws Exception
   */
  private void find_principal_axes() throws Exception {

    if (HyperEllipse.isZero(coeff)) {
      principal_axes = new double[3][3];
      normalVectors = new double[][] {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
      return;
    }

    principal_axes = new double[3][];

    // find trend and plunge of major axis in radians. length = NaN
    principal_axes[0] = find_extreme(-1);

    if (principal_axes[0].length == 0) {
      coeff = null;
      principal_axes = null;
      return;
    }

    // find trend and plunge of minor axis in radians. length = NaN
    principal_axes[2] = find_extreme(1);

    if (principal_axes[2].length == 0) {
      coeff = null;
      principal_axes = null;
      return;
    }

    // convert major and minor axes from trend/plunge to unit vectors in north-east-depth
    // coordinates
    double[] major = unit_vector(principal_axes[0]);
    double[] minor = unit_vector(principal_axes[2]);

    // intermediate axis is w cross u
    double[] intermediate = VectorUnit.cross(major, minor);

    // check that the length of the intermediate axis is 1. This indicates
    // that the major and minor axes are in fact perpendicular to each other.
    double len = 1. - GeoMath.length(intermediate);

    if (abs(len) > 1e-7) {
      coeff = null;
      principal_axes = null;
      return;
    }

    // convert intermediate to trend and plunge in radians. length = NaN.
    principal_axes[1] = trendPlunge(intermediate);

    // save the unit vectors of major, intermediate and minor axes in
    // north-east-depth coordinate system
    normalVectors = new double[][] {major, intermediate, minor};

    // for each principal axis, compute distance to the perimeter of the ellipsoid.
    // The lengths are unscaled. Multiply by kapp3 to get km.
    for (int i = 0; i < 3; ++i)
      principal_axes[i][2] = distance_to_perimeter(normalVectors[i]);


    if (principal_axes[1][1] < 0) {
      principal_axes[1][1] = -principal_axes[1][1];
      principal_axes[1][0] += PI;
    }

    // ensure that the trend is between 0 and 2PI
    principal_axes[1][0] = (principal_axes[1][0] + TWO_PI) % TWO_PI;
  }

  /**
   * find major or minor axis of the ellipsoid, depending on setting of maximize private member. All
   * angle are in radians.
   * 
   * @param maximize if -1 find the maximum, if +1 find the minimum
   * 
   * @return trend, plunge and length of major or minor axis.
   * @throws Exception
   */

  /**
   * find trend and plunge in radians of major or minor axis of the ellipsoid.
   * 
   * @param maximize -1 to find major axis, 1 to find minor axis
   * @return
   * @throws Exception
   */
  private double[] find_extreme(int maximize) throws Exception {
    this.maximize = maximize;

    Simplex simplex = new Simplex(this, 1e-8, 1000);

    // declare variables needed by amoeba.
    // p is 3 initial estimates of trend and plunge, in radians.
    double[][] p = new double[][] {{0., 0.7}, {0., 0.8}, {0.1, 0.75}};

    // y will initially hold the three distances from the center of the ellipsoid
    // to its perimeter. When searching for the major axis of the ellipsoid,
    // the y values will be negative because Simplex does minimization, not
    // maximization.
    double[] y = new double[3];

    // find the trend and plunge (p) and the length (y) of the principal axis in question
    y = simplex.search(p);

    // at this point, p[0] holds the trend and plunge of the principal axis
    // and y[0] holds the length of the axis. Move trend, plunge and length
    // into y.
    y[0] = p[0][0]; // trend
    y[1] = p[0][1]; // plunge
    y[2] = Double.NaN; // length

    // ensure that the plunge is in range -PI/2 to PI/2.
    while (y[1] < -PI_OVR_TWO)
      y[1] += PI;
    while (y[1] > PI_OVR_TWO)
      y[1] -= PI;

    // ensure that the plunge is positive (down from horizontal)
    if (y[1] < 0.) {
      y[1] = -y[1];
      y[0] += PI;
    }

    // ensure that the trend is in range 0 to 2*PI.
    y[0] = (y[0] + TWO_PI) % TWO_PI;

    return y;
  }

  /**
   * Find the distance from the center of the ellipsoid to the perimeter of the ellipsoid when
   * moving in the specified direction. The distance is unscaled. Multiply by kappa3 to get km.
   * 
   * @param v unit vector in north-east-depth coordinate system, that specifies the direction in
   *        which to move.
   */
  public double distance_to_perimeter(double... v) {
    // calculate distance from center of ellipsoid to perimeter of
    // ellipsoid, in the direction specified by vector v.
    // v is a 3-component unit vector specifying direction in which to
    // look. north-east-depth coordinates
    if (HyperEllipse.isZero(coeff))
      return 0;
    return 1. / sqrt(coeff[0] * v[0] * v[0] + coeff[1] * v[0] * v[1] + coeff[2] * v[0] * v[2]
        + coeff[3] * v[1] * v[1] + coeff[4] * v[1] * v[2] + coeff[5] * v[2] * v[2]);
  }

  /**
   * Find the unscaled distance from the center of the ellipsoid to the perimeter of the ellipsoid
   * when moving in the specified direction. Multiply by kappa3 to get km.
   * <p>
   * Set global parameter 'maximize' to -1 to search for the major axis of the ellipsoid and set it
   * to 1 to find the minor axis of the ellipsoid.
   * 
   * @param tp trend and plunge in radians. Trend is angle east of north measured in horizontal
   *        plane. Plunge is angle down from the horizontal.
   * @return unscled distance to the perimeter of the ellipsoid, multiplied by 'maximize'.
   */
  public double simplexFunction(double[] tp) {
    // tp is a 2-element array containing trend and plunge.
    // find the distance to the perimeter of the ellipsoid in the
    // direction specified by tp.

    double[] u = unit_vector(tp);

    // simplex does minimization. So to find major axis set maximize = -1.
    return maximize * distance_to_perimeter(u);
  }

  /**
   * Convert a 3 component unit vector in north-east-depth coordinate system into trend, plunge.
   * 
   * @param (input) u unit vector
   * @param tp (output) trend and plunge in radians. Trend is direction east of north and plunge is
   *        angle down from horizontal, in radians.
   */
  private double[] trendPlunge(double[] unitVector) {
    // v is in north-east-depth coordinate system
    double[] tp = new double[3];
    tp[0] = atan2(unitVector[1], unitVector[0]); // east component / north component
    tp[1] =
        atan(unitVector[2] / sqrt(unitVector[0] * unitVector[0] + unitVector[1] * unitVector[1]));
    tp[2] = Double.NaN;
    return tp;
  }

  /**
   * Convert a trend and plunge (in radians) into a unit vector. The length of the input vector is
   * ignored if present.
   * 
   * @param tp (input) 2-element array with trend and plunge in radians.
   * @param v (output) unit vector in north-east-depth coordinates
   */
  private double[] unit_vector(double[] tp) {
    return new double[] {cos(tp[0]) * cos(tp[1]), // north component
        sin(tp[0]) * cos(tp[1]), // east component
        sin(tp[1]) // depth component
    };
  }

  /**
   * Retrieve the principal axes of the ellipoid. They are contained in a 3 x 3 array. First row
   * contains the major axis, second row contains the intermediate axis, and third row contains the
   * minor axis. First column is the trend of the axis in radians, second column is plunge of the
   * axis in radians (positive down from the horizontal), and the third column is the length of the
   * axis in km.
   * 
   * @return the principal axes of the ellipsoid
   * @throws Exception
   */
  public double[][] getPrincipalAxes() {
    return principal_axes;
  }

  public static double ELLIPSOID_GRID_RESOLUTION = 1.;

  public void writeVTK(File f, GeoVector center) throws Exception {
    if (!f.getName().endsWith(".vtk"))
      throw new Exception("file name must end with 'vtk'");

    // get a GeoTessGrid object that will be shaped into an ellipsoid by
    // setting the radius of each vertex
    GeoTessGrid grid = (GeoTessGrid) GeoTessBuilderMain.getGrid(ELLIPSOID_GRID_RESOLUTION);

    // make a list of unit vectors with capacity of n vertices + 2
    List<double[]> points = new ArrayList<>(grid.getNVertices() + 2);

    // for each vertex of the grid, in east-north-elevation coordinates,
    // compute the distance to the perimeterof the ellipsoid and add a point to the list
    for (double[] vertex : grid.getVertices())
      // Call distance_to_perimeter with vector in north-east-depth coordinates
      points.add(GeoMath.multiply(vertex,
          kappa3 * distance_to_perimeter(vertex[1], vertex[0], -vertex[2])));

    // build the connectivity of the vtk dataset.
    List<VTKCell> cells = new ArrayList<>();

    for (int t = grid.getFirstTriangle(0); t <= grid.getLastTriangle(0); ++t)
      cells.add(new VTKCell(VTKCellType.VTK_TRIANGLE, grid.getTriangleVertexIndexes(t)));

    // add the full vector of the location of the center of the ellipsoid (not a unit vector).
    points.add(center.getVector());

    // write the offset of the center of the ellipsoid from the origin of the vtk reference frame.
    points.add(new double[] {0, 0, 0});

    // write the vtk dataset to output file. Add string '_ellipsoid' to the file name.
    String name = f.getName();
    int idx = name.indexOf('.');
    String ext = name.substring(idx);
    name = name.substring(0, idx) + "_ellipsoid" + ext;
    VTKDataSet.write(new File(f.getParent(), name), points, cells);

    // done writing the ellipsoid. Now write another vtk file for the primary axes of the
    // ellipsoid.
    points.clear();
    cells.clear();

    for (int i = 0; i < 3; ++i) // for each axis of the ellipsoid
    {
      // find orientation of axis in north-east-depth coordinates, length in km.
      double[] v = GeoMath.multiply(normalVectors[i], distance_to_perimeter(normalVectors[i]));

      // convert from north-east-depth coordinates to east-north-elevation
      double[] point = new double[] {v[1], v[0], -v[2]};

      // add a point at one end of the axis
      points.add(point);
      // add another point at other end of axis.
      points.add(GeoMath.multiply(point, -1.));

      // add a vtk_line for each axis
      cells.add(new VTKCell(VTKCellType.VTK_LINE, new int[] {i * 2, i * 2 + 1}));
    }

    // write the full vector of the location of the center of the ellipsoid.
    points.add(center.getVector());

    // write the offset of the center of the ellipsoid
    // from the origin of the vtk reference frame.
    points.add(new double[] {0, 0, 0});

    // write the 3 axes to a vtk file with the string '_axes' inserted before the extension.
    name = name.substring(0, idx) + "_axes" + ext;
    VTKDataSet.write(new File(f.getParent(), name), points, cells);

    // output a text file with the coefficients of the ellipsoid

    name = name.substring(0, idx) + "_ellipsoid.dat";
    FileWriter fout = new FileWriter(new File(f.getParent(), name));
    fout.append(String.format(
        "# Given a unit vector v in north, east, depth coordinates, the following calculation %n"
            + "# will return the distance from the center of the ellipsoid to its perimeter, in km.%n"));

    fout.append(String
        .format("# distance_km = kappa3 / sqrt(coeff[0] * v[0] * v[0] + coeff[1] * v[0] * v[1] %n"
            + "# + coeff[2] * v[0] * v[2] + coeff[3] * v[1] * v[1] + coeff[4] * v[1] * v[2] %n"
            + "# + coeff[5] * v[2] * v[2])%n%n"));

    fout.append(String.format("Center lat(deg), lon(deg), depth(km) = %s%n%n",
        center.toString("%1.8f %1.8f %1.6f")));

    fout.append(String.format("kappa3   = %22.15e%n", kappa3));
    for (int i = 0; i < coeff.length; ++i)
      fout.append(String.format("coeff[%d] = %22.15e%n", i, coeff[i]));

    fout.close();
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    try {
      buf.append(String.format("              length      trend     plunge%n"));
      buf.append(String.format("   major: %10.4f %10.4f %10.4f%n", getMajaxLength(),
          getMajaxTrend(), getMajaxPlunge()));

      buf.append(String.format("   inter: %10.4f %10.4f %10.4f%n", getIntaxLength(),
          getIntaxTrend(), getIntaxPlunge()));

      buf.append(String.format("   minor: %10.4f %10.4f %10.4f%n%n", getMinaxLength(),
          getMinaxTrend(), getMinaxPlunge()));

      String[] label = new String[] {"major:  ", "inter:  ", "minor:  "};

      buf.append("              north       east        depth\n");
      for (int i = 0; i < normalVectors.length; ++i)
        buf.append(String.format("%s %10.6f, %10.6f, %10.6f%n", label[i], normalVectors[i][0],
            normalVectors[i][1], normalVectors[i][2]));

    } catch (Exception e) {
      buf.setLength(0);
      buf.append("ERROR in Ellipsoid. " + e.getMessage());
    }
    return buf.toString();
  }

  public TestBuffer getTestBuffer() {
    TestBuffer buffer = new TestBuffer(this.getClass().getSimpleName());
    buffer.add("ellipsoid.majax_length", getMajaxLength());
    buffer.add("ellipsoid.majax_trend", getMajaxTrend());
    buffer.add("ellipsoid.majax_plunge", getMajaxPlunge());
    buffer.add("ellipsoid.intax_length", getIntaxLength());
    buffer.add("ellipsoid.intax_trend", getIntaxTrend());
    buffer.add("ellipsoid.intax_plunge", getIntaxPlunge());
    buffer.add("ellipsoid.minax_length", getMinaxLength());
    buffer.add("ellipsoid.minax_trend", getMinaxTrend());
    buffer.add("ellipsoid.minax_plunge", getMinaxPlunge());
    buffer.add();
    return buffer;
  }

  /**
   * This main() program will load a bunch of *_ellipsoid.vtk and *_axes.vtk files, compute the
   * centroid of their centers and then translate the contents of each file away from the centroid
   * by the appropriate distance. Input files are overwriiten but can be restored by running this
   * app with only a single command line parameter equal to the file to be restored.
   * <p>
   * Command line arguments are the names of vtk files and/or directories containing vtk files.
   * <p>
   * This main() can be run by executing: <br>
   * java -cp locoo3d.jar gov.sandia.gmp.locoo3d.Ellipsoid <list of vtk files/directories>
   * 
   * @param args
   */
  public static void main(String[] args) {
    try {
      if (args.length != 2) {
        throw new Exception(
            "Specify 2 directories: (1) directory containing ellipsoid vtk files to be translated and "
                + "(2) output directory where ellipsoid vtk files will be output");
      }

      File inputDir = new File(args[0]);

      List<File> vtkFiles = new ArrayList<>();
      findVTKFiles(inputDir, vtkFiles);

      File outputDir = new File(args[1]);
      outputDir.mkdirs();

      // list of vtk datasets loaded from vtk files.
      List<VTKDataSet> dataSets = new ArrayList<>(vtkFiles.size());

      // centroid will be the unit vector equal to the normalized vector
      // sum of all the ellipsoid centers.
      double[] centroid = new double[3];
      // centroidDepth will be the average depth of all the centers.
      double centroidDepth = 0;

      int nameLength = 0;
      for (File f : vtkFiles) {
        // load a dataset from the vtk file and save it
        VTKDataSet dataSet = new VTKDataSet(f);
        dataSets.add(dataSet);

        // get a reference to the list of points in the dataset
        List<double[]> points = dataSet.getPoints();
        // get a copy of the center.
        double[] center = points.get(points.size() - 2).clone();
        // normalize center to a unit vector and save the length of the
        // vector before it was normalized
        double rCenter = VectorUnit.normalize(center);

        // accumulate the sum of the center depths
        centroidDepth += GeoMath.getEarthRadius(center) - rCenter;
        // accumulate vector sum of all the centers.
        for (int i = 0; i < 3; ++i)
          centroid[i] += center[i];

        nameLength = max(nameLength, f.getName().length());
      }

      // normalize centroid to unit vector and find average depth
      VectorUnit.normalize(centroid);
      centroidDepth /= vtkFiles.size();

      // find the earth radius at location of centroid
      double earthRadiusCentroid = GeoMath.getEarthRadius(centroid);

      System.out.printf("Centroid lat, lon, depth: %s %1.3f%n%n", GeoMath.getLatLonString(centroid),
          centroidDepth);

      String format = String.format("%%-%ds %%s %%8.3f %%9.3f %%9.3f %%9.3f%n", nameLength);

      String s = "                                                                             "
          .substring(0, nameLength);
      System.out.printf(s + "       Lat        Lon    Depth     X(km)     Y(km)     Z(km)%n");

      // iterate over all the vtk datasets
      for (int i = 0; i < dataSets.size(); ++i) {
        // get a reference to the points in the dataSet
        List<double[]> points = dataSets.get(i).getPoints();

        // get a copy of the center of ellipsoid
        double[] center = points.get(points.size() - 2).clone();

        // normalize center to unit vector and save the radius.
        double centerRadius = VectorUnit.normalize(center);
        // convert the centerRadius to centerDepth
        double centerDepth = GeoMath.getEarthRadius(center) - centerRadius;

        // get reference to old offset (usually [0,0,0])
        double[] old_offset = points.get(points.size() - 1).clone();

        // initialize the new offset to zero
        double[] new_offset = new double[3];

        // find azimuth from centroid to center. Will be nan if they are colocated.
        double azimuth = GeoMath.azimuth(centroid, center, Double.NaN);

        if (!Double.isNaN(azimuth)) {
          double distanceKm = GeoMath.angle(centroid, center) * earthRadiusCentroid;
          new_offset[0] = distanceKm * sin(azimuth);
          new_offset[1] = distanceKm * cos(azimuth);
          new_offset[2] = centroidDepth - centerDepth;
        }

        System.out.printf(format, vtkFiles.get(i).getName(), GeoMath.getLatLonString(center),
            centerDepth, new_offset[0], new_offset[1], new_offset[2]);

        // translate the ellipsoid by new_offset minus old_offset
        for (int j = 0; j < points.size() - 2; ++j) {
          double[] point = points.get(j);
          for (int k = 0; k < 3; ++k)
            point[k] += new_offset[k] - old_offset[k];
        }

        // set offset to new offset
        points.set(points.size() - 1, new_offset);

        // write the dataset out to the same file that it was read from.
        dataSets.get(i).write(new File(outputDir, vtkFiles.get(i).getName()));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private static void findVTKFiles(File file, Collection<File> files) {
    if (file.isDirectory())
      for (File f : file.listFiles())
        findVTKFiles(f, files);
    else if (file.getName().toLowerCase().endsWith("vtk"))
      files.add(file);
  }
}
