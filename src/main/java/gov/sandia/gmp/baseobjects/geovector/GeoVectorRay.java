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
package gov.sandia.gmp.baseobjects.geovector;

import java.util.Objects;
import gov.sandia.gmp.baseobjects.globals.WaveType;

public class GeoVectorRay extends GeoVector {

  private static final long serialVersionUID = -5228143932572187407L;

  private int layerIndex = -1;

  /**
   * Either P or S
   */
  private WaveType waveType;

  public GeoVectorRay() {
    super();
  }

  public GeoVectorRay(double[] unitVector, double radius, int layerIndex, WaveType waveType) {
    super(unitVector, radius);
    this.layerIndex = layerIndex;
    this.waveType = waveType;
  }

  public GeoVectorRay(double[] vector3d, int layerIndex, WaveType waveType) {
    super(vector3d);
    this.layerIndex = layerIndex;
    this.waveType = waveType;
  }

  public GeoVectorRay(String s) {
    super();
    String[] tokens = s.trim().replaceAll(",", " ").split("\\s+");
    super.setGeoVector(Double.valueOf(tokens[0]), Double.valueOf(tokens[1]),
        Double.valueOf(tokens[2]), true);
    this.layerIndex = Integer.valueOf(tokens[3]);
    this.waveType = WaveType.valueOf(tokens[4]);
  }

  public int getLayerIndex() {
    return layerIndex;
  }

  public GeoVectorRay setLayerIndex(int layerIndex) {
    this.layerIndex = layerIndex;
    return this;
  }

  public WaveType getWaveType() {
    return waveType;
  }

  public GeoVectorRay setWaveType(WaveType waveType) throws Exception {
    if (waveType == WaveType.P || waveType == WaveType.S)
      this.waveType = waveType;
    else
      throw new Exception("waveType = " + waveType.toString() + " but must be either P or S");
    return this;
  }

  @Override
  public String toString() {
    return super.toString() + String.format(", %2d, %s", layerIndex, waveType);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Objects.hash(layerIndex, waveType);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (getClass() != obj.getClass())
      return false;
    GeoVectorRay other = (GeoVectorRay) obj;
    if (waveType != other.waveType || layerIndex != other.layerIndex)
      return false;
    return super.equals(obj);
  }
}
