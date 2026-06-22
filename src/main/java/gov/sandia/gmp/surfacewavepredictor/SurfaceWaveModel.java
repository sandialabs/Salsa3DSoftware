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
package gov.sandia.gmp.surfacewavepredictor;

import static gov.sandia.gmp.util.globals.Globals.extractsubarray;
import static gov.sandia.gmp.util.globals.Globals.hunt;
import static gov.sandia.gmp.util.globals.Globals.interpolate;
import static gov.sandia.gmp.util.globals.Globals.polint;
import static gov.sandia.gmp.util.globals.Globals.subarrayvalues;
import static java.lang.Math.PI;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.surfacewavepredictor.SurfaceWavePredictor.SurfaceWavePredictionMethod;
import gov.sandia.gmp.util.mapprojection.RobinsonProjection;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.vector.GeoMath;
import gov.sandia.gmp.util.vtk.VTKCell;
import gov.sandia.gmp.util.vtk.VTKCellType;
import gov.sandia.gmp.util.vtk.VTKDataSet;

/**
 * 
 */
abstract public class SurfaceWaveModel {

  public static final double[] northPole = new double[] {0, 0, 1};
  public static final double TWO_PI = 2 * PI;
  public static boolean DEBUG = false;

  public static final EnumSet<SeismicPhase> supportedPhases =
      EnumSet.of(SeismicPhase.LR, SeismicPhase.LQ);

  /**
   * Array of period values loaded from the velocity file.
   */
  protected double[] periods;

  protected SeismicPhase phase;

  /**
   * Grid spacing in radians.
   */
  protected double spacing;

  public SurfaceWaveModel() {}

  public static EnumMap<SeismicPhase, SurfaceWaveModel> getSurfaceWaveModels(File modelDirectory)
      throws Exception {
    EnumMap<SeismicPhase, SurfaceWaveModel> models =
        new EnumMap<SeismicPhase, SurfaceWaveModel>(SeismicPhase.class);

    // See if the modelDirectory contains an old fashion LP_grid model.
    for (SeismicPhase phase : supportedPhases) {
      File gridFile = new File(modelDirectory, "LP_grid." + phase.name());
      if (gridFile.exists()) {
        File velFile = new File(modelDirectory, "LP_vel." + phase.name());
        if (velFile.exists())
          models.put(phase, new SurfaceWaveModelLP(modelDirectory, phase));
      }
    }
    if (models.size() == supportedPhases.size())
      return models;

    // Search for models in GeoTessModelSurfaceWaves format
    for (File f : modelDirectory.listFiles()) {
      try {
        SurfaceWaveModelGeoTess model = new SurfaceWaveModelGeoTess(f);
        if (supportedPhases.contains(model.getPhase()))
          models.put(model.getPhase(), model);
      } catch (Exception ex) {
      }
    }
    if (models.size() == supportedPhases.size())
      return models;

    // Search for a model in plain GeoTessModel format. Must satisfy constraints on file
    // names and attribute names. Periods are deduced from attribute names.
    File f = new File(modelDirectory, "rayleigh_groupvel_litho1.geotess");
    if (f.exists())
      models.put(SeismicPhase.LR, new SurfaceWaveModelGeoTess(f, "LR"));

    f = new File(modelDirectory, "love_groupvel_litho1.geotess");
    if (f.exists())
      models.put(SeismicPhase.LQ, new SurfaceWaveModelGeoTess(f, "LQ"));

    return models;
  }

  public SeismicPhase getPhase() {
    return phase;
  }

  /**
   * Find the travel time along a great circle between two points.
   * 
   * @param lat1 in degrees
   * @param lon1 in degrees
   * @param lat2 in degrees
   * @param lon2 in degrees
   * @param period in seconds
   * @return travel time in seconds
   * @throws Exception
   */
  public double getTravelTime(double lat1, double lon1, double lat2, double lon2, double period,
      SurfaceWavePredictionMethod predictionMethod) throws Exception {
    return getTravelTime(new GreatCircle(lat1, lon1, lat2, lon2, true), period, predictionMethod);
  }

