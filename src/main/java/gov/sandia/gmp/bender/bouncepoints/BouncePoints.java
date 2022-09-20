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
package gov.sandia.gmp.bender.bouncepoints;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;

public class BouncePoints
{
static public final double[] bouncePointDistance = new double[] {
  0.000,   2.000,   4.000,   6.000,   8.000,  10.000,  12.000,  14.000,  16.000,  18.000,
 20.000,  22.000,  24.000,  26.000,  28.000,  30.000,  32.000,  34.000,  36.000,  38.000,
 40.000,  42.000,  44.000,  46.000,  48.000,  50.000,  52.000,  54.000,  56.000,  58.000,
 60.000,  62.000,  64.000,  66.000,  68.000,  70.000,  72.000,  74.000,  76.000,  78.000,
 80.000,  82.000,  84.000,  86.000,  88.000,  90.000,  92.000,  94.000,  96.000,  98.000,
100.000, 102.000, 104.000, 106.000, 108.000, 110.000, 112.000, 114.000, 116.000, 118.000,
120.000, 122.000, 124.000, 126.000, 128.000, 130.000, 132.000, 134.000, 136.000, 138.000,
140.000, 142.000, 144.000, 146.000, 148.000, 150.000, 152.000, 154.000, 156.000, 158.000,
160.000, 162.000, 164.000, 166.000, 168.000, 170.000, 172.000, 174.000, 176.000, 178.000,
180.000};

static public final double[] bouncePointDepth = new double[] {
  0.000,  10.000,  20.000,  30.000,  40.000,  50.000,  60.000,  70.000,  80.000,  90.000,
100.000, 110.000, 120.000, 130.000, 140.000, 150.000, 160.000, 170.000, 180.000, 190.000,
200.000, 210.000, 220.000, 230.000, 240.000, 250.000, 260.000, 270.000, 280.000, 290.000,
300.000, 310.000, 320.000, 330.000, 340.000, 350.000, 360.000, 370.000, 380.000, 390.000,
400.000, 410.000, 420.000, 430.000, 440.000, 450.000, 460.000, 470.000, 480.000, 490.000,
500.000, 510.000, 520.000, 530.000, 540.000, 550.000, 560.000, 570.000, 580.000, 590.000,
600.000, 610.000, 620.000, 630.000, 640.000, 650.000, 660.000, 670.000, 680.000, 690.000,
700.000};

static public double[][] getBouncePoints(SeismicPhase phase)
{
	switch (phase)
	{
	case pP:
		return BouncePoints_pP.bouncePoints;
	case PP:
		return BouncePoints_bigPP.bouncePoints;
	case sP:
		return BouncePoints_sP.bouncePoints;
	case SP:
		return BouncePoints_bigSP.bouncePoints;
	case pS:
		return BouncePoints_pS.bouncePoints;
	case PS:
		return BouncePoints_bigPS.bouncePoints;
	case sS:
		return BouncePoints_sS.bouncePoints;
	case SS:
		return BouncePoints_bigSS.bouncePoints;
	default:
		return null;
	}
}

static public final double[][] bouncePoints_pP = BouncePoints_pP.bouncePoints;
static public final double[][] bouncePoints_PP = BouncePoints_bigPP.bouncePoints;

static public final double[][] bouncePoints_sP = BouncePoints_sP.bouncePoints;
static public final double[][] bouncePoints_SP = BouncePoints_bigSP.bouncePoints;

static public final double[][] bouncePoints_pS = BouncePoints_pS.bouncePoints;
static public final double[][] bouncePoints_PS = BouncePoints_bigPS.bouncePoints;

static public final double[][] bouncePoints_sS = BouncePoints_sS.bouncePoints;
static public final double[][] bouncePoints_SS = BouncePoints_bigSS.bouncePoints;

}
