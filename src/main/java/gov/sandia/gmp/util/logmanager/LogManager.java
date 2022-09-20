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
package gov.sandia.gmp.util.logmanager;

import java.io.IOException;

public class LogManager
{

	/**
	 * Text alignment enum, used by padString().
	 * 
	 * @author mchang
	 *
	 */
	public enum TextAlignment {
		LEFT_ALIGNED,
		CENTER_ALIGNED,
		RIGHT_ALIGNED
	}

	/**
	 * Prefix string to be prepended to log messages.
	 */
	private String prefix = "";
	
	/**
	 * File output logger which may be setup by calling
	 * setupFileLogs().
	 */
	private FileOutputLog fileLogger = null;
	
	/**
	 * Terminal output logger which may be setup by calling
	 * setupTerminalLog().
	 */
	private TerminalOutputLog terminalLogger = null;
	
	/**
	 * Create a log manager with the given prefix to prepend to log messages.
	 * 
	 * @param logPrefix string prefix to prepend to log messages.
	 */
	public LogManager(String logPrefix) {
		prefix = logPrefix;
	}
	
	/**
	 * Setup file logs and delete existing files.
	 * 
	 * @param errorFilePath path to error log file
	 * @param warningFilePath path to warning log file
	 * @param statusFilePath path to status log file
	 * 
	 * @return true on success, false if there were one or more problems encountered
	 */
	public boolean setupFileLogs(String errorFilePath, String warningFilePath, String statusFilePath) {
		
		// create file logs and overwrite any existing files (without writing a header)
		return setupFileLogs(errorFilePath, warningFilePath, statusFilePath, false);
	}
	
	/**
	 * Setup file logs and only delete existing files if the flag is true.
	 * 
	 * @param errorFilePath path to error log file
	 * @param warningFilePath path to warning log file
	 * @param statusFilePath path to status log file
	 * @param appendFiles true to append any existing files, false to overwrite
	 * 
	 * @return true on success, false if there were one or more problems encountered
	 * 
	 */
	public boolean setupFileLogs(String errorFilePath, String warningFilePath, String statusFilePath, 
			boolean appendFiles) {
		
		// create files without writing a header
		return setupFileLogs(errorFilePath, warningFilePath, statusFilePath, appendFiles, null);
	}
	
	/**
	 * Setup file logs and only delete existing files if the flag is true.  Also write
	 * the given header to the files if non-null.
	 * 
	 * @param errorFilePath path to error log file
	 * @param warningFilePath path to warning log file
	 * @param statusFilePath path to status log file
	 * @param appendFiles true to append any existing files, false to overwrite
	 * @param header a string header to be written to each of the output files
	 * 
	 * 
	 * @return true on success, false if there were one or more problems encountered
	 * 
	 */
	public boolean setupFileLogs(String errorFilePath, String warningFilePath, String statusFilePath, 
			boolean appendFiles, String header) {
	
		try {
			fileLogger = new FileOutputLog(prefix, errorFilePath, warningFilePath, statusFilePath, appendFiles, header);
		} catch (IOException e) {
			// the constructor throws a generic exception indicating one or more problems occurred, but execution can 
			// still continue as the FileOutputLog handles errors internally, so simply return false to indicate there
			// was an issue - the caller can decide whether to ignore the error
			return false;
		}
		
		return true;
	}
	
	/**
	 * Setup window terminal logger.
	 * 
	 * @return true on success
	 */
	public boolean setupTerminalLog() {
		terminalLogger = new TerminalOutputLog(prefix);
		return true;
	}
	
	/**
	 * Output error message to the output methods that have been setup.
	 */
	public void outputError(String msg) {
		if(fileLogger != null)
			fileLogger.outputError(msg);
		if(terminalLogger != null)
			terminalLogger.outputError(msg);
	}

	/**
	 * Output status message to the output methods that have been setup.
	 */
	public void outputStatus(String msg) {
		if(fileLogger != null)
			fileLogger.outputStatus(msg);
		if(terminalLogger != null)
			terminalLogger.outputStatus(msg);
	}

	/**
	 * Output warning message to the output methods that have been setup.
	 */
	public void outputWarning(String msg) {
		if(fileLogger != null)
			fileLogger.outputWarning(msg);
		if(terminalLogger != null)
			terminalLogger.outputWarning(msg);
	}


	/**
	 * Output error message and stack trace to the output methods that have been setup.
	 */
	public void outputError(Exception exception) {
		if(fileLogger != null)
			fileLogger.outputError(exception);
		if(terminalLogger != null)
			terminalLogger.outputError(exception);
	}

	/**
	 * Output status message and stack trace to the output methods that have been setup.
	 */
	public void outputStatus(Exception exception) {
		if(fileLogger != null)
			fileLogger.outputStatus(exception);
		if(terminalLogger != null)
			terminalLogger.outputStatus(exception);
	}

	/**
	 * Output warning message and stack trace to the output methods that have been setup.
	 */
	public void outputWarning(Exception exception) {
		if(fileLogger != null)
			fileLogger.outputWarning(exception);
		if(terminalLogger != null)
			terminalLogger.outputWarning(exception);
	}
	
	/**
	 * Constructs a new string with extra space padding, such that the returned string is
	 * guaranteed to be of length lineWidth, with the desired text alignment.  However,
	 * if the input text is longer than the desired line width, the input text is 
	 * immediately returned unmodified. 
	 * 
	 * @param text text used to construct new string with extra padding 
	 * @param lineWidth desired total line width
	 * @param alignment desired text alignment (left, right, or center)
	 * @return new string with extra space padding, but if input text is longer than
	 * lineWidth, then the input text is returned unmodified
	 *
	 */
	public static String padString(String text, int lineWidth, TextAlignment alignment) 
	{
		// return copy of input string if it is already wider than desired line width
		
		if(text.length() > lineWidth)
			return new String(text);
		
		// first determine how many blank pad characters are needed 
		// to fill the desired line width
		
		int numPad = lineWidth - text.length();
		
		// form padding string
		
		StringBuffer pad = new StringBuffer();
		String emptyChar = " ";
		for(int i = 0; i < numPad; i++)
			pad.append(emptyChar);
		String padString = pad.toString();
		
		// place pad string buffer based on desired alignment
		
		if(alignment == TextAlignment.LEFT_ALIGNED)
			return text + padString;
		else if(alignment == TextAlignment.RIGHT_ALIGNED)
			return padString + text;
		else 
		{
			// center aligned
			
			// split pad into 2 pieces (roughly) before and after input string
			
			// if pad.length() is odd, then there will be one extra space, so 
			// decide where to put the extra space (either before or after the text)
			// based on whether the desired line width is odd or even
			//    -if lineWidth is odd, the extra space goes before the text
			//    -if lineWidth is even, the extra space goes after the text
			
			double halfLen = pad.length()/2.0;
			if(lineWidth % 2 == 1)
				halfLen = Math.ceil(halfLen);  // odd
			else
				halfLen = Math.floor(halfLen); // even
			
			return padString.substring(0, (int)halfLen) + text + padString.substring((int)halfLen);
		}
	}
	
	public static void main (String[] args)
	{
		// Simple test of padString() text alignment function
		System.out.println(padString("Test Left Alignment", 100, TextAlignment.LEFT_ALIGNED));
		System.out.println(padString("Test Right Alignment", 100, TextAlignment.RIGHT_ALIGNED));
		System.out.println(padString("Test Center Alignment", 100, TextAlignment.CENTER_ALIGNED));
		
		// mark line width with a .
		System.out.println(padString("", 100, TextAlignment.CENTER_ALIGNED) + "."); 
	}
	
}
