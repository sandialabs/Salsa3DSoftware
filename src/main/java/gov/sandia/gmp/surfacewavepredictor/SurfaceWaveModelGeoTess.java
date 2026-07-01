/**
 * Copyright 2026 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the
 * terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this
 * software.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.containers.Tuple;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerDouble;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;

/**
 * 
 */
public class SurfaceWaveModelGeoTess extends SurfaceWaveModel {

  private GeoTessModelSurfaceWaves model;

  private double[] indeces;

  /**
   * Retrieve a SurfaceWaveModelGeoTess which has a GeoTessSurfaceWaveModel that is loaded from a
   * file.
   * 
   * @param file File containing a SurfaceWaveModelGeoTess object.
   * @throws Exception
   */
  public SurfaceWaveModelGeoTess(File file) throws Exception {
    model = new GeoTessModelSurfaceWaves(file);

    phase = SeismicPhase.valueOf(model.getPhase());
    periods = model.getPeriods();

    indeces = new double[periods.length];
    for (int i = 0; i < periods.length; ++i)
      indeces[i] = i;

    spacing = Math.toRadians(64. / Math.pow(2, model.getGrid().getNLevels(0) - 1));
  }

  /**
   * Retrieve a SurfaceWaveModelGeoTess which has a GeoTessSurfaceWaveModel. The
   * GeoTessSurfaceWaveModel is loaded from a File which contains a plain GeoTessModel and the
   * periods are deduced from the Attribute names.
   * 
   * @param file
   * @param phase the name of the phase supported by this model
   * @throws IOException
   */
  public SurfaceWaveModelGeoTess(File file, String phase) throws Exception {

    // read plain geotess file
    GeoTessModel geotessModel = new GeoTessModel(file);

    // expand the plain geotess model to a geotess surface wave model
    // which records phase and periods as well as other info.
    model = new GeoTessModelSurfaceWaves(geotessModel);

    model.setPhase(phase);

    // deduce the periods from the Attribute names.
    // attribute names look like: 0: rayl_sg_10s etc.
    periods = new double[model.getMetaData().getNAttributes()];
    indeces = new double[model.getMetaData().getNAttributes()];
    for (int i = 0; i < model.getMetaData().getNAttributes(); ++i) {
      String[] tokens = model.getMetaData().getAttributeName(i).split("_");
      String speriod = tokens[2].replace("s", "");
      periods[i] = Double.parseDouble(speriod);
      indeces[i] = i;
    }

    model.setPeriods(periods);

    spacing = Math.toRadians(64. / Math.pow(2, model.getGrid().getNLevels(0) - 1));

  }

  /**
   * Compute the path integral (travel time in seconds) for the specified great circle using phase
   * velocities at the specified period(s).
   * 
   * @param path
   * @param periods in seconds
   * @param points (optional) if this array is not null it will be cleared and populated with the
   *        points in the grid that were used to compute the path integral.
   * @param pathVelocities (optional) if this array is not null then it will be cleared and
   *        populated with the phase velocities at the points in the grid used to compute the path
   *        integral. pathVelocities.size() will equal points.size()-1.
   * @return travel time of the ray path in seconds
   * @throws Exception
   */
  @Override
  public double[] pathIntegral(GreatCircle path, double[] inputPeriods, ArrayList<double[]> points,
      ArrayList<double[]> pathVelocities) throws Exception {

    double[] results = new double[inputPeriods.length];

    HashMapIntegerDouble weights = new HashMapIntegerDouble();
    model.getWeights(path, spacing * 0.1, -1., InterpolatorType.LINEAR, weights);

    if (points != null) {
      points = new ArrayList<double[]>(weights.size());
      Tuple<int[], double[]> keyValues = weights.getKeysAndValues();
      int[] indeces = keyValues.first;
      double[] coefficients = keyValues.second;
      for (int i : indeces)
        points.add(model.getVertex(i));
    }

    double index, value, fraction, right;
    int idx;

    for (int i = 0; i < inputPeriods.length; ++i) {
      index = Globals.interpolate(model.getPeriods(), indeces, inputPeriods[i]);
      if (Double.isNaN(index))
        // out-of-range
        value = Double.NaN;
      else {
        idx = (int) index;
        fraction = index - idx;
        value = model.getPathIntegral(idx, weights);
        if (fraction > 1e-6) {
          right = model.getPathIntegral(idx + 1, weights);
          value = value * (1. - fraction) + right * fraction;
        }
      }
      results[i] = value;
    }

    return results;
  }

  public GeoTessModelSurfaceWaves getModel() {
    return model;
  }

  @Override
  public void close() throws Exception {
    if (model != null)
      model.close();
  }

  @Override
  public double[] getVelocityRange() {
    double[] range = new double[] {Double.MAX_VALUE, Double.MIN_VALUE};
    for (int i = 0; i < model.getNPoints(); ++i)
      for (int j = 0; j < model.getNAttributes(); ++j) {
        double v = model.getValueDouble(i, i);
        if (v < range[0])
          range[0] = v;
        if (v > range[1])
          range[1] = v;
      }
    return range;
  }

  @Override
  public void vtkIntersections(File outputFile, double lat1Degrees, double lon1Degrees,
      double lat2Degrees, double lon2Degrees) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void vtkIntersections(File outputFile, GreatCircle path) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void vtkIntersections(File outputFile, ArrayList<double[]> intersections)
      throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void vtkGrid(File outputFile) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void vtkRobinson(File dir, String baseName, GreatCircle path, double centerLon)
      throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void vtkRobinsonGrid(File outputFile, double centerLon) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void vtkRobinsonIntersections(File outputFile, GreatCircle path, double centerLon)
      throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void vtkRobinsonIntersections(File outputFile, ArrayList<double[]> intersections,
      double centerLon) throws Exception {
    // TODO Auto-generated method stub

  }

}
