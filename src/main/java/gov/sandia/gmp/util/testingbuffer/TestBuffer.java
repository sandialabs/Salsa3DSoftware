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
package gov.sandia.gmp.util.testingbuffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class TestBuffer {

	private StringBuffer buffer;

	/**
	 * A map from a variable name (e.g. latitude) to a precision value (e.g. 0.0001) where
	 * the precision value specifies the tolerance that will be used when comparing two 
	 * values of type double.
	 * 
	 * <p>Note that there are two possible states for this map, high precision or low 
	 * precision.  Low precision is the default.  High precision can be specified by 
	 * setting environment variable HIGH_RESOLUTION = true.
	 * 
	 * <p>The two definitions of the map are located at the bottom of this file.
	 */
	static public Map<String, Double> precision;
	static {
		if (System.getenv("TEST_BUFFER_PRECISION") != null 
				&& System.getenv("TEST_BUFFER_PRECISION").toUpperCase().startsWith("LO")) {
			// here we specify the precision for low resolution tests where the precision 
			// used in comparisons of values of type double is relatively low.
			// If you have tests failing because two values differ by a small amount
			// you can loosen the precision of of value here.
			precision = LowPrecision.precision;
		}
		else {
			// here we specify the precision for high resolution tests where the precision 
			// used in comparisons of values of type double is relatively tight.
			// DO NOT CHANGE THESE PRECISION VALUES
			precision = HighPrecision.precision;
		}
	}
	
	public TestBuffer() {
		buffer = new StringBuffer();
	}

	public TestBuffer(String className) {
		this();
		add("class", className);
	}

	public TestBuffer(File inputFile) throws FileNotFoundException {
		this();
		Scanner scanner = new Scanner(inputFile);
		while (scanner.hasNextLine()) 
			buffer.append(scanner.nextLine()+System.lineSeparator());
		scanner.close();
	}

	public TestBuffer(TestBuffer other) {
		this.buffer = new StringBuffer(other.buffer);
	}

	public TestBuffer(StringBuffer buf) {
		this.buffer = new StringBuffer(buf);
	}

	public void add(String variable, boolean value) { 
		buffer.append(String.format("%s\tboolean\t%b%n", variable, value));
	}
	public void add(String variable, String value) { 
		if (value == null || value.length() == 0)
			value = "null";
		buffer.append(String.format("%s\tstring\t%s%n", variable, 
				value.replace("\r\n","\n").replace("\n", "<BR>")));
	}

	public void add(String variable, long value) { 
		buffer.append(String.format("%s\tlong\t%d%n", variable, value));
	}

	public void add(String variable, Double value) { 
		if (value == null)
			buffer.append(String.format("%s\tstring\tnull%n", variable));
		else
			buffer.append(String.format("%s\tdouble\t%1.15g%n", variable, value));
	}

	public void add() {
		buffer.append(System.lineSeparator());
	}

	public void add(TestBuffer testBuffer) {
		buffer.append(testBuffer.buffer.toString());
	}

	public StringBuffer getBuffer() {
		return buffer;
	}


	@Override
	public String toString() {
		return buffer.toString();
	}
	
	public void toFile(File f) throws IOException {
		FileWriter fw = new FileWriter(f);
		fw.append(toString());
		fw.close();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || !(o instanceof TestBuffer)) { return false; }

		TestBuffer other = (TestBuffer)o;

		boolean result = false;
		try (
				Scanner this_scanner = new Scanner(this.buffer.toString());
				Scanner other_scanner = new Scanner(other.buffer.toString()); 
				) 
		{

			while (this_scanner.hasNextLine() && other_scanner.hasNextLine()) {
				String thisLine = this_scanner.nextLine();
				String otherLine = other_scanner.nextLine();

				if (thisLine.length() == 0 && otherLine.length() == 0)
					continue;

				String[] thisTokens = thisLine.split("\t");
				String[] otherTokens = otherLine.split("\t");

				if (thisTokens.length == 0 ^ otherTokens.length == 0)
					return false;

				if(thisTokens.length < 3)
					return false;
				//throw new RuntimeException(Arrays.toString(thisTokens)+System.lineSeparator()+Arrays.toString(otherTokens));

				// compare variable names
				if (!thisTokens[0].equals(otherTokens[0]))
					return false;

				// compare variable types (double, long, string, boolean)
				if (!thisTokens[1].equals(otherTokens[1]))
					return false;

				// compare the values
				if (thisTokens[1].equals("double")) {
					double maxDifference = Double.NaN;
					try {
						maxDifference = precision.get(thisTokens[0]);
					} catch (Exception e) {
						throw new RuntimeException(String.format("No precision value available for variable %s\n"
								+ "Add precision values to classes gov.sandia.gmp.util.testingbuffer.LowPrecision.java\n"
								+ "and gov.sandia.gmp.util.testingbuffer.HighPrecision.java",thisTokens[0]));
					}

					double v1 = Double.parseDouble(thisTokens[2]);
					double v2 = Double.parseDouble(otherTokens[2]);
					if (Math.abs(v1-v2) >= maxDifference || 
							(Double.isNaN(v1) ^ Double.isNaN(v2))) 
						return false;
				}
				else  {
					if (!thisTokens[2].equals(otherTokens[2])) 
						return false;
				}
			}
			// return true if both scanners have no more lines.
			result = !this_scanner.hasNextLine() && !other_scanner.hasNextLine();
		}
		return result;
	}

	public String compare(TestBuffer outputBuffer) throws Exception {
		StringBuffer output = new StringBuffer();

		try (Scanner thisScanner = new Scanner(buffer.toString()); ) {
			ArrayList<Map<String, String[]>> thisList = new ArrayList<Map<String,String[]>>();
			while (thisScanner.hasNextLine()) {
				Map<String, String[]> map = new LinkedHashMap<>();
				String line = thisScanner.nextLine();
				while (line.length() > 0) {
					String[] tokens = line.split("\t");
					map.put(tokens[0], tokens);
					line = thisScanner.nextLine();
				}
				thisList.add(map);
			}

			Scanner otherScanner = new Scanner(outputBuffer.buffer.toString());
			ArrayList<Map<String, String[]>> otherList = new ArrayList<Map<String,String[]>>();
			while (otherScanner.hasNextLine()) {
				Map<String, String[]> map = new LinkedHashMap<>();
				String line = otherScanner.nextLine();
				while (line.length() > 0) {
					String[] tokens = line.split("\t");
					map.put(tokens[0], tokens);
					line = otherScanner.nextLine();
				}
				otherList.add(map);
			}
			otherScanner.close();

			for (int i=0; i<Math.max(thisList.size(), otherList.size()); ++i) {
				Map<String, String[]> thisMap = i < thisList.size() ? thisList.get(i) : new HashMap<>();
				Map<String, String[]> otherMap = i < otherList.size() ? otherList.get(i) : new HashMap<>();
				Set<String> union = new LinkedHashSet<>();
				union.addAll(thisMap.keySet());
				union.addAll(otherMap.keySet());
				for (String variableName : union) {
					if (variableName.equals("class") && output.length() > 0)
						output.append(System.lineSeparator());
					String[] thisTokens = thisMap.get(variableName);
					String[] otherTokens = otherMap.get(variableName);
					if (thisTokens != null && otherTokens != null) {
						if (thisTokens[1].equals(otherTokens[1])) {
							// the datatypes are equal
							if (thisTokens[1].equalsIgnoreCase("double")) {

								Double maxDifference = precision.get(variableName);
								if (maxDifference == null)
									throw new Exception("no precision available for variable "+variableName);

								long digits = Math.max(0, -Math.round(Math.log10(maxDifference)));

								double thisValue = Double.valueOf(thisTokens[2]);
								double otherValue = Double.valueOf(otherTokens[2]);

								if (Math.abs(thisValue - otherValue) < maxDifference)
									output.append(String.format("   \t%s\t%1."+digits+"f%n", variableName, thisValue));
								else
									output.append(String.format("*  \t%s\t%1."+digits+"f\t%1."+digits+"f\t%1."+(digits+1)+"f%n", 
											variableName, thisValue, otherValue, Math.abs(thisValue-otherValue)));								
							}
							else {
								if (thisTokens[2].equals(otherTokens[2])) 
									output.append(String.format("   \t%s\t%s%n", variableName, thisTokens[2]));
								else
									output.append(String.format("*  \t%s\t%s\t%s%n", variableName, thisTokens[2], otherTokens[2]));								
							}
						}
						else {
							// the datatypes are not equal
							output.append(String.format("*  \t%s\t%s\t%s", variableName, thisTokens[1], otherTokens[1]));
						}
					}
					else {
						try {
							if (thisTokens == null) 
								output.append(String.format("*  \t%s\tnull\t%s%n", variableName, otherTokens[2]));
							else
								output.append(String.format("*  \t%s\t%s\tnull%n", variableName, thisTokens[2]));
						} catch (Exception x) {
							x.printStackTrace();
						}
					}
				}
			}
		}
		return output.toString();
	}

}
