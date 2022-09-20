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
package gov.sandia.gmp.util.headerinfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class HeaderInfo
{

	ArrayList<String> javaHeader;
	
	ArrayList<String> cHeader;
	
	int nFilesModified;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

		try
		{
			HeaderInfo hi = new HeaderInfo();
			hi.run(args);
			System.out.printf("HeaderInfo modified %d files.%n", hi.nFilesModified);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	protected void run(String[] args) throws IOException
	{
		nFilesModified = 0;
		loadHeaders(args[0]);
		for (int i=1; i<args.length; ++i)
			replaceHeader(new File(args[i]));
	}

	private void replaceHeader(File dir) throws IOException
	{
		//System.out.println("Processing directory "+dir.getCanonicalPath());
		for (File f : dir.listFiles())
			if (f.isDirectory())
				replaceHeader(f);
			else if (f.isFile())
			{
				if (f.getName().endsWith(".java"))
					replace(f, "package ");
				else if (f.getName().endsWith(".c"))
					replace(f, "#include ");
				else if (f.getName().endsWith(".cc"))
					replace(f, "#ifndef ", "#include ");
				else if (f.getName().endsWith(".h"))
					replace(f, "#ifndef ", "#include ");
			}

	}

	private void replace(File f, String ... triggers) throws IOException
	{
		ArrayList<String> contents = new ArrayList<String>(1000);
		Scanner input = new Scanner(f);
		String line;
		boolean found = false;
		int linenum = 1;
		int nlines = 0;
		while (input.hasNext())
		{
			line = input.nextLine();
			if (!found)
				for (int j=0; j<triggers.length; ++j)
					if (line.startsWith(triggers[j]))
						found = true;

			if (found)
				contents.add(line);
			else
				++linenum;

			++nlines;
		}

		input.close();

		if (!found)
		System.out.printf("%6d / %6d  %s%n", linenum, nlines, f.getName());
		
		ArrayList<String> header = null;
		if (f.getName().endsWith(".java"))
			header = javaHeader;
		else
			header = cHeader;

		BufferedWriter output = new BufferedWriter(new FileWriter(f));
		for (int i=0; i<header.size(); ++i)
		{
			output.write(header.get(i));
			output.newLine();
		}
		for (int i=0; i<contents.size(); ++i)
		{
			output.write(contents.get(i));
			output.newLine();
		}
		++nFilesModified;
		output.close();
	}

	protected void loadHeaders(String file) throws IOException
	{
		ArrayList<String> header = new ArrayList<String>();
		Scanner input = new Scanner(new File(file));
		while (input.hasNext())
			header.add(input.nextLine());
		input.close();
		
		while (header.size() > 0 && header.get(0).trim().length()==0)
			header.remove(0);
		while (header.size() > 0 && header.get(header.size()-1).trim().length()==0)
			header.remove(header.size()-1);
		
		javaHeader = new ArrayList<String>(header.size()+2);
		
		javaHeader.add("/**");
		
		for (String line : header)
			javaHeader.add(" * "+line);
		
		javaHeader.add(" */");
		
		cHeader = new ArrayList<String>(header.size()+3);
		
		cHeader.add("//- ****************************************************************************");
		cHeader.add("//- ");
		
		for (String line : header)
			cHeader.add("//- "+line);
		
		cHeader.add("//-");
		cHeader.add("//- ****************************************************************************");
		cHeader.add("");
	}
}
