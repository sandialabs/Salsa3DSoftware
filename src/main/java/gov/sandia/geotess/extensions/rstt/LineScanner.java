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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * A simple, yet fast, ascii file scanner that handles empty lines and lines
 * with comments and lines with trailing comments.
 *
 * @author jrhipp
 *
 */
public class LineScanner {
	/**
	 * The maximum size of a StringBuilder used to assemble the input into an
	 * instance of a Java Scanner object.
	 */
	private static final int builderSize = 1000 * 8192;

	/**
	 * The maximum size of the input String argument to a Scanner object. This is
	 * slightly less than the builder to prevent the StringBuilder from reallocating
	 * more memory should builderSize be exceeded.
	 */
	private static final int scannerSize = builderSize - 8192;

	/**
	 * The default comment string.
	 */
	private static String commentString = "#";

	/*
	 * The last line read for input into the current Scanner (lineScanner).
	 */
	private String line;

	/**
	 * The current Scanner instance providing results to the caller.
	 */
	private Scanner lineScanner;

	/**
	 * The input BufferedReader providing input into the Scanner.
	 */
	private BufferedReader input;

	/**
	 * Initialize a new LineScanner with input.
	 *
	 * @param readerInput The input BufferedReader.
	 */
	public LineScanner(BufferedReader readerInput) {
		input = readerInput;
	}

	/**
	 * Initialize a new LineScanner with input.
	 *
	 * @param readerInput The input BufferedReader.
	 * @param cmntStr     The new comment string.
	 */
	public LineScanner(BufferedReader readerInput, String cmntStr) {
		input = readerInput;
		commentString = cmntStr;
	}

	/**
	 * Sets the comment string.
	 *
	 * @param cmntStr The new comment string.
	 */
	public static void setCommentString(String cmntStr) {
		commentString = cmntStr;
	}

	/**
	 * Returns true if this LineScanner has more data.
	 *
	 * @return True if this LineScanner has more data.
	 * @throws IOException
	 */
	public boolean hasNext() throws IOException {
		if (input.ready())
			return true;
		else
			return lineScanner.hasNext();
	}

	/**
	 * Returns the next token in lineScanner as a double. This method checks the
	 * current lineScanner instance for more input and creates a new instance with
	 * the next set of input if the current scanner is empty.
	 *
	 * @return The next scanner token as a double.
	 * @throws IOException
	 */
	public double nextDouble() throws IOException {
		checkScanner();
		return lineScanner.nextDouble();
	}

	/**
	 * Returns the next token in lineScanner as a float. This method checks the
	 * current lineScanner instance for more input and creates a new instance with
	 * the next set of input if the current scanner is empty.
	 *
	 * @return The next scanner token as a float.
	 * @throws IOException
	 */
	public float nextFloat() throws IOException {
		checkScanner();
		return lineScanner.nextFloat();
	}

	/**
	 * Returns the next token in lineScanner as an long. This method checks the
	 * current lineScanner instance for more input and creates a new instance with
	 * the next set of input if the current scanner is empty.
	 *
	 * @return The next scanner token as an long.
	 * @throws IOException
	 */
	public long nextLong() throws IOException {
		checkScanner();
		return lineScanner.nextLong();
	}

	/**
	 * Returns the next token in lineScanner as an int. This method checks the
	 * current lineScanner instance for more input and creates a new instance with
	 * the next set of input if the current scanner is empty.
	 *
	 * @return The next scanner token as an int.
	 * @throws IOException
	 */
	public int nextInt() throws IOException {
		checkScanner();
		return lineScanner.nextInt();
	}

	/**
	 * Returns the next token in lineScanner as an short. This method checks the
	 * current lineScanner instance for more input and creates a new instance with
	 * the next set of input if the current scanner is empty.
	 *
	 * @return The next scanner token as an short.
	 * @throws IOException
	 */
	public short nextShort() throws IOException {
		checkScanner();
		return lineScanner.nextShort();
	}

	/**
	 * Returns the next token in lineScanner as an byte. This method checks the
	 * current lineScanner instance for more input and creates a new instance with
	 * the next set of input if the current scanner is empty.
	 *
	 * @return The next scanner token as an byte.
	 * @throws IOException
	 */
	public byte nextByte() throws IOException {
		checkScanner();
		return lineScanner.nextByte();
	}

	/**
	 * Returns the next token in lineScanner as a boolean. This method checks the
	 * current lineScanner instance for more input and creates a new instance with
	 * the next set of input if the current scanner is empty.
	 *
	 * @return The next scanner token as a boolean.
	 * @throws IOException
	 */
	public boolean nextBoolean() throws IOException {
		checkScanner();
		return lineScanner.nextBoolean();
	}

	/**
	 * Returns the next token in lineScanner as a string. This method checks the
	 * current lineScanner instance for more input and creates a new instance with
	 * the next set of input if the current scanner is empty.
	 *
	 * @return The next scanner token as a string.
	 * @throws IOException
	 */
	public String nextString() throws IOException {
		checkScanner();
		return lineScanner.next();
	}

	/**
	 * Returns the last line read from input into the current lineScanner.
	 *
	 * @return The last line read from input into the current lineScanner.
	 * @throws IOException
	 */
	public String getLastLine() throws IOException {
		return line;
	}

	/**
	 * Checks the current Scanner instance for more input availability. If the
	 * Scanner is null (never created) or empty, a new Scanner is created and filled
	 * with the next set of input.
	 *
	 * @throws IOException
	 */
	private void checkScanner() throws IOException {
		if ((lineScanner == null) || !lineScanner.hasNext())
			buildScanner();
	}

	/**
	 * Produces a Scanner using lines from the entire file where the size of the
	 * returned Scanner has between scannerSize and builderSize characters. The
	 * Scanner is formed from file lines with all comments, partial comments, and
	 * empty lines removed.
	 *
	 * @return A new Scanner instance (lineScanner).
	 * @throws IOException
	 */
	private void buildScanner() throws IOException {
		// allocate sufficient space for the builder ... loop over all lines

		StringBuilder strBuilder = new StringBuilder(builderSize);
		while ((line = input.readLine()) != null) {
			// strip leading and trailing space and see if line is empty or a comment
			// line

			line = line.trim();
			if (!((line.equals("")) || (line.substring(0, commentString.length()).equals(commentString)))) {
				// This is a data line ... add line to StringBuilder if no trailing
				// comment is present. Otherwise, add the first part of the line with
				// the trailing comment portion stripped.

				int embeddedCommentIndex = line.indexOf(commentString);
				if (embeddedCommentIndex == -1)
					strBuilder.append(line);
				else
					strBuilder.append(line.substring(0, embeddedCommentIndex));

				// add a space separator ... break if the StringBuilder length exceeds
				// scannerSize

				strBuilder.append(" ");
				if (strBuilder.length() > scannerSize)
					break;
			}
		}

		// done ... return a scanner with the entire StringBuilder contents as a
		// parse ready string

		lineScanner = new Scanner(strBuilder.toString());
	}
}