  /**
   * Find the travel time along a great circle
   * 
   * @param greatcircle
   * @param period in seconds
   * @return travel time in seconds
   * @throws Exception
   */
  public double getTravelTime(GreatCircle greatcircle, double period,
      SurfaceWavePredictionMethod predictionMethod) throws Exception {
    return getTravelTime(greatcircle, period, new double[] {16.6666666666667, 20.0,
        22.2222222222222, 25.0, 28.5714285714286, 33.3333333333333, 40.0, 50.0}, predictionMethod);
  }

  /**
   * Find the travel time along a great circle
   * 
   * @param greatcircle
   * @param period
   * @param requestedPeriods
   * @param predictionMethod
   * @return
   * @throws Exception
   */
  public double getTravelTime(GreatCircle greatcircle, double period, double[] requestedPeriods,
      SurfaceWavePredictionMethod predictionMethod) throws Exception {

    if (predictionMethod == SurfaceWavePredictionMethod.SIMPLE)
      return pathIntegral(greatcircle, new double[] {period})[0];
    else {
      // source receiver distance in km
      double delta = greatcircle.getDistance() * 6371.;

      // find a subarray of requestedPeriods of length 2 that encompasses the requested period
      double[] requestedPeriods_subarray = subarrayvalues(requestedPeriods, period, 2);

      // find the indices in the model periods array that span the 2 requestedPeriods, with some
      // padding
      int p0 = hunt(periods, requestedPeriods_subarray[0]) - 1;
      int p1 = hunt(periods, requestedPeriods_subarray[requestedPeriods_subarray.length - 1]) + 2;

      // extract the subarray of relevant periods
      double[] periods_subarray = extractsubarray(periods, p0, p1 - p0 + 1);

      // compute travel times for all periods in subarray along the given path
      double[] tt_subarray = pathIntegral(greatcircle, periods_subarray);

      // compute velocities in km/sec as function of period.
      double[] velocity_subarray = new double[periods_subarray.length];
      for (int i = 0; i < periods_subarray.length; ++i)
        velocity_subarray[i] = delta / tt_subarray[i];

      if (DEBUG) {
        System.out.printf("lat1 = %f%n", GeoMath.getLatDegrees(greatcircle.getFirst()));
        System.out.printf("lon1 = %f%n", GeoMath.getLonDegrees(greatcircle.getFirst()));
        System.out.printf("lat2 = %f%n", GeoMath.getLatDegrees(greatcircle.getLast()));
        System.out.printf("lon2 = %f%n", GeoMath.getLonDegrees(greatcircle.getLast()));
        System.out.printf("period = %f%n", period);
        System.out.println();
        System.out.printf("delta = %f km%n", delta);
        System.out.printf("requested periods = %s%n", Arrays.toString(requestedPeriods_subarray));
        System.out.printf("model periods = %s%n", Arrays.toString(periods_subarray));
        System.out.printf("travel times at model periods (LP_Trace_Ray) = %s%n",
            Arrays.toString(tt_subarray));
        System.out.printf("velocities at model periods = %s%n", Arrays.toString(velocity_subarray));
        System.out.println();
        System.out.println("====================================================");
        System.out.println("quadratic interpolation of velocities at 2 requested periods");
        System.out.println();
      }

      // use quadratic interpolation to interpolate velocities at the 2 requestedPeriods
      double[] v_requested = new double[requestedPeriods_subarray.length];
      int[] m;
      for (int i = 0; i < requestedPeriods_subarray.length; ++i) {
        // extract the indices of 3 periods in the periods_subarray that encompass
        // requestedPeriod[i].

        int k = 1;
        while (requestedPeriods_subarray[i] > periods_subarray[k])
          ++k;

        if (requestedPeriods_subarray[i] == periods_subarray[k]) {
          v_requested[i] = velocity_subarray[k];
          m = new int[] {k};
        } else {
          m = new int[] {k - 1, k, k + 1};

          // m = subarrayindices(periods_subarray, requestedPeriods_subarray[i], 3);

          double[] sub_p = extractsubarray(periods_subarray, m);
          double[] sub_v = extractsubarray(velocity_subarray, m);

          // extract the 3 periods and 3 velocities from the subarrays and use quadratic
          // interpolation
          // to interpolate velocity values at the requestedPeriod
          v_requested[i] = polint(sub_p, sub_v, requestedPeriods_subarray[i], false);
        }

        // v_requested[i] = quadinterp2(toFloat(periods_subarray), toFloat(velocity_subarray),
        // (float)requestedPeriods_subarray[i]);
        // v_requested[i] = quadinterp2(periods_subarray, velocity_subarray,
        // requestedPeriods_subarray[i]);

        // v_requested[i] = LP_quadinterp_f(periods_subarray, velocity_subarray, 3,
        // requestedPeriods_subarray[i]);

        if (DEBUG) {
          System.out.printf("requested period = %f%n", requestedPeriods_subarray[i]);
          System.out.printf("indices in model periods array = %s%n", Arrays.toString(m));
          System.out.printf("model periods = %s%n",
              Arrays.toString(extractsubarray(periods_subarray, m)));
          System.out.printf("velocities at model periods = %s%n",
              Arrays.toString(extractsubarray(velocity_subarray, m)));
          System.out.printf("velocity at requested period = %f%n", v_requested[i]);
          System.out.println();
        }

      }

      // use linear interpolation to find the velocity at the input period
      double v = interpolate(requestedPeriods_subarray, v_requested, period);
      // compute travel time by dividing distance by velocity.
      double tt = delta / v;

      if (DEBUG) {
        System.out.println("====================================================");
        System.out.println();
        System.out.printf("velocity at period %f = %f%n", period, v);
        System.out.printf("travel time at period %f = %f%n", period, tt);
        System.out.println();
      }

      return tt;
    }
  }

