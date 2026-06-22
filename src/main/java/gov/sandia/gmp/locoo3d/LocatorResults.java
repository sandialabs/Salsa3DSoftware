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
package gov.sandia.gmp.locoo3d;

import java.util.Objects;
import gov.sandia.gmp.baseobjects.Location;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.flinnengdahl.FlinnEngdahlCodes;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.hyperellipse.Ellipse;
import gov.sandia.gmp.baseobjects.hyperellipse.Ellipsoid;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_custom.Azgap;

/**
 * <p>
 * Title: LocOOJava
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 *
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 *
 * @author Sandy Ballard
 * @version 1.0
 */

public class LocatorResults implements Comparable<LocatorResults> {
  Source source;

  /**
   * if property ellipsoidVTK is set, and a 3D error ellipsoid can be calculated, a vtk file is
   * generated with an image of the ellipsoid.
   */
  private String ellipsoidVTK;

  public LocatorResults(Event event) {
    source = event.source;
  }

  public void setResults(Event event) throws Exception {}

  public void setResults(Event event, Exception e) throws Exception {}

  public Location getLocation() {
    return source.getLocation();
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    try {
      if (source.isValid()) {
        buf.append(String.format("Final location for evid: %d   Orid: %d%n%n", source.getEvid(),
            source.getSourceId()));
        Location location = source.getLocation();
        if (location != null) {

          buf.append(String.format(
              "  latitude longitude     depth     origin_time           origin_date_gmt       origin_jdate%n"));
          buf.append(String.format("%10.4f %9.4f %9.3f %15.3f %25s %18d%n%n",
              location.getLatDegrees(), location.getLonDegrees(), location.getDepth(),
              location.getTime(), GMTFormat.GMT_MS.format(GMTFormat.getDate(location.getTime())),
              GMTFormat.getJDate(location.getTime())));

          buf.append(String.format("  geographic region: %s (%d)   seismic region %s (%d)%n%n",
              FlinnEngdahlCodes.getGeoRegionName(location.getLatDegrees(),
                  location.getLonDegrees()),
              FlinnEngdahlCodes.getGeoRegionIndex(location.getLatDegrees(),
                  location.getLonDegrees()),
              FlinnEngdahlCodes.getSeismicRegionName(location.getLatDegrees(),
                  location.getLonDegrees()),
              FlinnEngdahlCodes.getSeismicRegionIndex(location.getLatDegrees(),
                  location.getLonDegrees())));

          buf.append(String.format(
              "  converged  loc_min   Nit Nfunc     M  Nobs  Ndel  Nass  Ndef     sdobs    rms_wr%n"));
          buf.append(String.format("%11b %8b %5d %5d %5d %5d %5d %5d %5d %9.4f %9.4f%n%n",
              source.isValid(), source.hasLocalMinima(), source.getNIterations(), source.getNFunc(),
              source.nFree(), source.getNobs(), source.countRemovedObservations(), source.getNass(),
              source.getNdef(), source.getSdobs(), source.getRMSWeightedResiduals()));


          Azgap azgap = source.getAzgap();
          buf.append(String.format("    az_gap  az_gap_2 station  Nsta   N30  N250%n"));
          buf.append(String.format("%10.4f %9.4f %7s %5d %5d %5d%n%n", azgap.getAzgap1(),
              azgap.getAzgap2(), azgap.getSta(), azgap.getNsta(), azgap.getNsta30(),
              azgap.getNsta250()));

          buf.append(String.format(
              "      conf        type     K   apriori     sigma   kappa_1   kappa_2   kappa_3   kappa_4%n"));
          buf.append(String.format("%10.4f %11s %5s %9.4f %9.4f %9.4f %9.4f %9.4f %9.4f%n%n",
              source.getHyperEllipse().getConfidence(),
              (source.getHyperEllipse().getK() == 0 ? "confidence"
                  : (source.getHyperEllipse().getK() == -1 ? "coverage" : "mixed")),
              (source.getHyperEllipse().getK() == 0 ? "0"
                  : (source.getHyperEllipse().getK() == -1 ? "Inf"
                      : Integer.toString(source.getHyperEllipse().getK()))),
              source.getHyperEllipse().getAprioriStandardError(),
              source.getHyperEllipse().getSigmaSqr(), source.getHyperEllipse().getKappa(1),
              source.getHyperEllipse().getKappa(2), source.getHyperEllipse().getKappa(3),
              source.getHyperEllipse().getKappa(4)));

          if (!source.getFixed()[GMPGlobals.LAT] && !source.getFixed()[GMPGlobals.LON]
              && !source.getFixed()[GMPGlobals.DEPTH])
            try {
              buf.append(String.format("3D Hypocentral uncertainty ellipsoid:%n%n"));

              Ellipsoid ellipsoid = source.getHyperEllipse().getEllipsoid();

              buf.append(String.format("              length      trend     plunge%n"));
              buf.append(
                  String.format("   major: %10.4f %10.4f %10.4f%n", ellipsoid.getMajaxLength(),
                      ellipsoid.getMajaxTrend(), ellipsoid.getMajaxPlunge()));

              buf.append(
                  String.format("   minor: %10.4f %10.4f %10.4f%n", ellipsoid.getMinaxLength(),
                      ellipsoid.getMinaxTrend(), ellipsoid.getMinaxPlunge()));

              buf.append(
                  String.format("   inter: %10.4f %10.4f %10.4f%n%n", ellipsoid.getIntaxLength(),
                      ellipsoid.getIntaxTrend(), ellipsoid.getIntaxPlunge()));

            } catch (Exception e) {
              if (e.getMessage().startsWith("ERROR in Simplex.amoeba()"))
                buf.append("Calculation of ellipsoid failed.\n\n");
              else
                buf.append(e.getMessage() + "\n\n");
            }

          if (!source.getFixed()[GMPGlobals.LAT] && !source.getFixed()[GMPGlobals.LON]) 
            try {
              Ellipse ellipse = source.getHyperEllipse().getEllipse();
              buf.append(String.format("2D Epicentral uncertainty ellipse:%n%n"));
              buf.append(String.format("    smajax    sminax     trend      area%n"));
              buf.append(String.format("%10.4f %9.4f %9.4f %9.2f%n%n", ellipse.getMajaxLength(),
                  ellipse.getMinaxLength(), ellipse.getMajaxTrend(), ellipse.getArea()));
            } catch (Exception e) {
              buf.append("\n" + e.getMessage() + "\n\n\n");
            }
          
          if (!source.getFixed()[GMPGlobals.DEPTH] || !source.getFixed()[GMPGlobals.TIME])           
          try {
            buf.append(String.format("1D linear uncertainties:%n%n"));
            buf.append(String.format("  depth_se   time_se%n"));
            buf.append(String.format("%10.4f %9.4f%n%n", source.getHyperEllipse().getSdepth(),
                source.getHyperEllipse().getStime()));
          } catch (Exception e) {
            buf.append("\n" + e.getMessage() + "\n\n");
          }
          
          if (source.getFixed()[GMPGlobals.LAT] && source.getFixed()[GMPGlobals.LON]
              && source.getFixed()[GMPGlobals.DEPTH] && source.getFixed()[GMPGlobals.TIME])
            buf.append("Location uncertainty is zero\n\n");
          
        } else
          buf.append(String.format("No results available for event orid=%d source.getEvid()=%d%n%n",
              source.getSourceId(), source.getEvid()));
      }
    }

    catch (Exception e) {
      e.printStackTrace();
      buf.append(String.format("%nERROR: %s%n%n", e.getMessage()));
    }

    return buf.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Objects.hash(source.getEvid(), source.getLocation(),
        source.getRMSWeightedResiduals(), source.getSdobs(), source.getSourceId());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LocatorResults other = (LocatorResults) obj;
    return source.getEvid() == other.source.getEvid()
        && Objects.equals(source.getLocation(), other.source.getLocation())
        && Double.doubleToLongBits(source.getRMSWeightedResiduals()) == Double
            .doubleToLongBits(other.source.getRMSWeightedResiduals())
        && Double.doubleToLongBits(source.getSdobs()) == Double
            .doubleToLongBits(other.source.getSdobs())
        && source.getSourceId() == other.source.getSourceId();
  }

  @Override
  public int compareTo(LocatorResults o) {
    if (source.getNobs() == o.source.getNobs())
      return (int) Math
          .signum(source.getSumSQRWeightedResiduals() - o.source.getSumSQRWeightedResiduals());
    return source.getNobs() > o.source.getNobs() ? -1 : 1;
  }

  public String getEllipsoidVTK() {
    return ellipsoidVTK;
  }

}
