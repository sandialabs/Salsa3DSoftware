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
package gov.sandia.geotess.extensions.rstt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

/**
 * Base class for UncertaintySLBM and UncertaintyPathDep. Contains the enums to convert phase and
 * attribute strings from numbers-to-strings and strings-to- numbers. Also stores the fields
 * fileName and phaseNum which are common to both derived classes.
 *
 * @author jrhipp
 *
 */
public abstract class Uncertainty {
	/*
	 * Convenience constant for Pn phase
	 */
	public static final int Pn = 0;

	/*
	 * Convenience constant for Sn phase
	 */
	public static final int Sn = 1;

	/*
	 * Convenience constant for Pg phase
	 */
	public static final int Pg = 2;

	/*
	 * Convenience constant for Lg phase
	 */
	public static final int Lg = 3;

	/*
	 * Convenience constant for attribute TT
	 */
	public static final int TT = 0;

	/*
	 * Convenience constant for attribute SH
	 */
	public static final int SH = 1;

	/*
	 * Convenience constant for attribute AZ
	 */
	public static final int AZ = 2;

	/**
	 * Returns the phase string given its index.
	 *
	 * @param phaseIndex The input phase index.
	 * @return The returned phase string.
	 */
	public static String getPhase(int phaseIndex) {
		switch (phaseIndex) {
			case 0:
				return "Pn";
			case 1:
				return "Sn";
			case 2:
				return "Pg";
			case 3:
				return "Lg";
			default:
				return "XX";
		}
	}

	/**
	 * Returns the attribute string given its index.
	 *
	 * @param attributeIndex The input attribute index.
	 * @return The returned attribute string.
	 */
	public static String getAttribute(int attributeIndex) {
		switch (attributeIndex) {
			case 0:
				return "TT";
			case 1:
				return "Sh";
			case 2:
				return "Az";
			default:
				return "XX";
		}
	}

	/**
	 * The returned phase index given the phase string.
	 *
	 * @param phase The input phase string.
	 * @return phase index.
	 */
	public static int getPhase(String phase) {
		if (phase.equals("Pn"))
			return Pn;
		if (phase.equals("Sn"))
			return Sn;
		if (phase.equals("Pg"))
			return Pg;
		if (phase.equals("Lg"))
			return Lg;
		return -1;
	}

	/**
	 * The returned attribute index given the attribute string.
	 *
	 * @param attribute The input attribute string.
	 * @return attribute index.
	 */
	public static int getAttribute(String attribute) {
		if (attribute.equals("TT"))
			return TT;
		if (attribute.equals("Sh"))
			return SH;
		if (attribute.equals("Az"))
			return AZ;
		return -1;
	}

	/**
	 * The Phase number (Pn, Sn, Pg, Lg) for which this UncertaintySLBM object is defined.
	 */
	protected int phaseNum;

	/**
	 * Default constructor
	 */
	public Uncertainty() {
		phaseNum = -1;
	}

	/**
	 * Standard constructor that sets the phase number but nothing else.
	 *
	 * @param phase The input phase number.
	 */
	public Uncertainty(int phase) {
		phaseNum = phase;
	}

	/**
	 * Standard constructor that sets the phase number but nothing else.
	 *
	 * @param phase The input phase string.
	 */
	public Uncertainty(String phase) {
		phaseNum = getPhase(phase);
	}

	public int getPhase() {
		return phaseNum;
	}

	public String getPhaseStr() {
		return getPhase(phaseNum);
	}

	public abstract void readFile(File fileName, boolean readBinary) throws IOException;

	public abstract void readFileAscii(File fileName) throws IOException;

	public abstract void readFileBinary(File fileName) throws IOException;

	protected abstract void readFileAscii(Scanner input) throws IOException;

	protected abstract void readFileBinary(DataInputStream input) throws IOException;

	/*
	 * abstract file writers
	 */
	public abstract void writeFile(File fileName, boolean writeBinary) throws IOException;

	public abstract void writeFileAscii(File fileName) throws IOException;

	public abstract void writeFileBinary(File fileName) throws IOException;

	protected abstract void writeFileAscii(Writer output) throws IOException;

	protected abstract void writeFileBinary(DataOutputStream output) throws IOException;

	/**
	 * Returns false for UncertaintySLBM and true for UncertaintyPathDep.
	 *
	 * @return False for UncertaintySLBM and true for UncertaintyPathDep.
	 */
	public abstract boolean isPathDependent();
}