  /**
   * Compute the path integral (travel time in seconds) for the great circle using phase velocities
   * at the specified period(s).
   * 
   * @param path
   * @param period in seconds
   * @return travel time of the ray path in seconds
   * @throws Exception
   */
  public double[] pathIntegral(GreatCircle path, double... periods) throws Exception {
    return pathIntegral(path, periods, null, null);
  }

  /**
   * Compute the path integral (travel time in seconds) for the great circle using phase velocities
   * at the specified period(s).
   * 
   * @param path
   * @param periods in seconds
   * @param points (optional) if this array is not null it will be cleared and populated with the
   *        points along the ray path that were used to compute the path integral. These points will
   *        reside along either the colatitudes or the longitudes of the grid.
   * @param pathVelocities (optional) if this array is not null then it will be cleared and
   *        populated with the phase velocities used to compute the path integral. The phase
   *        velocity values are interpolated at the center of each path interval.
   *        pathVelocities.size() will equal points.size()-1.
   * @return travel time of the ray path in seconds
   * @throws Exception
   */
  abstract public double[] pathIntegral(GreatCircle path, double[] periods,
      ArrayList<double[]> points, ArrayList<double[]> pathVelocities) throws Exception;

  public double[] getPeriods() {
    return periods;
  }

  public int getNPeriods() {
    return periods.length;
  }

  public double getPeriod(int i) {
    return periods[i];
  }

  abstract public void close() throws Exception;

  /**
   * Grid spacing in radians
   * 
   * @return
   */
  public double getSpacing() {
    return spacing;
  }

  public void vtk(File dir, String baseName, GreatCircle path) throws Exception {
    dir.mkdir();
    vtkGrid(new File(dir, baseName + "_grid.vtk"));
    vtkPath(new File(dir, baseName + "_path.vtk"), path);
    vtkIntersections(new File(dir, baseName + "_intersections.vtk"), path);
  }

  /**
   * 
   * @param outputFile
   * @param lat in degrees
   * @param lon in degrees
   * @throws Exception
   */
  public void vtkPath(File outputFile, double lat1Degrees, double lon1Degrees, double lat2Degrees,
      double lon2Degrees) throws Exception {
    vtkPath(outputFile, new GreatCircle(GeoMath.getVectorDegrees(lat1Degrees, lon1Degrees),
        GeoMath.getVectorDegrees(lat2Degrees, lon2Degrees)));
  }

