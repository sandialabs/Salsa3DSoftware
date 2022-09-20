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

public class TerminalOutputLog extends AbstractLogOutput {

	
	/**
	 * Create a terminal output log which will print messages to the
	 * terminal window.  Note that all messages are output to System.err
	 * in order to avoid timing issues when mixing output with System.out.
	 * 
	 * @param modulePrefix a string prefix to prepend to output messages (typically a module name)
	 */
	public TerminalOutputLog(String modulePrefix) {
		if(modulePrefix != null)
			prefix = modulePrefix;
	}
	
	/**
	 * Output error message to screen.
	 */
	@Override
	public boolean outputError(String msg) {
		System.err.println(newLine + "-"
				+ prefix + " Error-" + newLine + msg);
		return true;
	}

	/**
	 * Output status message to screen.
	 */
	@Override
	public boolean outputStatus(String msg) {
		System.err.println(newLine + "-"
				+ prefix + " Status-" + newLine + msg);
		return true;
	}

	/**
	 * Output warning message to screen.
	 */
	@Override
	public boolean outputWarning(String msg) {
		System.err.println(newLine + "-"
				+ prefix + " Warning-" + newLine + msg);
		return true;
	}


	/**
	 * Output error message and stack trace to screen.
	 */
	@Override
	public boolean outputError(Exception exception) {
		// output message and stack trace
		outputError(exception.getMessage());
		outputExceptionStackTrace(exception);
		return false;
	}

	/**
	 * Output status message and stack trace to screen.
	 */
	@Override
	public boolean outputStatus(Exception exception) {
		// output message and stack trace
		outputStatus(exception.getMessage());
		outputExceptionStackTrace(exception);
		return false;
	}

	/**
	 * Output warning message and stack trace to screen.
	 */
	@Override
	public boolean outputWarning(Exception exception) {
		// output message and stack trace
		outputWarning(exception.getMessage());
		outputExceptionStackTrace(exception);
		return false;
	}

	/**
	 * Output exception's stack trace to screen.
	 * 
	 * @param exception exception
	 */
	private void outputExceptionStackTrace(Exception exception) {
		exception.printStackTrace();
	}



}
