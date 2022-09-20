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
package gov.sandia.gmp.util.globals;

/**
 * @author sballar
 * 
 */
public enum OSType
{
	WINDOWS, MACOSX, SUNOS, LINUX,  UNIX, UNRECOGNIZED;
	
	public String toString()
	{
		switch (this)
		{
		case WINDOWS:
			return "Windows";
		case MACOSX:
			return "MacOSX";
		case SUNOS:
			return "SunOS";
		case LINUX:
			return "Linux";
		case UNIX:
			return "Unix";
		default:
			return "Unrecognized";
		}
	}
	
	/**
	 * <ul>
	 * Return the operating system on which Utils is currently running:
	 * <li>OS.WINDOWS
	 * <li>OS.MAC
	 * <li>OS.UNIX
	 * <li>OS.LINUX
	 * <li>OS.UNRECOGNIZED
	 * </ul>
	 * <p>
	 * Here is a pretty comprehensive list of possible os.name values:
	 * http://lopica.sourceforge.net/os.html
	 * 
	 * @return current operating system
	 */
	public static OSType getOS()
	{
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win"))
			return OSType.WINDOWS;
		else if (os.contains("mac"))
			return OSType.MACOSX;
		else if (os.contains("linux"))
			return OSType.LINUX;
		else if (os.contains("unix"))
			return OSType.UNIX;
		else if (os.contains("sun") || os.contains("solaris"))
			return OSType.SUNOS;

		return OSType.UNRECOGNIZED;
	}

}