  /**
   * 
   * @param outputFile
   * @param path
   * @throws Exception
   */
  public void vtkPath(File outputFile, GreatCircle path) throws Exception {

    ArrayList<double[]> intersections =
        path.getPoints((int) Math.ceil(Math.toDegrees(path.getDistance())), false);


    ArrayList<double[]> points = new ArrayList<double[]>(intersections.size());
    ArrayList<Integer> ids = new ArrayList<Integer>(intersections.size());

    int count = 0;
    for (int i = 1; i < intersections.size(); ++i) {
      GreatCircle gc = new GreatCircle(intersections.get(i - 1), intersections.get(i));
      ArrayList<double[]> pts =
          gc.getPoints((int) Math.ceil(Math.toDegrees(path.getDistance())), false);


      for (double[] pt : pts) {
        ids.add(count++);
        points.add(pt);
      }
    }

    // generate list of cells
    List<VTKCell> cells = new ArrayList<>();
    cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, ids));

    // write the vtk file.
    VTKDataSet.write(outputFile, points, cells);
  }


  /**
   * 
   * @param outputFile
   * @param paths
   * @throws Exception
   */
  public void vtkPath(File outputFile, Collection<GreatCircle> paths) throws Exception {
    ArrayList<double[]> points = new ArrayList<>();
    List<VTKCell> cells = new ArrayList<>();

    int id = 0;
    for (GreatCircle path : paths) {
      ArrayList<double[]> p =
          path.getPoints((int) Math.ceil(Math.toDegrees(path.getDistance())), false);
      points.addAll(p);
      ArrayList<Integer> ids = new ArrayList<Integer>();
      for (int i = 0; i < p.size(); ++i)
        ids.add(id++);

      // vtk poly line
      cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, ids));

    }

    // write the vtk file.
    VTKDataSet.write(outputFile, points, cells);
  }

  public void vtkRobinsonPath(File outputFile, GreatCircle path, double centerLon)
      throws Exception {
    RobinsonProjection map = new RobinsonProjection(centerLon);

    ArrayList<double[]> points = new ArrayList<>();
    List<VTKCell> cells = new ArrayList<>();

    int id = 0;
    ArrayList<double[]> p =
        path.getPoints((int) Math.ceil(Math.toDegrees(path.getDistance())) / 10, false);

    ArrayList<ArrayList<double[]>> more = map.project(p);

    for (ArrayList<double[]> x : more) {
      points.addAll(x);
      ArrayList<Integer> ids = new ArrayList<Integer>();
      for (int i = 0; i < x.size(); ++i)
        ids.add(id++);

      // vtk poly line
      cells.add(new VTKCell(VTKCellType.VTK_POLY_LINE, ids));
    }

    // write the vtk file.
    VTKDataSet.write(outputFile, points, cells);
  }

  abstract public double[] getVelocityRange();

  /**
   * 
   * @param outputFile
   * @param geographic lat in degrees
   * @param geographic lon in degrees
   * @throws Exception
   */
  abstract public void vtkIntersections(File outputFile, double lat1Degrees, double lon1Degrees,
      double lat2Degrees, double lon2Degrees) throws Exception;

  abstract public void vtkIntersections(File outputFile, GreatCircle path) throws Exception;

  abstract public void vtkIntersections(File outputFile, ArrayList<double[]> intersections)
      throws Exception;

  abstract public void vtkGrid(File outputFile) throws Exception;

  abstract public void vtkRobinson(File dir, String baseName, GreatCircle path, double centerLon)
      throws Exception;

  abstract public void vtkRobinsonGrid(File outputFile, double centerLon) throws Exception;

  abstract public void vtkRobinsonIntersections(File outputFile, GreatCircle path, double centerLon)
      throws Exception;

  abstract public void vtkRobinsonIntersections(File outputFile, ArrayList<double[]> intersections,
      double centerLon) throws Exception;

}
