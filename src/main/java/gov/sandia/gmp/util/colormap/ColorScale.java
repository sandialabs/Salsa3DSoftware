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
package gov.sandia.gmp.util.colormap;

import java.awt.Color;

/**
 * Creates a set of color gradients which can be interpolated on by this class.  An example
 * use of one is NAME.getColorScale(), which returns a Color[].
 * @author jwvicke
 *
 */
public enum ColorScale
{ WHITE_RED_YELLOW_GREEN(Color.WHITE, Color.RED, Color.YELLOW, Color.GREEN),
  RED_WHITE_BLUE(Color.RED, Color.WHITE, Color.BLUE),
  WHITE_CYAN_PINK_GREEN_RED(Color.WHITE, Color.cyan, Color.pink, Color.green, Color.red),
  WHITE_BLUE_CYAN_GREEN_YELLOW_RED(Color.white, Color.blue, Color.cyan, Color.green, Color.yellow, Color.red),
  RED_YELLOW_GREEN_CYAN_BLUE(Color.red, Color.yellow, Color.green, Color.cyan, Color.blue),
  BLUE_CYAN_GREEN_YELLOW_RED(Color.blue, Color.cyan, Color.green, Color.yellow, Color.red),
  BLUE_CYAN_WHITE_YELLOW_RED(Color.blue, Color.cyan, Color.white, Color.yellow, Color.red),
  RED_YELLOW_WHITE_CYAN_BLUE(Color.RED, Color.yellow, Color.white, Color.cyan, Color.blue);

  private Color[] colors;
  private ColorScale(Color...colors)
  { this.colors = colors;
  }
  
  public Color[] getColorScale()
  { return colors;
  }
  
}
