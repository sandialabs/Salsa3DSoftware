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
package gov.sandia.gmp.bender.ray;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;

/**
 * @author Benjamin Lawry (bjlawry@sandia.gov)
 * created on 04/29/2022
 *
 */
public class RaySegmentInfo {
  private ArrayList<double[]> points;
  private ArrayListDouble radii;
  private GeoAttributes waveType;
  private int layerIndex;
  
  public RaySegmentInfo(List<GeoTessPosition> pos, GeoAttributes wType,
      int layerIdx) {
    if(pos == null) throw new NullPointerException();
    if(wType == null) throw new NullPointerException();
    
    points = pos.stream().map(GeoTessPosition::getVector)
        .collect(Collectors.toCollection(ArrayList::new));
    
    radii = new ArrayListDouble(pos.size());
    for(GeoTessPosition p : pos) radii.add(p.getRadius());
    
    waveType = wType;
    layerIndex = layerIdx;
  }

  public ArrayList<double[]> getPoints() {
    return points;
  }

  public void setPoints(ArrayList<double[]> points) {
    this.points = points;
  }

  public GeoAttributes getWaveType() {
    return waveType;
  }

  public void setWaveType(GeoAttributes waveType) {
    this.waveType = waveType;
  }

  public int getLayerIndex() {
    return layerIndex;
  }

  public void setLayerIndex(int layerIndex) {
    this.layerIndex = layerIndex;
  }
  
  /**
   * @return the radii
   */
  public ArrayListDouble getRadii() {
    return radii;
  }

  /**
   * @param radii the radii to set
   */
  public void setRadii(ArrayListDouble radii) {
    this.radii = radii;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + layerIndex;
    result = prime * result + ((points == null) ? 0 : points.hashCode());
    result = prime * result + ((radii == null) ? 0 : radii.hashCode());
    result = prime * result + ((waveType == null) ? 0 : waveType.hashCode());
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
    RaySegmentInfo other = (RaySegmentInfo) obj;
    if (layerIndex != other.layerIndex)
      return false;
    if (points == null) {
      if (other.points != null)
        return false;
    } else if (!points.equals(other.points))
      return false;
    if (radii == null) {
      if (other.radii != null)
        return false;
    } else if (!radii.equals(other.radii))
      return false;
    if (waveType != other.waveType)
      return false;
    return true;
  }
}
