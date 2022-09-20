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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FileOutputLog extends AbstractLogOutput {

	/**
	 * Strings for storing the file paths to the output log files.
	 */
	private String errorFileLoc = null;
	private String warningFileLoc = null;
	private String statusFileLoc = null;
	
	/**
	 * Output file writers for the output log files.
	 */
	private BufferedWriter errorWriter = null;
	private BufferedWriter warningWriter = null;
	private BufferedWriter statusWriter = null;
	
	
	/**
	 * Create error, warning, and status output log files, after first deleting
	 * the contents of any existing log files.  If any path is null or invalid,
	 * then that output file will be skipped.
	 * 
	 * @param logPrefix a string prefix to prepend to output messages (typically a module name)
	 * @param errorFilePath path to error log file
	 * @param warningFilePath path to warning log file
	 * @param statusFilePath path to status log file
	 * @param appendFiles true to append any existing files, false to overwrite
	 * @param header if non-null, write the header to each output file
	 * 
	 * @throws IOException throws notification exception at the end of constructor if any 
	 * problems were encountered (note that execution continues despite any exceptions)
	 */
	protected FileOutputLog(String logPrefix, String errorFilePath, String warningFilePath,
			String statusFilePath, boolean appendFiles, String header) throws IOException {
		
		// set flag to true and throw a notification exception at the end of the constructor
		// if there are issues (but continue with execution despite exceptions)
		boolean exceptionsOccurred = false;
		
		// save prefix
		if(logPrefix != null)
			prefix = logPrefix;
		
		// save file paths
		errorFileLoc = errorFilePath;
		warningFileLoc = errorFilePath;
		statusFileLoc = statusFilePath;
				
		// get the current time to output to log files 
		String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
		String timeHeaderString = newLine + newLine
			+ "********** " + prefix + " Log Created: " 
			+  formatter.format(cal.getTime()) + " **********" + newLine + newLine;
		
		// if header is non-null, add it to the timestamp string
		if(header != null && header.length() > 0)
			timeHeaderString = timeHeaderString + newLine + header + newLine + newLine;
		
		// create file writers but check to see if any file paths are duplicate,
		// and if so, just reuse the writer that was created first (this allows a 
		// user to write multiple types of output to the same file)

		if(errorFilePath != null) {
			try {

				errorWriter = new BufferedWriter(new FileWriter(errorFilePath, appendFiles));
				errorWriter.write(timeHeaderString);
			} catch (Exception e) {
				// output error to screen 
				System.err.println("FileOutputLog Creation FAILURE: Failed to create ERROR log file in: "
						+ errorFilePath);
				e.printStackTrace();
				if(errorWriter != null) {	
					try {
						errorWriter.close();
					} catch (IOException e1) {
						// do nothing
					}
				}
				errorWriter = null;
				exceptionsOccurred = true;
			}
		}

		// now create warning file but check to ensure it's not the same as the error file
		if(warningFilePath == null) {
			// skip if null
		}
		else if(warningFilePath.equalsIgnoreCase(errorFilePath)) {
			// if same file, reuse the writer
			warningWriter = errorWriter;
		} 
		else{
			// create a new warning output file
			
			try {
				warningWriter = new BufferedWriter(new FileWriter(
						warningFilePath, appendFiles));
				warningWriter.write(timeHeaderString);
			} catch (Exception e) {
				// output error to screen 
				System.err.println("FileOutputLog Creation FAILURE: Failed to create WARNING log file in: "
								+ warningFilePath);
				e.printStackTrace();
				if(warningWriter != null) {	
					try {
						warningWriter.close();
					} catch (IOException e1) {
						// do nothing
					}
				}
				warningWriter = null;
				exceptionsOccurred = true;
			}
		}

		// now create status file but check to ensure it's not the same as the error file or warning file
		if(statusFilePath == null) {
			// skip if null
		}
		else if(statusFilePath.equalsIgnoreCase(errorFilePath)) {
			// if same file, reuse the writer
			statusWriter = errorWriter;
		}
		else if(statusFilePath.equalsIgnoreCase(warningFilePath)) {
			// if same file, reuse the writer
			statusWriter = warningWriter;
		}
		else{
			// create a new status output file
			
			try {
				statusWriter = new BufferedWriter(new FileWriter(
						statusFilePath, appendFiles));
				statusWriter.write(timeHeaderString);
			} catch (Exception e) {
				// output error to screen 
				System.err.println("FileOutputLog Creation FAILURE: Failed to create STATUS log file in: "
								+ statusFilePath);
				e.printStackTrace();
				if(statusWriter != null) {	
					try {
						statusWriter.close();
					} catch (IOException e1) {
						// do nothing
					}
				}
				statusWriter = null;
				exceptionsOccurred = true;
			}
		}
		
		// throw an exception just to notify callers, even though execution completed successfully
		if(exceptionsOccurred == true) {
			throw new IOException("FileOutputLog Error: One or more exceptions encountered in constructor. "
					+ "See terminal window for details.");
		}
		
	}

	/**
	 * Output message and stack trace to error log file.
	 */
	@Override
	protected boolean outputError(Exception exception) {
		boolean flag = outputError(exception.getMessage());
		outputExceptionStackTrace(errorWriter, exception);
		return flag;
	}

	/**
	 * Output message to error log file.
	 */
	@Override
	protected boolean outputError(String msg) {

		if (errorWriter == null) {
			return false;
		}
		
		try {
			errorWriter.write(newLine 
					+ "-" + prefix + " Error-" + newLine + msg + newLine);
			errorWriter.flush();

		} catch (IOException e) {
			System.err.println("FileOutputLog Error: Failed to write to ERROR log file in: "
					+ errorFileLoc);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Output message and stack trace to status log file.
	 */
	@Override
	protected boolean outputStatus(Exception exception) {
		boolean flag = outputStatus(exception.getMessage());
		outputExceptionStackTrace(statusWriter, exception);
		return flag;
	}

	/**
	 * Output message to status log file.
	 */
	@Override
	protected boolean outputStatus(String msg) {

		if (statusWriter == null) {
			return false;
		}
		
		try {
			statusWriter.write(newLine 
					+ "-" + prefix + " Status-" + newLine + msg + newLine);
			statusWriter.flush();

		} catch (IOException e) {
			System.err.println("FileOutputLog Error: Failed to write to STATUS log file in: "
					+ statusFileLoc);
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	/**
	 * Output message and stack trace to warning log file.
	 */
	@Override
	protected boolean outputWarning(Exception exception) {
		boolean flag = outputWarning(exception.getMessage());
		outputExceptionStackTrace(warningWriter, exception);
		return flag;
	}
	
	/**
	 * Output message to warning log file.
	 */
	@Override
	protected boolean outputWarning(String msg) {

		if (warningWriter == null) {
			return false;
		}
		
		try {
			warningWriter.write(newLine 
					+ "-" + prefix + " Warning-" + newLine + msg + newLine);
			warningWriter.flush();

		} catch (IOException e) {
			System.err.println("FileOutputLog Error: Failed to write to WARNING log file in: "
					+ warningFileLoc);
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Output exception stack trace using the given writer.
	 * 
	 * @param writer writer to output stack trace with (writes to file)
	 * @param exception exception 
	 */
	private void outputExceptionStackTrace(Writer writer, Exception exception) {
		
		synchronized (writer) {
			try {
				writer.write("    " + exception.getClass().getName() + newLine);

				StackTraceElement[] trace = exception.getStackTrace();
				for (int i=0; i < trace.length; i++)
					writer.write("        at " + trace[i] + newLine);

				writer.flush();
				
			} catch (IOException e) {
				// do nothing
			}
		}
	}

}
