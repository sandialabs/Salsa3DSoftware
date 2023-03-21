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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Buff extends LinkedHashMap<String, String> {

    private static final long serialVersionUID = -7519424899477784508L;

    private String className;

    private List<Buff> moreBuffers;

    /**
     * 
     */
    public Buff(String className) {
	super();
	put("className", className);
	moreBuffers = new ArrayList<>();
    }

    /**
     * Read tokens into the buffer until end-of-file or blank line is encountered
     * @param input
     * @throws Exception 
     */
    public Buff(Scanner input) {
	this(split(input.nextLine())[1]); 
	while (input.hasNextLine()) {
	    String line = input.nextLine();
	    if (line.length() == 0)
		break;
	    String[] tokens = split(line);
	    put(tokens[0].trim(), tokens[1].trim());
	}
    }

    public void add(String variable, long value) { put(variable, String.format("%d", value)); }
    public void add(String variable, boolean value) { put(variable, String.format("%b", value)); }
    
    public void add(String variable, String value) { 
	put(variable, value.replaceAll("\n", "<BR>")); 
    }

    public void add(String variable, double value) { put(variable, String.format("%1.3f", value)); }

    public void add(String variable, double value, String format) { 
	String s = String.format(format, value);
	// if value is -0.0  change it to 0.0
	if (s.startsWith("-")
		&& !s.contains("9")
		&& !s.contains("8")
		&& !s.contains("7")
		&& !s.contains("6")
		&& !s.contains("5")
		&& !s.contains("4")
		&& !s.contains("3")
		&& !s.contains("2")
		&& !s.contains("1")
		) s = s.substring(1);
	put(variable, s); 
    }

    public void add(String variable, double value, int precision) {
	add(variable, value, String.format("%%1.%df", precision)); 
    }

    public void add(Buff more) { moreBuffers.add(more);	}

    @SuppressWarnings("unchecked")
    public void insert(Buff other) { 
	LinkedHashMap<String, String> copy = (LinkedHashMap<String, String>) other.clone();
	copy.remove("className");
	for (java.util.Map.Entry<String, String> e : copy.entrySet())
	    add(e.getKey(), e.getValue());
    }

    public String getString(String variable) { return get(variable); }

    public Boolean getBoolean(String variable) {
	return Boolean.valueOf(get(variable)); 
    }

    public Integer getInt(String variable) { return Integer.valueOf(get(variable)); } 

    public Long getLong(String variable) { return Long.valueOf(get(variable)); } 

    public Double getDouble(String variable) { return Double.valueOf(get(variable)); } 

    public Float getFloat(String variable) { return Float.valueOf(get(variable)); } 

    @Override public String toString() {

	StringBuffer buf = new StringBuffer();
	for (java.util.Map.Entry<String, String> e : entrySet())
	    buf.append(String.format("%s = %s%n", e.getKey(), e.getValue()));

	buf.append("\n");

	for (Buff b : moreBuffers)
	    buf.append(b);

	return buf.toString(); 
    }

    public String getClassName() {
	return className;
    }

    public void setClassName(String className) {
	this.className = className;
    }

    /**
     * split s at equal sign and return the two halves, trimmed.
     * @param s
     * @return String[] key-value pair
     * @throws Exception
     */
    private static String[] split(String s) {
	s = s.replaceAll("\n", "<BR>");
	int i=s.indexOf('=');
	if (i <= 0)
	    System.out.println(("Error in Buff: no '=' in String s = "+s));
	return new String[] {s.substring(0,i).trim(), s.substring(i+1).trim()};
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((className == null) ? 0 : className.hashCode());
	result = prime * result + ((moreBuffers == null) ? 0 : moreBuffers.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Buff other = (Buff) obj;
	if (className == null) {
	    if (other.className != null)
		return false;
	} else if (!className.equals(other.className))
	    return false;
	if (moreBuffers == null) {
	    if (other.moreBuffers != null)
		return false;
	} else if (!moreBuffers.equals(other.moreBuffers))
	    return false;
	return true;
    }

    public static String compare(Buff exp, Buff out) {
	Set<String> union = new LinkedHashSet<>(Math.max(exp.size(), out.size()));
	union.addAll(exp.keySet());
	union.addAll(out.keySet());

	StringBuffer diff = new StringBuffer();
	for (String key : union) {
	    String v1 = exp.get(key);
	    String v2 = out.get(key);
	    if ((v1 == null && v2 == null) || 
		    (v1 != null && v2 != null && v1.equals(v2)))
		diff.append(String.format("   %-15s %s%n", key, v1));
	    else
		diff.append(String.format("*  %-15s %s   %s%n", key, v1, v2));
	}
	diff.append("\n");
	for (int i=0; i< Math.min(out.moreBuffers.size(), exp.moreBuffers.size()); ++i) {
	    diff.append(compare(exp.moreBuffers.get(i), out.moreBuffers.get(i)));
	}
	return diff.toString();
    }

    public static boolean compare(Buff exp, Buff out, PrintStream outputBuffer) {
	Set<String> union = new LinkedHashSet<>(Math.max(exp.size(), out.size()));
	union.addAll(exp.keySet());
	union.addAll(out.keySet());
	boolean equal = true;

	for (String key : union) {
	    String v1 = exp.get(key);
	    String v2 = out.get(key);
	    boolean same = (v1 == null && v2 == null) || (v1 != null && v2 != null && v1.equals(v2));
	    equal = equal && same;

	    if (outputBuffer != null)
		if (!same) {
		    equal = false;
		    outputBuffer.print(String.format("*  %-15s %s   %s%n", key, v1, v2));
		}
		else
		    outputBuffer.print(String.format("   %-15s %s%n", key, v1));
	}

	if (outputBuffer != null) outputBuffer.println();

	equal = equal && exp.moreBuffers.size() == out.moreBuffers.size();

	for (int i=0; i<Math.min(exp.moreBuffers.size(), out.moreBuffers.size()); ++i) {
	    equal = equal && compare(exp.moreBuffers.get(i), out.moreBuffers.get(i), outputBuffer);
	}
	return equal;
    }

}
